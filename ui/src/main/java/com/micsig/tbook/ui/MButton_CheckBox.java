package com.micsig.tbook.ui; // UI组件库根包，包含示波器自定义UI控件

import android.content.Context; // Android上下文对象，用于获取资源和系统服务
import android.content.res.TypedArray; // 类型化数组，用于读取XML属性
import android.graphics.Bitmap; // 位图对象，用于存储复选框图片资源
import android.graphics.Canvas; // 画布对象，用于自定义绘制
import android.graphics.Rect; // 矩形区域，用于图片绘制区域计算
import android.graphics.drawable.BitmapDrawable; // 位图可绘制对象
import android.text.Layout; // 文本布局接口
import android.text.StaticLayout; // 静态文本布局，用于多行文本绘制
import android.text.TextPaint; // 文本画笔，用于绘制文本
import android.util.AttributeSet; // 属性集，用于XML属性解析
import android.view.MotionEvent; // 触摸事件，用于处理用户交互
import android.view.View; // Android视图基类

import androidx.annotation.Nullable; // 可空性注解

//import com.micsig.base.Logger; // 日志工具类（已注释）

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                         MButton_CheckBox - 自定义复选框控件                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                  ║
 * ║   UI组件库 > 自定义控件 > 复选框组件                                           ║
 * ║   MHO系列示波器软件核心UI控件之一                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【核心职责】                                                                  ║
 * ║   1. 提供支持选中/未选中两种状态图片切换的自定义复选框                           ║
 * ║   2. 支持自定义文本显示，选中和未选中状态可显示不同颜色                         ║
 * ║   3. 处理触摸事件，实现复选框状态切换和点击回调                                 ║
 * ║   4. 支持启用/禁用状态控制                                                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【架构设计】                                                                  ║
 * ║   继承关系: MButton_CheckBox extends View                                     ║
 * ║   设计模式: 自定义视图模式，通过onDraw自定义绘制                                ║
 * ║   状态管理: checked布尔值控制选中/未选中状态                                    ║
 * ║   扩展性: MButton_CheckBox_ThreeClick继承此类实现三击功能                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【数据流向】                                                                  ║
 * ║   XML属性 → TypedArray解析 → 成员变量存储 → onDraw绘制 → 触摸事件更新状态      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【依赖关系】                                                                  ║
 * ║   内部依赖: 无                                                                ║
 * ║   外部依赖: Android SDK (View, Canvas, Paint, Bitmap等)                       ║
 * ║   资源依赖: R.styleable.MButton_CheckBox (自定义属性集)                        ║
 * ║   子类依赖: MButton_CheckBox_ThreeClick                                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【使用示例】                                                                  ║
 * ║   XML布局:                                                                   ║
 * ║   <com.micsig.tbook.ui.MButton_CheckBox                                      ║
 * ║       android:layout_width="100dp"                                            ║
 * ║       android:layout_height="50dp"                                            ║
 * ║       app:checkedBitmap="@drawable/checkbox_checked"                          ║
 * ║       app:unCheckedBitmap="@drawable/checkbox_unchecked"                      ║
 * ║       app:checkedTextColor="#FF0000"                                          ║
 * ║       app:unCheckedTextColor="#FFFFFF"                                        ║
 * ║       android:text="启用" />                                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【注意事项】                                                                  ║
 * ║   1. 必须设置checkedBitmap和unCheckedBitmap属性                               ║
 * ║   2. 文本绘制使用StaticLayout支持多行文本                                      ║
 * ║   3. 可通过setChecked()方法程序化设置选中状态                                  ║
 * ║   4. 禁用状态下无法通过点击改变选中状态                                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * @author liwb
 * @date 2017/3/31
 * @version 1.0
 */

public class MButton_CheckBox extends View {
    // ================================ 常量定义 ================================
    
    private final static String TAG="MButton_CheckBox"; // 日志标签，用于调试输出
    
    // ================================ 成员变量定义 ================================
    
