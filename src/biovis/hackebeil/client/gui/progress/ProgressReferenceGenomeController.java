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

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.vfs2.FileObject;

import biovis.hackebeil.client.commander.ClientCommander;
import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.common.data.Messages;
import biovislib.vfsjfilechooser.VFSJFileChooser;
import biovislib.vfsjfilechooser.VFSJFileChooser.RETURN_TYPE;
import biovislib.vfsjfilechooser.filechooser.VFSFileFilter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

/**
 * @author nhinzmann, Alrik Hausdorf
 *
 */
public class ProgressReferenceGenomeController {

    private ClientConfiguration clientConfiguration;
    private ClientCommander clientCommander;
    private AnchorPane anchorReferenceGenome;
    private ObservableList<String> logList;

    @FXML
    private Button btnSelectGenome;
    @FXML
    private Button btnLoadIndex;
    @FXML
    private Button btnCreateIndex;
    @FXML
    private TextField tfGenomePath;
    @FXML
    private TextField tfGenomeIndexPath;
    @FXML
    private ProgressIndicator progressCreateIndex;
    @FXML
    private ListView<String> listView;

    public ProgressReferenceGenomeController() {
    }

    public void updateProgressIndicatorIsVisible(Boolean visible) {
        progressCreateIndex.setVisible(visible);

    }

    public void initLogList() {
        logList = listView.getItems();
        setListViewIsVisible(true);
    }

    public void updateLogList(String log) {
        logList.add(0, log);
    }

    public void setListViewIsVisible(boolean isVisible) {
        listView.setVisible(isVisible);
    }

    /**
     *
     * @param rootPane
     */
    public void setPane(AnchorPane rootPane) {
        this.anchorReferenceGenome = rootPane;
    }

    /**
     *
     * @return
     */
    public static ProgressReferenceGenomeController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(ProgressReferenceGenomeController.class.getResource("ProgressReferenceGenome.fxml"));
            AnchorPane anchorReferenceGenome = loader.load();
            ProgressReferenceGenomeController progressRefGenomeController = loader
                .<ProgressReferenceGenomeController>getController();
            progressRefGenomeController.setPane(anchorReferenceGenome);
            return progressRefGenomeController;
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
    public AnchorPane loadProgressReferenceGenome(
        ClientConfiguration clientConfiguration,
        ClientCommander commander
    ) {
        this.clientConfiguration = clientConfiguration;
        this.clientCommander = commander;
        testForReady();

        return anchorReferenceGenome;
    }

    public void updateLogs(String log) {
        // System.out.println("logs update in RefGenomeController " + log);
        if (!listView.isVisible()) {
            listView.setVisible(true);
        }
        Platform.runLater(() -> {
            ObservableList<String> logList1 = listView.getItems();
            logList1.add(0, log);
        });
    }

    @FXML
    public void handleLoadGenome() {
        clientConfiguration.initFileChooser();

        VFSJFileChooser remoteFileChooser = clientConfiguration.getRemoteFileChooser();
        remoteFileChooser.setDialogTitle("Load reference genome");
        remoteFileChooser.addChoosableFileFilter(new VFSFileFilter("FASTA file (*.fa)", "fa"));
        remoteFileChooser.addChoosableFileFilter(new VFSFileFilter("FASTA file (*.fa.gz)", "fa.gz"));
        RETURN_TYPE answer = remoteFileChooser.showOpenDialog(null);
        // System.out.println(answer);
        // check if a file was selected
        if (answer == RETURN_TYPE.APPROVE) {
            clientConfiguration.clear();
            setListViewIsVisible(false);
            final FileObject aFileObject = remoteFileChooser.getSelectedFileObject();
            String pathOfSelectedFile = aFileObject.getName().getPath();

            if (pathOfSelectedFile != null) {
                String remotePath = pathOfSelectedFile.substring(0,
                                                                 pathOfSelectedFile.indexOf(aFileObject.getName().getBaseName()));
                if (remotePath != null) {
                    clientConfiguration.setRemotePathForFileChooser(remotePath);
                }
                tfGenomePath.setText(pathOfSelectedFile);
                tfGenomePath.positionCaret(tfGenomePath.getLength());
                clientConfiguration.setSelectedGenomeFile(pathOfSelectedFile);

                // clear Index and Progress
                ObservableList<String> list = listView.getItems();
                list.clear();
                setListViewIsVisible(false);
                tfGenomeIndexPath.clear();
                // TODO Clear Indexfiles in cache
            }
        }
    }

