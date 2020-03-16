package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.LatencyMeasurement;

public abstract class LatencyOperation extends Operation {

    LatencyOperation(Benchmark benchmark, Mode mode, int operationCount, int operationSize) {
        super(benchmark, mode, new LatencyMeasurement(operationCount, operationSize));
    }

    @Override
    protected LatencyMeasurement getMeasurement() {
        return (LatencyMeasurement) super.getMeasurement();
    }
}
