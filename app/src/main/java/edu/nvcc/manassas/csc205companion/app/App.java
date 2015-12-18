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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import edu.nvcc.manassas.csc205companion.io.IO;
import edu.nvcc.manassas.csc205companion.io.Savelog;


public class App extends Application {
    private static final String TAG = App.class.getSimpleName()+"_class";

    // ATTENTION: be very careful. The name of this class is not changeable once this app is posted onto GooglePlay
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }


    public static void clear() {
        // internal files
        IO.clearInternalFiles(mContext);
        // all default shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        prefs.edit().clear().commit();
        // Clear all statics
        Savelog.clear();

        // ATTN: may need to call finishAffinity() to close all activities immediately after this.
    }
}