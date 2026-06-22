package com.micsig.smart; // 智能属性管理包，包含示波器设备属性定义与管理核心类

/**
 * ╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗
 * ║                                              Property - 设备属性数据模型                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╣
 * ║  【模块定位】                                                                                                        ║
 * ║  MHO系列示波器智能属性数据模型，定义设备属性的数据结构和操作接口，是智能属性管理模块的核心数据载体。                    ║
 * ║                                                                                                                      ║
 * ║  【核心职责】                                                                                                        ║
 * ║  1. 属性数据存储：封装设备UUID、SN、带宽、存储深度、保修日期等核心属性                                               ║
 * ║  2. 功能授权管理：管理总线解码、语言包、高级功能（频率计、HDMI、WiFi等）的授权状态                                    ║
 * ║  3. 数据持久化：通过JNI接口实现属性数据的序列化与反序列化，支持EEPROM存储                                            ║
 * ║  4. 序列号升级：支持通过序列码升级设备功能授权                                                                        ║
 * ║                                                                                                                      ║
 * ║  【架构设计】                                                                                                        ║
 * ║  采用数据模型模式，通过JNI调用Native层实现数据解析与序列化，Java层仅负责数据封装和访问接口。                           ║
 * ║  属性数据存储在EEPROM中，通过PropertyManage进行统一管理。                                                            ║
 * ║                                                                                                                      ║
 * ║  【数据流向】                                                                                                        ║
 * ║  ┌───────────────┐    ┌───────────────┐    ┌───────────────┐    ┌───────────────┐                                   ║
 * ║  │   EEPROM      │───→│PropertyManage │───→│   Property    │───→│   业务层      │                                   ║
 * ║  └───────────────┘    └───────────────┘    └───────────────┘    └───────────────┘                                   ║
 * ║         ↑                    │                    │                                                                 ║
 * ║         │                    ↓                    │                                                                 ║
 * ║         └────────────────────┴────────────────────┘                                                                 ║
 * ║                      JNI Native Layer                                                                               ║
 * ║                                                                                                                      ║
 * ║  【属性分类】                                                                                                        ║
 * ║  ┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐║
 * ║  │ 基础属性：UUID、SN、显示SN、型号、发货日期、OEM名称、硬件版本                                                   │║
 * ║  │ 性能属性：带宽、存储深度、高刷新率、保修日期                                                                     │║
 * ║  │ 功能授权：频率计、HDMI、500uV、自动量程、WiFi、汽车总线、高低通滤波、按键光标                                   │║
 * ║  │ 总线授权：UART、LIN、SPI、CAN、I2C、1553B、429、CAN-FD                                                          │║
 * ║  │ 语言授权：12种语言（英语、简中、繁中、德语、俄语、西班牙语、韩语、捷克语、阿拉伯语、意大利语、土耳其语、法语）    │║
 * ║  └─────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘║
 * ║                                                                                                                      ║
 * ║  【依赖关系】                                                                                                        ║
 * ║  - 依赖：property-lib（JNI Native库）                                                                                ║
 * ║  - 被依赖：PropertyManage（属性管理器）                                                                               ║
 * ║                                                                                                                      ║
 * ║  【使用示例】                                                                                                        ║
 * ║  Property prop = new Property();                                                                                    ║
 * ║  prop.initProperty(eepromData);  // 从EEPROM数据初始化                                                               ║
 * ║  int bandwidth = prop.getBandWidth();  // 获取带宽                                                                  ║
 * ║  boolean canEnabled = prop.isEnableBus(Property.BUS_CAN);  // 检查CAN总线授权                                       ║
 * ║  prop.serialCodeUpgrade("XXXX-XXXX");  // 通过序列码升级                                                            ║
 * ║                                                                                                                      ║
 * ║  【线程安全】                                                                                                        ║
 * ║  本类非线程安全，外部调用需通过PropertyManage进行同步控制。                                                          ║
 * ╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝
 *
 * @author zhuzh
 * @version 2.0
 * @since 2018-07-06
 */
public class Property {

    // ==================== 总线类型常量定义 ====================

