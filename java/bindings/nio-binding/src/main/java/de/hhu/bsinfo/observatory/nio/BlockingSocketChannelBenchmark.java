package de.hhu.bsinfo.observatory.nio;

import de.hhu.bsinfo.observatory.benchmark.result.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class BlockingSocketChannelBenchmark extends SocketChannelBenchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockingSocketChannelBenchmark.class);

    public BlockingSocketChannelBenchmark(SocketChannel socket) {
        super(socket);
    }

    @Override
    Status sendMultipleMessages(int messageCount) {
        sendBuffer.clear();
        receiveBuffer.clear();

        try {
            for (int i = 0; i < messageCount; i++) {
                socket.write(sendBuffer);
                sendBuffer.clear();
            }

            socket.write(sendBuffer);
            while (receiveBuffer.hasRemaining()) {
                socket.read(receiveBuffer);
            }
        } catch (IOException e) {
            LOGGER.error("Sending messages failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    Status receiveMultipleMessages(int messageCount) {
        sendBuffer.clear();
        receiveBuffer.clear();

        try {
            for (int i = 0; i < messageCount; i++) {
                while (receiveBuffer.hasRemaining()) {
                    socket.read(receiveBuffer);
                }
                receiveBuffer.clear();
            }

            socket.write(sendBuffer);
            while (receiveBuffer.hasRemaining()) {
                socket.read(receiveBuffer);
            }
        } catch (IOException e) {
            LOGGER.error("Receiving messages failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    Status sendSingleMessage() {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    Status performPingPongIterationServer() {
        try {
            socket.write(sendBuffer);
            sendBuffer.flip();

            while (sendBuffer.hasRemaining()) {
                socket.read(sendBuffer);
            }
            sendBuffer.clear();
        } catch (IOException e) {
            LOGGER.error("Performing ping pong iteration failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    Status performPingPongIterationClient() {
        try {
            while (sendBuffer.hasRemaining()) {
                socket.read(sendBuffer);
            }
            sendBuffer.flip();

            socket.write(sendBuffer);
            sendBuffer.clear();
        } catch (IOException e) {
            LOGGER.error("Performing ping pong iteration failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }
}
