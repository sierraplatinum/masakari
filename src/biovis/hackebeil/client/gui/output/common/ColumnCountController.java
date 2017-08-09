/**
 *  Copyright 2016 Alrik Hausdorf, Nicole Hinzmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package biovis.hackebeil.client.gui.output.common;

import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

import biovis.hackebeil.client.io.ImageExport;
import biovis.hackebeil.client.utilities.ChartUtilities;
import biovislib.javafx.chart.LogarithmicAxis;
import biovislib.javafx.chart.XNumberAxis;
import java.io.IOException;
import java.util.SortedMap;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * @author Dirk Zeckzer, Alrik Hausdorf
 *
 */
public class ColumnCountController {

    private static final Logger log = Logger.getLogger(ColumnCountController.class.getName());

    private final static int DEFAULT_BLOCK_COUNT = 100;

    private Boolean isInitialized = false;

    private AnchorPane anchorPane;

    private ColumnCountInterface columnCountInterface = null;

    @FXML
    private GridPane baseLayoutGrid;
    @FXML
    private HBox logScaleSettings;
    @FXML
    private CheckBox cbXAxisLogScale;
    @FXML
    private CheckBox cbYAxisLogScale;
    @FXML
    private CheckBox cbIgnoreNullValues;
    @FXML
    private HBox rangeSettings;
    @FXML
    private Button btnReset;
    @FXML
    private TextField tfFrom;
    @FXML
    private TextField tfTo;
    @FXML
    private AnchorPane chartPane;

    private int xAxisBlockCount = DEFAULT_BLOCK_COUNT;

    private BarChart<String, Integer> chart;

    /**
     */
    public ColumnCountController() {
    }

    /**
     *
     * @param pane
     */
    private void setAnchorPane(
        AnchorPane pane
    ) {
        this.anchorPane = pane;
    }

    /**
     *
     * @return
     */
    public static ColumnCountController getInstance() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(ColumnCountController.class.getResource("ColumnCount.fxml"));
            AnchorPane anchorPane = loader.load();
            ColumnCountController controller = loader.<ColumnCountController>getController();
            controller.setAnchorPane(anchorPane);
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void initialize() {
        isInitialized = true;
        testForReady();
    }

    /**
     * Redraw information for normalized values.
     * @param normalizedValues
     */
    public void redrawNormalizedValues(
        List<Double> normalizedValues
    ) {
        if (normalizedValues == null
            || normalizedValues.isEmpty()) {
            return;
        }

        // Start Computing
        Platform.runLater(() -> {
            int correction = DEFAULT_BLOCK_COUNT;
            SortedMap<Integer, Integer> motifDistribution = createMap(normalizedValues,
                                                                      correction);

            long from = motifDistribution.firstKey();
            long to = motifDistribution.lastKey();
            if (from == to) {
                createChartSingleValue(from);
                return;
            }

            // Calc Blockstarts
            long[] blockstarts = ChartUtilities.createBins(from, to,
                                                           xAxisBlockCount + 1,
                                                           cbXAxisLogScale.isSelected());

            List<XYChart.Data<String, Integer>> chartData
                = ChartUtilities.createChartDataforBinsWithCorrection(motifDistribution,
                                                                      blockstarts,
                                                                      correction,
                                                                      from, to);

            createChart(chartData);
        });
    }

    /**
     * Redraw information for counts.
     * @param counts
     */
    public void redrawCounts(
        List<Double> counts
    ) {
        SortedMap<Integer, Integer> countDistribution = createMap(counts, 1);
        redrawCounts(countDistribution);
    }

