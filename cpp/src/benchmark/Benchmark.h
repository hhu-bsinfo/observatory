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

#ifndef OBSERVATORY_BENCHMARK_H
#define OBSERVATORY_BENCHMARK_H

#include <log4cpp/Category.hh>
#include <nlohmann/json.hpp>
#include <detector/IbFabric.h>
#include <benchmark/result/Status.h>
#include "benchmark/phase/BenchmarkPhase.h"
#include "benchmark/util/SocketAddress.h"

namespace Observatory {

class Benchmark {

public:

    enum Mode {
        SEND, RECEIVE
    };

    enum RdmaMode {
        READ, WRITE
    };

public:

    Benchmark() = default;

    Benchmark(const Benchmark &other) = delete;

    Benchmark& operator=(const Benchmark &other) = delete;

    virtual ~Benchmark() = default;

    void addBenchmarkPhase(const std::shared_ptr<BenchmarkPhase>& phase);

    void setParameter(const char *key, const char *value);

    std::string getParameter(const char *key, const std::string defaultValue) const;

    uint8_t getParameter(const char *key, uint8_t defaultValue) const;

    uint16_t getParameter(const char *key, uint16_t defaultValue) const;

    uint32_t getParameter(const char *key, uint32_t defaultValue) const;

    uint64_t getParameter(const char *key, uint64_t defaultValue) const;

    int getOffChannelSocket() const;

    std::string getResultName() const;

    bool isServer() const;

    uint32_t getConnectionRetries() const;

    SocketAddress getBindAddress() const;

    SocketAddress getRemoteAddress() const;

    std::string getResultPath() const;

    int getIterationNumber() const;

    bool measureOverhead() const;

    Detector::IbPerfCounter& getPerfCounter();

    void setResultName(const char *resultName);

    void setServer(bool isServer);

    void setConnectionRetries(uint32_t connectionRetries);

    void setBindAddress(const SocketAddress &bindAddress);

    void setRemoteAddress(const SocketAddress &remoteAddress);

    void setResultPath(const char *resultPath);

    void setIterationNumber(uint32_t iterationNumber);

    void setDetectorConfig(const nlohmann::json &detectorConfig);

    Status setup();

    bool synchronize();

    void executePhases();

    virtual const char* getClassName() const = 0;

    virtual Benchmark* instantiate() const = 0;

    virtual Status initialize() = 0;

    virtual Status serve(SocketAddress &bindAddress) = 0;

    virtual Status connect(SocketAddress &bindAddress, SocketAddress &remoteAddress) = 0;

    virtual Status prepare(uint32_t operationSize, uint32_t operationCount) = 0;

    virtual Status cleanup() = 0;

    virtual Status fillReceiveQueue() = 0;

    virtual Status sendMultipleMessages(uint32_t messageCount) = 0;

    virtual Status receiveMultipleMessages(uint32_t messageCount) = 0;

    virtual Status performMultipleRdmaOperations(RdmaMode mode, uint32_t operationCount) = 0;

    virtual Status sendSingleMessage() = 0;

    virtual Status performSingleRdmaOperation(RdmaMode mode) = 0;

    virtual Status performPingPongIterationServer() = 0;

    virtual Status performPingPongIterationClient() = 0;

private:

    bool sendSync();

    bool receiveSync();

private:

    static const char SYNC_SIGNAL[];

    log4cpp::Category &LOGGER = log4cpp::Category::getInstance("Benchmark");

    std::string resultName;
    uint32_t iterationNumber{};

    bool server{};
    uint32_t connectionRetries{};

    SocketAddress bindAddress;
    SocketAddress remoteAddress;

    nlohmann::json detectorConfig;

    int offChannelSocket{};

    std::shared_ptr<Detector::IbFabric> fabric;
    Detector::IbPerfCounter *perfCounter{};

    std::string resultPath;

    std::map<std::string, std::string> parameters;
    std::vector<std::shared_ptr<BenchmarkPhase>> phases;

};

}

#endif
