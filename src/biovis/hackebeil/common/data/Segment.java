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
package biovis.hackebeil.common.data;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;

import java.util.Map;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Based on epiwgseg.segmentation.DataPoint
 *
 * @author müller, nhinzmann, Alrik Hausdorf, Dirk Zeckzer
 */
public class Segment {

    final private transient static DecimalFormat motifFormatter = new DecimalFormat("#.#####");

    // Location, code, and other data
    private Location location;
    private int code;
    private String nucleotids;

    private Map<String, Double> additionalData;
    private Map<String, Double> motifData;
    private Map<String, Double> pwmData;
    private boolean shortSegment;

    private transient StringProperty nucleotidsProp;

    /**
     * Constructor.
     *
     * @param location location
     * @param code code
     */
    public Segment(
        Location location,
        int code
    ) {
        this.location = location;
        this.code = code;
        additionalData = new HashMap<>();
        motifData = new HashMap<>();
        pwmData = new HashMap<>();
        setNucleotids("");
    }

    /**
     * Get location.
     *
     * @return location
     */
    public Location getLocation() {
        return location;
    }

    public StringProperty getNucleotidsProp() {
        if (this.nucleotidsProp == null) {
            nucleotidsProp = new SimpleStringProperty(nucleotids);
        }
        return this.nucleotidsProp;
    }

    public void setNucleotids(String nucl) {
        if (this.nucleotidsProp == null) {
            nucleotidsProp = new SimpleStringProperty("");
        }
        this.nucleotids = nucl;

        this.nucleotidsProp.set(nucleotids);
    }

    /**
     * Get code.
     *
     * @return code
     */
    public int getCode() {
        return code;
    }

    public int getLength() {
        return location.getLength();
    }

    public String getLongId() {
        return location.getLongId();
    }

    public String getShortId() {
        return location.getShortId();
    }

    /**
     * @return the dataMotif
     */
    public Map<String, Double> getMotifData() {
        return motifData;
    }

    /**
     * @return the dataMotif
     */
    public Map<String, Double> getPWMData() {
        return pwmData;
    }

    /**
     * Get Data stored.
     *
     * @return data stored
     */
    public Map<String, Double> getAdditionalData() {
        return additionalData;
    }

    /**
     * Add additional value.
     *
     * @param df
     * @param val value to add
     */
    public void add(String df, double val) {

        additionalData.put(df, val);
    }

    public void addMotifData(String motifId, double value) {
        motifData.put(motifId, value);
    }

    public void addPWMData(String pwmId, double value) {
        pwmData.put(pwmId, value);
    }

    /**
     * @param motif
     * @return
     */
    public Double getMotifValue(String motif) {
        return motifData.get(motif);
    }

    /**
     * @param pwm
     * @return
     */
    public Double getPWMValue(String pwm) {
        return pwmData.get(pwm);
    }

    public String getMotifValueAsString(String motif) {
        Double value = this.getMotifValue(motif);
        //TODO: testing
        if (value == null) {
            return null;
        }
        if (value == value.intValue()) {
            return Integer.toString(value.intValue());
        }
        return motifFormatter.format(value.doubleValue());

    }

    /**
     * @param df
     * @return
     */
    public Double getAdditionalDataValue(String df) {
        return additionalData.get(df);
    }

    @Override
    public String toString() {
        return "[" + this.getShortId()
               + ", " + this.getLongId()
               + ", " + this.getCode()
               + ", " + this.getNucleotidsProp().get()
               + ", " + Arrays.toString(this.getAdditionalData().values().toArray())
               + ", " + Arrays.toString(this.getMotifData().values().toArray())
               + ", " + this.getLength() + "]";
    }

    private int[] getBytesByInt(int input, int length) {

        int[] bits = new int[length];
        for (int i = bits.length - 1; i >= 0; i--) {
            bits[bits.length - 1 - i] = ((input & (1 << i)) != 0) ? 1 : 0;
        }

        return bits;
    }

    public int[] getCodeArray(int size) {
        return this.getBytesByInt(this.getCode(), size);
    }

    public boolean isShortSegment() {
        return shortSegment;
    }

    public void setShortSegment(boolean shortSegment) {
        this.shortSegment = shortSegment;
    }
}