    /**
     * Redraw information for counts.
     * @param counts
     */
    public void redrawCounts(
        SortedMap<Integer, Integer> counts
    ) {
        if (counts == null
            || counts.isEmpty()) {
            return;
        }

        // Start Computing
        Platform.runLater(() -> {
            initRange(counts);

            long from;
            if (tfFrom.getText().equals("")) {
                from = counts.firstKey();
            } else {
                from = Integer.parseInt(tfFrom.getText());
            }
            long to;
            if (tfTo.getText().equals("")) {
                to = counts.lastKey();
            } else {
                to = Integer.parseInt(tfTo.getText());
            }

            if (from == to) {
                createChartSingleValue(from);
                return;
            }

            // Calc Blockstarts
            long[] blockstarts = ChartUtilities.createBins(from, to,
                                                           //xAxisBlockCount + 1,
                                                           xAxisBlockCount,
                                                           cbXAxisLogScale.isSelected());

            List<XYChart.Data<String, Integer>> chartData
                = ChartUtilities.createChartDataforBins(counts,
                                                        blockstarts,
                                                        from, to);

            createChart(chartData, true);
        });
    }

    /**
     * Redraw information for pwm data.
     * @param id
     * @param logarithmicValues
     */
    public void redrawLogarithmicValues(
        String id,
        List<Double> logarithmicValues
    ) {
        if (logarithmicValues == null
            || logarithmicValues.isEmpty()) {
            return;
        }

        Platform.runLater(() -> {
            // Determine correction value
            logarithmicValues.sort((d1, d2) -> Double.compare(d1, d2));
            double minValue = logarithmicValues.get(0);
            int noMatch = 0;
            while (minValue == -Double.MAX_VALUE) {
                logarithmicValues.remove(0);
                minValue = logarithmicValues.get(0);
                ++noMatch;
            }

            double maxValue = logarithmicValues.get(logarithmicValues.size() - 1);
            // Adjust min and max value
            /*
            double lowerMagnitude = Math.floor(Math.log10(minValue));
            double upperMagnitude = Math.ceil(Math.log10(maxValue));
            */
            double lowerMagnitude = Math.floor(minValue);
            double upperMagnitude = Math.ceil(maxValue);
            if (lowerMagnitude < 0.0) {
                lowerMagnitude = 0.0;
            }
            /*
            minValue = Math.pow(10, lowerMagnitude);
            maxValue = Math.pow(10, upperMagnitude);
            */
            minValue = lowerMagnitude;
            maxValue = upperMagnitude;

            final int correction = (int) Math.ceil(maxValue - minValue) * DEFAULT_BLOCK_COUNT;

            // Start Computing
            SortedMap<Integer, Integer> pwmDistribution;

            pwmDistribution = createMap(logarithmicValues,
                                        correction);

            double from = minValue * correction; //pwmDistribution.firstKey();
            double to = maxValue * correction; //pwmDistribution.lastKey();

            long fromLong = (long) from;// Math.round(from * correction);
            long toLong = (long) to; //Math.round(to * correction);

            // Calc Blockstarts
            long[] blockstarts = ChartUtilities.createBins(fromLong, toLong,
                                                           xAxisBlockCount + 1,
                                                           cbXAxisLogScale.isSelected());

            // System.out.println(Arrays.toString(blockstarts));
            List<XYChart.Data<String, Integer>> chartData
                = ChartUtilities.createChartDataforBinsWithCorrection(pwmDistribution, blockstarts, correction,
                                                                      (long) Math.floor(from),
                                                                      (long) Math.ceil(to));

            createChart(chartData);
        });
    }

    private void initRange(
        SortedMap<Integer, Integer> distribution
    ) {
        Platform.runLater(() -> {
            if (!tfFrom.getText().equals(distribution.firstKey().toString())
                || !tfTo.getText().equals(distribution.lastKey().toString())) {
                btnReset.setDisable(false);
            }
            if (tfFrom.getText().equals("")) {
                tfFrom.setText(distribution.firstKey().toString());
            }
            if (tfTo.getText().equals("")) {
                tfTo.setText(distribution.lastKey().toString());
            }
        });
    }

    /**
     * Create a binned map of integer values for a list of doubles.
     *
     * @param dataValues initial list of double values
     * @param correction correction factor
     * @return binned map of integer values
     */
    public SortedMap<Integer, Integer> createMap(
        List<Double> dataValues,
        int correction
    ) {
        SortedMap<Integer, Integer> dataMap = new TreeMap<>();
        boolean ignoreNull = false;
        if (cbIgnoreNullValues.isVisible()) {
            ignoreNull = cbIgnoreNullValues.isSelected();
        }
        for (Double value : dataValues) {
            Integer key = (int) Math.round(value * correction);
            if (ignoreNull && key == 0) {
                continue;
            }
            if (dataMap.containsKey(key)) {
                dataMap.replace(key, dataMap.get(key) + 1);
            } else {
                dataMap.put(key, 1);
            }
        }
        return dataMap;
    }

