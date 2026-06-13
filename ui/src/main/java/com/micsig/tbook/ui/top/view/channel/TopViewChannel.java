package com.micsig.tbook.ui.top.view.channel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.svg.SelectorUtil;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by Administrator on 2017/4/5.
 */

public class TopViewChannel {
    public static final int CH1 = 0;
    public static final int CH2 = CH1 + 1;
    public static final int CH3 = CH2 + 1;
    public static final int CH4 = CH3 + 1;
    public static final int CH5 = CH4 + 1;
    public static final int CH6 = CH5 + 1;
    public static final int CH7 = CH6 + 1;
    public static final int CH8 = CH7 + 1;
    public static final int MATH1 = CH8 + 1;
    public static final int MATH2 = MATH1 + 1;
    public static final int MATH3 = MATH2 + 1;
    public static final int MATH4 = MATH3 + 1;
    public static final int MATH5 = MATH4 + 1;
    public static final int MATH6 = MATH5 + 1;
    public static final int MATH7 = MATH6 + 1;
    public static final int MATH8 = MATH7 + 1;
    public static final int REF1 = MATH8 + 1;
    public static final int REF2 = REF1 + 1;
    public static final int REF3 = REF2 + 1;
    public static final int REF4 = REF3 + 1;
    public static final int REF5 = REF4 + 1;
    public static final int REF6 = REF5 + 1;
    public static final int REF7 = REF6 + 1;
    public static final int REF8 = REF7 + 1;

    private Context context;
    private View inflate;
    private String[] arrayString;
    private int[] arrayColor;
    private RadioGroup radioGroup;
    private onItemClickListener changeListener;

    public interface onItemClickListener {
        void checkChanged(int viewId, int checkedIndex,RadioButton radio);
    }

    public TopViewChannel(Context context) {
        this.context = context;
        initView();
    }


    public void setData(int arrayResId, int arrayColorResId, onItemClickListener changeListener) {
        this.arrayString = context.getResources().getStringArray(arrayResId);
        arrayColor = SvgNodeInfo.getColorsIntForView();
        this.changeListener = changeListener;
        updateView(context);
    }

    public void setChangeListener(onItemClickListener changeListener) {
        this.changeListener = changeListener;
    }

