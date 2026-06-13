package com.micsig.tbook.scope.Calibrate.MHO68v2;

import android.util.Log;

import com.micsig.tbook.scope.Calibrate.CabteRegister;
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
import com.micsig.tbook.scope.math.MathNative;
import com.micsig.tbook.scope.vertical.VerticalAxis;

import java.util.Arrays;

/**
 * MHO68 V2示波器通道系数校准扩展实现类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate.MHO68v2（MHO68 V2示波器校准模块）</li>
 *   <li>架构层级：业务逻辑层 - 具体校准实现</li>
 *   <li>设计模式：继承Calibrate抽象基类，实现通道系数校准流程</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现MHO68 V2示波器的通道偏移系数校准</li>
 *   <li>通过迭代算法找到最优通道系数</li>
 *   <li>支持1MΩ和50Ω两种阻抗模式</li>
 *   <li>支持正向和负向偏移校准</li>
 * </ul>
 * 
 * <p><b>通道系数校准原理：</b>
 * <pre>
 * 校准目的：
 *   示波器通道存在直流偏移，需要校准通道系数使波形零点与屏幕中心对齐。
 *   通道系数决定了ADC值到屏幕像素的转换比例。
 * 
 * 校准方法：
 *   1. 输入已知幅度的直流信号
 *   2. 测量波形在屏幕上的位置
 *   3. 计算实际位置与目标位置的偏差
 *   4. 调整通道系数使偏差最小化
 *   5. 迭代直到偏差小于阈值
 * 
 * 校准流程：
 *   ┌─────────────────────────────────────────────────────────────┐
 *   │  1. calibratePrepare() - 准备阶段                           │
 *   │     ├── 设置触发模式为自动触发                              │
 *   │     ├── 设置垂直档位和阻抗类型                              │
 *   │     ├── 设置波形位置（偏移）                                │
 *   │     └── 初始化通道系数为默认值                              │
 *   │                                                             │
 *   │  2. onCalibrate() - 执行阶段                                │
 *   │     ├── 采集波形数据                                        │
 *   │     ├── 计算波形平均值                                      │
 *   │     ├── 计算位置偏差                                        │
 *   │     ├── 调整通道系数                                        │
 *   │     └── 迭代直到偏差小于0.5像素                             │
 *   │                                                             │
 *   │  3. 保存校准结果                                            │
 *   └─────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>系数调整算法：</b>
 * <pre>
 * 偏差计算：
 *   mean = offset - average[i]  // 目标位置 - 实际位置
 * 
 * 系数调整：
 *   fx = coef_default * mean / offset  // 调整量
 *   
 *   根据偏差大小调整调整速度：
 *   - 偏差 < 50像素：调整量 /= 4（精细调节）
 *   - 偏差 < 100像素：调整量 /= 2（中等调节）
 *   - 偏差 >= 100像素：全速调节
 * 
 * 新系数：
 *   xval = tpCoef - fx
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
 *   <li>出厂校准时校准所有通道的偏移系数</li>
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
public class MHO68v2_ChCofitCalibrateEx extends Calibrate {
    
    /**
     * 构造函数
     * 
     * <p>调用父类构造函数，初始化校准类型，并设置初始延时为0
     * 
     * @param calibrateType 校准类型标识
     */
    public MHO68v2_ChCofitCalibrateEx(int calibrateType) {
        super(calibrateType);  // 调用父类Calibrate的构造函数
        delaySet(0);            // 设置初始延时为0毫秒
    }
    
    /**
     * 设置错误码
     * 
     * <p>错误码定义：
     * <ul>
     *   <li>0: 校准成功</li>
     *   <li>非0: 校准失败</li>
     * </ul>
     * 
     * @param errcode 错误码
     */
    @Override
    public void setErrcode(int errcode) {
        this.errcode = errcode;  // 保存错误码到成员变量
    }
    
