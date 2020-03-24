#include <vector>
#include <stdexcept>
#include "SocketAddressConverter.h"

std::vector<std::string> SocketAddressConverter::splitAddressString(std::string address) {
    std::vector<std::string> ret;

    size_t pos = 0;
    std::string token;

    while (pos != std::string::npos) {
        pos = address.find(':');
        ret.push_back(address.substr(0, pos));
        address.erase(0, pos + 1);
    }

    return ret;
}

void SocketAddressConverter::operator()(const std::string &name, const std::string &value,
        Observatory::SocketAddress &destination) {

    std::vector<std::string> splitAddress = splitAddressString(value);

    if(splitAddress.empty() || splitAddress.size() > 2) {
        throw std::runtime_error("Invalid connection string specified");
    }

    std::string defaultAddress(DEFAULT_ADDRESS);

    destination.setAddress(splitAddress[0].empty() ? defaultAddress : splitAddress[0]);
    destination.setPort(splitAddress.size() == 1 ? DEFAULT_PORT: std::stoi(splitAddress[1]));
}
