#include <sstream>
#include "MemoryRegionInformation.h"

namespace Verbs {

MemoryRegionInformation::MemoryRegionInformation(uint64_t address, uint32_t remoteKey) :
        address(address),
        remoteKey(remoteKey) {}

MemoryRegionInformation Verbs::MemoryRegionInformation::fromBytes(const uint8_t *bytes) {
    uint64_t address = *reinterpret_cast<const uint64_t*>(bytes);
    bytes += sizeof(uint64_t);

    uint32_t remoteKey = *reinterpret_cast<const uint32_t*>(bytes);

    return Verbs::MemoryRegionInformation(address, remoteKey);
}

uint64_t MemoryRegionInformation::getAddress() {
    return address;
}

uint32_t MemoryRegionInformation::getRemoteKey() {
    return remoteKey;
}

std::shared_ptr<uint8_t[]> MemoryRegionInformation::toBytes() {
    auto bytes = std::shared_ptr<uint8_t[]>(new uint8_t[sizeof(MemoryRegionInformation)]);
    uint8_t index = 0;

    *reinterpret_cast<uint64_t*>(&bytes.get()[index]) = address;
    index += sizeof(uint64_t);

    *reinterpret_cast<uint32_t*>(&bytes.get()[index]) = remoteKey;

    return bytes;
}

MemoryRegionInformation::operator std::string() {
    std::ostringstream stream;

    stream << "ConnectionInformation {"
           << "\n\t" << "address=" << address
           << ",\n\t" << "remoteKey=" << remoteKey
           << "\n}";

    return stream.str();
}

}
