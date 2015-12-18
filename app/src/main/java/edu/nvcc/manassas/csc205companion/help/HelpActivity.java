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

package edu.nvcc.manassas.csc205companion.help;


import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.jar.Attributes;

import edu.nvcc.manassas.csc205companion.R;
import edu.nvcc.manassas.csc205companion.app.AppSettings;
import edu.nvcc.manassas.csc205companion.io.Savelog;


public class HelpActivity extends AppCompatActivity {
    private static final String TAG = HelpActivity.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private HelpFragment fragment;
    private int fragmentId;

    //OnCreate Method:
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        setContentView(R.layout.activity_container);
        fragmentId = R.id.activity_container_id;

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.logo_csc205);
        getSupportActionBar().setDisplayUseLogoEnabled(true);


        // Check if fragment already hasCourse
        FragmentManager fm = getFragmentManager();
        fragment = (HelpFragment) fm.findFragmentById(fragmentId);
        if (fragment == null) {
            fragment = HelpFragment.newInstance();
            fm.beginTransaction().add(fragmentId, fragment).commit();
        }
    }
}