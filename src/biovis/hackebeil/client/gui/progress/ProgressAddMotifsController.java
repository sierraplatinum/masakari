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
import biovis.hackebeil.common.data.Motif;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

/**
 * @author nhinzmann, Alrik Hausdorf
 *
 */
public class ProgressAddMotifsController {

    private static final Logger log = Logger.getLogger(ProgressAddMotifsController.class.getName());

    private ClientConfiguration clientConfiguration;
    private ClientCommander clientCommander;
    private AnchorPane anchorAddMotifs;

    @FXML
    private Button btnAddMotif;
    @FXML
    private Button btnUp;
    @FXML
    private Button btnDown;
    @FXML
    private Button btnSearchMotif;
    @FXML
    private TableView<Motif> tableView;
    @FXML
    private TableColumn<Motif, String> colMotif;
    @FXML
    private TableColumn<Motif, Boolean> colIsNormalized;
    @FXML
    private TableColumn<Motif, Boolean> colActions;

    private final static Image IMAGE_UP = new Image(
        ProgressAddMotifsController.class.getResourceAsStream("img/fa-arrow-up.png"));
    private final static Image IMAGE_DOWN = new Image(
        ProgressAddMotifsController.class.getResourceAsStream("img/fa-arrow-down.png"));

    public ProgressAddMotifsController() {
    }

    /**
     *
     * @param rootPane
     */
    public void setPane(AnchorPane rootPane) {
        this.anchorAddMotifs = rootPane;
    }

    /**
     *
     * @return
     */
    public static ProgressAddMotifsController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(ProgressAddMotifsController.class.getResource("ProgressAddMotifs.fxml"));
            AnchorPane anchorAddMotifs = loader.load();
            ProgressAddMotifsController progressMotifsController = loader.<ProgressAddMotifsController>getController();
            progressMotifsController.setPane(anchorAddMotifs);
            return progressMotifsController;
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
    public AnchorPane loadProgressAddMotifs(
        ClientConfiguration clientConfiguration,
        ClientCommander commander
    ) {
        this.clientConfiguration = clientConfiguration;
        this.clientCommander = commander;
        testForReady();

        return anchorAddMotifs;
    }

    public void initialize() {
        btnUp.setGraphic(new ImageView(IMAGE_UP));
        btnDown.setGraphic(new ImageView(IMAGE_DOWN));

        colMotif.setCellFactory((TableColumn<Motif, String> column) -> {
            TableCell<Motif, String> cell = new TableCell<Motif, String>() {
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

        colIsNormalized.setCellFactory((TableColumn<Motif, Boolean> p) -> new CheckBoxCellNormalized(tableView));
        colActions.setCellFactory((TableColumn<Motif, Boolean> p) -> new ButtonCellDelete(clientConfiguration));

        this.testForReady();
    }

    private void init() {
        this.tableView.setItems(clientConfiguration.getMotifList());
        this.clientConfiguration.setProgressAddMotifsController(this);
        this.setDisabled(true);

    }

    @FXML
    private void handleAddMotif() {
        AddMotifDialogController.createDialog(tableView.getScene().getWindow(), clientConfiguration);
    }

    @FXML
    private void handleUp() {
        int indexOfSelected = tableView.getSelectionModel().getSelectedIndex();
        if (indexOfSelected != -1) {
            ObservableList<Motif> fileList = tableView.getItems();
            if (indexOfSelected > 0) {
                Motif selectedMotif = fileList.remove(indexOfSelected);
                fileList.add(indexOfSelected - 1, selectedMotif);
                tableView.getSelectionModel().clearAndSelect(indexOfSelected - 1);
            }
        }
    }

    @FXML
    private void handleDown() {
        int indexOfSelected = tableView.getSelectionModel().getSelectedIndex();
        if (indexOfSelected != -1) {
            ObservableList<Motif> fileList = tableView.getItems();
            if (indexOfSelected < fileList.size() - 1) {
                Motif selectedMotif = fileList.remove(indexOfSelected);
                fileList.add(indexOfSelected + 1, selectedMotif);
                tableView.getSelectionModel().clearAndSelect(indexOfSelected + 1);
            }
        }
    }

    @FXML
    public void handleSearch() {
        ObservableList<Motif> list = clientConfiguration.getMotifList();
        List<Motif> tempList = new ArrayList<>();
        for (Motif df : list) {
            if (df.isChanged()) {
                tempList.add(df);
            }
        }
        if (clientCommander.isActive() && tempList.size() > 0) {
            this.setDisabled(true);
            Object[] command = new Object[2];
            command[0] = Messages.SERVER_addMotif;
            Gson gson = new Gson();
            String mList = gson.toJson(tempList);
            command[1] = mList;
            clientCommander.sendCommand(command);
        }
    }

    public void setDisabled(boolean disable) {
        Platform.runLater(() -> {
            this.btnAddMotif.setDisable(disable);
            this.btnUp.setDisable(disable);
            this.btnDown.setDisable(disable);
            setComputeDisabled(disable);
            this.tableView.setDisable(disable);
        });
    }

    public void setComputeDisabled(boolean disable) {
        this.btnSearchMotif.setDisable(disable);
    }

    private void testForReady() {
        if (this.clientConfiguration != null
            && this.clientCommander != null) {
            this.init();
        }
    }

    public ReadOnlyBooleanProperty getIsDisabledProp() {
        return this.btnAddMotif.disabledProperty();
    }

    public ReadOnlyBooleanProperty getIsComputeDisabledProp() {
        return this.btnSearchMotif.disabledProperty();
    }
}
