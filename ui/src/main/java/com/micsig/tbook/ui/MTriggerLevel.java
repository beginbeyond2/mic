package com.micsig.tbook.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.micsig.base.Logger;
import com.micsig.tbook.ui.util.BitmapUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.util.svg.SvgManager;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * ╔═══════════════════════════════════════════════════════════════════════════════════════════════════╗
 *                                    MTriggerLevel - 触发电平控件
 * ╠═══════════════════════════════════════════════════════════════════════════════════════════════════╣
 * 【模块定位】
 * 示波器UI核心组件，用于设置和显示触发电平的自定义视图控件。
 *
 * 【核心职责】
 * 1. 显示触发电平位置（按钮模式和拖动模式）
 * 2. 支持触发电平的上下拖动调整
 * 3. 支持多通道切换（CH1-CH8 + 外部触发）
 * 4. 支持三种工作模式：普通模式、高低电平模式、逻辑触发模式
 * 5. 提供展开/收起动画效果
 * 6. 支持左滑手势隐藏控件
 *
 * 【架构设计】
 * 继承自View，采用状态机模式管理显示和工作模式：
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    MTriggerLevel                                 │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
 * │  │ 状态管理层   │  │ 渲染层      │  │ 交互层                  │  │
 * │  │ 显示模式    │  │ onDraw     │  │ dealTouchEvent         │  │
 * │  │ 工作模式    │  │ 动画效果   │  │ 滑动/点击处理          │  │
 * │  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 【显示模式】
 * - Button模式：紧凑按钮状态，显示当前通道的触发电平图标
 * - Drag模式：展开拖动状态，显示上下箭头和通道切换功能
 * - Changing模式：动画过渡状态
 *
 * 【工作模式】
 * - Normal模式：普通触发电平设置
 * - HighLow模式：高低电平选择
 * - Logic模式：逻辑触发通道选择
 *
 * 【数据流向】
 * 外部设置参数 → setXxx方法 → 状态更新 → invalidate() → onDraw()渲染
 * 用户触摸交互 → dealTouchEvent → 状态变化 → 回调接口 → 外部响应
 *
 * 【依赖关系】
 * - View: Android视图基类
 * - TChan: 通道常量定义
 * - SvgManager: SVG图形管理器
 * - ObjectAnimator: 属性动画
 *
 * 【使用示例】
 * MTriggerLevel triggerLevel = new MTriggerLevel(context);
 * triggerLevel.setCurrCh(TChan.Ch1);
 * triggerLevel.setTriggerLevel_Mode_Show(TriggerLevel_Mode_Show_Button);
 * triggerLevel.setOnMouseMoveListener(new OnMouseMoveListener() {
 *     @Override
 *     public void onMouseMove(View view, double deltaY, int Ch, int openType, boolean isFromUser) {
 *         // 处理触发电平移动
 *     }
 * });
 *
 * 【注意事项】
 * 1. 控件高度在Button和Drag模式间动态变化
 * 2. 使用@IntDef注解确保模式参数的类型安全
 * 3. 5秒无操作会自动收起到Button模式
 * 4. 支持多点触控处理
 *
 * @author liwb
 * @version 1.0
 * @since 2017/4/5
 */
public class MTriggerLevel extends View {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 日志标签
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 日志标签，用于调试输出 */
    private String TAG = "MTriggerLevel";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 显示模式常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 显示模式：按钮状态 */
    public static final int TriggerLevel_Mode_Show_Button = 0x01;

    /** 显示模式：拖动状态 */
    public static final int TriggerLevel_Mode_Show_Drag = 0x02;

    /** 显示模式：变化过渡状态 */
    public static final int TriggerLevel_Mode_Show_Changing = 0x03;

