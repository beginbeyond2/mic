package com.micsig.tbook.ui; // UI组件库根包，包含示波器自定义UI控件

import android.annotation.SuppressLint; // 抑制lint警告的注解
import android.content.Context; // Android上下文对象
import android.graphics.Rect; // 矩形区域，用于位置计算
import android.util.AttributeSet; // 属性集，用于XML属性解析
import android.view.View; // Android视图基类
import android.view.ViewGroup; // Android视图组基类
import android.widget.CheckBox; // 复选框控件
import android.widget.HorizontalScrollView; // 水平滚动视图
import android.widget.ImageView; // 图片视图
import android.widget.LinearLayout; // 线性布局
import android.widget.RadioButton; // 单选按钮
import android.widget.RadioGroup; // 单选按钮组
import android.widget.RelativeLayout; // 相对布局

import com.micsig.tbook.ui.top.view.channel.TopViewChannel; // 通道视图组件
import com.micsig.tbook.ui.top.view.channel.TopViewChannelMultipleChoice; // 多选通道视图组件

import java.util.List; // 列表接口

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   MMainMenuChannel - 主菜单通道选择控件                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                  ║
 * ║   UI组件库 > 自定义控件 > 菜单控件 > 通道选择                                   ║
 * ║   MHO系列示波器软件核心UI控件之一                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【核心职责】                                                                  ║
 * ║   1. 提供示波器通道选择功能，支持单选和多选模式                                  ║
 * ║   2. 管理通道的显示和隐藏状态                                                 ║
 * ║   3. 支持水平滚动浏览所有通道                                                 ║
 * ║   4. 提供左右导航箭头控制滚动                                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【架构设计】                                                                  ║
 * ║   继承关系: MMainMenuChannel extends RelativeLayout                           ║
 * ║   组合模式: 内部包含TopViewChannel(单选)和TopViewChannelMultipleChoice(多选)    ║
 * ║   状态切换: 通过setAllSelectShow方法切换单选/多选模式                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【数据流向】                                                                  ║
 * ║   XML布局 → initView初始化 → 设置数据 → 用户交互 → 回调通知                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【依赖关系】                                                                  ║
 * ║   内部依赖: TopViewChannel, TopViewChannelMultipleChoice                      ║
 * ║   外部依赖: Android SDK (RelativeLayout, ScrollView等)                        ║
 * ║   资源依赖: R.layout.view_main_menu_channel                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【使用示例】                                                                  ║
 * ║   MMainMenuChannel channelMenu = findViewById(R.id.channel_menu);             ║
 * ║   channelMenu.setData(R.array.channels, R.array.channel_colors);              ║
 * ║   channelMenu.setChangeListener(channelListener, multiSelectListener);        ║
 * ║   // 切换到多选模式                                                           ║
 * ║   channelMenu.setAllSelectShow(true);                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【注意事项】                                                                  ║
 * ║   1. 通道数量超过显示范围时自动显示左右导航箭头                                 ║
 * ║   2. 单选和多选模式互斥，只能显示其中一种                                       ║
 * ║   3. 通道颜色通过setChannelColor方法动态设置                                   ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * @author Liwb
 * @date 2024-2-26 16:59
 * @version 1.0
 */

public class MMainMenuChannel extends RelativeLayout {
    // ================================ 成员变量定义 ================================
    
    private Context context; // Android上下文对象引用
    
    // 通道视图组件
    private TopViewChannel viewChannel; // 单选通道视图
    private TopViewChannelMultipleChoice viewChannelMultipleChoice; // 多选通道视图
    
    // 滚动和导航组件
    private HorizontalScrollView scrollView; // 水平滚动视图
    private ImageView imgLeft, imgRight; // 左右导航箭头图片
    private LinearLayout llInScrollView; // 滚动视图内部的线性布局容器
    
