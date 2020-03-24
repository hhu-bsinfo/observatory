#include "InitializationPhase.h"

namespace Observatory {

InitializationPhase::InitializationPhase(Benchmark &benchmark) :
        BenchmarkPhase(benchmark) {}

std::string InitializationPhase::getClassName() const {
    return "Observatory::InitializationPhase";
}

Result::Status InitializationPhase::execute() {
    return getBenchmark().setup();
}

}