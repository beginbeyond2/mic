package com.micsig.tbook.scope.channel;  // 定义包名：示波器通道管理模块


import com.micsig.base.FilterThread;  // 导入FilterThread类：过滤线程，用于防抖
import com.micsig.base.Logger;  // 导入Logger类：基础日志工具
import com.micsig.tbook.scope.Action.FPGAMessage;  // 导入FPGAMessage类：FPGA消息常量定义
import com.micsig.tbook.scope.Action.HardwareMessage;  // 导入HardwareMessage类：硬件消息常量定义
import com.micsig.tbook.scope.Action.UiMessage;  // 导入UiMessage类：UI消息常量定义
import com.micsig.tbook.scope.Action.XAction;  // 导入XAction类：动作代理基类
import com.micsig.tbook.scope.Event.EventBase;  // 导入EventBase类：事件基类
import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂


/**
 * 通道动作代理类 - 模拟通道消息发送与事件分发
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.channel（示波器通道管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 通道动作代理</li>
 *   <li>设计模式：代理模式 + 防抖模式</li>
 *   <li>职责类型：通道状态变化的消息发送、事件分发、防抖处理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>处理模拟通道各种状态变化时的消息发送</li>
 *   <li>向FPGA发送配置命令（档位、耦合、位置等）</li>
 *   <li>向硬件发送控制消息（继电器控制）</li>
 *   <li>向UI发送事件通知（界面更新）</li>
 *   <li>提供位置变化的防抖处理，避免频繁发送消息</li>
 * </ul>
 * 
 * <p><b>通道动作类型架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   ChannelAction - 通道动作代理                                            │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │                      通道状态变化事件                            │   │
 * │   │                                                                   │   │
 * │   │   open()              打开通道                                    │   │
 * │   │   close()             关闭通道                                    │   │
 * │   │   active()            激活通道                                    │   │
 * │   │   probeChange()       探头变化                                    │   │
 * │   │   pos()               位置变化（带防抖）                          │   │
 * │   │   movezero()          零点移动                                    │   │
 * │   │   offsetChange()      偏移变化                                    │   │
 * │   │   vScaleIdChange()    档位变化                                    │   │
 * │   │   vFineChange()       微调变化                                    │   │
 * │   │   InvertChange()      反相变化                                    │   │
 * │   │   CoupleTypeChange()  耦合变化                                    │   │
 * │   │   ResistanceTypeChange() 阻抗变化                                 │   │
 * │   │   ProbeRateChange()   探头比率变化                                │   │
 * │   │   BandWidthChange()   带宽变化                                    │   │
 * │   │   busLevelChange()    总线电平变化（带防抖）                      │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │                      消息发送目标                                │   │
 * │   │                                                                   │   │
 * │   │   sendFpgaMsg()      ───► FPGA（FPGA配置命令）                   │   │
 * │   │   sendHwMsg()        ───► Hardware（硬件控制消息）               │   │
 * │   │   sendEvent()        ───► UI（界面更新事件）                     │   │
 * │   │   sendUiMsg()        ───► UI（UI消息）                           │   │
 * │   │   sendChSample()     ───► 采样系统（通道采样状态）               │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>防抖机制：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   FilterThread防抖机制                                                   │
 * │                                                                          │
 * │   问题：用户快速调整位置时，会产生大量位置变化事件                        │
 * │         如果每次都发送FPGA消息，会导致：                                  │
 * │         1. FPGA消息队列拥堵                                              │
 * │         2. 系统响应变慢                                                  │
 * │         3. 硬件频繁切换                                                  │
 * │                                                                          │
 * │   解决方案：FilterThread防抖                                              │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │  用户操作          时间线                                         │   │
 * │   │                                                                   │   │
 * │   │  pos(100)     ────┼──── 110ms后执行                              │   │
 * │   │  pos(200)         ┼──┼── 重置计时器                              │   │
 * │   │  pos(300)           ┼ 重置计时器                                  │   │
 * │   │                      └── 110ms后执行posChange()                   │   │
 * │   │                                                                   │   │
 * │   │  结果：只发送最后一次位置变化的FPGA消息                            │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   防抖延时：110ms（调试得到的最佳值，不要修改）                          │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>FPGA命令组合：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   常用FPGA命令组合                                                        │
 * │                                                                          │
 * │   ChannelChange() - 通道全面更新：                                       │
 * │   ├── FPGA_CMD_DIS_MODE      显示模式                                   │
 * │   ├── FPGA_CMD_Y_PLACE       Y轴位置                                   │
 * │   ├── FPGA_CMD_CH_OFFSET     通道偏移                                   │
 * │   ├── FPGA_CMD_SAMP_MODE     采样模式                                   │
 * │   ├── FPGA_CMD_COUY          耦合Y                                     │
 * │   ├── FPGA_CMD_DIS_PIX       显示像素                                   │
 * │   ├── FPGA_CMD_AD_CH_CHANGE  AD通道变化                                │
 * │   ├── FPGA_CMD_SAMP_ZUN_DEPTH采样深度                                   │
 * │   ├── FPGA_CMD_SAMP_PLACE    采样位置                                   │
 * │   ├── FPGA_CMD_DIS           显示                                       │
 * │   ├── FPGA_CMD_TRIG_LEVEL    触发电平                                   │
 * │   ├── FPGA_CMD_TRIG          触发                                       │
 * │   ├── FPGA_CMD_EXT_CHAZHI_COEF 插值系数                                 │
 * │   ├── FPGA_CMD_EXT_AD_ZERO   AD零点                                    │
 * │   ├── FPGA_CMD_EXT_AD_COEF   AD系数                                    │
 * │   └── FPGA_CMD_BUS_TYPE      总线类型                                   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>继承关系：</b>
 * <pre>
 *   XAction（动作代理基类）
 *       │
 *       └── ChannelAction（通道动作代理）
 *               │
 *               └── 继承sendFpgaMsg、sendHwMsg、sendEvent、sendUiMsg、sendChSample等方法
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：Channel（模拟通道，持有此Action引用）</li>
 *   <li>依赖：XAction（动作代理基类，提供消息发送方法）</li>
 *   <li>依赖：FilterThread（防抖线程）</li>
 *   <li>依赖：FPGAMessage（FPGA消息常量）</li>
 *   <li>依赖：HardwareMessage（硬件消息常量）</li>
 *   <li>依赖：EventFactory（事件工厂）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @see Channel 模拟通道
 * @see XAction 动作代理基类
 * @see FilterThread 防抖线程
 */