    /**
     * 总线类型：串行总线（UART）
     * 通用异步收发传输器，最常用的串行通信协议
     */
    public static final int BUS_UART  = 0;  // 串型总线常量，索引值0

    /**
     * 总线类型：LIN总线
     * 局部互联网络，用于汽车电子系统
     */
    public static final int BUS_LIN  = 1;   // LIN总线常量，索引值1

    /**
     * 总线类型：SPI总线
     * 串行外设接口，高速同步串行通信
     */
    public static final int BUS_SPI  = 2;   // SPI总线常量，索引值2

    /**
     * 总线类型：CAN总线
     * 控制器局域网，汽车电子标准总线
     */
    public static final int BUS_CAN  = 3;   // CAN总线常量，索引值3

    /**
     * 总线类型：I2C总线
     * 两线式串行总线，用于短距离通信
     */
    public static final int BUS_I2C  = 4;   // I2C总线常量，索引值4

    /**
     * 总线类型：1553B总线
     * MIL-STD-1553B军用航空总线标准
     */
    public static final int BUS_1553B = 5;  // 1553B总线常量，索引值5

    /**
     * 总线类型：ARINC429总线
     * 航空电子数字信息传输标准
     */
    public static final int BUS_429  = 6;   // 429总线常量，索引值6

    /**
     * 总线类型：CAN-FD总线
     * CAN with Flexible Data-rate，CAN的升级版本
     */
    public static final int BUS_CAN_FD  = 7; // CAN-FD总线常量，索引值7

    /**
     * 总线类型总数
     * 用于数组边界检查和循环遍历
     */
    public static final int BUS_CNT = 8;    // 总线类型总数，共8种总线

    // ==================== 语言类型常量定义 ====================

    /**
     * 语言类型：英语（美国）
     */
    public static final int LANGUAGE_en_US = 0;	// 英语常量，索引值0

    /**
     * 语言类型：简体中文
     */
    public static final int LANGUAGE_zh_CN = 1;	// 简体中文常量，索引值1

    /**
     * 语言类型：繁体中文
     */
    public static final int LANGUAGE_zh_TW = 2;	// 繁体中文常量，索引值2

    /**
     * 语言类型：德语
     */
    public static final int LANGUAGE_de_DE = 3;	// 德语常量，索引值3

    /**
     * 语言类型：俄语
     */
    public static final int LANGUAGE_ru_RU = 4;	// 俄语常量，索引值4

    /**
     * 语言类型：西班牙语
     */
    public static final int LANGUAGE_es_ES = 5;	// 西班牙语常量，索引值5

    /**
     * 语言类型：朝鲜语（韩语）
     */
    public static final int LANGUAGE_ko_KR = 6;	// 朝鲜语常量，索引值6

    /**
     * 语言类型：捷克语
     */
    public static final int LANGUAGE_cs_CZ = 7;	// 捷克语常量，索引值7

    /**
     * 语言类型：阿拉伯语
     */
    public static final int LANGUAGE_ar_AE = 8;	// 阿拉伯语常量，索引值8

    /**
     * 语言类型：意大利语
     */
    public static final int LANGUAGE_it_CH = 9;	// 意大利语常量，索引值9

    /**
     * 语言类型：土耳其语
     */
    public static final int LANGUAGE_tr_TR = 10;	// 土耳其语常量，索引值10

    /**
     * 语言类型：法语
     */
    public static final int LANGUAGE_fr_FR = 11;	// 法语常量，索引值11

    /**
     * 语言类型总数
     * 用于数组边界检查和循环遍历
     */
    public static final int LANGUAGE_CNT = 12; // 语言类型总数，共12种语言

    // ==================== Native层指针与基础属性 ====================

    /**
     * Native层属性对象指针
     * 用于JNI调用时传递Native对象引用
     */
    private long ptr; // Native层属性对象指针，用于JNI方法调用

    /**
     * 设备唯一标识UUID
     * 由硬件生成的全球唯一标识符
     */
    private String uuid=""; // 设备UUID，全局唯一标识符

    /**
     * 设备序列号（内部使用）
     * 生产时分配的序列号
     */
    private String sn=""; // 设备序列号，生产标识

