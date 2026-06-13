package com.micsig.tbook.scope.Bus;  // 定义包名：示波器总线分析模块

/**
 * ARINC429总线配置类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Bus（示波器总线分析模块）</li>
 *   <li>架构层级：协议解析层 - 总线配置</li>
 *   <li>设计模式：继承模式（继承自IBus基类）</li>
 *   <li>职责类型：ARINC429航空总线参数配置与触发控制</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>管理ARINC429总线的数据格式配置</li>
 *   <li>管理ARINC429总线的触发类型设置</li>
 *   <li>存储各触发类型的Label、SDI、Data、SSM参数</li>
 *   <li>配置波特率和显示格式</li>
 *   <li>管理源通道索引</li>
 * </ul>
 * 
 * <p><b>ARINC429协议简介：</b>
 * <ul>
 *   <li>ARINC429是航空电子数据总线标准，广泛应用于商用飞机</li>
 *   <li>单工串行通信，数据传输速率12.5Kbps或100Kbps</li>
 *   <li>32位数据字格式，包含Label、SDI、Data、SSM、奇偶校验</li>
 *   <li>用于航电设备间的数据交换，如飞行控制、导航、发动机参数等</li>
 * </ul>
 * 
 * <p><b>ARINC429数据字格式（32位）：</b>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                    ARINC429 32位数据字结构                              │
 * │                                                                         │
 * │  Bit 31      Bit 30-29    Bit 28-11        Bit 10-9      Bit 8-1       │
 * │  ┌─────┐    ┌───────┐    ┌───────────┐    ┌───────┐    ┌───────────┐   │
 * │  │Parity│    │  SSM  │    │   Data    │    │  SDI  │    │   Label   │   │
 * │  │ 奇偶 │    │符号状态│    │  19位数据 │    │源/目标│    │  8位标签  │   │
 * │  └─────┘    └───────┘    └───────────┘    └───────┘    └───────────┘   │
 * │                                                                         │
 * │  Label (8位): 标识数据类型，如高度、速度、航向等                         │
 * │  SDI (2位): 源/目标标识，用于多设备寻址                                  │
 * │  Data (19位): 实际数据内容，根据Label定义解析                            │
 * │  SSM (2位): 符号/状态矩阵，表示数据符号和有效性                          │
 * │  Parity (1位): 奇偶校验位                                               │
 * └─────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>数据格式类型：</b>
 * <pre>
 * ┌────────────────────────────────┬────────────────────────────────────────┐
 * │ 格式常量                       │ 格式说明                               │
 * ├────────────────────────────────┼────────────────────────────────────────┤
 * │ ARINC429_LABEL_SDI_DATA_SSM    │ 完整格式：Label+SDI+Data+SSM           │
 * │ ARINC429_LABEL_DATA_SSM        │ 简化格式：Label+Data+SSM               │
 * │ ARINC429_LABEL_DATA            │ 最简格式：Label+Data                   │
 * └────────────────────────────────┴────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>触发类型说明：</b>
 * <pre>
 * ┌────────────────────────────────┬────────────────────────────────────────┐
 * │ 触发类型常量                   │ 触发说明                               │
 * ├────────────────────────────────┼────────────────────────────────────────┤
 * │ ARINC429_TRIGGER_WORD_BEGIN    │ 字起始触发：数据字开始时触发           │
 * │ ARINC429_TRIGGER_WORD_END      │ 字结束触发：数据字结束时触发           │
 * │ ARINC429_TRIGGER_WORD          │ 字触发：匹配完整数据字                 │
 * │ ARINC429_TRIGGER_LABEL         │ Label触发：匹配标签字段                │
 * │ ARINC429_TRIGGER_SDI           │ SDI触发：匹配源/目标标识               │
 * │ ARINC429_TRIGGER_DATA          │ Data触发：匹配数据字段                 │
 * │ ARINC429_TRIGGER_SSM           │ SSM触发：匹配符号状态矩阵              │
 * │ ARINC429_TRIGGER_LABEL_SDI     │ Label+SDI触发：匹配标签和源标识        │
 * │ ARINC429_TRIGGER_LABEL_DATA    │ Label+Data触发：匹配标签和数据         │
 * │ ARINC429_TRIGGER_LABEL_SSM     │ Label+SSM触发：匹配标签和状态          │
 * │ ARINC429_TRIGGER_WORD_ERROR    │ 字错误触发：数据字校验错误             │
 * │ ARINC429_TRIGGER_WORD_INTERVAL │ 字间隔触发：字间间隔超时               │
 * │ ARINC429_TRIGGER_VERIFY_ERROR  │ 校验错误触发：奇偶校验错误             │
 * │ ARINC429_TRIGGER_ALL_ERROR     │ 全错误触发：任何错误                   │
 * │ ARINC429_TRIGGER_ALL_0         │ 全0触发：数据字全为0                   │
 * │ ARINC429_TRIGGER_ALL_1         │ 全1触发：数据字全为1                   │
 * └────────────────────────────────┴────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：IBus（总线基类）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <pre>
 * // 创建ARINC429总线配置
 * ARINC429Bus bus = new ARINC429Bus(0);
 * 
 * // 设置源通道
 * bus.setSrcChIdx(1);
 * 
 * // 设置波特率（100Kbps）
 * bus.setBaudRate(100 * 1000);
 * 
 * // 设置触发类型为Label触发
 * bus.setTriggerType(ARINC429Bus.ARINC429_TRIGGER_LABEL);
 * 
 * // 设置Label值
 * bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL, 0x55);
 * </pre>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-5-29
 * @see IBus 总线基类
 */
