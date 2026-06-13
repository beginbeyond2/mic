package com.micsig.tbook.scope.Calibrate.MHO38v1;  // 包声明：MHO38 V1示波器校准模块

import android.util.Log;  // 导入：Android日志工具，用于输出调试信息

import com.micsig.base.DoubleUtil;  // 导入：双精度工具类，用于模糊比较
import com.micsig.base.Logger;  // 导入：日志工具类，用于日志输出
import com.micsig.tbook.hardware.HardwareProduct;  // 导入：硬件产品类，用于产品信息
import com.micsig.tbook.scope.Action.ChannelHardw;  // 导入：通道硬件操作类，用于硬件控制
import com.micsig.tbook.scope.Calibrate.CabteRegister;  // 导入：校准寄存器管理类，用于管理校准数据
import com.micsig.tbook.scope.Calibrate.Calibrate;  // 导入：校准抽象基类，定义校准流程接口
import com.micsig.tbook.scope.Calibrate.HwConfig;  // 导入：硬件配置类，用于获取硬件参数
import com.micsig.tbook.scope.Data.WaveData;  // 导入：波形数据类，用于存储波形数据
import com.micsig.tbook.scope.ScopeBase;  // 导入：示波器基类，包含基础配置常量
import com.micsig.tbook.scope.Trigger.Trigger;  // 导入：触发器基类，定义触发类型
import com.micsig.tbook.scope.Trigger.TriggerCommon;  // 导入：触发器公共配置类
import com.micsig.tbook.scope.Trigger.TriggerEdge;  // 导入：边沿触发器类，用于边沿触发控制
import com.micsig.tbook.scope.Trigger.TriggerFactory;  // 导入：触发器工厂类，用于创建触发器
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入：通道工厂类，用于通道管理
import com.micsig.tbook.scope.fpga.FPGACommand;  // 导入：FPGA命令类，用于发送FPGA控制命令
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 导入：水平轴类，用于时基控制
import com.micsig.tbook.scope.math.MathNative;  // 导入：数学运算本地方法类，用于波形计算
import com.micsig.tbook.scope.vertical.VerticalAxis;  // 导入：垂直轴类，用于垂直档位转换

/**
 * MHO38 V1示波器通道增益校准实现类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate.MHO38v1（MHO38 V1示波器校准模块）</li>
 *   <li>架构层级：校准业务层 - 增益校准具体实现</li>
 *   <li>设计模式：继承Calibrate抽象基类，实现增益校准流程</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现MHO38 V1示波器的通道增益校准</li>
 *   <li>通过测量标准信号计算增益系数</li>
 *   <li>支持单通道和全通道校准</li>
 *   <li>支持多个输入幅度点的增益校准</li>
 * </ul>
 * 
 * <p><b>校准原理：</b>
 * <pre>
 * 增益校准目标：
 *   测量不同输入幅度下的增益值，用于计算PGA步进dB、低增益高增益比和ADC满量程系数
 * 
 * 校准方法：
 *   1. 输入标准方波信号（30Hz，占空比50%）
 *   2. 方波高电压：scale*4(V)
 *   3. 方波低电压：-scale*4(V)
 *   4. 采集波形数据
 *   5. 计算波形高电平和低电平的平均值差
 *   6. 记录增益值到对应数组
 * 
 * 输入幅度点：
 *   - 40.8mV：用于计算PGA步进dB和ADC满量程
 *   - 257mV：用于计算PGA步进dB
 *   - 25.7mV：用于计算低增益高增益比
 *   - 10.2mV：用于计算ADC满量程
 *   - 11.2mV：用于计算ADC满量程
 *   - 44.8mV：用于计算ADC满量程
 *   - 510mV：用于计算ADC满量程
 *   - 560mV：用于计算ADC满量程
 * </pre>
 * 
 * <p><b>校准流程：</b>
 * <pre>
 * 1. calibratePrepare()：初始化校准参数
 *    ├── 设置时基为100μs/div
 *    ├── 设置触发模式为普通触发
 *    ├── 根据输入幅度选择档位和PGA值
 *    ├── 设置通道参数（探头、档位、位置）
 *    ├── 设置PGA增益值
 *    └── 初始化ADC满量程
 * 
 * 2. onCalibrate()：执行校准
 *    ├── step 0：双通道模式采集数据
 *    │   └── 记录各通道增益值
 *    ├── step 1：单通道模式采集数据（部分幅度点需要）
 *    │   └── 记录各ADC增益值
 *    └── step 2：计算增益系数并保存
 * 
 * 3. 增益系数计算：
 *    - PGA步进dB：根据40.8mV和257mV数据计算
 *    - 低增益高增益比：根据25.7mV数据计算
 *    - ADC满量程：根据10.2mV、11.2mV、44.8mV、510mV、560mV数据计算
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：Calibrate（校准抽象基类）</li>
 *   <li>依赖：CabteRegister（校准寄存器管理）</li>
 *   <li>依赖：HW_MHO38V1（硬件操作类，校准状态定义和增益数组）</li>
 *   <li>依赖：FPGACommand（FPGA命令管理）</li>
 *   <li>依赖：ChannelHardw（通道硬件操作）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>示波器出厂校准时校准增益系数</li>
 *   <li>周期性校准维护时校准增益系数</li>
 *   <li>用户手动触发增益校准</li>
 * </ul>
 * 
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>校准前需要连接标准信号源</li>
 *   <li>校准过程中不要切换档位或通道</li>
 *   <li>校准结果存储在Flash中，断电不丢失</li>
 *   <li>每个通道有8个输入幅度点的增益数据</li>
 * </ul>
 * 
 * @author xuj  // 作者：xuj
 * @version 1.0  // 版本号：1.0
 * @see Calibrate 校准抽象基类  // 参见：Calibrate类
 * @see HW_MHO38V1 MHO38 V1硬件操作类  // 参见：HW_MHO38V1类
 * @see CabteRegister 校准寄存器管理  // 参见：CabteRegister类
 */
