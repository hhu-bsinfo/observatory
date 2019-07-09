package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

public abstract class SetupPhase extends BenchmarkPhase {

    SetupPhase(Benchmark benchmark) {
        super(benchmark);
    }

    @Override
    public final void runPhase() {
        setStatus(execute());
    }

    protected abstract Status execute();
}
