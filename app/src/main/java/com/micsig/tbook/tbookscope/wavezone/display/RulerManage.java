package com.micsig.tbook.tbookscope.wavezone.display;  // 标尺管理类所在包

import android.content.Context;  // Android上下文，用于获取资源
import android.graphics.Bitmap;  // 位图，用于离屏绘制标尺刻度
import android.graphics.Canvas;  // 画布，用于在Bitmap上绘制文字
import android.graphics.Color;  // 颜色常量
import android.graphics.Paint;  // 画笔，用于绘制文字和清除
import android.graphics.PorterDuff;  // PorterDuff混合模式
import android.graphics.PorterDuffXfermode;  // Xfermode封装，用于混合模式设置
import android.graphics.Rect;  // 矩形，用于测量文字边界
import android.graphics.Xfermode;  // 混合模式基类
import android.text.TextPaint;  // 文字画笔，用于绘制描边文字

import com.chillingvan.canvasgl.ICanvasGL;  // OpenGL画布接口，用于纹理绘制
import com.micsig.base.DoubleUtil;  // 双精度工具类，用于取整运算
import com.micsig.base.Logger;  // 日志工具
import com.micsig.tbook.scope.Event.EventBase;  // 事件基类
import com.micsig.tbook.scope.Event.EventFactory;  // 事件工厂，用于注册/分发事件
import com.micsig.tbook.scope.Event.EventUIObserver;  // UI事件观察者
import com.micsig.tbook.scope.Scope;  // 示波器核心实例
import com.micsig.tbook.scope.ScopeBase;  // 示波器基础参数（网格像素、坐标转换系数）
import com.micsig.tbook.scope.channel.Channel;  // 通道基类（动态通道）
import com.micsig.tbook.scope.channel.ChannelFactory;  // 通道工厂，获取通道实例和状态
import com.micsig.tbook.scope.channel.MathChannel;  // 数学运算通道
import com.micsig.tbook.scope.channel.RefChannel;  // 参考通道
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 水平轴，时基参数
import com.micsig.tbook.tbookscope.GlobalVar;  // 全局变量，波形区尺寸
import com.micsig.tbook.tbookscope.R;  // 资源ID
import com.micsig.tbook.tbookscope.main.mainbottom.MainBottomMsgTimeBase;  // 底部时基消息
import com.micsig.tbook.tbookscope.main.mainbottom.MainHolderBottom;  // 底部时基持有者
import com.micsig.tbook.tbookscope.main.maincenter.MainCenterMsgChannels;  // 中间区域通道选择消息
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum;  // 消息队列枚举
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgMath;  // 右侧数学运算消息
import com.micsig.tbook.tbookscope.rxjava.RxBus;  // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister;  // RxJava事件总线注册辅助
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.Tools;  // 工具类，文字边界测量
import com.micsig.tbook.tbookscope.util.App;  // 应用全局上下文
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;  // 工作模式接口
import com.micsig.tbook.tbookscope.wavezone.trigger.MainWaveMsgTriggerTimeBase;  // 触发时基消息
import com.micsig.tbook.ui.util.TBookUtil;  // UI工具类，数值格式化
import com.micsig.tbook.ui.wavezone.TChan;  // 通道颜色映射

import java.util.Arrays;  // 数组工具，用于填充

import io.reactivex.rxjava3.functions.Consumer;  // RxJava消费者接口


/*
 * +=============================================================================+
 * |                        RulerManage — 标尺管理器                              |
 * +=============================================================================+
 * | 模块定位：tbookscope.wavezone.display 显示层                                 |
 * | 核心职责：管理波形区垂直/水平标尺刻度的计算与绘制，实时显示电压/时间刻度值     |
 * | 架构设计：单例模式 + 事件驱动，实现IWorkMode接口支持YT/XY模式切换            |
 * |           采用Bitmap离屏绘制+OpenGL纹理渲染架构                              |
 * | 数据流向：通道/时基/触发事件 → RxBus/EventFactory → drawRowBmp/drawColBmp  |
 * |           → Bitmap离屏绘制 → ICanvasGL纹理渲染 → 屏幕显示                   |
 * | 依赖关系：ChannelFactory（通道工厂）、HorizontalAxis（水平轴）、              |
 * |           ScopeBase（坐标转换）、TBookUtil（数值格式化）、                     |
 * |           RxBus/EventFactory（事件总线）、ICanvasGL（OpenGL渲染）             |
 * | 使用场景：波形区右侧显示垂直电压刻度、底部显示水平时间/频率刻度              |
 * |           通道切换/时基变更/触发电平变化时自动刷新标尺                        |
 * +=============================================================================+
 */