public class ARINC429Bus extends IBus {

    // ==================== 数据格式常量定义 ====================
    
    /**
     * 数据格式：Label + SDI + Data + SSM（完整格式）
     * 
     * <p><b>格式说明：</b>
     * <ul>
     *   <li>显示完整的ARINC429数据字结构</li>
     *   <li>包含所有字段：标签、源标识、数据、状态</li>
     * </ul>
     */
    public static final int ARINC429_LABEL_SDI_DATA_SSM = 0;  // 完整格式：Label+SDI+Data+SSM
    
    /**
     * 数据格式：Label + Data + SSM（简化格式）
     * 
     * <p><b>格式说明：</b>
     * <ul>
     *   <li>不显示SDI字段</li>
     *   <li>适用于单设备场景</li>
     * </ul>
     */
    public static final int ARINC429_LABEL_DATA_SSM = 1;  // 简化格式：Label+Data+SSM
    
    /**
     * 数据格式：Label + Data（最简格式）
     * 
     * <p><b>格式说明：</b>
     * <ul>
     *   <li>只显示标签和数据字段</li>
     *   <li>适用于快速查看数据内容</li>
     * </ul>
     */
    public static final int ARINC429_LABEL_DATA = 2;  // 最简格式：Label+Data

    // ==================== 触发类型常量定义 ====================
    
    /**
     * 触发类型：字起始触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>检测到ARINC429数据字开始传输时触发</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_WORD_BEGIN = 1;  // 字起始触发
    
    /**
     * 触发类型：字结束触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>检测到ARINC429数据字传输结束时触发</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_WORD_END = 2;  // 字结束触发
    
    /**
     * 触发类型：字触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>匹配完整的32位数据字时触发</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_WORD = 3;  // 字触发：匹配完整数据字
    
    /**
     * 触发类型：Label触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>匹配Label字段（8位）时触发</li>
     *   <li>Label用于标识数据类型</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_LABEL = 4;  // Label触发：匹配标签字段
    
    /**
     * 触发类型：SDI触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>匹配SDI字段（2位）时触发</li>
     *   <li>SDI用于源/目标设备标识</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_SDI = 5;  // SDI触发：匹配源/目标标识
    
    /**
     * 触发类型：Data触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>匹配Data字段（19位）时触发</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_DATA = 6;  // Data触发：匹配数据字段
    
    /**
     * 触发类型：SSM触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>匹配SSM字段（2位）时触发</li>
     *   <li>SSM表示数据符号和有效性</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_SSM = 7;  // SSM触发：匹配符号状态矩阵
    
    /**
     * 触发类型：Label+SDI触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>同时匹配Label和SDI字段时触发</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_LABEL_SDI = 8;  // Label+SDI触发
    
    /**
     * 触发类型：Label+Data触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>同时匹配Label和Data字段时触发</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_LABEL_DATA = 9;  // Label+Data触发
    
    /**
     * 触发类型：Label+SSM触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>同时匹配Label和SSM字段时触发</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_LABEL_SSM = 10;  // Label+SSM触发
    
    /**
     * 触发类型：字错误触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>检测到数据字格式错误时触发</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_WORD_ERROR = 11;  // 字错误触发
    
    /**
     * 触发类型：字间隔触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>检测到数据字间隔超时时触发</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_WORD_INTERVAL = 12;  // 字间隔触发
    
    /**
     * 触发类型：校验错误触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>奇偶校验错误时触发</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_VERIFY_ERROR = 13;  // 校验错误触发
    
    /**
     * 触发类型：全错误触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>任何类型的错误时触发</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_ALL_ERROR = 14;  // 全错误触发
    
    /**
     * 触发类型：全0触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>数据字所有位都为0时触发</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_ALL_0 = 15;  // 全0触发
    
    /**
     * 触发类型：全1触发
     * 
     * <p><b>触发条件：</b>
     * <ul>
     *   <li>数据字所有位都为1时触发</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_ALL_1 = 16;  // 全1触发
    
    /**
     * 触发类型最大值
     * 
     * <p><b>用途：</b>
     * <ul>
     *   <li>用于触发类型数组的大小定义</li>
     *   <li>用于触发类型有效性检查</li>
     * </ul>
     */
    public static final int ARINC429_TRIGGER_MAX = 17;  // 触发类型最大值，用于数组大小和边界检查

