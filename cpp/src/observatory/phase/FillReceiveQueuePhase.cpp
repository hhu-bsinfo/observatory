#include "FillReceiveQueuePhase.h"
#include <observatory/Benchmark.h>

#include <utility>
#include <observatory/operation/Operation.h>

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