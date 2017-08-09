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
public class MotifWorkerOrig
    implements Runnable,
               MotifWorkerInterface
{

    private static final Logger log = Logger.getLogger(HackebeilService.class.toString());

    private Map<String, Long> startFilePos;
    private Map<String, Integer> lineLength;
    private List<Segment> segments;

    private List<Motif> motifList;
    private Map<String, List<Double>> motifValues;

    private String fileName;

    private ServerCache cache;

    /**
     *
     * @param cache
     */
    public MotifWorkerOrig(
        ServerCache cache
    ) {
        this.cache = cache;
        this.startFilePos = (HashMap<String, Long>) cache.getStartFilePos();
        this.lineLength = (HashMap<String, Integer>) cache.getLineLength();
        this.fileName = cache.getFilePathToRefGenome();
        this.segments = cache.getSegments();
    }

    @Override
    public void run() {
        if (this.computeMotifs(motifList)) {
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
    }

    /**
     *
     * @param motifList
     * @return
     */
    private boolean computeMotifs(List<Motif> motifList) {
        int threads = cache.getNumberOfThreads();

        final int numberOfChunks = threads;
        final int chunkSize = segments.size() / (numberOfChunks - 1);
        Parallel2 p2 = ParallelizationFactory.getInstance(1); //threads);
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
                        for (Motif motif : motifList) {
                            if (motif.getIsNormalized()) {
                                motifValue = computeMotifDensity(motif.getMotif(),
                                                                 segment.getLocation(),
                                                                 bufferedReader);
                            } else {
                                motifValue = computeMotifCount(motif.getMotif(),
                                                               segment.getLocation(),
                                                               bufferedReader);
                            }
                            segment.addMotifData(motif.getMotifId(), motifValue);
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
     * @param motif
     * @param location
     * @param bufferedReader
     */
    private long computeMotifCount(
        String motif,
        Location location,
        BufferedRandomAccessFile bufferedReader
    ) {
        // long startTime = System.nanoTime();
        long count = countMotif(motif, location, bufferedReader);
        // long estimatedTime = System.nanoTime() - startTime;
        // long startTime2 = System.nanoTime();
        // int count2 = countMotif2(loc, motif, bufferedReader);
        // long estimatedTime2 = System.nanoTime() - startTime2;
        // System.out.println("motifCountDiff: " + (count - count2) + " after "
        // + (estimatedTime - estimatedTime2));

        return count;
    }

    /**
     *
     * @param motif
     * @param location
     * @param bufferedReader
     */
    private double computeMotifDensity(
        String motif,
        Location location,
        BufferedRandomAccessFile bufferedReader
    ) {
        long count = computeMotifCount(motif, location, bufferedReader);
        long theoCount = location.getLength() - motif.length() + 1;
        return ((double) count) / ((double) theoCount * 2);
    }

    /**
     * Count number of motifs.
     *
     * @param motif motif to search for
     * @param loc location
     * @return number of motif occurrences found
     */
    private Integer countMotif(
        String motif,
        Location loc,
        BufferedRandomAccessFile bufferedReader
    ) {
        // String motif = search.toUpperCase();

        // System.err.println("Counting motif: " + motif + " " + loc);
        // try (RandomAccessFile bufferedReader = new RandomAccessFile(filename,
        // "r")) {
        // System.out.println(bufferedReader);
        try {
            if (startFilePos.containsKey(loc.getChr())
                && loc.getStart() >= 0
                && loc.getLength() >= motif.length()) {

                // calculate file position and set reader to this position
                // System.out.println("loc.getChr() " + loc.getChr());
                Long fileStart = startFilePos.get(loc.getChr());
                // System.out.println("fileStart " + fileStart);
                Integer linelen = lineLength.get(loc.getChr());
                // System.out.println("linelen " + linelen);
                Long posInFile = (long) ((loc.getStart() / linelen) * (linelen + 1) + loc.getStart() % linelen)
                                 + fileStart;
                // System.out.println("posInFile " + posInFile);
                bufferedReader.seek(posInFile);

                // read sequence and count
                Integer counter = 0;
                Integer read = 0;
                Integer seqCounter = 0;
                String seq = "";
                String line = bufferedReader.readLine();
                while (line != null && (line.charAt(0) != '>') && read < loc.getLength()) {
                    // System.out.println("While");
                    line = line.trim();
                    // System.out.println((++lineNumber) + " line " + line);
                    line = line.toUpperCase();
                    if ((read + line.length()) <= loc.getLength()) {
                        read += line.length();
                    } else {
                        Integer remaining = loc.getLength() - read;
                        line = line.substring(0, remaining);
                        read += remaining;
                    }
                    seq += line;
                    while (seq.length() >= motif.length() + seqCounter) {
                        if (seq.startsWith(motif, seqCounter)) {
                            counter++;
                            // System.out.println("found@" + read);
                        }
                        seqCounter++;
                    }
                    seq = seq.substring(seqCounter);
                    seqCounter = 0;
                    line = bufferedReader.readLine();
                }
                // System.out.println("Counter " + counter);
                return counter;
            } else {
                // System.out.println("startFilePos does not contain: " + loc.getChr());
                return 0;
            }
        } catch (IOException ioEx) {
            log.log(Level.SEVERE, "IOException while counting motifs: {0} {1}", new Object[]{ioEx.getMessage()});
            System.out.println(ioEx.getMessage());
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
