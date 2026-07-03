package com.micsig.tbook.tbookscope.menu; // 滑动菜单区域管理类所在包

import android.graphics.Rect; // 导入矩形类，用于定义区域范围
import android.view.MotionEvent; // 导入触摸事件类，用于处理滑动事件
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图容器类

import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.tools.Tools; // 导入工具类，用于打印控件位置
import com.micsig.tbook.ui.MButton_CheckBox; // 导入复选框按钮控件

/**
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │                    SliderZone                                       │
 * │                 滑动菜单区域管理类                                    │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：menu → 滑动菜单基础设施层                                   │
 * │ 核心职责：定义滑动菜单的滑出方向、触发区域、显示/隐藏区域               │
 * │ 架构设计：纯数据模型类，配合SlipMenuManager实现菜单滑入滑出动画        │
 * │ 数据流向：SlipMenuManager → SliderZone → Rect区域计算 → 触摸事件分发 │
 * │ 依赖关系：ISliderZone接口（动态区域回调）、Tools（位置打印）           │
 * │ 使用场景：顶部/右侧/底部滑动菜单的区域判定与方向计算                   │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 滑动方向常量：LeftToRight/RightToLeft/TopToBottom/BottomToTop        │
 * │ 区域类型：DownZone_Hide（隐藏时触发区）/ShowLayout（显示时布局区）     │
 * └─────────────────────────────────────────────────────────────────────┘
 *
 * Created by liwb on 2017/3/27.
 */

public class SliderZone { // 滑动菜单区域管理类
    private static final String TAG = "SliderZone"; // 日志标签

    public static final int SlipDirectionSize_X = 20; // 水平滑动判定阈值（像素）
    public static final int SlipDirectionSize_Y = 20; // 垂直滑动判定阈值（像素）

    public static final int SliderDir_None = 0x00; // 无滑动方向
    public static final int SliderDir_LeftToRight = 0x01; // 从左向右滑出
    public static final int SliderDir_RightToLeft = ~SliderDir_LeftToRight; // 从右向左滑出
    public static final int SliderDir_TopToBottom = 0x02; // 从上向下滑出
    public static final int SliderDir_BottomToTop = ~SliderDir_TopToBottom; // 从下向上滑出

    /**
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                    ISliderZone                                       │
     * │                动态滑动区域回调接口                                    │
     ├─────────────────────────────────────────────────────────────────────┤
     * │ 核心职责：由菜单按钮实现，动态返回可滑出区域（如通道菜单按钮变化）       │
     * │ 使用场景：通道状态切换后，菜单按钮大小变化，触发区域需动态计算          │
     * └─────────────────────────────────────────────────────────────────────┘
     */
    public interface ISliderZone { // 动态滑动区域回调接口
        /**
         * 返回可滑动有效范围
         */
        public Rect getAvailableSliderRect(); // 获取可滑出区域的矩形范围
    }

    //region  属性
    private MButton_CheckBox checkBox; // 关联的复选框按钮控件

    /** 设置关联的复选框按钮 */
    public void setCheckBox(MButton_CheckBox checkBox) { // 设置复选框按钮
        this.checkBox = checkBox; // 赋值复选框按钮引用
    }

    private String NameTag; // 菜单名称标签，用于日志标识

    /** 获取菜单名称标签 */
    public String getNameTag() { // 获取名称标签
        return NameTag; // 返回名称标签
    }

    /** 设置菜单名称标签 */
    public void setNameTag(String name) { // 设置名称标签
        this.NameTag = name; // 赋值名称标签
    }

    //滑动方向,显示的方向
    private int SliderDir = SliderDir_LeftToRight; // 滑动方向，默认从左向右
    /**
     * 滑出菜单的范围，指定固定范围。如：固定的下滑菜单与上滑菜单
     */
    private Rect DownZone_Hide_Screen = null; // 隐藏状态下的触发区域（固定范围）
    /**
     * 滑出菜单的范围，根据按钮大小动态改变。如：通道菜单的通道状态变化后，按范围大小生成
     */
    private ISliderZone sliderZoneFromBtn = null; // 动态区域回调接口

