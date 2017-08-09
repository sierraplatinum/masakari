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

import org.apache.commons.vfs2.FileObject;

import com.google.gson.Gson;

import biovis.hackebeil.client.commander.ClientCommander;
import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.client.gui.dialog.AddAdditionalDataDialogController;
import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.common.data.Messages;
import biovislib.vfsjfilechooser.VFSJFileChooser;
import biovislib.vfsjfilechooser.VFSJFileChooser.RETURN_TYPE;
import biovislib.vfsjfilechooser.filechooser.VFSFileFilter;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
 * @author nhinzmann, Dirk Zeckzer
 *
 */
public class ProgressAdditionalMeasurementsController {

    private static final Logger log = Logger.getLogger(ProgressAdditionalMeasurementsController.class.getName());

    private ClientConfiguration clientConfiguration;
    private ClientCommander clientCommander;
    private AnchorPane anchorAdditionalData;

    @FXML
    private Button btnAddFile;
    @FXML
    private Button btnUpFile;
    @FXML
    private Button btnDownFile;
    @FXML
    private Button btnCompute;
    @FXML
    private TableView<DataFile> tableView;
    @FXML
    private TableColumn<DataFile, Boolean> colUseScore;
    @FXML
    private TableColumn<DataFile, Boolean> colActions;
    @FXML
    private TableColumn<DataFile, String> colDataSetName;

    private final static Image IMAGE_UP = new Image(
        ProgressAdditionalMeasurementsController.class.getResourceAsStream("img/fa-arrow-up.png"));
    private final static Image IMAGE_DOWN = new Image(
        ProgressAdditionalMeasurementsController.class.getResourceAsStream("img/fa-arrow-down.png"));

    public ProgressAdditionalMeasurementsController() {
    }

    /**
     *
     * @param rootPane
     */
    public void setPane(AnchorPane rootPane) {
        this.anchorAdditionalData = rootPane;
    }

    /**
     *
     * @return
     */
    public static ProgressAdditionalMeasurementsController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(ProgressAdditionalMeasurementsController.class.getResource("ProgressAdditionalMeasurements.fxml"));
            AnchorPane anchorAdditionalData = loader.load();
            ProgressAdditionalMeasurementsController progressAdditionalDataController = loader
                .<ProgressAdditionalMeasurementsController>getController();
            progressAdditionalDataController.setPane(anchorAdditionalData);
            return progressAdditionalDataController;
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
    public AnchorPane loadProgressAdditionalMeasurements(
        ClientConfiguration clientConfiguration,
        ClientCommander commander
    ) {
        this.clientConfiguration = clientConfiguration;
        this.clientCommander = commander;
        testForReady();

        return anchorAdditionalData;
    }

