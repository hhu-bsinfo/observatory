package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

class PreparationPhase extends BenchmarkPhase {

    private final int operationSize;

    PreparationPhase(Benchmark benchmark, Mode mode, int operationSize) {
        super(benchmark);

        this.operationSize = operationSize;
    }

    @Override
    public Status execute() {
        return getBenchmark().prepare(operationSize);
    }
}
