/**
 *  Copyright 2016 Dirk Zeckzer
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

import java.io.IOException;
import java.util.logging.Logger;

import biovis.hackebeil.client.io.ImageExport;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * @author Dirk Zeckzer
 *
 */
public class CountHeatmapController {

    private static final Logger log = Logger.getLogger(CountHeatmapController.class.getName());

    private static final Color HEATMAP_COLOR = Color.hsb(315.0, 1.0, 1.0);

    private static final int HEADER_WIDTH = 400;
    private static final int HEADER_HEIGHT = 400;
    private static final int CELL_WIDTH = 100;
    private static final int CELL_HEIGHT = 100;

    private Boolean isInitialized = false;

    private AnchorPane rootPane;

    @FXML
    private Label lbNumberOfEntries;
    @FXML
    private CheckBox logColorScale;
    @FXML
    private AnchorPane anchorHeatmap;

    private String[] rowNames;
    private String[] columnNames;
    private int[][] matrix;
    private int nonNullEntries;
    private int maxValue;

    public CountHeatmapController() {
    }

    private void setRootPane(
        AnchorPane pane
    ) {
        this.rootPane = pane;
    }

    public static CountHeatmapController getInstance() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(CountHeatmapController.class.getResource("CountHeatmap.fxml"));
            AnchorPane rootPane = loader.load();
            CountHeatmapController segmentPairController = loader.<CountHeatmapController>getController();
            segmentPairController.setRootPane(rootPane);
            return segmentPairController;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @return
     */
    public AnchorPane loadView() {
        testForReady();

        return rootPane;
    }

    /**
     * Initialization, called by FXMLLoader.
     */
    public void initialize() {
        isInitialized = true;
        testForReady();
    }

    /**
     *
     */
    public void redraw() {
        if (matrix == null) {
            return;
        }

        Platform.runLater(() -> {
            redrawHeatMap();
        });
    }

    private void redrawHeatMap() {
        lbNumberOfEntries.setText(nonNullEntries + "");

        anchorHeatmap.getChildren().clear();
        GridPane heatmap = new GridPane();

        int lines = matrix.length + 1;
        int cols = matrix[0].length + 1;

        // cols
        ColumnConstraints colConstr = new ColumnConstraints();
        colConstr.setMinWidth(CELL_WIDTH);

        // rows
        RowConstraints rowConstr = new RowConstraints();
        rowConstr.setMinHeight(CELL_HEIGHT);

        for (int x = 0; x < cols; x++) {
            heatmap.getColumnConstraints().add(colConstr);
        }
        for (int y = 0; y < lines; y++) {
            heatmap.getRowConstraints().add(rowConstr);

            for (int x = 0; x < cols; x++) {
                StackPane element = new StackPane();

                int xElement = x - 1;

                int yElement = y - 1;
                element.setAlignment(Pos.CENTER);
                Label label = new Label();
                if (x == 0 || y == 0) {
                    element.setBackground(new Background(
                        new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, new Insets(1, 1, 1, 1))));
                }
                if (x == 0 && y == 0) {
                } else if (x == 0) {
                    label.setText(rowNames[yElement]);
                    element.getChildren().add(label);

                    // Tooltip
                    Tooltip tp = new Tooltip(rowNames[yElement].replaceAll("[\n\r]", " "));
                    Tooltip.install(element, tp);
                } else if (y == 0) {
                    label.setText(columnNames[xElement]);
                    element.getChildren().add(label);

                    // Tooltip
                    Tooltip tp = new Tooltip(columnNames[xElement].replaceAll("[\n\r]", " "));
                    Tooltip.install(element, tp);
                } else {
                    double value;
                    if (logColorScale.isSelected()) {
                        value = Math.log(matrix[yElement][xElement] + 1) / Math.log(maxValue + 1);
                    } else {
                        value = matrix[yElement][xElement] / maxValue;
                    }
                    if (value > 0.5) {
                        label.setTextFill(Color.WHITE);
                    }
                    label.setText(matrix[yElement][xElement] + "");
                    element.getChildren().add(label);
                    Tooltip tp = new Tooltip(rowNames[yElement].replaceAll("[\n\r]", " ")
                                             + "-" + columnNames[xElement].replaceAll("[\n\r]", " ")
                                             + ": " + matrix[yElement][xElement]);
                    Tooltip.install(element, tp);
                    element.setBackground(
                        new Background(
                            new BackgroundFill(
                                getColorForValue((double) matrix[yElement][xElement], (double) maxValue,
                                                 logColorScale.isSelected()),
                                CornerRadii.EMPTY, new Insets(1, 1, 1, 1))));
                }

                heatmap.add(element, x, y);
            }
        }

        heatmap.setStyle("-fx-padding: 1;" + "-fx-hgap: 1; -fx-vgap: 1;");
        AnchorPane.setBottomAnchor(heatmap, 0.0);
        AnchorPane.setLeftAnchor(heatmap, 0.0);
        AnchorPane.setRightAnchor(heatmap, 0.0);
        AnchorPane.setTopAnchor(heatmap, 0.0);
        anchorHeatmap.getChildren().add(heatmap);
        MenuItem cmItem1 = new MenuItem("Save as image");
        cmItem1.setOnAction((ActionEvent e) -> {
            ImageExport.exportPaneToPng(anchorHeatmap, null);
        });
        final ContextMenu cm = new ContextMenu(cmItem1);
        anchorHeatmap.setOnMouseClicked((event) -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                cm.show(anchorHeatmap, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private Color getColorForValue(Double baseValue, Double max, boolean log) {
        Double value;
        if (log) {

            value = Math.log(baseValue + 1) / Math.log(max + 1);
        } else {
            value = baseValue / max;

        }
        if (value < -1.0 || value > 1.0 || value.isNaN()) {
            return Color.BLACK;
        }
        if (value >= 0.0) {

            return Color.hsb(HEATMAP_COLOR.getHue(), value, 1.0);
        } else {
            return Color.hsb(HEATMAP_COLOR.getHue(), (-1.0 * value), 1.0);
        }
    }

    private void init() {
        logColorScale.selectedProperty().addListener((ov, before, after) -> {
            redrawHeatMap();
        });
    }

    private void testForReady() {
        if (isInitialized == true) {
            init();
        }
    }

    /**
     *
     * @param rowNames
     * @param columnNames
     * @param matrix
     * @param nonNullEntries
     * @param maxValue
     */
    public void setHeatmapData(
        String[] rowNames,
        String[] columnNames,
        int[][] matrix,
        int nonNullEntries,
        int maxValue
    ) {
        this.rowNames = rowNames;
        this.columnNames = columnNames;
        this.matrix = matrix;
        this.nonNullEntries = nonNullEntries;
        this.maxValue = maxValue;

        redraw();
    }

    private void saveHeatMapToFile(String path) {
        ImageExport.exportPaneToPng(this.anchorHeatmap, path);
    }

    /**
     *
     * @param path
     */
    public void exportToPng(String path) {
        path += "-Heatmap";
        this.saveHeatMapToFile(path);
    }
}
