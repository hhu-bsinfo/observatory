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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OperationPhase extends BenchmarkPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationPhase.class);

    private final Operation operation;

    OperationPhase(Benchmark benchmark, Operation operation) {
        super(benchmark);

        this.operation = operation;
    }

    private void calculateOverhead() {
        IbPerfCounter perfCounter = getBenchmark().getPerfCounter();

        try {
            perfCounter.refreshCounters();
        } catch (IbFileException | IbMadException e) {
            LOGGER.error("Unable to refresh performance counters", e);
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
    }

    private void saveSingleResult(String path, String operationSize, Map<String, String> valueMap) throws IOException {
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

        for(String value : valueMap.values()) {
            writer.append(",").append(value);
        }

        writer.newLine();

        writer.flush();
        writer.close();
    }

    private void saveResults() throws IOException {
        if(operation instanceof ThroughputOperation) {
            ThroughputMeasurement measurement = ((ThroughputOperation) operation).getMeasurement();

            saveSingleResult(getBenchmark().getResultPath() + "/" + operation.getOutputFilename() + ".csv",
                    String.valueOf(measurement.getOperationSize()),
                    new LinkedHashMap<String, String>(){{
                        put("OperationThroughput", String.valueOf(measurement.getOperationThroughput()));
                        put("DataThroughput", String.valueOf(measurement.getDataThroughput()));

                        if(getBenchmark().measureOverhead()) {
                            put("DataOverhead", String.valueOf(operation.getOverheadMeasurement().getOverheadData()));
                            put("DataOverheadFactor", String.valueOf(operation.getOverheadMeasurement().getOverheadFactor()));
                            put("DataThroughputOverhead", String.valueOf(operation.getOverheadMeasurement().getOverheadDataThroughput()));
                        }
                    }});
        } else if(operation instanceof LatencyOperation) {
            LatencyMeasurement measurement = ((LatencyOperation) operation).getMeasurement();

            saveSingleResult(getBenchmark().getResultPath() + "/" + operation.getOutputFilename() + ".csv",
                    String.valueOf(measurement.getOperationSize()),
                    new LinkedHashMap<String, String>(){{
                        put("OperationThroughput", String.valueOf(measurement.getOperationThroughput()));
                        put("AverageLatency", String.valueOf(measurement.getAverageLatency()));
                        put("MinimumLatency", String.valueOf(measurement.getMinimumLatency()));
                        put("MaximumLatency", String.valueOf(measurement.getMaximumLatency()));
                        put("50thLatency", String.valueOf(measurement.getPercentileLatency(0.5f)));
                        put("95thLatency", String.valueOf(measurement.getPercentileLatency(0.95f)));
                        put("99thLatency", String.valueOf(measurement.getPercentileLatency(0.99f)));
                        put("999thLatency", String.valueOf(measurement.getPercentileLatency(0.999f)));
                        put("9999thLatency", String.valueOf(measurement.getPercentileLatency(0.9999f)));

                        if(getBenchmark().measureOverhead()) {
                            put("DataOverhead", String.valueOf(operation.getOverheadMeasurement().getOverheadData()));
                            put("DataOverheadFactor", String.valueOf(operation.getOverheadMeasurement().getOverheadFactor()));
                            put("DataThroughputOverhead", String.valueOf(operation.getOverheadMeasurement().getOverheadDataThroughput()));
                        }
                    }});
        }
    }

    @Override
    Status execute() {
        LOGGER.info("Executing phase of type '{}' with {} operations of size {} bytes", operation.getClass().getSimpleName(),
                operation.getMeasurement().getOperationCount(), operation.getMeasurement().getOperationSize());

        if(!getBenchmark().synchronize()) {
            return Status.SYNC_ERROR;
        }

        if(getBenchmark().measureOverhead()) {
            try {
                getBenchmark().getPerfCounter().resetCounters();
            } catch (IbFileException | IbMadException e) {
                LOGGER.error("Unable to reset performance counters", e);
            }
        }

        Status status = operation.execute();

        if(status == Status.OK && getBenchmark().measureOverhead()) {
            calculateOverhead();
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
