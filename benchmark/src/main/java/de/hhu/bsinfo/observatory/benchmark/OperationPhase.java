package de.hhu.bsinfo.observatory.benchmark;

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

        Status status = operation.execute();

        if(status == Status.OK) {
            LOGGER.info("Operation finished with results:\n{}", operation.getMeasurement());
        } else if(status == Status.OK_NO_MEASUREMENT) {
            return Status.OK;
        }

        return status;
    }
}
