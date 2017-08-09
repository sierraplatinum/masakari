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
package epiwgseg.data;

import java.util.ArrayList;
import java.util.List;

/**
 * List of options.
 *
 * @author müller
 */
public class FileListOptionTable extends FileListTable {

    //
    private static final long serialVersionUID = 2884852113757574259L;

    // flags if score is provided
    private List<Boolean> withScore;

    /**
     * Constructor.
     *
     */
    public FileListOptionTable() {
        super();
        withScore = new ArrayList<>();
    }

    /**
     * Clear information.
     *
     */
    @Override
    public void clear() {
        super.clear();
        withScore.clear();
    }

    /**
     * Add option.
     *
     * @param file file name
     * @param useScore flag if score is provided
     */
    public void add(String file, Boolean useScore) {
        super.add(file);
        withScore.add(useScore);
    }

    /**
     * Move entry up
     *
     * @param row entry to move (row number)
     */
    @Override
    public void up(int row) {
        if (row == 0) {
            return;
        }
        if (row >= files.size()) {
            return;
        }
        super.up(row);

        // swap score
        Boolean score = withScore.get(row - 1);
        withScore.set(row - 1, withScore.get(row));
        withScore.set(row, score);
    }

    /**
     * Move entry down.
     *
     * @param row entry to move (row number)
     */
    @Override
    public void down(int row) {
        if (row >= (files.size() - 1)) {
            return;
        }
        Boolean score = withScore.get(row + 1);
        withScore.set(row + 1, withScore.get(row));
        withScore.set(row, score);
    }

    /**
     * Delete entry.
     *
     * @param row entry to delete (row number)
     */
    @Override
    public void delete(int row) {
        super.delete(row);
        withScore.remove(row);
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (files == null) {
            return null;
        }
        switch (columnIndex) {
            case 0:
            case 1:
                return super.getValueAt(rowIndex, columnIndex);
            case 2:
                return withScore.get(rowIndex);
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 2) {
            return true;
        }
        return super.isCellEditable(rowIndex, columnIndex);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Class getColumnClass(int column) {
        if (column == 2) {
            return Boolean.class;
        }
        return super.getColumnClass(column);
    }

    @Override
    public void setValueAt(Object newVal, int row, int col) {
        switch (col) {
            case 1:
                super.setValueAt(newVal, row, col);
                break;
            case 2:
                if (newVal.getClass() == Boolean.class && col == 2) {
                    Boolean useScore = (Boolean) newVal;
                    withScore.set(row, useScore);
                }
                break;
            default:
        }
    }

    @Override
    public String getColumnName(int colNum
    ) {
        switch (colNum) {
            case 0:
            case 1:
                return super.getColumnName(colNum);
            case 2:
                return "use score";
            default:
                return "";
        }
    }

    /**
     * Get all score flags.
     *
     * @return all score flags
     */
    public List<Boolean> getWithScore() {
        return withScore;
    }
}
