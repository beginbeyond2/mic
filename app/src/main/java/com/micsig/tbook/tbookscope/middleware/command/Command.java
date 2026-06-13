package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.util.CacheUtil;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


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

public class Command {
    public static final String TAG=Command.class.getSimpleName();
    //region 单例
    private static class CommandHolder {
        private static final Command instance = new Command();
    }

    public static final Command get() {
//        Logger.i("Command.get()");
        return CommandHolder.instance;
    }
    //endregion

    //region 功能定义
    /**
     * 主菜单 自动页面
     */
    private Command_Auto auto;
    private Command_Bus bus;
    private Command_Bus_429 bus_429;
    private Command_Bus_1553B bus_1553B;
    private Command_Bus_Can bus_can;
    private Command_Bus_IIC bus_iic;
    private Command_Bus_Lin bus_lin;
    private Command_Bus_Spi bus_spi;
    private Command_Bus_Uart bus_uart;
    private Command_Calibrate calibrate;
    private Command_Channel channel;
    private Command_Cursor cursor;
    private Command_Display display;
    private Command_FunctionMenu functionMenu;
    private Command_Math math;
    private Command_Math_Base math_base;
    private Command_Math_AXB math_axb;
    private Command_Math_Advanced math_advanced;
    private Command_Math_FFT math_fft;
    private Command_Measure measure;
    private Command_Measure_Statistic measure_statistic;
    private Command_Measure_Setting measure_setting;
    private Command_Menu menu;
    private Command_Reference reference;
    private Command_Sample sample;
    private Command_Storage storage;
    private Command_Timebase timebase;
    private Command_Trigger trigger;
    private Command_Trigger_B trigger_b;
    private Command_Trigger_Can trigger_can;
    private Command_Trigger_Dwart trigger_dwart;
    private Command_Trigger_Edge trigger_edge;
    private Command_Trigger_IIC trigger_iic;
    private Command_Trigger_Lin trigger_lin;
    private Command_Trigger_Logic trigger_logic;
    private Command_Trigger_Nedge trigger_nedge;
    private Command_Trigger_Pulse trigger_pulse;
    private Command_Trigger_Runt trigger_runt;
    private Command_Trigger_Setup trigger_setup;
    private Command_Trigger_Slope trigger_slope;
    private Command_Trigger_SPI trigger_spi;
    private Command_Trigger_Timeout trigger_timeout;
    private Command_Trigger_Uart trigger_uart;
    private Command_Trigger_Video trigger_video;
    private Command_Waveform waveform;
    private Command_Frequency frequency;
    private Command_Userset userset;
    private Command_Trigger_M429 trigger_m429;
    private Command_Trigger_M1553B trigger_m1553B;
    private Command_Production production;
    private Command_Common common;

    private CommandMsgToUI msgToUI = new CommandMsgToUI();

    public Command_Auto getAuto() {
        return auto;
    }

    public Command_Bus getBus() {
        return bus;
    }

    public Command_Bus_429 getBus_429() {
        return bus_429;
    }

    public Command_Bus_1553B getBus_1553B() {
        return bus_1553B;
    }

    public Command_Bus_Can getBus_can() {
        return bus_can;
    }

    public Command_Bus_IIC getBus_iic() {
        return bus_iic;
    }

    public Command_Bus_Lin getBus_lin() {
        return bus_lin;
    }

    public Command_Bus_Spi getBus_spi() {
        return bus_spi;
    }

    public Command_Bus_Uart getBus_uart() {
        return bus_uart;
    }

    public Command_Calibrate getCalibrate(){return calibrate;}

    public Command_Channel getChannel() {
        return channel;
    }

    public Command_Cursor getCursor() {
        return cursor;
    }

    public Command_Display getDisplay() {
        return display;
    }

    public Command_FunctionMenu getFunctionMenu() {
        return functionMenu;
    }

    public Command_Math getMath() {
        return math;
    }

    public Command_Math_Base getMath_base(){
        return math_base;
    }

    public Command_Math_AXB getMath_axb(){return math_axb;}

    public Command_Math_Advanced getMath_advanced() {
        return math_advanced;
    }

    public Command_Math_FFT getMath_fft() {
        return math_fft;
    }

    public Command_Measure getMeasure() {
        return measure;
    }

    public Command_Measure_Statistic getMeasure_statistic(){ return measure_statistic;}
    public Command_Measure_Setting getMeasure_setting(){return measure_setting;}

    public Command_Menu getMenu() {
        return menu;
    }

    public Command_Reference getReference() {
        return reference;
    }

    public Command_Sample getSample() {
        return sample;
    }

    public Command_Storage getStorage() {
        return storage;
    }

    public Command_Timebase getTimebase() {
        return timebase;
    }

    public Command_Trigger getTrigger() {
        return trigger;
    }

