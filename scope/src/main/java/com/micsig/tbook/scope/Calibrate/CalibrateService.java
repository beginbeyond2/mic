package com.micsig.tbook.scope.Calibrate;  // 定义包名：示波器校准功能模块

import android.os.Build;  // 导入Build类：获取设备产品信息
import android.os.Handler;  // 导入Handler类：线程间消息处理
import android.os.HandlerThread;  // 导入HandlerThread类：带消息队列的后台线程
import android.os.Message;  // 导入Message类：消息封装
import android.os.SystemClock;  // 导入SystemClock类：系统时钟

import com.micsig.tbook.hardware.HardwareProduct;  // 导入HardwareProduct类：硬件产品型号常量
import com.micsig.tbook.scope.Calibrate.MHO38v1.*;  // 导入MHO38v1校准实现类
import com.micsig.tbook.scope.Calibrate.MHO68v1.*;  // 导入MHO68v1校准实现类
import com.micsig.tbook.scope.Calibrate.MHO68v2.*;  // 导入MHO68v2校准实现类
import com.micsig.tbook.scope.Event.EventBase;  // 导入EventBase类：事件基类
import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂
import com.micsig.tbook.scope.ScopeMessage;  // 导入ScopeMessage类：示波器消息管理

import java.util.Observable;  // 导入Observable类：被观察者基类
import java.util.Observer;  // 导入Observer类：观察者接口

/**
 * 校准服务管理器 - 校准系统核心服务类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate（示波器校准功能模块）</li>
 *   <li>架构层级：服务层 - 校准任务调度与执行</li>
 *   <li>设计模式：单例模式 + 观察者模式 + 工厂模式</li>
 *   <li>职责类型：校准任务调度、校准实例管理、FPGA状态监控</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>管理所有校准类型的实例（零点/增益/电容等）</li>
 *   <li>在后台线程执行校准任务</li>
 *   <li>监听波形更新事件，触发校准执行</li>
 *   <li>FPGA状态监控与超时检测</li>
 *   <li>根据产品型号创建对应的校准实例</li>
 * </ul>
 * 
 * <p><b>校准类型架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   校准类型枚举                                                            │
 * │                                                                          │
 * │   MIN_CALIBRATE (0)                                                      │
 * │       │                                                                  │
 * │       ├── ZERO_CALIBRATE (0)      零点校准                               │
 * │       │       └── 校准通道零点偏移                                        │
 * │       │                                                                  │
 * │       ├── AD_OFFSET_CALIBRATE (1) AD偏移校准                             │
 * │       │       └── 校准ADC偏移量                                          │
 * │       │                                                                  │
 * │       ├── CHCOEF_CALIBRATE (2)    通道系数校准                           │
 * │       │       └── 校准通道增益系数                                        │
 * │       │                                                                  │
 * │       ├── CHGAIN_CALIBRATE (3)    通道增益校准                           │
 * │       │       └── 校准通道PGA增益                                         │
 * │       │                                                                  │
 * │       ├── CHCAP_CALIBRATE (4)     电容校准                               │
 * │       │       └── 校准通道电容值                                          │
 * │       │                                                                  │
 * │       ├── CH_ZERO_CALIBRATE (5)   通道零点校准                           │
 * │       │       └── 校准通道大道零点                                        │
 * │       │                                                                  │
 * │       ├── PROBE_DA_CALIBRATE (6)  探头DA校准                             │
 * │       │       └── 校准探头DAC输出                                         │
 * │       │                                                                  │
 * │       ├── CHGAIN_CALIBRATE_EX (7) 扩展通道增益校准                       │
 * │       │       └── 扩展增益校准功能                                        │
 * │       │                                                                  │
 * │       └── MAX_CALIBRATE (8)        校准类型上限                          │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>产品型号适配：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   产品型号 → 校准实例映射                                                 │
 * │                                                                          │
 * │   RK3588_MHO28_V1 / RK3588_MHO38_V1:                                     │
 * │   ├── MHO38v1_ZeroCalibrateEx      (零点校准)                            │
 * │   ├── MHO38v1_ChCofitCalibrateEx   (系数校准)                            │
 * │   ├── MHO38v1_ChGainCalibrate      (增益校准)                            │
 * │   ├── MHO38v1_ChCapCalibrate       (电容校准)                            │
 * │   └── MHO38v1_ZeroCalibrate        (通道零点)                            │
 * │                                                                          │
 * │   RK3588_MHO68_V1:                                                       │
 * │   ├── MHO68v1_ZeroCalibrateEx      (零点校准)                            │
 * │   ├── MHO68v1_ADOffsetCalibrate    (AD偏移)                              │
 * │   ├── MHO68v1_ChCofitCalibrateEx   (系数校准)                            │
 * │   ├── MHO68v1_ChGainCalibrate      (增益校准)                            │
 * │   ├── MHO68v1_ChCapCalibrate       (电容校准)                            │
 * │   ├── MHO68v1_ZeroCalibrate        (通道零点)                            │
 * │   ├── MHO68v1_ProbeDACalibrate     (探头DA)                              │
 * │   └── MHO68v1_ChGainCalibrateEx    (扩展增益)                            │
 * │                                                                          │
 * │   RK3588_MHO68_V2 (默认):                                                │
 * │   ├── MHO68v2_ZeroCalibrateEx      (零点校准)                            │
 * │   ├── MHO68v2_ChCofitCalibrateEx   (系数校准)                            │
 * │   ├── MHO68v2_ChGainCalibrate      (增益校准)                            │
 * │   ├── MHO68v2_ChCapCalibrate       (电容校准)                            │
 * │   ├── MHO68v2_ZeroCalibrate        (通道零点)                            │
 * │   └── MHO68v2_ChGainCalibrateEx    (扩展增益)                            │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>消息处理流程：</b>
 * <pre>
 *   波形更新事件 (EVENT_CH_WAVE_UPDATE)
 *         │
 *         ▼
 *   CalibrateService.update() ──────► calibrate.begin()
 *         │                                │
 *         ▼                                ▼
 *   发送 CALIBRATE_MSG ──────────► CALIBRATE_INIT
 *         │                                │
 *         ▼                                ▼
 *   handleMessage() ──────────────► calibrate.iniCalibrate()
 *         │
 *         ▼
 *   calibrate.onCalibrate()
 *         │
 *         ▼
 *   EVENT_CALIBRATE_ITEM_FINISH
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：Calibrate（校准基类，定义校准接口）</li>
 *   <li>依赖：EventFactory（事件工厂，事件通知机制）</li>
 *   <li>依赖：HardwareProduct（硬件产品型号）</li>
 *   <li>依赖：ScopeMessage（示波器消息管理）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @see Calibrate 校准基类
 * @see FactorCalibrate 工厂校准管理器
 * @see SelfCalibrate 自校准管理器
 */
