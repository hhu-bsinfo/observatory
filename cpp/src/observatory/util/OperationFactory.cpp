#include "OperationFactory.h"

namespace Observatory {

std::map<std::string, std::unique_ptr<Operation>> OperationFactory::prototypeTable;

std::shared_ptr<Operation> OperationFactory::newInstance(const std::string &type, Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize) {
    if(prototypeTable.count(type)) {
        return std::shared_ptr<Operation>(prototypeTable[type]->instantiate(benchmark, mode, operationCount, operationSize));
    }

    throw std::runtime_error("No prototype registered for '" + type + "'!");
}

void OperationFactory::registerPrototype(Operation *prototype) {
    prototypeTable[prototype->getClassName()] = std::unique_ptr<Operation>(prototype);
}

void OperationFactory::deregisterPrototype(const std::string &type) {
    if(prototypeTable.count(type)) {
        prototypeTable.erase(type);
    }
}

}