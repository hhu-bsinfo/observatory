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

#ifndef OBSERVATORY_RDMAWRITETHROUGHPUTOPERATION_H
#define OBSERVATORY_RDMAWRITETHROUGHPUTOPERATION_H

#include <benchmark/util/OperationFactory.h>
#include "RdmaThroughputOperation.h"

namespace Observatory {

class RdmaWriteThroughputOperation : public RdmaThroughputOperation {

public:

    RdmaWriteThroughputOperation(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize);

    RdmaWriteThroughputOperation(const RdmaWriteThroughputOperation &other) = delete;

    RdmaWriteThroughputOperation& operator=(const RdmaWriteThroughputOperation &other) = delete;

    ~RdmaWriteThroughputOperation() override = default;

    OPERATION_IMPLEMENT_INSTANTIATE(Observatory::RdmaWriteThroughputOperation)

    const char* getClassName() const override;

    const char* getOutputFilename() const override;

};

}

#endif
