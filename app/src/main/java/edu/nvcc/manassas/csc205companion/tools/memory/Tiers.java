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

package edu.nvcc.manassas.csc205companion.tools.memory;

import android.graphics.Color;

import edu.nvcc.manassas.csc205companion.app.AppSettings;
import edu.nvcc.manassas.csc205companion.io.Savelog;


public class Tiers {
    private static final String TAG = Tiers.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;


    /*
     * For average access time
     * EATs = h*T1 + (1-h)*(T1+T2)    .... sequential
     * EATp = h*T1 + (1-h)*T2         .... parallel
     *
     * For efficiency
     * Effs = T1 / EATs = 1 / (1 + (1-h)*T2/T1)  .... sequential
     * Effp = T1 / EATp = 1 / (h + (1-h)*T2/T1)  .... parallel
     *
     * For h:
     * hs = (T1 + T2 - EATs) / T2    .... sequential
     * hp = (EATp - T2) / (T1 - T2)  .... parallel
     *
     * The point of inflection is useful for plotting the function Eff.
     * Instead of sampling Eff uniformly at regular intervals of h,
     * partition Eff into two parts using the h at the point of inflection.
     * Sample each part with equal number of intervals.
     * This approach provides higher accuracy than the uniform sampling
     * as the function Eff changes much more rapidly after the point of
     * inflection.
     *
     * To find h at the point of inflection of Eff (where d(Eff)/d(h) = 1
     *
     * d(Effs)/d(hs) = (T2/T1) / (1+(1-hs)*T2/T1)^2
     * When d(Effs)/d(hs) = 1, hs = ( T2/T1 - sqrt(T2/T1) + 1 ) / (T2/T1)  .... sequential
     *
     * d(Effp)/d(hp) = (T2/T1-1) / (hp+(1-hp)*T2/T1)^2
     * When d(Effp)/d(hp) = 1, hp = ( sqrt(T2/T1-1) - T2/T1 ) / (1 - T2/T1)  .... parallel
     */


    // Need to use color of chart to distinguish the different model type.
    private static final int colors[] = { Color.parseColor("#009ec2"), Color.parseColor("#608b32"), Color.BLACK };

    public static final int type_parallel = 101;
    public static final int type_sequential = 102;
    public static final int type_default = type_sequential;

    public static final double MAX_H = 1d;
    public static final double MIN_H = 0d;
    public static final double MAX_T1_TO_T2_RATIO = 0.8d;
    public static final double MIN_T1_TO_T2_RATIO = 0.000001d;

    public static final double tolerance = 0.00001; // number of decimal digits required for accuracy is 4

    public static final double DEFAULT_T2 = 100d;
    public static final double DEFAULT_T1 = DEFAULT_T2 * 0.5; // default is half of T2
    public static final double DEFAULT_H = 0.5;

    private double maxT1;    // use this for both sequential and parallel
    private double minT1;   // use this for both sequential and parallel

    private double T1 = DEFAULT_T1;
    private double T2 = DEFAULT_T2;
    private double h = DEFAULT_H;

    private int type = type_default; // default type


    public Tiers() {
        this(type_default, DEFAULT_T1, DEFAULT_T2, DEFAULT_H);
    }

    public Tiers(int type, double T1, double T2, double h) {
        if (type==type_parallel || type==type_sequential)
            this.type = type;

        setH(h);
        setT2(T2); // must do this before setting T1
        setT1(T1);
    }

    private void setT1Bounds() {
        this.maxT1 = T2*MAX_T1_TO_T2_RATIO;
        this.minT1 = T2*MIN_T1_TO_T2_RATIO;
        Savelog.d(TAG, debug, "Tier Type=" + type + " T2= " + T2 + " T1=" + this.T1);
    }

    public void setType(int type) {
        if (type!=type_parallel && type!=type_sequential) type = type_default;
        this.type = type;
        setT1Bounds();
        if (T1<=minT1) T1 = minT1;
        else if (T1>=maxT1) T1 = maxT1;
    }
    public int getType() {
        return type;
    }

    public boolean isValidT2(double T2) {
        if (T2>0) return true;
        else return false;
    }
    public boolean isValidT1(double T1) {
        if (T1>=minT1 && T1<=maxT1) return true;
        else {
            double lowDiff = Math.abs(T1-minT1)/minT1;
            if (lowDiff<=tolerance) return true; // slightly off from lower limit
            double highDiff = Math.abs(T1-maxT1)/maxT1;
            if (highDiff<=tolerance) return true; // slightly off from upper limit
            return false;
        }
    }
    public boolean isValidH(double h) {
        if (h>=MIN_H && h<=MAX_H) return true;
        else return false;
    }

    public void setT2(double T2) {
        if (T2<=0) T2 = DEFAULT_T2;
        this.T2 = T2;
        setT1Bounds();
        if (T1<minT1) T1 = minT1;
        else if (T1>maxT1) T1 = maxT1;
    }


    public void setT1(double T1) {
        Savelog.d(TAG, debug, "trying to set T1 as " + T1);
        if (T1<=this.minT1) T1 = this.minT1;
        else if (T1>=this.maxT1) T1 = this.maxT1;

        Savelog.d(TAG, debug, "finally setting T1 as " + T1);
        this.T1 = T1;
    }

    public void setH(double h) {
        if (h<MIN_H) h=MIN_H;
        else if (h>MAX_H) h=MAX_H;
        this.h = h;
    }

