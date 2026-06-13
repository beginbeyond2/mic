package com.micsig.tbook.tbookscope.wavezone.wave; // 波形显示区域-波形子模块包，包含示波器波形显示的核心组件，1

import android.graphics.Bitmap; // Android位图类，用于加载和绘制通道指示器图像，1

import com.chillingvan.canvasgl.ICanvasGL; // OpenGL画布接口，用于OpenGL渲染绘制，1
import com.micsig.tbook.scope.Display.DisplayXYService; // XY模式显示服务，用于设置XY模式的显示参数，1
import com.micsig.tbook.scope.ScopeBase; // 示波器基础类，提供坐标转换和精度处理，1
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量类，获取波形区域尺寸，1
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类，保存和读取通道位置缓存，1
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 工作模式接口，定义工作模式切换方法，1
import com.micsig.tbook.ui.wavezone.IWave; // 波形显示接口，定义波形的基本操作方法，1
import com.micsig.tbook.ui.wavezone.TChan; // 通道类，定义通道常量（Ch1、Ch2等），1


/**
 * XY模式通道波形管理类 - 示波器XY模式下双通道波形管理的核心实现类
 * 
 * 【模块定位】
 * - 所属模块：wavezone.wave（波形显示区域-波形子模块）
 * - 核心职责：管理XY模式下的两个通道波形（Ch1垂直波形和Ch2水平波形）
 * - 架构层级：管理层组件，实现IWorkMode和IWaveManage接口
 * 
 * 【核心职责】
 * 1. XY模式通道波形管理：管理两个通道波形对象（Ch1垂直波形和Ch2水平波形）
 * 2. XY模式通道位置管理：Ch1控制X轴位置，Ch2控制Y轴位置
 * 3. 通道选中状态管理：管理通道的选中状态，实现互斥选择机制
 * 4. 通道位置移动：支持垂直移动（Ch2）和水平移动（Ch1）
 * 5. 工作模式切换：支持XY模式的切换和初始化
 * 6. 通道可见性管理：XY模式下通道默认显示
 * 
 * 【架构图】
 * ┌─────────────────────────────────────────────────────────────┐
 * │              WaveZoneDisplay_XY（XY模式显示组件）             │
 * │                   (XY模式显示核心)                           │
 * │  - 管理XY模式显示区域                                        │
 * │  - 协调波形绘制和交互                                        │
 * │  - 处理用户操作事件                                          │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 创建和管理
 * ┌─────────────────────────────────────────────────────────────┐
 * │            ChannelWaveManage_XY（XY模式通道波形管理类）        │
 * │                (XY模式双通道波形管理核心)                      │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  核心属性                                            │  │
 * │  │  - channelWave: Ch2水平波形（控制Y轴）               │  │
 * │  │  - channelWaveVertical: Ch1垂直波形（控制X轴）       │  │
 * │  │  - selectChNo: 当前选中的通道号                      │  │
 * │  │  - onSelectChangeEvent: 选中状态变化事件             │  │
 * │  │  - onMovingWaveEvent: 波形移动事件                   │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  XY模式通道管理流程                                  │  │
 * │  │  1. 初始化：创建两个通道波形对象                      │  │
 * │  │  2. 位置设置：Ch1设置X轴，Ch2设置Y轴                  │  │
 * │  │  3. 选中管理：互斥选择，只能选中一个通道              │  │
 * │  │  4. 移动处理：根据选中通道执行相应移动                │  │
 * │  │  5. 绘制渲染：先绘制垂直波形，再绘制水平波形          │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 创建和管理
 * ┌─────────────────────────────────────────────────────────────┐
 * │     ChannelWaveVertical（Ch1垂直波形）                       │
 * │              ChannelWave（Ch2水平波形）                       │
 * │                (通道波形实现类)                              │
 * │  - 实现波形绘制                                              │
 * │  - 管理通道位置                                              │
 * │  - 处理选中状态                                              │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 实现接口
 * ┌─────────────────────────────────────────────────────────────┐
 * │              IWorkMode + IWaveManage（接口层）                │
 * │  - IWorkMode: 工作模式接口，定义模式切换方法                │
 * │  - IWaveManage: 波形管理接口，定义波形管理方法              │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * 【XY模式通道波形管理流程】
 * ┌─────────────────────────────────────────────────────────────┐
 * │                    XY模式初始化流程                          │
 * └─────────────────────────────────────────────────────────────┘
 *        ↓
 * ┌─────────────┐
 * │ 创建XY模式  │
 * │ 波形管理器  │
 * │ 构造函数    │
 * └──────┬──────┘
 *        ↓
 * ┌─────────────┐     ┌─────────────┐
 * │ 创建Ch1     │────→│ 创建Ch2     │
 * │ 垂直波形    │     │ 水平波形    │
 * │ (X轴控制)   │     │ (Y轴控制)   │
 * └──────┬──────┘     └──────┬──────┘
 *        ↓                    ↓
 * ┌─────────────┐     ┌─────────────┐
 * │ 从缓存读取  │     │ 从缓存读取  │
 * │ X轴位置     │     │ Y轴位置     │
 * └──────┬──────┘     └──────┬──────┘
 *        ↓                    ↓
 * ┌─────────────┐     ┌─────────────┐
 * │ 设置Display │     │ 设置Display │
 * │ XYService   │     │ XYService   │
 * │ X坐标       │     │ Y坐标       │
 * └──────┬──────┘     └──────┬──────┘
 *        ↓                    ↓
 * ┌─────────────┐     ┌─────────────┐
 * │ 设置选中    │     │ 设置选中    │
 * │ 状态=false  │     │ 状态=true   │
 * └──────┬──────┘     └──────┬──────┘
 *        ↓                    ↓
 * ┌─────────────────────────────────────┐
 * │          初始化完成                  │
 * │  Ch1控制X轴，Ch2控制Y轴              │
 * │  默认选中Ch2                         │
 * └─────────────────────────────────────┘
 * 
 * ┌─────────────────────────────────────────────────────────────┐
 * │                    XY模式通道移动流程                        │
 * └─────────────────────────────────────────────────────────────┘
 *        ↓
 * ┌─────────────┐
 * │ 用户拖动    │
 * │ 通道波形    │
 * └──────┬──────┘
 *        ↓
 * ┌─────────────┐
 * │ 判断选中    │
 * │ 通道号      │
 * └──────┬──────┘
 *        ↓
 *    ┌───┴───┐
 *    ↓       ↓
 * ┌───────┐ ┌───────┐
 * │Ch1选中│ │Ch2选中│
 * │(X轴)  │ │(Y轴)  │
 * └───┬───┘ └───┬───┘
 *     ↓         ↓
 * ┌───────┐ ┌───────┐
 * │水平移动│ │垂直移动│
 * │setX() │ │setY() │
 * └───┬───┘ └───┬───┘
 *     ↓         ↓
 * ┌───────┐ ┌───────┐
 * │更新    │ │更新    │
 * │Display │ │Display │
 * │XYService│ │XYService│
 * │X坐标   │ │Y坐标   │
 * └───┬───┘ └───┬───┘
 *     ↓         ↓
 * ┌─────────────────────────────────────┐
 * │          触发移动事件                │
 * │          通知外部管理器              │
 * └─────────────────────────────────────┘
 * 
 * 【依赖关系】
 * 实现接口：
 * - IWorkMode：工作模式接口，定义工作模式切换方法（switchWorkMode）
 * - IWaveManage：波形管理接口，定义波形管理方法（draw、setVisible、setOffsetY等）
 * 
 * 管理对象：
 * - ChannelWave：Ch2水平波形类，控制Y轴位置，实现水平波形显示
 * - ChannelWaveVertical：Ch1垂直波形类，控制X轴位置，继承VerticalChannelBase
 * 
 * 依赖服务：
 * - DisplayXYService：XY模式显示服务，设置XY模式的显示参数（X/Y坐标）
 * - ScopeBase：示波器基础类，提供XY模式宽度和高度
 * - GlobalVar：全局变量类，获取波形区域尺寸
 * - CacheUtil：缓存工具类，保存和读取通道位置缓存
 * - TChan：通道类，定义通道常量（Ch1、Ch2等）
 * 
 * 被管理类：
 * - WaveZoneDisplay_XY：XY模式显示组件，创建和管理ChannelWaveManage_XY实例
 * 
 * 【设计模式】
 * 1. 管理者模式（Manager Pattern）：
 *    - 作为通道波形的管理者，协调两个通道波形的显示和交互
 *    - 提供统一的接口管理多个通道波形对象
 * 
 * 2. 观察者模式（Observer Pattern）：
 *    - OnSelectChangeEvent：选中状态变化事件接口
 *    - OnMovingWaveEvent：波形移动事件接口
 *    - 当通道状态变化时通知观察者
 * 
 * 3. 状态模式（State Pattern）：
 *    - 根据选中通道号（selectChNo）执行不同的移动操作
 *    - Ch1选中时执行水平移动，Ch2选中时执行垂直移动
 * 
 * 4. 互斥选择模式：
 *    - 通道选中状态互斥，只能选中一个通道
 *    - 选中Ch1时自动取消Ch2选中，选中Ch2时自动取消Ch1选中
 * 
 * 【使用场景】
 * 1. XY模式初始化：初始化XY模式下的双通道波形显示
 * 2. XY模式通道位置调整：调整Ch1的X轴位置和Ch2的Y轴位置
 * 3. XY模式通道选择：选择要操作的通道（Ch1或Ch2）
 * 4. XY模式波形绘制：绘制XY模式下的通道波形标签
 * 5. XY模式工作模式切换：切换到XY模式或从XY模式切换
 * 
 * 【XY模式特殊说明】
 * 1. 通道位置映射：
 *    - Ch1（垂直波形）：控制X轴位置，使用setX()方法
 *    - Ch2（水平波形）：控制Y轴位置，使用setY()方法
 *    - 这与YT模式相反（YT模式下Ch1控制Y轴）
 * 
 * 2. 通道可见性：
 *    - XY模式下通道默认显示，setVisible()方法强制设置visible=true
 *    - 这是因为XY模式需要两个通道都显示才能形成X-Y关系图
 * 
 * 3. 通道选中状态：
 *    - 默认选中Ch2（水平波形）
 *    - 通道选中状态互斥，只能选中一个通道
 * 
 * 4. 位置移动方向：
 *    - Ch1选中时：水平移动（改变X轴位置）
 *    - Ch2选中时：垂直移动（改变Y轴位置）
 * 
 * 【数据流向】
 * 用户操作 → ChannelWaveManage_XY → ChannelWave/ChannelWaveVertical → DisplayXYService → XY模式显示
 * 
 * 【性能考虑】
 * 1. 绘制顺序：先绘制垂直波形（Ch1），再绘制水平波形（Ch2），避免遮挡
 * 2. 位置缓存：通道位置从缓存中读取，避免重复计算
 * 3. 事件通知：状态变化时触发事件通知，避免频繁回调
 * 
 * 【注意事项】
 * 1. XY模式下通道位置映射与YT模式不同
 * 2. 通道选中状态互斥，需要正确处理选中状态切换
 * 3. 位置移动需要根据选中通道执行相应操作
 * 4. XY模式下通道强制显示，setVisible()方法无效
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/11/6
 * @see IWorkMode
 * @see IWaveManage
 * @see ChannelWave
 * @see ChannelWaveVertical
 * @see DisplayXYService
 * @see WaveZoneDisplay_XY
 */

