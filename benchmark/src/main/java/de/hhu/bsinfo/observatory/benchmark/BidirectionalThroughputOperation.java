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
        super(sendOperation.getBenchmark(), sendOperation.getMode(), sendOperation.getMeasurement().getOperationCount(), sendOperation.getMeasurement().getOperationSize());

        this.sendOperation = sendOperation;
        this.receiveOperation = receiveOperation;
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
            e.printStackTrace();
            return Status.UNKNOWN_ERROR;
        }

        if(sendStatus.get() != Status.OK && sendStatus.get() != Status.OK_NO_MEASUREMENT) {
            return sendStatus.get();
        } else if(receiveStatus.get() != Status.OK && receiveStatus.get() != Status.OK_NO_MEASUREMENT) {
            return receiveStatus.get();
        } else {
            getMeasurement().setTotalTime(sendOperation.getMeasurement().getTotalTime());
            getMeasurement().setOperationThroughput(sendOperation.getMeasurement().getOperationThroughput() + receiveOperation.getMeasurement().getDataThroughput());
            getMeasurement().setDataThroughput(sendOperation.getMeasurement().getDataThroughput() + receiveOperation.getMeasurement().getDataThroughput());

            return Status.OK;
        }
    }
}
