package com.micsig.tbook.scope.Display;                                                   // 包声明：显示模块

import android.util.Log;                                                                  // 导入：日志类

import com.micsig.tbook.scope.Action.FPGAMessage;                                        // 导入：FPGA消息类
import com.micsig.tbook.scope.Action.UiMessage;                                          // 导入：UI消息类
import com.micsig.tbook.scope.Action.XAction;                                            // 导入：动作基类
import com.micsig.tbook.scope.Event.EventBase;                                           // 导入：事件基类
import com.micsig.tbook.scope.Event.EventFactory;                                        // 导入：事件工厂类
import com.micsig.tbook.scope.Scope;                                                     // 导入：示波器主类
import com.micsig.tbook.scope.horizontal.HorizontalAxis;                                 // 导入：水平轴管理类

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              DisplayAction - 示波器显示动作处理类                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Display模块的显示动作处理类，位于Display包下，                                ║
 * ║   继承自XAction基类，负责处理显示相关的硬件和UI动作。                           ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 处理缩放模式变化动作                                                     ║
 * ║   2. 处理显示模式变化动作                                                     ║
 * ║   3. 处理Roll模式变化动作                                                     ║
 * ║   4. 处理余辉相关动作                                                         ║
 * ║   5. 处理亮度、绘制类型、背景色等变化动作                                     ║
 * ║   6. 发送FPGA命令控制硬件                                                     ║
 * ║   7. 发送UI消息更新界面                                                       ║
 * ║   8. 发送事件通知其他模块                                                     ║
 * ║                                                                              ║
 * ║ 【架构位置】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        Display模块架构                                │ ║
 * ║   │                                                                      │ ║
 * ║   │   ┌─────────────────┐         ┌─────────────────┐                   │ ║
 * ║   │   │     Display     │────────▶│  DisplayAction  │                   │ ║
 * ║   │   │   (数据管理类)   │         │   (动作处理类)   │                   │ ║
 * ║   │   └─────────────────┘         └────────┬────────┘                   │ ║
 * ║   │          │                             │                            │ ║
 * ║   │          ▼                             ▼                            │ ║
 * ║   │   ┌─────────────────┐         ┌─────────────────┐                   │ ║
 * ║   │   │  显示设置数据   │         │  FPGA/UI/Event  │                   │ ║
 * ║   │   └─────────────────┘         └─────────────────┘                   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【缩放模式处理流程】                                                         ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        进入缩放模式流程                               │ ║
 * ║   │                                                                      │ ║
 * ║   │   用户开启缩放 ──▶ Display.setZoom(true)                             │ ║
 * ║   │                           │                                          │ ║
 * ║   │                           ▼                                          │ ║
 * ║   │              DisplayAction.zoomChange()                              │ ║
 * ║   │                           │                                          │ ║
 * ║   │              ┌────────────┼────────────┐                             │ ║
 * ║   │              ▼            ▼            ▼                             │ ║
 * ║   │         保存当前时基  计算放大时基  设置时间位置                       │ ║
 * ║   │                           │                                          │ ║
 * ║   │              ┌────────────┼────────────┐                             │ ║
 * ║   │              ▼            ▼            ▼                             │ ║
 * ║   │         FPGA命令      UI消息       Event事件                         │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【缩放时基计算逻辑】                                                         ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        放大窗口时基计算                               │ ║
 * ║   │                                                                      │ ║
 * ║   │   【计算规则】                                                         │ ║
 * ║   │   - 放大窗口时基必须小于主窗口时基                                    │ ║
 * ║   │   - 放大倍数至少为10倍                                                │ ║
 * ║   │   - 从主窗口时基开始，逐步减小时基直到满足放大倍数                    │ ║
 * ║   │                                                                      │ ║
 * ║   │   【计算公式】                                                         │ ║
 * ║   │   放大倍数 = 主窗口时基 / 放大窗口时基                                │ ║
 * ║   │   要求：放大倍数 > 9                                                  │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【FPGA命令说明】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        FPGA命令类型                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【显示相关命令】                                                     │ ║
 * ║   │   - FPGA_CMD_DIS: 显示控制                                           │ ║
 * ║   │   - FPGA_CMD_DIS_MODE: 显示模式                                      │ ║
 * ║   │   - FPGA_CMD_DIS_PIX: 显示像素                                       │ ║
 * ║   │   - FPGA_CMD_Y_PLACE: Y轴位置                                        │ ║
 * ║   │                                                                      │ ║
 * ║   │   【采样相关命令】                                                     │ ║
 * ║   │   - FPGA_CMD_SAMP_MODE: 采样模式                                     │ ║
 * ║   │   - FPGA_CMD_SAMP_PLACE: 采样位置                                    │ ║
 * ║   │   - FPGA_CMD_SAMP_ZUN_DEPTH: 存储深度                                │ ║
 * ║   │                                                                      │ ║
 * ║   │   【插值相关命令】                                                     │ ║
 * ║   │   - FPGA_CMD_EXT_CHAZHI_COEF: 插值系数                               │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【UI消息说明】                                                               ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        UI消息类型                                     │ ║
 * ║   │                                                                      │ ║
 * ║   │   - UI_MESSAGE_TIME_SCALE_CHANGE: 时基变化消息                       │ ║
 * ║   │   - UI_MESSAGE_ZOOM_ENTER: 进入缩放消息                              │ ║
 * ║   │   - UI_MESSAGE_DEPTH_SAMPFRE: 深度和采样频率消息                     │ ║
 * ║   │   - UI_MESSAGE_SIMPLE_WIN_DIS: 简单窗口显示消息                      │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【事件说明】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        Event事件类型                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   - EVENT_DISPLAY_ZOOM: 显示缩放事件                                 │ ║
 * ║   │   - EVENT_DISPLAY_MODE: 显示模式事件                                 │ ║
 * ║   │   - EVENT_AFTERGLOW_TIME: 余辉时间事件                               │ ║
 * ║   │   - EVENT_AFTERGLOW_CLEAR: 清除余辉事件                              │ ║
 * ║   │   - EVENT_AFTERGLOW_MATH_CLEAR: 清除数学余辉事件                     │ ║
 * ║   │   - EVENT_WAVE_CLEAR: 清除波形事件                                   │ ║
 * ║   │   - EVENT_AFTERGLOW_ENABLE: 余辉使能事件                             │ ║
 * ║   │   - EVENT_DIAPLAY_WAVE_BRIGHTNESS: 波形亮度事件                      │ ║
 * ║   │   - EVENT_DRAW_TYPE: 绘制类型事件                                    │ ║
 * ║   │   - EVENT_HORREF: 水平参考事件                                       │ ║
 * ║   │   - EVENT_DISPLAY_CCT: 显示色温事件                                  │ ║
 * ║   │   - EVENT_DISPLAY_BACKGROUND: 显示背景事件                           │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 缩放模式：进入/退出缩放模式，计算放大窗口时基                          ║
 * ║   2. 显示模式：YT模式和XY模式切换                                          ║
 * ║   3. Roll模式：滚动模式和触发模式切换                                      ║
 * ║   4. 余辉设置：设置余辉类型和时间                                          ║
 * ║   5. 显示调整：亮度、绘制类型、背景色设置                                  ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 动作模式：将动作封装为独立类                                           ║
 * ║   - 与Display类关联，处理Display的状态变化                                ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - Display: 显示管理类                                                   ║
 * ║   - XAction: 动作基类                                                     ║
 * ║   - FPGAMessage: FPGA消息类                                               ║
 * ║   - UiMessage: UI消息类                                                   ║
 * ║   - EventFactory: 事件工厂                                                ║
 * ║   - HorizontalAxis: 水平轴管理类                                          ║
 * ║   - Scope: 示波器主类                                                     ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 示波器显示动作处理类
 * 继承自XAction基类，负责处理显示相关的硬件和UI动作
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>缩放模式处理：计算放大窗口时基，设置时间位置</li>
 *   <li>显示模式处理：YT模式和XY模式切换</li>
 *   <li>Roll模式处理：滚动模式和触发模式切换</li>
 *   <li>余辉处理：清除余辉，设置余辉类型和时间</li>
 *   <li>显示设置：亮度、绘制类型、背景色变化处理</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 在Display中创建DisplayAction实例
 * DisplayAction displayAction = new DisplayAction(display);
 *
 * // 处理缩放模式变化
 * displayAction.zoomChange(true);
 *
 * // 处理显示模式变化
 * displayAction.displayModeChange();
 *
 * // 清除余辉
 * displayAction.clearPersist();
 * </pre>
 *
 * @see Display
 * @see XAction
 * @see FPGAMessage
 * @see UiMessage
 */
