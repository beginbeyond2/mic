package com.micsig.tbook.scope.Calibrate.MHO38v1;  // 包声明：MHO38 V1示波器校准模块

import android.os.SystemClock;  // 导入：Android系统时钟类，用于获取系统运行时间
import android.util.Log;  // 导入：Android日志工具，用于输出调试信息

import com.micsig.tbook.scope.Action.ChannelHardw;  // 导入：通道硬件操作类，用于硬件控制
import com.micsig.tbook.scope.Calibrate.CabteRegister;  // 导入：校准寄存器管理类，用于管理校准数据
import com.micsig.tbook.scope.Calibrate.Calibrate;  // 导入：校准抽象基类，定义校准流程接口
import com.micsig.tbook.scope.Calibrate.HW;  // 导入：硬件操作抽象基类
import com.micsig.tbook.scope.Calibrate.HwConfig;  // 导入：硬件配置类，用于获取硬件参数
import com.micsig.tbook.scope.Calibrate.MHO68v2.HW_MHO68V2;  // 导入：MHO68 V2硬件操作类
import com.micsig.tbook.scope.Data.WaveData;  // 导入：波形数据类，用于存储波形数据
import com.micsig.tbook.scope.ScopeMessage;  // 导入：示波器消息类，用于消息队列管理
import com.micsig.tbook.scope.Trigger.Trigger;  // 导入：触发器基类，定义触发类型
import com.micsig.tbook.scope.Trigger.TriggerEdge;  // 导入：边沿触发器类，用于边沿触发控制
import com.micsig.tbook.scope.Trigger.TriggerFactory;  // 导入：触发器工厂类，用于创建触发器
import com.micsig.tbook.scope.channel.Channel;  // 导入：通道类，定义通道属性和操作
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入：通道工厂类，用于通道管理
import com.micsig.tbook.scope.fpga.FPGACommand;  // 导入：FPGA命令类，用于发送FPGA控制命令
import com.micsig.tbook.scope.math.MathNative;  // 导入：数学运算本地方法类，用于波形计算
import com.micsig.tbook.scope.vertical.VerticalAxis;  // 导入：垂直轴类，用于垂直档位转换

