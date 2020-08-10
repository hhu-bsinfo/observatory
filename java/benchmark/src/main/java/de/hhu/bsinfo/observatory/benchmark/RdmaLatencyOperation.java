package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Connection.Mode;
import de.hhu.bsinfo.observatory.benchmark.Connection.RdmaMode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

public abstract class RdmaLatencyOperation extends LatencyOperation {

    private final RdmaMode rdmaMode;

    RdmaLatencyOperation(Connection connection, Mode mode, int operationCount, int operationSize, RdmaMode rdmaMode) {
        super(connection, mode, operationCount, operationSize);

        this.rdmaMode = rdmaMode;
    }

    @Override
    boolean needsFilledReceiveQueue() {
        return false;
    }

    @Override
    Status warmUp(int operationCount) {
        Status status = Status.OK;

        if (getMode() == Mode.SEND) {
            for (int i = 0; i < operationCount; i++) {
                status = getConnection().performSingleRdmaOperation(rdmaMode);

                if (status != Status.OK) {
                    return status;
                }
            }
        }

        if (!getConnection().synchronize()) {
            return Status.SYNC_ERROR;
        }

        return status;
    }

    @Override
    public Status execute() {
        Status status = Status.OK;

        if (getMode() == Mode.SEND) {
            long startTime = System.nanoTime();

            for (int i = 0; i < getMeasurement().getOperationCount(); i++) {
                getMeasurement().startSingleMeasurement();
                status = getConnection().performSingleRdmaOperation(rdmaMode);
                getMeasurement().stopSingleMeasurement();

                if (status != Status.OK) {
                    return status;
                }
            }

            getMeasurement().finishMeasuring(System.nanoTime() - startTime);
        }

        if (!getConnection().synchronize()) {
            return Status.SYNC_ERROR;
        }

        return status;
    }
}