    private void createChartSingleValue(long from) {
        createChartSingleValue(Long.toString(from));
    }

    private void createChartSingleValue(
        String valueString
    ) {
        Platform.runLater(() -> {
            ColumnCountController.this.createChart(1.0);
            chart.getData().clear();
            chart.setTitle("Just one Value present(" + valueString + ")");
            chart.setTitleSide(Side.TOP);
        });
    }

    /**
     * Create a new chart.
     *
     * @param chartData
     */
    public void createChart(
        List<XYChart.Data<String, Integer>> chartData
    ) {
        createChart(chartData, false);
    }

    /**
     * Create a new chart.
     *
     * @param chartData
     * @param allowZoom
     */
    public void createChart(
        List<XYChart.Data<String, Integer>> chartData,
        boolean allowZoom
    ) {
        Platform.runLater(() -> {
            // compute maximum y value
            int maximum = 0;
            XYChart.Series<String, Integer> series1 = new XYChart.Series<>();
            for (XYChart.Data<String, Integer> a : chartData) {
                series1.getData().add(a);
                if (a.getYValue() > maximum) {
                    maximum = a.getYValue();
                }
            }

            // create chart
            createChart(maximum);
            chart.getData().clear();
            chart.getData().add(series1);
            for (Series<String, Integer> series : chart.getData()) {
                for (Data<String, Integer> data : series.getData()) {
                    Tooltip tooltip = ChartUtilities.createTooltip(data);
                    Tooltip.install(data.getNode(), tooltip);
                    if (allowZoom) {
                        data.getNode().setOnMousePressed((MouseEvent event) -> {
                            if (event.getClickCount() > 1) {
                                zoomTo(data);
                            }
                        });
                    }
                }
            }
        });
    }

