package de.hhu.bsinfo.observatory.jucx;

import de.hhu.bsinfo.observatory.benchmark.Benchmark;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
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

    UcpContext context;
    UcpWorker worker;
    UcpListener listener;
    UcpEndpoint clientToServer, serverToClient;
    UcpMemory sendMemory, recvMemory;
    UcpConnectionRequest connectionRequest;
    UcpRemoteKey remoteKey;

    long remoteAddress;
    Stack<Closeable> resources = new Stack<>();

    @Override
    protected Status initialize() {
        LOGGER.info("Initializing...");
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
       InetSocketAddress listenAddr = new InetSocketAddress(bindAddress.getAddress(), bindAddress.getPort() + 12);
       LOGGER.info("Listener started on {}", listenAddr);
       UcpListenerParams listenerParams = new UcpListenerParams().setSockAddr(listenAddr)
                .setConnectionHandler(request -> {
                   this.connectionRequest = request;
                });
        listener = worker.newListener(listenerParams);
        resources.add(listener);

        while (this.connectionRequest == null) {
            if (worker.progress() == 0) {
                worker.waitForEvents();
            }
        }

        UcpEndpointParams endpointParams = new UcpEndpointParams().setConnectionRequest(connectionRequest)
                .setPeerErrorHadnlingMode();
        serverToClient = worker.newEndpoint(endpointParams);

        // Exchange small message to wire-up connection
        ByteBuffer buffer = ByteBuffer.allocateDirect(8);
        worker.progressRequest(worker.recvTaggedNonBlocking(buffer, null));
        worker.progressRequest(serverToClient.sendTaggedNonBlocking(buffer, null));

        return Status.OK;
    }

    @Override
    protected Status connect(InetSocketAddress bindAddress, InetSocketAddress serverAddress) {
        InetSocketAddress socketAddress = new InetSocketAddress(serverAddress.getAddress(),
                serverAddress.getPort() + 12);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("Connecting to {}", socketAddress);
        UcpEndpointParams epParams = new UcpEndpointParams().setSocketAddress(socketAddress)
                .setPeerErrorHadnlingMode();
        clientToServer = worker.newEndpoint(epParams);
        // Exchange small message to wire-up connection
        ByteBuffer buffer = ByteBuffer.allocateDirect(8);
        worker.progressRequest(clientToServer.sendTaggedNonBlocking(buffer, null));
        worker.progressRequest(worker.recvTaggedNonBlocking(buffer, null));
        return Status.OK;
    }

    private void exchangeMemoryInformation() {
        UcpEndpoint endpoint = (clientToServer != null) ? clientToServer : serverToClient;
        ByteBuffer recvMemoryRkey = recvMemory.getRemoteKeyBuffer();
        ByteBuffer sendMessage = ByteBuffer.allocateDirect(8 + recvMemoryRkey.capacity());
        ByteBuffer recvMessage = ByteBuffer.allocateDirect(4096);

        sendMessage.putLong(recvMemory.getAddress());
        sendMessage.put(recvMemoryRkey);
        sendMessage.clear();

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
    protected Status prepare(int operationSize) {
        LOGGER.info("Preparing memory regions and exchanging metadata");
        sendMemory = context.registerMemory(ByteBuffer.allocateDirect(operationSize));
        recvMemory = context.registerMemory(ByteBuffer.allocateDirect(operationSize));

        resources.add(sendMemory);
        resources.add(recvMemory);

        exchangeMemoryInformation();
        return Status.OK;
    }

    @Override
    protected Status cleanup() {
        LOGGER.info("Cleanup ...");
        while (!resources.isEmpty()) {
            try {
                resources.pop().close();
            } catch (IOException e) {
                return Status.FILE_ERROR;
            }
        }
        return Status.OK;
    }

    @Override
    protected Status fillReceiveQueue() {
        LOGGER.info("Posting recv request");
        worker.recvTaggedNonBlocking(recvMemory.getAddress(), recvMemory.getLength(), 0, 0, null);
        return Status.OK;
    }

    @Override
    protected Status sendMultipleMessages(int messageCount) {
        LOGGER.info("Sending {} messages", messageCount);
        UcpRequest[] requests = new UcpRequest[messageCount];
        UcpEndpoint endpoint = (clientToServer != null) ? clientToServer : serverToClient;

        for (int i = 0; i < messageCount; i++) {
            requests[i] = endpoint.sendTaggedNonBlocking(sendMemory.getAddress(),
                    sendMemory.getLength(), 0, null);
        }

        for (int i = 0; i < messageCount; i++) {
            if (!requests[i].isCompleted()) {
                worker.progressRequest(requests[i]);
            }
        }

        return Status.OK;
    }

    @Override
    protected Status receiveMultipleMessages(int messageCount) {
        LOGGER.info("Receiving {} messages", messageCount);
        UcpRequest[] requests = new UcpRequest[messageCount];

        for (int i = 0; i < messageCount; i++) {
            requests[i] = worker.recvTaggedNonBlocking(recvMemory.getAddress(),
                    recvMemory.getLength(), 0, 0, null);
        }

        for (int i = 0; i < messageCount; i++) {
            if (!requests[i].isCompleted()) {
                worker.progressRequest(requests[i]);
            }
        }

        return Status.OK;
    }

    @Override
    protected Status performMultipleRdmaOperations(RdmaMode mode, int operationCount) {
        LOGGER.info("Performing {} RDMA {} operations", operationCount, mode);
        UcpEndpoint endpoint = (clientToServer != null) ? clientToServer : serverToClient;

        for (int i = 0; i < operationCount; i++) {
            if (mode == RdmaMode.READ) {
                endpoint.getNonBlockingImplicit(remoteAddress, remoteKey,
                        recvMemory.getAddress(), recvMemory.getLength());
            } else {
               endpoint.putNonBlockingImplicit(sendMemory.getAddress(), sendMemory.getLength(),
                        remoteAddress, remoteKey);
            }
        }

        worker.progressRequest(endpoint.flushNonBlocking(null));

        return Status.OK;
    }

    @Override
    protected Status sendSingleMessage() {
        sendMultipleMessages(1);
        return Status.OK;
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
