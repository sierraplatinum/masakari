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
package epiwgseg.data;

import biovis.hackebeil.server.data.BufferedRandomAccessFile;
import biovis.hackebeil.common.data.Location;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import javax.swing.JProgressBar;

/**
 * Read fasta DB file and store references and indices to chromosome names and
 * position within the RandomAccess file.
 *
 * @author zeckzer
 */
public class GenomeFastaDb {

    // file name
    private String filename;
    private String index;
    // number of sequences in fasta DB
    private Integer numberOfSeqs;
    // internal variables storing chromosome related information
    private HashMap<String, Integer> seqLengths;
    private HashMap<String, Long> startFilePos;
    private HashMap<String, Integer> lineLength;
    private HashMap<String, Integer> seqStart;
    // semaphore
    private boolean reading;

    // buffered reader, stays open
    private BufferedRandomAccessFile bufferedReader = null;

    /**
     * Constructor.
     *
     * @param filename input file name
     */
    public GenomeFastaDb(String filename) {
        this(filename, filename + ".idx");
    }

    /**
     * Constructor.
     *
     * @param filename input file name
     * @param index name of index file
     */
    public GenomeFastaDb(
            String filename,
            String index
    ) {
        this.filename = filename;
        this.index = index;
        reading = false;
        numberOfSeqs = 0;
    }

    /**
     * Initialize the search structure. Load or create index.
     *
     * @param progressBar progress bar for showing information
     * @param create true iff new index file should be created, else: read index
     * file
     * @throws IOException in case of IO errors
     */
    public void initFast(
            JProgressBar progressBar,
            boolean create
    ) throws IOException {
        if (create) {
            // create index
            createIndex(progressBar);
        } else {
            // load index
            loadIndex(index);
            bufferedReader = new BufferedRandomAccessFile(filename, "r", 1024 * 16);
        }
    }