    /**
     * 不通过ISliderZone.getAvailableSliderRect()获取可划出范围，
     * 直接设置范围值
     */
    private Rect Slider_Zone_Rect = null; // 直接设置的可滑出范围矩形

    /*
      显示的有效区域可以通过一些计处得出来。
      显示位置+滑动方向+ViewGroup的大小=显示在屏幕上的区域
     */
    private Rect DownZone_Show_Sreen = null; // 显示状态下的屏幕区域

    private Rect ShowLayout_Zone = null; // 显示时的布局区域
    private Rect HideLayout_Zone = null; // 隐藏时的布局区域

    /*
    显示菜单的起始位置。
                 */
    private int ShowMenu_BeginPosion; // 显示菜单的起始位置坐标
    /*
    菜单类
     */
    private View SliderViewGroup; // 滑动菜单的视图容器

    /*
        操作是否做用到这个菜单
     */
    private boolean Enable; // 是否启用该菜单
    /*
        当前的显示状态 true 显示，false不显示
    */
    private boolean CurrShowState; // 当前显示状态标志

    /**
     * 允许滑动
     */
    private boolean EnableSlip; // 是否允许滑动操作

    /** 获取是否允许滑动 */
    public boolean isEnableSlip() { // 查询是否允许滑动
        return EnableSlip; // 返回允许滑动标志
    }

    /**
     * 设置是否允许滑动
     *
     * @param enableSlip 是否允许滑动
     */
    public void setEnableSlip(boolean enableSlip) { // 设置是否允许滑动
        EnableSlip = enableSlip; // 赋值允许滑动标志
    }

    /** 获取滑动方向 */
    public int getSliderDir() { // 查询滑动方向
        return SliderDir; // 返回滑动方向
    }

    /** 设置滑动方向 */
    public void setSliderDir(int sliderDir) { // 设置滑动方向
        SliderDir = sliderDir; // 赋值滑动方向
    }

    /** 获取当前显示状态 */
    public boolean isCurrShowState() { // 查询当前显示状态
        return CurrShowState; // 返回当前显示状态
    }

    /** 设置当前显示状态 */
    public void setCurrShowState(boolean currShowState) { // 设置当前显示状态
        CurrShowState = currShowState; // 赋值当前显示状态
        if (currShowState){ // 如果设置为显示状态
            Tools.PrintControlsLocation(NameTag,(ViewGroup) SliderViewGroup); // 打印控件位置信息用于调试
        }
    }


    /** 获取显示菜单的起始位置 */
    public int getShowMenu_BeginPosion() { // 查询显示起始位置
        return ShowMenu_BeginPosion; // 返回显示起始位置
    }

    /** 设置显示菜单的起始位置 */
    public void setShowMenu_BeginPosion(int showMenu_BeginPosion) { // 设置显示起始位置
        ShowMenu_BeginPosion = showMenu_BeginPosion; // 赋值显示起始位置
    }

    /** 获取是否启用 */
    public boolean isEnable() { // 查询是否启用
        return Enable; // 返回启用标志
    }

    /** 设置是否启用 */
    public void setEnable(boolean enable) { // 设置是否启用
        Enable = enable; // 赋值启用标志
    }


    /** 获取滑动菜单视图容器 */
    public View getSliderViewGroup() { // 查询滑动菜单视图
        return SliderViewGroup; // 返回滑动菜单视图
    }

    /** 设置滑动菜单视图容器 */
    public void setSliderViewGroup(View sliderViewGroup) { // 设置滑动菜单视图
        SliderViewGroup = sliderViewGroup; // 赋值滑动菜单视图
    }

    /** 获取显示时的布局区域 */
    public Rect getShowLayout_Zone() { // 查询显示布局区域
        return ShowLayout_Zone; // 返回显示布局区域矩形
    }

    /** 设置显示时的布局区域 */
    public void setShowLayout_Zone(Rect showLayout_Zone) { // 设置显示布局区域
        ShowLayout_Zone = showLayout_Zone; // 赋值显示布局区域矩形
    }

    /** 获取隐藏时的布局区域 */
    public Rect getHideLayout_Zone() { // 查询隐藏布局区域
        return HideLayout_Zone; // 返回隐藏布局区域矩形
    }

