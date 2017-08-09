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
package biovis.hackebeil.client.gui.output.segmentation;

import biovis.hackebeil.client.gui.output.common.ColumnCountTab;
import biovis.hackebeil.client.gui.output.common.ColumnCountInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;

/**
 * @author zeckzer
 */
public class CodeDistributionTab
    extends ColumnCountTab
    implements ColumnCountInterface {

    private SortedMap<Integer, Integer> codeDistribution = null;

    /**
     *
     * @param title
     */
    public CodeDistributionTab(
        String title
    ) {
        super(title);

        Platform.runLater(() -> {
            controller.setCallback(this);
            controller.setVisibleElements(false, true, false, false);
            this.redraw();
        });
    }

    /**
     * Update the information on the code distribution tab.
     * @param codeDistribution
     */
    public void updateCodeDistribution(
        SortedMap<Integer, Integer> codeDistribution
    ) {
        if (controller == null) {
            return;
        }
        this.codeDistribution = codeDistribution;
        redraw();
    }

    /**
     * Redraw.
     */
    @Override
    public void redraw() {
        if (codeDistribution == null
            || codeDistribution.isEmpty()) {
            return;
        }

        Platform.runLater(() -> {
            List<XYChart.Data<String, Integer>> chartData = new ArrayList<>();
            Set<Integer> keys = codeDistribution.keySet();
            for (Integer testElement : keys) {
                chartData.add(new XYChart.Data<>("" + testElement, codeDistribution.get(testElement)));
            }
            controller.createChart(chartData);
        });
    }
}
