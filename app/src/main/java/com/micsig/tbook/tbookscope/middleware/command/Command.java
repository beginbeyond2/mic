package com.micsig.tbook.tbookscope.middleware.command; // 命令模式中间件包，统一管理示波器所有功能指令

import com.micsig.tbook.tbookscope.LoadCache; // 缓存加载事件类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线，用于组件间通信
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举定义
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类，管理加载状态

import io.reactivex.rxjava3.annotations.NonNull; // RxJava非空注解
import io.reactivex.rxjava3.functions.Consumer; // RxJava消费者接口


/**
 * Created by liwb on 2018/1/17.
 * <pre class="prettyprint">
 *     注：command中所有的通道号的索引值（0~3）之间。需要变换时，在外面变换好后，再传进来。
 * </pre>
 * Command类，通一接口，由界面和SCPI使用。
 * 注：指令中是不进行判断是否合法，会直接进行执行指令
 * 如：在XY模式下没有数学，通过SCPI执行就会先行判断，是否在XY模式下。界面中，则不能点击数学。
 *
 */
/*
 * +=============================================================================+
 * |                              Command 类                                     |
 * +=============================================================================+
 * | 【模块定位】                                                                 |
 * |   示波器中间件层核心命令调度中心，采用单例模式，作为UI界面与SCPI远程控制       |
 * |   统一访问入口，集中管理所有功能子命令模块                                     |
 * +-------------------------------------------------------------------------------+
 * | 【核心职责】                                                                 |
 * |   1. 以单例模式提供全局唯一命令入口                                           |
 * |   2. 持有并管理所有功能子命令模块实例（Auto/Bus/Channel/Trigger等）           |
 * |   3. 通过RxBus订阅缓存加载事件，在APP启动时初始化命令状态                     |
 * |   4. 提供各子命令模块的getter访问接口                                         |
 * +-------------------------------------------------------------------------------+
 * | 【架构设计】                                                                 |
 * |   单例模式（静态内部类持有）+ 门面模式（统一入口）                            |
 * |   Command作为门面，内部聚合30+个子命令模块，每个模块负责一类功能              |
 * +-------------------------------------------------------------------------------+
 * | 【数据流向】                                                                 |
 * |   UI/SCPI → Command.get() → 子命令模块 → CommandMsgToUI → RxBus → UI更新    |
 * +-------------------------------------------------------------------------------+
 * | 【依赖关系】                                                                 |
 * |   - RxBus: 事件总线，用于接收缓存加载事件                                    |
 * |   - CacheUtil: 缓存工具，标记命令模块加载完成状态                             |
 * |   - CommandMsgToUI: 命令消息载体，用于向UI层传递更新通知                      |
 * |   - 所有Command_*子类: 各功能领域的具体命令处理器                              |
 * +-------------------------------------------------------------------------------+
 * | 【使用场景】                                                                 |
 * |   1. UI层操作示波器功能时，通过Command.get().getXxx()调用对应命令模块          |
 * |   2. SCPI远程控制指令解析后，通过Command单例执行对应操作                      |
 * |   3. APP启动时，Command订阅LoadCache事件初始化状态                            |
 * +=============================================================================+
 */

public class Command {
    public static final String TAG=Command.class.getSimpleName(); // 类标签，用于日志输出
    //region 单例
    private static class CommandHolder { // 静态内部类实现懒加载单例
        private static final Command instance = new Command(); // 静态常量持有唯一实例
    }

    /**
     * 获取Command单例实例
     * @return Command全局唯一实例
     */
    public static final Command get() {
//        Logger.i("Command.get()");
        return CommandHolder.instance; // 返回静态内部类持有的单例实例
    }
    //endregion

