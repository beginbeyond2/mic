package com.micsig.tbook.scope.Calibrate.MHO38v1;  // 包声明：MHO38 V1示波器校准模块

import android.util.Log;  // 导入：Android日志工具，用于输出调试信息

import com.micsig.base.Logger;  // 导入：日志工具类，用于日志输出
import com.micsig.tbook.hardware.HardwareProduct;  // 导入：硬件产品类，用于产品信息
import com.micsig.tbook.scope.Calibrate.CabteRegister;  // 导入：校准寄存器管理类，用于管理校准数据
import com.micsig.tbook.scope.Calibrate.Calibrate;  // 导入：校准抽象基类，定义校准流程接口
import com.micsig.tbook.scope.Calibrate.HwConfig;  // 导入：硬件配置类，用于获取硬件参数
import com.micsig.tbook.scope.Data.WaveData;  // 导入：波形数据类，用于存储波形数据
import com.micsig.tbook.scope.Sample.Sample;  // 导入：采样控制类，用于控制采样模式
import com.micsig.tbook.scope.ScopeBase;  // 导入：示波器基类，包含基础配置常量
import com.micsig.tbook.scope.Trigger.Trigger;  // 导入：触发器基类，定义触发类型
import com.micsig.tbook.scope.Trigger.TriggerCommon;  // 导入：触发器公共配置类
import com.micsig.tbook.scope.Trigger.TriggerEdge;  // 导入：边沿触发器类，用于边沿触发控制
import com.micsig.tbook.scope.Trigger.TriggerFactory;  // 导入：触发器工厂类，用于创建触发器
import com.micsig.tbook.scope.channel.Channel;  // 导入：通道类，定义通道属性和操作
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入：通道工厂类，用于通道管理
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 导入：水平轴类，用于时基控制
import com.micsig.tbook.scope.math.MathNative;  // 导入：数学运算本地方法类，用于波形计算
import com.micsig.tbook.scope.vertical.VerticalAxis;  // 导入：垂直轴类，用于垂直档位转换

/**
 * MHO38 V1示波器通道电容校准实现类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate.MHO38v1（MHO38 V1示波器校准模块）</li>
 *   <li>架构层级：校准业务层 - 电容校准具体实现</li>
 *   <li>设计模式：继承Calibrate抽象基类，实现电容校准流程</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现MHO38 V1示波器的通道输入电容校准</li>
 *   <li>通过调整电容高值（chCapacitanceHigh）校准输入电容</li>
 *   <li>最小化波形前后段的平均值差异</li>
 *   <li>支持单通道和全通道校准</li>
 * </ul>
 * 
 * <p><b>校准原理：</b>
 * <pre>
 * 电容校准目标：
 *   使波形前后段的平均值相等，消除输入电容对测量的影响
 * 
 * 校准方法：
 *   1. 采集波形数据
 *   2. 计算波形前段和后段的平均值
 *   3. 计算平均值差异（val = average[1] - average[0]）
 *   4. 根据差异调整电容高值：
 *      - val > 阈值：增加电容高值
 *      - val < -阈值：减少电容高值
 *      - |val| < 阈值：校准完成
 *   5. 使用二分法逐步逼近最优值
 * 
 * 电容高值范围：0 ~ 0xFFFF（16位）
 * </pre>
 * 
 * <p><b>校准流程：</b>
 * <pre>
 * 1. calibratePrepare()：初始化校准参数
 *    ├── 设置时基为10μs/div
 *    ├── 设置触发模式为普通触发
 *    ├── 设置触发源和触发电平
 *    ├── 设置通道档位为500mV/div
 *    └── 初始化电容高值为中间值（0x7FFF）
 * 
 * 2. onCalibrate()：执行校准
 *    ├── step 0：延时等待
 *    ├── step 1：延时等待
 *    ├── step 2：迭代校准
 *    │   ├── 采集波形数据
 *    │   ├── 计算前后段平均值
 *    │   ├── 计算差异并调整电容高值
 *    │   └── 判断是否收敛
 *    └── step 3：保存校准结果
 * 
 * 3. 收敛条件：
 *    - |val| < 0.05（阈值）
 *    - 或二分步长为1且连续16次无改进
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
 *   <li>示波器出厂校准时校准输入电容</li>
 *   <li>周期性校准维护时校准输入电容</li>
 *   <li>用户手动触发电容校准</li>
 * </ul>
 * 
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>校准前需要连接标准信号源</li>
 *   <li>校准过程中不要切换档位或通道</li>
 *   <li>校准结果存储在Flash中，断电不丢失</li>
 * </ul>
 * 
 * @author zhuzh  // 作者：zhuzh
 * @version 1.0  // 版本号：1.0
 * @see Calibrate 校准抽象基类  // 参见：Calibrate类
 * @see HW_MHO38V1 MHO38 V1硬件操作类  // 参见：HW_MHO38V1类
 * @see CabteRegister 校准寄存器管理  // 参见：CabteRegister类
 */
