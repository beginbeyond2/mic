package com.micsig.tbook.tbookscope.services.ExternalKeys.client;  // 外部按键协议处理模块所在包

import android.content.Intent;  // Android意图类，用于活动跳转
import android.os.Handler;  // Android处理器，用于延时操作
import android.os.SystemClock;  // 系统时钟，用于获取启动后时间
import android.util.Log;  // Android日志类，用于调试输出
import android.view.View;  // Android视图类，用于可见性控制

import com.micsig.base.Logger;  // 自定义日志工具类
import com.micsig.tbook.hardware.HardwareProduct;  // 硬件产品信息类，用于区分不同硬件型号
import com.micsig.tbook.scope.Data.AutoSave;  // 自动保存数据管理类
import com.micsig.tbook.scope.Trigger.Trigger;  // 触发器基类
import com.micsig.tbook.scope.Trigger.TriggerFactory;  // 触发器工厂类，用于创建触发器对象
import com.micsig.tbook.scope.Trigger.TriggerVideo;  // 视频触发器类
import com.micsig.tbook.scope.channel.ChannelFactory;  // 通道工厂类，用于判断通道类型
import com.micsig.tbook.tbookscope.MainMsgSlip;  // 主界面滑动消息类
import com.micsig.tbook.tbookscope.MainViewGroup;  // 主视图组类，管理弹窗标识
import com.micsig.tbook.tbookscope.main.ExternalKeysMsgChannel;  // 外部按键通道消息常量
import com.micsig.tbook.tbookscope.main.ExternalKeysMsgCursor;  // 外部按键光标消息常量
import com.micsig.tbook.tbookscope.main.maincenter.serialsword.ISerialsWord;  // 串行总线字接口
import com.micsig.tbook.tbookscope.middleware.Tag;  // 中间件标签常量
import com.micsig.tbook.tbookscope.middleware.command.Command;  // 命令管理类
import com.micsig.tbook.tbookscope.middleware.command.Command_Trigger_Video;  // 视频触发命令类
import com.micsig.tbook.tbookscope.rxjava.RxBus;  // RxJava事件总线，用于组件间通信
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举常量
import com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob.ExternalKeysManager;  // 外部按键管理器，处理UI焦点和菜单导航
import com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob.ExternalKeysNode;  // 外部按键节点类，用于菜单项类型判断
import com.micsig.tbook.tbookscope.struct.ExternalKeysMsg_ToMCU;  // ARM发送给MCU的LED控制消息结构体
import com.micsig.tbook.tbookscope.structdata.ExternalKeysCommand;  // 外部按键命令执行类，封装所有按键操作
import com.micsig.tbook.tbookscope.structdata.ExternalKeysUI;  // 外部按键UI控制类
import com.micsig.tbook.tbookscope.tools.LockScreenUtils;  // 锁屏工具类
import com.micsig.tbook.tbookscope.tools.PlaySound;  // 播放音效工具类
import com.micsig.tbook.tbookscope.tools.SaveManage;  // 保存管理工具类
import com.micsig.tbook.tbookscope.tools.ScreenControls;  // 屏幕控制工具类，判断外部按键/触屏锁定
import com.micsig.tbook.tbookscope.tools.Tools;  // 通用工具类
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogCandidatesWord;  // 候选词弹窗类
import com.micsig.tbook.tbookscope.util.App;  // 应用上下文工具类
import com.micsig.tbook.tbookscope.util.CacheUtil;  // 缓存工具类，存取UI状态
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;  // 工作模式接口，定义XY/YT/YTZoom模式常量
import com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_XY;  // XY模式波形区显示控制
import com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_YT;  // YT模式波形区显示控制
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;  // 工作模式管理器，获取当前工作模式
import com.micsig.tbook.tbookscope.wavezone.bean.YTZoomMsgDisplay;  // YT缩放显示消息Bean
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;  // 光标管理类
import com.micsig.tbook.tbookscope.wavezone.display.MsgCursorVisible;  // 光标可见性消息类
import com.micsig.tbook.tbookscope.wavezone.display.WaveMaskLayer_YTZoom;  // YT缩放遮罩层控制
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;  // 测量管理类
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;  // 波形管理类
import com.micsig.tbook.ui.wavezone.TChan;  // 通道常量定义类

import java.util.ArrayList;  // 动态数组
import java.util.Arrays;  // 数组工具类
import java.util.List;  // 列表接口
import java.util.Objects;  // 对象工具类

import io.reactivex.rxjava3.functions.Consumer;  // RxJava消费者接口


/**
 * Created by liwb on 2017/12/6.
 */

/*
 * +=================================================================================================+
 * |                                  ExternalKeysProtocol 类说明                                     |
 * +=================================================================================================+
 * | 模块定位：外部按键通信协议解析与执行模块，位于 ExternalKeys 子系统的 client 层                      |
 * |-------------------------------------------------------------------------------------------------|
 * | 核心职责：                                                                                       |
 * |   1. 解析MCU发送给ARM的按键数据帧，识别按键类型、按键值和按键状态                                |
 * |   2. 根据按键类型分发到对应的处理方法，执行示波器操作（通道、光标、触发、缩放等）                    |
 * |   3. 编码ARM发送给MCU的LED控制指令，控制面板LED灯状态                                            |
 * |   4. 管理Back按键状态机（光标/通道列表/分段/缩放），协调多焦点切换                              |
 * |   5. 处理编码器（旋钮）旋转事件，实现参数连续调节                                                |
 * |-------------------------------------------------------------------------------------------------|
 * | 架构设计：                                                                                       |
 * |   MCU → [串口数据帧] → parseMCUTOARM() → parseMCUTOARM_0~4/10~12() → ExternalKeysCommand       |
 * |   ARM → parseToMCU() → [LED控制帧] → MCU                                                        |
 * |   编码器 → parseEditor() → SelectKey()/editorMulMove() → ExternalKeysCommand/ExternalKeysManager |
 * |-------------------------------------------------------------------------------------------------|
 * | 数据流向：                                                                                       |
 * |   输入：MCU按键数据帧(byte[6])、编码器数据帧(byte[2])、RxBus事件流                              |
 * |   输出：ExternalKeysCommand操作调用、RxBus事件发送、ExternalKeysMsg_ToMCU LED控制帧              |
 * |-------------------------------------------------------------------------------------------------|
 * | 依赖关系：                                                                                       |
 * |   上层依赖：ExternalKeysService（调用parseMCUTOARM/parseEditor/parseToMCU）                     |
 * |   下层调用：ExternalKeysCommand（按键命令执行）、ExternalKeysManager（UI焦点管理）                 |
 * |            ExternalKeysUI（UI状态控制）、RxBus（事件总线）                                       |
 * |-------------------------------------------------------------------------------------------------|
 * | 使用场景：                                                                                       |
 * |   用户操作示波器前面板按键/旋钮时，MCU将按键事件通过串口发送给ARM，                                |
 * |   本类负责解析协议帧并调用对应业务逻辑，同时根据当前状态控制面板LED灯反馈                         |
 * +=================================================================================================+
 */
public class ExternalKeysProtocol {
    public static String Debug="ExternalKeys";  // 调试日志标签名
    private static String TAG = "ExternalKeysProtocol";  // 类级别日志标签

    /**
     * 是否打印调试位置信息
     */
    public static boolean isPrintDebugLocation = false;  // 调试位置打印开关，默认关闭
    //region 类型定义
    /**
     * 发送类型 MCU发送给ARM
     */
    public final static byte TYPE_KEY_PRESS = 0x01;  // 按键按下类型：MCU→ARM
    /**
     * 发送类型 编码器编码发送
     */
    public final static byte TYPE_KEY_PRESS_LONG_2 = 0x02;  // 按键长按第二阶段类型
    public final static byte TYPE_KEY_PRESS_LONG = 0x03;  // 按键长按类型
    public final static byte TYPE_KEY_PRESS_EXT = 0x05;  // 扩展按键按下类型（扩展键盘）
    public final static byte TYPE_KEY_PRESS_LONG_EXT = 0x0D;  // 扩展按键长按类型（扩展键盘）
    public final static byte TYPE_KNOB = 0x0E;  // 编码器旋转类型

    public final static byte KNOB_CLOCKWISE = 0;            //顺  // 旋钮顺时针方向
    public final static byte KNOB_ANTICLOCKWISE = 1;        //逆  // 旋钮逆时针方向

    public final static byte KNOB_1 = 0;  // 编码器1标识（菜单导航旋钮）
    public final static byte KNOB_2 = 1;  // 编码器2标识（光标/候选词旋钮）
    public final static byte KNOB_HORIZONTAL_POSITION = 2;  // 水平位置旋钮标识
    public final static byte KNOB_TRIGGER_LEVEL = 3;  // 触发电平旋钮标识
    public final static byte KNOB_HORIZONTAL_SCALE = 4;  // 水平刻度旋钮标识
    public final static byte KNOB_VERTICAL_POSITION = 5;  // 垂直位置旋钮标识
    public final static byte KNOB_VERTICAL_SCALE = 6;  // 垂直刻度旋钮标识

    /**
     * 发送类型　ＡＲＭ发送给ＭＣＵ
     */
    public final static byte TYPE_ARMTOMCU = (byte) 0xF1;  // ARM→MCU LED控制帧类型
    public final static byte TYPE_ARMTOMCU_EXT = (byte) 0xF2;  // ARM→MCU 扩展LED控制帧类型
    //endregion

    //region 类型1的定义
    //region 第一个字节

    private final static byte KEY_00_SEQ = 1 << 0;  // 序列按键位掩码
    private final static byte KEY_01_QUICK_MENU = 1 << 1;  // 快捷菜单按键位掩码
    private final static byte KEY_02_ZOOM = 1 << 2;  // 缩放按键位掩码
    private final static byte KEY_03_CH2 = 1 << 3;  // 通道2按键位掩码
    private final static byte KEY_04_POSITION_LEFT = 1 << 4;  // 位置左旋钮按下位掩码
    private final static byte KEY_05_SCALE_LEFT = 1 << 5;  // 刻度左旋钮按下位掩码
    private final static byte KEY_06_SELECT_LEFT = 1 << 6;  // 选择左旋钮按下位掩码
    private final static byte KEY_07_TRIGGER_LEFT = (byte) (1 << 7);  // 触发左旋钮按下位掩码

    private final static byte KEY_10_RUN_STOP = 1 << 0;  // 运行/停止按键位掩码
    private final static byte KEY_11_TOP_MENU = 1 << 1;  // 顶部菜单按键位掩码
    private final static byte KEY_12_VERTICAL_CURSOR = 1 << 2;  // 垂直光标按键位掩码
    private final static byte KEY_13_CH3 = 1 << 3;  // 通道3按键位掩码
    private final static byte KEY_14_POSITION_DOWN = 1 << 4;  // 位置下旋钮按下位掩码
    private final static byte KEY_15_SCALE_DOWN = 1 << 5;  // 刻度下旋钮按下位掩码
    private final static byte KEY_16_SELECT_DOWN = 1 << 6;  // 选择下旋钮按下位掩码
    private final static byte KEY_17_TRIGGER_DOWN = (byte) (1 << 7);  // 触发下旋钮按下位掩码

    private final static byte KEY_20_CAPTURE = 1 << 0;  // 截图按键位掩码
    private final static byte KEY_21_BACK = 1 << 1;  // 返回按键位掩码
    private final static byte KEY_22_CH_MENU = 1 << 2;  // 通道菜单按键位掩码
    private final static byte KEY_23_CH4 = 1 << 3;  // 通道4按键位掩码
    private final static byte KEY_24_POSITION_RIGHT = 1 << 4;  // 位置右旋钮按下位掩码
    private final static byte KEY_25_SCALE_RIGHT = 1 << 5;  // 刻度右旋钮按下位掩码
    private final static byte KEY_26_SELECT_RIGHT = 1 << 6;  // 选择右旋钮按下位掩码
    private final static byte KEY_27_TRIGGER_RIGHT = (byte) (1 << 7);  // 触发右旋钮按下位掩码

    private final static byte KEY_30_AUTO = 1 << 0;  // 自动设置按键位掩码
    private final static byte KEY_31_HORIZONTAL_CURSOR = 1 << 1;  // 水平光标按键位掩码
    private final static byte KEY_32_CH1 = 1 << 2;  // 通道1按键位掩码
    private final static byte KEY_33_MATH = 1 << 3;  // 数学运算按键位掩码
    private final static byte KEY_34_POSITION_CENTER = 1 << 4;  // 位置中旋钮按下位掩码
    private final static byte KEY_35_SCALE_CENTER = 1 << 5;  // 刻度中旋钮按下位掩码
    private final static byte KEY_36_SELECT_CENTER = 1 << 6;  // 选择中旋钮按下位掩码
    private final static byte KEY_37_TRIGGER_CENTER = (byte) (1 << 7);  // 触发中旋钮按下位掩码

    private final static byte KEY_40_REF = 1 << 0;  // 参考波形按键位掩码
    private final static byte KEY_41_TOUCH_OFF = 1 << 1;  // 触摸锁定按键位掩码
    private final static byte KEY_42_HOME = 1 << 2;  // 主页按键位掩码
    private final static byte KEY_43_NULL = 1 << 3;  // 空按键位掩码（保留位）
    private final static byte KEY_44_POSITION_UP = 1 << 4;  // 位置上旋钮按下位掩码
    private final static byte KEY_45_SCALE_UP = 1 << 5;  // 刻度上旋钮按下位掩码
    private final static byte KEY_46_SELECT_UP = 1 << 6;  // 选择上旋钮按下位掩码
    private final static byte KEY_47_TRIGGER_UP = (byte) (1 << 7);  // 触发上旋钮按下位掩码

    private final static byte KEY_100_KNOB5 = 1 << 0;  // 旋钮5按下位掩码（扩展键盘-位置归零）
    private final static byte KEY_101_KNOB3 = 1 << 1;  // 旋钮3按下位掩码（扩展键盘-触发电平归零/切换）
    private final static byte KEY_102_KNOB2 = 1 << 2;  // 旋钮2按下位掩码（扩展键盘-光标切换）
    private final static byte KEY_103_BUS2 = 1 << 3;  // 总线2按键位掩码（扩展键盘）
    private final static byte KEY_104_BUS1 = 1 << 4;  // 总线1按键位掩码（扩展键盘）
    private final static byte KEY_105_BUS = 1 << 5;  // 总线按键位掩码（扩展键盘）
    private final static byte KEY_106_EDGE = 1 << 6;  // 边沿触发按键位掩码（扩展键盘）
    private final static byte KEY_107_TMENU = (byte) (1 << 7);  // 触发菜单按键位掩码（扩展键盘）


    private final static byte KEY_117_KNOB6 = (byte) (1 << 7);  // 旋钮6按下位掩码（通道位置归零）
    private final static byte KEY_116_CH5 = 1 << 6;  // 通道5按键位掩码
    private final static byte KEY_115_CH6 = 1 << 5;  // 通道6按键位掩码
    private final static byte KEY_114_CH7 = 1 << 4;  // 通道7按键位掩码
    private final static byte KEY_113_CH8 = 1 << 3;  // 通道8按键位掩码
    private final static byte KEY_112_Select=1 << 2;  // Select按键位掩码（扩展键盘，保留）
    private final static byte KEY_111_Force=1<<1;  // 强制触发按键位掩码（扩展键盘）
    private final static byte KEY_110_MODE=1<<0;  // 触发模式按键位掩码（扩展键盘-自动/普通切换）

    private final static byte KEY_127_CHX=(byte)(1<<7);  // 通道列表按键位掩码（扩展键盘）
    private final static byte KEY_126_Measure=(byte)(1<<6);  // 测量按键位掩码（扩展键盘）


    public final static byte KEY_SELECT_UP = 1;  // 选择键上方向标识
    public final static byte KEY_SELECT_DOWN = 2;  // 选择键下方向标识
    public final static byte KEY_SELECT_LEFT = 3;  // 选择键左方向标识
    public final static byte KEY_SELECT_RIGHT = 4;  // 选择键右方向标识

    public final static byte KEY_PRESS_LONG_VAL = 1;  // 长按步进值常量

    //endregion
    //endregion

    //region 类型F1的定义
    private final static byte LED_00_STOP = 1 << 0;  // STOP指示灯位掩码
    private final static byte LED_01_AUTO = 1 << 1;  // AUTO指示灯位掩码
    private final static byte LED_02_TOUCH_OFF = 1 << 2;  // TOUCH_OFF指示灯位掩码
    private final static byte LED_03_BUS1_BLUE = 1 << 3;  // 总线1蓝色LED位掩码
    private final static byte LED_04_BUS1_GREEN = 1 << 4;  // 总线1绿色LED位掩码
    private final static byte LED_05_BUS2_RED = 1 << 5;  // 总线2红色LED位掩码
    private final static byte LED_06_BUS2_BLUE = 1 << 6;  // 总线2蓝色LED位掩码
    private final static byte LED_07_BUS2_GREEN = (byte) (1 << 7);  // 总线2绿色LED位掩码


    private final static byte LED_10_MATH_RED = 1 << 0;  // MATH红色LED位掩码
    private final static byte LED_11_MATH_BLUE = 1 << 1;  // MATH蓝色LED位掩码
    private final static byte LED_12_MATH_GREEN = 1 << 2;  // MATH绿色LED位掩码

    private final static byte LED_13_SEQ = 1 << 3;  // SEQ指示灯位掩码
    private final static byte LED_14_CAPTURE = 1 << 4;  // CAPTURE指示灯位掩码
    private final static byte LED_15_ZOOM = 1 << 5;  // ZOOM指示灯位掩码
    private final static byte LED_16_HOME = 1 << 6;  // HOME指示灯位掩码
    private final static byte LED_17_RUN = (byte) (1 << 7);  // RUN指示灯位掩码


    private final static byte LED_20_CH3_GREEN = 1 << 0;  // CH3绿色LED位掩码
    private final static byte LED_21_CH4_RED = 1 << 1;  // CH4红色LED位掩码
    private final static byte LED_22_CH4_BLUE = 1 << 2;  // CH4蓝色LED位掩码
    private final static byte LED_23_CH4_GREEN = 1 << 3;  // CH4绿色LED位掩码
    private final static byte LED_24_REF_RED = 1 << 4;  // REF红色LED位掩码
    private final static byte LED_25_REF_BLUE = 1 << 5;  // REF蓝色LED位掩码
    private final static byte LED_26_REF_GREEN = 1 << 6;  // REF绿色LED位掩码
    private final static byte LED_27_BUS1_RED = (byte) (1 << 7);  // 总线1红色LED位掩码

    private final static byte LED_30_CH1_RED = 1 << 0;  // CH1红色LED位掩码
    private final static byte LED_31_CH1_BLUE = 1 << 1;  // CH1蓝色LED位掩码
    private final static byte LED_32_CH1_GREEN = 1 << 2;  // CH1绿色LED位掩码
    private final static byte LED_33_CH2_RED = 1 << 3;  // CH2红色LED位掩码
    private final static byte LED_34_CH2_BLUE = 1 << 4;  // CH2蓝色LED位掩码
    private final static byte LED_35_CH2_GREEN = 1 << 5;  // CH2绿色LED位掩码
    private final static byte LED_36_CH3_RED = 1 << 6;  // CH3红色LED位掩码
    private final static byte LED_37_CH3_BLUE = (byte) (1 << 7);  // CH3蓝色LED位掩码


