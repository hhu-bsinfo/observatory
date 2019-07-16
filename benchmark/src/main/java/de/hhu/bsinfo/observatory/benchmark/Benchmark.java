package de.hhu.bsinfo.observatory.benchmark;

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

    private boolean isServer;
    private InetSocketAddress bindAddress;
    private InetSocketAddress remoteAddress;

    private Socket offChannelSocket;

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

    boolean isServer() {
        return isServer;
    }

    void setBindAddress(final InetSocketAddress bindAddress) {
        this.bindAddress = bindAddress;
    }

    void setRemoteAddress(final InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    void setServer(boolean server) {
        isServer = server;
    }

    InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    Status setup() {
        try {
            if (isServer) {
                ServerSocket serverSocket = new ServerSocket(bindAddress.getPort(), 0, bindAddress.getAddress());
                offChannelSocket = serverSocket.accept();

                serverSocket.close();
            } else {
                offChannelSocket = new Socket(remoteAddress.getAddress(), remoteAddress.getPort(),
                        bindAddress.getAddress(), bindAddress.getPort());
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Unable to setup off channel communication");

            return Status.NETWORK_ERROR;
        }

        return Status.OK;
    }

    void sendSync() {
        try {
            LOGGER.info("Sending sync signal '{}' to client", SYNC_SIGNAL);
            new DataOutputStream(offChannelSocket.getOutputStream()).write(SYNC_SIGNAL.getBytes());
        } catch (IOException e) {
            LOGGER.warn("Unable to synchronize with remote benchmark");
        }
    }

    void receiveSync() {
        try {
            LOGGER.info("Waiting for sync signal from server");

            byte[] bytes = new byte[SYNC_SIGNAL.getBytes().length];
            new DataInputStream(offChannelSocket.getInputStream()).readFully(bytes);

            String received = new String(bytes);

            if (!received.equals(SYNC_SIGNAL)) {
                LOGGER.warn("Received invalid signal from server (Got '{}', Expected '{}'", received, SYNC_SIGNAL);
            }

            Thread.sleep(100);
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Unable to synchronize with remote benchmark");
        }
    }

    void synchronize() {
        if(isServer) {
            sendSync();
        } else {
            receiveSync();
        }
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
                System.exit(1);
            }

            LOGGER.info("{} finished with status [{}]", phaseName, status);
        }

        try {
            offChannelSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warn("Unable to close off channel communication");
        }
    }

    protected abstract Status initialize();

    protected abstract Status serve(final InetSocketAddress bindAddress);

    protected abstract Status connect(final InetSocketAddress bindAddress, final InetSocketAddress serverAddress);

    protected abstract Status prepare(final int operationSize);

    protected abstract Status fillReceiveQueue();

    protected abstract Status cleanup();

    protected abstract Status benchmarkMessagingSendThroughput(int operationCount);

    protected abstract Status benchmarkMessagingReceiveThroughput(int operationCount);

    protected abstract Status benchmarkRdmaThroughput(RdmaMode mode, int operationCount);
}
