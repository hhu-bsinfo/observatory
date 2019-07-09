package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.BenchmarkMode;
import de.hhu.bsinfo.observatory.benchmark.result.Measurement;
import de.hhu.bsinfo.observatory.benchmark.result.Status;
import de.hhu.bsinfo.observatory.benchmark.result.ThroughputMeasurement;
import java.util.HashMap;
import java.util.Map;

public class MessagingThroughputPhase extends ThroughputPhase {

    MessagingThroughputPhase(Benchmark benchmark, BenchmarkMode mode, Map<Integer, Integer> measurementOptions) {
        super(benchmark, mode, measurementOptions);
    }

    @Override
    protected Status execute(Measurement measurement) {
        return getBenchmark().measureMessagingThroughput(getMode(), (ThroughputMeasurement) measurement);
    }
}
