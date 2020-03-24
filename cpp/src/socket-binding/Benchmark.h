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

#ifndef OBSERVATORY_SOCKETBENCHMARK_H
#define OBSERVATORY_SOCKETBENCHMARK_H

#include <observatory/Benchmark.h>
#include <observatory/util/BenchmarkFactory.h>

namespace Socket {

class Benchmark : public Observatory::Benchmark {

public:

    Benchmark() = default;

    Benchmark(const Benchmark &other) = default;

    Benchmark& operator=(const Benchmark &other) = delete;

    ~Benchmark() override = default;

    BENCHMARK_IMPLEMENT_CLONE(Socket::Benchmark);

    std::string getClassName() const override ;

    Observatory::Result::Status initialize() override;

    Observatory::Result::Status isServer(std::string &bindAddress) override;

    Observatory::Result::Status connect(std::string &bindAddress, std::string &remoteAddress) override;

    Observatory::Result::Status prepare(uint32_t operationSize) override;

    Observatory::Result::Status cleanup() override;

    Observatory::Result::Status fillReceiveQueue() override;

    Observatory::Result::Status sendMultipleMessage(uint32_t messageCount) override;

    Observatory::Result::Status receiveMultipleMessage(uint32_t messageCount) override;

    Observatory::Result::Status performMultipleRdmaOperations(RdmaMode mode, uint32_t operationCount) override;

    Observatory::Result::Status sendSingleMessage() override;

    Observatory::Result::Status performSingleRdmaOperation(RdmaMode mode) override;

    Observatory::Result::Status performPingPongIterationServer() override;

    Observatory::Result::Status performPingPongIterationClient() override;
    
};

}

#endif
