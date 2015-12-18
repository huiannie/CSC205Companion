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


public class NumToRepFragment extends Fragment {
    private static final String TAG = NumToRepFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private static final int ButtonColorId = R.color.button_very_dark;
    private static final String Unfound = "?";
    private static final int Default_base = 10;
    private int mBase = Default_base;
    private String mData = "";


    private Keypad.Button[] numericButtons = Keypad.numericButtons;
    private Keypad.Button[] baseButtons = Keypad.baseButtons;

    private GridView mNumericGrid;
    private KeypadAdapter mNumericAdapter;
    private GridView mBaseGrid;
    private KeypadAdapter mBaseAdapter;
    private TextView mDisplayView;

    public static NumToRepFragment newInstance() {
        Bundle args = new Bundle();

           NumToRepFragment fragment = new NumToRepFragment();
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
            mNumericAdapter = new KeypadAdapter(this, numericButtons);
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
        NumToRepFragment hostFragment;

        public KeypadAdapter(NumToRepFragment hostFragment, Keypad.Button[] buttonArray) {
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
        NumToRepFragment hostFragment;
        public OnButtonClickListener(NumToRepFragment hostFragment) {
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
                if (keypadButton.isSign()) {
                    if (hostFragment.mData.length()>0 && hostFragment.mData.charAt(0)=='-') {
                        hostFragment.mData = hostFragment.mData.substring(1); // trim of existing negative sign
                    }
                    else {
                        hostFragment.mData = "-" + hostFragment.mData; // add a new negative sign
                    }
                    hostFragment.formatScreenData();
                }
                else if (keypadButton.isPoint()) {
                    if (!hostFragment.mData.contains(".")) {
                        hostFragment.mData += "."; // add a point, if there isn't one already
                        hostFragment.formatScreenData();
                    }
                }
                else if (keypadButton.isClear()) {
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



    private static boolean isFractional(String data) {
        if (data==null || data.length()==0) return false;
        if (data.contains("."))
            return true;
        else
            return false;
    }


    private static String toBase(String data, int inputBase, int outputBase) {
        if (data==null || data.length()==0) return ""; // no data

        {   // Do some pre-processing

            // Add a 0 if data starts with -. or .
            if (data.startsWith("-.") || data.startsWith(".")) data = data.replace(".", "0.");

        }
        Savelog.d(TAG, debug, "after preprocessing:" + data);


        try {
            if (!data.contains(".")) {
                int value;
                value = Integer.valueOf(data, inputBase);

                if (outputBase==2) {
                    return Convert.intToBinaryString32digit(value);
                }
                else if (outputBase==16) {
                    return Convert.intToHexString8digit(value);
                }
                else {
                    return Integer.toString(value, outputBase);
                }
            }
            else {
                int bits;
                float floatValue = 0;
                boolean signPositive = true;
                String integerPart;
                String fractionalPart;

                {   // Break the string into two parts based on the binary point
                    String parts[] = data.split("\\.");
                    integerPart = parts[0];

                    if (parts.length==2)
                        fractionalPart = parts[1];
                    else
                        fractionalPart = "0";

                    if (integerPart.startsWith("-")) {
                        signPositive=false;
                        integerPart = integerPart.substring(1); // trim off sign from integer part.
                    }
                    Savelog.d(TAG, debug, integerPart + " " + fractionalPart);
                }

                for (int pos=0; pos<fractionalPart.length(); pos++) {
                    int digitValue = Keypad.Button.getValue(fractionalPart.substring(pos, pos + 1));
                    floatValue += Math.pow(inputBase, -(pos+1))*digitValue;
                }
                for (int pos=integerPart.length()-1; pos>=0; pos--) {
                    int digitValue = Keypad.Button.getValue(integerPart.substring(pos, pos + 1));
                    floatValue += Math.pow(inputBase, (integerPart.length()-1-pos))*digitValue;
                }

                if (!signPositive) {
                    floatValue = -floatValue;
                }

                bits = Float.floatToRawIntBits(floatValue);

                if (outputBase==2) {
                    return Convert.intToBinaryString32digit(bits);
                }
                else if (outputBase==16) {
                    return Convert.intToHexString8digit(bits);
                }
                else if (outputBase==10)
                    return Float.toString(floatValue);

                return Unfound;
            }
        }
        catch (NumberFormatException e) {
            return Unfound; // overflow
        }

    }


    private void formatScreenData() {
        String data = mData;
        int base = mBase;
        String dataType = "int";  // default type
        String dataBinary = toBase(data, base, 2) + "\n";
        String dataDecimal = toBase(data, base, 10) + "\n";
        String dataHexadecimal = toBase(data, base, 16) + "\n";
        String error = "";

        if (isFractional(data)) {
            dataType = "float";  // change to float if input is fractional
        }

        String headerInput = "Numeric input (base " + base + "):\n";
        String headerDecimal = "Decimal value:\n";

        String headerBinary = dataType + " (binary):\n";
        String headerHexadecimal = dataType + " (hexadecimal):\n";

        if (dataBinary.contains(Unfound) || dataDecimal.contains(Unfound) || dataHexadecimal.contains(Unfound)) {
            if (base==2) error = "binary input exceeeded (-2^31, 2^31-1)";
            else if (base==10) error = "decimal input exceeded (-2147483648, 2147483647)";
            else if (base==16) error = "hexadecimal input exceeded (-80000000,7FFFFFFF)";
            error = "<br><br>! " + error;
        }

        String displayData = "";
        displayData += "<body>" + "<em>" + headerInput + "</em>" + "<br>";
        displayData +=  data + "<br>";
        displayData +=  "<em>" + headerDecimal + "</em>" + "<br>";
        displayData +=  dataDecimal + "<br>";
        displayData +=  "<em>" + headerBinary + "</em>" + "<br>";
        displayData +=  dataBinary + "<br>";
        displayData +=  "<em>" + headerHexadecimal + "</em>" + "<br>";
        displayData +=  dataHexadecimal + "<br>";
        displayData +=  (error.length()>0) ? (error + "<br>") : "";
        displayData += "</body>";

        mDisplayView.setText(Html.fromHtml(displayData));
    }
}
