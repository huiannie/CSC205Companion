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

package edu.nvcc.manassas.csc205companion.tools;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import edu.nvcc.manassas.csc205companion.R;
import edu.nvcc.manassas.csc205companion.app.AppSettings;
import edu.nvcc.manassas.csc205companion.tools.arithmetic.ArithIntFragment;
import edu.nvcc.manassas.csc205companion.tools.memory.Mem2LFragment;
import edu.nvcc.manassas.csc205companion.tools.pipeline.PipelineFragment;
import edu.nvcc.manassas.csc205companion.tools.representation.NumToRepFragment;
import edu.nvcc.manassas.csc205companion.tools.representation.RepToNumFragment;


public class ToolsActivity extends AppCompatActivity {
    public static final String TAG = ToolsActivity.class.getSimpleName()+"_class";

    public static final String EXTRA_Type = ToolsActivity.class.getSimpleName()+".Type";

    public static final int Type_numToRep = 0;
    public static final int Type_repToNum = 1;
    public static final int Type_arithInt = 2;
    public static final int Type_pipelineIdeal = 3;
    public static final int Type_pipelineReal = 4;
    public static final int Type_2l = 5;

    public static final int[] Types = {Type_numToRep, Type_repToNum, Type_arithInt, Type_pipelineIdeal, Type_pipelineReal, Type_2l};
    public static final int[] Type_NameId = {
            R.string.menu_tools_numToRep,
            R.string.menu_tools_repToNum,
            R.string.menu_tools_arithInt,
            R.string.menu_tools_pipelineIdeal,
            R.string.menu_tools_pipelineOverhead,
            R.string.menu_tools_mem2L};


    public static final int Type_default = Type_numToRep;

    private int mType = Type_default;
    private Fragment fragment;
    private int fragmentId;

    //OnCreate Method:
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        fragmentId = R.id.activity_container_id;

        mType = getIntent().getIntExtra(EXTRA_Type, Type_default);
        setTitle(Type_NameId[mType]);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.logo_csc205);
        getSupportActionBar().setDisplayUseLogoEnabled(true);


        // Check if fragment already exists
        FragmentManager fm = getFragmentManager();
        fragment = fm.findFragmentById(fragmentId);
        if (fragment == null) {
            if (mType == Type_numToRep) {
                fragment = NumToRepFragment.newInstance();
            } else if (mType == Type_repToNum) {
                fragment = RepToNumFragment.newInstance();
            } else if (mType == Type_arithInt) {
                fragment = ArithIntFragment.newInstance();
            } else if (mType == Type_pipelineIdeal) {
                fragment = PipelineFragment.newInstance(PipelineFragment.type_idealPipeline);
            } else if (mType == Type_pipelineReal) {
                fragment = PipelineFragment.newInstance(PipelineFragment.type_realPipeline);
            } else if (mType == Type_2l) {
                fragment = Mem2LFragment.newInstance();
            }

            fm.beginTransaction().add(fragmentId, fragment).commit();
        }
    }



    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(EXTRA_Type, mType);
        super.onSaveInstanceState(savedInstanceState);
    }

}
