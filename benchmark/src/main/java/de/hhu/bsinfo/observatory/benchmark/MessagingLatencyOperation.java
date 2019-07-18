package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

public class MessagingLatencyOperation extends LatencyOperation {

    MessagingLatencyOperation(Benchmark benchmark, Mode mode, int operationCount, int operationSize) {
        super(benchmark, mode, operationCount, operationSize);
    }

    @Override
    boolean needsFilledReceiveQueue() {
        return !getBenchmark().isServer();
    }

    @Override
    Status warmUp(int operationCount) {
        if(getMode() == Mode.SEND) {
            for(int i = 0; i < operationCount; i++) {
                Status status = getBenchmark().benchmarkSendSingleMessageLatency();

                if(status != Status.OK) {
                    return status;
                }
            }
        } else {
            for(int i = 0; i < operationCount; i++) {
                Status status = getBenchmark().benchmarkReceiveSingleMessageLatency();

                if (status != Status.OK) {
                    return status;
                }
            }
        }

        return Status.OK;
    }

    @Override
    Status execute() {
        if(getMode() == Mode.SEND) {
            for(int i = 0; i < getMeasurement().getOperationCount(); i++) {
                getMeasurement().startSingleMeasurement();
                Status status = getBenchmark().benchmarkSendSingleMessageLatency();
                getMeasurement().stopSingleMeasurement();

                if(status != Status.OK) {
                    return status;
                }
            }
        } else {
            for(int i = 0; i < getMeasurement().getOperationCount(); i++) {
                getMeasurement().startSingleMeasurement();
                Status status = getBenchmark().benchmarkReceiveSingleMessageLatency();
                getMeasurement().stopSingleMeasurement();

                if (status != Status.OK) {
                    return status;
                }
            }
        }

        getMeasurement().finishMeasuring();

        return Status.OK;
    }
}
