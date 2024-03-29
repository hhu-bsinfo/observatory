package de.hhu.bsinfo.observatory.jucx;

import de.hhu.bsinfo.observatory.benchmark.Benchmark;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Stack;

import org.openucx.jucx.ucp.UcpConnectionRequest;
import org.openucx.jucx.ucp.UcpContext;
import org.openucx.jucx.ucp.UcpEndpoint;
import org.openucx.jucx.ucp.UcpEndpointParams;
import org.openucx.jucx.ucp.UcpListener;
import org.openucx.jucx.ucp.UcpListenerParams;
import org.openucx.jucx.ucp.UcpMemory;
import org.openucx.jucx.ucp.UcpParams;
import org.openucx.jucx.ucp.UcpRemoteKey;
import org.openucx.jucx.ucp.UcpRequest;
import org.openucx.jucx.ucp.UcpWorker;
import org.openucx.jucx.ucp.UcpWorkerParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JucxBenchmark extends Benchmark {
    private static final Logger LOGGER = LoggerFactory.getLogger(JucxBenchmark.class);
    private static final String PARAM_KEY_QUEUE_SIZE = "queueSize";
    private static final int DEFAULT_QUEUE_SIZE = 1000;

    private int queueSize;

    private UcpContext context;
    private UcpWorker worker;
    private UcpListener listener;
    private UcpEndpoint clientToServer, serverToClient;
    private UcpMemory sendMemory, recvMemory;
    private UcpConnectionRequest connectionRequest;
    private UcpRemoteKey remoteKey;
    private UcpRequest[] requests;

    private long remoteAddress;
    private final Stack<Closeable> resources = new Stack<>();

    @Override
    protected Status initialize() {
        LOGGER.info("Initializing...");
        queueSize = getParameter(PARAM_KEY_QUEUE_SIZE, DEFAULT_QUEUE_SIZE);

        UcpParams params = new UcpParams().requestRmaFeature()
                .requestStreamFeature().requestTagFeature().requestWakeupFeature();
        context = new UcpContext(params);
        worker = context.newWorker(new UcpWorkerParams());

        resources.add(context);
        resources.add(worker);

        return Status.OK;
    }

    @Override
    protected Status serve(InetSocketAddress bindAddress) {
       LOGGER.info("Listener started on {}", bindAddress);

       UcpListenerParams listenerParams = new UcpListenerParams().setSockAddr(bindAddress)
                .setConnectionHandler(request -> this.connectionRequest = request);
        listener = worker.newListener(listenerParams);
        resources.add(listener);

        while (this.connectionRequest == null) {
            try {
                if (worker.progress() == 0) {
                    worker.waitForEvents();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to progress worker while waiting for an incoming connection", e);
                return Status.NETWORK_ERROR;
            }
        }

        UcpEndpointParams endpointParams = new UcpEndpointParams().setConnectionRequest(connectionRequest)
                .setPeerErrorHandlingMode();
        serverToClient = worker.newEndpoint(endpointParams);

        // Exchange small message to wire-up connection
        ByteBuffer buffer = ByteBuffer.allocateDirect(8);

        try {
            worker.progressRequest(worker.recvTaggedNonBlocking(buffer, null));
            worker.progressRequest(serverToClient.sendTaggedNonBlocking(buffer, null));
        } catch (Exception e) {
            LOGGER.error("Failed to progress worker while wiring up the connection", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status connect(InetSocketAddress bindAddress, InetSocketAddress serverAddress) {
        LOGGER.info("Connecting to {}", serverAddress);

        UcpEndpointParams epParams = new UcpEndpointParams().setSocketAddress(serverAddress)
                .setPeerErrorHandlingMode();
        clientToServer = worker.newEndpoint(epParams);

        // Exchange small message to wire-up connection
        ByteBuffer buffer = ByteBuffer.allocateDirect(8);

        try {
            worker.progressRequest(clientToServer.sendTaggedNonBlocking(buffer, null));
            worker.progressRequest(worker.recvTaggedNonBlocking(buffer, null));
        } catch (Exception e) {
            LOGGER.error("Failed to progress worker while wiring up the connection", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    private void exchangeMemoryInformation() throws Exception {
        UcpEndpoint endpoint = (clientToServer != null) ? clientToServer : serverToClient;
        ByteBuffer recvMemoryRkey = recvMemory.getRemoteKeyBuffer();
        ByteBuffer sendMessage = ByteBuffer.allocateDirect(8 + recvMemoryRkey.capacity());
        ByteBuffer recvMessage = ByteBuffer.allocateDirect(4096);

        sendMessage.putLong(recvMemory.getAddress());
        sendMessage.put(recvMemoryRkey);
        ((Buffer) sendMessage).clear();

        UcpRequest sendRequest = endpoint.sendStreamNonBlocking(sendMessage, null);
        UcpRequest recvRequest = endpoint.recvStreamNonBlocking(recvMessage, 0L, null);

        worker.progressRequest(sendRequest);
        worker.progressRequest(recvRequest);

        remoteAddress = recvMessage.getLong();
        remoteKey = endpoint.unpackRemoteKey(recvMessage);

        resources.add(endpoint);
        resources.add(remoteKey);
    }

    @Override
    protected Status prepare(int operationSize, int operationCount) {
        LOGGER.info("Preparing memory regions and exchanging metadata");
        requests = new UcpRequest[operationCount];

        sendMemory = context.registerMemory(ByteBuffer.allocateDirect(operationSize));
        recvMemory = context.registerMemory(ByteBuffer.allocateDirect(operationSize));

        resources.add(sendMemory);
        resources.add(recvMemory);

        try {
            exchangeMemoryInformation();
        }  catch (Exception e) {
            LOGGER.error("Failed to exchange memory information", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status cleanup() {
        while (!resources.isEmpty()) {
            try {
                resources.pop().close();
            } catch (IOException e) {
                return Status.IO_ERROR;
            }
        }
        return Status.OK;
    }

    @Override
    protected Status fillReceiveQueue() {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status sendMultipleMessages(int messageCount) {
        int batch = Math.min(queueSize, messageCount);
        int completed = 0;

        UcpEndpoint endpoint = (clientToServer != null) ? clientToServer : serverToClient;

        while (completed < messageCount) {
            int pendingCompletion = Math.min(batch, messageCount - completed);

            for (int i = 0; i < pendingCompletion; i++) {
                requests[i] = endpoint.sendTaggedNonBlocking(sendMemory.getAddress(),
                        sendMemory.getLength(), 0, null);
            }

            try {
                for (int i = 0; i < pendingCompletion; i++) {
                    if (!requests[i].isCompleted()) {
                        worker.progressRequest(requests[i]);
                    }

                    completed++;
                }
            }  catch (Exception e) {
                LOGGER.error("Failed to progress worker while sending a message", e);
                return Status.NETWORK_ERROR;
            }
        }

        return Status.OK;
    }

    @Override
    protected Status receiveMultipleMessages(int messageCount) {
        int batch = Math.min(queueSize, messageCount);
        int completed = 0;

        while (completed < messageCount) {
            int pendingCompletion = Math.min(batch, messageCount - completed);

            for (int i = 0; i < pendingCompletion; i++) {
                requests[i] =  worker.recvTaggedNonBlocking(recvMemory.getAddress(),
                  recvMemory.getLength(), 0, 0, null);
            }

            try {
                for (int i = 0; i < pendingCompletion; i++) {
                    if (!requests[i].isCompleted()) {
                        worker.progressRequest(requests[i]);
                    }
                    completed++;
                }
            }  catch (Exception e) {
                LOGGER.error("Failed to progress worker while receiving a message", e);
                return Status.NETWORK_ERROR;
            }
        }

        return Status.OK;
    }

    @Override
    protected Status performMultipleRdmaOperations(RdmaMode mode, int operationCount) {
        int batch = Math.min(queueSize, operationCount);
        int completed = 0;

        UcpEndpoint endpoint = (clientToServer != null) ? clientToServer : serverToClient;

        while (completed < operationCount) {
            int pendingCompletion = Math.min(batch, operationCount - completed);

            for (int i = 0; i < pendingCompletion; i++) {
                if (mode == RdmaMode.READ) {
                    endpoint.getNonBlockingImplicit(remoteAddress, remoteKey,
                      recvMemory.getAddress(), recvMemory.getLength());
                } else {
                    endpoint.putNonBlockingImplicit(sendMemory.getAddress(), sendMemory.getLength(),
                      remoteAddress, remoteKey);
                }
            }

            try {
                worker.progressRequest(endpoint.flushNonBlocking(null));
            }  catch (Exception e) {
                LOGGER.error("Failed to progress worker while waiting for RDMA operations to complete", e);
                return Status.NETWORK_ERROR;
            }

            completed += pendingCompletion;
        }

        return Status.OK;
    }

    @Override
    protected Status sendSingleMessage() {
        return sendMultipleMessages(1);
    }

    @Override
    protected Status performSingleRdmaOperation(RdmaMode mode) {
        return performMultipleRdmaOperations(mode, 1);
    }

    @Override
    protected Status performPingPongIterationServer() {
        sendMultipleMessages(1);
        receiveMultipleMessages(1);

        return Status.OK;
    }

    @Override
    protected Status performPingPongIterationClient() {
        receiveMultipleMessages(1);
        sendMultipleMessages(1);

        return Status.OK;
    }
}