/**
 * MHO38 V1示波器零点校准扩展实现类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate.MHO38v1（MHO38 V1示波器校准模块）</li>
 *   <li>架构层级：校准业务层 - 零点校准具体实现</li>
 *   <li>设计模式：继承Calibrate抽象基类，实现零点校准流程</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现MHO38 V1示波器的零点校准</li>
 *   <li>通过调整DA输出值校准通道零点</li>
 *   <li>支持多档位、多PGA值的零点校准</li>
 *   <li>自动切换档位和PGA值完成全量程校准</li>
 * </ul>
 * 
 * <p><b>校准原理：</b>
 * <pre>
 * 零点校准目标：
 *   使通道在无信号输入时，波形平均值接近0
 * 
 * 校准方法：
 *   1. 设置通道垂直位置为0
 *   2. 采集波形数据
 *   3. 计算波形平均值
 *   4. 根据平均值调整DA输出值：
 *      - average > 0：减小DA值
 *      - average < 0：增大DA值
 *   5. 迭代直到平均值接近0
 * 
 * DA值范围：0 ~ 65535（16位）
 * 中间值：32767（0x7FFF）
 * </pre>
 * 
 * <p><b>校准流程：</b>
 * <pre>
 * 1. calibratePrepare()：初始化校准参数
 *    ├── 设置档位为1mV/div
 *    ├── 设置PGA值为0x400
 *    ├── 设置通道位置为0
 *    └── 设置零点值为0
 * 
 * 2. onCalibrate()：执行校准
 *    ├── 采集波形数据
 *    ├── 计算波形平均值
 *    ├── 检测是否有外部信号输入
 *    ├── 状态机控制校准流程
 *    │   ├── state 0：寻找最佳值
 *    │   └── state 1：验证收敛
 *    ├── 计算DA调整量
 *    └── 更新零点值
 * 
 * 3. DoStateMachine()：档位切换
 *    ├── PGA值递增（0x400 → 0x40A → 0x410 → 0x41A）
 *    ├── 档位切换（档位1 → 档位2 → 档位3 → 档位4）
 *    └── 判断是否完成所有档位
 * 
 * 4. 收敛条件：
 *    - |average| < 1像素，或
 *    - 连续3次无改进
 * </pre>
 * 
 * <p><b>PGA值和档位映射：</b>
 * <pre>
 * PGA值格式：0x4XY
 *   - X：增益模式（0=低增益，1=高增益）
 *   - Y：PGA步进值（0-A）
 * 
 * 档位映射：
 *   - 档位1：PGA 0x400, 0x410
 *   - 档位2：PGA 0x400, 0x410
 *   - 档位3：PGA 0x400, 0x410
 *   - 档位4：PGA 0x400, 0x410
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：Calibrate（校准抽象基类）</li>
 *   <li>依赖：CabteRegister（校准寄存器管理）</li>
 *   <li>依赖：HW_MHO38V1（硬件操作类，校准状态定义）</li>
 *   <li>依赖：FPGACommand（FPGA命令管理）</li>
 *   <li>依赖：ChannelHardw（通道硬件操作）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>示波器出厂校准时校准零点</li>
 *   <li>周期性校准维护时校准零点</li>
 *   <li>用户手动触发零点校准</li>
 * </ul>
 * 
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>校准前需要断开所有信号输入</li>
 *   <li>校准过程中不要切换档位或通道</li>
 *   <li>校准结果存储在Flash中，断电不丢失</li>
 *   <li>每个通道每个档位每个PGA值都有一个零点值</li>
 * </ul>
 * 
 * @author zhuzh  // 作者：zhuzh
 * @version 1.0  // 版本号：1.0
 * @see Calibrate 校准抽象基类  // 参见：Calibrate类
 * @see HW_MHO38V1 MHO38 V1硬件操作类  // 参见：HW_MHO38V1类
 * @see CabteRegister 校准寄存器管理  // 参见：CabteRegister类
 */
/**
 * Created by zhuzh on 2018-6-29.
 * 零点校准
 *
 */