    /**
     * 设备显示序列号
     * 用户可见的序列号，可能与内部SN不同
     */
    private String displaySN=""; // 显示用序列号，用户界面展示

    /**
     * 设备型号
     * 如MHO3系列、MHO5系列等
     */
    private String type=""; // 设备型号名称

    /**
     * 发货日期
     * 格式：YYYY-MM-DD
     */
    private String deliveryDate=""; // 发货日期，用于保修计算

    /**
     * OEM厂商名称
     * 定制设备的品牌名称
     */
    private String oemName=""; // OEM厂商名称，定制品牌标识

    /**
     * 硬件版本号
     * 默认值为"1"
     */
    private String hwVersion="1"; // 硬件版本号，默认为1

    // ==================== 性能属性 ====================

    /**
     * 带宽值（单位：MHz）
     * 示波器的模拟带宽
     */
    private int bandWidth=0; // 带宽值，单位MHz

    /**
     * 存储深度（单位：pts）
     * 示波器的最大存储点数
     */
    private int memDepth=0; // 存储深度，单位pts（采样点）

    /**
     * 保修日期（单位：月）
     * 从发货日期计算的保修期限
     */
    private int warrantyDate=0; // 保修期限，单位月

    /**
     * 高刷新率标志
     * 0-标准刷新率，非0-高刷新率
     */
    private int highRefresh=0; // 高刷新率标志，0为标准刷新率

    // ==================== 功能授权标志 ====================

    /**
     * 频率计功能授权标志
     * true-已授权，false-未授权
     */
    private boolean bEnableFreqCounter = false; // 频率计功能授权标志

    /**
     * HDMI输出功能授权标志
     * true-已授权，false-未授权
     */
    private boolean bEnableHdmi = false; // HDMI输出功能授权标志

    /**
     * 500uV灵敏度功能授权标志
     * true-已授权，false-未授权
     */
    private boolean bEnable500uV = false; // 500uV灵敏度功能授权标志

    /**
     * 自动量程功能授权标志
     * true-已授权，false-未授权
     */
    private boolean bEnableAutoRange = false; // 自动量程功能授权标志

    /**
     * 发货日期有效标志
     * true-发货日期已设置，false-未设置
     */
    private boolean bDeliveryDate = false; // 发货日期有效标志

    /**
     * WiFi功能授权标志
     * true-已授权，false-未授权
     */
    private boolean bEnableWlan = false; // WiFi功能授权标志

    /**
     * 汽车总线功能授权标志
     * true-已授权，false-未授权
     */
    private boolean bEnableAutomotive = false; // 汽车总线功能授权标志

    /**
     * 高低通滤波功能授权标志
     * true-已授权，false-未授权
     */
    private boolean bHighLowPassFilter = false; // 高低通滤波功能授权标志

    /**
     * 按键光标功能授权标志
     * true-已授权，false-未授权
     */
    private boolean bKeyCursorEnable = false; // 按键光标功能授权标志

    // ==================== 状态标志与授权数组 ====================

    /**
     * 属性数据有效标志
     * true-属性数据已成功初始化，false-初始化失败
     */
    private boolean bVaild = false; // 属性数据有效标志

    /**
     * 总线授权状态数组
     * 索引对应BUS_*常量，true表示已授权
     */
    private boolean [] busEnableArray = new boolean[BUS_CNT]; // 总线授权状态数组，索引对应总线类型

    /**
     * 语言授权状态数组
     * 索引对应LANGUAGE_*常量，true表示已授权
     */
    private boolean [] languageEnableArray = new boolean[LANGUAGE_CNT]; // 语言授权状态数组，索引对应语言类型

    /**
     * 私有UUID
     * 用于特殊场景的设备标识
     */
    private String privateUUID=""; // 私有UUID，特殊场景使用

    // ==================== 索引有效性检查方法 ====================

    /**
     * 检查总线索引是否有效
     *
     * @param busIdx 总线索引值
     * @return true-索引有效；false-索引无效
     */
    private static boolean isBusValid(int busIdx){ // 检查总线索引是否在有效范围内
        return busIdx >= BUS_UART && busIdx < BUS_CNT; // 返回索引是否在[0, BUS_CNT)范围内
    }

