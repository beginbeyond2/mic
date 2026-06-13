package com.micsig.tbook.tbookscope.wavezone.wave; // 波形显示管理包，定义波形显示管理接口，1

import android.graphics.Bitmap; // Android位图类，用于图像处理，1
import android.graphics.Canvas; // Android画布类，用于绘制图形，1

import com.chillingvan.canvasgl.ICanvasGL; // OpenGL画布接口，用于OpenGL绘制，1

/**
 * 波形显示管理接口 - 定义波形显示管理的核心操作规范
 * 
 * 【模块定位】
 * - 所属模块：wavezone.wave（波形显示区域-波形管理模块）
 * - 核心职责：定义波形显示管理器的基本行为规范，包括通道选中、位置调整、波形绘制等核心操作
 * - 架构层级：接口层，位于业务逻辑层之上，为波形管理提供统一抽象
 * 
 * 【核心职责】
 * 1. 通道选中状态管理：查询当前选中的通道号
 * 2. 波形位置管理：设置波形在Y轴的偏移位置
 * 3. 波形绘制管理：支持Android Canvas和OpenGL Canvas两种绘制方式
 * 
 * 【架构图】
 * ┌─────────────────────────────────────────────────────────────┐
 * │            WaveZoneDisplay（波形显示区域组件）                │
 * │                   (显示层控制器)                             │
 * │  - 管理波形显示区域                                          │
 * │  - 协调波形管理器                                            │
 * │  - 处理绘制请求                                              │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 持有引用
 * ┌─────────────────────────────────────────────────────────────┐
 * │            IWaveShowManage（波形显示管理接口）                │
 * │                   (波形管理抽象层)                           │
 * │  - 定义波形显示管理的核心操作规范                            │
 * │  - 提供统一的波形管理接口                                    │
 * │  - 支持多种渲染方式                                          │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 实现类
 * ┌─────────────────────────────────────────────────────────────┐
 * │  ChannelWaveManage_YT  │  MathRefWaveManage  │ SerialBusManage │
 * │  (YT模式通道波形管理)   │  (数学/参考通道管理) │ (串行总线管理)   │
 * │  - 管理CH1-CH4通道     │  - 管理数学通道      │ - 管理串行总线   │
 * │  - YT模式波形绘制      │  - 管理参考通道      │ - 解码波形显示   │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * 【接口方法说明】
 * 1. isSelected()：查询当前选中的通道号，返回-1表示未选中
 * 2. setOffsetY(int)：设置波形在Y轴的偏移位置，用于调整波形垂直位置
 * 3. draw(Canvas)：使用Android Canvas绘制波形，用于标准2D渲染
 * 4. draw(ICanvasGL)：使用OpenGL Canvas绘制波形，用于硬件加速渲染
 * 
 * 【依赖关系】
 * - 依赖类：
 *   · android.graphics.Canvas - Android标准画布，用于2D绘制
 *   · com.chillingvan.canvasgl.ICanvasGL - OpenGL画布接口，用于硬件加速
 * - 实现类：
 *   · ChannelWaveManage_YT - YT模式通道波形管理器
 *   · MathRefWaveManage - 数学/参考通道波形管理器
 *   · SerialBusManage - 串行总线波形管理器
 * - 调用方：
 *   · WaveZoneDisplay_YT - YT模式波形显示区域组件
 *   · WaveZoneDisplay_YTZoom - YT缩放模式波形显示组件
 * 
 * 【设计模式】
 * 1. 策略模式（Strategy Pattern）：
 *    - 定义波形显示的算法族，使不同类型的波形管理器可以互换
 *    - 支持多种渲染策略（Canvas/OpenGL）
 * 2. 接口隔离原则（Interface Segregation Principle）：
 *    - 接口职责单一，只包含波形显示管理的核心方法
 *    - 避免实现类依赖不需要的方法
 * 3. 多态性（Polymorphism）：
 *    - 通过接口引用调用不同实现类的绘制方法
 *    - 支持运行时动态切换波形管理器
 * 
 * 【使用场景】
 * 1. YT模式波形显示：ChannelWaveManage_YT实现此接口，管理CH1-CH4通道波形
 * 2. 数学/参考通道显示：MathRefWaveManage实现此接口，管理数学通道和参考通道
 * 3. 串行总线解码显示：SerialBusManage实现此接口，显示解码后的协议波形
 * 4. 波形位置调整：通过setOffsetY调整波形的垂直位置
 * 5. 通道选中切换：通过isSelected查询当前选中的通道
 * 
 * 【性能考虑】
 * - draw方法在每帧都会被调用，需要保证绘制性能
 * - OpenGL渲染路径适用于高性能场景
 * - Canvas渲染路径适用于兼容性场景
 * 
 * 【线程安全】
 * - 接口方法可能在UI线程和渲染线程中被调用
 * - 实现类需要保证线程安全
 * 
 * 【扩展性】
 * - 可扩展新的波形管理器类型（如FFT、XY模式等）
 * - 可扩展新的渲染方式（如Vulkan、Metal等）
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/5/19
 * @see ChannelWaveManage_YT
 * @see MathRefWaveManage
 * @see SerialBusManage
 */
