package com.micsig.tbook.scope.channel;  // 包声明：示波器通道模块

import com.micsig.base.Logger;  // 导入：基础日志工具类
import com.micsig.tbook.scope.Data.IBufferQueue;  // 导入：数据缓冲队列接口，用于波形数据缓冲管理
import com.micsig.tbook.scope.Data.IDataBuffer;  // 导入：数据缓冲接口，用于波形数据存储
import com.micsig.tbook.scope.Event.EventFactory;  // 导入：事件工厂类，用于发送事件通知
import com.micsig.tbook.scope.ScopeBase;  // 导入：示波器基础配置类，提供显示参数和坐标转换
import com.micsig.tbook.scope.measure.Measure;  // 导入：测量类，用于波形参数测量


/**
 * 通道抽象基类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.channel（示波器通道模块）</li>
 *   <li>架构层级：业务逻辑层 - 通道抽象基类</li>
 *   <li>设计模式：模板方法模式，实现IChannel接口，为子类提供通用实现</li>
 *   <li>继承关系：实现IChannel接口，被Channel/RefChannel/MathChannel继承</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义通道的基础属性：位置、开关状态、标签、Z序等</li>
 *   <li>管理通道的波形数据缓冲队列</li>
 *   <li>提供通道的测量功能支持</li>
 *   <li>实现通道的垂直模式管理（零点模式/屏幕中心模式）</li>
 *   <li>提供波形数据保存功能（二进制/CSV格式）</li>
 * </ul>
 * 
 * <p><b>通道属性说明：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────┐
 * │ 属性          │ 类型      │ 说明                              │
 * ├───────────────┼───────────┼───────────────────────────────────┤
 * │ zOrder        │ int       │ Z序，控制通道显示层级             │
 * │ pos           │ double    │ 垂直位置，FPGA坐标系统            │
 * │ bOpen         │ boolean   │ 通道开关状态                      │
 * │ chIdx         │ int       │ 通道索引（0-7）                   │
 * │ bWaveValid    │ boolean   │ 波形数据是否有效                  │
 * │ label         │ String    │ 通道标签                          │
 * │ name          │ String    │ 通道名称                          │
 * │ verticalMode  │ int       │ 垂直模式（零点/屏幕中心）         │
 * │ centerVal     │ double    │ 中心电压值                        │
 * └───────────────┴───────────┴───────────────────────────────────┘
 * </pre>
 * 
 * <p><b>垂直模式说明：</b>
 * <pre>
 * 1. VERTICAL_MODE_CH_ZERO（零点模式，默认）：
 *    - 通道位置以通道零点为基准
 *    - 档位切换时，零点位置不变
 *    - 适用于正常测量场景
 * 
 * 2. VERTICAL_MODE_SCREEN_CENTER（屏幕中心模式）：
 *    - 通道位置以屏幕中心为基准
 *    - 档位切换时，波形保持在屏幕中心
 *    - 适用于观察波形相对位置的场景
 * </pre>
 * 
 * <p><b>坐标系统说明：</b>
 * <pre>
 * FPGA坐标系统：
 *   - 高度固定为1000像素
 *   - 位置值范围：-500 ~ +500（屏幕中心为0）
 *   - 用于波形数据处理和存储
 * 
 * UI坐标系统：
 *   - 高度根据屏幕分辨率变化
 *   - 位置值范围：-Height/2 ~ +Height/2
 *   - 用于界面显示
 * 
 * 转换公式：
 *   UI坐标 = FPGA坐标 × ToUICoff
 *   FPGA坐标 = UI坐标 × ToFPGACoff
 * </pre>
 * 
 * <p><b>数据缓冲管理：</b>
 * <pre>
 * 使用生产者-消费者模式管理波形数据：
 *   - obtain()：从缓冲池获取空闲缓冲区
 *   - recycle()：将缓冲区归还缓冲池
 *   - dequeue()：从队列取出已填充的缓冲区
 *   - enqueue()：将填充好的缓冲区放入队列
 * </pre>
 * 
 * <p><b>继承关系：</b>
 * <pre>
 *                IChannel（接口）
 *                    │
 *                    ▼
 *              BaseChannel（抽象基类）
 *                    │
 *     ┌──────────────┼──────────────┐
 *     │              │              │
 *     ▼              ▼              ▼
 *  Channel      RefChannel    MathChannel
 * （物理通道）  （参考通道）   （数学通道）
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>实现：IChannel（通道接口）</li>
 *   <li>依赖：IBufferQueue（数据缓冲队列）</li>
 *   <li>依赖：IDataBuffer（数据缓冲）</li>
 *   <li>依赖：Measure（测量功能）</li>
 *   <li>依赖：ScopeBase（基础配置）</li>
 *   <li>依赖：EventFactory（事件通知）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>作为所有通道类型的基类，提供通用功能</li>
 *   <li>通道管理器创建通道时使用子类实例</li>
 *   <li>波形显示时获取通道位置和状态</li>
 *   <li>测量功能通过通道获取波形数据</li>
 * </ul>
 * 
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>所有属性访问都使用synchronized保护</li>
 *   <li>volatile关键字保证可见性</li>
 *   <li>适用于多线程环境（UI线程+数据处理线程）</li>
 * </ul>
 * 
 * @author zhuzh  // 作者：zhuzh
 * @version 1.0  // 版本号：1.0
 * @since 2018/3/15  // 创建日期：2018年3月15日
 * @see IChannel 通道接口  // 参见：IChannel接口
 * @see Channel 物理通道实现  // 参见：Channel类
 * @see RefChannel 参考通道实现  // 参见：RefChannel类
 * @see MathChannel 数学通道实现  // 参见：MathChannel类
 * @see Measure 测量功能类  // 参见：Measure类
 */