/**
 * Created by xuj on 2018/7/18.
 * 通道增益校准，每次校准1个通道，或4个通道同时校准，但每次只能校准1个档位
 * 输入校准信号标准：
 *   1. 30Hz交流方波，占空比50%；
 *   2. 方波高电压scale*4(V)；;
 *   3. 方波低电压-scale*4(V)；
 */
public class MHO38v1_ChGainCalibrate extends Calibrate {  // 继承Calibrate抽象基类，实现增益校准
    
    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，传递校准类型</li>
     *   <li>设置初始延时为2秒</li>
     * </ul>
     * 
     * @param calibrateType 校准类型（参见HW_MHO38V1中的校准状态常量）
     */
    public MHO38v1_ChGainCalibrate(int calibrateType) {  // 构造函数，接收校准类型参数
        super(calibrateType);  // 调用父类Calibrate的构造函数，传递校准类型
        delaySet(2);  // 设置初始延时为2秒
    }  // 构造函数结束

    /**
     * 错误码成员变量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储校准过程中的错误码</li>
     *   <li>0表示无错误，非0表示校准失败</li>
     * </ul>
     * 
     * <p><b>错误码定义：</b>
     * <pre>
     * 0：校准成功
     * 3005：输入幅度索引无效
     * 其他：硬件错误或参数错误
     * </pre>
     */
    private int errcode;  // 错误码成员变量
    
    /**
     * 日志标签
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于日志输出时标识增益校准模块</li>
     *   <li>简化日志输出，便于日志过滤</li>
     * </ul>
     */
    private final String TAG = "ChGain";  // 日志标签，固定为"ChGain"
    
    /**
     * 完整日志标签
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>组合父类标签和当前标签</li>
     *   <li>格式："父类标签:ChGain"</li>
     * </ul>
     */
    private final String TAG1=TAG_PRI+":"+TAG;  // 完整日志标签，格式为"父类标签:ChGain"
    
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
    private volatile int ch=0;  // 当前校准通道索引，volatile保证多线程可见性，默认为0
    
    /**
     * 当前处理的通道索引
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>在多通道校准时，标识当前处理的通道</li>
     *   <li>用于单通道模式切换</li>
     * </ul>
     */
    private volatile int chIdx = 0;  // 当前处理的通道索引，volatile保证多线程可见性

    /**
     * 校准方波的基准幅度值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>校准方波的基准幅度值（像素）</li>
     *   <li>应为8*50个像素</li>
     * </ul>
     */
    private int vPos = 100;  // 校准方波的基准幅度值应为8*50个像素
    
    /**
     * 校准方波的最大幅度值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>校准方波的最大幅度值（像素）</li>
     *   <li>为基准值的5倍</li>
     * </ul>
     */
    private int vMax = 100*5;  // 校准方波的最大幅度值，为基准值的5倍
    
    /**
     * 校准步骤
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>控制校准流程的状态机步骤</li>
     * </ul>
     * 
     * <p><b>步骤定义：</b>
     * <pre>
     * 0：双通道模式采集
     * 1：单通道模式采集（部分幅度点需要）
     * 2：计算增益系数并保存
     * </pre>
     */
    private volatile int step;  // 校准步骤，volatile保证多线程可见性
    
    /**
     * 测量计数器
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录当前已采集的波形数量</li>
     *   <li>用于多次测量取平均值</li>
     * </ul>
     */
    private int meatCnt;  // 测量计数器
    
    /**
     * 信号源幅度
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>校准信号源的幅度值</li>
     *   <li>单位：V</li>
     *   <li>计算公式：srcAmp = vScaleVal * 8</li>
     * </ul>
     */
    private volatile double srcAmp;  // 信号源幅度，volatile保证多线程可见性
    
    /**
     * 校准档位电压值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>增益校准使用的档位电压值</li>
     * </ul>
     */
    private volatile double vScaleVal;  // 校准档位电压值，volatile保证多线程可见性

    /**
     * 输入幅度索引
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>标识当前校准的输入幅度点索引</li>
     *   <li>对应HW_MHO38V1.GAIN_INPUT_AMP_xxx常量</li>
     * </ul>
     */
    private volatile int idx;  // 输入幅度索引，volatile保证多线程可见性
    
    /**
     * 波形平均值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道波形高电平和低电平的平均值差</li>
     *   <li>用于计算增益值</li>
     * </ul>
     */
    private double average[]={0,0,0,0,0,0,0,0};  // 波形平均值数组，每个通道一个值
    
    /**
     * 波形最小值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道波形的最小值</li>
     * </ul>
     */
    private double min[]={0,0,0,0,0,0,0,0};  // 波形最小值数组，每个通道一个值
    
    /**
     * 波形最大值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道波形的最大值</li>
     * </ul>
     */
    private double max[]={0,0,0,0,0,0,0,0};  // 波形最大值数组，每个通道一个值

    /**
     * 最佳误差值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道历史最小的误差值</li>
     *   <li>用于记录最佳校准结果</li>
     * </ul>
     */
    private double []best_value=new double[ChannelFactory.CH_CNT];  // 最佳误差值数组，每个通道一个值
    
    /**
     * 备份系数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道最佳校准时的系数值</li>
     * </ul>
     */
    private short []bakCoef=new short[ChannelFactory.CH_CNT];  // 备份系数数组，每个通道一个值
    
    /**
     * 状态数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道的校准状态</li>
     * </ul>
     */
    private int []state=new int[ChannelFactory.CH_CNT];  // 状态数组，每个通道一个值
    
    /**
     * 尝试次数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录每个通道校准迭代的次数</li>
     * </ul>
     */
    private int []tryNum=new int[ChannelFactory.CH_CNT];  // 尝试次数数组，每个通道一个值
    
