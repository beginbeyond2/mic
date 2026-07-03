package com.micsig.tbook.tbookscope.rightslipmenu;

import android.annotation.SuppressLint;                                               // 注解：抑制lint警告
import android.content.Context;                                                       // 上下文环境
import android.content.res.TypedArray;                                                // XML自定义属性数组
import android.os.Handler;                                                            // 消息处理器
import android.os.Message;                                                            // 消息对象
import android.text.TextUtils;                                                        // 文本工具
import android.util.AttributeSet;                                                     // XML属性集
import android.util.Log;                                                              // 日志工具
import android.view.MotionEvent;                                                      // 触摸事件
import android.view.View;                                                             // 视图基类
import android.view.ViewConfiguration;                                                // 视图配置（长按超时等）
import android.view.ViewGroup;                                                        // 视图组
import android.widget.Button;                                                         // 按钮
import android.widget.ImageView;                                                      // 图片视图
import android.widget.LinearLayout;                                                   // 线性布局
import android.widget.RadioButton;                                                    // 单选按钮
import android.widget.RadioGroup;                                                     // 单选按钮组
import android.widget.TextView;                                                       // 文本视图

import androidx.constraintlayout.widget.ConstraintLayout;                              // 约束布局
import androidx.recyclerview.widget.LinearLayoutManager;                              // RecyclerView线性布局管理器
import androidx.recyclerview.widget.RecyclerView;                                     // RecyclerView列表控件

import com.micsig.base.Logger;                                                        // 自定义日志
import com.micsig.tbook.scope.Data.LoadCsv;                                           // CSV数据加载器
import com.micsig.tbook.scope.Data.WaveData;                                          // 波形数据常量
import com.micsig.tbook.scope.Event.EventBase;                                        // 事件基类
import com.micsig.tbook.scope.Event.EventFactory;                                     // 事件工厂
import com.micsig.tbook.scope.Event.EventUIObserver;                                  // 事件UI观察者
import com.micsig.tbook.scope.ScopeBase;                                              // 示波器基础常量
import com.micsig.tbook.scope.channel.Channel;                                        // 通道基类
import com.micsig.tbook.scope.channel.ChannelFactory;                                 // 通道工厂
import com.micsig.tbook.scope.channel.RefChannel;                                     // 参考通道
import com.micsig.tbook.scope.horizontal.HorizontalAxis;                              // 水平轴
import com.micsig.tbook.scope.horizontal.HorizontalAxisRef;                           // 参考通道水平轴
import com.micsig.tbook.tbookscope.GlobalVar;                                         // 全局变量
import com.micsig.tbook.tbookscope.MainActivity;                                      // 主Activity
import com.micsig.tbook.tbookscope.MainMsgSlip;                                       // 滑出菜单消息
import com.micsig.tbook.tbookscope.MainViewGroup;                                     // 主视图组
import com.micsig.tbook.tbookscope.R;                                                 // 资源ID
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;                // 右侧其他通道消息
import com.micsig.tbook.tbookscope.middleware.command.Command;                        // 命令中间件
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;                 // 命令到UI的消息
import com.micsig.tbook.tbookscope.middleware.command.Command_Channel;                // 通道命令
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum;                              // 消息队列枚举
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogChannelLabel;           // 通道标签对话框
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogLoadRefCsvWave;         // CSV加载对话框
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogSelectColor;            // 颜色选择对话框
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.ForEightRefRecallAdapter;     // 8通道Ref回调列表适配器
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogRefRecallBean;          // Ref回调数据Bean
import com.micsig.tbook.tbookscope.rxjava.RxBus;                                     // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister;                             // RxBus注册器
import com.micsig.tbook.tbookscope.rxjava.RxEnum;                                    // RxJava事件枚举
import com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob.ExternalKeysNode; // 外部按键节点
import com.micsig.tbook.tbookscope.tools.PlaySound;                                   // 按键音效
import com.micsig.tbook.tbookscope.tools.SaveManage;                                  // 存储管理
import com.micsig.tbook.tbookscope.tools.ScreenControls;                              // 屏幕控制（锁屏/进度）
import com.micsig.tbook.tbookscope.tools.Tools;                                       // 通用工具
import com.micsig.tbook.tbookscope.top.layout.save.TopMsgSaveRef;                     // 保存Ref消息
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard; // 浮动键盘对话框
import com.micsig.tbook.tbookscope.util.App;                                          // 应用上下文工具
import com.micsig.tbook.tbookscope.util.CacheUtil;                                    // 缓存工具
import com.micsig.tbook.tbookscope.util.DToast;                                       // 自定义Toast
import com.micsig.tbook.tbookscope.util.FileSelector;                                 // 文件选择器
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;                     // 光标管理
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;                          // 波形管理
import com.micsig.tbook.ui.FixedSizeHashSet;                                          // 固定大小HashSet
import com.micsig.tbook.ui.MSwitchBox;                                                // 自定义开关控件
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;                             // 右侧选择项Bean
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;                             // 右侧选择视图
import com.micsig.tbook.ui.top.view.TopViewEdit;                                      // 顶部可编辑视图
import com.micsig.tbook.ui.top.view.TopViewSpinner;                                   // 顶部下拉选择器
import com.micsig.tbook.ui.util.FileBeanToStr;                                        // FileBean转字符串
import com.micsig.tbook.ui.util.StrUtil;                                              // 字符串工具
import com.micsig.tbook.ui.util.TBookUtil;                                            // 示波器工具类
import com.micsig.tbook.ui.util.svg.SelectorUtil;                                     // 选择器工具
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;                                      // SVG节点信息（颜色管理）
import com.micsig.tbook.ui.wavezone.TChan;                                            // 通道常量
import com.molihuan.pathselector.entity.FileBean;                                     // 文件数据Bean

import java.io.File;                                                                   // 文件操作
import java.lang.ref.WeakReference;                                                    // 弱引用
import java.text.SimpleDateFormat;                                                     // 日期格式化
import java.util.ArrayList;                                                            // 动态数组
import java.util.Arrays;                                                               // 数组工具
import java.util.Date;                                                                 // 日期类
import java.util.HashMap;                                                              // 哈希映射
import java.util.Iterator;                                                             // 迭代器
import java.util.List;                                                                 // 列表接口
import java.util.Map;                                                                  // 映射接口
import java.util.Optional;                                                             // 可选值
import java.util.concurrent.ConcurrentHashMap;                                        // 并发哈希映射
import java.util.concurrent.atomic.AtomicInteger;                                     // 原子整数

import io.reactivex.rxjava3.annotations.NonNull;                                      // 非空注解
import io.reactivex.rxjava3.functions.Consumer;                                       // RxJava消费者接口


/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                          RightLayoutRef                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：右侧滑出菜单 - 8通道参考通道(Ref)参数设置面板                       ║
 * ║ 核心职责：管理单个参考通道(R1-R8)的开关/加载/颜色/延迟/标签等参数，            ║
 * ║          支持WAV/CSV格式文件加载，通过RxBus与业务层通信                        ║
 * ║ 架构设计：继承LinearLayout的自定义视图，包含RecyclerView文件列表、             ║
 * ║          MSwitchBox开关、TopViewSpinner目录选择、浮动键盘输入延迟值等          ║
 * ║          支持普通模式(R1-R8独立)和Math/Ref/Serials共享模式两种布局            ║
 * ║ 数据流向：UI操作 → RightMsgRefForEight → RxBus → 订阅方                     ║
 * ║          外部事件 → RxBus/EventUIObserver → UI更新 → sendMsg                ║
 * ║ 依赖关系：RxBus, CacheUtil, Command, ChannelFactory, SaveManage,            ║
 * ║           LoadCsv, ScreenControls, ForEightRefRecallAdapter, TChan          ║
 * ║ 使用场景：用户在右侧菜单中管理参考通道参数、加载参考波形文件时显示此面板        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * @Description: 适配8通道Ref设置页面
 * @Author: lmh
 * @CreateDate: 2024/3/28 9:45
 * 8通道参考通道设置面板
 * <p>
 * 继承LinearLayout，管理单个参考通道(R1-R8)的所有参数：
 * - 开关：参考通道显示/隐藏
 * - 加载类型：WAV/CSV格式选择
 * - 文件浏览：选择参考波形文件
 * - Recall列表：已加载的参考文件列表
 * - 标签：通道标签编辑
 * - 颜色：通道颜色选择
 * - 延迟：相位延迟设置
 * - 垂直/水平位置：通过浮动键盘输入
 * </p>
 */
public class RightLayoutRef extends LinearLayout {

    /** 日志标签 */
    private static final String TAG = "RightLayoutRef";
    /** 上下文引用 */
    private Context context;
    /** 参考通道消息封装对象 */
    private RightMsgRefForEight msgRef;
    /** 根视图组 */
    private ViewGroup rootView;

    /** 文件浏览按钮 */
    private Button btnBrowse;
    /** 目录选择下拉框 */
    private TopViewSpinner spinner;

    /** Recall文件列表RecyclerView */
    private RecyclerView recyclerView;
    /** Recall列表适配器 */
    private ForEightRefRecallAdapter adapter;
    /** Recall文件数据列表 */
    private ArrayList<DialogRefRecallBean> list;
    /** 开关和标签的显示容器 */
    private LinearLayout llDisplaySwitch;
    /** 参考通道开关控件 */
    private MSwitchBox switchBox;
    /** 参考通道编号，值域TChan.R1--TChan.R8 */
    private int refChannelNumber;
    /** 通道标题区域（R1+删除按钮） */
    private ConstraintLayout topChannelTitle, clTopChannelGroup;
    /** 通道标题文本（如"R1"） */
    private TextView channelTitle;
    /** 删除通道/添加通道按钮 */
    private TextView btnDeleteChannel, btnAddChannel;
    /** Math/Ref/Serials通道类型切换按钮 */
    private RadioButton channelMath, channelRef, channelSerials;
    /** 加载类型选择控件（WAV/CSV等） */
    private RightViewSelect refLoadType;
    /** 是否需要更新refMaster位置标记 */
    private boolean needUpdateMasterLocation = false;

    /** 上下按钮和背景图片容器 */
    private ConstraintLayout clImgBtn;
    /** 上移/下移按钮 */
    private Button btnTop, btnBottom;
    /** 背景图片和上下滚动指示图 */
    private ImageView ivBackground, imgBottom, imgUp;
    /** 弱引用Handler，用于垂直档位按钮背景恢复 */
    private MyHandler myHandler;
    /** 文件是否加载成功标记 */
    private boolean isLoadSuccess = false;
    /** CSV加载确认对话框 */
    private DialogLoadRefCsvWave dialogLoadRefCsvWave;
    /** 通道标签/颜色选择/相位延迟/双击编辑控件 */
    private TopViewEdit chLabel, selectColor, phaseDelay, forDoubleClick;
    /** 通道标签编辑对话框 */
    private DialogChannelLabel dialogChannelLabel;
    /** 颜色选择对话框 */
    private DialogSelectColor dialogSelectColor;
    /** 浮动键盘对话框（用于输入延迟值等） */
    private TopDialogFloatKeyBoard dialogFloatKeyBoard;

    /** 最近使用的文件路径集合（固定大小10，用于Spinner下拉历史） */
    private final FixedSizeHashSet<FileBean> pathSet = new FixedSizeHashSet<>(10);

    /**
     * 单参数构造方法
     * @param context 上下文
     */
    public RightLayoutRef(Context context) {
        this(context, null);                                                            // 委托给两参数构造
    }

    /**
     * 两参数构造方法
     * @param context 上下文
     * @param attrs XML属性集
     */
    public RightLayoutRef(Context context, AttributeSet attrs) {
        this(context, attrs, 0);                                                        // 委托给三参数构造
    }

