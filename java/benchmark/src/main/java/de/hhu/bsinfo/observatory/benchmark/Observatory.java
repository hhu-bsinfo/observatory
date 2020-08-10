package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.config.ParameterConfig;
import de.hhu.bsinfo.observatory.generated.BuildConfig;
import de.hhu.bsinfo.observatory.benchmark.Connection.Mode;
import de.hhu.bsinfo.observatory.benchmark.config.BenchmarkConfig;
import de.hhu.bsinfo.observatory.benchmark.config.OperationConfig;
import de.hhu.bsinfo.observatory.benchmark.config.IterationConfig;
import de.hhu.bsinfo.observatory.benchmark.config.OperationConfig.OperationMode;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Observatory {

    private static final Logger LOGGER = LoggerFactory.getLogger(Observatory.class);

    private final BenchmarkConfig config;
    private final String resultPath;
    private final boolean isServer;
    private final int connectionRetries;
    private final int threadCount;
    private final InetSocketAddress bindAddress;
    private final InetSocketAddress remoteAddress;

    public Observatory(BenchmarkConfig config, String resultPath, boolean isServer, int connectionRetries, int threadCount, InetSocketAddress bindAddress, InetSocketAddress remoteAddress) {
        this.config = config;
        this.resultPath = resultPath;
        this.isServer = isServer;
        this.connectionRetries = connectionRetries;
        this.threadCount = threadCount;
        this.bindAddress = bindAddress;
        this.remoteAddress = remoteAddress;
    }

    public void start() {
        Class<?> connectionClass;

        try {
            String connectionClassName = (String) BuildConfig.class.getField("CONNECTION_CLASS_NAME").get("");
            connectionClass = Observatory.class.getClassLoader().loadClass(connectionClassName);
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            LOGGER.error("Unable to load connection class", e);
            return;
        }

        for (OperationConfig operationConfig : config.getOperations()) {
            for (OperationMode mode : operationConfig.getModes()) {
                String operationClassName = "de.hhu.bsinfo.observatory.benchmark." + operationConfig.getName() + "Operation";

                for (IterationConfig iterationConfig : operationConfig.getIterations()) {
                    for (int i = 0; i < operationConfig.getRepetitions(); i++) {
                        Map<String, String> benchmarkParameters = Arrays.stream(config.getParameters()).collect(Collectors.toMap(ParameterConfig::getKey, ParameterConfig::getValue));
                        Benchmark benchmark = new Benchmark(connectionClass, connectionRetries, config.getDetectorConfig(), benchmarkParameters, isServer, connectionRetries, bindAddress, remoteAddress);


                        /*Benchmark connection = instantiateBenchmark();

                        if (connection == null) {
                            return;
                        }

                        Operation operation = null;

                        if (mode == OperationMode.UNIDIRECTIONAL) {
                            operation = instantiateOperation(operationClassName, connection,
                                    isServer ? Mode.SEND : Mode.RECEIVE, iterationConfig.getCount(),
                                    iterationConfig.getSize());
                        } else if (mode == OperationMode.BIDIRECTIONAL) {
                            Operation sendOperation = instantiateOperation(operationClassName, connection,
                                    Mode.SEND, iterationConfig.getCount(), iterationConfig.getSize());

                            Operation receiveOperation = instantiateOperation(operationClassName, connection,
                                    Mode.RECEIVE, iterationConfig.getCount(), iterationConfig.getSize());

                            if (sendOperation == null || receiveOperation == null) {
                                return;
                            }

                            if (!(sendOperation instanceof ThroughputOperation) || !(receiveOperation instanceof ThroughputOperation)) {
                                LOGGER.error("Invalid configuration: Only throughput operations may be executed bidirectionally");
                                return;
                            }

                            operation = new BidirectionalThroughputOperation((ThroughputOperation) sendOperation,
                                    (ThroughputOperation) receiveOperation);
                        }

                        if (operation == null) {
                            return;
                        }

                        Arrays.stream(config.getParameters()).forEach(parameter -> connection.setParameter(parameter.getKey(), parameter.getValue()));
                        connection.setServer(isServer);
                        connection.setConnectionRetries(connectionRetries);

                        connection.setDetectorConfig(config.getDetectorConfig());

                        connection.setBindAddress(bindAddress);
                        connection.setRemoteAddress(remoteAddress);


                        connection.setResultName(config.getResultName() == null ? config.getClassName() : config.getResultName());
                        connection.setResultPath(resultPath);
                        connection.setIterationNumber(i);

                        connection.addBenchmarkPhase(new InitializationPhase(connection));
                        connection.addBenchmarkPhase(new ConnectionPhase(connection));
                        connection.addBenchmarkPhase(new PreparationPhase(connection, iterationConfig.getSize(), iterationConfig.getCount()));

                        connection.addBenchmarkPhase(new FillReceiveQueuePhase(connection, operation));
                        connection.addBenchmarkPhase(new WarmUpPhase(connection, operation, iterationConfig.getWarmUpIterations()));

                        connection.addBenchmarkPhase(new FillReceiveQueuePhase(connection, operation));
                        connection.addBenchmarkPhase(new OperationPhase(connection, operation));

                        connection.addBenchmarkPhase(new CleanupPhase(connection));

                        executeBenchmark(connection);*/
                    }
                }
            }
        }
    }

    private static void executeBenchmark(Connection connection)  {
        LOGGER.info("Executing benchmark '{}'", connection.getClass().getSimpleName());

        Status status = connection.setup();
        if (status != Status.OK) {
            System.exit(status.ordinal());
        }

        connection.executePhases();
    }

    private static Benchmark instantiateBenchmark() {
        String className = "";

        try {
            className = (String) BuildConfig.class.getField("CONNECTION_CLASS_NAME").get("");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("Unable to read benchmark class name", e);

            return null;
        }

        try {
            Class<?> clazz = Observatory.class.getClassLoader().loadClass(className);
            return (Benchmark) clazz.getConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            LOGGER.error("Unable to create benchmark of type '{}'", className, e);

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Operation instantiateOperation(String className, Connection connection, Mode mode, int operationCount, int operationSize) {
        try {
            Class<? extends Operation> clazz = (Class<? extends Operation>) Class.forName(className);

            return clazz.getDeclaredConstructor(Connection.class, Mode.class, int.class, int.class).newInstance(connection, mode, operationCount, operationSize);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.error("Unable to create benchmark operation of type '{}'", className, e);

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
        String benchmarkClassName = "";

        try {
            benchmarkClassName = (String) BuildConfig.class.getField("CONNECTION_CLASS_NAME").get("");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.error("Unable to read benchmark class name", e);
        }

        System.out.print("\n");
        System.out.printf(banner, BuildConfig.VERSION, BuildConfig.BUILD_DATE, BuildConfig.GIT_BRANCH, BuildConfig.GIT_COMMIT, benchmarkClassName);
        System.out.print("\n\n");
    }
}
