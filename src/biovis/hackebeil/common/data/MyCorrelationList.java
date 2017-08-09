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
 *****************************************************************************
 */package biovis.hackebeil.common.data;

import java.util.ArrayList;
import java.util.List;

import biovislib.statistics.correlation.CorrelationItem;
import biovislib.statistics.correlation.CorrelationList;

public class MyCorrelationList implements CorrelationList {

    private List<CorrelationItem> listOfCorrelationItems = new ArrayList<>();

    public MyCorrelationList(List<double[]> list) {
        for (double[] listElements : list) {
            this.listOfCorrelationItems.add(new MyCorrelationItem(listElements));
        }
    }

    /**
     *
     * @return
     */
    @Override
    public int getSize() {
        return listOfCorrelationItems.size();
    }

    /**
     *
     * @return
     */
    @Override
    public List<? extends CorrelationItem> getCorrelationItems() {
        return listOfCorrelationItems;
    }
}
