package com.micsig.tbook.scope.Sample;

import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Scope;

/**
 * 段采样管理类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Sample（示波器采样管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 状态管理</li>
 *   <li>设计模式：单例模式</li>
 *   <li>职责类型：段采样状态与配置管理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>管理段采样模式的启用/禁用</li>
 *   <li>管理段数量配置</li>
 *   <li>计算段采样相关参数</li>
 *   <li>管理段显示模式（单帧/拟合）</li>
 *   <li>管理帧号和帧范围</li>
 *   <li>协调段采样参数变化事件</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>集中管理示波器段采样相关状态</li>
 *   <li>提供段采样参数计算能力</li>
 *   <li>支持多段波形的捕获和显示</li>
 *   <li>优化存储深度与段数的平衡</li>
 * </ul>
 * 
 * <p><b>段采样概念说明：</b>
 * <pre>
 * 段采样是一种特殊的采样模式，用于捕获多个波形段：
 * 
 * ┌────────────────────────────────────────────────────────────────┐
 * │ 传统采样：                                                     │
 * │   [==========一段波形==========]                               │
 * │                                                                │
 * │ 段采样：                                                       │
 * │   [段1][段2][段3][段4]...[段N]                                 │
 * │   每段独立存储，适合捕获快速瞬态信号                            │
 * └────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>段显示模式说明：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────┐
 * │ 显示模式              │ 值  │ 说明                              │
 * ├───────────────────────┼─────┼──────────────────────────────────┤
 * │ SEGMENT_DISPLAY_SFRAME│  0  │ 单帧显示：显示指定帧号的波形      │
 * │ SEGMENT_DISPLAY_FITTING│  1 │ 拟合显示：显示帧范围的拟合波形    │
 * └───────────────────────┴─────┴──────────────────────────────────┘
 * </pre>
 * 
 * <p><b>存储空间分配：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────┐
 * │ 产品型号      │ 总存储空间     │ 说明                          │
 * ├───────────────┼────────────────┼────────────────────────────────┤
 * │ MHO68 V1/V2   │ 4GB (4000MB)   │ 高端示波器                    │
 * │ 其他          │ 1.92GB (1920MB)│ 中端示波器                    │
 * └───────────────┴────────────────┴────────────────────────────────┘
 * </pre>
 * 
 * <p><b>类结构图：</b>
 * <pre>
 * SegmentSample (单例类)
 *   │
 *   ├── 持有 ──→ SegmentSampleAction (动作处理器)
 *   │
 *   ├── 依赖 ──→ MemDepthFactory (存储深度工厂)
 *   │
 *   ├── 依赖 ──→ Scope (示波器状态)
 *   │
 *   ├── 依赖 ──→ Sample (采样管理)
 *   │
 *   └── 依赖 ──→ HwConfig (硬件配置)
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>持有：SegmentSampleAction（段采样动作处理器）</li>
 *   <li>依赖：MemDepthFactory（获取存储深度）</li>
 *   <li>依赖：Scope（获取通道数）</li>
 *   <li>依赖：Sample（获取采样参数）</li>
 *   <li>依赖：HwConfig（获取FPGA数量）</li>
 *   <li>依赖：HardwareProduct（判断产品型号）</li>
 * </ul>
 * 
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>单例创建使用双重检查锁定</li>
 *   <li>关键状态变量使用synchronized保护</li>
 *   <li>使用volatile保证可见性</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>
 * // 获取段采样管理实例
 * SegmentSample segment = SegmentSample.getInstance();
 * 
 * // 启用段采样模式
 * segment.setSegmentEnable(true);
 * 
 * // 设置段数量
 * segment.setSegmentNums(100);
 * 
 * // 切换到单帧显示模式
 * segment.setSegmentDisplayType(SegmentSample.SEGMENT_DISPLAY_SFRAME);
 * 
 * // 设置当前显示的帧号
 * segment.setFrameNo(50);
 * </pre>
 * 
 * @author zhuzh
 * @version 1.0
 * @see SegmentSampleAction 段采样动作处理器
 * @see MemDepthFactory 存储深度工厂
 * @see Sample 采样管理
 */
public class SegmentSample {
    
    /**
     * 日志标签
     */
    private final static String TAG = "SegmentSample";
    
    /**
     * 单例实例
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>全局唯一的段采样管理实例</li>
     *   <li>使用volatile保证多线程可见性</li>
     * </ul>
     */
    private static volatile SegmentSample instance = null;

