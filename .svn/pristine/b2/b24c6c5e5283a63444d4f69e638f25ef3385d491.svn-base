package com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
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
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.micsig.base.DoubleUtil;
import com.micsig.base.keyevent.KeyEventUtil;
import com.micsig.base.Logger;
import com.micsig.base.NumberUnitParser;
import com.micsig.base.filter.UnitInputFilter;
import com.micsig.tbook.scope.math.MathNative;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil;
import com.micsig.tbook.ui.util.StrUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by yangj on 2017/4/28.
 */
public class TopDialogFullFloatKeyBoard extends AbsoluteLayout implements IFullKeyBoardFloat {
    private static final String TAG = "TopDialogFullFloatKeyBoard";
    private Context context;
    private View fromView;
    private LinearLayout showLayout;
    private TextView tvSymbol;
    private EditText tvShow;
    private RelativeLayout gridView;
    private ArrayList<Button> list;
    private OnDismissListener onDismissListener;
    private boolean first = false;//是否是本次打开后第一点击
    private boolean isFromUser = false;
    public static final int INPUT_LENGTH_LIMIT = 15;


    private ViewGroup rootViewGroup;
    public interface OnDismissListener {
        void onDismiss(View fromView, String show);
    }

    public TopDialogFullFloatKeyBoard(Context context) {
        this(context, null);
    }

    public TopDialogFullFloatKeyBoard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopDialogFullFloatKeyBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    //[0, 240]	608	360
    private void init() {
        setClickable(true);
        rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_full_keyboard_float, this);