    /**
     * 检查语言索引是否有效
     *
     * @param langIdx 语言索引值
     * @return true-索引有效；false-索引无效
     */
    private static boolean isLanguageValid(int langIdx){ // 检查语言索引是否在有效范围内
        return langIdx >= LANGUAGE_en_US && langIdx < LANGUAGE_CNT; // 返回索引是否在[0, LANGUAGE_CNT)范围内
    }

    // ==================== 构造函数 ====================

    /**
     * 默认构造函数
     * 初始化所有授权数组为false状态
     */
    public Property(){ // 默认构造函数，初始化属性对象
        for(int i=0;i<busEnableArray.length;i++){ // 遍历总线授权数组
            busEnableArray[i] = false; // 初始化总线索引i的授权状态为false
        }
        for(int i=0;i<languageEnableArray.length;i++){ // 遍历语言授权数组
            languageEnableArray[i] = false; // 初始化语言索引i的授权状态为false
        }
    }

    // ==================== 属性初始化与验证 ====================

    /**
     * 从字节数组初始化属性数据
     * 通过JNI调用Native层解析EEPROM数据
     *
     * @param bytes EEPROM数据字节数组
     * @return true-初始化成功；false-初始化失败
     */
    public boolean initProperty(byte[] bytes){ // 从字节数组初始化属性数据
        bVaild = nativeInit(bytes); // 调用Native方法解析数据，并更新有效标志
        return bVaild; // 返回初始化结果
    }

    /**
     * 检查属性数据是否有效
     *
     * @return true-属性数据有效；false-属性数据无效
     */
    public boolean isValid(){ // 检查属性数据是否已成功初始化
        return bVaild; // 返回属性数据有效标志
    }

    // ==================== UUID相关方法 ====================

    /**
     * 设置设备UUID
     *
     * @param uuid 设备唯一标识字符串
     */
    public void setUUID(String uuid){ // 设置设备UUID
        this.uuid = uuid; // 更新UUID成员变量
    }

    /**
     * 获取设备UUID
     *
     * @return 设备唯一标识字符串
     */
    public String getUUID(){ // 获取设备UUID
        return uuid; // 返回UUID字符串
    }

    // ==================== 序列号相关方法 ====================

    /**
     * 获取设备序列号（内部使用）
     *
     * @return 设备序列号字符串
     */
    public String getSN() { // 获取设备序列号
        return sn; // 返回序列号字符串
    }

    /**
     * 设置设备序列号
     *
     * @param sn 设备序列号字符串
     */
    public void setSN(String sn){ // 设置设备序列号
        this.sn = sn; // 更新序列号成员变量
    }

    /**
     * 获取显示序列号
     *
     * @return 显示用序列号字符串
     */
    public String getDisplaySN() { // 获取显示序列号
        return displaySN; // 返回显示序列号字符串
    }

    /**
     * 设置显示序列号
     *
     * @param displaySN 显示用序列号字符串
     */
    public void setDisplaySN(String displaySN){ // 设置显示序列号
        this.displaySN = displaySN; // 更新显示序列号成员变量
    }

    // ==================== 型号相关方法 ====================

    /**
     * 设置设备型号
     *
     * @param type 设备型号字符串
     */
    public void setType(String type){ // 设置设备型号
        this.type = type; // 更新型号成员变量
    }

    /**
     * 获取设备型号
     *
     * @return 设备型号字符串
     */
    public String getType() { // 获取设备型号
        return type; // 返回型号字符串
    }

    // ==================== 发货日期相关方法 ====================

    /**
     * 设置发货日期
     *
     * @param deliveryDate 发货日期字符串，格式：YYYY-MM-DD
     */
    public void setDeliveryDate(String deliveryDate){ // 设置发货日期
        this.deliveryDate = deliveryDate; // 更新发货日期成员变量
    }