    /**
     * 获取单例实例
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>使用双重检查锁定保证线程安全</li>
     *   <li>延迟初始化，首次调用时创建实例</li>
     * </ul>
     * 
     * @return SegmentSample单例实例
     */
    public static SegmentSample getInstance() {
        if (instance == null) {
            synchronized (SegmentSample.class) {
                if (instance == null ) {
                    instance = new SegmentSample();
                }
            }
        }
        return instance;
    }
    
    /**
     * 段采样启用标志
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>true：段采样模式已启用</li>
     *   <li>false：普通采样模式</li>
     * </ul>
     */
    volatile boolean bSegmentEnable = false;
    
    /**
     * 段采样动作处理器
     */
    SegmentSampleAction segmentSampleAction;
    
    /**
     * 私有构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>创建段采样动作处理器</li>
     * </ul>
     */
    private SegmentSample(){
        segmentSampleAction = new SegmentSampleAction(this);
    }

    /**
     * 判断段采样是否启用
     * 
     * @return true表示段采样模式已启用
     */
    public boolean isSegmentEnable() {
        return bSegmentEnable;
    }

    /**
     * 设置段采样启用状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>启用或禁用段采样模式</li>
     *   <li>触发段采样启用事件</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>用户切换段采样模式时调用</li>
     * </ul>
     * 
     * @param bSegmentEnable true启用，false禁用
     */
    public void setSegmentEnable(boolean bSegmentEnable) {
        Log.d(TAG, "setSegmentEnable() called with: bSegmentEnable = [" + bSegmentEnable + "]");
        this.bSegmentEnable = bSegmentEnable;
        segmentSampleAction.SegmentEnable();
    }
    
    /**
     * 最大段数量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>段采样的最大段数限制</li>
     *   <li>值为10001（10K+1）</li>
     * </ul>
     */
    public static final int SEGMENT_MAX = 10 * 1000 + 1;
    
    /**
     * 最小段数量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>段采样的最小段数限制</li>
     *   <li>值为2</li>
     * </ul>
     */
    public static final int SEGMENT_MIN = 2;
    
    /**
     * 当前段数量
     */
    private int segmentNums = SEGMENT_MIN;
    
    /**
     * 设置段数量
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>配置段采样的段数量</li>
     *   <li>触发段采样启用事件</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>用户调整段数量时调用</li>
     * </ul>
     * 
     * @param segmentNums 段数量
     */
    public void setSegmentNums(int segmentNums){

        Logger.d(TAG,"segmentNums:" + segmentNums);
        this.segmentNums = segmentNums;
        segmentSampleAction.SegmentEnable();
    }
    
    /**
     * 获取段数量
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>如果启用了最大段模式，返回最大可用段数</li>
     *   <li>否则返回当前配置的段数量</li>
     * </ul>
     * 
     * @return 段数量
     */
    public int getSegmentNums(){
        if(bEnableMaxSegment){
            this.segmentNums = getMaxSegmentNums();
        }
        return this.segmentNums;
    }
    
    /**
     * 最大段模式启用标志
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>true：自动使用最大可用段数</li>
     *   <li>false：使用用户配置的段数</li>
     * </ul>
     */
    private boolean bEnableMaxSegment = false;
    
    /**
     * 设置最大段模式启用状态
     * 
     * @param bEnableMaxSegment true启用，false禁用
     */
    public void setEnableMaxSegment(boolean bEnableMaxSegment){
        this.bEnableMaxSegment = bEnableMaxSegment;
    }
    
    /**
     * 判断是否启用了最大段模式
     * 
     * @return true表示已启用最大段模式
     */
    public boolean isEnableMaxSegment(){
        return bEnableMaxSegment;
    }
    
    /**
     * 16M存储深度阈值常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>用于计算段存储空间的分界值</li>
     *   <li>小于等于16M时按2的幂次对齐</li>
     *   <li>大于16M时按16M的倍数对齐</li>
     * </ul>
     */
    public static final int SEGMENT_16M = 16 * 1024 * 1024;
    
    /**
     * 段采样总存储空间
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>MHO68 V1/V2：4GB (4000MB)</li>
     *   <li>其他产品：1.92GB (1920MB)</li>
     * </ul>
     */
    public static final long SEGMENT_LENGTH = (
            HardwareProduct.isMHO68V1()
            || HardwareProduct.isMHO68V2() )
            ? 4000L * 1024 * 1024
            : 1920L * 1024 * 1024;
    
    /**
     * 计算指定存储深度下可用的段数量
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据存储深度计算最大可分段数</li>
     *   <li>公式：总存储空间 / 段存储空间</li>
     * </ul>
     * 
     * <p><b>计算逻辑：</b>
     * <pre>
     * 可用段数 = SEGMENT_LENGTH / getSegmentSpace(zun)
     * </pre>
     * 
     * @param zun 存储深度值
     * @return 可用的段数量
     */
    public int getSegmentSample(long zun){

        return (int)(SegmentSample.SEGMENT_LENGTH / getSegmentSpace(zun));
    }