    /**
     * 尝试次数数组（备用）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录每个通道校准迭代的次数（备用）</li>
     * </ul>
     */
    private int []tryChiShu=new int[ChannelFactory.CH_CNT];  // 尝试次数数组（备用），每个通道一个值
    
    /**
     * PGA尝试值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录每个通道PGA调整的尝试值</li>
     * </ul>
     */
    private int []trypga=new int[ChannelFactory.CH_CNT];  // PGA尝试值数组，每个通道一个值
    
    /**
     * 完成标志数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>标识每个通道是否完成校准</li>
     * </ul>
     */
    private boolean []finished=new boolean[ChannelFactory.CH_CNT];  // 完成标志数组，每个通道一个值

    /**
     * 通道使能数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>标识每个通道是否参与校准</li>
     * </ul>
     */
    private boolean []ch_en=new boolean[ChannelFactory.CH_CNT];  // 通道使能数组，每个通道一个值
    
    /**
     * 通道位置数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道的垂直位置值</li>
     * </ul>
     */
    private volatile double [] chpos ={0,0,0,0,0,0,0,0};  // 通道位置数组，每个通道一个值
    
    /**
     * 通道硬件操作对象
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于设置通道PGA增益等硬件操作</li>
     * </ul>
     */
    private ChannelHardw channelHardw;  // 通道硬件操作对象

    /**
     * MHO38 V1硬件操作对象
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于访问校准状态和增益数组</li>
     * </ul>
     */
    HW_MHO38V1 hw;  // MHO38 V1硬件操作对象

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
     * 1. 重置最佳误差值为最大值
     * 2. 清零状态数组
     * 3. 清零平均值数组
     * 4. 重置最大值为负最大值
     * 5. 重置最小值为最大值
     * 6. 清零尝试次数数组
     * 7. 清零测量计数
     * </pre>
     */
    //校准下一档初始化
    private void rstCalculate(){  // 重置计算变量方法
        for(int i=0; i<ChannelFactory.CH_CNT; i++) {  // 循环：遍历所有通道
            best_value[i] = Float.MAX_VALUE;  // 重置最佳误差值为最大值
            state[i] = 0;  // 清零状态
            average[i] = 0;  // 清零平均值
            max[i] = -Double.MAX_VALUE;  // 重置最大值为负最大值
            min[i] = Double.MAX_VALUE;  // 重置最小值为最大值
            tryNum[i] = 0;  // 清零尝试次数
            tryChiShu[i] = 0;  // 清零尝试次数（备用）
            trypga[i] = 0;  // 清零PGA尝试值
        }  // 循环结束
        meatCnt = 0;  // 清零测量计数
    }  // 方法结束

