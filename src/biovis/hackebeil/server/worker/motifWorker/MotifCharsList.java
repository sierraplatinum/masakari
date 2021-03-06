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
package biovis.hackebeil.server.worker.motifWorker;

import biovis.hackebeil.common.data.Motif;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zeckzer
 */
public class MotifCharsList {

    private List<MotifChars> list;

    /**
     *
     * @param motifList
     */
    public MotifCharsList(
        List<Motif> motifList
    ) {
        list = new ArrayList<>();
        for (Motif motif : motifList) {
            list.add(new MotifChars(motif));
        }
    }

    public List<MotifChars> getList() {
        return list;
    }
}
