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
package biovis.hackebeil.common.data;

/**
 * Based on epiwgseg.segmentation.DataPoint
 *
 * @author müller, nhinzmann, Alrik Hausdorf, Dirk Zeckzer
 */
public class BreakSegment {

	private int beforeCode = -1;
	private int breakCode = -1;
	private transient int afterCode = -1;

	// Location, code, and other data
	private Location loc;

	/**
	 * Constructor.
	 *
	 * @param l
	 *            location
	 * @param code
	 *            code
	 * @param beforeCode
	 */
	public BreakSegment(Location l, int code, int beforeCode) {
		this.loc = l;
		this.breakCode = code;
		this.beforeCode = beforeCode;
	}

    @Override
	public String toString() {
		return "[" + getShortId() + ", " + getLongId() + ", " + getBreakCode() + ", " + getLength() + "]";
	}

	public int getBeforeCode() {
		return beforeCode;
	}

	public int getBreakCode() {
		return breakCode;
	}

	public String getShortId() {
		return loc.getShortId();
	}

	public String getLongId() {
		return loc.getLongId();
	}

	public int getLength() {
		return loc.getLength();
	}

	public Location getLoc() {
		return loc;
	}

	public int getAfterCode() {
		return afterCode;
	}

	public void setAfterCode(int afterCode) {
		this.afterCode = afterCode;
	}
}
