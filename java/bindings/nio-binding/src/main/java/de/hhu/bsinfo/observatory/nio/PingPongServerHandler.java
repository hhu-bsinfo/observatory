package de.hhu.bsinfo.observatory.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class PingPongServerHandler implements Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingPongServerHandler.class);

    private final SocketChannel socket;
    private final SelectionKey key;
    private final ByteBuffer messageBuffer;

    private boolean finished = false;

    public PingPongServerHandler(final SocketChannel socket, final SelectionKey key, final ByteBuffer messageBuffer) {
        this.socket = socket;
        this.key = key;
        this.messageBuffer = messageBuffer;
        key.interestOps(SelectionKey.OP_WRITE);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void reset(int messageCount) {
        finished = false;
        key.interestOps(SelectionKey.OP_WRITE);
    }

    @Override
    public void run() {
        if (key.isWritable()) {
            LOGGER.debug("Writing");
            try {
                socket.write(messageBuffer);
            } catch (IOException e) {
                LOGGER.error("Failed to send a message!");
            }

            if (!messageBuffer.hasRemaining()) {
                LOGGER.debug("Switching to OP_READ");
                messageBuffer.flip();
                key.interestOps(SelectionKey.OP_READ);
            }
        } else if (key.isReadable()) {
            LOGGER.debug("Reading");
            try {
                socket.read(messageBuffer);
            } catch (IOException e) {
                LOGGER.error("Failed to receive a message!");
            }

            if (!messageBuffer.hasRemaining()) {
                LOGGER.debug("Switching to OP_WRITE");
                messageBuffer.flip();
                finished = true;
            }
        }
    }
}
