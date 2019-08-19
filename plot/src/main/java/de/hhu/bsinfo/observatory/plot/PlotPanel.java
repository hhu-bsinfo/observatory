package de.hhu.bsinfo.observatory.plot;

import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.io.plots.DrawableWriterFactory;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.plots.points.PointRenderer;
import de.erichseifert.gral.ui.InteractivePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PlotPanel extends JPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlotPanel.class);

    private static final HashMap<String, Color> COLOR_MAP = new HashMap<>();
    private static final Random COLOR_RANDOMIZER = new Random();

    private final Stack<Color> COLORS = new Stack<Color>() {{
        Color.decode("#8dd3c7"); Color.decode("#ffffb3"); Color.decode("#bebada"); Color.decode("#fb8072");
        Color.decode("#80b1d3"); Color.decode("#fdb462"); Color.decode("#b3de69"); Color.decode("#fccde5");
        Color.decode("#d9d9d9"); Color.decode("#bc80bd"); Color.decode("#ccebc5"); Color.decode("#ffed6f"); }};

    private final File dataDirectory;

    private PlotData plotData;

    PlotPanel(File dataDirectory) throws IOException {
        this.dataDirectory = dataDirectory;

        plotData = new PlotData(dataDirectory);

        setLayout(new BorderLayout());

        setupGraphView();
    }

    private void setupGraphView() {
        JTabbedPane tabbedPane = new JTabbedPane();

        for(String measurement : plotData.getMeasurements()) {
            List<DataSource> dataList = new ArrayList<>();
            List<Color> colorList = new ArrayList<>();

            LOGGER.info("Generating plot for '{}/{}'", plotData.getName(), measurement);

            for (String implementation : plotData.getImplementations(measurement)) {
                DataSource currentData = plotData.getData(measurement, implementation);

                if (currentData != null) {
                    dataList.add(currentData);

                    if (!COLOR_MAP.containsKey(implementation)) {
                        if(COLORS.isEmpty()) {
                            COLOR_MAP.put(implementation,new Color(COLOR_RANDOMIZER.nextFloat(), COLOR_RANDOMIZER.nextFloat(), COLOR_RANDOMIZER.nextFloat()));
                        } else {
                            COLOR_MAP.put(implementation, COLORS.pop());
                        }
                    }

                    colorList.add(COLOR_MAP.get(implementation));
                }
            }

            DataSource[] data = new DataSource[0];
            data = dataList.toArray(data);

            XYPlot plot;

            if(measurement.equals("DataOverheadFactor")) {
                plot = new ThroughputPlot(data, "Data Overhead Factor", "");
            } else {
                if(dataDirectory.getName().toLowerCase().contains("throughput")) {
                    if(measurement.equals("DataThroughput")) {
                        plot = new ThroughputPlot(data, "Data Throughput", "B/s");
                    } else if(measurement.equals("OperationThroughput")) {
                        plot = new ThroughputPlot(data, "Operation Throughput", "Op/s");
                    } else if(measurement.equals("DataThroughputOverhead")) {
                        plot = new ThroughputPlot(data, "Data Throughput Overhead", "B/s");
                    } else {
                        continue;
                    }
                } else if(dataDirectory.getName().toLowerCase().contains("latency")) {
                    if(measurement.contains("Overhead")) {
                        continue;
                    }

                    plot = new LatencyPlot(data);
                } else {
                    continue;
                }
            }

            for(int i = 0; i < data.length; i++) {
                LineRenderer lineRenderer = new DefaultLineRenderer2D();
                lineRenderer.setColor(colorList.get(i));

                plot.setLineRenderers(data[i], lineRenderer);

                PointRenderer pointRenderer = plot.getPointRenderers(data[i]).get(0);
                pointRenderer.setColor(colorList.get(i));
                pointRenderer.setErrorColor(colorList.get(i));
                pointRenderer.setErrorColumnBottom(2);
                pointRenderer.setErrorColumnTop(2);
                pointRenderer.setErrorVisible(true);
            }

            InteractivePanel graphPanel = new InteractivePanel(plot);
            graphPanel.setPannable(false);
            graphPanel.setZoomable(false);

            JCheckBoxList list = new JCheckBoxList(plotData.getImplementations(measurement));
            list.selectAll();

            for(int i = 0; i < list.getModel().getSize(); i++) {
                JCheckBox checkBox = list.getModel().getElementAt(i);
                checkBox.setForeground(colorList.get(i));
            }

            list.addItemListener(e -> {
                JCheckBox checkBox = (JCheckBox) e.getItem();

                if(checkBox.isSelected()) {
                    DataSource currentData =  plotData.getData(measurement, checkBox.getText());
                    plot.add(currentData);

                    Color color = COLOR_MAP.get(checkBox.getText());

                    LineRenderer lineRenderer = new DefaultLineRenderer2D();
                    lineRenderer.setColor(color);

                    plot.setLineRenderers(currentData, lineRenderer);

                    PointRenderer pointRenderer = plot.getPointRenderers(currentData).get(0);
                    pointRenderer.setColor(color);
                    pointRenderer.setErrorColor(color);
                    pointRenderer.setErrorColumnBottom(2);
                    pointRenderer.setErrorColumnTop(2);
                    pointRenderer.setErrorVisible(true);
                } else {
                    plot.remove(plotData.getData(measurement, checkBox.getText()));
                }

                graphPanel.repaint();
            });

            JPanel savePanel = new JPanel(new FlowLayout());
            JTextField resX = new JTextField("2560");
            JLabel xLabel = new JLabel("x");
            JTextField resY = new JTextField("1440");
            JComboBox<String> formatBox = new JComboBox<>(new String[]{"svg", "pdf", "png", "jpeg", "bmp"});
            JButton saveButton = new JButton("Save plot");

            saveButton.addActionListener(e -> {
                File file = new File("plot/" + tabbedPane.getTitleAt(tabbedPane.getSelectedIndex()) + "." + formatBox.getSelectedItem());
                if(!file.getParentFile().exists()) {
                    if(!file.getParentFile().mkdirs()) {
                        LOGGER.error("Unable to create folder '{}'", file.getParentFile().getPath());
                        return;
                    }
                }

                String mimeType = (String) formatBox.getSelectedItem();

                if(mimeType == null) {
                    LOGGER.error("Invalid file type selected");
                    return;
                }

                if(mimeType.equals("svg")) {
                    mimeType += "+xml";
                }

                if(mimeType.equals("pdf")) {
                    mimeType = "application/" + mimeType;
                } else {
                    mimeType = "image/" + mimeType;
                }

                try {
                    DrawableWriterFactory.getInstance().get(mimeType).write(plot, new FileOutputStream(file),
                            Integer.parseInt(resX.getText()), Integer.parseInt(resY.getText()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            savePanel.add(resX);
            savePanel.add(xLabel);
            savePanel.add(resY);
            savePanel.add(formatBox);
            savePanel.add(saveButton);

            JPanel tabRoot = new JPanel(new BorderLayout());
            tabRoot.add(BorderLayout.CENTER, graphPanel);
            tabRoot.add(BorderLayout.WEST, new JScrollPane(list));
            tabRoot.add(BorderLayout.SOUTH, savePanel);

            tabbedPane.addTab(measurement, tabRoot);
            graphPanel.repaint();
        }

        add(BorderLayout.CENTER, tabbedPane);
    }
}