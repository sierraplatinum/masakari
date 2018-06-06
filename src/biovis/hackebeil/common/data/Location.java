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

/** Location of chromosome.
 * 
 * @author müller, Dirk Zeckzer
 */
public class Location
    implements Comparable<Location> {

    // chromosome name
    protected String chr;
    // start position
    protected int start;
    // end position
    protected int end;

    /** Constructor.
     * 
     * @param chr chromosome name
     * @param start start position
     * @param end end position
     */
    public Location(String chr, int start, int end) {
        super();
        this.chr = chr;
        this.start = start;
        this.end = end;
    }

    /** Constructor.
     * 
     * @param loc
     */
    public Location(Location loc) {
        this(loc.chr, loc.start, loc.end);
    }

    /** Create long ID.
     * 
     * @return long ID created
     */
    public String getLongId() {
        return chr + ":" + String.valueOf(start) + "-" + String.valueOf(end);
    }

    /** Create short ID.
     * 
     * @return short ID created
     */
    public String getShortId() {
        return chr + "_" + String.valueOf(start) + "_" + String.valueOf(end);
    }

    /** Return length.
     * 
     * @return  length
     */
    public int getLength() {
        return end - start + 1;
    }

    /** Compute coverage of other locations.
     * 
     * @param locs list of other locations
     * @return coverage
     */
    public double coverage(List<Location> locs) {
        // coverage in base pairs
        int cov = 0;
        // add up coverage of individual locations
        for (Location loc : locs) {
            //skip locations before and after current location
            if (loc.getEnd() < start) {
                continue;
            }
            if (loc.getStart() > end) {
                continue;
            }
            // calculate coverage depending on relative location to current location
            if (loc.getEnd() <= end) {
                if (loc.getStart() <= start) {
                    cov += loc.getEnd() - start + 1;
                } else {
                    cov += loc.getLength();
                }
            } else if (loc.getStart() <= start) {
                cov += end - start + 1;
            } else {
                cov += end - loc.getStart() + 1;
            }
        }
        // return coverage normalized by length
        //System.err.println(chr+":"+start+"-"+end+" "+locs.size()+" "+locs.get(0).getChr()+":"+locs.get(0).getStart()+"-"+locs.get(0).getEnd()+" "+cov+" "+this.getLength()+" "+(cov.doubleValue()/this.getLength().doubleValue()));
        return ((double) cov) / ((double) getLength());
    }

    /** Check if location is contained in this location.
     * 
     * @param location
     * @return true iff location is contained in this location
     */
    public boolean isContained(
        Location location
    ) {
        if (location.getStart() >= start
            && location.getEnd() <= end) {
  //          System.out.print(location + "@" + this);
            return true;
        } else {
            return false;
        }
    }

    /** Get chromosome name.
     * 
     * @return  chromosome name
     */
    public String getChr() {
        return chr;
    }

    /** Get start position.
     * 
     * @return start position
     */
    public int getStart() {
        return start;
    }

    /** Get end position
     * 
     * @return end position
     */
    public int getEnd() {
        return end;
    }

    /**
     *
     * @param end 
     */
    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((chr == null) ? 0 : chr.hashCode());
        result = prime * result + end;
        result = prime * result + start;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Location other = (Location) obj;
        if (chr == null) {
            if (other.chr != null) {
                return false;
            }
        } else if (!chr.equals(other.chr)) {
            return false;
        }
        if (end != other.end) {
            return false;
        }
        return start == other.start;
    }

    @Override
    public int compareTo(Location o) {
        int chrComp = chr.compareTo(o.getChr());
        if (chrComp != 0) {
            return chrComp;
        }

        if (start < o.start) {
            return -1;
        }
        if (start > o.start) {
            return 1;
        }

        if (end < o.end) {
            return -1;
        }
        if (end > o.end) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Location: " + chr + " | " + start + " | " + end;
    }
}
