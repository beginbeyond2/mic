package com.micsig.tbook.tbookscope.util;

import android.graphics.Point;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.SaveRecoverySession;
import com.micsig.tbook.scope.Sample.MemDepthFactory;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.vertical.VerticalAxis;
import com.micsig.tbook.scope.vertical.VerticalAxisMathDual;
import com.micsig.tbook.scope.vertical.VerticalAxisMathFft;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.main.maincenter.MainCenterMsgChannels;
import com.micsig.tbook.tbookscope.main.maincenter.serialsword.ISerialsWord;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.tools.SaveManage;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                           C A C H E   U T I L                              │
 * │                         缓 存 工 具 类（全 局 参 数 中 心）                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │                                                                             │
 * │  【模块定位】                                                               │
 * │  MHO系列示波器Android应用的缓存管理核心类，负责所有UI参数的存储、           │
 * │  读取、默认值初始化和持久化。是整个应用的"参数中枢"，所有界面组件           │
 * │  的状态均通过本类进行缓存和恢复。                                           │
 * │                                                                             │
 * │  【核心职责】                                                               │
 * │  1. 定义200+缓存Key常量，覆盖通道/触发/串口/测量/存储/显示等全部参数       │
 * │  2. 提供类型安全的读取接口：getInt/getDouble/getLong/getString/getBoolean   │
 * │  3. 管理两个HashMap缓存：map（主参数）和otherMap（杂项/路径/校准）         │
 * │  4. 提供巨大的默认值初始化表（getValueFromInit/getValueFromInit1）          │
 * │  5. 缓存持久化：通过SaveManage写入/读取文件                                │
 * │  6. 加载状态管理：跟踪各UI模块的参数加载完成情况                           │
 * │  7. 参数合法性校验：启动时检查REF文件、通道选择等状态                      │
 * │  8. 串口阈值电平初始值管理：UART/LIN/CAN/SPI/I2C/M429/M1553B              │
 * │                                                                             │
 * │  【架构设计】                                                               │
 * │  ┌──────────┐    读取     ┌──────────┐    持久化    ┌──────────┐          │
 * │  │  UI组件   │ ────────→ │ CacheUtil │ ────────→  │SaveManage│          │
 * │  │(各Layout) │ ←──────── │ (单例)    │ ←────────  │ (文件IO) │          │
 * │  └──────────┘    写入     └──────────┘    加载     └──────────┘          │
 * │       ↑                         │                                           │
 * │       │                         ↓                                           │
 * │       │               ┌──────────────────┐                                 │
 * │       │               │ getValueFromInit │                                 │
 * │       │               │ (默认值初始化表)  │                                 │
 * │       │               └──────────────────┘                                 │
 * │                                                                             │
 * │  【数据流向】                                                               │
 * │  应用启动 → CacheUtil单例创建 → 加载持久化参数 → 各UI模块读取缓存         │
 * │  → 用户操作 → putMap写入缓存 → 标记isChange → 定时/退出时持久化           │
 * │                                                                             │
 * │  【依赖关系】                                                               │
 * │  ← GlobalVar（全局变量：通道数/波形区域尺寸）                              │
 * │  ← SaveManage（持久化：文件读写）                                          │
 * │  ← TChan（通道编号常量）                                                   │
 * │  ← ChannelFactory（通道工厂）                                              │
 * │  ← HorizontalAxis/VerticalAxis（轴参数）                                   │
 * │  ← Scope/ScopeBase（示波器核心）                                           │
 * │  ← RightLayoutSerials（串口解码常量）                                      │
 * │  → 被所有UI Layout类依赖（读取/写入参数）                                  │
 * │                                                                             │
 * │  【缓存结构】                                                               │
 * │  map：主参数缓存，存储通道/触发/串口/测量/显示等核心参数                   │
 * │  otherMap：杂项缓存，存储路径/文件名/校准/语言/序号等辅助参数              │
 * │  mapLoadProcess：加载状态跟踪，ConcurrentHashMap保证线程安全               │
 * │                                                                             │
 * │  【使用示例】                                                               │
 * │  // 读取通道1的垂直档位ID                                                  │
 * │  int vScaleId = CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_SCALE_ID      │
 * │      + TChan.Ch1);                                                         │
 * │  // 写入运行/停止状态                                                      │
 * │  CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_RUNSTOP,                       │
 * │      String.valueOf(true));                                                │
 * │  // 读取其他缓存（路径）                                                   │
 * │  String path = CacheUtil.get().getOtherString(                             │
 * │      CacheUtil.TOP_SLIP_SAVE_WAVE_PATH + WAVE_TYPE_WAV);                  │
 * │                                                                             │
 * │  【线程安全】                                                               │
 * │  - map和otherMap为非线程安全的HashMap，应在主线程访问                      │
 * │  - mapLoadProcess使用ConcurrentHashMap，支持多线程加载状态标记             │
 * │  - loadComplete使用volatile修饰，保证多线程可见性                          │
 * │                                                                             │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * @author yangj
 * @since 2017/7/14
 */
public class CacheUtil { // 缓存工具类，全局参数中心
    private static final String TAG = "CacheUtil"; // 日志标签

    //region key值定义
    public static final String MAIN_LEFT_RUNSTOP = "mainLeftRunStop"; // 运行/停止状态
    public static final String MAIN_LEFT_SEQ = "mainLeftSEQ"; // 序列模式
    public static final String MAIN_LEFT_AUTO = "mainLeftAuto"; // 自动模式

    public static final String MAIN_CHANNEL_OPEN_STATE = "MAIN_CHANNEL_OPEN_STATE"; // 通道开启状态

    public static final String MAIN_RIGHT_MATH = "mainRightMath"; // Math通道总开关

    public static final String MAIN_RIGHT_REF = "mainRightRef"; // Ref通道总开关
    public static final String MAIN_RIGHT_SERIAL = "mainRightSERIAL"; // 串口解码总开关
    public static final String MAIN_CHAN_V_SCALE_ID = "MAIN_CHAN_V_EXTENT_ID"; // 通道垂直档位ID

    public static final String MAIN_RIGHT_MATH_FFT_INFO_DISPLAY = "MAIN_RIGHT_MATH_FFT_INFO_DISPLAY";//显示哪个通道的FftInfo
    public static final String MAIN_RIGHT_MATH_FFT_INFO_DISPLAY_SWITCH = "MAIN_RIGHT_MATH_FFT_INFO_DISPLAY_SWITCH";//是否显示FftInfo
    public static final String MAIN_RIGHT_MATH_DW_VSCALE_ID = "MAIN_RIGHT_MATH_DW_VSCALE_ID"; // Math双波形垂直档位ID
    public static final String MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID = "MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID"; // Math FFT dB垂直档位ID
    public static final String MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID = "MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID"; // Math FFT RMS垂直档位ID
    public static final String MAIN_RIGHT_MATH_AXB_VSCALE_ID = "MAIN_RIGHT_MATH_AXB_VSCALE_ID"; // Math A×B垂直档位ID
    public static final String MAIN_RIGHT_MATH_AM_VSCALE_ID = "MAIN_RIGHT_MATH_AM_VSCALE_ID"; // Math高级运算垂直档位ID

    public static final String MAIN_CHAN_REF_VSCALE_ID = "MAIN_CHAN_REF_VSCALE_ID"; // Ref通道垂直档位ID

    public static final String MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE = "mainBottomTimeBaseNormalScale"; // 普通模式时基档位
    public static final String ZOOM_BOTTOM_TIMEBASE_LARGE_SCALE = "zoomBottomTimeBaseLargeScale"; // Zoom模式时基档位
    public static final String MAIN_BOTTOM_TIMEBASE_FFT_SCALE = "mainBottomTimeBaseFftScale"; // FFT模式时基档位
    public static final String MAIN_BOTTOM_TIMEBASE_REF_SCALE = "mainBottomTimeBaseRefScale"; // Ref模式时基档位

    public static final String MAIN_BOTTOM_TIMEBASE_Rn_SCALE = "mainBottomTimeBaseR%dScale"; // RefN时基档位（格式化字符串，%d为通道号）
    public static final String MAIN_BOTTOM_CHANNELLIST_VISIBLE = "mainBottomChannelListVisible"; // 通道列表可见性
    public static final String MAIN_BOTTOM_FINE = "MAIN_BOTTOM_FINE"; // 微调模式
    public static final String MAIN_WAVE_YT_CURSORH_POSITION = "mainBottomCursorHPosition"; // YT模式水平光标位置
    public static final String MAIN_WAVE_YT_CURSORV_POSITION = "mainBottomCursorVPosition"; // YT模式垂直光标位置
    public static final String MAIN_WAVE_YT_CURSOR_LABEL_X = "MAIN_WAVE_YT_CURSOR_LABEL_X"; // YT模式光标X标签
    public static final String MAIN_WAVE_YT_CURSOR_LABEL_Y = "MAIN_WAVE_YT_CURSOR_LABEL_Y"; // YT模式光标Y标签

    public static final String MAIN_WAVE_YT_CURSOR_SINGLE_LABLE_Y = "MAIN_WAVE_YT_CURSOR_SINGLE_LABLE_Y"; // YT模式单光标Y标签
    public static final String MAIN_WAVE_YT_CURSOR_SINGLE_LABLE_X = "MAIN_WAVE_YT_CURSOR_SINGLE_LABLE_X"; // YT模式单光标X标签

    public static final String MAIN_WAVE_CURSOR_POSITION_X1="MAIN_WAVE_CURSOR_POSITION_X1"; // 光标X1位置
    public static final String MAIN_WAVE_CURSOR_POSITION_X2="MAIN_WAVE_CURSOR_POSITION_X2"; // 光标X2位置
    public static final String MAIN_WAVE_CURSOR_POSITION_Y1="MAIN_WAVE_CURSOR_POSITION_Y1"; // 光标Y1位置
    public static final String MAIN_WAVE_CURSOR_POSITION_Y2="MAIN_WAVE_CURSOR_POSITION_Y2"; // 光标Y2位置
    public static final String MAIN_WAVE_CURSOR_POSITION_X1_HZ="MAIN_WAVE_CURSOR_POSITION_X1_HZ"; // 光标X1频率值
    public static final String MAIN_WAVE_CURSOR_POSITION_X2_HZ="MAIN_WAVE_CURSOR_POSITION_X2_HZ"; // 光标X2频率值
    public static final String MAIN_WAVE_XY_CURSORH_POSITION = "mainBottomXYCursorHPosition"; // XY模式水平光标位置
    public static final String MAIN_WAVE_XY_CURSORV_POSITION = "mainBottomXYCursorVPosition"; // XY模式垂直光标位置
    public static final String MAIN_WAVE_YT_CURSORH_VISIBLE = "mainBottomCursorHVisible"; // YT模式水平光标可见性
    public static final String MAIN_WAVE_YT_CURSORV_VISIBLE = "mainBottomCursorVVisible"; // YT模式垂直光标可见性
    public static final String MAIN_WAVE_XY_CURSORH_VISIBLE = "mainBottomXYCursorHVisible"; // XY模式水平光标可见性
    public static final String MAIN_WAVE_XY_CURSORV_VISIBLE = "mainBottomXYCursorVVisible"; // XY模式垂直光标可见性
    public static final String MAIN_BOTTOM_RIGHTSWITCH_CHANNEL = "mainBottomRightSwitch"; // 底部右侧切换通道
    public static final String MAIN_BOTTOM_SLIP_ALLMEASURE = "mainBottomSlipAllMeasure"; // 全部测量滑出菜单
    public static final String MAIN_BOTTOM_SLIP_SERIALBUSTXT = "mainBottomSlipSerialBusTxt"; // 串口总线文本滑出菜单
    public static final String MAIN_BOTTOM_SLIP_ZOOM = "mainBottomSlipZoom"; // Zoom模式滑出菜单
    public static final String MAIN_BOTTOM_SLIP_MENU = "mainBottomSlipMenu"; // 底部滑出菜单
    public static final String MAIN_CENTER_CHANNELS_X = "mainCenterChannelsX"; // 中心通道X坐标
    public static final String MAIN_CENTER_CHANNELS_Y = "mainCenterChannelsY"; // 中心通道Y坐标
    public static final String MAIN_RECOVERY_CHANNEL_SELECT = "mainRecoveryChannelSelect";//保存设置时的当前通道
    public static final String MAIN_CENTER_CHANNELS_SELECT = "mainCenterChannelsSelect";//当前通道
    public static final String MAIN_CENTER_CHANNELS_SELECT_WILL_NULL = "MAIN_CENTER_CHANNELS_SELECT_WILL_NULL";//当前通道即将变成无通道时的最后关闭的通道
    public static final String MAIN_CENTER_CHANNELS_SELECT_UNXY = "mainCenterChannelsSelectResIdUnXY";//非xy模式下的当前通道的控件resId
    public static final String MAIN_CENTER_MENU_X = "mainCenterMenuX"; // 中心菜单X坐标
    public static final String MAIN_CENTER_MENU_Y = "mainCenterMenuY"; // 中心菜单Y坐标
    public static final String MAIN_CENTER_SEGMENTED_VISIBLE = "mainCenterSegmentedVisible"; // 分段采样可见性
    public static final String MAIN_CENTER_SEGMENTED_X = "mainCenterSegmentedX"; // 分段采样X坐标
    public static final String MAIN_CENTER_SEGMENTED_Y = "mainCenterSegmentedY"; // 分段采样Y坐标

    public static final String RIGHT_SLIP_REF_CLOSE_SAVE = "RIGHT_SLIP_REF_CLOSE_SAVE"; // Ref关闭时保存的通道号

    public static final String RIGHT_SLIP_MAX_CHANNEL_NUMBER_REF = "rightSlipMaxChannelNumberRef";//保存的最大ref通道号
    public static final String RIGHT_SLIP_REF_CHECK = "rightSlipRefCheck"; // Ref通道开关
    public static final String RIGHT_SLIP_ADD_BY_USER_REF = "rightSlipAddByUserRef";//用户手动添加的ref通道
    public static final String RIGHT_SLIP_REF_DATA_FROM = "rightSlipRefDataFrom";//Ref数据来源
    public static final String RIGHT_SLIP_REF_CSV_INDEX = "rightSlipRefCsvIndex";//Ref csv列表旋钮选中的是第几个

    public static final String RIGHT_SLIP_MAX_CHANNEL_NUMBER_MATH = "rightSlipMaxChannelNumberMath";//保存的最大Math通道号
    public static final String RIGHT_SLIP_MATH_TYPE = "rightSlipMathType"; // Math运算类型
    public static final String RIGHT_SLIP_REF_TYPE = "rightSlipRefType"; // Ref数据类型
    public static final String RIGHT_SLIP_ADD_BY_USER_MATH = "rightSlipAddByUserMath";//用户手动添加的math通道
    public static final int MATHTYPE_DW = 0; // Math类型：双波形运算(A+B/A-B/A×B/A÷B)
    public static final int MATHTYPE_FFT = 1; // Math类型：FFT频谱分析
    public static final int MATHTYPE_AXB = 2; // Math类型：A×B标量运算
    public static final int MATHTYPE_AM = 3; // Math类型：高级运算(Advanced Math)

    public static final int REFTYPE_WAV = 0; // Ref类型：WAV波形文件
    public static final int REFTYPE_CSV = 1; // Ref类型：CSV数据文件
    public static final int REFTYPE_BIN = 2; // Ref类型：BIN二进制文件
    public static final int REFTYPE_ALL = 3; // Ref类型：全部文件

    public static final String RIGHT_SLIP_MATH_DW_SOURCE1 = "rightSlipMathDwSource1"; // 双波形运算源1
    public static final String RIGHT_SLIP_MATH_DW_SOURCE2 = "rightSlipMathDwSource2"; // 双波形运算源2
    public static final String RIGHT_SLIP_MATH_DW_SYMBOL = "rightSlipMathSymbol"; // 双波形运算符号(+/-/×/÷)
    public static final String RIGHT_SLIP_MATH_FFT_TYPE_ID = "rightSlipMathFftTypeId"; // FFT类型ID
    public static final String RIGHT_SLIP_MATH_FFT_SOURCE = "rightSlipMathFftSource"; // FFT源通道
    public static final String RIGHT_SLIP_MATH_FFT_WINDOW = "rightSlipMathFftWindow"; // FFT窗函数
    public static final String RIGHT_SLIP_MATH_FFT_PERSIST = "rightSlipMathFftPersist"; // FFT余辉开关
    public static final String RIGHT_SLIP_MATH_FFT_PERSIST_VALUE = "rightSlipMathFFTPersistValue"; // FFT余辉值
    public static final String RIGHT_SLIP_MATH_AXB_UNIT = "rightSlipMathAxbUnit"; // A×B运算单位
    public static final String RIGHT_SLIP_MATH_AXB_SOURCE = "rightSlipMathAxbSource"; // A×B运算源通道
    public static final String RIGHT_SLIP_MATH_AXB_A = "rightSlipMathAxbA"; // A×B运算系数A
    public static final String RIGHT_SLIP_MATH_AXB_B = "rightSlipMathAxbB"; // A×B运算系数B
    public static final String RIGHT_SLIP_MATH_AM_UNIT = "RIGHT_SLIP_MATH_AM_UNIT"; // 高级运算单位
    public static final String RIGHT_SLIP_MATH_AM_FORMULA = "RIGHT_SLIP_MATH_AM_FORMULA"; // 高级运算公式
    public static final String RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE = "RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE"; // 高级运算微分是否存在
    public static final String RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET = "RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET"; // 高级运算微分重置
    public static final String RIGHT_SLIP_MATH_AM_VAR1_NUMBER = "RIGHT_SLIP_MATH_AM_VAR1_NUMBER"; // 高级运算变量1系数
    public static final String RIGHT_SLIP_MATH_AM_VAR1_POWER = "RIGHT_SLIP_MATH_AM_VAR1_POWER"; // 高级运算变量1幂次
    public static final String RIGHT_SLIP_MATH_AM_VAR2_NUMBER = "RIGHT_SLIP_MATH_AM_VAR2_NUMBER"; // 高级运算变量2系数
    public static final String RIGHT_SLIP_MATH_AM_VAR2_POWER = "RIGHT_SLIP_MATH_AM_VAR2_POWER"; // 高级运算变量2幂次
    public static final String RIGHT_SLIP_MATH_VERTICALBASE = "rightSlipMathVerticalBase"; // Math垂直基准

    public static final String RIGHT_SLIP_CH_FINE_ENABLE = "RIGHT_SLIP_CH_FINE_ENABLE"; // 通道微调使能
    public static final String RIGHT_SLIP_CH_INVERT = "rightSlipChannelInvert"; // 通道反相
    public static final String RIGHT_SLIP_CH_COUPLE = "rightSlipChannelCouple"; // 通道耦合方式(AC/DC)
    public static final String RIGHT_SLIP_CH_PROBE_TYPE = "rightSlipChannelProbeType"; // 探头类型
    public static final String RIGHT_SLIP_CH_PROBE_MULTIPLE = "rightSlipChannelProbeMultiple"; // 探头倍率
    public static final String RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE = "rightSlipChannelProbeMultipleUserdefine"; // 探头自定义倍率
    public static final String RIGHT_SLIP_CH_BANDWIDTH = "rightSlipChannelBandWidth"; // 通道带宽限制
    public static final String RIGHT_SLIP_CH_VERTICALBASE = "rightSlipChannelVerticalBase"; // 通道垂直基准
    public static final String RIGHT_SLIP_CH_IMPED = "rightSlipChannelImped"; // 通道阻抗(1MΩ/50Ω)
    public static final String RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT = "rightSlipChannelBandWidthHighEdit"; // 高带宽限制编辑值
    public static final String RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT = "rightSlipChannelBandWidthLowEdit"; // 低带宽限制编辑值
    public static final String RIGHT_SLIP_CH_LABEL = "rightSlipChannelLabel"; // 通道标签
    public static final String RIGHT_SLIP_CH_LABEL_USERDEFINE = "rightSlipChannelLabelUserdefine"; // 通道自定义标签
    public static final String RIGHT_SLIP_CH_DELAY = "rightSlipChannelDelay"; // 通道延迟
    public static final String RIGHT_SLIP_CH_OFFSET = "rightSlipChannelOffset"; // 通道偏移
    public static final String RIGHT_SLIP_CH_POSITION = "rightSlipChannelPosition"; // 通道垂直位置
    public static final String RIGHT_SLIP_CH_FINE_EXTENT = "RIGHT_SLIP_CH_FINE_EXTENT"; // 通道微调档位
    public static final String RIGHT_SLIP_CH_PROBE_BANDWIDTH = "RIGHT_SLIP_CH_PROBE_BANDWIDTH"; // 探头带宽

//    public static final String RIGHT_SLIP_SAMPLE = "rightSlipSample";
//    public static final String RIGHT_SLIP_SAMPLE_DETAIL_INDEX = "rightSlipSampleDetailIndex";

    public static final String RIGHT_SLIP_MAX_CHANNEL_NUMBER_SERIALS = "rightSlipMaxChannelNumberSerials";//保存的最大Serial通道号
    public static final String RIGHT_SLIP_ADD_BY_USER_SERIALS = "rightSlipAddByUserSerials";//用户手动添加的Serial通道
    public static final String RIGHT_SLIP_SERIALS = "rightSlipSerials"; // 串口总线类型
    public static final String RIGHT_SLIP_OTHERS_CHANNEL_ORDER = "rightSlipOrderChannelOrder";//Math/Ref/Serials通道顺序
    public static final String RIGHT_SLIP_SERIALS_UART_RX = "rightSlipSerialsUartRx"; // UART Rx通道
    public static final String RIGHT_SLIP_SERIALS_UART_IDLE = "rightSlipSerialsUartIdle"; // UART空闲电平
    public static final String RIGHT_SLIP_SERIALS_UART_CHECK = "rightSlipSerialsUartCheck"; // UART校验位
    public static final String RIGHT_SLIP_SERIALS_UART_BITS = "rightSlipSerialsUartBits"; // UART数据位
    public static final String RIGHT_SLIP_SERIALS_UART_DISPLAY = "rightSlipSerialsUartDisplay"; // UART显示格式
    public static final String RIGHT_SLIP_SERIALS_UART_BAUDRATE = "rightSlipSerialsUartBaudRate"; // UART波特率
    public static final String RIGHT_SLIP_SERIALS_UART_USERDEFINE = "rightSlipSerialsUartUserDefine"; // UART自定义波特率

    public static final String RIGHT_SLIP_SERIALS_LIN_SOURCE = "rightSlipSerialsLinSource"; // LIN源通道
    public static final String RIGHT_SLIP_SERIALS_LIN_TYPE = "rightSlipSerialsLinType"; // LIN类型
    public static final String RIGHT_SLIP_SERIALS_LIN_IDLE = "rightSlipSerialsLinIdle"; // LIN空闲电平
    public static final String RIGHT_SLIP_SERIALS_LIN_BAUDRATE = "rightSlipSerialsLinBaudRate"; // LIN波特率
    public static final String RIGHT_SLIP_SERIALS_LIN_USERDEFINE = "rightSlipSerialsLinUserDefine"; // LIN自定义波特率

    public static final String RIGHT_SLIP_SERIALS_CAN_SOURCE = "rightSlipSerialsCanSource"; // CAN源通道
    public static final String RIGHT_SLIP_SERIALS_CAN_SIGNAL = "rightSlipSerialsCanSignal"; // CAN信号类型
    public static final String RIGHT_SLIP_SERIALS_CAN_BAUDRATE = "rightSlipSerialsCanBaudRate"; // CAN波特率
    public static final String RIGHT_SLIP_SERIALS_CAN_USERDEFINE = "rightSlipSerialsCanUserDefine"; // CAN自定义波特率
    public static final String RIGHT_SLIP_SERIALS_CAN_FDBAUDRATE = "rightSlipSerialsCanFDBaudRate"; // CAN FD波特率
    public static final String RIGHT_SLIP_SERIALS_CAN_FDUSERDEFINE = "rightSlipSerialsCanFDUserDefine"; // CAN FD自定义波特率
    public static final String RIGHT_SLIP_SERIALS_CAN_PERCENT = "RIGHT_SLIP_SERIALS_CAN_PERCENT"; // CAN采样点
    public static final String RIGHT_SLIP_SERIALS_CAN_FDPERCENT = "RIGHT_SLIP_SERIALS_CAN_FDPERCENT"; // CAN FD采样点
    public static final String RIGHT_SLIP_SERIALS_CAN_ISO = "RIGHT_SLIP_SERIALS_CAN_ISO"; // CAN ISO模式

    public static final String RIGHT_SLIP_SERIALS_SPI_CLK = "rightSlipSerialsSpiClk"; // SPI时钟通道
    public static final String RIGHT_SLIP_SERIALS_SPI_DATA = "rightSlipSerialsSpiData"; // SPI数据通道
    public static final String RIGHT_SLIP_SERIALS_SPI_CS = "rightSlipSerialsSpiCs"; // SPI片选通道
    public static final String RIGHT_SLIP_SERIALS_SPI_BIT = "rightSlipSerialsSpiBit"; // SPI数据位宽
    public static final String RIGHT_SLIP_SERIALS_SPI_CLKCHECK = "rightSlipSerialsSpiClkCheck"; // SPI时钟极性
    public static final String RIGHT_SLIP_SERIALS_SPI_DATACHECK = "rightSlipSerialsSpiDataCheck"; // SPI数据极性
    public static final String RIGHT_SLIP_SERIALS_SPI_CSCHECK = "rightSlipSerialsSpiCsCheck"; // SPI片选极性
    public static final String RIGHT_SLIP_SERIALS_SPI_CSSWITCH = "rightSlipSerialsSpiCsSwitch"; // SPI片选开关

    public static final String RIGHT_SLIP_SERIALS_I2C_SDA = "rightSlipSerialsSda"; // I2C SDA通道
    public static final String RIGHT_SLIP_SERIALS_I2C_SCL = "rightSlipSerialsScl"; // I2C SCL通道

    public static final String RIGHT_SLIP_SERIALS_M429_SOURCE = "rightSlipSerialsM429Source"; // M429源通道
    public static final String RIGHT_SLIP_SERIALS_M429_FORMAT = "rightSlipSerialsM429Format"; // M429数据格式
    public static final String RIGHT_SLIP_SERIALS_M429_DISPLAY = "rightSlipSerialsM429Display"; // M429显示格式
    public static final String RIGHT_SLIP_SERIALS_M429_BAUDRATE = "rightSlipSerialsM429BaudRate"; // M429波特率

    public static final String RIGHT_SLIP_SERIALS_M1553B_SOURCE = "rightSlipSerialsM1553bSource"; // M1553B源通道
    public static final String RIGHT_SLIP_SERIALS_M1553B_DISPLAY = "rightSlipSerialsM1553bDisplay"; // M1553B显示格式

    public static final String TOP_SLIP = "topSlip"; // 顶部滑出菜单
    public static final String TOP_SLIP_MEASURE = "TOP_SLIP_MEASURE"; // 测量菜单
    public static final String TOP_SLIP_MEASURE_STATIC_ALL = "TOP_SLIP_MEASURE_STATIC_ALL"; // 静态测量-全部
    public static final String TOP_SLIP_MEASURE_STATIC_MEAN = "TOP_SLIP_MEASURE_STATIC_MEAN"; // 静态测量-均值
    public static final String TOP_SLIP_MEASURE_STATIC_MAX = "TOP_SLIP_MEASURE_STATIC_MAX"; // 静态测量-最大值
    public static final String TOP_SLIP_MEASURE_STATIC_MIN = "TOP_SLIP_MEASURE_STATIC_MIN"; // 静态测量-最小值
    public static final String TOP_SLIP_MEASURE_STATIC_DELTA = "TOP_SLIP_MEASURE_STATIC_DELTA"; // 静态测量-差值
    public static final String TOP_SLIP_MEASURE_STATIC_COUNT = "TOP_SLIP_MEASURE_STATIC_COUNT"; // 静态测量-计数

    public static final String TOP_SLIP_MEASURE_SETTING_INDICATOR = "TOP_SLIP_MEASURE_SETTING_INDICATOR"; // 测量设置-指示器
    public static final String TOP_SLIP_MEASURE_SETTING_RANGE = "TOP_SLIP_MEASURE_SETTING_RANGE"; // 测量设置-范围
    public static final String TOP_SLIP_MEASURE_SETTING_CHANNEL_SELECT = "TOP_SLIP_MEASURE_SETTING_CHANNEL_SELECT"; // 测量设置-通道选择
    public static final String TOP_SLIP_MEASURE_SETTING_THRESHOLDS = "TOP_SLIP_MEASURE_SETTING_THRESHOLDS"; // 测量设置-阈值
    public static final String TOP_SLIP_MEASURE_SETTING_HIGH = "TOP_SLIP_MEASURE_SETTING_HIGH"; // 测量设置-高阈值
    public static final String TOP_SLIP_MEASURE_SETTING_MIDDLE = "TOP_SLIP_MEASURE_SETTING_MIDDLE"; // 测量设置-中阈值
    public static final String TOP_SLIP_MEASURE_SETTING_LOW = "TOP_SLIP_MEASURE_SETTING_LOW"; // 测量设置-低阈值

    public static final String TOP_SLIP_MEASURE_CHANNEL_SELECT = "topSlipMeasureChannelSelect"; // 测量通道选择
    public static final String TOP_SLIP_MEASURE_SELECT_LIST_CHANNEL = "topSlipMeasureSelectListChannel"; // 测量选择列表通道
    public static final String TOP_SLIP_MEASURE_SELECT_LIST_INDEX = "topSlipMeasureSelectListIndex"; // 测量选择列表索引
    public static final String TOP_SLIP_MEASURE_SELECT_LIST_NO = "topSlipMeasureSelectListNo"; // 测量选择列表编号
    public static final String MEASURE_SELECT_LIST_SLIP = ","; // 测量选择列表分隔符
    public static final String TOP_SLIP_MEASURE_DELAY_DATA = "topSlipMeasureDelayData"; // 延迟测量数据
    public static final String DELAY_SLIP = ","; // 延迟数据分隔符
    public static final String TOP_SLIP_MEASURE_PHASE_DATA = "topSlipMeasurePhaseData"; // 相位测量数据

    public static final String TOP_SLIP_MEASURE_TVALUE_DATA = "topSlipMeasureTValueData"; // T值测量数据
    public static final String TOP_SLIP_SAVE = "topSlipSave"; // 保存菜单
    public static final String WAVE_STORE_PATH_SLIP = ";"; // 波形存储路径分隔符

    public static final String TOP_SLIP_MEASURE_TVALUE_X1 = "topSlipMeasureTValueX1"; // T值测量X1

    public static final String TOP_SLIP_MEASURE_TVALUE_X2 = "topSlipMeasureTValueX2"; // T值测量X2

    public static final String TOP_SLIP_SAVE_WAVE_NAME = "topSlipSaveWaveName"; //保存波形 文件名
    public static final String TOP_SLIP_SAVE_WAVE_PATH = "topSlipSaveStorePath";//保存波形文件的路径
    public static final String TOP_SLIP_SAVE_WAVE_ABSOLUTE_PATH = "topSlipSaveStoreAbsolutePath";//保存波形文件的 绝对路径

    public static final String TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK ="TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK";// 波形 是否选中文件名 递增
    public static final String TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM ="TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM";//波形文件名递增序号
    public static final String TOP_SLIP_SAVE_WAVE_PATH_CURRENT = "topSlipSaveStorePathCurrent";//当前保存波形文件的路径
    public static final String TOP_SLIP_INVOKE_WAVE_FILE_FILTER = "topSlipInvokeWaveFileFilter";//调用 只显示文件夹 选项

    public static final String TOP_SLIP_SAVE_SESSION_NAME = "topSlipSaveSessionName";// 保存状态 文件名
    public static final String TOP_SLIP_SAVE_SESSION_PATH = "topSlipSaveStatePath";//保存状态路径
    public static final String TOP_SLIP_SAVE_SESSION_ABSOLUTE_PATH = "topSlipSaveStateAbsolutePath";//保存状态路径
    public static final String TOP_SLIP_SAVE_SESSION_SUFFIX_CHECK = "TopSlipSaveSessionSuffixCheck";//是否 选中文件名递增
    public static final String TOP_SLIP_SAVE_SESSION_SUFFIX_CHECK_NUM = "TopSlipSaveSessionSuffixCheckNum";//文件名递增序号
    public static final String TOP_SLIP_SAVE_SESSION_PATH_CURRENT = "topSlipSaveSessionPathCurrent";//当前保存状态文件的路径
    public static final String TOP_SLIP_INVOKE_SESSION_FILE_FILTER = "topSlipInvokeSessionFileFilter";//调用 只显示文件夹 选项

    public static final String TOP_SLIP_SAVE_SETTING_NAME = "topSlipSaveSettingName";// 保存设置 文件名
    public static final String TOP_SLIP_SAVE_SETTING_PATH = "topSlipSaveSettingPath";//保存 设置 路径
    public static final String TOP_SLIP_SAVE_SETTING_ABSOLUTE_PATH = "topSlipSaveSettingAbsolutePath";//保存 设置 绝对路径
    public static final String TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK = "topSlipSaveSettingSuffixCheck";//是否选中 文件名递增
    public static final String TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK_NUM = "topSlipSaveSettingSuffixCheckNum";//文件名递增序号
    public static final String TOP_SLIP_SAVE_SETTING_PATH_CURRENT = "topSlipSaveSettingPathCurrent";//当前保存设置文件的路径
    public static final String TOP_SLIP_INVOKE_SETTING_FILE_FILTER = "topSlipInvokeSettingFileFilter";//调用 只显示文件夹 选项

