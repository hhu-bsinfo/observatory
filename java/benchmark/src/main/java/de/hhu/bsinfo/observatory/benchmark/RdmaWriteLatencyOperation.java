package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Connection.Mode;
import de.hhu.bsinfo.observatory.benchmark.Connection.RdmaMode;

public class RdmaWriteLatencyOperation extends RdmaLatencyOperation {

    RdmaWriteLatencyOperation(Connection connection, Mode mode, int operationCount, int operationSize) {
        super(connection, mode, operationCount, operationSize, RdmaMode.WRITE);
    }

    @Override
    String getOutputFilename() {
        return "RDMA Write Latency";
    }
}
