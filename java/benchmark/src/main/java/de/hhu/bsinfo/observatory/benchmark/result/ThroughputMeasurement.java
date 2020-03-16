package de.hhu.bsinfo.observatory.benchmark.result;

public class ThroughputMeasurement extends Measurement {

    private double totalTime;
    private double operationThroughput;
    private double dataThroughput;

    public ThroughputMeasurement(int operationCount, int operationSize) {
        super(operationCount, operationSize);
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setMeasuredTime(long timeInNanos) {
        this.totalTime = timeInNanos / 1000000000d;

        operationThroughput = (double) getOperationCount() / totalTime;
        dataThroughput = (double) getTotalData() / totalTime;
    }

    public double getOperationThroughput() {
        return operationThroughput;
    }

    public double getDataThroughput() {
        return dataThroughput;
    }

    public void setTotalTime(double timeInSeconds) {
        this.totalTime = timeInSeconds;
    }

    public void setOperationThroughput(double operationThroughput) {
        this.operationThroughput = operationThroughput;
    }

    public void setDataThroughput(double dataThroughput) {
        this.dataThroughput = dataThroughput;
    }

    @Override
    public String toString() {
        return "ThroughputMeasurement {" +
            "\n\t" + ValueFormatter.formatValue("operationCount", getOperationCount()) +
            ",\n\t" + ValueFormatter.formatValue("operationSize", getOperationSize(), "Byte") +
            ",\n\t" + ValueFormatter.formatValue("totalData", getTotalData(), "Byte") +
            ",\n\t" + ValueFormatter.formatValue("totalTime", totalTime, "s") +
            ",\n\t" + ValueFormatter.formatValue("operationThroughput", operationThroughput, "Operations/s") +
            ",\n\t" + ValueFormatter.formatValue("dataThroughput", dataThroughput, "Byte/s") +
            "\n}";
    }
}
