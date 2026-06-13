package com.micsig.tbook.tbookscope.top.popwindow.keyboardtext;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.micsig.base.keyevent.KeyEventUtil;
import com.micsig.base.Logger;
import com.micsig.base.filter.FilterFactory;
import com.micsig.base.filter.UnitInputFilter;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.ui.util.StrUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yangj on 2017/11/30.
 */

public class TopDialogTextKeyBoard extends LinearLayout implements KeyBoardTextItem.SpecialKey {
    private static final String TAG = "TopDialogTextKeyBoard";
    public static final int INPUT_TYPE_ALL = 21;//全部可点击
    public static final int INPUT_TYPE_LETTER = 22;//只有字母可点击
    public static final int INPUT_TYPE_NUMBER_INT = 23;//只有数字可点击
    public static final int INPUT_TYPE_NUMBER_DOUBLE = 24;//只有数字和小数点可点击
    public static final int INPUT_TYPE_ALL_BUT_SYMBOL = 25;//除了不可切换到符号页面之外都可以
    public static final int INPUT_TYPE_ALL_POINT_LINE = 26;//字母数字下划线小数点可点击

    public static final int HANDLE_TYPE_CHANNEL_LABEL = 1;//标签长度
    public static final int HANDLE_TYPE_SAVE_SESSION = 2;//一键存储中 文件名长度
    public static final int HANDLE_TYPE_MATH = 3; //高级数学/axb 中单位
    public int currentHandleType = 0;

    private Context context;
    private EditText tvShow;
    private KeyBoardTextAdapter adapter;
    private TopDialogCandidatesWord layoutCandidatesWord;
    private boolean isEnglish = false;//强制只能输入英文

    private int inputType = INPUT_TYPE_ALL;
    private int inputLength = -1;
    private int inputDBig = 1;//输入类型为double时，最大值的整数部分位数
    private int inputDSmall = 1;//输入类型为double时，最大值的小数部分位数

    private List<KeyBoardTextItem> curList = new ArrayList<>();
    private List<KeyBoardTextItem> letterLowerList = new ArrayList<>();
    private List<KeyBoardTextItem> letterUpperList = new ArrayList<>();
    private List<KeyBoardTextItem> numberList = new ArrayList<>();
    private List<KeyBoardTextItem> symbolList = new ArrayList<>();
    private OnDialogDismissListener dismissListener;

    private ViewGroup rootViewGroup;

    public interface OnDialogDismissListener {
        void onDismiss(String result);
    }

    public TopDialogTextKeyBoard(Context context) {
        this(context, null);
    }

