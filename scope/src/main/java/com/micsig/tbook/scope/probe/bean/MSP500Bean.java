package com.micsig.tbook.scope.probe.bean;


import androidx.annotation.NonNull;

import com.micsig.tbook.scope.probe.BaseProbe;
import com.micsig.tbook.scope.probe.ProbeMSP500;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                MSP500Bean - MSP500系列探头配置类                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Probe模块的MSP500系列探头配置类，位于probe.bean包下，                        ║
 * ║   继承BaseBean，为MSP500系列探头提供配置数据和创建探头实例的能力。               ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 定义MSP500探头类型标识常量                                               ║
 * ║   2. 实现createProbe()工厂方法，创建ProbeMSP500实例                           ║
 * ║   3. 提供MSP500配置的克隆功能                                                 ║
 * ║   4. 初始化DA功能支持标志                                                     ║
 * ║   5. 继承BaseBean的所有配置属性和方法                                         ║
 * ║                                                                              ║
 * ║ 【继承体系】                                                                 ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │   Cloneable     │ ← 接口：支持对象克隆             ║
 * ║                          └────────┬────────┘                                 ║
 * ║                                   │                                          ║
 * ║                          ┌────────▼────────┐                                 ║
 * ║                          │    BaseBean     │ ← 父类：探头配置基类             ║
 * ║                          │   (abstract)    │                                 ║
 * ║                          └────────┬────────┘                                 ║
 * ║                                   │                                          ║
 * ║                          ┌────────▼────────┐                                 ║
 * ║                          │   MSP500Bean    │ ← 本类：MSP500探头配置           ║
 * ║                          └─────────────────┘                                 ║
 * ║                                                                              ║
 * ║ 【MSP500探头特性】                                                           ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                      MSP500系列探头                                  │   ║
 * ║   │                                                                     │   ║
 * ║   │   【产品特点】                                                       │   ║
 * ║   │   - 支持DA（数模转换）功能                                           │   ║
 * ║   │   - 高精度测量能力                                                   │   ║
 * ║   │   - 多种探头比例可选                                                 │   ║
 * ║   │   - 自动识别功能支持                                                 │   ║
 * ║   │                                                                     │   ║
 * ║   │   【关键特性】                                                       │   ║
 * ║   │   - bDa = true: 支持DA功能（区别于其他探头）                         │   ║
 * ║   │   - 探头比例：从配置文件加载（scaleNames/scaleValues）               │   ║
 * ║   │   - 默认比例：使用配置中的第一个比例值                               │   ║
 * ║   │                                                                     │   ║
 * ║   │   【典型型号】                                                       │   ║
 * ║   │   - MSP500-100: 100MHz探头                                         │   ║
 * ║   │   - MSP500-200: 200MHz探头                                         │   ║
 * ║   │   - MSP500-350: 350MHz探头                                         │   ║
 * ║   │                                                                     │   ║
 * ║   │   【应用场景】                                                       │   ║
 * ║   │   - 通用电子测量                                                     │   ║
 * ║   │   - 信号分析                                                         │   ║
 * ║   │   - DA输出功能应用                                                   │   ║
 * ║   │   - 自动化测试系统                                                   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【创建流程】                                                                 ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ BeanFactory │───▶│ MSP500Bean  │───▶│ createProbe │                   ║
 * ║   │ .getBean()  │    │  实例化     │    │    ()       │                   ║
 * ║   └─────────────┘    └─────────────┘    └──────┬──────┘                   ║
 * ║                                                 │                          ║
 * ║                                                 ▼                          ║
 * ║                                        ┌─────────────┐                     ║
 * ║                                        │ ProbeMSP500 │                     ║
 * ║                                        │ (克隆配置)  │                     ║
 * ║                                        └─────────────┘                     ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 原型模式：通过clone()创建配置副本，避免共享状态                           ║
 * ║   - 工厂方法模式：createProbe()由子类实现具体探头创建                         ║
 * ║                                                                              ║
 * ║ 【使用示例】                                                                 ║
 * ║   <pre>                                                                      ║
 * ║   // 通过工厂创建MSP500配置                                                  ║
 * ║   MSP500Bean bean = (MSP500Bean) BeanFactory.getInstance().getBean("MSP500");║
 * ║                                                                              ║
 * ║   // 解析JSON配置                                                            ║
 * ║   bean.parseJson(jsonElement);                                              ║
 * ║                                                                              ║
 * ║   // 创建MSP500探头实例                                                      ║
 * ║   BaseProbe probe = bean.createProbe();                                     ║
 * ║                                                                              ║
 * ║   // 检查DA功能支持                                                          ║
 * ║   boolean hasDA = bean.isDa(); // 返回true                                  ║
 * ║   </pre>                                                                     ║
 * ║                                                                              ║
 * ║ 【配置继承】                                                                 ║
 * ║   继承自BaseBean的属性：                                                     ║
 * ║   - prefix: 探头前缀                                                         ║
 * ║   - className: 探头类别                                                      ║
 * ║   - probeType: 探头类型                                                      ║
 * ║   - name: 探头别名                                                           ║
 * ║   - bandwidth: 探头带宽                                                      ║
 * ║   - bDa: DA功能标志（本类初始化为true）                                      ║
 * ║   - scaleNames/scaleValues: 比例尺配置                                       ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 继承BaseBean的线程安全特性                                               ║
 * ║   - clone()方法创建新实例，避免并发问题                                      ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - BaseBean: 父类，提供探头配置基类功能                                     ║
 * ║   - ProbeMSP500: 创建的探头实例类                                            ║
 * ║   - BeanFactory: 工厂类，创建MSP500Bean实例                                  ║
 * ║                                                                              ║
 * ║ 【与MDPBean的区别】                                                          ║
 * ║   ┌──────────────┬──────────────────┬──────────────────┐                   ║
 * ║   │ 特性          │ MSP500Bean       │ MDPBean          │                   ║
 * ║   ├──────────────┼──────────────────┼──────────────────┤                   ║
 * ║   │ DA功能        │ 支持（bDa=true） │ 不支持           │                   ║
 * ║   │ 探头类型      │ 通用探头         │ 差分探头         │                   ║
 * ║   │ MCU数量       │ 单MCU            │ 双MCU            │                   ║
 * ║   │ 应用场景      │ 通用测量         │ 高压差分测量     │                   ║
 * ║   └──────────────┴──────────────────┴──────────────────┘                   ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class MSP500Bean extends BaseBean implements Cloneable{

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * MSP500探头类型标识常量
     * 用于在BeanFactory中注册和查找MSP500配置
     * 
     * <p><b>使用场景：</b></p>
     * <ul>
     *   <li>BeanFactory.initMap()中注册：map.put(MSP500, MSP500Bean.class)</li>
     *   <li>精确查找：BeanFactory.getInstance().getBean(MSP500)</li>
     *   <li>前缀匹配：BeanFactory.getInstance().getMatchBean("MSP500-100")</li>
     * </ul>
     * 
     * <p><b>命名规则：</b></p>
     * <ul>
     *   <li>与类名对应：MSP500Bean → "MSP500"</li>
     *   <li>简洁明了，便于识别</li>
     *   <li>作为JSON配置中的probetype字段值</li>
     * </ul>
     */
    public static final String MSP500 = "MSP500";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 默认构造方法
     * 创建MSP500Bean实例，初始化父类配置和DA功能标志
     * 
     * <p><b>初始化流程：</b></p>
     * <ol>
     *   <li>调用父类BaseBean的构造方法</li>
     *   <li>初始化默认配置值（继承自BaseBean）</li>
     *   <li><b>设置bDa = true</b>：启用DA功能支持</li>
     * </ol>
     * 
     * <p><b>DA功能说明：</b></p>
     * <ul>
     *   <li>DA（数模转换）：Digital-to-Analog的缩写</li>
     *   <li>MSP500探头支持DA输出功能</li>
     *   <li>这是MSP500与MDP等探头的重要区别</li>
     *   <li>通过isDa()方法可查询此功能是否支持</li>
     * </ul>
     * 
     * <p><b>后续操作：</b></p>
     * <ul>
     *   <li>通过parseJson()加载JSON配置</li>
     *   <li>通过setter方法设置具体属性</li>
     * </ul>
     */
    public MSP500Bean(){
        super();                                                                    // 调用父类构造方法
        bDa = true;                                                                 // 设置DA功能支持标志为true
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 工厂方法实现
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 创建MSP500探头实例
     * 实现BaseBean的抽象方法，工厂方法模式的核心
     * 
     * <p><b>创建流程：</b></p>
     * <ol>
     *   <li>克隆当前配置对象（避免共享状态）</li>
     *   <li>使用克隆的配置创建ProbeMSP500实例</li>
     *   <li>捕获并处理克隆异常</li>
     * </ol>
     * 
     * <p><b>设计考虑：</b></p>
     * <ul>
     *   <li><b>原型模式：</b>使用clone()创建配置副本，确保每个探头实例拥有独立的配置</li>
     *   <li><b>解耦：</b>客户端无需知道具体探头类名，通过配置即可创建</li>
     *   <li><b>扩展性：</b>新增探头类型只需创建新的Bean子类</li>
     * </ul>
     * 
     * <p><b>为什么使用克隆？</b></p>
     * <ul>
     *   <li>避免多个探头共享同一配置对象</li>
     *   <li>每个探头可以独立修改配置而不影响其他探头</li>
     *   <li>保证线程安全，避免并发修改问题</li>
     *   <li>保留DA功能标志（bDa=true）的设置</li>
     * </ul>
     * 
     * <p><b>异常处理：</b></p>
     * <ul>
     *   <li>CloneNotSupportedException: 克隆不支持异常</li>
     *   <li>异常时打印堆栈跟踪，返回null</li>
     * </ul>
     *
     * @return ProbeMSP500实例，创建失败返回null
     */
    @Override
    public BaseProbe createProbe() {
        try {
            return new ProbeMSP500(this.clone());                                   // 克隆配置并创建ProbeMSP500实例
        } catch (CloneNotSupportedException e) {                                    // 捕获克隆异常
            e.printStackTrace();                                                    // 打印异常堆栈
        }
        return null;                                                                // 创建失败返回null
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 克隆方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 克隆MSP500Bean实例
     * 重写父类clone()方法，返回MSP500Bean类型
     * 
     * <p><b>克隆策略：</b></p>
     * <ul>
     *   <li>调用父类BaseBean的clone()方法</li>
     *   <li>父类已实现深拷贝（scaleNames、scaleValues列表）</li>
     *   <li>返回MSP500Bean类型，便于直接使用</li>
     * </ul>
     * 
     * <p><b>深拷贝内容：</b></p>
     * <ul>
     *   <li>所有基本类型字段（自动拷贝）</li>
     *   <li>bDa字段（值为true，自动拷贝）</li>
     *   <li>scaleNames列表（父类实现）</li>
     *   <li>scaleValues列表（父类实现）</li>
     * </ul>
     * 
     * <p><b>使用场景：</b></p>
     * <ul>
     *   <li>createProbe()方法中创建配置副本</li>
     *   <li>需要独立配置实例时</li>
     * </ul>
     *
     * @return 克隆的MSP500Bean实例
     * @throws CloneNotSupportedException 克隆不支持异常
     */
    @NonNull
    @Override
    protected MSP500Bean clone() throws CloneNotSupportedException {

        return (MSP500Bean) super.clone();                                          // 调用父类克隆并转换为MSP500Bean类型
    }
}
