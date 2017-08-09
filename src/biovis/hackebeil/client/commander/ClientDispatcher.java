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
package biovis.hackebeil.client.commander;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import biovis.hackebeil.client.gui.dialog.ConnectionManagerController;
import biovis.hackebeil.client.gui.RootLayoutController;
import biovis.hackebeil.client.gui.StatusBarController;
import biovis.hackebeil.common.data.BreakSegment;
import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.common.data.Messages;
import biovis.hackebeil.server.worker.segmentation.SegmentationWorker;
import biovislib.remoteControl.CommandDispatcherInterface;
import java.util.Map;
import java.util.SortedMap;
import javafx.application.Platform;

/**
 * @author nhinzmann
 *
 */
public class ClientDispatcher implements CommandDispatcherInterface {

    private Logger log = Logger.getLogger(ClientDispatcher.class.toString());

    private RootLayoutController rootLayoutController;
    private StatusBarController statusBarController;

    /**
     *
     * @param rootController
     */
    public ClientDispatcher(RootLayoutController rootController) {
        this.rootLayoutController = rootController;
        this.statusBarController = rootController.getStatusBarController();
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.kl.vis.lib.remoteControl.CommandDispatcherInterface#dispatchCommand(
	 * java.lang.Object[])
     */
    @Override
    public void dispatchCommand(Object[] command) {
        if (command.length < 2) {
            this.updateLastServerEvent("Invalid command received; command-length = " + command.length);
            return;
        }

        String scommand = (String) command[0];
        String data = (String) command[1];

        switch (scommand) {
            case Messages.CLIENT_ECHO:
                this.updateLastServerEvent("ECHO");
                break;
            case Messages.CLIENT_BUSY:
                dispatchBusy();
                break;
            case Messages.CLIENT_DONE:
                dispatchStateDone();
                break;
            case Messages.CLIENT_IndexProgress:
                dispatchProgressFromIndexWorker(data);
                break;
            case Messages.CLIENT_Segmentation:
                dispatchResultsFromSegmentation(data);
                break;
            case Messages.CLIENT_SegmentationCodeCounts:
                dispatchResultsFromSegmentationCodeCount(data);
                break;
            case Messages.CLIENT_SegmentationBreakSegments:
                dispatchSegmentationBreakSegments(data);
                break;
            case Messages.CLIENT_SegmentationSegmentPairs:
                dispatchSegmentationSegmentPairs(data);
                break;
            case Messages.CLIENT_SegmentationSegmentShortSegmentPairs:
                dispatchSegmentationSegmentShortSegmentPairs(data);
                break;
            case Messages.CLIENT_SegmentationShortSegmentSegmentPairs:
                dispatchSegmentationShortSegmentSegmentPairs(data);
                break;
            case Messages.CLIENT_SegmentationSegmentPairOccurrence:
                dispatchSegmentationSegmentPairOccurrence(data);
                break;
            case Messages.CLIENT_SegmentationShortSegmentChainsCounts:
                dispatchSegmentationShortSegmentChainsCounts(data);
                break;
            case Messages.CLIENT_SegmentationShortSegmentChainsLengths:
                dispatchSegmentationShortSegmentChainsLengths(data);
                break;
            case Messages.CLIENT_SegmentationDroppedPeaks:
                dispatchSegmentationDroppedPeaks(data);
                break;
            case Messages.CLIENT_AdditionalData:
                dispatchResultsFromAdditional(data);
                break;
            case Messages.CLIENT_Motifs:
                dispatchResultsFromAddMotif(data);
                break;
            case Messages.CLIENT_PWM:
                dispatchResultsFromAddPWM(data);
                break;
            case Messages.CLIENT_SegmentationLength:
                dispatchSegmentationLength(data);
                break;
            case Messages.CLIENT_Correlation:
                dispatchCorrelationResult(data);
                break;
            case Messages.CLIENT_FateOfCode:
                dispatchFateOfCodeResult(data);
                break;
            case Messages.QUIT:
                dispatchQuit(data);
                break;
            default:
                this.updateLastServerEvent("Unknown command received: " + scommand);
                break;
        }
    }

    /**
     * @param results
     */
    private void dispatchCorrelationResult(String results) {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        double[][] result = gson.fromJson(results,
                                          new TypeToken<double[][]>() {
                                          }.getType());
//		for (double[] sf : result) {
//			System.out.println(Arrays.toString(sf));
//		}
        rootLayoutController.setCorrelationData(result);

        this.updateLastServerEvent("CorrelationWorker: done", true);
    }

