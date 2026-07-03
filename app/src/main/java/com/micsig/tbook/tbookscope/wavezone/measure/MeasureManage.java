package com.micsig.tbook.tbookscope.wavezone.measure;

import android.content.Context;                                       // 上下文类，用于获取应用环境信息
import android.graphics.Bitmap;                                       // 位图类，用于离屏绘制
import android.graphics.Canvas;                                       // 画布类，用于在位图上绘制
import android.graphics.Color;                                        // 颜色类，提供颜色常量
import android.graphics.DashPathEffect;                               // 虚线效果类
import android.graphics.LinearGradient;                               // 线性渐变类
import android.graphics.Paint;                                        // 画笔类，控制绘制样式
import android.graphics.Point;                                        // 点类，存储x/y坐标
import android.graphics.PorterDuff;                                   // PorterDuff混合模式
import android.graphics.PorterDuffXfermode;                           // PorterDuff混合模式转换器
import android.graphics.Rect;                                         // 矩形类，用于测量文本边界和点击区域
import android.graphics.Shader;                                       // 着色器基类
import android.os.Build;                                              // 系统版本信息
import android.text.TextPaint;                                        // 文本画笔，支持文字描边效果
import android.text.TextUtils;                                        // 文本工具类
import android.util.Log;                                              // 日志类
import android.view.MotionEvent;                                      // 触摸事件类

import com.chillingvan.canvasgl.ICanvasGL;                            // OpenGL画布接口
import com.micsig.tbook.scope.ScopeBase;                              // 示波器基类，提供坐标转换系数
import com.micsig.tbook.scope.channel.BaseChannel;                    // 通道基类
import com.micsig.tbook.scope.channel.ChannelFactory;                 // 通道工厂，获取通道实例
import com.micsig.tbook.scope.measure.Measure;                        // 测量类型定义
import com.micsig.tbook.scope.measure.MeasureService;                  // 测量服务，提供硬件测量数据
import com.micsig.tbook.tbookscope.GlobalVar;                         // 全局变量管理类
import com.micsig.tbook.tbookscope.R;                                 // 资源ID类
import com.micsig.tbook.tbookscope.middleware.Tag;                    // 中间件标签
import com.micsig.tbook.tbookscope.middleware.command.Command;        // 中间件命令，与ARM通信
import com.micsig.tbook.tbookscope.rxjava.RxBus;                     // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;                    // RxJava事件枚举
import com.micsig.tbook.tbookscope.top.layout.measure.TopLayoutMeasureCommon; // 顶部测量布局公共常量
import com.micsig.tbook.tbookscope.util.App;                          // 应用上下文工具类
import com.micsig.tbook.tbookscope.util.CacheUtil;                    // 缓存工具类，读写配置
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;                // 工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;           // 工作模式管理器
import com.micsig.tbook.ui.util.StrUtil;                              // 字符串工具类
import com.micsig.tbook.ui.util.TBookUtil;                            // 设备工具类
import com.micsig.tbook.ui.wavezone.TChan;                            // 通道工具类

import java.util.ArrayList;                                           // 动态数组列表
import java.util.List;                                                // 列表接口
import java.util.ListIterator;                                        // 列表迭代器
import java.util.Objects;                                             // 对象工具类
import java.util.function.Consumer;                                   // 消费者函数接口

/**
 * Created by liwb on 2017/5/8.
 * 测量管理
 *
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                            MeasureManage                                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：示波器测量子系统的顶层管理类，统一协调所有测量模块的创建和调度        ║
 * ║ 核心职责：                                                                  ║
 * ║   1. 以单例模式管理所有测量子模块（MeasureItem/AllMeasure/Cursor等）          ║
 * ║   2. 协调各子模块的绘制（draw）和工作模式切换（switchWorkMode）              ║
 * ║   3. 提供光标测量、频率计、FFT测量、段测量的统一对外接口                      ║
 * ║   4. 管理测量项目的增删改查及用户交互（点击选择）                            ║
 * ║ 架构设计：                                                                  ║
 * ║   - 单例模式 + 组合模式：持有各子模块实例，统一调度                          ║
 * ║   - IMeasure接口定义绘制规范，IWorkMode定义模式切换规范                     ║
 * ║   - 内部类CursorMeasure/AllMeasure/FrequencyMeterMeasure等实现具体绘制      ║
 * ║ 数据流向：                                                                  ║
 * ║   硬件采集 → MeasureService → 各子模块setData → draw()更新位图 → 渲染显示    ║
 * ║   用户操作 → MeasureItem点击 → MeasureItemListener → 更新ARM阈值配置        ║
 * ║ 依赖关系：                                                                  ║
 * ║   - GlobalVar/CacheUtil：全局配置和缓存                                     ║
 * ║   - TChan/ChannelFactory：通道信息和通道实例                                ║
 * ║   - MeasureService：硬件测量数据源                                          ║
 * ║   - RxBus：事件总线，通知测量行数变化                                       ║
 * ║ 使用场景：                                                                  ║
 * ║   波形区域的测量功能入口，所有测量相关操作都通过此类或其子模块完成              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

public class MeasureManage implements IWorkMode {

//region  单例

    /**
     * 静态内部类持有单例实例，实现懒加载线程安全
     */
    private static class MeasureManageHolder {
        private static final MeasureManage instance = new MeasureManage(); // 静态内部类持有唯一实例
    }

    /**
     * 获取MeasureManage单例实例
     * @return MeasureManage实例
     */
    public static final MeasureManage getInstance() {
        return MeasureManageHolder.instance;                            // 返回静态内部类持有的单例
    }

    //endregion

    //region 属性
    private MeasureItem measureItem;                                    // 测量项列表管理器

    /**
     * 获取测量项列表管理器
     * @return MeasureItem实例
     */
    public MeasureItem getMeasureItem() {
        return measureItem;                                             // 返回测量项管理器
    }

    private FrequencyMeterMeasure frequencyMeterMeasure;                // 频率计测量管理器

    /**
     * 获取频率计测量管理器
     * @return FrequencyMeterMeasure实例
     */
    public FrequencyMeterMeasure getFrequencyMeterMeasure() {
        return frequencyMeterMeasure;                                   // 返回频率计测量管理器
    }

    private AllMeasure allMeasure;                                      // 全部测量管理器

    /**
     * 获取全部测量管理器
     * @return AllMeasure实例
     */
    public AllMeasure getAllMeasure() {
        return allMeasure;                                              // 返回全部测量管理器
    }

    private CursorMeasureManage cursorMeasureManage;                    // 光标测量管理器（按模式分发）
    private CursorMeasure cursorMeasure;                                // 光标测量（旧版，直接绘制）

    /**
     * 获取旧版光标测量实例
     * @return CursorMeasure实例
     */
    public CursorMeasure getCursorMeasure() {
        return cursorMeasure;                                           // 返回旧版光标测量实例
    }


    private List<FFTMeasure> fftMeasureList = new ArrayList<>();        // FFT测量列表，每个数学通道一个

    /**
     * 获取指定数学通道号的FFT测量实例
     * @param mathNumber 数学通道号（从1开始）
     * @return FFTMeasure实例
     */
    public FFTMeasure getFftMeasure(int mathNumber) {
        return fftMeasureList.get(mathNumber - 1);                      // 数学通道号转为列表索引
    }

