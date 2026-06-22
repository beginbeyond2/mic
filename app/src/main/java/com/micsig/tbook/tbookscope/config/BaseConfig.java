package com.micsig.tbook.tbookscope.config; // 配置模块包，负责设备功能授权与参数配置

import com.micsig.smart.Property;                   // 授权属性模型，封装设备授权文件解析结果
import com.micsig.smart.PropertyManage;              // 授权属性管理器，负责读取和提交授权文件
import com.micsig.tbook.hardware.HardwareProduct;    // 硬件产品型号工具类，提供产品型号判断
import com.micsig.tbook.scope.BuildConfig;           // 构建配置，包含DEBUG标志等编译期常量
import com.micsig.tbook.scope.Bus.IBus;              // 串行总线接口，定义总线类型常量与使能控制
import com.micsig.tbook.scope.Display.Display;       // 显示控制类，管理高刷新率等显示参数
import com.micsig.tbook.scope.Sample.MemDepthFactory; // 存储深度工厂，管理采样存储深度配置
import com.micsig.tbook.scope.channel.Channel;       // 通道类，管理通道带宽等参数
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 水平轴类，管理时基档位范围
import com.micsig.tbook.scope.vertical.VerticalAxis;     // 垂直轴类，管理垂直灵敏度档位范围

/**
 * +============================================================================+
 * |                     BaseConfig - 配置抽象基类                                |
 * +============================================================================+
 * |                                                                            |
 * | 【模块定位】                                                                |
 * |   MHO系列示波器配置模块的抽象基类，实现IConfig接口，负责从设备授权文件          |
 * |   （Property）读取功能授权信息并配置硬件参数（带宽、存储深度、总线使能等）。      |
 * |   子类仅需定义产品特定的档位范围和产品校验逻辑。                               |
 * |                                                                            |
 * | 【核心职责】                                                                |
 * |   1. 从PropertyManage读取并解析设备授权文件                                  |
 * |   2. 根据授权信息配置硬件参数（带宽/存储深度/高刷新/总线使能/档位范围）         |
 * |   3. 在DEBUG模式下开放全部功能用于开发调试                                   |
 * |   4. 在无授权且非DEBUG模式下提供最小功能集（70MHz带宽）                       |
 * |   5. 缓存功能授权状态，提供IConfig接口查询                                   |
 * |   6. 初始化硬件版本号（若不存在则设为"1"）                                   |
 * |                                                                            |
 * | 【架构设计】                                                                |
 * |   采用模板方法模式：                                                         |
 * |   - BaseConfig定义配置初始化骨架（构造函数），子类通过抽象方法提供档位参数      |
 * |   - 抽象方法: getMinVerticalGear/getMaxVerticalGear/getMinHorizontalGear/    |
 * |     getMaxHorizontalGear 由子类实现                                         |
 * |   - IConfig接口方法由BaseConfig统一实现，基于授权缓存字段返回结果             |
 * |                                                                            |
 * |   IConfig (接口)                                                            |
 * |      |                                                                      |
 * |      +-- BaseConfig (抽象基类) ← 本类                                       |
 * |              |                                                               |
 * |              +-- SmartTO1000Config (SmartTO1000产品配置)                      |
 * |                                                                            |
 * | 【数据流向】                                                                |
 * |   授权文件 -> PropertyManage.update() -> Property对象                        |
 * |       -> BaseConfig构造函数(解析Property) -> 缓存字段(bEnableXxx)             |
 * |       -> 硬件参数设置(Channel/Display/MemDepthFactory/IBus等)                |
 * |       -> IConfig方法(对外查询)                                               |
 * |                                                                            |
 * | 【依赖关系】                                                                |
 * |   上层依赖: ScopeConfig(工厂类创建子类实例)                                  |
 * |   下层依赖: PropertyManage/Property(授权读取), Channel(带宽配置),             |
 * |             Display(高刷新配置), MemDepthFactory(存储深度配置),               |
 * |             IBus(总线使能配置), VerticalAxis(垂直档位配置),                   |
 * |             HorizontalAxis(水平档位配置), BuildConfig(DEBUG判断)             |
 * |                                                                            |
 * | 【使用示例】                                                                |
 * |   // 通过ScopeConfig工厂获取配置实例                                        |
 * |   IConfig config = ScopeConfig.getConfig();                                 |
 * |   // 查询功能授权                                                          |
 * |   boolean hasFreq = config.isEnableFreqCounter();                           |
 * |   boolean hasCAN = config.isBusEnable(IBus.CAN);                            |
 * |                                                                            |
 * +============================================================================+
 *
 * @author zhuzh
 * @since 2018-12-05
 */