    /**
     * 显示模式注解
     * 用于编译时类型检查
     */
    @IntDef({TriggerLevel_Mode_Show_Button, TriggerLevel_Mode_Show_Drag, TriggerLevel_Mode_Show_Changing})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TriggerLevel_Mode_Show {
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 工作模式常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 工作模式：普通模式 */
    public static final int TriggerLevel_Mode_Work_Normal = 0x01;

    /** 工作模式：高低电平模式 */
    public static final int TriggerLevel_Mode_Work_HighLow = 0x02;

    /** 工作模式：逻辑触发模式 */
    public static final int TriggerLevel_Mode_Work_Logic = 0x03;

    /**
     * 工作模式注解
     * 用于编译时类型检查
     */
    @IntDef({TriggerLevel_Mode_Work_Normal, TriggerLevel_Mode_Work_HighLow, TriggerLevel_Mode_Work_Logic})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TriggerLevel_Mode_Work {
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 逻辑触发状态常量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 逻辑触发状态：高电平 */
    public static final int TriggerLevel_Mode_Work_Logic_High = 0x01;

    /** 逻辑触发状态：低电平 */
    public static final int TriggerLevel_Mode_Work_Logic_Low = 0x02;

    /** 逻辑触发状态：未选中 */
    public static final int TriggerLevel_Mode_work_Logic_None = 0x03;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 尺寸常量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 按钮模式高度 */
    private static final int BUTTONHEIGHT = 106;

    /** 拖动模式高度 */
    private static final int DRAGHEIGHT = 1040;

    /** 波形高度 */
    private static int waveHeight = 1040;

    /** 中心Y坐标 */
    private static float CenterY = (DRAGHEIGHT - BUTTONHEIGHT) / 2.0f;

    /** 当前触发类型索引，只有边沿触发时才显示外部触发 */
    private int currentTriggerIndex = 1;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 接口定义 - 鼠标移动监听
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 鼠标移动监听接口
     * 用于处理触发电平的拖动和通道切换
     */
    public interface OnMouseMoveListener {
        /**
         * 触发电平移动回调
         *
         * @param view 触发控件
         * @param deltaY 移动距离（正数表示向上移动）
         * @param Ch 通道号（1~4或更多）
         * @param openType 打开方式：OPENTYPE_TRIGGER, OPENTYPE_SERIALS1等
         * @param isFromUser 是否来自用户操作
         */
        void onMouseMove(View view, double deltaY, int Ch, @OpenType int openType, boolean isFromUser);

        /**
         * 上箭头点击回调
         *
         * @param view 触发控件
         * @param Ch 通道号
         * @param openType 打开方式
         */
        void onUpClick(View view, int Ch, @OpenType int openType);

        /**
         * 下箭头点击回调
         *
         * @param view 触发控件
         * @param Ch 通道号
         * @param openType 打开方式
         */
        void onDownClick(View view, int Ch, @OpenType int openType);

        /**
         * 移动完成回调
         *
         * @param view 触发控件
         * @param openType 打开方式
         */
        void onMouseMoveComplete(View view, @OpenType int openType);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 打开类型常量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 打开类型：触发 */
    public static final int OPENTYPE_TRIGGER = 0;

    /** 打开类型：串口1 */
    public static final int OPENTYPE_SERIALS1 = 1;

    /** 打开类型：串口2 */
    public static final int OPENTYPE_SERIALS2 = 2;

    /** 打开类型：串口3 */
    public static final int OPENTYPE_SERIALS3 = 3;

    /** 打开类型：串口4 */
    public static final int OPENTYPE_SERIALS4 = 4;

    /**
     * 判断是否为串口类型
     *
     * @param type 打开类型
     * @return true表示是串口类型
     */
    public static boolean isOpenSerial(int type){
        if (type>=OPENTYPE_SERIALS1 && type<=OPENTYPE_SERIALS4){
            return true;
        }
        return false;
    }

    /**
     * 打开类型注解
     * 用于编译时类型检查
     */
    @IntDef({OPENTYPE_TRIGGER, OPENTYPE_SERIALS1, OPENTYPE_SERIALS2,OPENTYPE_SERIALS3,OPENTYPE_SERIALS4})
    @Retention(RetentionPolicy.SOURCE)
    public @interface OpenType {
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 接口定义 - 打开关闭监听
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 打开关闭监听接口
     * 用于通知控件的展开和收起状态
     */
    public interface OnOpenCloseListener {
        /**
         * 控件打开回调
         *
         * @param type 打开类型
         */
        void onOpen(@OpenType int type);

        /**
         * 控件关闭回调
         *
         * @param type 打开类型
         */
        void onClose(@OpenType int type);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 属性参数
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 上箭头触摸区域 */
    private Rect upRect, downRect;

    /** 逻辑触发模式下各通道的状态数组 */
    private int[] TriggerLevel_Mode_Work_Logic_state = new int[TChan.MaxLogicChan+1];

    /** 通道数量 */
    private int channelCount = 4;

    /** 应用上下文 */
    private Context context;

    /** 打开关闭监听器 */
    private OnOpenCloseListener onOpenCloseListener;

    /** 打开类型 */
    @OpenType
    private int openType = OPENTYPE_TRIGGER;

    // ═══════════════════════════════════════════════════════════════════════════════
    // Getter/Setter方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置打开类型
     *
     * @param openType 打开类型
     */
    public void setOpenType(@OpenType int openType) {
        this.openType = openType;
    }

    /**
     * 获取打开类型
     *
     * @return 打开类型
     */
    public int getOpenType() {
        return openType;
    }

    /**
     * 获取打开关闭监听器
     *
     * @return 打开关闭监听器
     */
    public OnOpenCloseListener getOnOpenCloseListener() {
        return onOpenCloseListener;
    }

    /**
     * 设置打开关闭监听器
     *
     * @param onOpenCloseListener 打开关闭监听器
     */
    public void setOnOpenCloseListener(OnOpenCloseListener onOpenCloseListener) {
        this.onOpenCloseListener = onOpenCloseListener;
    }

    /**
     * 设置通道数量
     *
     * @param channelCount 通道数量
     */
    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }

    /**
     * 设置逻辑触发模式下指定通道的状态
     *
     * @param chId 通道ID
     * @param channelState 通道状态
     */
    public void setTriggerLevel_Mode_Work_Logic_state(int chId, int channelState) {
        TriggerLevel_Mode_Work_Logic_state[chId] = channelState;
        invalidate();
    }

    /**
     * 设置多个通道的逻辑触发状态
     *
     * @param chId 选中的通道ID数组
     */
    public void setTriggerLevel_Mode_Work_Logic_states(int... chId) {
        // 先清除所有状态
        for (int i = 0; i < TriggerLevel_Mode_Work_Logic_state.length; i++) {
            TriggerLevel_Mode_Work_Logic_state[i] = TriggerLevel_Mode_work_Logic_None;
        }
        // 设置选中的通道为高电平状态
        for (int i = 0; i < chId.length; i++) {
            TriggerLevel_Mode_Work_Logic_state[chId[i]] = TriggerLevel_Mode_Work_Logic_High;
        }
        invalidate();
    }

    /**
     * 获取逻辑触发状态数组
     *
     * @return 状态数组
     */
    public int[] getTriggerLevel_Mode_Work_Logic_state() {
        return TriggerLevel_Mode_Work_Logic_state;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 高低电平模式相关
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 高低电平模式下的状态，true为高电平 */
    private boolean TriggerLevel_Mode_Work_HighLow_State;

    /**
     * 获取高低电平状态
     *
     * @return true表示高电平
     */
    public boolean isTriggerLevel_Mode_Work_HighLow_State() {
        return TriggerLevel_Mode_Work_HighLow_State;
    }

    /**
     * 获取高低电平索引
     *
     * @return 1:高电平; 2:低电平
     */
    public int isTriggerLevel_Mode_Work_HighLow_Index() {
        return TriggerLevel_Mode_Work_HighLow_State ? 1 : 2;
    }

    /**
     * 设置高低电平模式下的选择
     *
     * @param triggerLevel_Mode_Work_HighLow_State true为高电平，false为低电平
     */
    public void setTriggerLevel_Mode_Work_HighLow_State(boolean triggerLevel_Mode_Work_HighLow_State) {
        TriggerLevel_Mode_Work_HighLow_State = triggerLevel_Mode_Work_HighLow_State;
        invalidate();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 当前通道相关
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前通道
     *
     * @return 当前通道号
     */
    public int getCurrCh() {
        return CurrCh;
    }

    /**
     * 设置当前通道
     *
     * @param currCh 通道号
     */
    public void setCurrCh(int currCh) {
        if (currCh % (channelCount + 2) == 0) currCh = channelCount;
        CurrCh = (currCh % (channelCount + 2));
        invalidate();
    }

    /** 当前选中的通道 */
    private int CurrCh = TChan.Ch1;

    /** 按钮颜色对应的通道 */
    private int buttonChColor = TChan.Ch1;

    /**
     * 设置按钮颜色对应的通道
     *
     * @param buttonChColor 通道号
     */
    public void setButtonChColor(int buttonChColor) {
        this.buttonChColor = buttonChColor;
        invalidate();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 显示模式相关
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取显示模式
     *
     * @return 显示模式
     */
    @TriggerLevel_Mode_Show
    public int getTriggerLevel_Mode_Show() {
        return TriggerLevel_Mode_Show;
    }

    /**
     * 设置显示模式
     * 会自动调整控件高度和位置
     *
     * @param triggerLevel_Mode_Show 显示模式
     */
    public void setTriggerLevel_Mode_Show(@TriggerLevel_Mode_Show int triggerLevel_Mode_Show) {
        TriggerLevel_Mode_Show = triggerLevel_Mode_Show;
        if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Button) {
            // 按钮模式：设置高度为按钮高度
            ViewGroup.LayoutParams lp = this.getLayoutParams();
            lp.height = BUTTONHEIGHT;
            this.setLayoutParams(lp);
            setY(CenterY);
        } else if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag) {
            // 拖动模式：设置高度为拖动高度
            ViewGroup.LayoutParams lp = this.getLayoutParams();
            lp.height = DRAGHEIGHT;
            this.setLayoutParams(lp);
            setY(40);
        }
        invalidate();
    }

    /** 当前显示模式 */
    @TriggerLevel_Mode_Show
    private int TriggerLevel_Mode_Show = TriggerLevel_Mode_Show_Button;

    /**
     * 设置工作模式
     *
     * @param triggerLevel_Mode_Work 工作模式
     */
    public void setTriggerLevel_Mode_Work(@TriggerLevel_Mode_Work int triggerLevel_Mode_Work) {
        TriggerLevel_Mode_Work = triggerLevel_Mode_Work;
        invalidate();
    }

    /**
     * 获取当前工作模式
     *
     * @return 工作模式
     */
    public int getTriggerLevel_Mode_Work() {
        return TriggerLevel_Mode_Work;
    }

    /** 当前工作模式 */
    @TriggerLevel_Mode_Work
    private int TriggerLevel_Mode_Work = TriggerLevel_Mode_Work_Normal;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 监听器
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 点击监听器 */
    private View.OnClickListener onClickListener;

    /**
     * 设置点击监听器
     *
     * @param onClickListener 点击监听器
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * 设置鼠标移动监听器
     *
     * @param onMouseMoveListener 鼠标移动监听器
     */
    public void setOnMouseMoveListener(OnMouseMoveListener onMouseMoveListener) {
        this.onMouseMoveListener = onMouseMoveListener;
    }

    /** 鼠标移动监听器 */
    private OnMouseMoveListener onMouseMoveListener = null;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 动画相关
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置动画视图高度
     * 用于动画过程中的高度变化
     *
     * @param animation_ViewHeight 动画高度
     */
    public void setAnimation_ViewHeight(int animation_ViewHeight) {
        Animation_ViewHeight = animation_ViewHeight;

        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) this.getLayoutParams();
        lp.height = Animation_ViewHeight;
        this.setLayoutParams(lp);
        post(() -> {
            CenterY = (DRAGHEIGHT - this.getHeight()) / 2.0f;
            setY(CenterY);
        });
        invalidate();
    }

    /** 动画视图高度 */
    private int Animation_ViewHeight;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量区 - 位图资源
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 各通道的上下箭头位图数组 [通道][0=下箭头, 1=上箭头] */
    private Bitmap[][] bmp = new Bitmap[TChan.MaxLogicChan + 2][2];

    /** 各通道的上下箭头位图 */
    private Bitmap ch1_down, ch1_up, ch2_down, ch2_up, ch3_down, ch3_up, ch4_down, ch4_up,
            ch5_down, ch5_up, ch6_down, ch6_up, ch7_down, ch7_up, ch8_down, ch8_up, out_down, out_up, levelSlider;

    /** 触发电平图标位图 */
    private Bitmap triglevel_ch1, triglevel_ch2, triglevel_ch3, triglevel_ch4;

    /** 触发电平图标绘制区域 */
    private Rect rectTrigLevel;

    /** 按钮模式高度 */
    private int buttonHeight;

    /** 绘制用的画笔 */
    private Paint mPaint;

    /** 触摸事件相关坐标 */
    private int lastX, lastY, oldX, oldY;

    /** 按下状态标志 */
    private boolean downState = false;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 简单构造方法
     *
     * @param context 应用上下文
     */
    public MTriggerLevel(Context context) {
        this(context, null);
    }

    /**
     * XML属性构造方法
     *
     * @param context 应用上下文
     * @param attrs XML属性集
     */
    public MTriggerLevel(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 完整构造方法
     * 初始化所有位图资源和画笔
     *
     * @param context 应用上下文
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public MTriggerLevel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;

        // 使用SVG动态生成各通道的上下箭头位图
        ch1_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch1), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch1_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch1), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch2_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch2), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch2_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch2), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch3_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch3), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch3_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch3), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch4_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch4), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch4_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch4), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);

        // 通道5-8的位图
        ch5_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch5), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch5_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch5), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch6_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch6), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch6_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch6), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch7_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch7), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch7_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch7), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch8_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.Ch8), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        ch8_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.Ch8), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);

        // 外部触发的位图
        out_down = SvgManager.createSvg(SvgNodeInfo.getTLDownPaths(), SvgNodeInfo.getTLColors(TChan.NULL), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);
        out_up = SvgManager.createSvg(SvgNodeInfo.getTLUpPaths(), SvgNodeInfo.getTLColors(TChan.NULL), SvgNodeInfo.TL_WIDTH, SvgNodeInfo.TL_HEIGHT);

        // 加载并缩放滑块背景
        Bitmap temp = BitmapUtil.getBitmapFromDrawable(context, R.drawable.trigger_level_slider);
        levelSlider = BitmapUtil.scaleBitmap(temp, temp.getWidth(), DRAGHEIGHT);

        // 加载触发电平图标
        triglevel_ch1 = readSvgBmp(context,R.drawable.svg_main_trigger_level);
        triglevel_ch2 = readSvgBmp(context,R.drawable.svg_main_trigger_level);
        triglevel_ch3 = readSvgBmp(context,R.drawable.svg_main_trigger_level);
        triglevel_ch4 = readSvgBmp(context,R.drawable.svg_main_trigger_level);

        rectTrigLevel = new Rect(0, 0, this.triglevel_ch1.getWidth(), triglevel_ch1.getHeight());

        // 初始化位图数组
        bmp[1][0] = ch1_down;
        bmp[2][0] = ch2_down;
        bmp[3][0] = ch3_down;
        bmp[4][0] = ch4_down;
        bmp[5][0] = ch5_down;
        bmp[6][0] = ch6_down;
        bmp[7][0] = ch7_down;
        bmp[8][0] = ch8_down;
        bmp[9][0] = out_down;

        bmp[1][1] = ch1_up;
        bmp[2][1] = ch2_up;
        bmp[3][1] = ch3_up;
        bmp[4][1] = ch4_up;
        bmp[5][1] = ch5_up;
        bmp[6][1] = ch6_up;
        bmp[7][1] = ch7_up;
        bmp[8][1] = ch8_up;
        bmp[9][1] = out_up;

        // 初始化画笔
        buttonHeight = BUTTONHEIGHT;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        upRect = new Rect();
        downRect = new Rect();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 静态辅助方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 从SVG资源读取位图
     *
     * @param context 应用上下文
     * @param drawableId 资源ID
     * @return 位图对象
     */
    public static Bitmap readSvgBmp(Context context, int drawableId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return BitmapFactory.decodeResource(context.getResources(), drawableId);
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 测量方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 测量控件尺寸
     *
     * @param widthMeasureSpec 宽度测量规格
     * @param heightMeasureSpec 高度测量规格
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 获取文本的边界矩形
     *
     * @param text 文本内容
     * @param paint 画笔
     * @return 文本边界矩形
     */
    public static Rect getTextRect(String text, Paint paint) {
        Rect rectText = new Rect();
        paint.getTextBounds(text, 0, text.length(), rectText);
        return rectText;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 绘制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 绘制视图
     * 根据显示模式选择不同的绘制策略
     *
     * @param canvas 画布
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Button) {
            if (this.getHeight() <= this.buttonHeight) {
                // 按钮模式：绘制触发电平图标
                mPaint.setColor(Color.rgb(0xAD,0xBD,0xCC));
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setTextSize(24);
                Rect rect = getTextRect("Level", mPaint);

                // 根据通道颜色绘制对应的图标
                switch (buttonChColor) {
                    case TChan.Ch1:
                        canvas.drawBitmap(triglevel_ch1, (this.getWidth() - triglevel_ch1.getWidth()) / 2, (this.getHeight() - triglevel_ch1.getHeight()) / 2, mPaint);
                        canvas.drawText("Level",(this.getWidth()-rect.width())/2,(this.getHeight()-rect.height())/2+rect.height(),mPaint);
                        break;
                    case TChan.Ch2:
                        canvas.drawBitmap(triglevel_ch2, (this.getWidth() - triglevel_ch1.getWidth()) / 2, (this.getHeight() - triglevel_ch1.getHeight()) / 2, mPaint);
                        canvas.drawText("Level",(this.getWidth()-rect.width())/2,(this.getHeight()-rect.height())/2+rect.height(),mPaint);
                        break;
                    case TChan.Ch3:
                        canvas.drawBitmap(triglevel_ch3, (this.getWidth() - triglevel_ch1.getWidth()) / 2, (this.getHeight() - triglevel_ch1.getHeight()) / 2, mPaint);
                        canvas.drawText("Level",(this.getWidth()-rect.width())/2,(this.getHeight()-rect.height())/2+rect.height(),mPaint);
                        break;
                    case TChan.Ch4:
                    case TChan.Ch5:
                    case TChan.Ch6:
                    case TChan.Ch7:
                    case TChan.Ch8:
                    case TChan.Ch8 + 1:
                        canvas.drawBitmap(triglevel_ch4, (this.getWidth() - triglevel_ch1.getWidth()) / 2, (this.getHeight() - triglevel_ch1.getHeight()) / 2, mPaint);
                        canvas.drawText("Level",(this.getWidth()-rect.width())/2,(this.getHeight()-rect.height())/2+rect.height(),mPaint);
                        break;
                }
            } else {
                // 正在从button变为drag模式的过渡状态
                switchDrawModeShowDrag(canvas);
            }

        } else if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag) {
            autoDragToButton();
            if (this.getHeight() >= ((View) this.getParent()).getHeight()) {
                // 拖动模式：绘制完整界面
                switchDrawModeShowDrag(canvas);
            } else {
                // 正在从drag变为button模式的过渡状态
                switchDrawModeShowDrag(canvas);
            }
        } else if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Changing) {
            // 变化过渡状态
            switchDrawModeShowDrag(canvas);
        }
    }

    /**
     * 获取逻辑触发模式下的前一个通道
     *
     * @param currCh 当前通道
     * @return 前一个有效通道
     */
    private int getPreCh(int currCh) {
        int i = currCh;
        do {
            i--;
            if (i == 0) i = TriggerLevel_Mode_Work_Logic_state.length - 1;
            if (TriggerLevel_Mode_Work_Logic_state[i] != TriggerLevel_Mode_work_Logic_None)
                return i;
        } while (i != currCh);
        return currCh;
    }

    /**
     * 获取逻辑触发模式下的下一个通道
     *
     * @param currCh 当前通道
     * @return 下一个有效通道
     */
    private int getNextCh(int currCh) {
        int i = currCh;
        do {
            i++;
            if (i == TriggerLevel_Mode_Work_Logic_state.length) i = 1;
            if (TriggerLevel_Mode_Work_Logic_state[i] != TriggerLevel_Mode_work_Logic_None)
                return i;
        } while (i != currCh);
        return currCh;
    }

    /**
     * 切换到拖动模式的绘制
     * 绘制滑块背景、上下箭头和通道指示
     *
     * @param canvas 画布
     */
    private void switchDrawModeShowDrag(Canvas canvas) {
        int paddingEnd = getPaddingEnd();

        // 绘制滑块背景
        Rect src = new Rect(0, 0, levelSlider.getWidth(), levelSlider.getHeight());
        Rect dst = new Rect(0, 0, getWidth(), getHeight());
        canvas.drawBitmap(levelSlider, src, dst, mPaint);

        if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Normal || TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_HighLow) {
            // 普通模式或高低电平模式
            switch (CurrCh) {
                case TChan.Ch1:
                    if (getCurrentTriggerIndex() == 1) {
                        if (channelCount == 2) {
                            drawModeShowDrag(canvas, ch1_up, ch1_down, out_up, ch2_down);
                        } else if (channelCount == 4) {
                            drawModeShowDrag(canvas, ch1_up, ch1_down, out_up, ch2_down);
                        } else if (channelCount == 8) {
                            drawModeShowDrag(canvas, ch1_up, ch1_down, out_up, ch2_down);
                        }
                    } else {
                        if (channelCount == 2) {
                            drawModeShowDrag(canvas, ch1_up, ch1_down, ch2_up, ch2_down);
                        } else if (channelCount == 4) {
                            drawModeShowDrag(canvas, ch1_up, ch1_down, ch4_up, ch2_down);
                        } else if (channelCount == 8) {
                            drawModeShowDrag(canvas, ch1_up, ch1_down, ch8_up, ch2_down);
                        }
                    }
                    break;
                case TChan.Ch2:
                    if (channelCount == 2) {
                        drawModeShowDrag(canvas, ch2_up, ch2_down, ch1_up, ch1_down);
                    } else{
                        drawModeShowDrag(canvas, ch2_up, ch2_down, ch1_up, ch3_down);
                    }
                    break;
                case TChan.Ch3:
                    drawModeShowDrag(canvas, ch3_up, ch3_down, ch2_up, ch4_down);
                    break;
                case TChan.Ch4:
                    if (channelCount==4) {
                        drawModeShowDrag(canvas, ch4_up, ch4_down, ch3_up, ch1_down);
                    }else {
                        drawModeShowDrag(canvas, ch4_up, ch4_down, ch3_up, ch5_down);
                    }
                    break;
                case TChan.Ch5:
                    drawModeShowDrag(canvas, ch5_up, ch5_down, ch4_up, ch6_down);
                    break;
                case TChan.Ch6:
                    drawModeShowDrag(canvas, ch6_up, ch6_down, ch5_up, ch7_down);
                    break;
                case TChan.Ch7:
                    drawModeShowDrag(canvas, ch7_up, ch7_down, ch6_up, ch8_down);
                    break;
                case TChan.Ch8:
                    if(getCurrentTriggerIndex() == 1) {
                        drawModeShowDrag(canvas, ch8_up, ch8_down, ch7_up, out_down);
                    } else {
                        drawModeShowDrag(canvas, ch8_up, ch8_down, ch7_up, ch1_down);
                    }
                    break;
                case TChan.Ch8 + 1:
                    if (getCurrentTriggerIndex() == 1) {
                        drawModeShowDrag(canvas, out_up, out_down, ch8_up, ch1_down);
                    }
                    break;
            }
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Logic) {
            // 逻辑触发模式
            switch (CurrCh) {
                case TChan.Ch1:
                    drawModeShowDrag(canvas, ch1_up, ch1_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
                case TChan.Ch2:
                    drawModeShowDrag(canvas, ch2_up, ch2_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
                case TChan.Ch3:
                    drawModeShowDrag(canvas, ch3_up, ch3_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
                case TChan.Ch4:
                    drawModeShowDrag(canvas, ch4_up, ch4_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
                case TChan.Ch5:
                    drawModeShowDrag(canvas, ch5_up, ch5_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
                case TChan.Ch6:
                    drawModeShowDrag(canvas, ch6_up, ch6_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
                case TChan.Ch7:
                    drawModeShowDrag(canvas, ch7_up, ch7_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
                case TChan.Ch8:
                    drawModeShowDrag(canvas, ch8_up, ch8_down, bmp[getPreCh(CurrCh)][1], bmp[getNextCh(CurrCh)][0]);
                    break;
            }
        }
    }

    /**
     * 绘制拖动模式的界面
     *
     * @param canvas 画布
     * @param curr_up 当前通道上箭头
     * @param curr_down 当前通道下箭头
     * @param up 上方通道的上箭头
     * @param down 下方通道的下箭头
     */
    private void drawModeShowDrag(Canvas canvas, Bitmap curr_up, Bitmap curr_down, Bitmap up, Bitmap down) {
        int paddingEnd = getPaddingEnd();
        Rect src = new Rect(0, 0, curr_up.getWidth(), curr_up.getHeight());
        Rect des = new Rect(0, this.getHeight() - curr_up.getHeight(), curr_up.getWidth(), this.getHeight());
        int leftBmp = (this.getWidth() - ch1_down.getWidth() - getPaddingEnd()) / 2;
        int leftTxt = (this.getWidth() - 20 - paddingEnd) / 2;

        if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Normal) {
            // 普通模式：绘制上下箭头
            canvas.drawBitmap(up, leftBmp, leftBmp, null);
            canvas.drawBitmap(down, leftBmp, des.top - leftBmp, null);
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_HighLow) {
            // 高低电平模式：绘制H/L标记
            mPaint.setStyle(Paint.Style.FILL);
            if (TriggerLevel_Mode_Work_HighLow_State) {
                canvas.drawBitmap(curr_up, leftBmp, leftBmp, null);
                mPaint.setColor(TChan.getChannelColor(context, CurrCh));
                canvas.drawText("L", leftTxt, leftBmp + 60, mPaint);
                canvas.drawBitmap(curr_down, leftBmp, des.top - leftBmp, null);
                canvas.drawText("L", leftTxt, this.getHeight() - 40 - leftBmp, mPaint);
            } else {
                canvas.drawBitmap(curr_up, leftBmp, leftBmp, null);
                mPaint.setColor(TChan.getChannelColor(context, CurrCh));
                canvas.drawText("H", leftTxt, leftBmp + 60, mPaint);
                canvas.drawBitmap(curr_down, leftBmp, des.top - leftBmp, null);
                canvas.drawText("H", leftTxt, this.getHeight() - 40 - leftBmp, mPaint);
            }
        } else {
            // 逻辑触发模式
            canvas.drawBitmap(up, leftBmp, leftBmp, null);
            canvas.drawBitmap(down, leftBmp, des.top - leftBmp, null);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 移动菜单监听器
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 左滑标志 */
    private boolean moveLeft = false;

    /** 上下滑动标志 */
    private boolean moveUpDown = false;

    /** 移动菜单监听器 */
    private MoveLevelMenuListener onMoveLevelMenuListener;

    /**
     * 设置移动菜单监听器
     *
     * @param onMoveLevelMenuListener 移动菜单监听器
     */
    public void setOnMoveLevelMenuListener(MoveLevelMenuListener onMoveLevelMenuListener) {
        this.onMoveLevelMenuListener = onMoveLevelMenuListener;
    }

    /**
     * 移动菜单监听接口
     * 用于处理控件的左滑隐藏和可见性变化
     */
    public interface MoveLevelMenuListener {
        /**
         * 正在移动回调
         *
         * @param triggerLevel 触发控件
         * @param moveX X方向移动距离
         */
        void onMoving(MTriggerLevel triggerLevel, int moveX);

        /**
         * 移动结束回调
         *
         * @param triggerLevel 触发控件
         */
        void onMoveEnd(MTriggerLevel triggerLevel);

        /**
         * 可见性变化回调
         *
         * @param triggerLevel 触发控件
         * @param visible 是否可见
         */
        void onLevelVisible(MTriggerLevel triggerLevel, boolean visible);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 滑动方向常量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 滑动方向：无 */
    public static final int SliderDir_None = 0x00;

    /** 滑动方向：从左到右 */
    public static final int SliderDir_LeftToRight = 0x01;

    /** 滑动方向：从右到左 */
    public static final int SliderDir_RightToLeft = ~SliderDir_LeftToRight;

    /** 滑动方向：从上到下 */
    public static final int SliderDir_TopToBottom = 0x02;

    /** 滑动方向：从下到上 */
    public static final int SliderDir_BottomToTop = ~SliderDir_TopToBottom;

    /** 当前滑动方向 */
    private int slipDir = SliderDir_None;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触摸事件处理
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 处理触摸事件
     * 支持点击、滑动、多点触控等手势
     *
     * @param event 触摸事件
     * @return true表示事件已处理
     */
    public boolean dealTouchEvent(MotionEvent event) {
        int[] location = new int[2];
        getLocationOnScreen(location);
        autoDragToButton();

        // 更新上下箭头的触摸区域
        upRect.set(location[0], location[1], location[0] + BUTTONHEIGHT, location[1] + BUTTONHEIGHT);
        downRect.set(location[0], location[1] + this.getHeight() - BUTTONHEIGHT, location[0] + BUTTONHEIGHT, location[0] + this.getHeight());

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                downState = true;
                oldX = lastX = (int) event.getRawX();
                oldY = lastY = (int) event.getRawY();
                moveLeft = false;
                moveUpDown = false;
                slipDir = SliderDir_None;

                // 点击按钮区域，展开到拖动模式
                if (getViewInScreen().contains((int) event.getRawX(), (int) event.getRawY())
                        && TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Button
                ) {
                    Animation_ButtonToDrag(OPENTYPE_TRIGGER);
                    if (onClickListener != null) {
                        onClickListener.onClick(this);
                    }
                    break;
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                // 判断滑动方向
                if (slipDir == SliderDir_None) {
                    if (!moveUpDown && Math.abs(oldY - event.getRawY()) >= 20) {
                        moveUpDown = true;
                        slipDir = SliderDir_TopToBottom;
                    } else if (!moveLeft && !moveUpDown && oldX - event.getRawX() >= 20) {
                        moveLeft = true;
                        slipDir = SliderDir_RightToLeft;
                    }
                }

                // 处理左滑隐藏
                if (slipDir == SliderDir_RightToLeft && openType == OPENTYPE_TRIGGER) {
                    if (onMoveLevelMenuListener != null) {
                        onMoveLevelMenuListener.onMoving(MTriggerLevel.this, (int) (oldX - event.getRawX()));
                    }
                } else if (slipDir == SliderDir_TopToBottom) {
                    // 处理上下滑动
                    if (this.TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag) {
                        // 检查是否在箭头区域内
                        if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag && upRect.contains(oldX, oldY) &&
                                upRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            break;
                        } else if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag && downRect.contains(oldX, oldY) &&
                                downRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            break;
                        } else if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag) {
                            Rect rect = getViewRect(this);
                            if (!rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                                break;
                            }
                        }

                        // 计算移动距离并回调
                        int tempY = lastY - (int) event.getRawY();
                        setChannelMoveChannel(TBookUtil.isFine() ? (tempY / TBookUtil.getNumFine()) : tempY);

                        if (TBookUtil.isFine()) {
                            if (Math.abs(tempY) > TBookUtil.getNumFine()) {
                                lastY = (int) event.getRawY();
                            }
                        } else {
                            lastY = (int) event.getRawY();
                        }
                    }
                }
            }
            break;

            case MotionEvent.ACTION_UP: {
                downState = false;

                // 处理左滑结束
                if (moveLeft && openType == OPENTYPE_TRIGGER) {
                    if (onMoveLevelMenuListener != null) {
                        onMoveLevelMenuListener.onMoveEnd(MTriggerLevel.this);
                    }
                }

                // 检查是否在按钮区域
                if (getViewInScreen().contains((int) event.getRawX(), (int) event.getRawY()) &&
                        TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Button) {
                    break;
                }

                // 处理上箭头点击
                if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag && upRect.contains(oldX, oldY) &&
                        upRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    setChangeUpChannel();
                    break;
                }
                // 处理下箭头点击
                else if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Drag && downRect.contains(oldX, oldY) &&
                        downRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    setChangeDownChannel();
                    break;
                } else {
                    // 移动完成回调
                    if (onMouseMoveListener != null)
                        onMouseMoveListener.onMouseMoveComplete(this, openType);
                }
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                // 多点触控按下
                lastX = oldX = (int) event.getRawX();
                lastY = oldY = (int) event.getRawY();
            }
            break;

            case MotionEvent.ACTION_POINTER_UP: {
                // 多点触控抬起，更新焦点
                int id = event.getActionIndex();
                if (id == 0) {
                    int left = (int) (event.getRawX() - event.getX(0));
                    int top = (int) (event.getRawY() - event.getY(0));
                    lastX = oldX = (int) event.getX(1) + left;
                    lastY = oldY = (int) event.getY(1) + top;
                } else {
                    lastX = oldX = (int) event.getRawX();
                    lastY = oldY = (int) event.getRawY();
                }
            }
            break;
        }

        return true;
    }

    /**
     * 设置通道移动
     *
     * @param deltaY 移动距离（正数表示向上移动）
     */
    public void setChannelMoveChannel(double deltaY) {
        if (onMouseMoveListener != null) {
            onMouseMoveListener.onMouseMove(this, deltaY, CurrCh, openType, true);
        }
    }

    /**
     * 判断是否为第一个通道
     *
     * @return true表示是第一个通道
     */
    public boolean isFirstChannel() {
        if (this.TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Normal) {
            return CurrCh == TChan.Ch1;

        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_HighLow) {
            return TriggerLevel_Mode_Work_HighLow_State;

        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Logic) {
            boolean bFirst = true;
            for (int i = CurrCh - 1; i >= 0; i--) {
                if (TriggerLevel_Mode_Work_Logic_state[i] != TriggerLevel_Mode_work_Logic_None) {
                    bFirst = false;
                    break;
                }
            }
            return bFirst;
        }
        return false;
    }

    /**
     * 判断是否为最后一个通道
     *
     * @param chnums 通道总数
     * @return true表示是最后一个通道
     */
    public boolean isLastChannel(int chnums) {
        if (this.TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Normal) {
            if (chnums == 2) {
                return CurrCh == TChan.Ch2;
            } else if (chnums == 4) {
                return CurrCh == TChan.Ch4;
            } else {
                return CurrCh == TChan.Ch8;
            }

        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_HighLow) {
            return !TriggerLevel_Mode_Work_HighLow_State;

        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Logic) {
            boolean bLast = true;
            for (int i = CurrCh + 1; i < TriggerLevel_Mode_Work_Logic_state.length; i++) {
                if (TriggerLevel_Mode_Work_Logic_state[i] != TriggerLevel_Mode_work_Logic_None) {
                    bLast = false;
                    break;
                }
            }
            return bLast;
        }
        return false;
    }

    /**
     * 获取可切换的通道数量
     *
     * @return 可切换的通道数量
     */
    public int getChangeChannelCount() {
        int nums = 4;
        if (this.TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Normal) {
            nums = 4;
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_HighLow) {
            nums = 2;
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Logic) {
            nums = 0;
            for (int i = 1; i < TriggerLevel_Mode_Work_Logic_state.length; i++) {
                if (TriggerLevel_Mode_Work_Logic_state[i] != TriggerLevel_Mode_work_Logic_None) {
                    nums++;
                }
            }
        }
        return nums;
    }

    /**
     * 切换到下一个通道
     */
    public void setChangeDownChannel() {
        Logger.i("down Click");
        int finalCount = channelCount + 1;
        if(getCurrentTriggerIndex() == 1) {
            finalCount = channelCount + 2;
        }

        if (this.TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Normal) {
            if ((getCurrCh() + 1) % (finalCount) == 0) {
                setCurrCh(TChan.Ch1);
            } else {
                setCurrCh(Math.abs(getCurrCh() + 1));
            }
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_HighLow) {
            setTriggerLevel_Mode_Work_HighLow_State(!TriggerLevel_Mode_Work_HighLow_State);
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Logic) {
            setCurrCh(getNextCh(CurrCh));
        }

        if (onMouseMoveListener != null) {
            onMouseMoveListener.onDownClick(this, CurrCh, openType);
        }
    }

    /**
     * 切换到上一个通道
     */
    public void setChangeUpChannel() {
        Logger.i("up Click");
        int finalCount = channelCount + 1;
        if(getCurrentTriggerIndex() == 1) {
            finalCount = channelCount + 2;
        }

        if (this.TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Normal) {
            if ((getCurrCh() - 1) % (finalCount) == 0) {
                setCurrCh(finalCount - 1);
            } else {
                setCurrCh(Math.abs(getCurrCh() - 1));
            }
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_HighLow) {
            setTriggerLevel_Mode_Work_HighLow_State(!TriggerLevel_Mode_Work_HighLow_State);
        } else if (TriggerLevel_Mode_Work == TriggerLevel_Mode_Work_Logic) {
            setCurrCh(getPreCh(CurrCh));
        }

        if (onMouseMoveListener != null) {
            onMouseMoveListener.onUpClick(this, CurrCh, openType);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 视图位置辅助方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 位置数组 */
    private int[] location = new int[2];

    /** 控件矩形 */
    private Rect controlRect = new Rect();

    /**
     * 获取控件在屏幕中的位置矩形
     *
     * @return 控件矩形
     */
    private Rect getViewInScreen() {
        this.getLocationOnScreen(location);
        controlRect.set(location[0], location[1], this.getWidth() + location[0], this.getHeight() + location[1]);
        return controlRect;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 自动收起处理
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 自动收起消息 */
    private static final int MSG_AUTO_DRAGTOBTN = 0x666;

    /** Handler用于处理自动收起逻辑 */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_AUTO_DRAGTOBTN:
                    if (getTriggerLevel_Mode_Show() == TriggerLevel_Mode_Show_Drag) {
                        Animation_DragToButton();
                    }
                    break;
            }
        }
    };

    /**
     * 启动自动收起计时
     * 5秒无操作自动收起到按钮模式
     */
    public void autoDragToButton() {
        if (handler.hasMessages(MSG_AUTO_DRAGTOBTN)) {
            handler.removeMessages(MSG_AUTO_DRAGTOBTN);
        }
        handler.sendEmptyMessageDelayed(MSG_AUTO_DRAGTOBTN, 5000);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 动画效果
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 按钮到拖动模式的动画 */
    private ObjectAnimator oaButtonToDrag;

    /**
     * 执行从按钮模式到拖动模式的动画
     *
     * @param type 打开类型
     */
    public void Animation_ButtonToDrag(@OpenType final int type) {
        openType = type;
        TriggerLevel_Mode_Show = TriggerLevel_Mode_Show_Changing;

        // 创建高度变化动画
        oaButtonToDrag = ObjectAnimator.ofInt(this, "Animation_ViewHeight", BUTTONHEIGHT, DRAGHEIGHT);
        oaButtonToDrag.setDuration(150);
        oaButtonToDrag.start();

        oaButtonToDrag.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int cVal = (int) animation.getAnimatedValue();
                if (cVal >= DRAGHEIGHT) {
                    // 动画完成，切换到拖动模式
                    setTriggerLevel_Mode_Show(TriggerLevel_Mode_Show_Drag);
                    if (onOpenCloseListener != null) {
                        onOpenCloseListener.onOpen(type);
                    }
                    return;
                }
                setAnimation_ViewHeight(cVal);
                autoDragToButton();
            }
        });
    }

    /** 拖动到按钮模式的动画 */
    private ObjectAnimator oaDragToButton;

    /**
     * 执行从拖动模式到按钮模式的动画
     */
    public void Animation_DragToButton() {
        if (TriggerLevel_Mode_Show != TriggerLevel_Mode_Show_Drag) return;
        TriggerLevel_Mode_Show = TriggerLevel_Mode_Show_Changing;

        // 移除自动收起消息
        if (handler.hasMessages(MSG_AUTO_DRAGTOBTN)) {
            handler.removeMessages(MSG_AUTO_DRAGTOBTN);
        }

        // 创建高度变化动画
        oaDragToButton = ObjectAnimator.ofInt(this, "Animation_ViewHeight", DRAGHEIGHT, BUTTONHEIGHT);
        oaDragToButton.setDuration(150);
        oaDragToButton.start();

        oaDragToButton.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int cVal = (int) animation.getAnimatedValue();
                if (cVal <= BUTTONHEIGHT) {
                    // 动画完成，切换到按钮模式
                    setTriggerLevel_Mode_Show(TriggerLevel_Mode_Show_Button);
                    if (onOpenCloseListener != null) {
                        onOpenCloseListener.onClose(openType);
                    }
                    return;
                }
                setAnimation_ViewHeight(cVal);
            }
        });

        // 通知可见性变化
        if (onMoveLevelMenuListener != null) {
            onMoveLevelMenuListener.onLevelVisible(MTriggerLevel.this, false);
        }
    }

    /**
     * 设置可见性
     * 同时通知可见性变化
     *
     * @param visibility 可见性状态
     */
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (onMoveLevelMenuListener != null) {
            onMoveLevelMenuListener.onLevelVisible(MTriggerLevel.this, visibility == VISIBLE);
        }
    }

    /**
     * 检查指定点是否在控件内
     *
     * @param x X坐标
     * @param y Y坐标
     * @return true表示在控件内
     */
    public boolean hasPoint(int x, int y) {
        return getVisibility() == VISIBLE && getViewInScreen().contains(x, y);
    }

    /**
     * 判断是否为串口类型
     *
     * @param type 打开类型
     * @return true表示是串口类型
     */
    public static boolean isSerialType(int type) {
        return (type >= OPENTYPE_SERIALS1) && (type <= OPENTYPE_SERIALS4);
    }

    /**
     * 根据串口号获取打开类型
     *
     * @param sNo 串口号
     * @return 打开类型
     */
    public static @OpenType
    int getOpenType(int sNo) {
        return sNo;
    }

    /**
     * 获取视图的屏幕矩形
     *
     * @param v 视图
     * @return 矩形区域
     */
    private Rect getViewRect(View v) {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        Rect rect = new Rect(x, y, x + v.getWidth(), y + v.getHeight());
        return rect;
    }

    /**
     * 获取当前触发类型索引
     *
     * @return 触发类型索引
     */
    public int getCurrentTriggerIndex() {
        return currentTriggerIndex;
    }

    /**
     * 设置当前触发类型索引
     *
     * @param currentTriggerIndex 触发类型索引
     */
    public void setCurrentTriggerIndex(int currentTriggerIndex) {
        this.currentTriggerIndex = currentTriggerIndex;
    }

    /**
     * 设置中心Y坐标
     * 根据波形高度重新计算中心位置
     *
     * @param height 波形高度
     */
    public void setCenterY(int height) {
        waveHeight = height;
        CenterY = (height - BUTTONHEIGHT) / 2.0f;

        if (TriggerLevel_Mode_Show == TriggerLevel_Mode_Show_Button) {
            // 更新动画参数
            if (oaButtonToDrag != null) {
                oaButtonToDrag.setIntValues(BUTTONHEIGHT, DRAGHEIGHT);
            }
            if (oaDragToButton != null) {
                oaDragToButton.setIntValues(BUTTONHEIGHT, DRAGHEIGHT);
            }

            // 重新缩放滑块背景
            Bitmap temp = BitmapUtil.getBitmapFromDrawable(context, R.drawable.trigger_level_slider);
            levelSlider = Bitmap.createScaledBitmap(levelSlider, levelSlider.getWidth(), DRAGHEIGHT, true);
            setY(CenterY);
            invalidate();
        } else {
            autoDragToButton();
        }
    }
}
