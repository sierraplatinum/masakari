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

import java.io.IOException;

import biovis.hackebeil.client.commander.ClientCommander;
import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.client.gui.output.OutputOverviewController;
import biovis.hackebeil.client.gui.progress.ProgressOverviewController;
import biovis.hackebeil.common.data.BreakSegment;
import biovis.hackebeil.server.worker.segmentation.SegmentationWorker;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;

/**
 * @author nhinzmann
 *
 */
public class SplitScreenController {

    private Boolean isInitialized = false;

    private ClientConfiguration clientConfiguration;
    private ClientCommander clientCommander;

    private AnchorPane splitScreen;

    private ProgressOverviewController progressOverview;
    private OutputOverviewController outputOverview;

    @FXML
    private SplitPane splitPane;
    @FXML
    private AnchorPane progressPane;
    @FXML
    private AnchorPane outputPane;

    public SplitScreenController() {
    }

    private void setRootPane(
        AnchorPane pane
    ) {
        this.splitScreen = pane;
    }

    public static SplitScreenController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(SplitScreenController.class.getResource("SplitScreen.fxml"));
            AnchorPane splitScreen = (AnchorPane) loader.load();
            SplitScreenController splitScreenController = loader.<SplitScreenController>getController();
            splitScreenController.setRootPane(splitScreen);
            return splitScreenController;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param clientDispatcher
     * @param commander
     * @return
     */
    public AnchorPane initSplitScreen(
        ClientConfiguration clientDispatcher,
        ClientCommander commander
    ) {
        this.clientConfiguration = clientDispatcher;
        this.clientCommander = commander;
        testForReady();

        return splitScreen;
    }

    private void initNextController() {
        progressOverview = ProgressOverviewController.getInstance();
        outputOverview = OutputOverviewController.getInstance();
    }

    private void fillWithContent() {
        //fill leftside
        progressPane = progressOverview.loadProgressOverview(clientConfiguration, clientCommander);
        splitPane.getItems().set(0, progressPane);

        //fill rightside
        outputPane = outputOverview.loadOutputOverview(clientConfiguration, clientCommander);
        splitPane.getItems().set(1, outputPane);

        //set divider to correct the position
        splitPane.setDividerPosition(0, 0.4);
    }

    public void initialize() {
        isInitialized = true;
        testForReady();
    }

    private void testForReady() {
        if (clientConfiguration != null
            && clientCommander != null
            && isInitialized == true) {
            initNextController();
            fillWithContent();
        }
    }

    /**
     *
     */
    public void resetProgress() {
        progressOverview.resetProgress();
    }

    /**
     *
     * @param segmentationResults
     */
    public void updateSegmentationResults(
        SegmentationWorker segmentationResults
    ) {
        outputOverview.updateSegmentationResults(segmentationResults);
        progressOverview.updateSegmentationResults();
    }

    /**
     *
     * @param numberOfDroppedPeaks
     */
    public void updateSegmentationDroppedPeaks(
        int numberOfDroppedPeaks
    ) {
        outputOverview.updateSegmentationDroppedPeaks(numberOfDroppedPeaks);
    }

    /**
     *
     * @param segmentationCodeCounts
     */
    public void updateCodeDistribution(
        SortedMap<Integer, Integer> segmentationCodeCounts
    ) {
        outputOverview.updateCodeDistribution(segmentationCodeCounts);
    }

    /**
     *
     * @param additionalValues
     */
    public void updateAdditionalDataTabs(
        Map<String, List<Double>> additionalValues
    ) {
        outputOverview.updateAdditionalMeasurementsTabs(additionalValues);
        progressOverview.updateAdditionalDataTabs();
    }

    /*
     *
     * @param motifValues
     */
    public void updateMotifDataTabs(
        Map<String, List<Double>> motifValues
    ) {
        outputOverview.updateMotifDataTabs(motifValues);
        progressOverview.updateMotifsTabs();
    }

    /*
     *
     * @param motifValues
     */
    public void updatePWMDataTabs(
        Map<String, List<Double>> pwmValues
    ) {
        outputOverview.updatePWMDataTabs(pwmValues);
        progressOverview.updatePWMTabs();
    }

    public void setCorrelationData(double[][] data) {
        outputOverview.setCorrelationData(data);
    }

    public void setFateOfCodeData(List<int[][]> data) {
        outputOverview.setFateOfCodeResults(data);
    }

    public void exportToPng(String filename) {
        outputOverview.exportToPng(filename);
    }

    public void setBreakSegmentData(List<BreakSegment> breakSegmentData) {
        outputOverview.setBreakSegmentData(breakSegmentData);
    }

    public void setSegmentPairsData(Map<String, Integer> segmentPairsData) {
        outputOverview.setSegmentPairsData(segmentPairsData);
    }

    public void setSegmentShortSegmentPairsData(Map<String, Integer> segmentShortSegmentPairsData) {
        outputOverview.setSegmentShortSegmentPairsData(segmentShortSegmentPairsData);
    }

    public void setShortSegmentSegmentPairsData(Map<String, Integer> shortSegementSegmentPairsData) {
        outputOverview.setShortSegmentSegmentPairsData(shortSegementSegmentPairsData);
    }

    public void setSegmentPairOccurrenceData(Map<String, Double> segmentPairOccurrenceData) {
        outputOverview.setSegmentPairOccurrenceData(segmentPairOccurrenceData);
    }

    public void setShortSegmentChainsCounts(SortedMap<Integer, Integer> shortSegmentChainsCounts) {
        outputOverview.setShortSegmentChainsCounts(shortSegmentChainsCounts);
    }

    public void setShortSegmentChainsLengths(SortedMap<Integer, Integer> shortSegmentChainsLengths) {
        outputOverview.setShortSegmentChainsLengths(shortSegmentChainsLengths);
    }

    /**
     *
     */
    public void indexWorkerSuccess() {
        progressOverview.indexWorkerSuccess();
    }
}
