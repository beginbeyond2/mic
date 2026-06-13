package com.micsig.tbook.scope.channel;

import android.os.SystemClock;
import android.util.Log;

import com.micsig.base.DoubleUtil;
import com.micsig.base.Logger;
import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Data.DataFactory;
import com.micsig.tbook.scope.Data.IDataBuffer;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.ScopeFrozen;
import com.micsig.tbook.scope.Trigger.TriggerLevel;
import com.micsig.tbook.scope.probe.BaseProbe;
import com.micsig.tbook.scope.vertical.VerticalAxis;

/**
 * 通道类，用于管理示波器通道的各项属性和操作,1
 * 包括垂直档位、耦合方式、带宽限制、阻抗匹配、探头设置等功能,1
 */
public class Channel extends BaseChannel {
    private static final String TAG = "Channel"; // 日志标签,1

    public static final int COUPLE_TYPE_GND = 0; // 接地耦合类型,1
    public static final int COUPLE_TYPE_DC = 1; // 直流耦合类型,1
    public static final int COUPLE_TYPE_AC = 2; // 交流耦合类型,1


    //这个posFix和pos略有不同，主要在小档位时会有差异。这是因为硬件的精度有限，导致需要用posFix来补充。posFix
    //为硬件的实际通道位置，而pos为界面上体现的通道位置。
    private volatile double posFix = 0; // 硬件实际通道位置修正值,1

    public static final int BANDWIDTH_TYPE_FULL = 0; // 全带宽类型,1
    public static final int BANDWIDTH_TYPE_300M = 1; // 300MHz带宽类型,1
    public static final int BANDWIDTH_TYPE_200M = 2; // 200MHz带宽类型,1
    public static final int BANDWIDTH_TYPE_20M = 3; // 20MHz带宽类型,1
    public static final int BANDWIDTH_TYPE_HIGHPASS = 4; // 高通滤波带宽类型,1
    public static final int BANDWIDTH_TYPE_LOWPASS = 5; // 低通滤波带宽类型,1

    public static final int RESISTANCE_1M = 0; // 1M欧姆阻抗类型,1
    public static final int RESISTANCE_50 = 1; // 50欧姆阻抗类型,1



    private boolean bInvert = false;//反相
    private int CoupleType = COUPLE_TYPE_DC;//耦合方式
    private volatile int resistanceType = RESISTANCE_1M; // 阻抗类型,1
    private int BandWidthType = BANDWIDTH_TYPE_FULL;//带宽类型


    public static final long MAX_BANDWIDTH = HardwareProduct.isMHO38V1() ? 500*1000*1000L : 1000*1000*1000L; // 最大带宽值，根据硬件型号确定,1

    private double [] BandWidth = {MAX_BANDWIDTH,300e6,200e6,20e6,1.0,1.0}; //带宽

    private volatile int vMin = -350; // 电压最小值,1
    private volatile int vMax = 350; // 电压最大值,1

    private int placeVal = 0; // 位置值,1

    private double adPix = 1; // AD像素值,1
    private double busPrimaryLevel = 0; // 总线主电平,1
    private double busSecondaryLevel = 0; // 总线从电平,1

    private int m; // M参数,1
    private int n; // N参数,1
    //private int bc;
    private volatile boolean bSample = false; // 采样标志,1

    private TriggerLevel [] triggerLevels = new TriggerLevel[TriggerLevel.TRIGGER_LEVEL_MAX]; // 触发电平数组,1
    private VerticalAxis verticalAxis; // 垂直轴对象,1
    private ChannelAction channelAction; // 通道动作对象,1


    /**
     * 设置探头,1
     * @param baseProbe 基础探头对象，null表示拔出探头,1
     */
    public void setProbe(BaseProbe baseProbe){
        if (null==baseProbe && verticalAxis.getProbe()!=null){ // 探头拔出情况,1
            EventFactory.sendEvent(new EventBase(EventFactory.EVENT_PROBE_UNPLUG,getChId()),true); // 发送探头拔出事件,1
        }else if (null!=baseProbe && verticalAxis.getProbe()==null) { // 探头插入情况,1
            EventFactory.sendEvent(new EventBase(EventFactory.EVENT_PROBE_PLUG,getChId()), true); // 发送探头插入事件,1
        }
        verticalAxis.setProbe(baseProbe); // 设置垂直轴的探头,1
        channelAction.probeChange(); // 触发探头变化动作,1
    }

    /**
     * 获取探头对象,1
     * @return 基础探头对象,1
     */
    public BaseProbe getProbe(){
        return verticalAxis.getProbe(); // 返回垂直轴的探头,1
    }

    /**
     * 构造函数，初始化通道,1
     * @param idx 通道索引号,1
     */
    public Channel(int idx) {
        super(idx,"CH" + (idx + 1)); // 调用父类构造函数，设置通道ID和名称,1
        verticalAxis = new VerticalAxis(); // 创建垂直轴对象,1
        channelAction = new ChannelAction(this); // 创建通道动作对象,1
        for(int i=TriggerLevel.TRIGGER_LEVEL_NORMAL;i<TriggerLevel.TRIGGER_LEVEL_MAX;i++){ // 初始化触发电平数组,1
            triggerLevels[i] = new TriggerLevel(i); // 创建每个触发电平对象,1
        }
        for (int i=0;i<channelZeroPos.length;i++){ // 初始化通道零位数组,1
            channelZeroPos[i] = 0; // 设置初始零位为0,1
        }
        setBuffer(DataFactory.allocateBufferQueue(false)); // 分配数据缓冲区,1
    }

    /**
     * 获取指定类型的触发电平,1
     * @param triggerLevelType 触发电平类型,1
     * @return 触发电平对象，如果类型无效则返回null,1
     */
    public TriggerLevel getTriggerLevel(int triggerLevelType){
        if(TriggerLevel.isTriggerLevelVaild(triggerLevelType)) // 检查触发电平类型是否有效,1
            return triggerLevels[triggerLevelType]; // 返回对应的触发电平对象,1
        return null; // 类型无效返回null,1
    }

    /**
     * 获取电压最小值,1
     * @return 电压最小值,1
     */
    public int getVMin() {
        return vMin; // 返回电压最小值,1
    }

    /**
     * 获取电压最大值,1
     * @return 电压最大值,1
     */
    public int getVMax() {
        return vMax; // 返回电压最大值,1
    }

