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
package biovis.hackebeil.server.worker.segmentation;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.common.data.Segment;
import biovis.hackebeil.common.data.Location;
import biovis.hackebeil.server.data.BedData;
import biovis.hackebeil.server.data.Boundary;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Based on epiwgseg.segmentation.TableBuilder.java and
 * epiwgseg.segmentation.DataTable.java
 *
 * @author nhinzmann, Alrik Hausdorf, Dirk Zeckzer, Lydia Mueller
 *
 */
public class SegmentationWorker {

    transient private Logger log = Logger.getLogger(SegmentationWorker.class.toString());

    // Segment information
    transient private List<Segment> segments = new ArrayList<>();

    private long segmentCount = 0;
    private long totalSegmentLength = 0;
    private SortedMap<Integer, Integer> segmentLengthDistribution = new TreeMap<>();
    private SortedMap<Integer, Integer> segmentModifiedLengthDistribution = new TreeMap<>();
    private SortedMap<Integer, Integer> segmentUnmodifiedLengthDistribution = new TreeMap<>();

    // Short segment information
    private long shortSegmentCount = 0;
    private long totalShortSegmentLength = 0;
    private SortedMap<Integer, Integer> shortSegmentLengthDistribution = new TreeMap<>();

    // Maps
    transient private Map<String, Integer> seqLengths;
    transient private Map<String, Integer> seqStart;

    // private Cache cache;
    transient private List<DataFile> refFiles;
    transient private List<BedData> bedData;

    private boolean divided = false;

    /**
     * Constructor.
     *
     */
    public SegmentationWorker() {
    }

    /**
     * Load bed file content.
     *
     * @param refFiles
     *            names of the reference files
     * @return list of reference data
     */
    private List<BedData> loadBedFiles(List<DataFile> refFiles) {
        // load Bed files
        log.info("reading bed files");

        int readBeds = 0;
        List<BedData> beds = new ArrayList<>();
        for (DataFile refFile : refFiles) {
            log.log(Level.INFO, "reading bed files: {0}/{1}", new Object[]{(readBeds + 1), refFiles.size()});
            BedData bed = new BedData(refFile.getFilePath());
            beds.add(bed);
            readBeds++;
        }
        log.info("bed files read");

        return beds;
    }

    /**
     * Construct segments from references
     *
     * @param references reference modifications
     *
     */
    private void constructSegments(List<BedData> references) {
        log.log(Level.INFO, "construct segments start");

        divided = false;

        // get boundaries of all references
        SortedSet<Boundary> boundaries = new TreeSet<>();
        for (BedData bed : references) {
            boundaries.addAll(bed.getAllBoundaries());
        }

        // construct segments
        segments.clear();

        String lastChr = "";
        Integer lastPos = -1;

        Location location;
        Integer endPos;
        for (Boundary b : boundaries) {
            if (!b.getChr().equals(lastChr)) {
                // new chromosome
                if (!lastChr.equals("")) {
                    // not the first chromosome -> add end element of old chromosome
                    endPos = getSeqLength(lastChr);
                    location = new Location(lastChr,
                                            lastPos,
                                            endPos);
                    createSegment(location, references);
                }

                // new chromosome: add start element of new chromosome
                lastChr = b.getChr();
                location = new Location(lastChr,
                                        getSeqStart(lastChr),
                                        b.getPosition() - 1);
                createSegment(location, references);
            } else {
                // same chromosome
                location = new Location(lastChr,
                                        lastPos,
                                        b.getPosition() - 1);
                createSegment(location, references);
            }

            // remember last position
            lastPos = b.getPosition();
        }

        // end segment for last chromosome
        endPos = getSeqLength(lastChr);
        location = new Location(lastChr,
                                lastPos,
                                endPos);
        createSegment(location, references);

        log.log(Level.INFO, "construct segments end");
    }

