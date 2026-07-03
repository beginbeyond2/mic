package com.micsig.tbook.tbookscope.top.layout.cursor; // 光标模块主容器Fragment所在包

import android.content.Context; // 导入上下文类，用于获取Activity和资源
import android.os.Bundle; // 导入Bundle，用于保存和恢复Fragment状态
import android.view.LayoutInflater; // 导入布局填充器，用于加载XML布局
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件类，用于RxBus通知
import com.micsig.tbook.tbookscope.R; // 导入资源ID常量类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入Rx事件总线，用于组件间通信
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx事件枚举常量
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效工具，用于按键音反馈
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情发送消息监听接口
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具，用于读写SharedPreferences
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle; // 导入顶部标题数据Bean
import com.micsig.tbook.ui.top.view.title.TopViewTitle; // 导入顶部标题视图，用于标题切换监听
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll; // 导入可滚动顶部标题视图

import io.reactivex.rxjava3.annotations.NonNull; // 导入RxJava非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/**
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │                      TopLayoutCursor                                │
 * │                  光标模块主容器Fragment                              │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：top.layout.cursor → 光标功能的顶层容器页面                 │
 * │ 核心职责：管理光标子页面（常规/设置）的切换和消息转发                │
 * │ 架构设计：Fragment容器模式，通过标题栏切换子Fragment                 │
 * │ 数据流向：子Fragment → TopMsgCursor → RxBus → 主界面光标管理器      │
 * │ 依赖关系：TopLayoutCursorCommon(常规)、TopLayoutCursorSetting(设置)  │
 * │           TopMsgCursor(消息)、RxBus(事件总线)                        │
 * │ 使用场景：Micsig示波器光标测量模式下，切换光标类型和参数设置          │
 * └─────────────────────────────────────────────────────────────────────┘
 * @auother Liwb
 * @description:
 * @data:2023-9-5 18:20
 */
public class TopLayoutCursor extends Fragment { // 光标模块主容器Fragment
    public static final int DETAIL_COMMON = 0; // 常规子页面索引常量
    public static final int DETAIL_SETTING= 1; // 设置子页面索引常量
    private Context context; // Activity上下文引用
    private TopViewTitleWithScroll measureTitle; // 可滚动标题栏视图，用于切换常规/设置
    private TopLayoutCursorCommon cursorCommonLayout;             //常规 // 常规光标子Fragment
    private TopLayoutCursorSetting cursorSettingLayout;           //设置 // 设置光标子Fragment
    private String[] tags = {"CommonLayout","SettingLayout"}; // 子Fragment标签数组，用于Fragment事务查找
    private Fragment[] fragments = new Fragment[2]; // 子Fragment数组，保存常规和设置页面引用
    private TopMsgCursor msgCursor; // 光标消息数据对象，用于RxBus传递

