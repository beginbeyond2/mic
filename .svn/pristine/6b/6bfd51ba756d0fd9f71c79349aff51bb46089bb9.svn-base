package com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber;

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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.micsig.base.keyevent.KeyEventUtil;
import com.micsig.base.Logger;
import com.micsig.base.filter.FilterFactory;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;

import java.util.ArrayList;

/**
 * Created by yangj on 2017/4/28.
 */
public class TopDialogNumberKeyBoard extends AbsoluteLayout implements IDigits, IKeyBoardNumber {
    private static final String TAG = "TopDialogNumberKeyBoard";
    public static final String KEYBOARD_BS = "b/s";
    public static final String KEYBOARD_KBS = "kb/s";
    public static final String KEYBOARD_MBS = "Mb/s";
    private final int BITS_FOR_BAUDRATE = 20;//波特率长度限制

    private Context context;
    private LinearLayout showLayout;
    private EditText tvShow;
    private RelativeLayout gridView;
    private ArrayList<Button> list;
    private OnDismissListener onDismissListener;
    private OnOriginalDismissListener onOriginalDismissListener;
    private int digits;
    private int bits = -1;//输入的限制位数，进制模式下使用，-1为不限制
    private String bs;
    private int minBaudRate, maxBaudRate;//输入的限制大小，波特率模式使用
    private double minFloat, maxFloat;//输入的限制大小，float模式使用
    private boolean first = false;//是否是本次打开后第一点击
    private boolean changeLetter = false;//ABCDEF六个字母是否需要变成十进制显示和使用
    private boolean isFormatting = false;

    private ViewGroup rootViewGroup;

    public interface OnDismissListener {
        void onDismiss(String result);
    }

    public interface OnOriginalDismissListener {//保留相对原始的输入数据，只补齐偶数位的0。
        void onOriginalDismiss(String result, String originalResult);
    }

    public TopDialogNumberKeyBoard(Context context) {
        this(context, null);
    }

    public TopDialogNumberKeyBoard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopDialogNumberKeyBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    //[0, 240]	608	360
    private void init() {
        setClickable(true);
        rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_keyboard_number, this);

