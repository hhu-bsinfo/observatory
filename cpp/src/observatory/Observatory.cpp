#include <socket-binding/Benchmark.h>
#include "util/BenchmarkFactory.h"
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

    BENCHMARK_PHASE_REGISTER(InitializationPhase)
}

void Observatory::start() {
    std::string benchmarkClassName = config["className"];

    for(const auto &operationConfig : config["operations"]) {
        for(const auto &mode : operationConfig["modes"]) {
            std::string operationClassName = std::string(operationConfig["name"]).append("Operation");

            for(const auto &iterationConfig : operationConfig["iterations"]) {
                for(int i = 0; i < operationConfig["repetitions"]; i++) {
                    auto *benchmark = BenchmarkFactory::newInstance(benchmarkClassName);
                    benchmark->setDetectorConfig(config["detector"]);

                    executeBenchmark(*benchmark);
                }
            }
        }
    }
}

void Observatory::executeBenchmark(Benchmark &benchmark) {
    Result::Status status = benchmark.setup();
    if(status != Result::Status::OK) {
        exit(Result::Status::OK);
    }

    if(benchmark.synchronize()) {
        exit(Result::Status::OK);
    }

    benchmark.executePhases();
}

}