    /** 设置隐藏时的布局区域 */
    public void setHideLayout_Zone(Rect hideLayout_Zone) { // 设置隐藏布局区域
        HideLayout_Zone = hideLayout_Zone; // 赋值隐藏布局区域矩形
    }

    /** 设置动态区域回调接口 */
    public void setSliderZoneFromBtn(ISliderZone sliderZoneFromBtn){ // 设置动态区域回调
        this.sliderZoneFromBtn=sliderZoneFromBtn; // 赋值动态区域回调接口
    }

    /** 获取直接设置的可滑出范围 */
    public Rect getSlider_Zone_Rect() { // 查询直接设置的可滑出范围
        return Slider_Zone_Rect; // 返回可滑出范围矩形
    }

    /** 设置直接设置的可滑出范围 */
    public void setSlider_Zone_Rect(Rect slider_Zone_Rect) { // 设置可滑出范围
        Slider_Zone_Rect = slider_Zone_Rect; // 赋值可滑出范围矩形
    }

    //endregion

    /**
     * 通过一些计算得出来的参数，所有参数都设置完成后，最后调用。
     * 根据滑动方向计算显示区域和隐藏区域的矩形范围。
     * BUG:从右到左的菜单显示会偏差2个像素，原因没有找到.以下补上两个像素
     *
     * @param update 是否强制更新，true则重新计算，false则仅首次计算
     */
    public void createParam(boolean update) { // 创建参数，计算显示/隐藏区域
        if (HideLayout_Zone != null && !update) return; // 若已计算且不强制更新，直接返回


        //生成显示时的有效区域
        int left, top, width, height; // 声明矩形四要素变量
        ViewGroup parentViewGroup = (ViewGroup) this.SliderViewGroup.getParent(); // 获取父容器视图
        switch (this.getSliderDir()) { // 根据滑动方向分支计算
            case SliderDir_BottomToTop: { // 从下向上滑出
                left = parentViewGroup.getLeft(); // 左边界取父容器左边界
                width = this.SliderViewGroup.getWidth(); // 宽度取菜单视图宽度
                top = 0; // 上边界取屏幕顶部
                height = this.SliderViewGroup.getHeight(); // 高度取菜单视图高度
                DownZone_Show_Sreen = new Rect(left, top, left + width, height + top); // 构建显示区域矩形
            }
            break; // 跳出case分支
            case SliderDir_TopToBottom: { // 从上向下滑出
                left = parentViewGroup.getLeft(); // 左边界取父容器左边界
                width = this.SliderViewGroup.getWidth(); // 宽度取菜单视图宽度
                top = this.getShowMenu_BeginPosion(); // 上边界取显示起始位置
                height = this.SliderViewGroup.getHeight(); // 高度取菜单视图高度
                DownZone_Show_Sreen = new Rect(left, top, left + width, height + top); // 构建显示区域矩形
            }
            break; // 跳出case分支
            case SliderDir_LeftToRight: { // 从左向右滑出
                left = getShowMenu_BeginPosion(); // 左边界取显示起始位置
                width = this.SliderViewGroup.getWidth(); // 宽度取菜单视图宽度
                top = parentViewGroup.getTop(); // 上边界取父容器上边界
                height = this.SliderViewGroup.getHeight(); // 高度取菜单视图高度
                DownZone_Show_Sreen = new Rect(left, top, left + width, height + top); // 构建显示区域矩形
            }
            break; // 跳出case分支
            case SliderDir_RightToLeft: { // 从右向左滑出
                left = getShowMenu_BeginPosion() - this.SliderViewGroup.getWidth(); // 左边界=起始位置-菜单宽度
                width = this.SliderViewGroup.getWidth(); // 宽度取菜单视图宽度
                top = parentViewGroup.getTop(); // 上边界取父容器上边界
                height = this.SliderViewGroup.getHeight(); // 高度取菜单视图高度
                DownZone_Show_Sreen = new Rect(left, top, left + width, height + top); // 构建显示区域矩形
            }
            break; // 跳出case分支
            default: // 默认分支（无方向）
                DownZone_Show_Sreen = null; // 显示区域置空
                break; // 跳出default分支
        }

        // 生成显示RECT
        switch (this.SliderDir) { // 根据滑动方向计算显示布局区域
            case SliderDir_LeftToRight: { // 从左向右滑出
                left = SliderViewGroup.getLeft() + SliderViewGroup.getWidth(); // 左边界=视图左边界+宽度
                top = this.SliderViewGroup.getTop(); // 上边界取视图上边界
                width = SliderViewGroup.getWidth(); // 宽度取视图宽度
                height = SliderViewGroup.getHeight(); // 高度取视图高度
            }
            break; // 跳出case分支
            case SliderDir_RightToLeft: { // 从右向左滑出
                left = SliderViewGroup.getLeft() - SliderViewGroup.getWidth(); // 左边界=视图左边界-宽度
                top = this.SliderViewGroup.getTop(); // 上边界取视图上边界
                width = SliderViewGroup.getWidth(); // 宽度取视图宽度
                height = SliderViewGroup.getHeight(); // 高度取视图高度
            }
            break; // 跳出case分支
            case SliderDir_TopToBottom: { // 从上向下滑出
                left = this.SliderViewGroup.getLeft(); // 左边界取视图左边界
                top = SliderViewGroup.getHeight() + SliderViewGroup.getTop(); // 上边界=视图高度+视图上边界
                width = SliderViewGroup.getWidth(); // 宽度取视图宽度
                height = SliderViewGroup.getHeight(); // 高度取视图高度
            }
            break; // 跳出case分支
            case SliderDir_BottomToTop: { // 从下向上滑出
                left = this.SliderViewGroup.getLeft(); // 左边界取视图左边界
                top = SliderViewGroup.getHeight() - SliderViewGroup.getTop(); // 上边界=视图高度-视图上边界
                width = SliderViewGroup.getWidth(); // 宽度取视图宽度
                height = SliderViewGroup.getHeight(); // 高度取视图高度
            }
            break; // 跳出case分支
            default: // 默认分支（无方向）
                left = 0; // 左边界置0
                top = 0; // 上边界置0
                width = 0; // 宽度置0
                height = 0; // 高度置0
                break; // 跳出default分支
        }
        ShowLayout_Zone = new Rect(left, top, left + width, top + height); // 构建显示布局区域矩形
        //生成隐藏RECT
        left = this.SliderViewGroup.getLeft(); // 左边界取视图左边界
        top = this.SliderViewGroup.getTop(); // 上边界取视图上边界
        width = this.SliderViewGroup.getWidth(); // 宽度取视图宽度
        height = this.SliderViewGroup.getHeight(); // 高度取视图高度
        HideLayout_Zone = new Rect(left, top, left + width, top + height); // 构建隐藏布局区域矩形

    }

