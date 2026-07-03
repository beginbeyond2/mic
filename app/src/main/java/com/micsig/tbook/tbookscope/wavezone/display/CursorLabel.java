package com.micsig.tbook.tbookscope.wavezone.display;  // 光标标签显示类所在包

import static com.micsig.tbook.scope.measure.Measure.MeasureType.MEASURE_CURSOR_X1;  // 光标X1测量项类型
import static com.micsig.tbook.scope.measure.Measure.MeasureType.MEASURE_CURSOR_X2;  // 光标X2测量项类型
import static com.micsig.tbook.tbookscope.wavezone.IWorkMode.WorkMode_YTZOOM;  // YT缩放工作模式

import android.content.Context;  // Android上下文
import android.graphics.Bitmap;  // 位图，用于绘制标签纹理
import android.graphics.Canvas;  // 画布，用于绘制标签图形
import android.graphics.Color;  // 颜色常量
import android.graphics.Matrix;  // 矩阵，用于位图旋转
import android.graphics.Paint;  // 画笔
import android.graphics.PorterDuff;  // 混合模式
import android.graphics.PorterDuffXfermode;  // 混合模式封装
import android.graphics.Rect;  // 矩形区域
import android.text.TextPaint;  // 文字画笔（带描边效果）
import android.text.TextUtils;  // 文本工具（截断处理）
import android.util.Log;  // 日志

import com.chillingvan.canvasgl.ICanvasGL;  // OpenGL画布接口
import com.micsig.tbook.scope.Event.EventBase;  // 事件基类
import com.micsig.tbook.scope.Event.EventFactory;  // 事件工厂
import com.micsig.tbook.scope.Event.EventUIObserver;  // UI事件观察者
import com.micsig.tbook.scope.Scope;  // 示波器实例
import com.micsig.tbook.scope.ScopeBase;  // 示波器基础参数
import com.micsig.tbook.scope.channel.BaseChannel;  // 通道基类
import com.micsig.tbook.scope.channel.ChannelFactory;  // 通道工厂
import com.micsig.tbook.scope.channel.RefChannel;  // 参考通道
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 水平轴
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath;  // 数学通道水平轴（FFT）
import com.micsig.tbook.scope.measure.Measure;  // 测量接口
import com.micsig.tbook.scope.measure.MeasureService;  // 测量服务
import com.micsig.tbook.tbookscope.GlobalVar;  // 全局变量
import com.micsig.tbook.tbookscope.R;  // 资源ID
import com.micsig.tbook.tbookscope.middleware.command.Command;  // 命令中间件
import com.micsig.tbook.tbookscope.tools.Tools;  // 通用工具类
import com.micsig.tbook.tbookscope.util.App;  // 应用上下文工具
import com.micsig.tbook.tbookscope.util.CacheUtil;  // 缓存工具
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;  // 工作模式管理
import com.micsig.tbook.tbookscope.wavezone.measure.CurSorMeasureBase;  // 光标测量基类
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;  // 测量管理
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;  // 波形管理
import com.micsig.tbook.ui.util.TBookUtil;  // UI工具
import com.micsig.tbook.ui.wavezone.TChan;  // 通道常量定义

import java.util.HashMap;  // 哈希映射
import java.util.List;  // 列表
import java.util.Map;  // 映射接口

/*
 * +=============================================================================+
 * |                      CursorLabel — 光标测量标签绘制类                        |
 * +=============================================================================+
 * | 模块定位：tbookscope.wavezone.display 显示层                                 |
 * | 核心职责：绘制光标测量参数标签（X1/X2/ΔX/1/ΔX/Y1/Y2/ΔY/ΔV/ΔV/Δt等），       |
 * |          计算光标测量值，处理标签跟随/固定显示模式，绘制光标箭头指示器         |
 * | 架构设计：独立类，由CursorManage持有和调用，监听测量更新事件自动刷新标签       |
 * | 数据流向：                                                                   |
 * |   输入：CursorManage光标位置 → CursorLabel.calDataYT/calDataXY → 计算测量值   |
 * |   输出：CursorLabel.drawMeasure → Bitmap位图 → ICanvasGL OpenGL渲染          |
 * | 依赖关系：Cursor_impIWave, CursorManage, MeasureService, ChannelFactory,     |
 * |           HorizontalAxis, Command, CacheUtil, WorkModeManage, EventFactory   |
 * | 使用场景：示波器光标测量功能中，显示各光标的坐标值、差值、频率、斜率等参数     |
 * +=============================================================================+
 */

/**
 * @auother Liwb
 * @description:
 * @data:2023-11-17 9:19
 */
public class CursorLabel {
    private static final String SPACE = " ";  // 空格分隔符
    //region 绘制出 测量参数显示
    private static final int X1 = 0;  // X1标签索引
    private static final int X2 = 1;  // X2标签索引
    private static final int XDelta = 2;  // ΔX标签索引
    private static final int Y1 = 3;  // Y1标签索引
    private static final int Y2 = 4;  // Y2标签索引
    private static final int YDelta = 5;  // ΔY标签索引
    private static final int XFre = 6;  // 1/ΔX频率标签索引
    private static final int S = 7;  // ΔV/Δt斜率标签索引
    private static final int XArrow = 8;  // X方向箭头矩形索引
    private static final int YArrow = 9;  // Y方向箭头矩形索引

    private static final int X1CursorInteraction = 10;  // X1光标交互测量值索引

    private static final int X2CursorInteraction = 11;  // X2光标交互测量值索引

    private static final int VoltgaeDelta = 12;  // ΔV电压差索引

    private static final int SingleBmp = 13;  // 单独标签位图矩形索引

    private static final int VoltageDivideXDelta = 14;  // ΔV/Δt电压变化率索引


    private static final boolean EnableMeasure = true;  // 是否启用测量功能
    private Bitmap[] bmp = new Bitmap[6];  // 各标签位图数组（常规尺寸）

    private Bitmap[] smallBmps = new Bitmap[6];  // 各标签位图数组（小尺寸，行+列同时显示时）

    private Bitmap[] bigBmps = new Bitmap[6];  // 各标签位图数组（大尺寸，仅列光标时）

    private Bitmap[] tvalBmp = new Bitmap[6];  // 各标签位图数组（T值跟踪模式尺寸）
    private Bitmap rowBmp, colBmp, oldColBmp, singleBmp, smallSingleBmp, middleSingleBmp,tvalueBmp;  // 行箭头位图、列箭头位图、旧列位图、单独标签位图、小单独位图、中单独位图、T值位图
    private Canvas canvas;  // 绘制用画布
    private Paint paint;  // 普通画笔
    private TextPaint textPaint;  // 文字描边画笔
    private HashMap<Integer, String> dataMap = new HashMap();  // 测量数据映射（索引→显示文本）
    private HashMap<Integer, Rect> rectMap = new HashMap<>();  // 标签矩形映射（索引→屏幕矩形区域）
    private List<Cursor_impIWave> cursorList;  // 光标线实例列表
    private boolean isChangeBmp = false;  // 位图内容是否已改变
    private ICanvasGL canvasGL;  // OpenGL画布引用

    /**
     * 通知OpenGL画布刷新纹理
     */
    public void onRefresh() {  // 刷新纹理
        if (canvasGL != null) {  // 画布非空时
            canvasGL.onRefreshTexture();  // 调用OpenGL纹理刷新
        }
    }

    private boolean colVisible = false;  // 列光标是否可见
    private boolean rowVisible = false;  // 行光标是否可见

