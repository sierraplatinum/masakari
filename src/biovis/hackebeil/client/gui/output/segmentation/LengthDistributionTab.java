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
import java.util.SortedMap;
import javafx.application.Platform;

/**
 * @author zeckzer
 */
public class LengthDistributionTab
    extends ColumnCountTab
    implements ColumnCountInterface {

    private SortedMap<Integer, Integer> lengthDistribution = null;

    /**
     * Create tab for code count distribution.
     *
     * @param title
     */
    public LengthDistributionTab(
        String title
    ) {
        super(title);

        Platform.runLater(() -> {
            controller.setCallback(this);
            controller.setVisibleElements(true, true, false, true);
            this.redraw();
        });
    }

    /**
     * Update the information on the code distribution tab.
     * @param lengthDistribution
     */
    public void updateLengthDistribution(
        SortedMap<Integer, Integer> lengthDistribution
    ) {
        if (controller == null) {
            return;
        }
        this.lengthDistribution = lengthDistribution;
        redraw();
    }

    @Override
    public void redraw() {
        if (lengthDistribution == null) {
            return;
        }

        Platform.runLater(() -> {
            controller.redrawCounts(lengthDistribution);
        });
    }
}
