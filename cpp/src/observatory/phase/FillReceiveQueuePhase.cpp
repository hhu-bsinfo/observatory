#include "FillReceiveQueuePhase.h"
#include <observatory/Benchmark.h>

#include <utility>

namespace Observatory {

FillReceiveQueuePhase::FillReceiveQueuePhase(Benchmark &benchmark) :
        BenchmarkPhase(benchmark) {}

const char* FillReceiveQueuePhase::getName() {
    return "FillReceiveQueuePhase";
}

Status FillReceiveQueuePhase::execute() {
    return getBenchmark().fillReceiveQueue();
}

}