    private int labelX, labelY, singleLabelX, singleLabelY;  // 标签X/Y坐标、单独标签X/Y坐标
    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);  // 清除混合模式
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);  // 源混合模式
    private Bitmap[] resCursorBmp = new Bitmap[30];  // 光标箭头资源位图数组（30种样式）

    private Context context = App.get().getApplicationContext();  // 应用上下文

    private double xDeltaCalculate = 0.0;  // 保存ΔX计算值（用于ΔV/Δt计算）

    private String xDeltaUnit = "";  // 保存ΔX的单位

    /**
     * 初始化光标箭头资源位图数组
     */
    private void initResCursorBmp() {  // 初始化光标资源位图

        resCursorBmp[0] = Tools.readNineBmp(R.drawable.cursor_ch1_lr);  // 通道1光标箭头
        resCursorBmp[1] = Tools.readNineBmp(R.drawable.cursor_ch2_lr);  // 通道2光标箭头
        resCursorBmp[2] = Tools.readNineBmp(R.drawable.cursor_ch3_lr);  // 通道3光标箭头
        resCursorBmp[3] = Tools.readNineBmp(R.drawable.cursor_ch4_lr);  // 通道4光标箭头
        resCursorBmp[4] = Tools.readNineBmp(R.drawable.cursor_ch5_lr);  // 通道5光标箭头
        resCursorBmp[5] = Tools.readNineBmp(R.drawable.cursor_ch6_lr);  // 通道6光标箭头
        resCursorBmp[6] = Tools.readNineBmp(R.drawable.cursor_ch7_lr);  // 通道7光标箭头
        resCursorBmp[7] = Tools.readNineBmp(R.drawable.cursor_ch8_lr);  // 通道8光标箭头
        resCursorBmp[8] = Tools.readNineBmp(R.drawable.cursor_m_lr);  // 数学通道光标箭头
        resCursorBmp[9] = Tools.readNineBmp(R.drawable.cursor_m_lr);  // 数学通道光标箭头（备用）
        resCursorBmp[10] = Tools.readNineBmp(R.drawable.cursor_m_lr);  // 数学通道光标箭头（备用）
        resCursorBmp[11] = Tools.readNineBmp(R.drawable.cursor_m_lr);  // 数学通道光标箭头（备用）
        resCursorBmp[12] = Tools.readNineBmp(R.drawable.cursor_m_lr);  // 数学通道光标箭头（备用）
        resCursorBmp[13] = Tools.readNineBmp(R.drawable.cursor_m_lr);  // 数学通道光标箭头（备用）
        resCursorBmp[14] = Tools.readNineBmp(R.drawable.cursor_m_lr);  // 数学通道光标箭头（备用）
        resCursorBmp[15] = Tools.readNineBmp(R.drawable.cursor_m_lr);  // 数学通道光标箭头（备用）
        resCursorBmp[16] = Tools.readNineBmp(R.drawable.cursor_r_lr);  // 参考通道光标箭头
        resCursorBmp[17] = Tools.readNineBmp(R.drawable.cursor_r_lr);  // 参考通道光标箭头（备用）
        resCursorBmp[18] = Tools.readNineBmp(R.drawable.cursor_r_lr);  // 参考通道光标箭头（备用）
        resCursorBmp[19] = Tools.readNineBmp(R.drawable.cursor_r_lr);  // 参考通道光标箭头（备用）
        resCursorBmp[20] = Tools.readNineBmp(R.drawable.cursor_r_lr);  // 参考通道光标箭头（备用）
        resCursorBmp[21] = Tools.readNineBmp(R.drawable.cursor_r_lr);  // 参考通道光标箭头（备用）
        resCursorBmp[22] = Tools.readNineBmp(R.drawable.cursor_r_lr);  // 参考通道光标箭头（备用）
        resCursorBmp[23] = Tools.readNineBmp(R.drawable.cursor_r_lr);  // 参考通道光标箭头（备用）
        resCursorBmp[24] = Tools.readNineBmp(R.drawable.cursor_s1_lr);  // 串行通道1光标箭头
        resCursorBmp[25] = Tools.readNineBmp(R.drawable.cursor_s2_lr);  // 串行通道2光标箭头
        resCursorBmp[26] = Tools.readNineBmp(R.drawable.cursor_s3_lr);  // 串行通道3光标箭头
        resCursorBmp[27] = Tools.readNineBmp(R.drawable.cursor_s4_lr);  // 串行通道4光标箭头
        resCursorBmp[28] = Tools.readNineBmp(R.drawable.cursor_chx_lr);  // 通用通道光标箭头
        resCursorBmp[29] = Tools.readNineBmp(R.drawable.cursor_xy_lr);  // XY模式光标箭头
    }

    /**
     * 构造光标标签实例
     *
     * @param curList 光标线实例列表
     */
    public CursorLabel(List<Cursor_impIWave> curList) {  // 构造函数
        this.cursorList = curList;  // 保存光标线列表引用
    }

    /**
     * 初始化测量标签的所有位图、画笔、数据映射和事件监听
     */
    public void initMeasure() {  // 初始化测量标签
        initResCursorBmp();  // 初始化光标箭头资源位图
        for (int i = 0; i < bmp.length; i++) {  // 遍历各标签位图
            if (i == XDelta) {  // ΔX标签需要较大尺寸（显示多行数据）
                bmp[i] = Bitmap.createBitmap(190, 80, Bitmap.Config.ARGB_8888);  // 常规尺寸
                bigBmps[i] = Bitmap.createBitmap(190, 85, Bitmap.Config.ARGB_8888);  // 大尺寸
                smallBmps[i] = Bitmap.createBitmap(190, 75, Bitmap.Config.ARGB_8888);  // 小尺寸
                tvalBmp[i] = Bitmap.createBitmap(190, 50, Bitmap.Config.ARGB_8888);  // T值跟踪尺寸
            } else if (i == YDelta) {  // ΔY标签
                bmp[i] = Bitmap.createBitmap(135, 40, Bitmap.Config.ARGB_8888);  // 常规尺寸
                bigBmps[i] = Bitmap.createBitmap(135, 30, Bitmap.Config.ARGB_8888);  // 大尺寸
                smallBmps[i] = Bitmap.createBitmap(135, 30, Bitmap.Config.ARGB_8888);  // 小尺寸
                tvalBmp[i] = Bitmap.createBitmap(190, 30, Bitmap.Config.ARGB_8888);  // T值跟踪尺寸
            } else {  // X1/X2/Y1/Y2标签
                bmp[i] = Bitmap.createBitmap(135, 40, Bitmap.Config.ARGB_8888);  // 常规尺寸
                bigBmps[i] = Bitmap.createBitmap(135, 43, Bitmap.Config.ARGB_8888);  // 大尺寸
                if(i == Y1 || i == Y2){  // Y1/Y2标签
                    bigBmps[i] = Bitmap.createBitmap(135, 30, Bitmap.Config.ARGB_8888);  // 调整大尺寸高度
                }
                smallBmps[i] = Bitmap.createBitmap(135, 30, Bitmap.Config.ARGB_8888);  // 小尺寸
                tvalBmp[i] = Bitmap.createBitmap(190, 30, Bitmap.Config.ARGB_8888);  // T值跟踪尺寸
            }
        }
        rowBmp = Bitmap.createBitmap(1800, 16, Bitmap.Config.ARGB_8888);  // 行方向箭头位图
        colBmp = Bitmap.createBitmap(64, 1000, Bitmap.Config.ARGB_8888);  // 列方向箭头位图
        singleBmp = Bitmap.createBitmap(185, 80, Bitmap.Config.ARGB_8888);  // 单独标签位图
        //只显示y的情况下用
        smallSingleBmp = Bitmap.createBitmap(200, 62, Bitmap.Config.ARGB_8888);  // 仅Y标签的单独位图
        //只显示x的情况下，以及xy都显示的时候用
        middleSingleBmp = Bitmap.createBitmap(200, 153, Bitmap.Config.ARGB_8888);  // X+Y标签的单独位图

        tvalueBmp = Bitmap.createBitmap(200, 82, Bitmap.Config.ARGB_8888);  // T值标签位图
        canvas = new Canvas();  // 创建画布
        paint = new Paint();  // 创建画笔
        paint.setTextSize(18);  // 设置字体大小
        paint.setAntiAlias(true);  // 开启抗锯齿

        textPaint = new TextPaint();  // 创建文字描边画笔
        textPaint.setTextSize(18);  // 设置字体大小
        textPaint.setAntiAlias(true);  // 开启抗锯齿
        textPaint.setColor(Color.BLACK);  // 设置描边颜色为黑色
        textPaint.setStyle(Paint.Style.STROKE);  // 设置描边样式
        textPaint.setStrokeWidth(4);  // 设置描边宽度

        dataMap.put(X1, "---");  // 初始化X1显示值
        dataMap.put(X2, "---");  // 初始化X2显示值
        dataMap.put(XDelta, "---");  // 初始化ΔX显示值
        dataMap.put(Y1, "---");  // 初始化Y1显示值
        dataMap.put(Y2, "---");  // 初始化Y2显示值
        dataMap.put(YDelta, "---");  // 初始化ΔY显示值
        dataMap.put(XFre, "---");  // 初始化1/ΔX显示值
        dataMap.put(X1CursorInteraction, "---");  // 初始化X1交互测量值
        dataMap.put(X2CursorInteraction, "---");  // 初始化X2交互测量值
        dataMap.put(VoltageDivideXDelta, "---");  // 初始化ΔV/Δt显示值

        dataMap.put(VoltgaeDelta, "---");  // 初始化ΔV显示值

        rectMap.put(X1, new Rect());  // 初始化X1矩形区域
        rectMap.put(X2, new Rect());  // 初始化X2矩形区域
        rectMap.put(XDelta, new Rect());  // 初始化ΔX矩形区域
        rectMap.put(Y1, new Rect());  // 初始化Y1矩形区域
        rectMap.put(Y2, new Rect());  // 初始化Y2矩形区域
        rectMap.put(YDelta, new Rect());  // 初始化ΔY矩形区域
        rectMap.put(XFre, new Rect());  // 初始化1/ΔX矩形区域
        rectMap.put(XArrow, new Rect());  // 初始化X箭头矩形区域
        rectMap.put(YArrow, new Rect());  // 初始化Y箭头矩形区域
        rectMap.put(SingleBmp, new Rect());  // 初始化单独标签矩形区域
        EventFactory.addEventObserver(EventFactory.EVENT_CH_MEASURE_UPDATE, eventUIObserver);  // 注册通道测量更新事件
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_MEASURE_UPDATE, eventUIObserver);  // 注册数学通道测量更新事件
        EventFactory.addEventObserver(EventFactory.EVENT_REF_MEASURE_UPDATE, eventUIObserver);  // 注册参考通道测量更新事件
        labelX = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_YT_CURSOR_LABEL_X);  // 从缓存读取标签X坐标
        labelY = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_YT_CURSOR_LABEL_Y);  // 从缓存读取标签Y坐标
        singleLabelX = 1600;  // 初始化单独标签X坐标
        singleLabelY = 0;  // 初始化单独标签Y坐标
    }

    /**
     * 改变列光标位图高度（波形区高度变化时调用）
     *
     * @param height 新的高度值
     */
    public void changeHeight(int height) {  // 改变高度
        oldColBmp = colBmp;  // 保存旧列位图
        colBmp = Bitmap.createBitmap(64, height, Bitmap.Config.ARGB_8888);  // 创建新高度列位图
        for (Cursor_impIWave c : cursorList) {  // 遍历光标列表
            if (TChan.isCursorRow4(c.getCursorType())) {  // 如果是行光标4（Delta光标）
                c.changeCursorH(height);  // 更新光标高度范围
            }
        }
        drawMeasure();  // 重新绘制测量标签
    }

    /**
     * 绘制测量标签到位图（内部绘制方法）
     * 根据当前工作模式和标签显示模式，分别绘制跟随/固定两种标签样式
     */
    public synchronized void drawMeasure() {  // 同步绘制测量标签到位图
        if (!colVisible && !rowVisible) return;  // 行列光标都不可见则返回
        int chan = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE);  // 获取光标源通道

        boolean bTValueTrace = MeasureManage.getInstance().isCursorTValueTrace();  // 是否T值跟踪模式

        int txtColor = App.get().getResources().getColor(R.color.color_Text_white);  // 获取文本颜色
        if (chan == TChan.AUTO) { //自由选择模式  // 自动选择模式
            chan = WaveManage.get().getCurCh() - 1;  // 使用当前活动通道
        }
        if (chan < 0) chan = resCursorBmp.length - 2; //cursor_chx_lr  // 无效通道使用通用箭头
        //如果yt模式下进行绘制
        String ellipsizedVoltageDivideXDelta = TextUtils.ellipsize(  // 截断ΔV/Δt文本
                dataMap.get(VoltageDivideXDelta),  // 获取原始文本
                textPaint,  // 文字画笔
                175,  // 最大宽度175像素
                TextUtils.TruncateAt.END  // 末尾截断
        ).toString();  // 转为字符串
        dataMap.put(VoltageDivideXDelta,ellipsizedVoltageDivideXDelta);  // 保存截断后的文本
        if (WorkModeManage.getInstance().isYTMode() || WorkModeManage.getInstance().getmWorkMode()==WorkMode_YTZOOM) {  // YT或YT缩放模式
            //如果是标签跟随状态，分为好几个框分别绘制
            if (!CursorManage.getInstance().isLabelNotFollowCursor()) {  // 标签跟随光标模式
                //在跟随光标的情况下，对宽度进行限制
                updateStringWidth();  // 更新字符串宽度限制
                for (int i = 0; i < bmp.length; i++) {  // 遍历各标签
                    if (colVisible && rowVisible ) {  // 行列光标都可见
                        bmp[i] = smallBmps[i];  // 使用小尺寸位图
                    } else {  // 只有一种光标可见
                        if(bTValueTrace){  // T值跟踪模式
                            bmp[i] = tvalBmp[i];  // 使用T值跟踪尺寸位图
                        }else {  // 非T值跟踪模式
                            bmp[i] = bigBmps[i];  // 使用大尺寸位图
                        }
                    }
                    canvas.setBitmap(bmp[i]);  // 设置画布目标位图
                    paint.setXfermode(clearMode);  // 设置清除模式
                    canvas.drawPaint(paint);  // 清空位图
                    paint.setXfermode(srcMode);  // 设置源绘制模式
                    int color = TChan.getChannelColor(context, chan + 1);  // 获取通道颜色
                    Paint strokePaint = new Paint();  // 创建描边画笔
                    strokePaint.setStyle(Paint.Style.STROKE);  // 设置描边样式
                    strokePaint.setStrokeWidth(1);  // 设置线宽1像素
                    strokePaint.setColor(color);  // 设置通道颜色
                    Paint fillPaint = new Paint();  // 创建填充画笔
                    fillPaint.setStyle(Paint.Style.FILL);  // 设置填充样式
                    fillPaint.setColor(Color.argb(180,18,18,18));  // 设置半透明深灰背景色
                    Rect rect = new Rect(1, 1, bmp[i].getWidth()-1, bmp[i].getHeight()-1);  // 计算标签内部矩形
                    canvas.drawRect(rect,fillPaint);  // 绘制填充背景
                    canvas.drawRoundRect(1, 1, bmp[i].getWidth()-1f, bmp[i].getHeight()-1f, 3, 3, strokePaint);  // 绘制圆角描边
                    paint.setAntiAlias(true);  // 开启抗锯齿
                    paint.setStyle(Paint.Style.FILL);  // 设置填充样式
                    paint.setColor(txtColor);  // 设置文本颜色
                    if (i == XDelta) {  // ΔX标签（显示多行：ΔX、1/ΔX、可选S/ΔV/ΔV/Δt）
                        int y = 19;  // 第一行Y坐标
                        if (colVisible == true && rowVisible == true) {  // 行列都可见
                            y = 21;  // 调整行间距
                        }

                        Tools.drawText(canvas, dataMap.get(i), 5, y, bmp[i].getWidth(), paint, textPaint);  // 绘制ΔX文本
                        Tools.drawText(canvas, dataMap.get(XFre), 5, y * 2, bmp[i].getWidth(), paint, textPaint);  // 绘制1/ΔX文本
                        if (colVisible == true && rowVisible == false ) {  // 仅列光标可见
                            if(!bTValueTrace) {  // 非T值跟踪模式
                                Tools.drawText(canvas, dataMap.get(VoltgaeDelta), 5, y * 3, bmp[i].getWidth(), paint, textPaint);  // 绘制ΔV文本
                                Tools.drawText(canvas, dataMap.get(VoltageDivideXDelta), 5, y * 4, bmp[i].getWidth(), paint, textPaint);  // 绘制ΔV/Δt文本
                            }
                        } else if (colVisible && rowVisible) {  // 行列都可见
                            Tools.drawText(canvas, dataMap.get(S), 5, y * 3, bmp[i].getWidth(), paint, textPaint);  // 绘制ΔV/Δt文本
                        }

                    } else if (i == X1) {  // X1标签
                        int x = centerTxtX(bmp[i], dataMap.get(i), paint);  // 计算居中X坐标
                        int y = centerTxtY(bmp[i], dataMap.get(i), paint);  // 计算居中Y坐标
                        if (colVisible == true && rowVisible == false) {  // 仅列光标可见
                            y = 18;  // 调整Y坐标
                        }
                        Tools.drawText(canvas, dataMap.get(i), 5, y, bmp[i].getWidth(), paint, textPaint);  // 绘制X1文本
                        if (colVisible == true && rowVisible == false) {  // 仅列光标可见
                            if(!bTValueTrace) {  // 非T值跟踪模式
                                Tools.drawText(canvas, dataMap.get(X1CursorInteraction), 22, y * 2, bmp[i].getWidth(), paint, textPaint);  // 绘制X1交互测量值
                            }
                        }
                    } else if (i == X2) {  // X2标签
                        int x = centerTxtX(bmp[i], dataMap.get(i), paint);  // 计算居中X坐标
                        int y = centerTxtY(bmp[i], dataMap.get(i), paint);  // 计算居中Y坐标
                        if (colVisible == true && rowVisible == false) {  // 仅列光标可见
                            y = 18;  // 调整Y坐标
                        }
                        Tools.drawText(canvas, dataMap.get(i), 5, y, bmp[i].getWidth(), paint, textPaint);  // 绘制X2文本
                        if (colVisible == true && rowVisible == false) {  // 仅列光标可见
                            if(!bTValueTrace) {  // 非T值跟踪模式
                                Tools.drawText(canvas, dataMap.get(X2CursorInteraction), 22, y * 2, bmp[i].getWidth(), paint, textPaint);  // 绘制X2交互测量值
                            }
                        }
                    } else {  // Y1/Y2/ΔY标签
                        int y = centerTxtY(bmp[i], dataMap.get(i), paint);  // 计算居中Y坐标
                        int x = centerTxtX(bmp[i], dataMap.get(i), paint);  // 计算居中X坐标
                        Tools.drawText(canvas, dataMap.get(i), x, y, bmp[i].getWidth(), paint, textPaint);  // 居中绘制文本
                    }
                }
            } else {  // 标签固定不跟随光标模式
                if (colVisible) {  // 列光标可见

                    if(bTValueTrace && !rowVisible){  // T值跟踪且行光标不可见
                        singleBmp = tvalueBmp;  // 使用T值位图
                    }else{  // 非T值跟踪或行光标可见
                        singleBmp = middleSingleBmp;  // 使用中等尺寸单独位图
                    }
                } else if (rowVisible) {  // 仅行光标可见
                    singleBmp = smallSingleBmp;  // 使用小尺寸单独位图
                }
                canvas.setBitmap(singleBmp);  // 设置画布目标位图
                paint.setXfermode(clearMode);  // 设置清除模式
                canvas.drawPaint(paint);  // 清空位图
                paint.setXfermode(srcMode);  // 设置源绘制模式
                int color = TChan.getChannelColor(context, chan + 1);  // 获取通道颜色
                Paint strokePaint = new Paint();  // 创建描边画笔
                strokePaint.setStyle(Paint.Style.STROKE);  // 设置描边样式
                strokePaint.setStrokeWidth(1);  // 设置线宽1像素
                strokePaint.setColor(color);  // 设置通道颜色
                Paint fillPaint = new Paint();  // 创建填充画笔
                fillPaint.setStyle(Paint.Style.FILL);  // 设置填充样式
                fillPaint.setColor(Color.argb(180,18,18,18));  // 设置半透明深灰背景色
                Rect rect = new Rect(1, 1, singleBmp.getWidth()-1, singleBmp.getHeight()-1);  // 计算标签内部矩形
                canvas.drawRect(rect,fillPaint);  // 绘制填充背景
                canvas.drawRoundRect(1, 1, singleBmp.getWidth()-1f, singleBmp.getHeight()-1f, 5, 5, strokePaint);  // 绘制圆角描边

//                canvas.drawRoundRect(0, 0, singleBmp.getWidth(), singleBmp.getHeight(), 6f, 6f, strokePaint);
                paint.setAntiAlias(true);  // 开启抗锯齿
                paint.setStyle(Paint.Style.FILL);  // 设置填充样式
                paint.setColor(txtColor);  // 设置文本颜色
                if (colVisible && !rowVisible) {  // 仅列光标可见
                    for (int i = 0; i < bmp.length; i++) {  // 遍历各标签
                        if (i == X1) {  // X1标签
                            int x = 6;  // 左边距6像素
                            int y = 18;  // 行高18像素
                            Tools.drawText(canvas, dataMap.get(i), x, y * 1, singleBmp.getWidth(), paint, textPaint);  // 绘制X1文本
                            if(!bTValueTrace) {  // 非T值跟踪模式
                                Tools.drawText(canvas, dataMap.get(X1CursorInteraction), 22, y * 2, singleBmp.getWidth(), paint, textPaint);  // 绘制X1交互测量值
                            }
                        } else if (i == X2) {  // X2标签
                            int x = 6;  // 左边距6像素
                            int y = 18;  // 行高18像素
                            Tools.drawText(canvas, dataMap.get(i), x, y * (bTValueTrace ? 2 : 3), singleBmp.getWidth(), paint, textPaint);  // 绘制X2文本
                            if(!bTValueTrace) {  // 非T值跟踪模式
                                Tools.drawText(canvas, dataMap.get(X2CursorInteraction), 22, y * 4, singleBmp.getWidth(), paint, textPaint);  // 绘制X2交互测量值
                            }
                        } else if (i == XDelta) {  // ΔX标签
                            int y = 18;  // 行高18像素
                            int x = 6;  // 左边距6像素
                            Tools.drawText(canvas, dataMap.get(i), x, y * (bTValueTrace ? 3 : 5), bmp[i].getWidth(), paint, textPaint);  // 绘制ΔX文本
                            Tools.drawText(canvas, dataMap.get(XFre), x, y * (bTValueTrace ? 4 : 6), bmp[i].getWidth(), paint, textPaint);  // 绘制1/ΔX文本
                            if(!bTValueTrace) {  // 非T值跟踪模式
                                Tools.drawText(canvas, dataMap.get(VoltgaeDelta), x, y * 7, bmp[i].getWidth(), paint, textPaint);  // 绘制ΔV文本
                                Tools.drawText(canvas, dataMap.get(VoltageDivideXDelta), x, y * 8, bmp[i].getWidth(), paint, textPaint);  // 绘制ΔV/Δt文本
                            }
                        }
                    }
                } else if (rowVisible && !colVisible) {  // 仅行光标可见
                    for (int i = 0; i < bmp.length; i++) {  // 遍历各标签
                        if (i == XDelta || i == X1 || i == X2) {  // 跳过列光标标签
                            continue;  // 跳过
                        } else {  // Y标签
                            int y = 18;  // 行高18像素
                            int x = 6;  // 左边距6像素
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i - 2), singleBmp.getWidth(), paint, textPaint);  // 绘制Y标签文本
                        }
                    }
                } else if (colVisible && rowVisible) {  // 行列光标都可见
                    for (int i = 0; i < bmp.length; i++) {  // 遍历各标签
                        int x = 6;  // 左边距6像素
                        if (i == X1) {  // X1标签
                            int y = 18;  // 行高18像素
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i + 4), singleBmp.getWidth(), paint, textPaint);  // 绘制X1文本
                        } else if (i == X2) {  // X2标签
                            int y = 18;  // 行高18像素
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i + 4), singleBmp.getWidth(), paint, textPaint);  // 绘制X2文本
                        } else if (i == XDelta) {  // ΔX标签
                            int y = 18;  // 行高18像素
                            if (dataMap.get(XFre).isEmpty()) {  // 频率值为空
                                y = 25;  // 调整行间距
                            }
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i + 4), singleBmp.getWidth(), paint, textPaint);  // 绘制ΔX文本
                            Tools.drawText(canvas, dataMap.get(XFre), x, y * (i + 5), singleBmp.getWidth(), paint, textPaint);  // 绘制1/ΔX文本
                            Tools.drawText(canvas, dataMap.get(S), x, y * (i + 6), singleBmp.getWidth(), paint, textPaint);  // 绘制ΔV/Δt文本
                        } else {  // Y标签
                            int y = 18;  // 行高18像素
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i - 2), singleBmp.getWidth(), paint, textPaint);  // 绘制Y标签文本
                        }
                    }
                }
            }
        } else if (WorkModeManage.getInstance().isXyMode()) {  // XY模式
            updateStringWidth();  // 更新字符串宽度限制
            if (!CursorManage.getInstance().isLabelNotFollowCursor()) {  // 标签跟随光标模式
                for (int i = 0; i < bmp.length; i++) {  // 遍历各标签
                    bmp[i] = smallBmps[i];  // 使用小尺寸位图
                    if (i == XDelta) {  // ΔX标签
                        bmp[i] = Bitmap.createBitmap(140, 25, Bitmap.Config.ARGB_8888);  // XY模式下ΔX标签尺寸
                    }
                    canvas.setBitmap(bmp[i]);  // 设置画布目标位图
                    paint.setXfermode(clearMode);  // 设置清除模式
                    canvas.drawPaint(paint);  // 清空位图
                    paint.setXfermode(srcMode);  // 设置源绘制模式
                    int color = Color.LTGRAY;  // XY模式使用浅灰色
                    paint.setColor(color);  // 设置颜色
                    paint.setStyle(Paint.Style.STROKE);  // 设置描边样式
                    paint.setStrokeWidth(1.5f);  // 设置线宽1.5像素
                    canvas.drawRoundRect(0, 0, bmp[i].getWidth(), bmp[i].getHeight(), 6f, 6f, paint);  // 绘制圆角矩形边框
                    paint.setAntiAlias(true);  // 开启抗锯齿
                    paint.setStyle(Paint.Style.FILL);  // 设置填充样式
                    paint.setColor(txtColor);  // 设置文本颜色
                    if (i == XDelta) {  // ΔX标签
                        int y = centerTxtY(bmp[i], dataMap.get(i), paint);  // 计算居中Y坐标
                        int x = centerTxtX(bmp[i], dataMap.get(i), paint);  // 计算居中X坐标
                        Tools.drawText(canvas, dataMap.get(i), x, y, bmp[i].getWidth(), paint, textPaint);  // 居中绘制ΔX文本
                    } else if (i == X1) {  // X1标签
                        int x = centerTxtX(bmp[i], dataMap.get(i), paint);  // 计算居中X坐标
                        int y = centerTxtY(bmp[i], dataMap.get(i), paint);  // 计算居中Y坐标
                        Tools.drawText(canvas, dataMap.get(i), x, y, bmp[i].getWidth(), paint, textPaint);  // 居中绘制X1文本
                    } else if (i == X2) {  // X2标签
                        int x = centerTxtX(bmp[i], dataMap.get(i), paint);  // 计算居中X坐标
                        int y = centerTxtY(bmp[i], dataMap.get(i), paint);  // 计算居中Y坐标
                        Tools.drawText(canvas, dataMap.get(i), x, y, bmp[i].getWidth(), paint, textPaint);  // 居中绘制X2文本
                    } else {  // Y1/Y2/ΔY标签
                        int y = centerTxtY(bmp[i], dataMap.get(i), paint);  // 计算居中Y坐标
                        int x = centerTxtX(bmp[i], dataMap.get(i), paint);  // 计算居中X坐标
                        Tools.drawText(canvas, dataMap.get(i), x, y, bmp[i].getWidth(), paint, textPaint);  // 居中绘制文本
                    }
                }
            } else {  // XY模式标签固定不跟随
                if (colVisible && rowVisible) {  // 行列都可见
                    singleBmp = Bitmap.createBitmap(120, 115, Bitmap.Config.ARGB_8888);  // 创建大尺寸单独位图
                } else if (rowVisible || colVisible) {  // 只有一种可见
                    singleBmp = Bitmap.createBitmap(120, 60, Bitmap.Config.ARGB_8888);  // 创建小尺寸单独位图
                }

                canvas.setBitmap(singleBmp);  // 设置画布目标位图
                paint.setXfermode(clearMode);  // 设置清除模式
                paint.setAntiAlias(true);  // 开启抗锯齿
                canvas.drawPaint(paint);  // 清空位图
                paint.setXfermode(srcMode);  // 设置源绘制模式
                int color = Color.LTGRAY;  // XY模式使用浅灰色
                paint.setColor(color);  // 设置颜色
                paint.setStyle(Paint.Style.STROKE);  // 设置描边样式
                paint.setStrokeWidth(1.5f);  // 设置线宽1.5像素
//            canvas.drawRect(0, 0, singleBmp.getWidth(), singleBmp.getHeight(), paint);
                canvas.drawRoundRect(0, 0, singleBmp.getWidth(), singleBmp.getHeight(), 6f, 6f, paint);  // 绘制圆角矩形边框
                paint.setAntiAlias(true);  // 开启抗锯齿
                paint.setStyle(Paint.Style.FILL);  // 设置填充样式
//            canvas.drawRoundRect(1, 1, singleBmp.getWidth() - 1, singleBmp.getHeight() - 1,8,8,paint);
                paint.setColor(txtColor);  // 设置文本颜色
                CurSorMeasureBase curSorMeasureBase = new CurSorMeasureBase();  // 创建光标测量基类实例（未使用）
                if (colVisible && !rowVisible) {  // 仅列光标可见
                    for (int i = 0; i < bmp.length; i++) {  // 遍历各标签
                        if (i == X1) {  // X1标签
                            int x = 4;  // 左边距4像素
                            int y = 18;  // 行高18像素
                            Tools.drawText(canvas, dataMap.get(i), x, y, singleBmp.getWidth(), paint, textPaint);  // 绘制X1文本
                        } else if (i == X2) {  // X2标签
                            int x = 4;  // 左边距4像素
                            int y = 18;  // 行高18像素
                            Tools.drawText(canvas, dataMap.get(i), x, y * 2, singleBmp.getWidth(), paint, textPaint);  // 绘制X2文本
                        } else if (i == XDelta) {  // ΔX标签
                            int y = 18;  // 行高18像素
                            int x = 4;  // 左边距4像素
                            Tools.drawText(canvas, dataMap.get(i), x, y * 3, bmp[i].getWidth(), paint, textPaint);  // 绘制ΔX文本
                        }
                    }
                } else if (rowVisible && !colVisible) {  // 仅行光标可见
                    for (int i = 0; i < bmp.length; i++) {  // 遍历各标签
                        if (i == XDelta || i == X1 || i == X2) {  // 跳过列光标标签
                            continue;  // 跳过
                        } else {  // Y标签
                            int y = 18;  // 行高18像素
                            int x = 4;  // 左边距4像素
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i - 2), singleBmp.getWidth(), paint, textPaint);  // 绘制Y标签文本
                        }
                    }
                } else if (colVisible && rowVisible) {  // 行列都可见
                    for (int i = 0; i < bmp.length; i++) {  // 遍历各标签
                        int x = 4;  // 左边距4像素
                        if (i == X1) {  // X1标签
                            int y = 18;  // 行高18像素
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i + 4), singleBmp.getWidth(), paint, textPaint);  // 绘制X1文本
                        } else if (i == X2) {  // X2标签
                            int y = 18;  // 行高18像素
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i + 4), singleBmp.getWidth(), paint, textPaint);  // 绘制X2文本
                        } else if (i == XDelta) {  // ΔX标签
                            int y = 18;  // 行高18像素
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i + 4), singleBmp.getWidth(), paint, textPaint);  // 绘制ΔX文本
                        } else {  // Y标签
                            int y = 18;  // 行高18像素
                            Tools.drawText(canvas, dataMap.get(i), x, y * (i - 2), singleBmp.getWidth(), paint, textPaint);  // 绘制Y标签文本
                        }
                    }
                }
            }
        }


        //绘制光标
        int pX1 = (int) getCursor(TChan.Cursor_col_1);  // 获取列光标1的X像素位置
        int pX2 = (int) getCursor(TChan.Cursor_col_2);  // 获取列光标2的X像素位置
        int pY1 = (int) getCursor(TChan.Cursor_row_1);  // 获取行光标1的Y像素位置
        int pY2 = (int) getCursor(TChan.Cursor_row_2);  // 获取行光标2的Y像素位置
        int distance = Math.abs(pX1 - pX2);  // 计算两个列光标间距
        canvas.setBitmap(rowBmp);  // 设置画布目标为行箭头位图
        paint.setXfermode(clearMode);  // 设置清除模式
        canvas.drawPaint(paint);  // 清空位图
        paint.setXfermode(srcMode);  // 设置源绘制模式
        Bitmap bmp = Tools.getNineBmp(resCursorBmp[29], distance, 16, 0, 100);  // 生成X方向箭头九宫格位图
        if (bmp != null && !CursorManage.getInstance().isLabelNotFollowCursor()) {  // 位图有效且标签跟随模式
            canvas.drawBitmap(bmp, Math.min(pX1, pX2), 0, paint);  // 在较左侧光标位置绘制箭头
            putRect(XArrow, Math.min(pX1, pX2), 0, bmp);  // 记录箭头矩形区域
        }
        if (bmp != null) bmp.recycle();  // 回收临时位图

        distance = Math.abs(pY1 - pY2);  // 计算两个行光标间距
        canvas.setBitmap(colBmp);  // 设置画布目标为列箭头位图
        paint.setXfermode(clearMode);  // 设置清除模式
        canvas.drawPaint(paint);  // 清空位图
        paint.setXfermode(srcMode);  // 设置源绘制模式
        Bitmap bmp1 = Tools.getNineBmp(resCursorBmp[29], distance, 16, 0, 75);  // 生成Y方向箭头九宫格位图
        if (WorkModeManage.getInstance().isXyMode()) {  // XY模式下
            bmp1 = Tools.getNineBmp(resCursorBmp[29], distance, 16, 0, 75);  // 重新生成XY模式箭头位图
        }
        if (bmp1 != null && !CursorManage.getInstance().isLabelNotFollowCursor()) {  // 位图有效且标签跟随模式
            Matrix m = new Matrix();  // 创建矩阵
            m.postTranslate((float) -bmp1.getWidth() / 2, (float) -bmp1.getHeight() / 2);  // 平移到中心
            m.postRotate(90);  // 旋转90度（横向箭头变为纵向）
            m.postTranslate((float) bmp1.getHeight() / 2, (float) bmp1.getWidth() / 2 + Math.min(pY1, pY2));  // 平移到目标位置
            canvas.drawBitmap(bmp1, m, paint);  // 用矩阵变换绘制箭头
        }
        if (bmp1 != null) bmp1.recycle();  // 回收临时位图
        isChangeBmp = true;  // 标记位图已改变
        onRefresh();  // 通知OpenGL刷新纹理
    }

    /**
     * 获取指定类型光标的像素位置
     *
     * @param cursorType 光标类型（TChan.Cursor_col_1/2, TChan.Cursor_row_1/2）
     * @return 光标像素位置，列光标返回X坐标，行光标返回Y坐标
     */
    public double getCursor(int cursorType) {  // 获取光标位置
        for (Cursor_impIWave c : cursorList) {  // 遍历光标列表
            if (c.getCursorType() == cursorType) {  // 找到匹配类型
                if (TChan.isCursorCol2(cursorType)) {  // 列光标返回X坐标
                    return c.getX();  // 返回X像素位置
                } else if (TChan.isCursorRow2(cursorType)) {  // 行光标返回Y坐标
                    return c.getY();  // 返回Y像素位置
                }
            }
        }
//        Log.d("Tag.Debug", String.format("getCursor error: %d",0 ));
        return 0;  // 未找到返回0
    }

    /**
     * 计算文本在位图中水平居中的X坐标
     *
     * @param bmp   目标位图
     * @param s     文本内容
     * @param paint 画笔
     * @return 居中的X坐标
     */
    private int centerTxtX(Bitmap bmp, String s, Paint paint) {  // 计算水平居中X坐标
        int width = Tools.getTextRect(s, paint).width();  // 获取文本宽度
        int x = (bmp.getWidth() - width) / 2;  // 计算居中位置
        return x;  // 返回X坐标
    }

    /**
     * 计算文本在位图中垂直居中的Y坐标（基线位置）
     *
     * @param bmp   目标位图
     * @param s     文本内容
     * @param paint 画笔
     * @return 基线Y坐标
     */
    private int centerTxtY(Bitmap bmp, String s, Paint paint) {  // 计算垂直居中Y坐标
        Paint.FontMetrics metrics = paint.getFontMetrics();  // 获取字体度量
        float distance = (metrics.bottom - metrics.top) / 2 - metrics.bottom;  // 计算基线偏移
        float baseline = distance + bmp.getHeight() / 2;  // 计算基线Y坐标
        return (int) baseline;  // 返回Y坐标
    }

    /**
     * 在OpenGL画布上绘制测量标签（外部调用入口）
     * 计算各标签的屏幕位置，处理标签重叠，最终渲染到画布
     *
     * @param canvas OpenGL画布
     */
    public synchronized void drawMeasure(ICanvasGL canvas) {  // 同步绘制到OpenGL画布
        if (EnableMeasure == false) return;  // 测量功能未启用则返回
        canvasGL = canvas;  // 保存画布引用
        int interval = 30;  // 间隔像素
        int x = labelX + 160, y = labelY + 150;  // 计算标签基准坐标偏移
        int screenWidth = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());  // 获取波形区宽度
        int screenHeight = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());  // 获取波形区高度
        int pX1 = (int) Math.round(getCursor(TChan.Cursor_col_1));  // 获取列光标1位置
        int pX2 = (int) Math.round(getCursor(TChan.Cursor_col_2));  // 获取列光标2位置
        int pY1 = (int) Math.round(getCursor(TChan.Cursor_row_1));  // 获取行光标1位置
        int pY2 = (int) Math.round(getCursor(TChan.Cursor_row_2));  // 获取行光标2位置
        for (int i = 0; i < bmp.length; i++) {  // 遍历各标签
            if (isChangeBmp) canvas.invalidateTextureContent(bmp[i], null);  // 位图已改变则使纹理失效
        }
        if (isChangeBmp) {  // 位图已改变
            canvas.invalidateTextureContent(rowBmp, null);  // 使行箭头纹理失效
            canvas.invalidateTextureContent(colBmp, oldColBmp);  // 使列箭头纹理失效（传入旧位图用于比较）
            canvas.invalidateTextureContent(singleBmp, null);  // 使单独标签纹理失效
            oldColBmp = null;  // 清空旧列位图引用
        }

        if (colVisible && !CursorManage.getInstance().isLabelNotFollowCursor()) {  // 列光标可见且标签跟随
            drawGLXDelta(canvas, y, pX1, pX2, screenWidth, bmp[XDelta]);  // 绘制ΔX标签
            drawGLX1(canvas, y, pX1, pX2, screenWidth, bmp[X1]);  // 绘制X1标签
            drawGLX2(canvas, y, pX1, pX2, screenWidth, bmp[X2]);  // 绘制X2标签
        } else if (colVisible && CursorManage.getInstance().isLabelNotFollowCursor()) {  // 列光标可见且标签固定
            drawGLSingle(canvas, screenHeight, screenWidth, singleBmp);  // 绘制单独标签
            drawGLX1(canvas, y, pX1, pX2, screenWidth, bmp[X1]);  // 绘制X1标签
        }
        if (rowVisible && !CursorManage.getInstance().isLabelNotFollowCursor()) {  // 行光标可见且标签跟随
            drawGLY1(canvas, x, pY1, pY2, screenHeight, bmp[Y1]);  // 绘制Y1标签
            drawGLY2(canvas, x, pY1, pY2, screenHeight, bmp[Y2]);  // 绘制Y2标签
            drawGLYDelta(canvas, x, pY1, pY2, screenHeight, bmp[YDelta]);  // 绘制ΔY标签
        } else if (rowVisible && CursorManage.getInstance().isLabelNotFollowCursor()) {  // 行光标可见且标签固定
            drawGLSingle(canvas, screenHeight, screenWidth, singleBmp);  // 绘制单独标签
        }
        putRect(XArrow, 0, labelY + 140, rowBmp);  // 记录X箭头矩形区域
        putRect(YArrow, labelX + 150, 0, colBmp);  // 记录Y箭头矩形区域
        intersect(screenWidth, screenHeight);  // 处理标签重叠
        drawScreen(canvas);  // 实际绘制到屏幕
        isChangeBmp = false;  // 重置位图改变标志
    }

    /**
     * 记录标签矩形区域（使用位图宽高）
     *
     * @param key 矩形索引键
     * @param x   左上角X坐标
     * @param y   左上角Y坐标
     * @param bmp 位图（用于获取宽高）
     */
    private void putRect(int key, int x, int y, Bitmap bmp) {  // 记录矩形区域
        Rect rect = rectMap.get(key);  // 获取矩形对象
        rect.set(x, y, x + bmp.getWidth(), y + bmp.getHeight());  // 设置矩形范围
        rectMap.put(key, rect);  // 保存到映射
    }

    /**
     * 记录标签矩形区域（保持原有宽高）
     *
     * @param key 矩形索引键
     * @param x   新的左上角X坐标
     * @param y   新的左上角Y坐标
     */
    private void putRect(int key, int x, int y) {  // 记录矩形区域（保持宽高）
        Rect rect = rectMap.get(key);  // 获取矩形对象
        int width = rect.width();  // 获取当前宽度
        int height = rect.height();  // 获取当前高度
        rect.set(x, y, x + width, y + height);  // 设置新位置（保持宽高）
        rectMap.put(key, rect);  // 保存到映射
    }

    /**
     * 计算并记录ΔX标签在跟随光标模式下的屏幕位置
     *
     * @param canvas      OpenGL画布
     * @param y           基准Y坐标
     * @param pX1         列光标1的X像素位置
     * @param pX2         列光标2的X像素位置
     * @param screenWidth 波形区宽度
     * @param bmp         ΔX标签位图
     */
    private void drawGLXDelta(ICanvasGL canvas, int y, int pX1, int pX2, int screenWidth, Bitmap bmp) {  // 绘制ΔX标签位置
        int xInterval = Math.abs(pX1 - pX2) + 0;  // 计算两光标间距
        int x;  // 标签X坐标
        if (xInterval < bmp.getWidth() && pX1 < bmp.getWidth() && pX2 < bmp.getWidth()) {  // 左边delta显示不全情况
            x = Math.min(pX1, pX2);  // 在较左光标处对齐
        } else if (xInterval < bmp.getWidth() && pX1 + bmp.getWidth() > screenWidth && pX2 + bmp.getWidth() > screenWidth) {  // 右边delta显示不全情况
            x = Math.max(pX1, pX2) - bmp.getWidth();  // 在较右光标左侧对齐
        } else if (xInterval < bmp.getWidth()) {  // 中间delta显示不全情况
            x = CenterBmpX(pX1, pX2, bmp);  // 居中显示
        } else {  // 中间可以显示完全
            x = CenterBmpX(pX1, pX2, bmp);  // 居中显示
        }
        int screenHeight = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());  // 获取波形区高度

        if(y<0){  // Y坐标超出上边界
            y=0;  // 钳位到0
        }else if(y+bmp.getHeight() > screenHeight){  // Y坐标超出下边界
            y = screenHeight - bmp.getHeight();  // 钳位到下边界
        }
