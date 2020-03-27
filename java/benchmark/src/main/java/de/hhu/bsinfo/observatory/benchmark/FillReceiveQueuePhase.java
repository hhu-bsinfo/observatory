package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

class FillReceiveQueuePhase extends BenchmarkPhase {

    private final boolean fillReceiveQueue;

    FillReceiveQueuePhase(Benchmark benchmark, Operation operation) {
        super(benchmark);

        this.fillReceiveQueue = operation.needsFilledReceiveQueue();
    }

    @Override
    Status execute() {
        if(fillReceiveQueue) {
            return getBenchmark().fillReceiveQueue();
        }

        return Status.OK;
    }
}
