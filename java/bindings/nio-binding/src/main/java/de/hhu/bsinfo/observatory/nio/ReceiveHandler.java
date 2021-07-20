package de.hhu.bsinfo.observatory.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ReceiveHandler implements Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveHandler.class);

    private final SocketChannel socket;
    private final SelectionKey key;
    private final ByteBuffer messageBuffer;

    private int remainingMessages;
    private boolean finished = false;

    public ReceiveHandler(final SocketChannel socket, final SelectionKey key, final ByteBuffer messageBuffer, final int messageCount) {
        this.socket = socket;
        this.key = key;
        this.messageBuffer = messageBuffer;
        remainingMessages = messageCount;
        key.interestOps(SelectionKey.OP_READ);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void reset(int messageCount) {
        this.remainingMessages = messageCount;
        finished = false;
        key.interestOps(SelectionKey.OP_READ);
    }

    @Override
    public void run() {
        if (key.isReadable()) {
            try {
                socket.read(messageBuffer);
            } catch (IOException e) {
                LOGGER.error("Failed to receive a message!");
            }

            if (!messageBuffer.hasRemaining()) {
                messageBuffer.clear();
                remainingMessages--;
            }

            if (remainingMessages <= 0) {
                key.interestOps(SelectionKey.OP_WRITE);
            }
        } else if (key.isWritable()) {
            try {
                socket.write(messageBuffer);
            } catch (IOException e) {
                LOGGER.error("Failed to send a message!");
            }

            if (!messageBuffer.hasRemaining()) {
                messageBuffer.clear();
                finished = true;
            }
        }
    }
}
