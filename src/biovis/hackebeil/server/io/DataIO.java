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
package biovis.hackebeil.server.io;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import biovis.hackebeil.common.data.DataFile;
import biovis.hackebeil.common.data.Segment;
import biovis.hackebeil.common.data.Motif;
import biovis.hackebeil.common.data.PositionWeightMatrix;
import biovis.hackebeil.server.data.ServerCache;
import java.util.logging.Level;

/**
 * @author Alrik Hausdorf, Dirk Zeckzer
 *
 */
public class DataIO {

    private static final Logger log = Logger.getLogger(DataIO.class.getName());

    /**
     *
     * @param filename
     * @param data
     */
    public static void exportData(
        String filename,
        ServerCache data
    ) {
        new Thread(() -> {
            log.log(Level.INFO, "Export data to: {0}", filename);
            // test for ending
            if (filename.endsWith(".data")) {
                exportDataPlain(filename, data);
            } else if (filename.endsWith(".data.gz")) {
                exportDataGZ(filename, data);
            } else {
                log.warning("not '.data' or '.data.gz'");
            }
        }).start();

    }

    /**
     *
     * @param filename
     * @param data
     */
    private static void exportDataPlain(
        String filename,
        ServerCache data
    ) {
        try (FileWriter fw = new FileWriter(filename);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw);) {
            exportData(pw, data);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    /**
     *
     * @param filename
     * @param data
     */
    private static void exportDataGZ(
        String filename,
        ServerCache data
    ) {
        try (FileOutputStream fos = new FileOutputStream(filename);
             GZIPOutputStream gzos = new GZIPOutputStream(fos);
             BufferedOutputStream bos = new BufferedOutputStream(gzos);
             PrintWriter pw = new PrintWriter(bos);) {
            exportData(pw, data);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    private static void exportData(
        PrintWriter printWriter,
        ServerCache data
    ) {
        printWriter.println("!Data");

        // write Header line 1
        printWriter.print("shortId");
        printWriter.print(";longId");
        printWriter.print(";code");
        // write addData
        List<DataFile> additionalData = data.getDfList();
        if (additionalData != null) {
            for (DataFile df : additionalData) {
                printWriter.print(";" + df.getDataSetName());
            }
        }
        // write MotifData
        List<Motif> motifData = data.getMotifList();
        if (motifData != null) {
            for (Motif motif : motifData) {
                printWriter.print(";" + motif.getMotifId());
            }
        }
        // write PWMData
        List<PositionWeightMatrix> pwmData = data.getPWMList();
        if (pwmData != null) {
            for (PositionWeightMatrix pwm : pwmData) {
                printWriter.print(";" + pwm.getMotifId());
            }
        }
        printWriter.println(";" + "length");

        // write Datatypes (line 2)
        printWriter.print("String");
        printWriter.print(";String");
        printWriter.print(";Integer");

        if (additionalData != null) {
            for (int i = 0; i < additionalData.size(); i++) {
                printWriter.print(";Double");
            }
        }
        if (motifData != null) {
            for (int i = 0; i < motifData.size(); i++) {
                printWriter.print(";Double");
            }
        }
        if (pwmData != null) {
            for (int i = 0; i < pwmData.size(); i++) {
                printWriter.print(";Double");
            }
        }
        printWriter.println(";Integer");

        // print data
        List<Segment> dataPoints = data.getAllSegments();
        for (Segment dp : dataPoints) {
            printWriter.print(dp.getShortId()
                              + ";" + dp.getLongId()
                              + ";" + dp.getCode()
            );
            if (additionalData != null) {
                for (DataFile df : additionalData) {
                    printWriter.print(";" + dp.getAdditionalDataValue(df.getDataSetName()));
                }
            }
            if (motifData != null) {
                for (Motif motif : motifData) {
                    printWriter.print(";" + dp.getMotifValue(motif.getMotifId()));
                }
            }
            if (pwmData != null) {
                for (PositionWeightMatrix pwm : pwmData) {
                    printWriter.print(";" + dp.getPWMValue(pwm.getMotifId()));
                }
            }
            printWriter.println(";" + dp.getLength());
        }
    }
}
