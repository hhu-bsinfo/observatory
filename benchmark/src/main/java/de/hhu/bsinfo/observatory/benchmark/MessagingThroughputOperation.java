package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import de.hhu.bsinfo.observatory.benchmark.result.ThroughputMeasurement;

class MessagingThroughputOperation extends ThroughputOperation {

    MessagingThroughputOperation(Benchmark benchmark, Mode mode, int operationCount, int operationSize) {
        super(benchmark, mode, operationCount, operationSize);
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
