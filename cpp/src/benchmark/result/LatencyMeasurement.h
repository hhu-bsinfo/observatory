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

#ifndef OBSERVATORY_LATENCYMEASUREMENT_H
#define OBSERVATORY_LATENCYMEASUREMENT_H

#include "LatencyStatistics.h"
#include "Measurement.h"

namespace Observatory {

class LatencyMeasurement : public Measurement {

public:

    LatencyMeasurement(uint32_t operationCount, uint32_t operationSize);

    LatencyMeasurement(const LatencyMeasurement &other) = default;

    LatencyMeasurement& operator=(const LatencyMeasurement &other) = delete;

    ~LatencyMeasurement() = default;

    void startSingleMeasurement();

    void stopSingleMeasurement();

    void finishMeasuring(uint64_t timeInNanos);

    double getTotalTime() const override;

    double getAverageLatency() const;

    double getMinimumLatency() const;

    double getMaximumLatency() const;

    double getPercentileLatency(float percentile) const;

    double getOperationThroughput() const;

    explicit operator std::string() const override;

private:

    double totalTime{};
    double operationThroughput{};
    LatencyStatistics latencyStatistics;

};

}

#endif
