package com.micsig.tbook.scope;  // 定义包名：示波器核心功能模块

// ==================== Android系统导入 ====================
import android.content.Context;  // Android上下文，用于访问系统资源和服务
import android.content.res.Resources;  // Android资源管理器，用于加载资源文件
import android.os.SystemClock;  // Android系统时钟，用于获取启动时间
import android.util.Log;  // Android日志工具，用于调试输出

// ==================== 项目基础库导入 ====================
import com.micsig.base.Logger;  // 项目日志工具，封装统一的日志输出

// ==================== 硬件层导入 ====================
import com.micsig.tbook.hardware.Hardware;  // 硬件抽象层，提供硬件访问接口

// ==================== 自动设置模块导入 ====================
import com.micsig.tbook.scope.Auto.Auto;  // 自动设置配置管理

// ==================== 校准模块导入 ====================
import com.micsig.tbook.scope.Calibrate.CabteRegister;  // 校准寄存器管理
import com.micsig.tbook.scope.Calibrate.HwConfig;  // 硬件配置管理
import com.micsig.tbook.scope.Calibrate.IADC;  // ADC接口定义

// ==================== 数据库模块导入 ====================
import com.micsig.tbook.scope.DB.DBHelper;  // 数据库助手，用于存储配置和探头信息

// ==================== 数据管理模块导入 ====================
import com.micsig.tbook.scope.Data.DataFactory;  // 数据工厂，管理数据保存
import com.micsig.tbook.scope.Data.SaveBin;  // 二进制格式波形数据保存
import com.micsig.tbook.scope.Data.SaveCsv;  // CSV格式波形数据保存
import com.micsig.tbook.scope.Data.SaveRecoverySession;  // 会话恢复数据保存
import com.micsig.tbook.scope.Data.WaveData;  // 波形数据结构定义

// ==================== 显示模块导入 ====================
import com.micsig.tbook.scope.Display.Display;  // 显示管理，控制波形显示方式
import com.micsig.tbook.scope.Display.DisplayXYService;  // XY模式显示服务

// ==================== 事件模块导入 ====================
import com.micsig.tbook.scope.Event.EventBase;  // 事件基类，封装事件数据
import com.micsig.tbook.scope.Event.EventFactory;  // 事件工厂，管理事件订阅和发送

// ==================== 采样模块导入 ====================
import com.micsig.tbook.scope.Sample.Sample;  // 采样状态管理
import com.micsig.tbook.scope.Sample.SegmentSample;  // 段采样管理

// ==================== 触发模块导入 ====================
import com.micsig.tbook.scope.Trigger.TriggerFactory;  // 触发工厂，管理触发配置

// ==================== 通道模块导入 ====================
import com.micsig.tbook.scope.channel.Channel;  // 物理通道基类
import com.micsig.tbook.scope.channel.ChannelFactory;  // 通道工厂，管理所有通道
import com.micsig.tbook.scope.channel.MathChannel;  // 数学运算通道
import com.micsig.tbook.scope.channel.RefChannel;  // 参考波形通道
import com.micsig.tbook.scope.channel.SegmentedSingleBean;  // 段采样单帧数据结构
import com.micsig.tbook.scope.channel.SerialChannel;  // 串行总线解码通道

// ==================== FPGA模块导入 ====================
import com.micsig.tbook.scope.fpga.FPGABoot;  // FPGA加载和管理

// ==================== 水平轴模块导入 ====================
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 水平轴（时基）管理
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath;  // 数学通道水平轴

// ==================== 数学运算模块导入 ====================
import com.micsig.tbook.scope.math.MathService;  // 数学运算服务
import com.micsig.tbook.scope.math.mathEx.MathExt;  // 数学表达式扩展（OpenCL加速）

// ==================== 测量模块导入 ====================
import com.micsig.tbook.scope.measure.MeasureService;  // 测量服务，提供自动测量功能

// ==================== 探头模块导入 ====================
import com.micsig.tbook.scope.probe.BaseProbe;  // 探头基类
import com.micsig.tbook.scope.probe.ProbeFactory;  // 探头工厂，管理探头配置
import com.micsig.tbook.scope.probe.ProbeUtils;  // 探头工具类

// ==================== 设备抽象层导入 ====================
import com.micsig.tbook.scope.surface.DeviceFactory;  // 设备工厂，分配硬件设备
import com.micsig.tbook.scope.surface.HwDevice;  // 硬件设备接口

// ==================== 垂直轴模块导入 ====================
import com.micsig.tbook.scope.vertical.VerticalAxis;  // 垂直轴（幅度）管理

// ==================== Java标准库导入 ====================
import java.io.InputStream;  // 输入流，用于读取资源文件
import java.io.RandomAccessFile;  // 随机访问文件，用于文件操作
import java.util.ArrayList;  // 动态数组，用于存储段采样时间戳
import java.util.List;  // 列表接口
import java.util.Observable;  // 被观察者类，实现观察者模式
import java.util.Observer;  // 观察者接口，接收事件通知

/**
 * 示波器核心管理类 - 整个示波器系统的中央协调器
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope（示波器核心模块）</li>
 *   <li>架构层级：业务逻辑层 - 核心协调器</li>
 *   <li>设计模式：单例模式 + 观察者模式</li>
 *   <li>职责类型：核心协调器、状态管理器、事件处理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>协调示波器各子模块的初始化和交互</li>
 *   <li>管理示波器运行状态（运行/停止/单次/自动）</li>
 *   <li>管理显示模式（普通/Zoom/XY/滚屏）</li>
 *   <li>计算采样率和存储深度</li>
 *   <li>管理通道采样状态</li>
 *   <li>处理事件通知</li>
 *   <li>协调硬件和软件之间的交互</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>作为示波器的核心协调器，统一管理各子模块</li>
 *   <li>提供统一的示波器状态访问入口</li>
 *   <li>实现示波器的核心业务逻辑</li>
 *   <li>解耦各子模块之间的依赖关系</li>
 * </ul>
 *
 * <p><b>类结构图：</b>
 * <pre>
 * Scope (核心协调器)
 *   │
 *   ├── 硬件层
 *   │     ├── Hardware (硬件抽象)
 *   │     ├── FPGABoot (FPGA加载)
 *   │     └── HwConfig (硬件配置)
 *   │
 *   ├── 采样层
 *   │     ├── Sample (采样管理)
 *   │     └── SegmentSample (段采样)
 *   │
 *   ├── 显示层
 *   │     ├── Display (显示管理)
 *   │     └── DisplayXYService (XY显示)
 *   │
 *   ├── 通道层
 *   │     ├── ChannelFactory (通道工厂)
 *   │     ├── Channel (物理通道)
 *   │     ├── MathChannel (数学通道)
 *   │     └── RefChannel (参考通道)
 *   │
 *   ├── 水平轴
 *   │     └── HorizontalAxis (时基管理)
 *   │
 *   ├── 触发层
 *   │     └── TriggerFactory (触发工厂)
 *   │
 *   ├── 测量层
 *   │     └── MeasureService (测量服务)
 *   │
 *   ├── 数学运算
 *   │     └── MathService (数学服务)
 *   │
 *   ├── 数据层
 *   │     ├── DataFactory (数据工厂)
 *   │     ├── SaveBin (二进制保存)
 *   │     └── SaveCsv (CSV保存)
 *   │
 *   └── 事件层
 *         └── EventFactory (事件工厂)
 * </pre>
 *
 * <p><b>运行状态说明：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────┐
 * │ 状态        │ 方法           │ 说明                            │
 * ├─────────────┼────────────────┼────────────────────────────────┤
 * │ 运行        │ setRun(true)   │ 连续采样，实时更新波形          │
 * │ 停止        │ setRun(false)  │ 停止采样，保持当前波形          │
 * │ 单次        │ setSingle(true)│ 单次触发后停止                  │
 * │ 自动        │ Auto(true)     │ 自动设置参数                    │
 * └─────────────┴────────────────┴────────────────────────────────┘
 * </pre>
 *
 * <p><b>显示模式说明：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────┐
 * │ 模式        │ 判断方法        │ 说明                           │
 * ├─────────────┼─────────────────┼───────────────────────────────┤
 * │ Zoom模式    │ isZoom()        │ 放大显示，同时显示缩略图       │
 * │ XY模式      │ isInXYMode()    │ X-Y显示模式                    │
 * │ 滚屏模式    │ isInScrollMode()│ 慢时基滚屏显示                 │
 * │ 慢时基模式  │ isInSlowScaleMode()│ 慢时基非滚屏显示            │
 * └─────────────┴─────────────────┴───────────────────────────────┘
 * </pre>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：Hardware（硬件抽象层）</li>
 *   <li>依赖：FPGABoot（FPGA加载和管理）</li>
 *   <li>依赖：Sample（采样状态管理）</li>
 *   <li>依赖：Display（显示管理）</li>
 *   <li>依赖：HorizontalAxis（水平轴管理）</li>
 *   <li>依赖：ChannelFactory（通道工厂）</li>
 *   <li>依赖：TriggerFactory（触发工厂）</li>
 *   <li>依赖：EventFactory（事件工厂）</li>
 * </ul>
 *
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>单例创建使用双重检查锁定</li>
 *   <li>关键状态变量使用volatile和synchronized保护</li>
 *   <li>事件处理在主线程执行</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 获取Scope实例
 * Scope scope = Scope.getInstance(context);
 *
 * // 初始化示波器
 * scope.initScope();
 *
 * // 启动采样
 * scope.setRun(true);
 *
 * // 判断是否正在运行
 * if(scope.isRun()){
 *     // 获取采样率
 *     double sampleRate = scope.getSampleRate();
 * }
 *
 * // 停止采样
 * scope.setRun(false);
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/13
 * @see Sample 采样管理
 * @see Display 显示管理
 * @see HorizontalAxis 水平轴管理
 * @see ChannelFactory 通道工厂
 * @see EventFactory 事件工厂
 * @see Observer 观察者接口
 */
public class Scope implements Observer {  // 实现Observer接口，接收事件通知

    // ==================== 常量定义 ====================

    /**
     * 日志标签
     *
     * <p><b>用途：</b>用于Log输出时标识来源，方便调试和日志过滤
     */
    private static final String TAG="Scope";

    /**
     * FPGA类型：无
     *
     * <p><b>业务含义：</b>表示FPGA未加载或加载失败的状态
     */
    public static final int TYPE_NONE = FPGABoot.FPGA_TYPE_NONE;

    /**
     * FPGA类型：正常模式
     *
     * <p><b>业务含义：</b>标准示波器模式，支持常规采样和触发功能
     */
    public static final int TYPE_NORMAL = FPGABoot.FPGA_TYPE_NORMAL;

    /**
     * FPGA类型：文本模式
     *
     * <p><b>业务含义：</b>串行总线解码模式，支持I2C/SPI/CAN等协议解码
     */
    public static final int TYPE_TXT = FPGABoot.FPGA_TYPE_TXT;

    // ==================== 静态初始化块 ====================