    /**
     * 三参数构造方法（最终初始化入口）
     * @param context 上下文
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    public RightLayoutRef(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);                                            // 调用父类构造
        this.context = context;                                                         // 保存上下文引用
        initView(attrs, defStyleAttr);                                                  // 初始化视图控件
        initControl();                                                                  // 初始化事件订阅
        myHandler = new MyHandler(RightLayoutRef.this);                                 // 创建弱引用Handler
    }

    /**
     * 初始化事件订阅
     * <p>订阅RxBus事件和EventFactory事件，涵盖缓存加载、保存Ref、通道显示、
     * 垂直档位、Recall列表变更、通道删除、U盘响应、时基更新、鼠标点击等</p>
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);             // 订阅缓存加载
        RxBus.getInstance().getObservable(RxEnum.TOPSLIP_SAVE_REF).subscribe(consumerSaveRef);             // 订阅保存Ref
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers);     // 订阅右侧其他通道
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);            // 订阅命令到UI
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SHOW_NORMAL_STATE).subscribe(consumerShowNormalState); // 订阅显示正常状态
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_REF).subscribe(consumerOtherChannelCanAdd); // 订阅通道可用性
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_VERTICAL_SCALE).subscribe(consumerChannelVscale); // 订阅垂直档位
        RxBus.getInstance().getObservable(RxEnum.DIALOG_REFRECALL_CHANGED).subscribe(consumerRefRecallChanged); // 订阅Recall变更
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_UPDATE_REF_DATA_LIST).subscribe(consumerUpdateRefDataList); // 订阅数据列表更新
        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE, this::OnChanActiveChange);    // 订阅通道激活变更
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_DELETE_OTHER_CHANNEL).subscribe(consumerDeleteChannel); // 订阅删除通道
        RxBus.getInstance().getObservable(RxEnum.UDISK_RESPONSE).subscribe(consumerUDiskResponse);         // 订阅U盘响应
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_UPDATE_TIMEBASE).subscribe(consumerUpdateTimeBase); // 订阅时基更新
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_USER_SET_TIMEBASE).subscribe(consumerRefTimeBase); // 订阅用户设置时基
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_MOUSE_CLICK_POSITION).subscribe(consumerMouseClick); // 订阅鼠标点击

        EventFactory.addEventObserver(EventFactory.EVENT_LOADCSV_RUN, eventLoadCsvObserver);              // 注册CSV加载进度观察者
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor); // 订阅颜色选择

//        recyclerView.addOnScrollListener(scrollListener);
    }

    /**
     * 通道激活变更回调
     * @param obj 消息对象
     */
    private void OnChanActiveChange(Object obj) {
        MQEnum mqEnum = RxBusRegister.parseMqEnum(obj);                                // 解析消息队列枚举
        if (mqEnum == MQEnum.CH_OPEN || mqEnum == MQEnum.CH_CLOSE) {                   // 通道打开或关闭

        }
    }

    /**
     * 初始化视图控件
     * <p>加载布局、绑定控件引用、设置监听器、初始化RecyclerView和适配器</p>
     * @param attrs XML属性集
     * @param defStyleAttr 默认样式属性
     */
    @SuppressLint("SetTextI18n")
    private void initView(AttributeSet attrs, int defStyleAttr) {
        rootView = (ViewGroup) View.inflate(context, R.layout.layout_right_ref_for_eight, this); // 填充布局
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RightLayoutRef); // 获取自定义属性
        int refNumber = ta.getInt(R.styleable.RightLayoutRef_refChannelNumber, 1);      // 读取通道编号属性
        ta.recycle();                                                                   // 回收TypedArray
        refChannelNumber = TChan.toRefTChan(refNumber);                                 // 转换为TChan参考通道编号
        topChannelTitle = findViewById(R.id.top_channel_title);                         // 通道标题区域
        btnDeleteChannel = findViewById(R.id.btn_delete_channel);                       // 删除通道按钮
        channelTitle = findViewById(R.id.channel_title);                                // 通道标题文本
        llDisplaySwitch = findViewById(R.id.ll_display_switch);                         // 开关显示容器
        switchBox = findViewById(R.id.channel_switch);                                  // 通道开关
        clImgBtn = findViewById(R.id.cl_img_btn);                                       // 上下按钮容器
        btnTop = findViewById(R.id.btnTop);                                             // 上移按钮
        btnBottom = findViewById(R.id.btnBottom);                                       // 下移按钮
        ivBackground = findViewById(R.id.img_back_src);                                 // 背景图片
        refLoadType = (RightViewSelect) findViewById(R.id.ref_load_type);               // 加载类型选择
        imgBottom = findViewById(R.id.img_bottom);                                      // 下滚指示图
        imgUp = findViewById(R.id.img_up);                                              // 上滚指示图
        chLabel = findViewById(R.id.chLabel);                                           // 通道标签编辑
        selectColor = findViewById(R.id.select_color);                                  // 颜色选择
        phaseDelay = findViewById(R.id.phase_delay);                                    // 相位延迟
        forDoubleClick = findViewById(R.id.for_doubleClick);                            // 双击编辑控件
        imgBottom.setOnTouchListener(onTouchListener);                                  // 下滚触摸监听
        imgUp.setOnTouchListener(onTouchListener);                                      // 上滚触摸监听
        btnBrowse = findViewById(R.id.btn_browse);                                      // 文件浏览按钮
        btnBrowse.setOnClickListener(onClickListener);                                  // 浏览按钮点击
        spinner = findViewById(R.id.spinner);                                           // 目录选择下拉
        spinner.setData(context.getResources().getString(R.string.top_save_wave_directory), getPreviousDirectory(),R.layout.layout_item_for_ref_directory, onItemSelectListener); // 初始化Spinner数据

        clTopChannelGroup = findViewById(R.id.cl_top_channel_group);                    // Math/Ref/Serials区域
        channelMath = findViewById(R.id.channelMath);                                   // Math按钮
        channelRef = findViewById(R.id.channelRef);                                     // Ref按钮
        channelSerials = findViewById(R.id.channelSerials);                             // Serials按钮
        btnAddChannel = findViewById(R.id.btn_add_channel);                             // 添加通道按钮
        channelMath.setOnClickListener(onClickListener);                                // Math按钮点击
        channelRef.setOnClickListener(onClickListener);                                 // Ref按钮点击
        channelSerials.setOnClickListener(onClickListener);                             // Serials按钮点击
        btnAddChannel.setOnClickListener(onClickListener);                              // 添加按钮点击
        btnTop.setOnClickListener(onClickListener);                                     // 上移按钮点击
        btnBottom.setOnClickListener(onClickListener);                                  // 下移按钮点击
        refLoadType.setOnItemClickListener(onRightViewSelectItemClickListener);          // 加载类型选择监听

        recyclerView = findViewById(R.id.rcv_recall_data);                              // Recall列表RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(context));                // 设置线性布局管理器
        list = getList(spinner.getSelectItem());                                        // 获取文件列表
        adapter = new ForEightRefRecallAdapter(context, list, refChannelNumber);        // 创建适配器
        adapter.setOnItemClickListener(onItemClickListener);                            // 设置列表项点击监听
        recyclerView.setAdapter(adapter);                                               // 设置适配器
        channelTitle.setText("R" + TChan.toRefNumber(refChannelNumber));                // 设置通道标题
        btnDeleteChannel.setOnClickListener(onClickListener);                            // 删除按钮点击
        switchBox.setOnToggleStateChangedListener(onToggleStateChangedListener);        // 开关状态变更监听
        chLabel.setOnClickEditListener(onClickEditListener);                            // 标签编辑监听
        phaseDelay.setOnClickEditListener(onClickEditListener);                         // 延迟编辑监听
        forDoubleClick.setOnClickEditListener(onClickEditListener);                     // 双击编辑监听
        selectColor.setOnClickEditListener(onClickEditListener);                        // 颜色编辑监听

        dialogLoadRefCsvWave = (DialogLoadRefCsvWave) ((MainActivity) context).findViewById(R.id.dialogLoadRefCsv); // CSV加载对话框

        initData();                                                                     // 初始化数据
        setControlColorByChIdx(refChannelNumber);                                       // 设置控件颜色
        showUpBottomImg();                                                              // 显示/隐藏滚动指示图
    }

    /**
     * 根据RecyclerView处理topImg/bottomImg显示隐藏逻辑
     * <p>根据列表是否超出可视区域及滚动位置决定上下箭头是否显示</p>
     */
    private void showUpBottomImg() {
        int firstItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition(); // 第一个完全可见项
        int lastItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition(); // 最后一个完全可见项
        if (list.size() < 9) {                                                          // 列表不足9项，无需滚动
            imgUp.setVisibility(View.INVISIBLE);                                        // 隐藏上箭头
            imgBottom.setVisibility(View.INVISIBLE);                                    // 隐藏下箭头
        } else {
            if (firstItemPosition > 0) {                                                // 第一项未完全可见
                imgUp.setVisibility(View.VISIBLE);                                      // 显示上箭头
            } else {
                imgUp.setVisibility(View.INVISIBLE);                                    // 隐藏上箭头
            }

            if (lastItemPosition < (adapter.getItemCount() - 1)) {                      // 最后一项未完全可见
                imgBottom.setVisibility(View.VISIBLE);                                  // 显示下箭头
            } else {
                imgBottom.setVisibility(View.INVISIBLE);                                // 隐藏下箭头
            }
        }
    }

    /**
     * 初始化消息对象
     */
    private void initData() {
        msgRef = new RightMsgRefForEight();                                             // 创建消息对象
        msgRef.setRefChannelNumber(refChannelNumber);                                   // 设置通道编号
        msgRef.setRefChecked(false);                                                    // 默认关闭
        msgRef.setLabel("");                                                            // 默认空标签
    }

//    @Override
//    protected void onVisibilityChanged(@android.support.annotation.NonNull View changedView, int visibility) {
//        super.onVisibilityChanged(changedView, visibility);
//        if(visibility == VISIBLE) {
//            fillSelectData();
//        } else {
//            clearSelect();
//        }
//    }

    /**
     * 通过RxBus发送参考通道消息
     */
    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_REF, msgRef);                       // 发送Ref消息
    }


    /**
     * 缓存加载事件消费者
     * <p>应用启动时从缓存恢复参数并标记加载完成</p>
     */
    private Consumer consumerLoadCache = new Consumer() {
        @Override
        public void accept(@NonNull Object o) throws Exception {
            setCache();                                                                 // 从缓存恢复参数
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutRef, true);      // 标记Ref面板缓存已加载
        }
    };

    /**
     * 从缓存加载参考通道参数并恢复UI状态
     * <p>读取缓存中的开关状态、标签、加载类型、文件路径等，发送命令到FPGA</p>
     */
    private void setCache() {
        //获取参考通道cache
        boolean isAddByUser = isAddByUser(refChannelNumber);                            // 是否由用户添加
        boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber); // 读取开关状态
        String label = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_LABEL + refChannelNumber); // 读取标签
        int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber); // 读取加载类型
        Command.get().getChannel().Display(TChan.toFpgaChNo(refChannelNumber), refCheck && isAddByUser, false); // 发送显示命令
        switchBox.setState(refCheck);                                                   // 设置开关状态
//        String delay = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_DELAY + refChannelNumber);
        resetDelayUI();                                                                 // 重置延迟UI
        restorePath();                                                                  // 恢复路径列表
        //TODO


