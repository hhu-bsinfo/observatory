package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

public class CleanupPhase extends SetupPhase {

    CleanupPhase(Benchmark benchmark) {
        super(benchmark);
    }

    @Override
    protected Status execute() {
        return getBenchmark().cleanup();
    }
}