public class CalibrateService implements Observer {  // 实现Observer接口，监听波形更新事件

    /** 日志标签 */
    private static final String TAG = "CalibrateService";  // 日志输出标签
    
    /** 单例实例：使用volatile保证多线程可见性 */
    private static volatile CalibrateService instance = null;  // 单例实例引用
    
    /**
     * 获取单例实例
     * 
     * <p>使用双重检查锁定（Double-Checked Locking）模式确保线程安全。
     * 
     * @return CalibrateService单例实例
     */
    public static CalibrateService getInstance() {
        if (instance == null) {  // 第一次检查：避免不必要的同步
            synchronized (CalibrateService.class) {  // 同步锁
                if (instance == null ) {  // 第二次检查：确保只创建一个实例
                    instance = new CalibrateService();  // 创建单例实例
                }
            }
        }
        return instance;  // 返回单例实例
    }

    // ==================== 消息类型常量 ====================
    
    /** 校准执行消息：执行校准算法 */
    public static final int CALIBRATE_MSG = 0x1001;  // 校准执行消息类型
    
    /** 工厂校准开始消息：通知工厂校准流程开始 */
    public static final int CALIBRATE_BEGIN_FACTOR = 0x1002;  // 工厂校准开始
    
    /** 自校准开始消息：通知自校准流程开始 */
    public static final int CALIBRATE_BEGIN_SELF = 0x1003;  // 自校准开始
    
    /** 校准项开始消息：通知单个校准项开始 */
    public static final int CALIBRATE_ITEM_BEGIN = 0x1004;  // 校准项开始
    
