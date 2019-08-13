package de.hhu.bsinfo.observatory.plot;

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

    static String formatThroughputValue(final double value, final String unit) {
        double formattedValue = value;

        int counter = 0;
        while(formattedValue >= 1000 && formattedValue != 0 && counter < throughputMetricTable.length - 1) {
            formattedValue /= 1000;
            counter++;
        }

        if(String.valueOf((long) formattedValue).length() >= 3) {
            return String.format("%d %c%s", (long) formattedValue, throughputMetricTable[counter], unit);
        } else {
            return String.format("%.1f %c%s", formattedValue, throughputMetricTable[counter], unit);
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
