package de.hhu.bsinfo.observatory.app.command;

import de.hhu.bsinfo.observatory.app.Application;
import de.hhu.bsinfo.observatory.benchmark.config.Operation;
import de.hhu.bsinfo.observatory.benchmark.MeasurementPhase;
import de.hhu.bsinfo.observatory.benchmark.Observatory;
import de.hhu.bsinfo.observatory.app.util.JsonResourceLoader;
import de.hhu.bsinfo.observatory.benchmark.config.Config;
import de.hhu.bsinfo.observatory.benchmark.config.Parameter;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
    name = "benchmark",
    description = "Executes a benchmark.%n",
    showDefaultValues = true,
    separator = " ")
public class Benchmark implements Callable<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private static final int DEFAULT_SERVER_PORT = 2998;

    @CommandLine.Option(
        names = {"-c", "--config"},
        description = "Path to the config JSON file. If empty, observatory will try to load 'config.json' from it's resource folder.")
    private String configPath;

    @CommandLine.Option(
        names = {"-s", "--server"},
        description = "Runs this instance in server mode.")
    private boolean isServer = false;

    @CommandLine.Option(
        names = {"-a", "--address"},
        description = "The address to bind to.")
    private InetSocketAddress bindAddress = new InetSocketAddress(DEFAULT_SERVER_PORT);

    @CommandLine.Option(
        names = {"-r", "--remote"},
        description = "The address to connect to.")
    private InetSocketAddress remoteAddress;

    public Void call() throws Exception {
        LOGGER.info("Loading configuration");

        Config config;

        if(configPath == null) {
            config = JsonResourceLoader.loadJsonObjectFromResource("config.json", Config.class);
        } else {
            config = JsonResourceLoader.loadJsonObjectFromFile(configPath, Config.class);
        }

        LOGGER.info("Creating benchmark instance");

        new Observatory(instantiateBenchmark(config.getClassName()), config, isServer, bindAddress, remoteAddress).start();

        return null;
    }

    private static de.hhu.bsinfo.observatory.benchmark.Benchmark instantiateBenchmark(String className) throws Exception {
        Class<?> clazz = Observatory.class.getClassLoader().loadClass(className);
        return (de.hhu.bsinfo.observatory.benchmark.Benchmark) clazz.getConstructor().newInstance();
    }
}