    /**
     * 设置电压范围,1
     * @param min 电压最小值,1
     * @param max 电压最大值,1
     */
    public void setVRange(int min, int max) {
        vMin = min; // 设置电压最小值,1
        vMax = max; // 设置电压最大值,1
    }
    /**
     * 检查并校正位置,1
     */
    public void checkPos(){
        setChOffsetVal(chOffsetVal * getProbeRate()); // 设置通道偏移值，考虑探头倍率,1
        if(getVerticalMode() == VERTICAL_MODE_CH_ZERO){ // 如果垂直模式为通道零位模式,1
            setPos(getPosUI()); // 设置位置为UI位置,1
        }
    }


//    public void setPos(double pos) {
//        setPos(pos,false);
//    }

    /**
     * 设置位置（带延迟选项）,1
     * @param pos 位置值（像素单位的正负偏移量）,1
     * @param delay 是否延迟执行,1
     */
    public void setPos(double pos, boolean delay) {//这里传递过来的是 正负偏移量的像素值
        pos = pos * ScopeBase.getToFPGACoff();//转换成1000对应的偏移量
        double v = getZero() + getChOffset() * ScopeBase.getToFPGACoff() + pos; // 计算实际电压值,1
        if (v >= vMin && v <= vMax) { // 检查电压是否在有效范围内,1
            super.setPos(pos); // 调用父类设置位置,1
        }
        channelAction.pos((int) Math.round(getPos()), delay); // 触发位置变化动作,1
    }

    /**
     * 设置位置（无延迟）,1
     * @param pos 位置值（像素单位的正负偏移量）,1
     */
    public void setPos(double pos){
        pos = pos * ScopeBase.getToFPGACoff(); // 转换为FPGA系数,1
        double zero = getZero() + getChOffset() * ScopeBase.getToFPGACoff(); // 计算零位和偏移,1
        if(pos + zero < vMin){ // 检查是否小于最小电压,1
            pos = vMin - zero; // 限制为最小值,1
        }else if(pos + zero > vMax){ // 检查是否大于最大电压,1
            pos = vMax - zero; // 限制为最大值,1
        }
        super.setPos(pos); // 调用父类设置位置,1
        channelAction.pos((int) Math.round(getPos()),false); // 触发位置变化动作，无延迟,1
    }

    /**
     * 获取移动零位值,1
     * @return 移动零位值（每格电压单位）,1
     */
    public double getMoveZeroVal(){
        return  (double)getZero()/ScopeBase.getVerticalPerGridPixels(); // 将零位转换为每格电压单位,1
    }

    /**
     * 设置零位,1
     * @param pos 零位位置（像素单位）,1
     */
    public void setZero(int pos){
        if (DoubleUtil.FuzzyCompare(getFineScale(),1.0)) { // 如果精细档位为1.0,1
            if (Math.abs(pos) <= ScopeBase.getHeight() / 2) { // 检查位置是否在屏幕范围内,1
                boolean bMoveZero = false; // 是否移动零位标志,1
                synchronized (this) { // 同步块，保证线程安全,1
                    int vScaleId = this.getVScaleId(); // 获取当前电压档位ID,1
                    if(channelZeroPos[vScaleId]  != pos) { // 如果零位发生变化,1
                        channelZeroPos[this.getVScaleId()] = pos; // 更新零位数组,1
                        bMoveZero = true; // 设置移动零位标志,1
                    }
                }
                if(bMoveZero) { // 如果需要移动零位,1
                    channelAction.movezero(); // 触发移动零位动作,1
                }
            }
        }
    }

    /**
     * 设置指定档位的零位,1
     * @param vDang 电压档位ID,1
     * @param pos 零位位置（像素单位）,1
     */
    public synchronized void setZero(int vDang,int pos){

        channelZeroPos[vDang] = pos; // 设置指定档位的零位,1
    }
    /**
     * 判断是否为精细档位扩展状态,1
     * @return true表示是精细档位扩展，false表示不是,1
     */
    public boolean isFineExtent(){
        return !DoubleUtil.FuzzyCompare(getFineScale(),1.0); // 精细档位不等于1.0时为扩展状态,1
    }
    /**
     * 获取零位值,1
     * @return 零位值（像素单位）,1
     */
    public synchronized int getZero(){
        if (DoubleUtil.FuzzyCompare(getFineScale(),1.0)) { // 如果精细档位为1.0,1
            return channelZeroPos[this.getVScaleId()]; // 返回当前档位的零位,1
        }else{ // 否则,1
            return 0; // 返回0,1
        }
    }



    /**
     * 设置位置修正值,1
     * @param pos 位置修正值,1
     */
    public void setPosFix(double pos){
        synchronized (this) { // 同步块，保证线程安全,1
            this.posFix = pos; // 设置位置修正值,1
        }
    }

    private int [] channelZeroPos = new int[VerticalAxis.DANG_CNT]; // 通道零位数组，每个档位一个零位,1

    private volatile double chOffsetVal = 0; // 通道偏移值,1
    /**
     * 设置通道偏移值,1
     * @param val 偏移值（电压单位）,1
     */
    public void setChOffsetVal(double val){
        double v = getChOffset(val/getProbeRate());//计算出设置的偏移量 等于 多少个像素
        this.chOffsetVal = v * getADVerticalPerPix()/getProbeRate();//根据像素个数、档位、探针倍数 计算出实际的偏移值
        Log.d(TAG, "setChOffsetVal() called with: val = [" + val + "]" + " ,Pix= " + v + " ,chOffsetVal= " + chOffsetVal); // 打印调试日志,1
        channelAction.pos((int) Math.round(getPos()),false); // 触发位置变化动作,1
        channelAction.offsetChange(); // 触发偏移变化动作,1
    }

    /**
     * 获取通道偏移（像素单位）,1
     * @param val 偏移值（电压单位）,1
     * @return 偏移值（像素单位）,1
     */
    private double getChOffset(double val){ //实际对应的偏移像素
        double v = val * getProbeRate() / getADVerticalPerPix() * ScopeBase.getToFPGACoff(); // 计算像素值,1
        if(v < vMin){ // 检查是否小于最小值,1
            v = vMin; // 限制为最小值,1
        }else if(v > vMax){ // 检查是否大于最大值,1
            v = vMax; // 限制为最大值,1
        }
        return v * ScopeBase.getToUICoff(); // 转换为UI坐标,1
    }

