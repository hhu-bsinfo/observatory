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

#ifndef OBSERVATORY_BENCHMARKPHASE_H
#define OBSERVATORY_BENCHMARKPHASE_H

#include <string>
#include "result/Status.h"

namespace Observatory {

class Benchmark;

class BenchmarkPhase {

public:

    explicit BenchmarkPhase(Benchmark &benchmark);

    BenchmarkPhase(const BenchmarkPhase &other) = default;

    BenchmarkPhase& operator=(const BenchmarkPhase &other) = delete;

    virtual ~BenchmarkPhase() = default;

    Benchmark& getBenchmark() const;

    virtual std::string getClassName() const = 0;

    virtual BenchmarkPhase* clone(Benchmark &benchmark) const = 0;

    virtual Result::Status execute() = 0;

private:

    Benchmark &benchmark;

};

}

#endif
