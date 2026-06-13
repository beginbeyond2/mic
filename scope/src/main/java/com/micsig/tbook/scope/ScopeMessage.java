package com.micsig.tbook.scope;  // 定义包名：示波器核心模块

import android.os.Handler;  // 导入Handler类：用于Android消息处理机制
import android.os.HandlerThread;  // 导入HandlerThread类：创建带消息队列的工作线程
import android.os.Message;  // 导入Message类：消息载体，用于线程间通信
import android.os.SystemClock;  // 导入SystemClock类：获取系统运行时间
import android.util.Log;  // 导入Log类：Android日志输出工具

import com.micsig.tbook.hardware.Hardware;  // 导入Hardware类：硬件管理，提供温度等硬件信息
import com.micsig.tbook.hardware.HardwareProduct;  // 导入HardwareProduct类：硬件产品信息判断
import com.micsig.tbook.scope.Action.FPGAMessage;  // 导入FPGAMessage类：FPGA命令消息处理
import com.micsig.tbook.scope.Action.HardwareMessage;  // 导入HardwareMessage类：硬件消息处理
import com.micsig.tbook.scope.Action.UiMessage;  // 导入UiMessage类：UI消息处理
import com.micsig.tbook.scope.Data.SyncHeader;  // 导入SyncHeader类：同步头管理，标识波形数据
import com.micsig.tbook.scope.Display.Display;  // 导入Display类：显示管理，XY模式等
import com.micsig.tbook.scope.Event.EventBase;  // 导入EventBase类：事件基类
import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂，发送事件通知
import com.micsig.tbook.scope.Sample.Sample;  // 导入Sample类：采样状态管理
import com.micsig.tbook.scope.Sample.SegmentSample;  // 导入SegmentSample类：段采样管理
import com.micsig.tbook.scope.fpga.FPGABoot;  // 导入FPGABoot类：FPGA固件加载管理
import com.micsig.tbook.scope.fpga.FPGA_Status;  // 导入FPGA_Status类：FPGA状态数据结构
import com.micsig.tbook.scope.surface.DeviceFactory;  // 导入DeviceFactory类：设备工厂
import com.micsig.tbook.scope.surface.HwDevice;  // 导入HwDevice类：硬件设备抽象

/**
 * 示波器消息处理中心
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope（示波器核心模块）</li>
 *   <li>架构层级：通信调度层 - 消息处理</li>
 *   <li>设计模式：单例模式 + 生产者-消费者模式</li>
 *   <li>职责类型：消息队列管理与FPGA通信调度</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>管理FPGA命令消息队列，统一调度FPGA通信</li>
 *   <li>管理硬件操作消息队列，处理硬件相关操作</li>
 *   <li>管理UI更新消息队列，协调UI刷新时机</li>
 *   <li>协调FPGA加载和初始化流程</li>
 *   <li>处理探头事件和定时检查</li>
 *   <li>监控系统温度和风扇状态</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>将所有FPGA命令集中到一个线程处理，避免多线程竞争</li>
 *   <li>通过消息队列实现命令的异步执行和延迟执行</li>
 *   <li>确保FPGA操作的顺序性和原子性</li>
 *   <li>提供统一的命令发送接口，简化上层调用</li>
 * </ul>
 * 
 * <p><b>消息处理架构：</b>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                         ScopeMessage（消息中心）                         │
 * │                                                                         │
 * │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐             │
 * │  │ FPGAMessage  │    │HardwareMessage│    │  UiMessage   │             │
 * │  │  FPGA命令队列 │    │  硬件命令队列  │    │  UI消息队列   │             │
 * │  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘             │
 * │         │                   │                   │                      │
 * │         └───────────────────┼───────────────────┘                      │
 * │                             ▼                                          │
 * │                    ┌────────────────┐                                  │
 * │                    │  HandlerThread │                                  │
 * │                    │   (工作线程)    │                                  │
 * │                    └───────┬────────┘                                  │
 * │                            ▼                                           │
 * │                    ┌────────────────┐                                  │
 * │                    │    Handler     │                                  │
 * │                    │  (消息处理器)   │                                  │
 * │                    └────────────────┘                                  │
 * └─────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/13
 */
public class ScopeMessage {

    // ==================== 消息类型常量定义 ====================
    
    private static final String TAG = "ScopeMessageThread";  // 日志标签：用于日志输出标识
    
    // 消息类型常量：每种消息类型对应一个唯一值，用于消息分发
    private static final int MSG_FPGA = 0x1001;  // FPGA命令消息：将命令添加到FPGA命令队列
    private static final int MSG_HARDWARE = MSG_FPGA + 1;  // 硬件操作消息：将命令添加到硬件命令队列
    private static final int MSG_FPGA_RUN = MSG_HARDWARE + 1;  // 执行FPGA命令：执行命令队列中的命令
    private static final int MSG_FPGA_BOOT = MSG_FPGA_RUN + 1;  // FPGA加载：加载FPGA固件
    private static final int MSG_SCOPE_RESUME = MSG_FPGA_BOOT + 1;  // 示波器恢复：恢复命令发送
    private static final int MSG_SCOPE_PAUSE = MSG_SCOPE_RESUME + 1;  // 示波器暂停：暂停命令发送

