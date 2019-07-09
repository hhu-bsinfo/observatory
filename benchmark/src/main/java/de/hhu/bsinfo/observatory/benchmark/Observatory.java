package de.hhu.bsinfo.observatory.benchmark;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.stream.Collectors;

import de.hhu.bsinfo.observatory.generated.BuildConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Observatory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Observatory.class);

    private final Benchmark benchmark;

    public Observatory(Benchmark benchmark, Map<String, String> benchmarkParameters, boolean isServer, InetSocketAddress address) {
        this.benchmark = benchmark;

        benchmarkParameters.forEach(benchmark::setParameter);
        benchmark.setServer(isServer);
        benchmark.setAddress(address);
    }

    public void start() throws Exception {
        LOGGER.info("Executing benchmark '{}'", benchmark.getClass().getSimpleName());

        runPhase(benchmark.getInitializationPhase());
        runPhase(benchmark.getConnectionPhase());
        runPhase(benchmark.getCleanupPhase());
    }

    private void runPhase(BenchmarkPhase phase) {
        String phaseName = phase.getClass().getSimpleName();

        LOGGER.info("Running {}", phaseName);

        phase.run();

        if(phase.getStatus() == Status.NOT_IMPLEMENTED) {
            LOGGER.warn("{} returned [{}] and is being skipped", phaseName, phase.getStatus());
            return;
        }

        if(phase.getStatus() != Status.OK) {
            LOGGER.error("{} failed with status [{}]", phaseName, phase.getStatus());
            System.exit(1);
        }

        LOGGER.info("{} finished with status [{}]", phaseName, phase.getStatus());
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
