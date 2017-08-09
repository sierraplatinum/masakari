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
package biovis.hackebeil.client.gui.progress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;

import biovis.hackebeil.client.commander.ClientCommander;
import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.common.data.Messages;
import biovis.hackebeil.common.data.PositionWeightMatrix;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * @author nhinzmann, Alrik Hausdorf
 *
 */
public class ProgressAddPWMController {

    private static final Logger log = Logger.getLogger(ProgressAddPWMController.class.getName());

    private ClientConfiguration clientConfiguration;
    private ClientCommander clientCommander;
    private AnchorPane anchorAddPWM;

    @FXML
    private Button btnAddPWM;
    @FXML
    private Button btnUp;
    @FXML
    private Button btnDown;
    @FXML
    private Button btnSearchPWM;
    @FXML
    private TableView<PositionWeightMatrix> tableView;
    @FXML
    private TableColumn<PositionWeightMatrix, String> colPWM;
    @FXML
    private TableColumn<PositionWeightMatrix, Integer> colComputationMethod;
    @FXML
    private TableColumn<PositionWeightMatrix, Boolean> colIsNormalized;
    @FXML
    private TableColumn<PositionWeightMatrix, Double> colCutoffValue;
    @FXML
    private TableColumn<PositionWeightMatrix, Boolean> colActions;

    private final static Image IMAGE_UP = new Image(
        ProgressAddPWMController.class.getResourceAsStream("img/fa-arrow-up.png"));
    private final static Image IMAGE_DOWN = new Image(
        ProgressAddPWMController.class.getResourceAsStream("img/fa-arrow-down.png"));

    public ProgressAddPWMController() {
    }

    /**
     *
     * @param rootPane
     */
    public void setPane(AnchorPane rootPane) {
        this.anchorAddPWM = rootPane;
    }

    /**
     *
     * @return
     */
    public static ProgressAddPWMController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(ProgressAddPWMController.class.getResource("ProgressAddPWM.fxml"));
            AnchorPane anchorAddPWM = loader.load();
            ProgressAddPWMController progressPWMController = loader.<ProgressAddPWMController>getController();
            progressPWMController.setPane(anchorAddPWM);
            return progressPWMController;
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
    public AnchorPane loadProgressAddPWM(
        ClientConfiguration clientConfiguration,
        ClientCommander commander
    ) {
        this.clientConfiguration = clientConfiguration;
        this.clientCommander = commander;
        testForReady();

        return anchorAddPWM;
    }

    public void initialize() {
        btnUp.setGraphic(new ImageView(IMAGE_UP));
        btnDown.setGraphic(new ImageView(IMAGE_DOWN));

        colPWM.setCellFactory((TableColumn<PositionWeightMatrix, String> column) -> {
            TableCell<PositionWeightMatrix, String> cell = new TableCell<PositionWeightMatrix, String>() {
                @Override
                protected void updateItem(String name, boolean empty) {
                    if (name == null || empty) {
                        setText(null);
                    } else {
                        setText(name);
                    }
                }
            };

            return cell;
        });

        colComputationMethod.setCellFactory((TableColumn<PositionWeightMatrix, Integer> column) -> {
            TableCell<PositionWeightMatrix, Integer> cell = new TableCell<PositionWeightMatrix, Integer>() {
                @Override
                protected void updateItem(Integer computationMethod, boolean empty) {
                    if (computationMethod == null || empty) {
                        setText(null);
                    } else {
                        switch (computationMethod) {
                            case PositionWeightMatrix.COMPUTATION_METHOD_MEDIAN:
                                setText("Median");
                                break;
                            case PositionWeightMatrix.COMPUTATION_METHOD_MAX:
                                setText("Maximum");
                                break;
                            case PositionWeightMatrix.COMPUTATION_METHOD_CUTOFF:
                                setText("Cutoff");
                                break;
                            default:
                                setText("None");
                                break;
                        }
                    }
                }
            };

            return cell;
        });

        colIsNormalized.setCellFactory((TableColumn<PositionWeightMatrix, Boolean> column) -> {
            CheckBoxTableCell<PositionWeightMatrix, Boolean> cbcell = new CheckBoxTableCell<PositionWeightMatrix, Boolean>() {
            };
            cbcell.setOnMouseClicked((MouseEvent event) -> {
                if (tableView.getItems().size() > cbcell.getIndex()) {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        PositionWeightMatrix element = tableView.getItems().get(cbcell.getIndex());
                        if (element.isChanged()) {
                            tableView.getItems().get(cbcell.getIndex()).changeIsNormalized();
                        }
                    }
                }

            });
            return cbcell;
        });