    private static final int MSG_TOUCH_RESUME = MSG_SCOPE_PAUSE + 1;  // 触摸恢复：恢复触摸功能
    private static final int MSG_TOUCH_PAUSE = MSG_TOUCH_RESUME + 1;  // 触摸暂停：暂停触摸功能
    private static final int MSG_READ_FPGA_ID = MSG_TOUCH_PAUSE + 1;  // 读取FPGA ID：验证FPGA是否正常
    private static final int MSG_READ_FPGA_STATUS  = MSG_READ_FPGA_ID + 1;  // 读取FPGA状态：获取FPGA运行状态
    private static final int MSG_CH_SAMPLE_CHANGE = MSG_READ_FPGA_STATUS + 1;  // 通道采样变化：通道采样状态改变
    private static final int MSG_SAMPLE_STATE = MSG_CH_SAMPLE_CHANGE + 1;  // 采样状态：采样状态变化
    private static final int MSG_UI = MSG_SAMPLE_STATE + 1;  // UI消息：UI更新消息
    private static final int MSG_UI_RUN = MSG_UI + 1;  // 执行UI消息：执行UI消息队列
    private static final int MSG_FORCE_TRIGGER = MSG_UI_RUN + 1;  // 强制触发：强制触发一次采集
    private static final int MSG_SEGMENT_TS = MSG_FORCE_TRIGGER + 1;  // 段采样时间戳：获取段采样时间戳
    private static final int MSG_SEGMENT_FRAMES = MSG_SEGMENT_TS + 1;  // 段采样帧数：获取段采样帧数
    private static final int MSG_50O_STATUS = MSG_SEGMENT_FRAMES + 1;  // 50Ω状态：查询阻抗和温度
    private static final int MSG_AD_CHECK = MSG_50O_STATUS + 1;  // AD校准检查：检查AD校准状态
    private static final int MSG_CMD_SEGMENT = MSG_AD_CHECK + 1;  // 段采样命令：发送段采样命令

    private static final int MSG_AD_RESET = MSG_CMD_SEGMENT + 1;  // AD重置：重置AD校准
    private static final int MSG_PROBE_EVENT = MSG_AD_RESET + 1;  // 探头事件：处理探头事件
    private static final int MSG_SEND_PROBE = MSG_PROBE_EVENT + 1;  // 发送探头命令：向探头发送命令
    private static final int MSG_CLEAR_WAVE = MSG_SEND_PROBE + 1;  // 清除波形：清除显示的波形
    private static final int MAG_AD_JZ = MSG_CLEAR_WAVE + 1;  // AD校准：执行AD校准操作
    private static final int MSG_PROBE_TIMER = MAG_AD_JZ + 1;  // 探头定时器：定时检查探头状态
    private static final int MSG_FPGA_CLOCK_INOUT_STATUS = MSG_PROBE_TIMER + 1;  // FPGA时钟状态：查询时钟输入输出状态
    private static final int MSG_FPGA_STATUS = MSG_FPGA_CLOCK_INOUT_STATUS + 1;  // FPGA物理状态：查询FPGA物理状态

    // ==================== 延迟时间常量 ====================
    
    private static final int MSG_DELAY_MS = 20;  // FPGA命令延迟时间：20ms，用于合并命令减少同步次数
    private static final int MSG_UI_DELAY_MS = 150;  // UI消息延迟时间：150ms，用于合并UI更新优化性能

    // ==================== 成员变量 ====================
    
    private HandlerThread mHandlerThread;  // 消息处理线程：独立的工作线程处理所有消息
    private Handler mHandler;  // 消息处理器：绑定到HandlerThread的Looper
    private FPGAMessage fpgaMessage;  // FPGA命令处理对象：管理FPGA命令队列
    private UiMessage uiMessage;  // UI消息处理对象：管理UI更新消息队列
    private HardwareMessage hwMessage;  // 硬件消息处理对象：管理硬件操作消息队列
    private volatile int syncHeader = 0;  // 当前同步头：标识当前处理的波形数据，volatile保证可见性
    private volatile boolean bMessageEmpty = true;  // 消息队列空标志：true表示队列已处理完毕

    private static final int FPGA_ID = 0x46504741;  // FPGA ID标识：固定值"FPGA"的ASCII码，用于验证FPGA

    private int sysTemperature = 0;  // 系统温度缓存：记录上次温度，用于检测温度变化触发AD校准
    private boolean bProbeResume = false;  // 探头恢复标志：FPGA加载完成后需要检测探头