/**
 * @auother Liwb
 * @description:
 * @data:2023-8-15 14:02
 */
public class RulerManage implements IWorkMode {  // 标尺管理类，实现IWorkMode支持工作模式切换

    /** 日志标签 */  // 日志TAG
    private static final String TAG = "RulerManage";  // 日志标签
    /** 单例实例 */  // 单例引用
    private static RulerManage ins=null;  // 单例实例，延迟初始化

    /**
     * 获取RulerManage单例实例（懒汉模式，非线程安全）
     *
     * @return RulerManage单例
     */
    public static RulerManage getIns(){  // 获取单例实例
        if (ins==null){  // 若实例为空
            ins=new RulerManage();  // 创建新实例
        }
        return ins;  // 返回实例
    }


    /**
     * 构造函数，初始化视图和事件控制
     */
    public RulerManage(){  // 构造函数
        initView();  // 初始化画笔和位图
        initControl();  // 初始化事件监听
    }

    /**
     * 空初始化方法（预留扩展）
     */
    public void init(){  // 初始化入口（当前为空实现）

    }

    /** 行标尺位图数组（垂直刻度），共11格 */  // 竖向刻度位图数组
    private Bitmap rowBmp[]=new Bitmap[11];//竖向刻度
    /** 列标尺位图数组（水平刻度），共12格 */  // 横向刻度位图数组
    private Bitmap colBmp[]=new Bitmap[12];//横向刻度
    /** 普通画笔，用于绘制刻度文字和清除操作 */  // 普通画笔
    private Paint paint;  // 绘制画笔
    /** 描边文字画笔，用于绘制文字描边效果（提高可读性） */  // 描边文字画笔
    private TextPaint textPaint;  // 描边文字画笔
    /** 离屏画布，用于在Bitmap上绘制 */  // 离屏画布
    private Canvas mCanvas;  // 离屏Canvas
    /** CLEAR混合模式，用于清除位图内容 */  // 清除混合模式
    private Xfermode modeClear=new PorterDuffXfermode(PorterDuff.Mode.CLEAR);  // CLEAR模式，擦除
    /** SRC混合模式，用于正常绘制覆盖 */  // SRC混合模式
    private Xfermode modeSrc=new PorterDuffXfermode(PorterDuff.Mode.SRC);  // SRC模式，覆盖绘制
    /** 标记是否已绘制，用于纹理刷新控制 */  // 绘制标记，volatile保证可见性
    private  volatile boolean isDraw=false;  // 是否已绘制标记
    /** OpenGL画布引用，用于纹理刷新 */  // OpenGL画布引用
    private ICanvasGL canvasGL;  // OpenGL画布引用
    /** 应用上下文，用于获取颜色资源 */  // 应用上下文
	private Context context=App.get().getApplicationContext();  // 获取应用上下文
    /** 标尺是否可见标志 */  // 标尺可见性标志
    private boolean bShow=true;  // 默认显示标尺

    /**
     * 设置标尺是否可见
     *
     * @param bShow true显示标尺，false隐藏标尺
     */
    public void setShow(boolean bShow){  // 设置标尺可见性
        this.bShow=bShow;  // 赋值可见性标志
    }

    /**
     * 查询标尺是否可见
     *
     * @return true标尺可见，false标尺隐藏
     */
    public boolean isShow(){  // 查询标尺是否可见
        return bShow;  // 返回可见性标志
    }

    /**
     * 通知OpenGL画布刷新纹理
     */
    public void onRefresh(){  // 刷新纹理
        if(canvasGL != null){  // 画布引用不为空
            canvasGL.onRefreshTexture();  // 通知OpenGL刷新纹理内容
        }
    }

