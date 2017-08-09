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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.gson.Gson;

import biovis.hackebeil.common.data.Segment;
import biovis.hackebeil.server.commander.ServerCommander;
import biovis.hackebeil.server.data.ServerCache;
import biovis.hackebeil.server.data.BufferedRandomAccessFile;
import biovis.hackebeil.common.data.Location;
import biovis.hackebeil.common.data.Messages;

/**
 * Based on epiwgseg.data.GenomeFastaDb.java and
 * epiwgseg.segmentation.TableBuilder.java
 *
 * @author nhinzmann, Dirk Zeckzer
 *
 */
public class IndexWorker {

    private Logger log = Logger.getLogger("IndexWorker");

    private ServerCommander serverCommander;
    private ServerCache cache;

    // number of sequences in fasta DB
    private Integer numberOfSeqs;

    /**
     *
     * @param cache
     * @param commander
     */
    public IndexWorker(
        ServerCache cache,
        ServerCommander commander
    ) {
        this.cache = cache;
        this.serverCommander = commander;
    }

    /**
     * Create index. Extracts the chromosomes and the none N part of the
     * chromosome.
     *
     *
     * @throws IOException in case of IO errors
     */
    private void createIndex() throws IOException {
        final String startLog = "Create index of reference genome";
        Object[] startCommand = {Messages.CLIENT_IndexProgress, startLog};
        serverCommander.sendCommand(startCommand);

        numberOfSeqs = 0;

        // init HashMaps
        Map<String, Integer> seqLengths = cache.getSeqLengths();
        Map<String, Integer> seqStart = cache.getSeqStart();
        Map<String, Long> startFilePos = cache.getStartFilePos();
        Map<String, Integer> lineLength = cache.getLineLength();

        // read and parse fasta
        BufferedRandomAccessFile genomeReader = new BufferedRandomAccessFile(cache.getFilePathToRefGenome(), "r", 1024 * 16);
        cache.setGenomeReader(genomeReader);
        String line = genomeReader.readLine();
        String chr = "";
        boolean measureLineLength = false;
        Integer length = 0;
        Integer firstNoneN = -1;
        Integer lastNoneN = -1;
        while (line != null) {
            if (line.startsWith(">")) {
                numberOfSeqs++;

                // Send progress message
                final String log1 = "loading genome: " + numberOfSeqs + " sequences";
                Object[] command = {Messages.CLIENT_IndexProgress, log1};
                serverCommander.sendCommand(command);

                // set line measurement to true
                measureLineLength = true;
                // save old line length
                if (!chr.equals("")) {
                    // save sequence length
                    if (lastNoneN != -1) {
                        seqLengths.put(chr, lastNoneN);
                    } else {
                        seqLengths.put(chr, length);
                    }
                    // just if sequence purely N put length as sequence start
                    if (firstNoneN == -1) {
                        seqStart.put(chr, length);
                    }
                    length = 0;
                }
                // reset variables
                firstNoneN = -1;
                lastNoneN = -1;
                // parse chromosome name
                if (line.contains(" ")) {
                    chr = line.substring(1, line.indexOf(" "));
                } else {
                    chr = line.substring(1);
                }
                // save start position of sequence
                startFilePos.put(chr, genomeReader.getFilePointer());
            } else {
                // System.err.println("processing line "+line);
                // measure line length if first line after header
                if (measureLineLength) {
                    lineLength.put(chr, line.length());
                    measureLineLength = false;
                }

                // look for first none N character
                line = line.trim();
                line = line.toUpperCase();
                if (firstNoneN == -1) {
                    Integer noneN = -1;

                    Integer a = line.indexOf("A");
                    if (a != -1) {
                        noneN = a;
                    }

                    Integer t = line.indexOf("T");
                    if (t != -1) {
                        if (noneN == -1) {
                            noneN = t;
                        } else if (t < noneN) {
                            noneN = t;
                        }
                    }

                    Integer c = line.indexOf("C");
                    if (c != -1) {
                        if (noneN == -1) {
                            noneN = c;
                        } else if (c < noneN) {
                            noneN = c;
                        }
                    }

                    Integer g = line.indexOf("G");
                    if (g != -1) {
                        if (noneN == -1) {
                            noneN = g;
                        } else if (g < noneN) {
                            noneN = g;
                        }
                    }

                    if (noneN != -1) {
                        firstNoneN = length + noneN;
                        seqStart.put(chr, firstNoneN);
                    }
                }

                // look for the last none N character
                Integer noneN = -1;

                Integer a = line.lastIndexOf("A");
                if (a != -1) {
                    noneN = a;
                }

                Integer t = line.lastIndexOf("T");
                if (t != -1) {
                    if (t > noneN) {
                        noneN = t;
                    }
                }

                Integer c = line.lastIndexOf("C");
                if (c != -1) {
                    if (c > noneN) {
                        noneN = c;
                    }
                }

                Integer g = line.lastIndexOf("G");
                if (g != -1) {
                    if (g > noneN) {
                        noneN = g;
                    }
                }

                if (noneN != -1) {
                    lastNoneN = length + noneN;
                }

                // measure sequence length
                length += line.length();
            }

            // read next line
            line = genomeReader.readLine();
        }

        // save last line length
        if (!chr.equals("")) {
            if (lastNoneN != -1) {
                seqLengths.put(chr, lastNoneN);
            } else {
                seqLengths.put(chr, length);
            }
            // just if sequence purely N put length as sequence start
            if (firstNoneN == -1) {
                seqStart.put(chr, length);
            }
        }

        final String isCreated = "Create index of reference genome complete";
        Object[] isCreatedCommand = {Messages.CLIENT_IndexProgress, isCreated};
        serverCommander.sendCommand(isCreatedCommand);
    }

