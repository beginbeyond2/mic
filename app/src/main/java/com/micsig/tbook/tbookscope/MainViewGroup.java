package com.micsig.tbook.tbookscope;

import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.AutoSave;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.tbookscope.main.dialog.DialogMeasureStatics;
import com.micsig.tbook.tbookscope.main.dialog.DialogOk;
import com.micsig.tbook.tbookscope.main.dialog.DialogOkCancel;
import com.micsig.tbook.tbookscope.main.dialog.MainChannelVerticalScale;
import com.micsig.tbook.tbookscope.main.dialog.MainDialogMenuHalf;
import com.micsig.tbook.tbookscope.main.mainbottom.MainBottomMeasureItem;
import com.micsig.tbook.tbookscope.main.mainbottom.MainHolderBottom;
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterChannel;
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterMenu;
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterSegmented;
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterTest;
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterTimeBase;
import com.micsig.tbook.tbookscope.main.maincenter.MainMsgCenterMenuCommand;
import com.micsig.tbook.tbookscope.main.maincenter.serialsword.MainLayoutCenterSerialsWord;
import com.micsig.tbook.tbookscope.main.mainright.MainRightLayoutItemChannelMaster;
import com.micsig.tbook.tbookscope.main.mainright.MainRightLayoutItemSerialsMaster;
import com.micsig.tbook.tbookscope.main.maintop.MainTopLayoutCenter;
import com.micsig.tbook.tbookscope.main.maintop.MainTopLayoutLeft;
import com.micsig.tbook.tbookscope.main.maintop.MainTopLayoutRight;
import com.micsig.tbook.tbookscope.main.maintop.MainTopLayoutRightButton;
import com.micsig.tbook.tbookscope.menu.MainMsgSliderZone;
import com.micsig.tbook.tbookscope.menu.SliderZone;
import com.micsig.tbook.tbookscope.middleware.MiddleMain;
import com.micsig.tbook.tbookscope.rightslipmenu.RightLayoutMath;
import com.micsig.tbook.tbookscope.rightslipmenu.RightLayoutRef;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogLoadRefCsvWave;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogSelectColor;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogSetChannelInfo;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogMeasureTValue;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFullFloatKeyBoard;
import com.micsig.tbook.tbookscope.util.ViewUtils;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.display.RulerManage;
import com.micsig.tbook.tbookscope.wavezone.display.WaveGridManage;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.tbookscope.wavezone.trigger.VoltageLineManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.wavezone.IChan;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum;
import com.micsig.tbook.tbookscope.middleware.mq.msg.MsgChActiveChange;
import com.micsig.tbook.tbookscope.rightslipmenu.RightLayoutChannel;
import com.micsig.tbook.tbookscope.rightslipmenu.RightLayoutLevel;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogBandWidth;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogBandWidthHz;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogBaudRate;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogChannelLabel;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogMathFFTPersist;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogProbeInterface;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogProbeMultiple;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogRefRecall;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob.ExternalKeysManager;
import com.micsig.tbook.tbookscope.structdata.ExternalKeysUI;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.ScreenControls;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplay;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplayCommon;
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSegmentedState;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogCount;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogMeasureDelay;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogMeasurePhase;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogNumberPicker;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogScale;
import com.micsig.tbook.tbookscope.top.popwindow.TopLayoutPopWindow;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardformula.TopDialogFormulaKeyBoard;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.Screen;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.ui.MSlideLayout;
import com.micsig.tbook.ui.MTriggerLevel;
import com.micsig.tbook.ui.top.view.selectHorList.TopViewSelectHorListToList;
import com.micsig.tbook.ui.wavezone.TChan;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                      MainViewGroup - 示波器主视图容器                         │
 * └─────────────────────────────────────────────────────────────────────────────┘
 * 
 * 【架构图】
 * 
 *      ┌──────────────────────────────────────────────────────────────────┐
 *      │                        MainActivity                              │
 *      │                    (主Activity - 界面入口)                        │
 *      └───────────────────────────┬──────────────────────────────────────┘
 *                                  │
 *                                  │ 创建并管理
 *                                  ▼
 *      ┌──────────────────────────────────────────────────────────────────┐
 *      │                       MainViewGroup                              │
 *      │                    (主视图容器 - 核心容器类)                        │
 *      │  ┌─────────────────────────────────────────────────────────────┐ │
 *      │  │  四大滑动菜单区域管理                                         │ │
 *      │  │  - TOPSLIP: 顶部菜单 (触发/采样/测量/显示等)                  │ │
 *      │  │  - BOTTOMSLIP: 底部菜单 (快捷菜单/时基等)                     │ │
 *      │  │  - RIGHTSLIP_CH1-8: 右侧通道菜单 (8个模拟通道)                │ │
 *      │  │  - RIGHTSLIP_MATH1-8: 右侧数学运算菜单 (8个Math通道)          │ │
 *      │  │  - RIGHTSLIP_REF1-8: 右侧参考波形菜单 (8个Ref通道)            │ │
 *      │  │  - RIGHTSLIP_S1-4: 右侧串行总线菜单 (4个Serials通道)          │ │
 *      │  │  - LEFTSLIP: 左侧菜单 (预留)                                  │ │
 *      │  └─────────────────────────────────────────────────────────────┘ │
 *      │  ┌─────────────────────────────────────────────────────────────┐ │
 *      │  │  触摸事件处理系统                                             │ │
 *      │  │  - dispatchTouchEvent: 事件分发入口                          │ │
 *      │  │  - onEvent: 核心事件处理逻辑                                  │ │
 *      │  │  - dealSliderZone: 滑动菜单拖动处理                           │ │
 *      │  │  - VelocityTracker: 速度追踪器                                │ │
 *      │  └─────────────────────────────────────────────────────────────┘ │
 *      │  ┌─────────────────────────────────────────────────────────────┐ │
 *      │  │  RxBus事件订阅系统                                            │ │
 *      │  │  - consumerMainSlipFromOther: 菜单滑出/隐藏事件               │ │
 *      │  │  - consumerCommandToUI: 命令消息处理                          │ │
 *      │  │  - consumerWorkModeChange: 工作模式切换                       │ │
 *      │  │  - consumerMeasureRowCount: 测量行数变化                      │ │
 *      │  │  - OnChanActiveChange: 通道激活状态变化                       │ │
 *      │  └─────────────────────────────────────────────────────────────┘ │
 *      │  ┌─────────────────────────────────────────────────────────────┐ │
 *      │  │  对话框管理系统                                               │ │
 *      │  │  - 28种对话框类型 (使用@Dialog注解)                           │ │
 *      │  │  - isDialogShow: 检查对话框显示状态                           │ │
 *      │  │  - hideDialog: 隐藏指定对话框                                  │ │
 *      │  │  - hideAllDialogsButDialogOkCancel: 隐藏所有对话框            │ │
 *      │  └─────────────────────────────────────────────────────────────┘ │
 *      └───────────────────────────┬──────────────────────────────────────┘
 *                                  │
 *                                  │ 包含子布局
 *                                  ▼
 *      ┌──────────────────────────────────────────────────────────────────┐
 *      │                       子布局组件层                                │
 *      │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐   │
 *      │  │ TopLayoutPopWindow│  │ MainTopLayoutLeft│  │ MainTopLayoutRight│ │
 *      │  │  (顶部弹出菜单)   │  │  (左侧顶部布局)  │  │  (右侧顶部布局)  │   │
 *      │  └─────────────────┘  └─────────────────┘  └─────────────────┘   │
 *      │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐   │
 *      │  │RightLayoutChannel│  │ RightLayoutMath │  │ RightLayoutRef  │   │
 *      │  │  (通道菜单)      │  │  (数学运算菜单)  │  │  (参考波形菜单) │   │
 *      │  └─────────────────┘  └─────────────────┘  └─────────────────┘   │
 *      │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐   │
 *      │  │RightLayoutSerials│  │ MainLayoutCenter│  │ MTriggerLevel   │   │
 *      │  │  (串行总线菜单)  │  │  (中央布局)      │  │  (触发电平线)   │   │
 *      │  └─────────────────┘  └─────────────────┘  └─────────────────┘   │
 *      └──────────────────────────────────────────────────────────────────┘
 * 
 * 【模块定位】
 * - 所属模块: tbookscope (示波器核心模块)
 * - 模块类型: UI层 (Presentation Layer)
 * - 职责范围: 示波器主界面的容器管理、触摸事件处理、滑动菜单控制
 * - 设计模式: 
 *   1. 容器模式 - 作为所有子布局的容器
 *   2. 观察者模式 - 通过RxBus订阅和响应事件
 *   3. 状态模式 - 管理不同滑动菜单的显示/隐藏状态
 *   4. 注解模式 - 使用@Slip和@Dialog注解定义菜单和对话框类型
 * 
 * 【核心职责】
 * 1. 界面容器管理: 管示波器主界面的所有子布局组件
 * 2. 滑动菜单管理: 管理顶部/底部/左侧/右侧的滑动菜单显示和隐藏
 * 3. 触摸事件处理: 处理用户的触摸事件，实现滑动菜单的拖动和点击
 * 4. RxBus事件订阅: 订阅并响应各种业务事件（菜单切换、通道激活等）
 * 5. 对话框管理: 管理28种不同类型的对话框的显示和隐藏
 * 6. 波形区域控制: 控制波形显示区域的启用/禁用状态
 * 7. 触发电平管理: 管理触发电平线的显示和拖动
 * 
 * 【界面布局结构】
 * ┌────────────────────────────────────────────────────────────────────┐
 * │                         TOPSLIP (顶部菜单)                          │
 * │  ┌──────────────────────────────────────────────────────────────┐ │
 * │  │  TopLayoutPopWindow (触发/采样/测量/显示/自动设置等菜单)       │ │
 * │  └──────────────────────────────────────────────────────────────┘ │
 * ├────────────────────────────────────────────────────────────────────┤
 * │                        MainTopLayout (顶部栏)                       │
 * │  ┌────────────┐  ┌──────────────────┐  ┌──────────────────────┐  │
 * │  │ Left       │  │ Center           │  │ Right                │  │
 * │  │ (左侧按钮) │  │ (中央信息显示)   │  │ (右侧按钮)           │  │
 * │  └────────────┘  └──────────────────┘  └──────────────────────┘  │
 * ├────────────────────────────────────────────────────────────────────┤
 * │                    MainLayoutCenter (中央区域)                      │
 * │  ┌──────────────────────────────────────────────────────────────┐ │
 * │  │  MainLayoutCenterChannel (通道选择条)                          │ │
 * │  │  MainLayoutCenterMenu (中央菜单)                               │ │
 * │  │  MainLayoutCenterSegmented (分段存储)                          │ │
 * │  │  MainLayoutCenterTest (测试布局)                               │ │
 * │  │  MTriggerLevel (触发电平线)                                    │ │
 * │  └──────────────────────────────────────────────────────────────┘ │
 * ├────────────────────────────────────────────────────────────────────┤
 * │                       RIGHTSLIP (右侧菜单)                          │
 * │  ┌──────────────────────────────────────────────────────────────┐ │
 * │  │  RIGHTSLIP_CH1-8: 8个模拟通道菜单                               │ │
 * │  │  RIGHTSLIP_MATH1-8: 8个数学运算菜单                            │ │
 * │  │  RIGHTSLIP_REF1-8: 8个参考波形菜单                             │ │
 * │  │  RIGHTSLIP_S1-4: 4个串行总线菜单                               │ │
 * │  │  RightLayoutLevel (电平菜单)                                   │ │
 * │  └──────────────────────────────────────────────────────────────┘ │
 * ├────────────────────────────────────────────────────────────────────┤
 * │                       BOTTOMSLIP (底部菜单)                         │
 * │  ┌──────────────────────────────────────────────────────────────┐ │
 * │  │  Quick菜单 (快捷操作菜单)                                      │ │
 * │  │  TimeBase菜单 (时基设置)                                       │ │
 * │  └──────────────────────────────────────────────────────────────┘ │
 * ├────────────────────────────────────────────────────────────────────┤
 * │                       LEFTSLIP (左侧菜单)                           │
 * │  ┌──────────────────────────────────────────────────────────────┐ │
 * │  │  预留区域 (当前未启用)                                         │ │
 * │  └──────────────────────────────────────────────────────────────┘ │
 * └────────────────────────────────────────────────────────────────────┘
 * 
 * 【滑动菜单类型说明】
 * ┌──────────────┬──────────────────────────────────────────────────────┐
 * │   菜单类型    │                         说明                          │
 * ├──────────────┼──────────────────────────────────────────────────────┤
 * │ TOPSLIP      │ 顶部滑动菜单 - 从屏幕顶部向下滑出                      │
 * │              │ - 包含: 触发设置、采样设置、测量设置、显示设置等        │
 * │              │ - 滑动方向: SliderDir_TopToBottom                    │
 * │              │ - 触发区域: 屏幕顶部边缘                               │
 * ├──────────────┼──────────────────────────────────────────────────────┤
 * │ BOTTOMSLIP   │ 底部滑动菜单 - 从屏幕底部向上滑出                      │
 * │              │ - 包含: 快捷菜单、时基设置等                            │
 * │              │ - 滑动方向: SliderDir_BottomToTop                    │
 * │              │ - 触发区域: 屏幕底部边缘                               │
 * ├──────────────┼──────────────────────────────────────────────────────┤
 * │ RIGHTSLIP_CH │ 右侧通道菜单 - 从屏幕右侧向左滑出                      │
 * │  (1-8)       │ - 包含: 8个模拟通道(CH1-CH8)的参数设置                 │
 * │              │ - 滑动方向: SliderDir_RightToLeft                    │
 * │              │ - 触发区域: 右侧通道按钮区域                           │
 * ├──────────────┼──────────────────────────────────────────────────────┤
 * │ RIGHTSLIP_MATH│ 右侧数学运算菜单 - 从屏幕右侧向左滑出                 │
 * │  (1-8)       │ - 包含: 8个数学运算通道(Math1-Math8)的参数设置         │
 * │              │ - 滑动方向: SliderDir_RightToLeft                    │
 * │              │ - 触发区域: 右侧Math按钮区域                           │
 * ├──────────────┼──────────────────────────────────────────────────────┤
 * │ RIGHTSLIP_REF│ 右侧参考波形菜单 - 从屏幕右侧向左滑出                  │
 * │  (1-8)       │ - 包含: 8个参考波形通道(Ref1-Ref8)的参数设置           │
 * │              │ - 滑动方向: SliderDir_RightToLeft                    │
 * │              │ - 触发区域: 右侧Ref按钮区域                            │
 * ├──────────────┼──────────────────────────────────────────────────────┤
 * │ RIGHTSLIP_S  │ 右侧串行总线菜单 - 从屏幕右侧向左滑出                  │
 * │  (1-4)       │ - 包含: 4个串行总线通道(S1-S4)的参数设置               │
 * │              │ - 滑动方向: SliderDir_RightToLeft                    │
 * │              │ - 触发区域: 右侧Serials按钮区域                        │
 * ├──────────────┼──────────────────────────────────────────────────────┤
 * │ LEFTSLIP     │ 左侧滑动菜单 - 从屏幕左侧向右滑出                      │
 * │              │ - 当前预留，未启用                                    │
 * │              │ - 滑动方向: SliderDir_LeftToRight                    │
 * │              │ - 触发区域: 屏幕左侧边缘                               │
 * └──────────────┴──────────────────────────────────────────────────────┘
 * 
 * 【依赖关系】
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │                         依赖的组件                                   │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 1. GlobalVar: 全局变量管理类，提供屏幕尺寸和通道配置                  │
 * │ 2. RxBus: RxJava事件总线，用于订阅和发布事件                          │
 * │ 3. SliderZone: 滑动区域管理类，定义滑动菜单的参数                     │
 * │ 4. Command: 命令管理类，处理菜单命令                                  │
 * │ 5. Scope: 示波器核心类，提供硬件信息和操作                            │
 * │ 6. ChannelFactory: 通道工厂类，创建和管理通道对象                     │
 * │ 7. CacheUtil: 缓存工具类，存储和读取配置                              │
 * │ 8. ScreenControls: 屏幕控制类，管理屏幕锁定等                         │
 * │ 9. ExternalKeysManager: 外部按键管理类                                │
 * │ 10. PlaySound: 音效播放类                                            │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │                         包含的子布局                                 │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 1. TopLayoutPopWindow: 顶部弹出菜单布局                               │
 * │ 2. MainTopLayoutLeft/Center/Right: 顶部栏布局                        │
 * │ 3. MainLayoutCenterChannel/Menu/Segmented/Test: 中央布局             │
 * │ 4. RightLayoutChannel/Math/Ref/Serials/Level: 右侧菜单布局           │
 * │ 5. MTriggerLevel: 触发电平线布局                                      │
 * │ 6. MSlideLayout: 滑动布局容器                                         │
 * │ 7. MainBottomMeasureItem: 底部测量项布局                              │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │                         管理的对话框                                 │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 1. DialogMeasureDelay/Phase/TValue: 测量对话框                        │
 * │ 2. DialogTopCount/Scale: 计数/缩放对话框                              │
 * │ 3. DialogTextKeyBoard/NumberKeyBoard/FloatKeyBoard: 键盘对话框       │
 * │ 4. DialogFormulaKeyBoard/FullFloatKeyBoard: 公式键盘对话框           │
 * │ 5. DialogBaudRate/BandWidth/BandWidthHz: 波特率/带宽对话框           │
 * │ 6. DialogProbeMultiple/Interface: 探头对话框                          │
 * │ 7. DialogRefRecall/LoadRefCsvWave: 参考波形对话框                     │
 * │ 8. DialogAfterGlow/FftAfterGlow: 余辉对话框                           │
 * │ 9. DialogCenterTimeBase: 时基对话框                                   │
 * │ 10. DialogOkCancel/Ok/MenuHalf: 确认对话框                            │
 * │ 11. DialogChannelLabel/SelectColor/SetChannelInfo: 通道信息对话框    │
 * │ 12. DialogMathFFTPersist: FFT持久化对话框                             │
 * │ 13. DialogMeasureStatics: 测量统计对话框                              │
 * └─────────────────────────────────────────────────────────────────────┘
 * 
 * 【使用场景】
 * 1. MainActivity创建MainViewGroup作为主界面容器
 * 2. 用户触摸屏幕时，MainViewGroup处理触摸事件并控制滑动菜单
 * 3. 用户点击通道按钮时，MainViewGroup打开对应的右侧滑动菜单
 * 4. 用户从屏幕边缘滑动时，MainViewGroup显示对应的滑动菜单
 * 5. 业务逻辑通过RxBus发送事件，MainViewGroup响应并更新界面
 * 6. 用户点击波形区域外部时，MainViewGroup隐藏所有菜单和对话框
 * 7. 工作模式切换时，MainViewGroup调整界面布局参数
 * 8. 测量行数变化时，MainViewGroup调整波形区域高度
 * 
 * 【设计模式应用】
 * 1. 容器模式: MainViewGroup作为容器，管理所有子布局的添加、移除和布局
 * 2. 观察者模式: 通过RxBus订阅事件，响应业务逻辑的变化
 * 3. 状态模式: 管理滑动菜单的显示/隐藏状态，不同状态有不同的行为
 * 4. 注解模式: 使用@Slip和@Dialog注解定义类型，提高代码可读性
 * 5. 单例模式: 部分管理器类使用单例模式（如ExternalKeysManager）
 * 
 * 【关键方法说明】
 * ┌───────────────────────────────────────────────────────────────────────┐
 * │                        触摸事件处理方法                                │
 * ├───────────────────────────────────────────────────────────────────────┤
 * │ dispatchTouchEvent(MotionEvent ev):                                   │
 * │   - 功能: 事件分发入口，处理屏幕锁定和特殊区域                         │
 * │   - 调用时机: 用户触摸屏幕时由系统调用                                 │
 * │   - 返回值: boolean - 是否消费事件                                     │
 * │                                                                        │
 * │ onEvent(MotionEvent event):                                           │
 * │   - 功能: 核心事件处理逻辑，处理滑动菜单和点击                         │
 * │   - 调用时机: dispatchTouchEvent中调用                                 │
 * │   - 返回值: boolean - 是否拦截事件                                     │
 * │                                                                        │
 * │ dealSliderZone(MotionEvent event, int x, int y):                      │
 * │   - 功能: 处理滑动菜单的拖动显示/隐藏                                   │
 * │   - 调用时机: ACTION_MOVE事件时调用                                    │
 * │   - 参数: event - 触摸事件, x/y - 当前坐标                             │
 * │                                                                        │
 * │ dealSliderZoneComplete(int x, int y):                                 │
 * │   - 功能: 处理滑动菜单的完成状态（显示或隐藏）                         │
 * │   - 调用时机: ACTION_UP事件时调用                                      │
 * │   - 参数: x/y - 当前坐标                                               │
 * │   - 返回值: boolean - 是否拦截事件                                     │
 * ├───────────────────────────────────────────────────────────────────────┤
 * │                        滑动菜单管理方法                                │
 * ├───────────────────────────────────────────────────────────────────────┤
 * │ openSlip(@Slip int slip):                                             │
 * │   - 功能: 打开指定的滑动菜单                                           │
 * │   - 参数: slip - 菜单类型（使用@Slip注解）                             │
 * │   - 调用时机: 用户点击按钮或RxBus事件触发                               │
 * │                                                                        │
 * │ hideSlip(@Slip int slip):                                             │
 * │   - 功能: 隐藏指定的滑动菜单                                           │
 * │   - 参数: slip - 菜单类型（使用@Slip注解）                             │
 * │   - 调用时机: 用户点击外部区域或RxBus事件触发                           │
 * │                                                                        │
 * │ isSlipShow(@Slip int slip):                                           │
 * │   - 功能: 检查指定滑动菜单是否显示                                     │
 * │   - 参数: slip - 菜单类型（使用@Slip注解）                             │
 * │   - 返回值: boolean - 是否显示                                         │
 * │                                                                        │
 * │ hideAllSlip():                                                        │
 * │   - 功能: 隐藏所有滑动菜单                                             │
 * │   - 调用时机: 用户点击波形区域外部                                     │
 * ├───────────────────────────────────────────────────────────────────────┤
 * │                        对话框管理方法                                  │
 * ├───────────────────────────────────────────────────────────────────────┤
 * │ isDialogShow(@Dialog int dialog):                                     │
 * │   - 功能: 检查指定对话框是否显示                                       │
 * │   - 参数: dialog - 对话框类型（使用@Dialog注解）                       │
 * │   - 返回值: boolean - 是否显示                                         │
 * │                                                                        │
 * │ hideDialog(@Dialog int dialog):                                       │
 * │   - 功能: 隐藏指定对话框                                               │
 * │   - 参数: dialog - 对话框类型（使用@Dialog注解）                       │
 * │   - 返回值: boolean - 是否成功隐藏                                     │
 * │                                                                        │
 * │ hideAllDialogsButDialogOkCancel():                                    │
 * │   - 功能: 隐藏所有对话框（除了确认对话框）                             │
 * │   - 返回值: boolean - 是否有对话框被隐藏                               │
 * ├───────────────────────────────────────────────────────────────────────┤
 * │                        RxBus事件订阅方法                               │
 * ├───────────────────────────────────────────────────────────────────────┤
 * │ consumerMainSlipFromOther:                                            │
 * │   - 功能: 处理菜单滑出/隐藏事件                                         │
 * │   - 订阅事件: RxEnum.MAIN_SLIP_FROM_OTHER                             │
 * │                                                                        │
 * │ consumerCommandToUI:                                                  │
 * │   - 功能: 处理命令消息，打开/关闭菜单                                   │
 * │   - 订阅事件: RxEnum.COMMAND_TO_UI                                    │
 * │                                                                        │
 * │ OnChanActiveChange:                                                   │
 * │   - 功能: 处理通道激活状态变化                                         │
 * │   - 订阅事件: MQEnum.CH_ACTIVE                                        │
 * └───────────────────────────────────────────────────────────────────────┘
 * 
 * 【重要注意事项】
 * 1. 所有滑动菜单互斥，打开一个菜单会自动关闭其他菜单
 * 2. 用户触摸屏幕时，外部按键功能会被禁用
 * 3. 屏幕锁定时，触摸事件会被特殊处理
 * 4. 波形区域启用/禁用状态影响触摸事件的处理
 * 5. 触发电平线有特殊的触摸处理逻辑
 * 6. 测量行数变化会动态调整波形区域高度
 * 7. 工作模式切换会影响界面布局参数
 * 
 * Created by liwb on 2017/3/27.
 */

public class MainViewGroup extends AbsoluteLayout {

    // ==================== 常量定义 ====================
    
    /**
     * 识别最小点击范围（像素值）
     * - 业务含义: 用于区分点击和滑动操作，当触摸点移动距离小于此值时认为是点击
     * - 取值范围: 正整数，单位为像素
     * - 使用场景: 在clickSlipOutsideClose方法中判断是否为点击操作
     */
    public static final int MinClickRangePx = 1000;
    
    /**
     * 最小的当做滑动的距离（像素值）
     * - 业务含义: 用于区分点击和滑动操作，当触摸点移动距离大于此值时认为是滑动
     * - 取值范围: 正整数，单位为像素
     * - 使用场景: 在dealSliderZoneComplete方法中判断是否为滑动操作
     */
    public static final int SLIDE_DISTANCE_MIN = 35;

    /**
     * 日志标签
     * - 业务含义: 用于日志输出时的类标识
     */
    private static final String TAG = "MainViewGroup";

    // ==================== 触摸事件处理相关变量 ====================
    
    /**
     * 上次拦截事件的X坐标
     * - 业务含义: 用于计算滑动距离和方向
     * - 取值范围: 屏幕坐标范围内的整数
     */
    private int lastXIntercept, lastYIntercept;
    
    /**
     * 上次触发电平控制的原始X/Y坐标
     * - 业务含义: 用于触发电平线的触摸处理
     * - 取值范围: 屏幕坐标范围内的整数
     */
    private int lastXRawTriggerControl, lastYRawTriggerControl;
    
    /**
     * 滑动区域列表
     * - 业务含义: 存储所有滑动菜单的配置参数
     * - 元素类型: SliderZone对象，包含滑动方向、触发区域、显示/隐藏状态等
     */
    private List<SliderZone> sliderZoneList = new ArrayList<SliderZone>();

    // ==================== 顶部菜单相关变量 ====================
    
    /**
     * 顶部快捷菜单弹出窗口
     * - 业务含义: 顶部滑动菜单的主布局，包含触发、采样、测量、显示等子菜单
     * - 关联布局: TopLayoutPopWindow
     */
    private TopLayoutPopWindow mTopBar_quick;

    /**
     * 获取顶部主菜单
     * @return 顶部快捷菜单弹出窗口对象
     */
    public TopLayoutPopWindow getMainMenu(){return mTopBar_quick;}

    // ==================== 底部菜单相关变量 ====================
    
    /**
     * 底部右侧通道按钮区域
     * - 业务含义: 底部菜单中通道相关的按钮区域
     */
    private RelativeLayout bottomRightChannel;
    
    /**
     * 底部右侧其他按钮区域
     * - 业务含义: 底部菜单中Math/Ref/Serials等按钮区域
     */
    private RelativeLayout bottomRightOther;
    
    /**
     * 左侧屏幕按钮
     * - 业务含义: 屏幕左侧的操作按钮
     */
    private Button btnLeftScreen;
    
    /**
     * 顶部右侧按钮布局
     * - 业务含义: 顶部栏右侧的按钮区域
     */
    private MainTopLayoutRightButton mainTopLayoutRightButton;
    
    /**
     * 顶部左侧布局
     * - 业务含义: 顶部栏左侧的布局区域
     */
    private MainTopLayoutLeft mainTopLayoutLeft;
    
    /**
     * 底部快捷菜单布局
     * - 业务含义: 底部滑动菜单的主布局
     */
    private ConstraintLayout mBottomBar_quick;

    // ==================== 右侧通道菜单相关变量 ====================
    
    /**
     * 右侧通道菜单布局（CH1-CH8）
     * - 业务含义: 8个模拟通道的参数设置菜单
     * - 关联布局: RightLayoutChannel
     */
    private RightLayoutChannel mRightBar_channel1, mRightBar_channel2, mRightBar_channel3, mRightBar_channel4, mRightBar_channel5, mRightBar_channel6, mRightBar_channel7, mRightBar_channel8;
    
    /**
     * 右侧数学运算菜单布局（Math1-Math8）
     * - 业务含义: 8个数学运算通道的参数设置菜单
     * - 关联布局: RightLayoutMath
     */
    public RightLayoutMath mRightBar_math1, mRightBar_math2, mRightBar_math3, mRightBar_math4, mRightBar_math5, mRightBar_math6, mRightBar_math7, mRightBar_math8;
    
    /**
     * 数学运算通道的主控制按钮（Math1-Math8）
     * - 业务含义: 右侧Math按钮区域的主控制按钮
     */
    private MainRightLayoutItemChannelMaster math1Master, math2Master, math3Master, math4Master, math5Master, math6Master, math7Master, math8Master;

    /**
     * 右侧参考波形菜单布局（Ref1-Ref8）
     * - 业务含义: 8个参考波形通道的参数设置菜单
     * - 关联布局: RightLayoutRef
     */
    public RightLayoutRef mRightBar_ref1, mRightBar_ref2, mRightBar_ref3, mRightBar_ref4, mRightBar_ref5, mRightBar_ref6, mRightBar_ref7, mRightBar_ref8;
    
    /**
     * 右侧串行总线菜单布局（S1-S4）
     * - 业务含义: 4个串行总线通道的参数设置菜单
     * - 关联布局: RightLayoutSerials
     */
    public RightLayoutSerials mRightBar_serials1, mRightBar_serials2, mRightBar_serials3, mRightBar_serials4;
    
    /**
     * 右侧电平菜单布局
     * - 业务含义: 触发电平设置菜单
     * - 关联布局: RightLayoutLevel
     */
    private RightLayoutLevel mRightBar_level;

    // ==================== 滑动区域配置变量 ====================
    
    /**
     * 滑动区域配置对象
     * - 业务含义: 定义各滑动菜单的触发区域、滑动方向、显示/隐藏状态
     * - 关联类: SliderZone
     */
    private SliderZone sliderZone_bottom, sliderZone_top, sliderZone_left, sliderZone_right;
    
    /**
     * 右侧通道滑动区域配置（CH1-CH8）
     * - 业务含义: 定义8个模拟通道菜单的滑动参数
     */
    private SliderZone sliderZone_rightCh1, sliderZone_rightCh2, sliderZone_rightCh3, sliderZone_rightCh4, sliderZone_rightCh5, sliderZone_rightCh6, sliderZone_rightCh7, sliderZone_rightCh8;
    
    /**
     * 右侧串行总线滑动区域配置（S1-S4）
     * - 业务含义: 定义4个串行总线菜单的滑动参数
     */
    public SliderZone sliderZone_rightS1, sliderZone_rightS2, sliderZone_rightS3, sliderZone_rightS4;

    /**
     * 右侧数学运算滑动区域配置（Math1-Math8）
     * - 业务含义: 定义8个数学运算菜单的滑动参数
     */
    public SliderZone sliderZone_rightMath1, sliderZone_rightMath2, sliderZone_rightMath3, sliderZone_rightMath4, sliderZone_rightMath5, sliderZone_rightMath6, sliderZone_rightMath7, sliderZone_rightMath8;

    /**
     * 右侧参考波形滑动区域配置（Ref1-Ref8）
     * - 业务含义: 定义8个参考波形菜单的滑动参数
     */
    public SliderZone sliderZone_rightRef1, sliderZone_rightRef2, sliderZone_rightRef3, sliderZone_rightRef4, sliderZone_rightRef5, sliderZone_rightRef6, sliderZone_rightRef7, sliderZone_rightRef8;

    // ==================== 中央布局相关变量 ====================
    
    /**
     * 中央测试布局
     * - 业务含义: 用于测试功能的中央布局
     */
    private MainLayoutCenterTest test;
    
    /**
     * 中央通道选择布局
     * - 业务含义: 显示通道选择条的中央布局
     */
    private MainLayoutCenterChannel channelsLayout;
    
    /**
     * 中央菜单布局
     * - 业务含义: 中央区域的菜单布局
     */
    private MainLayoutCenterMenu centerMenuLayout;
    
    /**
     * 中央分段存储布局
     * - 业务含义: 分段存储功能的中央布局
     */
    private MainLayoutCenterSegmented centerSegmentedLayout;
    
    /**
     * 触发电平线布局
     * - 业务含义: 显示触发电平线的可拖动布局
     */
    private MTriggerLevel triggerLevel;
    
    /**
     * 电平滑动左侧区域
     * - 业务含义: 触发电平线左侧的滑动区域
     */
    private View levelSwipeLeft;
    
    /**
     * 主滑动外部视图
     * - 业务含义: 用于控制波形区域的启用/禁用状态
     * - 使用场景: 当滑动菜单显示时，此视图可见，禁用波形区域的触摸操作
     */
    private View mainSlipOutView;

    /**
     * 测量菜单按钮
     * - 业务含义: 打开测量菜单的按钮
     */
    private Button measureMenu;

    // ==================== 对话框相关变量 ====================
    
    /**
     * 测量延迟对话框
     * - 业务含义: 设置测量延迟参数的对话框
     */
    private TopDialogMeasureDelay dialogMeasureDelay;
    
    /**
     * 测量相位对话框
     * - 业务含义: 设置测量相位参数的对话框
     */
    private TopDialogMeasurePhase dialogMeasurePhase;
    
    /**
     * 测量T值对话框
     * - 业务含义: 设置测量T值参数的对话框
     */
    private TopDialogMeasureTValue dialogMeasureTValue;
    
    /**
     * 计数对话框
     * - 业务含义: 设置计数参数的对话框
     */
    private TopDialogCount dialogTopCount;
    
    /**
     * 缩放对话框
     * - 业务含义: 设置缩放参数的对话框
     */
    private TopDialogScale dialogTopScale;
    
    /**
     * 文本键盘对话框
     * - 业务含义: 输入文本的键盘对话框
     */
    private TopDialogTextKeyBoard dialogTextKeyBoard;
    
    /**
     * 数字键盘对话框
     * - 业务含义: 输入数字的键盘对话框
     */
    private TopDialogNumberKeyBoard dialogNumberKeyBoard;
    
    /**
     * 浮点数键盘对话框
     * - 业务含义: 输入浮点数的键盘对话框
     */
    private TopDialogFloatKeyBoard dialogFloatKeyBoard;
    
    /**
     * 完整浮点数键盘对话框
     * - 业务含义: 输入完整浮点数的键盘对话框
     */
    private TopDialogFullFloatKeyBoard dialogFullFloatKeyBoard;
    
    /**
     * 公式键盘对话框
     * - 业务含义: 输入数学公式的键盘对话框
     */
    private TopDialogFormulaKeyBoard dialogFormulaKeyBoard;
    
    /**
     * 数字选择器对话框
     * - 业务含义: 选择数字的对话框
     */
    private TopDialogNumberPicker dialogNumberPicker;
    
    /**
     * 波特率对话框
     * - 业务含义: 设置串行通信波特率的对话框
     */
    private DialogBaudRate dialogBaudRate;
    
    /**
     * 带宽对话框
     * - 业务含义: 设置通道带宽限制的对话框
     */
    private DialogBandWidth dialogBandWidth;
    
    /**
     * 探头倍数对话框
     * - 业务含义: 设置探头衰减倍数的对话框
     */
    private DialogProbeMultiple dialogProbeMultiple;
    
    /**
     * FFT持久化对话框
     * - 业务含义: 设置FFT波形持久化的对话框
     */
    private DialogMathFFTPersist dialogMathFFTPersist;
    
    /**
     * 探头接口对话框
     * - 业务含义: 设置探头接口类型的对话框
     */
    private DialogProbeInterface dialogProbeInterface;
    
    /**
     * 参考波形回调对话框
     * - 业务含义: 选择回调参考波形的对话框
     */
    private DialogRefRecall dialogRefRecall;
    
    /**
     * 余辉对话框
     * - 业务含义: 设置波形余辉效果的对话框
     */
    private TopViewSelectHorListToList dialogAfterGlow, dialogFftAfterGlow;
    
    /**
     * 中央时基对话框
     * - 业务含义: 设置时基参数的中央对话框
     */
    private MainLayoutCenterTimeBase dialogCenterTimeBase;
    
    /**
     * 确认取消对话框
     * - 业务含义: 双按钮确认对话框
     */
    private DialogOkCancel dialogOkCancel;
    
    /**
     * 确认对话框
     * - 业务含义: 单按钮确认对话框
     */
    private DialogOk dialogOk;
    
    /**
     * 半屏菜单对话框
     * - 业务含义: 显示半屏菜单的对话框
     */
    private MainDialogMenuHalf dialogMenuHalf;
    
    /**
     * 通道标签对话框
     * - 业务含义: 设置通道标签的对话框
     */
    private DialogChannelLabel dialogChannelLabel;
    
    /**
     * 带宽Hz对话框
     * - 业务含义: 设置带宽频率的对话框
     */
    private DialogBandWidthHz dialogBandWidthHz;
    
    /**
     * 加载参考波形CSV对话框
     * - 业务含义: 从CSV文件加载参考波形的对话框
     */
    private DialogLoadRefCsvWave dialogLoadRefCsvWave;
    
    /**
     * 颜色选择对话框
     * - 业务含义: 选择通道颜色的对话框
     */
    private DialogSelectColor dialogSelectColor;
    
    /**
     * 设置通道信息对话框
     * - 业务含义: 设置通道详细信息的对话框
     */
    private DialogSetChannelInfo dialogSetChannelInfo;

    // ==================== 其他布局相关变量 ====================
    
    /**
     * 主右侧布局
     * - 业务含义: 右侧区域的容器布局
     */
    private MainRightLayout mainRightLayout;
    
    /**
     * 滑动布局容器
     * - 业务含义: 可滑动的布局容器，用于右侧菜单的上下滑动
     */
    private MSlideLayout mSlideLayout;

    /**
     * Toast提示文本
     * - 业务含义: 显示临时提示信息的文本视图
     */
    private TextView tvToast;

    /**
     * 测量统计对话框
     * - 业务含义: 显示测量统计数据的对话框
     */
    private DialogMeasureStatics dialogMeasurestatics;

    // ==================== 速度追踪相关变量 ====================
    
    /**
     * 速度追踪器
     * - 业务含义: 用于追踪触摸事件的滑动速度
     * - 使用场景: 在滑动菜单处理中判断快速滑动
     */
    private VelocityTracker vTracker = null;
    
    /**
     * 最小速度阈值
     * - 业务含义: 用于判断是否为快速滑动的速度阈值
     * - 取值范围: 正浮点数，单位为像素/秒
     */
    private float VELOCITY_MIN = 100;
    
    /**
     * X/Y方向的速度
     * - 业务含义: 记录触摸事件的滑动速度
     * - 取值范围: 浮点数，正负表示方向
     */
    private float velocityX, velocityY;
    
    /**
     * 触摸点ID
     * - 业务含义: 用于多点触控时追踪特定触摸点
     */
    private int pointerId;
    
    /**
     * 最大速度
     * - 业务含义: 系统允许的最大滑动速度
     */
    private int maxVelocity;

    // ==================== 视图管理相关变量 ====================
    
    /**
     * 透明视图列表
     * - 业务含义: 存储需要设置透明度的视图
     * - 使用场景: 在显示设置中调整界面透明度
     */
    private List<View> listTransparencyView = new ArrayList<>();
    
    /**
     * 顶部菜单弹出窗口
     * - 业务含义: 顶部菜单的弹出窗口布局
     */
    private TopLayoutPopWindow topMenu;
    
    /**
     * 顶部右侧布局
     * - 业务含义: 顶部栏右侧的布局
     */
    private MainTopLayoutRight mainTopRight;
    
    /**
     * 顶部中央布局
     * - 业务含义: 顶部栏中央的布局
     */
    private MainTopLayoutCenter mainTopLayoutCenter;
    
    /**
     * 状态栏布局
     * - 业务含义: 显示状态信息的布局
     */
    private ConstraintLayout statusBar;
    
    /**
     * 简要显示文本（CH1-CH4）
     * - 业务含义: 显示通道简要信息的文本视图
     */
    private TextView briefDisplayCh1, briefDisplayCh2, briefDisplayCh3, briefDisplayCh4;
    
    /**
     * 通道垂直缩放布局
     * - 业务含义: 显示通道垂直缩放参数的布局
     */
    private MainChannelVerticalScale mainChannelVerticalScale;
    
    /**
     * 列表视图容器
     * - 业务含义: 用于显示列表内容的容器
     */
    private AbsoluteLayout rlListView;
    
    /**
     * 纹理视图
     * - 业务含义: 用于显示波形图像的纹理视图
     */
    private TextureView textureView;
    
    /**
     * 空间视图和空视图
     * - 业务含义: 用于布局占位和特殊处理的视图
     */
    private View viewSpace, nullView;

    /**
     * 停止自动保存按钮
     * - 业务含义: 用于停止自动保存功能的按钮
     */
    private LinearLayout btnStopAutoSave;
    
    /**
     * 底部测量项布局
     * - 业务含义: 显示测量项的底部布局
     */
    private MainBottomMeasureItem mainBottomMeasureItem;
    
    /**
     * 8通道右侧布局和触发电平布局
     * - 业务含义: 8通道模式的右侧布局和触发电平容器
     */
    private ConstraintLayout mainRightChannelLayoutEight,clTriggerLevel;

    // ==================== 通道配置相关变量 ====================
    
    /**
     * 通道数量
     * - 业务含义: 当前示波器的通道数量（2/4/8）
     * - 取值范围: GlobalVar.CHANNEL_COUNT_2/4/8
     */
    private final int channelCount = GlobalVar.get().getChannelsCount();
    
    /**
     * 数学运算菜单列表
     * - 业务含义: 存储所有数学运算菜单布局
     * - 元素数量: 最多8个
     */
    private final List<RightLayoutMath> mRightBarMaths = new ArrayList<>(GlobalVar.CHANNEL_COUNT_8);
    
    /**
     * 数学运算滑动区域列表
     * - 业务含义: 存储所有数学运算滑动区域配置
     * - 元素数量: 最多8个
     */
    private final List<SliderZone> mSlideZoneMaths = new ArrayList<>(GlobalVar.CHANNEL_COUNT_8);
    
    /**
     * 数学运算主控制按钮列表
     * - 业务含义: 存储所有数学运算主控制按钮
     * - 元素数量: 最多8个
     */
    private final List<MainRightLayoutItemChannelMaster> mathMasters = new ArrayList<>(GlobalVar.CHANNEL_COUNT_8);

    /**
     * 参考波形菜单列表
     * - 业务含义: 存储所有参考波形菜单布局
     * - 元素数量: 最多8个
     */
    private final List<RightLayoutRef> mRightBarRefs = new ArrayList<>(GlobalVar.CHANNEL_COUNT_8);
    
    /**
     * 参考波形滑动区域列表
     * - 业务含义: 存储所有参考波形滑动区域配置
     * - 元素数量: 最多8个
     */
    private final List<SliderZone> mSlideZoneRefs = new ArrayList<>(GlobalVar.CHANNEL_COUNT_8);
    
    /**
     * 参考波形主控制按钮列表
     * - 业务含义: 存储所有参考波形主控制按钮
     * - 元素数量: 最多8个
     */
    private final List<MainRightLayoutItemChannelMaster> refMasters = new ArrayList<>(GlobalVar.CHANNEL_COUNT_8);

    /**
     * 串行总线菜单列表
     * - 业务含义: 存储所有串行总线菜单布局
     * - 元素数量: 最多4个
     */
    private final List<RightLayoutSerials> mRightBarBus = new ArrayList<>(4);
    
    /**
     * 串行总线滑动区域列表
     * - 业务含义: 存储所有串行总线滑动区域配置
     * - 元素数量: 最多4个
     */
    private final List<SliderZone> mSlideZoneBus = new ArrayList<>(4);
    
    /**
     * 串行总线主控制按钮列表
     * - 业务含义: 存储所有串行总线主控制按钮
     * - 元素数量: 最多4个
     */
    private final List<MainRightLayoutItemSerialsMaster> busMasters = new ArrayList<>(4);

    //region 写好的。不用动

    private Context context;
    
    /**
     * 构造函数1：单参数构造函数
     * 
     * @param context Android上下文对象，用于获取资源和系统服务
     * @description 通过调用双参数构造函数创建MainViewGroup实例
     * @调用时机 在代码中动态创建MainViewGroup时使用
     */
    public MainViewGroup(Context context) {
        this(context, null);
    }

    /**
     * 构造函数2：双参数构造函数
     * 
     * @param context Android上下文对象，用于获取资源和系统服务
     * @param attrs XML属性集合，从布局文件中读取的属性
     * @description 通过调用三参数构造函数创建MainViewGroup实例
     * @调用时机 在XML布局文件中定义MainViewGroup时由系统调用
     */
    public MainViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造函数3：三参数构造函数（主构造函数）
     * 
     * @param context Android上下文对象，用于获取资源和系统服务
     * @param attrs XML属性集合，从布局文件中读取的属性
     * @param defStyleAttr 默认样式属性
     * @description MainViewGroup的主构造函数，完成以下初始化工作：
     *              1. 加载布局文件 activity_main2.xml
     *              2. 初始化所有子布局组件（顶部、底部、中央、右侧）
     *              3. 根据通道数量（2/4/8）配置界面布局
     *              4. 初始化所有对话框组件
     *              5. 初始化RxBus事件订阅
     *              6. 配置右侧滑动菜单
     *              7. 配置触发电平菜单
     *              8. 设置点击事件监听器
     * @调用时机 在创建MainViewGroup实例时最终调用此构造函数
     * @关键步骤 
     *              - inflate布局文件并设置背景色
     *              - 初始化通道数量相关的布局（2通道、4通道、8通道）
     *              - 初始化Math/Ref/Serials的主控制按钮列表
     *              - 调用initFindDialog初始化所有对话框
     *              - 调用initControl订阅RxBus事件
     *              - 调用setRightScroll配置右侧滑动布局
     *              - 调用setLevelMenu配置触发电平菜单
     */
    public MainViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造函数
        this.context = context; // 保存上下文引用
        View view = inflate(context, R.layout.activity_main2, this); // 加载布局文件

        setBackgroundResource(R.color.bg_main_outside); // 设置背景色为示波器主背景色
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT); // 设置布局参数为全屏
        this.setLayoutParams(lp);

        // 初始化中央布局组件
        channelsLayout = (MainLayoutCenterChannel) view.findViewById(R.id.mainLayoutCenterChannels); // 通道选择布局
        test = (MainLayoutCenterTest)view.findViewById(R.id.mainLayoutCenterTests); // 测试布局
        centerMenuLayout = (MainLayoutCenterMenu) view.findViewById(R.id.mainLayoutCenterMenu); // 中央菜单布局
        centerSegmentedLayout = (MainLayoutCenterSegmented) view.findViewById(R.id.mainLayoutCenterSegmented); // 分段存储布局
        triggerLevel = (MTriggerLevel) view.findViewById(R.id.triggerLevel); // 触发电平线布局
        levelSwipeLeft = (View) view.findViewById(R.id.levelSwipeLeft); // 电平滑动左侧区域
        tvToast = (TextView) view.findViewById(R.id.toast); // Toast提示文本
        mainChannelVerticalScale = view.findViewById(R.id.main_chan_vertical_scale); // 通道垂直缩放布局
        triggerLevel.setChannelCount(channelCount); // 设置触发电平线的通道数量
        maxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity(); // 获取系统最大滑动速度
        mainRightChannelLayoutEight = view.findViewById(R.id.mainRightChannelLayoutEight); // 8通道右侧布局
        
        // 根据通道数量配置界面布局
        if (channelCount == GlobalVar.CHANNEL_COUNT_2) { // 2通道模式
            findViewById(R.id.mainRightDoubleChannelLayout).setVisibility(VISIBLE); // 显示双通道布局
            findViewById(R.id.mainRightChannelLayout).setVisibility(GONE); // 隐藏4通道布局
            mainRightChannelLayoutEight.setVisibility(GONE); // 隐藏8通道布局
        } else if (channelCount == GlobalVar.CHANNEL_COUNT_4) { // 4通道模式
            findViewById(R.id.mainRightDoubleChannelLayout).setVisibility(GONE); // 隐藏双通道布局
            findViewById(R.id.mainRightChannelLayout).setVisibility(VISIBLE); // 显示4通道布局
            mainRightChannelLayoutEight.setVisibility(GONE); // 隐藏8通道布局
        } else { // 8通道模式
            findViewById(R.id.mainRightDoubleChannelLayout).setVisibility(GONE); // 隐藏双通道布局
            findViewById(R.id.mainRightChannelLayout).setVisibility(GONE); // 隐藏4通道布局
            mainRightChannelLayoutEight.setVisibility(VISIBLE); // 显示8通道布局
        }
        
        // 初始化其他布局组件
        mainSlipOutView = findViewById(R.id.mainSlipOutView); // 主滑动外部视图
        mainTopLayoutLeft=findViewById(R.id.mainTopLayoutLeft); // 顶部左侧布局
        mainTopLayoutLeft.setMainViewGroup(this); // 设置MainViewGroup引用
        rlListView = view.findViewById(R.id.rlListView); // 列表视图容器
        textureView = view.findViewById(R.id.textureView); // 纹理视图（用于显示波形）
        viewSpace = view.findViewById(R.id.viewSpace); // 空间视图
        nullView = view.findViewById(R.id.nullView); // 空视图（用于占位）
        mainBottomMeasureItem = view.findViewById(R.id.bottom_measure_item); // 底部测量项布局
        clTriggerLevel = view.findViewById(R.id.cl_triggerLevel); // 触发电平容器布局

        // 初始化简要显示文本（根据通道数量）
        if (channelCount == GlobalVar.CHANNEL_COUNT_4) { // 4通道模式
            briefDisplayCh1 = findViewById(R.id.briefDisplayTextFourCh1); // CH1简要显示
            briefDisplayCh2 = findViewById(R.id.briefDisplayTextFourCh2); // CH2简要显示
            briefDisplayCh3 = findViewById(R.id.briefDisplayTextFourCh3); // CH3简要显示
            briefDisplayCh4 = findViewById(R.id.briefDisplayTextFourCh4); // CH4简要显示
        } else { // 2通道模式
            briefDisplayCh1 = findViewById(R.id.briefDisplayTextDoubleCh1); // CH1简要显示
            briefDisplayCh2 = findViewById(R.id.briefDisplayTextDoubleCh2); // CH2简要显示
        }
        
        // 初始化Math主控制按钮列表（Math1-Math8）
        mathMasters.add(findViewById(R.id.math1Master));
        mathMasters.add(findViewById(R.id.math2Master));
        mathMasters.add(findViewById(R.id.math3Master));
        mathMasters.add(findViewById(R.id.math4Master));
        mathMasters.add(findViewById(R.id.math5Master));
        mathMasters.add(findViewById(R.id.math6Master));
        mathMasters.add(findViewById(R.id.math7Master));
        mathMasters.add(findViewById(R.id.math8Master));
        
        // 初始化Ref主控制按钮列表（Ref1-Ref8）
        refMasters.add(findViewById(R.id.ref1Master));
        refMasters.add(findViewById(R.id.ref2Master));
        refMasters.add(findViewById(R.id.ref3Master));
        refMasters.add(findViewById(R.id.ref4Master));
        refMasters.add(findViewById(R.id.ref5Master));
        refMasters.add(findViewById(R.id.ref6Master));
        refMasters.add(findViewById(R.id.ref7Master));
        refMasters.add(findViewById(R.id.ref8Master));
        
        // 初始化Serials主控制按钮列表（S1-S4）
        busMasters.add(findViewById(R.id.rightS1Master));
        busMasters.add(findViewById(R.id.rightS2Master));
        busMasters.add(findViewById(R.id.rightS3Master));
        busMasters.add(findViewById(R.id.rightS4Master));

        setEnableWaveOption(true); // 启用波形区域操作
        initFindDialog(view); // 初始化所有对话框
        initControl(); // 初始化RxBus事件订阅
        setRightScroll(); // 配置右侧滑动布局
        setLevelMenu(); // 配置触发电平菜单

        // 设置波形XY区域外部点击监听器
        findViewById(R.id.waveXYOutView).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideAllDialogsButDialogOkCancel(); // 隐藏所有对话框（除了确认对话框）
            }
        });

        Command.get().getMenu().initMainViewGroup(this); // 初始化命令菜单系统

        // 设置空视图点击监听器（用于占位，不处理任何操作）
        nullView.setOnClickListener((v) -> {
            Logger.i(TAG, "click nullView"); // 记录日志
        });

        // 设置测量菜单按钮点击监听器
        measureMenu=findViewById(R.id.mainTopMeasureMenu);
        measureMenu.setOnClickListener((v)->{
            OnBtnMeasureClick(); // 打开测量菜单
        });

        // 设置顶部右侧按钮点击监听器
        mainTopRight=findViewById(R.id.mainTopRight);
        mainTopRight.OnClickEvent=(v)->{
            if (topMenu==null) { // 如果顶部菜单未初始化
                topMenu = (TopLayoutPopWindow) findViewById(R.id.topSlipMenuBar_Quick); // 初始化顶部菜单
            }
            if (isSlipShow(TOPSLIP)==false || topMenu.isShowLayoutTrigger()==false) { // 如果顶部菜单未显示或触发菜单未显示
                topMenu.showLayoutTrigger(); // 显示触发菜单
                openSlip(TOPSLIP); // 打开顶部滑动菜单
            } else { // 如果顶部菜单已显示
                hideSlip(TOPSLIP); // 隐藏顶部滑动菜单
            }
        };

        // 设置状态栏点击监听器
        statusBar=findViewById(R.id.statusBar);
        statusBar.setOnClickListener((v)->{
            MainMsgCenterMenuCommand command= new MainMsgCenterMenuCommand(MainMsgCenterMenuCommand.CommandReturnHome); // 创建返回主页命令
            RxBus.getInstance().post(RxEnum.MainLeft_To_Menu_Command,command); // 发送命令到RxBus
        });

        // 设置顶部中央布局点击监听器
        mainTopLayoutCenter=findViewById(R.id.mainTopLayoutCenter);
        mainTopLayoutCenter.OnClickEvent = ((isDoubleClick) -> {
            if (topMenu==null) { // 如果顶部菜单未初始化
                topMenu = (TopLayoutPopWindow) findViewById(R.id.topSlipMenuBar_Quick); // 初始化顶部菜单
            }
            Logger.d(TAG, "isDoubleClick= " + isDoubleClick); // 记录日志
            if (isDoubleClick) { // 如果是双击
                hideSlip(TOPSLIP); // 隐藏顶部滑动菜单
                RxBus.getInstance().post(RxEnum.MQ_MSG_MOUSE_CLICK_POSITION, ChannelFactory.getChActivate() + ";" + 4); // 发送鼠标点击位置消息
            } else { // 如果是单击
                if (isSlipShow(TOPSLIP) == false || topMenu.isShowLayoutSample() == false) { // 如果顶部菜单未显示或采样菜单未显示
                    topMenu.showLayoutSample(); // 显示采样菜单
                    openSlip(TOPSLIP); // 打开顶部滑动菜单
                } else { // 如果顶部菜单已显示
                    hideSlip(TOPSLIP); // 隐藏顶部滑动菜单
                }
            }
        });

        btnStopAutoSave = findViewById(R.id.stopAutoSave); // 初始化停止自动保存按钮
    }

    /**
     * 测量菜单按钮点击处理方法
     * 
     * @description 处理测量菜单按钮的点击事件，打开或关闭测量菜单
     * @调用时机 用户点击测量菜单按钮时调用
     * @业务逻辑 如果顶部菜单未显示或测量菜单未显示，则打开测量菜单；否则关闭顶部菜单
     */
    public void OnBtnMeasureClick(){
        if (topMenu==null) { // 如果顶部菜单未初始化
            topMenu = (TopLayoutPopWindow) findViewById(R.id.topSlipMenuBar_Quick); // 初始化顶部菜单
        }
        if (isSlipShow(TOPSLIP)==false || topMenu.isShowLayoutMeasureCommon()==false) { // 如果顶部菜单未显示或测量菜单未显示
            topMenu.showLayoutMeasure(); // 显示测量菜单
            openSlip(TOPSLIP); // 打开顶部滑动菜单
        } else { // 如果顶部菜单已显示
            hideSlip(TOPSLIP); // 隐藏顶部滑动菜单
        }
    }

    /**
     * 初始化透明度视图列表
     * 
     * @description 将所有需要支持透明度调整的视图添加到列表中
     *              包括：顶部菜单、中央布局、右侧通道菜单、数学运算菜单、参考波形菜单、串行总线菜单、对话框等
     * @调用时机 在initLayout方法中调用，初始化完成后可通过setViewTransparency方法统一调整透明度
     * @业务场景 用于显示设置中的界面透明度调整功能
     * @关联方法 setViewTransparency - 设置视图透明度
     */
    void initTransparencyView(){
        // 添加顶部菜单相关视图
        listTransparencyView.add(mTopBar_quick); // 顶部快捷菜单
        listTransparencyView.add(dialogCenterTimeBase); // 中央时基对话框

        listTransparencyView.add(channelsLayout);
        listTransparencyView.add(centerMenuLayout);
        listTransparencyView.add(centerSegmentedLayout);
        listTransparencyView.add(test);

        listTransparencyView.add(mRightBar_channel1);
        listTransparencyView.add(mRightBar_channel2);
        listTransparencyView.add(mRightBar_channel3);
        listTransparencyView.add(mRightBar_channel4);
        listTransparencyView.add(mRightBar_channel5);
        listTransparencyView.add(mRightBar_channel6);
        listTransparencyView.add(mRightBar_channel7);
        listTransparencyView.add(mRightBar_channel8);
        listTransparencyView.add(mRightBar_math1);
        listTransparencyView.add(mRightBar_math2);
        listTransparencyView.add(mRightBar_math3);
        listTransparencyView.add(mRightBar_math4);
        listTransparencyView.add(mRightBar_math5);
        listTransparencyView.add(mRightBar_math6);
        listTransparencyView.add(mRightBar_math7);
        listTransparencyView.add(mRightBar_math8);
        listTransparencyView.add(mRightBar_ref1);
        listTransparencyView.add(mRightBar_ref2);
        listTransparencyView.add(mRightBar_ref3);
        listTransparencyView.add(mRightBar_ref4);
        listTransparencyView.add(mRightBar_ref5);
        listTransparencyView.add(mRightBar_ref6);
        listTransparencyView.add(mRightBar_ref7);
        listTransparencyView.add(mRightBar_ref8);
        listTransparencyView.add(mRightBar_serials1);
        listTransparencyView.add(mRightBar_serials2);
        listTransparencyView.add(mRightBar_serials3);
        listTransparencyView.add(mRightBar_serials4);

        listTransparencyView.add(dialogTextKeyBoard);
        listTransparencyView.add(dialogMeasureDelay);
        listTransparencyView.add(dialogMeasurePhase);
        listTransparencyView.add(dialogMeasureTValue);
        listTransparencyView.add(dialogTopCount);
        listTransparencyView.add(dialogTopScale);
        listTransparencyView.add(dialogBaudRate);
        listTransparencyView.add(dialogNumberKeyBoard);
        listTransparencyView.add(dialogFloatKeyBoard);
        listTransparencyView.add(dialogFormulaKeyBoard);
        listTransparencyView.add(dialogNumberPicker);
        listTransparencyView.add(dialogBandWidth);
        listTransparencyView.add(dialogProbeMultiple);
        listTransparencyView.add(dialogRefRecall);
        listTransparencyView.add(dialogAfterGlow);
        listTransparencyView.add(dialogFftAfterGlow);
        listTransparencyView.add(dialogCenterTimeBase);
        listTransparencyView.add(dialogOkCancel);
        listTransparencyView.add(dialogOk);
        listTransparencyView.add(dialogMenuHalf);
        listTransparencyView.add(dialogChannelLabel);
        listTransparencyView.add(dialogBandWidthHz);
        listTransparencyView.add(dialogMathFFTPersist);
        listTransparencyView.add(dialogProbeInterface);
        listTransparencyView.add(tvToast);
        listTransparencyView.add(dialogMeasurestatics);
        listTransparencyView.add(mRightBar_level);
        listTransparencyView.add(dialogLoadRefCsvWave);
        listTransparencyView.add(dialogSelectColor);
        listTransparencyView.add(dialogSetChannelInfo);
        listTransparencyView.add(dialogFullFloatKeyBoard);
    }
    /**
     * 设置视图透明度
     * 
     * @param alpha 透明度值（0.0-1.0），0.0表示完全透明，1.0表示完全不透明
     * @description 统一设置所有视图的透明度，用于显示设置中的界面透明度调整功能
     *              当前代码已注释，功能暂时禁用
     * @调用时机 通过RxBus接收TopMsgDisplay事件时调用
     * @业务场景 用户在显示设置中调整界面透明度时触发
     * @关联方法 initTransparencyView - 初始化透明度视图列表
     * @RxBus订阅 consumerDisplay - 订阅TopMsgDisplay事件
     */
    public void setViewTransparency(float alpha){
//        for (View view:listTransparencyView){
//            view.setAlpha(alpha);
//        }
    }

    /**
     * 初始化所有对话框组件
     * 
     * @param view 主布局视图，用于查找对话框组件
     * @description 初始化示波器应用中使用的所有对话框组件，包括：
     *              1. 键盘类对话框：文本键盘、数字键盘、浮点数键盘、公式键盘、完整浮点数键盘
     *              2. 测量类对话框：测量延迟、测量相位、测量T值、测量统计
     *              3. 通道参数对话框：波特率、带宽、探头倍数、探头接口、通道标签、颜色选择、通道信息
     *              4. 参考波形对话框：参考波形回调、加载CSV波形
     *              5. 显示设置对话框：余辉、FFT余辉
     *              6. 时基对话框：中央时基
     *              7. 确认类对话框：确认取消、确认、半屏菜单
     * @调用时机 在构造函数中调用，完成所有对话框的初始化
     * @对话框数量 共28种对话框（使用@Dialog注解定义）
     * @关联方法 isDialogShow - 检查对话框是否显示
     * @关联方法 hideDialog - 隐藏指定对话框
     * @关联方法 hideAllDialogsButDialogOkCancel - 隐藏所有对话框（除了确认对话框）
     */
    private void initFindDialog(View view) {
        // 初始化键盘类对话框
        dialogTextKeyBoard = (TopDialogTextKeyBoard) findViewById(R.id.dialogTextKeyBoard); // 文本输入键盘
        dialogNumberKeyBoard = (TopDialogNumberKeyBoard) findViewById(R.id.dialogNumberKeyBoard); // 数字输入键盘
        dialogFloatKeyBoard = (TopDialogFloatKeyBoard) findViewById(R.id.dialogFloatKeyBoard); // 浮点数输入键盘
        dialogFullFloatKeyBoard = (TopDialogFullFloatKeyBoard) findViewById(R.id.dialogFullFloatKeyBoard); // 完整浮点数键盘
        dialogFormulaKeyBoard = (TopDialogFormulaKeyBoard) findViewById(R.id.dialogFormulaKeyBoard); // 公式输入键盘
        dialogNumberPicker = (TopDialogNumberPicker) findViewById(R.id.dialogNumberPicker); // 数字选择器
        
        // 初始化测量类对话框
        dialogMeasureDelay = (TopDialogMeasureDelay) findViewById(R.id.dialogMeasureDelay); // 测量延迟设置
        dialogMeasurePhase = (TopDialogMeasurePhase) findViewById(R.id.dialogMeasurePhase); // 测量相位设置
        dialogMeasureTValue = (TopDialogMeasureTValue) findViewById(R.id.dialogMeasureTValue); // 测量T值设置
        dialogMeasurestatics =findViewById(R.id.dialogMeasureStatics); // 测量统计显示
        
        // 初始化计数和缩放对话框
        dialogTopCount = (TopDialogCount) findViewById(R.id.dialogTopCount); // 计数设置
        dialogTopScale = (TopDialogScale) findViewById(R.id.dialogTopScale); // 缩放设置
        
        // 初始化通道参数对话框
        dialogBaudRate = (DialogBaudRate) findViewById(R.id.dialogBaudRate); // 波特率设置
        dialogBandWidth = (DialogBandWidth) findViewById(R.id.dialogBandWidth); // 带宽限制设置
        dialogBandWidthHz = (DialogBandWidthHz) findViewById(R.id.dialogBandWidthHz); // 带宽频率设置
        dialogProbeMultiple = (DialogProbeMultiple) findViewById(R.id.dialogProbeMultiple); // 探头倍数设置
        dialogProbeInterface=findViewById(R.id.dialogProbeInterface); // 探头接口设置
        dialogChannelLabel = (DialogChannelLabel) findViewById(R.id.dialogChannelLabel); // 通道标签设置
        dialogSelectColor = (DialogSelectColor) findViewById(R.id.dialogSelectColor); // 颜色选择
        dialogSetChannelInfo = (DialogSetChannelInfo) findViewById(R.id.dialogSetChannelInfo); // 通道信息设置
        
        // 初始化参考波形对话框
        dialogRefRecall = (DialogRefRecall) findViewById(R.id.dialogRefRecall); // 参考波形回调
        dialogLoadRefCsvWave = (DialogLoadRefCsvWave)findViewById(R.id.dialogLoadRefCsv); // 加载CSV波形
        
        // 初始化显示设置对话框
        dialogAfterGlow = (TopViewSelectHorListToList) findViewById(R.id.selectListToList); // 余辉设置
        dialogFftAfterGlow = (TopViewSelectHorListToList) findViewById(R.id.fft_selectListToList); // FFT余辉设置
        dialogMathFFTPersist=findViewById(R.id.dialogMathFFTPersist); // FFT持久化设置
        
        // 初始化时基对话框
        dialogCenterTimeBase = (MainLayoutCenterTimeBase) findViewById(R.id.mainCenterTimeBase); // 中央时基设置
        
        // 初始化确认类对话框
        dialogOkCancel = (DialogOkCancel) findViewById(R.id.dialogOkCancel); // 确认取消双按钮
        dialogOk = (DialogOk) findViewById(R.id.dialogOk); // 确认单按钮
        dialogMenuHalf = (MainDialogMenuHalf) findViewById(R.id.dialogMenuHalf); // 半屏菜单
        
        // 设置半屏菜单的50%按钮区域（用于屏幕百分比选择）
        this.post(()->{
            Rect r=Tools.getViewRect(findViewById(R.id.per50)); // 获取50%按钮的位置矩形
            dialogMenuHalf.setRectBtnPer50(r); // 设置半屏菜单的按钮区域
        });

    }

    /**
     * 初始化RxBus事件订阅
     * 
     * @description 订阅所有与MainViewGroup相关的RxBus事件，包括：
     *              1. MAIN_SLIP_FROM_OTHER: 菜单滑出/隐藏事件（来自其他组件）
     *              2. MAIN_MENU_ENABLESLIP: 菜单启用/禁用滑动事件
     *              3. CENTER_SERIALSWORD_VISIBLE: 串行文本可见性事件
     *              4. WAVEZONE_WORKMODE_CHANGE: 工作模式切换事件
     *              5. TOPLAYOUT_SAMPLESEGMENTED_STATE: 分段存储状态事件
     *              6. COMMAND_TO_UI: 命令消息事件（来自中间层）
     *              7. TOPLAYOUT_DISPLAY: 显示设置事件
     *              8. EXTERNALKEYS_FORCE: 强制触发事件
     *              9. MQ_CHANNEL_ACTIVE_CHANGE: 通道激活状态变化事件（MQ消息）
     *              10. MQ_MSG_MEASURE_ITEM_COUNT: 测量项数量事件
     *              11. MQ_MSG_MEASURE_ROW_COUNT: 测量行数事件
     *              12. MQ_MSG_CHANNEL_SELECT_COLOR: 通道颜色选择事件
     * @调用时机 在构造函数中调用，完成所有RxBus事件的订阅
     * @订阅方式 使用RxBus.getInstance().getObservable()订阅事件，使用Consumer处理事件
     * @关联方法 consumerMainSlipFromOther - 处理菜单滑出/隐藏事件
     * @关联方法 consumerCommandToUI - 处理命令消息事件
     * @关联方法 OnChanActiveChange - 处理通道激活状态变化事件
     */
    private void initControl() {
        // 订阅菜单滑出/隐藏事件（来自其他组件）
        RxBus.getInstance().getObservable(RxEnum.MAIN_SLIP_FROM_OTHER).subscribe(consumerMainSlipFromOther);
        
        // 订阅菜单启用/禁用滑动事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_MENU_ENABLESLIP).subscribe(consumerMainMenuEnableSlip);
        
        // 订阅串行文本可见性事件
        RxBus.getInstance().getObservable(RxEnum.CENTER_SERIALSWORD_VISIBLE).subscribe(consumerSerialswordVisible);
        
        // 订阅工作模式切换事件
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);
        
        // 订阅分段存储状态事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE).subscribe(consumerSegmentedState);
        
        // 订阅命令消息事件（来自中间层）
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        
        // 订阅显示设置事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_DISPLAY).subscribe(consumerDisplay);
        
        // 订阅强制触发事件
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_FORCE).subscribe(consumerForceTrigger);

        // 订阅通道激活状态变化事件（MQ消息）
        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,this::OnChanActiveChange);

        // 订阅测量项数量事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_MEASURE_ITEM_COUNT).subscribe(consumerCommandMeasureOpenToUI);
        
        // 订阅测量行数事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_MEASURE_ROW_COUNT).subscribe(consumerMeasureRowCount);
        
        // 订阅通道颜色选择事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor);
    }



    private float rightSlipX; // 右侧滑动菜单的X坐标基准值
    private float rightSlipLevelWidth; // 右侧电平菜单的宽度

    /**
     * 配置触发电平菜单
     * 
     * @description 设置触发电平线的拖动监听器，实现触发电平菜单的滑动显示/隐藏功能
     *              当用户拖动触发电平线时，右侧的电平菜单会跟随滑动显示
     *              监听器包含三个回调：
     *              1. onMoving: 拖动过程中，实时更新电平菜单的位置
     *              2. onMoveEnd: 拖动结束，显示完整的电平菜单
     *              3. onLevelVisible: 触发电平线可见性变化，控制波形区域的启用/禁用状态
     * @调用时机 在构造函数中调用，完成触发电平菜单的配置
     * @关联布局 MTriggerLevel - 触发电平线布局
     * @关联布局 RightLayoutLevel - 右侧电平菜单布局
     * @关联方法 showLevelMenu - 显示电平菜单
     * @关联方法 hideLevelMenu - 隐藏电平菜单
     * @关联方法 setEnableWaveOption - 设置波形区域启用/禁用状态
     */
    private void setLevelMenu() {
        rightSlipX = getResources().getDimension(R.dimen.rightSlipX); // 获取右侧滑动菜单的X坐标基准值
        rightSlipLevelWidth = getResources().getDimension(R.dimen.rightSlipLevelWidth); // 获取右侧电平菜单的宽度
        
        // 设置触发电平线的移动监听器
        triggerLevel.setOnMoveLevelMenuListener(new MTriggerLevel.MoveLevelMenuListener() {
            /**
             * 拖动过程中回调
             * @param triggerLevel 触发电平线对象
             * @param moveX 拖动的X方向移动距离
             */
            @Override
            public void onMoving(MTriggerLevel triggerLevel, int moveX) {
                if (triggerLevel.getVisibility() == VISIBLE) { // 触发电平线可见时才处理
                    // 如果是按钮模式显示，不处理拖动
                    if (triggerLevel.getTriggerLevel_Mode_Show() == MTriggerLevel.TriggerLevel_Mode_Show_Button) {
                        return;
                    }
                    // 如果电平菜单未显示，则显示并禁用波形区域
                    if (mRightBar_level.getVisibility() != View.VISIBLE) {
                        mRightBar_level.setVisibility(VISIBLE); // 显示电平菜单
                        setEnableWaveOption(false); // 禁用波形区域操作
                    }
                    // 根据拖动距离更新电平菜单的位置
                    if (moveX >= SliderZone.SlipDirectionSize_X) {
                        mRightBar_level.setX(Math.max(rightSlipX - moveX, rightSlipX - rightSlipLevelWidth)); // 计算并设置X坐标
                    }
                    hideAllDialogSlipButLevelMenu(); // 隐藏所有对话框和滑动菜单（除了电平菜单）
                }
            }

            /**
             * 拖动结束回调
             * @param triggerLevel 触发电平线对象
             */
            @Override
            public void onMoveEnd(MTriggerLevel triggerLevel) {
                if (triggerLevel.getVisibility() == VISIBLE) { // 触发电平线可见时才处理
                    showLevelMenu(); // 显示完整的电平菜单
                }
            }

            /**
             * 触发电平线可见性变化回调
             * @param triggerLevel 触发电平线对象
             * @param visible 是否可见
             */
            @Override
            public void onLevelVisible(MTriggerLevel triggerLevel, boolean visible) {
                if (!visible) { // 触发电平线不可见时，隐藏电平菜单
                    mRightBar_level.setVisibility(GONE);
                }
                // 根据触发电平线的显示模式和滑动菜单状态，控制波形区域的启用/禁用状态
                if (MainViewGroup.this.triggerLevel.getTriggerLevel_Mode_Show()==MTriggerLevel.TriggerLevel_Mode_Show_Drag
                    || MainViewGroup.this.triggerLevel.getTriggerLevel_Mode_Show()== MTriggerLevel.TriggerLevel_Mode_Show_Changing
                        || MainViewGroup.this.isSlipShow()
                ) {
                    setEnableWaveOption(false); // 禁用波形区域操作
                }else{
                    setEnableWaveOption(true); // 启用波形区域操作
                }
                levelSwipeLeft.setVisibility(visible ? VISIBLE : GONE); // 控制左侧滑动区域的可见性
            }
        });

    }

    /**
     * 检查触发电平线是否显示
     * 
     * @return boolean - 触发电平线是否可见
     * @description 检查触发电平线布局的可见性状态
     * @调用时机 在触摸事件处理中判断是否需要处理触发电平线的触摸操作
     */
    public boolean isTriggerLevelShow() {
        return triggerLevel.getVisibility() == VISIBLE;
    }

    /**
     * 显示电平菜单
     * 
     * @description 显示完整的右侧电平菜单，播放滑动音效，隐藏其他对话框和滑动菜单
     *              如果触发电平线是按钮模式显示，则不执行显示操作
     * @调用时机 在触发电平线拖动结束时调用
     * @关联方法 hideLevelMenu - 隐藏电平菜单
     * @关联方法 setEnableWaveOption - 设置波形区域启用/禁用状态
     */
    private void showLevelMenu() {
        if (triggerLevel.getTriggerLevel_Mode_Show() == MTriggerLevel.TriggerLevel_Mode_Show_Button) { // 检查是否为按钮模式
            return; // 按钮模式下不显示电平菜单
        }
        PlaySound.getInstance().playSlide(); // 播放滑动音效
        hideAllDialogSlipButLevelMenu(); // 隐藏所有对话框和滑动菜单（除了电平菜单）
        mRightBar_level.setVisibility(VISIBLE); // 显示电平菜单
        setEnableWaveOption(false); // 禁用波形区域操作
        mRightBar_level.setX(rightSlipX - rightSlipLevelWidth); // 设置电平菜单的X坐标（完全显示位置）
//        mRightBar_level.setX(rightSlipX - 500);
    }

    /**
     * 隐藏电平菜单
     * 
     * @description 隐藏右侧电平菜单，启用波形区域操作
     * @调用时机 在触发电平线隐藏时、或用户点击外部区域时调用
     * @关联方法 showLevelMenu - 显示电平菜单
     * @关联方法 setEnableWaveOption - 设置波形区域启用/禁用状态
     */
    public void hideLevelMenu() {
//        mRightBar_level.setX(rightSlipX);
        mRightBar_level.setVisibility(GONE); // 隐藏电平菜单
        setEnableWaveOption(true); // 启用波形区域操作
    }

    /**
     * 隐藏文件选择对话框
     * 
     * @description 发送消息隐藏文件选择器对话框
     * @调用时机 在隐藏滑动菜单时调用
     * @RxBus消息 MSG_HIDE_FILESELECTOR - 隐藏文件选择器消息
     */
    public void hideFileSelectDialog() {
//        mRightBar_level.setX(rightSlipX);
        RxBus.getInstance().post(RxEnum.MSG_HIDE_FILESELECTOR,false); // 发送隐藏文件选择器消息
    }


    /**
     * 检查电平菜单是否显示
     * 
     * @return boolean - 电平菜单是否可见
     * @description 检查右侧电平菜单布局的可见性状态
     * @调用时机 在触摸事件处理中判断是否需要特殊处理电平菜单
     */
    public boolean isLevelMenu() {
        return mRightBar_level.getVisibility() == VISIBLE;
    }

    private boolean noMove = false; // 是否禁止右侧滑动布局移动的标志

    /**
     * 设置右侧滑动布局是否禁止移动
     * 
     * @param noMove true表示禁止移动，false表示允许移动
     * @description 控制右侧滑动布局（MSlideLayout）是否可以上下滑动
     *              在XY工作模式下禁止滑动，在其他工作模式下允许滑动
     * @调用时机 在工作模式切换时调用（consumerWorkModeChange）
     * @关联方法 setRightScroll - 配置右侧滑动布局
     * @关联布局 MSlideLayout - 右侧滑动布局容器
     */
    private void setRightScrollNoMove(boolean noMove) {
        this.noMove = noMove; // 设置禁止移动标志
        mSlideLayout.setNoMove(noMove); // 设置滑动布局的禁止移动状态
    }

    /**
     * 配置右侧滑动布局
     * 
     * @description 初始化右侧滑动布局（MSlideLayout），设置标签切换监听器
     *              当用户切换右侧布局的标签页（Channel/Others）时，播放音效并隐藏所有滑动菜单
     *              同时将切换状态保存到缓存中
     * @调用时机 在构造函数中调用，完成右侧滑动布局的配置
     * @关联布局 MSlideLayout - 右侧滑动布局容器
     * @关联方法 showRightLayout - 显示右侧布局
     * @关联方法 hideAllDialogSlip - 隐藏所有对话框和滑动菜单
     * @缓存键 MAIN_BOTTOM_RIGHTSWITCH_CHANNEL - 右侧布局切换状态
     */
    private void setRightScroll() {
        mSlideLayout = (MSlideLayout) findViewById(R.id.mainRightLayout); // 获取右侧滑动布局
        mSlideLayout.setOnTabChanged(new MSlideLayout.OnTabChanged() {
            /**
             * 标签切换回调
             * @param channel true表示切换到Channel页面，false表示切换到Others页面
             */
            @Override
            public void onTabChanged(boolean channel) {
                PlaySound.getInstance().playSlide(); // 播放滑动音效
                // 如果切换状态与缓存中的状态不同，隐藏所有滑动菜单
                if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_RIGHTSWITCH_CHANNEL) != channel) {
                    hideAllDialogSlip();
                }
                // 保存切换状态到缓存
                CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_RIGHTSWITCH_CHANNEL, String.valueOf(channel));
            }
        });

    }

    /**
     * 显示右侧布局
     * 
     * @param channel true表示显示Channel页面，false表示显示Others页面
     * @description 显示右侧滑动布局的指定页面，播放音效并隐藏所有滑动菜单
     *              如果设置了禁止移动标志（noMove），则不执行显示操作
     * @调用时机 在底部菜单切换Channel/Others按钮时调用
     * @关联方法 isRightLayout - 检查右侧布局当前状态
     * @关联方法 setRightScrollNoMove - 设置禁止移动标志
     * @缓存键 MAIN_BOTTOM_RIGHTSWITCH_CHANNEL - 右侧布局切换状态
     */
    public void showRightLayout(boolean channel) {
        if (!noMove) { // 如果没有禁止移动
//            if (channel) {
                mSlideLayout.ShowIndexLayout(0); // 显示第一个页面（Channel页面）
//            } else { //FixMe 右侧垂直菜单目前只有channel一栏，没有Math/Ref/Serials，不能上下滑动, 这里先注释掉
//                mSlideLayout.ShowIndexLayout(1);
//            }
        }
        PlaySound.getInstance().playSlide(); // 播放滑动音效
//        if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_RIGHTSWITCH_CHANNEL) != channel) {
            hideAllDialogSlip(); // 隐藏所有对话框和滑动菜单
//        }
        CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_RIGHTSWITCH_CHANNEL, String.valueOf(channel)); // 保存切换状态到缓存
    }

    /**
     * 检查右侧布局当前状态
     * 
     * @return boolean - true表示当前显示Channel页面，false表示当前显示Others页面
     * @description 从缓存中读取右侧布局的当前切换状态
     * @调用时机 在判断右侧布局当前显示的页面类型时调用
     * @缓存键 MAIN_BOTTOM_RIGHTSWITCH_CHANNEL - 右侧布局切换状态
     */
    public boolean isRightLayout() {
        return CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_RIGHTSWITCH_CHANNEL);
    }

    /**
     * 检查键盘候选词是否显示
     * 
     * @return boolean - 文本键盘是否显示且候选词列表是否显示
     * @description 检查文本键盘对话框是否显示，以及候选词列表是否显示
     * @调用时机 在触摸事件处理中判断是否需要特殊处理键盘候选词
     * @关联布局 TopDialogTextKeyBoard - 文本键盘对话框
     */
    public boolean isKeyboradCandidatesWordShow() {
        return (dialogTextKeyBoard.getVisibility() == View.VISIBLE) && dialogTextKeyBoard.isCandidatesWordShow();
    }

    /**
     * 设置波形区域操作启用/禁用状态
     * 
     * @param b true表示启用波形区域操作，false表示禁用波形区域操作
     * @description 通过控制mainSlipOutView的可见性来启用/禁用波形区域的触摸操作
     *              当滑动菜单或对话框显示时，禁用波形区域操作，防止误触
     *              当所有菜单和对话框隐藏时，启用波形区域操作
     * @调用时机 在显示/隐藏滑动菜单或对话框时调用
     * @关联方法 isEnableWaveOption - 检查波形区域操作是否启用
     * @关联布局 mainSlipOutView - 主滑动外部视图（用于禁用波形区域）
     */
    private void setEnableWaveOption(boolean b){
        if (b){
            mainSlipOutView.setVisibility(GONE); // 隐藏外部视图，启用波形区域操作
        }else {
            mainSlipOutView.setVisibility(VISIBLE); // 显示外部视图，禁用波形区域操作
        }
    }

    /**
     * 检查波形区域操作是否启用
     * 
     * @return boolean - true表示启用，false表示禁用
     * @description 检查mainSlipOutView是否隐藏，如果隐藏则表示波形区域操作已启用
     * @调用时机 在触摸事件处理中判断是否需要处理波形区域的触摸操作
     * @关联方法 setEnableWaveOption - 设置波形区域操作启用/禁用状态
     */
    public boolean isEnableWaveOption() {
        return mainSlipOutView.getVisibility() == GONE;
    }

    /**
     * 检查是否有对话框显示
     * 
     * @return boolean - true表示有对话框显示，false表示没有对话框显示
     * @description 检查所有28种对话框的可见性状态，只要有一个对话框显示就返回true
     * @调用时机 在触摸事件处理中判断是否需要隐藏对话框
     * @对话框列表 包括所有使用@Dialog注解定义的对话框
     * @关联方法 hideAllDialogsButDialogOkCancel - 隐藏所有对话框（除了确认对话框）
     */
    public boolean isDialogsShow() {
        // 检查测量类对话框
        if (dialogMeasureDelay.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogMeasurePhase.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogMeasureTValue.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogTopCount.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogTopScale.getVisibility() == VISIBLE) {
            return true;
        } 
        // 检查键盘类对话框
        else if (dialogTextKeyBoard.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogNumberKeyBoard.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogFloatKeyBoard.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogFormulaKeyBoard.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogNumberPicker.getVisibility() == VISIBLE) {
            return true;
        } 
        // 检查通道参数对话框
        else if (dialogBaudRate.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogBandWidth.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogProbeMultiple.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogRefRecall.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogAfterGlow.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogFftAfterGlow.getVisibility() == VISIBLE) {
            return true;
        } 
        // 检查时基和确认类对话框
        else if (dialogCenterTimeBase.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogOkCancel.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogOk.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogMenuHalf.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogChannelLabel.getVisibility() == VISIBLE) {
            return true;
        }else if (dialogBandWidthHz.getVisibility()==VISIBLE){
            return true;
        }else if (dialogMathFFTPersist.getVisibility()==VISIBLE){
            return true;
        }else if (dialogProbeInterface.getVisibility()==VISIBLE){
            return true;
        } 
        // 检查参考波形和颜色选择对话框
        else if (dialogLoadRefCsvWave.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogSelectColor.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogSetChannelInfo.getVisibility() == VISIBLE) {
            return true;
        } else if (dialogFullFloatKeyBoard.getVisibility() == VISIBLE) {
            return true;
        }
        return false;
    }

    /**
     * 获取串行文本工作布局
     * 
     * @return MainLayoutCenterSerialsWord - 串行文本显示布局
     * @description 获取中央区域的串行文本显示布局
     * @调用时机 在需要操作串行文本显示时调用
     * @关联布局 MainLayoutCenterSerialsWord - 中央串行文本布局
     */
    public MainLayoutCenterSerialsWord getSerialWorkLayout(){
        MainLayoutCenterSerialsWord  layoutSerialsWord = (MainLayoutCenterSerialsWord) findViewById(R.id.layoutSerialsWord);
        return layoutSerialsWord;
    }
    
    /**
     * 获取通道选择布局
     * 
     * @return MainLayoutCenterChannel - 通道选择条布局
     * @description 获取中央区域的通道选择条布局
     * @调用时机 在需要操作通道选择条时调用
     * @关联布局 MainLayoutCenterChannel - 中央通道选择布局
     */
    public MainLayoutCenterChannel getChannelsLayout(){return channelsLayout;}
    
    /**
     * 获取中央分段存储布局
     * 
     * @return MainLayoutCenterSegmented - 分段存储布局
     * @description 获取中央区域的分段存储布局
     * @调用时机 在需要操作分段存储功能时调用
     * @关联布局 MainLayoutCenterSegmented - 中央分段存储布局
     */
    public MainLayoutCenterSegmented getCenterSegmentedLayout() {
        return centerSegmentedLayout;
    }
    
    /**
     * 获取触发电平线布局
     * 
     * @return MTriggerLevel - 触发电平线布局
     * @description 获取触发电平线布局对象
     * @调用时机 在需要操作触发电平线时调用
     * @关联布局 MTriggerLevel - 触发电平线布局
     */
    public MTriggerLevel getTriggerLevel() {
        return triggerLevel;
    }
    
    /**
     * 获取探头接口对话框
     * 
     * @return DialogProbeInterface - 探头接口设置对话框
     * @description 获取探头接口设置对话框对象
     * @调用时机 在需要操作探头接口设置时调用
     * @关联对话框 DialogProbeInterface - 探头接口对话框
     */
    public DialogProbeInterface getDialogProbeInterface(){
        return dialogProbeInterface;
    }


    /**
     * 初始化布局
     * 
     * @description 初始化所有滑动菜单区域，将滑动区域配置添加到列表中
     *              包括：底部菜单、左侧菜单、顶部菜单、右侧通道菜单（8个）、右侧数学运算菜单（8个）、右侧参考波形菜单（8个）、右侧串行总线菜单（4个）
     *              最后初始化透明度视图列表
     * @调用时机 在MainActivity中调用，完成所有布局的初始化
     * @关联方法 initPublicSlipMenu - 初始化公共滑动菜单
     * @关联方法 addSliderZone - 添加滑动区域配置
     * @关联方法 initTransparencyView - 初始化透明度视图列表
     */
    public void initLayout() {
        initPublicSlipMenu(); // 初始化公共滑动菜单（顶部、底部、左侧、右侧通道菜单）
        
        // 添加所有滑动区域配置到列表
        this.addSliderZone(sliderZone_bottom); // 底部滑动菜单
        this.addSliderZone(sliderZone_left); // 左侧滑动菜单
        //this.addSliderZone(sliderZone_right);
        this.addSliderZone(sliderZone_top); // 顶部滑动菜单
        
        // 添加右侧通道滑动菜单（CH1-CH8）
        this.addSliderZone(sliderZone_rightCh1);
        this.addSliderZone(sliderZone_rightCh2);
        this.addSliderZone(sliderZone_rightCh3);
        this.addSliderZone(sliderZone_rightCh4);
        this.addSliderZone(sliderZone_rightCh5);
        this.addSliderZone(sliderZone_rightCh6);
        this.addSliderZone(sliderZone_rightCh7);
        this.addSliderZone(sliderZone_rightCh8);
        
        // 添加右侧数学运算滑动菜单（Math1-Math8）
        this.addSliderZone(sliderZone_rightMath1);
        this.addSliderZone(sliderZone_rightMath2);
        this.addSliderZone(sliderZone_rightMath3);
        this.addSliderZone(sliderZone_rightMath4);
        this.addSliderZone(sliderZone_rightMath5);
        this.addSliderZone(sliderZone_rightMath6);
        this.addSliderZone(sliderZone_rightMath7);
        this.addSliderZone(sliderZone_rightMath8);
        
        // 添加右侧参考波形滑动菜单（Ref1-Ref8）
        this.addSliderZone(sliderZone_rightRef1);
        this.addSliderZone(sliderZone_rightRef2);
        this.addSliderZone(sliderZone_rightRef3);
        this.addSliderZone(sliderZone_rightRef4);
        this.addSliderZone(sliderZone_rightRef5);
        this.addSliderZone(sliderZone_rightRef6);
        this.addSliderZone(sliderZone_rightRef7);
        this.addSliderZone(sliderZone_rightRef8);
        
        // 添加右侧串行总线滑动菜单（S1-S4）
        this.addSliderZone(sliderZone_rightS1);
        this.addSliderZone(sliderZone_rightS2);
        this.addSliderZone(sliderZone_rightS3);
        this.addSliderZone(sliderZone_rightS4);
        
        initTransparencyView(); // 初始化透明度视图列表
    }

    /**
     * 上划页面是否进行过滑动
     * - 业务含义: 标记顶部滑动菜单是否被用户拖动过
     * - 使用场景: 在触摸事件处理中判断是否需要特殊处理顶部菜单的回拉
     */
    boolean isTopSlipMoveBack = false;
    
    /**
     * 视图位置数组
     * - 业务含义: 用于获取视图在屏幕上的位置坐标
     * - 使用场景: 在触发电平线触摸处理中获取位置信息
     */
    int[] location = new int[2];


    /**
     * 当前是否有用户触摸屏幕
     * - 业务含义: 标记用户是否正在触摸屏幕
     * - 使用场景: 在外部按键处理中判断是否需要禁用按键功能
     * - 注意: 用户触摸屏幕时，外部按键功能会被禁用
     */
    volatile boolean userTouch = false; // 用户触摸标志
    
    /**
     * 用户触摸时间戳
     * - 业务含义: 记录用户最后一次触摸屏幕的时间
     * - 使用场景: 判断用户触摸状态是否超时（3秒超时）
     */
    volatile long userTouchTs = 0; // 用户触摸时间戳
    
    /**
     * 检查用户是否正在触摸屏幕
     * 
     * @return boolean - true表示用户正在触摸，false表示用户未触摸或触摸超时
     * @description 检查用户触摸状态，如果触摸时间超过3秒则认为未触摸
     * @调用时机 在外部按键处理中判断是否需要禁用按键功能
     * @超时时间 3秒（3000毫秒）
     */
    public boolean isUserTouch() {
        if(userTouch){
            if(SystemClock.elapsedRealtime() - userTouchTs > 3000){ // 检查是否超时
                return false; // 超时则认为未触摸
            }
        }
        return userTouch; // 返回触摸状态
    }
    
    /**
     * 设置用户触摸状态
     * 
     * @param b true表示用户正在触摸，false表示用户未触摸
     * @description 设置用户触摸状态标志，同步方法确保线程安全
     * @调用时机 在触摸事件处理中设置触摸状态
     * @关联方法 isUserTouch - 检查用户触摸状态
     */
    public synchronized void setUserTouch(boolean b){
        this.userTouch = b; // 设置触摸状态
    }

    private Rect r = new Rect(); // 用于存储视图位置的矩形对象

    /**
     * 检查触摸点是否在视图内部（用于DOWN事件）
     * 
     * @param v 要检查的视图对象
     * @param event 触摸事件对象
     * @return boolean - true表示触摸点在视图内部且视图可见且是DOWN事件，false表示不在
     * @description 检查触摸事件的DOWN点是否在指定视图的范围内，同时检查视图是否可见
     * @调用时机 在触摸事件处理中判断是否需要特殊处理特定视图
     * @使用场景 用于判断触摸点是否在底部按钮区域、左侧屏幕按钮等特定区域
     */
    private boolean containsDownPoint(View v, MotionEvent event) {
        int x = (int) event.getRawX(); // 获取触摸点的原始X坐标（屏幕坐标）
        int y = (int) event.getRawY(); // 获取触摸点的原始Y坐标（屏幕坐标）
        Rect r = Screen.getViewLocation(v); // 获取视图在屏幕上的位置矩形
        boolean b1 = x > r.left && x < r.right && y > r.top && y < r.bottom; // 检查触摸点是否在矩形内
        boolean b2 = getVisibility() == View.VISIBLE; // 检查视图是否可见
        boolean b3 = event.getAction() == MotionEvent.ACTION_DOWN; // 检查是否是DOWN事件
        boolean b = b1 && b2 && b3; // 综合判断
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction()==MotionEvent.ACTION_UP) {
//            Logger.i(TAG, "containsDownPoint:" + r.toShortString()
//                    + "," + event.getRawX() + "," + event.getRawY()
//                    + "," + b + "," + b1 + "," + b2 + "," + b3);
        }
        return b;
    }
    
    /**
     * 停止自动保存按钮的位置矩形
     * - 业务含义: 缓存停止自动保存按钮的位置，避免重复计算
     * - 使用场景: 在触摸事件处理中判断触摸点是否在停止自动保存按钮区域
     */
    Rect btnStopAutoSaveRect = null;
    
    /**
     * 获取停止自动保存按钮的位置矩形
     * 
     * @return Rect - 停止自动保存按钮的位置矩形
     * @description 获取停止自动保存按钮在屏幕上的位置矩形，使用缓存避免重复计算
     * @调用时机 在触摸事件处理中判断触摸点是否在停止自动保存按钮区域
     * @关联布局 btnStopAutoSave - 停止自动保存按钮
     */
    Rect getStopAutoSaveRect(){
        if(btnStopAutoSaveRect == null){ // 如果缓存为空，则计算位置
            int [] loc = new int[2]; // 位置数组
            btnStopAutoSave.getLocationOnScreen(loc); // 获取按钮在屏幕上的位置
            btnStopAutoSaveRect = new Rect(loc[0], // 创建位置矩形
                    loc[1],
                    loc[0] + btnStopAutoSave.getWidth(),
                    loc[1] + btnStopAutoSave.getHeight()
            );
        }
        return btnStopAutoSaveRect; // 返回位置矩形
    }
    
    /**
     * 事件分发入口方法
     * 
     * @param ev 触摸事件对象
     * @return boolean - 是否消费事件
     * @description 处理屏幕锁定状态和特殊区域的触摸事件分发
     *              主要处理以下情况：
     *              1. 停止自动保存按钮区域的触摸事件（直接传递给子视图）
     *              2. 屏幕锁定状态下的触摸事件（特殊处理）
     *              3. 外部按键模式下的触摸事件（拦截处理）
     *              4. 正常触摸事件（调用onEvent处理）
     * @调用时机 用户触摸屏幕时由系统调用
     * @关联方法 onEvent - 核心事件处理逻辑
     * @关联类 ScreenControls - 屏幕控制类（管理屏幕锁定状态）
     * @重要逻辑 屏幕锁定时，触摸事件会被特殊处理，防止误操作
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 处理停止自动保存按钮区域的触摸事件
        if(btnStopAutoSave.getVisibility() == View.VISIBLE){ // 如果停止自动保存按钮可见
            Rect r = getStopAutoSaveRect(); // 获取按钮位置矩形
            float rawX = ev.getRawX(); // 获取触摸点原始X坐标
            float rawY = ev.getRawY(); // 获取触摸点原始Y坐标
            if(rawX >= r.left && rawX <= r.right && rawY >= r.top && rawY <= r.bottom){ // 如果触摸点在按钮区域内
                return onEvent(ev) || super.dispatchTouchEvent(ev); // 处理事件或传递给子视图
            }
        }
        
        // 处理屏幕锁定状态下的触摸事件
        if (ScreenControls.getInstance().isLockScreen() && ScreenControls.getInstance().isExternalKey()) { // 如果屏幕锁定且是外部按键模式
            return super.onInterceptTouchEvent(ev); // 拦截事件，不传递给子视图
        }
        
        // 处理屏幕锁定且metaState为1的触摸事件（外部按键触发）
        if (ScreenControls.getInstance().isLockScreen() && ev.getMetaState() == 1) {
            return super.dispatchTouchEvent(ev); // 正常分发事件
        } 
        // 处理屏幕锁定但不是外部按键的触摸事件
        else if (ScreenControls.getInstance().isLockScreen()) {
            ScreenControls.getInstance().setMaskLayerLayoutOnTouchListener(ev); // 设置遮罩层触摸监听器
            return super.onInterceptTouchEvent(ev); // 拦截事件
        }
        
        // 正常触摸事件处理
        return onEvent(ev) || super.dispatchTouchEvent(ev); // 处理事件或传递给子视图
    }



    private boolean onEvent(MotionEvent event) {
        boolean intercept = false;
        AutoSave.getInstance().setUserInput(true);
        //从手指接触屏幕开始，直到手指离开屏幕为止，为用户接触状态
        //此状态下，外部按键不管用
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            userTouch = true;
            userTouchTs = SystemClock.elapsedRealtime();
            CacheUtil.get().putMap(CacheUtil.USER_TOUCH, String.valueOf(userTouch));
            lastXRawTriggerControl = (int) event.getRawX();
            lastYRawTriggerControl = (int) event.getRawY();
            //Logger.i("MainViewGroup,MotionEvent.ACTION_DOWN:" + lastXRawTriggerControl + "," + lastYRawTriggerControl);
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            userTouch = false;
            userTouchTs = 0;
            CacheUtil.get().putMap(CacheUtil.USER_TOUCH, String.valueOf(userTouch));
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            userTouchTs = SystemClock.elapsedRealtime();
        } else {
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {//设置外部按键的预选框位置，和显现性
            boolean isFromExternal = event.getMetaState() == 1;
            if (!isFromExternal) {
                try {
                    ExternalKeysManager.get().userClick((int) event.getX(), (int) event.getY());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            ExternalKeysUI.getInstance().setFocusViewVisible(isFromExternal);

        }

        boolean b=isContainTopTools(event);
        if (b) return intercept;


        if (!channelsLayout.containsDownPoint(event) && !centerMenuLayout.containsDownPoint(event)
                && !isDialogsShow()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastXIntercept = (int) event.getX();
                lastYIntercept = (int) event.getY();
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {//目的是点击其他位置时隐藏垂直挡位按钮，不影响点击事件的传递
                hideMainVerticalScale(event);
            }
            //这里点击要扣掉SerialsMaster位置
            boolean containsRightLayoutLevel = Tools.getViewRect(mRightBar_level).contains(lastXRawTriggerControl, lastYRawTriggerControl);
            if (!triggerLevel.hasPoint(lastXRawTriggerControl, lastYRawTriggerControl)
                    && !containsSerialsMaster(lastXRawTriggerControl, lastYRawTriggerControl)
                    && (!containsRightLayoutLevel || (containsRightLayoutLevel && mRightBar_level.getVisibility() != View.VISIBLE))
                /*&& ((lastXRawTriggerControl < rightSlipX - rightSlipLevelWidth || lastXRawTriggerControl > rightSlipX))*/
            ) {
                triggerLevel.Animation_DragToButton();
            } else {
                triggerLevel.getLocationOnScreen(location);
                MotionEvent event1 = MotionEvent.obtain(event);
                event1.setLocation(event.getX() - location[0], event.getY() - location[1]);
                triggerLevel.dealTouchEvent(event1);
                return false;
            }

            if (vTracker == null) {
                vTracker = VelocityTracker.obtain();
            } else {
                vTracker.clear();
            }
            vTracker.addMovement(event);


            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    pointerId = event.getPointerId(0);
                    velocityX = 0;
                    velocityY = 0;

                    sliderZone_top.setSliderViewGroup(mTopBar_quick);
                    lastXIntercept = (int) event.getX();
                    lastYIntercept = (int) event.getY();
                    slipDirection = SliderZone.SliderDir_None;
                    dealSliderZoneSelect((int) event.getX(), (int) event.getY());
                    intercept = false;
                    isTopSlipMoveBack = false;

                    break;
                case MotionEvent.ACTION_MOVE:
                    vTracker.computeCurrentVelocity(1000, maxVelocity);
                    float vX = vTracker.getXVelocity(pointerId);
                    float vY = vTracker.getYVelocity(pointerId);
                    if (vX != 0 || vY != 0) {
                        velocityX = vX;
                        velocityY = vY;
                    }
                    dealSliderZone(event, (int) event.getX(), (int) event.getY());
                    intercept = false;
                    break;
                case MotionEvent.ACTION_UP:
                    if (isClickTopToolsBtn(event)){
                        return intercept;
                    }
                    clickSlipOutsideClose(((int) event.getX()), (int) event.getY());
                    dealSliderZoneComplete((int) event.getX(), (int) event.getY());
                    if (vTracker != null) {
                        vTracker.clear();
                        vTracker.recycle();
                        vTracker = null;
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
            }
        }
//        Logger.i(TAG, "intercept:" + intercept);
        return intercept;
    }

    private boolean containsSerialsMaster(int lastXRawTriggerControl, int lastYRawTriggerControl) {
        View s1Master = findViewById(R.id.rightS1Master);
        View s2Master = findViewById(R.id.rightS2Master);
        View s3Master = findViewById(R.id.rightS3Master);
        View s4Master = findViewById(R.id.rightS4Master);
        boolean s1Contains = s1Master.getVisibility() == View.VISIBLE
                && Tools.getViewRect(s1Master).contains(lastXRawTriggerControl, lastYRawTriggerControl);
        boolean s2Contains = s2Master.getVisibility() == View.VISIBLE
                && Tools.getViewRect(s2Master).contains(lastXRawTriggerControl, lastYRawTriggerControl);
        boolean s3Contains = s3Master.getVisibility() == View.VISIBLE
                && Tools.getViewRect(s3Master).contains(lastXRawTriggerControl, lastYRawTriggerControl);
        boolean s4Contains = s4Master.getVisibility() == View.VISIBLE
                && Tools.getViewRect(s4Master).contains(lastXRawTriggerControl, lastYRawTriggerControl);

        return s1Contains || s2Contains || s3Contains || s4Contains;
    }

    private void hideMainVerticalScale(MotionEvent event) {
        if (mainChannelVerticalScale == null) return;
        if (mainChannelVerticalScale.getVisibility() != View.VISIBLE) return;
        if (!mainChannelVerticalScale.containsDownPoint(event)) { //点击位置在垂直调节挡位外面
            mainChannelVerticalScale.hideVerticalScale();
        }
    }

    private boolean isClickTopToolsBtn(MotionEvent event){
        boolean b = lastXIntercept==(int)event.getX() && lastYIntercept==(int)event.getY();
        if (mainTopLayoutLeft==null){
            mainTopLayoutLeft=findViewById(R.id.mainTopLayoutLeft);
        }
        if (mainTopLayoutLeft.containsRect(event) && b){
            return true;
        }

        if (mainTopLayoutCenter==null){
            mainTopLayoutCenter=findViewById(R.id.mainTopLayoutCenter);
        }
        if (mainTopLayoutCenter.containsRect(event) && b){
            return true;
        }
        if  (mainTopRight.containsRect(event) && b){
            return true;
        }
        return false;
    }
    private boolean isContainTopTools(MotionEvent event){
        if (bottomRightChannel == null) {
            bottomRightChannel = (RelativeLayout) findViewById(R.id.bottomRightChannel);
        }
        if (bottomRightOther == null) {
            bottomRightOther = (RelativeLayout) findViewById(R.id.bottomRightOther);
        }
        if (btnLeftScreen == null) {
            btnLeftScreen = (Button) findViewById(R.id.btnLeftScreen);
        }


//        if  (mainTopRight.containsRect(event)){
//            return true;
//        }
//        if (mainTopLayoutRightButton == null) {
//            mainTopLayoutRightButton = (MainTopLayoutRightButton) findViewById(R.id.mainTopLayoutRightButton);
//        }
//        if (mainTopLayoutRightButton.containsRect(event)) {
//            return true;
//        }
//        if (mainTopLayoutLeft==null){
//            mainTopLayoutLeft=findViewById(R.id.mainTopLayoutLeft);
//        }
//        if (mainTopLayoutLeft.containsRect(event)){
//            return true;
//        }
//        if (mainTopLayoutCenter==null){
//            mainTopLayoutCenter=findViewById(R.id.mainTopLayoutCenter);
//        }
//        if (mainTopLayoutCenter.containsRect(event)){
//            return true;
//        }
        if (containsDownPoint(bottomRightChannel, event)) {
            return true;
        }
        if (containsDownPoint(bottomRightOther, event)) {
            return true;
        }
        if (containsDownPoint(btnLeftScreen, event)) {
            return true;
        }
        if (dialogBandWidthHz.contains(event) && dialogBandWidthHz.getVisibility()==View.VISIBLE){
            return true;
        }


        return false;
    }
    private boolean isClickScreenshots(MotionEvent ev) {
        boolean flag = false;
//        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
//            Button btn1 = findViewById(R.id.screenShot);
//            Button btn2 = findViewById(R.id.mButton_screen);
//
//            Rect r1 = Tools.getViewRect(btn1);
//            Rect r2 = Tools.getViewRect(btn2);
//            if (r1.contains((int) ev.getRawX(), (int) ev.getRawY()) || r2.contains((int) ev.getRawX(), (int) ev.getRawY())) {
//                btn1.performClick();
//                btn1.setPressed(true);
//                btn2.setPressed(true);
//                PlaySound.getInstance().playButton();
//                new Handler().postDelayed(() -> {
//                    btn1.setPressed(false);
//                    btn2.setPressed(false);
//                }, 100);
//                flag = true;
//            }
//        }
        return flag;
    }

    /**
     * 点击slip外部的区域使slip关闭
     */
    private void clickSlipOutsideClose(int x, int y) {
        Rect viewLocation = null;
        int slip = 0;
        if (isSlipShow(TOPSLIP)) {
            slip = TOPSLIP;
            viewLocation = Screen.getViewLocation(mTopBar_quick);
        } else if (isSlipShow(RIGHTSLIP_CH1)) {
            slip = RIGHTSLIP_CH1;
            viewLocation = Screen.getViewLocation(mRightBar_channel1);
        } else if (isSlipShow(RIGHTSLIP_CH2)) {
            slip = RIGHTSLIP_CH2;
            viewLocation = Screen.getViewLocation(mRightBar_channel2);
        } else if (isSlipShow(RIGHTSLIP_CH3)) {
            slip = RIGHTSLIP_CH3;
            viewLocation = Screen.getViewLocation(mRightBar_channel3);
        } else if (isSlipShow(RIGHTSLIP_CH4)) {
            slip = RIGHTSLIP_CH4;
            viewLocation = Screen.getViewLocation(mRightBar_channel4);
        } else if (isSlipShow(RIGHTSLIP_CH5)) {
            slip = RIGHTSLIP_CH5;
            viewLocation = Screen.getViewLocation(mRightBar_channel5);
        } else if (isSlipShow(RIGHTSLIP_CH6)) {
            slip = RIGHTSLIP_CH6;
            viewLocation = Screen.getViewLocation(mRightBar_channel6);
        } else if (isSlipShow(RIGHTSLIP_CH7)) {
            slip = RIGHTSLIP_CH7;
            viewLocation = Screen.getViewLocation(mRightBar_channel7);
        } else if (isSlipShow(RIGHTSLIP_CH8)) {
            slip = RIGHTSLIP_CH8;
            viewLocation = Screen.getViewLocation(mRightBar_channel8);
        } else if (isSlipShow(RIGHTSLIP_MATH1)) {
            slip = RIGHTSLIP_MATH1;
            viewLocation = Screen.getViewLocation(mRightBar_math1);
        } else if (isSlipShow(RIGHTSLIP_MATH2)) {
            slip = RIGHTSLIP_MATH2;
            viewLocation = Screen.getViewLocation(mRightBar_math2);
        } else if (isSlipShow(RIGHTSLIP_MATH3)) {
            slip = RIGHTSLIP_MATH3;
            viewLocation = Screen.getViewLocation(mRightBar_math3);
        } else if (isSlipShow(RIGHTSLIP_MATH4)) {
            slip = RIGHTSLIP_MATH4;
            viewLocation = Screen.getViewLocation(mRightBar_math4);
        } else if (isSlipShow(RIGHTSLIP_MATH5)) {
            slip = RIGHTSLIP_MATH5;
            viewLocation = Screen.getViewLocation(mRightBar_math5);
        } else if (isSlipShow(RIGHTSLIP_MATH6)) {
            slip = RIGHTSLIP_MATH6;
            viewLocation = Screen.getViewLocation(mRightBar_math6);
        } else if (isSlipShow(RIGHTSLIP_MATH7)) {
            slip = RIGHTSLIP_MATH7;
            viewLocation = Screen.getViewLocation(mRightBar_math7);
        } else if (isSlipShow(RIGHTSLIP_MATH8)) {
            slip = RIGHTSLIP_MATH8;
            viewLocation = Screen.getViewLocation(mRightBar_math8);
        } else if (isSlipShow(RIGHTSLIP_REF1)) {
            slip = RIGHTSLIP_REF1;
            viewLocation = Screen.getViewLocation(mRightBar_ref1);
        } else if (isSlipShow(RIGHTSLIP_REF2)) {
            slip = RIGHTSLIP_REF2;
            viewLocation = Screen.getViewLocation(mRightBar_ref2);
        } else if (isSlipShow(RIGHTSLIP_REF3)) {
            slip = RIGHTSLIP_REF3;
            viewLocation = Screen.getViewLocation(mRightBar_ref3);
        } else if (isSlipShow(RIGHTSLIP_REF4)) {
            slip = RIGHTSLIP_REF4;
            viewLocation = Screen.getViewLocation(mRightBar_ref4);
        } else if (isSlipShow(RIGHTSLIP_REF5)) {
            slip = RIGHTSLIP_REF5;
            viewLocation = Screen.getViewLocation(mRightBar_ref5);
        } else if (isSlipShow(RIGHTSLIP_REF6)) {
            slip = RIGHTSLIP_REF6;
            viewLocation = Screen.getViewLocation(mRightBar_ref6);
        } else if (isSlipShow(RIGHTSLIP_REF7)) {
            slip = RIGHTSLIP_REF7;
            viewLocation = Screen.getViewLocation(mRightBar_ref7);
        } else if (isSlipShow(RIGHTSLIP_REF8)) {
            slip = RIGHTSLIP_REF8;
            viewLocation = Screen.getViewLocation(mRightBar_ref8);
        } else if (isSlipShow(RIGHTSLIP_S1)) {
            slip = RIGHTSLIP_S1;
            viewLocation = Screen.getViewLocation(mRightBar_serials1);
        } else if (isSlipShow(RIGHTSLIP_S2)) {
            slip = RIGHTSLIP_S2;
            viewLocation = Screen.getViewLocation(mRightBar_serials2);
        } else if (isSlipShow(RIGHTSLIP_S3)) {
            slip = RIGHTSLIP_S3;
            viewLocation = Screen.getViewLocation(mRightBar_serials3);
        } else if (isSlipShow(RIGHTSLIP_S4)) {
            slip = RIGHTSLIP_S4;
            viewLocation = Screen.getViewLocation(mRightBar_serials4);
        }
        else if (isSlipShow(BOTTOMSLIP)) {
            slip = BOTTOMSLIP;
            viewLocation = Screen.getViewLocation(mBottomBar_quick);
        }
        //由于模拟通道和其他通道显示区域已经按钮区域有变化，所以分开处理
        if (Math.abs(x - lastXIntercept) < MinClickRangePx && Math.abs(y - lastYIntercept) < MinClickRangePx//确认是点击反应
                && !isDialogsShow()) {//确认没有dialog显示
            boolean needHide = false;
            if (isRightSlipChannelShow()) {//模拟通道去除右侧区域
                if (x <= GlobalVar.get().getMainWave().x) {
                    needHide = true;
                }
            } else if (isRightSlipOtherShow()) {//去掉底部Math/Ref/Serials按钮显示区域
                Rect rect = new Rect(1098, GlobalVar.get().getMainWave().y, 1768, GlobalVar.get().getScreen().bottom);
                Rect rect2 = Screen.getViewLocation(findViewById(R.id.btn_other_channel));//Math/Ref/Bus三合一按钮
                if (!rect.contains(x, y) && !rect2.contains(x, y)) {
                    needHide = true;
                }
            } else {
                needHide = true;
            }

            if (needHide) {
                if (viewLocation != null && !viewLocation.contains(x, y) && !centerMenuLayout.containsPoint(x, y)) {
                    hideSlip(slip);
                }
            }
        }
    }

    //endregion

    /**
     * 滑动菜单方向标志
     * - 业务含义: 记录当前滑动菜单的操作方向（显示或隐藏）
     * - 使用场景: 在触摸事件处理中判断滑动菜单的操作方向
     * - 取值范围: SliderZone.SliderDir_None（无方向）、SliderZone.SliderDir_TopToBottom（从上到下）等
     */
    int slipDirection = SliderZone.SliderDir_None;

    /**
     * 处理滑动菜单的显示或隐藏（MOVE事件处理）
     * 
     * @param event 触摸事件对象
     * @param x 触摸点的X坐标（相对于父视图）
     * @param y 触摸点的Y坐标（相对于父视图）
     * @description 根据触摸点的移动距离和方向，动态调整滑动菜单的位置
     *              主要处理以下情况：
     *              1. 拖动显示菜单：当滑动方向与菜单显示方向一致时，逐步显示菜单
     *              2. 拖动隐藏菜单：当滑动方向与菜单隐藏方向一致时，逐步隐藏菜单
     *              3. 菜单互斥：显示一个菜单时，隐藏其他所有菜单
     * @调用时机 在触摸事件的ACTION_MOVE阶段调用
     * @关联方法 dealSliderZoneSelect - 选择滑动菜单区域
     * @关联方法 dealSliderZoneComplete - 处理滑动菜单完成后的状态
     * @关联类 SliderZone - 滑动菜单区域管理类
     * @重要逻辑 菜单显示和隐藏是互斥的，显示一个菜单时会隐藏其他所有菜单
     */
    private void dealSliderZone(MotionEvent event, int x, int y) {
        // 判断滑动方向（如果还没有确定方向）
        if (slipDirection == SliderZone.SliderDir_None) {
            slipDirection = SliderZone.getSlipDirection(lastXIntercept, lastYIntercept, x, y); // 根据起始点和当前点计算滑动方向
        }
        
        // 遍历所有滑动菜单区域
        for (SliderZone s : sliderZoneList) {
            // 处理拖动显示菜单的情况
            if (s.isEnable() && slipDirection == s.getSliderDir() && !s.isCurrShowState()) {//拖动显示
                // 除了下菜单操作时候不管，其它菜单操作的时候，显示的菜单进行隐藏
                // 现已变成所有slip互斥，通道选择条也需要跟所有slip互斥
//                if (s != sliderZone_bottom) {
                
                // 隐藏其他所有已显示的菜单（菜单互斥）
                for (SliderZone ss : sliderZoneList) {
                    if (s == ss) continue; // 跳过当前菜单
                    if (ss.isCurrShowState()) // 如果其他菜单正在显示
                        hideSliderZone(ss, ss.getHideLayout_Zone().left, ss.getHideLayout_Zone().top); // 隐藏其他菜单
                }
//                }

                // 根据滑动方向调整菜单位置
                switch (s.getSliderDir()) {
                    case SliderZone.SliderDir_BottomToTop: { // 从底部向顶部滑动（显示底部菜单）
                        int deltaY = lastYIntercept - s.getDownZone_Hide_Screen().top; // 计算Y轴偏移量
                        if (deltaY > 0 && s.getSliderViewGroup().getY() >= 0) {  //到了显示的位置,开始显示。
                            ViewGroup parentViewGroup = (ViewGroup) s.getSliderViewGroup().getParent(); // 获取父视图
                            deltaY = Math.max(y - lastYIntercept + s.getDownZone_Hide_Screen().height(), 0); // 计算新的Y位置
                            s.getSliderViewGroup().setY(deltaY); // 设置菜单的Y位置
                            s.cancelViewMotion_OutView(event, this); // 取消视图的触摸事件
                        }
                    }
                    break;
                    case SliderZone.SliderDir_TopToBottom: { // 从顶部向底部滑动（显示顶部菜单）
                        int deltaY = y - (lastYIntercept); // 计算Y轴偏移量
                        if (deltaY >= 0) { //到了显示的位置
                            ViewGroup parentViewGroup = (ViewGroup) s.getSliderViewGroup().getParent(); // 获取父视图
                            deltaY = deltaY > s.getSliderViewGroup().getHeight() ? s.getSliderViewGroup().getHeight() : deltaY; // 限制最大偏移量
                            s.getSliderViewGroup().setY(deltaY - s.getSliderViewGroup().getHeight()); // 设置菜单的Y位置（负值表示在顶部上方）
                        }
                    }
                    break;
                    case SliderZone.SliderDir_LeftToRight: { // 从左侧向右侧滑动（显示左侧菜单）
                        int deltaX = x - lastXIntercept; // 计算X轴偏移量
                        if (deltaX >= 0) {//到了显示的位置
                            s.getSliderViewGroup().setX(deltaX - s.getSliderViewGroup().getWidth()); // 设置菜单的X位置（负值表示在左侧左方）
                        }
                    }
                    break;
                    case SliderZone.SliderDir_RightToLeft: { // 从右侧向左侧滑动（显示右侧菜单）
                        int deltaX = s.getShowMenu_BeginPosion() - x; // 计算X轴偏移量
                        if (deltaX >= 0) { //注意这里x,y得用父窗口的宽高
                            ViewGroup parentViewGroup = (ViewGroup) s.getSliderViewGroup().getParent(); // 获取父视图
                            deltaX = deltaX > s.getSliderViewGroup().getWidth() ? s.getSliderViewGroup().getWidth() : deltaX; // 限制最大偏移量
                            s.getSliderViewGroup().setX(parentViewGroup.getWidth() - deltaX); // 设置菜单的X位置
                            event.setAction(MotionEvent.ACTION_CANCEL); // 设置事件为取消状态
                            //mainRight.dispatchTouchEvent(event); // 分发触摸事件到主右侧布局
                            mSlideLayout.dispatchTouchEvent(event); // 分发触摸事件到滑动布局
                        }
                    }
                    break;
                    default:
                        break;
                }
            }

            // 处理拖动隐藏菜单的情况
            if (s.isEnable() && slipDirection == ~s.getSliderDir() && s.isCurrShowState()) {//拖动隐藏
                switch (~s.getSliderDir()) {
                    case SliderZone.SliderDir_BottomToTop: { // 从顶部向底部滑动（隐藏顶部菜单）
                        int deltaY = s.getSliderViewGroup().getHeight() - (lastYIntercept - y); // 计算Y轴偏移量
                        if (deltaY > 0 && mTopBar_quick.getValidRect().contains(x, y)) {  //到了关闭的位置,开始关闭。
                            isTopSlipMoveBack = true; // 标记顶部菜单正在回拉
                            if (deltaY - s.getSliderViewGroup().getHeight() <= 0) { // 如果偏移量小于菜单高度
                                s.getSliderViewGroup().setY(deltaY - s.getSliderViewGroup().getHeight()); // 设置菜单的Y位置
                                s.cancelViewMotion_InView(event, s.getSliderViewGroup()); // 取消视图内部的触摸事件
                            } else {
                                s.getSliderViewGroup().setY(0); // 设置菜单的Y位置为0
                            }
                        }
                    }
                    break;
                    case SliderZone.SliderDir_TopToBottom: { // 从底部向顶部滑动（隐藏底部菜单）
                        int deltaY = y - lastYIntercept; // 计算Y轴偏移量
                        deltaY = deltaY > 0 ? deltaY : 0; // 确保偏移量为正数
                        s.getSliderViewGroup().setY(deltaY); // 设置菜单的Y位置
                        s.cancelViewMotion_InView(event, s.getSliderViewGroup()); // 取消视图内部的触摸事件
                    }
                    break;
                    case SliderZone.SliderDir_LeftToRight: { // 从右侧向左侧滑动（隐藏右侧菜单）
                        int deltaX = x - lastXIntercept; // 计算X轴偏移量
                        deltaX = deltaX > 0 ? deltaX : 0; // 确保偏移量为正数
                        ViewGroup parentViewGroup = (ViewGroup) s.getSliderViewGroup().getParent(); // 获取父视图
                        s.getSliderViewGroup().setX(parentViewGroup.getWidth() - s.getSliderViewGroup().getWidth() + deltaX); // 设置菜单的X位置
                        s.cancelViewMotion_InView(event, s.getSliderViewGroup()); // 取消视图内部的触摸事件
                    }
                    break;
                    case SliderZone.SliderDir_RightToLeft: { // 从左侧向右侧滑动（隐藏左侧菜单）
                        int deltaX = s.getSliderViewGroup().getWidth() - (lastXIntercept - x); // 计算X轴偏移量
                        if (deltaX >= 0) { //注意这里x,y得用父窗口的宽高
                            s.getSliderViewGroup().setX(deltaX - s.getSliderViewGroup().getWidth()); // 设置菜单的X位置
                            s.cancelViewMotion_InView(event, s.getSliderViewGroup()); // 取消视图内部的触摸事件
                        }
                    }
                    break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 处理滑动菜单选择（DOWN事件处理）
     * 
     * @param x 触摸点的X坐标（相对于父视图）
     * @param y 触摸点的Y坐标（相对于父视图）
     * @return boolean - true表示选中了某个滑动菜单区域，false表示未选中
     * @description 根据触摸点的位置，判断是否选中了某个滑动菜单区域
     *              主要处理以下情况：
     *              1. 隐藏状态的菜单：触摸点在菜单的隐藏触发区域内
     *              2. 显示状态的菜单：触摸点在菜单的显示区域内
     *              3. 右侧菜单分组：根据当前显示的页面（group1/group2/group3）判断是否启用
     * @调用时机 在触摸事件的ACTION_DOWN阶段调用
     * @关联方法 dealSliderZone - 处理滑动菜单的显示或隐藏
     * @关联类 SliderZone - 滑动菜单区域管理类
     * @重要逻辑 右侧菜单根据页面分组，只有当前页面的菜单才能被选中
     */
    private boolean dealSliderZoneSelect(int x, int y) {
        boolean isSelect = false; // 选中标志
        
        // 遍历所有滑动菜单区域
        for (SliderZone s : sliderZoneList) {
            // 处理隐藏状态的菜单（触摸点在隐藏触发区域内）
            if (s.getDownZone_Hide_Screen().contains(x, y)) {
                // 判断右侧菜单的分组（根据当前显示的页面）
                if (s.getNameTag().contains("right") && mSlideLayout.getCurViewIdx() == 2 && s.getNameTag().contains("group3")) {
                    // 当前显示第3页（group3），且菜单属于group3
                    s.setEnable(true); // 启用该菜单
                    isSelect = true; // 标记为选中
                } else if (s.getNameTag().contains("right") && mSlideLayout.getCurViewIdx() == 1 && s.getNameTag().contains("group2")) {
                    // 当前显示第2页（group2），且菜单属于group2
                    s.setEnable(true); // 启用该菜单
                    isSelect = true; // 标记为选中
                } else if (s.getNameTag().contains("right") && mSlideLayout.getCurViewIdx()==0 && s.getNameTag().contains("group1")) {
                    // 当前显示第1页（group1），且菜单属于group1
                    s.setEnable(true); // 启用该菜单
                    isSelect = true; // 标记为选中
                } else if (!s.getNameTag().contains("right")) {
                    // 不是右侧菜单（顶部、底部、左侧菜单）
                    s.setEnable(true); // 启用该菜单
                    isSelect = true; // 标记为选中
                }
            }
            
            // 处理显示状态的菜单（触摸点在显示区域内）
            if (s.getDownZone_Show_Sreen().contains(x, y) && s.isCurrShowState()) {
                s.setEnable(true); // 启用该菜单
                isSelect = true; // 标记为选中
            }
        }
//        for(SliderZone s : sliderZoneList){
//            Log.d("Tag.Debug", String.format("dealSliderZoneSelect: %s,%s, %s,%b",
//                    s.getNameTag(),
//                    (x+","+y),
//                    s.getDownZone_Hide_Screen().toString(),
//                    s.isEnable()));
//        }
        return isSelect; // 返回选中标志
    }

    private boolean isRightOtherVisible() {
        return !CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_RIGHTSWITCH_CHANNEL);
    }

    /**
     * 处理滑动菜单完成后的状态（UP事件处理）
     * 
     * @param x 触摸点的X坐标（相对于父视图）
     * @param y 触摸点的Y坐标（相对于父视图）
     * @return boolean - true表示拦截事件，false表示不拦截
     * @description 根据滑动菜单的位置和滑动速度，决定菜单的最终状态（显示或隐藏）
     *              主要处理以下情况：
     *              1. 顶部菜单：根据Y位置和滑动速度决定显示或隐藏
     *              2. 底部菜单：根据Y位置和滑动速度决定显示或隐藏
     *              3. 左侧菜单：根据X位置决定显示或隐藏
     *              4. 右侧菜单：根据X位置和滑动速度决定显示或隐藏
     *              5. Math/Ref/Serials菜单：特殊处理，减少误关闭
     * @调用时机 在触摸事件的ACTION_UP阶段调用
     * @关联方法 showSliderZone - 显示滑动菜单区域
     * @关联方法 hideSliderZone - 隐藏滑动菜单区域
     * @关联类 SliderZone - 滑动菜单区域管理类
     * @重要逻辑 滑动速度超过阈值时，直接决定菜单状态，否则根据位置判断
     */
    private boolean dealSliderZoneComplete(int x, int y) {
        boolean intercept = false; // 拦截标志
        Logger.i(TAG, "dealSliderZoneComplete!"); // 记录日志
        
        // 点击菜单中按键时，防止微滑动关闭菜单
        if (Math.abs(x - lastXIntercept) < SLIDE_DISTANCE_MIN && Math.abs(y - lastYIntercept) < SLIDE_DISTANCE_MIN) {
            velocityX = 0; // 清零X轴速度
            velocityY = 0; // 清零Y轴速度
        }
        
        // 遍历所有滑动菜单区域
        for (SliderZone s : sliderZoneList) {
            if (s.isEnable()) { // 只处理启用的菜单
                int width = s.getSliderViewGroup().getWidth(); // 获取菜单宽度
                int height = s.getSliderViewGroup().getHeight(); // 获取菜单高度
                boolean prevState = s.isCurrShowState(); // 记录菜单之前的状态
                
                // 处理顶部菜单
                if (s == sliderZone_top) {
                    if (velocityY > VELOCITY_MIN && slipDirection == SliderZone.SliderDir_TopToBottom) {
                        // Y轴速度超过阈值且方向为从上到下，显示菜单
                        showSliderZone(s, x, y);
                    } else if (velocityY < 0 - VELOCITY_MIN) {
                        // Y轴速度超过阈值且方向为从下到上，隐藏菜单
                        intercept = true; // 拦截事件
                        hideSliderZone(s, x, y);
                    } else if (s.getSliderViewGroup().getY() > -height / 3) {
                        // Y位置超过菜单高度的1/3，显示菜单
                        showSliderZone(s, x, y);
                    } else {
                        // Y位置未超过菜单高度的1/3，隐藏菜单
                        intercept = true; // 拦截事件
                        hideSliderZone(s, x, y);
                    }
                } 
                // 处理底部菜单
                else if (s == sliderZone_bottom) {
                    if (velocityY < 0 - VELOCITY_MIN && slipDirection == SliderZone.SliderDir_BottomToTop) {
                        // Y轴速度超过阈值且方向为从下到上，显示菜单
                        showSliderZone(s, x, y);
                    } else if (velocityY > VELOCITY_MIN && s.isCurrShowState()) {
                        // Y轴速度超过阈值且菜单正在显示，隐藏菜单
                        intercept = true; // 拦截事件
                        hideSliderZone(s, x, y);
                    } else if (s.getSliderViewGroup().getY() < height / 2) {
                        // Y位置小于菜单高度的1/2，显示菜单
                        showSliderZone(s, x, y);
                    } else {
                        // Y位置大于菜单高度的1/2，隐藏菜单
                        hideSliderZone(s, x, y);
                    }
                } 
                // 处理左侧菜单
                else if (s == sliderZone_left) {
                    if (s.getSliderViewGroup().getX() > width / 2) {
                        // X位置超过菜单宽度的1/2，显示菜单
                        showSliderZone(s, x, y);
                    } else {
                        // X位置未超过菜单宽度的1/2，隐藏菜单
                        hideSliderZone(s, x, y);
                    }
                } 
                // 处理右侧通道菜单（CH1-CH8）
                else if (s == sliderZone_rightCh1 || s == sliderZone_rightCh2
                        || s == sliderZone_rightCh3 || s == sliderZone_rightCh4
                        || s == sliderZone_rightCh5 || s == sliderZone_rightCh6
                        || s == sliderZone_rightCh7 || s == sliderZone_rightCh8
                ) {
                    if (velocityX < 0 - VELOCITY_MIN && slipDirection == SliderZone.SliderDir_RightToLeft) {
                        // X轴速度超过阈值且方向为从右到左，显示菜单
                        showSliderZone(s, x, y);
                    } else if (velocityX > VELOCITY_MIN && s.isCurrShowState()) {
                        // X轴速度超过阈值且菜单正在显示，隐藏菜单
                        intercept = true; // 拦截事件
                        hideSliderZone(s, x, y);
                    } else if (s.getSliderViewGroup().getX() < (GlobalVar.get().getMainWave().x - width / 2)) {
                        // X位置小于波形区域X坐标减去菜单宽度的一半，显示菜单
                        showSliderZone(s, x, y);
                    } else {
                        // X位置大于波形区域X坐标减去菜单宽度的一半，隐藏菜单
                        hideSliderZone(s, x, y);
                    }

                } 
                // 处理右侧Math/Ref/Serials菜单（特殊处理，减少误关闭）
                else if (s == sliderZone_rightMath1 || s == sliderZone_rightMath2
                        || s == sliderZone_rightMath3 || s == sliderZone_rightMath4
                        || s == sliderZone_rightMath5 || s == sliderZone_rightMath6
                        || s == sliderZone_rightMath7 || s == sliderZone_rightMath8
                        || s == sliderZone_rightRef1 || s == sliderZone_rightRef2
                        || s == sliderZone_rightRef3 || s == sliderZone_rightRef4
                        || s == sliderZone_rightRef5 || s == sliderZone_rightRef6
                        || s == sliderZone_rightRef7 || s == sliderZone_rightRef8
                        || s == sliderZone_rightS1 || s == sliderZone_rightS2
                        || s == sliderZone_rightS3 || s == sliderZone_rightS4
                ) { //Math/Ref/Serials 主要为了减少滑动Ref列表时 误关闭菜单
                    Logger.d(TAG, "velocityX= " + velocityX + " ,velocityY= " + velocityY); // 记录速度日志
                    if (velocityX > VELOCITY_MIN && (Math.abs(velocityY) + 100) < Math.abs(velocityX) && s.isCurrShowState()) {
                        // X轴速度超过阈值且Y轴速度较小，隐藏菜单（防止滑动列表时误关闭）
                        intercept = true; // 拦截事件
                        hideSliderZone(s, x, y);
                    } else if (s.getSliderViewGroup().getX() < (GlobalVar.get().getMainWave().x - width / 2)) {
                        // X位置小于波形区域X坐标减去菜单宽度的一半，显示菜单
                        showSliderZone(s, x, y);
                    }
//                    else {
//                        hideSliderZone(s, x, y);
//                    }
                }
                
                // 播放滑动音效（当菜单状态改变且移动距离超过阈值时）
                if (s.isCurrShowState() != prevState &&
                        (Math.abs(lastXIntercept - x) >= 2 || Math.abs(lastYIntercept - y) >= 2)) {
                    PlaySound.getInstance().playSlide(); // 播放滑动音效
                }
            }


//            if (s.isEnabled() == true && slipDirection == s.getSliderDir()) {
//                showSliderZone(s, x, y);
//            }
//            if (s.isEnabled() == true && slipDirection == ~s.getSliderDir()) {
//                if (~s.getSliderDir() == SliderZone.SliderDir_BottomToTop) {//topSLip页面回拉时，拦截继续向子控件的继续响应
//                    intercept = true;
//                    if (isTopSlipMoveBack && mTopBar_quick.getValidRect().contains(x, y)) {
//                        hideSliderZone(s, x, y);
//                    }
//                } else {
//                    hideSliderZone(s, x, y);
//                }
//            }
            //如果右菜单显示
            s.setEnable(false); // 禁用菜单

        }
        return intercept; // 返回拦截标志
    }

    /**
     * 显示滑动菜单区域
     * 
     * @param sliderZone 滑动菜单区域对象
     * @param x 触摸点的X坐标（用于动画，-1表示无动画）
     * @param y 触摸点的Y坐标（用于动画，-1表示无动画）
     * @description 将滑动菜单移动到显示位置，并更新菜单状态
     *              主要执行以下操作：
     *              1. 设置菜单的位置（X和Y坐标）
     *              2. 更新菜单的显示状态标志
     *              3. 禁用菜单的滑动功能
     *              4. 隐藏通道选择视图
     *              5. 禁用波形选项
     *              6. 发送菜单打开消息到RxBus
     * @调用时机 在滑动菜单完成处理后，需要显示菜单时调用
     * @关联方法 hideSliderZone - 隐藏滑动菜单区域
     * @关联方法 postMenuMsg - 发送菜单消息到RxBus
     * @关联类 SliderZone - 滑动菜单区域管理类
     * @重要逻辑 显示菜单时，会禁用菜单的滑动功能，防止重复触发
     */
    private void showSliderZone(SliderZone sliderZone, int x, int y) {
        // 调试日志（底部菜单）
        if (sliderZone == sliderZone_bottom) {
            Logger.i("aa"); // 记录日志
        }
        
        // 获取菜单显示位置的区域
        int left = sliderZone.getShowLayout_Zone().left; // 左边界
        int top = sliderZone.getShowLayout_Zone().top; // 上边界
        int right = sliderZone.getShowLayout_Zone().right; // 右边界
        int bottom = sliderZone.getShowLayout_Zone().bottom; // 下边界
        Logger.i("showSliderZone.Rect=" + sliderZone.getShowLayout_Zone() + ",setY= " + top); // 记录位置日志
        
        // 设置菜单的位置
        sliderZone.getSliderViewGroup().setX(left); // 设置X坐标
        sliderZone.getSliderViewGroup().setY(top); // 设置Y坐标
        
        // 更新菜单状态
        sliderZone.setCurrShowState(true); // 设置为显示状态
        sliderZone.setEnable(false); // 禁用菜单的滑动功能

        // 隐藏通道选择视图（菜单互斥）
        hideChannelSelectView();
        
        // 禁用波形选项（防止波形区域干扰菜单操作）
        setEnableWaveOption(false);

//        boolean isSerialTxt= CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);
//        if (isSerialTxt) return;
        
        // 发送菜单打开消息到RxBus
        postMenuMsg(sliderZone,true); // 发送消息，参数true表示打开菜单

    }

    private void postMenuMsg(SliderZone sliderZone, boolean isOpen) {
        switch (sliderZone.getNameTag()) {
            case "sliderZone_top":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(TOPSLIP, isOpen));
                break;
            case "sliderZone_group1_rightCh1":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_CH1, isOpen));
                break;
            case "sliderZone_group1_rightCh2":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_CH2, isOpen));
                break;
            case "sliderZone_group1_rightCh3":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_CH3, isOpen));
                break;
            case "sliderZone_group1_rightCh4":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_CH4, isOpen));
                break;
            case "sliderZone_group1_rightCh5":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_CH5, isOpen));
                break;
            case "sliderZone_group1_rightCh6":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_CH6, isOpen));
                break;
            case "sliderZone_group1_rightCh7":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_CH7, isOpen));
                break;
            case "sliderZone_group1_rightCh8":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_CH8, isOpen));
                break;
            case "sliderZone_group2_rightMath1":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_MATH1, isOpen));
                break;
            case "sliderZone_group2_rightMath2":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_MATH2, isOpen));
                break;
            case "sliderZone_group2_rightMath3":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_MATH3, isOpen));
                break;
            case "sliderZone_group2_rightMath4":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_MATH4, isOpen));
                break;
            case "sliderZone_group2_rightMath5":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_MATH5, isOpen));
                break;
            case "sliderZone_group2_rightMath6":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_MATH6, isOpen));
                break;
            case "sliderZone_group2_rightMath7":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_MATH7, isOpen));
                break;
            case "sliderZone_group2_rightMath8":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_MATH8, isOpen));
                break;
            case "sliderZone_group2_rightRef1":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_REF1, isOpen));
                break;
            case "sliderZone_group2_rightRef2":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_REF2, isOpen));
                break;
            case "sliderZone_group2_rightRef3":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_REF3, isOpen));
                break;
            case "sliderZone_group2_rightRef4":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_REF4, isOpen));
                break;
            case "sliderZone_group2_rightRef5":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_REF5, isOpen));
                break;
            case "sliderZone_group2_rightRef6":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_REF6, isOpen));
                break;
            case "sliderZone_group2_rightRef7":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_REF7, isOpen));
                break;
            case "sliderZone_group2_rightRef8":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_REF8, isOpen));
                break;
            case "sliderZone_group3_rightS1":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_S1, isOpen));
                break;
            case "sliderZone_group3_rightS2":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_S2, isOpen));
                break;
            case "sliderZone_group3_rightS3":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_S3, isOpen));
                break;
            case "sliderZone_group3_rightS4":
                RxBus.getInstance().post(RxEnum.MAIN_SLIP_TO_OTHER, new MainMsgSlip(RIGHTSLIP_S4, isOpen));
                break;

        }
    }

    /**
     * 隐藏滑动菜单区域
     * 
     * @param sliderZone 滑动菜单区域对象
     * @param x 触摸点的X坐标（用于动画，-1表示无动画）
     * @param y 触摸点的Y坐标（用于动画，-1表示无动画）
     * @description 将滑动菜单移动到隐藏位置，并更新菜单状态
     *              主要执行以下操作：
     *              1. 检查菜单对象和隐藏区域是否有效
     *              2. 隐藏外部按键预选框（如果菜单从显示变为隐藏）
     *              3. 隐藏中心菜单（如果是底部菜单）
     *              4. 隐藏触发电平菜单（如果触发电平菜单正在显示）
     *              5. 设置菜单的位置（X和Y坐标）
     *              6. 更新菜单的隐藏状态标志
     *              7. 禁用菜单的滑动功能
     *              8. 启用波形选项
     *              9. 发送菜单关闭消息到RxBus
     *              10. 隐藏文件选择对话框
     * @调用时机 在滑动菜单完成处理后，需要隐藏菜单时调用
     * @关联方法 showSliderZone - 显示滑动菜单区域
     * @关联方法 postMenuMsg - 发送菜单消息到RxBus
     * @关联类 SliderZone - 滑动菜单区域管理类
     * @重要逻辑 隐藏菜单时，会启用波形选项，恢复波形区域的操作
     */
    private void hideSliderZone(SliderZone sliderZone, int x, int y) {
        // 检查菜单对象和隐藏区域是否有效
        if (sliderZone==null || sliderZone.getHideLayout_Zone()==null) return; // 如果无效则直接返回
        
        // 隐藏外部按键预选框（当菜单从显示变为隐藏时）
        if (sliderZone.isCurrShowState()) { //当有slip从显示变为隐藏时，让外部按键预选框消失
            ExternalKeysUI.getInstance().setFocusViewVisible(false); // 隐藏外部按键预选框
            
            // 隐藏中心菜单（如果是底部菜单）
            if (sliderZone == sliderZone_bottom) {
                hideCenterMenuAndCenterHalf(); // 隐藏中心菜单和中心半屏
            }
        }
//        if (sliderZone.getNameTag().equals(sliderZone_rightRef.getNameTag())) {
//            Logger.d(TAG, "hideSliderZone() called with: sliderZone = [" + sliderZone.getNameTag() + "], x = [" + x + "], y = [" + y + "]");
//            try {
//                throw new IllegalAccessException();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
        
        // 隐藏触发电平菜单（如果触发电平菜单正在显示）
        if (isLevelMenu() && triggerLevel.getTriggerLevel_Mode_Show() == MTriggerLevel.TriggerLevel_Mode_Show_Button) {
            hideLevelMenu(); // 隐藏触发电平菜单
        }
        
        // 获取菜单隐藏位置的区域
        int left = sliderZone.getHideLayout_Zone().left; // 左边界
        int top = sliderZone.getHideLayout_Zone().top; // 上边界
        int right = sliderZone.getHideLayout_Zone().right; // 右边界
        int bottom = sliderZone.getHideLayout_Zone().bottom; // 下边界

//        Logger.d(TAG,"left:" + left + ",top:" + top);

        // 设置菜单的位置
        sliderZone.getSliderViewGroup().setX(left); // 设置X坐标
        sliderZone.getSliderViewGroup().setY(top); // 设置Y坐标
        
        // 更新菜单状态
        sliderZone.setCurrShowState(false); // 设置为隐藏状态
        sliderZone.setEnable(false); // 禁用菜单的滑动功能

        // 启用波形选项（恢复波形区域的操作）
        setEnableWaveOption(true);
        
        // 发送菜单关闭消息到RxBus
        postMenuMsg(sliderZone,false); // 发送消息，参数false表示关闭菜单
        
        // 隐藏文件选择对话框
        hideFileSelectDialog();
    }

    private void hideChannelSelectView() {
        ExternalKeysManager.get().moveBackChannelsSelect();
    }

    private void initPublicSlipMenu() {
        GlobalVar var = GlobalVar.get();
        mBottomBar_quick = (ConstraintLayout) findViewById(R.id.mBottomBar_Quick);
        mBottomBar_quick.bringToFront();
        ViewUtils.doAfterLayout(mBottomBar_quick, ()-> sliderZone_bottom.createParam(false));
        sliderZone_bottom = new SliderZone();
        sliderZone_bottom.setEnable(false);
        sliderZone_bottom.setEnableSlip(true);
        sliderZone_bottom.setDownZone_Hide_Screen(new Rect(
                0,
                var.getScreen().height() - var.getBottomSlip().y + 20,
                var.getMainWave().x,
                var.getScreen().height()));
        sliderZone_bottom.setCurrShowState(false);
        sliderZone_bottom.setShowMenu_BeginPosion(var.getScreen().height());
        sliderZone_bottom.setSliderDir(SliderZone.SliderDir_BottomToTop);
        sliderZone_bottom.setSliderViewGroup(mBottomBar_quick);
        sliderZone_bottom.setNameTag("sliderZone_bottom");

        ViewStub mTopBar_quick_viewStub = (ViewStub) findViewById(R.id.topSlipMenuBar_Quick_viewStub);
        if (mTopBar_quick_viewStub != null) {
            mTopBar_quick_viewStub.inflate();
        }
        mTopBar_quick = (TopLayoutPopWindow) findViewById(R.id.topSlipMenuBar_Quick);
        mTopBar_quick.bringToFront();
        ViewUtils.doAfterLayout(mBottomBar_quick, ()-> sliderZone_top.createParam(false));
        sliderZone_top = new SliderZone();
        sliderZone_top.setEnable(false);
        sliderZone_top.setEnableSlip(true);
        sliderZone_top.setDownZone_Hide_Screen(new Rect(0, 0, var.getMainWave().x
                , var.getMainTop().y - 2));//-2的原因是：防止正好点击到边界的时候，slip与channel一起滑动
        sliderZone_top.setCurrShowState(false);
        sliderZone_top.setShowMenu_BeginPosion(var.getMainTop().y);
        sliderZone_top.setSliderDir(SliderZone.SliderDir_TopToBottom);
        sliderZone_top.setSliderViewGroup(mTopBar_quick);
        sliderZone_top.setNameTag("sliderZone_top");

        LinearLayout mLeftBar_quick = (LinearLayout) findViewById(R.id.leftSlipMenuBar_Quick);
        mLeftBar_quick.bringToFront();
        ViewUtils.doAfterLayout(mLeftBar_quick, () ->sliderZone_left.createParam(false));
        sliderZone_left = new SliderZone();
        sliderZone_left.setEnable(false);
        sliderZone_left.setEnableSlip(true);
        boolean isLeftEnable = false;//左侧菜单是否可以打开
        if (isLeftEnable) {
            sliderZone_left.setDownZone_Hide_Screen(new Rect(0, var.getMainTop().y, var.getMainTop().y, 600 - 68));
        } else {
            sliderZone_left.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
        }
        sliderZone_left.setCurrShowState(false);
        sliderZone_left.setShowMenu_BeginPosion(0);
        sliderZone_left.setSliderDir(SliderZone.SliderDir_LeftToRight);
        sliderZone_left.setSliderViewGroup(mLeftBar_quick);
        sliderZone_left.setNameTag("sliderZone_left");

        ViewStub mRightBar_channel1_viewStub = (ViewStub) findViewById(R.id.rightslipmenubar_channel1_viewStub);
        if (mRightBar_channel1_viewStub != null) {
            mRightBar_channel1 = (RightLayoutChannel) mRightBar_channel1_viewStub.inflate();
        }
        if(mRightBar_channel1 == null) {
            mRightBar_channel1 = (RightLayoutChannel) findViewById(R.id.rightslipmenubar_channel1);
        }
        mRightBar_channel1.bringToFront();
        ViewUtils.doAfterLayout(mRightBar_channel1, () ->sliderZone_rightCh1.createParam(false));
        sliderZone_rightCh1 = new SliderZone();
        sliderZone_rightCh1.setEnable(false);
        sliderZone_rightCh1.setEnableSlip(true);
        sliderZone_rightCh1.setCurrShowState(false);

        if (channelCount == GlobalVar.CHANNEL_COUNT_2) {
            sliderZone_rightCh1.setSliderZoneFromBtn(findViewById(R.id.rightCh1MasterDouble));
        } else if (channelCount == GlobalVar.CHANNEL_COUNT_4) {
            sliderZone_rightCh1.setSliderZoneFromBtn(findViewById(R.id.rightCh1Master));
        } else {
            sliderZone_rightCh1.setSliderZoneFromBtn(findViewById(R.id.rightCh1MasterEight));
        }
        if (sliderZone_rightCh1.isEnableSlip()) {
//            sliderZone_rightCh1.setDownZone_Hide_Screen(new Rect(
//                    var.getMainWave().x + 2,
//                    var.getMainTop().y,
//                    var.getScreen().width(),
//                    var.getMainTop().y + var.getMainWave().y / channelCount));
            sliderZone_rightCh1.setDownZone_Hide_Screen(Screen.getViewLocation(mRightBar_channel1_viewStub));
        } else {
            sliderZone_rightCh1.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
        }
        sliderZone_rightCh1.setShowMenu_BeginPosion(var.getMainWave().x);
        sliderZone_rightCh1.setSliderDir(SliderZone.SliderDir_RightToLeft);
        sliderZone_rightCh1.setSliderViewGroup(mRightBar_channel1);
        sliderZone_rightCh1.setNameTag("sliderZone_group1_rightCh1");

        ViewStub mRightBar_channel2_viewStub = (ViewStub) findViewById(R.id.rightslipmenubar_channel2_viewStub);
        if (mRightBar_channel2_viewStub != null) {
            mRightBar_channel2 = (RightLayoutChannel) mRightBar_channel2_viewStub.inflate();
        }
        if(mRightBar_channel2 == null) {
            mRightBar_channel2 = (RightLayoutChannel) findViewById(R.id.rightslipmenubar_channel2);
        }
        mRightBar_channel2.bringToFront();
        ViewUtils.doAfterLayout(mRightBar_channel2, () ->sliderZone_rightCh2.createParam(false));
        sliderZone_rightCh2 = new SliderZone();
        sliderZone_rightCh2.setEnable(false);
        sliderZone_rightCh2.setEnableSlip(true);
        sliderZone_rightCh2.setCurrShowState(false);

        if (channelCount == GlobalVar.CHANNEL_COUNT_2) {
            sliderZone_rightCh2.setSliderZoneFromBtn(findViewById(R.id.rightCh2MasterDouble));
        } else if (channelCount == GlobalVar.CHANNEL_COUNT_4) {
            sliderZone_rightCh2.setSliderZoneFromBtn(findViewById(R.id.rightCh2Master));
        } else {
            sliderZone_rightCh2.setSliderZoneFromBtn(findViewById(R.id.rightCh2MasterEight));
        }
        if (sliderZone_rightCh2.isEnableSlip()) {
//            sliderZone_rightCh2.setDownZone_Hide_Screen(new Rect(
//                    var.getMainWave().x + 2,
//                    var.getMainTop().y + var.getMainWave().y / channelCount,
//                    var.getScreen().width(),
//                    var.getMainTop().y + var.getMainWave().y / channelCount * 2));
            sliderZone_rightCh1.setDownZone_Hide_Screen(Screen.getViewLocation(mRightBar_channel2_viewStub));
        } else {
            sliderZone_rightCh2.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
        }
        sliderZone_rightCh2.setShowMenu_BeginPosion(var.getMainWave().x);
        sliderZone_rightCh2.setSliderDir(SliderZone.SliderDir_RightToLeft);
        sliderZone_rightCh2.setSliderViewGroup(mRightBar_channel2);
        sliderZone_rightCh2.setNameTag("sliderZone_group1_rightCh2");

        ViewStub mRightBar_channel3_viewStub = (ViewStub) findViewById(R.id.rightslipmenubar_channel3_viewStub);
        if (mRightBar_channel3_viewStub != null) {
            mRightBar_channel3 = (RightLayoutChannel) mRightBar_channel3_viewStub.inflate();
        }
        if(mRightBar_channel3 == null) {
            mRightBar_channel3 = (RightLayoutChannel) findViewById(R.id.rightslipmenubar_channel3);
        }
        mRightBar_channel3.bringToFront();
        ViewUtils.doAfterLayout(mRightBar_channel3, () ->sliderZone_rightCh3.createParam(false));
        sliderZone_rightCh3 = new SliderZone();
        sliderZone_rightCh3.setEnable(false);
        sliderZone_rightCh3.setEnableSlip(true);
        sliderZone_rightCh3.setCurrShowState(false);
        if (channelCount == GlobalVar.CHANNEL_COUNT_4) {
            sliderZone_rightCh3.setSliderZoneFromBtn(findViewById(R.id.rightCh3Master));
        } else {
            sliderZone_rightCh3.setSliderZoneFromBtn(findViewById(R.id.rightCh3MasterEight));
        }

        if (sliderZone_rightCh3.isEnableSlip()) {
            if (channelCount == GlobalVar.CHANNEL_COUNT_2) {
                sliderZone_rightCh3.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
            } else {
//                sliderZone_rightCh3.setDownZone_Hide_Screen(new Rect(
//                        var.getMainWave().x + 2,
//                        var.getMainTop().y + var.getMainWave().y / channelCount * 2,
//                        var.getScreen().width(),
//                        var.getMainTop().y + var.getMainWave().y / channelCount * 3));
                sliderZone_rightCh1.setDownZone_Hide_Screen(Screen.getViewLocation(mRightBar_channel3_viewStub));
            }
        } else {
            sliderZone_rightCh3.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
        }
        sliderZone_rightCh3.setShowMenu_BeginPosion(var.getMainWave().x);
        sliderZone_rightCh3.setSliderDir(SliderZone.SliderDir_RightToLeft);
        sliderZone_rightCh3.setSliderViewGroup(mRightBar_channel3);
        sliderZone_rightCh3.setNameTag("sliderZone_group1_rightCh3");

        ViewStub mRightBar_channel4_viewStub = (ViewStub) findViewById(R.id.rightslipmenubar_channel4_viewStub);
        if (mRightBar_channel4_viewStub != null) {
            mRightBar_channel4 = (RightLayoutChannel) mRightBar_channel4_viewStub.inflate();
        }
        if(mRightBar_channel4 == null) {
            mRightBar_channel4 = (RightLayoutChannel) findViewById(R.id.rightslipmenubar_channel4);
        }
        mRightBar_channel4.bringToFront();
        ViewUtils.doAfterLayout(mRightBar_channel4, () ->sliderZone_rightCh4.createParam(false));
        sliderZone_rightCh4 = new SliderZone();
        sliderZone_rightCh4.setEnable(false);
        sliderZone_rightCh4.setEnableSlip(true);
        sliderZone_rightCh4.setCurrShowState(false);
        if (channelCount == GlobalVar.CHANNEL_COUNT_4) {
            sliderZone_rightCh4.setSliderZoneFromBtn(findViewById(R.id.rightCh4Master));
        } else {
            sliderZone_rightCh4.setSliderZoneFromBtn(findViewById(R.id.rightCh4MasterEight));
        }
        if (sliderZone_rightCh4.isEnableSlip()) {
            if (channelCount == GlobalVar.CHANNEL_COUNT_2) {
                sliderZone_rightCh4.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
            } else {
//                sliderZone_rightCh4.setDownZone_Hide_Screen(new Rect(
//                        var.getMainWave().x + 2,
//                        var.getMainTop().y + var.getMainWave().y / channelCount * 3,
//                        var.getScreen().width(),
//                        var.getMainTop().y + var.getMainWave().y / channelCount * 4));
                sliderZone_rightCh1.setDownZone_Hide_Screen(Screen.getViewLocation(mRightBar_channel4_viewStub));
            }
        } else {
            sliderZone_rightCh4.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
        }
        sliderZone_rightCh4.setShowMenu_BeginPosion(var.getMainWave().x);
        sliderZone_rightCh4.setSliderDir(SliderZone.SliderDir_RightToLeft);
        sliderZone_rightCh4.setSliderViewGroup(mRightBar_channel4);
        sliderZone_rightCh4.setNameTag("sliderZone_group1_rightCh4");

        ViewStub mRightBar_channel5_viewStub = (ViewStub) findViewById(R.id.rightslipmenubar_channel5_viewStub);
        if (mRightBar_channel5_viewStub != null) {
            mRightBar_channel5 = (RightLayoutChannel) mRightBar_channel5_viewStub.inflate();
        }
        if(mRightBar_channel5 == null) {
            mRightBar_channel5 = (RightLayoutChannel) findViewById(R.id.rightslipmenubar_channel5);
        }
        mRightBar_channel5.bringToFront();
        ViewUtils.doAfterLayout(mRightBar_channel5, () ->sliderZone_rightCh5.createParam(false));
        sliderZone_rightCh5 = new SliderZone();
        sliderZone_rightCh5.setEnable(false);
        sliderZone_rightCh5.setEnableSlip(true);
        sliderZone_rightCh5.setCurrShowState(false);
        if (channelCount == GlobalVar.CHANNEL_COUNT_8) {
            sliderZone_rightCh5.setSliderZoneFromBtn(findViewById(R.id.rightCh5MasterEight));
        }
        if (sliderZone_rightCh5.isEnableSlip()) {
            if (channelCount == GlobalVar.CHANNEL_COUNT_2 || channelCount == GlobalVar.CHANNEL_COUNT_4) {
                sliderZone_rightCh5.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
            } else {
//                sliderZone_rightCh5.setDownZone_Hide_Screen(new Rect(
//                        var.getMainWave().x + 2,
//                        var.getMainTop().y + var.getMainWave().y / channelCount * 4,
//                        var.getScreen().width(),
//                        var.getMainTop().y + var.getMainWave().y / channelCount * 5));
                sliderZone_rightCh1.setDownZone_Hide_Screen(Screen.getViewLocation(mRightBar_channel5_viewStub));
            }
        } else {
            sliderZone_rightCh5.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
        }
        sliderZone_rightCh5.setShowMenu_BeginPosion(var.getMainWave().x);
        sliderZone_rightCh5.setSliderDir(SliderZone.SliderDir_RightToLeft);
        sliderZone_rightCh5.setSliderViewGroup(mRightBar_channel5);
        sliderZone_rightCh5.setNameTag("sliderZone_group1_rightCh5");

        ViewStub mRightBar_channel6_viewStub = (ViewStub) findViewById(R.id.rightslipmenubar_channel6_viewStub);
        if (mRightBar_channel6_viewStub != null) {
            mRightBar_channel6 = (RightLayoutChannel) mRightBar_channel6_viewStub.inflate();
        }
        if(mRightBar_channel6 == null) {
            mRightBar_channel6 = (RightLayoutChannel) findViewById(R.id.rightslipmenubar_channel6);
        }
        mRightBar_channel6.bringToFront();
        ViewUtils.doAfterLayout(mRightBar_channel6, () ->sliderZone_rightCh6.createParam(false));
        sliderZone_rightCh6 = new SliderZone();
        sliderZone_rightCh6.setEnable(false);
        sliderZone_rightCh6.setEnableSlip(true);
        sliderZone_rightCh6.setCurrShowState(false);
        if (channelCount == GlobalVar.CHANNEL_COUNT_8) {
            sliderZone_rightCh6.setSliderZoneFromBtn(findViewById(R.id.rightCh6MasterEight));
        }
        if (sliderZone_rightCh6.isEnableSlip()) {
            if (channelCount == GlobalVar.CHANNEL_COUNT_2 || channelCount == GlobalVar.CHANNEL_COUNT_4) {
                sliderZone_rightCh6.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
            } else {
//                sliderZone_rightCh6.setDownZone_Hide_Screen(new Rect(
//                        var.getMainWave().x + 2,
//                        var.getMainTop().y + var.getMainWave().y / channelCount * 5,
//                        var.getScreen().width(),
//                        var.getMainTop().y + var.getMainWave().y / channelCount * 6));
                sliderZone_rightCh1.setDownZone_Hide_Screen(Screen.getViewLocation(mRightBar_channel6_viewStub));
            }
        } else {
            sliderZone_rightCh6.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
        }
        sliderZone_rightCh6.setShowMenu_BeginPosion(var.getMainWave().x);
        sliderZone_rightCh6.setSliderDir(SliderZone.SliderDir_RightToLeft);
        sliderZone_rightCh6.setSliderViewGroup(mRightBar_channel6);
        sliderZone_rightCh6.setNameTag("sliderZone_group1_rightCh6");

        ViewStub mRightBar_channel7_viewStub = (ViewStub) findViewById(R.id.rightslipmenubar_channel7_viewStub);
        if (mRightBar_channel7_viewStub != null) {
            mRightBar_channel7 = (RightLayoutChannel) mRightBar_channel7_viewStub.inflate();
        }
        if(mRightBar_channel7 == null) {
            mRightBar_channel7 = (RightLayoutChannel) findViewById(R.id.rightslipmenubar_channel7);
        }
        mRightBar_channel7.bringToFront();
        ViewUtils.doAfterLayout(mRightBar_channel7, () ->sliderZone_rightCh7.createParam(false));
        sliderZone_rightCh7 = new SliderZone();
        sliderZone_rightCh7.setEnable(false);
        sliderZone_rightCh7.setEnableSlip(true);
        sliderZone_rightCh7.setCurrShowState(false);
        if (channelCount == GlobalVar.CHANNEL_COUNT_8) {
            sliderZone_rightCh7.setSliderZoneFromBtn(findViewById(R.id.rightCh7MasterEight));
        }
        if (sliderZone_rightCh7.isEnableSlip()) {
            if (channelCount == GlobalVar.CHANNEL_COUNT_2 || channelCount == GlobalVar.CHANNEL_COUNT_4) {
                sliderZone_rightCh7.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
            } else {
//                sliderZone_rightCh7.setDownZone_Hide_Screen(new Rect(
//                        var.getMainWave().x + 2,
//                        var.getMainTop().y + var.getMainWave().y / channelCount * 6,
//                        var.getScreen().width(),
//                        var.getMainTop().y + var.getMainWave().y / channelCount * 7));
                sliderZone_rightCh1.setDownZone_Hide_Screen(Screen.getViewLocation(mRightBar_channel7_viewStub));
            }
        } else {
            sliderZone_rightCh7.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
        }
        sliderZone_rightCh7.setShowMenu_BeginPosion(var.getMainWave().x);
        sliderZone_rightCh7.setSliderDir(SliderZone.SliderDir_RightToLeft);
        sliderZone_rightCh7.setSliderViewGroup(mRightBar_channel7);
        sliderZone_rightCh7.setNameTag("sliderZone_group1_rightCh7");

        ViewStub mRightBar_channel8_viewStub = (ViewStub) findViewById(R.id.rightslipmenubar_channel8_viewStub);
        if (mRightBar_channel8_viewStub != null) {
            mRightBar_channel8 = (RightLayoutChannel) mRightBar_channel8_viewStub.inflate();
        }
        if(mRightBar_channel8 == null) {
            mRightBar_channel8 = (RightLayoutChannel) findViewById(R.id.rightslipmenubar_channel8);
        }
        mRightBar_channel8.bringToFront();
        ViewUtils.doAfterLayout(mRightBar_channel8, () ->sliderZone_rightCh8.createParam(false));
        sliderZone_rightCh8 = new SliderZone();
        sliderZone_rightCh8.setEnable(false);
        sliderZone_rightCh8.setEnableSlip(true);
        sliderZone_rightCh8.setCurrShowState(false);
        if (channelCount == GlobalVar.CHANNEL_COUNT_8) {
            sliderZone_rightCh8.setSliderZoneFromBtn(findViewById(R.id.rightCh8MasterEight));
        }
        if (sliderZone_rightCh8.isEnableSlip()) {
            if (channelCount == GlobalVar.CHANNEL_COUNT_2 || channelCount == GlobalVar.CHANNEL_COUNT_4) {
                sliderZone_rightCh8.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
            } else {
//                sliderZone_rightCh8.setDownZone_Hide_Screen(new Rect(
//                        var.getMainWave().x + 2,
//                        var.getMainTop().y + var.getMainWave().y / channelCount * 7,
//                        var.getScreen().width(),
//                        var.getMainTop().y + var.getMainWave().y / channelCount * 8));
                sliderZone_rightCh1.setDownZone_Hide_Screen(Screen.getViewLocation(mRightBar_channel8_viewStub));
            }
        } else {
            sliderZone_rightCh8.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
        }

        sliderZone_rightCh8.setShowMenu_BeginPosion(var.getMainWave().x);
        sliderZone_rightCh8.setSliderDir(SliderZone.SliderDir_RightToLeft);
        sliderZone_rightCh8.setSliderViewGroup(mRightBar_channel8);
        sliderZone_rightCh8.setNameTag("sliderZone_group1_rightCh8");

        ViewStub mRightBar_Math1_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_math1_viewStub);
        if (mRightBar_Math1_viewStub != null) {
            mRightBar_math1 = (RightLayoutMath) mRightBar_Math1_viewStub.inflate();
        }
        if (mRightBar_math1 == null) {
            mRightBar_math1 = (RightLayoutMath) findViewById(R.id.rightSlipMenuBar_math1);
        }
        mRightBarMaths.add(0, mRightBar_math1);
        sliderZone_rightMath1 = new SliderZone();
        mSlideZoneMaths.add(0, sliderZone_rightMath1);

        ViewStub mRightBar_Math2_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_math2_viewStub);
        if (mRightBar_Math2_viewStub != null) {
            mRightBar_math2 = (RightLayoutMath) mRightBar_Math2_viewStub.inflate();
        }
        if (mRightBar_math2 == null) {
            mRightBar_math2 = (RightLayoutMath) findViewById(R.id.rightSlipMenuBar_math2);
        }
        mRightBarMaths.add(1, mRightBar_math2);
        sliderZone_rightMath2 = new SliderZone();
        mSlideZoneMaths.add(1, sliderZone_rightMath2);

        ViewStub mRightBar_Math3_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_math3_viewStub);
        if (mRightBar_Math3_viewStub != null) {
            mRightBar_math3 = (RightLayoutMath) mRightBar_Math3_viewStub.inflate();
        }
        if (mRightBar_math3 == null) {
            mRightBar_math3 = (RightLayoutMath) findViewById(R.id.rightSlipMenuBar_math3);
        }
        mRightBarMaths.add(2, mRightBar_math3);
        sliderZone_rightMath3 = new SliderZone();
        mSlideZoneMaths.add(2, sliderZone_rightMath3);

        ViewStub mRightBar_Math4_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_math4_viewStub);
        if (mRightBar_Math4_viewStub != null) {
            mRightBar_math4 = (RightLayoutMath) mRightBar_Math4_viewStub.inflate();
        }
        if (mRightBar_math4 == null) {
            mRightBar_math4 = (RightLayoutMath) findViewById(R.id.rightSlipMenuBar_math4);
        }
        mRightBarMaths.add(3, mRightBar_math4);
        sliderZone_rightMath4 = new SliderZone();
        mSlideZoneMaths.add(3, sliderZone_rightMath4);

        ViewStub mRightBar_Math5_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_math5_viewStub);
        if (mRightBar_Math5_viewStub != null) {
            mRightBar_math5 = (RightLayoutMath) mRightBar_Math5_viewStub.inflate();
        }
        if (mRightBar_math5 == null) {
            mRightBar_math5 = (RightLayoutMath) findViewById(R.id.rightSlipMenuBar_math5);
        }
        mRightBarMaths.add(4, mRightBar_math5);
        sliderZone_rightMath5 = new SliderZone();
        mSlideZoneMaths.add(4, sliderZone_rightMath5);

        ViewStub mRightBar_Math6_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_math6_viewStub);
        if (mRightBar_Math6_viewStub != null) {
            mRightBar_math6 = (RightLayoutMath) mRightBar_Math6_viewStub.inflate();
        }
        if (mRightBar_math6 == null) {
            mRightBar_math6 = (RightLayoutMath) findViewById(R.id.rightSlipMenuBar_math6);
        }
        mRightBarMaths.add(5, mRightBar_math6);
        sliderZone_rightMath6 = new SliderZone();
        mSlideZoneMaths.add(5, sliderZone_rightMath6);

        ViewStub mRightBar_Math7_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_math7_viewStub);
        if (mRightBar_Math7_viewStub != null) {
            mRightBar_math7 = (RightLayoutMath) mRightBar_Math7_viewStub.inflate();
        }
        if (mRightBar_math7 == null) {
            mRightBar_math7 = (RightLayoutMath) findViewById(R.id.rightSlipMenuBar_math7);
        }
        mRightBarMaths.add(6, mRightBar_math7);
        sliderZone_rightMath7 = new SliderZone();
        mSlideZoneMaths.add(6, sliderZone_rightMath7);

        ViewStub mRightBar_Math8_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_math8_viewStub);
        if (mRightBar_Math8_viewStub != null) {
            mRightBar_math8 = (RightLayoutMath) mRightBar_Math8_viewStub.inflate();
        }
        if (mRightBar_math8 == null) {
            mRightBar_math8 = (RightLayoutMath) findViewById(R.id.rightSlipMenuBar_math8);
        }
        mRightBarMaths.add(7, mRightBar_math8);
        sliderZone_rightMath8 = new SliderZone();
        mSlideZoneMaths.add(7, sliderZone_rightMath8);
        dealMathSlideZone();

        ViewStub mRightBar_Ref1_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_ref1_viewStub);
        if (mRightBar_Ref1_viewStub != null) {
            mRightBar_ref1 = (RightLayoutRef) mRightBar_Ref1_viewStub.inflate();
        }
        if (mRightBar_ref1 == null) {
            mRightBar_ref1 = (RightLayoutRef) findViewById(R.id.rightSlipMenuBar_ref1);
        }
        mRightBarRefs.add(0, mRightBar_ref1);
        sliderZone_rightRef1 = new SliderZone();
        mSlideZoneRefs.add(0, sliderZone_rightRef1);

        ViewStub mRightBar_Ref2_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_ref2_viewStub);
        if (mRightBar_Ref2_viewStub != null) {
            mRightBar_ref2 = (RightLayoutRef) mRightBar_Ref2_viewStub.inflate();
        }
        if (mRightBar_ref2 == null) {
            mRightBar_ref2 = (RightLayoutRef) findViewById(R.id.rightSlipMenuBar_ref2);
        }
        mRightBarRefs.add(1, mRightBar_ref2);
        sliderZone_rightRef2 = new SliderZone();
        mSlideZoneRefs.add(1, sliderZone_rightRef2);

        ViewStub mRightBar_Ref3_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_ref3_viewStub);
        if (mRightBar_Ref3_viewStub != null) {
            mRightBar_ref3 = (RightLayoutRef) mRightBar_Ref3_viewStub.inflate();
        }
        if (mRightBar_ref3 == null) {
            mRightBar_ref3 = (RightLayoutRef) findViewById(R.id.rightSlipMenuBar_ref3);
        }
        mRightBarRefs.add(2, mRightBar_ref3);
        sliderZone_rightRef3 = new SliderZone();
        mSlideZoneRefs.add(2, sliderZone_rightRef3);

        ViewStub mRightBar_Ref4_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_ref4_viewStub);
        if (mRightBar_Ref4_viewStub != null) {
            mRightBar_ref4 = (RightLayoutRef) mRightBar_Ref4_viewStub.inflate();
        }
        if (mRightBar_ref4 == null) {
            mRightBar_ref4 = (RightLayoutRef) findViewById(R.id.rightSlipMenuBar_ref4);
        }
        mRightBarRefs.add(3, mRightBar_ref4);
        sliderZone_rightRef4 = new SliderZone();
        mSlideZoneRefs.add(3, sliderZone_rightRef4);

        ViewStub mRightBar_Ref5_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_ref5_viewStub);
        if (mRightBar_Ref5_viewStub != null) {
            mRightBar_ref5 = (RightLayoutRef) mRightBar_Ref5_viewStub.inflate();
        }
        if (mRightBar_ref5 == null) {
            mRightBar_ref5 = (RightLayoutRef) findViewById(R.id.rightSlipMenuBar_ref5);
        }
        mRightBarRefs.add(4, mRightBar_ref5);
        sliderZone_rightRef5 = new SliderZone();
        mSlideZoneRefs.add(4, sliderZone_rightRef5);

        ViewStub mRightBar_Ref6_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_ref6_viewStub);
        if (mRightBar_Ref6_viewStub != null) {
            mRightBar_ref6 = (RightLayoutRef) mRightBar_Ref6_viewStub.inflate();
        }
        if (mRightBar_ref6 == null) {
            mRightBar_ref6 = (RightLayoutRef) findViewById(R.id.rightSlipMenuBar_ref6);
        }
        mRightBarRefs.add(5, mRightBar_ref6);
        sliderZone_rightRef6 = new SliderZone();
        mSlideZoneRefs.add(5, sliderZone_rightRef6);

        ViewStub mRightBar_Ref7_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_ref7_viewStub);
        if (mRightBar_Ref7_viewStub != null) {
            mRightBar_ref7 = (RightLayoutRef) mRightBar_Ref7_viewStub.inflate();
        }
        if (mRightBar_ref7 == null) {
            mRightBar_ref7 = (RightLayoutRef) findViewById(R.id.rightSlipMenuBar_ref7);
        }
        mRightBarRefs.add(6, mRightBar_ref7);
        sliderZone_rightRef7 = new SliderZone();
        mSlideZoneRefs.add(6, sliderZone_rightRef7);

        ViewStub mRightBar_Ref8_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_ref8_viewStub);
        if (mRightBar_Ref8_viewStub != null) {
            mRightBar_ref8 = (RightLayoutRef) mRightBar_Ref8_viewStub.inflate();
        }
        if (mRightBar_ref8 == null) {
            mRightBar_ref8 = (RightLayoutRef) findViewById(R.id.rightSlipMenuBar_ref8);
        }
        mRightBarRefs.add(7, mRightBar_ref8);
        sliderZone_rightRef8 = new SliderZone();
        mSlideZoneRefs.add(7, sliderZone_rightRef8);
        dealRefSlideZone();

        ViewStub mRightBar_Serial1_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_serials1_viewStub);
        if (mRightBar_Serial1_viewStub != null) {
            mRightBar_serials1 = (RightLayoutSerials) mRightBar_Serial1_viewStub.inflate();
        }
        if(mRightBar_serials1 == null) {
            mRightBar_serials1 = (RightLayoutSerials) findViewById(R.id.rightSlipMenuBar_serials1);
        }
        mRightBarBus.add(0, mRightBar_serials1);
        sliderZone_rightS1 = new SliderZone();
        mSlideZoneBus.add(0, sliderZone_rightS1);


        ViewStub mRightBar_Serial2_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_serials2_viewStub);
        if (mRightBar_Serial2_viewStub != null) {
            mRightBar_serials2 = (RightLayoutSerials) mRightBar_Serial2_viewStub.inflate();
        }
        if(mRightBar_serials2 == null) {
            mRightBar_serials2 = (RightLayoutSerials) findViewById(R.id.rightSlipMenuBar_serials2);
        }
        mRightBarBus.add(1, mRightBar_serials2);
        sliderZone_rightS2 = new SliderZone();
        mSlideZoneBus.add(1, sliderZone_rightS2);

        ViewStub mRightBar_Serial3_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_serials3_viewStub);
        if (mRightBar_Serial3_viewStub != null) {
            mRightBar_serials3 = (RightLayoutSerials) mRightBar_Serial3_viewStub.inflate();
        }
        if(mRightBar_serials3 == null) {
            mRightBar_serials3 = (RightLayoutSerials) findViewById(R.id.rightSlipMenuBar_serials3);
        }
        mRightBarBus.add(2, mRightBar_serials3);
        sliderZone_rightS3 = new SliderZone();
        mSlideZoneBus.add(2, sliderZone_rightS3);

        ViewStub mRightBar_Serial4_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_serials4_viewStub);
        if (mRightBar_Serial4_viewStub != null) {
            mRightBar_serials4 = (RightLayoutSerials) mRightBar_Serial4_viewStub.inflate();
        }
        if(mRightBar_serials4 == null) {
            mRightBar_serials4 = (RightLayoutSerials) findViewById(R.id.rightSlipMenuBar_serials4);
        }
        mRightBarBus.add(3, mRightBar_serials4);
        sliderZone_rightS4 = new SliderZone();
        mSlideZoneBus.add(3, sliderZone_rightS4);
        dealBusSlideZone();

        ViewStub mRightBar_Level_viewStub = (ViewStub) findViewById(R.id.rightSlipMenuBar_level_viewStub);
        if (mRightBar_Level_viewStub != null) {
            mRightBar_Level_viewStub.inflate();
        }
        mRightBar_level = (RightLayoutLevel) findViewById(R.id.rightSlipMenuBar_level);
        mRightBar_level.bringToFront();
        mRightBar_level.setOnButtonClickListener((radioGroup, radioButton) -> triggerLevel.autoDragToButton());
    }

    //region  public

    public void addSliderZone(SliderZone sliderZone) {
        sliderZoneList.add(sliderZone);
    }

    public SliderZone getSliderZone(int index) {
        return sliderZoneList.get(index);
    }

    public void createParam() {
        sliderZone_bottom.createParam(false);
        sliderZone_left.createParam(false);
        sliderZone_top.createParam(false);
        sliderZone_rightCh1.createParam(false);
        sliderZone_rightCh2.createParam(false);
        sliderZone_rightCh3.createParam(false);
        sliderZone_rightCh4.createParam(false);
        sliderZone_rightCh5.createParam(false);
        sliderZone_rightCh6.createParam(false);
        sliderZone_rightCh7.createParam(false);
        sliderZone_rightCh8.createParam(false);
        sliderZone_rightMath1.createParam(false);
        sliderZone_rightMath2.createParam(false);
        sliderZone_rightMath3.createParam(false);
        sliderZone_rightMath4.createParam(false);
        sliderZone_rightMath5.createParam(false);
        sliderZone_rightMath6.createParam(false);
        sliderZone_rightMath7.createParam(false);
        sliderZone_rightMath8.createParam(false);
        sliderZone_rightRef1.createParam(false);
        sliderZone_rightRef2.createParam(false);
        sliderZone_rightRef3.createParam(false);
        sliderZone_rightRef4.createParam(false);
        sliderZone_rightRef5.createParam(false);
        sliderZone_rightRef6.createParam(false);
        sliderZone_rightRef7.createParam(false);
        sliderZone_rightRef8.createParam(false);
        sliderZone_rightS1.createParam(false);
        sliderZone_rightS2.createParam(false);
        sliderZone_rightS3.createParam(false);
        sliderZone_rightS4.createParam(false);
    }

    public RightLayoutRef getRightRef(int index) {
        RightLayoutRef rightLayoutRef = null;
        switch (index) {
            case TChan.R1:
                rightLayoutRef = mRightBar_ref1;
                break;
            case TChan.R2:
                rightLayoutRef = mRightBar_ref2;
                break;
            case TChan.R3:
                rightLayoutRef = mRightBar_ref3;
                break;
            case TChan.R4:
                rightLayoutRef = mRightBar_ref4;
                break;
            case TChan.R5:
                rightLayoutRef = mRightBar_ref5;
                break;
            case TChan.R6:
                rightLayoutRef = mRightBar_ref6;
                break;
            case TChan.R7:
                rightLayoutRef = mRightBar_ref7;
                break;
            case TChan.R8:
                rightLayoutRef = mRightBar_ref8;
                break;
        }
        return rightLayoutRef;
    }

    /*

     */
    public void hideAllMenu() {
        for (SliderZone s : sliderZoneList) {
            s.setEnable(false);
            s.setCurrShowState(false);
            ViewGroup parentViewGroup = (ViewGroup) s.getSliderViewGroup().getParent();
            switch (s.getSliderDir()) {
                case SliderZone.SliderDir_BottomToTop:
                    s.getSliderViewGroup().layout(s.getSliderViewGroup().getLeft(), parentViewGroup.getHeight(),
                            s.getSliderViewGroup().getRight(), parentViewGroup.getHeight() + s.getSliderViewGroup().getHeight());
                    break;
                case SliderZone.SliderDir_TopToBottom:
                    s.getSliderViewGroup().layout(s.getSliderViewGroup().getLeft(), 0 - s.getSliderViewGroup().getHeight(),
                            s.getSliderViewGroup().getRight(), 0);
                    break;
                case SliderZone.SliderDir_LeftToRight:
                    s.getSliderViewGroup().layout(0 - s.getSliderViewGroup().getWidth(), s.getSliderViewGroup().getTop(),
                            0, s.getSliderViewGroup().getBottom());
                    break;
                case SliderZone.SliderDir_RightToLeft:
                    s.getSliderViewGroup().layout(parentViewGroup.getWidth(), s.getSliderViewGroup().getTop(),
                            parentViewGroup.getWidth() + s.getSliderViewGroup().getWidth(), s.getSliderViewGroup().getBottom());
                    break;
                default:
                    break;
            }

        }
    }

    /**
     * RxBus事件订阅：处理来自其他模块的滑出菜单消息
     * <p>
     * 功能描述：
     * - 接收MainMsgSlip消息，根据消息内容打开或关闭指定的滑出菜单
     * - 处理Math/Ref/Serials等非通道菜单的滑出显示请求
     * - 同步更新菜单的显示样式状态
     * </p>
     * <p>
     * 调用时机：
     * - 当其他模块（如Remote远程控制、外部命令等）需要控制菜单显示时
     * - 通过RxBus订阅机制自动触发
     * - 订阅事件：RxEnum.MAIN_MSG_SLIP
     * </p>
     * <p>
     * 参数说明：
     * @param msgSlip 滑出菜单消息对象，包含以下信息：
     *                - slip: 滑出菜单编号（RIGHTSLIP_MATH1-8, RIGHTSLIP_REF1-8, RIGHTSLIP_S1-4）
     *                - isOpen: 是否打开菜单（true=打开，false=关闭）
     *                - isNormal: 是否为正常显示状态
     * </p>
     * <p>
     * 返回值：无（Consumer接口方法）
     * </p>
     * <p>
     * 相关方法：
     * - openSlip(): 打开指定滑出菜单
     * - hideSlip(): 关闭指定滑出菜单
     * - changeSlipStyle(): 改变菜单显示样式
     * </p>
     */
    private Consumer<MainMsgSlip> consumerMainSlipFromOther = new Consumer<MainMsgSlip>() {
        @Override
        public void accept(MainMsgSlip msgSlip) throws Exception {
            if (msgSlip.isOpen()) { // 判断是否需要打开菜单
                openSlip(msgSlip.getSlip()); // 打开指定的滑出菜单
                changeSlipStyle(msgSlip, 0); // 改变slide Math/Ref/Serials显示样式，传入0表示使用msgSlip中的slip值
            } else { // 需要关闭菜单
                hideSlip(msgSlip.getSlip()); // 关闭指定的滑出菜单
            }
        }
    };

    /**
     * 改变滑出菜单的显示样式
     * <p>
     * 功能描述：
     * - 根据滑出菜单编号，将滑出菜单索引转换为通道编号
     * - 发送消息通知相关模块更新Math/Ref/Serials的显示样式
     * - 支持正常模式和特殊模式的显示状态切换
     * </p>
     * <p>
     * 调用时机：
     * - consumerMainSlipFromOther中打开菜单时调用
     * - OnChanActiveChange中通道激活状态变化时调用
     * - 需要更新菜单显示样式时调用
     * </p>
     * <p>
     * 参数说明：
     * @param msgSlip 滑出菜单消息对象，可能为null
     *                - 当不为null时，从中获取slip和isNormal信息
     *                - 当为null时，使用slipNumber参数
     * @param slipNumber 滑出菜单编号（备用参数）
     *                   - 当msgSlip为null时使用此参数
     *                   - 取值范围：RIGHTSLIP_MATH1-8, RIGHTSLIP_REF1-8, RIGHTSLIP_S1-4
     * </p>
     * <p>
     * 返回值：无
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 确定滑出菜单索引：优先使用msgSlip.getSlip()，否则使用slipNumber
     * 2. 确定显示状态：优先使用msgSlip.isNormal()，否则默认为true
     * 3. 将滑出菜单索引转换为通道编号（TChan.Math1-8, TChan.R1-8, TChan.S1-4）
     * 4. 发送RxBus消息通知显示状态更新
     * </p>
     * <p>
     * 相关方法：
     * - consumerMainSlipFromOther(): RxBus订阅方法
     * - OnChanActiveChange(): 通道激活状态变化处理
     * </p>
     */
    private void changeSlipStyle(MainMsgSlip msgSlip, int slipNumber) {
        int channelNumber = -1; // 初始化通道编号为-1，表示无效值
        int slipIndex = null == msgSlip ? slipNumber : msgSlip.getSlip(); // 确定滑出菜单索引：msgSlip为null时使用slipNumber，否则使用msgSlip中的值
        boolean isNormal = null == msgSlip || msgSlip.isNormal(); // 确定显示状态：msgSlip为null时默认为true，否则使用msgSlip中的值
        switch (slipIndex) { // 根据滑出菜单索引转换为对应的通道编号
            case RIGHTSLIP_MATH1: channelNumber = TChan.Math1;break; // Math1通道
            case RIGHTSLIP_MATH2: channelNumber = TChan.Math2;break; // Math2通道
            case RIGHTSLIP_MATH3: channelNumber = TChan.Math3;break; // Math3通道
            case RIGHTSLIP_MATH4: channelNumber = TChan.Math4;break; // Math4通道
            case RIGHTSLIP_MATH5: channelNumber = TChan.Math5;break; // Math5通道
            case RIGHTSLIP_MATH6: channelNumber = TChan.Math6;break; // Math6通道
            case RIGHTSLIP_MATH7: channelNumber = TChan.Math7;break; // Math7通道
            case RIGHTSLIP_MATH8: channelNumber = TChan.Math8;break; // Math8通道
            case RIGHTSLIP_REF1: channelNumber = TChan.R1;break; // Ref1参考通道
            case RIGHTSLIP_REF2: channelNumber = TChan.R2;break; // Ref2参考通道
            case RIGHTSLIP_REF3: channelNumber = TChan.R3;break; // Ref3参考通道
            case RIGHTSLIP_REF4: channelNumber = TChan.R4;break; // Ref4参考通道
            case RIGHTSLIP_REF5: channelNumber = TChan.R5;break; // Ref5参考通道
            case RIGHTSLIP_REF6: channelNumber = TChan.R6;break; // Ref6参考通道
            case RIGHTSLIP_REF7: channelNumber = TChan.R7;break; // Ref7参考通道
            case RIGHTSLIP_REF8: channelNumber = TChan.R8;break; // Ref8参考通道
            case RIGHTSLIP_S1: channelNumber = TChan.S1;break; // Serial1串行通道
            case RIGHTSLIP_S2: channelNumber = TChan.S2;break; // Serial2串行通道
            case RIGHTSLIP_S3: channelNumber = TChan.S3;break; // Serial3串行通道
            case RIGHTSLIP_S4: channelNumber = TChan.S4;break; // Serial4串行通道
        }
        if(channelNumber > 0) { // 判断通道编号是否有效（大于0表示有效）
            RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_NORMAL_STATE, String.valueOf(channelNumber) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(isNormal)); // 发送消息通知显示状态更新，格式：通道编号+分隔符+显示状态
        }
    }

    /**
     * RxBus事件订阅：处理工作模式变化
     * <p>
     * 功能描述：
     * - 监听示波器工作模式的变化（如YT模式、XY模式、Roll模式等）
     * - 根据不同的工作模式调整右侧布局的显示状态
     * - 在XY模式下显示右侧布局并禁止滚动移动
     * </p>
     * <p>
     * 调用时机：
     * - 当示波器工作模式发生变化时
     * - 通过RxBus订阅机制自动触发
     * - 订阅事件：RxEnum.WORK_MODE_CHANGE
     * </p>
     * <p>
     * 参数说明：
     * @param workModeBean 工作模式信息对象，包含以下信息：
     *                     - nextWorkMode: 下一个工作模式（WorkMode_YT, WorkMode_XY, WorkMode_Roll等）
     *                     - currentWorkMode: 当前工作模式
     * </p>
     * <p>
     * 返回值：无（Consumer接口方法）
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 判断下一个工作模式是否为XY模式
     * 2. 如果是XY模式：
     *    - 显示右侧布局（showRightLayout(true)）
     *    - 禁止右侧滚动移动（setRightScrollNoMove(true)）
     * 3. 如果不是XY模式：
     *    - 允许右侧滚动移动（setRightScrollNoMove(false)）
     * </p>
     * <p>
     * 特殊说明：
     * - XY模式下需要特殊的UI布局，右侧布局固定显示
     * - 其他模式下右侧布局可以自由滚动切换
     * </p>
     * <p>
     * 相关方法：
     * - showRightLayout(): 显示右侧布局
     * - setRightScrollNoMove(): 设置右侧滚动是否可移动
     * </p>
     */
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            if (workModeBean.getNextWorkMode() == IWorkMode.WorkMode_XY) { // 判断下一个工作模式是否为XY模式
//                if (!CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_RIGHTSWITCH_CHANNEL)) {
                showRightLayout(true); // 显示右侧布局，传入true表示显示Others页面
//                }
                setRightScrollNoMove(true); // 禁止右侧滚动移动，XY模式下右侧布局固定显示
            } else { // 不是XY模式
                setRightScrollNoMove(false); // 允许右侧滚动移动，其他模式下可以自由切换右侧布局
            }
        }
    };
    /**
     * RxBus事件订阅：处理分段状态变化
     * <p>
     * 功能描述：
     * - 监听分段存储状态的变化消息
     * - 根据串行总线文本显示状态控制底部滑出菜单的启用状态
     * - 当串行总线文本打开时，禁用底部菜单的滑出功能
     * </p>
     * <p>
     * 调用时机：
     * - 当分段存储状态发生变化时
     * - 通过RxBus订阅机制自动触发
     * - 订阅事件：RxEnum.TOP_MSG_SEGMENTED_STATE
     * </p>
     * <p>
     * 参数说明：
     * @param msgSegmentedState 分段状态消息对象（未在方法中使用）
     * </p>
     * <p>
     * 返回值：无（Consumer接口方法）
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 从缓存中读取串行总线文本显示状态（MAIN_BOTTOM_SLIP_SERIALBUSTXT）
     * 2. 根据状态设置底部滑出菜单的启用状态：
     *    - 如果串行总线文本打开（isSerialsTxt=true），则禁用底部菜单滑出
     *    - 如果串行总线文本关闭（isSerialsTxt=false），则启用底部菜单滑出
     * </p>
     * <p>
     * 特殊说明：
     * - 串行总线文本打开时，底部区域用于显示文本信息，不应响应滑出手势
     * - 串行总线文本关闭时，底部区域恢复正常的滑出菜单功能
     * </p>
     * <p>
     * 相关方法：
     * - sliderZone_bottom.setEnableSlip(): 设置底部滑出菜单的启用状态
     * </p>
     */
    private Consumer<TopMsgSegmentedState> consumerSegmentedState = new Consumer<TopMsgSegmentedState>() {
        @Override
        public void accept(TopMsgSegmentedState msgSegmentedState) throws Exception {
            // 串型文本打开与关闭，是通过分段存储消息过来的
            boolean isSerialsTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT); // 从缓存中读取串行总线文本显示状态
            sliderZone_bottom.setEnableSlip(!isSerialsTxt); // 设置底部滑出菜单的启用状态：串行总线文本打开时禁用滑出，关闭时启用滑出
        }
    };

    /**
     * 处理通道激活状态变化
     * <p>
     * 功能描述：
     * - 监听通道激活状态的变化（CH1-CH8、Math1-Math8、R1-R8、S1-S4）
     * - 根据激活的通道类型自动切换右侧布局页面（Channels页面或Others页面）
     * - 如果右侧滑出菜单已显示，自动打开对应通道的滑出菜单
     * </p>
     * <p>
     * 调用时机：
     * - 当通道激活状态发生变化时（用户切换通道或自动切换）
     * - 通过RxBus订阅机制触发
     * - 订阅事件：MQEnum.CH_ACTIVE
     * </p>
     * <p>
     * 参数说明：
     * @param obj 消息对象，包含以下信息：
     *            - MQEnum: 消息枚举类型（必须为CH_ACTIVE）
     *            - chan: 激活的通道对象（IChan类型）
     * </p>
     * <p>
     * 返回值：无
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 解析消息枚举类型，如果不是CH_ACTIVE则直接返回
     * 2. 获取激活的通道对象
     * 3. 根据通道类型判断是否需要切换右侧布局：
     *    - 如果是CH1-CH8通道，且当前不是Channels页面，则切换到Channels页面
     *    - 如果是Math/S1-S4通道，且当前是Channels页面，则切换到Others页面
     * 4. 如果右侧滑出菜单已显示，自动打开对应通道的滑出菜单
     * </p>
     * <p>
     * 特殊说明：
     * - 该方法主要用于响应通道切换事件，自动调整UI布局
     * - 当右侧滑出菜单已显示时，会自动切换到对应通道的菜单
     * - 注释掉的代码表示之前的实现方式（通过RxBus发送切换消息）
     * </p>
     * <p>
     * 相关方法：
     * - isRightLayout(): 判断当前是否为Channels页面
     * - isRightSlipShow(): 判断右侧滑出菜单是否显示
     * - openSlip(): 打开指定滑出菜单
     * - changeSlipStyle(): 改变菜单显示样式
     * </p>
     */
    private void OnChanActiveChange(Object obj) {
        MQEnum mqEnum= RxBusRegister.parseMqEnum(obj); // 解析消息枚举类型
        if (mqEnum != MQEnum.CH_ACTIVE) return; // 如果不是通道激活消息，则直接返回
        IChan chan = ((MsgChActiveChange) obj).getChan(); // 获取激活的通道对象

        if (IChan.isCh1ToCh8(chan))  { // 判断是否为模拟通道CH1-CH8
            if (!isRightLayout()) { // 如果当前不是Channels页面
//                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_CHANNELS); // 发送消息切换到Channels页面（已注释）
            }
        } else if (IChan.isMathToS4(chan) ) { // 判断是否为Math/Ref/Serials通道
            if (isRightLayout()) { // 如果当前是Channels页面
//                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送消息切换到Others页面（已注释）
            }
        }
        if (isRightSlipShow()) { // 判断右侧滑出菜单是否已显示
            int slipNumber = -1; // 初始化滑出菜单编号为-1，表示未找到对应的滑出菜单
            switch (chan) { // 根据激活的通道类型确定需要打开的滑出菜单编号
                case CH1: // CH1通道
                    if (!isSlipShow(RIGHTSLIP_CH1)) slipNumber = RIGHTSLIP_CH1; // 如果CH1滑出菜单未显示，则设置为RIGHTSLIP_CH1
                    break;
                case CH2: // CH2通道
                    if (!isSlipShow(RIGHTSLIP_CH2)) slipNumber = RIGHTSLIP_CH2; // 如果CH2滑出菜单未显示，则设置为RIGHTSLIP_CH2
                    break;
                case CH3: // CH3通道
                    if (!isSlipShow(RIGHTSLIP_CH3)) slipNumber = RIGHTSLIP_CH3; // 如果CH3滑出菜单未显示，则设置为RIGHTSLIP_CH3
                    break;
                case CH4: // CH4通道
                    if (!isSlipShow(RIGHTSLIP_CH4)) slipNumber = RIGHTSLIP_CH4; // 如果CH4滑出菜单未显示，则设置为RIGHTSLIP_CH4
                    break;
                case CH5: // CH5通道
                    if (!isSlipShow(RIGHTSLIP_CH5)) slipNumber = RIGHTSLIP_CH5; // 如果CH5滑出菜单未显示，则设置为RIGHTSLIP_CH5
                    break;
                case CH6: // CH6通道
                    if (!isSlipShow(RIGHTSLIP_CH6)) slipNumber = RIGHTSLIP_CH6; // 如果CH6滑出菜单未显示，则设置为RIGHTSLIP_CH6
                    break;
                case CH7: // CH7通道
                    if (!isSlipShow(RIGHTSLIP_CH7)) slipNumber = RIGHTSLIP_CH7; // 如果CH7滑出菜单未显示，则设置为RIGHTSLIP_CH7
                    break;
                case CH8: // CH8通道
                    if (!isSlipShow(RIGHTSLIP_CH8)) slipNumber = RIGHTSLIP_CH8; // 如果CH8滑出菜单未显示，则设置为RIGHTSLIP_CH8
                    break;
                case Math1: // Math1数学通道
                    if (!isSlipShow(RIGHTSLIP_MATH1)) slipNumber = RIGHTSLIP_MATH1; // 如果Math1滑出菜单未显示，则设置为RIGHTSLIP_MATH1
                    break;
                case Math2: // Math2数学通道
                    if (!isSlipShow(RIGHTSLIP_MATH2)) slipNumber = RIGHTSLIP_MATH2; // 如果Math2滑出菜单未显示，则设置为RIGHTSLIP_MATH2
                    break;
                case Math3: // Math3数学通道
                    if (!isSlipShow(RIGHTSLIP_MATH3)) slipNumber = RIGHTSLIP_MATH3; // 如果Math3滑出菜单未显示，则设置为RIGHTSLIP_MATH3
                    break;
                case Math4: // Math4数学通道
                    if (!isSlipShow(RIGHTSLIP_MATH4)) slipNumber = RIGHTSLIP_MATH4; // 如果Math4滑出菜单未显示，则设置为RIGHTSLIP_MATH4
                    break;
                case Math5: // Math5数学通道
                    if (!isSlipShow(RIGHTSLIP_MATH5)) slipNumber = RIGHTSLIP_MATH5; // 如果Math5滑出菜单未显示，则设置为RIGHTSLIP_MATH5
                    break;
                case Math6: // Math6数学通道
                    if (!isSlipShow(RIGHTSLIP_MATH6)) slipNumber = RIGHTSLIP_MATH6; // 如果Math6滑出菜单未显示，则设置为RIGHTSLIP_MATH6
                    break;
                case Math7: // Math7数学通道
                    if (!isSlipShow(RIGHTSLIP_MATH7)) slipNumber = RIGHTSLIP_MATH7; // 如果Math7滑出菜单未显示，则设置为RIGHTSLIP_MATH7
                    break;
                case Math8: // Math8数学通道
                    if (!isSlipShow(RIGHTSLIP_MATH8)) slipNumber = RIGHTSLIP_MATH8; // 如果Math8滑出菜单未显示，则设置为RIGHTSLIP_MATH8
                    break;
                case R1: // R1参考通道
                    if (!isSlipShow(RIGHTSLIP_REF1)) slipNumber = RIGHTSLIP_REF1; // 如果R1滑出菜单未显示，则设置为RIGHTSLIP_REF1
                    break;
                case R2: // R2参考通道
                    if (!isSlipShow(RIGHTSLIP_REF2)) slipNumber = RIGHTSLIP_REF2; // 如果R2滑出菜单未显示，则设置为RIGHTSLIP_REF2
                    break;
                case R3: // R3参考通道
                    if (!isSlipShow(RIGHTSLIP_REF3)) slipNumber = RIGHTSLIP_REF3; // 如果R3滑出菜单未显示，则设置为RIGHTSLIP_REF3
                    break;
                case R4: // R4参考通道
                    if (!isSlipShow(RIGHTSLIP_REF4)) slipNumber = RIGHTSLIP_REF4; // 如果R4滑出菜单未显示，则设置为RIGHTSLIP_REF4
                    break;
                case R5: // R5参考通道
                    if (!isSlipShow(RIGHTSLIP_REF5)) slipNumber = RIGHTSLIP_REF5; // 如果R5滑出菜单未显示，则设置为RIGHTSLIP_REF5
                    break;
                case R6: // R6参考通道
                    if (!isSlipShow(RIGHTSLIP_REF6)) slipNumber = RIGHTSLIP_REF6; // 如果R6滑出菜单未显示，则设置为RIGHTSLIP_REF6
                    break;
                case R7: // R7参考通道
                    if (!isSlipShow(RIGHTSLIP_REF7)) slipNumber = RIGHTSLIP_REF7; // 如果R7滑出菜单未显示，则设置为RIGHTSLIP_REF7
                    break;
                case R8: // R8参考通道
                    if (!isSlipShow(RIGHTSLIP_REF8)) slipNumber = RIGHTSLIP_REF8; // 如果R8滑出菜单未显示，则设置为RIGHTSLIP_REF8
                    break;
                case S1: // S1串行通道
                    if (!isSlipShow(RIGHTSLIP_S1)) slipNumber = RIGHTSLIP_S1; // 如果S1滑出菜单未显示，则设置为RIGHTSLIP_S1
                    break;
                case S2: // S2串行通道
                    if (!isSlipShow(RIGHTSLIP_S2)) slipNumber = RIGHTSLIP_S2; // 如果S2滑出菜单未显示，则设置为RIGHTSLIP_S2
                    break;
                case S3: // S3串行通道
                    if (!isSlipShow(RIGHTSLIP_S3)) slipNumber = RIGHTSLIP_S3; // 如果S3滑出菜单未显示，则设置为RIGHTSLIP_S3
                    break;
                case S4: // S4串行通道
                    if (!isSlipShow(RIGHTSLIP_S4)) slipNumber = RIGHTSLIP_S4; // 如果S4滑出菜单未显示，则设置为RIGHTSLIP_S4
                    break;
            }
            if (slipNumber > 0) { // 判断是否找到了对应的滑出菜单编号（大于0表示有效）
                openSlip(slipNumber); // 打开对应的滑出菜单
                changeSlipStyle(null, slipNumber); // 改变slide Math/Ref/Serials显示样式，传入null表示使用slipNumber参数
            }
        }
    }

    /**
     * RxBus事件订阅：处理显示消息
     * <p>
     * 功能描述：
     * - 监听显示设置的变化消息
     * - 根据显示消息中的透明度设置调整视图的透明度
     * - 支持通用显示设置（TopMsgDisplayCommon）的处理
     * </p>
     * <p>
     * 调用时机：
     * - 当显示设置发生变化时（如透明度调整）
     * - 通过RxBus订阅机制自动触发
     * - 订阅事件：RxEnum.TOP_MSG_DISPLAY
     * </p>
     * <p>
     * 参数说明：
     * @param topMsgDisplay 显示消息对象，包含以下信息：
     *                      - displayDetail: 显示详细信息对象
     *                      - 当displayDetail为TopMsgDisplayCommon时，包含alpha透明度值
     * </p>
     * <p>
     * 返回值：无（Consumer接口方法）
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 检查topMsgDisplay是否为null
     * 2. 检查displayDetail是否为TopMsgDisplayCommon类型
     * 3. 如果满足条件，则获取透明度值并设置视图透明度
     * </p>
     * <p>
     * 特殊说明：
     * - 仅处理TopMsgDisplayCommon类型的显示消息
     * - 其他类型的显示消息不在此方法中处理
     * </p>
     * <p>
     * 相关方法：
     * - setViewTransparency(): 设置视图透明度
     * </p>
     */
    private Consumer<TopMsgDisplay> consumerDisplay = new Consumer<TopMsgDisplay>() {
        @Override
        public void accept(TopMsgDisplay topMsgDisplay) throws Exception {
            if (topMsgDisplay != null && topMsgDisplay.getDisplayDetail() instanceof TopMsgDisplayCommon) { // 判断消息对象是否有效且为通用显示类型
                TopMsgDisplayCommon displayCommon = (TopMsgDisplayCommon) topMsgDisplay.getDisplayDetail(); // 获取通用显示详细信息对象
                setViewTransparency(displayCommon.getAlpha()); // 设置视图透明度，alpha值范围0-255
            }
        }
    };

    /**
     * RxBus事件订阅：处理强制触发消息
     * <p>
     * 功能描述：
     * - 监听强制触发消息
     * - 触发示波器的强制触发操作
     * - 用于在特定条件下强制示波器进行一次触发采集
     * </p>
     * <p>
     * 调用时机：
     * - 当需要强制触发采集时（如单次触发模式、手动触发等）
     * - 通过RxBus订阅机制自动触发
     * - 订阅事件：RxEnum.FORCE_TRIGGER
     * </p>
     * <p>
     * 参数说明：
     * @param object 消息对象（未在方法中使用）
     * </p>
     * <p>
     * 返回值：无（Consumer接口方法）
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 调用Scope.getInstance().forceTrigger()方法
     * 2. 执行强制触发操作
     * </p>
     * <p>
     * 特殊说明：
     * - 强制触发会立即触发一次采集，不受触发条件限制
     * - 主要用于单次触发模式或手动触发场景
     * </p>
     * <p>
     * 相关方法：
     * - Scope.getInstance().forceTrigger(): 执行强制触发操作
     * </p>
     */
    private Consumer<Object> consumerForceTrigger = new Consumer<Object>() {
        @Override
        public void accept(Object object) throws Exception {
            Scope.getInstance().forceTrigger(); // 调用Scope实例的强制触发方法，执行一次强制触发采集
        }
    };


    /**
     * RxBus事件订阅：处理命令消息到UI
     * <p>
     * 功能描述：
     * - 监听来自外部命令系统的菜单控制消息
     * - 根据命令标志打开或关闭指定的滑出菜单
     * - 支持多种菜单类型的控制（Trigger、Measure、Main、QuickBottom、Channel等）
     * </p>
     * <p>
     * 调用时机：
     * - 当外部命令系统（如远程控制、脚本命令等）发送菜单控制命令时
     * - 通过RxBus订阅机制自动触发
     * - 订阅事件：RxEnum.COMMAND_MSG_TO_UI
     * </p>
     * <p>
     * 参数说明：
     * @param commandMsgToUI 命令消息对象，包含以下信息：
     *                       - flag: 命令标志，标识命令类型
     *                       - param: 命令参数，包含具体操作信息
     * </p>
     * <p>
     * 返回值：无（Consumer接口方法）
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 根据命令标志（flag）判断命令类型
     * 2. 处理不同类型的命令：
     *    - FLAG_MENU_TRIGGER/FLAG_MENU_MEASURE: 打开顶部滑出菜单（TOPSLIP）
     *    - FLAG_MENU_Main: 根据参数打开或关闭顶部滑出菜单
     *    - FLAG_MENU_QuickBottom: 根据参数打开或关闭底部滑出菜单
     *    - FLAG_MENU_CHANNEL: 根据参数打开或关闭指定通道的滑出菜单
     * 3. 对于通道菜单命令，解析参数获取通道编号和操作类型
     * </p>
     * <p>
     * 特殊说明：
     * - 该方法主要用于响应外部命令系统的菜单控制请求
     * - 支持所有通道类型的菜单控制（CH1-CH8、Math1-Math8、R1-R8、S1-S4）
     * - 参数格式：对于通道菜单，格式为"通道编号:是否打开"
     * </p>
     * <p>
     * 相关方法：
     * - openSlip(): 打开指定滑出菜单
     * - hideSlip(): 关闭指定滑出菜单
     * - slipOption(): 滑出菜单选项操作方法
     * </p>
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) { // 根据命令标志判断命令类型
                case CommandMsgToUI.FLAG_MENU_TRIGGER: // Trigger触发菜单命令
                case CommandMsgToUI.FLAG_MENU_MEASURE:{ // Measure测量菜单命令
                    openSlip(TOPSLIP); // 打开顶部滑出菜单
                }break;
                case CommandMsgToUI.FLAG_MENU_Main:{ // Main主菜单命令
                    boolean isOpen=Boolean.parseBoolean(commandMsgToUI.getParam()); // 解析参数，获取是否打开菜单的布尔值
                    slipOption(isOpen,TOPSLIP); // 根据参数打开或关闭顶部滑出菜单
                }break;
                case CommandMsgToUI.FLAG_MENU_QuickBottom:{ // QuickBottom快速底部菜单命令
                    boolean isOpen=Boolean.parseBoolean(commandMsgToUI.getParam()); // 解析参数，获取是否打开菜单的布尔值
                    slipOption(isOpen,BOTTOMSLIP); // 根据参数打开或关闭底部滑出菜单
                }break;
                case CommandMsgToUI.FLAG_MENU_CHANNEL:{ // Channel通道菜单命令
                    String[] params= commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 分割参数，格式为"通道编号:是否打开"
                    int ch=Integer.parseInt(params[0]); // 获取通道编号
                    boolean isOpen=Boolean.parseBoolean(params[1]); // 获取是否打开菜单的布尔值
                    ch= TChan.toUiChNo(ch); // 将FPGA通道编号转换为UI通道编号
                    switch (ch) // 根据通道编号打开或关闭对应的滑出菜单
                    {
                        case TChan.Ch1:slipOption(isOpen,RIGHTSLIP_CH1);break; // CH1通道菜单
                        case TChan.Ch2:slipOption(isOpen,RIGHTSLIP_CH2);break; // CH2通道菜单
                        case TChan.Ch3:slipOption(isOpen,RIGHTSLIP_CH3);break; // CH3通道菜单
                        case TChan.Ch4:slipOption(isOpen,RIGHTSLIP_CH4);break; // CH4通道菜单
                        case TChan.Ch5:slipOption(isOpen,RIGHTSLIP_CH5);break; // CH5通道菜单
                        case TChan.Ch6:slipOption(isOpen,RIGHTSLIP_CH6);break; // CH6通道菜单
                        case TChan.Ch7:slipOption(isOpen,RIGHTSLIP_CH7);break; // CH7通道菜单
                        case TChan.Ch8:slipOption(isOpen,RIGHTSLIP_CH8);break; // CH8通道菜单
                        case TChan.Math1:slipOption(isOpen,RIGHTSLIP_MATH1);break; // Math1数学通道菜单
                        case TChan.Math2:slipOption(isOpen,RIGHTSLIP_MATH2);break; // Math2数学通道菜单
                        case TChan.Math3:slipOption(isOpen,RIGHTSLIP_MATH3);break; // Math3数学通道菜单
                        case TChan.Math4:slipOption(isOpen,RIGHTSLIP_MATH4);break; // Math4数学通道菜单
                        case TChan.Math5:slipOption(isOpen,RIGHTSLIP_MATH5);break; // Math5数学通道菜单
                        case TChan.Math6:slipOption(isOpen,RIGHTSLIP_MATH6);break; // Math6数学通道菜单
                        case TChan.Math7:slipOption(isOpen,RIGHTSLIP_MATH7);break; // Math7数学通道菜单
                        case TChan.Math8:slipOption(isOpen,RIGHTSLIP_MATH8);break; // Math8数学通道菜单
                        case TChan.R1:slipOption(isOpen,RIGHTSLIP_REF1);break; // R1参考通道菜单
                        case TChan.R2:slipOption(isOpen,RIGHTSLIP_REF2);break; // R2参考通道菜单
                        case TChan.R3:slipOption(isOpen,RIGHTSLIP_REF3);break; // R3参考通道菜单
                        case TChan.R4:slipOption(isOpen,RIGHTSLIP_REF4);break; // R4参考通道菜单
                        case TChan.R5:slipOption(isOpen,RIGHTSLIP_REF5);break; // R5参考通道菜单
                        case TChan.R6:slipOption(isOpen,RIGHTSLIP_REF6);break; // R6参考通道菜单
                        case TChan.R7:slipOption(isOpen,RIGHTSLIP_REF7);break; // R7参考通道菜单
                        case TChan.R8: slipOption(isOpen,RIGHTSLIP_REF8);break; // R8参考通道菜单
                        case TChan.S1:slipOption(isOpen,RIGHTSLIP_S1);break; // S1串行通道菜单
                        case TChan.S2:slipOption(isOpen,RIGHTSLIP_S2);break; // S2串行通道菜单
                        case TChan.S3:slipOption(isOpen,RIGHTSLIP_S3);break; // S3串行通道菜单
                        case TChan.S4:slipOption(isOpen,RIGHTSLIP_S4);break; // S4串行通道菜单

                    }
                }break;
            }
        }
    };

    /**
     * 滑出菜单选项操作方法
     * <p>
     * 功能描述：
     * - 根据isOpen参数打开或关闭指定的滑出菜单
     * - 提供统一的菜单操作接口，简化菜单控制逻辑
     * </p>
     * <p>
     * 调用时机：
     * - consumerCommandToUI中处理菜单命令时调用
     * - 其他需要根据布尔值控制菜单显示/隐藏的场景
     * </p>
     * <p>
     * 参数说明：
     * @param isOpen 是否打开菜单
     *               - true: 打开菜单
     *               - false: 关闭菜单
     * @param slip 滑出菜单编号，使用@Slip注解限定取值范围
     *             - TOPSLIP: 顶部滑出菜单
     *             - BOTTOMSLIP: 底部滑出菜单
     *             - RIGHTSLIP_CH1-8: 右侧通道滑出菜单
     *             - RIGHTSLIP_MATH1-8: 右侧数学通道滑出菜单
     *             - RIGHTSLIP_REF1-8: 右侧参考通道滑出菜单
     *             - RIGHTSLIP_S1-4: 右侧串行通道滑出菜单
     * </p>
     * <p>
     * 返回值：无
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 判断isOpen参数的值
     * 2. 如果isOpen为true，调用openSlip()打开菜单
     * 3. 如果isOpen为false，调用hideSlip()关闭菜单
     * </p>
     * <p>
     * 相关方法：
     * - openSlip(): 打开指定滑出菜单
     * - hideSlip(): 关闭指定滑出菜单
     * </p>
     */
    private void slipOption(boolean isOpen,@Slip int slip){
        if (isOpen){ // 判断是否需要打开菜单
            openSlip(slip); // 打开指定的滑出菜单
        }else { // 需要关闭菜单
            hideSlip(slip); // 关闭指定的滑出菜单
        }
    }

    /**
     * RxBus事件订阅：处理串口字可见性变化
     * <p>
     * 功能描述：
     * - 监听串口字可见性的变化消息
     * - 根据串口字显示状态调整右侧布局和滚动状态
     * - 当串口字显示时，隐藏右侧布局并禁止滚动移动
     * </p>
     * <p>
     * 调用时机：
     * - 当串口字显示状态发生变化时
     * - 通过RxBus订阅机制自动触发
     * - 订阅事件：RxEnum.SERIALS_WORD_VISIBLE
     * </p>
     * <p>
     * 参数说明：
     * @param aBoolean 串口字是否可见
     *                 - true: 串口字可见，隐藏右侧布局
     *                 - false: 串口字不可见，恢复右侧布局
     * </p>
     * <p>
     * 返回值：无（Consumer接口方法）
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 判断串口字是否可见
     * 2. 如果可见（aBoolean=true）：
     *    - 显示右侧布局的Channels页面（showRightLayout(false)）
     *    - 禁止右侧滚动移动（setRightScrollNoMove(true)）
     * 3. 如果不可见（aBoolean=false）：
     *    - 允许右侧滚动移动（setRightScrollNoMove(false)）
     * </p>
     * <p>
     * 特殊说明：
     * - 串口字显示时，右侧布局固定显示Channels页面，不允许切换
     * - 注释掉的代码表示之前的实现方式（调整布局偏移量）
     * </p>
     * <p>
     * 相关方法：
     * - showRightLayout(): 显示右侧布局
     * - setRightScrollNoMove(): 设置右侧滚动是否可移动
     * </p>
     */
    private Consumer<Boolean> consumerSerialswordVisible = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            if (aBoolean) { // 判断串口字是否可见
                showRightLayout(false); // 显示右侧布局的Channels页面，传入false表示显示Channels页面
//                finalSlipOtherY = (int) getResources().getDimension(R.dimen.rightSlipOtherYBig); // 设置Math/Ref/Bus布局的Y坐标偏移量（已注释）
//                finalDialogFormulaOffset = (int) getResources().getDimension(R.dimen.rightDialogFormulaKeyBoardBig); // 设置表达式键盘偏移量（已注释）
//                finalDialogFloatOffset = (int) getResources().getDimension(R.dimen.rightDialogFloatKeyBoardBig); // 设置浮点数键盘偏移量（已注释）
//                finalDialogNumberOffset = (int) getResources().getDimension(R.dimen.rightDialogNumberKeyBoardBig); // 设置数字键盘偏移量（已注释）
            } else { // 串口字不可见
//                finalSlipOtherY = slipOtherY; // 恢复默认的Math/Ref/Bus布局Y坐标偏移量（已注释）
//                finalDialogFormulaOffset = dialogFormulaOffset; // 恢复默认的表达式键盘偏移量（已注释）
//                finalDialogFloatOffset = dialogFloatOffset; // 恢复默认的浮点数键盘偏移量（已注释）
//                finalDialogNumberOffset = dialogNumberOffset; // 恢复默认的数字键盘偏移量（已注释）
            }
//            changeBusLayout(finalSlipOtherY); // 处理RightLayoutSerials显示位置（已注释）
//            ViewUtils.setDialogFormulaOffset(finalDialogFormulaOffset); // 设置表达式键盘偏移量（已注释）
//            ViewUtils.setDialogFloatOffset(finalDialogFloatOffset); // 设置浮点数键盘偏移量（已注释）
//            ViewUtils.setDialogNumberOffset(finalDialogNumberOffset); // 设置数字键盘偏移量（已注释）
            setRightScrollNoMove(aBoolean); // 设置右侧滚动是否可移动：串口字可见时禁止滚动，不可见时允许滚动
        }
    };

    /**
     * RxBus事件订阅：处理菜单滑出启用状态变化
     * <p>
     * 功能描述：
     * - 监听菜单滑出启用状态的变化消息
     * - 根据消息内容启用或禁用指定菜单的滑出功能
     * - 支持所有菜单类型的滑出状态控制（Left、Top、Bottom、Channel、Math、Ref、Serials）
     * </p>
     * <p>
     * 调用时机：
     * - 当菜单滑出启用状态发生变化时（如某些模式下禁用特定菜单的滑出）
     * - 通过RxBus订阅机制自动触发
     * - 订阅事件：RxEnum.MAIN_MSG_SLIDER_ZONE
     * </p>
     * <p>
     * 参数说明：
     * @param mainMsgSliderZone 菜单滑出消息对象，包含以下信息：
     *                           - menuIndex: 菜单索引，标识菜单类型
     *                           - enableSlip: 是否启用滑出功能
     * </p>
     * <p>
     * 返回值：无（Consumer接口方法）
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 根据menuIndex判断菜单类型
     * 2. 根据enableSlip设置对应菜单的滑出启用状态
     * </p>
     * <p>
     * 特殊说明：
     * - 该方法用于动态控制菜单的滑出功能，适应不同场景需求
     * - 滑出功能禁用时，菜单不会响应滑出手势，但仍可通过其他方式打开
     * </p>
     * <p>
     * 相关方法：
     * - sliderZone.setEnableSlip(): 设置滑出区域的启用状态
     * </p>
     */
    private Consumer<MainMsgSliderZone> consumerMainMenuEnableSlip = new Consumer<MainMsgSliderZone>() {
        @Override
        public void accept(MainMsgSliderZone mainMsgSliderZone) throws Exception {
            switch (mainMsgSliderZone.getMenuIndex()) { // 根据菜单索引判断菜单类型
                case MainMsgSliderZone.MENUSLIP_LEFT: // 左侧菜单
                    sliderZone_left.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置左侧菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_TOP: // 顶部菜单
                    sliderZone_top.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置顶部菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_CH1: // CH1通道菜单
                    sliderZone_rightCh1.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置CH1菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_CH2: // CH2通道菜单
                    sliderZone_rightCh2.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置CH2菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_CH3: // CH3通道菜单
                    sliderZone_rightCh3.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置CH3菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_CH4: // CH4通道菜单
                    sliderZone_rightCh4.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置CH4菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_CH5: // CH5通道菜单
                    sliderZone_rightCh5.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置CH5菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_CH6: // CH6通道菜单
                    sliderZone_rightCh6.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置CH6菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_CH7: // CH7通道菜单
                    sliderZone_rightCh7.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置CH7菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_CH8: // CH8通道菜单
                    sliderZone_rightCh8.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置CH8菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_MATH1: // Math1数学通道菜单
                    sliderZone_rightMath1.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Math1菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_MATH2: // Math2数学通道菜单
                    sliderZone_rightMath2.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Math2菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_MATH3: // Math3数学通道菜单
                    sliderZone_rightMath3.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Math3菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_MATH4: // Math4数学通道菜单
                    sliderZone_rightMath4.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Math4菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_MATH5: // Math5数学通道菜单
                    sliderZone_rightMath5.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Math5菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_MATH6: // Math6数学通道菜单
                    sliderZone_rightMath6.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Math6菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_MATH7: // Math7数学通道菜单
                    sliderZone_rightMath7.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Math7菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_MATH8: // Math8数学通道菜单
                    sliderZone_rightMath8.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Math8菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_REF1: // Ref1参考通道菜单
                    sliderZone_rightRef1.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Ref1菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_REF2: // Ref2参考通道菜单
                    sliderZone_rightRef2.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Ref2菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_REF3: // Ref3参考通道菜单
                    sliderZone_rightRef3.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Ref3菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_REF4: // Ref4参考通道菜单
                    sliderZone_rightRef4.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Ref4菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_REF5: // Ref5参考通道菜单
                    sliderZone_rightRef5.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Ref5菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_REF6: // Ref6参考通道菜单
                    sliderZone_rightRef6.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Ref6菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_REF7: // Ref7参考通道菜单
                    sliderZone_rightRef7.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Ref7菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_REF8: // Ref8参考通道菜单
                    sliderZone_rightRef8.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Ref8菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_S1: // Serial1串行通道菜单
                    sliderZone_rightS1.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Serial1菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_S2: // Serial2串行通道菜单
                    sliderZone_rightS2.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Serial2菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_S3: // Serial3串行通道菜单
                    sliderZone_rightS3.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Serial3菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_S4: // Serial4串行通道菜单
                    sliderZone_rightS4.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置Serial4菜单的滑出启用状态
                    break;
                case MainMsgSliderZone.MENUSLIP_BOTTOM: // 底部菜单
                    sliderZone_bottom.setEnableSlip(mainMsgSliderZone.isEnableSlip()); // 设置底部菜单的滑出启用状态
                    break;
            }
        }
    };

    /**
     * 隐藏中心菜单和半屏菜单对话框
     * 当底部滑动菜单从显示变为隐藏时调用此方法，确保相关的菜单对话框也被隐藏
     * 
     * 功能说明：
     * - 当bottomSlip隐藏时，同步隐藏centerMenu和centerHalf对话框
     * - 检查DIALOG_MENUHALF是否正在显示，如果显示则隐藏它
     * - 确保界面状态的一致性，避免菜单重叠显示
     * 
     * 调用时机：
     * - 当bottomSlip从显示状态变为隐藏状态时调用
     * - 在hideAllSlip()方法中被调用
     * - 在滑动菜单状态切换时被调用
     * 
     * 注意事项：
     * - 注释掉的代码显示曾经需要隐藏DIALOG_CENTERTIMEBASE，但现在已经不需要
     * - 只处理DIALOG_MENUHALF的隐藏逻辑
     */
    public void hideCenterMenuAndCenterHalf() {
        //当前为bottomSlip从显示变为隐藏时，使centerMenu和centerHalf变为隐藏

//        if (centerMenuLayout.getVisibility() == VISIBLE) {
//            RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, DIALOG_CENTERTIMEBASE);
//        }
        // 检查半屏菜单对话框是否正在显示
        if (isDialogShow(DIALOG_MENUHALF)) {
            // 如果正在显示，则隐藏半屏菜单对话框
            hideDialog(DIALOG_MENUHALF);
        }

    }

    /**
     * 隐藏所有对话框和滑出菜单（除了触发电平菜单）
     * <p>
     * 功能描述：
     * - 循环调用hideAllDialogsButDialogOkCancel()隐藏所有对话框
     * - 调用hideAllSlip()隐藏所有滑出菜单
     * - 保留触发电平菜单（LevelMenu）不被隐藏
     * </p>
     * <p>
     * 调用时机：
     * - 当需要清理界面，但保留触发电平菜单时
     * - 在某些特殊操作前需要清空其他UI元素时
     * </p>
     * <p>
     * 返回值：无
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 使用do-while循环反复调用hideAllDialogsButDialogOkCancel()
     * 2. 直到所有对话框都被隐藏（返回false）
     * 3. 调用hideAllSlip()隐藏所有滑出菜单
     * </p>
     * <p>
     * 特殊说明：
     * - 触发电平菜单是特殊的UI元素，在某些场景下需要保持显示
     * - 使用循环确保所有嵌套的对话框都被隐藏
     * </p>
     * <p>
     * 相关方法：
     * - hideAllDialogsButDialogOkCancel(): 隐藏所有对话框（除了确认取消对话框）
     * - hideAllSlip(): 隐藏所有滑出菜单
     * </p>
     */
    public void hideAllDialogSlipButLevelMenu() {
        boolean b; // 定义布尔变量，用于判断是否还有对话框需要隐藏
        do { // 使用do-while循环反复隐藏对话框
            b = hideAllDialogsButDialogOkCancel(); // 调用hideAllDialogsButDialogOkCancel()方法，返回true表示还有对话框被隐藏
        } while (b); // 如果返回true，继续循环，直到所有对话框都被隐藏
        hideAllSlip(); // 隐藏所有滑出菜单
    }

    /**
     * 隐藏所有对话框和滑出菜单（包括触发电平菜单）
     * <p>
     * 功能描述：
     * - 调用hideAllDialogSlipButLevelMenu()隐藏所有对话框和滑出菜单（除了触发电平菜单）
     * - 调用hideLevelMenu()隐藏触发电平菜单
     * - 完全清空界面上的所有UI元素
     * </p>
     * <p>
     * 调用时机：
     * - 当需要完全清空界面时
     * - 在切换模式、退出功能等场景下调用
     * </p>
     * <p>
     * 返回值：无
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 调用hideAllDialogSlipButLevelMenu()隐藏除触发电平外的所有UI元素
     * 2. 调用hideLevelMenu()隐藏触发电平菜单
     * </p>
     * <p>
     * 特殊说明：
     * - 该方法会隐藏所有UI元素，包括触发电平菜单
     * - 与hideAllDialogSlipButLevelMenu()的区别在于是否隐藏触发电平菜单
     * </p>
     * <p>
     * 相关方法：
     * - hideAllDialogSlipButLevelMenu(): 隐藏所有对话框和滑出菜单（除了触发电平菜单）
     * - hideLevelMenu(): 隐藏触发电平菜单
     * </p>
     */
    public void hideAllDialogSlip() {
        hideAllDialogSlipButLevelMenu(); // 隐藏所有对话框和滑出菜单（除了触发电平菜单）
        hideLevelMenu(); // 隐藏触发电平菜单
    }

    /**
     * 测量行数变化消费者
     * 用于响应测量项行数的变化，动态调整波形显示区域的高度
     * 
     * 功能说明：
     * - 监听测量项行数的变化事件（通过RxBus订阅）
     * - 根据新的行数计算对应的波形区域高度
     * - 如果高度没有变化且行数不为0，则不执行任何操作
     * - 如果高度发生变化，则调用changeWaveHeight()调整波形区域高度
     * - 同时调整底部测量项视图的高度
     * 
     * 调用时机：
     * - 当用户添加或删除测量项时触发
     * - 当测量项的显示行数发生变化时触发
     * - 通过RxBus订阅MEASURE_ROW_COUNT事件
     * 
     * 参数说明：
     * @param rowCount 新的测量项行数（0-4行）
     *                 0: 无测量项显示
     *                 1: 1行测量项显示
     *                 2: 2行测量项显示
     *                 3: 3行测量项显示
     *                 4: 4行测量项显示
     * 
     * 实现细节：
     * - GlobalVar.get().getMeasureRowToHeightMap()维护了行数到高度的映射关系
     * - ScopeBase.getNewHeight()获取当前波形区域的高度
     * - changeWaveHeight()方法负责调整波形区域高度及相关组件
     * - mainBottomMeasureItem.changeItemViewHeight()调整底部测量项视图高度
     * 
     * @see GlobalVar.getMeasureRowToHeightMap() 获取行数到高度的映射表
     * @see ScopeBase.getNewHeight() 获取当前波形区域高度
     * @see changeWaveHeight() 调整波形区域高度
     * @see MainBottomMeasureItem.changeItemViewHeight() 调整测量项视图高度
     */
    private Consumer<Integer> consumerMeasureRowCount = new Consumer<Integer>() {
        @Override
        public void accept(Integer rowCount) throws Throwable {
            // 根据测量行数获取对应的新高度值
            int newHeight = GlobalVar.get().getMeasureRowToHeightMap().get(rowCount);
            // 如果高度没有变化且行数不为0，则直接返回，不执行后续操作
            if (ScopeBase.getNewHeight() == newHeight && rowCount != 0) return;
            // 调用changeWaveHeight()方法调整波形区域高度
            changeWaveHeight(newHeight);
            // 调整底部测量项视图的高度
            mainBottomMeasureItem.changeItemViewHeight();
        }
    };


    /**
     * 改变波形显示区域的高度
     * 当测量项行数变化时，动态调整波形区域及相关组件的高度和位置
     * 
     * 功能说明：
     * - 根据新的高度值调整波形显示区域（textureView和viewSpace）的高度
     * - 控制底部测量项的显示状态（是否显示测量项区域）
     * - 更新所有与波形高度相关的组件和模块：
     *   - ScopeBase的转换比例
     *   - GlobalVar的主波形高度
     *   - WorkModeManage的波形区域大小
     *   - WaveGridManage的网格区域高度
     *   - WaveManage的波形和Zoom遮罩层
     *   - 右侧Channel区域的高度
     *   - CursorManage的光标高度范围
     *   - RulerManage的刻度显示
     *   - VoltageLineManage的电平位置
     *   - MeasureManage的测量指示器线高
     *   - TopBar的触发电平值
     * - 修复因高度变化导致的通道显示问题
     * 
     * 调用时机：
     * - 当测量项行数发生变化时调用
     * - 在consumerMeasureRowCount消费者中被调用
     * - 在测量项添加或删除时触发
     * 
     * 参数说明：
     * @param newHeight 新的波形区域高度（像素值）
     *                  可能的值：
     *                  - 880px: 4行测量项
     *                  - 920px: 3行测量项
     *                  - 960px: 2行测量项
     *                  - 1000px: 1行测量项
     *                  - 1040px: 0行测量项
     * 
     * 实现细节：
     * - 通过修改LayoutParams来调整视图高度
     * - 使用ScopeBase.setConvertScale()更新转换比例
     * - 调用各个管理类的相应方法来同步更新相关组件
     * - fixChannelNotShow()修复静态波形消失的问题
     * 
     * 注意事项：
     * - 注释掉的代码显示曾经需要调整更多组件的高度，但现在已经简化
     * - 高度变化会影响整个界面的布局和显示
     * - 需要确保所有相关组件都同步更新，避免显示不一致
     * 
     * @see ScopeBase.setConvertScale() 设置转换比例
     * @see GlobalVar.changeMainWaveH() 改变主波形高度
     * @see WorkModeManage.changeWaveZoneSize() 改变波形区域大小
     * @see WaveGridManage.setHeightDiv() 设置网格区域高度
     * @see WaveManage.setWaveChange() 设置波形变化
     * @see changeSlideView() 改变滑动视图
     * @see fixChannelNotShow() 修复通道不显示问题
     */
    private void changeWaveHeight(int newHeight) {
        // 判断是否需要显示底部测量项：如果新高度不等于0行测量项的高度，则需要显示
        boolean isShowBottomItem = newHeight != GlobalVar.get().getMeasureRowToHeightMap().get(0);
        // 获取textureView的布局参数
        ViewGroup.LayoutParams paramsTexture = textureView.getLayoutParams();
        // 获取viewSpace的布局参数
        ViewGroup.LayoutParams paramsSpace = viewSpace.getLayoutParams();
        // 设置textureView的新高度
        paramsTexture.height = newHeight;
        // 设置viewSpace的新高度
        paramsSpace.height = newHeight;
//                paramsSlide.height = newHeight; // 注释：曾经需要调整滑动布局高度
//                paramsChannelEight.height = newHeight; // 注释：曾经需要调整右侧8通道布局高度
//                measureLayoutParams.y = newHeight - 1; // 注释：曾经需要调整测量项布局的Y坐标

        // 应用viewSpace的新布局参数
        viewSpace.setLayoutParams(paramsSpace);
        // 应用textureView的新布局参数
        textureView.setLayoutParams(paramsTexture);
//                mSlideLayout.setLayoutParams(paramsSlide); // 注释：曾经需要应用滑动布局参数
//                mainRightChannelLayoutEight.setLayoutParams(paramsChannelEight); // 注释：曾经需要应用右侧通道布局参数
//                mainBottomMeasureItem.setLayoutParams(measureLayoutParams); // 注释：曾经需要应用测量项布局参数
        // 设置底部测量项的显示状态：如果需要显示则设置为VISIBLE，否则设置为GONE
        mainBottomMeasureItem.setVisibility(isShowBottomItem ? View.VISIBLE : View.GONE);

        // 更新ScopeBase的转换比例（根据新高度重新计算）
        ScopeBase.setConvertScale(newHeight);
        // 更新GlobalVar的主波形高度
        GlobalVar.get().changeMainWaveH(newHeight); //波形区域高
        // 更新WorkModeManage的波形区域大小（包括波形区域和Zoom区域）
        WorkModeManage.getInstance().changeWaveZoneSize(newHeight);//波形区域 + Zoom区域 显示位置调整
        // 更新WaveGridManage的网格区域高度（高度除以垂直网格数量）
        WaveGridManage.getInstance().setHeightDiv(newHeight / ScopeBase.getVerticalGridCnt());//网格区域高
        // 更新WaveManage的波形和Zoom遮罩层
        WaveManage.get().setWaveChange(newHeight);//波形 + Zoom遮罩层
        // 更新右侧Channel区域的高度
        changeSlideView(newHeight);//右边channel区域
        // 更新CursorManage的光标高度显示范围
        CursorManage.getInstance().changeHeight(newHeight);//光标高度显示范围
        // 更新RulerManage的刻度显示（根据当前工作模式）
        RulerManage.getIns().switchWorkMode(WorkModeManage.getInstance().getmWorkMode());//刻度
        // 更新VoltageLineManage的各电平位置
        VoltageLineManage.getInstance().refresh();//各电平位置
        // 更新MeasureManage的测量指示器线高
        MeasureManage.getInstance().getMeasureIndication().changeLineHeight();//测量指示器 线高
        // 更新TopBar的触发电平值（发送消息更新保存的值）
        mTopBar_quick.getLayoutTrigger().sendMsg();//更新保存的触发电平值
        // 修复因高度变化导致的通道不显示问题（特别是静态波形）
        fixChannelNotShow();
    }

    /**
     * 测量项打开命令消费者（已废弃）
     * 曾经用于响应测量项打开/关闭的命令消息，动态调整波形区域高度
     * 
     * 功能说明（历史功能，现已废弃）：
     * - 监听测量项打开/关闭的命令消息（通过RxBus订阅）
     * - 解析命令字符串，获取是否添加测量项和当前测量项数量
     * - 根据测量项数量的变化，计算新的波形区域高度
     * - 调整波形区域及相关组件的高度和显示状态
     * 
     * 废弃原因：
     * - 此功能已经被consumerMeasureRowCount替代
     * - 现在使用更简洁的行数变化监听方式
     * - 代码逻辑复杂，维护成本高
     * - 保留了注释代码作为历史参考
     * 
     * 历史实现细节（注释代码说明）：
     * - 参数格式：isAdd;measureCount（例如："true;1"表示添加测量项，当前有1个）
     * - measureCount的值：
     *   - 1: 从无到有（添加第一个测量项）
     *   - 11: 从1行到2行（添加测量项使行数变为2）
     *   - 21: 从2行到3行
     *   - 31: 从3行到4行
     *   - 30: 从4行到3行（删除测量项）
     *   - 20: 从3行到2行
     *   - 10: 从2行到1行
     *   - 0: 从有到无（删除最后一个测量项）
     * - 根据measureCount和isAdd判断高度变化方向
     * - 调用changeWaveHeight()和相关组件更新方法
     * 
     * 当前状态：
     * - 方法体为空，所有代码都被注释
     * - 保留作为历史参考，不执行任何操作
     * - 功能已迁移到consumerMeasureRowCount
     * 
     * @see consumerMeasureRowCount 当前的测量行数变化消费者
     * @see changeWaveHeight() 改变波形区域高度
     */
    private Consumer<String> consumerCommandMeasureOpenToUI = new Consumer<String>() {
        @Override
        public void accept(String str) throws Exception {
//            if(StrUtil.isEmpty(str)) return; // 注释：曾经检查字符串是否为空
//            Logger.e(TAG, "str= " + str); // 注释：曾经记录日志
//            boolean isAdd = Boolean.parseBoolean(str.split(CommandMsgToUI.PARAM_SPLIT)[0]); // 注释：曾经解析是否添加测量项
//            int measureCount = Integer.parseInt(str.split(CommandMsgToUI.PARAM_SPLIT)[1]); // 注释：曾经解析测量项数量
//            ViewGroup.LayoutParams paramsTexture = textureView.getLayoutParams(); // 注释：曾经获取textureView布局参数
//            ViewGroup.LayoutParams paramsSpace = viewSpace.getLayoutParams(); // 注释：曾经获取viewSpace布局参数
////            ViewGroup.LayoutParams paramsSlide = mSlideLayout.getLayoutParams(); // 注释：曾经获取滑动布局参数
////            ViewGroup.LayoutParams paramsChannelEight = mainRightChannelLayoutEight.getLayoutParams(); // 注释：曾经获取右侧通道布局参数
////            AbsoluteLayout.LayoutParams measureLayoutParams = (AbsoluteLayout.LayoutParams) mainBottomMeasureItem.getLayoutParams(); // 注释：曾经获取测量项布局参数
//            boolean isShowBottomItem = false;//默认不显示 // 注释：曾经设置默认不显示底部测量项
//            boolean needChange = false; // 注释：曾经标记是否需要改变高度
//            int newHeight = (int) getResources().getDimension(R.dimen.mainCenterHeightMeasureRow0); // 注释：曾经设置默认高度（0行测量项）
//            switch (measureCount) { // 注释：曾经根据测量项数量判断高度变化
//                case 1: // 注释：从无到有（添加第一个测量项）
//                    if (isAdd) {//从无到有
//                        newHeight = (int) getResources().getDimension(R.dimen.mainCenterHeightMeasureRow1); // 注释：设置为1行测量项高度
//                        isShowBottomItem = true; // 注释：显示底部测量项
//                        needChange = true; // 注释：标记需要改变
//                    }
//                    break;
//                case 11://从一行到两行 // 注释：从1行到2行
//                    if (isAdd) {
//                        newHeight = (int) getResources().getDimension(R.dimen.mainCenterHeightMeasureRow2); // 注释：设置为2行测量项高度
//                        isShowBottomItem = true; // 注释：显示底部测量项
//                        needChange = true; // 注释：标记需要改变
//                    }
//                    break;
//                case 21://从两行到三行 // 注释：从2行到3行
//                    if (isAdd) {
//                        newHeight = (int) getResources().getDimension(R.dimen.mainCenterHeightMeasureRow3); // 注释：设置为3行测量项高度
//                        isShowBottomItem = true; // 注释：显示底部测量项
//                        needChange = true; // 注释：标记需要改变
//                    }
//                    break;
//                case 31://从三行到四行 // 注释：从3行到4行
//                    if (isAdd) {
//                        newHeight = (int) getResources().getDimension(R.dimen.mainCenterHeightMeasureRow4); // 注释：设置为4行测量项高度
//                        isShowBottomItem = true; // 注释：显示底部测量项
//                        needChange = true; // 注释：标记需要改变
//                    }
//                    break;
//                case 30: // 注释：从4行到3行（删除测量项）
//                    if (!isAdd) { //从四行到三行
//                        newHeight = (int) getResources().getDimension(R.dimen.mainCenterHeightMeasureRow3); // 注释：设置为3行测量项高度
//                        isShowBottomItem = true; // 注释：显示底部测量项
//                        needChange = true; // 注释：标记需要改变
//                    }
//                    break;
//                case 20: // 注释：从3行到2行（删除测量项）
//                    if (!isAdd) { //从三行到两行
//                        newHeight = (int) getResources().getDimension(R.dimen.mainCenterHeightMeasureRow2); // 注释：设置为2行测量项高度
//                        isShowBottomItem = true; // 注释：显示底部测量项
//                        needChange = true; // 注释：标记需要改变
//                    }
//                    break;
//                case 10: // 注释：从2行到1行（删除测量项）
//                    if (!isAdd) { //从两行到一行
//                        newHeight = (int) getResources().getDimension(R.dimen.mainCenterHeightMeasureRow1); // 注释：设置为1行测量项高度
//                        isShowBottomItem = true; // 注释：显示底部测量项
//                        needChange = true; // 注释：标记需要改变
//                    }
//                    break;
//                case 0: // 注释：从有到无（删除最后一个测量项）
//                    if (!isAdd) { //从有到无
//                        newHeight = (int) getResources().getDimension(R.dimen.mainCenterHeightMeasureRow0); // 注释：设置为0行测量项高度
//                        isShowBottomItem = false; // 注释：隐藏底部测量项
//                        needChange = true; // 注释：标记需要改变
//                    }
//                    break;
//                default: // 注释：默认情况，不改变高度
//                    break;
//            }
//
//            if (needChange) { // 注释：如果需要改变高度，则执行以下操作
//                paramsTexture.height = newHeight; // 注释：设置textureView新高度
//                paramsSpace.height = newHeight; // 注释：设置viewSpace新高度
////                paramsSlide.height = newHeight; // 注释：曾经设置滑动布局高度
////                paramsChannelEight.height = newHeight; // 注释：曾经设置右侧通道布局高度
////                measureLayoutParams.y = newHeight - 1; // 注释：曾经设置测量项布局Y坐标
//
//                viewSpace.setLayoutParams(paramsSpace); // 注释：应用viewSpace布局参数
//                textureView.setLayoutParams(paramsTexture); // 注释：应用textureView布局参数
////                mSlideLayout.setLayoutParams(paramsSlide); // 注释：曾经应用滑动布局参数
////                mainRightChannelLayoutEight.setLayoutParams(paramsChannelEight); // 注释：曾经应用右侧通道布局参数
////                mainBottomMeasureItem.setLayoutParams(measureLayoutParams); // 注释：曾经应用测量项布局参数
//                mainBottomMeasureItem.setVisibility(isShowBottomItem ? View.VISIBLE : View.GONE); // 注释：设置底部测量项显示状态
//
//                ScopeBase.setConvertScale(newHeight); // 注释：更新转换比例
//                GlobalVar.get().changeMainWaveH(newHeight); //波形区域高 // 注释：更新主波形高度
//                WorkModeManage.getInstance().changeWaveZoneSize(newHeight);//波形区域 + Zoom区域 显示位置调整 // 注释：更新波形区域大小
//                WaveGridManage.getInstance().setHeightDiv(newHeight / ScopeBase.getVerticalGridCnt());//网格区域高 // 注释：更新网格高度
//                WaveManage.get().setWaveChange(newHeight);//波形 + Zoom遮罩层 // 注释：更新波形变化
//                changeSlideView(newHeight);//右边channel区域 // 注释：更新右侧通道区域
//                CursorManage.getInstance().changeHeight(newHeight);//光标高度显示范围 // 注释：更新光标高度
//                RulerManage.getIns().switchWorkMode(WorkModeManage.getInstance().getmWorkMode());//刻度 // 注释：更新刻度显示
//                VoltageLineManage.getInstance().refresh();//各电平位置 // 注释：更新电平位置
//                MeasureManage.getInstance().getMeasureIndication().changeLineHeight();//测量指示器 线高 // 注释：更新测量指示器线高
//                mTopBar_quick.getLayoutTrigger().sendMsg();//更新保存的触发电平值 // 注释：更新触发电平值
//                fixChannelNotShow(); // 注释：修复通道不显示问题
//            }
        }
    };

    //#7805 添加/删除测量项致测量行数变化，ref波形就会消失
    //0010508 静态波形 添加/删除测量项致使波形区域大小变化，波形不应该消失
    /**
     * 修复通道不显示问题
     * 当波形区域高度变化时，修复静态波形（Ref）和动态波形（Channel）消失的问题
     * 
     * 功能说明：
     * - 解决Bug #7805：添加/删除测量项导致测量行数变化时，Ref波形会消失
     * - 解决Bug #0010508：静态波形在波形区域大小变化时不应该消失
     * - 重新激活Ref通道（静态波形通道）
     * - 重新激活动态通道（普通波形通道）
     * - 设置当前活动通道，确保通道选择器状态正确
     * 
     * 调用时机：
     * - 在changeWaveHeight()方法中被调用
     * - 当波形区域高度发生变化时调用
     * - 在测量项添加或删除导致高度变化后调用
     * 
     * 实现细节：
     * - 首先获取当前活动通道的索引（从缓存中读取）
     * - 遍历所有Ref通道（R1-R8），检查是否开启
     * - 如果Ref通道开启，重新调用open()方法激活它
     * - 遍历所有动态通道（Ch1-Ch8），检查是否可见
     * - 如果动态通道可见，重新调用activate()方法激活它
     * - 最后设置当前活动通道，确保通道选择器状态正确
     * 
     * 问题原因：
     * - 波形区域高度变化会影响波形的渲染和显示
     * - 某些通道在高度变化后可能不会自动重新显示
     * - 需要手动触发通道的激活操作
     * 
     * 注意事项：
     * - 只处理第一个找到的开启的Ref通道（使用break跳出循环）
     * - 只处理第一个找到的可见的动态通道（使用break跳出循环）
     * - 确保通道选择器的状态与当前活动通道一致
     * 
     * @see CacheUtil.getInt() 获取缓存的整数值
     * @see CacheUtil.getBoolean() 获取缓存的布尔值
     * @see ChannelFactory.getRefChannel() 获取Ref通道实例
     * @see ChannelFactory.getDynamicChannel() 获取动态通道实例
     * @see RefChannel.open() 打开Ref通道
     * @see Channel.activate() 激活动态通道
     * @see MiddleMain.getChanSelectorManage().setActivityChannel() 设置活动通道
     */
    private void fixChannelNotShow() {
        // 从缓存中获取当前活动通道的索引
        int chActive = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT);

        // 遍历所有Ref通道（R1到R8）
        for (int i = TChan.R1; i <= TChan.R8; i++) {
            // 从缓存中获取该Ref通道是否开启的状态
            boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + i);
            // 如果Ref通道未开启，跳过继续检查下一个
            if (!refCheck) continue;
            // 获取Ref通道实例（通过FPGA通道号）
            RefChannel channel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(i));
            // 如果通道实例为null，跳过继续检查下一个
            if (channel == null) continue;
            // 重新打开Ref通道，激活静态波形显示
            channel.open();
            // 只处理第一个开启的Ref通道，跳出循环
            break;
        }

        // 遍历所有动态通道（Ch1到Ch8）
        for (int i = TChan.Ch1; i <= TChan.Ch8; i++) {
            // 从缓存中获取该动态通道是否可见的状态
            boolean channelVisible = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + i);
            // 如果动态通道不可见，跳过继续检查下一个
            if (!channelVisible) continue;
            // 获取动态通道实例（通过FPGA通道号）
            Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(i));
            // 如果通道实例为null，跳过继续检查下一个
            if (channel == null) continue;
            // 重新激活动态通道，确保波形显示
            channel.activate();
            // 只处理第一个可见的动态通道，跳出循环
            break;
        }

        // 设置当前活动通道，确保通道选择器状态正确
        MiddleMain.getIns().getChanSelectorManage().setActivityChannel(IChan.toIChan(chActive));

    }

    // 定义右侧滑动菜单的Y坐标偏移量（用于Math/Ref/Bus等菜单）
    private int slipOtherY = (int) getResources().getDimension(R.dimen.rightSlipOtherYBig);
    // 定义最终使用的Y坐标偏移量（初始值等于slipOtherY）
    private int finalSlipOtherY = slipOtherY;
    // 定义公式键盘对话框的偏移量（根据测量行数动态调整）
    private int dialogFormulaOffset = (int) getResources().getDimension(R.dimen.rightDialogFormulaKeyBoardRow0);
    // 定义最终使用的公式键盘偏移量（初始值等于dialogFormulaOffset）
    private int finalDialogFormulaOffset = dialogFormulaOffset;
    // 定义浮点数键盘对话框的偏移量（根据测量行数动态调整）
    private int dialogFloatOffset = (int) getResources().getDimension(R.dimen.rightDialogFloatKeyBoardRow0);
    // 定义最终使用的浮点数键盘偏移量（初始值等于dialogFloatOffset）
    private int finalDialogFloatOffset = dialogFloatOffset;
    // 定义数字键盘对话框的偏移量（根据测量行数动态调整）
    private int dialogNumberOffset = (int) getResources().getDimension(R.dimen.rightDialogNumberKeyBoardRow0);
    // 定义最终使用的数字键盘偏移量（初始值等于dialogNumberOffset）
    private int finalDialogNumberOffset = dialogNumberOffset;

    /**
     * 改变滑动视图的布局和偏移量
     * 根据波形区域高度动态调整右侧滑动菜单和相关对话框的显示位置
     * 
     * 功能说明：
     * - 根据波形区域高度（对应不同的测量行数）调整右侧滑动菜单的布局
     * - 更新公式键盘、浮点数键盘、数字键盘对话框的显示偏移量
     * - 调整Math/Ref/Bus选择旋钮框的偏移量
     * - 更新右侧Channel布局的垂直边距
     * - 通知ExternalKeysNodeUtil更新Math/Ref/Bus设置页面的旋钮坐标
     * 
     * 调用时机：
     * - 在changeWaveHeight()方法中被调用
     * - 当波形区域高度发生变化时调用
     * - 在测量项添加或删除导致高度变化后调用
     * 
     * 参数说明：
     * @param height 新的波形区域高度（像素值）
     *               可能的值：
     *               - 880px: 4行测量项
     *               - 920px: 3行测量项
     *               - 960px: 2行测量项
     *               - 1000px: 1行测量项
     *               - 1040px: 0行测量项
     * 
     * 实现细节：
     * - 使用switch语句根据高度值设置不同的偏移量
     * - 每个case对应不同的测量行数状态
     * - 更新ViewUtils中的全局偏移量设置
     * - 调整右侧Channel布局中各个子视图的垂直边距
     * - 通过RxBus通知旋钮坐标更新
     * 
     * 偏移量说明：
     * - dialogFormulaOffset: 公式键盘对话框的Y坐标偏移量
     * - dialogFloatOffset: 浮点数键盘对话框的Y坐标偏移量
     * - dialogNumberOffset: 数字键盘对话框的Y坐标偏移量
     * - mathRefBusOffset: Math/Ref/Bus选择旋钮框的偏移量（相对于原来位置）
     * - verticalMargin: 右侧Channel布局的垂直边距
     * 
     * 注意事项：
     * - 注释掉的代码显示曾经需要调整更多参数，但现在已经简化
     * - 高度变化会影响所有右侧滑动菜单和对话框的显示位置
     * - 需要确保所有相关组件都同步更新，避免显示不一致
     * 
     * @see ViewUtils.setDialogFormulaOffset() 设置公式键盘偏移量
     * @see ViewUtils.setDialogFloatOffset() 设置浮点数键盘偏移量
     * @see ViewUtils.setDialogNumberOffset() 设置数字键盘偏移量
     * @see ViewUtils.setMathRefBusOffset() 设置Math/Ref/Bus偏移量
     */
    private void changeSlideView(int height) {
        // 定义右侧Channel布局的垂直边距（默认为中等大小）
        int verticalMargin = (int) getResources().getDimension(R.dimen.rightSlipMarginVerticalMiddle);
        // 定义Math/Ref/Bus选择旋钮框相对于原来位置的偏移量（默认为-7）
        int mathRefBusOffset = -7;//Math/Ref/Bus select旋钮框相对于原来偏移
        // 根据高度值（对应不同的测量行数）设置不同的偏移量
        switch (height) {
            case 880://4行测量项
                // 设置4行测量项时的公式键盘偏移量
                dialogFormulaOffset = (int) getResources().getDimension(R.dimen.rightDialogFormulaKeyBoardRow4);
                // 设置4行测量项时的浮点数键盘偏移量
                dialogFloatOffset = (int) getResources().getDimension(R.dimen.rightDialogFloatKeyBoardRow4);
                // 设置4行测量项时的数字键盘偏移量
                dialogNumberOffset = (int) getResources().getDimension(R.dimen.rightDialogNumberKeyBoardRow4);
                // 设置4行测量项时的Math/Ref/Bus偏移量
                mathRefBusOffset = -167;
                break;
            case 920://3行测量项
                // 设置3行测量项时的公式键盘偏移量
                dialogFormulaOffset = (int) getResources().getDimension(R.dimen.rightDialogFormulaKeyBoardRow3);
                // 设置3行测量项时的浮点数键盘偏移量
                dialogFloatOffset = (int) getResources().getDimension(R.dimen.rightDialogFloatKeyBoardRow3);
                // 设置3行测量项时的数字键盘偏移量
                dialogNumberOffset = (int) getResources().getDimension(R.dimen.rightDialogNumberKeyBoardRow3);
                // 设置3行测量项时的Math/Ref/Bus偏移量
                mathRefBusOffset = -127;
                break;
            case 960://2行测量项
                // 设置2行测量项时的公式键盘偏移量
                dialogFormulaOffset = (int) getResources().getDimension(R.dimen.rightDialogFormulaKeyBoardRow2);
                // 设置2行测量项时的浮点数键盘偏移量
                dialogFloatOffset = (int) getResources().getDimension(R.dimen.rightDialogFloatKeyBoardRow2);
                // 设置2行测量项时的数字键盘偏移量
                dialogNumberOffset = (int) getResources().getDimension(R.dimen.rightDialogNumberKeyBoardRow2);
                // 设置2行测量项时的Math/Ref/Bus偏移量
                mathRefBusOffset = -87;
                break;
            case 1000://1行测量项
                // 设置1行测量项时的公式键盘偏移量
                dialogFormulaOffset = (int) getResources().getDimension(R.dimen.rightDialogFormulaKeyBoardRow1);
                // 设置1行测量项时的浮点数键盘偏移量
                dialogFloatOffset = (int) getResources().getDimension(R.dimen.rightDialogFloatKeyBoardRow1);
                // 设置1行测量项时的数字键盘偏移量
                dialogNumberOffset = (int) getResources().getDimension(R.dimen.rightDialogNumberKeyBoardRow1);
                // 设置1行测量项时的Math/Ref/Bus偏移量
                mathRefBusOffset = -47;
                break;
            case 1040://0行测量项
                // 设置0行测量项时的公式键盘偏移量
                dialogFormulaOffset = (int) getResources().getDimension(R.dimen.rightDialogFormulaKeyBoardRow0);
                // 设置0行测量项时的浮点数键盘偏移量
                dialogFloatOffset = (int) getResources().getDimension(R.dimen.rightDialogFloatKeyBoardRow0);
                // 设置0行测量项时的数字键盘偏移量
                dialogNumberOffset = (int) getResources().getDimension(R.dimen.rightDialogNumberKeyBoardRow0);
                // 设置0行测量项时的Math/Ref/Bus偏移量
                mathRefBusOffset = -7;
                break;
        }

//        if (height == (int) getResources().getDimension(R.dimen.mainCenterHeightMeasureRow2)) { // 注释：曾经的条件判断逻辑
//            verticalMargin = (int) getResources().getDimension(R.dimen.rightSlipMarginVerticalSmall); // 注释：曾经设置小边距
//            slipOtherY = (int) getResources().getDimension(R.dimen.rightSlipOtherYSmall); // 注释：曾经设置小Y偏移
//            mathRefBusOffset = -83; // 注释：曾经设置偏移量
//            dialogFormulaOffset = (int) getResources().getDimension(R.dimen.rightDialogFormulaKeyBoardRow2); // 注释：曾经设置公式键盘偏移
//            dialogFloatOffset = (int) getResources().getDimension(R.dimen.rightDialogFloatKeyBoardRow2); // 注释：曾经设置浮点键盘偏移
//            dialogNumberOffset = (int) getResources().getDimension(R.dimen.rightDialogNumberKeyBoardRow2); // 注释：曾经设置数字键盘偏移
//        } else if (height == (int) getResources().getDimension(R.dimen.mainCenterHeightMeasureRow1)) { // 注释：曾经的条件判断逻辑
//            verticalMargin = (int) getResources().getDimension(R.dimen.rightSlipMarginVerticalMiddle); // 注释：曾经设置中等边距
//            slipOtherY = (int) getResources().getDimension(R.dimen.rightSlipOtherYMiddle); // 注释：曾经设置中等Y偏移
//            mathRefBusOffset = -43; // 注释：曾经设置偏移量
//            dialogFormulaOffset = (int) getResources().getDimension(R.dimen.rightDialogFormulaKeyBoardRow1); // 注释：曾经设置公式键盘偏移
//            dialogFloatOffset = (int) getResources().getDimension(R.dimen.rightDialogFloatKeyBoardRow1); // 注释：曾经设置浮点键盘偏移
//            dialogNumberOffset = (int) getResources().getDimension(R.dimen.rightDialogNumberKeyBoardRow1); // 注释：曾经设置数字键盘偏移
//        } else if (height == (int) getResources().getDimension(R.dimen.mainCenterHeightMeasureRow0)) { // 注释：曾经的条件判断逻辑
//            verticalMargin = (int) getResources().getDimension(R.dimen.rightSlipMarginVerticalBig); // 注释：曾经设置大边距
//            slipOtherY = (int) getResources().getDimension(R.dimen.rightSlipOtherYBig); // 注释：曾经设置大Y偏移
//            mathRefBusOffset = -3; // 注释：曾经设置偏移量
//            dialogFormulaOffset = (int) getResources().getDimension(R.dimen.rightDialogFormulaKeyBoardRow0); // 注释：曾经设置公式键盘偏移
//            dialogFloatOffset = (int) getResources().getDimension(R.dimen.rightDialogFloatKeyBoardRow0); // 注释：曾经设置浮点键盘偏移
//            dialogNumberOffset = (int) getResources().getDimension(R.dimen.rightDialogNumberKeyBoardRow0); // 注释：曾经设置数字键盘偏移
//        } else { // 注释：曾经的其他情况
//            return; // 注释：曾经直接返回
//        }
//        finalSlipOtherY = slipOtherY; // 注释：曾经设置最终Y偏移
        // 设置最终使用的公式键盘偏移量
        finalDialogFormulaOffset = dialogFormulaOffset;
        // 设置最终使用的浮点数键盘偏移量
        finalDialogFloatOffset = dialogFloatOffset;
        // 设置最终使用的数字键盘偏移量
        finalDialogNumberOffset = dialogNumberOffset;
        //处理TriggerLevel // 注释：曾经处理触发电平布局
//        ViewGroup.LayoutParams paramsTriggerLevel = clTriggerLevel.getLayoutParams(); // 注释：曾经获取触发电平布局参数
//        paramsTriggerLevel.height = height; // 注释：曾经设置触发电平高度
//        clTriggerLevel.setLayoutParams(paramsTriggerLevel); // 注释：曾经应用触发电平布局参数
//        levelSwipeLeft.setY((height - levelSwipeLeft.getHeight()) * 1.0f / 2 - 1); // 注释：曾经设置左侧滑动Y坐标
//        triggerLevel.setCenterY(height); // 注释：曾经设置触发电平中心Y坐标

        //处理MainRightLayoutItemChannelMaster // 注释：处理右侧Channel布局项
        // 定义子视图变量
        View childView;
        // 定义ConstraintLayout布局参数变量
        ConstraintLayout.LayoutParams layoutParams;
        // 遍历右侧Channel布局中的所有子视图
        for (int i = 0; i < mainRightChannelLayoutEight.getChildCount(); i++) {
            // 跳过第0个、第4个和最后一个子视图（这些是特殊视图，不需要调整边距）
            if (i == 0 || i == 4 || i == mainRightChannelLayoutEight.getChildCount() - 1) continue;
            // 获取当前子视图
            childView = mainRightChannelLayoutEight.getChildAt(i);
            // 获取当前子视图的布局参数
            layoutParams = (ConstraintLayout.LayoutParams) childView.getLayoutParams();
            // 根据子视图的位置设置不同的边距
            if (i < 4) {
                // 对于前4个子视图，设置顶部边距
                layoutParams.topMargin = verticalMargin;
            } else {
                // 对于后4个子视图，设置底部边距
                layoutParams.bottomMargin = verticalMargin;
            }
            // 应用新的布局参数到子视图
            childView.setLayoutParams(layoutParams);
        }

//        changeChannelLayout(height);//处理RightLayoutChannel // 注释：曾经处理右侧Channel布局
//        changeMathLayout(finalSlipOtherY);//处理RightLayoutMath // 注释：曾经处理右侧Math布局
//        changeRefLayout(finalSlipOtherY);//处理RightLayoutRef // 注释：曾经处理右侧Ref布局
//        changeBusLayout(finalSlipOtherY);//处理RightLayoutSerials // 注释：曾经处理右侧Bus布局

        //Math/Ref/Bus中涉及到的几个dialog // 注释：设置Math/Ref/Bus相关的对话框偏移量
        // 设置ViewUtils中的公式键盘偏移量
        ViewUtils.setDialogFormulaOffset(finalDialogFormulaOffset);
        // 设置ViewUtils中的浮点数键盘偏移量
        ViewUtils.setDialogFloatOffset(finalDialogFloatOffset);
        // 设置ViewUtils中的数字键盘偏移量
        ViewUtils.setDialogNumberOffset(finalDialogNumberOffset);
        // 设置ViewUtils中的Math/Ref/Bus偏移量
        ViewUtils.setMathRefBusOffset(mathRefBusOffset);

        //通知ExternalKeysNodeUtil中Math/Ref/Bus设置页面的旋钮坐标更新 // 注释：通知旋钮坐标更新
        // 通过RxBus发送消息，通知旋钮坐标更新
        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_SELECT_RECTANGLE, mathRefBusOffset);
    }


    /**
     * 改变Bus布局的Y坐标位置
     * 根据波形区域高度变化，调整所有Bus（串行通道）滑动菜单的Y坐标位置
     * 
     * 功能说明：
     * - 遍历所有Bus通道（S1-S4）
     * - 获取每个Bus布局的LayoutParams
     * - 设置新的Y坐标位置（slipOtherY）
     * - 应用新的布局参数
     * - 调用dealBusSlideZone()处理滑动区域参数
     * 
     * 调用时机：
     * - 在changeSlideView()方法中被调用（注释掉的代码）
     * - 当波形区域高度发生变化时可能被调用
     * - 当需要调整Bus菜单位置时调用
     * 
     * 参数说明：
     * @param slipOtherY 新的Y坐标偏移量（像素值）
     *                   根据测量行数不同，有不同的值
     * 
     * @see dealBusSlideZone() 处理Bus滑动区域参数
     */
    private void changeBusLayout(int slipOtherY) {
        // 遍历所有Bus通道（串行通道数量）
        for (int i = 0; i < ChannelFactory.SERIAL_CNT; i++) {
            // 获取第i个Bus布局实例
            RightLayoutSerials layoutBus = mRightBarBus.get(i);
            // 如果布局实例为null，跳过继续处理下一个
            if (layoutBus == null) continue;
            // 获取Bus布局的LayoutParams（AbsoluteLayout类型）
            AbsoluteLayout.LayoutParams busLayoutParams = (AbsoluteLayout.LayoutParams) layoutBus.getLayoutParams();
            // 设置新的Y坐标位置
            busLayoutParams.y = slipOtherY;
            // 应用新的布局参数到Bus布局
            layoutBus.setLayoutParams(busLayoutParams);
        }
        // 处理Bus滑动区域的参数设置
        dealBusSlideZone();
    }

    /**
     * 处理Bus滑动区域的参数设置
     * 为每个Bus通道配置滑动区域（SliderZone）的参数
     * 
     * 功能说明：
     * - 遍历所有Bus通道（S1-S4）
     * - 为每个Bus配置滑动区域参数：
     *   - 设置滑动区域为禁用状态
     *   - 设置滑动功能为启用
     *   - 设置当前显示状态为false
     *   - 设置滑动区域来源按钮
     *   - 设置隐藏区域屏幕坐标
     *   - 设置菜单显示起始位置
     *   - 设置滑动方向（从右到左）
     *   - 设置滑动视图组
     *   - 设置名称标签
     * - 将Bus布局置于最前端
     * - 在布局完成后创建滑动参数
     * 
     * 调用时机：
     * - 在changeBusLayout()方法中被调用
     * - 当Bus布局位置发生变化时调用
     * - 当需要重新配置Bus滑动区域时调用
     * 
     * 实现细节：
     * - 使用bringToFront()确保Bus布局在最上层
     * - 使用ViewUtils.doAfterLayout()在布局完成后创建参数
     * - 使用Screen.getViewLocation()获取布局在屏幕上的位置
     * - 滑动方向设置为SliderDir_RightToLeft（从右向左滑动）
     * 
     * @see SliderZone 滑动区域管理类
     * @see ViewUtils.doAfterLayout() 布局完成后执行操作
     * @see Screen.getViewLocation() 获取视图在屏幕上的位置
     */
    private void dealBusSlideZone() {
        // 遍历所有Bus通道（串行通道数量）
        for (int i = 0; i < ChannelFactory.SERIAL_CNT; i++) {
            // 获取第i个Bus布局实例
            RightLayoutSerials layoutBus = mRightBarBus.get(i);
            // 如果布局实例为null，跳过继续处理下一个
            if (layoutBus == null) continue;
            // 获取第i个Bus滑动区域实例
            SliderZone busSlide = mSlideZoneBus.get(i);
            // 如果滑动区域实例为null，跳过继续处理下一个
            if (busSlide == null) continue;
            // 获取第i个Bus主控按钮实例
            MainRightLayoutItemSerialsMaster busMaster = busMasters.get(i);
            // 如果主控按钮实例为null，跳过继续处理下一个
            if (busMaster == null) continue;

            // 将Bus布局置于最前端（确保在最上层显示）
            layoutBus.bringToFront();
            // 在布局完成后创建滑动参数（异步执行）
            ViewUtils.doAfterLayout(layoutBus, () -> busSlide.createParam(true));
            // 设置滑动区域为禁用状态（初始状态）
            busSlide.setEnable(false);
            // 设置滑动功能为启用（允许滑动操作）
            busSlide.setEnableSlip(true);
            // 设置当前显示状态为false（初始状态为隐藏）
            busSlide.setCurrShowState(false);
            // 设置滑动区域来源按钮（用于触发滑动）
            busSlide.setSliderZoneFromBtn(busMaster);
            // 如果滑动功能启用，设置隐藏区域的屏幕坐标
            if (busSlide.isEnableSlip()) {
                // 获取Bus布局在屏幕上的位置，设置为隐藏区域
                busSlide.setDownZone_Hide_Screen(Screen.getViewLocation(layoutBus));

            } else {
                // 如果滑动功能未启用，设置无效的隐藏区域
                busSlide.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
            }
            // 设置菜单显示的起始位置（使用主波形区域的X坐标）
            busSlide.setShowMenu_BeginPosion(GlobalVar.get().getMainWave().x);
            // 设置滑动方向为从右到左
            busSlide.setSliderDir(SliderZone.SliderDir_RightToLeft);
            // 设置滑动视图组（Bus布局）
            busSlide.setSliderViewGroup(layoutBus);
            // 设置滑动区域的名称标签（用于调试和识别）
            busSlide.setNameTag("sliderZone_group3_rightS" + (i + 1));
        }
    }

    /**
     * 改变Ref布局的Y坐标位置
     * 根据波形区域高度变化，调整所有Ref（参考通道）滑动菜单的Y坐标位置
     * 
     * 功能说明：
     * - 遍历所有Ref通道（R1-R8）
     * - 获取每个Ref布局的LayoutParams
     * - 设置新的Y坐标位置（slipOtherY）
     * - 应用新的布局参数
     * - 调用dealRefSlideZone()处理滑动区域参数
     * 
     * 调用时机：
     * - 在changeSlideView()方法中被调用（注释掉的代码）
     * - 当波形区域高度发生变化时可能被调用
     * - 当需要调整Ref菜单位置时调用
     * 
     * 参数说明：
     * @param slipOtherY 新的Y坐标偏移量（像素值）
     *                   根据测量行数不同，有不同的值
     * 
     * @see dealRefSlideZone() 处理Ref滑动区域参数
     */
    private void changeRefLayout(int slipOtherY) {
        // 遍历所有Ref通道（参考通道数量）
        for (int i = 0; i < ChannelFactory.REF_CNT; i++) {
            // 获取第i个Ref布局实例
            RightLayoutRef layoutRef = mRightBarRefs.get(i);
            // 如果布局实例为null，跳过继续处理下一个
            if (layoutRef == null) continue;
            // 获取Ref布局的LayoutParams（AbsoluteLayout类型）
            AbsoluteLayout.LayoutParams refLayoutParams = (AbsoluteLayout.LayoutParams) layoutRef.getLayoutParams();
            // 设置新的Y坐标位置
            refLayoutParams.y = slipOtherY;
            // 应用新的布局参数到Ref布局
            layoutRef.setLayoutParams(refLayoutParams);
        }
        // 处理Ref滑动区域的参数设置
        dealRefSlideZone();
    }

    /**
     * 处理Ref滑动区域的参数设置
     * 为每个Ref通道配置滑动区域（SliderZone）的参数
     * 
     * 功能说明：
     * - 遍历所有Ref通道（R1-R8）
     * - 为每个Ref配置滑动区域参数：
     *   - 设置滑动区域为禁用状态
     *   - 设置滑动功能为启用
     *   - 设置当前显示状态为false
     *   - 设置滑动区域来源按钮
     *   - 设置隐藏区域屏幕坐标
     *   - 设置菜单显示起始位置
     *   - 设置滑动方向（从右到左）
     *   - 设置滑动视图组
     *   - 设置名称标签
     * - 将Ref布局置于最前端
     * - 在布局完成后创建滑动参数
     * 
     * 调用时机：
     * - 在changeRefLayout()方法中被调用
     * - 当Ref布局位置发生变化时调用
     * - 当需要重新配置Ref滑动区域时调用
     * 
     * 实现细节：
     * - 使用bringToFront()确保Ref布局在最上层
     * - 使用ViewUtils.doAfterLayout()在布局完成后创建参数
     * - 使用Screen.getViewLocation()获取布局在屏幕上的位置
     * - 滑动方向设置为SliderDir_RightToLeft（从右向左滑动）
     * 
     * @see SliderZone 滑动区域管理类
     * @see ViewUtils.doAfterLayout() 布局完成后执行操作
     * @see Screen.getViewLocation() 获取视图在屏幕上的位置
     */
    private void dealRefSlideZone() {
        // 遍历所有Ref通道（参考通道数量）
        for (int i = 0; i < ChannelFactory.REF_CNT; i++) {
            // 获取第i个Ref布局实例
            RightLayoutRef layoutRef = mRightBarRefs.get(i);
            // 如果布局实例为null，跳过继续处理下一个
            if (layoutRef == null) continue;
            // 获取第i个Ref滑动区域实例
            SliderZone refSlide = mSlideZoneRefs.get(i);
            // 如果滑动区域实例为null，跳过继续处理下一个
            if (refSlide == null) continue;
            // 获取第i个Ref主控按钮实例
            MainRightLayoutItemChannelMaster refMaster = refMasters.get(i);
            // 如果主控按钮实例为null，跳过继续处理下一个
            if (refMaster == null) continue;
            // 将Ref布局置于最前端（确保在最上层显示）
            layoutRef.bringToFront();
            // 在布局完成后创建滑动参数（异步执行）
            ViewUtils.doAfterLayout(layoutRef, () -> refSlide.createParam(true));
            // 设置滑动区域为禁用状态（初始状态）
            refSlide.setEnable(false);
            // 设置滑动功能为启用（允许滑动操作）
            refSlide.setEnableSlip(true);
            // 设置当前显示状态为false（初始状态为隐藏）
            refSlide.setCurrShowState(false);
            // 设置滑动区域来源按钮（用于触发滑动）
            refSlide.setSliderZoneFromBtn(refMasters.get(i));
            // 如果滑动功能启用，设置隐藏区域的屏幕坐标
            if (refSlide.isEnableSlip()) {
                // 获取Ref布局在屏幕上的位置，设置为隐藏区域
                refSlide.setDownZone_Hide_Screen(Screen.getViewLocation(layoutRef));
            } else {
                // 如果滑动功能未启用，设置无效的隐藏区域
                refSlide.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
            }
            // 设置菜单显示的起始位置（使用主波形区域的X坐标）
            refSlide.setShowMenu_BeginPosion(GlobalVar.get().getMainWave().x);
            // 设置滑动方向为从右到左
            refSlide.setSliderDir(SliderZone.SliderDir_RightToLeft);
            // 设置滑动视图组（Ref布局）
            refSlide.setSliderViewGroup(layoutRef);
            // 设置滑动区域的名称标签（用于调试和识别）
            refSlide.setNameTag("sliderZone_group2_rightRef" + (i + 1));
        }
    }

    /**
     * 改变Math布局的Y坐标位置
     * 根据波形区域高度变化，调整所有Math（数学通道）滑动菜单的Y坐标位置
     * 
     * 功能说明：
     * - 遍历所有Math通道（M1-M8）
     * - 获取每个Math布局的LayoutParams
     * - 设置新的Y坐标位置（slipOtherY）
     * - 应用新的布局参数
     * - 调用dealMathSlideZone()处理滑动区域参数
     * 
     * 调用时机：
     * - 在changeSlideView()方法中被调用（注释掉的代码）
     * - 当波形区域高度发生变化时可能被调用
     * - 当需要调整Math菜单位置时调用
     * 
     * 参数说明：
     * @param slipOtherY 新的Y坐标偏移量（像素值）
     *                   根据测量行数不同，有不同的值
     * 
     * @see dealMathSlideZone() 处理Math滑动区域参数
     */
    private void changeMathLayout(int slipOtherY) {
        // 遍历所有Math通道（数学通道数量）
        for (int i = 0; i < ChannelFactory.MATH_CNT; i++) {
            // 获取第i个Math布局实例
            RightLayoutMath layoutMath = mRightBarMaths.get(i);
            // 如果布局实例为null，跳过继续处理下一个
            if (layoutMath == null) continue;
            // 获取Math布局的LayoutParams（AbsoluteLayout类型）
            AbsoluteLayout.LayoutParams mathLayoutParams = (AbsoluteLayout.LayoutParams) layoutMath.getLayoutParams();
            // 设置新的Y坐标位置
            mathLayoutParams.y = slipOtherY;
            // 应用新的布局参数到Math布局
            layoutMath.setLayoutParams(mathLayoutParams);
        }
        // 处理Math滑动区域的参数设置
        dealMathSlideZone();
    }

    /**
     * 处理Math滑动区域的参数设置
     * 为每个Math通道配置滑动区域（SliderZone）的参数
     * 
     * 功能说明：
     * - 遍历所有Math通道（M1-M8）
     * - 为每个Math配置滑动区域参数：
     *   - 设置滑动区域为禁用状态
     *   - 设置滑动功能为启用
     *   - 设置当前显示状态为false
     *   - 设置滑动区域来源按钮
     *   - 设置隐藏区域屏幕坐标
     *   - 设置菜单显示起始位置
     *   - 设置滑动方向（从右到左）
     *   - 设置滑动视图组
     *   - 设置名称标签
     * - 将Math布局置于最前端
     * - 在布局完成后创建滑动参数
     * 
     * 调用时机：
     * - 在changeMathLayout()方法中被调用
     * - 当Math布局位置发生变化时调用
     * - 当需要重新配置Math滑动区域时调用
     * 
     * 实现细节：
     * - 使用bringToFront()确保Math布局在最上层
     * - 使用ViewUtils.doAfterLayout()在布局完成后创建参数
     * - 使用Screen.getViewLocation()获取布局在屏幕上的位置
     * - 滑动方向设置为SliderDir_RightToLeft（从右向左滑动）
     * 
     * @see SliderZone 滑动区域管理类
     * @see ViewUtils.doAfterLayout() 布局完成后执行操作
     * @see Screen.getViewLocation() 获取视图在屏幕上的位置
     */
    private void dealMathSlideZone() {
        // 遍历所有Math通道（数学通道数量）
        for (int i = 0; i < ChannelFactory.MATH_CNT; i++) {
            // 获取第i个Math布局实例
            RightLayoutMath layoutMath = mRightBarMaths.get(i);
            // 如果布局实例为null，跳过继续处理下一个
            if (layoutMath == null) continue;
            // 获取第i个Math滑动区域实例
            SliderZone mathSlide = mSlideZoneMaths.get(i);
            // 如果滑动区域实例为null，跳过继续处理下一个
            if (mathSlide == null) continue;
            // 获取第i个Math主控按钮实例
            MainRightLayoutItemChannelMaster mathMaster = mathMasters.get(i);
            // 如果主控按钮实例为null，跳过继续处理下一个
            if(mathMaster == null) continue;
            // 将Math布局置于最前端（确保在最上层显示）
            layoutMath.bringToFront();
            // 在布局完成后创建滑动参数（异步执行）
            ViewUtils.doAfterLayout(layoutMath, () -> mathSlide.createParam(true));
            // 设置滑动区域为禁用状态（初始状态）
            mathSlide.setEnable(false);
            // 设置滑动功能为启用（允许滑动操作）
            mathSlide.setEnableSlip(true);
            // 设置当前显示状态为false（初始状态为隐藏）
            mathSlide.setCurrShowState(false);
            // 设置滑动区域来源按钮（用于触发滑动）
            mathSlide.setSliderZoneFromBtn(mathMaster);
            // 如果滑动功能启用，设置隐藏区域的屏幕坐标
            if (mathSlide.isEnableSlip()) {
                // 获取Math布局在屏幕上的位置，设置为隐藏区域
                mathSlide.setDownZone_Hide_Screen(Screen.getViewLocation(layoutMath));
            } else {
                // 如果滑动功能未启用，设置无效的隐藏区域
                mathSlide.setDownZone_Hide_Screen(new Rect(-1, -1, -1, -1));
            }
            // 设置菜单显示的起始位置（使用主波形区域的X坐标）
            mathSlide.setShowMenu_BeginPosion(GlobalVar.get().getMainWave().x);
            // 设置滑动方向为从右到左
            mathSlide.setSliderDir(SliderZone.SliderDir_RightToLeft);
            // 设置滑动视图组（Math布局）
            mathSlide.setSliderViewGroup(layoutMath);
            // 设置滑动区域的名称标签（用于调试和识别）
            mathSlide.setNameTag("sliderZone_group2_rightMath" + (i + 1));
        }
    }

    /**
     * 改变Channel布局的高度
     * 根据波形区域高度变化，调整所有Channel（模拟通道）布局的高度
     * 
     * 功能说明：
     * - 遍历所有Channel通道（Ch1-Ch8）
     * - 获取每个Channel布局的LayoutParams
     * - 设置新的高度值
     * - 应用新的布局参数
     * - 确保所有Channel布局的高度与波形区域高度一致
     * 
     * 调用时机：
     * - 在changeSlideView()方法中被调用（注释掉的代码）
     * - 当波形区域高度发生变化时可能被调用
     * - 当需要调整Channel菜单高度时调用
     * 
     * 参数说明：
     * @param height 新的高度值（像素值）
     *               可能的值：
     *               - 880px: 4行测量项
     *               - 920px: 3行测量项
     *               - 960px: 2行测量项
     *               - 1000px: 1行测量项
     *               - 1040px: 0行测量项
     * 
     * 实现细节：
     * - 使用AbsoluteLayout.LayoutParams来设置高度
     * - 每个Channel布局都需要单独设置高度
     * - 使用相同的LayoutParams对象，修改高度后应用到不同的布局
     * 
     * 注意事项：
     * - 此方法目前被注释掉，可能不再使用
     * - Channel布局的高度应该与波形区域高度保持一致
     */
    private void changeChannelLayout(int height) {
        // 获取Channel1布局的LayoutParams（AbsoluteLayout类型）
        AbsoluteLayout.LayoutParams channelLayoutParams = (AbsoluteLayout.LayoutParams) mRightBar_channel1.getLayoutParams();
        // 设置Channel1的新高度
        channelLayoutParams.height = height;
        // 应用新的布局参数到Channel1布局
        mRightBar_channel1.setLayoutParams(channelLayoutParams);

        // 获取Channel2布局的LayoutParams（复用同一个LayoutParams对象）
        channelLayoutParams = (AbsoluteLayout.LayoutParams) mRightBar_channel2.getLayoutParams();
        // 设置Channel2的新高度
        channelLayoutParams.height = height;
        // 应用新的布局参数到Channel2布局
        mRightBar_channel2.setLayoutParams(channelLayoutParams);

        // 获取Channel3布局的LayoutParams（复用同一个LayoutParams对象）
        channelLayoutParams = (AbsoluteLayout.LayoutParams) mRightBar_channel3.getLayoutParams();
        // 设置Channel3的新高度
        channelLayoutParams.height = height;
        // 应用新的布局参数到Channel3布局
        mRightBar_channel3.setLayoutParams(channelLayoutParams);

        // 获取Channel4布局的LayoutParams（复用同一个LayoutParams对象）
        channelLayoutParams = (AbsoluteLayout.LayoutParams) mRightBar_channel4.getLayoutParams();
        // 设置Channel4的新高度
        channelLayoutParams.height = height;
        // 应用新的布局参数到Channel4布局
        mRightBar_channel4.setLayoutParams(channelLayoutParams);

        // 获取Channel5布局的LayoutParams（复用同一个LayoutParams对象）
        channelLayoutParams = (AbsoluteLayout.LayoutParams) mRightBar_channel5.getLayoutParams();
        // 设置Channel5的新高度
        channelLayoutParams.height = height;
        // 应用新的布局参数到Channel5布局
        mRightBar_channel5.setLayoutParams(channelLayoutParams);

        // 获取Channel6布局的LayoutParams（复用同一个LayoutParams对象）
        channelLayoutParams = (AbsoluteLayout.LayoutParams) mRightBar_channel6.getLayoutParams();
        // 设置Channel6的新高度
        channelLayoutParams.height = height;
        // 应用新的布局参数到Channel6布局
        mRightBar_channel6.setLayoutParams(channelLayoutParams);

        // 获取Channel7布局的LayoutParams（复用同一个LayoutParams对象）
        channelLayoutParams = (AbsoluteLayout.LayoutParams) mRightBar_channel7.getLayoutParams();
        // 设置Channel7的新高度
        channelLayoutParams.height = height;
        // 应用新的布局参数到Channel7布局
        mRightBar_channel7.setLayoutParams(channelLayoutParams);

        // 获取Channel8布局的LayoutParams（复用同一个LayoutParams对象）
        channelLayoutParams = (AbsoluteLayout.LayoutParams) mRightBar_channel8.getLayoutParams();
        // 设置Channel8的新高度
        channelLayoutParams.height = height;
        // 应用新的布局参数到Channel8布局
        mRightBar_channel8.setLayoutParams(channelLayoutParams);
    }

    public static final int TOPSLIP = 1;
    public static final int RIGHTSLIP_MATH1 = TOPSLIP + 1;
    public static final int RIGHTSLIP_MATH2 = RIGHTSLIP_MATH1 + 1;
    public static final int RIGHTSLIP_MATH3 = RIGHTSLIP_MATH2 + 1;
    public static final int RIGHTSLIP_MATH4 = RIGHTSLIP_MATH3 + 1;
    public static final int RIGHTSLIP_MATH5 = RIGHTSLIP_MATH4 + 1;
    public static final int RIGHTSLIP_MATH6 = RIGHTSLIP_MATH5 + 1;
    public static final int RIGHTSLIP_MATH7 = RIGHTSLIP_MATH6 + 1;
    public static final int RIGHTSLIP_MATH8 = RIGHTSLIP_MATH7 + 1;
    public static final int RIGHTSLIP_REF1 = RIGHTSLIP_MATH8 + 1;
    public static final int RIGHTSLIP_REF2 = RIGHTSLIP_REF1 + 1;
    public static final int RIGHTSLIP_REF3 = RIGHTSLIP_REF2 + 1;
    public static final int RIGHTSLIP_REF4 = RIGHTSLIP_REF3 + 1;
    public static final int RIGHTSLIP_REF5 = RIGHTSLIP_REF4 + 1;
    public static final int RIGHTSLIP_REF6 = RIGHTSLIP_REF5 + 1;
    public static final int RIGHTSLIP_REF7 = RIGHTSLIP_REF6 + 1;
    public static final int RIGHTSLIP_REF8 = RIGHTSLIP_REF7 + 1;
    public static final int RIGHTSLIP_S1 = RIGHTSLIP_REF8 + 1;
    public static final int RIGHTSLIP_S2 = RIGHTSLIP_S1 + 1;
    public static final int RIGHTSLIP_S3 = RIGHTSLIP_S2 + 1;
    public static final int RIGHTSLIP_S4 = RIGHTSLIP_S3 + 1;
    public static final int RIGHTSLIP_CH1 = RIGHTSLIP_S4 + 1;
    public static final int RIGHTSLIP_CH2 = RIGHTSLIP_CH1 + 1;
    public static final int RIGHTSLIP_CH3 = RIGHTSLIP_CH2 + 1;
    public static final int RIGHTSLIP_CH4 = RIGHTSLIP_CH3 + 1;
    public static final int RIGHTSLIP_CH5 = RIGHTSLIP_CH4 + 1;
    public static final int RIGHTSLIP_CH6 = RIGHTSLIP_CH5 + 1;
    public static final int RIGHTSLIP_CH7 = RIGHTSLIP_CH6 + 1;
    public static final int RIGHTSLIP_CH8 = RIGHTSLIP_CH7 + 1;
    public static final int LEFTSLIP = RIGHTSLIP_CH8 + 1;
    public static final int BOTTOMSLIP = LEFTSLIP + 1;

    @IntDef({
            TOPSLIP,
            RIGHTSLIP_MATH1,
            RIGHTSLIP_MATH2,
            RIGHTSLIP_MATH3,
            RIGHTSLIP_MATH4,
            RIGHTSLIP_MATH5,
            RIGHTSLIP_MATH6,
            RIGHTSLIP_MATH7,
            RIGHTSLIP_MATH8,
            RIGHTSLIP_REF1,
            RIGHTSLIP_REF2,
            RIGHTSLIP_REF3,
            RIGHTSLIP_REF4,
            RIGHTSLIP_REF5,
            RIGHTSLIP_REF6,
            RIGHTSLIP_REF7,
            RIGHTSLIP_REF8,
            RIGHTSLIP_S1,
            RIGHTSLIP_S2,
            RIGHTSLIP_S3,
            RIGHTSLIP_S4,
            RIGHTSLIP_CH1,
            RIGHTSLIP_CH2,
            RIGHTSLIP_CH3,
            RIGHTSLIP_CH4,
            RIGHTSLIP_CH5,
            RIGHTSLIP_CH6,
            RIGHTSLIP_CH7,
            RIGHTSLIP_CH8,
            LEFTSLIP,
            BOTTOMSLIP
    })

    @Retention(RetentionPolicy.SOURCE)
    public @interface Slip {
    }

    /**
     * 打开指定的滑动菜单
     * 
     * @param slip 滑动菜单标识（使用@Slip注解）
     * @description 打开指定的滑动菜单，并隐藏其他所有菜单
     *              主要执行以下操作：
     *              1. 播放滑动音效
     *              2. 隐藏所有对话框（除了确认对话框）
     *              3. 根据菜单标识获取对应的滑动菜单区域对象
     *              4. 发送右侧菜单切换消息（如果是Math/Ref/Serials或Channel菜单）
     *              5. 隐藏其他所有滑动菜单
     *              6. 显示指定的滑动菜单
     * @调用时机 在需要打开特定菜单时调用，如外部按键触发、RxBus消息触发等
     * @关联方法 hideAllSlip - 隐藏所有滑动菜单
     * @关联方法 hideSlip - 隐藏指定的滑动菜单
     * @关联方法 showSliderZone - 显示滑动菜单区域
     * @关联类 SliderZone - 滑动菜单区域管理类
     * @重要逻辑 打开一个菜单时，会隐藏其他所有菜单（菜单互斥）
     */
    public void openSlip(@Slip int slip) {

        PlaySound.getInstance().playSlide(); // 播放滑动音效
        
        // 隐藏所有对话框（除了确认对话框）
        if (isDialogsShow()) {
            hideAllDialogsButDialogOkCancel(); // 隐藏所有对话框，但保留确认对话框
        }
        
        SliderZone sliderZone = null; // 滑动菜单区域对象
        
        // 根据菜单标识获取对应的滑动菜单区域对象
        switch (slip) {
            case TOPSLIP: // 顶部菜单
                sliderZone = sliderZone_top; // 获取顶部菜单区域
                break;
            case LEFTSLIP: // 左侧菜单
                sliderZone = sliderZone_left; // 获取左侧菜单区域
                break;
            case BOTTOMSLIP: // 底部菜单
                sliderZone = sliderZone_bottom; // 获取底部菜单区域
                break;
            case RIGHTSLIP_MATH1: // Math1菜单
                sliderZone = sliderZone_rightMath1; // 获取Math1菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_MATH2: // Math2菜单
                sliderZone = sliderZone_rightMath2; // 获取Math2菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_MATH3: // Math3菜单
                sliderZone = sliderZone_rightMath3; // 获取Math3菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_MATH4: // Math4菜单
                sliderZone = sliderZone_rightMath4; // 获取Math4菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_MATH5: // Math5菜单
                sliderZone = sliderZone_rightMath5; // 获取Math5菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_MATH6: // Math6菜单
                sliderZone = sliderZone_rightMath6; // 获取Math6菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_MATH7: // Math7菜单
                sliderZone = sliderZone_rightMath7; // 获取Math7菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_MATH8: // Math8菜单
                sliderZone = sliderZone_rightMath8; // 获取Math8菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_REF1: // Ref1菜单
                sliderZone = sliderZone_rightRef1; // 获取Ref1菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_REF2: // Ref2菜单
                sliderZone = sliderZone_rightRef2; // 获取Ref2菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_REF3: // Ref3菜单
                sliderZone = sliderZone_rightRef3; // 获取Ref3菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_REF4: // Ref4菜单
                sliderZone = sliderZone_rightRef4; // 获取Ref4菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_REF5: // Ref5菜单
                sliderZone = sliderZone_rightRef5; // 获取Ref5菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_REF6: // Ref6菜单
                sliderZone = sliderZone_rightRef6; // 获取Ref6菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_REF7: // Ref7菜单
                sliderZone = sliderZone_rightRef7; // 获取Ref7菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_REF8: // Ref8菜单
                sliderZone = sliderZone_rightRef8; // 获取Ref8菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_S1: // S1菜单
                sliderZone = sliderZone_rightS1; // 获取S1菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_S2: // S2菜单
                sliderZone = sliderZone_rightS2; // 获取S2菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_S3: // S3菜单
                sliderZone = sliderZone_rightS3; // 获取S3菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_S4: // S4菜单
                sliderZone = sliderZone_rightS4; // 获取S4菜单区域
                //mainRight切换为Other页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_CH1: // CH1菜单
                sliderZone = sliderZone_rightCh1; // 获取CH1菜单区域
                //mainRight切换为channel页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_CHANNELS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_CH2: // CH2菜单
                sliderZone = sliderZone_rightCh2; // 获取CH2菜单区域
                //mainRight切换为channel页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_CHANNELS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_CH3: // CH3菜单
                sliderZone = sliderZone_rightCh3; // 获取CH3菜单区域
                //mainRight切换为channel页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_CHANNELS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_CH4: // CH4菜单
                sliderZone = sliderZone_rightCh4; // 获取CH4菜单区域
                //mainRight切换为channel页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_CHANNELS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_CH5: // CH5菜单
                sliderZone = sliderZone_rightCh5; // 获取CH5菜单区域
                //mainRight切换为channel页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_CHANNELS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_CH6: // CH6菜单
                sliderZone = sliderZone_rightCh6; // 获取CH6菜单区域
                //mainRight切换为channel页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_CHANNELS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_CH7: // CH7菜单
                sliderZone = sliderZone_rightCh7; // 获取CH7菜单区域
                //mainRight切换为channel页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_CHANNELS); // 发送右侧菜单切换消息
                break;
            case RIGHTSLIP_CH8: // CH8菜单
                sliderZone = sliderZone_rightCh8; // 获取CH8菜单区域
                //mainRight切换为channel页面
                RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_CHANNELS); // 发送右侧菜单切换消息
                break;
        }



        // 隐藏其他所有滑动菜单（菜单互斥）
        for (int i = 0; i < sliderZoneList.size(); i++) {
            hideSliderZone(sliderZoneList.get(i), -1, -1); // 隐藏每个菜单
        }
        
        // 显示指定的滑动菜单
        if (sliderZone != null) {
            showSliderZone(sliderZone, -1, -1); // 显示菜单，参数-1表示无动画
        }
    }

    /**
     * 隐藏所有滑动菜单
     * 
     * @description 隐藏所有滑动菜单，并隐藏相关的对话框和文件选择对话框
     *              主要执行以下操作：
     *              1. 隐藏所有对话框（除了确认对话框）
     *              2. 隐藏所有滑动菜单
     *              3. 隐藏文件选择对话框
     * @调用时机 在需要隐藏所有菜单时调用，如屏幕锁定、切换模式等
     * @关联方法 hideSlip - 隐藏指定的滑动菜单
     * @关联方法 hideSliderZone - 隐藏滑动菜单区域
     * @关联类 SliderZone - 滑动菜单区域管理类
     * @重要逻辑 隐藏所有菜单时，会同时隐藏相关的对话框和文件选择对话框
     */
    public void hideAllSlip() {
        hideAllDialogsButDialogOkCancel(); // 隐藏所有对话框，但保留确认对话框
        
        // 隐藏所有滑动菜单
        for (int i = 0; i < sliderZoneList.size(); i++) {
            hideSliderZone(sliderZoneList.get(i), -1, -1); // 隐藏每个菜单
        }
        
        hideFileSelectDialog(); // 隐藏文件选择对话框

    }

    /**
     * 隐藏指定的滑动菜单
     * 
     * @param slip 滑动菜单标识（使用@Slip注解）
     * @description 隐藏指定的滑动菜单，并隐藏相关的对话框和文件选择对话框
     *              主要执行以下操作：
     *              1. 检查菜单是否正在显示（如果未显示则直接返回）
     *              2. 播放滑动音效
     *              3. 隐藏所有对话框（除了确认对话框）
     *              4. 根据菜单标识获取对应的滑动菜单区域对象
     *              5. 隐藏指定的滑动菜单
     *              6. 隐藏文件选择对话框
     * @调用时机 在需要隐藏特定菜单时调用，如外部按键触发、RxBus消息触发等
     * @关联方法 openSlip - 打开指定的滑动菜单
     * @关联方法 hideAllSlip - 隐藏所有滑动菜单
     * @关联方法 hideSliderZone - 隐藏滑动菜单区域
     * @关联类 SliderZone - 滑动菜单区域管理类
     * @重要逻辑 隐藏菜单前会检查菜单是否正在显示，避免重复操作
     */
    public void hideSlip(@Slip int slip) {
        // 检查菜单是否正在显示
        if (!isSlipShow(slip)) return; // 如果未显示则直接返回
        
        PlaySound.getInstance().playSlide(); // 播放滑动音效
        
        hideAllDialogsButDialogOkCancel(); // 隐藏所有对话框，但保留确认对话框
        
        SliderZone sliderZone = null; // 滑动菜单区域对象
        
        // 根据菜单标识获取对应的滑动菜单区域对象
        switch (slip) {
            case TOPSLIP: // 顶部菜单
                sliderZone = sliderZone_top; // 获取顶部菜单区域
                break;
            case LEFTSLIP: // 左侧菜单
                sliderZone = sliderZone_left; // 获取左侧菜单区域
                break;
            case BOTTOMSLIP: // 底部菜单
                sliderZone = sliderZone_bottom; // 获取底部菜单区域
                break;
            case RIGHTSLIP_MATH1: // Math1菜单
                sliderZone = sliderZone_rightMath1; // 获取Math1菜单区域
                break;
            case RIGHTSLIP_MATH2: // Math2菜单
                sliderZone = sliderZone_rightMath2; // 获取Math2菜单区域
                break;
            case RIGHTSLIP_MATH3: // Math3菜单
                sliderZone = sliderZone_rightMath3; // 获取Math3菜单区域
                break;
            case RIGHTSLIP_MATH4: // Math4菜单
                sliderZone = sliderZone_rightMath4; // 获取Math4菜单区域
                break;
            case RIGHTSLIP_MATH5: // Math5菜单
                sliderZone = sliderZone_rightMath5; // 获取Math5菜单区域
                break;
            case RIGHTSLIP_MATH6: // Math6菜单
                sliderZone = sliderZone_rightMath6; // 获取Math6菜单区域
                break;
            case RIGHTSLIP_MATH7: // Math7菜单
                sliderZone = sliderZone_rightMath7; // 获取Math7菜单区域
                break;
            case RIGHTSLIP_MATH8: // Math8菜单
                sliderZone = sliderZone_rightMath8; // 获取Math8菜单区域
                break;
            case RIGHTSLIP_REF1: // Ref1菜单
                sliderZone = sliderZone_rightRef1; // 获取Ref1菜单区域
                break;
            case RIGHTSLIP_REF2: // Ref2菜单
                sliderZone = sliderZone_rightRef2; // 获取Ref2菜单区域
                break;
            case RIGHTSLIP_REF3: // Ref3菜单
                sliderZone = sliderZone_rightRef3; // 获取Ref3菜单区域
                break;
            case RIGHTSLIP_REF4: // Ref4菜单
                sliderZone = sliderZone_rightRef4; // 获取Ref4菜单区域
                break;
            case RIGHTSLIP_REF5: // Ref5菜单
                sliderZone = sliderZone_rightRef5; // 获取Ref5菜单区域
                break;
            case RIGHTSLIP_REF6: // Ref6菜单
                sliderZone = sliderZone_rightRef6; // 获取Ref6菜单区域
                break;
            case RIGHTSLIP_REF7: // Ref7菜单
                sliderZone = sliderZone_rightRef7; // 获取Ref7菜单区域
                break;
            case RIGHTSLIP_REF8: // Ref8菜单
                sliderZone = sliderZone_rightRef8; // 获取Ref8菜单区域
                break;
            case RIGHTSLIP_S1: // S1菜单
                sliderZone = sliderZone_rightS1; // 获取S1菜单区域
                break;
            case RIGHTSLIP_S2: // S2菜单
                sliderZone = sliderZone_rightS2; // 获取S2菜单区域
                break;
            case RIGHTSLIP_S3: // S3菜单
                sliderZone = sliderZone_rightS3; // 获取S3菜单区域
                break;
            case RIGHTSLIP_S4: // S4菜单
                sliderZone = sliderZone_rightS4; // 获取S4菜单区域
                break;
            case RIGHTSLIP_CH1: // CH1菜单
                sliderZone = sliderZone_rightCh1; // 获取CH1菜单区域
                break;
            case RIGHTSLIP_CH2: // CH2菜单
                sliderZone = sliderZone_rightCh2; // 获取CH2菜单区域
                break;
            case RIGHTSLIP_CH3: // CH3菜单
                sliderZone = sliderZone_rightCh3; // 获取CH3菜单区域
                break;
            case RIGHTSLIP_CH4: // CH4菜单
                sliderZone = sliderZone_rightCh4; // 获取CH4菜单区域
                break;
            case RIGHTSLIP_CH5: // CH5菜单
                sliderZone = sliderZone_rightCh5; // 获取CH5菜单区域
                break;
            case RIGHTSLIP_CH6: // CH6菜单
                sliderZone = sliderZone_rightCh6; // 获取CH6菜单区域
                break;
            case RIGHTSLIP_CH7: // CH7菜单
                sliderZone = sliderZone_rightCh7; // 获取CH7菜单区域
                break;
            case RIGHTSLIP_CH8: // CH8菜单
                sliderZone = sliderZone_rightCh8; // 获取CH8菜单区域
                break;
        }
        
        // 隐藏指定的滑动菜单
        if (sliderZone != null) {
            hideSliderZone(sliderZone, -1, -1); // 隐藏菜单，参数-1表示无动画
        }
        
        hideFileSelectDialog(); // 隐藏文件选择对话框

    }

    /**
     * 右侧Channel菜单是否有划出
     */
    public boolean isRightSlipShow() {
        return isSlipShow(RIGHTSLIP_CH1) || isSlipShow(RIGHTSLIP_CH2)
                || isSlipShow(RIGHTSLIP_CH3) || isSlipShow(RIGHTSLIP_CH4)
                || isSlipShow(RIGHTSLIP_CH5) || isSlipShow(RIGHTSLIP_CH6)
                || isSlipShow(RIGHTSLIP_CH7) || isSlipShow(RIGHTSLIP_CH8)
                || isSlipShow(RIGHTSLIP_MATH1) || isSlipShow(RIGHTSLIP_MATH2)
                || isSlipShow(RIGHTSLIP_MATH3) || isSlipShow(RIGHTSLIP_MATH4)
                || isSlipShow(RIGHTSLIP_MATH5) || isSlipShow(RIGHTSLIP_MATH6)
                || isSlipShow(RIGHTSLIP_MATH7) || isSlipShow(RIGHTSLIP_MATH8)
                || isSlipShow(RIGHTSLIP_REF1) || isSlipShow(RIGHTSLIP_REF2)
                || isSlipShow(RIGHTSLIP_REF3) || isSlipShow(RIGHTSLIP_REF4)
                || isSlipShow(RIGHTSLIP_REF5) || isSlipShow(RIGHTSLIP_REF6)
                || isSlipShow(RIGHTSLIP_REF7) || isSlipShow(RIGHTSLIP_REF8)
                || isSlipShow(RIGHTSLIP_S1) || isSlipShow(RIGHTSLIP_S2)
                || isSlipShow(RIGHTSLIP_S3) || isSlipShow(RIGHTSLIP_S4);
    }

    /**
     * 右侧Math/Ref/Serials菜单是否滑出来
     */
    public boolean isRightSlipOtherShow() {
        return isSlipShow(RIGHTSLIP_MATH1) || isSlipShow(RIGHTSLIP_MATH2)
                || isSlipShow(RIGHTSLIP_MATH3) || isSlipShow(RIGHTSLIP_MATH4)
                || isSlipShow(RIGHTSLIP_MATH5) || isSlipShow(RIGHTSLIP_MATH6)
                || isSlipShow(RIGHTSLIP_MATH7) || isSlipShow(RIGHTSLIP_MATH8)
                || isSlipShow(RIGHTSLIP_REF1) || isSlipShow(RIGHTSLIP_REF2)
                || isSlipShow(RIGHTSLIP_REF3) || isSlipShow(RIGHTSLIP_REF4)
                || isSlipShow(RIGHTSLIP_REF5) || isSlipShow(RIGHTSLIP_REF6)
                || isSlipShow(RIGHTSLIP_REF7) || isSlipShow(RIGHTSLIP_REF8)
                || isSlipShow(RIGHTSLIP_S1) || isSlipShow(RIGHTSLIP_S2)
                || isSlipShow(RIGHTSLIP_S3) || isSlipShow(RIGHTSLIP_S4);
    }

    /**
     * 右侧模拟通道菜单是否划出
     */
    public boolean isRightSlipChannelShow() {
        return isSlipShow(RIGHTSLIP_CH1) || isSlipShow(RIGHTSLIP_CH2)
                || isSlipShow(RIGHTSLIP_CH3) || isSlipShow(RIGHTSLIP_CH4)
                || isSlipShow(RIGHTSLIP_CH5) || isSlipShow(RIGHTSLIP_CH6)
                || isSlipShow(RIGHTSLIP_CH7) || isSlipShow(RIGHTSLIP_CH8);
    }

    /**
     * 数学通道菜单是否划出
     */
    public boolean isRightSlipMathShow() {
        return isSlipShow(RIGHTSLIP_MATH1) || isSlipShow(RIGHTSLIP_MATH2)
                || isSlipShow(RIGHTSLIP_MATH3) || isSlipShow(RIGHTSLIP_MATH4)
                || isSlipShow(RIGHTSLIP_MATH5) || isSlipShow(RIGHTSLIP_MATH6)
                || isSlipShow(RIGHTSLIP_MATH7) || isSlipShow(RIGHTSLIP_MATH8);
    }

    /**
     * 检查所有滑动菜单是否有显示
     * 
     * @return boolean - true表示有菜单正在显示，false表示所有菜单都隐藏
     * @description 检查所有滑动菜单（顶部、底部、左侧、右侧）是否有任何一个正在显示
     * @调用时机 在需要判断是否有菜单显示时调用，如屏幕锁定判断、触摸事件处理等
     * @关联方法 isRightSlipShow - 检查右侧菜单是否显示
     * @关联方法 isSlipShow(int slip) - 检查指定菜单是否显示
     * @关联类 SliderZone - 滑动菜单区域管理类
     * @重要逻辑 只要有一个菜单显示，就返回true
     */
    public boolean isSlipShow() {
        return isRightSlipShow() || isSlipShow(TOPSLIP) || isSlipShow(BOTTOMSLIP); // 检查右侧、顶部、底部菜单
    }

    /**
     * 检查指定的滑动菜单是否显示
     * 
     * @param slip 滑动菜单标识（使用@Slip注解）
     * @return boolean - true表示指定菜单正在显示，false表示未显示
     * @description 根据菜单标识获取对应的滑动菜单区域对象，并检查其显示状态
     * @调用时机 在需要判断特定菜单是否显示时调用，如菜单操作判断、状态检查等
     * @关联方法 isSlipShow() - 检查所有菜单是否有显示
     * @关联类 SliderZone - 滑动菜单区域管理类
     * @重要逻辑 如果菜单对象为null，则返回false
     */
    public boolean isSlipShow(@Slip int slip) {
        SliderZone sliderZone = null; // 滑动菜单区域对象
        
        // 根据菜单标识获取对应的滑动菜单区域对象
        switch (slip) {
            case TOPSLIP: // 顶部菜单
                sliderZone = sliderZone_top; // 获取顶部菜单区域
                break;
            case LEFTSLIP: // 左侧菜单
                sliderZone = sliderZone_left; // 获取左侧菜单区域
                break;
            case BOTTOMSLIP: // 底部菜单
                sliderZone = sliderZone_bottom; // 获取底部菜单区域
                break;
            case RIGHTSLIP_MATH1: // Math1菜单
                sliderZone = sliderZone_rightMath1; // 获取Math1菜单区域
                break;
            case RIGHTSLIP_MATH2: // Math2菜单
                sliderZone = sliderZone_rightMath2; // 获取Math2菜单区域
                break;
            case RIGHTSLIP_MATH3: // Math3菜单
                sliderZone = sliderZone_rightMath3; // 获取Math3菜单区域
                break;
            case RIGHTSLIP_MATH4: // Math4菜单
                sliderZone = sliderZone_rightMath4; // 获取Math4菜单区域
                break;
            case RIGHTSLIP_MATH5: // Math5菜单
                sliderZone = sliderZone_rightMath5; // 获取Math5菜单区域
                break;
            case RIGHTSLIP_MATH6: // Math6菜单
                sliderZone = sliderZone_rightMath6; // 获取Math6菜单区域
                break;
            case RIGHTSLIP_MATH7: // Math7菜单
                sliderZone = sliderZone_rightMath7; // 获取Math7菜单区域
                break;
            case RIGHTSLIP_MATH8: // Math8菜单
                sliderZone = sliderZone_rightMath8; // 获取Math8菜单区域
                break;
            case RIGHTSLIP_REF1: // Ref1菜单
                sliderZone = sliderZone_rightRef1; // 获取Ref1菜单区域
                break;
            case RIGHTSLIP_REF2: // Ref2菜单
                sliderZone = sliderZone_rightRef2; // 获取Ref2菜单区域
                break;
            case RIGHTSLIP_REF3: // Ref3菜单
                sliderZone = sliderZone_rightRef3; // 获取Ref3菜单区域
                break;
            case RIGHTSLIP_REF4: // Ref4菜单
                sliderZone = sliderZone_rightRef4; // 获取Ref4菜单区域
                break;
            case RIGHTSLIP_REF5: // Ref5菜单
                sliderZone = sliderZone_rightRef5; // 获取Ref5菜单区域
                break;
            case RIGHTSLIP_REF6: // Ref6菜单
                sliderZone = sliderZone_rightRef6; // 获取Ref6菜单区域
                break;
            case RIGHTSLIP_REF7: // Ref7菜单
                sliderZone = sliderZone_rightRef7; // 获取Ref7菜单区域
                break;
            case RIGHTSLIP_REF8: // Ref8菜单
                sliderZone = sliderZone_rightRef8; // 获取Ref8菜单区域
                break;
            case RIGHTSLIP_S1: // S1菜单
                sliderZone = sliderZone_rightS1; // 获取S1菜单区域
                break;
            case RIGHTSLIP_S2: // S2菜单
                sliderZone = sliderZone_rightS2; // 获取S2菜单区域
                break;
            case RIGHTSLIP_S3: // S3菜单
                sliderZone = sliderZone_rightS3; // 获取S3菜单区域
                break;
            case RIGHTSLIP_S4: // S4菜单
                sliderZone = sliderZone_rightS4; // 获取S4菜单区域
                break;
            case RIGHTSLIP_CH1: // CH1菜单
                sliderZone = sliderZone_rightCh1; // 获取CH1菜单区域
                break;
            case RIGHTSLIP_CH2: // CH2菜单
                sliderZone = sliderZone_rightCh2; // 获取CH2菜单区域
                break;
            case RIGHTSLIP_CH3: // CH3菜单
                sliderZone = sliderZone_rightCh3; // 获取CH3菜单区域
                break;
            case RIGHTSLIP_CH4: // CH4菜单
                sliderZone = sliderZone_rightCh4; // 获取CH4菜单区域
                break;
            case RIGHTSLIP_CH5: // CH5菜单
                sliderZone = sliderZone_rightCh5; // 获取CH5菜单区域
                break;
            case RIGHTSLIP_CH6: // CH6菜单
                sliderZone = sliderZone_rightCh6; // 获取CH6菜单区域
                break;
            case RIGHTSLIP_CH7: // CH7菜单
                sliderZone = sliderZone_rightCh7; // 获取CH7菜单区域
                break;
            case RIGHTSLIP_CH8: // CH8菜单
                sliderZone = sliderZone_rightCh8; // 获取CH8菜单区域
                break;
        }
        
        // 检查菜单的显示状态
        if (sliderZone != null) {
            return sliderZone.isCurrShowState(); // 返回菜单的显示状态
        }
        return false; // 如果菜单对象为null，则返回false
    }

    /**
     * 判断指定滑动区域是否启用滑动功能
     * <p>
     * 功能描述：
     * - 根据滑动区域编号判断对应的滑动区域是否启用滑动功能
     * - 检查SliderZone的isEnableSlip状态
     * - 支持所有类型的滑动区域状态查询
     * </p>
     * <p>
     * 调用时机：
     * - 在需要判断滑动区域是否启用滑动功能时调用
     * - 在处理滑动事件前调用，判断是否允许滑动
     * - 在配置滑动区域参数时调用
     * </p>
     * <p>
     * 参数说明：
     * @param slip 滑动区域编号，使用@Slip注解限定取值范围
     *             - TOPSLIP: 顶部滑动区域
     *             - LEFTSLIP: 左侧滑动区域
     *             - BOTTOMSLIP: 底部滑动区域
     *             - RIGHTSLIP_MATH1~8: 右侧Math1~8滑动区域
     *             - RIGHTSLIP_REF1~8: 右侧Ref1~8滑动区域
     *             - RIGHTSLIP_S1~4: 右侧S1~4滑动区域
     *             - RIGHTSLIP_CH1~8: 右侧Ch1~8滑动区域
     * </p>
     * <p>
     * 返回值：
     * @return true: 滑动区域启用了滑动功能
     *         false: 滑动区域未启用滑动功能或滑动区域不存在
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 根据滑动区域编号使用switch语句获取对应的SliderZone实例
     * 2. 如果SliderZone实例不为null，返回其isEnableSlip状态
     * 3. 如果SliderZone实例为null，返回false
     * </p>
     * <p>
     * 相关方法：
     * - setEnableSlip(): 设置滑动功能启用状态
     * - dealSliderZone(): 处理滑动区域事件
     * </p>
     */
    public boolean isSlipEnable(@Slip int slip) {
        SliderZone sliderZone = null; // 初始化滑动区域实例为null
        switch (slip) { // 根据滑动区域编号获取对应的滑动区域实例
            case TOPSLIP: // 顶部滑动区域
                sliderZone = sliderZone_top; // 获取顶部滑动区域实例
                break;
            case LEFTSLIP: // 左侧滑动区域
                sliderZone = sliderZone_left; // 获取左侧滑动区域实例
                break;
            case BOTTOMSLIP: // 底部滑动区域
                sliderZone = sliderZone_bottom; // 获取底部滑动区域实例
                break;
            case RIGHTSLIP_MATH1: // 右侧Math1滑动区域
                sliderZone = sliderZone_rightMath1; // 获取Math1滑动区域实例
                break;
            case RIGHTSLIP_MATH2: // 右侧Math2滑动区域
                sliderZone = sliderZone_rightMath2; // 获取Math2滑动区域实例
                break;
            case RIGHTSLIP_MATH3: // 右侧Math3滑动区域
                sliderZone = sliderZone_rightMath3; // 获取Math3滑动区域实例
                break;
            case RIGHTSLIP_MATH4: // 右侧Math4滑动区域
                sliderZone = sliderZone_rightMath4; // 获取Math4滑动区域实例
                break;
            case RIGHTSLIP_MATH5: // 右侧Math5滑动区域
                sliderZone = sliderZone_rightMath5; // 获取Math5滑动区域实例
                break;
            case RIGHTSLIP_MATH6: // 右侧Math6滑动区域
                sliderZone = sliderZone_rightMath6; // 获取Math6滑动区域实例
                break;
            case RIGHTSLIP_MATH7: // 右侧Math7滑动区域
                sliderZone = sliderZone_rightMath7; // 获取Math7滑动区域实例
                break;
            case RIGHTSLIP_MATH8: // 右侧Math8滑动区域
                sliderZone = sliderZone_rightMath8; // 获取Math8滑动区域实例
                break;
            case RIGHTSLIP_REF1: // 右侧Ref1滑动区域
                sliderZone = sliderZone_rightRef1; // 获取Ref1滑动区域实例
                break;
            case RIGHTSLIP_REF2: // 右侧Ref2滑动区域
                sliderZone = sliderZone_rightRef2; // 获取Ref2滑动区域实例
                break;
            case RIGHTSLIP_REF3: // 右侧Ref3滑动区域
                sliderZone = sliderZone_rightRef3; // 获取Ref3滑动区域实例
                break;
            case RIGHTSLIP_REF4: // 右侧Ref4滑动区域
                sliderZone = sliderZone_rightRef4; // 获取Ref4滑动区域实例
                break;
            case RIGHTSLIP_REF5: // 右侧Ref5滑动区域
                sliderZone = sliderZone_rightRef5; // 获取Ref5滑动区域实例
                break;
            case RIGHTSLIP_REF6: // 右侧Ref6滑动区域
                sliderZone = sliderZone_rightRef6; // 获取Ref6滑动区域实例
                break;
            case RIGHTSLIP_REF7: // 右侧Ref7滑动区域
                sliderZone = sliderZone_rightRef7; // 获取Ref7滑动区域实例
                break;
            case RIGHTSLIP_REF8: // 右侧Ref8滑动区域
                sliderZone = sliderZone_rightRef8; // 获取Ref8滑动区域实例
                break;
            case RIGHTSLIP_S1: // 右侧S1滑动区域
                sliderZone = sliderZone_rightS1; // 获取S1滑动区域实例
                break;
            case RIGHTSLIP_S2: // 右侧S2滑动区域
                sliderZone = sliderZone_rightS2; // 获取S2滑动区域实例
                break;
            case RIGHTSLIP_S3: // 右侧S3滑动区域
                sliderZone = sliderZone_rightS3; // 获取S3滑动区域实例
                break;
            case RIGHTSLIP_S4: // 右侧S4滑动区域
                sliderZone = sliderZone_rightS4; // 获取S4滑动区域实例
                break;
            case RIGHTSLIP_CH1: // 右侧Ch1滑动区域
                sliderZone = sliderZone_rightCh1; // 获取Ch1滑动区域实例
                break;
            case RIGHTSLIP_CH2: // 右侧Ch2滑动区域
                sliderZone = sliderZone_rightCh2; // 获取Ch2滑动区域实例
                break;
            case RIGHTSLIP_CH3: // 右侧Ch3滑动区域
                sliderZone = sliderZone_rightCh3; // 获取Ch3滑动区域实例
                break;
            case RIGHTSLIP_CH4: // 右侧Ch4滑动区域
                sliderZone = sliderZone_rightCh4; // 获取Ch4滑动区域实例
                break;
            case RIGHTSLIP_CH5: // 右侧Ch5滑动区域
                sliderZone = sliderZone_rightCh5; // 获取Ch5滑动区域实例
                break;
            case RIGHTSLIP_CH6: // 右侧Ch6滑动区域
                sliderZone = sliderZone_rightCh6; // 获取Ch6滑动区域实例
                break;
            case RIGHTSLIP_CH7: // 右侧Ch7滑动区域
                sliderZone = sliderZone_rightCh7; // 获取Ch7滑动区域实例
                break;
            case RIGHTSLIP_CH8: // 右侧Ch8滑动区域
                sliderZone = sliderZone_rightCh8; // 获取Ch8滑动区域实例
                break;
        }
        // 如果滑动区域实例不为null，返回其滑动功能启用状态
        if (sliderZone != null) {
            return sliderZone.isEnableSlip(); // 返回滑动区域的滑动功能启用状态
        }
        return false; // 如果滑动区域实例为null，返回false
    }

    /**
     * 对话框
     * dialogMeasureDelay
     * dialogMeasurePhase
     * dialogTopCount
     * dialogTopScale
     * dialogTextKeyBoard
     * dialogNumberKeyBoard
     * dialogFloatKeyBoard
     * dialogFormulaKeyBoard
     * dialogNumberPicker
     * dialogBaudRate
     * dialogBandWidth
     * dialogProbeMultiple
     * dialogRefRecall
     * dialogAfterGlow
     * dialogFftAfterGlow
     * dialogCenterTimeBase
     * dialogOkCancel
     * dialogOk
     * dialogMenuHalf
     * dialogChannelLabel
     */
    public static final int DIALOG_MEASUREDELAY = 1;
    public static final int DIALOG_MEASUREPHASE = 2;
    public static final int DIALOG_TOPCOUNT = 3;
    public static final int DIALOG_TOPSCALE = 4;
    public static final int DIALOG_TEXTKEYBOARD = 5;
    public static final int DIALOG_NUMBERKEYBOARD = 6;
    public static final int DIALOG_FLOATKEYBOARD = 7;
    public static final int DIALOG_FORMULAKEYBOARD = 8;
    public static final int DIALOG_NUMBERPICKER = 9;
    public static final int DIALOG_BAUDRATE = 10;
    public static final int DIALOG_BANDWIDTH = 11;
    public static final int DIALOG_PROBEMULTIPLE = 12;
    public static final int DIALOG_REFRECALL = 13;
    public static final int DIALOG_AFTERGLOW = 14;
    public static final int DIALOG_CENTERTIMEBASE = 15;
    public static final int DIALOG_OKCANCEL = 16;
    public static final int DIALOG_OK = 17;
    public static final int DIALOG_MENUHALF = 18;
    public static final int DIALOG_CHANNELLABEL = 19;
    public static final int DIALOG_BANDWIDTHHZ = 20;
    public static final int DIALOG_MEASURE_STATICS=21;
    public static final int DIALOG_MATH_FFT_PERSIST=22;
    public static final int DIALOG_PROBE_INTERFACE=23;
    public static final int DIALOG_FFT_AFTERGLOW = 24;
    public static final int DIALOG_LOAF_REF_CSV = 25;
    public static final int DIALOG_SELECT_COLOR = 26;
    public static final int DIALOG_SET_CHANNEL_INFO = 27;
    public static final int DIALOG_FULL_FLOAT_KEYBOARD = 28;
    public static final int DIALOG_MEASURE_TVALUE = 29;

    @IntDef({
            DIALOG_MEASUREDELAY,
            DIALOG_MEASUREPHASE,
            DIALOG_TOPCOUNT,
            DIALOG_TOPSCALE,
            DIALOG_TEXTKEYBOARD,
            DIALOG_NUMBERKEYBOARD,
            DIALOG_FLOATKEYBOARD,
            DIALOG_FORMULAKEYBOARD,
            DIALOG_NUMBERPICKER,
            DIALOG_BAUDRATE,
            DIALOG_BANDWIDTH,
            DIALOG_PROBEMULTIPLE,
            DIALOG_REFRECALL,
            DIALOG_AFTERGLOW,
            DIALOG_CENTERTIMEBASE,
            DIALOG_OKCANCEL,
            DIALOG_OK,
            DIALOG_MENUHALF,
            DIALOG_CHANNELLABEL,
            DIALOG_BANDWIDTHHZ,
            DIALOG_MEASURE_STATICS,
            DIALOG_MATH_FFT_PERSIST,
            DIALOG_PROBE_INTERFACE,
            DIALOG_FFT_AFTERGLOW,
            DIALOG_LOAF_REF_CSV,
            DIALOG_SELECT_COLOR,
            DIALOG_SET_CHANNEL_INFO,
            DIALOG_FULL_FLOAT_KEYBOARD,
            DIALOG_MEASURE_TVALUE
    })

    @Retention(RetentionPolicy.SOURCE)
    public @interface Dialog {
    }

    /**
     * 判断指定对话框是否正在显示
     * <p>
     * 功能描述：
     * - 根据对话框编号判断对应的对话框是否正在显示
     * - 检查对话框的Visibility状态是否为VISIBLE
     * - 支持所有类型的对话框状态查询
     * </p>
     * <p>
     * 调用时机：
     * - 在需要判断对话框显示状态时调用
     * - 在hideDialog()方法中调用，判断是否需要隐藏
     * - 在其他需要检查对话框状态的场景中调用
     * </p>
     * <p>
     * 参数说明：
     * @param dialog 对话框编号，使用@Dialog注解限定取值范围
     *               - DIALOG_MEASUREDELAY: 测量延迟对话框
     *               - DIALOG_MEASUREPHASE: 测量相位对话框
     *               - DIALOG_TOPCOUNT: 顶部计数对话框
     *               - DIALOG_TOPSCALE: 顶部刻度对话框
     *               - DIALOG_TEXTKEYBOARD: 文本键盘对话框
     *               - DIALOG_NUMBERKEYBOARD: 数字键盘对话框
     *               - DIALOG_FLOATKEYBOARD: 浮点数键盘对话框
     *               - DIALOG_FORMULAKEYBOARD: 公式键盘对话框
     *               - DIALOG_NUMBERPICKER: 数字选择器对话框
     *               - DIALOG_BAUDRATE: 波特率对话框
     *               - DIALOG_BANDWIDTH: 带宽对话框
     *               - DIALOG_PROBEMULTIPLE: 探头倍数对话框
     *               - DIALOG_REFRECALL: 参考召回对话框
     *               - DIALOG_AFTERGLOW: 余辉对话框
     *               - DIALOG_FFT_AFTERGLOW: FFT余辉对话框
     *               - DIALOG_CENTERTIMEBASE: 中心时基对话框
     *               - DIALOG_OKCANCEL: 确认取消对话框
     *               - DIALOG_OK: 确认对话框
     *               - DIALOG_MENUHALF: 半屏菜单对话框
     *               - DIALOG_CHANNELLABEL: 通道标签对话框
     *               - DIALOG_BANDWIDTHHZ: 带宽Hz对话框
     *               - DIALOG_MATH_FFT_PERSIST: Math FFT持久化对话框
     *               - DIALOG_PROBE_INTERFACE: 探头接口对话框
     *               - DIALOG_LOAF_REF_CSV: 加载参考CSV对话框
     *               - DIALOG_SELECT_COLOR: 选择颜色对话框
     *               - DIALOG_SET_CHANNEL_INFO: 设置通道信息对话框
     *               - DIALOG_FULL_FLOAT_KEYBOARD: 全浮点数键盘对话框
     *               - DIALOG_MEASURE_TVALUE: 测量T值对话框
     * </p>
     * <p>
     * 返回值：
     * @return true: 对话框正在显示（Visibility为VISIBLE）
     *         false: 对话框未显示（Visibility不为VISIBLE）
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 根据对话框编号使用switch语句判断
     * 2. 检查对应对话框视图的Visibility状态
     * 3. 如果Visibility为VISIBLE，返回true
     * 4. 如果Visibility不为VISIBLE，返回false
     * </p>
     * <p>
     * 相关方法：
     * - getDialog(): 获取对话框视图对象
     * - hideDialog(): 隐藏指定对话框
     * </p>
     */
    public boolean isDialogShow(@Dialog int dialog) {

        switch (dialog) { // 根据对话框编号判断对应的对话框是否显示
            case DIALOG_MEASUREDELAY: // 测量延迟对话框
                return dialogMeasureDelay.getVisibility() == View.VISIBLE; // 判断测量延迟对话框是否可见
            case DIALOG_MEASUREPHASE: // 测量相位对话框
                return dialogMeasurePhase.getVisibility() == View.VISIBLE; // 判断测量相位对话框是否可见
            case DIALOG_MEASURE_TVALUE: // 测量T值对话框
                return dialogMeasureTValue.getVisibility() == View.VISIBLE; // 判断测量T值对话框是否可见
            case DIALOG_TOPCOUNT: // 顶部计数对话框
                return dialogTopCount.getVisibility() == View.VISIBLE; // 判断顶部计数对话框是否可见
            case DIALOG_TOPSCALE: // 顶部刻度对话框
                return dialogTopScale.getVisibility() == View.VISIBLE; // 判断顶部刻度对话框是否可见
            case DIALOG_TEXTKEYBOARD: // 文本键盘对话框
                return dialogTextKeyBoard.getVisibility() == View.VISIBLE; // 判断文本键盘对话框是否可见
            case DIALOG_NUMBERKEYBOARD: // 数字键盘对话框
                return dialogNumberKeyBoard.getVisibility() == View.VISIBLE; // 判断数字键盘对话框是否可见
            case DIALOG_FLOATKEYBOARD: // 浮点数键盘对话框
                return dialogFloatKeyBoard.getVisibility() == View.VISIBLE; // 判断浮点数键盘对话框是否可见
            case DIALOG_FORMULAKEYBOARD: // 公式键盘对话框
                return dialogFormulaKeyBoard.getVisibility() == View.VISIBLE; // 判断公式键盘对话框是否可见
            case DIALOG_NUMBERPICKER: // 数字选择器对话框
                return dialogNumberPicker.getVisibility() == View.VISIBLE; // 判断数字选择器对话框是否可见
            case DIALOG_BAUDRATE: // 波特率对话框
                return dialogBaudRate.getVisibility() == View.VISIBLE; // 判断波特率对话框是否可见
            case DIALOG_BANDWIDTH: // 带宽对话框
                return dialogBandWidth.getVisibility() == View.VISIBLE; // 判断带宽对话框是否可见
            case DIALOG_PROBEMULTIPLE: // 探头倍数对话框
                return dialogProbeMultiple.getVisibility() == View.VISIBLE; // 判断探头倍数对话框是否可见
            case DIALOG_REFRECALL: // 参考召回对话框
                return dialogRefRecall.getVisibility() == View.VISIBLE; // 判断参考召回对话框是否可见
            case DIALOG_AFTERGLOW: // 余辉对话框
                return dialogAfterGlow.getVisibility() == View.VISIBLE; // 判断余辉对话框是否可见
            case DIALOG_FFT_AFTERGLOW: // FFT余辉对话框
                return dialogFftAfterGlow.getVisibility() == View.VISIBLE; // 判断FFT余辉对话框是否可见
            case DIALOG_CENTERTIMEBASE: // 中心时基对话框
                return dialogCenterTimeBase.getVisibility() == View.VISIBLE; // 判断中心时基对话框是否可见
            case DIALOG_OKCANCEL: // 确认取消对话框
                return dialogOkCancel.getVisibility() == VISIBLE; // 判断确认取消对话框是否可见
            case DIALOG_OK: // 确认对话框
                return dialogOk.getVisibility() == VISIBLE; // 判断确认对话框是否可见
            case DIALOG_MENUHALF: // 半屏菜单对话框
                return dialogMenuHalf.getVisibility() == VISIBLE; // 判断半屏菜单对话框是否可见
            case DIALOG_CHANNELLABEL: // 通道标签对话框
                return dialogChannelLabel.getVisibility() == VISIBLE; // 判断通道标签对话框是否可见
            case DIALOG_BANDWIDTHHZ: // 带宽Hz对话框
                return dialogBandWidthHz.getVisibility() == View.VISIBLE; // 判断带宽Hz对话框是否可见
            case DIALOG_MATH_FFT_PERSIST: // Math FFT持久化对话框
                return dialogMathFFTPersist.getVisibility()==View.VISIBLE; // 判断Math FFT持久化对话框是否可见
            case DIALOG_PROBE_INTERFACE: // 探头接口对话框
                return dialogProbeInterface.getVisibility() == View.VISIBLE; // 判断探头接口对话框是否可见
            case DIALOG_LOAF_REF_CSV: // 加载参考CSV对话框
                return dialogLoadRefCsvWave.getVisibility() == View.VISIBLE; // 判断加载参考CSV对话框是否可见
            case DIALOG_SELECT_COLOR: // 选择颜色对话框
                return dialogSelectColor.getVisibility() == View.VISIBLE; // 判断选择颜色对话框是否可见
            case DIALOG_SET_CHANNEL_INFO: // 设置通道信息对话框
                return dialogSetChannelInfo.getVisibility() == View.VISIBLE; // 判断设置通道信息对话框是否可见
            case DIALOG_FULL_FLOAT_KEYBOARD: // 全浮点数键盘对话框
                return dialogFullFloatKeyBoard.getVisibility() == View.VISIBLE; // 判断全浮点数键盘对话框是否可见
        }
        return false; // 如果对话框编号不在switch范围内，返回false
    }

    /**
     * 获取指定对话框的视图对象
     * <p>
     * 功能描述：
     * - 根据对话框编号获取对应的对话框视图对象
     * - 返回ViewGroup类型的对话框视图
     * - 支持所有类型的对话框视图获取
     * </p>
     * <p>
     * 调用时机：
     * - 在需要操作对话框视图时调用
     * - 在需要对对话框进行自定义操作时调用
     * </p>
     * <p>
     * 参数说明：
     * @param dialog 对话框编号，使用@Dialog注解限定取值范围
     *               - 取值范围同isDialogShow()方法
     * </p>
     * <p>
     * 返回值：
     * @return ViewGroup: 对话框视图对象
     *         null: 如果对话框编号不在范围内，返回null
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 根据对话框编号使用switch语句判断
     * 2. 返回对应的对话框视图对象
     * 3. 如果对话框编号不在范围内，返回null
     * </p>
     * <p>
     * 相关方法：
     * - isDialogShow(): 判断对话框是否显示
     * - hideDialog(): 隐藏指定对话框
     * </p>
     */
    public ViewGroup getDialog(@Dialog int dialog) {
        switch (dialog) { // 根据对话框编号返回对应的对话框视图对象
            case DIALOG_MEASUREDELAY: // 测量延迟对话框
                return dialogMeasureDelay; // 返回测量延迟对话框视图
            case DIALOG_MEASUREPHASE: // 测量相位对话框
                return dialogMeasurePhase; // 返回测量相位对话框视图
            case DIALOG_MEASURE_TVALUE: // 测量T值对话框
                return dialogMeasureTValue; // 返回测量T值对话框视图
            case DIALOG_TOPCOUNT: // 顶部计数对话框
                return dialogTopCount; // 返回顶部计数对话框视图
            case DIALOG_TOPSCALE: // 顶部刻度对话框
                return dialogTopScale; // 返回顶部刻度对话框视图
            case DIALOG_TEXTKEYBOARD: // 文本键盘对话框
                return dialogTextKeyBoard; // 返回文本键盘对话框视图
            case DIALOG_NUMBERKEYBOARD: // 数字键盘对话框
                return dialogNumberKeyBoard; // 返回数字键盘对话框视图
            case DIALOG_FLOATKEYBOARD: // 浮点数键盘对话框
                return dialogFloatKeyBoard; // 返回浮点数键盘对话框视图
            case DIALOG_FORMULAKEYBOARD: // 公式键盘对话框
                return dialogFormulaKeyBoard; // 返回公式键盘对话框视图
            case DIALOG_NUMBERPICKER: // 数字选择器对话框
                return dialogNumberPicker; // 返回数字选择器对话框视图
            case DIALOG_BAUDRATE: // 波特率对话框
                return dialogBaudRate; // 返回波特率对话框视图
            case DIALOG_BANDWIDTH: // 带宽对话框
                return dialogBandWidth; // 返回带宽对话框视图
            case DIALOG_PROBEMULTIPLE: // 探头倍数对话框
                return dialogProbeMultiple; // 返回探头倍数对话框视图
            case DIALOG_REFRECALL: // 参考召回对话框
                return dialogRefRecall; // 返回参考召回对话框视图
            case DIALOG_AFTERGLOW: // 余辉对话框
                return dialogAfterGlow; // 返回余辉对话框视图
            case DIALOG_FFT_AFTERGLOW: // FFT余辉对话框
                return dialogFftAfterGlow; // 返回FFT余辉对话框视图
            case DIALOG_CENTERTIMEBASE: // 中心时基对话框
                return dialogCenterTimeBase; // 返回中心时基对话框视图
            case DIALOG_OKCANCEL: // 确认取消对话框
                return dialogOkCancel; // 返回确认取消对话框视图
            case DIALOG_OK: // 确认对话框
                return dialogOk; // 返回确认对话框视图
            case DIALOG_MENUHALF: // 半屏菜单对话框
                return dialogMenuHalf; // 返回半屏菜单对话框视图
            case DIALOG_CHANNELLABEL: // 通道标签对话框
                return dialogChannelLabel; // 返回通道标签对话框视图
            case DIALOG_BANDWIDTHHZ: // 带宽Hz对话框
                return dialogBandWidthHz; // 返回带宽Hz对话框视图
            case DIALOG_MATH_FFT_PERSIST: // Math FFT持久化对话框
                return dialogMathFFTPersist; // 返回Math FFT持久化对话框视图
            case DIALOG_PROBE_INTERFACE: // 探头接口对话框
                return dialogProbeInterface; // 返回探头接口对话框视图
            case DIALOG_LOAF_REF_CSV: // 加载参考CSV对话框
                return dialogLoadRefCsvWave; // 返回加载参考CSV对话框视图
            case DIALOG_SELECT_COLOR: // 选择颜色对话框
                return dialogSelectColor; // 返回选择颜色对话框视图
            case DIALOG_SET_CHANNEL_INFO: // 设置通道信息对话框
                return dialogSetChannelInfo; // 返回设置通道信息对话框视图
            case DIALOG_FULL_FLOAT_KEYBOARD: // 全浮点数键盘对话框
                return dialogFullFloatKeyBoard; // 返回全浮点数键盘对话框视图
        }
        return null; // 如果对话框编号不在switch范围内，返回null
    }

    /**
     * 隐藏指定对话框
     * <p>
     * 功能描述：
     * - 根据对话框编号隐藏对应的对话框
     * - 先检查对话框是否正在显示（使用isDialogShow方法）
     * - 如果对话框正在显示，调用其hide()方法隐藏
     * - 支持所有类型的对话框隐藏操作
     * </p>
     * <p>
     * 调用时机：
     * - 在需要关闭指定对话框时调用
     * - 在hideAllDialogsButDialogOkCancel()方法中被调用
     * - 在切换菜单或功能时调用，隐藏不需要的对话框
     * - 在用户点击对话框外部区域时调用
     * </p>
     * <p>
     * 参数说明：
     * @param dialog 对话框编号，使用@Dialog注解限定取值范围
     *               - DIALOG_MEASUREDELAY: 测量延迟对话框
     *               - DIALOG_MEASUREPHASE: 测量相位对话框
     *               - DIALOG_MEASURE_TVALUE: 测量T值对话框
     *               - DIALOG_TOPCOUNT: 顶部计数对话框
     *               - DIALOG_TOPSCALE: 顶部刻度对话框
     *               - DIALOG_TEXTKEYBOARD: 文本键盘对话框
     *               - DIALOG_NUMBERKEYBOARD: 数字键盘对话框
     *               - DIALOG_FLOATKEYBOARD: 浮点数键盘对话框
     *               - DIALOG_FORMULAKEYBOARD: 公式键盘对话框
     *               - DIALOG_NUMBERPICKER: 数字选择器对话框
     *               - DIALOG_BAUDRATE: 波特率对话框
     *               - DIALOG_BANDWIDTH: 带宽对话框
     *               - DIALOG_PROBEMULTIPLE: 探头倍数对话框
     *               - DIALOG_REFRECALL: 参考召回对话框
     *               - DIALOG_AFTERGLOW: 余辉对话框
     *               - DIALOG_FFT_AFTERGLOW: FFT余辉对话框
     *               - DIALOG_CENTERTIMEBASE: 中心时基对话框
     *               - DIALOG_OKCANCEL: 确认取消对话框
     *               - DIALOG_OK: 确认对话框
     *               - DIALOG_MENUHALF: 半屏菜单对话框
     *               - DIALOG_CHANNELLABEL: 通道标签对话框
     *               - DIALOG_BANDWIDTHHZ: 带宽Hz对话框
     *               - DIALOG_MATH_FFT_PERSIST: Math FFT持久化对话框
     *               - DIALOG_PROBE_INTERFACE: 探头接口对话框
     *               - DIALOG_LOAF_REF_CSV: 加载参考CSV对话框
     *               - DIALOG_SELECT_COLOR: 选择颜色对话框
     *               - DIALOG_SET_CHANNEL_INFO: 设置通道信息对话框
     *               - DIALOG_FULL_FLOAT_KEYBOARD: 全浮点数键盘对话框
     * </p>
     * <p>
     * 返回值：
     * @return true: 对话框正在显示并成功关闭
     *         false: 对话框未显示或关闭失败
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 根据对话框编号使用switch语句判断
     * 2. 使用isDialogShow()检查对话框是否正在显示
     * 3. 如果正在显示，调用对话框的hide()方法隐藏
     * 4. 返回true表示成功关闭，返回false表示未关闭
     * </p>
     * <p>
     * 相关方法：
     * - isDialogShow(): 判断对话框是否显示
     * - getDialog(): 获取对话框视图对象
     * - hideAllDialogsButDialogOkCancel(): 隐藏所有对话框（除了确认对话框）
     * </p>
     */
    public boolean hideDialog(@Dialog int dialog) {
        switch (dialog) { // 根据对话框编号隐藏对应的对话框
            case DIALOG_MEASUREDELAY: // 测量延迟对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogMeasureDelay.hide(); // 隐藏测量延迟对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_MEASUREPHASE: // 测量相位对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogMeasurePhase.hide(); // 隐藏测量相位对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_MEASURE_TVALUE: // 测量T值对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogMeasureTValue.hide(); // 隐藏测量T值对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_TOPCOUNT: // 顶部计数对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogTopCount.hide(); // 隐藏顶部计数对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_TOPSCALE: // 顶部刻度对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogTopScale.hide(); // 隐藏顶部刻度对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_TEXTKEYBOARD: // 文本键盘对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogTextKeyBoard.hide(); // 隐藏文本键盘对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_NUMBERKEYBOARD: // 数字键盘对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogNumberKeyBoard.hide(); // 隐藏数字键盘对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_FLOATKEYBOARD: // 浮点数键盘对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogFloatKeyBoard.hide(); // 隐藏浮点数键盘对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_FORMULAKEYBOARD: // 公式键盘对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogFormulaKeyBoard.hide(); // 隐藏公式键盘对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_NUMBERPICKER: // 数字选择器对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogNumberPicker.hide(); // 隐藏数字选择器对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_BAUDRATE: // 波特率对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogBaudRate.hide(); // 隐藏波特率对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_BANDWIDTH: // 带宽对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogBandWidth.hide(); // 隐藏带宽对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_PROBEMULTIPLE: // 探头倍数对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogProbeMultiple.hide(); // 隐藏探头倍数对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_REFRECALL: // 参考召回对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogRefRecall.hide(); // 隐藏参考召回对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_AFTERGLOW: // 余辉对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogAfterGlow.hide(); // 隐藏余辉对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_FFT_AFTERGLOW: // FFT余辉对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogFftAfterGlow.hide(); // 隐藏FFT余辉对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_CENTERTIMEBASE: // 中心时基对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogCenterTimeBase.hide(); // 隐藏中心时基对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_OKCANCEL: // 确认取消对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogOkCancel.hide(); // 隐藏确认取消对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_OK: // 确认对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogOk.hide(); // 隐藏确认对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_MENUHALF: // 半屏菜单对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogMenuHalf.hide(); // 隐藏半屏菜单对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_CHANNELLABEL: // 通道标签对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogChannelLabel.hide(); // 隐藏通道标签对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_BANDWIDTHHZ: // 带宽Hz对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogBandWidthHz.hide(); // 隐藏带宽Hz对话框
                    return true; // 返回true表示成功关闭
                }
                break;
            case DIALOG_MATH_FFT_PERSIST: // Math FFT持久化对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogMathFFTPersist.hide(); // 隐藏Math FFT持久化对话框
                    return true; // 返回true表示成功关闭
                }
            case DIALOG_PROBE_INTERFACE: // 探头接口对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogProbeInterface.hide(); // 隐藏探头接口对话框
                    return true; // 返回true表示成功关闭
                }
            case DIALOG_LOAF_REF_CSV: // 加载参考CSV对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogLoadRefCsvWave.hide(); // 隐藏加载参考CSV对话框
                    return true; // 返回true表示成功关闭
                }
            case DIALOG_SELECT_COLOR: // 选择颜色对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogSelectColor.hide(); // 隐藏选择颜色对话框
                    return true; // 返回true表示成功关闭
                }
            case DIALOG_SET_CHANNEL_INFO: // 设置通道信息对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogSetChannelInfo.hide(); // 隐藏设置通道信息对话框
                    return true; // 返回true表示成功关闭
                }
            case DIALOG_FULL_FLOAT_KEYBOARD: // 全浮点数键盘对话框
                if (isDialogShow(dialog)) { // 检查对话框是否正在显示
                    dialogFullFloatKeyBoard.hide(); // 隐藏全浮点数键盘对话框
                    return true; // 返回true表示成功关闭
                }
        }
        return false; // 如果对话框未显示或关闭失败，返回false
    }

    /**
     * 判断确认对话框是否可见
     * <p>
     * 功能描述：
     * - 判断确认取消对话框（DIALOG_OKCANCEL）或确认对话框（DIALOG_OK）是否正在显示
     * - 用于检查重要的确认提示对话框是否显示
     * - 防止在确认对话框显示时执行其他操作
     * </p>
     * <p>
     * 调用时机：
     * - 在需要判断确认对话框显示状态时调用
     * - 在hideAllDialogsButDialogOkCancel()方法中被调用（注释掉的代码）
     * - 在处理用户操作前调用，检查是否有确认对话框显示
     * - 在切换功能或菜单时调用，判断是否需要等待用户确认
     * </p>
     * <p>
     * 返回值：
     * @return true: 确认取消对话框或确认对话框正在显示
     *         false: 确认对话框未显示
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 使用isDialogShow(DIALOG_OKCANCEL)检查确认取消对话框是否显示
     * 2. 使用isDialogShow(DIALOG_OK)检查确认对话框是否显示
     * 3. 使用逻辑或运算（||），只要有一个显示就返回true
     * </p>
     * <p>
     * 相关方法：
     * - isDialogShow(): 判断对话框是否显示
     * - hideAllDialogsButDialogOkCancel(): 隐藏所有对话框（除了确认对话框）
     * </p>
     */
    public boolean isDialogOkVisible() {
        return isDialogShow(DIALOG_OKCANCEL) || isDialogShow(DIALOG_OK); // 判断确认取消对话框或确认对话框是否显示
    }

    /**
     * 隐藏所有对话框（除了确认对话框）
     * <p>
     * 功能描述：
     * - 批量隐藏所有对话框，但保留确认取消对话框（DIALOG_OKCANCEL）和确认对话框（DIALOG_OK）
     * - 用于清理界面，隐藏不需要的对话框
     * - 返回是否有对话框被隐藏的状态
     * </p>
     * <p>
     * 调用时机：
     * - 在切换功能或菜单时调用，清理其他对话框
     * - 在hideAllDialogSlipButLevelMenu()方法中被调用
     * - 在hideAllDialogSlip()方法中被调用
     * - 在用户点击外部区域时调用
     * - 在需要清理界面时调用
     * </p>
     * <p>
     * 返回值：
     * @return true: 有至少一个对话框被成功隐藏
     *         false: 所有对话框都未显示或未成功隐藏
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 调用hideDialog()方法隐藏每个对话框
     * 2. 使用boolean变量记录每个对话框的隐藏状态（b1~b25）
     * 3. 使用逻辑或运算（||）组合所有隐藏状态
     * 4. 只要有一个对话框被隐藏，就返回true
     * </p>
     * <p>
     * 隐藏的对话框列表：
     * - DIALOG_MEASUREDELAY: 测量延迟对话框
     * - DIALOG_MEASUREPHASE: 测量相位对话框
     * - DIALOG_TOPCOUNT: 顶部计数对话框
     * - DIALOG_TOPSCALE: 顶部刻度对话框
     * - DIALOG_TEXTKEYBOARD: 文本键盘对话框
     * - DIALOG_NUMBERKEYBOARD: 数字键盘对话框
     * - DIALOG_FLOATKEYBOARD: 浮点数键盘对话框
     * - DIALOG_FORMULAKEYBOARD: 公式键盘对话框
     * - DIALOG_NUMBERPICKER: 数字选择器对话框
     * - DIALOG_BAUDRATE: 波特率对话框
     * - DIALOG_BANDWIDTH: 带宽对话框
     * - DIALOG_PROBEMULTIPLE: 探头倍数对话框
     * - DIALOG_REFRECALL: 参考召回对话框
     * - DIALOG_AFTERGLOW: 余辉对话框
     * - DIALOG_CENTERTIMEBASE: 中心时基对话框
     * - DIALOG_MENUHALF: 半屏菜单对话框
     * - DIALOG_CHANNELLABEL: 通道标签对话框
     * - DIALOG_BANDWIDTHHZ: 带宽Hz对话框
     * - DIALOG_MATH_FFT_PERSIST: Math FFT持久化对话框
     * - DIALOG_PROBE_INTERFACE: 探头接口对话框
     * - DIALOG_FFT_AFTERGLOW: FFT余辉对话框
     * - DIALOG_LOAF_REF_CSV: 加载参考CSV对话框
     * - DIALOG_SELECT_COLOR: 选择颜色对话框
     * - DIALOG_SET_CHANNEL_INFO: 设置通道信息对话框
     * - DIALOG_MEASURE_TVALUE: 测量T值对话框
     * </p>
     * <p>
     * 注意事项：
     * - DIALOG_OKCANCEL和DIALOG_OK不会被隐藏（保留确认对话框）
     * - 注释掉的代码：DIALOG_OK原本也会被隐藏，但已注释掉
     * </p>
     * <p>
     * 相关方法：
     * - hideDialog(): 隐藏指定对话框
     * - isDialogShow(): 判断对话框是否显示
     * - hideAllDialogSlipButLevelMenu(): 隐藏所有对话框和滑动菜单（除了触发电平菜单）
     * - hideAllDialogSlip(): 隐藏所有对话框和滑动菜单
     * </p>
     */
    public boolean hideAllDialogsButDialogOkCancel() {
        boolean b1 = hideDialog(DIALOG_MEASUREDELAY); // 隐藏测量延迟对话框
        boolean b2 = hideDialog(DIALOG_MEASUREPHASE); // 隐藏测量相位对话框
        boolean b3 = hideDialog(DIALOG_TOPCOUNT); // 隐藏顶部计数对话框
        boolean b4 = hideDialog(DIALOG_TOPSCALE); // 隐藏顶部刻度对话框
        boolean b5 = hideDialog(DIALOG_TEXTKEYBOARD); // 隐藏文本键盘对话框
        boolean b6 = hideDialog(DIALOG_NUMBERKEYBOARD); // 隐藏数字键盘对话框
        boolean b7 = hideDialog(DIALOG_FLOATKEYBOARD); // 隐藏浮点数键盘对话框
        boolean b8 = hideDialog(DIALOG_FORMULAKEYBOARD); // 隐藏公式键盘对话框
        boolean b9 = hideDialog(DIALOG_NUMBERPICKER); // 隐藏数字选择器对话框
        boolean b10 = hideDialog(DIALOG_BAUDRATE); // 隐藏波特率对话框
        boolean b11 = hideDialog(DIALOG_BANDWIDTH); // 隐藏带宽对话框
        boolean b12 = hideDialog(DIALOG_PROBEMULTIPLE); // 隐藏探头倍数对话框
        boolean b13 = hideDialog(DIALOG_REFRECALL); // 隐藏参考召回对话框
        boolean b14 = hideDialog(DIALOG_AFTERGLOW); // 隐藏余辉对话框
        boolean b15 = hideDialog(DIALOG_CENTERTIMEBASE); // 隐藏中心时基对话框
        boolean b16 = hideDialog(DIALOG_MENUHALF); // 隐藏半屏菜单对话框
        boolean b17 = hideDialog(DIALOG_CHANNELLABEL); // 隐藏通道标签对话框
        boolean b18 = hideDialog(DIALOG_BANDWIDTHHZ); // 隐藏带宽Hz对话框
        boolean b19 = hideDialog(DIALOG_MATH_FFT_PERSIST); // 隐藏Math FFT持久化对话框
        boolean b20 = hideDialog(DIALOG_PROBE_INTERFACE); // 隐藏探头接口对话框
        boolean b21 = hideDialog(DIALOG_FFT_AFTERGLOW); // 隐藏FFT余辉对话框
        boolean b22 = hideDialog(DIALOG_LOAF_REF_CSV); // 隐藏加载参考CSV对话框
        boolean b23 = hideDialog(DIALOG_SELECT_COLOR); // 隐藏选择颜色对话框
        boolean b24 = hideDialog(DIALOG_SET_CHANNEL_INFO); // 隐藏设置通道信息对话框
        boolean b25 = hideDialog(DIALOG_MEASURE_TVALUE); // 隐藏测量T值对话框
//        boolean b15 = hideDialog(DIALOG_OK); // 注释掉的代码：隐藏确认对话框（已禁用）
        return b1 || b2 || b3 || b4 || b5 || b6 || b7 || b8 // 组合所有隐藏状态，返回是否有对话框被隐藏
                || b9 || b10 || b11 || b12 || b13 || b14 || b15
                || b16 || b17 || b18 || b19 || b20 || b21 || b22
                || b23 || b24 || b25;
    }

    public static final int VISIBLE_TOPMEASURE_CHANNEL = 1;
    public static final int VISIBLE_TOPMEASURE_SELECT = 2;
    public static final int VISIBLE_TOPTRIGGER_RUNT_HIGH = 3;
    public static final int VISIBLE_TOPTRIGGER_RUNT_LOW = 4;
    public static final int VISIBLE_TOPTRIGGER_SLOPE_HIGH = 5;
    public static final int VISIBLE_TOPTRIGGER_SLOPE_LOW = 6;
    public static final int VISIBLE_TOPTRIGGER_VIDEO = 7;
    public static final int VISIBLE_BOTTOMCHANNEL = 8;
    public static final int VISIBLE_RIGHTSERIALS1_LIN = 9;
    public static final int VISIBLE_RIGHTSERIALS1_CAN = 10;
    public static final int VISIBLE_RIGHTSERIALS2_LIN = 11;
    public static final int VISIBLE_RIGHTSERIALS2_CAN = 12;
    public static final int VISIBLE_RIGHTSERIALS3_LIN = 20;
    public static final int VISIBLE_RIGHTSERIALS3_CAN = 21;
    public static final int VISIBLE_RIGHTSERIALS4_LIN = 22;
    public static final int VISIBLE_RIGHTSERIALS4_CAN = 23;
    public static final int VISIBLE_TRIGGERVOLTAGELINE = 13;       //触发电平
    public static final int VISIBLE_DISCREETVOLTAGELINE_S1 = 14;   //阈值电平 S1
    public static final int VISIBLE_DISCREETVOLTAGELINE_S2 = 15;   //阈值电平 S2
    public static final int VISIBLE_DISCREETVOLTAGELINE_S3 = 16;
    public static final int VISIBLE_DISCREETVOLTAGELINE_S4 = 17;

    public static final int VISIBLE_TEXTKEYBOARD_CANDIDATESWORD = 18; //全键盘预选框
    public static final int VISIBLE_KEYBOARD_FORMULA = 19;//表达式键盘预选框
    //endregion

    /**
     * 颜色选择消费者回调
     * <p>
     * 功能描述：
     * - 处理颜色选择对话框返回的颜色信息
     * - 解析颜色信息字符串（格式：通道索引;颜色值）
     * - 调用handleChannelColorChange()方法处理颜色变更
     * </p>
     * <p>
     * 调用时机：
     * - 在颜色选择对话框（DIALOG_SELECT_COLOR）中选择颜色后触发
     * - 通过RxBus订阅颜色选择事件
     * - 在用户完成颜色选择操作时被调用
     * </p>
     * <p>
     * 参数说明：
     * @param colorInfo 颜色信息字符串，格式为"通道索引;颜色值"
     *                  - 例如："1;#FF0000"表示通道1选择红色
     *                  - 如果为空字符串，直接返回不处理
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 检查colorInfo是否为空，为空则直接返回
     * 2. 使用分号（;）分割字符串，获取通道索引和颜色值
     * 3. 将通道索引转换为整数
     * 4. 调用handleChannelColorChange()方法处理颜色变更
     * </p>
     * <p>
     * 相关方法：
     * - handleChannelColorChange(): 处理通道颜色变更
     * - DialogSelectColor: 颜色选择对话框
     * </p>
     */
    private Consumer<String> consumerSelectColor = new Consumer<String>() {
        @Override
        public void accept(String colorInfo) throws Throwable {
            if (colorInfo.isEmpty()) return; // 如果颜色信息为空，直接返回不处理
            Logger.i(TAG, "selectColorInfo= " + colorInfo); // 记录颜色选择信息日志
            String[] info = colorInfo.split(";"); // 使用分号分割颜色信息字符串
            int chIndex = Integer.parseInt(info[0]); // 解析通道索引（第一个元素）
            String colorStr = info[1]; // 获取颜色值字符串（第二个元素）
            handleChannelColorChange(chIndex, colorStr); // 调用颜色变更处理方法
        }
    };

    /**
     * 处理通道颜色变更
     * <p>
     * 功能描述：
     * - 统一处理通道颜色变更，更新所有相关UI组件的颜色
     * - 更新通道指示器颜色
     * - 更新波形颜色
     * - 更新通道按钮颜色
     * - 更新通道布局颜色
     * </p>
     * <p>
     * 调用时机：
     * - 在consumerSelectColor回调中被调用
     * - 在颜色选择对话框选择颜色后调用
     * - 在需要统一更新通道颜色时调用
     * </p>
     * <p>
     * 参数说明：
     * @param chIndex 通道索引（0-based）
     *                - 0-7: 模拟通道Ch1-Ch8
     *                - 8-15: Math通道M1-M8
     *                - 16-23: Ref通道R1-R8
     * @param colorStr 颜色值字符串（例如：#FF0000）
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 调用WaveManage.changeChannelColor()更新通道指示器颜色
     * 2. 调用MainActivity.changeColor()更新波形颜色
     * 3. 调用changeItemMasterColor()更新通道按钮颜色
     * 4. 调用channelsLayout.changeColor()更新通道布局颜色
     * </p>
     * <p>
     * 相关方法：
     * - WaveManage.changeChannelColor(): 更新通道指示器颜色
     * - MainActivity.changeColor(): 更新波形颜色
     * - changeItemMasterColor(): 更新通道按钮颜色
     * - channelsLayout.changeColor(): 更新通道布局颜色
     * </p>
     */
    private void handleChannelColorChange(int chIndex, String colorStr) {
        WaveManage.get().changeChannelColor(chIndex, colorStr); // 更新通道指示器颜色
        ((MainActivity) context).changeColor(chIndex); // 更新波形颜色
        changeItemMasterColor(chIndex, colorStr); // 更新通道按钮中文案颜色
        channelsLayout.changeColor(); // 更新CHX通道布局颜色
    }

    /**
     * 更新通道主控按钮的颜色
     * <p>
     * 功能描述：
     * - 根据通道类型（Ref或Math）更新对应的主控按钮颜色
     * - 只处理Ref通道和Math通道，不处理模拟通道
     * - 调用主控按钮的setContentColor()方法更新颜色
     * </p>
     * <p>
     * 调用时机：
     * - 在handleChannelColorChange()方法中被调用
     * - 在颜色选择对话框选择颜色后调用
     * - 在需要更新Ref或Math通道按钮颜色时调用
     * </p>
     * <p>
     * 参数说明：
     * @param chIndex 通道索引（0-based）
     *                - 16-23: Ref通道R1-R8
     *                - 8-15: Math通道M1-M8
     * @param colorStr 颀色值字符串（例如：#FF0000）
     *                 - 注意：实际未使用此参数，颜色由TChan.toFpgaChNo(chIndex)决定
     * </p>
     * <p>
     * 内部逻辑：
     * 1. 使用TChan.isRef()判断是否为Ref通道
     * 2. 如果是Ref通道，获取对应的Ref主控按钮，调用setContentColor()更新颜色
     * 3. 使用TChan.isMath()判断是否为Math通道
     * 4. 如果是Math通道，获取对应的Math主控按钮，调用setContentColor()更新颜色
     * </p>
     * <p>
     * 注意事项：
     * - 模拟通道（Ch1-Ch8）不在此方法中处理
     * - 颜色值由TChan.toFpgaChNo(chIndex)转换后的通道号决定，而非colorStr参数
     * </p>
     * <p>
     * 相关方法：
     * - TChan.isRef(): 判断是否为Ref通道
     * - TChan.isMath(): 判断是否为Math通道
     * - TChan.toRefNumber(): 获取Ref通道编号
     * - TChan.toMathNumber(): 获取Math通道编号
     * - TChan.toFpgaChNo(): 获取FPGA通道号
     * - MainRightLayoutItemChannelMaster.setContentColor(): 设置主控按钮颜色
     * </p>
     */
    private void changeItemMasterColor(int chIndex, String colorStr) {
        if (TChan.isRef(chIndex)) { // 判断是否为Ref通道
            refMasters.get(TChan.toRefNumber(chIndex) - 1).setContentColor(TChan.toFpgaChNo(chIndex)); // 获取Ref主控按钮并设置颜色
        }
        if (TChan.isMath(chIndex)) { // 判断是否为Math通道
            mathMasters.get(TChan.toMathNumber(chIndex) - 1).setContentColor(TChan.toFpgaChNo(chIndex)); // 获取Math主控按钮并设置颜色
        }
    }

}
