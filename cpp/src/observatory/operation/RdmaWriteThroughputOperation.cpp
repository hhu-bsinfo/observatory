#include "RdmaWriteThroughputOperation.h"

namespace Observatory {

RdmaWriteThroughputOperation::RdmaWriteThroughputOperation(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize) :
        RdmaThroughputOperation(benchmark, mode, operationCount, operationSize, Benchmark::RdmaMode::WRITE) {}

const char* RdmaWriteThroughputOperation::getClassName() const {
    return "RdmaWriteThroughputOperation";
}

const char* RdmaWriteThroughputOperation::getOutputFilename() const {
    return "RDMA Write Throughput";
}

}