    /**
     * Save index file
     *
     */
    private void saveIndex() {
        try (FileWriter fw = new FileWriter(cache.getFilePathToIndex());
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {

            pw.println("!filename: " + cache.getFilePathToIndex());
            pw.println("!numberOfSeqs: " + numberOfSeqs);
            pw.println("!startFilePos: " + cache.getStartFilePos().size());
            for (Entry<String, Long> sfpEntry : cache.getStartFilePos().entrySet()) {
                pw.println(sfpEntry.getKey() + " " + sfpEntry.getValue());
            }

            pw.println("!seqStart: " + cache.getSeqStart().size());
            for (Entry<String, Integer> seqStartEntry : cache.getSeqStart().entrySet()) {
                pw.println(seqStartEntry.getKey() + " " + seqStartEntry.getValue());
            }

            pw.println("!seqLenghts: " + cache.getSeqLengths().size());
            for (Entry<String, Integer> seqLengthsEntry : cache.getSeqLengths().entrySet()) {
                pw.println(seqLengthsEntry.getKey() + " " + seqLengthsEntry.getValue());
            }

            pw.println("!lineLenghts: " + cache.getLineLength().size());
            for (Entry<String, Integer> lineLengthEntry : cache.getLineLength().entrySet()) {
                pw.println(lineLengthEntry.getKey() + " " + lineLengthEntry.getValue());
            }

            final String isSaved = "Save index of reference genome";
            Object[] command = {Messages.CLIENT_IndexProgress, isSaved};
            serverCommander.sendCommand(command);
        } catch (IOException ioEx) {
            System.err.println("Can't save indexFile");
            ioEx.printStackTrace();

            Object[] command = {Messages.CLIENT_IndexProgress, "Can't save indexFile"};
            serverCommander.sendCommand(command);
        }
    }

    /**
     * Load index file.
     *
     * @throws IOException
     *
     */
    private String loadIndex() {
        // System.err.println(cache.getFilePathToIndex());
        String result = "Unknown";

        try (FileReader fr = new FileReader(cache.getFilePathToIndex());
             BufferedReader br = new BufferedReader(fr)) {
            String[] content;

            // Read filename
            String line = br.readLine().trim();
            if (!line.startsWith("!filename: ")) {
                return "File " + cache.getFilePathToIndex()
                       + " has the wrong format: First line has to contain '!Data' only!";
            }

            // Read number of sequences
            line = br.readLine().trim();
            content = line.split(": ");
            numberOfSeqs = Integer.parseInt(content[1]);

            // Read number of chromosome start positions
            line = br.readLine().trim();
            // System.err.println(line);
            content = line.split(": ");
            int startFilePosSize = Integer.parseInt(content[1]);

            // construct map of chromosome start positions
            Map<String, Long> startFilePos = cache.getStartFilePos();
            for (int currentStartFilePos = 0; currentStartFilePos < startFilePosSize; ++currentStartFilePos) {
                line = br.readLine().trim();
                content = line.split(" ");
                startFilePos.put(content[0], Long.parseLong(content[1]));
            }

            // Read number of sequence start positions
            line = br.readLine().trim();
            content = line.split(": ");
            int seqStartSize = Integer.parseInt(content[1]);

            // construct map of sequence start positions
            Map<String, Integer> seqStart = cache.getSeqStart();
            for (int currentSeqStart = 0; currentSeqStart < seqStartSize; ++currentSeqStart) {
                line = br.readLine().trim();
                content = line.split(" ");
                seqStart.put(content[0], Integer.parseInt(content[1]));
            }

            // Read number of sequence lengths
            line = br.readLine().trim();
            content = line.split(": ");
            int seqLengthsSize = Integer.parseInt(content[1]);

            // construct map of sequence lengths
            Map<String, Integer> seqLengths = cache.getSeqLengths();
            for (int currentSeqLengths = 0; currentSeqLengths < seqLengthsSize; ++currentSeqLengths) {
                line = br.readLine().trim();
                content = line.split(" ");
                seqLengths.put(content[0], Integer.parseInt(content[1]));
            }

            // Read number of line lengths
            line = br.readLine().trim();
            content = line.split(": ");
            int lineLengthSize = Integer.parseInt(content[1]);

            // construct map of line lengths
            Map<String, Integer> lineLength = cache.getLineLength();
            for (int currentLineLength = 0; currentLineLength < lineLengthSize; ++currentLineLength) {
                line = br.readLine().trim();
                content = line.split(" ");
                lineLength.put(content[0], Integer.parseInt(content[1]));
            }
            return result = "Load successful";
        } catch (IOException ioEx) {
            System.err.println("Exception: " + ioEx.toString());
            System.err.println("Exception: " + ioEx.getMessage());
            ioEx.printStackTrace();
            return "Exception: " + ioEx.toString() + " -- " + ioEx.getMessage();
        } catch (Exception ex) {
            System.err.println("Exception: " + ex.toString());
            System.err.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
            return "Exception: " + ex.toString() + " -- " + ex.getMessage();
        }
    }

    public void startCreateAndSaveIndex() {
        try {
            createIndex();
        } catch (IOException ioEx) {
            System.err.println("Exception: " + ioEx.toString());
            ioEx.printStackTrace();

            final String message = "Create index failed: "
                                   + ioEx.getMessage()
                                   + " -- " + ioEx.toString();
            Object[] command = {Messages.CLIENT_IndexProgress, message};
            serverCommander.sendCommand(command);
            return;
        }
        saveIndex();
    }

    /**
     */
    public void startLoadIndex() {
        String result = loadIndex();
        Object[] command = {Messages.CLIENT_IndexProgress, result};
        serverCommander.sendCommand(command);
    }

    /**
     * @param dp 
     */
    public void getNucleotides(Segment dp) {
        try {
            Map<String, Integer> lineLength = cache.getLineLength();
            Map<String, Long> startFilePos = cache.getStartFilePos();
            String[] parts = dp.getShortId().split("_");
            BufferedRandomAccessFile genomeReader = cache.getGenomeReader();
            String result = "";
            Location loc = new Location(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            int length = loc.getLength();
            boolean isTooBig = false;
            if (loc.getLength() > 100000) {
                isTooBig = true;
                length = 100000;
            }
            long fileStart = startFilePos.get(loc.getChr());
            int linelen = lineLength.get(loc.getChr());
            long posInFile = (long) ((loc.getStart() / linelen) * (linelen + 1) + loc.getStart() % linelen) + fileStart;
            genomeReader.seek(posInFile);
            String line = genomeReader.readLine();
            while (line != null && (line.charAt(0) != '>') && result.length() < length) {
                if (result.length() + line.length() <= length) {
                    result += line;
                } else {
                    result += line.substring(0, length - result.length() + 1);
                }
                line = genomeReader.readLine();
            }
            Gson gson = new Gson();
            if (isTooBig) {
                result = "Too many Nucleotides. Showing the first 100k:\n" + result;
            }
            dp.setNucleotids(result);

            /*
            String result1 = gson.toJson(dp);
			Object[] command = { Messages.CLIENT_GETNUCLEOTIDES, result1 };
			serverCommander.sendCommand(command);
             */
        } catch (IOException ioEx) {
            System.err.println("Exception: " + ioEx.toString());
            System.err.println("Exception: " + ioEx.getMessage());
            ioEx.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Exception: " + ex.toString());
            System.err.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
