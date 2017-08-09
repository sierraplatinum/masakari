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
package biovis.hackebeil.client.gui.dialog;

import java.util.logging.Logger;

import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.client.gui.RootLayoutController;
import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.common.data.Motif;
import biovis.hackebeil.common.data.PositionWeightMatrix;
import java.io.IOException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author Alrik Hausdorf
 *
 */
public class ImportDialogController {

    private static final Logger log = Logger.getLogger(ImportDialogController.class.getName());

    private Stage stage;

    @FXML
    private CheckBox cbRefGenome;
    @FXML
    private ProgressIndicator progressRefGenome;

    @FXML
    private Label lbSegmentation;
    @FXML
    private CheckBox cbSegmentation;
    @FXML
    private ProgressIndicator progressSegmentation;

    @FXML
    private Label lbAddData;
    @FXML
    private CheckBox cbAddData;
    @FXML
    private ProgressIndicator progressAddData;

    @FXML
    private Label lbMotifs;
    @FXML
    private CheckBox cbMotifs;
    @FXML
    private ProgressIndicator progressMotifs;

    @FXML
    private Label lbPWM;
    @FXML
    private CheckBox cbPWM;
    @FXML
    private ProgressIndicator progressPWM;

    @FXML
    private Button btnClose;

    private ClientConfiguration clientConfiguration;

    private boolean loadRefenceGenomeStarted;
    private boolean segmentationStarted;
    private boolean additionalDataStarted;
    private boolean computeMotifCoverageStarted;
    private boolean computePWMStarted;

    /**
     *
     */
    public ImportDialogController() {
        loadRefenceGenomeStarted = false;
        segmentationStarted = false;
        additionalDataStarted = false;
        computeMotifCoverageStarted = false;
        computePWMStarted = false;
    }

