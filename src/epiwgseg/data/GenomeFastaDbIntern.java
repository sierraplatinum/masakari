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
package epiwgseg.data;

import biovis.hackebeil.server.data.BufferedRandomAccessFile;
import biovis.hackebeil.common.data.Location;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.apache.commons.io.input.CountingInputStream;

import javax.swing.JProgressBar;

/**
 * Read fasta DB file and store references and indices to chromosome names and
 * position within the RandomAccess file.
 *
 * @author zeckzer
 */
public class GenomeFastaDbIntern {

    // file name
    private String filename;
    private HashMap<String, Integer> seqLengths;
    // number of sequences in fasta DB
    private Integer numberOfSeqs;
    // internal variables storing chromosome related information
    public HashMap<String, Long> startFilePos;
    public HashMap<String, Integer> lineLength;
    public HashMap<String, Integer> seqStart;
    public boolean reading;

    private BufferedRandomAccessFile bufferedReader;

    /**
     * Constructor. Sets the input file name.
     *
     * @param file input file name
     */
    public GenomeFastaDbIntern(String file) {
        filename = file;
        reading = false;
        numberOfSeqs = 0;
    }

    /**
     * Initialize the search structure. Load or create index.
     *
     * @param progressBar progress bar for showing information
     * file
     * @throws IOException in case of IO errors
     */
    public void init(
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
        RandomAccessFile reader = new RandomAccessFile(filename, "r");
        String line = reader.readLine();
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
                lastNoneN = -1;
                //parse chromosome name
                if (line.contains(" ")) {
                    line = line.substring(0, line.indexOf(" "));
                }
                chr = line.substring(1);
                //save start position of sequence
                startFilePos.put(chr, reader.getFilePointer());
                //read next line
                line = reader.readLine();
                continue;
            }
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

