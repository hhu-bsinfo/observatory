#include "Benchmark.h"

namespace Socket {

std::string Benchmark::getClassName() const {
    return "Socket::Benchmark";
}

Observatory::Result::Status Benchmark::initialize() {
    return Observatory::Result::Status::NOT_IMPLEMENTED;
}

Observatory::Result::Status Benchmark::isServer(std::string &bindAddress) {
    return Observatory::Result::Status::NOT_IMPLEMENTED;
}

Observatory::Result::Status Benchmark::connect(std::string &bindAddress, std::string &remoteAddress) {
    return Observatory::Result::Status::NOT_IMPLEMENTED;
}

Observatory::Result::Status Benchmark::prepare(uint32_t operationSize) {
    return Observatory::Result::Status::NOT_IMPLEMENTED;
}

Observatory::Result::Status Benchmark::cleanup() {
    return Observatory::Result::Status::NOT_IMPLEMENTED;
}

Observatory::Result::Status Benchmark::fillReceiveQueue() {
    return Observatory::Result::Status::NOT_IMPLEMENTED;
}

Observatory::Result::Status Benchmark::sendMultipleMessage(uint32_t messageCount) {
    return Observatory::Result::Status::NOT_IMPLEMENTED;
}

Observatory::Result::Status Benchmark::receiveMultipleMessage(uint32_t messageCount) {
    return Observatory::Result::Status::NOT_IMPLEMENTED;
}

Observatory::Result::Status
Benchmark::performMultipleRdmaOperations(Benchmark::RdmaMode mode, uint32_t operationCount) {
    return Observatory::Result::Status::NOT_IMPLEMENTED;
}

Observatory::Result::Status Benchmark::sendSingleMessage() {
    return Observatory::Result::Status::NOT_IMPLEMENTED;
}

Observatory::Result::Status Benchmark::performSingleRdmaOperation(Benchmark::RdmaMode mode) {
    return Observatory::Result::Status::NOT_IMPLEMENTED;
}

Observatory::Result::Status Benchmark::performPingPongIterationServer() {
    return Observatory::Result::Status::NOT_IMPLEMENTED;
}

Observatory::Result::Status Benchmark::performPingPongIterationClient() {
    return Observatory::Result::Status::NOT_IMPLEMENTED;
}

}
