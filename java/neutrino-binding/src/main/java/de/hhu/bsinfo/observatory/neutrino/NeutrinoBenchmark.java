package de.hhu.bsinfo.observatory.neutrino;

import de.hhu.bsinfo.neutrino.buffer.RegisteredBuffer;
import de.hhu.bsinfo.neutrino.struct.field.NativeLinkedList;
import de.hhu.bsinfo.neutrino.verbs.AccessFlag;
import de.hhu.bsinfo.neutrino.verbs.CompletionQueue;
import de.hhu.bsinfo.neutrino.verbs.CompletionQueue.WorkCompletionArray;
import de.hhu.bsinfo.neutrino.verbs.ReceiveWorkRequest;
import de.hhu.bsinfo.neutrino.verbs.ScatterGatherElement;
import de.hhu.bsinfo.neutrino.verbs.SendWorkRequest;
import de.hhu.bsinfo.neutrino.verbs.SendWorkRequest.OpCode;
import de.hhu.bsinfo.neutrino.verbs.SendWorkRequest.SendFlag;
import de.hhu.bsinfo.neutrino.verbs.WorkCompletion;
import de.hhu.bsinfo.observatory.benchmark.Benchmark;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeutrinoBenchmark extends Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(NeutrinoBenchmark.class);

    private static final String PARAM_KEY_DEVICE_NUMBER = "deviceNumber";
    private static final String PARAM_KEY_PORT_NUMBER = "portNumber";
    private static final String PARAM_KEY_QUEUE_SIZE = "queueSize";

    private static final int DEFAULT_DEVICE_NUMBER = 0;
    private static final byte DEFAULT_PORT_NUMBER = 1;
    private static final int DEFAULT_QUEUE_SIZE = 100;

    private int queueSize;

    private int pendingSendCompletions;
    private int pendingReceiveCompletions;

    private ConnectionContext context;

    private RegisteredBuffer sendBuffer;
    private RegisteredBuffer receiveBuffer;

    private WorkCompletionArray sendCompletionArray;
    private WorkCompletionArray receiveCompletionArray;

    private ScatterGatherElement sendScatterGatherElement;
    private ScatterGatherElement receiveScatterGatherElement;

    private SendWorkRequest[] sendWorkRequests;
    private ReceiveWorkRequest[] receiveWorkRequests;

    private NativeLinkedList<SendWorkRequest> sendList = new NativeLinkedList<>();
    private NativeLinkedList<ReceiveWorkRequest> receiveList = new NativeLinkedList<>();

    private MemoryRegionInformation remoteInfo;

    @Override
    protected Status initialize() {
        int deviceNumber = getParameter(PARAM_KEY_DEVICE_NUMBER, DEFAULT_DEVICE_NUMBER);
        byte portNumber = getParameter(PARAM_KEY_PORT_NUMBER, DEFAULT_PORT_NUMBER);
        queueSize = getParameter(PARAM_KEY_QUEUE_SIZE, DEFAULT_QUEUE_SIZE);

        sendCompletionArray = new CompletionQueue.WorkCompletionArray(queueSize);
        receiveCompletionArray = new CompletionQueue.WorkCompletionArray(queueSize);

        try {
            context = new ConnectionContext(deviceNumber, portNumber, queueSize);
        } catch (IOException e) {
            LOGGER.error("Initializing InfiniBand resources failed", e);
            return Status.UNKNOWN_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status serve(InetSocketAddress bindAddress) {
        try {
            context.connect(getOffChannelSocket());
        } catch (IOException e) {
            LOGGER.error("Connecting to remote benchmark failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status connect(InetSocketAddress bindAddress, InetSocketAddress serverAddress) {
        try {
            context.connect(getOffChannelSocket());
        } catch (IOException e) {
            LOGGER.error("Connecting to remote benchmark failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status prepare(int operationSize, int operationCount) {
        try {
            sendBuffer = context.getProtectionDomain().allocateMemory(operationSize, AccessFlag.LOCAL_WRITE, AccessFlag.REMOTE_READ, AccessFlag.REMOTE_WRITE);
            receiveBuffer = context.getProtectionDomain().allocateMemory(operationSize, AccessFlag.LOCAL_WRITE, AccessFlag.REMOTE_READ, AccessFlag.REMOTE_WRITE);
        } catch (IOException e) {
            LOGGER.error("Allocating memory regions failed", e);
            return Status.IO_ERROR;
        }

        sendScatterGatherElement = new ScatterGatherElement(sendBuffer.getHandle(), (int) sendBuffer.getNativeSize(), sendBuffer.getLocalKey());
        receiveScatterGatherElement = new ScatterGatherElement(receiveBuffer.getHandle(), (int) receiveBuffer.getNativeSize(), receiveBuffer.getLocalKey());

        sendWorkRequests = new SendWorkRequest[queueSize];
        receiveWorkRequests = new ReceiveWorkRequest[queueSize];

        SendWorkRequest.Builder sendBuilder = new SendWorkRequest.Builder()
                .withScatterGatherElement(sendScatterGatherElement)
                .withOpCode(OpCode.SEND);
        ReceiveWorkRequest.Builder receiveBuilder = new ReceiveWorkRequest.Builder()
                .withScatterGatherElement(receiveScatterGatherElement);

        for(int i = 0; i < queueSize; i++) {
            sendWorkRequests[i] = sendBuilder.build();
            receiveWorkRequests[i] = receiveBuilder.build();
        }

        try {
            remoteInfo = exchangeMemoryRegionInformation();
        } catch (IOException e) {
            LOGGER.error("Exchanging memory region information failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status cleanup() {
        try {
            sendBuffer.close();
            receiveBuffer.close();
            context.close();
        } catch (IOException e) {
            LOGGER.error("");
        }

        return Status.OK;
    }

    @Override
    protected Status fillReceiveQueue() {
        int batch = queueSize - pendingReceiveCompletions;

        try {
            postReceive(batch);
            pendingReceiveCompletions += batch;
        } catch (IOException e) {
            LOGGER.error("Posting receive work requests failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status sendMultipleMessages(int messageCount) {
        int messagesLeft = messageCount;

        try {
            while(messagesLeft > 0) {
                int batchSize = queueSize - pendingSendCompletions;

                if(batchSize > messagesLeft) {
                    batchSize = messagesLeft;
                }

                postSend(batchSize);

                pendingSendCompletions += batchSize;
                messagesLeft -= batchSize;

                pendingSendCompletions -= pollCompletions(Mode.SEND);
            }

            while(pendingSendCompletions > 0) {
                pendingSendCompletions -= pollCompletions(Mode.SEND);
            }
        } catch (IOException e) {
            LOGGER.error("Sending messages failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status receiveMultipleMessages(int messageCount) {
        int messagesLeft = messageCount - pendingReceiveCompletions;

        try {
            while(messagesLeft > 0) {
                int batchSize = queueSize - pendingReceiveCompletions;

                if(batchSize > messagesLeft) {
                    batchSize = messagesLeft;
                }

                postReceive(batchSize);

                pendingReceiveCompletions += batchSize;
                messagesLeft -= batchSize;

                pendingReceiveCompletions -= pollCompletions(Mode.RECEIVE);
            }

            while(pendingReceiveCompletions > 0) {
                pendingReceiveCompletions -= pollCompletions(Mode.RECEIVE);
            }
        } catch (IOException e) {
            LOGGER.error("Receiving messages failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status performMultipleRdmaOperations(RdmaMode mode, int operationCount) {
        int operationsLeft = operationCount;

        try {
            while(operationsLeft > 0) {
                int batchSize = queueSize - pendingSendCompletions;

                if(batchSize > operationsLeft) {
                    batchSize = operationsLeft;
                }

                postRdma(batchSize, mode);

                pendingSendCompletions += batchSize;
                operationsLeft -= batchSize;

                pendingSendCompletions -= pollCompletions(Mode.SEND);
            }

            while(pendingSendCompletions > 0) {
                pendingSendCompletions -= pollCompletions(Mode.SEND);
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
            postSend(1);

            do {
               polled = pollCompletions(Mode.SEND);
            } while(polled == 0);
        } catch (IOException e) {
            LOGGER.error("Sending message failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status performSingleRdmaOperation(RdmaMode mode) {
        int polled;

        try {
            postRdma(1, mode);

            do {
                polled = pollCompletions(Mode.SEND);
            } while(polled == 0);
        } catch (IOException e) {
            LOGGER.error("Performing RDMA operation failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status performPingPongIterationServer() {
        int polled;

        try {
            postSend(1);

            do {
                polled = pollCompletions(Mode.SEND);
            } while(polled == 0);

            do {
                polled = pollCompletions(Mode.RECEIVE);
            } while(polled == 0);

            postReceive(1);
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
                polled = pollCompletions(Mode.RECEIVE);
            } while(polled == 0);

            postSend(1);

            postReceive(1);

            do {
                polled = pollCompletions(Mode.SEND);
            } while(polled == 0);
        } catch (IOException e) {
            LOGGER.error("Performing ping pong iteration failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    private MemoryRegionInformation exchangeMemoryRegionInformation() throws IOException {
        MemoryRegionInformation localInfo = new MemoryRegionInformation(receiveBuffer.getHandle(), receiveBuffer.getRemoteKey());
        byte[] remoteBytes = new byte[MemoryRegionInformation.getSizeInBytes()];

        LOGGER.info("Sending local memory region information:\n{}", localInfo);

        new DataOutputStream(getOffChannelSocket().getOutputStream()).write(localInfo.toBytes());

        LOGGER.info("Waiting for remote memory region information");

        new DataInputStream(getOffChannelSocket().getInputStream()).readFully(remoteBytes);
        MemoryRegionInformation remoteInfo = MemoryRegionInformation.fromBytes(remoteBytes);

        LOGGER.info("Received remote memory region information:\n{}", remoteInfo);

        return remoteInfo;
    }

    private void postSend(int amount) throws IOException {
        if(amount == 0) {
            return;
        }

        sendList.clear();

        for(int i = 0; i < amount; i++) {
            sendWorkRequests[i].setOpCode(OpCode.SEND);
            sendWorkRequests[i].setSendFlags(SendFlag.SIGNALED);

            sendList.add(sendWorkRequests[i]);
        }

        context.getQueuePair().postSend(sendList);
    }

    private void postReceive(int amount) throws IOException {
        if(amount == 0) {
            return;
        }

        receiveList.clear();

        for(int i = 0; i < amount; i++) {
            receiveList.add(receiveWorkRequests[i]);
        }

        context.getQueuePair().postReceive(receiveList);
    }

    private void postRdma(int amount, RdmaMode mode) throws IOException {
        if(amount == 0) {
            return;
        }

        sendList.clear();

        for(int i = 0; i < amount; i++) {
            sendWorkRequests[i].setOpCode(mode == RdmaMode.WRITE ? OpCode.RDMA_WRITE : OpCode.RDMA_READ);
            sendWorkRequests[i].setSendFlags(SendFlag.SIGNALED);

            sendWorkRequests[i].rdma.setRemoteAddress(remoteInfo.getAddress());
            sendWorkRequests[i].rdma.setRemoteKey(remoteInfo.getRemoteKey());

            sendList.add(sendWorkRequests[i]);
        }

        context.getQueuePair().postSend(sendList);
    }

    private int pollCompletions(Mode mode) throws IOException {
        CompletionQueue completionQueue = mode == Mode.SEND ? context.getSendCompletionQueue() : context.getReceiveCompletionQueue();
        WorkCompletionArray completionArray = mode == Mode.SEND ? sendCompletionArray : receiveCompletionArray;

        completionQueue.poll(completionArray);

        for(int i = 0; i < completionArray.getLength(); i++) {
            WorkCompletion completion = completionArray.get(i);

            if(completion.getStatus() != WorkCompletion.Status.SUCCESS) {
                throw new IOException("Work completion failed with status [" + completion + "]: " + completion.getStatusMessage());
            }
        }

        return completionArray.getLength();
    }
}