    //region 功能定义
    /**
     * 主菜单 自动页面
     */
    private Command_Auto auto; // 自动设置命令模块
    private Command_Bus bus; // 串行总线通用命令模块
    private Command_Bus_429 bus_429; // ARINC429总线命令模块
    private Command_Bus_1553B bus_1553B; // MIL-STD-1553B总线命令模块
    private Command_Bus_Can bus_can; // CAN总线命令模块
    private Command_Bus_IIC bus_iic; // IIC/I2C总线命令模块
    private Command_Bus_Lin bus_lin; // LIN总线命令模块
    private Command_Bus_Spi bus_spi; // SPI总线命令模块
    private Command_Bus_Uart bus_uart; // UART串口总线命令模块
    private Command_Calibrate calibrate; // 校准命令模块
    private Command_Channel channel; // 通道命令模块
    private Command_Cursor cursor; // 光标命令模块
    private Command_Display display; // 显示命令模块
    private Command_FunctionMenu functionMenu; // 功能菜单命令模块
    private Command_Math math; // 数学运算命令模块
    private Command_Math_Base math_base; // 基础数学运算命令模块
    private Command_Math_AXB math_axb; // AX+B数学运算命令模块
    private Command_Math_Advanced math_advanced; // 高级数学运算命令模块
    private Command_Math_FFT math_fft; // FFT数学运算命令模块
    private Command_Measure measure; // 测量命令模块
    private Command_Measure_Statistic measure_statistic; // 测量统计命令模块
    private Command_Measure_Setting measure_setting; // 测量设置命令模块
    private Command_Menu menu; // 菜单命令模块
    private Command_Reference reference; // 参考波形命令模块
    private Command_Sample sample; // 采样命令模块
    private Command_Storage storage; // 存储命令模块
    private Command_Timebase timebase; // 时基命令模块
    private Command_Trigger trigger; // 触发命令模块
    private Command_Trigger_B trigger_b; // B触发命令模块
    private Command_Trigger_Can trigger_can; // CAN触发命令模块
    private Command_Trigger_Dwart trigger_dwart; // DWART触发命令模块
    private Command_Trigger_Edge trigger_edge; // 边沿触发命令模块
    private Command_Trigger_IIC trigger_iic; // IIC触发命令模块
    private Command_Trigger_Lin trigger_lin; // LIN触发命令模块
    private Command_Trigger_Logic trigger_logic; // 逻辑触发命令模块
    private Command_Trigger_Nedge trigger_nedge; // 非边沿触发命令模块
    private Command_Trigger_Pulse trigger_pulse; // 脉冲触发命令模块
    private Command_Trigger_Runt trigger_runt; // 欠幅触发命令模块
    private Command_Trigger_Setup trigger_setup; // 建立时间触发命令模块
    private Command_Trigger_Slope trigger_slope; // 斜率触发命令模块
    private Command_Trigger_SPI trigger_spi; // SPI触发命令模块
    private Command_Trigger_Timeout trigger_timeout; // 超时触发命令模块
    private Command_Trigger_Uart trigger_uart; // UART触发命令模块
    private Command_Trigger_Video trigger_video; // 视频触发命令模块
    private Command_Waveform waveform; // 波形命令模块
    private Command_Frequency frequency; // 频率计命令模块
    private Command_Userset userset; // 用户设置命令模块
    private Command_Trigger_M429 trigger_m429; // ARINC429触发命令模块
    private Command_Trigger_M1553B trigger_m1553B; // MIL-STD-1553B触发命令模块
    private Command_Production production; // 生产测试命令模块
    private Command_Common common; // 公共命令模块

    private CommandMsgToUI msgToUI = new CommandMsgToUI(); // 命令消息转发到UI的载体实例

    /**
     * 获取自动设置命令模块
     * @return Command_Auto自动设置命令实例
     */
    public Command_Auto getAuto() {
        return auto; // 返回自动设置命令模块实例
    }

    /**
     * 获取串行总线通用命令模块
     * @return Command_Bus总线命令实例
     */
    public Command_Bus getBus() {
        return bus; // 返回串行总线通用命令模块实例
    }

    /**
     * 获取ARINC429总线命令模块
     * @return Command_Bus_429 429总线命令实例
     */
    public Command_Bus_429 getBus_429() {
        return bus_429; // 返回ARINC429总线命令模块实例
    }