    public RadioButton getShowViewIndex(int index){
        if (index>=0 && index<radioGroup.getChildCount()){
            return (RadioButton) radioGroup.getChildAt(index);
        }else {
            return null;
        }
    }
    public RadioButton getSelectedRadioButton() {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            if (radioGroup.getChildAt(i).getId() == radioGroup.getCheckedRadioButtonId()) {
                return ((RadioButton) radioGroup.getChildAt(i));
            }
        }
        return null;
    }
    public int getSelectedIndex(){
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            if (radioGroup.getChildAt(i).getId() == radioGroup.getCheckedRadioButtonId()) {
                return i;
            }
        }
        return -1;
    }

    public void setChecked(int checkedIndex) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            if (i == checkedIndex) {
                radioGroup.check(radioGroup.getChildAt(i).getId());
            }
        }
    }

    public View getInflate() {
        return inflate;
    }

    private void setItemVisible_forwardSelected(boolean[] visible) {
        boolean changedSelect = false;
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setVisibility(visible[i] ? View.VISIBLE : View.GONE);
            if (((RadioButton) radioGroup.getChildAt(i)).isChecked() && !visible[i]) {
                changedSelect = true;
            }
        }
        if (changedSelect) {
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                if (radioGroup.getChildAt(i).getVisibility() == View.VISIBLE) {
                    onItemClickListener itemClickListener = changeListener;
                    changeListener = null;
                    radioGroup.check(radioGroup.getChildAt(i).getId());
                    changeListener = itemClickListener;
                    break;
                }
            }
        }
    }
   private void setItemVisible_reverseSelected(boolean[] visible){
       boolean changedSelect = false;
       for (int i = 0; i < radioGroup.getChildCount(); i++) {
           radioGroup.getChildAt(i).setVisibility(visible[i] ? View.VISIBLE : View.GONE);
           if (((RadioButton) radioGroup.getChildAt(i)).isChecked() && !visible[i]) {
               changedSelect = true;
           }
       }
       if (changedSelect) {
           for (int i = radioGroup.getChildCount()-1; i >= 0; i--) {
               if (radioGroup.getChildAt(i).getVisibility() == View.VISIBLE) {
                   onItemClickListener itemClickListener = changeListener;
                   changeListener = null;
                   radioGroup.check(radioGroup.getChildAt(i).getId());
                   changeListener = itemClickListener;
                   break;
               }
           }
       }
   }

    public void setItemVisible(boolean[] visible, boolean isForwardSelected) {
        if (isForwardSelected) {
            setItemVisible_forwardSelected(visible);
        } else {
            setItemVisible_reverseSelected(visible);
        }
    }

    public void setChannelColor(int chIndex, String colorStr) {
        int viewIndex = TChan.toFpgaChNo(chIndex);
        RadioButton radioButton = getShowViewIndex(viewIndex);
        if(radioButton == null) return;
        arrayColor = SvgNodeInfo.getColorsIntForView();
        radioButton.setTextColor(arrayColor[viewIndex]);
        setBtnDrawable(radioButton, viewIndex);
    }

    /**
     * @return ch: 0--8
     */
    public int getSelectChannel() {
        int i = 0;
        for (i = 0; i < radioGroup.getChildCount(); i++) {
            if (radioGroup.getChildAt(i).getId() == radioGroup.getCheckedRadioButtonId()) {
                break;
            }
        }
        return i;
    }

    public RadioGroup getRadioGroup(){
        return  radioGroup;
    }

    private void initView() {
        inflate = View.inflate(context, R.layout.view_channel, null);
    }

    private void updateView(Context context) {
        radioGroup = (RadioGroup) inflate.findViewById(R.id.radioGroupMeasure);
        radioGroup.setPadding(10, 0, 0, 1);
        for(int i=radioGroup.getChildCount()-1;i>=0;i--){
            radioGroup.removeView(radioGroup.getChildAt(i));
        }
        for (int i = 0; i < arrayString.length; i++) {
            RadioButton radioButton = new RadioButton(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 90);
            layoutParams.setMarginEnd(30);
            int r = 30;
            int tb = 0;
            radioButton.setPadding(0, tb, r, tb);
            radioButton.setLayoutParams(layoutParams);
            radioButton.setGravity(Gravity.CENTER);
            radioButton.setText(arrayString[i]);
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
//            radioButton.setTextColor(context.getResources().getColor(R.color.textColor));
            radioButton.setTextColor(arrayColor[i]);
            radioButton.setBackground(null);
            radioButton.setButtonDrawable(null);
            setBtnDrawable(radioButton, i);
            radioButton.setCompoundDrawablePadding(8);
            radioGroup.addView(radioButton);
            if (i == 0) {
                radioGroup.check(radioButton.getId());
//                radioButton.setTextColor(arrayColor[0]);
            }
        }
        radioGroup.setOnCheckedChangeListener(checkedChangeListener);
    }

    private void setBtnDrawable(RadioButton radioButton, int index) {
        Drawable drawable = SelectorUtil.createCheckedDrawable(TChan.toUiChNo(index));
        drawable.setBounds(0, 0, 21, 21);
        radioButton.setCompoundDrawables(drawable, null, null, null);
    }


    private RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            for (int i = 0; i < group.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) group.getChildAt(i);
                if (checkedId == radioButton.getId()) {
//                    radioButton.setTextColor(arrayColor[i]);
                    if (changeListener != null) {
                        changeListener.checkChanged(inflate.getId(), i,radioButton);
                    }
                } else {
//                    radioButton.setTextColor(arrayColor[arrayColor.length - 1]);
                }
            }
        }
    };
    @SuppressLint("ResourceType")
    public void setReadOnly(boolean enabled) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
            if(!enabled){
                if (!radioButton.isChecked()) {
                    radioButton.setTextColor(context.getResources().getColor(R.color.textColorNewTopViewDisable));
                    radioButton.setEnabled(false);
                } else {
                    radioButton.setTextColor(context.getResources().getColorStateList(R.drawable.selector_text_color));
                    radioButton.setClickable(false);
                }
            }else {
                radioButton.setTextColor(context.getResources().getColorStateList(R.drawable.selector_text_color));
                radioButton.setEnabled(true);
                radioButton.setClickable(true);
            }
        }
    }
}
