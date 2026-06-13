package com.micsig.tbook.tbookscope.wavezone.wave;   // 示波器波形显示区域-波形子模块包，包含垂直通道类

import android.graphics.Bitmap;   // 导入Android Bitmap类，用于位图资源管理

import com.micsig.tbook.tbookscope.wavezone.IWorkMode;   // 导入工作模式接口，用于工作模式切换
import com.micsig.tbook.tbookscope.wavezone.trigger.VerticalChannelBase;   // 导入垂直通道基类，提供垂直通道的基本功能

/**
 * 垂直通道波形类 - XY模式下显示的垂直通道波形（用于控制X轴位置）
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    垂直通道波形类架构                            │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │  ┌─────────────────────────────────────────────┐               │
 * │  │         ChannelWaveManage_XY                 │               │
 * │  │       (XY模式通道波形管理类)                  │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  channelWaveVertical (Ch1)          │   │               │
 * │  │  │  (垂直通道 - 控制X轴位置)            │   │               │
 * │  │  │                                     │   │               │
 * │  │  │  ┌─────────────────────────────┐   │   │               │
 * │  │  │  │  ChannelWaveVertical        │   │   │               │
 * │  │  │  │  (本类)                     │   │   │               │
 * │  │  │  │                             │   │   │               │
 * │  │  │  │  继承：VerticalChannelBase  │   │   │               │
 * │  │  │  │  实现：IWorkMode            │   │   │               │
 * │  │  │  │                             │   │   │               │
 * │  │  │  │  功能：                     │   │   │               │
 * │  │  │  │    - 显示垂直通道波形       │   │   │               │
 * │  │  │  │    - 控制X轴位置            │   │   │               │
 * │  │  │  │    - 工作模式切换           │   │   │               │
 * │  │  │  │                             │   │   │               │
 * │  │  │  │  XY模式特殊说明：           │   │   │               │
 * │  │  │  │    - Ch1控制X轴位置         │   │   │               │
 * │  │  │  │    - 水平移动控制X轴        │   │   │               │
 * │  │  │  │    - 与ChannelWave配合      │   │   │               │
 * │  │  │  │      (Ch2控制Y轴位置)       │   │   │               │
 * │  │  │  └─────────────────────────────┘   │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  channelWave (Ch2)                  │   │               │
 * │  │  │  (水平通道 - 控制Y轴位置)            │   │               │
 * │  │  │                                     │   │               │
 * │  │  │  ┌─────────────────────────────┐   │   │               │
 * │  │  │  │  ChannelWave                │   │   │               │
 * │  │  │  │  (水平通道类)               │   │   │               │
 * │  │  │  │                             │   │   │               │
 * │  │  │  │  功能：                     │   │   │               │
 * │  │  │  │    - 显示水平通道波形       │   │   │               │
 * │  │  │  │    - 控制Y轴位置            │   │   │               │
 * │  │  │  └─────────────────────────────┘   │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  └─────────────────────────────────────────────┘               │
 * │                       │                                       │
 * │                       │ 继承关系                               │
 * │                       ▼                                       │
 * │           ┌─────────────────────┐                             │
 * │           │  VerticalChannelBase │                             │
 * │           │  (垂直通道基类)       │                             │
 * │           │                     │                             │
 * │           │  实现接口：          │                             │
 * │           │    - IWave          │                             │
 * │           │                     │                             │
 * │           │  提供功能：          │                             │
 * │           │    - 位图管理        │                             │
 * │           │    - 位置管理        │                             │
 * │           │    - 绘制功能        │                             │
 * │           │    - 选中管理        │                             │
 * │           │    - 可见性管理      │                             │
 * │           │                     │                             │
 * │           │  核心方法：          │                             │
 * │           │    - init()         │                             │
 * │           │    - draw()         │                             │
 * │           │    - setX()         │                             │
 * │           │    - getX()         │                             │
 * │           │    - movePix()      │                             │
 * │           │    - selectCursor() │                             │
 * │           │    - onRefresh()    │                             │
 * │           └─────────────────────┘                             │
 * │                                                                 │
 * │  ┌─────────────────────────────────────────────┐               │
 * │  │           XY模式显示原理                      │               │
 * │  ├─────────────────────────────────────────────┤               │
 * │  │                                             │               │
 * │  │  X-Y图形模式（李萨如图形）                   │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  显示区域                            │   │               │
 * │  │  │                                     │   │               │
 * │  │  │         ↑ Ch2 (Y轴)                 │   │               │
 * │  │  │         │                           │   │               │
 * │  │  │         │                           │   │               │
 * │  │  │         │                           │   │               │
 * │  │  │  ───────┼──────────→ Ch1 (X轴)      │   │               │
 * │  │  │         │                           │   │               │
 * │  │  │         │                           │   │               │
 * │  │  │         │                           │   │               │
 * │  │  │         ↓                           │   │               │
 * │  │  │                                     │   │               │
 * │  │  │  Ch1: 垂直通道（本类）              │   │               │
 * │  │  │    - 控制X轴位置                    │   │               │
 * │  │  │    - 水平移动                       │   │               │
 * │  │  │                                     │   │               │
 * │  │  │  Ch2: 水平通道（ChannelWave）       │   │               │
 * │  │  │    - 控制Y轴位置                    │   │               │
 * │  │  │    - 垂直移动                       │   │               │
 * │  │  │                                     │   │               │
 * │  │  │  李萨如图形：                       │   │               │
 * │  │  │    - Ch1信号输入X轴                 │   │               │
 * │  │  │    - Ch2信号输入Y轴                 │   │               │
 * │  │  │    - 显示两信号的相位关系           │   │               │
 * │  │  │                                     │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  │                                             │               │
 * │  └─────────────────────────────────────────────┘               │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * <h3>模块定位</h3>
 * <p>本类是示波器波形显示区域的垂直通道波形类，属于wavezone.wave子模块。
 * 专门用于XY模式下的垂直通道显示，控制X轴位置，与ChannelWave（水平通道，控制Y轴）配合使用，
 * 实现X-Y图形模式（李萨如图形）的显示。</p>
 * 
 * <h3>核心职责</h3>
 * <ul>
 *   <li><b>垂直通道显示</b>：在XY模式下显示垂直通道波形，控制X轴位置</li>
 *   <li><b>X轴位置控制</b>：通过水平移动控制X轴位置，影响李萨如图形的显示</li>
 *   <li><b>工作模式切换</b>：实现IWorkMode接口，支持工作模式切换</li>
 *   <li><b>继承基类功能</b>：继承VerticalChannelBase，复用垂直通道的基本功能</li>
 *   <li><b>位图刷新</b>：在工作模式切换时刷新位图，更新显示</li>
 * </ul>
 * 
 * <h3>XY模式通道波形管理流程说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              XY模式垂直通道波形管理流程                        │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  Step 1: ChannelWaveManage_XY创建垂直通道                     │
 * │    └─ new ChannelWaveVertical(resBmp[TChan.Ch1])              │
 * │    └─ 调用super()构造函数                                      │
 * │    └─ 调用init(resBmp)初始化                                   │
 * │    └─ 设置位图资源                                             │
 * │    └─ 创建绘制画布                                             │
 * │                                                               │
 * │  Step 2: 设置通道ID和位置                                      │
 * │    └─ channelWaveVertical.setLineNameID(TChan.Ch1)            │
 * │    └─ 设置通道ID为Ch1                                          │
 * │    └─ channelWaveVertical.setX(缓存位置)                       │
 * │    └─ 设置X轴位置                                              │
 * │    └─ DisplayXYService.getInstance().setX(计算位置)           │
 * │    └─ 更新XY服务X轴位置                                        │
 * │                                                               │
 * │  Step 3: 设置选中状态和监听器                                  │
 * │    └─ channelWaveVertical.setSelected(false)                  │
 * │    └─ 设置选中状态（初始未选中）                                │
 * │    └─ channelWaveVertical.setOnSelectChangeEvent(...)         │
 * │    └─ 设置选中状态变化监听器                                   │
 * │    └─ channelWaveVertical.setOnMovingWaveEvent(...)           │
 * │    └─ 设置波形移动监听器                                       │
 * │                                                               │
 * │  Step 4: 用户操作（点击选择）                                  │
 * │    └─ 用户点击垂直通道区域                                     │
 * │    └─ ChannelWaveManage_XY.selectCursor(x, y)                 │
 * │    └─ channelWaveVertical.selectCursor(x, y)                  │
 * │    └─ 判断点击区域                                             │
 * │    └─ 返回选中通道ID                                           │
 * │    └─ ChannelWaveManage_XY.setSelectCursor(chNo)              │
 * │    └─ channelWaveVertical.setSelected(true)                   │
 * │    └─ 设置选中状态                                             │
 * │                                                               │
 * │  Step 5: 用户操作（拖动移动）                                  │
 * │    └─ 用户拖动垂直通道                                         │
 * │    └─ ChannelWaveManage_XY.setOffsetY(offsetY)                │
 * │    └─ 判断选中通道                                             │
 * │    └─ channelWaveVertical.setX(新位置)                         │
 * │    └─ 设置X轴位置                                              │
 * │    └─ DisplayXYService.getInstance().setX(计算位置)           │
 * │    └─ 更新XY服务X轴位置                                        │
 * │    └─ 触发OnMovingWaveEvent回调                                │
 * │    └─ 通知波形移动                                             │
 * │                                                               │
 * │  Step 6: 工作模式切换                                          │
 * │    └─ ChannelWaveManage_XY.switchWorkMode(workMode)           │
 * │    └─ channelWaveVertical.switchWorkMode(workMode)            │
 * │    └─ 设置isChanageBitmap = true                              │
 * │    └─ 标记位图需要更新                                         │
 * │    └─ 调用onRefresh()                                          │
 * │    └─ 刷新位图纹理                                             │
 * │                                                               │
 * │  Step 7: 波形绘制                                              │
 * │    └─ WaveZoneDisplay_XY.onGLDraw(canvas)                     │
 * │    └─ ChannelWaveManage_XY.draw(canvas)                       │
 * │    └─ channelWaveVertical.draw(canvas)                        │
 * │    └─ 绘制垂直通道波形                                         │
 * │    └─ 显示X轴位置指示器                                        │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>XY模式特殊说明</h3>
 * <table border="1">
 *   <tr><th>特性</th><th>说明</th></tr>
 *   <tr><td>通道类型</td><td>垂直通道（控制X轴位置）</td></tr>
 *   <tr><td>通道编号</td><td>Ch1（TChan.Ch1）</td></tr>
 *   <tr><td>移动方向</td><td>水平移动（控制X轴位置）</td></tr>
 *   <tr><td>位置属性</td><td>X轴位置（通过setX/getX管理）</td></tr>
 *   <tr><td>配合通道</td><td>ChannelWave（Ch2，控制Y轴位置）</td></tr>
 *   <tr><td>显示模式</td><td>X-Y图形模式（李萨如图形）</td></tr>
 *   <tr><td>功能用途</td><td>显示两信号的相位关系</td></tr>
 * </table>
 * 
 * <h3>与ChannelWave的对比</h3>
 * <table border="1">
 *   <tr><th>对比项</th><th>ChannelWaveVertical</th><th>ChannelWave</th></tr>
 *   <tr><td>通道编号</td><td>Ch1</td><td>Ch2</td></tr>
 *   <tr><td>控制轴</td><td>X轴</td><td>Y轴</td></tr>
 *   <tr><td>移动方向</td><td>水平移动</td><td>垂直移动</td></tr>
 *   <tr><td>位置属性</td><td>X位置</td><td>Y位置</td></tr>
 *   <tr><td>基类</td><td>VerticalChannelBase</td><td>无基类（直接实现IWave）</td></tr>
 *   <tr><td>使用模式</td><td>XY模式</td><td>YT模式、XY模式、YTZOOM模式</td></tr>
 * </table>
 * 
 * <h3>使用场景</h3>
 * <ul>
 *   <li><b>XY模式显示</b>：在XY模式下显示垂直通道波形，控制X轴位置</li>
 *   <li><b>李萨如图形</b>：与ChannelWave配合，显示两信号的相位关系</li>
 *   <li><b>通道选择</b>：用户点击选择垂直通道，进行位置调整</li>
 *   <li><b>位置移动</b>：用户拖动垂直通道，调整X轴位置</li>
 *   <li><b>工作模式切换</b>：切换到XY模式时，刷新位图显示</li>
 * </ul>
 * 
 * <h3>依赖关系</h3>
 * <ul>
 *   <li>{@link VerticalChannelBase} - 垂直通道基类，提供垂直通道的基本功能</li>
 *   <li>{@link IWorkMode} - 工作模式接口，支持工作模式切换</li>
 *   <li>{@link com.micsig.tbook.tbookscope.wavezone.wave.ChannelWaveManage_XY} - XY模式通道波形管理类，管理本类实例</li>
 *   <li>{@link com.micsig.tbook.tbookscope.wavezone.wave.ChannelWave} - 水平通道类，控制Y轴位置</li>
 *   <li>{@link com.micsig.tbook.scope.Display.DisplayXYService} - XY显示服务，接收X轴位置更新</li>
 *   <li>{@link android.graphics.Bitmap} - 位图资源，用于显示通道波形</li>
 * </ul>
 * 
 * <h3>设计模式</h3>
 * <p>本类采用继承模式（Inheritance Pattern），继承VerticalChannelBase基类，
 * 复用垂直通道的基本功能，并扩展工作模式切换功能。</p>
 * 
 * <h3>继承关系说明</h3>
 * <pre>
 * ┌─────────────────────────────────────────┐
 * │  继承关系                                │
 * ├─────────────────────────────────────────┤
 * │                                         │
 * │  IWave (接口)                           │
 * │    - 定义波形基本操作                    │
 * │                                         │
 * │  VerticalChannelBase (基类)             │
 * │    - 实现IWave接口                       │
 * │    - 提供垂直通道基本功能                │
 * │      - 位图管理                         │
 * │      - 位置管理                         │
 * │      - 绘制功能                         │
 * │      - 选中管理                         │
 * │      - 可见性管理                       │
 * │                                         │
 * │  ChannelWaveVertical (本类)             │
 * │    - 继承VerticalChannelBase            │
 * │    - 实现IWorkMode接口                   │
 * │    - 扩展工作模式切换功能                │
 * │      - switchWorkMode()                 │
 * │                                         │
 * │  继承优势：                              │
 * │    - 复用基类功能                        │
 * │    - 减少代码重复                        │
 * │    - 统一垂直通道行为                    │
 * │    - 易于维护和扩展                      │
 * │                                         │
 * └─────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>注意事项</h3>
 * <ul>
 *   <li><b>XY模式专用</b>：本类仅在XY模式下使用，不用于YT模式</li>
 *   <li><b>Ch1通道</b>：本类固定用于Ch1通道，控制X轴位置</li>
 *   <li><b>水平移动</b>：本类的移动是水平移动，控制X轴位置，与ChannelWave的垂直移动相反</li>
 *   <li><b>位图刷新</b>：工作模式切换时需要刷新位图，调用onRefresh()方法</li>
 * </ul>
 * 
 * @see VerticalChannelBase
 * @see IWorkMode
 * @see com.micsig.tbook.tbookscope.wavezone.wave.ChannelWaveManage_XY
 * @see com.micsig.tbook.tbookscope.wavezone.wave.ChannelWave
 * @see com.micsig.tbook.scope.Display.DisplayXYService
 * @author Micsig智能示波器团队
 * @version 2.0
 * @since 1.0
 */