    /**
     * Create a new Segment.
     *
     * @param location
     * @param references
     */
    private void createSegment(
        Location location,
        List<BedData> references
    ) {
        if (location.getLength() > 0) {
            int code = calcCode2(location, references);
            Segment segment = new Segment(location, code);
            segments.add(segment);
        } else {
            System.err.println("Segment invalid: " + location.toString());
        }
    }

    /**
     * Calculate code for current segment.
     *
     * @param loc location
     * @param references list of reference bed files
     * @return code
     */
    private int calcCode(
        Location loc,
        List<BedData> references
    ) {
        int base = 1;
        int code = 0;
        // calculate code over references
        for (int i = references.size() - 1; i >= 0; i--) {
            // either 0 or 1
            double cov = loc.coverage(references.get(i).overlappingWith(loc));
            if (cov > 0.6) {// all 1's go here
                code += base;
            }
            base *= 2;
        }
        return code;
    }

    /**
     * Calculate code for current segment.
     *
     * @param loc location
     * @param references list of reference bed files
     * @return code
     */
    private int calcCode2(
        Location loc,
        List<BedData> references
    ) {
        int code = 0;

        // calculate code over references
        for (int i = 0; i < references.size(); ++i) {
            // either 0 or 1
            double cov = loc.coverage(references.get(i).overlappingWith(loc));
            if (cov > 0.6) {// all 1's go here
                code += 1 << (references.size() - 1 - i);
            }
        }
        return code;
    }

    /**
     * Compute statistical information about segments
     * based on minimal segment length:
     * - segment count
     * - segment distribution
     * - total segment length
     * .
     *
     * Compute break segment analysis.
     *
     * Compute pairs of segments statistics.
     *
     * @param minLength minimal segment length (inclusive)
     *
     */
    private void compute(
        int minLength
    ) {
        divide(minLength);
        computeStatistics();
    }

    /**
     *
     * @param minLength minimal segment length (inclusive)
     *
     */
    private void divide(
        int minLength
    ) {
        log.log(Level.INFO, "divide");

        for (Segment segment : segments) {
            if (segment.getLength() <= 0) {
                System.err.println("Segment too short: " + segment.toString());
            } else {
                segment.setShortSegment(segment.getLength() < minLength);
            }
        }

        divided = true;

        log.log(Level.INFO, "divide end");

        // printSegmentation();
    }

