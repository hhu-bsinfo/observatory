#include <sstream>
#include <iomanip>
#include "ValueFormatter.h"

namespace Observatory {

const char ValueFormatter::lowMetricTable[] = {
        0,
        'm',
        'u',
        'n',
        'p',
        'f',
        'a'
};

const char ValueFormatter::highMetricTable[] = {
        0,
        'K',
        'M',
        'G',
        'T',
        'P',
        'E'
};

std::string ValueFormatter::formatValue(double value, const char *unit) {
    if (value >= 1) {
        return formatHighValue(value, unit);
    } else {
        return formatLowValue(value, unit);
    }
}

std::string ValueFormatter::formatValue(const char *name, double value, const char *unit) {
    std::ostringstream stream;

    stream << name << ":" << std::setw(20) << formatValue(value, unit);

    return stream.str();
}

std::string ValueFormatter::formatHighValue(double value, const char *unit) {
    double formattedValue = value;

    uint32_t counter = 0;
    while (formattedValue > 1000 && formattedValue != 0 && counter < sizeof(highMetricTable) - 1) {
        formattedValue /= 1000;
        counter++;
    }

    std::ostringstream stream;

    stream << std::fixed << std::setprecision(3)
           << formattedValue << " "
           << highMetricTable[counter] << unit
           << " (" << (value == static_cast<uint64_t>(value) ? static_cast<uint64_t>(value) : value) << ")";

    return stream.str();
}

std::string ValueFormatter::formatLowValue(double value, const char *unit) {
    double formattedValue = value;

    uint32_t counter = 0;
    while (formattedValue < 1 && formattedValue != 0 && counter < sizeof(lowMetricTable) - 1) {
        formattedValue *= 1000;
        counter++;
    }

    std::ostringstream stream;

    stream << std::fixed << std::setprecision(3)
           << formattedValue << " "
           << lowMetricTable[counter] << unit
           << " (" << (value == static_cast<uint64_t>(value) ? static_cast<uint64_t>(value) : value) << ")";

    return stream.str();
}

}