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
import biovis.hackebeil.server.data.ServerCache;
import biovis.hackebeil.server.data.BufferedRandomAccessFile;
import biovis.hackebeil.common.data.Location;
import biovis.hackebeil.server.worker.SequenceReader;
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
public class MotifWorkerMotifParallel
    implements MotifWorkerInterface {

    private static final Logger log = Logger.getLogger("MotifWorkerMotifParallel");

    private ServerCache cache;
    private String fileName;

    private List<Segment> segments;
    private SequenceReader sqr;

    private List<Motif> motifList;
    private Map<String, List<Double>> motifValues;

    /**
     *
     * @param cache
     */
    public MotifWorkerMotifParallel(
        ServerCache cache
    ) {
        this.cache = cache;
        this.fileName = cache.getFilePathToRefGenome();
        this.segments = cache.getSegments();
        sqr = new SequenceReader(cache);
    }

    /**
     *
     */
    public void computeMotifs() {
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
     *
     * @param motifList
     * @return
     */
    private boolean computeMotifs(
        MotifCharsList motifCharsList
    ) {
        try (BufferedRandomAccessFile bufferedReader = new BufferedRandomAccessFile(fileName, "r", 1024 * 16)) {
            for (Segment segment : segments) {
                char[] segmentDNA = sqr.readSegment(segment, bufferedReader);
                int threads = cache.getNumberOfThreads();
                Parallel2 p2 = ParallelizationFactory.getInstance(threads);
                int numberOfMotifs = motifCharsList.getList().size();
                new ParallelForInt2(p2, 0, numberOfMotifs).loop(new IterationInt() {
                    @Override
                    public void iteration(final int currentMotif) {
                        MotifChars motifChars = motifCharsList.getList().get(currentMotif);
                        double motifValue;

                        if (motifChars.getIsNormalized()) {
                            motifValue = computeMotifDensity(motifChars, segment.getLocation(), segmentDNA);
                        } else {
                            motifValue = computeMotifCount(motifChars, segmentDNA);
                        }
                        synchronized (segment) {
                            segment.addMotifData(motifChars.getMotifId(), motifValue);
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     *
     * @param motifChars
     * @param segmentDNA
     */
    private long computeMotifCount(
        MotifChars motifChars,
        char[] segmentDNA
    ) {
        long count = countMotif(
            motifChars.getMotifCharsUpperCase(),
            motifChars.getMotifCharsLowerCase(),
            segmentDNA
        );
        count += countMotif(
            motifChars.getMotifCharsRCUpperCase(),
            motifChars.getMotifCharsRCLowerCase(),
            segmentDNA
        );

        return count;
    }

    /**
     *
     * @param motifChars
     * @param location
     * @param segmentDNA
     */
    private double computeMotifDensity(
        MotifChars motifChars,
        Location location,
        char[] segmentDNA
    ) {
        long count = computeMotifCount(motifChars, segmentDNA);
        long theoCount = location.getLength() - motifChars.length() + 1;
        return ((double) count) / (((double) theoCount) * 2);
    }

    /**
     *
     * @param motifUpperCase motif to search for (upper case)
     * @param motifLowerCase motif to search for (lower case)
     * @param segmentDNA
     * @return number of motif occurrences found
     */
    private long countMotif(
        char[] motifUpperCase,
        char[] motifLowerCase,
        char[] segmentDNA
    ) {
        int motifLength = motifLowerCase.length;
        int segmentLength = segmentDNA.length;
        if (segmentLength >= motifLength) {
            // result
            long counter = 0;

            int motifPosition = 0;

            for (int segmentPosition = 0;
                 segmentPosition < segmentLength;
                 segmentPosition++) {
                if (segmentDNA[segmentPosition] == motifUpperCase[motifPosition]
                    || segmentDNA[segmentPosition] == motifLowerCase[motifPosition]) {
                    motifPosition++;
                    if (motifPosition >= motifLength) {
                        // Found complete motif
                        counter++;
                        segmentPosition -= motifPosition - 1;
                        motifPosition = 0;
                    }
                } else {
                    // no match found
                    if (motifPosition > 0) {
                        // reset to motif-part-found
                        segmentPosition -= motifPosition;
                        motifPosition = 0;
                    }
                }
            }

            return counter;
        } else {
//                System.out.println("startFilePos does not contain: " + loc.getChr());
            return 0;
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
