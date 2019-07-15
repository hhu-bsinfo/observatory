package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.BenchmarkMode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import de.hhu.bsinfo.observatory.benchmark.result.ThroughputMeasurement;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(Benchmark.class);

    private static final int OFF_CHANNEL_PORT = 1797;

    private boolean isServer;
    private InetSocketAddress bindAddress;
    private InetSocketAddress remoteAddress;

    private Socket offChannelSocket;

    private final Map<String, String> parameters = new HashMap<>();

    private final List<BenchmarkPhase> phases = new ArrayList<>();

    protected enum RdmaMode {
        READ, WRITE
    }

    void addBenchmarkPhase(BenchmarkPhase phase) {
        phases.add(phase);
    }

    void setParameter(final String key, final String value) {
        parameters.put(key, value);
    }

    protected String getParameter(String key, String defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }

    protected byte getParameter(String key, byte defaultValue) {
        return Byte.parseByte(parameters.getOrDefault(key, String.valueOf(defaultValue)));
    }

    protected short getParameter(String key, short defaultValue) {
        return Short.parseShort(parameters.getOrDefault(key, String.valueOf(defaultValue)));
    }

    protected int getParameter(String key, int defaultValue) {
        return Integer.parseInt(parameters.getOrDefault(key, String.valueOf(defaultValue)));
    }

    protected long getParameter(String key, long defaultValue) {
        return Long.parseLong(parameters.getOrDefault(key, String.valueOf(defaultValue)));
    }

    void setServer(final boolean isServer) {
        this.isServer = isServer;
    }

    void setBindAddress(final InetSocketAddress bindAddress) {
        this.bindAddress = bindAddress;
    }

    void setRemoteAddress(final InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    boolean isServer() {
        return isServer;
    }

    InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    Socket getOffChannelSocket() {
        return offChannelSocket;
    }

    void executePhases() {
        LOGGER.info("Creating socket for off channel communication");

        try {
            if(isServer) {
                LOGGER.info("Waiting for incoming connection request");

                ServerSocket serverSocket = new ServerSocket(1797, 0, getBindAddress().getAddress());
                offChannelSocket = serverSocket.accept();

                LOGGER.info("Successfully connected to {}", offChannelSocket.getInetAddress());

                serverSocket.close();
            } else {
                LOGGER.info("Connecting to remote benchmark server");

                offChannelSocket = new Socket(getRemoteAddress().getAddress(), OFF_CHANNEL_PORT,
                    getBindAddress().getAddress(), OFF_CHANNEL_PORT);

                LOGGER.info("Successfully connected to {}", offChannelSocket.getInetAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Unable to setup off channel communication");

            return;
        }

        for(BenchmarkPhase phase : phases) {
            String phaseName = phase.getClass().getSimpleName();

            LOGGER.info("Running {}", phaseName);

            phase.runPhase();

            if(phase.getStatus() == Status.NOT_IMPLEMENTED) {
                LOGGER.warn("{} returned [{}] and is being skipped", phaseName, phase.getStatus());
                continue;
            }

            if(phase.getStatus() != Status.OK) {
                LOGGER.error("{} failed with status [{}]", phaseName, phase.getStatus());
                System.exit(1);
            }

            LOGGER.info("{} finished with status [{}]", phaseName, phase.getStatus());
        }

        try {
            offChannelSocket.close();
        } catch (IOException e) {
            LOGGER.warn("Unable to close off channel socket");
        }
    }

    protected abstract Status initialize();

    protected abstract Status serve(final InetSocketAddress bindAddress);

    protected abstract Status connect(final InetSocketAddress bindAddress, final InetSocketAddress serverAddress);

    protected abstract Status cleanup();

    protected abstract Status measureMessagingThroughput(BenchmarkMode mode, ThroughputMeasurement measurement);

    protected abstract Status measureRdmaThroughput(BenchmarkMode mode, RdmaMode rdmaMode, ThroughputMeasurement measurement);
}