    /**
     * 私有构造函数 - 初始化消息处理中心
     * 
     * <p>执行流程：
     * 1. 创建命令处理对象（FPGA/硬件/UI）
     * 2. 创建并启动消息处理线程
     * 3. 创建消息处理器并定义消息处理逻辑
     * 4. 启动探头定时检查
     */
    private ScopeMessage(){
        fpgaMessage = new FPGAMessage();  // 创建FPGA命令处理对象
        hwMessage = new HardwareMessage();  // 创建硬件消息处理对象
        uiMessage = new UiMessage();  // 创建UI消息处理对象
        mHandlerThread = new HandlerThread(TAG);  // 创建消息处理线程
        mHandlerThread.start();  // 启动消息处理线程
        mHandler = new Handler(mHandlerThread.getLooper()){  // 创建消息处理器，绑定到工作线程的Looper
            @Override
            public void handleMessage(Message msg) {  // 重写消息处理方法

                FPGABoot fpgaBoot = FPGABoot.getInstance();  // 获取FPGA加载管理单例
                Scope scope = Scope.getInstance();  // 获取示波器核心管理单例
                Sample sample = Sample.getInstance();  // 获取采样管理单例
                Display display = Display.getInstance();  // 获取显示管理单例
                switch (msg.what){  // 根据消息类型分发处理
                    case MSG_SAMPLE_STATE:  // 处理采样状态变化消息
                        // 检查特殊模式：XY模式、单次、自动、串口文本、零通道使能、校准模式
                        if(display.isXYMode()  // XY模式：双通道XY显示
                                || sample.isSingle()  // 单次模式：只采集一次
                                || scope.isAuto()  // 自动模式：自动设置
                                || scope.isSerialText()  // 串口文本模式
                                || scope.isEnableChannelZero()  // 零通道使能
                                ||fpgaMessage.isCalibrate()){  // 校准模式

                            if(msg.arg1 >= Sample.SAMPLE_RUN){  // 如果状态>=运行状态
                                msg.arg1 = Sample.SAMPLE_RUN;  // 限制为运行状态，避免特殊模式下状态异常
                            }
                        }

                        if(msg.arg1 == Sample.SAMPLE_TRANSIENT_DIAPLAY){  // 如果是瞬态显示状态
                            fpgaMessage.setTransientDisplayTimestamp(SystemClock.elapsedRealtime());  // 记录瞬态显示时间戳
                        }

                        sample.setSampleState(msg.arg1);  // 设置采样状态
                        if(mHandler.hasMessages(MSG_FPGA_RUN)){  // 如果已有FPGA执行消息
                            mHandler.removeMessages(MSG_FPGA_RUN);  // 移除旧的执行消息
                        }
                        Message m = Message.obtain();  // 创建新消息
                        m.what = MSG_FPGA_RUN;  // 设置消息类型为FPGA执行
                        m.arg1 = m.arg2 = 0;  // 清空参数
                        mHandler.sendMessageAtFrontOfQueue(m);  // 将消息插入队列头部优先执行
                        break;
                    case MSG_CLEAR_WAVE:  // 处理清除波形消息
                        clearWave();  // 执行清除波形操作
                        break;
                    case MSG_FPGA:  // 处理FPGA命令消息
                        fpgaMessage.add(msg.arg1,msg.arg2);  // 将命令添加到FPGA命令队列
                        break;
                    case MSG_HARDWARE:  // 处理硬件操作消息
                        hwMessage.add(msg.arg1, msg.arg2);  // 将命令添加到硬件命令队列
                        break;
                    case MSG_FPGA_RUN:  // 处理执行FPGA命令消息
                        if(fpgaBoot.isFpgaBootOK()) {  // 检查FPGA是否加载完成
                            if(isRun()) {  // 检查是否允许执行命令
                                if(fpgaMessage.isDelayRun()  // 检查FPGA命令队列是否有待执行命令
                                        || hwMessage.isDelayRun()) {  // 检查硬件命令队列是否有待执行命令
                                    boolean bSync = true;  // 同步标志，默认需要同步
                                    if(fpgaMessage.isDisplayTransient()){  // 如果是瞬态显示模式
                                        bSync = fpgaMessage.display();  // 执行显示命令并获取同步结果
                                        hwMessage.add(HardwareMessage.HARD_ID_OFFSET_MASK,0);  // 添加硬件偏移命令
                                    }else{  // 非瞬态显示模式
                                        hwMessage.run();  // 执行硬件命令队列
                                        fpgaMessage.run();  // 执行FPGA命令队列
                                        if (sample.getSampleState() == Sample.SAMPLE_RUN) {  // 如果是运行状态
                                            setClearWave(true);  // 设置清除波形标志
                                        }
                                    }
                                    if(bSync) {  // 如果需要同步
                                        fpgaMessage.fpgaSync();  // 执行FPGA同步操作
                                    }
                                }
                            }
                        }
                        break;
                    case MSG_FPGA_BOOT: {  // 处理FPGA加载消息

                        {
                            int loadCounter = 0;  // 加载计数器：记录加载尝试次数
                            int vId = 0;  // FPGA ID：用于验证FPGA加载成功
                            SyncHeader.upSyncHeader();  // 更新同步头，标识新的数据周期
                            fpgaMessage.stop();  // 停止当前FPGA命令处理
                            if (fpgaBoot.getFpgaType()  // 如果FPGA类型
                                    != FPGABoot.FPGA_TYPE_NONE) {  // 不是NONE类型（即已有FPGA）
                                ms_sleep(300);  // 等待300ms让硬件稳定
                            }

                            Log.d(TAG, "bootType:" + msg.arg1);  // 输出加载类型日志
                            HwDevice hwDevice = DeviceFactory.allocDevice();  // 分配硬件设备
                            do {  // 外层循环：处理异步设备打开
                                hwDevice.suspend();  // 挂起硬件设备
                                ms_sleep(100);  // 等待100ms
                                do {  // 内层循环：尝试加载FPGA
                                    loadCounter = 0;  // 重置加载计数器
                                    fpgaBoot.loadFpga(msg.arg1);  // 加载FPGA固件，参数为加载类型
                                    while (loadCounter < 40) {  // 最多尝试40次检测FPGA ID
                                        vId = fpgaMessage.fpgaId();  // 读取FPGA ID

                                        if (vId == FPGA_ID) {  // 如果ID匹配
                                            break;  // 跳出循环，加载成功
                                        }
                                        ms_sleep(1000);  // 等待1秒后重试
                                        loadCounter++;  // 增加计数器
                                    }

                                } while (vId != FPGA_ID);  // 如果ID不匹配则继续尝试
                                hwDevice.resume();  // 恢复硬件设备
                                if (!hwDevice.isAsyncOpen()) {  // 如果不是异步打开模式
                                    break;  // 跳出外层循环
                                }
                                if (hwDevice.isOpen()) {  // 如果设备已打开
                                    break;  // 跳出外层循环
                                }
                            } while (true);  // 无限循环直到成功

                            EventFactory.sendEvent(new EventBase(EventFactory.EVENT_FPGA_LOAD_OK, msg.arg1),true);  // 发送FPGA加载完成事件

                            sysTemperature = Hardware.getInstance().getTemperature();  // 读取系统温度
                            Scope.fpgaVer = fpgaMessage.fpgaVersion();  // 读取FPGA版本号
                            hwMessage.resume();  // 恢复硬件消息处理
                            fpgaMessage.cmdInit();  // 初始化FPGA命令
                            fpgaMessage.resume();  // 恢复FPGA消息处理
                            send50O();  // 发送50Ω状态查询
                            sendAdCheck();  // 发送AD校准检查
                            fpgaBoot.setFpgaBootOK(true);  // 设置FPGA加载完成标志
                            bProbeResume = true;  // 设置探头恢复标志
                            Log.d(TAG,"boot end");  // 输出加载结束日志
                        }
                    }
                        break;
                    case MSG_PROBE_EVENT:  // 处理探头事件消息
                        if(fpgaBoot.isFpgaBootOK()) {  // 检查FPGA是否加载完成
                            fpgaMessage.probeProcess();  // 处理探头事件
                        }
                        break;
                    case MSG_SEND_PROBE:  // 处理发送探头命令消息
                        fpgaMessage.sendProbe(msg.arg1,(byte[]) msg.obj);  // 向指定通道发送探头命令
                        break;
                    case MSG_PROBE_TIMER:  // 处理探头定时检查消息
                        fpgaMessage.checkProbe();  // 检查探头状态
                        checkProbeCommand();  // 重新启动定时检查
                        break;
                    case MSG_SCOPE_PAUSE:  // 处理示波器暂停消息
                        hwMessage.pause();  // 暂停硬件消息处理
                        fpgaMessage.pause();  // 暂停FPGA消息处理
                        break;
                    case MSG_SCOPE_RESUME:  // 处理示波器恢复消息
                        fpgaMessage.resume();  // 恢复FPGA消息处理
                        break;
                    case MSG_TOUCH_PAUSE:  // 处理触摸暂停消息
                        fpgaMessage.touchPause();  // 暂停触摸功能
                        break;
                    case MSG_TOUCH_RESUME:  // 处理触摸恢复消息
                        fpgaMessage.touchResume();  // 恢复触摸功能
                        break;
                    case MSG_READ_FPGA_ID:  // 处理读取FPGA ID消息
                        if(fpgaMessage.fpgaId() != 0x46504741){  // 如果ID不正确
                            sendAdCheck();  // 发送AD校准检查
                        }
                        fpgaMessage.fpgaStatus();  // 读取FPGA状态
                        fpgaMessage.fpgaSync();  // 执行FPGA同步
                        break;

                    case MSG_READ_FPGA_STATUS:{  // 处理读取FPGA状态消息
                            fpgaStatus(msg);  // 处理FPGA状态消息
                        }
                        break;
                    case MSG_CH_SAMPLE_CHANGE:  // 处理通道采样变化消息
                        if(isRun()) {  // 如果允许执行命令
                            hwMessage.chSampleChange();  // 处理通道采样变化
                        }else{  // 如果不允许执行
                            if(!hasMessages(MSG_CH_SAMPLE_CHANGE)) {  // 如果没有待处理的消息
                                sendEmptyMessageDelayed(MSG_CH_SAMPLE_CHANGE, 100);  // 延迟100ms重试
                            }
                        }
                        break;
                    case MSG_UI:  // 处理UI消息
                        uiMessage.add(msg.arg1);  // 将UI消息添加到队列
                        break;
                    case MSG_UI_RUN:  // 处理执行UI消息
                        uiMessage.run();  // 执行UI消息队列
                        break;
                    case MSG_FORCE_TRIGGER:  // 处理强制触发消息
                        fpgaMessage.forceTrigger();  // 执行强制触发
                        break;
                    case MSG_SEGMENT_TS:  // 处理段采样时间戳消息
                        fpgaMessage.segmentTimestamp();  // 获取段采样时间戳
                        break;
                    case MSG_SEGMENT_FRAMES:  // 处理段采样帧数消息
                        {
                            SegmentSample segmentSample = SegmentSample.getInstance();  // 获取段采样管理单例
                            if(segmentSample.isSegmentEnable()){  // 如果段采样已启用
                                int frams = fpgaMessage.getSegmentFrames();  // 获取段采样帧数
                                if(frams < 0) frams = 0;  // 如果帧数小于0则设为0
                                segmentSample.setSegmentFrames(frams);  // 设置段采样帧数
                                segmentFrames();  // 发送帧数查询消息
                            }
                        }
                        break;
                    case MSG_50O_STATUS: {  // 处理50Ω阻抗状态消息
                        int val = fpgaMessage.get50O();  // 获取50Ω阻抗状态
                        Scope.getInstance().setResistanceType(val);  // 设置阻抗类型
                        Scope.fpgaTemperature1  = fpgaMessage.getFpgaTemperature(0);  // 获取FPGA温度1
                        Scope.fpgaTemperature2  = fpgaMessage.getFpgaTemperature(1);  // 获取FPGA温度2
                        Hardware.getInstance().setTemperature(Math.max(Scope.fpgaTemperature1,Scope.fpgaTemperature2));  // 设置系统温度为最大值
                        if(HardwareProduct.isFanSpeed()){  // 如果支持风扇转速
                            fpgaMessage.getFanSpeed(Scope.fanSpeed);  // 获取风扇转速
                            Hardware.getInstance().setFanSpeed(Math.max(Scope.fanSpeed[0],Scope.fanSpeed[1]));  // 设置风扇转速为最大值
                        }



                        send50O();  // 发送下一次50Ω状态查询
                        int t = Hardware.getInstance().getTemperature();  // 获取当前温度
                        if(Math.abs(sysTemperature - t) > 10){  // 如果温度变化超过10°C
                            fpgaMessage.forceAdCalibation();  // 强制AD校准
                            sysTemperature = t;  // 更新温度缓存
                        }

                    }
                    break;
                    case MSG_FPGA_CLOCK_INOUT_STATUS:  // 处理FPGA时钟状态消息
                        Scope.fpgaClockInStatus = fpgaMessage.getFpgaClockInOutStatus(true);  // 获取时钟输入状态
                        Scope.fpgaClockOutStatus = fpgaMessage.getFpgaClockInOutStatus(false);  // 获取时钟输出状态
                        break;
                    case MSG_FPGA_STATUS:  // 处理FPGA物理状态消息
                        fpgaPhyStatus(fpgaMessage.getfpgaStatus());  // 设置FPGA物理状态
                        break;
                    case MSG_CMD_SEGMENT:  // 处理段采样命令消息
                        fpgaMessage.cmdSegment();  // 发送段采样命令
                        break;
                    case MSG_AD_RESET:  // 处理AD重置消息
                        fpgaMessage.cmdInit();  // 重新初始化FPGA命令
                        break;
                    case MAG_AD_JZ:  // 处理AD校准消息
                        fpgaMessage.ADCalibrate();  // 执行AD校准
                        break;
                }

                // 消息处理后的延迟执行检查
                if(fpgaMessage.isDelayRun()){  // 如果FPGA命令队列有待执行命令
                    if(!hasMessages(MSG_FPGA_RUN)) {  // 如果没有待处理的执行消息
                        sendEmptyMessageDelayed(MSG_FPGA_RUN, MSG_DELAY_MS);  // 延迟20ms后执行
                    }
                }
                if(uiMessage.isDelayRun()){  // 如果UI消息队列有待执行消息
                    if(!hasMessages(MSG_UI_RUN)) {  // 如果没有待处理的UI执行消息
                        sendEmptyMessageDelayed(MSG_UI_RUN, MSG_UI_DELAY_MS);  // 延迟150ms后执行
                    }
                }
                synchronized (this){  // 同步块：保护共享数据
                    if(isMessageEmpty()  // 如果消息队列为空
                            && !hwMessage.isDelayRun()  // 且硬件命令队列没有待执行命令
                            && !fpgaMessage.isDelayRun()){  // 且FPGA命令队列没有待执行命令
                        bMessageEmpty = true;  // 设置消息队列空标志
                        syncHeader = SyncHeader.getSyncHeader();  // 更新同步头
                        if(sample.getSampleState() == Sample.SAMPLE_TRANSIENT_RUN){  // 如果是瞬态运行状态
                            sample.setSampleState(Sample.SAMPLE_TRANSIENT);  // 切换到瞬态状态
                        }

                        if (scope.isUI()) {  // 如果是UI模式，临时修正前四个模拟通道探头不识别
                            bProbeResume = false;  // 清除探头恢复标志
                            probeEvent(2500);  // 延迟2.5秒后检查探头
                        }
                    }
                }
            }
        };
        checkProbeCommand();  // 启动探头定时检查
    }