//        if (SaveRecoverySession.MSS_REF_TAG.equals(refFilePath)) { //这里用一个乱码匹配，
//            rEditFilePath.setText("");
//        } else {
//            rEditFilePath.setText(refFilePath);
//        }
        chLabel.setText(label);                                                         // 设置标签文本
        String selectFile = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber); // 读取当前选中文件
        if (isAddByUser) {                                                              // 仅用户添加的通道才加载文件
            if (!StrUtil.isEmpty(selectFile)) {                                         // 有选中文件
                if (selectFile.endsWith(".csv")) {                                      // CSV格式
                    ScreenControls.getInstance().lockScreen(ScreenControls.LOCK_LOADCSV << (refChannelNumber - TChan.R1)); // 锁屏
                    new Thread(loadCsRunnable).start();                                 // 启动CSV加载线程
                    //loadCsRunnable.run();
                } else if (selectFile.endsWith(".wav") || selectFile.endsWith(".mwav")) {// WAV格式
                    isLoadSuccess = SaveManage.getInstance().readRef(TChan.toFpgaChNo(refChannelNumber), selectFile); // 加载WAV
                }
            }
        }
        Command.get().getReference().Enable(TChan.toFpgaChNo(refChannelNumber), refCheck, false); // 发送Ref使能命令
        //获取要显示的Ref通道
        msgRef.setRefChecked(refCheck);                                                 // 更新消息
        msgRef.setLabel(label);                                                         // 更新消息
        updateSelect();                                                                 // 更新列表选中状态
        refLoadType.setSelectIndex(refType);                                            // 设置加载类型

        WaveManage.get().setChannelLabel(refChannelNumber, label);                      // 设置波形标签
        setChannelLabel(TChan.toFpgaChNo(refChannelNumber), label);                     // 设置通道标签

        String colorStr = CacheUtil.get().getString(CacheUtil.MAIN_CHANNEL_COLOR + refChannelNumber); // 读取颜色
        selectColor.setEditColor(colorStr);                                             // 设置颜色显示

        sendMsg();                                                                      // 发送消息
    }


    /**
     * 当存储参考时，如果参考没有打开，则先关闭所有参考
     * <p>确保保存Ref前，当前通道处于关闭状态，避免数据冲突</p>
     */
    //TODO 当存储参考时，如果参考没有打开，则先关闭所有参考
    private void closeAllRef_thenSaveRef() {
        if (!CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber)) { // 当前通道未开启
            if (switchBox.isState()) {                                                  // 开关处于开启状态
                switchBox.setState(false);                                              // 关闭开关
                onToggleStateChangedListener.onToggleStateChanged(switchBox, switchBox.isState()); // 触发状态变更
            }
//            if (r2Switch.isState()) {
//                r2Switch.setState(false);
//                onToggleStateChangedListener.onToggleStateChanged(r2Switch, false);
//            }
//            if (r3Switch.isState()) {
//                r3Switch.setState(false);
//                onToggleStateChangedListener.onToggleStateChanged(r3Switch, false);
//            }
//            if (r4Switch.isState()) {
//                r4Switch.setState(false);
//                onToggleStateChangedListener.onToggleStateChanged(r4Switch, false);
//            }
        }
    }

    /**
     * 保存参考通道事件消费者
     * <p>当从顶部菜单保存参考波形到当前Ref通道时，执行保存、刷新列表、开启通道</p>
     */
    private Consumer<TopMsgSaveRef> consumerSaveRef = new Consumer<TopMsgSaveRef>() {
        @SuppressLint("SetTextI18n")
        @Override
        public void accept(TopMsgSaveRef msgSaveRef) throws Exception {
            int ch = msgSaveRef.getFromIdChannelFactory();                              // 源通道编号
            if (msgSaveRef.getSaveToRefId() == TChan.toRefNumber(refChannelNumber)) {   // 目标为当前通道
                String refPath = SaveManage.getInstance().saveRef(ch, Tools.SaveType_LOCAL, TChan.toFpgaChNo(refChannelNumber), null); // 保存Ref
                int refIndex = TChan.toRefNumber(refChannelNumber);                     // 通道索引
                if (StrUtil.isEmpty(refPath)) {                                         // 保存失败
                    DToast.get().show(String.format(App.get().getString(R.string.msgTopSaveBinFailed), "REF" + TChan.toRefNumber(refChannelNumber))); // 提示失败
                    if (switchBox.isState()) {                                          // 当前已开启
                        switchBox.setState(false);                                      // 关闭开关
                        msgRef.setRefChecked(false);                                    // 更新消息
                        onToggleStateChangedListener.onToggleStateChanged(switchBox, false); // 触发状态变更
                    }
                } else {                                                                // 保存成功
                    ChannelFactory.chActivate(ch);                                      // 激活源通道
                    refLoadType.setSelectIndex(0);//WAV格式                             // 切换为WAV格式
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber, String.valueOf(refLoadType.getSelectIndex())); // 缓存类型
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber, refPath); // 缓存文件路径

                    String defaultStr = Tools.resultSavePath(Tools.SaveType_LOCAL, Tools.SaveDir_REFDEFAULT, (MainActivity) context); // 默认保存路径
                    FileBean fileBean = new FileBean();
                    fileBean.setPath(defaultStr);
                    fileBean.setDisplayName(defaultStr); //TODO

                    addPathToPathSet(fileBean);                                         // 添加到路径历史
                    spinner.updateDataList(getPreviousDirectory(), null);               // 更新Spinner
                    updateList();                                                       // 更新文件列表
                    DToast.get().show(String.format(App.get().getString(R.string.msgTopSaveBinSuccess), "REF" + TChan.toRefNumber(refChannelNumber))); // 提示成功
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + refChannelNumber, ""); // 清除关闭保存记录
                    closeAllRef_thenSaveRef();                                          // 先关闭再保存
                    switchBox.setState(true);                                           // 开启开关
                    msgRef.setRefChecked(true);                                         // 更新消息
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChannelNumber, String.valueOf(true)); // 标记用户添加
                    needUpdateMasterLocation = true;                                    // 标记需要更新位置
                    onToggleStateChangedListener.onToggleStateChanged(switchBox, true); // 触发状态变更
                    RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber)); // 获取参考通道
                    if (refChannel != null) {
                        setLabel(refChannel.getLabel().trim());                         // 设置标签
                        ChannelFactory.chActivate(TChan.toFpgaChNo(refChannelNumber)); // 激活参考通道
                    }
                }
            }
        }
    };

    /**
     * 设置通道标签文本
     * <p>同时更新UI、消息对象、缓存和波形管理器</p>
     * @param label 标签文本
     */
    private void setLabel(String label) {
        chLabel.setText(label);                                                         // 更新UI
        msgRef.setLabel(label);                                                         // 更新消息
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + refChannelNumber, label); // 更新缓存
        WaveManage.get().setChannelLabel(refChannelNumber, label);                      // 更新波形标签
        setChannelLabel(TChan.toFpgaChNo(refChannelNumber), label);                     // 更新通道标签
    }

    /**
     * 右侧其他通道消息消费者
     * <p>处理其他通道面板发来的Ref开关变更，同步当前面板状态</p>
     */
    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers msgOthers) throws Exception {
            boolean value = msgOthers.getRef(refChannelNumber).isValue();               // 获取目标开关值
            boolean isAddByUser = isAddByUser(refChannelNumber);                        // 是否用户添加
            Command.get().getChannel().Display(TChan.toFpgaChNo(refChannelNumber), value && isAddByUser, false); // 发送显示命令
            if (value == switchBox.isState()) {                                         // 状态一致，无需变更
                return;
            }
            if (!CacheUtil.get().isLoadParamComplete()) {                               // 参数未加载完成
                return;
            }
            int refIndex = TChan.toRefNumber(refChannelNumber);                         // 通道索引
            if (isAddByUser) {                                                          // 用户添加的通道
                switchBox.setState(value);                                              // 设置开关状态
                Command.get().getReference().Enable(TChan.toFpgaChNo(refChannelNumber), value, false); // 发送使能命令
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber, String.valueOf(value)); // 更新缓存
                ChannelFactory.chEnable(TChan.toFpgaChNo(refChannelNumber), value);     // 通道使能
                msgRef.setRefChecked(value);                                            // 更新消息
            }
        }
    };

    /**
     * 命令到UI消息消费者
     * <p>处理来自FPGA/底层的通道显示、Ref使能、存储加载、标签变更等命令</p>
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_CHANNEL_DISPLAY: {                             // 通道显示命令
                    List<Command_Channel.ChannelAttribute> list = (List<Command_Channel.ChannelAttribute>) commandMsgToUI.getObject(); // 获取通道属性列表
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                           // 通道索引
                    boolean isOpen = Boolean.parseBoolean(params[1]);                    // 开关状态
                    if (chIndex != TChan.toFpgaChNo(refChannelNumber)) return;           // 非当前通道
                    if (TChan.isRef(TChan.toUiChNo(chIndex)) /*&& isOpen==false*/) {    // 是Ref通道
                        switchBox.setState(isOpen);                                     // 设置开关状态
                        onToggleStateChangedListener.onToggleStateChanged(switchBox, switchBox.isState()); // 触发状态变更
                    }
                }
                break;
                case CommandMsgToUI.FLAG_REF_ENABLE: {                                  // Ref使能命令
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int refSource = Integer.parseInt(params[0]);                         // Ref源通道
                    boolean display = Boolean.parseBoolean(params[1]);                   // 显示状态
                    if (refSource == TChan.toFpgaChNo(refChannelNumber)) {              // 是当前Ref通道
                        switchBox.setState(display);                                    // 设置开关状态
                        onToggleStateChangedListener.onToggleStateChanged(switchBox, switchBox.isState()); // 触发状态变更
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_STOTAGE_LOAD: {                                // 存储加载命令
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int refSource = Integer.parseInt(params[0]);                         // Ref源通道
                    String fileName = params[1];                                         // 文件名
                    boolean display = Boolean.parseBoolean(params[2]);                   // 显示状态
                    if (refChannelNumber == refSource) {                                 // 是当前通道
                        loadRef(refSource, fileName, display);                          // 加载Ref
                    }
                }
                break;
                case CommandMsgToUI.FLAG_REF_LABEL: {                                   // Ref标签命令
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                           // 通道索引
                    String result = params[1];                                           // 标签文本
                    if (chIndex != TChan.toFpgaChNo(refChannelNumber)) return;           // 非当前通道
                    setLabel(result);                                                    // 设置标签
                }
                break;
                case CommandMsgToUI.FLAG_REF_LABEL_CLEAR: {                             // Ref标签清除命令
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT); // 解析参数
                    int chIndex = Integer.parseInt(params[0]);                           // 通道索引
                    String result = "";                                                  // 空标签
//                    Logger.i(Command.TAG, "label clear");
                    if (chIndex != TChan.toFpgaChNo(refChannelNumber)) return;           // 非当前通道
                    setLabel(result);                                                    // 清除标签
                }
                break;
            }
        }
    };

    /**
     * 显示正常/非正常状态消费者
     * <p>根据是否为普通R1-R8模式切换不同的布局样式：
     * - 普通模式：显示开关、标题、上下按钮
     * - 共享模式(Math/Ref/Serials)：隐藏开关，显示类型切换按钮</p>
     */
    private Consumer<String> consumerShowNormalState = new Consumer<String>() {
        @Override
        public void accept(String String) throws Exception {
            String[] params = String.split(CommandMsgToUI.PARAM_SPLIT);                 // 解析参数
            int channelNumber = Integer.parseInt(params[0]);                             // 通道编号
            boolean isNormal = Boolean.parseBoolean(params[1]);                          // 是否普通模式
            RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber)); // 获取参考通道

            if (channelNumber != refChannelNumber) return;                              // 非当前通道
            if (isNormal) {//R1...R8布局样式
                llDisplaySwitch.setVisibility(View.VISIBLE);//switch 显示
                topChannelTitle.setVisibility(View.VISIBLE);//R1 delete按钮 显示
                clTopChannelGroup.setVisibility(View.GONE);//Math/Ref/Serial 不显示
                clImgBtn.setVisibility(View.VISIBLE);                                   // 显示上下按钮
                if (refChannel != null && refChannel.getRefType() == WaveData.FFT_WAVE) { // FFT波形
                    phaseDelay.setVisibility(View.GONE);                                 // 隐藏延迟
                } else {
                    phaseDelay.setVisibility(View.VISIBLE);                              // 显示延迟
                }
            } else {//Math/Ref/Serials共同显示布局样式
                llDisplaySwitch.setVisibility(View.INVISIBLE);//switch 不显示
                topChannelTitle.setVisibility(View.INVISIBLE);//R1 delete按钮 不显示
                clTopChannelGroup.setVisibility(View.VISIBLE);//Math/Ref/Serial 显示
                clImgBtn.setVisibility(View.INVISIBLE);                                  // 隐藏上下按钮
                phaseDelay.setVisibility(View.GONE);                                     // 隐藏延迟
            }

            setRefLoadTypeEnable();                                                     // 设置加载类型可用性
            int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber); // 读取加载类型
            String label = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_LABEL + refChannelNumber); // 读取标签
            refLoadType.setSelectIndex(refType);                                        // 设置加载类型
            chLabel.setText(label);                                                     // 设置标签
            updateList();                                                               // 更新文件列表
            setControlColorByChIdx(refChannelNumber);                                   // 设置控件颜色
        }
    };

    /**
     * 其他通道可添加性消费者
     * <p>根据通道类型可用性设置Math/Ref/Serials按钮的启用状态</p>
     */
    private Consumer<String> consumerOtherChannelCanAdd = new Consumer<String>() {
        @Override
        public void accept(String available) throws Throwable {
            if (channelMath == null || channelRef == null || channelSerials == null) return; // 控件未初始化
            String[] params = available.split(CommandMsgToUI.PARAM_SPLIT);              // 解析参数
            int refSlideIndex = Integer.parseInt(params[0]);                             // 滑出菜单索引
            boolean mathAvailable = Boolean.parseBoolean(params[1]);                    // Math是否可用
            boolean refAvailable = Boolean.parseBoolean(params[2]);                     // Ref是否可用
            boolean serialAvailable = Boolean.parseBoolean(params[3]);                  // Serial是否可用
            int refChanNumber = TChan.R1;                                               // 默认R1
            switch (refSlideIndex) {
                case MainViewGroup.RIGHTSLIP_REF1:
                    refChanNumber = TChan.R1;                                           // R1
                    break;
                case MainViewGroup.RIGHTSLIP_REF2:
                    refChanNumber = TChan.R2;                                           // R2
                    break;
                case MainViewGroup.RIGHTSLIP_REF3:
                    refChanNumber = TChan.R3;                                           // R3
                    break;
                case MainViewGroup.RIGHTSLIP_REF4:
                    refChanNumber = TChan.R4;                                           // R4
                    break;
                case MainViewGroup.RIGHTSLIP_REF5:
                    refChanNumber = TChan.R5;                                           // R5
                    break;
                case MainViewGroup.RIGHTSLIP_REF6:
                    refChanNumber = TChan.R6;                                           // R6
                    break;
                case MainViewGroup.RIGHTSLIP_REF7:
                    refChanNumber = TChan.R7;                                           // R7
                    break;
                case MainViewGroup.RIGHTSLIP_REF8:
                    refChanNumber = TChan.R8;                                           // R8
                    break;
            }
            if (refChanNumber == refChannelNumber) {                                    // 是当前通道
                channelMath.setEnabled(mathAvailable);                                  // 设置Math可用性
                channelSerials.setEnabled(serialAvailable);                              // 设置Serial可用性
            } else {                                                                    // 非当前通道
                channelMath.setEnabled(true);                                           // Math可用
                channelSerials.setEnabled(true);                                        // Serial可用
            }
            boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM); // 是否缩放模式
            boolean slowTimeBase = Tools.isSlowTimeBase();                              // 是否慢时基
            if (zoom && slowTimeBase) {                                                 // 缩放+慢时基
                channelSerials.setEnabled(false);                                       // 禁用Serial
            }
        }
    };

    /**
     * 通道垂直档位变更消费者
     * <p>当Ref通道垂直档位调节时，更新背景图片方向并发送消息</p>
     */
    private Consumer<String> consumerChannelVscale = new Consumer<String>() {

        @Override
        public void accept(String adjustStr) throws Throwable {
            String[] param = adjustStr.split(CommandMsgToUI.PARAM_SPLIT);               // 解析参数
            boolean isClickTop = Boolean.parseBoolean(param[0]);                         // 是否向上调节
            int chan = Integer.parseInt(param[1]);                                       // 通道编号
            if (chan != refChannelNumber) return;                                       // 非当前通道
            if (isClickTop) {                                                            // 向上调节
                ivBackground.setImageResource(R.drawable.svg_right_chx_button_u_88x174); // 上箭头背景
                msgRef.setUpClick(true);                                                // 设置向上标记
                postChange();                                                           // 发送变更消息
            } else {                                                                    // 向下调节
                ivBackground.setImageResource(R.drawable.svg_right_chx_button_d_88x174); // 下箭头背景
                msgRef.setUpClick(false);                                               // 设置向下标记
                postChange();                                                           // 发送变更消息
            }
        }
    };

    /**
     * Ref回调列表变更消费者
     * <p>处理外部旋钮的Recall完成/上移/下移操作</p>
     */
    private Consumer<String> consumerRefRecallChanged = new Consumer<String>() {
        @Override
        public void accept(String recallInfo) throws Exception {
            String[] params = recallInfo.split(CommandMsgToUI.PARAM_SPLIT);             // 解析参数
            int integer = Integer.parseInt(params[0]);                                   // 操作类型
            int channelNumber = Integer.parseInt(params[1]);                             // 通道编号
            if (channelNumber != refChannelNumber) return;                              // 非当前通道
            switch (integer) {
                case ExternalKeysNode.ACTION_REFRECALL_FINISH:                          // 确认选择
                    DialogRefRecallBean bean = null;
                    for (DialogRefRecallBean item : list) {                              // 遍历列表
                        if (item.isSelect()) {                                           // 找到选中项
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber, item.getPathFile()); // 缓存文件路径
                            confirm();                                                  // 确认加载
                            break;
                        }
                    }
                    break;
                case ExternalKeysNode.ACTION_REFRECALL_UP:                              // 上移
                    moveOnlyScroll(true);                                               // 向上滚动
                    break;
                case ExternalKeysNode.ACTION_REFRECALL_DOWN:                            // 下移
                    moveOnlyScroll(false);                                              // 向下滚动
                    break;
            }
        }
    };

    /**
     * 更新Ref数据列表消费者
     * <p>当Ref通道的文件列表需要刷新时调用</p>
     */
    private Consumer<Integer> consumerUpdateRefDataList = new Consumer<Integer>() {
        @Override
        public void accept(Integer refChan) throws Throwable {
            if (refChan == refChannelNumber) {                                          // 是当前通道
                updateList();                                                           // 更新文件列表
            }
        }
    };


    /**
     * 更新Recall文件列表
     * <p>重新读取当前目录的文件列表，根据缓存设置选中项，刷新适配器</p>
     */
    private void updateList() {
        list.clear();                                                                   // 清空列表
        list = getList(spinner.getSelectItem());                                        // 重新获取文件列表
        int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber); // 读取加载类型
        String selectFile = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber); // 读取选中文件
        for (DialogRefRecallBean bean : list) {                                         // 遍历设置选中状态
            boolean isSelect = bean.getPathFile().equals(selectFile);                   // 比较文件路径
            bean.setSelect(isSelect);                                                   // 设置选中
        }
        adapter.setList(list);                                                          // 更新适配器数据
        adapter.notifyDataSetChanged();                                                  // 刷新适配器

    }


    /**
     * 删除通道消费者
     * <p>当外部请求删除当前Ref通道时，模拟点击删除按钮</p>
     */
    private Consumer<Integer> consumerDeleteChannel = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Throwable {
            if (integer == refChannelNumber) {                                          // 是当前通道
                btnDeleteChannel.performClick();                                        // 模拟点击删除按钮
            }
        }
    };

    /**
     * U盘响应消费者
     * <p>U盘插入/拔出时，通知刷新参考数据列表</p>
     */
    private Consumer<Boolean> consumerUDiskResponse = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            //通知刷新参考数据
            RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_REF_DATA_LIST, refChannelNumber); // 发送更新列表消息
        }
    };

    /**
     * 仅滚动Recall列表（不确认加载）
     * <p>支持外部旋钮上下滚动文件列表</p>
     * @param isUp true=向上滚动, false=向下滚动
     */
    public void moveOnlyScroll(boolean isUp) {
        int index = 0;                                                                  // 当前选中索引
        for (int i = 0; i < list.size(); i++) {                                         // 遍历查找当前选中项
            DialogRefRecallBean bean = list.get(i);
            if (bean.isSelect()) {                                                      // 找到选中项
                index = bean.getIndex();                                                // 记录索引
                break;
            }
        }
/*        if (isUp) {
            if (index != 0) {
                index -= 1;
            } else {
                index = list.size() - 1;
            }
        } else {
            if (index != list.size() - 1) {
                index += 1;
            } else {
                index = 0;
            }
        }*/

        if (isUp) {                                                                     // 向上
            index -= 1;                                                                 // 索引减1
            index = Math.max(index, 0);                                                 // 不小于0
        } else {                                                                        // 向下
            index += 1;                                                                 // 索引加1
            index = Math.min(index, list.size() - 1);                                   // 不超过最大值
        }

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager(); // 获取布局管理器
        int firstCompletely = layoutManager.findFirstCompletelyVisibleItemPosition();   // 第一个完全可见项
        int lastCompletely = layoutManager.findLastCompletelyVisibleItemPosition();      // 最后一个完全可见项
        if (firstCompletely > index) {                                                  // 目标在可见范围之上
            layoutManager.scrollToPositionWithOffset(index, 0);                         // 滚动到目标位置
        } else if (lastCompletely < index) {                                            // 目标在可见范围之下
            layoutManager.scrollToPositionWithOffset(index - 9, -30);                   // 滚动到目标位置（偏移）
        }
        for (int i = 0; i < list.size(); i++) {                                         // 遍历更新选中状态
            DialogRefRecallBean bean = list.get(i);
            bean.setSelect(bean.getIndex() == index);                                   // 设置选中
            if (bean.isSelect()) {                                                      // 选中项
                scrollToPos(bean.getIndex());                                           // 滚动到中间位置
            }
        }
        adapter.notifyDataSetChanged();                                                  // 刷新适配器
        confirm();                                                                      // 确认加载
        showUpBottomImg();                                                              // 更新滚动指示图
    }

    /**
     * 滚动到指定位置（使选中项显示在列表中间）
     * @param pos 目标位置索引
     */
    private void scrollToPos(int pos) {
        if (list.size() > 9 && pos > 4 && pos < list.size() - 5) {                     // 列表足够长且位置不在首尾
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager(); // 获取布局管理器
            linearLayoutManager.scrollToPositionWithOffset(pos, 225);                   // 滚动到指定偏移
        }
    }

    /**
     * 从文件加载参考波形
     * <p>在指定目录中查找匹配的WAV文件并加载</p>
     * @param refSource 参考通道源
     * @param fileName 文件名
     * @param display 是否显示
     */
    private void loadRef(int refSource, String fileName, boolean display) {
        File[] files = SaveManage.getInstance().getFliesFromCurRef(Tools.SaveDir_REFWAVE); // 获取Ref文件列表
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {     // API 24+
            Optional<File> any = Arrays.stream(files).filter(s -> s.getName().replace(".mwav", "").replace(".wav", "").equals(fileName.trim())).findAny(); // 查找匹配文件
            if (any.isPresent() == false) return;                                       // 文件不存在
            File file = any.get();                                                      // 获取文件

            long time = file.lastModified();                                             // 最后修改时间
            String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time)); // 格式化时间
            String title = file.getName().replace(".mwav", "").replace(".wav", "");     // 文件标题
            loafRefX(refSource, file.getAbsolutePath(), title, ctime, display);         // 加载Ref
        }
    }


    /**
     * 执行Ref文件加载
     * <p>读取WAV文件到FPGA，更新缓存和消息</p>
     * @param refSource 参考通道源
     * @param absolutePath 文件绝对路径
     * @param title 标题
     * @param ctime 创建时间
     * @param display 是否显示
     */
    private void loafRefX(int refSource, String absolutePath, String title, String ctime, boolean display) {
        isLoadSuccess = SaveManage.getInstance().readRef(TChan.toFpgaChNo(refSource), absolutePath); // 读取Ref文件
        if (isLoadSuccess) {                                                            // 加载成功
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + refSource, String.valueOf(display)); // 缓存开关状态
            double scaleVal = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refSource)).getRefTimeScaleVal(); // 获取时基缩放值
            CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + refSource, getStringRefScale(TChan.toFpgaChNo(refSource), scaleVal)); // 缓存时基缩放

            msgRef.setRefChecked(display);                                              // 更新消息
            sendMsg();                                                                  // 发送消息
            SaveManage.getInstance().readRef(TChan.toFpgaChNo(refSource), absolutePath); // 重新读取（确保FPGA已加载）
        }
    }

    /**
     * 开关状态变更监听器
     * <p>处理参考通道开关切换，控制FPGA显示/隐藏，加载/卸载参考波形</p>
     */
    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            PlaySound.getInstance().playButton();                                       // 播放按键音效
            switch (view.getId()) {
                case R.id.channel_switch:                                               // 通道开关
                    if (!switchBox.isState()) {                                          // 关闭状态
                        ((MainActivity) context).getMainViewGroup().hideAllDialogSlip(); // 隐藏所有对话框
                    }
                    if (state) {                                                         // 开启
                        isLoadSuccess = SaveManage.getInstance().readRef(TChan.toFpgaChNo(refChannelNumber), adapter.getSelectItem().getPathFile()); // 加载Ref文件
                        RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber)); // 获取参考通道
                        if (refChannel != null) {
                            setLabel(refChannel.getLabel());                            // 设置标签
                        }
                        if (isLoadSuccess) {                                             // 加载成功
                            resetDelayUI();                                             // 重置延迟UI
                        }
                    }
                    msgRef.setRefChecked(state);                                         // 更新消息
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber, String.valueOf(state)); // 更新缓存
                    ChannelFactory.chEnable(TChan.toFpgaChNo(refChannelNumber), switchBox.isState()); // 通道使能
                    Command.get().getChannel().Display(TChan.toFpgaChNo(refChannelNumber), state, false); // 发送显示命令
                    Command.get().getReference().Enable(TChan.toFpgaChNo(refChannelNumber), state, false); // 发送使能命令
                    sendMsg();                                                          // 发送消息
                    if (!state) {                                                        // 关闭
                        //关闭垂直调节按钮
                        RxBus.getInstance().post(RxEnum.MQ_MSG_FORCE_HIDE_VERTICAL_SCALE, true); // 发送隐藏垂直调节按钮消息
                    }
                    break;
            }
            boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber); // 读取当前开关状态
            int refIndex = TChan.toRefNumber(refChannelNumber);                         // 通道索引
            if (refCheck) {                                                              // 已开启
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + refChannelNumber, String.valueOf(refIndex)); // 缓存关闭前索引
            } else {                                                                    // 已关闭
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CLOSE_SAVE + refChannelNumber, ""); // 清除缓存
            }
        }
    };

    /**
     * 获取参考通道时基缩放字符串
     * @param refIndex 参考通道FPGA编号
     * @param scale 缩放值
     * @return 格式化的缩放字符串（如"1.00ms"或"1.00kHz"）
     */
    public static String getStringRefScale(int refIndex, double scale) {
        RefChannel refChannel = ChannelFactory.getRefChannel(refIndex);                 // 获取参考通道
        if (refChannel == null) return "";                                               // 空通道
        HorizontalAxisRef horizontalAxisRef = refChannel.getHorizontalAxisRef();        // 获取水平轴
        String tail = "s";                                                               // 默认时间单位
        if (horizontalAxisRef.getRefType() == HorizontalAxisRef.REFTYPE_MATHFFT) {      // FFT参考
            tail = "Hz";                                                                 // 频率单位
        }
        String s = TBookUtil.getMFromDouble(scale);                                     // 格式化缩放值
        if (TextUtils.isEmpty(s)) return "";                                             // 空值
        return s + tail;                                                                 // 拼接单位
    }


    /**
     * Recall列表项点击监听器
     * <p>选中文件后，WAV格式直接确认加载，CSV格式弹出通道选择对话框</p>
     */
    private ForEightRefRecallAdapter.OnItemClickListener onItemClickListener = new ForEightRefRecallAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(DialogRefRecallBean item) {
//            DToast.get().show("选中的Index= " + item.getIndex());
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber, item.getPathFile()); // 缓存选中文件

            PlaySound.getInstance().playButton();                                       // 播放按键音效
            for (DialogRefRecallBean bean : list) {                                     // 遍历列表
                if (getSelectedIndex() == 0) {                                          // WAV格式
                    bean.setSelect(bean.getIndex() == item.getIndex());                 // 设置选中
                    if (bean.isSelect()) {
                        confirm();                                                      // 确认加载
                    }
                }
            }
