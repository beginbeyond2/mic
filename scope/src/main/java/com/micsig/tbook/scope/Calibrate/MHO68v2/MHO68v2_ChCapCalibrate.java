package com.micsig.tbook.scope.Calibrate.MHO68v2;

import android.util.Log;

import com.micsig.tbook.scope.Calibrate.CabteRegister;
import com.micsig.tbook.scope.Calibrate.Calibrate;
import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.Sample.Sample;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.Trigger.Trigger;
import com.micsig.tbook.scope.Trigger.TriggerCommon;
import com.micsig.tbook.scope.Trigger.TriggerEdge;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.math.MathNative;
import com.micsig.tbook.scope.vertical.VerticalAxis;

/**
 * MHO68 V2示波器通道电容校准实现类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate.MHO68v2（MHO68 V2示波器校准模块）</li>
 *   <li>架构层级：业务逻辑层 - 具体校准实现</li>
 *   <li>设计模式：继承Calibrate抽象基类，实现电容校准流程</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现MHO68 V2示波器的通道输入电容校准</li>
 *   <li>通过迭代算法找到最优电容补偿值</li>
 *   <li>支持单通道和全通道校准</li>
 *   <li>记录校准结果和状态</li>
 * </ul>
 * 
 * <p><b>电容校准原理：</b>
 * <pre>
 * 输入电容校准目的：
 *   示波器输入端存在寄生电容，会导致高频信号衰减和相位失真。
 *   通过调整补偿电容，使输入端的RC时间常数匹配，实现平坦的频率响应。
 * 
 * 校准方法：
 *   1. 输入标准方波信号
 *   2. 测量方波的上升沿和顶部电平
 *   3. 调整电容值使上升沿最陡峭（无过冲或欠补偿）
 *   4. 迭代直到找到最优值
 * 
 * 校准流程：
 *   ┌─────────────────────────────────────────────────────────────┐
 *   │  1. calibratePrepare() - 准备阶段                           │
 *   │     ├── 设置时基为10us/div                                  │
 *   │     ├── 设置触发为边沿触发、上升沿                           │
 *   │     ├── 设置垂直档位为500mV/div                             │
 *   │     └── 初始化电容值为中间值                                 │
 *   │                                                             │
 *   │  2. onCalibrate() - 执行阶段                                │
 *   │     ├── 采集波形数据                                        │
 *   │     ├── 计算上升沿和顶部平均值                              │
 *   │     ├── 判断补偿状态（过补偿/欠补偿/正常）                   │
 *   │     ├── 调整电容值                                          │
 *   │     └── 迭代直到收敛                                        │
 *   │                                                             │
 *   │  3. 保存校准结果                                            │
 *   └─────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>电容值范围：</b>
 * <pre>
 * 电容控制值范围：0 ~ 0xFFFF (16位)
 * 中间值：0x7FFF (约32767)
 * 
 * 调整策略：
 *   - 初始步进：CAP_REF_VAL / 2
 *   - 步进逐次减半，实现二分搜索
 *   - 最大迭代次数：16次
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：Calibrate（校准抽象基类）</li>
 *   <li>依赖：CabteRegister（校准寄存器管理）</li>
 *   <li>依赖：HW_MHO68V2（硬件操作，校准状态常量）</li>
 *   <li>依赖：WaveData（波形数据）</li>
 *   <li>依赖：MathNative（数学计算库）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>出厂校准时校准所有通道的输入电容</li>
 *   <li>用户手动校准单个通道</li>
 *   <li>定期维护校准</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 2.0
 * @since 2019
 * @see Calibrate 校准抽象基类
 * @see CabteRegister 校准寄存器管理
 * @see HW_MHO68V2 硬件操作实现
 */
public class MHO68v2_ChCapCalibrate extends Calibrate {
    
    /**
     * 构造函数
     * 
     * <p>调用父类构造函数，初始化校准类型
     * 
     * @param calibrateType 校准类型标识
     */
    public MHO68v2_ChCapCalibrate(int calibrateType) {
        super(calibrateType);  // 调用父类Calibrate的构造函数

    }
    
