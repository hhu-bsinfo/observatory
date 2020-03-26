#include <chrono>
#include "MessagingThroughputOperation.h"

namespace Observatory {

MessagingThroughputOperation::MessagingThroughputOperation(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize) :
        ThroughputOperation(benchmark, mode, operationCount, operationSize) {}

const char *MessagingThroughputOperation::getClassName() const {
    return "MessagingThroughputOperation";
}

const char* MessagingThroughputOperation::getOutputFilename() const {
    return "Messaging Throughput";
}

bool MessagingThroughputOperation::needsFilledReceiveQueue() const {
    return getMode() != Benchmark::Mode::SEND;
}

Status MessagingThroughputOperation::warmUp(uint32_t operationCount) {
    if(getMode() == Benchmark::Mode::SEND) {
        return getBenchmark().sendMultipleMessages(operationCount);
    } else {
        return getBenchmark().receiveMultipleMessages(operationCount);
    }
}

Status MessagingThroughputOperation::execute() {
    if(getMode() == Benchmark::Mode::SEND) {
        auto startTime = std::chrono::high_resolution_clock::now();
        Status status = getBenchmark().sendMultipleMessages(getMeasurement().getOperationCount());
        uint64_t time = std::chrono::duration_cast<std::chrono::nanoseconds>(std::chrono::high_resolution_clock::now() - startTime).count();

        getMeasurement().setMeasuredTime(time);

        return status;
    } else {
        auto startTime = std::chrono::high_resolution_clock::now();
        Status status = getBenchmark().receiveMultipleMessages(getMeasurement().getOperationCount());
        uint64_t time = std::chrono::duration_cast<std::chrono::nanoseconds>(std::chrono::high_resolution_clock::now() - startTime).count();

        getMeasurement().setMeasuredTime(time);

        return status;
    }
}

}
