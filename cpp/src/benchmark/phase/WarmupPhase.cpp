#include "WarmupPhase.h"
#include <benchmark/Benchmark.h>

#include <utility>

namespace Observatory {

WarmupPhase::WarmupPhase(Benchmark &benchmark, Operation &operation, uint32_t operationCount) :
        BenchmarkPhase(benchmark),
        operation(operation),
        operationCount(operationCount) {}

const char* WarmupPhase::getName() {
    return "WarmupPhase";
}

Status WarmupPhase::execute() {
    return operation.warmUp(operationCount);
}

}