//    private float [][][]cofit=new float[ChannelFactory.CH_CNT][][];
    
    /**
     * 最佳值数组
     * 
     * <p>记录每个通道在迭代过程中找到的最小偏差值
     * 初始值为1000（一个较大的数）
     */
    private float []best_value=new float[ChannelFactory.CH_CNT];
    
    /**
     * 备份系数数组
     * 
     * <p>记录每个通道找到最佳值时的系数值
     * 用于在迭代过程中保存最优解
     */
    private float []bakCoef=new float[ChannelFactory.CH_CNT];


    /**
     * 偏移量
     * 
     * <p>目标波形位置（像素），根据信号幅度和垂直档位计算
     * volatile保证多线程可见性
     */
    private volatile int offset;

    /** 错误码，用于标识校准结果 */
    private int errcode;
    
    /** 日志标签：通道系数校准 */
    private final String TAG = "ChCofit";
    
    /** 完整日志标签：包含优先级前缀 */
    private final String TAG1=TAG_PRI+":"+TAG;

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
     *   <li>将所有通道的最佳值初始化为1000</li>
     * </ul>
     */
    private void rstCalculate(){

        Arrays.fill(best_value, 1000);  // 将所有通道的最佳值初始化为1000
    }

    /**
     * 初始化校准寄存器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>在开始校准前初始化相关寄存器</li>
     *   <li>当前为空实现，可以在现有零点基础上进行校准</li>
     * </ul>
     */
    @Override
    public void iniCalibrateReg(){
        // 可以不用复位零点，在目前的零点基础上进行校准
    }
    
    /**
     * 设置校准参数
     * 
     * <p><b>参数格式：</b>
     * <pre>
     * vol为double[]数组：
     *   vol[0]: 通道索引（0~7表示单通道，-1表示所有通道）
     *   vol[1]: 垂直档位值（如0.001表示1mV/div）
     *   vol[2]: 信号源幅度（如2.4表示2.4V）
     *   vol[3]: 阻抗类型（可选，0=1MΩ，1=50Ω）
     * </pre>
     * 
     * @param vol 校准参数对象
     */
    @Override
    public void setParam(Object vol) {
        ch = ChannelFactory.CH1;  // 默认校准通道1
        vScaleVal = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1mV);  // 默认档位1mV/div

        srcAmp = 2.4;  // 默认信号幅度2.4V
        idx = 0;       // 默认索引0（正向偏移）
        
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
                    srcAmp = param[2];     // 保存信号源幅度
                    // 检查是否有阻抗类型参数
                    if(param.length > 3) {
                        resistanceType = (int)(param[3] + 0.1);  // 保存阻抗类型
                    }
                }
            }
        }
        
        // 计算偏移量（像素）
        // 公式：信号幅度 × 每格像素数 ÷ 档位值
        double v = Math.round(srcAmp * ScopeBase.getVerticalPerGridPixels() / vScaleVal);
        // 转换为整数，保留符号
        offset = (int)( v > 0 ? v + 0.01 : v - 0.01) ;
        
        Log.d(TAG,"offset:" + offset + ",srcAmp:" + srcAmp + "," + vScaleVal + ",resistanceType:" + resistanceType);

        // 根据偏移量正负确定索引
        // idx=1: 正向偏移（信号在屏幕上方）
        // idx=0: 负向偏移（信号在屏幕下方）
        idx = offset > 0 ? 1 : 0;
        
        // 设置初始阻抗类型
        setInitResistanceType(resistanceType);
    }
    
    /** 参数数组，用于返回校准结果 */
    int []param = new int[2];
    
    /**
     * 获取校准参数
     * 
     * <p><b>返回格式：</b>
     * <pre>
     * int[0]: 校准的通道索引
     * int[1]: 校准项索引（考虑阻抗类型和偏移方向）
     * </pre>
     * 
     * @return 参数数组
     */
    @Override
    public Object getParam() {
        // 计算校准项索引
        // 50Ω阻抗：vIdx + 档位数
        // 1MΩ阻抗：vIdx
        int n = resistanceType == Channel.RESISTANCE_50 ? vIdx + CabteRegister.getRatioDangCnt() : vIdx;
        param[0] = ch;           // 设置通道索引
        param[1] = n * 2 + idx;  // 设置校准项索引（×2是因为有正向和负向两个偏移）
        return param;            // 返回参数数组
    }

    /** 当前校准通道索引 */
    int ch = ChannelFactory.CH1;
    
    /** 信号源幅度（V） */
    double srcAmp = 2.4;
    
    /** 校准使用的垂直档位值 */
    double vScaleVal = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1mV);
    
    /** 偏移方向索引（0=负向，1=正向），volatile保证多线程可见性 */
    volatile int idx = 0;
    
    /** 档位索引，volatile保证多线程可见性 */
    volatile int vIdx = 0;
    
    /** 默认系数值，volatile保证多线程可见性 */
    volatile float vDefaultVal = 0;
    
    /** 阻抗类型（1MΩ或50Ω），volatile保证多线程可见性 */
    volatile int resistanceType = Channel.RESISTANCE_1M;
    
    /**
     * 校准准备阶段
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置触发模式为自动触发</li>
     *   <li>配置通道参数（档位、位置、阻抗）</li>
     *   <li>初始化通道系数为默认值</li>
     *   <li>等待信号稳定</li>
     * </ul>
     */
    @Override
    public void calibratePrepare() {

        int tmpIdx = Math.max(ch,0);  // 获取有效的通道索引（-1转为0）
        
        // 根据阻抗类型和垂直档位获取档位索引
        vIdx = CabteRegister.getRatioIdx(resistanceType,vScaleVal);

        // 设置触发模式为自动触发（不依赖触发信号）
        TriggerFactory.getInstance().getTriggerCommon().setTriggerMode(TriggerCommon.TM_AUTO);
        
        // 遍历所有通道进行初始化
        for(int i=0; i<channelNums; i++) {
            // 检查是否为校准目标通道
            if(ch < 0 || i == ch) {
                channel[i].setProbeRate(1);  // 设置探头比例为1:1
                channel[i].setVScaleVal(vScaleVal);  // 设置垂直档位

                channel[i].setPos(-offset);  // 设置波形位置（负偏移）
                channel[i].setResistanceType(resistanceType);  // 设置阻抗类型
                
                // 获取边沿触发实例
                TriggerEdge triggerEdge = (TriggerEdge) TriggerFactory.getTriggerObj(Trigger.TRIG_TYPE_EDGE);
                // 设置触发电平位置（故意设置在屏幕1/3处，避免触发）
                triggerEdge.getTriggerLevel(i).setPos(ScopeBase.getHeight()/3);
            }
        }
        
        ms_sleep(100);  // 延时100ms等待配置生效
        
        // 如果是有效的动态通道，激活该通道
        if( ChannelFactory.isDynamicCh(tmpIdx) ){
            ChannelFactory.chActivate(tmpIdx);
        }

        // 获取默认通道系数
        vDefaultVal = (float) cabteRegister.vol_ChannelCoef_default(resistanceType,vIdx);
        
        // 遍历所有通道设置初始系数
        for(int i=0; i<channelNums; i++) {
//            cofit[i] = cabteRegister.vol_ChannelCoef(i);
            // 检查是否为校准目标通道
            if(ch < 0 || i == ch) {
                //cofit[i][vIdx][idx] = vDefaultVal;
                // 设置通道系数为默认值
                cabteRegister.setChannelCoef(i,vIdx,idx,vDefaultVal);
            }
        }

        // 等待信号稳定，检查3次参数
        for(int i=0;i<3;i++){
            ms_sleep(1100);  // 延时1.1秒
            checkPram();     // 检查参数是否变化
        }

        rstCalculate();  // 重置计算变量
        fpgaSync();      // 同步FPGA
        delaySet(2);     // 延时2ms
        errcode = 0;     // 清零错误码
        
        // 添加结果字符串
        resultString.add("<<<<<<<<<< chCofitCalibrate start ......");
        Log.i(TAG1,"<<<<<<<<<< chCofitCalibrate start ......");
    }

    /**
     * 检查参数是否变化
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查触发模式是否为自动触发</li>
     *   <li>检查所有通道的阻抗类型是否正确</li>
     *   <li>如果参数变化，重新设置并更新同步</li>
     * </ul>
     * 
     * @return true表示参数有变化，false表示参数稳定
     */
    private boolean checkPram(){

        boolean bChange = false;  // 参数变化标志
        
        // 获取触发通用设置实例
        TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon();
        
        // 检查触发模式是否为自动触发
        if(triggerCommon.getTriggerMode() != TriggerCommon.TM_AUTO) {
            triggerCommon.setTriggerMode(TriggerCommon.TM_AUTO);  // 重新设置为自动触发
            bChange = true;  // 标记参数变化
        }

        // 遍历所有通道检查阻抗类型
        for(int i=0;i<channelNums;i++){
            // 检查通道阻抗类型是否正确
            if(channel[i].getResistanceType() != resistanceType){
                bChange = true;  // 标记参数变化
                break;           // 发现不一致，退出循环
            }
        }
        
        // 如果参数有变化
        if(bChange){
            updateSync();  // 更新同步
            
            // 遍历所有通道重新设置阻抗类型
            for(int i=0;i<channelNums;i++){
                if(channel[i].getResistanceType() != resistanceType){
                    channel[i].setResistanceType(resistanceType);  // 重新设置阻抗类型
                }
            }
        }
        return bChange;  // 返回参数变化状态
    }
    
    /**
     * 执行校准
     * 
     * <p><b>校准算法：</b>
     * <pre>
     * 1. 采集波形数据
     * 2. 计算波形平均值
     * 3. 计算偏差：mean = offset - average[i]
     * 4. 记录最小偏差和对应的系数
     * 5. 计算系数调整量：
     *    fx = coef_default * mean / offset
     *    根据偏差大小调整调整速度
     * 6. 更新通道系数
     * 7. 迭代直到所有通道偏差小于0.5像素
     * </pre>
     * 
     * @return true表示校准完成，false表示校准进行中
     */
    @Override
    public boolean onCalibrate() {
        // 等待硬件操作完成
        if(!isFinishedAction())
            return false;  // 硬件操作未完成，继续等待
        
        // 检查参数是否变化
        if(checkPram()){
            return false;  // 参数变化，重新开始
        }
        
        delaySet(0);  // 设置延时为0
        
        float[] average ={0,0,0,0,0,0,0,0};  // 波形平均值数组
        long sum;       // 波形求和值
        WaveData waveData;  // 波形数据
        int N;          // 波形长度
        
        HwConfig hwConfig = HwConfig.getInstance();  // 获取硬件配置实例
        
        // 遍历所有通道获取波形平均值
        for(int i=0; i<channelNums; i++){
            waveData = (WaveData) getWave(i);  // 获取波形数据
            // 检查波形数据有效性
            if(waveData == null || (N = waveData.getWaveLength()) < 10)
                return false;  // 波形数据无效，继续等待
            
            sum = MathNative.calcSum(waveData.getByteBuffer());  // 计算波形求和
            average[i] = (float)( hwConfig.getWavFactor() * sum / N);  // 计算平均值（应用波形缩放因子）
        }
        
        updateSync();  // 更新同步
        
        // 遍历所有通道进行系数调整
        for(int i=0;i<channelNums;i++){
            // 检查是否为校准目标通道
            if(ch < 0 || ch == i){
                // 计算偏差：目标位置 - 实际位置
                float mean = offset-average[i];
                
                // 检查是否找到更小的偏差
                if(Math.abs(mean) < Math.abs(best_value[i])){
                    best_value[i] = mean;  // 更新最佳值
                    bakCoef[i] = cabteRegister.getChannelCoef(i,vIdx,idx);  // 备份当前系数
                    Log.i(TAG1,"ch"+(i+1)+"找到更佳值 :" + best_value[i]);  // 输出日志
                }


                // 获取当前通道系数
                float tpCoef = cabteRegister.getChannelCoef(i,vIdx,idx);
//                float coef_default = vDefaultVal;
                float coef_default = bakCoef[i];  // 使用备份的最优系数作为基准
                
                // 计算系数调整量
                float fx = coef_default * mean / offset;


                float wuc=Math.abs(mean);  // 计算误差绝对值

                // 根据误差大小调整调整速度
                if(wuc < 50)       // 误差小于50像素（约2格）
                    fx /= 4;       // 精细调节：调整量除以4
                else if(wuc < 100) // 误差小于100像素（约4格）
                    fx /= 2;       // 中等调节：调整量除以2

                // 计算新系数
                float xval = tpCoef - fx;
//                cofit[i][vIdx][idx] -= fx;


                // 检查系数是否为负值（异常情况）
                if(xval < 0){
                    xval = coef_default * 1.2f;  // 重置为默认值的1.2倍
                }
//                if(cofit[i][vIdx][idx]  < 0){
//                    cofit[i][vIdx][idx] = coef_default * 1.2f;
//                }
                
                // 设置新的通道系数
                cabteRegister.setChannelCoef(i,vIdx,idx,xval);

                // 输出详细日志
                Log.i(TAG1, (i+1)+":\t"+
                        "像素误差:"+wuc+
                        " ,旧系数:"+tpCoef+
                        " ,新系数:"+ xval +
                        " ,默认系数:"+coef_default +
                        ",mean:" + mean  +
                        ",fx:" + fx +
                        ",vIdx:" + vIdx
                );
                
                channel[i].setPos(-offset);  // 重新设置波形位置
            }
        }

        boolean bDone = true;  // 校准完成标志

        // 检查所有目标通道是否都满足条件
        for(int i=0;i<channelNums;i++){
            // 检查是否为校准目标通道
            if(ch < 0 || ch == i){
                // 检查偏差是否小于0.5像素
                if(Math.abs(best_value[i]) > 0.5){
                    bDone = false;  // 偏差过大，校准未完成
                    break;          // 退出循环
                }
            }
        }
        
        // 如果校准完成且无错误
        if(bDone
                && errcode == 0){
            // 计算校准项索引
            int n = resistanceType == Channel.RESISTANCE_50 ? vIdx + CabteRegister.getRatioDangCnt() : vIdx;
            param[0] = ch;           // 保存通道索引
            param[1] = n * 2 + idx;  // 保存校准项索引
            
            // 设置校准状态
            if(ch < 0){
                // 校准所有通道
                for(int i=0;i<channelNums;i++){
                    cabteRegister.setCalibrationState(HW_MHO68V2.CHOFFSET_CALIBRATE_CH1 + i * 12 + param[1] ,true);
                }
            }else{
                // 校准单个通道
                cabteRegister.setCalibrationState(HW_MHO68V2.CHOFFSET_CALIBRATE_CH1 + ch * 12 + param[1] ,true);
            }
        }
        return bDone;  // 返回校准是否完成
    }

}
