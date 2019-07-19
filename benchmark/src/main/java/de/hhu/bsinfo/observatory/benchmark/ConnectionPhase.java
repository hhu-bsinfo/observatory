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
            Status status = Status.UNKNOWN_ERROR;

            for(int i = 0; i < getBenchmark().getConnectionRetries(); i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}

                status = benchmark.connect(benchmark.getBindAddress(), benchmark.getRemoteAddress());

                if(status == Status.OK || status == Status.NOT_IMPLEMENTED) {
                    return status;
                }
            }

            return status;
        }
    }
}