    /** 每个水平网格的像素宽度（波形区宽度/12） */  // 水平网格像素宽度
    private int perHorGridPx = GlobalVar.get().getMainWave().x / 12, perVerGridPx = GlobalVar.get().getMainWave().y / 10;  // 初始化每格像素数

    /**
     * 初始化视图组件：创建画笔和位图
     */
    private void initView(){  // 初始化视图
        paint=new Paint();  // 创建普通画笔
        paint.setTextSize(16);  // 设置文字大小16px
        paint.setAntiAlias(true);  // 开启抗锯齿
//        paint.setTypeface(Typeface.SERIF);
        textPaint=new TextPaint();  // 创建描边文字画笔
        textPaint.setTextSize(16);  // 设置文字大小16px
        textPaint.setAntiAlias(true);  // 开启抗锯齿
        textPaint.setStyle(Paint.Style.STROKE);  // 描边样式
        textPaint.setColor(Color.BLACK);  // 描边颜色黑色
        textPaint.setStrokeWidth(4);  // 描边宽度4px

        mCanvas=new Canvas();  // 创建离屏画布（不绑定Bitmap）
        for(int i=0;i<rowBmp.length;i++){  // 遍历行标尺数组
            rowBmp[i]=Bitmap.createBitmap(100,20, Bitmap.Config.ARGB_8888);  // 创建100x20的ARGB位图
        }
        for(int i=0;i<colBmp.length;i++){  // 遍历列标尺数组
            colBmp[i]=Bitmap.createBitmap(100,20, Bitmap.Config.ARGB_8888);  // 创建100x20的ARGB位图
        }
    }

    /**
     * 初始化事件监听：订阅通道/时基/触发/颜色等事件
     */
    private void initControl(){  // 初始化事件控制
//        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_CHANNEL_SELECT).subscribe(consumerCenterChannelsSelect);
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_MATH).subscribe(consumerRightMath);  // 订阅数学运算消息
        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_TIMEBASE).subscribe(consumerMainBottomTimeBase);  // 订阅底部时基消息
        RxBus.getInstance().getObservable(RxEnum. MAINWAVE_TRIGGERTIMEBASE).subscribe(consumerTriggerTimeBase);  // 订阅触发时基消息
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor);  // 订阅通道颜色选择消息
        EventFactory.addEventObserver(EventFactory.EVENT_UI_SAMPLE_GRAPH, eventUIObserver);  // 监听采样率/存储深度变更
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_POS, eventUIObserver);  // 监听通道垂直位置变更
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_VPOS, eventUIObserver);  // 监听数学通道垂直位置变更
        EventFactory.addEventObserver(EventFactory.EVENT_REF_VPOS, eventUIObserver);  // 监听参考通道垂直位置变更
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE,eventUIObserver);  // 监听通道垂直档位变更
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE_USER,eventUIObserver);  // 监听用户自定义垂直档位变更
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_FFT_SCALE, eventUIObserver);  // 监听FFT水平刻度变更
        EventFactory.addEventObserver(EventFactory.EVENT_PROBE_EVENT, eventUIObserver);  // 监听探头事件
