#include <detector/exception/IbPerfException.h>
#include <memory>
#include <thread>
#include "Benchmark.h"

namespace Observatory {

void Benchmark::addBenchmarkPhase(const std::shared_ptr<BenchmarkPhase>& phase) {
    phases.push_back(phase);
}

void Benchmark::setParameter(const char *key, const char *value) {
    parameters[key] = value;
}

std::string Benchmark::getParameter(const char *key, const char *defaultValue) const {
    return parameters.at(key);
}

uint8_t Benchmark::getParameter(const char *key, uint8_t defaultValue) const {
    return std::stoi(parameters.at(key));
}

uint16_t Benchmark::getParameter(const char *key, uint16_t defaultValue) const {
    return std::stoi(parameters.at(key));
}

uint32_t Benchmark::getParameter(const char *key, uint32_t defaultValue) const {
    return std::stoi(parameters.at(key));
}

uint64_t Benchmark::getParameter(const char *key, uint64_t defaultValue) const {
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

uint32_t Benchmark::getConnectionRetries() const {
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

void Benchmark::setResultName(const char *resultName) {
    this->resultName = resultName;
}

void Benchmark::setServer(bool isServer) {
    this->server = isServer;
}

void Benchmark::setConnectionRetries(uint32_t connectionRetries) {
    this->connectionRetries = connectionRetries;
}

void Benchmark::setBindAddress(const SocketAddress &bindAddress) {
    this->bindAddress = bindAddress;
}

void Benchmark::setRemoteAddress(const SocketAddress &remoteAddress) {
    this->remoteAddress = remoteAddress;
}

void Benchmark::setResultPath(const char *resultPath) {
    this->resultPath = resultPath;
}

void Benchmark::setIterationNumber(uint32_t iterationNumber) {
    this->iterationNumber = iterationNumber;
}

void Benchmark::setDetectorConfig(const nlohmann::json &detectorConfig) {
    this->detectorConfig = detectorConfig;
}

Status Benchmark::setup() {
    if(detectorConfig["enabled"]) {
        LOGGER.info("Initializing Detector");

        try {
            fabric = std::make_shared<Detector::IbFabric>(false, detectorConfig["mode"] == "compat");
        } catch (Detector::IbPerfException &e) {
            LOGGER.error("Unable to initialize Detector!\n\033[0m %s\n", e.what());
            return Status::UNKNOWN_ERROR;
        }

        if(fabric->GetNumNodes() == 0) {
            LOGGER.error("Fabric scanned by Detector: 0 devices found");
            return Status::UNKNOWN_ERROR;
        } else {
            LOGGER.info("Fabric scanned by Detector: %u %s found",
                    fabric->GetNumNodes(), fabric->GetNumNodes() == 1 ? "device was" : "devices were");
        }

        if(detectorConfig["deviceNumber"] >= fabric->GetNumNodes()) {
            LOGGER.error("InfiniBand device with number %u does not exist",
                    static_cast<uint32_t>(detectorConfig["deviceNumber"]));
            return Status::UNKNOWN_ERROR;
        }

        LOGGER.info("Measuring overhead on %s",
                fabric->GetNodes()[detectorConfig["deviceNumber"]]->GetDescription().c_str());

        perfCounter = fabric->GetNodes()[detectorConfig["deviceNumber"]];
    }

    LOGGER.info("Setting up connection for off channel communication");

    sockaddr_in bindSocketAddress = bindAddress.getAddress();
    sockaddr_in remoteSocketAddress = remoteAddress.getAddress();
    uint32_t addressLength = sizeof(remoteSocketAddress);

    if(server) {
        LOGGER.info("Listening on address %s", static_cast<std::string>(bindAddress).c_str());

        int serverSocket;

        if((serverSocket = ::socket(AF_INET, SOCK_STREAM, 0)) <= 0) {
            LOGGER.error("Creating server socket failed (%s)", std::strerror(errno));
            return Status::NETWORK_ERROR;
        }

        int optval = 1;
        if(::setsockopt(serverSocket, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof(int))){
            LOGGER.error("Setting socket options failed (%s)", std::strerror(errno));
            return Status::NETWORK_ERROR;
        }

        if(::bind(serverSocket, reinterpret_cast<sockaddr*>(&bindSocketAddress), sizeof(bindSocketAddress))) {
            LOGGER.error("Binding socket to %s failed (%s)", static_cast<std::string>(bindAddress).c_str(), std::strerror(errno));
            return Status::NETWORK_ERROR;
        }

        if(::listen(serverSocket, 1)) {
            LOGGER.error("Listening for client failed (%s)", std::strerror(errno));
            return Status::NETWORK_ERROR;
        }

        if(!(offChannelSocket = ::accept(serverSocket, reinterpret_cast<sockaddr *>(&remoteSocketAddress), &addressLength))) {
            LOGGER.error("Accepting incoming client request failed (%s)", std::strerror(errno));
            return Status::NETWORK_ERROR;
        }

        remoteAddress = SocketAddress(remoteSocketAddress);

        ::close(serverSocket);
    } else {
        LOGGER.info("Connecting to server %s", static_cast<std::string>(remoteAddress).c_str());

        bool connected = false;

        for(uint32_t i = 0; i < connectionRetries && !connected; i++) {
            std::this_thread::sleep_for(std::chrono::seconds(1));
            connected = true;

            if ((offChannelSocket = ::socket(AF_INET, SOCK_STREAM, 0)) <= 0) {
                LOGGER.warn("Creating socket failed (%s)", std::strerror(errno));
                connected = false;
            }

            if (::connect(offChannelSocket, reinterpret_cast<sockaddr *>(&remoteSocketAddress), sizeof(remoteSocketAddress))) {
                LOGGER.warn("Connecting to server %s failed (%s)", static_cast<std::string>(remoteAddress).c_str(), std::strerror(errno));
                connected = false;
            }
        }

        if(offChannelSocket <= 0) {
            LOGGER.error("Setting up off channel communication failed (Retry amount exceeded)");
        }
    }

    LOGGER.info("Successfully connected to %s", static_cast<std::string>(remoteAddress).c_str());

    return Status::OK;
}

bool Benchmark::sendSync() {
    if(::send(offChannelSocket, SYNC_SIGNAL, sizeof(SYNC_SIGNAL), 0) < 0) {
        LOGGER.error("Unable to send synchronization signal (%s)", std::strerror(errno));
        return false;
    }

    return true;
}

bool Benchmark::receiveSync() {
    char buffer[sizeof(SYNC_SIGNAL)]{};

    if(::recv(offChannelSocket, buffer, sizeof(buffer), 0) < 0) {
        LOGGER.error("Unable to receive synchronization signal (%s)", std::strerror(errno));
        return false;
    }

    if(std::string(buffer) != SYNC_SIGNAL) {
        LOGGER.error("Received invalid signal (Got '%s', Expected '%s')", buffer, SYNC_SIGNAL);
        return false;
    }

    return true;
}

bool Benchmark::synchronize() {
    LOGGER.info("Synchronizing with remote benchmark");

    if(!sendSync() || !receiveSync()) {
        LOGGER.error("Unable to synchronize with remote benchmark");
        return false;
    }

    LOGGER.info("Synchronized with remote benchmark");

    return true;
}

void Benchmark::executePhases() {
    for(const auto &phase : phases) {
        std::string phaseName = phase->getName();

        LOGGER.info("Running %s", phaseName.c_str());

        Status status = phase->execute();

        if(status == Status::NOT_IMPLEMENTED) {
            LOGGER.warn("%s returned [%s] and is being skipped", phaseName.c_str(), getStatusString(status));
            continue;
        }

        if(status != Status::OK) {
            LOGGER.error("%s failed with status [%s]", phaseName.c_str(), getStatusString(status));
            exit(status);
        }

        LOGGER.info("%s finished with status [%s]", phaseName.c_str(), getStatusString(status));
    }

    if(::close(offChannelSocket)) {
        LOGGER.error("Closing off channel communication failed (%s)", std::strerror(errno));
    }
}

}