    /**
     * @param results
     */
    private void dispatchFateOfCodeResult(String results) {
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        List<int[][]> result = gson.fromJson(results,
                                             new TypeToken<List<int[][]>>() {
                                             }.getType());
//		for (double[] sf : result) {
//			System.out.println(Arrays.toString(sf));
//		}
        rootLayoutController.setFateOfCodeData(result);

        this.updateLastServerEvent("FateOfCodeWorker: done", true);
    }

    /**
     * @param results
     */
    private void dispatchSegmentationBreakSegments(String results) {
        Gson gson = new GsonBuilder().create();
        List<BreakSegment> breakSegmentData = gson.fromJson(results,
                                                            new TypeToken<List<BreakSegment>>() {
                                                            }.getType());
        rootLayoutController.setBreakSegmentData(breakSegmentData);

        this.updateLastServerEvent("segmentationBreakSegments: done");
    }

    /**
     * @param results
     */
    private void dispatchSegmentationSegmentPairs(String results) {
        Gson gson = new GsonBuilder().create();
        Map<String, Integer> segmentPairs = gson.fromJson(results,
                                                          new TypeToken<Map<String, Integer>>() {
                                                          }.getType());
        rootLayoutController.setSegmentPairsData(segmentPairs);

        this.updateLastServerEvent("segmentationSegmentPairs: done");
    }

    /**
     * @param results
     */
    private void dispatchSegmentationSegmentShortSegmentPairs(String results) {
        Gson gson = new GsonBuilder().create();
        Map<String, Integer> segmentPairs = gson.fromJson(results,
                                                          new TypeToken<Map<String, Integer>>() {
                                                          }.getType());
        rootLayoutController.setSegmentShortSegmentPairsData(segmentPairs);

        this.updateLastServerEvent("segmentationSegmentShortSegmentPairs: done");
    }

    /**
     * @param results
     */
    private void dispatchSegmentationShortSegmentSegmentPairs(String results) {
        Gson gson = new GsonBuilder().create();
        Map<String, Integer> segmentPairs = gson.fromJson(results,
                                                          new TypeToken<Map<String, Integer>>() {
                                                          }.getType());
        rootLayoutController.setShortSegmentSegmentPairsData(segmentPairs);

        this.updateLastServerEvent("segmentationShortSegmentSegmentPairs: done");
    }

    /**
     * @param results
     */
    private void dispatchSegmentationSegmentPairOccurrence(String results) {
        Gson gson = new GsonBuilder().create();
        Map<String, Double> segmentPairOccurrence = gson.fromJson(results,
                                                                new TypeToken<Map<String, Double>>() {
                                                                }.getType());
        rootLayoutController.setSegmentPairOccurrenceData(segmentPairOccurrence);

        this.updateLastServerEvent("segmentationSegmentPairOccurrence: done");
    }

    /**
     * @param results
     */
    private void dispatchSegmentationShortSegmentChainsCounts(String results) {
        Gson gson = new GsonBuilder().create();
        SortedMap<Integer, Integer> shortSegmentChainsCounts = gson.fromJson(results,
                                                                             new TypeToken<SortedMap<Integer, Integer>>() {
                                                                             }.getType());
        rootLayoutController.setShortSegmentChainsCounts(shortSegmentChainsCounts);

        this.updateLastServerEvent("segmentationShortSegmentChainsCounts: done");
    }

    /**
     * @param results
     */
    private void dispatchSegmentationShortSegmentChainsLengths(String results) {
        Gson gson = new GsonBuilder().create();
        SortedMap<Integer, Integer> shortSegmentChainsLengths = gson.fromJson(results,
                                                                              new TypeToken<SortedMap<Integer, Integer>>() {
                                                                              }.getType());
        rootLayoutController.setShortSegmentChainsLengths(shortSegmentChainsLengths);

        this.updateLastServerEvent("segmentationShortSegmentChainsLengths: done");
    }

    /**
     * @param results
     */
    private void dispatchSegmentationLength(String results) {
        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC).create();
        DataFile df = gson.fromJson(results, DataFile.class);
        rootLayoutController.updateSegmentationLength(df);

