package com.micsig.tbook.tbookscope.rxjava;  // 定义包名：RxJava事件总线模块

import com.micsig.tbook.tbookscope.middleware.mq.MQBase;  // 导入MQBase类：消息基类
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum;  // 导入MQEnum类：消息类型枚举
import com.micsig.tbook.tbookscope.middleware.mq.msg.MsgChActiveChange;  // 导入MsgChActiveChange类：通道激活变化消息
import com.micsig.tbook.tbookscope.middleware.mq.msg.MsgChOpenClose;  // 导入MsgChOpenClose类：通道开关消息

import java.util.HashMap;  // 导入HashMap类：哈希映射
import java.util.Map;  // 导入Map类：映射接口

/**
 * RxJava事件总线注册器 - 消息通道注册与消息类映射管理
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：tbookscope.rxjava（RxJava事件总线模块）</li>
 *   <li>架构层级：中间件层 - 消息总线管理</li>
 *   <li>设计模式：注册器模式 + 工厂模式</li>
 *   <li>职责类型：消息通道注册、消息类映射、消息创建</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>注册所有RxEnum定义的消息通道到RxBus</li>
 *   <li>维护RxEnum-MQEnum-消息类的三层映射关系</li>
 *   <li>提供消息类型解析功能</li>
 *   <li>提供消息实例创建功能</li>
 * </ul>
 * 
 * <p><b>消息总线架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   RxBusRegister - 消息总线注册器                                          │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   三层消息映射结构                                                │   │
 * │   │                                                                   │   │
 * │   │   RxEnum（消息通道）                                              │   │
 * │   │       │                                                          │   │
 * │   │       ├── MQ_CHANNEL_ACTIVE_CHANGE（通道激活变化）               │   │
 * │   │       │       │                                                  │   │
 * │   │       │       ├── MQEnum.CH_OPEN → MsgChOpenClose.class         │   │
 * │   │       │       ├── MQEnum.CH_CLOSE → MsgChOpenClose.class        │   │
 * │   │       │       └── MQEnum.CH_ACTIVE → MsgChActiveChange.class    │   │
 * │   │       │                                                          │   │
 * │   │       └── ...其他消息通道                                         │   │
 * │   │                                                                   │   │
 * │   │   映射关系：RxEnum → HashMap&lt;MQEnum, Class&gt;                   │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   消息流转流程                                                    │   │
 * │   │                                                                   │   │
 * │   │   发送方                                                          │   │
 * │   │       │                                                          │   │
 * │   │       ├──→ createMsg() 创建消息实例                               │   │
 * │   │       │       │                                                  │   │
 * │   │       │       └──→ 设置rxType、mqType                            │   │
 * │   │       │                                                          │   │
 * │   │       └──→ RxBus.post() 发送消息                                  │   │
 * │   │               │                                                  │   │
 * │   │               └──→ RxBus根据RxEnum分发到对应通道                  │   │
 * │   │                                                                   │   │
 * │   │   接收方                                                          │   │
 * │   │       │                                                          │   │
 * │   │       ├──→ RxBus.register() 注册监听                              │   │
 * │   │       │                                                          │   │
 * │   │       └──→ parseMqEnum() 解析消息类型                             │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>消息类型说明：</b>
 * <pre>
 *   RxEnum：消息通道枚举，定义消息的大类（如通道激活变化、触发变化等）
 *   MQEnum：消息子类型枚举，定义消息的具体类型（如CH_OPEN、CH_CLOSE等）
 *   MQBase：消息基类，所有消息都继承此类
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>应用启动时，创建RxBusRegister实例，注册所有消息通道</li>
 *   <li>发送消息时，调用createMsg()创建消息实例</li>
 *   <li>接收消息时，调用parseMqEnum()解析消息类型</li>
 *   <li>应用退出时，调用unRegister()注销所有消息通道</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：RxBus（事件总线，基于RxJava3）</li>
 *   <li>依赖：RxEnum（消息通道枚举）</li>
 *   <li>依赖：MQEnum（消息子类型枚举）</li>
 *   <li>依赖：MQBase（消息基类）</li>
 *   <li>依赖：各种消息实现类（MsgChOpenClose、MsgChActiveChange等）</li>
 * </ul>
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/5/16
 * @see RxBus 事件总线
 * @see RxEnum 消息通道枚举
 * @see MQEnum 消息子类型枚举
 * @see MQBase 消息基类
 */