    public static final String TOP_SLIP_SAVE_PICTURE_NAME = "topSlipSavePictureName";// 保存 图片 文件名
    public static final String TOP_SLIP_SAVE_PICTURE_PATH = "topSlipSavePicturePath";//保存 图片 路径
    public static final String TOP_SLIP_SAVE_PICTURE_ABSOLUTE_PATH = "topSlipSavePictureAbsolutePath";//保存 图片 绝对路径
    public static final String TOP_SLIP_SAVE_PICTURE_SUFFIX_CHECK = "topSlipSavePictureSuffixCheck";//图片 是否选中 文件名递增
    public static final String TOP_SLIP_SAVE_PICTURE_SUFFIX_CHECK_NUM = "topSlipSavePictureSuffixCheckNum";//图片 文件名递增序号
    public static final String TOP_SLIP_SAVE_PICTURE_PATH_CURRENT = "topSlipSavePicturePathCurrent";//当前保存 图片 文件的路径
    public static final String TOP_SLIP_INVOKE_PICTURE_FILE_FILTER = "topSlipInvokePictureFileFilter";//调用 只显示文件夹 选项

    public static final String TOP_SLIP_SAVE_PICTURE_PATH_AUTO_SAVE = "topSlipSavePicturePathAutoSave";//当前保存 图片 文件的路径
    public static final String TOP_SLIP_SAVE_PICTURE_AUTO_SAVE_NAME = "topSlipSavePictureAutoSaveName";// 保存 图片 文件名

    public static final String TOP_SLIP_AUTO_SAVE_NAME = "topSlipSaveAutoSaveName";// 自动保存文件名
    public static final String TOP_SLIP_AUTO_SAVE_PATH = "topSlipSaveAutoSavePath";//自动保存 路径
    public static final String TOP_SLIP_AUTO_SAVE_ABSOLUTE_PATH = "topSlipSaveAutoSaveAbsolutePath";//自动保存 图片 绝对路径
    public static final String TOP_SLIP_AUTO_SAVE_SUFFIX_CHECK = "topSlipSaveAutoSaveSuffixCheck";//自动保存 是否选中 文件名递增
    public static final String TOP_SLIP_AUTO_SAVE_SUFFIX_CHECK_NUM = "topSlipSaveAutoSaveSuffixCheckNum";//自动保存 文件名递增序号
    public static final String TOP_SLIP_AUTO_SAVE_PATH_CURRENT = "topSlipSaveAutoSavePathCurrent";//当前保存 自动保存 文件的路径
    public static final String TOP_SLIP_AUTO_SAVE_STOP_CONDITION = "topSlipSaveAutoSaveStopCondition";//自动保存停止条件
    public static final String TOP_SLIP_AUTO_SAVE_Interval = "topSlipSaveAutoSaveInterval";//自动保存时间间隔
    public static final String TOP_SLIP_AUTO_SAVE_SAVE_MODE = "topSlipSaveAutoSaveSaveMode";//自动保存保存模式
    public static final String TOP_SLIP_AUTO_SAVE_SAVE_TYPE = "topSlipSaveAutoSaveSaveType";//自动保存保存类型

    public static final String TOP_SLIP_AUTO_SAVE_STOP_TIME = "topSlipSaveAutoSaveStopTime";//自动保存停止时间
    public static final String TOP_SLIP_AUTO_SAVE_STOP_FRAME = "topSlipSaveAutoSaveStopFrame";//自动保存停止帧数



    public static final String RIGHT_SLIP_REF_DATA_PATH = "rightSlipRefDataPath";//ref菜单页面中选择过的路径
    public static final String RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH = "rightSlipRefDataAbsolutePath";//ref菜单页面中选择过的 绝对路径
    public static final String RIGHT_SLIP_REF_DATA_PATH_CURRENT = "rightSlipRefDataPathCurrent";//ref菜单页面中选择的 当前路径
    public static final String RIGHT_SLIP_REF_DATA_SELECT_CURRENT = "rightSlipRefDataSelectCurrent";//ref菜单页面中当前当前选中的文件全路径名

    public static final String TOP_SLIP_SAVE_STORE = "topSlipSaveStore"; // 保存存储
    public static final String TOP_SLIP_INVOKE_STORE = "topSlipInvokeStore"; // 调用存储
    public static final String TOP_SLIP_AUTO_SAVE_STORE = "topSlipAutoSaveStore"; // 自动保存存储
    public static final String TOP_SLIP_SAVE_CHANNEL_SELECT = "topSlipSaveChannelSelect"; // 保存通道选择
    public static final String TOP_SLIP_SAVE_ALL_SEGMENT_VISIBLE = "topSlipSaveAllSegmentVisible"; // 保存全部分段可见性
    public static final String TOP_SLIP_SAVE_ALL_SEGMENT_CHECK = "topSlipSaveAllSegmentCheck"; // 保存全部分段选中
    public static final String TOP_SLIP_SAVE_DIR = "topSlipSaveDir"; // 保存目录
    public static final String TOP_SLIP_SAVE_TYPE = "topSlipSaveType"; // 保存类型
    public static final String TOP_SLIP_CURSOR = "TOP_SLIP_CURSOR"; // 光标菜单
    public static final String TOP_SLIP_CURSOR_COMMON = "TOP_SLIP_CURSOR_COMMON"; // 光标通用设置
    public static final String TOP_SLIP_CURSOR_COMMON_SOURCE = "TOP_SLIP_CURSOR_COMMON_SOURCE"; // 光标源通道
    public static final String TOP_SLIP_CURSOR_SETTING_TRANCE="TOP_SLIP_CURSOR_SETTING_TRANCE"; // 光标追踪设置
    public static final String TOP_SLIP_CURSOR_SETTING_MODE="TOP_SLIP_CURSOR_SETTING_MODE"; // 光标模式设置

    public static final String TOP_SLIP_SAMPLE = "topSlipSample"; // 采样菜单
    public static final String TOP_SLIP_SAMPLE_MODE = "rightSlipSample"; // 采样模式
    public static final String TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX = "rightSlipSampleDetailIndex"; // 采样模式详情索引
    public static final String TOP_SLIP_SAMPLE_DEPTH = "topSlipUsersetDepth"; // 存储深度
    public static final String TOP_SLIP_SAMPLE_SEGMENTED_STATE = "topSlipSampleSegmentedState"; // 分段采样状态
    public static final String TOP_SLIP_SAMPLE_SEGMENTED_NUMBER = "topSlipSampleSegmentedNumber"; // 分段采样段数
    public static final String TOP_SLIP_SAMPLE_SEGMENTED_NUMBER_USERDEFINE = "topSlipSampleSegmentedNumberUserDefine"; // 分段采样自定义段数
    public static final String TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY = "topSlipSampleSegmentedDisplay"; // 分段采样显示
    public static final String TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_START = "topSlipSampleSegmentedDisplayFitStart"; // 分段采样显示适配起始
    public static final String TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_END = "topSlipSampleSegmentedDisplayFitEnd"; // 分段采样显示适配结束
    public static final String TOP_SLIP_SAMPLE_SEGMENTED_ORDER = "topSlipSampleSegmentedPlay"; // 分段采样播放顺序
    public static final String MAIN_CENTER_SAMPLE_SEGMENTED_PLAY_SPEED = "mainCenterSampleSegmentedPlaySpeed"; // 分段采样播放速度
    public static final String TOP_SLIP_DISPLAY = "topSlipDisplay"; // 显示菜单
    public static final String TOP_SLIP_DISPLAY_COMMON_HORREF = "topSlipDisplayCommonHorRef"; // 显示通用-水平Ref
    public static final String TOP_SLIP_DISPLAY_COMMON_TIMEBASE = "topSlipDisplayCommonTimeBase"; // 显示通用-时基
    public static final String TOP_SLIP_DISPLAY_COMMON_ROLL = "topSlipDisplayCommonRoll"; // 显示通用-滚屏
    public static final String TOP_SLIP_DISPLAY_COMMON_CCT = "topSlipDisplayCommonCCT"; // 显示通用-CCT色温
    public static final String TOP_SLIP_DISPLAY_COMMON_SCALE= "topSlipDisplayCommonScale"; // 显示通用-刻度
    public static final String TOP_SLIP_DISPLAY_COMMON_ALPHA = "topSlipDisplayCommonAlpha"; // 显示通用-透明度
    public static final String TOP_SLIP_DISPLAY_WAVEFORM_DRAWTYPE = "topSlipDisplayWaveformDrawType"; // 波形绘制类型
    public static final String TOP_SLIP_DISPLAY_WAVEFORM_BACKGROUND = "topSlipDisplayWaveformBackground"; // 波形背景色
    public static final String TOP_SLIP_DISPLAY_WAVEFORM_BRIGHT = "topSlipDisplayWaveformBright"; // 波形亮度
    public static final String TOP_SLIP_DISPLAY_GRATICULE_MODE = "topSlipDisplayGraticuleMode"; // 网格模式
    public static final String TOP_SLIP_DISPLAY_GRATICULE_INTENSITY = "topSlipDisplayGraticuleIntensity"; // 网格亮度
    public static final String TOP_SLIP_DISPLAY_FFT_PERSIST_PERSIST = "topSlipDisplayFftPersistPersist"; // FFT余辉-余辉值
    public static final String TOP_SLIP_DISPLAY_PERSIST_PERSIST = "topSlipDisplayPersistPersist"; // 余辉-余辉值
    public static final String TOP_SLIP_DISPLAY_FFT_PERSIST_SELECT = "topSlipDisplayFftPersistSELECT"; // FFT余辉-选择
    public static final String TOP_SLIP_DISPLAY_PERSIST_SELECT = "topSlipDisplayPersistSELECT"; // 余辉-选择
    public static final String TOP_SLIP_AUTO = "topSlipAuto"; // 自动菜单
    public static final String TOP_SLIP_AUTO_SET_CHANNEL = "topSlipAutoSetChannel"; // 自动设置通道
    public static final String TOP_SLIP_AUTO_SET_SOURCE = "topSlipAutoSetSource"; // 自动设置源
    public static final String TOP_SLIP_AUTO_SET_LEVELSELECT = "topSlipAutoSetLevelSelect"; // 自动设置电平选择
    public static final String TOP_SLIP_AUTO_SET_LEVELDETAIL = "topSlipAutoSetLevelDetail";//保存的是实际的mV数值int型，最大值为99V999mV
    public static final String TOP_SLIP_AUTO_RANGE_RANGE = "topSlipAutoRangeRange"; // 自动范围-范围
    public static final String TOP_SLIP_AUTO_RANGE_VERTICAL = "topSlipAutoRangeVertical"; // 自动范围-垂直
    public static final String TOP_SLIP_AUTO_RANGE_HORIZONTAL = "topSlipAutoRangeHorizontal"; // 自动范围-水平
    public static final String TOP_SLIP_AUTO_RANGE_LEVEL = "topSlipAutoRangeLevel"; // 自动范围-电平
    public static final String TOP_SLIP_FREQUENCY_METER = "topSlipFrequencyMeter"; // 频率计
    public static final String TOP_SLIP_USERSET = "topSlipUserset"; // 用户设置菜单
    //    public static final String TOP_SLIP_USERSET_DEPTH = "topSlipUsersetDepth";
    public static final String TOP_SLIP_USERSET_TIMESTAMP = "topSlipUsersetTimestamp"; // 用户设置-时间戳
    public static final String TOP_SLIP_USERSET_SCREENINVERT = "topSlipUsersetScreenInvert"; // 用户设置-屏幕反转
    public static final String TOP_SLIP_USERSET_SAVETHUMBNAIL = "topSlipUsersetSaveThumbnail"; // 用户设置-保存缩略图
    public static final String TOP_SLIP_USERSET_TRIGGER = "topSlipUsersetTrigger"; // 用户设置-触发
    public static final String TOP_SLIP_USERSET_CLOCK = "topSlipUsersetClock"; // 用户设置-时钟
    public static final String TOP_SLIP_USERSET_REF_TIMEBASE = "topSlipUserSetRefTimeBase"; // 用户设置-Ref时基

    public static final String TOP_SLIP_TRIGGER = "topSlipTrigger"; // 触发菜单
    public static final String TOP_SLIP_TRIGGER_COMMON_TIME = "topSlipTriggerCommonTime"; // 触发通用-时间
    public static final String TOP_SLIP_TRIGGER_COMMON_MODE = "topSlipTriggerCommonMode"; // 触发通用-模式

    public static final String TOP_SLIP_TRIGGER_SENSITIVITY = "topSlipTriggerSensitivity"; // 触发灵敏度
    /**
     * trigger中所有source公用一套channel，包括当前选择及triggerLevel值
     */
    public static final String TOP_SLIP_TRIGGER_SOURCE = "topSlipTriggerSource";
    public static final String TOP_SLIP_TRIGGER_EDGE_EDGE = "topSlipTriggerEdgeEdge"; // 边沿触发-边沿类型(上升/下降/双边沿)
    public static final String TOP_SLIP_TRIGGER_EDGE_COUPLE = "topSlipTriggerEdgeCouple"; // 边沿触发-耦合
    public static final String TOP_SLIP_TRIGGER_PULSEWIDTH_POLAR = "topSlipTriggerPulsewidthPolar"; // 脉宽触发-极性
    public static final String TOP_SLIP_TRIGGER_PULSEWIDTH_CONDITION = "topSlipTriggerPulsewidthCondition"; // 脉宽触发-条件(大于/小于/等于)
    public static final String TOP_SLIP_TRIGGER_PULSEWIDTH_PULSEWIDTH = "topSlipTriggerPulsewidthPulsewidth"; // 脉宽触发-脉宽值
    public static final String TOP_SLIP_TRIGGER_PULSEWIDTH_TIME_HIGH = "topSlipTriggerPulsewidthTimeHigh"; // 脉宽触发-时间上限
    public static final String TOP_SLIP_TRIGGER_PULSEWIDTH_TIME_LOW = "topSlipTriggerPulsewidthTimeLow"; // 脉宽触发-时间下限
    public static final String TOP_SLIP_TRIGGER_LOGIC_CH1 = "topSlipTriggerLogicCh1"; // 逻辑触发-CH1逻辑值
    public static final String TOP_SLIP_TRIGGER_LOGIC_CH2 = "topSlipTriggerLogicCh2"; // 逻辑触发-CH2逻辑值
    public static final String TOP_SLIP_TRIGGER_LOGIC_CH3 = "topSlipTriggerLogicCh3"; // 逻辑触发-CH3逻辑值
    public static final String TOP_SLIP_TRIGGER_LOGIC_CH4 = "topSlipTriggerLogicCh4"; // 逻辑触发-CH4逻辑值
    public static final String TOP_SLIP_TRIGGER_LOGIC_CH5 = "topSlipTriggerLogicCh5"; // 逻辑触发-CH5逻辑值
    public static final String TOP_SLIP_TRIGGER_LOGIC_CH6 = "topSlipTriggerLogicCh6"; // 逻辑触发-CH6逻辑值
    public static final String TOP_SLIP_TRIGGER_LOGIC_CH7 = "topSlipTriggerLogicCh7"; // 逻辑触发-CH7逻辑值
    public static final String TOP_SLIP_TRIGGER_LOGIC_CH8 = "topSlipTriggerLogicCh8"; // 逻辑触发-CH8逻辑值
    public static final String TOP_SLIP_TRIGGER_LOGIC_LOGIC = "topSlipTriggerLogicLogic"; // 逻辑触发-逻辑运算(AND/OR)
    public static final String TOP_SLIP_TRIGGER_LOGIC_CONDITION = "topSlipTriggerLogicCondition"; // 逻辑触发-条件
    public static final String TOP_SLIP_TRIGGER_LOGIC_TVLOGIC = "topSlipTriggerLogicTvLogic"; // 逻辑触发-TV逻辑
    public static final String TOP_SLIP_TRIGGER_LOGIC_TIME_HIGH = "topSlipTriggerLogicTimeHigh"; // 逻辑触发-时间上限
    public static final String TOP_SLIP_TRIGGER_LOGIC_TIME_LOW = "topSlipTriggerLogicTimeLow"; // 逻辑触发-时间下限
    public static final String TOP_SLIP_TRIGGER_NEDGE_SLOPE = "topSlipTriggerNEdgeSlope"; // N边沿触发-斜率
    public static final String TOP_SLIP_TRIGGER_NEDGE_IDLE = "topSlipTriggerNEdgeIdle"; // N边沿触发-空闲电平
    public static final String TOP_SLIP_TRIGGER_NEDGE_EDGE = "topSlipTriggerNEdgeEdge"; // N边沿触发-边沿数
    public static final String TOP_SLIP_TRIGGER_RUNT_POLAR = "topSlipTriggerRuntPolar"; // 矮脉冲触发-极性
    public static final String TOP_SLIP_TRIGGER_RUNT_CONDITION = "topSlipTriggerRuntCondition"; // 矮脉冲触发-条件
    public static final String TOP_SLIP_TRIGGER_RUNT_TIME_HIGH = "topSlipTriggerRuntTimeHigh"; // 矮脉冲触发-时间上限
    public static final String TOP_SLIP_TRIGGER_RUNT_TIME_LOW = "topSlipTriggerRuntTimeLow"; // 矮脉冲触发-时间下限
    public static final String TOP_SLIP_TRIGGER_SLOPE_EDGE = "topSlipTriggerSlopeEdge"; // 斜率触发-边沿
    public static final String TOP_SLIP_TRIGGER_SLOPE_CONDITION = "topSlipTriggerSlopeCondition"; // 斜率触发-条件
    public static final String TOP_SLIP_TRIGGER_SLOPE_TIME_HIGH = "topSlipTriggerSlopeTimeHigh"; // 斜率触发-时间上限
    public static final String TOP_SLIP_TRIGGER_SLOPE_TIME_LOW = "topSlipTriggerSlopeTimeLow"; // 斜率触发-时间下限
    public static final String TOP_SLIP_TRIGGER_TIMEOUT_POLAR = "topSlipTriggerTimeoutPolar"; // 超时触发-极性
    public static final String TOP_SLIP_TRIGGER_TIMEOUT_OVERTIME = "topSlipTriggerTimeoutOverTime"; // 超时触发-超时时间
    public static final String TOP_SLIP_TRIGGER_VIDEO_POLAR = "topSlipTriggerVideoPolar"; // 视频触发-极性
    public static final String TOP_SLIP_TRIGGER_VIDEO_STANDARD = "topSlipTriggerVideoStandard"; // 视频触发-标准(PAL/NTSC)
    public static final String TOP_SLIP_TRIGGER_VIDEO_TRIGGER = "topSlipTriggerVideoTrigger"; // 视频触发-触发方式
    public static final String TOP_SLIP_TRIGGER_VIDEO_FREQUENCY = "topSlipTriggerVideoFrequency"; // 视频触发-频率
    public static final String TOP_SLIP_TRIGGER_VIDEO_LINE = "topSlipTriggerVideoLine"; // 视频触发-行号
    public static final String TOP_SLIP_TRIGGER_SERIALS_UART = "topSlipTriggerSerialsUart"; // 串口触发-UART
    public static final String TOP_SLIP_TRIGGER_SERIALS_LIN = "topSlipTriggerSerialsLin"; // 串口触发-LIN
    public static final String TOP_SLIP_TRIGGER_SERIALS_CAN = "topSlipTriggerSerialsCan"; // 串口触发-CAN
    public static final String TOP_SLIP_TRIGGER_SERIALS_SPI = "topSlipTriggerSerialsSpi"; // 串口触发-SPI
    public static final String TOP_SLIP_TRIGGER_SERIALS_I2C = "topSlipTriggerSerialsI2c"; // 串口触发-I2C
    public static final String TOP_SLIP_TRIGGER_SERIALS_M429 = "topSlipTriggerSerialsM429"; // 串口触发-M429
    public static final String TOP_SLIP_TRIGGER_SERIALS_M1553B = "topSlipTriggerSerialsM1553b"; // 串口触发-M1553B

    public static final String TOP_SLIP_TRIGGER_SERIALS_M429_DATA = "topSlipTriggerSerialsM429DataData"; // M429触发-数据
    public static final String TOP_SLIP_TRIGGER_SERIALS_M429_LABEL = "topSlipTriggerSerialsM429Label"; // M429触发-标签
    public static final String TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_LABEL = "topSlipTriggerSerialsM429LabelDataLabel"; // M429触发-标签+数据-标签
    public static final String TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_DATA = "topSlipTriggerSerialsM429LabelDataData"; // M429触发-标签+数据-数据
    public static final String TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_LABEL = "topSlipTriggerSerialsM429LabelSdiLabel"; // M429触发-标签+SDI-标签
    public static final String TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_SDI = "topSlipTriggerSerialsM429LabelSdiSdi"; // M429触发-标签+SDI-SDI
    public static final String TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_LABEL = "topSlipTriggerSerialsM429LabelSsmLabel"; // M429触发-标签+SSM-标签
    public static final String TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_SSM = "topSlipTriggerSerialsM429LabelSsmSsm"; // M429触发-标签+SSM-SSM
    public static final String TOP_SLIP_TRIGGER_SERIALS_M429_SDI = "topSlipTriggerSerialsM429Sdi"; // M429触发-SDI
    public static final String TOP_SLIP_TRIGGER_SERIALS_M429_SSM = "topSlipTriggerSerialsM429Ssm"; // M429触发-SSM
    public static final String TOP_SLIP_TRIGGER_SERIALS_CAN_DATAID = "topSlipTriggerSerialsCanDataId"; // CAN触发-数据ID
    public static final String TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_ID = "topSlipTriggerSerialsCanIdDataId"; // CAN触发-ID+数据-ID
    public static final String TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC = "topSlipTriggerSerialsCanIdDataDic"; // CAN触发-ID+数据-DLC
    public static final String TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA = "topSlipTriggerSerialsCanIdDataData"; // CAN触发-ID+数据-数据
    public static final String TOP_SLIP_TRIGGER_SERIALS_CAN_RDID = "topSlipTriggerSerialsCanRdId"; // CAN触发-远程帧ID
    public static final String TOP_SLIP_TRIGGER_SERIALS_CAN_REMOTEID = "topSlipTriggerSerialsCanRemoteId"; // CAN触发-远程ID
    public static final String TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_ADDR = "topSlipTriggerSerialsI2c10WriteFrameAddr"; // I2C触发-10位写帧地址
    public static final String TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_DATA = "topSlipTriggerSerialsI2c10WriteFrameData"; // I2C触发-10位写帧数据
    public static final String TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_ADDR = "topSlipTriggerSerialsI2cFrame1Addr"; // I2C触发-帧1地址
    public static final String TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_DATA = "topSlipTriggerSerialsI2cFrame1Data"; // I2C触发-帧1数据
    public static final String TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_ADDR = "topSlipTriggerSerialsI2cFrame2Addr"; // I2C触发-帧2地址
    public static final String TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA1 = "topSlipTriggerSerialsI2cFrame2Data1"; // I2C触发-帧2数据1
    public static final String TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA2 = "topSlipTriggerSerialsI2cFrame2Data2"; // I2C触发-帧2数据2
    public static final String TOP_SLIP_TRIGGER_SERIALS_I2C_NOACKINADR = "topSlipTriggerSerialsI2cNoAckInAdr"; // I2C触发-地址中无ACK
    public static final String TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_CONDITION = "topSlipTriggerSerialsI2cRomDataCondition"; // I2C触发-ROM数据条件
    public static final String TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_DATA = "topSlipTriggerSerialsI2cRomDataData"; // I2C触发-ROM数据
    public static final String TOP_SLIP_TRIGGER_SERIALS_LIN_FRAMEID = "topSlipTriggerSerialsLinFrameId"; // LIN触发-帧ID
    public static final String TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_ID = "topSlipTriggerSerialsLinIdDataId"; // LIN触发-ID+数据-ID
    public static final String TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA = "topSlipTriggerSerialsLinIdDataData"; // LIN触发-ID+数据-数据
    public static final String TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID = "topSlipTriggerSerialsLinDataDataId";//用于存储帧ID输入为X时候的值
    public static final String TOP_SLIP_TRIGGER_SERIALS_M1553B_CSWORD = "topSlipTriggerSerialsM1553bCsWord"; // M1553B触发-命令字
    public static final String TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD = "topSlipTriggerSerialsM1553bDataWord"; // M1553B触发-数据字
    public static final String TOP_SLIP_TRIGGER_SERIALS_M1553B_RTADDR = "topSlipTriggerSerialsM1553bRrAddr"; // M1553B触发-RT地址
    public static final String TOP_SLIP_TRIGGER_SERIALS_SPI_DATA = "topSlipTriggerSerialsSpiData"; // SPI触发-数据
    public static final String TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_CONDITION = "topSlipTriggerSerialsUart0DataCondition"; // UART触发-0数据条件
    public static final String TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_EDIT = "topSlipTriggerSerialsUart0DataEdit"; // UART触发-0数据编辑值
    public static final String TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_CONDITION = "topSlipTriggerSerialsUart1DataCondition"; // UART触发-1数据条件
    public static final String TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT = "topSlipTriggerSerialsUart1DataEdit"; // UART触发-1数据编辑值
    public static final String TOP_SLIP_TRIGGER_SERIALS_UART_DATA_CONDITION = "topSlipTriggerSerialsUartDataCondition"; // UART触发-数据条件
    public static final String TOP_SLIP_TRIGGER_SERIALS_UART_DATA_EDIT = "topSlipTriggerSerialsUartDataEdit"; // UART触发-数据编辑值
    public static final String TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_CONDITION = "topSlipTriggerSerialsUartxDataCondition"; // UART触发-X数据条件
    public static final String TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_EDIT = "topSlipTriggerSerialsUartxDataEdit"; // UART触发-X数据编辑值


    public static final String WAVE_TYPE_WAV = "0"; // 波形类型：WAV
    public static final String WAVE_TYPE_CSV = "1"; // 波形类型：CSV
    public static final String WAVE_TYPE_BIN = "2"; // 波形类型：BIN
    public static final String SAVE_TYPE_SETTING = "3"; // 保存类型：设置
    public static final String SAVE_TYPE_PICTURE = "4"; // 保存类型：图片
    public static final String SAVE_TYPE_SESSION = "5"; // 保存类型：会话
    public static final String SAVE_TYPE_AUTOSAVE = "6"; // 保存类型：自动保存
    public static final String TOP_SLIP_WAVE_INVOKE_TYPE = "topSlipWaveInvokeType";//当前选中的波形文件类型

    public static final String TOP_SLIP_WAVE_FILE_PATH = "topSlipWaveFilePath";//调用 wav/csv/bin文件路径
    public static final String TOP_SLIP_WAVE_FILE_ABSOLUTE_PATH = "topSlipWaveFileAbsolutePath";//调用 wav/csv/bin文件 绝对路径
    public static final String TOP_SLIP_WAVE_FILE_PATH_CURRENT = "topSlipWaveFilePathCurrent";//调用 当前wav/csv/bin文件路径

    public static final String TOP_SLIP_SETTING_FILE_PATH = "topSlipSettingFilePath";//调用 设置文件路径
    public static final String TOP_SLIP_SETTING_FILE_ABSOLUTE_PATH = "topSlipSettingFileAbsolutePath";//调用 设置文件绝对路径
    public static final String TOP_SLIP_SETTING_FILE_PATH_CURRENT = "topSlipSettingFilePathCurrent";//调用 当前设置文件路径

    public static final String TOP_SLIP_SESSION_FILE_PATH = "topSlipSessionFilePath";//调用 会话文件路径
    public static final String TOP_SLIP_SESSION_FILE_ABSOLUTE_PATH = "topSlipSessionFileAbsolutePath";//调用 会话文件绝对路径
    public static final String TOP_SLIP_SESSION_FILE_PATH_CURRENT = "topSlipSessionFilePathCurrent";//调用 当前会话文件路径

    public static final String TOP_SLIP_SPINNER_ITEM_INDEX = "topSlipSpinnerItemIndex";//spinner中 旋钮 到第几个item

    /**
     * 串型文本
     */
    public static final String SERIAL_TXT_CURRTAB = "SerialTxtCurrTab";//文本当前选择的Tab页
    public static final String SERIAL_TXT_SELECT = "SerialTxtSelect";//是否参加文本组合显示

    /**
     * 触发电平阈值电平的相对0频线的位置
     * 当前通道0频线的px（通道控件画图使用） = 相对0频线位置px（计算数据使用） + 相对屏幕位置px（电平控件画图使用）
     */
    public static final String HIGH = "high";                         //当电平需要两个时，高的那个需要添加HIGH
    public static final String TRIGGER_CHANNEL = "TRIGGER_CHANNEL";   //用于一般触发的触发电平值
    public static final String VALUE_CHANNEL = "VALUE_CHANNEL";       //用于串口的阈值电平
    public static final String TRIGGER_CHANNEL_H = TRIGGER_CHANNEL + HIGH; // 高触发电平key后缀
    public static final String VALUE_CHANNEL_H = VALUE_CHANNEL + HIGH; // 高阈值电平key后缀
    public static final String TRIGGER_CHANNEL_TEMP = "TRIGGER_CHANNEL_TEMP";   //用于超出屏幕外的触发电平值的显示
    public static final String VALUE_CHANNEL_TEMP = "VALUE_CHANNEL_TEMP";       //用于超出屏幕外的阈值电平值的显示
    public static final String TRIGGER_CHANNEL_TEMP_H = TRIGGER_CHANNEL_TEMP + HIGH; // 超出屏幕外的高触发电平key后缀
    public static final String VALUE_CHANNEL_TEMP_H = VALUE_CHANNEL_TEMP + HIGH; // 超出屏幕外的高阈值电平key后缀

    //region 临时变量
    // BUG:0009859
    //使用情况：当触发不在串型触时，切入串型文本解码后，自动跳转串型触发，当无操作，返回YT模式要切换到之前的触发类型，否则不切换到之前的触发类型
    //是否操作的定义包括：二级标题及内容的选择，点击等操作
    public static final String SAVE_TEMP_TRIGGER_INDEX = "SAVE_TEMP_TRIGGER_INDEX"; // 临时保存的触发类型索引
    public static final String SAVE_TEMP_TRIGGER_IS_OPTION = "SAVE_TEM_TRIGGER_IS_OPTION"; // 临时保存的触发是否已操作
    //endregion
    //BUG:08887
    public static final String SAVE_TEMP_IS_CALIBRATION = "SAVE_TEMP_IS_CALIBRATION"; // 临时保存的校准标记

    public static final String USER_TOUCH = "USER_TOUCH"; // 用户触摸事件标记
    /**
     * 信号	    初始电平值
     * UART	    1V
     * LIN	    1V
     * CAN_H	3V
     * CAN_L	2V
     * H_L	    1V
     * L_H	    1V
     * Rx、Tx	1V
     * SPI	    1V
     * I2C	    1V
     * 1553B	-250mV
     * 429	    高低:±3V
     */
    /**
     * 这里保存的是电平值
     */
    private final double valueInitUart = 1; // UART初始阈值电平：1V
    private final double valueInitLin = 1; // LIN初始阈值电平：1V
    private final double valueInitCan0 = 3; // CAN_H初始阈值电平：3V
    private final double valueInitCan1 = 2; // CAN_L初始阈值电平：2V
    private final double valueInitCan2 = 1; // CAN H_L初始阈值电平：1V
    private final double valueInitCan3 = 1; // CAN L_H初始阈值电平：1V
    private final double valueInitCan4 = 1; // CAN Rx初始阈值电平：1V
    private final double valueInitCan5 = 1; // CAN Tx初始阈值电平：1V
    private final double valueInitSpi = 1; // SPI初始阈值电平：1V
    private final double valueInitI2c = 1; // I2C初始阈值电平：1V
    private final double valueInit429H = 3; // M429高电平初始值：3V
    private final double valueInit429L = -3; // M429低电平初始值：-3V
    private final double valueInit1553b = -0.25; // M1553B初始阈值电平：-250mV
    /**
     * 当前总线类型下的阈值电平值是否是使用的初始值
     * 一般形式的key为：VALUE_INIT + S1 + UART + CH1
     */
    public static final String VALUE_INIT = "VALUE_INIT";
    public static final int S1 = 1; // 串口槽位1
    public static final int S2 = 2; // 串口槽位2
    public static final int S3 = 3; // 串口槽位3
    public static final int S4 = 4; // 串口槽位4
    public static final int UART = 0; // 总线类型：UART
    public static final int LIN = 1; // 总线类型：LIN
    public static final int CAN = 2; // 总线类型：CAN
    public static final int SPI = 3; // 总线类型：SPI
    public static final int I2C = 4; // 总线类型：I2C
    public static final int M429 = 5; // 总线类型：ARINC429(M429)
    public static final int M1553B = 6; // 总线类型：MIL-STD-1553B

    /**
     * 遍历串口槽位S1到S3（不含S4），对每个槽位执行指定操作
     * @param action 对每个槽位编号执行的操作
     */
    public static void foreachS1ToS4(Consumer<Integer> action) {
        for (int i = S1; i < S4; i++) { // 从S1遍历到S3
            action.accept(i); // 对当前槽位执行操作
        }
    }

//    public static final int CH1 = 1;
//    public static final int CH2 = 2;
//    public static final int CH3 = 3;
//    public static final int CH4 = 4;
//    public static final int CH5 = 5;
//    public static final int CH6 = 6;
//    public static final int CH7 = 7;
//    public static final int CH8 = 8;
//    public static final int MATH = 9;
//    public static final int REF1 = 10;
//    public static final int REF2 = 11;
//    public static final int REF3 = 12;
//    public static final int REF4 = 13;
    /**
     * 最后一次更改总线类型的操作是s1进行的还是s2进行的，默认是s1返回1
     */
    public static final String LASTSET_SERIALS = "LASTSET_SERIALS";
    public static final String MAIN_WAVE_CH_Y_ZERO_POSITION = "mainWaveChYZeroPosition"; // 通道零频线Y位置
    public static final String MAIN_WAVE_CH_XY_POSITION = "mainWaveChXYPosition"; // XY模式通道位置
    public static final String MAIN_WAVE_CH_Y_POSITION = "mainWaveChannelYPosition"; // 通道Y位置
    public static final String MAIN_WAVE_CH_Y_POSITION_YT = "mainWaveChannelYPositionYT"; // YT模式通道Y位置
    public static final String MAIN_WAVE_MATH_DW_Y_POSITION = "MAIN_WAVE_MATH_DW_Y_POSITION"; // Math双波形Y位置
    public static final String MAIN_WAVE_MATH_FFT_DB_Y_POSITION = "MAIN_WAVE_MATH_FFT_DB_Y_POSITION"; // Math FFT dB Y位置
    public static final String MAIN_WAVE_MATH_FFT_RMS_Y_POSITION = "MAIN_WAVE_MATH_FFT_RMS_Y_POSITION"; // Math FFT RMS Y位置
    public static final String MAIN_WAVE_MATH_AXB_Y_POSITION = "MAIN_WAVE_MATH_AXB_Y_POSITION"; // Math A×B Y位置
    public static final String MAIN_WAVE_MATH_AM_Y_POSITION = "MAIN_WAVE_MATH_AM_Y_POSITION"; // Math高级运算Y位置
    public static final String MAIN_WAVE_REF_Y_POSITION = "mainWaveRefYPosition"; // Ref通道Y位置
    public static final String MAIN_WAVE_SERIAL_Y_POSITION = "MAIN_WAVE_SERIAL_Y_POSITION"; // 串口通道Y位置
    public static final String MAIN_WAVE_TIMEBASE_POSITION_NORMAL = "MAIN_WAVE_TIMEBASE_POSITION_NORMAL"; // 普通模式时基位置
    public static final String MAIN_WAVE_TIMEBASE_POSITION_FFTMOD = "MAIN_WAVE_TIMEBASE_POSITION_FFTMOD"; // FFT模式时基位置
    public static final String MAIN_WAVE_TIMEBASE_POSITION_RN = "MAIN_WAVE_TIMEBASE_POSITION_RN"; // RefN时基位置

