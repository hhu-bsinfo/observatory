#include "Benchmark.h"

namespace Socket {

const char* Benchmark::getClassName() const {
    return "Socket::Benchmark";
}

Observatory::Status Benchmark::initialize() {
    return Observatory::Status::OK;
}

Observatory::Status Benchmark::isServer() {
    return Observatory::Status::OK;
}

Observatory::Status Benchmark::serve(Observatory::SocketAddress &bindAddress) {
    return Observatory::Status::OK;
}

Observatory::Status Benchmark::connect(Observatory::SocketAddress &bindAddress, Observatory::SocketAddress &remoteAddress) {
    return Observatory::Status::OK;
}

Observatory::Status Benchmark::prepare(uint32_t operationSize) {
    return Observatory::Status::OK;
}

Observatory::Status Benchmark::cleanup() {
    return Observatory::Status::OK;
}

Observatory::Status Benchmark::fillReceiveQueue() {
    return Observatory::Status::OK;
}

Observatory::Status Benchmark::sendMultipleMessages(uint32_t messageCount) {
    return Observatory::Status::OK;
}

Observatory::Status Benchmark::receiveMultipleMessages(uint32_t messageCount) {
    return Observatory::Status::OK;
}

Observatory::Status
Benchmark::performMultipleRdmaOperations(Benchmark::RdmaMode mode, uint32_t operationCount) {
    return Observatory::Status::OK;
}

Observatory::Status Benchmark::sendSingleMessage() {
    return Observatory::Status::OK;
}

Observatory::Status Benchmark::performSingleRdmaOperation(Benchmark::RdmaMode mode) {
    return Observatory::Status::OK;
}

Observatory::Status Benchmark::performPingPongIterationServer() {
    return Observatory::Status::OK;
}

Observatory::Status Benchmark::performPingPongIterationClient() {
    return Observatory::Status::OK;
}

}