public class MHO38v1_ZeroCalibrateEx extends Calibrate {  // 继承Calibrate抽象基类，实现零点校准
    
    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，传递校准类型</li>
     * </ul>
     * 
     * @param calibrateType 校准类型（参见HW_MHO38V1中的校准状态常量）
     */
    public MHO38v1_ZeroCalibrateEx(int calibrateType) {  // 构造函数，接收校准类型参数
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
     * 初始值：Float.MAX_VALUE
     * 校准过程中逐渐减小
     * 收敛值：< 1像素
     * </pre>
     */
    private float []best_value=new float[ChannelFactory.CH_CNT];  // 最佳误差值数组，每个通道一个值
    
    /**
     * 最大电压值数组（已注释）
     */
    //private int []maxVol=new int[4];  // 已注释：最大电压值数组
    
    /**
     * 备份系数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道最佳校准时的零点值</li>
     *   <li>用于校准完成后恢复最佳零点</li>
     * </ul>
     */
    private float []bakCoef=new float[ChannelFactory.CH_CNT];  // 备份系数数组，每个通道一个值
    
    /**
     * 状态数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储每个通道的校准状态</li>
     *   <li>0：寻找最佳值阶段</li>
     *   <li>1：验证收敛阶段</li>
     * </ul>
     */
    private int []state=new int[ChannelFactory.CH_CNT];  // 状态数组，每个通道一个值

    /**
     * 尝试次数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录每个通道校准迭代的次数</li>
     *   <li>用于判断是否收敛</li>
     * </ul>
     */
    private int tryNum[]=new int[ChannelFactory.CH_CNT];  // 尝试次数数组，每个通道一个值
    
    /**
     * 尝试次数数组（备用）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录每个通道校准迭代的次数（备用）</li>
     *   <li>用于判断是否收敛</li>
     * </ul>
     */
    private int tryChiShu[]=new int[ChannelFactory.CH_CNT];  // 尝试次数数组（备用），每个通道一个值
    
    /**
     * 完成标志数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>标识每个通道是否完成校准</li>
     * </ul>
     */
    private boolean finished[]=new boolean[ChannelFactory.CH_CNT];  // 完成标志数组，每个通道一个值
    
    /**
     * 档位索引
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>当前校准档位的索引值</li>
     *   <li>范围：0-3，对应档位1-4</li>
     * </ul>
     */
    private int dwIdx = 0;  // 档位索引，初始为0
    
    /**
     * 档位ID
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>当前校准档位的ID</li>
     *   <li>默认为1mV/div</li>
     * </ul>
     */
    private int dangwei = VerticalAxis.DANG_1mV;  // 档位ID，默认为1mV/div

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
     * 103：平均值超标（大于5像素）
     * 104：零点系数验证失败
     * 105：检测到外部信号输入（已注释）
     * </pre>
     */
    private int errcode;  // 错误码成员变量
    
    /**
     * 日志标签
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于日志输出时标识零点校准扩展模块</li>
     *   <li>简化日志输出，便于日志过滤</li>
     * </ul>
     */
    private final String TAG = "ZeroEx";  // 日志标签，固定为"ZeroEx"
    
    /**
     * 完整日志标签
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>组合父类标签和当前标签</li>
     *   <li>格式："父类标签:ZeroEx"</li>
     * </ul>
     */
    private final String TAG1=TAG_PRI+":"+TAG;  // 完整日志标签，格式为"父类标签:ZeroEx"
    
    /**
     * 通道硬件操作对象
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于设置通道PGA增益等硬件操作</li>
     * </ul>
     */
    ChannelHardw channelHardw = ChannelHardw.getInstance();  // 获取通道硬件操作实例
    
    /**
     * FPGA命令对象
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于发送FPGA控制命令</li>
     * </ul>
     */
    FPGACommand fpgaCommand = FPGACommand.getInstance();  // 获取FPGA命令实例


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
     * 2. 清零备份系数
     * 3. 清零状态数组
     * 4. 清零尝试次数数组
     * 5. 根据通道开关状态设置完成标志
     * </pre>
     */
    //校准下一档初始化
    private void rstCalculate(){  // 重置计算变量方法
        for(int i=0; i<ChannelFactory.CH_CNT; i++) {  // 循环：遍历所有通道
            best_value[i] = Float.MAX_VALUE;  // 重置最佳误差值为最大值
            bakCoef[i] = 0;  // 清零备份系数
            state[i] = 0;  // 清零状态
            tryNum[i] = 0;  // 清零尝试次数
            tryChiShu[i] = 0;  // 清零尝试次数（备用）
            if(channel[i].isOpen())  // 判断：通道是否打开
                finished[i] = false;  // 通道打开，设置完成标志为false
            else  // 否则：通道关闭
                finished[i] = true;  // 通道关闭，设置完成标志为true

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
     * 恢复校准配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的restoreCalibrateConfig方法</li>
     *   <li>校准结束后恢复通道配置</li>
     * </ul>
     */
    @Override  // 覆盖父类方法
    protected void restoreCalibrateConfig() {  // 恢复校准配置方法
        super.restoreCalibrateConfig();  // 调用父类的恢复方法
        for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
            channel[i].setVScaleId(VerticalAxis.DANG_1V);  // 设置通道档位为1V/div
        }  // 循环结束
    }  // 方法结束

    /**
     * PGA值成员变量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>当前校准的PGA值</li>
     *   <li>格式：0x4XY</li>
     *   <li>X：增益模式（0=低增益，1=高增益）</li>
     *   <li>Y：PGA步进值（0-A）</li>
     * </ul>
     */
    int pgaVal = 0x400;  // PGA值，初始为0x400（低增益模式，步进0）
    
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
        fpgaCommand.cmdDevice(300);  // 发送设备命令300
        int [] res = {val,val,val,val,val,val,val,val};  // 初始化PGA值数组，所有通道相同
        channelHardw.set_ch_AD8370Gain(res);  // 设置所有通道的AD8370增益
        fpgaCommand.setPgaVal(val);  // 设置FPGA的PGA值
        fpgaCommand.cmdDevice(300);  // 发送设备命令300
    }  // 方法结束


    /**
     * 校准准备
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的calibratePrepare方法</li>
     *   <li>初始化校准环境和参数</li>
     *   <li>设置通道、触发等参数</li>
     * </ul>
     * 
     * <p><b>初始化流程：</b>
     * <pre>
     * 1. 初始化PGA值为0x400
     * 2. 设置档位为档位1
     * 3. 设置通道位置为0
     * 4. 设置触发电平（特意不让触发）
     * 5. 设置零点值为0
     * 6. 等待消息队列清空
     * 7. 刷新PGA配置
     * 8. 重置计算变量
     * </pre>
     */
    @Override  // 覆盖父类方法
    public void calibratePrepare() {  // 校准准备方法
        pgaVal = 0x400;  // 初始化PGA值为0x400（低增益模式，步进0）
        dwIdx = HW_MHO38V1.RATIO_DANG_1;  // 设置档位索引为档位1
        dangwei = CabteRegister.getRatioIdx2Dang(0,dwIdx);  // 获取档位ID


        for(int i=0; i<channelNums; i++) {  // 循环：遍历所有通道

            channel[i].setPos(0);  // 通道纵向位置归零
            channel[i].setVScaleId(dangwei);  // 设置通道档位
            TriggerEdge triggerEdge = (TriggerEdge)TriggerFactory.getTriggerObj(Trigger.TRIG_TYPE_EDGE);  // 获取边沿触发器
            triggerEdge.getTriggerLevel(i).setPos(-120);  // 设置触发电平为-120（特意不让触发）
            channel[i].setZero(0);  // 设置零点值为0
        }  // 循环结束
        do {  // 循环：等待消息队列清空
            ms_sleep(100);  // 延时100ms
        }while (!ScopeMessage.isEmpty());  // 判断：消息队列是否为空
        ms_sleep(300);  // 延时300ms，等待硬件稳定
        pgaTs = 0;  // 清零PGA时间戳
        checkParamEx();  // 检查参数并刷新PGA
        errcode = 0;  // 清零错误码
        sMax = 0;  // 清零最大幅度值
        resultString.add("<<<<<<<<<< ZeroCalibrate start ......");  // 添加校准开始日志
        Log.i(TAG1, "<<<<<<<<<< ZeroCalibrate start ......");  // 输出校准开始日志
    }  // 方法结束

    /**
     * PGA刷新时间戳
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录上次刷新PGA的时间</li>
     *   <li>用于定时刷新PGA配置</li>
     * </ul>
     */
    private long pgaTs = 0;  // PGA刷新时间戳，初始为0
    
    /**
     * 检查参数并刷新PGA
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查距离上次刷新PGA是否超过60秒</li>
     *   <li>如果超过，重新刷新PGA配置</li>
     * </ul>
     * 
     * @return true表示已刷新PGA，false表示未刷新
     */
    public boolean checkParamEx(){  // 检查参数并刷新PGA方法
        long ts = SystemClock.elapsedRealtime();  // 获取当前系统运行时间
        if(ts - pgaTs > 60*1000){  // 判断：距离上次刷新是否超过60秒
            if(pgaTs != 0) {  // 判断：是否不是首次刷新
                Log.e(TAG, "ts - pgaTs:" + (ts - pgaTs));  // 输出错误日志
            }  // 判断结束
            fpgaCommand.writeAD_gain(0,0,0,0);  // 写入ADC满量程值（ADC0）
            fpgaCommand.writeAD_gain(0,0,1,0);  // 写入ADC满量程值（ADC1）
            fpgaCommand.writeAD_gain(1,0,0,0);  // 写入ADC满量程值（ADC2）
            fpgaCommand.writeAD_gain(1,0,1,0);  // 写入ADC满量程值（ADC3）
            setPGAVal(pgaVal);  // 设置PGA增益值
            for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
                channel[i].setPos(0);  // 设置通道位置为0
            }  // 循环结束
            rstCalculate();  // 重置计算变量
            pgaTs = ts;  // 更新PGA刷新时间戳
            return true;  // 返回true，表示已刷新
        }  // 判断结束
        return false;  // 返回false，表示未刷新
    }  // 方法结束

