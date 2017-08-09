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
package biovis.hackebeil.server.worker.motifWorker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import biovis.hackebeil.common.data.Segment;
import biovis.hackebeil.common.data.Motif;
import biovis.hackebeil.server.commander.HackebeilService;
import biovis.hackebeil.server.data.ServerCache;
import biovis.hackebeil.server.data.BufferedRandomAccessFile;
import biovis.hackebeil.common.data.Location;
import biovislib.parallel4.IterationInt;
import biovislib.parallel4.Parallel2;
import biovislib.parallel4.ParallelForInt2;
import biovislib.parallel4.ParallelizationFactory;

/**
 * based on epiwgseg.segmentation.TableBuilder, epiwgseg.segmentation.DataTable
 *
 * @author nhinzmann, Alrik Hausdorf, Dirk Zeckzer
 *
 */
public class MotifWorkerMotifArrayPreprocessing
    implements Runnable,
               MotifWorkerInterface {

    private static final Logger log = Logger.getLogger(HackebeilService.class.toString());

    private Map<String, Long> startFilePos;
    private Map<String, Integer> lineLength;
    private Map<Location, Long> posInFileMap;
    private List<Segment> segments;

    private List<Motif> motifList;
    private Map<String, List<Double>> motifValues;

    private String fileName;

    private ServerCache cache;

    /**
     *
     * @param cache
     */
    public MotifWorkerMotifArrayPreprocessing(
        ServerCache cache
    ) {
        this.cache = cache;
        this.startFilePos = (HashMap<String, Long>) cache.getStartFilePos();
        this.lineLength = (HashMap<String, Integer>) cache.getLineLength();
        this.fileName = cache.getFilePathToRefGenome();
        this.segments = cache.getSegments();
        preProcessing();
    }

    @Override
    public void run() {
        log.log(Level.INFO, "Motif computation start");
        MotifCharsList motifCharsList = new MotifCharsList(motifList);
        if (computeMotifs(motifCharsList)) {
            log.log(Level.INFO, "Motif values");
            motifValues = new HashMap<>();
            for (Motif motif : motifList) {
                List<Double> valuesForMotif = new ArrayList<>();
                String motifId = motif.getMotifId();
                for (Segment segment : segments) {
                    if (segment.getLength() >= cache.getMinSegmentLength()) {
                        valuesForMotif.add(segment.getMotifValue(motifId));
                    }
                }
                motifValues.put(motifId, valuesForMotif);
            }
        }
        log.log(Level.INFO, "Motif computation end");
    }

    /**
     */
    private void preProcessing() {
        posInFileMap = new HashMap<>();
        Location location;
        for (Segment segment : segments) {
            location = segment.getLocation();
            Long posInFile = getPosInFile(location);
            posInFileMap.put(location, posInFile);
        }
    }

    /**
     *
     * @param motifList
     * @return
     */
    private boolean computeMotifs(
        MotifCharsList motifCharsList
    ) {
        int threads = cache.getNumberOfThreads();

        final int numberOfChunks = threads;
        final int chunkSize = segments.size() / (numberOfChunks - 1);
        Parallel2 p2 = ParallelizationFactory.getInstance(threads);
        new ParallelForInt2(p2, 0, numberOfChunks).loop(new IterationInt() {
            @Override
            public void iteration(final int currentChunk) {
                int chunkStart = currentChunk * chunkSize;
                int chunkEnd = chunkStart + chunkSize;
                if (chunkEnd > segments.size()) {
                    chunkEnd = segments.size();
                }

                double motifValue;
                Segment segment;
                try (BufferedRandomAccessFile bufferedReader = new BufferedRandomAccessFile(fileName, "r", 1024 * 16)) {
                    for (int i = chunkStart; i < chunkEnd; ++i) {
                        segment = segments.get(i);
                        for (MotifChars motifChars : motifCharsList.getList()) {
                            if (motifChars.getIsNormalized()) {
                                motifValue = computeMotifDensity(motifChars,
                                                                 segment.getLocation(),
                                                                 bufferedReader);
                            } else {
                                motifValue = computeMotifCount(motifChars,
                                                               segment.getLocation(),
                                                               bufferedReader);
                            }
                            segment.addMotifData(motifChars.getMotifId(), motifValue);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return true;
    }

    /**
     *
     * @param motifChars
     * @param location
     * @param bufferedReader
     */
    private long computeMotifCount(
        MotifChars motifChars,
        Location location,
        BufferedRandomAccessFile bufferedReader
    ) {
        long count = countMotif(
            motifChars.getMotifCharsUpperCase(),
            motifChars.getMotifCharsLowerCase(),
            location,
            bufferedReader
        );
        count += countMotif(
            motifChars.getMotifCharsRCUpperCase(),
            motifChars.getMotifCharsRCLowerCase(),
            location,
            bufferedReader
        );

        return count;
    }

    /**
     *
     * @param motifChars
     * @param location
     * @param bufferedReader
     */
    private double computeMotifDensity(
        MotifChars motifChars,
        Location location,
        BufferedRandomAccessFile bufferedReader
    ) {
        long count = computeMotifCount(motifChars, location, bufferedReader);
        long theoCount = location.getLength() - motifChars.length() + 1;
        return ((double) count) / (((double) theoCount) * 2);
    }

    /**
     *
     * @param motifUpperCase motif to search for (upper case)
     * @param motifLowerCase motif to search for (lower case)
     * @param location location
     * @param bufferedReader reader specially for threading
     * @return number of motif occurrences found
     */
    private long countMotif(
        char[] motifUpperCase,
        char[] motifLowerCase,
        Location location,
        BufferedRandomAccessFile bufferedReader
    ) {
        try {
//            long posInFile = getPosInFile(location);
            long posInFile = posInFileMap.get(location);
            int locLength = location.getLength();
            int motifLength = motifLowerCase.length;
            if (posInFile >= 0
                && locLength >= motifLength) {

                bufferedReader.seek(posInFile);

                // result
                long counter = 0;

                // current position
                int read = 0;

                // init search lines
                String newLine = bufferedReader.readLine();
                StringBuilder line = new StringBuilder(newLine);
                int offset = 0;
                int motifPosition = 0;

                while ((line.charAt(0) != '>')
                       && read < locLength) {
                    for (int segmentPosition = offset;
                         segmentPosition < line.length() && read < locLength;
                         segmentPosition++) {
                        if (line.charAt(segmentPosition) == motifUpperCase[motifPosition]
                            || line.charAt(segmentPosition) == motifLowerCase[motifPosition]) {
                            motifPosition++;
                            if (motifPosition >= motifLength) {
                                // Found complete motif
                                segmentPosition -= motifPosition - 1;
                                read -= motifPosition - 1;
                                counter++;
                                motifPosition = 0;
                            }
                        } else {
                            // no match found
                            if (motifPosition > 0) {
                                // reset to motif-part-found
                                segmentPosition -= motifPosition;
                                read -= motifPosition;
                                motifPosition = 0;
                            }
                        }
                        read++;
                    }
                    // if new line starts and there are parts found on old line
                    int deletion = newLine.length() - motifPosition;
                    try {
                        if (deletion > 0) {
                            line.delete(0, deletion);
                        }
                    } catch (StringIndexOutOfBoundsException ex) {
                        System.out.println("deletion: " + deletion
                                           + " " + newLine.length()
                                           + " " + motifPosition
                                           + " " + (newLine.length() - motifPosition));
                    }
                    offset = line.length();
                    newLine = bufferedReader.readLine();
                    line.append(newLine);
                }

                return counter;
            } else {
//                System.out.println("startFilePos does not contain: " + loc.getChr());
                return 0;
            }
        } catch (IOException ioEx) {
            log.log(Level.SEVERE, "IOException while counting motifs: {0} {1}", new Object[]{ioEx.getMessage()});
            System.out.println(ioEx.getMessage());
            return 0;
        }
    }

    private long getPosInFile(Location location) {
        String chromosome = location.getChr();
        long locationStart = location.getStart();
        if (startFilePos.containsKey(location.getChr())
            && locationStart >= 0) {
            long fileStart = startFilePos.get(chromosome);
            long linelen = lineLength.get(chromosome);
            long posInFile = (locationStart / linelen) * (linelen + 1)
                             + locationStart % linelen
                             + fileStart;
            return posInFile;
        } else {
            return -1;
        }
    }

    /**
     *
     * @param motifList
     */
    @Override
    public void setMotifList(List<Motif> motifList) {
        this.motifList = motifList;
    }

    @Override
    public Map<String, List<Double>> getMotifValues() {
        return motifValues;
    }
}
