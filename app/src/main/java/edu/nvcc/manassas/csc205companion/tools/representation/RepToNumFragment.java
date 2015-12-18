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


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import edu.nvcc.manassas.csc205companion.R;
import edu.nvcc.manassas.csc205companion.app.AppSettings;
import edu.nvcc.manassas.csc205companion.io.Savelog;


public class RepToNumFragment extends Fragment {
    private static final String TAG = RepToNumFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private static final int ButtonColorId = R.color.button_very_dark;
    private static final String Unfound = "?";
    private static final int Default_base = 16;
    private int mBase = Default_base;
    private String mData = "";


    private Keypad.Button[] codeButtons = Keypad.codeButtons;
    private Keypad.Button[] baseButtons = Keypad.baseButtons;

    private GridView mNumericGrid;
    private KeypadAdapter mNumericAdapter;
    private GridView mBaseGrid;
    private KeypadAdapter mBaseAdapter;
    private TextView mDisplayView;

    public static RepToNumFragment newInstance() {
        Bundle args = new Bundle();

           RepToNumFragment fragment = new RepToNumFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        setRetainInstance(true);
        setHasOptionsMenu(true);

    } // end to implementing onCreate()



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Savelog.d(TAG, debug, "onCreateView()");

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tool_basechange, container, false);

        mDisplayView = (TextView) v.findViewById(R.id.fragmentToolBaseChange_display);
        this.formatScreenData();

        mBaseGrid = (GridView) v.findViewById(R.id.fragmentToolBaseChange_opt_grid);
        if (mBaseAdapter==null) {
            mBaseAdapter = new KeypadAdapter(this, baseButtons);
            mBaseAdapter.setOnButtonClickListener(new OnButtonClickListener(this));
        }
        mBaseGrid.setAdapter(mBaseAdapter);

        mNumericGrid = (GridView) v.findViewById(R.id.fragmentToolBaseChange_digit_grid);
        if (mNumericAdapter==null) {
            mNumericAdapter = new KeypadAdapter(this, codeButtons);
            mNumericAdapter.setOnButtonClickListener(new OnButtonClickListener(this));
        }
        mNumericGrid.setAdapter(mNumericAdapter);
        return v;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mNumericGrid !=null) {
            mNumericGrid.setAdapter(null);
            mNumericGrid =null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mNumericAdapter !=null) {
            mNumericAdapter = null;
        }
    }


    public static class KeypadAdapter extends BaseAdapter {
        Keypad.Button[] buttonArray;
        OnButtonClickListener onButtonClickListener;
        private Context appContext;
        RepToNumFragment hostFragment;

        public KeypadAdapter(RepToNumFragment hostFragment, Keypad.Button[] buttonArray) {
            appContext = hostFragment.getActivity().getApplicationContext();
            this.hostFragment = hostFragment;
            this.buttonArray = buttonArray;
            Savelog.d(TAG, debug, "size of keypad = " + buttonArray.length);
        }

        public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
            this.onButtonClickListener = onButtonClickListener;
        }

        public int getCount() {
            return buttonArray.length;
        }

        public Object getItem(int position) {
            return buttonArray[position];
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final Button button;
            if (convertView == null) {
                // new
                button = new Button(appContext);
                Keypad.Button keypadButton = buttonArray[position];
                button.setTag(keypadButton);
                button.setTextColor(hostFragment.getResources().getColor(ButtonColorId));

                // One listener to be shared by all on this adapter
                button.setOnClickListener(onButtonClickListener);

            } else {
                // recycled
                button = (Button) convertView;

            }

            // Now adjust color. Need to do this for both new views and recycled views
            Keypad.Button keypadButton = (Keypad.Button) button.getTag();
            if (keypadButton.isBase() || keypadButton.isOperator()) {
                button.setBackgroundResource(R.drawable.tool_keypad_active);
            }
            else { // must be a digit
                if (keypadButton.getDigitValue()<hostFragment.mBase) {
                    button.setBackgroundResource(R.drawable.tool_keypad_active);
                }
                else {
                    button.setBackgroundResource(R.drawable.tool_keypad_inactive);
                }
            }

            button.setText(buttonArray[position].getLabel());
            return button;
        }

    }

    private static class OnButtonClickListener implements View.OnClickListener {
        RepToNumFragment hostFragment;
        public OnButtonClickListener(RepToNumFragment hostFragment) {
            this.hostFragment = hostFragment;
        }

        @Override
        public void onClick(View v) {
            Button button = (Button) v;
            Keypad.Button keypadButton = (Keypad.Button) button.getTag();
            if (keypadButton.isBase()) {
                int newBase = keypadButton.getBase();
                if (hostFragment.mBase!=newBase) {
                    hostFragment.mBase = newBase;
                    hostFragment.mData = ""; // reset data when base changes
                    hostFragment.formatScreenData();
                    // disable some buttons
                    hostFragment.mNumericAdapter.notifyDataSetChanged();
                    Savelog.d(TAG, debug, "use base " + hostFragment.mBase);
                }
            }
            else if (keypadButton.isDigit()) {
                if (keypadButton.getDigitValue()<hostFragment.mBase) {
                    hostFragment.mData += keypadButton.getDigit();
                    Savelog.d(TAG, debug, "pressed " + keypadButton.getDigit());
                    hostFragment.formatScreenData();
                }
                else {
                    Savelog.d(TAG, debug, "inactive ");
                }
            }
            else if (keypadButton.isOperator()) {
                if (keypadButton.isClear()) {
                    if (hostFragment.mData.length()>0) {
                        hostFragment.mData = "";
                        hostFragment.formatScreenData();
                    }
                }
                else if (keypadButton.isDelete()) {
                    int length = hostFragment.mData.length();
                    if (length>0) {
                        hostFragment.mData = hostFragment.mData.substring(0, length-1);
                        hostFragment.formatScreenData();
                    }
                }

                Savelog.d(TAG, debug, "pressed " + keypadButton.getDigit());
            }
        }
    }


    private static int codeToInt(String data, int inputBase) {
        // handle FF FF FF FF and bigger
        long value = Long.valueOf(data, inputBase);
        if (value>>32 != 0) {
            Savelog.d(TAG, debug, data + " exceeded 32 bits");
            throw new NumberFormatException(data + " exceeded 32 bits");
        }
        int intMask = 0xFFFFFFFF;
        int intValue = (int) (value & intMask);
        return intValue;
    }

    private static String getPaddedCode(String data, int inputBase) {
        if (data==null || data.length()==0) return "";
        try {
            int intValue = codeToInt(data, inputBase);
            return Convert.intToBinaryString32digit(intValue);
        }
        catch (Exception e) {
            return Unfound;
        }
    }

    private static String getIntValue(String data, int inputBase) {
        if (data==null || data.length()==0) return "";
        try {
            int intValue = codeToInt(data, inputBase);
            return Integer.toString(intValue);
        }
        catch (Exception e) {
            return Unfound;
        }
    }

    private static String getFloatValue(String data, int inputBase) {
        if (data==null || data.length()==0) return "";
        try {
            int bits = codeToInt(data, inputBase);
            float floatValue = Float.intBitsToFloat(bits);
            return Float.toString(floatValue);
        }
        catch (Exception e) {
            return Unfound;
        }
    }



    private void formatScreenData() {
        String data = mData;
        int base = mBase;
        String dataPadded = getPaddedCode(data, base) + "\n";
        String dataInt = getIntValue(data, base) + "\n";
        String dataFloat = getFloatValue(data, base) + "\n";
        String error = "";

        String headerInput = "4-byte input (base " + base + "):\n";
        String headerPadded = "4 bytes in 32 bits:\n";

        String headerInt = "Numeric value (as integer):\n";
        String headerFloat = "Numeric value (as real number):\n";

        if (dataPadded.contains(Unfound) || dataInt.contains(Unfound) || dataFloat.contains(Unfound)) {
            if (base==2) error = "binary input exceeeded 32 bits";
            else if (base==10) error = "decimal input exceeded 4294967296";
            else if (base==16) error = "hexadecimal input exceeded FFFFFFFF";
            error = "<br><br>! " + error;
        }

        String displayData = "";
        displayData += "<body>" + "<em>" + headerInput + "</em>" + "<br>";
        displayData +=  data + "<br>";
        displayData +=  "<em>" + headerPadded + "</em>" + "<br>";
        displayData +=  dataPadded + "<br>";
        displayData +=  "<em>" + headerInt + "</em>" + "<br>";
        displayData +=  dataInt + "<br>";
        displayData +=  "<em>" + headerFloat + "</em>" + "<br>";
        displayData +=  dataFloat + "<br>";
        displayData +=  (error.length()>0) ? (error + "<br>") : "";
        displayData += "</body>";

        mDisplayView.setText(Html.fromHtml(displayData));
    }
}
