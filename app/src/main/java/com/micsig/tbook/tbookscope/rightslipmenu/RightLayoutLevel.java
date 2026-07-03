package com.micsig.tbook.tbookscope.rightslipmenu;

import android.content.Context;                                                       // 上下文环境
import android.graphics.drawable.Drawable;                                            // 可绘制资源
import android.util.AttributeSet;                                                     // XML属性集
import android.view.View;                                                             // 视图基类
import android.widget.ImageView;                                                      // 图片视图
import android.widget.LinearLayout;                                                   // 线性布局
import android.widget.RadioButton;                                                    // 单选按钮
import android.widget.RadioGroup;                                                     // 单选按钮组
import android.widget.RelativeLayout;                                                 // 相对布局

import com.micsig.base.Logger;                                                        // 日志工具
import com.micsig.base.widget.MyRadioGroup;                                           // 自定义单选按钮组（支持多行布局）
import com.micsig.tbook.scope.Event.EventBase;                                        // 事件基类
import com.micsig.tbook.scope.Event.EventFactory;                                     // 事件工厂
import com.micsig.tbook.scope.Event.EventUIObserver;                                  // 事件UI观察者
import com.micsig.tbook.scope.Trigger.TriggerCommon;                                  // 触发通用常量
import com.micsig.tbook.scope.Trigger.TriggerFactory;                                 // 触发工厂
import com.micsig.tbook.tbookscope.GlobalVar;                                         // 全局变量
import com.micsig.tbook.tbookscope.LoadCache;                                         // 缓存加载标记
import com.micsig.tbook.tbookscope.R;                                                 // 资源ID
import com.micsig.tbook.tbookscope.main.mainright.MainHolderTriggerLevel;             // 触发电平持有者
import com.micsig.tbook.tbookscope.main.mainright.MainMsgTriggerLevel;                // 触发电平消息
import com.micsig.tbook.tbookscope.rxjava.RxBus;                                     // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;                                    // RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound;                                   // 按键音效
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger;               // 顶部触发布局
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTrigger;                  // 顶部触发消息
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerChannel;           // 顶部触发通道消息
import com.micsig.tbook.tbookscope.util.CacheUtil;                                   // 缓存工具
import com.micsig.tbook.ui.util.TimerUtils;                                           // 定时器工具
import com.micsig.tbook.ui.wavezone.TChan;                                            // 通道常量

import java.util.ArrayList;                                                           // 动态数组
import java.util.Arrays;                                                              // 数组工具
import java.util.List;                                                                // 列表接口

import io.reactivex.rxjava3.annotations.NonNull;                                      // 非空注解
import io.reactivex.rxjava3.functions.Consumer;                                       // RxJava消费者接口


/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                         RightLayoutLevel                                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：右侧滑出菜单 - 触发电平参数设置面板                                  ║
 * ║ 核心职责：管理触发类型子选项/触发模式/触发源通道的三级选择界面，                   ║
 * ║          并通过RxBus将选择结果发送给业务层                                      ║
 * ║ 架构设计：继承RelativeLayout的自定义视图，采用三级RadioGroup布局，              ║
 * ║          顶部-触发类型子选项、中间-自动/普通模式、底部-源通道选择                ║
 * ║          通过RxBus订阅外部事件（缓存加载/触发切换/通道切换等）                   ║
 * ║ 数据流向：UI操作 → onCheckedChangeListener → RightMsgLevel → RxBus → 订阅方  ║
 * ║          外部事件 → RxBus/EventUIObserver → UI更新 → RightMsgLevel → RxBus    ║
 * ║ 依赖关系：RxBus, CacheUtil, TimerUtils, TriggerFactory, TopLayoutTrigger,    ║
 * ║           RightMsgLevel, MyRadioGroup, PlaySound, TChan                      ║
 * ║ 使用场景：用户在右侧菜单中设置触发电平参数时显示此面板                            ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 触发电平参数设置面板
 * <p>
 * 继承RelativeLayout，提供三级触发参数选择界面：
 * - 顶部：触发类型子选项（如上升沿/下降沿/双边沿、正/负脉冲等）
 * - 中间：自动/普通触发模式切换
 * - 底部：触发源通道选择（CH1-CH8 + 外部触发）
 * <p>
 * 支持的触发类型包括：边沿(Edge)、脉宽(Pulse)、N边沿(NEdge)、
 * Runt、Slope、Timeout、Video、Logic及序列触发(S1-S4)
 * </p>
 */
public class RightLayoutLevel extends RelativeLayout {
    /** 日志标签 */
    private static final String TAG = RightLayoutLevel.class.getSimpleName();
    /** 上下文引用 */
    private Context context;

    /** 顶部RadioGroup：触发类型子选项（如上升沿/下降沿/双边沿） */
    private RadioGroup rgTopLayout;
    /** 顶部图标显示容器 */
    private LinearLayout llShowTopLayout;
    /** 中间RadioGroup：自动/普通触发模式 */
    private RadioGroup rgMiddleLayout;
    /** 底部MyRadioGroup：触发源通道选择（支持多行布局） */
    private MyRadioGroup rgBottomLayout;
    /** 顶部子选项1（如上升沿/正脉冲） */
    private RadioButton rbTop1;
    /** 顶部子选项2（如下降沿/负脉冲） */
    private RadioButton rbTop2;
    /** 顶部子选项3（如双边沿/任意脉冲） */
    private RadioButton rbTop3;
    /** 顶部子选项1的图标 */
    private ImageView ivShowTop1;
    /** 顶部子选项2的图标 */
    private ImageView ivShowTop2;
    /** 顶部子选项3的图标 */
    private ImageView ivShowTop3;
    /** 中间自动触发模式按钮 */
    private RadioButton rbAuto;
    /** 中间普通触发模式按钮 */
    private RadioButton rbNormal;
    /** 底部通道1-8 + 外部触发单选按钮 */
    private RadioButton rbBottom1, rbBottom2, rbBottom3, rbBottom4, rbBottom5, rbBottom6, rbBottom7, rbBottom8, rbBottom9;

