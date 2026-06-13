package com.micsig.tbook.scope.Calibrate.MHO68v2;

import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Action.ChannelHardw;
import com.micsig.tbook.scope.Calibrate.CabteRegister;
import com.micsig.tbook.scope.Calibrate.Calibrate;
import com.micsig.tbook.scope.Calibrate.HW;
import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.Trigger.Trigger;
import com.micsig.tbook.scope.Trigger.TriggerCommon;
import com.micsig.tbook.scope.Trigger.TriggerEdge;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.fpga.FPGACommand;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.math.MathNative;
import com.micsig.tbook.scope.vertical.VerticalAxis;

import java.util.Arrays;

/**
 * MHO68 V2示波器通道增益校准实现类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate.MHO68v2（MHO68 V2示波器校准模块）</li>
 *   <li>架构层级：业务逻辑层 - 具体校准实现</li>
 *   <li>设计模式：继承Calibrate抽象基类，实现通道增益校准流程</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现MHO68 V2示波器的通道增益校准</li>
 *   <li>支持单通道或全通道同时校准</li>
 *   <li>支持PGA步进校准和ADC满量程校准</li>
 *   <li>支持1MΩ和50Ω两种阻抗模式</li>
 * </ul>
 * 
 * <p><b>增益校准原理：</b>
 * <pre>
 * 校准目的：
 *   校准通道的增益系数，确保测量精度。
 *   增益校准分为两类：
 *   1. PGA步进校准：校准PGA芯片的增益步进值
 *   2. ADC满量程校准：校准ADC的满量程系数
 * 
 * 输入校准信号标准：
 *   1. 30Hz交流方波，占空比50%
 *   2. 方波高电压：scale × 4 (V)
 *   3. 方波低电压：-scale × 4 (V)
 * 
 * 校准流程：
 *   ┌─────────────────────────────────────────────────────────────┐
 *   │  1. calibratePrepare() - 准备阶段                           │
 *   │     ├── 设置时基为100us/div                                 │
 *   │     ├── 设置触发为边沿触发、上升沿                           │
 *   │     ├── 设置PGA和ADC满量程配置                              │
 *   │     └── 初始化通道系数                                       │
 *   │                                                             │
 *   │  2. onCalibrate() - 执行阶段                                │
 *   │     ├── 采集波形数据                                        │
 *   │     ├── 计算波形幅度（高电平-低电平）                        │
 *   │     ├── 计算实际增益                                        │
 *   │     ├── 更新增益系数                                        │
 *   │     └── 计算PGA步进或ADC满量程系数                          │
 *   │                                                             │
 *   │  3. 保存校准结果                                            │
 *   └─────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>校准索引说明：</b>
 * <pre>
 * idx索引对应关系：
 *   0-2:   PGA步进校准（三个不同PGA值）
 *   3-5:   1MΩ阻抗 档位1 ADC满量程校准
 *   6-8:   1MΩ阻抗 档位2 ADC满量程校准
 *   9-11:  1MΩ阻抗 档位3 ADC满量程校准
 *   12-14: 50Ω阻抗 档位1 ADC满量程校准
 *   15-17: 50Ω阻抗 档位2 ADC满量程校准
 *   18-20: 50Ω阻抗 档位3 ADC满量程校准
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：Calibrate（校准抽象基类）</li>
 *   <li>依赖：CabteRegister（校准寄存器管理）</li>
 *   <li>依赖：HW_MHO68V2（硬件操作，校准状态常量）</li>
 *   <li>依赖：ChannelHardw（通道硬件操作）</li>
 *   <li>依赖：FPGACommand（FPGA命令管理）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>出厂校准时校准所有通道的增益</li>
 *   <li>用户手动校准单个通道</li>
 *   <li>定期维护校准</li>
 * </ul>
 * 
 * @author xuj
 * @version 2.0
 * @since 2018/7/18
 * @see Calibrate 校准抽象基类
 * @see CabteRegister 校准寄存器管理
 * @see HW_MHO68V2 硬件操作实现
 */
public class MHO68v2_ChGainCalibrate extends Calibrate {
    
