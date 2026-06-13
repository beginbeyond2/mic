package com.micsig.tbook.scope.Sample;

import com.micsig.tbook.scope.Action.FPGAMessage;
import com.micsig.tbook.scope.Action.UiMessage;
import com.micsig.tbook.scope.Action.XAction;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;

/**
 * 存储深度动作处理类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Sample（示波器采样管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 动作处理层</li>
 *   <li>设计模式：命令模式 + 观察者模式</li>
 *   <li>职责类型：事件处理器</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>处理存储深度变化事件</li>
 *   <li>协调FPGA命令发送</li>
 *   <li>触发UI更新消息</li>
 *   <li>处理Zoom模式下的时基调整</li>
 *   <li>发送存储深度变化事件通知</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>解耦存储深度管理与硬件控制</li>
 *   <li>统一处理存储深度变化后的连锁反应</li>
 *   <li>协调多个模块响应存储深度变化</li>
 *   <li>确保FPGA配置与软件状态同步</li>
 * </ul>
 * 
 * <p><b>继承结构：</b>
 * <pre>
 * XAction (抽象动作基类)
 *   │
 *   └── MemDepthAction (当前类 - 存储深度动作处理)
 *          │
 *          ├── 持有 MemDepth 引用
 *          ├── 使用 FPGAMessage 发送硬件命令
 *          ├── 使用 UiMessage 发送UI更新
 *          └── 使用 EventFactory 发送事件通知
 * </pre>
 * 
 * <p><b>调用链路：</b>
 * <pre>
 * 用户切换存储深度档位
 *   │
 *   ▼
 * MemDepth.setMemDepthItem(idx)
 *   │
 *   ▼
 * MemDepthAction.memDepthChange()
 *   │
 *   ├─→ Zoom模式时基调整
 *   ├─→ FPGA命令发送
 *   ├─→ 事件通知发送
 *   └─→ UI消息发送
 * </pre>
 * 
 * <p><b>FPGA命令组合：</b>
 * <pre>
 * 存储深度变化时需要发送的FPGA命令：
 * ┌────────────────────────────────────────────────────────────┐
 * │ 命令                        │ 说明                          │
 * ├─────────────────────────────┼──────────────────────────────┤
 * │ FPGA_CMD_SAMP_ZUN_DEPTH     │ 采样存储深度配置              │
 * │ FPGA_CMD_SAMP_PLACE         │ 采样位置配置                  │
 * │ FPGA_CMD_COUY               │ 耦合配置                      │
 * │ FPGA_CMD_DIS                │ 显示配置                      │
 * │ FPGA_CMD_DIS_MODE           │ 显示模式配置                  │
 * │ FPGA_CMD_SAMP_MODE          │ 采样模式配置                  │
 * │ FPGA_CMD_SEGMENT            │ 段采样配置                    │
 * └─────────────────────────────┴──────────────────────────────┘
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>持有：MemDepth（存储深度管理对象）</li>
 *   <li>依赖：Scope（示波器状态管理）</li>
 *   <li>依赖：HorizontalAxis（水平轴管理）</li>
 *   <li>依赖：FPGAMessage（FPGA命令发送）</li>
 *   <li>依赖：UiMessage（UI消息发送）</li>
 *   <li>依赖：EventFactory（事件工厂）</li>
 * </ul>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>用户通过UI切换存储深度档位时</li>
 *   <li>恢复配置时设置存储深度</li>
 *   <li>通道数变化导致存储深度重新计算时</li>
 *   <li>段采样模式切换时</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-4-9
 * @see XAction 动作基类
 * @see MemDepth 存储深度管理
 * @see FPGAMessage FPGA命令
 * @see UiMessage UI消息
 * @see EventFactory 事件工厂
 * @see HorizontalAxis 水平轴管理
 */
public class MemDepthAction extends XAction {
    
    /**
     * 存储深度管理对象引用
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>持有当前存储深度配置的引用</li>
     *   <li>用于获取当前存储深度状态</li>
     * </ul>
     * 
     * <p><b>生命周期：</b>
     * <ul>
     *   <li>在MemDepth构造函数中创建MemDepthAction时传入</li>
     *   <li>与MemDepth对象生命周期相同</li>
     * </ul>
     */
    public MemDepth memDepth;
    
    /**
     * 构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>保存存储深度管理对象的引用</li>
     *   <li>建立MemDepth与MemDepthAction的关联</li>
     * </ul>
     * 
     * <p><b>调用时机：</b>
     * <ul>
     *   <li>MemDepth构造函数中创建MemDepthAction时调用</li>
     * </ul>
     * 
     * @param memDepth 存储深度管理对象
     */
    public MemDepthAction(MemDepth memDepth){
        this.memDepth = memDepth;

    }
    
