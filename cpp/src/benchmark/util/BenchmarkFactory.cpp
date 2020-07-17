#include "BenchmarkFactory.h"

namespace Observatory {

std::map<std::string, std::unique_ptr<Benchmark>> BenchmarkFactory::prototypeTable;

std::shared_ptr<Benchmark> BenchmarkFactory::newInstance(const std::string &type) {
    if(prototypeTable.count(type)) {
        return std::shared_ptr<Benchmark>(prototypeTable[type]->instantiate());
    }

    throw std::runtime_error("No prototype registered for '" + type + "'!");
}

void BenchmarkFactory::registerPrototype(Benchmark *prototype) {
    prototypeTable[prototype->getClassName()] = std::unique_ptr<Benchmark>(prototype);
}

void BenchmarkFactory::deregisterPrototype(const std::string &type) {
    if(prototypeTable.count(type)) {
        prototypeTable.erase(type);
    }
}

}