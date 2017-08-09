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
package biovis.hackebeil.client.gui.output.correlation;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;

import biovis.hackebeil.client.commander.ClientCommander;
import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.client.gui.dialog.AddAdditionalDataDialogController;
import biovis.hackebeil.client.gui.dialog.SegmentationDialogController;
import biovis.hackebeil.client.io.ImageExport;
import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.common.data.Messages;
import biovis.hackebeil.common.data.Motif;
import biovis.hackebeil.common.data.PositionWeightMatrix;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * @author Alrik Hausdorf, Dirk Zeckzer
 *
 */
public class CorrelationController {

    private static final int HEADER_WIDTH = 400;
    private static final int HEADER_HEIGHT = 400;
    private static final int CELL_WIDTH = 100;
    private static final int CELL_HEIGHT = 100;

    private static final Logger log = Logger.getLogger(CorrelationController.class.getName());

    private static final String TYPE_CODE = "code";
    private static final String TYPE_SEGMENT = "segment";
    private static final String TYPE_ADDITIONAL_DATA = "additional data";
    private static final String TYPE_MOTIF = "motif";
    private static final String TYPE_PWM = "pwm";

    private static final DecimalFormat formatter = new DecimalFormat("0.00");

    private ClientConfiguration clientConfiguration;
    private ClientCommander clientCommander;
    private Boolean isInitialized = false;

    private Tab rootTab;

    private List<String> headers = new ArrayList<>();
    private double[][] heatMapData = null;

    @FXML
    private AnchorPane rootPaneFxml;
    @FXML
    private AnchorPane overlayPane;
    @FXML
    private Button overlayStartComputing;
    @FXML
    private Label overlayLabel;
    @FXML
    private ProgressIndicator overlayProgress;
    @FXML
    private AnchorPane pane;

    public CorrelationController() {
    }

    private void setRootTab(
        Tab rootTab
    ) {
        this.rootTab = rootTab;
    }

    public static CorrelationController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(CorrelationController.class.getResource("Correlation.fxml"));
            Tab rootTab = loader.load();
            CorrelationController controller = loader.<CorrelationController>getController();
            controller.setRootTab(rootTab);
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Tab loadView(
        ClientConfiguration clientConfiguration,
        ClientCommander commander
    ) {
        this.clientConfiguration = clientConfiguration;
        this.clientCommander = commander;
        testForReady();

        return rootTab;
    }

    public void initialize() {
        isInitialized = true;
        testForReady();
    }

    /**
     */
    private void updateHeatMap() {
        if (heatMapData == null) {
            return;
        }

        headers = new ArrayList<>();

        headers.add(TYPE_CODE + ":\n");

        for (DataFile df : clientConfiguration.getReferenceDataList()) {
            headers.add(TYPE_SEGMENT + ":\n" + df.getDataSetName());
        }

        for (DataFile additionalDataFile : clientConfiguration.getAdditionalDataList()) {
            headers.add(TYPE_ADDITIONAL_DATA + ":\n" + additionalDataFile.getDataSetName());
        }

        for (Motif motif : clientConfiguration.getMotifList()) {
            headers.add(TYPE_MOTIF + ":\n" + motif.getMotifId());
        }

        for (PositionWeightMatrix pwm : clientConfiguration.getPWMList()) {
            headers.add(TYPE_PWM + ":\n" + pwm.getMotifId());
        }

        //if (heatMapData.length != headers.size()) {
//        System.err.println("Calculation of values did not yet complete! Please try computing correlation again later!");
        //} else {
        redrawHeatMap();
        //}
    }

