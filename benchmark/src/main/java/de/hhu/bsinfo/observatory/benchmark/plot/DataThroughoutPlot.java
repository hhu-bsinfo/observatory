package de.hhu.bsinfo.observatory.benchmark.plot;

import de.erichseifert.gral.data.Column;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.statistics.Statistics;
import de.erichseifert.gral.graphics.Label;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.axes.LinearRenderer2D;
import java.util.HashMap;
import java.util.Map;

class DataThroughoutPlot extends Plot {

    DataThroughoutPlot(DataSource... data) {
        super(data);

        AxisRenderer yRenderer = new LinearRenderer2D();
        Map<Double, String> yTicks = new HashMap<>();
        Column yData = getColumnWithHighestValue(data, 1);

        for(long i = 0; i < yData.getStatistics(Statistics.MAX) + 500000000; i += 500000000) {
            yTicks.put((double) i, ValueFormatter.formatDataThroughputValue(i));
        }

        yRenderer.setCustomTicks(yTicks);
        yRenderer.setTickSpacing(Long.MAX_VALUE);
        yRenderer.setLabel(new Label("Data Throughput"));
        yRenderer.setLabelDistance(5.0);

        setAxisRenderer(XYPlot.AXIS_Y, yRenderer);
        getAxis(XYPlot.AXIS_Y).setMin((long) (getColumnWithLowestValue(data, 1).getStatistics(Statistics.MIN) / 500000000) * 500000000);
        getAxis(XYPlot.AXIS_Y).setMax((long) (yData.getStatistics(Statistics.MAX) / 500000000) * 500000000 + 500000001);
        getAxis(XYPlot.AXIS_Y).setAutoscaled(false);
    }
}
