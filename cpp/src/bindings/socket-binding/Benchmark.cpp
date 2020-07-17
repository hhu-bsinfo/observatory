#include "Benchmark.h"

namespace Socket {

const char* Benchmark::getClassName() const {
    return "Socket::Benchmark";
}

Observatory::Status Benchmark::initialize() {
    return Observatory::Status::OK;
}

Observatory::Status Benchmark::serve(Observatory::SocketAddress &bindAddress) {
    sockaddr_in bindSocketAddress = bindAddress.getAddress();
    sockaddr_in remoteSocketAddress{};
    uint32_t addressLength = sizeof(remoteSocketAddress);

    LOGGER.info("Listening on address %s", static_cast<std::string>(bindAddress).c_str());

    int serverSocket;
    if((serverSocket = ::socket(AF_INET, SOCK_STREAM, 0)) <= 0) {
        LOGGER.error("Creating server socket failed (%s)", std::strerror(errno));
        return Observatory::Status::NETWORK_ERROR;
    }

    int optval = 1;
    if(::setsockopt(serverSocket, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof(int))){
        LOGGER.error("Setting socket options failed (%s)", std::strerror(errno));
        return Observatory::Status::NETWORK_ERROR;
    }

    if(::bind(serverSocket, reinterpret_cast<sockaddr*>(&bindSocketAddress), sizeof(bindSocketAddress))) {
        LOGGER.error("Binding socket to %s failed (%s)", static_cast<std::string>(bindAddress).c_str(), std::strerror(errno));
        return Observatory::Status::NETWORK_ERROR;
    }

    if(::listen(serverSocket, 1)) {
        LOGGER.error("Listening for client failed (%s)", std::strerror(errno));
        return Observatory::Status::NETWORK_ERROR;
    }

    if(!(socket = ::accept(serverSocket, reinterpret_cast<sockaddr*>(&remoteSocketAddress), &addressLength))) {
        LOGGER.error("Accepting incoming client request failed (%s)", std::strerror(errno));
        return Observatory::Status::NETWORK_ERROR;
    }

    ::close(serverSocket);

    Observatory::SocketAddress remoteAddress(remoteSocketAddress);
    LOGGER.info("Successfully connected to %s", static_cast<std::string>(remoteAddress).c_str());

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::connect(Observatory::SocketAddress &bindAddress, Observatory::SocketAddress &remoteAddress) {
    sockaddr_in bindSocketAddress = bindAddress.getAddress();
    sockaddr_in remoteSocketAddress = remoteAddress.getAddress();

    LOGGER.info("Connecting to server %s", static_cast<std::string>(remoteAddress).c_str());

    if ((socket = ::socket(AF_INET, SOCK_STREAM, 0)) <= 0) {
        LOGGER.error("Creating socket failed (%s)", std::strerror(errno));
        return Observatory::Status::NETWORK_ERROR;
    }

    if(::bind(socket, reinterpret_cast<sockaddr*>(&bindSocketAddress), sizeof(bindSocketAddress))) {
        LOGGER.error("Binding socket to %s failed (%s)", static_cast<std::string>(bindAddress).c_str(), std::strerror(errno));
        return Observatory::Status::NETWORK_ERROR;
    }

    if (::connect(socket, reinterpret_cast<sockaddr*>(&remoteSocketAddress), sizeof(remoteSocketAddress))) {
        LOGGER.error("Connecting to server %s failed (%s)", static_cast<std::string>(remoteAddress).c_str(), std::strerror(errno));
        return Observatory::Status::NETWORK_ERROR;
    }

    LOGGER.info("Successfully connected to %s", static_cast<std::string>(remoteAddress).c_str());

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::prepare(uint32_t operationSize, uint32_t operationCount) {
    bufferSize = operationSize;
    buffer = new uint8_t[bufferSize];

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::cleanup() {
    LOGGER.info("Closing socket");

    if(::shutdown(socket, SHUT_RDWR)) {
        LOGGER.warn("Shutting down socket failed (%s)", std::strerror(errno));
    }

    if(::close(socket)) {
        LOGGER.error("Closing socket failed (%s)", std::strerror(errno));
        return Observatory::Status::NETWORK_ERROR;
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::fillReceiveQueue() {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::sendMultipleMessages(uint32_t messageCount) {
    for(uint32_t i = 0; i < messageCount; i++) {
        uint32_t totalSent = 0;

        while(totalSent < bufferSize) {
            uint32_t sent;
            if((sent = ::send(socket, buffer + totalSent, bufferSize - totalSent, 0)) < 0) {
                LOGGER.error("Sending messages failed (%s)", strerror(errno));
                return Observatory::Status::NETWORK_ERROR;
            }

            totalSent += sent;
        }
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::receiveMultipleMessages(uint32_t messageCount) {
    for(uint32_t i = 0; i < messageCount; i++) {
        uint32_t totalReceived = 0;

        while(totalReceived < bufferSize) {
            uint32_t received;
            if((received = ::recv(socket, buffer + totalReceived, bufferSize - totalReceived, 0)) < 0) {
                LOGGER.error("Receiving messages failed (%s)", strerror(errno));
                return Observatory::Status::NETWORK_ERROR;
            }

            totalReceived += received;
        }
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::performMultipleRdmaOperations(Benchmark::RdmaMode mode, uint32_t operationCount) {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::sendSingleMessage() {
    uint32_t totalSent = 0;

    while(totalSent < bufferSize) {
        uint32_t sent;
        if((sent = ::send(socket, buffer + totalSent, bufferSize - totalSent, 0)) < 0) {
            LOGGER.error("Sending single message failed (%s)", strerror(errno));
            return Observatory::Status::NETWORK_ERROR;
        }

        totalSent += sent;
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::performSingleRdmaOperation(Benchmark::RdmaMode mode) {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::performPingPongIterationServer() {
    uint32_t totalSent = 0;
    while(totalSent < bufferSize) {
        uint32_t sent;
        if((sent = ::send(socket, buffer + totalSent, bufferSize - totalSent, 0)) < 0) {
            LOGGER.error("Sending single message failed (%s)", strerror(errno));
            return Observatory::Status::NETWORK_ERROR;
        }

        totalSent += sent;
    }

    uint32_t totalReceived = 0;
    while(totalReceived < bufferSize) {
        uint32_t received;
        if((received = ::recv(socket, buffer + totalReceived, bufferSize - totalReceived, 0)) < 0) {
            LOGGER.error("Receiving single message failed (%s)", strerror(errno));
            return Observatory::Status::NETWORK_ERROR;
        }

        totalReceived += received;
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::performPingPongIterationClient() {
    if(::recv(socket, buffer, bufferSize, 0) < 0) {
        LOGGER.error("Receiving messages failed (%s)", strerror(errno));
        return Observatory::Status::NETWORK_ERROR;
    }

    if(::send(socket, buffer, bufferSize, 0) < 0) {
        LOGGER.error("Sending messages failed (%s)", strerror(errno));
        return Observatory::Status::NETWORK_ERROR;
    }

    return Observatory::Status::OK;
}

}