    // ==================== 波形清除相关 ====================
    
    private volatile boolean bClearWave = false;  // 清除波形标志：true表示需要清除波形

    /**
     * 设置清除波形标志
     * @param bClearWave 是否清除波形
     */
    public void setClearWave(boolean bClearWave){

        synchronized (this) {  // 同步块：保护共享数据
            this.bClearWave = bClearWave;  // 设置清除波形标志
            if(bClearWave) {  // 如果需要清除波形
                if(mHandler.hasMessages(MSG_CLEAR_WAVE)){  // 如果已有清除波形消息
                    mHandler.removeMessages(MSG_CLEAR_WAVE);  // 移除旧消息
                }
                mHandler.sendEmptyMessageDelayed(MSG_CLEAR_WAVE, 500);  // 延迟500ms后发送清除消息
            }
        }
    }

    /**
     * 执行清除波形操作
     */
    private void clearWave(){
        synchronized (this) {  // 同步块：保护共享数据
            if(bClearWave) {  // 如果需要清除波形
                bClearWave = false;  // 清除标志
                Scope scope = Scope.getInstance();  // 获取示波器核心管理单例
                Sample sample = Sample.getInstance();  // 获取采样管理单例
                int s = sample.getSampleState();  // 获取当前采样状态
                if (s == Sample.SAMPLE_RUN) {  // 如果是运行状态
                    scope.clearWave();  // 清除波形数据
                }else if(s > Sample.SAMPLE_RUN){  // 如果是其他运行状态
                    scope.clearPersist();  // 清除持久化显示
                }
            }
        }
    }