public class ChannelWaveManage_XY implements IWorkMode, IWaveManage { // XY模式通道波形管理类，实现IWorkMode和IWaveManage接口，1
    
    /**
     * 日志标签
     * 用于日志输出时标识当前类
     */
    private final String TAG = "ChannelWaveManage_XY"; // 日志标签，用于日志输出时标识当前类，1

    //region 成员变量
    
    /**
     * Ch2水平波形对象
     * 用于XY模式下显示Ch2的水平波形标签
     * 在XY模式下控制Y轴位置（垂直位置）
     * 
     * 【职责说明】
     * - 显示Ch2通道标签（如"CH2"）
     * - 控制Y轴位置（垂直移动）
     * - 管理Ch2的选中状态和可见性
     * 
     * 【XY模式特殊说明】
     * - 在XY模式下，Ch2控制Y轴位置
     * - 与YT模式不同（YT模式下Ch2也控制Y轴）
     */
    private ChannelWave channelWave; // Ch2水平波形对象，控制Y轴位置，1
    
    /**
     * Ch1垂直波形对象
     * 用于XY模式下显示Ch1的垂直波形标签
     * 在XY模式下控制X轴位置（水平位置）
     * 
     * 【职责说明】
     * - 显示Ch1通道标签（如"CH1"）
     * - 控制X轴位置（水平移动）
     * - 管理Ch1的选中状态和可见性
     * 
     * 【XY模式特殊说明】
     * - 在XY模式下，Ch1控制X轴位置
     * - 与YT模式不同（YT模式下Ch1控制Y轴）
     * - 继承VerticalChannelBase，实现垂直波形显示
     */
    private ChannelWaveVertical channelWaveVertical; // Ch1垂直波形对象，控制X轴位置，1
    
