package com.micsig.tbook.tbookscope.middleware.command; // 命令模式中间件包

/**
 * Created by yangj on 2018/1/23.
 */
/*
 * +=============================================================================+
 * |                           CommandMsgToUI 类                                 |
 * +=============================================================================+
 * | 【模块定位】                                                                 |
 * |   命令层向UI层传递消息的数据载体类，定义了所有命令到UI的标志常量与消息结构     |
 * +-------------------------------------------------------------------------------+
 * | 【核心职责】                                                                 |
 * |   1. 定义命令→UI消息的FLAG标志常量，用于标识不同功能区域的UI更新类型          |
 * |   2. 封装flag（消息标志）、param（参数字符串）、object（附加对象）三要素     |
 * |   3. 作为RxBus事件的载荷，在Command子模块与UI层之间传递更新指令               |
 * +-------------------------------------------------------------------------------+
 * | 【架构设计】                                                                 |
 * |   简单POJO + 常量池模式，flag字符串标识消息类型，param传递参数，object传递对象|
 * |   所有FLAG以"FLAG_模块_属性"命名，便于按模块检索                             |
 * +-------------------------------------------------------------------------------+
 * | 【数据流向】                                                                 |
 * |   Command子模块 → new/setFlag/setParam → RxBus.post → UI层接收并解析         |
 * +-------------------------------------------------------------------------------+
 * | 【依赖关系】                                                                 |
 * |   - RxBus: 事件总线，传递CommandMsgToUI实例到UI层                             |
 * |   - Command子模块: 创建并填充CommandMsgToUI实例                              |
 * |   - UI层: 接收并解析CommandMsgToUI，根据flag更新界面                          |
 * +-------------------------------------------------------------------------------+
 * | 【使用场景】                                                                 |
 * |   1. 命令模块修改状态后，需要通知UI更新对应控件                               |
 * |   2. SCPI指令执行后，同步刷新界面显示                                        |
 * |   3. 作为RxBus事件载荷，解耦命令层与UI层                                     |
 * +=============================================================================+
 */

public class CommandMsgToUI {
    public static final String PARAM_SPLIT = ";"; // 参数分隔符，多个参数间用分号分隔

    //region flag
    public static final String FLAG_AUTO_CHANNEL = "FLAG_AUTO_CHANNEL"; // 自动设置-通道开关标志
    public static final String FLAG_AUTO_LEVEL = "FLAG_AUTO_LEVEL"; // 自动设置-触发电平标志
    public static final String FLAG_AUTO_SOURCE = "FLAG_AUTO_SOURCE"; // 自动设置-触发源标志
    public static final String FLAG_AUTO_RANGE = "FLAG_AUTO_RANGE"; // 自动设置-自动量程开关标志
    public static final String FLAG_AUTO_RANGEVERTICAL = "FLAG_AUTO_RANGEVERTICAL"; // 自动设置-垂直量程标志
    public static final String FLAG_AUTO_RANGEHORIZONTAL = "FLAG_AUTO_RANGEHORIZONTAL"; // 自动设置-水平量程标志
    public static final String FLAG_AUTO_RANGELEVEL = "FLAG_AUTO_RANGELEVEL"; // 自动设置-触发电平量程标志

    public static final String FLAG_CHANNEL_DISPLAY = "FLAG_CHANNEL_DISPLAY"; // 通道-显示开关标志
    public static final String FLAG_CHANNEL_INVERSE = "FLAG_CHANNEL_INVERSE"; // 通道-反相标志
    public static final String FLAG_CHANNEL_BANDWIDTH = "FLAG_CHANNEL_BANDWIDTH"; // 通道-带宽限制标志
    public static final String FLAG_CHANNEL_PROBETYPE = "FLAG_CHANNEL_PROBETYPE"; // 通道-探头类型标志
    public static final String FLAG_CHANNEL_PROBEMULTIPLE = "FLAG_CHANNEL_PROBEMULTIPLE"; // 通道-探头倍率标志
    public static final String FLAG_CHANNEL_COUPLE = "FLAG_CHANNEL_COUPLE"; // 通道-耦合方式标志
    public static final String FLAG_CHANNEL_EXTENT = "FLAG_CHANNEL_EXTENT"; // 通道-垂直挡位标志
    public static final String FLAG_CHANNEL_INPUTRES = "FLAG_CHANNEL_INPUTRES"; // 通道-输入阻抗标志
    public static final String FLAG_CHANNEL_CURRENT = "FLAG_CHANNEL_CURRENT"; // 通道-当前选中通道标志
    public static final String FLAG_CHANNEL_VREF = "FLAG_CHANNEL_VREF"; // 通道-垂直参考标志
    public static final String FLAG_CHANNEL_LABEL = "FLAG_CHANNEL_LABEL"; // 通道-标签标志
    public static final String FLAG_CHANNEL_LABEL_CLEAR = "FLAG_CHANNEL_LABEL_CLEAR"; // 通道-标签清除标志
    public static final String FLAG_CHANNEL_DELAY="FLAG_CHANNEL_DELAY"; // 通道-延迟标志
    public static final String FLAG_CHANNEL_OFFSET="FLAG_CHANNEL_OFFSET"; // 通道-偏移标志
    public static final String FLAG_CHANNEL_VERNIER="FLAG_CHANNEL_VERNIER"; // 通道-微调标志

