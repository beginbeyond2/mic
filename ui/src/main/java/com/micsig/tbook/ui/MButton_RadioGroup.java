package com.micsig.tbook.ui; // UI组件库根包，包含示波器自定义UI控件

import android.content.Context; // Android上下文对象，用于获取资源和系统服务
import android.content.res.TypedArray; // 类型化数组，用于读取XML属性
import android.util.AttributeSet; // 属性集，用于XML属性解析
import android.view.View; // Android视图基类
import android.view.ViewGroup; // Android视图组基类
import android.widget.LinearLayout; // 线性布局，用于加载布局资源

import java.util.ArrayList; // 动态数组，用于存储复选框列表
import java.util.List; // 列表接口

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                    MButton_RadioGroup - 自定义单选按钮组控件                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                  ║
 * ║   UI组件库 > 自定义控件 > 单选按钮组组件                                        ║
 * ║   MHO系列示波器软件核心UI控件之一                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【核心职责】                                                                  ║
 * ║   1. 提供基于MButton_CheckBox的单选按钮组功能                                   ║
 * ║   2. 确保同一组内只有一个按钮被选中                                             ║
 * ║   3. 支持从XML布局资源加载子视图                                               ║
 * ║   4. 自动管理按钮的选中状态互斥                                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【架构设计】                                                                  ║
 * ║   继承关系: MButton_RadioGroup extends ViewGroup                              ║
 * ║   设计模式: 组合模式，将多个MButton_CheckBox组合成单选组                        ║
 * ║   状态管理: 通过list维护所有复选框，实现互斥选中                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【数据流向】                                                                  ║
 * ║   XML布局 → inflate加载 → init遍历子视图 → 收集MButton_CheckBox → 点击互斥处理  ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【依赖关系】                                                                  ║
 * ║   内部依赖: MButton_CheckBox                                                  ║
 * ║   外部依赖: Android SDK (ViewGroup, Context等)                                ║
 * ║   资源依赖: R.styleable.MButton_RadioGroup (自定义属性集)                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【使用示例】                                                                  ║
 * ║   XML布局:                                                                   ║
 * ║   <com.micsig.tbook.ui.MButton_RadioGroup                                    ║
 * ║       android:layout_width="match_parent"                                     ║
 * ║       android:layout_height="wrap_content"                                    ║
 * ║       app:uiLayout="@layout/radio_group_layout" />                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【注意事项】                                                                  ║
 * ║   1. 子布局中必须包含MButton_CheckBox控件                                      ║
 * ║   2. 点击已选中的按钮不会取消选中                                              ║
 * ║   3. 自动实现单选互斥逻辑                                                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * @author liwb
 * @date 2017/4/11
 * @version 1.0
 */

public class MButton_RadioGroup extends ViewGroup {

    // ================================ 成员变量定义 ================================
    
    private View view; // 加载的布局视图根节点
    private List<MButton_CheckBox> list = new ArrayList<MButton_CheckBox>(); // 存储所有复选框按钮的列表

    //region 属性
    private OnClickListener onClickListener = null; // 点击事件监听器

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setOnClickListener
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置点击事件监听器
     * 
     * 【参数说明】
     *   @param onClickListener 点击事件监听器接口
     */
    @Override
    public void setOnClickListener(OnClickListener onClickListener) { // 设置点击监听器
        this.onClickListener = onClickListener; // 保存监听器引用
    }