    /**
     * 当前选中的通道号
     * 用于标识当前用户操作的通道
     * 
     * 【取值范围】
     * - TChan.Ch1：选中Ch1通道（垂直波形，控制X轴）
     * - TChan.Ch2：选中Ch2通道（水平波形，控制Y轴）
     * - -1：未选中任何通道（初始化状态）
     * 
     * 【互斥选择机制】
     * - 通道选中状态互斥，只能选中一个通道
     * - 选中Ch1时自动取消Ch2选中
     * - 选中Ch2时自动取消Ch1选中
     */
    private int selectChNo = -1; // 当前选中的通道号，初始值为-1表示未选中，1
    
    /**
     * 选中状态变化事件监听器
     * 当通道选中状态变化时触发，通知外部管理器
     * 
     * 【触发时机】
     * - 用户点击选择通道时触发
     * - setSelectCursor()方法调用时触发
     * 
     * 【事件参数】
     * - IWave对象：发生选中状态变化的通道波形对象
     * - boolean：选中状态（true为选中，false为取消选中）
     */
    private IWave.OnSelectChangeEvent onSelectChangeEvent; // 选中状态变化事件监听器，1
    
    /**
     * 波形移动事件监听器
     * 当通道位置移动时触发，通知外部管理器
     * 
     * 【触发时机】
     * - 用户拖动通道时触发
     * - setOffsetY()方法调用时触发
     * - setPositionY()方法调用时触发
     * 
     * 【事件参数】
     * - IWave对象：发生位置移动的通道波形对象
     * - long：X坐标位置
     * - double：Y坐标位置
     * - boolean：是否为工作模式切换
     * - boolean：是否来自EventBus
     */
    private IWave.OnMovingWaveEvent onMovingWaveEvent; // 波形移动事件监听器，1
    
    //endregion

