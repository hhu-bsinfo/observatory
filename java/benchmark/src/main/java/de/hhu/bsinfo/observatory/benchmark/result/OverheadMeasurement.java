package de.hhu.bsinfo.observatory.benchmark.result;

public class OverheadMeasurement {

    private long rawTotalData;
    private double overheadData;
    private double overheadFactor;
    private double overheadPercentage;

    private double rawDataThroughput;
    private double overheadDataThroughput;

    public OverheadMeasurement(long rawTotalData, Measurement measurement) {
        this.rawTotalData = rawTotalData;

        overheadData = rawTotalData - measurement.getTotalData();

        // This happens, when the benchmark is executed on an ethernet connection
        if(overheadData < 0) {
            overheadData = 0;
        }

        overheadFactor = (double) rawTotalData / measurement.getTotalData();
        overheadPercentage = (overheadData / measurement.getTotalData()) * 100;

        rawDataThroughput = (double) rawTotalData / measurement.getTotalTime();
        overheadDataThroughput = overheadData / measurement.getTotalTime();
    }

    public long getRawTotalData() {
        return rawTotalData;
    }

    public double getRawDataThroughput() {
        return rawDataThroughput;
    }

    public double getOverheadData() {
        return overheadData;
    }

    public double getOverheadFactor() {
        return overheadFactor;
    }

    public double getOverheadPercentage() {
        return overheadPercentage;
    }

    public double getOverheadDataThroughput() {
        return overheadDataThroughput;
    }

    @Override
    public String toString() {
        return "OverheadMeasurement {" +
                "\n\t" + ValueFormatter.formatValue("rawTotalData", rawTotalData, "Byte") +
                ",\n\t" + ValueFormatter.formatValue("overheadData", overheadData, "Byte") +
                ",\n\t" + ValueFormatter.formatValue("overheadFactor", overheadFactor) +
                ",\n\t" + ValueFormatter.formatValue("overheadPercentage", overheadPercentage, "%") +
                ",\n\t" + ValueFormatter.formatValue("rawDataThroughput", rawDataThroughput, "Byte/s") +
                ",\n\t" + ValueFormatter.formatValue("overheadDataThroughput", overheadDataThroughput, "Byte/s") +
                "\n}";
    }
}