        rootViewGroup.findViewById(R.id.outView).setOnClickListener((v) -> {
            hide();
        });
        initView(rootViewGroup);
        hide();
    }

    private void initView(View view) {
        showLayout = (LinearLayout) view.findViewById(R.id.showKeyBoardLayout);
        tvSymbol = (TextView) view.findViewById(R.id.symbol);
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
                Logger.d(TAG, "event= " + event.getKeyCode());
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (first) {
                        first = false;
                        tvShow.setText("");
                        tvSymbol.setVisibility(INVISIBLE);
                    }
                    if (KeyEventUtil.isBackKey(event)) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_del));
                        }
                        return true;
                    }
                    if (KeyEventUtil.isNegative(event)) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_negative));
                        }
                        return true;
                    }
                    if (KeyEventUtil.isPositive(event)) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onClick((Button) gridView.getChildAt(INDEX_positive));
                        }
                        return true;
                    }
                    if (KeyEventUtil.isConfirmKey(event)) {
                        inputDone(getUnitVal());
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
                if(s.length() > 0) {
                    char lastChar = s.charAt(s.length() - 1);
                    char[] unit = {'f','p', 'n', 'u', 'm', 'k', 'M', 'G', 'T'};
                    double[] unitVal = {1e-15,1e-12, 1e-9, 1e-6, 1e-3, 1e3, 1e6, 1e9, 1e12};
                    for (int i = 0; i < unit.length; i++) {
                        if (unit[i] == lastChar) {
                            inputDone(unitVal[i]);
                            break;
                        }
                    }
                }
            }
        });

        gridView = (RelativeLayout) view.findViewById(R.id.gridView);

        list = new ArrayList<Button>();
        for (int i = 0; i < gridView.getChildCount(); i++) {
            gridView.getChildAt(i).setOnClickListener(onItemClickListener);
            if ((Integer.parseInt((String) gridView.getChildAt(i).getTag())) == INDEX_del) {
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

    private void inputDone(double v) {
        String text = tvShow.getText().toString();
        if (onDismissListener != null) {
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
                if (isSymbolVisible() && !"0".equals(text)) {
                    text = "-" + text;
                }
                NumberUnitParser numberUnitParser = new NumberUnitParser(text);
                if (((Switch) list.get(INDEX_f)).isChecked()
                        || ((Switch) list.get(INDEX_p)).isChecked()
                        || ((Switch) list.get(INDEX_n)).isChecked()
                        || ((Switch) list.get(INDEX_u)).isChecked()
                        || ((Switch) list.get(INDEX_m)).isChecked()
                        || ((Switch) list.get(INDEX_k)).isChecked()
                        || ((Switch) list.get(INDEX_M)).isChecked()
                        || ((Switch) list.get(INDEX_G)).isChecked()
                        || ((Switch) list.get(INDEX_T)).isChecked()
                ) {
                    onDismissListener.onDismiss(fromView, getValueFromD(numberUnitParser.getNumberAsDouble() * v));
                } else {
                    onDismissListener.onDismiss(fromView, text);
                }
            } else {
                onDismissListener.onDismiss(fromView, String.valueOf(0));
            }
        }
        hide();
    }


    private double getUnitVal() {
        double unitVal = 1;
        if (((Switch) list.get(INDEX_f)).isChecked()) {
            unitVal = 1e-15;
        }
        if (((Switch) list.get(INDEX_p)).isChecked()) {
            unitVal = 1e-12;
        }
        if (((Switch) list.get(INDEX_n)).isChecked()) {
            unitVal = 1e-9;
        }
        if (((Switch) list.get(INDEX_u)).isChecked()) {
            unitVal = 1e-6;
        }
        if (((Switch) list.get(INDEX_m)).isChecked()) {
            unitVal = 1e-3;
        }
        if (((Switch) list.get(INDEX_k)).isChecked()) {
            unitVal = 1e3;
        }
        if (((Switch) list.get(INDEX_M)).isChecked()) {
            unitVal = 1e6;
        }
        if (((Switch) list.get(INDEX_G)).isChecked()) {
            unitVal = 1e9;
        }
        if (((Switch) list.get(INDEX_T)).isChecked()) {
            unitVal = 1e12;
        }
        return unitVal;
    }

    public void show() {
        moveViewToPosition(showLayout, 0, context.getResources().getDimensionPixelOffset(R.dimen.rightDialogFloatKeyBoardRow0));
        setVisibility(VISIBLE);
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_FLOATKEYBOARD);
        Tools.PrintControlsLocation("TopDialogFullFloatKeyBoard",rootViewGroup);
    }

    public void hide() {
        setVisibility(GONE);
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_FLOATKEYBOARD);
    }



    //改变光标选中的值 注意边界 以及 小数点
    public void changeSelection(int temp, boolean isRight) {
        int selection = getSelection() + temp;
        selection = Math.min(selection, tvShow.getText().length());
        selection = Math.max(selection, 1);
        if (".".equals(tvShow.getText().toString().substring(selection - 1, selection))) {
            if (isRight) {
                selection++;
            } else {
                selection--;
            }
        }
        tvShow.setSelection(selection, selection - 1);
    }

    public int getSelection() {
        int selectStart = tvShow.getSelectionStart();
        int selectEnd = tvShow.getSelectionEnd();
        if (selectStart != selectEnd) {
            return Math.max(selectStart, selectEnd);
        } else {
            if (selectStart == 0) {
                return 1;
            } else {
                return Math.max(selectStart, selectEnd);
            }
        }
    }

    //计算变化之后的值，并处理新的选中位
    public void changeNowUnitNumber(int unitTest, boolean isRight) {
        String oldText = tvShow.getText().toString();
        int oldLength = oldText.length();
        int pointIndex = oldText.indexOf(".") + 1;
        int selectIndex = getSelection();
        boolean isReduce = (isSymbolVisible() && isRight) || (!isSymbolVisible() && !isRight);
        if(selectIndex== 1 && oldLength > 1 && oldText.charAt(0) == '1' && isReduce) return;//当选中最高位为1时不能减少
        StringBuilder unitNumber = new StringBuilder("");
        for (int i = 1; i <= oldLength; i++) {
            unitNumber.append(i == pointIndex ? "." : (i == selectIndex ? "1" : "0"));
        }
        BigDecimal unit = new BigDecimal(unitNumber.toString());
        BigDecimal oldBig = new BigDecimal(oldText);
        if (isSymbolVisible()) {
            oldBig = oldBig.multiply(new BigDecimal("-1"));
        }
        String finalBigStr = isRight ? oldBig.add(unit).toPlainString() : oldBig.subtract(unit).toPlainString();
        if (finalBigStr.contains("-")) {
            finalBigStr = finalBigStr.replace("-", "");
            tvSymbol.setVisibility(VISIBLE);
        } else {
            tvSymbol.setVisibility(INVISIBLE);
        }
        if (finalBigStr.length() > INPUT_LENGTH_LIMIT) {
            finalBigStr = finalBigStr.substring(0, INPUT_LENGTH_LIMIT);
        }
        tvShow.setText(finalBigStr);
        int newLength = tvShow.getText().toString().length();
        if (newLength == oldLength) { //位数没变
            tvShow.setSelection(selectIndex, selectIndex - 1);
        } else if (newLength == oldLength + 1) { //有进位
            tvShow.setSelection(selectIndex + 1, selectIndex);
        } else if (newLength == oldLength - 1) { //最高位借位
            if (selectIndex - 2 < 0) {//处理类似 100 -> 0 的变化情况
                tvShow.setSelection(1, 0);
            } else {
                tvShow.setSelection(selectIndex - 1, selectIndex - 2);
            }
        } else {
            tvShow.setSelection(1, 0);
        }
        clickEnter();
    }


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

    private boolean isSymbolVisible() {
        return tvSymbol.getVisibility() == View.VISIBLE;
    }


    public void setFloatData_Extent(String number, View fromView, OnDismissListener onDismissListener) {
        this.fromView = fromView;
        if (number.contains("-")) {
            number = number.replace("-", "");
            tvSymbol.setVisibility(VISIBLE);
        } else {
            tvSymbol.setVisibility(INVISIBLE);
        }

        tvShow.setFilters(new InputFilter[]{new UnitInputFilter("fpnumkMGT"), new InputFilter.LengthFilter(INPUT_LENGTH_LIMIT)});

        (list.get(INDEX_positive)).setEnabled(false);
        (list.get(INDEX_negative)).setEnabled(false);

        ((Switch) list.get(INDEX_f)).setEnabled(true);
        ((Switch) list.get(INDEX_p)).setEnabled(true);
        ((Switch) list.get(INDEX_n)).setEnabled(true);
        ((Switch) list.get(INDEX_u)).setEnabled(true);
        ((Switch) list.get(INDEX_m)).setEnabled(true);
        ((Switch) list.get(INDEX_k)).setEnabled(true);
        ((Switch) list.get(INDEX_M)).setEnabled(true);
        ((Switch) list.get(INDEX_G)).setEnabled(true);
        ((Switch) list.get(INDEX_T)).setEnabled(true);


        ((Switch) list.get(INDEX_f)).setChecked(false);
        ((Switch) list.get(INDEX_p)).setChecked(false);
        ((Switch) list.get(INDEX_n)).setChecked(false);
        ((Switch) list.get(INDEX_u)).setChecked(false);
        ((Switch) list.get(INDEX_m)).setChecked(false);
        ((Switch) list.get(INDEX_k)).setChecked(false);
        ((Switch) list.get(INDEX_M)).setChecked(false);
        ((Switch) list.get(INDEX_G)).setChecked(false);
        ((Switch) list.get(INDEX_T)).setChecked(false);

        if (number.endsWith(list.get(INDEX_f).getText().toString())) {
            ((Switch) list.get(INDEX_f)).setChecked(true);
            number = number.replace(list.get(INDEX_f).getText().toString(), "");
        } else if (number.endsWith(list.get(INDEX_p).getText().toString())) {
            ((Switch) list.get(INDEX_p)).setChecked(true);
            number = number.replace(list.get(INDEX_p).getText().toString(), "");
        } else if (number.endsWith(list.get(INDEX_n).getText().toString())) {
            ((Switch) list.get(INDEX_n)).setChecked(true);
            number = number.replace(list.get(INDEX_n).getText().toString(), "");
        } else if (number.endsWith(list.get(INDEX_u).getText().toString())) {
            ((Switch) list.get(INDEX_u)).setChecked(true);
            number = number.replace(list.get(INDEX_u).getText().toString(), "");
        } else if (number.endsWith(list.get(INDEX_m).getText().toString())) {
            ((Switch) list.get(INDEX_m)).setChecked(true);
            number = number.replace(list.get(INDEX_m).getText().toString(), "");
        } else if (number.endsWith(list.get(INDEX_k).getText().toString())) {
            ((Switch) list.get(INDEX_k)).setChecked(true);
            number = number.replace(list.get(INDEX_k).getText().toString(), "");
        } else if (number.endsWith(list.get(INDEX_M).getText().toString())) {
            ((Switch) list.get(INDEX_M)).setChecked(true);
            number = number.replace(list.get(INDEX_M).getText().toString(), "");
        } else if (number.endsWith(list.get(INDEX_G).getText().toString())) {
            ((Switch) list.get(INDEX_G)).setChecked(true);
            number = number.replace(list.get(INDEX_G).getText().toString(), "");
        } else if (number.endsWith(list.get(INDEX_T).getText().toString())) {
            ((Switch) list.get(INDEX_T)).setChecked(true);
            number = number.replace(list.get(INDEX_T).getText().toString(), "");
        }

        first = true;
        tvShow.requestFocus();
        tvShow.setText(number);
        tvShow.setSelection(Math.min(number.length(), tvShow.getText().length()));
        gridView.requestFocus();
        this.onDismissListener = onDismissListener;
        show();
    }
    /**
     * 设置数据为Float时的参数
     *
     * @param number            数据
     * @param onDismissListener 关闭时的回调
     */
    public void setFloatData(String number, View fromView, OnDismissListener onDismissListener) {
        setFloatData(number, true, fromView, onDismissListener);
    }

    /**
     * 设置数据为Float时的参数
     *
     * @param number            数据
     * @param onDismissListener 关闭时的回调
     */
    public void setFloatData(String number, boolean isUnit, View fromView, OnDismissListener onDismissListener) {
        this.fromView = fromView;
        if (number.contains("-")) {
            number = number.replace("-", "");
            tvSymbol.setVisibility(VISIBLE);
        } else {
            tvSymbol.setVisibility(INVISIBLE);
        }

        tvShow.setFilters(new InputFilter[]{new UnitInputFilter("fpnumkMGT"), new InputFilter.LengthFilter(INPUT_LENGTH_LIMIT)});


        if (isUnit) {
            (list.get(INDEX_positive)).setEnabled(true);
            (list.get(INDEX_negative)).setEnabled(true);

            ((Switch) list.get(INDEX_f)).setEnabled(true);
            ((Switch) list.get(INDEX_p)).setEnabled(true);
            ((Switch) list.get(INDEX_n)).setEnabled(true);
            ((Switch) list.get(INDEX_u)).setEnabled(true);
            ((Switch) list.get(INDEX_m)).setEnabled(true);
            ((Switch) list.get(INDEX_k)).setEnabled(true);
            ((Switch) list.get(INDEX_M)).setEnabled(true);
            ((Switch) list.get(INDEX_G)).setEnabled(true);
            ((Switch) list.get(INDEX_T)).setEnabled(true);


            ((Switch) list.get(INDEX_f)).setChecked(false);
            ((Switch) list.get(INDEX_p)).setChecked(false);
            ((Switch) list.get(INDEX_n)).setChecked(false);
            ((Switch) list.get(INDEX_u)).setChecked(false);
            ((Switch) list.get(INDEX_m)).setChecked(false);
            ((Switch) list.get(INDEX_k)).setChecked(false);
            ((Switch) list.get(INDEX_M)).setChecked(false);
            ((Switch) list.get(INDEX_G)).setChecked(false);
            ((Switch) list.get(INDEX_T)).setChecked(false);

            if (number.endsWith(list.get(INDEX_f).getText().toString())) {
                ((Switch) list.get(INDEX_f)).setChecked(true);
                number = number.replace(list.get(INDEX_f).getText().toString(), "");
            } else if (number.endsWith(list.get(INDEX_p).getText().toString())) {
                ((Switch) list.get(INDEX_p)).setChecked(true);
                number = number.replace(list.get(INDEX_p).getText().toString(), "");
            } else if (number.endsWith(list.get(INDEX_n).getText().toString())) {
                ((Switch) list.get(INDEX_n)).setChecked(true);
                number = number.replace(list.get(INDEX_n).getText().toString(), "");
            } else if (number.endsWith(list.get(INDEX_u).getText().toString())) {
                ((Switch) list.get(INDEX_u)).setChecked(true);
                number = number.replace(list.get(INDEX_u).getText().toString(), "");
            } else if (number.endsWith(list.get(INDEX_m).getText().toString())) {
                ((Switch) list.get(INDEX_m)).setChecked(true);
                number = number.replace(list.get(INDEX_m).getText().toString(), "");
            } else if (number.endsWith(list.get(INDEX_k).getText().toString())) {
                ((Switch) list.get(INDEX_k)).setChecked(true);
                number = number.replace(list.get(INDEX_k).getText().toString(), "");
            } else if (number.endsWith(list.get(INDEX_M).getText().toString())) {
                ((Switch) list.get(INDEX_M)).setChecked(true);
                number = number.replace(list.get(INDEX_M).getText().toString(), "");
            } else if (number.endsWith(list.get(INDEX_G).getText().toString())) {
                ((Switch) list.get(INDEX_G)).setChecked(true);
                number = number.replace(list.get(INDEX_G).getText().toString(), "");
            } else if (number.endsWith(list.get(INDEX_T).getText().toString())) {
                ((Switch) list.get(INDEX_T)).setChecked(true);
                number = number.replace(list.get(INDEX_T).getText().toString(), "");
            }
            number =number.replace(" ","");
        } else {
            ((Switch) list.get(INDEX_f)).setChecked(false);
            ((Switch) list.get(INDEX_p)).setChecked(false);
            ((Switch) list.get(INDEX_n)).setChecked(false);
            ((Switch) list.get(INDEX_u)).setChecked(false);
            ((Switch) list.get(INDEX_m)).setChecked(false);
            ((Switch) list.get(INDEX_k)).setChecked(false);
            ((Switch) list.get(INDEX_M)).setChecked(false);
            ((Switch) list.get(INDEX_G)).setChecked(false);
            ((Switch) list.get(INDEX_T)).setChecked(false);
        }
        first = true;
        tvShow.requestFocus();
        tvShow.setText(number);
        tvShow.setSelection(Math.min(number.length(), tvShow.getText().length()));
        gridView.requestFocus();
        this.onDismissListener = onDismissListener;
        show();
    }

    //添加操作
    private void addText(String add) {
        int index = tvShow.getSelectionStart();
        Editable editable = tvShow.getText();
//        直接复制粘贴的问题，需要继承edittext，重写下面这个方法
//        tvShow.onTextContextMenuItem()
        editable.insert(index, add);
        int interval = KeyBoardNumberUtil.getInterval(IDigits.DIGITS_10);
        tvShow.setText(KeyBoardNumberUtil.reCalculateSpace(tvShow.getText().toString(), IDigits.DIGITS_10));
        if ((index + 2) % (interval + 1) == 0 || (index + 1) % (interval + 1) == 0) {
            tvShow.setSelection(index + 2);
        } else {
            tvShow.setSelection(index + 1);
        }
    }

    //删除操作
    private void delText() {
        int index = tvShow.getSelectionStart();
        Log.d("Command", "delText index: "+index);
        if (index > 0) {
            Editable editable = tvShow.getText();
            if (index >= 2 && ' ' == editable.charAt(index - 1)) {
                editable.delete(index - 2, index);
            } else {
                editable.delete(index - 1, index);
            }
            tvShow.setText(KeyBoardNumberUtil.reCalculateSpace(tvShow.getText().toString(), IDigits.DIGITS_10));
            if (index >= 2 && index % (KeyBoardNumberUtil.getInterval(IDigits.DIGITS_10) + 1) == 0) {
                tvShow.setSelection(index - 2);
            } else {
                tvShow.setSelection(index - 1);
            }
        }
    }

    public boolean isNumber(int index) {
        return index != INDEX_enter && index != INDEX_del && index != INDEX_negative
                && index != INDEX_positive && index != INDEX_point
                && index != INDEX_f && index != INDEX_p && index != INDEX_n
                && index != INDEX_u && index != INDEX_m && index != INDEX_k
                && index != INDEX_M && index != INDEX_G && index != INDEX_T;
    }

    public boolean isEnter(int index) {
        return index == INDEX_enter;
    }

    public boolean isPoint(int index) {
        return index == INDEX_point;
    }

    public boolean isDelete(int index) {
        return index == INDEX_del;
    }

    public boolean isSymbol(int index) {
        return index == INDEX_positive || index == INDEX_negative;
    }

    public boolean isUnit(int index) {
        return index == INDEX_f || index == INDEX_p || index == INDEX_n
                || index == INDEX_u || index == INDEX_m || index == INDEX_k
                || index == INDEX_M || index == INDEX_G || index == INDEX_T;
    }

    public int hasUnit() {
        for (int i = 0; i < list.size(); i++) {
            if (isUnit(i)) {
                if (((Switch) list.get(i)).isEnabled() && ((Switch) list.get(i)).isChecked()) {
                    return i;
                }
            }
        }
        return -1;
    }

    private double getValue(String text, int unit) {
        double dText = Double.valueOf(text);
        double dUnit = 1.0d;
        switch (unit) {
            case INDEX_f:
                dUnit = 0.001 * 0.001 * 0.001 * 0.001 * 0.001;
                break;
            case INDEX_p:
                dUnit = 0.001 * 0.001 * 0.001 * 0.001;
                break;
            case INDEX_n:
                dUnit = 0.001 * 0.001 * 0.001;
                break;
            case INDEX_u:
                dUnit = 0.001 * 0.001;
                break;
            case INDEX_m:
                dUnit = 0.001;
                break;
            case INDEX_k:
                dUnit = 1000;
                break;
            case INDEX_M:
                dUnit = 1000_000d;
                break;
            case INDEX_G:
                dUnit = 1000_000_000d;
                break;
            case INDEX_T:
                dUnit = 1000_000_000_000d;
                break;
            default:
                dUnit = 1;
                break;
        }
        return DoubleUtil.mul(dText, dUnit);
    }

    private String getValueFromD(double d) {
        if (DoubleUtil.compareTo(d, 0d) < 0) {
            return "-" + getValueFromD(DoubleUtil.mul(d, -1d));
        } else if (DoubleUtil.compareTo(d, 0d) == 0) {
            return "0 ";
        } else if (DoubleUtil.compareTo(d, 1000_000_000_000d) >= 0) {
            d = DoubleUtil.mul(d, 1e-12);
            return get5Bit(d) + " T";
        } else if (DoubleUtil.compareTo(d, 1000_000_000d) >= 0) {
            d = DoubleUtil.mul(d, 1e-9);
            return get5Bit(d) + " G";
        } else if (DoubleUtil.compareTo(d, 1000_000d) >= 0) {
            d = DoubleUtil.mul(d, 1e-6);
            return get5Bit(d) + " M";
        } else if (DoubleUtil.compareTo(d, 1000d) >= 0) {
            d = DoubleUtil.mul(d, 0.001);
            return get5Bit(d) + " k";
        } else if (DoubleUtil.compareTo(d, 1d) >= 0) {
            return get5Bit(d)+" ";
        } else if (DoubleUtil.compareTo(d, 0.001) >= 0) {
            d = DoubleUtil.mul(d, 1000d);
            return get5Bit(d) + " m";
        } else if (DoubleUtil.compareTo(d, 0.001 * 0.001) >= 0) {
            d = DoubleUtil.mul(d, 1000d * 1000d);
            return get5Bit(d) + " μ";
        } else if (DoubleUtil.compareTo(d, 0.001 * 0.001 * 0.001) >= 0) {
            d = DoubleUtil.mul(d, 1000d * 1000d * 1000d);
            String s = get5Bit(d);
            if (s.contains(".")) {
                String[] split = s.split("\\.");
                if (split[1].length() > 3) {
                    split[1] = split[1].substring(0, 3);
                    s = split[0] + "." + split[1];
                }
            }
            return s + " n";
        } else if (DoubleUtil.compareTo(d, 0.001 * 0.001 * 0.001 * 0.001) >= 0) {
            d = DoubleUtil.mul(d, 1000d * 1000d * 1000d * 1000d);
            return get5Bit(((int) d)) + " p";
        }  else if (DoubleUtil.compareTo(d, 0.001 * 0.001 * 0.001 * 0.001 * 0.001) >= 0) {
            d = DoubleUtil.mul(d, 1000d * 1000d * 1000d * 1000d * 1000d);
            return get5Bit(((int) d)) + " f";
        }

        else {
            return "1 p";
        }
    }

    private String get5Bit(double d) {
        String s = String.valueOf(d);
        if (s.length() >= 6) {
            s = s.substring(0, 6);
        }
        if (s.contains(".")) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.replace(".", "");
            }
        }
        return s;
    }

    private OnClickListener onItemClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            PlaySound.getInstance().playButton();
            int index = Integer.parseInt((String) v.getTag());
            String str = ((Button) v).getText().toString();

            if (isNumber(index)) {
                if (first) {
                    first = false;
                    tvSymbol.setVisibility(INVISIBLE);
                    tvShow.setText("");
                }
                if (tvShow.getText().toString().equals("0")) {
                    delText();
                }
                if (tvShow.getText().toString().length() >= INPUT_LENGTH_LIMIT) {
                    return;
                }
                addText(str);
            } else if (isDelete(index)) {
                if (StrUtil.isEmpty(tvShow.getText().toString())) {
                    tvSymbol.setVisibility(INVISIBLE);
                } else {
                    delText();
                }
            } else if (isSymbol(index)) {
                if (first) {
                    first = false;
                }
                if (index == INDEX_negative) {
                    tvSymbol.setVisibility(VISIBLE);
                } else if (index == INDEX_positive) {
                    tvSymbol.setVisibility(INVISIBLE);
                }
            } else if (isUnit(index)) {
                if (!((Switch) list.get(index)).isChecked()) {
                    ((Switch) list.get(index)).setChecked(false);
                } else {
                    for (int i = 0; i < list.size(); i++) {
                        if (isUnit(i)) {
                            if (index == i) {
                                ((Switch) list.get(i)).setChecked(true);
                            } else {
                                ((Switch) list.get(i)).setChecked(false);
                            }
                        }
                    }
                }
            } else if (isPoint(index)) {
                if (first) {
                    first = false;
                    tvShow.setText("0");
                    tvShow.setSelection(tvShow.getSelectionStart() + 1);
                    tvSymbol.setVisibility(INVISIBLE);
                }
                if (tvShow.getText().toString().isEmpty()) {
                    tvShow.setText("0");
                    tvShow.setSelection(tvShow.getSelectionStart() + 1);
                    tvSymbol.setVisibility(INVISIBLE);
                }
                if (!tvShow.getText().toString().contains(str)) {
                    addText(str);
                }
            } else if (isEnter(index)) {
                if (onDismissListener != null) {
                    clickEnter();
                }
                hide();
            }
        }
    };

    private void clickEnter() {
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
            if (isSymbolVisible() && !"0".equals(text)) {
                text = "-" + text;
            }
            if (((Switch)list.get(INDEX_f)).isChecked()
                    || ((Switch)list.get(INDEX_p)).isChecked()
                    || ((Switch)list.get(INDEX_n)).isChecked()
                    || ((Switch)list.get(INDEX_u)).isChecked()
                    || ((Switch)list.get(INDEX_m)).isChecked()
                    || ((Switch)list.get(INDEX_k)).isChecked()
                    || ((Switch)list.get(INDEX_M)).isChecked()
                    || ((Switch)list.get(INDEX_G)).isChecked()
                    || ((Switch)list.get(INDEX_T)).isChecked()
            ) {
                int hasUnit = hasUnit();
                onDismissListener.onDismiss(fromView, getValueFromD(getValue(text, hasUnit)));
            } else {
                onDismissListener.onDismiss(fromView, getValueFromD(getValue(text, 1)));
            }
        } else {
            onDismissListener.onDismiss(fromView, String.valueOf(0));
        }
    }

    private OnLongClickListener onLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (!StrUtil.isEmpty(tvShow.getText().toString())) {
                PlaySound.getInstance().playButton();
                tvShow.setText("0");
                tvShow.setSelection(tvShow.getSelectionStart() + 1);
                tvSymbol.setVisibility(INVISIBLE);
            }
            return false;
        }
    };

    private static String formatDouble(double d) {
        NumberFormat nf = NumberFormat.getInstance();
        //设置保留多少位小数
        nf.setMaximumFractionDigits(20);
        // 取消科学计数法
        nf.setGroupingUsed(false);
        //返回结果
        return nf.format(d);
    }
}