public abstract class BaseConfig implements IConfig {

    /** 日志标签，用于Logcat输出标识 */
    private final String TAG = "BaseConfig"; // 日志标签

    /** 频率计数器功能授权标志，默认未授权 */
    private boolean bEnableFreqCounter = false; // 频率计数器功能授权标志，false=未授权

    /** 高低通滤波器功能授权标志，默认未授权 */
    private boolean bEnableHighLowFilter = false; // 高低通滤波器功能授权标志，false=未授权

    /** 自动员程功能授权标志，默认未授权 */
    private boolean bEnableAutoRange = false; // 自动员程功能授权标志，false=未授权

    /** 出厂日期有效标志，默认有效（true表示在有效期内） */
    private boolean bDeliveryDate = true; // 出厂日期有效标志，true=在有效期内

    /** 汽车总线功能授权标志，默认未授权 */
    private boolean bAutomotive = false; // 汽车总线功能授权标志，false=未授权

    /**
     * 获取产品支持的最小垂直灵敏度档位。
     *
     * <p>由子类实现，不同产品型号支持的最小垂直档位不同
     * （如1mV/div或2mV/div）。</p>
     *
     * @return 最小垂直档位常量，参考{@link VerticalAxis}中的DANG_xxx常量
     */
    protected abstract int getMinVerticalGear(); // 获取最小垂直灵敏度档位（子类实现）

    /**
     * 获取产品支持的最大垂直灵敏度档位。
     *
     * <p>由子类实现，不同产品型号支持的最大垂直档位不同
     * （如5V/div或10V/div）。</p>
     *
     * @return 最大垂直档位常量，参考{@link VerticalAxis}中的DANG_xxx常量
     */
    protected abstract int getMaxVerticalGear(); // 获取最大垂直灵敏度档位（子类实现）

    /**
     * 获取产品支持的最小水平时基档位。
     *
     * <p>由子类实现，不同产品型号支持的最小时基档位不同
     * （如1KS/div等）。</p>
     *
     * @return 最小时基档位常量，参考{@link HorizontalAxis}中的TSI_xxx常量
     */
    protected abstract int getMinHorizontalGear(); // 获取最小时基档位（子类实现）

    /**
     * 获取产品支持的最大水平时基档位。
     *
     * <p>由子类实现，不同产品型号支持的最大时基档位不同
     * （如250pS/div或1nS/div等）。</p>
     *
     * @return 最大时基档位常量，参考{@link HorizontalAxis}中的TSI_xxx常量
     */
    protected abstract int getMaxHorizontalGear(); // 获取最大时基档位（子类实现）