    public int getTypeColor() {
        if (type==type_parallel) return colors[0];
        else if (type==type_sequential) return colors[1];
        else return colors[2];
    }

    public double getMinT1() { return minT1; }
    public double getMaxT1() { return maxT1; }
    public double getT1() { return T1; }
    public double getT2() { return T2; }
    public double getH() {
        return h;
    }
    public double getEAT() {
        return getEAT(this.h);
    }
    public double getEfficiency() { return getEfficiency(this.h); }

    public double getMinT1EATratio() {
        if (this.type==type_parallel) {
            return MIN_T1_TO_T2_RATIO;
        }
        else {
            return MIN_T1_TO_T2_RATIO / (1.0+MIN_T1_TO_T2_RATIO);
        }
    }
    public double getMaxT1EATratio() {
        if (this.type==type_parallel) {
            return MAX_T1_TO_T2_RATIO;
        }
        else {
            return MAX_T1_TO_T2_RATIO / (1.0+MAX_T1_TO_T2_RATIO);
        }
    }

    public double getT1EATratio() {
        if (type==type_parallel) return T1 / T2;
        else if (type==type_sequential) return T1/(T1+T2);
        return 0.0;
    }

    public double getT2EATratio() {
        if (type==type_parallel) return T2 / T2;
        else if (type==type_sequential) return T2/(T1+T2);
        else return 0.0;
    }



    private double effInflexPoint() {
        double ratio = T2/T1;
        // Compute the value of h at which the curve has gradient 1
        if (type==type_parallel)
            return (Math.sqrt(ratio-1.0) - ratio) / (1 - ratio);
        else if (type==type_sequential)
            return 1 - ((Math.sqrt(ratio) - 1) / ratio);
        else
            return 0.5;  // guess the middle
    }

    public double getH(double efficiency) {
        if (efficiency<=getEfficiency(0)) return 0.0;
        else if (efficiency>=1.0) return 1.0;
        else {
            // recover h for the given efficiency
            double EAT = T1 / efficiency;
            if (type==type_parallel) {
                return (T2-EAT)/(T2-T1);
            }
            else if (type==type_sequential) {
                return  (T1 + T2 - EAT)/(T2);
            }
            else return 0.0;
        }
    }



    public double getEAT(double h) {
        if (type==type_parallel) {
            return h * T1 + (1 - h) * T2;
        }
        else if (type==type_sequential) {
            return h * T1 + (1 - h) * (T1 + T2);
        }
        else return 0.0;
    }



    public double getEfficiency(double h) {
        return T1 / getEAT(h);
    }

    public double[] HSamplesUniform(int count) {
        if (count<1) return null;
        double hsamples[] = new double[count];

        int panels = count-1;
        // no need to use point of inflexion
        double panelWidth = 1.0/panels;
        for (int index=0; index<count; index++) {
            hsamples[index] = (double)index*panelWidth;
        }
        return hsamples;
    }

    public double[] HSamplesByReflexionOnEff(int count) {
        if (count<1) return null;
        double hsamples[] = new double[count];

        double h_inflex = effInflexPoint();
        Savelog.d(TAG, debug, "h_inflex=" + h_inflex);

        int panels = count-1;

        if (h_inflex<1) {
            boolean even = (panels%2==0);
            if (even) {  // point of inflexion will be one sample point
                for (int index=0; index<panels/2; index++) {
                    double h = (double) index*(h_inflex/panels*2);
                    hsamples[index] = h;
                }
                hsamples[panels/2] = h_inflex;

                for (int index=panels/2+1; index<count; index++) {
                    int subIndex = index - (panels/2+1) + 1;
                    double h = h_inflex + (double) subIndex*((1-h_inflex)/panels*2);
                    hsamples[index] = h;
                }
            }
            else {
                // Odd number of panels. One panel has the point of inflexion as its mid-point

                int halfPanels = panels/2;
                double halfPanelWidth1 = h_inflex/(halfPanels*2+1);
                for (int index=0; index<=halfPanels; index++) {
                    double h = (double) index*(halfPanelWidth1*2);
                    hsamples[index] = h;
                }


                double halfPanelWidth2 = (1-h_inflex)/(halfPanels*2+1);
                hsamples[halfPanels+1] = h_inflex + halfPanelWidth2;

                for (int index=halfPanels+2; index<count; index++) {
                    int subIndex = index - halfPanels-1;
                    double h = h_inflex + halfPanelWidth2 + (double) subIndex*(halfPanelWidth2*2);
                    hsamples[index] = h;
                }
            }
        }
        else {
            // no need to use point of inflexion
            double panelWidth = 1.0/panels;
            for (int index=0; index<count; index++) {
                hsamples[index] = (double)index*panelWidth;
            }
        }

        return hsamples;
    }


    public double getT1FromEATratio(double t1EATratio) {
        if (type==type_parallel) {
            return t1EATratio * T2;
        }
        else if (type==type_sequential) {
            return T2 * t1EATratio / (1 - t1EATratio);
        }
        return 0.0;
    }

    public String getDescription() {
        String data = "";
        data += (type==type_parallel ? "parallel access" : "sequential access");
        data += ": " + "T1=" + T1;
        data += ", " + "T2=" + T2;
        data += ", " + "h=" + h;
        data += "\n" + "EAT=" + getEAT() + ",";
        data += " " + "Eff=" + getEfficiency();
        return data;
    }
}
