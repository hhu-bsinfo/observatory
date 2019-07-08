package de.hhu.bsinfo.observatory.benchmark;

public abstract class BenchmarkPhase {

    private final Benchmark benchmark;

    private Status status;

    public BenchmarkPhase(Benchmark benchmark) {
        this.benchmark = benchmark;
    }

    public void run() {
        this.status = execute();
    }

    public Status getStatus() {
        return status;
    }

    protected abstract Status execute();

    protected Benchmark getBenchmark() {
        return benchmark;
    }
}