    /**
     * 获取发货日期
     *
     * @return 发货日期字符串
     */
    public String getDeliveryDate() { // 获取发货日期
        return deliveryDate; // 返回发货日期字符串
    }

    // ==================== OEM相关方法 ====================

    /**
     * 设置OEM厂商名称
     *
     * @param oemName OEM厂商名称字符串
     */
    public void setOemName(String oemName){ // 设置OEM厂商名称
        this.oemName = oemName; // 更新OEM名称成员变量
    }

    /**
     * 获取OEM厂商名称
     *
     * @return OEM厂商名称字符串
     */
    public String getOemName() { // 获取OEM厂商名称
        return oemName; // 返回OEM名称字符串
    }

    // ==================== 硬件版本相关方法 ====================

    /**
     * 设置硬件版本号
     *
     * @param hwVersion 硬件版本号字符串
     */
    public void setHwVersion(String hwVersion){ // 设置硬件版本号
        this.hwVersion = hwVersion; // 更新硬件版本成员变量
    }

    /**
     * 获取硬件版本号
     *
     * @return 硬件版本号字符串
     */
    public String getHwVersion() { // 获取硬件版本号
        return hwVersion; // 返回硬件版本字符串
    }

    /**
     * 获取私有UUID
     *
     * @return 私有UUID字符串
     */
    public String getPrivateUUID(){return privateUUID;} // 返回私有UUID字符串

    // ==================== 带宽相关方法 ====================

    /**
     * 设置带宽值
     *
     * @param bandWidth 带宽值，单位MHz
     */
    public void setBandWidth(int bandWidth){ // 设置带宽值
        this.bandWidth=bandWidth; // 更新带宽成员变量
    }

    /**
     * 获取带宽值
     *
     * @return 带宽值，单位MHz
     */
    public int getBandWidth() { // 获取带宽值
        return bandWidth; // 返回带宽值
    }

    // ==================== 存储深度相关方法 ====================

    /**
     * 获取存储深度
     *
     * @return 存储深度值，单位pts（采样点）
     */
    public int getMemDepth() { // 获取存储深度
        return memDepth; // 返回存储深度值
    }

    // ==================== 高刷新率相关方法 ====================

    /**
     * 获取高刷新率标志
     *
     * @return 高刷新率标志值
     */
    public int getHighRefresh(){return highRefresh;} // 返回高刷新率标志

    // ==================== 功能授权查询方法 ====================

    /**
     * 检查频率计功能是否已授权
     *
     * @return true-已授权；false-未授权
     */
    public boolean isEnableFreqCounter() { // 检查频率计功能授权
        return bEnableFreqCounter; // 返回频率计授权标志
    }

    /**
     * 检查HDMI输出功能是否已授权
     *
     * @return true-已授权；false-未授权
     */
    public boolean isEnableHdmi() { // 检查HDMI功能授权
        return bEnableHdmi; // 返回HDMI授权标志
    }

    /**
     * 检查500uV灵敏度功能是否已授权
     *
     * @return true-已授权；false-未授权
     */
    public boolean isEnable500uV() { // 检查500uV灵敏度功能授权
        return bEnable500uV; // 返回500uV授权标志
    }

    /**
     * 检查自动量程功能是否已授权
     *
     * @return true-已授权；false-未授权
     */
    public boolean isEnableAutoRange() { // 检查自动量程功能授权
        return bEnableAutoRange; // 返回自动量程授权标志
    }

    /**
     * 检查发货日期是否已设置
     *
     * @return true-已设置；false-未设置
     */
    public boolean isDeliveryDate() { // 检查发货日期是否有效
        if(deliveryDate != null && deliveryDate.length() > 0) // 检查发货日期非空且有内容
        {
            return  true; // 发货日期有效，返回true
        }
        return false; // 发货日期无效，返回false
    }

    /**
     * 检查WiFi功能是否已授权
     *
     * @return true-已授权；false-未授权
     */
    public boolean isEnableWlan() { // 检查WiFi功能授权
        return bEnableWlan; // 返回WiFi授权标志
    }