    // ==================== 成员变量 ====================
    
    /**
     * 源通道索引
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>指定ARINC429信号输入的物理通道</li>
     *   <li>取值范围：0 ~ 通道数-1</li>
     * </ul>
     */
    private int srcChIdx = 0;  // 源通道索引，默认为0
    
    /**
     * 数据格式
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>决定ARINC429数据的显示格式</li>
     *   <li>取值：ARINC429_LABEL_SDI_DATA_SSM、ARINC429_LABEL_DATA_SSM、ARINC429_LABEL_DATA</li>
     * </ul>
     */
    private int format = ARINC429_LABEL_SDI_DATA_SSM;  // 数据格式，默认为完整格式
    
    /**
     * 显示格式
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>决定数据的显示进制（十六进制/十进制/二进制等）</li>
     *   <li>继承自IBus的DISPLAY_HEX_DISPLAY常量</li>
     * </ul>
     */
    private int displayFormat = DISPLAY_HEX_DISPLAY;  // 显示格式，默认为十六进制显示
    
    /**
     * 波特率
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>ARINC429总线通信速率</li>
     *   <li>标准速率：12.5Kbps（低速）或100Kbps（高速）</li>
     *   <li>默认值：100Kbps</li>
     * </ul>
     */
    private int baudRate = 100*1000;  // 波特率，默认100Kbps
    
    /**
     * 触发类型
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>当前选择的触发类型</li>
     *   <li>取值范围：ARINC429_TRIGGER_WORD_BEGIN ~ ARINC429_TRIGGER_ALL_1</li>
     * </ul>
     */
    private int triggerType = ARINC429_TRIGGER_WORD_BEGIN;  // 触发类型，默认为字起始触发
    
    /**
     * 完整数据字
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于WORD触发类型的匹配值</li>
     *   <li>32位完整数据字</li>
     * </ul>
     */
    private int word = 0;  // 完整数据字，默认为0
    
    /**
     * Label数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储各触发类型对应的Label值</li>
     *   <li>数组索引为触发类型，值为Label</li>
     *   <li>Label为8位，取值范围：0~255</li>
     * </ul>
     */
    private int []label = new int[ARINC429_TRIGGER_MAX];  // Label数组，按触发类型索引
    
    /**
     * SDI数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储各触发类型对应的SDI值</li>
     *   <li>SDI为2位，取值范围：0~3</li>
     *   <li>用于源/目标设备标识</li>
     * </ul>
     */
    private int []sdi = new int[ARINC429_TRIGGER_MAX];  // SDI数组，按触发类型索引
    
    /**
     * Data数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储各触发类型对应的Data值</li>
     *   <li>Data为19位，取值范围：0~0x7FFFF</li>
     * </ul>
     */
    private int []data = new int[ARINC429_TRIGGER_MAX];  // Data数组，按触发类型索引
    
    /**
     * SSM数组
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储各触发类型对应的SSM值</li>
     *   <li>SSM为2位，取值范围：0~3</li>
     *   <li>表示数据符号和有效性状态</li>
     * </ul>
     */
    private int []SSM = new int[ARINC429_TRIGGER_MAX];  // SSM数组，按触发类型索引

    // ==================== 静态方法 ====================
    
    /**
     * 检查触发类型是否有效
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>验证触发类型是否在有效范围内</li>
     *   <li>有效范围：ARINC429_TRIGGER_WORD_BEGIN ~ ARINC429_TRIGGER_MAX-1</li>
     * </ul>
     * 
     * @param triggerType 触发类型值
     * @return true表示有效，false表示无效
     */
    public static boolean isTriggerTypeValid(int triggerType){
        return triggerType >= ARINC429_TRIGGER_WORD_BEGIN && triggerType<ARINC429_TRIGGER_MAX;  // 检查触发类型是否在有效范围内
    }