    private void redrawHeatMap() {
        if (headers.isEmpty()) {
            this.setCorrelationNoData();
            return;
        }

        this.setCorrelationShow();

        // heatmap layout and style
        GridPane heatmap = new GridPane();
        AnchorPane.setBottomAnchor(heatmap, 0.0);
        AnchorPane.setLeftAnchor(heatmap, 0.0);
        AnchorPane.setRightAnchor(heatmap, 0.0);
        AnchorPane.setTopAnchor(heatmap, 0.0);
        heatmap.setStyle("-fx-padding: 1;" + "-fx-hgap: 1; -fx-vgap: 1;");

        // draw heat map content
        drawMapParts(heatmap);

        // add heatmap
        pane.getChildren().clear();
        pane.getChildren().add(heatmap);

        // add interactions
        MenuItem cmItem1 = new MenuItem("Save as image");
        cmItem1.setOnAction((ActionEvent e) -> {
            ImageExport.exportPaneToPng(pane, null);
        });

        final ContextMenu cm = new ContextMenu(cmItem1);
        pane.setOnMouseClicked((event) -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                cm.show(pane, event.getScreenX(), event.getScreenY());
            }
        });
    }

    /**
     * @param heatmap
     */
    private void drawMapParts(
        GridPane heatmap
    ) {
        int offset = 2;

        for (int x = 0; x <= heatMapData.length; x++) {
            for (int y = 0; y <= heatMapData.length; y++) {
                StackPane element = new StackPane();
                element.setPadding(new Insets(5, 5, 5, 5));

                if (x == 0 && y == 0) {
                    Button btn = new Button();
                    btn.setText("recalculate\ncorrelation");
                    btn.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event e) -> {
                                        Platform.runLater(() -> {
                                            startComputing();
                                        });
                                    });
                    element.setAlignment(Pos.CENTER);
                    element.setMinWidth(HEADER_WIDTH);
                    element.setMinHeight(HEADER_HEIGHT);
                    element.getChildren().add(btn);
                } else if (x == 0) {
                    if (headers.get(y - 1).startsWith(TYPE_SEGMENT)) {
                        DataFile df = clientConfiguration.getReferenceDataList().get(y - offset);
                        element.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event e) -> {
                                                Platform.runLater(() -> {
                                                    showSegmentDialog(heatmap, df);
                                                });
                                            });
                    } else if (headers.get(y - 1).startsWith(TYPE_ADDITIONAL_DATA)) {
                        int index = y - clientConfiguration.getReferenceDataList().size() - offset;

                        DataFile df = clientConfiguration.getAdditionalDataFile(index);
                        element.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event e) -> {
                                                Platform.runLater(() -> {
                                                    showAdditionalDataDialog(heatmap, df);
                                                });
                                            });
                    }
                    Label label = new Label();
                    label.setText(headers.get(y - 1));
                    element.setAlignment(Pos.CENTER_LEFT);
                    element.setMaxHeight(CELL_HEIGHT);
                    element.setMinWidth(HEADER_WIDTH);
                    element.setBackground(
                        new Background(
                            new BackgroundFill(Color.LIGHTGRAY,
                                               CornerRadii.EMPTY,
                                               new Insets(1, 1, 1, 1)
                            )
                        )
                    );
                    element.getChildren().add(label);

                    // Tooltip
                    Tooltip tp = new Tooltip(headers.get(y - 1).replaceAll("[\n\r]", ""));
                    Tooltip.install(element, tp);
                } else if (y == 0) {
                    if (headers.get(x - 1).startsWith(TYPE_SEGMENT)) {
                        DataFile df = clientConfiguration.getReferenceDataList().get(x - offset);
                        element.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event e) -> {
                                                Platform.runLater(() -> {
                                                    showSegmentDialog(heatmap, df);
                                                });
                                            });
                    } else if (headers.get(x - 1).startsWith(TYPE_ADDITIONAL_DATA)) {
                        int index = x - clientConfiguration.getReferenceDataList().size() - offset;
                        DataFile df = clientConfiguration.getAdditionalDataFile(index);
                        element.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event e) -> {
                                                Platform.runLater(() -> {
                                                    showAdditionalDataDialog(heatmap, df);
                                                });
                                            });
                    }
                    Label label = new Label();
                    label.setMinWidth(HEADER_WIDTH);
                    label.setMaxHeight(CELL_HEIGHT);
                    label.setRotate(90);
                    label.setText(headers.get(x - 1));
                    element.setAlignment(Pos.CENTER);
                    element.setMinWidth(CELL_WIDTH);
                    element.setPrefWidth(CELL_WIDTH);
                    element.setMaxWidth(CELL_WIDTH);
                    element.setMinHeight(HEADER_HEIGHT);
                    element.setPrefHeight(HEADER_HEIGHT);
                    element.setMaxHeight(HEADER_HEIGHT);
                    element.setBackground(
                        new Background(
                            new BackgroundFill(Color.LIGHTGRAY,
                                               CornerRadii.EMPTY,
                                               new Insets(1, 1, 1, 1)
                            )
                        )
                    );
                    element.getChildren().add(label);

                    // Tooltip
                    Tooltip tp = new Tooltip(headers.get(x - 1).replaceAll("[\n\r]", ""));
                    Tooltip.install(element, tp);
                } else {
                    double value = heatMapData[x - 1][y - 1];
                    String valueText;
                    if (value == Double.NaN) {
                        valueText = "NaN";
                    } else {
                        valueText = formatter.format(value);
                    }
                    Label label = new Label();
                    label.setText(valueText);
                    element.setAlignment(Pos.CENTER);
                    element.getChildren().add(label);
                    element.setMinWidth(CELL_WIDTH);
                    element.setPrefWidth(CELL_WIDTH);
                    element.setMaxWidth(CELL_WIDTH);
                    element.setMinHeight(CELL_HEIGHT);
                    element.setPrefHeight(CELL_HEIGHT);
                    element.setMaxHeight(CELL_HEIGHT);
                    element.setBackground(
                        new Background(
                            new BackgroundFill(getColorForValue(value),
                                               CornerRadii.EMPTY, new Insets(1, 1, 1, 1)
                            )
                        )
                    );

                    // Tooltip
                    Tooltip tp = new Tooltip(headers.get(x - 1).replaceAll("[\n\r]", "") + "\n"
                                             + headers.get(y - 1).replaceAll("[\n\r]", "") + "\n"
                                             + valueText);
                    Tooltip.install(element, tp);
                }

                heatmap.add(element, x, y);
            }
        }
    }

    private void showSegmentDialog(
        GridPane heatmap,
        DataFile dataFile
    ) {
        SegmentationDialogController.createDialog(
            heatmap.getScene().getWindow(),
            dataFile
        );
    }

    private void showAdditionalDataDialog(
        GridPane heatmap,
        DataFile dataFile
    ) {
        AddAdditionalDataDialogController.createDialog(
            heatmap.getScene().getWindow(),
            dataFile
        );
    }

    private Color getColorForValue(Double value) {
        if (value < -1.0 || value > 1.0 || value.isNaN()) {
            return Color.BLACK;
        }
        if (value >= 0.0) {
            return Color.hsb(0, value, 1.0);
        } else {
            return Color.hsb(Color.BLUE.getHue(), (-1.0 * value), 1.0);
        }
    }

    @FXML
    public void click() {
    }

    @FXML
    public void startComputing() {
        this.setCorrelationComputing();

        // log.info("cbCorrelation" + this.cbCorrelation.isSelected());
        Object[] command = new Object[2];
        command[0] = Messages.SERVER_startCorrelation;
        Gson gson = new Gson();
        String dataFileList = gson.toJson("");
        command[1] = dataFileList;
        clientCommander.sendCommand(command);
    }

    private void init() {
        this.redrawHeatMap();
    }

    public void setCorrelationNoData() {
        this.pane.setVisible(false);
        this.overlayPane.setVisible(true);
        this.overlayLabel.setVisible(true);
        this.overlayProgress.setVisible(false);
        this.overlayStartComputing.setDisable(false);
    }

    public void setCorrelationComputing() {
        this.pane.setVisible(false);
        this.overlayPane.setVisible(true);
        this.overlayLabel.setVisible(false);
        this.overlayProgress.setVisible(true);
        this.overlayStartComputing.setDisable(true);
    }

    public void setCorrelationShow() {
        this.pane.setVisible(true);
        this.overlayPane.setVisible(false);
        this.overlayStartComputing.setDisable(false);
    }

    private void testForReady() {
        if (clientConfiguration != null
            && clientCommander != null
            && isInitialized == true) {
            this.init();
        }
    }

    public void updateCorrelationData(double[][] data) {
        heatMapData = data;
        Platform.runLater(() -> {
            updateHeatMap();
        });
    }

    public void exportToPng(String path) {
        path += "-correlation";
        if (pane != null && pane.getChildrenUnmodifiable().size() > 0) {
            ImageExport.exportPaneToPng(this.pane, path);
        }
    }
}