    private final static byte LED_100_CH8_RED=1<<0;  // CH8红色LED位掩码
    private final static byte LED_101_CH8_BLUE=1<<1;  // CH8蓝色LED位掩码
    private final static byte LED_102_CH8_GREEN=1<<2;  // CH8绿色LED位掩码
    private final static byte LED_103_VCURSOR = 1 << 3;  // 垂直光标LED位掩码
    private final static byte LED_104_HCURSOR = 1 << 4;  // 水平光标LED位掩码
    private final static byte LED_105_BUS_RED = 1 << 5;  // 总线红色LED位掩码
    private final static byte LED_106_BUS_BLUE = 1 << 6;  // 总线蓝色LED位掩码
    private final static byte LED_107_BUS_GREEN = (byte) (1 << 7);  // 总线绿色LED位掩码

    private final static byte LED_110_CH5_BLUE = 1 << 0;  // CH5蓝色LED位掩码
    private final static byte LED_111_CH5_GREEN = 1 << 1;  // CH5绿色LED位掩码
    private final static byte LED_112_CH6_RED = 1 << 2;  // CH6红色LED位掩码
    private final static byte LED_113_CH6_BLUE = 1 << 3;  // CH6蓝色LED位掩码
    private final static byte LED_114_CH6_GREEN = 1 << 4;  // CH6绿色LED位掩码
    private final static byte LED_115_CH7_RED = 1 << 5;  // CH7红色LED位掩码
    private final static byte LED_116_CH7_BLUE = 1 << 6;  // CH7蓝色LED位掩码
    private final static byte LED_117_CH7_GREEN = (byte) (1 << 7);  // CH7绿色LED位掩码

    private final static byte LED_127_CH5_RED=(byte)(1<<7);  // CH5红色LED位掩码

    /**
     * 判断是否是不同字节上的组合键
     * <p>遍历按键数据帧的前4个字节，统计非零字节数量，
     * 若超过1个字节非零则认为是组合键（同时按下多个按键）</p>
     *
     * @param bytes MCU发送的按键数据帧
     * @return true表示是组合键，false表示单一按键
     */
    private static boolean isCompositeKey(byte[] bytes) {
        int count = 0;  // 非零字节计数器
        for (int i = 0; i < 4; i++) {  // 遍历前4个字节（按键位域）
            if (bytes[i] != 0) {  // 当前字节非零表示有按键按下
                count++;  // 计数器递增
            }
        }
        return count > 1;  // 超过1个字节有按键则判定为组合键
    }

    public static final int BACKSTATE_CURSOR = 0;  // Back状态：光标（通用）
    public static final int BACKSTATE_VCURSOR = BACKSTATE_CURSOR;  // Back状态：垂直光标
    public static final int BACKSTATE_HCURSOR = 1;  // Back状态：水平光标
    public static final int BACKSTATE_CHLIST = 2;  // Back状态：通道列表
    public static final int BACKSTATE_SEGMENT = 3;  // Back状态：分段控制
    public static final int BACKSTATE_ZOOMUP = 4;  // Back状态：缩放上半区

    public static final int BACKSTATE_MIN = BACKSTATE_CHLIST;  // Back状态最小值（通道列表）
    public static final int BACKSTATE_MAX = BACKSTATE_ZOOMUP;  // Back状态最大值（缩放）
    public static final int BACKSTATE_NULL = -1;  // Back状态空值（无可操作对象）
    private static int backState = BACKSTATE_CHLIST;  // 当前Back状态，默认通道列表
    private static boolean singleVCursor = true;  // 垂直光标是否为单光标模式（true=单光标，false=双光标）
    private static boolean singleHCursor = true;  // 水平光标是否为单光标模式（true=单光标，false=双光标）
    private static boolean curHCursor = true;//当两个方向的光标都存在时，当前操作的是否是水平方向的光标  // 垂直/水平光标操作方向标识
    private static boolean positionV = true;  // Position旋钮是否控制垂直位置（true=垂直位置，false=水平位置）

    /**
     * 初始化外部按键协议模块
     * <p>注册RxBus事件监听，订阅光标可见性、通道列表、YT缩放、
     * 主界面滑动、分段控制等状态变化事件</p>
     */
    public static void init() {
        initControl();  // 调用控制初始化方法
    }

    /**
     * 初始化RxBus事件订阅
     * <p>订阅5个事件源：光标可见性变化、通道列表可见性变化、
     * YT缩放显示变化、主界面滑动切换、分段控制可见性变化</p>
     */
    private static void initControl() {
        RxBus.getInstance().getObservable(RxEnum.CURSOR_CHANGE_VISIBLE).subscribe(consumerCursorChangeVisible);  // 订阅光标可见性变化事件
        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_CHANNEL_VISIBLE_LAYOUTTOBTN).subscribe(consumerChannelVisible);  // 订阅通道列表可见性变化事件
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_DISPLAY_YTZOOM).subscribe(consumerYtZoomDisplay);  // 订阅YT缩放显示变化事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_SLIP_TO_OTHER).subscribe(consumerMainSlipToOther);  // 订阅主界面滑动切换事件
        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_SEGMENTED_VISIBLE).subscribe(consumerSegmentVisible);  // 订阅分段控制可见性变化事件
    }

    /** 光标可见性变化事件消费者（空实现，预留扩展） */
    private static Consumer<MsgCursorVisible> consumerCursorChangeVisible = new Consumer<MsgCursorVisible>() {
        @Override
        public void accept(MsgCursorVisible msgCursorVisible) throws Exception {
            // 预留：光标可见性变化时的回调处理
        }
    };

    /**
     * 通道列表可见性变化事件消费者
     * <p>当通道列表显示时，切换Back状态到通道列表；
     * 当通道列表隐藏时，根据当前Back状态重新选择可用的焦点对象</p>
     */
    private static Consumer<Integer> consumerChannelVisible = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Exception {
            if (integer == View.VISIBLE) {  // 通道列表变为可见
//                backState = BACKSTATE_CHLIST;
                BackStateUpdate(BACKSTATE_CHLIST);  // 更新Back状态为通道列表
                CacheUtil.get().setLastObjectIsCursor(false);  // 标记当前焦点非光标
            } else {  // 通道列表变为不可见
                if (backState==BACKSTATE_CHLIST){  // 当前Back状态仍为通道列表
                    selectBackStateList();  // 重新选择可用的Back状态
                }
                BackStateUpdate(backState);  // 刷新Back状态优先级
                if (backState!=BACKSTATE_HCURSOR && backState!=BACKSTATE_VCURSOR){  // 当前焦点非光标
                    CacheUtil.get().setLastObjectIsCursor(false);  // 标记当前焦点非光标
                }
                ExternalKeysUI.getInstance().setFocusControlBackVisible(false);  // 隐藏Back焦点控制
            }
        }
    };
    /**
     * 分段控制可见性变化事件消费者
     * <p>当分段控制显示时，切换Back状态到分段；
     * 当分段控制隐藏时，重新选择可用的焦点对象</p>
     */
    private static Consumer<Boolean> consumerSegmentVisible = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean bBoolean) throws Exception {
            if (bBoolean) {  // 分段控制变为可见
//                backState = BACKSTATE_SEGMENT;
                BackStateUpdate(BACKSTATE_SEGMENT);  // 更新Back状态为分段控制
                CacheUtil.get().setLastObjectIsCursor(false);  // 标记当前焦点非光标
                if (ExternalKeysUI.getInstance().isVisibleFocusControl()) {  // 焦点控件当前可见
                    ExternalKeysManager.get().showViewPlace(ExternalKeysManager.LISTTYPE_SEGMENTED);  // 显示分段列表焦点
                }
            }else {  // 分段控制变为不可见
                //Log.d("Tag.Debug", String.format("consumerSegmentVisible accept: %b",bBoolean ));
                if (backState==BACKSTATE_SEGMENT){  // 当前Back状态仍为分段控制
                    selectBackStateList();  // 重新选择可用的Back状态
                    BackStateUpdate(backState);  // 刷新Back状态优先级
                }
                if (backState!=BACKSTATE_HCURSOR && backState!=BACKSTATE_VCURSOR){  // 当前焦点非光标
                    CacheUtil.get().setLastObjectIsCursor(false);  // 标记当前焦点非光标
                }
            }
        }
    };
    /**
     * YT缩放显示变化事件消费者
     * <p>当YT缩放显示时，切换Back状态到缩放；
     * 当YT缩放隐藏时，重新选择可用的焦点对象</p>
     */
    private static Consumer<YTZoomMsgDisplay> consumerYtZoomDisplay = new Consumer<YTZoomMsgDisplay>() {
        @Override
        public void accept(YTZoomMsgDisplay ytZoomMsgDisplay) throws Exception {
            if (ytZoomMsgDisplay.isDisplay()) {  // YT缩放变为显示
//                backState = BACKSTATE_ZOOMUP;
                BackStateUpdate(BACKSTATE_ZOOMUP);  // 更新Back状态为缩放
                CacheUtil.get().setLastObjectIsCursor(false);  // 标记当前焦点非光标
            } else {  // YT缩放变为隐藏
                if (backState==BACKSTATE_ZOOMUP){  // 当前Back状态仍为缩放
                    selectBackStateList();  // 重新选择可用的Back状态
                }
                BackStateUpdate(backState);  // 刷新Back状态优先级
                if (backState!=BACKSTATE_HCURSOR && backState!=BACKSTATE_VCURSOR){  // 当前焦点非光标
                    CacheUtil.get().setLastObjectIsCursor(false);  // 标记当前焦点非光标
                }
            }
            ExternalKeysUI.getInstance().setFocusControlBackVisible(false);  // 隐藏Back焦点控制
        }
    };

    /**
     * 主界面滑动切换事件消费者
     * <p>当主界面发生滑动切换时，重置百分比调节标志</p>
     */
    private static Consumer<MainMsgSlip> consumerMainSlipToOther = new Consumer<MainMsgSlip>() {
        @Override
        public void accept(MainMsgSlip msgSlip) throws Exception {
            canPercentFlag = false;  // 重置百分比调节标志，取消正在进行的百分比调节
        }
    };

    /**
     * 解析扩展键盘第一字节按键（KEY_100~KEY_107）
     * <p>处理扩展键盘上的旋钮按下和功能按键事件，包括：
     * KNOB5（位置归零）、KNOB3（触发电平归零/切换）、KNOB2（光标切换）、
     * BUS1/BUS2/BUS（串行总线添加）、EDGE（边沿触发）、TMENU（触发菜单）</p>
     *
     * @param val   按键位掩码值
     * @param state 按键状态（TYPE_KEY_PRESS_EXT / TYPE_KEY_PRESS_LONG_EXT）
     */
    static void parseMCUTOARM_10(byte val, byte state){
        Log.d(TAG, "parseMCUTOARM_10: "+val);  // 打印调试日志
        switch (val){
            case KEY_100_KNOB5:  // 旋钮5按下：水平位置归50%
                //position
                if (ExternalKeysCommand.get().moveTimeBasePosition50Percent()) {  // 执行时基位置归50%并判断是否成功
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.TriggerTime);  // 通知波形区滑动方向最后操作对象为触发时间
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_LEFTRIGHT);  // 通知波形区滑动方向为左右
                }
                break;
            case KEY_101_KNOB3:  // 旋钮3按下：触发电平归零/切换
                //level
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                TriggerValueVoltageLine triggerValueVoltageLine = TriggerValueVoltageLine.getInstance();  // 获取触发电平/值电平切换管理实例
                if (state == TYPE_KEY_PRESS_EXT) {  // 扩展按键短按
                    if (triggerValueVoltageLine.isTriggerlevelActive()) {  // 当前激活的是触发电平
                        ExternalKeysCommand.get().clickTriggerLevelCenter();  // 触发电平归中
                    } else {  // 当前激活的是值电平
                        ExternalKeysCommand.get().clickValueLevelCenter();  // 值电平归中
                    }
                } else if (state == TYPE_KEY_PRESS_LONG_EXT) {  // 扩展按键长按
                    triggerValueVoltageLine.switchTriggerValueLevel();  // 切换触发电平/值电平模式
                }
                break;
            case KEY_102_KNOB2:  // 旋钮2按下：光标切换/候选词确认
//                当前为可选中光标状态时，切换水平方向的光标选择
               {

//                当前为全键盘窗口时，点击确定预选框中汉字
                    if (ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)) {  // 当前为文本键盘弹窗
                        ExternalKeysCommand.get().moveKeyBoardCandidatesWord(TopDialogCandidatesWord.ACTION_CANDIDATES_FINISH, 1);  // 确认选择当前候选词
                    } else {  // 非文本键盘弹窗时，切换光标模式
                        if (backState == BACKSTATE_VCURSOR) {  // 当前为垂直光标模式
                            singleVCursor = !singleVCursor;  // 切换单/双垂直光标
                            if (singleVCursor) {  // 切换为单光标
                                ExternalKeysCommand.get().setCursorSelected(false, TChan.Cursor_col_2);  // 选中垂直光标2
                            } else {  // 切换为双光标
                                ExternalKeysCommand.get().setCursorSelected(false, TChan.Cursor_col_4);  // 选中垂直光标4
                            }
                        } else if (backState == BACKSTATE_HCURSOR) {  // 当前为水平光标模式
                            singleHCursor = !singleHCursor;  // 切换单/双水平光标
                            if (singleHCursor) {  // 切换为单光标
                                ExternalKeysCommand.get().setCursorSelected(true, TChan.Cursor_row_2);  // 选中水平光标2
                            } else {  // 切换为双光标
                                ExternalKeysCommand.get().setCursorSelected(true, TChan.Cursor_row_4);  // 选中水平光标4
                            }
                        }
                    }

                }
                break;
