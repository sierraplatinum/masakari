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
package biovis.hackebeil.server.worker.motifWorker;

import biovis.hackebeil.common.data.Motif;

/**
 *
 * @author zeckzer
 */
public class MotifChars {

    private Motif motif;

    private char[] motifCharsUpperCase;
    private char[] motifCharsLowerCase;

    private char[] motifCharsRCUpperCase;
    private char[] motifCharsRCLowerCase;

    /**
     *
     * @param motif
     */
    public MotifChars(Motif motif) {
        this.motif = motif;
        motifCharsUpperCase = motif.getMotif().toUpperCase().toCharArray();
        motifCharsLowerCase = motif.getMotif().toLowerCase().toCharArray();
        motifCharsRCUpperCase = reverseComplement(motifCharsUpperCase);
        motifCharsRCLowerCase = reverseComplement(motifCharsLowerCase);
    }

    /**
     *
     * @param orig
     * @return
     */
    public static char[] reverseComplement(char[] orig) {
        char[] result = new char[orig.length];

        for (int current = 0; current < result.length; ++current) {
            result[current] = reverseComplement(orig[orig.length - 1 - current]);
        }

        return result;
    }

    /**
     *
     * @param orig
     * @return
     */
    public static char reverseComplement(char orig) {
        switch (orig) {
            case 'A':
                return 'T';
            case 'a':
                return 't';
            case 'T':
                return 'A';
            case 't':
                return 'a';
            case 'C':
                return 'G';
            case 'c':
                return 'g';
            case 'G':
                return 'C';
            case 'g':
                return 'c';
            default:
                return orig;
        }
    }

    public boolean getIsNormalized() {
        return motif.getIsNormalized();
    }

    public String getMotifId() {
        return motif.getMotifId();
    }

    public int length() {
        return motif.getMotif().length();
    }

    public char[] getMotifCharsUpperCase() {
        return motifCharsUpperCase;
    }

    public char[] getMotifCharsLowerCase() {
        return motifCharsLowerCase;
    }

    public char[] getMotifCharsRCUpperCase() {
        return motifCharsRCUpperCase;
    }

    public char[] getMotifCharsRCLowerCase() {
        return motifCharsRCLowerCase;
    }
}