public class MHO38v1_ChCapCalibrate extends Calibrate {  // 继承Calibrate抽象基类，实现电容校准
    
    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，传递校准类型</li>
     *   <li>初始化校准实例</li>
     * </ul>
     * 
     * @param calibrateType 校准类型（参见HW_MHO38V1中的校准状态常量）
     */
    public MHO38v1_ChCapCalibrate(int calibrateType) {  // 构造函数，接收校准类型参数
        super(calibrateType);  // 调用父类Calibrate的构造函数，传递校准类型

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
     * 705：波形幅度异常（已注释）
     * 其他：硬件错误或参数错误
     * </pre>
     */
    private int errcode;  // 错误码成员变量
    
    /**
     * 日志标签
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于日志输出时标识电容校准模块</li>
     *   <li>简化日志输出，便于日志过滤</li>
     * </ul>
     */
    private final String TAG = "ChCap";  // 日志标签，固定为"ChCap"
    
    /**
     * 完整日志标签
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>组合父类标签和当前标签</li>
     *   <li>格式："父类标签:ChCap"</li>
     * </ul>
     */
    private final String TAG1=TAG_PRI+":"+TAG;  // 完整日志标签，格式为"父类标签:ChCap"

    /**
     * 当前校准通道索引
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>指定当前校准的通道</li>
     *   <li>-1表示校准所有通道</li>
     *   <li>0-7表示校准指定通道</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <pre>
     * -1：校准所有通道
     * 0-7：校准指定通道（CH1-CH8）
     * </pre>
     */
    private volatile int ch=0;  // 当前校准通道索引，volatile保证多线程可见性，默认为0（CH1）
    
    /**
     * 校准档位电压值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>电容校准使用的档位电压值</li>
     *   <li>默认为500mV/div</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <pre>
     * 根据VerticalAxis定义的档位值
     * 默认：0.5V（500mV/div）
     * </pre>
     */
    private double vScaleVal = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_500mV);  // 校准档位电压值，默认为500mV/div

    /**
     * 测量计数器
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录当前已采集的波形数量</li>
     *   <li>用于多次测量取平均值</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <pre>
     * 0-6：采集阶段
     * >=6：计算平均值阶段
     * </pre>
     */
    private volatile int meatCnt=0;  // 测量计数器，volatile保证多线程可见性，初始为0
    
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
     * 0：延时等待步骤1
     * 1：延时等待步骤2
     * 2：迭代校准步骤
     * 3：保存结果步骤
     * </pre>
     */
    private volatile int step;  // 校准步骤，volatile保证多线程可见性
    
    /**
     * 档位索引
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>当前校准档位的索引值</li>
     *   <li>用于访问校准数据数组</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <pre>
     * 0-3：对应档位1-4
     * </pre>
     */
    private int dwIdx = 0;  // 档位索引，初始为0
    
    /**
     * 波形平均值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道波形前段和后段的平均值</li>
     *   <li>用于计算电容校准的差异值</li>
     * </ul>
     * 
     * <p><b>数组维度：</b>
     * <pre>
     * 第一维：段索引（0=前段，1=后段）
     * 第二维：通道索引（0-7）
     * </pre>
     */
    private volatile double average[][]={{0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0}};  // 波形平均值数组，初始化为0

    /**
     * 最小差异值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道历史最小的差异值</li>
     *   <li>用于二分法收敛判断</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <pre>
     * 初始值：Double.MAX_VALUE
     * 校准过程中逐渐减小
     * </pre>
     */
    private volatile double []minVal={Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE};  // 最小差异值数组，初始为最大值
    
