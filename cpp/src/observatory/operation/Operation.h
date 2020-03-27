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

#ifndef OBSERVATORY_OPERATION_H
#define OBSERVATORY_OPERATION_H

#include <observatory/result/Measurement.h>
#include <observatory/result/OverheadMeasurement.h>
#include "observatory/Benchmark.h"

namespace Observatory {

class Operation {

public:

    Operation(Benchmark *benchmark, Benchmark::Mode mode, std::shared_ptr<Measurement> measurement);

    Operation(const Operation &other) = delete;

    Operation& operator=(const Operation &other) = delete;

    virtual ~Operation() = default;

    Benchmark& getBenchmark() const;

    Benchmark::Mode getMode() const;

    OverheadMeasurement& getOverheadMeasurement() const;

    void setOverheadMeasurement(std::shared_ptr<OverheadMeasurement> overheadMeasurement);

    virtual Measurement& getMeasurement() const;

    virtual const char* getClassName() const = 0;

    virtual Operation* instantiate(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize) const = 0;

    virtual const char* getOutputFilename() const = 0;

    virtual bool needsFilledReceiveQueue() const = 0;

    virtual Status warmUp(uint32_t operationCount) = 0;

    virtual Status execute() = 0;

private:

    Benchmark *benchmark{};
    Benchmark::Mode mode;
    std::shared_ptr<Measurement> measurement;
    std::shared_ptr<OverheadMeasurement> overheadMeasurement;

};

}

#endif