/**
 * Created by zhuzh on 2018/3/15.
 */
public abstract class BaseChannel implements IChannel {  // 继承IChannel接口，实现通道抽象基类
    
    /**
     * 日志标签
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于日志输出时标识BaseChannel类</li>
     *   <li>便于日志过滤和调试</li>
     * </ul>
     */
    private static final String TAG = "BaseChannel";  // 日志标签，固定为"BaseChannel"
    
    /**
     * Z序（显示层级）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>控制通道波形在屏幕上的显示层级</li>
     *   <li>值越大，显示越靠前（上层）</li>
     *   <li>用于处理多通道波形重叠时的显示顺序</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <pre>
     * 默认值：0
     * 取值范围：0 ~ N（N为通道数量）
     * </pre>
     */
    private volatile int zOrder = 0;  // Z序，初始为0，volatile保证可见性
    
    /**
     * 垂直位置（FPGA坐标系统）
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>通道波形在屏幕上的垂直位置</li>
     *   <li>使用FPGA坐标系统，高度固定为1000</li>
     *   <li>0表示屏幕中心，正值向上，负值向下</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <pre>
     * 默认值：0（屏幕中心）
     * 取值范围：-500 ~ +500（对应屏幕上下边界）
     * 单位：像素（FPGA坐标系统）
     * </pre>
     * 
     * <p><b>坐标转换：</b>
     * <pre>
     * UI坐标 = pos × ToUICoff
     * </pre>
     */
    private volatile double pos = 0;  // 垂直位置，初始为0（屏幕中心），volatile保证可见性，注释：1000对应的位置
    
    /**
     * 通道开关状态
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>控制通道是否启用</li>
     *   <li>true：通道打开，显示波形</li>
     *   <li>false：通道关闭，不显示波形</li>
     * </ul>
     */
    private volatile boolean bOpen = false;  // 通道开关状态，初始为关闭，volatile保证可见性
    
    /**
     * 通道索引
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>通道的唯一标识符</li>
     *   <li>用于在通道数组中定位通道</li>
     * </ul>
     * 
     * <p><b>取值范围：</b>
     * <pre>
     * 物理通道：0 ~ 7（CH1~CH8）
     * 参考通道：8 ~ 15（REF1~REF8）
     * 数学通道：16 ~ 23（MATH1~MATH8）
     * 无效值：-1
     * </pre>
     */
    private volatile int chIdx = -1;  // 通道索引，初始为-1（无效），volatile保证可见性
    
    /**
     * 波形数据有效标志
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>标识当前通道是否有有效的波形数据</li>
     *   <li>true：有有效波形数据</li>
     *   <li>false：无有效波形数据</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>波形保存前检查数据有效性</li>
     *   <li>测量前检查数据有效性</li>
     * </ul>
     */
    private volatile boolean bWaveValid = false;  // 波形数据有效标志，初始为无效，volatile保证可见性
    