public class ChannelWaveVertical extends VerticalChannelBase implements IWorkMode {   // 垂直通道波形类：继承VerticalChannelBase，实现IWorkMode接口，用于XY模式下显示垂直通道波形

    /**
     * 构造函数 - 创建垂直通道波形实例
     * 
     * <p>构造函数，创建垂直通道波形实例。调用父类构造函数，
     * 然后调用init()方法初始化位图资源和绘制画布。</p>
     * 
     * <h4>构造流程</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  构造流程                                │
     * ├─────────────────────────────────────────┤
     * │                                         │
     * │  Step 1: 调用父类构造函数                │
     * │    └─ super()                           │
     * │    └─ 初始化VerticalChannelBase基类     │
     * │                                         │
     * │  Step 2: 初始化位图资源                  │
     * │    └─ init(resBmp)                      │
     * │    └─ 设置位图资源数组                  │
     * │    └─ 创建绘制位图                      │
     * │    └─ 创建绘制画布                      │
     * │    └─ 初始化绘制工具                    │
     * │                                         │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>resBmp</td><td>Bitmap[]</td><td>位图资源数组，包含垂直通道的各种状态图标（左侧、中心、右侧等）</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>ChannelWaveManage_XY构造函数中调用，创建垂直通道实例</li>
     *   <li>传入resBmp[TChan.Ch1]，即Ch1通道的位图资源</li>
     * </ul>
     * 
     * <h4>使用示例</h4>
     * <pre>
     * Bitmap[][] resBmp = new Bitmap[channelCount][];
     * ChannelWaveVertical channelWaveVertical = new ChannelWaveVertical(resBmp[TChan.Ch1]);
     * </pre>
     * 
     * <h4>注意事项</h4>
     * <ul>
     *   <li><b>调用顺序</b>：先调用super()，再调用init()，确保基类正确初始化</li>
     *   <li><b>位图资源</b>：resBmp数组必须包含完整的位图资源，否则init()会失败</li>
     * </ul>
     * 
     * @param resBmp 位图资源数组，包含垂直通道的各种状态图标
     * @see VerticalChannelBase#init(Bitmap[])
     */
    public ChannelWaveVertical(Bitmap[] resBmp) {   // 构造函数：创建垂直通道波形实例，接收位图资源数组参数
        super();   // 调用父类构造函数：初始化VerticalChannelBase基类，设置基本属性
        //this.resBmp=resBmp;   // 注释代码：直接设置位图资源（已废弃，使用init()方法代替）
        init(resBmp);   // 初始化位图资源：调用init()方法，设置位图资源数组，创建绘制位图和画布
    }   // 构造函数结束
    
//region interface IWorkMode - 工作模式接口实现区域