    public TopDialogTextKeyBoard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopDialogTextKeyBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setClickable(true);
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_keyboard_text, this);

        View outView = rootViewGroup.findViewById(R.id.outView);
        outView.setOnClickListener((v)->{
            hide();
            layoutCandidatesWord.hide();
        });
        tvShow = (EditText) rootViewGroup.findViewById(R.id.show);
        tvShow.setShowSoftInputOnFocus(false);
        tvShow.setLongClickable(false);
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
                Logger.d(TAG, "event= " + event.getKeyCode());
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    boolean needHandle = currentHandleType == HANDLE_TYPE_CHANNEL_LABEL || currentHandleType == HANDLE_TYPE_MATH;
                    if (KeyEventUtil.ignoreKeyForLabel(event) && needHandle) {
                        return true;
                    }
                    if (KeyEventUtil.isConfirmKey(event)) {
                        inputDone();
                        return true;
                    }
                }
                return false;
            }
        });


        tvShow.addTextChangedListener(new TextWatcher() {
            final String cnSymbols ="～｀！＠＃￥％…＆＊（）＿＋—－＝【】［］｛｝｜、：；“‘《》？，。／";
            final String enSymbols =" ~`!@#$%^&*()_-+=[]\\;',./<>?:\"{}|";
            private  String lastText="";
            private void SendDelEventInputMethod(){
                post(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm=(InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if(imm!=null){
                            tvShow.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DEL));
                            tvShow.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DEL));
                        }
                    }
                });
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s3) {
                String currentText= s3.toString();
                int currentPos=currentText.length();
                if(currentText.length()>lastText.length()){
                    String newChar=currentText.substring(currentPos-1);
                    //何种情况均不允许中文符号出现
                    if(cnSymbols.contains(newChar)){
                        SendDelEventInputMethod();
                    }
                    if (inputType == INPUT_TYPE_ALL_BUT_SYMBOL && enSymbols.contains(newChar)){
                        SendDelEventInputMethod();
                    }
                }
                if (inputType == INPUT_TYPE_NUMBER_DOUBLE) {
                    String s = s3.toString();
                    if (s.contains(".")) {
                        String[] split = s.split("\\.");
                        if (split[0].length() > inputDBig || (split.length == 2 && split[1].length() > inputDSmall)) {
                            tvShow.getText().delete(tvShow.getSelectionStart() - 1, tvShow.getSelectionStart());
                        }
                    } else {
                        if (s.length() > inputDBig) {
                            tvShow.getText().delete(tvShow.getSelectionStart() - 1, tvShow.getSelectionStart());
                        }
                    }
                }
            }
        });

        RecyclerView recyclerView = (RecyclerView) rootViewGroup.findViewById(R.id.gridView);
        recyclerView.requestFocus();
        GridLayoutManager layoutManager = new GridLayoutManager(context, 22) {
            @Override
            public boolean canScrollVertically() {
                return false;//禁止垂直滚动
            }
        };
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getSpanSize(position);
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        curList.clear();
        isEnglish = true;
        initKeyBoardData(false);
        curList.addAll(letterLowerList);
        adapter = new KeyBoardTextAdapter(context, curList);
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
        hide();

        layoutCandidatesWord = (TopDialogCandidatesWord) rootViewGroup.findViewById(R.id.candidatesWordView);
    }

    public TopDialogCandidatesWord getLayoutCandidatesWord() {
        return layoutCandidatesWord;
    }

    public void show() {
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_TEXTKEYBOARD);
        Tools.PrintControlsLocation("TopDialogTextKeyBoard",rootViewGroup);
    }

    public void hide() {
        tvShow.setInputType(InputType.TYPE_CLASS_TEXT);
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_TEXTKEYBOARD);
    }
    public boolean isCandidatesWordShow(){
        return layoutCandidatesWord.isShowing();
    }
    private void setData(String text, int inputType, OnDialogDismissListener dismissListener) {
        this.inputType = inputType;
        this.dismissListener = dismissListener;
        tvShow.requestFocus();
        tvShow.setFilters(new InputFilter[]{new InputFilter.LengthFilter(inputLength)});
        tvShow.setText(text);
        tvShow.setSelection(Math.min(text.length(), tvShow.getText().length()));
        switch (this.inputType) {
            case INPUT_TYPE_ALL:
            case INPUT_TYPE_ALL_BUT_SYMBOL:
            case INPUT_TYPE_LETTER:
            case INPUT_TYPE_ALL_POINT_LINE:
                adapter.setUpper(false);
                curList.clear();
                curList.addAll(letterLowerList);
                adapter.notifyDataSetChanged();
                break;
            case INPUT_TYPE_NUMBER_INT:
                adapter.setUpper(false);
                curList.clear();
                curList.addAll(numberList);
                adapter.notifyDataSetChanged();
                tvShow.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case INPUT_TYPE_NUMBER_DOUBLE:
                adapter.setUpper(false);
                curList.clear();
                curList.addAll(numberList);
                adapter.notifyDataSetChanged();
                break;
        }
        show();
        layoutCandidatesWord.hide();
    }

    public void setData(String text, OnDialogDismissListener dismissListener) {
        this.inputLength = -1;
        initKeyBoardData(false);
        setData(text, INPUT_TYPE_ALL, dismissListener);
    }

    public void setData(String text, int inputType, int inputLength, OnDialogDismissListener dismissListener) {
        this.inputLength = inputLength;
        initKeyBoardData(false);
        setData(text, inputType, dismissListener);
    }

    public void setData(String text, int handleType, int inputType, int inputLength, OnDialogDismissListener dismissListener) {
        this.inputLength = inputLength;
        this.currentHandleType = handleType;
        initKeyBoardData(false);
        setData(text, inputType, dismissListener);
    }

    public void setDataEnglish(int handleType, String text, int inputLength, OnDialogDismissListener dismissListener) {
        this.currentHandleType = handleType;
        this.inputLength = inputLength;
        inputType = INPUT_TYPE_ALL;
        initKeyBoardData(true);
        setData(text, inputType, dismissListener);
    }

    public void setDataDouble(double text, int maxValueBig, int maxValueSmall, OnDialogDismissListener dismissListener) {
        inputDBig = maxValueBig;
        inputDSmall = maxValueSmall;
        inputLength = inputDBig + inputDSmall + 1;
        initKeyBoardData(false);
        setData(String.valueOf(text), INPUT_TYPE_NUMBER_DOUBLE, dismissListener);
        tvShow.setFilters(new InputFilter[]{new UnitInputFilter(""), new InputFilter.LengthFilter(inputLength)});
        tvShow.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    private void initKeyBoardData(boolean isEnglish) {
        if (this.isEnglish == isEnglish) {
            return;
        }
        this.isEnglish = isEnglish;
        letterLowerList.clear();
        letterUpperList.clear();
        numberList.clear();
        symbolList.clear();
        boolean isLangCn = StrUtil.isLangCn();
        String enter = isLangCn ? ENTER_CN : ENTER;
        String hide = isLangCn ? HIDE_CN : HIDE;
        String lang = isLangCn && !isEnglish ? LANG_CN : LANG_ENG;
        String[][] letterLowers = {
                {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p", DELETE},
                {PLACEHOLDER, "a", "s", "d", "f", "g", "h", "j", "k", "l", enter},
                {UPPER, "z", "x", "c", "v", "b", "n", "m", ",", ".", "-"},
                {hide, NUMBER, SPACE, SYMBOL, lang}
        };
        String[][] letterUppers = {
                {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", DELETE},
                {PLACEHOLDER, "A", "S", "D", "F", "G", "H", "J", "K", "L", enter},
                {UPPER, "Z", "X", "C", "V", "B", "N", "M", ",", ".", "-"},
                {hide, NUMBER, SPACE, SYMBOL, lang}
        };
        String[][] numbers = {
                {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", DELETE},
                {PLACEHOLDER, "a", "s", "d", "f", "g", "h", "j", "k", "l", enter},
                {UPPER, "z", "x", "c", "v", "b", "n", "m", ",", ".", "-"},
                {hide, NUMBER, SPACE, SYMBOL, lang}
        };
        String[][] symbols = {
                {"[", "]", "{", "}", "#", "%", "^", "*", "+", "=", DELETE},
                {PLACEHOLDER, "_", "\\", "|", "~", "<", ">", "?", "!", "'", enter},
                {UPPER, "·", "/", ":", ";", "(", ")", "$", "&", "@", "`"},
                {hide, NUMBER, SPACE, SYMBOL, lang}
        };
        for (int i = 0; i < letterLowers[0].length; i++) {
            letterLowerList.add(new KeyBoardTextItem(letterLowerList.size(), letterLowers[0][i], 2));
            letterUpperList.add(new KeyBoardTextItem(letterUpperList.size(), letterUppers[0][i], 2));
            numberList.add(new KeyBoardTextItem(numberList.size(), numbers[0][i], 2));
            symbolList.add(new KeyBoardTextItem(symbolList.size(), symbols[0][i], 2));
        }
        for (int i = 0; i < letterLowers[1].length; i++) {
            int size;
            if (letterLowers[1][i].equals(PLACEHOLDER)) {
                size = 1;
            } else if (letterLowers[1][i].equals(enter)) {
                size = 3;
            } else {
                size = 2;
            }

            letterLowerList.add(new KeyBoardTextItem(letterLowerList.size(), letterLowers[1][i], size));
            letterUpperList.add(new KeyBoardTextItem(letterUpperList.size(), letterUppers[1][i], size));
            numberList.add(new KeyBoardTextItem(numberList.size(), numbers[1][i], size));
            symbolList.add(new KeyBoardTextItem(symbolList.size(), symbols[1][i], size));
        }
        for (int i = 0; i < letterLowers[2].length; i++) {
            letterLowerList.add(new KeyBoardTextItem(letterLowerList.size(), letterLowers[2][i], 2));
            letterUpperList.add(new KeyBoardTextItem(letterUpperList.size(), letterUppers[2][i], 2));
            numberList.add(new KeyBoardTextItem(numberList.size(), numbers[2][i], 2));
            symbolList.add(new KeyBoardTextItem(symbolList.size(), symbols[2][i], 2));
        }
        for (int i = 0; i < letterLowers[3].length; i++) {
            int size;
            if (letterLowers[3][i].equals(SPACE)) {
                size = 10;
            } else {
                size = 3;
            }
            letterLowerList.add(new KeyBoardTextItem(letterLowerList.size(), letterLowers[3][i], size));
            letterUpperList.add(new KeyBoardTextItem(letterUpperList.size(), letterUppers[3][i], size));
            numberList.add(new KeyBoardTextItem(numberList.size(), numbers[3][i], size));
            symbolList.add(new KeyBoardTextItem(symbolList.size(), symbols[3][i], size));
        }
        Logger.i("letterLowerList:" + Arrays.toString(letterLowerList.toArray()));
    }

    private KeyBoardTextAdapter.OnItemClickListener onItemClickListener = new KeyBoardTextAdapter.OnItemClickListener() {
        @Override
        public void onQuickDelete() {
            PlaySound.getInstance().playButton();
            if (layoutCandidatesWord.isShowing() && !StrUtil.isEmpty(layoutCandidatesWord.getPinyin())) {
                layoutCandidatesWord.setData("", onDialogClickWordListener);
            } else if (tvShow.getSelectionStart() != 0) {
                tvShow.setText("");
            }
        }

        @Override
        public void onDelete() {
            PlaySound.getInstance().playButton();
            if (layoutCandidatesWord.isShowing() && !StrUtil.isEmpty(layoutCandidatesWord.getPinyin())) {
                String pinyin = layoutCandidatesWord.getPinyin();
                layoutCandidatesWord.setData(pinyin.substring(0, pinyin.length() - 1), onDialogClickWordListener);
            } else if (tvShow.getSelectionStart() != 0) {
                tvShow.getText().delete(tvShow.getSelectionStart() - 1, tvShow.getSelectionStart());
            }
        }

        @Override
        public void onEnter() {
            PlaySound.getInstance().playButton();
            String pinyin = layoutCandidatesWord.getPinyin();
            if(!StrUtil.isEmpty(pinyin)){
                //如果在打中文，未选择时点enter，直接打出英文
                tvShow.getText().insert(tvShow.getSelectionStart(), pinyin);
                layoutCandidatesWord.hide();
                return;
            }
            String result = tvShow.getText().toString();
            Logger.i("tvShow=" + tvShow.getText().toString() + " pinyin=" + pinyin);

            if (currentHandleType == HANDLE_TYPE_SAVE_SESSION) {
                if (result.isEmpty()) {
                    DToast.get().show(R.string.file_name_not_null);
                    return;
                }
            }

            if (dismissListener != null) {
                dismissListener.onDismiss(result);
            }
            hide();
            layoutCandidatesWord.hide();
        }

        @Override
        public void onUpper(boolean isUpper) {
            PlaySound.getInstance().playButton();
            if (isUpper) {
                curList.clear();
                curList.addAll(letterUpperList);
                adapter.notifyDataSetChanged();
            } else {
                curList.clear();
                curList.addAll(letterLowerList);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onHide() {
            PlaySound.getInstance().playButton();
            hide();
            layoutCandidatesWord.hide();
        }

        @Override
        public void onNumber() {
            PlaySound.getInstance().playButton();
            adapter.setUpper(false);
            if (curList.contains(numberList.get(0))) {
                curList.clear();
                curList.addAll(letterLowerList);
                adapter.notifyDataSetChanged();
            } else {
                curList.clear();
                curList.addAll(numberList);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onSymbol() {
            PlaySound.getInstance().playButton();
            if (inputType == INPUT_TYPE_ALL_BUT_SYMBOL
                    || inputType == INPUT_TYPE_NUMBER_INT
                    || inputType == INPUT_TYPE_NUMBER_DOUBLE
            ) return;
            adapter.setUpper(false);
            if (curList.contains(symbolList.get(0))) {
                curList.clear();
                curList.addAll(letterLowerList);
                adapter.notifyDataSetChanged();
            } else {
                boolean isEng = curList.get(INDEX_LANG).getWord().equals(LANG_ENG);
                curList.clear();
                curList.addAll(symbolList);
                if (isEng) {
                    curList.get(INDEX_LANG).setWord(LANG_ENG);
                    curList.get(INDEX_ENTER).setWord(ENTER);
                    curList.get(INDEX_HIDE).setWord(HIDE);
                } else {
                    curList.get(INDEX_LANG).setWord(LANG_CN);
                    curList.get(INDEX_ENTER).setWord(ENTER_CN);
                    curList.get(INDEX_HIDE).setWord(HIDE_CN);
                }
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onEnglish(boolean isEng) {
            PlaySound.getInstance().playButton();
            if (!StrUtil.isLangCn() || isEnglish) {
                return;
            }
            layoutCandidatesWord.setData("", onDialogClickWordListener);
            if (isEng) {
                curList.get(INDEX_LANG).setWord(LANG_CN);
                curList.get(INDEX_ENTER).setWord(ENTER_CN);
                curList.get(INDEX_HIDE).setWord(HIDE_CN);
            } else {
                curList.get(INDEX_LANG).setWord(LANG_ENG);
                curList.get(INDEX_ENTER).setWord(ENTER);
                curList.get(INDEX_HIDE).setWord(HIDE);
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onOtherKey(String keyWord) {
            PlaySound.getInstance().playButton();
            if (currentHandleType == HANDLE_TYPE_CHANNEL_LABEL
                    || currentHandleType == HANDLE_TYPE_SAVE_SESSION) {
                if (tvShow.getText().toString().getBytes().length >= inputLength) {
                    DToast.get().show(R.string.keyBoardMsgBeyond);
                    return;
                }
            }
            if (inputType != INPUT_TYPE_NUMBER_DOUBLE) {
                if (inputLength != -1 && tvShow.getText().toString().length() >= inputLength) {
                    DToast.get().show(R.string.keyBoardMsgBeyond);
                    return;
                }
            }
            switch (inputType) {
                case INPUT_TYPE_ALL_POINT_LINE:
                    if (!StrUtil.isNumberLetter(keyWord)) {
                        if (!".".equals(keyWord) && !"_".equals(keyWord)) {
                            break;
                        }
                    }
                case INPUT_TYPE_ALL_BUT_SYMBOL:
                    String curLang = curList.get(INDEX_LANG).getWord();
                    if (curLang.equals(LANG_CN) && StrUtil.isLetter(keyWord)) {
                        layoutCandidatesWord.setData(layoutCandidatesWord.getPinyin() + keyWord,
                                onDialogClickWordListener);
                    } else {
                        tvShow.getText().insert(tvShow.getSelectionStart(), keyWord);
                    }
                    break;
                case INPUT_TYPE_ALL:
                    boolean needHandle = currentHandleType == HANDLE_TYPE_CHANNEL_LABEL || currentHandleType == HANDLE_TYPE_MATH;
                    if (ignoreSymbols(keyWord) && needHandle) break;
                    String curLangAll = curList.get(INDEX_LANG).getWord();
                    if (curLangAll.equals(LANG_CN) && StrUtil.isLetter(keyWord)) {
                        layoutCandidatesWord.setData(layoutCandidatesWord.getPinyin() + keyWord,
                                onDialogClickWordListener);
                    } else {
                        tvShow.getText().insert(tvShow.getSelectionStart(), keyWord);
                    }
                    break;
                case INPUT_TYPE_LETTER:
                    if (StrUtil.isLetter(keyWord)) {
                        tvShow.getText().insert(tvShow.getSelectionStart(), keyWord);
                    }
                    break;
                case INPUT_TYPE_NUMBER_INT:
                    if (StrUtil.isNumber(keyWord)) {
                        tvShow.getText().insert(tvShow.getSelectionStart(), keyWord);
                    }
                    break;
                case INPUT_TYPE_NUMBER_DOUBLE:

                    if ((".".equals(keyWord) && !StrUtil.isEmpty(tvShow.getText().toString()) && !tvShow.getText().toString().contains("."))
                            || StrUtil.isNumber(keyWord)) {
                        tvShow.getText().insert(tvShow.getSelectionStart(), keyWord);

                        String s = tvShow.getText().toString();
                        if (s.contains(".")) {
                            String[] split = s.split("\\.");
                            if (split[0].length() > inputDBig || (split.length == 2 && split[1].length() > inputDSmall)) {
                                tvShow.getText().delete(tvShow.getSelectionStart() - 1, tvShow.getSelectionStart());
                            }
                        } else {
                            if (s.length() > inputDBig) {
                                tvShow.getText().delete(tvShow.getSelectionStart() - 1, tvShow.getSelectionStart());
                            }
                        }
                    }
                    break;
            }
        }
    };

    private boolean ignoreSymbols(String keyWord) {//限制输入的符号
        return ",".equals(keyWord) || ":".equals(keyWord) || ";".equals(keyWord);
    }

    private TopDialogCandidatesWord.OnDialogClickWordListener onDialogClickWordListener = new TopDialogCandidatesWord.OnDialogClickWordListener() {
        @Override
        public boolean onClickWord(String s) {

            if (currentHandleType == HANDLE_TYPE_CHANNEL_LABEL
                    || currentHandleType == HANDLE_TYPE_SAVE_SESSION
            ) {
                String newInput = tvShow.getText().toString().trim() + s.trim();
                if (inputLength != -1 && newInput.getBytes().length >= inputLength) {
                    DToast.get().show(R.string.keyBoardMsgBeyond);
                    return false;
                }
            }

            if (inputLength != -1 && (tvShow.getText().toString().length() + s.length()) > inputLength) {
                DToast.get().show(R.string.keyBoardMsgBeyond);
                return false;
            }
            tvShow.getText().insert(tvShow.getSelectionStart(), s);
            return true;
        }
    };

    /**
     * 获取EditText光标所在的位置
     */
    private int getEditTextCursorIndex() {
        return tvShow.getSelectionStart();
    }

    /**
     * 向EditText指定光标位置插入字符串
     */
    private void insertText(String mText) {
        tvShow.getText().insert(getEditTextCursorIndex(), mText);
    }

    /**
     * 向EditText指定光标位置删除字符串
     */
    private void deleteText() {
        if (!TextUtils.isEmpty(tvShow.getText().toString())) {
            tvShow.getText().delete(tvShow.getSelectionStart() - 1, tvShow.getSelectionStart());
        }
    }
    public void showTitle(){
        TextView createTile = findViewById(R.id.createFolderTitle);
        createTile.setGravity(Gravity.CENTER_HORIZONTAL);
        createTile.setVisibility(VISIBLE);
    }

    private void inputDone() {
        String text = tvShow.getText().toString();
        if (dismissListener != null) {
            if (!StrUtil.isEmpty(text)) {
                dismissListener.onDismiss(text);
            } else {
                dismissListener.onDismiss("");
            }
        }
        hide();
    }

}
