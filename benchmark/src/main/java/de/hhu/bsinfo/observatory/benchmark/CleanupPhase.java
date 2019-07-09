package de.hhu.bsinfo.observatory.benchmark;

public class CleanupPhase extends BenchmarkPhase {

    CleanupPhase(Benchmark benchmark) {
        super(benchmark);
    }

    @Override
    protected Status execute() {
        return getBenchmark().cleanup();
    }
}