    /**
     * 最优电容高值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道历史最优的电容高值</li>
     *   <li>当差异值最小时记录的电容高值</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <pre>
     * 0-0xFFFF：16位电容高值
     * </pre>
     */
    private volatile int [] dcpVal = {0,0,0,0,0,0,0,0};  // 最优电容高值数组，初始为0
    
    /**
     * 无改进计数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录每个通道连续无改进的次数</li>
     *   <li>用于判断是否收敛</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <pre>
     * 0-16：连续无改进次数
     * >16：判定为收敛
     * </pre>
     */
    private volatile int [] minNum = {0,0,0,0,0,0,0,0};  // 无改进计数数组，初始为0
    
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
     * 电容高值最大值常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>定义电容高值的最大值</li>
     *   <li>16位无符号整数最大值</li>
     * </ul>
     */
    private static final int CAP_MAX_VAL = 0xFFFF;  // 电容高值最大值：0xFFFF（65535）
    
    /**
     * 电容高值参考值常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>定义电容高值的初始参考值</li>
     *   <li>为最大值的一半，作为二分法的起点</li>
     * </ul>
     */
    private static final int CAP_REF_VAL = CAP_MAX_VAL/2;  // 电容高值参考值：0x7FFF（32767）

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
     * 1. 清零平均值数组
     * 2. 重置参考值为中间值
     * 3. 重置最小值为最大值
     * 4. 清零最优电容高值
     * 5. 清零无改进计数
     * 6. 清零测量计数
     * </pre>
     */
    //校准下一档初始化
    private void rstCalculate(){  // 重置计算变量方法
        for(int j=0; j< 2;j++) {  // 循环：遍历前段和后段
            for (int i = 0; i < channelNums; i++) {  // 循环：遍历所有通道
                average[j][i] =0;  // 清零平均值
                refVal[i] = CAP_REF_VAL;  // 重置参考值为中间值
                minVal[i] = Double.MAX_VALUE;  // 重置最小值为最大值
                dcpVal[i] = 0;  // 清零最优电容高值
                minNum[i] = 0;  // 清零无改进计数
            }  // 循环结束：通道
        }  // 循环结束：段
        meatCnt = 0;  // 清零测量计数
    }  // 方法结束

    /**
     * 初始化校准寄存器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的iniCalibrateReg方法</li>
     *   <li>初始化校准寄存器中的电容高值</li>
     * </ul>
     * 
     * <p><b>初始化内容：</b>
     * <pre>
     * 当前代码已注释，实际初始化在calibratePrepare中完成
     * </pre>
     */
    @Override  // 覆盖父类方法
    public void iniCalibrateReg(){  // 初始化校准寄存器方法
        if(ChannelFactory.isDynamicCh(ch)){  // 判断：当前通道是否为有效动态通道
            //cabteRegister.getChCapacitanceHigh()[ch] = 127;  // 已注释：设置电容高值为127
        }  // 判断结束
    }  // 方法结束

    /**
     * 设置校准参数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的setParam方法</li>
     *   <li>设置校准的通道和档位参数</li>
     * </ul>
     * 
     * <p><b>参数格式：</b>
     * <pre>
     * vol为double[]数组：
     *   第1个值：通道索引（-1表示所有通道，0-7表示指定通道）
     *   第2个值：档位电压值（V/div）
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
        vScaleVal = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_500mV);  // 默认档位为500mV/div

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
                }  // 判断结束
            }  // 判断结束
        }  // 判断结束
    }  // 方法结束

    /**
     * 参数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于返回校准参数</li>
     *   <li>存储通道索引和档位索引</li>
     * </ul>
     */
    int []param=new int[2];  // 参数数组，长度为2
    
    /**
     * 获取校准参数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getParam方法</li>
     *   <li>返回当前校准的参数</li>
     * </ul>
     * 
     * @return 参数数组，[通道索引, 档位索引]
     */
    @Override  // 覆盖父类方法
    public Object getParam() {  // 获取校准参数方法
        param[0] = ch;  // 设置通道索引
        //param[1] = dangwei;  // 已注释：设置档位ID
        param[1] = 0;  // 设置档位索引为0
        return param;  // 返回参数数组
    }  // 方法结束