    /** 校准初始化消息：初始化校准环境 */
    public static final int CALIBRATE_INIT = 0x1005;  // 校准初始化
    
    /** FPGA状态检查消息：定时检查FPGA状态 */
    private static final int CALIBRATE_FPGA_STATUS = 0x1006;  // FPGA状态检查
    
    /** 探头校准开始消息：通知探头校准流程开始 */
    public static final int CALIBRATE_BEGIN_PROBE = 0x1007;  // 探头校准开始

    // ==================== 校准类型常量 ====================
    
    /** 校准类型最小值 */
    public static final int MIN_CALIBRATE = 0;  // 校准类型枚举起始值
    
    /** 零点校准：校准通道零点偏移 */
    public static final int ZERO_CALIBRATE = MIN_CALIBRATE;  // 零点校准类型ID
    
    /** AD偏移校准：校准ADC偏移量 */
    public static final int AD_OFFSET_CALIBRATE = ZERO_CALIBRATE + 1;  // AD偏移校准类型ID
    
    /** 通道系数校准：校准通道增益系数 */
    public static final int CHCOEF_CALIBRATE = AD_OFFSET_CALIBRATE + 1;  // 通道系数校准类型ID
    
    /** 通道增益校准：校准通道PGA增益 */
    public static final int CHGAIN_CALIBRATE = CHCOEF_CALIBRATE + 1;  // 通道增益校准类型ID
    
    /** 电容校准：校准通道电容值 */
    public static final int CHCAP_CALIBRATE = CHGAIN_CALIBRATE + 1;  // 电容校准类型ID
    
    /** 通道零点校准：校准通道大道零点 */
    public static final int CH_ZERO_CALIBRATE = CHCAP_CALIBRATE + 1;  // 通道零点校准类型ID
    
    /** 探头DA校准：校准探头DAC输出 */
    public static final int PROBE_DA_CALIBRATE = CH_ZERO_CALIBRATE + 1;  // 探头DA校准类型ID
    
    /** 扩展通道增益校准：扩展增益校准功能 */
    public static final int CHGAIN_CALIBRATE_EX = PROBE_DA_CALIBRATE + 1;  // 扩展增益校准类型ID
    
    /** 校准类型最大值 */
    public static final int MAX_CALIBRATE = CHGAIN_CALIBRATE_EX + 1;  // 校准类型枚举上限

    // ==================== 成员变量 ====================
    
    /** 校准实例数组：存储所有校准类型的实例 */
    private Calibrate [] calibrates = new Calibrate[MAX_CALIBRATE];  // 校准实例数组
    
    /** 后台处理线程：用于执行校准任务 */
    private HandlerThread mHandlerThread;  // Handler线程
    
    /** 消息处理器：处理校准相关消息 */
    private Handler mHandler;  // Handler消息处理器
    
    /** 当前校准类型：记录正在执行的校准类型ID */
    private int calibrateType = -1;  // 当前校准类型，-1表示无校准

    /**
     * 私有构造方法：初始化校准服务
     * 
     * <p>创建后台线程，初始化校准实例。
     */
    private CalibrateService() {

        initCalibrate();  // 根据产品型号初始化校准实例
        mHandlerThread = new HandlerThread(TAG);  // 创建后台处理线程
        mHandlerThread.start();  // 启动线程
        mHandler = new Handler(mHandlerThread.getLooper()){  // 创建消息处理器
            @Override
            public void handleMessage(Message msg) {  // 消息处理回调
                CalibrateService.this.handleMessage(msg);  // 转发到类方法处理
            }
        };
    }

