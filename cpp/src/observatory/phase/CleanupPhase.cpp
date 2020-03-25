#include "CleanupPhase.h"
#include <observatory/Benchmark.h>

#include <utility>

namespace Observatory {

CleanupPhase::CleanupPhase(Benchmark &benchmark) :
        BenchmarkPhase(benchmark) {}

const char* CleanupPhase::getName() {
    return "CleanupPhase";
}

Status CleanupPhase::execute() {
    return getBenchmark().cleanup();
}

}