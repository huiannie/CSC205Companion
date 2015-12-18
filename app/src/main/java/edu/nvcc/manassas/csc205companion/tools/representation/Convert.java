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

package edu.nvcc.manassas.csc205companion.tools.representation;


public class Convert {
    public static String intToBinaryString32digit(int value) {
        String fullString = Integer.toBinaryString(value);
        int padding = Integer.SIZE - fullString.length();
        for (int i=0; i<padding; i++) fullString = "0" + fullString;
        return fullString.substring(0,8) + " " + fullString.substring(8,16) + " " + fullString.substring(16, 24) + " " + fullString.substring(24,32);
    }

    public static String intToHexString8digit(int value) {
        String fullString = Integer.toHexString(value);
        int padding = Byte.SIZE - fullString.length();
        for (int i=0; i<padding; i++) fullString = "0" + fullString;
        return fullString.substring(0, 2) + " " + fullString.substring(2, 4) + " " + fullString.substring(4, 6) + " " + fullString.substring(6, 8);
    }

}