    /**
     * 线程休眠
     * @param ms 休眠时间（毫秒）
     */
    private void ms_sleep(long ms){
        try {
            Thread.sleep(ms);  // 线程休眠指定时间
        } catch (InterruptedException e) {  // 捕获中断异常
            e.printStackTrace();  // 打印异常堆栈
        }
    }

    // ==================== FPGA状态获取 ====================
    
    /**
     * 获取FPGA状态（同步方法）
     * @return FPGA状态对象
     */
    public FPGA_Status getFpgaStatus(){
        FPGA_Status fpgaStatus = new FPGA_Status();  // 创建FPGA状态对象
        synchronized (this){  // 同步块：保护共享数据
            bMessageEmpty = false;  // 设置消息队列非空标志
            Message msg = mHandler.obtainMessage();  // 获取消息对象
            msg.what = MSG_READ_FPGA_STATUS;  // 设置消息类型
            msg.obj = fpgaStatus;  // 设置消息携带的状态对象
            mHandler.sendMessage(msg);  // 发送消息

            try {
                this.wait(1000);  // 等待消息处理完成，最多1秒
            } catch (InterruptedException e) {  // 捕获中断异常
                e.printStackTrace();  // 打印异常堆栈
            }
        }
        return fpgaStatus;  // 返回FPGA状态对象
    }

