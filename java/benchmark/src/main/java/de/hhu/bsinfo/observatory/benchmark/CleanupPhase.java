package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

class CleanupPhase extends BenchmarkPhase {

    CleanupPhase(Connection connection) {
        super(connection);
    }

    @Override
    protected Status execute() {
        return getConnection().cleanup();
    }
}