    public void initialize() {
        btnUpFile.setGraphic(new ImageView(IMAGE_UP));
        btnDownFile.setGraphic(new ImageView(IMAGE_DOWN));

        colDataSetName.setCellFactory((TableColumn<DataFile, String> column) -> {
            TableCell<DataFile, String> cell = new TableCell<DataFile, String>() {
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

        colUseScore.setCellFactory((TableColumn<DataFile, Boolean> column) -> {
            CheckBoxTableCell<DataFile, Boolean> cell = new CheckBoxTableCell<DataFile, Boolean>() {
            };
            cell.setOnMouseClicked((MouseEvent event) -> {
                if (tableView.getItems().size() > cell.getIndex()) {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        DataFile element = tableView.getItems().get(cell.getIndex());
                        if (element.isChanged()) {
                            element.changeUseScore();
                        }
                    }
                }

            });
            return cell;

        });
        colActions.setCellValueFactory((TableColumn.CellDataFeatures<DataFile, Boolean> p) -> new SimpleBooleanProperty(p.getValue() != null));
        colActions.setCellFactory((TableColumn<DataFile, Boolean> p) -> new ButtonCell());

        this.testForReady();
    }

    private class ButtonCell
        extends TableCell<DataFile, Boolean> {

        private final HBox btngrActions = new HBox();
        private final Image imageEdit = new Image(
            ProgressAdditionalMeasurementsController.class.getResourceAsStream("img/fa-pencil.png"));
        private final Image imageDelete = new Image(
            ProgressAdditionalMeasurementsController.class.getResourceAsStream("img/fa-trash.png"));

        private final Button btnEdit = new Button("", new ImageView(imageEdit));
        private final Button btnDelete = new Button("", new ImageView(imageDelete));

        ButtonCell() {
            btnEdit.setOnAction((ActionEvent t) -> {
                DataFile dataFileToEdit = (DataFile) getTableRow().getItem();
                showAdditionalMeasurementsDialog(dataFileToEdit);
            });
            btnDelete.setOnAction((ActionEvent t) -> {
                DataFile dataFileToRemove = (DataFile) getTableRow().getItem();
                if (dataFileToRemove != null) {
                    clientConfiguration.removeAdditionalMeasurementFile(dataFileToRemove);
                }
            });
            btngrActions.getChildren().addAll(btnEdit, btnDelete);
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
        this.tableView.setItems(clientConfiguration.getAdditionalDataList());
        this.clientConfiguration.setProgressAdditionalMeasurementsController(this);
        this.setDisabled(true);

    }

    private void showAdditionalMeasurementsDialog(
        DataFile dataFile
    ) {
        AddAdditionalDataDialogController.createDialog(tableView.getScene().getWindow(), dataFile);
    }

    @FXML
    private void handleAddFile() {
        clientConfiguration.initFileChooser();
        VFSJFileChooser remoteFileChooser = clientConfiguration.getRemoteFileChooser();
        remoteFileChooser.setDialogTitle("Load additional bed files");
        remoteFileChooser.addChoosableFileFilter(new VFSFileFilter("gzip file (*.gz)", "gz"));
        remoteFileChooser.addChoosableFileFilter(new VFSFileFilter("BED file (*.bed)", "bed"));
        remoteFileChooser.setMultiSelectionEnabled(true);
        RETURN_TYPE answer = remoteFileChooser.showOpenDialog(null);
        if (answer == RETURN_TYPE.APPROVE) {
//			log.info("ListSize before:" + cache.getAdditionalDataList().size());
            final FileObject fileObjects[] = remoteFileChooser.getSelectedFileObjects();
            if (fileObjects != null) {
                for (FileObject fileObject : fileObjects) {
                    DataFile dataFile = new DataFile();
                    dataFile.setDataSetName(fileObject.getName().getBaseName());
                    dataFile.setFilePath(fileObject.getName().getPath());
                    clientConfiguration.addAdditionalMeasurementFile(dataFile);
                }
            }
//			log.info("ListSize after:" + cache.getAdditionalDataList().size());
        }
    }

    @FXML
    private void handleDeleteFile() {
        DataFile dataFileToRemove = tableView.getSelectionModel().getSelectedItem();

        if (dataFileToRemove != null
            && dataFileToRemove.getColumnNumber() != -1) {
            /*
            int deletedNumber = dataFileToRemove.getDataListNumber();
            System.out.println("deletedNumber: " + deletedNumber);
             */
            clientConfiguration.removeAdditionalMeasurementFile(dataFileToRemove);
        }
    }

    @FXML
    private void handleUp() {
        int indexOfSelectedDataFile = tableView.getSelectionModel().getSelectedIndex();
        if (indexOfSelectedDataFile != -1) {
            ObservableList<DataFile> fileList = tableView.getItems();
            if (indexOfSelectedDataFile > 0) {
                DataFile selectedDataFile = fileList.remove(indexOfSelectedDataFile);
                fileList.add(indexOfSelectedDataFile - 1, selectedDataFile);
                tableView.getSelectionModel().clearAndSelect(indexOfSelectedDataFile - 1);
            }
        }
    }

    @FXML
    private void handleDown() {
        int indexOfSelectedDataFile = tableView.getSelectionModel().getSelectedIndex();
        if (indexOfSelectedDataFile != -1) {
            ObservableList<DataFile> fileList = tableView.getItems();
            if (indexOfSelectedDataFile < fileList.size() - 1) {
                DataFile selectedDataFile = fileList.remove(indexOfSelectedDataFile);
                fileList.add(indexOfSelectedDataFile + 1, selectedDataFile);
                tableView.getSelectionModel().clearAndSelect(indexOfSelectedDataFile + 1);
            }
        }
    }

    @FXML
    public void handleAdditionalInformation() {
        List<DataFile> tempList = new ArrayList<>();
        for (DataFile df : clientConfiguration.getAdditionalDataList()) {
            if (df.isChanged()) {
                tempList.add(df);
            }
        }
        if (clientCommander.isActive() && tempList.size() > 0) {
            setComputeDisabled(true);
            this.setDisabled(true);
            Object[] command = new Object[2];
            command[0] = Messages.SERVER_addAdditionalData;
            Gson gson = new Gson();
            String dataFileList = gson.toJson(tempList);
            command[1] = dataFileList;
            clientCommander.sendCommand(command);
        }
    }

    public void clearDataFileList() {
        clientConfiguration.getAdditionalDataList().clear();
    }

    public void setDisabled(boolean disable) {
        this.btnAddFile.setDisable(disable);
        this.btnUpFile.setDisable(disable);
        this.btnDownFile.setDisable(disable);
        this.tableView.setDisable(disable);
        setComputeDisabled(disable);
    }

    public void setComputeDisabled(boolean disable) {
        this.btnCompute.setDisable(disable);
    }

    private void testForReady() {
        if (this.clientConfiguration != null
            && this.clientCommander != null) {
            this.init();
        }
    }

    public ReadOnlyBooleanProperty getIsDisabledProp() {
        return this.btnAddFile.disabledProperty();
    }

    public ReadOnlyBooleanProperty getIsComputeDisabledProp() {
        return this.btnCompute.disabledProperty();
    }
}
