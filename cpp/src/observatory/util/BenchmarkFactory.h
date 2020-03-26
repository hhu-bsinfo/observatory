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

#ifndef OBSERVATORY_FACTORY_H
#define OBSERVATORY_FACTORY_H

#define IMPLEMENT_CLONE(TYPE) TYPE* clone() const override { return new TYPE(*this); }
#define BENCHMARK_REGISTER(TYPE) BENCHMARK_FACTORY.registerPrototype(new TYPE());
#define OPERATION_REGISTER(TYPE) OPERATION_FACTORY.registerPrototype(new TYPE(nullptr, ::Observatory::Benchmark::Mode::SEND, 0, 0));

#include <string>
#include <map>
#include <observatory/Benchmark.h>
#include <observatory/operation/Operation.h>

namespace Observatory {

/**
 * Implementation of the prototype pattern, based on
 * http://www.cs.sjsu.edu/faculty/pearce/modules/lectures/oop/types/reflection/prototype.htm
 */
template<class T>
class Factory {

public:

    /**
     * Constructor.
     */
    Factory() = default;

    /**
     * Copy constructor.
     */
    Factory(const Factory &other) = delete;

    /**
     * Assignment operator.
     */
    Factory& operator=(const Factory &other) = delete;

    /**
     * Destructor.
     */
    ~Factory() = default;

    /**
     * Create a new instance of a given prototype.
     * Throws an exception, if the type is unknown.
     *
     * @param type The type
     *
     * @return A pointer to the newly created instance
     */
    std::unique_ptr<T> newInstance(const std::string &type) {
        if(prototypeTable.count(type)) {
            return std::unique_ptr<T>(prototypeTable[type]->clone());
        }

        throw std::runtime_error("No prototype registered for '" + type + "'!");
    }

    /**
     * Add a prototype.
     * Instances of this type can then be created by calling 'BenchmarkFactory::newInstance(type)'.
     *
     * @param type The type
     * @param prototype Instance, that will be used as a prototype for further instances
     */
    void registerPrototype(T *prototype) {
        prototypeTable[prototype->getClassName()] = prototype;
    }

    /**
     * Remove a prototype.
     *
     * @param type The type
     */
    void deregisterPrototype(const std::string &type) {
        if(prototypeTable.count(type)) {
            delete prototypeTable.at(type);
            prototypeTable.erase(type);
        }
    }

private:

    /**
     * Contains prototypes for all available implementations.
     */
    std::map<std::string, T*> prototypeTable;

};

static Factory<Observatory::Benchmark> BENCHMARK_FACTORY;
static Factory<Observatory::Operation> OPERATION_FACTORY;

}

#endif