//            case KEY_103_BUS2:
//                if(state == TYPE_KEY_PRESS_EXT) {
//                    ExternalKeysCommand.get().clickRightSerials(2);
//                }
//                break;
//            case KEY_104_BUS1:
//                if(state == TYPE_KEY_PRESS_EXT) {
//                    ExternalKeysCommand.get().clickRightSerials(1);
//                }
//                break;
//            case KEY_105_BUS:
//                if(state==TYPE_KEY_PRESS_EXT){
//                    Log.d(TAG, "parseMCUTOARM_10 ："+KEY_105_BUS);
//                    ExternalKeysCommand.get().clickRightSerials(1);
//                }
//                break;
            case KEY_105_BUS:  // 总线按键（含BUS1/BUS2，合并处理）
            case KEY_104_BUS1:  // 总线1按键
            case KEY_103_BUS2:  // 总线2按键
                if (state == TYPE_KEY_PRESS_EXT) {  // 扩展按键短按
                    ExternalKeysCommand.get().clickAddRightSerials();  // 添加串行总线解码
                }
                break;
            case KEY_106_EDGE:  // 边沿触发按键
                if(state == TYPE_KEY_PRESS_EXT){  // 扩展按键短按
                    PlaySound.getInstance().playButton();  // 播放按键音效
                    ExternalKeysCommand.get().clickTriggerEdge();  // 切换触发边沿
                }
                break;
            case KEY_107_TMENU:  // 触发菜单按键
                if(state == TYPE_KEY_PRESS_EXT) {  // 扩展按键短按
                    ExternalKeysCommand.get().clickTriggerMenu();  // 打开触发菜单
                }
                break;
        }
    }
    /**
     * 解析扩展键盘第二字节按键（KEY_110~KEY_117）
     * <p>处理扩展键盘上的通道按键和旋钮6按下事件，包括：
     * KNOB6（通道垂直位置归零）、CH5~CH8（通道开关）、
     * Select（保留）、Force（强制触发）、MODE（触发模式切换）</p>
     *
     * @param val   按键位掩码值
     * @param state 按键状态（TYPE_KEY_PRESS_EXT / TYPE_KEY_PRESS_LONG_EXT）
     */
    static void parseMCUTOARM_11(byte val, byte state){
        switch (val){
            case KEY_117_KNOB6:  // 旋钮6按下：通道垂直位置归零
                int chIdx = WaveManage.get().getCurCh();  // 获取当前选中通道索引
                WaveManage.get().setCenterChY(chIdx);  // 将当前通道垂直位置居中
                switch (WorkModeManage.getInstance().getmWorkMode()) {  // 根据当前工作模式处理
                    case IWorkMode.WorkMode_XY: {  // XY模式
                        if (ChannelFactory.isDynamicCh(TChan.toFpgaChNo(chIdx))) {  // 当前通道为动态通道
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, chIdx);  // 通知波形区最后操作对象
                            if (TChan.Ch1 == chIdx || TChan.Ch3 == chIdx) {  // CH1或CH3控制水平方向
                                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_XY.MOVE_LEFTRIGHT);  // 通知水平滑动
                            } else if (TChan.Ch2 == chIdx || TChan.Ch4 == chIdx) {  // CH2或CH4控制垂直方向
                                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_XY.MOVE_UPDOWN);  // 通知垂直滑动
                            }
                        }
                        break;
                    }
                    case IWorkMode.WorkMode_YT:  // YT模式
                    case IWorkMode.WorkMode_YTZOOM: {  // YT缩放模式
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, chIdx);  // 通知波形区最后操作对象为当前通道
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);  // 通知垂直滑动
                        break;
                    }
                }
                break;
            case KEY_116_CH5:{  // CH5通道按键
                // TODO: 2024-8-1 按键处理
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS_EXT || state==TYPE_KEY_PRESS_LONG_EXT) {  // 扩展按键短按或长按
                    ExternalKeysCommand.get().clickRightChannel(ExternalKeysMsgChannel.CH5);  // 切换CH5通道开关
                }
            }break;
            case KEY_115_CH6:{  // CH6通道按键
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS_EXT || state==TYPE_KEY_PRESS_LONG_EXT) {  // 扩展按键短按或长按
                    ExternalKeysCommand.get().clickRightChannel(ExternalKeysMsgChannel.CH6);  // 切换CH6通道开关
                }
            }break;

            case KEY_114_CH7:{  // CH7通道按键
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS_EXT || state==TYPE_KEY_PRESS_LONG_EXT) {  // 扩展按键短按或长按
                    ExternalKeysCommand.get().clickRightChannel(ExternalKeysMsgChannel.CH7);  // 切换CH7通道开关
                }
            }break;
            case KEY_113_CH8:{  // CH8通道按键
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS_EXT || state==TYPE_KEY_PRESS_LONG_EXT) {  // 扩展按键短按或长按
                    ExternalKeysCommand.get().clickRightChannel(ExternalKeysMsgChannel.CH8);  // 切换CH8通道开关
                }
            }break;
            case KEY_112_Select:{  // Select按键（保留，暂无功能）

            }break;
            case KEY_111_Force: {//force trigger  // 强制触发按键
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS_EXT || state == TYPE_KEY_PRESS_LONG_EXT) {  // 扩展按键短按或长按
                    ExternalKeysCommand.get().forceTrigger();  // 执行强制触发
                }
            }break;
            case KEY_110_MODE: {//trigger mode: auto、normal  // 触发模式切换按键（自动/普通）
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS_EXT || state == TYPE_KEY_PRESS_LONG_EXT) {  // 扩展按键短按或长按
                    ExternalKeysCommand.get().changeTriggerCommonMode();  // 切换触发模式（自动/普通）
                }
            }break;
        }
    }
    /**
     * 解析扩展键盘第三字节按键（KEY_126~KEY_127）
     * <p>处理扩展键盘上的测量按键和通道列表按键</p>
     *
     * @param val   按键位掩码值
     * @param state 按键状态
     */
    static void parseMCUTOARM_12(byte val, byte state){
        switch (val){
            case KEY_126_Measure: {  // 测量按键
                if (!ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面不可见时才处理
                    ExternalKeysCommand.get().ClickMeasureBtn();  // 点击测量按钮
                }
            }break;
            case KEY_127_CHX:{  // 通道列表按键
               ExternalKeysCommand.get().ClickChannelList();  // 打开通道列表
            }break;
        }
    }

    /**
     * 解析标准键盘第0字节按键（KEY_00~KEY_07）
     * <p>处理SEQ、QUICK_MENU、ZOOM、CH2、POSITION_LEFT、SCALE_LEFT、
     * SELECT_LEFT、TRIGGER_LEFT等按键事件</p>
     *
     * @param val   按键位掩码值
     * @param state 按键状态（TYPE_KEY_PRESS / TYPE_KEY_PRESS_LONG_2等）
     */
    static void parseMCUTOARM_0(byte val, byte state) {
        switch (val) {
            case KEY_00_SEQ:  // 序列按键
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2)  // 短按或长按第二阶段
                    ExternalKeysCommand.get().clickSeq();  // 切换序列模式
                break;
            case KEY_01_QUICK_MENU:  // 快捷菜单按键
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2)  // 短按或长按第二阶段
                    ExternalKeysCommand.get().openBottomSlip();  // 打开底部滑动菜单
                break;
            case KEY_02_ZOOM:  // 缩放按键
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2) {  // 短按或长按第二阶段
                    ExternalKeysCommand.get().clickZoom();  // 切换缩放模式
                    ExternalKeysManager.get().setFocusViewVisible(View.GONE);  // 隐藏焦点视图
                }
                break;
            case KEY_03_CH2:  // 通道2按键
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2) {  // 短按或长按第二阶段
                    Logger.d(TAG, "KEY_03_CH2," + state);  // 打印调试日志
                    ExternalKeysCommand.get().clickRightChannel(2);  // 切换CH2通道开关
                }
                break;
            case KEY_04_POSITION_LEFT: {  // 位置左旋钮按下：时基位置左移
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                int count = state == TYPE_KEY_PRESS ? 1 : getPressLongSpeed(1);  // 计算步进数：短按为1，长按为加速值
                switch (WorkModeManage.getInstance().getmWorkMode()) {  // 根据工作模式处理
                    case IWorkMode.WorkMode_XY:  // XY模式：移动CH1水平位置
                        if (WaveManage.get().getCurCh() != TChan.Ch1) {  // 当前通道非CH1
                            WaveManage.get().setSelectCursor(TChan.Ch1);  // 切换选中通道为CH1
                        }
                        ExternalKeysCommand.get().moveChannelPosition(true, count);  // 向左移动通道位置
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Ch1);  // 通知最后操作对象
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_XY.MOVE_LEFTRIGHT);  // 通知水平滑动
                        break;
                    case IWorkMode.WorkMode_YT:  // YT模式
                    case IWorkMode.WorkMode_YTZOOM:  // YT缩放模式

                        if (ExternalKeysCommand.get().moveTimeBasePosition(false, count)) {  // 时基位置左移
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.TriggerTime);  // 通知最后操作对象
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_LEFTRIGHT);  // 通知水平滑动
                        }
                        break;
                }
            }
            break;
            case KEY_05_SCALE_LEFT:  // 刻度左旋钮按下：时基刻度增大
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                ExternalKeysCommand.get().moveTimeBaseScale(true, state == TYPE_KEY_PRESS ? 1 : KEY_PRESS_LONG_VAL);  // 时基刻度增大
                break;
            case KEY_06_SELECT_LEFT:  // 选择左旋钮按下
                SelectKey(true, KEY_SELECT_LEFT, state);  // 调用选择键通用处理方法
                break;
            case KEY_07_TRIGGER_LEFT:  // 触发左旋钮按下：触发源切换
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                ExternalKeysCommand.get().clickTriggerSource(true);  // 向左切换触发源

                break;
        }
    }

    /**
     * 解析标准键盘第1字节按键（KEY_10~KEY_17）
     * <p>处理RUN_STOP、TOP_MENU、VERTICAL_CURSOR、CH3、POSITION_DOWN、
     * SCALE_DOWN、SELECT_DOWN、TRIGGER_DOWN等按键事件</p>
     *
     * @param val   按键位掩码值
     * @param state 按键状态
     */
    static void parseMCUTOARM_1(byte val, byte state) {
        switch (val) {
            case KEY_10_RUN_STOP:  // 运行/停止按键
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2)  // 短按或长按第二阶段
                    ExternalKeysCommand.get().clickRunStop();  // 切换运行/停止状态
                break;
            case KEY_11_TOP_MENU:  // 顶部菜单按键
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2)  // 短按或长按第二阶段
                    ExternalKeysCommand.get().openTopSlip();  // 打开顶部滑动菜单
                break;
            case KEY_12_VERTICAL_CURSOR: {  // 垂直光标按键
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2) {  // 短按或长按第二阶段
                    PlaySound.getInstance().playButton();  // 播放按键音效
                    boolean ver = getVisibleCursorV();  // 获取垂直光标是否可见

                    {
                        int s = -1;  // 光标操作类型：-1=无操作，TYPE_OPEN=打开，TYPE_CLOSE=关闭
                        if(ver) {  // 垂直光标当前可见
                            boolean b= CacheUtil.get().getLastObjectIsCursor();  // 获取上一次焦点是否为光标
                            if (b==false){  // 上一次焦点非光标，切换到垂直光标
                                CacheUtil.get().setLastObjectIsCursor(true);  // 标记当前焦点为光标
//                                backState = BACKSTATE_VCURSOR;
                                BackStateUpdate(BACKSTATE_VCURSOR);  // 更新Back状态为垂直光标
                                ExternalKeysCommand.get().setCursorSelected(false,  // 选中垂直光标
                                        ExternalKeysCommand.get().getCursorSelected(false));
                                singleVCursor = CursorManage.getInstance().getVerIndex() != 3;  // 判断是否为单光标模式
                            }else {  // 上一次焦点已经是光标
                                int selectCursor = CursorManage.getInstance().getCurrSelectCursor();  // 获取当前选中的光标类型
                                switch (selectCursor) {
                                    case TChan.Cursor_row_1:  // 水平光标1~4
                                    case TChan.Cursor_row_2:
                                    case TChan.Cursor_row_3:
                                    case TChan.Cursor_row_4:
                                        CacheUtil.get().setLastObjectIsCursor(true);  // 标记当前焦点为光标
//                                        backState = BACKSTATE_VCURSOR;
//                                    ExternalKeysCommand.get().switchCursor(false);
                                        BackStateUpdate(BACKSTATE_VCURSOR);  // 更新Back状态为垂直光标
                                        ExternalKeysCommand.get().setCursorSelected(false,  // 选中垂直光标
                                                ExternalKeysCommand.get().getCursorSelected(false));
                                        singleVCursor = CursorManage.getInstance().getVerIndex() != 3;  // 判断是否为单光标模式

                                        break;
                                    case TChan.Cursor_col_1:  // 垂直光标1~4：关闭垂直光标
                                    case TChan.Cursor_col_2:
                                    case TChan.Cursor_col_3:
                                    case TChan.Cursor_col_4:
                                        s = ExternalKeysMsgCursor.TYPE_CLOSE;  // 设置操作类型为关闭
                                        break;
                                    default:
                                }
                            }
                        }else{  // 垂直光标当前不可见，打开垂直光标
                            s = ExternalKeysMsgCursor.TYPE_OPEN;  // 设置操作类型为打开
                        }
                        if(s >= 0){  // 有需要执行的光标操作
                            if (!MeasureManage.getInstance().isCursorTValueTrace()) {  // 非光标T值追踪模式
                                ExternalKeysCommand.get().clickCursor(false, s);  // 执行光标打开/关闭操作
                            }
                        }
                    }
                }
            }
            break;
            case KEY_13_CH3:  // 通道3按键
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2) {  // 短按或长按第二阶段
//                    Logger.d(TAG, "KEY_13_CH3," + state);
                    ExternalKeysCommand.get().clickRightChannel(3);  // 切换CH3通道开关
                }
                break;
            case KEY_14_POSITION_DOWN: {  // 位置下旋钮按下：通道垂直位置下移
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                int count = state == TYPE_KEY_PRESS ? 1 : getPressLongSpeed(1);  // 计算步进数
                switch (WorkModeManage.getInstance().getmWorkMode()) {  // 根据工作模式处理
                    case IWorkMode.WorkMode_XY: {  // XY模式：移动CH2垂直位置
                        if (WaveManage.get().getCurCh() != TChan.Ch2) {  // 当前通道非CH2
                            WaveManage.get().setSelectCursor(TChan.Ch2);  // 切换选中通道为CH2
                        }
                        ExternalKeysCommand.get().moveChannelPosition(false, count);  // 向下移动通道位置
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Ch2);  // 通知最后操作对象
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_XY.MOVE_UPDOWN);  // 通知垂直滑动
                        break;
                    }
                    case IWorkMode.WorkMode_YT:  // YT模式
                    case IWorkMode.WorkMode_YTZOOM: {  // YT缩放模式
                        ExternalKeysCommand.get().moveChannelPosition(false, count);  // 向下移动当前通道垂直位置
                        int chIdx = WaveManage.get().getCurCh();  // 获取当前通道索引
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, chIdx);  // 通知最后操作对象
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);  // 通知垂直滑动
                        break;
                    }
                }
            }
            break;
            case KEY_15_SCALE_DOWN:  // 刻度下旋钮按下：通道刻度减小
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                Log.d("handler","调整------1-------------------scale delay");  // 调试日志
                ExternalKeysCommand.get().moveChannelScale(true, state == TYPE_KEY_PRESS ? 1 : getPressLongSpeed(4));  // 通道刻度减小
                break;
            case KEY_16_SELECT_DOWN:  // 选择下旋钮按下
                SelectKey(false, KEY_SELECT_DOWN, state);  // 调用选择键通用处理方法
                break;
            case KEY_17_TRIGGER_DOWN: {  // 触发下旋钮按下：触发电平/值电平减小
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                int count = state == TYPE_KEY_PRESS ? 1 : getPressLongSpeed(1);  // 计算步进数

                if (TriggerValueVoltageLine.getInstance().isTriggerlevelActive()) {  // 当前激活触发电平
                    ExternalKeysCommand.get().moveTriggerLevel(false, count);  // 触发电平减小
                } else {  // 当前激活值电平
                    moveValueLevel(false, count);  // 值电平减小
                }
            }
            break;
        }
    }

    /**
     * 解析标准键盘第2字节按键（KEY_20~KEY_27）
     * <p>处理CAPTURE、BACK、CH_MENU、CH4、POSITION_RIGHT、SCALE_RIGHT、
     * SELECT_RIGHT、TRIGGER_RIGHT等按键事件。
     * 其中BACK按键逻辑最复杂，需要根据当前UI状态判断返回行为。</p>
     *
     * @param val   按键位掩码值
     * @param state 按键状态
     */
    static void parseMCUTOARM_2(byte val, byte state) {
        switch (val) {
            case KEY_20_CAPTURE:  // 截图按键
                if (state == TYPE_KEY_PRESS)  // 短按：截图
                    ExternalKeysCommand.get().clickScreenCapture();  // 执行屏幕截图
                else if (state == TYPE_KEY_PRESS_LONG_2) {  // 长按第二阶段：快速保存
                    ExternalKeysCommand.get().clickQuickSave();  // 执行快速保存
                }
                break;
            case KEY_21_BACK:  // 返回按键（逻辑最复杂）
                PlaySound.getInstance().playButton();  // 播放按键音效
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2) {  // 短按或长按第二阶段
                    boolean isSerialsTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);  // 获取串行总线文本模式状态
                    if (isSerialsTxt && ExternalKeysCommand.get().isDialog()==false && ExternalKeysCommand.get().isSlip()==false){  // 串行文本模式且无弹窗/滑动菜单
                        ExternalKeysUI.getInstance().setFocusViewVisible(false);  // 隐藏焦点视图
                        ExternalKeysCommand.get().EnterToYt();  // 从串行文本模式返回YT视图
                    }else if (ExternalKeysCommand.get().isRightLayoutLevel()) {  // 右侧值电平面板可见
                        ExternalKeysCommand.get().hideRightLayoutLevel();  // 隐藏右侧值电平面板
                    } else if (ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_TOPSCALE)  // 顶部刻度弹窗可见
                            || ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_BANDWIDTHHZ)) {  // 带宽弹窗可见
                        ExternalKeysManager.get().closeDialog();  // 关闭弹窗
                        new Handler().postDelayed(() -> {  // 延迟50ms隐藏焦点视图
                            ExternalKeysUI.getInstance().setFocusViewVisible(false);  // 隐藏焦点视图
                        }, 50);
                    }
//                当前为topSlip打开状态时，点击退出本级菜单，返回上一级菜单
                    else if (ExternalKeysCommand.get().isSlip() || CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) {  // 滑动菜单或串行文本模式打开
                        if (canPercentFlag) {  // 正在进行百分比调节
                            canPercentFlag = false;  // 取消百分比调节
                        } else {
                            ExternalKeysManager.get().moveBack();  // 返回上一级菜单
                        }
                    }
