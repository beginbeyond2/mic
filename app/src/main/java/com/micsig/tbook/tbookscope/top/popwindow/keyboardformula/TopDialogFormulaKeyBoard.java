package com.micsig.tbook.tbookscope.top.popwindow.keyboardformula;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.micsig.base.DoubleUtil;
import com.micsig.base.keyevent.KeyEventUtil;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.math.MathExprError;
import com.micsig.tbook.scope.math.MathExprWave;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainbottom.MainHolderBottom;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.Screen;
import com.micsig.tbook.ui.MSelectionEditText;
import com.micsig.tbook.ui.util.StrUtil;

import java.util.ArrayList;

/**
 * 高级数学表达式
 */
public class TopDialogFormulaKeyBoard extends AbsoluteLayout implements IKeyBoardFormula {
    private static final String TAG = "TopFormulaKeyBoard";
    /**
     * 该公式最长长度
     */
    private static final int COUNT_FORMULA = 36;

    private Context context;
    private MSelectionEditText tvShow;
    private TextView tvMsgError;
    private RelativeLayout gridView;
    private ArrayList<String> showList = new ArrayList<>();
    private OnDismissListener onDismissListener;
    private String timeBase;
    private RelativeLayout showKeyBoardLayout;
    private boolean isFormatting = false;

    private ViewGroup rootViewGroup;

    public interface OnDismissListener {
        void onDismiss(String show);
    }

    public TopDialogFormulaKeyBoard(Context context) {
        this(context, null);
    }

