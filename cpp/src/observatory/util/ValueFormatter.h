#ifndef OBSERVATORY_VALUEFORMATTER_H
#define OBSERVATORY_VALUEFORMATTER_H

#include <string>

namespace Observatory {

class ValueFormatter {

public:

    ValueFormatter() = delete;

    ValueFormatter(const ValueFormatter &other) = delete;

    ValueFormatter &operator=(const ValueFormatter &other) = delete;

    ~ValueFormatter() = delete;

    static std::string formatValue(double value, const char *unit);

    static std::string formatValue(const char *name, double value, const char *unit = "Units");

private:

    static const char highMetricTable[];

    static const char lowMetricTable[];

    static std::string formatHighValue(double value, const char *unit);

    static std::string formatLowValue(double value, const char *unit);
};

}

#endif