    /**
     * 创建Fragment视图，加载光标主容器布局
     * @param inflater 布局填充器
     * @param container 父视图组
     * @param savedInstanceState 保存的实例状态
     * @return 光标主容器根视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建Fragment视图回调
        return inflater.inflate(R.layout.layout_cursor, container, false); // 加载光标主容器布局
    } // onCreateView方法结束

    /**
     * 视图创建完成后的初始化
     * @param view Fragment根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        initView(view, savedInstanceState); // 初始化视图控件和子Fragment
        initControl(); // 初始化RxBus事件监听
    } // onViewCreated方法结束

    /**
     * 初始化视图控件、子Fragment和标题栏
     * @param view Fragment根视图
     * @param savedInstanceState 保存的实例状态
     */
    private void initView(View view, Bundle savedInstanceState) { // 初始化视图方法
        if (savedInstanceState != null) { // 如果有保存的状态（Fragment重建场景）
            for (int i = 0; i < tags.length; i++) { // 遍历所有Fragment标签
                fragments[i] = getChildFragmentManager().findFragmentByTag(tags[i]); // 通过标签查找已存在的子Fragment
            } // for循环结束
        } // 状态恢复结束

        cursorCommonLayout = fragments[0] == null ? new TopLayoutCursorCommon() : (TopLayoutCursorCommon) fragments[0]; // 常规子Fragment：不存在则新建，存在则复用
        cursorSettingLayout=fragments[1]==null? new TopLayoutCursorSetting():(TopLayoutCursorSetting) fragments[1]; // 设置子Fragment：不存在则新建，存在则复用

        if (savedInstanceState == null) { // 首次创建时添加子Fragment
            getChildFragmentManager().beginTransaction() // 开启子Fragment事务
                    .add(R.id.measureDetail, cursorCommonLayout, tags[0]) // 添加常规子Fragment
                    .add(R.id.measureDetail,cursorSettingLayout,tags[1]) // 添加设置子Fragment
                    .hide(cursorSettingLayout) // 默认隐藏设置子Fragment
                    .commitAllowingStateLoss(); // 提交事务（允许状态丢失）
        } // 首次添加结束


        measureTitle = (TopViewTitleWithScroll) view.findViewById(R.id.measureTitle); // 查找标题栏视图
        String[] array = context.getResources().getStringArray(R.array.cursor); // 获取光标标题文本数组
        boolean[] arrayVisible = new boolean[array.length]; // 创建标题项可见性数组
        for (int i = 0; i < array.length; i++) { // 遍历所有标题项
            if (i == 0 || i==1 || i==2 || i == 3) { // 前4项（常规/手动/追踪/XY）可见
                arrayVisible[i] = true; // 设置可见
            } else { // 其他项不可见
                arrayVisible[i] = false; // 设置不可见
            } // if-else结束
        } // for循环结束
        measureTitle.setData(array, arrayVisible, onCheckChangedTitleListener, onItemClickListener); // 设置标题栏数据、可见性、监听器

        cursorCommonLayout.setOnDetailSendMsgListener(onDetailSendMsgListener); // 设置常规子Fragment的消息发送监听


        msgCursor = new TopMsgCursor(); // 创建光标消息对象
        msgCursor.setCursorTitle(measureTitle.getSelected()); // 设置当前选中的标题项
        msgCursor.setCursorDetail(cursorCommonLayout.getCursorDetail()); // 设置常规子Fragment的详情数据
        msgCursor.setFromEventBus(false); // 初始化为非EventBus来源
    } // initView方法结束

    /**
     * 初始化RxBus事件监听
     */
    private void initControl() { // 初始化控制监听方法
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
    } // initControl方法结束

