package com.micsig.tbook.tbookscope.wavezone.wave;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.Log;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.App;

import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * 数学/参考波形类 - 示波器数学运算波形和参考波形的显示管理核心类
 * 
 * 【模块定位】
 * - 所属模块：wavezone.wave（波形显示区域-波形管理子模块）
 * - 核心职责：管理数学运算波形（如A+B、A-B等）和参考波形的显示、位置、选中状态
 * - 架构层级：业务实现层，实现IWave和IWorkMode接口
 * 
 * 【核心职责】
 * 1. 波形显示管理：根据选中/未选中状态显示不同样式的波形标签
 * 2. 位置管理：管理波形的垂直位置（Y坐标），支持位置移动和像素偏移
 * 3. 工作模式切换：支持YT模式和YTZOOM模式之间的切换
 * 4. 标签管理：管理波形标签的显示位置和文本内容
 * 5. 可见性控制：控制波形的显示/隐藏状态
 * 6. 选中状态管理：管理波形的选中/未选中状态
 * 
 * 【架构图】
 * ┌─────────────────────────────────────────────────────────────┐
 * │            WaveManage_YT（波形管理器-YT模式）               │
 * │                   (波形管理核心)                             │
 * │  - 创建MathRefWave实例                                      │
 * │  - 管理波形资源Bitmap                                        │
 * │  - 协调波形显示流程                                          │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 创建和管理
 * ┌─────────────────────────────────────────────────────────────┐
 * │                  MathRefWave（本类）                         │
 * │                 (数学/参考波形实现类)                        │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  状态管理                                             │  │
 * │  │  - selected: 选中状态                                 │  │
 * │  │  - visible: 可见状态                                  │  │
 * │  │  - channelId: 通道ID                                 │  │
 * │  │  - posY: 位置坐标(1000对应系统)                       │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  显示资源                                             │  │
 * │  │  - resBitmap[6]: 6种状态位图                          │  │
 * │  │    [0]CH_N: 未选中-标准位置                           │  │
 * │  │    [1]CH_N_DOWN: 未选中-向下位置                      │  │
 * │  │    [2]CH_N_UP: 未选中-向上位置                        │  │
 * │  │    [3]CH_S: 选中-标准位置                             │  │
 * │  │    [4]CH_S_DOWN: 选中-向下位置                        │  │
 * │  │    [5]CH_S_UP: 选中-向上位置                          │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  核心方法                                             │  │
 * │  │  - draw(): 绘制波形标签                               │  │
 * │  │  - setY()/getY(): 位置管理                            │  │
 * │  │  - switchWorkMode(): 工作模式切换                    │  │
 * │  │  - setLabel()/getLabel(): 标签管理                   │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 实现接口
 * ┌─────────────────────────────────────────────────────────────┐
 * │    IWave接口              │        IWorkMode接口           │
 * │  - 波形基础操作           │  - 工作模式切换                 │
 * │  - 位置管理               │  - YT/YTZOOM模式支持           │
 * │  - 状态管理               │                                │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * 【依赖关系】
 * - 实现接口：IWave（波形基础接口）、IWorkMode（工作模式接口）
 * - 资源依赖：WaveManage_YT（提供波形位图资源）
 * - 工具依赖：ScopeBase（坐标转换）、GlobalVar（全局配置）、CacheUtil（缓存）
 * - 回调接口：OnSelectChangeEvent（选中状态变化回调）、OnMovingWaveEvent（波形移动回调）
 * 
 * 【数学/参考波形显示流程】
 * 1. 初始化阶段：
 *    WaveManage_YT创建 → 传入resBitmap资源 → 构造MathRefWave → 初始化draw()
 * 
 * 2. 显示更新阶段：
 *    状态变化(setSelected/setVisible/setY) → draw()重绘 → onRefresh()通知GL刷新
 * 
 * 3. 绘制流程：
 *    清空画布 → 判断可见性 → 判断选中状态 → 计算Y位置 → 选择对应位图 → 绘制标签文字
 * 
 * 4. 位置管理：
 *    UI坐标(y) ←→ 系统坐标(posY) 通过ScopeBase进行转换
 *    - y: 屏幕像素位置
 *    - posY: 1000对应的位置（系统内部坐标）
 * 
 * 【设计模式】
 * 1. 状态模式：根据selected和visible状态显示不同的位图样式
 * 2. 观察者模式：通过OnSelectChangeEvent和OnMovingWaveEvent回调通知状态变化
 * 3. 模板方法模式：实现IWave接口定义的标准波形操作方法
 * 
 * 【使用场景】
 * 1. 数学波形显示：显示A+B、A-B、A×B、A/B等数学运算结果波形
 * 2. 参考波形显示：显示用户保存的参考波形
 * 3. 波形位置调整：用户通过拖拽调整波形垂直位置
 * 4. 工作模式切换：在YT和YTZOOM模式间切换时更新波形位置
 * 
 * 【注意事项】
 * 1. 线程安全：draw()方法使用synchronized(bmp)保护位图操作
 * 2. 坐标转换：UI坐标和系统坐标需要通过ScopeBase进行转换
 * 3. 资源管理：位图资源由WaveManage_YT统一管理，本类只持有引用
 * 4. 标签位置：标签位置会被缓存到CacheUtil中，用于持久化
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/5/22
 * @see IWave
 * @see IWorkMode
 * @see WaveManage_YT
 * @see ScopeBase
 */

