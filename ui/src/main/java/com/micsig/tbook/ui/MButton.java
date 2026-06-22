package com.micsig.tbook.ui; // UI组件库根包，包含示波器自定义UI控件

import android.content.Context; // Android上下文对象，用于获取资源和系统服务
import android.content.res.TypedArray; // 类型化数组，用于读取XML属性
import android.graphics.Bitmap; // 位图对象，用于存储按钮图片资源
import android.graphics.Canvas; // 画布对象，用于自定义绘制
import android.graphics.Paint; // 画笔对象，用于绘制文本和图形
import android.graphics.PixelFormat; // 像素格式常量
import android.graphics.PorterDuff; // 图像混合模式
import android.graphics.PorterDuffXfermode; // 图像混合模式转换器
import android.graphics.Rect; // 矩形区域，用于图片绘制区域计算
import android.graphics.drawable.BitmapDrawable; // 位图可绘制对象
import android.graphics.drawable.Drawable; // 可绘制对象基类
import android.text.Layout; // 文本布局接口
import android.text.StaticLayout; // 静态文本布局，用于多行文本绘制
import android.text.TextPaint; // 文本画笔，用于绘制文本
import android.util.AttributeSet; // 属性集，用于XML属性解析
import android.view.MotionEvent; // 触摸事件，用于处理用户交互
import android.view.View; // Android视图基类

import androidx.annotation.Nullable; // 可空性注解


/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                              MButton - 自定义按钮控件                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                  ║
 * ║   UI组件库 > 自定义控件 > 按钮组件                                             ║
 * ║   MHO系列示波器软件核心UI控件之一                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【核心职责】                                                                  ║
 * ║   1. 提供支持按下/抬起两种状态图片切换的自定义按钮                               ║
 * ║   2. 支持自定义文本显示，支持文本居中和指定位置                                  ║
 * ║   3. 处理触摸事件，实现按钮按下/抬起状态切换和点击回调                           ║
 * ║   4. 支持触摸移动检测，防止误触                                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【架构设计】                                                                  ║
 * ║   继承关系: MButton extends View                                              ║
 * ║   设计模式: 自定义视图模式，通过onDraw自定义绘制                                ║
 * ║   状态管理: mState布尔值控制按下/抬起状态                                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【数据流向】                                                                  ║
 * ║   XML属性 → TypedArray解析 → 成员变量存储 → onDraw绘制 → 触摸事件更新状态      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【依赖关系】                                                                  ║
 * ║   内部依赖: 无                                                                ║
 * ║   外部依赖: Android SDK (View, Canvas, Paint, Bitmap等)                       ║
 * ║   资源依赖: R.styleable.MButton (自定义属性集)                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【使用示例】                                                                  ║
 * ║   XML布局:                                                                   ║
 * ║   <com.micsig.tbook.ui.MButton                                               ║
 * ║       android:layout_width="100dp"                                            ║
 * ║       android:layout_height="50dp"                                            ║
 * ║       app:touchDownBitmap="@drawable/btn_pressed"                             ║
 * ║       app:touchUpBitmap="@drawable/btn_normal"                                ║
 * ║       android:text="确定"                                                      ║
 * ║       app:textCenterX="true" />                                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【注意事项】                                                                  ║
 * ║   1. 必须设置touchDownBitmap和touchUpBitmap属性                               ║
 * ║   2. 文本绘制使用StaticLayout支持多行文本                                      ║
 * ║   3. 触摸移动超过20像素会取消点击                                              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * 
 * @author liwb
 * @date 2017/3/30
 * @version 1.0
 */

public class MButton extends View {
    // ================================ 成员变量定义 ================================
    