    /**
     * 获取通道偏移值,1
     * @return 通道偏移值（像素单位）,1
     */
    public double getChOffset(){//偏移量 实际的偏移像素
        return getChOffset(this.chOffsetVal); // 返回当前偏移值的像素表示,1
    }

    /**
     * 获取通道偏移值（电压单位）,1
     * @return 通道偏移值（电压单位）,1
     */
    public double getChOffsetVal(){// 偏移的幅值
        return getChOffset() * getADVerticalPerPix(); // 将像素值转换为电压值,1
    }


    /**
     * 获取位置修正值,1
     * @return 位置修正值,1
     */
    public double getPosFix(){
        synchronized (this) { // 同步块，保证线程安全,1
            return posFix; // 返回位置修正值,1
        }
    }

    /**
     * 设置采样状态,1
     * @param bSample true表示开启采样，false表示关闭采样,1
     */
    public void setSample(boolean bSample) {
        if (bSample) { // 如果开启采样,1
            if (!isSample()) { // 如果当前未采样,1
                channelAction.openSample(); // 触发开启采样动作,1
            }
        } else { // 如果关闭采样,1
            if (isSample()) { // 如果当前正在采样,1
                channelAction.closeSample(); // 触发关闭采样动作,1
            }
        }
        this.bSample = bSample; // 设置采样标志,1
        // Logger.d("Channel","Ch" + (getChId()+1) + ", Sample = " + bSample);
    }

    /**
     * 判断是否正在采样,1
     * @return true表示正在采样，false表示未采样,1
     */
    public boolean isSample() {
        return this.bSample; // 返回采样标志,1
    }


    int delay = 0; // 延迟值,1

    //ps 为单位
    /**
     * 获取延迟值,1
     * @return 延迟值（皮秒单位）,1
     */
    public int getDelay(){
        return delay; // 返回延迟值,1
    }

    //ps 为单位
    //public static final int MAX_DELAY_PS = 100*1000;//100ns
    public static final int MAX_DELAY_PS = 500*1000;//100ns // 最大延迟值（500纳秒）,1
    /**
     * 设置延迟值,1
     * @param delay 延迟值（皮秒单位），范围为-MAX_DELAY_PS到MAX_DELAY_PS,1
     * @throws IllegalArgumentException 当延迟值超出范围时抛出,1
     */
    public void setDelay(int delay){
        if(-MAX_DELAY_PS <= delay && delay <= MAX_DELAY_PS) { // 检查延迟值是否在有效范围内,1
            this.delay = delay; // 设置延迟值,1
            channelAction.changeDelay(); // 触发延迟变化动作,1
        }else{ // 否则,1
            throw new IllegalArgumentException(); // 抛出非法参数异常,1
        }
    }

    @Override
    public void open() {
        setOpen(true); // 设置打开标志,1
        channelAction.open(); // 触发打开通道动作,1
    }

    @Override
    public void close() {
        setOpen(false); // 设置关闭标志,1
        channelAction.close(); // 触发关闭通道动作,1
    }

    @Override
    public void activate() {
        if (isOpen()) { // 如果通道已打开,1
            channelAction.active(); // 触发激活通道动作,1
        }
    }


    /**
     * 判断是否反相,1
     * @return true表示反相，false表示不反相,1
     */
    public synchronized boolean isInvert() {
        return bInvert; // 返回反相标志,1
    }

    /**
     * 设置反相状态,1
     * @param bInvert true表示反相，false表示不反相,1
     */
    public void setInvert(boolean bInvert) {
        synchronized (this) { // 同步块，保证线程安全,1
            this.bInvert = bInvert; // 设置反相标志,1
        }
        channelAction.InvertChange(); // 触发反相变化动作,1
    }

    /**
     * 获取耦合类型,1
     * @return 耦合类型（COUPLE_TYPE_GND/DC/AC）,1
     */
    public int getCoupleType() {
        return CoupleType; // 返回耦合类型,1
    }

    /**
     * 设置耦合类型,1
     * @param coupleType 耦合类型（COUPLE_TYPE_GND/DC/AC）,1
     */
    public void setCoupleType(int coupleType) {
        CoupleType = coupleType; // 设置耦合类型,1
        channelAction.CoupleTypeChange(); // 触发耦合类型变化动作,1
    }

    /**
     * 判断是否为自动探头,1
     * @return true表示自动探头，false表示手动探头,1
     */
    public boolean isAutoProbe(){
        return verticalAxis.isAutoProbe(); // 返回垂直轴的自动探头标志,1
    }

    /**
     * 获取探头类型,1
     * @return 探头类型,1
     */
    public int getProbeType() {
        return verticalAxis.getProbeType(); // 返回垂直轴的探头类型,1
    }

    /**
     * 设置探头类型,1
     * @param probeType 探头类型,1
     */
    public void setProbeType(int probeType) {
        verticalAxis.setProbeType(probeType); // 设置垂直轴的探头类型,1
        channelAction.ProbeRateChange(); // 触发探头倍率变化动作,1
    }

    /**
     * 设置探头字符串描述,1
     * @param probeStr 探头字符串描述,1
     */
    public void setProbeStr(String probeStr){
        verticalAxis.setProbeStr(probeStr); // 设置垂直轴的探头字符串,1
    }

    /**
     * 获取探头字符串描述,1
     * @return 探头字符串描述,1
     */
    public String getProbeStr(){
        return verticalAxis.getProbeStr(); // 返回垂直轴的探头字符串,1
    }

    /**
     * 获取探头倍率,1
     * @return 探头倍率,1
     */
    public double getProbeRate() {
        return verticalAxis.getProbeRate(); // 返回垂直轴的探头倍率,1
    }

    /**
     * 设置探头倍率,1
     * @param probeRate 探头倍率,1
     */
    public void setProbeRate(double probeRate) {
        verticalAxis.setProbeRate(probeRate); // 设置垂直轴的探头倍率,1
        channelAction.ProbeRateChange(); // 触发探头倍率变化动作,1
    }
    /**
     * 设置探头DA值,1
     * @param val DA值,1
     */
    public void setProbeDaVal(int val){
        BaseProbe baseProbe = getProbe(); // 获取探头对象,1
        if(baseProbe != null && baseProbe.isDa()){ // 如果探头存在且支持DA,1
            baseProbe.setDaValue(val); // 设置探头的DA值,1
        }
    }
    /**
     * 改变探头DA,1
     */
    public void changeProbeDa(){
        channelAction.changeProbeDa(); // 触发改变探头DA动作,1
    }

