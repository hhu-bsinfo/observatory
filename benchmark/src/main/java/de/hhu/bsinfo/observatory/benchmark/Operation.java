package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.Measurement;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

abstract class Operation {

    private final Benchmark benchmark;
    private final Mode mode;
    private final Measurement measurement;

    Operation(Benchmark benchmark, Mode mode, Measurement measurement) {
        this.benchmark = benchmark;
        this.mode = mode;
        this.measurement = measurement;
    }

    protected Benchmark getBenchmark() {
        return benchmark;
    }

    protected Measurement getMeasurement() {
        return measurement;
    }

    protected Mode getMode() {
        return mode;
    }

    abstract boolean needsFilledReceiveQueue();

    abstract Status warmUp(int operationCount);

    abstract Status execute();
}
