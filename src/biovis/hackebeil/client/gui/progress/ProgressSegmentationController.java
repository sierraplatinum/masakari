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
import java.util.logging.Logger;

import org.apache.commons.vfs2.FileObject;

import com.google.gson.Gson;

import biovis.hackebeil.client.commander.ClientCommander;
import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.client.gui.dialog.SegmentationDialogController;
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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * @author nhinzmann, Dirk Zeckzer
 *
 */
public class ProgressSegmentationController {

    private static final Logger log = Logger.getLogger(ProgressSegmentationController.class.getName());

    private ClientConfiguration clientConfiguration = null;
    private ClientCommander clientCommander;
    private AnchorPane anchorSegmentation;

    @FXML
    private Button btnAddFile;
    @FXML
    private Button btnUpFile;
    @FXML
    private Button btnDownFile;
    @FXML
    private TableView<DataFile> tableView;
    @FXML
    private TableColumn<DataFile, String> colDataSetName;
    @FXML
    private TableColumn<DataFile, Boolean> colActions;
    @FXML
    private TextField tfMinSegmentLength;
    @FXML
    private Button btnStart;

    private final static Image IMAGE_UP = new Image(
        ProgressAdditionalMeasurementsController.class.getResourceAsStream("img/fa-arrow-up.png"));
    private final static Image IMAGE_DOWN = new Image(
        ProgressAdditionalMeasurementsController.class.getResourceAsStream("img/fa-arrow-down.png"));

    public ProgressSegmentationController() {
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
        colActions.setCellValueFactory((TableColumn.CellDataFeatures<DataFile, Boolean> p) -> new SimpleBooleanProperty(p.getValue() != null));
        colActions.setCellFactory((TableColumn<DataFile, Boolean> p) -> new ButtonCell());

        this.testForReady();
    }

    private class ButtonCell extends TableCell<DataFile, Boolean> {

        private final HBox btngrActions = new HBox();
        private final Image imageEdit = new Image(
            ProgressAdditionalMeasurementsController.class.getResourceAsStream("img/fa-pencil.png"));
        private final Image imageDelete = new Image(
            ProgressAdditionalMeasurementsController.class.getResourceAsStream("img/fa-trash.png"));

        private final Button btnEdit = new Button("", new ImageView(imageEdit));
        private final Button btnDelete = new Button("", new ImageView(imageDelete));