    /**
     * 获取带宽类型,1
     * @return 带宽类型,1
     */
    public int getBandWidthType() {
        return getBandWidthType(BandWidthType); // 返回当前带宽类型,1
    }

    /**
     * 获取指定带宽类型,1
     * @param BandWidthType 带宽类型索引,1
     * @return 带宽类型,1
     */
    public int getBandWidthType(int BandWidthType){
        int bwType = BandWidthType; // 带宽类型变量,1
        if(BandWidthType == BANDWIDTH_TYPE_FULL) { // 如果是全带宽类型,1
            double bw = getBandWidth(BANDWIDTH_TYPE_FULL); // 获取全带宽值,1
            for(int i=0;i<BANDWIDTH_TYPE_20M;i++){ // 遍历带宽类型,1
                if(bw > (BandWidth[i] - 10)){ // 如果带宽大于当前类型阈值,1
                    bwType = i; // 设置带宽类型为当前索引,1
                    break; // 跳出循环,1
                }
            }
        }
        return bwType; // 返回带宽类型,1
    }

    /**
     * 带宽类型变化通知,1
     */
    public void bandWidthTypeChange(){
        channelAction.BandWidthTypeChange(); // 触发带宽类型变化动作,1
    }
    /**
     * 设置带宽类型和带宽值,1
     * @param bandWidthType 带宽类型,1
     * @param bandWidth 带宽值,1
     */
    public void setBandWidthType(int bandWidthType,double bandWidth) {
        if(bandWidthType == BANDWIDTH_TYPE_FULL){ // 如果是全带宽类型,1
            if(bandWidth < getMaxBandWidth()){ // 如果带宽小于最大带宽,1
                bandWidthType = BANDWIDTH_TYPE_LOWPASS; // 设置为低通类型,1
            }
        }
        if(bandWidthType == BANDWIDTH_TYPE_FULL){ // 如果是全带宽类型,1
            bandWidth = maxBandWidth; // 设置为最大带宽,1
        }
        BandWidthType = bandWidthType; // 设置带宽类型,1
        BandWidth[BandWidthType] = bandWidth; // 设置对应类型的带宽值,1
        channelAction.BandWidthTypeChange(); // 触发带宽类型变化动作,1
    }

    /**
     * 获取指定类型的带宽值,1
     * @param BandWidthType 带宽类型,1
     * @return 带宽值,1
     */
    public double getBandWidth(int BandWidthType){
        double bw = BandWidth[BandWidthType]; // 获取带宽值,1
        if(bw > maxBandWidth){ // 如果带宽大于最大带宽,1
            bw = maxBandWidth; // 限制为最大带宽,1
        }
        return bw; // 返回带宽值,1
    }
    /**
     * 获取当前带宽值,1
     * @return 带宽值,1
     */
    public double getBandWidth() {
        return getBandWidth(BandWidthType); // 返回当前带宽类型的带宽值,1
    }

    public static double maxBandWidth = 70e6; // 最大带宽值（70MHz）,1
    /**
     * 设置最大带宽值,1
     * @param bandWidth 最大带宽值,1
     */
    public static void setMaxBandWidth(double bandWidth){
        maxBandWidth = bandWidth; // 设置最大带宽值,1
    }
    /**
     * 获取最大带宽值,1
     * @return 最大带宽值,1
     */
    public static double getMaxBandWidth(){
        return maxBandWidth; // 返回最大带宽值,1
    }

    /**
     * 获取最小档位,1
     * @return 最小档位ID,1
     */
    public int getMinGear() {
        return VerticalAxis.getMinGear(); // 返回垂直轴的最小档位,1
    }

    /**
     * 获取最大档位,1
     * @return 最大档位ID,1
     */
    public int getMaxGear() {
        return getResistanceType() == RESISTANCE_50 ? // 根据阻抗类型判断,1
                HardwareProduct.isMHO68V1() ? // 如果是MHO68V1硬件,1
                        (isEnable_50O_700mV() ? VerticalAxis.DANG_1V : VerticalAxis.DANG_500mV): // 根据700mV使能状态返回不同档位,1
                        VerticalAxis.DANG_1V // 否则返回1V档位,1
                : // 否则,1
                VerticalAxis.getMaxGear(); // 返回垂直轴的最大档位,1
    }

    boolean bEnable_50O_700mV = false; // 50欧姆700mV使能标志,1
    /**
     * 设置50欧姆700mV使能状态,1
     * @param bEnable_50O_700mV true表示使能，false表示禁用,1
     */
    public synchronized void set50O_700mV(boolean bEnable_50O_700mV){
        this.bEnable_50O_700mV = bEnable_50O_700mV; // 设置50欧姆700mV使能标志,1
    }
    /**
     * 判断50欧姆700mV是否使能,1
     * @return true表示使能，false表示禁用,1
     */
    public synchronized boolean isEnable_50O_700mV(){
        return bEnable_50O_700mV; // 返回50欧姆700mV使能标志,1
    }


    int scaleIdBak = -1; // 档位备份值,1
    private volatile long  resistanceTS = 0; // 阻抗切换时间戳,1
    /**
     * 获取阻抗切换时间戳,1
     * @return 阻抗切换时间戳,1
     */
    public long getResistanceTS(){
        return resistanceTS; // 返回阻抗切换时间戳,1
    }
    /**
     * 设置阻抗类型,1
     * @param resistanceType 阻抗类型（RESISTANCE_1M或RESISTANCE_50）,1
     */
    public void setResistanceType(int resistanceType) {
        Logger.d(TAG, "调整2-----------setResistanceType resistanceType= " + resistanceType); // 打印调试日志,1
        if(resistanceType == RESISTANCE_50){ // 如果设置为50欧姆,1
            resistanceTS = SystemClock.elapsedRealtime(); // 记录时间戳,1
        }else{ // 否则,1
            resistanceTS = 0; // 清空时间戳,1
        }
        synchronized (this) { // 同步块，保证线程安全,1
            this.resistanceType = resistanceType; // 设置阻抗类型,1
        }
        channelAction.ResistanceTypeChange(); // 触发阻抗类型变化动作,1
        if(!isValidScaleIdExt(verticalAxis.getScaleId())){ // 如果当前档位无效,1
            scaleIdBak = verticalAxis.getScaleId(); // 备份当前档位,1
//            setVScaleId(VerticalAxis.DANG_1V, 1.0,true, false);
            if(isEnable_50O_700mV()){ // 如果50欧姆700mV使能,1
                setVScaleId(VerticalAxis.DANG_1V, 0.7,true, false); // 设置为1V档位，0.7精细档位,1
            }else{ // 否则,1
                setVScaleId(
                        HardwareProduct.isMHO68V1() ? VerticalAxis.DANG_500mV : VerticalAxis.DANG_1V // 根据硬件型号选择档位,1
                        , 1.0,true, false); // 设置档位,1
            }
        }else{ // 如果当前档位有效,1
            if(resistanceType == RESISTANCE_1M && scaleIdBak != verticalAxis.getScaleId()){ // 如果切换到1M欧姆且备份档位与当前不同,1
                setVScaleId(scaleIdBak, 1.0,true, true); // 恢复备份档位,1
            }
        }
    }