public class DisplayAction extends XAction {                                               // 类声明：显示动作处理类，继承自XAction

    // ═══════════════════════════════════════════════════════════════════════════════
    // 日志标签
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 日志标签，用于日志输出时标识此类 */
    private static final String TAG = "DisplayAction";                                     // 静态常量：日志标签

    // ═══════════════════════════════════════════════════════════════════════════════
    // 缩放变化标志常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 无标志：不重新加载放大窗口时基，不重置备份时基ID */
    public static final int FLAGS_NONE_LARGE_TIME_SCALE = 0x00;                            // 静态常量：无标志，值为0x00

    /** 重新加载放大窗口时基标志 */
    public static final int FLAGS_RELOAD_LARGE_TIME_SCALE = 0x01;                          // 静态常量：重新加载放大窗口时基标志，值为0x01

    /** 重置备份时基ID标志 */
    public static final int FLAGS_RESET_BAKSCALEID = 0x02;                                 // 静态常量：重置备份时基ID标志，值为0x02

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 关联的显示管理对象 */
    private Display display;                                                               // 成员变量：关联的显示管理对象

    /** 备份时基ID，用于退出缩放时恢复原来的时基 */
    private int bakScaleId = -1;                                                           // 成员变量：备份时基ID，初始值为-1（无效值）

