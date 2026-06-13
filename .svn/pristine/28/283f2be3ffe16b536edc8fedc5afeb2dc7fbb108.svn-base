package com.micsig.tbook.tbookscope.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.ui.MTextView;
import com.micsig.tbook.ui.R;

import java.time.LocalDateTime;
import java.time.YearMonth;

import io.reactivex.rxjava3.functions.Consumer;

public class TopViewTimeSelector extends LinearLayout {
    public enum Type {START,STOP};

    protected TopDialogNumberKeyBoard dialogKeyBoard;

    public TopViewTimeSelector(Context context) {
        this(context, null);
    }

    public TopViewTimeSelector(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopViewTimeSelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(context, attrs, defStyleAttr);
    }
    private Type type;
    private Context context;

    private int resultYear, resultMonth, resultDay,resultHour, resultMinute, resultSecond;
    private TextView yearInput,monthInput,dayInput,hourInput,minuteInput,secondInput;

    public    GradientDrawable drawable = new GradientDrawable();
    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        View view = View.inflate(context, R.layout.view_top_time_selector, this);
        setOrientation(HORIZONTAL);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewTimeSelector);
        int typeValue = ta.getInt(R.styleable.TopViewTimeSelector_type,0);
        this.type = Type.values()[typeValue];
        ta.recycle();
        yearInput = view.findViewById(R.id.yearInput);
        monthInput = findViewById(R.id.monthInput);
        dayInput = findViewById(R.id.dayInput);
        hourInput = findViewById(R.id.hourInput);
        minuteInput = findViewById(R.id.minuteInput);
        secondInput = findViewById(R.id.secondInput);
        yearInput.setOnClickListener(onClickListener);
        monthInput.setOnClickListener(onClickListener);
        dayInput.setOnClickListener(onClickListener);
        hourInput.setOnClickListener(onClickListener);
        minuteInput.setOnClickListener(onClickListener);
        secondInput.setOnClickListener(onClickListener);
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setStroke(2, Color.GRAY);
        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById((com.micsig.tbook.tbookscope.R.id.dialogNumberKeyBoard));
        initControl();
    }

    public void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MSG_HIDE_INPUT_BACKGROUND).subscribe(consumerCommandToUI);
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
             if (v.getId() == yearInput.getId()) {
                dialogKeyBoard.setDecimalData(4, IDigits.DIGITS_10, onYearListener);
                yearInput.setBackground(drawable);
            }else if (v.getId() == monthInput.getId()) {
                dialogKeyBoard.setDecimalData(2, IDigits.DIGITS_10, onMonthListener);
                 monthInput.setBackground(drawable);
             }else if (v.getId() == dayInput.getId()) {
                 dialogKeyBoard.setDecimalData(2, IDigits.DIGITS_10, onDayListener);
                 dayInput.setBackground(drawable);
             }else if (v.getId() == hourInput.getId()) {
                 dialogKeyBoard.setDecimalData(2, IDigits.DIGITS_10, onHourListener);
                 hourInput.setBackground(drawable);
             }else if (v.getId() == minuteInput.getId()) {
                 dialogKeyBoard.setDecimalData(2, IDigits.DIGITS_10, onMinuteListener);
                 minuteInput.setBackground(drawable);
             }else if (v.getId() == secondInput.getId()) {
                 dialogKeyBoard.setDecimalData(2, IDigits.DIGITS_10, onSecondListener);
                 secondInput.setBackground(drawable);
             }
        }
    };
    private TopDialogNumberKeyBoard.OnDismissListener onNumSubFixListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onYearListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            if(result == null || result.trim().isEmpty()){
                return ;
            }
            int inputYear = Integer.parseInt(result.trim());
            int year = LocalDateTime.now().getYear();
            if(inputYear - year >1 || inputYear<year){
                DToast.get().show(R.string.inputCorrectYear);
            }else {
                resultYear = inputYear;
                yearInput.setText(result);
                yearInput.setBackground(null);
                if(type==Type.STOP){
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_TIME,String.valueOf(getTime()));
                }
            }

        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onMonthListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            if(result == null || result.trim().isEmpty()){
                return ;
            }
            int inputMonth = Integer.parseInt(result.trim());
            if(inputMonth >=1 && inputMonth <= 12){
                resultMonth = inputMonth;
                monthInput.setText(result);
                monthInput.setBackground(null);
                if(type==Type.STOP){
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_TIME,String.valueOf(getTime()));
                }
            }else {
                DToast.get().show(R.string.inputCorrectMonth);
            }
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onDayListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            if(result == null || result.trim().isEmpty()){
                return ;
            }
            String inputStringYear = (String) yearInput.getText();
            String inputStringMonth = (String) monthInput.getText();
            int inputYear = Integer.parseInt(inputStringYear.trim());
            int inputMonth = Integer.parseInt(inputStringMonth.trim());
            int maxDay = YearMonth.of(inputYear,inputMonth).lengthOfMonth();
            int inputDay = Integer.parseInt(result.trim());
            if(inputDay >=1 && inputDay <= maxDay){
                resultDay = inputDay;
                dayInput.setText(String.valueOf(inputDay));
                dayInput.setBackground(null);
                if(type==Type.STOP){
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_TIME,String.valueOf(getTime()));
                }
            }else {
                DToast.get().show(R.string.inputCorrectDay);
            }
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onHourListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            if(result == null || result.trim().isEmpty()){
                return ;
            }
            int inputHour = Integer.parseInt(result.trim());
            if(inputHour >=0 && inputHour <= 23){
                resultHour = inputHour;
                hourInput.setText(String.valueOf(inputHour));
                hourInput.setBackground(null);
                if(type==Type.STOP){
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_TIME,String.valueOf(getTime()));
                }
            }else {
                DToast.get().show(R.string.inputCorrectHour);
            }
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onMinuteListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            if(result == null || result.trim().isEmpty()){
                return ;
            }
            int inputMinute = Integer.parseInt(result.trim());
            if(inputMinute >=0 && inputMinute <= 59){
                resultMinute = inputMinute;
                minuteInput.setText(String.valueOf(inputMinute));
                minuteInput.setBackground(null);
                if(type==Type.STOP){
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_TIME,String.valueOf(getTime()));
                }
            }else {
                DToast.get().show(R.string.inputCorrectMinute);
            }
        }
    };

    private TopDialogNumberKeyBoard.OnDismissListener onSecondListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            if(result == null || result.trim().isEmpty()){
                return ;
            }
            int inputSecond = Integer.parseInt(result.trim());
            if(inputSecond >=0 && inputSecond <= 59){
                resultSecond = inputSecond;
                secondInput.setText(String.valueOf(inputSecond));
                secondInput.setBackground(null);
                if(type==Type.STOP){
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SAVE_STOP_TIME,String.valueOf(getTime()));
                }
            }else {
                DToast.get().show(R.string.inputCorrectMinute);
            }
        }
    };

    public LocalDateTime getTime(){
        return LocalDateTime.of(resultYear,resultMonth,resultDay,resultHour,resultMinute,resultSecond);
    }

    public void setTimeSelectorByString(String textTime){
        LocalDateTime dateTime = LocalDateTime.parse(textTime);
        yearInput.setText(String.valueOf(dateTime.getYear()));
        monthInput.setText(String.valueOf(dateTime.getMonthValue()));
        dayInput.setText(String.valueOf(dateTime.getDayOfMonth()));
        hourInput.setText(String.valueOf(dateTime.getHour()));
        minuteInput.setText(String.valueOf(dateTime.getMinute()));
        secondInput.setText(String.valueOf(dateTime.getSecond()));
    }

    public void setNowTime(){
        LocalDateTime now =LocalDateTime.now();
        resultYear = now.getYear();
        resultMonth = now.getMonthValue();
        resultDay = now.getDayOfMonth();
        resultHour = now.getHour();
        resultMinute = now.getMinute();
        resultSecond = now.getSecond();
        yearInput.setText(String.valueOf(resultYear));
        monthInput.setText(String.valueOf(resultMonth));
        dayInput.setText(String.valueOf(resultDay));
        hourInput.setText(String.valueOf(resultHour));
        minuteInput.setText(String.valueOf(resultMinute));
        secondInput.setText(String.valueOf(resultSecond));
    }
    private Consumer<Boolean> consumerCommandToUI = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean b ) throws Exception {
            yearInput.setBackground(null);
            monthInput.setBackground(null);
            dayInput.setBackground(null);
            hourInput.setBackground(null);
            minuteInput.setBackground(null);
            secondInput.setBackground(null);
        }
    };


    public void setReadOnly(boolean enabled){
        super.setEnabled(enabled);
        if(enabled){
            yearInput.setEnabled(true);
            monthInput.setEnabled(true);
            dayInput.setEnabled(true);
            hourInput.setEnabled(true);
            minuteInput.setEnabled(true);
            secondInput.setEnabled(true);
        }else {
            yearInput.setEnabled(false);
            monthInput.setEnabled(false);
            dayInput.setEnabled(false);
            hourInput.setEnabled(false);
            minuteInput.setEnabled(false);
            secondInput.setEnabled(false);
        }

    }
}