    /**
     * 处理FPGA状态消息
     * @param msg 消息对象
     */
    public void fpgaStatus(Message msg){
        synchronized (this) {  // 同步块：保护共享数据
            FPGA_Status fpgaStatus = (FPGA_Status) msg.obj;  // 从消息中获取状态对象
            if (fpgaStatus != null) {  // 如果状态对象有效
                fpgaMessage.getFPGA_Status(fpgaStatus);  // 读取FPGA状态信息
                this.notifyAll();  // 唤醒等待的线程
            }
        }
    }

    // ==================== 消息发送方法 ====================
    
    /**
     * 发送通道采样变化消息
     * @return true表示发送成功
     */
    public boolean sendChSample(){
        boolean bret = false;  // 返回值，默认失败
        synchronized (this){  // 同步块：保护共享数据
            bMessageEmpty = false;  // 设置消息队列非空标志
            if(mHandler.hasMessages(MSG_CH_SAMPLE_CHANGE)){  // 如果已有待处理的消息
                mHandler.removeMessages(MSG_CH_SAMPLE_CHANGE);  // 移除旧消息
            }
            Message msg = mHandler.obtainMessage();  // 获取消息对象
            msg.what = MSG_CH_SAMPLE_CHANGE;  // 设置消息类型
            bret = mHandler.sendMessageAtFrontOfQueue(msg);  // 插入队列头部优先处理
        }
        return bret;  // 返回发送结果
    }

    /**
     * 发送读取FPGA ID消息
     * @return true表示发送成功
     */
    public boolean sendFpgaId(){
        synchronized (this){  // 同步块：保护共享数据
            bMessageEmpty = false;  // 设置消息队列非空标志
            return mHandler.sendEmptyMessage(MSG_READ_FPGA_ID);  // 发送读取FPGA ID消息
        }
    }

    volatile boolean bRun = true;  // 命令执行使能标志：true允许执行，false暂停执行

    /**
     * 判断是否允许执行命令
     * @return true表示允许执行
     */
    public synchronized boolean isRun(){
        return bRun;  // 返回命令执行使能标志
    }

    /**
     * 设置命令执行使能
     * @param bEnable 是否使能
     */
    public void enableCommand(boolean bEnable){
        synchronized (this){  // 同步块：保护共享数据
            bRun = bEnable;  // 设置命令执行使能标志
            if(bRun){  // 如果使能
                if(mHandler.hasMessages(MSG_FPGA_RUN)){  // 如果已有执行消息
                    mHandler.removeMessages(MSG_FPGA_RUN);  // 移除旧消息
                }
                mHandler.sendEmptyMessage(MSG_FPGA_RUN);  // 发送新的执行消息
            }
        }
    }


    /**
     * 发送恢复消息
     * @return true表示发送成功
     */
    public boolean sendResume(){
        synchronized (this) {  // 同步块：保护共享数据
            bMessageEmpty = false;  // 设置消息队列非空标志
            if(mHandler.hasMessages(MSG_SCOPE_RESUME)){  // 如果已有恢复消息
                mHandler.removeMessages(MSG_SCOPE_RESUME);  // 移除旧消息
            }
            return mHandler.sendEmptyMessage(MSG_SCOPE_RESUME);  // 发送恢复消息
        }
    }

    /**
     * 发送暂停消息
     * @return true表示发送成功
     */
    public boolean sendPause(){
        synchronized (this) {  // 同步块：保护共享数据
            bMessageEmpty = false;  // 设置消息队列非空标志
            return mHandler.sendEmptyMessage(MSG_SCOPE_PAUSE);  // 发送暂停消息
        }
    }

    /**
     * 发送段采样帧数查询消息
     */
    public void segmentFrames(){
        if(!mHandler.hasMessages(MSG_SEGMENT_FRAMES)) {  // 如果没有待处理的消息
            sendMessageDelayed(MSG_SEGMENT_FRAMES, 0, 0, 300);  // 延迟300ms发送
        }
    }

    /**
     * 发送段采样命令消息
     */
    public void cmdSegment(){
        if(!mHandler.hasMessages(MSG_CMD_SEGMENT)){  // 如果没有待处理的消息
            mHandler.sendEmptyMessage(MSG_CMD_SEGMENT);  // 发送段采样命令消息
        }
    }

    /**
     * 发送探头命令（带延迟）
     * @param chIdx 通道索引
     * @param bytes 命令数据
     * @param ms 延迟时间（毫秒）
     */
    public void sendProbe(int chIdx,byte[] bytes,long ms){
        Message msg = Message.obtain();  // 获取消息对象
        msg.what = MSG_SEND_PROBE;  // 设置消息类型
        msg.arg1 = chIdx;  // 设置通道索引
        msg.obj = bytes;  // 设置命令数据
        if(ms > 0) {  // 如果有延迟
            mHandler.sendMessageDelayed(msg, ms);  // 延迟发送
        }else{  // 无延迟
            mHandler.sendMessage(msg);  // 立即发送
        }
    }

    /**
     * 发送探头命令（立即发送）
     * @param chIdx 通道索引
     * @param bytes 命令数据
     */
    public void sendProbe(int chIdx,byte[] bytes){
        sendProbe(chIdx,bytes,0);  // 调用带延迟的方法，延迟为0
    }

    /**
     * 发送探头事件消息（带延迟）
     * @param ms 延迟时间（毫秒）
     */
    public void probeEvent(long ms){
        if(!mHandler.hasMessages(MSG_PROBE_EVENT)){  // 如果没有待处理的消息
            if(ms > 0){  // 如果有延迟
                mHandler.sendEmptyMessageDelayed(MSG_PROBE_EVENT,ms);  // 延迟发送
            }else{  // 无延迟
                mHandler.sendEmptyMessage(MSG_PROBE_EVENT);  // 立即发送
            }
        }
    }

