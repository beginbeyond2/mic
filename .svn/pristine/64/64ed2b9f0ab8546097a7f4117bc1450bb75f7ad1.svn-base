package com.micsig.tbook.tbookscope.menu;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.ui.MButton_CheckBox;

/**
 * Created by liwb on 2017/3/27.
 */

public class SliderZone {
    private static final String TAG = "SliderZone";

    public static final int SlipDirectionSize_X = 20;
    public static final int SlipDirectionSize_Y = 20;

    public static final int SliderDir_None = 0x00;
    public static final int SliderDir_LeftToRight = 0x01;
    public static final int SliderDir_RightToLeft = ~SliderDir_LeftToRight;
    public static final int SliderDir_TopToBottom = 0x02;
    public static final int SliderDir_BottomToTop = ~SliderDir_TopToBottom;

    public interface ISliderZone {
        /**
         * 返回可滑动有效范围
         */
        public Rect getAvailableSliderRect();
    }

    //region  属性
    private MButton_CheckBox checkBox;

    public void setCheckBox(MButton_CheckBox checkBox) {
        this.checkBox = checkBox;
    }

    private String NameTag;

    public String getNameTag() {
        return NameTag;
    }

    public void setNameTag(String name) {
        this.NameTag = name;
    }

    //滑动方向,显示的方向
    private int SliderDir = SliderDir_LeftToRight;
    /**
     * 滑出菜单的范围，指定固定范围。如：固定的下滑菜单与上滑菜单
     */
    private Rect DownZone_Hide_Screen = null;
    /**
     * 滑出菜单的范围，根据按钮大小动态改变。如：通道菜单的通道状态变化后，按范围大小生成
     */
    private ISliderZone sliderZoneFromBtn = null;

    /**
     * 不通过ISliderZone.getAvailableSliderRect()获取可划出范围，
     * 直接设置范围值
     */
    private Rect Slider_Zone_Rect = null;//直接设置可划出范围

    /*
      显示的有效区域可以通过一些计处得出来。
      显示位置+滑动方向+ViewGroup的大小=显示在屏幕上的区域
     */
    private Rect DownZone_Show_Sreen = null;

    private Rect ShowLayout_Zone = null;
    private Rect HideLayout_Zone = null;

    /*
    显示菜单的起始位置。
                 */
    private int ShowMenu_BeginPosion;
    /*
    菜单类
     */
    private View SliderViewGroup;

    /*
        操作是否做用到这个菜单
     */
    private boolean Enable;
    /*
        当前的显示状态 true 显示，false不显示
    */
    private boolean CurrShowState;

    /**
     * 允许滑动
     */
    private boolean EnableSlip;

    public boolean isEnableSlip() {
        return EnableSlip;
    }

    /**
     * 设置是否允许滑动
     *
     * @param enableSlip
     */
    public void setEnableSlip(boolean enableSlip) {
        EnableSlip = enableSlip;
    }

    public int getSliderDir() {
        return SliderDir;
    }

    public void setSliderDir(int sliderDir) {
        SliderDir = sliderDir;
    }

    public boolean isCurrShowState() {
        return CurrShowState;
    }

    public void setCurrShowState(boolean currShowState) {
        CurrShowState = currShowState;
        if (currShowState){
            Tools.PrintControlsLocation(NameTag,(ViewGroup) SliderViewGroup);
        }
    }


    public int getShowMenu_BeginPosion() {
        return ShowMenu_BeginPosion;
    }

    public void setShowMenu_BeginPosion(int showMenu_BeginPosion) {
        ShowMenu_BeginPosion = showMenu_BeginPosion;
    }

    public boolean isEnable() {
        return Enable;
    }

    public void setEnable(boolean enable) {
        Enable = enable;
    }


    public View getSliderViewGroup() {
        return SliderViewGroup;
    }

    public void setSliderViewGroup(View sliderViewGroup) {
        SliderViewGroup = sliderViewGroup;
    }

    public Rect getShowLayout_Zone() {
        return ShowLayout_Zone;
    }

    public void setShowLayout_Zone(Rect showLayout_Zone) {
        ShowLayout_Zone = showLayout_Zone;
    }

    public Rect getHideLayout_Zone() {
        return HideLayout_Zone;
    }

    public void setHideLayout_Zone(Rect hideLayout_Zone) {
        HideLayout_Zone = hideLayout_Zone;
    }

    public void setSliderZoneFromBtn(ISliderZone sliderZoneFromBtn){
        this.sliderZoneFromBtn=sliderZoneFromBtn;
    }

    public Rect getSlider_Zone_Rect() {
        return Slider_Zone_Rect;
    }

    public void setSlider_Zone_Rect(Rect slider_Zone_Rect) {
        Slider_Zone_Rect = slider_Zone_Rect;
    }

    //endregion

