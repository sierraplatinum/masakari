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

import biovis.hackebeil.common.data.ScoredLocation;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

/**
 * Class for handling bed files with assigned scores.
 *
 * @author müller, Dirk Zeckzer
 */
public class ScoredBedData
    extends BedData {

    /**
     * Constructor.
     *
     * @param file name of the file to load
     */
    public ScoredBedData(String file) {
        super(file);

        try {
            if (file.endsWith("gz")) {
                loadScoredBedDataGZ(file);
            } else {
                loadScoredBedDataPlain(file);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Load data gzip format.
     *
     * @param file name of file to load
     */
    private void loadScoredBedDataGZ(String file) {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             GZIPInputStream gzis = new GZIPInputStream(bis);
             InputStreamReader isr = new InputStreamReader(gzis)) {
            loadScoredBedData(isr);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    /**
     * Load data plain text format.
     *
     * @param file name of file to load
     */
    private void loadScoredBedDataPlain(String file) {
        try (FileReader reader = new FileReader(file)) {
            loadScoredBedData(reader);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    /**
     * load data from reader.
     *
     * @param reader reader to use
     */
    private void loadScoredBedData(Reader reader) {
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
                double score = Double.parseDouble(line[4]);
                ScoredLocation loc = new ScoredLocation(chr, start, end, score);

                // init chr container if necessary
                if (!data.containsKey(chr)) {
                    data.put(chr, new HashMap<>());
                }

                // calculate search interval containing start and end
                // init search interval container and add location
                for (int startInt = getIntervalStart(start), endInt = getIntervalStart(end);
                     startInt <= endInt;
                     startInt += INTERVAL_SIZE) {
                    data.get(chr).put(startInt, new TreeSet<>());
                    data.get(chr).get(startInt).add(loc);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
