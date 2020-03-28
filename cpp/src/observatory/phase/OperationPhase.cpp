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

void OperationPhase::saveSingleResult(std::string &path, uint32_t operationSize, std::map<std::string, std::string> &valueMap) {
    std::ofstream file;

    // UGLY!!
    std::system(("mkdir -p '" + path.substr(0, path.find_last_of('/')) + "'").c_str());

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

    for(const auto &element : valueMap) {
        file << "," << element.second;
    }

    file << std::endl;
    file.close();
}

void OperationPhase::saveResults() {
    if(Util::instanceof<ThroughputOperation>(&operation)) {
        auto &measurement = dynamic_cast<ThroughputMeasurement&>(operation.getMeasurement());
        std::string path = getBenchmark().getResultPath() + "/" + operation.getOutputFilename() + ".csv";
        std::map<std::string, std::string> valueMap = {
                {"OperationThroughput", std::to_string(measurement.getOperationThroughput())},
                {"DataThroughput", std::to_string(measurement.getDataThroughput())}
        };

        if(getBenchmark().measureOverhead()) {
            valueMap["DataOverhead"] = std::to_string(operation.getOverheadMeasurement().getOverheadData());
            valueMap["DataOverheadFactor"] = std::to_string(operation.getOverheadMeasurement().getOverheadFactor());
            valueMap["DataThroughputOverhead"] = std::to_string(operation.getOverheadMeasurement().getOverheadDataThroughput());
        }

        saveSingleResult(path, measurement.getOperationSize(), valueMap);
    } else if(Util::instanceof<LatencyOperation>(&operation)) {
        auto &measurement = dynamic_cast<LatencyMeasurement&>(operation.getMeasurement());
        std::string path = getBenchmark().getResultPath() + "/" + operation.getOutputFilename() + ".csv";
        std::map<std::string, std::string> valueMap = {
                {"OperationThroughput", std::to_string(measurement.getOperationThroughput())},
                {"AverageLatency", std::to_string(measurement.getAverageLatency())},
                {"MinimumLatency", std::to_string(measurement.getMinimumLatency())},
                {"MaximumLatency", std::to_string(measurement.getMaximumLatency())},
                {"50thLatency", std::to_string(measurement.getPercentileLatency(0.5f))},
                {"95thLatency", std::to_string(measurement.getPercentileLatency(0.95f))},
                {"99thLatency", std::to_string(measurement.getPercentileLatency(0.99f))},
                {"999thLatency", std::to_string(measurement.getPercentileLatency(0.999f))},
                {"9999thLatency", std::to_string(measurement.getPercentileLatency(0.9999f))}
        };

        if(getBenchmark().measureOverhead()) {
            valueMap["DataOverhead"] = std::to_string(operation.getOverheadMeasurement().getOverheadData());
            valueMap["DataOverheadFactor"] = std::to_string(operation.getOverheadMeasurement().getOverheadFactor());
            valueMap["DataThroughputOverhead"] = std::to_string(operation.getOverheadMeasurement().getOverheadDataThroughput());
        }

        saveSingleResult(path, measurement.getOperationSize(), valueMap);
    }
}

}