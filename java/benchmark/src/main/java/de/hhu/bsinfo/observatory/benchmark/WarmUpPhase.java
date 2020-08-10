package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

class WarmUpPhase extends BenchmarkPhase {

    private final Operation operation;
    private final int iterationCount;

    WarmUpPhase(Connection connection, Operation operation, int iterationCount) {
        super(connection);

        this.operation = operation;
        this.iterationCount = iterationCount;
    }

    @Override
    Status execute() {
        return operation.warmUp(iterationCount);
    }
}