//                当前各种状态都不是时，则关闭通道选择预选框
                    else {  // 无弹窗/菜单时，切换Back焦点状态
                        if (WorkModeManage.getInstance().getmWorkMode()==IWorkMode.WorkMode_XY) break;  // XY模式下不处理
                        boolean vCursor = getVisibleCursorV();  // 获取垂直光标可见性
                        boolean hCursor = getVisibleCursorH();  // 获取水平光标可见性
                        boolean channelList = getVisibleChannelList();  // 获取通道列表可见性
                        boolean segment = getVisibleSegment();  // 获取分段控制可见性
                        boolean zoom = getVisibleZoom();  // 获取缩放可见性
                        if (!vCursor && !hCursor && !channelList && !zoom && !segment) {
                            //如果全都不可选则不做操作
                        } else {

                            boolean changed=false;  // Back状态是否发生变化
                            int beforeBackState=backState;  // 记录切换前的Back状态
                            if ((ExternalKeysManager.get().isFocusViewVisible())==false && (channelList || segment || zoom)) {  // 焦点不可见且有非光标可选项
                                //否则就切换可选项
                                changed = switchBackState();  // 切换Back状态到下一个可选项
                            }
                            BackStateUpdate(backState);  // 刷新Back状态优先级
                            if ((beforeBackState==BACKSTATE_HCURSOR || beforeBackState==BACKSTATE_VCURSOR) && beforeBackState!=backState ){  // 从光标状态切换到非光标状态
                                CacheUtil.get().setLastObjectIsCursor(false);  // 标记焦点非光标
                            }

//                            if (backState != BACKSTATE_CHLIST) {
//                                ExternalKeysUI.getInstance().setFocusViewVisible(false);
//                            }else if (backState==BACKSTATE_SEGMENT && !segment){
////                                ExternalKeysManager.get().moveBack();
//                            }
                            int selectCursor = 0;  // 当前选中光标索引（预留）
                            switch (backState) {
                                case BACKSTATE_CURSOR: {  // 光标状态（预留）
//                                    if (vCursor) {
//                                        if (singleVCursor) {
//                                            selectCursor = IWave.Cursor_col_1;
//                                        } else {
//                                            selectCursor = IWave.Cursor_col_3;
//                                        }
//                                    }
//                                    if (hCursor) {
//                                        if (singleHCursor) {
//                                            selectCursor = IWave.Cursor_row_1;
//                                        } else {
//                                            selectCursor = IWave.Cursor_row_3;
//                                        }
//                                    }
//                                    if (vCursor || hCursor) {
//                                        if (!ExternalKeysManager.get().isFocusViewVisible()) {
//                                            ExternalKeysUI.getInstance().showBackStateFocus(backState);
//                                        } else {
//                                            canPercentFlag = false;
//                                            ExternalKeysManager.get().moveBack();
//                                            if (ExternalKeysManager.get().isFocusViewVisible() == false) {
//                                                ExternalKeysUI.getInstance().showBackStateFocus(backState);
//                                            }
//                                        }
//                                    }
//                                    if (changed && ExternalKeysManager.get().isFocusViewVisible() == false) {
//                                        ExternalKeysUI.getInstance().showBackStateFocus(backState);
//                                    }
                                }
                                break;
                                case BACKSTATE_CHLIST: {  // 通道列表状态
                                    boolean visibleFocusControl = ExternalKeysUI.getInstance().isVisibleFocusControl();  // 获取焦点控件可见性
                                    if (channelList) {  // 通道列表可见
                                        if (!ExternalKeysManager.get().isFocusViewVisible()) {  // 焦点视图不可见
//                                            ExternalKeysManager.get().showViewPlace(ExternalKeysManager.LISTTYPE_CHANNELS);
                                            ExternalKeysUI.getInstance().showBackStateFocus(backState);  // 显示通道列表Back焦点
                                        } else {  // 焦点视图可见
                                            canPercentFlag = false;  // 取消百分比调节
                                            ExternalKeysManager.get().moveBack();  // 返回上一级
                                            if (ExternalKeysManager.get().isFocusViewVisible() == false) {  // 返回后焦点不可见
                                                ExternalKeysUI.getInstance().showBackStateFocus(backState);  // 重新显示Back焦点
                                            }
                                        }
                                    }
                                    if (changed && ExternalKeysManager.get().isFocusViewVisible() == false) {  // Back状态变化且焦点不可见
                                        ExternalKeysUI.getInstance().showBackStateFocus(backState);  // 显示Back焦点
                                        if (visibleFocusControl) {  // 焦点控件之前可见
                                            ExternalKeysUI.getInstance().setFocusControlBackVisible(true);  // 显示Back焦点控件
                                        }
                                    }
                                }
                                break;
                                case BACKSTATE_SEGMENT: {  // 分段控制状态
                                    boolean visibleFocusControl = ExternalKeysUI.getInstance().isVisibleFocusControl();  // 获取焦点控件可见性
                                    if (segment) {  // 分段控制可见
                                        if (!ExternalKeysManager.get().isFocusViewVisible()) {  // 焦点视图不可见
                                            ExternalKeysUI.getInstance().showBackStateFocus(backState);  // 显示分段Back焦点
                                        } else {  // 焦点视图可见
                                            canPercentFlag = false;  // 取消百分比调节
                                            ExternalKeysManager.get().moveBack();  // 返回上一级
                                            if (ExternalKeysManager.get().isFocusViewVisible() == false) {  // 返回后焦点不可见
                                                ExternalKeysUI.getInstance().showBackStateFocus(backState);  // 重新显示Back焦点
                                            }
                                        }
                                    }
                                    if (changed && ExternalKeysManager.get().isFocusViewVisible() == false) {  // Back状态变化且焦点不可见
                                        ExternalKeysUI.getInstance().showBackStateFocus(backState);  // 显示Back焦点
                                        if (visibleFocusControl) {  // 焦点控件之前可见
                                            ExternalKeysUI.getInstance().setFocusControlBackVisible(true);  // 显示Back焦点控件
                                        }
                                    }
                                }
                                break;
                                case BACKSTATE_ZOOMUP: {  // 缩放状态
//                                    if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {
//                                        int symbol = isRight ? 1 : -1;
//                                        WaveMaskLayer_YTZoom.getInstance().layerX_move(count * symbol);
//                                    }
                                    if (zoom) {  // 缩放可见
                                        if (!ExternalKeysManager.get().isFocusViewVisible()) {  // 焦点视图不可见
//                                            ExternalKeysManager.get().showViewPlace(ExternalKeysManager.LISTTYPE_CHANNELS);
                                            ExternalKeysUI.getInstance().showBackStateFocus(backState);  // 显示缩放Back焦点
                                        } else {  // 焦点视图可见
                                            canPercentFlag = false;  // 取消百分比调节
                                            ExternalKeysManager.get().moveBack();  // 返回上一级
                                            if (ExternalKeysManager.get().isFocusViewVisible() == false) {  // 返回后焦点不可见
                                                ExternalKeysUI.getInstance().showBackStateFocus(backState);  // 重新显示Back焦点
                                            }
                                        }
                                    }
                                    if (changed && ExternalKeysManager.get().isFocusViewVisible() == false) {  // Back状态变化且焦点不可见
                                        ExternalKeysUI.getInstance().showBackStateFocus(backState);  // 显示Back焦点
                                    }
                                }
                                break;

                            }

                        }
                    }
                }
                break;
            case KEY_22_CH_MENU:  // 通道菜单按键
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见
                    if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2) {  // 短按或长按第二阶段
                        int curTab = CacheUtil.get().getInt(CacheUtil.SERIAL_TXT_CURRTAB);  // 获取当前串行总线标签页
                        if (curTab == ISerialsWord.TYPE_S1) {  // S1标签页
                            ExternalKeysCommand.get().openRightSlip(TChan.S1);  // 打开S1右侧滑动菜单
                        } else if (curTab == ISerialsWord.TYPE_S2) {  // S2标签页
                            ExternalKeysCommand.get().openRightSlip(TChan.S2);  // 打开S2右侧滑动菜单
                        }
                    }
                    break;
                }
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2)  // 短按或长按第二阶段
                    ExternalKeysCommand.get().openRightCurSlip();  // 打开当前通道的右侧滑动菜单
                break;
            case KEY_23_CH4:  // 通道4按键
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2) {  // 短按或长按第二阶段
                    Logger.d(TAG, "KEY_23_CH4," + state);  // 打印调试日志

                    ExternalKeysCommand.get().clickRightChannel(4);  // 切换CH4通道开关
                }
                break;
            case KEY_24_POSITION_RIGHT: {  // 位置右旋钮按下：时基位置右移
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                int count = state == TYPE_KEY_PRESS ? 1 : getPressLongSpeed(1);  // 计算步进数
                switch (WorkModeManage.getInstance().getmWorkMode()) {  // 根据工作模式处理
                    case IWorkMode.WorkMode_XY:  // XY模式：移动CH1水平位置
                        if (WaveManage.get().getCurCh() != TChan.Ch1) {  // 当前通道非CH1
                            WaveManage.get().setSelectCursor(TChan.Ch1);  // 切换选中通道为CH1
                        }
                        ExternalKeysCommand.get().moveChannelPosition(false, count);  // 向右移动通道位置
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Ch1);  // 通知最后操作对象
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_XY.MOVE_LEFTRIGHT);  // 通知水平滑动
                        break;
                    case IWorkMode.WorkMode_YT:  // YT模式
                    case IWorkMode.WorkMode_YTZOOM:  // YT缩放模式
                        if (ExternalKeysCommand.get().moveTimeBasePosition(true, count)) {  // 时基位置右移
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.TriggerTime);  // 通知最后操作对象
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_LEFTRIGHT);  // 通知水平滑动
                        }
                        break;
                }
            }
            break;
            case KEY_25_SCALE_RIGHT:  // 刻度右旋钮按下：时基刻度减小
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                ExternalKeysCommand.get().moveTimeBaseScale(false, state == TYPE_KEY_PRESS ? 1 : KEY_PRESS_LONG_VAL);  // 时基刻度减小
                break;
            case KEY_26_SELECT_RIGHT:  // 选择右旋钮按下
                SelectKey(false, KEY_SELECT_RIGHT, state);  // 调用选择键通用处理方法
                break;
            case KEY_27_TRIGGER_RIGHT:  // 触发右旋钮按下：触发源切换
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                ExternalKeysCommand.get().clickTriggerSource(false);  // 向右切换触发源

                break;
        }
    }

    static boolean canPercentFlag = false;  // 百分比调节激活标志（Select键在CAN_PERCENT类型列表项上时翻转）
    static boolean canPercentTop = false;  // 百分比调节是否在列表上半部分（索引<12）

    /**
     * 修复/检测CAN百分比调节标志
     * <p>检查当前右侧滑动菜单中焦点所在项是否为CAN百分比类型，
     * 若是则翻转canPercentFlag标志并更新canPercentTop位置标志</p>
     *
     * @return true表示当前处于CAN百分比调节模式
     */
    static boolean fixCanPercentFlag() {
        if (ExternalKeysCommand.get().isSlipShow(MainViewGroup.RIGHTSLIP_S1)  // S1右侧滑动菜单可见
                && ExternalKeysManager.get().isFocusViewVisible()  // 焦点视图可见
                && Objects.equals(ExternalKeysNode.TYPE_CAN_PERCENT, ExternalKeysManager.get().getRightSerialsList(CacheUtil.S1)  // 当前焦点项类型为CAN百分比
                .get(ExternalKeysManager.get().getRightSerialsListIndex(CacheUtil.S1)).getType())) {
            canPercentTop = ExternalKeysManager.get().getRightSerialsListIndex(CacheUtil.S1) < 12;  // 判断是否在列表上半部分
            canPercentFlag = !canPercentFlag;  // 翻转百分比调节标志
            return canPercentFlag;  // 返回当前标志值
        } else if (ExternalKeysCommand.get().isSlipShow(MainViewGroup.RIGHTSLIP_S2)  // S2右侧滑动菜单可见
                && ExternalKeysManager.get().isFocusViewVisible()  // 焦点视图可见
                && Objects.equals(ExternalKeysNode.TYPE_CAN_PERCENT, ExternalKeysManager.get().getRightSerialsList(CacheUtil.S2)  // 当前焦点项类型为CAN百分比
                .get(ExternalKeysManager.get().getRightSerialsListIndex(CacheUtil.S2)).getType())) {
            canPercentTop = ExternalKeysManager.get().getRightSerialsListIndex(CacheUtil.S2) < 12;  // 判断是否在列表上半部分
            canPercentFlag = !canPercentFlag;  // 翻转百分比调节标志
            return canPercentFlag;  // 返回当前标志值
        } else if (ExternalKeysCommand.get().isSlipShow(MainViewGroup.RIGHTSLIP_S3)  // S3右侧滑动菜单可见
                && ExternalKeysManager.get().isFocusViewVisible()  // 焦点视图可见
                && Objects.equals(ExternalKeysNode.TYPE_CAN_PERCENT, ExternalKeysManager.get().getRightSerialsList(CacheUtil.S3)  // 当前焦点项类型为CAN百分比
                .get(ExternalKeysManager.get().getRightSerialsListIndex(CacheUtil.S3)).getType())) {
            canPercentTop = ExternalKeysManager.get().getRightSerialsListIndex(CacheUtil.S3) < 12;  // 判断是否在列表上半部分
            canPercentFlag = !canPercentFlag;  // 翻转百分比调节标志
            return canPercentFlag;  // 返回当前标志值
        } else if (ExternalKeysCommand.get().isSlipShow(MainViewGroup.RIGHTSLIP_S4)  // S4右侧滑动菜单可见
                && ExternalKeysManager.get().isFocusViewVisible()  // 焦点视图可见
                && Objects.equals(ExternalKeysNode.TYPE_CAN_PERCENT, ExternalKeysManager.get().getRightSerialsList(CacheUtil.S4)  // 当前焦点项类型为CAN百分比
                .get(ExternalKeysManager.get().getRightSerialsListIndex(CacheUtil.S4)).getType())) {
            canPercentTop = ExternalKeysManager.get().getRightSerialsListIndex(CacheUtil.S4) < 12;  // 判断是否在列表上半部分
            canPercentFlag = !canPercentFlag;  // 翻转百分比调节标志
            return canPercentFlag;  // 返回当前标志值
        }
        return false;  // 不在CAN百分比调节模式
    }


    /**
     * 解析标准键盘第3字节按键（KEY_30~KEY_37）
     * <p>处理AUTO、HORIZONTAL_CURSOR、CH1、MATH、POSITION_CENTER、
     * SCALE_CENTER、SELECT_CENTER、TRIGGER_CENTER等按键事件</p>
     *
     * @param val   按键位掩码值
     * @param state 按键状态
     */
    static void parseMCUTOARM_3(byte val, byte state) {
        switch (val) {
            case KEY_30_AUTO:  // 自动设置按键
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2)  // 短按或长按第二阶段
                    ExternalKeysCommand.get().clickAuto();  // 执行自动设置
                break;
            case KEY_31_HORIZONTAL_CURSOR: {  // 水平光标按键
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2) {  // 短按或长按第二阶段
                    PlaySound.getInstance().playButton();  // 播放按键音效
                    boolean hor = getVisibleCursorH();  // 获取水平光标是否可见

                    {
                        int s = -1;  // 光标操作类型：-1=无操作
                        if(hor){  // 水平光标当前可见
                            if (CacheUtil.get().getLastObjectIsCursor()==false){  // 上一次焦点非光标
//                                backState = BACKSTATE_HCURSOR;
                                BackStateUpdate(BACKSTATE_HCURSOR);  // 更新Back状态为水平光标
                                CacheUtil.get().setLastObjectIsCursor(true);  // 标记当前焦点为光标
                                ExternalKeysCommand.get().setCursorSelected(true,  // 选中水平光标
                                        ExternalKeysCommand.get().getCursorSelected(true));
                                singleHCursor = CursorManage.getInstance().getHorIndex() != 3;  // 判断是否为单光标模式
                            }else {  // 上一次焦点已经是光标
                                int selectCursor = CursorManage.getInstance().getCurrSelectCursor();  // 获取当前选中的光标类型
                                switch (selectCursor) {
                                    case TChan.Cursor_row_1:  // 水平光标1~4：关闭水平光标
                                    case TChan.Cursor_row_2:
                                    case TChan.Cursor_row_3:
                                    case TChan.Cursor_row_4:
                                        s = ExternalKeysMsgCursor.TYPE_CLOSE;  // 设置操作类型为关闭
                                        break;
                                    case TChan.Cursor_col_1:  // 垂直光标1~4：切换到水平光标
                                    case TChan.Cursor_col_2:
                                    case TChan.Cursor_col_3:
                                    case TChan.Cursor_col_4:
//                                        backState = BACKSTATE_HCURSOR;
                                        BackStateUpdate(BACKSTATE_HCURSOR);  // 更新Back状态为水平光标
                                        CacheUtil.get().setLastObjectIsCursor(true);  // 标记当前焦点为光标
                                        ExternalKeysCommand.get().setCursorSelected(true,  // 选中水平光标
                                                ExternalKeysCommand.get().getCursorSelected(true));
                                        singleHCursor = CursorManage.getInstance().getHorIndex() != 3;  // 判断是否为单光标模式
                                        break;
                                    default:
                                }
                            }
                        }else{  // 水平光标当前不可见，打开水平光标
                            s = ExternalKeysMsgCursor.TYPE_OPEN;  // 设置操作类型为打开
                        }
                        if(s >= 0) {  // 有需要执行的光标操作
                            ExternalKeysCommand.get().clickCursor(true, s);  // 执行光标打开/关闭操作
                        }
                    }
                }
            }
            break;
            case KEY_32_CH1:  // 通道1按键
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2) {  // 短按或长按第二阶段
                    Logger.d(TAG, "KEY_32_CH1," + state);  // 打印调试日志
                    ExternalKeysCommand.get().clickRightChannel(1);  // 切换CH1通道开关
                }
                break;
            case KEY_33_MATH:  // 数学运算按键
                Logger.e(TAG, "KEY_33_MATH=" + state);  // 打印错误级别日志
                if (state == TYPE_KEY_PRESS && !ExternalKeysCommand.get().isSerialsWordVisible()) {  // 短按且串行总线字不可见
//                    ExternalKeysCommand.get().clickRightMath(ExternalKeysMsgChannel.MATH1);
                    ExternalKeysCommand.get().clickAddRightMath();  // 添加数学运算通道
                }
//                else if (state == TYPE_KEY_PRESS_LONG_2) {
//                    ExternalKeysCommand.get().clickRightSerials(1);
//                }

                break;
            case KEY_34_POSITION_CENTER: {  // 位置中旋钮按下：通道位置居中/时基位置归50%
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                if (positionV) {  // 控制垂直位置模式
                    int chIdx = WaveManage.get().getCurCh();  // 获取当前通道索引
                    WaveManage.get().setCenterChY(chIdx);  // 通道垂直位置居中
                    switch (WorkModeManage.getInstance().getmWorkMode()) {  // 根据工作模式处理
                        case IWorkMode.WorkMode_XY: {  // XY模式
                            if (ChannelFactory.isDynamicCh(TChan.toFpgaChNo(chIdx))) {  // 动态通道
                                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, chIdx);  // 通知最后操作对象
                                if (TChan.Ch1 == chIdx || TChan.Ch3 == chIdx) {  // CH1/CH3控制水平
                                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_XY.MOVE_LEFTRIGHT);  // 通知水平滑动
                                } else if (TChan.Ch2 == chIdx || TChan.Ch4 == chIdx) {  // CH2/CH4控制垂直
                                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_XY.MOVE_UPDOWN);  // 通知垂直滑动
                                }
                            }
                            break;
                        }
                        case IWorkMode.WorkMode_YT:  // YT模式
                        case IWorkMode.WorkMode_YTZOOM: {  // YT缩放模式
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, chIdx);  // 通知最后操作对象
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);  // 通知垂直滑动
                            break;
                        }
                    }
                } else {  // 控制水平位置模式
                    if (ExternalKeysCommand.get().moveTimeBasePosition50Percent()) {  // 时基位置归50%
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.TriggerTime);  // 通知最后操作对象
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_LEFTRIGHT);  // 通知水平滑动
                    }
                }
            }
            break;
            case KEY_35_SCALE_CENTER:  // 刻度中旋钮按下：通道游标
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                ExternalKeysCommand.get().channelVernier();  // 切换通道游标模式
                break;
            case KEY_36_SELECT_CENTER:  // 选择中旋钮按下：确认/进入操作
                {

//                    当前为DialogOkCancel或者DialogOk窗口
                    if (ExternalKeysCommand.get().isDialogOkVisible()) {  // 确认弹窗可见
                        ExternalKeysManager.get().moveIntoDialogOkSelect();  // 确认弹窗选择操作
                    }
                    else
                    {
//                当前为dialogTopScale状态打开时，点击关闭dialog
                        if (ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_TOPSCALE)  // 顶部刻度弹窗可见
                                || ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_BANDWIDTHHZ)) {  // 带宽弹窗可见
                            if (ExternalKeysManager.get().showViewIfGone()) {  // 焦点视图已消失则重新显示
                                return;  // 直接返回，不执行后续操作
                            }
                            ExternalKeysManager.get().closeDialog();  // 关闭弹窗
                        }
////                当前为参考选择窗口时，点击选择可选参考数据
//                    else if (ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_REFRECALL)) {
//                        ExternalKeysCommand.get().moveRefRecall(DialogRefRecall.ACTION_REFRECALL_FINISH, 1);
//                        ExternalKeysManager.get().moveBackNoClick();
//                    }

//                当前为topSlip打开状态时，点击选中当前选项，并进入下一级菜单
                        else if (ExternalKeysCommand.get().isSlip()  // 滑动菜单打开
                                || CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) {  // 串行文本模式
                            ExternalKeysManager.get().moveInto();  // 进入当前选中项的下一级
                        }
