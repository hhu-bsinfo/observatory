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
import de.hhu.bsinfo.observatory.benchmark.result.BenchmarkMode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import de.hhu.bsinfo.observatory.benchmark.result.ThroughputMeasurement;
import de.hhu.bsinfo.observatory.disni.VerbsWrapper.CqType;
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

    private RdmaConnParam connectionParameter;
    private RdmaEventChannel eventChannel;
    private RdmaCmId connectionId;

    private VerbsWrapper verbs;

    private RdmaCmEvent getEvent() throws IOException {
        RdmaCmEvent event = eventChannel.getCmEvent(-1);

        LOGGER.info("Received event of type [{}]", event.getEvent());

        return event;
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
            this.connectionParameter.setRetry_count((byte) 3);
        } catch (Exception e) {
            LOGGER.warn("Unable to set connection parameter 'Retry count'!");
        }

        try {
            this.connectionParameter.setRnr_retry_count((byte) 6);
        } catch (Exception e) {
            LOGGER.warn("Unable to set connection parameter 'RNR Retry count'!");
        }

        try {
            eventChannel = RdmaEventChannel.createEventChannel();
        } catch (IOException e) {
            LOGGER.error("Unable to create event channel");
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

            return Status.OK;
        } catch (IOException e) {
            e.printStackTrace();
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

            return Status.OK;
        } catch (IOException e) {
            e.printStackTrace();
            return Status.NETWORK_ERROR;
        }
    }

    @Override
    protected Status cleanup() {
        try {
            verbs.destroy();
            return Status.OK;
        } catch (Exception e) {
            e.printStackTrace();
            return Status.UNKNOWN_ERROR;
        }
    }

    @Override
    protected Status measureMessagingThroughput(BenchmarkMode mode, ThroughputMeasurement measurement) {
        verbs.resetStatefulCalls();

        try {
            IbvMr memoryRegion = verbs.registerMemoryRegion(ByteBuffer.allocateDirect(measurement.getOperationSize()));

            IbvSge scatterGatherElement = new IbvSge();
            scatterGatherElement.setAddr(memoryRegion.getAddr());
            scatterGatherElement.setLength(memoryRegion.getLength());
            scatterGatherElement.setLkey(memoryRegion.getLkey());

            LinkedList<IbvSge> scatterGatherList = new LinkedList<>();
            scatterGatherList.add(scatterGatherElement);

            if(mode == BenchmarkMode.SEND) {
                return measureSendThroughput(measurement, scatterGatherList);
            } else {
                return measureReceiveThroughput(measurement, scatterGatherList);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return Status.NETWORK_ERROR;
        }
    }

    @Override
    protected Status measureRdmaThroughput(BenchmarkMode mode, RdmaMode rdmaMode, ThroughputMeasurement measurement) {
        return Status.OK;
    }

    private Status measureSendThroughput(ThroughputMeasurement measurement, LinkedList<IbvSge> scatterGatherList) throws IOException {
        int remainingMessages = measurement.getOperationCount();
        int pendingCompletions = 0;

        long startTime = System.nanoTime();

        while(remainingMessages > 0) {
            // Get the amount of free places in the queue
            int batchSize = queueSize - pendingCompletions;

            // Post in batches of 10, so that Stateful Verbs Methods can be reused
            if(batchSize < 10) {
                int polled = verbs.pollCompletionQueue(CqType.RECV_CQ);

                if(polled < 0) {
                    return Status.NETWORK_ERROR;
                }

                pendingCompletions -= polled;

                continue;
            }

            if(batchSize > remainingMessages) {
                batchSize = remainingMessages;

                verbs.sendMessages(batchSize, scatterGatherList);

                pendingCompletions += batchSize;
                remainingMessages -= batchSize;
            } else {
                int i = batchSize;

                while(i >= 10) {
                    verbs.sendMessages(10, scatterGatherList);
                    i -= 10;
                }

                pendingCompletions += batchSize - i;
                remainingMessages -= batchSize - i;
            }

            // Poll only a single time
            // It is not recommended to poll the completion queue empty, as this mostly costs too much time,
            // which would better be spent posting new work requests
            int polled = verbs.pollCompletionQueue(CqType.RECV_CQ);

            if(polled < 0) {
                return Status.NETWORK_ERROR;
            }

            pendingCompletions -= polled;
        }

        // At the end, poll the completion queue until it is empty
        while(pendingCompletions > 0) {
            pendingCompletions -= verbs.pollCompletionQueue(VerbsWrapper.CqType.SEND_CQ);
        }

        measurement.setMeasuredTime(System.nanoTime() - startTime);

        return Status.OK;
    }

    private Status measureReceiveThroughput(ThroughputMeasurement measurement, LinkedList<IbvSge> scatterGatherList) throws IOException {
        // Fill Receive Queue to avoid timeouts on sender side
        verbs.receiveMessages(queueSize, scatterGatherList);
        int pendingCompletions = queueSize;
        int remainingMessages = measurement.getOperationCount() - queueSize;

        long startTime = System.nanoTime();

        while(remainingMessages > 0) {
            // Get the amount of free places in the queue
            int batchSize = queueSize - pendingCompletions;

            // Post in batches of 10, so that Stateful Verbs Methods can be reused
            if(batchSize < 10) {
                int polled = verbs.pollCompletionQueue(CqType.SEND_CQ);

                if(polled < 0) {
                    return Status.NETWORK_ERROR;
                }

                pendingCompletions -= polled;

                continue;
            }

            if(batchSize > remainingMessages) {
                batchSize = remainingMessages;

                verbs.receiveMessages(batchSize, scatterGatherList);

                pendingCompletions += batchSize;
                remainingMessages -= batchSize;
            } else {
                int i = batchSize;

                while(i >= 10) {
                    verbs.receiveMessages(10, scatterGatherList);
                    i -= 10;
                }

                pendingCompletions += batchSize - i;
                remainingMessages -= batchSize - i;
            }

            // Poll only a single time
            // It is not recommended to poll the completion queue empty, as this mostly costs too much time,
            // which would better be spent posting new work requests
            int polled = verbs.pollCompletionQueue(CqType.SEND_CQ);

            if(polled < 0) {
                return Status.NETWORK_ERROR;
            }

            pendingCompletions -= polled;
        }

        measurement.setMeasuredTime(System.nanoTime() - startTime);

        return Status.OK;
    }
}