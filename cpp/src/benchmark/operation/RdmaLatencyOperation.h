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

#ifndef OBSERVATORY_RDMALATENCYOPERATION_H
#define OBSERVATORY_RDMALATENCYOPERATION_H

#include "LatencyOperation.h"

namespace Observatory {

class RdmaLatencyOperation : public LatencyOperation {

public:

    RdmaLatencyOperation(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize, Benchmark::RdmaMode rdmaMode);

    RdmaLatencyOperation(const RdmaLatencyOperation &other) = delete;

    RdmaLatencyOperation& operator=(const RdmaLatencyOperation &other) = delete;

    ~RdmaLatencyOperation() override = default;

    const char* getClassName() const override;

    bool needsFilledReceiveQueue() const override;

    Status warmUp(uint32_t operationCount) override;

    Status execute() override;

private:

    Benchmark::RdmaMode rdmaMode;

};

}

#endif