//                    当前各种状态都不是时，则点击通道选择预选框
                        else {  // 无弹窗/菜单时，根据Back状态切换操作

                            {
                                //Log.d(TAG,"backState:" +backState +",singleHCursor:" +singleHCursor);
                                if (backState == BACKSTATE_VCURSOR) {  // 垂直光标模式
                                    singleVCursor = !singleVCursor;  // 切换单/双垂直光标
                                    if (singleVCursor) {  // 切换为单光标
                                        ExternalKeysCommand.get().setCursorSelected(false, TChan.Cursor_col_1);  // 选中垂直光标1
                                    } else {  // 切换为双光标
                                        ExternalKeysCommand.get().setCursorSelected(false, TChan.Cursor_col_4);  // 选中垂直光标4
                                    }
                                } else if (backState == BACKSTATE_HCURSOR) {  // 水平光标模式
                                    singleHCursor = !singleHCursor;  // 切换单/双水平光标
                                    if (singleHCursor) {  // 切换为单光标
                                        ExternalKeysCommand.get().setCursorSelected(true, TChan.Cursor_row_1);  // 选中水平光标1
                                    } else {  // 切换为双光标
                                        ExternalKeysCommand.get().setCursorSelected(true, TChan.Cursor_row_4);  // 选中水平光标4
                                    }
                                } else if (backState == BACKSTATE_CHLIST) {  // 通道列表模式
                                    ExternalKeysManager.get().moveIntoChannelsSelect();  // 进入通道选择确认
                                } else if (backState == BACKSTATE_SEGMENT) {  // 分段控制模式
                                    ExternalKeysManager.get().moveIntoSegment();  // 进入分段选择确认
                                }
                            }
                        }
                    }
                }
                break;
            case KEY_37_TRIGGER_CENTER: {  // 触发中旋钮按下：时间游标
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                ExternalKeysCommand.get().timeVernier();  // 切换时间游标模式

//                if (ExternalKeysCommand.get().isSerialsWordVisible()) {
//                    break;
//                }
//                TriggerValueVoltageLine triggerValueVoltageLine = TriggerValueVoltageLine.getInstance();
//                if (state == TYPE_KEY_PRESS) {
//                    if (triggerValueVoltageLine.isTriggerlevelActive()) {
//                        ExternalKeysCommand.get().clickTriggerLevelCenter();
//                    } else {
//                        ExternalKeysCommand.get().clickValueLevelCenter();
//                    }
//                } else if (state == TYPE_KEY_PRESS_LONG_2) {
//                    triggerValueVoltageLine.switchTriggerValueLevel();
//                }
            }


            break;
        }
    }

    //    static boolean bKey37TriggerCenterTriggerValid = true;
    //TODO 需要匹配新的key协议
    /**
     * 解析标准键盘第4字节按键（KEY_40~KEY_47）
     * <p>处理REF、TOUCH_OFF、HOME、POSITION_UP、SCALE_UP、
     * SELECT_UP、TRIGGER_UP等按键事件</p>
     *
     * @param val   按键位掩码值
     * @param state 按键状态
     */
    static void parseMCUTOARM_4(byte val, byte state) {
        Logger.d(TAG,"val:" + Integer.toHexString(val) + ",state:" + state);  // 打印调试日志：按键值和状态
        switch (val) {
            case KEY_40_REF:  // 参考波形按键
                if (state == TYPE_KEY_PRESS && !ExternalKeysCommand.get().isSerialsWordVisible()) {  // 短按且串行总线字不可见
//                    ExternalKeysCommand.get().clickRightRef(ExternalKeysMsgChannel.REF1);
                    ExternalKeysCommand.get().clickAddRightRef();  // 添加参考波形通道
                }
//                else if (state == TYPE_KEY_PRESS_LONG_2) {
//                    ExternalKeysCommand.get().clickRightSerials(2);
//                }
                break;
            case KEY_41_TOUCH_OFF: {  // 触摸锁定按键
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2) {  // 短按或长按第二阶段
                    boolean bled = false;  // LED状态变量
                    bled=LockScreenUtils.isLockScreen(App.get().getApplicationContext());  // 获取当前锁屏状态
//                    if (ScreenControls.getInstance().isLockScreen(ScreenControls.LOCK_KEY)) {
//                        ScreenControls.getInstance().unLockScreen(ScreenControls.LOCK_KEY);
//                        bled = false;
//                    } else {
//                        ScreenControls.getInstance().lockScreen(ScreenControls.LOCK_KEY);
//                        bled = true;
//                    }
                    sendExternalToMcuMsg(bled ? ExternalKeysMsg_ToMCU.STATE_LED_ON : ExternalKeysMsg_ToMCU.STATE_LED_OFF);  // 发送LED控制消息：已锁屏亮灯，未锁屏灭灯
                }
            }
            break;
            case KEY_42_HOME:  // 主页按键
                Logger.d(TAG,"val:HOME" + Integer.toHexString(val) + ",state:" + state);  // 打印调试日志
                if (state == TYPE_KEY_PRESS || state == TYPE_KEY_PRESS_LONG_2 ||state == TYPE_KEY_PRESS_LONG) {  // 短按、长按第二阶段或长按
                    Logger.d(TAG,"val:test" + Integer.toHexString(val) + ",state:" + state);  // 打印调试日志
                    RxBus.getInstance().post(RxEnum.MSG_HIDE_KEYBOARD,false);  // 通知隐藏键盘

                    PlaySound.getInstance().playButton();  // 播放按键音效
                    if(state == TYPE_KEY_PRESS_LONG_2 || state == TYPE_KEY_PRESS_LONG){  // 长按：扩展键盘切换锁屏
                        if(HardwareProduct.isExtKeyboard()){  // 当前为扩展键盘型号
                            sendExternalToMcuMsg(LockScreenUtils.isLockScreen(App.get().getBaseContext()) ? ExternalKeysMsg_ToMCU.STATE_LED_ON : ExternalKeysMsg_ToMCU.STATE_LED_OFF);  // 发送锁屏LED状态
                            break;  // 扩展键盘长按仅切换锁屏，不执行保存
                        }
                    }

                    SaveManage.getInstance().saveToDefaultSaveName();  // 保存到默认文件名
                    SaveManage.getInstance().saveToOtherSaveName();  // 保存到其他文件名
//                    ExternalKeysCommand.get().clickHome();
                }
                break;

            case KEY_44_POSITION_UP: {  // 位置上旋钮按下：通道垂直位置上移
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                int count = state == TYPE_KEY_PRESS ? 1 : getPressLongSpeed(1);  // 计算步进数

                switch (WorkModeManage.getInstance().getmWorkMode()) {  // 根据工作模式处理
                    case IWorkMode.WorkMode_XY: {  // XY模式：移动CH2垂直位置
                        if (WaveManage.get().getCurCh() != TChan.Ch2) {  // 当前通道非CH2
                            WaveManage.get().setSelectCursor(TChan.Ch2);  // 切换选中通道为CH2
                        }
                        ExternalKeysCommand.get().moveChannelPosition(true, count);  // 向上移动通道位置
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Ch2);  // 通知最后操作对象
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_XY.MOVE_UPDOWN);  // 通知垂直滑动
                        break;
                    }
                    case IWorkMode.WorkMode_YT:  // YT模式
                    case IWorkMode.WorkMode_YTZOOM: {  // YT缩放模式
                        ExternalKeysCommand.get().moveChannelPosition(true, count);  // 向上移动当前通道垂直位置
                        int chIdx = WaveManage.get().getCurCh();  // 获取当前通道索引
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, chIdx);  // 通知最后操作对象
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);  // 通知垂直滑动
                        break;
                    }
                }
            }
            break;
            case KEY_45_SCALE_UP:  // 刻度上旋钮按下：通道刻度增大
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                Log.d("handler","调整------------2-------------scale delay");  // 调试日志
                ExternalKeysCommand.get().moveChannelScale(false, state == TYPE_KEY_PRESS ? 1 : getPressLongSpeed(4));  // 通道刻度增大
                break;
            case KEY_46_SELECT_UP:  // 选择上旋钮按下
                SelectKey(true, KEY_SELECT_UP, state);  // 调用选择键通用处理方法
                break;
            case KEY_47_TRIGGER_UP: {  // 触发上旋钮按下：触发电平/值电平增大
                if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                    break;
                }
                int count = state == TYPE_KEY_PRESS ? 1 : getPressLongSpeed(1);  // 计算步进数

                if (TriggerValueVoltageLine.getInstance().isTriggerlevelActive()) {  // 当前激活触发电平
                    ExternalKeysCommand.get().moveTriggerLevel(true, count);  // 触发电平增大
                } else {  // 当前激活值电平
                    moveValueLevel(true, count);  // 值电平增大
                }
            }
            break;
        }
    }

    /**
     * 执行值电平移动（无参版本，消费累积的步进值）
     * <p>在同步锁保护下读取并清零累积的步进值，然后执行值电平移动操作</p>
     */
    static void moveValueLevel() {
        int count = 0;  // 累积步进值
        synchronized (lock) {  // 加锁保护共享变量
            count = valueLevelCount;  // 读取累积步进值
            if (count > 0) {  // 有累积的步进值
                valueLevelCount = 0;  // 清零累积步进值
            }
        }
        if (count > 0) {  // 有需要执行的移动
            ExternalKeysCommand.get().moveValueLevel(bUpMoveValueLevel, count);  // 执行值电平移动
        }
    }

    /**
     * 累积或执行值电平移动
     * <p>如果移动方向与当前累积方向一致，则累加步进值；
     * 如果方向改变，则先执行当前累积的移动，再重置方向和步进值</p>
     *
     * @param isUp  true=向上移动，false=向下移动
     * @param count 移动步进数
     */
    static void moveValueLevel(boolean isUp, int count) {
        boolean bValue = false;  // 方向是否改变的标志
        synchronized (lock) {  // 加锁保护共享变量
            if (bUpMoveValueLevel == isUp) {  // 方向一致
                valueLevelCount += count;  // 累加步进值
            } else {  // 方向改变
                bValue = true;  // 标记方向改变
            }
        }
        if (bValue) {  // 方向改变时
            moveValueLevel();  // 先执行当前累积的移动
            synchronized (lock) {  // 加锁保护共享变量
                bUpMoveValueLevel = isUp;  // 更新移动方向
                valueLevelCount = count;  // 重置步进值
            }
        }
    }

    static Object lock = new Object();  // 值电平移动操作的同步锁
    static volatile boolean bUpMoveValueLevel = false;  // 当前值电平移动方向（true=上，false=下）
    static volatile int valueLevelCount = 0;  // 累积的值电平移动步进值

    static boolean bKeyboradCandidatesWord = false;  // 当前是否正在操作候选词列表

    /**
     * 选择键通用处理方法
     * <p>根据当前UI状态分发选择键操作到不同的处理器：
     * 确认弹窗、刻度弹窗、候选词列表、数字选择器、浮动键盘、
     * 串行文本列表、滑动菜单导航、光标/通道列表/分段/缩放移动</p>
     *
     * @param rightWard true=向右/向上方向，false=向左/向下方向
     * @param key       按键方向标识（KEY_SELECT_UP/DOWN/LEFT/RIGHT）
     * @param state     按键状态
     */
    static void SelectKey(boolean rightWard, int key, byte state) {
        Log.d(TAG, "SelectKey() called with: rightWard = [" + rightWard + "], key = [" + key + "]");  // 打印调试日志
        bKeyboradCandidatesWord = false;  // 重置候选词操作标志
        int count = state == TYPE_KEY_PRESS ? 1 : KEY_PRESS_LONG_VAL;  // 计算步进数：短按为1，长按为常量

        if (ExternalKeysCommand.get().isDialogOkVisible()) {  // 确认弹窗可见
            canPercentFlag = false;  // 取消百分比调节
            ExternalKeysManager.get().moveDialogOkSelect(!rightWard, count);  // 移动确认弹窗选择项
        } else if (ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_TOPSCALE)  // 顶部刻度弹窗可见
                || ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_BANDWIDTHHZ)) {  // 带宽弹窗可见

            ExternalKeysCommand.get().moveTopDialogScale(key == KEY_SELECT_UP || key == KEY_SELECT_DOWN, !rightWard, count);  // 移动顶部刻度弹窗值
        } else if (ExternalKeysCommand.get().isKeyboradCandidatesWordShow() && (key == KEY_SELECT_UP || key == KEY_SELECT_DOWN)) {  // 候选词列表可见且上下方向键

            bKeyboradCandidatesWord = true;  // 标记正在操作候选词
            if (rightWard) {  // 向右方向
                ExternalKeysCommand.get().moveKeyBoardCandidatesWord(TopDialogCandidatesWord.ACTION_CANDIDATES_LEFT, count);  // 候选词左移
            } else {  // 向左方向
                ExternalKeysCommand.get().moveKeyBoardCandidatesWord(TopDialogCandidatesWord.ACTION_CANDIDATES_RIGHT, count);  // 候选词右移
            }
        }
//                    当前为numberPicker窗口时，移动改变当前选中项的值
        else if (ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_NUMBERPICKER) && (key == KEY_SELECT_UP || key == KEY_SELECT_DOWN)) {  // 数字选择器弹窗且上下方向键

            ExternalKeysManager.get().changeNumberPickerItem(rightWard);  // 改变数字选择器值
        }

        else if (ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD) && (key == KEY_SELECT_UP || key == KEY_SELECT_DOWN)) {  // 浮动键盘弹窗且上下方向键
            ExternalKeysManager.get().changeNumberKeyboardItem(rightWard);  // 改变浮动键盘值
        }
//                当前为文本模式时，移动上下调节列表
        else if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT) && (key == KEY_SELECT_UP || key == KEY_SELECT_DOWN)  // 串行文本模式且上下方向键
                && !ExternalKeysCommand.get().isSlip()) {  // 滑动菜单未打开

            ExternalKeysCommand.get().moveSerialsWordList(rightWard ? count : count * -1);  // 移动串行文本列表
        }
//                当前为可选中光标状态时，移动垂直方向的光标
        else if (ExternalKeysCommand.get().isSlip() || CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) {  // 滑动菜单或串行文本模式打开
            if (canPercentFlag) {  // 正在进行CAN百分比调节
                boolean s1Show = ExternalKeysCommand.get().isSlipShow(MainViewGroup.RIGHTSLIP_S1);  // S1滑动菜单是否可见
                if (key == KEY_SELECT_UP || key == KEY_SELECT_LEFT) {  // 上/左方向键
                    ExternalKeysCommand.get().moveRightCanPercent(s1Show, false, canPercentTop, count);  // 百分比减小
                } else if (key == KEY_SELECT_DOWN || key == KEY_SELECT_RIGHT) {  // 下/右方向键
                    ExternalKeysCommand.get().moveRightCanPercent(s1Show, true, canPercentTop, count);  // 百分比增大
                }
            } else {  // 正常菜单导航

                long t = SystemClock.elapsedRealtime();  // 获取当前时间
                if(t - moveMenuTs > 25) {  // 距上次移动超过25ms，防止过快移动
                    count = state == TYPE_KEY_PRESS ? 1 : getPressLongSpeed(1);  // 重新计算步进数
                    switch (key) {
                        case KEY_SELECT_UP:  // 上方向键
                            ExternalKeysManager.get().moveUp(count);  // 菜单上移
                            break;
                        case KEY_SELECT_DOWN:  // 下方向键
                            ExternalKeysManager.get().moveDown(count);  // 菜单下移
                            break;
                        case KEY_SELECT_LEFT:  // 左方向键
                            ExternalKeysManager.get().moveLeft(count);  // 菜单左移
                            break;
                        case KEY_SELECT_RIGHT:  // 右方向键
                            ExternalKeysManager.get().moveRight(count);  // 菜单右移
                            break;
                    }
                    moveMenuTs = t;  // 更新上次菜单移动时间戳
                }
            }

        } else {  // 无弹窗/菜单时，根据Back状态移动光标/通道/分段/缩放
            boolean vCursor = getVisibleCursorV();  // 获取垂直光标可见性
            boolean hCursor = getVisibleCursorH();  // 获取水平光标可见性
            boolean chList = getVisibleChannelList();  // 获取通道列表可见性
            boolean segment = getVisibleSegment();  // 获取分段控制可见性
            boolean zoom = getVisibleZoom();  // 获取缩放可见性
                Logger.d(TAG,"vCursor:" + vCursor + ",hCursor:" + hCursor + ",chList:" + chList + ",zoom:" + zoom + ",key:" + key);  // 打印调试日志
//            if ((vCursor || hCursor) && !chList && !zoom && !segment
//                    && ((vCursor && (key == KEY_SELECT_RIGHT || key == KEY_SELECT_LEFT))
//                    || (hCursor && (key == KEY_SELECT_UP || key == KEY_SELECT_DOWN)))
//            ) {
//                //只有垂直光标可操作时
//                //只有水平光标可操作时
//                backState = BACKSTATE_CURSOR;
//            } else if (!(vCursor || hCursor) && chList && !zoom && !segment && (key == KEY_SELECT_RIGHT || key == KEY_SELECT_LEFT)) {
//                //只有通道选择条可操作时
//                backState = BACKSTATE_CHLIST;
//            } else if (!(vCursor || hCursor) && !chList && zoom && !segment && (key == KEY_SELECT_RIGHT || key == KEY_SELECT_LEFT)) {
//                //只有zoom上半部分可操作时
//                backState = BACKSTATE_ZOOMUP;
//            } else if (!(vCursor || hCursor) && !chList && !zoom && !segment) {
//                //全部不可操作时，显示并移动通道选择条
//                backState = BACKSTATE_CHLIST;
//            } else if (!(vCursor || hCursor) && !chList && !zoom && segment && (key == KEY_SELECT_RIGHT || key == KEY_SELECT_LEFT)) {
//                backState = BACKSTATE_SEGMENT;
//            } //有两项以上可操作时，不修改backState
            backState=getBackState();  // 根据优先级获取当前Back状态

            if (backState==BACKSTATE_NULL){  // 无可操作对象
                //backState=BACKSTATE_CHLIST;
                return;  // 直接返回
            }

            if (backState != BACKSTATE_CHLIST && backState != BACKSTATE_SEGMENT) {  // 非通道列表/分段状态
                ExternalKeysUI.getInstance().setFocusViewVisible(false);  // 隐藏焦点视图
            }

            if ((((backState == BACKSTATE_CHLIST) || (backState == BACKSTATE_ZOOMUP)) /*&& (key == KEY_SELECT_RIGHT || key == KEY_SELECT_LEFT)*/)  // 通道列表或缩放状态
                    || (backState == BACKSTATE_CURSOR) || (backState == BACKSTATE_HCURSOR)  // 光标或水平光标状态
                    || (backState == BACKSTATE_SEGMENT)) {  // 分段控制状态
                editorMulMove(key, !rightWard, state);  // 调用编辑器多方向移动方法
            }
        }
    }

    static long moveMenuTs = 0;  // 上次菜单移动的时间戳，用于节流控制
    /**
     * 解析编码器（旋钮）旋转数据帧
     * <p>解析编码器旋转事件，根据编码器索引分发到对应的处理逻辑：
     * KNOB_1（菜单导航）、KNOB_2（光标/候选词导航）、
     * KNOB_HORIZONTAL_POSITION（水平位置）、KNOB_TRIGGER_LEVEL（触发电平）、
     * KNOB_HORIZONTAL_SCALE（水平刻度）、KNOB_VERTICAL_POSITION（垂直位置）、
     * KNOB_VERTICAL_SCALE（垂直刻度）</p>
     *
     * @param bytes 编码器数据帧，bytes[0]=编码器索引，bytes[1]=方向(高位)+旋转量(低7位)
     */
    static void parseEditor(byte[] bytes){
        if (ScreenControls.getInstance().isExternalKey()) return;  // 外部按键锁定时忽略
        if (ExternalKeysCommand.get().isUserTouch()) return;  // 用户正在触摸操作时忽略
        ExternalKeysCommand.get().hideCenterMenuAndCenterHalf();  // 隐藏中心菜单和半屏
        AutoSave.getInstance().setUserInput(true);  // 标记有用户输入，触发自动保存
        int idx = bytes[0];  // 编码器索引
        int dir = (bytes[1] & 0x80) == 0 ? KNOB_CLOCKWISE : KNOB_ANTICLOCKWISE;  // 提取旋转方向：最高位0=顺时针，1=逆时针
        int val = bytes[1] & 0x7F;  // 提取旋转量（低7位）
        boolean rightWard = true;  // 方向标志，默认为右
        if (dir == KNOB_CLOCKWISE) {  // 顺时针方向
            rightWard = false;  // 顺时针为左方向
        }
        if(val > 0){  // 旋转量大于0才处理
            switch (idx){
                case KNOB_2: {  // 编码器2：光标/候选词导航
                    Log.d(Tag.Debug, String.format("ExternalKeysProtocol.parseEditor: KNOB2" ));  // 打印调试日志
                    if (ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_TOPSCALE)  // 顶部刻度弹窗可见
                    || ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_BANDWIDTHHZ)) {  // 带宽弹窗可见
                        ExternalKeysCommand.get().moveTopDialogScale(true, !rightWard, val);  // 移动顶部刻度弹窗值
                    }

                    else if (ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)) {  // 文本键盘弹窗可见
                        if (rightWard) {  // 右方向
                            ExternalKeysCommand.get().moveKeyBoardCandidatesWord(TopDialogCandidatesWord.ACTION_CANDIDATES_LEFT, val);  // 候选词左移
                        } else {  // 左方向
                            ExternalKeysCommand.get().moveKeyBoardCandidatesWord(TopDialogCandidatesWord.ACTION_CANDIDATES_RIGHT, val);  // 候选词右移
                        }
                    }

                    else if (ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_NUMBERPICKER)) {  // 数字选择器弹窗可见
                        ExternalKeysManager.get().changeNumberPickerItem(rightWard);  // 改变数字选择器值
                    }

                    else if (ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD)) {  // 浮动键盘弹窗可见
                        ExternalKeysManager.get().changeNumberKeyboardItem(rightWard);  // 改变浮动键盘值
                    }

                    else if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) {  // 串行文本模式
                        ExternalKeysCommand.get().moveSerialsWordList(rightWard ? val : val * -1);  // 移动串行文本列表
                    }

                    else {  // 无弹窗/菜单时，根据可见性判断Back状态

                        {
                            boolean vCursor = getVisibleCursorV();  // 获取垂直光标可见性
                            boolean hCursor = getVisibleCursorH();  // 获取水平光标可见性
                            boolean chList = getVisibleChannelList();  // 获取通道列表可见性
                            boolean zoom = getVisibleZoom();  // 获取缩放可见性
                            boolean segment=getVisibleSegment();  // 获取分段控制可见性
                            if (vCursor && hCursor && !chList && !zoom && !segment){  // 垂直和水平光标同时可见
                                int i=CursorManage.getInstance().getCurrSelectCursor();  // 获取当前选中光标
                                if (TChan.isCursorCol4(i)){  // 选中垂直光标4
                                    backState=BACKSTATE_VCURSOR;  // 切换为垂直光标状态
                                }else if (TChan.isCursorRow4(i)){  // 选中水平光标4
                                    backState=BACKSTATE_HCURSOR;  // 切换为水平光标状态
                                }

                            }else if (vCursor && !hCursor && !chList && !zoom && !segment) {
                                //只有垂直光标可操作时
                                backState = BACKSTATE_VCURSOR;  // 设置为垂直光标状态
                            } else if (!vCursor && hCursor && !chList && !zoom && !segment) {
                                //只有水平光标可操作时
                                backState = BACKSTATE_HCURSOR;  // 设置为水平光标状态
                            } else if (!vCursor && !hCursor && chList && !zoom && !segment) {
                                //只有通道选择条可操作时
                            } else if (!vCursor && !hCursor && !chList && zoom && !segment) {
                                //只有zoom上半部分可操作时
                            }else if (!vCursor && !hCursor && !chList && !zoom && segment){
                                //只有分段控制可操作时
                            } else if (!vCursor && !hCursor && !chList && !zoom && !segment) {
                                //全部不可操作时，显示并移动通道选择条
                            }//有两项以上可操作时，不修改backState
                            if (backState == BACKSTATE_VCURSOR || backState == BACKSTATE_HCURSOR) {  // 光标状态
                                editorMulMove2(false, !rightWard, val);  // 执行光标移动（非菜单导航模式）
                            }
                        }
                    }

                }
                break;
                case KNOB_1: {  // 编码器1：菜单导航旋钮
                    Log.d(Tag.Debug, String.format("ExternalKeysProtocol.parseEditor: KNOB1" ));  // 打印调试日志
                    if (ExternalKeysCommand.get().isDialogOkVisible()) {  // 确认弹窗可见

                        ExternalKeysManager.get().moveDialogOkSelect(!rightWard, val);  // 移动确认弹窗选择项
                    }
//                当前为可选中光标状态时，移动垂直方向的光标
                    else {  // 非确认弹窗

//                  当前为时间调节窗口时，移动微调光标
                        if (ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_TOPSCALE)  // 顶部刻度弹窗可见
                                || ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_BANDWIDTHHZ)) {  // 带宽弹窗可见
                            ExternalKeysCommand.get().moveTopDialogScale(false, !rightWard, val);  // 移动顶部刻度弹窗值（纵向模式）
                        }
