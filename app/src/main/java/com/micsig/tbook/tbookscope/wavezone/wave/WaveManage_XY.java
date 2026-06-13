package com.micsig.tbook.tbookscope.wavezone.wave; // 波形显示区域-波形子模块包，包含示波器波形显示的核心组件，1

import android.graphics.Bitmap; // Android位图类，用于创建和管理波形显示区域的位图，1
import android.graphics.Canvas; // Android画布类，用于在位图上绘制图形，1
import android.graphics.Paint; // Android画笔类，用于设置绘制样式（颜色、字体等），1
import android.graphics.drawable.BitmapDrawable; // Android位图drawable类，用于从资源加载位图，1

import com.chillingvan.canvasgl.ICanvasGL; // OpenGL画布接口，用于OpenGL高性能渲染绘制，1
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量类，获取波形显示区域的尺寸配置，1
import com.micsig.tbook.tbookscope.util.App; // 应用工具类，获取应用上下文和资源，1
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 工作模式接口，定义工作模式常量和切换方法，1
import com.micsig.tbook.ui.wavezone.IWave; // 波形显示接口，定义波形的基本操作和事件，1
import com.micsig.tbook.ui.wavezone.TChan; // 通道常量类，定义通道编号常量（Ch1、Ch2等），1

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              WaveManage_XY - XY模式波形管理类                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   - 所属模块：wavezone.wave（波形显示区域-波形子模块）                        ║
 * ║   - 核心职责：XY模式下波形显示区域的顶层管理类，封装通道波形管理器            ║
 * ║   - 架构层级：管理层组件，实现IWorkMode和IWaveManage接口                     ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. XY模式波形区域管理：管理XY模式下的波形显示区域位图                      ║
 * ║   2. 通道波形管理器封装：封装ChannelWaveManage_XY，代理所有波形管理操作      ║
 * ║   3. 工作模式切换：支持XY模式的切换和初始化                                  ║
 * ║   4. 波形绘制：绘制XY模式下的波形显示区域背景和通道波形                      ║
 * ║   5. 尺寸调整：支持波形显示区域尺寸的动态调整                                ║
 * ║                                                                              ║
 * ║ 【架构图】                                                                   ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │            WaveZoneDisplay_XY（XY模式显示组件）                      │   ║
 * ║   │                   (XY模式显示核心)                                   │   ║
 * ║   │  - 管理XY模式显示区域                                                │   ║
 * ║   │  - 创建WaveManage_XY实例                                             │   ║
 * ║   │  - 处理用户交互事件                                                  │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                              ↓ 创建和管理                                   ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │              WaveManage_XY（XY模式波形管理类）                        │   ║
 * ║   │                (XY模式波形管理核心) ← 本类                            │   ║
 * ║   │  ┌──────────────────────────────────────────────────────────────┐  │   ║
 * ║   │  │  核心属性                                                    │  │   ║
 * ║   │  │  - bmp: 波形显示区域位图                                    │  │   ║
 * ║   │  │  - mCanvas: 绘制画布                                        │  │   ║
 * ║   │  │  - paint: 绘制画笔                                          │  │   ║
 * ║   │  │  - channelWaveManage_xy: 通道波形管理器                     │  │   ║
 * ║   │  │  - resBmp: 通道指示器位图资源                               │  │   ║
 * ║   │  └──────────────────────────────────────────────────────────────┘  │   ║
 * ║   │  ┌──────────────────────────────────────────────────────────────┐  │   ║
 * ║   │  │  XY模式波形管理流程                                          │  │   ║
 * ║   │  │  1. 初始化：创建波形区域位图和通道管理器                     │  │   ║
 * ║   │  │  2. 绘制：绘制波形区域背景 + 通道波形                        │  │   ║
 * ║   │  │  3. 交互：代理通道选中、移动、位置设置                      │  │   ║
 * ║   │  │  4. 模式切换：更新通道管理器的工作模式                      │  │   ║
 * ║   │  └──────────────────────────────────────────────────────────────┘  │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                              ↓ 创建和管理                                   ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │          ChannelWaveManage_XY（XY模式通道波形管理类）                │   ║
 * ║   │                (XY模式双通道波形管理核心)                            │   ║
 * ║   │  - 管理Ch1垂直波形（控制X轴）                                        │   ║
 * ║   │  - 管理Ch2水平波形（控制Y轴）                                        │   ║
 * ║   │  - 实现通道选中、移动、位置设置                                      │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                              ↓ 实现接口                                     ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │              IWorkMode + IWaveManage（接口层）                        │   ║
 * ║   │  - IWorkMode: 工作模式接口，定义模式切换方法                        │   ║
 * ║   │  - IWaveManage: 波形管理接口，定义波形管理方法                      │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【XY模式波形管理流程】                                                       ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                    XY模式初始化流程                                  │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║          ↓                                                                  ║
 * ║   ┌─────────────┐                                                           ║
 * ║   │ 创建XY模式  │                                                           ║
 * ║   │ 波形管理器  │                                                           ║
 * ║   │ 构造函数    │                                                           ║
 * ║   └──────┬──────┘                                                           ║
 * ║          ↓                                                                  ║
 * ║   ┌─────────────┐                                                           ║
 * ║   │ 加载通道    │                                                           ║
 * ║   │ 指示器位图  │                                                           ║
 * ║   │ (resBmp)    │                                                           ║
 * ║   └──────┬──────┘                                                           ║
 * ║          ↓                                                                  ║
 * ║   ┌─────────────┐                                                           ║
 * ║   │ 创建波形    │                                                           ║
 * ║   │ 区域位图    │                                                           ║
 * ║   │ (bmp)       │                                                           ║
 * ║   └──────┬──────┘                                                           ║
 * ║          ↓                                                                  ║
 * ║   ┌─────────────┐                                                           ║
 * ║   │ 创建通道    │                                                           ║
 * ║   │ 波形管理器  │                                                           ║
 * ║   │ (ChannelWave│                                                           ║
 * ║   │ Manage_XY)  │                                                           ║
 * ║   └──────┬──────┘                                                           ║
 * ║          ↓                                                                  ║
 * ║   ┌─────────────────────────────────────┐                                   ║
 * ║   │          初始化完成                  │                                   ║
 * ║   │  WaveManage_XY封装通道管理器         │                                   ║
 * ║   │  所有操作代理到ChannelWaveManage_XY  │                                   ║
 * ║   └─────────────────────────────────────┘                                   ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   实现接口：                                                                 ║
 * ║   - IWorkMode：工作模式接口，定义工作模式切换方法（switchWorkMode）          ║
 * ║   - IWaveManage：波形管理接口，定义波形管理方法（draw、setVisible等）        ║
 * ║                                                                              ║
 * ║   管理对象：                                                                 ║
 * ║   - ChannelWaveManage_XY：XY模式通道波形管理类，管理Ch1和Ch2通道波形         ║
 * ║   - Bitmap：波形显示区域位图，用于绘制背景                                   ║
 * ║   - Canvas：绘制画布，用于在位图上绘制                                       ║
 * ║   - Paint：绘制画笔，用于设置绘制样式                                        ║
 * ║                                                                              ║
 * ║   依赖服务：                                                                 ║
 * ║   - GlobalVar：全局变量类，获取波形区域尺寸                                  ║
 * ║   - App：应用工具类，获取应用上下文和资源                                    ║
 * ║   - TChan：通道常量类，定义通道编号常量                                      ║
 * ║                                                                              ║
 * ║   被管理类：                                                                 ║
 * ║   - WaveZoneDisplay_XY：XY模式显示组件，创建和管理WaveManage_XY实例         ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   1. 代理模式（Proxy Pattern）：                                            ║
 * ║      - WaveManage_XY作为ChannelWaveManage_XY的代理                          ║
 * ║      - 所有IWaveManage接口方法都代理到channelWaveManage_xy                  ║
 * ║      - 提供统一的波形管理接口，隐藏内部实现                                  ║
 * ║                                                                              ║
 * ║   2. 门面模式（Facade Pattern）：                                            ║
 * ║      - 提供统一的波形管理接口，简化外部调用                                  ║
 * ║      - 封装复杂的通道波形管理逻辑                                            ║
 * ║      - 外部模块只需调用WaveManage_XY的方法                                  ║
 * ║                                                                              ║
 * ║   3. 组合模式（Composite Pattern）：                                         ║
 * ║      - WaveManage_XY组合了ChannelWaveManage_XY                              ║
 * ║      - 提供统一的波形管理接口                                                ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. XY模式初始化：初始化XY模式下的波形显示区域                              ║
 * ║   2. XY模式波形绘制：绘制XY模式下的波形显示区域和通道波形                    ║
 * ║   3. XY模式通道交互：代理通道选中、移动、位置设置等操作                      ║
 * ║   4. XY模式工作模式切换：切换到XY模式或从XY模式切换                          ║
 * ║   5. XY模式尺寸调整：调整波形显示区域尺寸                                    ║
 * ║                                                                              ║
 * ║ 【XY模式特殊说明】                                                           ║
 * ║   1. 波形显示区域：                                                          ║
 * ║      - XY模式有独立的波形显示区域位图（bmp）                                 ║
 * ║      - 位图尺寸由GlobalVar.get().getWaveZoneWidth_Pix/Height_Pix获取        ║
 * ║      - 位图用于绘制波形区域背景                                              ║
 * ║                                                                              ║
 * ║   2. 通道指示器位图：                                                        ║
 * ║      - resBmp[1][0-5]：Ch1通道的指示器位图（垂直波形）                       ║
 * ║      - resBmp[2][0-5]：Ch2通道的指示器位图（水平波形）                       ║
 * ║      - 位图从资源文件加载（ch1_s_down、ch1_s_left等）                        ║
 * ║                                                                              ║
 * ║   3. 代理机制：                                                              ║
 * ║      - 所有IWaveManage接口方法都代理到channelWaveManage_xy                  ║
 * ║      - setVisible() → channelWaveManage_xy.setVisible()                     ║
 * ║      - setPositionY() → channelWaveManage_xy.setPositionY()                 ║
 * ║      - selectCursor() → channelWaveManage_xy.selectCursor()                 ║
 * ║      - movePix() → channelWaveManage_xy.movePix()                           ║
 * ║                                                                              ║
 * ║   4. 绘制顺序：                                                              ║
 * ║      - 先绘制波形区域背景位图（bmp）                                         ║
 * ║      - 再绘制通道波形（由channelWaveManage_xy绘制）                          ║
 * ║                                                                              ║
 * ║ 【数据流向】                                                                 ║
 * ║   用户操作 → WaveManage_XY → ChannelWaveManage_XY →                         ║
 * ║   ChannelWave/ChannelWaveVertical → DisplayXYService → XY模式显示           ║
 * ║                                                                              ║
 * ║ 【性能考虑】                                                                 ║
 * ║   1. 位图缓存：波形区域位图在构造时创建，避免频繁创建                        ║
 * ║   2. 绘制优化：使用OpenGL（ICanvasGL）进行高性能渲染                         ║
 * ║   3. 同步保护：绘制时使用synchronized保护位图对象                            ║
 * ║                                                                              ║
 * ║ 【注意事项】                                                                 ║
 * ║   1. WaveManage_XY是ChannelWaveManage_XY的代理，所有操作都代理              ║
 * ║   2. 波形区域位图用于绘制背景，不包含通道波形                                ║
 * ║   3. 尺寸调整时需要重新创建位图                                              ║
 * ║   4. XY模式下通道位置映射与YT模式不同                                        ║
 * ║                                                                              ║
 * ║ 【作者】 Created by liwb on 2017/11/6                                        ║
 * ║ 【版本】 1.0                                                                 ║
 * ║ 【参见】 @see IWorkMode                                                      ║
 * ║        @see IWaveManage                                                     ║
 * ║        @see ChannelWaveManage_XY                                            ║
 * ║        @see WaveZoneDisplay_XY                                              ║
 * ║        @see GlobalVar                                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

