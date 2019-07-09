package de.hhu.bsinfo.observatory.example;

import de.hhu.bsinfo.observatory.benchmark.Benchmark;
import de.hhu.bsinfo.observatory.benchmark.Status;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleBenchmark extends Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleBenchmark.class);

    private Socket socket;

    @Override
    protected Status initialize() {
        LOGGER.info("param1: {}, param2: {}", getParameter("param1"), getParameter("param2"));

        return Status.OK;
    }

    @Override
    protected Status serve(InetSocketAddress bindAddress) {
        LOGGER.info("Listening on address {}", bindAddress.toString());

        try {
            ServerSocket serverSocket = new ServerSocket(bindAddress.getPort(), 1, bindAddress.getAddress());

            socket = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status connect(InetSocketAddress serverAddress) {
        LOGGER.info("Connecting to address {}", serverAddress.toString());

        try {
            socket = new Socket(serverAddress.getAddress(), serverAddress.getPort());
        } catch (IOException e) {
            e.printStackTrace();
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
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
}
