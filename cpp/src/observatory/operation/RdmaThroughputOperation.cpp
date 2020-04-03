#include <chrono>
#include "RdmaThroughputOperation.h"

namespace Observatory {

RdmaThroughputOperation::RdmaThroughputOperation(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize, Benchmark::RdmaMode rdmaMode) :
        ThroughputOperation(benchmark, mode, operationCount, operationSize),
        rdmaMode(rdmaMode) {

}

const char *RdmaThroughputOperation::getClassName() const {
    return "RdmaThroughputOperation";
}

bool RdmaThroughputOperation::needsFilledReceiveQueue() const {
    return false;
}

Status RdmaThroughputOperation::warmUp(uint32_t operationCount) {
    Status status = Status::OK;

    if(getMode() == Benchmark::Mode::SEND) {
        status = getBenchmark().performMultipleRdmaOperations(rdmaMode, operationCount);
    }

    if(!getBenchmark().synchronize()) {
        return Status::SYNC_ERROR;
    }

    return status;
}

Status RdmaThroughputOperation::execute() {
    if(getMode() == Benchmark::Mode::SEND) {
        auto startTime = std::chrono::steady_clock::now();
        Status status = getBenchmark().performMultipleRdmaOperations(rdmaMode, getMeasurement().getOperationCount());
        uint64_t time = std::chrono::duration_cast<std::chrono::nanoseconds>(std::chrono::steady_clock::now() - startTime).count();

        if(status != Status::OK) {
            return status;
        }

        getMeasurement().setMeasuredTime(time);

        if(::send(getBenchmark().getOffChannelSocket(), &time, sizeof(uint64_t), 0) < 0) {
            LOGGER.error("Sending measured time to remote benchmark failed (%s)", std::strerror(errno));
            return Status::NETWORK_ERROR;
        }

        return status;
    } else {
        uint64_t time;

        if(::recv(getBenchmark().getOffChannelSocket(), &time, sizeof(uint64_t), 0) < 0) {
            LOGGER.error("Receiving measured time from remote benchmark failed (%s)", std::strerror(errno));
            return Status::NETWORK_ERROR;
        }

        return Status::OK;
    }
}

}