#include <sstream>
#include <benchmark/util/ValueFormatter.h>
#include "LatencyMeasurement.h"

namespace Observatory {

LatencyMeasurement::LatencyMeasurement(uint32_t operationCount, uint32_t operationSize) :
        Measurement(operationCount, operationSize),
        latencyStatistics(operationCount) {}

void LatencyMeasurement::startSingleMeasurement() {
    latencyStatistics.start();
}

void LatencyMeasurement::stopSingleMeasurement() {
    latencyStatistics.stop();
}

void LatencyMeasurement::finishMeasuring(uint64_t timeInNanos) {
    totalTime = timeInNanos / 1000000000.0;

    operationThroughput = static_cast<double>(getOperationCount()) / totalTime;
    latencyStatistics.sortAscending();
}

double LatencyMeasurement::getTotalTime() const {
    return totalTime;
}

double LatencyMeasurement::getAverageLatency() const {
    return latencyStatistics.getAvgNs() / 1000000000;
}

double LatencyMeasurement::getMinimumLatency() const {
    return latencyStatistics.getMinNs() / 1000000000;
}

double LatencyMeasurement::getMaximumLatency() const {
    return latencyStatistics.getMaxNs() / 1000000000;
}

double LatencyMeasurement::getPercentileLatency(float percentile) const {
    return latencyStatistics.getPercentileNs(percentile) / 1000000000;
}

double LatencyMeasurement::getOperationThroughput() const {
    return operationThroughput;
}

LatencyMeasurement::operator std::string() const {
    std::ostringstream stream;

    stream << "ThroughputMeasurement {"
            << "\n\t" << ValueFormatter::formatValue("operationCount", getOperationCount())
            << ",\n\t" << ValueFormatter::formatValue("operationSize", getOperationSize(), "Byte")
            << ",\n\t" << ValueFormatter::formatValue("totalData", getTotalData(), "Byte")
            << ",\n\t" << ValueFormatter::formatValue("averageLatency", getAverageLatency(), "s")
            << ",\n\t" << ValueFormatter::formatValue("minimumLatency", getMinimumLatency(), "s")
            << ",\n\t" << ValueFormatter::formatValue("maximumLatency", getMaximumLatency(), "s")
            << ",\n\t" << ValueFormatter::formatValue("50% Latency", getPercentileLatency(0.5f), "s")
            << ",\n\t" << ValueFormatter::formatValue("90% Latency", getPercentileLatency(0.9f), "s")
            << ",\n\t" << ValueFormatter::formatValue("95% Latency", getPercentileLatency(0.95f), "s")
            << ",\n\t" << ValueFormatter::formatValue("99% Latency", getPercentileLatency(0.99f), "s")
            << ",\n\t" << ValueFormatter::formatValue("99.9% Latency", getPercentileLatency(0.999f), "s")
            << ",\n\t" << ValueFormatter::formatValue("99.99% Latency", getPercentileLatency(0.9999f), "s")
            << "\n}";

    return stream.str();
}
}