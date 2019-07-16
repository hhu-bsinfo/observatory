package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.Status;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(Benchmark.class);

    public enum Mode {
        SEND, RECEIVE
    }

    private boolean isServer;
    private InetSocketAddress bindAddress;
    private InetSocketAddress remoteAddress;

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
    }

    protected abstract Status initialize();

    protected abstract Status serve(final InetSocketAddress bindAddress);

    protected abstract Status connect(final InetSocketAddress bindAddress, final InetSocketAddress serverAddress);

    protected abstract Status prepare(final int operationSize);

    protected abstract Status fillReceiveQueue();

    protected abstract Status cleanup();

    protected abstract Status benchmarkMessagingSendThroughput(int operationCount);

    protected abstract Status benchmarkMessagingReceiveThroughput(int operationCount);
}
