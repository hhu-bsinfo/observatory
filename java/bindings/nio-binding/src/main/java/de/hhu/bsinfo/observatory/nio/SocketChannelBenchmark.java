package de.hhu.bsinfo.observatory.nio;

import de.hhu.bsinfo.observatory.benchmark.result.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class SocketChannelBenchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketChannelBenchmark.class);
    
    protected final SocketChannel socket;
    protected ByteBuffer sendBuffer;
    protected ByteBuffer receiveBuffer;
    
    public SocketChannelBenchmark(SocketChannel socket) {
        this.socket = socket;
    }

    Status prepare(int operationSize, int operationCount) {
        sendBuffer = ByteBuffer.allocateDirect(operationSize);
        receiveBuffer = ByteBuffer.allocateDirect(operationSize);
        return Status.OK;
    }

    Status cleanup() {
        LOGGER.info("Closing socket channel");

        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.error("Closing socket channel failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    abstract Status sendMultipleMessages(int messageCount);

    abstract Status receiveMultipleMessages(int messageCount);

    abstract Status sendSingleMessage();

    abstract Status performPingPongIterationServer();

    abstract Status performPingPongIterationClient();
}
