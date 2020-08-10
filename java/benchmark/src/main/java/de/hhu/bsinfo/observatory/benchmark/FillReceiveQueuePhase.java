package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

class FillReceiveQueuePhase extends BenchmarkPhase {

    private final boolean fillReceiveQueue;

    FillReceiveQueuePhase(Connection connection, Operation operation) {
        super(connection);

        this.fillReceiveQueue = operation.needsFilledReceiveQueue();
    }

    @Override
    Status execute() {
        if (fillReceiveQueue) {
            return getConnection().fillReceiveQueue();
        }

        return Status.OK;
    }
}
