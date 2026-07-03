/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位：Micsig MHO系列示波器 - 触发系统 - 主容器Fragment               ║
 * ║  文件名称：TopLayoutTrigger.java                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责：                                                              ║
 * ║    1. 作为触发系统的主容器Fragment，管理所有子Fragment的显示/隐藏           ║
 * ║    2. 处理13种触发类型（常规/边沿/脉宽/逻辑/N边沿/欠幅/斜率/超时/视频/     ║
 * ║       S1~S4串口）的切换逻辑                                              ║
 * ║    3. 初始化4组×7种串口触发列表（UART/LIN/CAN/SPI/I2C/ARINC429/M1553B）  ║
 * ║    4. 响应右侧滑菜单串口配置变更，动态更新串口触发选项                     ║
 * ║    5. 响应远程指令（CommandToUI）和事件总线（EventBus）更新触发类型        ║
 * ║    6. 处理子Fragment的消息转发（OnDetailSendMsgListener）                 ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  架构设计：                                                              ║
 * ║    - Fragment子类，实现SerialsDetailFlag和IOnScrollViewSliderListener    ║
 * ║    - 采用观察者模式：RxBus订阅 + EventFactory事件监听                     ║
 * ║    - 数据流向：UI → Command(硬件) + CacheUtil(缓存) + TriggerFactory(模型)║
 * ║    - 反向同步：EventBus/RxBus/CommandToUI → UI更新                       ║
 * ║    - 子Fragment通过FragmentTransaction管理，hide/show切换显示             ║
 * ║    - 串口触发列表按4组（S1~S4）×7种协议独立管理                          ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系：                                                              ║
 * ║    - TopLayoutTriggerCommon/Edge/Pulsewidth/Logic/NEdge/Runt/Slope/      ║
 * ║      Timeout/Video : 各触发类型子Fragment                                ║
 * ║    - TopLayoutTriggerSerials : 串口触发子Fragment（S1~S4复用）            ║
 * ║    - Command : 硬件指令发送中间件                                          ║
 * ║    - CacheUtil : 参数缓存持久化                                           ║
 * ║    - RxBus/EventFactory : 事件总线                                        ║
 * ║    - TriggerFactory : 触发器工厂（获取/设置触发类型）                      ║
 * ║    - TopMatchTrigger : 触发类型/视频参数映射工具（UI索引与示波器索引互转）  ║
 * ║    - TopViewTitleWithScroll : 顶部带滑动的标题栏控件                      ║
 * ║    - SerialsDetailFlag : 串口触发详情标志位接口                           ║
 * ║    - RightMsgSerials及子类 : 右侧滑菜单串口配置消息                      ║
 * ╠══════════════════════════════════════════════════════════════════════════╣
 * ║  使用场景：                                                              ║
 * ║    用户在顶部菜单选择"触发"功能时，显示此Fragment作为触发配置主界面         ║
 * ║    通过顶部标题栏切换不同触发类型，下方显示对应的参数配置子Fragment         ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发布局包

import android.content.Context; // 上下文类
import android.os.Bundle; // Fragment参数Bundle
import android.os.Handler; // 异步消息处理器
import android.os.Message; // 消息对象
import android.view.LayoutInflater; // 布局填充器
import android.view.View; // 视图基类
import android.view.ViewGroup; // 视图组基类
import android.widget.RelativeLayout; // 相对布局控件

import androidx.annotation.Nullable; // 可空注解
import androidx.fragment.app.Fragment; // Fragment基类

import com.micsig.tbook.scope.Event.EventBase; // 事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 事件工厂
import com.micsig.tbook.scope.Event.EventUIObserver; // 事件UI观察者
import com.micsig.tbook.scope.Trigger.TriggerFactory; // 触发器工厂
import com.micsig.tbook.tbookscope.LoadCache; // 缓存加载消息
import com.micsig.tbook.tbookscope.R; // 资源ID
import com.micsig.tbook.tbookscope.middleware.command.Command; // 硬件指令中间件
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 指令到UI的消息
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials; // 串口配置消息基类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsM429; // ARINC429串口配置消息
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsSpi; // SPI串口配置消息
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsUart; // UART串口配置消息
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 按键音效工具
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 详情发送消息监听器
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.Serials; // 串口触发选项数据模型
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsDetailFlag; // 串口触发详情标志位接口
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.TopLayoutTriggerSerials; // 串口触发子Fragment
import com.micsig.tbook.tbookscope.util.App; // 应用全局工具
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 标题栏数据Bean
import com.micsig.tbook.ui.top.view.title.TopViewTitle; // 标题栏视图
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll; // 带滑动的标题栏视图

import java.util.ArrayList; // 动态数组
import java.util.List; // 列表接口

import io.reactivex.rxjava3.annotations.NonNull; // 非空注解
import io.reactivex.rxjava3.functions.Consumer; // RxJava消费者接口


/**
 * 触发系统主容器Fragment
 * <p>
 * 管理所有触发类型的子Fragment（常规/边沿/脉宽/逻辑/N边沿/欠幅/斜率/超时/视频/S1~S4串口），
 * 通过顶部标题栏切换不同触发类型，下方显示对应的参数配置子Fragment。
 * 实现SerialsDetailFlag接口（串口触发详情标志位）和IOnScrollViewSliderListener接口（滑动监听）。
 * </p>
 * <p>Created by Administrator on 2017/4/10.</p>
 */
public class TopLayoutTrigger extends Fragment implements SerialsDetailFlag, TopViewTitleWithScroll.IOnScrollViewSliderListener { // 触发系统主容器Fragment
    private static final String TAG = "TopLayoutTrigger"; // 日志标签
    public static final int DETAIL_COMMON = 0; // 常规触发详情索引
    public static final int DETAIL_EDGE = 1; // 边沿触发详情索引
    public static final int DETAIL_PULSEWIDTH = 2; // 脉宽触发详情索引
    public static final int DETAIL_LOGIC = 3; // 逻辑触发详情索引
    public static final int DETAIL_NEDGE = 4; // N边沿触发详情索引
    public static final int DETAIL_RUNT = 5; // 欠幅触发详情索引
    public static final int DETAIL_SLOPE = 6; // 斜率触发详情索引
    public static final int DETAIL_TIMEOUT = 7; // 超时触发详情索引
    public static final int DETAIL_VIDEO = 8; // 视频触发详情索引

    public static final int DETAIL_S1 = 9; // S1串口触发详情索引
    public static final int DETAIL_S2 = 10; // S2串口触发详情索引
    public static final int DETAIL_S3 = 11; // S3串口触发详情索引
    public static final int DETAIL_S4 = 12; // S4串口触发详情索引

    private Context context; // 上下文引用
    private RelativeLayout triggerDetail; // 触发详情容器布局
    private TopViewTitleWithScroll triggerTitle; // 触发类型标题栏（带滑动）
    private TopLayoutTriggerCommon triggerCommonLayout;         //常规
    private TopLayoutTriggerEdge triggerEdgeLayout;             //边沿
    private TopLayoutTriggerPulsewidth triggerPulsewidthLayout; //脉宽
    private TopLayoutTriggerLogic triggerLogicLayout;           //逻辑
    private TopLayoutTriggerNEdge triggerNEdgeLayout;           //N边沿
    private TopLayoutTriggerRunt triggerRuntLayout;             //欠幅
    private TopLayoutTriggerSlope triggerSlopeLayout;           //斜率
    private TopLayoutTriggerTimeout triggerTimeoutLayout;       //超时
    private TopLayoutTriggerVideo triggerVideoLayout;           //视频
    private TopLayoutTriggerSerials triggerS1Layout;            //S1
    private TopLayoutTriggerSerials triggerS2Layout;            //S2
    private TopLayoutTriggerSerials triggerS3Layout;            //S3
    private TopLayoutTriggerSerials triggerS4Layout;            //S4