    //endregion
    
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
    public MButton_RadioGroup(Context context) { // 构造方法：仅传入Context
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
    public MButton_RadioGroup(Context context, AttributeSet attrs) { // 构造方法：传入Context和属性集
        this(context, attrs, 0); // 调用三参数构造方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：三参数版本（主构造方法）
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   完整的构造方法，完成属性解析和初始化
     *   从XML属性中读取布局资源ID并加载
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集
     *   @param defStyleAttr 默认样式属性
     * 
     * 【处理流程】
     *   1. 调用父类构造方法
     *   2. 解析uiLayout属性获取布局资源ID
     *   3. 加载布局资源
     *   4. 初始化子视图，收集MButton_CheckBox
     */
    public MButton_RadioGroup(Context context, AttributeSet attrs, int defStyleAttr) { // 构造方法：完整参数版本
        super(context, attrs, defStyleAttr); // 调用父类ViewGroup的构造方法

        // 从主题中获取MButton_RadioGroup自定义属性集
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MButton_RadioGroup, defStyleAttr, 0); // 获取类型化属性数组
        int n = a.getIndexCount(); // 获取属性数量
        for (int i = 0; i < n; i++) { // 遍历所有属性
            int attr = a.getIndex(i); // 获取当前属性的索引
            if (attr == R.styleable.MButton_RadioGroup_uiLayout) { // 如果是布局资源属性

                int id = a.getResourceId(attr, -1); // 获取布局资源ID
                if (id == -1) throw new RuntimeException("资源没有被找到，请设置布局"); // 如果未设置布局资源，抛出异常
                view = inflate(context, id, this); // 加载布局资源到当前ViewGroup
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( // 创建布局参数
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT); // 设置为匹配父容器
                this.setLayoutParams(lp); // 应用布局参数
            }
        }

        init(this.view); // 初始化子视图，收集MButton_CheckBox
    }


    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：onLayout
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   布局子视图的位置
     *   使用子视图自身的位置进行布局
     * 
     * 【参数说明】
     *   @param changed 布局是否发生变化
     *   @param l 左边界
     *   @param t 上边界
     *   @param r 右边界
     *   @param b 下边界
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) { // 布局子视图
        // 记录总高度
        int mTotalHeight = 0; // 累计高度（未使用）
        // 遍历所有子视图
        int childCount = getChildCount(); // 获取子视图数量
        for (int i = 0; i < childCount; i++) { // 遍历每个子视图
            View childView = getChildAt(i); // 获取当前子视图

            // 获取在onMeasure中计算的视图尺寸
            int mLeft = childView.getLeft(); // 获取子视图左边位置
            int mTop = childView.getTop(); // 获取子视图顶部位置
            int measureHeight = childView.getMeasuredHeight(); // 获取子视图测量高度
            int measuredWidth = childView.getMeasuredWidth(); // 获取子视图测量宽度
//            childView.layout(l, mTotalHeight, measuredWidth, mTotalHeight
//                    + measureHeight); // 注释掉的备用布局代码
            childView.layout(mLeft, mTop, measuredWidth, measureHeight); // 使用子视图自身位置进行布局
            mTotalHeight += measureHeight; // 累加高度
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：onMeasure
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   测量视图和子视图的尺寸
     *   根据测量模式计算最终尺寸
     * 
     * 【参数说明】
     *   @param widthMeasureSpec 宽度测量规格
     *   @param heightMeasureSpec 高度测量规格
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { // 测量视图尺寸
        int width = MeasureSpec.getSize(widthMeasureSpec); // 获取宽度尺寸
        int height = MeasureSpec.getSize(heightMeasureSpec); // 获取高度尺寸
        int widthMode = MeasureSpec.getMode(widthMeasureSpec); // 获取宽度测量模式
        int heightMode = MeasureSpec.getMode(heightMeasureSpec); // 获取高度测量模式

        int childCount = getChildCount(); // 获取子视图数量
        measureChildren(widthMeasureSpec, heightMeasureSpec); // 测量所有子视图
        if (childCount == 0) { // 如果没有子视图
            setMeasuredDimension(0, 0); // 设置尺寸为0
        } else if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) { // 如果宽高都是AT_MOST模式
            width = childCount * getChildAt(0).getMeasuredWidth(); // 计算宽度为所有子视图宽度之和
            height = getChildAt(0).getMeasuredHeight(); // 高度为第一个子视图高度
            setMeasuredDimension(width, height); // 设置测量尺寸
        } else if (widthMode == MeasureSpec.AT_MOST) { // 如果只有宽度是AT_MOST模式
            width = childCount * getChildAt(0).getMeasuredWidth(); // 计算宽度
            setMeasuredDimension(width, height); // 设置测量尺寸
        } else { // 其他情况
            height = getChildAt(0).getMeasuredHeight(); // 使用第一个子视图高度
            setMeasuredDimension(width, height); // 设置测量尺寸
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec); // 调用父类测量方法
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：init
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   初始化方法，递归遍历视图树，收集所有MButton_CheckBox控件
     *   为每个复选框设置点击监听器
     * 
     * 【参数说明】
     *   @param v 要遍历的视图根节点
     * 
     * 【处理逻辑】
     *   1. 遍历视图树中的所有子视图
     *   2. 如果是MButton_CheckBox，添加到列表并设置监听器
     *   3. 如果是ViewGroup，递归遍历
     */
    //遍历按钮 到记录
    private void init(View v) { // 初始化方法，收集所有复选框
        ViewGroup viewGroup = (ViewGroup) v; // 将视图转换为ViewGroup
        int count = viewGroup.getChildCount(); // 获取子视图数量
        for (int i = 0; i < count; i++) { // 遍历所有子视图
            View view = viewGroup.getChildAt(i); // 获取当前子视图
            if (view instanceof MButton_CheckBox) { // 如果是MButton_CheckBox
                list.add((MButton_CheckBox) view); // 添加到列表
                ((MButton_CheckBox) view).setOnClickListener(new OnClickListener() { // 设置点击监听器
                    @Override
                    public void onClick(View v) { // 点击回调
                        dealOnClick(v); // 处理点击事件
                    }
                });
                continue; // 继续下一个子视图
            }
            if (view instanceof ViewGroup) { // 如果是ViewGroup
                init(view); // 递归遍历
            }
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：dealOnClick
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   处理复选框的点击事件，实现单选互斥逻辑
     *   确保同一组内只有一个按钮被选中
     * 
     * 【参数说明】
     *   @param v 被点击的视图
     * 
     * 【处理逻辑】
     *   1. 如果点击的是未选中的按钮，先选中它然后返回
     *   2. 如果点击的是已选中的按钮，取消其他所有按钮的选中状态
     *   3. 触发外部设置的点击监听器
     */
    private void dealOnClick(View v) { // 处理点击事件
        MButton_CheckBox check = (MButton_CheckBox) v; // 获取被点击的复选框
        if (!((MButton_CheckBox) v).isChecked()) { // 如果当前未选中
            check.setChecked(true); // 设置为选中状态
            return; // 直接返回，不触发外部监听器
        }
        for (MButton_CheckBox m : list) { // 遍历所有复选框
            if (m != v) { // 如果不是当前点击的复选框
                m.setChecked(false); // 取消选中
            }
        }
        if (onClickListener != null) onClickListener.onClick(v); // 触发外部监听器
    }
}
