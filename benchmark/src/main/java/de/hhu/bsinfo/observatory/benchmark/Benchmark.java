package de.hhu.bsinfo.observatory.benchmark;

import java.util.HashMap;
import java.util.Map;

public abstract class Benchmark {

    private Map<String, String> parameters = new HashMap<>();

    private final InitializationPhase initializationPhase = new InitializationPhase(this);

    public void setParameter(final String key, final String value) {
        parameters.put(key, value);
    }

    protected String getParameter(String key) {
        return parameters.get(key);
    }

    protected Map<String, String> getParameters() {
        return parameters;
    }

    protected abstract Status initialize();

    public InitializationPhase getInitializationPhase() {
        return initializationPhase;
    }
}
