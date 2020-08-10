package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Connection.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.ThroughputMeasurement;

abstract class ThroughputOperation extends Operation {

    ThroughputOperation(Connection connection, Mode mode, int operationCount, int operationSize) {
        super(connection, mode, new ThroughputMeasurement(operationCount, operationSize));
    }

    @Override
    protected ThroughputMeasurement getMeasurement() {
        return (ThroughputMeasurement) super.getMeasurement();
    }
}
