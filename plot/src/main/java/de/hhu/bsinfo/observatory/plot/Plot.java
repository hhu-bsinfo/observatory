package de.hhu.bsinfo.observatory.plot;

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

    double getHighestValue(DataSource dataSource, int column, int deviationColumn) {
        double ret = 0;

        for(int i = 0; i < dataSource.getRowCount(); i++) {
            double value = (Double) dataSource.get(column, i) + (Double) dataSource.get(deviationColumn, i);
            if(value > ret) {
                ret = value;
            }
        }

        return ret;
    }

    private Column getColumnWithHighestValue(DataSource[] dataSources, int column) {
        Column ret = dataSources[0].getColumn(column);

        for(DataSource dataSource : dataSources) {
            if(dataSource.getColumn(column).getStatistics(Statistics.MAX) > ret.getStatistics(Statistics.MAX)) {
                ret = dataSource.getColumn(column);
            }
        }

        return ret;
    }

    DataSource getSourceWithHighestValue(DataSource[] dataSources, int column, int deviationColumn) {
        DataSource ret = dataSources[0];
        double highestValue = 0;

        for(DataSource dataSource : dataSources) {
            double value = getHighestValue(dataSource, column, deviationColumn);

            if(value > highestValue) {
                highestValue = value;
                ret = dataSource;
            }
        }

        return ret;
    }

    double getLowestValue(DataSource dataSource, int column, int deviationColumn) {
        Double ret = null;

        for(int i = 0; i < dataSource.getRowCount(); i++) {
            double value = (Double) dataSource.get(column, i) - (Double) dataSource.get(deviationColumn, i);
            if(ret == null || value < ret) {
                ret = value;
            }
        }

        return ret == null ? 0 : ret;
    }

    DataSource getSourceWithLowestValue(DataSource[] dataSources, int column, int deviationColumn) {
        DataSource ret = dataSources[0];
        double lowestValue = 0;

        for(DataSource dataSource : dataSources) {
            double value = getHighestValue(dataSource, column, deviationColumn);

            if(value < lowestValue) {
                lowestValue = value;
                ret = dataSource;
            }
        }

        return ret;
    }
}
