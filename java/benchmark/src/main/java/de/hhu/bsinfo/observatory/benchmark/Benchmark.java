package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.jdetector.lib.IbFabric;
import de.hhu.bsinfo.jdetector.lib.IbPerfCounter;
import de.hhu.bsinfo.jdetector.lib.exception.IbFileException;
import de.hhu.bsinfo.jdetector.lib.exception.IbMadException;
import de.hhu.bsinfo.jdetector.lib.exception.IbNetDiscException;
import de.hhu.bsinfo.jdetector.lib.exception.IbVerbsException;
import de.hhu.bsinfo.observatory.benchmark.config.DetectorConfig;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Benchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(Benchmark.class);

    private final Connection[] connections;
    private final DetectorConfig detectorConfig;

    private IbFabric fabric;
    private IbPerfCounter perfCounter;

    private String resultName;
    private String resultPath;

    private final Map<String, String> parameters;
    private final List<BenchmarkPhase> phases = new ArrayList<>();

    public Benchmark(Class<?> connectionClass, int threadCount, DetectorConfig detectorConfig, Map<String, String> parameters, boolean isServer, int connectionRetries, InetSocketAddress bindAddress, InetSocketAddress remoteAddress) {
        this.detectorConfig = detectorConfig;
        this.parameters = parameters;

        connections = new Connection[threadCount];

        for (Connection connection : connections) {
            try {
                connection = (Connection) connectionClass.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                LOGGER.error("Unable to instantiate connection of type {}", connectionClass, e);
            }
        }
    }

    void addBenchmarkPhase(BenchmarkPhase phase) {
        phases.add(phase);
    }

    protected String getParameter(String key, String defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }

    protected byte getParameter(String key, byte defaultValue) {
        return Byte.parseByte(parameters.getOrDefault(key, String.valueOf(defaultValue)));
    }

    protected short getParameter(String key, short defaultValue) {
        return Short.parseShort(parameters.getOrDefault(key, String.valueOf(defaultValue)));
    }

    protected int getParameter(String key, int defaultValue) {
        return Integer.parseInt(parameters.getOrDefault(key, String.valueOf(defaultValue)));
    }

    protected long getParameter(String key, long defaultValue) {
        return Long.parseLong(parameters.getOrDefault(key, String.valueOf(defaultValue)));
    }

    protected BenchmarkPhase[] getPhases() {
        BenchmarkPhase[] phasesArray = new BenchmarkPhase[phases.size()];
        return phases.toArray(phasesArray);
    }

    Status setup() {
        if (detectorConfig.isEnabled()) {
            LOGGER.info("Initializing jDetector");

            try {
                fabric = new IbFabric(false, detectorConfig.getMode() == DetectorConfig.MeasurementMode.COMPAT);
            } catch (IbFileException | IbMadException | IbVerbsException | IbNetDiscException e) {
                LOGGER.error("Unable to initialize jDetector!", e);
                return Status.UNKNOWN_ERROR;
            }

            if (fabric.getNumNodes() == 0) {
                LOGGER.error("Fabric scanned by jDetector: 0 devices found!");
                return Status.UNKNOWN_ERROR;
            } else {
                LOGGER.info("Fabric scanned by jDetector: {} {} found", fabric.getNumNodes(), fabric.getNumNodes() == 1 ? "device was" : "devices were");
            }

            LOGGER.info("Measuring overhead on {}", fabric.getNodes()[detectorConfig.getDeviceNumber()].getDescription());

            perfCounter = fabric.getNodes()[detectorConfig.getDeviceNumber()];
        }

        for (Connection connection : connections) {
            Status status = connection.setup();

            if (status != Status.OK) {
                return status;
            }
        }

        return Status.OK;
    }
}
