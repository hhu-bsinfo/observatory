package de.hhu.bsinfo.observatory.benchmark.result;

public class LatencyMeasurement extends Measurement {

    private double totalTime;
    private double operationThroughput;
    private LatencyStatistics latencyStatistics;

    public LatencyMeasurement(int operationCount, int operationSize) {
        super(operationCount, operationSize);

        latencyStatistics = new LatencyStatistics(operationCount);
    }

    public void startSingleMeasurement() {
        latencyStatistics.start();
    }

    public void stopSingleMeasurement() {
        latencyStatistics.stop();
    }

    public void finishMeasuring(long timeInNanos) {
        totalTime = timeInNanos / 1000000000d;

        operationThroughput = (double) getOperationCount() / totalTime;
        latencyStatistics.sortAscending();
    }

    public double getTotalTime() {
        return totalTime;
    }

    public double getAverageLatency() {
        return latencyStatistics.getAvgNs() / 1000000000;
    }

    public double getMinimumLatency() {
        return latencyStatistics.getMinNs() / 1000000000;
    }

    public double getMaximumLatency() {
        return latencyStatistics.getMaxNs() / 1000000000;
    }

    public double getPercentileLatency(float percentile) {
        return latencyStatistics.getPercentilesNs(percentile) / 1000000000;
    }

    public double getOperationThroughput() {
        return operationThroughput;
    }

    @Override
    public String toString() {
        return "LatencyMeasurement {" +
                "\n\t" + ValueFormatter.formatValue("operationCount", getOperationCount()) +
                ",\n\t" + ValueFormatter.formatValue("operationSize", getOperationSize(), "Byte") +
                ",\n\t" + ValueFormatter.formatValue("totalData", getTotalData(), "Byte") +
                ",\n\t" + ValueFormatter.formatValue("totalTime", getTotalTime(), "s") +
                ",\n\t" + ValueFormatter.formatValue("operationThroughput", operationThroughput, "Operations/s") +
                ",\n\t" + ValueFormatter.formatValue("averageLatency", getAverageLatency(), "s") +
                ",\n\t" + ValueFormatter.formatValue("minimumLatency", getMinimumLatency(), "s") +
                ",\n\t" + ValueFormatter.formatValue("maximumLatency", getMaximumLatency(), "s") +
                ",\n\t" + ValueFormatter.formatValue("50% Latency", getPercentileLatency(0.5f), "s") +
                ",\n\t" + ValueFormatter.formatValue("90% Latency", getPercentileLatency(0.9f), "s") +
                ",\n\t" + ValueFormatter.formatValue("95% Latency", getPercentileLatency(0.95f), "s") +
                ",\n\t" + ValueFormatter.formatValue("99% Latency", getPercentileLatency(0.99f), "s") +
                ",\n\t" + ValueFormatter.formatValue("99.9% Latency", getPercentileLatency(0.999f), "s") +
                ",\n\t" + ValueFormatter.formatValue("99.99% Latency", getPercentileLatency(0.9999f), "s") +
                "\n}";
    }
}
