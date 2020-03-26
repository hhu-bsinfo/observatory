#include "RdmaReadThroughputOperation.h"

namespace Observatory {

RdmaReadThroughputOperation::RdmaReadThroughputOperation(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize) :
        RdmaThroughputOperation(benchmark, mode, operationCount, operationSize, Benchmark::RdmaMode::WRITE) {}

const char* RdmaReadThroughputOperation::getClassName() const {
    return "RdmaReadThroughputOperation";
}

const char* RdmaReadThroughputOperation::getOutputFilename() const {
    return "RDMA Read Throughput";
}

}