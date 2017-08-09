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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import biovis.hackebeil.common.data.DataFile;
import java.io.Reader;

/**
 * @author: Dirk Zeckzer
 */
public class BedFileLoader {

    /**
     * @param df
     */
    public static void readLengths(DataFile df) {
        try {
            if (df.getFilePath().endsWith("gz")) {
                readLengthsGZ(df);
            } else {
                readLengthsPlain(df);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param df
     */
    private static void readLengthsGZ(DataFile df) {
        try (FileInputStream fis = new FileInputStream(new File(df.getFilePath()).getPath());
                BufferedInputStream bis = new BufferedInputStream(fis);
                GZIPInputStream gzis = new GZIPInputStream(bis);
                InputStreamReader isr = new InputStreamReader(gzis)) {
            readLengths(df, isr);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    /**
     * Load data plain text format.
     *
     * @param file name of file to load
     */
    private static void readLengthsPlain(DataFile df) {
        try (FileReader reader = new FileReader(new File(df.getFilePath()).getPath())) {
            readLengths(df, reader);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    /**
     * @param df
     * @param reader
     */
    private static void readLengths(
            DataFile df,
            Reader reader
    ) {
        try (BufferedReader br = new BufferedReader(reader)) {
            int lineNumber = 0;
            while (br.ready()) {
                // create current location
                // System.out.println("Line:"+lineNumber);
                String[] line = br.readLine().split("\t");

                Integer chromLength = Integer.parseInt(line[2]) - Integer.parseInt(line[1]) + 1;
                if (df.getChromLengths().containsKey(chromLength)) {
                    Integer newVal = df.getChromLengths().get(chromLength) + 1;
                    df.getChromLengths().replace(chromLength, newVal);
                } else {
                    df.getChromLengths().put(chromLength, 1);
                }
                // System.out.print(" chromLength"+chromLength);

                if (line.length >= 7) {
                    Integer thickLength = 0;
                    try {
                        thickLength = Integer.parseInt(line[7]) - Integer.parseInt(line[6]) + 1;
                    } catch (Exception e) {
                        continue;
                    }
                    if (df.getThickLengths().containsKey(thickLength)) {
                        Integer newVal = df.getThickLengths().get(thickLength) + 1;
                        df.getThickLengths().replace(thickLength, newVal);
                    } else {
                        df.getThickLengths().put(thickLength, 1);
                    }
                }
                // System.out.print(" thickLength"+thickLength);
                if (line.length > 10) {
                    String[] blockLengths = line[10].split(",");
                    for (int i = 0; i < blockLengths.length; i++) {
                        int blockLength = Integer.parseInt(blockLengths[i]);
                        if (df.getBlockLengths().containsKey(blockLength)) {
                            Integer newVal = df.getBlockLengths().get(blockLength) + 1;
                            df.getBlockLengths().replace(blockLength, newVal);
                        } else {
                            df.getBlockLengths().put(blockLength, 1);
                        }
                        // System.out.print(" blockLength("+i+")"+blockLength);
                    }
                }
                // System.out.println();

                lineNumber++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
