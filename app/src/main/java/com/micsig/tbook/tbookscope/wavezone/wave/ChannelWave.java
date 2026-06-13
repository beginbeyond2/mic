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
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * 通道波形类 - 示波器通道波形标签和位置指示器的核心实现类
 * 
 * 【模块定位】
 * - 所属模块：wavezone.wave（波形显示区域-波形子模块）
 * - 核心职责：管理示波器通道的波形标签显示、位置指示器、标签拖动和位置管理
 * - 架构层级：显示层组件，实现IWave和IWorkMode接口
 * 
 * 【核心职责】
 * 1. 通道波形标签显示：显示通道标签（如CH1、CH2等），支持选中/未选中状态切换
 * 2. 通道位置指示器：显示通道位置指示器，支持上箭头/下箭头/正常三种状态
 * 3. 通道标签位置管理：管理标签的水平位置，支持拖动和缓存
 * 4. 工作模式切换：支持YT/YTZOOM/XY三种工作模式的切换
 * 5. 通道位置移动：支持通道垂直位置的移动和调整
 * 6. 可见性和选中状态管理：管理通道的显示/隐藏和选中状态
 * 
 * 【架构图】
 * ┌─────────────────────────────────────────────────────────────┐
 * │                WaveManage_YT（波形管理器）                    │
 * │                   (波形显示核心)                             │
 * │  - 管理所有通道波形                                          │
 * │  - 协调波形显示和交互                                        │
 * │  - 处理用户操作事件                                          │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 创建和管理
 * ┌─────────────────────────────────────────────────────────────┐
 * │                  ChannelWave（通道波形类）                    │
 * │                (通道波形标签和位置指示器)                      │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  核心属性                                            │  │
 * │  │  - channelId: 通道ID                                │  │
 * │  │  - selected: 选中状态                                │  │
 * │  │  - visible: 可见性                                  │  │
 * │  │  - x, y: 屏幕坐标位置                               │  │
 * │  │  - posY: 逻辑位置（1000对应位置）                    │  │
 * │  │  - label: 通道标签文本                              │  │
 * │  │  - labelRect: 标签矩形区域                          │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  显示状态                                            │  │
 * │  │  - 选中状态：显示高亮标签                            │  │
 * │  │  - 未选中状态：显示普通标签                          │  │
 * │  │  - 上箭头：通道位置在屏幕上方                        │  │
 * │  │  - 下箭头：通道位置在屏幕下方                        │  │
 * │  │  - 正常：通道位置在屏幕内                            │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  核心方法                                            │  │
 * │  │  - draw(): 绘制通道波形标签                          │  │
 * │  │  - setY(): 设置通道垂直位置                          │  │
 * │  │  - switchWorkMode(): 切换工作模式                    │  │
 * │  │  - changeLabelPos(): 改变标签水平位置                │  │
 * │  │  - selectCursor(): 判断点击是否在标签区域            │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 实现接口
 * ┌─────────────────────────────────────────────────────────────┐
 * │              IWave + IWorkMode（接口层）                      │
 * │  - IWave: 波形显示接口，定义波形的基本操作                  │
 * │  - IWorkMode: 工作模式接口，定义模式切换方法                │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * 【通道波形显示流程】
 * ┌─────────────┐
 * │ 用户操作    │
 * │ (拖动/选择) │
 * └──────┬──────┘
 *        ↓
 * ┌─────────────┐
 * │ WaveManage  │
 * │ 调用setY()  │
 │ 或setSelected()│
 * └──────┬──────┘
 *        ↓
 * ┌─────────────┐
 * │ ChannelWave │
 * │ 更新位置    │
 * │ 更新状态    │
 * └──────┬──────┘
 *        ↓
 * ┌─────────────┐
 * │ draw()方法  │
 * │ 绘制标签    │
 * │ 绘制指示器  │
 * └──────┬──────┘
 *        ↓
 * ┌─────────────┐
 * │ OpenGL渲染  │
 * │ 显示到屏幕  │
 * └─────────────┘
 * 
 * 【依赖关系】
 * 实现接口：
 * - IWave：波形显示接口，定义波形的基本操作方法
 * - IWorkMode：工作模式接口，定义模式切换方法
 * 
 * 被管理类：
 * - WaveManage_YT：YT模式波形管理器，创建和管理ChannelWave实例
 * - WaveManage_YTZoom：YT缩放模式波形管理器
 * - WaveManage_XY：XY模式波形管理器
 * 
 * 依赖工具类：
 * - ScopeBase：示波器基础类，提供坐标转换和精度处理
 * - Tools：工具类，提供通道范围计算和位置转换
 * - CacheUtil：缓存工具类，保存标签位置缓存
 * - GlobalVar：全局变量类，获取波形区域高度
 * - WorkModeManage：工作模式管理器，获取当前工作模式
 * - TChan：通道类，获取通道颜色
 * - StrUtil：字符串工具类
 * 
 * 【设计模式】
 * 1. 观察者模式：
 *    - OnSelectChangeEvent：选中状态变化事件接口
 *    - OnMovingWaveEvent：波形移动事件接口
 *    - 当通道状态变化时通知观察者
 * 
 * 2. 状态模式：
 *    - 根据选中状态（selected）显示不同的标签样式
 *    - 根据位置状态（上箭头/下箭头/正常）显示不同的指示器
 * 
 * 3. 策略模式：
 *    - 根据工作模式（YT/YTZOOM/XY）采用不同的位置计算策略
 * 
 * 【使用场景】
 * 1. YT模式：显示时域波形通道标签，支持垂直拖动调整位置
 * 2. YTZOOM模式：显示YT缩放模式通道标签，支持在缩放窗口中调整位置
 * 3. XY模式：显示XY模式通道标签，位置固定
 * 4. 通道选择：用户点击选择通道时，标签高亮显示
 * 5. 通道隐藏：用户隐藏通道时，标签不显示
 * 
 * 【关键算法】
 * 1. 位置有效性检查算法（isYNoValid）：
 *    - 计算通道可移动范围
 *    - 限制Y坐标在有效范围内
 *    - 考虑工作模式差异
 * 
 * 2. 标签位置自适应算法（draw）：
 *    - 当通道在屏幕上方时，显示上箭头指示器
 *    - 当通道在屏幕下方时，显示下箭头指示器
 *    - 当通道在屏幕内时，显示正常指示器
 *    - 标签位置自动调整避免超出屏幕
 * 
 * 【性能考虑】
 * 1. Bitmap缓存：使用Bitmap缓存标签图像，避免重复创建
 * 2. 同步保护：使用synchronized保护Bitmap的并发访问
 * 3. 懒加载：标签位置从缓存中读取，避免重复计算
 * 4. OpenGL纹理更新：使用isChangeBitmap标记避免不必要的纹理更新
 * 
 * 【注意事项】
 * 1. 线程安全：draw()方法使用synchronized保护Bitmap访问
 * 2. 内存管理：Bitmap需要在适当时机释放
 * 3. 坐标转换：注意UI坐标和FPGA坐标的转换
 * 4. 工作模式：不同工作模式下位置计算方式不同
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/5/19
 * @see IWave
 * @see IWorkMode
 * @see WaveManage_YT
 */