    public static final String FLAG_CURSOR_HORIZONTAL = "FLAG_CURSOR_HORIZONTAL"; // 光标-水平光标标志
    public static final String FLAG_CURSOR_VERTICAL = "FLAG_CURSOR_VERTICAL"; // 光标-垂直光标标志
    public static final String FLAG_CURSOR_SOURCE = "FLAG_CURSOR_SOURCE"; // 光标-源通道标志
    public static final String FLAG_CURSOR_SETTING_TRACE="FLAG_CURSOR_SETTING_TRACE"; // 光标-追踪设置标志
    public static final String FLAG_DISPLAY_WAVEFORM = "FLAG_DISPLAY_WAVEFORM"; // 显示-波形类型标志
    public static final String FLAG_DISPLAY_BACKGROUND = "FLAG_DISPLAY_BACKGROUND"; // 显示-背景色标志
    public static final String FLAG_DISPLAY_BRIGHTNESS = "FLAG_DISPLAY_BRIGHTNESS"; // 显示-亮度标志
    public static final String FLAG_DISPLAY_GRATICULE = "FLAG_DISPLAY_GRATICULE"; // 显示-网格标志
    public static final String FLAG_DISPLAY_INTENSITY = "FLAG_DISPLAY_INTENSITY"; // 显示-波形亮度标志
    public static final String FLAG_DISPLAY_PERSISTMODE = "FLAG_DISPLAY_PERSISTMODE"; // 显示-余辉模式标志
    public static final String FLAG_DISPLAY_FFT_PERSISTMODE = "FLAG_DISPLAY_FFT_PERSISTMODE"; // 显示-FFT余辉模式标志
    public static final String FLAG_DISPLAY_PERSISTADJUST = "FLAG_DISPLAY_PERSISTADJUST"; // 显示-余辉调节标志
    public static final String FLAG_DISPLAY_FFT_PERSISTADJUST = "FLAG_DISPLAY_FFT_PERSISTADJUST"; // 显示-FFT余辉调节标志
    public static final String FLAG_DISPLAY_HIGHREFRESH = "FLAG_DISPLAY_HIGHREFRESH"; // 显示-高刷新率标志
    public static final String FLAG_DISPLAY_HORREF = "FLAG_DISPLAY_HORREF"; // 显示-水平参考线标志
    public static final String FLAG_DISPLAY_ZOOM = "FLAG_DISPLAY_ZOOM"; // 显示-缩放模式标志
    public static final String FLAG_DISPLAY_PERSIST_CLEAR = "FLAG_DISPLAY_PERSIST_CLEAR"; // 显示-余辉清除标志
    public static final String FLAG_DISPLAY_FFT_PERSIST_CLEAR = "FLAG_DISPLAY_FFT_PERSIST_CLEAR"; // 显示-FFT余辉清除标志

    public static final String FLAG_MENU_AUTO = "FLAG_MENU_AUTO"; // 菜单-自动标志
    public static final String FLAG_MENU_RUN = "FLAG_MENU_RUN"; // 菜单-运行标志
    public static final String FLAG_MENU_STOP = "FLAG_MENU_STOP"; // 菜单-停止标志
    public static final String FLAG_MENU_SINGLE = "FLAG_MENU_SINGLE"; // 菜单-单次标志
    public static final String FLAG_MENU_MULTIPLE = "FLAG_MENU_MULTIPLE"; // 菜单-多次标志
    public static final String FLAG_MENU_BEEP = "FLAG_MENU_BEEP"; // 菜单-蜂鸣器标志
    public static final String FLAG_MATH_DISPLAY = "FLAG_MATH_DISPLAY"; // 数学运算-显示开关标志
    public static final String FLAG_MATH_MODE = "FLAG_MATH_MODE"; // 数学运算-模式标志
    public static final String FLAG_MATH_ADDS1 = "FLAG_MATH_ADDS1"; // 数学运算-加法源1标志
    public static final String FLAG_MATH_ADDS2 = "FLAG_MATH_ADDS2"; // 数学运算-加法源2标志
    public static final String FLAG_MATH_SUBS1 = "FLAG_MATH_SUBS1"; // 数学运算-减法源1标志
    public static final String FLAG_MATH_SUBS2 = "FLAG_MATH_SUBS2"; // 数学运算-减法源2标志
    public static final String FLAG_MATH_MULS1 = "FLAG_MATH_MULS1"; // 数学运算-乘法源1标志
    public static final String FLAG_MATH_MULS2 = "FLAG_MATH_MULS2"; // 数学运算-乘法源2标志
    public static final String FLAG_MATH_DIVS1 = "FLAG_MATH_DIVS1"; // 数学运算-除法源1标志
    public static final String FLAG_MATH_DIVS2 = "FLAG_MATH_DIVS2"; // 数学运算-除法源2标志
    public static final String FLAG_MATH_FFTSOURCE = "FLAG_MATH_FFTSOURCE"; // 数学运算-FFT源标志
    public static final String FLAG_MATH_FFTWINDOW = "FLAG_MATH_FFTWINDOW"; // 数学运算-FFT窗函数标志
    public static final String FLAG_MATH_FFTTYPE = "FLAG_MATH_FFTTYPE"; // 数学运算-FFT类型标志
    public static final String FLAG_REF_DISPLAY = "FLAG_REF_DISPLAY"; // 参考波形-显示开关标志
    public static final String FLAG_REF_ENABLE = "FLAG_REF_ENABLE"; // 参考波形-使能标志
    public static final String FLAG_SAMPLE_TYPE = "FLAG_SAMPLE_TYPE"; // 采样-采样类型标志
    public static final String FLAG_SAMPLE_MEAN = "FLAG_SAMPLE_MEAN"; // 采样-平均采样次数标志
    public static final String FLAG_SAMPLE_ENVELOP = "FLAG_SAMPLE_ENVELOP"; // 采样-包络采样标志

