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

#ifndef OBSERVATORY_MEASUREMENT_H
#define OBSERVATORY_MEASUREMENT_H

#include <cstdint>
#include <string>

namespace Observatory {

class Measurement {

public:

    Measurement(uint32_t operationCount, uint32_t operationSize);

    Measurement(const Measurement &other) = default;

    Measurement &operator=(const Measurement &other) = delete;

    ~Measurement() = default;

    uint32_t getOperationCount() const;

    uint32_t getOperationSize() const;

    uint64_t getTotalData() const;

    void setTotalData(uint64_t totalData);

    virtual double getTotalTime() const = 0;

    virtual explicit operator std::string() const;

private:

    uint32_t operationCount;
    uint32_t operationSize;

    uint64_t totalData{};

};

}

#endif
