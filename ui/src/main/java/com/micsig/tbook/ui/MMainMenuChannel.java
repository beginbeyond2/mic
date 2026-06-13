package com.micsig.tbook.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.micsig.tbook.ui.top.view.channel.TopViewChannel;
import com.micsig.tbook.ui.top.view.channel.TopViewChannelMultipleChoice;

import java.util.List;

/**
 * @auother Liwb
 * @description:
 * @data:2024-2-26 16:59
 */
public class MMainMenuChannel extends RelativeLayout {
    private Context context;
    public MMainMenuChannel(Context context) {
        this(context,null);
    }

    public MMainMenuChannel(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MMainMenuChannel(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public MMainMenuChannel(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context=context;
        initView();
    }

    private TopViewChannel viewChannel;
    private TopViewChannelMultipleChoice viewChannelMultipleChoice;
    private HorizontalScrollView scrollView;
    private ImageView imgLeft, imgRight;
    public TopViewChannel.onItemClickListener onItemClickListener = null;
    private TopViewChannelMultipleChoice.onTestListener onItemTestListener = null;
    private LinearLayout llInScrollView;

    private void initView() {
        ViewGroup root= (ViewGroup) View.inflate(context,R.layout.view_main_menu_channel,this);
        imgLeft = root.findViewById(R.id.menu_channel_left);
        imgRight = root.findViewById(R.id.menu_channel_right);
        scrollView =  root.findViewById(R.id.main_menu_channel_scroll_view);
        llInScrollView = new LinearLayout(context);
        llInScrollView.setOrientation(LinearLayout.HORIZONTAL);
        viewChannel = new TopViewChannel(context);
        viewChannel.setData(R.array.popArrayAllChannelForSaveWave, R.array.popArrayAllChanColorForSaveWave, onChannelItemClickListener);

        viewChannelMultipleChoice = new TopViewChannelMultipleChoice(context);
        viewChannelMultipleChoice.setData(R.array.popArrayAllChannelForSaveWave, R.array.popArrayAllChanColorForSaveWave, onTestListener);


        llInScrollView.addView(viewChannelMultipleChoice.getInflate());
        llInScrollView.addView(viewChannel.getInflate());
        llInScrollView.getChildAt(0).setVisibility(View.GONE);//默认不显示

        scrollView.addView(llInScrollView);

        imgLeft.setOnClickListener(onClickListener);
        imgRight.setOnClickListener(onClickListener);
    }
    public void setData(int arrayResId, int arrayColorResId){
        viewChannel.setData(arrayResId, arrayColorResId, onChannelItemClickListener);
    }

    public void setAllSelectShow(boolean isShowMuti) {
        llInScrollView.getChildAt(0).setVisibility(isShowMuti ? View.VISIBLE : View.GONE);
        llInScrollView.getChildAt(1).setVisibility(isShowMuti ? View.GONE : View.VISIBLE);
    }


    private TopViewChannel.onItemClickListener onChannelItemClickListener = (viewId, checkedIndex, radioButton) -> {
        getOffsetXShowToParent(radioButton);
        if (onItemClickListener != null) {
            onItemClickListener.checkChanged(viewId, checkedIndex, radioButton);
        }
    };

    private TopViewChannelMultipleChoice.onTestListener onTestListener = checkBox -> {
//        getOffsetXShowToParent(checkBox);
        if (onItemTestListener != null) {
            onItemTestListener.onTest(checkBox);
        }
    };

    public void setChangeListener(TopViewChannel.onItemClickListener changeListener, TopViewChannelMultipleChoice.onTestListener testListener) {
       this.onItemClickListener=changeListener;
        this.onItemTestListener = testListener;
    }

    public void setChecked(int checkedIndex) {
        viewChannel.setChecked(checkedIndex);
    }

    public void setItemVisible(boolean[] visible, boolean isForwardSelected) {
        viewChannel.setItemVisible(visible, isForwardSelected);
        if (llInScrollView.getChildAt(0).getVisibility() == View.VISIBLE) {
            viewChannelMultipleChoice.setItemVisible(visible);
        }
        showLeftRightImg(visible);
    }

    public void setChannelColor(int chIndex, String colorStr) {
        viewChannel.setChannelColor(chIndex, colorStr);
        if (llInScrollView.getChildAt(0).getVisibility() == View.VISIBLE) {
            viewChannelMultipleChoice.setChannelColor(chIndex, colorStr);
        }
    }

    public int getSelectChannel(){
        return viewChannel.getSelectChannel();
    }

    public List<Integer> getAllSelectChannel() {
       return viewChannelMultipleChoice.getSelectChannel();
    }

    public RadioButton getSelectedRadioButton(){
        return viewChannel.getSelectedRadioButton();
    }

    public int getSelectedIndex(){
        return viewChannel.getSelectedIndex();
    }

    public RadioGroup getRadioGroup(){
        return viewChannel.getRadioGroup();
    }

    public LinearLayout getCheckBoxs() {
        return viewChannelMultipleChoice.getCheckBoxs();
    }

    public TopViewChannel getViewChannel() {
        return viewChannel;
    }

    public TopViewChannelMultipleChoice getViewChannelMultipleChoice() {
        return viewChannelMultipleChoice;
    }

    public void moveOnlyScroll(int curIndex) {
        if (viewChannel.getInflate().getVisibility() == View.VISIBLE) {
            RadioButton radioButton = viewChannel.getShowViewIndex(curIndex);
            if (radioButton != null) {
                getOffsetXShowToParent(radioButton);
            }
        }
        if (viewChannelMultipleChoice.getInflate().getVisibility() == View.VISIBLE) {
            CheckBox checkBox = viewChannelMultipleChoice.getShowViewIndex(curIndex);
            if (checkBox != null) {
                getOffsetXShowToParent(checkBox);
            }
        }
    }

    private int getOffsetXShowToParent(View radioButton){
        int scrollWidth=scrollView.getWidth();
        Rect outRect=new Rect();
        Rect srcRect=new Rect(0,0,radioButton.getWidth(),radioButton.getHeight());
        radioButton.getLocalVisibleRect(outRect);
        if (outRect.left==srcRect.left && outRect.top==srcRect.top
                && outRect.width()==srcRect.width() && outRect.height()==srcRect.height()){
            return 0;
        }else if (outRect.left<0){
            //左边完全不显示
            scrollView.scrollBy(outRect.left,0);
        }else if (outRect.right>scrollWidth){
            //右边完全不显示
            int offsetX= outRect.right-scrollWidth;
            scrollView.scrollBy(offsetX,0);
        }else if (outRect.left!=0){
            //左边显示不全
            scrollView.scrollBy(-outRect.left,0);
        }else if (outRect.width()!=srcRect.width()){
            //右边显示不全
            int offsetX= srcRect.width()-outRect.width();
            scrollView.scrollBy(offsetX,0);
        }
        return  0;
    }

    /**
     * FixMe 临时解决方案, 后续需要修改
     * @param visible
     */
    public void showLeftRightImg(boolean[] visible) {
        int finalLength = 0;
        for (int i = 0; i < visible.length; i++) {
            if (visible[i]) {
                if (i <= 7) { //Ch1--Ch8
                    finalLength += 127;
                } else if (i <= 15) {//Math1--Math8
//                    finalLength += 144;
                    finalLength += 112;//Math1 -> M1
                } else {//R1--R8
                    finalLength += 112;
                }
            }
        }
        if (finalLength > 1620) {
            imgLeft.setVisibility(View.VISIBLE);
            imgRight.setVisibility(View.VISIBLE);
        } else {
            imgLeft.setVisibility(View.GONE);
            imgRight.setVisibility(View.GONE);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.menu_channel_left) {
                scrollView.smoothScrollBy(-152, 0);
            } else if (id == R.id.menu_channel_right) {
                scrollView.smoothScrollBy(152, 0);
            }
        }
    };

    @SuppressLint("ResourceType")
    public void setReadOnly(boolean enabled) {
        super.setEnabled(enabled);
        if(!enabled){
            viewChannelMultipleChoice.setReadOnly(false);
        }else {
            viewChannelMultipleChoice.setReadOnly(true);
        }
    }

}
