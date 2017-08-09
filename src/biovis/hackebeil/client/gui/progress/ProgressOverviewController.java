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

import biovis.hackebeil.client.commander.ClientCommander;
import biovis.hackebeil.client.data.ClientConfiguration;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;

/**
 * @author nhinzmann, Alrik Hausdorf, Dirk Zeckzer
 *
 */
public class ProgressOverviewController {

    private ClientConfiguration clientConfiguration;
    private ClientCommander clientCommander;
    private Boolean isInitialized = false;

    private AnchorPane progressOverview;

    private ProgressReferenceGenomeController referenceGenomeController;
    private ProgressSegmentationController segmentationController;
    private ProgressAdditionalMeasurementsController additionalDataController;
    private ProgressAddMotifsController selectMotifsController;
    private ProgressAddPWMController selectPWMController;

    @FXML
    private Accordion accordion;

    @FXML
    private TitledPane referenceGenome;
    @FXML
    private TitledPane segmentation;
    @FXML
    private TitledPane additionalMeasurements;
    @FXML
    private TitledPane selectMotifs;
    @FXML
    private TitledPane selectPWM;

    @FXML
    private AnchorPane anchorReferenceGenome;
    @FXML
    private AnchorPane anchorSegmentation;
    @FXML
    private AnchorPane anchorAdditionalData;
    @FXML
    private AnchorPane anchorSelectMotifs;
    @FXML
    private AnchorPane anchorSelectPWM;

    public ProgressOverviewController() {
    }

    /**
     *
     * @param rootPane
     */
    public void setPane(AnchorPane rootPane) {
        this.progressOverview = rootPane;
    }

    /**
     *
     * @return
     */
    public static ProgressOverviewController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(ProgressOverviewController.class.getResource("ProgressOverview.fxml"));
            AnchorPane progressOverview = (AnchorPane) loader.load();
            ProgressOverviewController progressOverviewController = loader.<ProgressOverviewController>getController();
            progressOverviewController.setPane(progressOverview);
            return progressOverviewController;
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
    public AnchorPane loadProgressOverview(
        ClientConfiguration clientConfiguration,
        ClientCommander commander
    ) {
        this.clientConfiguration = clientConfiguration;
        this.clientCommander = commander;
        testForReady();

        return progressOverview;
    }

    public void initialize() {
        isInitialized = true;
        ObservableList<TitledPane> titlePaneList = accordion.getPanes();
        accordion.setExpandedPane(titlePaneList.get(0));
        testForReady();
    }

    private void initNextController() {
        referenceGenomeController = ProgressReferenceGenomeController.getInstance();
        segmentationController = ProgressSegmentationController.getInstance();
        additionalDataController = ProgressAdditionalMeasurementsController.getInstance();
        selectMotifsController = ProgressAddMotifsController.getInstance();
        selectPWMController = ProgressAddPWMController.getInstance();
    }

    private void fillWithContent() {
        anchorReferenceGenome = referenceGenomeController.loadProgressReferenceGenome(clientConfiguration, clientCommander);
        referenceGenome.setContent(anchorReferenceGenome);

        anchorSegmentation = segmentationController.loadProgressSegmentation(clientConfiguration, clientCommander);
        segmentation.setContent(anchorSegmentation);

        anchorAdditionalData = additionalDataController.loadProgressAdditionalMeasurements(clientConfiguration, clientCommander);
        additionalMeasurements.setContent(anchorAdditionalData);

        anchorSelectMotifs = selectMotifsController.loadProgressAddMotifs(clientConfiguration, clientCommander);
        selectMotifs.setContent(anchorSelectMotifs);

        anchorSelectPWM = selectPWMController.loadProgressAddPWM(clientConfiguration, clientCommander);
        selectPWM.setContent(anchorSelectPWM);
    }

    /**
     *
     * @param number 
     */
    public void openTitledPane(int number) {
        switch (number) {
            case 0:
                this.accordion.setExpandedPane(this.referenceGenome);
                break;
            case 1:
                this.accordion.setExpandedPane(this.segmentation);
                break;
            case 2:
                this.accordion.setExpandedPane(this.additionalMeasurements);
                break;
            case 3:
                this.accordion.setExpandedPane(this.selectMotifs);
                break;
            case 4:
                this.accordion.setExpandedPane(this.selectPWM);
                break;
        }
    }

    private void testForReady() {
        if (clientConfiguration != null
            && clientCommander != null
            && isInitialized) {
            initNextController();
            fillWithContent();
            clientConfiguration.setProgressOverviewController(this);
        }
    }

    /**
     *
     */
    public void resetProgress() {
        clientConfiguration.clear();
        setBtnsDisabled(false);
        referenceGenomeController.clear();
        segmentationController.setDisabled(true);
        additionalDataController.setDisabled(true);
        selectMotifsController.setDisabled(true);
        selectPWMController.setDisabled(true);
    }

    public void setBtnsDisabled(boolean disabled) {
        referenceGenomeController.setDisabled(disabled);
        segmentationController.setDisabled(disabled);
        additionalDataController.setDisabled(disabled);
        selectMotifsController.setDisabled(disabled);
        selectPWMController.setDisabled(disabled);
    }

    /**
     *
     */
    public void updateSegmentationResults() {
        Platform.runLater(() -> {
            segmentationController.setDisabled(false);
            additionalDataController.setDisabled(false);
            selectMotifsController.setDisabled(false);
            selectPWMController.setDisabled(false);
        });
    }

    /**
     *
     */
    public void updateAdditionalDataTabs() {
        Platform.runLater(() -> {
            additionalDataController.setDisabled(false);
        });
    }

    /**
     *
     */
    public void updateMotifsTabs() {
        Platform.runLater(() -> {
            selectMotifsController.setDisabled(false);
        });
    }

    /**
     *
     */
    public void updatePWMTabs() {
        Platform.runLater(() -> {
            selectPWMController.setDisabled(false);
        });
    }

    /**
     *
     */
    public void indexWorkerSuccess() {
        Platform.runLater(() -> {
            referenceGenomeController.setDisabled(false);
            segmentationController.setDisabled(false);
        });
    }
}
