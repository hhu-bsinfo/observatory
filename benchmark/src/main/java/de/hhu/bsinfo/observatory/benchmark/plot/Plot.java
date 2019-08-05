package de.hhu.bsinfo.observatory.benchmark.plot;

import de.erichseifert.gral.data.Column;
import de.erichseifert.gral.data.statistics.Statistics;
import de.erichseifert.gral.graphics.Insets2D;
import de.erichseifert.gral.graphics.Label;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.axes.LogarithmicRenderer2D;
import java.util.HashMap;
import java.util.Map;

class Plot extends XYPlot {

    Plot(DataSource... data) {
        super(data);

        AxisRenderer xRenderer = new LogarithmicRenderer2D();
        Map<Double, String> xTicks = new HashMap<>();
        Column xData = getColumnWithHighestValue(data, 0);

        for(int i = 0; i < xData.size(); i++) {
            double xValue = (Double) xData.get(i);
            String xLabel = ValueFormatter.formatByteValue(xValue);

            xTicks.put(xValue, xLabel);
        }

        xRenderer.setCustomTicks(xTicks);
        xRenderer.setTickSpacing(Long.MAX_VALUE);
        xRenderer.setLabel(new Label("Packet Size"));

        setAxisRenderer(XYPlot.AXIS_X, xRenderer);
        getAxis(XYPlot.AXIS_X).setRange(0, xData.getStatistics(Statistics.MAX) + 1);
        getAxis(XYPlot.AXIS_X).setAutoscaled(false);

        setInsets(new Insets2D.Double(20, 120, 60, 20));
    }

    Column getColumnWithHighestValue(DataSource[] dataSources, int column) {
        Column ret = dataSources[0].getColumn(column);

        for(DataSource dataSource : dataSources) {
            if(dataSource.getColumn(column).getStatistics(Statistics.MAX) > ret.getStatistics(Statistics.MAX)) {
                ret = dataSource.getColumn(column);
            }
        }

        return ret;
    }

    Column getColumnWithLowestValue(DataSource[] dataSources, int column) {
        Column ret = dataSources[0].getColumn(column);

        for(DataSource dataSource : dataSources) {
            if(dataSource.getColumn(column).getStatistics(Statistics.MIN) < ret.getStatistics(Statistics.MIN)) {
                ret = dataSource.getColumn(column);
            }
        }

        return ret;
    }
}
