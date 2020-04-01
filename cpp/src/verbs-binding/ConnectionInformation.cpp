#include <sstream>
#include "ConnectionInformation.h"

namespace Verbs {

ConnectionInformation::ConnectionInformation(uint8_t portNumber, uint16_t localId, uint32_t queuePairNumber) :
        portNumber(portNumber),
        localId(localId),
        queuePairNumber(queuePairNumber) {}

ConnectionInformation ConnectionInformation::fromBytes(const uint8_t *bytes) {
    uint8_t portNumber = *bytes;
    bytes += sizeof(uint8_t);

    uint16_t localId = *reinterpret_cast<const uint16_t*>(bytes);
    bytes += sizeof(uint16_t);

    uint32_t queuePairNumber = *reinterpret_cast<const uint32_t*>(bytes);

    return ConnectionInformation(portNumber, localId, queuePairNumber);
}

std::shared_ptr<uint8_t[]> ConnectionInformation::toBytes() {
    auto bytes = std::shared_ptr<uint8_t[]>(new uint8_t[sizeof(ConnectionInformation)]);
    uint8_t index = 0;

    *bytes.get() = portNumber;
    index += sizeof(uint8_t);

    *reinterpret_cast<uint16_t*>(&bytes.get()[index]) = localId;
    index += sizeof(uint16_t);

    *reinterpret_cast<uint32_t*>(&bytes.get()[index]) = queuePairNumber;

    return bytes;
}

uint8_t ConnectionInformation::getPortNumber() {
    return portNumber;
}

uint16_t ConnectionInformation::getLocalId() {
    return localId;
}

uint32_t ConnectionInformation::getQueuePairNumber() {
    return queuePairNumber;
}

ConnectionInformation::operator std::string() {
    std::ostringstream stream;

    stream << "ConnectionInformation {"
           << "\n\t" << "portNumber=" << static_cast<uint16_t>(portNumber)
           << ",\n\t" << "localId=" << localId
           << ",\n\t" << "queuePairNumber=" << queuePairNumber
           << "\n}";

    return stream.str();
}

}