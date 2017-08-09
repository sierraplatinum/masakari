/**
 * *****************************************************************************
 * Copyright 2015 Dirk Zeckzer, Lydia Müller, Daniel Gerighausen
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ****************************************************************************
 */
package biovis.hackebeil.server.worker;

import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.common.data.Segment;
import biovis.hackebeil.server.data.ServerCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author zeckzer
 */
public class FateOfCodeWorker {

    private ServerCache cache;
    private List<int[][]> results;

    /**
     *
     * @param cache
     */
    public FateOfCodeWorker(
        ServerCache cache
    ) {
        this.cache = cache;
    }

    /**
     *
     * @param selection
     * @param threshold
     * @return 
     */
    public boolean compute(
        HashMap<Integer, DataFile[]> selection,
        double threshold
    ) {
        int numberOfReferences = cache.getNumberOfRefererences();
        int numberOfCodes = (int) Math.round(Math.exp(Math.log(2) * numberOfReferences));
        /*
        System.out.println("references - codes "
                           + Math.exp(Math.log(2) * numberOfReferences)
                           + " " + numberOfReferences
                           + " " + numberOfCodes);

        */
        for (DataFile[] col : selection.values()) {
            for (DataFile df : col) {
                if (df == null) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error Dialog");
                        alert.setHeaderText("Data not valid");
                        alert.setContentText("One Reference is not set.");
                        alert.showAndWait();
                    });
                    return false;
                }
            }
        }

        results = new ArrayList<>();
        for (int i = 0; i < selection.size(); i++) {
            results.add(new int[numberOfCodes][numberOfCodes]);
        }

        for (Segment row : cache.getSegments()) {
            int fromCode = row.getCode();
            int newCode = 0;

            int resultNumber = 0;
            for (DataFile[] col : selection.values()) {
                char[] binString = new char[numberOfReferences];
                for (int j = 0; j < numberOfReferences; j++) {
                    if (row.getAdditionalDataValue(col[j].getDataSetName()) >= threshold) {
                        binString[j] = '1';
                    } else {
                        binString[j] = '0';
                    }
                }
                newCode = Integer.parseInt(String.valueOf(binString), 2);
                results.get(resultNumber)[fromCode][newCode]++;
                resultNumber++;
            }
        }

        return true;
    }

    /**
     *
     * @return
     */
    public List<int[][]> getResults() {
        return results;
    }
}
