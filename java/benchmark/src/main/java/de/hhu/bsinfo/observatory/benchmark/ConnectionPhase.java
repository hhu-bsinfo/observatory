package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;

class ConnectionPhase extends BenchmarkPhase {

    ConnectionPhase(Connection connection) {
        super(connection);
    }

    @Override
    Status execute() {
        Connection connection = getConnection();

        if (connection.isServer()) {
            return connection.serve(connection.getBindAddress());
        } else {
            Status status = Status.UNKNOWN_ERROR;

            for (int i = 0; i < getConnection().getConnectionRetries(); i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}

                status = connection.connect(connection.getBindAddress(), connection.getRemoteAddress());

                if (status == Status.OK || status == Status.NOT_IMPLEMENTED) {
                    return status;
                }
            }

            return status;
        }
    }
}
