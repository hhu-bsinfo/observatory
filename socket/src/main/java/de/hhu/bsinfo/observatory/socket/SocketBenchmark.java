package de.hhu.bsinfo.observatory.socket;

import de.hhu.bsinfo.observatory.benchmark.Benchmark;
import de.hhu.bsinfo.observatory.benchmark.result.BenchmarkMode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import de.hhu.bsinfo.observatory.benchmark.result.ThroughputMeasurement;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketBenchmark extends Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketBenchmark.class);

    private Socket socket;

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

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    @Override
    protected Status connect(InetSocketAddress bindAddress, InetSocketAddress serverAddress) {
        LOGGER.info("Connecting to address {}", serverAddress.toString());

        try {
            socket = new Socket(serverAddress.getAddress(), serverAddress.getPort(), bindAddress.getAddress(), bindAddress.getPort());
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

    @Override
    protected Status measureMessagingThroughput(BenchmarkMode mode, ThroughputMeasurement measurement) {
        try {
            if (mode == BenchmarkMode.SEND) {
                return measureSendThroughput(measurement);
            } else {
                return measureReceiveThroughput(measurement);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Status.NETWORK_ERROR;
        }
    }

    @Override
    protected Status measureRdmaThroughput(BenchmarkMode mode, RdmaMode rdmaMode, ThroughputMeasurement measurement) {
        return Status.NOT_IMPLEMENTED;
    }

    private Status measureSendThroughput(ThroughputMeasurement measurement) throws IOException {
        byte[] buffer = new byte[measurement.getOperationSize()];

        DataOutputStream stream = new DataOutputStream(socket.getOutputStream());

        long start = System.nanoTime();

        for(int i = 0; i < measurement.getOperationCount(); i++) {
            stream.write(buffer);
        }

        measurement.setMeasuredTime(System.nanoTime() - start);

        return Status.OK;
    }

    private Status measureReceiveThroughput(ThroughputMeasurement measurement) throws IOException {
        byte[] buffer = new byte[measurement.getOperationSize()];

        DataInputStream stream = new DataInputStream(socket.getInputStream());

        long start = System.nanoTime();

        for(int i = 0; i < measurement.getOperationCount(); i++) {
            stream.readFully(buffer);
        }

        measurement.setMeasuredTime(System.nanoTime() - start);

        return Status.OK;
    }
}