    public static final String FLAG_TRIGGER_TYPE = "FLAG_TRIGGER_TYPE"; // 触发-触发类型标志
    public static final String FLAG_TRIGGER_HOLDOFF = "FLAG_TRIGGER_HOLDOFF"; // 触发-释抑时间标志
    public static final String FLAG_TRIGGER_MODE = "FLAG_TRIGGER_MODE"; // 触发-触发模式标志
    public static final String FLAG_TRIGGERCAN_SOURCE = "FLAG_TRIGGERCAN_SOURCE"; // CAN触发-源通道标志
    public static final String FLAG_TRIGGERCAN_IDLE = "FLAG_TRIGGERCAN_IDLE"; // CAN触发-空闲电平标志
    public static final String FLAG_TRIGGERCAN_BAUDRATE = "FLAG_TRIGGERCAN_BAUDRATE"; // CAN触发-波特率标志
    public static final String FLAG_TRIGGERCAN_TYPE = "FLAG_TRIGGERCAN_TYPE"; // CAN触发-类型标志
    public static final String FLAG_TRIGGERCAN_LEVEL = "FLAG_TRIGGERCAN_LEVEL"; // CAN触发-触发电平标志
    public static final String FLAG_TRIGGERRUNT_SOURCE = "FLAG_TRIGGERRUNT_SOURCE"; // 欠幅触发-源通道标志
    public static final String FLAG_TRIGGERRUNT_POLAR = "FLAG_TRIGGERRUNT_POLAR"; // 欠幅触发-极性标志
    public static final String FLAG_TRIGGERRUNT_CONDITION = "FLAG_TRIGGERRUNT_CONDITION"; // 欠幅触发-条件标志
    public static final String FLAG_TRIGGERRUNT_HTIME = "FLAG_TRIGGERRUNT_HTIME"; // 欠幅触发-高电平时间标志
    public static final String FLAG_TRIGGERRUNT_LOWTIME = "FLAG_TRIGGERRUNT_LOWTIME"; // 欠幅触发-低电平时间标志
    public static final String FLAG_TRIGGERRUNT_BTIME = "FLAG_TRIGGERRUNT_BTIME"; // 欠幅触发-边界时间标志
    public static final String FLAG_TRIGGERRUNT_HLEVEL = "FLAG_TRIGGERRUNT_HLEVEL"; // 欠幅触发-高电平标志
    public static final String FLAG_TRIGGERRUNT_LLEVEL = "FLAG_TRIGGERRUNT_LLEVEL"; // 欠幅触发-低电平标志
    public static final String FLAG_TRIGGEREDGE_SOURCE = "FLAG_TRIGGEREDGE_SOURCE"; // 边沿触发-源通道标志
    public static final String FLAG_TRIGGEREDGE_SLOPE = "FLAG_TRIGGEREDGE_SLOPE"; // 边沿触发-边沿方向标志
    public static final String FLAG_TRIGGEREDGE_LEVEL = "FLAG_TRIGGEREDGE_LEVEL"; // 边沿触发-触发电平标志
    public static final String FLAG_TRIGGEREDGE_COUPLE = "FLAG_TRIGGEREDGE_COUPLE"; // 边沿触发-耦合方式标志
    public static final String FLAG_TRIGGERIIC_SOURCE = "FLAG_TRIGGERIIC_SOURCE"; // IIC触发-源通道标志
    public static final String FLAG_TRIGGERIIC_CLOCK = "FLAG_TRIGGERIIC_CLOCK"; // IIC触发-时钟通道标志
    public static final String FLAG_TRIGGERIIC_TYPE = "FLAG_TRIGGERIIC_TYPE"; // IIC触发-类型标志
    public static final String FLAG_TRIGGERIIC_LEVELDATA = "FLAG_TRIGGERIIC_LEVELDATA"; // IIC触发-数据电平标志
    public static final String FLAG_TRIGGERIIC_LEVELCLOCK = "FLAG_TRIGGERIIC_LEVELCLOCK"; // IIC触发-时钟电平标志
    public static final String FLAG_TRIGGERLIN_SOURCE = "FLAG_TRIGGERLIN_SOURCE"; // LIN触发-源通道标志
    public static final String FLAG_TRIGGERLIN_IDLE = "FLAG_TRIGGERLIN_IDLE"; // LIN触发-空闲电平标志
    public static final String FLAG_TRIGGERLIN_BAUDRATE = "FLAG_TRIGGERLIN_BAUDRATE"; // LIN触发-波特率标志
    public static final String FLAG_TRIGGERLIN_TYPE = "FLAG_TRIGGERLIN_TYPE"; // LIN触发-类型标志
    public static final String FLAG_TRIGGERLIN_LEVEL = "FLAG_TRIGGERLIN_LEVEL"; // LIN触发-触发电平标志
    public static final String FLAG_TRIGGERLOGIC_STATUS = "FLAG_TRIGGERLOGIC_STATUS"; // 逻辑触发-状态标志
    public static final String FLAG_TRIGGERLOGIC_FUNCTION = "FLAG_TRIGGERLOGIC_FUNCTION"; // 逻辑触发-逻辑功能标志
    public static final String FLAG_TRIGGERLOGIC_CONDITION = "FLAG_TRIGGERLOGIC_CONDITION"; // 逻辑触发-条件标志
    public static final String FLAG_TRIGGERLOGIC_TIME = "FLAG_TRIGGERLOGIC_TIME"; // 逻辑触发-时间标志
    public static final String FLAG_TRIGGERLOGIC_HTIME = "FLAG_TRIGGERLOGIC_HTIME"; // 逻辑触发-高电平时间标志
    public static final String FLAG_TRIGGERLOGIC_LOWTIME = "FLAG_TRIGGERLOGIC_LOWTIME"; // 逻辑触发-低电平时间标志
    public static final String FLAG_TRIGGERLOGIC_BTIME = "FLAG_TRIGGERLOGIC_BTIME"; // 逻辑触发-边界时间标志
    public static final String FLAG_TRIGGERLOGIC_LEVEL = "FLAG_TRIGGERLOGIC_LEVEL"; // 逻辑触发-触发电平标志
    public static final String FLAG_TRIGGERM429_SOURCE = "FLAG_TRIGGERM429_SOURCE"; // ARINC429触发-源通道标志
    public static final String FLAG_TRIGGERM429_FORMAT = "FLAG_TRIGGERM429_FORMAT"; // ARINC429触发-格式标志
    public static final String FLAG_TRIGGERM429_DISPLAY = "FLAG_TRIGGERM429_DISPLAY"; // ARINC429触发-显示标志
    public static final String FLAG_TRIGGERM429_BAUDRATE = "FLAG_TRIGGERM429_BAUDRATE"; // ARINC429触发-波特率标志
    public static final String FLAG_TRIGGERM429_TYPE = "FLAG_TRIGGERM429_TYPE"; // ARINC429触发-类型标志
    public static final String FLAG_TRIGGERM429_LEVELHIGH = "FLAG_TRIGGERM429_LEVELHIGH"; // ARINC429触发-高电平标志
    public static final String FLAG_TRIGGERM429_LEVELLOW = "FLAG_TRIGGERM429_LEVELLOW"; // ARINC429触发-低电平标志
    public static final String FLAG_TRIGGERM1553B_SOURCE = "FLAG_TRIGGERM1553B_SOURCE"; // 1553B触发-源通道标志
    public static final String FLAG_TRIGGERM1553B_DISPLAY = "FLAG_TRIGGERM1553B_DISPLAY"; // 1553B触发-显示标志
    public static final String FLAG_TRIGGERM1553B_TYPE = "FLAG_TRIGGERM1553B_TYPE"; // 1553B触发-类型标志
    public static final String FLAG_TRIGGERM1553B_LEVEL = "FLAG_TRIGGERM1553B_LEVEL"; // 1553B触发-触发电平标志
    public static final String FLAG_TRIGGERNEDGE_SOURCE = "FLAG_TRIGGERNEDGE_SOURCE"; // 非边沿触发-源通道标志
    public static final String FLAG_TRIGGERNEDGE_SLOPE = "FLAG_TRIGGERNEDGE_SLOPE"; // 非边沿触发-边沿方向标志
    public static final String FLAG_TRIGGERNEDGE_IDLE = "FLAG_TRIGGERNEDGE_IDLE"; // 非边沿触发-空闲电平标志
    public static final String FLAG_TRIGGERNEDGE_EDGE = "FLAG_TRIGGERNEDGE_EDGE"; // 非边沿触发-边沿数标志
    public static final String FLAG_TRIGGERNEDGE_LEVEL = "FLAG_TRIGGERNEDGE_LEVEL"; // 非边沿触发-触发电平标志
    public static final String FLAG_TRIGGERPULSE_SOURCE = "FLAG_TRIGGERPULSE_SOURCE"; // 脉冲触发-源通道标志
    public static final String FLAG_TRIGGERPULSE_POLAR = "FLAG_TRIGGERPULSE_POLAR"; // 脉冲触发-极性标志
    public static final String FLAG_TRIGGERPULSE_WIDTH = "FLAG_TRIGGERPULSE_WIDTH"; // 脉冲触发-脉冲宽度标志
    public static final String FLAG_TRIGGERPULSE_HTIME = "FLAG_TRIGGERPULSE_HTIME"; // 脉冲触发-高电平时间标志
    public static final String FLAG_TRIGGERPULSE_LTIME = "FLAG_TRIGGERPULSE_LTIME"; // 脉冲触发-低电平时间标志
    public static final String FLAG_TRIGGERPULSE_CONDITION = "FLAG_TRIGGERPULSE_CONDITION"; // 脉冲触发-条件标志
    public static final String FLAG_TRIGGERPULSE_LEVEL = "FLAG_TRIGGERPULSE_LEVEL"; // 脉冲触发-触发电平标志
    public static final String FLAG_TRIGGERSLOPE_SOURCE = "FLAG_TRIGGERSLOPE_SOURCE"; // 斜率触发-源通道标志
    public static final String FLAG_TRIGGERSLOPE_EDGE = "FLAG_TRIGGERSLOPE_EDGE"; // 斜率触发-边沿方向标志
    public static final String FLAG_TRIGGERSLOPE_CONDITION = "FLAG_TRIGGERSLOPE_CONDITION"; // 斜率触发-条件标志
    public static final String FLAG_TRIGGERSLOPE_HTIME = "FLAG_TRIGGERSLOPE_HTIME"; // 斜率触发-高电平时间标志
    public static final String FLAG_TRIGGERSLOPE_LTIME = "FLAG_TRIGGERSLOPE_LTIME"; // 斜率触发-低电平时间标志
    public static final String FLAG_TRIGGERSLOPE_BTIME = "FLAG_TRIGGERSLOPE_BTIME"; // 斜率触发-边界时间标志
    public static final String FLAG_TRIGGERSLOPE_HLEVEL = "FLAG_TRIGGERSLOPE_HLEVEL"; // 斜率触发-高电平标志
    public static final String FLAG_TRIGGERSLOPE_LLEVEL = "FLAG_TRIGGERSLOPE_LLEVEL"; // 斜率触发-低电平标志
    public static final String FLAG_TRIGGER_SERIALBUS_TYPE = "FLAG_TRIGGER_SERIALBUS_TYPE"; // 串行总线触发-类型标志
    public static final String FLAG_TRIGGERSPI_CLOCK = "FLAG_TRIGGERSPI_CLOCK"; // SPI触发-时钟通道标志
    public static final String FLAG_TRIGGERSPI_CLOCKSWITCH = "FLAG_TRIGGERSPI_CLOCKSWITCH"; // SPI触发-时钟边沿开关标志
    public static final String FLAG_TRIGGERSPI_DATA = "FLAG_TRIGGERSPI_DATA"; // SPI触发-数据通道标志
    public static final String FLAG_TRIGGERSPI_DATASWITCH = "FLAG_TRIGGERSPI_DATASWITCH"; // SPI触发-数据边沿开关标志
    public static final String FLAG_TRIGGERSPI_CS = "FLAG_TRIGGERSPI_CS"; // SPI触发-CS片选通道标志
    public static final String FLAG_TRIGGERSPI_CSWITCH = "FLAG_TRIGGERSPI_CSWITCH"; // SPI触发-CS边沿开关标志
    public static final String FLAG_TRIGGERSPI_CSENABLE = "FLAG_TRIGGERSPI_CSENABLE"; // SPI触发-CS使能标志
    public static final String FLAG_TRIGGERSPI_BITS = "FLAG_TRIGGERSPI_BITS"; // SPI触发-数据位宽标志
    public static final String FLAG_TRIGGERSPI_TYPE = "FLAG_TRIGGERSPI_TYPE"; // SPI触发-类型标志
    public static final String FLAG_TRIGGERSPI_LEVELCLOCK = "FLAG_TRIGGERSPI_LEVELCLOCK"; // SPI触发-时钟电平标志
    public static final String FLAG_TRIGGERSPI_LEVELDATA = "FLAG_TRIGGERSPI_LEVELDATA"; // SPI触发-数据电平标志
    public static final String FLAG_TRIGGERSPI_LEVELCS = "FLAG_TRIGGERSPI_LEVELCS"; // SPI触发-CS电平标志
    public static final String FLAG_TRIGGERTIMEOUT_SOURCE = "FLAG_TRIGGERTIMEOUT_SOURCE"; // 超时触发-源通道标志
    public static final String FLAG_TRIGGERTIMEOUT_POLAR = "FLAG_TRIGGERTIMEOUT_POLAR"; // 超时触发-极性标志
    public static final String FLAG_TRIGGERTIMEOUT_TIME = "FLAG_TRIGGERTIMEOUT_TIME"; // 超时触发-超时时间标志
    public static final String FLAG_TRIGGERTIMEOUT_LEVEL = "FLAG_TRIGGERTIMEOUT_LEVEL"; // 超时触发-触发电平标志
    public static final String FLAG_TRIGGERUART_SOURCE = "FLAG_TRIGGERUART_SOURCE"; // UART触发-源通道标志
    public static final String FLAG_TRIGGERUART_IDLE = "FLAG_TRIGGERUART_IDLE"; // UART触发-空闲电平标志
    public static final String FLAG_TRIGGERUART_CHECK = "FLAG_TRIGGERUART_CHECK"; // UART触发-校验位标志
    public static final String FLAG_TRIGGERUART_BITS = "FLAG_TRIGGERUART_BITS"; // UART触发-数据位宽标志
    public static final String FLAG_TRIGGERUART_BAUDRATE = "FLAG_TRIGGERUART_BAUDRATE"; // UART触发-波特率标志
    public static final String FLAG_TRIGGERUART_DISPLAY = "FLAG_TRIGGERUART_DISPLAY"; // UART触发-显示标志
    public static final String FLAG_TRIGGERUART_LEVEL = "FLAG_TRIGGERUART_LEVEL"; // UART触发-触发电平标志
    public static final String FLAG_TRIGGERUART_TYPE = "FLAG_TRIGGERUART_TYPE"; // UART触发-类型标志
    public static final String FLAG_TRIGGERVIDEO_SOURCE = "FLAG_TRIGGERVIDEO_SOURCE"; // 视频触发-源通道标志
    public static final String FLAG_TRIGGERVIDEO_POLAR = "FLAG_TRIGGERVIDEO_POLAR"; // 视频触发-极性标志
    public static final String FLAG_TRIGGERVIDEO_STANDARD = "FLAG_TRIGGERVIDEO_STANDARD"; // 视频触发-制式标准标志
    public static final String FLAG_TRIGGERVIDEO_AMODE = "FLAG_TRIGGERVIDEO_AMODE"; // 视频触发-A模式标志
    public static final String FLAG_TRIGGERVIDEO_BMODE = "FLAG_TRIGGERVIDEO_BMODE"; // 视频触发-B模式标志
    public static final String FLAG_TRIGGERVIDEO_AFREQUENCE = "FLAG_TRIGGERVIDEO_AFREQUENCE"; // 视频触发-A频率标志
    public static final String FLAG_TRIGGERVIDEO_BFREQUENCE = "FLAG_TRIGGERVIDEO_BFREQUENCE"; // 视频触发-B频率标志
    public static final String FLAG_TRIGGERVIDEO_LINE = "FLAG_TRIGGERVIDEO_LINE"; // 视频触发-行号标志