//            adapter.setList(list);
            adapter.notifyDataSetChanged();                                              // 刷新适配器

            if (getSelectedIndex() == 1) {//CSV
                loadCsvFile(item);                                                      // CSV加载
            }
        }
    };

    /**
     * 加载CSV文件
     * <p>解析CSV文件，弹出通道选择对话框，用户确认后启动后台加载</p>
     * @param item 选中的文件Bean
     */
    private void loadCsvFile(DialogRefRecallBean item) {
        LoadCsv loadCsv = new LoadCsv();                                                // 创建CSV加载器
        boolean loadSuccess = false;
        try {
            loadSuccess = loadCsv.load(item.getPathFile());                             // 加载CSV文件
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!loadSuccess) {
                DToast.get().show(context.getResources().getString(R.string.file_load_fail)); // 加载失败提示
            }
        }
        Log.d(TAG, "chNums:" + loadCsv.getChNums() + ",b:" + loadSuccess);
        if (!loadSuccess) return;                                                       // 加载失败
        channelInCsv.clear();                                                           // 清空CSV通道列表
        channelInCsv = loadCsv.getCsvInfos();                                           // 获取CSV中的通道信息
        for (int c : channelInCsv) {
            Log.d(TAG, "ch" + c);
        }
        dialogLoadRefCsvWave.setData(channelInCsv, new DialogLoadRefCsvWave.OnDismissListener() { // 弹出通道选择对话框
            @Override
            public void onDismiss(ConcurrentHashMap<Integer, Integer> channelToRef) {  // 用户确认
                if (channelToRef.size() <= 0) return;                                   // 无映射
                loadCsvInBackGround(channelToRef, loadCsv, item);                       // 后台加载
            }
        });
    }


    /**
     * 后台加载CSV到参考通道
     * <p>在新线程中执行CSV数据加载到FPGA，加载完成后更新缓存和UI</p>
     * @param channelToRef 通道到Ref的映射
     * @param loadCsv CSV加载器
     * @param item 选中的文件Bean
     */
    public void loadCsvInBackGround(ConcurrentHashMap<Integer, Integer> channelToRef, LoadCsv loadCsv, DialogRefRecallBean item) {
        new Thread(() -> {                                                              // 新线程
            Logger.d(TAG, "channelToRef= " + channelToRef.toString());
            for (Map.Entry<Integer, Integer> entry : channelToRef.entrySet()) {          // 遍历映射
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_FROM + TChan.toUiChNo(entry.getKey()),
                        item.getPathFile() + ";" + TChan.toUiChNo(entry.getValue()));    // 缓存来源信息
            }
            loadCsv.setLoadCsvProgress(val -> updateLoadProgress(val));                 // 设置进度回调
            loadCsv.loadToRef(channelToRef);                                            // 加载CSV到Ref
            while (!loadCsv.isFinish()) {                                               // 等待加载完成
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            post(() -> {                                                                // 切回UI线程
                channelToRef.forEach((key, value) -> {
                    ChannelFactory.chOpen(key);                                         // 打开通道
                    setCacheMapValue(key, item);                                        // 更新缓存
                });
            });
        }).start();
    }

    /**
     * 设置缓存映射值
     * <p>CSV加载完成后，更新Ref通道的所有缓存值</p>
     * @param key FPGA通道编号
     * @param item 选中的文件Bean
     */
    private void setCacheMapValue(int key, DialogRefRecallBean item) {
        if (item == null) return;                                                       // 空项
        int chanId = TChan.toUiChNo(key);                                               // 转换为UI通道编号
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chanId, String.valueOf(true)); // 标记用户添加
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + chanId, String.valueOf(true)); // 标记开启
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_TYPE + chanId, String.valueOf(refLoadType.getSelectIndex())); // 缓存加载类型
        double scaleVal = ChannelFactory.getRefChannel(TChan.toFpgaChNo(chanId)).getRefTimeScaleVal(); // 获取时基缩放
        CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + chanId, getStringRefScale(TChan.toFpgaChNo(chanId), scaleVal)); // 缓存时基缩放
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + chanId, item.getPathFile()); // 缓存选中文件
//        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_DELAY + chanId, "0 ns");

        ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();                // 隐藏所有对话框
        String label = getLabelFromChannel(key);                                        // 获取通道标签
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + chanId, label);          // 缓存标签
        resetDelayUI();                                                                 // 重置延迟UI
        WaveManage.get().setChannelLabel(chanId, label);                                // 设置波形标签
        RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_REF_STATE, chanId);               // 发送Ref状态更新消息
    }

    /**
     * 确认选择并加载参考波形
     * <p>当Recall列表中选中WAV文件后，执行加载操作</p>
     */
    public void confirm() {
//        for (DialogRefRecallBean item : list) {
//            if (item.isSelect()) {
////                rEditFilePath.setText(item.getPathFile());
//                break;
//            }
//        }
        //如果当前是 R1……R8单独的状态，需要更新当前通道的数据
        if (llDisplaySwitch.getVisibility() == View.VISIBLE) {                          // 普通R1-R8模式
            RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber)); // 获取参考通道
            if (refChannel == null) return;                                              // 空通道
            msgRef.setRefChecked(true);                                                 // 设置开启
            msgRef.setLabel(chLabel.getText());                                         // 设置标签
            //保存新增加的RefChannel 信息
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber, String.valueOf(true)); // 缓存开关
            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + refChannelNumber, chLabel.getText()); // 缓存标签
