package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Connection.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

public class MessagingLatencyOperation extends LatencyOperation {

    private final String OUTPUT_FILENAME = "Messaging Latency";

    MessagingLatencyOperation(Connection connection, Mode mode, int operationCount, int operationSize) {
        super(connection, mode, operationCount, operationSize);
    }

    @Override
    String getOutputFilename() {
        return OUTPUT_FILENAME;
    }

    @Override
    boolean needsFilledReceiveQueue() {
        return getMode() != Mode.SEND;
    }

    @Override
    Status warmUp(int operationCount) {
        if (getMode() == Mode.SEND) {
            for (int i = 0; i < operationCount; i++) {
                Status status = getConnection().sendSingleMessage();

                if (status != Status.OK) {
                    return status;
                }
            }

            return Status.OK;
        } else {
            return getConnection().receiveMultipleMessages(operationCount);
        }
    }

    @Override
    Status execute() {
        if (getMode() == Mode.SEND) {
            long startTime = System.nanoTime();

            for (int i = 0; i < getMeasurement().getOperationCount(); i++) {
                getMeasurement().startSingleMeasurement();
                Status status = getConnection().sendSingleMessage();
                getMeasurement().stopSingleMeasurement();

                if (status != Status.OK) {
                    return status;
                }
            }

            getMeasurement().finishMeasuring(System.nanoTime() - startTime);

            return Status.OK;
        } else {
            return getConnection().receiveMultipleMessages(getMeasurement().getOperationCount());
        }
    }
}
