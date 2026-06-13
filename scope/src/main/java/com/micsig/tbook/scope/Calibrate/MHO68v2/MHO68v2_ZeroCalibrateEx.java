package com.micsig.tbook.scope.Calibrate.MHO68v2;  // 包声明：MHO68 V2示波器校准模块

import android.os.SystemClock;  // 导入：Android系统时钟，用于获取运行时间
import android.util.Log;  // 导入：Android日志工具，用于输出调试信息

import com.micsig.tbook.scope.Action.ChannelHardw;  // 导入：通道硬件操作类，用于控制通道硬件
import com.micsig.tbook.scope.Calibrate.CabteRegister;  // 导入：校准寄存器管理类，用于管理校准数据
import com.micsig.tbook.scope.Calibrate.Calibrate;  // 导入：校准抽象基类，定义校准流程框架
import com.micsig.tbook.scope.Calibrate.HW;  // 导入：硬件常量定义，包含档位索引等常量
import com.micsig.tbook.scope.Calibrate.HwConfig;  // 导入：硬件配置类，用于获取硬件参数
import com.micsig.tbook.scope.Data.WaveData;  // 导入：波形数据类，用于存储波形数据
import com.micsig.tbook.scope.ScopeBase;  // 导入：示波器基类，包含屏幕尺寸等基础配置
import com.micsig.tbook.scope.Trigger.Trigger;  // 导入：触发器基类，定义触发类型常量
import com.micsig.tbook.scope.Trigger.TriggerEdge;  // 导入：边沿触发器类，用于设置边沿触发
import com.micsig.tbook.scope.Trigger.TriggerFactory;  // 导入：触发器工厂类，用于创建触发器实例
import com.micsig.tbook.scope.channel.Channel;  // 导入：通道类，定义通道属性和操作
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入：通道工厂类，用于通道验证
import com.micsig.tbook.scope.fpga.FPGACommand;  // 导入：FPGA命令类，用于发送FPGA控制命令
import com.micsig.tbook.scope.math.MathNative;  // 导入：数学运算本地方法类，用于波形计算
import com.micsig.tbook.scope.vertical.VerticalAxis;  // 导入：垂直轴类，用于垂直档位转换

/**
 * MHO68 V2示波器扩展零点校准实现类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate.MHO68v2（MHO68 V2示波器校准模块）</li>
 *   <li>架构层级：业务逻辑层 - 具体校准实现</li>
 *   <li>设计模式：继承Calibrate抽象基类，实现多档位零点校准流程</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现MHO68 V2示波器的多档位零点校准</li>
 *   <li>自动遍历所有档位和阻抗类型进行校准</li>
 *   <li>支持1MΩ和50Ω两种阻抗模式的自动切换</li>
 *   <li>通过状态机控制多档位校准流程</li>
 * </ul>
 * 
 * <p><b>与MHO68v2_ZeroCalibrate的区别：</b>
 * <pre>
 * MHO68v2_ZeroCalibrate：
 *   - 单档位校准，需要外部指定档位和阻抗
 *   - 适用于用户手动校准单个档位
 *   - 参数通过setParam方法传入
 * 
 * MHO68v2_ZeroCalibrateEx：
 *   - 多档位自动校准，自动遍历所有档位
 *   - 适用于出厂校准和批量校准
 *   - 自动切换阻抗类型（1MΩ → 50Ω）
 *   - 内置状态机控制校准流程
 * </pre>
 * 
 * <p><b>校准流程：</b>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │  1. calibratePrepare() - 准备阶段                           │
 * │     ├── 初始化校准模式为0（8通道模式）                       │
 * │     ├── 设置阻抗类型为1MΩ                                   │
 * │     ├── 设置档位索引为1（第一档）                           │
 * │     ├── 计算PGA增益值                                       │
 * │     ├── 设置所有通道参数（位置、档位、阻抗、耦合）          │
 * │     ├── 设置触发电平（特意不让触发）                        │
 * │     └── 初始化零点为0                                       │
 * │                                                             │
 * │  2. onCalibrate() - 执行阶段（循环执行）                    │
 * │     ├── 等待硬件操作完成                                    │
 * │     ├── 检查参数并刷新PGA配置                               │
 * │     ├── 采集所有通道波形数据                                │
 * │     ├── 计算波形平均值                                      │
 * │     ├── 检测外部信号输入                                    │
 * │     ├── 状态机控制单档位校准                                │
 * │     ├── 计算DA调整量并更新零点                              │
 * │     └── 检查是否完成当前档位                                │
 * │                                                             │
 * │  3. DoStateMachine() - 档位切换状态机                       │
 * │     ├── 检查PGA值是否超过范围                               │
 * │     ├── 切换到下一档位                                      │
 * │     ├── 检查是否需要切换阻抗类型                            │
 * │     │   ├── 1MΩ所有档位完成后 → 切换到50Ω                  │
 * │     │   └── 50Ω所有档位完成后 → 校准完成                   │
 * │     └── 初始化下一档位校准参数                              │
 * │                                                             │
 * │  4. 校准完成                                                │
 * │     ├── 恢复最佳零点值                                      │
 * │     ├── 验证零点系数有效性                                  │
 * │     ├── 设置校准状态标志                                    │
 * │     └── 保存校准状态参数                                    │
 * └─────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>档位遍历顺序：</b>
 * <pre>
 * 1MΩ阻抗：
 *   档位1 → 档位2 → ... → 档位N
 * 
 * 50Ω阻抗：
 *   档位1 → 档位2 → ... → 档位N
 * 
 * PGA值范围：
 *   0x200 → 0x201 → ... → 0x20（超过此范围切换档位）
 * </pre>
 * 
 * <p><b>状态机控制：</b>
 * <pre>
 * 状态0：初始状态
 *   - 计算平均值是否小于阈值（2像素）
 *   - 记录最佳值和备份系数
 *   - 如果没有更佳值，进入状态1
 * 
 * 状态1：收敛状态
 *   - 检查是否找到更佳值
 *   - 如果找到，回到状态0
 *   - 如果连续2次没有更佳值，完成当前档位
 * </pre>
 * 
 * <p><b>DA调整算法：</b>
 * <pre>
 * 调整量计算：
 *   volDA_perGrid = 默认通道系数（每格对应的DA值）
 *   zero = averageVol × volDA_perGrid  // 误差的DA值
 * 
 * 调整量限制：
 *   zero > 300 → zero = 300  // 限制最大调整量
 * 
 * 根据误差大小调整步进：
 *   误差 < 2像素：调整量 /= 4（精细调节）
 *   误差 < 5像素：调整量 /= 2（中等调节）
 *   调整量 < 0.2像素：固定调整0.1像素
 * 
 * DA值更新：
 *   1MΩ阻抗（非第一档）：zero = vv - zero
 *   其他情况：zero = vv + zero
 * 
 * DA值范围限制：
 *   0 ~ 65535（16位DA）
 *   超出范围时重置为中间值32768
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
 *   <li>出厂校准时自动校准所有档位的零点</li>
 *   <li>批量生产时的自动化校准</li>
 *   <li>定期维护校准（全档位）</li>
 * </ul>
 * 
 * @author zhuzh  // 作者：zhuzh
 * @version 1.0  // 版本号：1.0
 * @since 2018-6-29  // 创建日期：2018年6月29日
 * @see Calibrate 校准抽象基类  // 参见：Calibrate类
 * @see MHO68v2_ZeroCalibrate 单档位零点校准  // 参见：MHO68v2_ZeroCalibrate类
 * @see CabteRegister 校准寄存器管理  // 参见：CabteRegister类
 */
