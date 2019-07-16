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
            e.printStackTrace();
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
            e.printStackTrace();
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
    protected Status fillReceiveQueue() {
        return Status.NOT_IMPLEMENTED;
    }

    @Override
    protected Status cleanup() {
        LOGGER.info("Closing socket");

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status benchmarkMessagingSendThroughput(int operationCount) {
        try {
            for (int i = 0; i < operationCount; i++) {
                outputStream.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status benchmarkMessagingReceiveThroughput(int operationCount) {
        try {
            for (int i = 0; i < operationCount; i++) {
                inputStream.readFully(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status benchmarkRdmaThroughput(RdmaMode mode, int operationCount) {
        return Status.NOT_IMPLEMENTED;
    }
}
