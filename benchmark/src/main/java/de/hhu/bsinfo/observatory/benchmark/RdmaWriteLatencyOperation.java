package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.Benchmark.RdmaMode;

public class RdmaWriteLatencyOperation extends RdmaLatencyOperation {

    RdmaWriteLatencyOperation(Benchmark benchmark, Mode mode, int operationCount, int operationSize) {
        super(benchmark, mode, operationCount, operationSize, RdmaMode.WRITE);
    }
}
