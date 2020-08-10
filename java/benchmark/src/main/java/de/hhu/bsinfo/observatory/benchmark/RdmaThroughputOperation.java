package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Connection.Mode;
import de.hhu.bsinfo.observatory.benchmark.Connection.RdmaMode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RdmaThroughputOperation extends ThroughputOperation {

    private static final Logger LOGGER = LoggerFactory.getLogger(RdmaThroughputOperation.class);

    private final RdmaMode rdmaMode;

    RdmaThroughputOperation(Connection connection, Mode mode, int operationCount, int operationSize, RdmaMode rdmaMode) {
        super(connection, mode, operationCount, operationSize);

        this.rdmaMode = rdmaMode;
    }

    @Override
    boolean needsFilledReceiveQueue() {
        return false;
    }

    @Override
    Status warmUp(int operationCount) {
        Status status = Status.OK;

        if (getMode() == Mode.SEND) {
            status = getConnection().performMultipleRdmaOperations(rdmaMode, operationCount);
        }

        if (!getConnection().synchronize()) {
            return Status.SYNC_ERROR;
        }

        return status;
    }

    @Override
    public Status execute() {
        if (getMode() == Mode.SEND) {
            long startTime = System.nanoTime();
            Status status = getConnection().performMultipleRdmaOperations(rdmaMode, getMeasurement().getOperationCount());
            long time = System.nanoTime() - startTime;

            if (status != Status.OK) {
                return status;
            }

            getMeasurement().setMeasuredTime(time);

            ByteBuffer timeBuffer = ByteBuffer.allocate(Long.BYTES);
            timeBuffer.putLong(time);

            try {
                new DataOutputStream(getConnection().getOffChannelSocket().getOutputStream()).write(timeBuffer.array());
            } catch (IOException e) {
                LOGGER.error("Sending measured time to remote benchmark failed", e);
                return Status.NETWORK_ERROR;
            }

            return status;
        } else {
            try {
                byte[] timeBytes = new byte[Long.BYTES];
                new DataInputStream(getConnection().getOffChannelSocket().getInputStream()).readFully(timeBytes);

                ByteBuffer timeBuffer = ByteBuffer.wrap(timeBytes);

                getMeasurement().setMeasuredTime(timeBuffer.getLong());
            } catch (IOException e) {
                LOGGER.error("Receiving measured time from remote benchmark failed", e);
                return Status.NETWORK_ERROR;
            }

            return Status.OK;
        }
    }
}