public class ChannelWave implements IWave, IWorkMode {
    /**
     * 日志标签
     */
    private static final String TAG = "ChannelWave";

    //region  属性
    /**
     * 通道选中状态
     * true: 选中，显示高亮标签
     * false: 未选中，显示普通标签
     */
    private boolean selected;
    
    /**
     * 通道可见性
     * true: 可见，显示标签和指示器
     * false: 不可见，不显示标签和指示器
     */
    private boolean visible;
    
    /**
     * 通道ID
     * 标识通道的唯一ID，对应TChan中的通道常量
     * 如：TChan.Ch1、TChan.Ch2等
     */
    private int channelId;
    
    /**
     * 通道X坐标位置（屏幕像素）
     * 目前未使用，保留用于扩展
     */
    private long x;
    
    /**
     * 通道Y坐标位置（屏幕像素）
     * 表示通道在屏幕上的实际像素位置
     */
    private double y;
    
    /**
     * 通道逻辑Y位置
     * 使用1000作为基准单位的位置值
     * 用于内部计算和存储，需要转换为屏幕坐标
     */
    private double posY;
    
    /**
     * 波形移动事件监听器
     * 当通道位置移动时触发，通知外部管理器
     */
    private OnMovingWaveEvent onMovingWaveEvent;
    
    /**
     * 选中状态变化事件监听器
     * 当通道选中状态变化时触发，通知外部管理器
     */
    private OnSelectChangeEvent onSelectChangeEvent;
    //endregion


    //region  私有变量
    /**
     * 应用上下文
     * 用于获取资源和颜色值
     */
    private Context context= App.get().getApplicationContext();
    
    /**
     * 资源位图数组
     * 存储不同状态下的通道指示器位图
     * 索引对应WaveManage_YT中的常量：
     * - CH_N: 未选中正常状态
     * - CH_N_UP: 未选中上箭头状态
     * - CH_N_DOWN: 未选中下箭头状态
     * - CH_S: 选中正常状态
     * - CH_S_UP: 选中上箭头状态
     * - CH_S_DOWN: 选中下箭头状态
     */
    private Bitmap[] resBitmap ;
    
    /**
     * 绘制缓冲位图
     * 用于绘制通道标签和指示器的缓冲区
     * 宽度：1800像素
     * 高度：指示器高度+10像素
     */
    private final Bitmap bmp;
    
    /**
     * 画布对象
     * 用于在bmp上绘制标签和指示器
     */
    private Canvas mCanvas;
    
    /**
     * 绘制画笔
     * 用于绘制文本和位图
     */
    private Paint paint;
    
    /**
     * 文本绘制画笔
     * 用于绘制标签文本的描边效果
     */
    private TextPaint textPaint;
    
    /**
     * 位图变化标记
     * true: 位图已更新，需要刷新OpenGL纹理
     * false: 位图未变化，无需刷新纹理
     */
    private boolean isChangeBitmap = false;
    
    /**
     * OpenGL画布引用
     * 用于刷新纹理内容
     */
    private ICanvasGL canvasGL;
    
    /**
     * 刷新OpenGL纹理
     * 当位图内容更新时调用，通知OpenGL刷新纹理
     */
    public void onRefresh(){
        if(canvasGL != null){
            canvasGL.onRefreshTexture();
        }
    }
    
    /**
     * 通道标签文本
     * 显示在指示器旁边的标签文本，如"CH1"、"CH2"等
     */
    private String label;
    
    /**
     * 标签矩形区域
     * 用于标签的触摸检测和位置管理
     */
    private final Rect labelRect = new Rect();

    //endregion
    
