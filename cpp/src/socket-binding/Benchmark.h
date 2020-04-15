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

#ifndef OBSERVATORY_SOCKET_BENCHMARK_H
#define OBSERVATORY_SOCKET_BENCHMARK_H

#include <observatory/Benchmark.h>
#include <observatory/util/BenchmarkFactory.h>

namespace Socket {

class Benchmark : public Observatory::Benchmark {

public:

    Benchmark() = default;

    Benchmark(const Benchmark &other) = delete;

    Benchmark& operator=(const Benchmark &other) = delete;

    ~Benchmark() override = default;

    BENCHMARK_IMPLEMENT_INSTANTIATE(Socket::Benchmark);

    const char* getClassName() const override ;

    Observatory::Status initialize() override;

    Observatory::Status serve(Observatory::SocketAddress &bindAddress) override;

    Observatory::Status connect(Observatory::SocketAddress &bindAddress, Observatory::SocketAddress &remoteAddress) override;

    Observatory::Status prepare(uint32_t operationSize, uint32_t operationCount) override;

    Observatory::Status cleanup() override;

    Observatory::Status fillReceiveQueue() override;

    Observatory::Status sendMultipleMessages(uint32_t messageCount) override;

    Observatory::Status receiveMultipleMessages(uint32_t messageCount) override;

    Observatory::Status performMultipleRdmaOperations(RdmaMode mode, uint32_t operationCount) override;

    Observatory::Status sendSingleMessage() override;

    Observatory::Status performSingleRdmaOperation(RdmaMode mode) override;

    Observatory::Status performPingPongIterationServer() override;

    Observatory::Status performPingPongIterationClient() override;

private:

    log4cpp::Category &LOGGER = log4cpp::Category::getInstance(getClassName());

    int socket{};

    uint8_t *buffer{};
    uint32_t bufferSize{};
    
};

}

#endif
