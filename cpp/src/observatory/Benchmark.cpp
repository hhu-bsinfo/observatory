#include <detector/exception/IbPerfException.h>
#include "Benchmark.h"

namespace Observatory {

Benchmark::Benchmark(const Benchmark &other) {
    this->resultName = other.resultName;
    this->iterationNumber = other.iterationNumber;
    this->server = other.server;
    this->connectionRetries = other.connectionRetries;
    this->bindAddress = other.bindAddress;
    this->remoteAddress = other.remoteAddress;
    this->detectorConfig = other.detectorConfig;
    this->offChannelSocket = other.offChannelSocket;
    this->fabric = other.fabric;
    this->perfCounter = other.perfCounter;
    this->resultPath = other.resultPath;
    this->parameters = other.parameters;
    this->phases = other.phases;
}

void Benchmark::addBenchmarkPhase(BenchmarkPhase &phase) {
    phases.push_back(&phase);
}

void Benchmark::setParameter(std::string &key, std::string &value) {
    parameters[key] = value;
}

const std::string& Benchmark::getParameter(std::string &key, std::string &defaultValue) const {
    return parameters.at(key);
}

uint8_t Benchmark::getParameter(std::string &key, uint8_t defaultValue) const {
    return std::stoi(parameters.at(key));
}

uint16_t Benchmark::getParameter(std::string &key, uint16_t defaultValue) const {
    return std::stoi(parameters.at(key));
}

uint32_t Benchmark::getParameter(std::string &key, uint32_t defaultValue) const {
    return std::stoi(parameters.at(key));
}

uint64_t Benchmark::getParameter(std::string &key, uint64_t defaultValue) const {
    return std::stoi(parameters.at(key));
}

int Benchmark::getOffChannelSocket() const {
    return offChannelSocket;
}

std::string Benchmark::getResultName() const {
    return resultName;
}

bool Benchmark::isServer() const {
    return server;
}

int Benchmark::getConnectionRetries() const {
    return connectionRetries;
}

SocketAddress Benchmark::getBindAddress() const {
    return bindAddress;
}

SocketAddress Benchmark::getRemoteAddress() const {
    return remoteAddress;
}

std::string Benchmark::getResultPath() const {
    return resultPath;
}

int Benchmark::getIterationNumber() const {
    return iterationNumber;
}

bool Benchmark::measureOverhead() const {
    return detectorConfig["enabled"];
}

Detector::IbPerfCounter &Benchmark::getPerfCounter() {
    return *perfCounter;
}

void Benchmark::setResultName(std::string &resultName) {
    this->resultName = resultName;
}

void Benchmark::setConnectionRetires(uint32_t connectionRetries) {
    this->connectionRetries = connectionRetries;
}

void Benchmark::setBindAddress(SocketAddress &bindAddress) {
    this->bindAddress = bindAddress;
}

void Benchmark::setRemoteAddress(SocketAddress &remoteAddress) {
    this->remoteAddress = remoteAddress;
}

void Benchmark::setResultPath(std::string &resultPath) {
    this->resultPath = resultPath;
}

void Benchmark::setIterationNumber(uint32_t iterationNumber) {
    this->iterationNumber = iterationNumber;
}

void Benchmark::setDetectorConfig(const nlohmann::json &detectorConfig) {
    this->detectorConfig = detectorConfig;
}

Result::Status Benchmark::setup() {
    if(detectorConfig["enabled"]) {
        LOGGER.info("Initializing Detector");

        try {
            fabric = new Detector::IbFabric(false, detectorConfig["mode"] == "compat");
        } catch (Detector::IbPerfException &e) {
            LOGGER.error("Unable to initialize Detector!\n\033[0m %s\n", e.what());
            return Result::Status::UNKNOWN_ERROR;
        }

        if(fabric->GetNumNodes() == 0) {
            LOGGER.error("Fabric scanned by Detector: 0 devices found");
            return Result::Status::UNKNOWN_ERROR;
        } else {
            LOGGER.info("Fabric scanned by Detector: %u %s found",
                    fabric->GetNumNodes(), fabric->GetNumNodes() == 1 ? "device was" : "devices were");
        }

        if(detectorConfig["deviceNumber"] >= fabric->GetNumNodes()) {
            LOGGER.error("InfiniBand device with number %u does not exist",
                    static_cast<uint32_t>(detectorConfig["deviceNumber"]));
            return Result::Status::UNKNOWN_ERROR;
        }

        LOGGER.info("Measuring overhead on %s",
                fabric->GetNodes()[detectorConfig["deviceNumber"]]->GetDescription().c_str());

        perfCounter = fabric->GetNodes()[detectorConfig["deviceNumber"]];
    }

    return Result::Status::NOT_IMPLEMENTED;
}

bool Benchmark::synchronize() {
    return false;
}

void Benchmark::executePhases() {
}

bool Benchmark::sendSync() {
    return false;
}

bool Benchmark::receiveSync() {
    return false;
}

}
