package com.micsig.tbook.scope.Calibrate.MHO68v2;

import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Action.ChannelHardw;
import com.micsig.tbook.scope.Calibrate.Calibrate;
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
 * MHO68 V2示波器通道增益校准扩展实现类
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
 *   <li>实现MHO68 V2示波器的通道增益校准（扩展版本）</li>
 *   <li>支持单通道或全通道同时校准</li>
 *   <li>支持1MΩ和50Ω两种阻抗模式</li>
 *   <li>通过迭代算法调整ADC增益值</li>
 * </ul>
 * 
 * <p><b>与MHO68v2_ChGainCalibrate的区别：</b>
 * <pre>
 * MHO68v2_ChGainCalibrate：
 *   - 用于PGA步进校准和ADC满量程校准
 *   - 使用固定的PGA和ADC满量程配置
 *   - 校准索引idx决定校准类型
 * 
 * MHO68v2_ChGainCalibrateEx（本类）：
 *   - 用于标准增益校准（第二组增益校准）
 *   - 动态调整ADC增益值
 *   - 根据垂直档位和阻抗类型进行校准
 *   - 迭代调整直到误差满足要求
 * </pre>
 * 
 * <p><b>增益校准原理：</b>
 * <pre>
 * 校准目的：
 *   校准通道的ADC增益值，确保测量精度。
 *   通过调整ADC增益寄存器，使测量幅度与标准幅度匹配。
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
 *   │     ├── 设置垂直档位和阻抗类型                               │
 *   │     └── 设置波形位置                                        │
 *   │                                                             │
 *   │  2. onCalibrate() - 执行阶段                                │
 *   │     ├── 采集波形数据                                        │
 *   │     ├── 计算波形幅度（高电平-低电平）                        │
 *   │     ├── 计算误差                                            │
 *   │     ├── 调整ADC增益值                                       │
 *   │     └── 迭代直到误差小于阈值                                │
 *   │                                                             │
 *   │  3. 保存校准结果                                            │
 *   └─────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>迭代算法说明：</b>
 * <pre>
 * 误差计算：
 *   k = |average[i] - kk|
 *   其中：average[i] = 测量幅度，kk = 标准幅度
 * 
 * 增益调整：
 *   如果误差小于阈值：
 *     - 记录最佳值
 *     - 连续3次满足条件则完成
 *   如果误差大于阈值：
 *     - 根据误差大小计算调整步进
 *     - 调整ADC增益值
 * 
 * 调整步进计算：
 *   k < 1: s = 1
 *   k < 3: s = round(k × 5)
 *   k >= 3: s = round(k × 10)
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
 *   <li>出厂校准时校准所有通道的标准增益</li>
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
 * @see MHO68v2_ChGainCalibrate 增益校准基础版本
 */
public class MHO68v2_ChGainCalibrateEx extends Calibrate {
    
    /**
     * 构造函数
     * 
     * <p>调用父类构造函数，初始化校准类型，并设置初始延时为2ms
     * 
     * @param calibrateType 校准类型标识
     */
    public MHO68v2_ChGainCalibrateEx(int calibrateType) {
        super(calibrateType);  // 调用父类Calibrate的构造函数
        delaySet(2);            // 设置初始延时为2毫秒
    }

    /** 错误码，用于标识校准结果 */
    private int errcode;
    
    /** 日志标签：通道增益校准扩展版 */
    private final String TAG = "ChGainEx";
    
    /** 完整日志标签：包含优先级前缀 */
    private final String TAG1=TAG_PRI+":"+TAG;
    
    /** 当前校准通道索引，volatile保证多线程可见性 */
    private volatile int ch=0;
    
    /** 当前处理的通道索引 */
    private volatile int chIdx = 0;


    /** 校准步骤标识 */
    private volatile int step;
    
    /** 测量计数器 */
    private int meatCnt;
    
    /** 标准幅度数组（V），每个通道的校准信号标准幅度 */
    private volatile double [] stdAmp = {0,0,0,0,0,0,0,0};
    
