package com.micsig.tbook.tbookscope.config; // 配置模块包，负责设备功能授权与参数配置

import android.os.Build;                              // Android系统构建信息类，提供PRODUCT等设备标识

import com.micsig.tbook.hardware.Hardware;            // 硬件抽象类（本文件未直接使用，保留兼容）
import com.micsig.tbook.hardware.HardwareProduct;     // 硬件产品型号工具类，提供产品型号判断方法
import com.micsig.tbook.scope.Sample.MemDepthFactory; // 存储深度工厂，提供存储深度配置与查询
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 水平轴类，定义时基档位常量
import com.micsig.tbook.scope.vertical.VerticalAxis;     // 垂直轴类，定义灵敏度档位常量

/**
 * +============================================================================+
 * |                  SmartTO1000Config - SmartTO1000产品配置类                    |
 * +============================================================================+
 * |                                                                            |
 * | 【模块定位】                                                                |
 * |   MHO系列示波器中SmartTO1000产品型号的配置实现类，继承BaseConfig抽象基类，      |
 * |   定义SmartTO1000产品特有的垂直/水平档位范围和产品合法性校验逻辑。              |
 * |                                                                            |
 * | 【核心职责】                                                                |
 * |   1. 定义SmartTO1000产品的垂直灵敏度档位范围（1mV/div ~ 5V/10V）             |
 * |   2. 定义SmartTO1000产品的水平时基档位范围（1KS/div ~ 250pS/1nS）            |
 * |   3. 实现产品合法性校验（检查Build.PRODUCT是否以"rk3588_MHO"开头）            |
 * |   4. 支持构造时指定存储深度上限（不超过BaseConfig已配置值）                    |
 * |                                                                            |
 * | 【架构设计】                                                                |
 * |   继承BaseConfig模板方法模式，实现4个抽象方法提供产品特定参数：                 |
 * |   - getMinVerticalGear(): 1mV/div（所有SmartTO1000型号一致）                 |
 * |   - getMaxVerticalGear(): MHO68V1为5V/div，其他为10V/div                    |
 * |   - getMinHorizontalGear(): 1KS/div（所有SmartTO1000型号一致）               |
 * |   - getMaxHorizontalGear(): MHO38/MHO28为1nS/div，其他为250pS/div           |
 * |   - isValidProduct(): 检查产品名前缀"rk3588_MHO"                             |
 * |                                                                            |
 * |   IConfig (接口)                                                            |
 * |      |                                                                      |
 * |      +-- BaseConfig (抽象基类)                                              |
 * |              |                                                               |
 * |              +-- SmartTO1000Config ← 本类                                   |
 * |                                                                            |
 * | 【数据流向】                                                                |
 * *   ScopeConfig.Config() -> SmartTO1000Config(memDepth)                       |
 * *       -> BaseConfig构造函数(读取授权,配置硬件参数)                            |
     *       -> 子类抽象方法(提供档位参数)                                         |
 * |                                                                            |
 * | 【依赖关系】                                                                |
 * |   上层依赖: ScopeConfig(工厂类创建本类实例)                                  |
 * |   下层依赖: BaseConfig(继承), VerticalAxis(垂直档位常量),                     |
 * |             HorizontalAxis(时基档位常量), HardwareProduct(产品型号判断),      |
 * |             MemDepthFactory(存储深度配置), Build(设备产品标识)               |
 * |                                                                            |
 * | 【使用示例】                                                                |
 * *   // 通过工厂创建（推荐）                                                   |
 * *   IConfig config = ScopeConfig.getConfig();                                 |
 * *   // 直接创建（指定存储深度）                                                |
 * *   SmartTO1000Config config = new SmartTO1000Config(MemDepthFactory.MEM_DEPTH_1800M); |
 * |                                                                            |
 * +============================================================================+
 *
 * @author zhuzh
 * @since 2018-12-05
 */
public class SmartTO1000Config extends BaseConfig {

    /**
     * 默认构造函数：使用设备默认存储深度创建配置。
     *
     * <p>调用{@link MemDepthFactory#getDefaultMemDepth()}获取默认存储深度，
     * 再委托给{@link #SmartTO1000Config(int)}完成初始化。</p>
     */
    public SmartTO1000Config(){
        this(MemDepthFactory.getDefaultMemDepth());                  // 委托给带参构造函数，使用默认存储深度
    }

