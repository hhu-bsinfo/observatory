#include "ThroughputOperation.h"

namespace Observatory {

ThroughputOperation::ThroughputOperation(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize) :
        Operation(benchmark, mode, std::make_shared<ThroughputMeasurement>(operationCount, operationSize)) {

}

ThroughputMeasurement& ThroughputOperation::getMeasurement() const {
    return dynamic_cast<ThroughputMeasurement&>(Operation::getMeasurement());
}

const char *ThroughputOperation::getClassName() const {
    return "ThroughputOperation";
}

}