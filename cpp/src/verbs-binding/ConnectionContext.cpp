#include <cstring>
#include <sys/socket.h>
#include "ConnectionContext.h"
#include "ConnectionInformation.h"

namespace Verbs {

ConnectionContext::ConnectionContext(uint32_t deviceNumber, uint8_t portNumber, uint32_t queueSize) :
        portNumber(portNumber) {
    int numDevices;

    ibv_device **devices = ibv_get_device_list(&numDevices);
    if(devices == nullptr) {
        throw std::runtime_error("Unable to get device list (" + std::string(std::strerror(errno)) + ")");
    }

    if(static_cast<uint32_t>(numDevices) <= deviceNumber) {
        ibv_free_device_list(devices);
        throw std::runtime_error("Invalid device number '" + std::to_string(deviceNumber) + "'. Only " + std::to_string(numDevices) +
                                 "InfiniBand " + (numDevices == 1 ? "device was" : "devices were") + " found in your system");
    }

    context = ibv_open_device(devices[deviceNumber]);
    ibv_free_device_list(devices);
    if(context == nullptr) {
        throw std::runtime_error("Unable to open context (" + std::string(std::strerror(errno)) + ")");
    }

    LOGGER.info("Opened context for device %s", ibv_get_device_name(context->device));

    protectionDomain = ibv_alloc_pd(context);
    if(context == nullptr) {
        throw std::runtime_error("Unable to allocate protection domain (" + std::string(std::strerror(errno)) + ")");
    }

    LOGGER.info("Allocated protection domain");

    int ret = ibv_query_port(context, portNumber, &port);
    if(ret) {
        throw std::runtime_error("Unable to query port number '" + std::to_string(portNumber) + "' (" + std::string(std::strerror(ret)) + ")");
    }

    sendCompletionQueue = ibv_create_cq(context, queueSize, nullptr, nullptr, 0);
    if(sendCompletionQueue == nullptr) {
        throw std::runtime_error("Unable to create send completion queue (" + std::string(std::strerror(errno)) + ")");
    }

    receiveCompletionQueue = ibv_create_cq(context, queueSize, nullptr, nullptr, 0);
    if(receiveCompletionQueue == nullptr) {
        throw std::runtime_error("Unable to create receive completion queue (" + std::string(std::strerror(errno)) + ")");
    }

    LOGGER.info("Created completion queues");

    ibv_qp_init_attr queuePairInitialAttributes{};
    queuePairInitialAttributes.qp_type = IBV_QPT_RC;
    queuePairInitialAttributes.send_cq = sendCompletionQueue;
    queuePairInitialAttributes.recv_cq = receiveCompletionQueue;
    queuePairInitialAttributes.cap.max_send_wr = queueSize;
    queuePairInitialAttributes.cap.max_recv_wr = queueSize;
    queuePairInitialAttributes.cap.max_send_sge = 1;
    queuePairInitialAttributes.cap.max_recv_sge = 1;

    queuePair = ibv_create_qp(protectionDomain, &queuePairInitialAttributes);
    if(queuePair == nullptr) {
        throw std::runtime_error("Unable to create queue pair (" + std::string(std::strerror(errno)) + ")");
    }

    LOGGER.info("Created queue pair");

    ibv_qp_attr queuePairAttributes{};
    queuePairAttributes.qp_state = IBV_QPS_INIT;
    queuePairAttributes.pkey_index = 0;
    queuePairAttributes.port_num = 1;
    queuePairAttributes.qp_access_flags = IBV_ACCESS_LOCAL_WRITE | IBV_ACCESS_REMOTE_READ | IBV_ACCESS_REMOTE_WRITE;

    ret = ibv_modify_qp(queuePair, &queuePairAttributes, IBV_QP_STATE | IBV_QP_PKEY_INDEX | IBV_QP_PORT | IBV_QP_ACCESS_FLAGS);
    if(ret) {
        throw std::runtime_error("Unable to move queue pair into INIT state (" + std::string(std::strerror(ret)) + ")");
    }

    LOGGER.info("Moved queue pair into INIT state");
}

void ConnectionContext::connect(int socket) {
    ConnectionInformation localInfo(portNumber, port.lid, queuePair->qp_num);
    auto localBytes = localInfo.toBytes();
    auto remoteBytes = std::shared_ptr<uint8_t[]>(new uint8_t[sizeof(ConnectionInformation)]);

    LOGGER.info("Sending local connection information: %s", static_cast<std::string>(localInfo).c_str());

    uint32_t totalSent = 0;
    while(totalSent < sizeof(ConnectionInformation)) {
        uint32_t sent;
        if((sent = ::send(socket, localBytes.get() + totalSent, sizeof(ConnectionInformation) - totalSent, 0)) < 0) {
            throw std::runtime_error("Unable to send connection information (" + std::string(std::strerror(errno)) + ")");
        }

        totalSent += sent;
    }

    LOGGER.info("Waiting for remote connection information");

    uint32_t totalReceived = 0;
    while(totalReceived < sizeof(ConnectionInformation)) {
        uint32_t received;
        if((received = ::recv(socket, remoteBytes.get() + totalReceived, sizeof(ConnectionInformation) - totalReceived, 0)) < 0) {
            throw std::runtime_error("Unable to receive connection information (" + std::string(std::strerror(errno)) + ")");
        }

        totalReceived += received;
    }

    auto remoteInfo = ConnectionInformation::fromBytes(remoteBytes.get());
    LOGGER.info("Received remote connection information: %s", static_cast<std::string>(remoteInfo).c_str());
}

ibv_pd* ConnectionContext::getProtectionDomain() {
    return protectionDomain;
}

ibv_cq* ConnectionContext::getSendCompletionQueue() {
    return sendCompletionQueue;
}

ibv_cq* ConnectionContext::getReceiveCompletionQueue() {
    return receiveCompletionQueue;
}

ibv_qp* ConnectionContext::getQueuePair() {
    return queuePair;
}

ConnectionContext::~ConnectionContext() {
    if(ibv_destroy_qp(queuePair)) {
        LOGGER.warn("Unable to destroy queue pair (%s)", std::strerror(errno));
    }

    if(ibv_destroy_cq(sendCompletionQueue)) {
        LOGGER.warn("Unable to destroy send completion queue (%s)", std::strerror(errno));
    }

    if(ibv_destroy_cq(receiveCompletionQueue)) {
        LOGGER.warn("Unable to destroy receive mpletion queue (%s)", std::strerror(errno));
    }

    if(ibv_close_device(context)) {
        LOGGER.warn("Unable to close context (%s)", std::strerror(errno));
    }
}

}