    /** 底部单选按钮列表（便于批量操作） */
    private final List<RadioButton> rbBottoms = new ArrayList<>();
    /** 触发电平消息封装对象 */
    private RightMsgLevel msgLevel;
    /** 按钮点击监听器（供外部注册回调） */
    private OnButtonClickListener onButtonClickListener;
    /** 自动隐藏定时器 */
    private TimerUtils timer;
    /** 顶部/中间/底部按钮行背景Drawable */
    private Drawable drawableTop, drawableMiddle, drawableBottom;

    /** 当前设备通道数量 */
    private final int channelCount = GlobalVar.get().getChannelsCount();

    /** TChan通道常量列表，用于索引映射 */
    private final List<Integer> tChanChX = Arrays.asList(
            TChan.Ch1, TChan.Ch2, TChan.Ch3, TChan.Ch4,
            TChan.Ch5, TChan.Ch6, TChan.Ch7, TChan.Ch8
    );

    /**
     * 按钮点击监听接口
     * <p>当用户在任一RadioGroup中选中RadioButton时回调</p>
     */
    public interface OnButtonClickListener {
        /**
         * 按钮点击回调
         * @param group 所属RadioGroup
         * @param radioButton 被选中的RadioButton
         */
        void onClick(RadioGroup group, RadioButton radioButton);
    }

    /**
     * 获取按钮点击监听器
     * @return 当前的按钮点击监听器
     */
    public OnButtonClickListener getOnButtonClickListener() {
        return onButtonClickListener;
    }

    /**
     * 设置按钮点击监听器
     * @param onButtonClickListener 按钮点击监听器
     */
    public void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    /**
     * 单参数构造方法
     * @param context 上下文
     */
    public RightLayoutLevel(Context context) {
        this(context, null);                                                            // 委托给两参数构造
    }