public class RxBusRegister {
    
    /**
     * 构造方法：初始化消息总线注册器
     * 
     * <p>执行以下初始化操作：
     * <ol>
     *   <li>注册所有RxEnum定义的消息通道到RxBus</li>
     *   <li>初始化消息类映射关系</li>
     * </ol>
     */
    public RxBusRegister() {
        initRegister();  // 注册所有消息通道
        initMsg();  // 初始化消息类映射
    }

    /**
     * 初始化消息通道注册
     * 
     * <p>遍历RxEnum枚举的所有值，为每个值注册一个消息通道到RxBus。
     * 每个RxEnum值对应一个独立的PublishSubject，用于消息分发。
     */
    private void initRegister() {
        for(RxEnum bean: RxEnum.values()){  // 遍历所有RxEnum枚举值
            RxBus.getInstance().register(bean);  // 为每个枚举值注册一个消息通道
        }
    }

    /**
     * 注销所有消息通道
     * 
     * <p>遍历RxEnum枚举的所有值，从RxBus中注销对应的消息通道。
     * 通常在应用退出时调用，释放资源。
     */
    public void unRegister() {
        for(RxEnum bean: RxEnum.values()){  // 遍历所有RxEnum枚举值
            RxBus.getInstance().unregister(bean);  // 注销消息通道
        }
    }

    /**
     * 消息类映射表：存储RxEnum到(MQEnum, Class)映射的映射
     * 
     * <p>结构：RxEnum → HashMap&lt;MQEnum, Class&gt;
     * <ul>
     *   <li>第一层：RxEnum作为key，表示消息通道</li>
     *   <li>第二层：MQEnum作为key，Class作为value，表示具体消息类型和对应的类</li>
     * </ul>
     */
    private static HashMap<RxEnum, HashMap<MQEnum,Class>> msgClass=new HashMap<>();  // 消息类映射表
    
    /**
     * 初始化所有消息类映射
     * 
     * <p>调用各个初始化方法，建立完整的消息类映射关系。
     * 包括：通道激活变化、通道参数变化、数学通道参数变化、参考通道参数变化、
     * 串口参数变化、触发变化等。
     */
    public static void initMsg(){
        init_SaveLoad();  // 初始化保存/加载消息映射（当前已注释）
        init_chActiveChange();  // 初始化通道激活变化消息映射
        init_ChParamChange();  // 初始化通道参数变化消息映射（当前已注释）
        init_MathParamChange();  // 初始化数学通道参数变化消息映射（当前已注释）
        init_RefParamChange();  // 初始化参考通道参数变化消息映射（当前已注释）
        init_SerialParamChange();  // 初始化串口参数变化消息映射（当前已注释）

        init_TriggerChange();  // 初始化触发变化消息映射（当前已注释）

    }
    
    /**
     * 初始化保存/加载消息映射
     * 
     * <p>建立保存/加载相关的消息类映射。
     * 当前已注释，暂未使用。
     */
    private static void init_SaveLoad(){
//        msgClass.put(RxEnum.LOAD_SCOPE_PARAM,new HashMap<>());  // 创建LOAD_SCOPE_PARAM通道的映射
//        HashMap map=msgClass.get(RxEnum.LOAD_SCOPE_PARAM);  // 获取映射表
//        map.put(MQEnum.LOAD_SCOPE_FPGA, MsgLoadScope.class);  // FPGA加载消息
//        map.put(MQEnum.LOAD_SCOPE_UI,MsgLoadScope.class);  // UI加载消息
//        map.put(MQEnum.LOAD_SCOPE_COMPLETE,MsgLoadScope.class);  // 加载完成消息

    }

    /**
     * 初始化通道激活变化消息映射
     * 
     * <p>建立通道激活变化相关的消息类映射：
     * <ul>
     *   <li>CH_OPEN → MsgChOpenClose：通道打开消息</li>
     *   <li>CH_CLOSE → MsgChOpenClose：通道关闭消息</li>
     *   <li>CH_ACTIVE → MsgChActiveChange：通道激活消息</li>
     * </ul>
     */
    private static void init_chActiveChange(){
        msgClass.put(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,new HashMap<>());  // 创建MQ_CHANNEL_ACTIVE_CHANGE通道的映射
        HashMap map=msgClass.get(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE);  // 获取映射表
        map.put(MQEnum.CH_OPEN, MsgChOpenClose.class);  // 通道打开消息类
        map.put(MQEnum.CH_CLOSE, MsgChOpenClose.class);  // 通道关闭消息类
        map.put(MQEnum.CH_ACTIVE, MsgChActiveChange.class);  // 通道激活消息类
    }