public class MHO68v2_ZeroCalibrateEx extends Calibrate {  // 继承Calibrate抽象基类，实现扩展零点校准
    
    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，初始化校准类型</li>
     *   <li>设置校准类型标识，用于区分不同校准流程</li>
     * </ul>
     * 
     * @param calibrateType 校准类型标识（如：CALIBRATE_TYPE_ZERO_EX）
     */
    public MHO68v2_ZeroCalibrateEx(int calibrateType) {  // 构造函数，接收校准类型参数
        super(calibrateType);  // 调用父类Calibrate的构造函数，传递校准类型
    }  // 构造函数结束
    
    /**
     * 设置错误码
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的setErrcode方法</li>
     *   <li>用于外部设置校准错误码</li>
     *   <li>错误码定义：0=成功，104=系数无效</li>
     * </ul>
     * 
     * @param errcode 错误码值
     */
    @Override  // 覆盖父类方法
    public void setErrcode(int errcode) {  // 设置错误码方法
        this.errcode = errcode;  // 保存错误码到成员变量
    }  // 方法结束

    /**
     * 最佳值数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录每个通道在迭代过程中的最优平均值</li>
     *   <li>用于判断校准是否收敛到最优解</li>
     *   <li>初始值为Float.MAX_VALUE，表示尚未找到最佳值</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>理论上为负无穷到正无穷</li>
     *   <li>实际有效范围：-屏幕高度/2 ~ +屏幕高度/2（像素）</li>
     *   <li>校准目标：|best_value| < 1像素（Ex版本要求更严格）</li>
     * </ul>
     */
    private float []best_value=new float[ChannelFactory.CH_CNT];  // 最佳值数组，长度为通道数
    
    //private int []maxVol=new int[4];  // 已注释：最大电压数组（保留用于扩展）
    
    /**
     * 备份系数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录每个通道最佳值对应的DA值（零点系数）</li>
     *   <li>校准完成后恢复此值作为最终零点</li>
     *   <li>用于防止迭代过程中偏离最优解</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>DA芯片输出范围：0 ~ 65535（16位）</li>
     *   <li>典型值：32768（中间值）</li>
     * </ul>
     */
    private float []bakCoef=new float[ChannelFactory.CH_CNT];  // 备份系数数组，长度为通道数
    
    /**
     * 状态数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于状态机控制校准流程</li>
     *   <li>状态0：初始状态，寻找最佳值</li>
     *   <li>状态1：收敛状态，确认是否到达极限</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>0：初始状态</li>
     *   <li>1：收敛状态</li>
     * </ul>
     */
    private int []state=new int[ChannelFactory.CH_CNT];  // 状态数组，长度为通道数

    /**
     * 尝试次数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录每个通道在当前状态下的尝试次数</li>
     *   <li>用于判断是否需要切换状态或完成校准</li>
     *   <li>超过阈值（2次）后进入下一阶段</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>0 ~ 2+：尝试次数计数</li>
     * </ul>
     */
    private int tryNum[]=new int[ChannelFactory.CH_CNT];  // 尝试次数数组，长度为通道数
    
    /**
     * 尝试系数数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录每个通道的尝试轮次</li>
     *   <li>用于判断校准是否真正收敛</li>
     *   <li>超过阈值（2轮）后强制完成当前档位</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>0 ~ 2+：尝试轮次计数</li>
     * </ul>
     */
    private int tryChiShu[]=new int[ChannelFactory.CH_CNT];  // 尝试系数数组，长度为通道数
    
    /**
     * 通道完成标志数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>标记每个通道当前档位是否完成校准</li>
     *   <li>true：已完成当前档位校准</li>
     *   <li>false：当前档位校准进行中</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>true：已完成</li>
     *   <li>false：进行中</li>
     * </ul>
     */
    private boolean finished[]=new boolean[ChannelFactory.CH_CNT];  // 通道完成标志数组，长度为通道数
    
    /**
     * 档位索引
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>当前校准的档位索引</li>
     *   <li>用于从校准寄存器获取对应的零点系数</li>
     *   <li>初始值为1（第一档）</li>
     *   <li>自动递增遍历所有档位</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>1 ~ 档位总数</li>
     * </ul>
     */
    private int dwIdx = 1;  // 档位索引，初始为1（第一档）
    
    /**
     * 档位ID
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>当前校准的垂直档位ID</li>
     *   <li>用于设置通道的垂直档位</li>
     *   <li>初始值为DANG_1mV（1mV/div）</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>VerticalAxis中定义的档位ID</li>
     * </ul>
     */
    private int dangwei = VerticalAxis.DANG_1mV;  // 档位ID，初始为1mV/div
    
