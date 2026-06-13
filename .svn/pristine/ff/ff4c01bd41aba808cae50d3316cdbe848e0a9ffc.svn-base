package com.micsig.tbook.ui.top.view.channel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.util.svg.SelectorUtil;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/5.
 */

public class TopViewChannelMultipleChoice {

    private final Context context;
    private View inflate;
    private String[] arrayString;
    private int[] arrayColor;
    private LinearLayout llMutiCheckChannel;
    private boolean showAll = true;
    private int itemWidth = 0;
    private int itemHeight = 90;
    private onItemOnlyClickListener onlyClickListener;
    private onTestListener onTestListener;
    private int chType = 0;//0->ch  1->Math 2->Ref

    public interface onItemOnlyClickListener {
        void onlyClick(CheckBox checkBox);
    }

    public interface onTestListener {
        void onTest(CheckBox checkBox);
    }

    public TopViewChannelMultipleChoice(Context context) {
        this.context = context;
        initView();
    }

    public TopViewChannelMultipleChoice(Context context, boolean showAll) {
        this.context = context;
        this.showAll = showAll;
        initView();
    }

    public TopViewChannelMultipleChoice(Context context, boolean showAll, onItemOnlyClickListener onlyClickListener) {
        this.context = context;
        this.showAll = showAll;
        this.onlyClickListener = onlyClickListener;
        initView();
    }


    public void setData(int arrayResId, int arrayColorResId) {
        this.setData(arrayResId, arrayColorResId, null);
    }

    public void setData(int arrayResId, int arrayColorResId, onTestListener onTestListener) {
        this.onTestListener = onTestListener;
        this.arrayString = context.getResources().getStringArray(arrayResId);
        this.arrayColor = SvgNodeInfo.getColorsIntForView();
        updateView(context);
    }

    public void setData(int arrayResId, int arrayColorResId, int width, int height, int chType) {
        this.arrayString = context.getResources().getStringArray(arrayResId);
        this.arrayColor = SvgNodeInfo.getColorsIntForView();
        this.itemWidth = width;
        this.itemHeight = height;
        this.chType = chType;
        updateView(context);
    }

    public CheckBox getShowViewIndex(int index) {
//        int count = showAll ? llMutiCheckChannel.getChildCount() - 1 : llMutiCheckChannel.getChildCount();
        if (index >= 0 && index < llMutiCheckChannel.getChildCount()) {
            return (CheckBox) llMutiCheckChannel.getChildAt(index);
        } else {
            return null;
        }
    }

