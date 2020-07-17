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

#ifndef OBSERVATORY_THROUGHPUTMEASUREMENT_H
#define OBSERVATORY_THROUGHPUTMEASUREMENT_H

#include "Measurement.h"

namespace Observatory {

class ThroughputMeasurement : public Measurement {

public:

    ThroughputMeasurement(uint32_t operationCount, uint32_t operationSize);

    ThroughputMeasurement(const ThroughputMeasurement &other) = default;

    ThroughputMeasurement& operator=(const ThroughputMeasurement &other) = delete;

    ~ThroughputMeasurement() = default;

    double getTotalTime() const override;

    double getOperationThroughput() const;

    double getDataThroughput() const;

    void setTotalTime(double totalTime);

    void setOperationThroughput(double operationThroughput);

    void setDataThroughput(double dataThroughput);

    void setMeasuredTime(uint64_t timeInNanos);

    explicit operator std::string() const override;

private:

    double totalTime{};
    double operationThroughput{};
    double dataThroughput{};

};

}

#endif