public class WaveManage_XY implements IWorkMode, IWaveManage { // XY模式波形管理类，实现IWorkMode和IWaveManage接口，作为ChannelWaveManage_XY的代理，1

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 通道指示器位图资源数组
     * 用于存储Ch1和Ch2通道的指示器位图资源
     * 
     * 【数组结构】
     * - resBmp[1][0-5]：Ch1通道的指示器位图（垂直波形）
     * - resBmp[2][0-5]：Ch2通道的指示器位图（水平波形）
     * 
     * 【位图索引说明】
     * - 索引0：选中按下状态（s_down）
     * - 索引1：选中左移状态（s_left）
     * - 索引2：选中正常状态（s）
     * - 索引3：未选中按下状态（n_down）
     * - 索引4：未选中左移状态（n_left）
     * - 索引5：未选中正常状态（n）
     * 
     * 【用途】
     * - 传递给ChannelWaveManage_XY，用于创建通道波形对象
     * - 通道波形对象使用这些位图显示通道指示器
     */
    private Bitmap[][] resBmp = new Bitmap[3][6]; // 通道指示器位图资源数组，第一维为通道号（1=Ch1, 2=Ch2），第二维为状态索引（0-5），1

    /**
     * 初始化通道指示器位图资源
     * 从应用资源加载Ch1和Ch2通道的指示器位图
     * 
     * 【功能说明】
     * 从资源文件加载通道指示器位图，存储到resBmp数组中
     * 
     * 【加载流程】
     * 1. 加载Ch1通道位图（resBmp[1][0-5]）：
     *    - ch1_s_down：选中按下状态
     *    - ch1_s_left：选中左移状态
     *    - ch1_s：选中正常状态
     *    - ch1_n_down：未选中按下状态
     *    - ch1_n_left：未选中左移状态
     *    - ch1_n：未选中正常状态
     * 2. 加载Ch2通道位图（resBmp[2][0-5]）：
     *    - ch2_n：未选中正常状态
     *    - ch2_n_down：未选中按下状态
     *    - ch2_n_up：未选中上移状态
     *    - ch2_s：选中正常状态
     *    - ch2_s_down：选中按下状态
     *    - ch2_s_up：选中上移状态
     * 
     * 【调用时机】
     * - 构造函数中调用，初始化位图资源
     */
    private void initResBmp() { // 初始化通道指示器位图资源方法，从应用资源加载位图，1
        // 加载Ch1通道的指示器位图（垂直波形）
        resBmp[1][0] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.ch1_s_down)).getBitmap(); // 加载Ch1选中按下状态位图，从资源获取BitmapDrawable并转换为Bitmap，1
        resBmp[1][1] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.ch1_s_left)).getBitmap(); // 加载Ch1选中左移状态位图，从资源获取BitmapDrawable并转换为Bitmap，1
        resBmp[1][2] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.ch1_s)).getBitmap(); // 加载Ch1选中正常状态位图，从资源获取BitmapDrawable并转换为Bitmap，1
        resBmp[1][3] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.ch1_n_down)).getBitmap(); // 加载Ch1未选中按下状态位图，从资源获取BitmapDrawable并转换为Bitmap，1
        resBmp[1][4] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.ch1_n_left)).getBitmap(); // 加载Ch1未选中左移状态位图，从资源获取BitmapDrawable并转换为Bitmap，1
        resBmp[1][5] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.ch1_n)).getBitmap(); // 加载Ch1未选中正常状态位图，从资源获取BitmapDrawable并转换为Bitmap，1

        // 加载Ch2通道的指示器位图（水平波形）
        resBmp[2][0] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.ch2_n)).getBitmap(); // 加载Ch2未选中正常状态位图，从资源获取BitmapDrawable并转换为Bitmap，1
        resBmp[2][1] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.ch2_n_down)).getBitmap(); // 加载Ch2未选中按下状态位图，从资源获取BitmapDrawable并转换为Bitmap，1
        resBmp[2][2] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.ch2_n_up)).getBitmap(); // 加载Ch2未选中上移状态位图，从资源获取BitmapDrawable并转换为Bitmap，1
        resBmp[2][3] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.ch2_s)).getBitmap(); // 加载Ch2选中正常状态位图，从资源获取BitmapDrawable并转换为Bitmap，1
        resBmp[2][4] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.ch2_s_down)).getBitmap(); // 加载Ch2选中按下状态位图，从资源获取BitmapDrawable并转换为Bitmap，1
        resBmp[2][5] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.ch2_s_up)).getBitmap(); // 加载Ch2选中上移状态位图，从资源获取BitmapDrawable并转换为Bitmap，1
    } // 结束initResBmp方法，1

    /**
     * XY模式通道波形管理器
     * 管理XY模式下的两个通道波形（Ch1垂直波形和Ch2水平波形）
     * 
     * 【职责说明】
     * - 管理Ch1垂直波形（控制X轴位置）
     * - 管理Ch2水平波形（控制Y轴位置）
     * - 实现通道选中、移动、位置设置等操作
     * - 实现IWorkMode和IWaveManage接口
     * 
     * 【代理关系】
     * - WaveManage_XY作为ChannelWaveManage_XY的代理
     * - 所有IWaveManage接口方法都代理到channelWaveManage_xy
     * 
     * 【创建时机】
     * - 构造函数中创建，传入位图资源和事件监听器
     */
    private ChannelWaveManage_XY channelWaveManage_xy; // XY模式通道波形管理器，管理Ch1和Ch2通道波形，1

    /**
     * 波形显示区域位图
     * 用于绘制XY模式下的波形显示区域背景
     * 
     * 【尺寸说明】
     * - 宽度：GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_XY)
     * - 高度：GlobalVar.get().getWaveZoneHeight_Pix(IWorkMode.WorkMode_XY)
     * - 配置：Bitmap.Config.ARGB_8888（支持透明度）
     * 
     * 【用途】
     * - 绘制波形显示区域背景
     * - 不包含通道波形（通道波形由channelWaveManage_xy绘制）
     * 
     * 【同步保护】
     * - 绘制时使用synchronized(bmp)保护，避免并发问题
     */
    private Bitmap bmp, tempBmp; // 波形显示区域位图，bmp为主位图，tempBmp为临时位图（用于尺寸调整），1

    /**
     * 绘制画布
     * 用于在波形显示区域位图上绘制图形
     * 
     * 【创建时机】
     * - 构造函数中创建，绑定到bmp位图
     * - 尺寸调整时重新创建，绑定到新的bmp位图
     * 
     * 【用途】
     * - 在bmp位图上绘制图形
     * - 绘制波形显示区域背景
     */
    private Canvas mCanvas; // 绘制画布，用于在bmp位图上绘制图形，1

    /**
     * 绘制画笔
     * 用于设置绘制样式（颜色、字体等）
     * 
     * 【用途】
     * - 设置绘制颜色、字体大小等样式
     * - 用于在mCanvas上绘制图形
     */
    private Paint paint; // 绘制画笔，用于设置绘制样式（颜色、字体等），1

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造函数 - 初始化XY模式波形管理器
     * 
     * 【功能说明】
     * 创建XY模式波形管理器，初始化波形显示区域位图和通道波形管理器
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
     * - WaveZoneDisplay_XY初始化XY模式时调用
     * - 切换到XY模式时创建新的管理器实例
     * 
     * 【实现流程】
     * 1. 初始化通道指示器位图资源（initResBmp）
     * 2. 创建波形显示区域位图（bmp）
     *    - 尺寸：GlobalVar获取XY模式波形区域尺寸
     *    - 配置：ARGB_8888（支持透明度）
     * 3. 创建绘制画布（mCanvas），绑定到bmp位图
     * 4. 创建绘制画笔（paint）
     * 5. 创建通道波形管理器（channelWaveManage_xy）
     *    - 传入位图资源和事件监听器
     *    - channelWaveManage_xy负责管理Ch1和Ch2通道波形
     * 
     * 【关键代码行注释】
     */
    public WaveManage_XY(IWave.OnMovingWaveEvent onMovingWaveEvent, IWave.OnSelectChangeEvent onSelectChangeEvent) { // 构造函数，初始化XY模式波形管理器，参数为波形移动事件监听器和选中状态变化事件监听器，1
        initResBmp(); // 初始化通道指示器位图资源，从应用资源加载Ch1和Ch2的指示器位图，1
        bmp = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_XY), // 创建波形显示区域位图，宽度为XY模式波形区域宽度，从GlobalVar获取，1
                GlobalVar.get().getWaveZoneHeight_Pix(IWorkMode.WorkMode_XY), Bitmap.Config.ARGB_8888); // 创建波形显示区域位图，高度为XY模式波形区域高度，配置为ARGB_8888（支持透明度），1
        mCanvas = new Canvas(bmp); // 创建绘制画布，绑定到bmp位图，用于在位图上绘制图形，1
        paint = new Paint(); // 创建绘制画笔，用于设置绘制样式（颜色、字体等），1

        channelWaveManage_xy = new ChannelWaveManage_XY(resBmp, onMovingWaveEvent, onSelectChangeEvent); // 创建XY模式通道波形管理器，传入位图资源和事件监听器，1
    } // 结束构造函数，1


    // ═══════════════════════════════════════════════════════════════════════════════
    // interface IWaveManage - 波形管理接口实现
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 绘制波形 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 绘制XY模式下的波形显示区域背景和通道波形
     * 先绘制波形区域背景位图，再绘制通道波形
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
     * - WaveZoneDisplay_XY渲染波形时调用
     * - 每帧渲染时调用
     * 
     * 【实现细节】
     * 1. 使用synchronized保护bmp位图，避免并发绘制问题
     * 2. 绘制波形显示区域背景位图（bmp）
     *    - 使用canvas.drawBitmap()绘制bmp位图
     *    - 绘制位置：(0, 0)，即波形区域左上角
     * 3. 绘制通道波形（由channelWaveManage_xy绘制）
     *    - 调用channelWaveManage_xy.draw(canvas)
     *    - channelWaveManage_xy负责绘制Ch1和Ch2通道波形
     * 
     * 【绘制顺序说明】
     * - 先绘制波形区域背景（bmp），后绘制通道波形
     * - 这样可以确保通道波形显示在背景之上
     * 
     * 【同步保护说明】
     * - 使用synchronized(bmp)保护位图对象
     * - 避免并发绘制导致的竞争问题
     * - 确保绘制操作的线程安全
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void draw(ICanvasGL canvas) { // 绘制波形方法，参数为OpenGL画布对象，1
        synchronized (bmp) { // 使用synchronized保护bmp位图，避免并发绘制问题，1
            canvas.drawBitmap(bmp, 0, 0); // 绘制波形显示区域背景位图，位置为(0, 0)，即波形区域左上角，1
        } // 结束synchronized同步块，1
        channelWaveManage_xy.draw(canvas); // 绘制通道波形，代理到channelWaveManage_xy.draw()方法，1
    } // 结束draw方法，1

    /**
     * 设置波形区域尺寸变化
     * 
     * 【功能说明】
     * 根据高度变化调整波形显示区域位图的尺寸
     * 重新创建位图和画布
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
     * 1. 计算尺寸比例系数：height / 1040f
     *    - 1040f为基准高度，用于计算比例
     * 2. 计算新的波形区域尺寸：
     *    - 新宽度 = 原宽度 × 比例系数
     *    - 新高度 = 原高度 × 比例系数
     * 3. 重新创建位图（bmp）：
     *    - 尺寸：新宽度、新高度
     *    - 配置：ARGB_8888
     * 4. 重新创建画布（mCanvas），绑定到新位图
     * 
     * 【尺寸计算说明】
     * - 比例系数 = height / 1040f
     * - 1040f为基准高度，可能是设备的默认高度
     * - 新尺寸 = 原尺寸 × 比例系数
     * 
     * 【关键代码行注释】
     */
    public void setWaveChange(int height) { // 设置波形区域尺寸变化方法，参数为新的高度值（像素），1
        double temp = height / 1040f; // 计算尺寸比例系数，height除以基准高度1040f，1
        int waveWidth = (int) Math.round(GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_XY) * temp); // 计算新的波形区域宽度，原宽度乘以比例系数，四舍五入，1
        int waveHeight = (int) Math.round(GlobalVar.get().getWaveZoneHeight_Pix(IWorkMode.WorkMode_XY) * temp); // 计算新的波形区域高度，原高度乘以比例系数，四舍五入，1
        bmp = Bitmap.createBitmap(waveWidth, waveHeight, Bitmap.Config.ARGB_8888); // 重新创建波形显示区域位图，尺寸为新宽度和新高度，配置为ARGB_8888，1
        mCanvas = new Canvas(bmp); // 重新创建绘制画布，绑定到新的bmp位图，1
    } // 结束setWaveChange方法，1


    /**
     * 设置通道可见性 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 设置通道的可见性，代理到channelWaveManage_xy
     * 
     * 【参数说明】
     * @param ChNo 通道号，取值范围：TChan.Ch1、TChan.Ch2等
     * @param visible 可见性，true为可见，false为隐藏
     *                - XY模式下，通道强制显示，setVisible方法无效
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 用户打开/关闭通道时调用
     * - 程序控制通道显示/隐藏时调用
     * 
     * 【代理机制】
     * - 直接调用channelWaveManage_xy.setVisible(ChNo, visible)
     * - channelWaveManage_xy负责实际的可见性设置
     * 
     * 【XY模式特殊说明】
     * - XY模式下，通道强制显示，setVisible方法无效
     * - 这是因为XY模式需要两个通道都显示才能形成X-Y关系图
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void setVisible(int ChNo, boolean visible) { // 设置通道可见性方法，参数为通道号和可见性，1
        channelWaveManage_xy.setVisible(ChNo, visible); // 代理到channelWaveManage_xy.setVisible()方法，设置通道可见性，1
    } // 结束setVisible方法，1

    /**
     * 设置偏移量 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 设置波形显示区域的垂直偏移量，代理到channelWaveManage_xy
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
     * 【代理机制】
     * - 直接调用channelWaveManage_xy.setOffsetY(offsetY)
     * - channelWaveManage_xy负责实际的偏移量设置
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void setOffsetY(int offsetY) { // 设置偏移量方法，参数为垂直偏移量（像素），1
        channelWaveManage_xy.setOffsetY(offsetY); // 代理到channelWaveManage_xy.setOffsetY()方法，设置偏移量，1
    } // 结束setOffsetY方法，1

    /**
     * 设置通道位置 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 设置指定通道的垂直位置，代理到channelWaveManage_xy
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：TChan.Ch1、TChan.Ch2等
     * @param positionY 通道垂直位置（比例值）
     *                   - 单位：比例值（相对于显示区域高度）
     *                   - XY模式下：Ch2控制Y轴位置，Ch1控制X轴位置
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 用户拖动通道波形调整位置时调用
     * - 系统初始化时设置默认位置
     * - 从缓存恢复通道位置时调用
     * 
     * 【代理机制】
     * - 直接调用channelWaveManage_xy.setPositionY(chNo, positionY)
     * - channelWaveManage_xy负责实际的位置设置
     * 
     * 【XY模式特殊说明】
     * - XY模式下，通道位置映射与YT模式不同
     * - Ch2设置Y轴位置（垂直位置）
     * - Ch1设置X轴位置（水平位置）
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void setPositionY(int chNo, double positionY) { // 设置通道位置方法，参数为通道号和位置值，1
        channelWaveManage_xy.setPositionY(chNo, positionY); // 代理到channelWaveManage_xy.setPositionY()方法，设置通道位置，1
    } // 结束setPositionY方法，1

    /**
     * 从EventBus设置通道位置
     * 
     * 【功能说明】
     * 从EventBus接收位置更新，设置指定通道的位置，代理到channelWaveManage_xy
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：TChan.Ch1、TChan.Ch2等
     * @param positionY 位置值（像素）
     *                   - XY模式下：Ch2设置Y轴位置，Ch1设置X轴位置
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - EventBus接收到位置更新事件时调用
     * - 外部模块通过EventBus更新通道位置时调用
     * 
     * 【代理机制】
     * - 直接调用channelWaveManage_xy.setPositionYFromEventBus(chNo, positionY)
     * - channelWaveManage_xy负责实际的位置设置
     * 
     * 【与setPositionY的区别】
     * - setPositionY：普通设置位置方法
     * - setPositionYFromEventBus：从EventBus设置位置方法
     * - 区别在于EventBus设置会触发移动事件，标记isFromEventBus=true
     * 
     * 【关键代码行注释】
     */
    public void setPositionYFromEventBus(int chNo, int positionY) { // 从EventBus设置通道位置方法，参数为通道号和位置值，1
        channelWaveManage_xy.setPositionYFromEventBus(chNo, positionY); // 代理到channelWaveManage_xy.setPositionYFromEventBus()方法，从EventBus设置通道位置，1
    } // 结束setPositionYFromEventBus方法，1

    /**
     * 获取通道位置 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 获取指定通道的垂直位置，代理到channelWaveManage_xy
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：TChan.Ch1、TChan.Ch2等
     * 
     * 【返回值】
     * @return 通道垂直位置（比例值）
     *         - XY模式下：Ch2返回Y轴位置，Ch1返回X轴位置
     *         - 其他通道：返回0
     * 
     * 【调用时机】
     * - 外部模块获取通道位置时调用
     * - 保存通道位置到缓存时调用
     * 
     * 【代理机制】
     * - 直接调用channelWaveManage_xy.getPositionY(chNo)
     * - channelWaveManage_xy负责实际的位置获取
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public double getPositionY(int chNo) { // 获取通道位置方法，参数为通道号，返回通道位置值，1
        return channelWaveManage_xy.getPositionY(chNo); // 代理到channelWaveManage_xy.getPositionY()方法，获取通道位置，1
    } // 结束getPositionY方法，1


    /**
     * 设置通道中心位置 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 将指定通道的波形垂直位置设置为显示区域的中心位置（50%位置）
     * 代理到channelWaveManage_xy
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：TChan.Ch1、TChan.Ch2等
     *             - XY模式下：Ch2设置Y轴中心位置，Ch1设置X轴中心位置
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 用户点击"50%"按钮时调用
     * - 恢复通道到中心位置时调用
     * 
     * 【代理机制】
     * - 直接调用channelWaveManage_xy.setCenterChY(chNo)
     * - channelWaveManage_xy负责实际的中心位置设置
     * 
     * 【XY模式特殊说明】
     * - XY模式下，通道中心位置映射与YT模式不同
     * - Ch2设置Y轴中心位置（波形区域高度的一半）
     * - Ch1设置X轴中心位置（波形区域宽度的一半）
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void setCenterChY(int chNo) { // 设置通道中心位置方法，参数为通道号，1
        if (!TChan.isChan(chNo)) { // 判断通道号是否有效，使用TChan.isChan()方法检查，1
            return; // 如果通道号无效，直接返回，不执行后续操作，1
        } // 结束通道号有效性判断，1
//        channelWaveManage_xy.setCenterChY(); // 注释掉的代码，可能是之前的实现方式，1
        channelWaveManage_xy.setCenterChY(chNo); // 代理到channelWaveManage_xy.setCenterChY()方法，设置通道中心位置，1
    } // 结束setCenterChY方法，1

    /**
     * 设置选中通道 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 设置指定通道为选中状态，代理到channelWaveManage_xy
     * 
     * 【参数说明】
     * @param chNO 通道号，取值范围：TChan.Ch1、TChan.Ch2等
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
     * - 直接调用channelWaveManage_xy.setSelectCursor(chNO)
     * - channelWaveManage_xy负责实际的选中状态设置
     * 
     * 【互斥选择机制】
     * - 通道选中状态互斥，只能选中一个通道
     * - 选中Ch1时自动取消Ch2选中
     * - 选中Ch2时自动取消Ch1选中
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void setSelectCursor(int chNO) { // 设置选中通道方法，参数为通道号，1
        channelWaveManage_xy.setSelectCursor(chNO); // 代理到channelWaveManage_xy.setSelectCursor()方法，设置选中通道，1
    } // 结束setSelectCursor方法，1

    /**
     * 选择光标 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 根据点击坐标判断点击在哪个通道波形区域内，代理到channelWaveManage_xy
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
     *         - TChan.Ch1：点击在Ch1垂直波形区域内
     *         - TChan.Ch2：点击在Ch2水平波形区域内
     *         - -1：点击不在任何通道波形区域内
     * 
     * 【调用时机】
     * - 用户点击波形显示区域时调用
     * - 判断是否选中通道时调用
     * 
     * 【代理机制】
     * - 直接调用channelWaveManage_xy.selectCursor(x, y)
     * - channelWaveManage_xy负责实际的坐标判断和通道选择
     * 
     * 【XY模式特殊说明】
     * - 注释说"XY模式下应该没有用"，但实际代码中会判断点击位置
     * - 可能是因为XY模式下通道位置固定，不需要通过点击选择
     * - 类似于时基的操作，所以这个也不需要处理
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public int selectCursor(int x, int y) { // 选择光标方法，参数为点击坐标，返回点击的通道号，1
        return channelWaveManage_xy.selectCursor(x,y); // 代理到channelWaveManage_xy.selectCursor()方法，判断点击位置并返回通道号，1
    } // 结束selectCursor方法，1

    /**
     * 移动像素 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 移动当前选中通道波形的像素距离，代理到channelWaveManage_xy
     * 
     * 【参数说明】
     * @param px 移动的像素距离
     *           - 单位：像素
     *           - XY模式下：Ch1选中时水平移动，Ch2选中时垂直移动
     * 
     * 【返回值】
     * 无返回值（void）
     * 
     * 【调用时机】
     * - 用户拖动波形时调用（每次拖动事件）
     * - 系统自动调整波形位置时调用
     * 
     * 【代理机制】
     * - 直接调用channelWaveManage_xy.movePix(px)
     * - channelWaveManage_xy负责实际的像素移动
     * 
     * 【XY模式特殊说明】
     * - XY模式下，通道移动方向与选中通道相关
     * - Ch1选中时：水平移动（改变X轴位置）
     * - Ch2选中时：垂直移动（改变Y轴位置）
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void movePix(int px) { // 移动像素方法，参数为像素偏移量，1
        channelWaveManage_xy.movePix(px); // 代理到channelWaveManage_xy.movePix()方法，移动像素，1
    } // 结束movePix方法，1

    // ═══════════════════════════════════════════════════════════════════════════════
    // interface IWorkMode - 工作模式接口实现
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 切换工作模式 - IWorkMode接口实现
     * 
     * 【功能说明】
     * 切换工作模式，更新通道波形管理器的工作模式状态
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
     * - 从XY模式切换到其他模式时调用
     * 
     * 【代理机制】
     * - 直接调用channelWaveManage_xy.switchWorkMode(workMode)
     * - channelWaveManage_xy负责实际的工作模式切换
     * 
     * 【实现细节】
     * - channelWaveManage_xy会更新Ch1和Ch2通道波形的工作模式状态
     * - 通道波形需要更新显示状态以适应新的工作模式
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public void switchWorkMode(@WorkMode int workMode) { // 切换工作模式方法，参数为目标工作模式标识，1
        channelWaveManage_xy.switchWorkMode(workMode); // 代理到channelWaveManage_xy.switchWorkMode()方法，切换工作模式，1
    } // 结束switchWorkMode方法，1

    /**
     * 获取当前选中通道 - IWaveManage接口实现
     * 
     * 【功能说明】
     * 获取当前选中的通道号，代理到channelWaveManage_xy
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
     * 【代理机制】
     * - 直接调用channelWaveManage_xy.getCurCh()
     * - channelWaveManage_xy负责实际的选中通道获取
     * 
     * 【关键代码行注释】
     */
    @Override // 标记为接口方法实现，1
    public int getCurCh() { // 获取当前选中通道方法，无参数，返回当前选中的通道号，1
        return channelWaveManage_xy.getCurCh(); // 代理到channelWaveManage_xy.getCurCh()方法，获取当前选中通道，1
    } // 结束getCurCh方法，1

} // 结束WaveManage_XY类定义，1