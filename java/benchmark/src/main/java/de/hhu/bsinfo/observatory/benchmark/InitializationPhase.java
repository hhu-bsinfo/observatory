package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

class InitializationPhase extends BenchmarkPhase {

    InitializationPhase(Connection connection) {
        super(connection);
    }

    @Override
    protected Status execute() {
        return getConnection().initialize();
    }

}
