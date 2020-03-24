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

#ifndef OBSERVATORY_BENCHMARKPHASEFACTORY_H
#define OBSERVATORY_BENCHMARKPHASEFACTORY_H

#include <map>
#include <observatory/BenchmarkPhase.h>
#include <observatory/Benchmark.h>
#include "BenchmarkFactory.h"

#define BENCHMARK_PHASE_IMPLEMENT_CLONE(TYPE) Observatory::BenchmarkPhase* clone(Benchmark &benchmark) const override { return new TYPE(benchmark); }
#define BENCHMARK_PHASE_REGISTER(TYPE) BenchmarkPhaseFactory::registerPrototype(new TYPE(BenchmarkPhaseFactory::DUMMY_BENCHMARK));

namespace Observatory {

class DummyBenchmark;

/**
 * Implementation of the prototype pattern, based on
 * http://www.cs.sjsu.edu/faculty/pearce/modules/lectures/oop/types/reflection/prototype.htm
 */
class BenchmarkPhaseFactory {

public:

    /**
     * Constructor.
     */
    BenchmarkPhaseFactory() = delete;

    /**
     * Copy constructor.
     */
    BenchmarkPhaseFactory(const BenchmarkPhaseFactory &other) = delete;

    /**
     * Assignment operator.
     */
    BenchmarkPhaseFactory &operator=(const BenchmarkPhaseFactory &other) = delete;

    /**
     * Destructor.
     */
    virtual ~BenchmarkPhaseFactory() = delete;

    /**
     * Create a new instance of a given benchmark type.
     * Throws an exception, if the type is unknown.
     *
     * @param type The type
     *
     * @return A pointer to newly created instance
     */
    static BenchmarkPhase *newInstance(std::string &type, Benchmark &benchmark);

    /**
     * Add a benchmark type.
     * Instances of this type can then be created by calling 'BenchmarkPhaseFactory::newInstance(type)'.
     *
     * @param type The type
     * @param prototype Instance, that will be used as a prototype for further instances
     */
    static void registerPrototype(BenchmarkPhase *prototype);

    /**
     * Remove a benchmark type.
     *
     * @param type The type
     */
    static void deregisterPrototype(std::string &type);

public:

    static DummyBenchmark DUMMY_BENCHMARK;

private:

    /**
     * Contains prototypes for all available implementations.
     */
    static std::map<std::string, BenchmarkPhase*> PROTOTYPE_TABLE;

};

class DummyBenchmark : public Benchmark {

public:
    
    DummyBenchmark() = default;

    DummyBenchmark(const DummyBenchmark &other) = default;

    DummyBenchmark& operator=(const DummyBenchmark &other) = delete;

    ~DummyBenchmark() override = default;

    BENCHMARK_IMPLEMENT_CLONE(Observatory::DummyBenchmark);

    std::string getClassName() const override  {
        return "Observatory::DummyBenchmark";
    }

    Observatory::Result::Status initialize() override {
        return Result::Status::NOT_IMPLEMENTED;
    }

    Observatory::Result::Status isServer(std::string &bindAddress) override {
        return Result::Status::NOT_IMPLEMENTED;
    }

    Observatory::Result::Status connect(std::string &bindAddress, std::string &remoteAddress) override {
        return Result::Status::NOT_IMPLEMENTED;
    }

    Observatory::Result::Status prepare(uint32_t operationSize) override {
        return Result::Status::NOT_IMPLEMENTED;
    }

    Observatory::Result::Status cleanup() override {
        return Result::Status::NOT_IMPLEMENTED;
    }

    Observatory::Result::Status fillReceiveQueue() override {
        return Result::Status::NOT_IMPLEMENTED;
    }

    Observatory::Result::Status sendMultipleMessage(uint32_t messageCount) override {
        return Result::Status::NOT_IMPLEMENTED;
    }

    Observatory::Result::Status receiveMultipleMessage(uint32_t messageCount) override {
        return Result::Status::NOT_IMPLEMENTED;
    }

    Observatory::Result::Status performMultipleRdmaOperations(RdmaMode mode, uint32_t operationCount) override {
        return Result::Status::NOT_IMPLEMENTED;
    }

    Observatory::Result::Status sendSingleMessage() override {
        return Result::Status::NOT_IMPLEMENTED;
    }

    Observatory::Result::Status performSingleRdmaOperation(RdmaMode mode) override {
        return Result::Status::NOT_IMPLEMENTED;
    }

    Observatory::Result::Status performPingPongIterationServer() override {
        return Result::Status::NOT_IMPLEMENTED;
    }

    Observatory::Result::Status performPingPongIterationClient() override {
        return Result::Status::NOT_IMPLEMENTED;
    }

};

}

#endif
