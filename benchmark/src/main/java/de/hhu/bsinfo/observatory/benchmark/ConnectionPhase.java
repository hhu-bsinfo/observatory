package de.hhu.bsinfo.observatory.benchmark;

public class ConnectionPhase extends BenchmarkPhase {

    ConnectionPhase(Benchmark benchmark) {
        super(benchmark);
    }

    @Override
    protected Status execute() {
        Benchmark benchmark = getBenchmark();

        if(benchmark.isServer()) {
            return benchmark.serve(benchmark.getAddress());
        } else {
            return benchmark.connect(benchmark.getAddress());
        }
    }
}
