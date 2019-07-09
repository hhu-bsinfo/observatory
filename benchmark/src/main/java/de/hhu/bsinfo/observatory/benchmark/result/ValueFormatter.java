package de.hhu.bsinfo.observatory.benchmark.result;

public class ValueFormatter {

    private ValueFormatter() {}

    private static final char[] metricTable = {
        0,
        'k',
        'm',
        'g',
        't',
        'p',
        'e'
    };

    public static String formatValue(final double value, final String unit) {
        double formattedValue = value;

        int counter = 0;
        while (formattedValue > 1000 && counter < metricTable.length) {
            formattedValue = formattedValue / 1000;
            counter++;
        }

        if(value == (long) value) {
            return String.format("%.3f %c%s (%d)", formattedValue, metricTable[counter], unit, (long) value);
        }

        return String.format("%.3f %c%s (%f)", formattedValue, metricTable[counter], unit, value);
    }

    public static String formatValue(final double value) {
        return formatValue(value, "Units");
    }

    public static String formatValue(final String name, final double value, final String unit) {
        return String.format("%-20s %s", name + ":", formatValue(value, unit));
    }

    public static String formatValue(final String name, final double value) {
        return formatValue(name, value, "Units");
    }
}
