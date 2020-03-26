#ifndef OBSERVATORY_VALUEFORMATTER_H
#define OBSERVATORY_VALUEFORMATTER_H

#include <string>

class ValueFormatter {

public:

    ValueFormatter() = delete;

    ValueFormatter(const ValueFormatter &other) = delete;

    ValueFormatter& operator=(const ValueFormatter &other) = delete;

    ~ValueFormatter() = delete;

    static std::string formatValue(double value, const char *unit);

    static std::string formatValue(const char *name, double value, const char *unit = "Units");

private:

    static const constexpr char highMetricTable[] = {
            0,
            'K',
            'M',
            'G',
            'T',
            'P',
            'E'
    };

    static const constexpr char lowMetricTable[] = {
            0,
            'm',
            'u',
            'n',
            'p',
            'f',
            'a'
    };

    static std::string formatHighValue(double value, const char *unit);

    static std::string formatLowValue(double value, const char *unit);
};

#endif
