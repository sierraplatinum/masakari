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
package biovis.hackebeil.client.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Tooltip;

/**
 * Chart Helper Functions
 * @author nhinzmann, Alrik Hausdorf, Dirk Zeckzer
 *
 */
public class ChartUtilities {

    /**
     *
     * @param data
     * @return
     */
    public static Tooltip createTooltip(
        Data<String, Integer> data
    ) {
        String tooltipText = String.format(Locale.GERMAN, "f(%s)=%,d", data.getXValue(), data.getYValue());
        return new Tooltip(tooltipText);
    }

    /**
     *
     * @param from
     * @param to
     * @param numberOfBins
     * @param useLogBins
     * @return
     */
    public static long[] createBins(
        long from,
        long to,
        int numberOfBins,
        boolean useLogBins
    ) {
        long[] blockstarts;
        if (useLogBins) {
            return createLogBins(from, to, numberOfBins);
        } else {
            return createBins(from, to, numberOfBins);
        }
    }

    /**
     *
     * @param from
     * @param to
     * @param numberOfBins
     * @return
     */
    public static long[] createLogBins(
        long from,
        long to,
        int numberOfBins
    ) {
        long[] blockstarts;

        boolean fromNull = false;
        if (from == 0) {
            fromNull = true;
            from++;
        }

        // Adjust min and max value
        double lowerMagnitude = Math.floor(Math.log10(from));
        double upperMagnitude = Math.ceil(Math.log10(to));
        if (lowerMagnitude < 0.0) {
            lowerMagnitude = 0.0;
        }
        from = (long) Math.pow(10, lowerMagnitude);
        //to = (long) Math.pow(10, upperMagnitude);
        int diffMagnitude = (int) (upperMagnitude - lowerMagnitude);

        numberOfBins = (numberOfBins / diffMagnitude) * diffMagnitude;
        System.out.println(numberOfBins);
        blockstarts = new long[numberOfBins];

        double logDist = ((double) diffMagnitude) / ((double) numberOfBins - 1);
        double lastElementsStart = fromNull ? 0 : lowerMagnitude;
        for (int i = 0; i < numberOfBins; i++) {
            if (i == 0) {
                blockstarts[i] = fromNull ? (from - 1) : from;
            } else {
                lastElementsStart += logDist;
                blockstarts[i] = (long) Math.ceil(Math.pow(10, lastElementsStart));
            }
        }
        /*
        for (int i = 0; i < blockstarts.length - 1; i++) {
            System.out.println("block from " + Math.log(blockstarts[i])
                               + " to: " + Math.log(blockstarts[i + 1])
                               + " diff: " + (Math.log(blockstarts[i + 1]) - Math.log(blockstarts[i])));
        }
        */

        return blockstarts;
    }

    public static long[] createBins(
        long from,
        long to,
        int numberOfBins
    ) {
        long[] blockstarts = new long[numberOfBins];
        int blockLength = (int) Math.ceil((float) (to - from) / (numberOfBins * 1.0));
        for (int i = 0; i < numberOfBins; i++) {
            blockstarts[i] = from + i * blockLength;
        }
        return blockstarts;
    }

    /**
     *
     * @param data
     * @param from
     * @param to
     * @return
     */
    public static List<Data<String, Integer>> createChartDataWithoutBins(
        SortedMap<Integer, Integer> data,
        long from,
        long to
    ) {
        SortedMap<Integer, Integer> filteredData = new TreeMap<>();
        for (Entry<Integer, Integer> entry : data.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            if (key >= from && key <= to) {
                filteredData.put(key, value);
            }

        }
        // System.out.println("filteredDataLength:"+filteredData.size());
        List<Data<String, Integer>> resultList = new ArrayList<>();

        Set<Integer> keys1 = filteredData.keySet();
        java.util.Iterator<Integer> iterator1 = keys1.iterator();
        Integer testElement1;
        while (iterator1.hasNext()) {
            testElement1 = iterator1.next();
            resultList.add(new XYChart.Data<>("" + testElement1, filteredData.get(testElement1)));
        }
        return resultList;
    }

    /**
     *
     * @param data
     * @param blockstarts
     * @param from
     * @param to
     * @return
     */
    public static List<Data<String, Integer>> createChartDataforBins(
        SortedMap<Integer, Integer> data,
        long[] blockstarts,
        long from,
        long to
    ) {
        // System.out.println("createChartDataforBins called");
        return createChartDataforBinsWithCorrection(data, blockstarts, 1, from, to);
    }

    /**
     *
     * @param data
     * @param blockstarts
     * @param correction
     * @param from
     * @param to
     * @return
     */
    public static List<Data<String, Integer>> createChartDataforBinsWithCorrection(
        SortedMap<Integer, Integer> data,
        long[] blockstarts,
        int correction,
        long from,
        long to
    ) {
        // System.out.println("createChartDataforBinsWithCorrection called");
        // System.out.println("dataLength:"+data.size());
        SortedMap<Integer, Integer> filteredData = new TreeMap<>();
        for (Entry<Integer, Integer> entry : data.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            if (key >= from && key <= to) {
                filteredData.put(key, value);
            }

        }
        // System.out.println("filteredDataLength:"+filteredData.size());
        List<Data<String, Integer>> resultList = new ArrayList<>();
        Set<Integer> keys = filteredData.keySet();
        java.util.Iterator<Integer> iterator = keys.iterator();
        if (!iterator.hasNext()) {
            return resultList;
        }
        int testElement = iterator.next();

        for (int i = 0; i < blockstarts.length; i++) {

            long startElement = blockstarts[i];
            long lastElement = to;
            if (i < blockstarts.length - 1) {
                lastElement = blockstarts[i + 1] - 1;
            }
            // System.out.println("i:" + i + " startElement:" + startElement + "
            // lastElement:" + lastElement);
            int blocksum = 0;
            while (iterator.hasNext() && testElement >= startElement && testElement <= lastElement) {
                // System.out.println("next:" + testElement + "=" +
                // filteredData.get(testElement));
                blocksum += filteredData.get(testElement);
                testElement = iterator.next();
            }
            if (!iterator.hasNext() && testElement >= startElement && testElement <= lastElement) {
                blocksum += filteredData.get(testElement);
            }
            String key = "";
            if (correction != 1) {
                if (startElement == lastElement) {
                    key = ((float) lastElement / (correction)) + "";
                } else {
                    key = (((float) startElement / (correction)) + "-" + ((float) lastElement / (correction)));
                }
            } else if (startElement == lastElement) {
                key = (lastElement) + "";
            } else {
                key = ((startElement) + "-" + (lastElement));
            }
            // System.out.println(i + " - blocksum" + blocksum);
            if (startElement <= lastElement) {
                resultList.add(new XYChart.Data<>(key, blocksum));

            }
        }
        return resultList;
    }
}
