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

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.nvcc.manassas.csc205companion.app.AppSettings;

public class Savelog {
    public static final String TAG = Savelog.class.getSimpleName()+"_class";
    public static final String logname = "log.txt";

    public static File getLogFile() {
        File logFile = new File(IO.getDefaultExternalPath(), logname);
        return logFile;
    }

    public static boolean clear() {
        File logFile = getLogFile();
        if (logFile!=null && logFile.exists()) {
            logFile.delete();
            return true;
        }
        else {
            return false;
        }
    }

    // http://stackoverflow.com/questions/1756296/android-writing-logs-to-text-file
    private static void appendLog(String text) {

        if (AppSettings.testerEnabled) {
            // Save the log file in external folder if option is set

            File logFile = getLogFile();
            if (logFile!=null && !logFile.exists()) {
                try {
                    logFile.createNewFile();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                SimpleDateFormat timingFormat = new SimpleDateFormat("hh:mm");
                // Add time to log.
                text = "" + timingFormat.format(new Date()) + ": " + text;
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(text);
                buf.newLine();
                buf.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getStack(Throwable err) {
        String data = "";
        StackTraceElement[] errStack = err.getStackTrace();
        for (StackTraceElement e : errStack) {
            data += e.toString() + "\n";
        }
        return data;
    }

    public static void d(String tag, boolean debug, String message) {
        if (debug) {
            if (message==null) message="(no message)";
            String text = "Debug:\t"+tag+"\t"+message;
            appendLog(text);
            Log.d(tag, message);
        }
    }


    public static void i(String tag, String message) {
        if (message==null) message="(no message)";
        String text = "Information:\t"+tag+"\t"+message;
        appendLog(text);
        Log.i(tag, message);
    }
    public static void e(String tag, String message) {
        if (message==null) message="(no message)";
        String text = "ERROR:\t"+tag+"\t"+message;
        appendLog(text);
        Log.e(tag, message);
    }
    public static void w(String tag, String message) {
        if (message==null) message="(no message)";
        String text = "Warning:\t"+tag+"\t"+message;
        appendLog(text);
        Log.w(tag, message);
    }

    public static void e(String tag, String message, Throwable err) {
        if (err!=null) {
            e(tag, message + "\ne.message()=" + err.getMessage() + "\n" + getStack(err));
        }
        else {
            e(tag, message);
        }
    }
    public static void w(String tag, String message, Throwable err) {
        if (err!=null) {
            w(tag, message + "\ne.message()=" + err.getMessage() + "\n" + getStack(err));
        }
        else {
            w(tag, message);
        }
    }

}