    /**
     * 获取MIL-STD-1553B总线命令模块
     * @return Command_Bus_1553B 1553B总线命令实例
     */
    public Command_Bus_1553B getBus_1553B() {
        return bus_1553B; // 返回MIL-STD-1553B总线命令模块实例
    }

    /**
     * 获取CAN总线命令模块
     * @return Command_Bus_Can CAN总线命令实例
     */
    public Command_Bus_Can getBus_can() {
        return bus_can; // 返回CAN总线命令模块实例
    }

    /**
     * 获取IIC/I2C总线命令模块
     * @return Command_Bus_IIC IIC总线命令实例
     */
    public Command_Bus_IIC getBus_iic() {
        return bus_iic; // 返回IIC/I2C总线命令模块实例
    }

    /**
     * 获取LIN总线命令模块
     * @return Command_Bus_Lin LIN总线命令实例
     */
    public Command_Bus_Lin getBus_lin() {
        return bus_lin; // 返回LIN总线命令模块实例
    }

    /**
     * 获取SPI总线命令模块
     * @return Command_Bus_Spi SPI总线命令实例
     */
    public Command_Bus_Spi getBus_spi() {
        return bus_spi; // 返回SPI总线命令模块实例
    }

    /**
     * 获取UART串口总线命令模块
     * @return Command_Bus_Uart UART总线命令实例
     */
    public Command_Bus_Uart getBus_uart() {
        return bus_uart; // 返回UART串口总线命令模块实例
    }

    /**
     * 获取校准命令模块
     * @return Command_Calibrate校准命令实例
     */
    public Command_Calibrate getCalibrate(){return calibrate;} // 返回校准命令模块实例

    /**
     * 获取通道命令模块
     * @return Command_Channel通道命令实例
     */
    public Command_Channel getChannel() {
        return channel; // 返回通道命令模块实例
    }

    /**
     * 获取光标命令模块
     * @return Command_Cursor光标命令实例
     */
    public Command_Cursor getCursor() {
        return cursor; // 返回光标命令模块实例
    }

    /**
     * 获取显示命令模块
     * @return Command_Display显示命令实例
     */
    public Command_Display getDisplay() {
        return display; // 返回显示命令模块实例
    }

    /**
     * 获取功能菜单命令模块
     * @return Command_FunctionMenu功能菜单命令实例
     */
    public Command_FunctionMenu getFunctionMenu() {
        return functionMenu; // 返回功能菜单命令模块实例
    }

    /**
     * 获取数学运算命令模块
     * @return Command_Math数学运算命令实例
     */
    public Command_Math getMath() {
        return math; // 返回数学运算命令模块实例
    }

    /**
     * 获取基础数学运算命令模块
     * @return Command_Math_Base基础数学运算命令实例
     */
    public Command_Math_Base getMath_base(){
        return math_base; // 返回基础数学运算命令模块实例
    }

    /**
     * 获取AX+B数学运算命令模块
     * @return Command_Math_AXB AX+B数学运算命令实例
     */
    public Command_Math_AXB getMath_axb(){return math_axb;} // 返回AX+B数学运算命令模块实例

    /**
     * 获取高级数学运算命令模块
     * @return Command_Math_Advanced高级数学运算命令实例
     */
    public Command_Math_Advanced getMath_advanced() {
        return math_advanced; // 返回高级数学运算命令模块实例
    }

    /**
     * 获取FFT数学运算命令模块
     * @return Command_Math_FFT FFT数学运算命令实例
     */
    public Command_Math_FFT getMath_fft() {
        return math_fft; // 返回FFT数学运算命令模块实例
    }

    /**
     * 获取测量命令模块
     * @return Command_Measure测量命令实例
     */
    public Command_Measure getMeasure() {
        return measure; // 返回测量命令模块实例
    }

    /**
     * 获取测量统计命令模块
     * @return Command_Measure_Statistic测量统计命令实例
     */
    public Command_Measure_Statistic getMeasure_statistic(){ return measure_statistic;} // 返回测量统计命令模块实例

    /**
     * 获取测量设置命令模块
     * @return Command_Measure_Setting测量设置命令实例
     */
    public Command_Measure_Setting getMeasure_setting(){return measure_setting;} // 返回测量设置命令模块实例

