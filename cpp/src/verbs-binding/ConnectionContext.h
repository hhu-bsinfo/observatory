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

#ifndef OBSERVATORY_CONNECTIONCONTEXT_H
#define OBSERVATORY_CONNECTIONCONTEXT_H

#include <log4cpp/Category.hh>
#include <infiniband/verbs.h>

namespace Verbs {

class ConnectionContext {

public:

    ConnectionContext(uint32_t deviceNumber, uint8_t portNumber, uint32_t queueSize);

    ConnectionContext(ConnectionContext &other) = delete;

    ConnectionContext& operator=(ConnectionContext &other) = delete;

    ~ConnectionContext();

    void connect(int socket);

    ibv_pd* getProtectionDomain();

    ibv_cq* getSendCompletionQueue();

    ibv_cq* getReceiveCompletionQueue();

    ibv_qp* getQueuePair();

private:

    log4cpp::Category &LOGGER = log4cpp::Category::getInstance("ConnectionContext");

    ibv_context *context{};
    ibv_pd *protectionDomain{};

    ibv_cq *sendCompletionQueue{};
    ibv_cq *receiveCompletionQueue{};

    ibv_qp *queuePair{};

    uint8_t portNumber;
    ibv_port_attr port{};

};

}

#endif
