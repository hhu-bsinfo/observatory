#include <socket-binding/Benchmark.h>
#include <observatory/phase/ConnectionPhase.h>
#include <observatory/phase/PreparationPhase.h>
#include <observatory/phase/FillReceiveQueuePhase.h>
#include <observatory/phase/WarmupPhase.h>
#include <observatory/phase/OperationPhase.h>
#include <observatory/phase/CleanupPhase.h>
#include "observatory/util/Factory.h"
#include "observatory/phase/InitializationPhase.h"
#include "Observatory.h"

namespace Observatory {

Observatory::Observatory(nlohmann::json &config, std::string &resultPath, bool isServer, int connectionRetries,
                         SocketAddress &bindAddress, SocketAddress &remoteAddress) :
        config(config),
        resultPath(resultPath),
        isServer(isServer),
        connectionRetries(connectionRetries),
        bindAddress(bindAddress),
        remoteAddress(remoteAddress) {}

void Observatory::registerPrototypes() {
    BENCHMARK_REGISTER(Socket::Benchmark)
}

void Observatory::start() {
    const std::string &benchmarkClassName = config["className"];

    for(const auto &operationConfig : config["operations"]) {
        for(const auto &mode : operationConfig["modes"]) {
            std::string operationClassName = std::string(operationConfig["name"]).append("Operation");

            for(const auto &iterationConfig : operationConfig["iterations"]) {
                for(int i = 0; i < operationConfig["repetitions"]; i++) {
                    std::unique_ptr<Benchmark> benchmark = BENCHMARK_FACTORY.newInstance(benchmarkClassName);

                    if(benchmark == nullptr) {
                        return;
                    }

                    std::unique_ptr<Operation> operation;

                    for(const auto &parameter : config["parameters"]) {
                        benchmark->setParameter(parameter["key"], parameter["value"]);
                    }

                    benchmark->setServer(isServer);
                    benchmark->setConnectionRetries(connectionRetries);

                    benchmark->setDetectorConfig(config["detector"]);

                    benchmark->setBindAddress(bindAddress);
                    benchmark->setRemoteAddress(remoteAddress);

                    benchmark->setResultName(config["resultName"].empty() ? config["className"] : config["resultName"]);
                    benchmark->setResultPath(resultPath);
                    benchmark->setIterationNumber(i);

                    benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new InitializationPhase(*benchmark)));
                    benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new ConnectionPhase(*benchmark)));
                    benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new PreparationPhase(*benchmark, iterationConfig["size"])));

                    /*if(operation->needsFilledReceiveQueue()) {
                        benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new FillReceiveQueuePhase(*benchmark)));
                    }

                    benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new WarmupPhase(*benchmark, *operation, static_cast<uint32_t>(iterationConfig["warmUp"]))));

                    if(operation->needsFilledReceiveQueue()) {
                        benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new FillReceiveQueuePhase(*benchmark)));
                    }

                    benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new OperationPhase(*benchmark, *operation)));*/

                    benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new CleanupPhase(*benchmark)));

                    executeBenchmark(*benchmark);
                }
            }
        }
    }
}

void Observatory::executeBenchmark(Benchmark &benchmark) {
    Status status = benchmark.setup();
    if(status != Status::OK) {
        exit(status);
    }

    if(!benchmark.synchronize()) {
        exit(Status::SYNC_ERROR);
    }

    benchmark.executePhases();
}

}