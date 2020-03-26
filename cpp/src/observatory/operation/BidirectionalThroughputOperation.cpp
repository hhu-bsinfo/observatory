#include <thread>
#include <utility>
#include "BidirectionalThroughputOperation.h"

namespace Observatory {

BidirectionalThroughputOperation::BidirectionalThroughputOperation(std::shared_ptr<ThroughputOperation> sendOperation, std::shared_ptr<ThroughputOperation> receiveOperation) :
        ThroughputOperation(&sendOperation->getBenchmark(), sendOperation->getMode(), sendOperation->getMeasurement().getOperationCount(), sendOperation->getMeasurement().getOperationSize()),
        sendOperation(std::move(sendOperation)),
        receiveOperation(std::move(receiveOperation)) {}



BidirectionalThroughputOperation* BidirectionalThroughputOperation::instantiate(Benchmark *benchmark, Benchmark::Mode mode, uint32_t operationCount, uint32_t operationSize) const {
    LOGGER.error("BidirectionalThroughputOperation cannot be instantiated by calling 'instantiate()' (Use 'new' instead)");
    return nullptr;
}

const char *BidirectionalThroughputOperation::getClassName() const {
    return "BidirectionalThroughputOperation";
}

const char *BidirectionalThroughputOperation::getOutputFilename() const {
    return (std::string("Bidirectional ") + sendOperation->getOutputFilename()).c_str();
}

bool BidirectionalThroughputOperation::needsFilledReceiveQueue() const {
    return sendOperation->needsFilledReceiveQueue() || receiveOperation->needsFilledReceiveQueue();
}

Status BidirectionalThroughputOperation::warmUp(uint32_t operationCount) {
    LOGGER.info("Executing warm up for bidirectional phase of type '%s'", sendOperation->getClassName());

    Status sendStatus, receiveStatus;

    std::thread sendThread([&]{ sendStatus = sendOperation->warmUp(operationCount); });
    std::thread receiveThread([&]{ receiveStatus = receiveOperation->warmUp(operationCount); });

    sendThread.join();
    receiveThread.join();

    if(sendStatus != Status::OK) {
        return sendStatus;
    } else if(receiveStatus != Status::OK) {
        return receiveStatus;
    }

    return Status::OK;
}

Status BidirectionalThroughputOperation::execute() {
    LOGGER.info("Executing bidirectional phase of type '%s'", sendOperation->getClassName());

    Status sendStatus, receiveStatus;

    std::thread sendThread([&]{ sendStatus = sendOperation->execute(); });
    std::thread receiveThread([&]{ receiveStatus = receiveOperation->execute(); });

    sendThread.join();
    receiveThread.join();

    if(sendStatus != Status::OK) {
        return sendStatus;
    } else if(receiveStatus != Status::OK) {
        return receiveStatus;
    }
    getMeasurement().setTotalData(getMeasurement().getTotalData() * 2);
    getMeasurement().setTotalTime(sendOperation->getMeasurement().getTotalTime());
    getMeasurement().setOperationThroughput(sendOperation->getMeasurement().getOperationThroughput() + receiveOperation->getMeasurement().getOperationThroughput());
    getMeasurement().setDataThroughput(sendOperation->getMeasurement().getDataThroughput() + receiveOperation->getMeasurement().getDataThroughput());

    return Status::OK;
}

}