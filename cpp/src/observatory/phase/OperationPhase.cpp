#include "OperationPhase.h"
#include <observatory/Benchmark.h>
#include <detector/exception/IbPerfException.h>

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

    if(getBenchmark().measureOverhead()) {
        try {
            getBenchmark().getPerfCounter().ResetCounters();
        } catch(Detector::IbPerfException &e) {
            LOGGER.error("Unable to reset performance counters\n\033[0m %s", e.what());
            return Status::NETWORK_ERROR;
        }
    }

    Status status = operation.execute();

    if(status == Status::OK && getBenchmark().measureOverhead()) {
        Status overheadStatus = calculateOverhead();
        if(overheadStatus != Status::OK) {
            LOGGER.error("Measuring overhead failed with status [%s]", getStatusString(overheadStatus));
            return overheadStatus;
        }
    }

    if(status == Status::OK && getBenchmark().isServer()) {
        if(getBenchmark().measureOverhead()) {
            LOGGER.info("Operation finished with results:\n%s,\n%s", static_cast<std::string>(operation.getMeasurement()).c_str(), static_cast<std::string>(operation.getOverheadMeasurement()).c_str());
        } else {
            LOGGER.info("Operation finished with results:\n%s", static_cast<std::string>(operation.getMeasurement()).c_str());
        }

        try {
            saveResults();
        } catch (std::runtime_error &e) {
            LOGGER.error("Unable to save results\n\033[0m %s", e.what());
            return Status::FILE_ERROR;
        }
    }

    return status;
}

Status OperationPhase::calculateOverhead() {
    return Status::NOT_IMPLEMENTED;
}

void OperationPhase::saveSingleResult(std::string &path, std::string &operationSize, std::map<std::string, std::string> &valueMap) {

}

void OperationPhase::saveResults() {

}

}