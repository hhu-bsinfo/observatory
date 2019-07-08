package de.hhu.bsinfo.infinibench.app;

import de.hhu.bsinfo.infinibench.core.Benchmark;
import de.hhu.bsinfo.infinibench.core.InfiniBench;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        InfiniBench.printBanner();

        LOGGER.info("Loading configuration");

        Config config = JsonResourceLoader.loadJsonObject("config.json", Config.class);

        Set<Benchmark> benchmarks = Arrays.stream(config.getBenchmarks())
            .map(Application::instantiateBenchmark)
            .collect(Collectors.toSet());

        for(Benchmark benchmark : benchmarks) {
            benchmark.initialize();
        }
    }

    private static Benchmark instantiateBenchmark(String className) {
        try {
            Class<?> clazz = Application.class.getClassLoader().loadClass(className);
            return (Benchmark) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
