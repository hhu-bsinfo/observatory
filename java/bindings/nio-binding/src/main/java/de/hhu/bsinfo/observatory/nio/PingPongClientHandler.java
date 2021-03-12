package de.hhu.bsinfo.observatory.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class PingPongClientHandler implements Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingPongClientHandler.class);

    private final SocketChannel socket;
    private final SelectionKey key;
    private final ByteBuffer messageBuffer;

    private boolean finished = false;

    public PingPongClientHandler(final SocketChannel socket, final SelectionKey key, final ByteBuffer messageBuffer) {
        this.socket = socket;
        this.key = key;
        this.messageBuffer = messageBuffer;
        key.interestOps(SelectionKey.OP_READ);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void reset(int messageCount) {
        finished = false;
        key.interestOps(SelectionKey.OP_READ);
    }

    @Override
    public void run() {
        if (key.isReadable()) {
            try {
                socket.read(messageBuffer);
            } catch (IOException e) {
                LOGGER.error("Failed to read a message!");
            }

            if (!messageBuffer.hasRemaining()) {
                messageBuffer.flip();
                key.interestOps(SelectionKey.OP_WRITE);
            }
        } else if (key.isWritable()) {
            try {
                socket.write(messageBuffer);
            } catch (IOException e) {
                LOGGER.error("Failed to send a message!");
            }

            if (!messageBuffer.hasRemaining()) {
                messageBuffer.flip();
                finished = true;
            }
        }
    }
}