    /**
     * 设置错误码
     * 
     * <p>错误码定义：
     * <ul>
     *   <li>0: 校准成功</li>
     *   <li>705: 校准失败（信号幅度不满足要求）</li>
     * </ul>
     * 
     * @param errcode 错误码
     */
    @Override
    public void setErrcode(int errcode) {
        this.errcode = errcode;  // 保存错误码
    }
    
    /** 错误码，用于标识校准结果 */
    private int errcode;
    
    /** 日志标签：电容校准 */
    private final String TAG = "ChCap";
    
    /** 完整日志标签：包含优先级前缀 */
    private final String TAG1=TAG_PRI+":"+TAG;

    /** 当前校准通道索引，volatile保证多线程可见性 */
    private volatile int ch=0;
    
    /** 校准使用的垂直档位值，默认500mV/div */
    private double vScaleVal = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_500mV);

    /** 测量计数器，用于统计测量次数 */
    private volatile int meatCnt=0;
    
    /** 校准步骤标识 */
    private volatile int step;
    
    /**
     * 波形平均值数组
     * 
     * <p><b>数组结构：</b> [2][8]
     * <ul>
     *   <li>第一维：0=上升沿平均值，1=顶部平均值</li>
     *   <li>第二维：通道索引（0-7）</li>
     * </ul>
     */
    private volatile double average[][]={{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0}};

    /**
     * 最小差值数组
     * 
     * <p>记录每个通道在迭代过程中找到的最小差值
     */
    private volatile double []minVal={Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE
            ,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE};
    
    /**
     * 最优电容值数组
     * 
     * <p>记录每个通道找到最优值时的电容设置
     */
    private volatile int [] dcpVal = {0,0,0,0,0,0,0,0};
    
    /**
     * 连续未改善计数数组
     * 
     * <p>记录每个通道连续多少次没有找到更优值
     * 当超过16次时，认为已收敛
     */
    private volatile int [] minNum = {0,0,0,0,0,0,0,0};
    
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
    
    /** 电容控制最大值（16位） */
    private static final int CAP_MAX_VAL = 0xFFFF;
    
    /** 电容参考值（中间值） */
    private static final int CAP_REF_VAL = CAP_MAX_VAL/2;

    /** 档位索引，标识当前校准的档位 */
    private int dwIdx = 0;

    /**
     * 重置计算变量
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>在开始新档位校准前调用</li>
     *   <li>清零平均值数组</li>
     *   <li>重置参考值为中间值</li>
     *   <li>重置最小值和计数器</li>
     * </ul>
     */
    private void rstCalculate(){

        // 遍历所有通道
        for (int i = 0; i < channelNums; i++) {
            // 遍历两个平均值（上升沿和顶部）
            for(int j=0; j< 2;j++) {
                average[j][i] =0;           // 清零平均值
                refVal[i] = CAP_REF_VAL;    // 重置参考值为中间值
                minVal[i] = Double.MAX_VALUE;  // 重置最小值为最大值
                dcpVal[i] = 0;              // 清零最优电容值
                minNum[i] = 0;              // 清零连续未改善计数
            }
        }
        meatCnt = 0;  // 清零测量计数
    }

    /**
     * 初始化校准寄存器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>在开始校准前初始化相关寄存器</li>
     *   <li>当前为空实现，可由子类扩展</li>
     * </ul>
     */
    @Override
    public void iniCalibrateReg(){
        // 检查是否为动态通道
        if(ChannelFactory.isDynamicCh(ch)){
            // 预留：可在此设置初始电容值
            //cabteRegister.getChCapacitanceHigh()[ch] = 127;
        }
    }

    /**
     * 设置校准参数
     * 
     * <p><b>参数格式：</b>
     * <pre>
     * vol为double[]数组：
     *   vol[0]: 通道索引（0~7表示单通道，-1表示所有通道）
     *   vol[1]: 垂直档位值（如0.5表示500mV/div）
     * </pre>
     * 
     * @param vol 校准参数对象
     */
    @Override
    public void setParam(Object vol) {
        ch = ChannelFactory.CH1;  // 默认校准通道1
//        vScaleVal = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_500mV);

        // 检查参数类型是否为double数组
        if(vol instanceof double[]) {
            double[] param=(double[])vol;  // 类型转换
            // 检查参数长度
            if(param.length >= 2){
                int ix =(int)(param[0] + 0.1);  // 获取通道索引（加0.1避免浮点误差）
                // 检查是否为负数（表示所有通道）
                if(param[0] < 0){
                    ix = -1;  // 设置为-1表示所有通道
                }
                // 验证通道索引有效性
                if(ix == -1 || ChannelFactory.isDynamicCh(ix)) {
                    ch = ix;  // 保存通道索引
                    vScaleVal = param[1];  // 保存垂直档位值
                    Log.d(TAG,"ch:" + ch + ",vScaleVal:" + vScaleVal);  // 输出日志
                }
            }
        }
        // 设置初始阻抗类型为1MΩ
        setInitResistanceType(Channel.RESISTANCE_1M);
    }

    /** 参数数组，用于返回校准结果 */
    int []param=new int[2];
    
    /**
     * 获取校准参数
     * 
     * <p><b>返回格式：</b>
     * <pre>
     * int[0]: 校准的通道索引
     * int[1]: 校准的档位索引
     * </pre>
     * 
     * @return 参数数组
     */
    @Override
    public Object getParam() {
        param[0] = ch;      // 设置通道索引
        param[1] = dwIdx;   // 设置档位索引
        return param;       // 返回参数数组
    }

    /**
     * 校准准备阶段
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置时基、触发、通道等参数</li>
     *   <li>初始化电容值为中间值</li>
     *   <li>等待信号稳定</li>
     * </ul>
     * 
     * <p><b>配置详情：</b>
     * <pre>
     * 时基配置：10us/div
     * 触发配置：边沿触发、上升沿、正常模式
     * 垂直配置：500mV/div（或用户指定）
     * 位置配置：-4格（波形在屏幕下方）
     * </pre>
     */
    @Override
    public void calibratePrepare() {
        delaySet(1000);  // 延时1秒等待系统稳定
        
        // 根据垂直档位值获取档位索引
        dwIdx = CabteRegister.getRatioIdx(Channel.RESISTANCE_1M,vScaleVal);


        // 获取水平轴实例
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();

        // 设置时基为10us/div
        horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.TSI_10uS);
        
        // 设置时间位置（屏幕中央偏左）
        horizontalAxis.setTimePoseOfViewPix(ScopeBase.getWidth()/2-ScopeBase.getHorizonPerGridPixels()/2);
        
        // 获取触发工厂实例
        TriggerFactory triggerFactory=TriggerFactory.getInstance();
        
        // 设置触发模式为正常模式
        triggerFactory.getTriggerCommon().setTriggerMode(TriggerCommon.TM_NORMAL);
        
        // 获取边沿触发实例
        TriggerEdge triggerEdge=(TriggerEdge)(triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));
        
        // 设置触发边沿为上升沿
        triggerEdge.setTriggerEdge(TriggerEdge.TET_ASC);
        
        // 设置触发源
        if(ChannelFactory.isDynamicCh(ch)){
            triggerEdge.setTriggerSource(ch);  // 单通道：设置为当前通道
        }else{
            triggerEdge.setTriggerSource(0);   // 所有通道：设置为通道1
            ch = -1;  // 标记为校准所有通道
        }
        
        ms_sleep(100);  // 延时100ms等待触发稳定
        