    /**
     * 初始化校准实例
     * 
     * <p>根据产品型号创建对应的校准实例。
     * 不同型号的示波器可能支持不同的校准项目。
     */
    private void initCalibrate() {
        switch (Build.PRODUCT){  // 根据产品型号选择校准实例
            case HardwareProduct.RK3588_MHO28_V1:  // MHO28 V1型号
            case HardwareProduct.RK3588_MHO38_V1:  // MHO38 V1型号
                calibrates[ZERO_CALIBRATE] = new MHO38v1_ZeroCalibrateEx(ZERO_CALIBRATE);  // 零点校准
                calibrates[CHCOEF_CALIBRATE] = new MHO38v1_ChCofitCalibrateEx(CHCOEF_CALIBRATE);  // 系数校准
                calibrates[CHGAIN_CALIBRATE] = new MHO38v1_ChGainCalibrate(CHGAIN_CALIBRATE);  // 增益校准
                calibrates[CHCAP_CALIBRATE] = new MHO38v1_ChCapCalibrate(CHCAP_CALIBRATE);  // 电容校准
                calibrates[CH_ZERO_CALIBRATE] = new MHO38v1_ZeroCalibrate(CH_ZERO_CALIBRATE);  // 通道零点
                break;  // 结束case
            case HardwareProduct.RK3588_MHO68_V1:  // MHO68 V1型号
                calibrates[ZERO_CALIBRATE] = new MHO68v1_ZeroCalibrateEx(ZERO_CALIBRATE);  // 零点校准
                calibrates[AD_OFFSET_CALIBRATE] = new MHO68v1_ADOffsetCalibrate(AD_OFFSET_CALIBRATE);  // AD偏移
                calibrates[CHCOEF_CALIBRATE] = new MHO68v1_ChCofitCalibrateEx(CHCOEF_CALIBRATE);  // 系数校准
                calibrates[CHGAIN_CALIBRATE] = new MHO68v1_ChGainCalibrate(CHGAIN_CALIBRATE);  // 增益校准
                calibrates[CHCAP_CALIBRATE] = new MHO68v1_ChCapCalibrate(CHCAP_CALIBRATE);  // 电容校准
                calibrates[CH_ZERO_CALIBRATE] = new MHO68v1_ZeroCalibrate(CH_ZERO_CALIBRATE);  // 通道零点
                calibrates[PROBE_DA_CALIBRATE] = new MHO68v1_ProbeDACalibrate(PROBE_DA_CALIBRATE);  // 探头DA
                calibrates[CHGAIN_CALIBRATE_EX] = new MHO68v1_ChGainCalibrateEx(CHGAIN_CALIBRATE_EX);  // 扩展增益
                break;  // 结束case
            default:  // 默认情况
            case HardwareProduct.RK3588_MHO68_V2:  // MHO68 V2型号（默认）
                calibrates[ZERO_CALIBRATE] = new MHO68v2_ZeroCalibrateEx(ZERO_CALIBRATE);  // 零点校准
                calibrates[CHCOEF_CALIBRATE] = new MHO68v2_ChCofitCalibrateEx(CHCOEF_CALIBRATE);  // 系数校准
                calibrates[CHGAIN_CALIBRATE] = new MHO68v2_ChGainCalibrate(CHGAIN_CALIBRATE);  // 增益校准
                calibrates[CHCAP_CALIBRATE] = new MHO68v2_ChCapCalibrate(CHCAP_CALIBRATE);  // 电容校准
                calibrates[CH_ZERO_CALIBRATE] = new MHO68v2_ZeroCalibrate(CH_ZERO_CALIBRATE);  // 通道零点
                calibrates[CHGAIN_CALIBRATE_EX] = new MHO68v2_ChGainCalibrateEx(CHGAIN_CALIBRATE_EX);  // 扩展增益
                break;  // 结束case
        }

    }

