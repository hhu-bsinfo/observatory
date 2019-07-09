package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.BenchmarkMode;
import de.hhu.bsinfo.observatory.benchmark.result.Measurement;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.hhu.bsinfo.observatory.generated.BuildConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Observatory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Observatory.class);

    private final Benchmark benchmark;

    public Observatory(Benchmark benchmark, Map<String, String> benchmarkParameters, Map<Class<? extends MeasurementPhase>, Map<Integer, Integer>> benchmarkPhases, boolean isServer, InetSocketAddress address) {
        this.benchmark = benchmark;

        benchmarkParameters.forEach(benchmark::setParameter);
        benchmark.setServer(isServer);
        benchmark.setAddress(address);

        benchmark.addBenchmarkPhase(new InitializationPhase(benchmark));
        benchmark.addBenchmarkPhase(new ConnectionPhase(benchmark));

        for(Entry<Class<? extends MeasurementPhase>, Map<Integer, Integer>> entry : benchmarkPhases.entrySet()) {
            MeasurementPhase phase = instantiateMeasurementPhase(entry.getKey(), isServer ? BenchmarkMode.SEND : BenchmarkMode.RECEIVE, entry.getValue());

            if(phase != null) {
                benchmark.addBenchmarkPhase(phase);
            }
        }

        benchmark.addBenchmarkPhase(new CleanupPhase(benchmark));
    }

    public void start() {
        LOGGER.info("Executing benchmark '{}'", benchmark.getClass().getSimpleName());

        benchmark.executePhases();
    }

    private MeasurementPhase instantiateMeasurementPhase(Class<? extends MeasurementPhase> clazz, BenchmarkMode mode, Map<Integer, Integer> measurementOptions) {
        try {
            return clazz.getDeclaredConstructor(Benchmark.class, BenchmarkMode.class, Map.class).newInstance(benchmark, mode, measurementOptions);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.warn("Unable to create benchmark phase of type '{}'", clazz.getSimpleName());
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