    public static final String FLAG_USERSET_LENGTH = "FLAG_USERSET_LENGTH"; // 用户设置-长度标志
    public static final String FLAG_USERSET_FACTORYRESET = "FLAG_USERSET_FACTORYRESET"; // 用户设置-恢复出厂标志
    public static final String FLAG_USERSET_SELFADJUST = "FLAG_USERSET_SELFADJUST"; // 用户设置-自校准标志
    public static final String FLAG_USERSET_NAME = "FLAG_USERSET_NAME"; // 用户设置-名称标志
    public static final String FLAG_USERSET_SAVE = "FLAG_USERSET_SAVE"; // 用户设置-保存标志
    public static final String FLAG_USERSET_RECOVERY = "FLAG_USERSET_RECOVERY"; // 用户设置-恢复标志
    public static final String FLAG_USERSET_AutoZero = "FLAG_USERSET_AutoZero"; // 用户设置-自动清零标志

    public static final String FLAG_MATH_ADDEXTENT = "FLAG_MATH_ADDEXTENT"; // 数学运算-加法挡位标志
    public static final String FLAG_MATH_ADDOFFSET = "FLAG_MATH_ADDOFFSET"; // 数学运算-加法偏移标志
    public static final String FLAG_MATH_SUBEXTENT = "FLAG_MATH_SUBEXTENT"; // 数学运算-减法挡位标志
    public static final String FLAG_MATH_SUBOFFSET = "FLAG_MATH_SUBOFFSET"; // 数学运算-减法偏移标志
    public static final String FLAG_MATH_MULEXTENT = "FLAG_MATH_MULEXTENT"; // 数学运算-乘法挡位标志
    public static final String FLAG_MATH_MULOFFSET = "FLAG_MATH_MULOFFSET"; // 数学运算-乘法偏移标志
    public static final String FLAG_MATH_DIVEXTENT = "FLAG_MATH_DIVEXTENT"; // 数学运算-除法挡位标志
    public static final String FLAG_MATH_DIVOFFSET = "FLAG_MATH_DIVOFFSET"; // 数学运算-除法偏移标志

