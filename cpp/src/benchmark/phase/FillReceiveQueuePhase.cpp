#include "FillReceiveQueuePhase.h"
#include <benchmark/Benchmark.h>

#include <utility>
#include <benchmark/operation/Operation.h>

namespace Observatory {

FillReceiveQueuePhase::FillReceiveQueuePhase(Benchmark &benchmark, const Operation &operation) :
        BenchmarkPhase(benchmark),
        fillReceiveQueue(operation.needsFilledReceiveQueue()) {}

const char* FillReceiveQueuePhase::getName() {
    return "FillReceiveQueuePhase";
}

Status FillReceiveQueuePhase::execute() {
    if(fillReceiveQueue) {
        return getBenchmark().fillReceiveQueue();
    }

    return Status::OK;
}

}