    /**
     * 获取菜单命令模块
     * @return Command_Menu菜单命令实例
     */
    public Command_Menu getMenu() {
        return menu; // 返回菜单命令模块实例
    }

    /**
     * 获取参考波形命令模块
     * @return Command_Reference参考波形命令实例
     */
    public Command_Reference getReference() {
        return reference; // 返回参考波形命令模块实例
    }

    /**
     * 获取采样命令模块
     * @return Command_Sample采样命令实例
     */
    public Command_Sample getSample() {
        return sample; // 返回采样命令模块实例
    }

    /**
     * 获取存储命令模块
     * @return Command_Storage存储命令实例
     */
    public Command_Storage getStorage() {
        return storage; // 返回存储命令模块实例
    }

    /**
     * 获取时基命令模块
     * @return Command_Timebase时基命令实例
     */
    public Command_Timebase getTimebase() {
        return timebase; // 返回时基命令模块实例
    }

    /**
     * 获取触发命令模块
     * @return Command_Trigger触发命令实例
     */
    public Command_Trigger getTrigger() {
        return trigger; // 返回触发命令模块实例
    }

    /**
     * 获取B触发命令模块
     * @return Command_Trigger_B B触发命令实例
     */
    public Command_Trigger_B getTrigger_b() {
        return trigger_b; // 返回B触发命令模块实例
    }

    /**
     * 获取CAN触发命令模块
     * @return Command_Trigger_Can CAN触发命令实例
     */
    public Command_Trigger_Can getTrigger_can() {
        return trigger_can; // 返回CAN触发命令模块实例
    }

    /**
     * 获取DWART触发命令模块
     * @return Command_Trigger_Dwart DWART触发命令实例
     */
    public Command_Trigger_Dwart getTrigger_dwart() {
        return trigger_dwart; // 返回DWART触发命令模块实例
    }

    /**
     * 获取边沿触发命令模块
     * @return Command_Trigger_Edge边沿触发命令实例
     */
    public Command_Trigger_Edge getTrigger_edge() {
        return trigger_edge; // 返回边沿触发命令模块实例
    }

    /**
     * 获取IIC触发命令模块
     * @return Command_Trigger_IIC IIC触发命令实例
     */
    public Command_Trigger_IIC getTrigger_iic() {
        return trigger_iic; // 返回IIC触发命令模块实例
    }

    /**
     * 获取LIN触发命令模块
     * @return Command_Trigger_Lin LIN触发命令实例
     */
    public Command_Trigger_Lin getTrigger_lin() {
        return trigger_lin; // 返回LIN触发命令模块实例
    }

    /**
     * 获取逻辑触发命令模块
     * @return Command_Trigger_Logic逻辑触发命令实例
     */
    public Command_Trigger_Logic getTrigger_logic() {
        return trigger_logic; // 返回逻辑触发命令模块实例
    }

    /**
     * 获取非边沿触发命令模块
     * @return Command_Trigger_Nedge非边沿触发命令实例
     */
    public Command_Trigger_Nedge getTrigger_nedge() {
        return trigger_nedge; // 返回非边沿触发命令模块实例
    }

    /**
     * 获取脉冲触发命令模块
     * @return Command_Trigger_Pulse脉冲触发命令实例
     */
    public Command_Trigger_Pulse getTrigger_pulse() {
        return trigger_pulse; // 返回脉冲触发命令模块实例
    }

    /**
     * 获取欠幅触发命令模块
     * @return Command_Trigger_Runt欠幅触发命令实例
     */
    public Command_Trigger_Runt getTrigger_runt() {
        return trigger_runt; // 返回欠幅触发命令模块实例
    }

    /**
     * 获取建立时间触发命令模块
     * @return Command_Trigger_Setup建立时间触发命令实例
     */
    public Command_Trigger_Setup getTrigger_setup() {
        return trigger_setup; // 返回建立时间触发命令模块实例
    }

