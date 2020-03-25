#include <observatory/result/Status.h>
#include <memory>
#include <utility>
#include "BenchmarkPhase.h"

namespace Observatory {

BenchmarkPhase::BenchmarkPhase(Benchmark &benchmark) :
        benchmark(benchmark) {}

Benchmark& BenchmarkPhase::getBenchmark() const {
    return benchmark;
}

}
