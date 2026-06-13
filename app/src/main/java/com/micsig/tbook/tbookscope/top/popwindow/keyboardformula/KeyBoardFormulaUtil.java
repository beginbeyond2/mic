package com.micsig.tbook.tbookscope.top.popwindow.keyboardformula;

import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;

import java.util.ArrayList;

public class KeyBoardFormulaUtil implements IKeyBoardFormula {
    private static final String TAG = "KeyBoardFormulaUtil";


    public static String amFormulaToScope(String exprString, String centerTimeBase) {
        return exprString
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", "P")
                .replace(App.get().getResources().getString(R.string.key_formula_tb), handleTimeBase(centerTimeBase));
    }

    private static String[] searchList;
    private static String[] keyList;

    public static String[] getSearchList() {
        if (searchList == null) {
            searchList = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.key_formula_search_list);
        }
        return searchList;
    }

    public static String[] getKeyList() {
        if (keyList == null) {
            keyList = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.key_formula_show_list);
        }
        return keyList;
    }

    public static ArrayList<String> getSelectionListFromShowText(String string) {
        if (searchList == null) {
            searchList = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.key_formula_search_list);
        }
        ArrayList<String> list = new ArrayList<>();

        boolean startWithSearchItem = true;//这个变量是为了处理 键盘乱输入时避免下面死循环问题。
        while (!StrUtil.isEmpty(string) && startWithSearchItem) {
            boolean nowHasSearchItem = false;
            for (int i = 0; i < searchList.length; i++) {
                if (string.startsWith(searchList[i])) {
                    nowHasSearchItem = true;
                    break;
                }
            }
            startWithSearchItem = nowHasSearchItem;

            for (int i = 0; i < searchList.length; i++) {
                if (string.startsWith(searchList[i])) {
                    list.add(searchList[i]);
                    string = string.substring(searchList[i].length());
                    break;
                }
            }
            Logger.d(TAG, "getSelectionListFromShowText() called with: string = [" + string + "]");
        }
        Logger.d(TAG, "getSelectionListFromShowText() called with: string = [" + string + "], return :" + list);
        return list;
    }

    public static int getIndex(String s) {
        if (StrUtil.isEmpty(s)) {
            return INDEX_null;
        }
        if (keyList == null) {
            keyList = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.key_formula_show_list);
        }
        for (int i = 0; i < keyList.length; i++) {
            if (keyList[i].equals(s)) {
                return i;
            }
        }
        return INDEX_null;
    }

    /**
     * @param keyIndex                当前键位的序号...
     * @param eAddSubAfterBracketLeft +-之后的左括号的可点击性（在不在e±之后不一样）
     * @param numberAfterPoint        数字之后的小数点的可点击性（是否已包含小数点）
     * @param afterBracketRight       后括号的可点击性（是否比前括号多）
     * @param zeroAfterZero           0之后的0的可点击性（是否是纯0数字串）
     * @return
     */
    public static boolean[] getVisibleListFromCurSelection(int keyIndex, boolean eAddSubAfterBracketLeft,
                                                           boolean numberAfterPoint, boolean numberAfterE,
                                                           boolean afterBracketRight, boolean zeroAfterZero) {
        Logger.d(TAG, "getVisibleListFromCurSelection() called with: keyIndex = [" + keyIndex + "], eAddSubAfterBracketLeft = [" + eAddSubAfterBracketLeft + "], numberAfterPoint = [" + numberAfterPoint + "], numberAfterE = [" + numberAfterE + "], afterBracketRight = [" + afterBracketRight + "], zeroAfterZero = [" + zeroAfterZero + "]");
        if (keyList == null) {
            keyList = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.key_formula_show_list);
        }
        boolean[] visibleList = new boolean[keyList.length];
        for (int i = 0; i < visibleList.length; i++) {
            visibleList[i] = true;
        }
        switch (keyIndex) {
            case INDEX_null: {
                visibleList[INDEX_mul] = false;
                visibleList[INDEX_div] = false;
                visibleList[INDEX_less] = false;
                visibleList[INDEX_greater] = false;
                visibleList[INDEX_less_equal] = false;
                visibleList[INDEX_greater_equal] = false;
                visibleList[INDEX_bracket_right] = false;
                visibleList[INDEX_equal] = false;
                visibleList[INDEX_not_equal] = false;
                visibleList[INDEX_and] = false;
                visibleList[INDEX_or] = false;
                visibleList[INDEX_E] = false;
                visibleList[INDEX_point] = false;
                visibleList[INDEX_m] = false;
                visibleList[INDEX_T] = false;
                visibleList[INDEX_u] = false;
                visibleList[INDEX_G] = false;
                visibleList[INDEX_n] = false;
                visibleList[INDEX_M] = false;
                visibleList[INDEX_f] = false;
                visibleList[INDEX_p] = false;
                visibleList[INDEX_k] = false;
            }
            break;
            case INDEX_add:
            case INDEX_sub:
            case INDEX_mul:
            case INDEX_div: {
                if (keyIndex == INDEX_add || keyIndex == INDEX_sub) {
                    visibleList[INDEX_bracket_left] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_sqrt] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_abs] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_deg] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_rad] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_exp] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_diff] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_ln] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_sine] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_cos] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_tan] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_intg] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_lg] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_arcsin] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_arccos] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_arctan] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_ch1] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_ch2] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_ch3] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_ch4] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_ch5] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_ch6] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_ch7] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_ch8] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_var1] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_var2] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_time_base] = eAddSubAfterBracketLeft;
                    visibleList[INDEX_pi] = eAddSubAfterBracketLeft;
                }
                visibleList[INDEX_add] = false;
                visibleList[INDEX_sub] = false;
                visibleList[INDEX_mul] = false;
                visibleList[INDEX_div] = false;
                visibleList[INDEX_less] = false;
                visibleList[INDEX_greater] = false;
                visibleList[INDEX_less_equal] = false;
                visibleList[INDEX_greater_equal] = false;
                visibleList[INDEX_bracket_right] = false;
                visibleList[INDEX_equal] = false;
                visibleList[INDEX_not_equal] = false;
                visibleList[INDEX_and] = false;
                visibleList[INDEX_or] = false;
                visibleList[INDEX_no] = false;
                visibleList[INDEX_E] = false;
                visibleList[INDEX_point] = false;
                visibleList[INDEX_m] = false;
                visibleList[INDEX_T] = false;
                visibleList[INDEX_u] = false;
                visibleList[INDEX_G] = false;
                visibleList[INDEX_n] = false;
                visibleList[INDEX_M] = false;
                visibleList[INDEX_f] = false;
                visibleList[INDEX_p] = false;
                visibleList[INDEX_k] = false;
            }
            break;
            case INDEX_less:
            case INDEX_greater:
            case INDEX_less_equal:
            case INDEX_greater_equal:
            case INDEX_equal:
            case INDEX_not_equal:
            case INDEX_and:
            case INDEX_or: {
                visibleList[INDEX_add] = false;
                visibleList[INDEX_sub] = false;
                visibleList[INDEX_mul] = false;
                visibleList[INDEX_div] = false;
                visibleList[INDEX_less] = false;
                visibleList[INDEX_greater] = false;
                visibleList[INDEX_less_equal] = false;
                visibleList[INDEX_greater_equal] = false;
                visibleList[INDEX_bracket_right] = false;
                visibleList[INDEX_equal] = false;
                visibleList[INDEX_not_equal] = false;
                visibleList[INDEX_and] = false;
                visibleList[INDEX_or] = false;
                visibleList[INDEX_E] = false;
                visibleList[INDEX_point] = false;
                visibleList[INDEX_m] = false;
                visibleList[INDEX_T] = false;
                visibleList[INDEX_u] = false;
                visibleList[INDEX_G] = false;
                visibleList[INDEX_n] = false;
                visibleList[INDEX_M] = false;
                visibleList[INDEX_f] = false;
                visibleList[INDEX_p] = false;
                visibleList[INDEX_k] = false;
            }
            break;
            case INDEX_sqrt:
            case INDEX_abs:
            case INDEX_deg:
            case INDEX_rad:
            case INDEX_exp:
            case INDEX_diff:
            case INDEX_ln:
            case INDEX_sine:
            case INDEX_cos:
            case INDEX_tan:
            case INDEX_intg:
            case INDEX_lg:
            case INDEX_arcsin:
            case INDEX_arccos:
            case INDEX_arctan:
            case INDEX_no:
            case INDEX_bracket_left: {
                visibleList[INDEX_mul] = false;
                visibleList[INDEX_div] = false;
                visibleList[INDEX_less] = false;
                visibleList[INDEX_greater] = false;
                visibleList[INDEX_less_equal] = false;
                visibleList[INDEX_greater_equal] = false;
                visibleList[INDEX_bracket_right] = false;
                visibleList[INDEX_equal] = false;
                visibleList[INDEX_not_equal] = false;
                visibleList[INDEX_and] = false;
                visibleList[INDEX_or] = false;
                visibleList[INDEX_no] = false;
                visibleList[INDEX_E] = false;
                visibleList[INDEX_point] = false;
                visibleList[INDEX_m] = false;
                visibleList[INDEX_T] = false;
                visibleList[INDEX_u] = false;
                visibleList[INDEX_G] = false;
                visibleList[INDEX_n] = false;
                visibleList[INDEX_M] = false;
                visibleList[INDEX_f] = false;
                visibleList[INDEX_p] = false;
                visibleList[INDEX_k] = false;
            }
            break;
            case INDEX_bracket_right: {
                visibleList[INDEX_bracket_left] = false;
                visibleList[INDEX_bracket_right] = afterBracketRight;
                visibleList[INDEX_no] = false;
                visibleList[INDEX_sqrt] = false;
                visibleList[INDEX_abs] = false;
                visibleList[INDEX_deg] = false;
                visibleList[INDEX_rad] = false;
                visibleList[INDEX_exp] = false;
                visibleList[INDEX_diff] = false;
                visibleList[INDEX_ln] = false;
                visibleList[INDEX_sine] = false;
                visibleList[INDEX_cos] = false;
                visibleList[INDEX_tan] = false;
                visibleList[INDEX_intg] = false;
                visibleList[INDEX_lg] = false;
                visibleList[INDEX_arcsin] = false;
                visibleList[INDEX_arccos] = false;
                visibleList[INDEX_arctan] = false;
                visibleList[INDEX_ch1] = false;
                visibleList[INDEX_ch2] = false;
                visibleList[INDEX_ch3] = false;
                visibleList[INDEX_ch4] = false;
                visibleList[INDEX_ch5] = false;
                visibleList[INDEX_ch6] = false;
                visibleList[INDEX_ch7] = false;
                visibleList[INDEX_ch8] = false;
                visibleList[INDEX_pi] = false;
                visibleList[INDEX_time_base] = false;
                visibleList[INDEX_E] = false;
                visibleList[INDEX_var1] = false;
                visibleList[INDEX_var2] = false;
                visibleList[INDEX_0] = false;
                visibleList[INDEX_1] = false;
                visibleList[INDEX_2] = false;
                visibleList[INDEX_3] = false;
                visibleList[INDEX_4] = false;
                visibleList[INDEX_5] = false;
                visibleList[INDEX_6] = false;
                visibleList[INDEX_7] = false;
                visibleList[INDEX_8] = false;
                visibleList[INDEX_9] = false;
                visibleList[INDEX_point] = false;
            }
            break;
            case INDEX_ch1:
            case INDEX_ch2:
            case INDEX_ch3:
            case INDEX_ch4:
            case INDEX_ch5:
            case INDEX_ch6:
            case INDEX_ch7:
            case INDEX_ch8:
            case INDEX_time_base:
            case INDEX_pi:
            case INDEX_var1:
            case INDEX_var2:
            case INDEX_m:
            case INDEX_T:
            case INDEX_u:
            case INDEX_G:
            case INDEX_n:
            case INDEX_M:
            case INDEX_f:
            case INDEX_p:
            case INDEX_k: {
                visibleList[INDEX_bracket_right] = afterBracketRight;
                visibleList[INDEX_sqrt] = false;
                visibleList[INDEX_abs] = false;
                visibleList[INDEX_deg] = false;
                visibleList[INDEX_rad] = false;
                visibleList[INDEX_exp] = false;
                visibleList[INDEX_diff] = false;
                visibleList[INDEX_ln] = false;
                visibleList[INDEX_sine] = false;
                visibleList[INDEX_cos] = false;
                visibleList[INDEX_tan] = false;
                visibleList[INDEX_intg] = false;
                visibleList[INDEX_lg] = false;
                visibleList[INDEX_arcsin] = false;
                visibleList[INDEX_arccos] = false;
                visibleList[INDEX_arctan] = false;
                visibleList[INDEX_no] = false;
                visibleList[INDEX_bracket_left] = false;
                visibleList[INDEX_ch1] = false;
                visibleList[INDEX_ch2] = false;
                visibleList[INDEX_ch3] = false;
                visibleList[INDEX_ch4] = false;
                visibleList[INDEX_ch5] = false;
                visibleList[INDEX_ch6] = false;
                visibleList[INDEX_ch7] = false;
                visibleList[INDEX_ch8] = false;
                visibleList[INDEX_time_base] = false;
                visibleList[INDEX_pi] = false;
                visibleList[INDEX_var1] = false;
                visibleList[INDEX_var2] = false;
                visibleList[INDEX_E] = false;
                visibleList[INDEX_0] = false;
                visibleList[INDEX_1] = false;
                visibleList[INDEX_2] = false;
                visibleList[INDEX_3] = false;
                visibleList[INDEX_4] = false;
                visibleList[INDEX_5] = false;
                visibleList[INDEX_6] = false;
                visibleList[INDEX_7] = false;
                visibleList[INDEX_8] = false;
                visibleList[INDEX_9] = false;
                visibleList[INDEX_point] = false;
            }
            break;
            case INDEX_E: {
                visibleList[INDEX_mul] = false;
                visibleList[INDEX_div] = false;
                visibleList[INDEX_less] = false;
                visibleList[INDEX_greater] = false;
                visibleList[INDEX_less_equal] = false;
                visibleList[INDEX_greater_equal] = false;
                visibleList[INDEX_equal] = false;
                visibleList[INDEX_not_equal] = false;
                visibleList[INDEX_and] = false;
                visibleList[INDEX_or] = false;
                visibleList[INDEX_sqrt] = false;
                visibleList[INDEX_abs] = false;
                visibleList[INDEX_deg] = false;
                visibleList[INDEX_rad] = false;
                visibleList[INDEX_exp] = false;
                visibleList[INDEX_diff] = false;
                visibleList[INDEX_ln] = false;
                visibleList[INDEX_sine] = false;
                visibleList[INDEX_cos] = false;
                visibleList[INDEX_tan] = false;
                visibleList[INDEX_intg] = false;
                visibleList[INDEX_lg] = false;
                visibleList[INDEX_arcsin] = false;
                visibleList[INDEX_arccos] = false;
                visibleList[INDEX_arctan] = false;
                visibleList[INDEX_no] = false;
                visibleList[INDEX_bracket_left] = false;
                visibleList[INDEX_bracket_right] = false;
                visibleList[INDEX_ch1] = false;
                visibleList[INDEX_ch2] = false;
                visibleList[INDEX_ch3] = false;
                visibleList[INDEX_ch4] = false;
                visibleList[INDEX_ch5] = false;
                visibleList[INDEX_ch6] = false;
                visibleList[INDEX_ch7] = false;
                visibleList[INDEX_ch8] = false;
                visibleList[INDEX_time_base] = false;
                visibleList[INDEX_pi] = false;
                visibleList[INDEX_var1] = false;
                visibleList[INDEX_var2] = false;
                visibleList[INDEX_E] = false;
                visibleList[INDEX_point] = false;
                visibleList[INDEX_m] = false;
                visibleList[INDEX_T] = false;
                visibleList[INDEX_u] = false;
                visibleList[INDEX_G] = false;
                visibleList[INDEX_n] = false;
                visibleList[INDEX_M] = false;
                visibleList[INDEX_f] = false;
                visibleList[INDEX_p] = false;
                visibleList[INDEX_k] = false;
            }
            break;
            case INDEX_0:
            case INDEX_1:
            case INDEX_2:
            case INDEX_3:
            case INDEX_4:
            case INDEX_5:
            case INDEX_6:
            case INDEX_7:
            case INDEX_8:
            case INDEX_9: {
                visibleList[INDEX_sqrt] = false;
                visibleList[INDEX_abs] = false;
                visibleList[INDEX_deg] = false;
                visibleList[INDEX_rad] = false;
                visibleList[INDEX_exp] = false;
                visibleList[INDEX_diff] = false;
                visibleList[INDEX_ln] = false;
                visibleList[INDEX_sine] = false;
                visibleList[INDEX_cos] = false;
                visibleList[INDEX_tan] = false;
                visibleList[INDEX_intg] = false;
                visibleList[INDEX_lg] = false;
                visibleList[INDEX_arcsin] = false;
                visibleList[INDEX_arccos] = false;
                visibleList[INDEX_arctan] = false;
                visibleList[INDEX_no] = false;
                visibleList[INDEX_bracket_left] = false;
                visibleList[INDEX_bracket_right] = afterBracketRight;
                visibleList[INDEX_ch1] = false;
                visibleList[INDEX_ch2] = false;
                visibleList[INDEX_ch3] = false;
                visibleList[INDEX_ch4] = false;
                visibleList[INDEX_ch5] = false;
                visibleList[INDEX_ch6] = false;
                visibleList[INDEX_ch7] = false;
                visibleList[INDEX_ch8] = false;
                visibleList[INDEX_time_base] = false;
                visibleList[INDEX_pi] = false;
                visibleList[INDEX_var1] = false;
                visibleList[INDEX_var2] = false;
                visibleList[INDEX_E] = numberAfterE;
                visibleList[INDEX_point] = numberAfterPoint;
                visibleList[INDEX_0] = zeroAfterZero;
            }
            break;
            case INDEX_point: {
                visibleList[INDEX_add] = false;
                visibleList[INDEX_sub] = false;
                visibleList[INDEX_mul] = false;
                visibleList[INDEX_div] = false;
                visibleList[INDEX_bracket_left] = false;
                visibleList[INDEX_less] = false;
                visibleList[INDEX_greater] = false;
                visibleList[INDEX_less_equal] = false;
                visibleList[INDEX_greater_equal] = false;
                visibleList[INDEX_bracket_right] = false;
                visibleList[INDEX_equal] = false;
                visibleList[INDEX_not_equal] = false;
                visibleList[INDEX_and] = false;
                visibleList[INDEX_or] = false;
                visibleList[INDEX_no] = false;
                visibleList[INDEX_sqrt] = false;
                visibleList[INDEX_abs] = false;
                visibleList[INDEX_deg] = false;
                visibleList[INDEX_rad] = false;
                visibleList[INDEX_exp] = false;
                visibleList[INDEX_diff] = false;
                visibleList[INDEX_ln] = false;
                visibleList[INDEX_sine] = false;
                visibleList[INDEX_cos] = false;
                visibleList[INDEX_tan] = false;
                visibleList[INDEX_intg] = false;
                visibleList[INDEX_lg] = false;
                visibleList[INDEX_arcsin] = false;
                visibleList[INDEX_arccos] = false;
                visibleList[INDEX_arctan] = false;
                visibleList[INDEX_ch1] = false;
                visibleList[INDEX_ch2] = false;
                visibleList[INDEX_ch3] = false;
                visibleList[INDEX_ch4] = false;
                visibleList[INDEX_pi] = false;
                visibleList[INDEX_ch5] = false;
                visibleList[INDEX_ch6] = false;
                visibleList[INDEX_ch7] = false;
                visibleList[INDEX_ch8] = false;
                visibleList[INDEX_time_base] = false;
                visibleList[INDEX_E] = false;
                visibleList[INDEX_var1] = false;
                visibleList[INDEX_var2] = false;
                visibleList[INDEX_m] = false;
                visibleList[INDEX_T] = false;
                visibleList[INDEX_u] = false;
                visibleList[INDEX_G] = false;
                visibleList[INDEX_n] = false;
                visibleList[INDEX_M] = false;
                visibleList[INDEX_point] = false;
                visibleList[INDEX_f] = false;
                visibleList[INDEX_p] = false;
                visibleList[INDEX_k] = false;
            }
            break;
            case INDEX_enter:
            case INDEX_del:
                break;
        }
        visibleList[INDEX_null1] = false;
        return visibleList;
    }

    public static boolean isNumber(int index) {
        return index == INDEX_0 || index == INDEX_1 || index == INDEX_2
                || index == INDEX_3 || index == INDEX_4 || index == INDEX_5
                || index == INDEX_6 || index == INDEX_7 || index == INDEX_8
                || index == INDEX_9;
    }

    public static String handleTimeBase(String timeBase) {
        timeBase = timeBase.contains("\n") ? timeBase.split("\n")[1] : timeBase;
        if (timeBase.isEmpty()) {
            timeBase = "1";
        } else {
            timeBase = (TBookUtil.getSFromTime(timeBase.replace(" ", "")) + "").replaceAll(" ", "");
        }
        return timeBase;
    }
}
