package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Connection.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

class MessagingThroughputOperation extends ThroughputOperation {

    MessagingThroughputOperation(Connection connection, Mode mode, int operationCount, int operationSize) {
        super(connection, mode, operationCount, operationSize);
    }

    @Override
    String getOutputFilename() {
        return "Messaging Throughput";
    }

    @Override
    boolean needsFilledReceiveQueue() {
        return getMode() != Mode.SEND;
    }

    @Override
    Status warmUp(int operationCount) {
        if (getMode() == Mode.SEND) {
            return getConnection().sendMultipleMessages(operationCount);
        } else {
            return getConnection().receiveMultipleMessages(operationCount);
        }
    }

    @Override
    public Status execute() {
        if (getMode() == Mode.SEND) {
            long startTime = System.nanoTime();
            Status status = getConnection().sendMultipleMessages(getMeasurement().getOperationCount());
            getMeasurement().setMeasuredTime(System.nanoTime() - startTime);

            return status;
        } else {
            long startTime = System.nanoTime();
            Status status = getConnection().receiveMultipleMessages(getMeasurement().getOperationCount());
            getMeasurement().setMeasuredTime(System.nanoTime() - startTime);

            return status;
        }
    }
}