    /**
     * 构造函数：初始化设备配置。
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>获取PropertyManage单例并更新授权属性</li>
     *   <li>设置垂直/水平档位范围（由子类抽象方法提供参数）</li>
     *   <li>读取出厂日期标记，DEBUG模式下强制设为无效</li>
     *   <li>根据授权有效性分三种情况配置：
     *     <ul>
     *       <li>授权有效且非DEBUG：按授权文件配置所有参数</li>
     *       <li>DEBUG模式：开放全部功能用于开发调试</li>
     *       <li>无授权且非DEBUG：仅提供最小功能集（70MHz带宽）</li>
     *     </ul>
     *   </li>
     *   <li>检查并初始化硬件版本号</li>
     * </ol>
     */
    public  BaseConfig(){
        PropertyManage propertyManage = PropertyManage.getInstance(); // 获取PropertyManage单例
        propertyManage.update();                                      // 从授权文件更新属性数据
        Property property = propertyManage.getProperty();             // 获取解析后的Property对象
        setVerticalGear(getMinVerticalGear(),getMaxVerticalGear());   // 设置垂直灵敏度档位范围
        setHorizontalGear(getMinHorizontalGear(),getMaxHorizontalGear()); // 设置水平时基档位范围
        bDeliveryDate = property.isDeliveryDate();                    // 读取出厂日期有效标志
        if(BuildConfig.DEBUG){                                        // DEBUG模式下
            bDeliveryDate = false;                                    // 强制出厂日期无效，跳过日期校验
        }
        if(property.isValid() && !BuildConfig.DEBUG){                 // 授权有效且非DEBUG模式
            setMemDepth(property.getMemDepth());                      // 按授权设置存储深度
            setMaxBandWidth(property.getBandWidth()*1000*1000);       // 按授权设置最大带宽（MHz转Hz）
            setHighRefresh(property.getHighRefresh());                // 按授权设置高刷新率参数
            for (int i = 0; i < Property.BUS_CNT; i++) {             // 遍历所有总线类型
                setBusEnable(getBusType(i), property.isEnableBus(i)); // 按授权设置各总线使能状态
            }
            bEnableFreqCounter = property.isEnableFreqCounter();      // 读取频率计数器授权
            bEnableHighLowFilter = property.isHighLowPassFilter();    // 读取高低通滤波器授权
            bEnableAutoRange = property.isEnableAutoRange();          // 读取自动量程授权
            bAutomotive = property.isEnableAutomotive();              // 读取汽车总线功能授权
        }
        else                                                         // 授权无效或DEBUG模式
        {
            if(BuildConfig.DEBUG)                                    // DEBUG模式
            {

                setMemDepth(MemDepthFactory.getDefaultMemDepth());   // 设置默认存储深度
                setMaxBandWidth(Channel.MAX_BANDWIDTH);               // 开放最大带宽
                setHighRefresh(0);                                    // 高刷新率设为0（关闭）
                for (int i = 0; i < Property.BUS_CNT; i++) {         // 遍历所有总线类型
                    setBusEnable(getBusType(i), true);                // 开放所有总线解码功能
                }
                bEnableFreqCounter = true;                            // 开放频率计数器
                bEnableHighLowFilter = true;                          // 开放高低通滤波器
                bEnableAutoRange = true;                              // 开放自动量程
            }else{                                                   // 无授权且非DEBUG模式
                setMaxBandWidth(70e6);                                // 仅提供70MHz最小带宽
            }
        }

        String hw = property.getHwVersion();                         // 读取硬件版本号
        if(hw == null){                                              // 若硬件版本号不存在
            property.setHwVersion("1");                              // 初始化硬件版本号为"1"
            propertyManage.commit();                                 // 提交写入授权文件
        }
    }

    /**
     * 将Property总线索引转换为IBus总线类型常量。
     *
     * <p>Property中使用BUS_UART/BUS_LIN等索引标识总线类型，
     * IBus中使用UART/LIN等常量标识总线类型，两者编码不同，
     * 需要通过本方法进行映射转换。</p>
     *
     * @param idx Property中定义的总线索引，取值为Property.BUS_UART等常量
     * @return 对应的IBus总线类型常量；若索引无法识别则返回-1
     */
    private int getBusType(int idx){

        int busType = -1;                                             // 初始化为无效类型(-1)
        switch (idx){                                                 // 根据Property总线索引映射
            case Property.BUS_UART:  //：串型总线
                busType = IBus.UART;                                  // 映射为IBus.UART
                break;
            case Property.BUS_LIN:
                busType = IBus.LIN;                                   // 映射为IBus.LIN
                break;
            case Property.BUS_SPI:
                busType = IBus.SPI;                                   // 映射为IBus.SPI
                break;
            case Property.BUS_CAN:
                busType = IBus.CAN;                                   // 映射为IBus.CAN
                break;
            case Property.BUS_I2C:
                busType = IBus.I2C;                                   // 映射为IBus.I2C
                break;
            case Property.BUS_1553B:
                busType = IBus.MILSTD1553B;                           // 映射为IBus.MILSTD1553B
                break;
            case Property.BUS_429:
                busType = IBus.ARINC429;                              // 映射为IBus.ARINC429
                break;
            case Property.BUS_CAN_FD:
                busType = IBus.CAN_FD;                                // 映射为IBus.CAN_FD
                break;
        }
        return busType;                                               // 返回映射后的IBus总线类型
    }

