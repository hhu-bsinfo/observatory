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

#ifndef OBSERVATORY_OVERHEADMEASUREMENT_H
#define OBSERVATORY_OVERHEADMEASUREMENT_H

#include "Measurement.h"

namespace Observatory {

class OverheadMeasurement {

public:

    OverheadMeasurement(uint64_t rawTotalData, const Measurement &measurement);

    OverheadMeasurement(const OverheadMeasurement &other) = default;

    OverheadMeasurement &operator=(const OverheadMeasurement &other) = delete;

    ~OverheadMeasurement() = default;

    uint64_t getRawTotalData();

    double getRawDataThroughput();

    double getOverheadFactor();

    double getOverheadDataThroughput();

    explicit operator std::string() const;

private:

    uint64_t rawTotalData;
    double overheadData;
    double overheadFactor;

    double rawDataThroughput{};
    double overheadDataThroughput{};

};

}

#endif
