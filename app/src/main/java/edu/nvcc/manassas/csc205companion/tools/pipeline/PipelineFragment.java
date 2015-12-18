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


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import edu.nvcc.manassas.csc205companion.R;
import edu.nvcc.manassas.csc205companion.app.AppSettings;
import edu.nvcc.manassas.csc205companion.io.Savelog;


public class PipelineFragment extends Fragment {
    private static final String TAG = PipelineFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private static final String EXTRA_type = PipelineFragment.class.getSimpleName()+".type";

    public static final int type_idealPipeline = 601;
    public static final int type_realPipeline = 602;
    public static final int type_default = type_idealPipeline;

    private static final int MaxNumberOfStages = Pipeline.DEFAULT_MAX_STAGES;
    private static final int MinNumberOfStages = Pipeline.DEFAULT_MIN_STAGES;
    private static final int MaxN = 1000000;
    private static final int DefaultN = 1;

    private int mType = type_default;

    private String mStageLabels[] = null;
    private int mStage = 0;
    private Pipeline mPipeline = null;
    private int N = DefaultN;

    private ArrayAdapter mArrayAdapter = null;
    private Spinner mSpinner = null;
    private PipelineSeekBar mPipelineSeekBar = null;
    private TextView mTextView = null;
    private EditText mEditText = null;

    public static PipelineFragment newInstance(int type) {
        Bundle args = new Bundle();

        PipelineFragment fragment = new PipelineFragment();
        if (type==type_idealPipeline || type==type_realPipeline)
            args.putInt(EXTRA_type, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        mType = getArguments().getInt(EXTRA_type, type_default);

        int count = MaxNumberOfStages-MinNumberOfStages+1;
        mStageLabels = new String[count];
        for (int index=0; index<count; index++) {
            mStageLabels[index] = String.valueOf(MinNumberOfStages+index) + "-stage"; // stage label
        }
        mArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mStageLabels);

        mPipeline = new Pipeline(MinNumberOfStages);

        setRetainInstance(true);
        setHasOptionsMenu(true);

    } // end to implementing onCreate()




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Savelog.d(TAG, debug, "onCreateView()");

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tool_pipeline, container, false);


        // Setup the new range seek bar
        if (mType==type_idealPipeline)
            mPipelineSeekBar = new PipelineSeekBarIdeal(getActivity(), mPipeline);
        else if (mType==type_realPipeline)
            mPipelineSeekBar = new PipelineSeekBarOverhead(getActivity(), mPipeline);
        else // default
            mPipelineSeekBar = new PipelineSeekBarIdeal(getActivity(), mPipeline);

        mPipelineSeekBar.setId(R.id.fragmentToolPipeline_seekbar);
        mPipelineSeekBar.setOnSeekBarChangeListener(new OnSeekbarChangedListener(this));
        mPipelineSeekBar.setNotifyWhileDragging(true);

        // Add to layout
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.fragmentToolPipeline_seekbarPlaceholder);
        layout.addView(mPipelineSeekBar);
        
        mSpinner = (Spinner) v.findViewById(R.id.fragmentToolPipeline_spinner);
        mSpinner.setAdapter(mArrayAdapter);
        mSpinner.setOnItemSelectedListener(new StageSelectedListener(getActivity(), this));

        mTextView = (TextView) v.findViewById(R.id.fragmentToolPipeline_description);
        mTextView.setText(mPipeline.getStatistics(N));

        mEditText = (EditText) v.findViewById(R.id.fragmentToolPipeline_N);
        mEditText.addTextChangedListener(new NTextWatcher(this));
        mEditText.setText(String.valueOf(N));
        return v;
    }



    private static class StageSelectedListener implements AdapterView.OnItemSelectedListener {
        PipelineFragment hostFragment;
        Context appContext;

        public StageSelectedListener(Context context, PipelineFragment hostFragment) {
            this.hostFragment = hostFragment;
            this.appContext = context.getApplicationContext();
        }
        public void cleanup() {
            hostFragment = null;
        }
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long itemId) {
            Savelog.d(TAG, debug, "Selected option " + position);
            int newStage = MinNumberOfStages + position;

            if (newStage != hostFragment.mStage) {
                hostFragment.mStage = newStage;

                hostFragment.mPipeline = new Pipeline(newStage);
                hostFragment.mPipelineSeekBar.setPipeLine(appContext, hostFragment.mPipeline);
                hostFragment.mTextView.setText(hostFragment.mPipeline.getStatistics(hostFragment.N));
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private static class NTextWatcher implements TextWatcher {
        PipelineFragment hostFragment;
        public NTextWatcher(PipelineFragment hostFragment) {
            this.hostFragment = hostFragment;
        }


        @Override
        public void onTextChanged(CharSequence c, int start, int before, int count) {
            String data = "";
            boolean dataValid = true;

            if (c.length()>0) {
                int dataValue = 0;
                if (c!=null && c.toString().trim().length()>0) {
                    data = c.toString().trim();
                }
                Savelog.d(TAG, debug, "Entered " + data);
                try {
                    dataValue = Integer.valueOf(data);
                    if (dataValue>MaxN || dataValue<1) {
                        dataValid = false;
                    }
                } catch (NumberFormatException e) {
                    dataValid = false;
                }
                if (dataValid) {
                    hostFragment.N = dataValue;
                }
                else {
                    hostFragment.mEditText.setText("");
                    hostFragment.N = 0;
                }
            }
            else {
                hostFragment.N = 0;
            }
            hostFragment.mTextView.setText(hostFragment.mPipeline.getStatistics(hostFragment.N));
        }

        @Override
        public void beforeTextChanged(CharSequence c, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable c) {}
    }


    private static class OnSeekbarChangedListener implements PipelineSeekBar.OnSeekBarChangeListener {
        PipelineFragment hostFragment;

        public OnSeekbarChangedListener(PipelineFragment hostFragment) {
            this.hostFragment = hostFragment;
        }

        @Override
        public void onSeekBarValuesChanged(PipelineSeekBar bar) {
            hostFragment.mTextView.setText(hostFragment.mPipeline.getStatistics(hostFragment.N));
        }

    }
}
