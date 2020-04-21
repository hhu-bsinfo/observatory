#include "OperationPhase.h"
#include <observatory/Benchmark.h>
#include <detector/exception/IbPerfException.h>
#include <observatory/operation/ThroughputOperation.h>
#include <observatory/util/Util.h>
#include <observatory/operation/BidirectionalThroughputOperation.h>
#include <observatory/operation/MessagingPingPongOperation.h>
#include <observatory/operation/RdmaReadThroughputOperation.h>
#include <observatory/operation/RdmaReadLatencyOperation.h>
#include <fstream>
#include <iomanip>

namespace Observatory {

OperationPhase::OperationPhase(Benchmark &benchmark, Operation &operation) :
        BenchmarkPhase(benchmark),
        operation(operation) {}

const char* OperationPhase::getName() {
    return "OperationPhase";
}

Status OperationPhase::execute() {
    LOGGER.info("Executing phase of type '%s' with %u operations of size %u bytes", operation.getClassName(),
                operation.getMeasurement().getOperationCount(), operation.getMeasurement().getOperationSize());

    if(getBenchmark().measureOverhead()) {
        try {
            getBenchmark().getPerfCounter().ResetCounters();
        } catch(Detector::IbPerfException &e) {
            LOGGER.error("Unable to reset performance counters\n\033[0m %s", e.what());
            return Status::NETWORK_ERROR;
        }
    }

    Status status = operation.execute();

    if(status == Status::OK && getBenchmark().measureOverhead()) {
        Status overheadStatus = calculateOverhead();
        if(overheadStatus != Status::OK) {
            LOGGER.error("Measuring overhead failed with status [%s]", getStatusString(overheadStatus));
            return overheadStatus;
        }
    }

    if(status == Status::OK && getBenchmark().isServer()) {
        if(getBenchmark().measureOverhead()) {
            LOGGER.info("Operation finished with results:\n%s,\n%s", static_cast<std::string>(operation.getMeasurement()).c_str(), static_cast<std::string>(operation.getOverheadMeasurement()).c_str());
        } else {
            LOGGER.info("Operation finished with results:\n%s", static_cast<std::string>(operation.getMeasurement()).c_str());
        }

        try {
            saveResults();
        } catch (std::runtime_error &e) {
            LOGGER.error("Unable to save results\n\033[0m %s", e.what());
            return Status::FILE_ERROR;
        }
    }

    return status;
}

Status OperationPhase::calculateOverhead() {
    Detector::IbPerfCounter &perfCounter = getBenchmark().getPerfCounter();

    try {
        perfCounter.RefreshCounters();
    } catch (Detector::IbPerfException &e) {
        LOGGER.error("Unable to refresh performance counters\n\033[0m %s", e.what());
        return Status::NETWORK_ERROR;
    }

    if(Util::instanceof<BidirectionalThroughputOperation>(&operation) || Util::instanceof<MessagingPingPongOperation>(&operation)) {
        operation.setOverheadMeasurement(std::make_shared<OverheadMeasurement>(
                perfCounter.GetXmitDataBytes() + perfCounter.GetRcvDataBytes(), operation.getMeasurement()));
    } else if(Util::instanceof<RdmaReadThroughputOperation>(&operation) || Util::instanceof<RdmaReadLatencyOperation>(&operation)) {
        operation.setOverheadMeasurement(std::make_shared<OverheadMeasurement>(
                operation.getMode() == Benchmark::Mode::SEND ? perfCounter.GetRcvDataBytes() : perfCounter.GetXmitDataBytes(),operation.getMeasurement()));
    } else {
        operation.setOverheadMeasurement(std::make_shared<OverheadMeasurement>(
                operation.getMode() == Benchmark::Mode::SEND ? perfCounter.GetXmitDataBytes() : perfCounter.GetRcvDataBytes(), operation.getMeasurement()));
    }

    return Status::OK;
}

void OperationPhase::saveSingleResult(std::string &path, uint32_t operationSize, std::map<std::string, double> &valueMap) {
    std::ofstream file;
    std::string folderPath = path.substr(0, path.find_last_of('/')) + "'";

    // UGLY!!
    if(std::system(("mkdir -p '" + folderPath).c_str())) {
        throw std::runtime_error("Unable to open file '" + folderPath + "'!");
    }

    file.open(path, std::ios::out | std::ios::app);

    if(!file.is_open()) {
        throw std::runtime_error("Unable to open file '" + path + "'!");
    }

    file.seekp(0, std::ios::end);
    if(file.tellp() == 0) {
        file << "Benchmark,Iteration,Size";

        for(const auto &element : valueMap) {
            file << "," << element.first;
        }

        file << std::endl;
    }

    file << getBenchmark().getResultName() << "," << getBenchmark().getIterationNumber() << "," << operationSize;
    file << std::scientific << std::setprecision(12);

    for(const auto &element : valueMap) {
        file << "," << element.second;
    }

    file << std::endl;
    file.close();
}

void OperationPhase::saveResults() {
    std::string path = getBenchmark().getResultPath() + "/" + operation.getOutputFilename() + ".csv";
    std::map<std::string, double> valueMap;

    if(Util::instanceof<ThroughputOperation>(&operation)) {
        auto &measurement = dynamic_cast<ThroughputMeasurement&>(operation.getMeasurement());
        
        valueMap["OperationThroughput"] = measurement.getOperationThroughput();
        valueMap["DataThroughput"] = measurement.getDataThroughput();
    } else if(Util::instanceof<LatencyOperation>(&operation)) {
        auto &measurement = dynamic_cast<LatencyMeasurement&>(operation.getMeasurement());
        
        valueMap["OperationThroughput"] = measurement.getOperationThroughput();
        valueMap["AverageLatency"] = measurement.getAverageLatency();
        valueMap["MinimumLatency"] = measurement.getMinimumLatency();
        valueMap["MaximumLatency"] = measurement.getMaximumLatency();
        valueMap["50thLatency"] = measurement.getPercentileLatency(0.5f);
        valueMap["95thLatency"] = measurement.getPercentileLatency(0.95f);
        valueMap["99thLatency"] = measurement.getPercentileLatency(0.99f);
        valueMap["999thLatency"] = measurement.getPercentileLatency(0.999f);
        valueMap["9999thLatency"] = measurement.getPercentileLatency(0.9999f);
    }

    if(getBenchmark().measureOverhead()) {
        valueMap["DataOverheadFactor"] = operation.getOverheadMeasurement().getOverheadFactor();
        valueMap["DataOverheadPercentage"] = operation.getOverheadMeasurement().getOverheadPercentage();
        valueMap["DataOverheadThroughput"] = operation.getOverheadMeasurement().getOverheadDataThroughput();
    }

    saveSingleResult(path, operation.getMeasurement().getOperationSize(), valueMap);
}

}