    // ==================== 构造函数 ====================
    
    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置总线索引和总线类型</li>
     *   <li>总线类型为ARINC429</li>
     * </ul>
     * 
     * @param busIdx 总线索引，用于标识不同的总线实例
     */
    public ARINC429Bus(int busIdx) {
        super(busIdx,ARINC429);  // 调用父类构造函数，设置总线索引和类型
    }

    // ==================== Getter/Setter方法 ====================
    
    /**
     * 获取源通道索引
     * 
     * @return 源通道索引
     */
    public int getSrcChIdx() {
        return srcChIdx;  // 返回源通道索引
    }

    /**
     * 设置源通道索引
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置ARINC429信号的输入通道</li>
     *   <li>设置后触发通道变化通知</li>
     * </ul>
     * 
     * @param srcChIdx 源通道索引
     */
    public void setSrcChIdx(int srcChIdx) {
        this.srcChIdx = srcChIdx;  // 设置源通道索引
        chChange();  // 触发通道变化通知
    }

    /**
     * 获取数据格式
     * 
     * @return 数据格式（ARINC429_LABEL_SDI_DATA_SSM、ARINC429_LABEL_DATA_SSM或ARINC429_LABEL_DATA）
     */
    public int getFormat() {
        return format;  // 返回数据格式
    }

    /**
     * 设置数据格式
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置ARINC429数据的显示格式</li>
     *   <li>设置后触发总线变化通知</li>
     * </ul>
     * 
     * @param format 数据格式
     */
    public void setFormat(int format) {
        this.format = format;  // 设置数据格式
        busChange();  // 触发总线变化通知
    }

    /**
     * 获取显示格式
     * 
     * @return 显示格式（十六进制/十进制/二进制等）
     */
    public int getDisplayFormat() {
        return displayFormat;  // 返回显示格式
    }

    /**
     * 设置显示格式
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置数据的显示进制</li>
     *   <li>不触发总线变化通知</li>
     * </ul>
     * 
     * @param displayFormat 显示格式
     */
    public void setDisplayFormat(int displayFormat) {
        this.displayFormat = displayFormat;  // 设置显示格式
    }

    /**
     * 获取波特率
     * 
     * @return 波特率（单位：bps）
     */
    public int getBaudRate() {
        return baudRate;  // 返回波特率
    }