    /**
     * 从缓存恢复标题栏选中状态并触发切换
     */
    private void setCache() { // 从缓存恢复状态方法
        int index = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE); // 读取缓存中的标题选中索引
        measureTitle.setSelected(index); // 设置标题栏选中项
        onCheckChanged(measureTitle, measureTitle.getSelected(), false); // 触发标题切换逻辑
    } // setCache方法结束

    /**
     * 通过RxBus发送光标消息
     */
    private void sendMsg() { // 发送光标消息方法
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_CURSOR, msgCursor); // 通过RxBus发送光标消息
    } // sendMsg方法结束

    /**
     * 缓存加载事件消费者，恢复缓存状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载事件消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 恢复缓存状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutMeasure, true); // 标记光标菜单缓存已加载
        } // accept方法结束
    }; // consumerLoadCache定义结束

    /**
     * 标题栏点击监听器，播放按键音
     */
    private View.OnClickListener onItemClickListener = new View.OnClickListener() { // 标题栏点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
        } // onClick方法结束
    }; // onItemClickListener定义结束

    /**
     * 标题栏选中变更监听器，触发子页面切换
     */
    private TopViewTitle.OnCheckChangedTitleListener onCheckChangedTitleListener = new TopViewTitle.OnCheckChangedTitleListener() { // 标题选中变更监听器
        @Override
        public void checkChanged(View view, TopAllBeanTitle item) { // 选中变更回调
            onCheckChanged(view, item, false); // 委托给onCheckChanged处理，非EventBus来源
        } // checkChanged方法结束
    }; // onCheckChangedTitleListener定义结束

    /**
     * 子Fragment详情发送消息监听器，将子页面数据封装后通过RxBus转发
     */
    private OnDetailSendMsgListener onDetailSendMsgListener = new OnDetailSendMsgListener() { // 详情发送消息监听器
        @Override
        public void onClick(Fragment fragment, boolean isFromEventBus) { // 子Fragment点击回调
            if (msgCursor.getCursorTitle() == null) { // 如果消息中的标题为空
                msgCursor.setCursorTitle(measureTitle.getSelected()); // 设置当前选中的标题项
            } // 标题空判断结束
            if (fragment.equals(cursorCommonLayout)) { // 如果来自常规子Fragment
                msgCursor.setCursorDetail(cursorCommonLayout.getCursorDetail()); // 设置常规子Fragment的详情数据
                msgCursor.getCursorTitle().setRxMsgSelect(false); // 标记非Rx消息选中（保持标题不变）
                msgCursor.setFromEventBus(isFromEventBus); // 设置EventBus来源标志
                sendMsg(); // 发送光标消息
            }else if (fragment.equals(cursorSettingLayout)){ // 如果来自设置子Fragment
                msgCursor.setCursorDetail(cursorSettingLayout.getCursorDetail()); // 设置设置子Fragment的详情数据
                msgCursor.getCursorTitle().setRxMsgSelect(false); // 标记非Rx消息选中（保持标题不变）
                msgCursor.setFromEventBus(isFromEventBus); // 设置EventBus来源标志
                sendMsg(); // 发送光标消息
            } // Fragment判断结束
        } // onClick方法结束
    }; // onDetailSendMsgListener定义结束

    /**
     * 标题切换核心处理方法，切换子Fragment并发送消息
     * @param view 标题视图
     * @param item 选中的标题项
     * @param isFromEventBus 是否来自EventBus
     */
    private void onCheckChanged(View view, TopAllBeanTitle item, boolean isFromEventBus) { // 标题切换核心处理方法
        if (view.getId() == measureTitle.getId()) { // 确认是标题栏的变更事件
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE, String.valueOf(item.getIndex())); // 持久化当前选中索引到缓存
            getChildFragmentManager().beginTransaction() // 开启子Fragment事务
                    .hide(cursorCommonLayout) // 先隐藏常规子Fragment
                    .commitAllowingStateLoss(); // 提交事务
            switch (item.getIndex()) { // 根据选中索引切换
                case DETAIL_COMMON: // 选中常规页面
                    getChildFragmentManager().beginTransaction() // 开启子Fragment事务
                            .show(cursorCommonLayout) // 显示常规子Fragment
                            .hide(cursorSettingLayout) // 隐藏设置子Fragment
                            .commitAllowingStateLoss(); // 提交事务
                    msgCursor.setCursorTitle(item); // 设置消息标题为当前选中项
                    msgCursor.setCursorDetail(cursorCommonLayout.getCursorDetail()); // 设置常规子Fragment详情
                    msgCursor.setFromEventBus(isFromEventBus); // 设置EventBus来源标志
                    sendMsg(); // 发送光标消息
                    break; // 常规分支结束
                case DETAIL_SETTING: // 选中设置页面
                    getChildFragmentManager().beginTransaction() // 开启子Fragment事务
                            .hide(cursorCommonLayout) // 隐藏常规子Fragment
                            .show(cursorSettingLayout).commitAllowingStateLoss(); // 显示设置子Fragment并提交
                    msgCursor.setCursorTitle(item); // 设置消息标题为当前选中项
                    msgCursor.setCursorDetail(cursorSettingLayout.getCursorDetail()); // 设置设置子Fragment详情
                    msgCursor.setFromEventBus(isFromEventBus); // 设置EventBus来源标志
                    sendMsg(); // 发送光标消息
                    break; // 设置分支结束
            } // switch结束
        } // 标题栏判断结束
    } // onCheckChanged方法结束

    /**
     * 获取当前光标选中索引
     * @return 当前选中的标题项索引
     */
    public int getCursorIdx(){ // 获取当前光标选中索引方法
        return measureTitle.getSelected().getIndex(); // 返回标题栏当前选中项的索引
    } // getCursorIdx方法结束

} // TopLayoutCursor类结束
