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
package biovis.hackebeil.client.gui.output.fateOfCode;

import java.io.IOException;
import java.util.List;

import biovis.hackebeil.client.io.ImageExport;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * @author Alrik Hausdorf, Dirk Zeckzer
 *
 */
public class FateOfCodeResultController {

    private static final int HEADER_WIDTH = 400;
    private static final int HEADER_HEIGHT = 400;
    private static final int CELL_WIDTH = 100;
    private static final int CELL_HEIGHT = 100;

    private TitledPane rootPane;

    @FXML
    private TitledPane resultPane;
    @FXML
    private GridPane resultGridPane;

    private Boolean isInitialized = false;

    private List<int[][]> results = null;

    public FateOfCodeResultController() {
    }

    /**
     *
     * @param pane
     */
    private void setRoot(
        TitledPane pane
    ) {
        this.rootPane = pane;
    }

    /**
     *
     * @return
     */
    public static FateOfCodeResultController getInstance() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FateOfCodeResultController.class.getResource("FateOfCodeResult.fxml"));
            TitledPane rootPane = loader.load();
            FateOfCodeResultController controller = loader.<FateOfCodeResultController>getController();
            controller.setRoot(rootPane);
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @return
     */
    public TitledPane loadView() {
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

    @FXML
    public void click() {
    }

    /**
     *
     * @param baseValue
     * @param max
     * @param log
     * @return
     */
    private Color getColorForValue(
        Double baseValue,
        Double max,
        boolean log
    ) {
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

            return Color.hsb(Color.BLUE.getHue(), value, 1.0);
        } else {
            return Color.hsb(Color.RED.getHue(), (-1.0 * value), 1.0);
        }
    }

    /**
     *
     * @param results
     * @param selectionSize
     * @param isLogColorScale
     */
    public void setFateOfCodeResults(
        List<int[][]> results,
        int selectionSize,
        boolean isLogColorScale
    ) {
        this.results = results;
        drawMatrix(selectionSize, isLogColorScale);
    }

    /**
     *
     */
    public void clearMatrix() {
        // clear the matrix
        Platform.runLater(() -> {
            resultGridPane.getChildren().clear();

            // cols
            resultGridPane.getColumnConstraints().clear();

            // rows
            resultGridPane.getRowConstraints().clear();
        });
    }

    /**
     *
     * @param selectionSize
     * @param isLogColorScale
     */
    private void drawMatrix(
        int selectionSize,
        boolean isLogColorScale
    ) {
        if (results == null) {
            return;
        }

        int[] maxValues = getMaxValues(results, selectionSize);

        // draw the matrix
        Platform.runLater(() -> {
            // clearGridPane
            resultGridPane.getChildren().clear();

            GridPane heatmap = createRowHeader(results.get(0).length);
            resultGridPane.add(heatmap, 0, 0);

            int resultCol = 1;
            for (int[][] matrix : results) {
                heatmap = createHeatmap(matrix,
                                        maxValues[resultCol - 1],
                                        isLogColorScale);
                resultGridPane.add(heatmap, resultCol, 0);
                resultCol++;
            }

            MenuItem cmItem1 = new MenuItem("Save as image");
            cmItem1.setOnAction((ActionEvent e) -> {
                ImageExport.exportPaneToPng(resultGridPane, null);
            });

            final ContextMenu cm = new ContextMenu(cmItem1);
            resultGridPane.setOnMouseClicked((event) -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    cm.show(resultGridPane, event.getScreenX(), event.getScreenY());
                }
            });
        });
    }

    /**
     *
     * @param results
     * @param selectionSize
     * @return
     */
    private int[] getMaxValues(
        List<int[][]> results,
        int selectionSize
    ) {
        int[] maxValues = new int[selectionSize];
        int[][] matrix;
        int[] row;

        for (int resultNumber = 0; resultNumber < results.size(); ++resultNumber) {
            matrix = results.get(resultNumber);
            for (int fromCode = 0; fromCode < matrix.length; ++fromCode) {
                row = matrix[fromCode];
                for (int toCode = 0; toCode < row.length; ++toCode) {
                    maxValues[resultNumber] = Math.max(matrix[fromCode][toCode],
                                                       maxValues[resultNumber]);
                }
            }
        }
        return maxValues;
    }

    /**
     *
     * @param matrix
     * @param maxValues
     * @param isLogColorScale
     * @return
     */
    private GridPane createHeatmap(
        int[][] matrix,
        int maxValues,
        boolean isLogColorScale
    ) {
        GridPane heatmap = new GridPane();
        int heatmapRows = matrix.length + 1;
        int heatmapColumns = matrix.length;

        // cols
        ColumnConstraints colConstr = new ColumnConstraints();
        colConstr.setMinWidth(CELL_WIDTH);

        // rows
        RowConstraints rowConstr = new RowConstraints();
        rowConstr.setMinHeight(CELL_HEIGHT);

        for (int row = 0; row < heatmapRows; row++) {
            heatmap.getRowConstraints().add(rowConstr);
            if (row != 0) {
                heatmap.getColumnConstraints().add(colConstr);
            }

            for (int column = 0; column < heatmapColumns; column++) {
                StackPane element = new StackPane();

                int columnElement = column;
                int rowElement = row - 1;
                element.setAlignment(Pos.CENTER);
                Label label = new Label();
                if (row == 0) {
                    label.setText("To: " + columnElement);
                    element.getChildren().add(label);

                    // Tooltip
                    Tooltip tp = new Tooltip("To:" + columnElement);
                    Tooltip.install(element, tp);
                } else {
                    label.setText(matrix[rowElement][columnElement] + "");
                    element.getChildren().add(label);
                    Tooltip tp = new Tooltip("from " + (rowElement) + " to " + (columnElement) + " = " + matrix[rowElement][columnElement]);
                    Tooltip.install(element, tp);
                    double value;
                    if (isLogColorScale) {
                        value = Math.log(matrix[rowElement][columnElement] + 1) / Math.log(maxValues + 1);
                    } else {
                        value = matrix[rowElement][columnElement] / maxValues;
                    }
                    if (value > 0.5) {
                        label.setTextFill(Color.WHITE);
                    }
                    element.setBackground(
                        new Background(
                            new BackgroundFill(
                                getColorForValue((double) matrix[rowElement][columnElement],
                                                 (double) maxValues,
                                                 isLogColorScale),
                                CornerRadii.EMPTY, new Insets(1, 1, 1, 1)
                            )
                        )
                    );
                }

                heatmap.add(element, column, row);
            }
        }
        heatmap.setStyle("-fx-background-color: white");

        return heatmap;
    }

    /**
     *
     * @param heatmapRows
     * @return
     */
    private GridPane createRowHeader(
        int heatmapRows
    ) {
        GridPane heatmap = new GridPane();

        // cols
        ColumnConstraints colConstr = new ColumnConstraints();
        colConstr.setMinWidth(CELL_WIDTH);

        // rows
        RowConstraints rowConstr = new RowConstraints();
        rowConstr.setMinHeight(CELL_HEIGHT);

        heatmap.getColumnConstraints().add(colConstr);

        for (int row = 0; row < heatmapRows + 1; row++) {
            heatmap.getRowConstraints().add(rowConstr);
            StackPane element = new StackPane();
            element.setAlignment(Pos.CENTER);
            int rowElement = row - 1;
            Label label = new Label();
            if (row == 0) {
                label.setText("");
            } else {
                label.setText("From: " + rowElement);
                element.getChildren().add(label);

                // Tooltip
                Tooltip tp = new Tooltip("From:" + rowElement);
                Tooltip.install(element, tp);

            }
            heatmap.add(element, 0, row);
        }

        heatmap.setStyle("-fx-background-color: white");

        return heatmap;
    }

    private void init() {
    }

    private void testForReady() {
        if (isInitialized == true) {
            this.init();
        }
    }

    /**
     *
     * @param path
     */
    public void saveMatrixToFile(String path) {
        path += "-fateOfCode";
        if (resultGridPane.getChildren().size() <= 0) {
            return;
        }
        ImageExport.exportPaneToPng(this.resultGridPane, path);
    }
}