        ButtonCell() {
            btnEdit.setOnAction((ActionEvent t) -> {
                DataFile dataFileToView = (DataFile) getTableRow().getItem();
                showInfoDialog(dataFileToView);
            });
            btnDelete.setOnAction((ActionEvent t) -> {
                DataFile dataFileToRemove = (DataFile) getTableRow().getItem();
                if (dataFileToRemove != null) {
                    ObservableList<DataFile> fileList = clientConfiguration.getReferenceDataList();
                    fileList.remove(dataFileToRemove);
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

    private void showInfoDialog(DataFile dataFile) {
        SegmentationDialogController.createDialog(
            tableView.getScene().getWindow(),
            dataFile
        );
    }

    private void init() {
        this.tableView.setItems(clientConfiguration.getReferenceDataList());
        this.tfMinSegmentLength.setText(Integer.toString(clientConfiguration.getMinSegmentLength()));
        this.setDisabled(true);
        this.clientConfiguration.setProgressSegmentationController(this);
    }

    /**
     *
     * @param rootPane
     */
    public void setPane(AnchorPane rootPane) {
        this.anchorSegmentation = rootPane;
    }

    /**
     *
     * @return
     */
    public static ProgressSegmentationController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(ProgressSegmentationController.class.getResource("ProgressSegmentation.fxml"));
            AnchorPane anchorSegmentation = loader.load();
            ProgressSegmentationController progressSegmentationController = loader
                .<ProgressSegmentationController>getController();
            progressSegmentationController.setPane(anchorSegmentation);
            return progressSegmentationController;
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
    public AnchorPane loadProgressSegmentation(
        ClientConfiguration clientConfiguration,
        ClientCommander commander
    ) {
        this.clientConfiguration = clientConfiguration;
        this.clientCommander = commander;
        testForReady();

        return anchorSegmentation;
    }

    @FXML
    private void handleLoad() {
        clientConfiguration.initFileChooser();
        VFSJFileChooser remoteFileChooser = clientConfiguration.getRemoteFileChooser();
        remoteFileChooser.setDialogTitle("Load reference bed files");
        remoteFileChooser.addChoosableFileFilter(new VFSFileFilter("gzip file (*.gz)", "gz"));
        remoteFileChooser.addChoosableFileFilter(new VFSFileFilter("BED file (*.bed)", "bed"));
        remoteFileChooser.setMultiSelectionEnabled(true);
        RETURN_TYPE answer = remoteFileChooser.showOpenDialog(null);
        if (answer == RETURN_TYPE.APPROVE) {
            final FileObject fileObjects[] = remoteFileChooser.getSelectedFileObjects();
            if (fileObjects != null) {
                ObservableList<DataFile> fileList = clientConfiguration.getReferenceDataList();
                for (FileObject fileObject : fileObjects) {
                    DataFile dataFile = new DataFile();
                    dataFile.setDataSetName(fileObject.getName().getBaseName());
                    dataFile.setFilePath(fileObject.getName().getPath());
                    fileList.add(dataFile);
                    /* DZ
                    clientConfiguration.getLengthAnalysisController().invalidateTabs();
                     */
                }

                // cache.setFileList(fileList);
            }
        }
    }

    @FXML
    private void handleDelete() {
        ObservableList<DataFile> fileList = clientConfiguration.getReferenceDataList();
        DataFile dataFileToRemove = tableView.getSelectionModel().getSelectedItem();
        fileList.remove(dataFileToRemove);
        // cache.setFileList(fileList);
    }

    @FXML
    private void handleUp() {
        int indexOfSelectedDataFile = tableView.getSelectionModel().getSelectedIndex();
        if (indexOfSelectedDataFile != -1) {
            ObservableList<DataFile> fileList = clientConfiguration.getReferenceDataList();
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
            ObservableList<DataFile> fileList = clientConfiguration.getReferenceDataList();
            if (indexOfSelectedDataFile < fileList.size() - 1) {
                DataFile selectedDataFile = fileList.remove(indexOfSelectedDataFile);
                fileList.add(indexOfSelectedDataFile + 1, selectedDataFile);
                tableView.getSelectionModel().clearAndSelect(indexOfSelectedDataFile + 1);
            }
        }
    }

    @FXML
    private void handleStart() {
        this.setDisabled(true);
//        ObservableList<DataFile> fileList = tableView.getItems();
        String minSegmentLength = tfMinSegmentLength.getText();
        clientConfiguration.setMinSegmentLength(Integer.parseInt(minSegmentLength));
        startSegmentation();
    }

    public void startSegmentationBatch() {
        tfMinSegmentLength.setText(Integer.toString(clientConfiguration.getMinSegmentLength()));
        startSegmentation();
    }

    private void startSegmentation() {
        if (clientCommander.isActive()) {
            Object[] command = new Object[3];
            command[0] = Messages.SERVER_startSegmentation;
            Gson gson = new Gson();
            ObservableList<DataFile> fileList = clientConfiguration.getReferenceDataList();
            String dataFileList = gson.toJson(fileList);
            command[1] = dataFileList;
            command[2] = clientConfiguration.getMinSegmentLength();
            clientCommander.sendCommand(command);
        }
    }

    private void testForReady() {
        if (this.clientConfiguration != null
            && this.clientCommander != null
            && this.tableView != null) {
            this.init();
        }

    }

    public void setDisabled(boolean b) {
        this.tableView.setDisable(b);
        this.btnAddFile.setDisable(b);
        this.btnUpFile.setDisable(b);
        this.btnDownFile.setDisable(b);
        disableStart(b);
    }

    public void disableStart(boolean disable) {
        this.btnStart.setDisable(disable);
    }

    public ReadOnlyBooleanProperty getIsDisabledProp() {
        return this.tableView.disabledProperty();
    }
}
