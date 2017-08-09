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

import javax.swing.table.AbstractTableModel;

/**
 * Table of motifs.
 *
 * @author müller
 */
public class MotifTable extends AbstractTableModel {

    //
    private static final long serialVersionUID = 5375388494764341843L;

    // Motifs
    private ArrayList<String> motifs;
    private ArrayList<Boolean> normalized;

    /**
     * Constructor.
     *
     */
    public MotifTable() {
        motifs = new ArrayList<String>();
        normalized = new ArrayList<Boolean>();
    }

    /**
     * Clear table.
     *
     */
    public void clear() {
        motifs.clear();
        normalized.clear();
    }

    /**
     * Add motif.
     *
     * @param m motif
     * @param n normalization flag
     */
    public void add(String m, Boolean n) {
        motifs.add(m.toUpperCase());
        normalized.add(n);
    }

    /**
     * Delete motif.
     *
     * @param row motif to delete (row number)
     */
    public void delete(int row) {
        motifs.remove(row);
        normalized.remove(row);
    }

    @Override
    public int getRowCount() {
        if (motifs == null) {
            return 0;
        }
        return motifs.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (motifs == null) {
            return null;
        }
        if (columnIndex > 1 || columnIndex < 0) {
            return null;
        }
        if (rowIndex < 0 || rowIndex >= motifs.size()) {
            return null;
        }
        if (columnIndex == 0) {
            return motifs.get(rowIndex);
        }
        return normalized.get(rowIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Class getColumnClass(int column) {
        if (column == 0) {
            return String.class;
        }
        return Boolean.class;
    }

    @Override
    public void setValueAt(Object newVal, int row, int col) {
        if (newVal.getClass() == String.class && col == 0) {
            String newmotif = (String) newVal;
            motifs.set(row, newmotif);
        }
        if (newVal.getClass() == Boolean.class && col == 1) {
            Boolean newNorm = (Boolean) newVal;
            normalized.set(row, newNorm);
        }
    }

    @Override
    public String getColumnName(int colNum) {
        if (colNum == 0) {
            return "motif";
        }
        if (colNum == 1) {
            return "normalized";
        }
        return "";
    }

    public ArrayList<String> getMotifs() {
        return motifs;
    }

    public ArrayList<Boolean> getNormalized() {
        return normalized;
    }
}
