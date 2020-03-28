#include <sstream>
#include <libnet.h>
#include <cstring>
#include <iostream>
#include "SocketAddress.h"

namespace Observatory {

SocketAddress::SocketAddress(const std::string &hostname, uint16_t port) :
        hostname(hostname),
        port(port) {
    addrinfo hints{};
    addrinfo *resolvedAddress;

    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;

    int ret = getaddrinfo(hostname.c_str(), nullptr, &hints, &resolvedAddress);
    if(ret) {
        throw std::runtime_error("Address resolution failed (" + std::string(gai_strerror(ret)) + ")");
    }

    address = *reinterpret_cast<sockaddr_in*>(resolvedAddress->ai_addr);
    address.sin_port = htons(port);
}

SocketAddress::SocketAddress(uint16_t port) :
        hostname(DEFAULT_ADDRESS),
        port(port) {
    address.sin_family = AF_INET;
    address.sin_family = INADDR_ANY;
    address.sin_port = htons(port);
}

SocketAddress::SocketAddress(const sockaddr_in &address) :
        port(address.sin_port),
        address(address) {
    char buffer[32]{};

    const char *ret = inet_ntop(AF_INET, &address, buffer, sizeof(buffer));

    if(ret == nullptr) {
        throw std::runtime_error("Unable to get hostname from sockaddr_in struct (" + std::string(std::strerror(errno)) + ")");
    } else {
        hostname = ret;
    }
}

SocketAddress &SocketAddress::operator=(const SocketAddress &other) {
    if (&other == this) {
        return *this;
    }

    this->hostname = other.hostname;
    this->port = other.port;
    this->address = other.address;

    return *this;
}

const char* SocketAddress::getHostname() const {
    return hostname.c_str();
}

uint16_t SocketAddress::getPort() const {
    return port;
}

sockaddr_in SocketAddress::getAddress() const {
    return address;
}

SocketAddress::operator std::string() const {
    std::ostringstream stream;

    stream << hostname << ":" << port;

    return stream.str();
}

}