    public List<CheckBox> getSelectedCheckBox() {
        List<CheckBox> checkBoxes = new ArrayList<>();
        int count = showAll ? llMutiCheckChannel.getChildCount() - 1 : llMutiCheckChannel.getChildCount();
        for (int i = 0; i < count; i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i);
            if (checkBox.isChecked() && checkBox.getVisibility() == View.VISIBLE) {
                checkBoxes.add(checkBox);
            }
        }
        return checkBoxes;
    }

    public int getSelectCount() {
        int selectCount = 0;
        int count = showAll ? llMutiCheckChannel.getChildCount() - 1 : llMutiCheckChannel.getChildCount();
        for (int i = 0; i < count; i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i);
            if (checkBox.isChecked() && checkBox.getVisibility() == View.VISIBLE) {
                selectCount++;
            }
        }
        return selectCount;
    }

    public List<Integer> getSelectedIndex() {
        List<Integer> selectS = new ArrayList<>();
        int count = showAll ? llMutiCheckChannel.getChildCount() - 1 : llMutiCheckChannel.getChildCount();
        for (int i = 0; i < count; i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i);
            if (checkBox.isChecked() && checkBox.getVisibility() == View.VISIBLE) {
                selectS.add(i);
            }
        }
        return selectS;
    }

    public void setChecked(int checkedIndex) {
        for (int i = 0; i < llMutiCheckChannel.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i);
            if (i == checkedIndex) {
                checkBox.setChecked(true);
            }
        }
    }

    public View getInflate() {
        return inflate;
    }

    public void setItemVisible(boolean[] visible) {
        setCheckBoxItemVisible(visible);
    }

    private void setCheckBoxItemVisible(boolean[] visible) {
        int count = showAll ? llMutiCheckChannel.getChildCount() - 1 : llMutiCheckChannel.getChildCount();
        for (int i = 0; i < count; i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i);
            checkBox.setVisibility(visible[i] ? View.VISIBLE : View.GONE);
            if (checkBox.getVisibility() == View.GONE) {
                checkBox.setChecked(false);
            }
        }
        updateFinalAllstate();
    }


    public void setChannelColor(int chIndex, String colorStr) { //只更新个别
        int viewIndex = TChan.toFpgaChNo(chIndex);
        CheckBox checkBox = getShowViewIndex(viewIndex);
        if(checkBox == null) return;
        arrayColor = SvgNodeInfo.getColorsIntForView();
        checkBox.setTextColor(arrayColor[viewIndex]);
        setBtnDrawable(checkBox, viewIndex);
    }


    public void setChannelColorForDialogCSv(int chIndex, String colorStr) { //只更新个别
        int viewIndex = TChan.toFpgaChNo(chIndex);
        CheckBox checkBox = getShowViewIndex(viewIndex);
        switch (chType) {
            case 1:
                viewIndex = TChan.toFpgaChNo(TChan.toMathNumber(chIndex));
                break;
            case 2:
                viewIndex = TChan.toFpgaChNo(TChan.toRefNumber(chIndex));
                break;
            case 0:
            default:
                break;
        }
        checkBox = getShowViewIndex(viewIndex);
        if(checkBox == null) return;
        arrayColor = SvgNodeInfo.getColorsIntForView();
        checkBox.setTextColor(arrayColor[TChan.toFpgaChNo(chIndex)]);
        setBtnDrawable(checkBox, viewIndex);
    }

    /**
     * @return ch: 0--8
     */
    public List<Integer> getSelectChannel() {
        return getSelectedIndex();
    }

    public LinearLayout getCheckBoxs() {
        return llMutiCheckChannel;
    }

    private void initView() {
        inflate = View.inflate(context, R.layout.view_muti_check_channel, null);
    }

    private void updateView(Context context) {
        llMutiCheckChannel = (LinearLayout) inflate.findViewById(R.id.ll_muti_check_channel);
        llMutiCheckChannel.setPadding(10, 0, 0, 1);
        int count = showAll ? llMutiCheckChannel.getChildCount() - 1 : llMutiCheckChannel.getChildCount();
        for (int i = count; i >= 0; i--) {
            llMutiCheckChannel.removeView(llMutiCheckChannel.getChildAt(i));
        }
        int length = showAll ? arrayString.length + 1 : arrayString.length;
        for (int i = 0; i < length; i++) {
            CheckBox checkBox = new CheckBox(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, itemHeight);
            if (!showAll) {
                layoutParams.setMarginStart(30);
            }
            layoutParams.setMarginEnd(30);
            int r = 30;
            int tb = 0;
            checkBox.setPadding(0, tb, r, tb);
            checkBox.setLayoutParams(layoutParams);
            checkBox.setGravity(Gravity.CENTER);
            checkBox.setId(View.generateViewId());
            if (showAll && i == length - 1) {
                checkBox.setText("ALL");
                checkBox.setTextColor(context.getResources().getColor(R.color.colorChCommon));
            } else {
                checkBox.setText(arrayString[i]);
                checkBox.setTextColor(arrayColor[i]);
            }
            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
            checkBox.setBackground(null);
            checkBox.setButtonDrawable(null);
            setBtnDrawable(checkBox, i);
            checkBox.setCompoundDrawablePadding(8);
            if (showAll) {
                if (i != arrayString.length) {
                    checkBox.setOnCheckedChangeListener(checkedChangeListener);
                } else {
                    checkBox.setOnClickListener(clickListener);
                }
            } else {
                if (i != arrayString.length) {
                    checkBox.setOnCheckedChangeListener(checkedChangeListener);
                }
                checkBox.setOnClickListener(clickListener);
            }

            llMutiCheckChannel.addView(checkBox);
        }

        updateChild();
    }

    private void setBtnDrawable(CheckBox checkBox, int index) {
        Drawable drawable = SelectorUtil.createMuChoiceDrawable(TChan.toUiChNo(index));
        if (chType == 0) {//ch
            drawable = SelectorUtil.createMuChoiceDrawable(TChan.toUiChNo(index));
        } else if (chType == 1) { //math
            drawable = SelectorUtil.createMuChoiceDrawable(TChan.toUiChNo(index) + TChan.Ch8);
        } else if (chType == 2) { //ref
            drawable = SelectorUtil.createMuChoiceDrawable(TChan.toUiChNo(index) + TChan.Math8);
        } else { //default
            drawable = SelectorUtil.createMuChoiceDrawable(TChan.toUiChNo(index) + TChan.R8);
        }
        drawable.setBounds(0, 0, 21, 21);
        checkBox.setCompoundDrawables(drawable,null,null,null);
    }

    //针对All的点击操作
    private final View.OnClickListener clickListener = v -> {
        if(showAll) {
            CheckBox allCheckBox = (CheckBox) llMutiCheckChannel.getChildAt(llMutiCheckChannel.getChildCount() - 1);
            boolean isCheck = allCheckBox.isChecked();
            for (int i = 0; i < llMutiCheckChannel.getChildCount() - 1; i++) {
                CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i);
                checkBox.setChecked(checkBox.getVisibility() == View.VISIBLE && isCheck);
            }
        } else {
            if(onlyClickListener != null) {
                onlyClickListener.onlyClick((CheckBox) v);
            }
        }
    };

    private final CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            updateFinalAllstate();
            if (onTestListener != null) {
                onTestListener.onTest((CheckBox) buttonView);
            }
        }
    };

    private void updateFinalAllstate() {
        if (!showAll) return;
        CheckBox finalAllCheckBox = (CheckBox) llMutiCheckChannel.getChildAt(llMutiCheckChannel.getChildCount() - 1);
        boolean isChecked = true;//先默认选中
        for (int i = 0; i < llMutiCheckChannel.getChildCount() - 1; i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i);
            if (checkBox.getVisibility() == View.VISIBLE && !checkBox.isChecked()) {//显示的有一个未选中则 finalAll设置为不选中状态
                isChecked = false;
                break;
            }
        }
        finalAllCheckBox.setChecked(isChecked);
    }

    public void updateChild() {
        if (itemWidth == 0) return;
        for (int i = 0; i < llMutiCheckChannel.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i);
            checkBox.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            checkBox.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) checkBox.getLayoutParams();
            layoutParams.width = itemWidth;
            layoutParams.height = itemHeight;
            llMutiCheckChannel.getChildAt(i).setLayoutParams(layoutParams);
        }
    }

    public void unCheckAll() {
        for (int i = 0; i < llMutiCheckChannel.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i);
            checkBox.setChecked(false);
        }
    }

    @SuppressLint("ResourceType")
    public void setReadOnly(boolean enabled) {
        int length = showAll ? arrayString.length + 1 : arrayString.length;
        for (int i = 0; i < llMutiCheckChannel.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) llMutiCheckChannel.getChildAt(i);
            if(!enabled){
                if (!checkBox.isChecked()) {
                    checkBox.setTextColor(context.getResources().getColor(R.color.textColorNewTopViewDisable));
                    checkBox.setEnabled(false);
                } else {
                    checkBox.setClickable(false);
                }
            }
            else {
//               checkBox.setText(arrayString[i]);
                checkBox.setTextColor(arrayColor[i]);
                checkBox.setEnabled(true);
                checkBox.setClickable(true);
                if (showAll && i == length - 1) {
                    checkBox.setText("ALL");
                    checkBox.setTextColor(context.getResources().getColor(R.color.colorChCommon));
                }
            }
        }
    }
}
