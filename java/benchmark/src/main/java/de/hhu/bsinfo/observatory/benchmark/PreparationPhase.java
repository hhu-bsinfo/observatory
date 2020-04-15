package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

class PreparationPhase extends BenchmarkPhase {

    private final int operationSize;
    private final int operationCount;

    PreparationPhase(Benchmark benchmark, int operationSize, int operationCount) {
        super(benchmark);

        this.operationSize = operationSize;
        this.operationCount = operationCount;
    }

    @Override
    public Status execute() {
        return getBenchmark().prepare(operationSize, operationCount);
    }
}
