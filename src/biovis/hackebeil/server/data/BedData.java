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
package biovis.hackebeil.server.data;

import biovis.hackebeil.common.data.Location;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

/**
 * Class for handling bed files.
 *
 * @author müller, Dirk Zeckzer
 */
public class BedData {

    //chr        full 10.000  Locations
    protected Map<String, Map<Integer, SortedSet<Location>>> data = null;
    protected static final int INTERVAL_SIZE = 10000;
    protected String name;

    /**
     * Constructor.
     *
     * @param file name of the file to load
     */
    public BedData(String file) {
        initName(file);

        try {
            if (file.endsWith("gz")) {
                loadBedDataGZ(file);
            } else {
                loadBedDataPlain(file);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Initialize name from file name.
     *
     * @param file file name
     */
    protected void initName(String file) {
        int lastSep = file.lastIndexOf(File.separator);

        name = file.substring(lastSep + 1);
    }

    /**
     * Load data gzip format.
     *
     * @param file name of file to load
     */
    private void loadBedDataGZ(String file) {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             GZIPInputStream gzis = new GZIPInputStream(bis);
             InputStreamReader isr = new InputStreamReader(gzis)) {
            loadBedData(isr);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    /**
     * Load data plain text format.
     *
     * @param file name of file to load
     */
    private void loadBedDataPlain(String file) {
        try (FileReader reader = new FileReader(file)) {
            loadBedData(reader);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    /**
     * load data from reader.
     *
     * @param reader reader to use
     */
    private void loadBedData(Reader reader) {
        //init data structure
        data = new HashMap<>();

        //open file
        try (BufferedReader br = new BufferedReader(reader)) {
            //read line-wise
            while (br.ready()) {
                // create current location
                String[] line = br.readLine().split("\t");
                String chr = line[0];
                int start = Integer.parseInt(line[1]);
                int end = Integer.parseInt(line[2]);
                Location loc = new Location(chr, start, end);

                // init chr container if necessary
                if (!data.containsKey(chr)) {
                    data.put(chr, new HashMap<>());
                }

                // calculate search interval containing start and end
                int startInt = getIntervalStart(start);
                int endInt = getIntervalStart(end);

                // init search interval container and add location
                for (; startInt <= endInt; startInt += INTERVAL_SIZE) {
                    SortedSet<Location> locations = data.get(chr).get(startInt);
                    if (locations == null) {
                        data.get(chr).put(startInt, new TreeSet<>());
                        locations = data.get(chr).get(startInt);
                    }
                    locations.add(loc);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * compute interval start.
     *
     * @param position position in genome
     * @return interval border
     */
    protected static int getIntervalStart(int position) {
        return (position / INTERVAL_SIZE) * INTERVAL_SIZE;
    }

    /**
     * Compute all overlapping locations.
     *
     * @param loc location
     * @return all overlapping locations
     */
    public List<Location> overlappingWith(Location loc) {
        List<Location> overlapLocs = new ArrayList<>();
        SortedSet<Location> locs = new TreeSet<>();

        // get search intervals
        int startInt = getIntervalStart(loc.getStart());
        int endInt = getIntervalStart(loc.getEnd());
        for (; startInt <= endInt; startInt += INTERVAL_SIZE) {
            // add possible overlapping locations to HashSet --> removes duplicates
            if (data.get(loc.getChr()) != null
                && data.get(loc.getChr()).get(startInt) != null) {
                locs.addAll(data.get(loc.getChr()).get(startInt));
            }
        }

        // add unique locations to arraylist and return
        overlapLocs.addAll(locs);
        return overlapLocs;
    }

    /**
     * Compute all boundaries.
     *
     * @return all boundaries
     */
    public SortedSet<Boundary> getAllBoundaries() {
        SortedSet<Boundary> boundaries = new TreeSet<>();

        // iterate over all elements and add start and end as boundary to set
        for (String chr : data.keySet()) {
            Map<Integer, SortedSet<Location>> locationMap = data.get(chr);
            for (Integer interval : locationMap.keySet()) {
                for (Location loc : locationMap.get(interval)) {
                    boundaries.add(new Boundary(chr, loc.getStart()));
                    boundaries.add(new Boundary(chr, loc.getEnd() + 1));
                }
            }
        }

        return boundaries;
    }

    /**
     * Get name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Change name-
     *
     * @param name name to change to
     */
    public void setName(String name) {
        this.name = name;
    }
}
