package de.hhu.bsinfo.observatory.app;

import de.hhu.bsinfo.observatory.app.util.InetSocketAddressConverter;
import de.hhu.bsinfo.observatory.benchmark.Observatory;
import de.hhu.bsinfo.observatory.app.util.JsonResourceLoader;
import de.hhu.bsinfo.observatory.benchmark.config.BenchmarkConfig;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
    name = "observatory",
    description = "",
    showDefaultValues = true,
    separator = " ")
public class Application implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final int DEFAULT_SERVER_PORT = 2998;

    @CommandLine.Option(
        names = {"-c", "--config"},
        description = "Path to the config JSON file. If empty, observatory will try to load './config.json'")
    private String configPath = "./config.json";

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "Output path for the result files. If empty, observatory will save results in './result/'.")
    private String resultPath = "./result/";

    @CommandLine.Option(
        names = {"-s", "--server"},
        description = "Runs this instance in server mode.")
    private boolean isServer = false;

    @CommandLine.Option(
            names = {"-t", "--retries"},
            description = "The amount of connection attempts.")
    private int connectionRetries = 10;

    @CommandLine.Option(
        names = {"-a", "--address"},
        description = "The address to bind to.")
    private InetSocketAddress bindAddress = new InetSocketAddress(DEFAULT_SERVER_PORT);

    @CommandLine.Option(
        names = {"-r", "--remote"},
        description = "The address to connect to.")
    private InetSocketAddress remoteAddress;

    public void run() {
        if(!isServer && remoteAddress == null) {
            LOGGER.error("Please specify the server address");
            return;
        }

        if(connectionRetries <= 0) {
            LOGGER.error("The amount of connection retries must be at greater than zero");
            return;
        }

        LOGGER.info("Loading configuration");

        BenchmarkConfig config;

        try {
            config = JsonResourceLoader.loadJsonObjectFromFile(configPath, BenchmarkConfig.class);
        } catch (IOException e) {
            LOGGER.error("Unable to parse configuration file", e);
            return;
        }

        if(!isServer) {
            bindAddress = new InetSocketAddress(bindAddress.getAddress(), DEFAULT_SERVER_PORT);
        }

        LOGGER.info("Creating observatory instance");

        new Observatory(config, resultPath, isServer, connectionRetries, bindAddress, remoteAddress).start();
    }

    public static void main(String... args) {
        Observatory.printBanner();

        CommandLine cli = new CommandLine(new Application());
        cli.registerConverter(InetSocketAddress.class, new InetSocketAddressConverter(2998));
        cli.setCaseInsensitiveEnumValuesAllowed(true);
        int exitCode = cli.execute(args);

        System.exit(exitCode);
    }
}
