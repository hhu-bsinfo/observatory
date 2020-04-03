/*
 * Copyright (C) 2020 Heinrich-Heine-Universitaet Duesseldorf,
 * Institute of Computer Science, Department Operating Systems
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

#ifndef OBSERVATORY_LATENCYSTATISTICS_H
#define OBSERVATORY_LATENCYSTATISTICS_H

#include <vector>
#include <chrono>
#include <cstdint>

namespace Observatory {

class LatencyStatistics {

public:

    explicit LatencyStatistics(uint32_t size);

    LatencyStatistics(const LatencyStatistics &other) = default;

    LatencyStatistics& operator=(const LatencyStatistics &other) = delete;

    ~LatencyStatistics() = default;

    void start();

    void stop();

    void sortAscending();

    double getMinNs() const;

    double getMaxNs() const;

    double getTotalNs() const;

    double getAvgNs() const;

    double getPercentileNs(float percentile) const;

private:

    std::vector<uint64_t> times;
    uint32_t pos;

    std::chrono::time_point<std::chrono::steady_clock, std::chrono::nanoseconds> tmpTime;

};

}

#endif
