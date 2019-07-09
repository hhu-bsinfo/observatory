package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.BenchmarkMode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import de.hhu.bsinfo.observatory.benchmark.result.ThroughputMeasurement;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(Benchmark.class);

    private boolean isServer;
    private InetSocketAddress address;

    private final Map<String, String> parameters = new HashMap<>();

    private final List<BenchmarkPhase> phases = new ArrayList<>();

    void addBenchmarkPhase(BenchmarkPhase phase) {
        phases.add(phase);
    }

    void setParameter(final String key, final String value) {
        parameters.put(key, value);
    }

    protected String getParameter(String key) {
        return parameters.get(key);
    }

    void setServer(final boolean isServer) {
        this.isServer = isServer;
    }

    void setAddress(final InetSocketAddress address) {
        this.address = address;
    }

    boolean isServer() {
        return isServer;
    }

    InetSocketAddress getAddress() {
        return address;
    }

    void executePhases() {
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
    }

    protected abstract Status initialize();

    protected abstract Status serve(final InetSocketAddress bindAddress);

    protected abstract Status connect(final InetSocketAddress serverAddress);

    protected abstract Status cleanup();

    protected abstract Status measureMessagingThroughput(BenchmarkMode mode, ThroughputMeasurement measurement);
}
