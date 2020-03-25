#include <sstream>
#include <libnet.h>
#include <cstring>
#include "SocketAddress.h"

namespace Observatory {

SocketAddress::SocketAddress(const std::string &hostname, uint16_t port) :
        hostname(hostname),
        port(port) {
    address.sin_family = AF_INET;
    address.sin_port = htons(port);

    if(inet_pton(AF_INET, hostname.c_str(), &address.sin_addr) <= 0) {
        throw std::runtime_error("Invalid address '" + hostname + "'");
    }
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
    return hostname + ":" + std::to_string(port);
}

}