    /**
     * 数据缓冲队列
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>管理通道的波形数据缓冲区</li>
     *   <li>使用生产者-消费者模式</li>
     *   <li>支持多个缓冲区的循环使用</li>
     * </ul>
     * 
     * <p><b>操作方法：</b>
     * <pre>
     * obtain()：获取空闲缓冲区
     * recycle()：归还缓冲区
     * dequeue()：取出已填充缓冲区
     * enqueue()：放入已填充缓冲区
     * </pre>
     */
    private IBufferQueue dataBufferQueue = null;  // 数据缓冲队列，初始为null
    
    /**
     * 测量对象
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>负责通道的波形参数测量</li>
     *   <li>支持多种测量类型（周期、频率、幅值等）</li>
     *   <li>支持自动测量和光标测量</li>
     * </ul>
     */
    private Measure measure = null;  // 测量对象，初始为null
    
    /**
     * 通道标签
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用户自定义的通道标签</li>
     *   <li>显示在通道信息区域</li>
     *   <li>用于标识通道用途</li>
     * </ul>
     */
    private volatile String label;  // 通道标签，volatile保证可见性

    /**
     * 通道名称
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>通道的系统名称</li>
     *   <li>如"CH1"、"CH2"、"REF1"、"MATH1"等</li>
     *   <li>用于界面显示和日志输出</li>
     * </ul>
     */
    private String name;  // 通道名称


    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>初始化通道的基本属性</li>
     *   <li>创建测量对象并初始化</li>
     * </ul>
     * 
     * <p><b>初始化流程：</b>
     * <pre>
     * 1. 保存通道名称
     * 2. 保存通道索引
     * 3. 创建测量对象
     * 4. 设置测量波形高度
     * 5. 设置测量高度比例
     * </pre>
     * 
     * @param chIdx 通道索引（0-7为物理通道，8-15为参考通道，16-23为数学通道）
     * @param name 通道名称（如"CH1"、"REF1"、"MATH1"等）
     */
    public BaseChannel(int chIdx, String name) {  // 构造函数，接收通道索引和名称
        this.name = name;  // 保存通道名称
        this.chIdx = chIdx;  // 保存通道索引
        measure = new Measure(this);  // 创建测量对象，绑定当前通道
        measure.WaveHeight(ScopeBase.getHeight());  // 设置测量波形高度为示波器显示高度
        measure.WaveHeightRate(1.0f);  // 设置测量高度比例为1.0（100%）
    }  // 构造函数结束

    /**
     * 获取通道名称
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>返回通道的系统名称</li>
     * </ul>
     * 
     * @return 通道名称（如"CH1"、"REF1"、"MATH1"等）
     */
    @Override  // 覆盖IChannel接口方法
    public String getName() {  // 获取通道名称方法
        return name;  // 返回通道名称
    }  // 方法结束

    /**
     * 获取测量对象
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>返回通道的测量对象</li>
     *   <li>用于执行波形参数测量</li>
     * </ul>
     * 
     * @return 测量对象
     */
    public Measure getMeasure() {  // 获取测量对象方法
        return measure;  // 返回测量对象
    }  // 方法结束

    /**
     * 获取Z序
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>返回通道的显示层级</li>
     * </ul>
     * 
     * @return Z序值，值越大显示越靠前
     */
    public int getzOrder() {  // 获取Z序方法
        return zOrder;  // 返回Z序值
    }  // 方法结束

    /**
     * 设置Z序
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置通道的显示层级</li>
     *   <li>值越大，波形显示越靠前（上层）</li>
     * </ul>
     * 
     * @param zOrder Z序值
     */
    public void setzOrder(int zOrder) {  // 设置Z序方法
        this.zOrder = zOrder;  // 保存Z序值
    }  // 方法结束

    /**
     * 设置通道开关状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制通道的开启和关闭</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @param bOpen true表示打开通道，false表示关闭通道
     */
    public void setOpen(boolean bOpen) {  // 设置通道开关状态方法
        synchronized (this) {  // 同步锁，保证线程安全
            this.bOpen = bOpen;  // 保存开关状态
        }  // 同步块结束
    }  // 方法结束

