package com.micsig.tbook.tbookscope.wavezone.wave; // 波形显示区域-波形子模块包，包含示波器波形显示的核心组件，1

import android.graphics.Bitmap; // Android位图类，用于创建和管理波形显示区域的位图，1
import android.graphics.Canvas; // Android画布类，用于在位图上绘制图形，1
import android.graphics.Color; // Android颜色类，用于颜色转换和HSV计算，1
import android.graphics.PorterDuff; // Android图像混合模式类，用于图像合成，1
import android.graphics.PorterDuffXfermode; // Android图像混合模式转换类，用于设置画笔混合模式，1
import android.graphics.Rect; // Android矩形类，用于表示标签显示区域，1

import com.chillingvan.canvasgl.ICanvasGL; // OpenGL画布接口，用于OpenGL高性能渲染绘制，1
import com.micsig.base.Logger; // 日志工具类，用于日志输出调试信息，1
import com.micsig.tbook.scope.ScopeBase; // 示波器基础类，提供坐标转换和精度处理，1
import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂类，提供通道相关配置，1
import com.micsig.tbook.scope.surface.PreviewTextureView; // 预览纹理视图类，用于获取波形点阵数据，1
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量类，获取波形显示区域的尺寸配置，1
import com.micsig.tbook.ui.util.svg.SvgManager; // SVG管理类，用于从SVG路径创建位图，1
import com.micsig.tbook.ui.util.svg.SvgNodeInfo; // SVG节点信息类，提供SVG路径和颜色配置，1
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 工作模式接口，定义工作模式常量和切换方法，1
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 工作模式管理类，管理当前工作模式，1
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 光标管理类，管理光标测量功能，1
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage; // 测量管理类，管理测量功能，1
import com.micsig.tbook.ui.wavezone.IChan; // 通道接口，定义通道常量，1
import com.micsig.tbook.ui.wavezone.IWave; // 波形显示接口，定义波形的基本操作和事件，1
import com.micsig.tbook.ui.wavezone.TChan; // 通道常量类，定义通道编号常量（Ch1-Ch8、Math1-Math8、R1-R8），1

import java.nio.ByteBuffer; // Java字节缓冲区类，用于读取波形点阵数据，1

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              WaveManage_YT - YT模式波形管理类                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   - 所属模块：wavezone.wave（波形显示区域-波形子模块）                        ║
 * ║   - 核心职责：YT模式下波形显示区域的顶层管理类，封装通道波形管理器和数学参考  ║
 * ║               波形管理器，代理所有波形管理操作                                ║
 * ║   - 架构层级：管理层组件，实现IWorkMode和IWaveManage接口                     ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. YT模式波形区域管理：管理YT模式下的波形显示区域                          ║
 * ║   2. 通道波形管理器封装：封装ChannelWaveManage_YT，代理所有通道波形操作      ║
 * ║   3. 数学参考波形管理器封装：封装MathRefWaveManage，代理数学/参考波形操作    ║
 * ║   4. 工作模式切换：支持YT/YTZOOM模式的切换和初始化                           ║
 * ║   5. 波形绘制：绘制YT模式下的波形显示区域和通道波形                          ║
 * ║   6. 通道选择：支持通过标签选择和点阵数据选择通道                            ║
 * ║   7. 尺寸调整：支持波形显示区域尺寸的动态调整                                ║
 * ║   8. 颜色管理：支持通道颜色动态修改                                          ║
 * ║                                                                              ║
 * ║ 【架构图】                                                                   ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │            WaveZoneDisplay_YT（YT模式显示组件）                      │   ║
 * ║   │                   (YT模式显示核心)                                   │   ║
 * ║   │  - 管理YT模式显示区域                                                │   ║
 * ║   │  - 创建WaveManage_YT实例                                             │   ║
 * ║   │  - 处理用户交互事件                                                  │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                              ↓ 创建和管理                                   ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │              WaveManage_YT（YT模式波形管理类）                        │   ║
 * ║   │                (YT模式波形管理核心) ← 本类                            │   ║
 * ║   │  ┌──────────────────────────────────────────────────────────────┐  │   ║
 * ║   │  │  核心属性                                                    │  │   ║
 * ║   │  │  - resBmp: 通道指示器位图资源（SVG生成）                     │  │   ║
 * ║   │  │  - channelWaveManage: 通道波形管理器                        │  │   ║
 * ║   │  │  - mathRefWaveManage: 数学参考波形管理器                    │  │   ║
 * ║   │  │  - waveTextureView: 波形纹理视图（点阵数据）                 │  │   ║
 * ║   │  │  - chColorHSV: 通道颜色HSV数组                              │  │   ║
 * ║   │  │  - clickSelectEnable: 点选功能开关                          │  │   ║
 * ║   │  └──────────────────────────────────────────────────────────────┘  │   ║
 * ║   │  ┌──────────────────────────────────────────────────────────────┐  │   ║
 * ║   │  │  YT模式波形管理流程                                          │  │   ║
 * ║   │  │  1. 初始化：从SVG加载位图资源，创建管理器                   │  │   ║
 * ║   │  │  2. 绘制：绘制通道波形 + 数学参考波形                       │  │   ║
 * ║   │  │  3. 交互：代理通道选中、移动、位置设置                      │  │   ║
 * ║   │  │  4. 模式切换：更新管理器的工作模式                          │  │   ║
 * ║   │  │  5. 点选：通过点阵数据颜色匹配选择通道                      │  │   ║
 * ║   │  └──────────────────────────────────────────────────────────────┘  │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                              ↓ 创建和管理                                   ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │          ChannelWaveManage_YT（YT模式通道波形管理类）                │   ║
 * ║   │              MathRefWaveManage（数学参考波形管理类）                  │   ║
 * ║   │                (波形管理实现类)                                      │   ║
 * ║   │  - 管理Ch1-Ch8通道波形                                              │   ║
 * ║   │  - 管理Math1-Math8、R1-R8波形                                       │   ║
 * ║   │  - 实现通道选中、移动、位置设置                                      │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                              ↓ 实现接口                                     ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │              IWorkMode + IWaveManage（接口层）                        │   ║
 * ║   │  - IWorkMode: 工作模式接口，定义模式切换方法                        │   ║
 * ║   │  - IWaveManage: 波形管理接口，定义波形管理方法                      │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【YT模式波形管理流程】                                                       ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                    YT模式初始化流程                                  │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║          ↓                                                                  ║
 * ║   ┌─────────────┐                                                           ║
 * ║   │ 创建YT模式  │                                                           ║
 * ║   │ 波形管理器  │                                                           ║
 * ║   │ 构造函数    │                                                           ║
 * ║   └──────┬──────┘                                                           ║
 * ║          ↓                                                                  ║
 * ║   ┌─────────────┐                                                           ║
 * ║   │ 从SVG加载   │                                                           ║
 * ║   │ 通道指示器  │                                                           ║
 * ║   │ 位图资源    │                                                           ║
 * ║   │ (initResBmp)│                                                           ║
 * ║   └──────┬──────┘                                                           ║
 * ║          ↓                                                                  ║
 * ║   ┌─────────────┐                                                           ║
 * ║   │ 创建通道    │                                                           ║
 * ║   │ 波形管理器  │                                                           ║
 * ║   │ (ChannelWave│                                                           ║
 * ║   │ Manage_YT)  │                                                           ║
 * ║   └──────┬──────┘                                                           ║
 * ║          ↓                                                                  ║
 * ║   ┌─────────────┐                                                           ║
 * ║   │ 创建数学    │                                                           ║
 * ║   │ 参考波形    │                                                           ║
 * ║   │ 管理器      │                                                           ║
 * ║   │ (MathRefWave│                                                           ║
 * ║   │ Manage)     │                                                           ║
 * ║   └──────┬──────┘                                                           ║
 * ║          ↓                                                                  ║
 * ║   ┌─────────────────────────────────────┐                                   ║
 * ║   │          初始化完成                  │                                   ║
 * ║   │  WaveManage_YY封装两个管理器         │                                   ║
 * ║   │  所有操作代理到对应管理器            │                                   ║
 * ║   └─────────────────────────────────────┘                                   ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   实现接口：                                                                 ║
 * ║   - IWorkMode：工作模式接口，定义工作模式切换方法（switchWorkMode）          ║
 * ║   - IWaveManage：波形管理接口，定义波形管理方法（draw、setVisible等）        ║
 * ║                                                                              ║
 * ║   管理对象：                                                                 ║
 * ║   - ChannelWaveManage_YT：YT模式通道波形管理类，管理Ch1-Ch8通道波形          ║
 * ║   - MathRefWaveManage：数学参考波形管理类，管理Math1-Math8、R1-R8波形        ║
 * ║   - PreviewTextureView：波形纹理视图，提供点阵数据用于点选功能               ║
 * ║   - Bitmap[][]：通道指示器位图资源数组                                       ║
 * ║                                                                              ║
 * ║   依赖服务：                                                                 ║
 * ║   - GlobalVar：全局变量类，获取波形区域尺寸                                  ║
 * ║   - ScopeBase：示波器基础类，提供坐标转换                                    ║
 * ║   - SvgManager：SVG管理类，从SVG路径创建位图                                 ║
 * ║   - SvgNodeInfo：SVG节点信息类，提供SVG路径和颜色                            ║
 * ║   - TChan：通道常量类，定义通道编号常量                                      ║
 * ║   - WorkModeManage：工作模式管理类，管理当前工作模式                         ║
 * ║   - CursorManage：光标管理类，管理光标测量                                   ║
 * ║   - MeasureManage：测量管理类，管理测量功能                                  ║
 * ║   - SerialBusManage：串行总线管理类，管理串行总线显示                        ║
 * ║                                                                              ║
 * ║   被管理类：                                                                 ║
 * ║   - WaveZoneDisplay_YT：YT模式显示组件，创建和管理WaveManage_YT实例         ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   1. 代理模式（Proxy Pattern）：                                            ║
 * ║      - WaveManage_YT作为ChannelWaveManage_YT和MathRefWaveManage的代理       ║
 * ║      - 所有IWaveManage接口方法都代理到对应管理器                            ║
 * ║      - 提供统一的波形管理接口，隐藏内部实现                                  ║
 * ║                                                                              ║
 * ║   2. 门面模式（Facade Pattern）：                                            ║
 * ║      - 提供统一的波形管理接口，简化外部调用                                  ║
 * ║      - 封装复杂的通道波形管理和数学参考波形管理逻辑                          ║
 * ║      - 外部模块只需调用WaveManage_YT的方法                                  ║
 * ║                                                                              ║
 * ║   3. 组合模式（Composite Pattern）：                                         ║
 * ║      - WaveManage_YT组合了ChannelWaveManage_YT和MathRefWaveManage           ║
 * ║      - 提供统一的波形管理接口                                                ║
 * ║                                                                              ║
 * ║   4. 策略模式（Strategy Pattern）：                                          ║
 * ║      - 根据通道类型（Ch/Math/Ref）选择不同的管理器处理                      ║
 * ║      - TChan.isChan()判断物理通道，代理到channelWaveManage                  ║
 * ║      - TChan.isMathToR8()判断数学/参考通道，代理到mathRefWaveManage         ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. YT模式初始化：初始化YT模式下的波形显示区域                              ║
 * ║   2. YT模式波形绘制：绘制YT模式下的波形显示区域和通道波形                    ║
 * ║   3. YT模式通道交互：代理通道选中、移动、位置设置等操作                      ║
 * ║   4. YT模式工作模式切换：切换到YT模式或YTZOOM模式                            ║
 * ║   5. YT模式尺寸调整：调整波形显示区域尺寸                                    ║
 * ║   6. YT模式颜色修改：动态修改通道颜色                                        ║
 * ║   7. YT模式点选功能：通过点阵数据颜色匹配选择通道                            ║
 * ║                                                                              ║
 * ║ 【YT模式特殊说明】                                                           ║
 * ║   1. 通道指示器位图：                                                        ║
 * ║      - 使用SVG动态生成，支持颜色修改                                         ║
 * ║      - resBmp[通道号][状态索引]，状态索引0-5                                 ║
 * ║      - 索引0-2：未选中状态（标准、按下、上移）                               ║
 * ║      - 索引3-5：选中状态（标准、按下、上移）                                 ║
 * ║                                                                              ║
 * ║   2. 通道类型分类：                                                          ║
 * ║      - Ch1-Ch8：物理通道，由ChannelWaveManage_YT管理                        ║
 * ║      - Math1-Math8：数学通道，由MathRefWaveManage管理                       ║
 * ║      - R1-R8：参考通道，由MathRefWaveManage管理                             ║
 * ║                                                                              ║
 * ║   3. 代理机制：                                                              ║
 * ║      - 所有IWaveManage接口方法都代理到对应管理器                            ║
 * ║      - setVisible() → channelWaveManage/mathRefWaveManage.setVisible()     ║
 * ║      - setPositionY() → channelWaveManage.setY/mathRefWaveManage.setY      ║
 * ║      - selectCursor() → channelWaveManage/mathRefWaveManage.selectCursor   ║
 * ║      - movePix() → channelWaveManage/mathRefWaveManage.movePix             ║
 * ║                                                                              ║
 * ║   4. 绘制顺序：                                                              ║
 * ║      - 根据选中状态决定绘制顺序                                              ║
 * ║      - 选中通道波形时：先绘制数学参考波形，再绘制通道波形                    ║
 * ║      - 未选中通道波形时：先绘制通道波形，再绘制数学参考波形                  ║
 * ║                                                                              ║
 * ║   5. 点选功能：                                                              ║
 * ║      - 通过点阵数据颜色匹配选择通道                                          ║
 * ║      - 使用HSV颜色空间进行颜色匹配                                           ║
 * ║      - 支持从波形图像直接选择通道                                            ║
 * ║                                                                              ║
 * ║ 【数据流向】                                                                 ║
 * ║   用户操作 → WaveManage_YT → ChannelWaveManage_YT/MathRefWaveManage →       ║
 * ║   ChannelWave/MathRefWave → 显示更新                                        ║
 * ║                                                                              ║
 * ║ 【性能考虑】                                                                 ║
 * ║   1. SVG位图生成：使用SvgManager动态生成，支持颜色修改                       ║
 * ║   2. 绘制优化：使用OpenGL（ICanvasGL）进行高性能渲染                         ║
 * ║   3. 同步保护：绘制时使用synchronized保护对象                                ║
 * ║   4. 点选优化：使用HSV颜色空间快速匹配                                       ║
 * ║                                                                              ║
 * ║ 【注意事项】                                                                 ║
 * ║   1. WaveManage_YT是两个管理器的代理，所有操作都代理                        ║
 * ║   2. 通道类型判断使用TChan.isChan()和TChan.isMathToR8()                     ║
 * ║   3. 点选功能需要设置clickSelectEnable为true                                ║
 * ║   4. 颜色修改会重新生成SVG位图                                               ║
 * ║   5. YT模式下所有通道控制Y轴位置，与XY模式不同                               ║
 * ║                                                                              ║
 * ║ 【作者】 Created by liwb on 2017/5/19                                        ║
 * ║ 【版本】 1.0                                                                 ║
 * ║ 【参见】 @see IWorkMode                                                      ║
 * ║        @see IWaveManage                                                     ║
 * ║        @see ChannelWaveManage_YT                                            ║
 * ║        @see MathRefWaveManage                                               ║
 * ║        @see WaveZoneDisplay_YT                                              ║
 * ║        @see GlobalVar                                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

