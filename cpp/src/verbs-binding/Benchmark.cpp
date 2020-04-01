#include "Benchmark.h"

namespace Verbs {

const char *Benchmark::getClassName() const {
    return "Verbs::Benchmark";
}

Observatory::Status Benchmark::initialize() {
    uint32_t deviceNumber = getParameter(PARAM_KEY_DEVICE_NUMBER, DEFAULT_DEVICE_NUMBER);
    uint8_t portNumber = getParameter(PARAM_KEY_PORT_NUMBER, DEFAULT_PORT_NUMBER);
    queueSize = getParameter(PARAM_KEY_QUEUE_SIZE, DEFAULT_QUEUE_SIZE);

    sendWorkCompletions = new ibv_wc[queueSize];
    receiveWorkCompletions = new ibv_wc[queueSize];

    memset(sendWorkCompletions, 0, sizeof(ibv_wc) * queueSize);
    memset(receiveWorkCompletions, 0, sizeof(ibv_wc) * queueSize);

    try {
        context = new ConnectionContext(deviceNumber, portNumber, queueSize);
    } catch(std::runtime_error &e) {
        LOGGER.error("Initializing InfiniBand resources failed\n\033[0m %s", e.what());
        return Observatory::Status::UNKNOWN_ERROR;
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::serve(Observatory::SocketAddress &bindAddress) {
    try {
        context->connect(getOffChannelSocket());
    } catch(std::runtime_error &e) {
        LOGGER.error("Connecting to remote benchmark failed\n\033[0m %s", e.what());
        return Observatory::Status::UNKNOWN_ERROR;
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::connect(Observatory::SocketAddress &bindAddress, Observatory::SocketAddress &remoteAddress) {
    try {
        context->connect(getOffChannelSocket());
    } catch(std::runtime_error &e) {
        LOGGER.error("Connecting to remote benchmark failed\n\033[0m %s", e.what());
        return Observatory::Status::UNKNOWN_ERROR;
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::prepare(uint32_t operationSize) {
    sendBuffer = new uint8_t[operationSize];
    receiveBuffer = new uint8_t[operationSize];

    sendMemoryRegion = ibv_reg_mr(context->getProtectionDomain(), sendBuffer, operationSize, IBV_ACCESS_LOCAL_WRITE | IBV_ACCESS_REMOTE_READ | IBV_ACCESS_REMOTE_WRITE);
    if(sendMemoryRegion == nullptr) {
        LOGGER.error("Registering send memory region failed (%s)", std::strerror(errno));
    }

    receiveMemoryRegion = ibv_reg_mr(context->getProtectionDomain(), receiveBuffer, operationSize, IBV_ACCESS_LOCAL_WRITE | IBV_ACCESS_REMOTE_READ | IBV_ACCESS_REMOTE_WRITE);
    if(receiveMemoryRegion == nullptr) {
        LOGGER.error("Registering receive memory region failed (%s)", std::strerror(errno));
    }

    sendScatterGatherElement.addr = reinterpret_cast<uint64_t>(sendBuffer);
    sendScatterGatherElement.length = operationSize;
    sendScatterGatherElement.lkey = sendMemoryRegion->lkey;

    receiveScatterGatherElement.addr = reinterpret_cast<uint64_t>(receiveBuffer);
    receiveScatterGatherElement.length = operationSize;
    receiveScatterGatherElement.lkey = receiveMemoryRegion->lkey;

    sendWorkRequests = new ibv_send_wr[queueSize];
    receiveWorkRequests = new ibv_recv_wr[queueSize];

    memset(sendWorkRequests, 0, sizeof(ibv_send_wr) * queueSize);
    memset(receiveWorkRequests, 0, sizeof(ibv_recv_wr) * queueSize);

    for(uint32_t i = 0; i < queueSize; i++) {
        sendWorkRequests[i].opcode = IBV_WR_SEND;

        receiveWorkRequests[i].num_sge = 1;
        receiveWorkRequests[i].sg_list = &receiveScatterGatherElement;
    }

    try {
        exchangeMemoryRegionInformation();
    } catch(std::runtime_error &e) {
        LOGGER.error("Exchanging memory region information failed\n\033[0m %s", e.what());
        return Observatory::Status::NETWORK_ERROR;
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::cleanup() {
    int ret = ibv_dereg_mr(sendMemoryRegion);
    if(ret) {
        LOGGER.warn("Unable to deregister send memory region (%s)", ret);
    }

    ret = ibv_dereg_mr(receiveMemoryRegion);
    if(ret) {
        LOGGER.warn("Unable to deregister receive memory region (%s)", ret);
    }

    delete context;
    delete sendBuffer;
    delete receiveBuffer;
    delete sendWorkCompletions;
    delete receiveWorkCompletions;
    delete sendWorkRequests;
    delete receiveWorkRequests;

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::fillReceiveQueue() {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::sendMultipleMessages(uint32_t messageCount) {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::receiveMultipleMessages(uint32_t messageCount) {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::performMultipleRdmaOperations(Benchmark::RdmaMode mode, uint32_t operationCount) {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::sendSingleMessage() {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::performSingleRdmaOperation(Benchmark::RdmaMode mode) {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::performPingPongIterationServer() {
    return Observatory::Status::NOT_IMPLEMENTED;
}

Observatory::Status Benchmark::performPingPongIterationClient() {
    return Observatory::Status::NOT_IMPLEMENTED;
}

MemoryRegionInformation Benchmark::exchangeMemoryRegionInformation() {
    MemoryRegionInformation localInfo(reinterpret_cast<uint64_t>(receiveBuffer), receiveMemoryRegion->rkey);
    auto localBytes = localInfo.toBytes();
    auto remoteBytes = std::shared_ptr<uint8_t[]>(new uint8_t[sizeof(MemoryRegionInformation)]);

    LOGGER.info("Sending local memory region information: %s", static_cast<std::string>(localInfo).c_str());

    uint32_t totalSent = 0;
    while(totalSent < sizeof(MemoryRegionInformation)) {
        uint32_t sent;
        if((sent = ::send(getOffChannelSocket(), localBytes.get() + totalSent, sizeof(MemoryRegionInformation) - totalSent, 0)) < 0) {
            throw std::runtime_error("Unable to send memory region information (" + std::string(std::strerror(errno)) + ")");
        }

        totalSent += sent;
    }

    LOGGER.info("Waiting for remote memory region information");

    uint32_t totalReceived = 0;
    while(totalReceived < sizeof(MemoryRegionInformation)) {
        uint32_t received;
        if((received = ::recv(getOffChannelSocket(), remoteBytes.get() + totalReceived, sizeof(MemoryRegionInformation) - totalReceived, 0)) < 0) {
            throw std::runtime_error("Unable to receive memory region information (" + std::string(std::strerror(errno)) + ")");
        }

        totalReceived += received;
    }

    auto remoteInfo = MemoryRegionInformation::fromBytes(remoteBytes.get());
    LOGGER.info("Received remote memory region information: %s", static_cast<std::string>(remoteInfo).c_str());

    return remoteInfo;
}

}