    /**
     * 计算段存储空间
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据存储深度计算每段实际占用的存储空间</li>
     *   <li>存储空间按特定规则对齐</li>
     * </ul>
     * 
     * <p><b>计算规则：</b>
     * <pre>
     * ┌────────────────────────────────────────────────────────────────┐
     * │ 存储深度 ≤ 16M：                                               │
     * │   按2的幂次向上对齐到最近的值                                   │
     * │   例：5M → 8M, 10M → 16M                                      │
     * ├────────────────────────────────────────────────────────────────┤
     * │ 存储深度 > 16M：                                               │
     * │   按16M的倍数向上对齐                                          │
     * │   例：20M → 32M (2×16M), 40M → 48M (3×16M)                    │
     * └────────────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * <p><b>设计目的：</b>
     * <ul>
     *   <li>优化FPGA存储地址对齐</li>
     *   <li>提高存储访问效率</li>
     * </ul>
     * 
     * @param zun 存储深度值
     * @return 段存储空间（对齐后的值）
     */
    public static long getSegmentSpace(long zun){
        if (zun <= SegmentSample.SEGMENT_16M) {
            int k = 1024;
            while (k < zun) {
                k *= 2;
            }
            return  k;
        } else {
            long k = 1;
            while ((k * SegmentSample.SEGMENT_16M) < zun) {
                k++;
            }
            return k * SegmentSample.SEGMENT_16M;
        }
    }

    /**
     * 获取最大可用段数量
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据当前存储深度和通道数计算最大可用段数</li>
     *   <li>考虑存储深度档位的影响</li>
     * </ul>
     * 
     * <p><b>计算逻辑：</b>
     * <pre>
     * ┌────────────────────────────────────────────────────────────────┐
     * │ Auto模式（档位0）：                                            │
     * │   返回 SEGMENT_MAX - 1 (10000)                                 │
     * ├────────────────────────────────────────────────────────────────┤
     * │ 固定档位模式：                                                 │
     * │   1. 获取当前存储深度                                          │
     * │   2. 计算可用段数 = getSegmentSample(zun) / (通道数/FPGA数)   │
     * │   3. 限制在[SEGMENT_MIN, SEGMENT_MAX]范围内                   │
     * │   4. 返回段数 - 1（预留一个段）                                │
     * └────────────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * @return 最大可用段数量
     */
    public int getMaxSegmentNums(){
        IMemDepth memDepth = MemDepthFactory.getMemDepth();
        if(memDepth.getMemDepthItem() == 0){
            return SEGMENT_MAX-1;
        }else {
            int nums = SEGMENT_MIN;
            Scope scope = Scope.getInstance();
            Sample sample = Sample.getInstance();
            long zun = sample.getSampleMemDepth();
            nums = getSegmentSample(zun) / (scope.getChannelSampOnCnt(true) / HwConfig.getInstance().getFpgaNums());
            if (nums > SEGMENT_MAX) {
                nums = SEGMENT_MAX;
            }
            if (nums < SEGMENT_MIN) {
                nums = SEGMENT_MIN;
            }
            return nums - 1;
        }
    }
    
    /**
     * 单帧显示模式
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>显示指定帧号的波形</li>
     *   <li>用户可以通过帧号浏览每一段波形</li>
     * </ul>
     */
    public static final int SEGMENT_DISPLAY_SFRAME = 0;
    
    /**
     * 拟合显示模式
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>显示指定帧范围内所有波形的拟合结果</li>
     *   <li>适合观察信号的变化趋势</li>
     * </ul>
     */
    public static final int SEGMENT_DISPLAY_FITTING = 1;
    
    /**
     * 当前段显示类型
     */
    private int segmentDisplayType =SEGMENT_DISPLAY_SFRAME;
    
    /**
     * 设置段显示类型
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>切换段显示模式（单帧/拟合）</li>
     *   <li>触发段变化事件</li>
     * </ul>
     * 
     * @param segmentDisplayType 显示类型
     */
    public void setSegmentDisplayType(int segmentDisplayType){
        this.segmentDisplayType = segmentDisplayType;
        segmentSampleAction.segmentChange();
    }
    
    /**
     * 获取段显示类型
     * 
     * @return 当前显示类型
     */
    public synchronized int getSegmentDisplayType(){
        return segmentDisplayType;
    }
    