    /**
     * 指定存储深度的构造函数。
     *
     * <p>先调用父类BaseConfig构造函数完成授权读取和硬件参数配置，
     * 再根据指定的存储深度值调整：若指定值小于BaseConfig已配置的存储深度，
     * 则将存储深度下调为指定值；否则保持BaseConfig的配置不变。</p>
     *
     * @param mem 期望的存储深度值（采样点数），如MemDepthFactory.MEM_DEPTH_1800M
     */
    public SmartTO1000Config(int mem){
        super();                                                     // 调用BaseConfig构造函数，完成授权读取和硬件配置
        if(mem < getMemDepth()){                                     // 若指定存储深度小于当前已配置值
            setMemDepth(mem);                                        // 将存储深度下调为指定值
        }
    }

    /**
     * 获取SmartTO1000产品的最小垂直灵敏度档位。
     *
     * <p>所有SmartTO1000型号的最小垂直档位均为1mV/div，
     * 对应常量{@link VerticalAxis#DANG_1mV}。</p>
     *
     * @return 最小垂直档位常量 VerticalAxis.DANG_1mV（1mV/div）
     */
    @Override
    protected int getMinVerticalGear() {
        return VerticalAxis.DANG_1mV;                                // 返回1mV/div最小垂直档位
    }

    /**
     * 获取SmartTO1000产品的最大垂直灵敏度档位。
     *
     * <p>根据硬件产品型号区分：</p>
     * <ul>
     *   <li>MHO68V1型号：最大5V/div（{@link VerticalAxis#DANG_5V}）</li>
     *   <li>其他型号：最大10V/div（{@link VerticalAxis#DANG_10V}）</li>
     * </ul>
     *
     * @return 最大垂直档位常量，MHO68V1为DANG_5V，其他为DANG_10V
     */
    @Override
    protected int getMaxVerticalGear() {
        return HardwareProduct.isMHO68V1() ? VerticalAxis.DANG_5V : VerticalAxis.DANG_10V; // MHO68V1为5V/div，其他为10V/div
    }

    /**
     * 获取SmartTO1000产品的最小水平时基档位。
     *
     * <p>所有SmartTO1000型号的最小时基档位均为1KS/div，
     * 对应常量{@link HorizontalAxis#TSI_1KS}。</p>
     *
     * @return 最小时基档位常量 HorizontalAxis.TSI_1KS（1KS/div）
     */
    @Override
    protected int getMinHorizontalGear() {
        return HorizontalAxis.TSI_1KS;                               // 返回1KS/div最小时基档位
    }

    /**
     * 获取SmartTO1000产品的最大水平时基档位。
     *
     * <p>根据设备产品型号区分：</p>
     * <ul>
     *   <li>RK3588_MHO38_V1 / RK3588_MHO28_V1：最大1nS/div
     *       （{@link HorizontalAxis#TSI_1nS}）</li>
     *   <li>其他型号（如MHO68等）：最大250pS/div
     *       （{@link HorizontalAxis#TSI_250pS}）</li>
     * </ul>
     *
     * @return 最大时基档位常量，MHO38/MHO28为TSI_1nS，其他为TSI_250pS
     */
    @Override
    protected int getMaxHorizontalGear() {
        switch(Build.PRODUCT){                                       // 根据Android设备产品标识判断
            case HardwareProduct.RK3588_MHO38_V1:                    // MHO38 V1型号
            case HardwareProduct.RK3588_MHO28_V1:                    // MHO28 V1型号
                return HorizontalAxis.TSI_1nS;                       // 返回1nS/div最大时基档位
        }
        return HorizontalAxis.TSI_250pS;                             // 其他型号返回250pS/div最大时基档位
    }


    /**
     * 校验当前设备是否为合法的SmartTO1000产品。
     *
     * <p>通过检查{@link Build#PRODUCT}字符串是否以"rk3588_MHO"前缀开头，
     * 判断当前运行设备是否为MHO系列合法硬件产品。</p>
     *
     * @return true - 当前设备为合法MHO产品（产品名以"rk3588_MHO"开头）；
     *         false - 非法设备或未识别型号
     */
    @Override
    public boolean isValidProduct() {
        return Build.PRODUCT.startsWith("rk3588_MHO");               // 检查产品名是否以"rk3588_MHO"开头
    }
}
