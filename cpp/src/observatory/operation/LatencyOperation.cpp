#include "LatencyOperation.h"

namespace Observatory {

LatencyOperation::LatencyOperation(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize) :
        Operation(benchmark, mode, std::make_shared<LatencyMeasurement>(operationCount, operationSize)) {

}

LatencyMeasurement& LatencyOperation::getMeasurement() const {
    return dynamic_cast<LatencyMeasurement&>(Operation::getMeasurement());
}

const char *LatencyOperation::getClassName() const {
    return "LatencyOperation";
}

}