    /**
     * 检查通道是否打开
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>返回通道的开关状态</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @return true表示通道打开，false表示通道关闭
     */
    @Override  // 覆盖IChannel接口方法
    public boolean isOpen() {  // 检查通道是否打开方法
        synchronized (this) {  // 同步锁，保证线程安全
            return bOpen;  // 返回开关状态
        }  // 同步块结束
    }  // 方法结束

    /**
     * 设置垂直位置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>设置通道波形在屏幕上的垂直位置</li>
     *   <li>使用FPGA坐标系统</li>
     *   <li>位置改变时重新计算中心电压值</li>
     * </ul>
     * 
     * <p><b>坐标系统：</b>
     * <pre>
     * FPGA坐标系统，高度固定为1000
     * 0：屏幕中心
     * 正值：向上移动
     * 负值：向下移动
     * </pre>
     * 
     * @param pos 垂直位置（FPGA坐标系统）
     */
    @Override  // 覆盖IChannel接口方法
    public void setPos(double pos) {  // 设置垂直位置方法
        synchronized (this) {  // 同步锁，保证线程安全
            this.pos = pos;  // 保存垂直位置
            calcCenterVal();  // 重新计算中心电压值
        }  // 同步块结束
    }  // 方法结束

//    protected void setPos(double pos) {  // 已注释：受保护的设置位置方法
//        synchronized (this) {  // 同步锁
////            if (pos < 0) {  // 已注释：判断位置是否小于0
////                this.pos = (int) Math.round(pos - 0.5);  // 已注释：向下取整
////            } else {  // 已注释：否则
////                this.pos = (int) Math.round(pos + 0.5);  // 已注释：向上取整
////            }  // 已注释：判断结束
//            this.pos = pos;  // 已注释：保存位置
//        }  // 已注释：同步块结束
//    }  // 已注释：方法结束

    /**
     * 获取UI坐标系统的垂直位置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>将FPGA坐标转换为UI坐标</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * <p><b>转换公式：</b>
     * <pre>
     * UI坐标 = FPGA坐标 × ToUICoff
     * ToUICoff = UI高度 / FPGA高度(1000)
     * </pre>
     * 
     * @return UI坐标系统的垂直位置
     */
    @Override  // 覆盖IChannel接口方法
    public double getPosUI() {  // 获取UI坐标系统垂直位置方法
        synchronized (this) {  // 同步锁，保证线程安全
            return ScopeBase.changeAccuracy(pos * ScopeBase.getToUICoff());  // 转换为UI坐标并处理精度
        }  // 同步块结束
    }  // 方法结束

    /**
     * 获取FPGA坐标系统的垂直位置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>返回通道的垂直位置（FPGA坐标系统）</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @return FPGA坐标系统的垂直位置（高度=1000对应的位置）
     */
    @Override  // 覆盖IChannel接口方法
    public double getPos() {  // 获取FPGA坐标系统垂直位置方法
        synchronized (this) {  // 同步锁，保证线程安全
            return pos;  // 返回垂直位置，注释：height=1000对应的位置
        }  // 同步块结束
    }  // 方法结束

    /**
     * 设置通道标签
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>设置用户自定义的通道标签</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @param label 通道标签字符串
     */
    @Override  // 覆盖IChannel接口方法
    public void setLabel(String label) {  // 设置通道标签方法
        synchronized (this) {  // 同步锁，保证线程安全
            this.label = label;  // 保存通道标签
        }  // 同步块结束
    }  // 方法结束

    /**
     * 获取通道标签
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>返回用户自定义的通道标签</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @return 通道标签字符串，如果未设置则返回null
     */
    @Override  // 覆盖IChannel接口方法
    public String getLabel() {  // 获取通道标签方法
        synchronized (this) {  // 同步锁，保证线程安全
            return this.label;  // 返回通道标签
        }  // 同步块结束
    }  // 方法结束


    /**
     * 获取通道ID（索引）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>返回通道的唯一标识符</li>
     * </ul>
     * 
     * <p><b>返回值说明：</b>
     * <pre>
     * 0-7：物理通道CH1-CH8
     * 8-15：参考通道REF1-REF8
     * 16-23：数学通道MATH1-MATH8
     * </pre>
     * 
     * @return 通道索引
     */
    @Override  // 覆盖IChannel接口方法
    public int getChId() {  // 获取通道ID方法
        return chIdx;  // 返回通道索引
    }  // 方法结束