    /**
     * 根据触摸起点和终点坐标计算滑动方向。
     * 通过比较X/Y方向位移距离判定滑动方向。
     *
     * @param oldX 起点X坐标
     * @param oldY 起点Y坐标
     * @param newX 终点X坐标
     * @param newY 终点Y坐标
     * @return 滑动方向常量（SliderDir_LeftToRight/RightToLeft/TopToBottom/BottomToTop/None）
     */
    public static int getSlipDirection(int oldX, int oldY, int newX, int newY) { // 计算滑动方向
        int slipDir; // 声明滑动方向变量
        if (Math.abs(oldY - newY) < SlipDirectionSize_Y) { // 若Y方向位移小于阈值，判定为水平滑动
            //说明是左右滑动
            if (oldX - newX > 0 && Math.abs(oldX - newX) > SlipDirectionSize_X) { // X方向左移且超过阈值
                slipDir = SliderDir_RightToLeft; // 滑动方向为从右向左
            } else if (oldX - newX < 0 && Math.abs(oldX - newX) > SlipDirectionSize_X) { // X方向右移且超过阈值
                slipDir = SliderDir_LeftToRight; // 滑动方向为从左向右
            } else { // X方向位移未超过阈值
                slipDir = SliderDir_None; // 无有效滑动方向
            }
        } else { // Y方向位移超过阈值，判定为垂直滑动
            //说明是上下滑动
            if (oldY - newY > 0 && Math.abs(oldY - newY) > SlipDirectionSize_Y) { // Y方向上移且超过阈值
                slipDir = SliderDir_BottomToTop; // 滑动方向为从下向上
            } else if (oldY - newY < 0 && Math.abs(oldY - newY) > SlipDirectionSize_Y) { // Y方向下移且超过阈值
                slipDir = SliderDir_TopToBottom; // 滑动方向为从上向下
            } else { // Y方向位移未超过阈值
                slipDir = SliderDir_None; // 无有效滑动方向
            }
        }

        return slipDir; // 返回计算出的滑动方向
    }