    /**
     * 获取阻抗类型,1
     * @return 阻抗类型（RESISTANCE_1M或RESISTANCE_50）,1
     */
    public synchronized int getResistanceType() {

        return resistanceType; // 返回阻抗类型,1
    }

    /**
     * 获取电压档位值（每格幅值）,1
     * @return 电压档位值,1
     */
    public double getVScaleVal() {//档位，对应每格的幅值
        return verticalAxis.getScaleVal(); // 返回垂直轴的档位值,1
    }
    /**
     * 获取电压档位ID对应的值,1
     * @return 电压档位ID对应的值,1
     */
    public double getVScaleIdVal() {
        return verticalAxis.getScaleIdVal(); // 返回垂直轴的档位ID值,1
    }
    /**
     * 获取指定档位ID的值,1
     * @param scaleId 档位ID,1
     * @return 档位ID对应的值,1
     */
    public double getVScaleIdVal(int scaleId) {
        return verticalAxis.getScaleIdVal(scaleId); // 返回指定档位ID的值,1
    }
    /**
     * 根据值获取档位ID,1
     * @param val 电压值,1
     * @return 档位ID,1
     */
    public int getVScaleId(double val) {
        return verticalAxis.getScaleId(val); // 返回对应的档位ID,1
    }
    /**
     * 获取当前电压档位ID,1
     * @return 电压档位ID,1
     */
    public int getVScaleId() {
        return verticalAxis.getScaleId(); // 返回垂直轴的档位ID,1
    }
    /**
     * 获取精细档位值,1
     * @return 精细档位值,1
     */
    public double getFineScale() { return verticalAxis.getFineScale();} // 返回垂直轴的精细档位值,1

    /**
     * 设置电压档位ID,1
     * @param scaleId 档位ID,1
     * @param fine 精细档位值,1
     * @param bUser 是否用户操作,1
     * @param initScaleBak 是否初始化档位备份,1
     */
    private void setVScaleId(int scaleId,double fine,boolean bUser, boolean initScaleBak){
        Logger.d(TAG, "调整档位-----3------scaleId= " + scaleId + " ,fine= " + fine + " ,bUser= " + bUser); // 打印调试日志,1

        // 验证档位有效性
        if (VerticalAxis.isValidScaleId(scaleId) // 检查档位ID是否有效,1
                && isVScaleIdValid(scaleId)) { // 检查档位ID是否在当前条件下有效,1

            if(getResistanceType() == RESISTANCE_50 // 如果阻抗类型为50欧姆,1
                    && isEnable_50O_700mV()){ // 且50欧姆700mV使能,1
                if(scaleId >= VerticalAxis.DANG_1V){ // 如果档位ID大于等于1V,1
                    scaleId = VerticalAxis.DANG_1V; // 限制为1V档位,1
                    if(fine >= 0.7){ // 如果精细档位大于等于0.7,1
                        fine = 0.7; // 限制为0.7,1
                    }
                }
            }

            verticalAxis.setFineScale(fine); // 设置垂直轴的精细档位,1
            verticalAxis.setScaleId(scaleId);// 设置档位
            channelAction.vScaleIdChange();  // 触发档位变化动作 - 这里会触发继电器控制
            super.setVScaleId(scaleId); // 调用父类设置档位ID,1
            if (initScaleBak && isValidScaleIdExt(verticalAxis.getScaleId())) { // 如果需要初始化备份且档位有效,1
                scaleIdBak = -1; // 清空档位备份,1
            }
            if(bUser){ // 如果是用户操作,1
                channelAction.vScaleIdUserChange(); // 触发用户档位变化动作,1
            }

            if(isAutoProbe()){ // 如果是自动探头,1
                BaseProbe baseProbe = getProbe(); // 获取探头对象,1
                if(baseProbe != null){ // 如果探头存在,1
                    baseProbe.setVScaleVal(getVScaleVal()); // 设置探头的档位值,1
                }
            }
        }
    }

    /**
     * 设置电压档位ID（公开方法）,1
     * @param scaleId 档位ID,1
     * @param fine 精细档位值,1
     * @param bUser 是否用户操作,1
     */
    public void setVScaleId(int scaleId,double fine,boolean bUser){
        Log.d("TAG","调整档位---------------2------------------, scaleId:" + scaleId + ",fine:" + fine + ",bUser:" + bUser); // 打印调试日志,1
        setVScaleId(scaleId, fine,bUser, true); // 调用私有方法设置档位,1
    }
    /**
     * 设置电压档位ID,1
     * @param scaleId 档位ID,1
     * @param bUser 是否用户操作,1
     */
    public void setVScaleId(int scaleId,boolean bUser) {
        Logger.d(TAG, "调整档位-----1------" + "scaleId= " + scaleId + " ,bUser= " + bUser); // 打印调试日志,1
        setVScaleId(scaleId,1.0,bUser); // 调用重载方法，精细档位设为1.0,1
    }
    /**
     * 设置电压档位ID,1
     * @param scaleId 档位ID,1
     * @param fine 精细档位值,1
     */
    public void setVScaleId(int scaleId,double fine) {
       // Logger.d(TAG, "调整档位-----7------scaleId= " + scaleId + " ,fine= " + fine);
        setVScaleId(scaleId,fine,false); // 调用重载方法，用户操作设为false,1
    }

