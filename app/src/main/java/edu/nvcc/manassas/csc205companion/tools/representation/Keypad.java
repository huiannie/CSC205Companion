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


public class Keypad {
    private static final int Type_base = 1;
    private static final int Type_digit = 2;
    private static final int Type_operator = 3;

    public enum Button {
        Binary("Bin", Type_base),
        Decimal("Dec", Type_base),
        Hexadecimal("Hex", Type_base),
        ZERO("0", Type_digit),
        ONE("1", Type_digit),
        TWO("2", Type_digit),
        THREE("3", Type_digit),
        FOUR("4", Type_digit),
        FIVE("5", Type_digit),
        SIX("6", Type_digit),
        SEVEN("7", Type_digit),
        EIGHT("8", Type_digit),
        NINE("9", Type_digit),
        A("A", Type_digit),
        B("B", Type_digit),
        C("C", Type_digit),
        D("D", Type_digit),
        E("E", Type_digit),
        F("F", Type_digit),
        sign("±", Type_operator),
        point(".", Type_operator),
        delete("DEL", Type_operator),
        clear("CLR", Type_operator),
        blank("", Type_operator);


        CharSequence label = "";
        int type = 0;

        Button(CharSequence label, int type) {
            if (label != null) this.label = label;
            if (type==Type_base || type== Type_digit || type==Type_operator) this.type = type;
        }
        public CharSequence getLabel() {
            return label;
        }

        public boolean isBase() { return type==Type_base; }
        public boolean isDigit() { return type==Type_digit; }
        public boolean isOperator() { return type==Type_operator; }

        public int getBase() {
            if (label.equals(Binary.getLabel())) {
                return 2;
            }
            else if (label.equals(Decimal.getLabel())) {
                return 10;
            }
            else if (label.equals(Hexadecimal.getLabel())) {
                return 16;
            }
            else return 0;
        }
        public CharSequence getDigit() {
            if (type==Type_digit) {
                return label;
            }
            else {
                return "";
            }
        }
        public int getDigitValue() {
            if (type == Type_digit) {
                return getValue(label);
            }
            return 0;
        }
        public static int getValue(CharSequence label) {
            if (label.equals(ZERO.getLabel())) return 0;
            else if (label.equals(ONE.getLabel())) return 1;
            else if (label.equals(TWO.getLabel())) return 2;
            else if (label.equals(THREE.getLabel())) return 3;
            else if (label.equals(FOUR.getLabel())) return 4;
            else if (label.equals(FIVE.getLabel())) return 5;
            else if (label.equals(SIX.getLabel())) return 6;
            else if (label.equals(SEVEN.getLabel())) return 7;
            else if (label.equals(EIGHT.getLabel())) return 8;
            else if (label.equals(NINE.getLabel())) return 9;
            else if (label.equals(A.getLabel())) return 10;
            else if (label.equals(B.getLabel())) return 11;
            else if (label.equals(C.getLabel())) return 12;
            else if (label.equals(D.getLabel())) return 13;
            else if (label.equals(E.getLabel())) return 14;
            else if (label.equals(F.getLabel())) return 15;
            else return 0;
        }

        public boolean isSign() {
            return (type==Type_operator && label.equals(sign.getLabel()));
        }
        public boolean isPoint() {
            return (type==Type_operator && label.equals(point.getLabel()));
        }
        public boolean isClear() {
            return (type==Type_operator && label.equals(clear.getLabel()));
        }
        public boolean isDelete() {
            return (type==Type_operator && label.equals(delete.getLabel()));
        }
    }


    // For entering numbers
    public static final Button[] numericButtons = new Button[] {
            Button.ZERO,
            Button.ONE,
            Button.TWO,
            Button.THREE,
            Button.FOUR,
            Button.FIVE,
            Button.SIX,
            Button.SEVEN,
            Button.EIGHT,
            Button.NINE,
            Button.A,
            Button.B,
            Button.C,
            Button.D,
            Button.E,
            Button.F,
            Button.sign,
            Button.point,
            Button.delete,
            Button.clear
    };

    // For entering codes
    public static final Button[] codeButtons = new Button[] {
            Button.ZERO,
            Button.ONE,
            Button.TWO,
            Button.THREE,
            Button.FOUR,
            Button.FIVE,
            Button.SIX,
            Button.SEVEN,
            Button.EIGHT,
            Button.NINE,
            Button.A,
            Button.B,
            Button.C,
            Button.D,
            Button.E,
            Button.F,
            Button.delete,
            Button.clear
    };

    // For choosing a base
    public static final Button[] baseButtons = new Button[] {
            Button.Binary,
            Button.Decimal,
            Button.Hexadecimal,
    };



}