public class ChannelAction extends XAction {  // 继承XAction动作代理基类

    /** 日志标签 */
    private static final String TAG = "ChannelAction";  // 日志输出标签
    
    /** 关联的模拟通道引用 */
    private Channel channel;  // 持有Channel引用，用于获取通道信息
    
    /** 防抖线程：用于位置变化等频繁操作的防抖处理 */
    private FilterThread filterThread;  // 防抖线程
    
    /** 总线电平变化标志：用于防抖标记 */
    private volatile boolean busLevelChange = false;  // 总线电平变化标志
    
    /** 通道位置变化标志：用于防抖标记 */
    private volatile boolean chPosChange = false;  // 通道位置变化标志
    
    /** 零点变化标志：用于防抖标记 */
    private volatile boolean zeroChange = false;  // 零点变化标志

    /**
     * 构造方法：初始化通道动作代理
     * 
     * <p>创建防抖线程，设置110ms延时（调试得到的最佳值）。
     * 
     * @param channel 关联的模拟通道对象
     */
    public ChannelAction(Channel channel){
        this.channel = channel;  // 保存通道引用
        filterThread = new FilterThread(TAG);  // 创建防抖线程
        filterThread.setDelayMillis(110);  // 设置防抖延时110ms（调试得到的值，不要动）
        filterThread.setRunnable(new Runnable() {  // 设置防抖执行任务
            @Override
            public void run() {  // 防抖执行方法
                if(busLevelChange){  // 检查总线电平变化标志
                    synchronized (filterThread) {  // 同步锁
                        busLevelChange = false;  // 清除标志
                    }
                    sendFpgaMsg(0,FPGAMessage.FPGA_CMD_BUS_LEVEL);  // 发送总线电平FPGA命令
                }
                if(chPosChange) {  // 检查通道位置变化标志
                    synchronized (filterThread) {  // 同步锁
                        chPosChange = false;  // 清除标志
                    }
                    posChange();  // 执行位置变化处理
                }
                if(zeroChange){  // 检查零点变化标志
                    synchronized (filterThread) {  // 同步锁
                        zeroChange = false;  // 清除标志
                    }
                    posChange();  // 执行位置变化处理
                }

            }
        });
    }

