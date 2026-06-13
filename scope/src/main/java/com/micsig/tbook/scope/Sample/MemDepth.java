package com.micsig.tbook.scope.Sample;


import com.micsig.tbook.scope.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * 存储深度管理抽象基类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Sample（示波器采样管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 存储深度管理抽象层</li>
 *   <li>设计模式：抽象类 + 模板方法模式</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现IMemDepth接口的通用方法</li>
 *   <li>管理存储深度档位列表</li>
 *   <li>协调存储深度变化事件</li>
 *   <li>为子类提供基础框架和公共方法</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>封装存储深度管理的公共逻辑</li>
 *   <li>减少子类重复代码</li>
 *   <li>定义存储深度变化的统一处理流程</li>
 *   <li>提供档位列表管理能力</li>
 * </ul>
 * 
 * <p><b>继承结构：</b>
 * <pre>
 * IMemDepth (接口)
 *   │
 *   └── MemDepth (抽象类) ← 当前类
 *          │
 *          ├── MemDepth1800M (1800M点存储深度实现)
 *          ├── MemDepth360M (360M点存储深度实现)
 *          └── MemDepth36M (36M点存储深度实现)
 * </pre>
 * 
 * <p><b>核心功能：</b>
 * <ul>
 *   <li>存储深度基准值管理</li>
 *   <li>档位索引管理</li>
 *   <li>档位名称列表管理</li>
 *   <li>存储深度变化事件触发</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：Scope（获取通道数）</li>
 *   <li>依赖：MemDepthAction（存储深度变化处理）</li>
 *   <li>被依赖：MemDepth1800M/MemDepth360M/MemDepth36M</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>作为存储深度实现的基类</li>
 *   <li>子类通过addItem()添加档位名称</li>
 *   <li>子类实现getSampleMemDepth()等抽象方法</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/13
 * @see IMemDepth 存储深度接口
 * @see MemDepthAction 存储深度动作处理
 * @see MemDepth1800M 1800M点存储深度实现
 * @see MemDepth360M 360M点存储深度实现
 * @see MemDepth36M 36M点存储深度实现
 */
public abstract class MemDepth implements IMemDepth {
    
    /**
     * 存储深度基准值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>该实现支持的最大存储深度</li>
     *   <li>单通道模式下的最大样本点数</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>MemDepth1800M: 1,800,000,000</li>
     *   <li>MemDepth360M: 360,000,000</li>
     *   <li>MemDepth36M: 36,000,000</li>
     * </ul>
     */
    private int memDepth = 0;
    
    /**
     * 当前存储深度档位索引
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用户当前选择的存储深度档位</li>
     *   <li>索引对应ItemName列表中的位置</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <ul>
     *   <li>0: Auto（自动模式）</li>
     *   <li>1-5: 固定档位</li>
     * </ul>
     */
    private int memDepthItemIdx = 0;
    
    /**
     * 存储深度动作处理器
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>处理存储深度变化事件</li>
     *   <li>触发FPGA命令发送</li>
     *   <li>更新相关配置</li>
     * </ul>
     */
    private MemDepthAction memDepthAction;
    
    /**
     * 存储深度档位名称列表
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>存储所有可选的存储深度档位名称</li>
     *   <li>子类通过addItem()方法添加档位</li>
     * </ul>
     * 
     * <p><b>典型内容：</b>
     * <pre>
     * ["Auto", "1800/900/450M", "180/90/45M", "18/9/4.5M", "1800/900/450K", "180/90/45K"]
     * </pre>
     */
    private List<String> ItemName = new ArrayList();
    
    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>初始化存储深度基准值</li>
     *   <li>创建存储深度动作处理器</li>
     * </ul>
     * 
     * <p><b>调用时机：</b>
     * <ul>
     *   <li>子类构造时调用</li>
     *   <li>MemDepthFactory创建存储深度对象时</li>
     * </ul>
     * 
     * @param memDepth 存储深度基准值（单位：点）
     */
    public MemDepth(int memDepth) {
        // 保存存储深度基准值
        this.memDepth = memDepth;
        // 创建存储深度动作处理器，传入当前对象
        memDepthAction = new MemDepthAction(this);
    }
    
    /**
     * 获取存储深度基准值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回构造时设置的存储深度基准值</li>
     *   <li>该值是单通道模式下的最大存储深度</li>
     * </ul>
     * 
     * @return 存储深度基准值（单位：点）
     */
    @Override
    public int getMemDepth() {
        return memDepth;
    }

