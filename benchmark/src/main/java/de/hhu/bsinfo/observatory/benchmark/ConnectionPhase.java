package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

public class ConnectionPhase extends SetupPhase {

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
