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
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import biovis.hackebeil.client.commander.ClientCommander;
import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.common.data.FateOfCodeParameter;
import biovis.hackebeil.common.data.Messages;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;

/**
 * @author Alrik Hausdorf, Dirk Zeckzer
 *
 */
public class FateOfCodeController {

    private static final DecimalFormat formatter = new DecimalFormat("#.#####");

    private Tab rootTab;

    @FXML
    private AnchorPane rootPaneFxml;
    @FXML
    private TextField tfThreshold;
    @FXML
    private Slider sliderThreshold;
    @FXML
    private Button btnApplyThreshold;
    @FXML
    private CheckBox logColorScale;

    @FXML
    private Accordion accordionRoot;
    @FXML
    private TitledPane setupPane;
    @FXML
    private GridPane setupGridPane;
    @FXML
    private Button btnSubmit;

    private ClientConfiguration clientConfiguration;
    private ClientCommander clientCommander;

    private Boolean isInitialized = false;
    private boolean segmentationListHasChanged = false;
    private boolean additionalDataListHasChanged = false;

    private HashMap<Integer, DataFile[]> selection = new HashMap<>();

    private int lines = -1;
    private int columns = -1;
    private Button btnAdditionalData = null;

    private FateOfCodeResultController fateOfCodeResultController = null;
    private TitledPane fateOfCodeResultPane = null;
    private List<int[][]> results = null;

    public FateOfCodeController() {
    }

    /**
     *
     * @param tab
     */
    private void setRootTab(
        Tab tab
    ) {
        this.rootTab = tab;
    }

    /**
     *
     * @return
     */
    public static FateOfCodeController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(FateOfCodeController.class.getResource("FateOfCode.fxml"));
            Tab rootTab = loader.load();
            FateOfCodeController controller = loader.<FateOfCodeController>getController();
            controller.setRootTab(rootTab);
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param clientConfiguration
     * @param commander
     * @return
     */
    public Tab loadView(
        ClientConfiguration clientConfiguration,
        ClientCommander commander
    ) {
        this.clientConfiguration = clientConfiguration;
        this.clientCommander = commander;
        testForReady();

        return rootTab;
    }

    /**
     *
     */
    public void initialize() {
        isInitialized = true;
        testForReady();
    }

    @FXML
    public void applyThreshold() {
//		System.out.println("applyThreshold");
        btnApplyThreshold.setDisable(true);
        this.startComputing();
    }

    @FXML
    public void onThresholdInputChange() {
//		System.out.println("onThresholdInputChange");
        btnApplyThreshold.setDisable(false);
        this.startComputing();
    }

    private void createSetup() {
        setupGridPane.getChildren().clear();

        // set row constraints
        RowConstraints rowConstr = new RowConstraints();
        rowConstr.setPercentHeight(100 / (lines + 1));
        setupGridPane.getRowConstraints().clear();
        for (int l = 0; l < lines + 1; l++) {
            setupGridPane.getRowConstraints().add(rowConstr);
        }

        // set column constraints
        ColumnConstraints colConstr = new ColumnConstraints();
        colConstr.setPercentWidth(100 / (columns + 2));
        setupGridPane.getColumnConstraints().clear();
        for (int c = 0; c < columns + 2; c++) {
            setupGridPane.getColumnConstraints().add(colConstr);
        }

        // setup header
        Label label = new Label();
        label.setText("Segment");
        setupGridPane.add(label, 0, 0);

        for (int c = 0; c < columns; c++) {
            label = new Label();
            label.setText("Additional Data");
            setupGridPane.add(label, c + 1, 0);
        }

        btnAdditionalData = new Button("Add Additional Data");
        btnAdditionalData.addEventHandler(MouseEvent.MOUSE_CLICKED,
                                          (Event e) -> {
                                              Platform.runLater(() -> addReference());
                                          });
        setupGridPane.add(btnAdditionalData, columns + 1, 0);

        // Reference data
        for (int l = 0; l < lines; l++) {
            label = new Label();
            label.setText(clientConfiguration.getReferenceDataList().get(l).getDataSetName());
            setupGridPane.add(label, 0, l + 1);
        }

        // fill with Data
        for (int c = 0; c < columns; c++) {
            for (int l = 0; l < lines; l++) {
                HBox cb = createChoiceHBox(c + 1, l + 1);
                cb.setAlignment(Pos.CENTER_LEFT);
                setupGridPane.add(cb, c + 1, l + 1);
            }
        }
    }

    /**
     *
     * @param col
     * @param row
     * @return
     */
    private HBox createChoiceHBox(Integer col, Integer row) {
        ChoiceBox<DataFile> cb = new ChoiceBox<>(clientConfiguration.getAdditionalDataList());

        HBox hbox = new HBox();
        if (!selection.containsKey(col)) {
            selection.put(col, new DataFile[lines]);
        }
        if (selection.get(col)[row - 1] != null) {
            cb.setValue(selection.get(col)[row - 1]);
        }
        cb.valueProperty().addListener((ov, oldDataFile, newDataFile) -> {
            selection.get(col)[row - 1] = newDataFile;
        });
        hbox.getChildren().add(cb);
        Button btn = new Button("X");
        HBox.setMargin(btn, new Insets(0, 0, 0, 5));
        Tooltip tt = new Tooltip("remove selection");
        btn.setTooltip(tt);
        btn.addEventHandler(MouseEvent.MOUSE_CLICKED,
                            (Event e) -> {
                                Platform.runLater(() -> cb.getSelectionModel().clearSelection());
                            });
        hbox.getChildren().add(btn);
        return hbox;
    }