    /**
     * 检查汽车总线功能是否已授权
     *
     * @return true-已授权；false-未授权
     */
    public boolean isEnableAutomotive() { // 检查汽车总线功能授权
        return bEnableAutomotive; // 返回汽车总线授权标志
    }

    /**
     * 检查高低通滤波功能是否已授权
     *
     * @return true-已授权；false-未授权
     */
    public boolean isHighLowPassFilter() { // 检查高低通滤波功能授权
        return bHighLowPassFilter; // 返回高低通滤波授权标志
    }

    /**
     * 检查按键光标功能是否已授权
     *
     * @return true-已授权；false-未授权
     */
    public boolean isKeyCursorEnable(){return bKeyCursorEnable;} // 返回按键光标授权标志

    // ==================== 总线授权查询方法 ====================

    /**
     * 检查指定总线类型是否已授权
     *
     * @param busIdx 总线类型索引，使用BUS_*常量
     * @return true-已授权；false-未授权或索引无效
     */
    public boolean isEnableBus(int busIdx){ // 检查指定总线类型的授权状态
        if(isBusValid(busIdx)) // 验证总线索引是否有效
            return busEnableArray[busIdx]; // 索引有效，返回对应总线的授权状态
        return false; // 索引无效，返回false
    }

    // ==================== 语言授权查询方法 ====================

    /**
     * 检查指定语言类型是否已授权
     *
     * @param langIdx 语言类型索引，使用LANGUAGE_*常量
     * @return true-已授权；false-未授权或索引无效
     */
    public boolean isEnableLanguage(int langIdx){ // 检查指定语言类型的授权状态
        if(isLanguageValid(langIdx)) // 验证语言索引是否有效
            return languageEnableArray[langIdx]; // 索引有效，返回对应语言的授权状态
        return false; // 索引无效，返回false
    }

    // ==================== 保修相关方法 ====================

    /**
     * 获取保修期限
     *
     * @return 保修期限，单位月
     */
    public int getWarrantyDate(){ // 获取保修期限
        return warrantyDate; // 返回保修期限值
    }

    // ==================== 数据序列化方法 ====================

    /**
     * 获取属性数据的字节数组表示
     * 用于将属性数据写入EEPROM
     *
     * @return 属性数据字节数组
     */
    public byte[] getBytes(){ // 获取属性数据的字节数组
        return nativeGetBytes(); // 调用Native方法序列化属性数据
    }

    // ==================== 序列号升级方法 ====================

    /**
     * 通过序列码升级设备功能授权
     * 序列码格式由产品定义，包含授权信息
     *
     * @param serialCode 序列码字符串
     * @return true-升级成功；false-升级失败
     */
    public boolean serialCodeUpgrade(String serialCode){ // 通过序列码升级功能授权
        if(serialCode != null) { // 检查序列码非空
            return nativeSerialCodeUpgrade(serialCode); // 调用Native方法执行升级
        }
        return false; // 序列码为空，返回失败
    }

    // ==================== 属性清除方法 ====================

    /**
     * 清除所有属性数据
     * 恢复到出厂默认状态
     *
     * @return true-清除成功；false-清除失败
     */
    public boolean clear(){ // 清除所有属性数据
        return nativeClear(); // 调用Native方法清除属性
    }

    // ==================== JNI Native方法声明 ====================

    /**
     * Native方法：从字节数组初始化属性数据
     *
     * @param bytes 属性数据字节数组
     * @return true-初始化成功；false-初始化失败
     */
    private native boolean nativeInit(byte [] bytes); // JNI方法：解析字节数组初始化属性

    /**
     * Native方法：获取属性数据的字节数组
     *
     * @return 属性数据字节数组
     */
    private native byte[] nativeGetBytes(); // JNI方法：序列化属性为字节数组

    /**
     * Native方法：通过序列码升级功能授权
     *
     * @param serialCode 序列码字符串
     * @return true-升级成功；false-升级失败
     */
    private native boolean nativeSerialCodeUpgrade(String serialCode); // JNI方法：序列码升级

    /**
     * Native方法：清除所有属性数据
     *
     * @return true-清除成功；false-清除失败
     */
    private native boolean nativeClear(); // JNI方法：清除属性数据

}