    private TopMsgTrigger msgTrigger; // 触发消息对象，用于向主界面传递触发配置

    /** S1组串口触发列表：7种协议（UART/LIN/CAN/SPI/I2C/ARINC429/M1553B） */
    private ArrayList<Serials> uart1, lin1, can1, spi1, i2c1, arinc4291, m1553b1; // S1组串口列表
    /** S2组串口触发列表：7种协议 */
    private ArrayList<Serials> uart2, lin2, can2, spi2, i2c2, arinc4292, m1553b2; // S2组串口列表
    /** S3组串口触发列表：7种协议 */
    private ArrayList<Serials> uart3, lin3, can3, spi3, i2c3, arinc4293, m1553b3; // S3组串口列表
    /** S4组串口触发列表：7种协议 */
    private ArrayList<Serials> uart4, lin4, can4, spi4, i2c4, arinc4294, m1553b4; // S4组串口列表

    /** 子Fragment的Tag数组，用于FragmentManager查找和事务管理 */
    private String[] tags = {"triggerCommon", "triggerEdge", "triggerPulsewidth" // 子Fragment标签数组
            , "triggerLogic", "triggerNEdge", "triggerRunt", "triggerSlope", "triggerTimeout" // 续标签
            , "triggerVideo", "triggerS1", "triggerS2", "triggerS3", "triggerS4"}; // 续标签
    private Fragment[] fragments = new Fragment[13]; // 子Fragment数组，共13个

    /**
     * 创建Fragment视图
     * @param inflater 布局填充器
     * @param container 父容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建视图回调
        return inflater.inflate(R.layout.layout_trigger, container, false); // 填充触发布局
    }

    /**
     * 视图创建完成后的初始化
     * @param view 创建的视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        initView(view, savedInstanceState); // 初始化视图控件
        initControl(); // 初始化事件监听
    }

    /**
     * 初始化视图控件和子Fragment
     * @param view 根视图
     * @param savedInstanceState 保存的实例状态
     */
    private void initView(View view, Bundle savedInstanceState) { // 初始化视图
        triggerTitle = (TopViewTitleWithScroll) view.findViewById(R.id.triggerTitle); // 查找触发类型标题栏
        triggerDetail = (RelativeLayout) view.findViewById(R.id.triggerDetail); // 查找触发详情容器
        triggerTitle.setData(R.array.trigger, onCheckChangedTitleListener, onItemClickListener); // 设置标题栏数据和监听器
        triggerTitle.setOnScrollViewSliderListener(this); // 设置滑动监听器

        initSerialsList(); // 初始化串口触发列表
        initLayout(savedInstanceState); // 初始化子Fragment布局
    }

    //region interface TopViewTitleWithScroll.IOnScrollViewSliderListener
    @Override
    public void onSliderStop() { // 滑动停止回调（空实现）

    }
    //endregion

    /**
     * 初始化所有子Fragment，添加到容器中并设置消息监听器
     * @param savedInstanceState 保存的实例状态，非空时从FragmentManager恢复Fragment
     */
    private void initLayout(Bundle savedInstanceState) { // 初始化子Fragment布局
        if (savedInstanceState != null) { // 如果有保存的状态（Activity重建）
            for (int i = 0; i < tags.length; i++) { // 遍历所有标签
                fragments[i] = getChildFragmentManager().findFragmentByTag(tags[i]); // 从FragmentManager中恢复Fragment
            }
        }

        triggerCommonLayout = fragments[0] == null ? new TopLayoutTriggerCommon() : (TopLayoutTriggerCommon) fragments[0]; // 常规Fragment，新建或恢复
        triggerEdgeLayout = fragments[1] == null ? new TopLayoutTriggerEdge() : (TopLayoutTriggerEdge) fragments[1]; // 边沿Fragment，新建或恢复
        triggerPulsewidthLayout = fragments[2] == null ? new TopLayoutTriggerPulsewidth() : (TopLayoutTriggerPulsewidth) fragments[2]; // 脉宽Fragment，新建或恢复
        triggerLogicLayout = fragments[3] == null ? new TopLayoutTriggerLogic() : (TopLayoutTriggerLogic) fragments[3]; // 逻辑Fragment，新建或恢复
        triggerNEdgeLayout = fragments[4] == null ? new TopLayoutTriggerNEdge() : (TopLayoutTriggerNEdge) fragments[4]; // N边沿Fragment，新建或恢复
        triggerRuntLayout = fragments[5] == null ? new TopLayoutTriggerRunt() : (TopLayoutTriggerRunt) fragments[5]; // 欠幅Fragment，新建或恢复
        triggerSlopeLayout = fragments[6] == null ? new TopLayoutTriggerSlope() : (TopLayoutTriggerSlope) fragments[6]; // 斜率Fragment，新建或恢复
        triggerTimeoutLayout = fragments[7] == null ? new TopLayoutTriggerTimeout() : (TopLayoutTriggerTimeout) fragments[7]; // 超时Fragment，新建或恢复
        triggerVideoLayout = fragments[8] == null ? new TopLayoutTriggerVideo() : (TopLayoutTriggerVideo) fragments[8]; // 视频Fragment，新建或恢复
        triggerS1Layout = fragments[9] == null ? new TopLayoutTriggerSerials() : (TopLayoutTriggerSerials) fragments[9]; // S1串口Fragment，新建或恢复
        triggerS2Layout = fragments[10] == null ? new TopLayoutTriggerSerials() : (TopLayoutTriggerSerials) fragments[10]; // S2串口Fragment，新建或恢复
        triggerS3Layout = fragments[11] == null ? new TopLayoutTriggerSerials() : (TopLayoutTriggerSerials) fragments[11]; // S3串口Fragment，新建或恢复
        triggerS4Layout = fragments[12] == null ? new TopLayoutTriggerSerials() : (TopLayoutTriggerSerials) fragments[12]; // S4串口Fragment，新建或恢复

        if (savedInstanceState == null) { // 首次创建时添加所有子Fragment
            getChildFragmentManager().beginTransaction() // 开启Fragment事务
                    .add(R.id.triggerDetail, triggerCommonLayout, tags[0]) // 添加常规Fragment
                    .add(R.id.triggerDetail, triggerEdgeLayout, tags[1]) // 添加边沿Fragment
                    .add(R.id.triggerDetail, triggerPulsewidthLayout, tags[2]) // 添加脉宽Fragment
                    .add(R.id.triggerDetail, triggerLogicLayout, tags[3]) // 添加逻辑Fragment
                    .add(R.id.triggerDetail, triggerNEdgeLayout, tags[4]) // 添加N边沿Fragment
                    .add(R.id.triggerDetail, triggerRuntLayout, tags[5]) // 添加欠幅Fragment
                    .add(R.id.triggerDetail, triggerSlopeLayout, tags[6]) // 添加斜率Fragment
                    .add(R.id.triggerDetail, triggerTimeoutLayout, tags[7]) // 添加超时Fragment
                    .add(R.id.triggerDetail, triggerVideoLayout, tags[8]) // 添加视频Fragment
                    .add(R.id.triggerDetail, triggerS1Layout, tags[9]) // 添加S1串口Fragment
                    .add(R.id.triggerDetail, triggerS2Layout, tags[10]) // 添加S2串口Fragment
                    .add(R.id.triggerDetail, triggerS3Layout, tags[11]) // 添加S3串口Fragment
                    .add(R.id.triggerDetail, triggerS4Layout, tags[12]) // 添加S4串口Fragment
                    .hide(triggerEdgeLayout) // 隐藏边沿Fragment
                    .hide(triggerPulsewidthLayout) // 隐藏脉宽Fragment
                    .hide(triggerLogicLayout) // 隐藏逻辑Fragment
                    .hide(triggerNEdgeLayout) // 隐藏N边沿Fragment
                    .hide(triggerRuntLayout) // 隐藏欠幅Fragment
                    .hide(triggerSlopeLayout) // 隐藏斜率Fragment
                    .hide(triggerTimeoutLayout) // 隐藏超时Fragment
                    .hide(triggerVideoLayout) // 隐藏视频Fragment
                    .hide(triggerS1Layout) // 隐藏S1串口Fragment
                    .hide(triggerS2Layout) // 隐藏S2串口Fragment
                    .hide(triggerS3Layout) // 隐藏S3串口Fragment
                    .hide(triggerS4Layout) // 隐藏S4串口Fragment
                    .commitAllowingStateLoss(); // 提交事务（允许状态丢失）
        }

        triggerS1Layout.initList(uart1, lin1, can1, spi1, i2c1, arinc4291, m1553b1, CacheUtil.S1); // 初始化S1串口列表
        triggerS2Layout.initList(uart2, lin2, can2, spi2, i2c2, arinc4292, m1553b2, CacheUtil.S2); // 初始化S2串口列表
        triggerS3Layout.initList(uart3, lin3, can3, spi3, i2c3, arinc4293, m1553b3, CacheUtil.S3); // 初始化S3串口列表
        triggerS4Layout.initList(uart4, lin4, can4, spi4, i2c4, arinc4294, m1553b4, CacheUtil.S4); // 初始化S4串口列表

        triggerCommonLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置常规Fragment消息监听
        triggerEdgeLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置边沿Fragment消息监听
        triggerPulsewidthLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置脉宽Fragment消息监听
        triggerLogicLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置逻辑Fragment消息监听
        triggerNEdgeLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置N边沿Fragment消息监听
        triggerRuntLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置欠幅Fragment消息监听
        triggerSlopeLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置斜率Fragment消息监听
        triggerTimeoutLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置超时Fragment消息监听
        triggerVideoLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置视频Fragment消息监听
        triggerS1Layout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置S1串口Fragment消息监听
        triggerS2Layout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置S2串口Fragment消息监听
        triggerS3Layout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置S3串口Fragment消息监听
        triggerS4Layout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置S4串口Fragment消息监听

        msgTrigger = new TopMsgTrigger(); // 创建触发消息对象
        msgTrigger.setTriggerTitle(triggerTitle.getSelected()); // 设置当前选中的触发类型标题
        msgTrigger.setTriggerDetail(triggerCommonLayout.getTriggerDetail()); // 设置当前触发详情
        msgTrigger.setFromEventBus(false); // 标记非EventBus来源
    }