    /**
     * 设置数据缓冲队列
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>设置通道的数据缓冲队列</li>
     *   <li>用于波形数据的缓冲管理</li>
     * </ul>
     * 
     * @param dataBuffer 数据缓冲队列对象
     */
    @Override  // 覆盖IChannel接口方法
    public void setBuffer(IBufferQueue dataBuffer) {  // 设置数据缓冲队列方法
        this.dataBufferQueue = dataBuffer;  // 保存数据缓冲队列
    }  // 方法结束

    /**
     * 保存波形数据到文件（二进制格式）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>将通道的波形数据保存为二进制文件</li>
     *   <li>保存前检查波形数据是否有效</li>
     * </ul>
     * 
     * <p><b>保存流程：</b>
     * <pre>
     * 1. 从缓冲队列获取数据缓冲区
     * 2. 检查数据缓冲区和波形有效性
     * 3. 调用数据缓冲区的保存方法
     * 4. 归还数据缓冲区
     * </pre>
     * 
     * @param pathName 文件保存路径
     * @return true表示保存成功，false表示保存失败
     */
    @Override  // 覆盖IChannel接口方法
    public boolean save(String pathName) {  // 保存波形数据方法（二进制格式）
        boolean bret = false;  // 返回值，初始为false
        IDataBuffer dataBuffer = obtain();  // 从缓冲队列获取数据缓冲区
        if (dataBuffer != null && isWaveValid()) {  // 判断：数据缓冲区是否有效且波形有效
            bret = dataBuffer.save(pathName);  // 调用数据缓冲区的保存方法

        }  // 判断结束
        if (dataBuffer != null) {  // 判断：数据缓冲区是否不为null
            recycle(dataBuffer);  // 归还数据缓冲区到缓冲队列
        }  // 判断结束
        return bret;  // 返回保存结果
    }  // 方法结束

    /**
     * 保存波形数据到文件（CSV格式）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>将通道的波形数据保存为CSV文件</li>
     *   <li>保存前检查波形数据是否有效</li>
     * </ul>
     * 
     * <p><b>保存流程：</b>
     * <pre>
     * 1. 从缓冲队列获取数据缓冲区
     * 2. 检查数据缓冲区和波形有效性
     * 3. 调用数据缓冲区的CSV保存方法
     * 4. 归还数据缓冲区
     * </pre>
     * 
     * @param pathName 文件保存路径
     * @return true表示保存成功，false表示保存失败
     */
    @Override  // 覆盖IChannel接口方法
    public boolean saveCSV(String pathName) {  // 保存波形数据方法（CSV格式）
        boolean bret = false;  // 返回值，初始为false
        IDataBuffer dataBuffer = obtain();  // 从缓冲队列获取数据缓冲区
        if (dataBuffer != null && isWaveValid()) {  // 判断：数据缓冲区是否有效且波形有效
            bret = dataBuffer.saveCSV(pathName);  // 调用数据缓冲区的CSV保存方法
        }  // 判断结束
        if (dataBuffer != null) {  // 判断：数据缓冲区是否不为null
            recycle(dataBuffer);  // 归还数据缓冲区到缓冲队列
        }  // 判断结束
        return bret;  // 返回保存结果
    }  // 方法结束

    /**
     * 从缓冲池获取空闲数据缓冲区
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>从数据缓冲队列获取一个空闲缓冲区</li>
     *   <li>用于填充新的波形数据</li>
     * </ul>
     * 
     * @return 数据缓冲区对象，如果队列为空则返回null
     */
    @Override  // 覆盖IChannel接口方法
    public IDataBuffer obtain() {  // 获取空闲数据缓冲区方法
        return dataBufferQueue.obtain();  // 从缓冲队列获取空闲缓冲区
    }  // 方法结束