    /** 是否重新加载放大窗口时基标识 */
    private boolean isReloadLargeTimeScale = false;                                        // 成员变量：是否重新加载放大窗口时基标识，初始值为false

    /** 是否重置备份时基ID标识 */
    private boolean isResetBakScaleId = false;                                             // 成员变量：是否重置备份时基ID标识，初始值为false

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造函数
     * 初始化显示动作处理器
     *
     * @param display 关联的显示管理对象
     */
    public DisplayAction(Display display) {                                                // 构造方法：初始化显示动作处理器
        this.display = display;                                                            // 保存显示管理对象引用
    }                                                                                       // 构造方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 缩放变化标志设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置缩放变化标志
     * 根据标志位设置是否重新加载放大窗口时基和是否重置备份时基ID
     *
     * @param changeFlags 缩放变化标志（可以是多个标志的组合）
     */
    public void setZoomChangeFlags(int changeFlags) {                                      // 公有方法：设置缩放变化标志
        if ((changeFlags & FLAGS_RELOAD_LARGE_TIME_SCALE) == FLAGS_RELOAD_LARGE_TIME_SCALE) { // 如果包含重新加载放大窗口时基标志
            isReloadLargeTimeScale = true;                                                 // 设置重新加载放大窗口时基标识为true
        }                                                                                   // if语句结束
        if ((changeFlags & FLAGS_RESET_BAKSCALEID) == FLAGS_RESET_BAKSCALEID) {            // 如果包含重置备份时基ID标志
            isResetBakScaleId = true;                                                       // 设置重置备份时基ID标识为true
        }                                                                                   // if语句结束
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 缩放模式变化处理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 处理缩放模式变化
     * 进入或退出缩放模式，计算放大窗口时基，设置时间位置
     *
     * <p><b>进入缩放模式处理流程：</b></p>
     * <ol>
     *   <li>保存当前主窗口时基ID</li>
     *   <li>获取进入缩放后的主窗口时基</li>
     *   <li>计算放大窗口时基（至少放大10倍）</li>
     *   <li>设置主窗口和放大窗口的时基ID</li>
     *   <li>设置主窗口和放大窗口的时间位置</li>
     *   <li>发送UI消息、FPGA命令和事件通知</li>
     * </ol>
     *
     * <p><b>退出缩放模式处理流程：</b></p>
     * <ol>
     *   <li>恢复原来的主窗口时基ID（如果有备份）</li>
     *   <li>设置主窗口时间位置</li>
     *   <li>校正时间位置</li>
     *   <li>发送UI消息、FPGA命令和事件通知</li>
     * </ol>
     *
     * @param zoom true=强制更新放大窗口时基，false=不强制更新
     */
    public void zoomChange(boolean zoom) {                                                 // 公有方法：处理缩放模式变化
        Scope scope = Scope.getInstance();                                                 // 获取Scope实例
        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();                      // 获取HorizontalAxis实例

        if (display.isZoom()) {                                                            // 如果进入缩放模式
            // ─────────────────────────────────────────────────────────────────────
            // 保存当前主窗口时基ID
            // ─────────────────────────────────────────────────────────────────────
            bakScaleId = horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD);  // 保存当前主窗口时基ID

            // ─────────────────────────────────────────────────────────────────────
            // 获取进入缩放后的主窗口时基
            // ─────────────────────────────────────────────────────────────────────
            int scaleNow = scope.enterZoom_SL_scale();                                     // 获取进入缩放后的主窗口时基ID
            int scaleLast = horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD); // 获取当前主窗口时基ID

            if (scaleLast <= scaleNow)                                                     // 如果当前时基ID小于等于进入缩放后的时基ID
                scaleLast = scaleNow;                                                      // 则使用进入缩放后的时基ID

            // ─────────────────────────────────────────────────────────────────────
            // 计算放大窗口时基
            // ─────────────────────────────────────────────────────────────────────
            double scaleNowTime = horizontalAxis.getTimeScaleIdVal(scaleNow);               // 获取主窗口时基值（秒）
            double scaleLastTime = scaleNowTime;                                           // 初始化放大窗口时基值

            if (isReloadLargeTimeScale) {                                                  // 如果需要重新加载放大窗口时基
                scaleLast = horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_LARGE);  // 获取当前放大窗口时基ID
                if (scaleLast <= scaleNow) scaleLast = scaleNow;                           // 确保放大窗口时基ID不小于主窗口时基ID
                isReloadLargeTimeScale = false;                                            // 重置重新加载标识
            } else {                                                                       // 否则（自动计算放大窗口时基）
                for (int i = scaleLast; i < HorizontalAxis.getMaxGear() + 1; i++) {         // 循环：从当前时基ID到最大时基ID
                    scaleLast = i;                                                         // 更新放大窗口时基ID
                    scaleLastTime = horizontalAxis.getTimeScaleIdVal(i);                   // 获取当前时基值
                    if (((int) (scaleNowTime / scaleLastTime + 0.5)) > 9) {                 // 如果放大倍数大于9（至少10倍）
                        break;                                                             // 跳出循环
                    }                                                                       // if语句结束
                }                                                                           // 循环结束
            }                                                                               // if-else语句结束

