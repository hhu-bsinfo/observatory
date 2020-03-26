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

#ifndef OBSERVATORY_RDMAREADTHROUGHPUTOPERATION_H
#define OBSERVATORY_RDMAREADTHROUGHPUTOPERATION_H

#include <observatory/util/OperationFactory.h>
#include "RdmaThroughputOperation.h"

namespace Observatory {

class RdmaReadThroughputOperation  : public RdmaThroughputOperation {

public:

    RdmaReadThroughputOperation(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize);

    RdmaReadThroughputOperation(const RdmaReadThroughputOperation &other) = delete;

    RdmaReadThroughputOperation& operator=(const RdmaReadThroughputOperation &other) = delete;

    ~RdmaReadThroughputOperation() override = default;

    OPERATION_IMPLEMENT_INSTANTIATE(Observatory::RdmaReadThroughputOperation)

    const char* getClassName() const override;

    const char* getOutputFilename() const override;

};

}

#endif