    /**
     * Create index. Extracts the chromosomes and the none N part of the
     * chromosome.
     *
     * @param progressBar progress bar for showing information
     * @throws IOException in case of IO errors
     */
    public void createIndex(
            JProgressBar progressBar
    ) throws IOException {
        reading = true;
        numberOfSeqs = 0;
        // init HashMaps
        seqLengths = new HashMap<>();
        startFilePos = new HashMap<>();
        lineLength = new HashMap<>();
        seqStart = new HashMap<>();

        //read and parse fasta
        //BufferedRandomAccessFile reader = new BufferedRandomAccessFile(filename, "r", 1024 * 16);
        bufferedReader = new BufferedRandomAccessFile(filename, "r", 1024 * 16);
        String line = bufferedReader.readLine();
        String chr = "";
        boolean measureLineLength = false;
        Integer length = 0;
        Integer firstNoneN = -1;
        Integer lastNoneN = -1;
        while (line != null) {
            if (line.startsWith(">")) {
                numberOfSeqs++;
                if (progressBar != null) {
                    progressBar.setString("loading genome: " + numberOfSeqs + " sequences");
                }
                // set line measurement to true
                measureLineLength = true;
                //save old line length
                if (!chr.equals("")) {
                    //save sequence length
                    if (lastNoneN != -1) {
                        seqLengths.put(chr, lastNoneN);
                    } else {
                        seqLengths.put(chr, length);
                    }
                    //just if sequence purely N put length as sequence start
                    if (firstNoneN == -1) {
                        seqStart.put(chr, length);
                    }
                    length = 0;
                }
                //reset variables
                firstNoneN = -1;
                lastNoneN = -1;
                //parse chromosome name
                if (line.contains(" ")) {
                    chr = line.substring(1, line.indexOf(" "));
                } else {
                    chr = line.substring(1);
                }
                //save start position of sequence
                startFilePos.put(chr, bufferedReader.getFilePointer());
            } else {
                //System.err.println("processing line "+line);
                //measure line length if first line after header
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

                //look for the last none N character
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
            //read next line
            line = bufferedReader.readLine();
        }

        //save last line length
        if (!chr.equals("")) {
            if (lastNoneN != -1) {
                seqLengths.put(chr, lastNoneN);
            } else {
                seqLengths.put(chr, length);
            }
            //just if sequence purely N put length as sequence start
            if (firstNoneN == -1) {
                seqStart.put(chr, length);
            }
        }
        reading = false;
    }

    /**
     * Get length of the chromosome sequence.
     *
     * @param name of the chromosome
     * @return it's length
     */
    public Integer getSeqLength(String name) {
        if (seqLengths.containsKey(name)) {
            return seqLengths.get(name);
        }
        return 0;
    }

    /**
     * Get the start position of the chromosome sequence.
     *
     * @param name of the chromosome
     * @return it's start position
     */
    public Integer getSeqStart(String name) {
        if (seqStart.containsKey(name)) {
            return seqStart.get(name);
        }
        return 0;
    }

    /**
     * Count number of motifs.
     *
     * @param loc location
     * @param motif motif to search for
     * @return number of motif occurrences found
     */
    public Integer countMotif(
            Location loc,
            String motif
    ) {
        //String motif = search.toUpperCase();
        //System.err.println("Counting motif: " + motif + " " + loc);

        //try (RandomAccessFile bufferedReader = new RandomAccessFile(filename, "r")) {
        try {
            if (startFilePos.containsKey(loc.getChr())
                    && loc.getStart() >= 0
                    && loc.getLength() >= motif.length()) {

                // calculate file position and set reader to this position
                Long fileStart = startFilePos.get(loc.getChr());
                Integer linelen = lineLength.get(loc.getChr());
                Long posInFile = (long) ((loc.getStart() / linelen) * (linelen + 1) + loc.getStart() % linelen) + fileStart;
                bufferedReader.seek(posInFile);

                //read sequence and count
                Integer counter = 0;
                Integer read = 0;
                Integer seqCounter = 0;
                String seq = "";
                String line = bufferedReader.readLine();
                while (line != null && !line.startsWith(">") && read < loc.getLength()) {
                    line = line.trim();
                    line = line.toUpperCase();
                    if ((read + line.length()) <= loc.getLength()) {
                        read += line.length();
                    } else {
                        Integer remaining = loc.getLength() - read;
                        line = line.substring(0, remaining);
                        read += remaining;
                    }
                    seq += line;
                    while (seq.length() >= motif.length() + seqCounter) {
                        if (seq.startsWith(motif, seqCounter)) {
                            counter++;
                        }
                        seqCounter++;
                    }
                    seq = seq.substring(seqCounter);
                    seqCounter = 0;
                    line = bufferedReader.readLine();
                }
                return counter;
            } else {
                return 0;
            }
        } catch (IOException ioEx) {
            Logger log = Logger.getLogger("GenomeFastaDb");
            log.log(Level.SEVERE, "IOException while counting motifs: {0} {1}", new Object[]{filename, ioEx.getMessage()});
            return 0;
        }
    }

    /**
     * Save index file
     *
     * @param indexFile name of the index file
     */
    public void saveIndex(String indexFile) {
        try (FileWriter fw = new FileWriter(indexFile);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter pw = new PrintWriter(bw)) {

            pw.println("!filename: " + filename);
            pw.println("!numberOfSeqs: " + numberOfSeqs);
            pw.println("!startFilePos: " + startFilePos.size());
            for (Entry<String, Long> sfpEntry : startFilePos.entrySet()) {
                pw.println(sfpEntry.getKey() + " " + sfpEntry.getValue());
            }

            pw.println("!seqStart: " + seqStart.size());
            for (Entry<String, Integer> seqStartEntry : seqStart.entrySet()) {
                pw.println(seqStartEntry.getKey() + " " + seqStartEntry.getValue());
            }

            pw.println("!seqLenghts: " + seqLengths.size());
            for (Entry<String, Integer> seqLengthsEntry : seqLengths.entrySet()) {
                pw.println(seqLengthsEntry.getKey() + " " + seqLengthsEntry.getValue());
            }

            pw.println("!lineLenghts: " + lineLength.size());
            for (Entry<String, Integer> lineLengthEntry : lineLength.entrySet()) {
                pw.println(lineLengthEntry.getKey() + " " + lineLengthEntry.getValue());
            }
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }

    /**
     * Load index file.
     *
     * @param indexFile name of index file to load
     *
     */
    public void loadIndex(String indexFile) {
        try (FileReader fr = new FileReader(indexFile);
                BufferedReader br = new BufferedReader(fr)) {

            String[] content;
            String line = br.readLine().trim();
            if (!line.startsWith("!filename: ")) {
                JOptionPane.showMessageDialog(null,
                        "File " + indexFile + " has the wrong format: First line has to contain '!Data' only!",
                        "Wrong Format",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // "!numberOfSeqs: " + numberOfSeqs
            line = br.readLine().trim();
            content = line.split(": ");
            numberOfSeqs = Integer.parseInt(content[1]);

            // "!startFilePos: " + startFilePos.size()
            line = br.readLine().trim();
            content = line.split(": ");
            int startFilePosSize = Integer.parseInt(content[1]);
            startFilePos = new HashMap<String, Long>();
            for (int currentStartFilePos = 0; currentStartFilePos < startFilePosSize; ++currentStartFilePos) {
                // startFilePos
                // sfpEntry.getKey() + " " + sfpEntry.getValue()
                line = br.readLine().trim();
                content = line.split(" ");
                startFilePos.put(content[0], Long.parseLong(content[1]));
            }

            // "!seqStart: " + seqStart.size()
            line = br.readLine().trim();
            content = line.split(": ");
            Integer seqStartSize = Integer.parseInt(content[1]);
            seqStart = new HashMap<>();
            for (int currentSeqStart = 0; currentSeqStart < seqStartSize; ++currentSeqStart) {
                // seqStart
                // seqStartEntry.getKey() + " " + seqStartEntry.getValue()
                line = br.readLine().trim();
                content = line.split(" ");
                seqStart.put(content[0], Integer.parseInt(content[1]));
            }

            // "!seqLenghts: " + seqLengths.size()
            line = br.readLine().trim();
            content = line.split(": ");
            Integer seqLengthsSize = Integer.parseInt(content[1]);
            seqLengths = new HashMap<>();
            for (int currentSeqLengths = 0; currentSeqLengths < seqLengthsSize; ++currentSeqLengths) {
                // seqLengths
                // seqLengthsEntry.getKey() + " " + seqLengthsEntry.getValue()
                line = br.readLine().trim();
                content = line.split(" ");
                seqLengths.put(content[0], Integer.parseInt(content[1]));
            }

            // "!lineLenghts: " + lineLength.size()
            line = br.readLine().trim();
            content = line.split(": ");
            Integer lineLengthSize = Integer.parseInt(content[1]);
            lineLength = new HashMap<>();
            for (int currentLineLength = 0; currentLineLength < lineLengthSize; ++currentLineLength) {
                // lineLength
                // lineLengthEntry.getKey() + " " + lineLengthEntry.getValue()
                line = br.readLine().trim();
                content = line.split(" ");
                lineLength.put(content[0], Integer.parseInt(content[1]));
            }
        } catch (IOException ioEx) {
        } catch (Exception ex) {
            System.err.println("Exception: " + ex.toString());
            System.err.println("Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
