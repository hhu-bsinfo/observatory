#include <socket-binding/Benchmark.h>
#include <verbs-binding/Benchmark.h>
#include <observatory/phase/ConnectionPhase.h>
#include <observatory/phase/PreparationPhase.h>
#include <observatory/phase/CleanupPhase.h>
#include <observatory/operation/MessagingThroughputOperation.h>
#include <observatory/operation/RdmaWriteThroughputOperation.h>
#include <observatory/operation/RdmaReadThroughputOperation.h>
#include <observatory/operation/MessagingLatencyOperation.h>
#include <observatory/operation/MessagingPingPongOperation.h>
#include <observatory/operation/RdmaWriteLatencyOperation.h>
#include <observatory/operation/RdmaReadLatencyOperation.h>
#include <observatory/util/BenchmarkFactory.h>
#include <observatory/util/OperationFactory.h>
#include <observatory/util/Util.h>
#include <observatory/phase/InitializationPhase.h>
#include <observatory/operation/BidirectionalThroughputOperation.h>
#include <observatory/phase/FillReceiveQueuePhase.h>
#include <observatory/phase/WarmupPhase.h>
#include <observatory/phase/OperationPhase.h>
#include "Observatory.h"

namespace Observatory {

Observatory::Observatory(nlohmann::json &config, std::string &resultPath, bool isServer, int connectionRetries,
                         SocketAddress &bindAddress, SocketAddress &remoteAddress) :
        config(config),
        resultPath(resultPath),
        isServer(isServer),
        connectionRetries(connectionRetries),
        bindAddress(isServer ? bindAddress : SocketAddress(bindAddress.getHostname(), 0)),
        remoteAddress(remoteAddress) {}

void Observatory::registerPrototypes() {
    BENCHMARK_REGISTER(Socket::Benchmark)
    BENCHMARK_REGISTER(Verbs::Benchmark)

    OPERATION_REGISTER(::Observatory::MessagingThroughputOperation)
    OPERATION_REGISTER(::Observatory::RdmaWriteThroughputOperation)
    OPERATION_REGISTER(::Observatory::RdmaReadThroughputOperation)
    OPERATION_REGISTER(::Observatory::MessagingLatencyOperation)
    OPERATION_REGISTER(::Observatory::MessagingPingPongOperation)
    OPERATION_REGISTER(::Observatory::RdmaWriteLatencyOperation)
    OPERATION_REGISTER(::Observatory::RdmaReadLatencyOperation)
}

void Observatory::start() {
    const std::string &benchmarkClassName = config["className"];

    for(const auto &operationConfig : config["operations"]) {
        for(const auto &mode : operationConfig["modes"]) {
            std::string operationClassName = std::string(operationConfig["name"]).append("Operation");

            for(const auto &iterationConfig : operationConfig["iterations"]) {
                for(uint32_t i = 0; i < operationConfig["repetitions"]; i++) {
                    std::shared_ptr<Benchmark> benchmark;

                    try {
                        benchmark = BenchmarkFactory::newInstance(benchmarkClassName);
                    } catch(std::runtime_error &e) {
                        LOGGER.error("Unable to instantiate benchmark\n\033[0m %s", benchmarkClassName.c_str(), e.what());
                        return;
                    }

                    std::shared_ptr<Operation> operation;

                    try {
                        if(mode == "unidirectional") {
                            operation = OperationFactory::newInstance(operationClassName, benchmark.get(),
                                    isServer ? Benchmark::Mode::SEND : Benchmark::Mode::RECEIVE,iterationConfig["count"], iterationConfig["size"]);
                        } else if(mode == "bidirectional") {
                            std::shared_ptr<Operation> sendOperation = OperationFactory::newInstance(operationClassName, benchmark.get(),
                                    Benchmark::Mode::SEND,iterationConfig["count"],iterationConfig["size"]);
                            std::shared_ptr<Operation> receiveOperation = OperationFactory::newInstance(operationClassName, benchmark.get(),
                                    Benchmark::Mode::RECEIVE,iterationConfig["count"],iterationConfig["size"]);

                            if(!Util::instanceof<ThroughputOperation>(&*sendOperation) || !Util::instanceof<ThroughputOperation>(&*receiveOperation)) {
                                LOGGER.error("Invalid configuration: Only throughput operations may be executed bidirectionally");
                                return;
                            }

                            operation = std::shared_ptr<Operation>(new BidirectionalThroughputOperation(std::static_pointer_cast<ThroughputOperation>(sendOperation), std::static_pointer_cast<ThroughputOperation>(receiveOperation)));
                        }
                    } catch(std::runtime_error &e) {
                        LOGGER.error("Unable to instantiate operation of type '%s'\n\033[0m %s", operationClassName.c_str(), e.what());
                        return;
                    }

                    for(const auto &parameter : config["parameters"]) {
                        benchmark->setParameter(static_cast<std::string>(parameter["key"]).c_str(), static_cast<std::string>(parameter["value"]).c_str());
                    }

                    benchmark->setServer(isServer);
                    benchmark->setConnectionRetries(connectionRetries);

                    benchmark->setDetectorConfig(config["detector"]);

                    benchmark->setBindAddress(bindAddress);
                    benchmark->setRemoteAddress(remoteAddress);

                    benchmark->setResultName(static_cast<std::string>(config["resultName"]).empty() ? static_cast<std::string>(config["className"]).c_str() : static_cast<std::string>(config["resultName"]).c_str());
                    benchmark->setResultPath(static_cast<std::string>(resultPath).c_str());
                    benchmark->setIterationNumber(i);

                    benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new InitializationPhase(*benchmark)));
                    benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new ConnectionPhase(*benchmark)));
                    benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new PreparationPhase(*benchmark, iterationConfig["size"])));

                    benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new FillReceiveQueuePhase(*benchmark, *operation)));
                    benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new WarmupPhase(*benchmark, *operation, static_cast<uint32_t>(iterationConfig["warmUp"]))));

                    benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new FillReceiveQueuePhase(*benchmark, *operation)));
                    benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new OperationPhase(*benchmark, *operation)));

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

    benchmark.executePhases();
}

}