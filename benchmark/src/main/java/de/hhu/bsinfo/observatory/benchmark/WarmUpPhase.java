package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

class WarmUpPhase extends BenchmarkPhase {

    private final Operation operation;
    private final int iterationCount;

    WarmUpPhase(Benchmark benchmark, Operation operation, int iterationCount) {
        super(benchmark);

        this.operation = operation;
        this.iterationCount = iterationCount;
    }

    @Override
    Status execute() {
        if(!getBenchmark().synchronize()) {
            return Status.SYNC_ERROR;
        }

        return operation.warmUp(iterationCount);
    }
}
