package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

public class MessagingLatencyOperation extends LatencyOperation {

    private final String OUTPUT_FILENAME = "Messaging Latency";

    MessagingLatencyOperation(Benchmark benchmark, Mode mode, int operationCount, int operationSize) {
        super(benchmark, mode, operationCount, operationSize);
    }

    @Override
    String getOutputFilename() {
        return OUTPUT_FILENAME;
    }

    @Override
    boolean needsFilledReceiveQueue() {
        return !(getMode() == Mode.SEND);
    }

    @Override
    Status warmUp(int operationCount) {
        if(getMode() == Mode.SEND) {
            for(int i = 0; i < operationCount; i++) {
                Status status = getBenchmark().sendSingleMessage();

                if(status != Status.OK) {
                    return status;
                }
            }

            return Status.OK;
        } else {
            return getBenchmark().receiveMultipleMessage(operationCount);
        }
    }

    @Override
    Status execute() {
        if(getMode() == Mode.SEND) {
            long startTime = System.nanoTime();

            for(int i = 0; i < getMeasurement().getOperationCount(); i++) {
                getMeasurement().startSingleMeasurement();
                Status status = getBenchmark().sendSingleMessage();
                getMeasurement().stopSingleMeasurement();

                if(status != Status.OK) {
                    return status;
                }
            }

            getMeasurement().finishMeasuring(System.nanoTime() - startTime);

            return Status.OK;
        } else {
            return getBenchmark().receiveMultipleMessage(getMeasurement().getOperationCount());
        }
    }
}