    /**
     * 初始化校准寄存器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的iniCalibrateReg方法</li>
     *   <li>初始化通道硬件操作对象和硬件操作对象</li>
     * </ul>
     */
    @Override  // 覆盖父类方法
    public void iniCalibrateReg(){  // 初始化校准寄存器方法
        channelHardw = ChannelHardw.getInstance();  // 获取通道硬件操作实例
        hw = (HW_MHO38V1)cabteRegister.getHw();  // 获取MHO38 V1硬件操作实例
    }  // 方法结束

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
        if(vol instanceof double[]) {  // 判断：参数是否为double数组
            double[] param=(double[])vol;  // 转换参数类型
            if(param.length >= 3){  // 判断：参数长度是否足够
                int ix =(int)(param[0] + 0.1);  // 获取通道索引（加0.1避免浮点误差）
                if(param[0] < 0){  // 判断：通道索引是否为负
                    ix = -1;  // 设置为-1，表示所有通道
                }  // 判断结束
                if(ix == -1 || ChannelFactory.isDynamicCh(ix)) {  // 判断：通道索引是否有效
                    ch = ix;  // 设置校准通道
                    vScaleVal = param[1];  // 设置档位电压值
                    srcAmp = vScaleVal * 8;  // 计算信号源幅度（档位值*8）
                    Logger.d(TAG,"ch:" + ch + ",vScaleVal:" + vScaleVal + ",srcAmp:" + srcAmp);  // 输出调试日志
                    return;  // 返回

                }  // 判断结束
            }  // 判断结束
        }  // 判断结束
        ch = ChannelFactory.CH1;  // 默认校准通道1
        vScaleVal = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_10mV);  // 默认档位为10mV/div
    }  // 方法结束

    /**
     * 参数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于返回校准参数</li>
     *   <li>存储通道索引和输入幅度索引</li>
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
     * @return 参数数组，[通道索引, 输入幅度索引]
     */
    @Override  // 覆盖父类方法
    public Object getParam() {  // 获取校准参数方法
        param[0] = ch;  // 设置通道索引
        param[1] = idx;  // 设置输入幅度索引
        return param;  // 返回参数数组
    }  // 方法结束
    
    /**
     * 设置PGA增益值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置所有通道的PGA增益值</li>
     *   <li>通过FPGA命令和通道硬件操作设置</li>
     * </ul>
     * 
     * @param val PGA增益值
     */
    private void setPGAVal(int val){  // 设置PGA增益值方法
        FPGACommand fpgaCommand = FPGACommand.getInstance();  // 获取FPGA命令实例
        fpgaCommand.cmdDevice(300);  // 发送设备命令300
        int [] res ={val,val,val,val,val,val,val,val};  // 初始化PGA值数组，所有通道相同
        channelHardw.set_ch_AD8370Gain(res);  // 设置所有通道的AD8370增益
        fpgaCommand.setPgaVal(val);  // 设置FPGA的PGA值
        fpgaCommand.cmdDevice(300);  // 发送设备命令300
    }  // 方法结束
    
    /**
     * 根据电压值获取增益索引
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据输入幅度值查找对应的增益索引</li>
     *   <li>使用模糊比较匹配</li>
     * </ul>
     * 
     * @param v 输入幅度值（V）
     * @return 增益索引，未找到返回-1
     */
    public static int getGainIdx(double v){  // 根据电压值获取增益索引方法
        for(int i=0;i<HW_MHO38V1.GAIN_INPUT_AMP.length;i++){  // 循环：遍历所有输入幅度
            if(DoubleUtil.FuzzyCompare(HW_MHO38V1.GAIN_INPUT_AMP[i],v)){  // 判断：是否模糊匹配
                return i;  // 返回匹配的索引
            }  // 判断结束
        }  // 循环结束
        return -1;  // 未找到，返回-1
    }  // 方法结束


    /**
     * 档位索引
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>当前校准使用的档位索引</li>
     *   <li>根据输入幅度自动选择</li>
     * </ul>
     */
    volatile int ratioIdx = 0;  // 档位索引，volatile保证多线程可见性
    
    /**
     * 校准准备
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的calibratePrepare方法</li>
     *   <li>初始化校准环境和参数</li>
     *   <li>设置时基、触发、通道、PGA等参数</li>
     * </ul>
     * 
     * <p><b>初始化流程：</b>
     * <pre>
     * 1. 初始化完成标志和通道使能
     * 2. 设置ADC差分增益校准模式
     * 3. 设置时基为100μs/div
     * 4. 设置触发模式为普通触发
     * 5. 根据输入幅度选择档位和PGA值
     * 6. 设置通道参数（探头、档位、位置）
     * 7. 设置触发源和触发电平
     * 8. 设置PGA增益值
     * 9. 初始化ADC满量程
     * 10. 重置计算变量
     * </pre>
     */
    @Override  // 覆盖父类方法
    public void calibratePrepare() {  // 校准准备方法
        for(int i=0; i<finished.length; i++) {  // 循环：遍历所有通道
            finished[i] = true;  // 初始化完成标志为true
            ch_en[i] = false;  // 初始化通道使能为false
        }  // 循环结束
        FPGACommand.getInstance().setADdiffGainCalib(true);  // 设置ADC差分增益校准模式
        HorizontalAxis.getInstance().setTimeScaleIdOfView(HorizontalAxis.TSI_100uS);  // 设置时基为100μs/div
        TriggerFactory triggerFactory=TriggerFactory.getInstance();  // 获取触发器工厂实例
        triggerFactory.getTriggerCommon().setTriggerMode(TriggerCommon.TM_NORMAL);  // 设置触发模式为普通触发
        TriggerEdge triggerEdge=(TriggerEdge)(triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));  // 获取边沿触发器
        triggerEdge.setTriggerEdge(TriggerEdge.TET_ASC);  // 设置触发边沿为上升沿

        idx = getGainIdx(vScaleVal);  // 根据档位电压值获取增益索引
        if(idx < 0){  // 判断：增益索引是否无效
            Log.e(TAG,"error:vScaleVal:" + vScaleVal);  // 输出错误日志
        }  // 判断结束
        Logger.d(TAG,"idx:" + idx + ",vScaleVal:" + vScaleVal);  // 输出调试日志


        int pga = HW_MHO38V1.LG1;  // 初始化PGA值为低增益模式

        switch (idx){  // 根据输入幅度索引选择档位和PGA值
            case HW_MHO38V1.GAIN_INPUT_AMP_44_8_MV:  // 44.8mV输入幅度
            case HW_MHO38V1.GAIN_INPUT_AMP_40_8_MV:  // 40.8mV输入幅度
                ratioIdx = HW_MHO38V1.RATIO_DANG_2;  // 使用档位2
                pga = HW_MHO38V1.LG1;  // 使用低增益模式PGA值
                break;  // 跳出switch
            case HW_MHO38V1.GAIN_INPUT_AMP_257_MV:  // 257mV输入幅度
                ratioIdx = HW_MHO38V1.RATIO_DANG_2;  // 使用档位2
                pga = HW_MHO38V1.LG9;  // 使用低增益模式PGA值（9步）
                break;  // 跳出switch
            case HW_MHO38V1.GAIN_INPUT_AMP_25_7_MV:  // 25.7mV输入幅度
                ratioIdx = HW_MHO38V1.RATIO_DANG_2;  // 使用档位2
                pga = HW_MHO38V1.HG9;  // 使用高增益模式PGA值（9步）
                break;  // 跳出switch
            case HW_MHO38V1.GAIN_INPUT_AMP_11_2_MV:  // 11.2mV输入幅度
            case HW_MHO38V1.GAIN_INPUT_AMP_10_2_MV:  // 10.2mV输入幅度
                ratioIdx = HW_MHO38V1.RATIO_DANG_1;  // 使用档位1
                pga = HW_MHO38V1.LG1;  // 使用低增益模式PGA值
                break;  // 跳出switch
            case HW_MHO38V1.GAIN_INPUT_AMP_560_MV:  // 560mV输入幅度
            case HW_MHO38V1.GAIN_INPUT_AMP_510_MV:  // 510mV输入幅度
                ratioIdx = HW_MHO38V1.RATIO_DANG_3;  // 使用档位3
                pga = HW_MHO38V1.LG1;  // 使用低增益模式PGA值
                break;  // 跳出switch
        }  // switch结束
        ms_sleep(300);  // 延时300ms，等待硬件稳定
        double x = 1;  // 初始化变量x

        Log.d(TAG,"ch:" + ch + ",idx:" + idx + ",pga:" + pga + ",ratioIdx:" + ratioIdx);  // 输出调试日志
        for(int i=0; i<channelNums; i++) {  // 循环：遍历所有通道
            channel[i].setProbeRate(1);  // 设置探头衰减比为1X
            channel[i].setVScaleId(CabteRegister.getRatioIdx2Dang(0,ratioIdx));  // 设置档位ID
            x = channel[i].getADVerticalPerPix()/channel[i].getProbeRate();  // 计算每像素电压值
            chpos[i] = srcAmp / (2 * x);  // 计算通道垂直位置
            channel[i].setPos(-(int)(chpos[i]));  // 设置通道垂直位置
        }  // 循环结束
        ms_sleep(400);  // 延时400ms，等待参数生效
        if(ch < 0) {  // 判断：是否校准所有通道
            triggerEdge.setTriggerSource(0);  // 设置触发源为通道1
            x = channel[0].getADVerticalPerPix()/channel[0].getProbeRate();  // 计算每像素电压值
            for(int k = 0;k<channelNums;k++){  // 循环：遍历所有通道
                finished[k] = false;  // 设置完成标志为false
                ch_en[k] = true;  // 设置通道使能为true
            }  // 循环结束
            chIdx = 0;  // 初始化当前处理的通道索引为0
        } else {  // 否则：校准指定通道
            triggerEdge.setTriggerSource(ch);  // 设置触发源为指定通道
            x = channel[ch].getADVerticalPerPix()/channel[ch].getProbeRate();  // 计算每像素电压值
            finished[ch] = false;  // 设置完成标志为false
            ch_en[ch] = true;  // 设置通道使能为true
            chIdx = ch;  // 设置当前处理的通道索引
        }  // 判断结束
        step = 0;  // 初始化校准步骤为0
        chflag = 0;  // 清零通道标志
        int levelPos = (int)(srcAmp/(2 * x));  // 计算触发电平位置
        triggerEdge.getTriggerLevel().setPos(levelPos);  // 设置触发电平
        ms_sleep(500);  // 延时500ms，等待触发稳定
        triggerEdge.getTriggerLevel().setPos(levelPos);  // 再次设置触发电平（确保生效）
        setPGAVal(pga);  // 设置PGA增益值
        FPGACommand fpgaCommand = FPGACommand.getInstance();  // 获取FPGA命令实例
        for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
            fpgaCommand.writeAD_gain(i/4,(i % 4) / 2,i & 0x01, getFS());  // 写入ADC满量程值
        }  // 循环结束
        fpgaCommand.ADCalibrate();  // 执行ADC校准
        ms_sleep(400);  // 延时400ms，等待ADC校准完成
        triggerEdge.getTriggerLevel().setPos(levelPos);  // 第三次设置触发电平（界面消息乱发在设置一次）
        delaySet(5);  // 设置延时为5秒
        rstCalculate();  // 重置计算变量
        fpgaSync();  // FPGA同步
        errcode = 0;  // 清零错误码
        resultString.add("chGainCalibrate start");  // 添加校准开始日志
        Log.i(TAG1,"chGainCalibrate start");  // 输出校准开始日志

        bCheckParam = true;  // 设置参数检查标志为true
    }  // 方法结束
    
    /**
     * 参数检查标志
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>标识是否需要检查参数</li>
     *   <li>参数变化时重置校准状态</li>
     * </ul>
     */
    boolean bCheckParam = true;  // 参数检查标志，初始为true
    
    /**
     * 检查参数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的checkParam方法</li>
     *   <li>检查校准参数是否正确</li>
     *   <li>如果参数变化，重置校准状态</li>
     * </ul>
     * 
     * <p><b>检查内容：</b>
     * <pre>
     * 1. 通道垂直位置是否正确
     * 2. 触发源是否正确
     * 3. 触发电平是否正确
     * </pre>
     * 
     * @return true表示参数有变化，false表示参数正确
     */
    @Override  // 覆盖父类方法
    public boolean checkParam() {  // 检查参数方法
        boolean bChange = false;  // 参数变化标志，初始为false
        int chidx = Math.max(ch,0);  // 获取有效的通道索引（-1转为0）

        if(ChannelFactory.isDynamicCh(chidx)) {  // 判断：通道是否为有效动态通道

            for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
                double pp = -Math.round(chpos[i]);  // 计算目标位置
                if(Math.abs(pp - channel[i].getPos()) > 1){  // 判断：通道位置是否偏离目标
                    bChange = true;  // 设置参数变化标志
                }  // 判断结束
            }  // 循环结束

            if(bChange){  // 判断：参数是否有变化
                updateSync();  // 更新同步
                for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
                    double pp = -Math.round(chpos[i]);  // 计算目标位置
                    if(Math.abs(pp - channel[i].getPos()) > 1){  // 判断：通道位置是否偏离目标
                        channel[i].setPos(pp);  // 修正通道位置
                    }  // 判断结束
                }  // 循环结束
            }  // 判断结束

            TriggerFactory triggerFactory = TriggerFactory.getInstance();  // 获取触发器工厂实例

            TriggerEdge triggerEdge = (TriggerEdge) (triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));  // 获取边沿触发器
            if (!triggerEdge.isTriggerSource(chidx)) {  // 判断：触发源是否正确
                triggerEdge.setTriggerSource(chidx);  // 修正触发源
                bChange = true;  // 设置参数变化标志
            }  // 判断结束


            double levelPos = -channel[chidx].getPos();  // 计算触发电平位置
            if (Math.abs(triggerEdge.getTriggerLevel().getPos() - levelPos) > 1) {  // 判断：触发电平是否偏离目标
                triggerEdge.getTriggerLevel().setPos(levelPos);  // 修正触发电平
                bChange = true;  // 设置参数变化标志
            }  // 判断结束
        }  // 判断结束
        if(bChange){  // 判断：参数是否有变化
            rstCalculate();  // 重置计算变量
            delaySet(2);  // 设置延时为2秒
            fpgaSync();  // FPGA同步
        }  // 判断结束
        return bChange;  // 返回参数变化标志
    }  // 方法结束


    /**
     * 获取ADC满量程值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据输入幅度索引返回对应的ADC满量程值</li>
     * </ul>
     * 
     * <p><b>满量程映射：</b>
     * <pre>
     * 默认值：16384
     * 44.8mV、11.2mV、560mV：24576
     * </pre>
     * 
     * @return ADC满量程值
     */
    int getFS(){  // 获取ADC满量程值方法
        int fs = 16384;  // 默认满量程值：16384
        switch (idx) {  // 根据输入幅度索引选择满量程值
            case HW_MHO38V1.GAIN_INPUT_AMP_44_8_MV:  // 44.8mV输入幅度
                fs = 24576;  // 满量程值：24576
                break;  // 跳出switch
            case HW_MHO38V1.GAIN_INPUT_AMP_11_2_MV:  // 11.2mV输入幅度
                fs = 24576;  // 满量程值：24576
                break;  // 跳出switch
            case HW_MHO38V1.GAIN_INPUT_AMP_560_MV:  // 560mV输入幅度
                fs = 24576;  // 满量程值：24576
                break;  // 跳出switch
        }  // switch结束
        return fs;  // 返回满量程值
    }  // 方法结束

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
     * 通道标志
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录已完成校准的通道标志位</li>
     *   <li>每位表示一个通道</li>
     * </ul>
     */
    private volatile int chflag = 0;  // 通道标志，volatile保证多线程可见性
    
    /**
     * 执行校准
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的onCalibrate方法</li>
     *   <li>执行增益校准的核心逻辑</li>
     *   <li>通过采集波形数据计算增益值</li>
     * </ul>
     * 
     * <p><b>校准算法：</b>
     * <pre>
     * 1. 等待硬件操作完成
     * 2. 检查输入幅度索引是否有效
     * 3. 检查参数是否正确
     * 4. 采集波形数据
     * 5. 计算波形高电平和低电平的平均值差
     * 6. 多次测量取平均（5次）
     * 7. 根据输入幅度索引保存增益值到对应数组
     * 8. 切换单通道模式（部分幅度点需要）
     * 9. 计算增益系数并保存校准状态
     * </pre>
     * 
     * @return true表示校准完成，false表示校准进行中
     */
    @Override  // 覆盖父类方法
    public boolean onCalibrate() {  // 执行校准方法
        //等待硬件操作完成

        if (!isFinishedAction())  // 判断：硬件操作是否完成
            return false;  // 硬件操作未完成，继续等待

        if(idx < 0){  // 判断：输入幅度索引是否无效
            setErrcode(3005);  // 设置错误码3005
            return true;  // 返回true，校准失败
        }  // 判断结束

        if(bCheckParam){  // 判断：是否需要检查参数
            bCheckParam = checkParam();  // 检查参数
            return false;  // 返回false，继续校准
        }  // 判断结束
        Log.d(TAG, "onCalibrate() called");  // 输出调试日志
        long sum1,sum2;  // 波形求和变量
        WaveData waveData;  // 波形数据对象
        HwConfig hwConfig = HwConfig.getInstance();  // 获取硬件配置实例
        int N;  // 波形长度

        //获取每个通道的波形平均值
        for (int i = 0; i < channelNums; i++) {  // 循环：遍历所有通道

            waveData = (WaveData) getWave(i);  // 获取通道i的波形数据
            if (waveData == null || (N = waveData.getWaveLength()) < 10) {  // 判断：波形数据是否有效
                Log.d(TAG, "onCalibrate() waveData:" + waveData + "i:" + i);  // 输出调试日志
                return false;  // 波形数据无效，继续等待
            }  // 判断结束

            int n = N / ScopeBase.getHorizonGridCnt();  // 计算每格的采样点数
            int len1 = n * (ScopeBase.getHorizonGridCnt()/2 - 2);  // 计算计算窗口大小
            sum1 = MathNative.calcSum(waveData.getByteBuffer(), n, len1);  // 计算前半段波形和
            sum2 = MathNative.calcSum(waveData.getByteBuffer(), n * (ScopeBase.getHorizonGridCnt()/2 + 1), len1);  // 计算后半段波形和

            average[i] += Math.abs((double) sum2/len1 - (double)sum1/len1);  // 累加高电平和低电平的平均值差

        }  // 循环结束
        if(++meatCnt < 5) {  // 判断：是否已采集足够次数
            return false;  // 返回false，继续采集
        }  // 判断结束

        for(int i=0; i<channelNums; i++){  // 循环：遍历所有通道

            average[i] = average[i] / meatCnt;  // 计算平均值

            average[i] *= hwConfig.getWavFactor();  // 应用波形因子
            Log.d(TAG,"ch:" + i + ",average:" + average[i] + ",ch_en:" + ch_en[i]);  // 输出调试日志
        }  // 循环结束

        meatCnt = 0;  // 清零测量计数

        int adIdx = 0;  // ADC索引
        for(int i=0; i<channelNums; i++) {  // 循环：遍历所有通道
            //状态机控制流程
            if((!ch_en[i])){  // 判断：通道是否参与校准

                continue;  // 跳过不参与校准的通道
            }  // 判断结束
            chflag |= (1 << i);  // 设置通道标志位
            adIdx = i/2;  // 计算ADC索引
            if(step == 0) {  // 判断：是否为步骤0（双通道模式）
                switch (idx) {  // 根据输入幅度索引保存增益值
                    case HW_MHO38V1.GAIN_INPUT_AMP_40_8_MV:  // 40.8mV输入幅度
                        HW_MHO38V1.gain_pga_stepdb_a40_8[i] = average[i];  // 保存PGA步进dB增益值
                        HW_MHO38V1.gain_fs_a40_8[i][0] = average[i];  // 保存ADC满量程增益值（ADC0）
                        break;  // 跳出switch
                    case HW_MHO38V1.GAIN_INPUT_AMP_44_8_MV:  // 44.8mV输入幅度
                        HW_MHO38V1.gain_fs_a44_8[i][0] = average[i];  // 保存ADC满量程增益值（ADC0）
                        break;  // 跳出switch
                    case HW_MHO38V1.GAIN_INPUT_AMP_257_MV:  // 257mV输入幅度
                        HW_MHO38V1.gain_pga_stepdb_a257[i] = average[i];  // 保存PGA步进dB增益值
                        break;  // 跳出switch
                    case HW_MHO38V1.GAIN_INPUT_AMP_25_7_MV:  // 25.7mV输入幅度
                        HW_MHO38V1.gain_pga_lghg_a25_7[i] = average[i];  // 保存低增益高增益比增益值
                        break;  // 跳出switch
                    case HW_MHO38V1.GAIN_INPUT_AMP_11_2_MV:  // 11.2mV输入幅度
                        HW_MHO38V1.gain_fs_a11_2[i][0] = average[i];  // 保存ADC满量程增益值（ADC0）
                        break;  // 跳出switch
                    case HW_MHO38V1.GAIN_INPUT_AMP_10_2_MV:  // 10.2mV输入幅度
                        HW_MHO38V1.gain_fs_a10_2[i][0] = average[i];  // 保存ADC满量程增益值（ADC0）
                        break;  // 跳出switch
                    case HW_MHO38V1.GAIN_INPUT_AMP_510_MV:  // 510mV输入幅度
                        HW_MHO38V1.gain_fs_a510[i][0] = average[i];  // 保存ADC满量程增益值（ADC0）
                        break;  // 跳出switch
                    case HW_MHO38V1.GAIN_INPUT_AMP_560_MV:  // 560mV输入幅度
                        HW_MHO38V1.gain_fs_a560[i][0] = average[i];  // 保存ADC满量程增益值（ADC0）
                        break;  // 跳出switch
                }  // switch结束
            }else{  // 否则：步骤1（单通道模式）
                switch (idx){  // 根据输入幅度索引保存增益值
                    case HW_MHO38V1.GAIN_INPUT_AMP_40_8_MV:  // 40.8mV输入幅度
                        HW_MHO38V1.gain_fs_a40_8[i][1] = average[adIdx * 2];  // 保存ADC满量程增益值（ADC1）
                        HW_MHO38V1.gain_fs_a40_8[i][2] = average[adIdx * 2 + 1];  // 保存ADC满量程增益值（ADC2）
                        break;  // 跳出switch
                    case HW_MHO38V1.GAIN_INPUT_AMP_44_8_MV:  // 44.8mV输入幅度
                        HW_MHO38V1.gain_fs_a44_8[i][1] = average[adIdx * 2];  // 保存ADC满量程增益值（ADC1）
                        HW_MHO38V1.gain_fs_a44_8[i][2] = average[adIdx * 2 + 1];  // 保存ADC满量程增益值（ADC2）
                        break;  // 跳出switch
                    case HW_MHO38V1.GAIN_INPUT_AMP_257_MV:  // 257mV输入幅度
                        break;  // 不需要单通道模式
                    case HW_MHO38V1.GAIN_INPUT_AMP_25_7_MV:  // 25.7mV输入幅度
                        break;  // 不需要单通道模式
                    case HW_MHO38V1.GAIN_INPUT_AMP_11_2_MV:  // 11.2mV输入幅度
                        HW_MHO38V1.gain_fs_a11_2[i][1] = average[adIdx * 2];  // 保存ADC满量程增益值（ADC1）
                        HW_MHO38V1.gain_fs_a11_2[i][2] = average[adIdx * 2 + 1];  // 保存ADC满量程增益值（ADC2）
                        break;  // 跳出switch
                    case HW_MHO38V1.GAIN_INPUT_AMP_10_2_MV:  // 10.2mV输入幅度
                        HW_MHO38V1.gain_fs_a10_2[i][1] = average[adIdx * 2];  // 保存ADC满量程增益值（ADC1）
                        HW_MHO38V1.gain_fs_a10_2[i][2] = average[adIdx * 2 + 1];  // 保存ADC满量程增益值（ADC2）
                        break;  // 跳出switch
                    case HW_MHO38V1.GAIN_INPUT_AMP_510_MV:  // 510mV输入幅度
                        HW_MHO38V1.gain_fs_a510[i][1] = average[adIdx * 2];  // 保存ADC满量程增益值（ADC1）
                        HW_MHO38V1.gain_fs_a510[i][2] = average[adIdx * 2 + 1];  // 保存ADC满量程增益值（ADC2）
                        break;  // 跳出switch
                    case HW_MHO38V1.GAIN_INPUT_AMP_560_MV:  // 560mV输入幅度
                        HW_MHO38V1.gain_fs_a560[i][1] = average[adIdx * 2];  // 保存ADC满量程增益值（ADC1）
                        HW_MHO38V1.gain_fs_a560[i][2] = average[adIdx * 2 + 1];  // 保存ADC满量程增益值（ADC2）
                        break;  // 跳出switch
                }  // switch结束
            }  // 判断结束
        }  // 循环结束
        for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
            average[i] = 0;  // 清零平均值
        }  // 循环结束
        switch (idx){  // 根据输入幅度索引判断是否需要单通道模式
            case HW_MHO38V1.GAIN_INPUT_AMP_257_MV:  // 257mV输入幅度
            case HW_MHO38V1.GAIN_INPUT_AMP_25_7_MV:  // 25.7mV输入幅度
                step = 2;  // 直接跳到步骤2，不需要单通道模式
                break;  // 跳出switch
        }  // switch结束

        if(step != 2){  // 判断：是否未到达步骤2
            switchSignelChannel();  // 切换单通道模式
            delaySet(5);  // 设置延时为5秒
            return false;  // 返回false，继续校准
        }else {  // 否则：已到达步骤2
            FPGACommand fpgaCommand = FPGACommand.getInstance();  // 获取FPGA命令实例
            cabteRegister.calcGain(chflag);  // 计算增益系数
            fpgaCommand.setADdiffGainCalib(false);  // 关闭ADC差分增益校准模式
            step = 0;  // 重置步骤为0
            chflag = 0;  // 清零通道标志
//            fpgaCommand.SendADData(0, 0x0D, 0x3FFF); //双通道模式  // 已注释：发送ADC数据
//            fpgaCommand.SendADData(1, 0x0D, 0x3FFF); //双通道模式  // 已注释：发送ADC数据
            fpgaCommand.setADCChannel(new boolean[]{true,true,true,true,true,true,true,true});  // 设置所有ADC通道使能
            fpgaCommand.ADCalibrate();  // 执行ADC校准
            rstCalculate();  // 重置计算变量
            if(errcode == 0) {  // 判断：是否无错误
                if (ch < 0) {  // 判断：是否校准所有通道
                    for (int i = 0; i < channelNums; i++) {  // 循环：遍历所有通道
                        hw.setCalibrationState(HW_MHO38V1.CALIBRATION_CENTER_CH1GAIN + i * 8 + idx, true);  // 设置所有通道的增益校准状态为已完成
                    }  // 循环结束
                } else {  // 否则：校准指定通道
                    hw.setCalibrationState(HW_MHO38V1.CALIBRATION_CENTER_CH1GAIN + ch * 8 + idx, true);  // 设置指定通道的增益校准状态为已完成
                }  // 判断结束
            }  // 判断结束
            return true;  // 返回true，校准完成
        }  // 判断结束
    }  // 方法结束



    /**
     * 切换单通道模式
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将ADC从双通道模式切换到单通道模式</li>
     *   <li>用于采集每个ADC的独立增益值</li>
     * </ul>
     * 
     * <p><b>切换逻辑：</b>
     * <pre>
     * 1. 标记所有通道为已完成
     * 2. 禁用所有通道
     * 3. 根据校准模式（全通道或指定通道）设置ADC通道
     * 4. 写入ADC满量程值
     * 5. 执行ADC校准
     * </pre>
     */
    private void switchSignelChannel(){  // 切换单通道模式方法
        FPGACommand fpgaCommand = FPGACommand.getInstance();  // 获取FPGA命令实例
        for (int i = 0; i < ChannelFactory.CH_CNT; i++) {  // 循环：遍历所有通道
            finished[i] = true;  // 标记通道为已完成
            ch_en[i] = false;  // 禁用通道
        }  // 循环结束
        if (ch < 0) {  // 判断：是否校准所有通道
            boolean [] sel ={false,false,false,false,false,false,false,false};  // 初始化通道选择数组
            for (int k = 0; k < channelNums / 2; k++) {  // 循环：遍历通道对

                //fpgaCommand.SendADData(k, 0x0D, 0xFFFF);  // 已注释：发送ADC数据
                if ((chIdx & 0x01) == 0) {  // 判断：当前通道索引是否为偶数
                    //fpgaCommand.SendADData(k, 0x0E, 0x07FF); //单通道ch1或ch3  // 已注释：发送ADC数据

                } else {  // 否则：当前通道索引为奇数
                    //fpgaCommand.SendADData(k, 0x0E, 0x87FF); //单通道ch2或ch4  // 已注释：发送ADC数据
                }  // 判断结束
                sel[k * 2 + chIdx] = true;  // 设置通道选择


                fpgaCommand.writeAD_gain(k/2, (k * 2 + chIdx) % 4 / 2,0, getFS());  // 写入ADC满量程值（ADC0）
                fpgaCommand.writeAD_gain(k/2, (k * 2 + chIdx) % 4 / 2,1, getFS());  // 写入ADC满量程值（ADC1）

                finished[k * 2 + chIdx] = false;  // 标记通道为未完成
                ch_en[k * 2 + chIdx] = true;  // 启用通道
            }  // 循环结束
            fpgaCommand.setADCChannel(sel);  // 设置ADC通道选择
            fpgaCommand.ADCalibrate();  // 执行ADC校准
            chIdx++;  // 通道索引加1
            step++;  // 步骤加1
        } else {  // 否则：校准指定通道
            step = 2;  // 直接跳到步骤2
            boolean [] sel ={false,false,false,false,false,false,false,false};  // 初始化通道选择数组
            //fpgaCommand.SendADData(ch / 2, 0x0D, 0xFFFF);  // 已注释：发送ADC数据
            if ((ch & 0x01) == 0) {  // 判断：通道索引是否为偶数
                //fpgaCommand.SendADData(ch / 2, 0x0E, 0x07FF); //单通道ch1或ch3  // 已注释：发送ADC数据
                sel[0] = sel[2] = true;  // 设置通道选择
                sel[4] = sel[6] = true;  // 设置通道选择
            } else {  // 否则：通道索引为奇数
                //fpgaCommand.SendADData(ch / 2, 0x0E, 0x87FF); //单通道ch2或ch4  // 已注释：发送ADC数据
                sel[1] = sel[3] = true;  // 设置通道选择
                sel[5] = sel[7] = true;  // 设置通道选择
            }  // 判断结束

            finished[ch] = false;  // 标记通道为未完成
            ch_en[ch] = true;  // 启用通道
            fpgaCommand.writeAD_gain(ch / 4, ch % 4 / 2,0, getFS());  // 写入ADC满量程值（ADC0）
            fpgaCommand.writeAD_gain(ch / 4, ch % 4 / 2,1, getFS());  // 写入ADC满量程值（ADC1）
            fpgaCommand.setADCChannel(sel);  // 设置ADC通道选择
            fpgaCommand.ADCalibrate();  // 执行ADC校准
        }  // 判断结束

    }  // 方法结束

}  // 类结束