//        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_ACTIVE,eventUIObserver);
        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,this::OnChanActiveChange);  // 订阅通道激活状态变更
    }




    /**
     * 绘制行标尺位图（垂直刻度）
     * 使用synchronized保证线程安全，先清除再绘制文字（描边+填充双层）
     */
    private synchronized void drawRowBmp(){  // 绘制行标尺位图
        synchronized (this) {  // 同步锁
            String[] s = getRowContent();  // 获取行标尺刻度文字数组
            for (int i = 0; i < rowBmp.length; i++) {//竖向  // 遍历每行标尺
                paint.setXfermode(modeClear);  // 设置清除模式
                mCanvas.setBitmap(rowBmp[i]);  // 绑定当前行位图
                mCanvas.drawPaint(paint);  // 清除位图全部内容
                paint.setXfermode(modeSrc);  // 恢复SRC覆盖模式

                Rect rect = Tools.getTextRect(s[i], paint);  // 测量文字边界矩形
                mCanvas.drawText(s[i], rowBmp[i].getWidth() - rect.width(), 16, textPaint);  // 绘制描边文字（右对齐）
                mCanvas.drawText(s[i], rowBmp[i].getWidth() - rect.width(), 16, paint);  // 绘制填充文字（右对齐）
            }
            isDraw = true;  // 标记已绘制，等待draw()时刷新纹理
        }
        onRefresh();  // 通知OpenGL刷新纹理
    }

    /**
     * 计算行标尺刻度文字内容（垂直电压刻度）
     * 根据当前激活通道类型（动态/数学/参考）获取垂直档位和零点偏移，
     * 计算每格对应的电压值，格式化为字符串
     *
     * @return 长度为11的字符串数组，每行一个刻度值
     */
    private String[] getRowContent(){  // 获取行标尺刻度文字内容

        String[] strings = new String[rowBmp.length];  // 创建结果数组
        Arrays.fill(strings, "");  // 默认填充空字符串
        int curCh = ChannelFactory.getChActivate();  // 获取当前激活通道号
        if (ChannelFactory.isChOpen(curCh) == false) {  // 若通道未开启
            return strings;  // 返回空字符串数组
        }
        double interval, zeroVal;  // 每格间隔值、零点偏移值
        String unit = "";  // 单位字符串
        if (ChannelFactory.isDynamicCh(curCh)) {  // 动态通道（CH1-CH4）
            Channel channel = ChannelFactory.getDynamicChannel(curCh);  // 获取动态通道实例
            if (channel == null) return strings;  // 通道为空则返回
            interval = channel.getVScaleVal();  // 获取垂直档位值（每格电压）
            unit = ChannelFactory.getProbeType(curCh);  // 获取探头类型（单位）
            zeroVal = channel.getVScaleVal() * channel.getPosUI() / (ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff());  // 计算零点偏移值
        } else if (ChannelFactory.isMathCh(curCh)) {  // 数学通道
            MathChannel math = ChannelFactory.getMathChannel(curCh);  // 获取数学通道实例
            if (math == null) return strings;  // 通道为空则返回
            interval = math.getVScaleVal();  // 获取数学通道垂直档位值
            unit = ChannelFactory.getProbeType(curCh);  // 获取探头类型（单位）
            zeroVal = math.getVScaleIdVal() * math.getPosUI() / (ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff());  // 计算零点偏移值（使用档位ID值）
        } else if (ChannelFactory.isRefCh(curCh)) {  // 参考通道
            RefChannel ref = ChannelFactory.getRefChannel(curCh);  // 获取参考通道实例
            if (ref == null) return strings;  // 通道为空则返回
            interval = ref.getVScaleVal();  // 获取参考通道垂直档位值
            unit = ChannelFactory.getProbeType(curCh);  // 获取探头类型（单位）
            zeroVal = ref.getVScaleVal() * ref.getPosUI() / (ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff());  // 计算零点偏移值
        } else {  // 其他类型通道
            return strings;  // 返回空字符串数组
        }

        double v = interval / (ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff());  // 计算每像素对应的电压值（用于判断接近零值）
        for (int i = 0; i < strings.length; i++) {  // 遍历每行
            double val = zeroVal * (-1) + interval * (5 - i);  // 计算当前行的电压值（零点偏移取反 + 每格递增）
            if (Math.abs(val) < v) {  // 若值接近零（小于1像素对应值）
                val = 0;  // 强制归零，避免浮点误差
            }
            strings[i] = TBookUtil.getFourFromD_Trim0(val) + unit;  // 格式化为4位有效数字并追加单位
        }
        paint.setColor(TChan.getChannelColor(context,TChan.toUiChNo(curCh)));  // 设置画笔颜色为当前通道颜色
        return strings;  // 返回刻度文字数组
    }

    /**
     * 绘制列标尺位图（水平刻度）
     * 使用synchronized保证线程安全，先清除再绘制文字（描边+填充双层）
     * 第0列左对齐，其余列居中对齐
     */
    private synchronized void drawColBmp(){  // 绘制列标尺位图
        synchronized (this) {  // 同步锁
            String[] s = getColContent();  // 获取列标尺刻度文字数组
            int color = App.get().getApplicationContext().getResources().getColor(R.color.main_text_color);  // 获取主文字颜色
            paint.setColor(color);  // 设置画笔颜色
            for (int i = 0; i < colBmp.length; i++) {//横向  // 遍历每列标尺
                paint.setXfermode(modeClear);  // 设置清除模式
                mCanvas.setBitmap(colBmp[i]);  // 绑定当前列位图
                mCanvas.drawPaint(paint);  // 清除位图全部内容
                paint.setXfermode(modeSrc);  // 恢复SRC覆盖模式

                Rect rect = Tools.getTextRect(s[i], paint);  // 测量文字边界矩形
                if (i == 0) {  // 第0列（最左侧）
                    mCanvas.drawText(s[i], 0, 16, textPaint);  // 左对齐绘制描边文字
                    mCanvas.drawText(s[i], 0, 16, paint);  // 左对齐绘制填充文字
                } else {  // 其余列
                    mCanvas.drawText(s[i], (colBmp[i].getWidth() - rect.width()) / 2, 16, textPaint);  // 居中绘制描边文字
                    mCanvas.drawText(s[i], (colBmp[i].getWidth() - rect.width()) / 2, 16, paint);  // 居中绘制填充文字
                }
            }
            isDraw = true;  // 标记已绘制，等待draw()时刷新纹理
        }
        onRefresh();  // 通知OpenGL刷新纹理
    }

    /**
     * 计算列标尺刻度文字内容（水平时间/频率刻度）
     * 根据当前通道类型区分：普通通道显示时间、FFT通道显示频率、Ref通道分FFT/普通两种
     *
     * @return 长度为12的字符串数组，每列一个刻度值
     */
    private String[] getColContent(){  // 获取列标尺刻度文字内容
        String[] strings=new String[colBmp.length];  // 创建结果数组
        Arrays.fill(strings,"");  // 默认填充空字符串

        Scope scope = Scope.getInstance();  // 获取示波器实例
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();  // 获取水平轴实例
        double interval = horizontalAxis.getTimeScaleIdVal() * HorizontalAxis.TIME_FS_100;  // 计算每格时间间隔（100fs单位）
        double centerPos = interval * horizontalAxis.getTimePoseOfViewPix() / ScopeBase.getHorizonPerGridPixels();  // 计算中心位置对应的时间值
//        if (scope.isInScrollMode()) {
//            centerPos =  interval * ScopeBase.getHorizonGridCnt() / 2;
//        }
//        Logger.d("RulerManage", "centerPos channel= " + centerPos + " ,timePos= " + horizontalAxis.getTimePoseOfViewPix());

        int chActivate=ChannelFactory.getChActivate();  // 获取当前激活通道号
        if (ChannelFactory.isMath_FFT_Ch(chActivate)) {  // FFT数学通道
            MathChannel mathChannel = ChannelFactory.getMathChannel(chActivate);  // 获取数学通道实例
            interval= mathChannel.getHorizontalAxisMathFFT().fftXScaleIdVal();  // 获取FFT水平刻度值（Hz/格）
            long pos= mathChannel.getHorizontalAxisMathFFT().getXPosOfView();  // 获取FFT水平位置
            centerPos = interval * pos / ScopeBase.getHorizonPerGridPixels();  // 计算FFT中心频率

            for (int i = 0; i < strings.length; i++) {  // 遍历每列
                long s = DoubleUtil.floor(centerPos + interval * (i - 6));  // 计算当前列的频率值（以中心为基准偏移）
                if (s<0) {  // 频率值为负（无效）
                    strings[i] = "";  // 显示空字符串
                }else {  // 频率值为正
                    strings[i] = TBookUtil.getHz3FromHz(s);  // 格式化为Hz字符串（3位有效数字）
                }
            }
        } else if (ChannelFactory.isRefCh(chActivate)) {  // 参考通道
            if (ChannelFactory.getRefChannel(chActivate).getRefType() == 2) {  // FFT类型的参考通道
                //getreftype()= 2 = fft
                int id = ChannelFactory.getRefChannel(chActivate).getRefTimeScaleId();  // 获取参考FFT时基档位ID
                //档位
                interval = ChannelFactory.getRefChannel(chActivate).getHorizontalAxisRef().fftTimeScaleIdVal(id);  // 获取FFT时间刻度值
                //位置
                long pos = Math.abs(ChannelFactory.getRefChannel(chActivate).getRefMovPix());  // 获取参考通道移动像素（取绝对值）
                //中心点位置
                centerPos = interval * pos / ScopeBase.getHorizonPerGridPixels();  // 计算FFT中心位置

                for (int i = 0; i < strings.length; i++) {  // 遍历每列
                    long s = DoubleUtil.floor(centerPos + interval * (i - 6));  // 计算当前列的频率值
                    if (s < 0) {  // 频率值为负（无效）
                        strings[i] = "";  // 显示空字符串
                    } else {  // 频率值为正
                        strings[i] = TBookUtil.getHz3FromHz(s);  // 格式化为Hz字符串
                    }
                }
            } else {  // 普通参考通道（非FFT）
                RefChannel refChannel = (RefChannel) ChannelFactory.getInstance().getTopRefChannel();  // 获取顶部参考通道
                interval = TBookUtil.getDoubleFromM(handleTimeBase(MainHolderBottom.getCenterTimeBase()).replace("s", "")) * HorizontalAxis.TIME_FS_100;  // 从时基字符串解析每格时间间隔
                long timePos = refChannel.getTimePoseOfViewPix();  // 获取参考通道时间位置像素
                centerPos = interval * timePos / ScopeBase.getHorizonPerGridPixels();  // 计算中心位置
//                if (scope.isInScrollMode()) {
//                    centerPos = interval * ScopeBase.getHorizonGridCnt() / 2;
//                }
                for (int i = 0; i < strings.length; i++) {  // 遍历每列
                    long s = DoubleUtil.floor(centerPos + interval * (i - 6));  // 计算当前列的时间值
                    strings[i] = TBookUtil.getSFrom100Fs(s);  // 格式化为时间字符串（从100fs单位转换）
                }
            }
        }
        else {  // 普通通道（动态通道CH1-CH4）
            for (int i = 0; i < strings.length; i++) {  // 遍历每列
                long s = DoubleUtil.floor(centerPos + interval * (i - 6));  // 计算当前列的时间值
                strings[i] = TBookUtil.getSFrom100Fs(s);  // 格式化为时间字符串
            }
        }
        return strings;  // 返回刻度文字数组
    }


    /**
     * 在OpenGL画布上绘制标尺
     * 先刷新已变更的位图纹理，再将行标尺和列标尺绘制到画布对应位置
     *
     * @param canvas OpenGL画布对象
     */
    public synchronized void draw(ICanvasGL canvas){  // OpenGL绘制入口
        synchronized (this) {  // 同步锁
            if (this.bShow==false) return;  // 若标尺不显示则直接返回
            //Log.d("RulerManage", "1 draw() called with: canvas = [" + canvas + "]");
            canvasGL = canvas;  // 保存画布引用
            if (isDraw) {  // 若位图内容已更新
                for (int i = 0; i < rowBmp.length; i++)  // 遍历行标尺
                    canvas.invalidateTextureContent(rowBmp[i],null);  // 刷新行标尺纹理
                for (int i = 0; i < colBmp.length; i++) {  // 遍历列标尺
                    canvas.invalidateTextureContent(colBmp[i],null);  // 刷新列标尺纹理
                }
                isDraw = false;  // 重置绘制标记
            }
            for (int i = 0; i < rowBmp.length; i++) {  // 遍历行标尺，绘制到位图指定位置
                if (i == 0) {  // 第0行（最顶部）
                    canvas.drawBitmap(rowBmp[i], GlobalVar.get().getMainWave().x - rowBmp[i].getWidth() - 1, 0);  // 右侧顶部，Y=0
                } else if (i == rowBmp.length - 1) {  // 最后一行（最底部）
                    canvas.drawBitmap(rowBmp[i], GlobalVar.get().getMainWave().x - rowBmp[i].getWidth() - 1, perVerGridPx * i - rowBmp[i].getHeight());  // 右侧底部，底部对齐
                } else {  // 中间行
                    canvas.drawBitmap(rowBmp[i], GlobalVar.get().getMainWave().x - rowBmp[i].getWidth() - 1, perVerGridPx * i - rowBmp[i].getHeight() / 2);  // 右侧中间，垂直居中于网格线
                }
            }
            for (int i = 0; i < colBmp.length; i++) {  // 遍历列标尺，绘制到位图指定位置
                if (i == 0) {  // 第0列（最左侧）
                    canvas.drawBitmap(colBmp[i], 0, perVerGridPx * 10 - colBmp[i].getHeight());  // 底部左对齐
                } else {  // 其余列
                    canvas.drawBitmap(colBmp[i], perHorGridPx * i - colBmp[i].getWidth() / 2, perVerGridPx * 10 - colBmp[i].getHeight());  // 底部水平居中于网格线
                }
            }
            //Log.d("RulerManage", "2 draw() called with: canvas = [" + canvas + "]");
        }

    }


    /**
     * 通道选择事件消费者
     * 当中心区域通道选择变更时，重新绘制行标尺和列标尺
     */
    private Consumer<MainCenterMsgChannels> consumerCenterChannelsSelect = new Consumer<MainCenterMsgChannels>() {  // 通道选择消费者
        @Override
        public void accept(MainCenterMsgChannels msgChannels) throws Exception {  // 接收通道选择消息
            drawRowBmp();  // 重新绘制行标尺（垂直刻度可能因通道切换而变化）
            drawColBmp();  // 重新绘制列标尺（水平刻度可能因FFT切换而变化）
//            int curCh=msgChannels.getChNO();
//            if (curCh<IWave.Ch1 || curCh> IWave.R4) return;
//            if (curCh>=IWave.Ch1 && curCh<=IWave.Ch4){
//                drawRowBmp();
//                drawColBmp();
//            }else if(curCh== IWave.Math){
//                drawRowBmp();
//            }else {
//                drawRowBmp();
//            }
        }
    };

    /**
     * 数学运算事件消费者
     * 当右侧菜单数学运算设置变更时，重新绘制行标尺
     */
    private Consumer<RightMsgMath> consumerRightMath = new Consumer<RightMsgMath>() {  // 数学运算消费者
        @Override
        public void accept(RightMsgMath rightMsgMath) throws Exception {  // 接收数学运算消息
            drawRowBmp();  // 重新绘制行标尺（数学通道垂直刻度可能变化）
        }
    };

    /**
     * 时基变更事件消费者
     * 当底部时基旋钮调整时，重新绘制列标尺
     */
    private Consumer<MainBottomMsgTimeBase> consumerMainBottomTimeBase = new Consumer<MainBottomMsgTimeBase>() {  // 时基变更消费者
        @Override
        public void accept(MainBottomMsgTimeBase msgTimeBase) throws Exception {  // 接收时基变更消息
            drawColBmp();  // 重新绘制列标尺（水平时间刻度变化）
        }
    };

    /**
     * 触发时基变更事件消费者
     * 当触发时基变更时，重新绘制列标尺
     */
    private Consumer<MainWaveMsgTriggerTimeBase> consumerTriggerTimeBase=new Consumer<MainWaveMsgTriggerTimeBase>() {  // 触发时基消费者
        @Override
        public void accept(MainWaveMsgTriggerTimeBase mainWaveMsgTriggerTimeBase) throws Exception {  // 接收触发时基消息
            drawColBmp();  // 重新绘制列标尺（触发时基变化影响水平位置）
        }
    };

    /**
     * 通道激活状态变更处理
     * 当通道激活/去激活时，重新绘制行标尺和列标尺
     *
     * @param obj 事件对象，包含MQEnum枚举
     */
    private void OnChanActiveChange(Object obj) {  // 通道激活状态变更回调
        MQEnum mqEnum= RxBusRegister.parseMqEnum(obj);  // 解析消息队列枚举
        if (mqEnum==MQEnum.CH_ACTIVE){  // 若为通道激活事件
            drawRowBmp();  // 重新绘制行标尺
            drawColBmp();  // 重新绘制列标尺
        }
    }

    /**
     * UI事件观察者
     * 监听采样率、通道位置、垂直档位、FFT刻度、探头等事件，
     * 分别触发行标尺或列标尺的重新绘制
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() {  // UI事件观察者
        @Override
        public void update(Object data) {  // 事件更新回调
            EventBase eventBase = (EventBase) data;  // 转换为事件基类
            if (eventBase.getId() == EventFactory.EVENT_UI_SAMPLE_GRAPH  // 采样率/存储深度变更
                    || eventBase.getId()==EventFactory.EVENT_MATH_FFT_SCALE  // FFT刻度变更

//                    || eventBase.getId() == EventFactory.EVENT_TIME_SCALE ||
//                    eventBase.getId() == EventFactory.EVENT_UI_DEPTH_SAMPFRE_REFLASH
            ) {
                drawColBmp();  // 重新绘制列标尺（水平刻度受影响）
            }else if (eventBase.getId()==EventFactory.EVENT_CHANNEL_POS ||  // 通道垂直位置变更
                    eventBase.getId()==EventFactory.EVENT_MATH_VPOS ||  // 数学通道垂直位置变更
                    eventBase.getId()==EventFactory.EVENT_REF_VPOS ||  // 参考通道垂直位置变更
                    eventBase.getId()==EventFactory.EVENT_CHANNEL_VSCALE ||  // 通道垂直档位变更
                    eventBase.getId()==EventFactory.EVENT_CHANNEL_VSCALE_USER  // 用户自定义垂直档位变更
                    || eventBase.getId()==EventFactory.EVENT_PROBE_EVENT  // 探头事件（衰减比变更）
            ){
                drawRowBmp();  // 重新绘制行标尺（垂直刻度受影响）
            }
//            else if (eventBase.getId()==EventFactory.EVENT_CHANNEL_ACTIVE){
//                drawRowBmp();
//            }
        }
    };


    /**
     * 工作模式切换回调（IWorkMode接口实现）
     * 切换YT/XY/YT缩放模式时，重新计算每格像素数
     *
     * @param workMode 工作模式常量（YT/XY/YT_ZOOM）
     */
    @Override  // 覆写IWorkMode接口方法
    public void switchWorkMode(int workMode) {  // 切换工作模式
        perHorGridPx = GlobalVar.get().getWaveZoneWidth_Pix(workMode) / 12;  // 重新计算水平每格像素数
        perVerGridPx = GlobalVar.get().getWaveZoneHeight_Pix(workMode) / 10;  // 重新计算垂直每格像素数
    }

    /**
     * 处理时基字符串，提取数值部分
     * 如果时基含换行符，取第二行；空字符串默认为1
     *
     * @param timeBase 原始时基字符串（如"10\nμs"或"1ms"）
     * @return 处理后的时基数值字符串
     */
    private static String handleTimeBase(String timeBase) {  // 处理时基字符串
        timeBase = timeBase.contains("\n") ? timeBase.split("\n")[1] : timeBase;  // 若含换行符则取第二行
        if (timeBase.isEmpty()) {  // 若字符串为空
            timeBase = "1";  // 默认值为1
        } else {  // 字符串非空
            timeBase = (TBookUtil.getSFromTime(timeBase.replace(" ", "")) + "").replaceAll(" ", "");  // 转换时间单位并去除空格
        }
        return timeBase;  // 返回处理后的时基字符串
    }

    /**
     * 通道颜色选择事件消费者
     * 当通道颜色变更时，重新绘制行标尺（行标尺颜色跟随通道颜色）
     */
    private Consumer<String> consumerSelectColor = new Consumer<String>() {  // 通道颜色选择消费者
        @Override
        public void accept(String colorInfo) throws Exception {  // 接收颜色选择消息
//            if (colorInfo.isEmpty()) return;
            Logger.i(TAG, "selectColorInfo= " + colorInfo);  // 记录颜色信息日志
//            String[] info = colorInfo.split(";");
//            int chIndex = Integer.parseInt(info[0]);
//            String colorStr = info[1];
            drawRowBmp();  // 重新绘制行标尺（颜色变更需更新标尺颜色）
        }
    };

}