//            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_DELAY + refChannelNumber, "0 ns");
            double scaleVal = refChannel.getRefTimeScaleVal();                           // 获取时基缩放
            CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + refChannelNumber, getStringRefScale(TChan.toFpgaChNo(refChannelNumber), scaleVal)); // 缓存时基缩放
            String selectFile = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber); // 读取选中文件
            if (getSelectedIndex() == 0) {                                              // WAV格式
                isLoadSuccess = SaveManage.getInstance().readRef(TChan.toFpgaChNo(refChannelNumber), selectFile); // 加载WAV
                setLabel(refChannel.getLabel());                                        // 设置标签
                if (isLoadSuccess) {                                                     // 加载成功
                    resetDelayUI();                                                     // 重置延迟UI
                }
            }
        }
    }


    /**
     * 获取当前加载类型选中索引
     * @return 0=WAV, 1=CSV
     */
    public int getSelectedIndex() {
        return refLoadType.getSelectIndex();
    }


    /**
     * 按后缀名过滤文件列表
     * @param oldList 原始文件列表
     * @param suffix 后缀名数组
     * @return 过滤后的文件列表
     */
    public ArrayList<File> filterFileList(ArrayList<File> oldList, String... suffix) {
        String[] suffixS = suffix;
        ArrayList<File> files = new ArrayList<File>();
        if (oldList == null || oldList.size() <= 0) return files;                       // 空列表
        for (int i = 0; i < oldList.size(); i++) {                                     // 遍历文件
            if (suffixS != null && suffixS.length > 0) {                               // 有后缀过滤
                for (String s : suffixS) {
                    if (oldList.get(i).getAbsolutePath().endsWith(s)) {                  // 后缀匹配
                        files.add(oldList.get(i));
                        break;
                    }
                }
            }
        }
        return files;
    }


    /**
     * 从指定路径获取Recall文件列表
     * <p>根据加载类型(WAV/CSV)过滤文件，按修改时间倒序排列</p>
     * @param path 目录路径
     * @return Recall数据Bean列表
     */
    private ArrayList<DialogRefRecallBean> getList(String path) {
        ArrayList<DialogRefRecallBean> list = new ArrayList<DialogRefRecallBean>();
        if (path == null || path.isEmpty()) return list;                                // 空路径
        int refTypeIndex = getSelectedIndex();                                          // 获取加载类型
        ArrayList<File> files = SaveManage.getInstance().getFilesFromCur(path);         // 获取目录文件
        List<File> filterFiles = new ArrayList<File>();
        if (refTypeIndex == 1) { //CSV
            filterFiles = filterFileList(files ,".csv");                                // 过滤CSV文件
        } else if (refTypeIndex == 0) {//WAV
            filterFiles = filterFileList(files, ".wav", ".mwav");                       // 过滤WAV文件
        } else {
            filterFiles = filterFileList(files);                                        // 不过滤
        }

        if (filterFiles == null || filterFiles.size() == 0) return list;                // 无文件
        String selectFile = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber); // 当前选中文件
        for (int i = 0; i < filterFiles.size(); i++) {                                  // 遍历文件
            long time = filterFiles.get(i).lastModified();                               // 修改时间
            String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time)); // 格式化时间
            DialogRefRecallBean item = new DialogRefRecallBean();
            item.setIndex(i);                                                           // 设置索引
            item.setLastModifyTime(time);                                               // 设置修改时间
            item.setPathFile(filterFiles.get(i).getAbsolutePath());                     // 设置文件路径
            if (filterFiles.get(i).getAbsolutePath().equals(selectFile)) {              // 匹配选中文件
                item.setSelect(true);
            } else {
                item.setSelect(false);
            }
            item.setTime(ctime);                                                        // 设置时间文本
            item.setTitle(filterFiles.get(i).getName());                                // 设置标题
            list.add(item);
        }
        list.sort((o1, o2) -> {                                                         // 按修改时间倒序
            long i = o2.getLastModifyTime() - o1.getLastModifyTime();
            if (i == 0) {
                return o2.getTitle().compareTo(o1.getTitle());                          // 时间相同按标题倒序
            } else {
                return Long.compare(o2.getLastModifyTime(), o1.getLastModifyTime());
            }
        });
        for (int i = 0; i < list.size(); i++) {                                         // 重新设置索引
            DialogRefRecallBean item = list.get(i);
            item.setIndex(i);
        }
        return list;
    }

    /**
     * 清除所有选中状态
     */
    public void clearSelect() { //清除选中
        if (list.isEmpty()) return;                                                     // 空列表
        for (DialogRefRecallBean bean : list) {                                         // 遍历列表
            if (bean.isSelect()) bean.setSelect(false);                                 // 清除选中
            adapter.notifyItemChanged(list.indexOf(bean));                              // 刷新单项
        }
    }

    /**
     * 更新选中状态
     * <p>根据缓存中的选中文件路径，设置列表项的选中状态并滚动到选中位置</p>
     */
    private void updateSelect() {
        if (list.isEmpty()) return;                                                     // 空列表
        int index = -1;
        String selectFile = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber); // 读取选中文件
        for (DialogRefRecallBean bean : list) {                                         // 遍历列表
            boolean isSelect = bean.getPathFile().equals(selectFile);                   // 比较路径
            bean.setSelect(isSelect);                                                   // 设置选中
            if (isSelect) {
                index = bean.getIndex();                                                // 记录选中索引
            }
            adapter.notifyDataSetChanged();                                              // 刷新适配器
        }
        if (index > -1) { //由于recyclerView高度固定（一屏显示11条item），要想使选中的滚动到中间，这里选中index+5，视觉上即在中间位置
            if (list.size() <= 12) return;//一屏能展示完，无需滚动
            int targetIndex = index + 6;                                                // 目标滚动位置
            if (targetIndex >= list.size()) targetIndex = list.size() - 1;              // 不超过最大值
            recyclerView.smoothScrollToPosition(targetIndex);//滚动到选中的item
        }
    }

    /** 是否长按向上标记 */
    private boolean isLongPressUp;
    /** 长按连续滚动Runnable */
    private Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            moveOnlyScroll(isLongPressUp);                                              // 持续滚动
            myHandler.postDelayed(this, 100);                                           // 100ms后再次执行
        }
    };

    /**
     * 上下滚动指示图触摸监听器
     * <p>支持短按单步滚动和长按连续滚动</p>
     */
    private View.OnTouchListener onTouchListener = new OnTouchListener() {
        private long startTime;

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.img_up:
                    isLongPressUp = true;                                               // 向上
                    break;
                case R.id.img_bottom:
                    isLongPressUp = false;                                              // 向下
                    break;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:                                           // 按下
                    startTime = System.currentTimeMillis();                              // 记录开始时间
                    myHandler.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());//默认400ms之后开启持续长按事件
                    break;
                case MotionEvent.ACTION_UP:                                             // 抬起
                case MotionEvent.ACTION_CANCEL:                                          // 取消
                    long duration = System.currentTimeMillis() - startTime; //触摸开始到结束时间
                    if (duration < ViewConfiguration.getLongPressTimeout()) {//处理单击事件
                        moveOnlyScroll(isLongPressUp);                                   // 单步滚动
                    }
                    myHandler.removeCallbacks(longPressRunnable); //停止持续长按事件
                    break;
            }
            return true;
        }
    };

    /**
     * 按钮点击监听器
     * <p>处理删除通道、切换通道类型、上下调节、添加通道、浏览文件等操作</p>
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_delete_channel:                                           // 删除通道
                    CacheUtil.get().putMapInForce(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChannelNumber, String.valueOf(false)); // 标记非用户添加
                    CacheUtil.get().putMapInForce(CacheUtil.RIGHT_SLIP_REF_CHECK + refChannelNumber, String.valueOf(false)); // 标记关闭
                    switchBox.setState(false);                                           // 关闭开关
                    onToggleStateChangedListener.onToggleStateChanged(switchBox, switchBox.isState()); // 触发状态变更
//                    resetDefaultColor(refChannelNumber);
                    break;
                case R.id.channelMath:                                                  // 切换到Math
                    setChannelState();                                                   // 设置Ref选中
                    hideSlip();                                                          // 隐藏滑出菜单
                    RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_OTHER_CHANNEL, 0);      // 发送显示Math通道消息
                    break;
                case R.id.channelRef:                                                   // 切换到Ref
                    setChannelState();                                                   // 设置Ref选中
                    break;
                case R.id.channelSerials:                                               // 切换到Serial
                    setChannelState();                                                   // 设置Ref选中
                    hideSlip();                                                          // 隐藏滑出菜单
                    RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_OTHER_CHANNEL, 2);      // 发送显示Serial通道消息
                    break;
                case R.id.btnTop:                                                       // 上移（垂直档位增大）
                    ivBackground.setImageResource(R.drawable.svg_right_chx_button_u_88x174); // 上箭头背景
                    msgRef.setUpClick(true);                                             // 向上标记
                    postChange();                                                        // 发送变更消息
                    break;
                case R.id.btnBottom:                                                    // 下移（垂直档位减小）
                    ivBackground.setImageResource(R.drawable.svg_right_chx_button_d_88x174); // 下箭头背景
                    msgRef.setUpClick(false);                                            // 向下标记
                    postChange();                                                        // 发送变更消息
                    break;
                case R.id.btn_add_channel://TODO check文件存在性
                    if (adapter.getSelectItem() == null || adapter.getSelectItem().getPathFile().isEmpty()) { // 无选中文件
                        DToast.get().show(context.getResources().getString(R.string.select_ref_data_first)); // 提示选择文件
                    } else {
                        if (!Tools.fileIsExists(adapter.getSelectItem().getPathFile())) { // 文件不存在
                            DToast.get().show(context.getResources().getString(R.string.select_flie_not_exist)); // 提示文件不存在
                            return;
                        }
                        if(getSelectedIndex() == 1) { //csv
                            loadCsvFile(adapter.getSelectItem());                       // CSV加载
                        } else {
                            needUpdateMasterLocation = true;                             // 标记需要更新位置
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChannelNumber, String.valueOf(true)); // 标记用户添加
                            switchBox.setState(true);                                    // 开启开关
                            onToggleStateChangedListener.onToggleStateChanged(switchBox, switchBox.isState()); // 触发状态变更
                            hideSlip();                                                  // 隐藏滑出菜单
                        }
                    }
                    break;
                case R.id.btn_browse:                                                   // 浏览文件
                    handleBrowseClick();                                                 // 处理浏览点击
                    break;
            }
        }
    };

    /**
     * 处理浏览按钮点击
     * <p>打开文件选择器，让用户选择参考波形文件目录</p>
     */
    private void handleBrowseClick() {
        String spinnerSelectPath = spinner.getSelectItem();                              // 当前Spinner选中路径
        String disPlay = spinner.getDisPlaySelectItem();                                 // 显示名称
        File file = new File(spinnerSelectPath);                                         // 创建File对象

        if (!file.exists() || !file.isDirectory()) {                                    // 路径无效
            spinnerSelectPath = "/storage/emulated/0";                                  // 默认内部存储
            disPlay = context.getResources().getString(R.string.internal_storage);       // 内部存储名称
        }

        FileSelector fileSelector = new FileSelector((selectedPath) -> {                // 文件选择回调
            addPathToPathSet(selectedPath);                                             // 添加到路径历史
            updateList();                                                               // 更新文件列表
        });
        fileSelector.buildSaveFileRefSelector(spinnerSelectPath, disPlay, null, context); // 构建文件选择器
    }


    /**
     * Spinner目录选择监听器
     */
    TopViewSpinner.onItemSelectListener onItemSelectListener = new TopViewSpinner.onItemSelectListener() {
        @Override
        public void onItemSelected(FileBean str) {
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_WAVE_PATH_CURRENT + CacheUtil.WAVE_TYPE_BIN, str.getPath()); // 缓存当前路径
            addPathToPathSet(str);                                                      // 添加到路径历史
            updateList();                                                               // 更新文件列表
        }
    };

    /**
     * 获取最近使用的目录列表
     * @return FileBean列表（逆序）
     */
    private ArrayList<FileBean> getPreviousDirectory() {
        return pathSet.getReverseList();                                                // 返回逆序列表
    }

    /**
     * 添加路径到路径历史集合
     * @param pathStr 路径Bean
     */
    private void addPathToPathSet(FileBean pathStr) {
        handleAddPath(pathStr);                                                         // 处理重复路径
        pathSet.add(pathStr);                                                           // 添加到集合
        spinner.updateDataList(getPreviousDirectory(), null);                           // 更新Spinner
        savePathToCache();                                                              // 保存到缓存
    }

    /**
     * 处理添加路径前的去重
     * <p>如果路径已存在，先移除旧记录（使其移到最新位置）</p>
     * @param pathStr 路径Bean
     * @return true=新路径, false=已有路径
     */
    private boolean handleAddPath(FileBean pathStr) {
        boolean canAdd = true;
        FileBean temp = null;
        for (FileBean fileBean : pathSet) {                                             // 遍历查找重复
            if (fileBean.getPath().equals(pathStr.getPath())) {                         // 路径匹配
                canAdd = false;                                                         // 标记重复
                temp = fileBean;                                                        // 记录旧记录
                break;
            }
        }
        if (temp != null) {
            pathSet.remove(temp);                                                       // 移除旧记录
        }
        return canAdd;
    }

    /**
     * 将路径历史保存到缓存
     */
    public void savePathToCache() {
        int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber); // 读取加载类型

        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_PATH + refType + refChannelNumber,
                StrUtil.getStringFromList(FileBeanToStr.getDisPlayStrList(pathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP)); // 缓存显示路径

        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + refType + refChannelNumber,
                StrUtil.getStringFromList(FileBeanToStr.getAbsoluteStrList(pathSet.getPositiveList()), CacheUtil.WAVE_STORE_PATH_SLIP)); // 缓存绝对路径

        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_DATA_PATH_CURRENT + refType + refChannelNumber, spinner.getSelectItem()); // 缓存当前路径
    }

    /**
     * 从缓存恢复路径历史
     */
    public void restorePath() {
        int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber); // 读取加载类型
        pathSet.clear();                                                                // 清空路径集合
        String pathCacheStr = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_PATH + refType + refChannelNumber); // 读取显示路径
        String abPathCacheStr = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_ABSOLUTE_PATH + refType + refChannelNumber); // 读取绝对路径
        String currentPath = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_PATH_CURRENT + refType + refChannelNumber); // 读取当前路径
        ArrayList<String> pathCacheList = StrUtil.getListFromString(pathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析显示路径列表
        ArrayList<String> abPathCacheList = StrUtil.getListFromString(abPathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP); // 解析绝对路径列表
        if (!Tools.fileIsExists(currentPath)) {                                         // 当前路径无效
            currentPath = null;
        }

        ArrayList<FileBean> dataList = new ArrayList<>();
        FileBean currentBean = new FileBean();
        for (int i = 0; i < abPathCacheList.size(); i++) {                              // 遍历路径列表
            if (!Tools.fileIsExists(abPathCacheList.get(i))) continue;                  // 跳过不存在的路径
            FileBean fileBean = new FileBean();
            fileBean.setPath(abPathCacheList.get(i));                                   // 设置绝对路径
            fileBean.setDisplayName(pathCacheList.get(i));                              // 设置显示名称
            if(abPathCacheList.get(i).equals(currentPath)) {                            // 匹配当前路径
                currentBean.setPath(abPathCacheList.get(i));
                currentBean.setDisplayName(pathCacheList.get(i));
            }
            dataList.add(fileBean);
        }

        pathSet.addAll(dataList);                                                       // 添加到路径集合
        spinner.updateDataList(getPreviousDirectory(), null);                           // 更新Spinner
    }


    /**
     * 加载类型选择监听器
     */
    private RightViewSelect.OnItemClickListener onRightViewSelectItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();                                       // 播放按键音效
        }

        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            RightLayoutRef.this.onItemClick(viewId, item, false);                       // 委托处理
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {

        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {

        }
    };

    /**
     * 处理加载类型选择
     * @param viewId 控件ID
     * @param item 选择项
     * @param isFromEventBus 是否来自事件总线
     */
    @SuppressLint("NonConstantResourceId")
    private void onItemClick(int viewId, RightBeanSelect item, boolean isFromEventBus) {
        switch (viewId) {
            case R.id.ref_load_type:                                                    // 加载类型变更
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber, String.valueOf(refLoadType.getSelectIndex())); // 缓存类型
//                if (refChan == refChannelNumber) {
                restorePath();                                                          // 恢复路径
                updateList();                                                           // 更新文件列表
//                }
                break;
        }
    }


    /**
     * 设置通道类型为Ref选中状态
     */
    private void setChannelState() {
        channelMath.setChecked(false);                                                  // 取消Math选中
        channelRef.setChecked(true);                                                    // 选中Ref
        channelSerials.setChecked(false);                                               // 取消Serial选中
    }

    /**
     * 根据通道索引设置控件颜色
     * @param chIdx 通道索引
     */
    private void setControlColorByChIdx(int chIdx) {
        //textColorNewTopTitleUnSelect
        channelTitle.setTextColor(TChan.getChannelColor(context, refChannelNumber));    // 设置标题颜色
        switchBox.setControlColorByChIdx(chIdx);                                        // 设置开关颜色
        channelRef.setTextColor(TChan.getChannelColor(context, refChannelNumber));      // 设置Ref按钮颜色

        refLoadType.setControlColorByChIdx(chIdx);                                      // 设置加载类型颜色
        setRefLoadTypeEnable();                                                         // 设置加载类型可用性
        adapter.notifyItemChanged(list.indexOf(adapter.getSelectItem()));                // 刷新选中项
//        selectColor.setEditColor(SvgNodeInfo.getAllBaseColor(refChannelNumber));

        int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber); // 读取加载类型
        refLoadType.setSelectIndex(refType);                                            // 设置加载类型
    }

    /**
     * 设置加载类型的可用性
     * <p>目前只启用WAV和CSV，其他选项置灰</p>
     */
    private void setRefLoadTypeEnable() {//目前只是CSV WAV可选，其他置灰
        if (refLoadType == null) return;                                                // 控件未初始化
        this.refLoadType.setEnabled(0, true);//WAV                                      // WAV可用
        this.refLoadType.setEnabled(1, true);//CSV                                      // CSV可用
        this.refLoadType.setEnabled(2, false);                                          // 选项2不可用
        this.refLoadType.setEnabled(3, false);                                          // 选项3不可用
    }

    /**
     * 隐藏当前Ref通道的滑出菜单
     */
    private void hideSlip() {
        int slipIndex = MainViewGroup.RIGHTSLIP_REF1;                                   // 默认R1
        switch (refChannelNumber) {
            case TChan.R1:
                slipIndex = MainViewGroup.RIGHTSLIP_REF1;                               // R1
                break;
            case TChan.R2:
                slipIndex = MainViewGroup.RIGHTSLIP_REF2;                               // R2
                break;
            case TChan.R3:
                slipIndex = MainViewGroup.RIGHTSLIP_REF3;                               // R3
                break;
            case TChan.R4:
                slipIndex = MainViewGroup.RIGHTSLIP_REF4;                               // R4
                break;
            case TChan.R5:
                slipIndex = MainViewGroup.RIGHTSLIP_REF5;                               // R5
                break;
            case TChan.R6:
                slipIndex = MainViewGroup.RIGHTSLIP_REF6;                               // R6
                break;
            case TChan.R7:
                slipIndex = MainViewGroup.RIGHTSLIP_REF7;                               // R7
                break;
            case TChan.R8:
                slipIndex = MainViewGroup.RIGHTSLIP_REF8;                               // R8
                break;
        }
        RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(slipIndex, false)); // 发送隐藏消息
    }

    /**
     * 更新最大Ref通道编号缓存
     * <p>遍历所有Ref通道，找到用户添加的通道中编号最大的</p>
     */
    private void updateMaxChannelNumber() {
        AtomicInteger maxChan = new AtomicInteger(TChan.R1 - 1);                        // 初始值小于R1
        TChan.foreachRef(refChan -> {                                                   // 遍历Ref通道
            boolean refCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + refChan); // 读取开关状态
//            if (refCheck) {
            if (isAddByUser(refChan)) {                                                 // 用户添加的通道
                maxChan.set(Math.max(maxChan.get(), refChan));                          // 更新最大值
            }
        });
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MAX_CHANNEL_NUMBER_REF, maxChan.toString()); // 缓存最大编号
    }

    /**
     * 发送垂直档位变更消息（带延时恢复背景）
     */
    private void postChange() {
        if (myHandler.hasMessages(HANDLE_MSG)) {                                        // 已有延时消息
            myHandler.removeMessages(HANDLE_MSG);                                       // 移除旧消息
        }
        myHandler.sendEmptyMessageDelayed(HANDLE_MSG, 200);                             // 200ms后恢复背景
        RxBus.getInstance().post(RxEnum.MQ_MSG_CHANNEL_REFX, msgRef);                  // 发送Ref变更消息
    }


    /** Handler消息标识 */
    private static final int HANDLE_MSG = 1;

    /**
     * 弱引用Handler，用于延时恢复垂直档位按钮背景
     * <p>使用WeakReference防止内存泄漏</p>
     */
    public static class MyHandler extends Handler {
        private final WeakReference<RightLayoutRef> rightLayoutHandler;

        public MyHandler(RightLayoutRef layoutRef) {
            rightLayoutHandler = new WeakReference<RightLayoutRef>(layoutRef);          // 弱引用包装
        }

        @Override
        public void handleMessage(Message msg) {
            if (rightLayoutHandler.get() != null) {                                     // 引用未回收
                if (msg.what == HANDLE_MSG) {                                           // 恢复背景消息
                    RightLayoutRef layoutRef = (RightLayoutRef) rightLayoutHandler.get();
                    layoutRef.ivBackground.setImageResource(R.drawable.svg_right_chx_button_88x174); // 恢复默认背景
                }
            }
        }
    }

