package com.micsig.tbook.tbookscope.config; // 配置模块包，负责设备功能授权与参数配置

/**
 * +============================================================================+
 * |                          IConfig - 配置接口                                 |
 * +============================================================================+
 * |                                                                            |
 * | 【模块定位】                                                                |
 * |   MHO系列示波器Android应用的配置模块核心接口，定义设备功能授权查询契约。          |
 * |   所有配置实现类（如BaseConfig及其子类）必须实现本接口，以统一功能开关查询方式。    |
 * |                                                                            |
 * | 【核心职责】                                                                |
 * |   1. 声明频率计数器功能授权查询方法                                          |
 * |   2. 声明串行总线解码功能授权查询方法（含按类型查询与全局查询）                   |
 * |   3. 声明高低通滤波器功能授权查询方法                                        |
 * |   4. 声明自动量程功能授权查询方法                                            |
 * |   5. 声明出厂日期标记查询方法                                                |
 * |   6. 声明产品合法性校验方法                                                  |
 * |                                                                            |
 * | 【架构设计】                                                                |
 * |   采用接口隔离原则，将功能授权查询抽象为独立接口，与具体配置实现解耦。            |
 * |   调用方仅依赖IConfig接口，无需关心底层Property读取逻辑。                     |
 * |                                                                            |
 * |   IConfig (接口)                                                            |
 * |      |                                                                      |
 * |      +-- BaseConfig (抽象基类，实现IConfig)                                  |
 * |              |                                                               |
 * |              +-- SmartTO1000Config (SmartTO1000产品配置)                      |
 * |                                                                            |
 * | 【数据流向】                                                                |
 * |   Property(授权文件) -> PropertyManage(解析) -> BaseConfig(读取并缓存)        |
 * |       -> IConfig方法(对外查询)                                               |
 * |                                                                            |
 * | 【依赖关系】                                                                |
 * |   上层依赖: ScopeConfig(工厂类), 各业务模块通过IConfig查询功能授权              |
 * |   下层依赖: 无（纯接口定义）                                                 |
 * |                                                                            |
 * | 【使用示例】                                                                |
 * |   IConfig config = ScopeConfig.getConfig();                                 |
 * |   if (config.isEnableFreqCounter()) {                                       |
 * |       // 启用频率计数器功能                                                  |
 * |   }                                                                        |
 * |   if (config.isBusEnable(IBus.CAN)) {                                       |
 * |       // 启用CAN总线解码功能                                                 |
 * |   }                                                                        |
 * |                                                                            |
 * +============================================================================+
 *
 * @author zhuzh
 * @since 2018-12-05
 */
public interface IConfig {

    /**
     * 查询频率计数器功能是否已授权启用。
     *
     * <p>频率计数器（Frequency Counter）用于精确测量输入信号的频率值，
     * 该功能需要设备授权才可使用。</p>
     *
     * @return true - 频率计数器功能已授权启用；false - 未授权，功能不可用
     */
    boolean isEnableFreqCounter(); // 查询频率计数器功能授权状态

    /**
     * 查询是否存在任意一种串行总线解码功能已授权启用。
     *
     * <p>遍历所有总线类型（UART/LIN/SPI/CAN/I2C/1553B/429/CAN_FD），
     * 只要其中任意一种总线解码功能已启用即返回true。</p>
     *
     * @return true - 至少有一种总线解码功能已启用；false - 所有总线解码均未启用
     */
    boolean isBusEnable(); // 查询是否存在任意已授权的总线解码功能

    /**
     * 查询指定类型的串行总线解码功能是否已授权启用。
     *
     * <p>支持的总线类型包括：UART、LIN、SPI、CAN、I2C、MIL-STD-1553B、
     * ARINC429、CAN_FD等。总线类型常量定义在{@link com.micsig.tbook.scope.Bus.IBus}中。</p>
     *
     * @param busType 总线类型标识，取值参考IBus中的常量（如IBus.CAN、IBus.UART等）
     * @return true - 指定类型总线解码功能已授权启用；false - 未授权
     */
    boolean isBusEnable(int busType); // 查询指定类型总线解码功能的授权状态

    /**
     * 查询高低通滤波器功能是否已授权启用。
     *
     * <p>高低通滤波器（High/Low Pass Filter）用于对输入信号进行频率域滤波处理，
     * 可滤除高频噪声或低频干扰，该功能需要设备授权才可使用。</p>
     *
     * @return true - 高低通滤波器功能已授权启用；false - 未授权，功能不可用
     */
    boolean isEnableHighLowFilter(); // 查询高低通滤波器功能授权状态

    /**
     * 查询自动量程功能是否已授权启用。
     *
     * <p>自动量程（Auto Range）功能可根据输入信号自动调整垂直和水平档位，
     * 使波形显示达到最佳效果，该功能需要设备授权才可使用。</p>
     *
     * @return true - 自动员程功能已授权启用；false - 未授权，功能不可用
     */
    boolean isEnableAutoRange(); // 查询自动量程功能授权状态

    /**
     * 查询设备是否处于出厂日期有效期内。
     *
     * <p>出厂日期标记用于判断设备是否在授权有效期内。
     * DEBUG模式下此值始终返回false，以跳过日期校验限制。</p>
     *
     * @return true - 设备在出厂日期有效期内；false - 已超出有效期或DEBUG模式
     */
    boolean isDeliveryDate(); // 查询设备出厂日期是否有效

    /**
     * 校验当前设备是否为合法产品。
     *
     * <p>通过检查设备Build.PRODUCT信息判断当前运行设备是否为MHO系列合法硬件。
     * 不同产品型号的配置子类实现各自的校验逻辑（如SmartTO1000Config
     * 检查产品名是否以"rk3588_MHO"开头）。</p>
     *
     * @return true - 当前设备为合法MHO产品；false - 非法设备或未识别型号
     */
    boolean isValidProduct(); // 校验当前设备是否为合法MHO产品
}
