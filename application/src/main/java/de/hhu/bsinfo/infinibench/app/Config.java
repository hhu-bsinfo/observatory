package de.hhu.bsinfo.infinibench.app;

class Config {

    private final String[] benchmarks;

    Config(String... benchmarks) {
        this.benchmarks = benchmarks;
    }

    String[] getBenchmarks() {
        return benchmarks;
    }
}
