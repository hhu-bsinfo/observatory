package de.hhu.bsinfo.observatory.socket;

import de.hhu.bsinfo.observatory.benchmark.Benchmark;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketBenchmark extends Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketBenchmark.class);

    private Socket socket;

    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    private byte[] buffer;

    @Override
    protected Status initialize() {
        return Status.OK;
    }

    @Override
    protected Status serve(InetSocketAddress bindAddress) {
        LOGGER.info("Listening on address {}", bindAddress.toString());

        try {
            ServerSocket serverSocket = new ServerSocket(bindAddress.getPort(), 0, bindAddress.getAddress());
            socket = serverSocket.accept();

            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            serverSocket.close();
        } catch (IOException e) {
            LOGGER.error("Connecting to client failed", e);
            return Status.NETWORK_ERROR;
        }

        LOGGER.info("Connected to client {}", socket.getRemoteSocketAddress());

        return Status.OK;
    }

    @Override
    protected Status connect(InetSocketAddress bindAddress, InetSocketAddress serverAddress) {
        LOGGER.info("Connecting to server {}", serverAddress.toString());

        try {
            socket = new Socket(serverAddress.getAddress(), serverAddress.getPort(), bindAddress.getAddress(), 0);

            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            LOGGER.error("Connecting to server failed", e);
            return Status.NETWORK_ERROR;
        }

        LOGGER.info("Successfully connected to server {}", socket.getRemoteSocketAddress());

        return Status.OK;
    }

    @Override
    protected Status prepare(int operationSize) {
        buffer = new byte[operationSize];

        return Status.OK;
    }

    @Override
    protected Status cleanup() {
        LOGGER.info("Closing socket");

        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.error("Closing socket failed");
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status fillReceiveQueue() {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status sendMultipleMessages(int messageCount) {
        try {
            for (int i = 0; i < messageCount; i++) {
                outputStream.write(buffer);
            }

            outputStream.flush();
            socket.getOutputStream().flush();
        } catch (IOException e) {
            LOGGER.error("Sending messages failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status receiveMultipleMessages(int messageCount) {
        try {
            for (int i = 0; i < messageCount; i++) {
                inputStream.readFully(buffer);
            }
        } catch (IOException e) {
            LOGGER.error("Receiving messages failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status performMultipleRdmaOperations(RdmaMode mode, int operationCount) {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status sendSingleMessage() {
        try {
            outputStream.write(buffer);

            outputStream.flush();
            socket.getOutputStream().flush();
        } catch (IOException e) {
            LOGGER.error("Sending single message failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status performSingleRdmaOperation(RdmaMode mode) {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status performPingPongIterationServer() {
        try {
            outputStream.write(buffer);

            outputStream.flush();
            socket.getOutputStream().flush();

            inputStream.readFully(buffer);
        } catch (IOException e) {
            LOGGER.error("Performing ping pong iteration failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status performPingPongIterationClient() {
        try {
            inputStream.readFully(buffer);

            outputStream.write(buffer);

            outputStream.flush();
            socket.getOutputStream().flush();
        } catch (IOException e) {
            LOGGER.error("Performing ping pong iteration failed", e);
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }
}
