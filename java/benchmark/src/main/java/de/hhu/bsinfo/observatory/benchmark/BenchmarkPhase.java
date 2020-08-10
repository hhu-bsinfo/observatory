package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

abstract class BenchmarkPhase {

    private final Connection connection;

    BenchmarkPhase(Connection connection) {
        this.connection = connection;
    }

    Connection getConnection() {
        return connection;
    }

    abstract Status execute();
}
