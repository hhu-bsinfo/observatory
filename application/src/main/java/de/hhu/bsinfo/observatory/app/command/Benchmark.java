package de.hhu.bsinfo.observatory.app.command;

import de.hhu.bsinfo.observatory.Observatory;
import de.hhu.bsinfo.observatory.app.util.JsonResourceLoader;
import de.hhu.bsinfo.observatory.app.config.BenchmarkConfig;
import de.hhu.bsinfo.observatory.app.config.BenchmarkParameter;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
    name = "benchmark",
    description = "Executes a benchmark.%n",
    showDefaultValues = true,
    separator = " ")
public class Benchmark implements Callable<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Benchmark.class);

    public Void call() throws Exception {
        LOGGER.info("Loading configuration");

        BenchmarkConfig config = JsonResourceLoader.loadJsonObject("config.json", BenchmarkConfig.class);

        LOGGER.info("Creating benchmark instance");

        new Observatory(instantiateBenchmark(config)).start();

        return null;
    }

    private static de.hhu.bsinfo.observatory.benchmark.Benchmark instantiateBenchmark(BenchmarkConfig config) {
        try {
            Class<?> clazz = Observatory.class.getClassLoader().loadClass(config.getClassName());
            de.hhu.bsinfo.observatory.benchmark.Benchmark benchmark = (de.hhu.bsinfo.observatory.benchmark.Benchmark) clazz.getConstructor().newInstance();

            for(BenchmarkParameter parameter : config.getParameters()) {
                benchmark.setParameter(parameter.getKey(), parameter.getValue());
            }

            return benchmark;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