    /**
     *
     * @param window
     * @param clientConfiguration
     */
    public static void createDialog(
        Window window,
        ClientConfiguration clientConfiguration
    ) {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(ImportDialogController.class.getResource("ImportDialog.fxml"));
            AnchorPane root = loader.load();

            // Setup stage
            Stage stage = new Stage();
            stage.getIcons().add(new Image(RootLayoutController.class.getResource("masakari.png").toExternalForm()));
            stage.setScene(new Scene(root));
            stage.setTitle("Import configuration");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(window);

            // create ImportDialogController
            ImportDialogController controller = loader.<ImportDialogController>getController();
            controller.setClientConfiguration(clientConfiguration);
            controller.setStage(stage);
            stage.showAndWait();
        } catch (IOException e) {
            // TODO Automatisch generierter Erfassungsblock
            e.printStackTrace();
        }
    }

    public void initialize() {
    }

    /**
     *
     * @param clientConfiguration
     */
    public void setClientConfiguration(
        ClientConfiguration clientConfiguration
    ) {
        this.clientConfiguration = clientConfiguration;

        ObservableList<DataFile> segList = this.clientConfiguration.getReferenceDataList();
        if (!segList.isEmpty()) {
            this.lbSegmentation.setText(segList.size() + "");
        } else {
            cbSegmentation.setVisible(false);
        }

        ObservableList<DataFile> addList = this.clientConfiguration.getAdditionalDataList();
        if (!addList.isEmpty()) {
            this.lbAddData.setText(addList.size() + "");
        } else {
            cbAddData.setVisible(false);
        }

        ObservableList<Motif> motifList = this.clientConfiguration.getMotifList();
        if (!motifList.isEmpty()) {
            this.lbMotifs.setText(motifList.size() + "");
        } else {
            cbMotifs.setVisible(false);
        }

        ObservableList<PositionWeightMatrix> pwmList = this.clientConfiguration.getPWMList();
        if (!pwmList.isEmpty()) {
            this.lbPWM.setText(pwmList.size() + "");
        } else {
            cbPWM.setVisible(false);
        }

        start();
    }

    @FXML
    private void close() {
        stage.close();
    }

    @FXML
    private void start() {
        new Thread(() -> {
            loadRefGenome();
        }).start();
    }

    private void loadRefGenome() {
        // Set visible elements
        cbRefGenome.setVisible(false);
        progressRefGenome.setVisible(true);

        clientConfiguration.getReferenceGenomeLoaded().addListener((observable) -> {
            if (!segmentationStarted) {
                Platform.runLater(() -> {
                    cbRefGenome.setVisible(true);
                    progressRefGenome.setVisible(false);
                    cbRefGenome.setSelected(true);
                    loadSegmentation();
                });
            }
        });
        clientConfiguration.getProgressReferenceGenomeController().loadImportedIndex();
    }

    private void loadSegmentation() {
        if (clientConfiguration.getReferenceDataList().size() <= 0) {
            return;
        }

        segmentationStarted = true;

        // Set visible elements
        cbSegmentation.setVisible(false);
        progressSegmentation.setVisible(true);
        clientConfiguration.getProgressOverviewController().openTitledPane(1);

        clientConfiguration.getSegmentationReady().addListener((observable) -> {
            if (!additionalDataStarted) {
                Platform.runLater(() -> {
                    cbSegmentation.setVisible(true);
                    progressSegmentation.setVisible(false);
                    cbSegmentation.setSelected(true);
                    loadAdditionalData();
                });
            }
        });
        clientConfiguration.getProgressSegmentationController().startSegmentationBatch();
    }

    private void loadAdditionalData() {
        ObservableList<DataFile> dfImportList = clientConfiguration.getAdditionalDataList();
        if (dfImportList.isEmpty()) {
            computeMotifCoverage();
            return;
        }

        additionalDataStarted = true;

        // Set visible elements
        cbAddData.setVisible(false);
        progressAddData.setVisible(true);
        clientConfiguration.getProgressOverviewController().openTitledPane(2);

        clientConfiguration.getAdditionalDataReady().addListener((observable) -> {
            if (!computeMotifCoverageStarted) {
                Platform.runLater(() -> {
                    cbAddData.setVisible(true);
                    progressAddData.setVisible(false);
                    cbAddData.setSelected(true);
                    computeMotifCoverage();
                });
            }
        });
        clientConfiguration.getProgressAddAdditionalDataController().handleAdditionalInformation();
    }

    private void computeMotifCoverage() {
        ObservableList<Motif> motifImportList = clientConfiguration.getMotifList();
        if (motifImportList.isEmpty()) {
            computePWM();
            return;
        }

        computeMotifCoverageStarted = true;

        // Set visible elements
        cbMotifs.setVisible(false);
        progressMotifs.setVisible(true);
        clientConfiguration.getProgressOverviewController().openTitledPane(3);

        clientConfiguration.getMotifsReady().addListener((observable) -> {
            if (!computePWMStarted) {
                Platform.runLater(() -> {
                    cbMotifs.setVisible(true);
                    progressMotifs.setVisible(false);
                    cbMotifs.setSelected(true);
                    computePWM();
                });
            }
        });
        clientConfiguration.getProgressAddMotifsController().handleSearch();
    }

    private void computePWM() {
        ObservableList<PositionWeightMatrix> pwmImportList = clientConfiguration.getPWMList();
        if (pwmImportList.isEmpty()) {
            return;
        }

        computePWMStarted = true;

        // Set visible elements
        cbPWM.setVisible(false);
        progressPWM.setVisible(true);
        clientConfiguration.getProgressOverviewController().openTitledPane(4);

        clientConfiguration.getPWMsReady().addListener((observable) -> {
            //if (!after) {
            Platform.runLater(() -> {
                cbPWM.setVisible(true);
                progressPWM.setVisible(false);
                cbPWM.setSelected(true);
            });
            //}
        });
        clientConfiguration.getProgressAddPWMController().handleSearch();
    }

    private void setStage(Stage stage) {
        this.stage = stage;
    }
}