    private void printSegmentation() {
        try (PrintWriter pr = new PrintWriter("/home/zeckzer/Forschung/Projekte/Leipzig/BioVis/PeakCalling/PeakData/Dirk-segment.masakari-2.data");
             BufferedWriter bw = new BufferedWriter(pr)) {
            for (Segment segment : segments) {
                if (segment.getLength() > 0) {
//                    if (!segment.isShortSegment()) {
                    bw.write(segment.getShortId()
                             + ";" + segment.getLongId()
                             + ";" + segment.getCode()
                             + ";" + segment.getLength()
                    );
                    bw.newLine();
//                    }
                }
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
    }

    /**
     * Compute statistical information about segments
     * based on minimal segment length:
     * - segment count
     * - segment distribution
     * - total segment length
     */
    private void computeStatistics() {
        log.log(Level.INFO, "compute statistics start");

        for (Segment segment : segments) {
            if (!segment.isShortSegment()) {
                ++segmentCount;
                totalSegmentLength += segment.getLength();
                addLength(segmentLengthDistribution, segment.getLength());
                if (segment.getCode() == 0) {
                    addLength(segmentUnmodifiedLengthDistribution, segment.getLength());
                } else {
                    addLength(segmentModifiedLengthDistribution, segment.getLength());
                }
            } else {
                ++shortSegmentCount;
                totalShortSegmentLength += segment.getLength();
                addLength(shortSegmentLengthDistribution, segment.getLength());
            }
        }

        log.log(Level.INFO, "count(segments/short segments):{0}/{1}", new Object[]{segmentCount, shortSegmentCount});
        log.log(Level.INFO, "length(segments/short segments):{0}/{1}", new Object[]{totalSegmentLength, totalShortSegmentLength});

        log.log(Level.INFO, "compute statistics end");
    }

    /**
     * Add element to length map.
     *
     * @param lengths trash length map
     * @param length length
     *
     */
    private void addLength(Map<Integer, Integer> lengths, int length) {
        if (lengths.containsKey(length)) {
            Integer newVal = lengths.get(length) + 1;
            lengths.replace(length, newVal);
        } else {
            lengths.put(length, 1);
        }
    }

    /**
     * Compute code counts for long segments.
     * Based on minimal segment length.
     *
     * @param minLength minimal segment length (inclusive)
     * @return code count map
     */
    public SortedMap<Integer, Integer> getCodeCounts(int minLength) {
        SortedMap<Integer, Integer> codeCounts = new TreeMap<>();
        for (Segment segment : segments) {
            if (segment.getLength() >= minLength) {
                Integer key = segment.getCode();
                if (codeCounts.containsKey(key)) {
                    codeCounts.replace(key, codeCounts.get(key) + 1);
                } else {
                    codeCounts.put(key, 1);
                }
            }
        }
        return codeCounts;
    }

    /**
     * Get length of the chromosome sequence.
     *
     * @param name
     *            of the chromosome
     * @return it's length
     */
    private Integer getSeqLength(String name) {
        if (seqLengths.containsKey(name)) {
            return seqLengths.get(name);
        }
        return 0;
    }

    /**
     * Get the start position of the chromosome sequence.
     *
     * @param name
     *            of the chromosome
     * @return it's start position
     */
    private Integer getSeqStart(String name) {
        if (seqStart.containsKey(name)) {
            return seqStart.get(name);
        }
        return 0;
    }

    /**
     * Compute segmentation from the reference bed files
     *
     * @param minLength
     * @return
     */
    public boolean doSegmentation(int minLength) {
        log.info("Start segmentation");

        bedData = loadBedFiles(refFiles);
        constructSegments(bedData);

        //computeStatistics(minLength);
        compute(minLength);

        log.info("End segmentation");

        return true;

    }

    public void setSeqLengths(Map<String, Integer> seqLengths) {
        this.seqLengths = seqLengths;
    }

    public void setSeqStart(Map<String, Integer> seqStart) {
        this.seqStart = seqStart;
    }

    public void setRefFiles(List<DataFile> list) {
        this.refFiles = list;
    }

    public boolean isDivided() {
        return divided;
    }

    public List<Segment> getSegments() {
        return this.segments;
    }

    public SortedMap<Integer, Integer> getShortSegmentsLengths() {
        return shortSegmentLengthDistribution;
    }

    public long getSegmentCount() {
        return segmentCount;
    }

    public long getTotalSegmentLength() {
        return totalSegmentLength;
    }

    public SortedMap<Integer, Integer> getSegmentLengths() {
        return segmentLengthDistribution;
    }

    public SortedMap<Integer, Integer> getSegmentUnmodifiedLengths() {
        return segmentUnmodifiedLengthDistribution;
    }

    public SortedMap<Integer, Integer> getSegmentModifiedLengths() {
        return segmentModifiedLengthDistribution;
    }

    public long getShortSegmentCount() {
        return shortSegmentCount;
    }

    public long getTotalShortSegmentLength() {
        return totalShortSegmentLength;
    }

    /**
     * Calculate code for current segment.
     *
     * @param queryLocation location
     * @return code
     */
    public Map<String, List<Location>> getDroppedPeaks(
        Location queryLocation
    ) {
        Map<String, List<Location>> results = new HashMap<>();
        // calculate code over references
        for (BedData bedFile : bedData) {
            List<Location> locations = bedFile.overlappingWith(queryLocation);
            if (locations != null) {
                for (Location location : locations) {
                    if (queryLocation.isContained(location)) {
//                        System.out.println("|" + bedFile.getName());
                        List<Location> values = results.get(bedFile.getName());
                        if (values == null) {
                            values = new ArrayList<>();
                            results.put(bedFile.getName(), values);
                        }
                        values.add(location);
                    }
                }
            }
        }
        return results;
    }
}
