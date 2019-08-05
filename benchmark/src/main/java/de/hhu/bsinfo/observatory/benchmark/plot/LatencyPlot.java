package de.hhu.bsinfo.observatory.benchmark.plot;

import de.erichseifert.gral.data.Column;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.statistics.Statistics;
import de.erichseifert.gral.graphics.Label;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.axes.LogarithmicRenderer2D;
import java.util.HashMap;
import java.util.Map;

class LatencyPlot extends Plot {

    LatencyPlot(DataSource... data) {
        super(data);

        AxisRenderer yRenderer = new LogarithmicRenderer2D();
        Map<Double, String> yTicks = new HashMap<>();
        Column yData = getColumnWithHighestValue(data, 1);

        for(int i = 1; i < yData.getStatistics(Statistics.MAX); i *= 10) {
            for(int j = i; j < i * 10; j += i) {
                yTicks.put((double) j, ValueFormatter.formatTimeValue(j));
            }
        }

        yRenderer.setCustomTicks(yTicks);
        yRenderer.setTickSpacing(Long.MAX_VALUE);
        yRenderer.setLabel(new Label("Latency"));
        yRenderer.setLabelDistance(5.0);

        double maxValue = yData.getStatistics(Statistics.MAX);
        long maxPower = (long) Math.pow(10, Math.floor(Math.log10(maxValue)));
        double yMax = (int) ((maxValue + maxPower) / maxPower) * maxPower + 1;

        double minValue = getColumnWithLowestValue(data, 1).getStatistics(Statistics.MIN);
        long minPower = (long) Math.pow(10, Math.floor(Math.log10(minValue)));
        double yMin = (int) (minValue / minPower) * minPower;

        setAxisRenderer(XYPlot.AXIS_Y, yRenderer);
        getAxis(XYPlot.AXIS_Y).setMin(yMin);
        getAxis(XYPlot.AXIS_Y).setMax(yMax);
        getAxis(XYPlot.AXIS_Y).setAutoscaled(false);
    }
}