    public static final String MAIN_WAVE_TIMEBASE_MIN = "MAIN_WAVE_TIMEBASE_MIN"; // 时基最小值
    public static final String MAIN_WAVE_TIMEBASE_MAX = "MAIN_WAVE_TIMEBASE_MAX"; // 时基最大值
    public static final String MAIN_WAVE_ZONE_HEIGHT = "MAIN_WAVE_ZONE_HEIGHT";//当前波形区域的高
    public static final String MAIN_WAVE_LABEL_POSITION = "MAIN_WAVE_LABEL_POSITION";//波形标签的水平位置
    public static final String MAIN_CHANNEL_COLOR = "MAIN_CHANNEL_COLOR"; // 通道颜色

    //factoryCalibration
    public static final String CALIBRATION_TOP_ZERO = "CALIBRATION_TOP_ZERO"; // 校准-顶部零点
    public static final String CALIBRATION_TOP_ADGAIN = "CALIBRATION_TOP_ADGAIN"; // 校准-顶部AD增益
    public static final String CALIBRATION_TOP_ADZERO = "CALIBRATION_TOP_ADZERO"; // 校准-顶部AD零点
    public static final String CALIBRATION_CENTER_CH1GAIN = "CALIBRATION_CENTER_CH1GAIN"; // 校准-中心CH1增益
    public static final String CALIBRATION_CENTER_CH2GAIN = "CALIBRATION_CENTER_CH2GAIN"; // 校准-中心CH2增益
    public static final String CALIBRATION_CENTER_CH3GAIN = "CALIBRATION_CENTER_CH3GAIN"; // 校准-中心CH3增益
    public static final String CALIBRATION_CENTER_CH4GAIN = "CALIBRATION_CENTER_CH4GAIN"; // 校准-中心CH4增益
    public static final String CALIBRATION_BOTTOM_ZERO = "CALIBRATION_BOTTOM_ZERO"; // 校准-底部零点
    public static final String CALIBRATION_BOTTOM_OFFSET = "CALIBRATION_BOTTOM_OFFSET"; // 校准-底部偏移
    public static final String CALIBRATION_BOTTOM_CHDIFF = "CALIBRATION_BOTTOM_CHDIFF"; // 校准-底部通道差分

    //其它保存 othermap定义
    public static final String LANGUAGE = "language"; // 系统语言
    public static final String USERSET = "userset"; // 用户设置名称
    public static final String USERSET_DEFAULTNAME = "USERSET_"; // 用户设置默认名称前缀
    public static final String GENNAME_INDEXDATE_WAV = "genName_indexDate_WAV"; // WAV文件名日期索引
    public static final String GENNAME_INDEXDATE_CSV = "genName_indexDate_CSV"; // CSV文件名日期索引
    public static final String GENNAME_INDEXDATE_BIN = "genName_indexDate_BIN"; // BIN文件名日期索引
    public static final String GENNAME_INDEXDATE_SBT = "genName_indexDate_SBT";//seriasBusText
    public static final String GENNAME_INDEX_WAV = "genName_index_WAV"; // WAV文件名序号索引
    public static final String GENNAME_INDEX_CSV = "genName_index_CSV"; // CSV文件名序号索引
    public static final String GENNAME_INDEX_BIN = "genName_index_BIN"; // BIN文件名序号索引
    public static final String GENNAME_INDEX_SBT = "genName_index_SBT";//seriasBusText
    public static final String GENNAME_INDEX_FIRST = "0001"; // 文件名序号初始值
    public static final String MAIN_BOTTOM_USB_PATH = "MAIN_BOTTOM_USB_PATH"; // USB存储路径

    public static final String LAST_OBJECT_IS_CURSOR = "LAST_OBJECT_IS_CURSOR";
    //endregion

    //region 文件名定义
    public static final String DefaultSaveName = "DefaultSaveName";
    public static final String OtherDefaultSaveName = "otherDefault";
    //endregion

    //region 单例创建
    private static CacheUtil cacheUtil; // 单例实例引用

    /**
     * 私有构造函数，初始化缓存
     * 清空两个HashMap，加载otherMap默认值，初始化缓存机制
     */
    private CacheUtil() {
        map.clear(); // 清空主参数缓存
        otherMap.clear(); // 清空杂项缓存
//        getCacheMap(); // 不在此处加载主缓存，延迟到首次读取时加载
        getOtherMap(); // 加载杂项缓存默认值
        initCache(); // 初始化缓存机制（定时持久化等）
    }

    /**
     * 获取CacheUtil单例实例（懒汉式）
     * 首次调用时创建实例，后续调用直接返回已有实例
     * @return CacheUtil单例实例
     */
    public static CacheUtil get() {
        if (cacheUtil == null) { // 如果单例尚未创建
            cacheUtil = new CacheUtil(); // 创建新实例
        }
        return cacheUtil; // 返回单例实例
    }
    //endregion

    //region map读写
    private HashMap<String, String> map = new HashMap<>(); // 主参数缓存，存储所有UI参数（通道/触发/串口/测量/显示等）
    /**
     * 杂项：保存记录名称、语言
     */
    private HashMap<String, String> otherMap = new HashMap<>(); // 杂项缓存，存储路径/文件名/校准/语言/序号等辅助参数