    /**
     * 初始化通道参数变化消息映射
     * 
     * <p>建立通道参数变化相关的消息类映射。
     * 包括：反相、耦合、探头、带宽、阻抗、标签、延迟、偏移、微调、位置、扩展等。
     * 当前已注释，暂未使用。
     */
    private static void init_ChParamChange(){
//        msgClass.put(RxEnum.CHANNEL_PARAM_CHANGE,new HashMap<>());  // 创建CHANNEL_PARAM_CHANGE通道的映射
//        HashMap map=msgClass.get(RxEnum.CHANNEL_PARAM_CHANGE);  // 获取映射表
//        map.put(MQEnum.CH_INVERT, MsgChParamChange.class);  // 反相消息
//        map.put(MQEnum.CH_COUPLE,MsgChParamChange.class);  // 耦合消息
//        map.put(MQEnum.CH_PROBE,MsgChParamChange.class);  // 探头消息
//        map.put(MQEnum.CH_PROBE_VALUE,MsgChParamChange.class);  // 探头值消息
//        map.put(MQEnum.CH_BANDWIDTH,MsgChParamChange.class);  // 带宽消息
//        map.put(MQEnum.CH_BANDWIDTH_VALUE,MsgChParamChange.class);  // 带宽值消息
//        map.put(MQEnum.CH_IMP,MsgChParamChange.class);  // 阻抗消息
//        map.put(MQEnum.CH_V_SCALE_REF,MsgChParamChange.class);  // 垂直档位参考消息
//        map.put(MQEnum.CH_LABEL,MsgChParamChange.class);  // 标签消息
//        map.put(MQEnum.CH_DELAY,MsgChParamChange.class);  // 延迟消息
//        map.put(MQEnum.CH_OFFSET,MsgChParamChange.class);  // 偏移消息
//        map.put(MQEnum.CH_FINE,MsgChParamChange.class);  // 微调消息
//        map.put(MQEnum.CH_FINE_VALUE,MsgChParamChange.class);  // 微调值消息
//        map.put(MQEnum.CH_POSITION, MsgChParamChange.class);  // 位置消息
//        map.put(MQEnum.CH_EXTENT,MsgChParamChange.class);  // 扩展消息
    }

    /**
     * 初始化触发变化消息映射
     * 
     * <p>建立触发变化相关的消息类映射。
     * 包括：触发源变化、触发类型变化、触发电平变化、触发移动完成、阈值电平变化、阈值源变化等。
     * 当前已注释，暂未使用。
     */
    private  static void init_TriggerChange(){
//        msgClass.put(RxEnum.TRIGGER_CHANGE,new HashMap<>());  // 创建TRIGGER_CHANGE通道的映射
//        HashMap map=msgClass.get(RxEnum.TRIGGER_CHANGE);  // 获取映射表
//        map.put(MQEnum.TRIGGER_SRC_CHANGE, MsgTriggerChange.class);  // 触发源变化消息
//        map.put(MQEnum.TRIGGER_TYPE_CHANGE, MsgTriggerChange.class);  // 触发类型变化消息
//        map.put(MQEnum.TRIGGER_LEVEL_CHANGE,MsgTriggerChange.class);  // 触发电平变化消息
//        map.put(MQEnum.TRIGGER_MOVE_COMPLETE,MsgTriggerChange.class);  // 触发移动完成消息
//        map.put(MQEnum.THRESHOLD_LEVEL_CHANGE,MsgTriggerChange.class);  // 阈值电平变化消息
//        map.put(MQEnum.THRESHOLD_SRC_CHANGE,MsgTriggerChange.class);  // 阈值源变化消息
    }
    
