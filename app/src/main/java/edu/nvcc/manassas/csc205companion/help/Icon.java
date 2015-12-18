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

import java.util.ArrayList;

import edu.nvcc.manassas.csc205companion.R;
import edu.nvcc.manassas.csc205companion.app.AppSettings;
import edu.nvcc.manassas.csc205companion.io.Savelog;

public class Icon  {
    private static final String TAG = Icon.class.getSimpleName()+"_class";
    private static boolean debug = AppSettings.defaultDebug;

    private static final int iconIds[] = {
            R.mipmap.ic_tools_tocode,
            R.mipmap.ic_tools_tonum,
            R.mipmap.ic_tools_calc,
            R.mipmap.ic_tools_pipeideal,
            R.mipmap.ic_tools_pipeoverhead,
            R.mipmap.ic_tools_2l,
            R.mipmap.ic_info_outline_white_24dp,
            R.mipmap.ic_help_outline_white_24dp
    };
    private static final int nameIds[] = {
            R.string.menu_tools_numToRep,
            R.string.menu_tools_repToNum,
            R.string.menu_tools_arithInt,
            R.string.menu_tools_pipelineIdeal,
            R.string.menu_tools_pipelineOverhead,
            R.string.menu_tools_mem2L,
            R.string.menu_info,
            R.string.menu_help
    };
    private static final String descriptions[] = {
            "Convert a binary, decimal, or hexadecimal number to a computer\'s internal representation. Whole numbers are converted to the IEEE 32-bit int. Fractional numbers are converted to the IEEE 32-bit float.",
            "Convert a 32-bit computer\'s internal representation to its numerical value",
            "Perform number addition in 8-bit 2\'s complement form",
            "Analyze an ideal pipeline. The parameters of the simulation are the number of stages of the pipeline and the number of instructions executed.",
            "Analyze a pipeline with overhead. The parameters of the simulation are the number of stages of the pipeline, the number of instructions executed and the overhead.",
            "Evaluate the effective access time (EAT) and the efficiency of a 2-level memory system.",
            "Display information about this app",
            "Display this help"
    };

    private int iconId = 0;
    private int nameId = 0;
    private String description = "";
    public Icon(int iconId, int nameId, String description) throws Exception {
        if (iconId==0 || nameId==0 || description==null) throw new Exception("bad arguments");
        this.iconId = iconId;
        this.nameId = nameId;
        this.description = description;
    }
    public int getIconId() {
        return iconId;
    }
    public int getNameId() {
        return nameId;
    }
    public String getDescription() {
        return description;
    }

    public static ArrayList<Icon> makeList() {
        Savelog.d(TAG, debug, "makeList()");
        ArrayList<Icon> list = new ArrayList<Icon>();
        int total = iconIds.length;
        if (total!=nameIds.length || total!=descriptions.length) return list;
        for (int index=0; index<total; index++) {
            try {
                Icon icon = new Icon(iconIds[index], nameIds[index], descriptions[index]);
                list.add(icon);
            }
            catch (Exception e) {
                Savelog.w(TAG, "cannot create icon at " + index + "\n" + e.getMessage());
            }
        }
        return list;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==null) return false;
        if (obj instanceof Icon) {
            Icon icon2 = (Icon) obj;
            if (this.iconId==icon2.getIconId()
                    && this.nameId==icon2.getNameId()
                    && this.description.equals(icon2.getDescription())) {
                return true;
            }
        }
        return false;
    }
}