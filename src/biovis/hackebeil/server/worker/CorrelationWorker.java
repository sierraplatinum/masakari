/**
 *  Copyright 2016 Alrik Hausdorf, Nicole Hinzmann, Dirk Zeckzer
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
package biovis.hackebeil.server.worker;

import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.common.data.MyCorrelationList;
import biovis.hackebeil.common.data.Motif;
import biovis.hackebeil.common.data.PositionWeightMatrix;
import biovis.hackebeil.common.data.Segment;
import biovis.hackebeil.server.data.ServerCache;
import biovislib.parallel4.Parallel2;
import biovislib.statistics.correlation.SpearmanCorrelationParallelFast;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alrik Hausdorf, Dirk Zeckzer
 */
public class CorrelationWorker {

    private List<double[]> baseData = new ArrayList<>();
    private SpearmanCorrelationParallelFast correlationFast;
    private double[][] resultTable;

    public CorrelationWorker(
        ServerCache cache
    ) {
        prepareData(cache);
        Parallel2 p2 = new Parallel2(cache.getNumberOfThreads());
        this.correlationFast = new SpearmanCorrelationParallelFast(baseData.get(0).length, p2);
    }

    public boolean compute() {
        MyCorrelationList cl = new MyCorrelationList(baseData);
        this.resultTable = this.correlationFast.calculateCorrelations(cl);
        return true;
    }

    /**
     * Returns a list (with length of of number of Segmentations) of Lists with
     * [0,1] values
     *
     * @return
     */
    private void prepareData(
        ServerCache cache
    ) {
        baseData.clear();
        int lines = cache.getSegments().size();

        List<DataFile> referenceList = cache.getReferenceList();
        List<DataFile> additionalDataList = cache.getDfList();
        List<Motif> motifList = cache.getMotifList();
        List<PositionWeightMatrix> pwmList = cache.getPWMList();

        int numberOfReferenceData = referenceList.size();
        int colSize = numberOfReferenceData + 1;
        if (additionalDataList != null) {
            colSize += additionalDataList.size();
        }
        if (motifList != null) {
            colSize += motifList.size();
        }
        if (pwmList != null) {
            colSize += pwmList.size();
        }

        double[] values;
        int currentAttribute;
        Segment segment;

        List<Segment> segments = cache.getSegments();
        for (int i = 0; i < lines; i++) {
            segment = segments.get(i);

            // value array for this line
            values = new double[colSize];

            currentAttribute = 0;

            // Code
            values[currentAttribute] = segment.getCode();
            currentAttribute++;

            // Reference data
            int[] codeColumns = this.getBytesByInt(segment.getCode(), numberOfReferenceData);
            for (int currentCodeColumn = 0;
                 currentCodeColumn < numberOfReferenceData;
                 ++currentCodeColumn) {
                values[currentAttribute] = codeColumns[currentCodeColumn];
                currentAttribute++;
            }

            // Additional data
            if (additionalDataList != null) {
                for (DataFile df : additionalDataList) {
                    values[currentAttribute] = segment.getAdditionalData().get(df.getDataSetName());
                    currentAttribute++;
                }
            }

            // Motifs
            if (motifList != null) {
                for (Motif motif : motifList) {
                    values[currentAttribute] = segment.getMotifValue(motif.getMotifId());
                    currentAttribute++;
                }
            }

            // Position Weight Matrices
            if (pwmList != null) {
                for (PositionWeightMatrix pwm : pwmList) {
                    values[currentAttribute] = segment.getPWMValue(pwm.getMotifId());
                    currentAttribute++;
                }
            }

            // add attribute values for current line
            baseData.add(values);
        }
    }

    private int[] getBytesByInt(int input, int length) {
        int[] bits = new int[length];
        for (int i = bits.length - 1; i >= 0; i--) {
            bits[bits.length - 1 - i] = ((input & (1 << i)) != 0) ? 1 : 0;
        }

        return bits;
    }

    public double[][] getResultTable() {
        return resultTable;
    }
}
