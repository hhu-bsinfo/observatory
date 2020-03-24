#include <sstream>
#include "SocketAddress.h"

namespace Observatory {

SocketAddress::SocketAddress(std::string &address, uint16_t port) :
        address(address),
        port(port) {}

SocketAddress::SocketAddress(std::string &address) :
        address(address) {}

SocketAddress::SocketAddress(uint16_t port) :
        port(port) {}

SocketAddress::SocketAddress(const SocketAddress &other) {
    this->address = other.address;
    this->port = other.port;
}

SocketAddress &SocketAddress::operator=(const SocketAddress &other) {
    if (&other == this) {
        return *this;
    }

    this->address = other.address;
    this->port = other.port;

    return *this;
}

std::string SocketAddress::getAddress() const {
    return address;
}

uint16_t SocketAddress::getPort() const {
    return port;
}

void SocketAddress::setAddress(std::string &address) {
    this->address = address;
}

void SocketAddress::setPort(uint16_t port) {
    this->port = port;
}

SocketAddress::operator std::string() const {
    std::ostringstream stream;

    stream << "Socket Address {" << std::endl
            << "    Address: " << address << "," << std::endl
            << "    Port: " << port << std::endl
            << "}";

    return stream.str();
}

std::ostream& operator<<(std::ostream &os, const SocketAddress &o) {
    return os << (std::string) o;
}

}