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
package biovis.hackebeil.client.gui;

import biovis.hackebeil.client.commander.ClientCommander;
import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.client.gui.dialog.ConnectionManagerController;
import biovis.hackebeil.client.gui.dialog.ImportDialogController;
import biovis.hackebeil.client.gui.dialog.ProgressDialog;
import biovis.hackebeil.client.io.DataIO;
import biovis.hackebeil.common.data.BreakSegment;
import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.common.data.Messages;
import biovis.hackebeil.server.worker.segmentation.SegmentationWorker;
import biovislib.vfsjfilechooser.VFSJFileChooser;
import biovislib.vfsjfilechooser.VFSJFileChooser.RETURN_TYPE;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.apache.commons.vfs2.FileObject;

/**
 * @author nhinzmann
 *
 */
public class RootLayoutController {

    private static final Logger log = Logger.getLogger(StatusBarController.class.getName());

    private BorderPane rootLayout;

    @FXML
    private BorderPane rootBorderPane;

    private Scene scene;
    private Stage primaryStage;
    private SplitScreenController splitScreen;
    private StatusBarController statusBarController;

    private ClientConfiguration clientConfiguration;
    private ClientCommander clientCommander;
    private ConnectionManagerController connectionManagercontroller;

    private boolean isInitialized = false;
    private boolean connectionDone = false;

    public RootLayoutController() {
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        testForReady();
    }

    public ClientCommander getClientCommander() {
        return this.clientCommander;
    }

    public BorderPane getRootBorderPane() {
        return this.rootBorderPane;
    }

    public void setPane(BorderPane rootPane) {
        this.rootLayout = rootPane;
    }

    public static RootLayoutController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            // Load root layout from fxml file.
            loader.setLocation(RootLayoutController.class.getResource("RootLayout.fxml"));
            BorderPane rootLayout = (BorderPane) loader.load();
            RootLayoutController rootLayoutController = loader.<RootLayoutController>getController();
            rootLayoutController.setPane(rootLayout);
            return rootLayoutController;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Scene initRootLayout() {
        // Init configuration and commander
        clientConfiguration = new ClientConfiguration();
        clientCommander = new ClientCommander(this);
        testForReady();
        addSplitScreen();
        addStatusBar();
        // Show the scene containing the root layout.
        scene = new Scene(rootLayout);
        rootLayout.getStylesheets().clear();


		Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
		System.out.println("height: "+ visualBounds.getHeight());
		System.out.println("width " + visualBounds.getWidth());


		if(visualBounds.getWidth() > 1920.0)
		{
			 scene.getStylesheets().add(getClass().getResource("font4k.css").toExternalForm());
			 System.out.println("Loading 4k css stylesheet");
		}
		else
		{
        scene.getStylesheets().add(getClass().getResource("fonthd.css").toExternalForm());
		 System.out.println("Loading HD css stylesheet");
		}
        return scene;
    }

    public void initialize() {
        isInitialized = true;
        testForReady();
    }

    private void testForReady() {
        if (clientCommander != null
            && primaryStage != null
            && isInitialized == true) {
            clientCommander.setRootLayoutController(this);
            if (connectionDone == false) {
                connectionManagercontroller = ConnectionManagerController.getInstance();
                connectionManagercontroller.createServerDialog(this);
                connectionDone = true;
            }
        }
    }

    private void addSplitScreen() {
        splitScreen = SplitScreenController.getInstance();
        AnchorPane splitScreenView = splitScreen.initSplitScreen(clientConfiguration, clientCommander);
        rootLayout.setCenter(splitScreenView);
    }

    private void addStatusBar() {
        statusBarController = StatusBarController.getInstance();
        BorderPane statusBarView = statusBarController.initStatusBar(clientCommander);
        rootLayout.setBottom(statusBarView);
    }

