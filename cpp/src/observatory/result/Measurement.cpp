#include <sstream>
#include <observatory/util/ValueFormatter.h>
#include "Measurement.h"

namespace Observatory {

Measurement::Measurement(uint32_t operationCount, uint32_t operationSize) :
        operationCount(operationCount),
        operationSize(operationSize) {}

uint32_t Measurement::getOperationCount() const {
    return operationCount;
}

uint32_t Measurement::getOperationSize() const {
    return operationSize;
}

uint64_t Measurement::getTotalData() const {
    return totalData;
}

void Measurement::setTotalData(uint64_t totalData) {
    this->totalData = totalData;
}

Measurement::operator std::string() const {
    std::ostringstream stream;

    stream << "Measurement {"
            << "\n\t" << ValueFormatter::formatValue("operationCount", operationCount)
            << ",\n\t" << ValueFormatter::formatValue("operationSize", operationSize, "Byte")
            << ",\n\t" << ValueFormatter::formatValue("totalData", totalData, "Byte")
            << "\n}";

    return stream.str();
}

}