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
package biovis.hackebeil.server.commander;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.common.data.FateOfCodeParameter;
import biovis.hackebeil.common.data.Messages;
import biovis.hackebeil.common.data.Segment;
import biovis.hackebeil.common.data.Motif;
import biovis.hackebeil.common.data.PositionWeightMatrix;
import biovis.hackebeil.server.data.BedFileLoader;
import biovis.hackebeil.server.data.ServerCache;
import biovis.hackebeil.server.io.DataIO;
import biovis.hackebeil.server.worker.AdditionalDataWorker;
import biovis.hackebeil.server.worker.CorrelationWorker;
import biovis.hackebeil.server.worker.FateOfCodeWorker;
import biovis.hackebeil.server.worker.IndexWorker;
import biovis.hackebeil.server.worker.segmentation.SegmentationWorker;
import biovis.hackebeil.server.worker.motifWorker.MotifWorkerMotifParallel;
import biovis.hackebeil.server.worker.positionWeightMatrix.PWMWorker;
import biovis.hackebeil.server.worker.segmentation.BreakSegmentWorker;
import biovis.hackebeil.server.worker.segmentation.SegmentPairWorker;
import biovis.hackebeil.server.worker.segmentation.ShortSegmentWorker;
import biovislib.remoteControl.CommandDispatcherInterface;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Based on biovis.sierra.server.Commander.PeakDispatcher
 *
 * @author nhinzmann, Dirk Zeckzer
 *
 */