    public static final String FLAG_MENU_HALF_CHANNEL = "FLAG_MENU_HALF_CHANNEL"; // 菜单-半通道模式标志
    public static final String FLAG_MENU_TRIGPOS = "FLAG_MENU_TRIGPOS"; // 菜单-触发位置标志
    public static final String FLAG_MENU_XCURSOR = "FLAG_MENU_XCURSOR"; // 菜单-X光标标志
    public static final String FLAG_MENU_YCURSOR = "FLAG_MENU_YCURSOR"; // 菜单-Y光标标志
    public static final String FLAG_MENU_LEVEL = "FLAG_MENU_LEVEL"; // 菜单-触发电平标志
    public static final String FLAG_MENU_HOMEPAGE = "FLAG_MENU_HOMEPAGE"; // 菜单-主页标志
    public static final String FLAG_MENU_RETURN = "FLAG_MENU_RETURN"; // 菜单-返回标志
    public static final String FLAG_MENU_LOCK = "FLAG_MENU_LOCK"; // 菜单-锁屏标志
    public static final String FLAG_MENU_UNLOCK = "FLAG_MENU_UNLOCK"; // 菜单-解锁标志
    public static final String FLAG_MENU_COUNTER = "FLAG_MENU_COUNTER"; // 菜单-计数器标志
    public static final String FLAG_MENU_RESET = "FLAG_MENU_RESET"; // 菜单-重置标志
    public static final String FLAG_MENU_MEASURE = "FLAG_MENU_MEASURE"; // 菜单-测量标志
    public static final String FLAG_MENU_TRIGGER = "FLAG_MENU_TRIGGER"; // 菜单-触发标志
    public static final String FLAG_MENU_CHANNELSELECTOR = "FLAG_MENU_CHANNELSELECTOR"; // 菜单-通道选择器标志
    public static final String FLAG_MENU_Main = "FLAG_MENU_Main"; // 菜单-主菜单标志
    public static final String FLAG_MENU_AUX_TRIGGER="FLAG_MENU_AUX_TRIGGER"; // 菜单-辅助触发标志
    public static final String FLAG_MENU_AUX_CLOCK="FLAG_MENU_AUX_CLOCK"; // 菜单-辅助时钟标志
    public static final String FLAG_MENU_AUX_INPUTRES="FLAG_MENU_AUX_INPUTRES"; // 菜单-辅助输入阻抗标志