    /**
     * 初始化数学通道参数变化消息映射
     * 
     * <p>建立数学通道参数变化相关的消息类映射。
     * 包括：打开/关闭、类型、双波形源、双波形运算符、FFT类型/源/窗函数、表达式等。
     * 当前已注释，暂未使用。
     */
    private static void init_MathParamChange(){
//        msgClass.put(RxEnum.MATH_PARAM_CHANGE,new HashMap<>());  // 创建MATH_PARAM_CHANGE通道的映射
//        HashMap map=msgClass.get(RxEnum.MATH_PARAM_CHANGE);  // 获取映射表
//        map.put(MQEnum.MATH_OPEN, MsgMathParamChange.class);  // 数学通道打开消息
//        map.put(MQEnum.MATH_CLOSE, MsgMathParamChange.class);  // 数学通道关闭消息
//        map.put(MQEnum.MATH_TYPE, MsgMathParamChange.class);  // 数学类型消息
//        map.put(MQEnum.MATH_DW_SOURCE1, MsgMathParamChange.class);  // 双波形源1消息
//        map.put(MQEnum.MATH_DW_SOURCE2, MsgMathParamChange.class);  // 双波形源2消息
//        map.put(MQEnum.MATH_DW_OPERATOR, MsgMathParamChange.class);  // 双波形运算符消息
//        map.put(MQEnum.MATH_FFT_TYPE, MsgMathParamChange.class);  // FFT类型消息
//        map.put(MQEnum.MATH_FFT_SOURCE, MsgMathParamChange.class);  // FFT源消息
//        map.put(MQEnum.MATH_FFT_WINDOW, MsgMathParamChange.class);  // FFT窗函数消息
//        map.put(MQEnum.MATH_FFT_PERSIST_TYPE, MsgMathParamChange.class);  // FFT持续类型消息
//        map.put(MQEnum.MATH_FFT_PERSIST_VALUE, MsgMathParamChange.class);  // FFT持续值消息
//        map.put(MQEnum.MATH_AXB_UNIT, MsgMathParamChange.class);  // A*B单位消息
//        map.put(MQEnum.MATH_AXB_SOUCE, MsgMathParamChange.class);  // A*B源消息
//        map.put(MQEnum.MATH_AXB_A, MsgMathParamChange.class);  // A*B的A值消息
//        map.put(MQEnum.MATH_AXB_B, MsgMathParamChange.class);  // A*B的B值消息
//        map.put(MQEnum.MATH_ADV_EXPRESS, MsgMathParamChange.class);  // 高级表达式消息
//        map.put(MQEnum.MATH_ADV_VALUE1, MsgMathParamChange.class);  // 高级值1消息
//        map.put(MQEnum.MATH_ADV_VALUE2, MsgMathParamChange.class);  // 高级值2消息
//        map.put(MQEnum.MATH_ADV_UNIT, MsgMathParamChange.class);  // 高级单位消息
//        map.put(MQEnum.MATH_VERMODE, MsgMathParamChange.class);  // 垂直模式消息
//        map.put(MQEnum.MATH_POSITION,MsgMathParamChange.class);  // 位置消息
//        map.put(MQEnum.MATH_EXTENT,MsgMathParamChange.class);  // 扩展消息
    }
    
    /**
     * 初始化参考通道参数变化消息映射
     * 
     * <p>建立参考通道参数变化相关的消息类映射。
     * 包括：打开/关闭、扩展、位置、时基X、时基扩展等。
     * 当前已注释，暂未使用。
     */
    private static void init_RefParamChange(){
//        msgClass.put(RxEnum.REF_PARAM_CHANGE,new HashMap<>());  // 创建REF_PARAM_CHANGE通道的映射
//        HashMap map=msgClass.get(RxEnum.REF_PARAM_CHANGE);  // 获取映射表
//        map.put(MQEnum.REF_OPEN, MsgRefParamChange.class);  // 参考通道打开消息
//        map.put(MQEnum.REF_CLOSE, MsgRefParamChange.class);  // 参考通道关闭消息
//        map.put(MQEnum.REF_EXTENT, MsgRefParamChange.class);  // 参考扩展消息
//        map.put(MQEnum.REF_POSITION,MsgRefParamChange.class);  // 参考位置消息
//        map.put(MQEnum.REF_TIMEBASE_X,MsgRefParamChange.class);  // 参考时基X消息
//        map.put(MQEnum.REF_TIMEBASE_EXTENT,MsgRefParamChange.class);  // 参考时基扩展消息
    }
    