    /**
     * 打开通道
     * 
     * <p>发送通道采样状态、打开采样、发送通道打开事件。
     */
    public void open(){
        sendChSample();  // 发送通道采样状态
        openSample();  // 打开采样
        sendEvent(EventFactory.EVENT_CHANNEL_OPEN,true);  // 发送通道打开事件
    }

    /**
     * 通道延迟变化
     * 
     * <p>发送延迟变化事件，打开采样。
     */
    public void changeDelay(){
        sendEvent(EventFactory.EVENT_CHANNEL_DELAY);  // 发送延迟变化事件
        openSample();  // 打开采样
    }

    /**
     * 打开采样
     * 
     * <p>发送硬件消息和FPGA命令，启动通道采样。
     */
    public void openSample(){
        ChannelChangeHw();  // 发送硬件消息
        ChannelChange();  // 发送FPGA命令
    }

    /**
     * 关闭采样
     * 
     * <p>发送硬件消息和FPGA命令，停止通道采样。
     */
    public void closeSample(){
        sendHwMsg(HardwareMessage.HARD_ID_CH_vSCALE_CH1);  // 发送硬件消息
        ChannelChange();  // 发送FPGA命令
    }

    /**
     * 发送通道硬件变化消息
     * 
     * <p>发送耦合、偏移、档位相关的硬件消息。
     */
    private void ChannelChangeHw(){
        int idx = channel.getChId();  // 获取通道索引
        sendHwMsg(HardwareMessage.HARD_ID_CH_COUP_CH1 <<(idx));  // 发送耦合硬件消息
        sendHwMsg(HardwareMessage.HARD_ID_OFFSET_CH1 <<(idx));  // 发送偏移硬件消息
        sendHwMsg(HardwareMessage.HARD_ID_CH_vSCALE_CH1 <<(idx));  // 发送档位硬件消息
    }

    /**
     * 发送通道FPGA变化命令
     * 
     * <p>发送完整的通道配置FPGA命令组合。
     */
    private void ChannelChange(){
        sendFpgaMsg(FPGAMessage.FPGA_CMD_DIS_MODE  // 显示模式
                        | FPGAMessage.FPGA_CMD_Y_PLACE  // Y轴位置
                        | FPGAMessage.FPGA_CMD_CH_OFFSET  // 通道偏移
                        | FPGAMessage.FPGA_CMD_SAMP_MODE  // 采样模式
                        | FPGAMessage.FPGA_CMD_COUY  // 耦合Y
                        | FPGAMessage.FPGA_CMD_DIS_PIX  // 显示像素
                        | FPGAMessage.FPGA_CMD_AD_CH_CHANGE  // AD通道变化
                        | FPGAMessage.FPGA_CMD_SAMP_ZUN_DEPTH  // 采样深度
                        | FPGAMessage.FPGA_CMD_SAMP_PLACE  // 采样位置
                        | FPGAMessage.FPGA_CMD_DIS  // 显示
                        | FPGAMessage.FPGA_CMD_TRIG_LEVEL  // 触发电平
                        | FPGAMessage.FPGA_CMD_TRIG  // 触发
                , FPGAMessage.FPGA_CMD_EXT_CHAZHI_COEF  // 扩展：插值系数
                        | FPGAMessage.FPGA_CMD_EXT_AD_ZERO  // 扩展：AD零点
                        | FPGAMessage.FPGA_CMD_EXT_AD_COEF  // 扩展：AD系数
                        | FPGAMessage.FPGA_CMD_BUS_TYPE);  // 扩展：总线类型
    }

