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
        } else {
            operation.setOverheadMeasurement(new OverheadMeasurement(operation.getMode() == Mode.SEND ?
                    perfCounter.getXmitDataBytes() : perfCounter.getRcvDataBytes(), operation.getMeasurement()));
        }
    }

    private void saveSingleResult(String path, String operationSize, String value) throws IOException {
        File file = new File(path);

        if(!file.getParentFile().exists()) {
            if(!file.getParentFile().mkdirs()) {
                throw new IOException("Unable to create folder '" + file.getParentFile().getPath() + "'");
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

        if(file.length() > 0) {
            writer.write(",");
        }

        writer.write(operationSize + ":" + value);

        writer.flush();
        writer.close();
    }

    private void saveResults() throws IOException {
        if(operation instanceof ThroughputOperation) {
            ThroughputMeasurement measurement = ((ThroughputOperation) operation).getMeasurement();

            saveSingleResult(getBenchmark().getResultPath() + "/OperationThroughput/" +
                    getBenchmark().getClass().getSimpleName() + ".csv",
                    String.valueOf(measurement.getOperationSize()),
                    String.valueOf(measurement.getOperationThroughput()));

            saveSingleResult(getBenchmark().getResultPath() + "/DataThroughput/" +
                    getBenchmark().getClass().getSimpleName() + ".csv",
                    String.valueOf(measurement.getOperationSize()),
                    String.valueOf(measurement.getDataThroughput()));
        } else if(operation instanceof LatencyOperation) {
            LatencyMeasurement measurement = ((LatencyOperation) operation).getMeasurement();

            saveSingleResult(getBenchmark().getResultPath() + "/AverageLatency/" +
                            getBenchmark().getClass().getSimpleName() + ".csv",
                    String.valueOf(measurement.getOperationSize()),
                    String.valueOf(measurement.getAverageLatency()));

            saveSingleResult(getBenchmark().getResultPath() + "/MinimumLatency/" +
                            getBenchmark().getClass().getSimpleName() + ".csv",
                    String.valueOf(measurement.getOperationSize()),
                    String.valueOf(measurement.getMinimumLatency()));

            saveSingleResult(getBenchmark().getResultPath() + "/MaximumLatency/" +
                            getBenchmark().getClass().getSimpleName() + ".csv",
                    String.valueOf(measurement.getOperationSize()),
                    String.valueOf(measurement.getMaximumLatency()));

            saveSingleResult(getBenchmark().getResultPath() + "/50thLatency/" +
                            getBenchmark().getClass().getSimpleName() + ".csv",
                    String.valueOf(measurement.getOperationSize()),
                    String.valueOf(measurement.getPercentileLatency(0.5f)));

            saveSingleResult(getBenchmark().getResultPath() + "/95thLatency/" +
                            getBenchmark().getClass().getSimpleName() + ".csv",
                    String.valueOf(measurement.getOperationSize()),
                    String.valueOf(measurement.getPercentileLatency(0.95f)));

            saveSingleResult(getBenchmark().getResultPath() + "/99thLatency/" +
                            getBenchmark().getClass().getSimpleName() + ".csv",
                    String.valueOf(measurement.getOperationSize()),
                    String.valueOf(measurement.getPercentileLatency(0.99f)));

            saveSingleResult(getBenchmark().getResultPath() + "/999thLatency/" +
                            getBenchmark().getClass().getSimpleName() + ".csv",
                    String.valueOf(measurement.getOperationSize()),
                    String.valueOf(measurement.getPercentileLatency(0.999f)));

            saveSingleResult(getBenchmark().getResultPath() + "/9999thLatency/" +
                            getBenchmark().getClass().getSimpleName() + ".csv",
                    String.valueOf(measurement.getOperationSize()),
                    String.valueOf(measurement.getPercentileLatency(0.9999f)));
        }

        if(getBenchmark().measureOverhead()) {
            saveSingleResult(getBenchmark().getResultPath() + "/DataOverhead/" +
                            getBenchmark().getClass().getSimpleName() + ".csv",
                    String.valueOf(operation.getMeasurement().getOperationSize()),
                    String.valueOf(operation.getOverheadMeasurement().getOverheadData()));

            saveSingleResult(getBenchmark().getResultPath() + "/DataOverheadFactor/" +
                            getBenchmark().getClass().getSimpleName() + ".csv",
                    String.valueOf(operation.getMeasurement().getOperationSize()),
                    String.valueOf(operation.getOverheadMeasurement().getOverheadFactor()));

            saveSingleResult(getBenchmark().getResultPath() + "/ThroughputOverhead/" +
                            getBenchmark().getClass().getSimpleName() + ".csv",
                    String.valueOf(operation.getMeasurement().getOperationSize()),
                    String.valueOf(operation.getOverheadMeasurement().getOverheadDataThroughput()));
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