    /**
     * 获取存储深度档位数量
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回档位名称列表的大小</li>
     *   <li>表示可选的存储深度档位数量</li>
     * </ul>
     * 
     * @return 存储深度档位数量
     */
    @Override
    public int getMemDepthItemNum() {
        return ItemName.size();
    }

    /**
     * 获取存储深度档位名称列表
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回所有可选的存储深度档位名称</li>
     *   <li>列表由子类通过addItem()方法填充</li>
     * </ul>
     * 
     * @return 存储深度档位名称列表
     */
    @Override
    public List<String> getMemDepthItemName() {
        return ItemName;
    }

    /**
     * 获取初始化时的存储深度档位名称
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>默认实现返回空字符串</li>
     *   <li>子类可重写此方法提供默认档位名称</li>
     * </ul>
     * 
     * <p><b>注意：</b>子类通常重写返回"Auto"
     * 
     * @return 初始存储深度档位名称
     */
    @Override
    public String getMemDepthInitName() {
        return "";
    }

    /**
     * 设置存储深度档位
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>更新当前档位索引</li>
     *   <li>触发存储深度变化事件</li>
     * </ul>
     * 
     * <p><b>调用链路：</b>
     * <pre>
     * setMemDepthItem() → memDepthAction.memDepthChange()
     *                   → MemDepthAction.memDepthChange()
     *                   → HorizontalAxisAction.memDepthChange()
     *                   → FPGAMessage.add()
     *                   → FPGA命令发送
     * </pre>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>用户通过UI选择存储深度档位</li>
     *   <li>恢复配置时设置档位</li>
     * </ul>
     * 
     * @param idx 存储深度档位索引
     */
    @Override
    public void setMemDepthItem(int idx) {
        // 更新当前档位索引
        memDepthItemIdx = idx;
        // 触发存储深度变化事件
        // 会通知相关模块更新配置并发送FPGA命令
        memDepthAction.memDepthChange();
    }

    /**
     * 获取当前存储深度档位索引
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回当前选中的存储深度档位索引</li>
     *   <li>索引用于从ItemName列表获取档位名称</li>
     * </ul>
     * 
     * @return 当前存储深度档位索引（0表示Auto模式）
     */
    @Override
    public int getMemDepthItem() {
        return memDepthItemIdx;
    }
    
    /**
     * 获取当前通道数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从Scope获取当前示波器的通道数</li>
     *   <li>用于计算实际存储深度（通道数越多，每通道深度越小）</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>子类计算实际存储深度时调用</li>
     *   <li>getSampleMemDepth()方法中使用</li>
     * </ul>
     * 
     * @return 当前通道数
     */
    protected int getChNums() {
        return Scope.getInstance().getChNum();
    }
    
    /**
     * 添加存储深度档位名称
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>向档位名称列表添加新的档位</li>
     *   <li>子类在构造函数中调用此方法初始化档位列表</li>
     * </ul>
     * 
     * <p><b>调用时机：</b>
     * <ul>
     *   <li>子类构造函数中调用</li>
     *   <li>按顺序添加档位名称（顺序即为索引）</li>
     * </ul>
     * 
     * <p><b>示例：</b>
     * <pre>
     * // MemDepth1800M构造函数中
     * addItem("Auto");           // 索引0
     * addItem("1800/900/450M");  // 索引1
     * addItem("180/90/45M");     // 索引2
     * addItem("18/9/4.5M");      // 索引3
     * </pre>
     * 
     * @param itemName 档位名称
     */
    public void addItem(String itemName) {
        ItemName.add(itemName);
    }

    /**
     * 强制触发存储深度变化
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>不改变档位索引，仅触发变化事件</li>
     *   <li>用于重新计算和更新存储深度相关配置</li>
     * </ul>
     * 
     * <p><b>调用场景：</b>
     * <ul>
     *   <li>通道数变化时需要重新计算存储深度</li>
     *   <li>段采样模式切换时</li>
     *   <li>硬件配置变化时</li>
     *   <li>时基档位变化时（Auto模式）</li>
     * </ul>
     * 
     * <p><b>与setMemDepthItem的区别：</b>
     * <ul>
     *   <li>setMemDepthItem: 改变档位索引并触发变化</li>
     *   <li>forceMemDepthChange: 不改变档位索引，仅触发变化</li>
     * </ul>
     */
    @Override
    public void forceMemDepthChange() {
        // 触发存储深度变化事件
        // 用于在档位不变的情况下重新计算和更新配置
        memDepthAction.memDepthChange();
    }


}
