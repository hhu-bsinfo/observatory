#include "BenchmarkPhaseFactory.h"

namespace Observatory {

DummyBenchmark BenchmarkPhaseFactory::DUMMY_BENCHMARK;

std::map<std::string, BenchmarkPhase*> BenchmarkPhaseFactory::PROTOTYPE_TABLE;

BenchmarkPhase *BenchmarkPhaseFactory::newInstance(std::string &type, Benchmark &benchmark) {
    if(PROTOTYPE_TABLE.count(type)) {
        return PROTOTYPE_TABLE[type]->clone(benchmark);
    }

    throw std::runtime_error("No prototype registered for '" + type + "'!");
}

void BenchmarkPhaseFactory::registerPrototype(BenchmarkPhase *prototype) {
    PROTOTYPE_TABLE[prototype->getClassName()] = prototype;
}

void BenchmarkPhaseFactory::deregisterPrototype(std::string &type) {
    if(PROTOTYPE_TABLE.count(type)) {
        delete PROTOTYPE_TABLE.at(type);
        PROTOTYPE_TABLE.erase(type);
    }
}

}