    private Rect enableSlipFalseRect = new Rect(-1, -1, -1, -1); // 禁止滑动时的无效区域矩形

    /**
     * 获取隐藏状态下的触发区域。
     * 优先级：动态回调 > 直接设置矩形 > 固定区域 > 禁止滑动区域
     *
     * @return 触发区域矩形，若禁止滑动则返回无效区域（-1,-1,-1,-1）
     */
    public Rect getDownZone_Hide_Screen() { // 查询隐藏状态触发区域
        if (EnableSlip) { // 若允许滑动
            if (sliderZoneFromBtn != null) { // 若设置了动态回调
                return sliderZoneFromBtn.getAvailableSliderRect(); // 返回动态计算的区域
            } else if (Slider_Zone_Rect != null) { // 若直接设置了区域矩形
                return Slider_Zone_Rect; // 返回直接设置的区域
            } else { // 其他情况
                return DownZone_Hide_Screen; // 返回固定区域
            }
        } else return enableSlipFalseRect; // 若禁止滑动，返回无效区域
    }

    /** 设置隐藏状态下的触发区域（固定范围） */
    public void setDownZone_Hide_Screen(Rect downZone_Hide_Screen) { // 设置隐藏触发区域
        DownZone_Hide_Screen = downZone_Hide_Screen; // 赋值隐藏触发区域矩形
    }

    /** 获取显示状态下的屏幕区域 */
    public Rect getDownZone_Show_Sreen() { // 查询显示状态屏幕区域
        return DownZone_Show_Sreen; // 返回显示状态屏幕区域矩形
    }

    /** 设置显示状态下的屏幕区域 */
    public void setDownZone_Show_Sreen(Rect downZone_Show_Sreen) { // 设置显示状态屏幕区域
        DownZone_Show_Sreen = downZone_Show_Sreen; // 赋值显示状态屏幕区域矩形
    }

    /**
     * 在菜单中的控件，滑动后取消操作。
     * 遍历菜单容器内的子控件，找到触摸点所在的控件并发送CANCEL事件。
     *
     * @param event 触摸事件对象
     * @param view 菜单容器视图
     */
    public static void cancelViewMotion_InView(MotionEvent event, View view) { // 取消菜单内控件触摸操作
        View targetView = null; // 声明目标控件变量
        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) { // 遍历容器内所有子控件
            View tem = ((ViewGroup) view).getChildAt(i); // 获取当前子控件
            Rect rect = new Rect(); // 创建矩形对象
            tem.getGlobalVisibleRect(rect); // 获取子控件在屏幕上的可见矩形区域
            if (rect.contains((int) event.getRawX(), (int) event.getRawY())) { // 若触摸点在子控件区域内
                targetView = tem; // 记录目标控件
                break; // 找到后跳出循环
            }

        }
        if (targetView == null) return; // 若未找到目标控件，直接返回
        event.setAction(MotionEvent.ACTION_CANCEL); // 将触摸事件改为CANCEL动作
        targetView.dispatchTouchEvent(event); // 向目标控件分发CANCEL事件
    }

    /**
     * 与菜单平行的控件，滑动后取消操作，目前只有下菜单,且只有滑出时。
     * 查找底部栏控件并取消其触摸操作。
     *
     * @param event 触摸事件对象
     * @param mainViewGroup 主容器视图
     */
    public void cancelViewMotion_OutView(MotionEvent event, View mainViewGroup) { // 取消菜单外平行控件触摸操作
        View view = mainViewGroup.findViewById(R.id.mBottomBar); // 查找底部栏控件
        cancelViewMotion_InView(event, view); // 调用方法取消底部栏控件的触摸操作
    }
}