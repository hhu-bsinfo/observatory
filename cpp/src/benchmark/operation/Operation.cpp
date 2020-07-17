#include "Operation.h"

#include <utility>

namespace Observatory {

Operation::Operation(Observatory::Benchmark *benchmark, Observatory::Benchmark::Mode mode, std::shared_ptr<Measurement> measurement) :
        benchmark(benchmark),
        mode(mode),
        measurement(std::move(measurement)) {}

Benchmark& Operation::getBenchmark() const {
    return *benchmark;
}

Benchmark::Mode Operation::getMode() const {
    return mode;
}

OverheadMeasurement& Operation::getOverheadMeasurement() const {
    return *overheadMeasurement;
}

Measurement& Operation::getMeasurement() const {
    return *measurement;
}

void Operation::setOverheadMeasurement(std::shared_ptr<OverheadMeasurement> overheadMeasurement) {
    this->overheadMeasurement = std::move(overheadMeasurement);
}

}