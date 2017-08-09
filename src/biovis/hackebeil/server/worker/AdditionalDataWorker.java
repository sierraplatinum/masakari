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
package biovis.hackebeil.server.worker;

import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.common.data.Segment;
import biovis.hackebeil.server.data.BedData;
import biovis.hackebeil.common.data.Location;
import biovis.hackebeil.common.data.ScoredLocation;
import biovis.hackebeil.server.data.ScoredBedData;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * based on epiwgseg.segmentation.TableBuilder and epiwgseg.segmentation.DataTable
 * @author nhinzmann, Dirk Zeckzer
 *
 */
public class AdditionalDataWorker {

    private List<Segment> segments;

    /**
     *
     */
    public AdditionalDataWorker() {

    }

    /**
     *
     * @param modsData
     * @return
     */
    public boolean compute(List<DataFile> modsData) {
        Logger log = Logger.getLogger("Add modifications");
        log.info("Adding Modifications");
        for (DataFile df : modsData) {
            log.log(Level.INFO, "Adding Modification {0}", df.getDataSetName());
            if (df.getUseScore()) {
                // System.out.println("use score");
                ScoredBedData bed = new ScoredBedData(df.getFilePath());
                bed.setName(df.getDataSetName());
                addBedData(bed);
            } else {
                // System.out.println("use not score");
                BedData bed = new BedData(df.getFilePath());
                bed.setName(df.getDataSetName());
                addBedData(bed);
            }
        }
        log.info("Modifications added");
        return true;
    }

    /**
     * Add modifications to table.
     *
     * @param modsData modification file names
     */
    public void addModificationsWithOutScore(List<String> modsData) {
        Logger log = Logger.getLogger("Add modifications");
        log.info("Adding Modifications");
        for (String modFile : modsData) {
            log.log(Level.INFO, "Adding Modification {0}", modFile);

            //load data
            BedData bed = new BedData(modFile);

            //add data
            addBedData(bed);

            //cleaning
            System.gc();
        }
        log.info("Modifications added");
    }

    /**
     * Add modifications to table
     *
     * @param modsData modification file names
     * @param names names for the table columns
     * @param scores scores
     */
    public void addModificationsWithScore(
        List<String> modsData,
        List<String> names,
        List<Boolean> scores) {
        Logger log = Logger.getLogger("Add modifications");
        log.info("Adding Modifications");
        for (int i = 0; i < modsData.size(); i++) {
            String modFile = modsData.get(i);
            String name = names.get(i);

            log.log(Level.INFO, "Adding Modification {0}", name);

            //load data
            if (scores.get(i)) {
                ScoredBedData bed = new ScoredBedData(modFile);
                bed.setName(name);
                //add data
                addBedData(bed);
            } else {
                BedData bed = new BedData(modFile);
                bed.setName(name);
                //add data
                addBedData(bed);
            }
        }
        log.info("Modifications added");
    }

    /**
     * Add additional bed data.
     *
     * @param bed bed data to add
     */
    private void addBedData(BedData bed) {
        for (Segment dp : segments) {
            Location loc = dp.getLocation();
            double cov = loc.coverage(bed.overlappingWith(loc));
            dp.add(bed.getName(), cov);
        }
    }

    /**
     * Add additional bed data (scored).
     *
     * @param bed bed data to add
     */
    private void addBedData(ScoredBedData bed) {

        for (Segment dp : segments) {
            Location loc = dp.getLocation();
            double cov = ScoredLocation.coverageScore(loc, bed.overlappingWith(loc));
            dp.add(bed.getName(), cov);
        }
    }

    /**
     * @return the segments
     */
    public List<Segment> getSegments() {
        return segments;
    }

    /**
     * @param segments the segments
     */
    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }
}
