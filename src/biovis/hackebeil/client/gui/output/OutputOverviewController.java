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
package biovis.hackebeil.client.gui.output;

import java.io.IOException;

import biovis.hackebeil.client.commander.ClientCommander;
import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.client.gui.output.additionalData.AdditionalDataTabPaneController;
import biovis.hackebeil.client.gui.output.breakSegmentAnalysis.BreakSegmentAnalysisController;
import biovis.hackebeil.client.gui.output.correlation.CorrelationController;
import biovis.hackebeil.client.gui.output.fateOfCode.FateOfCodeController;
import biovis.hackebeil.client.gui.output.lengthAnalysis.LengthAnalysisRootController;
import biovis.hackebeil.client.gui.output.pairsAnalysis.PairsAnalysisController;
import biovis.hackebeil.client.gui.output.motif.MotifTabPaneController;
import biovis.hackebeil.client.gui.output.overview.OverviewController;
import biovis.hackebeil.client.gui.output.pwm.PWMTabPaneController;
import biovis.hackebeil.client.gui.output.segmentation.SegmentationTabPaneController;
import biovis.hackebeil.common.data.BreakSegment;
import biovis.hackebeil.server.worker.segmentation.SegmentationWorker;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

/**
 * @author nhinzmann
 *
 */
public class OutputOverviewController {

    private ClientConfiguration clientConfiguration;
    private ClientCommander clientCommander;

    private Boolean isInitialized = false;

    private AnchorPane rootPane;

    //private StatisticalInformationController statisticalInformationController;
    private OverviewController overviewRootController;
    private SegmentationTabPaneController segmentationTabPaneController;
    private LengthAnalysisRootController lengthAnalysisRootController;
    private PairsAnalysisController pairsAnalysisController;
    private BreakSegmentAnalysisController breakSegmentAnalysisController;

    private AdditionalDataTabPaneController additionalMeasurementsTabPaneController;
    private FateOfCodeController fateOfCodeController;

    private MotifTabPaneController motifTabPaneController;

    private PWMTabPaneController pwmTabPaneController;

    private CorrelationController correlationController;

    @FXML
    private TabPane tabs;

    private Tab fateOfCodeTab;
    private Tab correlationTab;

    public OutputOverviewController() {
    }

    private void setRootPane(
        AnchorPane rootPane
    ) {
        this.rootPane = rootPane;
    }

    public static OutputOverviewController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(OutputOverviewController.class.getResource("OutputOverview.fxml"));
            AnchorPane rootPane = loader.load();
            OutputOverviewController outputOverviewController = loader.<OutputOverviewController>getController();
            outputOverviewController.setRootPane(rootPane);
            return outputOverviewController;
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
    public AnchorPane loadOutputOverview(
        ClientConfiguration clientConfiguration,
        ClientCommander commander
    ) {
        this.clientConfiguration = clientConfiguration;
        this.clientCommander = commander;
        testForReady();

        return rootPane;
    }

    public void initialize() {
        isInitialized = true;
        testForReady();
    }

    private void initView() {
        // Segmentation
        overviewRootController = OverviewController.getInstance();
        Platform.runLater(() -> {
            Tab tab = overviewRootController.loadView();
            tabs.getTabs().add(tab);
        });

        segmentationTabPaneController = SegmentationTabPaneController.getInstance();
        Platform.runLater(() -> {
            Tab tab = segmentationTabPaneController.loadView(clientConfiguration);
            tabs.getTabs().add(tab);
            segmentationTabPaneController.initView();
        });

        lengthAnalysisRootController = LengthAnalysisRootController.getInstance();
        Platform.runLater(() -> {
            Tab tab = lengthAnalysisRootController.loadView(clientConfiguration);
            tabs.getTabs().add(tab);
        });

        pairsAnalysisController = PairsAnalysisController.getInstance();
        Platform.runLater(() -> {
            Tab tab = pairsAnalysisController.loadView(clientConfiguration);
            tabs.getTabs().add(tab);
        });

        breakSegmentAnalysisController = BreakSegmentAnalysisController.getInstance();
        Platform.runLater(() -> {
            Tab tab = breakSegmentAnalysisController.loadView(clientConfiguration);
            tabs.getTabs().add(tab);
        });

        // Additional Measurements
        additionalMeasurementsTabPaneController = AdditionalDataTabPaneController.getInstance();
        Platform.runLater(() -> {
            Tab tab = additionalMeasurementsTabPaneController.loadView(clientConfiguration);
            tabs.getTabs().add(tab);
        });

        fateOfCodeController = FateOfCodeController.getInstance();
        Platform.runLater(() -> {
            fateOfCodeTab = fateOfCodeController.loadView(clientConfiguration, clientCommander);
            tabs.getTabs().add(fateOfCodeTab);
        });

        motifTabPaneController = MotifTabPaneController.getInstance();
        Platform.runLater(() -> {
            Tab tab = motifTabPaneController.loadView(clientConfiguration);
            tabs.getTabs().add(tab);
        });

        pwmTabPaneController = PWMTabPaneController.getInstance();
        Platform.runLater(() -> {
            Tab tab = pwmTabPaneController.loadView(clientConfiguration);
            tabs.getTabs().add(tab);
        });

        correlationController = CorrelationController.getInstance();
        Platform.runLater(() -> {
            correlationTab = correlationController.loadView(clientConfiguration, clientCommander);
            tabs.getTabs().add(correlationTab);
        });
    }

