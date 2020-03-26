#include "ConnectionPhase.h"
#include <thread>
#include <utility>
#include <observatory/Benchmark.h>
#include <observatory/util/SocketAddress.h>

namespace Observatory {

ConnectionPhase::ConnectionPhase(Benchmark &benchmark) :
        BenchmarkPhase(benchmark) {}

const char* ConnectionPhase::getName() {
    return "ConnectionPhase";
}

Status ConnectionPhase::execute() {
    Benchmark &benchmark = getBenchmark();
    SocketAddress bindAddress = benchmark.getBindAddress();
    SocketAddress remoteAddress = benchmark.getRemoteAddress();

    if(benchmark.isServer()) {
        return benchmark.serve(bindAddress);
    } else {
        Status status = Status::UNKNOWN_ERROR;

        for (uint32_t i = 0; i < getBenchmark().getConnectionRetries(); i++) {
            std::this_thread::sleep_for(std::chrono::seconds(1));

            status = benchmark.connect(bindAddress, remoteAddress);

            if (status == Status::OK || status == Status::NOT_IMPLEMENTED) {
                return status;
            }
        }

        return status;
    }
}

}