package de.hhu.bsinfo.observatory.app.command;

import de.hhu.bsinfo.observatory.app.config.Operation;
import de.hhu.bsinfo.observatory.benchmark.BenchmarkPhase;
import de.hhu.bsinfo.observatory.benchmark.MeasurementPhase;
import de.hhu.bsinfo.observatory.benchmark.Observatory;
import de.hhu.bsinfo.observatory.app.util.JsonResourceLoader;
import de.hhu.bsinfo.observatory.app.config.Config;
import de.hhu.bsinfo.observatory.app.config.Parameter;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(Benchmark.class);

    private static final int DEFAULT_SERVER_PORT = 2998;

    @CommandLine.Option(
        names = {"-c", "--config"},
        description = "Path to the config JSON file. If empty, observatory will try to load 'config.json' from it's resource folder.")
    private String configPath;

    @CommandLine.Option(
        names = {"-s", "--server"},
        description = "Runs this instance in server mode.")
    private boolean isServer;

    @CommandLine.Option(
        names = {"-a", "--address"},
        description = "The address to listen on or connect to.")
    private InetSocketAddress address = new InetSocketAddress(DEFAULT_SERVER_PORT);

    public Void call() throws Exception {
        LOGGER.info("Loading configuration");

        Config config;

        if(configPath == null) {
            config = JsonResourceLoader.loadJsonObjectFromResource("config.json", Config.class);
        } else {
            config = JsonResourceLoader.loadJsonObjectFromFile(configPath, Config.class);
        }

        LOGGER.info("Creating benchmark instance");

        Map<String, String> parameters = Arrays.stream(config.getParameters())
            .collect(Collectors.toMap(Parameter::getKey, Parameter::getValue));

        Map<Class<? extends MeasurementPhase>, Map<Integer, Integer>> phases = Arrays.stream(config.getPhases())
            .collect(Collectors.toMap(phase -> getPhaseClass(phase.getName()),
                phase -> Arrays.stream(phase.getOperations())
                    .collect(Collectors.toMap(Operation::getSize, Operation::getCount))));

        new Observatory(instantiateBenchmark(config), parameters, phases, isServer, address).start();

        return null;
    }

    private static de.hhu.bsinfo.observatory.benchmark.Benchmark instantiateBenchmark(Config config) throws Exception {
        Class<?> clazz = Observatory.class.getClassLoader().loadClass(config.getClassName());
        return (de.hhu.bsinfo.observatory.benchmark.Benchmark) clazz.getConstructor().newInstance();
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends MeasurementPhase> getPhaseClass(String name) {
        try {
            return (Class<? extends MeasurementPhase>) Class.forName("de.hhu.bsinfo.observatory.benchmark." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();

            LOGGER.error("Unable to find class for benchmark phase '{}'", name);
            System.exit(1);
        }

        return null;
    }
}
