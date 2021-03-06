#include <algorithm>
#include <numeric>
#include <stdexcept>
#include <cmath>
#include "LatencyStatistics.h"

namespace Observatory {

LatencyStatistics::LatencyStatistics(uint32_t size) :
        times(size),
        pos(0) {}

void LatencyStatistics::start() {
    tmpTime = std::chrono::steady_clock::now();
}

void LatencyStatistics::stop() {
    times[pos++] = std::chrono::duration_cast<std::chrono::nanoseconds>(std::chrono::steady_clock::now() - tmpTime).count();
}

void LatencyStatistics::sortAscending() {
    std::sort(times.begin(), times.end());
}

double LatencyStatistics::getMinNs() const {
    return *std::min_element(times.begin(), times.end());
}

double LatencyStatistics::getMaxNs() const {
    return *std::max_element(times.begin(), times.end());
}

double LatencyStatistics::getTotalNs() const {
    double tmp = 0;

    for (uint32_t i = 0; i < pos; i++) {
        tmp += times[i];
    }

    return tmp;
}

double LatencyStatistics::getAvgNs() const {
    return getTotalNs() / pos;
}

double LatencyStatistics::getPercentileNs(float percentile) const {
    if(percentile < 0.0 || percentile > 1.0) {
        throw std::runtime_error("Percentage must be between 0 and 1!");
    }

    return times[(uint32_t) std::ceil(percentile * pos) - 1];
}

}
