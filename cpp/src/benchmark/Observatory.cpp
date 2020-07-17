/*#include <socket-binding/Benchmark.h>
#include <verbs-binding/Benchmark.h>*/

#include <benchmark/phase/ConnectionPhase.h>
#include <benchmark/phase/PreparationPhase.h>
#include <benchmark/phase/CleanupPhase.h>
#include <benchmark/operation/MessagingThroughputOperation.h>
#include <benchmark/operation/RdmaWriteThroughputOperation.h>
#include <benchmark/operation/RdmaReadThroughputOperation.h>
#include <benchmark/operation/MessagingLatencyOperation.h>
#include <benchmark/operation/MessagingPingPongOperation.h>
#include <benchmark/operation/RdmaWriteLatencyOperation.h>
#include <benchmark/operation/RdmaReadLatencyOperation.h>
#include <benchmark/util/BenchmarkFactory.h>
#include <benchmark/util/OperationFactory.h>
#include <benchmark/util/Util.h>
#include <benchmark/phase/InitializationPhase.h>
#include <benchmark/operation/BidirectionalThroughputOperation.h>
#include <benchmark/phase/FillReceiveQueuePhase.h>
#include <benchmark/phase/WarmupPhase.h>
#include <benchmark/phase/OperationPhase.h>
#include <benchmark/BuildConfig.h>
#include "Observatory.h"

#define STRINGIFY(a) #a
#define XSTRINGIFY(a) STRINGIFY(a)

#ifdef OBSERVATORY_BENCHMARK_HEADER_FILE
    #include XSTRINGIFY(OBSERVATORY_BENCHMARK_HEADER_FILE)
#else
    #error OBSERVATORY_BENCHMARK_HEADER_FILE is not set!
#endif

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
    #ifdef OBSERVATORY_BENCHMARK_CLASS_NAME
        BENCHMARK_REGISTER(OBSERVATORY_BENCHMARK_CLASS_NAME)
    #else
        #error OBSERVATORY_BENCHMARK_CLASS_NAME is not set!
    #endif

    OPERATION_REGISTER(::Observatory::MessagingThroughputOperation)
    OPERATION_REGISTER(::Observatory::RdmaWriteThroughputOperation)
    OPERATION_REGISTER(::Observatory::RdmaReadThroughputOperation)
    OPERATION_REGISTER(::Observatory::MessagingLatencyOperation)
    OPERATION_REGISTER(::Observatory::MessagingPingPongOperation)
    OPERATION_REGISTER(::Observatory::RdmaWriteLatencyOperation)
    OPERATION_REGISTER(::Observatory::RdmaReadLatencyOperation)
}

void Observatory::start() {
    const std::string &benchmarkClassName = BuildConfig::BENCHMARK_CLASS_NAME;

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
                    benchmark->addBenchmarkPhase(std::shared_ptr<BenchmarkPhase>(new PreparationPhase(*benchmark, iterationConfig["size"],
                                                                                                      0)));

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