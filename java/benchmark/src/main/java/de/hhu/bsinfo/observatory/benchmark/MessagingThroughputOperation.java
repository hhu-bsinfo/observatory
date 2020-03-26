package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

class MessagingThroughputOperation extends ThroughputOperation {

    MessagingThroughputOperation(Benchmark benchmark, Mode mode, int operationCount, int operationSize) {
        super(benchmark, mode, operationCount, operationSize);
    }

    @Override
    String getOutputFilename() {
        return "Messaging Throughput";
    }

    @Override
    boolean needsFilledReceiveQueue() {
        return getMode() != Mode.SEND;
    }

    @Override
    Status warmUp(int operationCount) {
        if(getMode() == Mode.SEND) {
            return getBenchmark().sendMultipleMessages(operationCount);
        } else {
            return getBenchmark().receiveMultipleMessages(operationCount);
        }
    }

    @Override
    public Status execute() {
        if(getMode() == Mode.SEND) {
            long startTime = System.nanoTime();
            Status status = getBenchmark().sendMultipleMessages(getMeasurement().getOperationCount());
            getMeasurement().setMeasuredTime(System.nanoTime() - startTime);

            return status;
        } else {
            long startTime = System.nanoTime();
            Status status = getBenchmark().receiveMultipleMessages(getMeasurement().getOperationCount());
            getMeasurement().setMeasuredTime(System.nanoTime() - startTime);

            return status;
        }
    }
}