        this.updateLastServerEvent("SegmentationLengthWorker: " + df.getDataSetName() + " done");
    }

    /**
     * @param results
     */
    private void dispatchResultsFromAddMotif(String results) {
        Gson gson = new Gson();
        Map<String, List<Double>> motifValues = gson.fromJson(results,
                                                              new TypeToken<Map<String, List<Double>>>() {
                                                              }.getType());
        rootLayoutController.updateMotifDataTabs(motifValues);

        this.updateLastServerEvent("MotifWorker: done", true);
    }

    /**
     * @param results
     */
    private void dispatchResultsFromAddPWM(String results) {
        Gson gson = new Gson();
        Map<String, List<Double>> pwmValues = gson.fromJson(results,
                                                            new TypeToken<Map<String, List<Double>>>() {
                                                            }.getType());
        rootLayoutController.updatePWMDataTabs(pwmValues);

        this.updateLastServerEvent("PWMWorker: done", true);
    }

    /**
     * @param results
     */
    private void dispatchResultsFromAdditional(String results) {
        Gson gson = new Gson();
        Map<String, List<Double>> additionalDataValues = gson.fromJson(results,
                                                                       new TypeToken<Map<String, List<Double>>>() {
                                                                       }.getType());
        rootLayoutController.updateAdditionalDataTabs(additionalDataValues);

        this.updateLastServerEvent("AdditionalWorker: done", true);
    }

    /**
     * @param results
     */
    private void dispatchResultsFromSegmentation(String results) {
        Gson gson = new Gson();
        SegmentationWorker segmentationResults = gson.fromJson(results,
                                                               new TypeToken<SegmentationWorker>() {
                                                               }.getType());
        rootLayoutController.updateSegmentationResults(segmentationResults);

        this.updateLastServerEvent("SegmentationResults: done", true);
    }

    /**
     * @param results
     */
    private void dispatchSegmentationDroppedPeaks(String results) {
        Gson gson = new Gson();
        Integer numberOfDroppedPeaks = gson.fromJson(results,
                                                     new TypeToken<Integer>() {
                                                     }.getType());
        rootLayoutController.updateSegmentationDroppedPeaks(numberOfDroppedPeaks);

        this.updateLastServerEvent("SegmentationResults: done", true);
    }

    /**
     * @param results
     */
    private void dispatchResultsFromSegmentationCodeCount(String results) {
        Gson gson = new Gson();
        SortedMap<Integer, Integer> segmentationCodeCounts = gson.fromJson(results,
                                                                           new TypeToken<SortedMap<Integer, Integer>>() {
                                                                           }.getType());
        rootLayoutController.updateCodeDistribution(segmentationCodeCounts);

        this.updateLastServerEvent("SegmentationCodeCount: done", true);
    }

    /**
     *
     * @param results
     */
    private void dispatchProgressFromIndexWorker(String log) {
        rootLayoutController.updateLogAtRefGenomeController(log);
        if (!log.equals("Save index of reference genome") && !log.equals("Load successful")) {
            this.updateLastServerEvent("IndexWorker: " + log, false);
        } else {
            rootLayoutController.indexWorkerSuccess();
            this.updateLastServerEvent("IndexWorker: " + log, true);
        }
    }

    /**
     *
     */
    private void dispatchStateDone() {
        this.updateLastServerEvent("State was exported");
    }

    /**
     *
     */
    private void dispatchBusy() {
        // TODO BusyWarning, Something what should not be done if Server is
        // busy?

    }

    /**
     * @param reason
     */
    private void dispatchQuit(String reason) {
        if (reason.equals("EXCEPTION")) {
            this.updateLastServerEvent("Quit: Exception", true);
            rootLayoutController.getClientCommander().setActive(false);
            this.updateServerState();
        } else if (reason.equals("AUTH_FAILURE")) {

            this.updateLastServerEvent("Quit: Authentication failed", true);
            rootLayoutController.getClientCommander().stopListener();
            rootLayoutController.getClientCommander().setActive(false);
            Platform.runLater(() -> {
                ConnectionManagerController reconnect = ConnectionManagerController.getInstance();
                reconnect.createServerDialog(rootLayoutController);
            });
        } else {
            this.updateLastServerEvent("Quit", true);
        }
    }

    private void updateServerState() {
        Platform.runLater(() -> {
            statusBarController.updateServerState();
        });

    }

    private void updateLastServerEvent(String message, boolean done) {
        Platform.runLater(() -> {
            statusBarController.updateProgress(message, done);
            log.info(message);
        });
    }

    private void updateLastServerEvent(String message) {
        updateLastServerEvent(message, true);
    }
}
