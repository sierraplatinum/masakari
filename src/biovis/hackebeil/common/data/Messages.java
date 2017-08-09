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

/**
 *
 * @author zeckzer
 */
public class Messages {

    public static final String SERVER_setClient = "setClient";
    public static final String SERVER_credentials = "credentials";
    public static final String SERVER_clear = "clear";
    public static final String SERVER_createIndex = "createIndex";
    public static final String SERVER_loadIndex = "loadIndex";
    public static final String SERVER_startSegmentation = "startSegmentation";
    public static final String SERVER_addAdditionalData = "addAdditionalData";
    public static final String SERVER_addMotif = "addMotif";
    public static final String SERVER_addPWM = "addPWM";
    public static final String SERVER_startCorrelation = "startCorrelation";
    public static final String SERVER_startFateOfCodeComputation = "startFateOfCodeComputation";
    public static final String SERVER_exportSegmentation = "exportSegmentation";
    public static final String QUIT = "QUIT";

    public static final String CLIENT_ECHO = "ECHO";
    public static final String CLIENT_BUSY = "BUSY";
    public static final String CLIENT_DONE = "STATE DONE";
    public static final String CLIENT_IndexProgress = "[INDEXWORKER] progress";
    public static final String CLIENT_Segmentation = "[Segmentation] results";
    public static final String CLIENT_SegmentationCodeCounts = "[SegmentationCodeCounts] results";
    public static final String CLIENT_SegmentationBreakSegments = "[SegmentationBreakSegments] result";
    public static final String CLIENT_SegmentationSegmentPairs = "[SegmentationSegmentPairs] result";
    public static final String CLIENT_SegmentationSegmentShortSegmentPairs = "[SegmentationSegmentShortSegmentPairs] result";
    public static final String CLIENT_SegmentationShortSegmentSegmentPairs = "[SegmentationShortSegmentSegmentPairs] result";
    public static final String CLIENT_SegmentationSegmentPairOccurrence = "[SegmentationPairRelation] result";
    public static final String CLIENT_SegmentationShortSegmentChainsCounts = "[SegmentationShortSegmentChainsCounts] result";
    public static final String CLIENT_SegmentationShortSegmentChainsLengths = "[SegmentationShortSegmentChainsLengths] result";
    public static final String CLIENT_SegmentationDroppedPeaks = "[SegmentationDroppedPeaks] result";
    public static final String CLIENT_AdditionalData = "[ADDITIONALDATAWORKER] results";
    public static final String CLIENT_Motifs = "[MOTIFWORKER] results";
    public static final String CLIENT_PWM = "[PWMWORKER] results";
    public static final String CLIENT_SegmentationLength = "[SegmentationLengthWorker] result";
    public static final String CLIENT_Correlation = "[CorrelationWorker] results";
    public static final String CLIENT_FateOfCode = "[FateOfCodeWorker] results";
}
