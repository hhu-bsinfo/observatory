package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

class InitializationPhase extends BenchmarkPhase {

    InitializationPhase(Benchmark benchmark) {
        super(benchmark);
    }

    @Override
    protected Status execute() {
        return getBenchmark().initialize();
    }

}