public class MathRefWave implements IWave, IWorkMode {

    // ================================ 属性定义区域 ================================
    
    /**
     * 选中状态标识
     * true: 当前波形被选中，显示选中样式
     * false: 当前波形未被选中，显示未选中样式
     */
    private boolean selected;
    
    /**
     * 可见性状态标识
     * true: 波形可见，参与绘制
     * false: 波形隐藏，不参与绘制
     */
    private boolean visible;
    
    /**
     * 通道ID
     * 用于标识当前波形对应的通道（如Math1、Ref1等）
     * 取值参考TChan常量定义
     */
    private int channelId;
    
    /**
     * X坐标位置（预留字段，当前未使用）
     * 用于水平位置管理，数学/参考波形通常不需要水平位置调整
     */
    private long x;
    
    /**
     * Y坐标位置（屏幕像素坐标）
     * 表示波形在屏幕上的实际像素位置
     * 范围：0 到 波形区域高度
     */
    private double y;
    
    /**
     * Y坐标位置（系统内部坐标）
     * 使用1000对应系统的坐标表示
     * 通过ScopeBase与y进行转换
     */
    private double posY;
    
    /**
     * 波形颜色值
     * 用于绘制波形标签文字的颜色
     * 取值参考TChan.getChannelColor()
     */
    private int color;
    
    /**
     * 波形移动事件回调接口
     * 当波形位置发生变化时触发回调
     */
    private OnMovingWaveEvent onMovingWaveEvent;
    
    /**
     * 选中状态变化事件回调接口
     * 当波形选中状态发生变化时触发回调
     */
    private OnSelectChangeEvent onSelectChangeEvent;

    // ================================ 私有变量区域 ================================
    
    /**
     * 应用上下文
     * 用于获取资源颜色等系统服务
     */
    private Context context = App.get().getApplicationContext();
    
    /**
     * 波形位图资源数组
     * 存储不同状态下的波形标签位图
     * 索引对应WaveManage_YT中的常量：
     * - [0]CH_N: 未选中-标准位置
     * - [1]CH_N_DOWN: 未选中-向下位置（波形超出屏幕下方）
     * - [2]CH_N_UP: 未选中-向上位置（波形超出屏幕上方）
     * - [3]CH_S: 选中-标准位置
     * - [4]CH_S_DOWN: 选中-向下位置
     * - [5]CH_S_UP: 选中-向上位置
     */
    private Bitmap[] resBitmap = new Bitmap[6];
    
    /**
     * 绘制缓冲位图
     * 用于缓存绘制内容，提高绘制效率
     * 宽度1800像素，高度与resBitmap[1]相同
     */
    private final Bitmap bmp;
    
    /**
     * 画布对象
     * 用于在bmp上进行绘制操作
     */
    private Canvas mCanvas;
    
    /**
     * 绘制画笔
     * 用于绘制波形标签文字
     */
    private Paint paint;
    
    /**
     * 文字绘制画笔
     * 用于绘制带描边的文字效果
     * 设置黑色描边，提高文字可读性
     */
    private TextPaint textPaint;
    
    /**
     * 位图变化标识
     * true: 位图内容已变化，需要刷新GL纹理
     * false: 位图内容未变化
     */
    private boolean isChanageBitmap = false;
    
    /**
     * OpenGL画布引用
     * 用于在OpenGL渲染时刷新纹理
     */
    private ICanvasGL canvasGL;
    
    /**
     * 波形标签文本
     * 显示在波形标签上的文字内容（如"Math1"、"Ref1"等）
     */
    private String label = "";
    
    /**
     * 标签矩形区域
     * 用于存储标签的显示位置和大小
     * 用于触摸检测和位置管理
     */
    private final Rect labelRect = new Rect();
    
