package com.micsig.tbook.ui;

import android.content.Context;  // Android上下文环境类
import android.graphics.Bitmap;  // 位图类
import android.graphics.BitmapFactory;  // 位图工厂类
import android.graphics.Canvas;  // 画布类
import android.graphics.Color;  // 颜色类
import android.graphics.Paint;  // 画笔类
import android.graphics.Rect;  // 矩形类
import android.util.AttributeSet;  // XML属性集类
import android.view.View;  // Android视图基类

import androidx.annotation.Nullable;  // 可空注解

import com.micsig.base.Logger;  // 日志工具类

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                              MProgress                                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位: UI组件库 - 自定义进度条控件                                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责:                                                                     │
 * │   1. 继承View，提供自定义进度条绘制功能                                         │
 * │   2. 使用位图资源实现进度条背景和激活状态                                       │
 * │   3. 支持百分比进度显示                                                        │
 * │   4. 实现渐变色进度效果                                                        │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计:                                                                     │
 * │   ┌─────────────────┐                                                       │
 * │   │      View       │  ← 继承自Android视图基类                                │
 * │   └────────┬────────┘                                                       │
 * │            ↓                                                                 │
 * │   ┌─────────────────┐                                                       │
 * │   │    MProgress    │  ← 自定义绘制进度条                                     │
 * │   └─────────────────┘                                                       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向:                                                                     │
 * │   setProgress() → 计算宽度 → invalidate() → onDraw() → 绘制进度条             │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系:                                                                     │
 * │   - View: 基类，提供视图框架                                                  │
 * │   - Bitmap: 位图资源                                                         │
 * │   - Canvas: 画布，用于绘制                                                    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例:                                                                     │
 * │   XML布局:                                                                   │
 * │   <com.micsig.tbook.ui.MProgress                                            │
 * │       android:id="@+id/progress"                                            │
 * │       android:layout_width="wrap_content"                                   │
 * │       android:layout_height="wrap_content" />                               │
 * │                                                                             │
 * │   Java代码:                                                                  │
 * │   MProgress progress = findViewById(R.id.progress);                         │
 * │   progress.setProgress(50);  // 设置50%进度                                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ @author liwb                                                                 │
 * │ @date 2018/5/25                                                             │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class MProgress extends View {
    
    // =========================== 成员变量定义 ===========================
    
    /** 日志标签 */  // 用于调试输出
    private String TAG = "MProgress";

    /** Android上下文环境 */  // 用于资源访问
    private Context context;
    
    /** 进度条位图资源 */  // bitmapPlate: 普通状态背景位图
    private Bitmap bitmapPlate, bitmapPlateActive;  // bitmapPlateActive: 激活状态前景位图
    
    /** 缓存画布 */  // 用于离屏绘制
    private Canvas cacheCanvas;
    
    /** 位图尺寸 */  // bitmapWidth: 位图宽度
    private int bitmapWidth, bitmapHeight;  // bitmapHeight: 位图高度
    
    /** 画笔对象 */  // backgroundPaint: 背景画笔
    private Paint backgroundPaint;  // 用于绘制背景和文本
    
    /** 进度画笔 */  // 用于绘制进度条
    private Paint progressPaint;
    
    /** 当前进度值 */  // 范围0-100
    private int progress;
    
    /** 绘制区域 */  // srcRect: 源矩形区域
    private Rect srcRect, desRect;  // desRect: 目标矩形区域

    /** 进度条的宽度 */  // 根据进度计算
    private int view_width;
    
    /** 画布的宽度 */  // 用于布局计算
    private int view_base_width;
    
    /** 控件的宽度 */  // 用于布局计算
    private int view_edge_width;

    /** 渐变色开始颜色 */  // 青色
    private static final int DEFAULT_START_COLOR = Color.parseColor("#34DAB5");
    
    /** 渐变色结束颜色 */  // 蓝色
    private static final int DEFAULT_END_COLOR = Color.parseColor("#27A5FE");

    // =========================== 构造方法 ===========================
    
    /**
     * 单参数构造方法
     * 
     * @param context Android上下文环境
     */
    public MProgress(Context context) {
        super(context);  // 调用父类构造
        initView(context);  // 初始化视图
    }

    /**
     * 双参数构造方法
     * 
     * @param context Android上下文环境
     * @param attrs XML属性集
     */
    public MProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);  // 调用父类构造
        initView(context);  // 初始化视图
    }

    /**
     * 三参数构造方法（完整构造）
     * 
     * @param context Android上下文环境
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public MProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造
        initView(context);  // 初始化视图
    }

    // =========================== 初始化方法 ===========================
    
    /**
     * 初始化视图
     * 加载位图资源并初始化画笔
     * 
     * @param context Android上下文环境
     */
    private void initView(Context context) {
        this.context = context;  // 保存上下文引用
        bitmapPlate = BitmapFactory.decodeResource(context.getResources(), R.drawable.sbtn_slider_plate);  // 加载普通状态背景位图
        bitmapWidth = bitmapPlate.getWidth();  // 获取位图宽度
        bitmapHeight = bitmapPlate.getHeight();  // 获取位图高度

        bitmapPlateActive = BitmapFactory.decodeResource(context.getResources(), R.drawable.sbtn_slider_plate_active);  // 加载激活状态前景位图
        bitmapWidth = bitmapPlateActive.getWidth();  // 更新位图宽度
        bitmapHeight = bitmapPlateActive.getHeight();  // 更新位图高度

        backgroundPaint = new Paint();  // 创建背景画笔
        backgroundPaint.setStrokeWidth(bitmapWidth);  // 设置画笔宽度
        backgroundPaint.setColor(Color.parseColor("#cccccc"));  // 设置画笔颜色（灰色）
        backgroundPaint.setDither(true);  // 启用抖动效果
        backgroundPaint.setAntiAlias(true);  // 启用抗锯齿
        backgroundPaint.setTextSize(17);  // 设置文本大小

        progressPaint = new Paint();  // 创建进度画笔
        progressPaint.setStrokeWidth(bitmapWidth);  // 设置画笔宽度
        progressPaint.setDither(true);  // 启用抖动效果
        progressPaint.setAntiAlias(true);  // 启用抗锯齿
        srcRect = new Rect(0, 0, bitmapPlate.getWidth(), bitmapPlate.getHeight());  // 初始化源矩形（整个位图）
        desRect = new Rect(0, 0, bitmapPlate.getWidth(), bitmapPlate.getHeight());  // 初始化目标矩形（初始为整个位图）
    }

    // =========================== 进度设置方法 ===========================
    
    /**
     * 设置进度值
     * 根据进度值计算绘制区域并触发重绘
     * 
     * @param progress 进度值（0-100）
     */
    public void setProgress(int progress) {
        this.progress = progress;  // 保存进度值
        view_width = bitmapPlate.getWidth() * progress / 100;  // 根据进度计算绘制宽度
        desRect.set(0, 0, view_width, bitmapPlate.getHeight());  // 更新目标矩形区域
        invalidate();  // 触发重绘
        // Logger.i(TAG, "setProgress:" + progress);  // 调试日志
    }

    // =========================== 绘制方法 ===========================
    
    /**
     * 绘制方法
     * 绘制进度条背景、前景和百分比文本
     * 
     * @param canvas 画布对象
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();  // 保存画布状态
        canvas.drawColor(0x00000000);  // 清空画布（透明色）
        canvas.drawBitmap(bitmapPlate, srcRect, srcRect, progressPaint);  // 绘制背景位图（普通状态）
        canvas.drawBitmap(bitmapPlateActive, desRect, desRect, progressPaint);  // 绘制前景位图（激活状态，根据进度裁剪）
        canvas.drawText(progress + "%", 200, 17, backgroundPaint);  // 绘制百分比文本
        canvas.restore();  // 恢复画布状态
        // Logger.i(TAG,"progress:"+progress);  // 调试日志
    }
}
