package com.micsig.tbook.scope.Calibrate.MHO38v1;  // 包声明：MHO38 V1示波器校准模块

import android.util.Log;  // 导入：Android日志工具，用于输出调试信息

import com.micsig.tbook.scope.Calibrate.CabteRegister;  // 导入：校准寄存器管理类，用于管理校准数据
import com.micsig.tbook.scope.Calibrate.Calibrate;  // 导入：校准抽象基类，定义校准流程接口
import com.micsig.tbook.scope.Calibrate.HwConfig;  // 导入：硬件配置类，用于获取硬件参数
import com.micsig.tbook.scope.Data.WaveData;  // 导入：波形数据类，用于存储波形数据
import com.micsig.tbook.scope.ScopeBase;  // 导入：示波器基类，包含基础配置常量
import com.micsig.tbook.scope.Trigger.Trigger;  // 导入：触发器基类，定义触发类型
import com.micsig.tbook.scope.Trigger.TriggerCommon;  // 导入：触发器公共配置类
import com.micsig.tbook.scope.Trigger.TriggerEdge;  // 导入：边沿触发器类，用于边沿触发控制
import com.micsig.tbook.scope.Trigger.TriggerFactory;  // 导入：触发器工厂类，用于创建触发器
import com.micsig.tbook.scope.channel.Channel;  // 导入：通道类，定义通道属性和操作
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入：通道工厂类，用于通道管理
import com.micsig.tbook.scope.math.MathNative;  // 导入：数学运算本地方法类，用于波形计算
import com.micsig.tbook.scope.vertical.VerticalAxis;  // 导入：垂直轴类，用于垂直档位转换

/**
 * MHO38 V1示波器通道偏移量系数校准扩展实现类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate.MHO38v1（MHO38 V1示波器校准模块）</li>
 *   <li>架构层级：校准业务层 - 偏移量系数校准具体实现</li>
 *   <li>设计模式：继承Calibrate抽象基类，实现偏移量系数校准流程</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现MHO38 V1示波器的通道偏移量系数校准</li>
 *   <li>通过调整通道系数（coefChannel）校准偏移量</li>
 *   <li>使波形平均值与目标偏移值一致</li>
 *   <li>支持单通道和全通道校准</li>
 * </ul>
 * 
 * <p><b>校准原理：</b>
 * <pre>
 * 偏移量校准目标：
 *   使波形平均值等于目标偏移值，消除通道偏移误差
 * 
 * 校准方法：
 *   1. 设置通道垂直位置为目标偏移值
 *   2. 采集波形数据
 *   3. 计算波形平均值
 *   4. 计算误差（mean = offset - average）
 *   5. 根据误差调整通道系数：
 *      - mean > 0：减小系数
 *      - mean < 0：增大系数
 *   6. 迭代直到误差小于0.5像素
 * 
 * 系数调整公式：
 *   fx = mean / offset * coef_default
 *   新系数 = 旧系数 - fx
 * </pre>
 * 
 * <p><b>校准流程：</b>
 * <pre>
 * 1. calibratePrepare()：初始化校准参数
 *    ├── 计算档位索引
 *    ├── 计算默认系数值
 *    ├── 设置触发模式为自动触发
 *    ├── 设置通道参数（探头、档位、位置）
 *    └── 初始化通道系数为默认值
 * 
 * 2. onCalibrate()：执行校准
 *    ├── 采集波形数据
 *    ├── 计算波形平均值
 *    ├── 计算误差并调整系数
 *    ├── 记录最佳系数值
 *    └── 判断是否收敛
 * 
 * 3. 收敛条件：
 *    - |best_value| < 0.5像素
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：Calibrate（校准抽象基类）</li>
 *   <li>依赖：CabteRegister（校准寄存器管理）</li>
 *   <li>依赖：HW_MHO38V1（硬件操作类，校准状态定义）</li>
 *   <li>依赖：TriggerFactory（触发器工厂）</li>
 *   <li>依赖：ChannelFactory（通道工厂）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>示波器出厂校准时校准偏移量系数</li>
 *   <li>周期性校准维护时校准偏移量系数</li>
 *   <li>用户手动触发偏移量校准</li>
 * </ul>
 * 
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>校准前需要连接标准信号源</li>
 *   <li>校准过程中不要切换档位或通道</li>
 *   <li>校准结果存储在Flash中，断电不丢失</li>
 *   <li>每个通道每个档位有2个系数（正负偏移）</li>
 * </ul>
 * 
 * @author zhuzh  // 作者：zhuzh
 * @version 1.0  // 版本号：1.0
 * @see Calibrate 校准抽象基类  // 参见：Calibrate类
 * @see HW_MHO38V1 MHO38 V1硬件操作类  // 参见：HW_MHO38V1类
 * @see CabteRegister 校准寄存器管理  // 参见：CabteRegister类
 */