    /**
     * 切换工作模式 - 切换到指定的工作模式
     * 
     * <p>切换工作模式，实现IWorkMode接口。在XY模式下，垂直通道的工作模式切换
     * 主要是刷新位图显示，标记位图需要更新，并调用onRefresh()方法刷新纹理。</p>
     * 
     * <h4>切换流程</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  工作模式切换流程                        │
     * ├─────────────────────────────────────────┤
     * │                                         │
     * │  Step 1: 标记位图需要更新                │
     * │    └─ this.isChanageBitmap = true       │
     * │    └─ 设置位图更新标志                  │
     * │                                         │
     * │  Step 2: 刷新位图纹理                    │
     * │    └─ onRefresh()                       │
     * │    └─ 调用父类onRefresh()方法           │
     * │    └─ 刷新OpenGL纹理                    │
     * │                                         │
     * │  XY模式特殊说明：                        │
     * │    - XY模式下不需要改变位置              │
     * │    - 只需要刷新位图显示                  │
     * │    - 垂直通道始终控制X轴位置             │
     * │                                         │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>workMode</td><td>@WorkMode int</td><td>工作模式常量（WorkMode_XY、WorkMode_YT、WorkMode_YTZOOM）</td></tr>
     * </table>
     * 
     * <h4>工作模式说明</h4>
     * <table border="1">
     *   <tr><th>工作模式</th><th>常量值</th><th>说明</th></tr>
     *   <tr><td>WorkMode_XY</td><td>0x03</td><td>X-Y图形模式（本类主要使用此模式）</td></tr>
     *   <tr><td>WorkMode_YT</td><td>0x01</td><td>Y-T时域模式（本类不使用）</td></tr>
     *   <tr><td>WorkMode_YTZOOM</td><td>0x02</td><td>Y-T缩放模式（本类不使用）</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>ChannelWaveManage_XY.switchWorkMode()中调用，切换工作模式</li>
     *   <li>WorkModeManage.setWorkMode()中调用，切换工作模式</li>
     * </ul>
     * 
     * <h4>使用示例</h4>
     * <pre>
     * channelWaveVertical.switchWorkMode(IWorkMode.WorkMode_XY);
     * </pre>
     * 
     * <h4>XY模式特殊说明</h4>
     * <ul>
     *   <li><b>位置不变</b>：XY模式下，垂直通道的位置不需要改变，始终控制X轴位置</li>
     *   <li><b>位图刷新</b>：只需要刷新位图显示，更新纹理</li>
     *   <li><b>不改变属性</b>：不改变通道的位置、选中状态、可见性等属性</li>
     * </ul>
     * 
     * <h4>注意事项</h4>
     * <ul>
     *   <li><b>XY模式专用</b>：本类主要用于XY模式，其他模式不使用</li>
     *   <li><b>位图更新</b>：必须设置isChanageBitmap = true，否则位图不会更新</li>
     *   <li><b>纹理刷新</b>：必须调用onRefresh()，否则OpenGL纹理不会更新</li>
     * </ul>
     * 
     * @param workMode 工作模式常量（WorkMode_XY、WorkMode_YT、WorkMode_YTZOOM）
     * @see IWorkMode#switchWorkMode(int)
     * @see VerticalChannelBase#onRefresh()
     */
    @Override   // 重写注解：重写IWorkMode接口的switchWorkMode方法
    public void switchWorkMode(@WorkMode int workMode) {   // 方法：切换工作模式，接收工作模式常量参数

        this.isChanageBitmap = true;   // 设置位图更新标志：标记位图需要更新，下次绘制时会更新纹理
        onRefresh();   // 刷新位图纹理：调用父类onRefresh()方法，刷新OpenGL纹理，更新显示
    }   // switchWorkMode方法结束

    //endregion - 工作模式接口实现区域结束
}   // ChannelWaveVertical类结束