    public static final String FLAG_MENU_QuickBottom = "FLAG_MENU_QuickBottom"; // 菜单-快捷底部栏标志
    public static final String FLAG_MENU_CHANNEL = "FLAG_MENU_CHANNEL"; // 菜单-通道菜单标志

    public static final String FLAG_DISPLAY_TIMEBASE = "FLAG_DISPLAY_TIMEBASE"; // 显示-时基标志
    public static final String FLAG_DISPLAY_CCT = "FLAG_DISPLAY_CCT"; // 显示-CCT标志

    public static final String FLAG_TIMEBASE_EXTENT = "FLAG_TIMEBASE_EXTENT"; // 时基-水平挡位标志
    public static final String FLAG_TIMEBASE_POSITION = "FLAG_TIMEBASE_POSITION"; // 时基-水平位置标志
    public static final String FLAG_TIMEBASE_MODE = "FLAG_TIMEBASE_MODE"; // 时基-模式标志
    public static final String FLAG_TIMEBASE_ROLL = "FLAG_TIMEBASE_ROLL"; // 时基-滚动模式标志
    public static final String FLAG_TIMEBASE_ZOOM_SCALE = "FLAG_TIMEBASE_ZOOM_SCALE"; // 时基-缩放比例标志

    public static final String FLAG_MATH_BASE_S1 = "FLAG_MATH_BASE_S1"; // 基础数学-源1标志
    public static final String FLAG_MATH_BASE_S2 = "FLAG_MATH_BASE_S2"; // 基础数学-源2标志
    public static final String FLAG_MATH_BASE_EXTENT = "FLAG_MATH_BASE_EXTENT"; // 基础数学-挡位标志
    public static final String FLAG_MATH_BASE_OFFSET = "FLAG_MATH_BASE_OFFSET"; // 基础数学-偏移标志
    public static final String FLAG_MATH_BASE_OPERATOR = "FLAG_MATH_BASE_OPERATOR"; // 基础数学-运算符标志
    public static final String FLAG_MATH_VREF = "FLAG_MATH_VREF"; // 数学运算-垂直参考标志
    public static final String FLAG_MATH_FFT_Extent = "FLAG_MATH_FFT_Extent"; // FFT数学-挡位标志
    public static final String FLAG_MATH_FFT_Offset = "FLAG_MATH_FFT_Offset"; // FFT数学-偏移标志
    public static final String FLAG_MATH_FFT_HsCal = "FLAG_MATH_FFT_HsCal"; // FFT数学-水平校准标志
    public static final String FLAG_MATH_FFT_Position = "FLAG_MATH_FFT_Position"; // FFT数学-位置标志
    public static final String FLAG_MATH_AXB_Source = "FLAG_MATH_AXB_Source"; // AX+B数学-源标志
    public static final String FLAG_MATH_AXB_Extent = "FLAG_MATH_AXB_Extent"; // AX+B数学-挡位标志
    public static final String FLAG_MATH_AXB_Offset = "FLAG_MATH_AXB_Offset"; // AX+B数学-偏移标志
    public static final String FLAG_MATH_AXB_A = "FLAG_MATH_AXB_A"; // AX+B数学-系数A标志
    public static final String FLAG_MATH_AXB_B = "FLAG_MATH_AXB_B"; // AX+B数学-系数B标志
    public static final String FLAG_MATH_AXB_UNIT = "FLAG_MATH_AXB_UNIT"; // AX+B数学-单位标志
    public static final String FLAG_MATH_LABEL = "FLAG_MATH_LABEL"; // 数学运算-标签标志
    public static final String FLAG_MATH_LABEL_CLEAR = "FLAG_MATH_LABEL_CLEAR"; // 数学运算-标签清除标志

    public static final String FLAG_MATH_ADV_Express = "FLAG_MATH_ADV_Express"; // 高级数学-表达式标志
    public static final String FLAG_MATH_ADV_Var1 = "FLAG_MATH_ADV_Var1"; // 高级数学-变量1标志
    public static final String FLAG_MATH_ADV_Var2 = "FLAG_MATH_ADV_Var2"; // 高级数学-变量2标志
    public static final String FLAG_MATH_ADV_Extent = "FLAG_MATH_ADV_Extent"; // 高级数学-挡位标志
    public static final String FLAG_MATH_ADV_Offset = "FLAG_MATH_ADV_Offset"; // 高级数学-偏移标志
    public static final String FLAG_MATH_ADV_Unit = "FLAG_MATH_ADV_Unit"; // 高级数学-单位标志

