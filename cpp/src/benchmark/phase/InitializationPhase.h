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

#ifndef OBSERVATORY_INITIALIZATIONPHASE_H
#define OBSERVATORY_INITIALIZATIONPHASE_H

#include <memory>
#include "BenchmarkPhase.h"

namespace Observatory {

class InitializationPhase : public BenchmarkPhase {

public:

    explicit InitializationPhase(Benchmark &benchmark);

    InitializationPhase(const InitializationPhase &other) = delete;

    InitializationPhase& operator=(const InitializationPhase &other) = delete;

    ~InitializationPhase() override = default;

    const char* getName() override;

    Status execute() override;

};

}

#endif
