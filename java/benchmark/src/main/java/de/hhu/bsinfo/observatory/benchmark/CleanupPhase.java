package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

class CleanupPhase extends BenchmarkPhase {

    CleanupPhase(Benchmark benchmark) {
        super(benchmark);
    }

    @Override
    protected Status execute() {
        return getBenchmark().cleanup();
    }
}