    /**
     * 两参数构造方法
     * @param context 上下文
     * @param attrs XML属性集
     */
    public RightLayoutLevel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);                                                        // 委托给三参数构造
    }

    /**
     * 三参数构造方法（最终初始化入口）
     * @param context 上下文
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public RightLayoutLevel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);                                            // 调用父类构造
        this.context = context;                                                         // 保存上下文引用
        initDrawable();                                                                 // 初始化背景Drawable
        initView();                                                                     // 初始化视图控件
        initMsg();                                                                      // 初始化消息对象
        initControl();                                                                  // 初始化事件订阅
    }

    /**
     * 初始化三级按钮行的背景Drawable
     * <p>分别为顶部行、中间行、底部行加载不同圆角的背景</p>
     */
    private void initDrawable() {
        drawableTop = context.getResources().getDrawable(R.drawable.bg_right_level_click_top);      // 顶部行背景（上圆角）
        drawableMiddle = context.getResources().getDrawable(R.drawable.bg_right_level_click_middle); // 中间行背景（无圆角）
        drawableBottom = context.getResources().getDrawable(R.drawable.bg_right_level_click_bottom); // 底部行背景（下圆角）
    }

    /**
     * 初始化视图控件
     * <p>加载布局、绑定控件引用、设置监听器、添加底部按钮到列表</p>
     */
    private void initView() {
        View.inflate(context, R.layout.layout_right_level, this);                       // 填充布局文件
        rgTopLayout = (RadioGroup) findViewById(R.id.rightLevelTopLayout);              // 顶部RadioGroup
        rgMiddleLayout = (RadioGroup) findViewById(R.id.rightLevelMiddleLayout);        // 中间RadioGroup
        rgBottomLayout = (MyRadioGroup) findViewById(R.id.rightLevelBottomLayout);      // 底部MyRadioGroup

        rbTop1 = (RadioButton) findViewById(R.id.rightLevelTop1);                       // 顶部子选项1
        rbTop2 = (RadioButton) findViewById(R.id.rightLevelTop2);                       // 顶部子选项2
        rbTop3 = (RadioButton) findViewById(R.id.rightLevelTop3);                       // 顶部子选项3
        rbAuto = (RadioButton) findViewById(R.id.rightLevelAuto);                       // 自动模式按钮
        rbNormal = (RadioButton) findViewById(R.id.rightLevelNormal);                   // 普通模式按钮
        rbBottom1 = (RadioButton) findViewById(R.id.rightLevelBottom1);                 // 底部CH1按钮
        rbBottom2 = (RadioButton) findViewById(R.id.rightLevelBottom2);                 // 底部CH2按钮
        rbBottom3 = (RadioButton) findViewById(R.id.rightLevelBottom3);                 // 底部CH3按钮
        rbBottom4 = (RadioButton) findViewById(R.id.rightLevelBottom4);                 // 底部CH4按钮
        rbBottom5 = (RadioButton) findViewById(R.id.rightLevelBottom5);                 // 底部CH5按钮
        rbBottom6 = (RadioButton) findViewById(R.id.rightLevelBottom6);                 // 底部CH6按钮
        rbBottom7 = (RadioButton) findViewById(R.id.rightLevelBottom7);                 // 底部CH7按钮
        rbBottom8 = (RadioButton) findViewById(R.id.rightLevelBottom8);                 // 底部CH8按钮
        rbBottom9 = (RadioButton) findViewById(R.id.rightLevelBottom9);                 // 底部外部触发按钮
        rbBottoms.add(rbBottom1);                                                       // 添加CH1到列表
        rbBottoms.add(rbBottom2);                                                       // 添加CH2到列表
        rbBottoms.add(rbBottom3);                                                       // 添加CH3到列表
        rbBottoms.add(rbBottom4);                                                       // 添加CH4到列表
        rbBottoms.add(rbBottom5);                                                       // 添加CH5到列表
        rbBottoms.add(rbBottom6);                                                       // 添加CH6到列表
        rbBottoms.add(rbBottom7);                                                       // 添加CH7到列表
        rbBottoms.add(rbBottom8);                                                       // 添加CH8到列表
        rbBottoms.add(rbBottom9);                                                       // 添加外部触发到列表

        llShowTopLayout = (LinearLayout) findViewById(R.id.rightLevelShowTopLayout);    // 顶部图标容器
        ivShowTop1 = (ImageView) findViewById(R.id.rightLevelShowTop1);                 // 顶部图标1
        ivShowTop2 = (ImageView) findViewById(R.id.rightLevelShowTop2);                 // 顶部图标2
        ivShowTop3 = (ImageView) findViewById(R.id.rightLevelShowTop3);                 // 顶部图标3

        rgTopLayout.setOnCheckedChangeListener(onCheckedChangeListener);                // 顶部选项变更监听
        rgMiddleLayout.setOnCheckedChangeListener(onCheckedChangeListener);              // 中间选项变更监听
        rgBottomLayout.setOnCheckedChangeListener(onCheckedChangeListener);              // 底部选项变更监听
        rgBottomLayout.setChildBackGround(drawableTop, drawableMiddle, drawableBottom); // 设置底部按钮行背景
    }

    /**
     * 初始化消息对象
     * <p>设置RightMsgLevel的初始选项数量和选中索引</p>
     */
    private void initMsg() {
        msgLevel = new RightMsgLevel();                                                 // 创建消息对象
        msgLevel.setTopCount(3);                                                        // 顶部默认3个选项
        msgLevel.setMiddleCount(2);                                                     // 中间默认2个选项（自动/普通）
//        msgLevel.setBottomCount(channelCount);                                         // 底部选项数=通道数
        msgLevel.setTopSelect(0);                                                       // 顶部默认选中第1项
        msgLevel.setMiddleSelect(0);                                                    // 中间默认选中自动模式
        msgLevel.setBottomSelect(0);                                                    // 底部默认选中CH1
    }

    /**
     * 初始化事件订阅
     * <p>订阅RxBus事件和EventFactory事件，注册自动隐藏定时器</p>
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);             // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_TRIGGER).subscribe(consumerTopTrigger);           // 订阅顶部触发类型切换
        RxBus.getInstance().getObservable(RxEnum.TOPTRIGGER_CHANNEL).subscribe(consumerTopTriggerChannel);   // 订阅顶部触发通道切换
        RxBus.getInstance().getObservable(RxEnum.MAINLEFT_MENU_AUTO).subscribe(consumerMainLeftMenuAuto);   // 订阅自动模式事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_TRIGGERLEVEL_TRIGGERCHANNEL).subscribe(consumerTriggerLevel); // 订阅触发电平通道
        RxBus.getInstance().getObservable(RxEnum.EXTERNALKEYS_MODE).subscribe(consumerModeChange);          // 订阅外部按键模式切换
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_COMMON_MODE, eventUIObserver);             // 注册触发模式变更观察者

        timer = new TimerUtils(timeOut);                                                // 创建自动隐藏定时器
        timer.setIntervalMs(4000);                                                      // 设置超时4秒
    }

    /**
     * 自动隐藏定时器回调
     * <p>面板可见4秒无操作后自动隐藏</p>
     */
    private TimerUtils.TimeOutEvent timeOut = new TimerUtils.TimeOutEvent() {
        @Override
        public void onTimeOut() {
            RightLayoutLevel.this.post(new Runnable() {                                 // 切回UI线程
                @Override
                public void run() {
                    RightLayoutLevel.this.setVisibility(GONE);                          // 隐藏面板
                }
            });
        }
    };

    /**
     * 从缓存加载触发参数并更新UI
     * <p>根据缓存中的触发模式和触发类型初始化面板状态</p>
     */
    private void setCache() {
        int triggerMode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_COMMON_MODE); // 读取触发模式缓存

        if (triggerMode == 0) {                                                         // 自动模式
            onlyCheck(rgMiddleLayout, rbAuto.getId());                                  // 选中自动按钮
        } else {                                                                        // 普通模式
            onlyCheck(rgMiddleLayout, rbNormal.getId());                                // 选中普通按钮
        }
        msgLevel.setMiddleSelect(triggerMode);                                          // 更新消息对象
        setTriggerIndex(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER));            // 设置触发类型索引
    }

    /**
     * 设置底部通道按钮的可见性和样式
     * @param isAllVisible true=全部可点击，false=按chxVisible列表决定
     * @param chxVisible 各通道是否可见的列表（最后一个是外部触发）
     */
    private void setBottomViewVisible(boolean isAllVisible, ArrayList<Boolean> chxVisible) {
        for (int i = 0; i < rbBottoms.size(); i++) {                                    // 遍历所有底部按钮
            if (i < chxVisible.size() - 1) {                                            // 非最后一个（通道按钮）
                rbBottoms.get(i).setEnabled(isAllVisible || chxVisible.get(i));         // 设置是否可点击
                rbBottoms.get(i).setText(TChan.getChannelName(i + 1));                  // 设置通道名称
                boolean type = isAllVisible ? true : chxVisible.get(i);                 // 决定颜色类型
                rbBottoms.get(i).setTextColor(type
                        ? TChan.getChannelColor(context, i + 1)
                        : context.getResources().getColor(R.color.main_text_color_disable)); // 设置文字颜色
            }
            if (i == chxVisible.size() - 1) {                                          // 最后一个（外部触发）
                ((RadioButton) rbBottoms.get(i)).setVisibility(chxVisible.get(i) ? View.VISIBLE : View.GONE); // 是否显示
                rbBottoms.get(i).setText(context.getResources().getString(R.string.top_edge_external_trigger)); // 设置"外部触发"文本
                rbBottoms.get(i).setTextColor(context.getResources().getColor(R.color.colorChCommon)); // 设置通用颜色
            }
        }
//        updateBackGround();
    }

    /**
     * 更新底部按钮的背景Drawable
     * <p>根据按钮在多行布局中的位置，分别设置顶部/中间/底部背景</p>
     */
    private void updateBackGround() {
        int maxRows = rgBottomLayout.getMaxRows();                                      // 获取每行最大按钮数
        for (int i = 0; i < rbBottoms.size(); i++) {                                    // 遍历所有底部按钮
            if (1 % maxRows == 0) {                                                     // 第一列（顶部行背景）
                rbBottoms.get(i).setBackgroundDrawable(drawableTop);
            } else if (1 % maxRows == maxRows - 1 || i == rbBottoms.size() - 1) {      // 最后列或最后按钮（底部行背景）
                rbBottoms.get(i).setBackgroundDrawable(drawableBottom);
            } else {                                                                    // 中间列（中间行背景）
                rbBottoms.get(i).setBackgroundDrawable(drawableMiddle);
            }
        }
        rgBottomLayout.requestLayout();                                                 // 请求重新布局
    }

    /**
     * 设置底部所有通道按钮的文本和颜色
     * <p>统一设置CH1-CH8的通道名和通道色</p>
     */
    private void setBottomTextAndColor() {
        for (int i = 0; i < rbBottoms.size(); i++) {                                    // 遍历所有底部按钮
            rbBottoms.get(i).setText(TChan.getChannelName(i + 1));                      // 设置通道名称
            rbBottoms.get(i).setTextColor(TChan.getChannelColor(context, i + 1));       // 设置通道颜色
        }
    }

    /**
     * 根据触发类型索引设置整个面板的UI状态
     * <p>这是核心方法，根据不同触发类型（边沿/脉宽/N边沿/Runt/Slope/Timeout/Video/Logic/序列）
     * 配置顶部选项的数量、图标、可见性，以及底部通道的可用性</p>
     * @param triggerIndex 触发类型索引（TopLayoutTrigger.DETAIL_xxx）
     */
    private void setTriggerIndex(int triggerIndex) {
        int source = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE);         // 读取触发源通道
        onlyCheck(rgBottomLayout, rbBottoms.get(source).getId());                       // 选中对应通道
        msgLevel.setBottomSelect(source);                                               // 更新消息对象
        int triggerMode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_COMMON_MODE); // 读取触发模式
        if (triggerMode == 0) {                                                         // 自动模式
            onlyCheck(rgMiddleLayout, rbAuto.getId());                                  // 选中自动
        } else {                                                                        // 普通模式
            onlyCheck(rgMiddleLayout, rbNormal.getId());                                // 选中普通
        }
        msgLevel.setMiddleSelect(triggerMode);                                          // 更新消息对象
        msgLevel.setTopCount(0);                                                        // 先置0，后续按类型设置

        ArrayList<Boolean> chXVisible = new ArrayList<>();                              // 通道可见性列表
        boolean triggerCh1Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH1) != 2; // CH1非"未使用"
        boolean triggerCh2Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH2) != 2; // CH2非"未使用"
        boolean triggerCh3Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH3) != 2; // CH3非"未使用"
        boolean triggerCh4Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH4) != 2; // CH4非"未使用"
        boolean triggerCh5Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH5) != 2; // CH5非"未使用"
        boolean triggerCh6Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH6) != 2; // CH6非"未使用"
        boolean triggerCh7Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH7) != 2; // CH7非"未使用"
        boolean triggerCh8Visible = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CH8) != 2; // CH8非"未使用"
        chXVisible.add(triggerCh1Visible);                                              // 添加CH1可见性
        chXVisible.add(triggerCh2Visible);                                              // 添加CH2可见性
        chXVisible.add(triggerCh3Visible);                                              // 添加CH3可见性
        chXVisible.add(triggerCh4Visible);                                              // 添加CH4可见性
        chXVisible.add(triggerCh5Visible);                                              // 添加CH5可见性
        chXVisible.add(triggerCh6Visible);                                              // 添加CH6可见性
        chXVisible.add(triggerCh7Visible);                                              // 添加CH7可见性
        chXVisible.add(triggerCh8Visible);                                              // 添加CH8可见性
        chXVisible.add(false);                                                          // 外部触发默认不可见
        if(triggerIndex == TopLayoutTrigger.DETAIL_EDGE) {                              // 边沿触发显示外部触发
            chXVisible.set(chXVisible.size() - 1, true);                               // 显示外部触发选项
        }