    /**
     * 刷新GL纹理
     * 当位图内容变化时，通知OpenGL刷新纹理
     */
    public void onRefresh() {
        if (canvasGL != null) { // 检查GL画布是否有效
            canvasGL.onRefreshTexture(); // 刷新纹理内容
        }
    }

    // ================================ 构造方法区域 ================================
    
    /**
     * 构造方法 - 初始化数学/参考波形实例
     * 
     * 【功能说明】
     * 创建MathRefWave实例，初始化绘制资源和画布
     * 
     * 【参数说明】
     * @param resBitmap 波形位图资源数组，包含6种状态的位图
     *                  由WaveManage_YT提供
     * 
     * 【初始化流程】
     * 1. 保存位图资源引用
     * 2. 创建绘制缓冲位图（1800x高度）
     * 3. 创建画布对象
     * 4. 初始化画笔（文字大小20，抗锯齿）
     * 5. 初始化文字画笔（黑色描边，宽度4）
     * 6. 执行首次绘制
     * 
     * 【使用示例】
     * Bitmap[] bitmaps = new Bitmap[6];
     * // ... 初始化bitmaps
     * MathRefWave wave = new MathRefWave(bitmaps);
     */
    public MathRefWave(Bitmap[] resBitmap) {
        this.resBitmap = resBitmap; // 保存位图资源引用
        bmp = Bitmap.createBitmap(1800, resBitmap[1].getHeight(), Bitmap.Config.ARGB_8888); // 创建缓冲位图，宽度1800，高度与资源位图相同
        mCanvas = new Canvas(bmp); // 创建画布对象，绑定到缓冲位图
        paint = new Paint(); // 创建普通画笔
        paint.setTextSize(20); // 设置文字大小为20
        paint.setAntiAlias(true); // 启用抗锯齿，提高文字显示质量
        paint.setDither(true); // 启用抖动，提高颜色渐变效果

        textPaint = new TextPaint(); // 创建文字专用画笔
        textPaint.setColor(Color.BLACK); // 设置描边颜色为黑色
        textPaint.setTextSize(20); // 设置文字大小为20
        textPaint.setStyle(Paint.Style.STROKE); // 设置描边样式
        textPaint.setStrokeWidth(4); // 设置描边宽度为4

        label = ""; // 初始化标签为空字符串
        draw(); // 执行首次绘制
    }

    // ================================ 资源管理方法区域 ================================
    
    /**
     * 更换位图资源
     * 
     * 【功能说明】
     * 更新波形显示使用的位图资源，通常在主题切换或资源更新时调用
     * 
     * 【参数说明】
     * @param resBitmap 新的波形位图资源数组
     * 
     * 【调用时机】
     * - 主题切换时
     * - 资源重新加载时
     * - 显示样式更新时
     */
    public void changeBitMap(Bitmap[] resBitmap) {
        this.resBitmap = resBitmap; // 更新位图资源引用
        draw(); // 重新绘制波形
    }