    /**
     * 获取斜率触发命令模块
     * @return Command_Trigger_Slope斜率触发命令实例
     */
    public Command_Trigger_Slope getTrigger_slope() {
        return trigger_slope; // 返回斜率触发命令模块实例
    }

    /**
     * 获取SPI触发命令模块
     * @return Command_Trigger_SPI SPI触发命令实例
     */
    public Command_Trigger_SPI getTrigger_spi() {
        return trigger_spi; // 返回SPI触发命令模块实例
    }

    /**
     * 获取超时触发命令模块
     * @return Command_Trigger_Timeout超时触发命令实例
     */
    public Command_Trigger_Timeout getTrigger_timeout() {
        return trigger_timeout; // 返回超时触发命令模块实例
    }

    /**
     * 获取UART触发命令模块
     * @return Command_Trigger_Uart UART触发命令实例
     */
    public Command_Trigger_Uart getTrigger_uart() {
        return trigger_uart; // 返回UART触发命令模块实例
    }

    /**
     * 获取视频触发命令模块
     * @return Command_Trigger_Video视频触发命令实例
     */
    public Command_Trigger_Video getTrigger_video() {
        return trigger_video; // 返回视频触发命令模块实例
    }

    /**
     * 获取波形命令模块
     * @return Command_Waveform波形命令实例
     */
    public Command_Waveform getWaveform() {
        return waveform; // 返回波形命令模块实例
    }

    /**
     * 获取频率计命令模块
     * @return Command_Frequency频率计命令实例
     */
    public Command_Frequency getFrequency() {
        return frequency; // 返回频率计命令模块实例
    }

    /**
     * 获取用户设置命令模块
     * @return Command_Userset用户设置命令实例
     */
    public Command_Userset getUserset() {
        return userset; // 返回用户设置命令模块实例
    }

    /**
     * 获取ARINC429触发命令模块
     * @return Command_Trigger_M429 429触发命令实例
     */
    public Command_Trigger_M429 getTrigger_m429() {
        return trigger_m429; // 返回ARINC429触发命令模块实例
    }

    /**
     * 获取MIL-STD-1553B触发命令模块
     * @return Command_Trigger_M1553B 1553B触发命令实例
     */
    public Command_Trigger_M1553B getTrigger_m1553B() {
        return trigger_m1553B; // 返回MIL-STD-1553B触发命令模块实例
    }

    /**
     * 获取生产测试命令模块
     * @return Command_Production生产测试命令实例
     */
    public Command_Production getProduction(){ return production;} // 返回生产测试命令模块实例

    /**
     * 获取公共命令模块
     * @return Command_Common公共命令实例
     */
    public Command_Common getCommon(){ return  common;} // 返回公共命令模块实例
    //endregion

    /**
     * 获取命令消息转发到UI的载体
     * @return CommandMsgToUI消息载体实例
     */
    public CommandMsgToUI getMsgToUI() {
        return msgToUI; // 返回命令消息到UI的载体实例
    }

