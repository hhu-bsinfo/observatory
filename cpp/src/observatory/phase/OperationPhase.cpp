#include "OperationPhase.h"
#include <observatory/Benchmark.h>

#include <utility>

namespace Observatory {

OperationPhase::OperationPhase(Benchmark &benchmark, Operation &operation) :
        BenchmarkPhase(benchmark),
        operation(operation) {}

const char* OperationPhase::getName() {
    return "OperationPhase";
}

Status OperationPhase::execute() {
    LOGGER.info("Executing phase of type '%s' with %u operations of size %u bytes", operation.getClassName(),
                operation.getMeasurement().getOperationCount(), operation.getMeasurement().getOperationSize());

    if(!getBenchmark().synchronize()) {
        return Status::SYNC_ERROR;
    }

    return operation.execute();
}

void OperationPhase::calculateOverhead() {

}

void OperationPhase::saveSingleResult(std::string &path, std::string &operationSize, std::map<std::string, std::string> &valueMap) {

}

void OperationPhase::saveResults() {

}

}