    /**
     * 设置电压档位ID,1
     * @param scaleId 档位ID,1
     */
    public void setVScaleId(int scaleId) {
        setVScaleId(scaleId,1.0,false); // 调用重载方法，精细档位设为1.0，用户操作设为false,1
    }


    /**
     * 判断档位ID是否在扩展范围内有效,1
     * @param vScaleId 档位ID,1
     * @return true表示有效，false表示无效,1
     */
    private boolean isValidScaleIdExt(int vScaleId){
        return (vScaleId >= getMinGear() && vScaleId <= getMaxGear()); // 检查档位是否在最小和最大档位之间,1
    }

    @Override
    public boolean isVScaleIdValid(int vScaleId) {
        if(VerticalAxis.isValidScaleIdExt(vScaleId) && isValidScaleIdExt(vScaleId)){ // 检查档位ID是否有效,1
            if(getVerticalMode() == VERTICAL_MODE_SCREEN_CENTER){ // 如果垂直模式为屏幕中心,1
                double vScaleVal = getVScaleIdVal(vScaleId) * getFineScale(); // 计算档位值,1
                double val = centerVal / vScaleVal; // 计算中心值对应的档位,1
                int max = Scope.vSpanOfView(getResistanceType(),vScaleVal); // 获取视图跨度,1
                return (val <= max) && (val >= -max); // 检查是否在视图范围内,1
            }else{ // 否则,1
                return true; // 返回有效,1
            }
        }
        return false; // 返回无效,1
    }



    //输入任意档位自动转换精细档位
    /**
     * 根据电压值自动设置档位,1
     * @param scaleVal 电压值,1
     */
    public void setVScaleVal(double scaleVal){
        Logger.d(TAG, "调整档位-----6------scaleVal= " + scaleVal); // 打印调试日志,1
        scaleVal /= verticalAxis.getProbeRate(); // 除以探头倍率,1

        int scaleId = VerticalAxis.getMinGear(); // 从最小档位开始,1
        double val = VerticalAxis.getScaleIdValById(scaleId); // 获取最小档位值,1
        if(scaleVal < val){ // 如果输入值小于最小档位值,1
            scaleVal = val; // 限制为最小档位值,1
        }

        for(int i=VerticalAxis.getMinGear();i<=VerticalAxis.getMaxGear();i++){ // 遍历所有档位,1
            val= VerticalAxis.getScaleIdValById(i); // 获取当前档位值,1
            scaleId = i; // 记录当前档位ID,1
            if(scaleVal <= val || DoubleUtil.FuzzyCompare(scaleVal,val)){ // 如果输入值小于等于当前档位值,1
                break; // 跳出循环,1
            }
        }
        if(scaleId == VerticalAxis.getMaxGear() // 如果是最大档位,1
                && scaleVal > VerticalAxis.getScaleIdValById(VerticalAxis.getMaxGear()) ){ // 且输入值大于最大档位值,1
            scaleVal = val; // 限制为最大档位值,1
        }

        setVScaleId(scaleId,scaleVal/val); // 设置档位ID和精细档位,1
    }

    /**
     * 计算档位ID,1
     * @param dang 基准档位,1
     * @param val 偏移量,1
     * @return 计算后的档位ID,1
     */
    public int calcVScaleId(int dang,int val){
        int i = 0; // 循环变量,1
        for(i=VerticalAxis.getMinGear();i<=VerticalAxis.getMaxGear();i++){ // 遍历所有档位,1
            if(i >= dang){ // 如果当前档位大于等于基准档位,1
                i += val; // 加上偏移量,1
                if(i < 0){ // 如果小于0,1
                    i = 0; // 限制为0,1
                }else if(i >= VerticalAxis.getMaxGear()){ // 如果大于等于最大档位,1
                    i = VerticalAxis.getMaxGear(); // 限制为最大档位,1
                }
                break; // 跳出循环,1
            }
        }
        return i; // 返回档位ID,1
    }

    /**
     * 计算档位ID（基于当前档位）,1
     * @param val 偏移量（正数向上，负数向下）,1
     * @return 计算后的档位ID,1
     */
    public int calcVScaleId(int val){
        int scaleId = getVScaleId(); // 获取当前档位ID,1
        double vScaleVal = getVScaleVal()/getProbeRate(); // 获取当前档位值,1
        if(val > 0){ // 如果向上调整,1
            vScaleVal += vScaleVal * 0.01; // 增加1%,1
            int i = VerticalAxis.getMinGear(); // 从最小档位开始,1
            for(i=VerticalAxis.getMinGear();i<=VerticalAxis.getMaxGear();i++){ // 遍历所有档位,1
                if(VerticalAxis.getScaleIdValById(i) > vScaleVal){ // 找到第一个大于当前值的档位,1
                    scaleId = i; // 设置为该档位,1
                    break; // 跳出循环,1
                }
            }
            if(i >= VerticalAxis.getMaxGear()){ // 如果超过最大档位,1
                scaleId = VerticalAxis.getMaxGear(); // 限制为最大档位,1
            }
        }else{ // 如果向下调整,1
            vScaleVal -= vScaleVal * 0.01; // 减少1%,1
            int i=VerticalAxis.getMaxGear(); // 从最大档位开始,1
            for(i=VerticalAxis.getMaxGear();i>=VerticalAxis.getMinGear();i--){ // 反向遍历所有档位,1
                if(VerticalAxis.getScaleIdValById(i) < vScaleVal){ // 找到第一个小于当前值的档位,1
                    scaleId = i; // 设置为该档位,1
                    break; // 跳出循环,1
                }
            }
            if(i <= VerticalAxis.getMinGear()){ // 如果低于最小档位,1
                scaleId = VerticalAxis.getMinGear(); // 限制为最小档位,1
            }
        }

        return scaleId; // 返回档位ID,1
    }

    /**
     * 设置精细档位,1
     * @param fineScale 精细档位值,1
     */
    public void setFineScale(double fineScale) {
        verticalAxis.setFineScale(fineScale); // 设置垂直轴的精细档位,1
        channelAction.vFineChange(); // 触发精细档位变化动作,1
    }

