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

package edu.nvcc.manassas.csc205companion.tools.arithmetic;


import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import edu.nvcc.manassas.csc205companion.R;
import edu.nvcc.manassas.csc205companion.app.AppSettings;
import edu.nvcc.manassas.csc205companion.io.Savelog;


public class ArithIntFragment extends Fragment {
    private static final String TAG = ArithIntFragment.class.getSimpleName()+"_class";
    private static final boolean debug = AppSettings.defaultDebug;

    private static final int GridHeight = 4;
    private static final int GridWidth = 12;
    private static final int ExcessBit = 2;
    private static final int NumberOfBits = 8;
    private static final int StartNumberBit = 3;
    private static final int EndNumberBit = StartNumberBit + NumberOfBits-1;
    private static final int NoteCell = StartNumberBit + NumberOfBits;

    private static final int MaxInteger = 127;
    private static final int MinInteger = -128;

    private String mInput1 = "";
    private String mInput2 = "";
    private int mNumber1 = 0;
    private int mNumber2 = 0;
    private String blank = "";

    private TextView mTextViews[];
    private EditText mInputView1;
    private EditText mInputView2;
    private InputTextWatcher mInputTextWatcher1;
    private InputTextWatcher mInputTextWatcher2;

    public static ArithIntFragment newInstance() {
        Bundle args = new Bundle();

        ArithIntFragment fragment = new ArithIntFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Savelog.d(TAG, debug, "onCreate()");

        blank = getResources().getString(R.string.label_blank);

        setRetainInstance(true);
        setHasOptionsMenu(true);

    } // end to implementing onCreate()



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Savelog.d(TAG, debug, "onCreateView()");

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tool_arithint, container, false);

        mTextViews = findTextViews(v);
        mInputView1 = (EditText) v.findViewById(R.id.fragmentToolArithInt_number1);
        mInputView2 = (EditText) v.findViewById(R.id.fragmentToolArithInt_number2);

        mInputTextWatcher1 = new InputTextWatcher(this, 0);
        mInputTextWatcher2 = new InputTextWatcher(this, 1);

        mInputView1.addTextChangedListener(mInputTextWatcher1);
        mInputView2.addTextChangedListener(mInputTextWatcher2);

        formatScreenData();
        return v;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mInputView1!=null) {
            mInputView1.removeTextChangedListener(mInputTextWatcher1);
            mInputView1=null;
        }
        if (mInputView2!=null) {
            mInputView2.removeTextChangedListener(mInputTextWatcher2);
            mInputView2=null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }




    private static class InputTextWatcher implements TextWatcher {
        ArithIntFragment hostFragment;
        int field;
        public InputTextWatcher(ArithIntFragment hostFragment, int field) {
            super();
            this.hostFragment = hostFragment;
            this.field = field;
            Savelog.d(TAG, debug, "textwatcher for index " + field);
        }
        @Override
        public void afterTextChanged(Editable arg0) {}
        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
        @Override
        public void onTextChanged(CharSequence c, int start, int before, int count) {
            String data = "";
            boolean dataValid = true;
            int dataValue = 0;
            if (c!=null && c.toString().trim().length()>0) {
                data = c.toString().trim();
            }
            Savelog.d(TAG, debug, "Entered " + data);

            try {
                dataValue = Integer.valueOf(data);
                if (dataValue>MaxInteger || dataValue<MinInteger) {
                    dataValid = false;
                }
            } catch (NumberFormatException e) {
                dataValid = false;
            }
            if (dataValid) {
                if (field == 0) {
                    hostFragment.mInput1 = data;
                    hostFragment.mNumber1 = dataValue;
                }
                else if (field == 1) {
                    hostFragment.mInput2 = data;
                    hostFragment.mNumber2 = dataValue;
                }
            }
            else {
                if (field == 0) {
                    hostFragment.mInput1 = "";
                    hostFragment.mNumber1 = 0;
                }
                else if (field == 1) {
                    hostFragment.mInput2 = "";
                    hostFragment.mNumber2 = 0;
                }
            }

            hostFragment.formatScreenData();
        }
        public void cleanup() { hostFragment = null; }
    }


    private void formatScreenData() {
        final String blanks[] = {blank, blank, blank, blank, blank, blank, blank, blank};

        boolean ready1 = (mInput1!=null && mInput1.length()>0);
        boolean ready2 = (mInput2!=null && mInput2.length()>0);

        {
            // Fill up row 1
            int row = 1;
            if (ready1) {
                String data[] = getBits(mNumber1);
                setTextViewData(row, data);
                setTextViewData(row, 1, Integer.toString(mNumber1));
            }
            else {
                setTextViewData(row, blanks);
                setTextViewData(row, 1, blank);
            }
        }

        {
            // Fill up row 2
            int row = 2;
            if (ready2) {
                String data[] = getBits(mNumber2);
                setTextViewData(row, data);
                setTextViewData(row, 1, Integer.toString(mNumber2));
            }
            else {
                setTextViewData(row, blanks);
                setTextViewData(row, 1, blank);
            }
        }

        {
            if (ready1 && ready2) {
                // Fill up rows 3 and 0 (carry)
                int row = 3;
                String data[] = getBits(mNumber1+mNumber2);
                setTextViewData(row, data);  // sum in binary
                setTextViewData(row, 1, Integer.toString((mNumber1+mNumber2)));  // sum in decimal

                // Fill up the row of carries
                row = 0;
                for (int column=EndNumberBit-1; column>=ExcessBit; column--) {
                    String carryIn = getTextViewData(row, column+1);
                    String bit1 = getTextViewData(row+1, column+1);
                    String bit2 = getTextViewData(row+2, column+1);
                    int count = 0;
                    if (carryIn.equals("1")) count++;
                    if (bit1.equals("1")) count++;
                    if (bit2.equals("1")) count++;
                    if (count>1) {
                        setTextViewData(row, column, "1");
                    }
                    else {
                        setTextViewData(row, column, blank);
                    }
                }
            }
            else {
                // either one number is not ready.
                setTextViewData(3, 1, blank); // empty sum in decimal
                setTextViewData(0, blanks);   // empty carries
                setTextViewData(3, blanks);   // empty sum in binary
                setTextViewData(0, ExcessBit, blank); // empty excess bit
            }
        }

        {
            // Test for overflow
            int carryIn = 0;
            int carryOut = 0;
            try {
                String data = getTextViewData(0, StartNumberBit);
                Savelog.d(TAG, debug, "carryIn=\"" + data + "\"");
                if (data.trim().length()>0)
                    carryIn = Integer.valueOf(data);
            } catch (Exception e) {
                Savelog.w(TAG, "Cannot convert data " + e.getMessage(), e);
            }
            try {
                String data = getTextViewData(0, ExcessBit);
                Savelog.d(TAG, debug, "carryOut=\"" + data + "\"");
                if (data.trim().length()>0)
                    carryOut = Integer.valueOf(data);
            } catch (Exception e) {
                Savelog.w(TAG, "Cannot convert data " + e.getMessage(), e);
            }


            if (carryIn!=carryOut) {
                Savelog.d(TAG, debug, "carryIn=" + carryIn + " carryOut=" + carryOut);

                if (getTextViewData(0, ExcessBit).equals(blank)) {
                    setTextViewData(0, ExcessBit, "0");
                }

                setTextViewData(3, NoteCell, "Overflow!");

            }
            else {
                setTextViewData(3, NoteCell, blank);
            }
        }

    }


    private static String[] getBits(int number) {
        String[] bitString = new String[NumberOfBits];

        for (int bit=0; bit<NumberOfBits; bit++) {
            int mask = 1 << bit;
            boolean value = (number & mask) !=0;
            int index = NumberOfBits-bit-1;
            if (value) {
                bitString[index] = "1";
            }
            else {
                bitString[index] = "0";
            }
        }

        String data = "";
        for (int bit=0; bit<NumberOfBits; bit++)
            data += bitString[bit];
        Savelog.d(TAG, debug, "number " + number + "=" + data);

        return bitString;
    }


    private void setTextViewData(int row, String data[]) {
        for (int pos=0; pos<NumberOfBits; pos++) {
            int column = StartNumberBit + pos;
            setTextViewData(row, column, data[pos]);
        }
    }

    private void setTextViewData(int row, int column, CharSequence data) {
        if (row<0 || row>=GridHeight || column<0 || column>=GridWidth) return;
        int index = row*GridWidth+column;
        if (mTextViews[index]!=null) {
            mTextViews[index].setText(data);
        }
    }
    private String getTextViewData(int row, int column) {
        if (row<0 || row>=GridHeight || column<0 || column>=GridWidth) return "";
        int index = row*GridWidth+column;
        if (mTextViews[index]!=null) {
            return mTextViews[index].getText().toString();
        }
        return "";
    }

    private TextView[] findTextViews(View v) {
        TextView[] textViews = new TextView[GridWidth*GridHeight];
        for (int row=0; row<GridHeight; row++) {
            for (int column=0; column<GridWidth; column++) {
                int index = row*GridWidth+column;
                textViews[index] = (TextView) v.findViewById(getTableCellIds(row, column));
            }
        }
        return textViews;
    }

    private int getTableCellIds(int row, int column) {
        if (row<0 || row>=GridHeight || column<0 || column>=GridWidth) return 0;
        int index = row*GridWidth+column;
        int tableCellId[] = {
            R.id.fragmentToolArithInt_cell_0_0,
            R.id.fragmentToolArithInt_cell_0_1,
            R.id.fragmentToolArithInt_cell_0_2,
            R.id.fragmentToolArithInt_cell_0_3,
            R.id.fragmentToolArithInt_cell_0_4,
            R.id.fragmentToolArithInt_cell_0_5,
            R.id.fragmentToolArithInt_cell_0_6,
            R.id.fragmentToolArithInt_cell_0_7,
            R.id.fragmentToolArithInt_cell_0_8,
            R.id.fragmentToolArithInt_cell_0_9,
            R.id.fragmentToolArithInt_cell_0_10,
            R.id.fragmentToolArithInt_cell_0_11,
            R.id.fragmentToolArithInt_cell_1_0,
            R.id.fragmentToolArithInt_cell_1_1,
            R.id.fragmentToolArithInt_cell_1_2,
            R.id.fragmentToolArithInt_cell_1_3,
            R.id.fragmentToolArithInt_cell_1_4,
            R.id.fragmentToolArithInt_cell_1_5,
            R.id.fragmentToolArithInt_cell_1_6,
            R.id.fragmentToolArithInt_cell_1_7,
            R.id.fragmentToolArithInt_cell_1_8,
            R.id.fragmentToolArithInt_cell_1_9,
            R.id.fragmentToolArithInt_cell_1_10,
            R.id.fragmentToolArithInt_cell_1_11,
            R.id.fragmentToolArithInt_cell_2_0,
            R.id.fragmentToolArithInt_cell_2_1,
            R.id.fragmentToolArithInt_cell_2_2,
            R.id.fragmentToolArithInt_cell_2_3,
            R.id.fragmentToolArithInt_cell_2_4,
            R.id.fragmentToolArithInt_cell_2_5,
            R.id.fragmentToolArithInt_cell_2_6,
            R.id.fragmentToolArithInt_cell_2_7,
            R.id.fragmentToolArithInt_cell_2_8,
            R.id.fragmentToolArithInt_cell_2_9,
            R.id.fragmentToolArithInt_cell_2_10,
            R.id.fragmentToolArithInt_cell_2_11,
            R.id.fragmentToolArithInt_cell_3_0,
            R.id.fragmentToolArithInt_cell_3_1,
            R.id.fragmentToolArithInt_cell_3_2,
            R.id.fragmentToolArithInt_cell_3_3,
            R.id.fragmentToolArithInt_cell_3_4,
            R.id.fragmentToolArithInt_cell_3_5,
            R.id.fragmentToolArithInt_cell_3_6,
            R.id.fragmentToolArithInt_cell_3_7,
            R.id.fragmentToolArithInt_cell_3_8,
            R.id.fragmentToolArithInt_cell_3_9,
            R.id.fragmentToolArithInt_cell_3_10,
            R.id.fragmentToolArithInt_cell_3_11
        };
        return tableCellId[index];
    }
}