    /**
     * 设置波特率
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置ARINC429总线通信速率</li>
     *   <li>标准值：12500（低速）或100000（高速）</li>
     *   <li>设置后触发总线变化通知</li>
     * </ul>
     * 
     * @param baudRate 波特率（单位：bps）
     */
    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;  // 设置波特率
        busChange();  // 触发总线变化通知
    }

    /**
     * 获取触发类型
     * 
     * @return 当前触发类型
     */
    public int getTriggerType() {
        return triggerType;  // 返回触发类型
    }

    /**
     * 设置触发类型
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置ARINC429总线的触发类型</li>
     *   <li>仅当触发类型有效时才设置</li>
     *   <li>设置后触发总线变化通知</li>
     * </ul>
     * 
     * @param triggerType 触发类型
     */
    public void setTriggerType(int triggerType) {
        if(isTriggerTypeValid(triggerType)) {  // 检查触发类型是否有效
            this.triggerType = triggerType;  // 设置触发类型
            busChange();  // 触发总线变化通知
        }
    }

    /**
     * 获取完整数据字
     * 
     * @return 完整数据字（32位）
     */
    public int getWord() {
        return word;  // 返回完整数据字
    }

    /**
     * 设置完整数据字
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置用于WORD触发类型的匹配值</li>
     *   <li>设置后触发总线变化通知</li>
     * </ul>
     * 
     * @param word 完整数据字（32位）
     */
    public void setWord(int word) {
        this.word = word;  // 设置完整数据字
        busChange();  // 触发总线变化通知
    }

    /**
     * 获取指定触发类型的Label值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取指定触发类型对应的Label匹配值</li>
     *   <li>如果触发类型无效，返回0</li>
     * </ul>
     * 
     * @param triggerType 触发类型
     * @return Label值（8位，0~255）
     */
    public int getLabel(int triggerType) {
        if(isTriggerTypeValid(triggerType))  // 检查触发类型是否有效
            return label[triggerType];  // 返回对应触发类型的Label值
        return 0;  // 触发类型无效时返回0
    }

    /**
     * 设置指定触发类型的Label值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置指定触发类型对应的Label匹配值</li>
     *   <li>仅当触发类型有效时才设置</li>
     *   <li>设置后触发总线变化通知</li>
     * </ul>
     * 
     * @param triggerType 触发类型
     * @param label Label值（8位，0~255）
     */
    public void setLabel(int triggerType,int label) {
        if(isTriggerTypeValid(triggerType)) {  // 检查触发类型是否有效
            this.label[triggerType] = label;  // 设置对应触发类型的Label值
            busChange();  // 触发总线变化通知
        }
    }

    /**
     * 获取指定触发类型的SDI值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取指定触发类型对应的SDI匹配值</li>
     *   <li>如果触发类型无效，返回0</li>
     * </ul>
     * 
     * @param triggerType 触发类型
     * @return SDI值（2位，0~3）
     */
    public int getSdi(int triggerType) {
        if(isTriggerTypeValid(triggerType))  // 检查触发类型是否有效
            return sdi[triggerType];  // 返回对应触发类型的SDI值
        return 0;  // 触发类型无效时返回0
    }

    /**
     * 设置指定触发类型的SDI值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置指定触发类型对应的SDI匹配值</li>
     *   <li>仅当触发类型有效时才设置</li>
     *   <li>设置后触发总线变化通知</li>
     * </ul>
     * 
     * @param triggerType 触发类型
     * @param sdi SDI值（2位，0~3）
     */
    public void setSdi(int triggerType,int sdi) {

        if(isTriggerTypeValid(triggerType)) {  // 检查触发类型是否有效
            this.sdi[triggerType] = sdi;  // 设置对应触发类型的SDI值
            busChange();  // 触发总线变化通知
        }
    }

    /**
     * 获取指定触发类型的Data值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取指定触发类型对应的Data匹配值</li>
     *   <li>如果触发类型无效，返回0</li>
     * </ul>
     * 
     * @param triggerType 触发类型
     * @return Data值（19位，0~0x7FFFF）
     */
    public int getData(int triggerType) {
        if(isTriggerTypeValid(triggerType))  // 检查触发类型是否有效
            return data[triggerType];  // 返回对应触发类型的Data值
        return 0;  // 触发类型无效时返回0
    }

    /**
     * 设置指定触发类型的Data值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置指定触发类型对应的Data匹配值</li>
     *   <li>仅当触发类型有效时才设置</li>
     *   <li>设置后触发总线变化通知</li>
     * </ul>
     * 
     * @param triggerType 触发类型
     * @param data Data值（19位，0~0x7FFFF）
     */
    public void setData(int triggerType,int data) {
        if(isTriggerTypeValid(triggerType)) {  // 检查触发类型是否有效
            this.data[triggerType] = data;  // 设置对应触发类型的Data值
            busChange();  // 触发总线变化通知
        }
    }

    /**
     * 获取指定触发类型的SSM值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取指定触发类型对应的SSM匹配值</li>
     *   <li>如果触发类型无效，返回0</li>
     * </ul>
     * 
     * @param triggerType 触发类型
     * @return SSM值（2位，0~3）
     */
    public int getSSM(int triggerType) {
        if(isTriggerTypeValid(triggerType))  // 检查触发类型是否有效
            return SSM[triggerType];  // 返回对应触发类型的SSM值
        return 0;  // 触发类型无效时返回0
    }

    /**
     * 设置指定触发类型的SSM值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置指定触发类型对应的SSM匹配值</li>
     *   <li>仅当触发类型有效时才设置</li>
     *   <li>设置后触发总线变化通知</li>
     * </ul>
     * 
     * @param triggerType 触发类型
     * @param SSM SSM值（2位，0~3）
     */
    public void setSSM(int triggerType,int SSM) {
        if(isTriggerTypeValid(triggerType)) {  // 检查触发类型是否有效
            this.SSM[triggerType] = SSM;  // 设置对应触发类型的SSM值
            busChange();  // 触发总线变化通知
        }
    }

    // ==================== 重写父类方法 ====================
    
    /**
     * 获取通道采样数量
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>ARINC429总线只需要1个通道</li>
     *   <li>返回1表示单通道采样</li>
     * </ul>
     * 
     * @return 通道采样数量，固定返回1
     */
    @Override
    public int getChSampleCnt() {
        return 1;  // ARINC429总线只需要1个通道
    }

    /**
     * 检查指定通道是否参与采样
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>判断指定通道是否为ARINC429信号的源通道</li>
     *   <li>只有源通道参与采样</li>
     * </ul>
     * 
     * @param chIdx 通道索引
     * @return true表示该通道参与采样，false表示不参与
     */
    @Override
    public boolean isChInSample(int chIdx) {
        return srcChIdx == chIdx;  // 只有源通道参与采样
    }
}
