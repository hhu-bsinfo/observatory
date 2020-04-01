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

#ifndef OBSERVATORY_VERBS_CONNECTIONINFORMATION_H
#define OBSERVATORY_VERBS_CONNECTIONINFORMATION_H

#include <cstdint>
#include <memory>

namespace Verbs {

class ConnectionInformation {

public:

    ConnectionInformation(uint8_t portNumber, uint16_t localId, uint32_t queuePairNumber);

    ConnectionInformation(const ConnectionInformation &other) = default;

    ConnectionInformation& operator=(const ConnectionInformation &other) = default;

    ~ConnectionInformation() = default;

    static ConnectionInformation fromBytes(const uint8_t *bytes);

    std::shared_ptr<uint8_t[]> toBytes();

    uint8_t getPortNumber();

    uint16_t getLocalId();

    uint32_t getQueuePairNumber();

    operator std::string();

private:

    uint8_t portNumber;
    uint16_t localId;
    uint32_t queuePairNumber;

};

}

#endif
