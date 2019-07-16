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
    public Status execute() {
        if(getMode() == Mode.SEND) {
            long startTime = System.nanoTime();

            Status status = getBenchmark().benchmarkRdmaThroughput(rdmaMode, getMeasurement().getOperationCount());
            getMeasurement().setMeasuredTime(System.nanoTime() - startTime);

            if(status != Status.OK) {
                return status;
            }

            getBenchmark().sendSync();

            return status;
        } else {
            getBenchmark().receiveSync();

            return Status.OK;
        }
    }
}
