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

#ifndef OBSERVATORY_BENCHMARKFACTORY_H
#define OBSERVATORY_BENCHMARKFACTORY_H

#include <string>
#include <map>
#include <observatory/Benchmark.h>
#include <observatory/operation/Operation.h>

#define BENCHMARK_IMPLEMENT_INSTANTIATE(TYPE) TYPE* instantiate() const override { return new TYPE(); }
#define BENCHMARK_REGISTER(TYPE) ::Observatory::BenchmarkFactory::registerPrototype(new TYPE());

namespace Observatory {

class BenchmarkFactory {

public:

    BenchmarkFactory() = delete;

    BenchmarkFactory(const BenchmarkFactory &other) = delete;

    BenchmarkFactory& operator=(const BenchmarkFactory &other) = delete;

    ~BenchmarkFactory() = delete;

    static std::shared_ptr<Benchmark> newInstance(const std::string &type);

    static void registerPrototype(Benchmark *prototype);

    static void deregisterPrototype(const std::string &type);

private:

    static std::map<std::string, std::unique_ptr<Benchmark>> prototypeTable;

};

}

#endif
