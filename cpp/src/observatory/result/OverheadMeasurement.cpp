#include <sstream>
#include <observatory/util/ValueFormatter.h>
#include "OverheadMeasurement.h"

namespace Observatory {

OverheadMeasurement::OverheadMeasurement(uint64_t rawTotalData, const Measurement &measurement) :
        rawTotalData(rawTotalData) {
    overheadData = static_cast<double>(rawTotalData) - measurement.getTotalData();

    // This happens, when the benchmark is executed on an ethernet connection
    if(overheadData < 0) {
        overheadData = 0;
    }

    overheadFactor = static_cast<double>(rawTotalData) / measurement.getTotalData();

    rawDataThroughput = static_cast<double>(rawTotalData) / measurement.getTotalTime();
    overheadDataThroughput = overheadData / measurement.getTotalTime();
}

uint64_t OverheadMeasurement::getRawTotalData() {
    return rawTotalData;
}

double OverheadMeasurement::getRawDataThroughput() {
    return rawDataThroughput;
}

double OverheadMeasurement::getOverheadData() {
    return overheadData;
}

double OverheadMeasurement::getOverheadFactor() {
    return overheadFactor;
}

double OverheadMeasurement::getOverheadDataThroughput() {
    return overheadDataThroughput;
}

OverheadMeasurement::operator std::string() const {
    std::ostringstream stream;

    stream << "OverheadMeasurement {"
           << "\n\t" << ValueFormatter::formatValue("rawTotalData", rawTotalData, "Byte")
           << ",\n\t" << ValueFormatter::formatValue("overheadData", overheadData, "Byte")
           << ",\n\t" << ValueFormatter::formatValue("overheadFactor", overheadFactor)
           << ",\n\t" << ValueFormatter::formatValue("rawDataThroughput", rawDataThroughput, "Byte/s")
           << ",\n\t" << ValueFormatter::formatValue("overheadDataThroughput", overheadDataThroughput, "Byte/s")
           << "\n}";

    return stream.str();
}

}