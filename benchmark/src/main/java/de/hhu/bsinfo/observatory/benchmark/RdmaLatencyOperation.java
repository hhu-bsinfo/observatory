package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.Benchmark.RdmaMode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

public class RdmaLatencyOperation extends LatencyOperation {

    private final RdmaMode rdmaMode;

    RdmaLatencyOperation(Benchmark benchmark, Mode mode, int operationCount, int operationSize, RdmaMode rdmaMode) {
        super(benchmark, mode, operationCount, operationSize);

        this.rdmaMode = rdmaMode;
    }

    @Override
    boolean needsFilledReceiveQueue() {
        return false;
    }

    @Override
    Status warmUp(int operationCount) {
        if(getMode() == Mode.SEND) {
            for(int i = 0; i < operationCount; i++) {
                Status status = getBenchmark().performSingleRdmaOperation(rdmaMode);

                if(status != Status.OK) {
                    return status;
                }
            }
        }

        getBenchmark().synchronize();

        return Status.OK;
    }

    @Override
    public Status execute() {
        Status status = Status.OK;

        if(getMode() == Mode.SEND) {
            for(int i = 0; i < getMeasurement().getOperationCount(); i++) {
                getMeasurement().startSingleMeasurement();
                status = getBenchmark().performSingleRdmaOperation(rdmaMode);
                getMeasurement().stopSingleMeasurement();

                if(status != Status.OK) {
                    return status;
                }
            }

            getMeasurement().finishMeasuring();
        }

        getBenchmark().synchronize();

        return status;
    }
}
