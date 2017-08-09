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

import java.io.File;
import java.util.SortedMap;
import java.util.TreeMap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author nhinzmann, Alrik Hausdorf, Dirk Zeckzer
 *
 */
public class DataFile {

    private String filePathValue;
    private String dataSetNameValue;

    private int id = this.hashCode();

    private transient BooleanProperty useScore;
    private transient BooleanProperty isChanged;
    private transient StringProperty dataSetName;

    private boolean useScoreValue;

    private int columnNumber = -1;
    private int dataListNumber = -1;

    private boolean changed = true;

    private transient SortedMap<Integer, Integer> chromLengths = new TreeMap<>();
    private transient SortedMap<Integer, Integer> thickLengths = new TreeMap<>();
    private transient SortedMap<Integer, Integer> blockLengths = new TreeMap<>();

    public void setFilePath(String path) {
        this.filePathValue = path;

    }

    public String getFilePath() {
        return this.filePathValue;
    }

    public void setDataSetName(String name) {
        dataSetNameProperty().set(name);
        this.dataSetNameValue = name;
    }

    public String getDataSetName() {
        return dataSetNameValue;
    }

    public StringProperty dataSetNameProperty() {
        if (dataSetName == null) {
            dataSetName = new SimpleStringProperty(this, "dataSetNameValue");
            dataSetName.set(dataSetNameValue);
        }
        return dataSetName;
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

    public BooleanProperty isChangedProperty() {
        if (isChanged == null) {
            isChanged = new SimpleBooleanProperty(this, "changed");
        }
        return isChanged;
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
        isChangedProperty().set(changed);
        this.changed = changed;
    }

    public void setUseScore(boolean value) {
        useScoreProperty().set(value);
        this.useScoreValue = value;
    }

    public boolean getUseScore() {
        return this.useScoreValue;
    }

    public BooleanProperty useScoreProperty() {
        if (useScore == null) {
            useScore = new SimpleBooleanProperty(this, "useScore");
        }
        return useScore;
    }

    public void changeUseScore() {
        if (getUseScore()) {
            setUseScore(false);
        } else {
            setUseScore(true);
        }
        setChanged(true);

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SortedMap<Integer, Integer> getChromLengths() {
        return chromLengths;
    }

    public void setChromLengths(SortedMap<Integer, Integer> chromLengths) {
        this.chromLengths = chromLengths;
    }

    public SortedMap<Integer, Integer> getThickLengths() {
        return thickLengths;
    }

    public void setThickLengths(SortedMap<Integer, Integer> thickLengths) {
        this.thickLengths = thickLengths;
    }

    public SortedMap<Integer, Integer> getBlockLengths() {
        return blockLengths;
    }

    public void setBlockLengths(SortedMap<Integer, Integer> blockLengths) {
        this.blockLengths = blockLengths;
    }

    @Override
    public String toString() {
        String parentFolder = new File(this.filePathValue).getParentFile().getName();
        return parentFolder + "/" + this.dataSetNameValue;
    }
}
