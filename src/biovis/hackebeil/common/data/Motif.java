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
package biovis.hackebeil.common.data;

/**
 * @author zeckzer, nhinzmann
 *
 */
public class Motif
    extends MotifBase {

    private String motif;

    public Motif() {
        super();
    }

    public void setMotif(String motif) {
        this.motif = motif;
        motifId = motif;

    }

    public String getMotif() {
        return this.motif;
    }

    @Override
    public String getMotifId() {
        if (motifId == null) {
            motifId = motif;
        }
        return motifId + "-" + (isNormalizedValue ? "Density" : "Count");
    }
}
