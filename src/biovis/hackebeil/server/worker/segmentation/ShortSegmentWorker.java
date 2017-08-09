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

import biovis.hackebeil.common.data.Location;
import java.util.logging.Level;
import java.util.logging.Logger;

import biovis.hackebeil.common.data.Segment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Based on epiwgseg.segmentation.TableBuilder.java and
 * epiwgseg.segmentation.DataTable.java
 *
 * @author nhinzmann, Alrik Hausdorf, Dirk Zeckzer, Lydia Mueller
 *
 */
public class ShortSegmentWorker {

    transient private Logger log = Logger.getLogger(ShortSegmentWorker.class.toString());

    // Segment pairs
    private SortedMap<Integer, Integer> shortSegmentChainsCounts = new TreeMap<>();
    private SortedMap<Integer, Integer> shortSegmentChainsLengths = new TreeMap<>();

    private Map<String, List<Location>> droppedPeaks = new HashMap<>();

    transient private SegmentationWorker segmentationWorker;

    /**
     * Constructor.
     *
     * @param segmentationWorker
     */
    public ShortSegmentWorker(
        SegmentationWorker segmentationWorker
    ) {
        this.segmentationWorker = segmentationWorker;
        compute();
    }

    /**
     * Compute pairs of segments statistics.
     *
     *
     */
    private void compute() {
        computeShortSegmentChains(segmentationWorker.getSegments());
        computeDroppedPeaks(segmentationWorker.getSegments());
    }

    /**
     * Compute pairs of segments statistics.
     *
     * @param segments segments
     *
     */
    private void computeShortSegmentChains(
        List<Segment> segments
    ) {
        log.log(Level.INFO, "compute short segment chains start");

        // compute information to transfer to server
        shortSegmentChainsCounts.clear();
        shortSegmentChainsLengths.clear();

        Integer count = 0;
        Integer countValue = 0;
        Integer length = 0;
        Integer lengthValue = 0;
        for (Segment segment : segments) {
            if (!segment.isShortSegment()) {
                if (count != 0) {
                    countValue = shortSegmentChainsCounts.get(count);
                    if (countValue == null) {
                        shortSegmentChainsCounts.put(count, 1);
                    } else {
                        shortSegmentChainsCounts.put(count, countValue + 1);
                    }
                }
                count = 0;

                if (length != 0) {
                    lengthValue = shortSegmentChainsLengths.get(length);
                    if (lengthValue == null) {
                        shortSegmentChainsLengths.put(length, 1);
                    } else {
                        shortSegmentChainsLengths.put(length, lengthValue + 1);
                    }
                }
                length = 0;
            } else {
                ++count;
                length += segment.getLength();
            }
        }

        log.log(Level.INFO, "compute short segment chains start");
    }

    /**
     * Compute pairs of segments statistics.
     *
     * @param segments segments
     *
     */
    private void computeDroppedPeaks(
        List<Segment> segments
    ) {
        log.log(Level.INFO, "compute dropped peaks start");

        // compute information to transfer to server
        droppedPeaks.clear();

        Location droppedLocation = null;
        for (Segment segment : segments) {
            if (!segment.isShortSegment()) {
                if (droppedLocation != null) {
                    Map<String, List<Location>> newDroppedPeaks = segmentationWorker.getDroppedPeaks(droppedLocation);
                    for (String key : newDroppedPeaks.keySet()) {
                        List<Location> value = droppedPeaks.get(key);
                        if (value == null) {
                            droppedPeaks.put(key, newDroppedPeaks.get(key));
                        } else {
                            droppedPeaks.get(key).addAll(newDroppedPeaks.get(key));
                        }
                    }
                }
                droppedLocation = null;
            } else {
                if (droppedLocation == null) {
                    droppedLocation = new Location(segment.getLocation());
                } else {
                    droppedLocation.setEnd(segment.getLocation().getEnd());
                }
            }
        }

        log.log(Level.INFO, "compute dropped peaks start");
    }

    public SortedMap<Integer, Integer> getShortSegmentChainsCounts() {
        return shortSegmentChainsCounts;
    }

    public SortedMap<Integer, Integer> getShortSegmentChainsLengths() {
        return shortSegmentChainsLengths;
    }

    public int getNumberOfDroppedPeaks() {
        int numberOfDroppedPeaks = 0;

        for (String key : droppedPeaks.keySet()) {
            numberOfDroppedPeaks += droppedPeaks.get(key).size();
        }

        return numberOfDroppedPeaks;
    }
}