    /**
     * 消息处理方法
     * 
     * <p>处理各种校准相关的消息。
     * 
     * @param msg 待处理的消息
     */
    private void handleMessage(Message msg){
        switch (msg.what){  // 根据消息类型分发处理
            case CALIBRATE_MSG:  // 校准执行消息
            {
                Calibrate calibrate = (Calibrate)msg.obj;  // 获取校准对象
                if(calibrate != null){  // 检查校准对象是否有效
                    if(calibrate.onCalibrate()) {  // 执行校准算法
                        calibrate.setFinished();  // 设置校准完成标志
                        calibrate.endCalibrate();  // 结束校准
                        EventFactory.sendEvent(EventFactory.EVENT_CALIBRATE_ITEM_FINISH);  // 发送校准完成事件
                    }
                    calibrate.end();  // 清理校准资源
                    waveUpdateTimestamp = SystemClock.elapsedRealtime();  // 更新时间戳
                    fpgaStatus();  // 启动FPGA状态监控
                }
                break;  // 结束case
            }
            case CALIBRATE_BEGIN_FACTOR: {  // 工厂校准开始消息
                EventFactory.sendEvent(EventFactory.EVENT_FACTOR_CALIBRATE_BEGIN);  // 发送工厂校准开始事件
                break;  // 结束case
            }
            case CALIBRATE_BEGIN_SELF: {  // 自校准开始消息
                EventFactory.sendEvent(EventFactory.EVENT_SELF_CALIBRATE_BEGIN);  // 发送自校准开始事件
                break;  // 结束case
            }
            case CALIBRATE_ITEM_BEGIN: {  // 校准项开始消息
                EventFactory.sendEvent((EventBase) msg.obj);  // 发送校准项开始事件
                break;  // 结束case
            }
            case CALIBRATE_BEGIN_PROBE:{  // 探头校准开始消息
                EventFactory.sendEvent(EventFactory.EVENT_PROBE_CALIBRATE_BEGIN);  // 发送探头校准开始事件
            }
            break;  // 结束case
            case CALIBRATE_INIT:{  // 校准初始化消息
                Calibrate calibrate = (Calibrate)msg.obj;  // 获取校准对象
                if(calibrate != null){  // 检查校准对象是否有效
                    calibrate.iniCalibrate();  // 初始化校准环境
                }
                waveUpdateTimestamp = SystemClock.elapsedRealtime();  // 更新时间戳
                fpgaStatus();  // 启动FPGA状态监控
                break;  // 结束case
            }
            case CALIBRATE_FPGA_STATUS:{  // FPGA状态检查消息
                if(bCalibrate) {  // 检查是否正在校准
                    if ((SystemClock.elapsedRealtime() - waveUpdateTimestamp) > 30 * 1000) {  // 检查是否超时30秒
                        Calibrate calibrate = getCalibrate();  // 获取当前校准对象
                        calibrate.setErrcode(1001);  // 设置错误码1001（超时）
                        calibrate.setFinished();  // 设置校准完成标志
                        calibrate.endCalibrate();  // 结束校准

                        EventFactory.sendEvent(EventFactory.EVENT_CALIBRATE_ITEM_FINISH);  // 发送校准完成事件
                    }
                    if ((SystemClock.elapsedRealtime() - waveUpdateTimestamp) > 2 * 1000) {  // 检查是否超过2秒
                        getCalibrate().checkParam();  // 检查校准参数
                    }
                    ScopeMessage.getInstance().sendFpgaId();  // 发送FPGA ID请求
                    fpgaStatus();  // 继续FPGA状态监控
                }
                break;  // 结束case
            }
        }
    }

    /**
     * 启动FPGA状态监控
     * 
     * <p>定时检查FPGA状态，检测校准超时。
     */
    private void fpgaStatus(){
        if(bCalibrate){  // 检查是否正在校准
            if(mHandler.hasMessages(CALIBRATE_FPGA_STATUS)){  // 检查是否有待处理的监控消息
                mHandler.removeMessages(CALIBRATE_FPGA_STATUS);  // 移除旧消息
            }
            mHandler.sendEmptyMessageDelayed(CALIBRATE_FPGA_STATUS,3*1000);  // 3秒后发送监控消息
        }
    }

    /** 校准进行中标志：使用volatile保证多线程可见性 */
    private volatile boolean bCalibrate = false;  // 校准进行中标志

    /**
     * 启动校准
     * 
     * <p>启动指定类型的校准任务，注册波形更新事件观察者。
     * 
     * @param calibrateType 校准类型ID
     */
    public void start(int calibrateType){
        synchronized (this) {  // 同步锁
            if (isCalibrateTypeValid(calibrateType)) {  // 验证校准类型是否有效
                bCalibrate = true;  // 设置校准进行中标志
                this.calibrateType = calibrateType;  // 设置当前校准类型
                //calibrates[calibrateType].iniCalibrate();//校准初始化（已注释）
                Message msg = mHandler.obtainMessage();  // 获取消息对象
                msg.what = CALIBRATE_INIT;  // 设置消息类型为初始化
                msg.obj = getCalibrate();  // 设置校准对象
                mHandler.sendMessage(msg);  // 发送消息
                EventFactory.addEventObserver(EventFactory.EVENT_CH_WAVE_UPDATE,this);  // 注册波形更新事件观察者

            }
        }
    }

