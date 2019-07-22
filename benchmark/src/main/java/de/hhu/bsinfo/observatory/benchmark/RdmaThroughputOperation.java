package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.Benchmark.RdmaMode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

public class RdmaThroughputOperation extends ThroughputOperation {

    private final RdmaMode rdmaMode;

    RdmaThroughputOperation(Benchmark benchmark, Mode mode, int operationCount, int operationSize, RdmaMode rdmaMode) {
        super(benchmark, mode, operationCount, operationSize);

        this.rdmaMode = rdmaMode;
    }

    @Override
    boolean needsFilledReceiveQueue() {
        return false;
    }

    @Override
    Status warmUp(int operationCount) {
        Status status = Status.OK;

        if(getMode() == Mode.SEND) {
            status = getBenchmark().performMultipleRdmaOperations(rdmaMode, operationCount);
        }

        getBenchmark().synchronize();

        return status;
    }

    @Override
    public Status execute() {
        Status status = Status.OK;

        if(getMode() == Mode.SEND) {
            long startTime = System.nanoTime();
            status = getBenchmark().performMultipleRdmaOperations(rdmaMode, getMeasurement().getOperationCount());
            getMeasurement().setMeasuredTime(System.nanoTime() - startTime);

            if(status != Status.OK) {
                return status;
            }
        }

        getBenchmark().synchronize();

        return status;
    }
}
