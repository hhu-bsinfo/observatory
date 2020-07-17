#include <sstream>
#include <benchmark/util/ValueFormatter.h>
#include "ThroughputMeasurement.h"

namespace Observatory {

ThroughputMeasurement::ThroughputMeasurement(uint32_t operationCount, uint32_t operationSize) :
        Measurement(operationCount, operationSize) {}

double ThroughputMeasurement::getTotalTime() const {
    return totalTime;
}

double ThroughputMeasurement::getOperationThroughput() const {
    return operationThroughput;
}

double ThroughputMeasurement::getDataThroughput() const {
    return dataThroughput;
}

void ThroughputMeasurement::setTotalTime(double totalTime) {
    this->totalTime = totalTime;
}

void ThroughputMeasurement::setOperationThroughput(double operationThroughput) {
    this->operationThroughput = operationThroughput;
}

void ThroughputMeasurement::setDataThroughput(double dataThroughput) {
    this->dataThroughput = dataThroughput;
}

void ThroughputMeasurement::setMeasuredTime(uint64_t timeInNanos) {
    this->totalTime = timeInNanos / 1000000000.0;

    operationThroughput = static_cast<double>(getOperationCount()) / totalTime;
    dataThroughput = static_cast<double>(getTotalData()) / totalTime;
}

ThroughputMeasurement::operator std::string() const {
    std::ostringstream stream;

    stream << "ThroughputMeasurement {"
            << "\n\t" << ValueFormatter::formatValue("operationCount", getOperationCount())
            << ",\n\t" << ValueFormatter::formatValue("operationSize", getOperationSize(), "Byte")
            << ",\n\t" << ValueFormatter::formatValue("totalData", getTotalData(), "Byte")
            << ",\n\t" << ValueFormatter::formatValue("totalTime", totalTime, "s")
            << ",\n\t" << ValueFormatter::formatValue("operationThroughput", operationThroughput, "Operations/s")
            << ",\n\t" << ValueFormatter::formatValue("dataThroughput", dataThroughput, "Byte/s")
            << "\n}";

    return stream.str();
}

}