#include "MessagingPingPongOperation.h"

namespace Observatory {

MessagingPingPongOperation::MessagingPingPongOperation(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize) :
        LatencyOperation(benchmark, mode, operationCount, operationSize) {}

const char *MessagingPingPongOperation::getClassName() const {
    return "MessagingPingPongOperation";
}

const char* MessagingPingPongOperation::getOutputFilename() const {
    return "Messaging PingPong";
}

bool MessagingPingPongOperation::needsFilledReceiveQueue() const {
    return getMode() != Benchmark::Mode::SEND;
}

Status MessagingPingPongOperation::warmUp(uint32_t operationCount) {
    if(getMode() == Benchmark::Mode::SEND) {
        for(uint32_t i = 0; i < operationCount; i++) {
            Status status = getBenchmark().performPingPongIterationServer();

            if(status != Status::OK) {
                return status;
            }
        }
    } else {
        for(uint32_t i = 0; i < operationCount; i++) {
            Status status = getBenchmark().performPingPongIterationServer();

            if(status != Status::OK) {
                return status;
            }
        }
    }

    return Status::OK;
}

Status MessagingPingPongOperation::execute() {
    if (getMode() == Benchmark::Mode::SEND) {
        auto startTime = std::chrono::high_resolution_clock::now();

        for (uint32_t i = 0; i < getMeasurement().getOperationCount(); i++) {
            getMeasurement().startSingleMeasurement();
            Status status = getBenchmark().performPingPongIterationServer();
            getMeasurement().stopSingleMeasurement();

            if (status != Status::OK) {
                return status;
            }
        }

        uint64_t time = std::chrono::duration_cast<std::chrono::nanoseconds>(std::chrono::high_resolution_clock::now() - startTime).count();
        getMeasurement().finishMeasuring(time);
    } else {
        for (uint32_t i = 0; i < getMeasurement().getOperationCount(); i++) {
            Status status = getBenchmark().performPingPongIterationServer();

            if (status != Status::OK) {
                return status;
            }
        }
    }

    return Status::OK;
}

}