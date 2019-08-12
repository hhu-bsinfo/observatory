package de.hhu.bsinfo.observatory.plot;

import com.google.common.math.Quantiles;
import com.google.common.math.Stats;
import de.erichseifert.gral.data.DataTable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PlotData {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlotData.class);

    private final String name;

    private final Map<String, Map<String, DataTable>> dataMap = new HashMap<>();

    PlotData(File benchmarkDirectory) throws IOException {
        this.name = benchmarkDirectory.getName();

        Map<String, Map<String, List<File>>> runMap = new HashMap<>();

        File[] runDirectories = benchmarkDirectory.listFiles();

        if(runDirectories != null) {
            for(File runDirectory : runDirectories) {
                File[] dataDirectories = runDirectory.listFiles();

                if (dataDirectories != null) {
                    for (File dataDirectory : dataDirectories) {
                        runMap.putIfAbsent(dataDirectory.getName(), new HashMap<>());

                        LOGGER.info("Reading files for '{}/{}'", name, dataDirectory.getName());

                        Map<String, DataTable> currentDataMap = new HashMap<>();

                        File[] dataFiles = dataDirectory.listFiles();

                        if (dataFiles != null) {
                            for (File dataFile : dataFiles) {
                                runMap.get(dataDirectory.getName()).putIfAbsent(dataFile.getName(), new ArrayList<>());
                                runMap.get(dataDirectory.getName()).get(dataFile.getName()).add(dataFile);
                            }
                        }

                        dataMap.put(dataDirectory.getName(), currentDataMap);
                    }
                }
            }
        }

        for(Entry<String, Map<String, List<File>>> entry : runMap.entrySet()) {
            dataMap.put(entry.getKey(), new HashMap<>());

            for(Entry<String, List<File>> fileEntry : entry.getValue().entrySet()) {
                LOGGER.info("Calculating values for '{}/{}'", name, fileEntry.getKey());

                String name = fileEntry.getKey().substring(0, fileEntry.getKey().lastIndexOf('.'));
                dataMap.get(entry.getKey()).put(name, generateDataTable(fileEntry.getValue()));
            }
        }
    }

    String getName() {
        return name;
    }

    String[] getMeasurements() {
        String[] ret = new String[0];
        ret = dataMap.keySet().toArray(ret);
        Arrays.sort(ret);

        return ret;
    }

    String[] getImplementations(String measurement) {
        String[] ret = new String[0];
        ret = dataMap.get(measurement).keySet().toArray(ret);
        Arrays.sort(ret);

        return ret;
    }

    DataTable getData(String measurement, String benchmarkName) {
        if(!dataMap.containsKey(measurement)) {
            return null;
        }

        if(!dataMap.get(measurement).containsKey(benchmarkName)) {
            return null;
        }

        return dataMap.get(measurement).get(benchmarkName);
    }

    @SuppressWarnings("unchecked")
    private DataTable generateDataTable(List<File> dataFiles) throws IOException {
        DataTable ret = new DataTable(Double.class, Double.class, Double.class);
        Map<Double, List<Double>> valueMap = new HashMap<>();

        for(File dataFile : dataFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(dataFile));

            String[] rawData = reader.readLine().split(",");

            for (String dataPoint : rawData) {
                String[] split = dataPoint.split(":");

                double xValue = Double.parseDouble(split[0]);
                double yValue = Double.parseDouble(split[1]);

                if(dataFile.getParentFile().getName().toLowerCase().contains("latency")) {
                    yValue *= 1e7;
                }

                valueMap.putIfAbsent(xValue, new ArrayList<>());
                valueMap.get(xValue).add(yValue);
            }
        }

        List<Entry<Double, List<Double>>> entries = new ArrayList<>(valueMap.entrySet());
        entries.sort(Comparator.comparing(Entry::getKey));

        for(Entry<Double, List<Double>> entry : entries) {
            double xValue = entry.getKey();
            double yValue = Quantiles.median().compute(entry.getValue());
            double yDeviation = Stats.of(entry.getValue()).sampleStandardDeviation();

            ret.add(xValue, yValue, yDeviation);
        }

        return ret;
    }
}
