package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

class PreparationPhase extends BenchmarkPhase {

    private final int operationSize;
    private final int operationCount;

    PreparationPhase(Connection connection, int operationSize, int operationCount) {
        super(connection);

        this.operationSize = operationSize;
        this.operationCount = operationCount;
    }

    @Override
    public Status execute() {
        return getConnection().prepare(operationSize, operationCount);
    }
}
