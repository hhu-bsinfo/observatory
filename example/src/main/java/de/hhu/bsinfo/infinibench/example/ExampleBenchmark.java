package de.hhu.bsinfo.infinibench.example;

import de.hhu.bsinfo.infinibench.core.Benchmark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleBenchmark implements Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleBenchmark.class);

    @Override
    public void initialize() {
        LOGGER.info("Initializing benchmark");
    }
}