    /**
     * 检查是否正在校准
     * 
     * @return true表示正在校准，false表示未在校准
     */
    public boolean isCalibrate(){
        return bCalibrate;  // 返回校准进行中标志
    }

    /**
     * 结束校准
     * 
     * <p>清理校准状态，移除事件观察者。
     */
    public void end(){
        synchronized (this) {  // 同步锁
            bCalibrate = false;  // 清除校准进行中标志
            EventFactory.delEventObserver(EventFactory.EVENT_CH_WAVE_UPDATE, this);  // 移除波形更新事件观察者
            calibrates[calibrateType].endCalibrate();  // 结束校准
//            this.calibrateType = -1;  // 重置校准类型（已注释）

        }
    }

    /**
     * 获取当前校准对象
     * 
     * @return 当前校准对象
     */
    public Calibrate getCalibrate(){
        return getCalibrate(calibrateType);  // 返回当前类型的校准对象
    }

    /**
     * 获取当前校准类型
     * 
     * @return 当前校准类型ID
     */
    public int getCalibrateType(){
        return calibrateType;  // 返回当前校准类型
    }

    /**
     * 获取指定类型的校准对象
     * 
     * @param idx 校准类型ID
     * @return 校准对象，无效ID返回null
     */
    public Calibrate getCalibrate(int idx){
        Calibrate calibrate = null;  // 初始化返回值
        synchronized (this) {  // 同步锁
            if (isCalibrateTypeValid(idx)) {  // 验证校准类型是否有效
                calibrate = calibrates[idx];  // 获取校准对象
            }
        }
        return calibrate;  // 返回校准对象
    }





    /**
     * 观察者更新方法
     * 
     * <p>接收波形更新事件，触发校准执行。
     * 当波形数据更新时，检查是否满足校准条件并启动校准。
     * 
     * @param observable 被观察者对象
     * @param data 事件数据
     */
    @Override
    public void update(Observable observable, Object data) {
        EventBase eventBase = (EventBase)data;  // 转换为事件基类
        if(eventBase.getId() == EventFactory.EVENT_CH_WAVE_UPDATE){  // 检查是否为波形更新事件
            //Log.i(TAG, "receive enent: EVENT_CH_WAVE_UPDATE");  // 日志输出（已注释）
            Calibrate calibrate = getCalibrate();  // 获取当前校准对象
            if(calibrate != null && !calibrate.isCalibrate()){  // 检查校准对象有效且未在校准中

                if(!mHandler.hasMessages(CALIBRATE_MSG)) {  // 检查是否有待处理的校准消息
                    if(calibrate.begin()) {  // 开始校准准备
                        Message msg = mHandler.obtainMessage();  // 获取消息对象
                        msg.what = CALIBRATE_MSG;  // 设置消息类型为校准执行
                        msg.obj = calibrate;  // 设置校准对象
                        mHandler.sendMessage(msg);  // 发送消息
                    }else{  // 校准准备失败
                        calibrate.end();  // 结束校准
                    }
                }
                waveUpdateTimestamp = SystemClock.elapsedRealtime();  // 更新波形更新时间戳
            }
        }

    }

    /** 波形更新时间戳：用于超时检测 */
    private long waveUpdateTimestamp = 0;  // 波形更新时间戳

    /**
     * 发送空消息
     * 
     * @param what 消息类型
     */
    public void sendMsg(int what) {
        //Message msg = mHandler.obtainMessage();  // 获取消息对象（已注释）
        //msg.what = CALIBRATE_BEGIN;  // 设置消息类型（已注释）
        mHandler.sendEmptyMessage(what);  // 发送空消息
    }

    /**
     * 发送消息
     * 
     * @param message 消息对象
     */
    public void sendMsg(Message message) {
        mHandler.sendMessage(message);  // 发送消息
    }

    /**
     * 验证校准类型是否有效
     * 
     * @param calibrateType 校准类型ID
     * @return true表示有效，false表示无效
     */
    public static boolean isCalibrateTypeValid(int calibrateType){
        return (calibrateType>=MIN_CALIBRATE && calibrateType<MAX_CALIBRATE);  // 检查是否在有效范围内
    }
}