    @Override
    public double getVerticalPerPix(){//获取垂直方向每个像素 对应的 幅值 只包含固定精细挡位，根据switch选择
        double val = getVScaleVal(); // 获取档位值,1
        double h = ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff(); // 计算每格像素高度,1
        if(Scope.getInstance().isZoom()){ // 如果处于缩放状态,1
            h = ScopeBase.getZoomVerticalPerGridPixels() * ScopeBase.getToUICoff(); // 使用缩放的每格像素高度,1
        }
        return val/h; // 返回每像素对应的电压值,1
    }

    /**
     * 获取AD垂直方向每像素对应的幅值,1
     * @return 每像素对应的电压值,1
     */
    public double getADVerticalPerPix(){//获取每个像素 对应的幅值
        double val = getVScaleVal(); // 获取档位值,1
        double h = ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff(); // 计算每格像素高度,1
        return val/h; // 返回每像素对应的电压值,1
    }

    @Override
    public double getSampleRate() {
        return Scope.getInstance().getSampleRate(); // 返回示波器的采样率,1
    }

    @Override
    public double getSampleRate2display(){
        return getSampleRate(); // 返回显示用的采样率,1
    }

    /**
     * 获取指定档位的垂直每像素值,1
     * @param scaleId 档位ID,1
     * @return 每像素对应的电压值,1
     */
    public double getVerticalPerPix(int scaleId){
        double val = verticalAxis.getScaleIdVal(scaleId) * getProbeRate(); // 计算档位值乘以探头倍率,1
        double h = ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff(); // 计算每格像素高度,1
        if(Scope.getInstance().isZoom()){ // 如果处于缩放状态,1
            h = ScopeBase.getZoomVerticalPerGridPixels() * ScopeBase.getToUICoff(); // 使用缩放的每格像素高度,1
        }
        return val/h; // 返回每像素对应的电压值,1
    }


    /**
     * 设置AD像素值,1
     * @param val AD像素值,1
     */
    public void setAdPix(double val){
        adPix = val; // 设置AD像素值,1
    }
    /**
     * 获取AD像素值,1
     * @return AD像素值,1
     */
    public double getAdPix(){
        return adPix; // 返回AD像素值,1
    }
    /**
     * 设置总线主电平,1
     * @param busPrimaryLevel 总线主电平值,1
     */
    public void setBusPrimaryLevel(double busPrimaryLevel){

        this.busPrimaryLevel = busPrimaryLevel; // 设置总线主电平,1
        channelAction.busLevelChange(); // 触发电平变化动作,1
    }
    /**
     * 获取总线主电平,1
     * @return 总线主电平值,1
     */
    public double getBusPrimaryLevel(){
        return busPrimaryLevel; // 返回总线主电平,1
    }
    /**
     * 设置总线从电平,1
     * @param busSecondaryLevel 总线从电平值,1
     */
    public void setBusSecondaryLevel(double busSecondaryLevel){
        this.busSecondaryLevel = busSecondaryLevel; // 设置总线从电平,1
        channelAction.busLevelChange(); // 触发电平变化动作,1

    }
    /**
     * 获取总线从电平,1
     * @return 总线从电平值,1
     */
    public double getBusSecondaryLevel(){
        return busSecondaryLevel; // 返回总线从电平,1
    }

    /**
     * 设置M参数,1
     * @param val M参数值,1
     */
    public void setM(int val){
        m = val; // 设置M参数,1
    }
    /**
     * 设置N参数,1
     * @param val N参数值,1
     */
    public void setN(int val){
        n = val; // 设置N参数,1
    }


    /**
     * 获取M参数,1
     * @return M参数值,1
     */
    public int getM(){
        return m; // 返回M参数,1
    }
    /**
     * 获取N参数,1
     * @return N参数值,1
     */
    public int getN(){
        return n; // 返回N参数,1
    }

    /**
     * 设置位置值,1
     * @param val 位置值,1
     */
    public synchronized void setPlaceVal(int val){
        placeVal = val; // 设置位置值,1
    }

    /**
     * 获取位置值,1
     * @return 位置值,1
     */
    public synchronized int getPlaceVal(){
        return placeVal; // 返回位置值,1
    }

    private double minVal = 0; // 最小值,1
    private double maxVal = 0; // 最大值,1
    private double pkpkVal = 0; // 峰峰值,1

    /**
     * 获取最小值,1
     * @return 最小值,1
     */
    public synchronized double getMinVal() {
        return minVal; // 返回最小值,1
    }

    /**
     * 获取最大值,1
     * @return 最大值,1
     */
    public synchronized double getMaxVal() {
        return maxVal; // 返回最大值,1
    }
    /**
     * 获取峰峰值,1
     * @return 峰峰值,1
     */
    public synchronized double getPKPKVal(){
        return pkpkVal; // 返回峰峰值,1
    }
    /**
     * 设置最大值和最小值,1
     * @param maxVal 最大值,1
     * @param minVal 最小值,1
     * @param bRun 是否运行状态,1
     */
    public  void setMaxMinVal(double maxVal,double minVal,boolean bRun) {
        double v = 0; // 电压系数,1
        if(bRun){ // 如果处于运行状态,1
            v = getADVerticalPerPix() * ScopeBase.getToFPGACoff(); // 计算运行状态的电压系数,1
        }else{ // 否则,1
            v = ScopeFrozen.getInstance().getChVertical(getChId()).getScaleVal()/(ScopeBase.getVerticalPerGridPixels()); // 计算冻结状态的电压系数,1
        }
        synchronized (this) { // 同步块，保证线程安全,1
            maxVal = (maxVal * 256 + m) * n / 65536 + placeVal; // 计算最大值,1
            minVal = (minVal * 256 + m) * n / 65536 + placeVal; // 计算最小值,1

            if (bRun) { // 如果处于运行状态,1
                maxVal_bak = maxVal; // 备份最大值,1
                minVal_bak = minVal; // 备份最小值,1
            } else { // 否则,1

                if (maxVal > maxVal_bak) maxVal = maxVal_bak; // 如果最大值大于备份值，使用备份值,1
                if (minVal < minVal_bak) minVal = minVal_bak; // 如果最小值小于备份值，使用备份值,1
            }

            this.maxVal = maxVal * v; // 设置最大值,1
            this.minVal = minVal * v; // 设置最小值,1
            this.pkpkVal = this.maxVal - this.minVal; // 计算峰峰值,1
        }

    }

