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
package biovis.hackebeil.server.data;

import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.common.data.Motif;
import biovis.hackebeil.common.data.PositionWeightMatrix;
import biovis.hackebeil.common.data.Segment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nhinzmann, Dirk Zeckzer
 *
 * Container for all server relevant data, mainly:
 * - file names
 * - results computed by workers
 */
public class ServerCache {

    private static final int MIN_SEGMENT_LENGTH = 200;

    // Genome and genome index paths
    private String filePathToRefGenome;
    private String filePathToIndex;

    // Genome information maps
    private Map<String, Integer> seqLengths;
    private Map<String, Integer> seqStart;
    private Map<String, Long> startFilePos;
    private Map<String, Integer> lineLength;

    // genome reader, stays open!
    private BufferedRandomAccessFile genomeReader;

    // Reference data sets and segmentation
    transient private List<DataFile> referenceList;
    private List<Segment> allSegments;
    private int minSegmentLength = MIN_SEGMENT_LENGTH;

    // Additional data sets
    transient private List<DataFile> dfList;
    private Map<String, List<Double>> additionalDataValues;

    // Motifs
    transient private List<Motif> motifList;
    private Map<String, List<Double>> motifValues;

    // PWMs
    transient private List<PositionWeightMatrix> pwmList;
    private Map<String, List<Double>> pwmValues;

    // Number of threads
    private int numberOfThreads = 6;

    /**
     *
     */
    public ServerCache() {
        seqLengths = new HashMap<>();
        seqStart = new HashMap<>();
        startFilePos = new HashMap<>();
        lineLength = new HashMap<>();

        genomeReader = null;
    }

    /**
     * Clear data.
     *
     */
    public void clear() {
        seqLengths.clear();
        seqStart.clear();
        startFilePos.clear();
        lineLength.clear();

        if (genomeReader != null) {
            try {
                genomeReader.close();
            } catch (IOException ioEx) {
                //
            }
        }

        referenceList = null;
        allSegments = null;
        dfList = null;
        additionalDataValues = null;
        motifList = null;
        motifValues = null;
        pwmList = null;
        pwmValues = null;

        minSegmentLength = MIN_SEGMENT_LENGTH;
    }

    /**
     * @return the seqLength
     */
    public Map<String, Integer> getSeqLengths() {
        return seqLengths;
    }

    /**
     * @param seqLengths
     *            the seqLength to set
     */
    public void setSeqLengths(Map<String, Integer> seqLengths) {
        // System.err.println(seqLength);
        this.seqLengths = seqLengths;
    }

    /**
     * @return the seqLength
     */
    public Map<String, Integer> getSeqStart() {
        return seqStart;
    }

    /**
     * @param seqStart
     *            the seqStart to set
     */
    public void setSeqStart(Map<String, Integer> seqStart) {
        // System.err.println(seqStart);
        this.seqStart = seqStart;
    }

    public List<DataFile> getReferenceList() {
        return referenceList;
    }

    public int getNumberOfRefererences() {
        return referenceList.size();
    }

    public void setReferenceList(List<DataFile> referenceList) {
        this.referenceList = referenceList;
    }

    /**
     * @param segments
     */
    public void setSegments(List<Segment> segments) {
        this.allSegments = segments;

    }

    public List<Segment> getAllSegments() {
        return this.allSegments;
    }

    public List<Segment> getSegments() {
        List<Segment> segments = new ArrayList<>();
        for (Segment segment : allSegments) {
            if (segment.getLength() >= minSegmentLength) {
                segments.add(segment);
            }
        }
        return segments;
    }

    public List<Segment> getShortSegments() {
        List<Segment> segments = new ArrayList<>();
        for (Segment segment : allSegments) {
            if (segment.getLength() < minSegmentLength) {
                segments.add(segment);
            }
        }
        return segments;
    }

    public int getMinSegmentLength() {
        return minSegmentLength;
    }

    public void setMinSegmentLength(int minSegmentLength) {
        this.minSegmentLength = minSegmentLength;
    }

    public void setModifications(
        List<DataFile> dfList,
        Map<String, List<Double>> additionalDataValues
    ) {
        this.dfList = dfList;
        this.additionalDataValues = additionalDataValues;
    }

    public List<DataFile> getDfList() {
        return dfList;
    }

    public Map<String, List<Double>> getAdditionalDataValues() {
        return additionalDataValues;
    }

    public void setMotifs(
        List<Motif> motifList,
        Map<String, List<Double>> motifValues
    ) {
        this.motifList = motifList;
        this.motifValues = motifValues;
    }

    public List<Motif> getMotifList() {
        return motifList;
    }

    public Map<String, List<Double>> getMotifValues() {
        return motifValues;
    }

    public void setPWM(
        List<PositionWeightMatrix> pwmList,
        Map<String, List<Double>> pwmValues
    ) {
        this.pwmList = pwmList;
        this.pwmValues = pwmValues;
    }

    public List<PositionWeightMatrix> getPWMList() {
        return pwmList;
    }

    public Map<String, List<Double>> getPWMValues() {
        return pwmValues;
    }

    /**
     * @return the startFilePos
     */
    public Map<String, Long> getStartFilePos() {
        return startFilePos;
    }

    /**
     * @param startFilePos the startFilePos to set
     */
    public void setStartFilePos(Map<String, Long> startFilePos) {
        this.startFilePos = startFilePos;
    }

    /**
     * @return the lineLength
     */
    public Map<String, Integer> getLineLength() {
        return lineLength;
    }

    /**
     * @param lineLength the lineLength to set
     */
    public void setLineLength(Map<String, Integer> lineLength) {
        this.lineLength = lineLength;
    }

    /**
     * @return the bufferedReader
     */
    public BufferedRandomAccessFile getGenomeReader() {
        return genomeReader;
    }

    /**
     * @param genomeReader the bufferedReader to set
     */
    public void setGenomeReader(BufferedRandomAccessFile genomeReader) {
        this.genomeReader = genomeReader;
    }

    /**
     * @return the filePathToRefGenome
     */
    public String getFilePathToRefGenome() {
        return filePathToRefGenome;
    }

    /**
     * @param filePathToRefGenome the filePathToRefGenome to set
     */
    public void setFilePathToRefGenome(String filePathToRefGenome) {
        this.filePathToRefGenome = filePathToRefGenome;
    }

    /**
     * @return the filePathToIndex
     */
    public String getFilePathToIndex() {
        return filePathToIndex;
    }

    /**
     * @param filePathToIndex he filePathToIndex to set
     */
    public void setFilePathToIndex(String filePathToIndex) {
        this.filePathToIndex = filePathToIndex;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }
}