                if (line.contains("A")) {
                    Integer a = line.indexOf("A");
                    if (a != -1) {
                        noneN = a;
                    }
                }
                if (line.contains("T")) {
                    Integer t = line.indexOf("T");
                    if (t != -1) {
                        if (noneN == -1) {
                            noneN = t;
                        } else if (t < noneN) {
                            noneN = t;
                        }
                    }
                }
                if (line.contains("C")) {
                    Integer c = line.indexOf("C");
                    if (c != -1) {
                        if (noneN == -1) {
                            noneN = c;
                        } else if (c < noneN) {
                            noneN = c;
                        }
                    }
                }
                if (line.contains("G")) {
                    Integer g = line.indexOf("G");
                    if (g != -1) {
                        if (noneN == -1) {
                            noneN = g;
                        } else if (g < noneN) {
                            noneN = g;
                        }
                    }
                }
                if (noneN != -1) {
                    firstNoneN = length + noneN;
                    seqStart.put(chr, firstNoneN);
                }
            }
            //look for the last none N character
            Integer noneN = -1;

            if (line.contains("A")) {
                Integer a = line.lastIndexOf("A");
                if (a != -1) {
                    noneN = a;
                }
            }
            if (line.contains("T")) {
                Integer t = line.lastIndexOf("T");
                if (t != -1) {
                    if (noneN == -1) {
                        noneN = t;
                    } else if (t < noneN) {
                        noneN = t;
                    }
                }
            }
            if (line.contains("C")) {
                Integer c = line.lastIndexOf("C");
                if (c != -1) {
                    if (noneN == -1) {
                        noneN = c;
                    } else if (c < noneN) {
                        noneN = c;
                    }
                }
            }
            if (line.contains("G")) {
                Integer g = line.lastIndexOf("G");
                if (g != -1) {
                    if (noneN == -1) {
                        noneN = g;
                    } else if (g < noneN) {
                        noneN = g;
                    }
                }
            }

            if (noneN != -1) {
                lastNoneN = length + noneN;
            }

            // measure sequence length
            length += line.length();

            //read next line
            line = reader.readLine();
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
        if (!create) {
            loadIndex(filename + ".idx");
            bufferedReader = new BufferedRandomAccessFile(filename, "r", 1024 * 16);
        } else {
            createIndex(progressBar);
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
                //read next line
                line = bufferedReader.readLine();
                continue;
            }

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

                //if (line.contains("A")) {
                    Integer a = line.indexOf("A");
                    if (a != -1) {
                        noneN = a;
                    }
                //}
                //if (line.contains("T")) {
                    Integer t = line.indexOf("T");
                    if (t != -1) {
                        if (noneN == -1) {
                            noneN = t;
                        } else if (t < noneN) {
                            noneN = t;
                        }
                    }
                //}
                //if (line.contains("C")) {
                    Integer c = line.indexOf("C");
                    if (c != -1) {
                        if (noneN == -1) {
                            noneN = c;
                        } else if (c < noneN) {
                            noneN = c;
                        }
                    }
                //}
                //if (line.contains("G")) {
                    Integer g = line.indexOf("G");
                    if (g != -1) {
                        if (noneN == -1) {
                            noneN = g;
                        } else if (g < noneN) {
                            noneN = g;
                        }
                    }
                //}
                if (noneN != -1) {
                    firstNoneN = length + noneN;
                    seqStart.put(chr, firstNoneN);
                }
            }
            //look for the last none N character
            Integer noneN = -1;

            //if (line.contains("A")) {
                Integer a = line.lastIndexOf("A");
                if (a != -1) {
                    noneN = a;
                }
            //}
            //if (line.contains("T")) {
                Integer t = line.lastIndexOf("T");
                if (t != -1) {
                    if (noneN == -1) {
                        noneN = t;
                    } else if (t < noneN) {
                        noneN = t;
                    }
                }
            //}
            //if (line.contains("C")) {
                Integer c = line.lastIndexOf("C");
                if (c != -1) {
                    if (noneN == -1) {
                        noneN = c;
                    } else if (c < noneN) {
                        noneN = c;
                    }
                }
            //}
            //if (line.contains("G")) {
                Integer g = line.lastIndexOf("G");
                if (g != -1) {
                    if (noneN == -1) {
                        noneN = g;
                    } else if (g < noneN) {
                        noneN = g;
                    }
                }
            //}

            if (noneN != -1) {
                lastNoneN = length + noneN;
            }

            // measure sequence length
            length += line.length();

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
     *
     * @param progressBar progress bar
     * @throws IOException in cae of IOError
     */
    public void initFastDz(
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
        try (FileInputStream fis = new FileInputStream(filename);
                BufferedInputStream bis = new BufferedInputStream(fis);
                //GZIPInputStream gzis = new GZIPInputStream(bis);
                CountingInputStream cis = new CountingInputStream(bis);
                InputStreamReader isr = new InputStreamReader(cis);
                BufferedReader reader = new BufferedReader(isr)) {

            long count = cis.getByteCount();
            long linePos = 0;
            String line = reader.readLine();
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
                        line = line.substring(0, line.indexOf(" "));
                    }
                    chr = line.substring(1);
                    //save start position of sequence
                    startFilePos.put(chr, count - 8096 + linePos);
//                    startFilePos.put(chr, cis.getByteCount());
                    //read next line
                    count = cis.getByteCount();
                    line = reader.readLine();
                    continue;
                }
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

                    if (line.contains("A")) {
                        Integer a = line.indexOf("A");
                        if (a != -1) {
                            noneN = a;
                        }
                    }
                    if (line.contains("T")) {
                        Integer t = line.indexOf("T");
                        if (t != -1) {
                            if (noneN == -1) {
                                noneN = t;
                            } else if (t < noneN) {
                                noneN = t;
                            }
                        }
                    }
                    if (line.contains("C")) {
                        Integer c = line.indexOf("C");
                        if (c != -1) {
                            if (noneN == -1) {
                                noneN = c;
                            } else if (c < noneN) {
                                noneN = c;
                            }
                        }
                    }
                    if (line.contains("G")) {
                        Integer g = line.indexOf("G");
                        if (g != -1) {
                            if (noneN == -1) {
                                noneN = g;
                            } else if (g < noneN) {
                                noneN = g;
                            }
                        }
                    }
                    if (noneN != -1) {
                        firstNoneN = length + noneN;
                        seqStart.put(chr, firstNoneN);
                    }
                }
                //look for the last none N character
                Integer noneN = -1;

                if (line.contains("A")) {
                    Integer a = line.lastIndexOf("A");
                    if (a != -1) {
                        noneN = a;
                    }
                }
                if (line.contains("T")) {
                    Integer t = line.lastIndexOf("T");
                    if (t != -1) {
                        if (noneN == -1) {
                            noneN = t;
                        } else if (t < noneN) {
                            noneN = t;
                        }
                    }
                }
                if (line.contains("C")) {
                    Integer c = line.lastIndexOf("C");
                    if (c != -1) {
                        if (noneN == -1) {
                            noneN = c;
                        } else if (c < noneN) {
                            noneN = c;
                        }
                    }
                }
                if (line.contains("G")) {
                    Integer g = line.lastIndexOf("G");
                    if (g != -1) {
                        if (noneN == -1) {
                            noneN = g;
                        } else if (g < noneN) {
                            noneN = g;
                        }
                    }
                }

                if (noneN != -1) {
                    lastNoneN = length + noneN;
                }

                // measure sequence length
                length += line.length();

                //read next line
                count = cis.getByteCount();
                line = reader.readLine();
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
            }
        } catch (IOException ioEx) {
        } finally {
            reading = false;
        }
    }

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
     * @throws IOException in case of IO error
     */
    public Integer countMotif(
            Location loc,
            String motif
    ) throws IOException {
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
                    line.trim();
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
            }
        } catch (IOException ioEx) {
        }
        return 0;
    }

    /**
     * Count number of motifs.
     *
     * @param loc location
     * @param motif motif to search for
     * @return number of motif occurrences found
     * @throws IOException in case of IO error
     */
    public Integer countMotifOrig(Location loc, String motif) throws IOException {
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
                String seq = "";
                Integer read = 0;
                String line = bufferedReader.readLine();
                while (line != null && !line.startsWith(">") && read < loc.getLength()) {
                    line.trim();
                    line = line.toUpperCase();
                    if ((read + line.length()) <= loc.getLength()) {
                        read += line.length();
                        seq += line;
                    } else {
                        Integer remaining = loc.getLength() - read;
                        line = line.substring(0, remaining);
                        read += remaining;
                        seq += line;
                    }
                    while (seq.length() >= motif.length()) {
                        if (seq.substring(0, motif.length()).equals(motif)) {
                            counter++;
                        }
                        seq = seq.substring(1);
                    }
                    line = bufferedReader.readLine();
                }
                return counter;
            }
        } catch (IOException ioEx) {
        }
        return 0;
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
            seqStart = new HashMap<String, Integer>();
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
            seqLengths = new HashMap<String, Integer>();
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
            lineLength = new HashMap<String, Integer>();
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
