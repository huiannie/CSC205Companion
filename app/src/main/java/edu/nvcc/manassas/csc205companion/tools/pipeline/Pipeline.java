/*
 * Copyright (c) 2015,2016 Annie Hui @ NVCC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.nvcc.manassas.csc205companion.tools.pipeline;

import android.graphics.Color;

import edu.nvcc.manassas.csc205companion.app.AppSettings;


public class Pipeline {
    private static final String TAG = Pipeline.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private static final int overheadColor = Color.parseColor("#ff8800");
    private static final int colors[] = { Color.parseColor("#ffd7d1"), Color.parseColor("#bffdfb"),
            Color.parseColor("#fefbb5"), Color.parseColor("#e6ccfe"), Color.parseColor("#e5fcb3")};

    public static final int DEFAULT_MIN_STAGES = 2;  // minimum 2 stages to make a pipeline
    public static final int DEFAULT_MAX_STAGES = colors.length;
    public static final double DEFAULT_MINIMUM = 0d;
    public static final double DEFAULT_MAXIMUM = 1d;


    private int numberOfStages;
    private int numberOfMarkers;  // use (m-1) markers to partition a job into m stages. Each stage performs one subtask.
    private final double stageRelativeWidth;  // only depends on the numberOfStages. For plotting only
    private double markers[];
    private double stageUtilization[];  // fraction of a stage that is utilized (or busy). This includes both the fixed overhead and the variable job-dependent component
    private double maxSubtaskTime = 0d; // depends on the longest subtask
    private double overhead = DEFAULT_MINIMUM;  // a value between 0 and 1, that is a fraction of the length of the job. A pipeline is not effective when overhead is 1.

    public Pipeline() {
        this(DEFAULT_MIN_STAGES);
    }

    public Pipeline(int numberOfStages) {
        if (numberOfStages<DEFAULT_MIN_STAGES)
            this.numberOfStages = DEFAULT_MIN_STAGES;
        else if (numberOfStages>DEFAULT_MAX_STAGES)
            this.numberOfStages = DEFAULT_MAX_STAGES;
        else
            this.numberOfStages = numberOfStages;

        // now create the stages. Starting with uniform distribution

        this.numberOfMarkers = this.numberOfStages-1;

        this.stageUtilization = new double[this.numberOfStages];
        this.markers = new double[this.numberOfMarkers];

        this.stageRelativeWidth = (DEFAULT_MAXIMUM - DEFAULT_MINIMUM) / this.numberOfStages;

        for (int pos=0; pos<this.numberOfMarkers; pos++) {
            this.markers[pos] = this.stageRelativeWidth *(pos+1);
        }

        calibratePipeline();
    }


    public Pipeline(double markers[], double overhead) {
        this.numberOfMarkers = markers.length;
        this.numberOfStages = markers.length + 1;

        if (numberOfStages<DEFAULT_MIN_STAGES)
            this.numberOfStages = DEFAULT_MIN_STAGES;
        else if (numberOfStages>DEFAULT_MAX_STAGES)
            this.numberOfStages = DEFAULT_MAX_STAGES;

        if (overhead>DEFAULT_MAXIMUM) overhead = DEFAULT_MAXIMUM;
        else if (overhead<DEFAULT_MINIMUM) overhead = DEFAULT_MINIMUM;
        this.overhead = overhead;

        this.stageUtilization = new double[this.numberOfStages];
        this.markers = new double[this.numberOfMarkers];

        this.stageRelativeWidth = (DEFAULT_MAXIMUM - DEFAULT_MINIMUM) / this.numberOfStages;

        // only accept the first ones that are within range
        for (int pos=0; pos<this.numberOfMarkers; pos++) {
            this.markers[pos] = markers[pos];
        }

        calibratePipeline();
    }


    public int getNumberOfStages() {
        return numberOfStages;
    }

    public int getNumberOfMarkers() {
        return numberOfMarkers;
    }

    public double getSubtaskTime(int stage) {
        if (stage<0 || stage>=numberOfStages) return 0;
        return getSubtaskTimeWithoutOverhead(stage) + overhead;  // include overhead as subtaskTime
    }

    public double getSubtaskTimeWithoutOverhead(int stage) {
        if (stage<0 || stage>=numberOfStages) return 0;

        double startMarker = 0d;
        double endMarker = 0d;
        double netSubtaskTime;

        if (stage==0) {
            startMarker = DEFAULT_MINIMUM;
            endMarker = markers[stage];
        }
        else if (stage==numberOfMarkers) {
            startMarker = markers[stage-1];
            endMarker = DEFAULT_MAXIMUM;
        }
        else if (stage>0 && stage<numberOfMarkers) {
            startMarker = markers[stage-1];
            endMarker = markers[stage];
        }
        netSubtaskTime = endMarker - startMarker;
        return netSubtaskTime;
    }

    public double getStageMinUtilization() {
        if (maxSubtaskTime >0)
            return overhead / maxSubtaskTime;
        else
            return DEFAULT_MINIMUM;
    }

    public double getStageUtilization(int stage) {
        if (stage<0 || stage>=numberOfStages) return 0;
        return stageUtilization[stage];
    }

    public double getMarker(int pos) {
        if (pos<0 || pos>=markers.length) return 0;
        else return markers[pos];
    }
    public int getColor(int stage) {
        if (stage<0 || stage>=numberOfStages) return 0;
        return colors[stage];
    }
    public int getOverheadColor() {
        return overheadColor; // the last one is the color of overhead
    }

    public double getStageRelativeWidth() {
        return stageRelativeWidth;
    }

    public double[] getMarkers() {
        return markers;
    }

    public void setMarker(int pos, double value) {
        if (pos<0 || pos>=markers.length) return;  // do nothing

        if (value>DEFAULT_MAXIMUM) value = DEFAULT_MAXIMUM;
        else if (value<DEFAULT_MINIMUM) value = DEFAULT_MINIMUM;

        double lowerLimit;
        double upperLimit;

        if (numberOfMarkers==1) {
            lowerLimit = DEFAULT_MINIMUM;
            upperLimit = DEFAULT_MAXIMUM;
        }
        else {
            if (pos==0) {
                lowerLimit = DEFAULT_MINIMUM;
                upperLimit = markers[pos+1];
            }
            else if (pos==numberOfMarkers-1) {
                lowerLimit = markers[pos-1];
                upperLimit = DEFAULT_MAXIMUM;
            }
            else {
                lowerLimit = markers[pos-1];
                upperLimit = markers[pos+1];
            }
        }

        // Set the current marker to be no more than its upper neighbor and no less than its lower neighbor
        markers[pos] = Math.max(Math.min(value, upperLimit), lowerLimit);

        // Once the marker is changed, the pipeline utilization needs to be re-calibrated
        calibratePipeline();
    }


    public void setOverhead(double value) {
        if (value>DEFAULT_MAXIMUM) value = DEFAULT_MAXIMUM;
        else if (value<DEFAULT_MINIMUM) value = DEFAULT_MINIMUM;
        overhead = value;
        // Once the overhead changes, the pipeline utilization needs to be re-calibrated
        calibratePipeline();
    }

    private void calibratePipeline() {

        double subtaskTime[] = new double[numberOfStages];

        maxSubtaskTime = 0d; // reset

        // Go through all the subtasks, use the start marker and the end marker to determine the length of the subtask
        for (int stage=0; stage<numberOfStages; stage++) {
            subtaskTime[stage] = getSubtaskTime(stage);
            if (subtaskTime[stage]> maxSubtaskTime)
                maxSubtaskTime = subtaskTime[stage];
        }

        for (int stage=0; stage<numberOfStages; stage++) {
            stageUtilization[stage] = subtaskTime[stage] / maxSubtaskTime;
        }
    }



    /*
     * Get statistics
     * 1. Without pipeline:
     * - One instruction takes time T=1
     * - N instructions take time N*T=N
     *
     * 2. With k-stage pipeline (in the ideal case, overhead=0):
     * - Longest stage is b, where b can range from T/k to T
     * - One instruction takes time k*b, which can range from T to T*k
     * - N instructions take time k*b+(N-1)b, which can range from T+(N-1)b to T*k+(N-1)b
     *
     * 3. With k-stage pipeline (in the less-than-ideal case, overhead>0):
     * - Longest stage is b, where b can range from T/k+overhead to T+overhead
     * - One instruction takes time k*b.
     * - N instructions take time k*b+(N-1)b.
     *
     * Speedup at N->inf = T/b
     */

    public double getStageTime() {
        return maxSubtaskTime;
    }
    public double getTsequential() {
        // For one instruction
        return DEFAULT_MAXIMUM;
    }
    public double getTpipelined() {
        // For one instruction
        return maxSubtaskTime *numberOfStages;
    }

    public double getNTsequential(int N) {
        if (N<1) N=1;
        return N*getTsequential();
    }
    public double getNTpipelined(int N) {
        if (N<1) N=1;
        return getTpipelined() + (N-1)* getStageTime();
    }
    public double getSpeedup(int N) {
        if (N<1) N=1;
        double NTsequential = getNTsequential(N);
        double NTpipelined = getNTpipelined(N);
        return NTsequential / NTpipelined;
    }
    public double getSpeedupInf() {
        return getTsequential() / getStageTime();
    }

    public double getOverhead() {
        return overhead;
    }

    public String getStatistics(int N) {
        if (N<1) return ""; // No statistics
        double stageTime = getStageTime(); // already included overhead
        double stageOverhead = getOverhead();

        double Tsequential = getTsequential();
        double Tpipelined = getTpipelined();

        double NTsequential = getNTsequential(N);
        double NTpipelined = getNTpipelined(N);

        double SpeedupN = getSpeedup(N);
        double SpeedupInf = getSpeedupInf();

        String data = "";

        data += "Sequential T=" + String.format("%.2f", Tsequential) + "\n";
        for (int stage=0; stage<numberOfStages; stage++) {
            data += "subtask " + (stage+1) + " = " + String.format("%.2f", getSubtaskTimeWithoutOverhead(stage)) + "\n";
        }
        data += "At N=" + N + ", total time=" + String.format("%.2f", NTsequential) + "\n";

        data += "\n";

        data += "Pipelined T=" + String.format("%.2f", Tpipelined) + "\n";
        data += "stage length: " + String.format("%.2f", stageTime) + " with overhead=" + String.format("%.2f", stageOverhead) + "\n";
        data += "At N=" + N + ", total time=" + String.format("%.2f", NTpipelined) + "\n";

        data += "\n";

        data += "Pipeline speedup at N=" + N + " is " + String.format("%.2f", SpeedupN) + "\n";
        data += "Pipeline speedup as N->inf is " + String.format("%.2f", SpeedupInf) + "\n";
        return data;
    }
}