    double maxVal_bak = Double.MIN_VALUE; // 最大值备份,1
    double minVal_bak = Double.MAX_VALUE; // 最小值备份,1
    /**
     * 设置16位最大值和最小值,1
     * @param maxVal 最大值,1
     * @param minVal 最小值,1
     * @param bRun 是否运行状态,1
     */
    public  void setMaxMinVal16Bit(double maxVal, double minVal,boolean bRun) {
        double v = 0; // 电压系数,1
        synchronized (this) { // 同步块，保证线程安全,1
            maxVal += 512 - c; // 调整最大值,1
            minVal += 512 - c; // 调整最小值,1
        }

        if(bRun){ // 如果处于运行状态,1
            v = getVScaleVal()/ScopeBase.getVerticalPerGridPixels(); // 计算运行状态的电压系数,1
            maxVal = maxVal / getYFactor() - getPos(); // 计算最大值,1
            minVal = minVal / getYFactor() - getPos(); // 计算最小值,1
        }else{ // 否则,1
            ScopeFrozen scopeFrozen = ScopeFrozen.getInstance(); // 获取冻结示波器实例,1
            v = scopeFrozen.getChVertical(getChId()).getScaleVal()/(ScopeBase.getVerticalPerGridPixels()); // 计算冻结状态的电压系数,1
            maxVal = maxVal / getYFactor() - scopeFrozen.getChPos(getChId()); // 计算最大值,1
            minVal = minVal / getYFactor() - scopeFrozen.getChPos(getChId()); // 计算最小值,1
        }

        synchronized (this) { // 同步块，保证线程安全,1
            this.maxVal = maxVal * v; // 设置最大值,1
            this.minVal = minVal * v; // 设置最小值,1
            this.pkpkVal = this.maxVal - this.minVal; // 计算峰峰值,1
        }
    }

    /**
     * 获取半波振幅（像素单位）,1
     * @return 半波振幅（像素单位）,1
     */
    public double getHalfWaveAmpInPix() {
        //notice: this value not contain ChannelZero
        waitWave(); // 等待波形数据,1
        return (getMaxVal() + getMinVal()) / 2 / getADVerticalPerPix(); // 计算半波振幅,1
    }

    private volatile boolean needWave = true; // 需要波形标志,1
    /**
     * 设置是否需要波形,1
     * @param needWave true表示需要波形，false表示不需要,1
     */
    public void setNeedWave(boolean needWave){
        this.needWave = needWave; // 设置需要波形标志,1
        if(!needWave){ // 如果不需要波形,1
            super.setWaveValid(false); // 设置波形无效,1
        }
    }

    @Override
    public IDataBuffer obtain() {
        if(isWaveValid()) { // 如果波形有效,1
            return super.obtain(); // 返回数据缓冲区,1
        }
        return null; // 波形无效返回null,1
    }

    /**
     * 判断是否需要波形,1
     * @return true表示需要波形，false表示不需要,1
     */
    public boolean isNeedWave(){
        return this.needWave || getMeasure().isEnable(); // 如果需要波形或测量使能，返回true,1
    }

    @Override
    public void setWaveValid(boolean bWaveValid) {

        if(bWaitWave){ // 如果正在等待波形,1
            Logger.d(TAG,"setWaveValid :" + bWaveValid); // 打印调试日志,1
            synchronized (this) { // 同步块，保证线程安全,1
                this.notify(); // 通知等待线程,1
            }
        }
        super.setWaveValid(bWaveValid); // 调用父类设置波形有效标志,1
    }

    volatile boolean bWaitWave = false; // 等待波形标志,1
    /**
     * 等待波形数据,1
     */
    public void waitWave(){
        if(!isNeedWave()) { // 如果不需要波形,1
            synchronized (this) { // 同步块，保证线程安全,1
                bWaitWave = true; // 设置等待波形标志,1
                boolean needWave_bak = this.needWave; // 备份需要波形标志,1
                this.needWave = true; // 临时设置为需要波形,1
                channelAction.getWave(); // 触发获取波形动作,1
                Logger.d(TAG,"getwave,needWave_bak:" + needWave_bak); // 打印调试日志,1
                try { // 尝试等待,1
                    this.wait(2000); // 等待最多2000毫秒,1
                } catch (InterruptedException e) { // 如果中断,1
                    e.printStackTrace(); // 打印堆栈信息,1
                }
                bWaitWave = false; // 清除等待波形标志,1
                needWave = needWave_bak; // 恢复需要波形标志,1
            }
        }
    }

    @Override
    public boolean save(String pathName) {
        this.waitWave(); // 等待波形数据,1
        return super.save(pathName); // 调用父类保存方法,1
    }

    @Override
    public boolean saveCSV(String pathName) {
        this.waitWave(); // 等待波形数据,1
        return super.saveCSV(pathName); // 调用父类保存CSV方法,1
    }


    private double YFactor = 1; // Y因子,1
    /**
     * 设置Y因子,1
     * @param yFactor Y因子值,1
     */
    public synchronized void setYFactor(double yFactor){
        this.YFactor = yFactor; // 设置Y因子,1
    }
    /**
     * 获取Y因子,1
     * @return Y因子值,1
     */
    public synchronized double getYFactor(){
        return YFactor; // 返回Y因子,1
    }

    public double ZPos = 0; // Z位置,1
    /**
     * 设置Z位置,1
     * @param pos Z位置值,1
     */
    public synchronized void setZPos(double pos){
        this.ZPos = pos; // 设置Z位置,1
    }
    /**
     * 获取Z位置,1
     * @return Z位置值,1
     */
    public synchronized double getZPos(){
        return this.ZPos; // 返回Z位置,1
    }

    public int YPos = 0; // Y位置,1
    /**
     * 设置Y位置,1
     * @param pos Y位置值,1
     */
    public synchronized void setYPos(int pos){
        this.YPos = pos; // 设置Y位置,1
    }
    /**
     * 获取Y位置,1
     * @return Y位置值,1
     */
    public synchronized int getYPos(){
        return this.YPos; // 返回Y位置,1
    }
    public int a,b,c; // ABC参数,1

    /**
     * 设置ABC参数,1
     * @param a A参数值,1
     * @param b B参数值,1
     * @param c C参数值,1
     */
    public synchronized void setABC(int a,int b,int c){
        this.a = a; // 设置A参数,1
        this.b = b; // 设置B参数,1
        this.c = c; // 设置C参数,1
    }
}