    // 监听器
    public TopViewChannel.onItemClickListener onItemClickListener = null; // 单选点击监听器
    private TopViewChannelMultipleChoice.onTestListener onItemTestListener = null; // 多选点击监听器

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：单参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   仅传入Context的构造方法，用于代码中动态创建实例
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     */
    public MMainMenuChannel(Context context) { // 构造方法：仅传入Context
        this(context, null); // 调用双参数构造方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：双参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   传入Context和AttributeSet的构造方法，用于XML布局文件中创建实例
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集
     */
    public MMainMenuChannel(Context context, AttributeSet attrs) { // 构造方法：传入Context和属性集
        this(context, attrs, 0); // 调用三参数构造方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：三参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   传入Context、AttributeSet和默认样式的构造方法
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集
     *   @param defStyleAttr 默认样式属性
     */
    public MMainMenuChannel(Context context, AttributeSet attrs, int defStyleAttr) { // 构造方法：三参数版本
        this(context, attrs, defStyleAttr, 0); // 调用四参数构造方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：四参数版本（主构造方法）
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   完整的构造方法，完成初始化
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集
     *   @param defStyleAttr 默认样式属性
     *   @param defStyleRes 默认样式资源
     */
    public MMainMenuChannel(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) { // 构造方法：完整参数版本
        super(context, attrs, defStyleAttr, defStyleRes); // 调用父类构造方法
        this.context = context; // 保存上下文引用
        initView(); // 初始化视图
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：initView
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   初始化视图组件，加载布局并设置监听器
     *   创建单选和多选通道视图，并添加到滚动容器中
     * 
     * 【处理流程】
     *   1. 加载布局资源
     *   2. 获取视图引用
     *   3. 创建并配置通道视图
     *   4. 设置导航箭头监听器
     */
    private void initView() { // 初始化视图
        ViewGroup root = (ViewGroup) View.inflate(context, R.layout.view_main_menu_channel, this); // 加载布局资源
        imgLeft = root.findViewById(R.id.menu_channel_left); // 获取左箭头图片
        imgRight = root.findViewById(R.id.menu_channel_right); // 获取右箭头图片
        scrollView = root.findViewById(R.id.main_menu_channel_scroll_view); // 获取滚动视图
        
        llInScrollView = new LinearLayout(context); // 创建线性布局容器
        llInScrollView.setOrientation(LinearLayout.HORIZONTAL); // 设置水平方向
        
        // 创建单选通道视图
        viewChannel = new TopViewChannel(context); // 实例化单选通道视图
        viewChannel.setData(R.array.popArrayAllChannelForSaveWave, R.array.popArrayAllChanColorForSaveWave, onChannelItemClickListener); // 设置默认数据
        
        // 创建多选通道视图
        viewChannelMultipleChoice = new TopViewChannelMultipleChoice(context); // 实例化多选通道视图
        viewChannelMultipleChoice.setData(R.array.popArrayAllChannelForSaveWave, R.array.popArrayAllChanColorForSaveWave, onTestListener); // 设置默认数据

        // 添加视图到容器
        llInScrollView.addView(viewChannelMultipleChoice.getInflate()); // 添加多选视图
        llInScrollView.addView(viewChannel.getInflate()); // 添加单选视图
        llInScrollView.getChildAt(0).setVisibility(View.GONE); // 默认隐藏多选视图

        scrollView.addView(llInScrollView); // 将容器添加到滚动视图

        // 设置导航箭头点击监听器
        imgLeft.setOnClickListener(onClickListener); // 设置左箭头监听器
        imgRight.setOnClickListener(onClickListener); // 设置右箭头监听器
    }
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setData
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置通道数据源
     * 
     * 【参数说明】
     *   @param arrayResId 通道名称数组资源ID
     *   @param arrayColorResId 通道颜色数组资源ID
     */
    public void setData(int arrayResId, int arrayColorResId) { // 设置通道数据
        viewChannel.setData(arrayResId, arrayColorResId, onChannelItemClickListener); // 更新单选视图数据
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setAllSelectShow
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置是否显示多选模式
     *   切换单选和多选模式的显示状态
     * 
     * 【参数说明】
     *   @param isShowMuti true=显示多选模式，false=显示单选模式
     */
    public void setAllSelectShow(boolean isShowMuti) { // 设置多选模式显示
        llInScrollView.getChildAt(0).setVisibility(isShowMuti ? View.VISIBLE : View.GONE); // 设置多选视图可见性
        llInScrollView.getChildAt(1).setVisibility(isShowMuti ? View.GONE : View.VISIBLE); // 设置单选视图可见性
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 内部类：单选通道点击监听器
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   处理单选通道的点击事件
     *   自动滚动到选中项，并通知外部监听器
     */
    private TopViewChannel.onItemClickListener onChannelItemClickListener = (viewId, checkedIndex, radioButton) -> { // 单选点击监听器
        getOffsetXShowToParent(radioButton); // 滚动到选中项
        if (onItemClickListener != null) { // 如果设置了外部监听器
            onItemClickListener.checkChanged(viewId, checkedIndex, radioButton); // 触发回调
        }
    };

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 内部类：多选通道点击监听器
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   处理多选通道的点击事件
     *   通知外部监听器选中状态变化
     */
    private TopViewChannelMultipleChoice.onTestListener onTestListener = checkBox -> { // 多选点击监听器
        if (onItemTestListener != null) { // 如果设置了外部监听器
            onItemTestListener.onTest(checkBox); // 触发回调
        }
    };

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setChangeListener
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置通道选择变化监听器
     * 
     * 【参数说明】
     *   @param changeListener 单选变化监听器
     *   @param testListener 多选变化监听器
     */
    public void setChangeListener(TopViewChannel.onItemClickListener changeListener, TopViewChannelMultipleChoice.onTestListener testListener) { // 设置变化监听器
        this.onItemClickListener = changeListener; // 保存单选监听器
        this.onItemTestListener = testListener; // 保存多选监听器
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setChecked
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置选中的通道索引
     * 
     * 【参数说明】
     *   @param checkedIndex 要选中的通道索引
     */
    public void setChecked(int checkedIndex) { // 设置选中项
        viewChannel.setChecked(checkedIndex); // 调用单选视图的设置方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setItemVisible
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置通道项的可见性
     *   同时更新导航箭头的显示状态
     * 
     * 【参数说明】
     *   @param visible 可见性数组，每个元素对应一个通道
     *   @param isForwardSelected 是否正向选择
     */
    public void setItemVisible(boolean[] visible, boolean isForwardSelected) { // 设置通道可见性
        viewChannel.setItemVisible(visible, isForwardSelected); // 更新单选视图可见性
        if (llInScrollView.getChildAt(0).getVisibility() == View.VISIBLE) { // 如果多选视图可见
            viewChannelMultipleChoice.setItemVisible(visible); // 更新多选视图可见性
        }
        showLeftRightImg(visible); // 更新导航箭头显示
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setChannelColor
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置指定通道的颜色
     * 
     * 【参数说明】
     *   @param chIndex 通道索引
     *   @param colorStr 颜色字符串
     */
    public void setChannelColor(int chIndex, String colorStr) { // 设置通道颜色
        viewChannel.setChannelColor(chIndex, colorStr); // 更新单选视图颜色
        if (llInScrollView.getChildAt(0).getVisibility() == View.VISIBLE) { // 如果多选视图可见
            viewChannelMultipleChoice.setChannelColor(chIndex, colorStr); // 更新多选视图颜色
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getSelectChannel
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取当前选中的通道索引（单选模式）
     * 
     * 【返回值】
     *   @return 选中的通道索引
     */
    public int getSelectChannel() { // 获取选中通道
        return viewChannel.getSelectChannel(); // 返回单选视图的选中项
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getAllSelectChannel
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取所有选中的通道索引列表（多选模式）
     * 
     * 【返回值】
     *   @return 选中的通道索引列表
     */
    public List<Integer> getAllSelectChannel() { // 获取所有选中通道
        return viewChannelMultipleChoice.getSelectChannel(); // 返回多选视图的选中项列表
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getSelectedRadioButton
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取当前选中的RadioButton控件
     * 
     * 【返回值】
     *   @return 选中的RadioButton实例
     */
    public RadioButton getSelectedRadioButton() { // 获取选中的RadioButton
        return viewChannel.getSelectedRadioButton(); // 返回单选视图的选中按钮
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getSelectedIndex
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取当前选中的索引
     * 
     * 【返回值】
     *   @return 选中的索引值
     */
    public int getSelectedIndex() { // 获取选中索引
        return viewChannel.getSelectedIndex(); // 返回单选视图的选中索引
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getRadioGroup
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取单选按钮组控件
     * 
     * 【返回值】
     *   @return RadioGroup实例
     */
    public RadioGroup getRadioGroup() { // 获取RadioGroup
        return viewChannel.getRadioGroup(); // 返回单选视图的RadioGroup
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getCheckBoxs
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取复选框容器
     * 
     * 【返回值】
     *   @return 包含CheckBox的LinearLayout实例
     */
    public LinearLayout getCheckBoxs() { // 获取CheckBox容器
        return viewChannelMultipleChoice.getCheckBoxs(); // 返回多选视图的CheckBox容器
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getViewChannel
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取单选通道视图实例
     * 
     * 【返回值】
     *   @return TopViewChannel实例
     */
    public TopViewChannel getViewChannel() { // 获取单选通道视图
        return viewChannel; // 返回单选通道视图
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getViewChannelMultipleChoice
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取多选通道视图实例
     * 
     * 【返回值】
     *   @return TopViewChannelMultipleChoice实例
     */
    public TopViewChannelMultipleChoice getViewChannelMultipleChoice() { // 获取多选通道视图
        return viewChannelMultipleChoice; // 返回多选通道视图
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：moveOnlyScroll
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   移动滚动视图到指定通道位置
     *   确保指定的通道项在可视区域内
     * 
     * 【参数说明】
     *   @param curIndex 目标通道索引
     */
    public void moveOnlyScroll(int curIndex) { // 移动滚动到指定位置
        if (viewChannel.getInflate().getVisibility() == View.VISIBLE) { // 如果单选视图可见
            RadioButton radioButton = viewChannel.getShowViewIndex(curIndex); // 获取对应的RadioButton
            if (radioButton != null) { // 如果RadioButton存在
                getOffsetXShowToParent(radioButton); // 滚动到该位置
            }
        }
        if (viewChannelMultipleChoice.getInflate().getVisibility() == View.VISIBLE) { // 如果多选视图可见
            CheckBox checkBox = viewChannelMultipleChoice.getShowViewIndex(curIndex); // 获取对应的CheckBox
            if (checkBox != null) { // 如果CheckBox存在
                getOffsetXShowToParent(checkBox); // 滚动到该位置
            }
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getOffsetXShowToParent
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   计算并执行滚动偏移，确保指定视图在可视区域内
     *   根据视图在滚动区域中的位置自动调整滚动偏移
     * 
     * 【参数说明】
     *   @param radioButton 需要显示在可视区域的视图
     * 
     * 【返回值】
     *   @return 偏移值（当前未使用，返回0）
     */
    private int getOffsetXShowToParent(View radioButton) { // 计算滚动偏移
        int scrollWidth = scrollView.getWidth(); // 获取滚动视图宽度
        Rect outRect = new Rect(); // 创建输出矩形
        Rect srcRect = new Rect(0, 0, radioButton.getWidth(), radioButton.getHeight()); // 创建源矩形
        radioButton.getLocalVisibleRect(outRect); // 获取视图在滚动区域中的可见矩形
        
        // 判断视图是否完全可见
        if (outRect.left == srcRect.left && outRect.top == srcRect.top
                && outRect.width() == srcRect.width() && outRect.height() == srcRect.height()) { // 如果完全可见
            return 0; // 不需要滚动
        } else if (outRect.left < 0) { // 如果左边完全不显示
            scrollView.scrollBy(outRect.left, 0); // 向左滚动
        } else if (outRect.right > scrollWidth) { // 如果右边完全不显示
            int offsetX = outRect.right - scrollWidth; // 计算偏移量
            scrollView.scrollBy(offsetX, 0); // 向右滚动
        } else if (outRect.left != 0) { // 如果左边显示不全
            scrollView.scrollBy(-outRect.left, 0); // 向左滚动
        } else if (outRect.width() != srcRect.width()) { // 如果右边显示不全
            int offsetX = srcRect.width() - outRect.width(); // 计算偏移量
            scrollView.scrollBy(offsetX, 0); // 向右滚动
        }
        return 0; // 返回0
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：showLeftRightImg
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   根据通道数量和可见性决定是否显示左右导航箭头
     *   当通道总宽度超过显示区域时显示箭头
     * 
     * 【参数说明】
     *   @param visible 通道可见性数组
     * 
     * 【实现说明】
     *   FixMe: 临时解决方案，后续需要修改
     *   根据通道类型计算总宽度，超过阈值则显示箭头
     */
    public void showLeftRightImg(boolean[] visible) { // 显示/隐藏左右箭头
        int finalLength = 0; // 初始化总宽度
        for (int i = 0; i < visible.length; i++) { // 遍历所有通道
            if (visible[i]) { // 如果通道可见
                if (i <= 7) { // Ch1--Ch8
                    finalLength += 127; // 模拟通道宽度
                } else if (i <= 15) { // Math1--Math8
                    finalLength += 112; // 数学通道宽度
                } else { // R1--R8
                    finalLength += 112; // 参考通道宽度
                }
            }
        }
        // 根据总宽度决定是否显示箭头
        if (finalLength > 1620) { // 如果超过阈值
            imgLeft.setVisibility(View.VISIBLE); // 显示左箭头
            imgRight.setVisibility(View.VISIBLE); // 显示右箭头
        } else { // 如果未超过阈值
            imgLeft.setVisibility(View.GONE); // 隐藏左箭头
            imgRight.setVisibility(View.GONE); // 隐藏右箭头
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 内部类：导航箭头点击监听器
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   处理左右导航箭头的点击事件
     *   实现平滑滚动效果
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 导航箭头点击监听器

        @Override
        public void onClick(View v) { // 点击回调
            int id = v.getId(); // 获取点击的视图ID
            if (id == R.id.menu_channel_left) { // 如果是左箭头
                scrollView.smoothScrollBy(-152, 0); // 向左平滑滚动
            } else if (id == R.id.menu_channel_right) { // 如果是右箭头
                scrollView.smoothScrollBy(152, 0); // 向右平滑滚动
            }
        }
    };

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setReadOnly
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置控件的只读状态
     *   只读状态下多选视图不可交互
     * 
     * 【参数说明】
     *   @param enabled true=启用交互，false=禁用交互
     */
    @SuppressLint("ResourceType") // 抑制资源类型警告
    public void setReadOnly(boolean enabled) { // 设置只读状态
        super.setEnabled(enabled); // 调用父类方法
        if (!enabled) { // 如果禁用
            viewChannelMultipleChoice.setReadOnly(false); // 设置多选视图为不可交互
        } else { // 如果启用
            viewChannelMultipleChoice.setReadOnly(true); // 设置多选视图为可交互
        }
    }

}
