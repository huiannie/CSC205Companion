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

package edu.nvcc.manassas.csc205companion.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import edu.nvcc.manassas.csc205companion.R;


public class MainActivity extends AppCompatActivity {
    private int fragmentId;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        fragmentId = R.id.activity_container_id;

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.logo_csc205);
        getSupportActionBar().setDisplayUseLogoEnabled(true);


        FragmentManager fm = getFragmentManager();
        if (savedInstanceState == null) {
            fragment = MainFragment.newInstance();
            fm.beginTransaction().add(fragmentId, fragment).commit();
        }
        else {
            fragment = fm.findFragmentById(fragmentId);
        }
    }
}