    /**
     * 构造函数，初始化所有子命令模块实例
     */
    public Command() {
        auto = new Command_Auto(); // 创建自动设置命令模块实例
        bus=new Command_Bus(); // 创建串行总线通用命令模块实例
        bus_429=new Command_Bus_429(); // 创建ARINC429总线命令模块实例
        bus_1553B=new Command_Bus_1553B(); // 创建MIL-STD-1553B总线命令模块实例
        bus_can=new Command_Bus_Can(); // 创建CAN总线命令模块实例
        bus_iic=new Command_Bus_IIC(); // 创建IIC/I2C总线命令模块实例
        bus_lin=new Command_Bus_Lin(); // 创建LIN总线命令模块实例
        bus_spi=new Command_Bus_Spi(); // 创建SPI总线命令模块实例
        bus_uart=new Command_Bus_Uart(); // 创建UART串口总线命令模块实例
        calibrate=new Command_Calibrate(); // 创建校准命令模块实例
        channel = new Command_Channel(); // 创建通道命令模块实例
        cursor = new Command_Cursor(); // 创建光标命令模块实例
        display = new Command_Display(); // 创建显示命令模块实例
        functionMenu = new Command_FunctionMenu(); // 创建功能菜单命令模块实例
        math = new Command_Math(); // 创建数学运算命令模块实例
        math_base=new Command_Math_Base(); // 创建基础数学运算命令模块实例
        math_axb=new Command_Math_AXB(); // 创建AX+B数学运算命令模块实例
        math_advanced = new Command_Math_Advanced(); // 创建高级数学运算命令模块实例
        math_fft = new Command_Math_FFT(); // 创建FFT数学运算命令模块实例
        measure = new Command_Measure(); // 创建测量命令模块实例
        measure_statistic=new Command_Measure_Statistic(); // 创建测量统计命令模块实例
        measure_setting=new Command_Measure_Setting(); // 创建测量设置命令模块实例
        menu = new Command_Menu(); // 创建菜单命令模块实例
        reference = new Command_Reference(); // 创建参考波形命令模块实例
        sample = new Command_Sample(); // 创建采样命令模块实例
        storage = new Command_Storage(); // 创建存储命令模块实例
        timebase = new Command_Timebase(); // 创建时基命令模块实例
        trigger = new Command_Trigger(); // 创建触发命令模块实例
        trigger_b = new Command_Trigger_B(); // 创建B触发命令模块实例
        trigger_can = new Command_Trigger_Can(); // 创建CAN触发命令模块实例
        trigger_dwart = new Command_Trigger_Dwart(); // 创建DWART触发命令模块实例
        trigger_edge = new Command_Trigger_Edge(); // 创建边沿触发命令模块实例
        trigger_iic = new Command_Trigger_IIC(); // 创建IIC触发命令模块实例
        trigger_lin = new Command_Trigger_Lin(); // 创建LIN触发命令模块实例
        trigger_logic = new Command_Trigger_Logic(); // 创建逻辑触发命令模块实例
        trigger_nedge = new Command_Trigger_Nedge(); // 创建非边沿触发命令模块实例
        trigger_pulse = new Command_Trigger_Pulse(); // 创建脉冲触发命令模块实例
        trigger_runt = new Command_Trigger_Runt(); // 创建欠幅触发命令模块实例
        trigger_setup = new Command_Trigger_Setup(); // 创建建立时间触发命令模块实例
        trigger_slope = new Command_Trigger_Slope(); // 创建斜率触发命令模块实例
        trigger_spi = new Command_Trigger_SPI(); // 创建SPI触发命令模块实例
        trigger_timeout = new Command_Trigger_Timeout(); // 创建超时触发命令模块实例
        trigger_uart = new Command_Trigger_Uart(); // 创建UART触发命令模块实例
        trigger_video = new Command_Trigger_Video(); // 创建视频触发命令模块实例
        waveform = new Command_Waveform(); // 创建波形命令模块实例
        frequency = new Command_Frequency(); // 创建频率计命令模块实例
        userset = new Command_Userset(); // 创建用户设置命令模块实例
        trigger_m429 = new Command_Trigger_M429(); // 创建ARINC429触发命令模块实例
        trigger_m1553B = new Command_Trigger_M1553B(); // 创建MIL-STD-1553B触发命令模块实例
        production=new Command_Production(); // 创建生产测试命令模块实例
        common=new Command_Common(); // 创建公共命令模块实例
    }

    /**
     * 初始化方法，订阅RxBus缓存加载事件
     */
    public void init(){
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅主缓存加载事件，收到事件后执行consumerLoadCache
    }

    /**
     * 缓存加载事件消费者，收到LoadCache事件后调用setCache初始化命令状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache(); // 收到缓存加载事件，执行缓存状态设置
        }
    };
    /**
     *  当APP启动的时候，进行初始化一下所有类中变量的值。
     *  只设置，不配置FPGA，当加载完成后，再进行一次性的配置FPGA。
     *  在所有命令类完成后，将补全。
     *  **/
    /**
     * 设置缓存加载状态，标记命令模块已加载完成
     */
    public void setCache(){

           CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_Command,true); // 标记命令模块缓存加载完成

    }



}
