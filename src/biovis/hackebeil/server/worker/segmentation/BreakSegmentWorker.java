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
import java.util.logging.Level;
import java.util.logging.Logger;

import biovis.hackebeil.common.data.BreakSegment;
import biovis.hackebeil.common.data.Segment;
import java.util.List;

/**
 * Based on epiwgseg.segmentation.TableBuilder.java and
 * epiwgseg.segmentation.DataTable.java
 *
 * @author nhinzmann, Alrik Hausdorf, Dirk Zeckzer, Lydia Mueller
 *
 */
public class BreakSegmentWorker {

    transient private Logger log = Logger.getLogger(BreakSegmentWorker.class.toString());

    // Break segment list
    transient private List<BreakSegment> breakSegmentList = new ArrayList<>();

    private SegmentationWorker segmentationWorker;

    /**
     * Constructor.
     *
     * @param segmentationWorker
     */
    public BreakSegmentWorker(SegmentationWorker segmentationWorker) {
        this.segmentationWorker = segmentationWorker;
        compute();
    }

    /**
     * Compute break segment analysis.
     *
     */
    private void compute() {
        if (segmentationWorker.isDivided()) {
            computeBreakSegments(segmentationWorker.getSegments());
        }
    }

    /**
     * Compute break segment analysis.
     *
     * Compute pairs of segments statistics.
     *
     * @param segments segments
     *
     */
    private void computeBreakSegments(
        List<Segment> segments
    ) {
        log.log(Level.INFO, "compute break segments start");

        // compute information to transfer to server
        breakSegmentList.clear();

        Segment lastSegment = null;
        BreakSegment breakSegment = null;
        for (Segment segment : segments) {
            if (!segment.isShortSegment()) {
                if (breakSegment != null
                    && segment.getLocation().getChr().equals(breakSegment.getLoc().getChr())) {
                    /*
                    System.out.println("S-B-S: "
                    + breakSegment.getBeforeCode()
                    + "-" + breakSegment.getBreakCode()
                    + "-" + segment.getCode());
                     */
                    if (segment.getCode() == breakSegment.getBeforeCode()) {
                        breakSegment.setAfterCode(segment.getCode());
                        breakSegmentList.add(breakSegment);
                    }
                }
                breakSegment = null;
                lastSegment = segment;
            } else {
                if (lastSegment == null) {
                    breakSegment = null;
                } else {
                    breakSegment = new BreakSegment(segment.getLocation(),
                                                    segment.getCode(),
                                                    lastSegment.getCode());
                }
                lastSegment = null;
            }
        }

        log.log(Level.INFO, "BreakSegments Count: {0}", breakSegmentList.size());

        log.log(Level.INFO, "compute break segments end");
    }

    public List<BreakSegment> getBreakSegmentList() {
        return breakSegmentList;
    }
}