    public static final String FLAG_STOTAGE_SAVE = "FLAG_STOTAGE_SAVE"; // 存储-保存标志
    public static final String FLAG_STOTAGE_CAPTURE = "FLAG_STOTAGE_CAPTURE"; // 存储-截图标志
    public static final String FLAG_STOTAGE_RECORD = "FLAG_STOTAGE_RECORD"; // 存储-录制标志
    public static final String FLAG_STOTAGE_SAVE_SOURCE = "FLAG_STOTAGE_SAVE_SOURCE"; // 存储-保存源标志
    public static final String FLAG_STOTAGE_SAVE_LOCATION = "FLAG_STOTAGE_SAVE_LOCATION"; // 存储-保存位置标志
    public static final String FLAG_STOTAGE_SAVE_TYPE = "FLAG_STOTAGE_SAVE_TYPE"; // 存储-保存类型标志
    public static final String FLAG_STOTAGE_SAVE_FILENAME = "FLAG_STOTAGE_SAVE_FILENAME"; // 存储-保存文件名标志
    public static final String FLAG_STOTAGE_SAVE_START = "FLAG_STOTAGE_SAVE_START"; // 存储-保存开始标志
    public static final String FLAG_STOTAGE_LOAD = "FLAG_STOTAGE_LOAD"; // 存储-加载标志
    public static final String FLAG_STOTAGE_CAPTURE_TIME = "FLAG_STOTAGE_CAPTURE_TIME"; // 存储-截图时间标志
    public static final String FLAG_STOTAGE_CAPTURE_INCOLOR = "FLAG_STOTAGE_CAPTURE_INCOLOR"; // 存储-截图反色标志
    public static final String FLAG_STOTAGE_CAPTURE_THUMBNAIL = "FLAG_STOTAGE_CAPTURE_THUMBNAIL"; // 存储-缩略图标志
    public static final String FLAG_STOTAGE_CAPTURE_START = "FLAG_STOTAGE_CAPTURE_START"; // 存储-截图开始标志
    public static final String FLAG_STOTAGE_SAVE_ALLSEGMENTS = "FLAG_STOTAGE_SAVE_ALLSEGMENTS"; // 存储-保存所有段标志
    public static final String FLAG_STOTAGE_CONSAVE = "FLAG_STOTAGE_CONSAVE"; // 存储-连续保存标志
    public static final String FLAG_STOTAGE_CONSAVE_START = "FLAG_STOTAGE_CONSAVE_START"; // 存储-连续保存开始标志
    public static final String FLAG_STOTAGE_CONLOAD = "FLAG_STOTAGE_CONLOAD"; // 存储-连续加载标志

    public static final String FLAG_Bus_Mode = "FLAG_Bus_Mode"; // 总线-模式标志
    public static final String FLAG_Bus_Type = "FLAG_Bus_Type"; // 总线-类型标志
    public static final String FLAG_Bus_Uart_Rx = "FLAG_Bus_Uart_Rx"; // UART总线-接收通道标志
    public static final String FLAG_Bus_Uart_IdLevel = "FLAG_Bus_Uart_IdLevel"; // UART总线-空闲电平标志
    public static final String FLAG_Bus_Uart_BaudRate = "FLAG_Bus_Uart_BaudRate"; // UART总线-波特率标志
    public static final String FLAG_Bus_Uart_Check = "FLAG_Bus_Uart_Check"; // UART总线-校验位标志
    public static final String FLAG_Bus_Uart_UserBaud = "FLAG_Bus_Uart_UserBaud"; // UART总线-自定义波特率标志
    public static final String FLAG_Bus_Uart_Width = "FLAG_Bus_Uart_Width"; // UART总线-数据位宽标志
    public static final String FLAG_Bus_Uart_Display = "FLAG_Bus_Uart_Display"; // UART总线-显示标志

    public static final String FLAG_Bus_Lin_Channel = "FLAG_Bus_Lin_Channel"; // LIN总线-通道标志
    public static final String FLAG_Bus_Lin_IdLevel = "FLAG_Bus_Lin_IdLevel"; // LIN总线-空闲电平标志
    public static final String FLAG_Bus_Lin_BaudRate = "FLAG_Bus_Lin_BaudRate"; // LIN总线-波特率标志
    public static final String FLAG_Bus_Lin_Userbaud = "FLAG_Bus_Lin_Userbaud"; // LIN总线-自定义波特率标志
    public static final String FLAG_Bus_Lin_TYPE = "FLAG_Bus_Lin_Type"; // LIN总线-类型标志

    public static final String FLAG_Bus_Can_Channel = "FLAG_Bus_Can_Channel"; // CAN总线-通道标志
    public static final String FLAG_Bus_Can_Signal = "FLAG_Bus_Can_Signal"; // CAN总线-信号类型标志
    public static final String FLAG_Bus_Can_BaudRate = "FLAG_Bus_Can_BaudRate"; // CAN总线-波特率标志
    public static final String FLAG_Bus_Can_UserBaud = "FLAG_Bus_Can_UserBaud"; // CAN总线-自定义波特率标志
    public static final String FLAG_Bus_Can_SamplePoint = "FLAG_Bus_Can_SamplePoint"; // CAN总线-采样点标志
    public static final String FLAG_Bus_Can_FDBaudrate = "FLAG_Bus_Can_FDBaudrate"; // CAN总线-FD波特率标志
    public static final String FLAG_Bus_Can_FDUserBaud = "FLAG_Bus_Can_FDUserBaud"; // CAN总线-FD自定义波特率标志
    public static final String FLAG_Bus_Can_FDSamplePoint = "FLAG_Bus_Can_FDSamplePoint"; // CAN总线-FD采样点标志
    public static final String FLAG_Bus_Can_ISO="FLAG_Bus_Can_ISO"; // CAN总线-ISO标准标志

    public static final String FLAG_Bus_IIC_SDA = "FLAG_Bus_IIC_SDA"; // IIC总线-SDA数据线通道标志
    public static final String FLAG_Bus_IIC_SCL = "FLAG_Bus_IIC_SCL"; // IIC总线-SCL时钟线通道标志

    public static final String FLAG_Bus_429_Channel = "FLAG_Bus_429_Channel"; // ARINC429总线-通道标志
    public static final String FLAG_Bus_429_Format = "FLAG_Bus_429_Format"; // ARINC429总线-格式标志
    public static final String FLAG_Bus_429_display = "FLAG_Bus_429_display"; // ARINC429总线-显示标志
    public static final String FLAG_Bus_429_Baudrate = "FLAG_Bus_429_Baudrate"; // ARINC429总线-波特率标志

