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
package biovis.hackebeil.client.gui.output.overview;

import biovis.hackebeil.server.worker.segmentation.SegmentationWorker;
import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;

/**
 * @author Alrik Hausdorf<alrik@gurkware.de>, nhinzmann, Dirk Zeckzer
 *
 */
public class OverviewController {

    private Boolean isInitialized = false;

    private Tab rootTab;

    // Elements of the primary information tab
    @FXML
    private Label numberOfSegments;
    @FXML
    private Label numberOfShortSegments;
    @FXML
    private Label totalSegmentLength;
    @FXML
    private Label totalShortSegmentLength;
    @FXML
    private Label numberOfAdditionalMeasurementsColumns;
    @FXML
    private Label numberOfMotifDataColumns;
    @FXML
    private Label numberOfPWMDataColumns;
    @FXML
    private Label numberOfDroppedPeaks;

    /**
     */
    public OverviewController() {
    }

    private void setRootTab(
        Tab rootTab
    ) {
        this.rootTab = rootTab;
    }

    public static OverviewController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(OverviewController.class.getResource("Overview.fxml"));
            Tab rootTab = loader.load();
            OverviewController controller = loader.<OverviewController>getController();
            controller.setRootTab(rootTab);
            return controller;
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
        if (isInitialized == true) {
            this.init();
        }
    }

    /**
     * Complete initialization.
     */
    private void init() {
        // Nothing to do here!
    }

    /**
     * Initialize the different sub-tabs of this tab.
     */
    public Tab loadView() {
        numberOfSegments.setText(Long.toString(0));
        numberOfShortSegments.setText(Long.toString(0));
        totalSegmentLength.setText(Long.toString(0));
        totalShortSegmentLength.setText(Long.toString(0));

        numberOfAdditionalMeasurementsColumns.setText(Long.toString(0));
        numberOfMotifDataColumns.setText(Long.toString(0));
        numberOfPWMDataColumns.setText(Long.toString(0));

        numberOfDroppedPeaks.setText(Long.toString(0));

        return rootTab;
    }

    /**
     * Update statistical information about segments.
     * @param segmentationResults
     */
    public void setSegmentInformation(
        SegmentationWorker segmentationResults
    ) {
        Platform.runLater(() -> {
            numberOfSegments.setText(Long.toString(segmentationResults.getSegmentCount()));
            totalSegmentLength.setText(Long.toString(segmentationResults.getTotalSegmentLength()));
            numberOfShortSegments.setText(Long.toString(segmentationResults.getShortSegmentCount()));
            totalShortSegmentLength.setText(Long.toString(segmentationResults.getTotalShortSegmentLength()));
        });
    }

    /**
     *
     * @param numberOfDroppedPeaks
     */
    public void setSegmentationDroppedPeaks(
        int numberOfDroppedPeaks
    ) {
        Platform.runLater(() -> {
            this.numberOfDroppedPeaks.setText(Integer.toString(numberOfDroppedPeaks));
        });
    }

    /**
     * Update statistical information about segments.
     * @param numberOfAdditionalMeasurements
     */
    public void setAdditionalMeasurementsInformation(
        long numberOfAdditionalMeasurements
    ) {
        Platform.runLater(() -> {
            numberOfAdditionalMeasurementsColumns.setText(Long.toString(numberOfAdditionalMeasurements));
        });
    }

    /**
     * Update statistical information about segments.
     * @param numberOfMotifs
     */
    public void setMotifInformation(
        long numberOfMotifs
    ) {
        Platform.runLater(() -> {
            numberOfMotifDataColumns.setText(Long.toString(numberOfMotifs));
        });
    }

    /**
     * Update statistical information about segments.
     * @param numberOfPWM
     */
    public void setPWMInformation(
        long numberOfPWM
    ) {
        Platform.runLater(() -> {
            numberOfPWMDataColumns.setText(Long.toString(numberOfPWM));
        });
    }
}
