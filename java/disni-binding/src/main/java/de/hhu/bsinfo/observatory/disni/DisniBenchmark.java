package de.hhu.bsinfo.observatory.disni;

import com.ibm.disni.verbs.IbvMr;
import com.ibm.disni.verbs.IbvSge;
import com.ibm.disni.verbs.RdmaCm;
import com.ibm.disni.verbs.RdmaCmEvent;
import com.ibm.disni.verbs.RdmaCmEvent.EventType;
import com.ibm.disni.verbs.RdmaCmId;
import com.ibm.disni.verbs.RdmaConnParam;
import com.ibm.disni.verbs.RdmaEventChannel;
import de.hhu.bsinfo.observatory.benchmark.Benchmark;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisniBenchmark extends Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(DisniBenchmark.class);

    private static final String PARAM_KEY_QUEUE_SIZE = "queueSize";

    private static final int DEFAULT_QUEUE_SIZE = 100;

    private int queueSize;
    private int pendingSendCompletions;
    private int pendingReceiveCompletions;

    private RdmaConnParam connectionParameter;
    private RdmaEventChannel eventChannel;
    private RdmaCmId connectionId;

    private IbvMr sendMemoryRegion;
    private IbvMr receiveMemoryRegion;

    private LinkedList<IbvSge> sendScatterGatherList;
    private LinkedList<IbvSge> receiveScatterGatherList;

    private MemoryRegionInformation remoteInfo;

    private VerbsWrapper verbs;

    private RdmaCmEvent getEvent() throws IOException {
        RdmaCmEvent event = eventChannel.getCmEvent(-1);

        LOGGER.info("Received event of type [{}]", event.getEvent());

        return event;
    }

    private MemoryRegionInformation exchangeMemoryRegionInformation() throws IOException {
        MemoryRegionInformation localInfo = new MemoryRegionInformation(receiveMemoryRegion.getAddr(), receiveMemoryRegion.getRkey());
        byte[] remoteBytes = new byte[MemoryRegionInformation.getSizeInBytes()];

        LOGGER.info("Sending local memory region information:\n{}", localInfo);

        new DataOutputStream(getOffChannelSocket().getOutputStream()).write(localInfo.toBytes());

        LOGGER.info("Waiting for remote memory region information");

        new DataInputStream(getOffChannelSocket().getInputStream()).readFully(remoteBytes);
        MemoryRegionInformation remoteInfo = MemoryRegionInformation.fromBytes(remoteBytes);

        LOGGER.info("Received remote memory region information:\n{}", remoteInfo);

        return remoteInfo;
    }

    @Override
    protected Status initialize() {
        queueSize = getParameter(PARAM_KEY_QUEUE_SIZE, DEFAULT_QUEUE_SIZE);

        connectionParameter = new RdmaConnParam();

        try {
            this.connectionParameter.setInitiator_depth((byte) 1);
        } catch (Exception e) {
            LOGGER.warn("Unable to set connection parameter 'Initiator depth'!");
        }

        try {
            this.connectionParameter.setResponder_resources((byte) 1);
        } catch (Exception e) {
            LOGGER.warn("Unable to set connection parameter 'Responder resources'!");
        }

        try{
            this.connectionParameter.setRetry_count((byte) 7);
        } catch (Exception e) {
            LOGGER.warn("Unable to set connection parameter 'Retry count'!");
        }

        try {
            this.connectionParameter.setRnr_retry_count((byte) 7);
        } catch (Exception e) {
            LOGGER.warn("Unable to set connection parameter 'RNR Retry count'!");
        }

        try {
            eventChannel = RdmaEventChannel.createEventChannel();
        } catch (IOException e) {
            LOGGER.error("Creating event channel failed", e);
            return Status.NETWORK_ERROR;
        }

        LOGGER.info("Created event channel");

        return Status.OK;
    }

    @Override
    protected Status serve(InetSocketAddress bindAddress) {
        RdmaCmEvent event;

        try {
            RdmaCmId serverId = eventChannel.createId(RdmaCm.RDMA_PS_TCP);
            serverId.bindAddr(bindAddress);

            LOGGER.info("Created connection id");

            serverId.listen(0);

            LOGGER.info("Waiting for incoming connection request");

            event = getEvent();

            if(event.getEvent() != EventType.RDMA_CM_EVENT_CONNECT_REQUEST.ordinal()) {
                LOGGER.error("Event has wrong type (Got [{}], Expected [{}])", event.getEvent(), EventType.RDMA_CM_EVENT_CONNECT_REQUEST.ordinal());
                return Status.NETWORK_ERROR;
            }

            connectionId = event.getConnIdPriv();

            event.ackEvent();

            LOGGER.info("Establishing connection");

            verbs = new VerbsWrapper(connectionId, queueSize);

            connectionId.accept(connectionParameter);

            event = getEvent();

            if(event.getEvent() != EventType.RDMA_CM_EVENT_ESTABLISHED.ordinal()) {
                LOGGER.error("Event has wrong type (Got [{}], Expected [{}])", event.getEvent(), EventType.RDMA_CM_EVENT_CONNECT_REQUEST.ordinal());
                return Status.NETWORK_ERROR;
            }

            event.ackEvent();

            LOGGER.info("Successfully connected to {}", connectionId.getDestination());

            eventChannel.close();
            serverId.destroyId();

            return Status.OK;
        } catch (IOException e) {
            LOGGER.error("Connecting to client failed", e);
            return Status.NETWORK_ERROR;
        }
    }

    @Override
    protected Status connect(InetSocketAddress bindAddress, InetSocketAddress serverAddress) {
        RdmaCmEvent event;

        try {
            connectionId = eventChannel.createId(RdmaCm.RDMA_PS_TCP);

            LOGGER.info("Created connection id");

            LOGGER.info("Resolving server address");

            connectionId.resolveAddr(bindAddress, serverAddress, 5000);

            event = getEvent();

            if(event.getEvent() != EventType.RDMA_CM_EVENT_ADDR_RESOLVED.ordinal()) {
                LOGGER.error("Event has wrong type (Got [{}], Expected [{}])", event.getEvent(), EventType.RDMA_CM_EVENT_ADDR_RESOLVED.ordinal());
                return Status.NETWORK_ERROR;
            }

            event.ackEvent();

            LOGGER.info("Resolving route");

            connectionId.resolveRoute(5000);

            event = getEvent();

            if(event.getEvent() != EventType.RDMA_CM_EVENT_ROUTE_RESOLVED.ordinal()) {
                LOGGER.error("Event has wrong type (Got [{}], Expected [{}])", event.getEvent(), EventType.RDMA_CM_EVENT_ROUTE_RESOLVED.ordinal());
                return Status.NETWORK_ERROR;
            }

            event.ackEvent();

            LOGGER.info("Establishing connection");

            verbs = new VerbsWrapper(connectionId, queueSize);

            connectionId.connect(connectionParameter);

            event = getEvent();

            if(event.getEvent() != EventType.RDMA_CM_EVENT_ESTABLISHED.ordinal()) {
                LOGGER.error("Event has wrong type (Got [{}], Expected [{}])", event.getEvent(), EventType.RDMA_CM_EVENT_ESTABLISHED.ordinal());
                return Status.NETWORK_ERROR;
            }

            event.ackEvent();

            LOGGER.info("Successfully connected to {}", connectionId.getDestination());

            eventChannel.close();

            return Status.OK;
        } catch (IOException e) {
            LOGGER.error("Connecting to server failed", e);
            return Status.NETWORK_ERROR;
        }
    }

    @Override
    protected Status prepare(int operationSize) {
        try {
            sendMemoryRegion = verbs.registerMemoryRegion(ByteBuffer.allocateDirect(operationSize));
            receiveMemoryRegion = verbs.registerMemoryRegion(ByteBuffer.allocateDirect(operationSize));

            sendScatterGatherList = new LinkedList<>();
            receiveScatterGatherList = new LinkedList<>();

            IbvSge sendSge = new IbvSge();
            sendSge.setAddr(sendMemoryRegion.getAddr());
            sendSge.setLength(sendMemoryRegion.getLength());
            sendSge.setLkey(sendMemoryRegion.getLkey());

            IbvSge receiveSge = new IbvSge();
            receiveSge.setAddr(receiveMemoryRegion.getAddr());
            receiveSge.setLength(receiveMemoryRegion.getLength());
            receiveSge.setLkey(receiveMemoryRegion.getLkey());

            sendScatterGatherList.add(sendSge);
            receiveScatterGatherList.add(receiveSge);

            remoteInfo = exchangeMemoryRegionInformation();
        } catch (IOException e) {
            LOGGER.error("Allocating resources failed", e);
            return Status.UNKNOWN_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status cleanup() {
        try {
            verbs.destroy();
            sendMemoryRegion.deregMr();
            receiveMemoryRegion.deregMr();
            connectionId.close();

            return Status.OK;
        } catch (Exception e) {
            LOGGER.error("Destroying resources failed", e);
            return Status.UNKNOWN_ERROR;
        }
    }

    @Override
    protected Status fillReceiveQueue() {
        try {
            int batch = queueSize - pendingReceiveCompletions;

            verbs.receiveMessages(batch, receiveScatterGatherList);
            pendingReceiveCompletions += batch;
        } catch (IOException e) {
            LOGGER.error("Posting receive work request failed");
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status sendMultipleMessages(int messageCount) {
        int remainingMessages = messageCount;

        try {
            while (remainingMessages > 0) {
                // Get the amount of free places in the queue
                int batchSize = queueSize - pendingSendCompletions;

                // Post in batches of 10, so that Stateful Verbs Methods can be reused
                if (batchSize < 10) {
                    int polled = verbs.pollCompletionQueue(Mode.SEND);

                    pendingSendCompletions -= polled;

                    continue;
                }

                if (batchSize > remainingMessages) {
                    batchSize = remainingMessages;

                    verbs.sendMessages(batchSize, sendScatterGatherList);

                    pendingSendCompletions += batchSize;
                    remainingMessages -= batchSize;
                } else {
                    int i = batchSize;

                    while (i >= 10) {
                        verbs.sendMessages(10, sendScatterGatherList);
                        i -= 10;
                    }

                    pendingSendCompletions += batchSize - i;
                    remainingMessages -= batchSize - i;
                }

                // Poll only a single time
                // It is not recommended to poll the completion queue empty, as this mostly costs too much time,
                // which would better be spent posting new work requests
                int polled = verbs.pollCompletionQueue(Mode.SEND);

                pendingSendCompletions -= polled;
            }

            // At the end, poll the completion queue until it is empty
            while (pendingSendCompletions > 0) {
                pendingSendCompletions -= verbs.pollCompletionQueue(Mode.SEND);
            }
        } catch (IOException e) {
            LOGGER.error("Sending messages failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status receiveMultipleMessage(int messageCount) {
        int remainingMessages = messageCount - pendingReceiveCompletions; // Receive queue has already been filled in fillReceiveQueue()

        try {
            while (remainingMessages > 0) {
                // Get the amount of free places in the queue
                int batchSize = queueSize - pendingReceiveCompletions;

                // Post in batches of 10, so that Stateful Verbs Methods can be reused
                if (batchSize < 10) {
                    int polled = verbs.pollCompletionQueue(Mode.RECEIVE);

                    pendingReceiveCompletions -= polled;

                    continue;
                }

                if (batchSize > remainingMessages) {
                    batchSize = remainingMessages;

                    verbs.receiveMessages(batchSize, receiveScatterGatherList);

                    pendingReceiveCompletions += batchSize;
                    remainingMessages -= batchSize;
                } else {
                    int i = batchSize;

                    while (i >= 10) {
                        verbs.receiveMessages(10, receiveScatterGatherList);
                        i -= 10;
                    }

                    pendingReceiveCompletions += batchSize - i;
                    remainingMessages -= batchSize - i;
                }

                // Poll only a single time
                // It is not recommended to poll the completion queue empty, as this mostly costs too much time,
                // which would better be spent posting new work requests
                int polled = verbs.pollCompletionQueue(Mode.RECEIVE);

                pendingReceiveCompletions -= polled;
            }

            // At the end, poll the completion queue until it is empty
            while (pendingReceiveCompletions > 0) {
                pendingReceiveCompletions -= verbs.pollCompletionQueue(Mode.RECEIVE);
            }
        } catch (IOException e) {
            LOGGER.error("Receiving messages failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status performMultipleRdmaOperations(RdmaMode mode, int operationCount) {
        int remainingOperations = operationCount;

        try {
            while (remainingOperations > 0) {
                // Get the amount of free places in the queue
                int batchSize = queueSize - pendingSendCompletions;

                // Post in batches of 10, so that Stateful Verbs Methods can be reused
                if (batchSize < 10) {
                    int polled = verbs.pollCompletionQueue(Mode.SEND);

                    pendingSendCompletions -= polled;

                    continue;
                }

                if (batchSize > remainingOperations) {
                    batchSize = remainingOperations;

                    verbs.executeRdmaOperations(batchSize, sendScatterGatherList, mode, remoteInfo);

                    pendingSendCompletions += batchSize;
                    remainingOperations -= batchSize;
                } else {
                    int i = batchSize;

                    while (i >= 10) {
                        verbs.executeRdmaOperations(10, sendScatterGatherList, mode, remoteInfo);
                        i -= 10;
                    }

                    pendingSendCompletions += batchSize - i;
                    remainingOperations -= batchSize - i;
                }

                // Poll only a single time
                // It is not recommended to poll the completion queue empty, as this mostly costs too much time,
                // which would better be spent posting new work requests
                int polled = verbs.pollCompletionQueue(Mode.SEND);

                pendingSendCompletions -= polled;
            }

            // At the end, poll the completion queue until it is empty
            while (pendingSendCompletions > 0) {
                pendingSendCompletions -= verbs.pollCompletionQueue(Mode.SEND);
            }
        } catch (IOException e) {
            LOGGER.error("Performing RDMA operations failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status sendSingleMessage() {
        int polled;

        try {
            verbs.sendMessages(1, sendScatterGatherList);

            do {
                polled = verbs.pollCompletionQueue(Mode.SEND);
            } while(polled == 0);
        } catch (IOException e) {
            LOGGER.error("Sending single message failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status performSingleRdmaOperation(RdmaMode mode) {
        int polled;

        try {
            verbs.executeRdmaOperations(1, sendScatterGatherList, mode, remoteInfo);

            do {
                polled = verbs.pollCompletionQueue(Mode.SEND);
            } while(polled == 0);
        } catch (IOException e) {
            LOGGER.error("Performing single RDMA operation failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status performPingPongIterationServer() {
        int polled;

        try {
            verbs.sendMessages(1, sendScatterGatherList);

            do {
                polled = verbs.pollCompletionQueue(Mode.SEND);
            } while(polled == 0);

            do {
                polled = verbs.pollCompletionQueue(Mode.RECEIVE);
            } while(polled == 0);

            verbs.receiveMessages(1, receiveScatterGatherList);
        } catch (IOException e) {
            LOGGER.error("Performing ping pong iteration failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status performPingPongIterationClient() {
        int polled;

        try {
            do {
                polled = verbs.pollCompletionQueue(Mode.RECEIVE);
            } while(polled == 0);

            verbs.sendMessages(1, sendScatterGatherList);

            verbs.receiveMessages(1, receiveScatterGatherList);

            do {
                polled = verbs.pollCompletionQueue(Mode.SEND);
            } while(polled == 0);
        } catch (IOException e) {
            LOGGER.error("Performing ping pong iteration failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }
}
