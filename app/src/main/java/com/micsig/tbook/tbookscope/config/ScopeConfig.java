package com.micsig.tbook.tbookscope.config; // 配置模块包，负责设备功能授权与参数配置


import com.micsig.tbook.hardware.HardwareProduct;    // 硬件产品型号工具类（本文件未直接使用，保留兼容）
import com.micsig.tbook.scope.Sample.MemDepthFactory; // 存储深度工厂，提供存储深度常量与配置

/**
 * +============================================================================+
 * |                     ScopeConfig - 示波器配置工厂类                           |
 * +============================================================================+
 * |                                                                            |
 * | 【模块定位】                                                                |
 * |   MHO系列示波器配置模块的工厂入口类，负责以懒加载方式创建并缓存IConfig实例。    |
 * |   全局所有业务模块通过本类获取配置对象，实现配置的统一分发。                     |
 * |                                                                            |
 * | 【核心职责】                                                                |
 * |   1. 以懒加载单例模式创建IConfig实现类实例                                   |
 * |   2. 缓存配置实例，避免重复创建                                              |
 * |   3. 对外提供统一的配置获取入口                                               |
 * |                                                                            |
 * | 【架构设计】                                                                |
 * |   采用懒加载单例模式（非线程安全），首次调用getConfig()时创建配置实例并缓存。    |
 * |   当前硬编码创建SmartTO1000Config实例，后续可扩展为根据产品型号               |
 * |   动态选择配置实现类。                                                       |
 * |                                                                            |
 * |   ScopeConfig (工厂)                                                        |
 * |      |                                                                      |
 * |      +-- getConfig() → IConfig (懒加载)                                     |
 * |              |                                                               |
 * |              +-- SmartTO1000Config (当前唯一实现)                             |
 * |                                                                            |
 * | 【数据流向】                                                                |
 * |   业务模块 -> ScopeConfig.getConfig() -> IConfig实例(缓存)                   |
 * |       -> SmartTO1000Config(创建) -> BaseConfig(初始化)                       |
 * |       -> PropertyManage(读取授权)                                            |
 * |                                                                            |
 * | 【依赖关系】                                                                |
 * |   上层依赖: 所有需要查询功能授权的业务模块                                    |
 * |   下层依赖: IConfig(接口), SmartTO1000Config(实现类),                        |
 * |             MemDepthFactory(存储深度常量)                                    |
 * |                                                                            |
 * | 【使用示例】                                                                |
 * *   // 获取配置实例                                                           |
 * *   IConfig config = ScopeConfig.getConfig();                                 |
 * *   // 查询功能授权                                                          |
 * *   if (config.isEnableFreqCounter()) {                                       |
 * *       // 启用频率计数器UI                                                   |
 * *   }                                                                        |
 * |                                                                            |
 * +============================================================================+
 *
 * @author zhuzh
 * @since 2018-12-05
 */
public class ScopeConfig {

    /** 配置实例缓存，懒加载初始化，首次调用getConfig()时创建 */
    private static IConfig config = null; // 配置实例缓存，null表示未初始化

    /**
     * 获取全局配置实例（懒加载单例）。
     *
     * <p>首次调用时创建配置实例并缓存，后续调用直接返回缓存实例。
     * 当前默认创建SmartTO1000Config实例，存储深度为1800M。</p>
     *
     * <p>注意：本方法非线程安全，若在多线程环境下首次调用可能创建多个实例。
     * 在当前Android应用中，配置初始化在主线程完成，不存在并发问题。</p>
     *
     * @return IConfig配置实例，不为null
     */
    public static IConfig getConfig(){
        if(config == null){                                          // 判断缓存实例是否为空
            config = Config();                                       // 首次调用，创建配置实例
        }
        return config;                                               // 返回缓存或新创建的配置实例
    }

    /**
     * 创建具体的配置实现类实例。
     *
     * <p>当前硬编码创建SmartTO1000Config实例，存储深度指定为1800M。
     * 后续可根据产品型号（如通过Build.PRODUCT判断）动态选择不同的配置实现类。</p>
     *
     * @return SmartTO1000Config实例，存储深度为MEM_DEPTH_1800M
     */
    private static IConfig Config(){
        config = new SmartTO1000Config( MemDepthFactory.MEM_DEPTH_1800M); // 创建SmartTO1000Config，指定1800M存储深度
        return config;                                               // 返回新创建的配置实例
    }
}
