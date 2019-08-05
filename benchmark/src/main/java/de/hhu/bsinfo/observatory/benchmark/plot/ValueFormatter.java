package de.hhu.bsinfo.observatory.benchmark.plot;

class ValueFormatter {

    private ValueFormatter() {}

    private static final char[] throughputMetricTable = {
        0,
        'K',
        'M',
        'G',
        'T',
        'P',
        'E'
    };

    private static final char[] latencyMetricTable = {
            'n',
            'u',
            'm',
            0,
            'K',
            'M',
            'G',
            'T',
            'P',
            'E'
    };

    static String formatByteValue(final double value) {
        double formattedValue = value;

        int counter = 0;
        while(formattedValue >= 1024 && formattedValue != 0 && counter < throughputMetricTable.length - 1) {
            formattedValue /= 1024;
            counter++;
        }

        if(counter == 0) {
            return String.format("%d B", (int) formattedValue);
        } else {
            return String.format("%d %ciB", (int) formattedValue, throughputMetricTable[counter]);
        }
    }

    static String formatDataThroughputValue(final double value) {
        double formattedValue = value;

        int counter = 0;
        while(formattedValue >= 1000 && formattedValue != 0 && counter < throughputMetricTable.length - 1) {
            formattedValue /= 1000;
            counter++;
        }

        if(String.valueOf((long) formattedValue).length() >= 3) {
            return String.format("%d %cB/s", (long) formattedValue, throughputMetricTable[counter]);
        } else {
            return String.format("%.2f %cB/s", formattedValue, throughputMetricTable[counter]);
        }
    }

    static String formatOperationThroughputValue(final double value) {
        double formattedValue = value;

        int counter = 0;
        while(formattedValue >= 1000 && formattedValue != 0 && counter < throughputMetricTable.length - 1) {
            formattedValue /= 1000;
            counter++;
        }

        if(String.valueOf((long) formattedValue).length() >= 3) {
            return String.format("%d %cOp/s", (long) formattedValue, throughputMetricTable[counter]);
        } else {
            return String.format("%.2f %cOp/s", formattedValue, throughputMetricTable[counter]);
        }
    }

    static String formatTimeValue(final double value) {
        double formattedValue = value * 100;

        int counter = 0;
        while(formattedValue >= 1000 && formattedValue != 0 && counter < latencyMetricTable.length - 1) {
            formattedValue /= 1000;
            counter++;
        }

        return String.format("%d %cs", (long) formattedValue, latencyMetricTable[counter]);
    }
}