    private TextPaint textPaint = null; // 文本画笔对象，控制文本的颜色、大小、抗锯齿等属性
    StaticLayout layout = null; // 静态文本布局对象，用于多行文本的绘制和排版

    private Bitmap checkBitmap = null, unCheckBitmap = null; // 选中状态和未选中状态的位图
    String text = null; // 复选框上显示的文本内容
    int checkTextColor, unCheckTextColor; // 选中和未选中状态的文本颜色
    float textSize; // 文本大小（像素值）

    private boolean enable=true; // 控件是否可用，true=可点击，false=禁用
    protected boolean checked = false; // 复选框当前状态：true=选中，false=未选中
    private int text_x = -1, text_y = -1; // 文本绘制位置的X和Y坐标，-1表示自动居中
    private boolean textCenterX = false; // 文本是否水平居中显示的标志
    private OnClickListener onClickListener = null; // 点击事件监听器

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：单参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   仅传入Context的构造方法，用于代码中动态创建MButton_CheckBox实例
     * 
     * 【参数说明】
     *   @param context Android上下文对象，用于获取资源和系统服务
     * 
     * 【调用链】
     *   this(context, null) → 双参数构造方法 → 三参数构造方法
     */
    public MButton_CheckBox(Context context) { // 构造方法：仅传入Context
        this(context, null); // 调用双参数构造方法，attrs传null
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：双参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   传入Context和AttributeSet的构造方法，用于XML布局文件中创建MButton_CheckBox
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集，包含布局文件中定义的属性
     * 
     * 【调用链】
     *   this(context, attrs, 0) → 三参数构造方法
     */
    public MButton_CheckBox(Context context, @Nullable AttributeSet attrs) { // 构造方法：传入Context和属性集
        this(context, attrs, 0); // 调用三参数构造方法，defStyleAttr传0
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：三参数版本（主构造方法）
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   完整的构造方法，完成所有属性的解析和初始化
     *   从XML属性中读取复选框图片、文本内容、文本样式等配置
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集
     *   @param defStyleAttr 默认样式属性
     * 
     * 【处理流程】
     *   1. 调用父类构造方法
     *   2. 从TypedArray中解析自定义属性
     *   3. 初始化文本画笔
     *   4. 初始化文本布局
     */
    public MButton_CheckBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { // 构造方法：完整参数版本
        super(context, attrs, defStyleAttr); // 调用父类View的构造方法
        
        // 从主题中获取MButton_CheckBox自定义属性集
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MButton_CheckBox, defStyleAttr, 0); // 获取类型化属性数组
        int n = a.getIndexCount(); // 获取属性数量
        for (int i = 0; i < n; i++) { // 遍历所有属性
            int attr = a.getIndex(i); // 获取当前属性的索引
            if (attr == R.styleable.MButton_CheckBox_checkedBitmap) { // 如果是选中状态图片属性
                checkBitmap = ((BitmapDrawable) a.getDrawable(attr)).getBitmap(); // 获取选中状态位图
            } else if (attr == R.styleable.MButton_CheckBox_unCheckedBitmap) { // 如果是未选中状态图片属性
                unCheckBitmap = ((BitmapDrawable) a.getDrawable(attr)).getBitmap(); // 获取未选中状态位图
            } else if (attr == R.styleable.MButton_CheckBox_android_text) { // 如果是文本内容属性
                text = a.getString(attr); // 获取文本字符串
            } else if (attr == R.styleable.MButton_CheckBox_android_textColor) { // 如果是文本颜色属性（兼容标准属性）
                unCheckTextColor = a.getColor(attr, 0xFFFFFF); // 获取未选中状态文本颜色，默认白色
            } else if (attr == R.styleable.MButton_CheckBox_checkedTextColor) { // 如果是选中状态文本颜色属性
                checkTextColor = a.getColor(attr, 0xff000000); // 获取选中状态文本颜色，默认黑色
            } else if (attr == R.styleable.MButton_CheckBox_unCheckedTextColor) { // 如果是未选中状态文本颜色属性
                unCheckTextColor = a.getColor(attr, 0xFFFFFF); // 获取未选中状态文本颜色，默认白色
            } else if (attr == R.styleable.MButton_CheckBox_android_textSize) { // 如果是文本大小属性
                textSize = a.getDimension(attr, 16); // 获取文本大小，默认16像素
            } else if (attr == R.styleable.MButton_CheckBox_text_x) { // 如果是文本X坐标属性
                text_x = (int) a.getDimension(attr, -1); // 获取文本X坐标，-1表示自动计算
            } else if (attr == R.styleable.MButton_CheckBox_text_y) { // 如果是文本Y坐标属性
                text_y = (int) a.getDimension(attr, -1); // 获取文本Y坐标，-1表示自动计算
            } else if (attr == R.styleable.MButton_CheckBox_textCenterX) { // 如果是文本水平居中属性
                textCenterX = a.getBoolean(attr, false); // 获取是否居中标志，默认不居中
            }
        }
        a.recycle(); // 回收TypedArray资源，避免内存泄漏

        // 初始化文本画笔
        textPaint = new TextPaint(); // 创建文本画笔实例
        textPaint.setColor(unCheckTextColor); // 设置默认文本颜色为未选中状态颜色
        textPaint.setTextSize(textSize); // 设置文本大小
        textPaint.setAntiAlias(true); // 启用抗锯齿，使文本边缘更平滑

        initLayout(); // 初始化文本布局
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：initLayout
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   初始化静态文本布局对象，用于多行文本的绘制
     *   根据复选框宽度和文本内容创建StaticLayout
     *   自动计算文本居中位置
     * 
     * 【前置条件】
     *   unCheckBitmap和text不为null
     * 
     * 【处理逻辑】
     *   1. 根据选中状态设置文本颜色
     *   2. 创建StaticLayout对象
     *   3. 如果坐标为-1，自动计算居中位置
     */
    private void initLayout() { // 初始化文本布局
        if (unCheckBitmap != null && text != null) { // 检查位图和文本是否有效
            if (checked) { // 如果当前是选中状态
                textPaint.setColor(checkTextColor); // 设置选中状态文本颜色
            } else { // 如果当前是未选中状态
                textPaint.setColor(unCheckTextColor); // 设置未选中状态文本颜色
            }
            layout = new StaticLayout(text, textPaint, unCheckBitmap.getWidth(), // 创建静态布局
                    Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true); // 参数：文本、画笔、宽度、对齐方式、行倍数、行间距、包含padding
            if (text_x == -1) { // 如果X坐标未设置
                text_x = (unCheckBitmap.getWidth() - layout.getWidth()) / 2; // 自动计算水平居中位置

            }
            if (text_y == -1) { // 如果Y坐标未设置
                text_y = (unCheckBitmap.getHeight() - layout.getHeight()) / 2; // 自动计算垂直居中位置
            }
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：onMeasure
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   测量视图尺寸，重写父类方法
     *   直接使用传入的测量规格作为最终尺寸
     * 
     * 【参数说明】
     *   @param widthMeasureSpec 宽度测量规格
     *   @param heightMeasureSpec 高度测量规格
     * 
     * 【覆写说明】
     *   重写View.onMeasure方法，简化测量逻辑
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { // 测量视图尺寸
        super.onMeasure(widthMeasureSpec, heightMeasureSpec); // 调用父类测量方法
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec); // 设置测量后的尺寸
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：onDraw
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   绘制复选框视图，根据当前状态绘制对应的位图和文本
     *   核心绘制逻辑：绘制复选框图片 → 绘制文本
     * 
     * 【参数说明】
     *   @param canvas 画布对象，用于绑定绘制操作
     * 
     * 【绘制流程】
     *   1. 计算复选框图片的水平居中位置
     *   2. 根据checked状态选择绘制选中或未选中状态的位图
     *   3. 如果有文本，计算文本位置并绘制
     * 
     * 【文本居中处理】
     *   根据文本方向（从左到右或从右到左）计算居中位置
     */
    @Override
    protected void onDraw(Canvas canvas) { // 绘制视图
        if (checkBitmap != null || unCheckBitmap != null) { // 检查是否有有效的位图资源
            Rect des, src; // 声明源矩形和目标矩形
            int center=(this.getWidth()-checkBitmap.getWidth())/2; // 计算图片水平居中位置
            src=new Rect(0, 0, checkBitmap.getWidth(), checkBitmap.getHeight()); // 设置源矩形区域
            des = new Rect(center, 0, checkBitmap.getWidth()+center, checkBitmap.getHeight()); // 设置目标矩形区域（水平居中）

            if (checked) { // 如果当前是选中状态
                canvas.drawBitmap(checkBitmap, src, des, null); // 绘制选中状态位图
            } else { // 如果当前是未选中状态
                canvas.drawBitmap(unCheckBitmap, src, des, null); // 绘制未选中状态位图
            }


            if (text != null) { // 如果有文本内容
                initLayout(); // 初始化文本布局
                if (textCenterX) { // 如果需要文本水平居中
                    if (layout.getParagraphDirection(0) == Layout.DIR_RIGHT_TO_LEFT) { // 如果文本方向是从右到左
                        text_x = (getTextWidth(text) - getMeasuredWidth()) / 2; // 计算居中X坐标（RTL情况）
                    } else { // 如果文本方向是从左到右
                        text_x = (getMeasuredWidth() - getTextWidth(text)) / 2; // 计算居中X坐标（LTR情况）
                    }
                }
                canvas.save(); // 保存画布状态
                canvas.translate(text_x, text_y); // 平移画布到文本绘制位置
                layout.draw(canvas); // 绘制文本布局
                canvas.restore(); // 恢复画布状态
            }
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：onTouchEvent
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   处理触摸事件，实现复选框的交互逻辑
     *   包括点击检测和状态切换
     * 
     * 【参数说明】
     *   @param event 触摸事件对象，包含动作类型和坐标信息
     *   @return 始终返回true，表示消费了触摸事件
     * 
     * 【事件处理】
     *   ACTION_DOWN: 按下事件，不做处理
     *   ACTION_UP: 抬起事件，检测是否触发点击并切换状态
     *   ACTION_MOVE: 移动事件，不做处理
     * 
     * 【点击检测】
     *   检查抬起位置是否在复选框范围内，且控件处于启用状态
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) { // 处理触摸事件
        switch (event.getAction()) { // 根据动作类型分发处理
            case MotionEvent.ACTION_DOWN: // 按下事件
                break; // 不做处理
            case MotionEvent.ACTION_UP: // 抬起事件
                //处理点击事件
                if (getViewInScreen().contains((int) event.getRawX(), (int) event.getRawY()) && enable) { // 检查抬起位置是否在范围内且控件可用
                    onSingleClick(); // 处理单击事件
                    invalidate(); // 请求重绘
                }
                break; // 结束case
            case MotionEvent.ACTION_MOVE: // 移动事件
                break; // 不做处理
        }
        return true; // 返回true表示消费了事件
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：onSingleClick
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   处理单击事件，切换复选框状态并触发回调
     *   子类可重写此方法实现不同的点击行为（如三击功能）
     * 
     * 【处理逻辑】
     *   1. 切换checked状态
     *   2. 如果设置了点击监听器，触发回调
     * 
     * 【扩展说明】
     *   MButton_CheckBox_ThreeClick重写此方法实现三击功能
     */
    protected void onSingleClick() { // 处理单击事件（可被子类重写）
        checked = !checked; // 切换选中状态
        if (onClickListener != null) { // 如果设置了点击监听器
            onClickListener.onClick(this); // 触发点击回调
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setEnabled
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置控件的启用状态
     *   禁用状态下无法通过点击改变选中状态
     * 
     * 【参数说明】
     *   @param enabled true=启用，false=禁用
     */
    @Override
    public void setEnabled(boolean enabled) { // 设置启用状态
        super.setEnabled(enabled); // 调用父类方法设置启用状态
        enable=enabled; // 更新内部启用状态标志
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getViewInScreen
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取复选框在屏幕上的矩形区域
     *   用于判断触摸点是否在复选框范围内
     * 
     * 【返回值】
     *   @return 复选框在屏幕坐标系的矩形区域
     */
    private Rect getViewInScreen() { // 获取视图在屏幕上的位置矩形
        int[] location = new int[2]; // 创建位置数组
        this.getLocationOnScreen(location); // 获取视图在屏幕上的位置
        return new Rect(location[0], location[1], this.getWidth() + location[0], this.getHeight() + location[1]); // 返回矩形区域
    }

    // 文本测量用的矩形对象
    Rect rect = new Rect(); // 用于测量文本宽度的临时矩形

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getTextWidth
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   计算文本的宽度
     * 
     * 【参数说明】
     *   @param text 要测量宽度的文本字符串
     * 
     * 【返回值】
     *   @return 文本的宽度（像素值）
     */
    private int getTextWidth(String text) { // 计算文本宽度
        textPaint.getTextBounds(text, 0, text.length(), rect); // 测量文本边界
        int w = rect.width(); // 获取文本宽度
        int h = rect.height(); // 获取文本高度（未使用）
        return w; // 返回文本宽度
    }

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

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：isChecked
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取复选框的选中状态
     * 
     * 【返回值】
     *   @return true=选中，false=未选中
     */
    public boolean isChecked() { // 获取选中状态
        return checked; // 返回当前选中状态
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setChecked
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置复选框的选中状态
     *   禁用状态下无法通过此方法改变状态
     * 
     * 【参数说明】
     *   @param checked true=选中，false=未选中
     */
    public void setChecked(boolean checked) { // 设置选中状态
        if (!enable) return; // 如果控件被禁用，直接返回
        this.checked = checked; // 更新选中状态
        invalidate(); // 请求重绘
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：getText
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取复选框上显示的文本内容
     * 
     * 【返回值】
     *   @return 文本字符串
     */
    public String getText() { // 获取文本内容
        return text; // 返回文本字符串
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setText
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置复选框上显示的文本内容
     * 
     * 【参数说明】
     *   @param text 要显示的文本字符串
     */
    public void setText(String text) { // 设置文本内容
        this.text = text; // 更新文本内容
        initLayout(); // 重新初始化文本布局
        invalidate(); // 请求重绘
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setTextSize
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置文本大小
     * 
     * 【参数说明】
     *   @param size 文本大小（像素值）
     */
    public void setTextSize(int size) { // 设置文本大小
        textPaint.setTextSize(size); // 更新画笔的文本大小
        initLayout(); // 重新初始化文本布局
        invalidate(); // 请求重绘
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setText_y
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置文本的Y坐标位置
     * 
     * 【参数说明】
     *   @param text_y Y坐标值（像素）
     */
    public void setText_y(int text_y) { // 设置文本Y坐标
        this.text_y = text_y; // 更新Y坐标
        initLayout(); // 重新初始化文本布局
        invalidate(); // 请求重绘
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setText_x
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置文本的X坐标位置
     * 
     * 【参数说明】
     *   @param text_x X坐标值（像素）
     */
    public void setText_x(int text_x) { // 设置文本X坐标
        this.text_x = text_x; // 更新X坐标
        initLayout(); // 重新初始化文本布局
        invalidate(); // 请求重绘
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setCheckBitmap
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置选中状态的复选框图片
     * 
     * 【参数说明】
     *   @param checkBitmap 选中状态的位图对象
     */
    public void setCheckBitmap(Bitmap checkBitmap) { // 设置选中状态位图
        this.checkBitmap = checkBitmap; // 更新选中状态位图引用
        invalidate(); // 请求重绘
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setUnCheckBitmap
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置未选中状态的复选框图片
     * 
     * 【参数说明】
     *   @param unCheckBitmap 未选中状态的位图对象
     */
    public void setUnCheckBitmap(Bitmap unCheckBitmap) { // 设置未选中状态位图
        this.unCheckBitmap = unCheckBitmap; // 更新未选中状态位图引用
        invalidate(); // 请求重绘
    }
}