    /**
     * 发送探头事件消息（立即触发）
     */
    public void probeEvent(){
        probeEvent(0);  // 调用带延迟的方法，延迟为0
    }

    /**
     * 启动探头定时检查
     */
    private void checkProbeCommand(){
        if(!mHandler.hasMessages(MSG_PROBE_TIMER)){  // 如果没有待处理的定时消息
            mHandler.sendEmptyMessageDelayed(MSG_PROBE_TIMER,500);  // 延迟500ms发送
        }
    }

    /**
     * 发送50Ω阻抗状态查询消息
     */
    private void send50O()
    {
        if(!mHandler.hasMessages(MSG_50O_STATUS)) {  // 如果没有待处理的消息
            sendMessageDelayed(MSG_50O_STATUS, 0, 0, 2000);  // 延迟2秒发送
        }
    }

    /**
     * 发送AD校准检查消息（已禁用）
     */
    private void sendAdCheck(){
        // 当前已禁用，保留接口
    }

    /**
     * 发送AD重置消息
     */
    public void sendADReset(){
        if(!mHandler.hasMessages(MSG_AD_RESET)){  // 如果没有待处理的消息
            mHandler.sendEmptyMessage(MSG_AD_RESET);  // 发送AD重置消息
        }
    }

    /**
     * 发送段采样时间戳消息
     */
    public void segmentTimestamp(){
        sendMessage(MSG_SEGMENT_TS,0,0);  // 发送段采样时间戳消息
    }

    /**
     * 发送强制触发消息
     */
    public void forceTrigger(){
        sendMessage(MSG_FORCE_TRIGGER,0,0);  // 发送强制触发消息
    }

    /**
     * 发送FPGA加载消息
     * @param arg 加载类型参数
     * @return true表示发送成功
     */
    public boolean sendFpgaBoot(int arg){
        return sendMessage(MSG_FPGA_BOOT,arg,0);  // 发送FPGA加载消息
    }

    /**
     * 发送FPGA命令消息（带延迟）
     * @param arg1 命令参数1
     * @param arg2 命令参数2
     * @param delayMillis 延迟时间（毫秒）
     * @return true表示发送成功
     */
    public boolean sendFpgaMsgDelayed(int arg1,int arg2,long delayMillis){
        return sendMessageDelayed(MSG_FPGA,arg1,arg2,delayMillis);  // 发送延迟的FPGA命令消息
    }

    /**
     * 发送硬件命令消息（带延迟）
     * @param arg1 硬件ID
     * @param arg2 命令参数
     * @param delayMillis 延迟时间（毫秒）
     * @return true表示发送成功
     */
    public boolean sendHwMsgDelayed(int arg1,int arg2,long delayMillis){
        return sendMessageDelayed(MSG_HARDWARE,arg1,arg2,delayMillis);  // 发送延迟的硬件命令消息
    }

    /**
     * 发送FPGA命令消息（立即发送）
     * @param arg1 命令参数1
     * @param arg2 命令参数2
     * @return true表示发送成功
     */
    public boolean sendFpgaMsg(int arg1,int arg2){

        return sendMessage(MSG_FPGA,arg1,arg2);  // 发送FPGA命令消息
    }

    /**
     * 发送UI消息
     * @param arg1 UI消息类型
     * @return true表示发送成功
     */
    public boolean sendUiMsg(int arg1){

        if (arg1 == UiMessage.UI_MESSAGE_DEPTH_SAMPFRE)  // 如果是存储深度/采样频率变化消息
            EventFactory.sendEvent(EventFactory.EVENT_DEPTH_SAMPFRE_CHANGE);  // 发送事件通知
        return sendMessage(MSG_UI, arg1, 0);  // 发送UI消息

    }

    /**
     * 发送硬件命令消息（立即发送）
     * @param arg1 硬件ID
     * @param arg2 命令参数
     * @return true表示发送成功
     */
    public boolean sendHwMsg(int arg1,int arg2){
        return sendMessage(MSG_HARDWARE,arg1,arg2);  // 发送硬件命令消息
    }

    /**
     * 发送消息（内部方法）
     * @param what 消息类型
     * @param arg1 参数1
     * @param arg2 参数2
     * @return true表示发送成功
     */
    private boolean sendMessage(int what,int arg1,int arg2){

        synchronized (this) {  // 同步块：保护共享数据
            bMessageEmpty = false;  // 设置消息队列非空标志
            Message msg = Message.obtain();  // 获取消息对象
            msg.what = what;  // 设置消息类型
            msg.arg1 = arg1;  // 设置参数1
            msg.arg2 = arg2;  // 设置参数2
            if(what == MSG_HARDWARE || what == MSG_SAMPLE_STATE){  // 如果是硬件或采样状态消息
                return mHandler.sendMessageAtFrontOfQueue(msg);  // 插入队列头部优先处理
            } else {  // 其他消息类型
                return mHandler.sendMessage(msg);  // 添加到队列尾部
            }
        }
    }

    /**
     * 发送延迟消息（内部方法）
     * @param what 消息类型
     * @param arg1 参数1
     * @param arg2 参数2
     * @param delayMillis 延迟时间（毫秒）
     * @return true表示发送成功
     */
    private boolean sendMessageDelayed(int what,int arg1,int arg2,long delayMillis){
        synchronized (this) {  // 同步块：保护共享数据
            bMessageEmpty = false;  // 设置消息队列非空标志
            Message msg = Message.obtain();  // 获取消息对象
            msg.what = what;  // 设置消息类型
            msg.arg1 = arg1;  // 设置参数1
            msg.arg2 = arg2;  // 设置参数2
            return mHandler.sendMessageDelayed(msg, delayMillis);  // 延迟发送消息
        }
    }