    /**
     * 激活通道
     * 
     * <p>发送激活事件和UI消息。
     */
    public void active(){
        sendEvent(EventFactory.EVENT_CHANNEL_ACTIVE);  // 发送激活事件
        sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);  // 发送采样频率UI消息
        sendUiMsg(UiMessage.UI_MESSAGE_SIMPLE_WIN_DIS);  // 发送窗口显示UI消息
    }

    /**
     * 探头变化
     * 
     * <p>发送探头变化事件和档位变化事件。
     */
    public void probeChange(){
        sendEvent(EventFactory.EVENT_PROBE_EVENT,true);  // 发送探头事件
        sendEvent(EventFactory.EVENT_CHANNEL_VSCALE,true);  // 发送档位变化事件
    }

    /**
     * 位置变化处理
     * 
     * <p>发送偏移硬件消息和位置相关FPGA命令。
     */
    private void posChange(){

        int idx = channel.getChId();  // 获取通道索引

        sendHwMsg(HardwareMessage.HARD_ID_OFFSET_CH1 <<(idx));  // 发送偏移硬件消息
        sendFpgaMsg(FPGAMessage.FPGA_CMD_DIS_PIX_ch1<<(idx)  // 显示像素
                | FPGAMessage.FPGA_CMD_TRIG_LEVEL  // 触发电平
                | FPGAMessage.FPGA_CMD_Y_PLACE  // Y轴位置
                | FPGAMessage.FPGA_CMD_CH_OFFSET  // 通道偏移
                | FPGAMessage.FPGA_CMD_DIS  // 显示
                ,FPGAMessage.FPGA_CMD_BUS_LEVEL  // 扩展：总线电平
        );
    }

    /**
     * 零点移动
     * 
     * <p>使用防抖机制处理零点移动，发送零点事件。
     */
    public void movezero(){
        synchronized (filterThread){  // 同步锁
            zeroChange = true;  // 设置零点变化标志
            filterThread.run();  // 触发防抖线程
        }

        sendEvent(EventFactory.EVENT_CHANNEL_ZERO,true);  // 发送零点事件
    }

    /**
     * 偏移变化
     * 
     * <p>发送偏移变化事件。
     */
    public void offsetChange(){
        sendEvent(EventFactory.EVENT_CHANNEL_OFFSET,true);  // 发送偏移变化事件
    }

    /** 备份位置值：用于防抖比较 */
    int bakpos = Integer.MAX_VALUE;  // 备份位置值

    /**
     * 设置通道位置
     * 
     * <p>支持防抖模式和非防抖模式。
     * 
     * @param pos 位置值
     * @param delay true表示使用防抖，false表示立即执行
     */
    public void pos(int pos,boolean delay){
        if(delay){  // 使用防抖模式
            synchronized (filterThread) {  // 同步锁
                if(pos != bakpos) {  // 检查位置是否变化
                    bakpos = pos;  // 更新备份位置
                    chPosChange = true;  // 设置位置变化标志
                    filterThread.run();  // 触发防抖线程
                }
            }
        }
        else{  // 立即执行模式
            bakpos = pos;  // 更新备份位置
            posChange();  // 立即执行位置变化
        }
        // 给界面的不需要延时处理
        sendEvent(EventFactory.EVENT_CHANNEL_POS,true);  // 发送位置事件
    }

    /**
     * 关闭通道
     * 
     * <p>发送通道采样状态、关闭采样、发送通道关闭事件。
     */
    public void close(){

        sendChSample();  // 发送通道采样状态
        closeSample();  // 关闭采样
        sendEvent(EventFactory.EVENT_CHANNEL_CLOSE,true);  // 发送通道关闭事件
    }

    /**
     * 档位用户变化
     * 
     * <p>发送用户档位变化事件。
     */
    public void vScaleIdUserChange(){
        sendEvent(EventFactory.EVENT_CHANNEL_VSCALE_USER);  // 发送用户档位变化事件
    }

    /**
     * 档位变化
     * 
     * <p>发送硬件消息和FPGA命令，触发继电器控制。
     */
    public void vScaleChange(){  // 档位变化时发送硬件消息
        Logger.d(TAG, "调整档位-----5------");  // 输出调试日志
        int idx = channel.getChId();  // 获取通道索引
        sendHwMsg(HardwareMessage.HARD_ID_OFFSET_CH1 << (idx));  // 发送偏移硬件消息
        sendHwMsg(HardwareMessage.HARD_ID_CH_vSCALE_CH1 << (idx));  // 发送硬件消息 - 触发继电器控制
         // 发送FPGA消息
        sendFpgaMsg(FPGAMessage.FPGA_CMD_DIS_PIX_ch1<<(idx)  // 显示像素
                        | FPGAMessage.FPGA_CMD_TRIG_HUICHA  // 触发回差
                        | FPGAMessage.FPGA_CMD_TRIG_LEVEL  // 触发电平
                        | FPGAMessage.FPGA_CMD_Y_PLACE  // Y轴位置
                        | FPGAMessage.FPGA_CMD_CH_OFFSET  // 通道偏移
                        | FPGAMessage.FPGA_CMD_SAMP_MODE  // 采样模式
                        | FPGAMessage.FPGA_CMD_DIS  // 显示

                , FPGAMessage.FPGA_CMD_EXT_AD_ZERO  // 扩展：AD零点
                        | (FPGAMessage.FPGA_CMD_EXT_vScale_CHANGE_CH1<<idx)  // 扩展：档位变化
        );
    }

    /**
     * 微调变化
     * 
     * <p>发送FPGA命令和档位变化事件。
     */
    public void vFineChange(){
        int idx = channel.getChId();  // 获取通道索引
        sendFpgaMsg(FPGAMessage.FPGA_CMD_DIS_PIX_ch1<<(idx)  // 显示像素
                        | FPGAMessage.FPGA_CMD_TRIG_HUICHA  // 触发回差
                        | FPGAMessage.FPGA_CMD_TRIG_LEVEL  // 触发电平
                        | FPGAMessage.FPGA_CMD_Y_PLACE  // Y轴位置
                        | FPGAMessage.FPGA_CMD_CH_OFFSET  // 通道偏移
                        | FPGAMessage.FPGA_CMD_SAMP_MODE  // 采样模式
                        | FPGAMessage.FPGA_CMD_DIS  // 显示
                , FPGAMessage.FPGA_CMD_EXT_AD_ZERO  // 扩展：AD零点
                        | (FPGAMessage.FPGA_CMD_EXT_vScale_CHANGE_CH1<<idx)  // 扩展：档位变化
        );
        sendEvent(EventFactory.EVENT_CHANNEL_VSCALE,true);  // 发送档位变化事件
    }

    /**
     * 档位ID变化
     * 
     * <p>触发继电器控制，发送多个事件。
     */
    public void vScaleIdChange(){
        Logger.d(TAG, "调整档位-----4------");  // 输出调试日志
        vScaleChange();  // 触发继电器控制
        sendEvent(EventFactory.EVENT_CHANNEL_VSCALE);  // 发送档位变化事件
        sendEvent(EventFactory.EVENT_CHANNEL_ZERO,true);  // 发送零点事件
        sendEvent(EventFactory.EVENT_CHANNEL_VSCALE_USER,true);  // 发送用户档位变化事件
    }

    /**
     * 反相变化
     * 
     * <p>发送硬件消息、FPGA命令和反相事件。
     */
    public void InvertChange(){
        int idx = channel.getChId();  // 获取通道索引
        sendHwMsg(HardwareMessage.HARD_ID_OFFSET_CH1 <<(idx));  // 发送偏移硬件消息
        sendFpgaMsg(FPGAMessage.FPGA_CMD_SAMP_MODE | FPGAMessage.FPGA_CMD_Y_PLACE | FPGAMessage.FPGA_CMD_DIS);  // 发送FPGA命令
        sendEvent(EventFactory.EVENT_CHANNEL_INVERT);  // 发送反相事件
    }

    /**
     * 耦合类型变化
     * 
     * <p>发送硬件消息、FPGA命令和耦合事件。
     */
    public void CoupleTypeChange(){
        int idx = channel.getChId();  // 获取通道索引
        sendHwMsg(HardwareMessage.HARD_ID_CH_COUP_CH1 <<(idx));  // 发送耦合硬件消息
        sendFpgaMsg(FPGAMessage.FPGA_CMD_SAMP_MODE);  // 发送采样模式FPGA命令
        sendEvent(EventFactory.EVENT_CHANNEL_COUPLE);  // 发送耦合事件
    }

    /**
     * 阻抗类型变化
     * 
     * <p>发送硬件消息、FPGA命令和阻抗事件。
     * 阻抗变化会影响多个参数，需要更新AD系数、零点等。
     */
    public void ResistanceTypeChange(){
        
        int idx = channel.getChId();  // 获取通道索引
        sendHwMsg(HardwareMessage.HARD_ID_CH_vSCALE_CH1 << (idx)|HardwareMessage.HARD_ID_OFFSET_CH1 <<(idx));  // 发送档位和偏移硬件消息

        sendFpgaMsg(FPGAMessage.FPGA_CMD_DIS_PIX_ch1<<(idx)  // 显示像素
                        | FPGAMessage.FPGA_CMD_TRIG_HUICHA  // 触发回差
                        | FPGAMessage.FPGA_CMD_TRIG_LEVEL  // 触发电平
                        | FPGAMessage.FPGA_CMD_Y_PLACE  // Y轴位置
                        | FPGAMessage.FPGA_CMD_CH_OFFSET  // 通道偏移
                        | FPGAMessage.FPGA_CMD_SAMP_MODE  // 采样模式
                        | FPGAMessage.FPGA_CMD_DIS  // 显示

                , FPGAMessage.FPGA_CMD_EXT_AD_ZERO|FPGAMessage.FPGA_CMD_EXT_AD_COEF  // 扩展：AD零点和AD系数
                        |FPGAMessage.FPGA_CMD_EXT_RESISTANCE| (FPGAMessage.FPGA_CMD_EXT_vScale_CHANGE_CH1<<idx)  // 扩展：阻抗和档位变化
        );
        sendEvent(EventFactory.EVENT_CHANNEL_RESISTANCETYPE,true);  // 发送阻抗事件
    }

    /**
     * 探头比率变化
     * 
     * <p>发送档位变化事件和位置变化处理。
     */
    public void ProbeRateChange(){
        sendEvent(EventFactory.EVENT_CHANNEL_VSCALE,true);  // 发送档位变化事件
        posChange();  // 执行位置变化处理
    }

    /**
     * 改变探头DA
     * 
     * <p>发送探头DA FPGA命令。
     */
    public void changeProbeDa(){
        sendFpgaMsg(0,FPGAMessage.FPGA_CMD_EXT_PROBE_DA);  // 发送探头DA FPGA命令
    }

    /**
     * 带宽类型变化
     * 
     * <p>发送AD系数FPGA命令和带宽事件。
     */
    public void BandWidthTypeChange(){
        int idx = channel.getChId();  // 获取通道索引
        sendFpgaMsg(0,FPGAMessage.FPGA_CMD_EXT_AD_COEF);  // 发送AD系数FPGA命令
        sendEvent(EventFactory.EVENT_CHANNEL_BANDWIDTH);  // 发送带宽事件
    }

    /**
     * 带宽变化
     * 
     * <p>发送AD系数FPGA命令和带宽事件。
     */
    public void BandWidthChange(){
        sendFpgaMsg(0,FPGAMessage.FPGA_CMD_EXT_AD_COEF);  // 发送AD系数FPGA命令
        sendEvent(EventFactory.EVENT_CHANNEL_BANDWIDTH);  // 发送带宽事件
    }

    /**
     * 总线电平变化
     * 
     * <p>使用防抖机制处理总线电平变化，发送总线电平事件。
     */
    public void busLevelChange(){
        synchronized (filterThread) {  // 同步锁
            busLevelChange = true;  // 设置总线电平变化标志
        }
        filterThread.run();  // 触发防抖线程
        //sendFpgaMsg(0,FPGAMessage.FPGA_CMD_BUS_LEVEL);  // 直接发送FPGA命令（已注释）
       sendEvent(EventFactory.EVENT_BUS_LEVEL);  // 发送总线电平事件
    }

    /**
     * 获取波形
     * 
     * <p>发送获取波形事件。
     */
    public void getWave(){
        sendEvent(EventFactory.EVENT_CHANNEL_GET_WAVE,true);  // 发送获取波形事件
    }

    /**
     * 发送事件（同步）
     * 
     * @param event 事件ID
     */
    @Override
    public void sendEvent(int event){
        sendEvent(event,false);  // 默认同步发送
    }

    /**
     * 发送事件
     * 
     * @param event 事件ID
     * @param async true表示异步发送，false表示同步发送
     */
    public void sendEvent(int event,boolean async){
        sendEvent(event,async,0);  // 调用三参数版本
    }

    /**
     * 发送事件（带延时）
     * 
     * @param event 事件ID
     * @param async true表示异步发送，false表示同步发送
     * @param ms 延时时间（毫秒）
     */
    public void sendEvent(int event,boolean async,long ms){
        int idx = channel.getChId();  // 获取通道索引
        sendEvent(new EventBase(event,idx),async,ms);  // 发送带通道ID的事件
    }
}