    /**
     * 设置高刷新率参数。
     *
     * <p>将高刷新率计数器值传递给Display模块，控制屏幕刷新策略。
     * 值为0表示关闭高刷新率功能。</p>
     *
     * @param highRefresh 高刷新率计数器值，0=关闭，正值=启用对应刷新级别
     */
    public void setHighRefresh(int highRefresh){
        Display.setHighRefreshCounter(highRefresh);                   // 设置Display模块的高刷新率计数器
    }

    /**
     * 配置最大存储深度。
     *
     * <p>若指定的存储深度超过设备默认最大值或为0（无效值），
     * 则自动回退为默认存储深度。最终将有效值设置到MemDepthFactory。</p>
     *
     * @param memDepth 存储深度值（采样点数），若无效则使用默认值
     */
    //配置最大存储深度
    public void setMemDepth(int memDepth){
        if(memDepth > MemDepthFactory.getDefaultMemDepth() || memDepth == 0){ // 超过默认值或为0
            memDepth =  MemDepthFactory.getDefaultMemDepth();        // 回退为默认存储深度
        }
        MemDepthFactory.setMemDepth(memDepth);                       // 设置MemDepthFactory的存储深度
    }

    /**
     * 获取当前配置的存储深度。
     *
     * @return 当前MemDepthFactory中设置的存储深度值（采样点数）
     */
    protected int getMemDepth(){
        return MemDepthFactory.getMemDepthSet();                     // 返回MemDepthFactory中已设置的存储深度
    }

    /**
     * 配置垂直灵敏度档位范围。
     *
     * <p>设置垂直轴（Y轴）可用的最小和最大灵敏度档位，
     * 用户在UI中只能在范围内选择档位。</p>
     *
     * @param minGear 最小垂直档位常量（如DANG_1mV）
     * @param maxGear 最大垂直档位常量（如DANG_10V）
     */
    //配置垂直方向 最大最小档位
    public void setVerticalGear(int minGear,int maxGear){
        VerticalAxis.setMinGear(minGear);                            // 设置垂直轴最小档位
        VerticalAxis.setMaxGear(maxGear);                            // 设置垂直轴最大档位
    }

    /**
     * 配置水平时基档位范围。
     *
     * <p>设置水平轴（X轴/时基）可用的最小和最大时基档位，
     * 用户在UI中只能在范围内选择时基。</p>
     *
     * @param minGear 最小时基档位常量（如TSI_1KS）
     * @param maxGear 最大时基档位常量（如TSI_250pS）
     */
    //配置水平方向 最大最小档位
    public void setHorizontalGear(int minGear,int maxGear){
        HorizontalAxis.setMinGear(minGear);                          // 设置水平轴最小时基档位
        HorizontalAxis.setMaxGear(maxGear);                          // 设置水平轴最大时基档位
    }

    /**
     * 配置通道最大带宽限制。
     *
     * <p>设置通道允许的最大模拟带宽值，超过此带宽的信号将被衰减。
     * 不同授权等级对应不同的带宽限制。</p>
     *
     * @param maxBandWidth 最大带宽值，单位Hz（如70e6表示70MHz）
     */
    //配置最大带宽
    public void setMaxBandWidth(double maxBandWidth){
        Channel.setMaxBandWidth(maxBandWidth);                       // 设置Channel模块的最大带宽
    }