    /**
     * 将数据缓冲区归还到缓冲池
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>将使用完的数据缓冲区归还到缓冲池</li>
     *   <li>使缓冲区可以被重复使用</li>
     * </ul>
     * 
     * @param dataBuffer 要归还的数据缓冲区
     */
    @Override  // 覆盖IChannel接口方法
    public void recycle(IDataBuffer dataBuffer) {  // 归还数据缓冲区方法
        dataBufferQueue.recycle(dataBuffer);  // 将缓冲区归还到缓冲队列
    }  // 方法结束

    /**
     * 从队列取出已填充的数据缓冲区
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>从数据缓冲队列取出一个已填充的缓冲区</li>
     *   <li>用于读取波形数据进行显示或测量</li>
     * </ul>
     * 
     * @return 已填充的数据缓冲区，如果队列为空则返回null
     */
    @Override  // 覆盖IChannel接口方法
    public IDataBuffer dequeue() {  // 取出已填充数据缓冲区方法
        return dataBufferQueue.dequeue();  // 从缓冲队列取出已填充缓冲区
    }  // 方法结束

    /**
     * 将填充好的数据缓冲区放入队列
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>实现IChannel接口方法</li>
     *   <li>将填充好的数据缓冲区放入队列</li>
     *   <li>使数据可以被读取使用</li>
     * </ul>
     * 
     * @param dataBuffer 已填充的数据缓冲区
     */
    @Override  // 覆盖IChannel接口方法
    public void enqueue(IDataBuffer dataBuffer) {  // 放入已填充数据缓冲区方法
        dataBufferQueue.enqueue(dataBuffer);  // 将缓冲区放入缓冲队列
    }  // 方法结束

    /**
     * 检查波形数据是否有效
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回通道是否有有效的波形数据</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @return true表示波形数据有效，false表示无效
     */
    public synchronized boolean isWaveValid() {  // 检查波形数据有效性方法
        return bWaveValid;  // 返回波形有效标志
    }  // 方法结束

    /**
     * 设置波形数据有效标志
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置通道的波形数据有效标志</li>
     *   <li>使用synchronized保证线程安全</li>
     * </ul>
     * 
     * @param bWaveValid true表示波形数据有效，false表示无效
     */
    public synchronized void setWaveValid(boolean bWaveValid) {  // 设置波形数据有效标志方法
        this.bWaveValid = bWaveValid;  // 保存波形有效标志
    }  // 方法结束

    /**
     * 垂直模式常量：零点模式
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>通道位置以通道零点为基准</li>
     *   <li>档位切换时，零点位置不变</li>
     *   <li>适用于正常测量场景</li>
     * </ul>
     */
    public static final int VERTICAL_MODE_CH_ZERO = 1;  // 垂直模式常量：零点模式，值为1

    /**
     * 垂直模式常量：屏幕中心模式
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>通道位置以屏幕中心为基准</li>
     *   <li>档位切换时，波形保持在屏幕中心</li>
     *   <li>适用于观察波形相对位置的场景</li>
     * </ul>
     */
    public static final int VERTICAL_MODE_SCREEN_CENTER = 0;  // 垂直模式常量：屏幕中心模式，值为0
    
    /**
     * 当前垂直模式
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>控制通道的垂直位置参考点</li>
     *   <li>VERTICAL_MODE_CH_ZERO：零点模式</li>
     *   <li>VERTICAL_MODE_SCREEN_CENTER：屏幕中心模式</li>
     * </ul>
     * 
     * <p><b>默认值：</b>
     * <pre>
     * VERTICAL_MODE_CH_ZERO（零点模式）
     * </pre>
     */
    private volatile int verticalMode = VERTICAL_MODE_CH_ZERO;  // 当前垂直模式，初始为零点模式，volatile保证可见性

    /**
     * 设置垂直模式
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置通道的垂直模式</li>
     *   <li>发送垂直模式变更事件通知UI更新</li>
     * </ul>
     * 
     * <p><b>模式说明：</b>
     * <pre>
     * VERTICAL_MODE_CH_ZERO（零点模式）：
     *   - 通道位置以通道零点为基准
     *   - 档位切换时，零点位置不变
     * 
     * VERTICAL_MODE_SCREEN_CENTER（屏幕中心模式）：
     *   - 通道位置以屏幕中心为基准
     *   - 档位切换时，波形保持在屏幕中心
     * </pre>
     * 
     * @param verticalMode 垂直模式（VERTICAL_MODE_CH_ZERO或VERTICAL_MODE_SCREEN_CENTER）
     */
    public void setVerticalMode(int verticalMode) {  // 设置垂直模式方法
        this.verticalMode = verticalMode;  // 保存垂直模式
        EventFactory.sendEvent(EventFactory.EVENT_VERTICAL_MODE);  // 发送垂直模式变更事件
    }  // 方法结束