    /**
     * 构造函数 - 初始化通道波形对象
     * 
     * 【功能说明】
     * 创建通道波形对象，初始化绘制资源和缓冲区
     * 
     * 【参数说明】
     * @param resBitmap 资源位图数组，包含不同状态下的指示器位图
     *                  索引对应WaveManage_YT中的常量
     * 
     * 【调用时机】
     * - WaveManage_YT初始化通道时调用
     * - 创建新的通道波形实例时调用
     * 
     * 【实现细节】
     * 1. 保存资源位图引用
     * 2. 创建缓冲位图（1800x高度+10）
     * 3. 初始化画布和画笔
     * 4. 设置文本样式（黑色、20sp、描边）
     * 5. 初始化标签为空字符串
     * 6. 执行首次绘制
     */
    public ChannelWave(Bitmap[] resBitmap) {
        this.resBitmap = resBitmap; // 保存资源位图引用
        bmp = Bitmap.createBitmap(1800, resBitmap[1].getHeight() + 10, Bitmap.Config.ARGB_8888); // 创建缓冲位图，宽度1800，高度为指示器高度+10

        mCanvas = new Canvas(bmp); // 创建画布对象，绑定到缓冲位图
        paint = new Paint(); // 创建绘制画笔
        paint.setTextSize(20); // 设置文本大小为20
        paint.setAntiAlias(true); // 启用抗锯齿
        paint.setDither(true); // 启用抖动

        textPaint=new TextPaint(); // 创建文本画笔
        textPaint.setColor(Color.BLACK); // 设置文本颜色为黑色
        textPaint.setTextSize(20); // 设置文本大小为20
        textPaint.setStyle(Paint.Style.STROKE); // 设置文本样式为描边
        textPaint.setStrokeWidth(4); // 设置描边宽度为4

        label = ""; // 初始化标签为空字符串
        draw(); // 执行首次绘制
    }

