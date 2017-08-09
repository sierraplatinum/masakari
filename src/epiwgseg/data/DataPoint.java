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
 */
package epiwgseg.data;

import java.util.ArrayList;

import biovis.hackebeil.common.data.Location;

/**
 * Single data point.
 *
 * @author müller
 */
public class DataPoint {

    //Location, code, and other data
    private Location loc;
    private Integer code;
    private ArrayList<Double> data;

    /**
     * Constructor.
     *
     * @param l location
     * @param c code
     */
    public DataPoint(Location l, Integer c) {
        loc = l;
        code = c;
        data = new ArrayList<>();
    }

    /**
     * Get location.
     *
     * @return location
     */
    public Location getLoc() {
        return loc;
    }

    /**
     * Get code.
     *
     * @return code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * Get Data stored.
     *
     * @return data stored
     */
    public ArrayList<Double> getData() {
        return data;
    }

    /**
     * Add additional value.
     *
     * @param val value to add
     */
    public void add(Double val) {
        data.add(val);
    }
}