    @FXML
    private void handleLoadIndex() {
        clientConfiguration.initFileChooser();
        VFSJFileChooser remoteFileChooser = clientConfiguration.getRemoteFileChooser();
        remoteFileChooser.setDialogTitle("Load Index");
        remoteFileChooser.addChoosableFileFilter(new VFSFileFilter("Index file (*.idx)", "idx"));
        RETURN_TYPE answer = remoteFileChooser.showOpenDialog(null);
        if (answer == RETURN_TYPE.APPROVE) {
            final FileObject aFileObject = remoteFileChooser.getSelectedFileObject();
            String pathOfSelectedFile = aFileObject.getName().getPath();
            if (pathOfSelectedFile != null) {
                // clear Progress
                ObservableList<String> list = listView.getItems();
                list.clear();

                tfGenomeIndexPath.setText(pathOfSelectedFile);
                tfGenomeIndexPath.positionCaret(tfGenomeIndexPath.getLength());
                clientConfiguration.setSelectedLoadIndexFilePath(pathOfSelectedFile);

                if (clientCommander.isActive()) {
                    Object[] filePath = new Object[2];
                    filePath[0] = clientConfiguration.getSelectedGenomeFilePath();
                    filePath[1] = clientConfiguration.getSelectedLoadIndexFilePath();
                    Object[] command = new Object[2];
                    command[0] = Messages.SERVER_loadIndex;
                    command[1] = filePath;
                    clientCommander.sendCommand(command);
                }
            }
        }
    }

    public void loadImportedIndex() {
        tfGenomePath.setText(clientConfiguration.getSelectedGenomeFilePath());
        tfGenomePath.positionCaret(tfGenomePath.getLength());
        tfGenomeIndexPath.setText(clientConfiguration.getSelectedLoadIndexFilePath());
        tfGenomeIndexPath.positionCaret(tfGenomeIndexPath.getLength());
        if (clientCommander.isActive()) {
            Object[] filePath = new Object[2];
            filePath[0] = clientConfiguration.getSelectedGenomeFilePath();
            filePath[1] = clientConfiguration.getSelectedLoadIndexFilePath();
            Object[] command = new Object[2];
            command[0] = Messages.SERVER_loadIndex;
            command[1] = filePath;
            clientCommander.sendCommand(command);
        }
    }

    @FXML
    void handleCreateIndex() {
        clientConfiguration.initFileChooser();
        VFSJFileChooser remoteFileChooser = clientConfiguration.getRemoteFileChooser();
        remoteFileChooser.setDialogTitle("Save Index");
        remoteFileChooser.addChoosableFileFilter(new VFSFileFilter("Index file (*.idx)", "idx"));
        RETURN_TYPE answer = remoteFileChooser.showSaveDialog(null);
        if (answer == RETURN_TYPE.APPROVE) {
            final FileObject aFileObject = remoteFileChooser.getSelectedFileObject();
            String pathOfSelectedFile = aFileObject.getName().getPath();
            if (pathOfSelectedFile != null) {
                ObservableList<String> list = listView.getItems();
                list.clear();
                File saveFile = new File(pathOfSelectedFile);
                if (saveFile.getName().endsWith(".idx") == false) {
                    saveFile = new File(saveFile.getAbsolutePath() + ".idx");
                }
                if (saveFile.exists()) {
                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle("File already exists?");
                    alert.setHeaderText(saveFile.getName() + " already exists.");
                    alert.setContentText("Do you want to overwrite the existing file?");
                    ButtonType buttonTypeYes = new ButtonType("Yes");
                    ButtonType buttonTypeNo = new ButtonType("No");
                    ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() != buttonTypeYes) {
                        handleCreateIndex();
                        return;
                    }
                }
                tfGenomeIndexPath.setText(saveFile.getAbsolutePath());
                tfGenomeIndexPath.positionCaret(tfGenomeIndexPath.getLength());
                clientConfiguration.setSelectedSaveIndexFilePath(saveFile.getAbsolutePath());
                // System.out.println("clientCommander.isActive() "
                // +clientCommander.isActive());

                if (clientCommander.isActive()) {
                    Object[] filePath = new Object[2];
                    filePath[0] = clientConfiguration.getSelectedGenomeFilePath();
                    filePath[1] = clientConfiguration.getSelectedSaveIndexFilePath();
                    Object[] command = new Object[2];
                    command[0] = Messages.SERVER_createIndex;
                    command[1] = filePath;
                    clientCommander.sendCommand(command);
                }
            }
        }

    }

    private void testForReady() {
        if (clientConfiguration != null
            && clientCommander != null
            && btnCreateIndex != null) {
            init();
        }
    }

    private void init() {
        clientConfiguration.setProgressReferenceGenomeController(this);
    }

    public void setDisabled(boolean b) {
        this.btnCreateIndex.setDisable(b);
        this.tfGenomeIndexPath.setDisable(b);
        this.btnLoadIndex.setDisable(b);
        this.tfGenomePath.setDisable(b);
        this.btnSelectGenome.setDisable(b);
    }

    public void clear() {
        this.tfGenomeIndexPath.clear();
        this.tfGenomePath.clear();
        this.setListViewIsVisible(false);
        if (logList == null) {
            initLogList();
        }
        logList.clear();
    }
}
