package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

class InitializationPhase extends BenchmarkPhase {

    InitializationPhase(Benchmark benchmark) {
        super(benchmark);
    }

    @Override
    protected Status execute() {
        Status status = getBenchmark().initialize();

        getBenchmark().synchronize();

        return status;
    }

}