public class MHO38v1_ChCofitCalibrateEx extends Calibrate {  // 继承Calibrate抽象基类，实现偏移量系数校准
    
    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，传递校准类型</li>
     *   <li>设置初始延时为0</li>
     * </ul>
     * 
     * @param calibrateType 校准类型（参见HW_MHO38V1中的校准状态常量）
     */
    public MHO38v1_ChCofitCalibrateEx(int calibrateType) {  // 构造函数，接收校准类型参数
        super(calibrateType);  // 调用父类Calibrate的构造函数，传递校准类型
        delaySet(0);  // 设置初始延时为0
    }  // 构造函数结束
    
    /**
     * 设置错误码
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的setErrcode方法</li>
     *   <li>设置校准过程中的错误码</li>
     * </ul>
     * 
     * @param errcode 错误码
     */
    @Override  // 覆盖父类方法
    public void setErrcode(int errcode) {  // 设置错误码方法
        this.errcode = errcode;  // 保存错误码到成员变量
    }  // 方法结束
    
    /**
     * 最佳误差值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道历史最小的误差值</li>
     *   <li>用于记录最佳校准结果</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <pre>
     * 初始值：1000（大值）
     * 校准过程中逐渐减小
     * 收敛值：< 0.5像素
     * </pre>
     */
//    private float [][][]cofit=new float[4][][];  // 已注释：系数三维数组
    private float []best_value=new float[ChannelFactory.CH_CNT];  // 最佳误差值数组，每个通道一个值
    
    /**
     * 备份系数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道最佳校准时的系数值</li>
     *   <li>用于校准完成后恢复最佳系数</li>
     * </ul>
     */
    private float []bakCoef=new float[ChannelFactory.CH_CNT];  // 备份系数数组，每个通道一个值
    
    /**
     * 校准类型
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>标识当前校准的类型或阶段</li>
     * </ul>
     */
    private int type=0;  // 校准类型，初始为0
    
    /**
     * 尝试次数计数器
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录校准迭代的次数</li>
     *   <li>用于防止无限循环</li>
     * </ul>
     */
    private int tryNum;  // 尝试次数计数器
    
    /**
     * 档位ID成员变量
     * 
     * <p><b>业务含义：</b>
     *   <li>当前校准的档位ID</li>
     * </ul>
     */
//    private int dangwei;  // 已注释：档位ID

    /**
     * 目标偏移值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>波形平均值的目标偏移值（像素）</li>
     *   <li>根据信号幅度和档位计算</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * offset = srcAmp * 每格像素数 / 档位电压值
     * </pre>
     */
    private volatile int offset;  // 目标偏移值，volatile保证多线程可见性
    
    /**
     * 校准结果数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录计算的结果</li>
     *   <li>分两个步骤，一次4个通道</li>
     * </ul>
     * 
     * <p><b>数组维度：</b>
     * <pre>
     * 第一维：步骤索引（0或1）
     * 第二维：通道索引（0-7）
     * </pre>
     */
    private float result[][]=new float[2][ChannelFactory.CH_CNT];  // 校准结果数组，记录计算的结果，分两个步骤，一次4个通道
    
    /**
     * 错误码成员变量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储校准过程中的错误码</li>
     *   <li>0表示无错误，非0表示校准失败</li>
     * </ul>
     */
    private int errcode;  // 错误码成员变量
    