public class WaveManage_YT implements IWorkMode, IWaveManage { // YT模式波形管理类，实现IWorkMode和IWaveManage接口，作为ChannelWaveManage_YT和MathRefWaveManage的代理，1

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 日志标签
     * 用于日志输出时标识当前类
     */
    private static final String TAG="WaveManage_YT"; // 日志标签，用于日志输出时标识当前类，1

    /**
     * 通道指示器位图资源数组
     * 用于存储所有通道的指示器位图资源
     * 
     * 【数组结构】
     * - resBmp[通道号][状态索引]
     * - 第一维：通道号（Ch1-Ch8、Math1-Math8、R1-R8）
     * - 第二维：状态索引（0-5）
     * 
     * 【位图索引说明】
     * - 索引0（CH_N）：未选中标准状态
     * - 索引1（CH_N_DOWN）：未选中按下状态
     * - 索引2（CH_N_UP）：未选中上移状态
     * - 索引3（CH_S）：选中标准状态
     * - 索引4（CH_S_DOWN）：选中按下状态
     * - 索引5（CH_S_UP）：选中上移状态
     * 
     * 【位图生成方式】
     * - 使用SvgManager从SVG路径动态生成
     * - 支持颜色修改，修改颜色后重新生成
     * 
     * 【用途】
     * - 传递给ChannelWaveManage_YT和MathRefWaveManage
     * - 用于创建通道波形对象
     * - 通道波形对象使用这些位图显示通道指示器
     */
    private Bitmap[][] resBmp = new Bitmap[TChan.MaxChan + 1][6]; // 通道指示器位图资源数组，第一维为通道号，第二维为状态索引（0-5），1