    /**
     * 配置指定类型串行总线的使能状态。
     *
     * <p>根据授权信息启用或禁用特定类型的串行总线解码功能。</p>
     *
     * @param busType 总线类型，取值参考IBus中的常量（如IBus.CAN）
     * @param bEnable true=启用该总线解码；false=禁用
     */
    //配置串行总线
    public void setBusEnable(int busType,boolean bEnable){
        IBus.setBusEnable(busType,bEnable);                          // 设置IBus中指定总线类型的使能状态
    }

    /**
     * {@inheritDoc}
     *
     * <p>返回频率计数器功能的授权状态。</p>
     */
    @Override
    public boolean isEnableFreqCounter() {
        return bEnableFreqCounter;                                   // 返回频率计数器授权标志
    }

    /**
     * {@inheritDoc}
     *
     * <p>查询指定类型串行总线解码功能的授权状态。
     * 需要将Property总线类型索引转换为IBus总线类型常量后查询。</p>
     *
     * @param busType Property中定义的总线索引常量
     */
    @Override
    public boolean isBusEnable(int busType) {
        int busTypes = -1;                                           // 初始化为无效类型(-1)
        switch (busType){                                            // 将Property总线索引映射为IBus类型
            case Property.BUS_UART:  //：串型总线
                busTypes = IBus.UART;                                // 映射为IBus.UART
                break;
            case Property.BUS_LIN:
                busTypes = IBus.LIN;                                 // 映射为IBus.LIN
                break;
            case Property.BUS_SPI:
                busTypes = IBus.SPI;                                 // 映射为IBus.SPI
                break;
            case Property.BUS_CAN:
                busTypes = IBus.CAN;                                 // 映射为IBus.CAN
                break;
            case Property.BUS_I2C:
                busTypes = IBus.I2C;                                 // 映射为IBus.I2C
                break;
            case Property.BUS_1553B:
                busTypes = IBus.MILSTD1553B;                         // 映射为IBus.MILSTD1553B
                break;
            case Property.BUS_429:
                busTypes = IBus.ARINC429;                            // 映射为IBus.ARINC429
                break;
            case Property.BUS_CAN_FD:
                busTypes = IBus.CAN_FD;                              // 映射为IBus.CAN_FD
                break;
        }
        return IBus.isBusEnable(busTypes);                           // 查询IBus中该总线类型的使能状态
    }

    /**
     * {@inheritDoc}
     *
     * <p>返回高低通滤波器功能的授权状态。</p>
     */
    @Override
    public boolean isEnableHighLowFilter() {
        return bEnableHighLowFilter;                                 // 返回高低通滤波器授权标志
    }

    /**
     * {@inheritDoc}
     *
     * <p>返回自动量程功能的授权状态。</p>
     */
    @Override
    public boolean isEnableAutoRange() {
        return bEnableAutoRange;                                     // 返回自动量程授权标志
    }

    /**
     * {@inheritDoc}
     *
     * <p>返回设备出厂日期的有效性状态。</p>
     */
    @Override
    public boolean isDeliveryDate() {
        return bDeliveryDate;                                        // 返回出厂日期有效标志
    }

    /**
     * {@inheritDoc}
     *
     * <p>遍历所有总线类型，判断是否存在任意一种已授权的总线解码功能。
     * 从BUS_UART开始到BUS_CNT结束逐一检查。</p>
     */
    @Override
    public boolean isBusEnable() {

        for(int i=Property.BUS_UART;i<Property.BUS_CNT;i++){        // 遍历所有总线类型索引
            if(isBusEnable(i)){                                      // 检查当前总线类型是否已启用
                return true;                                         // 发现已启用的总线，立即返回true
            }
        }
        return false;                                                // 所有总线均未启用，返回false
    }
}
