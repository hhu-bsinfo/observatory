#include "RdmaLatencyOperation.h"

namespace Observatory {

RdmaLatencyOperation::RdmaLatencyOperation(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize, Benchmark::RdmaMode rdmaMode) :
        LatencyOperation(benchmark, mode, operationCount, operationSize),
        rdmaMode(rdmaMode) {}

const char *RdmaLatencyOperation::getClassName() const {
    return "RdmaLatencyOperation";
}

bool RdmaLatencyOperation::needsFilledReceiveQueue() const {
    return getMode() != Benchmark::Mode::SEND;
}

Status RdmaLatencyOperation::warmUp(uint32_t operationCount) {
    Status status = Status::OK;

    if(getMode() == Benchmark::Mode::SEND) {
        for(uint32_t i = 0; i < operationCount; i++) {
            status = getBenchmark().performSingleRdmaOperation(rdmaMode);

            if(status != Status::OK) {
                return status;
            }
        }
    }

    if(!getBenchmark().synchronize()) {
        return Status::SYNC_ERROR;
    }

    return status;
}

Status RdmaLatencyOperation::execute() {
    Status status = Status::OK;

    if(getMode() == Benchmark::Mode::SEND) {
        auto startTime = std::chrono::steady_clock::now();

        for(uint32_t i = 0; i < getMeasurement().getOperationCount(); i++) {
            getMeasurement().startSingleMeasurement();
            status = getBenchmark().performSingleRdmaOperation(rdmaMode);
            getMeasurement().stopSingleMeasurement();

            if(status != Status::OK) {
                return status;
            }
        }

        uint64_t time = std::chrono::duration_cast<std::chrono::nanoseconds>(std::chrono::steady_clock::now() - startTime).count();
        getMeasurement().finishMeasuring(time);
    }

    if(!getBenchmark().synchronize()) {
        return Status::SYNC_ERROR;
    }

    return status;
}

}