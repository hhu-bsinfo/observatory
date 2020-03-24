#include <algorithm>
#include "BenchmarkFactory.h"

namespace Observatory {

std::map<std::string, Benchmark*> BenchmarkFactory::PROTOTYPE_TABLE;

Benchmark *BenchmarkFactory::newInstance(std::string &type) {
    if(PROTOTYPE_TABLE.count(type)) {
        return PROTOTYPE_TABLE[type]->clone();
    }

    throw std::runtime_error("No prototype registered for '" + type + "'!");
}

void BenchmarkFactory::registerPrototype(Benchmark *prototype) {
    PROTOTYPE_TABLE[prototype->getClassName()] = prototype;
}

void BenchmarkFactory::deregisterPrototype(std::string &type) {
    if(PROTOTYPE_TABLE.count(type)) {
        delete PROTOTYPE_TABLE.at(type);
        PROTOTYPE_TABLE.erase(type);
    }
}

}