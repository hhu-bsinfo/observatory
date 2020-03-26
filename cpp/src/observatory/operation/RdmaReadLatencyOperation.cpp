#include "RdmaReadLatencyOperation.h"

namespace Observatory {

RdmaReadLatencyOperation::RdmaReadLatencyOperation(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize) :
        RdmaLatencyOperation(benchmark, mode, operationCount, operationSize, Benchmark::RdmaMode::WRITE) {}

const char* RdmaReadLatencyOperation::getClassName() const {
    return "RdmaReadLatencyOperation";
}

const char* RdmaReadLatencyOperation::getOutputFilename() const {
    return "RDMA Read Latency";
}

}