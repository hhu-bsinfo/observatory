package de.hhu.bsinfo.observatory.benchmark.result;

public abstract class Measurement implements Cloneable {

    private final int operationCount;
    private final int operationSize;

    private long totalData;

    Measurement(int operationCount, int operationSize) {
        this.operationCount = operationCount;
        this.operationSize = operationSize;
        totalData = (long) operationCount * (long) operationSize;
    }

    public int getOperationCount() {
        return operationCount;
    }

    public int getOperationSize() {
        return operationSize;
    }

    public long getTotalData() {
        return totalData;
    }

    public abstract double getTotalTime();

    public void setTotalData(long totalData) {
        this.totalData = totalData;
    }

    @Override
    public String toString() {
        return "Measurement {" +
            "\n\t" + ValueFormatter.formatValue("operationCount", operationCount) +
            ",\n\t" + ValueFormatter.formatValue("operationSize", operationSize, "Byte") +
            ",\n\t" + ValueFormatter.formatValue("totalData", totalData, "Byte") +
            "\n}";
    }
}
