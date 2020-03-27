package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;

public class MessagingPingPongOperation extends LatencyOperation {

    MessagingPingPongOperation(Benchmark benchmark, Mode mode, int operationCount, int operationSize) {
        super(benchmark, mode, operationCount, operationSize);
    }

    @Override
    String getOutputFilename() {
        return "Messaging PingPong";
    }

    @Override
    boolean needsFilledReceiveQueue() {
        return true;
    }

    @Override
    Status warmUp(int operationCount) {
        if(getMode() == Mode.SEND) {
            for(int i = 0; i < operationCount; i++) {
                Status status = getBenchmark().performPingPongIterationServer();

                if(status != Status.OK) {
                    return status;
                }
            }
        } else {
            for(int i = 0; i < operationCount; i++) {
                Status status = getBenchmark().performPingPongIterationClient();

                if(status != Status.OK) {
                    return status;
                }
            }
        }

        return Status.OK;
    }

    @Override
    Status execute() {
        if(getMode() == Mode.SEND) {
            long startTime = System.nanoTime();

            for(int i = 0; i < getMeasurement().getOperationCount(); i++) {
                getMeasurement().startSingleMeasurement();
                Status status = getBenchmark().performPingPongIterationServer();
                getMeasurement().stopSingleMeasurement();

                if(status != Status.OK) {
                    return status;
                }
            }

            getMeasurement().setTotalData(getMeasurement().getTotalData() * 2);
            getMeasurement().finishMeasuring(System.nanoTime() - startTime);
        } else {
            for(int i = 0; i < getMeasurement().getOperationCount(); i++) {
                Status status = getBenchmark().performPingPongIterationClient();

                if(status != Status.OK) {
                    return status;
                }
            }
        }

        return Status.OK;
    }
}