    /**
     * 日志标签
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于日志输出时标识偏移量系数校准模块</li>
     *   <li>简化日志输出，便于日志过滤</li>
     * </ul>
     */
    private final String TAG = "ChCofit";  // 日志标签，固定为"ChCofit"
    
    /**
     * 完整日志标签
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>组合父类标签和当前标签</li>
     *   <li>格式："父类标签:ChCofit"</li>
     * </ul>
     */
    private final String TAG1=TAG_PRI+":"+TAG;  // 完整日志标签，格式为"父类标签:ChCofit"

    /**
     * 获取日志标签
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getTAG方法</li>
     *   <li>返回完整的日志标签</li>
     * </ul>
     * 
     * @return 完整日志标签
     */
    @Override  // 覆盖父类方法
    public String getTAG() {  // 获取日志标签方法
        return TAG1;  // 返回完整日志标签
    }  // 方法结束

    /**
     * 获取错误码
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getErrcode方法</li>
     *   <li>返回校准过程中的错误码</li>
     * </ul>
     * 
     * @return 错误码，0表示成功，非0表示失败
     */
    @Override  // 覆盖父类方法
    public int getErrcode() {  // 获取错误码方法
        //=1：校验错误
        return errcode;  // 返回错误码
    }  // 方法结束

    /**
     * 重置计算变量
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>重置所有校准相关的计算变量</li>
     *   <li>在开始新档位校准或参数变化时调用</li>
     * </ul>
     * 
     * <p><b>重置内容：</b>
     * <pre>
     * 1. 清零尝试次数
     * 2. 重置最佳误差值为大值（1000）
     * </pre>
     */
    //校准下一档初始化
    private void rstCalculate(){  // 重置计算变量方法
        tryNum = 0;  // 清零尝试次数
        for(int i=0; i<channelNums; i++) {  // 循环：遍历所有通道
            best_value[i] = 1000;  // 重置最佳误差值为大值
        }  // 循环结束
    }  // 方法结束

    /**
     * 初始化校准寄存器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的iniCalibrateReg方法</li>
     *   <li>当前为空实现</li>
     * </ul>
     * 
     * <p><b>说明：</b>
     * <pre>
     * 可以不用复位零点，在目前的零点基础上进行校准
     * </pre>
     */
    @Override  // 覆盖父类方法
    public void iniCalibrateReg(){  // 初始化校准寄存器方法
        //可以不用复位零点，在目前的零点基础上进行校准
    }  // 方法结束（空实现）
    
    /**
     * 设置校准参数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的setParam方法</li>
     *   <li>设置校准的通道、档位和信号幅度参数</li>
     * </ul>
     * 
     * <p><b>参数格式：</b>
     * <pre>
     * vol为double[]数组：
     *   第1个值：通道索引（-1表示所有通道，0-7表示指定通道）
     *   第2个值：档位电压值（V/div）
     *   第3个值：信号源幅度（V）
     * </pre>
     * 
     * @param vol 校准参数对象，类型为double[]
     */
    /**
     * 设置校准参数
     * @param vol
     * vol为int[];
     * 第1个值为通道：0~3,-1；（-1表示所有通道）
     * 第2个值为档位；
     */
    @Override  // 覆盖父类方法
    public void setParam(Object vol) {  // 设置校准参数方法
        ch = ChannelFactory.CH1;  // 默认校准通道1
        vScaleVal = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1mV);  // 默认档位为1mV/div

