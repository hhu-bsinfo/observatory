package de.hhu.bsinfo.observatory.benchmark.result;

public class Measurement implements Cloneable {

    private final int operationCount;
    private final int operationSize;
    private final long totalData;

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

    @Override
    public String toString() {
        return "Measurement {" +
            "\n\t" + ValueFormatter.formatValue("operationCount", operationCount) +
            ",\n\t" + ValueFormatter.formatValue("operationSize", operationSize) +
            ",\n\t" + ValueFormatter.formatValue("totalData",totalData) +
            "\n}";
    }

    @Override
    public Measurement clone() throws CloneNotSupportedException {
        return (Measurement) super.clone();
    }
}
