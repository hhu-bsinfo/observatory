package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

class MessagingThroughputOperation extends ThroughputOperation {

    MessagingThroughputOperation(Benchmark benchmark, Mode mode, int operationCount, int operationSize) {
        super(benchmark, mode, operationCount, operationSize);
    }

    @Override
    boolean needsFilledReceiveQueue() {
        return getMode() == Mode.SEND;
    }

    @Override
    Status warmUp(int operationCount) {
        if(getMode() == Mode.SEND) {
            return getBenchmark().benchmarkMessagingSendThroughput(operationCount);
        } else {
            return getBenchmark().benchmarkMessagingReceiveThroughput(operationCount);
        }
    }

    @Override
    public Status execute() {
        Status status;

        long startTime = System.nanoTime();

        if(getMode() == Mode.SEND) {
            status = getBenchmark().benchmarkMessagingSendThroughput(getMeasurement().getOperationCount());
        } else {
            status = getBenchmark().benchmarkMessagingReceiveThroughput(getMeasurement().getOperationCount());
        }

        long time = System.nanoTime() - startTime;

        getMeasurement().setMeasuredTime(time);

        return status;
    }
}