    /**
     * 加载本地库
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>在类加载时自动执行</li>
     *   <li>加载名为"scope-lib"的本地库（.so文件）</li>
     *   <li>本地库包含性能关键的C/C++代码</li>
     * </ul>
     *
     * <p><b>本地库功能：</b>
     * <ul>
     *   <li>波形数据处理算法</li>
     *   <li>FFT快速傅里叶变换</li>
     *   <li>插值算法</li>
     *   <li>测量计算</li>
     * </ul>
     */
    static {
        System.loadLibrary("scope-lib");  // 加载本地库，必须在类使用前完成
    }

    // ==================== 单例实例 ====================

    /**
     * 单例实例
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>全局唯一的Scope实例</li>
     *   <li>使用volatile保证多线程可见性</li>
     *   <li>延迟初始化，首次调用getInstance时创建</li>
     * </ul>
     *
     * <p><b>线程安全：</b>volatile关键字确保多线程环境下的可见性，防止指令重排序
     */
    private static volatile Scope instance = null;

    /**
     * 获取单例实例（已初始化后使用）
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>直接返回已创建的实例</li>
     *   <li>不进行初始化检查</li>
     *   <li>适用于已确保实例存在的场景</li>
     * </ul>
     *
     * @return Scope单例实例，未初始化时返回null
     */
    public static Scope getInstance(){
        return instance;  // 直接返回实例引用
    }

    /**
     * 获取单例实例（带初始化）
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>使用双重检查锁定（Double-Check Locking）保证线程安全</li>
     *   <li>首次调用时创建实例</li>
     *   <li>避免每次调用都进行同步，提高性能</li>
     * </ul>
     *
     * <p><b>实现原理：</b>
     * <pre>
     * 第一次检查：避免不必要的同步开销
     * 同步块：确保只有一个线程能创建实例
     * 第二次检查：防止其他线程在等待锁期间实例已被创建
     * </pre>
     *
     * @param context Android上下文，用于初始化硬件和资源
     * @return Scope单例实例
     */
    public static Scope getInstance(Context context) {
        // 第一次检查：如果实例已存在，直接返回，避免同步开销
        if (instance == null) {
            // 同步块：确保线程安全，只有一个线程能进入
            synchronized (Scope.class) {
                // 第二次检查：防止其他线程在等待锁期间已创建实例
                if (instance == null && context != null) {
                    // 创建单例实例，传入Android上下文
                    instance = new Scope(context);
                }
            }
        }
        // 返回单例实例
        return instance;
    }

    // ==================== FPGA状态静态变量 ====================

    /**
     * FPGA时钟输入状态
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>true：FPGA使用外部时钟输入</li>
     *   <li>false：FPGA使用内部时钟</li>
     * </ul>
     *
     * <p><b>使用场景：</b>多台示波器同步采样时，使用统一的外部时钟源
     */
    public volatile static boolean fpgaClockInStatus = false;

    /**
     * FPGA时钟输出状态
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>true：FPGA输出时钟给外部设备</li>
     *   <li>false：FPGA不输出时钟</li>
     * </ul>
     *
     * <p><b>使用场景：</b>多台示波器同步采样时，作为主时钟源
     */
    public volatile static boolean fpgaClockOutStatus = false;

    /**
     * FPGA温度1
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>FPGA芯片内部温度传感器读数</li>
     *   <li>单位：摄氏度</li>
     *   <li>用于过热保护</li>
     * </ul>
     */
    public volatile static int fpgaTemperature1 = 0;

    /**
     * FPGA温度2
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>FPGA芯片另一个温度传感器读数</li>
     *   <li>单位：摄氏度</li>
     *   <li>用于过热保护</li>
     * </ul>
     */
    public volatile static int fpgaTemperature2 = 0;

    /**
     * 风扇转速数组
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储多个风扇的转速</li>
     *   <li>单位：RPM（转/分钟）</li>
     *   <li>数组大小：4个风扇</li>
     * </ul>
     */
    public volatile static int [] fanSpeed = {0,0,0,0};

    /**
     * FPGA版本号
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>FPGA固件版本号</li>
     *   <li>用于功能兼容性判断</li>
     * </ul>
     */
    public static int fpgaVer = 0;

    /**
     * FPGA DNA
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>FPGA芯片的唯一标识码</li>
     *   <li>用于设备识别和授权</li>
     * </ul>
     */
    public static long fpgaDna = 0;

    // ==================== 成员变量 ====================

    /**
     * Android上下文
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于访问系统资源和服务</li>
     *   <li>用于加载资源文件</li>
     *   <li>用于初始化硬件抽象层</li>
     * </ul>
     */
    private Context mContext;

    /**
     * 硬件抽象层
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>提供硬件访问的统一接口</li>
     *   <li>封装底层硬件差异</li>
     *   <li>管理硬件版本和配置</li>
     * </ul>
     */
    private Hardware mHardware;

    /**
     * 通道工厂
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>管理所有通道（物理/数学/参考/串行）</li>
     *   <li>提供通道创建和访问接口</li>
     *   <li>协调通道间的交互</li>
     * </ul>
     */
    private ChannelFactory mChannelFactory;

    /**
     * 采样管理
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>管理采样状态（运行/停止/单次）</li>
     *   <li>管理采样类型（普通/峰值/平均/包络）</li>
     *   <li>管理存储深度配置</li>
     * </ul>
     */
    private Sample sample;

    /**
     * 段采样管理
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>管理段采样模式</li>
     *   <li>记录每段采样的时间戳</li>
     *   <li>支持历史波形回放</li>
     * </ul>
     */
    private SegmentSample segmentSample;

    /**
     * 事件工厂
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>管理事件订阅和发送</li>
     *   <li>实现模块间的松耦合通信</li>
     *   <li>支持观察者模式</li>
     * </ul>
     */
    private EventFactory eventFactory;

    /**
     * 显示管理
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>管理显示模式（普通/Zoom/XY/滚屏）</li>
     *   <li>管理波形绘制方式</li>
     *   <li>管理持久显示和余辉</li>
     * </ul>
     */
    private Display display;

    /**
     * 水平轴管理
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>管理时基档位</li>
     *   <li>管理时间位置</li>
     *   <li>计算采样率和存储深度</li>
     * </ul>
     */
    private HorizontalAxis horizontalAxis;

    /**
     * FPGA加载管理
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>加载FPGA固件</li>
     *   <li>切换FPGA工作模式</li>
     *   <li>管理FPGA状态</li>
     * </ul>
     */
    private FPGABoot fpgaBoot;

    /**
     * 数据工厂
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>管理波形数据保存</li>
     *   <li>支持多种格式（BIN/CSV）</li>
     *   <li>管理会话恢复</li>
     * </ul>
     */
    private DataFactory dataFactory;

    /**
     * 触发工厂
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>管理触发配置</li>
     *   <li>支持多种触发类型</li>
     *   <li>协调触发状态</li>
     * </ul>
     */
    private TriggerFactory triggerFactory;

    /**
     * 测量服务
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>提供自动测量功能</li>
     *   <li>计算各种波形参数</li>
     *   <li>支持统计测量</li>
     * </ul>
     */
    private MeasureService measureService;

    /**
     * 数学服务
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>提供数学运算功能</li>
     *   <li>支持加减乘除、FFT等运算</li>
     *   <li>管理数学通道</li>
     * </ul>
     */
    private MathService mathService;

    /**
     * 校准寄存器
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储校准参数</li>
     *   <li>管理垂直/水平校准</li>
     *   <li>支持自校准功能</li>
     * </ul>
     */
    private CabteRegister cabteRegister;

    /**
     * 示波器冻结状态
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>保存停止时的波形数据</li>
     *   <li>记录停止时的配置状态</li>
     *   <li>支持波形回放和分析</li>
     * </ul>
     */
    private ScopeFrozen scopeFrozen;

    /**
     * XY显示服务
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>管理XY模式显示</li>
     *   <li>计算XY波形数据</li>
     *   <li>支持李萨如图形</li>
     * </ul>
     */
    private DisplayXYService xyService;

    /**
     * 数据库助手
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>管理数据库操作</li>
     *   <li>存储探头配置</li>
     *   <li>存储校准数据</li>
     * </ul>
     */
    private DBHelper dbHelper;

    /**
     * ADC接口
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>提供ADC配置接口</li>
     *   <li>管理ADC通道映射</li>
     *   <li>获取ADC参数</li>
     * </ul>
     */
    private IADC adc;

    // ==================== 构造方法 ====================

    /**
     * 私有构造函数（单例模式）
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>私有化构造函数，防止外部直接创建实例</li>
     *   <li>初始化硬件抽象层</li>
     *   <li>初始化数据库助手</li>
     *   <li>分配设备资源</li>
     *   <li>初始化数学表达式OpenCL</li>
     * </ul>
     *
     * <p><b>初始化流程：</b>
     * <pre>
     * 1. 保存Android上下文引用
     * 2. 初始化硬件抽象层（访问底层硬件）
     * 3. 初始化数据库助手（存储配置和探头信息）
     * 4. 分配硬件设备资源（内存映射、DMA等）
     * 5. 初始化数学表达式OpenCL（GPU加速数学运算）
     * </pre>
     *
     * @param context Android上下文，用于访问系统服务和资源
     */
    private Scope(Context context){
        mContext = context;  // 保存Android上下文引用，后续用于访问资源
        mHardware = Hardware.getInstance(mContext);  // 初始化硬件抽象层，获取硬件访问接口
        dbHelper = new DBHelper(context);  // 创建数据库助手，用于存储配置和探头信息
        DeviceFactory.allocDevice(context);  // 分配硬件设备资源，建立内存映射和DMA通道
        MathExt.initMathExprOpenCL(context.getAssets());  // 初始化数学表达式OpenCL，启用GPU加速
    }

    // ==================== 初始化方法 ====================