////                当前为参考波形选择窗口时，移动选择可选参考数据
//                    else if (ExternalKeysCommand.get().isDialogShow(MainViewGroup.DIALOG_REFRECALL)) {
//                        if (rightWard) {
//                            ExternalKeysCommand.get().moveRefRecall(DialogRefRecall.ACTION_REFRECALL_DOWN, count);
//                        } else {
//                            ExternalKeysCommand.get().moveRefRecall(DialogRefRecall.ACTION_REFRECALL_UP, count);
//                        }
//                    }

//                当前为任意Slip打开状态时，在当前等级菜单中移动穿行
                        else if (ExternalKeysCommand.get().isSlip() || CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) {  // 滑动菜单或串行文本模式

                            long t = SystemClock.elapsedRealtime();  // 获取当前时间
                            if(t - moveMenuTs > 25){  // 距上次移动超过25ms
                                val = 1;  // 固定步进为1
                                if (rightWard) {  // 右方向
                                    ExternalKeysManager.get().moveLeft(getPressLongSpeed(1));  // 菜单左移
                                } else {  // 左方向
                                    ExternalKeysManager.get().moveRight(getPressLongSpeed(1));  // 菜单右移
                                }
                                moveMenuTs = t;  // 更新时间戳
                            }


                        }
//                    当前各种状态都不是时，则移动通道选择预选框
                        else {  // 无弹窗/菜单

                            {
                                boolean vCursor = getVisibleCursorV();  // 获取垂直光标可见性
                                boolean hCursor = getVisibleCursorH();  // 获取水平光标可见性
                                boolean chList = getVisibleChannelList();  // 获取通道列表可见性
                                boolean zoom = getVisibleZoom();  // 获取缩放可见性
                                boolean segment=getVisibleSegment();  // 获取分段控制可见性
                                backState=getBackState();  // 根据优先级获取当前Back状态
//                                if (vCursor && !hCursor && !chList && !zoom && !segment) {
//                                    //只有垂直光标可操作时
//                                    backState = BACKSTATE_VCURSOR;
//                                } else if (!vCursor && hCursor && !chList && !zoom && !segment) {
//                                    //只有水平光标可操作时
//                                    backState = BACKSTATE_HCURSOR;
//                                } else if (!vCursor && !hCursor && chList && !zoom && !segment) {
//                                    //只有通道选择条可操作时
//                                    backState = BACKSTATE_CHLIST;
//                                } else if (!vCursor && !hCursor && !chList && zoom && !segment) {
//                                    //只有zoom上半部分可操作时
//                                    backState = BACKSTATE_ZOOMUP;
//                                }else if (!vCursor && !hCursor && !chList && !zoom && segment){
//                                    backState=BACKSTATE_SEGMENT;
//                                } else if (!vCursor && !hCursor && !chList && !zoom && !segment) {
//                                    //全部不可操作时，显示并移动通道选择条
//                                    backState = BACKSTATE_CHLIST;
//                                }//有两项以上可操作时，不修改backState
                                editorMulMove2(true, !rightWard, val);  // 执行编辑器多方向移动（菜单导航模式）
                            }
                        }
                    }
                }
                break;

                case KNOB_HORIZONTAL_POSITION:  // 水平位置旋钮
                {
                    if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                        break;
                    }
                    val = (int)(Math.ceil((double) val * val / 2) + 0.1);  // 加速算法：旋转量平方/2取整
                    switch (WorkModeManage.getInstance().getmWorkMode()) {  // 根据工作模式处理
                        case IWorkMode.WorkMode_XY:  // XY模式：移动CH1水平位置
                            if (WaveManage.get().getCurCh() != TChan.Ch1) {  // 当前通道非CH1
                                WaveManage.get().setSelectCursor(TChan.Ch1);  // 切换选中通道为CH1
                            }
                            ExternalKeysCommand.get().moveChannelPosition(dir == KNOB_ANTICLOCKWISE, val);  // 移动通道水平位置
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Ch1);  // 通知最后操作对象
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_XY.MOVE_LEFTRIGHT);  // 通知水平滑动
                            break;
                        case IWorkMode.WorkMode_YT:  // YT模式
                        case IWorkMode.WorkMode_YTZOOM:  // YT缩放模式
                            if (ExternalKeysCommand.get().moveTimeBasePosition(dir == KNOB_CLOCKWISE, val)) {  // 移动时基水平位置
                                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.TriggerTime);  // 通知最后操作对象
                                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_LEFTRIGHT);  // 通知水平滑动
                            }
                            break;
                    }
                }
                    break;
                case KNOB_TRIGGER_LEVEL:  // 触发电平旋钮
                    if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                        break;
                    }

                    val = (int)(Math.ceil((double) val * val / 2) + 0.1);  // 加速算法：旋转量平方/2取整

                    int triggerType = TriggerFactory.getTriggerType();  // 获取当前触发类型
                    if(triggerType == Trigger.TRIG_TYPE_VIDEO){  // 视频触发类型
                        TriggerVideo triggerVideo = (TriggerVideo) TriggerFactory.getTriggerObj(Trigger.TRIG_TYPE_VIDEO);  // 获取视频触发对象
                        if(triggerVideo != null){  // 对象非空
                            if(triggerVideo.getVideoTrigger() == TriggerVideo.VIDEO_TRIGGER_LINE){  // 行触发模式
                                Command_Trigger_Video trigger_video = Command.get().getTrigger_video();  // 获取视频触发命令
                                trigger_video.Line(trigger_video.LineQ() + (dir == KNOB_CLOCKWISE ? val : -1),true);  // 调整行号
                            }
                        }

                    }else {  // 非视频触发类型
                        if (TriggerValueVoltageLine.getInstance().isTriggerlevelActive()) {  // 当前激活触发电平
                            ExternalKeysCommand.get().moveTriggerLevel(dir == KNOB_CLOCKWISE, val);  // 移动触发电平
                        } else {  // 当前激活值电平
                            moveValueLevel(dir == KNOB_CLOCKWISE, val);  // 移动值电平
                        }
                    }

                    break;
                case KNOB_HORIZONTAL_SCALE:  // 水平刻度旋钮
                    if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                        break;
                    }
                    ExternalKeysCommand.get().moveTimeBaseScale(dir == KNOB_ANTICLOCKWISE, val);  // 调整水平时基刻度
                    break;
                case KNOB_VERTICAL_POSITION:  // 垂直位置旋钮
                {
                    if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                        break;
                    }
                    val = (int)(Math.ceil((double) val * val / 2) + 0.1);  // 加速算法：旋转量平方/2取整
                    switch (WorkModeManage.getInstance().getmWorkMode()) {  // 根据工作模式处理
                        case IWorkMode.WorkMode_XY: {  // XY模式：移动CH2垂直位置
                            if (WaveManage.get().getCurCh() != TChan.Ch2) {  // 当前通道非CH2
                                WaveManage.get().setSelectCursor(TChan.Ch2);  // 切换选中通道为CH2
                            }
                            ExternalKeysCommand.get().moveChannelPosition(dir == KNOB_ANTICLOCKWISE, val);  // 移动通道垂直位置
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Ch2);  // 通知最后操作对象
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_XY.MOVE_UPDOWN);  // 通知垂直滑动
                            break;
                        }
                        case IWorkMode.WorkMode_YT:  // YT模式
                        case IWorkMode.WorkMode_YTZOOM: {  // YT缩放模式
                            ExternalKeysCommand.get().moveChannelPosition(dir == KNOB_ANTICLOCKWISE, val);  // 移动当前通道垂直位置
                            int chIdx = WaveManage.get().getCurCh();  // 获取当前通道索引
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, chIdx);  // 通知最后操作对象
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);  // 通知垂直滑动
                            break;
                        }
                    }
                }
                    break;
                case KNOB_VERTICAL_SCALE:  // 垂直刻度旋钮
                    if (ExternalKeysCommand.get().isSerialsWordVisible()) {  // 串行总线字界面可见时忽略
                        break;
                    }
                    Log.d("handler","调整-----------3--------------scale delay");  // 调试日志
                    ExternalKeysCommand.get().moveChannelScale(dir == KNOB_CLOCKWISE, val);  // 调整通道垂直刻度
                    break;
            }
        }
    }
    /**
     * 解析MCU发送给ARM的按键数据帧（主入口方法）
     * <p>解析按键数据帧的每个位，识别按下的按键和按键类型，
     * 然后分发到对应的parseMCUTOARM_x方法处理。
     * 数据帧格式：bytes[0~4]为5个字节的按键位域，bytes[5]为按键类型</p>
     *
     * @param bytes MCU按键数据帧（6字节）
     */
    static void parseMCUTOARM(byte[] bytes) {
        //Log.d("Tag.Debug", String.format("ExternalKeysProtocol.parseMCUTOARM rec: %s", Arrays.toString(bytes) ));
        if (ScreenControls.getInstance().isExternalKey()){ Log.d(TAG,"parseMCUTOARM.isExternalKey");return;}  // 外部按键锁定时忽略
        if (ExternalKeysCommand.get().isUserTouch()){ Log.d(TAG,"parseMCUTOARM.isUserTouch");return;}  // 用户正在触摸操作时忽略
        ExternalKeysCommand.get().hideCenterMenuAndCenterHalf();  // 隐藏中心菜单和半屏
        if (isCompositeKey(bytes)) {Log.d(TAG,"parseMCUTOARM.isCompositeKey");return;}  // 组合键时忽略
        //Log.d("Tag.Debug", String.format("ExternalKeysProtocol.parseMCUTOARM parse: %s",Arrays.toString(bytes) ));

        AutoSave.getInstance().setUserInput(true);  // 标记有用户输入，触发自动保存
        for (int i = 0; i < 5; i++) {  // 遍历5个按键位域字节
            for (int j = 0; j < 8; j++) {  // 遍历每个字节的8个位
                if ((bytes[i] & (1 << j)) != 0) {  // 检测第j位是否被按下
                    switch (bytes[5]) {  // 根据按键类型分发
                        case TYPE_KEY_PRESS:  // 按键按下
                        case TYPE_KEY_PRESS_LONG_2:  // 长按第二阶段
                        case TYPE_KEY_PRESS_LONG:  // 长按
                            switch (i) {  // 根据字节索引分发
                                case 0:  // 第0字节按键
                                    parseMCUTOARM_0((byte) (1 << j), bytes[5]);  // 解析KEY_00~KEY_07
                                    break;
                                case 1:  // 第1字节按键
                                    parseMCUTOARM_1((byte) (1 << j), bytes[5]);  // 解析KEY_10~KEY_17
                                    break;
                                case 2:  // 第2字节按键
                                    parseMCUTOARM_2((byte) (1 << j), bytes[5]);  // 解析KEY_20~KEY_27
                                    break;
                                case 3:  // 第3字节按键
                                    parseMCUTOARM_3((byte) (1 << j), bytes[5]);  // 解析KEY_30~KEY_37
                                    break;
                                case 4:  // 第4字节按键
                                    parseMCUTOARM_4((byte) (1 << j), bytes[5]);  // 解析KEY_40~KEY_47
                                    break;
                            }
                            break;
                        case TYPE_KEY_PRESS_EXT:  // 扩展按键按下
                        case TYPE_KEY_PRESS_LONG_EXT:  // 扩展按键长按
                            switch (i){  // 根据字节索引分发
                                case 0:  // 扩展键盘第0字节
                                    parseMCUTOARM_10((byte) (1 << j), bytes[5]);  // 解析KEY_100~KEY_107
                                    break;
                                case 1:  // 扩展键盘第1字节
                                    parseMCUTOARM_11((byte) (1 << j), bytes[5]);  // 解析KEY_110~KEY_117
                                    break;
                                case 2:{  // 扩展键盘第2字节
                                    parseMCUTOARM_12((byte) (1 << j), bytes[5]);  // 解析KEY_126~KEY_127
                                }break;
                            }

                    }
                }
            }
        }

    }


    static long pressTs = 0;  // 上一次按键按下时的时间戳
    static long startTs = 0;  // 长按起始时间戳（用于计算加速）
    /**
     * 计算长按加速速度。
     * <p>当按键持续按下时，随着时间推移移动速度逐渐加快，最大加速到15。
     * 如果按住时间不超过100ms，则返回初始速度的3倍；超过100ms后按比例加速。</p>
     *
     * @param speed 基础速度值
     * @return 加速后的速度值，最大为15
     */
    private static int getPressLongSpeed(int speed) {

        long ts = SystemClock.elapsedRealtime();  // 获取当前系统启动后的经过时间
        if(ts - pressTs >  100){  // 距上次按键超过100ms，认为是新的按下序列
            startTs = ts;  // 重置长按起始时间
        }
        pressTs = ts;  // 更新上次按键时间戳
        ts = ts - startTs;  // 计算从长按开始到现在的持续时间
        if(ts > 100){  // 持续时间超过100ms，进入加速阶段
            ts = speed * ts / 100;  // 按比例计算加速后的速度
            if(ts > 15) {  // 速度上限为15
                ts = 15;  // 钳制最大速度
            }
            return (int)(ts);  // 返回加速后的速度
        }else{  // 持续时间不超过100ms，使用初始加速
            return speed * 3;  // 返回3倍基础速度作为初始加速
        }
    }
    /**
     * 获取垂直光标是否可见。
     * <p>根据当前工作模式（YT/XY），从缓存中读取对应的垂直光标可见性状态。</p>
     *
     * @return true表示垂直光标可见，false表示不可见
     */
    private static boolean getVisibleCursorV() {
        if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {  // 非XY模式（YT/YTZoom模式）
            return CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_YT_CURSORV_VISIBLE);  // 读取YT模式垂直光标可见性
        } else {  // XY模式
            return CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_XY_CURSORV_VISIBLE);  // 读取XY模式垂直光标可见性
        }
    }

    /**
     * 获取水平光标是否可见。
     * <p>根据当前工作模式（YT/XY），从缓存中读取对应的水平光标可见性状态。</p>
     *
     * @return true表示水平光标可见，false表示不可见
     */
    private static boolean getVisibleCursorH() {
        if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY) {  // 非XY模式（YT/YTZoom模式）
            return CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_YT_CURSORH_VISIBLE);  // 读取YT模式水平光标可见性
        } else {  // XY模式
            return CacheUtil.get().getBoolean(CacheUtil.MAIN_WAVE_XY_CURSORH_VISIBLE);  // 读取XY模式水平光标可见性
        }
    }

    /**
     * 获取通道列表是否可见。
     * <p>通道列表仅在非XY模式下显示，同时需要缓存中标记为可见。</p>
     *
     * @return true表示通道列表可见，false表示不可见
     */
    private static boolean getVisibleChannelList() {
        return CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_CHANNELLIST_VISIBLE)  // 缓存中通道列表可见
                && WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY;  // 且非XY模式
    }

    /**
     * 获取缩放区域是否可见。
     * <p>缩放区域仅在非XY模式下显示，同时需要缓存中标记为可见。</p>
     *
     * @return true表示缩放区域可见，false表示不可见
     */
    private static boolean getVisibleZoom() {
        return CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM)  // 缓存中缩放区域可见
                && WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY;  // 且非XY模式
    }

    /**
     * 获取分段区域是否可见。
     * <p>分段区域仅在非XY模式下显示，同时需要缓存中标记为可见。</p>
     *
     * @return true表示分段区域可见，false表示不可见
     */
    private static boolean getVisibleSegment() {
        return CacheUtil.get().getBoolean(CacheUtil.MAIN_CENTER_SEGMENTED_VISIBLE)  // 缓存中分段区域可见
                && WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_XY;  // 且非XY模式
    }

    /**
     * 切换Back按键焦点状态。
     * <p>循环切换backState，跳过不可见的状态，直到找到第一个可见的UI区域。
     * 如果所有区域都不可见，则将状态设为BACKSTATE_NULL。</p>
     *
     * @return true表示状态发生了变化，false表示状态未变
     */
    private static boolean switchBackState() {
        int preBackState = backState;  // 保存切换前的状态，用于判断是否变化
        boolean vCursor =false;// getVisibleCursorV();  // 垂直光标可见性（当前禁用，固定为false）
        boolean hCursor =false;// getVisibleCursorH();  // 水平光标可见性（当前禁用，固定为false）
        boolean channelList = getVisibleChannelList();  // 获取通道列表可见性
        boolean segment = getVisibleSegment();  // 获取分段区域可见性
        boolean zoom = getVisibleZoom();  // 获取缩放区域可见性
            if (/*!vCursor && !hCursor &&*/ !channelList && !zoom && !segment) {  // 所有区域都不可见
                backState = BACKSTATE_NULL;  // 设置为空状态
                return backState != preBackState;  // 返回状态是否变化
            }
            boolean[] list = {vCursor , hCursor, channelList, segment, zoom};  // 各状态的可见性数组，索引对应backState值
            do {  // 循环递增backState，直到找到可见的状态
                if (backState<BACKSTATE_MIN){  // 状态小于最小值
                    backState=BACKSTATE_MIN;  // 重置为最小值
                }else  if (backState == BACKSTATE_MAX) {  // 到达最大值，循环回最小值
                    backState = BACKSTATE_MIN;  // 循环回到最小值
                } else {  // 正常递增
                    backState++;  // 切换到下一个状态
                }
            } while (!list[backState]);  // 如果当前状态不可见，继续循环
        if (backState<BACKSTATE_MIN) backState=BACKSTATE_MIN;  // 安全边界检查
        return backState != preBackState;  // 返回状态是否发生了变化
    }

    /**
     * 按优先级从backStateList中选择第一个可见的Back状态。
     * <p>遍历backStateList（按最近使用优先排序），找到第一个当前可见的状态，
     * 并设置对应的光标选择状态。如果都不可见则设为BACKSTATE_NULL。</p>
     */
    private static void selectBackStateList(){
        boolean vCursor =getVisibleCursorV();  // 获取垂直光标可见性
        boolean hCursor = getVisibleCursorH();  // 获取水平光标可见性
        boolean channelList = getVisibleChannelList();  // 获取通道列表可见性
        boolean segment = getVisibleSegment();  // 获取分段区域可见性
        boolean zoom = getVisibleZoom();  // 获取缩放区域可见性
        boolean[] bs=new boolean[]{vCursor,hCursor,channelList,segment,zoom};  // 可见性数组，索引对应backState值
        for(int i=0;i<backStateList.size();i++){  // 按优先级遍历backStateList
            if (bs[backStateList.get(i)]){  // 找到第一个可见的状态
                backState=backStateList.get(i);  // 设置当前backState
                    if (backStateList.get(i) == 1) {  // 水平光标状态（BACKSTATE_HCURSOR）
                        CacheUtil.get().setLastObjectIsCursor(true);  // 标记最后操作对象为光标
                        ExternalKeysCommand.get().setCursorSelected(true,  // 选中水平光标
                                ExternalKeysCommand.get().getCursorSelected(true));
                        singleHCursor = CursorManage.getInstance().getHorIndex() != 3;  // 判断是否为单水平光标（非3表示单光标）
                    } else if (backStateList.get(i) == 0) {  // 垂直光标状态（BACKSTATE_VCURSOR）
                        CacheUtil.get().setLastObjectIsCursor(true);  // 标记最后操作对象为光标
                        ExternalKeysCommand.get().setCursorSelected(false,  // 选中垂直光标
                                ExternalKeysCommand.get().getCursorSelected(false));
                        singleVCursor = CursorManage.getInstance().getVerIndex() != 3;  // 判断是否为单垂直光标（非3表示单光标）
                    }
                return;  // 找到后立即返回
            }
        }
//        backState=BACKSTATE_CHLIST;
        backState=BACKSTATE_NULL;  // 所有状态都不可见，设为空状态
    }
    /**
     * 选择默认的Back状态。
     * <p>按状态编号从小到大的顺序（VCURSOR→HCURSOR→CHLIST→SEGMENT→ZOOMUP），
     * 选择第一个可见的状态作为默认Back状态。与selectBackStateList不同，
     * 此方法不考虑使用优先级，而是按固定顺序选择。</p>
     */
    private static void selectDefaultBackState(){
        boolean vCursor =getVisibleCursorV();  // 获取垂直光标可见性
        boolean hCursor = getVisibleCursorH();  // 获取水平光标可见性
        boolean channelList = getVisibleChannelList();  // 获取通道列表可见性
        boolean segment = getVisibleSegment();  // 获取分段区域可见性
        boolean zoom = getVisibleZoom();  // 获取缩放区域可见性
        boolean[] bs=new boolean[]{vCursor,hCursor,channelList,segment,zoom};  // 可见性数组，索引对应backState值
        for(int i=0;i<bs.length;i++){  // 按固定顺序遍历所有状态
            if (bs[i]){  // 找到第一个可见的状态
                backState=i;  // 设置当前backState
                    if (i == 1) {  // 水平光标状态（BACKSTATE_HCURSOR）
                        CacheUtil.get().setLastObjectIsCursor(true);  // 标记最后操作对象为光标
                        ExternalKeysCommand.get().setCursorSelected(true,  // 选中水平光标
                                ExternalKeysCommand.get().getCursorSelected(true));
                        singleHCursor = CursorManage.getInstance().getHorIndex() != 3;  // 判断是否为单水平光标
                    } else if (i == 0) {  // 垂直光标状态（BACKSTATE_VCURSOR）
                        CacheUtil.get().setLastObjectIsCursor(true);  // 标记最后操作对象为光标
                        ExternalKeysCommand.get().setCursorSelected(false,  // 选中垂直光标
                                ExternalKeysCommand.get().getCursorSelected(false));
                        singleVCursor = CursorManage.getInstance().getVerIndex() != 3;  // 判断是否为单垂直光标
                    }
                return ;  // 找到后立即返回
            }
        }
//        backState=BACKSTATE_CHLIST;
        backState=BACKSTATE_NULL;  // 所有状态都不可见，设为空状态
    }
    /**
     * 多方向移动编辑器（旋钮/方向键控制Back焦点区域的移动）。
     * <p>根据当前backState，将移动操作分派到垂直光标、水平光标、通道列表、
     * 分段区域或缩放区域。isMulUp区分"上/下选择"和"左/右移动"，
     * count经过二次方加速处理以提供更好的旋钮操控手感。</p>
     *
     * @param isMulUp  true表示上/下方向操作（选择光标A/B线或移动选中项），false表示左/右方向操作
     * @param isRight  true表示向右/上移动，false表示向左/下移动
     * @param count    移动的步数
     */
    private static void editorMulMove2(boolean isMulUp, boolean isRight, int count) {
        boolean vCursor = getVisibleCursorV();  // 获取垂直光标可见性
        boolean hCursor = getVisibleCursorH();  // 获取水平光标可见性
        boolean chList = getVisibleChannelList();  // 获取通道列表可见性
        boolean zoom = getVisibleZoom();  // 获取缩放区域可见性
        boolean segment=getVisibleSegment();  // 获取分段区域可见性
        Log.d(Tag.Debug, String.format("ExternalKeysProtocol.editorMulMove2: isMulUp:%s,backState:%s,",isMulUp,backState ));  // 调试日志
        if (backState!=BACKSTATE_VCURSOR && backState!=BACKSTATE_HCURSOR){  // 非光标状态
            CacheUtil.get().setLastObjectIsCursor(false);  // 标记最后操作对象非光标
        }
        switch (backState) {
            case BACKSTATE_VCURSOR: {  // 垂直光标模式
                CacheUtil.get().setLastObjectIsCursor(true);  // 标记最后操作对象为光标
                ExternalKeysUI.getInstance().setFocusControlBackVisible(false);  // 隐藏Back焦点控件
                ExternalKeysManager.get().setFocusViewVisible(View.GONE);  // 隐藏焦点视图
                count = (int)(Math.ceil((double) count * count / 2) + 0.1);  // 二次方加速：步数越大加速越快
                if (isMulUp) {  // 上/下方向操作：选择垂直光标A线或缩放光标
                    if (singleVCursor) {  // 单垂直光标模式
                        ExternalKeysCommand.get().setCursorSelected(false, TChan.Cursor_col_1);  // 选中垂直光标A线
                        ExternalKeysCommand.get().moveCursor(false, isRight, count);  // 移动垂直光标A线
                    } else {  // 双垂直光标模式
                        ExternalKeysCommand.get().setCursorSelected(false, TChan.Cursor_col_3);  // 选中垂直缩放光标
                        ExternalKeysCommand.get().moveZoomCursor(false, isRight, count);  // 移动垂直缩放光标
                    }
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Cursor_col_1);  // 通知最后操作对象为A线
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_LEFTRIGHT);  // 通知滑动方向为左右
                } else {  // 左/右方向操作：选择垂直光标B线或缩放光标
                    if (singleVCursor) {  // 单垂直光标模式
                        ExternalKeysCommand.get().setCursorSelected(false, TChan.Cursor_col_2);  // 选中垂直光标B线
                    } else {  // 双垂直光标模式
                        ExternalKeysCommand.get().setCursorSelected(false, TChan.Cursor_col_3);  // 选中垂直缩放光标
                    }
                    ExternalKeysCommand.get().moveCursor(false, isRight, count);  // 移动选中的垂直光标
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Cursor_col_2);  // 通知最后操作对象为B线
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_LEFTRIGHT);  // 通知滑动方向为左右
                }
            }
            break;
            case BACKSTATE_HCURSOR: {  // 水平光标模式
                CacheUtil.get().setLastObjectIsCursor(true);  // 标记最后操作对象为光标
                ExternalKeysUI.getInstance().setFocusControlBackVisible(false);  // 隐藏Back焦点控件
                ExternalKeysManager.get().setFocusViewVisible(View.GONE);  // 隐藏焦点视图
                count = (int)(Math.ceil((double) count * count / 2) + 0.1);  // 二次方加速
               // Log.d(TAG,"singleHCursor:" + singleHCursor);
                if (isMulUp) {  // 上/下方向操作：选择水平光标A线或缩放光标
                    if (singleHCursor) {  // 单水平光标模式
                        ExternalKeysCommand.get().setCursorSelected(true, TChan.Cursor_row_1);  // 选中水平光标A线
                        ExternalKeysCommand.get().moveCursor(true, !isRight, count);  // 移动水平光标A线（方向取反：上=右移）
                    } else {  // 双水平光标模式
                        ExternalKeysCommand.get().setCursorSelected(true, TChan.Cursor_row_3);  // 选中水平缩放光标
                        ExternalKeysCommand.get().moveZoomCursor(true, !isRight, count);  // 移动水平缩放光标
                    }
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Cursor_row_1);  // 通知最后操作对象为A线
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);  // 通知滑动方向为上下
                } else {  // 左/右方向操作：选择水平光标B线或缩放光标
                    if (singleHCursor) {  // 单水平光标模式
                        ExternalKeysCommand.get().setCursorSelected(true, TChan.Cursor_row_2);  // 选中水平光标B线
                    } else {  // 双水平光标模式
                        ExternalKeysCommand.get().setCursorSelected(true, TChan.Cursor_row_3);  // 选中水平缩放光标
                    }
                    ExternalKeysCommand.get().moveCursor(true, !isRight, count);  // 移动选中的水平光标（方向取反）
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Cursor_row_2);  // 通知最后操作对象为B线
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);  // 通知滑动方向为上下
                }
            }
            break;
            case BACKSTATE_CHLIST: {  // 通道列表模式
                if (isMulUp) {  // 上/下方向操作
                    ExternalKeysManager.get().moveChannelsSelect(isRight, count);  // 移动通道选择
                    ExternalKeysUI.getInstance().setFocusControlBackVisible(false);  // 隐藏Back焦点控件
                }
            }
            break;
            case BACKSTATE_SEGMENT: {  // 分段区域模式
                if (isMulUp) {  // 上/下方向操作
                    ExternalKeysManager.get().moveSegment(isRight, count);  // 移动分段选择
                    ExternalKeysUI.getInstance().setFocusControlBackVisible(false);  // 隐藏Back焦点控件
                }
            }
            break;
            case BACKSTATE_ZOOMUP: {  // 缩放区域模式
                if (isMulUp) {  // 上/下方向操作
                    if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {  // 仅YTZoom模式下操作
                        ExternalKeysUI.getInstance().showBackStateFocus(backState);  // 显示当前Back状态焦点
                        ExternalKeysUI.getInstance().setFocusControlBackVisible(true);  // 显示Back焦点控件
                        int symbol = isRight ? 1 : -1;  // 方向符号：右=1，左=-1
                        count = (int)(Math.ceil((double) count * count / 2) + 0.1);  // 二次方加速
                        WaveMaskLayer_YTZoom.getInstance().layerX_move(count * symbol);  // 移动缩放窗口X位置
                    }
                }
            }
            break;
            default:  // 默认无操作
