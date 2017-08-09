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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author zeckzer, nhinzmann
 *
 */
public abstract class MotifBase {

    private transient BooleanProperty isNormalized;
    protected boolean isNormalizedValue;
    private int columnNumber = -1;
    private int dataListNumber = -1;
    private boolean changed = true;
    protected String motifId;

    public MotifBase() {
    }

    /**
     * @return the columnNumber
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * @param columnNumber the columnNumber to set
     */
    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    /**
     * @return the dataListNumber
     */
    public int getDataListNumber() {
        return dataListNumber;
    }

    /**
     * @param dataListNumber the dataListNumber to set
     */
    public void setDataListNumber(int dataListNumber) {
        this.dataListNumber = dataListNumber;
    }

    /**
     * @return the changed
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * @param changed the changed to set
     */
    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public void setIsNormalized(boolean value) {
        this.isNormalizedValue = value;
        isNormalizedProperty().set(value);
    }

    public boolean getIsNormalized() {
        return this.isNormalizedValue;
    }

    public BooleanProperty isNormalizedProperty() {
        if (isNormalized == null) {
            isNormalized = new SimpleBooleanProperty(this, "isNormalized");
            isNormalized.set(isNormalizedValue);
        }
        return isNormalized;
    }

    public void changeIsNormalized() {
        if (getIsNormalized()) {
            setIsNormalized(false);
        } else {
            setIsNormalized(true);
        }
    }

    public String getMotifId() {
        return motifId;
    }
}
