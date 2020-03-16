package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.Benchmark.RdmaMode;

public class RdmaReadThroughputOperation extends RdmaThroughputOperation {

    RdmaReadThroughputOperation(Benchmark benchmark, Mode mode, int operationCount, int operationSize) {
        super(benchmark, mode, operationCount, operationSize, RdmaMode.READ);
    }

    @Override
    String getOutputFilename() {
        return "RDMA Read Throughput";
    }
}