//        setBottomTextAndColor();
        setBottomViewVisible(triggerIndex != TopLayoutTrigger.DETAIL_LOGIC, chXVisible); // Logic触发全部不可点击，其他按可见性
        //其他触发的时候，CH8是显示的最后一个，所以更新下CH8的背景
        rgBottomLayout.getChildAt(rgBottomLayout.getChildCount() - 2).setBackground(drawableBottom); // CH8设为底部行背景
        switch (triggerIndex) {
//            case TopLayoutTrigger.DETAIL_COMMON:
//                rgTopLayout.setVisibility(GONE);
//                llShowTopLayout.setVisibility(GONE);
//
////                int triggerMode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_COMMON_MODE);
////                if (triggerMode == 0) {
////                    onlyCheck(rgMiddleLayout, rbAuto.getId());
////                } else {
////                    onlyCheck(rgMiddleLayout, rbNormal.getId());
////                }
////                msgLevel.setTopCount(0);
//                break;
            case TopLayoutTrigger.DETAIL_EDGE:                                          // 边沿触发
                rgTopLayout.setVisibility(VISIBLE);                                     // 显示顶部选项
                rbTop1.setVisibility(VISIBLE);                                          // 显示子选项1
                rbTop2.setVisibility(VISIBLE);                                          // 显示子选项2
                rbTop3.setVisibility(VISIBLE);                                          // 显示子选项3
                llShowTopLayout.setVisibility(VISIBLE);                                 // 显示图标容器
                ivShowTop1.setVisibility(VISIBLE);                                      // 显示图标1
                ivShowTop2.setVisibility(VISIBLE);                                      // 显示图标2
                ivShowTop3.setVisibility(VISIBLE);                                      // 显示图标3
                ivShowTop1.setImageResource(R.drawable.bg_right_level_rising);          // 上升沿图标
                ivShowTop2.setImageResource(R.drawable.bg_right_level_falling);         // 下降沿图标
                ivShowTop3.setImageResource(R.drawable.bg_right_level_double);          // 双边沿图标
                //N边沿的时候，CH8不是最后一个，所以更新下CH8的背景
                rgBottomLayout.getChildAt(rgBottomLayout.getChildCount() - 2).setBackground(drawableMiddle); // CH8改为中间行背景

                int edge = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_EDGE_EDGE); // 读取边沿类型缓存
                if (edge == 0) {                                                        // 上升沿
                    onlyCheck(rgTopLayout, rbTop1.getId());
                } else if (edge == 1) {                                                 // 下降沿
                    onlyCheck(rgTopLayout, rbTop2.getId());
                } else {                                                                // 双边沿
                    onlyCheck(rgTopLayout, rbTop3.getId());
                }
                msgLevel.setTopCount(3);                                                // 顶部3个选项
                msgLevel.setTopSelect(edge);                                            // 设置选中索引
                break;
            case TopLayoutTrigger.DETAIL_PULSEWIDTH:                                    // 脉宽触发
                rgTopLayout.setVisibility(VISIBLE);                                     // 显示顶部选项
                rbTop1.setVisibility(VISIBLE);                                          // 显示子选项1
                rbTop2.setVisibility(GONE);                                             // 隐藏子选项2
                rbTop3.setVisibility(VISIBLE);                                          // 显示子选项3
                llShowTopLayout.setVisibility(VISIBLE);                                 // 显示图标容器
                ivShowTop1.setVisibility(VISIBLE);                                      // 显示图标1
                ivShowTop2.setVisibility(GONE);                                         // 隐藏图标2
                ivShowTop3.setVisibility(VISIBLE);                                      // 显示图标3
                ivShowTop1.setImageResource(R.drawable.bg_right_level_positive);        // 正脉冲图标
                ivShowTop3.setImageResource(R.drawable.bg_right_level_negative);        // 负脉冲图标

                int polar = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_POLAR); // 读取脉宽极性
                if (polar == 0) {                                                       // 正极性
                    onlyCheck(rgTopLayout, rbTop1.getId());
                } else {                                                                // 负极性
                    onlyCheck(rgTopLayout, rbTop3.getId());
                }
                msgLevel.setTopCount(2);                                                // 顶部2个选项
                msgLevel.setTopSelect(polar);                                           // 设置选中索引
                break;
            case TopLayoutTrigger.DETAIL_NEDGE:                                         // N边沿触发
                rgTopLayout.setVisibility(VISIBLE);                                     // 显示顶部选项
                rbTop1.setVisibility(VISIBLE);                                          // 显示子选项1
                rbTop2.setVisibility(GONE);                                             // 隐藏子选项2
                rbTop3.setVisibility(VISIBLE);                                          // 显示子选项3
                llShowTopLayout.setVisibility(VISIBLE);                                 // 显示图标容器
                ivShowTop1.setVisibility(VISIBLE);                                      // 显示图标1
                ivShowTop2.setVisibility(GONE);                                         // 隐藏图标2
                ivShowTop3.setVisibility(VISIBLE);                                      // 显示图标3
                ivShowTop1.setImageResource(R.drawable.bg_right_level_rising);          // 上升沿图标
                ivShowTop3.setImageResource(R.drawable.bg_right_level_falling);         // 下降沿图标

                int slope = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_NEDGE_SLOPE); // 读取N边沿斜率
                if (slope == 0) {                                                       // 上升沿
                    onlyCheck(rgTopLayout, rbTop1.getId());
                } else {                                                                // 下降沿
                    onlyCheck(rgTopLayout, rbTop3.getId());
                }
                msgLevel.setTopCount(2);                                                // 顶部2个选项
                msgLevel.setTopSelect(slope);                                           // 设置选中索引
                break;
            case TopLayoutTrigger.DETAIL_RUNT:                                          // Runt触发
                rgTopLayout.setVisibility(VISIBLE);                                     // 显示顶部选项
                rbTop1.setVisibility(VISIBLE);                                          // 显示子选项1
                rbTop2.setVisibility(VISIBLE);                                          // 显示子选项2
                rbTop3.setVisibility(VISIBLE);                                          // 显示子选项3
                llShowTopLayout.setVisibility(VISIBLE);                                 // 显示图标容器
                ivShowTop1.setVisibility(VISIBLE);                                      // 显示图标1
                ivShowTop2.setVisibility(VISIBLE);                                      // 显示图标2
                ivShowTop3.setVisibility(VISIBLE);                                      // 显示图标3
                ivShowTop1.setImageResource(R.drawable.bg_right_level_positive);        // 正极性图标
                ivShowTop2.setImageResource(R.drawable.bg_right_level_negative);        // 负极性图标
                ivShowTop3.setImageResource(R.drawable.bg_right_level_either);          // 任意极性图标

                polar = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_RUNT_POLAR);  // 读取Runt极性
                if (polar == 0) {                                                       // 正极性
                    onlyCheck(rgTopLayout, rbTop1.getId());
                } else if (polar == 1) {                                                // 负极性
                    onlyCheck(rgTopLayout, rbTop2.getId());
                } else {                                                                // 任意极性
                    onlyCheck(rgTopLayout, rbTop3.getId());
                }
                msgLevel.setTopCount(3);                                                // 顶部3个选项
                msgLevel.setTopSelect(polar);                                           // 设置选中索引
                break;
            case TopLayoutTrigger.DETAIL_SLOPE:                                         // Slope触发
                rgTopLayout.setVisibility(VISIBLE);                                     // 显示顶部选项
                rbTop1.setVisibility(VISIBLE);                                          // 显示子选项1
                rbTop2.setVisibility(VISIBLE);                                          // 显示子选项2
                rbTop3.setVisibility(VISIBLE);                                          // 显示子选项3
                llShowTopLayout.setVisibility(VISIBLE);                                 // 显示图标容器
                ivShowTop1.setVisibility(VISIBLE);                                      // 显示图标1
                ivShowTop2.setVisibility(VISIBLE);                                      // 显示图标2
                ivShowTop3.setVisibility(VISIBLE);                                      // 显示图标3
                ivShowTop1.setImageResource(R.drawable.bg_right_level_rising);          // 上升沿图标
                ivShowTop2.setImageResource(R.drawable.bg_right_level_falling);         // 下降沿图标
                ivShowTop3.setImageResource(R.drawable.bg_right_level_double);          // 双边沿图标

                edge = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SLOPE_EDGE);   // 读取Slope边沿
                if (edge == 0) {                                                        // 上升沿
                    onlyCheck(rgTopLayout, rbTop1.getId());
                } else if (edge == 1) {                                                 // 下降沿
                    onlyCheck(rgTopLayout, rbTop2.getId());
                } else {                                                                // 双边沿
                    onlyCheck(rgTopLayout, rbTop3.getId());
                }
                msgLevel.setTopCount(3);                                                // 顶部3个选项
                msgLevel.setTopSelect(edge);                                            // 设置选中索引
                break;
            case TopLayoutTrigger.DETAIL_TIMEOUT:                                       // Timeout触发
                rgTopLayout.setVisibility(VISIBLE);                                     // 显示顶部选项
                rbTop1.setVisibility(VISIBLE);                                          // 显示子选项1
                rbTop2.setVisibility(VISIBLE);                                          // 显示子选项2
                rbTop3.setVisibility(VISIBLE);                                          // 显示子选项3
                llShowTopLayout.setVisibility(VISIBLE);                                 // 显示图标容器
                ivShowTop1.setVisibility(VISIBLE);                                      // 显示图标1
                ivShowTop2.setVisibility(VISIBLE);                                      // 显示图标2
                ivShowTop3.setVisibility(VISIBLE);                                      // 显示图标3
                ivShowTop1.setImageResource(R.drawable.bg_right_level_positive);        // 正极性图标
                ivShowTop2.setImageResource(R.drawable.bg_right_level_negative);        // 负极性图标
                ivShowTop3.setImageResource(R.drawable.bg_right_level_either);          // 任意极性图标

                polar = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_TIMEOUT_POLAR); // 读取Timeout极性
                if (polar == 0) {                                                       // 正极性
                    onlyCheck(rgTopLayout, rbTop1.getId());
                } else if (polar == 1) {                                                // 负极性
                    onlyCheck(rgTopLayout, rbTop2.getId());
                } else {                                                                // 任意极性
                    onlyCheck(rgTopLayout, rbTop3.getId());
                }
                msgLevel.setTopCount(3);                                                // 顶部3个选项
                msgLevel.setTopSelect(polar);                                           // 设置选中索引
                break;
            case TopLayoutTrigger.DETAIL_VIDEO:                                         // Video触发
                rgTopLayout.setVisibility(VISIBLE);                                     // 显示顶部选项
                rbTop1.setVisibility(VISIBLE);                                          // 显示子选项1
                rbTop2.setVisibility(GONE);                                             // 隐藏子选项2
                rbTop3.setVisibility(VISIBLE);                                          // 显示子选项3
                llShowTopLayout.setVisibility(VISIBLE);                                 // 显示图标容器
                ivShowTop1.setVisibility(VISIBLE);                                      // 显示图标1
                ivShowTop2.setVisibility(GONE);                                         // 隐藏图标2
                ivShowTop3.setVisibility(VISIBLE);                                      // 显示图标3
                ivShowTop1.setImageResource(R.drawable.bg_right_level_positive);        // 正极性图标
                ivShowTop3.setImageResource(R.drawable.bg_right_level_negative);        // 负极性图标

                polar = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_POLAR); // 读取Video极性
                if (polar == 0) {                                                       // 正极性
                    onlyCheck(rgTopLayout, rbTop1.getId());
                } else {                                                                // 负极性
                    onlyCheck(rgTopLayout, rbTop3.getId());
                }
                msgLevel.setTopCount(2);                                                // 顶部2个选项
                msgLevel.setTopSelect(polar);                                           // 设置选中索引
                break;
            case TopLayoutTrigger.DETAIL_LOGIC:                                         // Logic触发
            case TopLayoutTrigger.DETAIL_S1:                                            // 序列触发S1
            case TopLayoutTrigger.DETAIL_S2:                                            // 序列触发S2
            case TopLayoutTrigger.DETAIL_S3:                                            // 序列触发S3
            case TopLayoutTrigger.DETAIL_S4:                                            // 序列触发S4
                msgLevel.setTopCount(0);                                                // 无顶部选项
                rgTopLayout.setVisibility(GONE);                                        // 隐藏顶部选项
                llShowTopLayout.setVisibility(GONE);                                    // 隐藏图标容器
                break;
        }
        rgBottomLayout.setChildBackGround(drawableTop, drawableMiddle, drawableBottom); // 重新设置底部按钮行背景
    }

