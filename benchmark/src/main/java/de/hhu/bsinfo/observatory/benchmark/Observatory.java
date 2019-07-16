package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.Benchmark.Mode;
import de.hhu.bsinfo.observatory.benchmark.config.BenchmarkConfig;
import de.hhu.bsinfo.observatory.benchmark.config.OperationConfig;
import de.hhu.bsinfo.observatory.benchmark.config.IterationConfig;
import de.hhu.bsinfo.observatory.benchmark.config.OperationConfig.OperationMode;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.hhu.bsinfo.observatory.generated.BuildConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Observatory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Observatory.class);

    private final List<Benchmark> benchmarks = new ArrayList<>();

    public Observatory(BenchmarkConfig config, boolean isServer, InetSocketAddress bindAddress, InetSocketAddress remoteAddress) {
        for (OperationConfig operationConfig : config.getOperations()) {
            for(OperationMode mode : operationConfig.getModes()) {
                String operationClassName =
                        "de.hhu.bsinfo.observatory.benchmark." + operationConfig.getName() + "Operation";

                for (IterationConfig iterationConfig : operationConfig.getIterations()) {
                    Benchmark benchmark = instantiateBenchmark(config.getClassName());

                    if (benchmark == null) {
                        return;
                    }

                    Operation operation = null;

                    if (mode == OperationMode.UNIDIRECTIONAL) {
                        operation = instantiateOperation(operationClassName, benchmark,
                                isServer ? Mode.SEND : Mode.RECEIVE, iterationConfig.getCount(),
                                iterationConfig.getSize());
                    } else if (mode == OperationMode.BIDIRECTIONAL) {
                        Operation sendOperation = instantiateOperation(operationClassName, benchmark,
                                Mode.SEND, iterationConfig.getCount(), iterationConfig.getSize());

                        Operation receiveOperation = instantiateOperation(operationClassName, benchmark,
                                Mode.RECEIVE, iterationConfig.getCount(), iterationConfig.getSize());

                        if(sendOperation == null || receiveOperation == null) {
                            return;
                        }

                        if(!(sendOperation instanceof ThroughputOperation) || !(receiveOperation instanceof ThroughputOperation)) {
                            LOGGER.error("Invalid configuration: Only throughput opertations may be executed bidirectionally");
                            return;
                        }

                        operation = new BidirectionalThroughputOperation((ThroughputOperation) sendOperation, (ThroughputOperation) receiveOperation);
                    }

                    if (operation == null) {
                        return;
                    }

                    Arrays.stream(config.getParameters())
                            .forEach(parameter -> benchmark.setParameter(parameter.getKey(), parameter.getValue()));

                    benchmark.setServer(isServer);
                    benchmark.setBindAddress(bindAddress);
                    benchmark.setRemoteAddress(remoteAddress);

                    benchmark.addBenchmarkPhase(new InitializationPhase(benchmark));
                    benchmark.addBenchmarkPhase(new ConnectionPhase(benchmark));
                    benchmark.addBenchmarkPhase(new PreparationPhase(benchmark, isServer ? Mode.SEND : Mode.RECEIVE, iterationConfig.getSize()));
                    benchmark.addBenchmarkPhase(new FillReceiveQueuePhase(benchmark));
                    benchmark.addBenchmarkPhase(new WarmupPhase(benchmark));
                    benchmark.addBenchmarkPhase(new FillReceiveQueuePhase(benchmark));
                    benchmark.addBenchmarkPhase(new OperationPhase(benchmark, operation));
                    benchmark.addBenchmarkPhase(new CleanupPhase(benchmark));

                    benchmarks.add(benchmark);
                }
            }
        }
    }

        /*for(Measurement phaseConfig : config.getPhases()) {
            Map<Integer, Integer> measurementOptions = Arrays.stream(phaseConfig.getOperations())
                .collect(Collectors.toMap(Operation::getSize, Operation::getCount));

            for(Mode mode : phaseConfig.getModes()) {
                if(mode == Mode.UNIDIRECTIONAL) {
                    BenchmarkPhase phase = instantiateMeasurementPhase("de.hhu.bsinfo.observatory.benchmark." + phaseConfig.getName(),
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
        }*/

    public void start() throws InterruptedException {
        for(Benchmark benchmark : benchmarks) {
            LOGGER.info("Executing benchmark '{}'", benchmark.getClass().getSimpleName());

            // Wait for server to be ready to accept incoming connections
            if(!benchmark.isServer()) {
                Thread.sleep(100);
            }

            benchmark.executePhases();
        }
    }

    private static de.hhu.bsinfo.observatory.benchmark.Benchmark instantiateBenchmark(String className) {
        try {
            Class<?> clazz = Observatory.class.getClassLoader().loadClass(className);
            return (de.hhu.bsinfo.observatory.benchmark.Benchmark) clazz.getConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("Unable to create benchmark of type '{}'", className);

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Operation instantiateOperation(String className, Benchmark benchmark, Mode mode, int operationCount, int operationSize) {
        try {
            Class<? extends Operation> clazz = (Class<? extends Operation>) Class.forName(className);

            return clazz.getDeclaredConstructor(Benchmark.class, Mode.class, int.class, int.class).newInstance(benchmark, mode, operationCount, operationSize);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            LOGGER.error("Unable to create benchmark phase of type '{}'", className);

            return null;
        }
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
