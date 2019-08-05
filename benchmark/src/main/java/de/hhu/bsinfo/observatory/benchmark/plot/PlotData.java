package de.hhu.bsinfo.observatory.benchmark.plot;

import de.erichseifert.gral.data.DataTable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PlotData {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlotData.class);


    private final String name;

    private final Map<String, Map<String, DataTable>> dataMap = new HashMap<>();

    PlotData(File benchmarkDirectory) throws IOException {
        this.name = benchmarkDirectory.getName();

        File[] runDirectories = benchmarkDirectory.listFiles();

        if(runDirectories != null) {
            // TODO: Iterate over all run directories and generate error bars
            File runDirectory = new File(benchmarkDirectory.getPath() + "/1/");

            File[] dataDirectories = runDirectory.listFiles();

            if(dataDirectories != null) {
                for(File dataDirectory : dataDirectories) {
                    LOGGER.info("Parsing data for '{}/{}'", name, dataDirectory.getName());

                    Map<String, DataTable> currentDataMap = new HashMap<>();

                    File[] dataFiles = dataDirectory.listFiles();

                    if(dataFiles != null) {
                        for (File dataFile : dataFiles) {
                            currentDataMap.put(dataFile.getName().substring(0, dataFile.getName().lastIndexOf('.')), generateDataTable(dataFile));
                        }
                    }

                    dataMap.put(dataDirectory.getName(), currentDataMap);
                }
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

    private DataTable generateDataTable(File dataFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(dataFile));
        DataTable table = new DataTable(Double.class, Double.class);

        String[] rawData = reader.readLine().split(",");

        for(String dataPoint : rawData) {
            String[] split = dataPoint.split(":");

            if(dataFile.getParentFile().getName().toLowerCase().contains("latency")) {
                table.add(Double.parseDouble(split[0]), Double.parseDouble(split[1]) * 1e7);
            } else {
                table.add(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
            }
        }

        return table;
    }
}
