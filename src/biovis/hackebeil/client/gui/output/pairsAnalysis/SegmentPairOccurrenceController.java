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
package biovis.hackebeil.client.gui.output.pairsAnalysis;

import java.io.IOException;
import java.util.logging.Logger;

import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.client.io.ImageExport;
import java.text.DecimalFormat;
import java.util.Map;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
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
public class SegmentPairOccurrenceController {

    private static final Logger log = Logger.getLogger(SegmentPairOccurrenceController.class.getName());

    private static final DecimalFormat formatter = new DecimalFormat("#.#####");

    private static final int HEADER_WIDTH = 400;
    private static final int HEADER_HEIGHT = 400;
    private static final int CELL_WIDTH = 100;
    private static final int CELL_HEIGHT = 100;

    private ClientConfiguration clientConfiguration;
    private Boolean isInitialized = false;

    private TabPane rootPane;

    @FXML
    private TabPane tabs;

    @FXML
    private Tab generalTab;
    @FXML
    private Label lbNumberOfSegmentPairs;
    @FXML
    private AnchorPane anchorHeatmap;

    private Map<String, Double> segmentPairOccurrenceData;

    int numberOfReferences;
    int numberOfCodes;

    private double[][] codeCountMatrix;
    private double codeCountMax;

    public SegmentPairOccurrenceController() {
    }

    /**
     *
     * @param pane
     */
    private void setRootPane(
        TabPane pane
    ) {
        this.rootPane = pane;
    }

    /**
     *
     * @return
     */
    public static SegmentPairOccurrenceController getInstance() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SegmentPairOccurrenceController.class.getResource("SegmentPairOccurrence.fxml"));
            TabPane rootPane = loader.load();
            SegmentPairOccurrenceController segmentPairController = loader.<SegmentPairOccurrenceController>getController();
            segmentPairController.setRootPane(rootPane);
            return segmentPairController;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param clientConfiguration
     * @return
     */
    public TabPane loadView(
        ClientConfiguration clientConfiguration
    ) {
        this.clientConfiguration = clientConfiguration;
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
    private void redraw() {
        if (segmentPairOccurrenceData == null) {
            return;
        }

        Platform.runLater(() -> {
            calcMatrix();
            lbNumberOfSegmentPairs.setText(segmentPairOccurrenceData.size() + "");
            redrawHeatMap();
        });
    }

    private void redrawHeatMap() {
        GridPane heatmap = new GridPane();
        anchorHeatmap.getChildren().clear();
        double[][] matrix = codeCountMatrix;
        double maxValue = codeCountMax;

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
                    label.setText("First\ncode:\n" + yElement);
                    element.getChildren().add(label);

                    // Tooltip
                    Tooltip tp = new Tooltip("First code:" + yElement);
                    Tooltip.install(element, tp);
                } else if (y == 0) {
                    label.setText("Second\ncode:\n" + (xElement) + "");
                    element.getChildren().add(label);

                    // Tooltip
                    Tooltip tp = new Tooltip("Second code:" + xElement);
                    Tooltip.install(element, tp);
                } else {
                    Tooltip tp = new Tooltip();
                    double value = matrix[yElement][xElement];
                    if (value > 0.0) {
                        value = Math.log(value) / Math.log(maxValue);
                        label.setText(formatter.format(value) + "");
                        tp.setText("from " + (yElement)
                                   + " to " + (xElement)
                                   + " = " + value);
                        if (value > 0.5 || value < -0.5) {
                            label.setTextFill(Color.WHITE);
                        }

                        element.setBackground(
                            new Background(
                                new BackgroundFill(
                                    getColorForValue(value),
                                    CornerRadii.EMPTY, new Insets(1, 1, 1, 1))));
                    } else {
                        label.setText("");
                        element.setBackground(
                            new Background(
                                new BackgroundFill(
                                    Color.BLACK,
                                    CornerRadii.EMPTY, new Insets(1, 1, 1, 1))));
                    }
                    element.getChildren().add(label);
                    Tooltip.install(element, tp);
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

    private Color getColorForValue(Double value) {
        if (value.isNaN()) {
            return Color.BLACK;
        }
        if (value >= 0.0) {
            if (value > 1.0) {
                return Color.hsb(0.0, 1.0, 1.0);
            } else {
                return Color.hsb(0.0, value, 1.0);
            }
        } else {
            if (value < -1.0) {
                return Color.hsb(Color.BLUE.getHue(), 1.0, 1.0);
            } else {
                return Color.hsb(Color.BLUE.getHue(), (-1.0 * value), 1.0);
            }
        }
    }

    private void calcMatrix() {
        if (this.segmentPairOccurrenceData == null) {
            return;
        }

        codeCountMax = 0;

        numberOfReferences = clientConfiguration.getReferenceDataList().size();
        numberOfCodes = (int) Math.pow(2, numberOfReferences);
        codeCountMatrix = new double[numberOfCodes][numberOfCodes];
        for (int i = 0;
             i < codeCountMatrix.length;
             ++i) {
            for (int j = 0;
                 j < codeCountMatrix[i].length;
                 ++j) {
                String key = i + "-" + j;
                Double value = segmentPairOccurrenceData.get(key);
                if (value != null) {
                    codeCountMatrix[i][j] = value;
                    if (value > codeCountMax) {
                        codeCountMax = value;
                    }
                } else {
                    codeCountMatrix[i][j] = 0;
                }
            }
        }
    }

    public void invalidateTabs() {

    }

    private void init() {
    }

    private void testForReady() {
        if (clientConfiguration != null
            && isInitialized == true) {
            init();
        }
    }

    public void setSegmentPairOccurrenceData(Map<String, Double> segmentPairOccurrenceData) {
        this.segmentPairOccurrenceData = segmentPairOccurrenceData;
        redraw();
    }

    private void saveHeatMapToFile(String path) {
        ImageExport.exportPaneToPng(this.anchorHeatmap, path);
    }

    public void exportToPng(String path) {
        path += "-segmentPairOccurrence";
        this.saveHeatMapToFile(path + "_");
    }
}
