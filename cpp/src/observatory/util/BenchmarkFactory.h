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

#define BENCHMARK_IMPLEMENT_CLONE(TYPE) Observatory::Benchmark* clone() const override { return new TYPE(*this); }
#define BENCHMARK_REGISTER(TYPE) BenchmarkFactory::registerPrototype(new TYPE());

#include <string>
#include <map>
#include <observatory/Benchmark.h>

namespace Observatory {

/**
 * Implementation of the prototype pattern, based on
 * http://www.cs.sjsu.edu/faculty/pearce/modules/lectures/oop/types/reflection/prototype.htm
 */
class BenchmarkFactory {

public:

    /**
     * Constructor.
     */
    BenchmarkFactory() = delete;

    /**
     * Copy constructor.
     */
    BenchmarkFactory(const BenchmarkFactory &other) = delete;

    /**
     * Assignment operator.
     */
    BenchmarkFactory& operator=(const BenchmarkFactory &other) = delete;

    /**
     * Destructor.
     */
    virtual ~BenchmarkFactory() = delete;

    /**
     * Create a new instance of a given benchmark type.
     * Throws an exception, if the type is unknown.
     *
     * @param type The type
     *
     * @return A pointer to newly created instance
     */
    static Benchmark *newInstance(std::string &type);

    /**
     * Add a benchmark type.
     * Instances of this type can then be created by calling 'BenchmarkFactory::newInstance(type)'.
     *
     * @param type The type
     * @param prototype Instance, that will be used as a prototype for further instances
     */
    static void registerPrototype(Benchmark *prototype);

    /**
     * Remove a benchmark type.
     *
     * @param type The type
     */
    static void deregisterPrototype(std::string &type);

private:

    /**
     * Contains prototypes for all available implementations.
     */
    static std::map<std::string, Benchmark*> PROTOTYPE_TABLE;

};

}

#endif
