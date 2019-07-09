package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.BenchmarkMode;
import de.hhu.bsinfo.observatory.benchmark.result.Measurement;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import de.hhu.bsinfo.observatory.benchmark.result.ValueFormatter;
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

    public Measurement[] getMeasurements() {
        return measurements;
    }

    BenchmarkMode getMode() {
        return mode;
    }

    @Override
    public final void run() {
        for(Measurement measurement : measurements) {
            LOGGER.info("Running '{}' with {} of size {}", getClass().getSimpleName(),
                ValueFormatter.formatValue(measurement.getOperationCount(), "Operations"),
                ValueFormatter.formatValue(measurement.getOperationSize(), "Byte"));

            Status status = execute(measurement);

            if (status != Status.OK) {
                setStatus(status);
                return;
            }

            LOGGER.info("Measurement finished: {}", measurement.toString());
        }

        setStatus(Status.OK);
    }

    protected abstract Status execute(Measurement measurement);
}
