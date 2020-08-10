package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Connection.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.Measurement;
import de.hhu.bsinfo.observatory.benchmark.result.OverheadMeasurement;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

abstract class Operation {

    private final Connection connection;
    private final Mode mode;
    private final Measurement measurement;

    private OverheadMeasurement overheadMeasurement;

    Operation(Connection connection, Mode mode, Measurement measurement) {
        this.connection = connection;
        this.mode = mode;
        this.measurement = measurement;
    }

    protected Connection getConnection() {
        return connection;
    }

    protected Measurement getMeasurement() {
        return measurement;
    }

    protected Mode getMode() {
        return mode;
    }

    protected OverheadMeasurement getOverheadMeasurement() {
        return overheadMeasurement;
    }

    void setOverheadMeasurement(OverheadMeasurement overheadMeasurement) {
        this.overheadMeasurement = overheadMeasurement;
    }

    abstract String getOutputFilename();

    abstract boolean needsFilledReceiveQueue();

    abstract Status warmUp(int operationCount);

    abstract Status execute();
}
