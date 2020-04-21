package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.jdetector.lib.IbPerfCounter;
import de.hhu.bsinfo.jdetector.lib.exception.IbFileException;
import de.hhu.bsinfo.jdetector.lib.exception.IbMadException;
import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.LatencyMeasurement;
import de.hhu.bsinfo.observatory.benchmark.result.OverheadMeasurement;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import de.hhu.bsinfo.observatory.benchmark.result.ThroughputMeasurement;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OperationPhase extends BenchmarkPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationPhase.class);

    private final Operation operation;

    OperationPhase(Benchmark benchmark, Operation operation) {
        super(benchmark);

        this.operation = operation;
    }

    private Status calculateOverhead() {
        IbPerfCounter perfCounter = getBenchmark().getPerfCounter();

        try {
            perfCounter.refreshCounters();
        } catch (IbFileException | IbMadException e) {
            LOGGER.error("Unable to refresh performance counters", e);
            return Status.NETWORK_ERROR;
        }

        if(operation instanceof BidirectionalThroughputOperation || operation instanceof MessagingPingPongOperation) {
            operation.setOverheadMeasurement(new OverheadMeasurement(perfCounter.getXmitDataBytes() +
                    perfCounter.getRcvDataBytes(), operation.getMeasurement()));
        } else if(operation instanceof RdmaReadThroughputOperation || operation instanceof RdmaReadLatencyOperation) {
            operation.setOverheadMeasurement(new OverheadMeasurement(operation.getMode() == Mode.SEND ?
                    perfCounter.getRcvDataBytes() : perfCounter.getXmitDataBytes(), operation.getMeasurement()));
        } else {
            operation.setOverheadMeasurement(new OverheadMeasurement(operation.getMode() == Mode.SEND ?
                    perfCounter.getXmitDataBytes() : perfCounter.getRcvDataBytes(), operation.getMeasurement()));
        }

        return Status.OK;
    }

    private void saveSingleResult(String path, String operationSize, Map<String, Double> valueMap) throws IOException {
        File file = new File(path);

        if(!file.getParentFile().exists()) {
            if(!file.getParentFile().mkdirs()) {
                throw new IOException("Unable to create folder '" + file.getParentFile().getPath() + "'");
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

        if(file.length() == 0) {
            writer.append("Benchmark,Iteration,Size");

            for(String key : valueMap.keySet()) {
                writer.append(",").append(key);
            }

            writer.newLine();
        }

        writer.append(getBenchmark().getResultName()).append(",")
                .append(String.valueOf(getBenchmark().getIterationNumber())).append(",")
                .append(operationSize);

        for(double value : valueMap.values()) {
            writer.append(",").append(String.format("%.12e", value));
        }

        writer.newLine();

        writer.flush();
        writer.close();
    }

    private void saveResults() throws IOException {
        Map<String, Double> valueMap = new TreeMap<>();

        if(operation instanceof ThroughputOperation) {
            ThroughputMeasurement measurement = ((ThroughputOperation) operation).getMeasurement();

            valueMap.put("OperationThroughput", measurement.getOperationThroughput());
            valueMap.put("DataThroughput", measurement.getDataThroughput());
        } else if(operation instanceof LatencyOperation) {
            LatencyMeasurement measurement = ((LatencyOperation) operation).getMeasurement();

            valueMap.put("OperationThroughput", measurement.getOperationThroughput());
            valueMap.put("AverageLatency", measurement.getAverageLatency());
            valueMap.put("MinimumLatency", measurement.getMinimumLatency());
            valueMap.put("MaximumLatency", measurement.getMaximumLatency());
            valueMap.put("50thLatency", measurement.getPercentileLatency(0.5f));
            valueMap.put("95thLatency", measurement.getPercentileLatency(0.95f));
            valueMap.put("99thLatency", measurement.getPercentileLatency(0.99f));
            valueMap.put("999thLatency", measurement.getPercentileLatency(0.999f));
            valueMap.put("9999thLatency", measurement.getPercentileLatency(0.9999f));
        }

        if(getBenchmark().measureOverhead()) {
            valueMap.put("DataOverheadFactor", operation.getOverheadMeasurement().getOverheadFactor());
            valueMap.put("DataOverheadPercentage", operation.getOverheadMeasurement().getOverheadPercentage());
            valueMap.put("DataOverheadThroughput", operation.getOverheadMeasurement().getOverheadDataThroughput());
        }

        saveSingleResult(getBenchmark().getResultPath() + "/" + operation.getOutputFilename() + ".csv",
                String.valueOf(operation.getMeasurement().getOperationSize()), valueMap);
    }

    @Override
    Status execute() {
        LOGGER.info("Executing phase of type '{}' with {} operations of size {} bytes", operation.getClass().getSimpleName(),
                operation.getMeasurement().getOperationCount(), operation.getMeasurement().getOperationSize());

        if(getBenchmark().measureOverhead()) {
            try {
                getBenchmark().getPerfCounter().resetCounters();
            } catch (IbFileException | IbMadException e) {
                LOGGER.error("Unable to reset performance counters", e);
                return Status.NETWORK_ERROR;
            }
        }

        Status status = operation.execute();

        if(status == Status.OK && getBenchmark().measureOverhead()) {
            Status overheadStatus = calculateOverhead();
            if(overheadStatus != Status.OK) {
                LOGGER.error("Measuring overhead failed with status [{}]", overheadStatus);
                return overheadStatus;
            }
        }

        if(status == Status.OK && getBenchmark().isServer()) {
            if(getBenchmark().measureOverhead()) {
                LOGGER.info("Operation finished with results:\n{},\n{}", operation.getMeasurement(), operation.getOverheadMeasurement());
            } else {
                LOGGER.info("Operation finished with results:\n{}", operation.getMeasurement());
            }

            try {
                saveResults();
            } catch (IOException e) {
                LOGGER.error("Unable to save results", e);
                return Status.FILE_ERROR;
            }
        }

        return status;
    }
}