    public Command_Trigger_B getTrigger_b() {
        return trigger_b;
    }

    public Command_Trigger_Can getTrigger_can() {
        return trigger_can;
    }

    public Command_Trigger_Dwart getTrigger_dwart() {
        return trigger_dwart;
    }

    public Command_Trigger_Edge getTrigger_edge() {
        return trigger_edge;
    }

    public Command_Trigger_IIC getTrigger_iic() {
        return trigger_iic;
    }

    public Command_Trigger_Lin getTrigger_lin() {
        return trigger_lin;
    }

    public Command_Trigger_Logic getTrigger_logic() {
        return trigger_logic;
    }

    public Command_Trigger_Nedge getTrigger_nedge() {
        return trigger_nedge;
    }

    public Command_Trigger_Pulse getTrigger_pulse() {
        return trigger_pulse;
    }

    public Command_Trigger_Runt getTrigger_runt() {
        return trigger_runt;
    }

    public Command_Trigger_Setup getTrigger_setup() {
        return trigger_setup;
    }

    public Command_Trigger_Slope getTrigger_slope() {
        return trigger_slope;
    }

    public Command_Trigger_SPI getTrigger_spi() {
        return trigger_spi;
    }

    public Command_Trigger_Timeout getTrigger_timeout() {
        return trigger_timeout;
    }

    public Command_Trigger_Uart getTrigger_uart() {
        return trigger_uart;
    }

    public Command_Trigger_Video getTrigger_video() {
        return trigger_video;
    }

    public Command_Waveform getWaveform() {
        return waveform;
    }

    public Command_Frequency getFrequency() {
        return frequency;
    }

    public Command_Userset getUserset() {
        return userset;
    }

    public Command_Trigger_M429 getTrigger_m429() {
        return trigger_m429;
    }

    public Command_Trigger_M1553B getTrigger_m1553B() {
        return trigger_m1553B;
    }
    public Command_Production getProduction(){ return production;}
    public Command_Common getCommon(){ return  common;}
    //endregion

    public CommandMsgToUI getMsgToUI() {
        return msgToUI;
    }

    public Command() {
        auto = new Command_Auto();
        bus=new Command_Bus();
        bus_429=new Command_Bus_429();
        bus_1553B=new Command_Bus_1553B();
        bus_can=new Command_Bus_Can();
        bus_iic=new Command_Bus_IIC();
        bus_lin=new Command_Bus_Lin();
        bus_spi=new Command_Bus_Spi();
        bus_uart=new Command_Bus_Uart();
        calibrate=new Command_Calibrate();
        channel = new Command_Channel();
        cursor = new Command_Cursor();
        display = new Command_Display();
        functionMenu = new Command_FunctionMenu();
        math = new Command_Math();
        math_base=new Command_Math_Base();
        math_axb=new Command_Math_AXB();
        math_advanced = new Command_Math_Advanced();
        math_fft = new Command_Math_FFT();
        measure = new Command_Measure();
        measure_statistic=new Command_Measure_Statistic();
        measure_setting=new Command_Measure_Setting();
        menu = new Command_Menu();
        reference = new Command_Reference();
        sample = new Command_Sample();
        storage = new Command_Storage();
        timebase = new Command_Timebase();
        trigger = new Command_Trigger();
        trigger_b = new Command_Trigger_B();
        trigger_can = new Command_Trigger_Can();
        trigger_dwart = new Command_Trigger_Dwart();
        trigger_edge = new Command_Trigger_Edge();
        trigger_iic = new Command_Trigger_IIC();
        trigger_lin = new Command_Trigger_Lin();
        trigger_logic = new Command_Trigger_Logic();
        trigger_nedge = new Command_Trigger_Nedge();
        trigger_pulse = new Command_Trigger_Pulse();
        trigger_runt = new Command_Trigger_Runt();
        trigger_setup = new Command_Trigger_Setup();
        trigger_slope = new Command_Trigger_Slope();
        trigger_spi = new Command_Trigger_SPI();
        trigger_timeout = new Command_Trigger_Timeout();
        trigger_uart = new Command_Trigger_Uart();
        trigger_video = new Command_Trigger_Video();
        waveform = new Command_Waveform();
        frequency = new Command_Frequency();
        userset = new Command_Userset();
        trigger_m429 = new Command_Trigger_M429();
        trigger_m1553B = new Command_Trigger_M1553B();
        production=new Command_Production();
        common=new Command_Common();
    }

    public void init(){
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
        }
    };
    /**
     *  当APP启动的时候，进行初始化一下所有类中变量的值。
     *  只设置，不配置FPGA，当加载完成后，再进行一次性的配置FPGA。
     *  在所有命令类完成后，将补全。
     *  **/
    public void setCache(){

           CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_Command,true);

    }



}
