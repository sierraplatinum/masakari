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

import biovis.hackebeil.client.gui.output.common.CodeDistanceController;
import biovis.hackebeil.client.gui.output.common.CountHeatmapController;
import java.io.IOException;
import java.util.logging.Logger;

import biovis.hackebeil.client.data.ClientConfiguration;
import biovislib.utils.HammingDistance;
import java.util.Map;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

/**
 * @author Dirk Zeckzer
 *
 */
public class SegmentPairController {

    private static final Logger log = Logger.getLogger(SegmentPairController.class.getName());

    private ClientConfiguration clientConfiguration;
    private Boolean isInitialized = false;

    private TabPane rootPane;

    @FXML
    private TabPane tabs;
    @FXML
    private Tab heatmapCodeTab;
    @FXML
    private Tab heatmapDistanceTab;
    @FXML
    private Tab distanceTab;

    private CodeDistanceController segmentPairDistanceController;
    private CountHeatmapController segmentPairHeatmapCodeController;
    private CountHeatmapController segmentPairHeatmapDistanceController;

    private int numberOfCodes;

    private int[][] codeCountMatrix;
    private int[][] distanceCountMatrix;
    private int codeCountMax;
    private int distanceCountMax;

    public SegmentPairController() {
    }

    private void setRootPane(
        TabPane pane
    ) {
        this.rootPane = pane;
    }

    public static SegmentPairController getInstance() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(SegmentPairController.class.getResource("SegmentPair.fxml"));
            TabPane rootPane = loader.load();
            SegmentPairController segmentPairController = loader.<SegmentPairController>getController();
            segmentPairController.setRootPane(rootPane);
            return segmentPairController;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public TabPane loadView(
        ClientConfiguration clientConfiguration
    ) {
        this.clientConfiguration = clientConfiguration;
        testForReady();

        return rootPane;
    }

    /**
     * Initialization, called by FXMLLoader.
     */
    public void initialize() {
        isInitialized = true;
        testForReady();
    }

    private void testForReady() {
        if (clientConfiguration != null
            && isInitialized == true) {
            init();
        }
    }

    private void init() {
        segmentPairHeatmapCodeController = CountHeatmapController.getInstance();
        AnchorPane anchorPane = segmentPairHeatmapCodeController.loadView();
        heatmapCodeTab.setContent(anchorPane);

        segmentPairHeatmapDistanceController = CountHeatmapController.getInstance();
        anchorPane = segmentPairHeatmapDistanceController.loadView();
        heatmapDistanceTab.setContent(anchorPane);

        segmentPairDistanceController = CodeDistanceController.getInstance();
        AnchorPane anchorPaneDistance = segmentPairDistanceController.loadView();
        distanceTab.setContent(anchorPaneDistance);
    }

    /**
     *
     */
    private void redraw() {
        if (codeCountMatrix == null
            || distanceCountMatrix == null) {
            return;
        }

        Platform.runLater(() -> {
            segmentPairHeatmapCodeController.redraw();
            segmentPairHeatmapDistanceController.redraw();
            segmentPairDistanceController.redraw();
        });
    }

    private void calcMatrix(
        Map<String, Integer> segmentPairsData
    ) {
        if (segmentPairsData == null) {
            return;
        }

        int hammingDistanceCount;
        codeCountMax = 0;
        distanceCountMax = 0;

        int numberOfReferences = clientConfiguration.getReferenceDataList().size();
        numberOfCodes = (int) Math.pow(2, numberOfReferences);
        codeCountMatrix = new int[numberOfCodes][numberOfCodes];
        distanceCountMatrix = new int[numberOfCodes][numberOfReferences + 1];
        for (int i = 0;
             i < codeCountMatrix.length;
             ++i) {
            for (int j = 0;
                 j < codeCountMatrix[i].length;
                 ++j) {
                String key = i + "-" + j;
                Integer value = segmentPairsData.get(key);
                if (value != null) {
                    codeCountMatrix[i][j] = value;
                    if (value > codeCountMax) {
                        codeCountMax = value;
                    }

                    // hamming distance counts
                    distanceCountMatrix[i][HammingDistance.getDistance(i, j)] += value;
                    hammingDistanceCount = distanceCountMatrix[i][HammingDistance.getDistance(i, j)];
                    if (hammingDistanceCount > distanceCountMax) {
                        distanceCountMax = hammingDistanceCount;
                    }
                } else {
                    codeCountMatrix[i][j] = 0;
                }
            }
        }
    }

    /**
     *
     * @param segmentPairsData
     */
    public void setSegmentPairData(Map<String, Integer> segmentPairsData) {
        if (segmentPairsData == null
            || segmentPairsData.isEmpty()) {
            return;
        }

        calcMatrix(segmentPairsData);

        String[] columnNames = new String[codeCountMatrix[0].length];
        String[] rowNames = new String[codeCountMatrix.length];
        for (int row = 0;
             row < codeCountMatrix[0].length;
             ++row) {
            columnNames[row] = "Second\ncode:\n" + row;
        }
        for (int column = 0;
             column < codeCountMatrix.length;
             ++column) {
            rowNames[column] = "First\ncode:\n" + column;
        }

        segmentPairHeatmapCodeController.setHeatmapData(
            rowNames,
            columnNames,
            codeCountMatrix,
            segmentPairsData.size(),
            codeCountMax
        );

        String[] columnNamesDistance = new String[distanceCountMatrix[0].length];
        String[] rowNamesDistance = new String[distanceCountMatrix.length];
        for (int row = 0;
             row < distanceCountMatrix[0].length;
             ++row) {
            columnNamesDistance[row] = "Distance:\n" + row;
        }
        for (int column = 0;
             column < distanceCountMatrix.length;
             ++column) {
            rowNamesDistance[column] = "First\ncode:\n" + column;
        }
        segmentPairHeatmapDistanceController.setHeatmapData(
            rowNamesDistance,
            columnNamesDistance,
            distanceCountMatrix,
            segmentPairsData.size(),
            distanceCountMax
        );

        segmentPairDistanceController.setData(
            numberOfCodes,
            distanceCountMatrix
        );

        redraw();
    }

    /**
     *
     * @param path
     */
    public void exportToPng(String path) {
        segmentPairHeatmapCodeController.exportToPng(path + "-Code");
        segmentPairHeatmapDistanceController.exportToPng(path + "-Distance");
        segmentPairDistanceController.exportToPng(path);
    }
}