public class ServerDispatcher
    implements CommandDispatcherInterface {

    private static final Logger log = Logger.getLogger("ServerDispatcher");

    private ServerMapper server;
    private ServerCommander serverCommander = new ServerCommander();
    private ServerCache cache;

    private Gson gson = new Gson();

    /**
     * @param serverMapper
     * @param serverCache
     */
    public ServerDispatcher(
        ServerMapper serverMapper,
        ServerCache serverCache
    ) {
        this.server = serverMapper;
        this.cache = serverCache;
    }

    /**
     * @param command 
     */
    @Override
    public void dispatchCommand(Object[] command) {
        String scommand = (String) command[0];

        log.log(Level.INFO, "Command received: {0}", scommand);
        new Thread(() -> {
            // try {
            // Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // JsonParser jp = new JsonParser();
            // JsonElement je = jp.parse((String) command[1]);
            // String prettyJsonString = gson.toJson(je);
            // FileOutputStream out = new FileOutputStream("ServerDispatcher_" +
            // scommand + ".json");
            //
            // out.write(prettyJsonString.getBytes());
            // out.close();
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
            switch (scommand) {
                case Messages.SERVER_setClient:
                    log.info("set client");
                    setClient(command);
                    break;
                case Messages.SERVER_credentials:
                    log.info("received credentials");
                    setCredentials(command);
                    break;
                case Messages.SERVER_clear:
                    log.info("server clear");
                    clear();
                    break;
                case Messages.SERVER_createIndex:
                    log.info("create Index");
                    createIndex(command);
                    break;
                case Messages.SERVER_loadIndex:
                    log.info("load Index");
                    loadIndex(command);
                    break;
                case Messages.SERVER_startSegmentation:
                    log.info("start Segmentation");
                    computeSegmentation(command);
                    break;
                case Messages.SERVER_addAdditionalData:
                    log.info("start Additional Data");
                    computeAdditionalData(command);
                    break;
                case Messages.SERVER_addMotif:
                    log.info("start Motif search");
                    computeMotifs(command);
                    break;
                case Messages.SERVER_addPWM:
                    log.info("start PWM computation");
                    computePWM(command);
                    break;
                case Messages.SERVER_startCorrelation:
                    log.info("start Correlation");
                    computeCorrelation(command);
                    break;
                case Messages.SERVER_startFateOfCodeComputation:
                    log.info("start Fate-of-Code computation");
                    computeFateOfCode(command);
                    break;
                case Messages.SERVER_exportSegmentation:
                    log.info("export Segmentation");
                    exportSegmentation(command);
                    break;
                case Messages.QUIT:
                    log.info("quit");
                    dispatchQuit(command);
                    break;
                default:
                    log.log(Level.INFO, "Received unknown command: {0}", scommand);
                    break;
            }
        }).start();
    }

    /**
     * Set client.
     *
     * @param command
     */
    private void setClient(Object[] command) {
        Object[] url = (Object[]) command[1];

        String host = (String) url[0];
        int port = (int) url[1];
        String hash = (String) url[2];

        serverCommander.setServer(host, port, hash);
    }

    /**
     * Set new credentials.
     *
     * @param command
     */
    private void setCredentials(Object[] command) {
        log.info("setCredentials");
        Object[] payload = (Object[]) command[1];
        String passwd = (String) payload[0];
        // String hash = (String) payload[1];
        if (server.isLocked()) {
            log.info("check password");

            if (server.getPassword().equals(passwd)) {
                log.info("password matched!");

                // pc.sendCommand(command);
            } else {
                log.info("Auth Failure");
                Object[] c = {"QUIT", "AUTH_FAILURE"};
                serverCommander.sendCommand(c);
            }
        } else {
            log.info("Set password");
            server.setPassword(passwd);
            server.setLocked(true);
        }
    }

    /**
     * Clear data.
     *
     */
    private void clear() {
        cache.clear();
    }

    /**
     * @param command
     */
    private void loadIndex(Object[] command) {
        Object[] filePath = (Object[]) command[1];
        String pathToRefGenome = (String) filePath[0];
        String pathToLoadIndex = (String) filePath[1];
        cache.setFilePathToRefGenome(pathToRefGenome);
        cache.setFilePathToIndex(pathToLoadIndex);

        IndexWorker indexWorker = new IndexWorker(cache, serverCommander);
        indexWorker.startLoadIndex();
    }

    /**
     * @param command
     */
    private void createIndex(Object[] command) {
        // System.err.println("ServerDispatcher.creatingIndex");
        Object[] filePath = (Object[]) command[1];
        String pathToRefGenome = (String) filePath[0];
        String pathToSaveIndex = (String) filePath[1];
        cache.setFilePathToRefGenome(pathToRefGenome);
        cache.setFilePathToIndex(pathToSaveIndex);
        // System.err.println(pathToRefGenome);
        // System.err.println(pathToSaveIndex);

        IndexWorker indexWorker = new IndexWorker(cache, serverCommander);
        indexWorker.startCreateAndSaveIndex();
    }

    /**
     * @param command
     */
    private void computeSegmentation(Object[] command) {
        String dataFileList = (String) command[1];
        Integer minSegmentLength = (Integer) command[2];

        List<DataFile> list = gson.fromJson(dataFileList,
                                            new TypeToken<ArrayList<DataFile>>() {
                                            }.getType());
        if (cache.getSeqLengths() == null
            || cache.getSeqStart() == null) {
            log.info("No genome index found. Please create or load index first!");
            return;
        }
        if (list == null) {
            log.info("No elements found for segmentation!");
            return;
        }
        cache.setReferenceList(list);
        cache.setMinSegmentLength(minSegmentLength);
        computeLengthDistributionReferenceDataSets(list);

        SegmentationWorker segmentationWorker = new SegmentationWorker();
//            SegmentationWorkerD segmentationWorker = new SegmentationWorkerD();
        segmentationWorker.setRefFiles(list);
        segmentationWorker.setSeqLengths(cache.getSeqLengths());
        segmentationWorker.setSeqStart(cache.getSeqStart());
        if (segmentationWorker.doSegmentation(minSegmentLength)) {
            cache.setSegments(segmentationWorker.getSegments());
            sendSegmentationStatistics(segmentationWorker);

            BreakSegmentWorker breakSegmentWorker = new BreakSegmentWorker(segmentationWorker);
            sendBreakSegments(breakSegmentWorker);

            SegmentPairWorker segmentPairWorker = new SegmentPairWorker(segmentationWorker, 200);
            sendSegmentPairs(segmentPairWorker);

            ShortSegmentWorker shortSegmentWorker = new ShortSegmentWorker(segmentationWorker);
            sendShortSegmentChains(shortSegmentWorker);
            sendDroppedPeaks(shortSegmentWorker);
        }
    }

    /**
     * Send segment information to client.
     *
     * @param segmentationWorker
     */
    private void sendSegmentationStatistics(SegmentationWorker segmentationWorker) {
        log.log(Level.INFO, "send statistics start");

        // Send information about trash to client
        if (serverCommander.isActive() > 0) {
            Object[] answer = new Object[2];
            answer[0] = Messages.CLIENT_Segmentation;
            Gson toGson = new GsonBuilder().create();
            // Type listOfResults = new
            // TypeToken<List<DataPoint>>(){}.getType();
            //answer[1] = toGson.toJson(segmentationWorker.getTrashLengths());
            answer[1] = toGson.toJson(segmentationWorker);
            serverCommander.sendCommand(answer);
            // log.info("SEND SegmentationTrashLengthWorker: done(" +
            // trashLengths.size() + ")");
        }

        // Send information about break segments to client
        if (serverCommander.isActive() > 0) {
            Object[] answer = new Object[2];
            answer[0] = Messages.CLIENT_SegmentationCodeCounts;
            Gson toGson = new GsonBuilder().create();
            // Type listOfResults = new
            // TypeToken<List<DataPoint>>(){}.getType();
            answer[1] = toGson.toJson(segmentationWorker.getCodeCounts(cache.getMinSegmentLength()));
            serverCommander.sendCommand(answer);
            // log.info("SEND SegmentationTrashLengthWorker: done(" +
            // trashLengths.size() + ")");
        }

        log.log(Level.INFO, "send statistics end");
    }

    /**
     * Send segment information to client.
     *
     * @param breakSegmentWorker
     */
    private void sendBreakSegments(BreakSegmentWorker breakSegmentWorker) {
        log.log(Level.INFO, "send break segments start");

        if (breakSegmentWorker.getBreakSegmentList().isEmpty()) {
            return;
        }

        // Send information about break segments to client
        if (serverCommander.isActive() > 0) {
            Object[] answer = new Object[2];
            answer[0] = Messages.CLIENT_SegmentationBreakSegments;
            Gson toGson = new GsonBuilder().create();
            // Type listOfResults = new
            // TypeToken<List<DataPoint>>(){}.getType();
            answer[1] = toGson.toJson(breakSegmentWorker.getBreakSegmentList());
            serverCommander.sendCommand(answer);
            // log.info("SEND SegmentationTrashLengthWorker: done(" +
            // trashLengths.size() + ")");
        }

        log.log(Level.INFO, "send break segments end");
    }

    /**
     * Send segment information to client.
     *
     * @param segmentPairWorker
     */
    private void sendSegmentPairs(SegmentPairWorker segmentPairWorker) {
        log.log(Level.INFO, "send segments pairs start");

        // Send information about segment pairs to client
        if (serverCommander.isActive() > 0) {
            Object[] answer = new Object[2];
            answer[0] = Messages.CLIENT_SegmentationSegmentPairs;
            Gson toGson = new GsonBuilder().create();
            // Type listOfResults = new
            // TypeToken<List<DataPoint>>(){}.getType();
            answer[1] = toGson.toJson(segmentPairWorker.getSegmentPairMap());
            serverCommander.sendCommand(answer);
            // log.info("SEND SegmentationTrashLengthWorker: done(" +
            // trashLengths.size() + ")");
        }

        // Send information about segment pairs to client
        if (serverCommander.isActive() > 0) {
            Object[] answer = new Object[2];
            answer[0] = Messages.CLIENT_SegmentationSegmentShortSegmentPairs;
            Gson toGson = new GsonBuilder().create();
            // Type listOfResults = new
            // TypeToken<List<DataPoint>>(){}.getType();
            answer[1] = toGson.toJson(segmentPairWorker.getSegmentShortSegmentPairMap());
            serverCommander.sendCommand(answer);
            // log.info("SEND SegmentationTrashLengthWorker: done(" +
            // trashLengths.size() + ")");
        }

        // Send information about segment pairs to client
        if (serverCommander.isActive() > 0) {
            Object[] answer = new Object[2];
            answer[0] = Messages.CLIENT_SegmentationShortSegmentSegmentPairs;
            Gson toGson = new GsonBuilder().create();
            // Type listOfResults = new
            // TypeToken<List<DataPoint>>(){}.getType();
            answer[1] = toGson.toJson(segmentPairWorker.getShortSegmentSegmentPairMap());
            serverCommander.sendCommand(answer);
            // log.info("SEND SegmentationTrashLengthWorker: done(" +
            // trashLengths.size() + ")");
        }

        // Send information about segment pairs to client
        if (serverCommander.isActive() > 0) {
            Object[] answer = new Object[2];
            answer[0] = Messages.CLIENT_SegmentationSegmentPairOccurrence;
            Gson toGson = new GsonBuilder().create();
            // Type listOfResults = new
            // TypeToken<List<DataPoint>>(){}.getType();
            answer[1] = toGson.toJson(segmentPairWorker.getSegmentPairRelationMap());
            serverCommander.sendCommand(answer);
            // log.info("SEND SegmentationTrashLengthWorker: done(" +
            // trashLengths.size() + ")");
        }
        log.log(Level.INFO, "send segments pairs end");
    }

    /**
     * Send segment information to client.
     *
     * @param shortSegmentWorker 
     */
    private void sendShortSegmentChains(ShortSegmentWorker shortSegmentWorker) {
        log.log(Level.INFO, "send short segments chains start");

        // Send information about segment pairs to client
        if (serverCommander.isActive() > 0) {
            Object[] answer = new Object[2];
            answer[0] = Messages.CLIENT_SegmentationShortSegmentChainsCounts;
            Gson toGson = new GsonBuilder().create();
            // Type listOfResults = new
            // TypeToken<List<DataPoint>>(){}.getType();
            answer[1] = toGson.toJson(shortSegmentWorker.getShortSegmentChainsCounts());
            serverCommander.sendCommand(answer);
            // log.info("SEND SegmentationTrashLengthWorker: done(" +
            // trashLengths.size() + ")");
        }

        // Send information about segment pairs to client
        if (serverCommander.isActive() > 0) {
            Object[] answer = new Object[2];
            answer[0] = Messages.CLIENT_SegmentationShortSegmentChainsLengths;
            Gson toGson = new GsonBuilder().create();
            // Type listOfResults = new
            // TypeToken<List<DataPoint>>(){}.getType();
            answer[1] = toGson.toJson(shortSegmentWorker.getShortSegmentChainsLengths());
            serverCommander.sendCommand(answer);
            // log.info("SEND SegmentationTrashLengthWorker: done(" +
            // trashLengths.size() + ")");
        }

        log.log(Level.INFO, "send short segments chains end");
    }

    /**
     * Send segment information to client.
     *
     * @param shortSegmentWorker 
     */
    private void sendDroppedPeaks(ShortSegmentWorker shortSegmentWorker) {
        log.log(Level.INFO, "send number of dropped peaks start");

        // Send information about segment pairs to client
        if (serverCommander.isActive() > 0) {
            Object[] answer = new Object[2];
            answer[0] = Messages.CLIENT_SegmentationDroppedPeaks;
            Gson toGson = new GsonBuilder().create();
            // Type listOfResults = new
            // TypeToken<List<DataPoint>>(){}.getType();
            answer[1] = toGson.toJson(shortSegmentWorker.getNumberOfDroppedPeaks());
            serverCommander.sendCommand(answer);
            // log.info("SEND SegmentationTrashLengthWorker: done(" +
            // trashLengths.size() + ")");
        }

        log.log(Level.INFO, "send number of dropped peaks end");
    }

    /**
     * Compute length distribution of peaks in reference data sets.
     *
     * @param dataFiles data files
     */
    public void computeLengthDistributionReferenceDataSets(
        List<DataFile> dataFiles
    ) {
        new Thread(() -> {
            log.info("Start length count");
            for (DataFile df : dataFiles) {
                BedFileLoader.readLengths(df);
                if (serverCommander.isActive() > 0) {
                    Object[] answer = new Object[2];
                    answer[0] = Messages.CLIENT_SegmentationLength;
                    Gson toGson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC).create();
                    // Type listOfResults = new
                    // TypeToken<List<DataPoint>>(){}.getType();
                    answer[1] = toGson.toJson(df);
                    serverCommander.sendCommand(answer);
                }
            }
            log.info("End length count");
        }).start();
    }

    /**
     * @param command
     */
    private void computeAdditionalData(Object[] command) {
        String dataFileList = (String) command[1];
        // System.out.println(dataFileList);
        // ArrayList<DataFile> list = gson.fromJson(dataFileList,
        // ArrayList.class);
        List<DataFile> dfList = gson.fromJson(dataFileList,
                                              new TypeToken<ArrayList<DataFile>>() {
                                              }.getType());
        if (cache.getSegments() != null) {
            AdditionalDataWorker additionalDataWorker = new AdditionalDataWorker();
//            AdditionalDataWorkerD additionalDataWorker = new AdditionalDataWorkerD();
            additionalDataWorker.setSegments(cache.getSegments());

            if (additionalDataWorker.compute(dfList)) {
                cache.setSegments(additionalDataWorker.getSegments());

                Map<String, List<Double>> additionalDataValues = new HashMap<>();
                for (DataFile df : dfList) {
                    List<Double> valuesForAdditionalData = new ArrayList<>();
                    String dataSetName = df.getDataSetName();
                    for (Segment segment : cache.getSegments()) {
                        valuesForAdditionalData.add(segment.getAdditionalDataValue(dataSetName));
                    }
                    additionalDataValues.put(dataSetName, valuesForAdditionalData);
                }
                cache.setModifications(dfList, additionalDataValues);

                if (serverCommander.isActive() > 0) {
                    Object[] answer = new Object[2];
                    answer[0] = Messages.CLIENT_AdditionalData;
                    Gson toGson = new Gson();
                    String results = toGson.toJson(additionalDataValues);
                    // String results =
                    // toGson.toJson(additionalDataWorker.getTableOfDataSegment());
                    // System.out.println(results.substring(0, 500));
                    answer[1] = results;
                    serverCommander.sendCommand(answer);
                }
            }
        }
    }

    /**
     * @param command
     */
    private void computeMotifs(Object[] command) {
        String mList = (String) command[1];
        List<Motif> motifList = gson.fromJson(mList,
                                              new TypeToken<ArrayList<Motif>>() {
                                              }.getType());
        if (cache.getSegments() != null) {
            log.log(Level.INFO, "Compute motifs");
            MotifWorkerMotifParallel motifWorker = new MotifWorkerMotifParallel(cache);
            motifWorker.setMotifList(motifList);
            motifWorker.computeMotifs();

            log.log(Level.INFO, "Put motifs into cache");
            Map<String, List<Double>> motifValues = motifWorker.getMotifValues();
            cache.setMotifs(motifList, motifValues);

            log.log(Level.INFO, "Motif send command");
            if (serverCommander.isActive() > 0) {
                Object[] answer = new Object[2];
                answer[0] = Messages.CLIENT_Motifs;
                Gson toGson = new Gson();
                String results = toGson.toJson(motifValues);
                answer[1] = results;
                serverCommander.sendCommand(answer);
            }
        } else {
            log.info("segments is null");
        }
    }

    /**
     * @param command
     */
    private void computePWM(Object[] command) {
        String mList = (String) command[1];
        List<PositionWeightMatrix> pwmList = gson.fromJson(mList,
                                                           new TypeToken<ArrayList<PositionWeightMatrix>>() {
                                                           }.getType());
        if (cache.getSegments() != null) {
            PWMWorker pwmWorker = new PWMWorker(cache);
            pwmWorker.setPWMList(pwmList);
            pwmWorker.compute();

            log.log(Level.INFO, "Put PWM into cache");
            Map<String, List<Double>> pwmValues = pwmWorker.getPWMValues();
            cache.setPWM(pwmList, pwmValues);

            log.log(Level.INFO, "PWM send command");
            if (serverCommander.isActive() > 0) {
                Object[] answer = new Object[2];
                answer[0] = Messages.CLIENT_PWM;
                Gson toGson = new Gson();
                String results = toGson.toJson(pwmValues);
                answer[1] = results;
                serverCommander.sendCommand(answer);
            }
        } else {
            log.info("segments is null");
        }
    }

    /**
     * @param command
     */
    private void computeFateOfCode(Object[] command) {
        if (cache.getSeqLengths() != null && cache.getSeqStart() != null) {
            FateOfCodeWorker fateOfCodeWorker = new FateOfCodeWorker(cache);
            String fateOfCodeParameterString = (String) command[1];
            FateOfCodeParameter fateOfCodeParameter = gson.fromJson(fateOfCodeParameterString,
                                                                    new TypeToken<FateOfCodeParameter>() {
                                                                    }.getType());
            if (fateOfCodeWorker.compute(fateOfCodeParameter.getSelection(),
                                         fateOfCodeParameter.getThreshold())) {
                if (serverCommander.isActive() > 0) {
                    Object[] answer = new Object[2];
                    answer[0] = Messages.CLIENT_FateOfCode;
                    Gson toGson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
                    answer[1] = toGson.toJson(fateOfCodeWorker.getResults());
                    serverCommander.sendCommand(answer);
                }
            }
        }
    }

    /**
     * @param command
     */
    private void computeCorrelation(Object[] command) {
        if (cache.getSeqLengths() != null
            && cache.getSeqStart() != null) {
            CorrelationWorker correlationWorker = new CorrelationWorker(cache);
            if (correlationWorker.compute()) {
                if (serverCommander.isActive() > 0) {
                    Object[] answer = new Object[2];
                    answer[0] = Messages.CLIENT_Correlation;
                    Gson toGson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
                    // Type listOfResults = new
                    // TypeToken<List<DataPoint>>(){}.getType();
                    answer[1] = toGson.toJson(correlationWorker.getResultTable());
                    serverCommander.sendCommand(answer);
                }
            }
        }
    }

    /**
     *
     * @param command
     */
    public void exportSegmentation(Object[] command) {
        String filename = (String) command[1];
        DataIO.exportData(filename, cache);
    }

    /**
     * Quit.
     *
     * @param command
     */
    private void dispatchQuit(Object[] command) {
        log.info("dispacherQUIT");
        // Client terminate or connection done?
        // String command0 = (String) command[0];
        String command1 = (String) command[1];
        if (command1.equals("QUIT")) {
            // server.setLocked(true);
            serverCommander.setActive();
        }
    }
}