    /**
     * 校准准备
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的calibratePrepare方法</li>
     *   <li>初始化校准环境和参数</li>
     *   <li>设置时基、触发、通道等参数</li>
     * </ul>
     * 
     * <p><b>初始化流程：</b>
     * <pre>
     * 1. 获取档位索引
     * 2. 设置时基为10μs/div
     * 3. 设置触发模式为普通触发
     * 4. 设置边沿触发为上升沿
     * 5. 设置触发电平为屏幕4格位置
     * 6. 设置触发源
     * 7. 设置通道参数（探头、档位、位置）
     * 8. 初始化电容高值为中间值
     * 9. 设置采样模式为普通采样
     * 10. 重置计算变量
     * 11. 初始化校准状态
     * </pre>
     */
    @Override  // 覆盖父类方法
    public void calibratePrepare() {  // 校准准备方法

        dwIdx = CabteRegister.getRatioIdx(Channel.RESISTANCE_1M,vScaleVal);  // 获取档位索引
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();  // 获取水平轴实例

        horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.TSI_10uS);  // 设置时基为10μs/div
        horizontalAxis.setTimePoseOfViewPix(ScopeBase.getWidth()/2-ScopeBase.getHorizonPerGridPixels()/2);  // 设置时间位置为屏幕中央
        TriggerFactory triggerFactory=TriggerFactory.getInstance();  // 获取触发器工厂实例
        triggerFactory.getTriggerCommon().setTriggerMode(TriggerCommon.TM_NORMAL);  // 设置触发模式为普通触发
        TriggerEdge triggerEdge=(TriggerEdge)(triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));  // 获取边沿触发器
        triggerEdge.setTriggerEdge(TriggerEdge.TET_ASC);  // 设置触发边沿为上升沿
        int level = ScopeBase.getVerticalPerGridPixels() * 4;  // 计算触发电平位置（屏幕4格）
        triggerEdge.getTriggerLevel().setPos(level);  // 设置触发电平
        ms_sleep(500);  // 延时500ms，等待触发稳定
        if(ChannelFactory.isDynamicCh(ch)){  // 判断：当前通道是否为有效动态通道
            triggerEdge.setTriggerSource(ch);  // 设置触发源为当前通道
        }else{  // 否则：通道无效
            triggerEdge.setTriggerSource(0);  // 设置触发源为通道1
            ch = -1;  // 设置为校准所有通道
        }  // 判断结束