    /**
     * 阻抗类型
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>当前校准的阻抗类型</li>
     *   <li>影响校准参数和DA调整方向</li>
     *   <li>自动从1MΩ切换到50Ω</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>Channel.RESISTANCE_1M (0)：1MΩ高阻抗</li>
     *   <li>Channel.RESISTANCE_50 (1)：50Ω低阻抗</li>
     * </ul>
     */
    private int resistanceType = Channel.RESISTANCE_1M;  // 阻抗类型，默认为1MΩ

    /**
     * 错误码
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录校准过程中的错误状态</li>
     *   <li>0表示成功，非0表示失败</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>0：校准成功</li>
     *   <li>104：零点系数验证失败</li>
     * </ul>
     */
    private int errcode;  // 错误码，初始为0
    
    /**
     * 日志标签：扩展零点校准
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于日志输出时标识扩展零点校准模块</li>
     *   <li>简化日志输出，便于日志过滤</li>
     * </ul>
     */
    private final String TAG = "ZeroEx";  // 日志标签，固定为"ZeroEx"
    
    /**
     * 完整日志标签
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>包含优先级前缀的完整日志标签</li>
     *   <li>格式：优先级前缀 + ":" + TAG</li>
     *   <li>用于详细日志输出</li>
     * </ul>
     */
    private final String TAG1=TAG_PRI+":"+TAG;  // 完整日志标签，格式为"优先级:ZeroEx"
    
    /**
     * 通道硬件操作实例
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>单例模式获取通道硬件操作实例</li>
     *   <li>用于设置通道PGA增益等硬件参数</li>
     * </ul>
     */
    ChannelHardw channelHardw = ChannelHardw.getInstance();  // 获取通道硬件操作单例
    
    /**
     * FPGA命令实例
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>单例模式获取FPGA命令实例</li>
     *   <li>用于发送FPGA控制命令</li>
     *   <li>如：设置PGA值、写入ADC满量程等</li>
     * </ul>
     */
    FPGACommand fpgaCommand = FPGACommand.getInstance();  // 获取FPGA命令单例

    /**
     * 获取日志标签
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getTAG方法</li>
     *   <li>返回包含优先级前缀的完整日志标签</li>
     *   <li>用于日志系统统一输出格式</li>
     * </ul>
     * 
     * @return 完整日志标签（格式：优先级前缀:ZeroEx）
     */
    @Override  // 覆盖父类方法
    public String getTAG() {  // 获取日志标签方法
        return TAG1;  // 返回包含优先级前缀的完整标签
    }  // 方法结束

    /**
     * 获取错误码
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的getErrcode方法</li>
     *   <li>返回当前校准的错误码</li>
     *   <li>用于外部查询校准结果</li>
     * </ul>
     * 
     * @return 错误码，0表示成功，非0表示失败
     */
    @Override  // 覆盖父类方法
    public int getErrcode() {  // 获取错误码方法
        //=1：校验错误
        return errcode;  // 返回当前错误码
    }  // 方法结束

    /**
     * 通道模式
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>指定当前校准的通道模式</li>
     *   <li>0：8通道模式</li>
     *   <li>1：4通道模式</li>
     *   <li>2：2通道模式（1-5）</li>
     *   <li>3：2通道模式（2-6）</li>
     *   <li>当前版本未使用，保留用于扩展</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>0 ~ 3：通道模式</li>
     * </ul>
     */
    int chMode = 0;  // 通道模式，默认为0（8通道模式）

    /**
     * 重置计算变量
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>在开始新档位校准前调用</li>
     *   <li>初始化所有通道的状态变量</li>
     *   <li>关闭的通道直接标记为完成</li>
     *   <li>确保每个档位从干净状态开始</li>
     * </ul>
     * 
     * <p><b>初始化内容：</b>
     * <pre>
     * best_value[i] = Float.MAX_VALUE  // 最佳值初始化为最大值
     * bakCoef[i] = 0                    // 备份系数清零
     * state[i] = 0                      // 状态初始化为0
     * tryNum[i] = 0                     // 尝试次数清零
     * tryChiShu[i] = 0                  // 尝试系数清零
     * finished[i] = !channel[i].isOpen()  // 关闭的通道标记为完成
     * </pre>
     */
    //校准下一档初始化
    private void rstCalculate(){  // 重置计算变量方法
        for(int i=0; i<ChannelFactory.CH_CNT; i++) {  // 循环：遍历所有通道
            best_value[i] = Float.MAX_VALUE;  // 初始化最佳值为最大浮点数，表示尚未找到最佳值
            bakCoef[i] = 0;  // 清零备份系数，初始无备份
            state[i] = 0;  // 初始化状态为0，表示初始状态
            tryNum[i] = 0;  // 清零尝试次数计数器
            tryChiShu[i] = 0;  // 清零尝试系数计数器
            finished[i] = !channel[i].isOpen();  // 关闭的通道直接标记为完成，打开的通道标记为未完成
        }  // 循环结束
    }  // 方法结束

    /**
     * 初始化校准寄存器
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的iniCalibrateReg方法</li>
     *   <li>设置初始阻抗类型为1MΩ</li>
     *   <li>可以在现有零点基础上进行校准，无需复位</li>
     *   <li>保留此方法以便将来扩展</li>
     * </ul>
     */
    @Override  // 覆盖父类方法
    public void iniCalibrateReg(){  // 初始化校准寄存器方法
        //可以不用复位零点，在目前的零点基础上进行校准
        setInitResistanceType(Channel.RESISTANCE_1M);  // 设置初始阻抗类型为1MΩ
    }  // 方法结束

