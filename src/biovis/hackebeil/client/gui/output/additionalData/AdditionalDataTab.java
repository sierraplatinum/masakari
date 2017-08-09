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
package biovis.hackebeil.client.gui.output.additionalData;

import biovis.hackebeil.client.gui.output.common.ColumnCountInterface;
import biovis.hackebeil.client.gui.output.common.ColumnCountTab;
import biovis.hackebeil.common.data.DataFile;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;

/**
 * @author zeckzer
 */
public class AdditionalDataTab
    extends ColumnCountTab
    implements ColumnCountInterface {

    private DataFile additionalDataFile;
    private List<Double> additionalData = null;

    /**
     *
     * @param df
     * @param additionalData
     */
    public AdditionalDataTab(
        DataFile df,
        Map<String, List<Double>> additionalData
    ) {
        super(df.getDataSetName());

        this.additionalDataFile = df;
        this.additionalData = additionalData.get(additionalDataFile.getDataSetName());

        Platform.runLater(() -> {
            controller.setCallback(this);
            controller.setVisibleElements(false, true, true, false);
            this.redraw();
        });
    }

    @Override
    public void redraw() {
        Platform.runLater(() -> {
            if (additionalData != null && additionalDataFile != null) {
                controller.redrawNormalizedValues(additionalData);
            }
        });
    }

    @Override
    public String getLabel() {
        return title + "-values";
    }
}
