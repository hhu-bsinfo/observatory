#include "Benchmark.h"

namespace Socket {

const char* Benchmark::getClassName() const {
    return "Socket::Benchmark";
}

Observatory::Status Benchmark::initialize() {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::isServer() {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::serve(Observatory::SocketAddress &bindAddress) {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::connect(Observatory::SocketAddress &bindAddress, Observatory::SocketAddress &remoteAddress) {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::prepare(uint32_t operationSize) {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::cleanup() {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::fillReceiveQueue() {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::sendMultipleMessage(uint32_t messageCount) {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::receiveMultipleMessage(uint32_t messageCount) {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status
Benchmark::performMultipleRdmaOperations(Benchmark::RdmaMode mode, uint32_t operationCount) {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::sendSingleMessage() {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::performSingleRdmaOperation(Benchmark::RdmaMode mode) {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::performPingPongIterationServer() {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::performPingPongIterationClient() {
    return Observatory::Status::NOT_IMPLEMENTED;
}

}