    /**
     * 初始化标签矩形区域
     * 
     * 【功能说明】
     * 根据标签文本和缓存的位置信息，初始化标签的矩形区域
     * 用于标签的触摸检测和位置管理
     * 
     * 【调用时机】
     * - 通道初始化时调用
     * - 标签位置需要重新计算时调用
     * 
     * 【实现细节】
     * 1. 测量标签文本宽度
     * 2. 从缓存中读取标签水平位置
     * 3. 计算标签矩形的四个边界
     *    - left: 缓存的水平位置
     *    - top: Y坐标 - 指示器高度 + 4
     *    - right: left + 文本宽度
     *    - bottom: Y坐标 + 指示器高度 - 4
     */
    public void initLabelRect() {
        float labelTextW = textPaint.measureText(label); // 测量标签文本宽度
        int labelX = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_LABEL_POSITION + getLineNameID()); // 从缓存中读取标签水平位置
        Logger.i("channelWave = " + getLineNameID() + " labelx= " + labelX); // 记录日志
        labelRect.left = labelX; // 设置矩形左边界
        labelRect.top = (int) Math.round(getY() - (resBitmap[WaveManage_YT.CH_S].getHeight() - 4)); // 设置矩形上边界
        labelRect.right = labelX + Math.round(labelTextW); // 设置矩形右边界
        labelRect.bottom = (int) Math.round(getY() + (resBitmap[WaveManage_YT.CH_S].getHeight() - 4)); // 设置矩形下边界
    }


    /**
     * 清除模式 - PorterDuff清除模式
     * 用于清除画布内容
     */
    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    
    /**
     * 源模式 - PorterDuff源模式
     * 用于正常绘制
     */
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

    /**
     * 绘制通道波形标签和指示器 - 核心绘制方法
     * 
     * 【功能说明】
     * 根据通道的可见性、选中状态和位置，绘制相应的标签和指示器
     * 支持三种指示器状态：上箭头、下箭头、正常
     * 
     * 【调用时机】
     * - 构造函数中首次绘制
     * - 通道状态变化时（选中、可见性）
     * - 通道位置变化时（setY）
     * - 标签位置变化时（changeLabelPos）
     * - 工作模式切换时（switchWorkMode）
     * 
     * 【实现流程】
     * 1. 同步锁定缓冲位图
     * 2. 清除画布内容
     * 3. 计算通道Y坐标（转换为UI坐标）
     * 4. 根据可见性决定是否绘制
     * 5. 根据Y坐标位置判断指示器类型：
     *    - y < 0: 显示上箭头指示器
     *    - y > 屏幕高度: 显示下箭头指示器
     *    - 否则: 显示正常指示器
     * 6. 根据选中状态选择指示器样式：
     *    - selected=true: 使用选中样式（高亮）
     *    - selected=false: 使用未选中样式（普通）
     * 7. 绘制指示器位图
     * 8. 计算标签文本位置（自适应避免超出屏幕）
     * 9. 绘制标签文本（描边+填充）
     * 10. 标记位图已更新
     * 11. 刷新OpenGL纹理
     * 
     * 【关键算法】
     * 标签位置自适应算法：
     * - 当通道在屏幕上方时，标签固定在顶部
     * - 当通道在屏幕下方时，标签固定在底部
     * - 当通道在屏幕内时，标签跟随通道位置
     * - 标签位置自动调整避免超出屏幕边界
     */
    private void draw() {
        synchronized (bmp) { // 同步锁定缓冲位图，保证线程安全
            paint.setXfermode(clearMode); // 设置清除模式
            mCanvas.drawPaint(paint); // 清除画布内容
            paint.setXfermode(srcMode); // 恢复源模式
            int y = (int) Math.round(ScopeBase.changeAccuracy(getPosY() * ScopeBase.getToUICoff())); // 计算通道Y坐标（转换为UI坐标）
            int h = getTextHeight(label) + (getTextHeight(label) == 0 ? 0 : 3); // 计算文本高度
            if (this.visible) { // 判断通道是否可见
                int ly = Math.round(y - labelRect.top); // 计算标签Y坐标偏移

                int lh = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()); // 获取波形区域高度
                if (this.selected) { // 判断通道是否选中
                    if (y < 0) { // 通道在屏幕上方
                        mCanvas.drawBitmap(resBitmap[WaveManage_YT.CH_S_UP], 0, 10, paint); // 绘制选中上箭头指示器
                        ly = 30; // 设置标签Y坐标
                    } else if (y > lh) { // 通道在屏幕下方
                        mCanvas.drawBitmap(resBitmap[WaveManage_YT.CH_S_DOWN], 0, 10, paint); // 绘制选中下箭头指示器
                        ly = 38; // 设置标签Y坐标
                    } else { // 通道在屏幕内
                        mCanvas.drawBitmap(resBitmap[WaveManage_YT.CH_S], 0, 10, paint); // 绘制选中正常指示器
                        ly += 10; // 调整标签Y坐标
                        int h2 = resBitmap[WaveManage_YT.CH_S].getHeight()/2; // 获取指示器高度的一半
                        if(y < h2){ // 通道靠近屏幕顶部
                            ly += h2 - y; // 向下调整标签位置
                        }else if( y > (lh - h2)){ // 通道靠近屏幕底部
                            ly -= y - (lh - h2); // 向上调整标签位置
                        }
                    }
                    paint.setColor(TChan.getChannelColor(context, channelId)); // 设置画笔颜色为通道颜色
                    mCanvas.drawText(label, labelRect.left, ly, textPaint); // 绘制标签文本描边
                    mCanvas.drawText(label, labelRect.left, ly, paint); // 绘制标签文本填充
                } else { // 通道未选中
                    if (y < 0) { // 通道在屏幕上方
                        mCanvas.drawBitmap(resBitmap[WaveManage_YT.CH_N_UP], 0, 10, paint); // 绘制未选中上箭头指示器
                        ly = 30; // 设置标签Y坐标
                    } else if (y > lh) { // 通道在屏幕下方
                        mCanvas.drawBitmap(resBitmap[WaveManage_YT.CH_N_DOWN], 0, 10, paint); // 绘制未选中下箭头指示器
                        ly = 38; // 设置标签Y坐标
                    } else { // 通道在屏幕内
                        mCanvas.drawBitmap(resBitmap[WaveManage_YT.CH_N], 0, 10, paint); // 绘制未选中正常指示器
                        ly += 10; // 调整标签Y坐标
                        int h2 = resBitmap[WaveManage_YT.CH_S].getHeight()/2; // 获取指示器高度的一半
                        if(y < h2){ // 通道靠近屏幕顶部
                            ly += h2 - y; // 向下调整标签位置
                        }else if( y > (lh - h2)){ // 通道靠近屏幕底部
                            ly -= y - (lh - h2); // 向上调整标签位置
                        }
                    }
                    paint.setColor(TChan.getChannelColor(context, channelId)); // 设置画笔颜色为通道颜色
                    mCanvas.drawText(label, labelRect.left, ly, textPaint); // 绘制标签文本描边
                    mCanvas.drawText(label, labelRect.left, ly, paint); // 绘制标签文本填充
                }
            }
            isChangeBitmap = true; // 标记位图已更新
            onRefresh(); // 刷新OpenGL纹理
        }
    }

    /**
     * 改变标签水平位置
     * 
     * 【功能说明】
     * 改变通道标签的水平位置，并保存到缓存中
     * 位置会被限制在屏幕范围内（40到屏幕宽度-标签宽度）
     * 
     * 【参数说明】
     * @param x 新的标签水平位置（像素坐标）
     * 
     * 【调用时机】
     * - 用户拖动标签时调用
     * - setY方法中调用，更新标签位置
     * - setLabel方法中调用，初始化标签位置
     * 
     * 【实现细节】
     * 1. 测量标签文本宽度
     * 2. 限制位置在屏幕范围内
     * 3. 更新标签矩形区域
     * 4. 重新绘制标签
     * 5. 保存位置到缓存
     */
    public void changeLabelPos(int x) {
        float labelTextW = textPaint.measureText(label); // 测量标签文本宽度
        Paint.FontMetrics fm = textPaint.getFontMetrics(); // 获取字体度量
        if ((x + labelTextW) >= ScopeBase.getWidth()) { // 判断是否超出右边界
            x = ScopeBase.getWidth() - (int) labelTextW; // 限制在右边界内
        }
        if (x <= 40) { // 判断是否超出左边界
            x = 40; // 限制在左边界内
        }
        labelRect.left = x; // 更新矩形左边界
        labelRect.top = (int) Math.round(getY() - (resBitmap[WaveManage_YT.CH_S].getHeight() - 4)); // 更新矩形上边界
        labelRect.right = Math.round(x + labelTextW); // 更新矩形右边界
        labelRect.bottom = (int) Math.round(getY() + (resBitmap[WaveManage_YT.CH_S].getHeight() - 4)); // 更新矩形下边界
        draw(); // 重新绘制标签
        CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_LABEL_POSITION + getLineNameID(), String.valueOf(labelRect.left)); // 保存位置到缓存
    }

    /**
     * 获取文本宽度
     * 
     * 【功能说明】
     * 测量文本的宽度
     * 
     * 【参数说明】
     * @param text 要测量的文本
     * 
     * 【返回值】
     * @return 文本宽度（像素）
     */
    private int getTextWidth(String text) {
        paint.getTextBounds(text, 0, text.length(), rect); // 获取文本边界
        int w = rect.width(); // 获取宽度
        int h = rect.height(); // 获取高度（未使用）
        return w; // 返回宽度
    }

    /**
     * 获取文本高度
     * 
     * 【功能说明】
     * 测量文本的高度
     * 如果文本为空，使用默认字母字符串测量
     * 
     * 【参数说明】
     * @param text 要测量的文本，如果为空则使用默认字母
     * 
     * 【返回值】
     * @return 文本高度（像素），如果文本为空则返回0
     */
    private int getTextHeight(String text) {
        if (!StrUtil.isEmpty(text)) { // 判断文本是否为空
            text = "abcdefghijklmnopqrstuvwxyz"; // 使用默认字母字符串
        } else {
            return 0; // 文本为空，返回0
        }
        paint.getTextBounds(text, 0, text.length(), rect); // 获取文本边界
        int w = rect.width(); // 获取宽度（未使用）
        int h = rect.height(); // 获取高度
        return h; // 返回高度
    }

    //region IWorkMode接口

    /**
     * 切换工作模式 - IWorkMode接口实现
     * 
     * 【功能说明】
     * 根据工作模式切换通道的位置和显示状态
     * 不同工作模式下，通道位置的计算方式不同
     * 
     * 【参数说明】
     * @param workMode 目标工作模式，取值范围：
     *                 - IWorkMode.WorkMode_YT: YT模式
     *                 - IWorkMode.WorkMode_YTZOOM: YT缩放模式
     *                 - IWorkMode.WorkMode_XY: XY模式
     * 
     * 【调用时机】
     * - WorkModeManage切换工作模式时调用
     * - 用户切换工作模式时触发
     * 
     * 【实现细节】
     * 1. YT模式：从YT模式位置缓存中读取位置
     * 2. YTZOOM模式：从缩放模式位置缓存中读取位置
     * 3. XY模式：标记位图更新，刷新纹理
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {

        switch (workMode) {
            case IWorkMode.WorkMode_YT: // YT模式
                setY(Tools.getYTChannelPositionUI(channelId), true); // 设置YT模式位置
                break;
            case IWorkMode.WorkMode_YTZOOM: // YT缩放模式
                setY(Tools.getZoomChannelPositionUI(channelId), true); // 设置缩放模式位置
                break;
            case IWorkMode.WorkMode_XY: // XY模式
                this.isChangeBitmap = true; // 标记位图已更新
                onRefresh(); // 刷新纹理
                break;
        }

    }


    //endregion

    //region  IWave接口实现部分

    /**
     * 移动波形线 - IWave接口实现（未使用）
     * 
     * 【功能说明】
     * 移动波形线的位置
     * 当前实现为空，通道波形不支持水平移动
     * 
     * 【参数说明】
     * @param offsetX X方向偏移量（像素）
     * @param offsetY Y方向偏移量（像素）
     * 
     * 【调用时机】
     * - IWave接口要求实现，当前未使用
     */
    @Override
    public void moveLine(int offsetX, int offsetY) {

    }


    /**
     * 绘制波形 - IWave接口实现（Canvas版本，未使用）
     * 
     * 【功能说明】
     * 使用Canvas绘制波形
     * 当前实现为空，使用OpenGL版本绘制
     * 
     * 【参数说明】
     * @param canvas Canvas画布对象
     * 
     * 【调用时机】
     * - IWave接口要求实现，当前未使用
     * - 实际使用draw(ICanvasGL canvas)方法
     */
    @Override
    public void draw(Canvas canvas) {
//        synchronized (bmp) {
//            if (y <= bmp.getHeight()) {
//                canvas.drawBitmap(bmp, 0, 0, paint);
//            } else if (y >= GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - bmp.getHeight()) {
//                canvas.drawBitmap(bmp, 0, GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - bmp.getHeight(), paint);
//            } else {
//                canvas.drawBitmap(bmp, 0, y - resBitmap[WaveManage_YT.CH_S].getHeight() / 2, paint);
//            }
//        }
    }

    /**
     * 绘制波形 - IWave接口实现（OpenGL版本）
     * 
     * 【功能说明】
     * 使用OpenGL绘制通道波形标签和指示器
     * 根据通道位置和波形区域高度，计算绘制位置
     * 
     * 【参数说明】
     * @param canvas ICanvasGL画布对象，OpenGL渲染画布
     * 
     * 【调用时机】
     * - WaveManage_YT渲染波形时调用
     * - 每帧渲染时调用
     * 
     * 【实现细节】
     * 1. 同步锁定缓冲位图
     * 2. 保存OpenGL画布引用
     * 3. 计算通道Y坐标（考虑工作模式）
     * 4. 如果位图已更新，刷新纹理
     * 5. 根据Y坐标位置计算绘制位置：
     *    - y < 0: 绘制在顶部
     *    - y > 波形区域高度: 绘制在底部
     *    - 否则: 绘制在通道位置
     * 6. 重置位图更新标记
     */
    @Override
    public void draw(ICanvasGL canvas) {
        try {
            synchronized (bmp) { // 同步锁定缓冲位图
                canvasGL = canvas; // 保存OpenGL画布引用
                int y = (int) Math.round(ScopeBase.changeAccuracy(getPosY() * ScopeBase.getToUICoff())); // 计算通道Y坐标
                if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) { // 判断是否为XY模式
                    y = (int) Math.round(getPosY()); // XY模式直接使用posY
                }
                if (isChangeBitmap) canvas.invalidateTextureContent(bmp,null); // 如果位图已更新，刷新纹理
                if (y < 0) { // 通道在屏幕上方
                    canvas.drawBitmap(bmp, 0, -10); // 绘制在顶部
                } else if (y > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // 通道在屏幕下方
                    canvas.drawBitmap(bmp, 0, GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - bmp.getHeight()); // 绘制在底部
                } else { // 通道在屏幕内
                    canvas.drawBitmap(bmp, 0, -10 + y - resBitmap[WaveManage_YT.CH_S].getHeight() / 2); // 绘制在通道位置
                }
                isChangeBitmap = false; // 重置位图更新标记
            }
        } catch (Exception ignored) { // 捕获异常，忽略处理

        }
    }

    /**
     * 重置初始位置 - IWave接口实现（未使用）
     * 
     * 【功能说明】
     * 重置波形到初始位置（50%位置）
     * 当前实现为空
     * 
     * 【调用时机】
     * - IWave接口要求实现，当前未使用
     */
    @Override
    public void resultIniRect() {

    }

    /**
     * 设置通道ID - IWave接口实现
     * 
     * 【功能说明】
     * 设置通道的唯一标识ID
     * 
     * 【参数说明】
     * @param nameId 通道ID，对应TChan中的通道常量
     *               如：TChan.Ch1、TChan.Ch2等
     * 
     * 【调用时机】
     * - WaveManage_YT创建通道时调用
     * - 初始化通道时设置
     */
    @Override
    public void setLineNameId(int nameId) {
        this.channelId = nameId; // 设置通道ID
    }

    /**
     * 获取通道ID - IWave接口实现
     * 
     * 【功能说明】
     * 获取通道的唯一标识ID
     * 
     * 【返回值】
     * @return 通道ID，对应TChan中的通道常量
     */
    @Override
    public int getLineNameID() {
        return this.channelId; // 返回通道ID
    }

    /**
     * 获取X坐标 - IWave接口实现
     * 
     * 【功能说明】
     * 获取通道的X坐标位置
     * 当前未使用，保留用于扩展
     * 
     * 【返回值】
     * @return X坐标位置（像素）
     */
    @Override
    public long getX() {
        return this.x; // 返回X坐标
    }

    /**
     * 获取Y坐标 - IWave接口实现
     * 
     * 【功能说明】
     * 获取通道的Y坐标位置（屏幕像素）
     * 将逻辑位置转换为屏幕坐标
     * 
     * 【返回值】
     * @return Y坐标位置（像素）
     */
    @Override
    public double getY() {
        return ScopeBase.changeAccuracy(this.posY * ScopeBase.getToUICoff()); // 将逻辑位置转换为屏幕坐标
    }

    /**
     * 获取逻辑Y位置
     * 
     * 【功能说明】
     * 获取通道的逻辑Y位置（1000基准单位）
     * 
     * 【返回值】
     * @return 逻辑Y位置（1000基准单位）
     */
    public double getPosY() {
        return posY; // 返回逻辑Y位置
    }

    /**
     * 设置X坐标 - IWave接口实现
     * 
     * 【功能说明】
     * 设置通道的X坐标位置
     * 当前未使用，保留用于扩展
     * 
     * 【参数说明】
     * @param x X坐标位置（像素）
     */
    @Override
    public void setX(long x) {
        this.x = x; // 设置X坐标
    }

    /**
     * 设置Y坐标 - IWave接口实现（核心方法）
     * 
     * 【功能说明】
     * 设置通道的Y坐标位置，并进行有效性检查
     * 更新标签位置，触发移动事件
     * 
     * 【参数说明】
     * @param y Y坐标位置（像素）
     * 
     * 【调用时机】
     * - 用户拖动通道时调用
     * - 程序调整通道位置时调用
     * 
     * 【实现细节】
     * 1. 获取通道可移动范围
     * 2. 检查Y坐标有效性，限制在范围内
     * 3. 更新Y坐标
     * 4. 根据工作模式计算逻辑位置
     * 5. 更新标签位置
     * 6. 重新绘制
     * 7. 触发移动事件（isSwitchWorkMode=false, isFromEventBus=false）
     */
    @Override
    public void setY(double y) {
        int chRange = Tools.getChRange(channelId); // 获取通道可移动范围
        y = isYNoValid(y, chRange); // 检查Y坐标有效性
        this.y = y; // 更新Y坐标
        if (ScopeBase.getToFPGACoff() == 1.0) { // 判断是否需要转换
            ScopeBase.setConvertScale(CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_ZONE_HEIGHT)); // 设置转换比例
        }
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) { // 判断是否为XY模式
            this.posY = this.y; // XY模式直接使用Y坐标
        } else {
            this.posY = this.y * ScopeBase.getToFPGACoff(); // 其他模式转换为逻辑位置
        }
        Log.d("Tag.Debug", String.format("ChannelWave.setY: %s , channel: %d", y, getLineNameID())); // 记录日志
        changeLabelPos(labelRect.left); // 更新标签位置
        draw(); // 重新绘制
        if (this.onMovingWaveEvent != null) { // 判断是否有移动事件监听器
            onMovingWaveEvent.OnMovingWave(this, x, y, false, false); // 触发移动事件
        }
    }

    /**
     * 检查Y坐标有效性
     * 
     * 【功能说明】
     * 检查Y坐标是否在通道可移动范围内，如果超出范围则限制到边界
     * 
     * 【参数说明】
     * @param y Y坐标位置（像素）
     * @param range 通道可移动范围（像素）
     * 
     * 【返回值】
     * @return 有效的Y坐标位置（像素）
     * 
     * 【实现细节】
     * 1. 获取屏幕高度（考虑工作模式）
     * 2. 计算中心位置
     * 3. 转换为FPGA坐标
     * 4. 限制在可移动范围内
     * 5. 转换回UI坐标
     */
    private double isYNoValid(double y, int range) {
        int h = ScopeBase.getHeight(); // 获取屏幕高度
        if (WorkModeManage.getInstance().getmWorkMode() == WorkMode_YTZOOM) { // 判断是否为YT缩放模式
            h = ScopeBase.getZoomHeight(); // 使用缩放窗口高度
        }
        h /= 2; // 计算中心位置
        y = y * ScopeBase.getToFPGACoff(); // 转换为FPGA坐标
        if (y < (h - range)) { // 判断是否小于下限
            y = h - range; // 限制到下限
        }
        if (y > (h + range)) { // 判断是否大于上限
            y = h + range; // 限制到上限
        }
        return y * ScopeBase.getToUICoff(); // 转换回UI坐标
    }

    /**
     * 设置Y坐标（工作模式切换专用）
     * 
     * 【功能说明】
     * 设置通道的Y坐标位置，用于工作模式切换
     * 与setY(double y)的区别：触发移动事件时isSwitchWorkMode=true
     * 
     * 【参数说明】
     * @param y Y坐标位置（像素）
     * @param isSwitchWorkMode 是否为工作模式切换（未使用，仅用于区分方法签名）
     * 
     * 【调用时机】
     * - switchWorkMode方法中调用
     * - 工作模式切换时更新通道位置
     */
    private void setY(double y, boolean isSwitchWorkMode) {
        int chRange = Tools.getChRange(channelId); // 获取通道可移动范围
        y = isYNoValid(y, chRange); // 检查Y坐标有效性
        this.y = y; // 更新Y坐标
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) { // 判断是否为XY模式
            this.posY = this.y; // XY模式直接使用Y坐标
        } else {
            this.posY = this.y * ScopeBase.getToFPGACoff(); // 其他模式转换为逻辑位置
        }
        changeLabelPos(labelRect.left); // 更新标签位置
        draw(); // 重新绘制
        if (this.onMovingWaveEvent != null) { // 判断是否有移动事件监听器
            onMovingWaveEvent.OnMovingWave(this, x, y, true, false); // 触发移动事件（isSwitchWorkMode=true）
        }
    }

    /**
     * 从EventBus设置Y坐标
     * 
     * 【功能说明】
     * 从EventBus接收Y坐标更新，设置通道位置
     * 与setY(double y)的区别：触发移动事件时isFromEventBus=true
     * 
     * 【参数说明】
     * @param y Y坐标位置（像素）
     * 
     * 【调用时机】
     * - EventBus接收到位置更新事件时调用
     * - 外部模块更新通道位置时调用
     */
    public void setYFromEventBus(double y) {
        int chRange = Tools.getChRange(channelId); // 获取通道可移动范围
        y = isYNoValid(y, chRange); // 检查Y坐标有效性
        this.y = y; // 更新Y坐标
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) { // 判断是否为XY模式
            this.posY = this.y; // XY模式直接使用Y坐标
        } else {
            this.posY = this.y * ScopeBase.getToFPGACoff(); // 其他模式转换为逻辑位置
        }
        changeLabelPos(labelRect.left); // 更新标签位置
        draw(); // 重新绘制
        if (this.onMovingWaveEvent != null) { // 判断是否有移动事件监听器
            onMovingWaveEvent.OnMovingWave(this, x, y, false, true); // 触发移动事件（isFromEventBus=true）
        }
    }

    /**
     * 设置颜色 - IWave接口实现（未使用）
     * 
     * 【功能说明】
     * 设置通道颜色
     * 当前实现为空，通道颜色由TChan管理
     * 
     * 【参数说明】
     * @param color 颜色值
     */
    @Override
    public void setColor(int color) {

    }

    /**
     * 获取颜色 - IWave接口实现（未使用）
     * 
     * 【功能说明】
     * 获取通道颜色
     * 当前实现返回0，通道颜色由TChan管理
     * 
     * 【返回值】
     * @return 颜色值（当前返回0）
     */
    @Override
    public int getColor() {
        return 0; // 返回0
    }

    /**
     * 设置可见性 - IWave接口实现
     * 
     * 【功能说明】
     * 设置通道的可见性
     * 
     * 【参数说明】
     * @param visible 可见性，true为可见，false为隐藏
     * 
     * 【调用时机】
     * - 用户打开/关闭通道时调用
     * - 程序控制通道显示/隐藏时调用
     */
    @Override
    public void setVisible(boolean visible) {
        this.visible = visible; // 设置可见性
        draw(); // 重新绘制
    }

    /**
     * 获取标签文本
     * 
     * 【功能说明】
     * 获取通道标签文本
     * 
     * 【返回值】
     * @return 标签文本，如"CH1"、"CH2"等
     */
    public String getLabel() {
        return label; // 返回标签文本
    }

    /**
     * 获取标签矩形区域
     * 
     * 【功能说明】
     * 获取标签的矩形区域，用于触摸检测
     * 
     * 【返回值】
     * @return 标签矩形区域
     */
    public Rect getLabelRect() {
        return labelRect; // 返回标签矩形区域
    }

    /**
     * 设置标签文本
     * 
     * 【功能说明】
     * 设置通道标签文本，并更新标签位置
     * 
     * 【参数说明】
     * @param label 标签文本，如"CH1"、"CH2"等
     * 
     * 【调用时机】
     * - WaveManage_YT初始化通道时调用
     * - 通道标签需要更新时调用
     * 
     * 【实现细节】
     * 1. 设置标签文本
     * 2. 从缓存中读取标签位置
     * 3. 更新标签位置
     * 4. 重新绘制
     */
    public void setLabel(String label) {
        this.label = label; // 设置标签文本
        int cacheLabelPos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_LABEL_POSITION + getLineNameID()); // 从缓存中读取标签位置
        if (labelRect.left != cacheLabelPos) labelRect.left = cacheLabelPos; // 如果位置不同，更新位置
        changeLabelPos(labelRect.left); // 更新标签位置
        draw(); // 重新绘制
    }

    /**
     * 获取可见性 - IWave接口实现
     * 
     * 【功能说明】
     * 获取通道的可见性
     * 
     * 【返回值】
     * @return 可见性，true为可见，false为隐藏
     */
    @Override
    public boolean getVisible() {
        return this.visible; // 返回可见性
    }

    /**
     * 设置选中状态 - IWave接口实现
     * 
     * 【功能说明】
     * 设置通道的选中状态
     * 选中后标签会高亮显示
     * 
     * 【参数说明】
     * @param selected 选中状态，true为选中，false为未选中
     * 
     * 【调用时机】
     * - 用户点击选择通道时调用
     * - 程序控制通道选中状态时调用
     * 
     * 【实现细节】
     * 1. 设置选中状态
     * 2. 重新绘制
     * 3. 如果选中，触发选中事件
     */
    @Override
    public void setSelected(boolean selected) {
        this.selected = selected; // 设置选中状态
        draw(); // 重新绘制
        if (selected) { // 判断是否选中
            if (onSelectChangeEvent != null) onSelectChangeEvent.OnSelectChange(this, true); // 触发选中事件
        }
    }

    /**
     * 获取选中状态 - IWave接口实现
     * 
     * 【功能说明】
     * 获取通道的选中状态
     * 
     * 【返回值】
     * @return 选中状态，true为选中，false为未选中
     */
    @Override
    public boolean isSelected() {
        return this.selected; // 返回选中状态
    }

    /**
     * 移动像素 - IWave接口实现
     * 
     * 【功能说明】
     * 根据像素偏移量移动通道位置
     * 
     * 【参数说明】
     * @param px Y方向像素偏移量
     * 
     * 【调用时机】
     * - 用户拖动通道时调用
     * - 程序调整通道位置时调用
     * 
     * 【实现细节】
     * 1. 获取当前Y坐标
     * 2. 计算新Y坐标
     * 3. 调用setY设置新位置
     */
    @Override
    public void movePix(double px) {
        double y = ScopeBase.changeAccuracy(getPosY() * ScopeBase.getToUICoff()); // 获取当前Y坐标
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) { // 判断是否为XY模式
            y = this.y; // XY模式直接使用Y坐标
        }
        setY(y + px); // 设置新Y坐标
    }

    /**
     * 设置选中状态变化事件监听器 - IWave接口实现
     * 
     * 【功能说明】
     * 设置选中状态变化事件监听器
     * 
     * 【参数说明】
     * @param onSelectChangeEvent 选中状态变化事件监听器
     */
    @Override
    public void setOnSelectChangeEvent(OnSelectChangeEvent onSelectChangeEvent) {
        this.onSelectChangeEvent = onSelectChangeEvent; // 设置监听器
    }

    /**
     * 设置波形移动事件监听器 - IWave接口实现
     * 
     * 【功能说明】
     * 设置波形移动事件监听器
     * 
     * 【参数说明】
     * @param onMovingWaveEvent 波形移动事件监听器
     */
    @Override
    public void setOnMovingWaveEvent(OnMovingWaveEvent onMovingWaveEvent) {
        this.onMovingWaveEvent = onMovingWaveEvent; // 设置监听器
    }

    //endregion

    /**
     * 文本边界矩形
     * 用于测量文本宽度和高度
     */
    private Rect rect = new Rect();

    /**
     * 选择光标 - 判断点击是否在标签区域
     * 
     * 【功能说明】
     * 判断点击坐标是否在通道标签区域内
     * 用于判断用户是否点击了通道标签
     * 
     * 【参数说明】
     * @param x 点击X坐标（像素）
     * @param y 点击Y坐标（像素）
     * 
     * 【返回值】
     * @return true: 点击在标签区域内
     *         false: 点击不在标签区域内
     * 
     * 【调用时机】
     * - 用户点击屏幕时调用
     * - 判断是否选中通道时调用
     * 
     * 【实现细节】
     * 1. 获取通道Y坐标
     * 2. 处理边界情况（Y<0或Y>屏幕高度）
     * 3. 计算标签矩形区域
     * 4. 判断点击坐标是否在矩形内
     */
    public boolean selectCursor(int x, int y) {
        boolean selected; // 选择结果
        int temY = (int) Math.round(this.y); // 获取通道Y坐标
        if (temY < 0) temY = 0 + bmp.getHeight(); // 处理Y<0的情况
        else if (temY > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // 处理Y>屏幕高度的情况
            temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - bmp.getHeight(); // 限制在屏幕高度内
        }
        rect.set(0, temY - resBitmap[WaveManage_YT.CH_S].getHeight() / 2, bmp.getWidth(), bmp.getHeight() + temY - resBitmap[WaveManage_YT.CH_S].getHeight() / 2); // 设置标签矩形区域
        selected = rect.contains(x, y); // 判断点击是否在矩形内
        return selected; // 返回选择结果
    }

    /**
     * 刷新通道波形
     * 
     * 【功能说明】
     * 刷新通道波形的显示
     * 重新绘制标签和指示器
     * 
     * 【调用时机】
     * - 通道状态需要更新时调用
     * - 外部模块需要刷新通道显示时调用
     */
    public void refresh() {
        draw(); // 重新绘制
    }
}