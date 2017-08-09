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
package biovis.hackebeil.client.gui.output.segmentation;

import java.io.IOException;

import biovis.hackebeil.client.data.ClientConfiguration;
import java.util.SortedMap;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * @author Alrik Hausdorf<alrik@gurkware.de>, nhinzmann, Dirk Zeckzer
 *
 */
public class SegmentationTabPaneController {

    private Tab rootTab;
    private ClientConfiguration clientConfiguration;
    private Boolean isInitialized = false;

    // Elements of the primary information tab
    @FXML
    private TabPane tabs;

    // Tabs
    private CodeDistributionTab tabCodeDistribution = null;
    private LengthDistributionTab tabSegmentLengthDistribution = null;
    private LengthDistributionTab tabShortSegmentLengthDistribution = null;
    private CodeDistributionTab tabShortSegmentChainsCountsDistribution = null;
    private LengthDistributionTab tabShortSegmentChainsLengthsDistribution = null;

    /**
     */
    public SegmentationTabPaneController() {
    }

    /**
     *
     * @param rootTab
     */
    public void setRootTab(Tab rootTab) {
        this.rootTab = rootTab;
    }

    /**
     *
     * @return
     */
    public static SegmentationTabPaneController getInstance() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SegmentationTabPaneController.class.getResource("SegmentationTabPane.fxml"));
            Tab rootTab = loader.load();
            SegmentationTabPaneController statisticalInformationController = loader.<SegmentationTabPaneController>getController();
            statisticalInformationController.setRootTab(rootTab);
            return statisticalInformationController;
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
        if (clientConfiguration != null && isInitialized == true) {
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
        if (tabCodeDistribution == null) {
            tabCodeDistribution = new CodeDistributionTab(
                "Code"
            );
            if (tabCodeDistribution != null) {
                Platform.runLater(() -> {
                    tabs.getTabs().add(tabCodeDistribution);
                });
            }
        }

        if (tabSegmentLengthDistribution == null) {
            tabSegmentLengthDistribution = new LengthDistributionTab(
                "Segment Length"
            );
            if (tabSegmentLengthDistribution != null) {
                Platform.runLater(() -> {
                    tabs.getTabs().add(tabSegmentLengthDistribution);
                });
            }
        }

        if (tabShortSegmentLengthDistribution == null) {
            tabShortSegmentLengthDistribution = new LengthDistributionTab(
                "Short Segment Length"
            );
            if (tabShortSegmentLengthDistribution != null) {
                Platform.runLater(() -> {
                    tabs.getTabs().add(tabShortSegmentLengthDistribution);
                });
            }
        }

        if (tabShortSegmentChainsCountsDistribution == null) {
            tabShortSegmentChainsCountsDistribution = new CodeDistributionTab(
                "Short Segment Chains Counts"
            );
            if (tabShortSegmentChainsCountsDistribution != null) {
                Platform.runLater(() -> {
                    tabs.getTabs().add(tabShortSegmentChainsCountsDistribution);
                });
            }
        }

        if (tabShortSegmentChainsLengthsDistribution == null) {
            tabShortSegmentChainsLengthsDistribution = new LengthDistributionTab(
                "Short Segment Chains Lengths"
            );
            if (tabShortSegmentChainsLengthsDistribution != null) {
                Platform.runLater(() -> {
                    tabs.getTabs().add(tabShortSegmentChainsLengthsDistribution);
                });
            }
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

    /**
     * Update the information on the code distribution tab.
     * @param codeDistribution
     */
    public void updateCodeDistribution(
        SortedMap<Integer, Integer> codeDistribution
    ) {
        if (tabCodeDistribution == null) {
            return;
        }

        rootTab.setDisable(false);
        tabCodeDistribution.updateCodeDistribution(codeDistribution);
    }

    /**
     * Update the information of the segment length distribution tab.
     * @param lengthDistribution
     */
    public void updateSegmentLengthDistribution(
        SortedMap<Integer, Integer> lengthDistribution
    ) {
        if (tabSegmentLengthDistribution == null) {
            return;
        }

        rootTab.setDisable(false);
        tabSegmentLengthDistribution.updateLengthDistribution(lengthDistribution);
    }

    /**
     * Update the information of the short segment length distribution tab.
     * @param lengthDistribution
     */
    public void updateShortSegmentLengthDistribution(
        SortedMap<Integer, Integer> lengthDistribution
    ) {
        if (tabShortSegmentLengthDistribution == null) {
            return;
        }

        rootTab.setDisable(false);
        tabShortSegmentLengthDistribution.updateLengthDistribution(lengthDistribution);
    }

    public void updateShortSegmentChainsCounts(SortedMap<Integer, Integer> shortSegmentChainsCounts) {
        if (tabShortSegmentChainsCountsDistribution == null) {
            return;
        }

        rootTab.setDisable(false);
        tabShortSegmentChainsCountsDistribution.updateCodeDistribution(shortSegmentChainsCounts);
    }

    public void updateShortSegmentChainsLengths(SortedMap<Integer, Integer> shortSegmentChainsLengths) {
        if (tabShortSegmentChainsLengthsDistribution == null) {
            return;
        }

        rootTab.setDisable(false);
        tabShortSegmentChainsLengthsDistribution.updateLengthDistribution(shortSegmentChainsLengths);
    }

    /**
     *
     * @param baseName
     */
    public void exportToPng(String baseName) {
        final String segmentationBaseName = baseName + "-Segmentation";

        tabCodeDistribution.saveChartToFile(segmentationBaseName);
        tabSegmentLengthDistribution.saveChartToFile(segmentationBaseName);
        tabShortSegmentLengthDistribution.saveChartToFile(segmentationBaseName);
        tabShortSegmentChainsCountsDistribution.saveChartToFile(segmentationBaseName);
        tabShortSegmentChainsLengthsDistribution.saveChartToFile(segmentationBaseName);
    }
}