    /** 垂直档位值 */
    private volatile double vScaleVal;
    
    /** 阻抗类型（1MΩ或50Ω） */
    private volatile int resistanceType = Channel.RESISTANCE_1M;

    /** 垂直档位索引 */
    private volatile int dwIdx = VerticalAxis.DANG_NONE;

    /** 通道模式（采样通道数） */
    private volatile int chMode = 0;

    /** 波形平均值数组，用于计算波形幅度 */
    private double average[] = new double[ChannelFactory.CH_CNT];
    
    /** 波形最小值数组 */
    private double min[] = new double[ChannelFactory.CH_CNT];
    
    /** 波形最大值数组 */
    private double max[] = new double[ChannelFactory.CH_CNT];

    /** 最佳值数组，记录迭代过程中的最优值 */
    private double[] best_value = new double[ChannelFactory.CH_CNT];
    
    /** 备份系数数组 */
    private int[] bakCoef = new int[ChannelFactory.CH_CNT];
    
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
            finished[i] = false;               // 标记通道未完成
        }
        meatCnt = 0;  // 清零测量计数
    }

    /**
     * 初始化校准寄存器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取通道硬件操作实例</li>
     * </ul>
     */
    @Override
    public void iniCalibrateReg(){
        channelHardw = ChannelHardw.getInstance();  // 获取通道硬件操作单例
    }

    /**
     * 设置校准参数
     * 
     * <p><b>参数格式：</b>
     * <pre>
     * vol为double[]数组：
     *   vol[0]: 通道索引（0~7表示单通道，-1表示所有通道）
     *   vol[1]: 垂直档位值
     *   vol[2]: 阻抗类型（50或1000000）
     *   vol[3]: 标准幅度（单通道时使用）
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

                    resistanceType = (int)(param[2] + 0.1);  // 保存阻抗类型
                    
                    // 设置标准幅度
                    if(ch  < 0){
                        // 所有通道：从校准寄存器获取每个通道的幅度
                        StringBuilder sb = new StringBuilder();

                        sb.append("stdAmp =>");
                        // 遍历所有通道获取幅度
                        for(int i=0;i<stdAmp.length;i++){
                            stdAmp[i] = cabteRegister.getChAmp(i);  // 获取通道幅度
                            sb.append("ch").append(i).append(":").append(stdAmp[i]).append(",");
                        }
                        Log.d(TAG,sb.toString());  // 输出日志
                    }else{
                        // 单通道：使用参数中的幅度
                        Arrays.fill(stdAmp,param[3]);
                    }

                    // 转换阻抗类型
                    if(resistanceType == 50) resistanceType = Channel.RESISTANCE_50;
                    else if(resistanceType == 1000000) resistanceType = Channel.RESISTANCE_1M;


                    // 根据档位值获取档位索引
                    dwIdx = VerticalAxis.getScaleIdByValue(vScaleVal);
                    
                    // 输出调试日志
                    Logger.d(TAG,"ch:" + ch
                            + ",vScaleVal:" + vScaleVal
                            + ",srcAmp:" + stdAmp
                            + ",resistanceType:" + resistanceType
                    );
                    // 设置初始阻抗类型
                    setInitResistanceType(resistanceType);
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
     * <p><b>返回格式：</b>
     * <pre>
     * int[0]: 通道索引
     * int[1]: 阻抗类型 × 档位数 + 档位索引
     * </pre>
     * 
     * @return 参数数组
     */
    @Override
    public Object getParam() {
        param[0] = ch;  // 设置通道索引
        // 计算校准项索引
        param[1] = resistanceType * VerticalAxis.DANG_CNT + dwIdx;
        return param;  // 返回参数数组
    }


    /**
     * 校准准备阶段
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置时基、触发、通道参数</li>
     *   <li>设置垂直档位和阻抗类型</li>
     *   <li>设置波形位置</li>
     *   <li>等待信号稳定</li>
     * </ul>
     */
    @Override
    public void calibratePrepare() {

        // 初始化完成标志和使能标志
        for(int i=0; i<finished.length; i++) {
            finished[i] = false;  // 标记通道未完成
            // 设置通道使能：所有通道或指定通道
            ch_en[i] = ch < 0 || ch == i;
        }
        chIdx = Math.max(ch,0);  // 获取有效通道索引（-1转为0）
        
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
        // 设置触发源
        triggerEdge.setTriggerSource(chIdx);

        Logger.d(TAG,"resistanceType:" + resistanceType + ",vScaleVal:" + vScaleVal);  // 输出调试日志

        ms_sleep(200);  // 延时200ms

        // 遍历所有通道设置参数
        for(int i=0; i<channelNums; i++) {
            channel[i].setResistanceType(resistanceType);  // 设置阻抗类型
            channel[i].setProbeRate(1);  // 设置探头比例为1:1
            channel[i].setVScaleId(dwIdx);  // 设置垂直档位
            // 设置波形位置（负值，波形在屏幕下方）
            channel[i].setPos((int) -Math.round(stdAmp[i] / (2 * channel[i].getADVerticalPerPix())));
        }

        chMode = 3;  // 设置通道模式为4通道
        step = 0;    // 初始化步骤为0
        
        // 计算触发电平位置
        double levelPos = -channel[chIdx].getPos();
        triggerEdge.getTriggerLevel().setPos(levelPos);  // 设置触发电平

        // 等待信号稳定，检查3次参数
        for(int i=0;i<3;i++){
            ms_sleep(1000);  // 延时1秒
            checkParam();     // 检查参数是否变化
        }

        rstCalculate();  // 重置计算变量
        fpgaSync();      // 同步FPGA
        delaySet(2);     // 延时2ms
        errcode = 0;     // 清零错误码
        
        resultString.add("chGainCalibrate start");  // 添加结果字符串
        Log.i(TAG1,"chGainCalibrate start");        // 输出日志

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
                // 计算目标位置
                double pp = -Math.round(stdAmp[i] / (2 * channel[i].getADVerticalPerPix()));
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
                    // 计算目标位置
                    double pp = -Math.round(stdAmp[i] / (2 * channel[i].getADVerticalPerPix()));
                    // 重新设置通道位置
                    if(Math.abs(pp - channel[i].getPos()) > 1){
                        channel[i].setPos(pp);
                    }
                    // 重新设置阻抗类型
                    if(channel[i].getResistanceType() != resistanceType){
                        channel[i].setResistanceType(resistanceType);
                    }
                }
            }

            int triSrc = Math.max(ch,0);  // 获取触发源

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
     * 执行校准
     * 
     * <p><b>校准算法：</b>
     * <pre>
     * 1. 采集波形数据
     * 2. 计算波形高电平和低电平平均值
     * 3. 计算波形幅度（高电平-低电平）
     * 4. 计算误差：k = |average[i] - kk|
     * 5. 根据误差调整ADC增益值：
     *    - 误差小于阈值：记录最佳值，连续3次满足则完成
     *    - 误差大于阈值：计算调整步进，调整增益值
     * 6. 迭代直到所有通道完成
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

        // 检查参数是否变化
        if(checkParam()){
            return false;  // 参数变化，重新开始
        }
        
        double sum1,sum2;  // 临时变量
        WaveData waveData;  // 波形数据
        HwConfig hwConfig = HwConfig.getInstance();  // 获取硬件配置实例
        int N;  // 波形长度

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
            
            // 累加波形幅度（高电平-低电平）
            average[i] += Math.abs(sum2/len1 - sum1/len1);
        }

        // 检查测量次数是否足够
        if(++meatCnt < 5) {
            return false;  // 测量次数不足，继续采集
        }

        // 计算平均值
        for(int i=0; i<channelNums; i++){
            average[i] = average[i] / meatCnt;  // 计算平均幅度
            // 应用波形缩放因子和Y因子
            average[i] *= hwConfig.getWavFactor() * channel[i].getYFactor();
        }

        FPGACommand fpgaCommand = FPGACommand.getInstance();  // 获取FPGA命令实例
        boolean bOK = true;  // 所有通道完成标志
        
        // 遍历所有通道进行增益调整
        for(int i=0; i<channelNums; i++) {
            // 状态机控制流程
            if((!ch_en[i])){
                continue;  // 跳过未使能的通道
            }
            
            // 计算误差阈值
            double thresholdV = channel[i].getYFactor() * 0.7;
            {
                // 获取当前ADC增益值
                int m = cabteRegister.getChGain(i, resistanceType, dwIdx, 2, 0);
                // 计算标准幅度对应的像素值
                double kk = stdAmp[i] / channel[i].getADVerticalPerPix();
                // 计算误差
                double k = Math.abs(average[i] - kk);
                Log.d(TAG,"ch:" + i + ",average:" + average[i] + ",kk:" + kk + ",m:" + m);  // 输出日志
                
                // 检查误差是否小于阈值
                if (k < thresholdV) {
                    // 误差满足要求
                    if(k < best_value[i]) {
                        best_value[i] = average[i];  // 更新最佳值
                        bakCoef[i] =  m;              // 备份当前增益值
                    }
                    tryNum[i]++;  // 尝试次数加1
                    // 连续3次满足条件则完成
                    if(tryNum[i] > 3) {
                        finished[i] = true;  // 标记通道完成
                    }
                }else{
                    // 误差不满足要求，需要调整增益
                    tryNum[i] = 0;  // 清零尝试次数
                    k /= channel[i].getYFactor();  // 归一化误差
                    
                    // 根据误差大小计算调整步进
                    int s = (int)((k < 1 ? 1 : k < 3 ? Math.round(k*5) : Math.round(k*10)) + 0.1);
                    
                    // 根据误差方向调整增益
                    if(average[i] > kk){
                        m += s;  // 测量值偏大，增大增益
                    }else{
                        m -= s;  // 测量值偏小，减小增益
                    }
                    m &= 0xFFFF;  // 限制在16位范围内
                    
                    // 保存新的增益值
                    cabteRegister.setChGain(i, resistanceType, dwIdx, 2, 0, m);
                    // 写入FPGA
                    fpgaCommand.writeAD_gain(i/4,i % 4 / 2,i % 2,m);
                }
                // 更新完成标志
                bOK = bOK && finished[i];
            }
        }
        
        // 检查是否所有通道都完成
        if(bOK){
            // 所有通道完成，保存最佳增益值
            for(int i=0;i<channelNums;i++) {
                if (!ch_en[i]) {
                    continue;  // 跳过未使能的通道
                }
                // 设置最佳增益值
                cabteRegister.setChGain(i, resistanceType, dwIdx, 2, 0, bakCoef[i]);
            }
            rstCalculate();  // 重置计算变量
            fpgaSync();      // 同步FPGA

        }else{
            // 还有通道未完成，继续迭代
            meatCnt = 0;  // 清零测量计数
            // 清零平均值数组
            for(int i=0;i<channelNums;i++){
                average[i] = 0;
            }
            fpgaSync();      // 同步FPGA
            return false;    // 继续校准
        }

        fpgaCommand.setADdiffGainCalib(false);  // 关闭ADC差分增益校准模式
        rstCalculate();  // 重置计算变量
        
        // 如果校准成功，设置校准状态
        if(errcode == 0){
            param[0] = ch;  // 保存通道索引
            // 计算校准项索引
            param[1] = resistanceType * VerticalAxis.DANG_CNT + dwIdx;
            
            // 设置校准状态
            if(ch < 0){
                // 校准所有通道
                for(int i=0;i<channelNums;i++){
                    cabteRegister.setCalibrationState(HW_MHO68V2.CALIBRATION_CENTER_CH1GAIN_2
                            + i * VerticalAxis.DANG_CNT * 2+ param[1],true);
                }
            }else{
                // 校准单个通道
                cabteRegister.setCalibrationState(HW_MHO68V2.CALIBRATION_CENTER_CH1GAIN_2
                        + ch * VerticalAxis.DANG_CNT * 2+ param[1],true);
            }

        }
        return true;  // 校准完成
    }

}
