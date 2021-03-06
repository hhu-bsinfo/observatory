/*
 * Copyright (C) 2020 Heinrich-Heine-Universitaet Duesseldorf,
 * Heinrich-Heine University
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

#ifndef OBSERVATORY_FILLRECEIVEQUEUEPHASE_H
#define OBSERVATORY_FILLRECEIVEQUEUEPHASE_H

#include <memory>
#include <benchmark/operation/Operation.h>
#include "BenchmarkPhase.h"

namespace Observatory {

class FillReceiveQueuePhase : public BenchmarkPhase {

public:

    explicit FillReceiveQueuePhase(Benchmark &benchmark, const Operation &operation);

    FillReceiveQueuePhase(const FillReceiveQueuePhase &other) = delete;

    FillReceiveQueuePhase& operator=(const FillReceiveQueuePhase &other) = delete;

    ~FillReceiveQueuePhase() override = default;

    const char* getName() override;

    Status execute() override;

private:

    bool fillReceiveQueue;

};

}

#endif
