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

import java.io.File;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

/**
 * List of files to process.
 *
 * @author müller
 */
public class FileListTable extends AbstractTableModel {

    //
    private static final long serialVersionUID = 2884852113757574259L;

    // File names and aliases
    protected ArrayList<String> files;
    protected ArrayList<String> aliases;

    /**
     * Constructor.
     *
     */
    public FileListTable() {
        files = new ArrayList<String>();
        aliases = new ArrayList<String>();
    }

    /**
     * Clear information.
     *
     */
    public void clear() {
        files.clear();
        aliases.clear();
    }

    /**
     * Add file.
     *
     * @param file file name
     */
    public void add(String file) {
        files.add(file);
        String name = file;
        if (name.lastIndexOf(File.separator) != -1) {
            name = name.substring(name.lastIndexOf(File.separator) + 1);
        }
        aliases.add(name);
    }

    /**
     * Move entry up
     *
     * @param row entry to move (row number)
     */
    public void up(int row) {
        if (row == 0) {
            return;
        }
        if (row >= files.size()) {
            return;
        }

        // swap files
        String file = files.get(row - 1);
        files.set(row - 1, files.get(row));
        files.set(row, file);

        // swap aliases
        String alias = aliases.get(row - 1);
        aliases.set(row - 1, aliases.get(row));
        aliases.set(row, alias);
    }

    /**
     * Move entry down.
     *
     * @param row entry to move (row number)
     */
    public void down(int row) {
        if (row >= (files.size() - 1)) {
            return;
        }

        // swap files
        String file = files.get(row + 1);
        files.set(row + 1, files.get(row));
        files.set(row, file);

        // swap aliases
        String alias = aliases.get(row + 1);
        aliases.set(row + 1, aliases.get(row));
        aliases.set(row, alias);
    }

    /**
     * Delete entry.
     *
     * @param row entry to delete (row number)
     */
    public void delete(int row) {
        aliases.remove(row);
        files.remove(row);
    }

    /**
     * Set name.
     *
     * @param row row number
     * @param name name
     */
    public void setName(int row, String name) {
        if (row >= aliases.size()) {
            return;
        }
        aliases.set(row, name);
    }

    /**
     * Get name.
     *
     * @param row row number
     * @return name
     */
    public String getName(int row) {
        if (row >= aliases.size()) {
            return null;
        }
        return aliases.get(row);
    }

    /**
     * Get file name.
     *
     * @param row row number
     * @return name
     */
    public String getFile(int row) {
        if (row >= files.size()) {
            return null;
        }
        return files.get(row);
    }

    @Override
    public int getRowCount() {
        if (files == null) {
            return 0;
        }
        return files.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (files == null) {
            return null;
        }
        switch (columnIndex) {
            case 0:
                return files.get(rowIndex);
            case 1:
                return aliases.get(rowIndex);
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            return true;
        }
        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Class getColumnClass(int column) {
        return String.class;
    }

    @Override
    public void setValueAt(Object newVal, int row, int col) {
        if (col == 1 && newVal.getClass() == String.class) {
            String newName = (String) newVal;
            aliases.set(row, newName);
        }
    }

    @Override
    public String getColumnName(int colNum) {
        switch (colNum) {
            case 0:
                return "file path";
            case 1:
                return "data set name";
            default:
                return "";
        }
    }

    /**
     * Get all file names.
     *
     * @return all file names
     */
    public ArrayList<String> getFiles() {
        return files;
    }

    /**
     * Get all names.
     *
     * @return all names
     */
    public ArrayList<String> getNames() {
        return aliases;
    }
}