    StaticLayout layout = null; // 静态文本布局对象，用于多行文本的绘制和排版
    TextPaint textPaint = null; // 文本画笔对象，控制文本的颜色、大小、抗锯齿等属性
    Bitmap touchDownBitmap = null; // 按下状态时的按钮图片位图
    Bitmap touchUpBitmap = null; // 抬起状态时的按钮图片位图
    String text = null; // 按钮上显示的文本内容
    int textColor; // 文本颜色值
    float textSize; // 文本大小（像素值）
    boolean mState = false; // 按钮当前状态：true=按下状态，false=抬起状态
    private int text_x, text_y; // 文本绘制位置的X和Y坐标
    private boolean textCenterX = false; // 文本是否水平居中显示的标志
    private OnClickListener onClickListener = null; // 点击事件监听器


    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：单参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   仅传入Context的构造方法，用于代码中动态创建MButton实例
     * 
     * 【参数说明】
     *   @param context Android上下文对象，用于获取资源和系统服务
     * 
     * 【调用链】
     *   this(context, null) → 双参数构造方法 → 三参数构造方法
     */
    public MButton(Context context) { // 构造方法：仅传入Context
        this(context, null); // 调用双参数构造方法，attrs传null
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：双参数版本
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   传入Context和AttributeSet的构造方法，用于XML布局文件中创建MButton
     * 
     * 【参数说明】
     *   @param context Android上下文对象
     *   @param attrs XML属性集，包含布局文件中定义的属性
     * 
     * 【调用链】
     *   this(context, attrs, 0) → 三参数构造方法
     */
    public MButton(Context context, @Nullable AttributeSet attrs) { // 构造方法：传入Context和属性集
        this(context, attrs, 0); // 调用三参数构造方法，defStyleAttr传0
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法：三参数版本（主构造方法）
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   完整的构造方法，完成所有属性的解析和初始化
     *   从XML属性中读取按钮图片、文本内容、文本样式等配置
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
     */
    public MButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { // 构造方法：完整参数版本
        super(context, attrs, defStyleAttr); // 调用父类View的构造方法

        // 从主题中获取MButton自定义属性集
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MButton, defStyleAttr, 0); // 获取类型化属性数组
        int n = a.getIndexCount(); // 获取属性数量
        for (int i = 0; i < n; i++) { // 遍历所有属性
            int attr = a.getIndex(i); // 获取当前属性的索引
            if (attr == R.styleable.MButton_touchDownBitmap) { // 如果是按下状态图片属性
                touchDownBitmap = ((BitmapDrawable) a.getDrawable(attr)).getBitmap(); // 获取按下状态位图
            } else if (attr == R.styleable.MButton_touchUpBitmap) { // 如果是抬起状态图片属性
                touchUpBitmap = ((BitmapDrawable) a.getDrawable(attr)).getBitmap(); // 获取抬起状态位图
            } else if (attr == R.styleable.MButton_android_text) { // 如果是文本内容属性
                text = a.getString(attr); // 获取文本字符串
            } else if (attr == R.styleable.MButton_android_textColor) { // 如果是文本颜色属性
                textColor = a.getColor(attr, 0xFFFFFF); // 获取文本颜色，默认白色
            } else if (attr == R.styleable.MButton_android_textSize) { // 如果是文本大小属性
                textSize = a.getDimension(attr, 16); // 获取文本大小，默认16像素
            } else if (attr == R.styleable.MButton_text_x) { // 如果是文本X坐标属性
                text_x = (int) a.getDimension(attr, 16); // 获取文本X坐标，默认16像素
            } else if (attr == R.styleable.MButton_text_y) { // 如果是文本Y坐标属性
                text_y = (int) a.getDimension(attr, 16); // 获取文本Y坐标，默认16像素
            } else if (attr == R.styleable.MButton_textCenterX) { // 如果是文本水平居中属性
                textCenterX = a.getBoolean(attr, false); // 获取是否居中标志，默认不居中
            }

        }
        a.recycle(); // 回收TypedArray资源，避免内存泄漏

        // 初始化文本画笔
        textPaint = new TextPaint(); // 创建文本画笔实例
        textPaint.setColor(textColor); // 设置文本颜色
        textPaint.setTextSize(textSize); // 设置文本大小
        textPaint.setAntiAlias(true); // 启用抗锯齿，使文本边缘更平滑
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：initLayout
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   初始化静态文本布局对象，用于多行文本的绘制
     *   根据按钮宽度和文本内容创建StaticLayout
     * 
     * 【前置条件】
     *   touchUpBitmap和text不为null
     * 
     * 【处理逻辑】
     *   创建StaticLayout对象，设置文本对齐方式、行间距等参数
     */
    private void initLayout() { // 初始化文本布局
        if (touchUpBitmap != null && text != null) // 检查位图和文本是否有效
            layout = new StaticLayout(text, textPaint, touchUpBitmap.getWidth(), // 创建静态布局
                    Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true); // 参数：文本、画笔、宽度、对齐方式、行倍数、行间距、包含padding
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

    // 绘制相关的矩形区域变量
    private Rect src = new Rect(); // 源矩形，用于从位图中截取区域
    private Rect des; // 目标矩形，用于指定绘制到画布的位置

    // 图像混合模式对象
    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR); // 清除模式，用于清除画布
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC); // 源模式，用于绘制源图像
    private Paint paint=new Paint(); // 通用画笔对象，用于绘制操作
    
    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：onDraw
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   绘制按钮视图，根据当前状态绘制对应的位图和文本
     *   核心绘制逻辑：清除画布 → 绘制按钮图片 → 绘制文本
     * 
     * 【参数说明】
     *   @param canvas 画布对象，用于绑定绘制操作
     * 
     * 【绘制流程】
     *   1. 清除画布背景
     *   2. 根据mState状态选择绘制按下或抬起状态的位图
     *   3. 如果有文本，计算文本位置并绘制
     * 
     * 【文本居中处理】
     *   根据文本方向（从左到右或从右到左）计算居中位置
     */
    @Override
    protected void onDraw(Canvas canvas) { // 绘制视图
        if (touchDownBitmap != null || touchUpBitmap != null) { // 检查是否有有效的位图资源
//            Rect des, src; // 注释掉的局部变量声明
//            des = new Rect(0, 0, touchDownBitmap.getWidth(), touchDownBitmap.getHeight()); // 注释掉的代码
            paint.setXfermode(clearMode); // 设置画笔为清除模式
            canvas.drawPaint(paint); // 清除画布内容
            paint.setXfermode(srcMode); // 设置画笔为源模式
            src.set(0, 0, touchDownBitmap.getWidth(), touchDownBitmap.getHeight()); // 设置源矩形区域
            des = src; // 目标矩形与源矩形相同
            if (mState) { // 如果当前是按下状态
                canvas.drawBitmap(touchDownBitmap, src, des, paint); // 绘制按下状态位图
            } else { // 如果当前是抬起状态
                canvas.drawBitmap(touchUpBitmap, src, des, paint); // 绘制抬起状态位图
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

    // 触摸事件相关变量
    private int downx, downy; // 按下时的屏幕坐标，用于检测触摸移动距离

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：onTouchEvent
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   处理触摸事件，实现按钮的交互逻辑
     *   包括按下状态切换、移动检测、点击事件触发
     * 
     * 【参数说明】
     *   @param event 触摸事件对象，包含动作类型和坐标信息
     *   @return 始终返回true，表示消费了触摸事件
     * 
     * 【事件处理】
     *   ACTION_DOWN: 记录按下坐标，切换到按下状态
     *   ACTION_UP: 切换到抬起状态，检测是否触发点击
     *   ACTION_CANCEL: 切换到抬起状态
     *   ACTION_MOVE: 检测移动距离，超过阈值则取消按下状态
     * 
     * 【防误触逻辑】
     *   移动距离超过20像素时取消按下状态，防止误触
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) { // 处理触摸事件
        if (!isEnabled()) return true; // 如果控件被禁用，直接返回不处理
        switch (event.getAction()) { // 根据动作类型分发处理
            case MotionEvent.ACTION_DOWN: // 按下事件
                downx = (int) event.getRawX(); // 记录按下时的屏幕X坐标
                downy = (int) event.getRawY(); // 记录按下时的屏幕Y坐标
                mState = true; // 切换到按下状态
                invalidate(); // 请求重绘
                break; // 结束case
            case MotionEvent.ACTION_UP: // 抬起事件
                mState = false; // 切换到抬起状态
                invalidate(); // 请求重绘
                //处理点击事件
                if (getViewInScreen().contains((int) event.getRawX(), (int) event.getRawY())) { // 检查抬起位置是否在按钮范围内
                    if (onClickListener != null) { // 如果设置了点击监听器
                        onClickListener.onClick(this); // 触发点击回调
                    }
                }
                break; // 结束case
            case MotionEvent.ACTION_CANCEL: // 取消事件
                mState = false; // 切换到抬起状态
                invalidate(); // 请求重绘
                break; // 结束case
            case MotionEvent.ACTION_MOVE: // 移动事件
                if (!getViewInScreen().contains((int) event.getRawX(), (int) event.getRawY()) // 如果移出按钮区域
                        || (Math.abs(downx - (int) event.getRawX()) > 20 || Math.abs(downy - (int) event.getRawY()) > 20) // 或者移动距离超过20像素
                ) {
                    mState = false; // 切换到抬起状态
                    invalidate(); // 请求重绘
                }
                break; // 结束case
        }
        return true; // 返回true表示消费了事件
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setTextSize
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置文本大小，并更新显示
     * 
     * 【参数说明】
     *   @param textSize 文本大小（像素值）
     */
    public void setTextSize(int textSize) { // 设置文本大小
        textPaint.setTextSize(textSize); // 更新画笔的文本大小
        initLayout(); // 重新初始化文本布局
        invalidate(); // 请求重绘
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
     * 方法：getViewInScreen
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   获取按钮在屏幕上的矩形区域
     *   用于判断触摸点是否在按钮范围内
     * 
     * 【返回值】
     *   @return 按钮在屏幕坐标系的矩形区域
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
     *   计算文本的总宽度（支持多行文本）
     *   遍历所有行，返回最宽行的宽度
     * 
     * 【参数说明】
     *   @param text 要测量宽度的文本字符串
     * 
     * 【返回值】
     *   @return 文本的最大宽度（像素值）
     */
    private int getTextWidth(String text) { // 计算文本宽度
        String[] split = text.split("\\n"); // 按换行符分割文本
        int w = 0; // 初始化最大宽度为0
        for (String s : split) { // 遍历每一行
            textPaint.getTextBounds(s, 0, s.length(), rect); // 测量当前行的边界
            w = Math.max(w, rect.width()); // 更新最大宽度
        }
        return w; // 返回最大宽度
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：drawableToBitmap
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   将Drawable对象转换为Bitmap对象
     *   静态工具方法，可用于图片格式转换
     * 
     * 【参数说明】
     *   @param drawable 要转换的Drawable对象
     * 
     * 【返回值】
     *   @return 转换后的Bitmap对象
     * 
     * 【实现细节】
     *   根据Drawable的透明度选择合适的Bitmap配置
     *   ARGB_8888支持透明度，RGB_565不支持透明度但更省内存
     */
    public static Bitmap drawableToBitmap(Drawable drawable) { // Drawable转Bitmap的静态方法

        Bitmap bitmap = Bitmap.createBitmap( // 创建位图
                drawable.getIntrinsicWidth(), // 使用Drawable的原始宽度
                drawable.getIntrinsicHeight(), // 使用Drawable的原始高度
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 // 如果有透明度，使用ARGB_8888
                        : Bitmap.Config.RGB_565); // 否则使用RGB_565更省内存
        Canvas canvas = new Canvas(bitmap); // 创建画布并绑定到位图
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()); // 设置Drawable的边界
        drawable.draw(canvas); // 将Drawable绘制到画布上
        return bitmap; // 返回转换后的位图
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setTouchDownBitmap
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置按下状态的按钮图片
     * 
     * 【参数说明】
     *   @param touchDownBitmap 按下状态的位图对象
     */
    public void setTouchDownBitmap(Bitmap touchDownBitmap) { // 设置按下状态位图
        this.touchDownBitmap = touchDownBitmap; // 更新按下状态位图引用
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法：setTouchUpBitmap
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     *   设置抬起状态的按钮图片
     * 
     * 【参数说明】
     *   @param touchUpBitmap 抬起状态的位图对象
     */
    public void setTouchUpBitmap(Bitmap touchUpBitmap) { // 设置抬起状态位图
        this.touchUpBitmap = touchUpBitmap; // 更新抬起状态位图引用
    }
}