//        canvas.drawBitmap(bmp,x,y);
        putRect(XDelta, x, y, bmp);  // 记录ΔX标签矩形区域
    }

    /**
     * 计算并记录ΔY标签在跟随光标模式下的屏幕位置
     *
     * @param canvas       OpenGL画布
     * @param x            基准X坐标
     * @param pY1          行光标1的Y像素位置
     * @param pY2          行光标2的Y像素位置
     * @param screenHeight 波形区高度
     * @param bmp          ΔY标签位图
     */
    private void drawGLYDelta(ICanvasGL canvas, int x, int pY1, int pY2, int screenHeight, Bitmap bmp) {  // 绘制ΔY标签位置
        int xInterval = Math.abs(pY1 - pY2) + 0;  // 计算两光标间距
        int y;  // 标签Y坐标
        if (xInterval < bmp.getHeight() && pY1 < bmp.getHeight() && pY2 < bmp.getHeight()) {  // 左边delta显示不全情况
            y = Math.min(pY1, pY2);  // 在较上光标处对齐
        } else if (xInterval < bmp.getHeight() && pY1 + bmp.getHeight() > screenHeight && pY2 + bmp.getHeight() > screenHeight) {  // 右边delta显示不全情况
            y = Math.max(pY1, pY2) - bmp.getHeight();  // 在较下光标上侧对齐
        } else if (xInterval < bmp.getHeight()) {  // 中间delta显示不全情况
            y = CenterBmpY(pY1, pY2, bmp);  // 居中显示
        } else {  // 中间可以显示完全
            y = CenterBmpY(pY1, pY2, bmp);  // 居中显示
        }
//        canvas.drawBitmap(bmp,x,y);
        putRect(YDelta, x, y, bmp);  // 记录ΔY标签矩形区域
    }

    /**
     * 计算并记录X1标签在跟随光标模式下的屏幕位置
     *
     * @param canvas      OpenGL画布
     * @param y           基准Y坐标
     * @param pX1         列光标1的X像素位置
     * @param pX2         列光标2的X像素位置
     * @param screenWidth 波形区宽度
     * @param bmp         X1标签位图
     */
    private void drawGLX1(ICanvasGL canvas, int y, int pX1, int pX2, int screenWidth, Bitmap bmp) {  // 绘制X1标签位置
        int x;  // 标签X坐标
        y = labelY;  // 使用标签Y坐标
        int xInterval = Math.abs(pX1 - pX2) + 0;  // 计算两光标间距
        if (pX1 < bmp.getWidth() / 2) {  // 左边delta显示不全情况
            x = pX1;  // 在光标1处对齐
        } else if (pX1 + bmp.getWidth() / 2 > screenWidth) {  // 右边delta显示不全情况
            x = pX1 - bmp.getWidth();  // 在光标1左侧对齐
        } else if (xInterval < bmp.getWidth()) {  // 中间delta显示不全情况
            x = pX1 - bmp.getWidth() + xInterval / 2;  // 偏移显示避免重叠
            if (pX1 > pX2) {  // 光标1在右
                x = pX1 - xInterval / 2;  // 向左偏移
            }
        } else {  // 中间可以显示完全
            x = pX1 - bmp.getWidth() / 2;  // 居中显示
        }
//        if (x<0){
//            x=pX1;
//        }
//        canvas.drawBitmap(bmp,x,y);
        putRect(X1, x, y, bmp);  // 记录X1标签矩形区域
    }

    /**
     * 计算并记录X2标签在跟随光标模式下的屏幕位置
     *
     * @param canvas      OpenGL画布
     * @param y           基准Y坐标
     * @param pX1         列光标1的X像素位置
     * @param pX2         列光标2的X像素位置
     * @param screenWidth 波形区宽度
     * @param bmp         X2标签位图
     */
    private void drawGLX2(ICanvasGL canvas, int y, int pX1, int pX2, int screenWidth, Bitmap bmp) {  // 绘制X2标签位置
        int x;  // 标签X坐标
        y = labelY;  // 使用标签Y坐标
        int xInterval = Math.abs(pX1 - pX2) + 0;  // 计算两光标间距
        if (pX2 < pX1 + bmp.getWidth() + bmp.getWidth() / 2 && pX1 <= bmp.getWidth() / 2
            /*|| pX1==rectMap.get(X1).left*/) {  // 左边delta显示不全情况
            x = pX2;  // 在光标2处对齐
            y += 50;  // 下移50像素避免与X1重叠
        } else if (pX2 + bmp.getWidth() / 2 > pX1 - bmp.getWidth() && pX1 + bmp.getWidth() / 2 > screenWidth) {  // 右边delta显示不全情况
            x = pX2 - bmp.getWidth();  // 在光标2左侧对齐
            y += 50;  // 下移50像素
        } else if (pX2 + bmp.getWidth() / 2 > screenWidth) {  // 光标2靠近右边界
            x = pX2 - bmp.getWidth();  // 在光标2左侧对齐
            y += 50;  // 下移50像素
        } else if (pX2 < bmp.getWidth() / 2) {  // 光标2靠近左边界
            x = pX2;  // 在光标2处对齐
            y += 50;  // 下移50像素
        } else if (xInterval < bmp.getWidth()) {  // 中间delta显示不全情况
            x = pX2 - xInterval / 2;  // 偏移显示
            if (pX2 < pX1) {  // 光标2在左
                x = pX2 - bmp.getWidth() + xInterval / 2;  // 向左偏移
            }
        } else {  // 中间可以显示完全
            x = pX2 - bmp.getWidth() / 2;  // 居中显示
        }
//        canvas.drawBitmap(bmp,x,y);
        putRect(X2, x, y, bmp);  // 记录X2标签矩形区域
    }

    /**
     * 计算并记录Y1标签在跟随光标模式下的屏幕位置
     *
     * @param canvas       OpenGL画布
     * @param x            基准X坐标
     * @param pY1          行光标1的Y像素位置
     * @param pY2          行光标2的Y像素位置
     * @param screenHeight 波形区高度
     * @param bmp          Y1标签位图
     */
    private void drawGLY1(ICanvasGL canvas, int x, int pY1, int pY2, int screenHeight, Bitmap bmp) {  // 绘制Y1标签位置
        int y;  // 标签Y坐标
        x = labelX;  // 使用标签X坐标
        int xInterval = Math.abs(pY1 - pY2) + 0;  // 计算两光标间距
        if (pY1 < bmp.getHeight() / 2) {  // 左边delta显示不全情况
            y = pY1;  // 在光标1处对齐
        } else if (pY1 + bmp.getHeight() / 2 > screenHeight) {  // 右边delta显示不全情况
            y = pY1 - bmp.getHeight();  // 在光标1上侧对齐
        } else if (xInterval < bmp.getHeight()) {  // 中间delta显示不全情况
            y = pY1 - bmp.getHeight() + xInterval / 2;  // 偏移显示避免重叠
            if (pY1 > pY2) {  // 光标1在下
                y = pY1 - xInterval / 2;  // 向上偏移
            }
        } else {  // 中间可以显示完全
            y = pY1 - bmp.getHeight() / 2;  // 居中显示
        }
//        canvas.drawBitmap(bmp,x,y);
        putRect(Y1, x, y, bmp);  // 记录Y1标签矩形区域
    }

    /**
     * 计算并记录Y2标签在跟随光标模式下的屏幕位置
     *
     * @param canvas       OpenGL画布
     * @param x            基准X坐标
     * @param pY1          行光标1的Y像素位置
     * @param pY2          行光标2的Y像素位置
     * @param screenHeight 波形区高度
     * @param bmp          Y2标签位图
     */
    private void drawGLY2(ICanvasGL canvas, int x, int pY1, int pY2, int screenHeight, Bitmap bmp) {  // 绘制Y2标签位置
        int y;  // 标签Y坐标
        x = labelX;  // 使用标签X坐标
        int xInterval = Math.abs(pY1 - pY2) + 0;  // 计算两光标间距
        if (pY2 < pY1 + bmp.getHeight() + bmp.getHeight() / 2 && pY1 <= bmp.getHeight() / 2) {  // 左边delta显示不全情况
//            x = 50;
            y = pY2;  // 在光标2处对齐
        } else if (pY2 + bmp.getHeight() / 2 > pY1 - bmp.getHeight() && pY1 + bmp.getHeight() / 2 > screenHeight) {  // 右边delta显示不全情况
//            x=50;
            y = pY2 - bmp.getHeight();  // 在光标2上侧对齐
        } else if (pY2 < bmp.getHeight() / 2) {  // 左边delta显示不全情况
//            x=50;
            y = pY2;  // 在光标2处对齐
        } else if (pY2 + bmp.getHeight() / 2 > screenHeight) {  // 右边delta显示不全情况
//            x=50;
            y = pY2 - bmp.getHeight();  // 在光标2上侧对齐
        } else if (xInterval < bmp.getHeight()) {  // 中间delta显示不全情况
            y = pY2 - xInterval / 2;  // 偏移显示
            if (pY2 < pY1) {  // 光标2在上
                y = pY2 - bmp.getHeight() + xInterval / 2;  // 向上偏移
            }
        } else {  // 中间可以显示完全
            y = pY2 - bmp.getHeight() / 2;  // 居中显示
        }
//        canvas.drawBitmap(bmp,x,y);
        putRect(Y2, x, y, bmp);  // 记录Y2标签矩形区域
    }


    /**
     * 计算并记录单独标签在固定模式下的屏幕位置
     *
     * @param canvas       OpenGL画布
     * @param screenHeight 波形区高度
     * @param screenWidth  波形区宽度
     * @param bmp          单独标签位图
     */
    private void drawGLSingle(ICanvasGL canvas, int screenHeight, int screenWidth, Bitmap bmp) {  // 绘制单独标签位置

        int x = singleLabelX;  // 获取单独标签X坐标
        int y = singleLabelY;  // 获取单独标签Y坐标
        int bmpWidth = bmp.getWidth();  // 获取位图宽度
        int bmpHeight = bmp.getHeight();  // 获取位图高度
        if (x < 0) {  // X坐标超出左边界
            x = 0;  // 钳位到0
        } else if (x + bmpWidth > screenWidth) {  // X坐标超出右边界
            x = screenWidth - bmpWidth;  // 钳位到右边界
        }
        if (y < 0) {  // Y坐标超出上边界
            y = 0;  // 钳位到0
        } else if (y + bmpHeight > screenHeight) {  // Y坐标超出下边界
            y = screenHeight - bmpHeight;  // 钳位到下边界
        }
//        canvas.drawBitmap(bmp,x,y);
        putRect(SingleBmp, x, y, bmp);  // 记录单独标签矩形区域
    }

    /**
     * 处理标签矩形区域重叠，自动调整位置
     *
     * @param screenWidth  波形区宽度
     * @param screenHeight 波形区高度
     */
    private void intersect(int screenWidth, int screenHeight) {  // 处理标签重叠
        if (rowVisible) {  // 行光标可见
            int py1 = (int) getCursor(TChan.Cursor_row_1);  // 获取行光标1位置
            int py2 = (int) getCursor(TChan.Cursor_row_2);  // 获取行光标2位置

            if (Rect.intersects(rectMap.get(Y1), rectMap.get(Y2))) {  // Y1和Y2标签重叠
                Rect y2 = rectMap.get(Y2);  // 获取Y2矩形
                Rect yDelta = rectMap.get(YDelta);  // 获取ΔY矩形
                y2.offset(y2.width(), 0);  // Y2向右偏移一个宽度
                yDelta.offset(y2.width(), 0);  // ΔY向右偏移一个宽度
                if (y2.right > screenWidth) y2.offset(-y2.width() * 2, 0);  // Y2超出右边界则左移两倍宽度
                if (yDelta.right > screenWidth) yDelta.offset(-yDelta.width() * 3, 0);  // ΔY超出右边界则左移三倍宽度
                rectMap.put(Y2, y2);  // 更新Y2矩形
                rectMap.put(YDelta, yDelta);  // 更新ΔY矩形

                if (rectMap.get(Y1).top < 0) {  // Y1超出上边界
                    putRect(Y1, rectMap.get(Y1).left, py1);  // 重新定位Y1
                }
                if (rectMap.get(Y2).top < 0) {  // Y2超出上边界
                    putRect(Y2, rectMap.get(Y2).left, py2);  // 重新定位Y2
                }

                if (rectMap.get(Y1).bottom > screenHeight) {  // Y1超出下边界
                    putRect(Y1, rectMap.get(Y1).left, py1 - bmp[Y1].getHeight());  // 重新定位Y1
                }
                if (rectMap.get(Y2).bottom > screenHeight) {  // Y2超出下边界
                    putRect(Y2, rectMap.get(Y2).left, py2 - bmp[Y2].getHeight());  // 重新定位Y2
                }
            }

        }
        if (colVisible) {  // 列光标可见
            int pX1 = (int) getCursor(TChan.Cursor_col_1);  // 获取列光标1位置
            int pX2 = (int) getCursor(TChan.Cursor_col_2);  // 获取列光标2位置

            if (rectMap.get(X1).left < 0) {  // X1超出左边界
                putRect(X1, pX1, labelY);  // 重新定位X1
            }
            if (rectMap.get(X2).left < 0) {  // X2超出左边界
                putRect(X2, pX2, labelY + 50);  // 重新定位X2
            }

            if (rectMap.get(X1).right > screenWidth) {  // X1超出右边界
                putRect(X1, pX1 - bmp[X1].getWidth(), labelY);  // 重新定位X1
            }
            if (rectMap.get(X2).right > screenWidth) {  // X2超出右边界
                putRect(X2, pX2 - bmp[X2].getWidth(), labelY + 50);  // 重新定位X2
            }

            if (Rect.intersects(rectMap.get(X1), rectMap.get(X2)) && rectMap.get(X1).left == pX1) {  // X1/X2重叠且X1在光标1处
                putRect(X2, pX2, labelY + 50);  // X2下移50像素
            }
            if (Rect.intersects(rectMap.get(X1), rectMap.get(X2)) && rectMap.get(X1).right == pX1) {  // X1/X2重叠且X1右边缘在光标1处
                putRect(X2, pX2 - bmp[X2].getWidth(), labelY + 50);  // X2左对齐到光标2并下移
            }
        }
    }

    /**
     * 实际将所有标签位图绘制到OpenGL画布
     *
     * @param canvasGL OpenGL画布
     */
    private void drawScreen(ICanvasGL canvasGL) {  // 绘制到屏幕
        if (colVisible && !CursorManage.getInstance().isLabelNotFollowCursor()) {  // 列光标可见且标签跟随

            int screenHeight = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());  // 获取波形区高度

            Rect x = rectMap.get(X1);  // 获取X1矩形
            int y = x.top;  // 获取X1的Y坐标
            int hh = rectMap.get(XDelta).height() + 50;  // 计算总高度
            if ( y> screenHeight - hh) y = screenHeight - hh;  // 超出下边界则钳位

            canvasGL.drawBitmap(bmp[X1], x.left, y);  // 绘制X1标签
            x = rectMap.get(X2);  // 获取X2矩形
            y = x.top;  // 获取X2的Y坐标
            hh = rectMap.get(XDelta).height() + 50;  // 计算总高度
            if ( y> screenHeight - hh) y = screenHeight - hh;  // 超出下边界则钳位

            canvasGL.drawBitmap(bmp[X2], x.left, y);  // 绘制X2标签
            x = rectMap.get(XDelta);  // 获取ΔX矩形
            canvasGL.drawBitmap(bmp[XDelta], x.left, x.top);  // 绘制ΔX标签
        } else if (colVisible && CursorManage.getInstance().isLabelNotFollowCursor() && !rowVisible) {  // 列光标可见且标签固定且行光标不可见
            Rect x = rectMap.get(SingleBmp);  // 获取单独标签矩形
//            Rect x1 = rectMap.get(X1);
            canvasGL.drawBitmap(singleBmp, x.left, x.top);  // 绘制单独标签
//            canvasGL.drawBitmap(bmp[X1],x1.left,x1.top);
        }
        if (rowVisible && !CursorManage.getInstance().isLabelNotFollowCursor()) {  // 行光标可见且标签跟随
            Rect x = rectMap.get(Y1);  // 获取Y1矩形
            canvasGL.drawBitmap(bmp[Y1], x.left, x.top);  // 绘制Y1标签
            x = rectMap.get(Y2);  // 获取Y2矩形
            canvasGL.drawBitmap(bmp[Y2], x.left, x.top);  // 绘制Y2标签
            x = rectMap.get(YDelta);  // 获取ΔY矩形
            canvasGL.drawBitmap(bmp[YDelta], x.left, x.top);  // 绘制ΔY标签
        } else if (CursorManage.getInstance().isLabelNotFollowCursor() && rowVisible) {  // 行光标可见且标签固定
            Rect x = rectMap.get(SingleBmp);  // 获取单独标签矩形
            canvasGL.drawBitmap(singleBmp, x.left, x.top);  // 绘制单独标签
        }
        if (colVisible) {  // 列光标可见
            Rect x = rectMap.get(XArrow);  // 获取X箭头矩形
            int y = x.top;  // 获取箭头Y坐标
            int screenHeight = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());  // 获取波形区高度
            int hh = rectMap.get(XDelta).height() + 10;  // 计算总高度

            if ( y> screenHeight - hh) y = screenHeight - hh;  // 超出下边界则钳位

            canvasGL.drawBitmap(rowBmp, x.left, y);  // 绘制X方向箭头
        }
        if (rowVisible) {  // 行光标可见
            Rect y = rectMap.get(YArrow);  // 获取Y箭头矩形
            canvasGL.drawBitmap(colBmp, y.left, y.top);  // 绘制Y方向箭头
        }
    }

    /**
     * 计算位图在两个X坐标之间的水平居中位置
     *
     * @param x1  第一个X坐标
     * @param x2  第二个X坐标
     * @param bmp 位图
     * @return 居中的X坐标
     */
    private int CenterBmpX(int x1, int x2, Bitmap bmp) {  // X方向居中计算
        int min = Math.min(x1, x2);  // 取较小值
        int x = (Math.abs(x1 - x2) - bmp.getWidth()) / 2 + min;  // 计算居中X坐标
        return x;  // 返回X坐标
    }

    /**
     * 计算位图在两个Y坐标之间的垂直居中位置
     *
     * @param y1  第一个Y坐标
     * @param y2  第二一个Y坐标
     * @param bmp 位图
     * @return 居中的Y坐标
     */
    private int CenterBmpY(int y1, int y2, Bitmap bmp) {  // Y方向居中计算
        int min = Math.min(y1, y2);  // 取较小值
        int y = (Math.abs(y1 - y2) - bmp.getHeight()) / 2 + min;  // 计算居中Y坐标
        return y;  // 返回Y坐标
    }

    private int selectedIndex = -1;  // 当前选中的标签索引（-1表示未选中）

    /**
     * 根据触摸坐标选中对应的测量标签
     *
     * @param x 触摸X坐标
     * @param y 触摸Y坐标
     * @return 选中的光标类型索引，未选中返回-1
     */
    public int selectMeasure(int x, int y) {  // 选中测量标签
        if (EnableMeasure == false) return -1;  // 测量功能未启用返回-1
        Map.Entry<Integer, Rect> selected = null;  // 选中的条目
        for (Map.Entry<Integer, Rect> set : rectMap.entrySet()) {  // 遍历所有矩形
            if (set.getValue().contains(x, y) && (set.getKey() == XDelta || set.getKey() == YDelta)) {  // 命中ΔX或ΔY
                selected = set;  // 记录选中条目
                break;  // 跳出循环
            } else if (set.getValue().contains(x, y)) {  // 命中其他标签
                selected = set;  // 记录选中条目
                break;  // 跳出循环
            }
        }
        if (selected != null && !CursorManage.getInstance().isLabelNotFollowCursor()) {  // 有选中且标签跟随模式
            switch (selected.getKey()) {  // 根据选中标签类型返回对应光标类型
                case XDelta: {  // 选中ΔX
                    selectedIndex = TChan.Cursor_col_4;  // 选中列光标Delta
                    return TChan.Cursor_col_4;  // 返回列光标Delta类型
                }
                case YDelta: {  // 选中ΔY
                    selectedIndex = TChan.Cursor_row_4;  // 选中行光标Delta
                    return TChan.Cursor_row_4;  // 返回行光标Delta类型
                }
                case X1: {  // 选中X1
                    selectedIndex = -1;  // 不移动标签
                    return TChan.Cursor_col_1;  // 返回列光标1类型
                }
                case X2: {  // 选中X2
                    selectedIndex = -1;  // 不移动标签
                    return TChan.Cursor_col_2;  // 返回列光标2类型
                }
                case Y1: {  // 选中Y1
                    selectedIndex = -1;  // 不移动标签
                    return TChan.Cursor_row_1;  // 返回行光标1类型
                }
                case Y2: {  // 选中Y2
                    selectedIndex = -1;  // 不移动标签
                    return TChan.Cursor_row_2;  // 返回行光标2类型
                }
            }
        }else if(selected != null && CursorManage.getInstance().isLabelNotFollowCursor()){  // 有选中且标签固定模式
            switch (selected.getKey()) {  // 根据选中标签类型
                case SingleBmp: {  // 选中单独标签
                    selectedIndex = TChan.SingleRect;  // 选中单独矩形
                    return TChan.SingleRect;  // 返回单独矩形类型
                }
            }
        }

        return -1;  // 未选中返回-1
    }

    /**
     * 移动标签位置（标签跟随模式下的Delta标签或固定模式下的单独标签）
     *
     * @param offsetX X方向偏移量
     * @param offsetY Y方向偏移量
     */
    public void MoveLabel(int offsetX, int offsetY) {  // 移动标签
        int screenWidth = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());  // 获取波形区宽度
        int screenHeight = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());  // 获取波形区高度
        //Log.d("Tag.Debug", String.format("MoveLabel: [offsetX:%d, offsetY:%d]",screenWidth,screenHeight ));