    public static final String FLAG_Bus_1553B_Channel = "FLAG_Bus_1553B_Channel"; // 1553B总线-通道标志
    public static final String FLAG_Bus_1553B_Display = "FLAG_Bus_1553B_Display"; // 1553B总线-显示标志


    public static final String FLAG_SAMPLE_IsOpenSegMent = "FLAG_SAMPLE_IsOpenSegMent"; // 采样-分段采样开关标志
    public static final String FLAG_SAMPLE_SegmentedQTY = "FLAG_SAMPLE_SegmentedQTY"; // 采样-分段数量标志
    public static final String FLAG_SAMPLE_SegmentedDisplayType = "FLAG_SAMPLE_SegmentedDisplayType"; // 采样-分段显示类型标志
    public static final String FLAG_SAMPLE_SegmentedOrder = "FLAG_SAMPLE_SegmentedOrder"; // 采样-分段排序标志
    public static final String FLAG_SAMPLE_SegmentedPlay = "FLAG_SAMPLE_SegmentedPlay"; // 采样-分段播放标志
    public static final String FLAG_SAMPLE_SegmentedStop = "FLAG_SAMPLE_SegmentedStop"; // 采样-分段停止标志
    public static final String FLAG_SAMPLE_SegmentedFra1 = "FLAG_SAMPLE_SegmentedFra1"; // 采样-分段帧1标志
    public static final String FLAG_SAMPLE_SegmentedFra2 = "FLAG_SAMPLE_SegmentedFra2"; // 采样-分段帧2标志
    public static final String FLAG_SAMPLE_SegmentedFra3 = "FLAG_SAMPLE_SegmentedFra3"; // 采样-分段帧3标志
    public static final String FLAG_SAMPLE_SegmentedPlaySpeed = "FLAG_SAMPLE_SegmentedPlaySpeed"; // 采样-分段播放速度标志

    public static final String FLAG_TRIGGER_SPI_TYPE = "FLAG_TRIGGER_SPI_TYPE"; // SPI触发-类型标志
    public static final String FLAG_TRIGGER_SPI_DATA = "FLAG_TRIGGER_SPI_DATA"; // SPI触发-数据标志
    public static final String FLAG_TRIGGER_SPI_LEVEL = "FLAG_TRIGGER_SPI_LEVEL"; // SPI触发-电平标志

    public static final String FLAG_REF_Current_Channel = "FLAG_REF_Current_Channel"; // 参考波形-当前通道标志
    public static final String FLAG_REF_Hscal = "FLAG_REF_Hscal"; // 参考波形-水平校准标志
    public static final String FLAG_REF_Vscal = "FLAG_REF_Vscal"; // 参考波形-垂直校准标志
    public static final String FLAG_REF_Position = "FLAG_REF_Position"; // 参考波形-位置标志
    public static final String FLAG_REF_Timebase_Position = "FLAG_REF_Timebase_Position"; // 参考波形-时基位置标志
    public static final String FLAG_REF_LABEL = "FLAG_REF_LABEL"; // 参考波形-标签标志
    public static final String FLAG_REF_LABEL_CLEAR = "FLAG_REF_LABEL_CLEAR"; // 参考波形-标签清除标志

    public static final String FLAG_MEASURE_ALL_DISPLAY = "FLAG_MEASURE_ALL_DISPLAY"; // 测量-全部显示标志
    public static final String FLAG_MEASURE_COUNT_SOURCE = "FLAG_MEASURE_COUNT_SOURCE"; // 测量-计数源标志
    public static final String FLAG_MEASURE_COUNT_MODE = "FLAG_MEASURE_COUNT_MODE"; // 测量-计数模式标志

    public static final String FLAG_Measure_STAT_Display="FLAG_Measure_STAT_Display"; // 测量统计-显示标志
    public static final String FLAG_Measure_STAT_Reset="FLAG_Measure_STAT_Reset"; // 测量统计-重置标志
    public static final String FLAG_Measure_STAT_Mean="FLAG_Measure_STAT_Mean"; // 测量统计-均值标志
    public static final String FLAG_Measure_STAT_Max="FLAG_Measure_STAT_Max"; // 测量统计-最大值标志
    public static final String FLAG_Measure_STAT_Min="FLAG_Measure_STAT_Min"; // 测量统计-最小值标志
    public static final String FLAG_Measure_STAT_Dev="FLAG_Measure_STAT_Dev"; // 测量统计-标准差标志
    public static final String FLAG_Measure_STAT_Count="FLAG_Measure_STAT_Count"; // 测量统计-计数标志

    public static final String FLAG_Measure_Setting_Indicator="FLAG_Measure_Setting_Indicator"; // 测量设置-指示器标志
    public static final String FLAG_Measure_Setting_Range="FLAG_Measure_Setting_Range"; // 测量设置-范围标志
    //endregion

    private String flag; // 消息标志，标识消息类型
    private String param; // 消息参数，携带具体数值（多参数以PARAM_SPLIT分隔）
    private Object object; // 附加对象，用于传递复杂数据结构

    /**
     * 获取消息标志
     * @return 消息标志字符串
     */
    public String getFlag() {
        return flag; // 返回消息标志
    }

    /**
     * 获取消息参数
     * @return 消息参数字符串
     */
    public String getParam() {
        return param; // 返回消息参数
    }

    /**
     * 设置消息标志
     * @param flag 消息标志字符串
     */
    public void setFlag(String flag) {
        this.flag = flag; // 赋值消息标志
    }

    /**
     * 设置消息参数
     * @param param 消息参数字符串
     */
    public void setParam(String param) {
        this.param = param; // 赋值消息参数
    }

    /**
     * 设置附加对象
     * @param object 附加数据对象
     */
    public void setObject(Object object){
        this.object=object; // 赋值附加对象
    }

    /**
     * 获取附加对象
     * @return 附加数据对象
     */
    public Object getObject(){
        return this.object; // 返回附加对象
    }

    /**
     * 默认无参构造函数
     */
    public CommandMsgToUI() {
    }

    /**
     * 带参数的构造函数
     * @param flag 消息标志字符串
     * @param param 消息参数字符串
     */
    public CommandMsgToUI(String flag, String param) {
        this.flag = flag; // 赋值消息标志
        this.param = param; // 赋值消息参数
    }
}
