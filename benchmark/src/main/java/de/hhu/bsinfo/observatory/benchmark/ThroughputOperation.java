package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.ThroughputMeasurement;

abstract class ThroughputOperation extends Operation {

    ThroughputOperation(Benchmark benchmark, Mode mode, int operationCount, int operationSize) {
        super(benchmark, mode, new ThroughputMeasurement(operationCount, operationSize));
    }

    @Override
    protected ThroughputMeasurement getMeasurement() {
        return (ThroughputMeasurement) super.getMeasurement();
    }
}