        srcAmp = 2.4;  // 默认信号源幅度为2.4V
        idx = 0;  // 初始化系数索引为0
        if(vol instanceof double[]) {  // 判断：参数是否为double数组
            double[] param=(double[])vol;  // 转换参数类型
            if(param.length >= 2){  // 判断：参数长度是否足够
                int ix =(int)(param[0] + 0.1);  // 获取通道索引（加0.1避免浮点误差）
                if(param[0] < 0){  // 判断：通道索引是否为负
                    ix = -1;  // 设置为-1，表示所有通道
                }  // 判断结束
                if(ix == -1 || ChannelFactory.isDynamicCh(ix)) {  // 判断：通道索引是否有效
                    ch = ix;  // 设置校准通道
                    vScaleVal = param[1];  // 设置档位电压值
                    srcAmp = param[2];  // 设置信号源幅度
                }  // 判断结束
            }  // 判断结束
        }  // 判断结束
        offset = (int)(srcAmp * ScopeBase.getVerticalPerGridPixels() / vScaleVal + 0.01);  // 计算目标偏移值（像素）
        Log.d(TAG,"offset:" + offset + ",srcAmp:" + srcAmp + "," + vScaleVal);  // 输出调试日志

        idx = offset > 0 ? 1 : 0;  // 根据偏移值正负设置系数索引（正偏移用索引1，负偏移用索引0）
    }  // 方法结束
    
    /**
     * 参数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于返回校准参数</li>
     *   <li>存储通道索引和系数索引</li>
     * </ul>
     */
    int []param = new int[2];  // 参数数组，长度为2
    
    /**
     * 获取校准参数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getParam方法</li>
     *   <li>返回当前校准的参数</li>
     * </ul>
     * 
     * @return 参数数组，[通道索引, 系数索引]
     */
    @Override  // 覆盖父类方法
    public Object getParam() {  // 获取校准参数方法
        param[0] = ch;  // 设置通道索引
        param[1] = vIdx * 2 +  idx;  // 计算系数索引（档位索引*2 + 正负索引）
        return param;  // 返回参数数组
    }  // 方法结束
    
    /**
     * 当前校准通道索引
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>指定当前校准的通道</li>
     *   <li>-1表示校准所有通道</li>
     *   <li>0-7表示校准指定通道</li>
     * </ul>
     */
    int ch = ChannelFactory.CH1;  // 当前校准通道索引，默认为CH1
    
    /**
     * 信号源幅度
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>校准信号源的幅度值</li>
     *   <li>单位：V</li>
     *   <li>默认为2.4V</li>
     * </ul>
     */
    double srcAmp = 2.4;  // 信号源幅度，默认为2.4V
    
    /**
     * 校准档位电压值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>偏移量系数校准使用的档位电压值</li>
     *   <li>默认为1mV/div</li>
     * </ul>
     */
    double vScaleVal = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1mV);  // 校准档位电压值，默认为1mV/div
    
    /**
     * 系数索引
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>标识当前校准的系数索引</li>
     *   <li>0表示负偏移系数</li>
     *   <li>1表示正偏移系数</li>
     * </ul>
     */
    volatile int idx = 0;  // 系数索引，volatile保证多线程可见性，初始为0
    
    /**
     * 档位索引
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>当前校准档位的索引值</li>
     *   <li>用于访问校准数据数组</li>
     * </ul>
     */
    volatile int vIdx = 0;  // 档位索引，volatile保证多线程可见性，初始为0
    
    /**
     * 默认系数值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>当前档位的默认通道系数值</li>
     *   <li>用于系数调整的参考值</li>
     * </ul>
     */
    volatile float vDefaultVal = 0;  // 默认系数值，volatile保证多线程可见性，初始为0
    
    /**
     * 校准准备
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的calibratePrepare方法</li>
     *   <li>初始化校准环境和参数</li>
     *   <li>设置触发、通道等参数</li>
     * </ul>
     * 
     * <p><b>初始化流程：</b>
     * <pre>
     * 1. 获取档位索引
     * 2. 计算默认系数值
     * 3. 设置触发模式为自动触发
     * 4. 设置通道参数（探头、档位、位置）
     * 5. 初始化通道系数为默认值
     * 6. 设置触发电平（特意不让触发）
     * 7. 激活校准通道
     * 8. 重置计算变量
     * </pre>
     */
    @Override  // 覆盖父类方法
    public void calibratePrepare() {  // 校准准备方法

        int chidx = Math.max(ch, 0);  // 获取有效的通道索引（-1转为0）
        vIdx = CabteRegister.getRatioIdx(Channel.RESISTANCE_1M,vScaleVal);  // 获取档位索引
        int [] result = {0,0,0,0,0,0,0,0};  // 初始化结果数组
        cabteRegister.calc_pga_fs_gain(chidx,vScaleVal,result);  // 计算PGA和满量程值
        vDefaultVal = (float) cabteRegister.vol_ChannelCoef_defaultEx(chidx,CabteRegister.getRatioIdx2Dang(Channel.RESISTANCE_1M,vIdx),result[0]);  // 计算默认系数值
//        for(int i=0; i<channelNums; i++) {  // 已注释：遍历所有通道
//            cofit[i] = cabteRegister.vol_ChannelCoef(i);  // 已注释：获取通道系数
//        }  // 已注释：循环结束
        ms_sleep(500);  // 延时500ms，等待硬件稳定
        TriggerFactory.getInstance().getTriggerCommon().setTriggerMode(TriggerCommon.TM_AUTO);  // 设置触发模式为自动触发
        for(int i=0; i<channelNums; i++) {  // 循环：遍历所有通道
            if(ch < 0 || i == ch) {  // 判断：是否为需要校准的通道
                channel[i].setProbeRate(1);  // 设置探头衰减比为1X
                channel[i].setVScaleVal(vScaleVal);  // 设置档位电压值
//                cofit[i][vIdx][idx] = vDefaultVal;  // 已注释：设置系数为默认值
                cabteRegister.setChannelCoef(i,vIdx,idx,vDefaultVal);  // 设置通道系数为默认值
                channel[i].setPos(-offset);  // 设置垂直位置为负偏移值
                TriggerEdge triggerEdge = (TriggerEdge) TriggerFactory.getTriggerObj(Trigger.TRIG_TYPE_EDGE);  // 获取边沿触发器
                triggerEdge.getTriggerLevel(i).setPos(120);  // 设置触发电平为120（特意不让触发）
            }  // 判断结束
        }  // 循环结束
        if( ChannelFactory.isDynamicCh(ch) ){  // 判断：当前通道是否为有效动态通道
            ChannelFactory.chActivate(ch);  // 激活校准通道
        }  // 判断结束

        rstCalculate();  // 重置计算变量
        type = 0;  // 初始化校准类型为0
        errcode = 0;  // 清零错误码
        resultString.add("<<<<<<<<<< chCofitCalibrate start ......");  // 添加校准开始日志
        Log.i(TAG1,"<<<<<<<<<< chCofitCalibrate start ......");  // 输出校准开始日志
    }  // 方法结束



    /**
     * 执行校准
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的onCalibrate方法</li>
     *   <li>执行偏移量系数校准的核心逻辑</li>
     *   <li>通过迭代调整通道系数实现校准</li>
     * </ul>
     * 
     * <p><b>校准算法：</b>
     * <pre>
     * 1. 等待硬件操作完成
     * 2. 采集波形数据
     * 3. 计算波形平均值
     * 4. 计算误差（mean = offset - average）
     * 5. 记录最佳系数值
     * 6. 根据误差调整系数：
     *    - fx = mean / offset * coef_default
     *    - 精细调节：误差<2像素时fx/=4，误差<4像素时fx/=2
     *    - 新系数 = 旧系数 - fx
     * 7. 判断收敛条件：|best_value| < 0.5像素
     * 8. 保存校准结果
     * </pre>
     * 
     * @return true表示校准完成，false表示校准进行中
     */
    @Override  // 覆盖父类方法
    public boolean onCalibrate() {  // 执行校准方法
        //等待硬件操作完成
        if(!isFinishedAction())  // 判断：硬件操作是否完成
            return false;  // 硬件操作未完成，继续等待

        float average[]={0,0,0,0,0,0,0,0};  // 波形平均值数组，每个通道一个值
        long sum;  // 波形求和变量
        WaveData waveData;  // 波形数据对象
        int N;  // 波形长度
        HwConfig hwConfig = HwConfig.getInstance();  // 获取硬件配置实例
        //获取每个通道的波形平均值
        for(int i=0; i<channelNums; i++){  // 循环：遍历所有通道
            waveData = (WaveData) getWave(i);  // 获取通道i的波形数据
            if(waveData == null || (N = waveData.getWaveLength()) < 10)  // 判断：波形数据是否有效
                return false;  // 波形数据无效，继续等待
            sum = MathNative.calcSum(waveData.getByteBuffer());  // 计算波形数据和
            average[i] = (float) hwConfig.getWavFactor() * sum / N;  // 计算波形平均值（应用波形因子）
        }  // 循环结束

        for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
            if(ch < 0 || ch == i){  // 判断：是否为需要校准的通道
                    float mean = offset-average[i];  // 计算误差（目标偏移 - 实际平均值）
                    if(Math.abs(mean) < Math.abs(best_value[i])){  // 判断：误差是否为历史最小
                        best_value[i] = mean;  // 更新最佳误差值
                        bakCoef[i] = cabteRegister.getChannelCoef(i,vIdx,idx);//cofit[i][vIdx][idx];  // 备份当前系数值
                        Log.i(TAG1,"ch"+(i+1)+"找到更佳值");  // 输出日志：找到更佳值
                    }  // 判断结束

                    float tpCoef = cabteRegister.getChannelCoef(i,vIdx,idx);  // 获取当前系数值
                    float coef_default = vDefaultVal;  // 获取默认系数值
                    float fx = mean/offset*coef_default;  // 计算系数调整量

                    float wuc=Math.abs(mean);  // 计算误差绝对值

                    if(wuc < 2)  // 判断：误差是否小于2像素
                        fx /= 4;  // 精细调节：调整量除以4
                    else if(wuc < 4)  // 判断：误差是否小于4像素
                        fx /= 2;  // 中等调节：调整量除以2

                    float xvv = tpCoef;  // 初始化新系数为当前系数

                    xvv -= fx;  // 计算新系数（减去调整量）

                    if(xvv  < 0){  // 判断：新系数是否为负
                        xvv = coef_default * 1.2f;  // 重置为默认值的1.2倍
                    }  // 判断结束
                    cabteRegister.setChannelCoef(i,vIdx,idx,xvv);  // 设置新的通道系数

                    Log.i(TAG1, (i+1)+":\t"+  // 输出校准日志
                            "像素误差:"+wuc+  // 误差值
                            " ,旧系数:"+tpCoef+  // 旧系数
                            " ,新系数:"+xvv+  // 新系数
                            " ,默认系数:"+coef_default + ",mean:" + mean);  // 默认系数和误差

                    channel[i].setPos(-offset);  // 重新设置通道垂直位置
            }  // 判断结束
        }  // 循环结束



        delaySet(3);  // 设置延时为3秒

        boolean bDone = true;  // 校准完成标志，初始为true

        for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
            if(ch < 0 || ch == i){  // 判断：是否为需要校准的通道
                if(Math.abs(best_value[i]) > 0.5){  // 判断：最佳误差是否大于0.5像素
                    bDone = false;  // 未收敛，设置完成标志为false
                    break;  // 跳出循环
                }  // 判断结束
            }  // 判断结束
        }  // 循环结束
        if(bDone  // 判断：是否所有通道都收敛
                && errcode == 0){  // 判断：是否无错误

            param[0] = ch;  // 设置参数：通道索引
            param[1] = vIdx * 2 + idx;  // 设置参数：系数索引
            if(ch < 0){  // 判断：是否校准所有通道
                for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
                    cabteRegister.setCalibrationState(HW_MHO38V1.CHOFFSET_CALIBRATE_CH1 + i * 8 + param[1] ,true);  // 设置所有通道的偏移量校准状态为已完成
                }  // 循环结束
            }else{  // 否则：校准指定通道
                cabteRegister.setCalibrationState(HW_MHO38V1.CHOFFSET_CALIBRATE_CH1 + ch * 8 + param[1] ,true);  // 设置指定通道的偏移量校准状态为已完成
            }  // 判断结束
            return true;  // 返回true，校准完成
        }  // 判断结束

        return false;  // 返回false，继续校准
    }  // 方法结束


}  // 类结束