    /**
     * 获取当前垂直模式
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回通道当前的垂直模式</li>
     * </ul>
     * 
     * @return 垂直模式（VERTICAL_MODE_CH_ZERO或VERTICAL_MODE_SCREEN_CENTER）
     */
    public int getVerticalMode() {  // 获取垂直模式方法
        return verticalMode;  // 返回当前垂直模式
    }  // 方法结束

    /**
     * 获取垂直档位值（抽象方法）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>抽象方法，由子类实现</li>
     *   <li>返回当前档位对应的垂直一格幅值</li>
     * </ul>
     * 
     * <p><b>返回值说明：</b>
     * <pre>
     * 单位：V/div（伏特/格）
     * 例如：1mV/div、10mV/div、100mV/div、1V/div等
     * </pre>
     * 
     * @return 垂直档位值（V/div）
     */
    public abstract double getVScaleIdVal();  // 抽象方法：获取垂直档位值，注释：档位即垂直一格的幅值

    /**
     * 获取通道幅度值（抽象方法）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>抽象方法，由子类实现</li>
     *   <li>返回通道的幅度值</li>
     * </ul>
     * 
     * <p><b>返回值说明：</b>
     * <pre>
     * 单位：V（伏特）
     * 表示通道可测量的最大幅度
     * </pre>
     * 
     * @return 通道幅度值（V）
     */
    public abstract double getVScaleVal();  // 抽象方法：获取通道幅度值

    /**
     * 获取微调比例（抽象方法）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>抽象方法，由子类实现</li>
     *   <li>返回档位的微调比例</li>
     * </ul>
     * 
     * <p><b>返回值说明：</b>
     * <pre>
     * 范围：1.0 ~ 10.0
     * 1.0：无微调
     * >1.0：档位微调比例
     * </pre>
     * 
     * @return 微调比例
     */
    public abstract double getFineScale();  // 抽象方法：获取微调比例

    /**
     * 设置垂直档位ID
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置通道的垂直档位</li>
     *   <li>根据当前垂直模式自动调整位置</li>
     * </ul>
     * 
     * <p><b>处理逻辑：</b>
     * <pre>
     * 调用autoVerticalMode()方法：
     *   - 如果是屏幕中心模式，重新计算位置
     *   - 如果是零点模式，重新计算中心电压值
     * </pre>
     * 
     * @param scaleId 档位ID
     */
    public void setVScaleId(int scaleId) {  // 设置垂直档位ID方法
        autoVerticalMode();  // 根据垂直模式自动调整位置
    }  // 方法结束

    /**
     * 中心电压值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>通道位置对应的电压值</li>
     *   <li>用于屏幕中心模式的位置计算</li>
     *   <li>档位切换时保持此值不变</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * centerVal = pos × VScaleIdVal × FineScale
     * </pre>
     */
    protected volatile double centerVal = 0;  // 中心电压值，初始为0，volatile保证可见性

    /**
     * 计算中心电压值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据当前位置和档位计算中心电压值</li>
     *   <li>用于屏幕中心模式的位置保持</li>
     * </ul>
     * 
     * <p><b>计算公式：</b>
     * <pre>
     * centerVal = pos × VScaleIdVal × FineScale
     * </pre>
     */
    private void calcCenterVal() {  // 计算中心电压值方法
        centerVal = pos * getVScaleIdVal() * getFineScale();  // 计算中心电压值
    }  // 方法结束

