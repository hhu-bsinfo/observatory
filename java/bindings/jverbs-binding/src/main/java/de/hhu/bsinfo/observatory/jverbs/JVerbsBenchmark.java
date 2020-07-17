package de.hhu.bsinfo.observatory.jverbs;

import com.ibm.net.rdma.jverbs.cm.ConnectionEvent;
import com.ibm.net.rdma.jverbs.cm.ConnectionEvent.EventType;
import com.ibm.net.rdma.jverbs.cm.ConnectionId;
import com.ibm.net.rdma.jverbs.cm.ConnectionParameter;
import com.ibm.net.rdma.jverbs.cm.EventChannel;
import com.ibm.net.rdma.jverbs.cm.PortSpace;
import com.ibm.net.rdma.jverbs.verbs.MemoryRegion;
import com.ibm.net.rdma.jverbs.verbs.ScatterGatherElement;
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

public class JVerbsBenchmark extends Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(JVerbsBenchmark.class);

    private static final String PARAM_KEY_QUEUE_SIZE = "queueSize";
    private static final String PARAM_KEY_SVM_OPTIMIZATION = "svmOptimization";

    private static final int DEFAULT_QUEUE_SIZE = 100;

    private boolean svmOptimization;

    private int queueSize;
    private int pendingSendCompletions;
    private int pendingReceiveCompletions;

    private ConnectionParameter connectionParameter;
    private EventChannel eventChannel;
    private ConnectionId connectionId;

    private MemoryRegion sendMemoryRegion;
    private MemoryRegion receiveMemoryRegion;

    private LinkedList<ScatterGatherElement> sendScatterGatherList;
    private LinkedList<ScatterGatherElement> receiveScatterGatherList;

    private MemoryRegionInformation remoteInfo;

    private VerbsWrapper verbs;

    private ConnectionEvent getEvent() throws IOException {
        ConnectionEvent event = eventChannel.getConnectionEvent(-1);

        LOGGER.info("Received event of type [{}]", event.getEventType());

        return event;
    }

    private MemoryRegionInformation exchangeMemoryRegionInformation() throws IOException {
        MemoryRegionInformation localInfo = new MemoryRegionInformation(receiveMemoryRegion.getAddress(), receiveMemoryRegion.getRemoteKey());
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
        svmOptimization = Boolean.parseBoolean(getParameter(PARAM_KEY_SVM_OPTIMIZATION, String.valueOf(true)));

        if(!svmOptimization) {
            LOGGER.warn("Optimized use of stateful verbs methods is disabled");
        }

        connectionParameter = new ConnectionParameter();

        connectionParameter.setInitiatorDepth(1);
        connectionParameter.setResponderResources(1);
        connectionParameter.setRetryCount(7);
        connectionParameter.setRnrRetryCount(7);

        try {
            eventChannel = EventChannel.createEventChannel();
        } catch (IOException e) {
            LOGGER.error("Creating event channel failed", e);
            return Status.NETWORK_ERROR;
        }

        LOGGER.info("Created event channel");

        return Status.OK;
    }

    @Override
    protected Status serve(InetSocketAddress bindAddress) {
        ConnectionEvent event;

        try {
            ConnectionId serverId = ConnectionId.create(eventChannel, PortSpace.RDMA_PS_TCP);
            serverId.bindAddress(bindAddress);

            LOGGER.info("Created connection id");

            serverId.listen(0);

            LOGGER.info("Waiting for incoming connection request");

            event = getEvent();

            if(event.getEventType() != EventType.RDMA_CM_EVENT_CONNECT_REQUEST) {
                LOGGER.error("Event has wrong type (Got [{}], Expected [{}])", event.getEventType(), EventType.RDMA_CM_EVENT_CONNECT_REQUEST);
                return Status.NETWORK_ERROR;
            }

            connectionId = event.getConnectionId();

            eventChannel.ackConnectionEvent(event);

            LOGGER.info("Establishing connection");

            verbs = new VerbsWrapper(connectionId, queueSize, svmOptimization);

            connectionId.accept(connectionParameter);

            event = getEvent();

            if(event.getEventType() != EventType.RDMA_CM_EVENT_ESTABLISHED) {
                LOGGER.error("Event has wrong type (Got [{}], Expected [{}])", event.getEventType(), EventType.RDMA_CM_EVENT_CONNECT_REQUEST);
                return Status.NETWORK_ERROR;
            }

            eventChannel.ackConnectionEvent(event);

            LOGGER.info("Successfully connected to {}", connectionId.getDestinationAddress());

            eventChannel.destroyEventChannel();

            // Do not destroy the connection id, as this will hang up the application for some reason.
            // The following line can be uncommented, once this bug is fixed in the IBM SDK.

            // serverId.destroy();

            return Status.OK;
        } catch (IOException e) {
            LOGGER.error("Connecting to client failed", e);
            return Status.NETWORK_ERROR;
        }
    }

    @Override
    protected Status connect(InetSocketAddress bindAddress, InetSocketAddress serverAddress) {
        ConnectionEvent event;

        try {
            connectionId = ConnectionId.create(eventChannel, PortSpace.RDMA_PS_TCP);

            LOGGER.info("Created connection id");

            LOGGER.info("Resolving server address");

            connectionId.resolveAddress(bindAddress, serverAddress, 5000);

            event = getEvent();

            if(event.getEventType() != EventType.RDMA_CM_EVENT_ADDR_RESOLVED) {
                LOGGER.error("Event has wrong type (Got [{}], Expected [{}])", event.getEventType(), EventType.RDMA_CM_EVENT_ADDR_RESOLVED);
                return Status.NETWORK_ERROR;
            }

            eventChannel.ackConnectionEvent(event);

            LOGGER.info("Resolving route");

            connectionId.resolveRoute(5000);

            event = getEvent();

            if(event.getEventType() != EventType.RDMA_CM_EVENT_ROUTE_RESOLVED) {
                LOGGER.error("Event has wrong type (Got [{}], Expected [{}])", event.getEventType(), EventType.RDMA_CM_EVENT_ROUTE_RESOLVED);
                return Status.NETWORK_ERROR;
            }

            eventChannel.ackConnectionEvent(event);

            LOGGER.info("Establishing connection");

            verbs = new VerbsWrapper(connectionId, queueSize, svmOptimization);

            connectionId.connect(connectionParameter);

            event = getEvent();

            if(event.getEventType() != EventType.RDMA_CM_EVENT_ESTABLISHED) {
                LOGGER.error("Event has wrong type (Got [{}], Expected [{}])", event.getEventType(), EventType.RDMA_CM_EVENT_ESTABLISHED);
                return Status.NETWORK_ERROR;
            }

            eventChannel.ackConnectionEvent(event);

            LOGGER.info("Successfully connected to {}", connectionId.getDestinationAddress());

            eventChannel.destroyEventChannel();

            return Status.OK;
        } catch (IOException e) {
            LOGGER.error("Connecting to server failed", e);
            return Status.NETWORK_ERROR;
        }
    }

    @Override
    protected Status prepare(int operationSize, int operationCount) {
        try {
            sendMemoryRegion = verbs.registerMemoryRegion(ByteBuffer.allocateDirect(operationSize));
            receiveMemoryRegion = verbs.registerMemoryRegion(ByteBuffer.allocateDirect(operationSize));

            sendScatterGatherList = new LinkedList<>();
            receiveScatterGatherList = new LinkedList<>();

            ScatterGatherElement sendSge = new ScatterGatherElement();
            sendSge.setAddress(sendMemoryRegion.getAddress());
            sendSge.setLength(sendMemoryRegion.getLength());
            sendSge.setLocalKey(sendMemoryRegion.getLocalKey());

            ScatterGatherElement receiveSge = new ScatterGatherElement();
            receiveSge.setAddress(receiveMemoryRegion.getAddress());
            receiveSge.setLength(receiveMemoryRegion.getLength());
            receiveSge.setLocalKey(receiveMemoryRegion.getLocalKey());

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
            verbs.deregisterMemoryRegion(sendMemoryRegion);
            verbs.deregisterMemoryRegion(receiveMemoryRegion);

            // Do not destroy the connection id, as this will hang up the application for some reason.
            // The following line can be uncommented, once this bug is fixed in the IBM SDK.

            // connectionId.destroy();

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
    protected Status receiveMultipleMessages(int messageCount) {
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