    /**
     * 初始化通道指示器位图资源
     * 从SVG路径动态生成所有通道的指示器位图
     * 
     * 【功能说明】
     * 使用SvgManager从SVG路径创建所有通道（Ch1-Ch8、Math1-Math8、R1-R8）的指示器位图
     * 
     * 【生成流程】
     * 1. 为每个通道生成6种状态的位图：
     *    - 索引0：未选中标准状态（使用NColors）
     *    - 索引1：未选中按下状态（使用NColors）
     *    - 索引2：未选中上移状态（使用NColors）
     *    - 索引3：选中标准状态（使用SColors）
     *    - 索引4：选中按下状态（使用SColors）
     *    - 索引5：选中上移状态（使用SColors）
     * 2. 标准状态位图尺寸：32x24
     * 3. 按下/上移状态位图尺寸：24x32
     * 4. 调用handleColor()处理颜色HSV值
     * 
     * 【SVG路径来源】
     * - SvgNodeInfo.getStandardPath()：标准状态路径
     * - SvgNodeInfo.getDownPath()：按下状态路径
     * - SvgNodeInfo.getUpPath()：上移状态路径
     * 
     * 【颜色配置】
     * - SvgNodeInfo.getNColors()：未选中状态颜色
     * - SvgNodeInfo.getSColors()：选中状态颜色
     * 
     * 【调用时机】
     * - 构造函数中调用，初始化位图资源
     * - changeColor()修改颜色后调用，重新生成位图
     */
    private void initResBmp() { // 初始化通道指示器位图资源方法，从SVG路径动态生成位图，1
        // 加载Ch1通道的指示器位图（物理通道1）
        resBmp[TChan.Ch1][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch1), SvgNodeInfo.getNColors(TChan.Ch1), 32, 24); // 加载Ch1未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Ch1][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch1), SvgNodeInfo.getNColors(TChan.Ch1), 24, 32); // 加载Ch1未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch1][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch1), SvgNodeInfo.getNColors(TChan.Ch1), 24, 32); // 加载Ch1未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch1][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch1), SvgNodeInfo.getSColors(TChan.Ch1), 32, 24); // 加载Ch1选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Ch1][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch1), SvgNodeInfo.getSColors(TChan.Ch1), 24, 32); // 加载Ch1选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Ch1][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch1), SvgNodeInfo.getSColors(TChan.Ch1), 24, 32); // 加载Ch1选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Ch2通道的指示器位图（物理通道2）
        resBmp[TChan.Ch2][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch2), SvgNodeInfo.getNColors(TChan.Ch2), 32, 24); // 加载Ch2未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Ch2][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch2), SvgNodeInfo.getNColors(TChan.Ch2), 24, 32); // 加载Ch2未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch2][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch2), SvgNodeInfo.getNColors(TChan.Ch2), 24, 32); // 加载Ch2未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch2][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch2), SvgNodeInfo.getSColors(TChan.Ch2), 32, 24); // 加载Ch2选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Ch2][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch2), SvgNodeInfo.getSColors(TChan.Ch2), 24, 32); // 加载Ch2选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Ch2][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch2), SvgNodeInfo.getSColors(TChan.Ch2), 24, 32); // 加载Ch2选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Ch3通道的指示器位图（物理通道3）
        resBmp[TChan.Ch3][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch3), SvgNodeInfo.getNColors(TChan.Ch3), 32, 24); // 加载Ch3未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Ch3][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch3), SvgNodeInfo.getNColors(TChan.Ch3), 24, 32); // 加载Ch3未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch3][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch3), SvgNodeInfo.getNColors(TChan.Ch3), 24, 32); // 加载Ch3未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch3][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch3), SvgNodeInfo.getSColors(TChan.Ch3), 32, 24); // 加载Ch3选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Ch3][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch3), SvgNodeInfo.getSColors(TChan.Ch3), 24, 32); // 加载Ch3选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Ch3][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch3), SvgNodeInfo.getSColors(TChan.Ch3), 24, 32); // 加载Ch3选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Ch4通道的指示器位图（物理通道4）
        resBmp[TChan.Ch4][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch4), SvgNodeInfo.getNColors(TChan.Ch4), 32, 24); // 加载Ch4未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Ch4][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch4), SvgNodeInfo.getNColors(TChan.Ch4), 24, 32); // 加载Ch4未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch4][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch4), SvgNodeInfo.getNColors(TChan.Ch4), 24, 32); // 加载Ch4未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch4][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch4), SvgNodeInfo.getSColors(TChan.Ch4), 32, 24); // 加载Ch4选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Ch4][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch4), SvgNodeInfo.getSColors(TChan.Ch4), 24, 32); // 加载Ch4选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Ch4][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch4), SvgNodeInfo.getSColors(TChan.Ch4), 24, 32); // 加载Ch4选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Ch5通道的指示器位图（物理通道5）
        resBmp[TChan.Ch5][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch5), SvgNodeInfo.getNColors(TChan.Ch5), 32, 24); // 加载Ch5未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Ch5][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch5), SvgNodeInfo.getNColors(TChan.Ch5), 24, 32); // 加载Ch5未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch5][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch5), SvgNodeInfo.getNColors(TChan.Ch5), 24, 32); // 加载Ch5未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch5][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch5), SvgNodeInfo.getSColors(TChan.Ch5), 32, 24); // 加载Ch5选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Ch5][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch5), SvgNodeInfo.getSColors(TChan.Ch5), 24, 32); // 加载Ch5选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Ch5][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch5), SvgNodeInfo.getSColors(TChan.Ch5), 24, 32); // 加载Ch5选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Ch6通道的指示器位图（物理通道6）
        resBmp[TChan.Ch6][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch6), SvgNodeInfo.getNColors(TChan.Ch6), 32, 24); // 加载Ch6未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Ch6][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch6), SvgNodeInfo.getNColors(TChan.Ch6), 24, 32); // 加载Ch6未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch6][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch6), SvgNodeInfo.getNColors(TChan.Ch6), 24, 32); // 加载Ch6未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch6][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch6), SvgNodeInfo.getSColors(TChan.Ch6), 32, 24); // 加载Ch6选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Ch6][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch6), SvgNodeInfo.getSColors(TChan.Ch6), 24, 32); // 加载Ch6选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Ch6][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch6), SvgNodeInfo.getSColors(TChan.Ch6), 24, 32); // 加载Ch6选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Ch7通道的指示器位图（物理通道7）
        resBmp[TChan.Ch7][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch7), SvgNodeInfo.getNColors(TChan.Ch7), 32, 24); // 加载Ch7未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Ch7][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch7), SvgNodeInfo.getNColors(TChan.Ch7), 24, 32); // 加载Ch7未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch7][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch7), SvgNodeInfo.getNColors(TChan.Ch7), 24, 32); // 加载Ch7未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch7][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch7), SvgNodeInfo.getSColors(TChan.Ch7), 32, 24); // 加载Ch7选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Ch7][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch7), SvgNodeInfo.getSColors(TChan.Ch7), 24, 32); // 加载Ch7选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Ch7][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch7), SvgNodeInfo.getSColors(TChan.Ch7), 24, 32); // 加载Ch7选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Ch8通道的指示器位图（物理通道8）
        resBmp[TChan.Ch8][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch8), SvgNodeInfo.getNColors(TChan.Ch8), 32, 24); // 加载Ch8未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Ch8][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch8), SvgNodeInfo.getNColors(TChan.Ch8), 24, 32); // 加载Ch8未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch8][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch8), SvgNodeInfo.getNColors(TChan.Ch8), 24, 32); // 加载Ch8未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Ch8][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Ch8), SvgNodeInfo.getSColors(TChan.Ch8), 32, 24); // 加载Ch8选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Ch8][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Ch8), SvgNodeInfo.getSColors(TChan.Ch8), 24, 32); // 加载Ch8选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Ch8][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Ch8), SvgNodeInfo.getSColors(TChan.Ch8), 24, 32); // 加载Ch8选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Math1通道的指示器位图（数学通道1）
        resBmp[TChan.Math1][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math1), SvgNodeInfo.getNColors(TChan.Math1), 32, 24); // 加载Math1未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Math1][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math1), SvgNodeInfo.getNColors(TChan.Math1), 24, 32); // 加载Math1未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math1][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math1), SvgNodeInfo.getNColors(TChan.Math1), 24, 32); // 加载Math1未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math1][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math1), SvgNodeInfo.getSColors(TChan.Math1), 32, 24); // 加载Math1选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Math1][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math1), SvgNodeInfo.getSColors(TChan.Math1), 24, 32); // 加载Math1选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Math1][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math1), SvgNodeInfo.getSColors(TChan.Math1), 24, 32); // 加载Math1选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Math2通道的指示器位图（数学通道2）
        resBmp[TChan.Math2][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math2), SvgNodeInfo.getNColors(TChan.Math2), 32, 24); // 加载Math2未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Math2][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math2), SvgNodeInfo.getNColors(TChan.Math2), 24, 32); // 加载Math2未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math2][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math2), SvgNodeInfo.getNColors(TChan.Math2), 24, 32); // 加载Math2未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math2][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math2), SvgNodeInfo.getSColors(TChan.Math2), 32, 24); // 加载Math2选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Math2][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math2), SvgNodeInfo.getSColors(TChan.Math2), 24, 32); // 加载Math2选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Math2][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math2), SvgNodeInfo.getSColors(TChan.Math2), 24, 32); // 加载Math2选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Math3通道的指示器位图（数学通道3）
        resBmp[TChan.Math3][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math3), SvgNodeInfo.getNColors(TChan.Math3), 32, 24); // 加载Math3未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Math3][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math3), SvgNodeInfo.getNColors(TChan.Math3), 24, 32); // 加载Math3未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math3][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math3), SvgNodeInfo.getNColors(TChan.Math3), 24, 32); // 加载Math3未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math3][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math3), SvgNodeInfo.getSColors(TChan.Math3), 32, 24); // 加载Math3选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Math3][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math3), SvgNodeInfo.getSColors(TChan.Math3), 24, 32); // 加载Math3选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Math3][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math3), SvgNodeInfo.getSColors(TChan.Math3), 24, 32); // 加载Math3选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Math4通道的指示器位图（数学通道4）
        resBmp[TChan.Math4][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math4), SvgNodeInfo.getNColors(TChan.Math4), 32, 24); // 加载Math4未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Math4][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math4), SvgNodeInfo.getNColors(TChan.Math4), 24, 32); // 加载Math4未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math4][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math4), SvgNodeInfo.getNColors(TChan.Math4), 24, 32); // 加载Math4未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math4][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math4), SvgNodeInfo.getSColors(TChan.Math4), 32, 24); // 加载Math4选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Math4][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math4), SvgNodeInfo.getSColors(TChan.Math4), 24, 32); // 加载Math4选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Math4][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math4), SvgNodeInfo.getSColors(TChan.Math4), 24, 32); // 加载Math4选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Math5通道的指示器位图（数学通道5）
        resBmp[TChan.Math5][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math5), SvgNodeInfo.getNColors(TChan.Math5), 32, 24); // 加载Math5未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Math5][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math5), SvgNodeInfo.getNColors(TChan.Math5), 24, 32); // 加载Math5未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math5][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math5), SvgNodeInfo.getNColors(TChan.Math5), 24, 32); // 加载Math5未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math5][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math5), SvgNodeInfo.getSColors(TChan.Math5), 32, 24); // 加载Math5选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Math5][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math5), SvgNodeInfo.getSColors(TChan.Math5), 24, 32); // 加载Math5选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Math5][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math5), SvgNodeInfo.getSColors(TChan.Math5), 24, 32); // 加载Math5选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Math6通道的指示器位图（数学通道6）
        resBmp[TChan.Math6][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math6), SvgNodeInfo.getNColors(TChan.Math6), 32, 24); // 加载Math6未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Math6][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math6), SvgNodeInfo.getNColors(TChan.Math6), 24, 32); // 加载Math6未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math6][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math6), SvgNodeInfo.getNColors(TChan.Math6), 24, 32); // 加载Math6未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math6][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math6), SvgNodeInfo.getSColors(TChan.Math6), 32, 24); // 加载Math6选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Math6][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math6), SvgNodeInfo.getSColors(TChan.Math6), 24, 32); // 加载Math6选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Math6][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math6), SvgNodeInfo.getSColors(TChan.Math6), 24, 32); // 加载Math6选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Math7通道的指示器位图（数学通道7）
        resBmp[TChan.Math7][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math7), SvgNodeInfo.getNColors(TChan.Math7), 32, 24); // 加载Math7未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Math7][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math7), SvgNodeInfo.getNColors(TChan.Math7), 24, 32); // 加载Math7未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math7][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math7), SvgNodeInfo.getNColors(TChan.Math7), 24, 32); // 加载Math7未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math7][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math7), SvgNodeInfo.getSColors(TChan.Math7), 32, 24); // 加载Math7选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Math7][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math7), SvgNodeInfo.getSColors(TChan.Math7), 24, 32); // 加载Math7选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Math7][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math7), SvgNodeInfo.getSColors(TChan.Math7), 24, 32); // 加载Math7选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载Math8通道的指示器位图（数学通道8）
        resBmp[TChan.Math8][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math8), SvgNodeInfo.getNColors(TChan.Math8), 32, 24); // 加载Math8未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.Math8][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math8), SvgNodeInfo.getNColors(TChan.Math8), 24, 32); // 加载Math8未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math8][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math8), SvgNodeInfo.getNColors(TChan.Math8), 24, 32); // 加载Math8未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.Math8][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.Math8), SvgNodeInfo.getSColors(TChan.Math8), 32, 24); // 加载Math8选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.Math8][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.Math8), SvgNodeInfo.getSColors(TChan.Math8), 24, 32); // 加载Math8选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.Math8][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.Math8), SvgNodeInfo.getSColors(TChan.Math8), 24, 32); // 加载Math8选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载R1通道的指示器位图（参考通道1）
        resBmp[TChan.R1][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R1), SvgNodeInfo.getNColors(TChan.R1), 32, 24); // 加载R1未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.R1][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R1), SvgNodeInfo.getNColors(TChan.R1), 24, 32); // 加载R1未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R1][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R1), SvgNodeInfo.getNColors(TChan.R1), 24, 32); // 加载R1未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R1][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R1), SvgNodeInfo.getSColors(TChan.R1), 32, 24); // 加载R1选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.R1][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R1), SvgNodeInfo.getSColors(TChan.R1), 24, 32); // 加载R1选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.R1][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R1), SvgNodeInfo.getSColors(TChan.R1), 24, 32); // 加载R1选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载R2通道的指示器位图（参考通道2）
        resBmp[TChan.R2][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R2), SvgNodeInfo.getNColors(TChan.R2), 32, 24); // 加载R2未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.R2][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R2), SvgNodeInfo.getNColors(TChan.R2), 24, 32); // 加载R2未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R2][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R2), SvgNodeInfo.getNColors(TChan.R2), 24, 32); // 加载R2未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R2][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R2), SvgNodeInfo.getSColors(TChan.R2), 32, 24); // 加载R2选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.R2][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R2), SvgNodeInfo.getSColors(TChan.R2), 24, 32); // 加载R2选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.R2][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R2), SvgNodeInfo.getSColors(TChan.R2), 24, 32); // 加载R2选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载R3通道的指示器位图（参考通道3）
        resBmp[TChan.R3][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R3), SvgNodeInfo.getNColors(TChan.R3), 32, 24); // 加载R3未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.R3][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R3), SvgNodeInfo.getNColors(TChan.R3), 24, 32); // 加载R3未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R3][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R3), SvgNodeInfo.getNColors(TChan.R3), 24, 32); // 加载R3未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R3][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R3), SvgNodeInfo.getSColors(TChan.R3), 32, 24); // 加载R3选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.R3][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R3), SvgNodeInfo.getSColors(TChan.R3), 24, 32); // 加载R3选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.R3][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R3), SvgNodeInfo.getSColors(TChan.R3), 24, 32); // 加载R3选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载R4通道的指示器位图（参考通道4）
        resBmp[TChan.R4][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R4), SvgNodeInfo.getNColors(TChan.R4), 32, 24); // 加载R4未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.R4][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R4), SvgNodeInfo.getNColors(TChan.R4), 24, 32); // 加载R4未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R4][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R4), SvgNodeInfo.getNColors(TChan.R4), 24, 32); // 加载R4未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R4][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R4), SvgNodeInfo.getSColors(TChan.R4), 32, 24); // 加载R4选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.R4][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R4), SvgNodeInfo.getSColors(TChan.R4), 24, 32); // 加载R4选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.R4][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R4), SvgNodeInfo.getSColors(TChan.R4), 24, 32); // 加载R4选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载R5通道的指示器位图（参考通道5）
        resBmp[TChan.R5][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R5), SvgNodeInfo.getNColors(TChan.R5), 32, 24); // 加载R5未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.R5][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R5), SvgNodeInfo.getNColors(TChan.R5), 24, 32); // 加载R5未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R5][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R5), SvgNodeInfo.getNColors(TChan.R5), 24, 32); // 加载R5未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R5][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R5), SvgNodeInfo.getSColors(TChan.R5), 32, 24); // 加载R5选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.R5][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R5), SvgNodeInfo.getSColors(TChan.R5), 24, 32); // 加载R5选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.R5][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R5), SvgNodeInfo.getSColors(TChan.R5), 24, 32); // 加载R5选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载R6通道的指示器位图（参考通道6）
        resBmp[TChan.R6][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R6), SvgNodeInfo.getNColors(TChan.R6), 32, 24); // 加载R6未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.R6][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R6), SvgNodeInfo.getNColors(TChan.R6), 24, 32); // 加载R6未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R6][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R6), SvgNodeInfo.getNColors(TChan.R6), 24, 32); // 加载R6未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R6][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R6), SvgNodeInfo.getSColors(TChan.R6), 32, 24); // 加载R6选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.R6][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R6), SvgNodeInfo.getSColors(TChan.R6), 24, 32); // 加载R6选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.R6][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R6), SvgNodeInfo.getSColors(TChan.R6), 24, 32); // 加载R6选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载R7通道的指示器位图（参考通道7）
        resBmp[TChan.R7][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R7), SvgNodeInfo.getNColors(TChan.R7), 32, 24); // 加载R7未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.R7][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R7), SvgNodeInfo.getNColors(TChan.R7), 24, 32); // 加载R7未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R7][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R7), SvgNodeInfo.getNColors(TChan.R7), 24, 32); // 加载R7未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R7][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R7), SvgNodeInfo.getSColors(TChan.R7), 32, 24); // 加载R7选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.R7][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R7), SvgNodeInfo.getSColors(TChan.R7), 24, 32); // 加载R7选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.R7][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R7), SvgNodeInfo.getSColors(TChan.R7), 24, 32); // 加载R7选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1

        // 加载R8通道的指示器位图（参考通道8）
        resBmp[TChan.R8][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R8), SvgNodeInfo.getNColors(TChan.R8), 32, 24); // 加载R8未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[TChan.R8][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R8), SvgNodeInfo.getNColors(TChan.R8), 24, 32); // 加载R8未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R8][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R8), SvgNodeInfo.getNColors(TChan.R8), 24, 32); // 加载R8未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[TChan.R8][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(TChan.R8), SvgNodeInfo.getSColors(TChan.R8), 32, 24); // 加载R8选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[TChan.R8][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(TChan.R8), SvgNodeInfo.getSColors(TChan.R8), 24, 32); // 加载R8选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[TChan.R8][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(TChan.R8), SvgNodeInfo.getSColors(TChan.R8), 24, 32); // 加载R8选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1


        handleColor(); // 处理颜色HSV值，将颜色转换为HSV格式用于点选功能，1
    } // 结束initResBmp方法，1

    /**
     * 通道颜色数组（整数格式）
     * 用于存储所有通道的颜色值（24个颜色）
     * 
     * 【数组结构】
     * - 长度：24，包含所有通道的颜色
     * - 来源：SvgNodeInfo.getColorsIntForView()
     * 
     * 【用途】
     * - 用于点选功能中的颜色匹配
     * - 转换为HSV格式存储到chColorHSV
     */
    int[] chArrayColorId = new int[24]; // 通道颜色数组，存储24个通道的颜色值（整数格式），1

    /**
     * 通道颜色HSV数组
     * 用于存储所有通道颜色的HSV值
     * 
     * 【数组结构】
     * - chColorHSV[颜色索引][HSV分量]
     * - 第一维：颜色索引（0-23）
     * - 第二维：HSV分量（H、S、V）
     * 
     * 【用途】
     * - 用于点选功能中的颜色匹配
     * - HSV颜色空间更适合颜色比较
     * 
     * 【HSV说明】
     * - H（Hue）：色调，0-360度
     * - S（Saturation）：饱和度，0-1
     * - V（Value）：明度，0-1
     */
    private float[][] chColorHSV; // 通道颜色HSV数组，存储所有通道颜色的HSV值，1


    // ═══════════════════════════════════════════════════════════════════════════════
    // 位图状态索引常量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 位图状态索引常量定义
     * 用于标识resBmp数组中位图的状态索引
     */
    //region 对应图片数组的序号
    public static final int CH_N = 0; // 未选中标准状态索引，对应resBmp第二维索引0，1
    public static final int CH_N_DOWN = 1; // 未选中按下状态索引，对应resBmp第二维索引1，1
    public static final int CH_N_UP = 2; // 未选中上移状态索引，对应resBmp第二维索引2，1
    public static final int CH_S = 3; // 选中标准状态索引，对应resBmp第二维索引3，1
    public static final int CH_S_DOWN = 4; // 选中按下状态索引，对应resBmp第二维索引4，1
    public static final int CH_S_UP = 5; // 选中上移状态索引，对应resBmp第二维索引5，1
    //endregion

    // ═══════════════════════════════════════════════════════════════════════════════
    // 单例模式（已废弃）
    // ═══════════════════════════════════════════════════════════════════════════════

    //region 单例