//        int []chCapHigh = cabteRegister.getChCapacitanceHigh();
        // 遍历所有通道进行初始化
        for(int i=0; i<channelNums; i++) {
            channel[i].setProbeRate(1);  // 设置探头比例为1:1
            channel[i].setVScaleVal(vScaleVal);  // 设置垂直档位
            channel[i].setPos(-ScopeBase.getVerticalPerGridPixels() * 4);  // 设置位置为-4格
            
            // 如果是目标通道或校准所有通道
            if(ch == -1 || i == ch) {
                refVal[i] = CAP_REF_VAL;  // 设置参考值为中间值
                // 设置电容值为中间值
                cabteRegister.setChCapacitanceHigh(i,dwIdx,CAP_REF_VAL);
            }
        }
        
        // 设置采样类型为正常模式
        Sample.getInstance().setSampleType(Sample.SAMPLE_TYPE_NORMAL);
        
        // 计算触发电平位置（屏幕上方4格）
        int level = ScopeBase.getVerticalPerGridPixels() * 4;
        triggerEdge.getTriggerLevel().setPos(level);  // 设置触发电平

        // 等待信号稳定，检查3次参数
        for(int i=0;i<3;i++) {
            ms_sleep(1100);  // 延时1.1秒
            checkParam();    // 检查参数是否变化
        }
        
        meatCnt = 0;   // 清零测量计数
        step = 0;      // 初始化步骤为0
        errcode = 0;   // 清零错误码
        rstCalculate();  // 重置计算变量
        fpgaSync();    // 同步FPGA
        delaySet(2);   // 延时2ms
        
        // 添加结果字符串
        resultString.add("<<<<<<<<<< chCapCalibrate start ......");
        Log.i(TAG1,"<<<<<<<<<< chCapCalibrate start ......");

    }

    /**
     * 检查参数是否变化
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查通道位置、触发源、触发电平是否变化</li>
     *   <li>如果变化，重新设置并重置计算</li>
     * </ul>
     * 
     * @return true表示参数有变化，false表示参数稳定
     */
    public boolean checkParam(){
        boolean bChange = false;  // 参数变化标志
        int chidx = ch;           // 获取当前通道索引
        if(ch < 0){
            chidx = 0;  // 如果是所有通道，使用通道1检查
        }

        // 检查是否为动态通道
        if(ChannelFactory.isDynamicCh(chidx)) {
            Channel channel1 = ChannelFactory.getDynamicChannel(chidx);  // 获取通道实例
            int p = ScopeBase.getVerticalPerGridPixels() * 4;  // 计算目标位置

            // 检查通道位置是否正确
            if(Math.abs(channel1.getPos() + p) > 1){
                channel1.setPos(-p);  // 重新设置位置
                bChange = true;       // 标记参数变化
            }
            
            // 获取触发工厂实例
            TriggerFactory triggerFactory = TriggerFactory.getInstance();

            // 获取边沿触发实例
            TriggerEdge triggerEdge = (TriggerEdge) (triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));
            
            // 检查触发源是否正确
            if (!triggerEdge.isTriggerSource(chidx)) {
                triggerEdge.setTriggerSource(chidx);  // 重新设置触发源
                bChange = true;                       // 标记参数变化
            }

            // 检查触发电平是否正确
            if (Math.abs(triggerEdge.getTriggerLevel().getPos() - p) > 1) {
                triggerEdge.getTriggerLevel().setPos(p);  // 重新设置触发电平
                bChange = true;                           // 标记参数变化
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
     * 参考值数组
     * 
     * <p>用于二分搜索时的步进值，初始值为中间值
     */
    private volatile int [] refVal = {CAP_REF_VAL,CAP_REF_VAL,CAP_REF_VAL,CAP_REF_VAL,CAP_REF_VAL,CAP_REF_VAL,CAP_REF_VAL,CAP_REF_VAL};
    
    /** 标准值：8格的像素值，用于判断信号幅度 */
    private static final int STD_VAL = ScopeBase.getVerticalPerGridPixels() * 8;
    
    /**
     * 执行校准
     * 
     * <p><b>校准算法：</b>
     * <pre>
     * 1. 采集波形数据
     * 2. 计算上升沿平均值和顶部平均值
     * 3. 计算差值：diff = 顶部平均值 - 上升沿平均值
     * 4. 根据差值调整电容：
     *    - diff > 0.1：过补偿，减小电容
     *    - diff < -0.1：欠补偿，增大电容
     *    - |diff| <= 0.1：补偿正常，完成校准
     * 5. 使用二分搜索策略逐步逼近最优值
     * </pre>
     * 
     * @return true表示校准完成，false表示校准进行中
     */
    @Override
    public boolean onCalibrate() {
        // 等待硬件操作完成

        if (!isFinishedAction())
            return false;  // 硬件操作未完成，继续等待


        // 步骤0：初始延时
        if(step == 0){
            delaySet(2);  // 延时2ms
            step++;       // 进入下一步
            return false;  // 继续校准
        }
        
        // 步骤1：准备采集
        if(step == 1){
            delaySet(0);   // 无延时
            step++;        // 进入下一步
            meatCnt = 0;   // 清零测量计数
            return false;  // 继续校准
        }
        
        // 每次采集前清零延时
        if(meatCnt == 0){
            delaySet(0);  // 无延时
        }

        // 检查参数是否变化
        if(checkParam()){
            rstCalculate();  // 参数变化，重置计算
            return false;    // 继续校准
        }

//        int []chCapHigh = cabteRegister.getChCapacitanceHigh();

        // 获取硬件配置实例
        HwConfig hwConfig = HwConfig.getInstance();
        WaveData waveData;  // 波形数据
        int N;              // 波形长度
        int gridCnt = ScopeBase.getHorizonGridCnt();  // 获取水平格数
        int xx = ScopeBase.getHorizonPerGridPixels();  // 获取每格像素数
        
        // 遍历所有通道获取波形平均值
        for (int i = 0; i < channelNums; i++) {
            waveData = (WaveData) getWave(i);  // 获取波形数据
            // 检查波形数据有效性
            if (waveData == null || (N = waveData.getWaveLength()) < 10)
                return false;  // 波形数据无效，继续等待
            
            int len1 = N/gridCnt;   // 每格的采样点数
            int len2 = len1/xx;     // 每像素的采样点数
            N = len1/2;             // 计算起始位置
            int n = len2 * 10;      // 计算求和长度（10个像素宽度）
            
            // 计算上升沿平均值（波形中部偏前）
            average[0][i] += (double) MathNative.calcSum(waveData.getByteBuffer(), N + len2 * 5, n) / n;
            // 计算顶部平均值（波形后部）
            average[1][i] += (double) MathNative.calcSum(waveData.getByteBuffer(), N + len1 * 8, n) / n;
        }

        // 检查测量次数是否足够
        if(++meatCnt > 5) {
            // 计算平均值
            for (int i = 0; i < channelNums; i++) {
                average[0][i] /= meatCnt;  // 计算上升沿平均值
                average[1][i] /= meatCnt;  // 计算顶部平均值

                // 应用波形缩放因子
                average[0][i] *= hwConfig.getWavFactor();
                average[1][i] *= hwConfig.getWavFactor();

                Log.d(TAG, "ch:" + ch + ",step:" + step +",====average: " + average[0][i] + "," + average[1][i] + ",STD_VAL:" +STD_VAL);
                
                // 检查信号幅度是否满足要求
                if(ch == i || ch == -1){
                    // 如果顶部平均值与标准值偏差超过20%
                    if(Math.abs(average[1][i] - STD_VAL)/STD_VAL > 0.2){
                        errcode = 705;  // 设置错误码
                        return true;    // 校准失败，结束
                    }
                }
            }
        }else {
            return false;  // 测量次数不足，继续采集
        }

        meatCnt = 0;  // 清零测量计数

        // 步骤2：执行迭代调整
        if(step == 2){
            double TVAL = 0.1;  // 阈值：差值小于0.1认为补偿正常
            boolean [] bOk = {false,false,false,false,false,false,false,false};  // 各通道完成标志
            int idx;
            
            // 遍历所有通道
            for (int i = 0; i < channelNums; i++) {
                // 检查是否为校准目标
                if (ch == i || ch == -1) {
                    // 如果该通道尚未完成
                    if(!bOk[i]) {
                        // 计算差值：顶部平均值 - 上升沿平均值
                        double val = average[1][i] - average[0][i];
//                        idx = i * CabteRegister.MHO68V2_RATIO_DANG_CNT + dwIdx ;
                        
                        // 检查是否找到更小的差值
                        if (Math.abs(val) < minVal[i]) {
                            minVal[i] = Math.abs(val);  // 更新最小差值
                            dcpVal[i] = cabteRegister.getChCapacitanceHigh(i,dwIdx);  // 记录当前电容值
                            minNum[i] = 0;  // 清零连续未改善计数
                        } else {
                            minNum[i]++;  // 连续未改善计数加1
                        }
                        
                        // 计算新的步进值（二分搜索）
                        refVal[i] /= 2;
                        if (refVal[i] == 0) {
                            refVal[i] = 1;  // 最小步进为1
                        }
                        
                        // 检查是否收敛（连续16次未改善）
                        if (minNum[i] > 16) {
//                            chCapHigh[idx] = dcpVal[i];
                            // 设置最优电容值
                            cabteRegister.setChCapacitanceHigh(i,dwIdx,dcpVal[i]);
                            bOk[i] = true;  // 标记该通道完成
                        } else if (Math.abs(val) > TVAL) {
                            // 差值超过阈值，需要调整电容
                            int xvv = cabteRegister.getChCapacitanceHigh(i,dwIdx);
                            if (val > TVAL) {
                                // 过补偿：减小电容
                                xvv += refVal[i];
                                if (xvv > CAP_MAX_VAL) {
                                    xvv = 0;  // 溢出处理
                                }
                            } else if (val < -TVAL) {
                                // 欠补偿：增大电容
                                xvv -= refVal[i];
                                if (xvv < 0) {
                                    xvv = CAP_MAX_VAL;  // 溢出处理
                                }
                            }
                            // 设置新的电容值
                            cabteRegister.setChCapacitanceHigh(i,dwIdx,xvv);
                        }else{
                            bOk[i] = true;  // 差值在阈值内，校准完成
                        }
                        Log.d(TAG, "chCapHigh[" + i + "," + dwIdx + "] = " + cabteRegister.getChCapacitanceHigh(i,dwIdx));
                    }
                    average[0][i] = 0;  // 清零上升沿平均值
                    average[1][i] = 0;  // 清零顶部平均值
                }
            }
            
            // 检查是否所有目标通道都完成
            boolean bx = false;
            if(ch == -1){
                // 校准所有通道：检查所有通道是否完成
                int i =0;
                for(;i<channelNums;i++){
                    if(!bOk[i]){
                       break;  // 发现未完成的通道
                    }
                }
                bx = (i == channelNums);  // 所有通道都完成
            }else{
                bx = bOk[ch];  // 检查目标通道是否完成
            }
            
            // 如果所有目标通道都完成
            if(bx){
                step++;  // 进入下一步
            }
            
            updateSync();  // 更新同步
            Sample.getInstance().setSampleType(Sample.SAMPLE_TYPE_NORMAL);  // 设置采样模式
        }

        // 检查是否进入步骤3（校准完成）
        if (step != 3) {
            return false;  // 继续校准
        }

        // 输出校准结果
        for (int i = 0; i < channelNums; i++){
            resultString.add("chCapHigh[" + i + "] = " + cabteRegister.getChCapacitanceHigh(i,dwIdx));
        }
        resultString.add("chCapCalibrate Calibrate end >>>>>>>>>>>");

        // 如果校准成功
        if(errcode == 0){
            param[0] = ch;      // 保存通道索引
            param[1] = dwIdx;   // 保存档位索引
            
            // 设置校准状态
            if(ch < 0){
                // 校准所有通道
                for(int i=0;i<channelNums;i++){
                    cabteRegister.setCalibrationState(HW_MHO68V2.CHCAP_CALIBRATE_CH1 + i * CabteRegister.getRatioDangCnt() + dwIdx,true);
                }
            }else{
                // 校准单个通道
                cabteRegister.setCalibrationState(HW_MHO68V2.CHCAP_CALIBRATE_CH1 + ch * CabteRegister.getRatioDangCnt() + dwIdx,true);
            }
        }
        return true;  // 校准完成
    }
}
