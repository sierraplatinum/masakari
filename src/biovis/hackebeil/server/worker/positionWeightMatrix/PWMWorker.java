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
package biovis.hackebeil.server.worker.positionWeightMatrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import biovis.hackebeil.common.data.Segment;
import biovis.hackebeil.server.data.ServerCache;
import biovis.hackebeil.server.data.BufferedRandomAccessFile;
import biovis.hackebeil.common.data.PositionWeightMatrix;
import biovis.hackebeil.server.worker.SequenceReader;
import biovislib.parallel4.Parallel2;
import biovislib.parallel4.ParallelForInt2;
import biovislib.parallel4.ParallelizationFactory;

/**
 * based on epiwgseg.segmentation.TableBuilder, epiwgseg.segmentation.DataTable
 *
 * @author nhinzmann, Alrik Hausdorf, Dirk Zeckzer
 *
 */
public class PWMWorker {

    private static final Logger log = Logger.getLogger("PWMWorker");

    private ServerCache cache;
    private String fileName;

    private List<Segment> segments;
    private SequenceReader sqr;

    private List<PositionWeightMatrix> pwmList;
    private Map<String, List<Double>> pwmValues;

    /**
     *
     * @param cache
     */
    public PWMWorker(
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
    public void compute() {
        log.log(Level.INFO, "PWM computation start");
        if (computePWM()) {
            log.log(Level.INFO, "PWM values");
            pwmValues = new HashMap<>();
            for (PositionWeightMatrix pwm : pwmList) {
                List<Double> valuesForPWM = new ArrayList<>();
                String pwmId = pwm.getMotifId();
                for (Segment segment : segments) {
                    if (segment.getLength() >= cache.getMinSegmentLength()) {
                        valuesForPWM.add(segment.getPWMValue(pwmId));
                    }
                }
                pwmValues.put(pwmId, valuesForPWM);
            }
        }
        log.log(Level.INFO, "PWM computation end");
    }

    /**
     *
     * @return
     */
    private boolean computePWM() {
        try (BufferedRandomAccessFile bufferedReader = new BufferedRandomAccessFile(fileName, "r", 1024 * 16)) {
            for (Segment segment : segments) {
                if (segment.getLength() <= 0) {
                    System.out.println("Segment too short: " + segment.getLocation().toString());
                }

                char[] segmentDNA = sqr.readSegment(segment, bufferedReader);
                if (segmentDNA.length <= 0) {
                    System.out.println("SegmentDNA too short: " + segment.getLocation().toString());
                }

                int threads = cache.getNumberOfThreads();
                Parallel2 p2 = ParallelizationFactory.getInstance(threads);
                int numberOfPWM = pwmList.size();
                new ParallelForInt2(p2, 0, numberOfPWM).loop((final int currentPWM) -> {
                    PositionWeightMatrix pwm = pwmList.get(currentPWM);
                    double pwmValue = pwm.computePWM(segmentDNA);

                    synchronized (segment) {
                        segment.addPWMData(pwm.getMotifId(), pwmValue);
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
     * @param pwmList
     */
    public void setPWMList(List<PositionWeightMatrix> pwmList) {
        this.pwmList = pwmList;
        for (PositionWeightMatrix pwm : pwmList) {
            pwm.loadPwm();
        }
    }

    /**
     *
     * @return PWM values
     */
    public Map<String, List<Double>> getPWMValues() {
        return pwmValues;
    }
}
