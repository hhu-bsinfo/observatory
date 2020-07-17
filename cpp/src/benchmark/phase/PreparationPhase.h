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

#ifndef OBSERVATORY_PREPARATIONPHASE_H
#define OBSERVATORY_PREPARATIONPHASE_H

#include <memory>
#include "BenchmarkPhase.h"

namespace Observatory {

class PreparationPhase : public BenchmarkPhase {

public:

    PreparationPhase(Benchmark &benchmark, uint32_t operationSize, uint32_t operationCount);

    PreparationPhase(const PreparationPhase &other) = delete;

    PreparationPhase& operator=(const PreparationPhase &other) = delete;

    ~PreparationPhase() override = default;

    const char* getName() override;

    Status execute() override;

private:

    uint32_t operationSize;
    uint32_t operationCount;

};

}

#endif