    /**
     * 构造函数 - 初始化XY模式通道波形管理器
     * 
     * 【功能说明】
     * 创建XY模式通道波形管理器，初始化两个通道波形对象（Ch1垂直波形和Ch2水平波形）
     * 设置通道位置、选中状态和事件监听器
     * 
     * 【参数说明】
     * @param resBmp 资源位图二维数组，包含所有通道的指示器位图
     *               resBmp[TChan.Ch1]：Ch1通道的指示器位图数组
     *               resBmp[TChan.Ch2]：Ch2通道的指示器位图数组
     * @param onMovingWaveEvent 波形移动事件监听器，当通道位置移动时触发
     * @param onSelectChangeEvent 选中状态变化事件监听器，当通道选中状态变化时触发
     * 
     * 【调用时机】
     * - WaveZoneDisplay_XY初始化XY模式时调用
     * - 切换到XY模式时创建新的管理器实例
     * 
     * 【实现流程】
     * 1. 创建XY模式波形区域位图（用于计算中心位置）
     * 2. 初始化Ch1垂直波形：
     *    - 创建ChannelWaveVertical对象
     *    - 设置通道ID为Ch1
     *    - 从缓存读取X轴位置
     *    - 设置DisplayXYService的X坐标
     *    - 设置选中状态为false
     *    - 设置事件监听器
     * 3. 初始化Ch2水平波形：
     *    - 创建ChannelWave对象
     *    - 设置通道ID为Ch2
     *    - 从缓存读取Y轴位置
     *    - 设置DisplayXYService的Y坐标
     *    - 设置选中状态为true（默认选中Ch2）
     *    - 设置可见性为true
     *    - 设置事件监听器
     * 
     * 【XY模式通道位置说明】
     * - Ch1控制X轴位置：从缓存读取位置，设置到DisplayXYService
     * - Ch2控制Y轴位置：从缓存读取位置，设置到DisplayXYService
     * - DisplayXYService负责将位置转换为XY模式显示坐标
     * 
     * 【关键代码行注释】
     */
    public ChannelWaveManage_XY(Bitmap[][] resBmp, IWave.OnMovingWaveEvent onMovingWaveEvent, IWave.OnSelectChangeEvent onSelectChangeEvent) { // 构造函数，初始化XY模式通道波形管理器，1
        Bitmap bmp = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_XY), // 创建XY模式波形区域位图，宽度为XY模式波形区域宽度，1
                GlobalVar.get().getWaveZoneHeight_Pix(IWorkMode.WorkMode_XY), Bitmap.Config.ARGB_8888); // 创建XY模式波形区域位图，高度为XY模式波形区域高度，配置为ARGB_8888，1
        int y = bmp.getWidth() / 2; // 计算中心位置（波形区域宽度的一半），用于初始化通道位置，1


        channelWaveVertical = new ChannelWaveVertical(resBmp[TChan.Ch1]); // 创建Ch1垂直波形对象，使用Ch1通道的指示器位图，1
        channelWaveVertical.setLineNameID(TChan.Ch1); // 设置Ch1垂直波形的通道ID为Ch1，1
        channelWaveVertical.setX(Math.round(CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_XY_POSITION + TChan.Ch1))); // 从缓存读取Ch1的X轴位置并设置，XY模式下Ch1控制X轴，1
        DisplayXYService.getInstance().setX(ScopeBase.getXYWidth() / 2 - (int) channelWaveVertical.getX()); // 设置DisplayXYService的X坐标，计算方式：XY宽度一半减去Ch1位置，1
        channelWaveVertical.setSelected(false); // 设置Ch1垂直波形的选中状态为false，默认不选中Ch1，1
        channelWaveVertical.setOnSelectChangeEvent(onSelectChangeEvent); // 设置Ch1垂直波形的选中状态变化事件监听器，1
        channelWaveVertical.setOnMovingWaveEvent(onMovingWaveEvent); // 设置Ch1垂直波形的波形移动事件监听器，1

        channelWave = new ChannelWave(resBmp[TChan.Ch2]); // 创建Ch2水平波形对象，使用Ch2通道的指示器位图，1
        channelWave.setLineNameId(TChan.Ch2); // 设置Ch2水平波形的通道ID为Ch2，1
        channelWave.setY(CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_XY_POSITION + TChan.Ch2)); // 从缓存读取Ch2的Y轴位置并设置，XY模式下Ch2控制Y轴，1
        DisplayXYService.getInstance().setY(ScopeBase.getXYHeight() / 2 - (int) Math.round(channelWave.getPosY())); // 设置DisplayXYService的Y坐标，计算方式：XY高度一半减去Ch2位置，1
        channelWave.setSelected(true); // 设置Ch2水平波形的选中状态为true，默认选中Ch2，1
        channelWave.setVisible(true); // 设置Ch2水平波形的可见性为true，XY模式下通道默认显示，1
        channelWave.setOnSelectChangeEvent(onSelectChangeEvent); // 设置Ch2水平波形的选中状态变化事件监听器，1
        channelWave.setOnMovingWaveEvent(onMovingWaveEvent); // 设置Ch2水平波形的波形移动事件监听器，1

    } // 结束构造函数，1

    //region interface IWorkMode

    /**
     * 切换工作模式 - IWorkMode接口实现
     * 
     * 【功能说明】
     * 切换工作模式，更新两个通道波形的工作模式状态
     * XY模式下，通道波形需要更新显示状态以适应新的工作模式
     * 
     * 【参数说明】
     * @param workMode 目标工作模式，使用@WorkMode注解约束取值范围
     *                 取值范围：WorkMode_None、WorkMode_YT、WorkMode_YTZOOM、WorkMode_XY
     * 
     * 【调用时机】
     * - WorkModeManage切换工作模式时调用
     * - 用户切换工作模式时触发
     * - 从XY模式切换到其他模式时调用
     * 
     * 【实现细节】
     * 1. 调用Ch2水平波形的switchWorkMode方法
     * 2. 调用Ch1垂直波形的switchWorkMode方法
     * 3. 两个通道波形都会更新显示状态
     * 
     * 【XY模式特殊说明】
     * - XY模式下，通道波形需要标记位图更新
     * - 切换到其他模式时，通道波形需要恢复到对应模式的显示状态
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void switchWorkMode(@WorkMode int workMode) { // 切换工作模式方法，参数为目标工作模式标识，1
        channelWave.switchWorkMode(workMode); // 调用Ch2水平波形的switchWorkMode方法，更新Ch2的工作模式状态，1
        channelWaveVertical.switchWorkMode(workMode); // 调用Ch1垂直波形的switchWorkMode方法，更新Ch1的工作模式状态，1
    } // 结束switchWorkMode方法，1

    //endregion

    //region interface IWaveManage

    /**
     * 绘制波形 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 绘制XY模式下的两个通道波形标签
     * 先绘制Ch1垂直波形，再绘制Ch2水平波形
     * 
     * 【参数说明】
     * @param canvas ICanvasGL画布对象，OpenGL渲染画布
     * 
     * 【调用时机】
     * - WaveZoneDisplay_XY渲染波形时调用
     * - 每帧渲染时调用
     * 
     * 【实现细节】
     * 1. 先绘制Ch1垂直波形（channelWaveVertical）
     * 2. 再绘制Ch2水平波形（channelWave）
     * 3. 绘制顺序确保Ch2不会被Ch1遮挡
     * 
     * 【绘制顺序说明】
     * - 先绘制垂直波形（Ch1），后绘制水平波形（Ch2）
     * - 这样可以确保水平波形标签不会被垂直波形遮挡
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void draw(ICanvasGL canvas) { // 绘制波形方法，参数为OpenGL画布对象，1
        channelWaveVertical.draw(canvas); // 先绘制Ch1垂直波形，确保Ch1在底层，1
        channelWave.draw(canvas); // 再绘制Ch2水平波形，确保Ch2在上层不被遮挡，1
    } // 结束draw方法，1

    /**
     * 设置通道可见性 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 设置通道的可见性
     * XY模式下，通道强制显示，setVisible方法无效
     * 
     * 【参数说明】
     * @param ChNo 通道号，取值范围：TChan.Ch1、TChan.Ch2等
     * @param visible 可见性，true为可见，false为隐藏（XY模式下无效）
     * 
     * 【调用时机】
     * - 用户打开/关闭通道时调用
     * - 程序控制通道显示/隐藏时调用
     * 
     * 【XY模式特殊说明】
     * - XY模式下，通道强制显示，setVisible方法无效
     * - 这是因为XY模式需要两个通道都显示才能形成X-Y关系图
     * - 无论传入的visible参数是什么，都会强制设置visible=true
     * 
     * 【实现细节】
     * 1. 强制设置Ch2水平波形的可见性为true
     * 2. 不处理Ch1垂直波形（Ch1继承VerticalChannelBase，默认可见）
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void setVisible(int ChNo, boolean visible) { // 设置通道可见性方法，参数为通道号和可见性，1
        channelWave.setVisible(true); // 强制设置Ch2水平波形的可见性为true，XY模式下通道强制显示，1
    } // 结束setVisible方法，1

    /**
     * 设置偏移量 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 根据选中通道设置位置偏移量
     * Ch2选中时执行垂直移动（改变Y轴位置）
     * Ch1选中时执行水平移动（改变X轴位置）
     * 
     * 【参数说明】
     * @param offsetY 偏移量（像素）
     *                - Ch2选中时：表示Y轴偏移量（垂直移动）
     *                - Ch1选中时：表示X轴偏移量（水平移动）
     * 
     * 【调用时机】
     * - 用户拖动通道时调用
     * - 程序调整通道位置时调用
     * 
     * 【XY模式特殊说明】
     * - XY模式下，通道移动方向与选中通道相关
     * - Ch2选中时：垂直移动（改变Y轴位置）
     * - Ch1选中时：水平移动（改变X轴位置）
     * - 这与YT模式不同（YT模式下所有通道都是垂直移动）
     * 
     * 【实现细节】
     * 1. 判断当前选中的通道号
     * 2. 如果选中Ch2（水平波形）：
     *    - 执行垂直移动，调用channelWave.setY()方法
     *    - 参数为当前Y位置减去偏移量
     * 3. 如果选中Ch1（垂直波形）：
     *    - 执行水平移动，调用channelWaveVertical.setX()方法
     *    - 参数为当前X位置减去偏移量
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void setOffsetY(int offsetY) { // 设置偏移量方法，参数为偏移量（像素），1
        if (selectChNo == channelWave.getLineNameID()) { // 判断是否选中Ch2（水平波形），1
            channelWave.setY(channelWave.getPosY() - offsetY); // Ch2选中时执行垂直移动，设置Y位置为当前Y位置减去偏移量，1
        } else if (selectChNo == channelWaveVertical.getLineNameID()) { // 判断是否选中Ch1（垂直波形），1
            channelWaveVertical.setX(channelWaveVertical.getX() - offsetY); // Ch1选中时执行水平移动，设置X位置为当前X位置减去偏移量，1
        } // 结束选中通道判断，1
    } // 结束setOffsetY方法，1

    /**
     * 设置通道位置 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 设置指定通道的位置
     * Ch2设置Y轴位置（垂直位置）
     * Ch1设置X轴位置（水平位置）
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：TChan.Ch1、TChan.Ch2等
     * @param positionY 位置值（像素）
     *                   - Ch2时：表示Y轴位置（垂直位置）
     *                   - Ch1时：表示X轴位置（水平位置）
     * 
     * 【调用时机】
     * - 外部模块设置通道位置时调用
     * - 恢复通道位置时调用
     * 
     * 【XY模式特殊说明】
     * - XY模式下，通道位置映射与YT模式不同
     * - Ch2设置Y轴位置（垂直位置）
     * - Ch1设置X轴位置（水平位置）
     * 
     * 【实现细节】
     * 1. 判断通道号
     * 2. 如果是Ch2：
     *    - 调用channelWave.setY()方法设置Y轴位置
     * 3. 如果是Ch1：
     *    - 调用channelWaveVertical.setX()方法设置X轴位置
     *    - 参数需要四舍五入转换为整数
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void setPositionY(int chNo, double positionY) { // 设置通道位置方法，参数为通道号和位置值，1
        if (TChan.Ch2 == chNo) { // 判断是否为Ch2通道，1
            channelWave.setY(positionY); // Ch2时设置Y轴位置（垂直位置），调用channelWave.setY()方法，1
        } else if (TChan.Ch1 == chNo) { // 判断是否为Ch1通道，1
            channelWaveVertical.setX(Math.round(positionY)); // Ch1时设置X轴位置（水平位置），调用channelWaveVertical.setX()方法，参数四舍五入，1
        } // 结束通道号判断，1
    } // 结束setPositionY方法，1

    /**
     * 从EventBus设置通道位置
     * 
     * 【功能说明】
     * 从EventBus接收位置更新，设置指定通道的位置
     * Ch2设置Y轴位置（垂直位置）
     * Ch1设置X轴位置（水平位置）
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：TChan.Ch1、TChan.Ch2等
     * @param positionY 位置值（像素）
     *                   - Ch2时：表示Y轴位置（垂直位置）
     *                   - Ch1时：表示X轴位置（水平位置）
     * 
     * 【调用时机】
     * - EventBus接收到位置更新事件时调用
     * - 外部模块通过EventBus更新通道位置时调用
     * 
     * 【与setPositionY的区别】
     * - setPositionY：普通设置位置方法
     * - setPositionYFromEventBus：从EventBus设置位置方法
     * - 区别在于调用的通道波形方法不同：
     *   - setPositionY调用setY()和setX()
     *   - setPositionYFromEventBus调用setYFromEventBus()和setXFromEventBus()
     * - setYFromEventBus和setXFromEventBus会触发移动事件，标记isFromEventBus=true
     * 
     * 【XY模式特殊说明】
     * - XY模式下，通道位置映射与YT模式不同
     * - Ch2设置Y轴位置（垂直位置）
     * - Ch1设置X轴位置（水平位置）
     * 
     * 【关键代码行注释】
     */
    public void setPositionYFromEventBus(int chNo, int positionY) { // 从EventBus设置通道位置方法，参数为通道号和位置值，1
        if (TChan.Ch2 == chNo) { // 判断是否为Ch2通道，1
            channelWave.setYFromEventBus(positionY); // Ch2时从EventBus设置Y轴位置，调用channelWave.setYFromEventBus()方法，1
        } else if (TChan.Ch1 == chNo) { // 判断是否为Ch1通道，1
            channelWaveVertical.setXFromEventBus(positionY); // Ch1时从EventBus设置X轴位置，调用channelWaveVertical.setXFromEventBus()方法，1
        } // 结束通道号判断，1
    } // 结束setPositionYFromEventBus方法，1

    /**
     * 获取通道位置 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 获取指定通道的位置
     * Ch2获取Y轴位置（垂直位置）
     * Ch1获取X轴位置（水平位置）
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：TChan.Ch1、TChan.Ch2等
     * 
     * 【返回值】
     * @return 位置值（像素）
     *         - Ch2时：返回Y轴位置（垂直位置）
     *         - Ch1时：返回X轴位置（水平位置）
     *         - 其他通道：返回0
     * 
     * 【调用时机】
     * - 外部模块获取通道位置时调用
     * - 保存通道位置到缓存时调用
     * 
     * 【XY模式特殊说明】
     * - XY模式下，通道位置映射与YT模式不同
     * - Ch2获取Y轴位置（垂直位置）
     * - Ch1获取X轴位置（水平位置）
     * 
     * 【实现细节】
     * 1. 判断通道号
     * 2. 如果是Ch2：
     *    - 返回channelWave.getPosY()（Y轴位置）
     * 3. 如果是Ch1：
     *    - 返回channelWaveVertical.getX()（X轴位置）
     * 4. 其他通道返回0
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public double getPositionY(int chNo) { // 获取通道位置方法，参数为通道号，1
        if (TChan.Ch2 == chNo) { // 判断是否为Ch2通道，1
            return channelWave.getPosY(); // Ch2时返回Y轴位置（垂直位置），调用channelWave.getPosY()方法，1
        } else if (TChan.Ch1 == chNo) { // 判断是否为Ch1通道，1
            return channelWaveVertical.getX(); // Ch1时返回X轴位置（水平位置），调用channelWaveVertical.getX()方法，1
        } // 结束通道号判断，1
        return 0; // 其他通道返回0，表示无效通道号，1
    } // 结束getPositionY方法，1

    /**
     * 设置通道中心位置 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 将指定通道的位置设置为中心位置（50%位置）
     * Ch2设置Y轴中心位置（波形区域高度的一半）
     * Ch1设置X轴中心位置（波形区域宽度的一半）
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：TChan.Ch1、TChan.Ch2等
     *             注意：参数注释说"这里没有作用"，但实际代码中会根据通道号设置对应通道的中心位置
     * 
     * 【调用时机】
     * - 用户点击"50%"按钮时调用
     * - 恢复通道到中心位置时调用
     * 
     * 【XY模式特殊说明】
     * - XY模式下，通道中心位置映射与YT模式不同
     * - Ch2设置Y轴中心位置（波形区域高度的一半）
     * - Ch1设置X轴中心位置（波形区域宽度的一半）
     * 
     * 【实现细节】
     * 1. 判断通道号是否为Ch1或Ch2
     * 2. 如果是Ch2：
     *    - 设置Y轴位置为波形区域高度的一半
     * 3. 如果是Ch1：
     *    - 设置X轴位置为波形区域宽度的一半
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void setCenterChY(int chNo) { // 设置通道中心位置方法，参数为通道号，1
        if (TChan.Ch1 == chNo || TChan.Ch2 == chNo) { // 判断通道号是否为Ch1或Ch2，1
            if (TChan.Ch2 == chNo) { // 判断是否为Ch2通道，1
                channelWave.setY(GlobalVar.get().getWaveZoneHeight_Pix(IWorkMode.WorkMode_XY) / 2); // Ch2时设置Y轴中心位置，值为波形区域高度的一半，1
            } else { // 否则为Ch1通道，1
                channelWaveVertical.setX(GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_XY) / 2); // Ch1时设置X轴中心位置，值为波形区域宽度的一半，1
            } // 结束通道号判断，1
        } // 结束通道号有效性判断，1
    } // 结束setCenterChY方法，1

    /**
     * 设置所有通道中心位置
     * 
     * 【功能说明】
     * 将所有通道的位置设置为中心位置（50%位置）
     * Ch2设置Y轴中心位置（波形区域高度的一半）
     * Ch1设置X轴中心位置（波形区域宽度的一半）
     * 
     * 【调用时机】
     * - 初始化XY模式时调用
     * - 恢复所有通道到中心位置时调用
     * 
     * 【实现细节】
     * 1. 设置Ch2的Y轴位置为波形区域高度的一半
     * 2. 设置Ch1的X轴位置为波形区域宽度的一半
     * 
     * 【关键代码行注释】
     */
    public void setCenterChY() { // 设置所有通道中心位置方法，无参数，1
        channelWave.setY(GlobalVar.get().getWaveZoneHeight_Pix(IWorkMode.WorkMode_XY) / 2); // 设置Ch2的Y轴中心位置，值为波形区域高度的一半，1
        channelWaveVertical.setX(GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_XY) / 2); // 设置Ch1的X轴中心位置，值为波形区域宽度的一半，1
    } // 结束setCenterChY方法，1

    /**
     * 设置选中通道 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 设置当前选中的通道，实现互斥选择机制
     * 选中Ch1时自动取消Ch2选中
     * 选中Ch2时自动取消Ch1选中
     * 
     * 【参数说明】
     * @param chNO 通道号，取值范围：TChan.Ch1、TChan.Ch2等
     * 
     * 【调用时机】
     * - 用户点击选择通道时调用
     * - 程序控制通道选中状态时调用
     * 
     * 【互斥选择机制】
     * - 通道选中状态互斥，只能选中一个通道
     * - 选中Ch1时：
     *   - 设置Ch1选中状态为true
     *   - 设置Ch2选中状态为false
     * - 选中Ch2或其他通道时：
     *   - 设置Ch1选中状态为false
     *   - 设置Ch2选中状态为true
     * 
     * 【XY模式特殊说明】
     * - XY模式下，通道选中状态互斥
     * - 默认选中Ch2（水平波形）
     * 
     * 【实现细节】
     * 1. 保存选中的通道号到selectChNo
     * 2. 判断通道号是否为Ch1
     * 3. 如果是Ch1：
     *    - 设置Ch1选中状态为true
     *    - 设置Ch2选中状态为false
     * 4. 如果不是Ch1（包括Ch2）：
     *    - 设置Ch1选中状态为false
     *    - 设置Ch2选中状态为true
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void setSelectCursor(int chNO) { // 设置选中通道方法，参数为通道号，1

        this.selectChNo = chNO; // 保存选中的通道号到selectChNo成员变量，1
        if (chNO == TChan.Ch1) { // 判断通道号是否为Ch1，1
            channelWaveVertical.setSelected(true); // 设置Ch1选中状态为true，选中Ch1垂直波形，1
            channelWave.setSelected(false); // 设置Ch2选中状态为false，取消Ch2水平波形选中，实现互斥选择，1
        } else { // 否则通道号不是Ch1（包括Ch2），1
            channelWaveVertical.setSelected(false); // 设置Ch1选中状态为false，取消Ch1垂直波形选中，1
            channelWave.setSelected(true); // 设置Ch2选中状态为true，选中Ch2水平波形，实现互斥选择，1
        } // 结束通道号判断，1
    } // 结束setSelectCursor方法，1

    /**
     * 选择光标 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 根据点击坐标判断点击在哪个通道波形区域内
     * 返回点击的通道号
     * 
     * 【参数说明】
     * @param x 点击X坐标（像素）
     * @param y 点击Y坐标（像素）
     * 
     * 【返回值】
     * @return 点击的通道号
     *         - TChan.Ch1：点击在Ch1垂直波形区域内
     *         - TChan.Ch2：点击在Ch2水平波形区域内
     *         - -1：点击不在任何通道波形区域内
     * 
     * 【调用时机】
     * - 用户点击屏幕时调用
     * - 判断是否选中通道时调用
     * 
     * 【XY模式特殊说明】
     * - 注释说"XY模式下应该没有用"，但实际代码中会判断点击位置
     * - 可能是因为XY模式下通道位置固定，不需要通过点击选择
     * 
     * 【实现细节】
     * 1. 初始化选中索引为-1
     * 2. 判断点击是否在Ch1垂直波形区域内
     *    - 如果在，返回Ch1通道号
     * 3. 判断点击是否在Ch2水平波形区域内
     *    - 如果在，返回Ch2通道号
     * 4. 如果都不在，返回-1
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public int selectCursor(int x, int y) { // 选择光标方法，参数为点击坐标，返回点击的通道号，1
        int selectIndex = -1; // 初始化选中索引为-1，表示未选中任何通道，1
        if (channelWaveVertical.selectCursor(x, y)) { // 判断点击是否在Ch1垂直波形区域内，调用channelWaveVertical.selectCursor()方法，1
            selectIndex = channelWaveVertical.getLineNameID(); // 如果在Ch1区域内，返回Ch1通道号，1
        } else if (channelWave.selectCursor(x, y)) { // 判断点击是否在Ch2水平波形区域内，调用channelWave.selectCursor()方法，1
            selectIndex = channelWave.getLineNameID(); // 如果在Ch2区域内，返回Ch2通道号，1
        } // 结束点击区域判断，1
        return selectIndex; // 返回选中索引，-1表示未选中任何通道，1
    } // 结束selectCursor方法，1

    /**
     * 移动像素 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 根据选中通道执行像素移动
     * Ch1选中时执行水平移动
     * Ch2选中时执行垂直移动
     * 
     * 【参数说明】
     * @param px 像素偏移量
     *           - Ch1选中时：表示X轴偏移量（水平移动）
     *           - Ch2选中时：表示Y轴偏移量（垂直移动）
     * 
     * 【调用时机】
     * - 用户拖动通道时调用
     * - 程序调整通道位置时调用
     * 
     * 【XY模式特殊说明】
     * - XY模式下，通道移动方向与选中通道相关
     * - Ch1选中时：水平移动（改变X轴位置）
     * - Ch2选中时：垂直移动（改变Y轴位置）
     * 
     * 【实现细节】
     * 1. 判断当前选中的通道号
     * 2. 如果选中Ch1（垂直波形）：
     *    - 调用channelWaveVertical.movePix()方法执行水平移动
     * 3. 如果选中Ch2（水平波形）：
     *    - 调用channelWave.movePix()方法执行垂直移动
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void movePix(int px) { // 移动像素方法，参数为像素偏移量，1
        if (selectChNo == channelWaveVertical.getLineNameID()) { // 判断是否选中Ch1（垂直波形），1
            channelWaveVertical.movePix(px); // Ch1选中时执行水平移动，调用channelWaveVertical.movePix()方法，1
        } else if (selectChNo == channelWave.getLineNameID()) { // 判断是否选中Ch2（水平波形），1
            channelWave.movePix(px); // Ch2选中时执行垂直移动，调用channelWave.movePix()方法，1
        } // 结束选中通道判断，1
    } // 结束movePix方法，1

    /**
     * 获取当前选中通道 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 获取当前选中的通道号
     * 
     * 【返回值】
     * @return 当前选中的通道号
     *         - TChan.Ch1：选中Ch1通道（垂直波形，控制X轴）
     *         - TChan.Ch2：选中Ch2通道（水平波形，控制Y轴）
     *         - -1：未选中任何通道
     * 
     * 【调用时机】
     * - 外部模块获取当前选中通道时调用
     * - 判断当前操作通道时调用
     * 
     * 【实现细节】
     * 1. 返回selectChNo成员变量
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public int getCurCh() { // 获取当前选中通道方法，无参数，返回当前选中的通道号，1
        return selectChNo; // 返回selectChNo成员变量，表示当前选中的通道号，1
    } // 结束getCurCh方法，1

    /**
     * 设置波形移动事件监听器
     * 
     * 【功能说明】
     * 设置波形移动事件监听器
     * 当通道位置移动时触发，通知外部管理器
     * 
     * 【参数说明】
     * @param onMovingWaveEvent 波形移动事件监听器
     * 
     * 【调用时机】
     * - 外部模块设置事件监听器时调用
     * - 初始化时设置事件监听器
     * 
     * 【实现细节】
     * 1. 保存事件监听器到成员变量
     * 
     * 【关键代码行注释】
     */
    public void setOnMovingWaveEvent(IWave.OnMovingWaveEvent onMovingWaveEvent) { // 设置波形移动事件监听器方法，参数为事件监听器，1
        this.onMovingWaveEvent = onMovingWaveEvent; // 保存事件监听器到成员变量，1
    } // 结束setOnMovingWaveEvent方法，1

    /**
     * 设置选中状态变化事件监听器
     * 
     * 【功能说明】
     * 设置选中状态变化事件监听器
     * 当通道选中状态变化时触发，通知外部管理器
     * 
     * 【参数说明】
     * @param onSelectChangeEvent 选中状态变化事件监听器
     * 
     * 【调用时机】
     * - 外部模块设置事件监听器时调用
     * - 初始化时设置事件监听器
     * 
     * 【实现细节】
     * 1. 保存事件监听器到成员变量
     * 
     * 【关键代码行注释】
     */
    public void setOnSelectChangeEvent(IWave.OnSelectChangeEvent onSelectChangeEvent) { // 设置选中状态变化事件监听器方法，参数为事件监听器，1
        this.onSelectChangeEvent = onSelectChangeEvent; // 保存事件监听器到成员变量，1
    } // 结束setOnSelectChangeEvent方法，1

    //endregion
} // 结束ChannelWaveManage_XY类定义，1