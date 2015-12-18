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


import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.nvcc.manassas.csc205companion.R;
import edu.nvcc.manassas.csc205companion.app.AppSettings;
import edu.nvcc.manassas.csc205companion.io.Savelog;


public class Mem2LFragment extends Fragment {
    private static final String TAG = Mem2LFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private static final int InputType_H = 200;
    private static final int InputType_T1 = 201;
    private static final int InputType_T2 = 202;

    private static final int MaxLogNumberOfZeros = Mem2LCharts.MaxLogNumberOfZeros; // never reach this max because of mod.
    private boolean mLogscale = false;
    private int mLogNumberOfZeros = 0;
    private Tiers mTiers;

    private static final int buttonType_par = Tiers.type_parallel;
    private static final int buttonType_seq = Tiers.type_sequential;
    private static final int buttonType_scale = Tiers.type_parallel + Tiers.type_sequential + 1; // ensure uniqueness

    private EditText mT2View;
    private EditText mT1View;
    private EditText mHView;
    private TextView mAlertView;
    private String mAlertText = "";
    private Button mButtonP;
    private Button mButtonS;
    private Button mButtonScale;
    private String labelLog;
    private String labelLinear;


    private Mem2LCharts mCharts;

    public static Mem2LFragment newInstance() {
        Bundle args = new Bundle();

        Mem2LFragment fragment = new Mem2LFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        labelLog = getString(R.string.label_mem2L_logscaled);
        labelLinear = getString(R.string.label_mem2L_linear);

        mTiers = new Tiers(Tiers.type_sequential, Tiers.DEFAULT_T1, Tiers.DEFAULT_T2, Tiers.DEFAULT_H);
        setRetainInstance(true);
        setHasOptionsMenu(true);

    } // end to implementing onCreate()



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Savelog.d(TAG, debug, "onCreateView()");

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tool_mem2l, container, false);

        mCharts = new Mem2LCharts(getActivity(), mTiers);

        mCharts.setLogScaled(mLogscale);

        mCharts.setOnSeekBarChangeListener(new OnSeekbarChangedListener(this));
        mCharts.setNotifyWhileDragging(true);

        // Add to layout
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.fragmentToolMem2L_seekbarPlaceholder);
        layout.addView(mCharts);

        mT2View = (EditText) v.findViewById(R.id.fragmentToolMem2L_T2);
        mT2View.addTextChangedListener(new InputTextWatcher(this, InputType_T2));

        mT1View = (EditText) v.findViewById(R.id.fragmentToolMem2L_T1);
        mT1View.addTextChangedListener(new InputTextWatcher(this, InputType_T1));

        mHView = (EditText) v.findViewById(R.id.fragmentToolMem2L_h);
        mHView.addTextChangedListener(new InputTextWatcher(this, InputType_H));

        updateHView();
        updateT1View();
        updateT2View();

        mAlertView = (TextView) v.findViewById(R.id.fragmentToolMem2L_alert);
        updateAlertView(mAlertText);

        mButtonP = (Button) v.findViewById(R.id.fragmentToolMem2L_buttonPar);
        mButtonP.setOnClickListener(new OnButtonClickListener(this, buttonType_par));
        mButtonS = (Button) v.findViewById(R.id.fragmentToolMem2L_buttonSeq);
        mButtonS.setOnClickListener(new OnButtonClickListener(this, buttonType_seq));
        mButtonScale = (Button) v.findViewById(R.id.fragmentToolMem2L_buttonScale);
        mButtonScale.setOnClickListener(new OnButtonClickListener(this, buttonType_scale));
        setScaleButton(mLogNumberOfZeros);

        return v;
    }





    private void setScaleButton(int newLogNumberOfZeros) {
        Savelog.d(TAG, debug, "new number of zeros = " + newLogNumberOfZeros);
        if (newLogNumberOfZeros==0) {
            mLogscale = false;
            mLogNumberOfZeros = 0;
            mCharts.setLogScaled(false);
        }
        else {
            mLogscale = true;
            mLogNumberOfZeros = newLogNumberOfZeros;
            mCharts.setLogBase(newLogNumberOfZeros);
            mCharts.setLogScaled(true);
        }

        if (newLogNumberOfZeros==MaxLogNumberOfZeros-1) {
            // Rotate back to linear next time
            mButtonScale.setText(labelLinear);
        }
        else {
            // next time is still a log scale
            mButtonScale.setText(labelLog);
        }

    }

    private static class OnButtonClickListener implements View.OnClickListener {
        private Mem2LFragment hostFragment;
        private int type;

        public OnButtonClickListener(Mem2LFragment hostFragment, int type) {
            this.hostFragment = hostFragment;
            this.type = type;
        }

        @Override
        public void onClick(View v) {
            if (type==buttonType_par) {
                if (hostFragment.mTiers.getType() != type) {
                    hostFragment.mTiers.setType(type);
                    hostFragment.mCharts.refreshPointers();
                    Savelog.d(TAG, debug, "updating type=" + type);
                }
            }
            else if (type==buttonType_seq) {
                if (hostFragment.mTiers.getType() != type) {
                    hostFragment.mTiers.setType(type);
                    hostFragment.mCharts.refreshPointers();
                    Savelog.d(TAG, debug, "updating type=" + type);
                }
            }
            else if (type==buttonType_scale) {
                hostFragment.setScaleButton((hostFragment.mLogNumberOfZeros+1)%MaxLogNumberOfZeros);
            }
            else {
                // do nothing
            }
        }
    }



    private static class InputTextWatcher implements TextWatcher {
        Mem2LFragment hostFragment;
        int inputType;
        public InputTextWatcher(Mem2LFragment hostFragment, int inputType) {
            super();
            this.hostFragment = hostFragment;
            this.inputType = inputType;
        }
        @Override
        public void afterTextChanged(Editable arg0) {}
        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        @Override
        public void onTextChanged(CharSequence c, int start, int before, int count) {
            // Whenever there is a change, erase any error messages
            hostFragment.updateAlertView("");

            String data = "";
            if (c!=null && c.toString().trim().length()>0) {
                data = c.toString().trim();

                try {
                    Tiers tiers = hostFragment.mTiers;
                    double dataValue = Double.valueOf(data);
                    if (inputType==InputType_T2) {
                        if (tiers.isValidT2(dataValue)) {
                            if (tiers.getT2() != dataValue) {
                                tiers.setT2(dataValue);
                                hostFragment.mCharts.refreshPointers();
                                hostFragment.updateT1View(); // Changing T2 may affect T1
                            }
                        } else {
                            hostFragment.updateAlertView("T2 invalid");
                            // report invalid
                        }
                    }
                    else if (inputType==InputType_T1) {
                        if (tiers.isValidT1(dataValue)) {
                            if (tiers.getT1() != dataValue) {
                                Savelog.d(TAG, debug, "edittext updating T1 to " + dataValue);
                                tiers.setT1(dataValue);
                                hostFragment.mCharts.refreshPointers();
                            }
                        } else {
                            hostFragment.updateAlertView("T1 invalid");
                            // report invalid
                        }
                    }
                    else if (inputType==InputType_H) {
                        if (tiers.isValidH(dataValue)) {
                            if (tiers.getH()!=dataValue) {
                                tiers.setH(dataValue);
                                hostFragment.mCharts.refreshPointers();
                            }
                        } else {
                            hostFragment.updateAlertView("h invalid");
                            // report invalid
                        }
                    }
                } catch (NumberFormatException e) {
                    // no change
                    hostFragment.updateAlertView("Input invalid");
                    // report invalid
                }

            }
        }
        public void cleanup() { hostFragment = null; }
    }


    private void updateHView() {
        if (mHView != null) {
            mHView.setText("" + String.format("%f", mTiers.getH()));
            mHView.setSelection(mHView.getText().length());
        }
    }
    private void updateT1View() {
        if (mT1View!=null) {
            mT1View.setText("" + String.format("%f", mTiers.getT1()));
            mT1View.setSelection(mT1View.getText().length());
        }
    }
    private void updateT2View() {
        if (mT2View!=null) {
            mT2View.setText("" + String.format("%f", mTiers.getT2()));
            mT2View.setSelection(mT2View.getText().length());
        }
    }

    private void updateAlertView(String alertText) {
        if (alertText==null) alertText="";
        mAlertText = alertText;
        if (mAlertView!=null) {
            mAlertView.setText(mAlertText);
        }
    }

    private static class OnSeekbarChangedListener implements Mem2LCharts.OnSeekBarChangeListener {
        Mem2LFragment hostFragment;

        public OnSeekbarChangedListener(Mem2LFragment hostFragment) {
            this.hostFragment = hostFragment;
        }

        @Override
        public void onSeekBarValuesChanged(Mem2LCharts bar, double T1, double h) {
            hostFragment.mTiers.setH(h);
            hostFragment.mTiers.setT1(T1);
            Savelog.d(TAG, debug, "seekbarChanged T1 to " + hostFragment.mTiers.getT1());

            hostFragment.mCharts.refreshPointers();
            hostFragment.updateHView();
            hostFragment.updateT1View();
            Savelog.d(TAG, debug, "updating T1=" + T1 + " h=" + h);
        }
    }
}