//        int width=bmp[Y1].getWidth()+bmp[YDelta].getWidth()+40;
//        int height=bmp[X1].getHeight()+bmp[XDelta].getHeight()+110;
        int width = rectMap.get(YDelta).right - rectMap.get(Y1).left;  // 计算Y标签组宽度
        int height = rectMap.get(XDelta).bottom - rectMap.get(X1).top;  // 计算X标签组高度
        //设置标签位置，然后刷新
        if (selectedIndex == TChan.Cursor_col_4) {  // 选中列Delta（X方向标签组）
            labelY -= offsetY;  // 上下移动
            if (labelY < 0) labelY = 0;  // 不超出上边界
            if (labelY > screenHeight - height) labelY = screenHeight - height;  // 不超出下边界
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSOR_LABEL_Y, String.valueOf(labelY));  // 保存到缓存
        } else if (selectedIndex == TChan.Cursor_row_4) {  // 选中行Delta（Y方向标签组）
            labelX -= offsetX;  // 左右移动
            if (labelX < 0) labelX = 0;  // 不超出左边界
            if (labelX > screenWidth - width) labelX = screenWidth - width;  // 不超出右边界
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSOR_LABEL_X, String.valueOf(labelX));  // 保存到缓存
        } else if (selectedIndex == TChan.SingleRect) {  // 选中单独标签
            singleLabelX -= offsetX;  // 左右移动
            if (singleLabelX < 0) singleLabelX = 0;  // 不超出左边界
            width = singleBmp.getWidth();  // 获取单独标签宽度
            if (singleLabelX > screenWidth - width) singleLabelX = screenWidth - width;  // 不超出右边界
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSOR_SINGLE_LABLE_X, String.valueOf(singleLabelX));  // 保存到缓存
            singleLabelY -= offsetY;  // 上下移动
            if (singleLabelY < 0) singleLabelY = 0;  // 不超出上边界
            height = rectMap.get(SingleBmp).bottom - rectMap.get(SingleBmp).top;  // 获取单独标签高度
            if (singleLabelY > screenHeight - height) singleLabelY = screenHeight - height;  // 不超出下边界
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSOR_SINGLE_LABLE_Y, String.valueOf(singleLabelY));  // 保存到缓存
        }
        drawMeasure();  // 重新绘制
    }

    /**
     * 刷新标签位置（确保标签在屏幕范围内）
     */
    public void refreshLabelPos() {  // 刷新标签位置
        int screenWidth = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());  // 获取波形区宽度
        int screenHeight = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());  // 获取波形区高度
        int width = rectMap.get(YDelta).right - rectMap.get(Y1).left;  // 计算Y标签组宽度
        int height = rectMap.get(XDelta).bottom - rectMap.get(X1).top;  // 计算X标签组高度
        //设置标签位置，然后刷新
        if (CursorManage.getInstance().getColVisible()) {  // 列光标可见
            if (labelY < 0) labelY = 0;  // 不超出上边界
            if (labelY > screenHeight - height) labelY = screenHeight - height;  // 不超出下边界
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSOR_LABEL_Y, String.valueOf(labelY));  // 保存到缓存
        } else if (CursorManage.getInstance().getRowVisible()) {  // 行光标可见
            if (labelX < 0) labelX = 0;  // 不超出左边界
            if (labelX > screenWidth - width) labelX = screenWidth - width;  // 不超出右边界
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSOR_LABEL_X, String.valueOf(labelX));  // 保存到缓存
        }
        drawMeasure();  // 重新绘制
    }

    /**
     * 设置测量数据并触发重绘（外部调用入口）
     */
    public void setData() {  // 设置测量数据
        if (!colVisible && !rowVisible) return;  // 行列光标都不可见则返回
        if (WorkModeManage.getInstance().isXyMode()) {  // XY模式
            calDataXY();  // 计算XY模式测量数据
        } else {  // YT模式
            calDataYT();  // 计算YT模式测量数据
        }
        drawMeasure();  // 重新绘制
    }

    /**
     * 计算YT模式下的光标测量数据（X1/X2/ΔX/1/ΔX/Y1/Y2/ΔY等）
     * 支持光标跟踪模式和非跟踪模式
     */
    private void calDataYT() {  // 计算YT模式测量数据
//        if (CursorManage.getInstance().isCursorTrace()) return;
        int curCh = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE);  // 获取光标源通道
        if (curCh == TChan.AUTO) { //自由选择模式  // 自动选择模式
            curCh = WaveManage.get().getCurCh();  // 使用当前活动通道
        } else {  // 指定通道
            curCh = TChan.toUiChNo(curCh);  // 转换为UI通道号
        }
        if (curCh == -1 || Scope.getInstance().isUsersetReset()) {  // 无有效通道或用户重置中
            dataMap.put(X1, "X1:" + SPACE + "---");  // 显示X1无数据
            dataMap.put(X2, "X2:" + SPACE + "---");  // 显示X2无数据
            dataMap.put(XDelta, "ΔX:" + SPACE + "---");  // 显示ΔX无数据
            dataMap.put(XFre, "1/ΔX:" + SPACE + "---");  // 显示1/ΔX无数据
            dataMap.put(Y1, "Y1:" + SPACE + "---");  // 显示Y1无数据
            dataMap.put(Y2, "Y2:" + SPACE + "---");  // 显示Y2无数据
            dataMap.put(YDelta, "ΔY:" + SPACE + "---");  // 显示ΔY无数据
            dataMap.put(S, "S:" + SPACE + "---");  // 显示S无数据
            dataMap.put(X1CursorInteraction, SPACE + SPACE + SPACE + "---");  // 显示X1交互无数据
            dataMap.put(X2CursorInteraction, SPACE + SPACE + SPACE + "---");  // 显示X2交互无数据
            dataMap.put(VoltgaeDelta, "ΔV:" + SPACE + "---");  // 显示ΔV无数据
            dataMap.put(VoltageDivideXDelta, "ΔV/Δt:" + SPACE + "---");  // 显示ΔV/Δt无数据
            return;  // 返回
        }

        double curChY = WaveManage.get().getUIPositionY(curCh);  // 获取当前通道UI垂直位置
        long curTimeX = 0;  // 时间基准X位置（像素）
        double timeEvery = 0;  // 每像素对应的时间值
        int chIdx = TChan.toFpgaChNo(curCh);  // 获取FPGA通道索引
        if (ChannelFactory.isDynamicCh(chIdx)  // 动态通道
                || ChannelFactory.isMathCh(chIdx)  // 数学通道
                || ChannelFactory.isSerialCh(chIdx)) {  // 串行通道
            if (ChannelFactory.isMath_FFT_Ch(chIdx)) {  // FFT数学通道
                HorizontalAxisMath horizontalAxisMath = ChannelFactory.getMathChannel(chIdx).getHorizontalAxisMathFFT();  // 获取FFT水平轴
                curTimeX = ScopeBase.getWidth() / 2 - horizontalAxisMath.getXPosOfView();  // 计算时间基准X位置
                timeEvery = horizontalAxisMath.fftXScaleIdVal() / ScopeBase.getHorizonPerGridPixels();  // 计算每像素时间值（FFT）
            } else {  // 非FFT通道
                curTimeX = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix();  // 计算时间基准X位置
                timeEvery = HorizontalAxis.getInstance().getTimesPrePix();  // 获取每像素时间值
            }
        } else if (ChannelFactory.isRefCh(chIdx)) {  // 参考通道
            RefChannel refChannel = ChannelFactory.getRefChannel(chIdx);  // 获取参考通道实例
            curTimeX = ScopeBase.getWidth() / 2 - refChannel.getTimePoseOfViewPix();  // 计算时间基准X位置
            timeEvery = refChannel.getRefTimePerPix();  // 获取每像素时间值
        }

        double chEvery = 0;  // 每像素对应的电压值
        if (ChannelFactory.isDynamicCh(chIdx)) {  // 动态通道
            chEvery = ChannelFactory.getDynamicChannel(chIdx).getVerticalPerPix();  // 获取每像素电压值
        } else if (ChannelFactory.isMathCh(chIdx)) {  // 数学通道
            chEvery = ChannelFactory.getMathChannel(chIdx).getVerticalPerPix();  // 获取每像素电压值
        } else if (ChannelFactory.isRefCh(chIdx)) {  // 参考通道
            chEvery = ChannelFactory.getRefChannel(chIdx).getVerticalPerPix();  // 获取每像素电压值
        }
        if(!MeasureManage.getInstance().isCursorTValueTrace()) {  // 非T值跟踪模式
            MeasureService.setMeasureRange((int) CursorManage.getInstance().getCol1Position(),  // 设置测量范围（列光标1位置）
                    (int) CursorManage.getInstance().getCol2Position());  // 设置测量范围（列光标2位置）
        }

        double x1 = (CursorManage.getInstance().getCol1Position() - curTimeX) * timeEvery;  // 计算X1值（时间）
        double x2 = (CursorManage.getInstance().getCol2Position() - curTimeX) * timeEvery;  // 计算X2值（时间）
        double y1 = ((curChY - CursorManage.getInstance().getRow1Position()) * chEvery);  // 计算Y1值（电压）
        double y2 = ((curChY - CursorManage.getInstance().getRow2Position()) * chEvery);  // 计算Y2值（电压）


        if (CursorManage.getInstance().isCursorTrace() == false) {  // 非跟踪模式：保存测量值
//            Log.d("Tag.Debug", String.format("CursorLabel.calDataYT 保存测量值: x1:%s,x2:%s,y1:%s,y2:%s ,ch:%s",x1,x2,y1,y2,WaveManage.get().getCurCh() ));
//            try {
//                throw new Exception();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            if (CursorManage.getInstance().isFFT(curCh)) {  // FFT通道
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1_HZ, String.valueOf(x1));  // 保存FFT X1位置（Hz）
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2_HZ, String.valueOf(x2));  // 保存FFT X2位置（Hz）
            } else {  // 非FFT通道
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1, String.valueOf(x1));  // 保存X1位置（时间）
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2, String.valueOf(x2));  // 保存X2位置（时间）
            }
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y1, String.valueOf(y1));  // 保存Y1位置（电压）
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y2, String.valueOf(y2));  // 保存Y2位置（电压）
        }
        //else  if (CursorManage.getInstance().isAutoCursor() ||  CursorManage.getInstance().isSelectChanEqCursorChan())
        else if (CursorManage.getInstance().isEnableCursorTrance()) {  // 光标跟踪模式：恢复保存的测量值
            Log.d("HH_Tag.Debug", String.format("CursorLabel.calDataYT: 测量值不变:%s",WaveManage.get().getCurCh() ));  // 日志输出
            try {  // 调试用异常栈
                throw new Exception();  // 抛出异常以打印调用栈
            } catch (Exception e) {  // 捕获异常
                e.printStackTrace();  // 打印异常栈
            }
            if (CursorManage.getInstance().isColCursorTrace()) {  // 列光标跟踪
                double px1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1);  // 读取保存的X1位置
                double px2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2);  // 读取保存的X2位置
                if (CursorManage.getInstance().isFFT(WaveManage.get().getCurCh())) {  // FFT通道
                    px1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1_HZ);  // 读取FFT X1位置
                    px2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2_HZ);  // 读取FFT X2位置
                }
                long posX1 = Tools.TimebaseToPix(TChan.toFpgaChNo(WaveManage.get().getCurCh()), px1);  // 时间值转像素位置
                long posX2 = Tools.TimebaseToPix(TChan.toFpgaChNo(WaveManage.get().getCurCh()), px2);  // 时间值转像素位置
                x1 = (posX1 - curTimeX) * timeEvery;  // 重新计算X1值
                x2 = (posX2 - curTimeX) * timeEvery;  // 重新计算X2值
