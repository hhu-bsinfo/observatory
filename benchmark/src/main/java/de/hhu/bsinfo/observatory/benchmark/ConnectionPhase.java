package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

class ConnectionPhase extends BenchmarkPhase {

    ConnectionPhase(Benchmark benchmark) {
        super(benchmark);
    }

    @Override
    Status execute() {
        Benchmark benchmark = getBenchmark();

        if(benchmark.isServer()) {
            return benchmark.serve(benchmark.getBindAddress());
        } else {
            return benchmark.connect(benchmark.getBindAddress(), benchmark.getRemoteAddress());
        }
    }
}