public interface IWaveShowManage {

    /**
     * 查询当前选中的通道号
     * 
     * 【功能说明】
     * - 返回当前选中的通道编号，用于确定用户正在操作的通道
     * - 选中状态通常用于调整通道参数（如垂直位置、幅度等）
     * 
     * 【返回值】
     * @return int - 选中的通道号
     *         · -1：表示没有选中任何通道
     *         · 0-3：表示选中的CH1-CH4通道（对应CH1=0, CH2=1, CH3=2, CH4=3）
     *         · 其他值：根据具体实现类定义（如数学通道、参考通道等）
     * 
     * 【调用时机】
     * - 用户点击通道标签时，查询当前选中状态
     * - 调整通道参数时，确认操作的目标通道
     * - 绘制通道选中高亮效果时，确定需要高亮的通道
     * 
     * 【使用示例】
     * <pre>
     * IWaveShowManage manager = new ChannelWaveManage_YT(...);
     * int selectedChannel = manager.isSelected();
     * if (selectedChannel != -1) {
     *     // 有通道被选中，可以进行通道参数调整
     *     adjustChannelParameter(selectedChannel);
     * }
     * </pre>
     * 
     * 【实现要求】
     * - 必须返回有效的通道号或-1
     * - 不应该抛出异常
     * - 应该是线程安全的（如果在多线程环境中调用）
     */
    int isSelected();

    /**
     * 设置波形在Y轴的偏移坐标
     * 
     * 【功能说明】
     * - 设置波形在垂直方向（Y轴）的偏移位置
     * - 用于调整波形在屏幕上的垂直位置，实现波形的上下移动
     * - 偏移值以像素为单位，相对于波形显示区域的基准位置
     * 
     * 【参数说明】
     * @param offsetY int - Y轴偏移坐标（单位：像素）
     *         · 正值：波形向下移动
     *         · 负值：波形向上移动
     *         · 0：波形位于基准位置（通常是屏幕中心）
     *         · 范围：根据具体实现类定义，通常为负屏幕高度到正屏幕高度
     * 
     * 【调用时机】
     * - 用户拖动波形调整垂直位置时
     * - 加载保存的波形位置配置时
     * - 切换通道时，恢复该通道的上次位置
     * - 自动调整波形位置时
     * 
     * 【使用示例】
     * <pre>
     * IWaveShowManage manager = new ChannelWaveManage_YT(...);
     * // 将波形向下移动100像素
     * manager.setOffsetY(100);
     * // 将波形向上移动50像素
     * manager.setOffsetY(-50);
     * // 重置到基准位置
     * manager.setOffsetY(0);
     * </pre>
     * 
     * 【实现要求】
     * - 应该立即生效，影响下一次绘制
     * - 应该限制在合理的范围内，避免波形超出显示区域
     * - 应该是线程安全的（如果在多线程环境中调用）
     * - 可能需要触发重绘操作
     */
    void setOffsetY(int offsetY);

