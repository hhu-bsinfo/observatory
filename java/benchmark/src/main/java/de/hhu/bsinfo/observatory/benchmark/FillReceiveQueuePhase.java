package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

class FillReceiveQueuePhase extends BenchmarkPhase {

    FillReceiveQueuePhase(Benchmark benchmark) {
        super(benchmark);
    }

    @Override
    Status execute() {
        return getBenchmark().fillReceiveQueue();
    }
}