//    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
//
//        @Override
//        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//            super.onScrollStateChanged(recyclerView, newState);
//            if (newState == RecyclerView.SCROLL_STATE_IDLE) { //当前未滑动
//                showUpBottomImg();
//            }
//        }
//
//        @Override
//        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//            super.onScrolled(recyclerView, dx, dy);
//            // dy大于0表示正在向上滑动，小于等于0表示停止或向下滑动
//        }
//    };

    /**
     * 判断Ref通道是否由用户添加
     * @param refChan 参考通道编号
     * @return true=用户添加, false=非用户添加
     */
    private boolean isAddByUser(int refChan) {
        return CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + refChan); // 读取缓存标记
    }

    /**
     * 更新CSV加载进度
     * <p>通过EventFactory发送进度事件，通知进度条更新</p>
     * @param progress 加载进度(0-100)
     */
    private void updateLoadProgress(int progress) {
        EventBase eventBase = new EventBase(EventFactory.EVENT_LOADCSV_RUN);            // 创建CSV加载事件
        Logger.i("LoadCsv progress=" + progress + " ,ThreadName= " + Thread.currentThread().getName()); // 日志
        eventBase.setData(progress);                                                    // 设置进度数据
        EventFactory.sendEvent(eventBase, true);                                        // 发送事件

    }

    /**
     * 设置Ref通道标签
     * @param chNo FPGA通道编号
     * @param label 标签文本
     */
    public void setChannelLabel(int chNo, String label) {
        RefChannel refChannel = ChannelFactory.getRefChannel(chNo);                     // 获取参考通道
        if (refChannel != null) {
            refChannel.setLabel(label);                                                 // 设置标签
        }
    }

    /**
     * 从Ref通道获取标签
     * @param chNo FPGA通道编号
     * @return 标签文本
     */
    public String getLabelFromChannel(int chNo) {
        String label = "";
        RefChannel refChannel = ChannelFactory.getRefChannel(chNo);                     // 获取参考通道
        if (refChannel != null) {
            label = refChannel.getLabel();                                              // 获取标签
        }
        return label;
    }

    /**
     * 可编辑控件点击监听器
     * <p>处理通道标签、颜色选择、相位延迟等编辑操作</p>
     */
    @SuppressLint("NonConstantResourceId")
    private final TopViewEdit.OnClickEditListener onClickEditListener = (v, text) -> {
        PlaySound.getInstance().playButton();                                           // 播放按键音效
        switch (v.getId()) {
            case R.id.chLabel:                                                          // 通道标签
                if (dialogChannelLabel == null) {
                    dialogChannelLabel = (DialogChannelLabel) ((MainActivity) context).findViewById(R.id.dialogChannelLabel); // 获取对话框
                }
                dialogChannelLabel.setData(refChannelNumber, chLabel.getText()
                        , CacheUtil.RIGHT_SLIP_CH_LABEL_USERDEFINE + refChannelNumber
                        , DialogChannelLabel.FROM_MATHREF
                        , result -> {
                            PlaySound.getInstance().playButton();                       // 播放按键音效
                            setLabel(result);                                           // 设置标签
                        }
                );
                break;
            case R.id.select_color:                                                     // 颜色选择
                if (dialogSelectColor == null) {
                    dialogSelectColor = (DialogSelectColor) ((MainActivity)context).findViewById(R.id.dialogSelectColor); // 获取对话框
                }
                dialogSelectColor.setData(DialogSelectColor.FROM_MATHREF, refChannelNumber, (chIndex, colorStr) -> { // 设置数据
                    if (refChannelNumber == chIndex) {                                   // 匹配当前通道
                        Logger.d(TAG, "选中的颜色值为：" + colorStr + " channelNum= " + chIndex); // 日志
                        selectColor.setEditColor(colorStr);                              // 设置颜色
                    }
                });
                break;
            case R.id.phase_delay:                                                      // 相位延迟
                if(dialogFloatKeyBoard == null) {
                    dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard); // 获取浮动键盘
                }

                String txt= phaseDelay.getText().replace("s", "").replace(" ","");      // 去除单位
                dialogFloatKeyBoard.setRefFloatData(txt, phaseDelay, new TopDialogFloatKeyBoard.OnDismissListener() { // 设置数据
                    @Override
                    public void onDismiss(View fromView, String show) {
                        PlaySound.getInstance().playButton();                           // 播放按键音效
                        double delay = TBookUtil.getBigDoubleFromM(show);               // 解析延迟值
                        if ("0".equals(show.trim())) {                                   // 零值
                            show = "0 ns";
                        } else {
                            show = show + "s";                                           // 添加单位
                        }
                        RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber)); // 获取参考通道
                        double original = 0;
                        if(refChannel != null) {
                            original = refChannel.getRefXPos_original() / 1.0E13;       // 获取原始位置
                            refChannel.setDelay(delay);                                  // 设置延迟
                        }
                        Logger.d(TAG, "result= " + show + " ,delay= " + delay + " ,original= " + original); // 日志
//                        setTimePos(delay);
//                        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_DELAY + refChannelNumber, show);
                        phaseDelay.setText(show);                                       // 更新UI

                    }
                });
                break;
            default:
                break;
        }
    };

    /**
     * 设置时间位置（延迟）
     * @param delay 延迟值(秒)
     */
    private void setTimePos(Double delay) {
        RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber)); // 获取参考通道
        if (refChannel == null) return;                                                 // 空通道
        if (refChannel.getRefType() != WaveData.FFT_WAVE) {                             // 非FFT波形
            Command.get().getReference().Timebase_Position(TChan.toFpgaChNo(refChannelNumber), delay, true); // 发送时基位置命令
        }
        ((MainActivity) context).getSlider().updateUI();                                // 更新滑块UI
    }


    /**
     * 冷启动CSV加载Runnable
     * <p>从缓存读取CSV文件路径，在后台线程中加载CSV数据到参考通道</p>
     */
    private Runnable loadCsRunnable = new Runnable() {
        @Override
        public void run() {

            int flag = ScreenControls.LOCK_LOADCSV << (refChannelNumber - TChan.R1);    // 锁屏标志

            int refType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_TYPE + refChannelNumber); // 读取加载类型
            String refFilePath = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_SELECT_CURRENT + refChannelNumber); // 读取文件路径
            String tempString = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_REF_DATA_FROM + refChannelNumber); // 读取来源信息
            String[] channelToRef = tempString.split(";");                              // 解析通道映射
            if (channelToRef.length > 0 && channelToRef[0].equals(refFilePath)) {       // 来源与文件路径匹配
                LoadCsv loadCsv = new LoadCsv();                                        // 创建CSV加载器
                boolean loadSuccess = false;
                try {
                    loadSuccess = loadCsv.load(refFilePath);                            // 加载CSV文件
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "chNums:" + loadCsv.getChNums() + ",b:" + loadSuccess);
                if (!loadSuccess) return;//这里是冷启动加载Cache中记录的文件，解析失败暂不做处理。
                HashMap<Integer, Integer> map = new HashMap<>();
                map.put(TChan.toFpgaChNo(refChannelNumber), TChan.toFpgaChNo(Integer.parseInt(channelToRef[1]))); // 构建通道映射
                loadCsv.loadToRef(map);                                                 // 加载CSV到Ref
                loadCsv.setLoadCsvProgress(new LoadCsv.ILoadCsvProgress() {             // 设置进度回调
                    @Override
                    public void onProgress(int val) {
                        ScreenControls.getInstance().csvupdate(flag,val);               // 更新进度
                    }
                });

                while (!loadCsv.isFinish()) {                                           // 等待加载完成
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                post(()->{                                                              // 切回UI线程
                    DialogRefRecallBean item = null;
                    for (DialogRefRecallBean bean : getList(spinner.getSelectItem())) { // 查找匹配的文件Bean
                        if (bean.getPathFile().equals(refFilePath)) {
                            item = bean;
                            break;
                        }
                    }
                    ChannelFactory.chOpen(TChan.toFpgaChNo(refChannelNumber));         // 打开通道
                    setCacheMapValue(TChan.toFpgaChNo(refChannelNumber), item);         // 更新缓存
                    ScreenControls.getInstance().unLockScreen(flag);                    // 解锁屏幕
                });
            }
        }
    };

    /**
     * 获取RecyclerView
     * @return RecyclerView实例
     */
    public RecyclerView getRecyclerView() {
        return recyclerView;
    }


    /**
     * 滚动到指定位置并获取视图位置信息
     * <p>用于CSV加载时定位列表项</p>
     * @param targetPosition 目标位置
     */
    public void scrollToPositionAndGetView(int targetPosition) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();     // 获取布局管理器
        layoutManager.scrollToPosition(targetPosition);                                 // 滚动到目标位置
        recyclerView.post(() -> {                                                       // 等待布局完成
            View targetView = layoutManager.findViewByPosition(targetPosition);         // 查找目标视图
            if (targetView != null) {
                int[] local = new int[2];
                targetView.getLocationOnScreen(local);                                   // 获取屏幕坐标
                RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_CSV_ITEM_POS,
                        refChannelNumber + CommandMsgToUI.PARAM_SPLIT + local[1] + CommandMsgToUI.PARAM_SPLIT + targetPosition); // 发送位置信息
            }
        });
    }

    /** CSV中的通道编号列表 */
    private ArrayList<Integer> channelInCsv = new ArrayList<>();

    /**
     * 获取CSV中的通道编号列表
     * @return 通道编号列表
     */
    public ArrayList<Integer> getChannelInCsv() {
        return channelInCsv;
    }

    /**
     * CSV加载进度事件UI观察者
     * <p>根据进度值控制屏幕锁屏/解锁和进度条显示</p>
     */
    EventUIObserver eventLoadCsvObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase base = (EventBase) data;
            if (base == null) return;                                                   // 空数据
            int progress = 0;
            switch (base.getId()) {
                case EventFactory.EVENT_LOADCSV_RUN:                                    // CSV加载进度
                    progress = (int) ((EventBase) data).getData();                       // 获取进度值
                    break;
            }
            ScreenControls screenControls = ScreenControls.getInstance();                // 获取屏幕控制
            if (progress < 0 || progress >= 100) {                                      // 加载完成
                screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS);              // 解锁进度锁
            } else {                                                                    // 加载中
                if (!screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) {       // 未锁定
                    screenControls.lockScreen(ScreenControls.LOCK_PROGRESS);            // 锁定屏幕
                }
                screenControls.setProgressValue(progress);                              // 更新进度值
            }
        }
    };

    /**
     * 颜色选择消费者
     * <p>当通道颜色变更时，更新控件颜色</p>
     */
    private Consumer<String> consumerSelectColor = new Consumer<String>() {
        @Override
        public void accept(String colorInfo) throws Throwable {
            if (colorInfo.isEmpty()) return;                                            // 空信息
            Logger.i(TAG, "selectColorInfo= " + colorInfo);                             // 日志
            String[] info = colorInfo.split(";");                                       // 解析参数
            int chIndex = Integer.parseInt(info[0]);                                    // 通道索引
            String colorStr = info[1];                                                  // 颜色值
            setControlColorByChIdx(chIndex);                                            // 更新控件颜色
        }
    };

    /**
     * 重置通道颜色为默认值
     * @param chIndex 通道索引
     */
    private void resetDefaultColor(int chIndex) {
        String colorDefault = SvgNodeInfo.getDefaultColor(chIndex);                     // 获取默认颜色
        SvgNodeInfo.setChannelColor(chIndex, colorDefault);//改变颜色值
        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_COLOR + chIndex, colorDefault);   // 缓存颜色
        selectColor.setEditColor(colorDefault);                                         // 更新UI
        RxBus.getInstance().post(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR, chIndex + ";" + colorDefault);//发消息通知颜色值改变了
    }



    /**
     * 时基更新消费者
     * <p>当前已注释掉处理逻辑，预留接口</p>
     */
    private final Consumer<String> consumerUpdateTimeBase = new Consumer<String>() {
        @Override
        public void accept(String triggerTimeBaseInfo) throws Throwable {
//            Logger.d(TAG, "chActive= " + ChannelFactory.getChActivate() + " ,timePosInfo =" + triggerTimeBaseInfo);
//            String timePos = triggerTimeBaseInfo.split(";")[0];
//            int chIdx = Integer.parseInt(triggerTimeBaseInfo.split(";")[1]);
//            if (chIdx != TChan.toFpgaChNo(refChannelNumber)) return;
//            RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(refChannelNumber));
//            double original = 0;
//            double delay = 0;
//            if (refChannel != null) {
//                original = refChannel.getRefXPos_original() / 1.0E13;
//                delay = refChannel.getDelay();
//            }
//            double val = TBookUtil.getBigDoubleFromM(timePos.replace("s", "").replace(" ", ""));
//            double finalVal = val - original;
//            Logger.d(TAG, "oriainal= " + original + " ,val= " + val + " final= " + finalVal + " text= " + TBookUtil.getFourFromD(finalVal));
//            phaseDelay.setText(TBookUtil.getFourFromD(finalVal) + "s");
        }
    };


    /**
     * 用户设置时基消费者
     * <p>当切换到独立时基模式时，重置延迟UI</p>
     */
    private final Consumer<Integer> consumerRefTimeBase = new Consumer<Integer>() {
        @Override
        public void accept(Integer refTbIndex) throws Throwable {
            if (refTbIndex == 1) {//独立调节
                resetDelayUI();                                                         // 重置延迟UI
            }
        }
    };

    /**
     * 重置延迟UI为0 ns
     */
    private void resetDelayUI() {
        phaseDelay.setText("0 ns");                                                     // 设置为0 ns
    }


    /**
     * 鼠标点击位置消费者
     * <p>处理Ref通道的垂直档位/垂直位置/水平位置鼠标点击，
     * 弹出浮动键盘供用户输入数值</p>
     */
    private Consumer<String> consumerMouseClick = new Consumer<String>() {
        @Override
        public void accept(String clickInfo) throws Throwable {
            String[] info = clickInfo.split(";");                                       // 解析参数
            int chIdx = Integer.parseInt(info[0]);                                      // 通道索引
            int clickPos = Integer.parseInt(info[1]);//0垂直档位  1垂直位置  2水平挡位  3水平位置
            Logger.d(TAG, "ClickInfo chidx= " + chIdx + " ,clickPos= " + clickPos);    // 日志
            if (dialogFloatKeyBoard == null) {
                dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard); // 获取浮动键盘
            }
            if (TChan.toUiChNo(chIdx) != refChannelNumber) return;                     // 非当前通道
            String unit = ChannelFactory.getProbeType(chIdx);                           // 获取探头单位
            RefChannel refChannel = ChannelFactory.getRefChannel(chIdx);                // 获取参考通道
            if (refChannel == null) return;                                             // 空通道
            if (clickPos == 0) { //垂直档位
                double extent = refChannel.getVScaleIdVal();                             // 获取垂直档位值
                setChVScale(TBookUtil.getMFromDouble(extent) + unit, chIdx);            // 弹出垂直档位键盘
            }
            if (clickPos == 1) { //垂直位置
                double leftPos = Tools.getChannelPositionUI(TChan.toUiChNo(chIdx));     // 获取垂直位置
                int zoomHeight = (int) (ScopeBase.getNewHeight() * 0.75);               // 缩放高度
                double pos = (Tools.isZoom() ? zoomHeight : 1.0 * ScopeBase.getNewHeight()) / 2 - leftPos; // 计算偏移
                String number = TBookUtil.getFourFromD_Trim0(pos * refChannel.getVerticalPerPix()); // 格式化
                setChVPosition(number + unit, chIdx);                                   // 弹出垂直位置键盘

            }
            if (clickPos == 3) { //水平位置
                int followCh = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE); // 读取时基跟随模式
                String timePosStr = "";
                if (refChannel.getRefType() != WaveData.FFT_WAVE) {                     // 非FFT波形
                    long timePos;
                    if (followCh == 0) { //跟随模拟通道
                        timePos = HorizontalAxis.getInstance().getTimePosOfView();       // 获取模拟通道时基位置
                    } else {
                        timePos = refChannel.getTimePosOfView();                         // 获取独立时基位置
                    }
                    timePosStr = TBookUtil.getSFrom100Fs(timePos);                       // 格式化
                    setChHPosition(timePosStr, chIdx);                                  // 弹出水平位置键盘
                }

            }
        }
    };

    /**
     * 设置水平位置（弹出浮动键盘）
     * @param nowTxt 当前值文本
     * @param chIdx 通道索引
     */
    //水平位置
    public void setChHPosition(String nowTxt, int chIdx) {
        dialogFloatKeyBoard.bringToFront();                                             // 置顶对话框

        String unit = ChannelFactory.getProbeType(chIdx);                               // 获取探头单位
        String txt = nowTxt.toString().replaceAll("(?:s|\\s)", "");                     // 去除单位
        dialogFloatKeyBoard.setFloatData(txt, forDoubleClick, new TopDialogFloatKeyBoard.OnDismissListener() { // 弹出键盘
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();                                   // 播放按键音效
                double val = TBookUtil.getBigDoubleFromM(show);                         // 解析数值
                Command.get().getTimebase().Position(val, true);                        // 发送时基位置命令
            }
        });
    }


    /**
     * 设置垂直位置（弹出浮动键盘）
     * @param nowTxt 当前值文本
     * @param chIdx 通道索引
     */
    //垂直位置
    private void setChVPosition(String nowTxt, int chIdx) {
        dialogFloatKeyBoard.bringToFront();                                             // 置顶对话框

        String unit = ChannelFactory.getProbeType(chIdx);                               // 获取探头单位
        String txt = nowTxt.replace(unit, "").replace(" ", "");                         // 去除单位
        dialogFloatKeyBoard.setFloatData(txt, forDoubleClick, new TopDialogFloatKeyBoard.OnDismissListener() { // 弹出键盘
            @Override
            public void onDismiss(View fromView, String show) {
                Log.d(TAG, "onDismiss: " + chIdx);                                      // 日志
                RefChannel channel = ChannelFactory.getRefChannel(chIdx);               // 获取参考通道
                if(channel!=null){
                    PlaySound.getInstance().playButton();                               // 播放按键音效
                    double val = TBookUtil.getDoubleFromM(show);                         // 解析数值
                    Command.get().getReference().Position(chIdx, val, true);            // 发送位置命令
                }

            }
        });
    }


    /**
     * 设置垂直档位（弹出浮动键盘）
     * @param nowTxt 当前值文本
     * @param chIdx 通道索引
     */
    //垂直档位
    private void setChVScale(String nowTxt, int chIdx) {
        dialogFloatKeyBoard.bringToFront();                                             // 置顶对话框

        String unit = ChannelFactory.getProbeType(chIdx);                               // 获取探头单位
        String txt = nowTxt.replace(unit, "").replace(" ", "");                         // 去除单位
        dialogFloatKeyBoard.setFloatData_Extent(txt, forDoubleClick, new TopDialogFloatKeyBoard.OnDismissListener() { // 弹出键盘
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();                                   // 播放按键音效
                double d = TBookUtil.getDoubleFromM(show);                               // 解析数值
                Command.get().getReference().Vscale(chIdx, d, true);                    // 发送档位命令
            }
        });
    }


}
