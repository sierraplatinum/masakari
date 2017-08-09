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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import biovis.hackebeil.common.data.Segment;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;

/**
 * Based on epiwgseg.segmentation.TableBuilder.java and
 * epiwgseg.segmentation.DataTable.java
 *
 * @author nhinzmann, Alrik Hausdorf, Dirk Zeckzer, Lydia Mueller
 *
 */
public class SegmentPairWorker {

    transient private Logger log = Logger.getLogger(SegmentPairWorker.class.toString());

    // Segment pairs
    private Map<String, Integer> segmentPairMap = new HashMap<>();
    private Map<String, Integer> segmentShortSegmentPairMap = new HashMap<>();
    private Map<String, Integer> shortSegmentSegmentPairMap = new HashMap<>();

    private Map<String, Double> segmentPairRelationMap = new HashMap<>();
    private SortedMap<Integer, Integer> segmentCodeMap;

    transient private SegmentationWorker segmentationWorker;

    /**
     * Constructor.
     *
     * @param segmentationWorker
     * @param minLength
     */
    public SegmentPairWorker(
        SegmentationWorker segmentationWorker,
        int minLength
    ) {
        this.segmentationWorker = segmentationWorker;
        compute(minLength);
    }

    /**
     * Compute pairs of segments statistics.
     *
     * @param minLength minimal segment length (inclusive)
     *
     */
    private void compute(
        int minLength
    ) {
        computeSegmentPairs(segmentationWorker.getSegments());
        computeSegmentShortSegmentPairs(segmentationWorker.getSegments());
        computeShortSegmentSegmentPairs(segmentationWorker.getSegments());

        segmentCodeMap = segmentationWorker.getCodeCounts(minLength);
        computeSegmentPairRelation();
    }

    /**
     * Compute pairs of segments statistics.
     *
     * @param segments segments
     *
     */
    private void computeSegmentPairs(
        List<Segment> segments
    ) {
        log.log(Level.INFO, "compute segment pairs start");

        // compute information to transfer to server
        segmentPairMap.clear();

        Segment lastSegment = null;
        String key = null;
        Integer value = 0;
        for (Segment segment : segments) {
            if (!segment.isShortSegment()) {
                if ((lastSegment != null)
                    && segment.getLocation().getChr().equals(lastSegment.getLocation().getChr())) {
                    key = lastSegment.getCode() + "-" + segment.getCode();
                    value = segmentPairMap.get(key);
                    if (value == null) {
                        segmentPairMap.put(key, 1);
                    } else {
                        segmentPairMap.put(key, value + 1);
                    }
                }
                lastSegment = segment;
            } else {
                lastSegment = null;
            }
        }

        log.log(Level.INFO, "compute segment pairs end");
    }

    /**
     * Compute pairs of segments statistics.
     *
     * @param segments segments
     *
     */
    private void computeSegmentShortSegmentPairs(
        List<Segment> segments
    ) {
        log.log(Level.INFO, "compute segment-short segment pairs start");

        // compute information to transfer to server
        segmentShortSegmentPairMap.clear();

        Segment lastSegment = null;
        String key = null;
        Integer value = 0;
        for (Segment segment : segments) {
            if (!segment.isShortSegment()) {
                lastSegment = segment;
            } else {
                if ((lastSegment != null)
                    && segment.getLocation().getChr().equals(lastSegment.getLocation().getChr())) {
                    key = lastSegment.getCode() + "-" + segment.getCode();
                    value = segmentShortSegmentPairMap.get(key);
                    if (value == null) {
                        segmentShortSegmentPairMap.put(key, 1);
                    } else {
                        segmentShortSegmentPairMap.put(key, value + 1);
                    }
                }
                lastSegment = null;
            }
        }

        log.log(Level.INFO, "compute segment-short segment pairs end");
    }

    /**
     * Compute pairs of segments statistics.
     *
     * @param segments segments
     *
     */
    private void computeShortSegmentSegmentPairs(
        List<Segment> segments
    ) {
        log.log(Level.INFO, "compute segment pairs start");

        // compute information to transfer to server
        shortSegmentSegmentPairMap.clear();

        Segment lastSegment = null;
        String key = null;
        Integer value = 0;
        for (Segment segment : segments) {
            if (!segment.isShortSegment()) {
                if ((lastSegment != null)
                    && segment.getLocation().getChr().equals(lastSegment.getLocation().getChr())) {
                    key = lastSegment.getCode() + "-" + segment.getCode();
                    value = shortSegmentSegmentPairMap.get(key);
                    if (value == null) {
                        shortSegmentSegmentPairMap.put(key, 1);
                    } else {
                        shortSegmentSegmentPairMap.put(key, value + 1);
                    }
                }
                lastSegment = null;
            } else {
                lastSegment = segment;
            }
        }

        log.log(Level.INFO, "compute segment pairs end");
    }

    /**
     * Compute pairs of segments statistics.
     *
     */
    private void computeSegmentPairRelation() {
        log.log(Level.INFO, "compute segment pair relation start");

        // compute information to transfer to server
        segmentPairRelationMap.clear();

        long total = 0;
        for (Integer count : segmentCodeMap.values()) {
            total += count;
        }

        String key = null;
        Integer value = 0;
        for (Integer i : segmentCodeMap.keySet()) {
            for (Integer j : segmentCodeMap.keySet()) {
                key = i + "-" + j;
                value = segmentPairMap.get(key);
                if (value != null) {
                    double observed = value / ((double) total);
                    long pairs  = segmentCodeMap.get(i) * segmentCodeMap.get(j);
                    double expected = pairs / (total * total * 2.0);
                    segmentPairRelationMap.put(key, observed / expected);
                }
            }
        }

        log.log(Level.INFO, "compute segment pair relation end");
    }

    public Map<String, Integer> getSegmentPairMap() {
        return segmentPairMap;
    }

    public Map<String, Integer> getSegmentShortSegmentPairMap() {
        return segmentShortSegmentPairMap;
    }

    public Map<String, Integer> getShortSegmentSegmentPairMap() {
        return shortSegmentSegmentPairMap;
    }

    public Map<String, Double> getSegmentPairRelationMap() {
        return segmentPairRelationMap;
    }
}
