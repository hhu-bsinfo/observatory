package de.hhu.bsinfo.observatory.nio;

import de.hhu.bsinfo.observatory.benchmark.Benchmark;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NioBenchmark extends Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioBenchmark.class);

    private SocketChannelBenchmark benchmark;
    private boolean blocking;

    @Override
    protected Status initialize() {
        blocking = Boolean.parseBoolean(getParameter("blocking", "true"));
        if (blocking) {
            LOGGER.info("Using blocking socket channels for NIO benchmark");
        } else {
            LOGGER.info("Using non-blocking socket channels for NIO benchmark");
        }

        return Status.OK;
    }

    @Override
    protected Status serve(InetSocketAddress bindAddress) {
        LOGGER.info("Listening on address {}", bindAddress.toString());
        SocketChannel socket;

        try {
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(bindAddress.getAddress(), bindAddress.getPort() + 1));
            socket = serverSocket.accept();
            socket.configureBlocking(blocking);
            serverSocket.close();
            LOGGER.info("Connected to client {}", socket.getRemoteAddress());
        } catch (IOException e) {
            LOGGER.error("Connecting to client failed", e);
            return Status.NETWORK_ERROR;
        }

        benchmark = blocking ? new BlockingSocketChannelBenchmark(socket) : new NonBlockingSocketChannelBenchmark(socket);
        return Status.OK;
    }

    @Override
    protected Status connect(InetSocketAddress bindAddress, InetSocketAddress serverAddress) {
        LOGGER.info("Connecting to server {}", serverAddress.toString());
        SocketChannel socket;

        try {
            socket = SocketChannel.open();
            socket.bind(new InetSocketAddress(bindAddress.getAddress(), 0));
            socket.connect(new InetSocketAddress(serverAddress.getAddress(), serverAddress.getPort() + 1));
            socket.configureBlocking(blocking);
            LOGGER.info("Successfully connected to server {} with local address {}", socket.getRemoteAddress(), socket.getLocalAddress());
        } catch (IOException e) {
            LOGGER.error("Connecting to server failed", e);
            return Status.NETWORK_ERROR;
        }

        benchmark = blocking ? new BlockingSocketChannelBenchmark(socket) : new NonBlockingSocketChannelBenchmark(socket);
        return Status.OK;
    }

    @Override
    protected Status prepare(int operationSize, int operationCount) {
        return benchmark.prepare(operationSize, operationCount);
    }

    @Override
    protected Status cleanup() {
        return benchmark.cleanup();
    }

    @Override
    protected Status fillReceiveQueue() {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status sendMultipleMessages(int messageCount) {
        return benchmark.sendMultipleMessages(messageCount);
    }

    @Override
    protected Status receiveMultipleMessages(int messageCount) {
        return benchmark.receiveMultipleMessages(messageCount);
    }

    @Override
    protected Status performMultipleRdmaOperations(RdmaMode mode, int operationCount) {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status sendSingleMessage() {
        return benchmark.sendSingleMessage();
    }

    @Override
    protected Status performSingleRdmaOperation(RdmaMode mode) {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status performPingPongIterationServer() {
        return benchmark.performPingPongIterationServer();
    }

    @Override
    protected Status performPingPongIterationClient() {
        return benchmark.performPingPongIterationClient();
    }
}