    /**
     * 初始化示波器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>初始化各子模块实例</li>
     *   <li>加载探头配置</li>
     *   <li>注册事件监听</li>
     * </ul>
     *
     * <p><b>初始化顺序（重要）：</b>
     * <pre>
     * 1. ScopeFrozen (冻结状态) - 最先初始化，用于保存状态
     * 2. ChannelFactory (通道工厂) - 创建和管理所有通道
     * 3. FPGABoot (FPGA加载) - 加载FPGA固件
     * 4. DataFactory (数据工厂) - 管理数据保存
     * 5. EventFactory (事件工厂) - 管理事件通信
     * 6. Sample (采样管理) - 管理采样状态
     * 7. SegmentSample (段采样) - 管理段采样
     * 8. TriggerFactory (触发工厂) - 管理触发配置
     * 9. Display (显示管理) - 管理显示模式
     * 10. HorizontalAxis (水平轴) - 管理时基
     * 11. MeasureService (测量服务) - 提供测量功能
     * 12. MathService (数学服务) - 提供数学运算
     * 13. DisplayXYService (XY显示) - 管理XY模式
     * 14. ADC配置 - 获取ADC接口
     * 15. 探头加载 - 加载探头配置文件
     * 16. 事件监听注册 - 注册感兴趣的事件
     * </pre>
     *
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>初始化顺序很重要，有依赖关系的模块必须按顺序初始化</li>
     *   <li>必须在UI线程调用</li>
     *   <li>必须在getInstance(context)之后调用</li>
     * </ul>
     */
    public void initScope(){
        // 初始化冻结状态管理器，用于保存停止时的波形和配置
        scopeFrozen = ScopeFrozen.getInstance();

        // 初始化通道工厂，管理所有通道（物理/数学/参考/串行）
        mChannelFactory = ChannelFactory.getInstance();

        // 初始化FPGA加载管理器，负责加载FPGA固件
        fpgaBoot = FPGABoot.getInstance(mContext);

        // 初始化数据工厂，管理波形数据保存
        dataFactory = DataFactory.getInstance();

        // 初始化事件工厂，管理事件订阅和发送
        eventFactory = EventFactory.getInstance();

        // 初始化采样管理器，管理采样状态和配置
        sample = Sample.getInstance();

        // 初始化段采样管理器，管理段采样模式
        segmentSample = SegmentSample.getInstance();

        // 初始化触发工厂，管理触发配置
        triggerFactory = TriggerFactory.getInstance();

        // 初始化显示管理器，管理显示模式
        display = Display.getInstance();

        // 初始化水平轴管理器，管理时基
        horizontalAxis = HorizontalAxis.getInstance();

        // 初始化测量服务，提供自动测量功能
        measureService = MeasureService.getInstance();

        // 初始化数学服务，提供数学运算功能
        mathService = MathService.getInstance();

        // 初始化XY显示服务，管理XY模式
        xyService = DisplayXYService.getInstance();

        // 获取ADC接口，用于配置ADC参数
        adc = HwConfig.getInstance().getAdc();

        // 加载探头配置文件，初始化探头列表
        loadProbe();

        // 注册事件监听器，监听FPGA加载完成事件
        EventFactory.addEventObserver(EventFactory.EVENT_FPGA_LOAD_OK,this);

        // 注册事件监听器，监听通道垂直刻度变化事件
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE,this);

        // 注册事件监听器，监听示波器状态变化事件
        EventFactory.addEventObserver(EventFactory.EVENT_SCOPE_STATE,this);

