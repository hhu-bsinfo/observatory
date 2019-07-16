package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

abstract class BenchmarkPhase {

    private final Benchmark benchmark;

    BenchmarkPhase(Benchmark benchmark) {
        this.benchmark = benchmark;
    }

    Benchmark getBenchmark() {
        return benchmark;
    }

    abstract Status execute();
}