    @FXML
    public void handleQuit() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (clientCommander.isActive()) {
                    Object[] close = {"QUIT", "QUIT"};
                    clientCommander.sendCommand(close);
                    clientCommander.ack();
                }
                System.exit(0);
            }
        });
    }

    @FXML
    private void resetProgram() {
        splitScreen.resetProgress();
    }

    @FXML
    private void openServerConnection() {
        connectionManagercontroller = ConnectionManagerController.getInstance();
        connectionManagercontroller.createServerDialog(this);
        connectionDone = true;
    }

    @FXML
    private void handleExportTable() {
        clientConfiguration.initFileChooser();
        VFSJFileChooser remoteFileChooser = clientConfiguration.getRemoteFileChooser();
        remoteFileChooser.setDialogTitle("Export Table");
        RETURN_TYPE answer = remoteFileChooser.showSaveDialog(null);
        if (answer == RETURN_TYPE.APPROVE) {
            final FileObject aFileObject = remoteFileChooser.getSelectedFileObject();
            String pathOfSelectedFile = aFileObject.getName().getPath();

            if (!pathOfSelectedFile.endsWith(".data") && !pathOfSelectedFile.endsWith(".gz")) {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("File-extension missing");
                alert.setHeaderText("You can save as .data or as .data.gz (Gzip)");
                alert.setContentText("Wich one do you prefer?");
                ButtonType buttonTypePlain = new ButtonType(".data");
                ButtonType buttonTypeGzip = new ButtonType(".data.gz");
                ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(buttonTypePlain, buttonTypeGzip, buttonTypeCancel);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonTypePlain) {
                    checkSaveTargetPath(pathOfSelectedFile + ".data");
                } else if (result.get() == buttonTypeGzip) {
                    checkSaveTargetPath(pathOfSelectedFile + ".data.gz");
                }
            } else {
                checkSaveTargetPath(pathOfSelectedFile);
            }
        }
    }

    @FXML
    public void exportClientConfiguration() {
        DataIO.exportClientConfigurationToFile(clientConfiguration, null);
    }

    @FXML
    public void importClientConfiguration() {
        // load and update non-transient data
        ClientConfiguration importClientConfiguration = DataIO.importClientConfigurationFromFile(null);
        if (importClientConfiguration == null) {
            return;
        }
        clientConfiguration.clear();
        clientConfiguration.update(importClientConfiguration);

        // clear server data
        Object[] command = new Object[2];
        command[0] = Messages.SERVER_clear;
        command[1] = Messages.SERVER_clear;
        clientCommander.sendCommand(command);

        // handle import
        ImportDialogController.createDialog(
            rootBorderPane.getScene().getWindow(),
            clientConfiguration
        );
    }

    @FXML
    public void exportToPng() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("png files (*.png", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Save figures as png file");
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            splitScreen.exportToPng(file.getAbsoluteFile().getAbsolutePath());
        }
    }

    private void checkSaveTargetPath(String file) {
        File saveFile = new File(file);
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
                handleExportTable();
            } else {
                exportData(saveFile.getPath());
            }
        } else {
            exportData(saveFile.getPath());
        }
    }

    private void exportData(
        String filePath
    ) {
        ProgressDialog progress = new ProgressDialog("Export to " + filePath);
        Platform.runLater(() -> {
            progress.start();
            if (clientCommander.isActive()) {
                Object[] close = {Messages.SERVER_exportSegmentation, filePath};
                clientCommander.sendCommand(close);
                clientCommander.ack();
            }
            progress.close();
        });
    }

    public StatusBarController getStatusBarController() {
        return statusBarController;
    }

    /**
     *
     * @param segmentationResults
     */
    public void updateSegmentationResults(
        SegmentationWorker segmentationResults
    ) {
        splitScreen.updateSegmentationResults(segmentationResults);
        clientConfiguration.setSegmentationReady(true);
    }

    /**
     *
     * @param numberOfDroppedPeaks
     */
    public void updateSegmentationDroppedPeaks(
        int numberOfDroppedPeaks
    ) {
        splitScreen.updateSegmentationDroppedPeaks(numberOfDroppedPeaks);
    }

    /**
     *
     * @param df data file
     */
    public void updateSegmentationLength(DataFile df) {
        clientConfiguration.updateSegmentationLength(df);
    }

    /**
     *
     * @param segmentationCodeCounts
     */
    public void updateCodeDistribution(
        SortedMap<Integer, Integer> segmentationCodeCounts
    ) {
        splitScreen.updateCodeDistribution(segmentationCodeCounts);
    }

    /**
     *
     * @param additionalValues
     */
    public void updateAdditionalDataTabs(
        Map<String, List<Double>> additionalValues
    ) {
        splitScreen.updateAdditionalDataTabs(additionalValues);
        clientConfiguration.setAdditionalDataReady(true);
    }

    /**
     *
     * @param motifValues
     */
    public void updateMotifDataTabs(
        Map<String, List<Double>> motifValues
    ) {
        splitScreen.updateMotifDataTabs(motifValues);
        clientConfiguration.setMotifsReady(true);
    }

    /**
     *
     * @param pwmValues
     */
    public void updatePWMDataTabs(
        Map<String, List<Double>> pwmValues
    ) {
        splitScreen.updatePWMDataTabs(pwmValues);
        clientConfiguration.setPwmsReady(true);
    }

    public void setCorrelationData(double[][] data) {
        splitScreen.setCorrelationData(data);
    }

    public void setFateOfCodeData(List<int[][]> data) {
        splitScreen.setFateOfCodeData(data);
    }

    public void setBreakSegmentData(List<BreakSegment> breakSegmentData) {
        splitScreen.setBreakSegmentData(breakSegmentData);
    }

    public void setSegmentPairsData(Map<String, Integer> segmentPairsData) {
        splitScreen.setSegmentPairsData(segmentPairsData);
    }

    public void setSegmentShortSegmentPairsData(Map<String, Integer> segmentShortSegmentPairsData) {
        splitScreen.setSegmentShortSegmentPairsData(segmentShortSegmentPairsData);
    }

    public void setShortSegmentSegmentPairsData(Map<String, Integer> shortSegmentSegmentPairsData) {
        splitScreen.setShortSegmentSegmentPairsData(shortSegmentSegmentPairsData);
    }

    public void setSegmentPairOccurrenceData(Map<String, Double> segmentPairOccurrenceData) {
        splitScreen.setSegmentPairOccurrenceData(segmentPairOccurrenceData);
    }

    public void setShortSegmentChainsCounts(SortedMap<Integer, Integer> shortSegmentChainsCounts) {
        splitScreen.setShortSegmentChainsCounts(shortSegmentChainsCounts);
    }

    public void setShortSegmentChainsLengths(SortedMap<Integer, Integer> shortSegmentChainsLengths) {
        splitScreen.setShortSegmentChainsLengths(shortSegmentChainsLengths);
    }

    /**
     *
     * @param log
     */
    public void updateLogAtRefGenomeController(
        String log
    ) {
        clientConfiguration.updateLogAtRefGenomeController(log);
        clientConfiguration.setReferenceGenomeLoaded(true);
    }

    /**
     *
     */
    public void indexWorkerSuccess() {
        splitScreen.indexWorkerSuccess();
    }
}
