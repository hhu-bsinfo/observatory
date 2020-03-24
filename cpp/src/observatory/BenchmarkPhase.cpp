#include <observatory/result/Status.h>
#include "BenchmarkPhase.h"

namespace Observatory {

BenchmarkPhase::BenchmarkPhase(Benchmark &benchmark) :
        benchmark(benchmark) {}

Benchmark& BenchmarkPhase::getBenchmark() const {
    return benchmark;
}

}