    /**
     * 初始化标签矩形区域
     * 
     * 【功能说明】
     * 根据标签文本和当前位置计算标签的显示矩形区域
     * 用于触摸检测和标签位置管理
     * 
     * 【计算逻辑】
     * 1. 测量标签文本宽度
     * 2. 从缓存获取标签X位置
     * 3. 根据Y位置计算矩形的top和bottom
     * 
     * 【使用场景】
     * - 初始化标签位置时
     * - 标签文本变化后重新计算区域时
     */
    public void initLabelRect() {
        float labelTextW = textPaint.measureText(label); // 测量标签文本宽度
        int labelX = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_LABEL_POSITION + getLineNameID()); // 从缓存获取标签X位置
        labelRect.left = labelX; // 设置矩形左边界
        labelRect.top = (int) Math.round(getY() - (resBitmap[WaveManage_YT.CH_S].getHeight() - 4)); // 计算矩形上边界
        labelRect.right = labelX + Math.round(labelTextW); // 计算矩形右边界
        labelRect.bottom = (int) Math.round(getY() + (resBitmap[WaveManage_YT.CH_S].getHeight() - 4)); // 计算矩形下边界
    }

    // ================================ 标签管理方法区域 ================================
    
    /**
     * 获取波形标签文本
     * 
     * 【功能说明】
     * 返回当前波形的标签文本内容
     * 
     * 【返回值】
     * @return 标签文本字符串，如"Math1"、"Ref1"等
     */
    public String getLabel() {
        return label;
    }

    /**
     * 设置波形标签文本
     * 
     * 【功能说明】
     * 更新波形标签文本内容，并重新计算标签位置
     * 
     * 【参数说明】
     * @param label 新的标签文本内容
     * 
     * 【处理流程】
     * 1. 保存标签文本
     * 2. 从缓存获取标签X位置
     * 3. 如果位置有变化，更新标签矩形
     * 4. 重新计算标签位置
     * 5. 触发重绘
     */
    public void setLabel(String label) {
        this.label = label; // 保存标签文本
        int cacheLabelPos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_LABEL_POSITION + getLineNameID()); // 从缓存获取标签位置
        if (labelRect.left != cacheLabelPos) labelRect.left = cacheLabelPos; // 如果位置有变化，更新左边界
        changeLabelPos(labelRect.left); // 重新计算标签位置
        draw(); // 触发重绘
    }

    /**
     * 清除模式 - 用于清除画布内容
     * 使用PorterDuff.Mode.CLEAR模式，完全清除像素
     */
    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    
    /**
     * 源模式 - 用于绘制源图像
     * 使用PorterDuff.Mode.SRC模式，直接绘制源图像
     */
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

    /**
     * 绘制波形标签 - 核心绘制方法
     * 
     * 【功能说明】
     * 根据当前状态（可见性、选中状态、位置）绘制波形标签
     * 使用双缓冲技术，先绘制到bmp，再由GL渲染
     * 
     * 【绘制流程】
     * 1. 同步锁定bmp，确保线程安全
     * 2. 清空画布内容
     * 3. 计算Y位置（坐标转换）
     * 4. 根据可见性判断是否绘制
     * 5. 根据选中状态选择对应位图样式
     * 6. 根据Y位置选择标准/向上/向下位图
     * 7. 绘制位图和标签文字
     * 8. 标记位图已变化，通知GL刷新
     * 
     * 【状态组合】
     * - 可见 + 选中 + 标准位置：CH_S
     * - 可见 + 选中 + 向上位置：CH_S_UP
     * - 可见 + 选中 + 向下位置：CH_S_DOWN
     * - 可见 + 未选中 + 标准位置：CH_N
     * - 可见 + 未选中 + 向上位置：CH_N_UP
     * - 可见 + 未选中 + 向下位置：CH_N_DOWN
     * - 不可见：不绘制任何内容
     * 
     * 【线程安全】
     * 使用synchronized(bmp)确保多线程访问安全
     */
    private void draw() {
        synchronized (bmp) { // 同步锁定位图对象，确保线程安全
            paint.setXfermode(clearMode); // 设置清除模式
            mCanvas.drawPaint(paint); // 清空画布内容
            paint.setXfermode(srcMode); // 设置源图像模式
            int h = getTextHeight(label) + (getTextHeight(label) == 0 ? 0 : 3); // 计算文字高度（含间距）
            int y = (int) Math.round(ScopeBase.changeAccuracy(this.posY * ScopeBase.getToUICoff())); // 将系统坐标转换为UI坐标
            
            if (this.visible) { // 判断波形是否可见
                if (this.selected) { // 判断是否选中状态
                    if (y < 0) { // Y位置超出屏幕上方
                        mCanvas.drawBitmap(resBitmap[WaveManage_YT.CH_S_UP], 0, 0, paint); // 绘制选中-向上位图
                    } else if (y > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // Y位置超出屏幕下方
                        mCanvas.drawBitmap(resBitmap[WaveManage_YT.CH_S_DOWN], 0, 0, paint); // 绘制选中-向下位图
                    } else { // Y位置在屏幕范围内
                        mCanvas.drawBitmap(resBitmap[WaveManage_YT.CH_S], 0, 0, paint); // 绘制选中-标准位图

                        paint.setColor(TChan.getChannelColor(context, channelId)); // 设置文字颜色为通道颜色
                        // 绘制带描边的文字（先绘制描边层）
                        mCanvas.drawText(label, labelRect.left, Math.round(y - labelRect.top), textPaint);
                        // 绘制文字主体
                        mCanvas.drawText(label, labelRect.left, Math.round(y - labelRect.top), paint);
                    }
                } else { // 未选中状态
                    if (y < 0) { // Y位置超出屏幕上方
                        mCanvas.drawBitmap(resBitmap[WaveManage_YT.CH_N_UP], 0, 0, paint); // 绘制未选中-向上位图
                    } else if (y > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // Y位置超出屏幕下方
                        mCanvas.drawBitmap(resBitmap[WaveManage_YT.CH_N_DOWN], 0, 0, paint); // 绘制未选中-向下位图
                    } else { // Y位置在屏幕范围内
                        mCanvas.drawBitmap(resBitmap[WaveManage_YT.CH_N], 0, 0, paint); // 绘制未选中-标准位图

                        paint.setColor(TChan.getChannelColor(context, channelId)); // 设置文字颜色为通道颜色
                        // 绘制带描边的文字（先绘制描边层）
                        mCanvas.drawText(label, labelRect.left, Math.round(y - labelRect.top), textPaint);
                        // 绘制文字主体
                        mCanvas.drawText(label, labelRect.left, Math.round(y - labelRect.top), paint);
                    }
                }
            }
            isChanageBitmap = true; // 标记位图已变化
            onRefresh(); // 通知GL刷新纹理
        }
    }

    /**
     * 改变标签位置
     * 
     * 【功能说明】
     * 更新标签的水平位置，并进行边界检查
     * 同时更新缓存中的标签位置
     * 
     * 【参数说明】
     * @param x 标签新的X坐标位置
     * 
     * 【边界检查】
     * - 左边界：不小于35像素
     * - 右边界：不超过屏幕宽度减去标签宽度
     * 
     * 【处理流程】
     * 1. 测量标签文本宽度
     * 2. 进行左右边界检查
     * 3. 更新标签矩形区域
     * 4. 触发重绘
     * 5. 保存位置到缓存
     */
    public void changeLabelPos(int x) {
        float labelTextW = textPaint.measureText(label); // 测量标签文本宽度
        Paint.FontMetrics fm = textPaint.getFontMetrics(); // 获取字体度量信息
        if ((x + labelTextW) >= ScopeBase.getWidth()) { // 检查右边界
            x = ScopeBase.getWidth() - (int) labelTextW; // 限制不超过屏幕右边界
        }
        if (x <= 35) { // 检查左边界
            x = 35; // 限制不小于35像素
        }
        labelRect.left = x; // 更新矩形左边界
        labelRect.top = (int) Math.round(getY() - (resBitmap[WaveManage_YT.CH_S].getHeight() - 4)); // 更新矩形上边界
        labelRect.right = Math.round(x + labelTextW); // 更新矩形右边界
        labelRect.bottom = (int) Math.round(getY() + (resBitmap[WaveManage_YT.CH_S].getHeight() - 4)); // 更新矩形下边界
        draw(); // 触发重绘
        CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_LABEL_POSITION + getLineNameID(), String.valueOf(labelRect.left)); // 保存位置到缓存
    }

    // ================================ IWorkMode接口实现区域 ================================

    /**
     * 获取文本高度
     * 
     * 【功能说明】
     * 测量文本的高度，用于布局计算
     * 
     * 【参数说明】
     * @param text 要测量的文本内容
     * 
     * 【返回值】
     * @return 文本高度（像素），如果文本为空则返回0
     * 
     * 【处理逻辑】
     * 如果文本为空或null，使用标准字母表进行测量
     */
    private int getTextHeight(String text) {
        if (!StrUtil.isEmpty(text)) { // 检查文本是否为空
            text = "abcdefghijklmnopqrstuvwxyz"; // 使用标准字母表进行测量
        } else {
            return 0; // 空文本返回0
        }
        paint.getTextBounds(text, 0, text.length(), rect); // 获取文本边界
        int w = rect.width(); // 获取宽度（未使用）
        int h = rect.height(); // 获取高度
        return h; // 返回高度
    }

    /**
     * 切换工作模式 - IWorkMode接口实现
     * 
     * 【功能说明】
     * 根据工作模式更新波形的垂直位置
     * 不同工作模式下波形区域高度不同，需要重新计算位置
     * 
     * 【参数说明】
     * @param workMode 目标工作模式，取值：
     *                 - IWorkMode.WorkMode_YT: YT模式
     *                 - IWorkMode.WorkMode_YTZOOM: YT缩放模式
     * 
     * 【调用时机】
     * - 用户切换工作模式时
     * - WorkModeManage通知模式变化时
     * 
     * 【处理逻辑】
     * - YT模式：使用YT通道位置
     * - YTZOOM模式：使用缩放通道位置
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        switch (workMode) {
            case IWorkMode.WorkMode_YT: // YT模式
                setY(Tools.getYTChannelPositionUI(channelId), true); // 设置YT模式下的通道位置
                break;
            case IWorkMode.WorkMode_YTZOOM: // YT缩放模式
                setY(Tools.getZoomChannelPositionUI(channelId), true); // 设置缩放模式下的通道位置
                break;
        }
    }

    // ================================ IWave接口实现区域 ================================

    /**
     * 移动波形线 - IWave接口实现
     * 
     * 【功能说明】
     * 按偏移量移动波形位置（当前未实现，数学/参考波形通常不需要水平移动）
     * 
     * 【参数说明】
     * @param offsetX X方向偏移量（像素）
     * @param offsetY Y方向偏移量（像素）
     */
    @Override
    public void moveLine(int offsetX, int offsetY) {
        // 数学/参考波形不支持水平移动，方法体为空
    }

    /**
     * 绘制波形 - IWave接口实现（Canvas版本）
     * 
     * 【功能说明】
     * 使用Android Canvas绘制波形（当前未实现，使用OpenGL版本）
     * 
     * 【参数说明】
     * @param canvas Android画布对象
     */
    @Override
    public void draw(Canvas canvas) {
        // 当前使用OpenGL版本绘制，此方法保留为空
    }

    /**
     * 绘制波形 - IWave接口实现（OpenGL版本）
     * 
     * 【功能说明】
     * 使用OpenGL绘制波形标签到指定画布
     * 根据Y位置决定绘制位置（标准/向上/向下）
     * 
     * 【参数说明】
     * @param canvas OpenGL画布对象
     * 
     * 【绘制逻辑】
     * 1. 同步锁定bmp，确保线程安全
     * 2. 保存canvas引用用于后续刷新
     * 3. 计算UI坐标
     * 4. 如果位图有变化，通知GL刷新纹理
     * 5. 根据Y位置选择绘制位置
     *    - y < 0: 绘制在顶部
     *    - y > 区域高度: 绘制在底部
     *    - 其他: 绘制在居中位置
     * 
     * 【线程安全】
     * 使用synchronized(bmp)确保多线程访问安全
     */
    @Override
    public void draw(ICanvasGL canvas) {
        try {
            synchronized (bmp) { // 同步锁定位图对象，确保线程安全
                canvasGL = canvas; // 保存画布引用
                int y = (int) Math.round(ScopeBase.changeAccuracy(this.posY * ScopeBase.getToUICoff())); // 转换为UI坐标
                if (isChanageBitmap) canvas.invalidateTextureContent(bmp, null); // 如果位图有变化，刷新纹理
                if (y < 0) { // Y位置超出屏幕上方
                    canvas.drawBitmap(bmp, 0, 0); // 绘制在顶部
                } else if (y > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // Y位置超出屏幕下方
                    canvas.drawBitmap(bmp, 0, GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - bmp.getHeight()); // 绘制在底部
                } else { // Y位置在屏幕范围内
                    canvas.drawBitmap(bmp, 0, y - resBitmap[WaveManage_YT.CH_S].getHeight() / 2); // 绘制在居中位置
                }
                isChanageBitmap = false; // 重置位图变化标识
            }
        } catch (Exception ignored) { // 捕获并忽略异常
            // 忽略绘制异常，避免影响主流程
        }
    }

    /**
     * 重置初始位置 - IWave接口实现
     * 
     * 【功能说明】
     * 将波形位置重置到初始位置（当前未实现）
     */
    @Override
    public void resultIniRect() {
        // 预留方法，当前未实现
    }

    /**
     * 设置通道名称ID - IWave接口实现
     * 
     * 【功能说明】
     * 设置波形的通道标识符
     * 
     * 【参数说明】
     * @param nameId 通道ID，参考TChan常量定义
     *               如：TChan.Math1、TChan.Ref1等
     */
    @Override
    public void setLineNameId(int nameId) {
        this.channelId = nameId; // 保存通道ID
    }

    /**
     * 获取通道名称ID - IWave接口实现
     * 
     * 【功能说明】
     * 返回波形的通道标识符
     * 
     * 【返回值】
     * @return 通道ID
     */
    @Override
    public int getLineNameID() {
        return this.channelId;
    }

    /**
     * 获取X坐标 - IWave接口实现
     * 
     * 【功能说明】
     * 返回波形的X坐标位置（预留字段，当前未使用）
     * 
     * 【返回值】
     * @return X坐标值
     */
    @Override
    public long getX() {
        return this.x;
    }

    /**
     * 获取Y坐标 - IWave接口实现
     * 
     * 【功能说明】
     * 返回波形的Y坐标位置（屏幕像素坐标）
     * 将系统坐标转换为UI坐标后返回
     * 
     * 【返回值】
     * @return Y坐标值（像素）
     */
    @Override
    public double getY() {
        return ScopeBase.changeAccuracy(this.posY * ScopeBase.getToUICoff()); // 转换并返回UI坐标
    }

    /**
     * 获取Y坐标（系统坐标）
     * 
     * 【功能说明】
     * 返回波形的Y坐标位置（系统内部坐标）
     * 使用1000对应的坐标系统
     * 
     * 【返回值】
     * @return Y坐标值（系统坐标）
     */
    public double getPosY() {
        return posY;
    }

    /**
     * 设置X坐标 - IWave接口实现
     * 
     * 【功能说明】
     * 设置波形的X坐标位置（预留字段，当前未使用）
     * 
     * 【参数说明】
     * @param x X坐标值
     */
    @Override
    public void setX(long x) {
        this.x = x; // 保存X坐标
        draw(); // 触发重绘
        // if (this.onMovingWaveEvent!=null) onMovingWaveEvent.OnMovingWave(this);
    }

    /**
     * 设置Y坐标 - IWave接口实现
     * 
     * 【功能说明】
     * 设置波形的Y坐标位置，并触发位置变化回调
     * 
     * 【参数说明】
     * @param y Y坐标值（屏幕像素坐标）
     * 
     * 【处理流程】
     * 1. 保存Y坐标（像素值）
     * 2. 检查并设置坐标转换系数
     * 3. 转换为系统坐标并保存
     * 4. 更新标签位置
     * 5. 触发重绘
     * 6. 触发位置变化回调
     */
    @Override
    public void setY(double y) {
        this.y = y; // 保存Y坐标（像素值）
        if (ScopeBase.getToFPGACoff() == 1.0) { // 检查转换系数是否为默认值
            ScopeBase.setConvertScale(CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_ZONE_HEIGHT)); // 设置转换比例
        }
        this.posY = y * ScopeBase.getToFPGACoff(); // 转换为系统坐标
        changeLabelPos(labelRect.left); // 更新标签位置
        draw(); // 触发重绘
        if (this.onMovingWaveEvent != null) { // 检查是否有回调监听
            onMovingWaveEvent.OnMovingWave(this, x, y, false, false); // 触发位置变化回调
        }
    }

    /**
     * 设置Y坐标（来自EventBus）
     * 
     * 【功能说明】
     * 由EventBus触发的Y坐标设置，不触发EventBus回调
     * 避免EventBus消息循环
     * 
     * 【参数说明】
     * @param y Y坐标值（屏幕像素坐标）
     * 
     * 【与setY的区别】
     * 回调时isFromEventBus参数为true，标识来源
     */
    public void setYFromEventBus(double y) {
        this.y = y; // 保存Y坐标（像素值）
        this.posY = y * ScopeBase.getToFPGACoff(); // 转换为系统坐标
        changeLabelPos(labelRect.left); // 更新标签位置
        draw(); // 触发重绘
        if (this.onMovingWaveEvent != null) { // 检查是否有回调监听
            onMovingWaveEvent.OnMovingWave(this, x, y, false, true); // 触发位置变化回调，标识来自EventBus
        }
    }

    /**
     * 设置Y坐标（内部方法，支持工作模式切换标识）
     * 
     * 【功能说明】
     * 设置Y坐标位置，并标识是否由工作模式切换触发
     * 
     * 【参数说明】
     * @param y Y坐标值（屏幕像素坐标）
     * @param isSwitchWorkMode 是否由工作模式切换触发
     * 
     * 【调用场景】
     * 主要由switchWorkMode方法调用
     */
    private void setY(double y, boolean isSwitchWorkMode) {
        this.y = y; // 保存Y坐标（像素值）
        this.posY = y * ScopeBase.getToFPGACoff(); // 转换为系统坐标
        changeLabelPos(labelRect.left); // 更新标签位置
        draw(); // 触发重绘
        if (this.onMovingWaveEvent != null) { // 检查是否有回调监听
            onMovingWaveEvent.OnMovingWave(this, x, y, isSwitchWorkMode, false); // 触发位置变化回调，标识是否为模式切换
        }
    }

    /**
     * 移动像素偏移 - IWave接口实现
     * 
     * 【功能说明】
     * 按像素偏移量移动波形位置
     * 
     * 【参数说明】
     * @param px Y方向像素偏移量
     * 
     * 【处理逻辑】
     * 1. 获取当前Y位置
     * 2. 加上偏移量
     * 3. 设置新位置
     */
    @Override
    public void movePix(double px) {
        double y = ScopeBase.changeAccuracy(getPosY() * ScopeBase.getToUICoff()); // 获取当前Y位置
        setY(y + px); // 设置新位置（当前位置+偏移量）
    }

    /**
     * 设置波形颜色 - IWave接口实现
     * 
     * 【功能说明】
     * 设置波形标签的显示颜色
     * 
     * 【参数说明】
     * @param color 颜色值（ARGB格式）
     */
    @Override
    public void setColor(int color) {
        this.color = color; // 保存颜色值
    }

    /**
     * 获取波形颜色 - IWave接口实现
     * 
     * 【功能说明】
     * 返回波形标签的显示颜色
     * 
     * 【返回值】
     * @return 颜色值（ARGB格式）
     */
    @Override
    public int getColor() {
        return this.color;
    }

    /**
     * 设置可见性 - IWave接口实现
     * 
     * 【功能说明】
     * 设置波形的显示/隐藏状态
     * 
     * 【参数说明】
     * @param visible true: 显示波形; false: 隐藏波形
     */
    @Override
    public void setVisible(boolean visible) {
        this.visible = visible; // 保存可见性状态
        draw(); // 触发重绘
    }

    /**
     * 获取可见性 - IWave接口实现
     * 
     * 【功能说明】
     * 返回波形的显示/隐藏状态
     * 
     * 【返回值】
     * @return true: 波形可见; false: 波形隐藏
     */
    @Override
    public boolean getVisible() {
        return this.visible;
    }

    /**
     * 设置选中状态 - IWave接口实现
     * 
     * 【功能说明】
     * 设置波形的选中状态，并触发选中状态变化回调
     * 
     * 【参数说明】
     * @param selected true: 选中波形; false: 取消选中
     * 
     * 【处理流程】
     * 1. 保存选中状态
     * 2. 触发重绘（更新显示样式）
     * 3. 如果选中，触发选中状态变化回调
     */
    @Override
    public void setSelected(boolean selected) {
        this.selected = selected; // 保存选中状态
        draw(); // 触发重绘
        if (selected) { // 如果是选中操作
            if (onSelectChangeEvent != null) onSelectChangeEvent.OnSelectChange(this, true); // 触发选中状态变化回调
        }
    }

    /**
     * 获取选中状态 - IWave接口实现
     * 
     * 【功能说明】
     * 返回波形的选中状态
     * 
     * 【返回值】
     * @return true: 波形已选中; false: 波形未选中
     */
    @Override
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * 设置选中状态变化监听器 - IWave接口实现
     * 
     * 【功能说明】
     * 注册选中状态变化事件回调接口
     * 
     * 【参数说明】
     * @param onSelectChangeEvent 选中状态变化事件监听器
     */
    @Override
    public void setOnSelectChangeEvent(OnSelectChangeEvent onSelectChangeEvent) {
        this.onSelectChangeEvent = onSelectChangeEvent; // 保存回调引用
    }

    /**
     * 设置波形移动监听器 - IWave接口实现
     * 
     * 【功能说明】
     * 注册波形移动事件回调接口
     * 
     * 【参数说明】
     * @param onMovingWaveEvent 波形移动事件监听器
     */
    @Override
    public void setOnMovingWaveEvent(OnMovingWaveEvent onMovingWaveEvent) {
        this.onMovingWaveEvent = onMovingWaveEvent; // 保存回调引用
    }

    // ================================ 辅助方法区域 ================================

    /**
     * 文本边界矩形
     * 用于测量文本尺寸
     */
    private Rect rect = new Rect();

    /**
     * 判断触摸点是否在波形上
     * 
     * 【功能说明】
     * 检测指定坐标点是否落在波形标签区域内
     * 用于触摸事件处理和波形选择
     * 
     * 【参数说明】
     * @param x 触摸点X坐标
     * @param y 触摸点Y坐标
     * 
     * 【返回值】
     * @return true: 触摸点在波形上; false: 触摸点不在波形上
     * 
     * 【处理逻辑】
     * 1. 获取波形Y位置
     * 2. 处理边界情况（超出屏幕）
     * 3. 构建检测矩形
     * 4. 判断点是否在矩形内
     */
    public boolean selectCursor(int x, int y) {
        boolean selected = false; // 初始化选中结果
        int temY = (int) Math.round(this.y); // 获取Y位置
        if (temY < 0) temY = 0 + bmp.getHeight(); // 处理超出上方的情况
        else if (temY > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // 处理超出下方的情况
            temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - bmp.getHeight();
        }
        rect.set(0, temY - resBitmap[WaveManage_YT.CH_S].getHeight() / 2, bmp.getWidth(), bmp.getHeight() + temY - resBitmap[WaveManage_YT.CH_S].getHeight() / 2); // 构建检测矩形
        selected = rect.contains(x, y); // 判断点是否在矩形内
        return selected; // 返回选中结果
    }

    /**
     * 刷新波形显示
     * 
     * 【功能说明】
     * 强制重新绘制波形标签
     * 用于外部触发刷新
     */
    public void refresh() {
        draw(); // 触发重绘
    }

    /**
     * 获取标签矩形区域
     * 
     * 【功能说明】
     * 返回标签的显示矩形区域
     * 用于触摸检测和位置管理
     * 
     * 【返回值】
     * @return 标签矩形区域对象
     */
    public Rect getLabelRect() {
        return labelRect;
    }
}