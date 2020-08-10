package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BidirectionalThroughputOperation extends ThroughputOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(BidirectionalThroughputOperation.class);

    private final ThroughputOperation sendOperation;
    private final ThroughputOperation receiveOperation;

    BidirectionalThroughputOperation(ThroughputOperation sendOperation, ThroughputOperation receiveOperation) {
        super(sendOperation.getConnection(), sendOperation.getMode(), sendOperation.getMeasurement().getOperationCount(), sendOperation.getMeasurement().getOperationSize());

        this.sendOperation = sendOperation;
        this.receiveOperation = receiveOperation;
    }

    @Override
    String getOutputFilename() {
        return "Bidirectional " + sendOperation.getOutputFilename();
    }

    @Override
    boolean needsFilledReceiveQueue() {
        return sendOperation.needsFilledReceiveQueue() || receiveOperation.needsFilledReceiveQueue();
    }

    @Override
    Status warmUp(int operationCount) {
        LOGGER.info("Executing warm up for bidirectional phase of type '{}'", sendOperation.getClass().getSimpleName());

        AtomicReference<Status> sendStatus = new AtomicReference<>();
        AtomicReference<Status> receiveStatus = new AtomicReference<>();

        Thread sendThread = new Thread(() -> sendStatus.set(sendOperation.warmUp(operationCount)));
        Thread receiveThread = new Thread(() -> receiveStatus.set(receiveOperation.warmUp(operationCount)));

        sendThread.start();
        receiveThread.start();

        try {
            sendThread.join();
            receiveThread.join();
        } catch (InterruptedException e) {
            LOGGER.error("Joining threads failed", e);
            return Status.UNKNOWN_ERROR;
        }

        if (sendStatus.get() != Status.OK) {
            return sendStatus.get();
        } else if (receiveStatus.get() != Status.OK) {
            return receiveStatus.get();
        }

        return Status.OK;
    }

    @Override
    Status execute() {
        LOGGER.info("Executing bidirectional phase of type '{}'", sendOperation.getClass().getSimpleName());

        AtomicReference<Status> sendStatus = new AtomicReference<>();
        AtomicReference<Status> receiveStatus = new AtomicReference<>();

        Thread sendThread = new Thread(() -> sendStatus.set(sendOperation.execute()));
        Thread receiveThread = new Thread(() -> receiveStatus.set(receiveOperation.execute()));

        sendThread.start();
        receiveThread.start();

        try {
            sendThread.join();
            receiveThread.join();
        } catch (InterruptedException e) {
            LOGGER.error("Joining threads failed", e);
            return Status.UNKNOWN_ERROR;
        }

        if (sendStatus.get() != Status.OK) {
            return sendStatus.get();
        } else if (receiveStatus.get() != Status.OK) {
            return receiveStatus.get();
        }

        getMeasurement().setTotalData(getMeasurement().getTotalData() * 2);
        getMeasurement().setTotalTime(sendOperation.getMeasurement().getTotalTime());
        getMeasurement().setOperationThroughput(sendOperation.getMeasurement().getOperationThroughput() + receiveOperation.getMeasurement().getOperationThroughput());
        getMeasurement().setDataThroughput(sendOperation.getMeasurement().getDataThroughput() + receiveOperation.getMeasurement().getDataThroughput());

        return Status.OK;
    }
}
