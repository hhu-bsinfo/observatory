package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

public class InitializationPhase extends SetupPhase {

    InitializationPhase(Benchmark benchmark) {
        super(benchmark);
    }

    @Override
    public Status execute() {
        return getBenchmark().initialize();
    }
}
