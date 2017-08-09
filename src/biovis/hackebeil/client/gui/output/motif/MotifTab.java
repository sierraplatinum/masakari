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
package biovis.hackebeil.client.gui.output.motif;

import biovis.hackebeil.client.gui.output.common.ColumnCountInterface;
import biovis.hackebeil.client.gui.output.common.ColumnCountTab;
import biovis.hackebeil.common.data.MotifBase;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;

/**
 * @author zeckzer
 */
public class MotifTab
    extends ColumnCountTab
    implements ColumnCountInterface {

    private MotifBase motif;
    private Map<String, List<Double>> motifValues = null;

    /**
     *
     * @param motif
     * @param motifValues
     */
    public MotifTab(
        MotifBase motif,
        Map<String, List<Double>> motifValues
    ) {
        super(motif.getMotifId());

        this.motif = motif;
        this.motifValues = motifValues;

        Platform.runLater(() -> {
            controller.setCallback(this);
            if (motif.getIsNormalized()) {
                controller.setVisibleElements(false, true, false, false);
            } else {
                controller.setVisibleElements(true, true, true, true);
            }
            this.redraw();
        });
    }

    @Override
    public void redraw() {
        Platform.runLater(() -> {
            if (motif.getIsNormalized()) {
                controller.redrawNormalizedValues(motifValues.get(motif.getMotifId()));
            } else {
                controller.redrawCounts(motifValues.get(motif.getMotifId()));
            }
        });
    }
}