    /**
     * 恢复校准配置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的restoreCalibrateConfig方法</li>
     *   <li>校准完成后恢复通道配置</li>
     *   <li>将所有通道设置为1V/div档位</li>
     * </ul>
     */
    @Override  // 覆盖父类方法
    protected void restoreCalibrateConfig() {  // 恢复校准配置方法
        super.restoreCalibrateConfig();  // 调用父类方法，恢复基础配置
        for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
            channel[i].setVScaleId(VerticalAxis.DANG_1V);  // 设置通道档位为1V/div
        }  // 循环结束
    }  // 方法结束

    /**
     * PGA增益值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>当前校准使用的PGA增益值</li>
     *   <li>PGA（可编程增益放大器）用于调整信号幅度</li>
     *   <li>影响校准精度和范围</li>
     *   <li>初始值为0x200，自动递增</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>0x200 ~ 0x20：PGA值范围</li>
     *   <li>超过0x20时切换到下一档位</li>
     * </ul>
     */
    int pgaVal = 0x200;  // PGA增益值，初始为0x200

    /**
     * 设置PGA增益值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置所有通道的PGA增益值</li>
     *   <li>用于校准过程中的PGA控制</li>
     *   <li>通过FPGA命令和通道硬件操作实现</li>
     * </ul>
     * 
     * <p><b>操作流程：</b>
     * <pre>
     * 1. 发送设备命令300（准备设置PGA）
     * 2. 保存PGA值到命令对象
     * 3. 创建增益值数组，所有通道使用相同值
     * 4. 调用通道硬件设置AD8370增益
     * 5. 再次发送设备命令300（确认设置）
     * </pre>
     * 
     * @param val PGA增益值
     */
    private void setPGAVal(int val){  // 设置PGA增益值方法
        fpgaCommand.cmdDevice(300);  // 发送设备命令300，准备设置PGA
        fpgaCommand.setPgaVal(val);  // 保存PGA值到命令对象，用于后续操作
        int [] res = {val,val,val,val,val,val,val,val};  // 创建8元素数组，所有元素为val
        channelHardw.set_ch_AD8370Gain(res);  // 设置AD8370增益，传入增益数组
        fpgaCommand.cmdDevice(300);  // 再次发送设备命令300，确认PGA设置生效
    }  // 方法结束


    /**
     * 校准准备阶段
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的calibratePrepare方法</li>
     *   <li>初始化校准模式和参数</li>
     *   <li>设置所有通道的初始配置</li>
     *   <li>计算PGA增益值</li>
     * </ul>
     * 
     * <p><b>准备流程：</b>
     * <pre>
     * 1. 初始化校准参数
     *    ├── chMode = 0（8通道模式）
     *    ├── resistanceType = 1MΩ
     *    └── dwIdx = 1（第一档）
     * 
     * 2. 计算PGA增益值
     *    └── pgaVal = get10008PGA(true) = 0x200
     * 
     * 3. 获取档位ID
     *    └── dangwei = getRatioIdx2Dang(1MΩ, 档位1)
     * 
     * 4. 设置所有通道参数
     *    ├── 位置归零
     *    ├── 设置档位
     *    ├── 设置阻抗类型（1MΩ）
     *    ├── 设置耦合类型（DC）
     *    ├── 设置触发电平（特意不让触发）
     *    └── 设置零点为0
     * 
     * 5. 延时300ms等待参数生效
     * 
     * 6. 初始化校准状态
     *    ├── 清零PGA时间戳
     *    ├── 检查参数并刷新PGA
     *    ├── 清零错误码
     *    ├── 清零最大差值
     *    └── 输出开始日志
     * </pre>
     */
    @Override  // 覆盖父类方法
    public void calibratePrepare() {  // 校准准备阶段方法
        chMode = 0;  // 初始化通道模式为0（8通道模式）
        resistanceType = Channel.RESISTANCE_1M;  // 设置阻抗类型为1MΩ
        dwIdx = HW.RATIO_DANG_1;  // 设置档位索引为第一档

        pgaVal =  get10008PGA(true) ;  // 获取初始PGA值（0x200）

        dangwei = CabteRegister.getRatioIdx2Dang(resistanceType,dwIdx);  // 根据阻抗和档位索引获取档位ID

        for(int i=0; i<channelNums; i++) {  // 循环：遍历所有通道
            channel[i].setPos(0);  // 通道纵向位置归零
            channel[i].setVScaleId(dangwei);  // 设置垂直档位
            channel[i].setResistanceType(resistanceType);  // 设置阻抗类型（1MΩ）
            channel[i].setCoupleType(Channel.COUPLE_TYPE_DC);  // 设置耦合类型为DC（直流耦合）
            TriggerEdge triggerEdge = (TriggerEdge)TriggerFactory.getTriggerObj(Trigger.TRIG_TYPE_EDGE);  // 获取边沿触发实例
            triggerEdge.getTriggerLevel(i).setPos(-ScopeBase.getHeight()/3);  // 设置触发电平为屏幕下方1/3处，特意不让触发
            channel[i].setZero(0);  // 设置零点为0
        }  // 循环结束

        ms_sleep(300);  // 延时300ms，等待参数生效
        pgaTs = 0;  // 清零PGA时间戳，表示需要立即刷新
        checkParamEx(true);  // 检查参数并刷新PGA配置（强制刷新）
        errcode = 0;  // 清零错误码，表示准备成功
        sMax = 0;  // 清零最大差值，用于检测外部信号
        resultString.add("<<<<<<<<<< ZeroCalibrate start ......");  // 添加结果字符串到结果列表
        Log.i(TAG1, "<<<<<<<<<< ZeroCalibrate start ......");  // 输出开始日志
    }  // 方法结束

    /**
     * 检查参数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的checkParam方法</li>
     *   <li>调用扩展的参数检查方法</li>
     *   <li>调用父类的参数检查方法</li>
     * </ul>
     * 
     * @return true表示参数有变化，false表示参数无变化
     */
    @Override  // 覆盖父类方法
    public boolean checkParam() {  // 检查参数方法
        checkParamEx(false);  // 调用扩展的参数检查方法（非强制刷新）
        return super.checkParam();  // 调用父类的参数检查方法
    }  // 方法结束

    /**
     * PGA设置时间戳
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录上次刷新PGA配置的时间</li>
     *   <li>用于定时刷新PGA配置</li>
     *   <li>防止PGA配置丢失导致校准失败</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>0：表示需要立即刷新</li>
     *   <li>>0：上次刷新的时间戳（毫秒）</li>
     * </ul>
     */
    private long pgaTs = 0;  // PGA设置时间戳，初始为0
    
    /**
     * 检查参数并定时刷新PGA配置（扩展版本）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查所有通道的档位、阻抗、耦合类型是否正确</li>
     *   <li>每隔60秒刷新一次PGA配置</li>
     *   <li>防止PGA配置丢失</li>
     *   <li>重置通道位置为零</li>
     *   <li>重置计算变量</li>
     * </ul>
     * 
     * <p><b>刷新条件：</b>
     * <pre>
     * 1. 距离上次刷新超过60秒
     * 2. 强制刷新标志为true
     * 3. 通道参数发生变化
     * </pre>
     * 
     * <p><b>刷新流程：</b>
     * <pre>
     * 1. 检查所有通道参数
     *    ├── 检查档位是否正确
     *    ├── 检查阻抗类型是否正确
     *    └── 检查耦合类型是否正确
     * 
     * 2. 判断是否需要刷新
     *    ├── 距离上次刷新超过60秒
     *    └── 或强制刷新标志为true
     * 
     * 3. 执行刷新
     *    ├── 输出日志（非第一次）
     *    ├── 刷新ADC满量程配置（写入默认值0xA000）
     *    ├── 重新设置PGA值
     *    ├── 更新同步
     *    ├── 重置所有通道位置为零
     *    ├── 重置计算变量
     *    └── 更新时间戳
     * </pre>
     * 
     * @param b 强制刷新标志，true表示强制刷新
     * @return true表示执行了刷新或参数有变化，false表示未刷新
     */
    public boolean checkParamEx(boolean b){  // 检查参数扩展方法
        boolean bChange = false;  // 参数变化标志，初始为false
        long ts = SystemClock.elapsedRealtime();  // 获取当前系统运行时间（毫秒）
        for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
            if(channel[i].getVScaleId() != dangwei){  // 判断：通道档位是否与目标档位不同
                channel[i].setVScaleId(dangwei);  // 设置通道档位为目标档位
                bChange = true;  // 标记参数有变化
            }  // 判断结束
            if(channel[i].getResistanceType() != resistanceType){  // 判断：通道阻抗类型是否与目标阻抗不同
                channel[i].setResistanceType(resistanceType);  // 设置通道阻抗类型为目标阻抗
                bChange = true;  // 标记参数有变化
            }  // 判断结束
            if(channel[i].getCoupleType() != Channel.COUPLE_TYPE_DC){  // 判断：通道耦合类型是否不是DC
                channel[i].setCoupleType(Channel.COUPLE_TYPE_DC);  // 设置通道耦合类型为DC
                bChange = true;  // 标记参数有变化
            }  // 判断结束
        }  // 循环结束

        if(((ts - pgaTs) > (60*1000)) || b){  // 判断：距离上次刷新是否超过60秒，或强制刷新标志为true
            if(pgaTs != 0) {  // 判断：是否为第一次刷新
                Log.e(TAG, "ts - pgaTs:" + (ts - pgaTs));  // 输出日志：距离上次刷新的时间
            }  // 判断结束

            for(int i=0;i<2;i++){  // 循环：遍历ADC索引0-1
                for(int j=0;j<2;j++){  // 循环：遍历通道索引0-1
                    for(int k=0;k<2;k++) {  // 循环：遍历增益索引0-1
                        fpgaCommand.writeAD_gain(i, j, k,0xA000);  // 写入默认满量程值0xA000
                    }  // 循环结束：增益索引
                }  // 循环结束：通道索引
            }  // 循环结束：ADC索引
            setPGAVal(pgaVal);  // 重新设置PGA值
            updateSync();  // 更新同步，确保配置生效
            for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
                channel[i].setPos(0);  // 设置通道位置为零
            }  // 循环结束
            rstCalculate();  // 重置计算变量
            pgaTs = ts;  // 更新时间戳为当前时间
            bChange = true;  // 标记参数有变化
        }  // 判断结束：需要刷新
        return bChange;  // 返回参数变化标志
    }  // 方法结束

    /**
     * 最大差值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录校准过程中波形幅度的最大值</li>
     *   <li>用于检测是否有外部信号输入</li>
     *   <li>如果差值过大（>300），说明有外部信号</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>0 ~ ADC最大值（通常为4095或更高）</li>
     *   <li>正常无信号时：< 300</li>
     *   <li>有外部信号时：> 300</li>
     * </ul>
     */
    int sMax = 0;  // 最大差值，初始为0

    /**
     * 执行校准
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>覆盖父类的onCalibrate方法</li>
     *   <li>执行多档位零点校准的核心逻辑</li>
     *   <li>通过迭代算法调整DA输出值</li>
     *   <li>直到波形平均值接近零</li>
     *   <li>自动切换到下一档位继续校准</li>
     * </ul>
     * 
     * <p><b>校准算法：</b>
     * <pre>
     * 1. 采集波形数据
     *    ├── 等待硬件操作完成
     *    ├── 检查参数并刷新PGA
     *    └── 遍历所有通道获取波形
     * 
     * 2. 计算波形平均值
     *    ├── 计算波形求和
     *    └── 计算平均值（考虑波形因子）
     * 
     * 3. 检测外部信号输入
     *    ├── 计算波形幅度（最大值-最小值）
     *    └── 如果幅度>300，输出警告
     * 
     * 4. 状态机控制单档位校准
     *    ├── 状态0：寻找最佳值
     *    │   ├── 检查平均值是否小于阈值（2像素）
     *    │   ├── 记录最佳值和备份系数
     *    │   └── 如果没有更佳值，进入状态1
     *    └── 状态1：确认收敛
     *        ├── 检查是否找到更佳值
     *        ├── 如果找到，回到状态0
     *        └── 如果连续2次没有更佳值，完成当前档位
     * 
     * 5. 计算DA调整量
     *    ├── 计算默认通道系数
     *    ├── 限制调整量最大值（300）
     *    ├── 根据误差大小调整步进
     *    └── 计算新的零点值
     * 
     * 6. 更新零点值
     *    ├── DA值范围限制
     *    ├── 保存新的零点值
     *    └── 调节硬件
     * 
     * 7. 检查当前档位是否完成
     *    ├── 所有通道完成 → 进入档位切换状态机
     *    └── 有通道未完成 → 继续校准
     * </pre>
     * 
     * @return true表示所有档位校准完成，false表示校准进行中
     */
    @Override  // 覆盖父类方法
    public boolean onCalibrate() {  // 执行校准方法
        //等待硬件操作完成
        if(!isFinishedAction())  // 判断：硬件操作是否完成
            return false;  // 硬件操作未完成，继续等待
        delaySet(0);  // 设置延时为0，立即执行

        if(checkParamEx(false)){  // 判断：参数检查是否返回true（参数有变化）
            return false;  // 参数有变化，需要重新校准
        }  // 判断结束

        float[] average ={0,0,0,0,0,0,0,0};  // 波形平均值数组，每个通道一个值
        long sum;  // 波形求和值，用于计算平均值
        WaveData waveData = null;  // 波形数据对象，用于存储采集的波形
        int N;  // 波形长度，用于计算平均值

        HwConfig hwConfig = HwConfig.getInstance();  // 获取硬件配置实例，用于获取波形因子
        //获取每个通道的波形平均值
        for(int i=0; i<channelNums; i++){  // 循环：遍历所有通道
            if(channel[i].isOpen()) {  // 判断：通道是否打开
                waveData = (WaveData) getWave(i);  // 获取通道i的波形数据
                if(waveData == null || (N = waveData.getWaveLength()) < 10)  // 判断：波形数据是否有效
                    return false;  // 波形数据无效，继续等待
                sum = MathNative.calcSum(waveData.getByteBuffer());  // 计算波形求和（所有采样点之和）

                average[i] = (float) (hwConfig.getWavFactor() * sum / N);  // 计算平均值：波形因子 × 求和 / 长度

                int max1 = MathNative.calcMax(waveData.getByteBuffer());  // 计算波形最大值
                int min1 = MathNative.calcMin(waveData.getByteBuffer());  // 计算波形最小值
                int ss = Math.abs(max1 - min1);  // 计算波形幅度（峰峰值）
                if(ss > sMax){  // 判断：当前幅度是否大于记录的最大值
                    sMax = ss;  // 更新最大差值
                }  // 判断结束
                if(ss * hwConfig.getWavFactor() > 300){  // 判断：波形幅度是否超过300（可能有外部信号）
                    Log.i(TAG1, "ch:" + i + ",max="+max1+", min="+min1);  // 输出日志：通道和最大最小值
//                    Log.i(TAG1, "ch"+(i+1)+"有外接信号输入，校准结束！");
//                    errcode = 105;
//                    String str1 = "ZeroCalibrate error: ch"+(i+1)+"可能通道探头没有拔出，或者从外部输入了信号";
//                    Log.e(TAG1, str1);
//                    resultString.add(str1);
//                    return true;
                }  // 判断结束：幅度超过300

            }  // 判断结束：通道打开
        }  // 循环结束
        updateSync();  // 更新同步，确保硬件状态一致
        for(int i=0; i<channelNums; i++) {  // 循环：遍历所有通道
            if(channel[i].isOpen()) {  // 判断：通道是否打开
                float volDA_perGrid = (float) cabteRegister.vol_ChannelCoef_defaultEx(i,CabteRegister.getRatioIdx2Dang(channel[i].getResistanceType(),dwIdx),pgaVal);  // 获取每格对应的DA值

                float averageVol=average[i];  // 获取通道i的平均值
                //状态机控制流程
                switch (state[i]) {  // 根据当前状态执行不同逻辑
                    case 0: {  // 状态0：初始状态，寻找最佳值
                        int fV = 2;  // 阈值：2像素，判断平均值是否足够小
                        if(Math.abs(averageVol) <  fV){  // 判断：平均值绝对值是否小于阈值
                            if (Math.abs(averageVol) < Math.abs(best_value[i])) {  // 判断：是否比最佳值更好
                                //记录更佳值
                                best_value[i] = averageVol;  // 更新最佳值为当前平均值
                                bakCoef[i] = cabteRegister.getChannelZero(i, dwIdx, pgaVal & 0xFF);  // 获取并备份当前零点DA值
                                Log.d(TAG, "ch:" + i + ",averageVol:" + averageVol + ",coef:" + bakCoef[i]);  // 输出调试日志

                            } else {  // 否则：没有找到更佳值
                                //没有更佳化，说明可能到达极限，进入下一步
                                state[i]++;  // 状态加1，进入状态1
                            }  // 判断结束：是否找到更佳值
                        }  // 判断结束：平均值小于阈值
                        tryNum[i] = 0;  // 清零尝试次数
                        tryChiShu[i] = 0;  // 清零尝试系数
                        finished[i] = false;  // 标记未完成
                    }  // case 0代码块结束
                        break;  // 跳出switch
                    case 1:  // 状态1：收敛状态，确认是否到达极限
                        if (Math.abs(averageVol) < Math.abs(best_value[i])) {  // 判断：是否找到更佳值
                            //找到更佳值，说明没有到达极限，回到第1步
                            best_value[i] = averageVol;  // 更新最佳值
                            bakCoef[i] = cabteRegister.getChannelZero(i,dwIdx,pgaVal & 0xFF);  // 获取并备份当前零点DA值
                            Log.d(TAG,"ch:" + i +",averageVol:" + averageVol + ",coef:" + bakCoef[i]);  // 输出调试日志
                            state[i] = 0;  // 回到状态0
                            finished[i] = false;  // 标记未完成
                        } else {  // 否则：没有找到更佳值
                            int chi = 2;  // 尝试次数阈值：2次
                            if (++tryNum[i] > chi) {  // 判断：尝试次数是否超过阈值（先加1再判断）
                                if(!finished[i]) {  // 判断：是否尚未完成
                                    tryChiShu[i]++;  // 尝试系数加1
                                    if(Math.abs(best_value[i]) < 1 || tryChiShu[i] > chi) {  // 判断：最佳值足够小或尝试轮次足够多
                                        finished[i] = true;  // 标记完成
                                    }  // 判断结束：满足完成条件
                                    else {  // 否则：不满足完成条件
                                        tryNum[i] = 0;  // 清零尝试次数，再来一次
                                    }  // 判断结束：是否满足完成条件
                                }  // 判断结束：尚未完成
                            }  // 判断结束：尝试次数超过阈值
                        }  // 判断结束：是否找到更佳值
                        break;  // 跳出switch
                }  // switch结束

                float fx1 = Math.abs(averageVol);  // 误差多少像素
                float zero = averageVol*volDA_perGrid;  // 误差的DA值：平均值 × 每格DA值
                if(zero > 300){  // 判断：调整量是否超过300
                    zero = 300;  // 限制最大调整量为300
                }  // 判断结束

                if(fx1 < 2){  // 判断：误差小于2像素
                    zero /= 4;  // 每次调节到四分之一，精细调节
                }else if(fx1 < 5)  // 判断：误差小于5像素
                    zero /= 2;  // 每次调节二分之一，中等调节

                //当调节值小于0.2像素时，每次固定调节0.1像素
                if(Math.abs(zero) < Math.abs(volDA_perGrid/5))  // 判断：调节值是否小于0.2像素
                {  // 代码块开始
                    zero = Math.abs(volDA_perGrid/10);  // 固定调节0.1像素
                    if(fx1 < 0) zero = -zero;  // 保持方向：如果误差为负，调节值也为负
                    //这里zero值可能小于1，但是不用担心。虽然DA的精度为1，但是零点还是采用浮点数。
                    //在小档位时，可能需要移动好几个像素DA值才会修改1。
                    //处理方法是，FPGA增加偏移量补偿寄存器
                }  // 判断结束
                float vv = cabteRegister.getChannelZero(i,dwIdx,pgaVal & 0xFF);  // 从校准寄存器获取当前零点DA值
                //计算出新的零点

                if(channel[i].getResistanceType() == Channel.RESISTANCE_1M  // 判断：是否为1MΩ阻抗
                        && dwIdx != HW.RATIO_DANG_1){  // 判断：是否非第一档
                    zero = vv - zero;  // 1MΩ非第一档：减去调整量
                }else{  // 否则：其他情况
                    zero = vv + zero;  // 其他情况：加上调整量
                }  // 判断结束


                //DA芯片嵌位处理
                if(zero < 0) {  // 判断：DA值是否小于0
                    zero = 32768;  // 重置为中间值，防止溢出
                }  // 判断结束
                else if(zero > 65535) {  // 判断：DA值是否大于65535
                    zero = 32768;  // 重置为中间值，防止溢出
                }  // 判断结束
                cabteRegister.setChannelZero(i, dwIdx, pgaVal & 0xFF, zero);  // 将新的零点值写入校准寄存器


                Log.d(TAG,"ch:" + i + ",dwIdx:" + dwIdx + ",pga:" + Integer.toHexString(pgaVal)
                        + ",old zero:" + vv + ",zero:" + zero + ",averageVol:" +averageVol + ",volDA_perGrid:" + volDA_perGrid + ",best:" + best_value[i]);  // 输出调试日志

                //调节硬件
                channel[i].setPos(0);  // 通道纵向位置归零
            }  // 判断结束：通道打开
        }  // 循环结束

        boolean bFinished = true;  // 完成标志，初始假设已完成
        for(int i=0;i<channelNums;i++){  // 循环：遍历所有通道
            if(!finished[i]){  // 判断：通道是否未完成
                bFinished = false;  // 标记未完成
                break;  // 跳出循环
            }  // 判断结束
        }  // 循环结束

        if(bFinished){  // 判断：当前档位是否校准完成

            //本档位校准完成
            boolean ok=true;  // 成功标志，初始假设成功
            for(int i=0; i<channelNums; i++) {  // 循环：遍历所有通道
                if (channel[i].isOpen()) {  // 判断：通道是否打开

                    cabteRegister.setChannelZero(i,dwIdx,pgaVal &0xFF,bakCoef[i]);  // 恢复最佳零点值
                    String str1 = "档位="+pgaVal+", ch"+(i+1)  // 构建结果字符串：档位和通道
                            +"最佳: 平均值="+best_value[i]  // 添加最佳平均值
                            +", 零点值="+bakCoef[i];  // 添加最佳零点值
                    Log.i(TAG1, str1);  // 输出日志
                    resultString.add(str1);  // 添加结果字符串到结果列表
                    //校准结果判断
                    int wucha=5;  // 误差阈值：5像素

//                    if(Math.abs(best_value[i]) > wucha) {
//                        errcode = 103;
//                        str1 = "ZeroCalibrate error: ch"+(i+1)+"平均值超标，大于"+wucha +"," + best_value[i] + "," + VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1mV) + "V";
//                        Log.e(TAG1, str1);
//                        resultString.add(str1);
//                        ok = false;
//                    }
                }  // 判断结束：通道打开
            }  // 循环结束
            if(ok) {  // 判断：校准是否成功
                if(DoStateMachine()) {  // 判断：档位切换状态机是否返回true（所有档位完成）
                    //全部校准结束
                    if(!cabteRegister.verifyChZeroCoef())  // 判断：零点系数验证是否失败
                        errcode = 104;  // 设置错误码：系数验证失败

                    Log.d(TAG,"sMax:" + sMax);  // 输出调试日志：最大差值

                    if(errcode == 0){  // 判断：错误码是否为0（校准成功）
                        cabteRegister.setCalibrationState(HW_MHO68V2.CALIBRATION_TOP_ZERO,true);  // 设置零点校准状态为已完成
                        cabteRegister.saveCalibrationStateParam();  // 保存校准状态参数
                    }  // 判断结束
                    return true;  // 所有档位校准完成
                }  // 判断结束：所有档位完成
                else  // 否则：还有档位未校准
                    return false;  // 继续校准下一档位
            }  // 判断结束：校准成功
            else {  // 否则：校准失败
                return true;  // 校准失败，结束
            }  // 判断结束
        }  // 判断结束：当前档位完成
        else  // 否则：当前档位未完成
            return false;  // 继续校准当前档位
    }  // 方法结束


    /**
     * 获取PGA值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取或递增PGA值</li>
     *   <li>用于控制PGA值的递增逻辑</li>
     *   <li>支持重置和递增两种模式</li>
     * </ul>
     * 
     * <p><b>参数说明：</b>
     * <pre>
     * bFirst = true：
     *   - 重置PGA值为0x200
     *   - 用于开始新档位的校准
     * 
     * bFirst = false：
     *   - PGA值加1
     *   - 用于同一档位的不同PGA值校准
     * </pre>
     * 
     * @param bFirst 是否为第一次获取，true表示重置，false表示递增
     * @return 当前PGA值
     */
    private int get10008PGA(boolean bFirst){  // 获取PGA值方法

        if(bFirst){  // 判断：是否为第一次获取
            pgaVal = 0x200;  // 重置PGA值为0x200
        }else{  // 否则：非第一次获取
            pgaVal++;  // PGA值加1
        }  // 判断结束
        return pgaVal;  // 返回当前PGA值
    }  // 方法结束
    
    /**
     * 执行档位切换状态机
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制多档位校准的切换逻辑</li>
     *   <li>自动切换PGA值、档位索引和阻抗类型</li>
     *   <li>判断是否完成所有档位的校准</li>
     * </ul>
     * 
     * <p><b>状态机逻辑：</b>
     * <pre>
     * 1. 获取下一个PGA值
     *    └── pgaVal++
     * 
     * 2. 检查PGA值是否超过范围
     *    └── (pgaVal & 0xFF) > 0x20
     * 
     * 3. 如果超过范围，执行档位切换
     *    ├── 档位索引加1
     *    ├── 检查是否需要切换阻抗类型
     *    │   ├── 1MΩ所有档位完成 → 切换到50Ω
     *    │   └── 50Ω所有档位完成 → 返回true（校准完成）
     *    ├── 设置新的档位ID
     *    ├── 设置所有通道参数
     *    ├── 重置PGA值为0x200
     *    └── 延时300ms等待参数生效
     * 
     * 4. 初始化下一档位校准
     *    ├── 清零PGA时间戳
     *    ├── 检查参数并刷新PGA
     *    └── 输出日志
     * 
     * 5. 返回false，继续校准
     * </pre>
     * 
     * <p><b>档位切换顺序：</b>
     * <pre>
     * 1MΩ阻抗：
     *   PGA: 0x200 → 0x201 → ... → 0x20
     *   档位1 → 档位2 → ... → 档位N
     * 
     * 50Ω阻抗：
     *   PGA: 0x200 → 0x201 → ... → 0x20
     *   档位1 → 档位2 → ... → 档位N
     * </pre>
     * 
     * @return true表示所有档位校准完成，false表示还有档位未校准
     */
    private boolean DoStateMachine() {  // 执行档位切换状态机方法
        int val = get10008PGA(false);  // 获取下一个PGA值（递增）
        if((val & 0xFF) > 0x20){  // 判断：PGA值低8位是否超过0x20
            dwIdx++;  // 档位索引加1
            if(dwIdx >= CabteRegister.getRatioDangCnt() && resistanceType == Channel.RESISTANCE_50) {  // 判断：档位索引是否超过总数且阻抗为50Ω
//                switch (chMode){
//                    case 0://8通道
//                        ChannelFactory.chClose(ChannelFactory.CH2);
//                        ChannelFactory.chClose(ChannelFactory.CH4);
//                        ChannelFactory.chClose(ChannelFactory.CH6);
//                        ChannelFactory.chClose(ChannelFactory.CH8);
//                        ms_sleep(300);
//                        dwIdx = 0;
//                        resistanceType = Channel.RESISTANCE_1M;
//                        chMode++;
//                        break;
//                    case 1://4通道
//                        ChannelFactory.chClose(ChannelFactory.CH2);
//                        ChannelFactory.chClose(ChannelFactory.CH4);
//                        ChannelFactory.chClose(ChannelFactory.CH6);
//                        ChannelFactory.chClose(ChannelFactory.CH8);
//                        ChannelFactory.chClose(ChannelFactory.CH2);
//                        ChannelFactory.chClose(ChannelFactory.CH6);
//                        ms_sleep(300);
//                        chMode++;
//                        dwIdx = 0;
//                        resistanceType = Channel.RESISTANCE_1M;
//                        break;
//                    case 2://2通道 1-5
//                        ChannelFactory.chOpen(ChannelFactory.CH2);
//                        ChannelFactory.chOpen(ChannelFactory.CH6);
//                        ChannelFactory.chClose(ChannelFactory.CH1);
//                        ChannelFactory.chClose(ChannelFactory.CH5);
//                        ms_sleep(300);
//                        TriggerEdge triggerEdge = (TriggerEdge)TriggerFactory.getTriggerObj(Trigger.TRIG_TYPE_EDGE);
//                        triggerEdge.setTriggerSource(ChannelFactory.CH2);
//                        ms_sleep(300);
//                        triggerEdge.setTriggerSource(ChannelFactory.CH2);
//                        chMode++;
//                        dwIdx = 0;
//                        resistanceType = Channel.RESISTANCE_1M;
//                        break;
//                    case 3://2通道 2-6
//                        return true;
//                }
                return true;  // 50Ω所有档位校准完成，返回true
            }else if (dwIdx >= CabteRegister.getRatioDangCnt() && resistanceType == Channel.RESISTANCE_1M) {  // 判断：档位索引是否超过总数且阻抗为1MΩ
                dwIdx = 0;  // 重置档位索引为0
                resistanceType = Channel.RESISTANCE_50;  // 切换阻抗类型为50Ω
            }  // 判断结束
            dangwei = CabteRegister.getRatioIdx2Dang(resistanceType,dwIdx);  // 获取新的档位ID
            for (int i = 0; i < channelNums; i++) {  // 循环：遍历所有通道
                channel[i].setVScaleId(dangwei);  // 设置通道档位
                channel[i].setResistanceType(resistanceType);  // 设置通道阻抗类型
                channel[i].setCoupleType(Channel.COUPLE_TYPE_DC);  // 设置通道耦合类型为DC
            }  // 循环结束
            val = get10008PGA(true);  // 重置PGA值为0x200
            ms_sleep(300);  // 延时300ms，等待参数生效
        }  // 判断结束：PGA值超过范围

        pgaTs = 0;  // 清零PGA时间戳，表示需要立即刷新
        checkParamEx(true);  // 检查参数并刷新PGA配置（强制刷新）
        //校准下一个档位
        Log.i(TAG1, "开始校准档位"+val+"-------------------------------");  // 输出日志：开始校准新档位
        return false;  // 返回false，继续校准下一档位
    }  // 方法结束
}  // 类结束