//                Log.d("Tag.Debug", String.format("CursorLabel.calDataYT: px1:%s,px2:%s,posX1:%s,posX2:%s,curTimeX:%s,timeEvery:%s,x1:%s,x2:%s",px1,px2,posX1,posX2,curTimeX,timeEvery,x1,x2 ));
//                try {
//                    throw new Exception();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }

            if (CursorManage.getInstance().isRowCursorTrace()) {  // 行光标跟踪
                int chanIdx = TChan.toFpgaChNo(curCh);  // 获取FPGA通道索引
                if (CursorManage.getInstance().isScpiChanEqCursorChan()) {  // SCPI通道与光标通道相同
//                    Log.d("Tag.Debug", String.format("CursorLabel.calDataYT scpi: %s",chanIdx ));
                    chanIdx = CursorManage.getInstance().getScpiChanIdx();  // 使用SCPI通道索引
                    curChY = WaveManage.get().getUIPositionY(TChan.toUiChNo(chanIdx));  // 获取SCPI通道UI位置
                    if (ChannelFactory.isDynamicCh(chanIdx)) {  // 动态通道
                        chEvery = ChannelFactory.getDynamicChannel(chanIdx).getVerticalPerPix();  // 获取每像素电压值
                    } else if (ChannelFactory.isMathCh(chanIdx)) {  // 数学通道
                        chEvery = ChannelFactory.getMathChannel(chanIdx).getVerticalPerPix();  // 获取每像素电压值
                    } else if (ChannelFactory.isRefCh(chanIdx)) {  // 参考通道
                        chEvery = ChannelFactory.getRefChannel(chanIdx).getVerticalPerPix();  // 获取每像素电压值
                    }

                }
                double py1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y1);  // 读取保存的Y1位置
                double py2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y2);  // 读取保存的Y2位置
                double posY1 = Tools.ScaleToPix(chanIdx/*TChan.toFpgaChNo(WaveManage.get().getCurCh())*/, py1);  // 电压值转像素位置
                double posY2 = Tools.ScaleToPix(chanIdx/*TChan.toFpgaChNo(WaveManage.get().getCurCh())*/, py2);  // 电压值转像素位置

                y1 = ( curChY - posY1) * chEvery;  // 重新计算Y1值
                y2 = ( curChY - posY2) * chEvery;  // 重新计算Y2值
