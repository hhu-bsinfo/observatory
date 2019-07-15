package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.config.Config;
import de.hhu.bsinfo.observatory.benchmark.config.Operation;
import de.hhu.bsinfo.observatory.benchmark.config.Phase;
import de.hhu.bsinfo.observatory.benchmark.config.Phase.Mode;
import de.hhu.bsinfo.observatory.benchmark.result.BenchmarkMode;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import de.hhu.bsinfo.observatory.generated.BuildConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Observatory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Observatory.class);

    private final Benchmark benchmark;

    public Observatory(Benchmark benchmark, Config config, boolean isServer, InetSocketAddress bindAddress, InetSocketAddress remoteAddress) {
        this.benchmark = benchmark;

        Arrays.stream(config.getParameters()).forEach(parameter -> benchmark.setParameter(parameter.getKey(), parameter.getValue()));

        benchmark.setServer(isServer);
        benchmark.setBindAddress(bindAddress);
        benchmark.setRemoteAddress(remoteAddress);

        benchmark.addBenchmarkPhase(new InitializationPhase(benchmark));
        benchmark.addBenchmarkPhase(new ConnectionPhase(benchmark));

        for(Phase phaseConfig : config.getPhases()) {
            Map<Integer, Integer> measurementOptions = Arrays.stream(phaseConfig.getOperations())
                .collect(Collectors.toMap(Operation::getSize, Operation::getCount));

            for(Mode mode : phaseConfig.getModes()) {
                if(mode == Mode.UNIDIRECTIONAL) {
                    MeasurementPhase phase = instantiateMeasurementPhase("de.hhu.bsinfo.observatory.benchmark." + phaseConfig.getName(),
                        isServer ? BenchmarkMode.SEND : BenchmarkMode.RECEIVE, measurementOptions);

                    if(phase != null) {
                        benchmark.addBenchmarkPhase(phase);
                    }
                } else if(mode == Mode.BIDIRECTIONAL) {
                    ThroughputPhase sendPhase = (ThroughputPhase) instantiateMeasurementPhase(
                        "de.hhu.bsinfo.observatory.benchmark." + phaseConfig.getName(), BenchmarkMode.SEND, measurementOptions);
                    ThroughputPhase receivePhase = (ThroughputPhase) instantiateMeasurementPhase(
                        "de.hhu.bsinfo.observatory.benchmark." + phaseConfig.getName(), BenchmarkMode.RECEIVE, measurementOptions);

                    if(sendPhase != null && receivePhase != null) {
                        benchmark.addBenchmarkPhase(new BidirectionalThroughputPhase(sendPhase, receivePhase, measurementOptions));
                    }
                }
            }
        }

        benchmark.addBenchmarkPhase(new CleanupPhase(benchmark));
    }

    public void start() {
        LOGGER.info("Executing benchmark '{}'", benchmark.getClass().getSimpleName());

        benchmark.executePhases();
    }

    @SuppressWarnings("unchecked")
    private MeasurementPhase instantiateMeasurementPhase(String className, BenchmarkMode mode, Map<Integer, Integer> measurementOptions) {
        try {
            Class<? extends MeasurementPhase> clazz = (Class<? extends MeasurementPhase>) Class.forName(className);

            return clazz.getDeclaredConstructor(Benchmark.class, BenchmarkMode.class, Map.class).newInstance(benchmark, mode, measurementOptions);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.warn("Unable to create benchmark phase of type '{}'", className);
        }

        return null;
    }

    public static void printBanner() {
        InputStream inputStream = Observatory.class.getClassLoader().getResourceAsStream("banner.txt");

        if (inputStream == null) {
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String banner = reader.lines().collect(Collectors.joining(System.lineSeparator()));

        System.out.print("\n");
        System.out.printf(banner, BuildConfig.VERSION, BuildConfig.BUILD_DATE, BuildConfig.GIT_BRANCH, BuildConfig.GIT_COMMIT);
        System.out.print("\n\n");
    }
}