    /**
     * 最大幅度值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录所有通道波形幅度的最大值</li>
     *   <li>用于检测是否有外部信号输入</li>
     * </ul>
     */
    int sMax = 0;  // 最大幅度值，初始为0

    /**
     * 执行校准
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的onCalibrate方法</li>
     *   <li>执行零点校准的核心逻辑</li>
     *   <li>通过迭代调整DA输出值实现校准</li>
     * </ul>
     * 
     * <p><b>校准算法：</b>
     * <pre>
     * 1. 等待硬件操作完成
     * 2. 检查是否需要刷新PGA
     * 3. 采集波形数据
     * 4. 计算波形平均值
     * 5. 检测是否有外部信号输入
     * 6. 状态机控制校准流程：
     *    - state 0：寻找最佳值
     *    - state 1：验证收敛
     * 7. 计算DA调整量
     * 8. 更新零点值
     * 9. 判断是否完成所有档位校准
     * </pre>
     * 
     * @return true表示校准完成，false表示校准进行中
     */
    @Override  // 覆盖父类方法
    public boolean onCalibrate() {  // 执行校准方法
        //等待硬件操作完成
        if(!isFinishedAction())  // 判断：硬件操作是否完成
            return false;  // 硬件操作未完成，继续等待

        if(checkParamEx()){  // 判断：是否需要刷新PGA
            delaySet(3);  // 设置延时为3秒
        }else{  // 否则：不需要刷新PGA
            delaySet(0);  // 设置延时为0
        }  // 判断结束

        float average[]={0,0,0,0,0,0,0,0};  // 波形平均值数组，每个通道一个值
        long sum;  // 波形求和变量
        WaveData waveData = null;  // 波形数据对象
        int N;  // 波形长度

        HwConfig hwConfig = HwConfig.getInstance();  // 获取硬件配置实例
        //获取每个通道的波形平均值
        for(int i=0; i<channelNums; i++){  // 循环：遍历所有通道
            if(channel[i].isOpen()) {  // 判断：通道是否打开
                waveData = (WaveData) getWave(i);  // 获取通道i的波形数据
                if(waveData == null || (N = waveData.getWaveLength()) < 10)  // 判断：波形数据是否有效
                    return false;  // 波形数据无效，继续等待
                sum = MathNative.calcSum(waveData.getByteBuffer());  // 计算波形数据和

                average[i] = (float) (hwConfig.getWavFactor() * sum / N);  // 计算波形平均值（应用波形因子）

                int max1 = MathNative.calcMax(waveData.getByteBuffer());  // 计算波形最大值
                int min1 = MathNative.calcMin(waveData.getByteBuffer());  // 计算波形最小值
                int ss = Math.abs(max1 - min1);  // 计算波形幅度
                if(ss > sMax){  // 判断：是否为最大幅度
                    sMax = ss;  // 更新最大幅度值
                }  // 判断结束
                if(ss * hwConfig.getWavFactor() > 300){  // 判断：波形幅度是否过大（有外部信号）
                    Log.i(TAG1, "ch:" + i + ",max="+max1+", min="+min1);  // 输出日志
                    Log.i(TAG1, "ch"+(i+1)+"有外接信号输入，校准结束！");  // 输出日志：检测到外部信号
//                    errcode = 105;  // 已注释：设置错误码
//                    String str1 = "ZeroCalibrate error: ch"+(i+1)+"可能通道探头没有拔出，或者从外部输入了信号";  // 已注释：错误信息
//                    Log.e(TAG1, str1);  // 已注释：输出错误日志
//                    resultString.add(str1);  // 已注释：添加错误信息
//                    return true;  // 已注释：返回true，校准失败
                }  // 判断结束

            }  // 判断结束
        }  // 循环结束

        for(int i=0; i<channelNums; i++) {  // 循环：遍历所有通道
            if(channel[i].isOpen()) {  // 判断：通道是否打开
                float volDA_perGrid = (float) cabteRegister.vol_ChannelCoef_defaultEx(0,CabteRegister.getRatioIdx2Dang(0,dwIdx),pgaVal);  // 计算每格电压对应的DA值

                float averageVol=average[i];  // 获取波形平均值
                //状态机控制流程
                switch (state[i]) {  // 根据状态执行不同逻辑
                    case 0: {  // 状态0：寻找最佳值
                        int fV = 2;  // 初始化阈值
                        if (volDA_perGrid < 0.25) {  // 判断：DA值是否很小
                            fV = 5;  // 增大阈值
                        }  // 判断结束
                        if (Math.abs(averageVol) < Math.abs(best_value[i])) {  // 判断：误差是否为历史最小
                            //记录更佳值
                            best_value[i] = averageVol;  // 更新最佳误差值
                            bakCoef[i] = cabteRegister.getChannelZero(i, dwIdx, pgaVal & 0xFF);  // 备份当前零点值
                            Log.d(TAG, "ch:" + i + ",averageVol:" + averageVol + ",coef:" + bakCoef[i]);  // 输出调试日志

                        } else if (Math.abs(averageVol) > fV) {  // 判断：误差是否过大
                            //离结果差太多，继续


                        } else {  // 否则：误差在阈值范围内
                            //没有更佳化，说明可能到达极限，进入下一步
                            state[i]++;  // 状态加1，进入状态1
                            tryNum[i] = 0;  // 清零尝试次数

                        }  // 判断结束
                    }  // 代码块结束
                        break;  // 跳出switch
                    case 1:  // 状态1：验证收敛
                        if (Math.abs(averageVol) < Math.abs(best_value[i])) {  // 判断：误差是否为历史最小
                            //找到更佳值，说明没有到达极限，回到第1步
                            best_value[i] = averageVol;  // 更新最佳误差值
                            bakCoef[i] = cabteRegister.getChannelZero(i,dwIdx,pgaVal & 0xFF);  // 备份当前零点值
                            Log.d(TAG,"ch:" + i +",averageVol:" + averageVol + ",coef:" + bakCoef[i]);  // 输出调试日志
                            state[i] = 0;  // 回到状态0
                            finished[i] = false;  // 设置完成标志为false

                        } else {  // 否则：没有找到更佳值
                            int chi = 4;  // 设置尝试次数阈值

                            if (++tryNum[i] > chi) {  // 判断：尝试次数是否超过阈值
                                if(!finished[i]) {  // 判断：通道是否未完成
                                    tryChiShu[i]++;  // 尝试次数（备用）加1
                                    if(Math.abs(best_value[i]) < 1 || tryChiShu[i] > 3) {  // 判断：误差是否小于1像素或尝试次数超过3次
                                        finished[i] = true;  // 标记通道完成
                                    }  // 判断结束
                                    else {  // 否则：继续尝试
                                        tryNum[i] = 0;  // 清零尝试次数，再来一次
                                    }  // 判断结束
                                }  // 判断结束
                            }  // 判断结束
                        }  // 判断结束
                        break;  // 跳出switch
                }  // switch结束




                float fx1 = Math.abs(averageVol);  // 计算误差绝对值（像素）
                float zero = averageVol*volDA_perGrid;  // 计算误差的DA值
                if(zero > 300){  // 判断：DA调整量是否过大
                    zero = 150;  // 限制DA调整量
                }  // 判断结束

                if(fx1 < 2){  // 判断：误差是否小于2像素
                    zero /= 4;  // 精细调节：调整量除以4
                }else if(fx1 < 5)  // 判断：误差是否小于5像素
                    zero /= 2;  // 中等调节：调整量除以2

                //当调节值小于0.2像素时，每次固定调节0.1像素
                if(Math.abs(zero) < Math.abs(volDA_perGrid/5))  // 判断：调整量是否太小
                {  // 代码块开始
                    zero = Math.abs(volDA_perGrid/10);  // 固定调整量为0.1像素对应的DA值
                    if(fx1 < 0) zero = -zero;  // 判断：误差方向，设置调整方向
                    //这里zero值可能小于1，但是不用担心。虽然DA的精度为1，但是零点还是采用浮点数。
                    //在小档位时，可能需要移动好几个像素DA值才会修改1。
                    //处理方法是，FPGA增加偏移量补偿寄存器
                }  // 代码块结束
                float vv = cabteRegister.getChannelZero(i,dwIdx,pgaVal & 0xFF);  // 获取当前零点值
                //计算出新的零点

                zero = vv + zero;  // 计算新零点值

                //DA芯片嵌位处理
                if(zero < 0)  // 判断：零点值是否小于0
                    zero = 0;  // 限制为最小值0
                else if(zero > 65535)  // 判断：零点值是否大于65535
                    zero = 65535;  // 限制为最大值65535
                cabteRegister.setChannelZero(i, dwIdx, pgaVal & 0xFF, zero);  // 设置新的零点值

                Log.d(TAG,"ch:" + i + ",dwIdx:" + dwIdx + ",pga:" + Integer.toHexString(pgaVal)
                        + ",old zero:" + vv + ",zero:" + zero + ",averageVol:" +averageVol + ",volDA_perGrid:" + volDA_perGrid + ",best:" + best_value[i]);  // 输出调试日志

                //调节硬件
                channel[i].setPos(0);  // 通道纵向位置归零
            }  // 判断结束
        }  // 循环结束

        boolean bFinished = true;  // 所有通道完成标志，初始为true
        for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
            if(!finished[i]){  // 判断：通道是否未完成
                bFinished = false;  // 设置完成标志为false
                break;  // 跳出循环
            }  // 判断结束
        }  // 循环结束

