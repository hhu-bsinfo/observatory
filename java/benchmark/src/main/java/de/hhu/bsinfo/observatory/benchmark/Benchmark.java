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

public abstract class Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(Benchmark.class);

    private static final String SYNC_SIGNAL = "SYNC";

    public enum Mode {
        SEND, RECEIVE
    }

    public enum RdmaMode {
        READ, WRITE
    }

    private String resultName;
    private int iterationNumber;

    private boolean isServer;
    private int connectionRetries;

    private InetSocketAddress bindAddress;
    private InetSocketAddress remoteAddress;

    private DetectorConfig detectorConfig;

    private Socket offChannelSocket;

    private IbFabric fabric;
    private IbPerfCounter perfCounter;

    private String resultPath;

    private final Map<String, String> parameters = new HashMap<>();

    private final List<BenchmarkPhase> phases = new ArrayList<>();

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

    protected Socket getOffChannelSocket() {
        return offChannelSocket;
    }

    public String getResultName() {
        return resultName;
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

    String getResultPath() {
        return resultPath;
    }

    int getIterationNumber() {
        return iterationNumber;
    }

    boolean measureOverhead() {
        return detectorConfig.isEnabled();
    }

    IbPerfCounter getPerfCounter() {
        return perfCounter;
    }

    public void setResultName(String name) {
        this.resultName = name;
    }

    void setServer(final boolean server) {
        isServer = server;
    }

    void setConnectionRetries(final int connectionRetries) {
        this.connectionRetries = connectionRetries;
    }

    void setBindAddress(final InetSocketAddress bindAddress) {
        this.bindAddress = bindAddress;
    }

    void setRemoteAddress(final InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    void setResultPath(final String resultPath) {
        this.resultPath = resultPath;
    }

    public void setIterationNumber(int iterationNumber) {
        this.iterationNumber = iterationNumber;
    }

    void setDetectorConfig(final DetectorConfig detectorConfig) {
        this.detectorConfig = detectorConfig;
    }

    Status setup() {
        if(detectorConfig.isEnabled()) {
            LOGGER.info("Initializing jDetector");

            try {
                fabric = new IbFabric(false, detectorConfig.getMode() == MeasurementMode.COMPAT);
            } catch (IbFileException | IbMadException | IbVerbsException | IbNetDiscException e) {
                LOGGER.error("Unable to initialize jDetector!", e);
                return Status.UNKNOWN_ERROR;
            }

            if (fabric.getNumNodes() == 0) {
                LOGGER.error("Fabric scanned by jDetector: 0 devices found!");
                return Status.UNKNOWN_ERROR;
            } else {
                LOGGER.info("Fabric scanned by jDetector: {} {} found", fabric.getNumNodes(), fabric.getNumNodes() == 1 ? "device was" : "devices were");
            }

            LOGGER.info("Measuring overhead on {}", fabric.getNodes()[detectorConfig.getDeviceNumber()].getDescription());

            perfCounter = fabric.getNodes()[detectorConfig.getDeviceNumber()];
        }

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


            for(int i = 0; i < connectionRetries && (offChannelSocket == null || !offChannelSocket.isConnected()); i++) {
                try {
                    Thread.sleep(1000);

                    offChannelSocket = new Socket(remoteAddress.getAddress(), remoteAddress.getPort(),
                            bindAddress.getAddress(), bindAddress.getPort());
                } catch (IOException e) {
                    LOGGER.warn("Connecting to server {} failed ({})", remoteAddress, e.getMessage());
                } catch (InterruptedException ignored) {}
            }

            if(offChannelSocket == null || !offChannelSocket.isConnected()) {
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

        if(!sendSync() || !receiveSync()) {
            LOGGER.error("Unable to synchronize with remote benchmark");
            return false;
        }

        LOGGER.info("Synchronized with remote benchmark");

        return true;
    }

    void executePhases() {
        for(BenchmarkPhase phase : phases) {
            String phaseName = phase.getClass().getSimpleName();

            LOGGER.info("Running {}", phaseName);

            Status status = phase.execute();

            if(status == Status.NOT_IMPLEMENTED) {
                LOGGER.warn("{} returned [{}] and is being skipped", phaseName, status);
                continue;
            }

            if(status != Status.OK) {
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

        if(detectorConfig.isEnabled()) {
            fabric.close();
        }
    }

    protected abstract Status initialize();

    protected abstract Status serve(final InetSocketAddress bindAddress);

    protected abstract Status connect(final InetSocketAddress bindAddress, final InetSocketAddress serverAddress);

    protected abstract Status prepare(final int operationSize);

    protected abstract Status cleanup();

    protected abstract Status fillReceiveQueue();

    protected abstract Status sendMultipleMessages(int messageCount);

    protected abstract Status receiveMultipleMessage(int messageCount);

    protected abstract Status performMultipleRdmaOperations(RdmaMode mode, int operationCount);

    protected abstract Status sendSingleMessage();

    protected abstract Status performSingleRdmaOperation(RdmaMode mode);

    protected abstract Status performPingPongIterationServer();

    protected abstract Status performPingPongIterationClient();
}