    /**
     * 构造函数
     * 
     * <p>调用父类构造函数，初始化校准类型，并设置初始延时为2ms
     * 
     * @param calibrateType 校准类型标识
     */
    public MHO68v2_ChGainCalibrate(int calibrateType) {
        super(calibrateType);  // 调用父类Calibrate的构造函数
        delaySet(2);            // 设置初始延时为2毫秒
    }


    /** 错误码，用于标识校准结果 */
    private int errcode;
    
    /** 日志标签：通道增益校准 */
    private final String TAG = "ChGain";
    
    /** 完整日志标签：包含优先级前缀 */
    private final String TAG1=TAG_PRI+":"+TAG;
    
    /** 当前校准通道索引，volatile保证多线程可见性 */
    private volatile int ch=0;
    
    /** 当前处理的通道索引 */
    private volatile int chIdx = 0;

    /** 校准方波的基准幅度值（像素），应为8格×50像素 */
    private int vPos = 100;
    
    /** 最大幅度值（基准值×5） */
    private int vMax = 100*5;

    /** 测量计数器 */
    private int meatCnt;
    
    /** 信号源幅度数组（V），每个通道的校准信号幅度 */
    private volatile double []srcAmp = {0,0,0,0,0,0,0,0};

    /** 通道位置数组（像素），每个通道的波形位置 */
    private volatile double [] chpos ={0,0,0,0,0,0,0,0};
    
    /** 垂直档位值 */
    private volatile double vScaleVal;
    
    /** 通道模式（采样通道数） */
    private volatile int chMode;


    /** 校准索引，标识当前校准的项目 */
    private volatile int idx;


    /** 波形平均值数组，用于计算波形幅度 */
    private double average[] = new double[ChannelFactory.CH_CNT];
    
    /** 波形最小值数组 */
    private double min[] = new double[ChannelFactory.CH_CNT];
    
    /** 波形最大值数组 */
    private double max[] = new double[ChannelFactory.CH_CNT];

    /** 最佳值数组，记录迭代过程中的最优值 */
    private double[] best_value = new double[ChannelFactory.CH_CNT];
    
    /** 备份系数数组 */
    private short[] bakCoef = new short[ChannelFactory.CH_CNT];
    
    /** 状态数组，用于状态机控制 */
    private int[] state = new int[ChannelFactory.CH_CNT];
    
    /** 尝试次数数组 */
    private int[] tryNum = new int[ChannelFactory.CH_CNT];
    
    /** 尝试系数数组 */
    private int[] tryChiShu = new int[ChannelFactory.CH_CNT];
    
    /** 尝试PGA值数组 */
    private int[] trypga = new int[ChannelFactory.CH_CNT];
    
    /** 通道完成标志数组 */
    private boolean[] finished = new boolean[ChannelFactory.CH_CNT];

    /** 通道使能标志数组，标识哪些通道参与校准 */
    private boolean[] ch_en = new boolean[ChannelFactory.CH_CNT];

    /** 通道硬件操作实例 */
    private ChannelHardw channelHardw;

    /** MHO68 V2硬件操作实例 */
    private HW_MHO68V2 hw;

    /**
     * 获取日志标签
     * 
     * @return 完整日志标签
     */
    @Override
    public String getTAG() {
        return TAG1;  // 返回包含优先级前缀的完整标签
    }

    /**
     * 获取错误码
     * 
     * @return 错误码，0表示成功，非0表示失败
     */
    @Override
    public int getErrcode() {
        return errcode;  // 返回当前错误码
    }

    /**
     * 重置计算变量
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>在开始校准前调用</li>
     *   <li>初始化所有通道的状态变量</li>
     * </ul>
     */
    private void rstCalculate(){
        // 遍历所有通道
        for (int i = 0; i < ChannelFactory.CH_CNT; i++) {
            best_value[i] = Float.MAX_VALUE;  // 初始化最佳值为最大值
            state[i] = 0;                      // 初始化状态为0
            average[i] = 0;                    // 清零平均值
            max[i] = -Double.MAX_VALUE;        // 初始化最大值为负最大值
            min[i] = Double.MAX_VALUE;         // 初始化最小值为最大值
            tryNum[i] = 0;                     // 清零尝试次数
            tryChiShu[i] = 0;                  // 清零尝试系数
            trypga[i] = 0;                     // 清零尝试PGA值
        }
        meatCnt = 0;  // 清零测量计数
    }

