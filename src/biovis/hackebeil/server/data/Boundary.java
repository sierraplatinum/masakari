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
package biovis.hackebeil.server.data;

/** Boundary information
 *
 * @author müller, Dirk Zeckzer
 */
public class Boundary
    implements Comparable<Boundary> {

    // Chromosom ename
    private String chr;
    // Starting position
    private int position;

    /** Constructor.
     *
     * @param chr chromosome name
     * @param position starting position
     */
    public Boundary(String chr, int position) {
        this.chr = chr;
        this.position = position;
    }

    /** Retrieve chromosome name.
     *
     * @return  chromosome name
     */
    public String getChr() {
        return chr;
    }

    /** Retrieve starting position.
     *
     * @return starting position
     */
    public int getPosition() {
        return position;
    }

    /** Get hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((chr == null) ? 0 : chr.hashCode());
        result = prime * result + position;
        return result;
    }

    /** Check if this boundary is equal to another boundary.
     *
     * @param obj Boundary to check
     * @return If both boundaries are equal.
     */
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

        Boundary other = (Boundary) obj;
        if (chr == null) {
            if (other.chr != null) {
                return false;
            }
        } else if (!chr.equals(other.chr)) {
            return false;
        }

        return position == other.position;
    }

    /** Compare this boundary to another boundary
     *
     * @param o boundary to compare to
     * @return results of comparison
     */
    @Override
    public int compareTo(Boundary o) {
        int chrComp = chr.compareTo(o.getChr());
        if (chrComp != 0) {
            return chrComp;
        }
        if (position < o.position) {
            return -1;
        }
        if (position > o.position) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return chr + " - " + position;
    }
}
