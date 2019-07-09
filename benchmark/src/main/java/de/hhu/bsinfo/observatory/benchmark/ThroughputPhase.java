package de.hhu.bsinfo.observatory.benchmark;

import de.hhu.bsinfo.observatory.benchmark.result.BenchmarkMode;
import de.hhu.bsinfo.observatory.benchmark.result.ThroughputMeasurement;
import java.util.Map;
import java.util.TreeMap;

abstract class ThroughputPhase extends MeasurementPhase {

    ThroughputPhase(Benchmark benchmark, BenchmarkMode mode, Map<Integer, Integer> measurementOptions) {
        super(benchmark, mode, new ThroughputMeasurement[measurementOptions.size()]);

        Map<Integer, Integer> sortedOptions = new TreeMap<>(measurementOptions);

        int i = 0;
        for(int size : sortedOptions.keySet()) {
            getMeasurements()[i] = new ThroughputMeasurement(sortedOptions.get(size), size);
            i++;
        }
    }
}