//    private int getChTextResId(int channel) {
//        if (channel == IWave.Ch1) {
//            return R.string.mainCenterRadioButtonCh1;
//        } else if (channel == IWave.Ch2) {
//            return R.string.mainCenterRadioButtonCh2;
//        } else if (channel == IWave.Ch3) {
//            return R.string.mainCenterRadioButtonCh3;
//        } else if (channel == IWave.Ch4) {
//            return R.string.mainCenterRadioButtonCh4;
//        } else {
//            return R.string.mainCenterRadioButtonCh1;
//        }
//    }

    /**
     * 仅选中指定RadioButton，不触发监听器
     * <p>先移除监听器，再选中，再恢复监听器，避免程序化选中触发回调</p>
     * @param radioGroup 目标RadioGroup
     * @param radioButtonId 要选中的RadioButton ID
     */
    private void onlyCheck(RadioGroup radioGroup, int radioButtonId) {
        radioGroup.setOnCheckedChangeListener(null);                                    // 临时移除监听器
        radioGroup.check(radioButtonId);                                                // 选中指定按钮
        radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);                // 恢复监听器
    }

    /**
     * 获取底部当前选中项的索引
     * @return 0-8对应CH1-CH8+外部触发，-1表示无选中
     */
    private int getBottomCheckIndex() {
        if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom1.getId()) {            // CH1选中
            return 0;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom2.getId()) {     // CH2选中
            return 1;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom3.getId()) {     // CH3选中
            return 2;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom4.getId()) {     // CH4选中
            return 3;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom5.getId()) {     // CH5选中
            return 4;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom6.getId()) {     // CH6选中
            return 5;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom7.getId()) {     // CH7选中
            return 6;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom8.getId()) {     // CH8选中
            return 7;
        } else if (rgBottomLayout.getCheckedRadioButtonId() == rbBottom9.getId()) {     // 外部触发选中
            return 8;
        } else {                                                                        // 无选中
            return -1;
        }
    }

    /**
     * 获取中间当前选中项的索引
     * @return 0=自动模式, 1=普通模式
     */
    private int getMiddleCheckIndex() {
        if (rgMiddleLayout.getCheckedRadioButtonId() == rbAuto.getId()) {               // 自动模式
            return 0;
        } else {                                                                        // 普通模式
            return 1;
        }
    }


    /**
     * 重写setVisibility，面板可见时启动自动隐藏定时器
     * @param visibility 可见性状态
     */
    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.VISIBLE) {                                               // 设为可见时
            timer.start();                                                              // 启动自动隐藏计时
        }
        super.setVisibility(visibility);                                                // 调用父类设置可见性
    }

    /**
     * 通过RxBus发送触发电平消息
     * @param isFromEventBus true=来自内部事件, false=来自用户操作
     */
    private void sendMsg(boolean isFromEventBus) {
        msgLevel.setFromEventBus(isFromEventBus);                                       // 设置消息来源
        RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_LEVEL, msgLevel);                   // 通过RxBus发送消息
    }

    /**
     * 缓存加载事件消费者
     * <p>应用启动或缓存重载时，读取缓存并更新UI</p>
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();                                                                 // 从缓存加载参数
        }
    };

    /**
     * 顶部触发类型切换事件消费者
     * <p>用户在顶部菜单切换触发类型时，更新面板UI</p>
     */
    private Consumer<TopMsgTrigger> consumerTopTrigger = new Consumer<TopMsgTrigger>() {
        @Override
        public void accept(@NonNull TopMsgTrigger topMsgTrigger) throws Exception {
            setTriggerIndex(topMsgTrigger.getTriggerTitle().getIndex());                // 按新触发类型更新UI
        }
    };

    /**
     * 顶部触发通道切换事件消费者
     * <p>触发源通道变更时，更新面板并重新发送消息</p>
     */
    private Consumer<TopMsgTriggerChannel> consumerTopTriggerChannel = new Consumer<TopMsgTriggerChannel>() {
        @Override
        public void accept(TopMsgTriggerChannel topMsgTriggerChannel) throws Exception {
            setTriggerIndex(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER));        // 重新加载触发类型UI
            sendMsg(topMsgTriggerChannel.isFromEventBus());                             // 发送消息通知
        }
    };

    /**
     * 自动模式事件消费者
     * <p>外部请求切换自动模式时，将中间选项切换为Auto</p>
     */
    private Consumer<Boolean> consumerMainLeftMenuAuto = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            if (aBoolean) {                                                             // 请求自动模式
                if (getMiddleCheckIndex() != 0) {                                       // 当前不是自动模式
                    onlyCheck(rgMiddleLayout, rbAuto.getId());                          // 切换为自动模式
                    msgLevel.setMiddleSelect(0);                                        // 更新消息
                    sendMsg(true);                                                      // 发送消息（来源=事件总线）
                }
            }
        }
    };

    /**
     * 触发电平通道切换事件消费者
     * <p>Logic触发模式下，通过触发电平旋钮切换通道时同步更新底部选中</p>
     */
    private Consumer<MainMsgTriggerLevel> consumerTriggerLevel = new Consumer<MainMsgTriggerLevel>() {
        @Override
        public void accept(MainMsgTriggerLevel msgTriggerLevel) throws Exception {
            if (msgTriggerLevel.getCurLevel().equals(MainHolderTriggerLevel.LEVEL_TRIGGER_LOGIC)) { // Logic触发模式
                if (msgTriggerLevel.getCurCh() == getBottomCheckIndex()) {              // 当前通道已选中，无需切换
                    return;
                }
                String strCh1 = getResources().getString(R.string.mainCenterRadioButtonCh1);   // CH1名称
                String strCh2 = getResources().getString(R.string.mainCenterRadioButtonCh2);   // CH2名称
                String strCh3 = getResources().getString(R.string.mainCenterRadioButtonCh3);   // CH3名称
                String strCh4 = getResources().getString(R.string.mainCenterRadioButtonCh4);   // CH4名称
                String strCh5 = getResources().getString(R.string.mainCenterRadioButtonCh5);   // CH5名称
                String strCh6 = getResources().getString(R.string.mainCenterRadioButtonCh6);   // CH6名称
                String strCh7 = getResources().getString(R.string.mainCenterRadioButtonCh7);   // CH7名称
                String strCh8 = getResources().getString(R.string.mainCenterRadioButtonCh8);   // CH8名称
                List<String> strChx = Arrays.asList(
                        strCh1, strCh2, strCh3, strCh4, strCh5, strCh6, strCh7, strCh8
                );                                                                      // 通道名称列表
                for (int i = 0; i < strChx.size(); i++) {                               // 遍历通道名称
                    if (msgTriggerLevel.getCurCh() == i + 1) {                          // 匹配当前通道
                        if (strChx.get(i).equals(rbBottoms.get(i).getText().toString())) { // 按钮文本匹配
                            onlyCheck(rgBottomLayout, rbBottoms.get(i).getId());         // 选中对应按钮
                        }
                    }
                }
                msgLevel.setBottomSelect(msgTriggerLevel.getCurCh() - 1);              // 更新消息（通道索引从0开始）
            }
        }
    };

    /**
     * RadioGroup选中项变更监听器
     * <p>统一处理顶部/中间/底部三级选项的选中变更，播放按键音效，
     * 更新消息对象，并通知外部监听器和发送RxBus消息</p>
     */
    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            PlaySound.getInstance().playButton();                                       // 播放按键音效
            timer.start();                                                              // 重置自动隐藏计时
            if (group.getId() == rgTopLayout.getId()) {                                 // 顶部选项变更
                if (checkedId == rbTop1.getId()) {                                      // 选中子选项1
                    msgLevel.setTopSelect(0);                                           // 更新消息索引
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbTop1);                   // 通知外部监听
                    }
                } else if (checkedId == rbTop2.getId()) {                               // 选中子选项2
                    msgLevel.setTopSelect(1);                                           // 更新消息索引
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbTop2);                   // 通知外部监听
                    }
                } else if (checkedId == rbTop3.getId()) {                               // 选中子选项3
                    msgLevel.setTopSelect(2);                                           // 更新消息索引
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbTop3);                   // 通知外部监听
                    }
                }
            } else if (group.getId() == rgMiddleLayout.getId()) {                       // 中间选项变更
                if (checkedId == rbAuto.getId()) {                                      // 选中自动模式
                    msgLevel.setMiddleSelect(0);                                        // 更新消息索引
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbAuto);                   // 通知外部监听
                    }
                } else if (checkedId == rbNormal.getId()) {                             // 选中普通模式
                    msgLevel.setMiddleSelect(1);                                        // 更新消息索引
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbNormal);                 // 通知外部监听
                    }
                }
            } else if (group.getId() == rgBottomLayout.getId()) {                       // 底部选项变更
                if (checkedId == rbBottom1.getId()) {                                   // 选中CH1
                    msgLevel.setBottomSelect(getChIndex(rbBottom1));                    // 设置通道索引
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom1);                // 通知外部监听
                    }
                } else if (checkedId == rbBottom2.getId()) {                            // 选中CH2
                    msgLevel.setBottomSelect(getChIndex(rbBottom2));                    // 设置通道索引
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom2);                // 通知外部监听
                    }
                } else if (checkedId == rbBottom3.getId()) {                            // 选中CH3
                    msgLevel.setBottomSelect(getChIndex(rbBottom3));                    // 设置通道索引
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom3);                // 通知外部监听
                    }
                } else if (checkedId == rbBottom4.getId()) {                            // 选中CH4
                    msgLevel.setBottomSelect(getChIndex(rbBottom4));                    // 设置通道索引
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom4);                // 通知外部监听
                    }
                } else if (checkedId == rbBottom5.getId()) {                            // 选中CH5
                    msgLevel.setBottomSelect(getChIndex(rbBottom5));                    // 设置通道索引
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom5);                // 通知外部监听
                    }
                } else if (checkedId == rbBottom6.getId()) {                            // 选中CH6
                    msgLevel.setBottomSelect(getChIndex(rbBottom6));                    // 设置通道索引
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom6);                // 通知外部监听
                    }
                } else if (checkedId == rbBottom7.getId()) {                            // 选中CH7
                    msgLevel.setBottomSelect(getChIndex(rbBottom7));                    // 设置通道索引
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom7);                // 通知外部监听
                    }
                } else if (checkedId == rbBottom8.getId()) {                            // 选中CH8
                    msgLevel.setBottomSelect(getChIndex(rbBottom8));                    // 设置通道索引
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom8);                // 通知外部监听
                    }
                } else if (checkedId == rbBottom9.getId()) {                            // 选中外部触发
                    msgLevel.setBottomSelect(getChIndex(rbBottom9));                    // 设置通道索引
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onClick(group, rbBottom9);                // 通知外部监听
                    }
                }
            }
            sendMsg(false);                                                             // 发送消息（来源=用户操作）
        }
    };

    /**
     * 根据RadioButton文本获取通道索引
     * <p>通过比对按钮文本与通道名称列表来确定索引，外部触发返回8</p>
     * @param rb 目标RadioButton
     * @return 通道索引（0-7=CH1-CH8, 8=外部触发）
     */
    private int getChIndex(RadioButton rb) {
        List<String> strChX = Arrays.asList(
                getResources().getString(R.string.mainCenterRadioButtonCh1), getResources().getString(R.string.mainCenterRadioButtonCh2),
                getResources().getString(R.string.mainCenterRadioButtonCh3), getResources().getString(R.string.mainCenterRadioButtonCh4),
                getResources().getString(R.string.mainCenterRadioButtonCh5), getResources().getString(R.string.mainCenterRadioButtonCh6),
                getResources().getString(R.string.mainCenterRadioButtonCh7), getResources().getString(R.string.mainCenterRadioButtonCh8)
        );                                                                              // 通道名称列表
        int chIndex = 8;                                                                // 默认外部触发索引
        for (int i = 0; i < strChX.size(); i++) {                                       // 遍历通道名称
            if (strChX.get(i).equals(rb.getText().toString())) {                        // 文本匹配
                chIndex = i;                                                            // 更新为匹配的通道索引
            }
        }
        return chIndex;                                                                 // 返回通道索引
    }

    /**
     * 触发模式变更事件UI观察者
     * <p>当FPGA/底层通过EventFactory通知触发模式变更时，
     * 同步更新中间选项（Auto/Normal）并发送消息</p>
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon(); // 获取当前触发通用参数
            if (((EventBase) data).getId() == EventFactory.EVENT_TRIGGER_COMMON_MODE) { // 触发模式变更事件
                boolean auto = rgMiddleLayout.getCheckedRadioButtonId() == rbAuto.getId(); // 当前是否为自动模式
                if (auto != (triggerCommon.getTriggerMode() == TriggerCommon.TM_AUTO)) { // 与底层数据不一致
                    if (triggerCommon.getTriggerMode() == TriggerCommon.TM_AUTO) {       // 底层为自动模式
                        onlyCheck(rgMiddleLayout, rbAuto.getId());                       // 选中自动
                        msgLevel.setMiddleSelect(0);                                     // 更新消息
                    } else {                                                             // 底层为普通模式
                        onlyCheck(rgMiddleLayout, rbNormal.getId());                     // 选中普通
                        msgLevel.setMiddleSelect(1);                                     // 更新消息
                    }
                    sendMsg(true);                                                       // 发送消息（来源=事件总线）
                }
            }
        }
    };

    /**
     * 外部按键模式切换消费者
     * <p>外部硬按键触发模式切换时，在Auto/Normal之间切换</p>
     */
    private Consumer<Object> consumerModeChange = new Consumer<Object>() {
        @Override
        public void accept(Object object) throws Exception {
            boolean auto = rgMiddleLayout.getCheckedRadioButtonId() == rbAuto.getId();  // 当前是否为自动模式
            if (auto) {                                                                  // 当前自动→切普通
                onlyCheck(rgMiddleLayout, rbNormal.getId());                             // 选中普通
                msgLevel.setMiddleSelect(1);                                             // 更新消息
            } else {                                                                     // 当前普通→切自动
                onlyCheck(rgMiddleLayout, rbAuto.getId());                               // 选中自动
                msgLevel.setMiddleSelect(0);                                             // 更新消息
            }
            sendMsg(false);                                                              // 发送消息（来源=用户操作）
        }
    };


}