    /**
     * 使用Android Canvas绘制波形到波形区
     * 
     * 【功能说明】
     * - 使用Android标准Canvas API绘制波形
     * - 适用于标准2D渲染场景，兼容性好
     * - 绘制内容包括波形曲线、网格、标签等
     * 
     * 【参数说明】
     * @param canvas Canvas - Android画布对象，用于绑定绘制目标
     *         · 由调用方提供，通常是View的onDraw方法传入的Canvas
     *         · 画布的坐标系原点在左上角
     *         · 画布的尺寸对应波形显示区域的尺寸
     * 
     * 【调用时机】
     * - View的onDraw方法中被调用，每帧绘制时
     * - 波形数据更新后需要重绘时
     * - 波形位置、参数调整后需要刷新时
     * 
     * 【使用示例】
     * <pre>
     * // 在自定义View的onDraw方法中调用
     * protected void onDraw(Canvas canvas) {
     *     super.onDraw(canvas);
     *     IWaveShowManage manager = getWaveManager();
     *     manager.draw(canvas);
     * }
     * </pre>
     * 
     * 【实现要求】
     * - 必须在主线程（UI线程）中调用
     * - 绘制操作应该高效，避免创建临时对象
     * - 应该处理画布为null的情况
     * - 绘制内容应该限制在画布范围内
     * - 应该考虑性能优化，避免不必要的绘制
     * 
     * 【性能考虑】
     * - 该方法在每帧都会被调用，需要保证性能
     * - 避免在draw方法中进行耗时操作
     * - 使用硬件加速可以提高绘制性能
     */
    void draw(Canvas canvas);

    /**
     * 使用OpenGL Canvas绘制波形到波形区
     * 
     * 【功能说明】
     * - 使用OpenGL ES渲染方式绘制波形
     * - 适用于需要硬件加速的高性能渲染场景
     * - 支持复杂的图形效果和滤镜
     * 
     * 【参数说明】
     * @param canvas ICanvasGL - OpenGL画布接口，用于绑定OpenGL渲染目标
     *         · 由GLSurfaceView的渲染线程提供
     *         · 支持OpenGL ES 2.0/3.0特性
     *         · 提供硬件加速的图形绘制能力
     * 
     * 【调用时机】
     * - GLSurfaceView的渲染回调中被调用
     * - 需要高性能渲染时使用
     * - 需要使用OpenGL特效时使用
     * 
     * 【使用示例】
     * <pre>
     * // 在GLSurfaceView的渲染器中调用
     * public void onDrawFrame(GL10 unused) {
     *     ICanvasGL canvas = glSurfaceView.getCanvasGL();
     *     IWaveShowManage manager = getWaveManager();
     *     manager.draw(canvas);
     * }
     * </pre>
     * 
     * 【实现要求】
     * - 必须在OpenGL渲染线程中调用
     * - 需要正确管理OpenGL资源
     * - 应该处理canvas为null的情况
     * - 绘制操作应该符合OpenGL ES规范
     * 
     * 【性能考虑】
     * - OpenGL渲染通常比Canvas渲染更快
     * - 适合处理大量波形数据
     * - 支持GPU加速的图形效果
     * - 需要注意OpenGL资源的管理和释放
     * 
     * 【与draw(Canvas)的区别】
     * - draw(Canvas)：使用Android标准2D API，兼容性好，适合简单场景
     * - draw(ICanvasGL)：使用OpenGL ES，性能高，适合复杂场景
     * - 根据设备性能和渲染需求选择合适的绘制方式
     */
    void draw(ICanvasGL canvas);

    /**
     * 返回波形图
     * 
     * 【功能说明】
     * - 获取当前波形管理器管理的波形位图
     * - 用于波形截图、保存、导出等功能
     * 
     * 【返回值】
     * @return Bitmap - 波形位图对象
     *         · CH1~CH4通道共享一张波形图
     *         · 数学通道、参考通道各有独立的波形图
     *         · 总共可能有6张独立的波形图
     * 
     * 【调用时机】
     * - 用户截图保存波形时
     * - 导出波形数据时
     * - 波形打印功能时
     * 
     * 【实现要求】
     * - 返回的Bitmap应该是当前波形的快照
     * - 应该考虑内存管理，避免频繁创建Bitmap
     * - 可能需要同步机制保证数据一致性
     * 
     * 【注意】
     * - 此方法当前已被注释，可能在未来版本中启用
     * - 实现类可能需要提供此功能
     * 
     * @deprecated 此方法当前未启用，保留供未来扩展
     */
//    Bitmap getWavesBitmap();
}