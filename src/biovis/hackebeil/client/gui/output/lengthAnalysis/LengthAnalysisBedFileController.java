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
package biovis.hackebeil.client.gui.output.lengthAnalysis;

import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.client.io.ImageExport;
import biovis.hackebeil.client.utilities.ChartUtilities;
import biovis.hackebeil.common.data.DataFile;
import biovislib.javafx.chart.LogarithmicAxis;
import biovislib.javafx.chart.XNumberAxis;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
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
import javafx.scene.layout.VBox;

/**
 * @author Alrik Hausdorf
 *
 */
public class LengthAnalysisBedFileController
    implements LengthAnalysisControllerInterface {

    private static final Logger log = Logger.getLogger(LengthAnalysisBedFileController.class.getName());

    private final static int DEFAULT_BLOCK_COUNT = 100;

    private ClientConfiguration clientConfiguration;
    private LengthAnalysisRootController parent;
    private DataFile bedFile;
    private SortedMap<Integer, Integer> modifiedSegmentLengths;

    private Boolean isInitialized = false;

    private enum Type {
        CHROME, THICK, BLOCK
    }

    private int xAxisBlockCount = DEFAULT_BLOCK_COUNT;

    private AnchorPane anchorPane;

    @FXML
    private AnchorPane paneLengthAnalysisBedFile;
    @FXML
    private CheckBox cbXAxisLogScale;
    @FXML
    private CheckBox cbYAxisLogScale;
    @FXML
    private Button btnReset;
    @FXML
    private TextField tfFrom;
    @FXML
    private TextField tfTo;

    @FXML
    private VBox chromChartBox;
    @FXML
    private AnchorPane chromChartPane;
    @FXML
    private BarChart<String, Integer> chromChart;

    @FXML
    private VBox thickChartBox;
    @FXML
    private AnchorPane thickChartPane;
    @FXML
    private BarChart<String, Integer> thickChart;

    @FXML
    private VBox blockChartBox;
    @FXML
    private AnchorPane blockChartPane;
    @FXML
    private BarChart<String, Integer> blockChart;

    private boolean ignore = false;

    public LengthAnalysisBedFileController() {
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
    public static LengthAnalysisBedFileController getInstance() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LengthAnalysisBedFileController.class.getResource("LengthAnalysisBedFile.fxml"));
            AnchorPane anchorPane = loader.load();
            LengthAnalysisBedFileController lengthAnalysisBedFileController = loader.<LengthAnalysisBedFileController>getController();
            lengthAnalysisBedFileController.setAnchorPane(anchorPane);
            return lengthAnalysisBedFileController;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param clientConfiguration
     * @param parent
     * @param referenceDataFile
     * @param modifiedSegmentLengths
     * @return  rootPane
     */
    public AnchorPane loadView(
        ClientConfiguration clientConfiguration,
        LengthAnalysisRootController parent,
        DataFile referenceDataFile,
        SortedMap<Integer, Integer> modifiedSegmentLengths
    ) {
        this.clientConfiguration = clientConfiguration;
        this.parent = parent;
        this.bedFile = referenceDataFile;
        this.modifiedSegmentLengths = modifiedSegmentLengths;
        testForReady();

        return anchorPane;
    }

    public void initialize() {
        isInitialized = true;
        testForReady();
    }

    /**
     *
     * @param type
     * @param lengths
     */
    private void redrawChart(
        Type type,
        SortedMap<Integer, Integer> lengths
    ) {
        // Start Computing
        Platform.runLater(() -> {
            long from = 0;
            if (tfFrom.getText().equals("")) {
                if (modifiedSegmentLengths.firstKey() < lengths.firstKey()) {
                    from = modifiedSegmentLengths.firstKey();
                } else {
                    from = lengths.firstKey();
                }
            } else {
                from = Integer.parseInt(tfFrom.getText());
            }
            long to = 1000000;
            if (tfTo.getText().equals("")) {
                if (modifiedSegmentLengths.lastKey() > lengths.lastKey()) {
                    to = modifiedSegmentLengths.lastKey();
                } else {
                    to = lengths.lastKey();
                }
            } else {
                to = Integer.parseInt(tfTo.getText());
            }
            // Calc Blockstarts
            long[] blockstarts = ChartUtilities.createBins(from, to,
                                                           xAxisBlockCount,
                                                           cbXAxisLogScale.isSelected());

            int maxValue = 0;

            // Series 1
            XYChart.Series<String, Integer> series1 = new XYChart.Series<>();
            List<XYChart.Data<String, Integer>> chartData = ChartUtilities.createChartDataforBins(lengths,
                                                                                                  blockstarts,
                                                                                                  from,
                                                                                                  to);
            for (XYChart.Data<String, Integer> a : chartData) {
                series1.getData().add(a);
                if (a.getYValue() > maxValue) {
                    maxValue = a.getYValue();
                }
            }

            // Series 2
            XYChart.Series<String, Integer> series2 = new XYChart.Series<>();
            chartData = ChartUtilities.createChartDataforBins(modifiedSegmentLengths,
                                                              blockstarts,
                                                              from,
                                                              to);

            for (XYChart.Data<String, Integer> a : chartData) {
                series2.getData().add(a);
                if (a.getYValue() > maxValue) {
                    maxValue = a.getYValue();
                }
            }

            // Maximum as final
            final int finalMax = maxValue;
            Platform.runLater(() -> {
                BarChart<String, Integer> thisChart = chromChart;
                if (null != type) {
                    switch (type) {
                        case CHROME:
                            chromChart = createChart(chromChartPane, finalMax);
                            thisChart = chromChart;
                            break;
                        case THICK:
                            thickChart = createChart(thickChartPane, finalMax);
                            thisChart = thickChart;
                            break;
                        default:
                            blockChart = createChart(blockChartPane, finalMax);
                            thisChart = blockChart;
                            break;
                    }
                }
                // createChart(chart,finalMax, chartPane, xAxisLogScale,
                // yAxisLogScale);
                thisChart.setLegendVisible(false);
                thisChart.getStylesheets().clear();
                thisChart.getStylesheets().add(getClass().getResource("histogram.css").toExternalForm());

                thisChart.getData().clear();
                thisChart.getData().add(series1);
                thisChart.getData().add(series2);

                for (Series<String, Integer> serie : thisChart.getData()) {
                    for (XYChart.Data<String, Integer> item : serie.getData()) {
                        Tooltip tooltip = ChartUtilities.createTooltip(item);
                        Tooltip.install(item.getNode(), tooltip);
                        item.getNode().setOnMousePressed((MouseEvent event) -> {
                            if (event.getClickCount() > 1) {
                                if (item.getXValue().contains("-")) {
                                    String[] temp = item.getXValue().split("-");
                                    if (Integer.parseInt(temp[1]) - Integer.parseInt(temp[0]) < DEFAULT_BLOCK_COUNT) {
                                        int target = Integer.parseInt(temp[0]) + (int) Math.round(
                                            (Math.round(Integer.parseInt(temp[1]) - Integer.parseInt(temp[0])))
                                            / 2.0);
                                        Platform.runLater(() -> {
                                            setlocalRange((target - 50), (target + 49));
                                        });
                                    } else {
                                        Platform.runLater(() -> {
                                            setlocalRange(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]));
                                        });
                                    }
                                } else {
                                    int target = Integer.parseInt(item.getXValue());
                                    Platform.runLater(() -> {
                                        setlocalRange((target - 50), (target + 49));
                                    });
                                }
                            }
                        });
                    }
                }
            });

        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private BarChart<String, Integer> createChart(
        AnchorPane chartPane,
        int maxValue
    ) {
        chartPane.getChildren().clear();

        BarChart<String, Integer> chart;
        CategoryAxis xAxis = new CategoryAxis();
        if (maxValue <= 0) {
            chart = new BarChart(xAxis, new NumberAxis());
            chart.setTitle("No data available");
            chart.setTitleSide(Side.TOP);
        } else if (cbYAxisLogScale.isSelected()) {
            chart = new BarChart(xAxis, new LogarithmicAxis(0.4, maxValue, true));
            chart.getYAxis().setLabel("occurrence (log)");
        } else {
            chart = new BarChart(xAxis, new XNumberAxis(0.0, maxValue));
            chart.getYAxis().setLabel("occurrence (lin)");
        }
        chart.getXAxis().setAnimated(false);
        if (cbXAxisLogScale.isSelected()) {
            chart.getXAxis().setLabel("length (log)");
        } else {
            chart.getXAxis().setLabel("length (lin)");
        }

        chart.getYAxis().setAnimated(false);
        chart.setCategoryGap(0);
        chart.setBarGap(1);
        chart.setAnimated(false);
        MenuItem cmItem1 = new MenuItem("Save as image");
        cmItem1.setOnAction((ActionEvent e) -> {
            ImageExport.exportPaneToPng(chartPane, null);
        });
        final ContextMenu cm = new ContextMenu(cmItem1);
        chart.setOnMouseClicked((event) -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                cm.show(chart, event.getScreenX(), event.getScreenY());
            }
        });
        AnchorPane.setBottomAnchor(chart, 0.0);
        AnchorPane.setTopAnchor(chart, 50.0);
        AnchorPane.setLeftAnchor(chart, 0.0);
        AnchorPane.setRightAnchor(chart, 0.0);

        chartPane.getChildren().add(chart);

        return chart;
    }

    @FXML
    private void redrawChromChart() {
        SortedMap<Integer, Integer> lengths = bedFile.getChromLengths();
        if (lengths == null || lengths.size() <= 0) {
            chromChartBox.setVisible(false);
            chromChartBox.setManaged(false);
            return;
        }
        chromChartBox.setVisible(true);
        chromChartBox.setManaged(true);
        redrawChart(Type.CHROME, lengths);
    }

    @FXML
    private void redrawThickChart() {
        SortedMap<Integer, Integer> lengths = bedFile.getThickLengths();
        if (lengths == null || lengths.size() <= 0) {
            thickChartBox.setVisible(false);
            thickChartBox.setManaged(false);
            return;
        }
        thickChartBox.setVisible(true);
        thickChartBox.setManaged(true);
        redrawChart(Type.THICK, lengths);
    }

    @FXML
    private void redrawBlockChart() {
        SortedMap<Integer, Integer> lengths = bedFile.getBlockLengths();
        if (lengths == null || lengths.size() <= 0) {
            blockChartBox.setVisible(false);
            blockChartBox.setManaged(false);
            return;
        }
        blockChartBox.setVisible(true);
        blockChartBox.setManaged(true);
        redrawChart(Type.BLOCK, lengths);
    }

    @FXML
    public void redraw() {
        redrawChromChart();
        redrawThickChart();
        redrawBlockChart();
    }

    private void init() {
        cbXAxisLogScale.selectedProperty().addListener((ov) -> {
            if (ignore) {
                return;
            }
            setLocalXAxisLogScale();
        });
        cbYAxisLogScale.selectedProperty().addListener((ov) -> {
            if (ignore) {
                return;
            }
            setLocalYAxisLogScale();
        });
    }

    @Override
    public void setXAxisLogScale(boolean enable) {
        ignore = true;
        this.cbXAxisLogScale.setSelected(enable);
        ignore = false;
        redraw();
    }

    @Override
    public void setYAxisLogScale(boolean enable) {
        ignore = true;
        this.cbYAxisLogScale.setSelected(enable);
        ignore = false;
        redraw();
    }

    private void setLocalXAxisLogScale() {
        redraw();
    }

    private void setLocalYAxisLogScale() {
        redraw();
    }

    @Override
    public void setRange(int from, int to) {
        this.tfFrom.setText((from == -1) ? "" : from + "");
        this.tfTo.setText((to == -1) ? "" : to + "");
        btnReset.setDisable(false);
        redraw();
    }

    @FXML
    private void updateLocalRange() {
        int from = -1;
        try {
            from = Integer.parseInt(tfFrom.getText());
        } catch (NumberFormatException e) {
            from = -1;
        }
        int to = -1;
        try {
            to = Integer.parseInt(tfTo.getText());
        } catch (NumberFormatException e) {
            to = -1;
        }
        setlocalRange(from, to);
    }

    private void setlocalRange(int from, int to) {
        this.tfFrom.setText((from == -1) ? "" : from + "");
        this.tfTo.setText((to == -1) ? "" : to + "");
        btnReset.setDisable(false);
        redraw();
    }

    @Override
    public void resetRange() {
        tfFrom.setText("");
        tfTo.setText("");
        redraw();
    }

    @FXML
    private void resetLocalRange() {
        SortedMap<Integer, Integer> lengths = bedFile.getChromLengths();
        if (lengths == null || lengths.isEmpty()) {
            return;
        }

        Platform.runLater(() -> {
            tfFrom.setText("");
            tfTo.setText("");
            redraw();
        });
    }

    private void testForReady() {
        if (clientConfiguration != null
            && isInitialized == true) {
            this.init();
        }
    }

    @Override
    public void initView() {
        redraw();
    }

    /**
     *
     * @param path
     */
    public void saveChromChartToFile(String path) {
        if (chromChartBox.isVisible()) {
            ImageExport.exportPaneToPng(chromChartPane, path);
        }
    }

    /**
     *
     * @param path
     */
    public void saveThickChartToFile(String path) {
        if (thickChartBox.isVisible()) {
            ImageExport.exportPaneToPng(thickChartPane, path);
        }
    }

    /**
     *
     * @param path
     */
    public void saveBlockChartToFile(String path) {
        if (blockChartBox.isVisible()) {
            ImageExport.exportPaneToPng(blockChartPane, path);
        }
    }

    /**
     *
     * @param string
     */
    public void saveChartToFile(String string) {
        saveBlockChartToFile(string + "_" + bedFile.getDataSetName() + "_blocks");
        saveThickChartToFile(string + "_" + bedFile.getDataSetName() + "_thicks");
        saveChromChartToFile(string + "_" + bedFile.getDataSetName() + "_chromosomes");
    }
}
