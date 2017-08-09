/*******************************************************************************
 * Copyright 2015 Dirk Zeckzer, Lydia Müller, Daniel Gerighausen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package biovis.hackebeil.common.data;

import java.util.List;

/** Scored location of chromosome.
 * 
 * @author müller, Dirk Zeckzer
 */
public class ScoredLocation
    extends Location {

    private final static double EPS = 1e-15;
    private double score;

    /** Constructor.
     * 
     * @param chr chromosome name
     * @param start start position
     * @param end end position
     * @param score score
     */
    public ScoredLocation(String chr, int start, int end, double score) {
        super(chr, start, end);
        this.score = score;
    }

    /** Get scored coverage.
     * 
     * @param locs list of locations
     * @return scored coverage
     */
    public double getScoredCoverage(List<Location> locs) {
        double scoredCov = score * coverage(locs);
        return scoredCov;
    }

    /** Get score.
     * 
     * @return score
     */
    public double getScore() {
        return score;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) Math.round(score);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        ScoredLocation other = (ScoredLocation) obj;
        return score - other.score >= EPS;
    }

    /** Compare this scored location to another one.
     * 
     * @param o scored location to compare to
     * @return result of comparison
     */
    public int compareTo(ScoredLocation o) {
        int comp = super.compareTo(o);
        if (comp != 0) {
            return comp;
        }

        if (score < o.score) {
            return -1;
        }
        if (score > o.score) {
            return 1;
        }

        return 0;
    }

    /** Compute coverage score of a location with other locations.
     * 
     * @param location 
     * @param locs list of other locations
     * @return coverage score
     */
    public static double coverageScore(
        Location location,
        List<Location> locs
    ) {
        int start = location.getStart();
        int end = location.getEnd();

        // coverage in base pairs
        double cov = 0.0;
        int covLength = 0;

        ScoredLocation scoredLoc = null;

        // add up coverage of individual locations
        for (Location loc : locs) {
            scoredLoc = (ScoredLocation) loc;
            //skip locations before and after current location
            if (scoredLoc.getEnd() < start) {
                continue;
            }
            if (scoredLoc.getStart() > end) {
                continue;
            }
            // calculate coverage depending on relative location to current location
            if (scoredLoc.getEnd() <= end) {
                if (scoredLoc.getStart() <= start) {
                    int covTemp = scoredLoc.getEnd() - start + 1;
                    cov += covTemp * scoredLoc.getScore();
                    covLength += covTemp;
                } else {
                    cov += scoredLoc.getLength() * scoredLoc.getScore();
                    covLength += scoredLoc.getLength();
                }
            } else if (scoredLoc.getStart() <= start) {
                int covTemp = end - start + 1;
                cov += covTemp * scoredLoc.getScore();
                covLength += covTemp;
            } else {
                int covTemp = end - scoredLoc.getStart() + 1;
                cov += covTemp * scoredLoc.getScore();
                covLength += covTemp;
            }
        }
        // return coverage normalized by length
        //System.err.println(chr+":"+start+"-"+end+" "+locs.size()+" "+locs.get(0).getChr()+":"+locs.get(0).getStart()+"-"+locs.get(0).getEnd()+" "+cov+" "+this.getLength()+" "+(cov.doubleValue()/this.getLength().doubleValue()));
        //return cov/this.getLength().doubleValue();
        if (covLength == 0) {
            return 0.0;
        }
        return ((double) cov) / ((double) covLength);
    }
}
