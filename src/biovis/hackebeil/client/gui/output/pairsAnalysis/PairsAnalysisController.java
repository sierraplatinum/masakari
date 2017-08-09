/**
 *  Copyright 2016 Dirk Zeckzer
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
package biovis.hackebeil.client.gui.output.pairsAnalysis;

import java.io.IOException;

import biovis.hackebeil.client.data.ClientConfiguration;
import java.util.Map;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * @author Dirk Zeckzer
 *
 */
public class PairsAnalysisController {

    private ClientConfiguration clientConfiguration;
    private Boolean isInitialized = false;

    private Tab rootTab;

    // Elements of the primary information tab
    @FXML
    private Tab tabPairsAnalysisRoot;
    @FXML
    private TabPane segmentPairsTab;

    // Tabs
    private SegmentPairController segmentPairController;
    private SegmentPairController segmentShortSegmentPairController;
    private SegmentPairController shortSegmentSegmentPairController;
    private SegmentPairOccurrenceController segmentPairOccurrenceController;

    /**
     */
    public PairsAnalysisController() {
    }

    /**
     *
     * @param rootPane
     */
    private void setTab(
        Tab rootPane
    ) {
        this.rootTab = rootPane;
    }

    /**
     *
     * @return
     */
    public static PairsAnalysisController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(PairsAnalysisController.class.getResource("PairsAnalysis.fxml"));
            Tab rootTab = loader.load();
            PairsAnalysisController pairsAnalysisController = loader.<PairsAnalysisController>getController();
            pairsAnalysisController.setTab(rootTab);
            return pairsAnalysisController;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Initialization, called by FXMLLoader.
     */
    public void initialize() {
        isInitialized = true;
        testForReady();
    }

    /**
     * Check if all pre-conditions for using this tab are fulfilled.
     */
    private void testForReady() {
        if (clientConfiguration != null
            && isInitialized == true) {
            this.init();
        }
    }

    /**
     * Complete initialization.
     */
    private void init() {
    }

    /**
     * Initialize the different sub-tabs of this tab.
     */
    public void initView() {
        if (segmentPairController == null) {
            segmentPairController = SegmentPairController.getInstance();
            TabPane tabPane = segmentPairController.loadView(clientConfiguration);
            if (tabPane != null) {
                Platform.runLater(() -> {
                    Tab tab = new Tab("Segment Pairs", tabPane);
                    segmentPairsTab.getTabs().add(tab);
                });
            }
        }

        if (segmentShortSegmentPairController == null) {
            segmentShortSegmentPairController = SegmentPairController.getInstance();
            TabPane tabPane = segmentShortSegmentPairController.loadView(clientConfiguration);
            if (tabPane != null) {
                Platform.runLater(() -> {
                    Tab tab = new Tab("Segment - Short Segment Pairs", tabPane);
                    segmentPairsTab.getTabs().add(tab);
                });
            }
        }

        if (shortSegmentSegmentPairController == null) {
            shortSegmentSegmentPairController = SegmentPairController.getInstance();
            TabPane tabPane = shortSegmentSegmentPairController.loadView(clientConfiguration);
            if (tabPane != null) {
                Platform.runLater(() -> {
                    Tab tab = new Tab("Short Segment - Segment Pairs", tabPane);
                    segmentPairsTab.getTabs().add(tab);
                });
            }
        }

        if (segmentPairOccurrenceController == null) {
            segmentPairOccurrenceController = SegmentPairOccurrenceController.getInstance();
            TabPane tabPane = segmentPairOccurrenceController.loadView(clientConfiguration);
            if (tabPane != null) {
                Platform.runLater(() -> {
                    Tab tab = new Tab("Segment Pair Occurrence", tabPane);
                    segmentPairsTab.getTabs().add(tab);
                });
            }
        }
    }

    /**
     * Update statistical information about segments.
     * @param segmentPairs
     */
    public void updateSegmentPairsData(
        Map<String, Integer> segmentPairs
    ) {
        rootTab.setDisable(false);
        if (segmentPairController == null) {
            initView();
        }
        segmentPairController.setSegmentPairData(segmentPairs);
    }

    /**
     * Update statistical information about segments.
     * @param segmentShortSegmentPairs
     */
    public void updateSegmentShortSegmentPairsData(
        Map<String, Integer> segmentShortSegmentPairs
    ) {
        rootTab.setDisable(false);
        if (segmentShortSegmentPairController == null) {
            initView();
        }
        segmentShortSegmentPairController.setSegmentPairData(segmentShortSegmentPairs);
    }

    /**
     * Update statistical information about segments.
     * @param shortSegmentSegmentPairs
     */
    public void updateShortSegmentSegmentPairsData(
        Map<String, Integer> shortSegmentSegmentPairs
    ) {
        rootTab.setDisable(false);
        if (shortSegmentSegmentPairController == null) {
            initView();
        }
        shortSegmentSegmentPairController.setSegmentPairData(shortSegmentSegmentPairs);
    }

    /**
     * Update statistical information about segments.
     * @param segmentPairOccurrence
     */
    public void updateSegmentPairOccurrenceData(
        Map<String, Double> segmentPairOccurrence
    ) {
        rootTab.setDisable(false);
        if (segmentPairOccurrenceController == null) {
            initView();
        }
        segmentPairOccurrenceController.setSegmentPairOccurrenceData(segmentPairOccurrence);
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

    /**
     *
     * @param string
     */
    public void exportToPng(String string) {
        segmentPairController.exportToPng(string + "-segmentPair");
        segmentShortSegmentPairController.exportToPng(string + "-segmentShortSegmentPair");
        shortSegmentSegmentPairController.exportToPng(string + "-shortSegmentSegmentPair");
        segmentPairOccurrenceController.exportToPng(string + "-segmentPair");
    }
}