        // 初始化校准寄存器，加载校准参数
        cabteRegister = CabteRegister.getInstance(mContext);
    }

    /**
     * 加载探头配置
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从资源文件加载探头配置</li>
     *   <li>解析JSON格式的探头信息</li>
     *   <li>初始化探头工厂</li>
     * </ul>
     *
     * <p><b>探头配置内容：</b>
     * <ul>
     *   <li>探头型号和名称</li>
     *   <li>探头比例（1X, 10X, 100X等）</li>
     *   <li>探头带宽</li>
     *   <li>探头衰减系数</li>
     * </ul>
     */
    private void loadProbe(){
        // 获取探头工厂单例实例
        ProbeFactory probeFactory = ProbeFactory.getInstance();

        // 设置数据库助手，用于存储自定义探头配置
        probeFactory.setDbHelper(dbHelper);

        // 从资源文件加载探头配置，解析JSON并创建探头对象
        probeFactory.parseJsontoProbe(ProbeUtils.loadProbe(mContext));
    }

    // ==================== 硬件控制方法 ====================

    /**
     * 进入待机模式
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>硬件进入待机状态，降低功耗</li>
     *   <li>重置FPGA，释放硬件资源</li>
     * </ul>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>长时间不使用示波器时</li>
     *   <li>电池电量低时</li>
     *   <li>系统休眠时</li>
     * </ul>
     */
    public void standby(){
        mHardware.standby();  // 硬件进入待机状态，降低功耗
        fpgaBoot.reset();  // 重置FPGA，释放硬件资源
    }

    /**
     * 获取硬件版本
     *
     * <p><b>功能说明：</b>返回示波器硬件版本号，用于功能兼容性判断
     *
     * @return 硬件版本号
     */
    public int getHwVersion(){
        return mHardware.getHwVersion();  // 从硬件抽象层获取版本号
    }

    /**
     * 获取通道数量
     *
     * <p><b>功能说明：</b>返回示波器支持的物理通道数量（如2通道或4通道）
     *
     * @return 通道数量
     */
    public int getChNum(){
        return mHardware.getChNum();  // 从硬件抽象层获取通道数
    }

    // ==================== 运行状态控制方法 ====================

    /**
     * 恢复示波器运行
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据FPGA状态决定是否重新加载</li>
     *   <li>如果之前是运行状态，恢复运行</li>
     * </ul>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>从待机模式唤醒</li>
     *   <li>从暂停状态恢复</li>
     * </ul>
     */
    public void resume(){
        // 检查FPGA是否已加载，未加载则需要重新加载
        boolean bLoad = fpgaBoot.getFpgaType() ==FPGABoot.FPGA_TYPE_NONE;

        // 调用带参数的resume方法
        resume(bLoad);

        // 如果之前是运行状态，恢复采样
        if(isRun()){
            setRun(true);  // 恢复采样
            ScopeMessage.runResume();  // 通知FPGA恢复运行
        }
    }

    /**
     * 恢复示波器运行
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>重新加载FPGA或恢复探头状态</li>
     *   <li>清除暂停状态</li>
     * </ul>
     *
     * @param run true表示重新加载FPGA，false表示仅恢复探头
     */
    public void resume(boolean run){
        if(run) {
            // 重新加载FPGA固件
            fpgaBoot.resume();
        }else {
            // 仅恢复探头状态
            resumeProbe();
            // 清除暂停标志
            ScopeMessage.getInstance().touchPause();
        }
    }

    /**
     * 恢复探头状态
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>遍历所有动态通道</li>
     *   <li>恢复每个探头的配置</li>
     * </ul>
     */
    public void resumeProbe() {
        Channel channel;  // 通道引用
        int maxIdx = ChannelFactory.getMaxChIdx();  // 获取最大通道索引

        // 遍历所有动态通道（物理通道）
        for(int i=ChannelFactory.CH1;i<maxIdx;i++){
            // 获取动态通道实例
            channel = ChannelFactory.getDynamicChannel(i);

            // 检查通道是否存在
            if(channel != null){
                // 获取通道连接的探头
                BaseProbe baseProbe = channel.getProbe();

                // 检查探头是否存在
                if(baseProbe != null){
                    // 恢复探头配置（如自动识别、衰减比例等）
                    baseProbe.resume();
                }
            }
        }
    }

    /**
     * 获取自动探头比例
     *
     * <p><b>功能说明：</b>从硬件读取探头自动识别的比例
     *
     * @param chIdx 通道索引
     * @return 探头比例（如1.0, 10.0, 100.0等）
     */
    public double getAutoProbeRate(int chIdx){
        return mHardware.getChProbeRate(chIdx);  // 从硬件抽象层读取探头比例
    }

    /**
     * 暂停示波器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>暂停采样，但保持当前波形</li>
     *   <li>不同于停止，暂停可以快速恢复</li>
     * </ul>
     */
    public void pause(){
        ScopeMessage.runPause();  // 通知FPGA暂停采样
    }

    /**
     * 刷新显示
     *
     * <p><b>功能说明：</b>触发波形重绘，用于强制更新显示
     */
    public void Refresh(){
        // 发送绘制类型事件，触发UI重绘
        EventFactory.getInstance().sendEvent(EventFactory.EVENT_DRAW_TYPE);
    }

    /**
     * 设置运行状态
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制示波器的运行/停止状态</li>
     *   <li>自动退出自动模式和单次模式</li>
     *   <li>处理滚屏模式下的特殊逻辑</li>
     * </ul>
     *
     * <p><b>处理流程：</b>
     * <pre>
     * 1. 如果在自动模式，退出自动模式
     * 2. 如果在单次模式，退出单次模式
     * 3. 设置采样运行状态
     * 4. 如果启动运行：
     *    a. 滚屏模式下退出Zoom模式
     *    b. 发送时间位置事件
     *    c. 清除波形
     * </pre>
     *
     * @param bRun true运行，false停止
     */
    public void setRun(boolean bRun){
        // 如果在自动模式，先退出自动模式
        if(isAuto()){
            Auto(false);
        }

        // 如果在单次模式，先退出单次模式
        if(isSingle()){
            setSingle(false);
        }

        // 获取冻结状态实例
        ScopeFrozen scopeFrozen = ScopeFrozen.getInstance();

        // 获取滚屏模式标志
        boolean bRool = scopeFrozen.isRool();

        // 设置采样运行状态
        sample.setRunSample(bRun);

        // 如果启动运行
        if(bRun){
            // 如果在滚屏模式
            if(isInScrollMode()) {
                // 如果在Zoom模式，退出Zoom（滚屏模式不支持Zoom）
                if(isZoom()){
                    setZoom(false);
                    EventFactory.sendEvent(EventFactory.EVENT_DISPLAY_ZOOM,true);
                }
                // 发送时间位置事件，重置时间位置
                EventFactory.sendEvent(EventFactory.EVENT_TIME_POS,true);
            }

            // 清除波形数据
            clearWave();

            // 如果是滚屏模式，再次发送时间位置事件
            if(bRool){
                EventFactory.sendEvent(EventFactory.EVENT_TIME_POS,true);
            }
        }
    }

    /**
     * 判断是否正在运行
     *
     * <p><b>功能说明：</b>检查示波器是否正在采样
     *
     * @return true表示正在运行
     */
    public boolean isRun(){
        return isRun(false);  // 默认不检查FPGA状态
    }

    /**
     * 判断是否正在运行（可指定是否检查FPGA状态）
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查采样状态</li>
     *   <li>瞬态模式下可返回false</li>
     * </ul>
     *
     * <p><b>瞬态模式说明：</b>
     * <ul>
     *   <li>SAMPLE_TRANSIENT：瞬态采样中</li>
     *   <li>SAMPLE_TRANSIENT_DIAPLAY：瞬态显示中</li>
     *   <li>这两种状态虽然采样器在工作，但不应视为"运行"状态</li>
     * </ul>
     *
     * @param bFpga 是否检查FPGA状态（true时瞬态模式返回false）
     * @return true表示正在运行
     */
    public boolean isRun(boolean bFpga){
        // 获取采样状态
        boolean bSample = sample.isRunSample();

        // 如果需要检查FPGA状态且采样正在运行
        if(bFpga && bSample){
            // 根据采样状态判断
            switch (sample.getSampleState()){
                case Sample.SAMPLE_TRANSIENT:  // 瞬态采样中
                case Sample.SAMPLE_TRANSIENT_DIAPLAY:  // 瞬态显示中
                    bSample = false;  // 视为非运行状态
                    break;
            }
        }

        return bSample;
    }

    // ==================== Zoom模式控制方法 ====================

    /**
     * 设置Zoom模式
     *
     * <p><b>功能说明：</b>启用或禁用Zoom（放大）模式
     *
     * @param bEnable true启用，false禁用
     */
    public void setZoom(boolean bEnable){
        setZoom(bEnable,true);  // 默认修改时基
    }

    /**
     * 设置Zoom标志
     *
     * <p><b>功能说明：</b>设置Zoom模式的标志位，用于控制Zoom窗口显示
     *
     * @param flags Zoom标志位
     */
    public void setZoomFlags(int flags) {
        display.setZoomFlags(flags);  // 委托给Display设置标志
    }

    /**
     * 设置Zoom模式
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>启用Zoom模式时显示缩略图和放大图</li>
     *   <li>禁用时恢复普通显示</li>
     * </ul>
     *
     * @param bEnable true启用，false禁用
     * @param bModify 是否修改时基（true时自动调整时基）
     */
    public void setZoom(boolean bEnable,boolean bModify){
        display.setZoom(bEnable, bModify);  // 委托给Display设置Zoom模式
    }

    /**
     * 判断是否为Zoom模式
     *
     * <p><b>功能说明：</b>检查当前是否处于Zoom（放大）模式
     *
     * @return true表示Zoom模式
     */
    public boolean isZoom(){
        return display.isZoom();  // 从Display获取Zoom状态
    }

    // ==================== 自动模式控制方法 ====================

    /**
     * 设置自动模式
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>自动设置示波器参数（垂直档位、时基、触发电平）</li>
     *   <li>自动退出单次模式</li>
     *   <li>自动启动运行</li>
     * </ul>
     *
     * <p><b>处理流程：</b>
     * <pre>
     * 1. 如果启用自动模式：
     *    a. 退出单次模式
     *    b. 如果未运行，启动运行
     * 2. 设置自动模式标志
     * 3. 如果启用，确保采样正在运行
     * </pre>
     *
     * @param bAuto true启用，false禁用
     */
    public void Auto(boolean bAuto){
        // 如果启用自动模式
        if(bAuto){
            // 如果在单次模式，退出单次模式
            if(isSingle()){
                setSingle(false);
            }

            // 如果未运行，启动运行
            if(!isRun()){
                setRun(true);
            }
        }

        // 设置自动模式标志
        Auto.getInstance().setAuto(bAuto);

        // 如果启用自动模式，确保采样正在运行
        if(bAuto){
            sample.setRunSample(true);
        }
    }

    /**
     * 判断是否为自动模式
     *
     * <p><b>功能说明：</b>检查当前是否处于自动设置模式
     *
     * @return true表示自动模式
     */
    public boolean isAuto(){
        return Auto.getInstance().isAuto();  // 从Auto获取自动模式状态
    }

    // ==================== 单次模式控制方法 ====================

    /**
     * 设置单次采样模式
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>启用单次触发模式</li>
     *   <li>触发一次后自动停止</li>
     *   <li>自动退出自动模式</li>
     *   <li>滚屏模式下调整时间位置</li>
     * </ul>
     *
     * <p><b>处理流程：</b>
     * <pre>
     * 1. 如果启用单次模式：
     *    a. 退出自动模式
     *    b. 如果在滚屏模式，发送时间位置事件
     *    c. 清除波形
     * 2. 设置单次模式标志
     * </pre>
     *
     * @param bSingle true启用，false禁用
     */
    public void setSingle(boolean bSingle){
        // 如果启用单次模式
        if(bSingle){
            // 如果在自动模式，退出自动模式
            if(isAuto()){
                Auto(false);
            }

            // 如果在滚屏模式，发送时间位置事件
            if(isInScrollMode()) {
                EventFactory.sendEvent(EventFactory.EVENT_TIME_POS);
            }

            // 清除波形数据
            clearWave();
        }

        // 输出日志
        Logger.d(TAG,"Single:"+ bSingle );

        // 设置单次模式标志
        sample.setSingle(bSingle);
    }

    /**
     * 判断是否为单次模式
     *
     * <p><b>功能说明：</b>检查当前是否处于单次采样模式
     *
     * @return true表示单次模式
     */
    public boolean isSingle(){
        return sample.isSingle();  // 从Sample获取单次模式状态
    }

    // ==================== 显示模式判断方法 ====================

    /**
     * 判断是否为XY模式
     *
     * <p><b>功能说明：</b>检查当前是否处于X-Y显示模式
     *
     * <p><b>XY模式说明：</b>
     * <ul>
     *   <li>一个通道作为X轴，另一个通道作为Y轴</li>
     *   <li>用于显示李萨如图形</li>
     *   <li>用于相位差测量</li>
     * </ul>
     *
     * @return true表示XY模式
     */
    public boolean isInXYMode(){
        return display.isXYMode();  // 从Display获取XY模式状态
    }

    /**
     * 判断是否为滚屏模式
     *
     * <p><b>判断条件：</b>时基≥100ms/div且启用滚屏显示
     *
     * <p><b>滚屏模式说明：</b>
     * <ul>
     *   <li>波形从右向左滚动显示</li>
     *   <li>用于观察慢速变化的信号</li>
     *   <li>类似传统纸带记录仪</li>
     * </ul>
     *
     * @return true表示滚屏模式
     */
    public boolean isInScrollMode(){
        // 判断条件：时基≥100ms/div 且 启用滚屏显示
        return (horizontalAxis.isGreater100ms() && display.isRoll());
    }

    /**
     * 判断是否为慢时基模式
     *
     * <p><b>判断条件：</b>时基≥100ms/div且未启用滚屏显示
     *
     * <p><b>慢时基模式说明：</b>
     * <ul>
     *   <li>时基较慢，但不使用滚屏显示</li>
     *   <li>波形正常触发和显示</li>
     *   <li>适用于低频信号的常规测量</li>
     * </ul>
     *
     * @return true表示慢时基模式
     */
    public boolean isInSlowScaleMode(){
        // 判断条件：时基≥100ms/div 且 未启用滚屏显示
        return (horizontalAxis.isGreater100ms() && (!display.isRoll()));
    }

    /**
     * 判断是否为文本模式
     *
     * <p><b>功能说明：</b>检查FPGA是否工作在串行总线解码模式
     *
     * <p><b>文本模式说明：</b>
     * <ul>
     *   <li>支持I2C、SPI、CAN、LIN等协议解码</li>
     *   <li>显示解码后的文本数据</li>
     *   <li>用于嵌入式系统调试</li>
     * </ul>
     *
     * @return true表示文本模式
     */
    public boolean isSerialText(){
        // 检查FPGA类型是否为文本模式
        return fpgaBoot.getFpgaType() == FPGABoot.FPGA_TYPE_TXT;
    }

    /**
     * 获取FPGA类型
     *
     * <p><b>功能说明：</b>返回当前FPGA的工作模式类型
     *
     * @return FPGA类型（TYPE_NONE/TYPE_NORMAL/TYPE_TXT）
     */
    public int getType(){
        return fpgaBoot.getFpgaType();  // 从FPGABoot获取类型
    }

    /**
     * 设置FPGA类型
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>切换FPGA工作模式</li>
     *   <li>重新加载FPGA固件</li>
     *   <li>清除波形数据</li>
     * </ul>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>从普通模式切换到串行解码模式</li>
     *   <li>从串行解码模式切换回普通模式</li>
     * </ul>
     *
     * @param type FPGA类型（TYPE_NORMAL或TYPE_TXT）
     */
    public void setType(int type){
        // 检查是否需要切换
        if(fpgaBoot.getFpgaType() != type) {
            Logger.d(TAG,"loadFpga!....");  // 输出日志
            fpgaBoot.loadFpga(type);  // 加载对应的FPGA固件
            clearWave();  // 清除波形数据
        }
    }

    // ==================== 通道采样管理方法 ====================

    /**
     * 获取采样通道数
     *
     * <p><b>功能说明：</b>统计当前参与采样的通道数量
     *
     * @param bRun 是否检查运行状态
     * @return 采样通道数
     */
    public int getChannelSampOnCnt(boolean bRun){
        return getChannelSampOnCnt(bRun,null);  // 调用完整版本
    }

    /**
     * 获取采样通道数
     *
     * <p><b>功能说明：</b>统计当前参与采样的通道数量
     *
     * @return 采样通道数
     */
    public int getChannelSampOnCnt(){
        return getChannelSampOnCnt(null);  // 调用带数组参数版本
    }

    /**
     * 获取采样通道数
     *
     * <p><b>功能说明：</b>统计参与采样的通道数量，并返回通道使能数组
     *
     * @param ch_en 通道使能数组（输出参数，记录每个通道是否参与采样）
     * @return 采样通道数
     */
    public int getChannelSampOnCnt(boolean [] ch_en){
        return getChannelSampOnCnt(isRun(),ch_en);  // 根据运行状态统计
    }

    /**
     * 获取采样通道数
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>统计参与采样的通道数量</li>
     *   <li>考虑物理通道、串行通道、数学通道</li>
     *   <li>考虑触发源通道</li>
     * </ul>
     *
     * <p><b>计算逻辑：</b>
     * <pre>
     * 1. 遍历所有动态通道
     * 2. 判断每个通道是否参与采样
     * 3. 统计参与采样的通道数
     * 4. ADC可能对通道数进行调整（如交错采样）
     * </pre>
     *
     * @param bRun 是否为运行状态
     * @param ch_en 通道使能数组（输出参数，可为null）
     * @return 采样通道数
     */
    public int getChannelSampOnCnt(boolean bRun,boolean [] ch_en){
        int nums = 0;  // 采样通道计数器
        boolean[] sel = new boolean[ChannelFactory.CH_CNT];  // 通道选择数组
        int maxIdx = ChannelFactory.getMaxChIdx();  // 获取最大通道索引

        // 遍历所有动态通道
        for(int i=ChannelFactory.CH1;i<maxIdx;i++){
            // 判断通道是否参与采样
            sel[i] = isChannelInSample(i,bRun);
            if(sel[i]){
                nums++;  // 计数增加
            }
        }

        // ADC可能对通道数进行调整（如交错采样、通道复用等）
        nums = adc.getSampleChannel(sel);

        // 如果需要输出通道使能数组
        if(ch_en != null){
            // 复制通道选择状态到输出数组
            System.arraycopy(sel, 0, ch_en, 0, sel.length);
        }

        return nums;  // 返回采样通道数
    }

    /**
     * 判断通道是否参与采样
     *
     * <p><b>功能说明：</b>检查指定通道是否参与当前采样
     *
     * @param chIdx 通道索引
     * @return true表示参与采样
     */
    public boolean isChannelInSample(int chIdx){
        // 如果是动态通道，检查运行状态
        if(ChannelFactory.isDynamicCh(chIdx)){
            return isChannelInSample(chIdx,isRun());
        }else {
            return false;  // 非动态通道不参与采样
        }
    }

    /**
     * 判断通道是否参与采样
     *
     * <p><b>判断逻辑：</b>
     * <ul>
     *   <li>运行状态：检查通道开启状态、触发源、串行通道、数学通道</li>
     *   <li>停止状态：检查冻结状态中的采样通道</li>
     * </ul>
     *
     * <p><b>特殊考虑：</b>
     * <ul>
     *   <li>滚屏模式下，触发源通道不参与采样</li>
     *   <li>串行通道可能使用物理通道作为输入</li>
     *   <li>数学通道可能依赖物理通道的数据</li>
     * </ul>
     *
     * @param chIdx 通道索引
     * @param bRun 是否为运行状态
     * @return true表示参与采样
     */
    public boolean isChannelInSample(int chIdx,boolean bRun){
        boolean bInSample = false;  // 初始化为不参与采样

        // 如果是运行状态
        if(bRun){
            // 检查是否为有效的动态通道
            if(ChannelFactory.isDynamicCh(chIdx)
                    && chIdx < ChannelFactory.getMaxChIdx()){
                // 如果通道已开启，参与采样
                if(ChannelFactory.isChOpen(chIdx)){
                    bInSample = true;
                }else {
                    // 通道未开启，但可能是触发源
                    // 滚屏模式下触发源不参与采样
                    if(!isInScrollMode()
                            && TriggerFactory.isTriggerSource(chIdx)) {
                        bInSample = true;
                    }
                    else {
                        // 检查串行通道是否使用此物理通道
                        SerialChannel serialChannel;
                        int maxIdx = ChannelFactory.getMaxSerialIdx();

                        // 遍历所有串行通道
                        for (int i = ChannelFactory.S1; i < maxIdx; i++) {
                            serialChannel = ChannelFactory.getSerialChannel(i);

                            // 如果串行通道开启且使用此物理通道
                            if (serialChannel != null && serialChannel.isOpen()
                                    && serialChannel.getBus().isChInSample(chIdx)) {
                                bInSample = true;
                                break;  // 找到一个即可
                            }
                        }

                        // 如果串行通道未使用，检查数学通道
                        if(!bInSample) {
                            MathChannel mathChannel;
                            maxIdx = ChannelFactory.getMaxMathIdx();

                            // 遍历所有数学通道
                            for(int i=ChannelFactory.MATH1;i<maxIdx;i++){
                                mathChannel = ChannelFactory.getMathChannel(i);

                                // 如果数学通道开启且依赖此物理通道
                                if (mathChannel != null
                                        && mathChannel.isOpen()
                                        && mathChannel.isChInSample(chIdx)) {
                                    bInSample = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            // 停止状态，从冻结状态获取
            bInSample = ScopeFrozen.getInstance().isChSamped(chIdx);
        }

        return bInSample;  // 返回判断结果
    }

    // ==================== 采样率计算方法 ====================

    /**
     * 获取最大采样时钟频率
     *
     * <p><b>计算公式：</b>
     * <pre>
     * maxSampClk = ADC数量 × ADC最大时钟 / 采样通道数
     * </pre>
     *
     * <p><b>说明：</b>
     * <ul>
     *   <li>ADC数量：示波器中ADC芯片的数量</li>
     *   <li>ADC最大时钟：单个ADC的最大采样时钟</li>
     *   <li>采样通道数：当前参与采样的通道数</li>
     * </ul>
     *
     * @return 最大采样时钟频率（Hz）
     */
    public long maxSampClk(){
        long n = HwConfig.getInstance().getAdcNums();  // 获取ADC数量
        long m = getChannelSampOnCnt();  // 获取采样通道数

        // 确保通道数不小于ADC数量（交错采样）
        if(m < n){
            m = n;
        }

        // 计算最大采样时钟：ADC数量 × ADC最大时钟 / 通道数
        return n * adc.getMaxAdInClk() / m;
    }

    /**
     * 获取采样率
     *
     * <p><b>功能说明：</b>返回当前采样率
     *
     * @return 采样率（Hz）
     */
    public double getSampleRate(){
        return getSampleRate(isRun());  // 根据运行状态获取
    }

    /**
     * 获取采样率
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>运行状态：根据存储深度和时基计算</li>
     *   <li>停止状态：从冻结状态获取</li>
     * </ul>
     *
     * @param bRun 是否为运行状态
     * @return 采样率（Hz）
     */
    public double getSampleRate(boolean bRun){
        if(bRun){
            return sampFre_R();  // 运行状态下的采样率
        }
        return sampFre_S();  // 停止状态下的采样率
    }

    /**
     * 计算运行状态下的采样率
     *
     * <p><b>计算逻辑：</b>
     * <pre>
     * 1. 获取存储深度
     * 2. 计算理论采样率 = 存储深度 / (时基 × 屏幕格数)
     * 3. 限制不超过最大采样率
     * 4. 调整为整数分频（便于硬件实现）
     * </pre>
     *
     * <p><b>分频调整说明：</b>
     * <ul>
     *   <li>硬件时钟分频器只能进行整数分频</li>
     *   <li>分频系数必须是2或5的倍数</li>
     *   <li>找到最接近的分频系数</li>
     * </ul>
     *
     * @return 采样率（Hz）
     */
    private double sampFre_R(){
        // 获取存储深度
        int memdepth = sample.getSampleMemDepth();

        // 对齐到屏幕宽度（像素对齐）
        memdepth /= ScopeBase.getWidth();
        memdepth *= ScopeBase.getWidth();

        // 计算理论采样率：存储深度 / (时基 × 屏幕格数)
        long f1 = (long)(memdepth/timeScale_mainBoard()/ScopeBase.getHorizonGridCnt() + 0.1);

        // 获取最大采样率
        long maxSampFre = (long)maxSampClk()*1000*1000L;

        // 如果理论采样率超过最大采样率
        if(f1 > maxSampFre){
            f1 = maxSampFre;  // 限制为最大采样率
        }
        else {
            // 调整为整数分频
            if(maxSampFre % f1 != 0) {
                // 计算分频系数
                int c = (int)(maxSampFre / f1);

                // 寻找最接近的有效分频系数（2或5的倍数）
                do {
                    if (c % 2 == 0 || c % 5 == 0) {
                        if (maxSampFre % c == 0) {
                            break;  // 找到有效分频系数
                        }
                    }
                    c++;  // 尝试下一个分频系数
                } while (true);

                // 计算实际采样率
                f1 = maxSampFre/c;
            }
        }

        return f1;  // 返回采样率
    }

    /**
     * 获取停止状态下的采样率
     *
     * <p><b>功能说明：</b>从冻结状态获取采样率
     *
     * @return 采样率（Hz）
     */
    private double sampFre_S(){
        return scopeFrozen.getSampFre();  // 从冻结状态获取
    }

    /**
     * 获取指定通道的采样率
     *
     * <p><b>功能说明：</b>不同通道可能有不同的采样率（如降采样）
     *
     * @param chIdx 通道索引
     * @return 采样率（Hz）
     */
    public static double getSampleRate(int chIdx){
        // 检查通道索引有效性
        if(ChannelFactory.isValidCh(chIdx))
            return ChannelFactory.getValidChannel(chIdx).getSampleRate();
        return 1.0;  // 无效通道返回1.0
    }

    // ==================== 屏幕数计算方法 ====================

    /**
     * 获取主窗口屏幕数
     *
     * <p><b>功能说明：</b>计算存储深度可以显示多少屏波形
     *
     * @return 屏幕数
     */
    public double screenNum_Main(){
        return screenNum_Main(isRun());  // 根据运行状态计算
    }

    /**
     * 获取主窗口屏幕数
     *
     * <p><b>计算公式：</b>
     * <pre>
     * 屏幕数 = 存储深度 / (采样率 × 一屏时间)
     * </pre>
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>屏幕数 > 1：可以左右平移查看历史波形</li>
     *   <li>屏幕数 = 1：刚好显示一屏</li>
     *   <li>屏幕数 < 1：存储深度不足一屏</li>
     * </ul>
     *
     * @param bRun 是否为运行状态
     * @return 屏幕数
     */
    public double screenNum_Main(boolean bRun){
        if(bRun){
            return screenNum_Main_R();  // 运行状态
        }
        return screenNum_Main_S();  // 停止状态
    }

    /**
     * 计算运行状态下的主窗口屏幕数
     *
     * @return 屏幕数
     */
    private double screenNum_Main_R(){
        double f = getSampleRate(true);  // 获取采样率
        double t1 = timeScale_mainBoard()*ScopeBase.getHorizonGridCnt();  // 一屏时间
        return (double) sample.getSampleMemDepth()/(f*t1);  // 计算
    }

    /**
     * 获取停止状态下的主窗口屏幕数
     *
     * @return 屏幕数
     */
    private double screenNum_Main_S(){
        double f = scopeFrozen.getSampFre();  // 从冻结状态获取采样率
        int timeId = horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD);  // 时基档位
        double t1 = horizontalAxis.getTimeScaleIdVal(timeId) * ScopeBase.getHorizonGridCnt();  // 一屏时间

        double num = scopeFrozen.getMemDepth()/(f*t1);  // 计算
        return num;
    }

    /**
     * 获取Zoom窗口屏幕数
     *
     * @return 屏幕数
     */
    public double screenNum_zoom(){
        return screenNum_zoom(isRun());  // 根据运行状态计算
    }

    /**
     * 获取Zoom窗口屏幕数
     *
     * <p><b>计算公式：</b>
     * <pre>
     * 屏幕数 = 主窗口时基 / Zoom窗口时基
     * </pre>
     *
     * <p><b>业务含义：</b>Zoom窗口相对于主窗口的放大倍数
     *
     * @param bRun 是否为运行状态
     * @return 屏幕数
     */
    public double screenNum_zoom(boolean bRun){
        if(bRun){
            return screenNum_zoom_R();  // 运行状态
        }
        return screenNum_zoom_S();  // 停止状态
    }

    /**
     * 计算运行状态下的Zoom窗口屏幕数
     *
     * @return 屏幕数
     */
    private double screenNum_zoom_R(){
        return timeScale_mainBoard()/timeScale_zoomBoard();  // 时基比值
    }

    /**
     * 获取停止状态下的Zoom窗口屏幕数
     *
     * @return 屏幕数
     */
    private double screenNum_zoom_S(){
        return screenNum_zoom_R();  // 与运行状态相同
    }

    // ==================== 时基获取方法 ====================

    /**
     * 获取主窗口时基值
     *
     * @return 时基值（秒/div）
     */
    public double timeScale_mainBoard(){
        return horizontalAxis.getTimeScaleIdVal(horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD));
    }

    /**
     * 获取Zoom窗口时基值
     *
     * @return 时基值（秒/div）
     */
    public double timeScale_zoomBoard(){
        return horizontalAxis.getTimeScaleIdVal(horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_LARGE));
    }

    /**
     * 获取主窗口一屏的时间
     *
     * @return 时间（100飞秒单位）
     */
    public long timeOneScreen_main(){
        long t1 = HorizontalAxis.Sto100FS(timeScale_mainBoard());  // 转换为100飞秒单位
        return t1 * ScopeBase.getHorizonGridCnt();  // 乘以屏幕格数
    }

    /**
     * 获取Zoom窗口一屏的时间
     *
     * @return 时间（100飞秒单位）
     */
    public long timeOneScreen_zoom(){
        long t1 = HorizontalAxis.Sto100FS(timeScale_zoomBoard());  // 转换为100飞秒单位
        return t1 * ScopeBase.getHorizonGridCnt();  // 乘以屏幕格数
    }

    // ==================== Zoom模式计算方法 ====================

    /**
     * 计算进入Zoom模式时缩略图的时基档位
     *
     * <p><b>计算逻辑：</b>
     * <pre>
     * 1. 计算单屏档位界限值 = 存储深度 / (采样率 × 屏幕格数)
     * 2. 找到满足存储深度对齐的档位
     * </pre>
     *
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>进入Zoom模式时，缩略图需要显示全部存储深度</li>
     *   <li>时基档位需要满足存储深度对齐到屏幕宽度</li>
     * </ul>
     *
     * @return 时基档位ID
     */
    public int enterZoom_SL_scale() {
        long zunSampDepth = zunMemDepth();  // 获取存储深度
        int id;
        double zunSampFre = getSampleRate();  // 获取采样率

        // 获取当前时基档位
        if(isRun()) {
            id = horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD);
        } else {
            id = scopeFrozen.getTimeScaleId();
        }

        // 计算单屏档位界限值
        double dScaleValue = zunSampDepth / (zunSampFre * ScopeBase.getHorizonGridCnt());

        Log.d(TAG,"dScaleValue:" + dScaleValue + ",zunSampDepth:" + zunSampDepth +",zunSampFre:" + zunSampFre);

        // 转换为时基档位ID
        int id_max = horizontalAxis.timeValtoTimeScaleId(dScaleValue);

        // 确保不超过界限
        if(id > id_max){
            id = id_max;
        }

        // 找到满足存储深度对齐的档位
        do {
            // 计算一屏的采样点数
            long n = (long) (horizontalAxis.getTimeScaleIdVal(id) * ScopeBase.getHorizonGridCnt() * zunSampFre + 0.1);

            // 检查是否对齐到屏幕宽度
            if (n % ScopeBase.getWidth() == 0) {
                break;  // 找到合适的档位
            }
            id--;  // 尝试更小的档位
        }while (true);

        return id;  // 返回时基档位ID
    }

    /**
     * 计算进入Zoom模式时缩略图的时间位置
     *
     * @return 时间位置
     */
    public long enterZoom_SL_timePos() {
        long timePos;

        // 获取当前时间位置
        if(isRun()){
            timePos = horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_STANDARD);
        }
        else {
            timePos = scopeFrozen.getTimePosOfView();
        }

        // 计算半屏时间的负值
        long halfTimeScreen_n = -timeOneScreen_main()/2;

        // 确保时间位置不超过范围
        if(timePos < halfTimeScreen_n)
            return halfTimeScreen_n;
        else
            return timePos;
    }

    // ==================== 垂直位置转换方法 ====================

    /**
     * 垂直位置转换：普通位置转Zoom缩略图位置
     *
     * @param normalPosition 普通位置
     * @return Zoom缩略图位置
     */
    public int vPosOfZeroConvesionToZoomSuoLie(int normalPosition) {
        return (int)(1.0*ScopeBase.zoomYGrid_fangda()/2
                +1.0*normalPosition*ScopeBase.zoomYGrid_suolue()/ScopeBase.YGridWave());
    }

    /**
     * 垂直位置转换：普通位置转Zoom放大图位置
     *
     * @param normalPosition 普通位置
     * @return Zoom放大图位置
     */
    public int vPosOfZeroConvesionToZoomFanda(int normalPosition) {
        return (int)(-1.0*ScopeBase.zoomYGrid_suolue()/2
                +1.0*normalPosition*ScopeBase.zoomYGrid_fangda()/ScopeBase.YGridWave());
    }

    /**
     * 垂直位置转换：Zoom放大图位置转普通位置
     *
     * @param zoomPosition Zoom放大图位置
     * @return 普通位置
     */
    public int ZoomFandaConvesionTovPosOfZero(int zoomPosition) {
        return (int)(1.0*(zoomPosition+ScopeBase.zoomYGrid_suolue()/2)
                *(1.0*ScopeBase.YGridWave()/ScopeBase.zoomYGrid_fangda()));
    }

    // ==================== 事件处理方法 ====================

    /**
     * 事件更新回调（Observer接口实现）
     *
     * <p><b>处理事件：</b>
     * <ul>
     *   <li>EVENT_FPGA_LOAD_OK：FPGA加载完成</li>
     *   <li>EVENT_CHANNEL_VSCALE：通道垂直刻度变化</li>
     *   <li>EVENT_SCOPE_STATE：示波器状态变化</li>
     * </ul>
     *
     * @param observable 被观察对象
     * @param data 事件数据（EventBase对象）
     */
    @Override
    public void update(Observable observable, Object data) {
        // 将事件数据转换为EventBase对象
        EventBase event = (EventBase)data;

        // 处理FPGA加载完成事件
        if(event.getId() == EventFactory.EVENT_FPGA_LOAD_OK){
            Log.d("Scope","FPGA_LOAD_OK");
            ScopeMessage.getInstance().sendFpgaId();  // 发送FPGA ID
            setRun(true);  // 启动采样
        }
        // 处理通道垂直刻度变化事件
        else  if(event.getId() == EventFactory.EVENT_CHANNEL_VSCALE){
            int chIdx = (int)event.getData();  // 获取通道索引

            // 检查是否为动态通道
            if(ChannelFactory.isDynamicCh(chIdx)) {
                Channel channel = ChannelFactory.getDynamicChannel(chIdx);
                if(channel != null) {
                    // 计算垂直显示范围
                    int val = vSpanOfView(channel.getResistanceType(),channel.getVScaleVal()/channel.getProbeRate());
                    channel.setVRange(-val, val);  // 设置垂直范围
                    channel.checkPos();  // 检查位置是否有效
                }
            }
        }
        // 处理示波器状态变化事件
        else if( event.getId() == EventFactory.EVENT_SCOPE_STATE){
            // 如果正在运行
            if(isRun()) {
                int maxIdx = ChannelFactory.getMaxChIdx();

                // 遍历所有通道
                for (int i = ChannelFactory.CH1; i < maxIdx; i++) {
                    Channel channel = ChannelFactory.getDynamicChannel(i);
                    if (channel != null) {
                        // 计算垂直显示范围
                        int val = vSpanOfView(channel.getResistanceType(),channel.getVScaleVal() / channel.getProbeRate());
                        channel.setVRange(-val, val);  // 设置垂直范围
                        channel.checkPos();  // 检查位置是否有效
                    }
                }
            }
        }
    }

    /**
     * 计算垂直显示范围
     *
     * <p><b>计算逻辑：</b>
     * <pre>
     * 1. 根据阻抗类型和刻度值获取档位索引
     * 2. 获取垂直范围
     * 3. 转换为像素值
     * </pre>
     *
     * @param resistanceType 阻抗类型（1MΩ或50Ω）
     * @param vScaleVal 垂直刻度值（V/div）
     * @return 垂直显示范围（像素）
     */
    public static int vSpanOfView(int resistanceType,double vScaleVal){
        CabteRegister cab = CabteRegister.getInstance();  // 获取校准寄存器实例

        // 根据阻抗类型和刻度值获取档位索引
        int dang = cab.getRatioIdx(resistanceType,vScaleVal);

        // 如果停止状态，使用最大档位
        if(!Scope.getInstance().isRun()){
            dang = cab.getRatioDangCnt();
        }

        // 获取垂直范围（V）
        double vRang = cab.getVerticalRange(resistanceType,dang);

        // 转换为像素值
        vRang = vRang * ScopeBase.getVerticalPerGridPixels() / vScaleVal;

        return (int) Math.round(vRang);  // 四舍五入返回
    }

    /**
     * 获取Zoom模式下的Y轴缩放比例
     *
     * @param chIdx 通道索引
     * @return Y轴缩放比例
     */
    public double zoomYScale(int  chIdx){
        // 检查是否为动态通道
        if(ChannelFactory.isDynamicCh(chIdx)){
            Channel channel = ChannelFactory.getDynamicChannel(chIdx);
            VerticalAxis verticalAxis = scopeFrozen.getChVertical(chIdx);

            if(verticalAxis != null && channel != null){
                // 计算Y轴缩放比例
                return scopeFrozen.getYFactor(chIdx) * verticalAxis.getScaleIdVal()*verticalAxis.getFineScale()
                        /(channel.getVScaleIdVal() * channel.getFineScale());
            }
        }
        return 0;  // 无效通道返回0
    }

    // ==================== 存储深度获取方法 ====================

    /**
     * 获取存储深度
     *
     * @return 存储深度（点）
     */
    public long zunMemDepth(){
        return zunMemDepth(isRun());  // 根据运行状态获取
    }

    /**
     * 获取存储深度
     *
     * @param bRun 是否为运行状态
     * @return 存储深度（点）
     */
    public long zunMemDepth(boolean bRun){
        if(bRun){
            return sample.getSampleMemDepth();  // 从Sample获取
        }
        return scopeFrozen.getMemDepth();  // 从冻结状态获取
    }

    /**
     * 获取指定通道数下的存储深度
     *
     * @param chCnt 通道数
     * @return 存储深度（点）
     */
    public long zunMemDepth(int chCnt){
        return zunMemDepth(isRun(),chCnt);  // 根据运行状态获取
    }

    /**
     * 获取指定通道数下的存储深度
     *
     * @param bRun 是否为运行状态
     * @param chCnt 通道数
     * @return 存储深度（点）
     */
    public long zunMemDepth(boolean bRun,int chCnt){
        if(bRun){
            return sample.getSampleMemDepth(chCnt);  // 从Sample获取
        }
        return scopeFrozen.getMemDepth();  // 从冻结状态获取
    }

    // ==================== 显示清除方法 ====================

    /**
     * 清除持久显示
     *
     * <p><b>功能说明：</b>清除余辉和FFT持久显示
     */
    public void clearPersist(){
        display.clearPersist();  // 清除余辉
        display.clearFftPersist();  // 清除FFT持久显示
    }

    /**
     * 清除波形
     *
     * <p><b>功能说明：</b>清除所有波形数据和持久显示
     */
    public void clearWave(){
        display.clearFftPersist();  // 清除FFT持久显示
        display.clearPersist();  // 清除余辉
        display.clearWave();  // 清除波形

        // 清除所有通道的波形有效标志
        ChannelFactory.forEachCh(channel -> channel.setWaveValid(false));
        ChannelFactory.forEachMath(mathChannel -> mathChannel.setWaveValid(false));

        // 清除XY波形
        DisplayXYService.getInstance().ClearXY();
    }

    // ==================== 触发控制方法 ====================

    /**
     * 强制触发
     *
     * <p><b>功能说明：</b>手动触发一次采样
     */
    public void forceTrigger(){
        ScopeMessage.getInstance().forceTrigger();  // 发送强制触发命令
    }

    /**
     * 获取FPGA时钟输入输出状态
     *
     * @param isIn true获取输入状态，false获取输出状态
     * @return 时钟状态
     */
    public boolean getFpgaClockInOutStatus(boolean isIn) {
        return ScopeMessage.getInstance().getFpgaClockInOutStatus(isIn);
    }

    // ==================== 硬件保护方法 ====================

    /**
     * 电池保护
     *
     * <p><b>功能说明：</b>电池电量过低时进入保护模式
     */
    public void batteryProtect(){
        mHardware.batteryProtect();  // 硬件进入电池保护模式
    }

    // ==================== 命令控制方法 ====================

    /**
     * 启用/禁用命令处理
     *
     * <p><b>功能说明：</b>控制是否响应外部命令（如SCPI命令）
     *
     * @param bEnable true启用，false禁用
     */
    public void enableCommand(boolean bEnable){
        EventFactory.getInstance().setEnable(bEnable);  // 启用/禁用事件处理
        ScopeMessage.getInstance().enableCommand(bEnable);  // 启用/禁用消息处理
    }

    // ==================== 产品信息方法 ====================

    /**
     * 产品名称
     */
    public String product;

    /**
     * 序列号
     */
    private String sn;

    /**
     * 获取产品名称
     *
     * @return 产品名称
     */
    public String getProduct() {
        return product;
    }

    /**
     * 获取序列号
     *
     * @return 序列号
     */
    public String getSn() {
        return sn;
    }

    /**
     * 设置USB信息
     *
     * <p><b>功能说明：</b>从USB设备读取产品信息
     *
     * @param product 产品名称
     * @param serial 序列号
     * @param ver 版本
     */
    public void setUsbInfo(String product, String serial,String ver){
        // 去除换行符和空格
        product = product.replace("\r\n","").trim();
        serial = serial.replace("\r\n","").trim();
        ver = ver.replace("\r\n","").trim();

        // 保存产品信息
        this.product = product;
        this.sn = serial;

        // 传递给硬件层
        mHardware.setUsbInfo(this.product, this.sn,ver);
    }

    /**
     * 获取系统版本
     *
     * @return 系统版本
     */
    public int getSysVersion(){
        return mHardware.getSysVersion();
    }

    // ==================== 位置转换方法 ====================

    /**
     * 转换位置（考虑Zoom模式）
     *
     * <p><b>功能说明：</b>将普通坐标转换为Zoom模式坐标
     *
     * @param pos 原始位置
     * @return 转换后的位置
     */
    public double convertPos(double pos){
        if(isZoom()){
            pos = pos * ScopeBase.getZoomHeight() / ScopeBase.getHeight();
        }
        return pos;
    }

    // ==================== 阻抗类型方法 ====================

    /**
     * 获取阻抗类型
     *
     * <p><b>功能说明：</b>从寄存器值解析阻抗类型
     *
     * @param chIdx 通道索引
     * @param val 寄存器值
     * @return 阻抗类型（RESISTANCE_1M或RESISTANCE_50）
     */
    private int getResistanceType(int chIdx,int val){
        // 根据位值判断阻抗类型
        return ((val & (1<<chIdx)) == 0) ? Channel.RESISTANCE_1M : Channel.RESISTANCE_50;
    }

    /**
     * 设置阻抗类型
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据硬件信号自动切换阻抗类型</li>
     *   <li>50Ω转1MΩ时需要延时保护（防止损坏硬件）</li>
     * </ul>
     *
     * <p><b>保护机制：</b>
     * <ul>
     *   <li>50Ω转1MΩ需要至少2秒延时</li>
     *   <li>防止瞬间切换损坏输入电路</li>
     * </ul>
     *
     * @param val 阻抗类型值（位图，每位对应一个通道）
     */
    public void setResistanceType(int val){
        Channel channel;
        long ts = SystemClock.elapsedRealtime();  // 获取当前时间
        int maxIdx = ChannelFactory.getMaxChIdx();

        // 遍历所有通道
        for(int i=ChannelFactory.CH1;i<maxIdx;i++){
            // 解析阻抗类型
            int resistanceType = getResistanceType(i,val);

            // 如果要切换到1MΩ
            if(resistanceType == Channel.RESISTANCE_1M) {
                channel = ChannelFactory.getDynamicChannel(i);

                // 检查是否从50Ω切换到1MΩ
                if (channel != null && channel.getResistanceType() == Channel.RESISTANCE_50) {
                    // 检查延时是否足够（至少2秒）
                    if(channel.getResistanceTS() > 0 && (ts - channel.getResistanceTS()) > 2000) {
                        channel.setResistanceType(Channel.RESISTANCE_1M);  // 切换阻抗类型
                    }
                }
            }
        }
    }

    // ==================== 段采样方法 ====================

    /**
     * 获取段采样时间戳
     */
    public void segmentTimTimestamp(){
        ScopeMessage.getInstance().segmentTimestamp();  // 发送获取时间戳命令
    }

    /**
     * 判断段采样是否启用
     *
     * @return true表示段采样已启用
     */
    public boolean isSegmentEnable(){
        return isSegmentEnable(isRun());  // 根据运行状态判断
    }

    /**
     * 判断段采样是否启用
     *
     * @param bRun 是否为运行状态
     * @return true表示段采样已启用
     */
    public boolean isSegmentEnable(boolean bRun){
        if(bRun){
            return segmentSample.isSegmentEnable();  // 从SegmentSample获取
        }else{
            return scopeFrozen.isSegmentEnable();  // 从冻结状态获取
        }
    }

    /**
     * 设置段采样帧号
     *
     * <p><b>功能说明：</b>切换到指定的段采样帧
     *
     * @param frameNo 帧号
     */
    public void setSegmentFrameNo(long frameNo){
        // 仅在停止状态下有效
        if(!isRun()){
            long frameNums = getSegmentFrameNums();  // 获取总帧数

            if(frameNums > 0 && frameNo >= 0) {
                Logger.d(TAG,"frameNo:" + frameNo + ",frameNums:" + frameNums+",getSegmentFrameNo:"+getSegmentFrameNo() );

                // 如果帧号不同，切换帧
                if(getSegmentFrameNo() != frameNo) {
                    segmentSample.setFrameNo(frameNo % frameNums);  // 取模确保在有效范围内
                }
            }
        }
    }

    /**
     * 获取当前帧号
     *
     * @return 帧号
     */
    public int getSegmentFrameNo(){
        if(!isRun()){
            return scopeFrozen.getSegmentFrameNo();  // 从冻结状态获取
        }
        return 0;
    }

    /**
     * 获取可回放的帧数
     *
     * @return 帧数
     */
    public int getSegmentFrameNums(){
        if(!isRun()){
            return scopeFrozen.getSegmentFrameNums();  // 从冻结状态获取
        }
        return 0;
    }

    /**
     * 获取当前帧的时间戳
     *
     * @return 时间戳（纳秒）
     */
    public long getSegmentTimestamp(){
        if(!isRun()){
            return scopeFrozen.getSegmentTimestamp();  // 从冻结状态获取
        }
        return 0;
    }

    /**
     * 获取所有帧的时间戳列表
     *
     * @return 时间戳列表
     */
    public List<SegmentedSingleBean> getAllFrameTimestamp(){
        synchronized (this) {
            return listSegmentTimestamp;  // 返回时间戳列表
        }
    }

    /**
     * 段采样时间戳列表
     */
    private List<SegmentedSingleBean> listSegmentTimestamp = new ArrayList<>();

    /**
     * 清除段采样时间戳
     */
    public void clearSegmentTimestamp(){
        synchronized (this) {
            listSegmentTimestamp.clear();  // 清空列表
        }
    }

    /**
     * 添加段采样时间戳
     *
     * @param ts 时间戳
     */
    public void addSegmentTimestamp(long ts){
        synchronized (this) {
            int idx = listSegmentTimestamp.size() + 1;  // 计算帧序号
            listSegmentTimestamp.add(new SegmentedSingleBean(idx,ts));  // 添加到列表
        }
    }

    // ==================== 触摸微调方法 ====================

    /**
     * 判断是否正在触摸微调
     *
     * @return true表示正在触摸微调
     */
    public boolean isTouchFine(){
        return ChannelFactory.isDynamicCh(fineChidx) && bTouchFine;
    }

    // 微调状态变量
    private boolean bTouchFine = false;  // 触摸微调标志
    private int fineChidx = -1;  // 微调通道索引
    private double fineScaleVal = 0;  // 微调前的刻度值
    private double finePos = 0;  // 微调前的位置
    private int fineSampleBak = -1;  // 微调前的采样状态备份

    /**
     * 开始微调
     *
     * <p><b>功能说明：</b>记录微调前的状态
     */
    public void Sfine(){
        fineChidx = -1;
        int chIdx = ChannelFactory.getChActivate();  // 获取激活通道

        // 检查是否为动态通道
        if(ChannelFactory.isDynamicCh(chIdx)){
            Channel channel = ChannelFactory.getDynamicChannel(chIdx);
            if(channel != null){
                fineChidx = chIdx;  // 记录通道索引
                fineScaleVal = channel.getVScaleVal();  // 记录刻度值
                finePos = channel.getPos();  // 记录位置
            }
        }
    }

    /**
     * 结束微调
     *
     * <p><b>功能说明：</b>恢复采样状态
     */
    public void Efine(){
        int chIdx = fineChidx;
        fineChidx = -1;  // 清除通道索引
        bTouchFine = false;  // 清除微调标志

        // 检查通道有效性
        if(ChannelFactory.isDynamicCh(chIdx)){
            Channel channel = ChannelFactory.getDynamicChannel(chIdx);
            if(channel != null){
                channel.setVScaleVal(channel.getVScaleVal());  // 应用最终刻度值
            }

            // 恢复采样状态
            if(fineSampleBak != -1) {
                if (fineSampleBak != Sample.SAMPLE_STOP) {
                    sample.frozenSample(fineSampleBak);  // 恢复采样状态
                }
            }
            fineSampleBak = -1;  // 清除备份

            ScopeMessage.getInstance().touchResume();  // 恢复触摸处理
        }
    }

    /**
     * 垂直微调
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据触摸位置调整垂直刻度和位置</li>
     *   <li>支持物理通道、参考通道、数学通道</li>
     * </ul>
     *
     * @param y Y坐标（像素）
     * @param v 刻度值
     */
    public void Vfine(double y,double v){
        // 计算相对于屏幕中心的Y坐标
        y -= ((double) ScopeBase.getHeight() / 2);

        // 计算刻度比例
        v = fineScaleVal / v;

        // 计算位置偏移
        double a = (finePos + y) * fineScaleVal;

        // 检查通道有效性
        if(ChannelFactory.isDynamicCh(fineChidx)){
            Channel channel = ChannelFactory.getDynamicChannel(fineChidx);
            if(channel != null) {
                // 如果刻度变化超过5%，进入微调模式
                if(Math.abs(v - channel.getVScaleVal()) / channel.getVScaleVal() >= 0.05){
                    // 首次进入微调模式
                    if(!bTouchFine){
                        bTouchFine = true;
                        fineSampleBak = sample.getSampleState();  // 备份采样状态
                        ScopeMessage.getInstance().touchPause();  // 暂停触摸处理
                    }

                    // 考虑探头比例
                    v /= channel.getProbeRate();

                    // 获取档位范围
                    double max = VerticalAxis.getScaleIdValById(channel.getMaxGear());
                    double min = VerticalAxis.getScaleIdValById(channel.getMinGear());

                    // 限制在有效范围内
                    if(v < min){
                        v = min;
                    }else if(v > max){
                        v = max;
                    }

                    // 设置微调刻度和位置
                    channel.setFineScale(v/channel.getVScaleIdVal());
                    channel.setPos((int)(( a /channel.getVScaleVal() - y )));
                    channel.setVScaleVal(channel.getVScaleVal());
                }
            }
        }
    }

    // ==================== 水平轴缩放方法 ====================

    /**
     * 水平轴缩放
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据激活通道类型选择缩放方式</li>
     *   <li>支持FFT、参考通道、普通通道</li>
     * </ul>
     *
     * @param x X坐标（像素）
     * @param val 缩放值（正数放大，负数缩小）
     */
    public void horizontalAxisScale(int x,int val){
        Log.d(TAG, "horizontalAxisScale() called with: x = [" + x + "], val = [" + val + "]");

        // 计算相对于屏幕中心的X坐标
        x = ScopeBase.getWidth() / 2 - x;

        // 获取激活通道索引
        int chIdx = ChannelFactory.getChActivate();

        // 根据通道类型选择缩放方式
        if(ChannelFactory.isMath_FFT_Ch(chIdx) ){
            // FFT通道缩放
            MathChannel mathChannel = ChannelFactory.getMathChannel(chIdx);
            HorizontalAxisMath horizontalAxisMath = mathChannel.getHorizontalAxisMathFFT();
            List<Double> list = horizontalAxisMath.getxAxis();

            int bakIdx = horizontalAxisMath.getHorizontalScaleId();
            int idx = bakIdx + val;

            // 限制在有效范围内
            if(idx < 0)  idx = 0;
            else if(idx >= list.size()){
                idx = list.size() - 1;
            }

            // 如果档位变化
            if(idx != bakIdx) {
                if (idx >= 0 && idx < list.size()) {
                    long pos = horizontalAxisMath.getXPosOfView();

                    double sOld = horizontalAxisMath.fftXScaleIdVal();

                    horizontalAxisMath.setHorizontalScaleId(idx);

                    double sNew = horizontalAxisMath.fftXScaleIdVal();
                    pos = (long) (pos * sOld / sNew) - x;

                    horizontalAxisMath.setXPosOfView(pos);
                    horizontalAxisMath.correctXPose();
                    EventFactory.sendEvent(EventFactory.EVENT_TIME_SCALE, true);
                }
            }
        } else if (ChannelFactory.isRefCh(chIdx)) {
            // 参考通道缩放
            refHorScale(x, val, chIdx);
        } else {
            // 普通通道缩放
            chHorScale(x, val);
        }
    }

    /**
     * 参考通道水平缩放
     *
     * @param x X坐标
     * @param val 缩放值
     * @param chIdx 通道索引
     */
    private void refHorScale(int x,int val, int chIdx) {
        Scope scope = Scope.getInstance();
        if (scope.isSerialText()) return;  // 文本模式不支持缩放

        RefChannel refChannel = (RefChannel) ChannelFactory.getValidChannel(chIdx);

        List<Double> list = refChannel.getHorizontalAxisRef().getxAxis();
        int bakIdx = refChannel.getRefTimeScaleId_ui();

        // 如果启用时基跟随且不是FFT波形
        if (HorizontalAxis.getInstance().getScaleFollowingCh() && refChannel.getRefType() != WaveData.FFT_WAVE) {
            list = horizontalAxis.getxAxis();
            bakIdx = horizontalAxis.getTimeScaleIdOfView();
        }

        int idx = bakIdx + val;

        // 限制在有效范围内
        if(idx < 0)  idx = 0;
        else if(idx >= list.size()){
            idx = list.size() - 1;
        }

        // 如果档位变化
        if(bakIdx != idx) {
            if (idx >= 0 && idx < list.size()) {
                long oldTrTime = refChannel.getTimePosOfView();
                long clickTime = refChannel.getTimePosOfView(x);
                long deltTime = oldTrTime - clickTime;

                refChannel.setRefTimeScale(list.get(idx), 0);

                long newClickTime = refChannel.getTimePosOfView(x);
                long newTrTime = deltTime + newClickTime;
                long newTrPix = refChannel.getTimePoseOfViewPix(newTrTime);
                refChannel.setXPosOfViewPix(newTrPix);

                // 如果启用时基跟随
                if (HorizontalAxis.getInstance().getScaleFollowingCh()) {
                    int bak_id = horizontalAxis.getTimeScaleIdOfView();
                    int id = bak_id + val;

                    // 限制在有效范围内
                    if (id < HorizontalAxis.getMinGear()) {
                        id = HorizontalAxis.getMinGear();
                    } else if (id > HorizontalAxis.getMaxGear()) {
                        id = HorizontalAxis.getMaxGear();
                    }

                    if (HorizontalAxis.isValidScaleId(id)) {
                        horizontalAxis.setTimeScaleIdOfView(id);
                        horizontalAxis.setTimePosOfView(newTrPix);
                    }
                }

                EventFactory.sendEvent(EventFactory.EVENT_TIME_SCALE, true);
            }
        }
    }

    /**
     * 普通通道水平缩放
     *
     * @param x X坐标
     * @param val 缩放值
     */
    private void chHorScale(int x,int val) {
        Scope scope = Scope.getInstance();
        if (scope.isSerialText()) return;  // 文本模式不支持缩放

        int bak_id = horizontalAxis.getTimeScaleIdOfView();
        int id = bak_id + val;

        // 限制在有效范围内
        if(id < HorizontalAxis.getMinGear()){
            id = HorizontalAxis.getMinGear();
        }else if( id > HorizontalAxis.getMaxGear()){
            id = HorizontalAxis.getMaxGear();
        }

        // 如果档位变化
        if(bak_id != id) {
            if (HorizontalAxis.isValidScaleId(id)) {
                double v = horizontalAxis.getTimeScaleIdVal();
                long pos = horizontalAxis.getTimePosOfView();
                pos = horizontalAxis.timePoseOfViewChangeEx(v, pos, x);

                v = horizontalAxis.getTimeScaleIdVal(id);
                pos += horizontalAxis.timePoseOfViewChangeEx(v, 0, -x);

                horizontalAxis.setTimeScaleIdOfView(id);
                horizontalAxis.setTimePosOfView(pos);
                horizontalAxis.correctTimePose();
            }
        }
    }

    /**
     * 双击放大
     *
     * @param x X坐标
     */
    public void doubleClicked(int x){
        horizontalAxisScale(x,1);  // 放大一档
    }

    // ==================== AD复位方法 ====================

    /**
     * AD复位
     *
     * <p><b>功能说明：</b>复位ADC芯片
     */
    public void AdReset(){
        ScopeMessage.getInstance().sendADReset();  // 发送AD复位命令
    }

    // ==================== 用户设置重置方法 ====================

    private boolean bUsersetReset = false;  // 用户设置重置标志

    /**
     * 设置用户设置重置标志
     *
     * @param bUsersetReset 重置标志
     */
    public void setUsersetReset(boolean bUsersetReset){
        this.bUsersetReset = bUsersetReset;
    }

    /**
     * 判断是否需要重置用户设置
     *
     * @return true表示需要重置
     */
    public boolean isUsersetReset(){
        return bUsersetReset;
    }

    // ==================== UI状态方法 ====================

    private volatile boolean bUI = false;  // UI就绪标志

    /**
     * 判断UI是否就绪
     *
     * @return true表示UI就绪
     */
    public boolean isUI(){
        return bUI;
    }

    /**
     * 设置UI就绪状态
     *
     * @param b 就绪状态
     */
    public void setUI(boolean b){
        bUI = b;
    }

    // ==================== 资源加载方法 ====================

    /**
     * 获取输入流
     *
     * <p><b>功能说明：</b>从资源文件加载探头配置
     *
     * @param probe 探头ID
     * @param sw 开关值
     * @return 输入流
     */
    private InputStream getInputStream(int probe,int sw){
        InputStream is = null;

        Resources res = mContext.getResources();

        // 构造资源ID名称（十六进制格式）
        int resId = res.getIdentifier(Integer.toHexString(probe) + "_" + Integer.toHexString(sw), "raw", mContext.getPackageName());

        // 如果资源存在
        if(resId != 0){
            is = mContext.getResources().openRawResource(resId);  // 打开资源文件
        }

        return is;
    }

    // ==================== 待机状态方法 ====================

    private volatile boolean bStandby = false;  // 待机状态标志

    /**
     * 设置待机状态
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>进入待机时停止数据保存</li>
     *   <li>等待保存线程结束</li>
     * </ul>
     *
     * @param bStandby true待机，false唤醒
     */
    public  void setStandby(boolean bStandby){
        synchronized (this){
            this.bStandby = bStandby;
        }

        // 如果进入待机
        if(bStandby) {
            // 停止所有数据保存
            SaveBin.getInstance().stop();
            SaveCsv.getInstance().stop();
            SaveRecoverySession.getInstance().stop();

            // 等待保存线程结束
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断是否为待机状态
     *
     * @return true表示待机状态
     */
    public synchronized boolean isStandby(){
        return bStandby;
    }

    // ==================== 采样状态发送方法 ====================

    /**
     * 发送采样状态
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查是否允许发送指定状态</li>
     *   <li>XY模式、单次模式、自动模式、文本模式有特殊限制</li>
     * </ul>
     *
     * <p><b>限制说明：</b>
     * <ul>
     *   <li>XY模式：不支持瞬态模式</li>
     *   <li>单次模式：不支持瞬态模式</li>
     *   <li>自动模式：不支持瞬态模式</li>
     *   <li>文本模式：不支持瞬态模式</li>
     *   <li>零通道使能：不支持瞬态模式</li>
     * </ul>
     *
     * @param state 采样状态
     * @return true表示发送成功
     */
    public boolean sendSampleState(int state){
        boolean bRet = true;

        Display display  = Display.getInstance();
        Sample sample = Sample.getInstance();
        Scope scope = Scope.getInstance();

        // 检查是否允许发送瞬态状态
        if(display.isXYMode()
                || sample.isSingle()
                || scope.isAuto()
                || scope.isSerialText()
                || scope.isEnableChannelZero()
                ){
            // 瞬态模式不允许
            if(state > Sample.SAMPLE_RUN){
                bRet = false;
            }
        }

        // 如果允许发送
        if(bRet){
            sample.sendSampleState(state);
        }

        return bRet;
    }

    // ==================== 零通道使能方法 ====================

    private boolean bEnableChannelZero = false;  // 零通道使能标志

    /**
     * 设置零通道使能状态
     *
     * @param bEnableChannelZero 使能状态
     */
    public synchronized void setEnableChannelZero(boolean bEnableChannelZero){
        this.bEnableChannelZero = bEnableChannelZero;
    }

    /**
     * 判断零通道是否使能
     *
     * @return true表示使能
     */
    public synchronized boolean isEnableChannelZero(){
        return this.bEnableChannelZero;
    }

}