    @FXML
    public void resetSetup() {
        // System.out.println("resetSetup");
        if (clientConfiguration.getReferenceDataList().size() <= 0) {
            System.err.println("[Error]: No reference data -> no fate of code!");
            setupGridPane.getChildren().clear();
            this.btnSubmit.setDisable(true);
            return;
        }

        if (clientConfiguration.getAdditionalDataList().size() <= 0) {
            System.err.println("[Error]: No additional data -> no fate of code!");
            setupGridPane.getChildren().clear();
            this.btnSubmit.setDisable(true);
            return;
        }

        if (clientConfiguration.getAdditionalDataList().size() <= clientConfiguration.getReferenceDataList().size()) {
            System.err.println("[Warning]: Less additional data than reference data-> ambiguous fate of code!");
        }

        Platform.runLater(() -> {
            lines = clientConfiguration.getReferenceDataList().size();

            // cols
            columns = 1;
            selection.clear();
            createSetup();
            this.btnSubmit.setDisable(false);
        });
    }

    @FXML
    public void addReference() {
        columns++;
        // set column constraints
        ColumnConstraints colConstr = new ColumnConstraints();
        colConstr.setPercentWidth(100 / (columns + 2));
        setupGridPane.getColumnConstraints().clear();
        for (int c = 0; c < columns + 2; c++) {
            setupGridPane.getColumnConstraints().add(colConstr);
        }

        // setup header
        Label label = new Label();
        label.setText("Additional Data");
        setupGridPane.add(label, columns, 0);

        setupGridPane.getChildren().remove(btnAdditionalData);
        setupGridPane.add(btnAdditionalData, columns + 1, 0);

        // fill with Data
        for (int l = 0; l < lines; l++) {
            HBox cb = createChoiceHBox(columns, l + 1);
            cb.setAlignment(Pos.CENTER_LEFT);
            setupGridPane.add(cb, columns, l + 1);
        }
    }

    @FXML
    public void click() {
    }

    @FXML
    public void startComputing() {
        String entry = this.tfThreshold.getText().replaceAll(",", ".");
        if (entry.isEmpty()) {
            return;
        }

        this.accordionRoot.setExpandedPane(fateOfCodeResultPane);
        fateOfCodeResultController.clearMatrix();

        double threshold = Double.parseDouble(entry);

        FateOfCodeParameter fateOfCodeParameter = new FateOfCodeParameter(selection, threshold);

        Object[] command = new Object[2];
        command[0] = Messages.SERVER_startFateOfCodeComputation;
        Gson gson = new Gson();
        String dataFileList = gson.toJson(fateOfCodeParameter);
        command[1] = dataFileList;
        clientCommander.sendCommand(command);
    }

    /**
     *
     * @param results
     */
    public void setFateOfCodeResults(
        List<int[][]> results
    ) {
        this.accordionRoot.setExpandedPane(fateOfCodeResultPane);

        this.results = results;
        fateOfCodeResultController.setFateOfCodeResults(results, selection.size(), logColorScale.isSelected());
    }

    private void init() {
        this.accordionRoot.setExpandedPane(this.setupPane);
        sliderThreshold.valueProperty().addListener((o, vold_val, new_val) -> {
            tfThreshold.setText(formatter.format(new_val));
            btnApplyThreshold.setDisable(false);
        });

        clientConfiguration.getReferenceDataList().addListener((ListChangeListener<DataFile>) (c) -> {
            if (segmentationListHasChanged) {
                return;
            }
            segmentationListHasChanged = true;
            Platform.runLater(() -> resetSetup());
            new Thread(() -> {
                try {
                    Thread.sleep(800);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                segmentationListHasChanged = false;
            }).run();
        });

        clientConfiguration.getAdditionalDataList().addListener((ListChangeListener<DataFile>) (c) -> {
            if (additionalDataListHasChanged) {
                return;
            }
            additionalDataListHasChanged = true;
            Platform.runLater(() -> resetSetup());
            new Thread(() -> {
                try {
                    Thread.sleep(800);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                additionalDataListHasChanged = false;
            }).run();
        });

        fateOfCodeResultController = FateOfCodeResultController.getInstance();
        Platform.runLater(() -> {
            fateOfCodeResultPane = fateOfCodeResultController.loadView();
            accordionRoot.getPanes().add(fateOfCodeResultPane);
        });

        logColorScale.selectedProperty().addListener((ov, before, after) -> {
            this.accordionRoot.setExpandedPane(fateOfCodeResultPane);
            fateOfCodeResultController.setFateOfCodeResults(results, selection.size(), logColorScale.isSelected());
        });
    }

    private void testForReady() {
        if (clientConfiguration != null && clientCommander != null && isInitialized == true) {
            this.init();
        }
    }

    /**
     * @param path 
     */
    public void saveMatrixToFile(String path) {
        fateOfCodeResultController.saveMatrixToFile(path);
    }
}
