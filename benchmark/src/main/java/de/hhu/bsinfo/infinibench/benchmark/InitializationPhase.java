package de.hhu.bsinfo.infinibench.benchmark;

public class InitializationPhase extends BenchmarkPhase {

    public InitializationPhase(Benchmark benchmark) {
        super(benchmark);
    }

    @Override
    public Status execute() {
        return getBenchmark().initialize();
    }
}
