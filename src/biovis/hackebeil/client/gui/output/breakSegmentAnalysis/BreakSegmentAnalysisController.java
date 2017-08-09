/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biovis.hackebeil.client.gui.output.breakSegmentAnalysis;

import biovis.hackebeil.client.data.ClientConfiguration;
import biovis.hackebeil.client.gui.output.common.CodeDistanceController;
import biovis.hackebeil.client.gui.output.common.CountHeatmapController;
import biovis.hackebeil.common.data.BreakSegment;
import biovislib.utils.HammingDistance;
import java.io.IOException;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author zeckzer
 */
public class BreakSegmentAnalysisController {

    private ClientConfiguration clientConfiguration;
    private Boolean isInitialized = false;

    private Tab rootTab;

    @FXML
    private Tab tabBreakSegmentAnalysisRoot;
    @FXML
    private TabPane tabs;
    @FXML
    private Tab heatmapCodeTab;
    @FXML
    private Tab heatmapDistanceTab;
    @FXML
    private Tab distanceTab;
    @FXML
    private Tab lengthTab;

    private CountHeatmapController breakSegmentHeatmapCodeController;
    private CountHeatmapController breakSegmentHeatmapDistanceController;
    private CodeDistanceController breakSegmentDistanceController;
    private BreakSegmentLengthAnalysisController breakSegmentLengthAnalysisController;

    private int numberOfReferences;
    private int numberOfCodes;

    private int[][] codeCountMatrix;
    private int[][] distanceCountMatrix;
    private int codeCountMax;
    private int distanceCountMax;
    private int codeNonNull;
    private int distanceNonNull;

    public BreakSegmentAnalysisController() {
    }

    private void setRootTab(
        Tab pane
    ) {
        this.rootTab = pane;
    }

    public static BreakSegmentAnalysisController getInstance() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(BreakSegmentAnalysisController.class.getResource("BreakSegmentAnalysis.fxml"));
            Tab rootTab = loader.load();
            BreakSegmentAnalysisController breakSegmentController = loader.<BreakSegmentAnalysisController>getController();
            breakSegmentController.setRootTab(rootTab);
            return breakSegmentController;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Tab loadView(
        ClientConfiguration clientConfiguration
    ) {
        this.clientConfiguration = clientConfiguration;
        testForReady();

        return rootTab;
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
        breakSegmentHeatmapCodeController = CountHeatmapController.getInstance();
        AnchorPane anchorPane = breakSegmentHeatmapCodeController.loadView();
        heatmapCodeTab.setContent(anchorPane);

        breakSegmentHeatmapDistanceController = CountHeatmapController.getInstance();
        anchorPane = breakSegmentHeatmapDistanceController.loadView();
        heatmapDistanceTab.setContent(anchorPane);

        breakSegmentDistanceController = CodeDistanceController.getInstance();
        AnchorPane anchorPaneDistance = breakSegmentDistanceController.loadView();
        distanceTab.setContent(anchorPaneDistance);

        breakSegmentLengthAnalysisController = BreakSegmentLengthAnalysisController.getInstance();
        AnchorPane anchorPaneLengthAnalysis = breakSegmentLengthAnalysisController.loadView();
        lengthTab.setContent(anchorPaneLengthAnalysis);
    }

    /**
     *
     * @param breakSegmentData
     */
    public void setBreakSegmentData(
        List<BreakSegment> breakSegmentData
    ) {
        if (breakSegmentData == null
            || breakSegmentData.isEmpty()) {
            rootTab.setDisable(true);
            return;
        }

        rootTab.setDisable(false);

        calcMatrix(breakSegmentData);
        String[] columnNames = new String[codeCountMatrix[0].length];
        String[] rowNames = new String[codeCountMatrix.length];
        for (int row = 0;
             row < codeCountMatrix[0].length;
             ++row) {
            columnNames[row] = "Break\ncode:\n" + row;
        }
        for (int column = 0;
             column < codeCountMatrix.length;
             ++column) {
            rowNames[column] = "Wrapping\ncode:\n" + column;
        }

        breakSegmentHeatmapCodeController.setHeatmapData(
            rowNames,
            columnNames,
            codeCountMatrix,
            codeNonNull,
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
            rowNamesDistance[column] = "Wrapping\ncode:\n" + column;
        }

        breakSegmentHeatmapDistanceController.setHeatmapData(
            rowNamesDistance,
            columnNamesDistance,
            distanceCountMatrix,
            distanceNonNull,
            distanceCountMax
        );

        breakSegmentDistanceController.setData(
            numberOfCodes,
            distanceCountMatrix
        );

        breakSegmentLengthAnalysisController.setBreakSegmentData(
            numberOfCodes,
            breakSegmentData,
            clientConfiguration.getMinSegmentLength() - 1
        );

        redraw();
    }

    private void calcMatrix(
        List<BreakSegment> breakSegmentData
    ) {
        codeCountMax = 0;
        distanceCountMax = 0;

        codeNonNull = 0;
        distanceNonNull = 0;

        numberOfReferences = clientConfiguration.getReferenceDataList().size();
        numberOfCodes = (int) Math.pow(2, numberOfReferences);
        codeCountMatrix = new int[numberOfCodes][numberOfCodes];
        distanceCountMatrix = new int[numberOfCodes][numberOfReferences + 1];

        int beforeCode;
        int breakCode;
        for (BreakSegment dp : breakSegmentData) {
            beforeCode = dp.getBeforeCode();
            breakCode = dp.getBreakCode();

            // count
            codeCountMatrix[beforeCode][breakCode]++;

            // hamming distance counts
            distanceCountMatrix[beforeCode][HammingDistance.getDistance(beforeCode, breakCode)]++;
        }

        for (beforeCode = 0;
             beforeCode < codeCountMatrix.length;
             ++beforeCode) {
            for (breakCode = 0;
                 breakCode < codeCountMatrix[beforeCode].length;
                 ++breakCode) {
                if (codeCountMatrix[beforeCode][breakCode] > codeCountMax) {
                    codeCountMax = codeCountMatrix[beforeCode][breakCode];
                }
                if (codeCountMatrix[beforeCode][breakCode] > 0) {
                    ++codeNonNull;
                }
            }
        }

        for (beforeCode = 0;
             beforeCode < distanceCountMatrix.length;
             ++beforeCode) {
            for (int distance = 0;
                 distance < distanceCountMatrix[beforeCode].length;
                 ++distance) {
                if (distanceCountMatrix[beforeCode][distance] > distanceCountMax) {
                    distanceCountMax = distanceCountMatrix[beforeCode][distance];
                }
                if (distanceCountMatrix[beforeCode][distance] > 0) {
                    ++distanceNonNull;
                }
            }
        }
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
            breakSegmentHeatmapCodeController.redraw();
            breakSegmentHeatmapDistanceController.redraw();
            breakSegmentDistanceController.redraw();
            breakSegmentLengthAnalysisController.redraw();
        });
    }

    /**
     *
     * @param path
     */
    public void exportToPng(String path) {
        path += "-breakSegment";
        breakSegmentHeatmapCodeController.exportToPng(path + "-HeatmapCode");
        breakSegmentHeatmapDistanceController.exportToPng(path + "-HeatmapDistance");
        breakSegmentDistanceController.exportToPng(path + "-Distance");
        breakSegmentLengthAnalysisController.exportToPng(path + "-Length");
    }
}