//    private FFTMeasure fftMeasure;
//
//    public FFTMeasure getFftMeasure() {
//        return fftMeasure;
//    }

    private SegmentMeasure segmentMeasure;                              // 段测量管理器

    /**
     * 获取段测量管理器
     * @return SegmentMeasure实例
     */
    public SegmentMeasure getSegmentMeasure() {
        return segmentMeasure;                                          // 返回段测量管理器
    }

    private MeasureIndication measureIndication;                        // 测量指示线管理器

    /**
     * 获取测量指示线管理器
     * @return MeasureIndication实例
     */
    public MeasureIndication getMeasureIndication(){
        return measureIndication;                                       // 返回测量指示线管理器
    }
    private Context context=App.get().getApplicationContext();         // 应用上下文
    //endregion

    /**
     * 构造函数：初始化所有测量子模块，并注册测量项点击监听器
     */
    public MeasureManage() {
        measureItem = new MeasureItem();                                // 创建测量项管理器
        frequencyMeterMeasure = new FrequencyMeterMeasure();            // 创建频率计测量
        allMeasure = new AllMeasure();                                  // 创建全部测量
        cursorMeasure = new CursorMeasure();                            // 创建旧版光标测量
        cursorMeasureManage = new CursorMeasureManage();                // 创建新版光标测量管理器
        TChan.foreachMath(mathChan -> {                                 // 遍历所有数学通道
            fftMeasureList.add(TChan.toMathNumber(mathChan) - 1, new FFTMeasure(TChan.toMathNumber(mathChan))); // 为每个数学通道创建FFT测量
        });
        segmentMeasure = new SegmentMeasure();                          // 创建段测量
        measureIndication = new MeasureIndication();                    // 创建测量指示线


        measureItem.setMeasureItemListene(new MeasureItemListener() {   // 设置测量项点击监听器
            /**
             * 测量项被点击时的回调：更新ARM阈值参数并设置指示线
             * @param chIdx 通道索引（从1开始）
             * @param measure 测量项ID
             */
            @Override
            public void onClick(int chIdx, int measure) {
                updateARM(chIdx);                                       // 更新ARM端阈值参数
                measureIndication.setMeasureIndication(chIdx-1,measure + Measure.MeasureType.MEASURE_FIRST); // 设置指示线位置
            }

            /**
             * 根据通道索引更新ARM端的阈值参数（高/中/低）
             * @param chIdx 通道索引（从1开始）
             */
            private void updateARM(int chIdx){
                int channelIdx=chIdx-1;                                 // 转换为0-based通道索引
                int thresholdsIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS); // 获取阈值类型索引
                String high=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH+getSaveThresholdsParam(channelIdx,thresholdsIdx)); // 获取高阈值
                String middle=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam(channelIdx,thresholdsIdx)); // 获取中阈值
                String low=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW+getSaveThresholdsParam(channelIdx,thresholdsIdx)); // 获取低阈值
                BaseChannel channel = (BaseChannel) ChannelFactory.getValidChannel(channelIdx); // 获取通道实例
                if(channel != null){                                    // 通道有效
                    if(thresholdsIdx == 1){                             // 绝对值阈值模式
                        channel.setAbsLower(TBookUtil.getDoubleFromM(low)); // 设置绝对值下阈值
                        channel.setAbsUpper(TBookUtil.getDoubleFromM(high)); // 设置绝对值上阈值
                        channel.setAbsMiddle(TBookUtil.getDoubleFromM(middle)); // 设置绝对值中阈值
                    }else{                                              // 相对值阈值模式（百分比）
                        channel.setLower(Integer.parseInt(low));        // 设置下阈值
                        channel.setUpper(Integer.parseInt(high));       // 设置上阈值
                        channel.setMiddle(Integer.parseInt(middle));    // 设置中阈值
                    }
                    channel.setAbsEnable(thresholdsIdx == 1);           // 设置是否启用绝对值模式
                }
                MeasureService.forceMeasureRefresh();                   // 强制刷新硬件测量数据
            }

            /**
             * 生成阈值参数的缓存键后缀
             * @param chIdx 通道索引（0-based）
             * @param thresholdsIdx 阈值类型索引
             * @return 缓存键后缀字符串
             */
            private String getSaveThresholdsParam(int chIdx,int thresholdsIdx){
                return ""+thresholdsIdx+chIdx;                          // 拼接"阈值类型索引+通道索引"
            }
        });

        measureItem.setMeasureRowChangeListener(new MeasureRowChangeListener() { // 设置测量行数变化监听器
            /**
             * 测量行数变化时的回调：通过RxBus通知其他模块
             * @param rowCount 当前行数
             */
            @Override
            public void measureRowChange(int rowCount) {
                RxBus.getInstance().post(RxEnum.MQ_MSG_MEASURE_ROW_COUNT, rowCount); // 通过事件总线发送行数变化消息
            }
        });
    }

    /**
     * 初始化方法（当前为空实现）
     */
    public void init() {
    }

    /**
     * 根据通道ID获取硬件测量对象
     * @param chId 通道ID
     * @return Measure对象，通道无效时返回null
     */
    private Measure getHardwareMeasure(int chId) {
        BaseChannel baseChannel = null;                                 // 通道基类引用
        if (ChannelFactory.isDynamicCh(chId)) {                        // 如果是动态通道
            baseChannel = ChannelFactory.getDynamicChannel(chId);       // 获取动态通道实例
        } else if (ChannelFactory.isMathCh(chId)) {                    // 如果是数学通道
            baseChannel = ChannelFactory.getMathChannel(chId);          // 获取数学通道实例
        } else if (ChannelFactory.isRefCh(chId)) {                     // 如果是参考通道
            baseChannel = ChannelFactory.getRefChannel(chId);           // 获取参考通道实例
        }
        if (baseChannel != null) {                                     // 通道实例有效
            return baseChannel.getMeasure();                            // 返回通道关联的测量对象
        }
        return null;                                                    // 无效通道返回null
    }

    /**
     * 重置所有有效测量项的统计值
     */
    public void measureStaticReset() {
        for (int i = 0; i < MeasureManage.getInstance().getMeasureItem().getValidMeasureList().size(); i++) { // 遍历所有有效测量项
            MeasureManage.MeasureItemStruct item = MeasureManage.getInstance().getMeasureItem().getValidMeasureList().get(i); // 获取测量项
            int iWaveCh = item.getChannelId();                          // 获取通道ID
            int measureId = item.getMeasureId();                        // 获取测量ID
            String measureName = item.getMeasureName();                 // 获取测量名称
            Measure measure = getHardwareMeasure(iWaveCh - 1);          // 获取硬件测量对象（通道ID转0-based）
            measure.getMeasureStatics().reset();                        // 重置统计数据
        }
    }


    /**
     * 使用Canvas方式绘制所有测量模块
     * @param canvas 目标画布
     */
    public void draw(Canvas canvas) {
        measureItem.draw(canvas);                                       // 绘制测量项
        frequencyMeterMeasure.draw(canvas);                             // 绘制频率计
        allMeasure.draw(canvas);                                        // 绘制全部测量
//        cursorMeasureManage.draw();
//        cursorMeasure.draw(canvas);
    }

    /**
     * 使用ICanvasGL方式绘制所有测量模块
     * @param canvas GL画布
     */
    public void draw(ICanvasGL canvas) {
        measureIndication.draw(canvas);                                 // 绘制测量指示线
        measureItem.draw(canvas);                                       // 绘制测量项
        frequencyMeterMeasure.draw(canvas);                             // 绘制频率计
        allMeasure.draw(canvas);                                        // 绘制全部测量
//        cursorMeasure.draw(canvas);
        cursorMeasureManage.draw(canvas);                               // 绘制光标测量
//        fftMeasure.draw(canvas);
        fftMeasureList.forEach(fftMeasure -> fftMeasure.draw(canvas)); // 绘制所有FFT测量
//        segmentMeasure.draw(canvas);
    }

    //region IWorkMode接口
    private
    @WorkMode
    int mWorkMode = IWorkMode.WorkMode_YT;                             // 当前工作模式，默认YT

    /**
     * 切换工作模式，通知所有子模块更新
     * @param workMode 目标工作模式
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        //if (workMode == mWorkMode) return;
        this.mWorkMode = workMode;                                      // 更新当前工作模式
        measureItem.switchWorkMode(workMode);                           // 通知测量项切换模式
        measureIndication.switchWorkMode(workMode);                     // 通知指示线切换模式
        frequencyMeterMeasure.switchWorkMode(workMode);                 // 通知频率计切换模式
        allMeasure.switchWorkMode(workMode);                            // 通知全部测量切换模式
        cursorMeasureManage.switchWorkMode(workMode);                   // 通知光标测量切换模式
//        fftMeasure.switchWorkMode(workMode);
        fftMeasureList.forEach(fftMeasure -> fftMeasure.switchWorkMode(workMode)); // 通知所有FFT测量切换模式
//        segmentMeasure.switchWorkMode(workMode);
    }

    //endregion

    /**
     * 设置光标测量的通道颜色
     * @param ChNo 通道号
     */
    public void setCursorChannelColor(int ChNo) {
        cursorMeasureManage.setCursorChannelColor(ChNo);                // 委托给光标测量管理器
    }

    /**
     * 设置全部测量的通道颜色
     * @param ChNo 通道号
     */
    public void setAllMeasureChannelColor(int ChNo) {
        allMeasure.setChannelId(ChNo);                                  // 设置AllMeasure的通道ID
    }

    /**
     * 设置行光标可见性
     * @param rowVisible true表示可见
     */
    public void setRowVisible(boolean rowVisible) {
        cursorMeasureManage.setRowVisible(rowVisible);                  // 委托给光标测量管理器
    }


    /**
     * 设置列光标可见性
     * @param colVisible true表示可见
     */
    public void setColVisible(boolean colVisible) {
        cursorMeasureManage.setColVisible(colVisible);                  // 委托给光标测量管理器
    }

    /**
     * 按指定工作模式设置行光标可见性
     * @param workMode 工作模式
     * @param rowVisible true表示可见
     */
    public void setRowVisible(int workMode, boolean rowVisible) {
        cursorMeasureManage.setRowVisible(workMode, rowVisible);        // 委托给光标测量管理器
    }

    /**
     * 按指定工作模式设置列光标可见性
     * @param workMode 工作模式
     * @param colVisible true表示可见
     */
    public void setColVisible(int workMode, boolean colVisible) {
        cursorMeasureManage.setColVisible(workMode, colVisible);        // 委托给光标测量管理器
    }

    /****
     * 绘制接口
     *
     */
    public interface IMeasure {
        //region 常量
        /** 周期测量ID */
        public static final int MeasureId_Period = Measure.MeasureType.MEASURE_PERIOD - Measure.MeasureType.MEASURE_FIRST;
        /** 频率测量ID */
        public static final int MeasureId_Freq = Measure.MeasureType.MEASURE_FREQ - Measure.MeasureType.MEASURE_FIRST;
        /** 上升时间测量ID */
        public static final int MeasureId_RiseTime = Measure.MeasureType.MEASURE_RISETIME - Measure.MeasureType.MEASURE_FIRST;
        /** 下降时间测量ID */
        public static final int MeasureId_FallTime = Measure.MeasureType.MEASURE_FALLTIME - Measure.MeasureType.MEASURE_FIRST;
        /** 延迟测量ID */
        public static final int MeasureId_Delay = Measure.MeasureType.MEASURE_DELAY - Measure.MeasureType.MEASURE_FIRST;
        /** 正占空比测量ID */
        public static final int MeasureId_DutyAdd = Measure.MeasureType.MEASURE_POSITIVE_DUTY - Measure.MeasureType.MEASURE_FIRST;
        /** 负占空比测量ID */
        public static final int MeasureId_DutySub = Measure.MeasureType.MEASURE_NEGATIVE_DUTY - Measure.MeasureType.MEASURE_FIRST;
        /** 正脉宽测量ID */
        public static final int MeasureId_WidthAdd = Measure.MeasureType.MEASURE_POSITIVE_PULSE_WIDTH - Measure.MeasureType.MEASURE_FIRST;
        /** 负脉宽测量ID */
        public static final int MeasureId_WidthSub = Measure.MeasureType.MEASURE_NEGATIVE_PULSE_WIDTH - Measure.MeasureType.MEASURE_FIRST;
        /** 猝发脉宽测量ID */
        public static final int MeasureId_BurstW = Measure.MeasureType.MEASURE_BURST_WIDTH - Measure.MeasureType.MEASURE_FIRST;
        /** 正过冲测量ID */
        public static final int MeasureId_ROV = Measure.MeasureType.MEASURE_POSITIVE_OVERSHOOT - Measure.MeasureType.MEASURE_FIRST;
        /** 负过冲测量ID */
        public static final int MeasureId_FOV = Measure.MeasureType.MEASURE_NEGATIVE_OVERSHOOT - Measure.MeasureType.MEASURE_FIRST;
        /** 相位测量ID */
        public static final int MeasureId_Phase = Measure.MeasureType.MEASURE_PHASE - Measure.MeasureType.MEASURE_FIRST;
        /** 峰峰值测量ID */
        public static final int MeasureId_PKPK = Measure.MeasureType.MEASURE_PK_PK - Measure.MeasureType.MEASURE_FIRST;
        /** 幅度测量ID */
        public static final int MeasureId_Amp = Measure.MeasureType.MEASURE_AMPLITUDE - Measure.MeasureType.MEASURE_FIRST;
        /** 高电平测量ID */
        public static final int MeasureId_High = Measure.MeasureType.MEASURE_HIGH - Measure.MeasureType.MEASURE_FIRST;
        /** 低电平测量ID */
        public static final int MeasureId_Low = Measure.MeasureType.MEASURE_LOW - Measure.MeasureType.MEASURE_FIRST;
        /** 最大值测量ID */
        public static final int MeasureId_Max = Measure.MeasureType.MEASURE_MAX - Measure.MeasureType.MEASURE_FIRST;
        /** 最小值测量ID */
        public static final int MeasureId_Min = Measure.MeasureType.MEASURE_MIN - Measure.MeasureType.MEASURE_FIRST;
        /** 均方根值测量ID */
        public static final int MeasureId_RMS = Measure.MeasureType.MEASURE_RMS - Measure.MeasureType.MEASURE_FIRST;
        /** 电流均方根值测量ID */
        public static final int MeasureId_CRMS = Measure.MeasureType.MEASURE_CRMS - Measure.MeasureType.MEASURE_FIRST;
        /** 平均值测量ID */
        public static final int MeasureId_Mean = Measure.MeasureType.MEASURE_MEAN - Measure.MeasureType.MEASURE_FIRST;
        /** 电流平均值测量ID */
        public static final int MeasureId_CMean = Measure.MeasureType.MEASURE_CMEAN - Measure.MeasureType.MEASURE_FIRST;
        /** 交流均方根值测量ID */
        public static final int MeasureId_ACRMS = Measure.MeasureType.MEASURE_AC_RMS - Measure.MeasureType.MEASURE_FIRST;
        /** 正斜率测量ID */
        public static final int MeasureId_PostitiveRate = Measure.MeasureType.MEASURE_POSITIVE_RATE - Measure.MeasureType.MEASURE_FIRST;
        /** 负斜率测量ID */
        public static final int MeasureId_NegativeRate = Measure.MeasureType.MEASURE_NEGATIVE_RATE - Measure.MeasureType.MEASURE_FIRST;

        /** T值测量ID */
        public static final int MeasureId_TVALUE = Measure.MeasureType.MEASURE_TVALUE - Measure.MeasureType.MEASURE_FIRST;

        /** 列值Q测量ID */
        public static final int MeasureId_ColValQ = Measure.MeasureType.MEASURE_COLV - Measure.MeasureType.MEASURE_FIRST;

        /** 光标X1测量ID */
        public static final int MeasureId_Cursor_X1 = Measure.MeasureType.MEASURE_CURSOR_X1 - Measure.MeasureType.MEASURE_FIRST;

        /** 光标X2测量ID */
        public static final int MeasureId_Cursor_X2 = Measure.MeasureType.MEASURE_CURSOR_X2 - Measure.MeasureType.MEASURE_FIRST;



        //endregion

        /**
         * 使用Canvas方式绘制
         * @param canvas 目标画布
         */
        void draw(Canvas canvas);

        /**
         * 使用ICanvasGL方式绘制
         * @param canvas GL画布
         */
        void draw(ICanvasGL canvas);
    }

    /***
     * 光标参数结构
     */
    public class CursorMeasureStruct {
        public String row1;                                             // 行光标1的Y值
        public String row2;                                             // 行光标2的Y值
        public String deltaRow;                                         // 行光标差值ΔY

        public String col1;                                             // 列光标1的X值
        public String col2;                                             // 列光标2的X值
        public String deltaCol;                                         // 列光标差值ΔX
        public String deltaTCol;                                        // 列光标差值倒数1/ΔX

        public String S;                                                // 面积值S
    }

    /***
     * 光标测量类 移到CurosrMeasureBase中，分出YT和XY模式
     *
     * 旧版光标测量实现，直接在内部绘制位图，现已被CursorMeasureManage替代
     */
    public class CursorMeasure implements IMeasure, IWorkMode {

        //region  属性
        private boolean rowCursorVisible = true;                        // 行光标是否可见
        private boolean colCursorVisible = true;                        // 列光标是否可见
        private int channelId = 1;                                      // 关联通道ID

        /**
         * 获取通道ID
         * @return 通道ID
         */
        public int getChannelId() {
            return channelId;                                           // 返回通道ID
        }

        /**
         * 设置通道ID并重绘
         * @param channelId 通道ID
         */
        public void setChannelId(int channelId) {
            this.channelId = channelId;                                 // 更新通道ID
            draw();                                                     // 触发重绘
        }

        /**
         * 查询行光标是否可见
         * @return true表示可见
         */
        public boolean isRowCursorVisible() {
            return rowCursorVisible;                                    // 返回行光标可见性
        }

        /**
         * 设置行光标可见性并重绘
         * @param rowCursorVisible true表示可见
         */
        public void setRowCursorVisible(boolean rowCursorVisible) {
            this.rowCursorVisible = rowCursorVisible;                   // 更新行光标可见性
            draw();                                                     // 触发重绘
        }

        /**
         * 查询列光标是否可见
         * @return true表示可见
         */
        public boolean isColCursorVisible() {
            return colCursorVisible;                                    // 返回列光标可见性
        }

        /**
         * 设置列光标可见性并重绘
         * @param colCursorVisible true表示可见
         */
        public void setColCursorVisible(boolean colCursorVisible) {
            this.colCursorVisible = colCursorVisible;                   // 更新列光标可见性
            draw();                                                     // 触发重绘
        }

        //endregion
        private CursorMeasureStruct param = null;                       // 光标参数结构

        private int showX = 550, showY = 50;                            // 浮窗默认显示位置
        private Paint p;                                                // 画笔
        private Bitmap bmp;                                             // 离屏位图
        private Canvas mCanvas;                                         // 离屏画布
        private boolean isChanageBitmap = false;                        // 位图变更标记
        private ICanvasGL canvasGL;                                     // GL画布引用

        /**
         * 通知GL画布刷新纹理
         */
        public void onRefresh(){
            if(canvasGL != null){                                       // GL画布引用有效
                canvasGL.onRefreshTexture();                            // 刷新纹理
            }
        }

        /**
         * 构造函数：初始化光标测量的位图、画布和画笔
         */
        public CursorMeasure() {
            param = new CursorMeasureStruct();                          // 创建参数结构
            bmp = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888); // 创建150x150位图
            mCanvas = new Canvas(bmp);                                  // 绑定画布到位图
            p = new Paint();                                            // 创建画笔
            p.setTextSize(20);                                          // 设置文本大小
            p.setAntiAlias(true);                                       // 开启抗锯齿
            draw();                                                     // 执行初始绘制
        }

        //region IWorkMode 接口
        /**
         * 切换工作模式，更新浮窗显示位置
         * @param workMode 工作模式
         */
        @Override
        public void switchWorkMode(@WorkMode int workMode) {
            Point p = GlobalVar.get().getMeasureCursorPosition(workMode); // 获取浮窗位置
            showX = p.x;                                                // 更新X坐标
            showY = p.y;                                                // 更新Y坐标
        }


        //endregion

        /**
         * 使用Canvas方式绘制光标测量浮窗
         * @param canvas 目标画布
         */
        @Override
        public void draw(Canvas canvas) {
            if (colCursorVisible || rowCursorVisible) {                 // 行光标或列光标可见
                synchronized (bmp) {                                    // 同步锁定位图
                    canvas.drawBitmap(bmp, showX, showY, null);         // 绘制位图到目标画布
                }
            }
        }

        /**
         * 使用ICanvasGL方式绘制光标测量浮窗
         * @param canvas GL画布
         */
        @Override
        public void draw(ICanvasGL canvas) {
            if (colCursorVisible || rowCursorVisible) {                 // 行光标或列光标可见
                synchronized (bmp) {                                    // 同步锁定位图
                    canvasGL = canvas;                                  // 保存GL画布引用
                    if (isChanageBitmap) canvas.invalidateTextureContent(bmp,null); // 位图变更时刷新纹理
                    canvas.drawBitmap(bmp, showX, showY);               // 绘制位图到GL画布
                    isChanageBitmap = false;                            // 重置变更标记
                }
            }
        }

        //region 显示
        /**
         * 内部绘制方法：清除旧内容，按光标可见性绘制测量数据
         */
        private void draw() {
            synchronized (bmp) {                                        // 同步锁定位图
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); // 设置清除模式
                mCanvas.drawPaint(p);                                   // 清除位图内容
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC)); // 恢复源模式
                if (TChan.isRef(channelId)) {                           // 如果是Ref参考通道
                    p.setColor(TChan.getChannelColor(context,TChan.RefActive)); // 设置Ref颜色
                } else {
                    p.setColor(TChan.getChannelColor(context,channelId)); // 设置通道颜色
                }
                if (rowCursorVisible && colCursorVisible) {             // 行列光标都可见
                    int y = drawRow();                                  // 绘制行数据
                    y = drawCol(y);                                     // 绘制列数据
                    drawS(y);                                           // 绘制面积数据

                } else if (rowCursorVisible) {                          // 仅行光标可见
                    drawRow();                                          // 绘制行数据
                } else if (colCursorVisible) {                          // 仅列光标可见
                    drawCol(0);                                         // 绘制列数据，起始Y为0
                }
                isChanageBitmap = true;                                 // 标记位图已变更
                onRefresh();                                            // 通知GL刷新
            }
        }

        /**
         * 绘制行光标数据（Y1, Y2, ΔY）
         * @return 最后绘制行的Y坐标
         */
        private int drawRow() {
            int x = 0, y = 0;                                          // 起始坐标
            String text = "Y1:" + String.valueOf(param.row1);           // Y1文本
            x = 0;                                                      // X坐标为0
            y = getTextHeight(text) + 5;                                // Y坐标为文本高度+间距
            mCanvas.drawText(text, x, y, p);                            // 绘制Y1

            text = "Y2:" + String.valueOf(param.row2);                  // Y2文本
            x = 0;                                                      // X坐标为0
            y = y + getTextHeight(text) + 5;                            // Y坐标下移
            mCanvas.drawText(text, x, y, p);                            // 绘制Y2

            text = "ΔY:" + String.valueOf(param.deltaRow);              // ΔY文本
            x = 0;                                                      // X坐标为0
            y = y + getTextHeight(text) + 5;                            // Y坐标下移
            mCanvas.drawText(text, x, y, p);                            // 绘制ΔY

            return y;                                                   // 返回当前Y坐标
        }

        /**
         * 绘制列光标数据（X1, X2, ΔX, 1/ΔX）
         * @param y 起始Y坐标
         * @return 最后绘制行的Y坐标
         */
        private int drawCol(int y) {
            int x;                                                      // X坐标
            String text = "X1:" + String.valueOf(param.col1);           // X1文本
            x = 0;                                                      // X坐标为0
            y = y + getTextHeight(text) + 5;                            // Y坐标下移
            mCanvas.drawText(text, x, y, p);                            // 绘制X1

            text = "X2:" + String.valueOf(param.col2);                  // X2文本
            x = 0;                                                      // X坐标为0
            y = y + getTextHeight(text) + 5;                            // Y坐标下移
            mCanvas.drawText(text, x, y, p);                            // 绘制X2

            text = "ΔX:" + String.valueOf(param.deltaCol);              // ΔX文本
            x = 0;                                                      // X坐标为0
            y = y + getTextHeight(text) + 5;                            // Y坐标下移
            mCanvas.drawText(text, x, y, p);                            // 绘制ΔX

            text = "1/ΔX:" + String.valueOf(param.deltaTCol);           // 1/ΔX文本
            x = 0;                                                      // X坐标为0
            y = y + getTextHeight(text) + 5;                            // Y坐标下移
            mCanvas.drawText(text, x, y, p);                            // 绘制1/ΔX

            return y;                                                   // 返回当前Y坐标
        }

        /**
         * 绘制面积数据（S）
         * @param y 起始Y坐标
         */
        private void drawS(int y) {
            String text = "S:" + String.valueOf(param.S);               // S文本
            int x = 0;                                                  // X坐标为0
            y = y + getTextHeight(text) + 5;                            // Y坐标下移
            mCanvas.drawText(text, x, y, p);                            // 绘制S
        }

        /**
         * 获取文本像素高度
         * @param text 待测量的文本
         * @return 文本高度
         */
        private int getTextHeight(String text) {
            Rect rect = new Rect();                                     // 创建边界矩形
            p.getTextBounds(text, 0, text.length(), rect);              // 测量文本边界
            int w = rect.width();                                       // 文本宽度（未使用）
            int h = rect.height();                                      // 文本高度
            return h;                                                   // 返回文本高度
        }
        //endregion

        /**
         * 设置光标测量参数（委托给CursorMeasureManage）
         * @param row1     行光标1的Y值
         * @param row2     行光标2的Y值
         * @param deltaRow 行光标差值ΔY
         * @param col1     列光标1的X值
         * @param col2     列光标2的X值
         * @param deltaCol 列光标差值ΔX
         * @param deltaTCol 列光标差值倒数1/ΔX
         * @param S        面积值
         */
        public void setParam(String row1, String row2, String deltaRow, String col1, String col2, String deltaCol,
                             String deltaTCol, String S) {
            cursorMeasureManage.setParam(row1, row2, deltaRow, col1, col2, deltaCol, deltaTCol, S); // 委托给管理器
        }
    }

    /**
     * 测量指示线类：在被选中的测量项位置绘制十字虚线指示
     *
     * ╔══════════════════════════════════════════════════════════════════════════════╗
     * ║                          MeasureIndication                                 ║
     * ╠══════════════════════════════════════════════════════════════════════════════╣
     * ║ 在波形区域绘制左/上/右/下四条虚线指示线，标注选中测量项的阈值位置            ║
     * ╚══════════════════════════════════════════════════════════════════════════════╝
     */
    public class MeasureIndication implements IMeasure, IWorkMode{

        /** 指示线位置常量：左 */
        public static final int MEASURE_INDICATION_LEFT     = 0;
        /** 指示线位置常量：上 */
        public static final int MEASURE_INDICATION_TOP      = 1;
        /** 指示线位置常量：右 */
        public static final int MEASURE_INDICATION_RIGHT    = 2;
        /** 指示线位置常量：下 */
        public static final int MEASURE_INDICATION_BOTTOM   = 3;
        /** 指示线位置数量 */
        public static final int MEASURE_INDICATION_MAX = 4;
        private int [] pos = new int[MEASURE_INDICATION_MAX];           // 四条指示线的位置值
        private Bitmap [] bmp = new Bitmap[MEASURE_INDICATION_MAX];     // 四条指示线的位图

        private Bitmap [] oldBmp = new Bitmap[MEASURE_INDICATION_MAX];  // 旧位图（用于纹理对比刷新）
        private Canvas mCanvas;                                         // 画布
        private boolean [] visiable = new boolean[MEASURE_INDICATION_MAX]; // 四条指示线的可见性

        private boolean bEnable = false;                                // 指示线功能是否启用
        private boolean isChanageBitmap = false;                        // 位图变更标记

        private ICanvasGL canvasGL;                                     // GL画布引用

        /**
         * 通知GL画布刷新纹理
         */
        public void onRefresh(){
            if(canvasGL != null){                                       // GL画布引用有效
                canvasGL.onRefreshTexture();                            // 刷新纹理
            }
        }
        private int chIdx = -1;                                         // 选中通道索引（-1表示未选中）
        private int measureId = -1;                                     // 选中测量项ID（-1表示未选中）

        /**
         * 设置测量指示线的通道和测量项，并触发硬件测量刷新
         * @param chIdx 通道索引（0-based）
         * @param measureId 测量项ID
         */
        public synchronized void setMeasureIndication(int chIdx,int measureId){
            this.chIdx = chIdx;                                         // 更新通道索引
            this.measureId = measureId;                                 // 更新测量项ID
            MeasureService.forceMeasureRefresh();                       // 强制刷新硬件测量数据
        }

        /**
         * 获取选中的通道索引
         * @return 通道索引
         */
        public synchronized int getChIdx(){
            return chIdx;                                               // 返回通道索引
        }

        /**
         * 获取选中的测量项ID
         * @return 测量项ID
         */
        public synchronized int getMeasureId(){
            return measureId;                                           // 返回测量项ID
        }

        /**
         * 判断指示线是否有任何一条可见
         * @return true表示至少有一条可见
         */
        public boolean isVisiable(){
            return visiable[MEASURE_INDICATION_LEFT]                    // 左指示线可见
                    || visiable[MEASURE_INDICATION_TOP]                 // 或上指示线可见
                    || visiable[MEASURE_INDICATION_RIGHT]               // 或右指示线可见
                    || visiable[MEASURE_INDICATION_BOTTOM];             // 或下指示线可见
        }

        /**
         * 设置所有指示线的可见性
         * @param bVisiable true表示全部可见
         */
        public synchronized void setVisiable(boolean bVisiable){

            visiable[MEASURE_INDICATION_LEFT]                           // 设置左指示线可见性
                    = visiable[MEASURE_INDICATION_TOP]                  // 设置上指示线可见性
                    = visiable[MEASURE_INDICATION_RIGHT]                // 设置右指示线可见性
                    = visiable[MEASURE_INDICATION_BOTTOM] = bVisiable;  // 设置下指示线可见性
            onRefresh();                                                // 通知GL刷新
        }

        /**
         * 设置指定方向指示线的可见性
         * @param idx 方向索引（LEFT/TOP/RIGHT/BOTTOM）
         * @param bVisiable true表示可见
         */
        public synchronized void setVisiable(int idx,boolean bVisiable){

            visiable[idx] = bVisiable;                                  // 设置指定方向可见性
            onRefresh();                                                // 通知GL刷新
        }

        /**
         * 设置指示线位置（FPGA坐标，需乘以转换系数）
         * @param idx 方向索引
         * @param pos 位置值
         */
        public synchronized void setPos(int idx, int pos) {
            pos = (int) Math.round(pos * ScopeBase.getToFPGACoff());   // 将UI坐标转换为FPGA坐标
            this.pos[idx] = pos;                                        // 更新位置值
        }

        /**
         * 查询指示线功能是否启用
         * @return true表示已启用
         */
        public synchronized boolean isEnable() {
            return bEnable;                                             // 返回启用状态
        }

        /**
         * 设置指示线功能启用状态
         * @param bEnable true表示启用
         */
        public synchronized void setEnable(boolean bEnable) {
            this.bEnable = bEnable;                                     // 更新启用状态
            onRefresh();                                                // 通知GL刷新
        }

        /**
         * 构造函数：初始化指示线位图并刷新
         */
        public MeasureIndication(){
            initLine();                                                 // 初始化指示线位图
            isChanageBitmap = true;                                     // 标记位图已变更
            onRefresh();                                                // 通知GL刷新
        }

        /**
         * 初始化四条指示线的位图，根据当前工作模式的波形区域尺寸创建
         */
        private void initLine() {
            int m = WorkModeManage.getInstance().getmWorkMode();        // 获取当前工作模式
            int sw = GlobalVar.get().getWaveZoneWidth_Pix(m);           // 获取波形区域宽度
            int sh = GlobalVar.get().getWaveZoneHeight_Pix(m);          // 获取波形区域高度
            System.arraycopy(bmp,0,oldBmp,0, bmp.length);               // 备份旧位图引用
            bmp[MEASURE_INDICATION_LEFT] = Bitmap.createBitmap(3, sh, Bitmap.Config.ARGB_8888); //left  // 创建左指示线位图（3像素宽，全高）
            bmp[MEASURE_INDICATION_TOP] = Bitmap.createBitmap(sw, 3, Bitmap.Config.ARGB_8888); // top  // 创建上指示线位图（全宽，3像素高）
            bmp[MEASURE_INDICATION_RIGHT] = Bitmap.createBitmap(3, sh, Bitmap.Config.ARGB_8888);   //right // 创建右指示线位图
            bmp[MEASURE_INDICATION_BOTTOM] = Bitmap.createBitmap(sw, 3, Bitmap.Config.ARGB_8888); //bottom // 创建下指示线位图
            drawLine( bmp[MEASURE_INDICATION_LEFT],1,0,1,sh - 1);      // 在左指示线位图上绘制竖线
            drawLine( bmp[MEASURE_INDICATION_TOP],0,1,sw - 1, 1);      // 在上指示线位图上绘制横线
            drawLine( bmp[MEASURE_INDICATION_RIGHT],1,0,1,sh - 1);     // 在右指示线位图上绘制竖线
            drawLine( bmp[MEASURE_INDICATION_BOTTOM],0,1,sw - 1, 1);   // 在下指示线位图上绘制横线
        }

        /**
         * 波形区域高度变化时重新初始化指示线
         */
        public void changeLineHeight() {
            initLine();                                                 // 重新初始化指示线位图
        }


        /**
         * 在指定位图上绘制虚线
         * @param bmp 目标位图
         * @param x1 起点X
         * @param y1 起点Y
         * @param x2 终点X
         * @param y2 终点Y
         */
        private void drawLine(Bitmap bmp,int x1,int y1,int x2,int y2){
            Paint p = new Paint();                                      // 创建画笔

            Canvas canvas = new Canvas(bmp);                            // 创建画布绑定到位图
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); // 设置清除模式
            canvas.drawPaint(p);                                        // 清除位图
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC)); // 恢复源模式
            p.setStrokeWidth(1);                                        // 设置线宽1像素
            p.setColor(Color.LTGRAY);                                   // 设置浅灰色
            DashPathEffect dashPathEffect = new DashPathEffect(new float[]{20, 10}, 0);//设置虚线模式： 实现20 空白10 重复
            p.setPathEffect(dashPathEffect);                            // 应用虚线效果
            canvas.drawLine(x1,y1,x2,y2,p);                            // 绘制虚线
        }

        /**
         * 切换工作模式时重新初始化指示线
         * @param workMode 工作模式
         */
        @Override
        public void switchWorkMode(int workMode) {
            initLine();                                                 // 重新初始化指示线
        }

        /**
         * 使用Canvas方式绘制（空实现，指示线仅通过GL绘制）
         * @param canvas 目标画布
         */
        @Override
        public void draw(Canvas canvas) {

        }

        /**
         * 使用ICanvasGL方式绘制指示线
         * @param canvas GL画布
         */
        @Override
        public void draw(ICanvasGL canvas) {

            synchronized (this) {                                       // 同步锁
                canvasGL = canvas;                                      // 保存GL画布引用
                if ( isEnable() && isVisiable()) {                      // 功能启用且有可见线
                    if (isChanageBitmap) {                              // 位图有变更
                        for (int i = 0; i < bmp.length; i++) {          // 遍历所有指示线位图
                            canvas.invalidateTextureContent(bmp[i],oldBmp[i]); // 刷新纹理
                        }
                    }

                    if(visiable[MEASURE_INDICATION_LEFT]) {             // 左指示线可见
                        canvas.drawBitmap(bmp[MEASURE_INDICATION_LEFT], pos[MEASURE_INDICATION_LEFT] - 1, 0); // 绘制左指示线
                    }
                    if(visiable[MEASURE_INDICATION_TOP]) {              // 上指示线可见
                        canvas.drawBitmap(bmp[MEASURE_INDICATION_TOP], 0, (int) Math.round(ScopeBase.changeAccuracy(pos[MEASURE_INDICATION_TOP] * ScopeBase.getToUICoff())) - 1); // 绘制上指示线（FPGA坐标转UI坐标）
                    }
                    if(visiable[MEASURE_INDICATION_RIGHT]) {            // 右指示线可见
                        canvas.drawBitmap(bmp[MEASURE_INDICATION_RIGHT], pos[MEASURE_INDICATION_RIGHT] - 1, 0); // 绘制右指示线
                    }
                    if(visiable[MEASURE_INDICATION_BOTTOM]) {           // 下指示线可见
                        canvas.drawBitmap(bmp[MEASURE_INDICATION_BOTTOM], 0, (int) Math.round(ScopeBase.changeAccuracy(pos[MEASURE_INDICATION_BOTTOM] * ScopeBase.getToUICoff())) - 1); // 绘制下指示线（FPGA坐标转UI坐标）
                    }

                }
                isChanageBitmap = false;                                // 重置变更标记

            }
        }
    }

    /***
     * 所有测量
     *
     * ╔══════════════════════════════════════════════════════════════════════════════╗
     * ║                            AllMeasure                                     ║
     * ╠══════════════════════════════════════════════════════════════════════════════╣
     * ║ 在波形区域底部显示所有测量项（24项参数）的数值，按5列布局                   ║
     * ║ 带渐变分隔线，以通道颜色显示文本                                            ║
     * ╚══════════════════════════════════════════════════════════════════════════════╝
     */
    public class AllMeasure implements IMeasure, IWorkMode {

        //region 属性
        private boolean visible = false;                                // 是否可见

        /**
         * 查询是否可见
         * @return true表示可见
         */
        public boolean isVisible() {
            return visible;                                             // 返回可见性
        }

        /**
         * 设置可见性并重绘
         * @param visible true表示可见
         */
        public void setVisible(boolean visible) {
            this.visible = visible;                                     // 更新可见性
            draw();                                                     // 触发重绘
        }
        //endregion

        private List<MeasureItemStruct> measureList = new ArrayList<>(); // 所有测量项列表
        private Bitmap bmp;                                             // 离屏位图
        private Canvas mCanvas;                                         // 离屏画布
        private Paint p;                                                // 画笔
        private TextPaint textPaint;                                    // 文本描边画笔
        private boolean isChanageBitmap = false;                        // 位图变更标记
        private ICanvasGL canvasGL;                                     // GL画布引用

        /**
         * 通知GL画布刷新纹理
         */
        public void onRefresh(){
            if(canvasGL != null){                                       // GL画布引用有效
                canvasGL.onRefreshTexture();                            // 刷新纹理
            }
        }
        private int showX = 0, showY = 415;                             // 显示位置
        private int ItemInterval = 140;                                 // 测量项间距

        /**
         * 构造函数：初始化位图、画笔和渐变效果
         */
        public AllMeasure() {
            bmp = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), 200, Bitmap.Config.ARGB_8888); // 创建全宽位图
            mCanvas = new Canvas(bmp);                                  // 绑定画布
            p = new Paint();                                            // 创建画笔
            p.setTextSize(20);                                          // 设置文本大小
            p.setAntiAlias(true);                                       // 开启抗锯齿
            p.setStrokeWidth(1);                                        // 设置线宽

            textPaint=new TextPaint();                                  // 创建文本描边画笔
            textPaint.setTextSize(20);                                  // 设置文本大小
            textPaint.setAntiAlias(true);                               // 开启抗锯齿
            textPaint.setColor(Color.BLACK);                            // 设置描边色为黑色
            textPaint.setStrokeWidth(4);                                // 设置描边宽度
            textPaint.setStyle(Paint.Style.STROKE);                    // 设置描边样式

            int[] color = new int[]{                                    // 渐变颜色数组
                    Color.argb(20, 0, 160, 160),                        // 起始色：低透明度青色
                    Color.argb(100, 0, 255, 255),                       // 中间色：中透明度青色
                    Color.argb(20, 0, 160, 160)};                       // 结束色：低透明度青色
            linearGradient = new LinearGradient(0, 0, bmp.getWidth(), 0, color, null, Shader.TileMode.CLAMP); // 创建水平线性渐变
            iniParam();                                                 // 初始化测量项参数
            Point p = GlobalVar.get().getMeasureAllPosition(WorkModeManage.getInstance().getmWorkMode()); // 获取显示位置
            showX = p.x;                                                // 设置X坐标
            showY = p.y;                                                // 设置Y坐标
            draw();                                                     // 执行初始绘制
            ItemInterval = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) / 5; // 计算每列间距
        }

        //region IWorkMode接口

        /**
         * 切换工作模式时更新显示位置
         * @param workMode 工作模式
         */
        @Override
        public void switchWorkMode(@WorkMode int workMode) {
            updateShowXY();                                             // 更新显示坐标
        }

        //endregion

        /**
         * 根据测量行数和工作模式更新AllMeasure的Y坐标位置
         */
        private void updateShowXY() {
            Point p = GlobalVar.get().getMeasureAllPosition(WorkModeManage.getInstance().getmWorkMode()); // 获取基准位置
            int rowCount = MeasureManage.getInstance().getMeasureItem().getMeasureRowCount(); // 获取测量行数
            if (p == null) return;                                      // 位置为空则返回
            showX = p.x;                                                // 更新X坐标
            switch (rowCount) {                                         // 根据行数调整Y坐标
                case 0:
                    showY = 890;                                        // 0行时Y=890
                    break;
                case 1:
                    showY = 860;                                        // 1行时Y=860
                    break;
                case 2:
                    showY = 830;                                        // 2行时Y=830
                    break;
                case 3:
                    showY = 800;                                        // 3行时Y=800
                    break;
                case 4:
                    showY = 770;                                        // 4行时Y=770
                    break;
                default:
                    showY = p.y;                                        // 默认使用配置位置
                    break;
            }
            if(WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) { // YT缩放模式
                showY = Math.round((showY - 35) * GlobalVar.get().toZoomCoef()) - 10; // 按缩放系数调整Y坐标
            }
            boolean isShowMeasureStatic = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL); // 是否显示统计
            if (isShowMeasureStatic) {                                  // 如果启用了统计显示
//                    boolean all= CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);
                boolean mean = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MEAN); // 是否显示均值
                boolean max = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MAX); // 是否显示最大值
                boolean min = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MIN); // 是否显示最小值
                boolean delta = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_DELTA); // 是否显示差值
                boolean count = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_COUNT); // 是否显示计数
                int staticNum = 0;                                      // 统计项数量
                if (mean) staticNum++;                                  // 均值计数+1
                if (max) staticNum++;                                   // 最大值计数+1
                if (min) staticNum++;                                   // 最小值计数+1
                if (delta) staticNum++;                                 // 差值计数+1
                if (count) staticNum++;                                 // 计数计数+1
                showY = showY - 35 * Math.max(0, staticNum - (rowCount - 2)); // 根据统计项数量向上偏移
                if (rowCount == 0) {                                    // 无测量行时
                    showY += 55;                                        // 额外向下偏移
                }
            }
        }

        /**
         * 使用Canvas方式绘制全部测量
         * @param canvas 目标画布
         */
        @Override
        public void draw(Canvas canvas) {
            synchronized (bmp) {                                        // 同步锁定位图
                if (isVisible() == true) {                              // 如果可见
                    updateShowXY();                                     // 更新显示位置
                    canvas.drawBitmap(bmp, showX, showY, null);         // 绘制位图
                }
            }
        }

        /**
         * 使用ICanvasGL方式绘制全部测量
         * @param canvas GL画布
         */
        @Override
        public void draw(ICanvasGL canvas) {
            synchronized (bmp) {                                        // 同步锁定位图
                canvasGL = canvas;                                      // 保存GL画布引用
                if (isVisible() == true) {                              // 如果可见
                    if (isChanageBitmap) canvas.invalidateTextureContent(bmp,null); // 位图变更时刷新纹理
                    updateShowXY();                                     // 更新显示位置
                    canvas.drawBitmap(bmp, showX, showY);               // 绘制位图
                }
                isChanageBitmap = false;                                // 重置变更标记
            }
        }

        private volatile int chIdx = 0;                                 // 当前选中的通道索引

        //region  对外接口
        /**
         * 设置所有测量项的通道ID
         * @param channelId 通道ID
         */
        public void setChannelId(int channelId) {
            for (MeasureItemStruct c : measureList) {                   // 遍历所有测量项
                c.channelId = channelId;                                // 更新通道ID
            }
            draw();                                                     // 触发重绘
            chIdx = channelId;                                          // 更新选中通道索引
        }

        /**
         * 获取当前选中的通道索引
         * @return 通道索引
         */
        public int getChIdx() {
            return chIdx;                                               // 返回选中通道索引
        }

        /**
         * TODO
         * 延时、相位没有AllMeasure值
         * 设置所有测量项的数值数据
         * @param list 数据列表，顺序与measureList对应
         */
        public void setMeasureData(List<String> list) {
            if (list == null || measureList.size() != list.size()) {    // 列表为空或大小不匹配
                return;                                                 // 直接返回
            }
            for (int i = 0; i < measureList.size(); i++) {              // 遍历测量项
                measureList.get(i).data = list.get(i);                  // 更新测量数据
            }
            draw();                                                     // 触发重绘
        }
        //endregion

        //region 显示接口
        private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR); // 清除模式
        private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);     // 源模式
        private LinearGradient linearGradient;                          // 水平线性渐变

        /**
         * 内部绘制方法：清除旧内容，绘制渐变分隔线和测量数据文本
         */
        private void draw() {
            synchronized (bmp) {                                        // 同步锁定位图
                p.setXfermode(clearMode);                               // 设置清除模式
                mCanvas.drawPaint(p);                                   // 清除位图
                p.setXfermode(srcMode);                                 // 恢复源模式
                p.setShader(linearGradient);                            // 设置渐变着色器
                p.setAntiAlias(false);                                  // 关闭抗锯齿（线条更清晰）
                mCanvas.drawLine(0, 0, bmp.getWidth(), 0, p);          // 绘制顶部渐变线
                mCanvas.drawLine(0, 114, bmp.getWidth(), 114, p);      // 绘制中间渐变线
                p.setShader(null);                                      // 清除着色器
                p.setAntiAlias(true);                                   // 恢复抗锯齿
                for (int i = 0; i < measureList.size(); i++) {          // 遍历所有测量项
                    MeasureItemStruct c = measureList.get(i);           // 获取当前测量项
//                    if (TChan.isRef(c.getChannelId())) {
//                        p.setColor(TChan.getChannelColor(context,TChan.RefActive));
//                    } else {
                        p.setColor(TChan.getChannelColor(context, c.channelId)); // 设置通道颜色
//                    }
                    String text = c.measureName + ":";                  // 拼接测量名称
                    if (TChan.isSerial(c.getChannelId())) {             // 如果是串行通道
                        text += "----";                                 // 串行通道不显示数据
                    } else {
                        text += c.data;                                 // 拼接测量数据
                    }
                    int x, y, h;                                        // 坐标和高度
//                    h = getTextHeight(text);
//                    x = i * ItemInterval % GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());
                    x = i % 5 * 340;                                    // X坐标：每行5列，每列340像素
//                    y = i * 140 / GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.get().getmWorkMode()) * (bmp.getHeight() / 7) + h;
                    y = i / 5 * 22 + 21;                                // Y坐标：每行22像素，起始偏移21
                    mCanvas.drawText(text, x + 100, y, textPaint);      // 先用描边画笔绘制黑色描边
                    mCanvas.drawText(text, x + 100, y, p);             // 再用通道颜色画笔绘制文本
                }
                isChanageBitmap = true;                                 // 标记位图已变更
                onRefresh();                                            // 通知GL刷新
            }
        }

        private Rect rect = new Rect();                                 // 文本边界矩形

        /**
         * 获取文本像素高度
         * @param text 待测量的文本
         * @return 文本高度
         */
        private int getTextHeight(String text) {

            p.getTextBounds(text, 0, text.length(), rect);              // 测量文本边界
            int w = rect.width();                                       // 文本宽度（未使用）
            int h = rect.height();                                      // 文本高度
            return h;                                                   // 返回文本高度
        }
        //endregion

        //region 私有
        /**
         * 初始化AllMeasure的24个测量项参数
         */
        private void iniParam() {
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_Period, getMeasureIdToName(IMeasure.MeasureId_Period), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 周期
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_Freq, getMeasureIdToName(IMeasure.MeasureId_Freq), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 频率
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_RiseTime, getMeasureIdToName(IMeasure.MeasureId_RiseTime), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 上升时间
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_FallTime, getMeasureIdToName(IMeasure.MeasureId_FallTime), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 下降时间
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_DutyAdd, getMeasureIdToName(IMeasure.MeasureId_DutyAdd), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 正占空比
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_DutySub, getMeasureIdToName(IMeasure.MeasureId_DutySub), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 负占空比
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_WidthAdd, getMeasureIdToName(IMeasure.MeasureId_WidthAdd), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 正脉宽
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_WidthSub, getMeasureIdToName(IMeasure.MeasureId_WidthSub), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 负脉宽
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_BurstW, getMeasureIdToName(IMeasure.MeasureId_BurstW), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 猝发脉宽
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_ROV, getMeasureIdToName(IMeasure.MeasureId_ROV), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 正过冲
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_FOV, getMeasureIdToName(IMeasure.MeasureId_FOV), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 负过冲

            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_PKPK, getMeasureIdToName(IMeasure.MeasureId_PKPK), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 峰峰值
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_Amp, getMeasureIdToName(IMeasure.MeasureId_Amp), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 幅度
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_High, getMeasureIdToName(IMeasure.MeasureId_High), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 高电平
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_Low, getMeasureIdToName(IMeasure.MeasureId_Low), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 低电平
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_Max, getMeasureIdToName(IMeasure.MeasureId_Max), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 最大值
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_Min, getMeasureIdToName(IMeasure.MeasureId_Min), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 最小值
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_RMS, getMeasureIdToName(IMeasure.MeasureId_RMS), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 均方根值
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_CRMS, getMeasureIdToName(IMeasure.MeasureId_CRMS), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 电流均方根值
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_Mean, getMeasureIdToName(IMeasure.MeasureId_Mean), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 平均值
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_CMean, getMeasureIdToName(IMeasure.MeasureId_CMean), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 电流平均值
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_ACRMS, getMeasureIdToName(IMeasure.MeasureId_ACRMS), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 交流均方根值
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_PostitiveRate, getMeasureIdToName(IMeasure.MeasureId_PostitiveRate), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 正斜率
            addMeasureItem(new MeasureItemStruct(-1,TChan.Ch1, IMeasure.MeasureId_NegativeRate, getMeasureIdToName(IMeasure.MeasureId_NegativeRate), TopLayoutMeasureCommon.MEASURE_DATA_INIT)); // 负斜率
//            addMeasureItem(new MeasureItemStruct(TChan.Ch1, IMeasure.MeasureId_TVALUE, getMeasureIdToName(IMeasure.MeasureId_TVALUE), TopLayoutMeasureCommon.MEASURE_DATA_INIT));
        }

        /**
         * 添加测量项到列表
         * @param item 测量项结构
         * @return 始终返回true
         */
        private boolean addMeasureItem(MeasureItemStruct item) {
            measureList.add(item);                                      // 添加到列表末尾
            return true;                                                // 返回true
        }

        /**
         * 根据测量ID获取测量名称
         * @param MeasureId 测量ID
         * @return 测量名称字符串
         */
        private String getMeasureIdToName(int MeasureId) {
            String[] s = App.get().getResources().getStringArray(R.array.measures); // 获取测量名称数组
            return s[MeasureId];                                        // 返回对应名称

        }
        //endregion
    }


    /***
     *
     * 频率计 测量显示
     *
     * ╔══════════════════════════════════════════════════════════════════════════════╗
     * ║                       FrequencyMeterMeasure                               ║
     * ╠══════════════════════════════════════════════════════════════════════════════╣
     * ║ 在波形区域左上角显示频率计测量结果，格式为"通道名: xxx.xxx Hz"               ║
     * ╚══════════════════════════════════════════════════════════════════════════════╝
     */
    public class FrequencyMeterMeasure implements IMeasure, IWorkMode {

        //region 属性
        private int channelId = 1;                                      // 通道ID
        //        private float data = 0;
        private String data = "";                                       // 频率数据字符串
        private boolean visible = false;                                // 是否可见

        /**
         * 获取通道ID
         * @return 通道ID
         */
        public int getChannelId() {
            return channelId;                                           // 返回通道ID
        }

        /**
         * 设置通道ID并重绘
         * @param channelId 通道ID
         */
        public void setChannelId(int channelId) {
            this.channelId = channelId;                                 // 更新通道ID
            draw();                                                     // 触发重绘
        }

        /**
         * 设置频率数据并重绘
         * @param data 频率数据字符串
         */
        public void setData(String data) {
            this.data = data;                                           // 更新数据
            draw();                                                     // 触发重绘
        }

        /**
         * 获取频率数据
         * @return 频率数据字符串
         */
        public String getData() {
            return data;                                                // 返回数据
        }

        /**
         * 设置可见性并重绘
         * @param visible true表示可见
         */
        public void setVisible(boolean visible) {
            this.visible = visible;                                     // 更新可见性
            draw();                                                     // 触发重绘
        }

        /**
         * 获取可见性
         * @return true表示可见
         */
        public boolean getVisible() {
            return visible;                                             // 返回可见性
        }
        //endregion

        private Bitmap bmp;                                             // 离屏位图
        private Canvas mCanvas;                                         // 离屏画布
        private Paint p;                                                // 画笔
        private boolean isChangeBitmap = false;                         // 位图变更标记
        private ICanvasGL canvasGL;                                     // GL画布引用

        /**
         * 通知GL画布刷新纹理
         */
        public void onRefresh(){
            if(canvasGL != null){                                       // GL画布引用有效
                canvasGL.onRefreshTexture();                            // 刷新纹理
            }
        }
        private int showX = 30, showY = 30;                             // 显示位置

        /**
         * 构造函数：初始化位图、画笔，切换到当前工作模式并绘制
         */
        public FrequencyMeterMeasure() {
            bmp = Bitmap.createBitmap(200, 50, Bitmap.Config.ARGB_8888); // 创建200x50位图
            mCanvas = new Canvas(bmp);                                  // 绑定画布
            p = new Paint();                                            // 创建画笔
            p.setTextSize(20);                                          // 设置文本大小
            p.setAntiAlias(true);                                       // 开启抗锯齿
            switchWorkMode(WorkModeManage.getInstance().getmWorkMode()); // 切换到当前工作模式以获取位置
            draw();                                                     // 执行初始绘制
        }

        //region IWorkMode接口

        /**
         * 切换工作模式，更新频率计显示位置
         * @param workMode 工作模式
         */
        @Override
        public void switchWorkMode(@WorkMode int workMode) {
            Point p = GlobalVar.get().getMeasureFrequencyMeterPosition(workMode); // 获取频率计位置
            if (p == null) {                                            // 位置为空
                showX = 1000;                                           // X坐标设为屏幕外
                showY = 0;                                              // Y坐标设为0
                return;                                                 // 直接返回
            }
            showX = p.x;                                                // 更新X坐标
            showY = p.y;                                                // 更新Y坐标


        }


        //endregion


        /**
         * 使用Canvas方式绘制频率计
         * @param canvas 目标画布
         */
        @Override
        public void draw(Canvas canvas) {
            synchronized (bmp) {                                        // 同步锁定位图
                if (visible) canvas.drawBitmap(bmp, showX, showY, null); // 可见时绘制位图
            }
        }

        /**
         * 使用ICanvasGL方式绘制频率计
         * @param canvas GL画布
         */
        @Override
        public void draw(ICanvasGL canvas) {
            synchronized (bmp) {                                        // 同步锁定位图
                canvasGL = canvas;                                      // 保存GL画布引用
                if (visible) {                                          // 可见时
                    if (isChangeBitmap) canvas.invalidateTextureContent(bmp,null); // 位图变更时刷新纹理
                    canvas.drawBitmap(bmp, showX, showY);               // 绘制位图
                }
                isChangeBitmap = false;                                 // 重置变更标记
            }
        }

        //region 显示
        /**
         * 内部绘制方法：清除旧内容，绘制频率计文本
         */
        public void draw() {
            synchronized (bmp) {                                        // 同步锁定位图
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); // 设置清除模式
                mCanvas.drawPaint(p);                                   // 清除位图
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC)); // 恢复源模式

                if (TChan.isRef(channelId)) {                           // 如果是Ref参考通道
                    p.setColor(TChan.getChannelColor(context,TChan.RefActive)); // 设置Ref颜色
                } else {
                    p.setColor(TChan.getChannelColor(context,channelId)); // 设置通道颜色
                }
                String channelName = TChan.getChannelName(channelId);   // 获取通道名称
                if (Objects.equals(channelName, "")) return;            // 通道名为空则返回
                String text;                                            // 显示文本
                if (TextUtils.isEmpty(data)) {                          // 数据为空
                    text = channelName + ": " + "---.--- Hz";           // 显示占位符
                } else
                    text = channelName + ": " + data; /*String.valueOf(data) + "Hz"*/ // 显示实际数据
                ;

                int x, y, h;                                            // 坐标和高度
                h = getTextHeight(text);                                // 获取文本高度
                x = 0;                                                  // X坐标为0
                y = h;                                                  // Y坐标为文本高度
                mCanvas.drawText(text, x, y, p);                        // 绘制文本

                isChangeBitmap = true;                                  // 标记位图已变更
                onRefresh();                                            // 通知GL刷新

            }
        }

        /**
         * 获取文本像素高度
         * @param text 待测量的文本
         * @return 文本高度
         */
        private int getTextHeight(String text) {
            Rect rect = new Rect();                                     // 创建边界矩形
            p.getTextBounds(text, 0, text.length(), rect);              // 测量文本边界
            int w = rect.width();                                       // 文本宽度（未使用）
            int h = rect.height();                                      // 文本高度
            return h;                                                   // 返回文本高度
        }
        //endregion

        //region 界面接口

        //endregion

    }

    /**
     * 测量项点击监听器接口
     */
    public interface MeasureItemListener{
        /**
         * 测量项被点击时回调
         * @param chIdx 通道索引
         * @param measure 测量项ID
         */
        void onClick(int chIdx,int measure);
    }

    /**
     * 测量行数变化监听器接口
     */
    public interface MeasureRowChangeListener {
        /**
         * 测量行数变化时回调
         * @param rowCount 新的行数
         */
        void measureRowChange(int rowCount);
    }

    /**
     * 将数字转换为带圈数字符号（①~㊿）
     * @param number 数字（1~40）
     * @return 带圈数字Unicode字符串，不在范围内返回空串
     */
    public static String getEnclosedNumber(int number){
        if(number >= 1 && number <= 20){                                // 1~20使用Unicode带圈数字
            number += 0x245F;                                           // 偏移到①~⑳
        }else if(number >= 21 && number <= 35){                         // 21~35使用Unicode带圈数字
            number += 0x323C;                                           // 偏移到㉑~㉟
        }else if(number >= 36 && number <= 40){                         // 36~40使用Unicode带圈数字
            number += 0x328D;                                           // 偏移到㊱~㊵
        }else{
            return "";                                                  // 超出范围返回空串
        }
        return new String(Character.toChars(number));                   // 将Unicode码点转为字符串
    }

    /***
     * 测量显示类
     *
     * ╔══════════════════════════════════════════════════════════════════════════════╗
     * ║                           MeasureItem                                     ║
     * ╠══════════════════════════════════════════════════════════════════════════════╣
     * ║ 管理波形区域中的测量项列表，支持添加/删除/选中测量项，并绘制到离屏位图上      ║
     * ║ 支持触摸点击选中测量项，选中后触发阈值更新和指示线显示                       ║
     * ╚══════════════════════════════════════════════════════════════════════════════╝
     */
    public class MeasureItem implements IMeasure, IWorkMode {

        private List<MeasureItemStruct> measureList = new ArrayList<>(); // 测量项列表

        /**
         * 获取所有测量项列表（包括空占位项）
         * @return 测量项列表
         */
        public List<MeasureItemStruct> getMeasureList() {
            return measureList;                                         // 返回测量项列表
        }

        /**
         * 获取有效测量项列表（排除空占位项）
         * @return 有效测量项列表
         */
        public List<MeasureItemStruct> getValidMeasureList() {
            List<MeasureItemStruct> tempList = new ArrayList<>();       // 创建临时列表
            for (MeasureItemStruct itemStruct : measureList) {          // 遍历所有测量项
                if (itemStruct.getChannelId() > 0) {                   // 通道ID大于0表示有效
                    tempList.add(itemStruct);                           // 添加到有效列表
                }
            }
            return tempList;                                            // 返回有效列表
        }

        /**
         * 计算测量项的总行数
         * @return 行数
         */
        public int getMeasureRowCount() {
            int perRowCount = GlobalVar.get().getMeasureItemPerRowCount();//每行数量
            int measureCount = measureList.size();                      // 测量项总数
            return measureCount / perRowCount + ((measureCount % perRowCount == 0) ? 0 : 1);//总行数
        }

        private Bitmap bmp;                                             // 离屏位图
        private Canvas mCanvas;                                         // 离屏画布
        private Paint p;                                                // 画笔
        private boolean isChanageBitmap = false;                        // 位图变更标记
        private ICanvasGL canvasGL;                                     // GL画布引用

        /**
         * 通知GL画布刷新纹理
         */
        public void onRefresh(){
            if(canvasGL != null){                                       // GL画布引用有效
                canvasGL.onRefreshTexture();                            // 刷新纹理
            }
        }
        private int showX = 0, showY = 620;                             // 显示位置
        private int ItemInterval = 140;                                 // 测量项间距
        public Consumer<MeasureItemStruct> OnClickEvent=this::onClickEvent; // 点击事件消费者
        public Consumer<Boolean> OnRefresh;                             // 刷新事件消费者

        private MeasureItemListener itemListener;                       // 测量项点击监听器
        private MeasureRowChangeListener rowChangeListener;             // 行数变化监听器
        private boolean SelectEnable=false;                             // 选择功能是否启用
        private boolean visible=false;                                  // 是否可见

        /**
         * 设置测量项点击监听器
         * @param listener 监听器
         */
        public void setMeasureItemListene(MeasureItemListener listener){
            this.itemListener = listener;                               // 保存监听器
        }

        /**
         * 设置行数变化监听器
         * @param listener 监听器
         */
        public void setMeasureRowChangeListener(MeasureRowChangeListener listener) {
            this.rowChangeListener = listener;                          // 保存监听器
        }

        /**
         * 构造函数：初始化位图、画笔和显示位置
         */
        public MeasureItem() {
            bmp = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), 100, Bitmap.Config.ARGB_8888); // 创建位图
            mCanvas = new Canvas(bmp);                                  // 绑定画布
            p = new Paint();                                            // 创建画笔
            p.setTextSize(20);                                          // 设置文本大小
            p.setAntiAlias(true);                                       // 开启抗锯齿

            Point p = GlobalVar.get().getMeasureItemPosition(WorkModeManage.getInstance().getmWorkMode()); // 获取显示位置
            showX = p.x;                                                // 设置X坐标
            showY = p.y;                                                // 设置Y坐标
            ItemInterval = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) / 5; // 计算间距
        }

        /**
         * 点击事件内部处理：通知监听器
         * @param c 被点击的测量项
         */
        private void onClickEvent(MeasureItemStruct c){

            if(itemListener != null){                                   // 监听器不为空
                itemListener.onClick(c.channelId,c.measureId);          // 通知监听器
            }
        }
        //region IWorkMode接口

        /**
         * 切换工作模式，更新测量项显示位置
         * @param workMode 工作模式
         */
        @Override
        public void switchWorkMode(@WorkMode int workMode) {
            Point p = GlobalVar.get().getMeasureItemPosition(workMode); // 获取位置
            if (p == null) {                                            // 位置为空
                showX = 1000;                                           // X坐标设为屏幕外
                showY = 0;                                              // Y坐标设为0
                return;                                                 // 直接返回
            }
            showX = p.x;                                                // 更新X坐标
            showY = p.y;                                                // 更新Y坐标
        }

        //endregion


        //region 显示接口
        /**
         * 使用Canvas方式绘制测量项
         * @param canvas 目标画布
         */
        @Override
        public void draw(Canvas canvas) {
            if (isShow()) return;                                       // 统计显示模式下不绘制此层
            synchronized (bmp) {                                        // 同步锁定位图
                canvas.drawBitmap(bmp, showX, showY, null);             // 绘制位图
            }
        }

        /**
         * 使用ICanvasGL方式绘制测量项
         * @param canvas GL画布
         */
        @Override
        public void draw(ICanvasGL canvas) {
            if (isShow()) return;                                       // 统计显示模式下不绘制此层
            if (visible==false) return;                                 // 不可见时返回
            synchronized (bmp) {                                        // 同步锁定位图
                canvasGL = canvas;                                      // 保存GL画布引用
                if (isChanageBitmap) canvas.invalidateTextureContent(bmp,null); // 位图变更时刷新纹理
                canvas.drawBitmap(bmp, showX, showY);                   // 绘制位图
                isChanageBitmap = false;                                // 重置变更标记
            }
        }

        /**
         * 判断是否处于统计显示模式（统计模式下此层不绘制）
         * @return true表示正在统计模式
         */
        private boolean isShow() {
            boolean isShowMeasureStatic = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL); // 查询统计模式开关
            return isShowMeasureStatic;                                 // 返回统计模式状态
        }

        /**
         * 判断测量项点击选择功能是否启用
         * @return true表示启用
         */
        private boolean isMeasureItemClickEnable(){
            return CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_SETTING_INDICATOR); // 查询指示器开关
        }

        /**
         * 设置选择功能启用状态
         * @param state true表示启用
         */
        public void setSelectEnable(boolean state){
            this.SelectEnable=state;                                    // 更新启用状态
            draw();                                                     // 触发重绘
        }

        /**
         * 查询选择功能是否启用
         * @return true表示启用
         */
        public boolean isSelectEnable(){
            return this.SelectEnable;                                   // 返回启用状态
        }

        /**
         * 检查是否存在通过点击选中的测量项
         * @return true表示存在点击选中的项
         */
        public boolean isExistClickSelected(){
            for(int i=0;i<measureList.size();i++){                     // 遍历所有测量项
                MeasureItemStruct c= measureList.get(i);                // 获取当前项
                if (c.isSelected && c.isClickSelected){                 // 既是选中又是点击选中
                    return true;                                        // 存在点击选中项
                }
            }
            return false;                                               // 不存在点击选中项
        }

        /**
         * 取消所有测量项的选中状态
         */
        public void CancelAllSelected(){
            for(int i=0;i<measureList.size();i++){                     // 遍历所有测量项
                MeasureItemStruct c= measureList.get(i);                // 获取当前项
                c.isSelected=false;                                     // 取消选中
            }
        }



        /**
         * 处理触摸事件：点击选中测量项
         * @param event 触摸事件
         */
        public void dealCursorItem(MotionEvent event){

            switch (event.getAction() & MotionEvent.ACTION_MASK){       // 获取触摸动作类型
                case MotionEvent.ACTION_DOWN:                           // 按下事件
                     int x=(int)event.getX();                           // 获取触摸X坐标
                     int y= (int)event.getY()-showY;                    // 获取触摸Y坐标（减去偏移）

                     MeasureItemStruct tem=null;                        // 记录之前选中的项
                     boolean isChangeSelect=false;                      // 选中是否变化
                     for(int i=0;i<measureList.size();i++){             // 遍历所有测量项
                         MeasureItemStruct c= measureList.get(i);       // 获取当前项
                         if (c.isSelected) tem=c;                       // 记录之前选中的项
                         if (c.rect.contains(x,y) && isMeasureItemClickEnable()){ // 触摸点在项区域内且点击启用
                             c.isSelected=true;                         // 标记选中
                             c.isClickSelected=true;                    // 标记为点击选中
                             isChangeSelect=true;                       // 选中已变化
                             if ((OnClickEvent!=null) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)) { // 事件和版本检查
                                 OnClickEvent.accept(c);                // 触发点击事件
                             }
                         }else {
                             c.isSelected=false;                        // 取消选中
                         }
                     }
                     if (isChangeSelect==false && tem!=null){           // 没有新选中项但有旧选中项
                         tem.isSelected=true;                           // 恢复旧选中项
                     }else {
                         draw();                                        // 选中变化，触发重绘
                     }

                    break;
            }
        }

        /**
         * 通过索引选中测量项（用于键盘/旋钮操作）
         * @param idx 测量项索引
         * @return true表示成功选中
         */
        public boolean setSelectItem(int idx){
            if (idx<0) return false;                                    // 索引无效

            MeasureItemStruct tem=null;                                 // 记录之前选中的项
            boolean isChangeSelect=false;                               // 选中是否变化
            boolean isSelectItem=false;                                 // 是否成功选中
            boolean isShowDialog = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL); // 是否为统计模式
            List<MeasureItemStruct> validMeasureList = null;            // 待遍历的列表
            if (isShowDialog) {                                         // 统计模式
                validMeasureList = getValidMeasureList();               // 使用有效列表
            } else {
                validMeasureList = measureList;                         // 使用完整列表
            }
            for (int i = 0; i < validMeasureList.size(); i++) {         // 遍历测量项
                MeasureItemStruct c = validMeasureList.get(i);          // 获取当前项
                if (c.isSelected) tem=c;                                // 记录之前选中的项
                if (idx==i && isMeasureItemClickEnable()){              // 索引匹配且点击启用
                    c.isSelected=true;                                  // 标记选中
                    c.isClickSelected=true;                             // 标记为点击选中
                    isChangeSelect=true;                                // 选中已变化
                    if ((OnClickEvent!=null) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)) { // 事件和版本检查
                        OnClickEvent.accept(c);                         // 触发点击事件
                        isSelectItem=true;                              // 标记选中成功
                    }
                }else {
                    c.isSelected=false;                                 // 取消选中
                    c.isClickSelected=false;                            // 取消点击选中
                }
                if (isChangeSelect==false && tem!=null){                // 没有新选中项但有旧选中项
                    tem.isClickSelected=true;                           // 恢复旧选中项的点击选中状态
                }else {
                    draw();                                             // 选中变化，触发重绘
                }

            }

           return isSelectItem;                                         // 返回是否成功选中
        }

        /**
         * 获取当前选中的测量项
         * @return 选中的测量项，没有选中时返回最后一项
         */
        public MeasureItemStruct getSelectItem() {
            List<MeasureItemStruct> validMeasureList = getValidMeasureList(); // 获取有效列表
            if (validMeasureList == null || validMeasureList.size() == 0) return null; // 列表为空返回null
            for (int i = 0; i < validMeasureList.size(); i++) {         // 遍历有效列表
                MeasureItemStruct c = validMeasureList.get(i);          // 获取当前项
                if (c.isSelected) {                                     // 如果是选中项
                    return c;                                           // 返回选中项
                }
            }

            return validMeasureList.get(validMeasureList.size() - 1);   // 默认返回最后一项
        }



        /**
         * 内部绘制方法：清除旧内容，按5列布局绘制测量项文本
         */
        private void draw() {
            synchronized (bmp) {                                        // 同步锁定位图
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); // 设置清除模式
                mCanvas.drawPaint(p);                                   // 清除位图
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC)); // 恢复源模式
                if (measureList.size() > 0) {                           // 有测量项
                    int h = getTextHeight(measureList.get(0).measureName) + 3; // 获取文本高度+间距
                    int x, y;                                           // 坐标
                    for (int i = 0; i < measureList.size(); i++) {      // 遍历所有测量项
                        MeasureItemStruct c = measureList.get(i);       // 获取当前项

                        String text = c.measureName + ":" + c.data;     // 拼接文本

//                        x = i * ItemInterval % GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());
                        x = i % 5 * 340+100;                            // X坐标：每行5列，每列340像素，偏移100
//                        y = i * ItemInterval / GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) * (bmp.getHeight() / 5) + h;
                        y = i / 5 * 30 + 18;                            // Y坐标：每行30像素，起始偏移18
                        if (c.isSelected && SelectEnable){              // 选中且选择功能启用
                            text="["+text+"]";                          // 给选中文本加方括号
                            p.setColor(Color.WHITE);                    // 设置白色
                            int w=  getTextWidth(text);                 // 获取文本宽度
                            mCanvas.drawLine(x,y+5,x+w,y+5,p);         // 绘制下划线
                        }
                        if (TChan.isRef(c.getChannelId())) {            // 如果是Ref参考通道
                            p.setColor(TChan.getChannelColor(context,TChan.RefActive)); // 设置Ref颜色
                        } else {
                            p.setColor(TChan.getChannelColor(context, c.channelId)); // 设置通道颜色
                        }
                        mCanvas.drawText(text, x , y, p);               // 绘制文本
                        c.rect.set(x,y-18,x+200, y-18+30);             // 更新测量项的触摸区域矩形
                    }
                }
                isChanageBitmap = true;                                 // 标记位图已变更
                onRefresh();                                            // 通知GL刷新
                if (OnRefresh!=null){                                   // 刷新回调不为空
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 版本检查
                        OnRefresh.accept(isChanageBitmap);              // 通知刷新
                    }
                }