        rootViewGroup.findViewById(R.id.outView).setOnClickListener((v) -> {
            hide();
            RxBus.getInstance().post(RxEnum.MSG_HIDE_INPUT_BACKGROUND, false);
        });
        initView(rootViewGroup);
        hide();
    }

    private void initView(View view) {
        showLayout = (LinearLayout) view.findViewById(R.id.showKeyBoardLayout);
        tvShow = (EditText) view.findViewById(R.id.show);
        tvShow.setShowSoftInputOnFocus(false);
        tvShow.setLongClickable(false);
        tvShow.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
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
                    if (first) {
                        first = false;
                        tvShow.setText("");
                    }
                    if (KeyEventUtil.isConfirmKey(event)) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_ENTER));
                        }
                        return true;
                    }
                    if (KeyEventUtil.isBackKey(event)) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_ZUO));
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        tvShow.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                int interval = KeyBoardNumberUtil.getInterval(digits);
                int num = bits / interval;
                int remain = bits % interval;
                int finalBits = num * (interval + 1) + remain;
                if (digits == IDigits.DIGITS_10) {
                    finalBits = bits;
                }
                String str = s.toString();
                if (str.length() > finalBits && finalBits > 0) {
                    str = str.substring(0, finalBits);
                }
                String finalText = KeyBoardNumberUtil.reCalculateSpace(str, digits);

                if (!s.toString().equals(finalText)) {
                    s = Editable.Factory.getInstance().newEditable(finalText);
                    int selection = finalText.length();
                    if (selection > finalBits && finalBits > 0) {
                        selection = finalBits;
                    }
                    tvShow.setText(finalText);
                    tvShow.setSelection(selection);
                }
                isFormatting = false;

            }
        });

        gridView = (RelativeLayout) view.findViewById(R.id.gridView);

        list = new ArrayList<Button>();
        for (int i = 0; i < gridView.getChildCount(); i++) {
            gridView.getChildAt(i).setOnClickListener(onItemClickListener);
            if ((Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_ZUO) {
                gridView.getChildAt(i).setOnLongClickListener(onLongClickListener);
            }
            for (int j = 0; j < gridView.getChildCount(); j++) {
                if ((Integer.parseInt((String) gridView.getChildAt(j).getTag())) == i) {
                    list.add((Button) gridView.getChildAt(i));
                    break;
                }
            }
        }
    }

    public void show() {
//        moveViewToPosition(showLayout, 0, ViewUtils.getDialogNumberOffset());
        setVisibility(VISIBLE);
        tvShow.setSelection(tvShow.getText().length());
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_NUMBERKEYBOARD);
        Tools.PrintControlsLocation("TopDialogNumberKeyBoard",rootViewGroup);
    }

    public void hide() {
        tvShow.setText("");
        tvShow.setSelection(0);
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_NUMBERKEYBOARD);
    }

    public void setDecimalData(int bits, int digits, OnDismissListener onDismissListener) {
        setDecimalData(null, bits, digits, onDismissListener);
    }

    public void setDecimalData(int bits, int digits, boolean changeLetter, OnDismissListener onDismissListener) {
        setDecimalData(null, bits, digits, changeLetter, false, onDismissListener);
    }

    public void setDecimalData(String preText, int bits, int digits, OnDismissListener onDismissListener) {
        setDecimalData(preText, bits, digits, false, false, onDismissListener);
    }

    public void setDecimalData(String preText, int bits, int digits, boolean isRight, OnDismissListener onDismissListener) {
        setDecimalData(preText, bits, digits, false, isRight, onDismissListener);
    }


    public void setOriginalDecimalData(int bits, int digits, OnOriginalDismissListener onOriginalDismissListener) {
        setOriginalDecimalData(null, bits, digits, onOriginalDismissListener);
    }

    public void setOriginalDecimalData(String preText, int bits, int digits, OnOriginalDismissListener onOriginalDismissListener) {
        setOriginalDecimalData(preText, bits, digits, false, false, onOriginalDismissListener);
    }

    /**
     * 设置数据为进制数据类时的参数
     */
    private void setDecimalData(String preText, int bits, int digits, boolean changeLetter, boolean isRight, OnDismissListener onDismissListener) {
        first = true;
        this.bits = bits;
        this.digits = digits;
        this.changeLetter = changeLetter;
        tvShow.requestFocus();
        if (preText != null) {
            preText = KeyBoardNumberUtil.reCalculateSpace(preText, digits);
        }
        tvShow.setText(preText);
        setFilter(digits, bits);
        if (preText != null) {
            tvShow.setSelection(Math.min(preText.length(), tvShow.getText().length()));
        }
        gridView.requestFocus();
        list.get(INDEX_BS).setEnabled(false);
        list.get(INDEX_KBS).setEnabled(false);
        list.get(INDEX_MBS).setEnabled(false);
        ((RadioButton) list.get(INDEX_BS)).setChecked(false);
        ((RadioButton) list.get(INDEX_KBS)).setChecked(false);
        ((RadioButton) list.get(INDEX_MBS)).setChecked(false);
        this.onOriginalDismissListener = null;
        this.onDismissListener = onDismissListener;
        setLayoutPosition(isRight);
        setChangeLetter();
        setDigits();
        show();
    }

    /**
     * 设置数据为进制数据类时的参数
     * 保留原始的输入数据
     */
    private void setOriginalDecimalData(String preText, int bits, int digits, boolean changeLetter, boolean isRight, OnOriginalDismissListener onOriginalDismissListener) {
        this.bits = bits;
        first = true;
        this.digits = digits;
        this.changeLetter = changeLetter;
        tvShow.requestFocus();
        if (preText != null) {
            preText = KeyBoardNumberUtil.reCalculateSpace(preText, digits);
        }
        tvShow.setText(preText);
        setFilter(digits, bits);
        if (preText != null) {
            tvShow.setSelection(Math.min(preText.length(), tvShow.getText().length()));
        }
        gridView.requestFocus();
        list.get(INDEX_BS).setEnabled(false);
        list.get(INDEX_KBS).setEnabled(false);
        list.get(INDEX_MBS).setEnabled(false);
        ((RadioButton) list.get(INDEX_BS)).setChecked(false);
        ((RadioButton) list.get(INDEX_KBS)).setChecked(false);
        ((RadioButton) list.get(INDEX_MBS)).setChecked(false);
        this.onDismissListener = null;
        this.onOriginalDismissListener = onOriginalDismissListener;
        setLayoutPosition(isRight);
        setChangeLetter();
        setDigits();
        show();
    }

    /**
     * 设置数据为Float时的参数
     *
     * @param number            数据
     * @param minFloat          数据的最小值，小数点后有几位就是保留几位小数
     * @param maxFloat          数据的最大值
     * @param onDismissListener 关闭时的回调
     */
    public void setFloatData(double number, double minFloat, double maxFloat, OnDismissListener onDismissListener) {
        this.bs = "";
        this.minFloat = minFloat;
        this.maxFloat = maxFloat;
        this.bits = BITS_FOR_BAUDRATE;
        this.digits = DIGITS_FLOAT;
        first = true;
        changeLetter = false;
        list.get(INDEX_MBS).setEnabled(false);
        list.get(INDEX_BS).setEnabled(false);
        list.get(INDEX_KBS).setEnabled(false);
        ((RadioButton) list.get(INDEX_BS)).setChecked(false);
        ((RadioButton) list.get(INDEX_KBS)).setChecked(false);
        ((RadioButton) list.get(INDEX_MBS)).setChecked(false);
        tvShow.requestFocus();
        tvShow.setText(String.valueOf(number));
        setFilter(digits, bits);
        tvShow.setSelection(Math.min(String.valueOf(number).length(), tvShow.getText().length()));
        gridView.requestFocus();
        this.onDismissListener = onDismissListener;
        setLayoutPosition(true);
        setChangeLetter();
        setDigits();
        show();
    }

    /**
     * 设置数据为b/s类时的参数
     *
     * @param number            数据
     * @param bs                单位
     * @param minBs             数据的最小值，单位b/s
     * @param maxBs             数据的最大值，单位b/s
     * @param onDismissListener 关闭时的回调
     */
    public void setBaudRateData(double number, String bs, int minBs, int maxBs, OnDismissListener onDismissListener) {
        this.minBaudRate = minBs;
        this.maxBaudRate = maxBs;
        this.bits = BITS_FOR_BAUDRATE;
        this.digits = DIGITS_BAUDRATE;
        first = true;
        changeLetter = false;
        ((RadioButton) list.get(INDEX_BS)).setEnabled(true);
        ((RadioButton) list.get(INDEX_KBS)).setEnabled(true);
        ((RadioButton) list.get(INDEX_MBS)).setEnabled(true);
        if (KEYBOARD_BS.equals(bs)) {
            ((RadioButton) list.get(INDEX_BS)).setChecked(true);
            ((RadioButton) list.get(INDEX_KBS)).setChecked(false);
            ((RadioButton) list.get(INDEX_MBS)).setChecked(false);
            this.bs = KEYBOARD_BS;
        } else if (KEYBOARD_KBS.equals(bs)) {
            ((RadioButton) list.get(INDEX_KBS)).setChecked(true);
            ((RadioButton) list.get(INDEX_BS)).setChecked(false);
            ((RadioButton) list.get(INDEX_MBS)).setChecked(false);
            this.bs = KEYBOARD_KBS;
        } else if (KEYBOARD_MBS.equals(bs)) {
            ((RadioButton) list.get(INDEX_MBS)).setChecked(true);
            ((RadioButton) list.get(INDEX_BS)).setChecked(false);
            ((RadioButton) list.get(INDEX_KBS)).setChecked(false);
            this.bs = KEYBOARD_MBS;
        } else {
            return;
        }
        tvShow.requestFocus();
        tvShow.setText(String.valueOf(number));
        setFilter(digits, bits);
        tvShow.setSelection(Math.min(String.valueOf(number).length(), tvShow.getText().length()));
        gridView.requestFocus();
        this.onDismissListener = onDismissListener;
        setLayoutPosition(true);
        setChangeLetter();
        setDigits();
        show();
    }

    private void setChangeLetter() {
        list.get(INDEX_9).setText(changeLetter ? "12" : "9");
        list.get(INDEX_A).setText(changeLetter ? "16" : "A");
        list.get(INDEX_B).setText(changeLetter ? "20" : "B");
        list.get(INDEX_C).setText(changeLetter ? "24" : "C");
        list.get(INDEX_D).setText(changeLetter ? "32" : "D");
        list.get(INDEX_E).setText(changeLetter ? "48" : "E");
        list.get(INDEX_F).setText(changeLetter ? "64" : "F");
    }

    private void setDigits() {
        for (int i = 0; i < list.size(); i++) {
            Button item = list.get(i);
            item.setEnabled(KeyBoardNumberUtil.isEnabled(digits, i));
        }
    }

    private void setLayoutPosition(boolean isRight) {
        AbsoluteLayout.LayoutParams layoutParams = (LayoutParams) showLayout.getLayoutParams();
        if (isRight) {
//            layoutParams.y = 472;
        } else {
//            layoutParams.y = 472;
        }
        showLayout.setLayoutParams(layoutParams);
    }

    private boolean isOverBits() {
        String s = tvShow.getText().toString().replace(" ", "");
        if (digits == DIGITS_2 || digits == DIGITS_2X) {
            return bits != -1 && s.length() >= bits;
        } else if (digits == DIGITS_16 || digits == DIGITS_16X) {
            return bits != -1 && s.length() >= bits;
        } else {
            return bits != -1 && s.length() >= bits;
        }
    }

    //添加操作
    private void addText(String add) {
        int index = tvShow.getSelectionStart();
        Editable editable = tvShow.getText();
//        直接复制粘贴的问题，需要继承edittext，重写下面这个方法
//        tvShow.onTextContextMenuItem()
        editable.insert(index, add);
        int interval = KeyBoardNumberUtil.getInterval(digits);
        tvShow.setText(KeyBoardNumberUtil.reCalculateSpace(tvShow.getText().toString(), digits));
        if ((index + 2) % (interval + 1) == 0 || (index + 1) % (interval + 1) == 0) {
            tvShow.setSelection(index + 2);
        } else {
            tvShow.setSelection(index + 1);
        }
    }

    //删除操作
    private void delText() {
        int index = tvShow.getSelectionStart();
        if (index > 0) {
            Editable editable = tvShow.getText();
            if (index >= 2 && ' ' == editable.charAt(index - 1)) {
                editable.delete(index - 2, index);
            } else {
                editable.delete(index - 1, index);
            }
            tvShow.setText(KeyBoardNumberUtil.reCalculateSpace(tvShow.getText().toString(), digits));
            if (index >= 2 && index % (KeyBoardNumberUtil.getInterval(digits) + 1) == 0) {
                tvShow.setSelection(index - 2);
            } else {
                tvShow.setSelection(index - 1);
            }
        }
    }

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int index = Integer.parseInt((String) v.getTag());
            String str = ((Button) v).getText().toString();
            PlaySound.getInstance().playButton();
            if (KeyBoardNumberUtil.isNumber(index)) {
                if (first) {
                    first = false;
                    tvShow.setText("");
                }
                if (!isOverBits()) {
                    addText(str);
                    if (changeLetter) {
                        tvShow.setSelection(tvShow.getText().length());
                    }
                }
            } else if (KeyBoardNumberUtil.isDelete(index)) {
                delText();
            } else if (KeyBoardNumberUtil.isBs(index)) {
                bs = KEYBOARD_BS;
                for (int i = 0; i < list.size(); i++) {
                    if (i == INDEX_BS) {
                        ((RadioButton) list.get(i)).setChecked(true);
                    } else if (i == INDEX_KBS || i == INDEX_MBS) {
                        ((RadioButton) list.get(i)).setChecked(false);
                    }
                }
            } else if (KeyBoardNumberUtil.isKbs(index)) {
                bs = KEYBOARD_KBS;
                for (int i = 0; i < list.size(); i++) {
                    if (i == INDEX_KBS) {
                        ((RadioButton) list.get(i)).setChecked(true);
                    } else if (i == INDEX_BS || i == INDEX_MBS) {
                        ((RadioButton) list.get(i)).setChecked(false);
                    }
                }
            } else if (KeyBoardNumberUtil.isMbs(index)) {
                bs = KEYBOARD_MBS;
                for (int i = 0; i < list.size(); i++) {
                    if (i == INDEX_MBS) {
                        ((RadioButton) list.get(i)).setChecked(true);
                    } else if (i == INDEX_KBS || i == INDEX_BS) {
                        ((RadioButton) list.get(i)).setChecked(false);
                    }
                }
            } else if (KeyBoardNumberUtil.isPoint(index)) {
                if (first) {
                    first = false;
                    tvShow.setText("");
                }
                if (isOverBits()) return;
                if (!tvShow.getText().toString().contains(str)) {
                    addText(str);
                }
            } else if (KeyBoardNumberUtil.isEnter(index)) {
                if (digits == DIGITS_FLOAT) {
                    String text = tvShow.getText().toString();
                    String result = "";
                    if (!StrUtil.isEmpty(text) && !"0".equals(text) && !"0.0".equals(text)) {
                        if (text.startsWith(".")) {
                            text = "0" + text;
                        }
                        if (text.endsWith(".")) {
                            text = text.replace(".", "");
                            if (StrUtil.isEmpty(text)) {
                                text = "0";
                            }
                        }
                        double d = Double.parseDouble(text);
                        d = Math.max(d, minFloat);
                        d = Math.min(d, maxFloat);
                        int length = String.valueOf(minFloat).split("\\.")[1].length();
                        String[] results = String.valueOf(d).split("\\.");
                        if (results[1].length() > length) {
                            results[1] = results[1].substring(0, length);
                        }
                        result = results[0] + "." + results[1];
                    } else {
                        result = String.valueOf(minFloat);
                    }
                    if (onDismissListener != null) {
                        onDismissListener.onDismiss(result);
                    }
                } else if (digits == DIGITS_BAUDRATE) {
                    String text = tvShow.getText().toString();
                    if (!StrUtil.isEmpty(text)) {
                        if (text.startsWith(".")) {
                            text = "0" + text;
                        }
                        if (text.endsWith(".")) {
                            text = text.replace(".", "");
                            if (StrUtil.isEmpty(text)) {
                                text = "0";
                            }
                        }
                        int baudRate = TBookUtil.getIntFromBaudRate(text + bs);
                        baudRate = Math.max(baudRate, minBaudRate);
                        baudRate = Math.min(baudRate, maxBaudRate);
                        if (onDismissListener != null) {
                            onDismissListener.onDismiss(TBookUtil.getBaudRateFromInt(baudRate));
                        }
                    } else {
                        if (onDismissListener != null) {
                            onDismissListener.onDismiss(TBookUtil.getBaudRateFromInt(minBaudRate));
                        }
                    }
                } else {
                    if (changeLetter) {
                        if (onDismissListener != null) {
                            onDismissListener.onDismiss(KeyBoardNumberUtil.reCalculateSpace(
                                    tvShow.getText().toString().replace(" ", ""), digits).trim());
                        }
                    } else {
                        if (onDismissListener != null) {
                            onDismissListener.onDismiss(
                                    KeyBoardNumberUtil.reCalculateSpace(
                                            KeyBoardNumberUtil.toBits(tvShow.getText().toString().replace(" ", ""), bits), digits).trim()
                            );
                        }
                        if (onOriginalDismissListener != null) {
                            onOriginalDismissListener.onOriginalDismiss(
                                    KeyBoardNumberUtil.reCalculateSpace(
                                            KeyBoardNumberUtil.toBits(tvShow.getText().toString().replace(" ", ""), bits), digits).trim(),
                                    KeyBoardNumberUtil.reCalculateSpace(
                                            KeyBoardNumberUtil.toEvenNumberLength(tvShow.getText().toString().replace(" ", "")), digits).trim()
                            );
                        }
                    }
                }
                hide();
            }
        }
    };

    private OnLongClickListener onLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (!StrUtil.isEmpty(tvShow.getText().toString())) {
                PlaySound.getInstance().playButton();
                tvShow.setText("");
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


    private void setFilter(int digits, int bits) {
        String regex = "";
        switch (digits) {
            case IDigits.DIGITS_2:
                regex = FilterFactory.BINARY_REGEX;
                break;
            case IDigits.DIGITS_2X:
                regex = FilterFactory.BINARY_X_REGEX;
                break;
            case IDigits.DIGITS_8:
                regex = FilterFactory.OCTAL_REGEX;
                break;
            case IDigits.DIGITS_10:
                regex = FilterFactory.DECIMAL_REGEX;
                break;
            case IDigits.DIGITS_16:
                regex = FilterFactory.HEX_REGEX;
                break;
            case IDigits.DIGITS_16X:
                regex = FilterFactory.HEX_X_REGEX;
                break;
            case IDigits.DIGITS_BAUDRATE:
            case IDigits.DIGITS_FLOAT:
                regex = FilterFactory.BAUDRATE_REGEX;
                break;
        }

        int interval = KeyBoardNumberUtil.getInterval(digits);
        int num = bits / interval;
        int remain = bits % interval;
        int finalBits = num * (interval + 1) + remain;
        if (digits == IDigits.DIGITS_10) {
            finalBits = bits;
        }
        //bits有效位数
        Logger.d(TAG, "bits=" + bits + " ,interval=" + interval + " ,finalBits=" + finalBits + " ,digits=" + digits);

        if (bits != -1) {
            tvShow.setFilters(new InputFilter[]{FilterFactory.getFilter(regex), new InputFilter.LengthFilter(finalBits + 1)});
        } else {
            tvShow.setFilters(new InputFilter[]{FilterFactory.getFilter(regex)});
        }

    }
}