    /**
     * 检查消息队列是否为空
     * @return true表示消息队列为空
     */
    private boolean isMessageEmpty(){
        boolean bEmpty = true;  // 默认为空
        for (int i = MSG_FPGA; i <= MSG_SAMPLE_STATE; i++) {  // 遍历所有消息类型
            if (mHandler.hasMessages(i)) {  // 如果有该类型的消息
                bEmpty = false;  // 设置为非空
                break;  // 跳出循环
            }
        }
        return bEmpty;  // 返回检查结果
    }

    /**
     * 检查消息队列是否处理完毕
     * @return true表示消息队列已处理完毕
     */
    public boolean messageFinished(){
        synchronized (this) {  // 同步块：保护共享数据
            return bMessageEmpty;  // 返回消息队列空标志
        }
    }


    // ==================== 单例模式实现 ====================
    
    private static volatile ScopeMessage instance = null;  // 单例实例，volatile保证可见性

    /**
     * 获取单例实例
     * @return ScopeMessage单例实例
     */
    public static ScopeMessage getInstance() {
        if (instance == null) {  // 第一次检查
            synchronized (ScopeMessage.class) {  // 同步块
                if (instance == null ) {  // 第二次检查
                    instance = new ScopeMessage();  // 创建实例
                }
            }
        }
        return instance;  // 返回实例
    }

    /**
     * 静态方法：发送恢复消息
     */
    public static void runResume(){
        getInstance().sendResume();  // 调用实例方法发送恢复消息
    }

    /**
     * 静态方法：发送暂停消息
     */
    public static void runPause(){
        getInstance().sendPause();  // 调用实例方法发送暂停消息
    }

    /**
     * 静态方法：检查消息队列是否为空
     * @return true表示消息队列为空
     */
    public static boolean isEmpty(){
        return getInstance().messageFinished();  // 调用实例方法检查消息队列
    }


    // ==================== 温度相关方法 ====================
    
    /**
     * 获取当前温度
     * @return 系统温度（摄氏度）
     */
    public int getTemperature(){
        return Hardware.getInstance().getTemperature();  // 从硬件管理获取温度
    }

    /**
     * 获取缓存的系统温度
     * @return 上次记录的系统温度
     */
    public int getSysTemperature(){
        return sysTemperature;  // 返回缓存的系统温度
    }

    /**
     * 强制触发AD校准
     */
    public void forceAdCalibation(){
        sysTemperature = 0;  // 重置温度缓存为0，下次检查时触发AD校准
    }

    /**
     * 设置采样状态
     * @param state 采样状态
     * @return true表示发送成功
     */
    public boolean setSampleState(int state){
        return sendMessage(MSG_SAMPLE_STATE, state, state);  // 发送采样状态变化消息
    }

    /**
     * 设置AD校准
     */
    public void setADCalibrate(){
        sendMessage(MAG_AD_JZ,0,0);  // 发送AD校准消息
    }


    /**
     * 暂停触摸功能
     */
    public void touchPause(){
        Log.d(TAG, "touchPause() called");  // 输出调试日志
        mHandler.post(new Runnable() {  // 通过Handler post方式执行
            @Override
            public void run() {
                fpgaMessage.touchPause();  // 暂停触摸功能
            }
        });
    }

    /**
     * 恢复触摸功能
     */
    public void touchResume(){
        Log.d(TAG, "touchResume() called");  // 输出调试日志
        sendFpgaMsg(FPGAMessage.FPGA_CMD_DIS|FPGAMessage.FPGA_CMD_DIS_PIX,0);  // 发送FPGA命令恢复触摸和像素显示
    }

    /**
     * 获取当前同步头
     * @return 当前同步头值
     */
    public synchronized int getSyncHeader(){
        return syncHeader;  // 返回当前同步头
    }


    /**
     * 获取FPGA时钟输入输出状态
     * @param isIn true表示输入时钟，false表示输出时钟
     * @return 时钟状态
     */
    public boolean getFpgaClockInOutStatus(boolean isIn) {
        sendFpgaClockInOutMsg();  // 发送时钟状态查询消息
        ms_sleep(50);  // 等待50ms
        return isIn ? Scope.fpgaClockInStatus : Scope.fpgaClockOutStatus;  // 根据参数返回输入或输出状态
    }

    /**
     * 发送FPGA时钟状态查询消息
     */
    private void sendFpgaClockInOutMsg() {
        mHandler.sendEmptyMessage(MSG_FPGA_CLOCK_INOUT_STATUS);  // 发送时钟状态查询消息
    }

    // ==================== FPGA物理状态 ====================
    
    private int fpgaPhyStatus = 0;  // FPGA物理状态缓存

    /**
     * 获取FPGA物理状态（同步方法）
     * @return FPGA物理状态值，超时返回-1
     */
    public synchronized int getFpgaPhyStatus(){
        fpgaPhyStatus = 0;  // 重置状态缓存
        mHandler.sendEmptyMessage(MSG_FPGA_STATUS);  // 发送FPGA状态查询消息
        try {
            this.wait(1000);  // 等待消息处理完成，最多1秒
        } catch (InterruptedException e) {  // 捕获中断异常
            return -1;  // 返回错误值
        }
        return fpgaPhyStatus;  // 返回FPGA物理状态
    }

    /**
     * 设置FPGA物理状态（内部回调）
     * @param fpgaPhyStatus FPGA物理状态值
     */
    private synchronized void fpgaPhyStatus(int fpgaPhyStatus){
        this.fpgaPhyStatus = fpgaPhyStatus;  // 设置FPGA物理状态
        this.notifyAll();  // 唤醒等待的线程
    }


}