    public TopDialogFormulaKeyBoard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopDialogFormulaKeyBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        setClickable(true);
        rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_keyboard_formula, this);

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();
                return false;
            }
        });
        initView(rootViewGroup);
        hide();
    }

    private void initView(View view) {
        showKeyBoardLayout = view.findViewById(R.id.showKeyBoardLayout);
        tvShow = (MSelectionEditText) view.findViewById(R.id.formulaEdit);
        View leftSelection = view.findViewById(R.id.formulaSelectionLeft);
        View rightSelection = view.findViewById(R.id.formulaSelectionRight);
        tvMsgError = (TextView) view.findViewById(R.id.formulaMsgError);

        leftSelection.setOnClickListener(onClickListener);
        rightSelection.setOnClickListener(onClickListener);
        leftSelection.setOnLongClickListener(onLongClickListener);
        rightSelection.setOnLongClickListener(onLongClickListener);
        tvShow.setShowSoftInputOnFocus(false);
        tvShow.setLongClickable(false);
        tvShow.setOnSelectionChanged(onSelectionChanged);
        tvShow.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        tvShow.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });

        tvShow.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    Logger.d(TAG, "event= " + event.getKeyCode());
                    if (onItemClickListener == null) return true;
                    if (KeyEventUtil.isConfirmKey(event)) {
                        onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_enter));
                        return true;
                    }
                    if (KeyEventUtil.isBackKey(event)) {
                        onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_del));
                        return true;
                    }
                    if (KeyEventUtil.isForwardDel(event)) {
                        if (tvShow.getSelectionStart() == tvShow.getText().length()) {
                            return true;
                        }
                        rightSelection.performClick();
                        onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_del));
                        return true;
                    }
                    if (KeyEventUtil.isLeft(event)) {
                        leftSelection.performClick();
                        return true;
                    }
                    if (KeyEventUtil.isRight(event)) {
                        rightSelection.performClick();
                        return true;
                    }
                }
                return false;
            }
        });

        tvShow.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Logger.d(TAG, "beforeTextChanged=" + s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Logger.d(TAG, "onTextChanged=" + s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                String str = s.toString();
                str = formatExpress(str);
                int startSelection = tvShow.getSelectionStart();
                if (str.length() > COUNT_FORMULA) {
                    str = str.substring(0, COUNT_FORMULA);
                }
                showList = KeyBoardFormulaUtil.getSelectionListFromShowText(str);
                if (!s.toString().equals(str)) {
                    s = Editable.Factory.getInstance().newEditable(str);
                    int selection = str.length();
                    if (selection > COUNT_FORMULA) {
                        selection = COUNT_FORMULA;
                    }
                    tvShow.setText(s.toString());
                    tvShow.setSelection(s.toString().length());
                } else {
                    String leftSelection = str.substring(0, startSelection);
                    tvShow.setText(str);
                    tvShow.setSelection(leftSelection.length());
//                    tvShow.setText(str);
//                    tvShow.setSelection(str.length());
                }
                isFormatting = false;
            }
        });



        gridView = (RelativeLayout) view.findViewById(R.id.formulaGridView);

        for (int i = 0; i < gridView.getChildCount(); i++) {
            if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2
                    && ((Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch3
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch4
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch5
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch6
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch7
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch8)
            ) {
                gridView.getChildAt(i).setEnabled(false);
            } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4
                    && ((Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch5
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch6
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch7
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch8)
            ) {
                gridView.getChildAt(i).setEnabled(false);
            }
            gridView.getChildAt(i).setOnClickListener(onItemClickListener);
            if ((Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_del) {
                gridView.getChildAt(i).setOnLongClickListener(onLongClickListener);
            }
        }
    }

    public void setData(String showStr, String timeBase, OnDismissListener onDismissListener) {
        isFormatting = true;
        if ("0".equals(showStr)) {
            showStr = "";
        }
        this.timeBase = timeBase;
        showList = KeyBoardFormulaUtil.getSelectionListFromShowText(showStr);
        tvShow.requestFocus();
        tvShow.setText(showStr);
        tvShow.setFilters(new InputFilter[]{new InputFilter.LengthFilter(COUNT_FORMULA)});
        tvShow.setSelection(Math.min(showStr.length(), tvShow.getText().length()));
        tvMsgError.setText("");
        this.onDismissListener = onDismissListener;
        if (StrUtil.isEmpty(showStr)) {
            setViewListVisible(INDEX_null);
        }
        isFormatting = false;
        show();
    }

    public void show() {
        tvShow.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
//        moveViewToPosition(showKeyBoardLayout, 0, ViewUtils.getDialogFormulaOffset());
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_FORMULAKEYBOARD);
        Tools.PrintControlsLocation("TopDialogFormulaKeyBoard",rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_FORMULAKEYBOARD);
    }

    /**
     * 表达式正确的前提下，去除表达式中无效的0
     */
    private String removeInvalid0(String showText) {
        ArrayList<String> listStr = new ArrayList<>();
        ArrayList<Integer> listIndex = new ArrayList<>();
        String s = "";
        for (int i = 0; i < showText.length(); i++) {
            if (StrUtil.isNumber(showText.charAt(i)) || showText.charAt(i) == '.') {
                s = s + showText.charAt(i);
            } else {
                if (!StrUtil.isEmpty(s)) {
                    listIndex.add(i - s.length());
                    listStr.add(s);
                    s = "";
                }
            }
        }
        if (!StrUtil.isEmpty(s)) {
            listIndex.add(showText.length() - s.length());
            listStr.add(s);
            s = "";
        }
        Logger.d(TAG, "removeInvalid0() listStr:" + listStr + ",listIndex:" + listIndex + ",s:" + s);
        if (listStr.size() == 0) {
            Logger.d(TAG, "removeInvalid0() called with: showText = [" + showText + "],return1 = [" + showText + "]");
            return showText;
        }
        String s2 = "";
        int subStart = 0;
        for (int i = 0; i < listStr.size(); i++) {
            int length = listStr.get(i).length();
            if (listStr.get(i).contains(".") || listStr.get(i).contains("E")) {
                String s1 = listStr.get(i);
                if (s1.contains(".") && !s1.contains("E")) {
                    while (s1.endsWith("0")) {
                        s1 = s1.substring(0, s1.length() - 1);
                    }
                    if (s1.endsWith(".")) {
                        s1 = s1.substring(0, s1.length() - 1);
                    }
                }
                while (s1.length() >= 2 && s1.charAt(0) == '0' && s1.charAt(1) != '.') {
                    s1 = s1.substring(1);
                }
                listStr.set(i, s1);
            } else {
                while (listStr.get(i).length() >= 2 && listStr.get(i).charAt(0) == '0' && listStr.get(i).charAt(1) != '.') {
                    listStr.set(i, listStr.get(i).substring(1));
                }
                listStr.set(i, listStr.get(i));
            }
            if (listStr.get(i).startsWith(".")) {
                listStr.set(i, "0" + listStr.get(i));
            }
            s2 = s2 + showText.substring(subStart, listIndex.get(i)) + listStr.get(i);
            subStart = listIndex.get(i) + length;
        Logger.d(TAG, "removeInvalid0() called with: listStr.get(i) = [" + listStr.get(i) + "],s2 = [" + s2 + "]");
        }
        s2 = s2 + showText.substring(subStart);
        Logger.d(TAG, "removeInvalid0() called with: showText = [" + showText + "],return2 = [" + s2 + "]");
        return s2;
    }

    /**
     * visibleList
     */
    private void setViewListVisible(int index) {
        Logger.d(TAG, "setViewListVisible() called with: index = [" + index + "]");
        boolean eAddSubAfterBracketLeft = true;
        boolean numberAfterPoint = true;
        boolean numberAfterE = true;
        boolean afterBracketRight = false;
        boolean zeroAfterZero = true;
        if (index == INDEX_add || index == INDEX_sub) {
            String[] keyList = KeyBoardFormulaUtil.getKeyList();
            String s = keyList[index];
            if (tvShow.getSelectionStart() >= s.length()) {
                String substring = tvShow.getText().toString().substring(0, tvShow.getSelectionStart() - s.length());
                Logger.d(TAG, "setViewListVisible() called with: substring = [" + substring + "]");
                int index2 = INDEX_null;
                if (!StrUtil.isEmpty(substring)) {
                    String s2 = "";
                    String[] searchList = KeyBoardFormulaUtil.getSearchList();
                    for (int i = 0; i < searchList.length; i++) {
                        if (substring.endsWith(searchList[i])) {
                            s2 = searchList[i];
                            break;
                        }
                    }
                    index2 = KeyBoardFormulaUtil.getIndex(s2);
                    Logger.d(TAG, "setViewListVisible() called with: s2 = [" + s2 + "],index2 = [" + index2 + "]");
                }
                if (index2 == INDEX_E) {
                    eAddSubAfterBracketLeft = false;
                }
            }
        } else if (KeyBoardFormulaUtil.isNumber(index)) {
            String subShowText = tvShow.getText().toString().substring(0, tvShow.getSelectionStart());
            ArrayList<String> listStr = new ArrayList<>();
            ArrayList<Integer> listIndex = new ArrayList<>();
            String s = "";
            for (int i = 0; i < subShowText.length(); i++) {
                if (StrUtil.isNumber(subShowText.charAt(i)) || subShowText.charAt(i) == '.' || subShowText.charAt(i) == 'E') {
                    s = s + subShowText.charAt(i);
                } else if ((subShowText.charAt(i) == '+' || subShowText.charAt(i) == '-') && (i >= 1 && subShowText.charAt(i - 1) == 'E')) {
                    s = s + subShowText.charAt(i);
                } else {
                    if (!StrUtil.isEmpty(s)) {
                        listIndex.add(i - s.length());
                        listStr.add(s);
                        s = "";
                    }
                }
            }
            if (!StrUtil.isEmpty(s)) {
                listIndex.add(subShowText.length() - s.length());
                listStr.add(s);
                s = "";
            }
            int selection = tvShow.getSelectionStart();
            Logger.d(TAG, "setViewListVisible() listStr:" + listStr + ",listIndex:" + listIndex + ",selection:" + selection);
            String E = context.getString(R.string.key_formula_E);
            for (int i = 0; i < listIndex.size(); i++) {
                if (listIndex.get(i) < selection && (listIndex.get(i) + listStr.get(i).length()) >= selection) {
                    if (listStr.get(i).contains(".") || listStr.get(i).contains(E)) {
                        numberAfterPoint = false;
                    }
                    if (listStr.get(i).contains(E)) {
                        numberAfterE = false;
                    }
                }
            }
            if (index == INDEX_0) {
                if (selection >= 2) {
                    String substring = tvShow.getText().toString().substring(0, selection);
                    String strNumber = "";
                    for (int i = substring.length() - 1; i >= 0; i--) {
                        if (StrUtil.isNumber(substring.charAt(i)) || substring.charAt(i) == '.') {
                            strNumber = substring.charAt(i) + strNumber;
                        } else {
                            break;
                        }
                    }
                    if (!StrUtil.isEmpty(strNumber) && !strNumber.contains(".")
                            && DoubleUtil.compareTo(Double.valueOf(strNumber), 0d) == 0) {
                        zeroAfterZero = false;
                    }
                } else {
                    zeroAfterZero = false;
                }
            }
        }
        if (index != INDEX_null) {
            String substring = tvShow.getText().toString().substring(0, tvShow.getSelectionStart());
            String strLeftBracket = context.getString(R.string.key_formula_bracket_left);
            String strRightBracket = context.getString(R.string.key_formula_bracket_right);
            int leftBracket = StrUtil.getCountFromString(substring, strLeftBracket);
            int rightBracket = StrUtil.getCountFromString(substring, strRightBracket);
            if (leftBracket > rightBracket) {
                afterBracketRight = true;
            }
        }
        boolean[] visibleList = KeyBoardFormulaUtil.getVisibleListFromCurSelection(index, eAddSubAfterBracketLeft
                , numberAfterPoint, numberAfterE, afterBracketRight, zeroAfterZero);
        for (int i = 0; i < gridView.getChildCount(); i++) {
            if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2
                    && ((Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch3
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch4
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch5
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch6
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch7
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch8)
            ) {
                visibleList[i] = false;
            } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4
                    && ((Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch5
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch6
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch7
                    || (Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ch8)
            ) {
                visibleList[i] = false;
            }
            gridView.getChildAt(i).setEnabled(visibleList[i]);
        }
        RxBus.getInstance().post(RxEnum.KEYBOARD_FORMULA_ENABLE, visibleList);
    }

    private OnClickListener onItemClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Screen.getViewLocation(v);
            PlaySound.getInstance().playButton();
            int index = Integer.parseInt((String) v.getTag());
            String text = ((Button) v).getText().toString();
            String showText = tvShow.getText().toString();
            if (index == INDEX_time_base) {//时基按键
                text = App.get().getResources().getString(R.string.key_formula_tb);
            }
            switch (index) {
                case INDEX_add:
                case INDEX_sub:
                case INDEX_mul:
                case INDEX_div:
                case INDEX_bracket_left:
                case INDEX_less:
                case INDEX_greater:
                case INDEX_less_equal:
                case INDEX_greater_equal:
                case INDEX_bracket_right:
                case INDEX_equal:
                case INDEX_not_equal:
                case INDEX_and:
                case INDEX_or:
                case INDEX_no:
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
                case INDEX_ch1:
                case INDEX_ch2:
                case INDEX_ch3:
                case INDEX_ch4:
                case INDEX_pi:
                case INDEX_ch5:
                case INDEX_ch6:
                case INDEX_ch7:
                case INDEX_ch8:
                case INDEX_time_base:
                case INDEX_E:
                case INDEX_var1:
                case INDEX_var2:
                case INDEX_7:
                case INDEX_8:
                case INDEX_9:
                case INDEX_m:
                case INDEX_T:
                case INDEX_4:
                case INDEX_5:
                case INDEX_6:
                case INDEX_u:
                case INDEX_G:
                case INDEX_1:
                case INDEX_2:
                case INDEX_3:
                case INDEX_n:
                case INDEX_M:
                case INDEX_point:
                case INDEX_0:
                case INDEX_f:
                case INDEX_p:
                case INDEX_k: {
                    if (showText.length() <= COUNT_FORMULA) {
                        isFormatting = true;
                        int selectionStart = tvShow.getSelectionStart();
                        String leftSelection = showText.substring(0, selectionStart);
                        String rightSelection = showText.substring(selectionStart);
                        String s = leftSelection + text + rightSelection;
                        showList = KeyBoardFormulaUtil.getSelectionListFromShowText(s);
                        tvShow.setText(s);
                        tvShow.setSelection((leftSelection + text).length());
                        isFormatting = false;
                        setViewListVisible(index);
                    }
                }
                break;
                case INDEX_enter: {
                    isFormatting = true;
                    if (StrUtil.isEmpty(showText)) {
//                        tvMsgError.setText(R.string.msgMathAdvanceFormulaNull);
//                        return;
                        showText = "0";
                    }
                    String strLeftBracket = context.getString(R.string.key_formula_bracket_left);
                    String strRightBracket = context.getString(R.string.key_formula_bracket_right);
                    int leftBracket = StrUtil.getCountFromString(showText, strLeftBracket);
                    int rightBracket = StrUtil.getCountFromString(showText, strRightBracket);
                    if (leftBracket > rightBracket) {
                        for (int i = 0; i < leftBracket - rightBracket; i++) {
                            showText += strRightBracket;
                        }
                        showList = KeyBoardFormulaUtil.getSelectionListFromShowText(showText);
                        tvShow.setText(showText);
                    }
                    Logger.i("showText:" + showText);
                    MathExprError mathExprError = MathExprWave.isExprValid(
                            KeyBoardFormulaUtil.amFormulaToScope(showText, MainHolderBottom.getCenterTimeBase()));
                    if (mathExprError.isSuccess()) {
                        if (showText.contains("0") || showText.contains(".")) {
                            showText = removeInvalid0(showText);
                        }
                        onDismissListener.onDismiss(showText);
                        hide();
                    } else {
                        int position = mathExprError.getPosition();
                        String value = mathExprError.getValue();
                        String curValue = showText.substring(position, position + value.length());
                        String msg = String.format(
                                getResources().getString(R.string.msgMathAdvanceFormulaError)
                                , String.valueOf(position + 1), curValue);
                        tvMsgError.setText(msg);
                        tvShow.requestFocus();
                        Logger.i("formulaMsgError:" + mathExprError.toString());
                    }
                    isFormatting = false;
                }
                break;
                case INDEX_del: {
                    isFormatting = true;
                    int selectionStart = tvShow.getSelectionStart();
                    if (selectionStart - 1 >= 0) {
                        String[] fanc = KeyBoardFormulaUtil.getSearchList();
                        String substring = showText.substring(0, selectionStart);
                        int count = 1;
                        for (int i = 0; i < fanc.length; i++) {
                            if (substring.endsWith(fanc[i])) {
                                count = fanc[i].length();
                                break;
                            }
                        }
                        String leftStr = showText.substring(0, selectionStart - count);
                        String rightStr = "";
                        if (selectionStart + 1 <= tvShow.length()) {
                            rightStr = showText.substring(selectionStart);
                        }
                        showList = KeyBoardFormulaUtil.getSelectionListFromShowText(leftStr + rightStr);
                        tvShow.setText(leftStr + rightStr);
                        tvShow.setSelection(leftStr.length());

                        String s = "";
                        for (int i = 0; i < fanc.length; i++) {
                            if (leftStr.endsWith(fanc[i])) {
                                s = fanc[i];
                                break;
                            }
                        }
                        setViewListVisible(KeyBoardFormulaUtil.getIndex(s));
                    }
                    isFormatting = false;
                }
                break;
            }
        }
    };

    private MSelectionEditText.OnSelectionChanged onSelectionChanged = new MSelectionEditText.OnSelectionChanged() {
        @Override
        public void onSelectionChanged(int selStart, int selEnd) {
            int selection = 0;
            int index = INDEX_null;
            for (int i = 0; i < showList.size(); i++) {
                index = i;
                if (selection >= selStart) {
                    if (tvShow.getSelectionStart() == selection) {
                        break;
                    }
                    tvShow.setSelectionFromUser(selection);
                    break;
                } else {
                    selection += showList.get(i).length();
                    if (i == showList.size() - 1) {
                        index++;
                        if (selection < selStart) {
                            selection = selStart;
                        }
                        tvShow.setSelectionFromUser(selection);
                        break;
                    }
                }
            }
            Logger.d(TAG, "onSelectionChanged() selStart=[" + selStart + "],selection=["
                    + selection + "],index=[" + index + "],showList:" + showList);
            index--;
            if (index < INDEX_null) {
                return;
            }
            if (index != INDEX_null) {
                int keyIndex = KeyBoardFormulaUtil.getIndex(showList.get(index));
                Logger.d("key:" + showList.get(index));
                setViewListVisible(keyIndex);
            } else {
                setViewListVisible(INDEX_null);
            }
        }
    };

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Screen.getViewLocation(v);
            PlaySound.getInstance().playButton();
            if (v.getId() == R.id.formulaSelectionLeft) {
                if (tvShow.getSelectionStart() - 1 >= 0) {
                    String substring = tvShow.getText().toString().substring(0, tvShow.getSelectionStart());
                    String[] fanc = KeyBoardFormulaUtil.getSearchList();
                    int count = 1;
                    for (int i = 0; i < fanc.length; i++) {
                        if (substring.endsWith(fanc[i])) {
                            count = fanc[i].length();
                            break;
                        }
                    }
                    tvShow.setSelection(tvShow.getSelectionStart() - count);
                }
            } else if (v.getId() == R.id.formulaSelectionRight) {
                if (tvShow.getSelectionStart() + 1 <= tvShow.getText().toString().length()) {
                    String substring = tvShow.getText().toString().substring(tvShow.getSelectionStart());
                    String[] fanc = KeyBoardFormulaUtil.getSearchList();
                    int count = 1;
                    for (int i = 0; i < fanc.length; i++) {
                        if (substring.startsWith(fanc[i])) {
                            count = fanc[i].length();
                            break;
                        }
                    }
                    tvShow.setSelection(tvShow.getSelectionStart() + count);
                }
            }
        }
    };

    private OnLongClickListener onLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (v.getId() == R.id.key_del) {
                showList.clear();
                tvShow.setText("");
                setViewListVisible(INDEX_null);
            } else if (v.getId() == R.id.formulaSelectionLeft) {
                tvShow.setSelection(0);
                setViewListVisible(INDEX_null);
            } else if (v.getId() == R.id.formulaSelectionRight) {
                tvShow.setSelection(tvShow.getText().toString().length());
                int index = KeyBoardFormulaUtil.getIndex(showList.get(showList.size() - 1));
                setViewListVisible(index);
            }
            return false;
        }
    };

    private void moveViewToPosition(View view, int x, int y) {
        AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new AbsoluteLayout.LayoutParams(
                    view.getWidth(),
                    view.getHeight(),
                    x,
                    y
            );
            view.setLayoutParams(layoutParams);
        } else {
            layoutParams.x = x;
            layoutParams.y = y;
            view.setLayoutParams(layoutParams);
        }
    }

    private String formatExpress(String express) {
        if (StrUtil.isEmpty(express)) {
            return "";
        }
        express = express
                .replace("*", "×")
                .replace("/", "÷")
                .replace("pi", "π")
                .replace("TB", "tb")
                .replace("Tb", "tb")
                .replace("tB", "tb")
                .replace("×u", "×μ")
                .replace("×U", "×μ")
//                .replace(context.getString(R.string.key_formula_tb), KeyBoardFormulaUtil.handleTimeBase(MainHolderBottom.getCenterTimeBase()))
        ;
        return express;
    }

}