        colCutoffValue.setCellFactory((TableColumn<PositionWeightMatrix, Double> column) -> {
            TableCell<PositionWeightMatrix, Double> cell = new TableCell<PositionWeightMatrix, Double>() {
                @Override
                protected void updateItem(Double value, boolean empty) {
                    if (value == null || empty) {
                        setText(null);
                    } else {
                        setText(value.toString());
                    }
                }
            };
            return cell;
        });

        colActions.setCellFactory((TableColumn<PositionWeightMatrix, Boolean> p) -> new ButtonCell());

        this.testForReady();
    }

    private class ButtonCell extends TableCell<PositionWeightMatrix, Boolean> {

        private final HBox btngrActions = new HBox();
        private final Image imageDelete = new Image(ProgressAdditionalMeasurementsController.class.getResourceAsStream("img/fa-trash.png"));

        private final Button btnDelete = new Button("", new ImageView(imageDelete));

        ButtonCell() {

            btnDelete.setOnAction((ActionEvent t) -> {
                PositionWeightMatrix pwmToRemove = (PositionWeightMatrix) getTableRow().getItem();

                if (pwmToRemove != null) {
                    clientConfiguration.removePWMFromList(pwmToRemove);
                }
            });
            btngrActions.getChildren().addAll(btnDelete);
        }

        @Override
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);
            if (!empty) {
                setGraphic(btngrActions);
            } else {
                setGraphic(null);
            }
        }
    }

    private void init() {
        this.tableView.setItems(clientConfiguration.getPWMList());
        this.clientConfiguration.setProgressAddPWMController(this);
        this.setDisabled(true);

    }

    @FXML
    private void handleAddPWM() {
        AddPWMDialogController.createDialog(tableView.getScene().getWindow(), clientConfiguration);
    }

    @FXML
    private void handleUp() {
        int indexOfSelected = tableView.getSelectionModel().getSelectedIndex();
        if (indexOfSelected != -1) {
            ObservableList<PositionWeightMatrix> fileList = tableView.getItems();
            if (indexOfSelected > 0) {
                PositionWeightMatrix selectedPWM = fileList.remove(indexOfSelected);
                fileList.add(indexOfSelected - 1, selectedPWM);
                tableView.getSelectionModel().clearAndSelect(indexOfSelected - 1);
            }
        }
    }

    @FXML
    private void handleDown() {
        int indexOfSelected = tableView.getSelectionModel().getSelectedIndex();
        if (indexOfSelected != -1) {
            ObservableList<PositionWeightMatrix> fileList = tableView.getItems();
            if (indexOfSelected < fileList.size() - 1) {
                PositionWeightMatrix selectedPWM = fileList.remove(indexOfSelected);
                fileList.add(indexOfSelected + 1, selectedPWM);
                tableView.getSelectionModel().clearAndSelect(indexOfSelected + 1);
            }
        }
    }

    @FXML
    public void handleSearch() {
        ObservableList<PositionWeightMatrix> pwmList = clientConfiguration.getPWMList();
        List<PositionWeightMatrix> tempPWMList = new ArrayList<>();
        for (PositionWeightMatrix df : pwmList) {
            if (df.isChanged()) {
                tempPWMList.add(df);
            }
        }
        if (clientCommander.isActive() && tempPWMList.size() > 0) {
            this.setDisabled(true);
            Object[] command = new Object[2];
            command[0] = Messages.SERVER_addPWM;
            Gson gson = new Gson();
            String mList = gson.toJson(tempPWMList);
            command[1] = mList;
            clientCommander.sendCommand(command);
        }
    }

    public void setDisabled(boolean disable) {
        Platform.runLater(() -> {
            this.btnAddPWM.setDisable(disable);
            this.btnUp.setDisable(disable);
            this.btnDown.setDisable(disable);
            setComputeDisabled(disable);
            this.tableView.setDisable(disable);
        });
    }

    public void setComputeDisabled(boolean disable) {
        this.btnSearchPWM.setDisable(disable);
    }

    private void testForReady() {
        if (this.clientConfiguration != null
            && this.clientCommander != null) {
            this.init();
        }
    }

    public ReadOnlyBooleanProperty getIsDisabledProp() {
        return this.btnAddPWM.disabledProperty();
    }

    public ReadOnlyBooleanProperty getIsComputeDisabledProp() {
        return this.btnSearchPWM.disabledProperty();
    }
}
