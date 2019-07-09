package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

public abstract class BenchmarkPhase implements Runnable {

    private final Benchmark benchmark;

    private Status status;

    BenchmarkPhase(Benchmark benchmark) {
        this.benchmark = benchmark;
    }

    public Status getStatus() {
        return status;
    }

    void setStatus(Status status) {
        this.status = status;
    }

    Benchmark getBenchmark() {
        return benchmark;
    }

    @Override
    public abstract void run();
}