    /**
     * 从持久化文件加载主参数缓存（延迟加载）
     * 仅在map为空时执行加载，避免重复IO操作
     */
    private void loadMapFromUserSet() {
        if (map.isEmpty()) {
            try {
                SaveManage.getInstance().loadUserSet(DefaultSaveName, map);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从持久化文件加载杂项缓存（延迟加载）
     * 仅在otherMap为空时执行加载，避免重复IO操作
     */
    private void loadOtherMapFromUserSet() {
        if (otherMap.isEmpty()) {
            try {
                SaveManage.getInstance().loadUserSet(OtherDefaultSaveName, otherMap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断当前是否为Ref时基模式
     * @return true表示Ref时基模式，false表示普通时基模式
     */
    public boolean isRefTimebase(){
        return bRefTimebase; // 返回Ref时基标记
    }
    private boolean bRefTimebase = false; // Ref时基模式标记（由clearTempSaveParam设置）

    /**
     * 清除临时保存的参数
     * 处理校准标记和Ref时基模式标记，在加载参数后调用
     * @param map 要清除临时参数的缓存Map
     */
    public void clearTempSaveParam(HashMap<String, String> map) {
        if (map != null ) {
            if(map.containsKey(SAVE_TEMP_IS_CALIBRATION)) {
                map.put(SAVE_TEMP_IS_CALIBRATION, String.valueOf(false));
            }
            String key = TOP_SLIP_USERSET_REF_TIMEBASE;
            if(map.containsKey(key)){
                int v =  Integer.parseInt(map.get(key));
                bRefTimebase = v == 0;
                if(bRefTimebase){
                    map.put(key, String.valueOf(1));
                }
            }else{
                bRefTimebase = true;
            }
        }

    }

    /**
     * 从主缓存读取int类型值
     * 如果缓存中没有该key，从默认值初始化表获取
     * TValue类型参数默认返回-1，其他默认返回0
     * @param key 缓存键名
     * @return 对应的int值
     */
    public int getInt(String key) {
        loadMapFromUserSet();

        if (!map.containsKey(key)) {
            String s = getValueFromInit(key);
            if (TOP_SLIP_MEASURE_TVALUE_X1.equals(key)
                    || TOP_SLIP_MEASURE_TVALUE_X2.equals(key)) {
                s = !StrUtil.isEmpty(s) && StrUtil.isNumber(s) ? s : "-1";
            } else{
                s = !StrUtil.isEmpty(s) && StrUtil.isNumber(s) ? s : "0";
            }
            map.put(key, s);
            return Integer.parseInt(s);
        } else {
//            if (key.contains(VALUE_CHANNEL)) {//说明是阈值电平值的获取
//                int bus1Type = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + S1);
//                int bus2Type = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + S2);
//                int bus3Type = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + S3);
//                int bus4Type = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + S4);
//                boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
//                boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
//                boolean s3Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
//                boolean s4Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
//                int ch;
//                if (key.contains(VALUE_CHANNEL_TEMP_H)) {
//                    ch = Integer.parseInt(key.replace(VALUE_CHANNEL_TEMP_H, ""));
//                } else if (key.contains(VALUE_CHANNEL_TEMP)) {
//                    ch = Integer.parseInt(key.replace(VALUE_CHANNEL_TEMP, ""));
//                } else if (key.contains(VALUE_CHANNEL_H)) {
//                    ch = Integer.parseInt(key.replace(VALUE_CHANNEL_H, ""));
//                } else {
//                    ch = Integer.parseInt(key.replace(VALUE_CHANNEL, ""));
//                }
//                boolean isInit = false;
//                int serials = 1;
//                int busType = -1;
//                if (serialsId != 0) {
//                    serials = serialsId;
//                    if (serialsId == S1) {
//                        busType = bus1Type;
//                    } else if (serialsId == S2) {
//                        busType = bus2Type;
//                    } else if (serialsId == S3) {
//                        busType = bus3Type;
//                    } else if (serialsId == S4) {
//                        busType = bus4Type;
//                    }
//                    isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + serialsId + busType + ch);
//                } else if (s1Check && !s2Check && !s3Check && !s4Check) {
//                    serials = 1;
//                    busType = bus1Type;
//                    isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + S1 + bus1Type + ch);
//                } else if (s2Check && !s1Check && !s3Check && !s4Check) {
//                    serials = 2;
//                    busType = bus2Type;
//                    isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + S2 + bus2Type + ch);
//                } else if (s3Check && !s1Check && !s2Check && !s4Check) {
//                    serials = 3;
//                    busType = bus3Type;
//                    isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + S3 + bus3Type + ch);
//                } else if (s4Check && !s1Check && !s2Check && !s3Check) {
//                    serials = 4;
//                    busType = bus4Type;
//                    isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + S4 + bus4Type + ch);
//                } else if (s1Check && s2Check && s3Check && s4Check) {
//                    serials = getLastSetSerials();
//                    if (serials == S1) {
//                        busType = bus1Type;
//                    } else if (serials == S2) {
//                        busType = bus2Type;
//                    } else if (serials == S3) {
//                        busType = bus3Type;
//                    } else if (serials == S4) {
//                        busType = bus4Type;
//                    }
//                    isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + serials + busType + ch);
//                }
//                Logger.d(TAG, "serials:" + serials + ",busType:" + busType + ",ch:" + ch + ",isInit:" + isInit);
//                if (isInit) {
//
////                    Logger.d(TAG,"serials:" + serials + ",busType:" + busType);
//                    double vScaleVal = 1;
////                    if (ch == TChan.Ch1) {
////                        vScaleVal = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
////                                .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH1_VSCALEID));
////                        vScaleVal *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CH1));
////                        vScaleVal /= ScopeBase.getVerticalPerGridPixels();
////                    } else if (ch == TChan.Ch2) {
////                        vScaleVal = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
////                                .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH2_VSCALEID));
////                        vScaleVal *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CH2));
////                        vScaleVal /= ScopeBase.getVerticalPerGridPixels();
////                    } else if (ch == TChan.Ch3) {
////                        vScaleVal = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
////                                .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH3_VSCALEID));
////                        vScaleVal *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CH3));
////                        vScaleVal /= ScopeBase.getVerticalPerGridPixels();
////                    } else if (ch == TChan.Ch4) {
////                        vScaleVal = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
////                                .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH4_VSCALEID));
////                        vScaleVal *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CH4));
////                        vScaleVal /= ScopeBase.getVerticalPerGridPixels();
////                    }
//                    if (TChan.isChan(ch)) {
//                        Channel chan = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(ch));
//                        if (chan == null) return 0;
//                        vScaleVal = chan.getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_SCALE_ID + ch));
//                        vScaleVal *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + ch));
//                        vScaleVal /= ScopeBase.getVerticalPerGridPixels();
//                    }
//
//                    switch (busType) {
//                        case RightLayoutSerials.SERIALS_UART:
//                            return (int) (valueInitUart / vScaleVal);
//                        case RightLayoutSerials.SERIALS_LIN:
//                            return (int) (valueInitLin / vScaleVal);
//                        case RightLayoutSerials.SERIALS_CAN:
//                            int signal = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_SIGNAL + serials);
//                            switch (signal) {
//                                case 0:
//                                    return (int) (valueInitCan0 / vScaleVal);
//                                case 1:
//                                    return (int) (valueInitCan1 / vScaleVal);
//                                case 2:
//                                    return (int) (valueInitCan2 / vScaleVal);
//                                case 3:
//                                    return (int) (valueInitCan3 / vScaleVal);
//                                case 4:
//                                    return (int) (valueInitCan4 / vScaleVal);
//                                case 5:
//                                    return (int) (valueInitCan5 / vScaleVal);
//                            }
//                        case RightLayoutSerials.SERIALS_SPI:
//                            return (int) (valueInitSpi / vScaleVal);
//                        case RightLayoutSerials.SERIALS_I2C:
//                            return (int) (valueInitI2c / vScaleVal);
//                        case RightLayoutSerials.SERIALS_M429:
//                            if (key.contains(HIGH)) {
//                                return (int) (valueInit429H / vScaleVal);
//                            } else {
//                                return (int) (valueInit429L / vScaleVal);
//                            }
//                        case RightLayoutSerials.SERIALS_M1553B:
//                            return (int) (valueInit1553b / vScaleVal);
//                    }
//                }
//            }

            if (map.get(key).contains("b/s")) {
                return TBookUtil.getIntFromBaudRate(map.get(key));
            }else{
                return Integer.parseInt(map.get(key));
            }
        }
    }

    /**
     * 此参数主要用于界面初始化时阈值电平的获取。阈值电平的一般值是不区分s1和s2的，但是初始值是区分的。
     * 0表示未指定，1~4对应S1~S4
     */
    private int serialsId = 0; // 当前操作的串口通道编号

    /**
     * 设置当前操作的串口通道编号（用于阈值电平初始值计算）
     * @param serialsId 串口通道编号（S1=1, S2=2, S3=3, S4=4）
     */
    public void setValueLevelSerials(int serialsId) {
        this.serialsId = serialsId;
    }

    /**
     * 获取当前操作的串口通道编号
     * @return 串口通道编号（0=未指定, 1=S1, 2=S2, 3=S3, 4=S4）
     */
    public int getValueLevelSerials() {
        return this.serialsId;
    }

    /**
     * 获取最后一次设置总线类型的串口通道编号
     * 根据各串口通道的开启状态判断：仅一个开启则返回该通道，多个开启则返回LASTSET_SERIALS
     * @return 串口通道编号（1~4），0表示无串口通道开启
     */
    private int getLastSetSerials() {
        boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        boolean s3Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        boolean s4Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
        if (!s1Check && !s2Check && !s3Check && !s4Check) {
            return 0;
        } else if (s1Check && !s2Check && !s3Check && !s4Check) {
            return 1;
        } else if (!s1Check && s2Check && !s3Check && !s4Check) {
            return 2;
        } else if (!s1Check && !s2Check && s3Check && !s4Check) {
            return 3;
        } else if (!s1Check && !s2Check && !s3Check && s4Check) {
            return 4;
        } else {
            return CacheUtil.get().getInt(CacheUtil.LASTSET_SERIALS);
        }
    }

    /**
     * 判断指定key对应的阈值电平是否使用初始值
     * 根据当前串口通道的总线类型和通道号，查询VALUE_INIT标记
     * @param key 阈值电平的缓存key（包含VALUE_CHANNEL前缀）
     * @return true表示使用初始值，false表示使用用户自定义值
     */
    public boolean isValueInit(String key) {
        boolean isInit = false;
        if (key.contains(VALUE_CHANNEL)) {//说明是阈值电平值的获取
            int bus1Type = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + S1);
            int bus2Type = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + S2);
            int bus3Type = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + S3);
            int bus4Type = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + S4);
            boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
            boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
            boolean s3Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
            boolean s4Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
            int ch;
            if (key.contains(VALUE_CHANNEL_TEMP_H)) {
                ch = Integer.parseInt(key.replace(VALUE_CHANNEL_TEMP_H, ""));
            } else if (key.contains(VALUE_CHANNEL_TEMP)) {
                ch = Integer.parseInt(key.replace(VALUE_CHANNEL_TEMP, ""));
            } else if (key.contains(VALUE_CHANNEL_H)) {
                ch = Integer.parseInt(key.replace(VALUE_CHANNEL_H, ""));
            } else {
                ch = Integer.parseInt(key.replace(VALUE_CHANNEL, ""));
            }
            int serials = 1;
            int busType = -1;
            if (serialsId != 0) {
                serials = serialsId;
                if (serialsId == S1) {
                    busType = bus1Type;
                } else if (serialsId == S2) {
                    busType = bus2Type;
                } else if (serialsId == S3) {
                    busType = bus3Type;
                } else if (serialsId == S4) {
                    busType = bus4Type;
                }
                isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + serialsId + busType + ch);
            } else if (s1Check && !s2Check && !s3Check && !s4Check) {
                isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + S1 + bus1Type + ch);
                serials = 1;
                busType = bus1Type;
            } else if (!s1Check && s2Check && !s3Check && !s4Check) {
                isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + S2 + bus2Type + ch);
                serials = 2;
                busType = bus2Type;
            } else if (!s1Check && !s2Check && s3Check && !s4Check) {
                isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + S3 + bus3Type + ch);
                serials = 3;
                busType = bus3Type;
            } else if (!s1Check && !s2Check && !s3Check && s4Check) {
                isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + S4 + bus4Type + ch);
                serials = 4;
                busType = bus4Type;
            } else if (s1Check && s2Check && s3Check && s4Check) {
                serials = getLastSetSerials();

                if (serials == S1) {
                    busType = bus1Type;
                } else if (serials == S2) {
                    busType = bus2Type;
                } else if (serials == S3) {
                    busType = bus3Type;
                } else if (serials == S4) {
                    busType = bus4Type;
                }
                isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + serials + busType + ch);
            }


        }
        return isInit;
    }

    /**
     * 从主缓存读取double类型值
     * 对于阈值电平，如果使用初始值，则根据总线类型和通道垂直档位计算初始电平值
     * @param key 缓存键名
     * @return 对应的double值
     */
    public double getDouble(String key) {
        loadMapFromUserSet();
        if (!map.containsKey(key)) {
            String s = getValueFromInit(key);
            s = !StrUtil.isEmpty(s) && StrUtil.isNumber(s) ? s : "0";
            map.put(key, s);
            return Double.parseDouble(s);
        } else {

            if (key.contains(VALUE_CHANNEL)) {//说明是阈值电平值的获取
                int bus1Type = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + S1);
                int bus2Type = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + S2);
                int bus3Type = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + S3);
                int bus4Type = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + S4);
                boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
                boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
                boolean s3Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
                boolean s4Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
                int ch;
                if (key.contains(VALUE_CHANNEL_TEMP_H)) {
                    ch = Integer.parseInt(key.replace(VALUE_CHANNEL_TEMP_H, ""));
                } else if (key.contains(VALUE_CHANNEL_TEMP)) {
                    ch = Integer.parseInt(key.replace(VALUE_CHANNEL_TEMP, ""));
                } else if (key.contains(VALUE_CHANNEL_H)) {
                    ch = Integer.parseInt(key.replace(VALUE_CHANNEL_H, ""));
                } else {
                    ch = Integer.parseInt(key.replace(VALUE_CHANNEL, ""));
                }
                boolean isInit = false;
                int serials = 1;
                int busType = -1;
                if (serialsId != 0) {
                    serials = serialsId;
                    if (serialsId == S1) {
                        busType = bus1Type;
                    } else if (serialsId == S2) {
                        busType = bus2Type;
                    } else if (serialsId == S3) {
                        busType = bus3Type;
                    } else if (serialsId == S4) {
                        busType = bus4Type;
                    }
                    isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + serialsId + busType + ch);
                } else if (s1Check && !s2Check && !s3Check && !s4Check) {
                    serials = 1;
                    busType = bus1Type;
                    isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + S1 + bus1Type + ch);
                } else if (s2Check && !s1Check && !s3Check && !s4Check) {
                    serials = 2;
                    busType = bus2Type;
                    isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + S2 + bus2Type + ch);
                } else if (s3Check && !s1Check && !s2Check && !s4Check) {
                    serials = 3;
                    busType = bus3Type;
                    isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + S3 + bus3Type + ch);
                } else if (s4Check && !s1Check && !s2Check && !s3Check) {
                    serials = 4;
                    busType = bus4Type;
                    isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + S4 + bus4Type + ch);
                } else if (s1Check && s2Check && s3Check && s4Check) {
                    serials = getLastSetSerials();
                    if (serials == S1) {
                        busType = bus1Type;
                    } else if (serials == S2) {
                        busType = bus2Type;
                    } else if (serials == S3) {
                        busType = bus3Type;
                    } else if (serials == S4) {
                        busType = bus4Type;
                    }
                    isInit = CacheUtil.get().getBoolean(CacheUtil.VALUE_INIT + serials + busType + ch);
                }
                Logger.d(TAG, "serials:" + serials + ",busType:" + busType + ",ch:" + ch + ",isInit:" + isInit);
                if (isInit) {

//                    Logger.d(TAG,"serials:" + serials + ",busType:" + busType);
                    double vScaleVal = 1;
//                    if (ch == TChan.Ch1) {
//                        vScaleVal = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
//                                .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH1_VSCALEID));
//                        vScaleVal *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CH1));
//                        vScaleVal /= ScopeBase.getVerticalPerGridPixels();
//                    } else if (ch == TChan.Ch2) {
//                        vScaleVal = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
//                                .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH2_VSCALEID));
//                        vScaleVal *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CH2));
//                        vScaleVal /= ScopeBase.getVerticalPerGridPixels();
//                    } else if (ch == TChan.Ch3) {
//                        vScaleVal = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
//                                .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH3_VSCALEID));
//                        vScaleVal *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CH3));
//                        vScaleVal /= ScopeBase.getVerticalPerGridPixels();
//                    } else if (ch == TChan.Ch4) {
//                        vScaleVal = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
//                                .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH4_VSCALEID));
//                        vScaleVal *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CH4));
//                        vScaleVal /= ScopeBase.getVerticalPerGridPixels();
//                    }
                    if (TChan.isChan(ch)) {
                        Channel chan = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(ch));
                        if (chan == null) return 0;
                        vScaleVal = chan.getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_SCALE_ID + ch));
                        vScaleVal *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + ch));
                        vScaleVal /= ScopeBase.getVerticalPerGridPixels();
                    }

                    switch (busType) {
                        case RightLayoutSerials.SERIALS_UART:
                            return  (valueInitUart / vScaleVal);
                        case RightLayoutSerials.SERIALS_LIN:
                            return  (valueInitLin / vScaleVal);
                        case RightLayoutSerials.SERIALS_CAN:
                            int signal = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_SIGNAL + serials);
                            switch (signal) {
                                case 0:
                                    return  (valueInitCan0 / vScaleVal);
                                case 1:
                                    return  (valueInitCan1 / vScaleVal);
                                case 2:
                                    return  (valueInitCan2 / vScaleVal);
                                case 3:
                                    return  (valueInitCan3 / vScaleVal);
                                case 4:
                                    return  (valueInitCan4 / vScaleVal);
                                case 5:
                                    return (valueInitCan5 / vScaleVal);
                            }
                        case RightLayoutSerials.SERIALS_SPI:
                            return (valueInitSpi / vScaleVal);
                        case RightLayoutSerials.SERIALS_I2C:
                            return (valueInitI2c / vScaleVal);
                        case RightLayoutSerials.SERIALS_M429:
                            if (key.contains(HIGH)) {
                                return (valueInit429H / vScaleVal);
                            } else {
                                return (valueInit429L / vScaleVal);
                            }
                        case RightLayoutSerials.SERIALS_M1553B:
                            return (valueInit1553b / vScaleVal);
                    }
                }
            }
            return Double.parseDouble(map.get(key));
        }
    }

    /**
     * 从主缓存读取long类型值
     * @param key 缓存键名
     * @return 对应的long值，默认0
     */
    public long getLong(String key) {
        loadMapFromUserSet();
        if (!map.containsKey(key)) {
            String s = getValueFromInit(key);
            s = !StrUtil.isEmpty(s) && StrUtil.isNumber(s) ? s : "0";
            map.put(key, s);
            return Long.parseLong(s);
        } else {
            return Long.parseLong(map.get(key));
        }
    }


    /**
     * 从主缓存读取String类型值
     * @param key 缓存键名
     * @return 对应的String值，默认空字符串
     */
    public String getString(String key) {
        loadMapFromUserSet();
        if (!map.containsKey(key)) {
            String s = getValueFromInit(key);
            s = StrUtil.isEmpty(s) ? "" : s;
            map.put(key, s);
            return s;
        } else {
            return map.get(key);
        }
    }

    /**
     * 从主缓存读取boolean类型值
     * @param key 缓存键名
     * @return 对应的boolean值，默认false
     */
    public boolean getBoolean(String key) {
        loadMapFromUserSet();
        if (!map.containsKey(key)) {
            String s = getValueFromInit(key);
            s = "true".equalsIgnoreCase(s) ? "true" : "false";
            map.put(key, s);
            return Boolean.parseBoolean(s);
        } else {
            return Boolean.parseBoolean(map.get(key));
        }
    }

    /**
     * 从杂项缓存读取double类型值
     * @param key 缓存键名
     * @return 对应的double值，默认0
     */
    public double getOtherDouble(String key) {
        loadOtherMapFromUserSet();
        if (!otherMap.containsKey(key)) {
            String s = getOtherMapValue(key);
            s = !StrUtil.isEmpty(s) && StrUtil.isNumber(s) ? s : "0";
            otherMap.put(key, s);
            return Double.parseDouble(s);
        } else {
            return Double.parseDouble(otherMap.get(key));
        }
    }

    /**
     * 从杂项缓存读取long类型值
     * @param key 缓存键名
     * @return 对应的long值，默认0
     */
    public long getOtherLong(String key) {
        loadOtherMapFromUserSet();
        if (!otherMap.containsKey(key)) {
            String s = getOtherMapValue(key);
            s = !StrUtil.isEmpty(s) && StrUtil.isNumber(s) ? s : "0";
            otherMap.put(key, s);
            return Long.parseLong(s);
        } else {
            return Long.parseLong(otherMap.get(key));
        }
    }

    /**
     * 从杂项缓存读取String类型值
     * @param key 缓存键名
     * @return 对应的String值，默认空字符串
     */
    public String getOtherString(String key) {
        loadOtherMapFromUserSet();
        if (!otherMap.containsKey(key)) {
            String s = getOtherMapValue(key);
            s = StrUtil.isEmpty(s) ? "" : s;
            otherMap.put(key, s);
            return s;
        } else {
            return otherMap.get(key);
        }
    }

    /**
     * 从杂项缓存读取boolean类型值
     * @param key 缓存键名
     * @return 对应的boolean值，默认false
     */
    public boolean getOtherBoolean(String key) {
        loadOtherMapFromUserSet();
        if (!otherMap.containsKey(key)) {
            String s = getOtherMapValue(key);
            s = "true".equalsIgnoreCase(s) ? "true" : "false";
            otherMap.put(key, s);
            return Boolean.parseBoolean(s);
        } else {
            return Boolean.parseBoolean(otherMap.get(key));
        }
    }

    /**
     * 从杂项缓存读取int类型值
     * 支持波特率格式（包含"b/s"）的自动解析
     * @param key 缓存键名
     * @return 对应的int值，默认0
     */
    public int getOtherInt(String key) {
        loadOtherMapFromUserSet();
        if (!otherMap.containsKey(key)) {
            String s = getOtherMapValue(key);
            s = !StrUtil.isEmpty(s) && StrUtil.isNumber(s) ? s : "0";
            otherMap.put(key, s);
            return Integer.parseInt(s);
        } else {
            if (otherMap.get(key).contains("b/s")) {
                return TBookUtil.getIntFromBaudRate(otherMap.get(key));
            } else {
                return Integer.parseInt(otherMap.get(key));
            }
        }
    }

    /**
     * 清空主参数缓存并持久化空缓存到文件
     */
    public void clearCacheMap() {
        map.clear();
        try {
            SaveManage.getInstance().saveUserSet(DefaultSaveName, map, null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空杂项缓存并持久化空缓存到文件
     */
    public void clearOtherMap() {
        otherMap.clear();
        try {
            SaveManage.getInstance().saveUserSet(OtherDefaultSaveName, otherMap, null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 各种设置的初始化 —— 主缓存默认值表（第一部分）
     * 巨型switch语句，为每个缓存key提供默认值
     * 由于Java方法大小限制（64KB），分为getValueFromInit和getValueFromInit1两个方法
     * getValueFromInit处理：左侧菜单、通道开关、Math/Ref/Serial开关、垂直档位、时基、
     * 光标位置、通道选择、Ref/Math参数、通道参数、串口参数、顶部菜单、测量、
     * 采样、显示、自动设置、用户设置、触发、串口触发、触发电平、阈值电平、
     * VALUE_INIT标记、通道位置、零点位置等
     * @param key 缓存键名
     * @return 默认值字符串，空字符串表示无默认值
     */
    private String getValueFromInit(String key) {
        Point mainWaveYT = GlobalVar.get().getMainWave();
        int mainWaveXYHeight = GlobalVar.get().getWaveZoneHeight_Pix(IWorkMode.WorkMode_XY);
        int mainWaveXYWidth = GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_XY);
        switch (key) {
            case MAIN_LEFT_RUNSTOP:
                return String.valueOf(true);
            case MAIN_LEFT_SEQ:
                return String.valueOf(false);
            case MAIN_LEFT_AUTO:
                return String.valueOf(false);
            case MAIN_CHANNEL_OPEN_STATE + TChan.Ch1:
            case MAIN_CHANNEL_OPEN_STATE + TChan.Ch2:
            case MAIN_CHANNEL_OPEN_STATE + TChan.Ch3:
            case MAIN_CHANNEL_OPEN_STATE + TChan.Ch4:
            case MAIN_CHANNEL_OPEN_STATE + TChan.Ch5:
            case MAIN_CHANNEL_OPEN_STATE + TChan.Ch6:
            case MAIN_CHANNEL_OPEN_STATE + TChan.Ch7:
            case MAIN_CHANNEL_OPEN_STATE + TChan.Ch8:
                return String.valueOf(true);
            case MAIN_RIGHT_MATH + TChan.Math1:
            case MAIN_RIGHT_MATH + TChan.Math2:
            case MAIN_RIGHT_MATH + TChan.Math3:
            case MAIN_RIGHT_MATH + TChan.Math4:
            case MAIN_RIGHT_MATH + TChan.Math5:
            case MAIN_RIGHT_MATH + TChan.Math6:
            case MAIN_RIGHT_MATH + TChan.Math7:
            case MAIN_RIGHT_MATH + TChan.Math8:
                return String.valueOf(false);
            case MAIN_RIGHT_REF:
                return String.valueOf(false);
            case MAIN_RIGHT_SERIAL + CacheUtil.S1:
            case MAIN_RIGHT_SERIAL + CacheUtil.S2:
            case MAIN_RIGHT_SERIAL + CacheUtil.S3:
            case MAIN_RIGHT_SERIAL + CacheUtil.S4:
                return String.valueOf(false);
            case MAIN_CHAN_V_SCALE_ID + TChan.Ch1:
            case MAIN_CHAN_V_SCALE_ID + TChan.Ch2:
            case MAIN_CHAN_V_SCALE_ID + TChan.Ch3:
            case MAIN_CHAN_V_SCALE_ID + TChan.Ch4:
            case MAIN_CHAN_V_SCALE_ID + TChan.Ch5:
            case MAIN_CHAN_V_SCALE_ID + TChan.Ch6:
            case MAIN_CHAN_V_SCALE_ID + TChan.Ch7:
            case MAIN_CHAN_V_SCALE_ID + TChan.Ch8:
                return String.valueOf(VerticalAxis.DANG_100mV);
            case MAIN_CHAN_REF_VSCALE_ID + TChan.R1:
            case MAIN_CHAN_REF_VSCALE_ID + TChan.R2:
            case MAIN_CHAN_REF_VSCALE_ID + TChan.R3:
            case MAIN_CHAN_REF_VSCALE_ID + TChan.R4:
            case MAIN_CHAN_REF_VSCALE_ID + TChan.R5:
            case MAIN_CHAN_REF_VSCALE_ID + TChan.R6:
            case MAIN_CHAN_REF_VSCALE_ID + TChan.R7:
            case MAIN_CHAN_REF_VSCALE_ID + TChan.R8:
                return "";
            case MAIN_RIGHT_MATH_DW_VSCALE_ID + TChan.Math1:
            case MAIN_RIGHT_MATH_DW_VSCALE_ID + TChan.Math2:
            case MAIN_RIGHT_MATH_DW_VSCALE_ID + TChan.Math3:
            case MAIN_RIGHT_MATH_DW_VSCALE_ID + TChan.Math4:
            case MAIN_RIGHT_MATH_DW_VSCALE_ID + TChan.Math5:
            case MAIN_RIGHT_MATH_DW_VSCALE_ID + TChan.Math6:
            case MAIN_RIGHT_MATH_DW_VSCALE_ID + TChan.Math7:
            case MAIN_RIGHT_MATH_DW_VSCALE_ID + TChan.Math8:
            case MAIN_RIGHT_MATH_AXB_VSCALE_ID + TChan.Math1:
            case MAIN_RIGHT_MATH_AXB_VSCALE_ID + TChan.Math2:
            case MAIN_RIGHT_MATH_AXB_VSCALE_ID + TChan.Math3:
            case MAIN_RIGHT_MATH_AXB_VSCALE_ID + TChan.Math4:
            case MAIN_RIGHT_MATH_AXB_VSCALE_ID + TChan.Math5:
            case MAIN_RIGHT_MATH_AXB_VSCALE_ID + TChan.Math6:
            case MAIN_RIGHT_MATH_AXB_VSCALE_ID + TChan.Math7:
            case MAIN_RIGHT_MATH_AXB_VSCALE_ID + TChan.Math8:
            case MAIN_RIGHT_MATH_AM_VSCALE_ID + TChan.Math1:
            case MAIN_RIGHT_MATH_AM_VSCALE_ID + TChan.Math2:
            case MAIN_RIGHT_MATH_AM_VSCALE_ID + TChan.Math3:
            case MAIN_RIGHT_MATH_AM_VSCALE_ID + TChan.Math4:
            case MAIN_RIGHT_MATH_AM_VSCALE_ID + TChan.Math5:
            case MAIN_RIGHT_MATH_AM_VSCALE_ID + TChan.Math6:
            case MAIN_RIGHT_MATH_AM_VSCALE_ID + TChan.Math7:
            case MAIN_RIGHT_MATH_AM_VSCALE_ID + TChan.Math8:
                return String.valueOf(VerticalAxisMathDual.DANG_DUAL_1V);
            case MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID + TChan.Math1:
            case MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID + TChan.Math2:
            case MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID + TChan.Math3:
            case MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID + TChan.Math4:
            case MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID + TChan.Math5:
            case MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID + TChan.Math6:
            case MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID + TChan.Math7:
            case MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID + TChan.Math8:
                return String.valueOf(VerticalAxisMathFft.DANG_DBV_50DB);
            case MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID + TChan.Math1:
            case MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID + TChan.Math2:
            case MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID + TChan.Math3:
            case MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID + TChan.Math4:
            case MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID + TChan.Math5:
            case MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID + TChan.Math6:
            case MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID + TChan.Math7:
            case MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID + TChan.Math8:
                return String.valueOf(VerticalAxisMathFft.DANG_RMS_1V);
            case MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE:
                return "2 ms";
            case ZOOM_BOTTOM_TIMEBASE_LARGE_SCALE:
                return "200 μs";
            case MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math1:
            case MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math2:
            case MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math3:
            case MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math4:
            case MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math5:
            case MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math6:
            case MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math7:
            case MAIN_BOTTOM_TIMEBASE_FFT_SCALE + TChan.Math8:
            case MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R1:
            case MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R2:
            case MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R3:
            case MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R4:
            case MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R5:
            case MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R6:
            case MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R7:
            case MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.R8:
                return "";

            case MAIN_RIGHT_MATH_FFT_INFO_DISPLAY:
                return String.valueOf(0);
            case MAIN_RIGHT_MATH_FFT_INFO_DISPLAY_SWITCH:
                return String.valueOf(true);
            case MAIN_BOTTOM_CHANNELLIST_VISIBLE:
                return String.valueOf(false);
            case MAIN_BOTTOM_FINE:
                return String.valueOf(false);
            case MAIN_WAVE_YT_CURSORH_POSITION + TChan.Cursor_row_1:
                return String.valueOf(1000 / 4);
            case MAIN_WAVE_YT_CURSORH_POSITION + TChan.Cursor_row_2:
                return String.valueOf(1000 / 4 * 3);
            case MAIN_WAVE_YT_CURSORV_POSITION + TChan.Cursor_col_1:
                return String.valueOf(mainWaveYT.x / 4);
            case MAIN_WAVE_YT_CURSORV_POSITION + TChan.Cursor_col_2:
                return String.valueOf(mainWaveYT.x / 4 * 3);
            case MAIN_WAVE_XY_CURSORH_POSITION + TChan.Cursor_row_1:
                return String.valueOf(mainWaveXYHeight / 4);
            case MAIN_WAVE_XY_CURSORH_POSITION + TChan.Cursor_row_2:
                return String.valueOf(mainWaveXYHeight / 4 * 3);
            case MAIN_WAVE_XY_CURSORV_POSITION + TChan.Cursor_col_1:
                return String.valueOf(mainWaveXYWidth / 4);
            case MAIN_WAVE_XY_CURSORV_POSITION + TChan.Cursor_col_2:
                return String.valueOf(mainWaveXYWidth / 4 * 3);
            case MAIN_WAVE_YT_CURSOR_LABEL_X:
                return String.valueOf(50);
            case MAIN_WAVE_YT_CURSOR_LABEL_Y:
                return String.valueOf(50);
            case MAIN_WAVE_CURSOR_POSITION_X1:
                return String.valueOf(-6e-3);
            case MAIN_WAVE_CURSOR_POSITION_X2:
                return String.valueOf(6e-3);
            case MAIN_WAVE_CURSOR_POSITION_Y1:
                return String.valueOf(1.046);
            case MAIN_WAVE_CURSOR_POSITION_Y2:
                return String.valueOf(-6.046);

            case MAIN_WAVE_YT_CURSORH_VISIBLE:
                return String.valueOf(false);
            case MAIN_WAVE_YT_CURSORV_VISIBLE:
                return String.valueOf(false);
            case MAIN_WAVE_XY_CURSORH_VISIBLE:
                return String.valueOf(false);
            case MAIN_WAVE_XY_CURSORV_VISIBLE:
                return String.valueOf(false);
            case MAIN_BOTTOM_RIGHTSWITCH_CHANNEL:
                return String.valueOf(true);//true是channel页面，false是other页面
            case MAIN_BOTTOM_SLIP_ALLMEASURE:
                return String.valueOf(false);
            case MAIN_BOTTOM_SLIP_SERIALBUSTXT:
                return String.valueOf(false);
            case MAIN_BOTTOM_SLIP_ZOOM:
                return String.valueOf(false);
            case MAIN_BOTTOM_SLIP_MENU:
                return String.valueOf(false);
            case MAIN_CENTER_CHANNELS_X:
                return String.valueOf(300);
            case MAIN_CENTER_CHANNELS_Y:
                return String.valueOf(200);
            case MAIN_RECOVERY_CHANNEL_SELECT + "-1"://保存普通状态下当前通道值，用以重启后恢复活动通道
            case MAIN_RECOVERY_CHANNEL_SELECT + "0":
            case MAIN_RECOVERY_CHANNEL_SELECT + "1":
            case MAIN_RECOVERY_CHANNEL_SELECT + "2":
            case MAIN_RECOVERY_CHANNEL_SELECT + "3":
            case MAIN_RECOVERY_CHANNEL_SELECT + "4":
            case MAIN_RECOVERY_CHANNEL_SELECT + "5":
            case MAIN_RECOVERY_CHANNEL_SELECT + "6":
            case MAIN_RECOVERY_CHANNEL_SELECT + "7":
            case MAIN_RECOVERY_CHANNEL_SELECT + "8":
            case MAIN_RECOVERY_CHANNEL_SELECT + "9":
            case MAIN_CENTER_CHANNELS_SELECT:
                return String.valueOf(0);
            case MAIN_CENTER_CHANNELS_SELECT_WILL_NULL:
                return String.valueOf(0);
            case MAIN_CENTER_CHANNELS_SELECT_UNXY:
                return String.valueOf(0);
            case MAIN_CENTER_MENU_X:
                return String.valueOf(400);
            case MAIN_CENTER_MENU_Y:
                return String.valueOf(100);
            case MAIN_CENTER_SEGMENTED_VISIBLE:
                return String.valueOf(false);
            case MAIN_CENTER_SEGMENTED_X:
                return String.valueOf(640);
            case MAIN_CENTER_SEGMENTED_Y:
                return String.valueOf(80);
            case RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R1:
            case RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R2:
            case RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R3:
            case RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R4:
            case RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R5:
            case RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R6:
            case RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R7:
            case RIGHT_SLIP_REF_CLOSE_SAVE + TChan.R8:
                return "";
            case RIGHT_SLIP_MAX_CHANNEL_NUMBER_REF:
                return String.valueOf(TChan.R1 - TChan.Ch1);
            case RIGHT_SLIP_REF_CHECK + TChan.R1:
            case RIGHT_SLIP_REF_CHECK + TChan.R2:
            case RIGHT_SLIP_REF_CHECK + TChan.R3:
            case RIGHT_SLIP_REF_CHECK + TChan.R4:
            case RIGHT_SLIP_REF_CHECK + TChan.R5:
            case RIGHT_SLIP_REF_CHECK + TChan.R6:
            case RIGHT_SLIP_REF_CHECK + TChan.R7:
            case RIGHT_SLIP_REF_CHECK + TChan.R8:
            case RIGHT_SLIP_ADD_BY_USER_REF + TChan.R1:
            case RIGHT_SLIP_ADD_BY_USER_REF + TChan.R2:
            case RIGHT_SLIP_ADD_BY_USER_REF + TChan.R3:
            case RIGHT_SLIP_ADD_BY_USER_REF + TChan.R4:
            case RIGHT_SLIP_ADD_BY_USER_REF + TChan.R5:
            case RIGHT_SLIP_ADD_BY_USER_REF + TChan.R6:
            case RIGHT_SLIP_ADD_BY_USER_REF + TChan.R7:
            case RIGHT_SLIP_ADD_BY_USER_REF + TChan.R8:
                return String.valueOf(false);
            case RIGHT_SLIP_REF_DATA_FROM + TChan.R1:
            case RIGHT_SLIP_REF_DATA_FROM + TChan.R2:
            case RIGHT_SLIP_REF_DATA_FROM + TChan.R3:
            case RIGHT_SLIP_REF_DATA_FROM + TChan.R4:
            case RIGHT_SLIP_REF_DATA_FROM + TChan.R5:
            case RIGHT_SLIP_REF_DATA_FROM + TChan.R6:
            case RIGHT_SLIP_REF_DATA_FROM + TChan.R7:
            case RIGHT_SLIP_REF_DATA_FROM + TChan.R8:
                return "";
            case RIGHT_SLIP_REF_CSV_INDEX + TChan.R1:
            case RIGHT_SLIP_REF_CSV_INDEX + TChan.R2:
            case RIGHT_SLIP_REF_CSV_INDEX + TChan.R3:
            case RIGHT_SLIP_REF_CSV_INDEX + TChan.R4:
            case RIGHT_SLIP_REF_CSV_INDEX + TChan.R5:
            case RIGHT_SLIP_REF_CSV_INDEX + TChan.R6:
            case RIGHT_SLIP_REF_CSV_INDEX + TChan.R7:
            case RIGHT_SLIP_REF_CSV_INDEX + TChan.R8:
                return String.valueOf(0);
            case RIGHT_SLIP_MAX_CHANNEL_NUMBER_MATH:
                return String.valueOf(TChan.Math1 - TChan.Ch1);
            case RIGHT_SLIP_MATH_TYPE + TChan.Math1:
            case RIGHT_SLIP_MATH_TYPE + TChan.Math2:
            case RIGHT_SLIP_MATH_TYPE + TChan.Math3:
            case RIGHT_SLIP_MATH_TYPE + TChan.Math4:
            case RIGHT_SLIP_MATH_TYPE + TChan.Math5:
            case RIGHT_SLIP_MATH_TYPE + TChan.Math6:
            case RIGHT_SLIP_MATH_TYPE + TChan.Math7:
            case RIGHT_SLIP_MATH_TYPE + TChan.Math8:
                return String.valueOf(MATHTYPE_DW);
            case RIGHT_SLIP_REF_TYPE + TChan.R1:
            case RIGHT_SLIP_REF_TYPE + TChan.R2:
            case RIGHT_SLIP_REF_TYPE + TChan.R3:
            case RIGHT_SLIP_REF_TYPE + TChan.R4:
            case RIGHT_SLIP_REF_TYPE + TChan.R5:
            case RIGHT_SLIP_REF_TYPE + TChan.R6:
            case RIGHT_SLIP_REF_TYPE + TChan.R7:
            case RIGHT_SLIP_REF_TYPE + TChan.R8:
                return String.valueOf(REFTYPE_WAV);
            case RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math1:
            case RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math2:
            case RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math3:
            case RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math4:
            case RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math5:
            case RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math6:
            case RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math7:
            case RIGHT_SLIP_ADD_BY_USER_MATH + TChan.Math8:
                return String.valueOf(false);
            case RIGHT_SLIP_MATH_DW_SOURCE1 + TChan.Math1:
            case RIGHT_SLIP_MATH_DW_SOURCE1 + TChan.Math2:
            case RIGHT_SLIP_MATH_DW_SOURCE1 + TChan.Math3:
            case RIGHT_SLIP_MATH_DW_SOURCE1 + TChan.Math4:
            case RIGHT_SLIP_MATH_DW_SOURCE1 + TChan.Math5:
            case RIGHT_SLIP_MATH_DW_SOURCE1 + TChan.Math6:
            case RIGHT_SLIP_MATH_DW_SOURCE1 + TChan.Math7:
            case RIGHT_SLIP_MATH_DW_SOURCE1 + TChan.Math8:
                return String.valueOf(0);
            case RIGHT_SLIP_MATH_DW_SOURCE2 + TChan.Math1:
            case RIGHT_SLIP_MATH_DW_SOURCE2 + TChan.Math2:
            case RIGHT_SLIP_MATH_DW_SOURCE2 + TChan.Math3:
            case RIGHT_SLIP_MATH_DW_SOURCE2 + TChan.Math4:
            case RIGHT_SLIP_MATH_DW_SOURCE2 + TChan.Math5:
            case RIGHT_SLIP_MATH_DW_SOURCE2 + TChan.Math6:
            case RIGHT_SLIP_MATH_DW_SOURCE2 + TChan.Math7:
            case RIGHT_SLIP_MATH_DW_SOURCE2 + TChan.Math8:
                return String.valueOf(1);
            case RIGHT_SLIP_MATH_DW_SYMBOL + TChan.Math1:
            case RIGHT_SLIP_MATH_DW_SYMBOL + TChan.Math2:
            case RIGHT_SLIP_MATH_DW_SYMBOL + TChan.Math3:
            case RIGHT_SLIP_MATH_DW_SYMBOL + TChan.Math4:
            case RIGHT_SLIP_MATH_DW_SYMBOL + TChan.Math5:
            case RIGHT_SLIP_MATH_DW_SYMBOL + TChan.Math6:
            case RIGHT_SLIP_MATH_DW_SYMBOL + TChan.Math7:
            case RIGHT_SLIP_MATH_DW_SYMBOL + TChan.Math8:
                return String.valueOf(0);
            case RIGHT_SLIP_MATH_FFT_TYPE_ID + TChan.Math1:
            case RIGHT_SLIP_MATH_FFT_TYPE_ID + TChan.Math2:
            case RIGHT_SLIP_MATH_FFT_TYPE_ID + TChan.Math3:
            case RIGHT_SLIP_MATH_FFT_TYPE_ID + TChan.Math4:
            case RIGHT_SLIP_MATH_FFT_TYPE_ID + TChan.Math5:
            case RIGHT_SLIP_MATH_FFT_TYPE_ID + TChan.Math6:
            case RIGHT_SLIP_MATH_FFT_TYPE_ID + TChan.Math7:
            case RIGHT_SLIP_MATH_FFT_TYPE_ID + TChan.Math8:
                return String.valueOf(0);
            case RIGHT_SLIP_MATH_FFT_SOURCE + TChan.Math1:
            case RIGHT_SLIP_MATH_FFT_SOURCE + TChan.Math2:
            case RIGHT_SLIP_MATH_FFT_SOURCE + TChan.Math3:
            case RIGHT_SLIP_MATH_FFT_SOURCE + TChan.Math4:
            case RIGHT_SLIP_MATH_FFT_SOURCE + TChan.Math5:
            case RIGHT_SLIP_MATH_FFT_SOURCE + TChan.Math6:
            case RIGHT_SLIP_MATH_FFT_SOURCE + TChan.Math7:
            case RIGHT_SLIP_MATH_FFT_SOURCE + TChan.Math8:
                return String.valueOf(0);
            case RIGHT_SLIP_MATH_FFT_WINDOW + TChan.Math1:
            case RIGHT_SLIP_MATH_FFT_WINDOW + TChan.Math2:
            case RIGHT_SLIP_MATH_FFT_WINDOW + TChan.Math3:
            case RIGHT_SLIP_MATH_FFT_WINDOW + TChan.Math4:
            case RIGHT_SLIP_MATH_FFT_WINDOW + TChan.Math5:
            case RIGHT_SLIP_MATH_FFT_WINDOW + TChan.Math6:
            case RIGHT_SLIP_MATH_FFT_WINDOW + TChan.Math7:
            case RIGHT_SLIP_MATH_FFT_WINDOW + TChan.Math8:

            case RIGHT_SLIP_MATH_FFT_PERSIST + TChan.Math1:
            case RIGHT_SLIP_MATH_FFT_PERSIST + TChan.Math2:
            case RIGHT_SLIP_MATH_FFT_PERSIST + TChan.Math3:
            case RIGHT_SLIP_MATH_FFT_PERSIST + TChan.Math4:
            case RIGHT_SLIP_MATH_FFT_PERSIST + TChan.Math5:
            case RIGHT_SLIP_MATH_FFT_PERSIST + TChan.Math6:
            case RIGHT_SLIP_MATH_FFT_PERSIST + TChan.Math7:
            case RIGHT_SLIP_MATH_FFT_PERSIST + TChan.Math8:
                return String.valueOf(0);
            case RIGHT_SLIP_MATH_FFT_PERSIST_VALUE + TChan.Math1:
            case RIGHT_SLIP_MATH_FFT_PERSIST_VALUE + TChan.Math2:
            case RIGHT_SLIP_MATH_FFT_PERSIST_VALUE + TChan.Math3:
            case RIGHT_SLIP_MATH_FFT_PERSIST_VALUE + TChan.Math4:
            case RIGHT_SLIP_MATH_FFT_PERSIST_VALUE + TChan.Math5:
            case RIGHT_SLIP_MATH_FFT_PERSIST_VALUE + TChan.Math6:
            case RIGHT_SLIP_MATH_FFT_PERSIST_VALUE + TChan.Math7:
            case RIGHT_SLIP_MATH_FFT_PERSIST_VALUE + TChan.Math8:
                return "200ms";
            case RIGHT_SLIP_MATH_AXB_UNIT + TChan.Math1:
            case RIGHT_SLIP_MATH_AXB_UNIT + TChan.Math2:
            case RIGHT_SLIP_MATH_AXB_UNIT + TChan.Math3:
            case RIGHT_SLIP_MATH_AXB_UNIT + TChan.Math4:
            case RIGHT_SLIP_MATH_AXB_UNIT + TChan.Math5:
            case RIGHT_SLIP_MATH_AXB_UNIT + TChan.Math6:
            case RIGHT_SLIP_MATH_AXB_UNIT + TChan.Math7:
            case RIGHT_SLIP_MATH_AXB_UNIT + TChan.Math8:
                return "V";
            case RIGHT_SLIP_MATH_AXB_SOURCE + TChan.Math1:
            case RIGHT_SLIP_MATH_AXB_SOURCE + TChan.Math2:
            case RIGHT_SLIP_MATH_AXB_SOURCE + TChan.Math3:
            case RIGHT_SLIP_MATH_AXB_SOURCE + TChan.Math4:
            case RIGHT_SLIP_MATH_AXB_SOURCE + TChan.Math5:
            case RIGHT_SLIP_MATH_AXB_SOURCE + TChan.Math6:
            case RIGHT_SLIP_MATH_AXB_SOURCE + TChan.Math7:
            case RIGHT_SLIP_MATH_AXB_SOURCE + TChan.Math8:
                return String.valueOf(0);
            case RIGHT_SLIP_MATH_AXB_A + TChan.Math1:
            case RIGHT_SLIP_MATH_AXB_A + TChan.Math2:
            case RIGHT_SLIP_MATH_AXB_A + TChan.Math3:
            case RIGHT_SLIP_MATH_AXB_A + TChan.Math4:
            case RIGHT_SLIP_MATH_AXB_A + TChan.Math5:
            case RIGHT_SLIP_MATH_AXB_A + TChan.Math6:
            case RIGHT_SLIP_MATH_AXB_A + TChan.Math7:
            case RIGHT_SLIP_MATH_AXB_A + TChan.Math8:
                return String.valueOf(1);
            case RIGHT_SLIP_MATH_AXB_B + TChan.Math1:
            case RIGHT_SLIP_MATH_AXB_B + TChan.Math2:
            case RIGHT_SLIP_MATH_AXB_B + TChan.Math3:
            case RIGHT_SLIP_MATH_AXB_B + TChan.Math4:
            case RIGHT_SLIP_MATH_AXB_B + TChan.Math5:
            case RIGHT_SLIP_MATH_AXB_B + TChan.Math6:
            case RIGHT_SLIP_MATH_AXB_B + TChan.Math7:
            case RIGHT_SLIP_MATH_AXB_B + TChan.Math8:
                return String.valueOf(0);
            case RIGHT_SLIP_MATH_AM_UNIT + TChan.Math1:
            case RIGHT_SLIP_MATH_AM_UNIT + TChan.Math2:
            case RIGHT_SLIP_MATH_AM_UNIT + TChan.Math3:
            case RIGHT_SLIP_MATH_AM_UNIT + TChan.Math4:
            case RIGHT_SLIP_MATH_AM_UNIT + TChan.Math5:
            case RIGHT_SLIP_MATH_AM_UNIT + TChan.Math6:
            case RIGHT_SLIP_MATH_AM_UNIT + TChan.Math7:
            case RIGHT_SLIP_MATH_AM_UNIT + TChan.Math8:
                return "";
            case RIGHT_SLIP_MATH_AM_FORMULA + TChan.Math1:
            case RIGHT_SLIP_MATH_AM_FORMULA + TChan.Math2:
            case RIGHT_SLIP_MATH_AM_FORMULA + TChan.Math3:
            case RIGHT_SLIP_MATH_AM_FORMULA + TChan.Math4:
            case RIGHT_SLIP_MATH_AM_FORMULA + TChan.Math5:
            case RIGHT_SLIP_MATH_AM_FORMULA + TChan.Math6:
            case RIGHT_SLIP_MATH_AM_FORMULA + TChan.Math7:
            case RIGHT_SLIP_MATH_AM_FORMULA + TChan.Math8:
                return "0";
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE + TChan.Math1:
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE + TChan.Math2:
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE + TChan.Math3:
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE + TChan.Math4:
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE + TChan.Math5:
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE + TChan.Math6:
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE + TChan.Math7:
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE + TChan.Math8:
                return String.valueOf(false);
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET + TChan.Math1:
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET + TChan.Math2:
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET + TChan.Math3:
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET + TChan.Math4:
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET + TChan.Math5:
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET + TChan.Math6:
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET + TChan.Math7:
            case RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET + TChan.Math8:
                return String.valueOf(false);
            case RIGHT_SLIP_MATH_AM_VAR1_NUMBER + TChan.Math1:
            case RIGHT_SLIP_MATH_AM_VAR1_NUMBER + TChan.Math2:
            case RIGHT_SLIP_MATH_AM_VAR1_NUMBER + TChan.Math3:
            case RIGHT_SLIP_MATH_AM_VAR1_NUMBER + TChan.Math4:
            case RIGHT_SLIP_MATH_AM_VAR1_NUMBER + TChan.Math5:
            case RIGHT_SLIP_MATH_AM_VAR1_NUMBER + TChan.Math6:
            case RIGHT_SLIP_MATH_AM_VAR1_NUMBER + TChan.Math7:
            case RIGHT_SLIP_MATH_AM_VAR1_NUMBER + TChan.Math8:
                return "0.0000";
            case RIGHT_SLIP_MATH_AM_VAR1_POWER + +TChan.Math1:
            case RIGHT_SLIP_MATH_AM_VAR1_POWER + TChan.Math2:
            case RIGHT_SLIP_MATH_AM_VAR1_POWER + TChan.Math3:
            case RIGHT_SLIP_MATH_AM_VAR1_POWER + TChan.Math4:
            case RIGHT_SLIP_MATH_AM_VAR1_POWER + TChan.Math5:
            case RIGHT_SLIP_MATH_AM_VAR1_POWER + TChan.Math6:
            case RIGHT_SLIP_MATH_AM_VAR1_POWER + TChan.Math7:
            case RIGHT_SLIP_MATH_AM_VAR1_POWER + TChan.Math8:
                return "1";
            case RIGHT_SLIP_MATH_AM_VAR2_NUMBER + TChan.Math1:
            case RIGHT_SLIP_MATH_AM_VAR2_NUMBER + TChan.Math2:
            case RIGHT_SLIP_MATH_AM_VAR2_NUMBER + TChan.Math3:
            case RIGHT_SLIP_MATH_AM_VAR2_NUMBER + TChan.Math4:
            case RIGHT_SLIP_MATH_AM_VAR2_NUMBER + TChan.Math5:
            case RIGHT_SLIP_MATH_AM_VAR2_NUMBER + TChan.Math6:
            case RIGHT_SLIP_MATH_AM_VAR2_NUMBER + TChan.Math7:
            case RIGHT_SLIP_MATH_AM_VAR2_NUMBER + TChan.Math8:
                return "0.0000";
            case RIGHT_SLIP_MATH_AM_VAR2_POWER + TChan.Math1:
            case RIGHT_SLIP_MATH_AM_VAR2_POWER + TChan.Math2:
            case RIGHT_SLIP_MATH_AM_VAR2_POWER + TChan.Math3:
            case RIGHT_SLIP_MATH_AM_VAR2_POWER + TChan.Math4:
            case RIGHT_SLIP_MATH_AM_VAR2_POWER + TChan.Math5:
            case RIGHT_SLIP_MATH_AM_VAR2_POWER + TChan.Math6:
            case RIGHT_SLIP_MATH_AM_VAR2_POWER + TChan.Math7:
            case RIGHT_SLIP_MATH_AM_VAR2_POWER + TChan.Math8:
                return "1";
            case RIGHT_SLIP_MATH_VERTICALBASE + TChan.Math1:
            case RIGHT_SLIP_MATH_VERTICALBASE + TChan.Math2:
            case RIGHT_SLIP_MATH_VERTICALBASE + TChan.Math3:
            case RIGHT_SLIP_MATH_VERTICALBASE + TChan.Math4:
            case RIGHT_SLIP_MATH_VERTICALBASE + TChan.Math5:
            case RIGHT_SLIP_MATH_VERTICALBASE + TChan.Math6:
            case RIGHT_SLIP_MATH_VERTICALBASE + TChan.Math7:
            case RIGHT_SLIP_MATH_VERTICALBASE + TChan.Math8:
                return String.valueOf(1);
            case RIGHT_SLIP_CH_FINE_ENABLE + TChan.Ch1:
            case RIGHT_SLIP_CH_FINE_ENABLE + TChan.Ch2:
            case RIGHT_SLIP_CH_FINE_ENABLE + TChan.Ch3:
            case RIGHT_SLIP_CH_FINE_ENABLE + TChan.Ch4:
            case RIGHT_SLIP_CH_FINE_ENABLE + TChan.Ch5:
            case RIGHT_SLIP_CH_FINE_ENABLE + TChan.Ch6:
            case RIGHT_SLIP_CH_FINE_ENABLE + TChan.Ch7:
            case RIGHT_SLIP_CH_FINE_ENABLE + TChan.Ch8:
                return String.valueOf(false);
            case RIGHT_SLIP_CH_FINE_EXTENT + TChan.Ch1:
            case RIGHT_SLIP_CH_FINE_EXTENT + TChan.Ch2:
            case RIGHT_SLIP_CH_FINE_EXTENT + TChan.Ch3:
            case RIGHT_SLIP_CH_FINE_EXTENT + TChan.Ch4:
            case RIGHT_SLIP_CH_FINE_EXTENT + TChan.Ch5:
            case RIGHT_SLIP_CH_FINE_EXTENT + TChan.Ch6:
            case RIGHT_SLIP_CH_FINE_EXTENT + TChan.Ch7:
            case RIGHT_SLIP_CH_FINE_EXTENT + TChan.Ch8:
                return "1";
            case RIGHT_SLIP_CH_INVERT + TChan.Ch1:
            case RIGHT_SLIP_CH_INVERT + TChan.Ch2:
            case RIGHT_SLIP_CH_INVERT + TChan.Ch3:
            case RIGHT_SLIP_CH_INVERT + TChan.Ch4:
            case RIGHT_SLIP_CH_INVERT + TChan.Ch5:
            case RIGHT_SLIP_CH_INVERT + TChan.Ch6:
            case RIGHT_SLIP_CH_INVERT + TChan.Ch7:
            case RIGHT_SLIP_CH_INVERT + TChan.Ch8:
                return String.valueOf(false);
            case RIGHT_SLIP_CH_COUPLE + TChan.Ch1:
            case RIGHT_SLIP_CH_COUPLE + TChan.Ch2:
            case RIGHT_SLIP_CH_COUPLE + TChan.Ch3:
            case RIGHT_SLIP_CH_COUPLE + TChan.Ch4:
            case RIGHT_SLIP_CH_COUPLE + TChan.Ch5:
            case RIGHT_SLIP_CH_COUPLE + TChan.Ch6:
            case RIGHT_SLIP_CH_COUPLE + TChan.Ch7:
            case RIGHT_SLIP_CH_COUPLE + TChan.Ch8:
                return String.valueOf(0);
            case RIGHT_SLIP_CH_PROBE_TYPE + TChan.Ch1:
            case RIGHT_SLIP_CH_PROBE_TYPE + TChan.Ch2:
            case RIGHT_SLIP_CH_PROBE_TYPE + TChan.Ch3:
            case RIGHT_SLIP_CH_PROBE_TYPE + TChan.Ch4:
            case RIGHT_SLIP_CH_PROBE_TYPE + TChan.Ch5:
            case RIGHT_SLIP_CH_PROBE_TYPE + TChan.Ch6:
            case RIGHT_SLIP_CH_PROBE_TYPE + TChan.Ch7:
            case RIGHT_SLIP_CH_PROBE_TYPE + TChan.Ch8:
                return String.valueOf(0);
            case RIGHT_SLIP_CH_PROBE_MULTIPLE + TChan.Ch1:
            case RIGHT_SLIP_CH_PROBE_MULTIPLE + TChan.Ch2:
            case RIGHT_SLIP_CH_PROBE_MULTIPLE + TChan.Ch3:
            case RIGHT_SLIP_CH_PROBE_MULTIPLE + TChan.Ch4:
            case RIGHT_SLIP_CH_PROBE_MULTIPLE + TChan.Ch5:
            case RIGHT_SLIP_CH_PROBE_MULTIPLE + TChan.Ch6:
            case RIGHT_SLIP_CH_PROBE_MULTIPLE + TChan.Ch7:
            case RIGHT_SLIP_CH_PROBE_MULTIPLE + TChan.Ch8:
                return "10X";
            case RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + TChan.Ch1:
            case RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + TChan.Ch2:
            case RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + TChan.Ch3:
            case RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + TChan.Ch4:
            case RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + TChan.Ch5:
            case RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + TChan.Ch6:
            case RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + TChan.Ch7:
            case RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE + TChan.Ch8:
                return "";
            case RIGHT_SLIP_CH_BANDWIDTH + TChan.Ch1:
            case RIGHT_SLIP_CH_BANDWIDTH + TChan.Ch2:
            case RIGHT_SLIP_CH_BANDWIDTH + TChan.Ch3:
            case RIGHT_SLIP_CH_BANDWIDTH + TChan.Ch4:
            case RIGHT_SLIP_CH_BANDWIDTH + TChan.Ch5:
            case RIGHT_SLIP_CH_BANDWIDTH + TChan.Ch6:
            case RIGHT_SLIP_CH_BANDWIDTH + TChan.Ch7:
            case RIGHT_SLIP_CH_BANDWIDTH + TChan.Ch8:
                return String.valueOf(0);
            case RIGHT_SLIP_CH_VERTICALBASE + TChan.Ch1:
            case RIGHT_SLIP_CH_VERTICALBASE + TChan.Ch2:
            case RIGHT_SLIP_CH_VERTICALBASE + TChan.Ch3:
            case RIGHT_SLIP_CH_VERTICALBASE + TChan.Ch4:
            case RIGHT_SLIP_CH_VERTICALBASE + TChan.Ch5:
            case RIGHT_SLIP_CH_VERTICALBASE + TChan.Ch6:
            case RIGHT_SLIP_CH_VERTICALBASE + TChan.Ch7:
            case RIGHT_SLIP_CH_VERTICALBASE + TChan.Ch8:
                return String.valueOf(1);
            case RIGHT_SLIP_CH_IMPED + TChan.Ch1:
            case RIGHT_SLIP_CH_IMPED + TChan.Ch2:
            case RIGHT_SLIP_CH_IMPED + TChan.Ch3:
            case RIGHT_SLIP_CH_IMPED + TChan.Ch4:
            case RIGHT_SLIP_CH_IMPED + TChan.Ch5:
            case RIGHT_SLIP_CH_IMPED + TChan.Ch6:
            case RIGHT_SLIP_CH_IMPED + TChan.Ch7:
            case RIGHT_SLIP_CH_IMPED + TChan.Ch8:
                return String.valueOf(0);
            case RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + TChan.Ch1:
            case RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + TChan.Ch2:
            case RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + TChan.Ch3:
            case RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + TChan.Ch4:
            case RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + TChan.Ch5:
            case RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + TChan.Ch6:
            case RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + TChan.Ch7:
            case RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT + TChan.Ch8:
            case RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + TChan.Ch1:
            case RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + TChan.Ch2:
            case RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + TChan.Ch3:
            case RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + TChan.Ch4:
            case RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + TChan.Ch5:
            case RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + TChan.Ch6:
            case RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + TChan.Ch7:
            case RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT + TChan.Ch8:
                return "30Hz";
            case RIGHT_SLIP_CH_LABEL + TChan.Ch1:
            case RIGHT_SLIP_CH_LABEL + TChan.Ch2:
            case RIGHT_SLIP_CH_LABEL + TChan.Ch3:
            case RIGHT_SLIP_CH_LABEL + TChan.Ch4:
            case RIGHT_SLIP_CH_LABEL + TChan.Ch5:
            case RIGHT_SLIP_CH_LABEL + TChan.Ch6:
            case RIGHT_SLIP_CH_LABEL + TChan.Ch7:
            case RIGHT_SLIP_CH_LABEL + TChan.Ch8:
            case RIGHT_SLIP_CH_LABEL + TChan.Math1:
            case RIGHT_SLIP_CH_LABEL + TChan.Math2:
            case RIGHT_SLIP_CH_LABEL + TChan.Math3:
            case RIGHT_SLIP_CH_LABEL + TChan.Math4:
            case RIGHT_SLIP_CH_LABEL + TChan.Math5:
            case RIGHT_SLIP_CH_LABEL + TChan.Math6:
            case RIGHT_SLIP_CH_LABEL + TChan.Math7:
            case RIGHT_SLIP_CH_LABEL + TChan.Math8:
            case RIGHT_SLIP_CH_LABEL + TChan.R1:
            case RIGHT_SLIP_CH_LABEL + TChan.R2:
            case RIGHT_SLIP_CH_LABEL + TChan.R3:
            case RIGHT_SLIP_CH_LABEL + TChan.R4:
            case RIGHT_SLIP_CH_LABEL + TChan.R5:
            case RIGHT_SLIP_CH_LABEL + TChan.R6:
            case RIGHT_SLIP_CH_LABEL + TChan.R7:
            case RIGHT_SLIP_CH_LABEL + TChan.R8:
                return "";
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Ch1:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Ch2:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Ch3:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Ch4:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Ch5:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Ch6:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Ch7:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Ch8:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Math1:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Math2:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Math3:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Math4:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Math5:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Math6:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Math7:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.Math8:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.R1:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.R2:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.R3:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.R4:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.R5:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.R6:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.R7:
            case RIGHT_SLIP_CH_LABEL_USERDEFINE + TChan.R8:
                return "";
            case RIGHT_SLIP_CH_DELAY + TChan.Ch1:
            case RIGHT_SLIP_CH_DELAY + TChan.Ch2:
            case RIGHT_SLIP_CH_DELAY + TChan.Ch3:
            case RIGHT_SLIP_CH_DELAY + TChan.Ch4:
            case RIGHT_SLIP_CH_DELAY + TChan.Ch5:
            case RIGHT_SLIP_CH_DELAY + TChan.Ch6:
            case RIGHT_SLIP_CH_DELAY + TChan.Ch7:
            case RIGHT_SLIP_CH_DELAY + TChan.Ch8:
            case RIGHT_SLIP_CH_DELAY + TChan.R1:
            case RIGHT_SLIP_CH_DELAY + TChan.R2:
            case RIGHT_SLIP_CH_DELAY + TChan.R3:
            case RIGHT_SLIP_CH_DELAY + TChan.R4:
            case RIGHT_SLIP_CH_DELAY + TChan.R5:
            case RIGHT_SLIP_CH_DELAY + TChan.R6:
            case RIGHT_SLIP_CH_DELAY + TChan.R7:
            case RIGHT_SLIP_CH_DELAY + TChan.R8:
                return "0 ns";
            case RIGHT_SLIP_CH_OFFSET + TChan.Ch1:
            case RIGHT_SLIP_CH_OFFSET + TChan.Ch2:
            case RIGHT_SLIP_CH_OFFSET + TChan.Ch3:
            case RIGHT_SLIP_CH_OFFSET + TChan.Ch4:
            case RIGHT_SLIP_CH_OFFSET + TChan.Ch5:
            case RIGHT_SLIP_CH_OFFSET + TChan.Ch6:
            case RIGHT_SLIP_CH_OFFSET + TChan.Ch7:
            case RIGHT_SLIP_CH_OFFSET + TChan.Ch8:
                return "0 V";
//            case RIGHT_SLIP_SAMPLE:
//                return String.valueOf(0);
//            case RIGHT_SLIP_SAMPLE_DETAIL_INDEX:
//                return String.valueOf(0);
            case RIGHT_SLIP_MAX_CHANNEL_NUMBER_SERIALS:
                return String.valueOf(TChan.S1 - TChan.Ch1);
            case RIGHT_SLIP_ADD_BY_USER_SERIALS + S1:
            case RIGHT_SLIP_ADD_BY_USER_SERIALS + S2:
            case RIGHT_SLIP_ADD_BY_USER_SERIALS + S3:
            case RIGHT_SLIP_ADD_BY_USER_SERIALS + S4:
                return String.valueOf(false);
            case RIGHT_SLIP_SERIALS + S1:
            case RIGHT_SLIP_SERIALS + S2:
            case RIGHT_SLIP_SERIALS + S3:
            case RIGHT_SLIP_SERIALS + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_UART_RX + S1:
            case RIGHT_SLIP_SERIALS_UART_RX + S2:
            case RIGHT_SLIP_SERIALS_UART_RX + S3:
            case RIGHT_SLIP_SERIALS_UART_RX + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_UART_IDLE + S1:
            case RIGHT_SLIP_SERIALS_UART_IDLE + S2:
            case RIGHT_SLIP_SERIALS_UART_IDLE + S3:
            case RIGHT_SLIP_SERIALS_UART_IDLE + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_UART_CHECK + S1:
            case RIGHT_SLIP_SERIALS_UART_CHECK + S2:
            case RIGHT_SLIP_SERIALS_UART_CHECK + S3:
            case RIGHT_SLIP_SERIALS_UART_CHECK + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_UART_BITS + S1:
            case RIGHT_SLIP_SERIALS_UART_BITS + S2:
            case RIGHT_SLIP_SERIALS_UART_BITS + S3:
            case RIGHT_SLIP_SERIALS_UART_BITS + S4:
                return String.valueOf(3);
            case RIGHT_SLIP_SERIALS_UART_DISPLAY + S1:
            case RIGHT_SLIP_SERIALS_UART_DISPLAY + S2:
            case RIGHT_SLIP_SERIALS_UART_DISPLAY + S3:
            case RIGHT_SLIP_SERIALS_UART_DISPLAY + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_UART_BAUDRATE + S1:
            case RIGHT_SLIP_SERIALS_UART_BAUDRATE + S2:
            case RIGHT_SLIP_SERIALS_UART_BAUDRATE + S3:
            case RIGHT_SLIP_SERIALS_UART_BAUDRATE + S4:
                return String.valueOf(3);
            case RIGHT_SLIP_SERIALS_UART_USERDEFINE + S1:
            case RIGHT_SLIP_SERIALS_UART_USERDEFINE + S2:
            case RIGHT_SLIP_SERIALS_UART_USERDEFINE + S3:
            case RIGHT_SLIP_SERIALS_UART_USERDEFINE + S4:
                return "";
            case RIGHT_SLIP_SERIALS_LIN_SOURCE + S1:
            case RIGHT_SLIP_SERIALS_LIN_SOURCE + S2:
            case RIGHT_SLIP_SERIALS_LIN_SOURCE + S3:
            case RIGHT_SLIP_SERIALS_LIN_SOURCE + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_LIN_IDLE + S1:
            case RIGHT_SLIP_SERIALS_LIN_IDLE + S2:
            case RIGHT_SLIP_SERIALS_LIN_IDLE + S3:
            case RIGHT_SLIP_SERIALS_LIN_IDLE + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_LIN_BAUDRATE + S1:
            case RIGHT_SLIP_SERIALS_LIN_BAUDRATE + S2:
            case RIGHT_SLIP_SERIALS_LIN_BAUDRATE + S3:
            case RIGHT_SLIP_SERIALS_LIN_BAUDRATE + S4:
                return String.valueOf(3);
            case RIGHT_SLIP_SERIALS_LIN_USERDEFINE + S1:
            case RIGHT_SLIP_SERIALS_LIN_USERDEFINE + S2:
            case RIGHT_SLIP_SERIALS_LIN_USERDEFINE + S3:
            case RIGHT_SLIP_SERIALS_LIN_USERDEFINE + S4:
                return "";
//                return "19.2kb/s";
            case RIGHT_SLIP_SERIALS_CAN_SOURCE + S1:
            case RIGHT_SLIP_SERIALS_CAN_SOURCE + S2:
            case RIGHT_SLIP_SERIALS_CAN_SOURCE + S3:
            case RIGHT_SLIP_SERIALS_CAN_SOURCE + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_CAN_SIGNAL + S1:
            case RIGHT_SLIP_SERIALS_CAN_SIGNAL + S2:
            case RIGHT_SLIP_SERIALS_CAN_SIGNAL + S3:
            case RIGHT_SLIP_SERIALS_CAN_SIGNAL + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_CAN_BAUDRATE + S1:
            case RIGHT_SLIP_SERIALS_CAN_BAUDRATE + S2:
            case RIGHT_SLIP_SERIALS_CAN_BAUDRATE + S3:
            case RIGHT_SLIP_SERIALS_CAN_BAUDRATE + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_CAN_USERDEFINE + S1:
            case RIGHT_SLIP_SERIALS_CAN_USERDEFINE + S2:
            case RIGHT_SLIP_SERIALS_CAN_USERDEFINE + S3:
            case RIGHT_SLIP_SERIALS_CAN_USERDEFINE + S4:
                return "";
            case RIGHT_SLIP_SERIALS_CAN_FDBAUDRATE + S1:
            case RIGHT_SLIP_SERIALS_CAN_FDBAUDRATE + S2:
            case RIGHT_SLIP_SERIALS_CAN_FDBAUDRATE + S3:
            case RIGHT_SLIP_SERIALS_CAN_FDBAUDRATE + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_CAN_FDUSERDEFINE + S1:
            case RIGHT_SLIP_SERIALS_CAN_FDUSERDEFINE + S2:
            case RIGHT_SLIP_SERIALS_CAN_FDUSERDEFINE + S3:
            case RIGHT_SLIP_SERIALS_CAN_FDUSERDEFINE + S4:
                return "";
            case RIGHT_SLIP_SERIALS_CAN_PERCENT + S1://范围0 - 99...
            case RIGHT_SLIP_SERIALS_CAN_PERCENT + S2://范围0 - 99...
            case RIGHT_SLIP_SERIALS_CAN_PERCENT + S3://范围0 - 99...
            case RIGHT_SLIP_SERIALS_CAN_PERCENT + S4://范围0 - 99...
                return String.valueOf(65);
            case RIGHT_SLIP_SERIALS_CAN_FDPERCENT + S1://范围0 - 99...
            case RIGHT_SLIP_SERIALS_CAN_FDPERCENT + S2://范围0 - 99...
            case RIGHT_SLIP_SERIALS_CAN_FDPERCENT + S3://范围0 - 99...
            case RIGHT_SLIP_SERIALS_CAN_FDPERCENT + S4://范围0 - 99...
                return String.valueOf(65);
//                return "125kb/s";
            case RIGHT_SLIP_SERIALS_CAN_ISO + S1:
            case RIGHT_SLIP_SERIALS_CAN_ISO + S2:
            case RIGHT_SLIP_SERIALS_CAN_ISO + S3:
            case RIGHT_SLIP_SERIALS_CAN_ISO + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_SPI_CLK + S1:
            case RIGHT_SLIP_SERIALS_SPI_CLK + S2:
            case RIGHT_SLIP_SERIALS_SPI_CLK + S3:
            case RIGHT_SLIP_SERIALS_SPI_CLK + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_SPI_DATA + S1:
            case RIGHT_SLIP_SERIALS_SPI_DATA + S2:
            case RIGHT_SLIP_SERIALS_SPI_DATA + S3:
            case RIGHT_SLIP_SERIALS_SPI_DATA + S4:
                return String.valueOf(1);
            case RIGHT_SLIP_SERIALS_SPI_CS + S1:
            case RIGHT_SLIP_SERIALS_SPI_CS + S2:
            case RIGHT_SLIP_SERIALS_SPI_CS + S3:
            case RIGHT_SLIP_SERIALS_SPI_CS + S4:
                return String.valueOf(2);
            case RIGHT_SLIP_SERIALS_SPI_BIT + S1:
            case RIGHT_SLIP_SERIALS_SPI_BIT + S2:
            case RIGHT_SLIP_SERIALS_SPI_BIT + S3:
            case RIGHT_SLIP_SERIALS_SPI_BIT + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_SPI_CLKCHECK + S1:
            case RIGHT_SLIP_SERIALS_SPI_CLKCHECK + S2:
            case RIGHT_SLIP_SERIALS_SPI_CLKCHECK + S3:
            case RIGHT_SLIP_SERIALS_SPI_CLKCHECK + S4:
                return String.valueOf(false);
            case RIGHT_SLIP_SERIALS_SPI_DATACHECK + S1:
            case RIGHT_SLIP_SERIALS_SPI_DATACHECK + S2:
            case RIGHT_SLIP_SERIALS_SPI_DATACHECK + S3:
            case RIGHT_SLIP_SERIALS_SPI_DATACHECK + S4:
                return String.valueOf(false);
            case RIGHT_SLIP_SERIALS_SPI_CSCHECK + S1:
            case RIGHT_SLIP_SERIALS_SPI_CSCHECK + S2:
            case RIGHT_SLIP_SERIALS_SPI_CSCHECK + S3:
            case RIGHT_SLIP_SERIALS_SPI_CSCHECK + S4:
                return String.valueOf(false);
            case RIGHT_SLIP_SERIALS_SPI_CSSWITCH + S1:
            case RIGHT_SLIP_SERIALS_SPI_CSSWITCH + S2:
            case RIGHT_SLIP_SERIALS_SPI_CSSWITCH + S3:
            case RIGHT_SLIP_SERIALS_SPI_CSSWITCH + S4:
                return String.valueOf(false);
            case RIGHT_SLIP_SERIALS_I2C_SDA + S1:
            case RIGHT_SLIP_SERIALS_I2C_SDA + S2:
            case RIGHT_SLIP_SERIALS_I2C_SDA + S3:
            case RIGHT_SLIP_SERIALS_I2C_SDA + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_I2C_SCL + S1:
            case RIGHT_SLIP_SERIALS_I2C_SCL + S2:
            case RIGHT_SLIP_SERIALS_I2C_SCL + S3:
            case RIGHT_SLIP_SERIALS_I2C_SCL + S4:
                return String.valueOf(1);
            case RIGHT_SLIP_SERIALS_M429_SOURCE + S1:
            case RIGHT_SLIP_SERIALS_M429_SOURCE + S2:
            case RIGHT_SLIP_SERIALS_M429_SOURCE + S3:
            case RIGHT_SLIP_SERIALS_M429_SOURCE + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_M429_FORMAT + S1:
            case RIGHT_SLIP_SERIALS_M429_FORMAT + S2:
            case RIGHT_SLIP_SERIALS_M429_FORMAT + S3:
            case RIGHT_SLIP_SERIALS_M429_FORMAT + S4:
                return String.valueOf(2);
            case RIGHT_SLIP_SERIALS_M429_DISPLAY + S1:
            case RIGHT_SLIP_SERIALS_M429_DISPLAY + S2:
            case RIGHT_SLIP_SERIALS_M429_DISPLAY + S3:
            case RIGHT_SLIP_SERIALS_M429_DISPLAY + S4:
                return String.valueOf(1);
            case RIGHT_SLIP_SERIALS_M429_BAUDRATE + S1:
            case RIGHT_SLIP_SERIALS_M429_BAUDRATE + S2:
            case RIGHT_SLIP_SERIALS_M429_BAUDRATE + S3:
            case RIGHT_SLIP_SERIALS_M429_BAUDRATE + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_M1553B_SOURCE + S1:
            case RIGHT_SLIP_SERIALS_M1553B_SOURCE + S2:
            case RIGHT_SLIP_SERIALS_M1553B_SOURCE + S3:
            case RIGHT_SLIP_SERIALS_M1553B_SOURCE + S4:
                return String.valueOf(0);
            case RIGHT_SLIP_SERIALS_M1553B_DISPLAY + S1:
            case RIGHT_SLIP_SERIALS_M1553B_DISPLAY + S2:
            case RIGHT_SLIP_SERIALS_M1553B_DISPLAY + S3:
            case RIGHT_SLIP_SERIALS_M1553B_DISPLAY + S4:
                return String.valueOf(1);
            case TOP_SLIP:
                return String.valueOf(0);
            case TOP_SLIP_MEASURE:
                return String.valueOf(0);
            case TOP_SLIP_MEASURE_STATIC_ALL:
                return String.valueOf(false);
            case TOP_SLIP_MEASURE_STATIC_MEAN:
            case TOP_SLIP_MEASURE_STATIC_MAX:
            case TOP_SLIP_MEASURE_STATIC_MIN:
            case TOP_SLIP_MEASURE_STATIC_DELTA:
            case TOP_SLIP_MEASURE_STATIC_COUNT:
                return String.valueOf(true);
            case TOP_SLIP_MEASURE_SETTING_INDICATOR:
                return String.valueOf(false);
            case TOP_SLIP_MEASURE_SETTING_RANGE:
                return String.valueOf(0);
            case TOP_SLIP_MEASURE_SETTING_CHANNEL_SELECT:
                return "";
            case TOP_SLIP_MEASURE_SETTING_THRESHOLDS:
                return String.valueOf(0);
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Ch1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Ch2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Ch3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Ch4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Ch5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Ch6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Ch7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Ch8 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Math1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Math2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Math3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Math4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Math5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Math6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Math7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.Math8 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.R1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.R2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.R3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.R4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.R5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.R6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.R7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "0" + (TChan.R8 - TChan.Ch1):
                return String.valueOf(90);
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Ch1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Ch2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Ch3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Ch4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Ch5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Ch6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Ch7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Ch8 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Math1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Math2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Math3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Math4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Math5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Math6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Math7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.Math8 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.R1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.R2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.R3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.R4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.R5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.R6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.R7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + (TChan.R8 - TChan.Ch1):
                return String.valueOf(50);
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Ch1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Ch2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Ch3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Ch4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Ch5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Ch6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Ch7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Ch8 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Math1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Math2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Math3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Math4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Math5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Math6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Math7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.Math8 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.R1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.R2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.R3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.R4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.R5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.R6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.R7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "0" + (TChan.R8 - TChan.Ch1):
                return String.valueOf(10);
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Ch1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Ch2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Ch3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Ch4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Ch5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Ch6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Ch7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Ch8 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Math1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Math2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Math3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Math4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Math5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Math6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Math7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.Math8 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.R1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.R2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.R3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.R4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.R5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.R6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.R7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_HIGH + "1" + (TChan.R8 - TChan.Ch1):
                return String.valueOf(1);
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Ch1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Ch2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Ch3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Ch4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Ch5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Ch6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Ch7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Ch8 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Math1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Math2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Math3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Math4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Math5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Math6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Math7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.Math8 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.R1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.R2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.R3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.R4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.R5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.R6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.R7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + (TChan.R8 - TChan.Ch1):
                return String.valueOf(0);
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Ch1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Ch2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Ch3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Ch4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Ch5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Ch6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Ch7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Ch8 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Math1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Math2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Math3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Math4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Math5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Math6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Math7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.Math8 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.R1 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.R2 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.R3 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.R4 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.R5 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.R6 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.R7 - TChan.Ch1):
            case TOP_SLIP_MEASURE_SETTING_LOW + "1" + (TChan.R8 - TChan.Ch1):
                return String.valueOf(-1);

            case TOP_SLIP_MEASURE_CHANNEL_SELECT:
                return String.valueOf(0);
            case TOP_SLIP_MEASURE_SELECT_LIST_CHANNEL:
                return "";
            case TOP_SLIP_MEASURE_SELECT_LIST_NO:
            case TOP_SLIP_MEASURE_SELECT_LIST_INDEX:
                return "";
            case TOP_SLIP_MEASURE_DELAY_DATA + ("-1"):
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Ch1:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Ch2:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Ch3:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Ch4:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Ch5:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Ch6:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Ch7:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Ch8:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Math1:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Math2:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Math3:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Math4:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Math5:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Math6:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Math7:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.Math8:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.R1:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.R2:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.R3:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.R4:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.R5:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.R6:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.R7:
            case TOP_SLIP_MEASURE_DELAY_DATA + TChan.R8:
                return "0" + CacheUtil.DELAY_SLIP + "0" + CacheUtil.DELAY_SLIP + "0";

            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Ch1:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Ch2:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Ch3:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Ch4:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Ch5:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Ch6:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Ch7:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Ch8:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Math1:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Math2:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Math3:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Math4:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Math5:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Math6:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Math7:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.Math8:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.R1:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.R2:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.R3:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.R4:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.R5:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.R6:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.R7:
            case TOP_SLIP_MEASURE_PHASE_DATA + TChan.R8:
                return String.valueOf(0);
            case TOP_SLIP_SAVE_CHANNEL_SELECT:
                return String.valueOf(0);
            case TOP_SLIP_SAVE_DIR:
                return String.valueOf(0);
            case TOP_SLIP_SAVE_TYPE:
                return String.valueOf(0);
            case TOP_SLIP_CURSOR:
                return String.valueOf(0);
            case TOP_SLIP_CURSOR_COMMON:
                return String.valueOf(0);
            case TOP_SLIP_CURSOR_COMMON_SOURCE:
                return String.valueOf(ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT + ChannelFactory.REF_CNT);
            case TOP_SLIP_CURSOR_SETTING_TRANCE:
                return String.valueOf(true);
            case TOP_SLIP_CURSOR_SETTING_MODE:
                return String.valueOf(false);
            case TOP_SLIP_SAMPLE:
                return String.valueOf(0);
            case TOP_SLIP_SAMPLE_MODE:
                return String.valueOf(0);
            case TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX:
                return String.valueOf(0);
            case TOP_SLIP_SAMPLE_DEPTH:
                return String.valueOf(0);
            case TOP_SLIP_SAMPLE_SEGMENTED_STATE:
                return String.valueOf(1);
            case TOP_SLIP_SAMPLE_SEGMENTED_NUMBER:
                return String.valueOf(2);
            case TOP_SLIP_SAMPLE_SEGMENTED_NUMBER_USERDEFINE:
                return String.valueOf(2);
            case TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY:
                return String.valueOf(0);
            case TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_START:
                return String.valueOf(1);
            case TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_END:
                return String.valueOf(1);
            case TOP_SLIP_SAMPLE_SEGMENTED_ORDER:
                return String.valueOf(0);
            case MAIN_CENTER_SAMPLE_SEGMENTED_PLAY_SPEED:
                return String.valueOf(1);
            case TOP_SLIP_DISPLAY:
                return String.valueOf(0);
            case TOP_SLIP_DISPLAY_COMMON_HORREF:
                return String.valueOf(0);
            case TOP_SLIP_DISPLAY_COMMON_TIMEBASE:
                return String.valueOf(0);
            case TOP_SLIP_DISPLAY_COMMON_ROLL:
                return String.valueOf(0);
            case TOP_SLIP_DISPLAY_COMMON_CCT:
                return String.valueOf(1);
            case TOP_SLIP_DISPLAY_COMMON_SCALE:
                return String.valueOf(1);
            case TOP_SLIP_DISPLAY_COMMON_ALPHA:
                return String.valueOf(0);
            case TOP_SLIP_DISPLAY_WAVEFORM_DRAWTYPE:
                return String.valueOf(1);
            case TOP_SLIP_DISPLAY_WAVEFORM_BACKGROUND:
                return String.valueOf(0);
            case TOP_SLIP_DISPLAY_WAVEFORM_BRIGHT:
                return String.valueOf(50);
            case TOP_SLIP_DISPLAY_GRATICULE_MODE:
                return String.valueOf(0);
            case TOP_SLIP_DISPLAY_GRATICULE_INTENSITY:
                return String.valueOf(30);
            case TOP_SLIP_DISPLAY_PERSIST_PERSIST:
            case TOP_SLIP_DISPLAY_FFT_PERSIST_PERSIST:
                return String.valueOf(1);
            case TOP_SLIP_DISPLAY_FFT_PERSIST_SELECT:
                return String.valueOf(0);
            case TOP_SLIP_DISPLAY_PERSIST_SELECT:
                return String.valueOf(4);
            case TOP_SLIP_AUTO:
                return String.valueOf(0);
            case TOP_SLIP_AUTO_SET_CHANNEL:
                return String.valueOf(1);
            case TOP_SLIP_AUTO_SET_SOURCE:
                return String.valueOf(0);
            case TOP_SLIP_AUTO_SET_LEVELSELECT:
                return String.valueOf(1);
            case TOP_SLIP_AUTO_SET_LEVELDETAIL:
                return String.valueOf(10);
            case TOP_SLIP_AUTO_RANGE_RANGE:
                return String.valueOf(1);
            case TOP_SLIP_AUTO_RANGE_VERTICAL:
                return String.valueOf(0);
            case TOP_SLIP_AUTO_RANGE_HORIZONTAL:
                return String.valueOf(0);
            case TOP_SLIP_AUTO_RANGE_LEVEL:
                return String.valueOf(0);
            case TOP_SLIP_FREQUENCY_METER:
                return String.valueOf(0);
            case TOP_SLIP_USERSET:
                return String.valueOf(0);
//            case TOP_SLIP_USERSET_DEPTH:
//                return String.valueOf(0);
            case TOP_SLIP_USERSET_TIMESTAMP:
                return String.valueOf(false);
            case TOP_SLIP_USERSET_SCREENINVERT:
                return String.valueOf(false);
            case TOP_SLIP_USERSET_TRIGGER:
            case TOP_SLIP_USERSET_CLOCK:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER:
                return String.valueOf(1);
            case TOP_SLIP_TRIGGER_SENSITIVITY:
                return String.valueOf(45);
            case TOP_SLIP_TRIGGER_COMMON_TIME:
                return "200ns";
            case TOP_SLIP_TRIGGER_COMMON_MODE:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_SOURCE:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_EDGE_EDGE:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_EDGE_COUPLE:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_PULSEWIDTH_POLAR:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_PULSEWIDTH_CONDITION:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_PULSEWIDTH_PULSEWIDTH:
            case TOP_SLIP_TRIGGER_PULSEWIDTH_TIME_HIGH:
            case TOP_SLIP_TRIGGER_PULSEWIDTH_TIME_LOW:
                return "1μs";
            case TOP_SLIP_TRIGGER_LOGIC_CH1:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_LOGIC_CH2:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_LOGIC_CH3:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_LOGIC_CH4:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_LOGIC_LOGIC:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_LOGIC_CONDITION:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_LOGIC_TIME_HIGH:
                return "1μs";
            case TOP_SLIP_TRIGGER_LOGIC_TIME_LOW:
                return "1μs";
            case TOP_SLIP_TRIGGER_LOGIC_TVLOGIC:
                return "1μs";
            case TOP_SLIP_TRIGGER_NEDGE_SLOPE:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_NEDGE_IDLE:
                return "1μs";
            case TOP_SLIP_TRIGGER_NEDGE_EDGE:
                return String.valueOf(1);
            case TOP_SLIP_TRIGGER_RUNT_POLAR:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_RUNT_CONDITION:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_RUNT_TIME_HIGH:
                return "1μs";
            case TOP_SLIP_TRIGGER_RUNT_TIME_LOW:
                return "1μs";
            case TOP_SLIP_TRIGGER_SLOPE_EDGE:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_SLOPE_CONDITION:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_SLOPE_TIME_HIGH:
                return "1μs";
            case TOP_SLIP_TRIGGER_SLOPE_TIME_LOW:
                return "1μs";
            case TOP_SLIP_TRIGGER_TIMEOUT_POLAR:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_TIMEOUT_OVERTIME:
                return "8ns";
            case TOP_SLIP_TRIGGER_VIDEO_POLAR:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_VIDEO_STANDARD:
                return String.valueOf(2);
            case TOP_SLIP_TRIGGER_VIDEO_TRIGGER + 0:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_VIDEO_TRIGGER + 1:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_VIDEO_TRIGGER + 2:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_VIDEO_TRIGGER + 3:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_VIDEO_TRIGGER + 4:
                return String.valueOf(2);
            case TOP_SLIP_TRIGGER_VIDEO_TRIGGER + 5:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_VIDEO_FREQUENCY + 0:
            case TOP_SLIP_TRIGGER_VIDEO_FREQUENCY + 1:
            case TOP_SLIP_TRIGGER_VIDEO_FREQUENCY + 2:
            case TOP_SLIP_TRIGGER_VIDEO_FREQUENCY + 3:
            case TOP_SLIP_TRIGGER_VIDEO_FREQUENCY + 4:
            case TOP_SLIP_TRIGGER_VIDEO_FREQUENCY + 5:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_VIDEO_LINE + 0:
            case TOP_SLIP_TRIGGER_VIDEO_LINE + 1:
            case TOP_SLIP_TRIGGER_VIDEO_LINE + 2:
            case TOP_SLIP_TRIGGER_VIDEO_LINE + 3:
            case TOP_SLIP_TRIGGER_VIDEO_LINE + 4:
            case TOP_SLIP_TRIGGER_VIDEO_LINE + 5:
                return String.valueOf(1);
            case TOP_SLIP_TRIGGER_SERIALS_UART + S1:
            case TOP_SLIP_TRIGGER_SERIALS_UART + S2:
            case TOP_SLIP_TRIGGER_SERIALS_UART + S3:
            case TOP_SLIP_TRIGGER_SERIALS_UART + S4:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_SERIALS_LIN + S1:
            case TOP_SLIP_TRIGGER_SERIALS_LIN + S2:
            case TOP_SLIP_TRIGGER_SERIALS_LIN + S3:
            case TOP_SLIP_TRIGGER_SERIALS_LIN + S4:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_SERIALS_CAN + S1:
            case TOP_SLIP_TRIGGER_SERIALS_CAN + S2:
            case TOP_SLIP_TRIGGER_SERIALS_CAN + S3:
            case TOP_SLIP_TRIGGER_SERIALS_CAN + S4:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_SERIALS_SPI + S1:
            case TOP_SLIP_TRIGGER_SERIALS_SPI + S2:
            case TOP_SLIP_TRIGGER_SERIALS_SPI + S3:
            case TOP_SLIP_TRIGGER_SERIALS_SPI + S4:
                return String.valueOf(1);
            case TOP_SLIP_TRIGGER_SERIALS_I2C + S1:
            case TOP_SLIP_TRIGGER_SERIALS_I2C + S2:
            case TOP_SLIP_TRIGGER_SERIALS_I2C + S3:
            case TOP_SLIP_TRIGGER_SERIALS_I2C + S4:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_SERIALS_M429 + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M429 + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M429 + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M429 + S4:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_SERIALS_M1553B + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M1553B + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M1553B + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M1553B + S4:
                return String.valueOf(0);
            case TOP_SLIP_TRIGGER_SERIALS_M429_DATA + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M429_DATA + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M429_DATA + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M429_DATA + S4:
                return "0 00 00";
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABEL + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABEL + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABEL + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABEL + S4:
                return "000";
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_LABEL + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_LABEL + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_LABEL + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_LABEL + S4:
                return "000";
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_DATA + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_DATA + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_DATA + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELDATA_DATA + S4:
                return "0 00 00";
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_LABEL + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_LABEL + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_LABEL + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_LABEL + S4:
                return "000";
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_SDI + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_SDI + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_SDI + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_SDI + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_LABEL + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_LABEL + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_LABEL + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_LABEL + S4:
                return "000";
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_SSM + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_SSM + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_SSM + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M429_LABELSSM_SSM + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_M429_SDI + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M429_SDI + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M429_SDI + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M429_SDI + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_M429_SSM + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M429_SSM + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M429_SSM + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M429_SSM + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_CAN_DATAID + S1:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_DATAID + S2:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_DATAID + S3:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_DATAID + S4:
                return "00 00 00 00";
            case TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_ID + S1:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_ID + S2:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_ID + S3:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_ID + S4:
                return "00 00 00 00";
            case TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC + S1:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC + S2:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC + S3:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC + S4:
                return "0";
            case TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA + S1:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA + S2:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA + S3:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA + S4:
                return "";
            case TOP_SLIP_TRIGGER_SERIALS_CAN_RDID + S1:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_RDID + S2:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_RDID + S3:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_RDID + S4:
                return "00 00 00 00";
            case TOP_SLIP_TRIGGER_SERIALS_CAN_REMOTEID + S1:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_REMOTEID + S2:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_REMOTEID + S3:
            case TOP_SLIP_TRIGGER_SERIALS_CAN_REMOTEID + S4:
                return "00 00 00 00";
            case TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_ADDR + S1:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_ADDR + S2:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_ADDR + S3:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_ADDR + S4:
                return "000";
            case TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_DATA + S1:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_DATA + S2:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_DATA + S3:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_10WRITEFRAME_DATA + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_ADDR + S1:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_ADDR + S2:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_ADDR + S3:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_ADDR + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_DATA + S1:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_DATA + S2:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_DATA + S3:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME1_DATA + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_ADDR + S1:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_ADDR + S2:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_ADDR + S3:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_ADDR + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA1 + S1:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA1 + S2:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA1 + S3:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA1 + S4:

                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA2 + S1:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA2 + S2:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA2 + S3:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA2 + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_I2C_NOACKINADR + S1:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_NOACKINADR + S2:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_NOACKINADR + S3:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_NOACKINADR + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_CONDITION + S1:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_CONDITION + S2:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_CONDITION + S3:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_CONDITION + S4:
                return String.valueOf(2);
            case TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_DATA + S1:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_DATA + S2:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_DATA + S3:
            case TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_DATA + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_LIN_FRAMEID + S1:
            case TOP_SLIP_TRIGGER_SERIALS_LIN_FRAMEID + S2:
            case TOP_SLIP_TRIGGER_SERIALS_LIN_FRAMEID + S3:
            case TOP_SLIP_TRIGGER_SERIALS_LIN_FRAMEID + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_ID + S1:
            case TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_ID + S2:
            case TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_ID + S3:
            case TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_ID + S4:
            case TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID + S1:
            case TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID + S2:
            case TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID + S3:
            case TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA + S1:
            case TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA + S2:
            case TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA + S3:
            case TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA + S4:
                return "00 00";
            case TOP_SLIP_TRIGGER_SERIALS_M1553B_CSWORD + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M1553B_CSWORD + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M1553B_CSWORD + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M1553B_CSWORD + S4:
                return "00 00";
            case TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + S4:
                return "00 00";
            case TOP_SLIP_TRIGGER_SERIALS_M1553B_RTADDR + S1:
            case TOP_SLIP_TRIGGER_SERIALS_M1553B_RTADDR + S2:
            case TOP_SLIP_TRIGGER_SERIALS_M1553B_RTADDR + S3:
            case TOP_SLIP_TRIGGER_SERIALS_M1553B_RTADDR + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_SPI_DATA + S1:
            case TOP_SLIP_TRIGGER_SERIALS_SPI_DATA + S2:
            case TOP_SLIP_TRIGGER_SERIALS_SPI_DATA + S3:
            case TOP_SLIP_TRIGGER_SERIALS_SPI_DATA + S4:
                return "XXXX XXXX XXXX XXXX XXXX XXXX XXXX XXXX";
            case TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_CONDITION + S1:
            case TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_CONDITION + S2:
            case TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_CONDITION + S3:
            case TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_CONDITION + S4:
                return String.valueOf(2);
            case TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_EDIT + S1:
            case TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_EDIT + S2:
            case TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_EDIT + S3:
            case TOP_SLIP_TRIGGER_SERIALS_UART_0DATA_EDIT + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_CONDITION + S1:
            case TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_CONDITION + S2:
            case TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_CONDITION + S3:
            case TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_CONDITION + S4:
                return String.valueOf(2);
            case TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + S1:
            case TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + S2:
            case TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + S3:
            case TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_UART_DATA_CONDITION + S1:
            case TOP_SLIP_TRIGGER_SERIALS_UART_DATA_CONDITION + S2:
            case TOP_SLIP_TRIGGER_SERIALS_UART_DATA_CONDITION + S3:
            case TOP_SLIP_TRIGGER_SERIALS_UART_DATA_CONDITION + S4:
                return String.valueOf(2);
            case TOP_SLIP_TRIGGER_SERIALS_UART_DATA_EDIT + S1:
            case TOP_SLIP_TRIGGER_SERIALS_UART_DATA_EDIT + S2:
            case TOP_SLIP_TRIGGER_SERIALS_UART_DATA_EDIT + S3:
            case TOP_SLIP_TRIGGER_SERIALS_UART_DATA_EDIT + S4:
                return "00";
            case TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_CONDITION + S1:
            case TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_CONDITION + S2:
            case TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_CONDITION + S3:
            case TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_CONDITION + S4:
                return String.valueOf(2);
            case TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_EDIT + S1:
            case TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_EDIT + S2:
            case TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_EDIT + S3:
            case TOP_SLIP_TRIGGER_SERIALS_UART_XDATA_EDIT + S4:
                return "00";
            case TRIGGER_CHANNEL + TChan.Ch1:
            case TRIGGER_CHANNEL + TChan.Ch2:
            case TRIGGER_CHANNEL + TChan.Ch3:
            case TRIGGER_CHANNEL + TChan.Ch4:
            case TRIGGER_CHANNEL + TChan.Ch5:
            case TRIGGER_CHANNEL + TChan.Ch6:
            case TRIGGER_CHANNEL + TChan.Ch7:
            case TRIGGER_CHANNEL + TChan.Ch8:
            case TRIGGER_CHANNEL_TEMP + TChan.Ch1:
            case TRIGGER_CHANNEL_TEMP + TChan.Ch2:
            case TRIGGER_CHANNEL_TEMP + TChan.Ch3:
            case TRIGGER_CHANNEL_TEMP + TChan.Ch4:
            case TRIGGER_CHANNEL_TEMP + TChan.Ch5:
            case TRIGGER_CHANNEL_TEMP + TChan.Ch6:
            case TRIGGER_CHANNEL_TEMP + TChan.Ch7:
            case TRIGGER_CHANNEL_TEMP + TChan.Ch8:
                return String.valueOf(0);
            case VALUE_CHANNEL + TChan.Ch1:
            case VALUE_CHANNEL + TChan.Ch2:
            case VALUE_CHANNEL + TChan.Ch3:
            case VALUE_CHANNEL + TChan.Ch4:
            case VALUE_CHANNEL + TChan.Ch5:
            case VALUE_CHANNEL + TChan.Ch6:
            case VALUE_CHANNEL + TChan.Ch7:
            case VALUE_CHANNEL + TChan.Ch8:
            case VALUE_CHANNEL_TEMP + TChan.Ch1:
            case VALUE_CHANNEL_TEMP + TChan.Ch2:
            case VALUE_CHANNEL_TEMP + TChan.Ch3:
            case VALUE_CHANNEL_TEMP + TChan.Ch4:
            case VALUE_CHANNEL_TEMP + TChan.Ch5:
            case VALUE_CHANNEL_TEMP + TChan.Ch6:
            case VALUE_CHANNEL_TEMP + TChan.Ch7:
            case VALUE_CHANNEL_TEMP + TChan.Ch8:
                return String.valueOf(100);
            case VALUE_CHANNEL + TChan.Ch8 + 1://针对外部触发
            case VALUE_CHANNEL_TEMP + TChan.Ch8 + 1:
                return String.valueOf(500);
            case VALUE_INIT + S1 + UART + TChan.Ch1:
            case VALUE_INIT + S1 + UART + TChan.Ch2:
            case VALUE_INIT + S1 + UART + TChan.Ch3:
            case VALUE_INIT + S1 + UART + TChan.Ch4:
            case VALUE_INIT + S1 + UART + TChan.Ch5:
            case VALUE_INIT + S1 + UART + TChan.Ch6:
            case VALUE_INIT + S1 + UART + TChan.Ch7:
            case VALUE_INIT + S1 + UART + TChan.Ch8:
            case VALUE_INIT + S1 + LIN + TChan.Ch1:
            case VALUE_INIT + S1 + LIN + TChan.Ch2:
            case VALUE_INIT + S1 + LIN + TChan.Ch3:
            case VALUE_INIT + S1 + LIN + TChan.Ch4:
            case VALUE_INIT + S1 + LIN + TChan.Ch5:
            case VALUE_INIT + S1 + LIN + TChan.Ch6:
            case VALUE_INIT + S1 + LIN + TChan.Ch7:
            case VALUE_INIT + S1 + LIN + TChan.Ch8:
            case VALUE_INIT + S1 + CAN + TChan.Ch1:
            case VALUE_INIT + S1 + CAN + TChan.Ch2:
            case VALUE_INIT + S1 + CAN + TChan.Ch3:
            case VALUE_INIT + S1 + CAN + TChan.Ch4:
            case VALUE_INIT + S1 + CAN + TChan.Ch5:
            case VALUE_INIT + S1 + CAN + TChan.Ch6:
            case VALUE_INIT + S1 + CAN + TChan.Ch7:
            case VALUE_INIT + S1 + CAN + TChan.Ch8:
            case VALUE_INIT + S1 + SPI + TChan.Ch1:
            case VALUE_INIT + S1 + SPI + TChan.Ch2:
            case VALUE_INIT + S1 + SPI + TChan.Ch3:
            case VALUE_INIT + S1 + SPI + TChan.Ch4:
            case VALUE_INIT + S1 + SPI + TChan.Ch5:
            case VALUE_INIT + S1 + SPI + TChan.Ch6:
            case VALUE_INIT + S1 + SPI + TChan.Ch7:
            case VALUE_INIT + S1 + SPI + TChan.Ch8:
            case VALUE_INIT + S1 + I2C + TChan.Ch1:
            case VALUE_INIT + S1 + I2C + TChan.Ch2:
            case VALUE_INIT + S1 + I2C + TChan.Ch3:
            case VALUE_INIT + S1 + I2C + TChan.Ch4:
            case VALUE_INIT + S1 + I2C + TChan.Ch5:
            case VALUE_INIT + S1 + I2C + TChan.Ch6:
            case VALUE_INIT + S1 + I2C + TChan.Ch7:
            case VALUE_INIT + S1 + I2C + TChan.Ch8:
            case VALUE_INIT + S1 + M429 + TChan.Ch1:
            case VALUE_INIT + S1 + M429 + TChan.Ch2:
            case VALUE_INIT + S1 + M429 + TChan.Ch3:
            case VALUE_INIT + S1 + M429 + TChan.Ch4:
            case VALUE_INIT + S1 + M429 + TChan.Ch5:
            case VALUE_INIT + S1 + M429 + TChan.Ch6:
            case VALUE_INIT + S1 + M429 + TChan.Ch7:
            case VALUE_INIT + S1 + M429 + TChan.Ch8:
            case VALUE_INIT + S1 + M1553B + TChan.Ch1:
            case VALUE_INIT + S1 + M1553B + TChan.Ch2:
            case VALUE_INIT + S1 + M1553B + TChan.Ch3:
            case VALUE_INIT + S1 + M1553B + TChan.Ch4:
            case VALUE_INIT + S1 + M1553B + TChan.Ch5:
            case VALUE_INIT + S1 + M1553B + TChan.Ch6:
            case VALUE_INIT + S1 + M1553B + TChan.Ch7:
            case VALUE_INIT + S1 + M1553B + TChan.Ch8:
            case VALUE_INIT + S2 + UART + TChan.Ch1:
            case VALUE_INIT + S2 + UART + TChan.Ch2:
            case VALUE_INIT + S2 + UART + TChan.Ch3:
            case VALUE_INIT + S2 + UART + TChan.Ch4:
            case VALUE_INIT + S2 + UART + TChan.Ch5:
            case VALUE_INIT + S2 + UART + TChan.Ch6:
            case VALUE_INIT + S2 + UART + TChan.Ch7:
            case VALUE_INIT + S2 + UART + TChan.Ch8:
            case VALUE_INIT + S2 + LIN + TChan.Ch1:
            case VALUE_INIT + S2 + LIN + TChan.Ch2:
            case VALUE_INIT + S2 + LIN + TChan.Ch3:
            case VALUE_INIT + S2 + LIN + TChan.Ch4:
            case VALUE_INIT + S2 + LIN + TChan.Ch5:
            case VALUE_INIT + S2 + LIN + TChan.Ch6:
            case VALUE_INIT + S2 + LIN + TChan.Ch7:
            case VALUE_INIT + S2 + LIN + TChan.Ch8:
            case VALUE_INIT + S2 + CAN + TChan.Ch1:
            case VALUE_INIT + S2 + CAN + TChan.Ch2:
            case VALUE_INIT + S2 + CAN + TChan.Ch3:
            case VALUE_INIT + S2 + CAN + TChan.Ch4:
            case VALUE_INIT + S2 + CAN + TChan.Ch5:
            case VALUE_INIT + S2 + CAN + TChan.Ch6:
            case VALUE_INIT + S2 + CAN + TChan.Ch7:
            case VALUE_INIT + S2 + CAN + TChan.Ch8:
            case VALUE_INIT + S2 + SPI + TChan.Ch1:
            case VALUE_INIT + S2 + SPI + TChan.Ch2:
            case VALUE_INIT + S2 + SPI + TChan.Ch3:
            case VALUE_INIT + S2 + SPI + TChan.Ch4:
            case VALUE_INIT + S2 + SPI + TChan.Ch5:
            case VALUE_INIT + S2 + SPI + TChan.Ch6:
            case VALUE_INIT + S2 + SPI + TChan.Ch7:
            case VALUE_INIT + S2 + SPI + TChan.Ch8:
            case VALUE_INIT + S2 + I2C + TChan.Ch1:
            case VALUE_INIT + S2 + I2C + TChan.Ch2:
            case VALUE_INIT + S2 + I2C + TChan.Ch3:
            case VALUE_INIT + S2 + I2C + TChan.Ch4:
            case VALUE_INIT + S2 + I2C + TChan.Ch5:
            case VALUE_INIT + S2 + I2C + TChan.Ch6:
            case VALUE_INIT + S2 + I2C + TChan.Ch7:
            case VALUE_INIT + S2 + I2C + TChan.Ch8:
            case VALUE_INIT + S2 + M429 + TChan.Ch1:
            case VALUE_INIT + S2 + M429 + TChan.Ch2:
            case VALUE_INIT + S2 + M429 + TChan.Ch3:
            case VALUE_INIT + S2 + M429 + TChan.Ch4:
            case VALUE_INIT + S2 + M429 + TChan.Ch5:
            case VALUE_INIT + S2 + M429 + TChan.Ch6:
            case VALUE_INIT + S2 + M429 + TChan.Ch7:
            case VALUE_INIT + S2 + M429 + TChan.Ch8:
            case VALUE_INIT + S2 + M1553B + TChan.Ch1:
            case VALUE_INIT + S2 + M1553B + TChan.Ch2:
            case VALUE_INIT + S2 + M1553B + TChan.Ch3:
            case VALUE_INIT + S2 + M1553B + TChan.Ch4:
            case VALUE_INIT + S2 + M1553B + TChan.Ch5:
            case VALUE_INIT + S2 + M1553B + TChan.Ch6:
            case VALUE_INIT + S2 + M1553B + TChan.Ch7:
            case VALUE_INIT + S2 + M1553B + TChan.Ch8:
            case VALUE_INIT + S3 + UART + TChan.Ch1:
            case VALUE_INIT + S3 + UART + TChan.Ch2:
            case VALUE_INIT + S3 + UART + TChan.Ch3:
            case VALUE_INIT + S3 + UART + TChan.Ch4:
            case VALUE_INIT + S3 + UART + TChan.Ch5:
            case VALUE_INIT + S3 + UART + TChan.Ch6:
            case VALUE_INIT + S3 + UART + TChan.Ch7:
            case VALUE_INIT + S3 + UART + TChan.Ch8:
            case VALUE_INIT + S3 + LIN + TChan.Ch1:
            case VALUE_INIT + S3 + LIN + TChan.Ch2:
            case VALUE_INIT + S3 + LIN + TChan.Ch3:
            case VALUE_INIT + S3 + LIN + TChan.Ch4:
            case VALUE_INIT + S3 + LIN + TChan.Ch5:
            case VALUE_INIT + S3 + LIN + TChan.Ch6:
            case VALUE_INIT + S3 + LIN + TChan.Ch7:
            case VALUE_INIT + S3 + LIN + TChan.Ch8:
            case VALUE_INIT + S3 + CAN + TChan.Ch1:
            case VALUE_INIT + S3 + CAN + TChan.Ch2:
            case VALUE_INIT + S3 + CAN + TChan.Ch3:
            case VALUE_INIT + S3 + CAN + TChan.Ch4:
            case VALUE_INIT + S3 + CAN + TChan.Ch5:
            case VALUE_INIT + S3 + CAN + TChan.Ch6:
            case VALUE_INIT + S3 + CAN + TChan.Ch7:
            case VALUE_INIT + S3 + CAN + TChan.Ch8:
            case VALUE_INIT + S3 + SPI + TChan.Ch1:
            case VALUE_INIT + S3 + SPI + TChan.Ch2:
            case VALUE_INIT + S3 + SPI + TChan.Ch3:
            case VALUE_INIT + S3 + SPI + TChan.Ch4:
            case VALUE_INIT + S3 + SPI + TChan.Ch5:
            case VALUE_INIT + S3 + SPI + TChan.Ch6:
            case VALUE_INIT + S3 + SPI + TChan.Ch7:
            case VALUE_INIT + S3 + SPI + TChan.Ch8:
            case VALUE_INIT + S3 + I2C + TChan.Ch1:
            case VALUE_INIT + S3 + I2C + TChan.Ch2:
            case VALUE_INIT + S3 + I2C + TChan.Ch3:
            case VALUE_INIT + S3 + I2C + TChan.Ch4:
            case VALUE_INIT + S3 + I2C + TChan.Ch5:
            case VALUE_INIT + S3 + I2C + TChan.Ch6:
            case VALUE_INIT + S3 + I2C + TChan.Ch7:
            case VALUE_INIT + S3 + I2C + TChan.Ch8:
            case VALUE_INIT + S3 + M429 + TChan.Ch1:
            case VALUE_INIT + S3 + M429 + TChan.Ch2:
            case VALUE_INIT + S3 + M429 + TChan.Ch3:
            case VALUE_INIT + S3 + M429 + TChan.Ch4:
            case VALUE_INIT + S3 + M429 + TChan.Ch5:
            case VALUE_INIT + S3 + M429 + TChan.Ch6:
            case VALUE_INIT + S3 + M429 + TChan.Ch7:
            case VALUE_INIT + S3 + M429 + TChan.Ch8:
            case VALUE_INIT + S3 + M1553B + TChan.Ch1:
            case VALUE_INIT + S3 + M1553B + TChan.Ch2:
            case VALUE_INIT + S3 + M1553B + TChan.Ch3:
            case VALUE_INIT + S3 + M1553B + TChan.Ch4:
            case VALUE_INIT + S3 + M1553B + TChan.Ch5:
            case VALUE_INIT + S3 + M1553B + TChan.Ch6:
            case VALUE_INIT + S3 + M1553B + TChan.Ch7:
            case VALUE_INIT + S3 + M1553B + TChan.Ch8:
            case VALUE_INIT + S4 + UART + TChan.Ch1:
            case VALUE_INIT + S4 + UART + TChan.Ch2:
            case VALUE_INIT + S4 + UART + TChan.Ch3:
            case VALUE_INIT + S4 + UART + TChan.Ch4:
            case VALUE_INIT + S4 + UART + TChan.Ch5:
            case VALUE_INIT + S4 + UART + TChan.Ch6:
            case VALUE_INIT + S4 + UART + TChan.Ch7:
            case VALUE_INIT + S4 + UART + TChan.Ch8:
            case VALUE_INIT + S4 + LIN + TChan.Ch1:
            case VALUE_INIT + S4 + LIN + TChan.Ch2:
            case VALUE_INIT + S4 + LIN + TChan.Ch3:
            case VALUE_INIT + S4 + LIN + TChan.Ch4:
            case VALUE_INIT + S4 + LIN + TChan.Ch5:
            case VALUE_INIT + S4 + LIN + TChan.Ch6:
            case VALUE_INIT + S4 + LIN + TChan.Ch7:
            case VALUE_INIT + S4 + LIN + TChan.Ch8:
            case VALUE_INIT + S4 + CAN + TChan.Ch1:
            case VALUE_INIT + S4 + CAN + TChan.Ch2:
            case VALUE_INIT + S4 + CAN + TChan.Ch3:
            case VALUE_INIT + S4 + CAN + TChan.Ch4:
            case VALUE_INIT + S4 + CAN + TChan.Ch5:
            case VALUE_INIT + S4 + CAN + TChan.Ch6:
            case VALUE_INIT + S4 + CAN + TChan.Ch7:
            case VALUE_INIT + S4 + CAN + TChan.Ch8:
            case VALUE_INIT + S4 + SPI + TChan.Ch1:
            case VALUE_INIT + S4 + SPI + TChan.Ch2:
            case VALUE_INIT + S4 + SPI + TChan.Ch3:
            case VALUE_INIT + S4 + SPI + TChan.Ch4:
            case VALUE_INIT + S4 + SPI + TChan.Ch5:
            case VALUE_INIT + S4 + SPI + TChan.Ch6:
            case VALUE_INIT + S4 + SPI + TChan.Ch7:
            case VALUE_INIT + S4 + SPI + TChan.Ch8:
            case VALUE_INIT + S4 + I2C + TChan.Ch1:
            case VALUE_INIT + S4 + I2C + TChan.Ch2:
            case VALUE_INIT + S4 + I2C + TChan.Ch3:
            case VALUE_INIT + S4 + I2C + TChan.Ch4:
            case VALUE_INIT + S4 + I2C + TChan.Ch5:
            case VALUE_INIT + S4 + I2C + TChan.Ch6:
            case VALUE_INIT + S4 + I2C + TChan.Ch7:
            case VALUE_INIT + S4 + I2C + TChan.Ch8:
            case VALUE_INIT + S4 + M429 + TChan.Ch1:
            case VALUE_INIT + S4 + M429 + TChan.Ch2:
            case VALUE_INIT + S4 + M429 + TChan.Ch3:
            case VALUE_INIT + S4 + M429 + TChan.Ch4:
            case VALUE_INIT + S4 + M429 + TChan.Ch5:
            case VALUE_INIT + S4 + M429 + TChan.Ch6:
            case VALUE_INIT + S4 + M429 + TChan.Ch7:
            case VALUE_INIT + S4 + M429 + TChan.Ch8:
            case VALUE_INIT + S4 + M1553B + TChan.Ch1:
            case VALUE_INIT + S4 + M1553B + TChan.Ch2:
            case VALUE_INIT + S4 + M1553B + TChan.Ch3:
            case VALUE_INIT + S4 + M1553B + TChan.Ch4:
            case VALUE_INIT + S4 + M1553B + TChan.Ch5:
            case VALUE_INIT + S4 + M1553B + TChan.Ch6:
            case VALUE_INIT + S4 + M1553B + TChan.Ch7:
            case VALUE_INIT + S4 + M1553B + TChan.Ch8:
                return String.valueOf(true);
            case LASTSET_SERIALS:
                return String.valueOf(1);
            case TRIGGER_CHANNEL_H + TChan.Ch1:
            case TRIGGER_CHANNEL_H + TChan.Ch2:
            case TRIGGER_CHANNEL_H + TChan.Ch3:
            case TRIGGER_CHANNEL_H + TChan.Ch4:
            case TRIGGER_CHANNEL_H + TChan.Ch5:
            case TRIGGER_CHANNEL_H + TChan.Ch6:
            case TRIGGER_CHANNEL_H + TChan.Ch7:
            case TRIGGER_CHANNEL_H + TChan.Ch8:
            case TRIGGER_CHANNEL_TEMP_H + TChan.Ch1:
            case TRIGGER_CHANNEL_TEMP_H + TChan.Ch2:
            case TRIGGER_CHANNEL_TEMP_H + TChan.Ch3:
            case TRIGGER_CHANNEL_TEMP_H + TChan.Ch4:
            case TRIGGER_CHANNEL_TEMP_H + TChan.Ch5:
            case TRIGGER_CHANNEL_TEMP_H + TChan.Ch6:
            case TRIGGER_CHANNEL_TEMP_H + TChan.Ch7:
            case TRIGGER_CHANNEL_TEMP_H + TChan.Ch8:
                return String.valueOf(0);
            case VALUE_CHANNEL_H + TChan.Ch1:
            case VALUE_CHANNEL_H + TChan.Ch2:
            case VALUE_CHANNEL_H + TChan.Ch3:
            case VALUE_CHANNEL_H + TChan.Ch4:
            case VALUE_CHANNEL_H + TChan.Ch5:
            case VALUE_CHANNEL_H + TChan.Ch6:
            case VALUE_CHANNEL_H + TChan.Ch7:
            case VALUE_CHANNEL_H + TChan.Ch8:
            case VALUE_CHANNEL_TEMP_H + TChan.Ch1:
            case VALUE_CHANNEL_TEMP_H + TChan.Ch2:
            case VALUE_CHANNEL_TEMP_H + TChan.Ch3:
            case VALUE_CHANNEL_TEMP_H + TChan.Ch4:
            case VALUE_CHANNEL_TEMP_H + TChan.Ch5:
            case VALUE_CHANNEL_TEMP_H + TChan.Ch6:
            case VALUE_CHANNEL_TEMP_H + TChan.Ch7:
            case VALUE_CHANNEL_TEMP_H + TChan.Ch8:
                return String.valueOf(50);
            case MAIN_WAVE_CH_XY_POSITION + TChan.Ch1:
            case MAIN_WAVE_CH_XY_POSITION + TChan.Ch3:
            case MAIN_WAVE_CH_XY_POSITION + TChan.Ch5:
            case MAIN_WAVE_CH_XY_POSITION + TChan.Ch7:
                return String.valueOf(mainWaveXYWidth / 2);
            case MAIN_WAVE_CH_XY_POSITION + TChan.Ch2:
            case MAIN_WAVE_CH_XY_POSITION + TChan.Ch4:
            case MAIN_WAVE_CH_XY_POSITION + TChan.Ch6:
            case MAIN_WAVE_CH_XY_POSITION + TChan.Ch8:
                return String.valueOf(mainWaveXYHeight / 2);
            case MAIN_WAVE_CH_Y_POSITION + TChan.Ch1:
            case MAIN_WAVE_CH_Y_POSITION_YT + TChan.Ch1:
                if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) {
                    return String.valueOf(1040 / 4);
                } else {
                    return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 1));
                }
            case MAIN_WAVE_CH_Y_POSITION + TChan.Ch2:
            case MAIN_WAVE_CH_Y_POSITION_YT + TChan.Ch2:
                if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) {
                    return String.valueOf(1040 / 4 * 3);
                }else {
                    return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 1) * 2);
                }
            case MAIN_WAVE_CH_Y_POSITION + TChan.Ch3:
                return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 1) * 3);
            case MAIN_WAVE_CH_Y_POSITION + TChan.Ch4:
                return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 1) * 4);
            case MAIN_WAVE_CH_Y_POSITION + TChan.Ch5:
                return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 1) * 5);
            case MAIN_WAVE_CH_Y_POSITION + TChan.Ch6:
                return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 1) * 6);
            case MAIN_WAVE_CH_Y_POSITION + TChan.Ch7:
                return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 1) * 7);
            case MAIN_WAVE_CH_Y_POSITION + TChan.Ch8:
                return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 1) * 8);
            case MAIN_WAVE_MATH_DW_Y_POSITION + TChan.Math1:
            case MAIN_WAVE_MATH_AXB_Y_POSITION + TChan.Math1:
            case MAIN_WAVE_MATH_AM_Y_POSITION + TChan.Math1:
            case MAIN_WAVE_MATH_FFT_DB_Y_POSITION + TChan.Math1:
            case MAIN_WAVE_MATH_FFT_RMS_Y_POSITION + TChan.Math1:
                return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 2) * (GlobalVar.get().getChannelsCount() + 2 - 3));
            case MAIN_WAVE_MATH_DW_Y_POSITION + TChan.Math2:
            case MAIN_WAVE_MATH_AXB_Y_POSITION + TChan.Math2:
            case MAIN_WAVE_MATH_AM_Y_POSITION + TChan.Math2:
            case MAIN_WAVE_MATH_FFT_DB_Y_POSITION + TChan.Math2:
            case MAIN_WAVE_MATH_FFT_RMS_Y_POSITION + TChan.Math2:
                return String.valueOf((int) (1040 / (GlobalVar.get().getChannelsCount() + 2) * (GlobalVar.get().getChannelsCount() + 2 - 3.5f)));
            case MAIN_WAVE_MATH_DW_Y_POSITION + TChan.Math3:
            case MAIN_WAVE_MATH_AXB_Y_POSITION + TChan.Math3:
            case MAIN_WAVE_MATH_AM_Y_POSITION + TChan.Math3:
            case MAIN_WAVE_MATH_FFT_DB_Y_POSITION + TChan.Math3:
            case MAIN_WAVE_MATH_FFT_RMS_Y_POSITION + TChan.Math3:
                return String.valueOf((int) (1040 / (GlobalVar.get().getChannelsCount() + 2) * (GlobalVar.get().getChannelsCount() + 2 - 4.0f)));
            case MAIN_WAVE_MATH_DW_Y_POSITION + TChan.Math4:
            case MAIN_WAVE_MATH_AXB_Y_POSITION + TChan.Math4:
            case MAIN_WAVE_MATH_AM_Y_POSITION + TChan.Math4:
            case MAIN_WAVE_MATH_FFT_DB_Y_POSITION + TChan.Math4:
            case MAIN_WAVE_MATH_FFT_RMS_Y_POSITION + TChan.Math4:
                return String.valueOf((int) (1040 / (GlobalVar.get().getChannelsCount() + 2) * (GlobalVar.get().getChannelsCount() + 2 - 4.5f)));
            case MAIN_WAVE_MATH_DW_Y_POSITION + TChan.Math5:
            case MAIN_WAVE_MATH_AXB_Y_POSITION + TChan.Math5:
            case MAIN_WAVE_MATH_AM_Y_POSITION + TChan.Math5:
            case MAIN_WAVE_MATH_FFT_DB_Y_POSITION + TChan.Math5:
            case MAIN_WAVE_MATH_FFT_RMS_Y_POSITION + TChan.Math5:
                return String.valueOf((int) (1040 / (GlobalVar.get().getChannelsCount() + 2) * (GlobalVar.get().getChannelsCount() + 2 - 5.0f)));
            case MAIN_WAVE_MATH_DW_Y_POSITION + TChan.Math6:
            case MAIN_WAVE_MATH_AXB_Y_POSITION + TChan.Math6:
            case MAIN_WAVE_MATH_AM_Y_POSITION + TChan.Math6:
            case MAIN_WAVE_MATH_FFT_DB_Y_POSITION + TChan.Math6:
            case MAIN_WAVE_MATH_FFT_RMS_Y_POSITION + TChan.Math6:
                return String.valueOf((int) (1040 / (GlobalVar.get().getChannelsCount() + 2) * (GlobalVar.get().getChannelsCount() + 2 - 5.5f)));
            case MAIN_WAVE_MATH_DW_Y_POSITION + TChan.Math7:
            case MAIN_WAVE_MATH_AXB_Y_POSITION + TChan.Math7:
            case MAIN_WAVE_MATH_AM_Y_POSITION + TChan.Math7:
            case MAIN_WAVE_MATH_FFT_DB_Y_POSITION + TChan.Math7:
            case MAIN_WAVE_MATH_FFT_RMS_Y_POSITION + TChan.Math7:
                return String.valueOf((int) (1040 / (GlobalVar.get().getChannelsCount() + 2) * (GlobalVar.get().getChannelsCount() + 2 - 6.0f)));
            case MAIN_WAVE_MATH_DW_Y_POSITION + TChan.Math8:
            case MAIN_WAVE_MATH_AXB_Y_POSITION + TChan.Math8:
            case MAIN_WAVE_MATH_AM_Y_POSITION + TChan.Math8:
            case MAIN_WAVE_MATH_FFT_DB_Y_POSITION + TChan.Math8:
            case MAIN_WAVE_MATH_FFT_RMS_Y_POSITION + TChan.Math8:
                return String.valueOf((int) (1040 / (GlobalVar.get().getChannelsCount() + 2) * (GlobalVar.get().getChannelsCount() + 2 - 6.5f)));
            case MAIN_WAVE_REF_Y_POSITION + TChan.R1:
                return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 2));
            case MAIN_WAVE_REF_Y_POSITION + TChan.R2:
                return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 2) * 2);
            case MAIN_WAVE_REF_Y_POSITION + TChan.R3:
                if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {
                    return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 2) * 3);
                } else {
                    return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 2) * 4);
                }
            case MAIN_WAVE_REF_Y_POSITION + TChan.R4:
                if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {
                    return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 2) * 4);
                } else {
                    return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 2) * 5);
                }
            case MAIN_WAVE_REF_Y_POSITION + TChan.R5:
                return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 2) * 6);
            case MAIN_WAVE_REF_Y_POSITION + TChan.R6:
                return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 2) * 7);
            case MAIN_WAVE_REF_Y_POSITION + TChan.R7:
                return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 2) * 8);
            case MAIN_WAVE_REF_Y_POSITION + TChan.R8:
                return String.valueOf(1040 / (GlobalVar.get().getChannelsCount() + 2) * 9);
            case MAIN_WAVE_SERIAL_Y_POSITION + TChan.S1:
                return String.valueOf(1000 / (GlobalVar.get().getChannelsCount() + 2) * (GlobalVar.get().getChannelsCount() + 1) - 12);//12为串口通道图标高
            case MAIN_WAVE_SERIAL_Y_POSITION + TChan.S2:
                return String.valueOf((int)(1000 / (GlobalVar.get().getChannelsCount() + 2) * (GlobalVar.get().getChannelsCount() + 0.5f)) - 12);
            case MAIN_WAVE_SERIAL_Y_POSITION + TChan.S3:
                return String.valueOf((int) (1000 / (GlobalVar.get().getChannelsCount() + 2) * (GlobalVar.get().getChannelsCount())) - 12);
            case MAIN_WAVE_SERIAL_Y_POSITION + TChan.S4:
                return String.valueOf((int) (1000 / (GlobalVar.get().getChannelsCount() + 2) * (GlobalVar.get().getChannelsCount() - 0.5f)) - 12);
            case MAIN_WAVE_TIMEBASE_POSITION_NORMAL:
            case MAIN_WAVE_TIMEBASE_POSITION_FFTMOD:
            case MAIN_WAVE_TIMEBASE_POSITION_RN + TChan.R1:
            case MAIN_WAVE_TIMEBASE_POSITION_RN + TChan.R2:
            case MAIN_WAVE_TIMEBASE_POSITION_RN + TChan.R3:
            case MAIN_WAVE_TIMEBASE_POSITION_RN + TChan.R4:
            case MAIN_WAVE_TIMEBASE_POSITION_RN + TChan.R5:
            case MAIN_WAVE_TIMEBASE_POSITION_RN + TChan.R6:
            case MAIN_WAVE_TIMEBASE_POSITION_RN + TChan.R7:
            case MAIN_WAVE_TIMEBASE_POSITION_RN + TChan.R8:
                return String.valueOf(mainWaveYT.x / 2);
            case MAIN_WAVE_TIMEBASE_MIN:
                return String.valueOf(HorizontalAxis.getMinGear());
            case MAIN_WAVE_TIMEBASE_MAX:
                return String.valueOf(HorizontalAxis.getMaxGear());
            case MAIN_WAVE_ZONE_HEIGHT:
                return String.valueOf(1040);
            case SERIAL_TXT_CURRTAB:
                return String.valueOf(ISerialsWord.TYPE_S1);
            case SERIAL_TXT_SELECT + S1:
            case SERIAL_TXT_SELECT + S2:
            case SERIAL_TXT_SELECT + S3:
            case SERIAL_TXT_SELECT + S4:
                return String.valueOf(false);
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch1 + VerticalAxis.DANG_500uV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch1 + VerticalAxis.DANG_1mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch1 + VerticalAxis.DANG_2mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch1 + VerticalAxis.DANG_5mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch1 + VerticalAxis.DANG_10mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch1 + VerticalAxis.DANG_20mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch1 + VerticalAxis.DANG_50mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch1 + VerticalAxis.DANG_100mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch1 + VerticalAxis.DANG_200mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch1 + VerticalAxis.DANG_500mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch1 + VerticalAxis.DANG_1V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch1 + VerticalAxis.DANG_2V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch1 + VerticalAxis.DANG_5V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch1 + VerticalAxis.DANG_10V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch2 + VerticalAxis.DANG_500uV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch2 + VerticalAxis.DANG_1mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch2 + VerticalAxis.DANG_2mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch2 + VerticalAxis.DANG_5mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch2 + VerticalAxis.DANG_10mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch2 + VerticalAxis.DANG_20mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch2 + VerticalAxis.DANG_50mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch2 + VerticalAxis.DANG_100mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch2 + VerticalAxis.DANG_200mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch2 + VerticalAxis.DANG_500mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch2 + VerticalAxis.DANG_1V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch2 + VerticalAxis.DANG_2V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch2 + VerticalAxis.DANG_5V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch2 + VerticalAxis.DANG_10V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch3 + VerticalAxis.DANG_500uV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch3 + VerticalAxis.DANG_1mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch3 + VerticalAxis.DANG_2mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch3 + VerticalAxis.DANG_5mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch3 + VerticalAxis.DANG_10mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch3 + VerticalAxis.DANG_20mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch3 + VerticalAxis.DANG_50mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch3 + VerticalAxis.DANG_100mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch3 + VerticalAxis.DANG_200mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch3 + VerticalAxis.DANG_500mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch3 + VerticalAxis.DANG_1V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch3 + VerticalAxis.DANG_2V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch3 + VerticalAxis.DANG_5V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch3 + VerticalAxis.DANG_10V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch4 + VerticalAxis.DANG_500uV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch4 + VerticalAxis.DANG_1mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch4 + VerticalAxis.DANG_2mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch4 + VerticalAxis.DANG_5mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch4 + VerticalAxis.DANG_10mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch4 + VerticalAxis.DANG_20mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch4 + VerticalAxis.DANG_50mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch4 + VerticalAxis.DANG_100mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch4 + VerticalAxis.DANG_200mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch4 + VerticalAxis.DANG_500mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch4 + VerticalAxis.DANG_1V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch4 + VerticalAxis.DANG_2V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch4 + VerticalAxis.DANG_5V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch4 + VerticalAxis.DANG_10V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch5 + VerticalAxis.DANG_500uV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch5 + VerticalAxis.DANG_1mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch5 + VerticalAxis.DANG_2mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch5 + VerticalAxis.DANG_5mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch5 + VerticalAxis.DANG_10mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch5 + VerticalAxis.DANG_20mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch5 + VerticalAxis.DANG_50mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch5 + VerticalAxis.DANG_100mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch5 + VerticalAxis.DANG_200mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch5 + VerticalAxis.DANG_500mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch5 + VerticalAxis.DANG_1V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch5 + VerticalAxis.DANG_2V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch5 + VerticalAxis.DANG_5V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch5 + VerticalAxis.DANG_10V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch6 + VerticalAxis.DANG_500uV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch6 + VerticalAxis.DANG_1mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch6 + VerticalAxis.DANG_2mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch6 + VerticalAxis.DANG_5mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch6 + VerticalAxis.DANG_10mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch6 + VerticalAxis.DANG_20mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch6 + VerticalAxis.DANG_50mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch6 + VerticalAxis.DANG_100mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch6 + VerticalAxis.DANG_200mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch6 + VerticalAxis.DANG_500mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch6 + VerticalAxis.DANG_1V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch6 + VerticalAxis.DANG_2V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch6 + VerticalAxis.DANG_5V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch6 + VerticalAxis.DANG_10V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch7 + VerticalAxis.DANG_500uV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch7 + VerticalAxis.DANG_1mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch7 + VerticalAxis.DANG_2mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch7 + VerticalAxis.DANG_5mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch7 + VerticalAxis.DANG_10mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch7 + VerticalAxis.DANG_20mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch7 + VerticalAxis.DANG_50mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch7 + VerticalAxis.DANG_100mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch7 + VerticalAxis.DANG_200mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch7 + VerticalAxis.DANG_500mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch7 + VerticalAxis.DANG_1V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch7 + VerticalAxis.DANG_2V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch7 + VerticalAxis.DANG_5V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch7 + VerticalAxis.DANG_10V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch8 + VerticalAxis.DANG_500uV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch8 + VerticalAxis.DANG_1mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch8 + VerticalAxis.DANG_2mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch8 + VerticalAxis.DANG_5mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch8 + VerticalAxis.DANG_10mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch8 + VerticalAxis.DANG_20mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch8 + VerticalAxis.DANG_50mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch8 + VerticalAxis.DANG_100mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch8 + VerticalAxis.DANG_200mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch8 + VerticalAxis.DANG_500mV:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch8 + VerticalAxis.DANG_1V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch8 + VerticalAxis.DANG_2V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch8 + VerticalAxis.DANG_5V:
            case MAIN_WAVE_CH_Y_ZERO_POSITION + TChan.Ch8 + VerticalAxis.DANG_10V:
                return "0";
            case SAVE_TEMP_TRIGGER_INDEX:
                return "-1";
            case SAVE_TEMP_TRIGGER_IS_OPTION:
                return String.valueOf(false);
            case SAVE_TEMP_IS_CALIBRATION:
                return String.valueOf(false);
            case LAST_OBJECT_IS_CURSOR:
                return String.valueOf(false);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.Math1:
                return String.valueOf(TChan.Math1 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.Math2:
                return String.valueOf(TChan.Math2 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.Math3:
                return String.valueOf(TChan.Math3 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.Math4:
                return String.valueOf(TChan.Math4 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.Math5:
                return String.valueOf(TChan.Math5 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.Math6:
                return String.valueOf(TChan.Math6 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.Math7:
                return String.valueOf(TChan.Math7 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.Math8:
                return String.valueOf(TChan.Math8 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.R1:
                return String.valueOf(TChan.R1 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.R2:
                return String.valueOf(TChan.R2 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.R3:
                return String.valueOf(TChan.R3 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.R4:
                return String.valueOf(TChan.R4 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.R5:
                return String.valueOf(TChan.R5 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.R6:
                return String.valueOf(TChan.R6 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.R7:
                return String.valueOf(TChan.R7 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.R8:
                return String.valueOf(TChan.R8 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.S1:
                return String.valueOf(TChan.S1 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.S2:
                return String.valueOf(TChan.S2 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.S3:
                return String.valueOf(TChan.S3 - TChan.Math1);
            case RIGHT_SLIP_OTHERS_CHANNEL_ORDER + TChan.S4:
                return String.valueOf(TChan.S4 - TChan.Math1);
            default:
                return getValueFromInit1(key);
        }
    }

    /**
     * 主缓存默认值表（第二部分）
     * 处理：波形标签位置、波形调用类型、Ref时基模式、Ref数据路径、
     * 通道颜色、TValue测量数据、保存缩略图等
     * @param key 缓存键名
     * @return 默认值字符串，空字符串表示无默认值
     */
    private String getValueFromInit1(String key) {
        switch (key) {
            case MAIN_WAVE_LABEL_POSITION + TChan.Ch1:
            case MAIN_WAVE_LABEL_POSITION + TChan.Ch2:
            case MAIN_WAVE_LABEL_POSITION + TChan.Ch3:
            case MAIN_WAVE_LABEL_POSITION + TChan.Ch4:
            case MAIN_WAVE_LABEL_POSITION + TChan.Ch5:
            case MAIN_WAVE_LABEL_POSITION + TChan.Ch6:
            case MAIN_WAVE_LABEL_POSITION + TChan.Ch7:
            case MAIN_WAVE_LABEL_POSITION + TChan.Ch8:
            case MAIN_WAVE_LABEL_POSITION + TChan.Math1:
            case MAIN_WAVE_LABEL_POSITION + TChan.Math2:
            case MAIN_WAVE_LABEL_POSITION + TChan.Math3:
            case MAIN_WAVE_LABEL_POSITION + TChan.Math4:
            case MAIN_WAVE_LABEL_POSITION + TChan.Math5:
            case MAIN_WAVE_LABEL_POSITION + TChan.Math6:
            case MAIN_WAVE_LABEL_POSITION + TChan.Math7:
            case MAIN_WAVE_LABEL_POSITION + TChan.Math8:
            case MAIN_WAVE_LABEL_POSITION + TChan.R1:
            case MAIN_WAVE_LABEL_POSITION + TChan.R2:
            case MAIN_WAVE_LABEL_POSITION + TChan.R3:
            case MAIN_WAVE_LABEL_POSITION + TChan.R4:
            case MAIN_WAVE_LABEL_POSITION + TChan.R5:
            case MAIN_WAVE_LABEL_POSITION + TChan.R6:
            case MAIN_WAVE_LABEL_POSITION + TChan.R7:
            case MAIN_WAVE_LABEL_POSITION + TChan.R8:
                return String.valueOf(40);
            case TOP_SLIP_WAVE_INVOKE_TYPE:
            case TOP_SLIP_USERSET_REF_TIMEBASE:
                return String.valueOf(0);
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_WAV + TChan.R1:
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_WAV + TChan.R2:
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_WAV + TChan.R3:
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_WAV + TChan.R4:
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_WAV + TChan.R5:
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_WAV + TChan.R6:
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_WAV + TChan.R7:
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_WAV + TChan.R8:
                return SaveManage.getInstance().getDefaultPath(SaveManage.SAVE_WAVE_DEFAULT);
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_WAV + TChan.R1:
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_WAV + TChan.R2:
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_WAV + TChan.R3:
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_WAV + TChan.R4:
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_WAV + TChan.R5:
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_WAV + TChan.R6:
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_WAV + TChan.R7:
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_WAV + TChan.R8:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_WAV + TChan.R1:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_WAV + TChan.R2:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_WAV + TChan.R3:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_WAV + TChan.R4:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_WAV + TChan.R5:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_WAV + TChan.R6:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_WAV + TChan.R7:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_WAV + TChan.R8:
                return SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_WAVE_DEFAULT);
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_CSV + TChan.R1:
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_CSV + TChan.R2:
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_CSV + TChan.R3:
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_CSV + TChan.R4:
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_CSV + TChan.R5:
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_CSV + TChan.R6:
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_CSV + TChan.R7:
            case RIGHT_SLIP_REF_DATA_PATH + WAVE_TYPE_CSV + TChan.R8:
                return SaveManage.getInstance().getDefaultPath(SaveManage.SAVE_CSV_DEFAULT);
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_CSV + TChan.R1:
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_CSV + TChan.R2:
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_CSV + TChan.R3:
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_CSV + TChan.R4:
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_CSV + TChan.R5:
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_CSV + TChan.R6:
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_CSV + TChan.R7:
            case RIGHT_SLIP_REF_DATA_PATH_CURRENT + WAVE_TYPE_CSV + TChan.R8:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_CSV + TChan.R1:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_CSV + TChan.R2:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_CSV + TChan.R3:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_CSV + TChan.R4:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_CSV + TChan.R5:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_CSV + TChan.R6:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_CSV + TChan.R7:
            case RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + WAVE_TYPE_CSV + TChan.R8:
                return SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_CSV_DEFAULT);
            case RIGHT_SLIP_REF_DATA_SELECT_CURRENT + TChan.R1:
            case RIGHT_SLIP_REF_DATA_SELECT_CURRENT + TChan.R2:
            case RIGHT_SLIP_REF_DATA_SELECT_CURRENT + TChan.R3:
            case RIGHT_SLIP_REF_DATA_SELECT_CURRENT + TChan.R4:
            case RIGHT_SLIP_REF_DATA_SELECT_CURRENT + TChan.R5:
            case RIGHT_SLIP_REF_DATA_SELECT_CURRENT + TChan.R6:
            case RIGHT_SLIP_REF_DATA_SELECT_CURRENT + TChan.R7:
            case RIGHT_SLIP_REF_DATA_SELECT_CURRENT + TChan.R8:
                return String.valueOf("");
            case MAIN_CHANNEL_COLOR + TChan.R1:
                return SvgNodeInfo.getDefaultColor(TChan.R1);
            case MAIN_CHANNEL_COLOR + TChan.R2:
                return SvgNodeInfo.getDefaultColor(TChan.R2);
            case MAIN_CHANNEL_COLOR + TChan.R3:
                return SvgNodeInfo.getDefaultColor(TChan.R3);
            case MAIN_CHANNEL_COLOR + TChan.R4:
                return SvgNodeInfo.getDefaultColor(TChan.R4);
            case MAIN_CHANNEL_COLOR + TChan.R5:
                return SvgNodeInfo.getDefaultColor(TChan.R5);
            case MAIN_CHANNEL_COLOR + TChan.R6:
                return SvgNodeInfo.getDefaultColor(TChan.R6);
            case MAIN_CHANNEL_COLOR + TChan.R7:
                return SvgNodeInfo.getDefaultColor(TChan.R7);
            case MAIN_CHANNEL_COLOR + TChan.R8:
                return SvgNodeInfo.getDefaultColor(TChan.R8);
            case MAIN_CHANNEL_COLOR + TChan.Math1:
                return SvgNodeInfo.getDefaultColor(TChan.Math1);
            case MAIN_CHANNEL_COLOR + TChan.Math2:
                return SvgNodeInfo.getDefaultColor(TChan.Math2);
            case MAIN_CHANNEL_COLOR + TChan.Math3:
                return SvgNodeInfo.getDefaultColor(TChan.Math3);
            case MAIN_CHANNEL_COLOR + TChan.Math4:
                return SvgNodeInfo.getDefaultColor(TChan.Math4);
            case MAIN_CHANNEL_COLOR + TChan.Math5:
                return SvgNodeInfo.getDefaultColor(TChan.Math5);
            case MAIN_CHANNEL_COLOR + TChan.Math6:
                return SvgNodeInfo.getDefaultColor(TChan.Math6);
            case MAIN_CHANNEL_COLOR + TChan.Math7:
                return SvgNodeInfo.getDefaultColor(TChan.Math7);
            case MAIN_CHANNEL_COLOR + TChan.Math8:
                return SvgNodeInfo.getDefaultColor(TChan.Math8);
            case TOP_SLIP_MEASURE_TVALUE_X1:
            case TOP_SLIP_MEASURE_TVALUE_X2:
                return "-1";
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Ch1:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Ch2:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Ch3:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Ch4:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Ch5:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Ch6:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Ch7:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Ch8:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Math1:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Math2:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Math3:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Math4:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Math5:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Math6:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Math7:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.Math8:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.R1:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.R2:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.R3:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.R4:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.R5:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.R6:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.R7:
            case TOP_SLIP_MEASURE_TVALUE_DATA + TChan.R8:
                return "0" + CacheUtil.DELAY_SLIP + "1" + CacheUtil.DELAY_SLIP + "0";
            case TOP_SLIP_USERSET_SAVETHUMBNAIL:
                return String.valueOf(true);
        }
        return "";
    }


    /**
     * 设置最后操作对象是否为光标
     * 如果不是光标，则清除光标选中状态
     * @param isCursor true表示最后操作的是光标
     */
    public void setLastObjectIsCursor(boolean isCursor) {
        cacheUtil.putMap(LAST_OBJECT_IS_CURSOR, String.valueOf(isCursor));
        if (isCursor == false) {
            CursorManage.getInstance().setSelectCursor(0);
        }
    }

    /**
     * 获取最后操作对象是否为光标
     * @return true表示最后操作的是光标
     */
    public boolean getLastObjectIsCursor() {
        return cacheUtil.getBoolean(LAST_OBJECT_IS_CURSOR);
    }

    /**
     * 获取主参数缓存的只读引用
     * 如果缓存为空，先从持久化文件加载
     * 注意：此处只读，向map写入需要使用putMap()
     * @return 主参数缓存的HashMap引用
     */
    public HashMap<String, String> getCacheMap() {
        if (map.isEmpty()) {
            boolean b = false;
            try {
                b = SaveManage.getInstance().loadUserSet(DefaultSaveName, map);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (b) return map;
        }
        return map;
    }

    /**
     * 向主缓存写入键值对（modify=false，加载完成前不写入）
     * @param key 缓存键名
     * @param value 缓存值
     */
    public void putMap(String key, String value) {
        putMap(key, value, false);
    }

    /**
     * 向主缓存写入键值对
     * 加载完成前（modify=false）不写入，防止覆盖默认值
     * 如果修改的是串口总线类型，自动更新LASTSET_SERIALS
     * @param key 缓存键名
     * @param value 缓存值
     * @param modify true表示强制写入（即使加载未完成），false表示加载完成后才写入
     */
    public void putMap(String key, String value, boolean modify) {
        //初始化加载完成之前，不进行保存
        // if (!CacheUtil.get().isLoadParamComplete()) return;

        if (!modify && !CacheUtil.get().isLoadParamComplete()) return;
        if (!value.equals(map.get(key))) {
            if (key.equals(CacheUtil.RIGHT_SLIP_SERIALS + S1)
                    || key.equals(CacheUtil.RIGHT_SLIP_SERIALS + S2)
                    || key.equals(CacheUtil.RIGHT_SLIP_SERIALS + S3)
                    || key.equals(CacheUtil.RIGHT_SLIP_SERIALS + S4)
            ) {
                try {
                    int serials = Integer.parseInt(key.replace(CacheUtil.RIGHT_SLIP_SERIALS, ""));
                    CacheUtil.get().putMap(CacheUtil.LASTSET_SERIALS, String.valueOf(serials));
                } catch (NumberFormatException e) {
                    Logger.i("CacheUtil,putMap()key:" + key + ",value:" + value);
                    e.printStackTrace();
                }
            }

            map.put(key, value);
            isChange = true;
        }
    }

    /**
     * 在参数加载完成前向主缓存写入键值对（用于初始化/校验阶段）
     * 与putMap不同，此方法不受加载完成状态限制
     * @param key 缓存键名
     * @param value 缓存值
     */
    private void putMapBeforeLoadParam(String key, String value) {
        if (!value.equals(map.get(key))) {
            if (key.equals(CacheUtil.RIGHT_SLIP_SERIALS + S1)
                    || key.equals(CacheUtil.RIGHT_SLIP_SERIALS + S2)
                    || key.equals(CacheUtil.RIGHT_SLIP_SERIALS + S3)
                    || key.equals(CacheUtil.RIGHT_SLIP_SERIALS + S4)
            ) {
                try {
                    int serials = Integer.parseInt(key.replace(CacheUtil.RIGHT_SLIP_SERIALS, ""));
                    CacheUtil.get().putMap(CacheUtil.LASTSET_SERIALS, String.valueOf(serials));
                } catch (NumberFormatException e) {
                    Logger.i("CacheUtil,putMapBeforeLoadParam()key:" + key + ",value:" + value);
                    e.printStackTrace();
                }
            }

            map.put(key, value);
            isChange = true;
        }
    }

    /**
     * 强制向主缓存写入键值对（不受加载完成状态限制）
     * 使用key.contains匹配串口总线类型key，比精确匹配更宽泛
     * @param key 缓存键名
     * @param value 缓存值
     */
    public void putMapInForce(String key, String value) {
        if (!value.equals(map.get(key))) {
            if (key.contains(CacheUtil.RIGHT_SLIP_SERIALS)) {
                try {
                    int serials = Integer.parseInt(key.replace(CacheUtil.RIGHT_SLIP_SERIALS, ""));
                    CacheUtil.get().putMap(CacheUtil.LASTSET_SERIALS, String.valueOf(serials));
                } catch (NumberFormatException e) {
                    Logger.i("CacheUtil,putMap()key:" + key + ",value:" + value);
                }
            }

            map.put(key, value);
            isChange = true;
        }
    }

    /**
     * 批量替换主缓存内容
     * 清空现有缓存，将目标Map全部写入
     * @param targetMap 要写入的目标Map
     */
    public void putMapAll(HashMap<String, String> targetMap) {
        if (!targetMap.isEmpty()) {
            map.clear();
            map.putAll(targetMap);
        }
        isChange = true;
    }

    /**
     * 获取杂项缓存（延迟加载）
     * 如果缓存为空，先从持久化文件加载
     * @return 杂项缓存的HashMap引用
     */
    private HashMap<String, String> getOtherMap() {
        if (otherMap.isEmpty()) {
            boolean b = false;
            try {
                b = SaveManage.getInstance().loadUserSet(OtherDefaultSaveName, otherMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (b) return otherMap;
        }
        return otherMap;
    }

    /**
     * 获取主参数缓存的直接引用（不触发延迟加载）
     * @return 主参数缓存HashMap引用
     */
    public HashMap<String, String> getCurrMap() {
        return map;
    }

    /**
     * 获取杂项缓存的直接引用（不触发延迟加载）
     * @return 杂项缓存HashMap引用
     */
    public HashMap<String, String> getCurrOtherMap() {
        return otherMap;
    }

    /**
     * 杂项缓存默认值表
     * 提供用户设置名称、语言、文件名索引、校准参数、保存路径等默认值
     * @param key 缓存键名
     * @return 默认值字符串
     */
    public String getOtherMapValue(String key) {
        if (!otherMap.containsKey(key)) {
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
            String str_time = sdf.format(date);
            switch (key) {
                case USERSET + 0:
                    otherMap.put(key, USERSET_DEFAULTNAME + 0);
                    break;
                case USERSET + 1:
                    otherMap.put(key, USERSET_DEFAULTNAME + 1);
                    break;
                case USERSET + 2:
                    otherMap.put(key, USERSET_DEFAULTNAME + 2);
                    break;
                case USERSET + 3:
                    otherMap.put(key, USERSET_DEFAULTNAME + 3);
                    break;
                case USERSET + 4:
                    otherMap.put(key, USERSET_DEFAULTNAME + 4);
                    break;
                case USERSET + 5:
                    otherMap.put(key, USERSET_DEFAULTNAME + 5);
                    break;
                case USERSET + 6:
                    otherMap.put(key, USERSET_DEFAULTNAME + 6);
                    break;
                case USERSET + 7:
                    otherMap.put(key, USERSET_DEFAULTNAME + 7);
                    break;
                case USERSET + 8:
                    otherMap.put(key, USERSET_DEFAULTNAME + 8);
                    break;
                case USERSET + 9:
                    otherMap.put(key, USERSET_DEFAULTNAME + 9);
                    break;
                case LANGUAGE:
                    otherMap.put(key, "en_US");
                    break;
                case GENNAME_INDEXDATE_WAV:
                    otherMap.put(key, str_time);
                    break;
                case GENNAME_INDEXDATE_CSV:
                    otherMap.put(key, str_time);
                    break;
                case GENNAME_INDEXDATE_BIN:
                    otherMap.put(key, str_time);
                    break;
                case GENNAME_INDEXDATE_SBT:
                    otherMap.put(key, str_time);
                    break;
                case GENNAME_INDEX_WAV:
                    otherMap.put(key, GENNAME_INDEX_FIRST);
                    break;
                case GENNAME_INDEX_CSV:
                    otherMap.put(key, GENNAME_INDEX_FIRST);
                    break;
                case GENNAME_INDEX_BIN:
                    otherMap.put(key, GENNAME_INDEX_FIRST);
                    break;
                case GENNAME_INDEX_SBT:
                    otherMap.put(key, GENNAME_INDEX_FIRST);
                    break;
                case MAIN_BOTTOM_USB_PATH:
                    otherMap.put(key, "");
                    break;
                case CALIBRATION_TOP_ZERO:
                    otherMap.put(key, String.valueOf(false));
                    break;
                case CALIBRATION_TOP_ADGAIN:
                    otherMap.put(key, String.valueOf(false));
                    break;
                case CALIBRATION_TOP_ADZERO:
                    otherMap.put(key, String.valueOf(false));
                    break;
                case CALIBRATION_BOTTOM_ZERO:
                    otherMap.put(key, String.valueOf(false));
                    break;
                case CALIBRATION_BOTTOM_OFFSET:
                    otherMap.put(key, String.valueOf(false));
                    break;
                case CALIBRATION_BOTTOM_CHDIFF:
                    otherMap.put(key, String.valueOf(false));
                    break;
                case CALIBRATION_CENTER_CH1GAIN + VerticalAxis.DANG_500uV:
                case CALIBRATION_CENTER_CH1GAIN + VerticalAxis.DANG_1mV:
                case CALIBRATION_CENTER_CH1GAIN + VerticalAxis.DANG_2mV:
                case CALIBRATION_CENTER_CH1GAIN + VerticalAxis.DANG_5mV:
                case CALIBRATION_CENTER_CH1GAIN + VerticalAxis.DANG_10mV:
                case CALIBRATION_CENTER_CH1GAIN + VerticalAxis.DANG_20mV:
                case CALIBRATION_CENTER_CH1GAIN + VerticalAxis.DANG_50mV:
                case CALIBRATION_CENTER_CH1GAIN + VerticalAxis.DANG_100mV:
                case CALIBRATION_CENTER_CH1GAIN + VerticalAxis.DANG_200mV:
                case CALIBRATION_CENTER_CH1GAIN + VerticalAxis.DANG_500mV:
                case CALIBRATION_CENTER_CH1GAIN + VerticalAxis.DANG_1V:
                case CALIBRATION_CENTER_CH1GAIN + VerticalAxis.DANG_2V:
                case CALIBRATION_CENTER_CH1GAIN + VerticalAxis.DANG_5V:
                case CALIBRATION_CENTER_CH1GAIN + VerticalAxis.DANG_10V:
                case CALIBRATION_CENTER_CH2GAIN + VerticalAxis.DANG_500uV:
                case CALIBRATION_CENTER_CH2GAIN + VerticalAxis.DANG_1mV:
                case CALIBRATION_CENTER_CH2GAIN + VerticalAxis.DANG_2mV:
                case CALIBRATION_CENTER_CH2GAIN + VerticalAxis.DANG_5mV:
                case CALIBRATION_CENTER_CH2GAIN + VerticalAxis.DANG_10mV:
                case CALIBRATION_CENTER_CH2GAIN + VerticalAxis.DANG_20mV:
                case CALIBRATION_CENTER_CH2GAIN + VerticalAxis.DANG_50mV:
                case CALIBRATION_CENTER_CH2GAIN + VerticalAxis.DANG_100mV:
                case CALIBRATION_CENTER_CH2GAIN + VerticalAxis.DANG_200mV:
                case CALIBRATION_CENTER_CH2GAIN + VerticalAxis.DANG_500mV:
                case CALIBRATION_CENTER_CH2GAIN + VerticalAxis.DANG_1V:
                case CALIBRATION_CENTER_CH2GAIN + VerticalAxis.DANG_2V:
                case CALIBRATION_CENTER_CH2GAIN + VerticalAxis.DANG_5V:
                case CALIBRATION_CENTER_CH2GAIN + VerticalAxis.DANG_10V:
                case CALIBRATION_CENTER_CH3GAIN + VerticalAxis.DANG_500uV:
                case CALIBRATION_CENTER_CH3GAIN + VerticalAxis.DANG_1mV:
                case CALIBRATION_CENTER_CH3GAIN + VerticalAxis.DANG_2mV:
                case CALIBRATION_CENTER_CH3GAIN + VerticalAxis.DANG_5mV:
                case CALIBRATION_CENTER_CH3GAIN + VerticalAxis.DANG_10mV:
                case CALIBRATION_CENTER_CH3GAIN + VerticalAxis.DANG_20mV:
                case CALIBRATION_CENTER_CH3GAIN + VerticalAxis.DANG_50mV:
                case CALIBRATION_CENTER_CH3GAIN + VerticalAxis.DANG_100mV:
                case CALIBRATION_CENTER_CH3GAIN + VerticalAxis.DANG_200mV:
                case CALIBRATION_CENTER_CH3GAIN + VerticalAxis.DANG_500mV:
                case CALIBRATION_CENTER_CH3GAIN + VerticalAxis.DANG_1V:
                case CALIBRATION_CENTER_CH3GAIN + VerticalAxis.DANG_2V:
                case CALIBRATION_CENTER_CH3GAIN + VerticalAxis.DANG_5V:
                case CALIBRATION_CENTER_CH3GAIN + VerticalAxis.DANG_10V:
                case CALIBRATION_CENTER_CH4GAIN + VerticalAxis.DANG_500uV:
                case CALIBRATION_CENTER_CH4GAIN + VerticalAxis.DANG_1mV:
                case CALIBRATION_CENTER_CH4GAIN + VerticalAxis.DANG_2mV:
                case CALIBRATION_CENTER_CH4GAIN + VerticalAxis.DANG_5mV:
                case CALIBRATION_CENTER_CH4GAIN + VerticalAxis.DANG_10mV:
                case CALIBRATION_CENTER_CH4GAIN + VerticalAxis.DANG_20mV:
                case CALIBRATION_CENTER_CH4GAIN + VerticalAxis.DANG_50mV:
                case CALIBRATION_CENTER_CH4GAIN + VerticalAxis.DANG_100mV:
                case CALIBRATION_CENTER_CH4GAIN + VerticalAxis.DANG_200mV:
                case CALIBRATION_CENTER_CH4GAIN + VerticalAxis.DANG_500mV:
                case CALIBRATION_CENTER_CH4GAIN + VerticalAxis.DANG_1V:
                case CALIBRATION_CENTER_CH4GAIN + VerticalAxis.DANG_2V:
                case CALIBRATION_CENTER_CH4GAIN + VerticalAxis.DANG_5V:
                case CALIBRATION_CENTER_CH4GAIN + VerticalAxis.DANG_10V:
                    otherMap.put(key, String.valueOf(false));
                    break;
                case TOP_SLIP_SAVE_SESSION_SUFFIX_CHECK:
                case TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK + WAVE_TYPE_WAV:
                case TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK + WAVE_TYPE_CSV:
                case TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK + WAVE_TYPE_BIN:
                case TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK:
                case TOP_SLIP_SAVE_PICTURE_SUFFIX_CHECK:
                case TOP_SLIP_AUTO_SAVE_SUFFIX_CHECK:
                    otherMap.put(key, String.valueOf(true));
                    break;
                case TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + WAVE_TYPE_WAV:
                case TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + WAVE_TYPE_CSV:
                case TOP_SLIP_SAVE_WAVE_SUFFIX_CHECK_NUM + WAVE_TYPE_BIN:
                case TOP_SLIP_SAVE_SESSION_SUFFIX_CHECK_NUM:
                case TOP_SLIP_SAVE_SETTING_SUFFIX_CHECK_NUM:
                case TOP_SLIP_SAVE_PICTURE_SUFFIX_CHECK_NUM:
                case TOP_SLIP_AUTO_SAVE_SUFFIX_CHECK_NUM:
                    otherMap.put(key, "000");
                    break;
                case TOP_SLIP_SAVE_WAVE_NAME + WAVE_TYPE_WAV:
                case TOP_SLIP_SAVE_WAVE_NAME + WAVE_TYPE_CSV:
                case TOP_SLIP_SAVE_WAVE_NAME + WAVE_TYPE_BIN:
                case TOP_SLIP_SAVE_SESSION_NAME:
                case TOP_SLIP_SAVE_SETTING_NAME:
                case TOP_SLIP_SAVE_PICTURE_NAME:
                case TOP_SLIP_AUTO_SAVE_NAME:
                    otherMap.put(key, "");
                    break;
                case TOP_SLIP_SAVE_WAVE_PATH + WAVE_TYPE_WAV:
                    otherMap.put(key, SaveManage.getInstance().getDefaultPath(SaveManage.SAVE_WAVE_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_WAVE_PATH_CURRENT + WAVE_TYPE_WAV:
                    otherMap.put(key, SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_WAVE_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_WAVE_ABSOLUTE_PATH + WAVE_TYPE_WAV:
                    otherMap.put(key, SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_WAVE_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_WAVE_PATH + WAVE_TYPE_CSV:
                    otherMap.put(key, SaveManage.getInstance().getDefaultPath(SaveManage.SAVE_CSV_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_WAVE_PATH_CURRENT + WAVE_TYPE_CSV:
                    otherMap.put(key, SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_CSV_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_WAVE_ABSOLUTE_PATH + WAVE_TYPE_CSV:
                    otherMap.put(key, SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_CSV_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_WAVE_PATH + WAVE_TYPE_BIN:
                    otherMap.put(key, SaveManage.getInstance().getDefaultPath(SaveManage.SAVE_BIN_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_WAVE_PATH_CURRENT + WAVE_TYPE_BIN:
                    otherMap.put(key, SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_BIN_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_WAVE_ABSOLUTE_PATH + WAVE_TYPE_BIN:
                    otherMap.put(key, SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_BIN_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_SETTING_PATH:
                    otherMap.put(key, SaveManage.getInstance().getDefaultPath(SaveManage.SAVE_SETTING_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_SETTING_PATH_CURRENT:
                    otherMap.put(key, SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_SETTING_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_SETTING_ABSOLUTE_PATH:
                    otherMap.put(key, SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_SETTING_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_SESSION_PATH:
                    otherMap.put(key, SaveManage.getInstance().getDefaultPath(SaveManage.SAVE_SESSION_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_SESSION_PATH_CURRENT:
                    otherMap.put(key, SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_SESSION_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_SESSION_ABSOLUTE_PATH:
                    otherMap.put(key, SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_SESSION_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_PICTURE_PATH:
                    otherMap.put(key, SaveManage.getInstance().getDefaultPath(SaveManage.SAVE_PICTURE_DEFAULT));
                    break;
                case TOP_SLIP_SAVE_PICTURE_PATH_CURRENT:
                case TOP_SLIP_SAVE_PICTURE_ABSOLUTE_PATH:
                    otherMap.put(key, SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_PICTURE_DEFAULT));
                    break;
                case TOP_SLIP_AUTO_SAVE_PATH:
                    otherMap.put(key, SaveManage.getInstance().getDefaultPath(SaveManage.SAVE_AUTOSAVE_DEFAULT));
                    break;
                case TOP_SLIP_AUTO_SAVE_PATH_CURRENT:
                    otherMap.put(key, SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_AUTOSAVE_DEFAULT));
                case TOP_SLIP_AUTO_SAVE_ABSOLUTE_PATH:
                    otherMap.put(key, SaveManage.getInstance().getAbDefaultPath(SaveManage.SAVE_AUTOSAVE_DEFAULT));
                    break;
                case TOP_SLIP_WAVE_FILE_PATH:
                case TOP_SLIP_WAVE_FILE_PATH + WAVE_TYPE_WAV:
                case TOP_SLIP_WAVE_FILE_PATH + WAVE_TYPE_CSV:
                case TOP_SLIP_WAVE_FILE_PATH + WAVE_TYPE_BIN:
                case TOP_SLIP_WAVE_FILE_ABSOLUTE_PATH + WAVE_TYPE_WAV:
                case TOP_SLIP_WAVE_FILE_ABSOLUTE_PATH + WAVE_TYPE_CSV:
                case TOP_SLIP_WAVE_FILE_ABSOLUTE_PATH + WAVE_TYPE_BIN:
                case TOP_SLIP_WAVE_FILE_PATH_CURRENT:
                case TOP_SLIP_WAVE_FILE_PATH_CURRENT + WAVE_TYPE_WAV:
                case TOP_SLIP_WAVE_FILE_PATH_CURRENT + WAVE_TYPE_CSV:
                case TOP_SLIP_WAVE_FILE_PATH_CURRENT + WAVE_TYPE_BIN:
                case TOP_SLIP_SETTING_FILE_PATH:
                case TOP_SLIP_SETTING_FILE_ABSOLUTE_PATH:
                case TOP_SLIP_SETTING_FILE_PATH_CURRENT:
                case TOP_SLIP_SESSION_FILE_PATH:
                case TOP_SLIP_SESSION_FILE_ABSOLUTE_PATH:
                case TOP_SLIP_SESSION_FILE_PATH_CURRENT:
                case TOP_SLIP_SAVE:
                case TOP_SLIP_SAVE_STORE:
                case TOP_SLIP_INVOKE_STORE:
                    otherMap.put(key, String.valueOf(0));
                    break;
                case TOP_SLIP_INVOKE_PICTURE_FILE_FILTER:
                case TOP_SLIP_INVOKE_SETTING_FILE_FILTER:
                case TOP_SLIP_INVOKE_SESSION_FILE_FILTER:
                case TOP_SLIP_INVOKE_WAVE_FILE_FILTER + WAVE_TYPE_WAV:
                case TOP_SLIP_INVOKE_WAVE_FILE_FILTER + WAVE_TYPE_CSV:
                case TOP_SLIP_INVOKE_WAVE_FILE_FILTER + WAVE_TYPE_BIN:
                    otherMap.put(key, String.valueOf(false));
                    break;
                case TOP_SLIP_SPINNER_ITEM_INDEX:
                    otherMap.put(key, String.valueOf(0));
                    break;
            }
        }
        return otherMap.get(key);
    }

    /**
     * 向杂项缓存写入键值对（不立即持久化）
     * 空值将被忽略
     * @param key 缓存键名
     * @param value 缓存值
     */
    public void putOtherMap(String key, String value) {
        if (StrUtil.isEmpty(value)) return;
        if (!value.equals(otherMap.get(key))) {
            otherMap.put(key, value);
        }
        isChange = true;
    }

    /**
     * 向杂项缓存写入键值对并立即持久化到文件
     * 空值将被忽略
     * @param key 缓存键名
     * @param value 缓存值
     */
    public void putOtherMapAndSave(String key, String value) {
        if (StrUtil.isEmpty(value)) return;
        if (!value.equals(otherMap.get(key))) {
            otherMap.put(key, value);
            try {
                SaveManage.getInstance().saveUserSet(OtherDefaultSaveName, otherMap, null);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isChange = true;
        }
    }

    /**
     * 批量替换杂项缓存内容
     * @param targetMap 要写入的目标Map
     */
    public void putOtherMapAll(HashMap<String, String> targetMap) {
        if (!targetMap.isEmpty()) {
            otherMap.clear();
            otherMap.putAll(targetMap);
            isChange = true;
        }
    }

    //endregion

    //region 缓存机制
    private boolean isChange = false; // 缓存变更标记，true表示有未持久化的变更
    private int time = 1000 * 60 * 60 * 24; // 定时持久化间隔（24小时，当前未启用定时机制）
    //private static Handler handler = new Handler();
//    private Thread thread;

    /**
     * 初始化缓存机制（当前为空实现，原定时持久化线程已注释）
     */
    private void initCache() {
//        thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Thread.currentThread().setName("CacheUtil");
//                while (true) {
//                    try {
//                        Thread.sleep(time);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    putCache();
//                }
//            }
//        });
//        thread.start();
    }

    /**
     * 暂停缓存机制，立即执行一次持久化
     * 通常在应用退出或进入后台时调用
     */
    public void pause() {
        putCache();
    }

    /**
     * 执行缓存持久化
     * 只有isChange为true时才执行，将主缓存和杂项缓存写入文件
     */
    private void putCache() {
        if (isChange) {
            Logger.i("putCache start");
            long startTime = System.currentTimeMillis();
            SaveManage.getInstance().saveToDefaultSaveName();
            long putDefaultEndTime = System.currentTimeMillis();
            SaveManage.getInstance().saveToOtherSaveName();
            long putCacheEndTime = System.currentTimeMillis();

//            Logger.i("putDefaultCache time:" + (putDefaultEndTime - startTime));
//            Logger.i("putOtherCache time:" + (putCacheEndTime - putDefaultEndTime));
//            Logger.i("putCache time:" + (putCacheEndTime - startTime));
            isChange = false;
        }
    }
    //endregion

    //region  加载状态标记 —— 跟踪各UI模块的参数加载完成情况
    public static final String LOAD_Command = "Command"; // 命令模块加载标记
    public static final String LOAD_ExternalKeysOnBindService = "ExternalKeysOnBindService"; // 外部按键服务加载标记
    public static final String LOAD_MainHolderBottom = "MainHolderBottom"; // 底部菜单加载标记
    public static final String LOAD_MainHolderBottomQuick = "MainHolderBottomQuick"; // 底部快捷菜单加载标记
    public static final String LOAD_MainHolderLeftMenu = "MainHolderLeftMenu"; // 左侧菜单加载标记
    public static final String LOAD_MainHolderRightChannels = "MainHolderRightChannels"; // 右侧通道菜单加载标记
    public static final String LOAD_MainHolderRightOthers = "MainHolderRightOthers"; // 右侧其他菜单加载标记
    public static final String LOAD_MainLayoutCenterChannel = "MainLayoutCenterChannel"; // 中心通道布局加载标记
    public static final String LOAD_RightLayoutChannel = "RightLayoutChannel"; // 通道布局加载标记
    public static final String LOAD_RightLayoutMath = "RightLayoutMath"; // Math布局加载标记
    public static final String LOAD_RightLayoutRef = "RightLayoutRef"; // Ref布局加载标记
    public static final String LOAD_RightLayoutSerials = "RightLayoutSerials"; // 串口布局加载标记
    public static final String LOAD_RightLayoutSerialsCan = "RightLayoutSerialsCan"; // CAN布局加载标记
    public static final String LOAD_RightLayoutSerialsI2c = "RightLayoutSerialsI2c"; // I2C布局加载标记
    public static final String LOAD_RightLayoutSerialsLin = "RightLayoutSerialsLin"; // LIN布局加载标记
    public static final String LOAD_RightLayoutSerialsM1553B = "RightLayoutSerialsM1553B"; // M1553B布局加载标记
    public static final String LOAD_RightLayoutSerialsM429 = "RightLayoutSerialsM429"; // M429布局加载标记
    public static final String LOAD_RightLayoutSerialsSpi = "RightLayoutSerialsSpi"; // SPI布局加载标记
    public static final String LOAD_RightLayoutSerialsUart = "RightLayoutSerialsUart"; // UART布局加载标记
    public static final String LOAD_TMessage = "TMessage"; // T消息加载标记
    public static final String LOAD_TopLayoutAuto = "TopLayoutAuto"; // 自动设置布局加载标记
    public static final String LOAD_TopLayoutAutoRange = "TopLayoutAutoRange"; // 自动范围布局加载标记
    public static final String LOAD_TopLayoutAutoSet = "TopLayoutAutoSet"; // 自动设置详细布局加载标记
    public static final String LOAD_TopLayoutDisplay = "TopLayoutDisplay"; // 显示布局加载标记
    public static final String LOAD_TopLayoutDisplayCommon = "TopLayoutDisplayCommon"; // 显示通用布局加载标记
    public static final String LOAD_TopLayoutDisplayGraticule = "TopLayoutDisplayGraticule"; // 网格显示布局加载标记
    public static final String LOAD_TopLayoutDisplayPersist = "TopLayoutDisplayPersist"; // 持久化显示布局加载标记
    public static final String LOAD_TopLayoutDisplayWaveform = "TopLayoutDisplayWaveform"; // 波形显示布局加载标记
    public static final String LOAD_TopLayoutFactoryCalibration = "TopLayoutFactoryCalibration"; // 出厂校准布局加载标记
    public static final String LOAD_TopLayoutFrequencyMeter = "TopLayoutFrequencyMeter"; // 频率计布局加载标记
    public static final String LOAD_TopLayoutMeasure = "TopLayoutMeasure"; // 测量布局加载标记
    public static final String LOAD_TopLayoutMeasureCommon = "TopLayoutMeasureCommon"; // 测量通用布局加载标记
    public static final String LOAD_TopLayoutPopWindow = "TopLayoutPopWindow"; // 弹窗布局加载标记
    public static final String LOAD_TopLayoutSave = "TopLayoutSave"; // 保存布局加载标记
    public static final String LOAD_TopLayoutSaveWave = "TopLayoutSaveWave"; // 保存波形布局加载标记
    public static final String LOAD_TopLayoutSample = "TopLayoutUserset"; // 采样/用户设置布局加载标记
    public static final String LOAD_TopLayoutSampleMode = "TopLayoutUsersetMode"; // 采样模式布局加载标记
    public static final String LOAD_TopLayoutSampleDepth = "TopLayoutUsersetDepth"; // 存储深度布局加载标记
    public static final String LOAD_TopLayoutSampleSegmented = "TopLayoutUsersetSegmented"; // 分段采集布局加载标记
    public static final String LOAD_TopLayoutTrigger = "TopLayoutTrigger"; // 触发布局加载标记
    public static final String LOAD_TopLayoutTriggerCommon = "TopLayoutTriggerCommon"; // 触发通用布局加载标记
    public static final String LOAD_TopLayoutTriggerEdge = "TopLayoutTriggerEdge"; // 边沿触发布局加载标记
    public static final String LOAD_TopLayoutTriggerLogic = "TopLayoutTriggerLogic"; // 逻辑触发布局加载标记
    public static final String LOAD_TopLayoutTriggerNEdge = "TopLayoutTriggerNEdge"; // N边沿触发布局加载标记
    public static final String LOAD_TopLayoutTriggerPulsewidth = "TopLayoutTriggerPulsewidth"; // 脉宽触发布局加载标记
    public static final String LOAD_TopLayoutTriggerRunt = "TopLayoutTriggerRunt"; // 矮脉冲触发布局加载标记
    public static final String LOAD_TopLayoutTriggerSerials = "TopLayoutTriggerSerials"; // 串口触发布局加载标记
    public static final String LOAD_TopLayoutTriggerSerialsBaseDetail = "TopLayoutTriggerSerialsBaseDetail"; // 串口触发详细布局加载标记
    public static final String LOAD_TopLayoutTriggerSlope = "TopLayoutTriggerSlope"; // 斜率触发布局加载标记
    public static final String LOAD_TopLayoutTriggerTimeout = "TopLayoutTriggerTimeout"; // 超时触发布局加载标记
    public static final String LOAD_TopLayoutTriggerVideo = "TopLayoutTriggerVideo"; // 视频触发布局加载标记
    public static final String LOAD_TopLayoutUserset = "TopLayoutUserset"; // 用户设置布局加载标记
    public static final String LOAD_TopLayoutUsersetDepth = "TopLayoutUsersetDepth"; // 用户设置深度布局加载标记
    public static final String LOAD_TopLayoutUsersetSaveRecovery = "TopLayoutUsersetSaveRecovery"; // 保存恢复布局加载标记
    public static final String LOAD_WaveManage = "WaveManage"; // 波形管理加载标记
    public static final String LOAD_WaveZoneDisplayManage = "WaveZoneDisplayManage"; // 波形区域显示管理加载标记

    //private Map<String, Boolean> mapLoadProcess = new HashMap<>();
//    private CopyOnWriteArrayList map=new CopyOnWriteArrayList();
    /** 各UI模块的参数加载状态跟踪表，key为LOAD_*常量，value为是否加载完成；使用ConcurrentHashMap保证多线程安全 */
    private ConcurrentHashMap<String, Boolean> mapLoadProcess = new ConcurrentHashMap<>();
    /** 全局参数加载完成标志，volatile保证多线程可见性；所有UI模块参数加载完毕后置为true */
    private volatile boolean loadComplete = false;
    /** 是否点击了恢复出厂设置按钮，true表示用户刚执行了Factory Reset操作 */
    private boolean clickFactoryReset = false;

    /**
     * 设置全局参数加载完成标志
     * @param loadComplete true表示所有参数加载完成，false表示尚未完成
     */
    public void setLoadComplete(boolean loadComplete) {
        this.loadComplete = loadComplete; // 设置加载完成标志
    }

    /**
     * 判断全局参数是否加载完成
     * @return true表示所有参数已加载完成，false表示尚未完成
     */
    public boolean isLoadComplete() {
        return loadComplete; // 返回加载完成标志
    }

    /**
     * 判断是否点击了恢复出厂设置按钮
     * @return true表示用户刚执行了Factory Reset操作，false表示未执行
     */
    public boolean isClickFactoryReset() {
        return clickFactoryReset; // 返回出厂设置点击标记
    }

    /**
     * 设置恢复出厂设置按钮的点击状态
     * @param clickFactoryReset true表示刚点击了Factory Reset，false表示重置标记
     */
    public void setClickFactoryReset(boolean clickFactoryReset) {
        this.clickFactoryReset = clickFactoryReset; // 设置出厂设置点击标记
    }

    /**
     * 初始化各UI模块的参数加载状态标志位
     * 将所有活跃的UI模块标记为"未加载"（false），被注释掉的模块表示已废弃或合并到其他模块中。
     * 各UI模块在完成自身参数加载后，通过setLoadMenuState()将对应标志位置为true。
     * isLoadParamComplete()会遍历此表检查是否所有模块都加载完成。
     */
    public void initStateCacheLoad() {
        mapLoadProcess.put(LOAD_Command, false); // 命令模块加载标记
        mapLoadProcess.put(LOAD_ExternalKeysOnBindService, false); // 外部按键绑定服务加载标记
        mapLoadProcess.put(LOAD_MainHolderBottom, false); // 主界面底部栏加载标记
        mapLoadProcess.put(LOAD_MainHolderBottomQuick, false); // 主界面底部快捷栏加载标记
        mapLoadProcess.put(LOAD_MainHolderLeftMenu, false); // 主界面左侧菜单加载标记
        mapLoadProcess.put(LOAD_MainHolderRightChannels, false); // 主界面右侧通道区加载标记
        mapLoadProcess.put(LOAD_MainHolderRightOthers, false); // 主界面右侧其他区加载标记
        mapLoadProcess.put(LOAD_MainLayoutCenterChannel, false); // 主界面中心通道加载标记
        mapLoadProcess.put(LOAD_RightLayoutChannel, false); // 右滑通道布局加载标记
        mapLoadProcess.put(LOAD_RightLayoutMath, false); // 右滑Math布局加载标记
        mapLoadProcess.put(LOAD_RightLayoutRef, false); // 右滑Ref布局加载标记
//        mapLoadProcess.put(LOAD_RightLayoutSerials, false); // 已废弃：串口总布局
//        mapLoadProcess.put(LOAD_RightLayoutSerialsCan, false); // 已废弃：CAN串口布局
//        mapLoadProcess.put(LOAD_RightLayoutSerialsI2c, false); // 已废弃：I2C串口布局
//        mapLoadProcess.put(LOAD_RightLayoutSerialsLin, false); // 已废弃：LIN串口布局
//        mapLoadProcess.put(LOAD_RightLayoutSerialsM1553B, false); // 已废弃：M1553B串口布局
//        mapLoadProcess.put(LOAD_RightLayoutSerialsM429, false); // 已废弃：M429串口布局
//        mapLoadProcess.put(LOAD_RightLayoutSerialsSpi, false); // 已废弃：SPI串口布局
//        mapLoadProcess.put(LOAD_RightLayoutSerialsUart, false); // 已废弃：UART串口布局
        //mapLoadProcess.put(LOAD_TMessage, false); // 已废弃：T消息布局
        mapLoadProcess.put(LOAD_TopLayoutAuto, false); // 顶部自动菜单加载标记
        mapLoadProcess.put(LOAD_TopLayoutAutoRange, false); // 顶部自动范围菜单加载标记
        mapLoadProcess.put(LOAD_TopLayoutAutoSet, false); // 顶部自动设置菜单加载标记
        mapLoadProcess.put(LOAD_TopLayoutSample, false); // 顶部采样菜单加载标记
        mapLoadProcess.put(LOAD_TopLayoutSampleMode, false); // 顶部采样模式加载标记
        mapLoadProcess.put(LOAD_TopLayoutSampleDepth, false); // 顶部采样深度加载标记
//        mapLoadProcess.put(LOAD_TopLayoutSampleSegmented, false); // 已废弃：分段采样
        mapLoadProcess.put(LOAD_TopLayoutDisplay, false); // 顶部显示菜单加载标记
        mapLoadProcess.put(LOAD_TopLayoutDisplayCommon, false); // 顶部显示通用设置加载标记
        mapLoadProcess.put(LOAD_TopLayoutDisplayGraticule, false); // 顶部显示网格设置加载标记
        mapLoadProcess.put(LOAD_TopLayoutDisplayPersist, false); // 顶部显示余辉设置加载标记
        mapLoadProcess.put(LOAD_TopLayoutDisplayWaveform, false); // 顶部显示波形设置加载标记
//        mapLoadProcess.put(LOAD_TopLayoutFactoryCalibration, false); // 已废弃：出厂校准
        mapLoadProcess.put(LOAD_TopLayoutFrequencyMeter, false); // 顶部频率计加载标记
        mapLoadProcess.put(LOAD_TopLayoutMeasure, false); // 顶部测量菜单加载标记
        mapLoadProcess.put(LOAD_TopLayoutMeasureCommon, false); // 顶部测量通用设置加载标记
        mapLoadProcess.put(LOAD_TopLayoutPopWindow, false); // 顶部弹出窗口加载标记
        mapLoadProcess.put(LOAD_TopLayoutSave, false); // 顶部保存菜单加载标记
        mapLoadProcess.put(LOAD_TopLayoutSaveWave, false); // 顶部保存波形加载标记
//        mapLoadProcess.put(LOAD_TopLayoutTrigger, false); // 已废弃：触发总布局
//        mapLoadProcess.put(LOAD_TopLayoutTriggerCommon, false); // 已废弃：触发通用
//        mapLoadProcess.put(LOAD_TopLayoutTriggerEdge, false); // 已废弃：边沿触发
//        mapLoadProcess.put(LOAD_TopLayoutTriggerLogic, false); // 已废弃：逻辑触发
//        mapLoadProcess.put(LOAD_TopLayoutTriggerNEdge, false); // 已废弃：N边沿触发
//        mapLoadProcess.put(LOAD_TopLayoutTriggerPulsewidth, false); // 已废弃：脉宽触发
//        mapLoadProcess.put(LOAD_TopLayoutTriggerRunt, false); // 已废弃：矮脉冲触发
//        mapLoadProcess.put(LOAD_TopLayoutTriggerSerials, false); // 已废弃：串口触发
//        mapLoadProcess.put(LOAD_TopLayoutTriggerSerialsBaseDetail, false); // 已废弃：串口触发详情
//        mapLoadProcess.put(LOAD_TopLayoutTriggerSlope, false); // 已废弃：斜率触发
//        mapLoadProcess.put(LOAD_TopLayoutTriggerTimeout, false); // 已废弃：超时触发
//        mapLoadProcess.put(LOAD_TopLayoutTriggerVideo, false); // 已废弃：视频触发
        mapLoadProcess.put(LOAD_TopLayoutUserset, false); // 顶部用户设置加载标记
        mapLoadProcess.put(LOAD_TopLayoutUsersetDepth, false); // 顶部用户设置深度加载标记
        // mapLoadProcess.put(LOAD_TopLayoutUsersetSaveRecovery, false); // 已废弃：保存恢复
        mapLoadProcess.put(LOAD_WaveManage, false); // 波形管理加载标记
        mapLoadProcess.put(LOAD_WaveZoneDisplayManage, false); // 波形区域显示管理加载标记
    }

    /**
     * 设置指定UI模块的参数加载完成状态
     * 各UI模块在完成自身参数加载后调用此方法，将对应标志位置为true
     * @param key 模块标识，对应LOAD_*常量
     * @param isComplete true表示该模块参数已加载完成，false表示未完成
     */
    public void setLoadMenuState(String key, boolean isComplete) {
        mapLoadProcess.put(key, isComplete); // 更新指定模块的加载状态
    }

    /**
     * 判断可见的基础界面模块是否加载完成
     * 当前实现始终返回false，此方法为预留接口，未来可用于判断核心UI模块是否就绪
     * @return true表示基础模块加载完成（当前始终返回false）
     */
    public boolean isLoadCompleteBaseModule() {
        return false; // 预留接口，当前始终返回false
    }

    /**
     * 判断所有UI模块的参数是否全部加载完成
     * 遍历mapLoadProcess中所有模块的加载状态，只要有一个未完成就返回false。
     * 当所有模块都加载完成时，将loadComplete标志置为true并返回true。
     * 方法使用synchronized修饰，保证多线程调用时的原子性。
     * @return true表示所有模块参数已加载完成，false表示至少有一个模块未完成
     */
    public synchronized boolean isLoadParamComplete() {
        for (Iterator<String> iterator = mapLoadProcess.keySet().iterator(); iterator.hasNext(); ) { // 遍历所有模块
            String key = iterator.next(); // 获取模块标识
            if (mapLoadProcess.get(key) == false) { // 检查该模块是否未加载完成
                Logger.d(TAG, "isLoadParamComplete:" + key); // 打印未完成的模块名
                return false; // 有模块未完成，返回false
            }
        }
        loadComplete = true; // 所有模块加载完成，设置全局标志
        return true; // 全部加载完成
    }

    //endregion

    /**
     * 应用启动初始化
     * 将运行/停止状态设置为"运行"（true），确保示波器启动时处于运行状态
     */
    public void startUp() {
        CacheUtil.get().putMap(CacheUtil.MAIN_LEFT_RUNSTOP, String.valueOf(true), true); // 强制设置为运行状态
    }

    //region 检验参数合法性

    /**
     * 校验缓存参数的合法性
     * 在加载持久化参数后调用，检查并修正不合法的参数状态：
     * 1. 检查REF文件是否存在，不存在的Ref通道将被关闭
     * 2. 设置存储深度
     * 3. 设置当前激活通道
     * 4. 初始化水平轴参数（时基档位）
     * 5. 检查滚屏模式与Zoom模式的冲突
     */
    public void checkCacheParam() {
        checkLoadRefFile(); // 检查REF文件合法性
        MemDepthFactory.getMemDepth().setMemDepthItem(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_DEPTH)); // 设置存储深度

        //设置当前通道
        ChannelFactory.chActivate(CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT)); // 激活当前选中通道
        //设置档位的初始值
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance(); // 获取水平轴单例
        horizontalAxis.initXAxis(); // 初始化X轴
        double normalScale = TBookUtil.getSFromTime(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE)); // 读取普通时基档位值
        horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD, horizontalAxis.timeValtoTimeScaleId(normalScale)); // 设置普通视图时基
        double zoomLargeScale = TBookUtil.getSFromTime(CacheUtil.get().getString(CacheUtil.ZOOM_BOTTOM_TIMEBASE_LARGE_SCALE)); // 读取Zoom时基档位值
        horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_LARGE, horizontalAxis.timeValtoTimeScaleId(zoomLargeScale)); // 设置Zoom视图时基
        int roll = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ROLL); // 读取滚屏模式
        boolean runStop = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP); // 读取运行/停止状态
        if (horizontalAxis.isGreater100ms() // 时基大于100ms
                && roll == 0 // 滚屏模式关闭
                && runStop) { // 且处于运行状态
            // zoom 不能滚屏
            //设置zoom标记
            CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM, String.valueOf(false), true); // 强制关闭Zoom模式
        }
        Scope.getInstance().setZoom(CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM), false); // 设置Zoom状态
        //设置timeBasePos位置
//        TriggerTimebase.getInstance().setCache();
    }

    /**
     * 检查MSS存储中的Ref通道映射
     * 对于已开启的Ref通道，将其数据来源设置为MSS标签，类型重置为WAV(0)
     * 用于MSS（Mico Storage System）存储恢复后的Ref通道状态修正
     */
    public void checkMSSStoreMap(){
        TChan.foreachRef(refChan -> { // 遍历所有Ref通道
           boolean b = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChan); // 检查Ref通道是否开启
           if(b){ // 如果Ref通道已开启
               putMapBeforeLoadParam(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChan, SaveRecoverySession.MSS_REF_TAG); // 设置数据来源为MSS标签
               putMapBeforeLoadParam(CacheUtil.RIGHT_SLIP_REF_TYPE + refChan, "0"); // 重置Ref类型为WAV
           }
        });
    }

    /**
     * 检验加载的REF文件是否合法
     * 遍历所有Ref通道，检查其引用的文件是否存在：
     * - 如果文件不存在且不是MSS标签，则关闭该Ref通道并清除引用路径
     * - 如果所有Ref通道都被关闭，则关闭Ref总开关
     * - 如果当前选中通道是已关闭的Ref通道，则自动切换到其他可用通道
     */
    private void checkLoadRefFile() {
        boolean ref = CacheUtil.get().getBoolean((CacheUtil.MAIN_RIGHT_REF)); // 读取Ref总开关
        boolean r1Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R1); // R1通道开关
        boolean r2Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R2); // R2通道开关
        boolean r3Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R3); // R3通道开关
        boolean r4Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R4); // R4通道开关
        boolean r5Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R5); // R5通道开关
        boolean r6Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R6); // R6通道开关
        boolean r7Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R7); // R7通道开关
        boolean r8Check = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + TChan.R8); // R8通道开关
        boolean[] refChecks = {r1Check, r2Check, r3Check, r4Check, r5Check, r6Check, r7Check, r8Check}; // 封装为数组便于遍历

        TChan.foreachRef(refChan -> { // 遍历所有Ref通道
            int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChan); // 读取Ref类型
            String refFilePath = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChan); // 读取Ref文件路径
            File file = new File(refFilePath); // 创建文件对象
            if (!file.exists()) { // 文件不存在
                if(SaveRecoverySession.MSS_REF_TAG.equals(refFilePath)) { // 如果是MSS标签路径
                    putMapBeforeLoadParam(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChan, SaveRecoverySession.MSS_REF_TAG); // 保留MSS标签
                    putMapBeforeLoadParam(CacheUtil.RIGHT_SLIP_REF_TYPE + refChan, "0"); // 类型重置为WAV
                }else{ // 普通文件不存在
                    refChecks[refChan - TChan.R1] = false; // 标记该Ref通道为关闭
                    CacheUtil.get().putMapBeforeLoadParam(CacheUtil.RIGHT_SLIP_REF_CHECK + refChan, String.valueOf(false)); // 关闭该Ref通道
                    CacheUtil.get().putMapBeforeLoadParam(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChan, ""); // 清空文件路径
                    String closeSave = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + refChan); // 读取关闭保存记录
                    if (closeSave.contains((refChan - ChannelFactory.MATH_MAX) + "")) { // 如果关闭保存记录中包含该通道
                        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + refChan, closeSave.replace((refChan - ChannelFactory.MATH_MAX) + "", "")); // 从关闭保存记录中移除该通道
                    }
                }
            }
        });

        if (!(r1Check || r2Check || r3Check || r4Check || r5Check || r6Check || r7Check || r8Check)) { // 如果所有Ref通道都被关闭
            ref = false; // 关闭Ref总开关
            CacheUtil.get().putMapBeforeLoadParam(CacheUtil.MAIN_RIGHT_REF, String.valueOf(false)); // 写入缓存
        }
        int channelSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT); // 读取当前选中通道
        if (!refChecks[0] && channelSelect == ChannelFactory.REF1 || !refChecks[1] && channelSelect == ChannelFactory.REF2 // 如果当前选中通道是已关闭的Ref通道
                || !refChecks[2] && channelSelect == ChannelFactory.REF3 || !refChecks[3] && channelSelect == ChannelFactory.REF4
                || !refChecks[4] && channelSelect == ChannelFactory.REF5 || !refChecks[5] && channelSelect == ChannelFactory.REF6
                || !refChecks[6] && channelSelect == ChannelFactory.REF7 || !refChecks[7] && channelSelect == ChannelFactory.REF8
        ) {
            AtomicBoolean hasSet = new AtomicBoolean(false); // 是否已找到替代通道
            TChan.foreachChan(chan -> { // 遍历所有物理通道
                if (CacheUtil.get().getBoolean(MAIN_CHANNEL_OPEN_STATE + chan)) { // 如果该物理通道已开启
                    CacheUtil.get().putMapBeforeLoadParam(CacheUtil.MAIN_CENTER_CHANNELS_SELECT, String.valueOf(TChan.toFpgaChNo(chan))); // 切换到该物理通道
                    CacheUtil.get().putMapBeforeLoadParam(CacheUtil.MAIN_CENTER_CHANNELS_SELECT_UNXY, String.valueOf(TChan.toFpgaChNo(chan))); // 同步更新非XY模式选中通道
                    hasSet.set(true); // 标记已找到替代通道
                }
            });
            TChan.foreachMath((mathChan) -> { // 遍历所有Math通道
                boolean mathCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChan); // Math通道开关
                boolean mathAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + mathChan); // 用户手动添加标记
                if (mathCheck && mathAddByUser) { // Math通道开启且用户手动添加
                    CacheUtil.get().putMapBeforeLoadParam(CacheUtil.MAIN_CENTER_CHANNELS_SELECT, String.valueOf(TChan.toFpgaChNo(mathChan))); // 切换到该Math通道
                    CacheUtil.get().putMapBeforeLoadParam(CacheUtil.MAIN_CENTER_CHANNELS_SELECT_UNXY, String.valueOf(TChan.toFpgaChNo(mathChan))); // 同步更新
                    hasSet.set(true); // 标记已找到替代通道
                }
            });
            TChan.foreachRef((refChan) -> { // 遍历所有Ref通道
                boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChan); // Ref通道开关
                boolean refAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan); // 用户手动添加标记
                if (refCheck && refAddByUser) { // Ref通道开启且用户手动添加
                    CacheUtil.get().putMapBeforeLoadParam(CacheUtil.MAIN_CENTER_CHANNELS_SELECT, String.valueOf(TChan.toFpgaChNo(refChan))); // 切换到该Ref通道
                    CacheUtil.get().putMapBeforeLoadParam(CacheUtil.MAIN_CENTER_CHANNELS_SELECT_UNXY, String.valueOf(TChan.toFpgaChNo(refChan))); // 同步更新
                    hasSet.set(true); // 标记已找到替代通道
                }
            });
//            TChan.foreachSerial(serialsChan -> { // 已废弃：串口通道遍历
//                boolean serialsCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + TChan.toSerialNumber(serialsChan));
//                boolean serialsAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + serialsChan);
//                if (serialsCheck && serialsAddByUser) {
//                    CacheUtil.get().putMapBeforeLoadParam(CacheUtil.MAIN_CENTER_CHANNELS_SELECT, String.valueOf(TChan.toFpgaChNo(serialsChan)));
//                    CacheUtil.get().putMapBeforeLoadParam(CacheUtil.MAIN_CENTER_CHANNELS_SELECT_UNXY, String.valueOf(TChan.toFpgaChNo(serialsChan)));
//                    hasSet.set(true);
//                }
//            });
            if (!hasSet.get()) { // 如果没有找到任何可用通道
                CacheUtil.get().putMapBeforeLoadParam(CacheUtil.MAIN_CENTER_CHANNELS_SELECT, String.valueOf(MainCenterMsgChannels.CH_NULL)); // 设置为空通道
                CacheUtil.get().putMapBeforeLoadParam(CacheUtil.MAIN_CENTER_CHANNELS_SELECT_UNXY, String.valueOf(MainCenterMsgChannels.CH_NULL)); // 同步更新
            }
        }
    }
}
