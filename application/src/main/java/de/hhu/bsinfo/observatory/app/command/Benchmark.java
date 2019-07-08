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

    private static final int DEFAULT_SERVER_PORT = 2998;

    @CommandLine.Option(
        names = {"-c", "--config"},
        description = "Path to the config JSON file. If empty, observatory will try to load 'config.json' from it's resource folder.")
    private String configPath;

    @CommandLine.Option(
        names = "--server",
        description = "Runs this instance in server mode.")
    private boolean isServer;

    @CommandLine.Option(
        names = {"-p", "--port"},
        description = "The port the server will listen on.")
    private int port = DEFAULT_SERVER_PORT;

    public Void call() throws Exception {
        LOGGER.info("Loading configuration");

        BenchmarkConfig config;

        if(configPath == null) {
            config = JsonResourceLoader.loadJsonObjectFromResource("config.json", BenchmarkConfig.class);
        } else {
            config = JsonResourceLoader.loadJsonObjectFromFile(configPath, BenchmarkConfig.class);
        }

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
