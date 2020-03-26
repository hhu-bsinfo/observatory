#include "MessagingLatencyOperation.h"

namespace Observatory {

MessagingLatencyOperation::MessagingLatencyOperation(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize) :
        LatencyOperation(benchmark, mode, operationCount, operationSize) {}

const char *MessagingLatencyOperation::getClassName() const {
    return "MessagingLatencyOperation";
}

const char* MessagingLatencyOperation::getOutputFilename() const {
    return "Messaging Latency";
}

bool MessagingLatencyOperation::needsFilledReceiveQueue() const {
    return getMode() != Benchmark::Mode::SEND;
}

Status MessagingLatencyOperation::warmUp(uint32_t operationCount) {
    if(getMode() == Benchmark::Mode::SEND) {
        for(uint32_t i = 0; i < operationCount; i++) {
            Status status = getBenchmark().sendSingleMessage();

            if(status != Status::OK) {
                return status;
            }
        }

        return Status::OK;
    } else {
        return getBenchmark().receiveMultipleMessages(operationCount);
    }
}

Status MessagingLatencyOperation::execute() {
    if(getMode() == Benchmark::Mode::SEND) {
        auto startTime = std::chrono::high_resolution_clock::now();

        for(uint32_t i = 0; i < getMeasurement().getOperationCount(); i++) {
            getMeasurement().startSingleMeasurement();
            Status status = getBenchmark().sendSingleMessage();
            getMeasurement().stopSingleMeasurement();

            if(status != Status::OK) {
                return status;
            }
        }

        uint64_t time = std::chrono::duration_cast<std::chrono::nanoseconds>(std::chrono::high_resolution_clock::now() - startTime).count();
        getMeasurement().finishMeasuring(time);

        return Status::OK;
    } else {
        return getBenchmark().receiveMultipleMessages(getMeasurement().getOperationCount());
    }
}

}