    /**
     * 注册RxBus事件订阅和EventFactory事件观察者
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS_FOLLOW).subscribe(consumerRightSerials); // 订阅右侧串口配置变更事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅指令到UI事件
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_TYPE, eventUIObserver); // 注册触发类型事件观察者
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SYNC_EXTERNAL_TRIGGER_STATE).subscribe(consumerSyncExternalTriggerState); // 订阅外部触发状态同步事件
    }

    /**
     * 初始化4组×7种串口触发列表
     * 每组包含UART/LIN/CAN/SPI/I2C/ARINC429/M1553B七种协议的触发选项
     */
    private void initSerialsList() { // 初始化串口触发列表
        int[] uartsDetailFlag = {NULL, NULL, UART_DATA, UART_0DATA, UART_1DATA // UART详情标志位数组
                , UART_XDATA, NULL}; // 续UART标志位
        int[] linsDetailFlag = new int[]{NULL, LIN_FRAMEID, LIN_IDDATA, NULL, NULL}; // LIN详情标志位数组
        int[] cansDetailFlag = new int[]{NULL, CAN_REMOTEID, CAN_DATAID, CAN_RDID, CAN_IDDATA // CAN详情标志位数组
                , NULL, NULL, NULL, NULL}; // 续CAN标志位
        int[] spisDetailFlag = new int[]{NULL, SPI_DATA, NULL}; // SPI详情标志位数组
        int[] i2csDetailFlag = new int[]{NULL, NULL, NULL, NULL, I2C_NOACKINADR // I2C详情标志位数组
                , I2C_FRAME1, I2C_FRAME2, I2C_ROMDATA, I2C_10WRITEFRAME}; // 续I2C标志位
        int[] m429sDetailFlag = new int[]{NULL, NULL, ARINC429_LABEL, ARINC429_SDI, ARINC429_DATA // ARINC429详情标志位数组
                , ARINC429_SSM, ARINC429_LABELSDI, ARINC429_LABELDATA, ARINC429_LABELSSM, NULL // 续ARINC429标志位
                , NULL, NULL, NULL, NULL, NULL}; // 续ARINC429标志位
        int[] m1553bDetailFlag = new int[]{NULL, NULL, M1553B_CSWORD, M1553B_RTADDR, NULL // M1553B详情标志位数组
                , M1553B_DATAWORD, NULL, NULL}; // 续M1553B标志位

        uart1 = getSerialsListFromStrings(R.array.triggerSerialsUART, uartsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + CacheUtil.S1); // 构建S1组UART列表
        lin1 = getSerialsListFromStrings(R.array.triggerSerialsLIN, linsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + CacheUtil.S1); // 构建S1组LIN列表
        can1 = getSerialsListFromStrings(R.array.triggerSerialsCAN, cansDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + CacheUtil.S1); // 构建S1组CAN列表
        spi1 = getSerialsListFromStrings(R.array.triggerSerialsSPI, spisDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI + CacheUtil.S1); // 构建S1组SPI列表
        i2c1 = getSerialsListFromStrings(R.array.triggerSerialsI2C, i2csDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + CacheUtil.S1); // 构建S1组I2C列表
        arinc4291 = getSerialsListFromStrings(R.array.triggerSerialsARINC429, m429sDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + CacheUtil.S1); // 构建S1组ARINC429列表
        m1553b1 = getSerialsListFromStrings(R.array.triggerSerials1553B, m1553bDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + CacheUtil.S1); // 构建S1组M1553B列表

        uart2 = getSerialsListFromStrings(R.array.triggerSerialsUART, uartsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + CacheUtil.S2); // 构建S2组UART列表
        lin2 = getSerialsListFromStrings(R.array.triggerSerialsLIN, linsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + CacheUtil.S2); // 构建S2组LIN列表
        can2 = getSerialsListFromStrings(R.array.triggerSerialsCAN, cansDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + CacheUtil.S2); // 构建S2组CAN列表
        spi2 = getSerialsListFromStrings(R.array.triggerSerialsSPI, spisDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI + CacheUtil.S2); // 构建S2组SPI列表
        i2c2 = getSerialsListFromStrings(R.array.triggerSerialsI2C, i2csDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + CacheUtil.S2); // 构建S2组I2C列表
        arinc4292 = getSerialsListFromStrings(R.array.triggerSerialsARINC429, m429sDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + CacheUtil.S2); // 构建S2组ARINC429列表
        m1553b2 = getSerialsListFromStrings(R.array.triggerSerials1553B, m1553bDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + CacheUtil.S2); // 构建S2组M1553B列表

        uart3 = getSerialsListFromStrings(R.array.triggerSerialsUART, uartsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + CacheUtil.S3); // 构建S3组UART列表
        lin3 = getSerialsListFromStrings(R.array.triggerSerialsLIN, linsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + CacheUtil.S3); // 构建S3组LIN列表
        can3 = getSerialsListFromStrings(R.array.triggerSerialsCAN, cansDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + CacheUtil.S3); // 构建S3组CAN列表
        spi3 = getSerialsListFromStrings(R.array.triggerSerialsSPI, spisDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI + CacheUtil.S3); // 构建S3组SPI列表
        i2c3 = getSerialsListFromStrings(R.array.triggerSerialsI2C, i2csDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + CacheUtil.S3); // 构建S3组I2C列表
        arinc4293 = getSerialsListFromStrings(R.array.triggerSerialsARINC429, m429sDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + CacheUtil.S3); // 构建S3组ARINC429列表
        m1553b3 = getSerialsListFromStrings(R.array.triggerSerials1553B, m1553bDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + CacheUtil.S3); // 构建S3组M1553B列表

        uart4 = getSerialsListFromStrings(R.array.triggerSerialsUART, uartsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + CacheUtil.S4); // 构建S4组UART列表
        lin4 = getSerialsListFromStrings(R.array.triggerSerialsLIN, linsDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + CacheUtil.S4); // 构建S4组LIN列表
        can4 = getSerialsListFromStrings(R.array.triggerSerialsCAN, cansDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + CacheUtil.S4); // 构建S4组CAN列表
        spi4 = getSerialsListFromStrings(R.array.triggerSerialsSPI, spisDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI + CacheUtil.S4); // 构建S4组SPI列表
        i2c4 = getSerialsListFromStrings(R.array.triggerSerialsI2C, i2csDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + CacheUtil.S4); // 构建S4组I2C列表
        arinc4294 = getSerialsListFromStrings(R.array.triggerSerialsARINC429, m429sDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + CacheUtil.S4); // 构建S4组ARINC429列表
        m1553b4 = getSerialsListFromStrings(R.array.triggerSerials1553B, m1553bDetailFlag, CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + CacheUtil.S4); // 构建S4组M1553B列表

    }

