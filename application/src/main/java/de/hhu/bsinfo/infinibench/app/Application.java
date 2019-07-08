package de.hhu.bsinfo.infinibench.app;

import de.hhu.bsinfo.infinibench.InfiniBench;
import de.hhu.bsinfo.infinibench.benchmark.Benchmark;
import de.hhu.bsinfo.infinibench.app.config.BenchmarkConfig;
import de.hhu.bsinfo.infinibench.app.config.BenchmarkParameter;
import de.hhu.bsinfo.infinibench.app.config.RootConfig;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        InfiniBench.printBanner();

        LOGGER.info("Loading configuration");

        RootConfig config = JsonResourceLoader.loadJsonObject("config.json", RootConfig.class);

        Benchmark[] benchmarks = Arrays.stream(config.getBenchmarks())
            .map(Application::instantiateBenchmark)
            .distinct()
            .toArray(Benchmark[]::new);

        new InfiniBench(benchmarks).start();
    }

    private static Benchmark instantiateBenchmark(BenchmarkConfig config) {
        try {
            Class<?> clazz = InfiniBench.class.getClassLoader().loadClass(config.getClassName());
            Benchmark benchmark = (Benchmark) clazz.getConstructor().newInstance();

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
