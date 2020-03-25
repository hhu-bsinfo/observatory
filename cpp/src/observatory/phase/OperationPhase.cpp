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
    return Status::NOT_IMPLEMENTED;
}

void OperationPhase::calculateOverhead() {

}

void OperationPhase::saveSingleResult(std::string &path, std::string &operationSize, std::map<std::string, std::string> &valueMap) {

}

void OperationPhase::saveResults() {

}

}