package de.hhu.bsinfo.infinibench.example;

import de.hhu.bsinfo.infinibench.benchmark.Benchmark;
import de.hhu.bsinfo.infinibench.benchmark.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleBenchmark extends Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleBenchmark.class);

    @Override
    protected Status initialize() {
        LOGGER.info("Parameters: {}", getParameters().toString());

        return Status.OK;
    }
}
