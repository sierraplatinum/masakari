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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author zeckzer
 */
public class PositionWeightMatrix
    extends MotifBase {

    private static final double LOG2 = Math.log(2);

    public static final int COMPUTATION_METHOD_MEDIAN = 1;
    public static final int COMPUTATION_METHOD_MAX = 2;
    public static final int COMPUTATION_METHOD_CUTOFF = 3;

    public static final int PWM_COUNT = 1;
    public static final int PWM_RELATIVE = 2;
    public static final int PWM_LOG = 3;

    private String filePathValue;
    private String dataSetNameValue;

    private Map<Character, List<Double>> pwm;
    private Map<Character, List<Double>> pwmInverseComplement;
    private int numberOfCharacters;
    private int length;
    private int computationMethod = COMPUTATION_METHOD_MEDIAN;
    private double cutoffValue = 1.0;

    /**
     *
     */
    public PositionWeightMatrix() {
        pwm = new HashMap<>();
        pwmInverseComplement = new HashMap<>();
    }

    /**
     *
     * @param filePathValue
     * @param dataSetNameValue
     */
    public void setPWM(
        String filePathValue,
        String dataSetNameValue
    ) {
        this.filePathValue = filePathValue;
        this.dataSetNameValue = dataSetNameValue;
    }

    /**
     *
     */
    public void loadPwm() {
        if (filePathValue.endsWith(".mat")) {
            loadPwmMat(filePathValue);
        }
    }

    /**
     *
     * @param filePathValue
     */
    private void loadPwmMat(
        String filePathValue
    ) {
        List<Character> characterList = new ArrayList<>();

        List<List<Double>> weights = new ArrayList<>();
        List<Double> sums = new ArrayList<>();

        try (FileReader reader = new FileReader(new File(filePathValue));
             BufferedReader br = new BufferedReader(reader)) {
            String line = br.readLine();

            // Skip header
            while (line != null
                   && !line.startsWith("P0")) {
                line = br.readLine();
            }

            // Extract characters
            if (line != null && line.startsWith("P0")) {
                StringTokenizer strTok = new StringTokenizer(line);
                strTok.nextToken();
                while (strTok.hasMoreElements()) {
                    characterList.add(strTok.nextToken().charAt(0));
                }
                line = br.readLine();
            }
            numberOfCharacters = characterList.size();

            // Collect weights
            for (int i = 0; i < numberOfCharacters; ++i) {
                weights.add(new ArrayList<>());
            }
            this.length = weights.size();

            double frequency;
            double frequencySum;
            while (line != null
                   && !line.startsWith("XX")) {
                StringTokenizer strTok = new StringTokenizer(line);
                strTok.nextToken();

                frequencySum = 0.0;
                for (int i = 0; i < numberOfCharacters; ++i) {
                    frequency = Double.valueOf(strTok.nextToken());
                    weights.get(i).add(frequency);
                    frequencySum += frequency;
                }
                sums.add(frequencySum);

                line = br.readLine();
            }
        } catch (FileNotFoundException fnfEx) {
            fnfEx.printStackTrace();
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }

        computeProbabilities(weights, sums);
        /*
        for (List<Double> wl : weights) {
            print(wl);
        }
        System.out.println("========================================");
         */
        logTransform(weights);
        /*
        for (List<Double> wl : weights) {
            print(wl);
        }
         */

        List<Double> weightList;
        for (int i = 0; i < numberOfCharacters; ++i) {
            weightList = weights.get(i);
            List<Double> reverseWeights = new ArrayList<>();
            for (int currentWeight = weights.size() - 1; currentWeight >= 0; --currentWeight) {
                reverseWeights.add(weightList.get(currentWeight));
            }

            switch (characterList.get(i)) {
                case 'A':
                    pwm.put('a', weightList);
                    pwm.put('A', weightList);
                    pwmInverseComplement.put('t', reverseWeights);
                    pwmInverseComplement.put('T', reverseWeights);
                    break;
                case 'C':
                    pwm.put('c', weightList);
                    pwm.put('C', weightList);
                    pwmInverseComplement.put('g', reverseWeights);
                    pwmInverseComplement.put('G', reverseWeights);
                    break;
                case 'T':
                    pwm.put('t', weightList);
                    pwm.put('T', weightList);
                    pwmInverseComplement.put('a', reverseWeights);
                    pwmInverseComplement.put('A', reverseWeights);
                    break;
                case 'G':
                    pwm.put('g', weightList);
                    pwm.put('G', weightList);
                    pwmInverseComplement.put('c', reverseWeights);
                    pwmInverseComplement.put('C', reverseWeights);
                    break;
                default:
                    ;
            }
        }
        /*
        print(pwm);
        */
    }

    private void print(Map<Character, List<Double>> pwm) {
        List<Double> weights;
        for (Character character : pwm.keySet()) {
            weights = pwm.get(character);
            System.out.print(character + ":");
            print(weights);
        }
    }

    private void print(List<Double> weights) {
        for (Double weight : weights) {
            System.out.print(" " + weight);
        }
        System.out.println();
    }

    /**
     *
     * @param weights
     * @param sums 
     */
    private void computeProbabilities(
        List<List<Double>> weights,
        List<Double> sums
    ) {
        for (int position = 0; position < weights.size(); position++) {
            List<Double> weightList = weights.get(position);
            for (int character = 0; character < weightList.size(); ++character) {
                if (weightList.get(character) > 0.0) {
                    weightList.set(character, weightList.get(character) / sums.get(character));
                }
            }
        }
    }

    /**
     *
     * @param weights
     */
    private void logTransform(
        List<List<Double>> weights
    ) {
        int alphabetSize = weights.size();
        for (List<Double> weightList : weights) {
            for (int character = 0; character < weightList.size(); ++character) {
                if (weightList.get(character) != 0.0) {
                    weightList.set(character, Math.log(weightList.get(character) * alphabetSize) / LOG2);
                } else {
                    weightList.set(character, Double.NEGATIVE_INFINITY);
                }
            }
        }
    }

    public String getFilePathValue() {
        return filePathValue;
    }

    public String getPWMName() {
        return dataSetNameValue;
    }

    public int getComputationMethod() {
        return computationMethod;
    }

    public void checkNormalization() {
        switch (computationMethod) {
            case COMPUTATION_METHOD_MAX:
            case COMPUTATION_METHOD_MEDIAN:
                setIsNormalized(false);
                break;
            default:
        }
    }

    public void setComputationMethod(int computationMethod) {
        this.computationMethod = computationMethod;
    }

    public double computePWM(
        char[] segment
    ) {
        switch (computationMethod) {
            case COMPUTATION_METHOD_MEDIAN:
                return computePWMMedian(segment);
            case COMPUTATION_METHOD_MAX:
                return computePWMMax(segment);
            case COMPUTATION_METHOD_CUTOFF:
                long count = computePWMCutoff(segment);
                if (isNormalizedValue) {
                    long theoCount = segment.length - pwm.size() + 1;
                    return ((double) count) / (((double) theoCount) * 2);
                } else {
                    return count;
                }
            default:
                return 0.0;
        }
    }

    private double computePWMMedian(
        char[] segment
    ) {
        int numberOfValues = segment.length - length;
        if (numberOfValues < 1) {
            System.err.println("Segment is shorter than the PWM: " + segment.length + " < " + length);
            return -Double.MAX_VALUE;
        }

        double median = 0.0;
        int totalNumberOfValues = numberOfValues * 2;
        int numberOfNulls = 0;
        double[] result = new double[totalNumberOfValues];

        for (int start = 0; start < numberOfValues; ++start) {
            result[start] = computeSingleValue(pwm, segment, start);
            if (result[start] == Double.NEGATIVE_INFINITY) {
                ++numberOfNulls;
            }
            result[start + numberOfValues] = computeSingleValue(pwmInverseComplement, segment, start);
            if (result[start + numberOfValues] == Double.NEGATIVE_INFINITY) {
                ++numberOfNulls;
            }
        }

        Arrays.sort(result);

        int nonNulls = totalNumberOfValues - numberOfNulls;
        try {
            if (nonNulls > 0) {
                if (nonNulls % 2 == 0) {
                    median = (result[numberOfNulls + nonNulls / 2 - 1] + result[numberOfNulls + nonNulls / 2]) / 2.0;
                } else {
                    median = result[numberOfNulls + nonNulls / 2];
                }
                return median;
            } else {
                return -Double.MAX_VALUE;
            }
        } catch (Exception ex) {
            System.out.println("Exception thrown: " + ex);
            System.out.println(numberOfNulls + "|" + nonNulls + "|" + totalNumberOfValues + "|" + result.length);
            System.out.println((nonNulls / 2));
            System.out.println((numberOfNulls + nonNulls / 2 - 1) + "|" + +(numberOfNulls + nonNulls / 2));
            return -Double.MAX_VALUE;
        }
    }

    private double computePWMMax(
        char[] segment
    ) {
        double max = -Double.MAX_VALUE;
        int numberOfValues = segment.length - length;

        max = computePWMMaxLoop(numberOfValues, segment, max, pwm);
        max = computePWMMaxLoop(numberOfValues, segment, max, pwmInverseComplement);

        return max;
    }

    private double computePWMMaxLoop(
        int numberOfValues,
        char[] segment,
        double max,
        Map<Character, List<Double>> pwm
    ) {
        double result;
        for (int start = 0; start < numberOfValues; ++start) {
            result = computeSingleValue(pwm, segment, start);
            if (result != Double.NEGATIVE_INFINITY) {
                if (result > max) {
                    max = result;
                }
            }
        }
        return max;
    }

    private int computePWMCutoff(
        char[] segment
    ) {
        int count = 0;
        int numberOfValues = segment.length - length;

        count = computePWMCutoffLoop(numberOfValues, segment, count, pwm);
        count = computePWMCutoffLoop(numberOfValues, segment, count, pwmInverseComplement);

        return count;
    }

    private int computePWMCutoffLoop(
        int numberOfValues,
        char[] segment,
        int count,
        Map<Character, List<Double>> pwm
    ) {
        double result;
        for (int start = 0; start < numberOfValues; ++start) {
            result = computeSingleValue(pwm, segment, start);
            if (result != Double.NEGATIVE_INFINITY) {
                if (result > cutoffValue) {
                    ++count;
                }
            }
        }
        return count;
    }

    private double computeSingleValue(
        Map<Character, List<Double>> pwm,
        char[] segment,
        int start
    ) {
        double result = 0.0;
        List<Double> weightList;
        for (int position = 0; position < length; ++position) {
            weightList = pwm.get(segment[start + position]);
            if (weightList != null) {
                result += weightList.get(position);
            } else {
                return Double.NEGATIVE_INFINITY;
            }
        }

        /*
        if (result != Double.NEGATIVE_INFINITY && result < 0) {
            System.out.println("DZ: " + result +  " < 0 ");
            System.out.println("segment: " + String.valueOf(segment));
        }
        */
        return result;
    }

    public double getCutoffValue() {
        return cutoffValue;
    }

    public void setCutoffValue(double cutoffValue) {
        this.cutoffValue = cutoffValue;
    }

    /**
     *
     * @return motif identifier
     */
    @Override
    public String getMotifId() {
        String motifId = getPWMName();

        switch (computationMethod) {
            case COMPUTATION_METHOD_MEDIAN:
                motifId += "-Median";
                break;
            case COMPUTATION_METHOD_MAX:
                motifId += "-Max";
                break;
            case COMPUTATION_METHOD_CUTOFF:
                motifId += "-Cutoff";
                motifId += "-" + (isNormalizedValue ? "Density" : "Count");
                break;
            default:
                ;
        }
        return motifId;
    }
}