    /**
     * 处理存储深度变化事件
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>响应存储深度变化，执行连锁更新操作</li>
     *   <li>仅在示波器运行状态下执行</li>
     *   <li>处理Zoom模式下的特殊逻辑</li>
     *   <li>发送FPGA命令更新硬件配置</li>
     *   <li>发送事件通知其他模块</li>
     *   <li>发送UI消息更新界面显示</li>
     * </ul>
     * 
     * <p><b>处理流程：</b>
     * <pre>
     * ┌─────────────────────────────────────────────────────────────┐
     * │ 1. 检查示波器运行状态                                        │
     * │    └─→ 非运行状态：直接返回，不处理                          │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 2. Zoom模式特殊处理                                          │
     * │    ├─→ 计算缩略视图允许的最大时基档位                        │
     * │    ├─→ 检查当前缩略视图时基是否超出限制                      │
     * │    └─→ 超出则调整缩略视图时基并发送UI消息                    │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 3. 发送FPGA命令组合                                          │
     * │    ├─→ 存储深度配置 (SAMP_ZUN_DEPTH)                        │
     * │    ├─→ 采样位置配置 (SAMP_PLACE)                            │
     * │    ├─→ 耦合配置 (COUY)                                      │
     * │    ├─→ 显示配置 (DIS)                                       │
     * │    ├─→ 显示模式配置 (DIS_MODE)                              │
     * │    ├─→ 采样模式配置 (SAMP_MODE)                             │
     * │    └─→ 段采样配置 (SEGMENT)                                 │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 4. 发送事件通知                                              │
     * │    └─→ EVENT_MEM_DEPTH：通知其他模块存储深度已变化          │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 5. 发送UI更新消息                                            │
     * │    ├─→ UI_MESSAGE_DEPTH_SAMPFRE：更新采样率显示             │
     * │    └─→ UI_MESSAGE_SIMPLE_WIN_DIS：更新简单窗口显示          │
     * └─────────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * <p><b>Zoom模式处理逻辑：</b>
     * <ul>
     *   <li>存储深度变化可能影响Zoom模式下的时基范围</li>
     *   <li>需要检查缩略视图时基是否仍然有效</li>
     *   <li>如果当前时基超出允许范围，自动调整到最大允许值</li>
     * </ul>
     * 
     * <p><b>FPGA命令说明：</b>
     * <ul>
     *   <li>SAMP_ZUN_DEPTH：更新FPGA的存储深度寄存器</li>
     *   <li>SAMP_PLACE：更新采样位置（触发位置相关）</li>
     *   <li>COUY：更新通道耦合配置</li>
     *   <li>DIS：更新显示相关配置</li>
     *   <li>DIS_MODE：更新显示模式（普通/Zoom/XY等）</li>
     *   <li>SAMP_MODE：更新采样模式（实时/等效/段采样等）</li>
     *   <li>SEGMENT：更新段采样相关配置</li>
     * </ul>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>用户切换存储深度档位时触发</li>
     *   <li>通道数变化导致存储深度重新计算时触发</li>
     *   <li>段采样模式切换时触发</li>
     *   <li>调用MemDepth.forceMemDepthChange()时触发</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>仅在示波器运行状态下执行，停止状态下不发送FPGA命令</li>
     *   <li>Zoom模式处理需要同步更新UI显示</li>
     *   <li>事件通知使用异步方式，避免阻塞主线程</li>
     * </ul>
     */
    public void memDepthChange(){
        if(Scope.getInstance().isRun()) {
            if(Scope.getInstance().isZoom()){
                Scope scope = Scope.getInstance();
                HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();
                int scaleNow = scope.enterZoom_SL_scale();
                int scaleLast = horizontalAxis.getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD);
                if (scaleLast > scaleNow) {
                    horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_SMALL, scaleNow);
                    sendUiMsg(UiMessage.UI_MESSAGE_TIME_SCALE_CHANGE);
                }
            }

            sendFpgaMsg(FPGAMessage.FPGA_CMD_SAMP_ZUN_DEPTH|FPGAMessage.FPGA_CMD_SAMP_PLACE
                    |FPGAMessage.FPGA_CMD_COUY|FPGAMessage.FPGA_CMD_DIS | FPGAMessage.FPGA_CMD_DIS_MODE
                    |FPGAMessage.FPGA_CMD_SAMP_MODE|FPGAMessage.FPGA_CMD_SEGMENT);
            sendEvent(EventFactory.EVENT_MEM_DEPTH,true);
            sendUiMsg(UiMessage.UI_MESSAGE_DEPTH_SAMPFRE);
            sendUiMsg(UiMessage.UI_MESSAGE_SIMPLE_WIN_DIS);
        }
    }
}
