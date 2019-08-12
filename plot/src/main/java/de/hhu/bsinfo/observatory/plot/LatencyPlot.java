package de.hhu.bsinfo.observatory.plot;

import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.graphics.Label;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.axes.LogarithmicRenderer2D;
import java.util.HashMap;
import java.util.Map;

class LatencyPlot extends Plot {

    LatencyPlot(DataSource... data) {
        super(data);

        AxisRenderer yRenderer = new LogarithmicRenderer2D();
        Map<Double, String> yTicks = new HashMap<>();
        DataSource sourceLow = getSourceWithLowestValue(data, 1, 2);
        DataSource sourceHigh = getSourceWithHighestValue(data, 1, 2);

        double minValue = getLowestValue(sourceLow, 1, 2);
        long minPower = (long) Math.pow(10, Math.floor(Math.log10(minValue)));
        double yMin = (int) (minValue / minPower) * minPower;

        double maxValue = getHighestValue(sourceHigh, 1, 2);
        long maxPower = (long) Math.pow(10, Math.floor(Math.log10(maxValue)));
        double yMax = (int) ((maxValue + maxPower) / maxPower) * maxPower + 1;

        for(int i = 1; i < maxValue; i *= 10) {
            for(int j = i; j < i * 10; j += i) {
                yTicks.put((double) j, ValueFormatter.formatTimeValue(j));
            }
        }

        yRenderer.setCustomTicks(yTicks);
        yRenderer.setTickSpacing(Long.MAX_VALUE);
        yRenderer.setLabel(new Label("Latency"));
        yRenderer.setLabelDistance(5.0);

        setAxisRenderer(AXIS_Y, yRenderer);
        getAxis(AXIS_Y).setMin(yMin);
        getAxis(AXIS_Y).setMax(yMax);
        getAxis(AXIS_Y).setAutoscaled(false);
    }
}