    /**
     * 初始化校准寄存器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取通道硬件操作实例</li>
     *   <li>获取MHO68 V2硬件操作实例</li>
     * </ul>
     */
    @Override
    public void iniCalibrateReg(){
        channelHardw = ChannelHardw.getInstance();  // 获取通道硬件操作单例
        hw = (HW_MHO68V2) cabteRegister.getHw();    // 获取硬件操作实例
    }

    /**
     * 设置校准参数
     * 
     * <p><b>参数格式：</b>
     * <pre>
     * vol为double[]数组：
     *   vol[0]: 通道索引（0~7表示单通道，-1表示所有通道）
     *   vol[1]: 垂直档位值
     *   vol[2]: 通道模式
     *   vol[3]: 校准索引
     * </pre>
     * 
     * @param vol 校准参数对象
     */
    @Override
    public void setParam(Object vol) {
        // 检查参数类型是否为double数组
        if(vol instanceof double[]) {
            double[] param=(double[])vol;  // 类型转换
            // 检查参数长度
            if(param.length >= 3){
                int ix =(int)(param[0] + 0.1);  // 获取通道索引（加0.1避免浮点误差）
                // 检查是否为负数（表示所有通道）
                if(param[0] < 0){
                    ix = -1;  // 设置为-1表示所有通道
                }
                // 验证通道索引有效性
                if(ix == -1 || ChannelFactory.isDynamicCh(ix)) {
                    ch = ix;  // 保存通道索引
                    vScaleVal = param[1];  // 保存垂直档位值
                    
                    // 设置信号源幅度
                    if(ch >= 0) {
                        // 单通道：所有通道使用相同幅度
                        Arrays.fill(srcAmp, vScaleVal * 8);
                    }else {
                        // 所有通道：从校准寄存器获取每个通道的幅度
                        StringBuilder sb = new StringBuilder();

                        sb.append("srcAmp =>");
                        // 遍历所有通道获取幅度
                        for (int i = 0; i < srcAmp.length; i++) {
                            srcAmp[i] = cabteRegister.getChAmp(i) * 8;  // 获取通道幅度×8
                            sb.append("ch").append(i).append(":").append(srcAmp[i]).append(",");
                        }
                        Log.d(TAG,sb.toString());  // 输出日志
                    }
                    chMode = (int)(param[2] + 0.1);  // 保存通道模式
                    idx = (int)(param[3] + 0.1);     // 保存校准索引
                    
                    // 输出调试日志
                    Logger.d(TAG,"ch:" + ch
                            + ",vScaleVal:" + vScaleVal
                            + ",srcAmp:" + srcAmp
                            + ",chMode:" + chMode
                            + ",idx:" + idx
                    );
                    
                    // 检查校准索引有效性
                    if(idx < 0 || idx >= HW_MHO68V2.GAIN_PGA_CODE.length){
                        idx = 0;  // 索引无效，重置为0
                    }
                    // PGA步进校准（idx<3）时，强制使用4通道模式
                    if(idx < 3){
                        chMode = 3;
                    }
                    chMode = 3;  // 强制使用4通道模式
                    
                    // 根据索引设置阻抗类型
                    // idx >= 12 表示50Ω阻抗
                    setInitResistanceType(idx >= 12 ? Channel.RESISTANCE_50 : Channel.RESISTANCE_1M);
                    return;  // 参数设置完成，返回

                }
            }
        }
        // 参数无效时使用默认值
        ch = ChannelFactory.CH1;  // 默认校准通道1
        vScaleVal = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_10mV);  // 默认档位10mV/div
    }

    /** 参数数组，用于返回校准结果 */
    int []param = new int[2];
    
    /**
     * 获取校准参数
     * 
     * @return 参数数组 [通道索引, 校准索引]
     */
    @Override
    public Object getParam() {
        param[0] = ch;   // 设置通道索引
        param[1] = idx;  // 设置校准索引
        return param;    // 返回参数数组
    }
    
    /**
     * 设置PGA增益值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置所有通道的PGA增益值</li>
     *   <li>用于校准过程中的PGA控制</li>
     * </ul>
     * 
     * @param val PGA增益值
     */
    private void setPGAVal(int val){
        FPGACommand fpgaCommand = FPGACommand.getInstance();  // 获取FPGA命令实例
        fpgaCommand.cmdDevice(300);  // 发送设备命令300
        
        // 创建增益值数组，所有通道使用相同值
        int [] res ={val,val,val,val,val,val,val,val};
        channelHardw.set_ch_AD8370Gain(res);  // 设置AD8370增益
        fpgaCommand.setPgaVal(val);            // 保存PGA值到命令对象
        fpgaCommand.cmdDevice(300);            // 再次发送设备命令300
    }

    /** 档位索引 */
    volatile int ratioIdx = 0;

    /** ADC满量程配置值 */
    int adfs = 0;
    
    /** PGA增益值 */
    int pgaVal = 0;
    
    /** 阻抗类型 */
    int resistanceType = Channel.RESISTANCE_1M;
    
    /**
     * 校准准备阶段
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置时基、触发、通道参数</li>
     *   <li>配置PGA和ADC满量程</li>
     *   <li>初始化通道系数</li>
     *   <li>等待信号稳定</li>
     * </ul>
     */
    @Override
    public void calibratePrepare() {

        cabteRegister.setNewInputV(-1);  // 重置输入电压标志
        
        // 初始化完成标志和使能标志
        for(int i=0; i<finished.length; i++) {
            finished[i] = true;   // 标记所有通道已完成
            ch_en[i] = false;     // 禁用所有通道
        }

        // 设置时基为100us/div
        HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.TSI_100uS);
        
        // 设置ADC差分增益校准模式
        FPGACommand.getInstance().setADdiffGainCalib(true);
        
        // 获取触发工厂实例
        TriggerFactory triggerFactory=TriggerFactory.getInstance();
        // 设置触发模式为正常模式
        triggerFactory.getTriggerCommon().setTriggerMode(TriggerCommon.TM_NORMAL);
        // 获取边沿触发实例
        TriggerEdge triggerEdge=(TriggerEdge)(triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));
        // 设置触发边沿为上升沿
        triggerEdge.setTriggerEdge(TriggerEdge.TET_ASC);
        // 设置触发耦合为噪声抑制
        triggerEdge.setTriggerCouple(TriggerEdge.COUPLING_NOISERS);
        
        Logger.d(TAG,"idx:" + idx + ",vScaleVal:" + vScaleVal);  // 输出调试日志


        ratioIdx = HW.RATIO_DANG_2;  // 默认档位索引为第二档
        resistanceType = Channel.RESISTANCE_1M;  // 默认阻抗类型为1MΩ

        // 根据校准索引获取PGA值和ADC满量程值
        pgaVal = HW_MHO68V2.GAIN_PGA_CODE[idx];  // 获取PGA配置值
        adfs = HW_MHO68V2.GAIN_ADFS_CODE[idx];   // 获取ADC满量程配置值

        // 根据校准索引确定档位和阻抗类型
        if(idx < 3){
            // PGA步进校准：使用第二档
            ratioIdx = HW.RATIO_DANG_2;
        }else{
            // ADC满量程校准：根据索引计算档位
            ratioIdx = HW.RATIO_DANG_1 + idx / 3 - 1;
            // idx >= 12 表示50Ω阻抗
            if(idx >= 12){
                resistanceType = Channel.RESISTANCE_50;
                ratioIdx = (idx - 12) / 3;  // 计算50Ω阻抗的档位索引
            }
        }
        double x = 1;  // 临时变量
        Log.d(TAG,"ch:" + ch + ",idx:" + idx + ",pga:" + pgaVal + ",ratioIdx:" + ratioIdx);  // 输出日志

        ms_sleep(100);  // 延时100ms
        int triSrc = Math.max(ch,0);  // 获取触发源（-1转为0）
        
        // 根据校准模式设置通道
        if(ch < 0) {
            // 校准所有通道
            triggerEdge.setTriggerSource(triSrc);  // 设置触发源
            // 遍历所有通道
            for(int k = 0;k<channelNums;k++){
                finished[k] = false;  // 标记通道未完成
                ch_en[k] = true;      // 使能通道
                // 重置通道系数
                cabteRegister.rst_coefChannel(k,ratioIdx,resistanceType);
            }
            chIdx = 0;  // 从第一个通道开始

        } else {
            // 校准单个通道
            cabteRegister.rst_coefChannel(ch,ratioIdx,resistanceType);  // 重置通道系数
            triggerEdge.setTriggerSource(triSrc);  // 设置触发源
            finished[ch] = false;  // 标记通道未完成
            ch_en[ch] = true;      // 使能通道
            chIdx = ch;            // 设置当前通道索引
        }
        ms_sleep(200);  // 延时200ms
        
        // 遍历所有通道设置参数
        for(int i=0; i<channelNums; i++) {
            channel[i].setResistanceType(resistanceType);  // 设置阻抗类型
            channel[i].setProbeRate(1);  // 设置探头比例为1:1
            // 设置垂直档位
            channel[i].setVScaleId(CabteRegister.getRatioIdx2Dang(resistanceType,ratioIdx));
            // 计算每像素对应的ADC值
            x = channel[i].getADVerticalPerPix()/channel[i].getProbeRate();
            // 计算波形位置
            chpos[i] = srcAmp[i] / (2 * x);
            channel[i].setPos(-(int)(chpos[i]));  // 设置波形位置
        }
        chflag = 0;  // 清零通道标志
        double levelPos = -channel[triSrc].getPos();  // 计算触发电平位置
        triggerEdge.getTriggerLevel().setPos(levelPos);  // 设置触发电平
        setPgaAndAdfs();  // 设置PGA和ADC满量程

        // 等待信号稳定，检查3次参数
        for(int i=0;i<3;i++){
            ms_sleep(1100);  // 延时1.1秒
            checkParam();     // 检查参数是否变化
        }
        ms_sleep(150);  // 延时150ms
        setPgaAndAdfs();  // 再次设置PGA和ADC满量程
        rstCalculate();   // 重置计算变量
        fpgaSync();       // 同步FPGA
        delaySet(2);      // 延时2ms
        errcode = 0;      // 清零错误码
        
        resultString.add("chGainCalibrate start");  // 添加结果字符串
        Log.i(TAG1,"chGainCalibrate start");        // 输出日志

    }
    
    /**
     * 设置PGA和ADC满量程配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置所有通道的PGA增益值</li>
     *   <li>设置所有通道的ADC满量程值</li>
     *   <li>执行ADC校准命令</li>
     * </ul>
     */
    private void setPgaAndAdfs(){

        setPGAVal(pgaVal);  // 设置PGA增益值
        FPGACommand fpgaCommand = FPGACommand.getInstance();  // 获取FPGA命令实例
        
        // 遍历所有通道设置ADC满量程
        for(int i=0;i<channelNums;i++){
            // 写入ADC满量程配置
            // 参数：FPGA索引, ADC索引, 通道索引, 满量程值
            fpgaCommand.writeAD_gain(i/4,i % 4 / 2,i % 2, adfs);
        }
        fpgaCommand.ADCalibrate();  // 执行ADC校准命令
    }

    /**
     * 检查参数是否变化
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查通道位置是否正确</li>
     *   <li>检查阻抗类型是否正确</li>
     *   <li>检查触发源和触发电平是否正确</li>
     * </ul>
     * 
     * @return true表示参数有变化，false表示参数稳定
     */
    @Override
    public boolean checkParam() {
        boolean bChange = false;  // 参数变化标志
        int chidx = Math.max(ch,0);  // 获取有效通道索引
        
        // 检查是否为动态通道
        if(ChannelFactory.isDynamicCh(chidx)) {
            // 遍历所有通道检查参数
            for(int i=0;i<channelNums;i++){
                double pp = -Math.round(chpos[i]);  // 计算目标位置
                // 检查通道位置是否正确
                if(Math.abs(pp - channel[i].getPos()) > 1){
                    bChange = true;  // 标记参数变化
                }
                // 检查阻抗类型是否正确
                if(channel[i].getResistanceType() != resistanceType){
                    bChange = true;  // 标记参数变化
                    break;           // 发现不一致，退出循环
                }
            }
            
            // 如果参数有变化
            if(bChange){
                updateSync();  // 更新同步
                // 遍历所有通道重新设置参数
                for(int i=0;i<channelNums;i++){
                    double pp = -Math.round(chpos[i]);  // 计算目标位置
                    // 重新设置通道位置
                    if(Math.abs(pp - channel[i].getPos()) > 1){
                        channel[i].setPos(pp);
                    }
                    // 重新设置阻抗类型
                    if(channel[i].getResistanceType() != resistanceType){
                        channel[i].setResistanceType(resistanceType);
                        channel[i].setVScaleId(CabteRegister.getRatioIdx2Dang(resistanceType,ratioIdx));
                    }
                }
            }

            int triSrc = Math.max(0,ch);  // 获取触发源

            TriggerFactory triggerFactory = TriggerFactory.getInstance();  // 获取触发工厂实例

            TriggerEdge triggerEdge = (TriggerEdge) (triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));
            // 检查触发源是否正确
            if (!triggerEdge.isTriggerSource(triSrc)) {
                triggerEdge.setTriggerSource(triSrc);  // 重新设置触发源
                bChange = true;  // 标记参数变化
            }

            double levelPos = -channel[triSrc].getPos();  // 计算触发电平位置
            // 检查触发电平是否正确
            if (Math.abs(triggerEdge.getTriggerLevel().getPos() - levelPos) > 1) {
                triggerEdge.getTriggerLevel().setPos(levelPos);  // 重新设置触发电平
                bChange = true;  // 标记参数变化
            }
        }
        
        // 如果参数有变化
        if(bChange){
            ms_sleep(100);   // 延时100ms
            setPgaAndAdfs(); // 重新设置PGA和ADC满量程
            rstCalculate();  // 重置计算变量
            fpgaSync();      // 同步FPGA
        }
        return bChange;  // 返回参数变化状态
    }


    /**
     * 设置错误码
     * 
     * @param errcode 错误码
     */
    @Override
    public void setErrcode(int errcode) {
        this.errcode = errcode;  // 保存错误码
    }

    /**
     * 检查信号幅度是否满足要求
     * 
     * <p>检查测量幅度与标准幅度的偏差是否小于5%
     * 
     * @param v 测量幅度值
     * @return true表示满足要求，false表示不满足
     */
    private boolean isCheckSig(double v){
        double vv = ScopeBase.getVerticalPerGridPixels() * 8;  // 标准幅度（8格像素）
        return (Math.abs(v - vv) / vv) < 0.05;  // 偏差小于5%
    }
    
    /** 通道标志，用于传递校准信息 */
    private volatile int chflag = 0;
    
    /**
     * 执行校准
     * 
     * <p><b>校准算法：</b>
     * <pre>
     * 1. 采集波形数据
     * 2. 计算波形高电平和低电平平均值
     * 3. 计算波形幅度（高电平-低电平）
     * 4. 根据校准类型处理：
     *    - PGA步进校准：保存PGA步进值
     *    - ADC满量程校准：计算ADC满量程系数
     * 5. 更新校准寄存器
     * </pre>
     * 
     * @return true表示校准完成，false表示校准进行中
     */
    @Override
    public boolean onCalibrate() {
        // 等待硬件操作完成
        if (!isFinishedAction()) {
            return false;  // 硬件操作未完成，继续等待
        }

        delaySet(0);  // 设置延时为0
        
        // 检查校准索引有效性
        if(idx < 0){
            setErrcode(3005);  // 设置错误码
            return true;        // 校准失败，结束
        }

        // 检查参数是否变化
        if (checkParam()) {
            return false;  // 参数变化，重新开始
        }
        
        double sum1,sum2,m1,m2;  // 临时变量
        WaveData waveData;       // 波形数据
        HwConfig hwConfig = HwConfig.getInstance();  // 获取硬件配置实例
        int N=0;  // 波形长度
        
        // 遍历所有通道获取波形数据
        for (int i = 0; i < channelNums; i++) {
            waveData = (WaveData) getWave(i);  // 获取波形数据
            // 检查波形数据有效性
            if (waveData == null || (N = waveData.getWaveLength()) < 10) {
                return false;  // 波形数据无效，继续等待
            }
            
            int n = N / ScopeBase.getHorizonGridCnt();  // 每格采样点数
            int len1 = n * (ScopeBase.getHorizonGridCnt()/2 - 2);  // 计算长度
            
            // 计算低电平平均值（波形前半部分）
            sum1 = MathNative.calcSum(waveData.getByteBuffer(), n, len1);
            // 计算高电平平均值（波形后半部分）
            sum2 = MathNative.calcSum(waveData.getByteBuffer(), n * (ScopeBase.getHorizonGridCnt()/2 + 1), len1);
            // 计算高电平最大值
            m1 = MathNative.calcMax(waveData.getByteBuffer(),n * (ScopeBase.getHorizonGridCnt()/2 + 1), len1) ;
            // 计算低电平最小值
            m2 = MathNative.calcMin(waveData.getByteBuffer(),n,len1) ;
            
            // 累加波形幅度（高电平-低电平）
            average[i] += Math.abs(sum2/len1 - sum1/len1);

            double chPos = channel[i].getPos();  // 获取通道位置
            int h = ScopeBase.getHeight()/2;     // 屏幕半高度
            // 限制位置范围
            if(chPos < -h){
                chPos = -h;
            }else if(chPos > h){
                chPos = h;
            }

            // 计算实际最小值和最大值（考虑波形位置）
            min[i] = m2 * hwConfig.getWavFactor()  + chPos;
            max[i] = m1 * hwConfig.getWavFactor()  + chPos;

        }
        
        boolean bPosChange = false;  // 位置变化标志
        
        // 遍历所有通道检查波形是否超出屏幕
        for(int i=0;i<channelNums;i++){
            // 检查通道是否参与校准
            if((!ch_en[i])){
                continue;  // 跳过未使能的通道
            }
            int h = ScopeBase.getHeight()/2;  // 屏幕半高度
            double chPos = channel[i].getPos();  // 获取通道位置

            int v = ScopeBase.getVerticalPerGridPixels()/4;  // 最小调整量
            // 计算默认系数
            double coef = cabteRegister.vol_ChannelCoef_defaultEx(i,CabteRegister.getRatioIdx2Dang(resistanceType,ratioIdx),pgaVal);
            // 计算当前系数
            double coef1 = cabteRegister.calc_coefChannel(i,channel[i].getVScaleVal()/channel[i].getProbeRate(),0);

            double minVal = min[i];  // 获取最小值
            double maxVal = max[i];  // 获取最大值
            Log.d(TAG,"ch:" + i + ",minVal:" + minVal + ",maxVal:" + maxVal);  // 输出日志

            // 检查波形是否超出屏幕下方
            if(minVal < -h){
                // 波形超出下方，向上调整位置
                chPos += Math.max((h - maxVal) / 2,v) * coef / coef1;
                bPosChange = true;  // 标记位置变化
            }else if(maxVal > h){
                // 波形超出上方，向下调整位置
                chPos -= Math.max((h + minVal) / 2,v) * coef / coef1;
                bPosChange = true;  // 标记位置变化
            }
            // 如果位置有变化
            if(bPosChange){
                chpos[i] = -chPos;  // 更新位置数组
                channel[i].setPos(chPos);  // 设置新位置
            }
        }

        // 如果位置有变化
        if(bPosChange){
            ms_sleep(100);  // 延时100ms
            // 检查参数是否稳定
            if(!checkParam()) {
                setPgaAndAdfs();  // 重新设置PGA和ADC满量程
                rstCalculate();   // 重置计算变量
                updateSync();     // 更新同步
            }
            return false;  // 继续校准
        }

        // 检查测量次数是否足够
        if(++meatCnt < 5) {
            return false;  // 测量次数不足，继续采集
        }

        // 计算平均值
        for(int i=0; i<channelNums; i++){
            average[i] = average[i] / meatCnt;  // 计算平均幅度
            average[i] *= hwConfig.getWavFactor();  // 应用波形缩放因子
            Log.d(TAG,"ch:" + i + ",average:" + average[i] + ",ch_en:" + ch_en[i]);  // 输出日志
        }

        meatCnt = 0;  // 清零测量计数

        double [] newAmp = new double[ChannelFactory.CH_CNT];  // 新幅度数组
        Arrays.fill(newAmp,0);  // 初始化为0
        int ampCnt = 0;  // 幅度计数
        boolean bCheckSig = true;  // 信号检查标志

        // 遍历所有通道处理校准结果
        for(int i=0; i<channelNums; i++) {
            // 状态机控制流程
            if((!ch_en[i])){
                continue;  // 跳过未使能的通道
            }
            chflag |= (1 << i);  // 设置通道标志
            
            // 根据校准索引处理
            if(idx < 3){
                // PGA步进校准
                switch(idx){
                    case 0:
                        // 保存PGA步进值1
                        hw.setGainPgaA1(i,average[i]);
                        break;
                    case 1:
                        // 保存PGA步进值2
                        hw.setGainPgaA2(i,average[i]);
                        break;
                    case 2:
                        // 保存PGA步进值3
                        hw.setGainPgaA3(i,average[i]);
                        break;
                }
            }else{
                // ADC满量程校准
                chflag |= (idx/3 - 1) << 24;  // 设置档位索引

                bCheckSig = bCheckSig && isCheckSig(average[i]);  // 检查信号幅度
                ampCnt++;  // 计数加1
                // 计算新的输入幅度
                newAmp[i] = srcAmp[i] * 1024 /10.23/average[i];
                Log.d(TAG,"newAmp ch" + i +":" + newAmp[i] + ",srcAmp:" + srcAmp[i] + "," + average[i]);
                // 设置增益ADC满量程系数D
                hw.setGainAdFsD(i,2,idx % 3,0,newAmp[i]);
                chflag |= 2 << 16;  // 设置通道模式标志

            }
        }
        
        // 清零平均值数组
        for(int i=0;i<channelNums;i++){
            average[i] = 0;
        }
        
        // 根据校准类型处理结果
        if(idx >= 3){
            // ADC满量程校准
            for (int i=0;i<newAmp.length;i++) {
                Log.d(TAG,"newAmp ch" + i +":" + newAmp[i] + ",srcAmp:" + srcAmp[i]);  // 输出日志
            }
            cabteRegister.setNewInputV(newAmp);  // 设置新的输入电压
            cabteRegister.calcGain(chflag);      // 计算增益系数
            chflag = 0;  // 清零通道标志
            
            // 检查信号是否满足要求
            if(!bCheckSig){
                setErrcode(3007);  // 设置错误码
            }
        }else if(idx == 2){
            // PGA步进校准完成
            cabteRegister.calcPgaStep(chflag);  // 计算PGA步进值
            chflag = 0;  // 清零通道标志
        }

        FPGACommand fpgaCommand = FPGACommand.getInstance();
        fpgaCommand.setADdiffGainCalib(false);  // 关闭ADC差分增益校准模式
        rstCalculate();  // 重置计算变量
        
        // 如果校准成功，设置校准状态
        if(errcode == 0) {
            if (ch < 0) {
                // 校准所有通道
                for (int i = 0; i < channelNums; i++) {
                    hw.setCalibrationState(HW_MHO68V2.CALIBRATION_CENTER_CH1GAIN_1 + i * 21 + idx, true);
                }
            } else {
                // 校准单个通道
                hw.setCalibrationState(HW_MHO68V2.CALIBRATION_CENTER_CH1GAIN_1 + ch * 21 + idx, true);
            }
        }
        return true;  // 校准完成
    }

}