    /**
     * 自动调整垂直模式
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据当前垂直模式调整位置或中心电压值</li>
     *   <li>档位切换时调用</li>
     * </ul>
     * 
     * <p><b>处理逻辑：</b>
     * <pre>
     * 如果是屏幕中心模式（VERTICAL_MODE_SCREEN_CENTER）：
     *   - 根据中心电压值重新计算位置
     *   - 保持波形在屏幕中心
     * 
     * 如果是零点模式（VERTICAL_MODE_CH_ZERO）：
     *   - 重新计算中心电压值
     *   - 保持零点位置不变
     * </pre>
     */
    private void autoVerticalMode() {  // 自动调整垂直模式方法
        if (verticalMode == VERTICAL_MODE_SCREEN_CENTER) {  // 判断：是否为屏幕中心模式
            double val = centerVal / (getVScaleIdVal() * getFineScale()) * ScopeBase.getToUICoff();  // 计算新位置（UI坐标）
            setPos(val);  // 设置新位置
        }else{  // 否则：零点模式
            calcCenterVal();  // 重新计算中心电压值
        }  // 判断结束
    }  // 方法结束

    /**
     * 检查档位ID是否有效（抽象方法）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>抽象方法，由子类实现</li>
     *   <li>检查指定的档位ID是否在有效范围内</li>
     * </ul>
     * 
     * @param vScaleId 要检查的档位ID
     * @return true表示有效，false表示无效
     */
    public abstract boolean isVScaleIdValid(int vScaleId);  // 抽象方法：检查档位ID是否有效


    /**
     * 设置测量上限位置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置测量区域的上限位置</li>
     *   <li>用于光标测量和区域测量</li>
     * </ul>
     * 
     * @param upper 上限位置（像素）
     */
    public void setUpper(int upper) {  // 设置测量上限位置方法
        measure.MeasureUpper(upper);  // 设置测量对象的上限位置
    }  // 方法结束

    /**
     * 设置测量中间位置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置测量区域的中间位置</li>
     *   <li>用于光标测量和区域测量</li>
     * </ul>
     * 
     * @param middle 中间位置（像素）
     */
    public void setMiddle(int middle) {  // 设置测量中间位置方法
        measure.MeasureMiddle(middle);  // 设置测量对象的中间位置
    }  // 方法结束

    /**
     * 设置测量下限位置
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置测量区域的下限位置</li>
     *   <li>用于光标测量和区域测量</li>
     * </ul>
     * 
     * @param lower 下限位置（像素）
     */
    public void setLower(int lower) {  // 设置测量下限位置方法
        measure.MeasureLower(lower);  // 设置测量对象的下限位置
    }  // 方法结束

    /**
     * 设置绝对测量上限值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置绝对测量的上限电压值</li>
     *   <li>用于绝对测量模式</li>
     * </ul>
     * 
     * @param val 上限电压值（V）
     */
    public void setAbsUpper(double val){  // 设置绝对测量上限值方法
        measure.setAbsUpper(val);  // 设置测量对象的绝对上限值
    }  // 方法结束

    /**
     * 设置绝对测量中间值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置绝对测量的中间电压值</li>
     *   <li>用于绝对测量模式</li>
     * </ul>
     * 
     * @param val 中间电压值（V）
     */
    public void setAbsMiddle(double val){  // 设置绝对测量中间值方法
        measure.setAbsMiddle(val);  // 设置测量对象的绝对中间值
    }  // 方法结束

    /**
     * 设置绝对测量下限值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置绝对测量的下限电压值</li>
     *   <li>用于绝对测量模式</li>
     * </ul>
     * 
     * @param val 下限电压值（V）
     */
    public void setAbsLower(double val){  // 设置绝对测量下限值方法
        measure.setAbsLow(val);  // 设置测量对象的绝对下限值
    }  // 方法结束

    /**
     * 设置绝对测量使能
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>启用或禁用绝对测量模式</li>
     *   <li>绝对测量使用电压值而非像素位置</li>
     * </ul>
     * 
     * @param bAbsEnable true表示启用绝对测量，false表示禁用
     */
    public void setAbsEnable(boolean bAbsEnable){  // 设置绝对测量使能方法
        measure.setMeasureAbs(bAbsEnable);  // 设置测量对象的绝对测量使能
    }  // 方法结束

    /**
     * 检查绝对测量是否启用
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回当前是否启用了绝对测量模式</li>
     * </ul>
     * 
     * @return true表示已启用绝对测量，false表示未启用
     */
    public boolean isAbsEnable(){  // 检查绝对测量是否启用方法
        return measure.isAbsMeasure();  // 返回测量对象的绝对测量状态
    }  // 方法结束

}  // 类结束