//                for(int i=0;i<measureList.size();i++){
//                    Log.d("Tag.Debug", String.format("draw: %s",measureList.get(i).toString() ));
//                }
            }
        }


        /**
         * 获取文本像素高度
         * @param text 待测量的文本
         * @return 文本高度
         */
        private int getTextHeight(String text) {
            Rect rect = new Rect();                                     // 创建边界矩形
            p.getTextBounds(text, 0, text.length(), rect);              // 测量文本边界
            int w = rect.width();                                       // 文本宽度（未使用）
            int h = rect.height();                                      // 文本高度
            return h;                                                   // 返回文本高度
        }

        /**
         * 获取文本像素宽度
         * @param text 待测量的文本
         * @return 文本宽度
         */
        private int getTextWidth(String text) {
            Rect rect = new Rect();                                     // 创建边界矩形
            p.getTextBounds(text, 0, text.length(), rect);              // 测量文本边界
            int w = rect.width();                                       // 文本宽度
            int h = rect.height();                                      // 文本高度（未使用）
            return w;                                                   // 返回文本宽度
        }
        //endregion

        //region 最外层接口

        /***
         * 添加测量项目
         * @param item 测量项结构
         * @return -1不成功
         */
        public boolean addMeasureItem(MeasureItemStruct item) {
            if (getValidMeasureList().size() >= GlobalVar.get().getMeasureItemCount()) return false; // 超出最大数量
            if (isExist(item)) {                                        // 不存在重复项

                if (!isExistClickSelected()){                           // 没有点击选中项
                    CancelAllSelected();                                // 取消所有选中
                    item.isSelected=true;                               // 选中新添加的项
                }

                boolean addInEmptyPos = false;                          // 是否添加到空位
                ListIterator<MeasureItemStruct> iterator = measureList.listIterator(); // 创建列表迭代器
                while (iterator.hasNext()) {                            // 遍历列表
                    MeasureItemStruct itemStruct = iterator.next();     // 获取当前项
                    if (itemStruct.getChannelId() < 0) {                // 找到空占位项
                        iterator.remove();                              // 移除空占位项
                        iterator.add(item);                             // 在空位添加新项
                        addInEmptyPos = true;                           // 标记已添加到空位
                        break;                                          // 跳出循环
                    }
                }
                if(!addInEmptyPos) {                                    // 没有找到空位
                    measureList.add(item);                              // 添加到列表末尾
                }
                clearRowEmpty();                                        // 清理空行
                draw();                                                 // 触发重绘
                return true;                                            // 添加成功
            }
            return false;                                               // 已存在重复项，添加失败
        }


        /**
         * 指定位置添加空占位item
         * @param position 插入位置
         * @param item 测量项结构
         * @return true表示添加成功
         */
        public boolean addEmptyMeasureItem(int position, MeasureItemStruct item) {
            if (position >= measureList.size()) {                       // 位置超出列表范围
                measureList.add(item);                                  // 添加到末尾
            } else {
                measureList.add(position, item);                        // 在指定位置插入
            }

            if (measureList.size() > GlobalVar.get().getMeasureItemCount()) { // 超出最大数量
                measureList.remove(position);                           // 移除刚添加的项
                return false;                                           // 返回失败
            }
//            clearRowEmpty();
            draw();                                                     // 触发重绘
            return true;                                                // 返回成功
        }


        /**
         * 根据通道ID和测量ID查找测量项
         * @param channelId 通道ID
         * @param measureId 测量ID
         * @return 匹配的测量项，未找到返回null
         */
        public MeasureItemStruct getMeasureItemStruct(int channelId, int measureId) {
            MeasureItemStruct measureItemStruct = null;                 // 结果引用
            for (MeasureItemStruct itemStruct : measureList) {          // 遍历所有测量项
                if (itemStruct.getChannelId() == channelId && itemStruct.getMeasureId() == measureId) { // 通道和ID都匹配
                    measureItemStruct = itemStruct;                     // 记录匹配项
                    break;                                              // 跳出循环
                }
            }
            return measureItemStruct;                                   // 返回结果
        }



        /**
         * 更新测量项的数据值
         * @param item 包含新数据的测量项
         * @return true表示更新成功
         */
        public boolean updateMeasureItem(MeasureItemStruct item) {
            if (!isExist(item)) {                                       // 不存在重复项（可以更新）
                for (MeasureItemStruct itemStruct : measureList) {      // 遍历所有测量项
                    if (itemStruct.channelId == item.channelId && itemStruct.measureId == item.measureId) { // 匹配项
                        itemStruct.data = item.data;                    // 更新数据
                    }
                }
                draw();                                                 // 触发重绘
                return true;                                            // 更新成功
            }
            return false;                                               // 已存在重复项，更新失败
        }

        /***
         * 删除测量项目
         * @param index 列表索引
         * @return true表示删除成功
         */
        public boolean delMeasureItem(int index) {
            if (measureList.size() > index) {                           // 索引有效
                if (measureList.get(index).isSelected && measureList.size()>=2){ // 被删除项是选中项且列表至少2项
                    measureList.remove(index);                          // 移除项
                    MeasureItemStruct m= measureList.get(measureList.size()-1); // 获取最后一项
                    m.isSelected=true;                                  // 选中最后一项
                }else {
                  measureList.remove(index);                            // 直接移除
                }
                clearRowEmpty();                                        // 清理空行
                draw();                                                 // 触发重绘
                return true;                                            // 删除成功
            }
            return false;                                               // 索引无效
        }

        /**
         * 删除所有测量项
         * @return true表示删除成功
         */
        public boolean delAllMeasureItem() {
            for (int i = measureList.size() - 1; i >= 0; i--) {        // 从后往前遍历
                measureList.remove(i);                                  // 移除每一项
            }
            clearRowEmpty();                                            // 清理空行
            draw();                                                     // 触发重绘
            return true;                                                // 删除成功
        }

        /**
         * 根据通道ID和测量ID删除测量项（替换为空占位项）
         * @param channelId 通道ID
         * @param measureId 测量ID
         * @return true表示删除成功
         */
        public boolean delMeasureItem(int channelId, int measureId) {
            for (int i = 0; i < measureList.size(); i++) {              // 遍历所有测量项
                MeasureItemStruct c = measureList.get(i);               // 获取当前项
                if (c.channelId == channelId && c.measureId == measureId) { // 匹配
//                    measureList.remove(i);
                    MeasureManage.MeasureItemStruct item = MeasureManage.getInstance().new MeasureItemStruct(-1,-1, -1, "", ""); // 创建空占位项
                    measureList.set(i, item);                           // 替换为空占位项
                    clearRowEmpty();                                    // 清理空行
                    setLastValidItemSelect(c);                          // 将选中状态转移到最后一项
                    draw();                                             // 触发重绘
                    return true;                                        // 删除成功
                }
            }
            return false;                                               // 未找到匹配项
        }

        /**
         * 将选中状态从被删除项转移到最后一个有效项
         * @param c 被删除的测量项
         */
        private void setLastValidItemSelect(MeasureItemStruct c) {
            if (!c.isSelected || measureList.size() <= 0) return;       // 被删除项未选中或列表为空
            for (int i = measureList.size() - 1; i >= 0; i--) {        // 从后往前遍历
                MeasureItemStruct item = measureList.get(i);            // 获取当前项
                if (item.getChannelId() < 0) continue;                  // 跳过空占位项
                item.isSelected = true;                                 // 选中最后一个有效项
                break;                                                  // 跳出循环
            }
        }

        /***
         * 是否存在
         * @return true不存在
         */
        private boolean isExist(MeasureItemStruct item) {
            for (MeasureItemStruct c : measureList) {                   // 遍历所有测量项
                if (c.channelId == item.channelId && c.measureId == item.measureId) // 通道和ID都匹配
                    return false;                                       // 已存在，返回false
            }
            return true;                                                // 不存在，返回true
        }

        /**
         * 若一行都为空，则删除此行空数据
         */
        public void clearRowEmpty() {
            List<MeasureItemStruct> newList = new ArrayList<>();        // 新列表，存放有效行
            int perRowCount = GlobalVar.get().getMeasureItemPerRowCount();//每行数量
            int measureCount = measureList.size();                      // 测量项总数
            int measureRowCount = measureCount / perRowCount + ((measureCount % perRowCount == 0) ? 0 : 1);//总行数
            for (int i = 0; i < measureCount; i += perRowCount) {       // 逐行遍历
                int end = Math.min(i + perRowCount, measureCount);      // 当前行结束索引
                boolean isEmptyRow = true;                              // 假设当前行为空

                for (int j = i; j < end; j++) {                         // 遍历当前行的所有项
                    if (measureList.get(j).getChannelId() >= 0) {       // 找到有效项
                        isEmptyRow = false;                             // 当前行非空
                        break;                                          // 跳出行内循环
                    }
                }
                if (!isEmptyRow) {                                      // 非空行
                    newList.addAll(measureList.subList(i, end));         // 将整行添加到新列表
                }
            }
            measureList.clear();                                        // 清空原列表
            measureList.addAll(newList);                                // 用新列表替换
            measureCount = measureList.size();                          // 更新总数
            measureRowCount = measureCount / perRowCount + ((measureCount % perRowCount == 0) ? 0 : 1);//总行数
            rowChangeListener.measureRowChange(measureRowCount);        // 通知行数变化
            Command.get().getMeasure().changeMeasurePos();              // 通知ARM更新测量位置
        }

        // endregion
    }

    /***
     * 测量结构
     *
     * ╔══════════════════════════════════════════════════════════════════════════════╗
     * ║                         MeasureItemStruct                                 ║
     * ╠══════════════════════════════════════════════════════════════════════════════╣
     * ║ 测量项数据结构，存储单个测量项的通道、ID、名称、数值和显示位置信息           ║
     * ╚══════════════════════════════════════════════════════════════════════════════╝
     */
    public class MeasureItemStruct {
        int no;                                                         // 序号
        /**
         * 1 - 9
         */
        int channelId;                                                  // 通道ID（1~9）
        int measureId;                                                  // 测量项ID
        String measureName;                                             // 测量项名称
        /*float或者'----' */
        String data;                                                    // 测量数据值
        /**
         * 在当前图片中显示的位置
         */
        Rect rect=new Rect();                                           // 触摸区域矩形
        boolean isSelected=false;                                       // 是否选中
        /** 是否是点击选择 */
        boolean isClickSelected=false;                                  // 是否通过点击选中（区分旋钮选中）

        /**
         * 构造函数
         * @param no 序号
         * @param channelId 通道ID
         * @param measureId 测量ID
         * @param measureName 测量名称
         * @param data 测量数据
         */
        public MeasureItemStruct(int no,int channelId, int measureId, String measureName, String data) {
            this.no = no;                                               // 设置序号
            this.channelId = channelId;                                 // 设置通道ID
            this.measureId = measureId;                                 // 设置测量ID
            this.measureName = measureName;                             // 设置测量名称
            this.data = data;                                           // 设置测量数据
        }

        /**
         * 获取序号
         * @return 序号
         */
        public int getNo(){return no;}

        /**
         * 获取通道ID
         * @return 通道ID
         */
        public int getChannelId() {
            return channelId;                                           // 返回通道ID
        }

        /**
         * 获取测量ID
         * @return 测量ID
         */
        public int getMeasureId() {
            return measureId;                                           // 返回测量ID
        }

        /**
         * 获取测量名称
         * @return 测量名称
         */
        public String getMeasureName() {
            return measureName;                                         // 返回测量名称
        }

        /**
         * 查询是否选中
         * @return true表示选中
         */
        public boolean isSelected(){
            return isSelected;                                          // 返回选中状态
        }

        /**
         * 查询是否点击选中
         * @return true表示点击选中
         */
        public boolean isClickSelected(){
            return isClickSelected;                                     // 返回点击选中状态
        }

        /**
         * 获取测量数据
         * @return 测量数据字符串
         */
        public String getData() {
            return data;                                                // 返回测量数据
        }

        /**
         * 转为字符串描述
         * @return 格式化字符串
         */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("MeasureItemStruct{"); // 创建StringBuilder
            sb.append("channelId=").append(channelId);                  // 拼接通道ID
            sb.append(", measureId=").append(measureId);                // 拼接测量ID
            sb.append(", measureName='").append(measureName).append('\''); // 拼接测量名称
            sb.append(", data='").append(data).append('\'');            // 拼接测量数据
            sb.append(", rect=").append(rect);                          // 拼接触摸区域
            sb.append('}');                                             // 拼接结尾括号
            return sb.toString();                                       // 返回字符串
        }
    }


    /**
     * FFT测量类：在波形区域显示FFT分析的频率/幅度结果
     *
     * ╔══════════════════════════════════════════════════════════════════════════════╗
     * ║                           FFTMeasure                                      ║
     * ╠══════════════════════════════════════════════════════════════════════════════╣
     * ║ 显示FFT（快速傅里叶变换）测量结果，包括数学通道号、幅度、频率、DC分量      ║
     * ╚══════════════════════════════════════════════════════════════════════════════╝
     */
    public static class FFTMeasure implements IMeasure, IWorkMode {

        //region 属性
        private String dataA = "---";                                   // 幅度数据
        private String dataV = "---";                                   // DC分量数据
        private String dataFre = "---";                                 // 频率数据
        private String labelA = "";                                     // 幅度标签
        private String labelV = "";                                     // DC分量标签
        private String labelFre = "";                                   // 频率标签
        private boolean visible = false;                                // 是否可见
        private int mathNumber;                                         // 数学通道号

        /**
         * 设置可见性并重绘
         * @param visible true表示可见
         */
        public void setVisible(boolean visible) {
            this.visible = visible;                                     // 更新可见性
            draw();                                                     // 触发重绘
        }

        /**
         * 设置文本颜色
         * @param color 颜色值
         */
        public void setColor(int color) {
            p.setColor(color);                                          // 更新画笔颜色
        }

        /**
         * 查询是否可见
         * @return true表示可见
         */
        public boolean isVisible() {
            return visible;                                             // 返回可见性
        }

        /**
         * 设置标签文本并重绘
         * @param labelDC DC分量标签
         * @param labelAmp 幅度标签
         * @param labelFre 频率标签
         */
        public void setLabel(String labelDC, String labelAmp, String labelFre) {
            this.labelA = labelDC;                                      // 设置DC标签
            this.labelV = labelAmp;                                     // 设置幅度标签
            this.labelFre = labelFre;                                   // 设置频率标签
            draw();                                                     // 触发重绘
        }

        /**
         * 设置测量数据并重绘
         * @param dataDC DC分量数据
         * @param dataAmp 幅度数据
         * @param dataFre 频率数据
         */
        public void setData(String dataDC, String dataAmp, String dataFre) {
            this.dataA = dataDC;                                        // 设置DC数据
            this.dataV = dataAmp;                                       // 设置幅度数据
            this.dataFre = dataFre;                                     // 设置频率数据
            draw();                                                     // 触发重绘
        }
        //endregion


        private final Bitmap bmp;                                       // 离屏位图
        private Canvas mCanvas;                                         // 离屏画布
        private Paint p;                                                // 画笔
        private boolean isChangeBitmap = false;                         // 位图变更标记
        private ICanvasGL canvasGL;                                     // GL画布引用

        /**
         * 通知GL画布刷新纹理
         */
        public void onRefresh(){
            if(canvasGL != null){                                       // GL画布引用有效
                canvasGL.onRefreshTexture();                            // 刷新纹理
            }
        }
        private int showX = 30, showY = 55;                             // 显示位置

        /**
         * 构造函数：初始化位图、画笔和数学通道号
         * @param mathNumber 数学通道号
         */
        public FFTMeasure(int mathNumber) {
            bmp = Bitmap.createBitmap(150, 100, Bitmap.Config.ARGB_8888); // 创建150x100位图
            mCanvas = new Canvas(bmp);                                  // 绑定画布
            p = new Paint();                                            // 创建画笔
            p.setColor(App.get().getResources().getColor(R.color.color_Math) /*Color.RED*/); // 设置数学通道颜色
            p.setTextSize(20);                                          // 设置文本大小
            p.setAntiAlias(true);                                       // 开启抗锯齿
            switchWorkMode(WorkModeManage.getInstance().getmWorkMode()); // 切换到当前工作模式
            this.mathNumber = mathNumber;                               // 保存数学通道号
            draw();                                                     // 执行初始绘制
        }

        private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR); // 清除模式
        private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);     // 源模式

        /**
         * 内部绘制方法：清除旧内容，绘制数学通道号和测量数据
         */
        private void draw() {
            p.setXfermode(clearMode);                                   // 设置清除模式
            mCanvas.drawPaint(p);                                       // 清除位图
            p.setXfermode(srcMode);                                     // 恢复源模式

            int x = 0, y = 22;                                         // 起始坐标
            mCanvas.drawText("M" + this.mathNumber, x, y, p);          // 绘制数学通道号
            y += 22;                                                    // Y坐标下移
            mCanvas.drawText(labelA + ": " + dataA, x, y, p);          // 绘制DC分量数据
            y += 22;                                                    // Y坐标下移
            mCanvas.drawText(labelFre + ": " + dataFre, x, y, p);      // 绘制频率数据
            y += 22;                                                    // Y坐标下移
            mCanvas.drawText(labelV + ": " + dataV, x, y, p);          // 绘制幅度数据

            isChangeBitmap = true;                                      // 标记位图已变更
            onRefresh();                                                // 通知GL刷新
        }

        //region IworkMode
        /**
         * 切换工作模式，更新FFT显示位置
         * @param workMode 工作模式
         */
        @Override
        public void switchWorkMode(int workMode) {
            Point pp = GlobalVar.get().getMeasureFFTPosition(workMode); // 获取FFT位置
            if (pp == null) {                                           // 位置为空
                showX = 10000;                                          // X坐标设为屏幕外
                showY = -100;                                           // Y坐标设为屏幕外
                return;                                                 // 直接返回
            }
            showX = pp.x;                                               // 更新X坐标
            showY = pp.y;                                               // 更新Y坐标
        }
        //endregion

        //region IMeasure

        /**
         * 使用Canvas方式绘制（空实现）
         * @param canvas 目标画布
         */
        @Override
        public void draw(Canvas canvas) {

        }

        /**
         * 使用ICanvasGL方式绘制FFT测量结果
         * @param canvas GL画布
         */
        @Override
        public void draw(ICanvasGL canvas) {
            synchronized (bmp) {                                        // 同步锁定位图
                canvasGL = canvas;                                      // 保存GL画布引用
                if (visible) {                                          // 可见时
                    if (isChangeBitmap) canvas.invalidateTextureContent(bmp,null); // 位图变更时刷新纹理
                    canvas.drawBitmap(bmp, showX, showY);               // 绘制位图
                }
                isChangeBitmap = false;                                 // 重置变更标记
            }
        }

        //endregion


    }

    /**
     * 段测量类：在波形区域显示段测量结果（如"起始段/结束段"）
     *
     * ╔══════════════════════════════════════════════════════════════════════════════╗
     * ║                         SegmentMeasure                                    ║
     * ╠══════════════════════════════════════════════════════════════════════════════╣
     * ║ 显示段测量文本，格式为"段号/总段数"                                        ║
     * ╚══════════════════════════════════════════════════════════════════════════════╝
     */
    public class SegmentMeasure implements IMeasure, IWorkMode {
        private String text = "--/--";                                  // 段测量文本
        private boolean visible = false;                                // 是否可见

        /**
         * 获取段测量文本
         * @return 段测量文本
         */
        public String getText() {
            return text;                                                // 返回文本
        }

        /**
         * 设置段测量文本并重绘
         * @param text 段测量文本
         */
        public void setText(String text) {
            this.text = text;                                           // 更新文本
            draw();                                                     // 触发重绘
        }

        /**
         * 查询是否可见
         * @return true表示可见
         */
        public boolean isVisible() {
            return visible;                                             // 返回可见性
        }

        /**
         * 设置可见性并重绘
         * @param visible true表示可见
         */
        public void setVisible(boolean visible) {
            this.visible = visible;                                     // 更新可见性
            draw();                                                     // 触发重绘
        }

        private final Bitmap bmp;                                       // 离屏位图
        private Canvas mCanvas;                                         // 离屏画布
        private Paint p;                                                // 画笔
        private boolean isChangeBitmap = false;                         // 位图变更标记
        private ICanvasGL canvasGL;                                     // GL画布引用

        /**
         * 通知GL画布刷新纹理
         */
        public void onRefresh(){
            if(canvasGL != null){                                       // GL画布引用有效
                canvasGL.onRefreshTexture();                            // 刷新纹理
            }
        }
        private int showX = 30, showY = 5;                              // 显示位置

        /**
         * 构造函数：初始化位图、画笔并绘制
         */
        public SegmentMeasure() {
            bmp = Bitmap.createBitmap(150, 50, Bitmap.Config.ARGB_8888); // 创建150x50位图
            mCanvas = new Canvas(bmp);                                  // 绑定画布
            p = new Paint();                                            // 创建画笔
            p.setColor(App.get().getResources().getColor(R.color.color_Math) /*Color.RED*/); // 设置数学通道颜色
            p.setTextSize(20);                                          // 设置文本大小
            p.setAntiAlias(true);                                       // 开启抗锯齿
            draw();                                                     // 执行初始绘制
        }

        private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR); // 清除模式
        private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);     // 源模式

        /**
         * 内部绘制方法：清除旧内容，绘制段测量文本
         */
        private void draw() {
            p.setXfermode(clearMode);                                   // 设置清除模式
            mCanvas.drawPaint(p);                                       // 清除位图
            p.setXfermode(srcMode);                                     // 恢复源模式

            int x = 0, y = 18;                                         // 起始坐标
            mCanvas.drawText(text, x, y, p);                            // 绘制文本

            isChangeBitmap = true;                                      // 标记位图已变更
            onRefresh();                                                // 通知GL刷新
        }

        //region IworkMode
        /**
         * 切换工作模式，更新段测量显示位置
         * @param workMode 工作模式
         */
        @Override
        public void switchWorkMode(int workMode) {
            Point pp = GlobalVar.get().getMeasureSegmentPosition(workMode); // 获取段测量位置
            if (pp == null) {                                           // 位置为空
                showX = 10000;                                          // X坐标设为屏幕外
                showY = -100;                                           // Y坐标设为屏幕外
                return;                                                 // 直接返回
            }
            showX = pp.x;                                               // 更新X坐标
            showY = pp.y;                                               // 更新Y坐标
        }
        //endregion

        //region IMeasure

        /**
         * 使用Canvas方式绘制（空实现）
         * @param canvas 目标画布
         */
        @Override
        public void draw(Canvas canvas) {

        }

        /**
         * 使用ICanvasGL方式绘制段测量结果
         * @param canvas GL画布
         */
        @Override
        public void draw(ICanvasGL canvas) {
            synchronized (bmp) {                                        // 同步锁定位图
                canvasGL = canvas;                                      // 保存GL画布引用
                if (visible) {                                          // 可见时

                    if (isChangeBitmap) canvas.invalidateTextureContent(bmp,null); // 位图变更时刷新纹理
                    canvas.drawBitmap(bmp, showX, showY);               // 绘制位图
                }
                isChangeBitmap = false;                                 // 重置变更标记
            }
        }
    }

    /**
     * 判断光标是否处于TValue跟踪模式
     * @return true表示TValue跟踪模式已启用
     */
    public boolean isCursorTValueTrace() {
        boolean isTValueTrace = false;                                  // 默认未启用
        int X1 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X1); // 获取TValue光标X1位置
        int X2 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X2); // 获取TValue光标X2位置
//        Log.d("asdf","X1:" + X1 + ",X2:" +X2);
        if (X1 >= 0 || X2 >= 0) {                                      // 任一光标位置有效
            isTValueTrace = true;                                       // 启用TValue跟踪
        }
        return isTValueTrace;                                           // 返回TValue跟踪状态
    }

}
