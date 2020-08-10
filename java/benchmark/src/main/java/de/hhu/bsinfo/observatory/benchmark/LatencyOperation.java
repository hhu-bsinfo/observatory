package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Connection.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.LatencyMeasurement;

public abstract class LatencyOperation extends Operation {

    LatencyOperation(Connection connection, Mode mode, int operationCount, int operationSize) {
        super(connection, mode, new LatencyMeasurement(operationCount, operationSize));
    }

    @Override
    protected LatencyMeasurement getMeasurement() {
        return (LatencyMeasurement) super.getMeasurement();
    }
}
