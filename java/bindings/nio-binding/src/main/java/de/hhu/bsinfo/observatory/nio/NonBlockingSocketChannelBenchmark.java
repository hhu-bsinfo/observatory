package de.hhu.bsinfo.observatory.nio;

import de.hhu.bsinfo.observatory.benchmark.result.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class NonBlockingSocketChannelBenchmark extends SocketChannelBenchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(NonBlockingSocketChannelBenchmark.class);

    private Selector selector;
    private SelectionKey key;
    private Handler handler;

    public NonBlockingSocketChannelBenchmark(SocketChannel socket) {
        super(socket);

        try {
            selector = Selector.open();
        } catch (IOException e) {
            LOGGER.error("Failed to open selector", e);
        }
    }

    @Override
    Status sendMultipleMessages(int messageCount) {
        if (handler == null) {
            try {
                key = socket.register(selector, 0);
                handler = new SendHandler(socket, key, sendBuffer, messageCount);
                key.attach(handler);
            } catch (IOException e) {
                LOGGER.error("Failed to register key", e);
                return Status.IO_ERROR;
            }
        }

        try {
            handler.reset(messageCount);
            pollSelector();
        } catch (IOException e) {
            LOGGER.error("Failed to select keys", e);
            return Status.IO_ERROR;
        }

        return Status.OK;
    }

    @Override
    Status receiveMultipleMessages(int messageCount) {
        if (handler == null) {
            try {
                key = socket.register(selector, 0);
                handler = new ReceiveHandler(socket, key, sendBuffer, messageCount);
                key.attach(handler);
            } catch (IOException e) {
                LOGGER.error("Failed to register key", e);
                return Status.IO_ERROR;
            }
        }

        try {
            handler.reset(messageCount);
            pollSelector();
        } catch (IOException e) {
            LOGGER.error("Failed to select keys", e);
            return Status.IO_ERROR;
        }

        return Status.OK;
    }

    @Override
    Status sendSingleMessage() {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    Status performPingPongIterationServer() {
        if (handler == null) {
            try {
                key = socket.register(selector, 0);
                handler = new PingPongServerHandler(socket, key, sendBuffer);
                key.attach(handler);
            } catch (IOException e) {
                LOGGER.error("Failed to register key", e);
                return Status.IO_ERROR;
            }
        }

        try {
            handler.reset(1);
            pollSelector();
        } catch (IOException e) {
            LOGGER.error("Failed to select keys", e);
            return Status.IO_ERROR;
        }

        return Status.OK;
    }

    @Override
    Status performPingPongIterationClient() {
        if (handler == null) {
            try {
                key = socket.register(selector, 0);
                handler = new PingPongClientHandler(socket, key, sendBuffer);
                key.attach(handler);
            } catch (IOException e) {
                LOGGER.error("Failed to register key", e);
                return Status.IO_ERROR;
            }
        }

        try {
            handler.reset(1);
            pollSelector();
        } catch (IOException e) {
            LOGGER.error("Failed to select keys", e);
            return Status.IO_ERROR;
        }

        return Status.OK;
    }

    private void pollSelector() throws IOException {
        while (!handler.isFinished()) {
            selector.selectNow();

            for (SelectionKey key : selector.selectedKeys()) {
                ((Handler) key.attachment()).run();
            }

            selector.selectedKeys().clear();
        }
    }
}
