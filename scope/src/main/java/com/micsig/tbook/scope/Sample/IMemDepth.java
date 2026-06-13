package com.micsig.tbook.scope.Sample;

import java.util.List;

/**
 * 存储深度（记录长度）管理接口
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Sample（示波器采样管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 存储深度管理接口</li>
 *   <li>设计模式：接口抽象 + 多实现（策略模式）</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义存储深度管理的统一接口规范</li>
 *   <li>提供存储深度档位配置能力</li>
 *   <li>支持自动和手动存储深度模式</li>
 *   <li>提供时基与存储深度的关联计算</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>抽象存储深度管理行为，支持不同硬件配置</li>
 *   <li>为不同型号示波器（1800M/360M/36M）提供统一接口</li>
 *   <li>实现存储深度与时基档位的自动匹配</li>
 *   <li>支持段采样模式下的存储深度计算</li>
 * </ul>
 * 
 * <p><b>实现结构：</b>
 * <pre>
 * IMemDepth (接口)
 *   │
 *   └── MemDepth (抽象类)
 *          │
 *          ├── MemDepth1800M (1800M点存储深度实现)
 *          ├── MemDepth360M (360M点存储深度实现)
 *          └── MemDepth36M (36M点存储深度实现)
 * </pre>
 * 
 * <p><b>存储深度概念：</b>
 * <ul>
 *   <li>记录长度：示波器一次采集能够存储的样本点数量</li>
 *   <li>存储深度越大，能够捕获的信号时间越长</li>
 *   <li>存储深度与采样率、时基档位相关联</li>
 *   <li>多通道采样时，存储深度按通道数分配</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>被依赖：MemDepthFactory（存储深度工厂）</li>
 *   <li>被依赖：Sample（采样状态管理）</li>
 *   <li>被依赖：HorizontalAxisAction（水平轴动作处理）</li>
 *   <li>被依赖：MemDepthAction（存储深度动作处理）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>用户切换存储深度档位时</li>
 *   <li>用户调整时基档位时自动计算存储深度</li>
 *   <li>段采样模式下计算段存储深度</li>
 *   <li>数学运算通道配置耦合数组</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/13
 * @see MemDepth 存储深度抽象基类
 * @see MemDepth1800M 1800M点存储深度实现
 * @see MemDepth360M 360M点存储深度实现
 * @see MemDepth36M 36M点存储深度实现
 * @see MemDepthFactory 存储深度工厂
 * @see Sample 采样状态管理
 */
public interface IMemDepth {

    /**
     * 获取存储深度基准值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回该实现支持的最大存储深度基准值</li>
     *   <li>该值是单通道模式下的最大存储深度</li>
     * </ul>
     * 
     * <p><b>返回值说明：</b>
     * <ul>
     *   <li>MemDepth1800M: 返回 1,800,000,000</li>
     *   <li>MemDepth360M: 返回 360,000,000</li>
     *   <li>MemDepth36M: 返回 36,000,000</li>
     * </ul>
     * 
     * @return 存储深度基准值（单位：点）
     */
    int getMemDepth();

    /**
     * 获取存储深度档位数量
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回可选的存储深度档位数量</li>
     *   <li>通常包括：Auto、最大值、中间值、最小值等</li>
     * </ul>
     * 
     * <p><b>典型档位：</b>
     * <ul>
     *   <li>0: Auto（自动模式）</li>
     *   <li>1: 最大档位（如1800M/900M/450M）</li>
     *   <li>2-5: 中间档位</li>
     * </ul>
     * 
     * @return 存储深度档位数量
     */
    int getMemDepthItemNum();

    /**
     * 获取存储深度档位名称列表
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回所有可选的存储深度档位名称</li>
     *   <li>名称格式通常为"1800/900/450M"表示不同通道数下的深度</li>
     * </ul>
     * 
     * <p><b>示例返回值：</b>
     * <pre>
     * ["Auto", "1800/900/450M", "180/90/45M", "18/9/4.5M", "1800/900/450K", "180/90/45K"]
     * </pre>
     * 
     * @return 存储深度档位名称列表
     */
    List<String> getMemDepthItemName();

    /**
     * 获取初始化时的存储深度档位名称
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回示波器启动时的默认存储深度档位</li>
     *   <li>通常返回"Auto"表示自动模式</li>
     * </ul>
     * 
     * @return 初始存储深度档位名称
     */
    String getMemDepthInitName();

    /**
     * 设置存储深度档位
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据索引设置当前存储深度档位</li>
     *   <li>会触发存储深度变化事件</li>
     * </ul>
     * 
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>idx=0: Auto（自动模式）</li>
     *   <li>idx=1: 最大档位</li>
     *   <li>idx=2-5: 其他档位</li>
     * </ul>
     * 
     * <p><b>调用场景：</b>
     * <ul>
     *   <li>用户通过UI选择存储深度档位</li>
     *   <li>恢复出厂设置</li>
     * </ul>
     * 
     * @param idx 存储深度档位索引
     */
    void setMemDepthItem(int idx);