    /**
     * 初始化串口参数变化消息映射
     * 
     * <p>建立串口参数变化相关的消息类映射。
     * 支持多种串口协议：UART、LIN、CAN、SPI、I2C、ARINC429、MIL-STD-1553B。
     * 当前已注释，暂未使用。
     */
    private static void init_SerialParamChange() {
//        msgClass.put(RxEnum.SERIAL_PARAM_CHANGE, new HashMap<>());  // 创建SERIAL_PARAM_CHANGE通道的映射
//        HashMap map = msgClass.get(RxEnum.SERIAL_PARAM_CHANGE);  // 获取映射表
//        map.put(MQEnum.SERIAL_OPEN, MsgSerialParamChange.class);  // 串口打开消息
//        map.put(MQEnum.SERIAL_CLOSE, MsgSerialParamChange.class);  // 串口关闭消息
//        map.put(MQEnum.SERIAL_TYPE, MsgSerialParamChange.class);  // 串口类型消息
//        // UART相关消息
//        map.put(MQEnum.SERIAL_UART_SOURCE, MsgSerialParamChange.class);  // UART源消息
//        map.put(MQEnum.SERIAL_UART_IDLE_LEVEL, MsgSerialParamChange.class);  // UART空闲电平消息
//        map.put(MQEnum.SERIAL_UART_CHECK, MsgSerialParamChange.class);  // UART校验消息
//        map.put(MQEnum.SERIAL_UART_BIT, MsgSerialParamChange.class);  // UART数据位消息
//        map.put(MQEnum.SERIAL_UART_BAUD_RATE, MsgSerialParamChange.class);  // UART波特率消息
//        map.put(MQEnum.SERIAL_UART_DISPLAY_FORMAT, MsgSerialParamChange.class);  // UART显示格式消息
//        // LIN相关消息
//        map.put(MQEnum.SERIAL_LIN_SOURCE, MsgSerialParamChange.class);  // LIN源消息
//        map.put(MQEnum.SERIAL_LIN_IDLE_LEVEL, MsgSerialParamChange.class);  // LIN空闲电平消息
//        map.put(MQEnum.SERIAL_LIN_BAUD_RATE, MsgSerialParamChange.class);  // LIN波特率消息
//        // CAN相关消息
//        map.put(MQEnum.SERIAL_CAN_SOURCE, MsgSerialParamChange.class);  // CAN源消息
//        map.put(MQEnum.SERIAL_CAN_SIGNAL_TYPE, MsgSerialParamChange.class);  // CAN信号类型消息
//        map.put(MQEnum.SERIAL_CAN_BAUD_RATE, MsgSerialParamChange.class);  // CAN波特率消息
//        map.put(MQEnum.SERIAL_CAN_BAUD_RATE_PERCENT, MsgSerialParamChange.class);  // CAN波特率百分比消息
//        map.put(MQEnum.SERIAL_CAN_FD_BAUD_RATE, MsgSerialParamChange.class);  // CAN FD波特率消息
//        map.put(MQEnum.SERIAL_CAN_FD_BAUD_RATE_PERCENT, MsgSerialParamChange.class);  // CAN FD波特率百分比消息
//        map.put(MQEnum.SERIAL_CAN_STANDARD, MsgSerialParamChange.class);  // CAN标准消息
//        // SPI相关消息
//        map.put(MQEnum.SERIAL_SPI_CLOCK_SOURCE, MsgSerialParamChange.class);  // SPI时钟源消息
//        map.put(MQEnum.SERIAL_SPI_CLOCK_LEVEL, MsgSerialParamChange.class);  // SPI时钟电平消息
//        map.put(MQEnum.SERIAL_SPI_DATA, MsgSerialParamChange.class);  // SPI数据消息
//        map.put(MQEnum.SERIAL_SPI_DATA_LEVEL, MsgSerialParamChange.class);  // SPI数据电平消息
//        map.put(MQEnum.SERIAL_SPI_CS_ENABLE, MsgSerialParamChange.class);  // SPI片选使能消息
//        map.put(MQEnum.SERIAL_SPI_CS_SOURCE, MsgSerialParamChange.class);  // SPI片选源消息
//        map.put(MQEnum.SERIAL_SPI_CS_LEVEL, MsgSerialParamChange.class);  // SPI片选电平消息
//        map.put(MQEnum.SERIAL_SPI_BIT, MsgSerialParamChange.class);  // SPI数据位消息
//        // I2C相关消息
//        map.put(MQEnum.SERIAL_I2C_DATA_SOURCE, MsgSerialParamChange.class);  // I2C数据源消息
//        map.put(MQEnum.SERIAL_I2C_CLOCK_SOURCE, MsgSerialParamChange.class);  // I2C时钟源消息
//        // ARINC429相关消息
//        map.put(MQEnum.SERIAL_A429_SOURCE, MsgSerialParamChange.class);  // ARINC429源消息
//        map.put(MQEnum.SERIAL_A429_FORMAT, MsgSerialParamChange.class);  // ARINC429格式消息
//        map.put(MQEnum.SERIAL_A429_DISPLAY_FORMAT, MsgSerialParamChange.class);  // ARINC429显示格式消息
//        map.put(MQEnum.SERIAL_A429_BAUD_RATE, MsgSerialParamChange.class);  // ARINC429波特率消息
//        // MIL-STD-1553B相关消息
//        map.put(MQEnum.SERIAL_M1553B_SOURCE, MsgSerialParamChange.class);  // M1553B源消息
//        map.put(MQEnum.SERIAL_M1553B_DISPLAY_FORMAT, MsgSerialParamChange.class);  // M1553B显示格式消息
    }


