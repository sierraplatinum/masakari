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
package biovis.hackebeil.client.gui.output.lengthAnalysis;

import java.io.IOException;
import java.util.logging.Logger;

import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.server.worker.segmentation.SegmentationWorker;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.SortedMap;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

/**
 * @author Alrik Hausdorf, Dirk Zeckzer
 *
 */
public class LengthAnalysisRootController {

    private static final Logger log = Logger.getLogger(LengthAnalysisRootController.class.getName());

    private Tab rootTab;
    private ClientConfiguration clientConfiguration;
    private Boolean isInitialized = false;
    private LengthAnalysisSegmentationController lengthAnalysisSegmentationController;
    private List<LengthAnalysisBedFileController> bedFileControllerList = new ArrayList<>();

    private boolean isUpdatingTabs = false;

    @FXML
    private Tab tabLengthAnalysisRoot;
    @FXML
    private TabPane tabs;

    public LengthAnalysisRootController() {
    }

    /**
     *
     * @param pane 
     */
    private void setRootTab(
        Tab pane
    ) {
        this.rootTab = pane;
    }

    /**
     *
     * @return
     */
    public static LengthAnalysisRootController getInstance() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(LengthAnalysisRootController.class.getResource("LengthAnalysisRoot.fxml"));
            Tab rootTab = loader.load();
            LengthAnalysisRootController lengthAnalysisRootController = loader.<LengthAnalysisRootController>getController();
            lengthAnalysisRootController.setRootTab(rootTab);
            return lengthAnalysisRootController;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param clientConfiguration
     * @return
     */
    public Tab loadView(
        ClientConfiguration clientConfiguration
    ) {
        this.clientConfiguration = clientConfiguration;
        testForReady();

        return rootTab;
    }

    public void initialize() {
        isInitialized = true;
        testForReady();
    }

    public void invalidateTabs() {
        // System.out.println("invalidateTabs");
        Platform.runLater(() -> {
            tabs.getTabs().clear();
        });
    }

    /**
     *
     * @param modifiedSegments 
     * @param unmodifiedSegments 
     */
    private void createTabs(
        SortedMap<Integer, Integer> modifiedSegments,
        SortedMap<Integer, Integer> unmodifiedSegments
    ) {
        if (!clientConfiguration.getReferenceDataList().isEmpty()) {
            if (isUpdatingTabs) {
                return;
            }
            isUpdatingTabs = true;

            ObservableList<DataFile> referenceDataList = clientConfiguration.getReferenceDataList();
            bedFileControllerList.clear();
            try {
                for (DataFile referenceDataFile : referenceDataList) {
                    this.createTabForReferenceDataFile(referenceDataFile, modifiedSegments);
                }
            } catch (ConcurrentModificationException e) {
                e.printStackTrace();
            }
            this.createTabForSegmentLengths(unmodifiedSegments, modifiedSegments);

            isUpdatingTabs = false;
        }
    }

    private void createTabForReferenceDataFile(
        DataFile referenceDataFile,
        SortedMap<Integer, Integer> modifiedSegments
    ) {
        LengthAnalysisBedFileController lengthAnalysisBedFileController = LengthAnalysisBedFileController.getInstance();
        AnchorPane anchorPane = lengthAnalysisBedFileController.loadView(clientConfiguration, this, referenceDataFile, modifiedSegments);

        Tab newTab = new Tab();
        newTab.setText(referenceDataFile.getDataSetName());
        newTab.setContent(anchorPane);
        newTab.setClosable(false);
        tabs.getTabs().add(newTab);
        bedFileControllerList.add(lengthAnalysisBedFileController);
        lengthAnalysisBedFileController.initView();
    }

    private void createTabForSegmentLengths(
        SortedMap<Integer, Integer> unmodifiedSegments,
        SortedMap<Integer, Integer> modifiedSegments
    ) {
        lengthAnalysisSegmentationController = LengthAnalysisSegmentationController.getInstance();
        AnchorPane anchorPane = lengthAnalysisSegmentationController.loadView(clientConfiguration, this, unmodifiedSegments, modifiedSegments);

        Tab newTab = new Tab();
        newTab.setText("Segmentation");
        newTab.setContent(anchorPane);
        newTab.setClosable(false);
        tabs.getTabs().add(newTab);
        lengthAnalysisSegmentationController.initView();
        lengthAnalysisSegmentationController.redraw();
    }

    private void init() {
    }

    private void testForReady() {
        if (clientConfiguration != null
            && isInitialized == true) {
            this.init();
        }
    }

    /**
     * Update the information of the segment length distribution tab.
     * @param segmentationResults
     */
    public void update(
        SegmentationWorker segmentationResults
    ) {
        if (segmentationResults == null) {
            rootTab.setDisable(true);
            return;
        }

        rootTab.setDisable(false);

        Platform.runLater(() -> {
            if (lengthAnalysisSegmentationController == null) {
                createTabs(segmentationResults.getSegmentModifiedLengths(),
                           segmentationResults.getSegmentUnmodifiedLengths());
            }
        });
    }

    /*
     *
     * @param string
     */
    public void exportToPng(String string) {
        string += "-lengthAnalysis";
        for (LengthAnalysisBedFileController bedFileController : bedFileControllerList) {
            bedFileController.saveChartToFile(string);
        }

        if (this.lengthAnalysisSegmentationController != null) {
            this.lengthAnalysisSegmentationController.saveChartToFile(string + "_Segmentation");
        }
    }
}
