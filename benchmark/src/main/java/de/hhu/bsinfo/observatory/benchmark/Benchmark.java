package de.hhu.bsinfo.observatory.benchmark;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public abstract class Benchmark {

    private boolean isServer;
    private InetSocketAddress address;

    private final Map<String, String> parameters = new HashMap<>();

    private final InitializationPhase initializationPhase = new InitializationPhase(this);
    private final ConnectionPhase connectionPhase = new ConnectionPhase(this);
    private final CleanupPhase cleanupPhase = new CleanupPhase(this);

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

    InitializationPhase getInitializationPhase() {
        return initializationPhase;
    }

    ConnectionPhase getConnectionPhase() {
        return connectionPhase;
    }

    CleanupPhase getCleanupPhase() {
        return cleanupPhase;
    }

    protected abstract Status initialize();

    protected abstract Status serve(final InetSocketAddress bindAddress);

    protected abstract Status connect(final InetSocketAddress serverAddress);

    protected abstract Status cleanup();
}
