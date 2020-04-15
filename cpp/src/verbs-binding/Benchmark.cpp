#include <verbs.h>
#include "Benchmark.h"

namespace Verbs {

Benchmark::Benchmark() : remoteMemoryRegionInfo(0, 0) {}

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

Observatory::Status Benchmark::prepare(uint32_t operationSize, uint32_t operationCount) {
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
        sendWorkRequests[i].num_sge = 1;
        sendWorkRequests[i].sg_list = &sendScatterGatherElement;

        receiveWorkRequests[i].num_sge = 1;
        receiveWorkRequests[i].sg_list = &receiveScatterGatherElement;
    }

    try {
        remoteMemoryRegionInfo = exchangeMemoryRegionInformation();
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
    delete[] sendBuffer;
    delete[] receiveBuffer;
    delete[] sendWorkCompletions;
    delete[] receiveWorkCompletions;
    delete[] sendWorkRequests;
    delete[] receiveWorkRequests;

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::fillReceiveQueue() {
    uint32_t batch = queueSize - pendingReceiveCompletions;

    try {
        postReceive(batch);
    } catch(std::runtime_error &e) {
        LOGGER.error("Filling receive queue failed\n\033[0m %s\n", e.what());
        return Observatory::Status::NETWORK_ERROR;
    }

    pendingReceiveCompletions += batch;

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::sendMultipleMessages(uint32_t messageCount) {
    int32_t messagesLeft = messageCount;

    try {
        while(messagesLeft > 0) {
            int32_t batchSize = queueSize - pendingSendCompletions;

            if(batchSize > messagesLeft) {
                batchSize = messagesLeft;
            }

            postSend(batchSize);

            pendingSendCompletions += batchSize;
            messagesLeft -= batchSize;

            pendingSendCompletions -= pollCompletions(Mode::SEND);
        }

        while(pendingSendCompletions > 0) {
            pendingSendCompletions -= pollCompletions(Mode::SEND);
        }
    } catch(std::runtime_error &e) {
        LOGGER.error("Sending messages failed\n\033[0m %s\n", e.what());
        return Observatory::Status::NETWORK_ERROR;
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::receiveMultipleMessages(uint32_t messageCount) {
    int32_t messagesLeft = messageCount - pendingReceiveCompletions;

    try {
        while(messagesLeft > 0) {
            int32_t batchSize = queueSize - pendingReceiveCompletions;

            if(batchSize > messagesLeft) {
                batchSize = messagesLeft;
            }

            postReceive(batchSize);

            pendingReceiveCompletions += batchSize;
            messagesLeft -= batchSize;

            pendingReceiveCompletions -= pollCompletions(Mode::RECEIVE);
        }

        while(pendingReceiveCompletions > 0) {
            pendingReceiveCompletions -= pollCompletions(Mode::RECEIVE );
        }
    } catch(std::runtime_error &e) {
        LOGGER.error("Sending messages failed\n\033[0m %s\n", e.what());
        return Observatory::Status::NETWORK_ERROR;
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::performMultipleRdmaOperations(Benchmark::RdmaMode mode, uint32_t operationCount) {
    int32_t operationsLeft = operationCount;

    try {
        while(operationsLeft > 0) {
            int32_t batchSize = queueSize - pendingSendCompletions;

            if(batchSize > operationsLeft) {
                batchSize = operationsLeft;
            }

            postRdma(batchSize, mode);

            pendingSendCompletions += batchSize;
            operationsLeft -= batchSize;

            pendingSendCompletions -= pollCompletions(Mode::SEND);
        }

        while(pendingSendCompletions > 0) {
            pendingSendCompletions -= pollCompletions(Mode::SEND);
        }
    } catch(std::runtime_error &e) {
        LOGGER.error("Performing RDMA operations failed\n\033[0m %s\n", e.what());
        return Observatory::Status::NETWORK_ERROR;
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::sendSingleMessage() {
    uint32_t polled = 0;

    try {
        postSend(1);

        do {
            polled = pollCompletions(Mode::SEND);
        } while(polled == 0);
    } catch(std::runtime_error &e) {
        LOGGER.error("Sending message failed\n\033[0m %s\n", e.what());
        return Observatory::Status::NETWORK_ERROR;
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::performSingleRdmaOperation(Benchmark::RdmaMode mode) {
    uint32_t polled = 0;

    try {
        postRdma(1, mode);

        do {
            polled = pollCompletions(Mode::SEND);
        } while(polled == 0);
    } catch(std::runtime_error &e) {
        LOGGER.error("Sending message failed\n\033[0m %s\n", e.what());
        return Observatory::Status::NETWORK_ERROR;
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::performPingPongIterationServer() {
    uint32_t polled = 0;

    try {
        postSend(1);

        do {
            polled = pollCompletions(Mode::SEND);
        } while(polled == 0);

        do {
            polled = pollCompletions(Mode::RECEIVE);
        } while(polled == 0);

        postReceive(1);
    } catch(std::runtime_error &e) {
        LOGGER.error("Performing ping pong operation failed\n\033[0m %s\n", e.what());
        return Observatory::Status::NETWORK_ERROR;
    }

    return Observatory::Status::OK;
}

Observatory::Status Benchmark::performPingPongIterationClient() {
    uint32_t polled = 0;

    try {
        do {
            polled = pollCompletions(Mode::RECEIVE);
        } while(polled == 0);

        postSend(1);

        postReceive(1);

        do {
            polled = pollCompletions(Mode::SEND);
        } while(polled == 0);
    } catch(std::runtime_error &e) {
        LOGGER.error("Performing ping pong operation failed\n\033[0m %s\n", e.what());
        return Observatory::Status::NETWORK_ERROR;
    }

    return Observatory::Status::OK;
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

void Benchmark::postSend(uint32_t amount) {
    if(amount == 0) {
        return;
    }

    for(uint32_t i = 0; i < amount; i++) {
        sendWorkRequests[i].opcode = IBV_WR_SEND;
        sendWorkRequests[i].send_flags = IBV_SEND_SIGNALED;

        sendWorkRequests[i].next = i < amount - 1 ? &sendWorkRequests[i + 1] : nullptr;
    }

    ibv_send_wr *bad_wr;

    int ret = ibv_post_send(context->getQueuePair(), sendWorkRequests, &bad_wr);
    if(ret) {
        LOGGER.error("Posting send work requests failed (" + std::string(std::strerror(ret)) + ")");
    }
}

void Benchmark::postReceive(uint32_t amount) {
    if(amount == 0) {
        return;
    }

    for(uint32_t i = 0; i < amount; i++) {
        receiveWorkRequests[i].next = i < amount - 1 ? &receiveWorkRequests[i + 1] : nullptr;
    }

    ibv_recv_wr *bad_wr;

    int ret = ibv_post_recv(context->getQueuePair(), receiveWorkRequests, &bad_wr);
    if(ret) {
        LOGGER.error("Posting receive work requests failed (" + std::string(std::strerror(ret)) + ")");
    }
}

void Benchmark::postRdma(uint32_t amount, RdmaMode mode) {
    if(amount == 0) {
        return;
    }

    for(uint32_t i = 0; i < amount; i++) {
        sendWorkRequests[i].opcode = mode == RdmaMode::WRITE ? IBV_WR_RDMA_WRITE : IBV_WR_RDMA_READ;
        sendWorkRequests[i].send_flags = IBV_SEND_SIGNALED;

        sendWorkRequests[i].wr.rdma.remote_addr = remoteMemoryRegionInfo.getAddress();
        sendWorkRequests[i].wr.rdma.rkey = remoteMemoryRegionInfo.getRemoteKey();

        sendWorkRequests[i].next = i < amount - 1 ? &sendWorkRequests[i + 1] : nullptr;
    }

    ibv_send_wr *bad_wr;

    int ret = ibv_post_send(context->getQueuePair(), sendWorkRequests, &bad_wr);
    if(ret) {
        LOGGER.error("Posting send work requests failed (" + std::string(std::strerror(ret)) + ")");
    }
}

uint32_t Benchmark::pollCompletions(Mode mode) {
    ibv_cq *completionQueue = mode == Mode::SEND ? context->getSendCompletionQueue() : context->getReceiveCompletionQueue();
    ibv_wc *workCompletions = mode == Mode::SEND ? sendWorkCompletions : receiveWorkCompletions;

    int polled = ibv_poll_cq(completionQueue, queueSize, workCompletions);
    if(polled < 0) {
        throw std::runtime_error("Polling completions failed (" + std::string(std::strerror(polled)) + ")");
    }

    for(uint32_t i = 0; i < static_cast<uint32_t>(polled); i++) {
        if(workCompletions[i].status != IBV_WC_SUCCESS) {
            throw std::runtime_error("Work completion failed with status [" + std::string(ibv_wc_status_str(workCompletions[i].status)) + "}");
        }
    }

    return polled;
}

}