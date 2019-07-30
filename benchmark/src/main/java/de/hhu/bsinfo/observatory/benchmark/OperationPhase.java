package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.jdetector.lib.IbPerfCounter;
import de.hhu.bsinfo.jdetector.lib.exception.IbFileException;
import de.hhu.bsinfo.jdetector.lib.exception.IbMadException;
import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.OverheadMeasurement;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OperationPhase extends BenchmarkPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationPhase.class);

    private final Operation operation;

    OperationPhase(Benchmark benchmark, Operation operation) {
        super(benchmark);

        this.operation = operation;
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
            IbPerfCounter perfCounter = getBenchmark().getPerfCounter();

            try {
                perfCounter.refreshCounters();
            } catch (IbFileException | IbMadException e) {
                LOGGER.error("Unable to refresh performance counters", e);
            }

            if(operation instanceof BidirectionalThroughputOperation) {
                operation.setOverheadMeasurement(new OverheadMeasurement(perfCounter.getXmitDataBytes() +
                        perfCounter.getRcvDataBytes(), operation.getMeasurement()));
            } else {
                operation.setOverheadMeasurement(new OverheadMeasurement(operation.getMode() == Mode.SEND ?
                        perfCounter.getXmitDataBytes() : perfCounter.getRcvDataBytes(), operation.getMeasurement()));
            }
        }

        if(status == Status.OK && getBenchmark().isServer()) {
            if(getBenchmark().measureOverhead()) {
                LOGGER.info("Operation finished with results:\n{},\n{}", operation.getMeasurement(), operation.getOverheadMeasurement());
            } else {
                LOGGER.info("Operation finished with results:\n{}", operation.getMeasurement());
            }
        }

        return status;
    }
}