    /**
     * 解析消息的MQEnum类型
     * 
     * <p>从消息对象中提取RxEnum和MQEnum，然后在映射表中查找对应的MQEnum。
     * 
     * @param o 消息对象，必须是MQBase或其子类的实例
     * @return 对应的MQEnum，如果未找到则返回MQEnum.NULL
     */
    public static MQEnum parseMqEnum(Object o){
        RxEnum rxEnum= ((MQBase)o).getRxType();  // 获取消息的RxEnum类型
        MQEnum mqEnum= ((MQBase)o).getMqType();  // 获取消息的MQEnum类型
        HashMap<MQEnum,Class> classList=msgClass.get(rxEnum);  // 获取该RxEnum对应的映射表
        for(Map.Entry<MQEnum,Class> item:classList.entrySet() ){  // 遍历映射表
            if (item.getKey()==mqEnum){  // 检查是否匹配
                return item.getKey();  // 返回匹配的MQEnum
            }
        }
        return MQEnum.NULL;  // 未找到返回NULL
    }
    
    /**
     * 创建消息实例
     * 
     * <p>根据RxEnum和MQEnum创建对应的消息实例。
     * 使用反射机制动态创建消息类实例，并设置rxType和mqType属性。
     * 
     * <p><b>使用示例：</b>
     * <pre>
     * MsgChOpenClose msg = RxBusRegister.createMsg(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE, MQEnum.CH_OPEN);
     * RxBus.getInstance().post(msg);
     * </pre>
     * 
     * @param <T> 消息类型，继承自MQBase
     * @param rxEnum 消息通道枚举
     * @param mqEnum 消息子类型枚举
     * @return 创建的消息实例，如果创建失败则返回null
     * @throws RuntimeException 如果反射创建实例失败
     */
    public static <T> T createMsg(RxEnum rxEnum,MQEnum mqEnum){
        HashMap<MQEnum,Class> classList=msgClass.get(rxEnum);  // 获取该RxEnum对应的映射表
        for(Map.Entry<MQEnum,Class> item:classList.entrySet()){  // 遍历映射表
            if (mqEnum==item.getKey()){  // 检查是否匹配
                try {
                    MQBase base= (MQBase) item.getValue().newInstance();  // 使用反射创建消息实例
                    base.setMqType(mqEnum);  // 设置消息子类型
                    base.setRxType(rxEnum);  // 设置消息通道类型
                    return (T)base;  // 返回消息实例
                } catch (IllegalAccessException e) {  // 反射访问异常
                    throw new RuntimeException(e);  // 包装为运行时异常抛出
                } catch (InstantiationException e) {  // 实例化异常
                    throw new RuntimeException(e);  // 包装为运行时异常抛出
                }
            }
        }
        return null;  // 未找到对应的消息类，返回null
    }

}
