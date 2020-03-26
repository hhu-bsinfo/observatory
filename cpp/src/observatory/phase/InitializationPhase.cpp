#include "InitializationPhase.h"
#include <observatory/Benchmark.h>

#include <utility>

namespace Observatory {

InitializationPhase::InitializationPhase(Benchmark &benchmark) :
        BenchmarkPhase(benchmark) {}

const char* InitializationPhase::getName() {
    return "InitializationPhase";
}

Status InitializationPhase::execute() {
    return getBenchmark().initialize();
}

}