//                if (isMulUp) {
//
//                    ExternalKeysManager.get().moveChannelsSelect(isRight, count);
//                }
                break;
        }
    }

    /**
     * 选择键多方向移动编辑器（SELECT_LEFT/RIGHT/UP/DOWN按键处理）。
     * <p>根据当前backState和按键方向，将移动操作分派到光标、通道列表、
     * 分段或缩放区域。如果当前backState为NULL，则自动选择一个可见的状态。</p>
     *
     * @param key      按键码（KEY_SELECT_LEFT/RIGHT/UP/DOWN）
     * @param isRight  true表示向右/上移动，false表示向左/下移动
     * @param state    按键类型（TYPE_KEY_PRESS短按 或 TYPE_KEY_PRESS_LONG长按）
     */
    private static void editorMulMove(int key, boolean isRight, byte state) {
        int count = state == TYPE_KEY_PRESS ? 1 : KEY_PRESS_LONG_VAL;  // 短按移动1步，长按移动多步
        boolean isLeftRight = key == KEY_SELECT_RIGHT || key == KEY_SELECT_LEFT;  // 判断是否为左右方向键
        boolean vCursor = getVisibleCursorV();  // 获取垂直光标可见性
        boolean hCursor = getVisibleCursorH();  // 获取水平光标可见性
        boolean chList = getVisibleChannelList();  // 获取通道列表可见性
        boolean segment = getVisibleSegment();  // 获取分段区域可见性
        boolean zoom = getVisibleZoom();  // 获取缩放区域可见性
//        if (backState == BACKSTATE_CURSOR && !(vCursor || hCursor)) {
//            switchBackState();
//        } else if (backState == BACKSTATE_CHLIST && !chList) {
//            switchBackState();
//        } else if (backState == BACKSTATE_SEGMENT && !segment) {
//            switchBackState();
//        } else if (backState == BACKSTATE_ZOOMUP && !zoom) {
//            switchBackState();
//        }

        if (backState==BACKSTATE_NULL){  // 当前无Back状态，需要自动选择
            selectBackStateList();  // 按优先级选择可见状态

            BackStateUpdate(backState);  // 更新状态优先级列表
            if (backState!=BACKSTATE_HCURSOR && backState!=BACKSTATE_VCURSOR){  // 非光标状态
                CacheUtil.get().setLastObjectIsCursor(false);  // 标记最后操作对象非光标
            }
        }


        switch (backState) {
            case BACKSTATE_HCURSOR:  // 水平光标模式
            case BACKSTATE_CURSOR: {  // 通用光标模式（兼容旧逻辑）
                if (vCursor && hCursor) {  // 垂直和水平光标都可见
                    curHCursor = !isLeftRight;  // 左右键操作垂直光标，上下键操作水平光标
                }
                if (state != TYPE_KEY_PRESS) {  // 长按状态
                    count = getPressLongSpeed(1);  // 使用加速算法计算步数
                }
                if (isLeftRight) {  // 左右方向操作
                    if (vCursor) {  // 垂直光标可见
                        ExternalKeysCommand.get().moveCursor(false, isRight, count);  // 移动垂直光标
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Cursor_col_1);  // 通知最后操作对象
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_LEFTRIGHT);  // 通知滑动方向
                    }
                } else {  // 上下方向操作
                    if (hCursor) {  // 水平光标可见
                        ExternalKeysCommand.get().moveCursor(true, isRight, count);  // 移动水平光标
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Cursor_row_1);  // 通知最后操作对象
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN);  // 通知滑动方向
                    }
                }
            }
            break;
            case BACKSTATE_CHLIST: {  // 通道列表模式

                if (isLeftRight) {  // 左右方向操作
                    canPercentFlag = false;  // 清除百分比标志
                    ExternalKeysManager.get().moveChannelsSelect(isRight, count);  // 移动通道选择
                    ExternalKeysUI.getInstance().setFocusControlBackVisible(false);  // 隐藏Back焦点控件
                }
            }
            break;
            case BACKSTATE_SEGMENT: {  // 分段区域模式
//                if (isLeftRight)
                {  // 所有方向均可操作分段
                    canPercentFlag = false;  // 清除百分比标志
                    ExternalKeysManager.get().moveSegment(key, isRight, count);  // 移动分段选择（传入具体按键码）
                    ExternalKeysUI.getInstance().setFocusControlBackVisible(false);  // 隐藏Back焦点控件
                }
            }
            break;
            case BACKSTATE_ZOOMUP: {  // 缩放区域模式
                if (isLeftRight) {  // 左右方向操作
                    if (state != TYPE_KEY_PRESS) {  // 长按状态，加速3倍
                        count *= 3;  // 长按加速
                    }
                    if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {  // 仅YTZoom模式下操作
                        int symbol = isRight ? 1 : -1;  // 方向符号：右=1，左=-1
                        WaveMaskLayer_YTZoom.getInstance().layerX_move(count * symbol);  // 移动缩放窗口X位置
                    }
                }
            }
            break;
            default:  // 默认（包括BACKSTATE_NULL等），左右方向操作通道列表
                if (isLeftRight) {  // 左右方向操作
                    canPercentFlag = false;  // 清除百分比标志
                    ExternalKeysManager.get().moveChannelsSelect(isRight, count);  // 移动通道选择
                    ExternalKeysUI.getInstance().setFocusControlBackVisible(false);  // 隐藏Back焦点控件
                }
                break;
        }
    }


    /**
     * 设置最后操作的光标/通道对象，并更新对应的Back状态。
     * <p>当用户通过触摸或其他方式操作了某个对象时，调用此方法将Back状态
     * 同步到该对象对应的区域，确保后续旋钮操作作用于正确的目标。</p>
     *
     * @param lastObject 最后操作的对象标识（TChan中的光标或通道常量）
     */
    public static void setLastCursor(int lastObject) {
        //Log.d(TAG, "setLastCursor() called with: lastObject = [" + lastObject + "]");

        switch (lastObject) {
            case TChan.Cursor_col_1:  // 垂直光标A线
            case TChan.Cursor_col_2:  // 垂直光标B线
            case TChan.Cursor_col_3:  // 垂直缩放光标
                BackStateUpdate(BACKSTATE_VCURSOR);  // 更新Back状态为垂直光标
                singleVCursor = CursorManage.getInstance().getVerIndex() != 3;  // 判断是否为单垂直光标
                break;
            case TChan.Cursor_row_1:  // 水平光标A线
            case TChan.Cursor_row_2:  // 水平光标B线
            case TChan.Cursor_row_3:  // 水平缩放光标
                BackStateUpdate(BACKSTATE_HCURSOR);  // 更新Back状态为水平光标
                singleHCursor = CursorManage.getInstance().getHorIndex() != 3;  // 判断是否为单水平光标
                break;
            case TChan.TriggerTime:  // 触发时间位置
                positionV = false;  // 标记为水平方向位置调节
                break;
            case TChan.Ch1:  // 通道1
            case TChan.Ch2:  // 通道2
            case TChan.Ch3:  // 通道3
            case TChan.Ch4:  // 通道4
            case TChan.R1:  // 参考通道1
            case TChan.R2:  // 参考通道2
            case TChan.R3:  // 参考通道3
            case TChan.R4:  // 参考通道4
            case TChan.Math1:  // 数学运算通道
            case TChan.S1:  // 串行解码1
            case TChan.S2:  // 串行解码2
                positionV = true;  // 标记为垂直方向位置调节
                break;
        }
    }

    /** LED控制帧的位域缓存数组，保存当前各LED的亮灭状态，每次编码时在此基础上修改 */
    private static byte[] save = {0x00, 0x00, 0x00, 0x00,0,0,0,0};



    /**
     * 将ARM→MCU的LED控制消息编码为byte[]帧数据。
     * <p>根据LED类型和状态，对save数组中对应位进行置位（亮）或清零（灭）操作。
     * 使用位或（|）点亮LED，位与取反（& ~）熄灭LED。此方法为synchronized，
     * 确保多线程操作save数组时的线程安全。</p>
     *
     * @param toMCU ARM→MCU的LED控制消息对象
     * @return 编码后的LED帧数据（byte[]），或null表示无需发送（如TYPE_FINE/TYPE_SEARCH）
     */
    static synchronized byte[] parseToMCU(ExternalKeysMsg_ToMCU toMCU) {

//        if (ScreenControls.getInstance().isExternalKey()) return save;

        if (toMCU == null) return save;  // 空消息直接返回当前缓存
        switch (toMCU.getLedType()) {
            case ExternalKeysMsg_ToMCU.TYPE_CH + TChan.Ch1:  // CH1通道LED控制
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[3] = (byte) (save[3] | LED_30_CH1_RED | LED_32_CH1_GREEN);  // 点亮CH1红色+绿色LED
                } else {  // 关闭状态→熄灭
                    save[3] = (byte) (save[3] & ~(LED_30_CH1_RED | LED_32_CH1_GREEN));  // 熄灭CH1红色+绿色LED
                }
                break;
            case ExternalKeysMsg_ToMCU.TYPE_CH + TChan.Ch2:  // CH2通道LED控制
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[3] = (byte) (save[3] | LED_34_CH2_BLUE | LED_35_CH2_GREEN);  // 点亮CH2蓝色+绿色LED
                } else {  // 关闭状态→熄灭
                    save[3] = (byte) (save[3] & ~(LED_34_CH2_BLUE | LED_35_CH2_GREEN));  // 熄灭CH2蓝色+绿色LED
                }
                break;
            case ExternalKeysMsg_ToMCU.TYPE_CH + TChan.Ch3:  // CH3通道LED控制
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[3] = (byte) (save[3] | LED_36_CH3_RED | LED_37_CH3_BLUE);  // 点亮CH3红色+蓝色LED

                } else {  // 关闭状态→熄灭
                    save[3] = (byte) (save[3] & ~(LED_36_CH3_RED | LED_37_CH3_BLUE));  // 熄灭CH3红色+蓝色LED
                }
                break;
            case ExternalKeysMsg_ToMCU.TYPE_CH + TChan.Ch4:  // CH4通道LED控制
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[2] = (byte) (save[2] | (LED_23_CH4_GREEN));  // 点亮CH4绿色LED
                } else {  // 关闭状态→熄灭
                    save[2] = (byte) (save[2] & ~(LED_23_CH4_GREEN));  // 熄灭CH4绿色LED
                }
                break;
            case ExternalKeysMsg_ToMCU.TYPE_CH+TChan.Ch5:{  // CH5通道LED控制（扩展键盘）

                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[5] = (byte) (save[5] | (LED_111_CH5_GREEN) );  // 点亮CH5绿色LED
                    save[6] =(byte)(save[6] | LED_127_CH5_RED);  // 点亮CH5红色LED
                } else {  // 关闭状态→熄灭
                    save[5] = (byte) (save[5] & ~(LED_111_CH5_GREEN));  // 熄灭CH5绿色LED
                    save[6] =(byte)(save[6] & ~(LED_127_CH5_RED));  // 熄灭CH5红色LED
                }
            }break;
            case ExternalKeysMsg_ToMCU.TYPE_CH+TChan.Ch6:{  // CH6通道LED控制（扩展键盘）
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[5] = (byte) (save[5] | (LED_114_CH6_GREEN) | (LED_113_CH6_BLUE) );  // 点亮CH6绿色+蓝色LED
                } else {  // 关闭状态→熄灭
                    save[5] = (byte) (save[5] & ~(LED_114_CH6_GREEN | LED_113_CH6_BLUE ) );  // 熄灭CH6绿色+蓝色LED
                }
            }break;
            case ExternalKeysMsg_ToMCU.TYPE_CH+TChan.Ch7:{  // CH7通道LED控制（扩展键盘）
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[5] = (byte) (save[5] | (LED_115_CH7_RED) | (LED_117_CH7_GREEN)| LED_116_CH7_BLUE );  // 点亮CH7红+绿+蓝LED
                } else {  // 关闭状态→熄灭
                    save[5] = (byte) (save[5] & ~(LED_115_CH7_RED | LED_117_CH7_GREEN | LED_116_CH7_BLUE) );  // 熄灭CH7红+绿+蓝LED
                }
            }break;
            case ExternalKeysMsg_ToMCU.TYPE_CH+TChan.Ch8:{  // CH8通道LED控制（扩展键盘）
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[4] = (byte) (save[4] |   (LED_102_CH8_GREEN)| LED_101_CH8_BLUE );  // 点亮CH8绿色+蓝色LED
                } else {  // 关闭状态→熄灭
                    save[4] = (byte) (save[4] & ~(  LED_102_CH8_GREEN | LED_101_CH8_BLUE) );  // 熄灭CH8绿色+蓝色LED
                }
            }break;


            case ExternalKeysMsg_ToMCU.TYPE_MATH:  // MATH按键LED控制
            case ExternalKeysMsg_ToMCU.TYPE_MATH+TChan.Math1:  // Math1通道LED控制
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[1] = (byte) (save[1] & ~(LED_10_MATH_RED | LED_11_MATH_BLUE | LED_12_MATH_GREEN));  // 先清零MATH所有LED
                    save[1] = (byte) (save[1] | (LED_10_MATH_RED));  // 只点亮MATH红色LED
                } else {  // 关闭状态→熄灭
                    save[1] = (byte) (save[1] & ~(LED_10_MATH_RED | LED_11_MATH_BLUE | LED_12_MATH_GREEN));  // 熄灭MATH所有LED
                }

                break;
            case ExternalKeysMsg_ToMCU.TYPE_RUNSTOP:  // Run/Stop按键LED控制（双色LED）
                if (toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_RED)) {  // 红色状态→停止
                    save[0] = (byte) (save[0] | LED_00_STOP);  // 点亮STOP红色LED
                } else {  // 非红色→熄灭STOP
                    save[0] = (byte) (save[0] & ~(LED_00_STOP));  // 熄灭STOP红色LED
                }
                if (toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_GREEN)) {  // 绿色状态→运行
                    save[1] = (byte) (save[1] | LED_17_RUN);  // 点亮RUN绿色LED
                } else {  // 非绿色→熄灭RUN
                    save[1] = (byte) (save[1] & ~(LED_17_RUN));  // 熄灭RUN绿色LED
                }
                break;
            case ExternalKeysMsg_ToMCU.TYPE_AUTO:  // Auto按键LED控制
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[0] = (byte) (save[0] | LED_01_AUTO);  // 点亮AUTO LED
                } else {  // 关闭状态→熄灭
                    save[0] = (byte) (save[0] & ~LED_01_AUTO);  // 熄灭AUTO LED
                }
                break;
            case ExternalKeysMsg_ToMCU.TYPE_SEQ:  // Seq按键LED控制
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[1] = (byte) (save[1] | LED_13_SEQ);  // 点亮SEQ LED
                } else {  // 关闭状态→熄灭
                    save[1] = (byte) (save[1] & ~LED_13_SEQ);  // 熄灭SEQ LED
                }
                break;
            case ExternalKeysMsg_ToMCU.TYPE_FINE:  // Fine按键LED（无硬件LED，不发送）
                return null;  // 返回null表示无需发送LED帧

            case ExternalKeysMsg_ToMCU.TYPE_ZOOM:  // Zoom按键LED控制
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[1] = (byte) (save[1] | LED_15_ZOOM);  // 点亮ZOOM LED
                } else {  // 关闭状态→熄灭
                    save[1] = (byte) (save[1] & ~LED_15_ZOOM);  // 熄灭ZOOM LED
                }
                break;
            case ExternalKeysMsg_ToMCU.TYPE_SEARCH:  // Search按键LED（无硬件LED，不发送）
                return null;  // 返回null表示无需发送LED帧

            case ExternalKeysMsg_ToMCU.TYPE_REF:  // Ref按键LED控制
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[2] = (byte) (save[2] & ~(LED_24_REF_RED | LED_25_REF_BLUE | LED_26_REF_GREEN));  // 先清零REF所有LED
                    save[2] = (byte) (save[2] | (LED_24_REF_RED | LED_25_REF_BLUE | LED_26_REF_GREEN));  // 点亮REF白色（红+蓝+绿混光）
                } else {  // 关闭状态→熄灭
                    save[2] = (byte) (save[2] & ~(LED_24_REF_RED | LED_25_REF_BLUE | LED_26_REF_GREEN));  // 熄灭REF所有LED
                }
                break;
            case ExternalKeysMsg_ToMCU.TYPE_SERIAL:  // Serial按键LED控制
            case ExternalKeysMsg_ToMCU.TYPE_S1:  // S1解码通道LED控制
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[4] = (byte) (save[4] | (LED_105_BUS_RED | LED_107_BUS_GREEN | LED_106_BUS_BLUE));  // 点亮BUS白色LED（红+绿+蓝混光）
                } else {  // 关闭状态→熄灭
                    save[4] = (byte) (save[4] & ~(LED_105_BUS_RED | LED_107_BUS_GREEN | LED_106_BUS_BLUE));  // 熄灭BUS所有LED
                }

