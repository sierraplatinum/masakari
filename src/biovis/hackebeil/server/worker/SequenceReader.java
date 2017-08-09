/**
 *  Copyright 2016 Lydia Müller, Dirk Zeckzer
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

import biovis.hackebeil.common.data.Location;
import biovis.hackebeil.common.data.Segment;
import biovis.hackebeil.server.data.BufferedRandomAccessFile;
import biovis.hackebeil.server.data.ServerCache;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * based on epiwgseg.segmentation.TableBuilder, epiwgseg.segmentation.DataTable
 *
 * @author Lydia Müller, Dirk Zeckzer
 *
 */
public class SequenceReader {

    private static final Logger log = Logger.getLogger("SequenceReader");

    private Map<String, Long> startFilePos;
    private Map<String, Integer> lineLength;
    private Map<Location, Long> posInFileMap;

    /**
     *
     * @param cache
     */
    public SequenceReader(
        ServerCache cache
    ) {
        this.startFilePos = cache.getStartFilePos();
        this.lineLength = cache.getLineLength();
        preProcessing(cache.getSegments());
    }

    /**
     *
     * @param segment segment
     * @param bufferedReader reader specially for threading
     * @return character array containing nt's of current segment
     */
    public char[] readSegment(
        Segment segment,
        BufferedRandomAccessFile bufferedReader
    ) {
        char[] segmentDNA;

        Location location = segment.getLocation();

        try {
            Long posInFile = posInFileMap.get(location);
            if (posInFile != null
                && posInFile >= 0) {
                bufferedReader.seek(posInFile);

                // init search lines
                String newLine = bufferedReader.readLine();
                StringBuilder line = new StringBuilder(newLine);

                while ((line.charAt(0) != '>')
                       && (line.length() <= segment.getLength())) {
                    newLine = bufferedReader.readLine();
                    line.append(newLine);
                }
                segmentDNA = line.substring(0, segment.getLength()).toCharArray();

                return segmentDNA;
            } else {
                System.err.println("location not found in posInFileMap: " + location.toString());
                return new char[0];
            }
        } catch (IOException ioEx) {
            log.log(Level.SEVERE, "IOException while reading segment sequence from file: {0} {1}", new Object[]{ioEx.getMessage()});
            System.err.println(ioEx.getMessage());
            return new char[0];
        }
    }

    /**
     *
     * @param segments 
     */
    public void preProcessing(List<Segment> segments) {
        posInFileMap = new HashMap<>();
        Location location;
        for (Segment segment : segments) {
            location = segment.getLocation();
            Long posInFile = getPosInFile(location);
            if (posInFile >= 0) {
                posInFileMap.put(location, posInFile);
            }
        }
    }

    /**
     * Get position of start of location in file.
     *
     * @param location
     * @return position of location start in file
     */
    private long getPosInFile(Location location) {
        String chromosome = location.getChr();
        long locationStart = location.getStart();
        if (startFilePos.containsKey(location.getChr())) {
            if (locationStart >= 0) {
                long fileStart = startFilePos.get(chromosome);
                long linelen = lineLength.get(chromosome);
                long posInFile = (locationStart / linelen) * (linelen + 1)
                                 + locationStart % linelen
                                 + fileStart;
                return posInFile;
            } else {
                System.err.println("Starting position of location less than zero: " + locationStart);
                return -1;
            }
        } else {
            System.err.println("startFilePos map does not contain chromosome of location: " + location.getChr());
            return -1;
        }
    }
}