//        int []chCapHigh = cabteRegister.getChCapacitanceHigh();  // 已注释：获取电容高值数组
        for(int i=0; i<channelNums; i++) {  // 循环：遍历所有通道
            channel[i].setProbeRate(1);  // 设置探头衰减比为1X
            channel[i].setVScaleVal(vScaleVal);  // 设置档位电压值
            channel[i].setPos(-ScopeBase.getVerticalPerGridPixels() * 4);  // 设置垂直位置为-4格
            if(ch == -1 || i == ch) {  // 判断：是否为需要校准的通道
//              chCapHigh[i] =  // 已注释：设置电容高值
                refVal[i] = CAP_REF_VAL;  // 设置参考值为中间值
                cabteRegister.setChCapacitanceHigh(i,dwIdx,CAP_REF_VAL);  // 设置电容高值为中间值
            }  // 判断结束
        }  // 循环结束
        Sample.getInstance().setSampleType(Sample.SAMPLE_TYPE_NORMAL);  // 设置采样模式为普通采样

        triggerEdge.getTriggerLevel().setPos(level);  // 再次设置触发电平（确保生效）
        ms_sleep(500);  // 延时500ms，等待参数稳定
        triggerEdge.getTriggerLevel().setPos(level);  // 第三次设置触发电平（确保生效）

        rstCalculate();  // 重置计算变量


        meatCnt = 0;  // 清零测量计数
        step = 0;  // 初始化校准步骤为0
        errcode = 0;  // 清零错误码
        delaySet(3);  // 设置延时为3秒
        resultString.add("<<<<<<<<<< chCapCalibrate start ......");  // 添加校准开始日志
        Log.i(TAG1,"<<<<<<<<<< chCapCalibrate start ......");  // 输出校准开始日志
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
    public boolean checkParam(){  // 检查参数方法
        boolean bChange = false;  // 参数变化标志，初始为false
        int chidx = ch;  // 获取当前通道索引
        if(ch < 0){  // 判断：通道索引是否为负
            chidx = 0;  // 使用通道0进行检查
        }  // 判断结束

        if(ChannelFactory.isDynamicCh(chidx)) {  // 判断：通道是否为有效动态通道
            Channel channel1 = ChannelFactory.getDynamicChannel(chidx);  // 获取动态通道对象
            int p = ScopeBase.getVerticalPerGridPixels() * 4;  // 计算目标位置（4格）

            if(Math.abs(channel1.getPos() + p) > 1){  // 判断：通道位置是否偏离目标
                channel1.setPos(-p);  // 修正通道位置
                bChange = true;  // 设置参数变化标志
            }  // 判断结束
            TriggerFactory triggerFactory = TriggerFactory.getInstance();  // 获取触发器工厂实例

            TriggerEdge triggerEdge = (TriggerEdge) (triggerFactory.getTrigger(Trigger.TRIG_TYPE_EDGE));  // 获取边沿触发器
            if (!triggerEdge.isTriggerSource(chidx)) {  // 判断：触发源是否正确
                triggerEdge.setTriggerSource(chidx);  // 修正触发源
                bChange = true;  // 设置参数变化标志
            }  // 判断结束

            if (Math.abs(triggerEdge.getTriggerLevel().getPos() - p) > 1) {  // 判断：触发电平是否偏离目标
                triggerEdge.getTriggerLevel().setPos(p);  // 修正触发电平
                bChange = true;  // 设置参数变化标志
            }  // 判断结束
        }  // 判断结束
        if(bChange){  // 判断：参数是否有变化
            rstCalculate();  // 重置计算变量
            delaySet(3);  // 设置延时为3秒
        }  // 判断结束
        return bChange;  // 返回参数变化标志
    }  // 方法结束
    
    /**
     * 电容高值参考值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道的电容高值调整步长</li>
     *   <li>用于二分法调整</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <pre>
     * 初始值：CAP_REF_VAL（0x7FFF）
     * 二分过程中逐渐减半
     * 最小值：1
     * </pre>
     */
    private volatile int [] refVal = {CAP_REF_VAL,CAP_REF_VAL,CAP_REF_VAL,CAP_REF_VAL,CAP_REF_VAL,CAP_REF_VAL,CAP_REF_VAL,CAP_REF_VAL};  // 电容高值参考值数组，初始为中间值
    
    /**
     * 标准值常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>定义波形的标准幅度值</li>
     *   <li>用于验证波形是否正常</li>
     * </ul>
     */
    private static final int STD_VAL = ScopeBase.getVerticalPerGridPixels() * 8;  // 标准值：屏幕8格像素数
    
    /**
     * 执行校准
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的onCalibrate方法</li>
     *   <li>执行电容校准的核心逻辑</li>
     *   <li>通过迭代调整电容高值实现校准</li>
     * </ul>
     * 
     * <p><b>校准算法：</b>
     * <pre>
     * 1. 等待硬件操作完成
     * 2. 检查参数是否正确
     * 3. step 0：延时等待
     * 4. step 1：延时等待
     * 5. step 2：迭代校准
     *    a. 采集波形数据
     *    b. 计算波形前段和后段的平均值
     *    c. 多次测量取平均（6次）
     *    d. 计算差异值（val = average[1] - average[0]）
     *    e. 根据差异调整电容高值：
     *       - val > 0.05：增加电容高值
     *       - val < -0.05：减少电容高值
     *       - |val| < 0.05：校准完成
     *    f. 使用二分法逐步逼近
     *    g. 判断收敛条件
     * 6. step 3：保存校准结果
     * </pre>
     * 
     * @return true表示校准完成，false表示校准进行中
     */
    @Override  // 覆盖父类方法
    public boolean onCalibrate() {  // 执行校准方法
        //等待硬件操作完成

        if (!isFinishedAction())  // 判断：硬件操作是否完成
            return false;  // 硬件操作未完成，继续等待
        if(checkParam()){  // 判断：参数是否有变化
            return false;  // 参数有变化，重新开始校准
        }  // 判断结束
        if(step == 0){  // 判断：是否为步骤0
            delaySet(2);  // 设置延时为2秒
            step++;  // 步骤加1
            return false;  // 返回false，继续校准
        }  // 判断结束
        if(step == 1){  // 判断：是否为步骤1
            delaySet(0);  // 设置延时为0
            step++;  // 步骤加1
            meatCnt = 0;  // 清零测量计数
            return false;  // 返回false，继续校准
        }  // 判断结束
        if(meatCnt == 0){  // 判断：测量计数是否为0
            delaySet(0);  // 设置延时为0
        }  // 判断结束


//        int []chCapHigh = cabteRegister.getChCapacitanceHigh();  // 已注释：获取电容高值数组

        HwConfig hwConfig = HwConfig.getInstance();  // 获取硬件配置实例
        WaveData waveData;  // 波形数据对象
        int N;  // 波形长度
        int gridCnt = ScopeBase.getHorizonGridCnt();  // 获取水平格数
        int xx = ScopeBase.getHorizonPerGridPixels();  // 获取每格像素数
        //获取每个通道的波形平均值
        for (int i = 0; i < channelNums; i++) {  // 循环：遍历所有通道

            waveData = (WaveData) getWave(i);  // 获取通道i的波形数据
            if (waveData == null || (N = waveData.getWaveLength()) < 10)  // 判断：波形数据是否有效
                return false;  // 波形数据无效，继续等待
            int len1 = N/gridCnt;  // 计算每格的采样点数

            int n = len1/xx * 2;  // 计算计算窗口大小
            average[0][i] += (double) MathNative.calcSum(waveData.getByteBuffer(), len1 / 2 + n, n) / n;  // 计算前段平均值并累加
            average[1][i] += (double) MathNative.calcSum(waveData.getByteBuffer(),  len1 * 10, n) / n;  // 计算后段平均值并累加

        }  // 循环结束
        meatCnt++;  // 测量计数加1


        if(meatCnt >= 6) {  // 判断：是否已采集足够次数
            for (int i = 0; i < channelNums; i++) {  // 循环：遍历所有通道

                average[0][i] /= meatCnt;  // 计算前段平均值
                average[1][i] /= meatCnt;  // 计算后段平均值

                average[0][i] *= hwConfig.getWavFactor();  // 应用波形因子
                average[1][i] *= hwConfig.getWavFactor();  // 应用波形因子

                Log.i(TAG, "ch:" + ch + ",step:" + step +",====average: " + average[0][i] + "," + average[1][i] + ",STD_VAL:" +STD_VAL);  // 输出平均值日志
//                if(ch == i || ch == -1){  // 已注释：判断是否为校准通道
//                    if(Math.abs(average[1][i] - STD_VAL)/STD_VAL > 0.25){  // 已注释：判断波形幅度是否异常
//                        errcode = 705;  // 已注释：设置错误码
//                        return true;  // 已注释：返回true，校准失败
//                    }  // 已注释：判断结束
//                }  // 已注释：判断结束
            }  // 循环结束
        }else {  // 否则：采集次数不足
            return false;  // 返回false，继续采集
        }  // 判断结束
        meatCnt = 0;  // 清零测量计数
        if(step == 2){  // 判断：是否为步骤2（迭代校准步骤）
            double TVAL = 0.05;  // 定义收敛阈值
            boolean [] bOk = {false,false,false,false,false,false,false,false};  // 通道校准完成标志数组
            for (int i = 0; i < channelNums; i++) {  // 循环：遍历所有通道
                if (ch == i || ch == -1) {  // 判断：是否为需要校准的通道
//                    if(!bOk[i])  // 已注释：判断通道是否未完成校准
                    {  // 代码块开始
                        double val = average[1][i] - average[0][i];  // 计算差异值

                        if (Math.abs(val) < minVal[i]) {  // 判断：差异值是否为历史最小
                            minVal[i] = Math.abs(val);  // 更新最小差异值
                            dcpVal[i] = cabteRegister.getChCapacitanceHigh(i,dwIdx);//chCapHigh[i];  // 记录当前电容高值为最优值
                            minNum[i] = 0;  // 清零无改进计数
                            Logger.d(TAG,"ch:" + i + ",min:" +minVal[i] + ",cap:" + dcpVal[i]);  // 输出日志
                        }  // 判断结束
                        refVal[i] /= 2;  // 二分法：参考值减半
                        if (refVal[i] == 0) {  // 判断：参考值是否为0
                            refVal[i] = 1;  // 设置参考值为最小值1
                            minNum[i]++;  // 无改进计数加1
                        }  // 判断结束
                        if (minNum[i] > 16) {  // 判断：无改进次数是否超过阈值
//                            chCapHigh[i] = dcpVal[i];  // 已注释：设置电容高值为最优值
                            cabteRegister.setChCapacitanceHigh(i,dwIdx,dcpVal[i]);  // 设置电容高值为最优值
                            bOk[i] = true;  // 标记通道校准完成
                        } else if (Math.abs(val) > TVAL) {  // 判断：差异值是否超过阈值
                            int xvv = cabteRegister.getChCapacitanceHigh(i,dwIdx);  // 获取当前电容高值
                            if (val > TVAL) {  // 判断：差异值是否为正
                                xvv += refVal[i];  // 增加电容高值
                                if (xvv > CAP_MAX_VAL) {  // 判断：是否超过最大值
                                    xvv = 0;  // 回绕到最小值
                                }  // 判断结束
                            } else if (val < -TVAL) {  // 判断：差异值是否为负
                                xvv -= refVal[i];  // 减少电容高值
                                if (xvv < 0) {  // 判断：是否小于最小值
                                    xvv = CAP_MAX_VAL;  // 回绕到最大值
                                }  // 判断结束
                            }  // 判断结束
                            cabteRegister.setChCapacitanceHigh(i,dwIdx,xvv);  // 设置新的电容高值
                        }else{  // 否则：差异值在阈值范围内
                            bOk[i] = true;  // 标记通道校准完成
                        }  // 判断结束
                        Log.d(TAG, "chCapHigh[" + i + "] = " + cabteRegister.getChCapacitanceHigh(i,dwIdx));  // 输出电容高值日志
                    }  // 代码块结束
                    average[0][i] = 0;  // 清零前段平均值
                    average[1][i] = 0;  // 清零后段平均值
                }  // 判断结束
            }  // 循环结束
            boolean bx = false;  // 所有通道校准完成标志
            if(ch == -1){  // 判断：是否校准所有通道
                int i =0;  // 初始化循环变量
                for(;i<channelNums;i++){  // 循环：遍历所有通道
                    if(!bOk[i]){  // 判断：是否有通道未完成
                       break;  // 有通道未完成，跳出循环
                    }  // 判断结束
                }  // 循环结束
                bx = (i == channelNums);  // 判断是否所有通道都完成
            }else{  // 否则：校准指定通道
                bx = bOk[ch];  // 获取指定通道的完成状态
            }  // 判断结束
            if(bx){  // 判断：是否所有需要校准的通道都完成
                step++;  // 步骤加1，进入下一阶段
            }  // 判断结束
            Sample.getInstance().setSampleType(Sample.SAMPLE_TYPE_NORMAL);  // 设置采样模式为普通采样
            delaySet(2);  // 设置延时为2秒
        }  // 判断结束

        if (step != 3) {  // 判断：是否未到达步骤3
            return false;  // 返回false，继续校准
        }  // 判断结束


        for (int i = 0; i < channelNums; i++){  // 循环：遍历所有通道

            resultString.add("chCapHigh[" + i + "] = " + cabteRegister.getChCapacitanceHigh(i,dwIdx));  // 添加电容高值结果

        }  // 循环结束

        resultString.add("chCapCalibrate Calibrate end >>>>>>>>>>>");  // 添加校准结束日志
        if(errcode == 0){  // 判断：是否无错误
            param[0] = ch;  // 设置参数：通道索引
            param[1] = dwIdx;  // 设置参数：档位索引
            if(ch < 0){  // 判断：是否校准所有通道
                for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
                    cabteRegister.setCalibrationState(HW_MHO38V1.CHCAP_CALIBRATE_CH1 + i ,true);  // 设置所有通道的电容校准状态为已完成
                }  // 循环结束
            }else{  // 否则：校准指定通道
                cabteRegister.setCalibrationState(HW_MHO38V1.CHCAP_CALIBRATE_CH1 + ch ,true);  // 设置指定通道的电容校准状态为已完成
            }  // 判断结束
        }  // 判断结束
        return true;  // 返回true，校准完成
    }  // 方法结束
}  // 类结束
