#include "PreparationPhase.h"
#include <observatory/Benchmark.h>

#include <utility>

namespace Observatory {

PreparationPhase::PreparationPhase(Benchmark &benchmark, uint32_t operationSize, uint32_t operationCount) :
        BenchmarkPhase(benchmark),
        operationSize(operationSize),
        operationCount(operationCount) {}

const char* PreparationPhase::getName() {
    return "PreparationPhase";
}

Status PreparationPhase::execute() {
    return getBenchmark().prepare(operationSize, operationCount);
}

}
