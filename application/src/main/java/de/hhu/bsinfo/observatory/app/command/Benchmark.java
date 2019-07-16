package de.hhu.bsinfo.observatory.app.command;

import de.hhu.bsinfo.observatory.app.Application;
import de.hhu.bsinfo.observatory.benchmark.Observatory;
import de.hhu.bsinfo.observatory.app.util.JsonResourceLoader;
import de.hhu.bsinfo.observatory.benchmark.config.BenchmarkConfig;
import java.net.InetSocketAddress;
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
        if(!isServer && remoteAddress == null) {
            LOGGER.error("Please specify the server address");
            return null;
        }

        LOGGER.info("Loading configuration");

        BenchmarkConfig config;

        if(configPath == null) {
            config = JsonResourceLoader.loadJsonObjectFromResource("config.json", BenchmarkConfig.class);
        } else {
            config = JsonResourceLoader.loadJsonObjectFromFile(configPath, BenchmarkConfig.class);
        }

        if(!isServer) {
            bindAddress = new InetSocketAddress(bindAddress.getAddress(), 0);
        }

        LOGGER.info("Creating observatory instance");

        new Observatory(config, isServer, bindAddress, remoteAddress).start();

        return null;
    }
}