//    private static final class WaveManage_YTHolder { // 已废弃：单例持有者类，1
//        public static final WaveManage_YT instance = new WaveManage_YT(); // 已废弃：单例实例，1
//    }
//
//    public static final WaveManage_YT get() { // 已废弃：获取单例实例方法，1
//        return WaveManage_YTHolder.instance; // 已废弃：返回单例实例，1
//    }
    //endregion

    /**
     * YT模式通道波形管理器
     * 管理YT模式下的物理通道波形（Ch1-Ch8）
     * 
     * 【职责说明】
     * - 管理Ch1-Ch8物理通道波形
     * - 实现通道选中、移动、位置设置等操作
     * - 实现IWaveShowManage和IWorkMode接口
     * 
     * 【代理关系】
     * - WaveManage_YT作为ChannelWaveManage_YT的代理
     * - 物理通道相关操作都代理到channelWaveManage
     */
    private ChannelWaveManage_YT channelWaveManage; // YT模式通道波形管理器，管理Ch1-Ch8物理通道波形，1

    /**
     * 数学参考波形管理器
     * 管理YT模式下的数学通道和参考通道波形（Math1-Math8、R1-R8）
     * 
     * 【职责说明】
     * - 管理Math1-Math8数学通道波形
     * - 管理R1-R8参考通道波形
     * - 实现通道选中、移动、位置设置等操作
     * 
     * 【代理关系】
     * - WaveManage_YT作为MathRefWaveManage的代理
     * - 数学/参考通道相关操作都代理到mathRefWaveManage
     */
    private MathRefWaveManage mathRefWaveManage; // 数学参考波形管理器，管理Math1-Math8、R1-R8波形，1

    /**
     * 波形纹理视图
     * 用于获取波形点阵数据，支持点选功能
     * 
     * 【职责说明】
     * - 提供波形点阵数据（ByteBuffer）
     * - 用于点选功能中的颜色匹配
     * - 提供波形宽度、高度、stride等信息
     * 
     * 【点选功能】
     * - 通过点阵数据颜色匹配选择通道
     * - 使用HSV颜色空间进行颜色比较
     */
    /** 波形数据 */
    private PreviewTextureView waveTextureView; // 波形纹理视图，提供点阵数据用于点选功能，1
    //bmp 就是波形图片，还要获得点阵数据为了选择波形用。 // 注释：bmp是波形图片，还需要点阵数据用于选择波形，1

    /**
     * 点选功能开关
     * 控制是否启用通过点阵数据选择通道的功能
     * 
     * 【取值范围】
     * - true：启用点选功能，可以通过点击波形图像选择通道
     * - false：禁用点选功能，只能通过标签选择通道
     * 
     * 【默认值】
     * - false，默认禁用点选功能
     */
    /** 点选功能 */
    private boolean clickSelectEnable=false; // 点选功能开关，默认false表示禁用点选功能，1

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造函数 - 初始化YT模式波形管理器
     * 
     * 【功能说明】
     * 创建YT模式波形管理器，初始化位图资源和两个管理器
     * 
     * 【参数说明】
     * @param onMovingWaveEvent 波形移动事件监听器，当通道位置移动时触发
     *                           - 触发时机：用户拖动通道、程序调整位置
     *                           - 事件参数：IWave对象、X坐标、Y坐标、是否工作模式切换、是否来自EventBus
     * @param onSelectChangeEvent 选中状态变化事件监听器，当通道选中状态变化时触发
     *                             - 触发时机：用户点击选择通道、程序设置选中状态
     *                             - 事件参数：IWave对象、选中状态（true/false）
     * 
     * 【调用时机】
     * - WaveZoneDisplay_YT初始化YT模式时调用
     * - 切换到YT模式时创建新的管理器实例
     * 
     * 【实现流程】
     * 1. 初始化通道指示器位图资源（initResBmp）
     *    - 从SVG路径动态生成所有通道的指示器位图
     *    - 处理颜色HSV值
     * 2. 创建通道波形管理器（channelWaveManage）
     *    - 传入位图资源和事件监听器
     *    - channelWaveManage负责管理Ch1-Ch8物理通道波形
     * 3. 创建数学参考波形管理器（mathRefWaveManage）
     *    - 传入位图资源和事件监听器
     *    - mathRefWaveManage负责管理Math1-Math8、R1-R8波形
     * 
     * 【关键代码行注释】
     */
    public WaveManage_YT(IWave.OnMovingWaveEvent onMovingWaveEvent, IWave.OnSelectChangeEvent onSelectChangeEvent) { // 构造函数，初始化YT模式波形管理器，参数为波形移动事件监听器和选中状态变化事件监听器，1
        initResBmp(); // 初始化通道指示器位图资源，从SVG路径动态生成所有通道的指示器位图，1

        channelWaveManage = new ChannelWaveManage_YT(resBmp, onMovingWaveEvent, onSelectChangeEvent); // 创建YT模式通道波形管理器，传入位图资源和事件监听器，管理Ch1-Ch8物理通道波形，1
        mathRefWaveManage = new MathRefWaveManage(resBmp, onMovingWaveEvent, onSelectChangeEvent); // 创建数学参考波形管理器，传入位图资源和事件监听器，管理Math1-Math8、R1-R8波形，1

    } // 结束构造函数，1

    /**
     * 处理颜色HSV值
     * 将通道颜色转换为HSV格式，用于点选功能中的颜色匹配
     * 
     * 【功能说明】
     * 从SvgNodeInfo获取颜色值，转换为HSV格式存储到chColorHSV数组
     * 
     * 【处理流程】
     * 1. 从SvgNodeInfo获取24个颜色值，复制到chArrayColorId
     * 2. 创建chColorHSV数组，大小为[24][3]
     * 3. 遍历所有颜色，转换为HSV格式
     * 4. 使用Color.colorToHSV()进行转换
     * 5. 将HSV值复制到chColorHSV数组
     * 
     * 【HSV说明】
     * - H（Hue）：色调，0-360度，表示颜色类型
     * - S（Saturation）：饱和度，0-1，表示颜色纯度
     * - V（Value）：明度，0-1，表示颜色亮度
     * 
     * 【用途】
     * - 用于点选功能中的颜色匹配
     * - HSV颜色空间更适合颜色比较，对光照变化不敏感
     * 
     * 【调用时机】
     * - initResBmp()中调用，初始化颜色HSV值
     * - changeColor()中调用，修改颜色后重新处理HSV值
     */
    private void handleColor() { // 处理颜色HSV值方法，将颜色转换为HSV格式用于点选功能，1
        System.arraycopy(SvgNodeInfo.getColorsIntForView(), 0, chArrayColorId, 0, 24); // 从SvgNodeInfo复制24个颜色值到chArrayColorId数组，1
        int color; // 定义颜色变量，用于临时存储颜色值，1
        float[] hsv = {0, 0, 0}; // 定义HSV数组，初始化为{0, 0, 0}，用于存储HSV值，1
        chColorHSV = new float[chArrayColorId.length][hsv.length]; // 创建chColorHSV数组，大小为[颜色数量][HSV分量数量]，1
        for (int i = 0; i < chArrayColorId.length; i++) { // 遍历所有颜色，i为颜色索引，1
            color = chArrayColorId[i]; // 获取当前颜色值，1
            Color.colorToHSV(color, hsv); // 将颜色转换为HSV格式，使用Color.colorToHSV()方法，1
            System.arraycopy(hsv, 0, chColorHSV[i], 0, hsv.length); // 将HSV值复制到chColorHSV数组对应位置，1
        } // 结束for循环，1
    } // 结束handleColor方法，1

    /**
     * 设置波形区域尺寸变化
     * 
     * 【功能说明】
     * 根据高度变化调整波形显示区域尺寸，更新两个管理器并刷新显示
     * 
     * 【参数说明】
     * @param height 新的高度值（像素）
     *                - 单位：像素
     *                - 用于计算新的波形区域尺寸
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 波形显示区域尺寸变化时调用
     * - 屏幕尺寸调整时调用
     * 
     * 【实现细节】
     * 1. 调用channelWaveManage.setWaveChange(height)更新通道波形管理器
     * 2. 调用mathRefWaveManage.setWaveChange(height)更新数学参考波形管理器
     * 3. 调用SerialBusManage.getInstance().changeSerialRect()更新串行总线显示区域
     * 4. 调用refresh()刷新所有波形显示
     * 
     * 【关键代码行注释】
     */
    public void setWaveChange(int height) { // 设置波形区域尺寸变化方法，参数为新的高度值（像素），1
        channelWaveManage.setWaveChange(height); // 代理到channelWaveManage.setWaveChange()方法，更新通道波形管理器的尺寸，1
        mathRefWaveManage.setWaveChange(height); // 代理到mathRefWaveManage.setWaveChange()方法，更新数学参考波形管理器的尺寸，1
        SerialBusManage.getInstance().changeSerialRect(); // 更新串行总线显示区域，调用SerialBusManage的changeSerialRect方法，1
        refresh(); // 刷新所有波形显示，调用refresh方法更新显示，1
    } // 结束setWaveChange方法，1

    /**
     * 修改通道颜色
     * 
     * 【功能说明】
     * 动态修改指定通道的颜色，重新生成SVG位图
     * 
     * 【参数说明】
     * @param chIndex 通道索引，取值范围：Ch1-Ch8、Math1-Math8、R1-R8
     * @param colorStr 颜色字符串，格式为十六进制颜色值（如"#FF0000"）
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 用户修改通道颜色时调用
     * - 需要动态修改通道颜色时调用
     * 
     * 【实现细节】
     * 1. 判断通道索引是否有效（TChan.isCh1ToR8）
     * 2. 重新生成该通道的6种状态位图：
     *    - 索引0：未选中标准状态（使用NColors）
     *    - 索引1：未选中按下状态（使用NColors）
     *    - 索引2：未选中上移状态（使用NColors）
     *    - 索引3：选中标准状态（使用SColors）
     *    - 索引4：选中按下状态（使用SColors）
     *    - 索引5：选中上移状态（使用SColors）
     * 3. 更新mathRefWaveManage的位图资源
     * 4. 重新处理颜色HSV值
     * 
     * 【关键代码行注释】
     */
    public void changeColor(int chIndex, String colorStr) { // 修改通道颜色方法，参数为通道索引和颜色字符串，1
        if (!TChan.isCh1ToR8(chIndex)) return; // 判断通道索引是否有效，如果不是Ch1-R8范围内的通道，直接返回，1
        resBmp[chIndex][0] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(chIndex), SvgNodeInfo.getNColors(chIndex), 32, 24); // 重新生成未选中标准状态位图，使用SVG路径和NColors，尺寸32x24，1
        resBmp[chIndex][1] = SvgManager.createSvg(SvgNodeInfo.getDownPath(chIndex), SvgNodeInfo.getNColors(chIndex), 24, 32); // 重新生成未选中按下状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[chIndex][2] = SvgManager.createSvg(SvgNodeInfo.getUpPath(chIndex), SvgNodeInfo.getNColors(chIndex), 24, 32); // 重新生成未选中上移状态位图，使用SVG路径和NColors，尺寸24x32，1
        resBmp[chIndex][3] = SvgManager.createSvg(SvgNodeInfo.getStandardPath(chIndex), SvgNodeInfo.getSColors(chIndex), 32, 24); // 重新生成选中标准状态位图，使用SVG路径和SColors，尺寸32x24，1
        resBmp[chIndex][4] = SvgManager.createSvg(SvgNodeInfo.getDownPath(chIndex), SvgNodeInfo.getSColors(chIndex), 24, 32); // 重新生成选中按下状态位图，使用SVG路径和SColors，尺寸24x32，1
        resBmp[chIndex][5] = SvgManager.createSvg(SvgNodeInfo.getUpPath(chIndex), SvgNodeInfo.getSColors(chIndex), 24, 32); // 重新生成选中上移状态位图，使用SVG路径和SColors，尺寸24x32，1
        mathRefWaveManage.updateResBmp(chIndex, resBmp); // 更新mathRefWaveManage的位图资源，传递新的位图数组，1
        handleColor(); // 重新处理颜色HSV值，更新chColorHSV数组，1
    } // 结束changeColor方法，1

    // ═══════════════════════════════════════════════════════════════════════════════
    // interface IWorkMode - 工作模式接口实现
    // ═══════════════════════════════════════════════════════════════════════════════

    //region IWorkMode接口
    /**
     * 当前工作模式
     * 存储当前的工作模式状态
     * 
     * 【取值范围】
     * - WorkMode_YT（0x00）：YT模式
     * - WorkMode_YTZOOM（0x01）：YT缩放模式
     * - WorkMode_XY（0x02）：XY模式
     * - WorkMode_None（-1）：无模式
     * 
     * 【默认值】
     * - WorkMode_YT，默认为YT模式
     */
    private @WorkMode int mWorkMode = IWorkMode.WorkMode_YT; // 当前工作模式，默认为YT模式，使用@WorkMode注解约束取值范围，1

    /**
     * 切换工作模式 - IWorkMode接口实现
     * 
     * 【功能说明】
     * 切换工作模式，更新两个管理器的工作模式状态
     * 
     * 【参数说明】
     * @param workMode 目标工作模式，使用@WorkMode注解约束取值范围
     *                 取值范围：WorkMode_None、WorkMode_YT、WorkMode_YTZOOM、WorkMode_XY
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - WorkModeManage切换工作模式时调用
     * - 用户切换工作模式时触发
     * - 从YT模式切换到其他模式时调用
     * 
     * 【代理机制】
     * - 直接调用channelWaveManage.switchWorkMode(workMode)
     * - 直接调用mathRefWaveManage.switchWorkMode(workMode)
     * - 两个管理器负责实际的工作模式切换
     * 
     * 【实现细节】
     * - 如果目标模式与当前模式相同，直接返回不执行
     * - 更新mWorkMode成员变量
     * - channelWaveManage会更新Ch1-Ch8通道波形的工作模式状态
     * - mathRefWaveManage会更新Math1-Math8、R1-R8波形的工作模式状态
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void switchWorkMode(@WorkMode int workMode) { // 切换工作模式方法，参数为目标工作模式标识，1
        if (mWorkMode == workMode) return; // 如果目标模式与当前模式相同，直接返回不执行，避免重复切换，1
        this.mWorkMode = workMode; // 更新mWorkMode成员变量，记录当前工作模式，1
        channelWaveManage.switchWorkMode(workMode); // 代理到channelWaveManage.switchWorkMode()方法，更新通道波形管理器的工作模式，1
        mathRefWaveManage.switchWorkMode(workMode); // 代理到mathRefWaveManage.switchWorkMode()方法，更新数学参考波形管理器的工作模式，1
    } // 结束switchWorkMode方法，1

    //endregion

    /**
     * 绘制波形到Canvas - 使用Android Canvas绘制（已废弃）
     * 
     * 【功能说明】
     * 使用Android Canvas绘制波形（已废弃，现在使用OpenGL绘制）
     * 
     * 【参数说明】
     * @param canvas Android Canvas对象，用于绘制图形
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 已废弃，不再调用
     * 
     * 【注释说明】
     * - 方法体内的代码已注释，表示已废弃
     * - 原实现使用PorterDuff混合模式绘制波形
     * - 现在使用draw(ICanvasGL canvas)方法进行OpenGL绘制
     */
    public void draw(Canvas canvas) { // 绘制波形到Canvas方法，已废弃，参数为Android Canvas对象，1
//        synchronized (this) { // 已废弃：同步块，保护绘制操作，1
//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); // 已废弃：设置清除混合模式，1
//            mCanvas.drawPaint(paint); // 已废弃：清除画布，1
//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC)); // 已废弃：设置源混合模式，1
//
//            if (channelWaveManage.isSelected() != -1) { // 已废弃：判断是否有选中的通道波形，1
//                synchronized (mathRefWaveManage.getWavesBitmap()) { // 已废弃：同步数学参考波形位图，1
//                    mCanvas.drawBitmap(mathRefWaveManage.getWavesBitmap(), 0, 0, paint); // 已废弃：绘制数学参考波形位图，1
//                } // 已废弃：结束同步块，1
//                synchronized (channelWaveManage.getWavesBitmap()) { // 已废弃：同步通道波形位图，1
//                    mCanvas.drawBitmap(channelWaveManage.getWavesBitmap(), 0, 0, paint); // 已废弃：绘制通道波形位图，1
//                } // 已废弃：结束同步块，1
////                mCanvas.drawBitmap(tempBmp, 0, 0, paint); // 已废弃：绘制临时位图，1
//
////                canvas.drawBitmap(bmp, 0, 0, null); // 已废弃：绘制位图到Canvas，1
//                mathRefWaveManage.draw(canvas); // 已废弃：绘制数学参考波形，1
//                channelWaveManage.draw(canvas); // 已废弃：绘制通道波形，1
//            } else { // 已废弃：没有选中通道波形，1
//                synchronized (channelWaveManage.getWavesBitmap()) { // 已废弃：同步通道波形位图，1
//                    mCanvas.drawBitmap(channelWaveManage.getWavesBitmap(), 0, 0, paint); // 已废弃：绘制通道波形位图，1
//                } // 已废弃：结束同步块，1
//                synchronized (mathRefWaveManage.getWavesBitmap()) { // 已废弃：同步数学参考波形位图，1
//                    mCanvas.drawBitmap(mathRefWaveManage.getWavesBitmap(), 0, 0, paint); // 已废弃：绘制数学参考波形位图，1
//                } // 已废弃：结束同步块，1
////                mCanvas.drawBitmap(tempBmp, 0, 0, paint); // 已废弃：绘制临时位图，1
////                canvas.drawBitmap(bmp, 0, 0, null); // 已废弃：绘制位图到Canvas，1
//                channelWaveManage.draw(canvas); // 已废弃：绘制通道波形，1
//                mathRefWaveManage.draw(canvas); // 已废弃：绘制数学参考波形，1
//            } // 已废弃：结束if-else判断，1
//        } // 已废弃：结束同步块，1
    } // 结束draw方法，已废弃，1

    /**
     * 绘制波形到OpenGL Canvas - IWaveManage接口实现
     * 
     * 【功能说明】
     * 使用OpenGL绘制YT模式下的波形显示区域和通道波形
     * 根据选中状态决定绘制顺序
     * 
     * 【参数说明】
     * @param canvas ICanvasGL画布对象，OpenGL渲染画布
     *               - 类型：ICanvasGL（OpenGL画布接口）
     *               - 用途：提供OpenGL渲染环境，支持高性能图形绘制
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - WaveZoneDisplay_YT渲染波形时调用
     * - 每帧渲染时调用
     * 
     * 【实现细节】
     * 1. 使用synchronized保护绘制操作，避免并发问题
     * 2. 判断是否有选中的通道波形（channelWaveManage.isSelected() != -1）
     * 3. 如果选中通道波形：
     *    - 先绘制数学参考波形（mathRefWaveManage.draw(canvas))
     *    - 再绘制通道波形（channelWaveManage.draw(canvas))
     *    - 这样通道波形显示在上层
     * 4. 如果未选中通道波形：
     *    - 先绘制通道波形（channelWaveManage.draw(canvas))
     *    - 再绘制数学参考波形（mathRefWaveManage.draw(canvas))
     *    - 这样数学参考波形显示在上层
     * 
     * 【绘制顺序说明】
     * - 选中通道波形时：数学参考波形在下层，通道波形在上层
     * - 未选中通道波形时：通道波形在下层，数学参考波形在上层
     * - 绘制顺序影响显示层级
     * 
     * 【同步保护说明】
     * - 使用synchronized(this)保护绘制操作
     * - 避免并发绘制导致的竞争问题
     * - 确保绘制操作的线程安全
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void draw(ICanvasGL canvas) { // 绘制波形到OpenGL Canvas方法，参数为OpenGL画布对象，1
        synchronized (this) { // 使用synchronized保护绘制操作，避免并发问题，1
            if (channelWaveManage.isSelected() != -1) { // 判断是否有选中的通道波形，-1表示未选中，1
                //canvas.drawBitmap(bmp, 0, 0, false); // 注释掉的代码，可能是之前的实现方式，1
                mathRefWaveManage.draw(canvas); // 先绘制数学参考波形，代理到mathRefWaveManage.draw()方法，1
                channelWaveManage.draw(canvas); // 再绘制通道波形，代理到channelWaveManage.draw()方法，通道波形显示在上层，1
            } else { // 没有选中通道波形，1
                //canvas.drawBitmap(bmp, 0, 0, false); // 注释掉的代码，可能是之前的实现方式，1
                channelWaveManage.draw(canvas); // 先绘制通道波形，代理到channelWaveManage.draw()方法，1
                mathRefWaveManage.draw(canvas); // 再绘制数学参考波形，代理到mathRefWaveManage.draw()方法，数学参考波形显示在上层，1
            } // 结束if-else判断，1
        } // 结束synchronized同步块，1
    } // 结束draw方法，1

    /**
     * 设置通道可见性 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 设置通道的可见性，代理到两个管理器
     * 
     * 【参数说明】
     * @param ChNo 通道号，取值范围：Ch1-Ch8、Math1-Math8、R1-R8
     * @param visible 可见性，true为可见，false为隐藏
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 用户打开/关闭通道时调用
     * - 程序控制通道显示/隐藏时调用
     * 
     * 【代理机制】
     * - 直接调用channelWaveManage.setVisible(ChNo, visible)
     * - 直接调用mathRefWaveManage.setVisible(ChNo, visible)
     * - 两个管理器都会尝试设置可见性，只有对应类型的通道会生效
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void setVisible(int ChNo, boolean visible) { // 设置通道可见性方法，参数为通道号和可见性，1
        channelWaveManage.setVisible(ChNo, visible); // 代理到channelWaveManage.setVisible()方法，设置通道可见性，1
        mathRefWaveManage.setVisible(ChNo, visible); // 代理到mathRefWaveManage.setVisible()方法，设置数学参考波形可见性，1
    } // 结束setVisible方法，1

    /**
     * 设置通道标签
     * 
     * 【功能说明】
     * 设置指定通道的标签文本，代理到两个管理器
     * 
     * 【参数说明】
     * @param ChNo 通道号，取值范围：Ch1-Ch8、Math1-Math8、R1-R8
     * @param label 标签文本内容
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 用户修改通道标签时调用
     * - 需要程序化设置通道标签时调用
     * 
     * 【代理机制】
     * - 直接调用channelWaveManage.setChannelLabel(ChNo, label)
     * - 直接调用mathRefWaveManage.setChannelLabel(ChNo, label)
     * - 两个管理器都会尝试设置标签，只有对应类型的通道会生效
     * 
     * 【关键代码行注释】
     */
    public void setChannelLabel(int ChNo, String label) { // 设置通道标签方法，参数为通道号和标签文本，1
        channelWaveManage.setChannelLabel(ChNo, label); // 代理到channelWaveManage.setChannelLabel()方法，设置通道标签，1
        mathRefWaveManage.setChannelLabel(ChNo, label); // 代理到mathRefWaveManage.setChannelLabel()方法，设置数学参考波形标签，1
    } // 结束setChannelLabel方法，1

    /**
     * 获取通道标签
     * 
     * 【功能说明】
     * 获取指定通道的标签文本，根据通道类型选择对应管理器
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：Ch1-Ch8、Math1-Math8、R1-R8
     * 
     * 【返回值】
     * @return 标签文本内容
     * 
     * 【调用时机】
     * - 需要查询通道标签时调用
     * - UI显示通道标签时调用
     * 
     * 【策略模式】
     * - 使用TChan.isChan(chNo)判断是否为物理通道
     * - 如果是物理通道（Ch1-Ch8），代理到channelWaveManage.getChannelLabel(chNo)
     * - 如果不是物理通道（Math1-Math8、R1-R8），代理到mathRefWaveManage.getChannelLabel(chNo)
     * 
     * 【关键代码行注释】
     */
    public String getChannelLabel(int chNo) { // 获取通道标签方法，参数为通道号，返回标签文本，1
        if (TChan.isChan(chNo)) { // 判断是否为物理通道（Ch1-Ch8），使用TChan.isChan()方法，1
            return channelWaveManage.getChannelLabel(chNo); // 物理通道，代理到channelWaveManage.getChannelLabel()方法获取标签，1
        } else { // 不是物理通道，为数学或参考通道，1
            return mathRefWaveManage.getChannelLabel(chNo); // 数学/参考通道，代理到mathRefWaveManage.getChannelLabel()方法获取标签，1
        } // 结束if-else判断，1
    } // 结束getChannelLabel方法，1

    /**
     * 获取通道标签矩形区域
     * 
     * 【功能说明】
     * 获取指定通道标签的矩形显示区域，根据通道类型选择对应管理器
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：Ch1-Ch8、Math1-Math8、R1-R8
     * 
     * 【返回值】
     * @return 标签矩形区域（Rect）
     * 
     * 【调用时机】
     * - 需要检测标签点击时调用
     * - 需要获取标签显示位置时调用
     * 
     * 【策略模式】
     * - 使用TChan.isChan(chNo)判断是否为物理通道
     * - 如果是物理通道（Ch1-Ch8），代理到channelWaveManage.getLabelRect(chNo)
     * - 如果不是物理通道（Math1-Math8、R1-R8），代理到mathRefWaveManage.getLabelRect(chNo)
     * 
     * 【关键代码行注释】
     */
    public Rect getLabelRect(int chNo) { // 获取通道标签矩形区域方法，参数为通道号，返回标签矩形区域，1
        if (TChan.isChan(chNo)) { // 判断是否为物理通道（Ch1-Ch8），使用TChan.isChan()方法，1
            return channelWaveManage.getLabelRect(chNo); // 物理通道，代理到channelWaveManage.getLabelRect()方法获取标签矩形，1
        } else { // 不是物理通道，为数学或参考通道，1
            return mathRefWaveManage.getLabelRect(chNo); // 数学/参考通道，代理到mathRefWaveManage.getLabelRect()方法获取标签矩形，1
        } // 结束if-else判断，1
    } // 结束getLabelRect方法，1

    /**
     * 设置偏移量 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 设置波形显示区域的垂直偏移量，代理到选中通道对应的管理器
     * 
     * 【参数说明】
     * @param offsetY 垂直偏移量（像素）
     *                - 单位：像素
     *                - 正值：波形向下偏移
     *                - 负值：波形向上偏移
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 用户滚动波形显示区域时调用
     * - 系统自动调整显示区域时调用
     * 
     * 【策略模式】
     * - 判断哪个管理器有选中的通道
     * - 如果channelWaveManage.isSelected() != -1，代理到channelWaveManage.setOffsetY(offsetY)
     * - 如果mathRefWaveManage.isSelected() != -1，代理到mathRefWaveManage.setOffsetY(offsetY)
     * - 只有一个管理器会有选中通道
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void setOffsetY(int offsetY) { // 设置偏移量方法，参数为垂直偏移量（像素），1
        if (channelWaveManage.isSelected() != -1) { // 判断通道波形管理器是否有选中的通道，-1表示未选中，1
            channelWaveManage.setOffsetY(offsetY); // 通道波形管理器有选中通道，代理到channelWaveManage.setOffsetY()方法设置偏移量，1
        } else if (mathRefWaveManage.isSelected() != -1) { // 判断数学参考波形管理器是否有选中的通道，-1表示未选中，1
            mathRefWaveManage.setOffsetY(offsetY); // 数学参考波形管理器有选中通道，代理到mathRefWaveManage.setOffsetY()方法设置偏移量，1
        } // 结束if-else判断，1
    } // 结束setOffsetY方法，1

    /**
     * 设置通道位置 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 设置指定通道的垂直位置，根据通道类型选择对应管理器
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：Ch1-Ch8、Math1-Math8、R1-R8
     * @param positionY 通道垂直位置（像素）
     *                   - 单位：像素
     *                   - YT模式下所有通道控制Y轴位置
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 用户拖动通道波形调整位置时调用
     * - 系统初始化时设置默认位置
     * - 从缓存恢复通道位置时调用
     * 
     * 【策略模式】
     * - 使用TChan.isChan(chNo)判断是否为物理通道
     * - 如果是物理通道（Ch1-Ch8），代理到channelWaveManage.setY(chNo, positionY)
     * - 使用TChan.isMathToR8(chNo)判断是否为数学/参考通道
     * - 如果是数学/参考通道（Math1-Math8、R1-R8），代理到mathRefWaveManage.setY(chNo, positionY)
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void setPositionY(int chNo, double positionY) { // 设置通道位置方法，参数为通道号和位置值，1
        if (TChan.isChan(chNo)) { // 判断是否为物理通道（Ch1-Ch8），使用TChan.isChan()方法，1
            channelWaveManage.setY(chNo, positionY); // 物理通道，代理到channelWaveManage.setY()方法设置Y位置，1
        } else if (TChan.isMathToR8(chNo)) { // 判断是否为数学/参考通道（Math1-Math8、R1-R8），使用TChan.isMathToR8()方法，1
            mathRefWaveManage.setY(chNo, positionY); // 数学/参考通道，代理到mathRefWaveManage.setY()方法设置Y位置，1
        } // 结束if-else判断，1
    } // 结束setPositionY方法，1

    /**
     * 从EventBus设置通道位置
     * 
     * 【功能说明】
     * 从EventBus接收位置更新，设置指定通道的位置，根据通道类型选择对应管理器
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：Ch1-Ch8、Math1-Math8、R1-R8
     * @param positionY 位置值（像素）
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - EventBus接收到位置更新事件时调用
     * - 外部模块通过EventBus更新通道位置时调用
     * 
     * 【策略模式】
     * - 使用TChan.isChan(chNo)判断是否为物理通道
     * - 如果是物理通道（Ch1-Ch8），代理到channelWaveManage.setYFromEventBus(chNo, positionY)
     * - 使用TChan.isMathToR8(chNo)判断是否为数学/参考通道
     * - 如果是数学/参考通道（Math1-Math8、R1-R8），代理到mathRefWaveManage.setYFromEventBus(chNo, positionY)
     * 
     * 【与setPositionY的区别】
     * - setPositionY：普通设置位置方法
     * - setPositionYFromEventBus：从EventBus设置位置方法
     * - 区别在于EventBus设置会触发移动事件，标记isFromEventBus=true
     * 
     * 【关键代码行注释】
     */
    public void setPositionYFromEventBus(int chNo, double positionY) { // 从EventBus设置通道位置方法，参数为通道号和位置值，1
        if (TChan.isChan(chNo)) { // 判断是否为物理通道（Ch1-Ch8），使用TChan.isChan()方法，1
            channelWaveManage.setYFromEventBus(chNo, positionY); // 物理通道，代理到channelWaveManage.setYFromEventBus()方法从EventBus设置Y位置，1
        } else if (TChan.isMathToR8(chNo)) { // 判断是否为数学/参考通道（Math1-Math8、R1-R8），使用TChan.isMathToR8()方法，1
            mathRefWaveManage.setYFromEventBus(chNo, positionY); // 数学/参考通道，代理到mathRefWaveManage.setYFromEventBus()方法从EventBus设置Y位置，1
        } // 结束if-else判断，1
    } // 结束setPositionYFromEventBus方法，1

    /**
     * 获取通道位置 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 获取指定通道的垂直位置，根据通道类型选择对应管理器
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：Ch1-Ch8、Math1-Math8、R1-R8
     * 
     * 【返回值】
     * @return 通道垂直位置（像素）
     *         - 如果通道不存在返回0
     * 
     * 【调用时机】
     * - 外部模块获取通道位置时调用
     * - 保存通道位置到缓存时调用
     * 
     * 【策略模式】
     * - 使用TChan.isChan(chNo)判断是否为物理通道
     * - 如果是物理通道（Ch1-Ch8），代理到channelWaveManage.getY(chNo)
     * - 使用TChan.isMathToR8(chNo)判断是否为数学/参考通道
     * - 如果是数学/参考通道（Math1-Math8、R1-R8），代理到mathRefWaveManage.getY(chNo)
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public double getPositionY(int chNo) { // 获取通道位置方法，参数为通道号，返回通道位置值，1
        if (TChan.isChan(chNo)) { // 判断是否为物理通道（Ch1-Ch8），使用TChan.isChan()方法，1
            return channelWaveManage.getY(chNo); // 物理通道，代理到channelWaveManage.getY()方法获取Y位置，1
        } else if (TChan.isMathToR8(chNo)) { // 判断是否为数学/参考通道（Math1-Math8、R1-R8），使用TChan.isMathToR8()方法，1
            return mathRefWaveManage.getY(chNo); // 数学/参考通道，代理到mathRefWaveManage.getY()方法获取Y位置，1
        } // 结束if-else判断，1
        return 0; // 通道不存在，返回0，1
    } // 结束getPositionY方法，1

    /**
     * 获取通道UI位置
     * 
     * 【功能说明】
     * 获取指定通道的UI垂直位置（经过精度转换），根据通道类型选择对应管理器
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：Ch1-Ch8、Math1-Math8、R1-R8
     * 
     * 【返回值】
     * @return 通道UI垂直位置（经过精度转换）
     *         - 如果通道不存在返回0
     * 
     * 【调用时机】
     * - 需要获取通道的UI显示位置时调用
     * - UI更新时调用
     * 
     * 【策略模式】
     * - 使用TChan.isChan(chNo)判断是否为物理通道
     * - 如果是物理通道（Ch1-Ch8），代理到channelWaveManage.getUIY(chNo)
     * - 使用TChan.isMathToR8(chNo)判断是否为数学/参考通道
     * - 如果是数学/参考通道（Math1-Math8、R1-R8），代理到mathRefWaveManage.getUIY(chNo)
     * 
     * 【关键代码行注释】
     */
    public double getUIPositionY(int chNo) { // 获取通道UI位置方法，参数为通道号，返回UI位置值，1
        if (TChan.isChan(chNo)) { // 判断是否为物理通道（Ch1-Ch8），使用TChan.isChan()方法，1
            return channelWaveManage.getUIY(chNo); // 物理通道，代理到channelWaveManage.getUIY()方法获取UI位置，1
        } else if (TChan.isMathToR8(chNo)) { // 判断是否为数学/参考通道（Math1-Math8、R1-R8），使用TChan.isMathToR8()方法，1
            return mathRefWaveManage.getUIY(chNo); // 数学/参考通道，代理到mathRefWaveManage.getUIY()方法获取UI位置，1
        } // 结束if-else判断，1
        return 0; // 通道不存在，返回0，1
    } // 结束getUIPositionY方法，1

    /**
     * 设置通道中心位置 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 将指定通道的波形垂直位置设置为显示区域的中心位置（50%位置）
     * 根据通道类型选择对应管理器
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：Ch1-Ch8、Math1-Math8、R1-R8
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 用户点击"50%"按钮时调用
     * - 恢复通道到中心位置时调用
     * 
     * 【实现细节】
     * 1. 计算中心位置：波形区域高度的一半
     * 2. 使用TChan.isChan(chNo)判断是否为物理通道
     * 3. 如果是物理通道且可见，代理到channelWaveManage.setY(chNo, centerY)
     * 4. 使用TChan.isMathToR8(chNo)判断是否为数学/参考通道
     * 5. 如果是数学/参考通道且可见，代理到mathRefWaveManage.setY(chNo, centerY)
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void setCenterChY(int chNo) { // 设置通道中心位置方法，参数为通道号，1
        //if (chNo != IWave.Ch1 && chNo != IWave.Ch2 && chNo != IWave.Ch3 && chNo != IWave.Ch4) { // 注释掉的代码，可能是之前的通道范围判断，1
        //    return; // 注释掉的代码，可能是之前的返回逻辑，1
        //} // 注释掉的代码，结束if判断，1
        int centerY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) / 2; // 计算中心位置，波形区域高度的一半，从GlobalVar获取高度，1
        Logger.e(TAG, "WaveManage_YT setCenterY= " + centerY); // 输出日志，记录中心位置值，用于调试，1
        if (TChan.isChan(chNo)) { // 判断是否为物理通道（Ch1-Ch8），使用TChan.isChan()方法，1
            if (channelWaveManage.isVisible(chNo)) { // 判断物理通道是否可见，使用channelWaveManage.isVisible()方法，1
                channelWaveManage.setY(chNo, centerY); // 物理通道可见，代理到channelWaveManage.setY()方法设置Y位置为中心位置，1
            } // 结束可见性判断，1
        } // 结束物理通道判断，1
        else if(TChan.isMathToR8(chNo)) { // 判断是否为数学/参考通道（Math1-Math8、R1-R8），使用TChan.isMathToR8()方法，1
            if (mathRefWaveManage.isVisible(chNo)) { // 判断数学/参考通道是否可见，使用mathRefWaveManage.isVisible()方法，1
                mathRefWaveManage.setY(chNo, centerY); // 数学/参考通道可见，代理到mathRefWaveManage.setY()方法设置Y位置为中心位置，1
            } // 结束可见性判断，1
        } // 结束数学/参考通道判断，1
    } // 结束setCenterChY方法，1


    /**
     * 清除混合模式
     * 用于清除画布内容（已废弃）
     */
    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR); // 清除混合模式，用于清除画布内容（已废弃），1

    /**
     * 源混合模式
     * 用于绘制源图像（已废弃）
     */
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC); // 源混合模式，用于绘制源图像（已废弃），1

    /**
     * 设置波形层次（已废弃）
     * 
     * 【功能说明】
     * 根据选择的通道不同，确定波形通道在上，还是数据参考在上（已废弃）
     * 
     * 【调用时机】
     * - 已废弃，不再调用
     * 
     * 【注释说明】
     * - 方法体内的代码已注释，表示已废弃
     * - 原实现使用PorterDuff混合模式设置波形层次
     * - 现在绘制顺序在draw(ICanvasGL canvas)方法中直接控制
     */
    /**
     * 设置波形层次
     * 根据选择的通道不同，确定波形通道在上，还是数据参考在上。
     */
    private void setWaveLayer() { // 设置波形层次方法，已废弃，根据选择的通道不同确定波形层次，1
//        synchronized (bmp) { // 已废弃：同步块，保护位图操作，1
//            paint.setXfermode(clearMode); // 已废弃：设置清除混合模式，1
//            mCanvas.drawPaint(paint); // 已废弃：清除画布，1
//            paint.setXfermode(srcMode); // 已废弃：设置源混合模式，1
//            if (channelWaveManage.isSelected() != -1) { // 已废弃：判断是否有选中的通道波形，1
//                synchronized (mathRefWaveManage.getWavesBitmap()) { // 已废弃：同步数学参考波形位图，1
//                    mCanvas.drawBitmap(mathRefWaveManage.getWavesBitmap(), 0, 0, paint); // 已废弃：绘制数学参考波形位图，1
//                } // 已废弃：结束同步块，1
//                synchronized (channelWaveManage.getWavesBitmap()) { // 已废弃：同步通道波形位图，1
//                    mCanvas.drawBitmap(channelWaveManage.getWavesBitmap(), 0, 0, paint); // 已废弃：绘制通道波形位图，1
//                } // 已废弃：结束同步块，1
//            } else { // 已废弃：没有选中通道波形，1
//                synchronized (channelWaveManage.getWavesBitmap()) { // 已废弃：同步通道波形位图，1
//                    mCanvas.drawBitmap(channelWaveManage.getWavesBitmap(), 0, 0, paint); // 已废弃：绘制通道波形位图，1
//                } // 已废弃：结束同步块，1
//                synchronized (mathRefWaveManage.getWavesBitmap()) { // 已废弃：同步数学参考波形位图，1
//                    mCanvas.drawBitmap(mathRefWaveManage.getWavesBitmap(), 0, 0, paint); // 已废弃：绘制数学参考波形位图，1
//                } // 已废弃：结束同步块，1
//            } // 已废弃：结束if-else判断，1
//
//        } // 已废弃：结束同步块，1
    } // 结束setWaveLayer方法，已废弃，1

    /**
     * 设置选中通道 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 设置指定通道为选中状态，代理到两个管理器
     * 同时更新测量和光标管理的通道颜色
     * 
     * 【参数说明】
     * @param chNO 通道号，取值范围：Ch1-Ch8、Math1-Math8、R1-R8
     *             - 注意：参数名为chNO（大写O），不是chNo（小写o）
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 外部代码需要选中特定通道时调用
     * - 工作模式切换时恢复选中状态
     * - 系统初始化时设置默认选中通道
     * 
     * 【代理机制】
     * - 直接调用channelWaveManage.setSelectCursor(chNO)
     * - 直接调用mathRefWaveManage.setSelectCursor(chNO)
     * - 两个管理器都会尝试设置选中状态，只有对应类型的通道会生效
     * 
     * 【额外操作】
     * - 更新MeasureManage的所有测量通道颜色
     * - 如果光标自动切换通道开启，更新光标和测量的通道颜色
     * 
     * 【关键代码行注释】
     */
    /***
     * 设置通道号
     * @param chNO
     */
    @Override // 标记为接口方法实现，1
    public void setSelectCursor(int chNO) { // 设置选中通道方法，参数为通道号，1
        channelWaveManage.setSelectCursor(chNO); // 代理到channelWaveManage.setSelectCursor()方法，设置通道选中状态，1
        mathRefWaveManage.setSelectCursor(chNO); // 代理到mathRefWaveManage.setSelectCursor()方法，设置数学参考波形选中状态，1
        // TODO: 2023-9-7 光标测量值是否切换 // TODO注释：光标测量值是否切换，待实现，1
        MeasureManage.getInstance().setAllMeasureChannelColor(chNO); // 更新所有测量通道颜色，调用MeasureManage的setAllMeasureChannelColor方法，1
        if (CursorManage.isAutoSwitchChannel()) { // 判断光标是否自动切换通道，使用CursorManage.isAutoSwitchChannel()方法，1
            CursorManage.getInstance().setCursorChannelColor(chNO); // 自动切换开启，更新光标通道颜色，调用CursorManage的setCursorChannelColor方法，1
            MeasureManage.getInstance().setCursorChannelColor(chNO); // 自动切换开启，更新光标测量通道颜色，调用MeasureManage的setCursorChannelColor方法，1
        } // 结束自动切换判断，1
    } // 结束setSelectCursor方法，1

    /**
     * 选择光标 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 根据点击坐标判断点击在哪个通道波形区域内
     * 首先在通道波形选择器中选择，再从数学参考波形选择器中选择，最后从图片中进行选择
     * 
     * 【参数说明】
     * @param x 点击X坐标（像素）
     *          - 单位：像素
     *          - 坐标系：相对于波形显示区域左上角
     * @param y 点击Y坐标（像素）
     *          - 单位：像素
     *          - 坐标系：相对于波形显示区域左上角
     * 
     * 【返回值】
     * @return 选中的通道号
     *         - Ch1-Ch8：点击在物理通道波形区域内
     *         - Math1-Math8、R1-R8：点击在数学/参考波形区域内
     *         - -1：点击不在任何通道波形区域内
     * 
     * 【调用时机】
     * - 用户点击波形显示区域时调用
     * - 判断是否选中通道时调用
     * 
     * 【选择流程】
     * 1. 初始化选中索引为-1
     * 2. 在通道波形选择器中选择（channelWaveManage.selectCursor(x, y))
     * 3. 如果通道波形选择器未选中，在数学参考波形选择器中选择（mathRefWaveManage.selectCursor(x, y))
     * 4. 如果数学参考波形选择器未选中，从图片中进行选择（selectCursorByBmp(x, y))
     * 5. 返回选中的通道号
     * 
     * 【关键代码行注释】
     */
    /***
     * 在波形通道选择器中选择前，先判断通道选择器是否打开。
     * 首先在波形通道选择器中选择，再从图片中进行选择。
     *
     * @param x
     * @param y
     * @return 返回通道号  -1为没有选择
     */
    @Override // 标记为接口方法实现，1
    public int selectCursor(int x, int y) { // 选择光标方法，参数为点击坐标，返回点击的通道号，1
        int selectIndex = -1; // 初始化选中索引为-1，表示未选中，1
        selectIndex = channelWaveManage.selectCursor(x, y); // 在通道波形选择器中选择，调用channelWaveManage.selectCursor()方法，1
        if (selectIndex < 0) selectIndex = mathRefWaveManage.selectCursor(x, y); // 如果通道波形选择器未选中（返回值<0），在数学参考波形选择器中选择，1
        //if (selectIndex < 0) selectIndex = this.selectCursorTobmp(x, y); // 注释掉的代码，可能是之前的实现方式，1
        if (selectIndex < 0) selectIndex = this.selectCursorByBmp(x, y); // 如果数学参考波形选择器未选中（返回值<0），从图片中进行选择，调用selectCursorByBmp方法，1
        //if (selectIndex != -1) setWaveLayer(); // 注释掉的代码，可能是之前的波形层次设置，已废弃，1

        return selectIndex; // 返回选中的通道号，-1表示未选中，1
    } // 结束selectCursor方法，1

    /**
     * 选择光标（排除当前通道）
     * 
     * 【功能说明】
     * 根据点击坐标判断点击在哪个通道波形区域内，支持排除当前通道
     * 
     * 【参数说明】
     * @param x 点击X坐标（像素）
     * @param y 点击Y坐标（像素）
     * @param curChan 当前通道号，用于排除当前通道的优先选择
     * 
     * 【返回值】
     * @return 选中的通道号
     *         - -1：点击不在任何通道波形区域内
     * 
     * 【调用时机】
     * - 用户点击波形区域选择通道，需要排除当前通道时调用
     * 
     * 【选择流程】
     * 1. 初始化选中索引为-1
     * 2. 在通道波形选择器中选择（channelWaveManage.selectCursor(x, y, curChan))
     * 3. 如果通道波形选择器未选中，在数学参考波形选择器中选择（mathRefWaveManage.selectCursor(x, y, curChan))
     * 4. 如果数学参考波形选择器未选中，从图片中进行选择（selectCursorByBmp(x, y, curChan))
     * 5. 返回选中的通道号
     * 
     * 【关键代码行注释】
     */
    public int selectCursor(int x,int y,int curChan){ // 选择光标方法，三参数版本，支持排除当前通道，1
        int selectIndex = -1; // 初始化选中索引为-1，表示未选中，1
        selectIndex = channelWaveManage.selectCursor(x, y,curChan); // 在通道波形选择器中选择，排除当前通道，调用channelWaveManage.selectCursor()方法，1
        if (selectIndex < 0) selectIndex = mathRefWaveManage.selectCursor(x, y,curChan); // 如果通道波形选择器未选中，在数学参考波形选择器中选择，排除当前通道，1
        if (selectIndex < 0) selectIndex = this.selectCursorByBmp(x, y,curChan); // 如果数学参考波形选择器未选中，从图片中进行选择，排除当前通道，调用selectCursorByBmp方法，1
        return selectIndex; // 返回选中的通道号，-1表示未选中，1
    } // 结束selectCursor方法，三参数版本，1

    /**
     * 移动像素 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 移动当前选中通道波形的像素距离，代理到两个管理器
     * 
     * 【参数说明】
     * @param px 移动的像素距离
     *           - 单位：像素
     *           - YT模式下所有通道垂直移动
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 用户拖动波形时调用（每次拖动事件）
     * - 系统自动调整波形位置时调用
     * 
     * 【代理机制】
     * - 直接调用channelWaveManage.movePix(px)
     * - 直接调用mathRefWaveManage.movePix(px)
     * - 两个管理器都会尝试移动，只有选中通道的管理器会生效
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void movePix(int px) { // 移动像素方法，参数为像素偏移量，1
        channelWaveManage.movePix(px); // 代理到channelWaveManage.movePix()方法，移动通道波形，1
        mathRefWaveManage.movePix(px); // 代理到mathRefWaveManage.movePix()方法，移动数学参考波形，1
    } // 结束movePix方法，1

    /**
     * 获取当前选中通道 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 获取当前选中的通道号，根据哪个管理器有选中通道返回
     * 
     * 【返回值】
     * @return 当前选中的通道号
     *         - Ch1-Ch8：选中物理通道
     *         - Math1-Math8、R1-R8：选中数学/参考通道
     *         - -1：未选中任何通道
     * 
     * 【调用时机】
     * - 外部模块获取当前选中通道时调用
     * - 判断当前操作通道时调用
     * 
     * 【策略模式】
     * - 判断channelWaveManage.isSelected()是否不为-1
     * - 如果通道波形管理器有选中通道，返回channelWaveManage.isSelected()
     * - 否则返回mathRefWaveManage.isSelected()
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public int getCurCh() { // 获取当前选中通道方法，无参数，返回当前选中的通道号，1

        return channelWaveManage.isSelected() == -1 ? mathRefWaveManage.isSelected() : channelWaveManage.isSelected(); // 如果通道波形管理器未选中，返回数学参考波形管理器的选中通道；否则返回通道波形管理器的选中通道，1
    } // 结束getCurCh方法，1

    /**
     * 设置波形纹理视图
     * 
     * 【功能说明】
     * 设置波形纹理视图，用于获取点阵数据支持点选功能
     * 
     * 【参数说明】
     * @param waveTextureView 波形纹理视图对象
     *                         - 类型：PreviewTextureView
     *                         - 用途：提供点阵数据用于点选功能
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 初始化点选功能时调用
     * - WaveZoneDisplay_YT设置波形纹理视图时调用
     * 
     * 【关键代码行注释】
     */
    public void setWaveTextureView(PreviewTextureView waveTextureView){ // 设置波形纹理视图方法，参数为波形纹理视图对象，1
        this.waveTextureView=waveTextureView; // 设置waveTextureView成员变量，用于获取点阵数据，1
    } // 结束setWaveTextureView方法，1

    /**
     * 从bmp中返回选择的通道号（点选功能）
     * 
     * 【功能说明】
     * 通过点阵数据颜色匹配选择通道，支持从波形图像直接选择通道
     * 
     * 【参数说明】
     * @param x 点击的位置 X（像素）
     * @param y 点击的位置 Y（像素）
     * 
     * 【返回值】
     * @return 通道号，返回-1说明没有选择
     *         - Ch1-Ch8：选中物理通道
     *         - Math1-Math8、R1-R8：选中数学/参考通道
     *         - -1：未选中任何通道
     * 
     * 【调用时机】
     * - selectCursor()方法中调用，作为最后的选择方式
     * - 用户点击波形图像选择通道时调用
     * 
     * 【实现细节】
     * 1. 判断点选功能是否开启（clickSelectEnable）
     * 2. 如果点选功能关闭，直接返回-1
     * 3. 从waveTextureView获取点阵数据（ByteBuffer）
     * 4. 如果点阵数据和颜色HSV数组存在，进行颜色匹配
     * 5. 计算YTZOOM模式的偏移量T
     * 6. 调用selectCursorTobmp进行颜色匹配选择通道
     * 7. 如果选中RefActive或MathActive，返回当前选中通道
     * 8. 根据选中通道类型设置对应管理器的选中状态
     * 
     * 【关键代码行注释】
     */
    /**
     *  从bmp中返回选择的通道号
     * @param x 点击的位置 X
     * @param y  点击的位置 Y
     * @return  返回通道号  返回-1说明没有选择
     */
    private int selectCursorByBmp(int x,int y){ // 从bmp中返回选择的通道号方法，参数为点击坐标，返回通道号，1
        if (!clickSelectEnable) return -1; // 判断点选功能是否开启，如果关闭直接返回-1，1
        int selectIndex = -1; // 初始化选中索引为-1，表示未选中，1

        ByteBuffer byteBuffer = this.waveTextureView.getBuffer(); // 从waveTextureView获取点阵数据（ByteBuffer），1
        if(byteBuffer != null && chColorHSV != null){ // 判断点阵数据和颜色HSV数组是否存在，1
            int stride = waveTextureView.getStride(); // 获取stride（行宽度），用于计算像素位置，1
            int T = 0; // 定义偏移量T，用于YTZOOM模式，初始化为0，1
            if(WorkModeManage.getInstance().getmWorkMode()==IWorkMode.WorkMode_YTZOOM){ // 判断当前工作模式是否为YTZOOM模式，1
                T = GlobalVar.get().getRectZoom().height(); // 如果是YTZOOM模式，获取缩放区域高度作为偏移量T，1
            } // 结束工作模式判断，1
            selectIndex = selectCursorTobmp(byteBuffer,ScopeBase.getWidth(),ScopeBase.getNewHeight(),stride,x, y+T,T); // 调用selectCursorTobmp进行颜色匹配选择通道，传入点阵数据、宽度、高度、stride、点击坐标和偏移量，1
            Logger.d(TAG,"selectIndex:" + selectIndex); // 输出日志，记录选中的通道号，用于调试，1
        } // 结束点阵数据判断，1

        if (selectIndex == TChan.RefActive || selectIndex == TChan.MathActive) selectIndex = getCurCh(); // 如果选中RefActive或MathActive，返回当前选中通道，1

        if (TChan.isChan(selectIndex)) channelWaveManage.setSelectCursor(selectIndex); // 如果选中物理通道，设置channelWaveManage的选中状态，1
        if (TChan.isMathToR8(selectIndex)) mathRefWaveManage.setSelectCursor(selectIndex); // 如果选中数学/参考通道，设置mathRefWaveManage的选中状态，1
        return selectIndex; // 返回选中的通道号，-1表示未选中，1
    } // 结束selectCursorByBmp方法，1

    /**