        if(bFinished){  // 判断：是否所有通道都完成
            //本档位校准完成
            boolean ok=true;  // 校准成功标志，初始为true
            for(int i=0; i<channelNums; i++) {  // 循环：遍历所有通道
                if (channel[i].isOpen()) {  // 判断：通道是否打开

                    cabteRegister.setChannelZero(i,dwIdx,pgaVal &0xFF,bakCoef[i]);  // 恢复最佳零点值
                    String str1 = "档位="+pgaVal+", ch"+(i+1)
                            +"最佳: 平均值="+best_value[i]
                            +", 零点值="+bakCoef[i];  // 构建结果字符串
                    Log.i(TAG1, str1);  // 输出日志
                    resultString.add(str1);  // 添加结果字符串
                    //校准结果判断
                    int wucha=5;  // 设置误差阈值

                    if(Math.abs(best_value[i]) > wucha) {  // 判断：最佳误差是否超过阈值
                        errcode = 103;  // 设置错误码103
                        str1 = "ZeroCalibrate error: ch"+(i+1)+"平均值超标，大于"+wucha +"," + best_value[i] + "," + VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1mV) + "V";  // 构建错误信息
                        Log.e(TAG1, str1);  // 输出错误日志
                        resultString.add(str1);  // 添加错误信息
                        ok = false;  // 设置成功标志为false
                    }  // 判断结束
                }  // 判断结束
            }  // 循环结束
            if(ok) {  // 判断：是否校准成功
                if(DoStateMachine()) {  // 判断：是否完成所有档位
                    //全部校准结束
                    if(!cabteRegister.verifyChZeroCoef())  // 判断：零点系数验证是否通过
                        errcode = 104;  // 设置错误码104

                    if(errcode == 0){  // 判断：是否无错误
                        cabteRegister.setCalibrationState(HW_MHO38V1.CALIBRATION_TOP_ZERO,true);  // 设置零点校准状态为已完成
                        cabteRegister.saveCalibrationStateParam();  // 保存校准状态参数
                    }  // 判断结束
                    Log.d(TAG,"sMax:" + sMax);  // 输出最大幅度值
                    return true;  // 返回true，校准完成
                }  // 判断结束
                else {  // 否则：还有档位未校准
                    return false;  // 返回false，继续校准
                }  // 判断结束
            }  // 判断结束
            else  // 否则：校准失败
                return true;  // 返回true，校准失败
        }  // 判断结束
        else  // 否则：还有通道未完成
            return false;  // 返回false，继续校准
    }  // 方法结束

    /**
     * 执行档位切换状态机
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制多档位校准的切换逻辑</li>
     *   <li>自动切换PGA值和档位索引</li>
     *   <li>判断是否完成所有档位的校准</li>
     * </ul>
     * 
     * <p><b>状态机逻辑：</b>
     * <pre>
     * 1. PGA值递增
     *    └── pgaVal++
     * 
     * 2. 检查PGA低4位是否超过0xA
     *    └── (pgaVal & 0xF) > 0xA
     * 
     * 3. 如果超过0xA，执行PGA模式切换
     *    ├── 高增益模式（bit4=1）→ 档位切换
     *    │   ├── 档位索引加1
     *    │   ├── 检查是否完成所有档位
     *    │   │   ├── 档位索引 >= 4 → 返回true（校准完成）
     *    │   │   └── 否则 → 设置新档位，重置PGA值为0x400
     *    │   └── 延时300ms等待参数生效
     *    └── 低增益模式（bit4=0）→ 切换到高增益模式
     *        └── pgaVal = 0x410
     * 
     * 4. 初始化下一档位校准
     *    ├── 清零PGA时间戳
     *    ├── 检查参数并刷新PGA
     *    └── 输出日志
     * 
     * 5. 返回false，继续校准
     * </pre>
     * 
     * @return true表示所有档位校准完成，false表示还有档位未校准
     */
    private boolean DoStateMachine() {  // 执行档位切换状态机方法

        pgaVal++;  // PGA值加1
        if((pgaVal&0xF) > 0xA){  // 判断：PGA低4位是否超过0xA
            if((pgaVal & 0x10) != 0){  // 判断：是否为高增益模式（bit4=1）
                dwIdx++;  // 档位索引加1
                if(dwIdx >= 4){  // 判断：档位索引是否超过3
                    return true;  // 所有档位校准完成，返回true
                }else{  // 否则：还有档位未校准
                    dangwei = CabteRegister.getRatioIdx2Dang(0,dwIdx);  // 获取新档位ID
                    for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
                        channel[i].setVScaleId(dangwei);  // 设置通道档位
                        channel[i].setPos(0);  // 设置通道位置为0
                    }  // 循环结束
                    ms_sleep(300);  // 延时300ms，等待参数生效
                }  // 判断结束
                pgaVal = 0x400;  // 重置PGA值为0x400（低增益模式）
            }else{  // 否则：低增益模式
                pgaVal = 0x410;  // 切换到高增益模式
            }  // 判断结束
        }  // 判断结束

        pgaTs = 0;  // 清零PGA时间戳，表示需要立即刷新
        checkParamEx();  // 检查参数并刷新PGA配置
        delaySet(5);  // 设置延时为5秒
        //校准下一个档位
        Log.i(TAG1, "开始校准档位"+pgaVal+"-------------------------------");  // 输出日志：开始校准新档位
        return false;  // 返回false，继续校准
    }  // 方法结束
}  // 类结束