    /**
     * 拟合起始帧号
     */
    private int fittingBegingFrame = 0;
    
    /**
     * 拟合结束帧号
     */
    private int fittingEndFrame = 1;

    /**
     * 获取拟合起始帧号
     * 
     * @return 拟合起始帧号
     */
    public int getFittingBegingFrame() {
        return fittingBegingFrame;
    }

    /**
     * 设置拟合起始帧号
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置拟合显示的起始帧</li>
     *   <li>仅在拟合模式下触发段变化事件</li>
     * </ul>
     * 
     * @param fittingBegingFrame 起始帧号
     */
    public void setFittingBegingFrame(int fittingBegingFrame) {
        this.fittingBegingFrame = fittingBegingFrame;
        if(getSegmentDisplayType() == SEGMENT_DISPLAY_FITTING) {
            segmentSampleAction.segmentChange();
        }
    }

    /**
     * 获取拟合结束帧号
     * 
     * @return 拟合结束帧号
     */
    public int getFittingEndFrame() {
        return fittingEndFrame;
    }

    /**
     * 设置拟合结束帧号
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置拟合显示的结束帧</li>
     *   <li>仅在拟合模式下触发段变化事件</li>
     * </ul>
     * 
     * @param fittingEndFrame 结束帧号
     */
    public void setFittingEndFrame(int fittingEndFrame) {
        this.fittingEndFrame = fittingEndFrame;
        if(getSegmentDisplayType() == SEGMENT_DISPLAY_FITTING) {
            segmentSampleAction.segmentChange();
        }
    }
    
    /**
     * 当前帧号
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>单帧模式下显示的帧号</li>
     *   <li>用于浏览段采样捕获的各段波形</li>
     * </ul>
     */
    private long frameNo = 0;

    /**
     * 获取当前帧号
     * 
     * @return 当前帧号
     */
    public synchronized long getFrameNo() {
        return frameNo;

    }
    
    /**
     * 设置帧号
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>更新当前帧号</li>
     *   <li>根据参数决定触发哪种事件</li>
     * </ul>
     * 
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>bChange=true：单帧模式下触发段变化事件</li>
     *   <li>bChange=false：仅发送段命令</li>
     * </ul>
     * 
     * @param frameNo 帧号
     * @param bChange 是否触发段变化事件
     */
    public void setFrameNo(long frameNo,boolean bChange){
        this.frameNo = frameNo;
        if(bChange) {
            if(getSegmentDisplayType() == SEGMENT_DISPLAY_SFRAME) {
                segmentSampleAction.segmentChange();
            }
        }else{
            segmentSampleAction.cmdSegment();
        }
    }
    
    /**
     * 设置帧号和显示类型
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>同时更新帧号和显示类型</li>
     *   <li>触发段变化事件</li>
     * </ul>
     * 
     * @param frameNo 帧号
     * @param segmentDisplayType 显示类型
     */
    public synchronized void setFrameNo(long frameNo,int segmentDisplayType){
        this.frameNo = frameNo;
        this.segmentDisplayType = segmentDisplayType;
        segmentSampleAction.segmentChange();
    }

    /**
     * 设置帧号（默认触发变化事件）
     * 
     * @param frameNo 帧号
     */
    public void setFrameNo(long frameNo) {
        setFrameNo(frameNo,true);
    }
    
    /**
     * 采样开始时间
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录段采样开始的时间戳</li>
     *   <li>用于计算采样持续时间</li>
     * </ul>
     */
    private long beginSampleTime = System.currentTimeMillis();
    
    /**
     * 获取采样开始时间
     * 
     * @return 采样开始时间戳（毫秒）
     */
    public long getBeginSampleTime(){
        return beginSampleTime;
    }
    
    /**
     * 更新采样开始时间
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将采样开始时间更新为当前时间</li>
     * </ul>
     */
    public void updateBeginSampleTime(){
        this.beginSampleTime = System.currentTimeMillis();
    }

    /**
     * 已捕获的段帧数
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>记录当前已捕获的段数量</li>
     *   <li>用于显示采样进度</li>
     * </ul>
     */
    private int segmentFrames = 0;
    
    /**
     * 设置已捕获的段帧数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>更新已捕获的段数量</li>
     *   <li>触发段帧数事件</li>
     * </ul>
     * 
     * @param nums 已捕获的段数量
     */
    public void setSegmentFrames(int nums){
        segmentFrames = nums;
        segmentSampleAction.segmentFrames();
    }
    
    /**
     * 获取已捕获的段帧数
     * 
     * @return 已捕获的段数量
     */
    public int getSegmentFrames(){

        return segmentFrames;
    }
}
