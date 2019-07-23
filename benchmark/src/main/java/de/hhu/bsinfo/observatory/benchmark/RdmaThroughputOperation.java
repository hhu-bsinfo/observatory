package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.Benchmark.RdmaMode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdmaThroughputOperation extends ThroughputOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(RdmaThroughputOperation.class);

    private final RdmaMode rdmaMode;

    RdmaThroughputOperation(Benchmark benchmark, Mode mode, int operationCount, int operationSize, RdmaMode rdmaMode) {
        super(benchmark, mode, operationCount, operationSize);

        this.rdmaMode = rdmaMode;
    }

    @Override
    boolean needsFilledReceiveQueue() {
        return false;
    }

    @Override
    Status warmUp(int operationCount) {
        Status status = Status.OK;

        if(getMode() == Mode.SEND) {
            status = getBenchmark().performMultipleRdmaOperations(rdmaMode, operationCount);
        }

        if(!getBenchmark().synchronize()) {
            return Status.SYNC_ERROR;
        }

        return status;
    }

    @Override
    public Status execute() {
        Status status = Status.OK;

        if(getMode() == Mode.SEND) {
            long startTime = System.nanoTime();
            status = getBenchmark().performMultipleRdmaOperations(rdmaMode, getMeasurement().getOperationCount());
            long time = System.nanoTime() - startTime;

            getMeasurement().setMeasuredTime(time);

            ByteBuffer timeBuffer = ByteBuffer.allocate(Long.BYTES);
            timeBuffer.putLong(time);

            try {
                new DataOutputStream(getBenchmark().getOffChannelSocket().getOutputStream()).write(timeBuffer.array());
            } catch (IOException e) {
                LOGGER.error("Sending measured time to remote benchmark failed", e);
            }

            if(status != Status.OK) {
                return status;
            }
        } else {
            try {
                byte[] timeBytes = new byte[Long.BYTES];
                new DataInputStream(getBenchmark().getOffChannelSocket().getInputStream()).readFully(timeBytes);

                ByteBuffer timeBuffer = ByteBuffer.wrap(timeBytes);

                getMeasurement().setMeasuredTime(timeBuffer.getLong());
            } catch (IOException e) {
                LOGGER.error("Receiving measured time from remote benchmark failed", e);
            }
        }

        return status;
    }
}