    private void zoomTo(Data<String, Integer> data) {
        if (data.getXValue().contains("-")) {
            String[] temp = data.getXValue().split("-");
            if (Integer.parseInt(temp[1]) - Integer.parseInt(temp[0]) < DEFAULT_BLOCK_COUNT) {
                int target = Integer.parseInt(temp[0])
                             + (int) Math.round((Math.round(Integer.parseInt(temp[1]) - Integer.parseInt(temp[0]))) / 2.0);
                Platform.runLater(() -> {
                    setlocalRange((target - 50), (target + 49));
                });
            } else {
                Platform.runLater(() -> {
                    setlocalRange(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]));
                });
            }
        } else {
            int target = Integer.parseInt(data.getXValue());
            Platform.runLater(() -> {
                setlocalRange(target - 50, (target + 49));
            });
        }
    }

    @FXML
    private void redraw() {
        if (columnCountInterface != null) {
            columnCountInterface.redraw();
        }
    }

    @FXML
    private void resetRange() {
        Platform.runLater(() -> {
            tfFrom.setText("");
            tfTo.setText("");
            redraw();
        });
    }

    @FXML
    private void setlocalRange(int from, int to) {
        Platform.runLater(() -> {
            this.tfFrom.setText((from == -1) ? "" : from + "");
            this.tfTo.setText((to == -1) ? "" : to + "");
            redraw();
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void createChart(double maxValue) {
        // System.out.println("createChart");
        this.chartPane.getChildren().clear();
        CategoryAxis xAxis = new CategoryAxis();
        if (maxValue <= 0) {
            chart = new BarChart(xAxis, new NumberAxis());
            chart.setTitle("No data available");
            chart.setTitleSide(Side.TOP);
        } else if (this.cbYAxisLogScale.isSelected()) {
            chart = new BarChart(xAxis, new LogarithmicAxis(0.4, maxValue, true));
            chart.getYAxis().setLabel("occurrence (log)");
        } else {
            chart = new BarChart(xAxis, new XNumberAxis(0.0, maxValue));
            chart.getYAxis().setLabel("occurrence (lin)");
        }
        chart.getXAxis().setAnimated(false);
        if (columnCountInterface != null) {
            chart.getXAxis().setLabel(columnCountInterface.getLabel());
        } else {
            chart.getXAxis().setLabel("value");
        }
        chart.getStylesheets().clear();
        chart.getStylesheets().add(getClass().getResource("histogram.css").toExternalForm());
        chart.getYAxis().setAnimated(false);
        chart.getYAxis().setTickLength(1);
        chart.setCategoryGap(0);
        chart.setBarGap(1);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        MenuItem cmItem1 = new MenuItem("Save as image");
        cmItem1.setOnAction((ActionEvent e) -> {
            ImageExport.exportPaneToPng(chartPane, null);
        });
        final ContextMenu cm = new ContextMenu(cmItem1);
        chartPane.setOnMouseClicked((event) -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                cm.show(chartPane, event.getScreenX(), event.getScreenY());
            }
        });
        AnchorPane.setBottomAnchor(chart, 0.0);
        AnchorPane.setTopAnchor(chart, 0.0);
        AnchorPane.setLeftAnchor(chart, 0.0);
        AnchorPane.setRightAnchor(chart, 0.0);
        this.chartPane.getChildren().add(chart);
    }

    private void init() {
        logScaleSettings.managedProperty().bind(logScaleSettings.visibleProperty());
        rangeSettings.managedProperty().bind(rangeSettings.visibleProperty());
        cbXAxisLogScale.managedProperty().bind(cbXAxisLogScale.visibleProperty());
        cbIgnoreNullValues.managedProperty().bind(cbIgnoreNullValues.visibleProperty());

        cbIgnoreNullValues.selectedProperty().addListener((ov, before, after) -> {
            this.redraw();
        });
        cbXAxisLogScale.selectedProperty().addListener((ov, before, after) -> {
            this.redraw();
        });
        cbYAxisLogScale.selectedProperty().addListener((ov, before, after) -> {
            this.redraw();
        });
    }

    private void testForReady() {
        if (isInitialized == true) {
            this.init();
        }
    }

    public void initView() {
        this.redraw();
    }

    /**
     *
     * @param xAxisLogScale show x axis log scale
     * @param yAxisLogScale show y axis log scale
     * @param ignoreNullValues show ignore null values
     * @param showRange show range
     */
    public void setVisibleElements(
        boolean xAxisLogScale,
        boolean yAxisLogScale,
        boolean ignoreNullValues,
        boolean showRange
    ) {
        double height = 0.0; // button

        cbXAxisLogScale.setVisible(xAxisLogScale);

        if (yAxisLogScale) {
            logScaleSettings.setVisible(true);
            logScaleSettings.setMaxHeight(35.0);
            logScaleSettings.setPrefHeight(35.0);
            height += 35.0;
        } else {
            logScaleSettings.setMaxHeight(0.0);
            logScaleSettings.setVisible(false);
        }

        cbIgnoreNullValues.setVisible(ignoreNullValues);

        if (showRange) {
            rangeSettings.setVisible(true);
            rangeSettings.setMaxHeight(35.0);
            rangeSettings.setPrefHeight(35.0);
            height += 35.0;
        } else {
            rangeSettings.setMaxHeight(0.0);
            rangeSettings.setVisible(false);
        }

        baseLayoutGrid.getRowConstraints().get(0).setMaxHeight(height);
        baseLayoutGrid.getRowConstraints().get(0).setPrefHeight(height);
    }

    /**
     *
     * @param columnCountInterface
     */
    public void setCallback(ColumnCountInterface columnCountInterface) {
        this.columnCountInterface = columnCountInterface;
        if (columnCountInterface != null) {
            String label = columnCountInterface.getLabel();
            cbXAxisLogScale.setText(label + " log-scale");
            cbIgnoreNullValues.setText("Ignore " + label + " with value 0");
        }
    }

    /**
     *
     * @param fileName
     */
    public void saveChartToFile(
        String fileName
    ) {
        ImageExport.exportPaneToPng(this.chartPane, fileName);
    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }
}
