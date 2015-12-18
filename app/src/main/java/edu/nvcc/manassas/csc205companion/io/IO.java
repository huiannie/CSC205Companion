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

package edu.nvcc.manassas.csc205companion.io;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import edu.nvcc.manassas.csc205companion.app.AppSettings;

public class IO {
    private static final String TAG = IO.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private static final String externalFolder = AppSettings.defaultFolder;


    public static File[] getInternalFiles(Context context) {
        File internalDirectory = context.getFilesDir();
        return getInternalFiles(context, internalDirectory);
    }


    public static File[] getInternalFiles(Context context, File internalDirectory) {
        // Return empty files if input is invalid
        if (internalDirectory==null || !internalDirectory.exists()) return new File[0];

        ArrayList<File> output = new ArrayList<File>();
        output = recurGetFiles(internalDirectory.getAbsolutePath(), output);
        File files[] = new File[output.size()];
        for (int index=0; index<output.size(); index++) {
            files[index] = output.get(index);
        }
        return files;
    }

    private static ArrayList<File> recurGetFiles( String path, ArrayList<File>output ) {
        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return output;
        for ( File f : list ) {
            if ( f.isDirectory() ) {
                output = recurGetFiles(f.getAbsolutePath(), output);
                // Savelog.d(TAG, debug, "Dir:" + f.getAbsoluteFile() );
            }
            else {
                // Savelog.d(TAG, debug, "File:" + f.getAbsoluteFile());
            }
            output.add(f); // include f after traversal. This way, if a directory needs to be cleared, it will be cleared after it's emptied.
        }
        return output;
    }


    public static void clearInternalFiles(Context context) {
        File[] internalFiles = getInternalFiles(context);
        for (File f : internalFiles) {
            Savelog.d(TAG, debug, "Deleting internal file " + f.getAbsolutePath());
            f.delete();
        }
    }



    private static String readStreamAsString(InputStream is) throws IOException {
        //create a buffer that has the same size as the InputStream
        byte[] buffer = new byte[is.available()];
        //read the text file as a stream, into the buffer
        is.read(buffer);
        //create a output stream to write the buffer into
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        //write this buffer to the output stream
        os.write(buffer);
        //Close the Input and Output streams
        os.close();
        is.close();

        String content = os.toString();
        return content;
    }

    public static String getRawResourceAsString(Context context, int id) throws IOException {
        InputStream is;
        //get the file as a stream from res/raw/
        is = context.getResources().openRawResource(id);
        return readStreamAsString(is);
    }


    public static File getDefaultExternalPath() {
        // if the default directory does not exist, create it!
        File defaultPath = null;
        File root;
        if (checkExternalMedia()) {
            root = Environment.getExternalStorageDirectory();
        }
        else {
            root = Environment.getDownloadCacheDirectory();
        }


        if (root.getAbsolutePath()!=null) {
            defaultPath = new File (root.getAbsolutePath() + externalFolder);
            if (!defaultPath.exists()) {
                Log.i(TAG, "Creating directory " + defaultPath);  // Since the external directory is not yet available, we cannot call Savelog here!!!
                defaultPath.mkdirs();
            }
            else {  // there is something at the location.
                // If that something is a file, remove it and replace it by
                // a directory.
                if (!defaultPath.isDirectory()) {
                    boolean success = defaultPath.delete();
                    if (success) {
                        defaultPath.mkdirs();
                    }
                    else {
                        // Unable to remove file. Return null.
                        defaultPath = null;
                    }
                }
                else {
                    // Default external directory already exists. No need to do anything.
                }
            }
        }
        else {
            // No external media available.
            // Do nothing. Return null.
            defaultPath = null;
        }

        return defaultPath;
    }

    private static boolean checkExternalMedia(){
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        return (mExternalStorageAvailable && mExternalStorageWriteable);
    }

}