    /**
     * 通过一些计算得出来的参数，所有参数都设置完成后，最后调用
     * BUG:从右到左的菜单显示会偏差2个像素，原因没有找到.以下补上两个像素
     */
    public void createParam(boolean update) {
        if (HideLayout_Zone != null && !update) return;


        //生成显示时的有效区域
        int left, top, width, height;
        ViewGroup parentViewGroup = (ViewGroup) this.SliderViewGroup.getParent();
        switch (this.getSliderDir()) {
            case SliderDir_BottomToTop: {
                left = parentViewGroup.getLeft();
                width = this.SliderViewGroup.getWidth();
                top = 0;
                height = this.SliderViewGroup.getHeight();
                DownZone_Show_Sreen = new Rect(left, top, left + width, height + top);
            }
            break;
            case SliderDir_TopToBottom: {
                left = parentViewGroup.getLeft();
                width = this.SliderViewGroup.getWidth();
                top = this.getShowMenu_BeginPosion();
                height = this.SliderViewGroup.getHeight();
                DownZone_Show_Sreen = new Rect(left, top, left + width, height + top);
            }
            break;
            case SliderDir_LeftToRight: {
                left = getShowMenu_BeginPosion();
                width = this.SliderViewGroup.getWidth();
                top = parentViewGroup.getTop();
                height = this.SliderViewGroup.getHeight();
                DownZone_Show_Sreen = new Rect(left, top, left + width, height + top);
            }
            break;
            case SliderDir_RightToLeft: {
                left = getShowMenu_BeginPosion() - this.SliderViewGroup.getWidth();
                width = this.SliderViewGroup.getWidth();
                top = parentViewGroup.getTop();
                height = this.SliderViewGroup.getHeight();
                DownZone_Show_Sreen = new Rect(left, top, left + width, height + top);
            }
            break;
            default:
                DownZone_Show_Sreen = null;
                break;
        }

        // 生成显示RECT
        switch (this.SliderDir) {
            case SliderDir_LeftToRight: {
                left = SliderViewGroup.getLeft() + SliderViewGroup.getWidth();
                top = this.SliderViewGroup.getTop();
                width = SliderViewGroup.getWidth();
                height = SliderViewGroup.getHeight();
            }
            break;
            case SliderDir_RightToLeft: {
                left = SliderViewGroup.getLeft() - SliderViewGroup.getWidth();
                top = this.SliderViewGroup.getTop();
                width = SliderViewGroup.getWidth();
                height = SliderViewGroup.getHeight();
            }
            break;
            case SliderDir_TopToBottom: {
                left = this.SliderViewGroup.getLeft();
                top = SliderViewGroup.getHeight() + SliderViewGroup.getTop();
                width = SliderViewGroup.getWidth();
                height = SliderViewGroup.getHeight();
            }
            break;
            case SliderDir_BottomToTop: {
                left = this.SliderViewGroup.getLeft();
                top = SliderViewGroup.getHeight() - SliderViewGroup.getTop();
                width = SliderViewGroup.getWidth();
                height = SliderViewGroup.getHeight();
            }
            break;
            default:
                left = 0;
                top = 0;
                width = 0;
                height = 0;
                break;
        }
        ShowLayout_Zone = new Rect(left, top, left + width, top + height);
        //生成隐藏RECT
        left = this.SliderViewGroup.getLeft();
        top = this.SliderViewGroup.getTop();
        width = this.SliderViewGroup.getWidth();
        height = this.SliderViewGroup.getHeight();
        HideLayout_Zone = new Rect(left, top, left + width, top + height);

    }

    /*
    返回滑动方向
     */
    public static int getSlipDirection(int oldX, int oldY, int newX, int newY) {
        int slipDir;
        if (Math.abs(oldY - newY) < SlipDirectionSize_Y) {
            //说明是左右滑动
            if (oldX - newX > 0 && Math.abs(oldX - newX) > SlipDirectionSize_X) {
                slipDir = SliderDir_RightToLeft;
            } else if (oldX - newX < 0 && Math.abs(oldX - newX) > SlipDirectionSize_X) {
                slipDir = SliderDir_LeftToRight;
            } else {
                slipDir = SliderDir_None;
            }
        } else {
            //说明是上下滑动
            if (oldY - newY > 0 && Math.abs(oldY - newY) > SlipDirectionSize_Y) {
                slipDir = SliderDir_BottomToTop;
            } else if (oldY - newY < 0 && Math.abs(oldY - newY) > SlipDirectionSize_Y) {
                slipDir = SliderDir_TopToBottom;
            } else {
                slipDir = SliderDir_None;
            }
        }

        return slipDir;
    }

    private Rect enableSlipFalseRect = new Rect(-1, -1, -1, -1);

    public Rect getDownZone_Hide_Screen() {
        if (EnableSlip) {
            if (sliderZoneFromBtn != null) {
                return sliderZoneFromBtn.getAvailableSliderRect();
            } else if (Slider_Zone_Rect != null) {
                return Slider_Zone_Rect;
            } else {
                return DownZone_Hide_Screen;
            }
        } else return enableSlipFalseRect;
    }

    public void setDownZone_Hide_Screen(Rect downZone_Hide_Screen) {
        DownZone_Hide_Screen = downZone_Hide_Screen;
    }

    public Rect getDownZone_Show_Sreen() {
        return DownZone_Show_Sreen;
    }

    public void setDownZone_Show_Sreen(Rect downZone_Show_Sreen) {
        DownZone_Show_Sreen = downZone_Show_Sreen;
    }

    /**
     * 在菜单中的控件 滑动后取消操作
     */
    public static void cancelViewMotion_InView(MotionEvent event, View view) {
        View targetView = null;
        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            View tem = ((ViewGroup) view).getChildAt(i);
            Rect rect = new Rect();
            tem.getGlobalVisibleRect(rect);
            if (rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                targetView = tem;
                break;
            }

        }
        if (targetView == null) return;
        event.setAction(MotionEvent.ACTION_CANCEL);
        targetView.dispatchTouchEvent(event);
    }

    /**
     * 与菜单平行的控件，滑动后取消操作，目前只有下菜单,且只有滑出时
     */
    public void cancelViewMotion_OutView(MotionEvent event, View mainViewGroup) {
        View view = mainViewGroup.findViewById(R.id.mBottomBar);
        cancelViewMotion_InView(event, view);
    }
}