//                if(HardwareProduct.isExtKeyboard()){
//
//                    if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {
//                        save[0] = (byte) (save[0] & ~(LED_03_BUS1_BLUE | LED_04_BUS1_GREEN ));
//                        save[2] = (byte) (save[2] & ~(LED_27_BUS1_RED));
//                        save[2] = (byte) (save[2] | LED_27_BUS1_RED);
//                        save[0] = (byte) (save[0] | LED_04_BUS1_GREEN);
//
//                    } else {
//                        save[0] = (byte) (save[0] & ~(LED_03_BUS1_BLUE | LED_04_BUS1_GREEN ));
//                        save[2] = (byte) (save[2] & ~(LED_27_BUS1_RED));
//                    }
//
//                }else {
//                    if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {
//                        save[1] = (byte) (save[1] & ~(LED_10_MATH_RED | LED_11_MATH_BLUE | LED_12_MATH_GREEN));
//                        save[1] = (byte) (save[1] | (LED_10_MATH_RED | LED_12_MATH_GREEN));
//
//                    } else {
//                        save[1] = (byte) (save[1] & ~(LED_10_MATH_RED | LED_11_MATH_BLUE | LED_12_MATH_GREEN));
//                    }
//                }
                break;
            case ExternalKeysMsg_ToMCU.TYPE_S2:  // S2解码通道LED控制
                if(HardwareProduct.isExtKeyboard()){  // 扩展键盘版本
                    if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                        save[0] = (byte) (save[0] & ~(LED_05_BUS2_RED | LED_06_BUS2_BLUE |  LED_07_BUS2_GREEN));  // 先清零BUS2所有LED
                        save[0] = (byte) (save[0] | (LED_05_BUS2_RED | LED_07_BUS2_GREEN));  // 点亮BUS2黄色LED（红+绿混光）
                    } else {  // 关闭状态→熄灭
                        save[0] = (byte) (save[0] & ~(LED_05_BUS2_RED | LED_06_BUS2_BLUE |  LED_07_BUS2_GREEN));  // 熄灭BUS2所有LED
                    }
                }else {  // 标准键盘版本
                    if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                        save[2] = (byte) (save[2] & ~(LED_24_REF_RED | LED_25_REF_BLUE | LED_26_REF_GREEN));  // 先清零REF所有LED
                        save[2] = (byte) (save[2] | (LED_24_REF_RED | LED_26_REF_GREEN));  // 点亮REF黄色LED（红+绿混光）
                    } else {  // 关闭状态→熄灭
                        save[2] = (byte) (save[2] & ~(LED_24_REF_RED | LED_25_REF_BLUE | LED_26_REF_GREEN));  // 熄灭REF所有LED
                    }
                }
                break;

            case ExternalKeysMsg_ToMCU.TYPE_DIGITAL:  // Digital按键LED（无硬件LED，不发送）
                return null;  // 返回null表示无需发送LED帧

            case ExternalKeysMsg_ToMCU.TYPE_WAVEFORM:  // Waveform按键LED（无硬件LED，不发送）
                return null;  // 返回null表示无需发送LED帧

            case ExternalKeysMsg_ToMCU.TYPE_POWER:  // Power按键LED（暂无控制逻辑）

                break;
            case ExternalKeysMsg_ToMCU.TYPE_HCURSOR:  // 水平光标按键LED控制
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[4] = (byte) (save[4] | (LED_104_HCURSOR));  // 点亮水平光标LED
                } else {  // 关闭状态→熄灭
                    save[4] = (byte) (save[4] & ~(LED_104_HCURSOR));  // 熄灭水平光标LED
                }
                break;
            case ExternalKeysMsg_ToMCU.TYPE_VCURSOR:  // 垂直光标按键LED控制
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[4] = (byte) (save[4] | (LED_103_VCURSOR));  // 点亮垂直光标LED
                } else {  // 关闭状态→熄灭
                    save[4] = (byte) (save[4] & ~(LED_103_VCURSOR));  // 熄灭垂直光标LED
                }
                break;
            case ExternalKeysMsg_ToMCU.TYPE_TOUCH_OFF:  // 触摸锁定按键LED控制
                if (!toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF)) {  // 非关闭状态→点亮
                    save[0] = (byte) (save[0] | LED_02_TOUCH_OFF);  // 点亮触摸锁定LED
                } else {  // 关闭状态→熄灭
                    save[0] = (byte) (save[0] & ~LED_02_TOUCH_OFF);  // 熄灭触摸锁定LED
                }
                break;
            case ExternalKeysMsg_ToMCU.TYPE_ALL: {  // 全部LED控制（全亮或全灭）
                byte xsave[] = new byte[save.length];  // 创建新的帧数据（不修改save缓存）
                for (int i = 0; i < save.length; i++) {  // 遍历每个字节
                    xsave[i] = (byte) (toMCU.getLedState().equals(ExternalKeysMsg_ToMCU.STATE_LED_OFF) ? 0 : 0xFF);  // 全灭=0x00，全亮=0xFF
                }
                return xsave;  // 返回新的帧数据（不影响save缓存）
            }
        }
        return save;  // 返回修改后的LED帧缓存
    }

    /**
     * 通过RxBus发送触摸锁定LED控制消息给MCU。
     *
     * @param state LED状态字符串（如STATE_LED_OFF/STATE_LED_GREEN等）
     */
    private static void sendExternalToMcuMsg(String state) {
        RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU, new ExternalKeysMsg_ToMCU(ExternalKeysMsg_ToMCU.TYPE_TOUCH_OFF, state));  // 发送触摸锁定LED消息
    }

    /** Back状态优先级列表，按最近使用顺序排列，索引0为最高优先级 */
    public static List<Integer> backStateList=new ArrayList<>();
    /**
     * 更新Back状态优先级列表。
     * <p>如果列表为空，先初始化为默认顺序（VCURSOR→HCURSOR→CHLIST→SEGMENT→ZOOMUP）。
     * 然后将指定状态移到列表头部，使其成为最高优先级。同时更新backState变量。</p>
     *
     * @param bs 要提升优先级的Back状态值
     */
    public static void BackStateUpdate(int bs){
        if (backStateList.size()==0){  // 列表为空，初始化默认顺序
            backStateList.add(BACKSTATE_VCURSOR);  // 0: 垂直光标
            backStateList.add(BACKSTATE_HCURSOR);  // 1: 水平光标
            backStateList.add(BACKSTATE_CHLIST);  // 2: 通道列表
            backStateList.add(BACKSTATE_SEGMENT);  // 3: 分段区域
            backStateList.add(BACKSTATE_ZOOMUP);  // 4: 缩放区域
        }
        if (backStateList.contains(bs)){  // 列表中存在指定状态
            int index= Tools.indexOf(backStateList,i->i==bs);  // 查找该状态的索引
            backStateList.add(0,backStateList.remove(index));  // 移除并插入到头部，提升为最高优先级
            backState=bs;  // 更新当前backState
        }
    }
    /**
     * 获取当前最高优先级且可见的Back状态。
     * <p>按优先级从高到低遍历backStateList，返回第一个当前可见的状态。
     * 可见性判断：垂直/水平光标通过CursorManage判断，通道列表/分段通过
     * ExternalKeysCommand判断对话框是否显示，缩放通过工作模式判断。</p>
     *
     * @return 当前可见的最高优先级Back状态，如果都不可见则返回BACKSTATE_NULL
     */
    public static int getBackState(){
        Log.d(Tag.Debug, String.format("ExternalKeysProtocol.getBackState: %s",Arrays.toString(backStateList.toArray()) ));  // 调试日志：打印优先级列表
        for(int i=0;i<backStateList.size();i++){  // 按优先级从高到低遍历
                switch (backStateList.get(i)) {
                    case BACKSTATE_VCURSOR: {  // 垂直光标
                        if (CursorManage.getInstance().getColVisible()) {  // 垂直光标列可见
                            return backStateList.get(i);  // 返回垂直光标状态
                        }
                    }
                    break;
                    case BACKSTATE_HCURSOR: {  // 水平光标
                        if (CursorManage.getInstance().getRowVisible()) {  // 水平光标行可见
                            return backStateList.get(i);  // 返回水平光标状态
                        }
                    }
                    break;
                    case BACKSTATE_CHLIST: {  // 通道列表
                        if (ExternalKeysCommand.get().isDialogChListShow()) {  // 通道列表对话框正在显示
                            return backStateList.get(i);  // 返回通道列表状态
                        }
                    }
                    break;
                    case BACKSTATE_SEGMENT: {  // 分段区域
                        if (ExternalKeysCommand.get().isDialogSegmentShow()) {  // 分段对话框正在显示
                            return backStateList.get(i);  // 返回分段状态
                        }
                    }
                    break;
                    case BACKSTATE_ZOOMUP: {  // 缩放区域
                        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {  // 当前为YTZoom模式
                            return backStateList.get(i);  // 返回缩放状态
                        }
                    }
                    break;
                    default:  // 未知状态
                        return BACKSTATE_NULL;  // 返回空状态
                }
        }
        return BACKSTATE_NULL;  // 所有状态都不可见，返回空状态
    }

}