            // ─────────────────────────────────────────────────────────────────────
            // 设置主窗口和放大窗口的时基ID
            // ─────────────────────────────────────────────────────────────────────
            horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_SMALL, scaleNow);        // 设置主窗口时基ID
            if (!zoom) {                                                                   // 如果不强制更新
                horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_LARGE, scaleLast);   // 设置放大窗口时基ID
            }                                                                               // if语句结束

            // ─────────────────────────────────────────────────────────────────────
            // 设置主窗口和放大窗口的时间位置
            // ─────────────────────────────────────────────────────────────────────
            long timePosNow = scope.enterZoom_SL_timePos();                                // 获取进入缩放后的主窗口时间位置
            long timePosLast = horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_SMALL);  // 获取当前主窗口时间位置

            long halfTimeScreen_n = -scope.timeOneScreen_zoom() / 2;                       // 计算半屏时间（负值）
            if (timePosLast < halfTimeScreen_n)                                            // 如果时间位置小于半屏时间
                timePosLast = halfTimeScreen_n;                                            // 则限制为半屏时间

            horizontalAxis.setTimePosOfView(HorizontalAxis.WPI_LARGE, timePosLast);        // 设置放大窗口时间位置
            horizontalAxis.setTimePosOfView(HorizontalAxis.WPI_SMALL, timePosNow);         // 设置主窗口时间位置

            // ─────────────────────────────────────────────────────────────────────
            // 发送UI消息
            // ─────────────────────────────────────────────────────────────────────
            sendUiMsg(UiMessage.UI_MESSAGE_TIME_SCALE_CHANGE);                             // 发送时基变化消息
            sendUiMsg(UiMessage.UI_MESSAGE_ZOOM_ENTER);                                    // 发送进入缩放消息
        } else {                                                                           // 否则（退出缩放模式）
            // ─────────────────────────────────────────────────────────────────────
            // 处理备份时基ID
            // ─────────────────────────────────────────────────────────────────────
            if (isResetBakScaleId) {                                                       // 如果需要重置备份时基ID
                isResetBakScaleId = false;                                                 // 重置标识
                bakScaleId = -1;                                                           // 重置备份时基ID为无效值
            }                                                                               // if语句结束

            // ─────────────────────────────────────────────────────────────────────
            // 恢复主窗口时基ID
            // ─────────────────────────────────────────────────────────────────────
            if (bakScaleId >= 0) {                                                         // 如果有有效的备份时基ID
                horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD, bakScaleId); // 恢复主窗口时基ID为备份值
            } else {                                                                       // 否则（没有备份）
                horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD, horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD)); // 保持当前主窗口时基ID
            }                                                                               // if-else语句结束

            // ─────────────────────────────────────────────────────────────────────
            // 设置主窗口时间位置
            // ─────────────────────────────────────────────────────────────────────
            horizontalAxis.setTimePosOfView(HorizontalAxis.WPI_STANDARD, horizontalAxis.getTimePosOfView(HorizontalAxis.WPI_LARGE)); // 设置主窗口时间位置为放大窗口时间位置
            horizontalAxis.correctTimePose();                                              // 校正时间位置

            // ─────────────────────────────────────────────────────────────────────
            // 发送UI消息
            // ─────────────────────────────────────────────────────────────────────
            sendUiMsg(UiMessage.UI_MESSAGE_TIME_SCALE_CHANGE);                             // 发送时基变化消息
        }                                                                                   // if-else语句结束

        // ─────────────────────────────────────────────────────────────────────────
        // 发送FPGA命令、事件和UI消息
        // ─────────────────────────────────────────────────────────────────────────
        sendFpgaMsg(FPGAMessage.FPGA_CMD_DIS_MODE | FPGAMessage.FPGA_CMD_DIS | FPGAMessage.FPGA_CMD_DIS_PIX); // 发送FPGA命令：显示模式、显示控制、显示像素
        if (Scope.getInstance().isRun()) {                                                 // 如果示波器处于运行状态
            sendFpgaMsg(0, FPGAMessage.FPGA_CMD_EXT_CHAZHI_COEF);                          // 发送FPGA命令：插值系数
        }                                                                                   // if语句结束
        sendEvent(EventFactory.EVENT_DISPLAY_ZOOM);                                        // 发送显示缩放事件
        sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);                                     // 发送深度和采样频率消息
        sendUiMsg(UiMessage.UI_MESSAGE_SIMPLE_WIN_DIS);                                    // 发送简单窗口显示消息
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 显示模式变化处理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 处理显示模式变化
     * YT模式和XY模式切换时调用
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>发送显示模式变化事件</li>
     *   <li>发送通道采样命令</li>
     *   <li>发送FPGA命令</li>
     *   <li>发送UI消息</li>
     * </ol>
     */
    public void displayModeChange() {                                                      // 公有方法：处理显示模式变化
        sendEvent(EventFactory.EVENT_DISPLAY_MODE);                                        // 发送显示模式变化事件
        sendChSample();                                                                    // 发送通道采样命令
        sendFpgaMsg(FPGAMessage.FPGA_CMD_DIS_MODE | FPGAMessage.FPGA_CMD_DIS | FPGAMessage.FPGA_CMD_Y_PLACE); // 发送FPGA命令：显示模式、显示控制、Y轴位置
        sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);                                     // 发送深度和采样频率消息
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // Roll模式变化处理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 处理Roll模式变化
     * 滚动模式和触发模式切换时调用
     *
     * <p><b>处理逻辑：</b></p>
     * <ul>
     *   <li>只有在Roll时基档位且示波器运行时才处理</li>
     *   <li>如果是Roll模式，设置时间位置为0</li>
     *   <li>发送FPGA命令更新采样参数</li>
     * </ul>
     */
    public void rollChange() {                                                             // 公有方法：处理Roll模式变化
        if (HorizontalAxis.isRoolScale() && Scope.getInstance().isRun()) {                 // 如果是Roll时基档位且示波器运行
            if (display.isRoll()) {                                                        // 如果是Roll模式
                HorizontalAxis.getInstance().setTimePosOfView(0);                          // 设置时间位置为0
                EventFactory.sendEvent(EventFactory.EVENT_TIME_POS);                       // 发送时间位置变化事件
            }                                                                               // if语句结束
            sendFpgaMsg(FPGAMessage.FPGA_CMD_DIS                                           // 发送FPGA命令：显示控制
                    | FPGAMessage.FPGA_CMD_SAMP_MODE                                       // 采样模式
                    | FPGAMessage.FPGA_CMD_SAMP_PLACE                                      // 采样位置
                    | FPGAMessage.FPGA_CMD_SAMP_ZUN_DEPTH);                                // 存储深度
        }                                                                                   // if语句结束
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 余辉相关方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 处理余辉调整时间变化
     * 发送余辉时间变化事件
     */
    public void persistAdjustTimeChange() {                                                // 公有方法：处理余辉调整时间变化
        sendEvent(EventFactory.EVENT_AFTERGLOW_TIME);                                      // 发送余辉时间变化事件
    }                                                                                       // 方法结束

    /**
     * 清除余辉
     * 发送清除余辉事件
     */
    public void clearPersist() {                                                           // 公有方法：清除余辉
        sendEvent(EventFactory.EVENT_AFTERGLOW_CLEAR);                                     // 发送清除余辉事件
    }                                                                                       // 方法结束

    /**
     * 清除FFT余辉
     * 发送清除数学余辉事件（异步，延迟200ms）
     */
    public void clearFftPersist() {                                                        // 公有方法：清除FFT余辉
        EventFactory.sendEvent(new EventBase(EventFactory.EVENT_AFTERGLOW_MATH_CLEAR), true, 200); // 发送清除数学余辉事件（异步，延迟200ms）
    }                                                                                       // 方法结束

    /**
     * 清除波形
     * 发送清除波形事件
     */
    public void clearWave() {                                                              // 公有方法：清除波形
        sendEvent(EventFactory.EVENT_WAVE_CLEAR);                                          // 发送清除波形事件
    }                                                                                       // 方法结束

    /**
     * 处理余辉类型变化
     * 发送FPGA命令和余辉使能事件
     */
    public void persistTypeChange() {                                                      // 公有方法：处理余辉类型变化
        sendFpgaMsg(FPGAMessage.FPGA_CMD_DIS);                                             // 发送FPGA命令：显示控制
        sendEvent(EventFactory.EVENT_AFTERGLOW_ENABLE);                                    // 发送余辉使能事件
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 亮度变化处理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 处理波形亮度变化
     * 发送FPGA命令和波形亮度变化事件
     */
    public void brightnessChange() {                                                       // 公有方法：处理波形亮度变化
        sendFpgaMsg(FPGAMessage.FPGA_CMD_DIS_MODE | FPGAMessage.FPGA_CMD_DIS);             // 发送FPGA命令：显示模式、显示控制
        sendEvent(EventFactory.EVENT_DIAPLAY_WAVE_BRIGHTNESS);                             // 发送波形亮度变化事件
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 绘制类型变化处理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 处理绘制类型变化
     * 点模式和线模式切换时调用
     * 发送FPGA命令和绘制类型变化事件
     */
    public void drawTypeChange() {                                                         // 公有方法：处理绘制类型变化
        sendFpgaMsg(FPGAMessage.FPGA_CMD_DIS_MODE                                          // 发送FPGA命令：显示模式
                | FPGAMessage.FPGA_CMD_DIS                                                 // 显示控制
                , FPGAMessage.FPGA_CMD_EXT_CHAZHI_COEF);                                   // 插值系数
        sendEvent(EventFactory.EVENT_DRAW_TYPE);                                           // 发送绘制类型变化事件
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 水平参考变化处理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 处理水平参考变化
     * 发送水平参考变化事件
     */
    public void horRefChange() {                                                           // 公有方法：处理水平参考变化
        sendEvent(EventFactory.EVENT_HORREF);                                              // 发送水平参考变化事件
    }                                                                                       // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 色温和背景色变化处理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 处理色温变化
     * 发送显示色温变化事件
     */
    public void cctChange() {                                                              // 公有方法：处理色温变化
        sendEvent(EventFactory.EVENT_DISPLAY_CCT);                                         // 发送显示色温变化事件
    }                                                                                       // 方法结束

    /**
     * 处理背景色变化
     * 发送显示背景变化事件
     */
    public void backgroundChange() {                                                       // 公有方法：处理背景色变化
        sendEvent(EventFactory.EVENT_DISPLAY_BACKGROUND);                                  // 发送显示背景变化事件
    }                                                                                       // 方法结束
}                                                                                           // 类结束
