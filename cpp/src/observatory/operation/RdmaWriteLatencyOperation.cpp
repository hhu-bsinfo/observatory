#include "RdmaWriteLatencyOperation.h"

namespace Observatory {

RdmaWriteLatencyOperation::RdmaWriteLatencyOperation(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize) :
        RdmaLatencyOperation(benchmark, mode, operationCount, operationSize, Benchmark::RdmaMode::WRITE) {}

const char* RdmaWriteLatencyOperation::getClassName() const {
    return "RdmaWriteLatencyOperation";
}

const char* RdmaWriteLatencyOperation::getOutputFilename() const {
    return "RDMA Write Latency";
}

}