    /**
     * 从字符串数组资源构建串口触发选项列表
     * @param arrayResId 字符串数组资源ID
     * @param detailFlags 详情标志位数组，标识每个选项对应的详情类型
     * @param cacheListKey 缓存键，用于读取当前选中项
     * @return 串口触发选项列表
     */
    private ArrayList<Serials> getSerialsListFromStrings(int arrayResId, int[] detailFlags, String cacheListKey) { // 从字符串数组构建串口列表
        String[] strings = App.get().getResources().getStringArray(arrayResId); // 获取字符串数组
        ArrayList<Serials> list = new ArrayList<>(); // 创建列表
        for (int i = 0; i < strings.length; i++) { // 遍历字符串数组
            Serials serials = new Serials(strings[i], i, cacheListKey, detailFlags[i]); // 创建串口选项对象
            serials.setSelected(i == CacheUtil.get().getInt(cacheListKey)); // 根据缓存设置选中状态
            list.add(serials); // 添加到列表
        }

        if (arrayResId == R.array.triggerSerialsUART && list.size() >= 7) { // 如果是UART且列表足够长
            int bits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_BITS + cacheListKey.replace(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART, "")); // 读取UART数据位配置
            int check = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_CHECK + cacheListKey.replace(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART, "")); // 读取UART校验位配置
            list.get(2).setEnabled(bits != 4); // UART_DATA选项：非4位数据位时可用
            list.get(3).setEnabled(bits == 4); // UART_0DATA选项：4位数据位时可用
            list.get(4).setEnabled(bits == 4); // UART_1DATA选项：4位数据位时可用
            list.get(5).setEnabled(bits == 4); // UART_XDATA选项：4位数据位时可用
            list.get(6).setEnabled(check != 0); // 校验选项：有校验时可用
        }
        return list; // 返回列表
    }

    /**
     * 从缓存恢复触发参数，同步到UI、硬件和模型
     * 重新初始化串口列表、更新标题栏文本、切换触发类型
     */
    private void setCache() { // 设置缓存数据
        initSerialsList(); // 重新初始化串口列表
        triggerS1Layout.initList(uart1, lin1, can1, spi1, i2c1, arinc4291, m1553b1, CacheUtil.S1); // 重新初始化S1串口列表
        triggerS2Layout.initList(uart2, lin2, can2, spi2, i2c2, arinc4292, m1553b2, CacheUtil.S2); // 重新初始化S2串口列表
        triggerS3Layout.initList(uart3, lin3, can3, spi3, i2c3, arinc4293, m1553b3, CacheUtil.S3); // 重新初始化S3串口列表
        triggerS4Layout.initList(uart4, lin4, can4, spi4, i2c4, arinc4294, m1553b4, CacheUtil.S4); // 重新初始化S4串口列表
        triggerS1Layout.setInitCache(); // S1串口Fragment设置缓存
        triggerS2Layout.setInitCache(); // S2串口Fragment设置缓存
        triggerS3Layout.setInitCache(); // S3串口Fragment设置缓存
        triggerS4Layout.setInitCache(); // S4串口Fragment设置缓存

        int serials1 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1); // 读取S1串口类型
        int serials2 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2); // 读取S2串口类型
        int serials3 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3); // 读取S3串口类型
        int serials4 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4); // 读取S4串口类型
        String[] serialsStr = context.getResources().getStringArray(R.array.triggerSerialsTitle); // 获取串口类型标题数组
        triggerTitle.updateItemText(DETAIL_S1, ("S1 " + serialsStr[serials1]).toUpperCase()); // 更新S1标题文本
        triggerTitle.updateItemText(DETAIL_S2, ("S2 " + serialsStr[serials2]).toUpperCase()); // 更新S2标题文本
        triggerTitle.updateItemText(DETAIL_S3, ("S3 " + serialsStr[serials3]).toUpperCase()); // 更新S3标题文本
        triggerTitle.updateItemText(DETAIL_S4, ("S4 " + serialsStr[serials4]).toUpperCase()); // 更新S4标题文本
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 读取当前触发类型索引

        handler.sendEmptyMessageDelayed(MSG, 200); // 延迟200ms发送消息，等待布局更新后滚动标题栏
        triggerTitle.setCacheSelect(triggerIndex); // 设置标题栏缓存选中项
        onCheckChanged(triggerTitle.getSelected(), false); // 触发类型切换
        Command.get().getTrigger().Type(triggerIndex,false); // 发送触发类型指令到硬件

        TriggerFactory.getInstance().setTriggerType(TopMatchTrigger.triggerTypeViewToScope(triggerIndex)); // 设置触发器工厂的触发类型（UI索引转示波器索引）

        if (rightMsgSerials1 != null) { // 如果S1串口消息不为空
            try {
                consumerRightSerials.accept(rightMsgSerials1); // 重新处理S1串口配置
            } catch (Throwable e) {
                e.printStackTrace(); // 打印异常堆栈
            }
        }
        if (rightMsgSerials2 != null) { // 如果S2串口消息不为空
            try {
                consumerRightSerials.accept(rightMsgSerials2); // 重新处理S2串口配置
            }  catch (Throwable e) {
                e.printStackTrace(); // 打印异常堆栈
            }
        }
        if (rightMsgSerials3 != null) { // 如果S3串口消息不为空
            try {
                consumerRightSerials.accept(rightMsgSerials3); // 重新处理S3串口配置
            }  catch (Throwable e) {
                e.printStackTrace(); // 打印异常堆栈
            }
        }
        if (rightMsgSerials4 != null) { // 如果S4串口消息不为空
            try {
                consumerRightSerials.accept(rightMsgSerials4); // 重新处理S4串口配置
            }  catch (Throwable e) {
                e.printStackTrace(); // 打印异常堆栈
            }
        }
    }

    private static final int MSG = 164; // Handler消息标识，用于延迟滚动标题栏
    private Handler handler = new Handler() { // 延迟滚动标题栏的Handler
        @Override
        public void handleMessage(Message msg) { // 处理消息
            super.handleMessage(msg); // 调用父类处理
            if (msg.what == MSG) { // 如果是延迟滚动消息
                int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 读取当前触发类型索引
                triggerTitle.moveOnlyScroll(triggerIndex); // 仅滚动标题栏到指定位置（不触发选中事件）
            }
        }
    };

    /**
     * 设置串口字显示/隐藏时，控制触发标签的可点击性
     * 串口字可见时，禁用常规触发类型标签，保留S1~S4标签可点击
     * @param serialsWordVisible 串口字是否可见
     */
    public void setSerialsWordVisible(boolean serialsWordVisible) { // 设置串口字可见性
        triggerTitle.setEnable(DETAIL_COMMON, !serialsWordVisible); // 常规标签可点击性
        triggerTitle.setEnable(DETAIL_EDGE, !serialsWordVisible); // 边沿标签可点击性
        triggerTitle.setEnable(DETAIL_PULSEWIDTH, !serialsWordVisible); // 脉宽标签可点击性
        triggerTitle.setEnable(DETAIL_LOGIC, !serialsWordVisible); // 逻辑标签可点击性
        triggerTitle.setEnable(DETAIL_NEDGE, !serialsWordVisible); // N边沿标签可点击性
        triggerTitle.setEnable(DETAIL_RUNT, !serialsWordVisible); // 欠幅标签可点击性
        triggerTitle.setEnable(DETAIL_SLOPE, !serialsWordVisible); // 斜率标签可点击性
        triggerTitle.setEnable(DETAIL_TIMEOUT, !serialsWordVisible); // 超时标签可点击性
        triggerTitle.setEnable(DETAIL_VIDEO, !serialsWordVisible); // 视频标签可点击性
        triggerTitle.setEnable(DETAIL_S1, true); // S1标签始终可点击
        triggerTitle.setEnable(DETAIL_S2, true); // S2标签始终可点击
        triggerTitle.setEnable(DETAIL_S3, true); // S3标签始终可点击
        triggerTitle.setEnable(DETAIL_S4, true); // S4标签始终可点击

//        if (serialsWordVisible && triggerTitle.getSelected().getIndex() != DETAIL_S1 && triggerTitle.getSelected().getIndex() != DETAIL_S2) {
//            triggerTitle.moveOnlyScroll(DETAIL_S1);
//            triggerTitle.setSelected(DETAIL_S1);
//            onCheckChanged(triggerTitle.getSelected(), false);
//        }
    }

    /**
     * 发送触发消息到主界面
     */
    public void sendMsg() { // 发送触发消息
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_TRIGGER, msgTrigger); // 通过RxBus发送触发消息
    }

    /**
     * 缓存加载事件消费者
     * 收到缓存加载事件后，恢复触发参数并标记加载完成
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 恢复缓存数据
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutTrigger, true); // 标记触发菜单缓存加载完成
        }
    };

    private RightMsgSerials rightMsgSerials1; // S1组串口配置消息缓存
    private RightMsgSerials rightMsgSerials2; // S2组串口配置消息缓存
    private RightMsgSerials rightMsgSerials3; // S3组串口配置消息缓存
    private RightMsgSerials rightMsgSerials4; // S4组串口配置消息缓存

    /**
     * 右侧滑菜单串口配置变更事件消费者
     * 根据串口组号（S1~S4）和串口类型（UART/LIN/CAN/SPI/I2C/ARINC429/M1553B）
     * 更新对应的串口触发选项列表和子Fragment显示
     */
    private Consumer<RightMsgSerials> consumerRightSerials = new Consumer<RightMsgSerials>() { // 右侧串口配置消费者
        @Override
        public void accept(@NonNull RightMsgSerials rightMsgSerials) throws Exception { // 接收串口配置变更事件
            if (rightMsgSerials.isSerials1()) { // 如果是S1组
                TopLayoutTrigger.this.rightMsgSerials1 = rightMsgSerials; // 缓存S1串口消息
            } else if (rightMsgSerials.isSerials2()) { // 如果是S2组
                TopLayoutTrigger.this.rightMsgSerials2 = rightMsgSerials; // 缓存S2串口消息
            } else if (rightMsgSerials.isSerials3()) { // 如果是S3组
                TopLayoutTrigger.this.rightMsgSerials3 = rightMsgSerials; // 缓存S3串口消息
            } else if (rightMsgSerials.isSerials4()) { // 如果是S4组
                TopLayoutTrigger.this.rightMsgSerials4 = rightMsgSerials; // 缓存S4串口消息
            }
            int serialsType = rightMsgSerials.getSerialsType().getIndex(); // 获取串口类型索引
            int detailSerialsIndex; // 串口详情索引
            String namePre; // 标题名称前缀
            TopLayoutTriggerSerials triggerSerials; // 串口触发子Fragment
            List<Serials> uart, lin, can, spi, i2c, arinc429, m1553b; // 各协议列表引用
            if (rightMsgSerials.isSerials1()) { // S1组
                detailSerialsIndex = DETAIL_S1; // 设置详情索引为S1
                namePre = "s1 "; // 设置名称前缀
                triggerSerials = triggerS1Layout; // 设置串口Fragment为S1
                uart = uart1; // UART列表引用S1
                lin = lin1; // LIN列表引用S1
                can = can1; // CAN列表引用S1
                spi = spi1; // SPI列表引用S1
                i2c = i2c1; // I2C列表引用S1
                arinc429 = arinc4291; // ARINC429列表引用S1
                m1553b = m1553b1; // M1553B列表引用S1
            } else if (rightMsgSerials.isSerials2()) { // S2组
                detailSerialsIndex = DETAIL_S2; // 设置详情索引为S2
                namePre = "s2 "; // 设置名称前缀
                triggerSerials = triggerS2Layout; // 设置串口Fragment为S2
                uart = uart2; // UART列表引用S2
                lin = lin2; // LIN列表引用S2
                can = can2; // CAN列表引用S2
                spi = spi2; // SPI列表引用S2
                i2c = i2c2; // I2C列表引用S2
                arinc429 = arinc4292; // ARINC429列表引用S2
                m1553b = m1553b2; // M1553B列表引用S2
            } else if (rightMsgSerials.isSerials3()) { // S3组
                detailSerialsIndex = DETAIL_S3; // 设置详情索引为S3
                namePre = "s3 "; // 设置名称前缀
                triggerSerials = triggerS3Layout; // 设置串口Fragment为S3
                uart = uart3; // UART列表引用S3
                lin = lin3; // LIN列表引用S3
                can = can3; // CAN列表引用S3
                spi = spi3; // SPI列表引用S3
                i2c = i2c3; // I2C列表引用S3
                arinc429 = arinc4293; // ARINC429列表引用S3
                m1553b = m1553b3; // M1553B列表引用S3
            } else { // S4组
                detailSerialsIndex = DETAIL_S4; // 设置详情索引为S4
                namePre = "s4 "; // 设置名称前缀
                triggerSerials = triggerS4Layout; // 设置串口Fragment为S4
                uart = uart4; // UART列表引用S4
                lin = lin4; // LIN列表引用S4
                can = can4; // CAN列表引用S4
                spi = spi4; // SPI列表引用S4
                i2c = i2c4; // I2C列表引用S4
                arinc429 = arinc4294; // ARINC429列表引用S4
                m1553b = m1553b4; // M1553B列表引用S4
            }
            String[] serials = context.getResources().getStringArray(R.array.triggerSerialsTitle); // 获取串口类型标题数组
            triggerTitle.updateItemText(detailSerialsIndex, (namePre + serials[serialsType]).toUpperCase()); // 更新标题栏串口标签文本
            switch (serialsType) { // 根据串口类型处理
                case UART: // UART串口
                    RightMsgSerialsUart serialsUart = (RightMsgSerialsUart) rightMsgSerials.getSerialsDetails(); // 获取UART详情
                    uart.get(2).setEnabled(serialsUart.getBits().getIndex() != 4); // UART_DATA：非4位数据位时可用
                    uart.get(3).setEnabled(serialsUart.getBits().getIndex() == 4); // UART_0DATA：4位数据位时可用
                    uart.get(4).setEnabled(serialsUart.getBits().getIndex() == 4); // UART_1DATA：4位数据位时可用
                    uart.get(5).setEnabled(serialsUart.getBits().getIndex() == 4); // UART_XDATA：4位数据位时可用
                    uart.get(6).setEnabled(serialsUart.getCheck().getIndex() != 0); // 校验选项：有校验时可用
                    for (int i = 0; i < uart.size(); i++) { // 遍历UART选项
                        if (uart.get(i).isSelected() && !uart.get(i).isEnabled()) { // 如果选中项被禁用
                            uart.get(0).setSelected(true); // 选中第一项（禁用）
                            uart.get(i).setSelected(false); // 取消当前选中
                            break; // 跳出循环
                        }
                    }
                    int i1; // 选中项索引
                    for (i1 = 0; i1 < uart.size(); i1++) { // 查找当前选中项
                        if (uart.get(i1).isSelected()) { // 找到选中项
                            break; // 跳出循环
                        }
                    }
                    triggerSerials.setList(uart, i1, rightMsgSerials.isFromEventBus()); // 更新串口Fragment列表
                    break; // 跳出UART分支
                case LIN: // LIN串口
                    int i2; // 选中项索引
                    for (i2 = 0; i2 < lin.size(); i2++) { // 查找当前选中项
                        if (lin.get(i2).isSelected()) { // 找到选中项
                            break; // 跳出循环
                        }
                    }
                    triggerSerials.setList(lin, i2, rightMsgSerials.isFromEventBus()); // 更新串口Fragment列表
                    break; // 跳出LIN分支
                case CAN: // CAN串口
                    int i3; // 选中项索引
                    for (i3 = 0; i3 < can.size(); i3++) { // 查找当前选中项
                        if (can.get(i3).isSelected()) { // 找到选中项
                            break; // 跳出循环
                        }
                    }
                    triggerSerials.setList(can, i3, rightMsgSerials.isFromEventBus()); // 更新串口Fragment列表
                    break; // 跳出CAN分支
                case SPI: // SPI串口
                    RightMsgSerialsSpi serialsSpi = (RightMsgSerialsSpi) rightMsgSerials.getSerialsDetails(); // 获取SPI详情
                    spi.get(0).setEnabled(serialsSpi.getCsSwitch().isValue()); // CS选项：CS开关开启时可用
                    if (spi.get(0).isSelected() && !spi.get(0).isEnabled()) { // 如果CS选中但被禁用
                        spi.get(0).setSelected(false); // 取消CS选中
                        spi.get(1).setSelected(true); // 选中DATA选项
                    }
                    int i4; // 选中项索引
                    for (i4 = 0; i4 < spi.size(); i4++) { // 查找当前选中项
                        if (spi.get(i4).isSelected()) { // 找到选中项
                            break; // 跳出循环
                        }
                    }
                    triggerSerials.setList(spi, i4, rightMsgSerials.isFromEventBus()); // 更新串口Fragment列表
                    break; // 跳出SPI分支
                case I2C: // I2C串口
                    int i5; // 选中项索引
                    for (i5 = 0; i5 < i2c.size(); i5++) { // 查找当前选中项
                        if (i2c.get(i5).isSelected()) { // 找到选中项
                            break; // 跳出循环
                        }
                    }
                    triggerSerials.setList(i2c, i5, rightMsgSerials.isFromEventBus()); // 更新串口Fragment列表
                    break; // 跳出I2C分支
                case ARINC429: // ARINC429串口
                    RightMsgSerialsM429 serialsM429 = (RightMsgSerialsM429) rightMsgSerials.getSerialsDetails(); // 获取ARINC429详情
                    switch (serialsM429.getFormat().getIndex()) { // 根据格式索引设置选项可用性
                        case 0: // 格式0
                            arinc429.get(3).setEnabled(false); // SDI选项禁用
                            arinc429.get(5).setEnabled(false); // SSM选项禁用
                            arinc429.get(6).setEnabled(false); // LABEL+SDI选项禁用
                            arinc429.get(8).setEnabled(false); // LABEL+SSM选项禁用
                            break; // 跳出格式0分支
                        case 1: // 格式1
                            arinc429.get(3).setEnabled(false); // SDI选项禁用
                            arinc429.get(5).setEnabled(true); // SSM选项启用
                            arinc429.get(6).setEnabled(false); // LABEL+SDI选项禁用
                            arinc429.get(8).setEnabled(true); // LABEL+SSM选项启用
                            break; // 跳出格式1分支
                        default: // 其他格式
                            arinc429.get(3).setEnabled(true); // SDI选项启用
                            arinc429.get(5).setEnabled(true); // SSM选项启用
                            arinc429.get(6).setEnabled(true); // LABEL+SDI选项启用
                            arinc429.get(8).setEnabled(true); // LABEL+SSM选项启用
                            break; // 跳出默认分支
                    }
                    for (int i = 0; i < arinc429.size(); i++) { // 遍历ARINC429选项
                        if (arinc429.get(i).isSelected() && !arinc429.get(i).isEnabled()) { // 如果选中项被禁用
                            arinc429.get(0).setSelected(true); // 选中第一项（禁用）
                            arinc429.get(i).setSelected(false); // 取消当前选中
                            break; // 跳出循环
                        }
                    }
                    int i6; // 选中项索引
                    for (i6 = 0; i6 < arinc429.size(); i6++) { // 查找当前选中项
                        if (arinc429.get(i6).isSelected()) { // 找到选中项
                            break; // 跳出循环
                        }
                    }
                    triggerSerials.setList(arinc429, i6, rightMsgSerials.isFromEventBus()); // 更新串口Fragment列表
                    break; // 跳出ARINC429分支
                case M1553B: // M1553B串口
                    int i7; // 选中项索引
                    for (i7 = 0; i7 < m1553b.size(); i7++) { // 查找当前选中项
                        if (m1553b.get(i7).isSelected()) { // 找到选中项
                            break; // 跳出循环
                        }
                    }
                    triggerSerials.setList(m1553b, i7, rightMsgSerials.isFromEventBus()); // 更新串口Fragment列表
                    break; // 跳出M1553B分支
            }
        }
    };

    /**
     * 远程指令到UI事件消费者
     * 处理来自远程控制端的触发类型变更指令
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 指令到UI消费者
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收指令到UI事件
            switch (commandMsgToUI.getFlag()) { // 根据指令标志分发
                case CommandMsgToUI.FLAG_TRIGGER_TYPE: { // 触发类型变更指令
                    if (triggerTitle.getSelected().getIndex() != Integer.parseInt(commandMsgToUI.getParam())) { // 如果当前选中与指令不同
                        triggerTitle.setSelected(Integer.parseInt(commandMsgToUI.getParam())); // 设置标题栏选中项
                        onCheckChanged(triggerTitle.getSelected(), false); // 触发类型切换
                    }
                    break; // 跳出触发类型指令分支
                }

            }
        }
    };

    /**
     * 标题栏项点击监听器
     * 播放按键音效并标记触发选项已操作
     */
    private View.OnClickListener onItemClickListener = new View.OnClickListener() { // 标题栏项点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            CacheUtil.get().putMap(CacheUtil.SAVE_TEMP_TRIGGER_IS_OPTION,String.valueOf(true)); // 标记触发选项已操作
        }
    };

    /**
     * 设置触发类型索引（外部调用）
     * @param triggerIdx 触发类型索引
     */
    public void setTriggerIdx(int triggerIdx){ // 设置触发类型索引
        triggerTitle.setSelected(triggerIdx); // 设置标题栏选中项
        onCheckChanged(triggerTitle.getSelected(), false); // 触发类型切换
    }

    /**
     * 获取当前触发类型索引
     * @return 当前选中的触发类型索引
     */
    public int getTriggerIdx(){ // 获取触发类型索引
        return triggerTitle.getSelected().getIndex(); // 返回当前选中项索引
    }

    /**
     * 触发类型切换核心方法
     * 隐藏所有子Fragment，根据选中索引显示对应的子Fragment，
     * 发送触发类型指令到硬件，更新缓存和触发器工厂
     * @param item 选中的标题项
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(TopAllBeanTitle item, boolean isFromEventBus) { // 触发类型切换核心方法

        Command.get().getTrigger().Type(item.getIndex(), false); // 发送触发类型指令到硬件
        if (item.getIndex() != DETAIL_COMMON) { // 如果不是常规触发
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER, String.valueOf(item.getIndex())); // 缓存当前触发类型
        }
        if (!isFromEventBus) { // 如果不是来自EventBus
            TriggerFactory.getInstance().setTriggerType(TopMatchTrigger.triggerTypeViewToScope(item.getIndex())); // 设置触发器工厂的触发类型（UI索引转示波器索引）
        }
        getChildFragmentManager().beginTransaction() // 开启Fragment事务
                .hide(triggerCommonLayout) // 隐藏常规Fragment
                .hide(triggerEdgeLayout) // 隐藏边沿Fragment
                .hide(triggerPulsewidthLayout) // 隐藏脉宽Fragment
                .hide(triggerLogicLayout) // 隐藏逻辑Fragment
                .hide(triggerNEdgeLayout) // 隐藏N边沿Fragment
                .hide(triggerRuntLayout) // 隐藏欠幅Fragment
                .hide(triggerSlopeLayout) // 隐藏斜率Fragment
                .hide(triggerTimeoutLayout) // 隐藏超时Fragment
                .hide(triggerVideoLayout) // 隐藏视频Fragment
                .hide(triggerS1Layout) // 隐藏S1串口Fragment
                .hide(triggerS2Layout) // 隐藏S2串口Fragment
                .hide(triggerS3Layout) // 隐藏S3串口Fragment
                .hide(triggerS4Layout) // 隐藏S4串口Fragment
                .commitAllowingStateLoss(); // 提交事务（允许状态丢失）
        switch (item.getIndex()) { // 根据选中索引显示对应Fragment
            case DETAIL_COMMON:             //常规
                getChildFragmentManager().beginTransaction() // 开启Fragment事务
                        .show(triggerCommonLayout).commitAllowingStateLoss(); // 显示常规Fragment
                msgTrigger.setTriggerTitle(item); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerCommonLayout.getTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
                break; // 跳出常规分支
            case DETAIL_EDGE:             //边沿
                getChildFragmentManager().beginTransaction() // 开启Fragment事务
                        .show(triggerEdgeLayout).commitAllowingStateLoss(); // 显示边沿Fragment
                msgTrigger.setTriggerTitle(item); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerEdgeLayout.getMsgTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
                break; // 跳出边沿分支
            case DETAIL_PULSEWIDTH:             //脉宽
                getChildFragmentManager().beginTransaction() // 开启Fragment事务
                        .show(triggerPulsewidthLayout).commitAllowingStateLoss(); // 显示脉宽Fragment
                msgTrigger.setTriggerTitle(item); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerPulsewidthLayout.getMsgTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
                break; // 跳出脉宽分支
            case DETAIL_LOGIC:             //逻辑
                getChildFragmentManager().beginTransaction() // 开启Fragment事务
                        .show(triggerLogicLayout).commitAllowingStateLoss(); // 显示逻辑Fragment
                msgTrigger.setTriggerTitle(item); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerLogicLayout.getTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
                break; // 跳出逻辑分支
            case DETAIL_NEDGE:             //N边沿
                getChildFragmentManager().beginTransaction() // 开启Fragment事务
                        .show(triggerNEdgeLayout).commitAllowingStateLoss(); // 显示N边沿Fragment
                msgTrigger.setTriggerTitle(item); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerNEdgeLayout.getMsgTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
                break; // 跳出N边沿分支
            case DETAIL_RUNT:             //欠幅
                getChildFragmentManager().beginTransaction() // 开启Fragment事务
                        .show(triggerRuntLayout).commitAllowingStateLoss(); // 显示欠幅Fragment
                msgTrigger.setTriggerTitle(item); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerRuntLayout.getMsgTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
                break; // 跳出欠幅分支
            case DETAIL_SLOPE:             //斜率
                getChildFragmentManager().beginTransaction() // 开启Fragment事务
                        .show(triggerSlopeLayout).commitAllowingStateLoss(); // 显示斜率Fragment
                msgTrigger.setTriggerTitle(item); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerSlopeLayout.getMsgTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
                break; // 跳出斜率分支
            case DETAIL_TIMEOUT:             //超时
                getChildFragmentManager().beginTransaction() // 开启Fragment事务
                        .show(triggerTimeoutLayout).commitAllowingStateLoss(); // 显示超时Fragment
                msgTrigger.setTriggerTitle(item); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerTimeoutLayout.getMsgTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
                break; // 跳出超时分支
            case DETAIL_VIDEO:             //视频
                getChildFragmentManager().beginTransaction() // 开启Fragment事务
                        .show(triggerVideoLayout).commitAllowingStateLoss(); // 显示视频Fragment
                msgTrigger.setTriggerTitle(item); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerVideoLayout.getMsgTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
                break; // 跳出视频分支
            case DETAIL_S1:             //S1 Serials
                getChildFragmentManager().beginTransaction() // 开启Fragment事务
                        .show(triggerS1Layout).commitAllowingStateLoss(); // 显示S1串口Fragment
                msgTrigger.setTriggerTitle(item); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerS1Layout.getTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
                break; // 跳出S1分支
            case DETAIL_S2:            //S2 Serials
                getChildFragmentManager().beginTransaction() // 开启Fragment事务
                        .show(triggerS2Layout).commitAllowingStateLoss(); // 显示S2串口Fragment
                msgTrigger.setTriggerTitle(item); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerS2Layout.getTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
                break; // 跳出S2分支
            case DETAIL_S3: // S3串口
                getChildFragmentManager().beginTransaction() // 开启Fragment事务
                        .show(triggerS3Layout).commitAllowingStateLoss(); // 显示S3串口Fragment
                msgTrigger.setTriggerTitle(item); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerS3Layout.getTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
                break; // 跳出S3分支
            case DETAIL_S4: // S4串口
                getChildFragmentManager().beginTransaction() // 开启Fragment事务
                        .show(triggerS4Layout).commitAllowingStateLoss(); // 显示S4串口Fragment
                msgTrigger.setTriggerTitle(item); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerS4Layout.getTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
                break; // 跳出S4分支
        }
    }

    /**
     * 标题栏选中变更监听器
     * 用户在标题栏切换触发类型时触发
     */
    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() { // 标题栏选中变更监听器
        @Override
        public void checkChanged(View view, TopAllBeanTitle item) { // 选中变更回调
            onCheckChanged(item, false); // 触发类型切换（非EventBus来源）
        }
    };

    /**
     * 子Fragment详情消息发送监听器
     * 当子Fragment中的参数发生变化时，将消息转发到主界面
     * 根据当前选中的触发类型和消息来源Fragment，组装正确的触发消息
     */
    private OnDetailSendMsgListener onDetailSendMsgListener = new OnDetailSendMsgListener() { // 详情消息发送监听器
        @Override
        public void onClick(Fragment fragment, boolean isFromEventBus) { // 点击回调
            if (fragment.equals(triggerCommonLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON) { // 常规Fragment且当前选中常规
                msgTrigger.setTriggerTitle(triggerTitle.getSelected()); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerCommonLayout.getTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.getTriggerTitle().setRxMsgSelect(false); // 清除Rx消息选中标记
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
            } else if ((fragment.equals(triggerEdgeLayout) && triggerTitle.getSelected().getIndex() == DETAIL_EDGE) // 边沿Fragment且当前选中边沿
                    || (fragment.equals(triggerEdgeLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON)) { // 或边沿Fragment且当前选中常规
                msgTrigger.setTriggerTitle(triggerTitle.getItem(DETAIL_EDGE)); // 设置消息中的触发标题为边沿
                msgTrigger.setTriggerDetail(triggerEdgeLayout.getMsgTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.getTriggerTitle().setRxMsgSelect(false); // 清除Rx消息选中标记
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
            } else if ((fragment.equals(triggerPulsewidthLayout) && triggerTitle.getSelected().getIndex() == DETAIL_PULSEWIDTH) // 脉宽Fragment且当前选中脉宽
                    || (fragment.equals(triggerPulsewidthLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON)) { // 或脉宽Fragment且当前选中常规
                msgTrigger.setTriggerTitle(triggerTitle.getItem(DETAIL_PULSEWIDTH)); // 设置消息中的触发标题为脉宽
                msgTrigger.setTriggerDetail(triggerPulsewidthLayout.getMsgTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.getTriggerTitle().setRxMsgSelect(false); // 清除Rx消息选中标记
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
            } else if ((fragment.equals(triggerLogicLayout) && triggerTitle.getSelected().getIndex() == DETAIL_LOGIC) // 逻辑Fragment且当前选中逻辑
                    || (fragment.equals(triggerLogicLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON)) { // 或逻辑Fragment且当前选中常规
                msgTrigger.setTriggerTitle(triggerTitle.getItem(DETAIL_LOGIC)); // 设置消息中的触发标题为逻辑
                msgTrigger.setTriggerDetail(triggerLogicLayout.getTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.getTriggerTitle().setRxMsgSelect(false); // 清除Rx消息选中标记
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
            } else if ((fragment.equals(triggerNEdgeLayout) && triggerTitle.getSelected().getIndex() == DETAIL_NEDGE) // N边沿Fragment且当前选中N边沿
                    || (fragment.equals(triggerNEdgeLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON)) { // 或N边沿Fragment且当前选中常规
                msgTrigger.setTriggerTitle(triggerTitle.getItem(DETAIL_NEDGE)); // 设置消息中的触发标题为N边沿
                msgTrigger.setTriggerDetail(triggerNEdgeLayout.getMsgTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.getTriggerTitle().setRxMsgSelect(false); // 清除Rx消息选中标记
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
            } else if ((fragment.equals(triggerRuntLayout) && triggerTitle.getSelected().getIndex() == DETAIL_RUNT) // 欠幅Fragment且当前选中欠幅
                    || (fragment.equals(triggerRuntLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON)) { // 或欠幅Fragment且当前选中常规
                msgTrigger.setTriggerTitle(triggerTitle.getItem(DETAIL_RUNT)); // 设置消息中的触发标题为欠幅
                msgTrigger.setTriggerDetail(triggerRuntLayout.getMsgTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.getTriggerTitle().setRxMsgSelect(false); // 清除Rx消息选中标记
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
            } else if ((fragment.equals(triggerSlopeLayout) && triggerTitle.getSelected().getIndex() == DETAIL_SLOPE) // 斜率Fragment且当前选中斜率
                    || (fragment.equals(triggerSlopeLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON)) { // 或斜率Fragment且当前选中常规
                msgTrigger.setTriggerTitle(triggerTitle.getItem(DETAIL_SLOPE)); // 设置消息中的触发标题为斜率
                msgTrigger.setTriggerDetail(triggerSlopeLayout.getMsgTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.getTriggerTitle().setRxMsgSelect(false); // 清除Rx消息选中标记
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
            } else if ((fragment.equals(triggerTimeoutLayout) && triggerTitle.getSelected().getIndex() == DETAIL_TIMEOUT) // 超时Fragment且当前选中超时
                    || (fragment.equals(triggerTimeoutLayout) && triggerTitle.getSelected().getIndex() == DETAIL_COMMON)) { // 或超时Fragment且当前选中常规
                msgTrigger.setTriggerTitle(triggerTitle.getItem(DETAIL_TIMEOUT)); // 设置消息中的触发标题为超时
                msgTrigger.setTriggerDetail(triggerTimeoutLayout.getMsgTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.getTriggerTitle().setRxMsgSelect(false); // 清除Rx消息选中标记
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
            } else if (fragment.equals(triggerVideoLayout) && triggerTitle.getSelected().getIndex() == DETAIL_VIDEO) { // 视频Fragment且当前选中视频
                msgTrigger.setTriggerTitle(triggerTitle.getSelected()); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerVideoLayout.getMsgTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.getTriggerTitle().setRxMsgSelect(false); // 清除Rx消息选中标记
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
            } else if (fragment.equals(triggerS1Layout) && triggerTitle.getSelected().getIndex() == DETAIL_S1) { // S1串口Fragment且当前选中S1
                msgTrigger.setTriggerTitle(triggerTitle.getSelected()); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerS1Layout.getTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.getTriggerTitle().setRxMsgSelect(false); // 清除Rx消息选中标记
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
            } else if (fragment.equals(triggerS2Layout) && triggerTitle.getSelected().getIndex() == DETAIL_S2) { // S2串口Fragment且当前选中S2
                msgTrigger.setTriggerTitle(triggerTitle.getSelected()); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerS2Layout.getTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.getTriggerTitle().setRxMsgSelect(false); // 清除Rx消息选中标记
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
            } else if (fragment.equals(triggerS3Layout) && triggerTitle.getSelected().getIndex() == DETAIL_S3) { // S3串口Fragment且当前选中S3
                msgTrigger.setTriggerTitle(triggerTitle.getSelected()); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerS3Layout.getTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.getTriggerTitle().setRxMsgSelect(false); // 清除Rx消息选中标记
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
            } else if (fragment.equals(triggerS4Layout) && triggerTitle.getSelected().getIndex() == DETAIL_S4) { // S4串口Fragment且当前选中S4
                msgTrigger.setTriggerTitle(triggerTitle.getSelected()); // 设置消息中的触发标题
                msgTrigger.setTriggerDetail(triggerS4Layout.getTriggerDetail()); // 设置消息中的触发详情
                msgTrigger.getTriggerTitle().setRxMsgSelect(false); // 清除Rx消息选中标记
                msgTrigger.setFromEventBus(isFromEventBus); // 设置是否来自EventBus
                sendMsg(); // 发送触发消息
            }
        }
    };

    /**
     * 触发类型事件UI观察者
     * 当EventBus发布触发类型变更事件时，同步更新UI选中状态
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() { // 触发类型事件观察者
        @Override
        public void update(Object data) { // 事件更新回调
            if (((EventBase) data).getId() == EventFactory.EVENT_TRIGGER_TYPE) { // 如果是触发类型事件
                int triggerType = TopMatchTrigger.triggerTypeScopeToView(TriggerFactory.getTriggerType()); // 将示波器索引转为UI索引
                if (triggerTitle.getSelected().getIndex() != triggerType) { // 如果当前选中与事件不同
                    triggerTitle.moveOnlyScroll(0); // 先滚动到起始位置
                    triggerTitle.setSelected(triggerType); // 设置标题栏选中项
                    onCheckChanged(triggerTitle.getSelected(), true); // 触发类型切换（来自EventBus）
                }
            }
        }
    };


    /**
     * 外部触发状态同步消费者
     * 当收到外部触发状态同步消息时，如果当前不是边沿触发则切换到边沿触发
     */
    private Consumer<Boolean> consumerSyncExternalTriggerState = new Consumer<Boolean>() { // 外部触发状态同步消费者
        @Override
        public void accept(Boolean aBoolean) throws Throwable { // 接收外部触发状态
            if (aBoolean && getTriggerIdx() != DETAIL_EDGE) { // 如果启用外部触发且当前不是边沿触发
                setTriggerIdx(DETAIL_EDGE); // 切换到边沿触发
            }
        }
    };

}