//                Log.d("Tag.Debug", String.format("CursorLabel.calDataYT: py1:%s ,py2:%s,posY1:%s,posY2:%s",py1,py2,posY1,posY2 ));
//                Log.d("Tag.Debug", String.format("CursorLabel.calDataYT: y1:%s,y2:%s ,chIdx:%s ,curChy:%s,chEvery:%s",y1,y2,chanIdx,curChY,chEvery ));
            }
//            Log.d("Tag.Debug", String.format("CursorLabel.calDataYT:  x1:%s ,posX1:%s ,curTimex:%s ,timeEvery:%s",x1,posX1,curTimeX,timeEvery ));
//            Log.d("Tag.Debug", String.format("CursorLabel.calDataYT:py1:%s,  y1:%s ,posY1:%s ,curChY:%s ,chEvery:%s",py1, y1,posY1,curChY,chEvery ));
//            Log.d("Tag.Debug", String.format("CursorLabel.calDataYT:py2:%s,  y2:%s ,posY2:%s ,curChY:%s ,chEvery:%s",py2, y2,posY2,curChY,chEvery ));
        }


        double deltaY = Math.abs(y1 - y2);  // 计算ΔY绝对值
        double deltaX = Math.abs(x1 - x2);  // 计算ΔX绝对值
        xDeltaCalculate = deltaX;  // 保存ΔX计算值
        String yUnit = ChannelFactory.getProbeType(TChan.toFpgaChNo(curCh));  // 获取Y轴单位（电压单位）
        String xUnit;  // X轴单位
        if (ChannelFactory.isMath_FFT_Ch(TChan.toFpgaChNo(curCh))  // FFT数学通道
                || (ChannelFactory.isRefCh(TChan.toFpgaChNo(curCh)) && ChannelFactory.getRefChannel(TChan.toFpgaChNo(curCh)).getRefType() == 2)) {  // FFT参考通道
            xUnit = "Hz";  // X轴单位为Hz
        } else {  // 普通通道
            xUnit = "s";  // X轴单位为秒
        }
        xDeltaUnit = xUnit;  // 保存ΔX单位
        if (TChan.isSerial(curCh)) {  // 串行通道（不显示Y值）
            dataMap.put(X1, "X1:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(x1) + xUnit));  // 设置X1显示值
            dataMap.put(X2, "X2:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(x2) + xUnit));  // 设置X2显示值
            dataMap.put(XDelta, "ΔX" + SPACE + String.valueOf(TBookUtil.getFourFromD_(deltaX) + xUnit));  // 设置ΔX显示值
            dataMap.put(XFre, "1/ΔX:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(1.0 / deltaX) + (xUnit.equals("s") ? "Hz" : "s")));  // 设置1/ΔX显示值（频率）
            dataMap.put(Y1, "Y1:" + SPACE + "---");  // 串行通道不显示Y1
            dataMap.put(Y2, "Y2:" + SPACE + "---");  // 串行通道不显示Y2
            dataMap.put(YDelta, "ΔY:" + SPACE + "---");  // 串行通道不显示ΔY
            dataMap.put(S, "ΔV/Δt:" + SPACE + "---");  // 串行通道不显示斜率

        } else {  // 普通通道（显示所有值）
            dataMap.put(X1, "X1:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(x1) + xUnit));  // 设置X1显示值
            dataMap.put(X2, "X2:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(x2) + xUnit));  // 设置X2显示值
            dataMap.put(XDelta, "ΔX:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(deltaX) + xUnit));  // 设置ΔX显示值
            dataMap.put(XFre, "1/ΔX:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(1.0 / deltaX) + (xUnit.equals("s") ? "Hz" : "s")));  // 设置1/ΔX显示值
            dataMap.put(Y1, "Y1:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(y1) + yUnit));  // 设置Y1显示值
            dataMap.put(Y2, "Y2:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(y2) + yUnit));  // 设置Y2显示值
            dataMap.put(YDelta, "ΔY:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(deltaY) + yUnit));  // 设置ΔY显示值
            dataMap.put(S, "ΔV/Δt:" + SPACE + String.valueOf(TBookUtil.getFourFromD_(deltaY * 1.0 / deltaX) + yUnit + "/" + xUnit));  // 设置ΔV/Δt显示值
        }
        Command.get().getCursor().setCursorMeasureInfo(y1, y2, deltaY, x1, x2, deltaX, 1.0 / deltaX, deltaY * 1.0 / deltaX);  // 发送光标测量信息命令

        if (colVisible || !rowVisible) {  // 列光标可见或行光标不可见
            int pX1 = (int) getCursor(TChan.Cursor_col_1);  // 获取列光标1像素位置
            int pX2 = (int) getCursor(TChan.Cursor_col_2);  // 获取列光标2像素位置
            Measure measure = getHardwareMeasure(curCh-1);  // 获取硬件测量实例
            double px1Position = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1);  // 读取X1缓存位置
            double px2Position = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2);  // 读取X2缓存位置
            if (CursorManage.getInstance().isFFT(WaveManage.get().getCurCh())) {  // FFT通道
                px1Position = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1_HZ);  // 读取FFT X1位置
                px2Position = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2_HZ);  // 读取FFT X2位置
            }
            long posX1 = Tools.TimebaseToPix(chIdx,px1Position);  // 时间值转像素位置
            long posX2 = Tools.TimebaseToPix(chIdx,px2Position);  // 时间值转像素位置
            if(posX1<0 || posX1 >1799){  // X1超出屏幕范围
                pX1 = -1;  // 设为无效值
            }
            if(posX2<0 || posX2 >1799){  // X2超出屏幕范围
                pX2 = -1;  // 设为无效值
            }

            if (ChannelFactory.isChOpen(curCh-1)  // 通道已打开
                    && measure != null) {  // 测量实例有效
                measure.setMeasureCursor(pX1, pX2);  // 设置测量光标位置
                measure.MeasureItemEnable(MEASURE_CURSOR_X1, true);  // 启用X1测量项
                measure.MeasureItemEnable(MEASURE_CURSOR_X2, true);  // 启用X2测量项
            }
        }

    }

    /**
     * 计算XY模式下的光标测量数据
     * XY模式下X轴使用CH1电压，Y轴使用CH2电压
     */
    private void calDataXY() {  // 计算XY模式测量数据
        double curChX = WaveManage.get().getPositionY(TChan.Ch1);  // 获取CH1垂直位置（XY模式下为X轴）
        double curChY = WaveManage.get().getPositionY(TChan.Ch2);  // 获取CH2垂直位置（XY模式下为Y轴）
        double chEveryX = ChannelFactory.getDynamicChannel(ChannelFactory.CH1).getVerticalPerPix();  // 获取CH1每像素电压值
        double chEveryY = ChannelFactory.getDynamicChannel(ChannelFactory.CH2).getVerticalPerPix();  // 获取CH2每像素电压值
        double x1 = (getCursor(TChan.Cursor_col_1) - curChX) * chEveryX;  // 计算X1值（CH1电压）
        double x2 = (getCursor(TChan.Cursor_col_2) - curChX) * chEveryX;  // 计算X2值
        double y1 = (curChY - getCursor(TChan.Cursor_row_1)) * chEveryY;  // 计算Y1值（CH2电压）
        double y2 = (curChY - getCursor(TChan.Cursor_row_2)) * chEveryY;  // 计算Y2值
        double deltaY = Math.abs(y1 - y2);  // 计算ΔY绝对值
        double deltaX = Math.abs(x1 - x2);  // 计算ΔX绝对值
        String unitX = ChannelFactory.getProbeType(ChannelFactory.CH1);  // 获取X轴单位
        String unitY = ChannelFactory.getProbeType(ChannelFactory.CH2);  // 获取Y轴单位
        dataMap.put(X1, "X1:" + SPACE + (TBookUtil.getFourFromD_(x1) + unitX));  // 设置X1显示值
        dataMap.put(X2, "X2:" + SPACE + (TBookUtil.getFourFromD_(x2) + unitX));  // 设置X2显示值
        dataMap.put(XDelta, "ΔX:" + SPACE + (TBookUtil.getFourFromD_(deltaX) + unitX));  // 设置ΔX显示值
        dataMap.put(XFre, "");  // XY模式不显示频率
        dataMap.put(Y1, "Y1:" + SPACE + (TBookUtil.getFourFromD_(y1) + unitY));  // 设置Y1显示值
        dataMap.put(Y2, "Y2:" + SPACE + (TBookUtil.getFourFromD_(y2) + unitY));  // 设置Y2显示值
        dataMap.put(YDelta, "ΔY:" + SPACE + (TBookUtil.getFourFromD_(deltaY) + unitY));  // 设置ΔY显示值
        dataMap.put(S, "ΔV/Δt:" + SPACE + TBookUtil.getFourFromD_(deltaY * 1.0 / deltaX) + unitY + "/" + unitX);  // 设置斜率显示值
//        dataMap.put(X1CursorInteraction, SPACE+SPACE + SPACE + "---" );
//        dataMap.put(X2CursorInteraction, SPACE+SPACE + SPACE + "---");
//        dataMap.put(VoltgaeDelta,"ΔV:" + SPACE + "---");
//        dataMap.put(VoltageDivideXDelta,"ΔV/Δt:" + SPACE + "---");
        Command.get().getCursor().setCursorMeasureInfo(y1, y2, deltaY, x1, x2, deltaX, 0, 0);  // 发送XY模式光标测量信息
    }

    /**
     * 设置行光标测量可见性
     *
     * @param b true显示行光标测量，false隐藏
     */
    public void setRowMeasureVisible(boolean b) {  // 设置行测量可见性
        this.rowVisible = b;  // 保存行光标可见性
//        drawMeasure();
        setData();  // 更新数据并重绘

    }

    /**
     * 设置列光标测量可见性
     *
     * @param b true显示列光标测量，false隐藏
     */
    public void setColMeasureVisible(boolean b) {  // 设置列测量可见性
        this.colVisible = b;  // 保存列光标可见性
//        drawMeasure();
        setData();  // 更新数据并重绘
    }


    /**
     * 测量更新事件观察者，监听通道/数学/参考通道的测量值变化
     * 更新X1/X2光标交互测量值、ΔV和ΔV/Δt
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() {  // 测量事件观察者
        @Override  // 覆写update方法
        public void update(Object data) {  // 事件更新回调
            EventBase eventBase = (EventBase) data;  // 转换为事件基类
            int id = eventBase.getId();  // 获取事件ID
            if (id== EventFactory.EVENT_CH_MEASURE_UPDATE  // 通道测量更新
                    || id == EventFactory.EVENT_MATH_MEASURE_UPDATE  // 数学通道测量更新
                    || id == EventFactory.EVENT_REF_MEASURE_UPDATE) {  // 参考通道测量更新
                int chan = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE);  // 获取光标源通道
                if (chan == TChan.AUTO) { //自由选择模式  // 自动选择模式
                    chan = WaveManage.get().getCurCh();  // 使用当前活动通道
                } else {  // 指定通道
                    chan = TChan.toUiChNo(chan);  // 转换为UI通道号
                }
                int chIdx = TChan.toFpgaChNo(chan);  // 获取FPGA通道索引

                switch(id){  // 根据事件类型过滤
                    case EventFactory.EVENT_CH_MEASURE_UPDATE:  // 通道测量更新
                        if(!ChannelFactory.isDynamicCh(chIdx)){  // 非动态通道
                            return;  // 不处理
                        }
                        break;  // 跳出
                    case EventFactory.EVENT_MATH_MEASURE_UPDATE:  // 数学通道测量更新
                        if(!ChannelFactory.isMathCh(chIdx)){  // 非数学通道
                            return;  // 不处理
                        }
                        break;  // 跳出
                    case EventFactory.EVENT_REF_MEASURE_UPDATE:  // 参考通道测量更新
                        if(!ChannelFactory.isRefCh(chIdx)){  // 非参考通道
                            return;  // 不处理
                        }
                        break;  // 跳出
                }

                Measure measure = getHardwareMeasure(chIdx);  // 获取硬件测量实例
                int idx1 = MEASURE_CURSOR_X1;  // X1测量项索引
                int idx2 = MEASURE_CURSOR_X2;  // X2测量项索引
                //形式计算单位用v方便后续函数换算，但实际显示单位是实时获取的，并在计算后添加到计算结果后面
                String realUnitY = ChannelFactory.getProbeType(TChan.toFpgaChNo(chan));  // 获取实际电压单位
                String unitY = "V";  // 内部计算使用V单位
//                Log.d("TAG", "ch: " + measure.getChIdx() + ",update: "  + measure.isMeasureItemValid(idx1));
                float itemCursorX1Interaction = 0.0F;  // X1交互测量值
                float itemCursorX2Interaction = 0.0F;  // X2交互测量值
                if (ChannelFactory.isChOpen(chan-1) && measure != null) {  // 通道打开且测量有效
                    //如果idx1，idx2有效就分别放数据， 全都有效则更新 v/t数据，如果有一个不有效就失效单个数据以及v/t
                    if(measure.isMeasureItemValid(idx1) || measure.isMeasureItemValid(idx2)) {  // 任一测量项有效
                        if(measure.isMeasureItemValid(idx1)){  // X1测量项有效
                            itemCursorX1Interaction = measure.getMeasureItemVal(idx1);  // 获取X1交互测量值
                            if (CursorManage.getInstance().isLabelNotFollowCursor()) {  // 标签固定模式
                                dataMap.put(X1CursorInteraction, SPACE + SPACE + SPACE + TBookUtil.getFourFromD_(itemCursorX1Interaction) + realUnitY);  // 设置X1交互值
                            } else {  // 标签跟随模式
                                dataMap.put(X1CursorInteraction, SPACE + SPACE + SPACE + TBookUtil.getFourFromD_(itemCursorX1Interaction) + realUnitY);  // 设置X1交互值
                            }
                        }
                        if(measure.isMeasureItemValid(idx2)){  // X2测量项有效
                            itemCursorX2Interaction = measure.getMeasureItemVal(idx2);  // 获取X2交互测量值
                            if (CursorManage.getInstance().isLabelNotFollowCursor()) {  // 标签固定模式
                                dataMap.put(X2CursorInteraction, SPACE + SPACE + SPACE + TBookUtil.getFourFromD_(itemCursorX2Interaction) + realUnitY);  // 设置X2交互值
                            } else {  // 标签跟随模式
                                dataMap.put(X2CursorInteraction, SPACE + SPACE + SPACE + TBookUtil.getFourFromD_(itemCursorX2Interaction) + realUnitY);  // 设置X2交互值
                            }
                        }
                        float deltaVolatage = Math.abs(itemCursorX2Interaction - itemCursorX1Interaction);  // 计算ΔV绝对值
                        dataMap.put(VoltgaeDelta, "ΔV:" + SPACE + TBookUtil.getFourFromD_(Math.abs(itemCursorX2Interaction - itemCursorX1Interaction)) + realUnitY);  // 设置ΔV显示值
                        String voltageTime;  // ΔV/Δt结果字符串
                        if (xDeltaUnit != null) {  // ΔX单位有效
                            String voltageUnitDataDetail = TBookUtil.getFourFromD_(Math.abs(itemCursorX2Interaction - itemCursorX1Interaction)) + unitY;  // 构建电压+单位字符串
                            String voltageUnitDetail = voltageUnitDataDetail.substring(voltageUnitDataDetail.length() - 2);  // 提取单位后缀
                            realUnitY = voltageUnitDataDetail.charAt(voltageUnitDataDetail.length() - 2) + realUnitY;  // 组合前缀+单位
                            //传递电压，需要固定电压单位，然后for循环转换时间单位
                            if (deltaVolatage != 0) {  // 电压差不为0
                                voltageTime = calculateVoltageOverTime(deltaVolatage, voltageUnitDetail, realUnitY, xDeltaCalculate);  // 计算ΔV/Δt
                            } else {  // 电压差为0
                                voltageTime = "---";  // 显示无数据
                            }
                            dataMap.put(VoltageDivideXDelta, "ΔV/Δt:" + SPACE + voltageTime);  // 设置ΔV/Δt显示值
                        }
                    }
                    if(!measure.isMeasureItemValid(idx1) || !measure.isMeasureItemValid(idx2)){  // 任一测量项无效
                        if(!measure.isMeasureItemValid(idx1)){  // X1无效
                            if (CursorManage.getInstance().isLabelNotFollowCursor()) {  // 标签固定模式
                                dataMap.put(X1CursorInteraction, SPACE + SPACE + SPACE + "---");  // 设置X1交互无数据
                            } else {  // 标签跟随模式
                                dataMap.put(X1CursorInteraction, "---");  // 设置X1交互无数据
                            }
                        }
                        if(!measure.isMeasureItemValid(idx2)){  // X2无效
                            if (CursorManage.getInstance().isLabelNotFollowCursor()) {  // 标签固定模式
                                dataMap.put(X2CursorInteraction, SPACE + SPACE + SPACE + "---");  // 设置X2交互无数据
                            } else {  // 标签跟随模式
                                dataMap.put(X2CursorInteraction, "---");  // 设置X2交互无数据
                            }
                        }
                        dataMap.put(VoltgaeDelta, "ΔV:" + SPACE + "---");  // 设置ΔV无数据
                        dataMap.put(VoltageDivideXDelta, "ΔV/Δt:" + SPACE + "---");  // 设置ΔV/Δt无数据
                    }

                }
                drawMeasure();  // 重新绘制
            }
        }
    };

    /**
     * 计算电压除以时间的值，并返回指定单位的结果
     *
     * @param voltageValue 电压数值
     * @param voltageUnit  电压单位 (V, mV, kV, uV, μV)
     * @param timeValue    时间数值
     * @return 计算结果
     * @throws IllegalArgumentException 如果单位不支持
     */
    public static String calculateVoltageOverTime(
            double voltageValue,
            String voltageUnit,
            String realUnit,
            double timeValue) {  // 计算ΔV/Δt

        // 标准化单位字符串（去除空格，转小写用于查找）
        String vUnit = voltageUnit.trim();  // 去除空格
        double voltageInVolts = voltageValue;  // 电压值
        double timeInSeconds = timeValue;  // 时间值
        // 计算基础结果 (V/s)
        double resultInVs = voltageInVolts / timeInSeconds;  // 计算V/s

        double resultVoltage = convertToVoltageUnit(resultInVs, vUnit);  // 转换为目标电压单位
        String result = autoConvert(resultVoltage, realUnit);  // 自动选择最佳时间单位
        return result;  // 返回格式化结果
    }


    /**
     * 将 V/s 转换为指定电压单位的数值（仍以 /s 为时间单位）
     *
     * @param voltsPerSecond 每秒伏特值
     * @param voltageUnit    目标电压单位
     * @return 转换后的数值
     */
    private static double convertToVoltageUnit(double voltsPerSecond, String voltageUnit) {  // 转换电压单位
        switch (voltageUnit) {  // 根据单位转换
            case "V":  // 伏特
                return voltsPerSecond;  // 不转换
            case "mV":  // 毫伏
                return voltsPerSecond * 1000;    // 1 V = 1000 mV
            case "kV":  // 千伏
                return voltsPerSecond / 1000;    // 1 kV = 1000 V
            case "MV":  // 兆伏
                return voltsPerSecond / 1000000; // 1 MV = 1e6 V
            case "μV":  // 微伏（Unicode）
            case "uV":  // 微伏（ASCII）
                return voltsPerSecond * 1000000; // 1 V = 1e6 μV
            case "nV":  // 纳伏
                return voltsPerSecond * 1000000000;  // 1 V = 1e9 nV
            case "pV":  // 皮伏
                return voltsPerSecond * 1000000000000L;  // 1 V = 1e12 pV
            default:  // 未知单位
                return 0;  // 返回0
        }
    }

    /**
     * 自动选择最佳时间单位，使数值在0.1~1000范围内
     *
     * @param rateValue   速率值（电压单位/s）
     * @param voltageUnit 电压单位字符串
     * @return 格式化的速率字符串
     */
    public static String autoConvert(double rateValue, String voltageUnit) {  // 自动转换单位
        // 秒的单位数组 + 秒与该单位的换算因子
        String[] timeUnits = {"s", "ms", "μs", "ns", "ps"};  // 时间单位数组
        double[] factors = {1, 1_000, 1_000_000, 1_000_000_000, 1_000_000_000_000L};  // 换算因子

        for (int i = 0; i < timeUnits.length; i++) {  // 遍历时间单位
            double value = rateValue / factors[i];  // 计算当前单位下的值
            // 判断这个时间单位是否让值更直观
            if (value >= 0.1 && value < 1000) {  // 值在合理范围内
                String formatted = formatMaxDigits(value, 4);  // 格式化为最多4位有效数字
                return formatted + voltageUnit + "/" + timeUnits[i];  // 返回带单位的字符串
            }
        }

        // 如果都不合适，返回原速率
        return String.format("%.6f %s/s", rateValue, voltageUnit);  // 返回V/s格式
    }

    /**
     * 格式化数字，保留最多maxDigits位有效数字
     *
     * @param num       数字值
     * @param maxDigits 最大有效数字位数
     * @return 格式化后的字符串
     */
    private static String formatMaxDigits(double num, int maxDigits) {  // 格式化有效数字
        String intPart = String.valueOf((long) Math.floor(num));  // 整数部分字符串
        int integerLength = intPart.length();  // 整数部分位数

        // 可保留小数位数
        int decimalPlaces = Math.max(0, maxDigits - integerLength);  // 计算小数位数
        return String.format("%." + decimalPlaces + "f", num);  // 格式化输出
    }

    /**
     * 获取指定通道的硬件测量实例
     *
     * @param chId 通道FPGA索引
     * @return Measure实例，通道未开启或无测量返回null
     */
    private Measure getHardwareMeasure(int chId) {  // 获取硬件测量实例
        BaseChannel baseChannel = null;  // 通道基类引用
        if (ChannelFactory.isDynamicCh(chId)) {  // 动态通道
            baseChannel = ChannelFactory.getDynamicChannel(chId);  // 获取动态通道
        } else if (ChannelFactory.isMathCh(chId)) {  // 数学通道
            baseChannel = ChannelFactory.getMathChannel(chId);  // 获取数学通道
        } else if (ChannelFactory.isRefCh(chId)) {  // 参考通道
            baseChannel = ChannelFactory.getRefChannel(chId);  // 获取参考通道
        }
        if (baseChannel != null) {  // 通道有效
            return baseChannel.getMeasure();  // 返回测量实例
        }
        return null;  // 无效返回null
    }
    //endregion

    /**
     * 更新标签字符串宽度限制，截断过长的文本
     */
    private void updateStringWidth(){  // 更新字符串宽度

        String ellipsizedVoltageDivideX1 = TextUtils.ellipsize(  // 截断X1交互文本
                dataMap.get(X1CursorInteraction),  // 获取原始文本
                textPaint,  // 文字画笔
                100,  // 最大宽度100像素
                TextUtils.TruncateAt.END  // 末尾截断
        ).toString();  // 转为字符串
        dataMap.put(X1CursorInteraction,ellipsizedVoltageDivideX1);  // 保存截断后的文本

        String ellipsizedVoltageDivideX2 = TextUtils.ellipsize(  // 截断X2交互文本
                dataMap.get(X2CursorInteraction),  // 获取原始文本
                textPaint,  // 文字画笔
                100,  // 最大宽度100像素
                TextUtils.TruncateAt.END  // 末尾截断
        ).toString();  // 转为字符串
        dataMap.put(X2CursorInteraction,ellipsizedVoltageDivideX2);  // 保存截断后的文本

        String ellipsizedY1 = TextUtils.ellipsize(  // 截断Y1文本
                dataMap.get(Y1),  // 获取原始文本
                textPaint,  // 文字画笔
                125,  // 最大宽度125像素
                TextUtils.TruncateAt.END  // 末尾截断
        ).toString();  // 转为字符串
        dataMap.put(Y1,ellipsizedY1);  // 保存截断后的文本
        String ellipsizedY2 = TextUtils.ellipsize(  // 截断Y2文本
                dataMap.get(Y2),  // 获取原始文本
                textPaint,  // 文字画笔
                125,  // 最大宽度125像素
                TextUtils.TruncateAt.END  // 末尾截断
        ).toString();  // 转为字符串
        dataMap.put(Y2,ellipsizedY2);  // 保存截断后的文本
        String ellipsizedYDelta = TextUtils.ellipsize(  // 截断ΔY文本
                dataMap.get(YDelta),  // 获取原始文本
                textPaint,  // 文字画笔
                125,  // 最大宽度125像素
                TextUtils.TruncateAt.END  // 末尾截断
        ).toString();  // 转为字符串
        dataMap.put(YDelta,ellipsizedYDelta);  // 保存截断后的文本
    }
}