    private void init() {
    }

    private void testForReady() {
        if (clientConfiguration != null
            && clientCommander != null
            && isInitialized == true) {
            initView();
            init();
        }
    }

    /**
     *
     * @param segmentationResults
     */
    public void updateSegmentationResults(
        SegmentationWorker segmentationResults
    ) {
        overviewRootController.setSegmentInformation(segmentationResults);
        segmentationTabPaneController.updateSegmentLengthDistribution(segmentationResults.getSegmentLengths());
        segmentationTabPaneController.updateShortSegmentLengthDistribution(segmentationResults.getShortSegmentsLengths());
        lengthAnalysisRootController.update(segmentationResults);
    }

    /**
     *
     * @param numberOfDroppedPeaks
     */
    public void updateSegmentationDroppedPeaks(
        int numberOfDroppedPeaks
    ) {
        overviewRootController.setSegmentationDroppedPeaks(numberOfDroppedPeaks);
    }

    /**
     *
     * @param segmentationCodeCounts
     */
    public void updateCodeDistribution(
        SortedMap<Integer, Integer> segmentationCodeCounts
    ) {
        segmentationTabPaneController.updateCodeDistribution(segmentationCodeCounts);
        correlationTab.setDisable(false);
    }

    /**
     *
     * @param additionalValues
     */
    public void updateAdditionalMeasurementsTabs(
        Map<String, List<Double>> additionalValues
    ) {
        overviewRootController.setAdditionalMeasurementsInformation(additionalValues.size());
        additionalMeasurementsTabPaneController.updateTabs(additionalValues);

        if (additionalValues != null
            && !additionalValues.isEmpty()) {
            fateOfCodeTab.setDisable(false);
            fateOfCodeController.resetSetup();
        }
    }

    /*
     *
     * @param motifValues
     */
    public void updateMotifDataTabs(
        Map<String, List<Double>> motifValues
    ) {
        overviewRootController.setMotifInformation(motifValues.size());
        motifTabPaneController.updateTabs(motifValues);
    }

    /*
     *
     * @param motifValues
     */
    public void updatePWMDataTabs(
        Map<String, List<Double>> pwmValues
    ) {
        overviewRootController.setPWMInformation(pwmValues.size());
        pwmTabPaneController.updateTabs(pwmValues);
    }

    public void setCorrelationData(double[][] data) {
        correlationController.updateCorrelationData(data);
    }

    public void setFateOfCodeResults(List<int[][]> results) {
        fateOfCodeController.setFateOfCodeResults(results);
    }

    public void setSegmentPairsData(Map<String, Integer> segmentPairsData) {
        pairsAnalysisController.updateSegmentPairsData(segmentPairsData);
    }

    public void setSegmentShortSegmentPairsData(Map<String, Integer> segmentShortSegmentPairsData) {
        pairsAnalysisController.updateSegmentShortSegmentPairsData(segmentShortSegmentPairsData);
    }

    public void setShortSegmentSegmentPairsData(Map<String, Integer> shortSegmentSegmentPairsData) {
        pairsAnalysisController.updateShortSegmentSegmentPairsData(shortSegmentSegmentPairsData);
    }

    public void setSegmentPairOccurrenceData(Map<String, Double> segmentPairOccurrenceData) {
        pairsAnalysisController.updateSegmentPairOccurrenceData(segmentPairOccurrenceData);
    }

    public void setBreakSegmentData(List<BreakSegment> breakSegmentData) {
        breakSegmentAnalysisController.setBreakSegmentData(breakSegmentData);
    }

    public void setShortSegmentChainsCounts(SortedMap<Integer, Integer> shortSegmentChainsCounts) {
        segmentationTabPaneController.updateShortSegmentChainsCounts(shortSegmentChainsCounts);
    }

    public void setShortSegmentChainsLengths(SortedMap<Integer, Integer> shortSegmentChainsLengths) {
        segmentationTabPaneController.updateShortSegmentChainsLengths(shortSegmentChainsLengths);
    }

    public void exportToPng(String filename) {
        segmentationTabPaneController.exportToPng(filename);
        lengthAnalysisRootController.exportToPng(filename);
        pairsAnalysisController.exportToPng(filename);
        breakSegmentAnalysisController.exportToPng(filename);

        additionalMeasurementsTabPaneController.exportToPng(filename);
        fateOfCodeController.saveMatrixToFile(filename);

        motifTabPaneController.exportToPng(filename);

        pwmTabPaneController.exportToPng(filename);

        correlationController.exportToPng(filename);
    }
}
