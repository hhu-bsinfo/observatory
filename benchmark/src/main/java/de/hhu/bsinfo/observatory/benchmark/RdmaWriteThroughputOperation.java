package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.Benchmark.RdmaMode;

public class RdmaWriteThroughputOperation extends RdmaThroughputOperation {

    RdmaWriteThroughputOperation(Benchmark benchmark, Mode mode, int operationCount, int operationSize) {
        super(benchmark, mode, operationCount, operationSize, RdmaMode.WRITE);
    }

    @Override
    String getOutputFilename() {
        return "RDMA Write Throughput";
    }
}