    /**
     * 获取当前存储深度档位索引
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回当前选中的存储深度档位索引</li>
     * </ul>
     * 
     * @return 当前存储深度档位索引（0表示Auto模式）
     */
    int getMemDepthItem();

    /**
     * 获取实际采样存储深度（自动获取通道数）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据当前通道配置自动计算实际存储深度</li>
     *   <li>内部调用getSampleMemDepth(chCnt)</li>
     * </ul>
     * 
     * <p><b>计算逻辑：</b>
     * <ul>
     *   <li>获取当前采样通道数</li>
     *   <li>根据档位和通道数计算实际存储深度</li>
     * </ul>
     * 
     * @return 实际采样存储深度（单位：点）
     */
    int getSampleMemDepth();

    /**
     * 获取实际采样存储深度（指定通道数）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据指定通道数计算实际存储深度</li>
     *   <li>核心方法：实现存储深度的具体计算逻辑</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * 实际存储深度 = 基准值 ÷ 通道系数
     * 通道系数 = 通道数 ÷ 2
     * 
     * 例如：
     *   基准值 = 18,000,000
     *   通道数 = 4
     *   通道系数 = 4 ÷ 2 = 2
     *   实际存储深度 = 18,000,000 ÷ 2 = 9,000,000点
     * </pre>
     * 
     * <p><b>Auto模式计算：</b>
     * <ul>
     *   <li>快速时基（≤50ms/div）：使用最大存储深度</li>
     *   <li>慢速时基（>50ms/div）：根据时基自动计算</li>
     * </ul>
     * 
     * @param chCnt 采样通道数（2/4/8）
     * @return 实际采样存储深度（单位：点）
     */
    int getSampleMemDepth(int chCnt);

    /**
     * 获取时基档位对应的存储深度比例
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算指定时基档位下的存储深度调整比例</li>
     *   <li>用于Auto模式下自动匹配存储深度</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>时基档位变化时计算合适的存储深度</li>
     *   <li>判断是否需要调整存储深度</li>
     * </ul>
     * 
     * @param timeScaleId 时基档位ID
     * @return 存储深度比例（1.0表示标准比例）
     */
    double getTimeScaleRatio(int timeScaleId);

    /**
     * 获取时基档位对应的存储深度比例（指定档位）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算指定时基档位和存储深度档位下的比例</li>
     *   <li>用于预计算或比较不同档位组合</li>
     * </ul>
     * 
     * @param timeScaleId 时基档位ID
     * @param memDepthItemIdx 存储深度档位索引
     * @return 存储深度比例
     */
    double getTimeScaleRatio(int timeScaleId, int memDepthItemIdx);

    /**
     * 判断是否为特殊时基档位
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>判断当前时基是否需要特殊处理</li>
     *   <li>特殊时基通常指滚屏模式或慢时基模式</li>
     * </ul>
     * 
     * <p><b>特殊时基：</b>
     * <ul>
     *   <li>滚屏模式：时基>100ms/div且启用滚屏</li>
     *   <li>慢时基模式：时基>100ms/div且禁用滚屏</li>
     * </ul>
     * 
     * @return true表示特殊时基，false表示正常时基
     */
    boolean isSpecialTimeScale();

    /**
     * 获取数学运算通道耦合数组
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回数学运算通道可用的耦合通道配置</li>
     *   <li>用于数学运算功能（加减乘除FFT等）</li>
     * </ul>
     * 
     * <p><b>返回值说明：</b>
     * <ul>
     *   <li>数组元素为可用的源通道索引</li>
     *   <li>根据存储深度和通道配置动态计算</li>
     * </ul>
     * 
     * @return 数学运算耦合通道数组
     */
    int[] getMathCouArray();

    /**
     * 强制触发存储深度变化
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>强制触发存储深度变化事件</li>
     *   <li>用于重新计算和更新存储深度相关配置</li>
     * </ul>
     * 
     * <p><b>调用场景：</b>
     * <ul>
     *   <li>通道数变化时</li>
     *   <li>段采样模式切换时</li>
     *   <li>硬件配置变化时</li>
     * </ul>
     */
    void forceMemDepthChange();

    /**
     * 将存储深度字符串转换为菜单索引
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据存储深度名称查找对应的菜单索引</li>
     *   <li>用于配置恢复或设置</li>
     * </ul>
     * 
     * <p><b>示例：</b>
     * <pre>
     * memDepth2menuIdx("Auto") → 0
     * memDepth2menuIdx("1800/900/450M") → 1
     * </pre>
     * 
     * @param memDepth 存储深度名称
     * @return 菜单索引，未找到返回-1
     */
    int memDepth2menuIdx(String memDepth);
}
