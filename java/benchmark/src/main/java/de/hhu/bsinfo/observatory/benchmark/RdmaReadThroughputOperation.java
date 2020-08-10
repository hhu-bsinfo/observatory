package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Connection.Mode;
import de.hhu.bsinfo.observatory.benchmark.Connection.RdmaMode;

public class RdmaReadThroughputOperation extends RdmaThroughputOperation {

    RdmaReadThroughputOperation(Connection connection, Mode mode, int operationCount, int operationSize) {
        super(connection, mode, operationCount, operationSize, RdmaMode.READ);
    }

    @Override
    String getOutputFilename() {
        return "RDMA Read Throughput";
    }
}
