package de.hhu.bsinfo.observatory.plot;

import de.erichseifert.gral.data.DataSource;
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
        DataSource sourceHigh = getSourceWithHighestValue(data, 1, 2);

        double highestValue = (long) (getHighestValue(sourceHigh, 1, 2) / 500000000) * 500000000 + 500000001;

        for(long i = 0; i < highestValue + 500000000; i += 500000000) {
            yTicks.put((double) i, ValueFormatter.formatDataThroughputValue(i));
        }

        yRenderer.setCustomTicks(yTicks);
        yRenderer.setTickSpacing(Long.MAX_VALUE);
        yRenderer.setLabel(new Label("Data Throughput"));
        yRenderer.setLabelDistance(5.0);

        setAxisRenderer(XYPlot.AXIS_Y, yRenderer);
        getAxis(XYPlot.AXIS_Y).setMin(0);
        getAxis(XYPlot.AXIS_Y).setMax(highestValue);
        getAxis(XYPlot.AXIS_Y).setAutoscaled(false);
    }
}
