#include "PreparationPhase.h"
#include <observatory/Benchmark.h>

#include <utility>

namespace Observatory {

PreparationPhase::PreparationPhase(Benchmark &benchmark, uint32_t operationSize) :
        BenchmarkPhase(benchmark),
        operationSize(operationSize) {}

const char* PreparationPhase::getName() {
    return "PreparationPhase";
}

Status PreparationPhase::execute() {
    return getBenchmark().prepare(operationSize);
}

}
