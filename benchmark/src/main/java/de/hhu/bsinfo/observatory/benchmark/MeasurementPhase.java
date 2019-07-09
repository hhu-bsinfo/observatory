package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.BenchmarkMode;
import de.hhu.bsinfo.observatory.benchmark.result.Measurement;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MeasurementPhase extends BenchmarkPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementPhase.class);

    private final BenchmarkMode mode;
    private final Measurement[] measurements;

    MeasurementPhase(Benchmark benchmark, BenchmarkMode mode, Measurement[] measurements) {
        super(benchmark);

        this.mode = mode;
        this.measurements = measurements;
    }

    Measurement[] getMeasurements() {
        return measurements;
    }

    BenchmarkMode getMode() {
        return mode;
    }

    @Override
    public final void runPhase() {
        for(Measurement measurement : measurements) {
            LOGGER.info("Running '{}' with {} operations of size {} byte", getClass().getSimpleName(),
                measurement.getOperationCount(), measurement.getOperationSize());

            Status status = executeSingleMeasurement(measurement);

            if (status != Status.OK) {
                setStatus(status);
                return;
            }

            LOGGER.info("Measurement finished: {}", measurement.toString());
        }

        setStatus(Status.OK);
    }

    protected abstract Status executeSingleMeasurement(Measurement measurement);
}
