package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.jdetector.lib.IbFabric;
import de.hhu.bsinfo.jdetector.lib.IbPerfCounter;
import de.hhu.bsinfo.jdetector.lib.exception.IbFileException;
import de.hhu.bsinfo.jdetector.lib.exception.IbMadException;
import de.hhu.bsinfo.jdetector.lib.exception.IbNetDiscException;
import de.hhu.bsinfo.jdetector.lib.exception.IbVerbsException;
import de.hhu.bsinfo.observatory.benchmark.config.DetectorConfig;
import de.hhu.bsinfo.observatory.benchmark.config.DetectorConfig.MeasurementMode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

public abstract class Connection {

    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

    private static final String SYNC_SIGNAL = "SYNC";

    public enum Mode {
        SEND, RECEIVE
    }

    public enum RdmaMode {
        READ, WRITE
    }

    private Benchmark benchmark;

    private final boolean isServer;
    private final int connectionRetries;

    private final InetSocketAddress bindAddress;
    private final InetSocketAddress remoteAddress;
    private final IbPerfCounter perfCounter;

    private int iterationNumber;
    private Socket offChannelSocket;

    public Connection(Benchmark benchmark, boolean isServer, int connectionRetries, InetSocketAddress bindAddress, InetSocketAddress remoteAddress, IbPerfCounter perfCounter) {
        this.benchmark = benchmark;
        this.isServer = isServer;
        this.connectionRetries = connectionRetries;
        this.bindAddress = bindAddress;
        this.remoteAddress = remoteAddress;
        this.perfCounter = perfCounter;
    }

    protected String getParameter(String key, String defaultValue) {
        return benchmark.getParameter(key, defaultValue);
    }

    protected byte getParameter(String key, byte defaultValue) {
        return benchmark.getParameter(key, defaultValue);
    }

    protected short getParameter(String key, short defaultValue) {
        return benchmark.getParameter(key, defaultValue);
    }

    protected int getParameter(String key, int defaultValue) {
        return benchmark.getParameter(key, defaultValue);
    }

    protected long getParameter(String key, long defaultValue) {
        return benchmark.getParameter(key, defaultValue);
    }

    protected void setBenchmark(Benchmark benchmark) {
        this.benchmark = benchmark;
    }

    protected Socket getOffChannelSocket() {
        return offChannelSocket;
    }

    boolean isServer() {
        return isServer;
    }

    int getConnectionRetries() {
        return connectionRetries;
    }

    InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    int getIterationNumber() {
        return iterationNumber;
    }

    boolean measureOverhead() {
        return perfCounter != null;
    }

    IbPerfCounter getPerfCounter() {
        return perfCounter;
    }

    Status setup() {
        LOGGER.info("Setting up connection for off channel communication");

        if (isServer) {
            LOGGER.info("Listening on address {}", bindAddress.toString());

            try {
                ServerSocket serverSocket = new ServerSocket(bindAddress.getPort(), 0, bindAddress.getAddress());
                offChannelSocket = serverSocket.accept();

                serverSocket.close();
            } catch (IOException e) {
                LOGGER.error("Setting up off channel communication failed", e);

                return Status.NETWORK_ERROR;
            }
        } else {
            LOGGER.info("Connecting to server {}", remoteAddress.toString());

            for (int i = 0; i < connectionRetries && (offChannelSocket == null || !offChannelSocket.isConnected()); i++) {
                try {
                    Thread.sleep(1000);

                    offChannelSocket = new Socket(remoteAddress.getAddress(), remoteAddress.getPort(),
                            bindAddress.getAddress(), bindAddress.getPort());
                } catch (IOException e) {
                    LOGGER.warn("Connecting to server {} failed ({})", remoteAddress, e.getMessage());
                } catch (InterruptedException ignored) {}
            }

            if (offChannelSocket == null || !offChannelSocket.isConnected()) {
                LOGGER.error("Setting up off channel communication failed (Retry amount exceeded)");
                return Status.NETWORK_ERROR;
            }
        }

        LOGGER.info("Successfully connected to {}", offChannelSocket.getRemoteSocketAddress());

        return Status.OK;
    }

    private boolean sendSync() {
        try {
            new DataOutputStream(offChannelSocket.getOutputStream()).write(SYNC_SIGNAL.getBytes());

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean receiveSync() {
        try {
            byte[] bytes = new byte[SYNC_SIGNAL.getBytes().length];
            new DataInputStream(offChannelSocket.getInputStream()).readFully(bytes);

            String received = new String(bytes);

            if (!received.equals(SYNC_SIGNAL)) {
                LOGGER.error("Received invalid signal (Got '{}', Expected '{}')", received, SYNC_SIGNAL);
                return false;
            }

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    boolean synchronize() {
        LOGGER.info("Synchronizing with remote benchmark");

        if (!sendSync() || !receiveSync()) {
            LOGGER.error("Unable to synchronize with remote benchmark");
            return false;
        }

        LOGGER.info("Synchronized with remote benchmark");

        return true;
    }

    void executePhases() {
        for (BenchmarkPhase phase : benchmark.getPhases()) {
            String phaseName = phase.getClass().getSimpleName();

            LOGGER.info("Running {}", phaseName);

            if (!synchronize()) {
                System.exit(Status.SYNC_ERROR.ordinal());
            }

            Status status = phase.execute();

            if (status == Status.NOT_IMPLEMENTED) {
                LOGGER.warn("{} returned [{}] and is being skipped", phaseName, status);
                continue;
            }

            if (status != Status.OK) {
                LOGGER.error("{} failed with status [{}]", phaseName, status);
                System.exit(status.ordinal());
            }

            LOGGER.info("{} finished with status [{}]", phaseName, status);
        }

        try {
            offChannelSocket.close();
        } catch (IOException e) {
            LOGGER.error("Closing off channel communication failed", e);
        }
    }

    protected abstract Status initialize();

    protected abstract Status serve(final InetSocketAddress bindAddress);

    protected abstract Status connect(final InetSocketAddress bindAddress, final InetSocketAddress serverAddress);

    protected abstract Status prepare(final int operationSize, int operationCount);

    protected abstract Status cleanup();

    protected abstract Status fillReceiveQueue();

    protected abstract Status sendMultipleMessages(int messageCount);

    protected abstract Status receiveMultipleMessages(int messageCount);

    protected abstract Status performMultipleRdmaOperations(RdmaMode mode, int operationCount);

    protected abstract Status sendSingleMessage();

    protected abstract Status performSingleRdmaOperation(RdmaMode mode);

    protected abstract Status performPingPongIterationServer();

    protected abstract Status performPingPongIterationClient();
}
