package com.micsig.tbook.tbookscope.top.layout.cursor; // 光标设置Fragment所在包

import android.content.Context; // 导入上下文类，用于获取Activity和资源
import android.os.Bundle; // 导入Bundle，用于保存和恢复Fragment状态
import android.view.LayoutInflater; // 导入布局填充器，用于加载XML布局
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.TableLayout; // 导入表格布局（未使用，预留）
import android.widget.TableRow; // 导入表格行（未使用，预留）

import androidx.annotation.NonNull; // 导入非空注解
import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.base.Logger; // 导入日志工具，用于调试输出
import com.micsig.tbook.scope.measure.Measure; // 导入测量类（未使用，预留）
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件类，用于RxBus通知
import com.micsig.tbook.tbookscope.R; // 导入资源ID常量类
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令转UI消息类，用于远程指令同步
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入Rx事件总线，用于组件间通信
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx事件枚举常量
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情发送消息监听接口
import com.micsig.tbook.tbookscope.top.layout.measure.IMeasureDetail; // 导入测量详情接口
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具，用于读写SharedPreferences
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 导入光标管理器，用于设置光标追踪和标签跟随
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage; // 导入测量管理器，用于判断T值追踪状态
import com.micsig.tbook.ui.MSwitchBox; // 导入自定义开关组件

import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口

/**
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │                  TopLayoutCursorSetting                             │
 * │                  光标设置Fragment                                    │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：top.layout.cursor → 光标设置子页面                        │
 * │ 核心职责：配置光标追踪开关和光标标签跟随模式                         │
 * │ 架构设计：Fragment子页面，通过开关控件控制光标行为                    │
 * │ 数据流向：开关切换→CacheUtil持久化→CursorManage同步光标行为         │
 * │ 依赖关系：MSwitchBox(开关控件)、CursorManage(光标管理)、              │
 * │           MeasureManage(测量管理)、CommandMsgToUI(远程指令)           │
 * │ 使用场景：Micsig示波器光标模式下，开关光标追踪和标签跟随功能          │
 * └─────────────────────────────────────────────────────────────────────┘
 * @auother Liwb
 * @description:
 * @data:2025-7-14 16:23
 */
public class TopLayoutCursorSetting extends Fragment { // 光标设置Fragment
    private static String TAG = "TopLayoutCursorSetting"; // 日志标签
    private Context context; // Activity上下文引用
    private MSwitchBox radioTrace; // 光标追踪开关控件

    private MSwitchBox radioCursorMode; // 光标标签跟随模式开关控件

    /**
     * 创建Fragment视图，加载光标设置布局
     * @param inflater 布局填充器
     * @param container 父视图组
     * @param savedInstanceState 保存的实例状态
     * @return 光标设置根视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建Fragment视图回调
        return inflater.inflate(R.layout.layout_cursor_setting, container, false); // 加载光标设置布局
    } // onCreateView方法结束

    /**
     * 视图创建完成后的初始化
     * @param view Fragment根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        initView(view); // 初始化视图控件
        initControls(); // 初始化RxBus事件监听
    } // onViewCreated方法结束

    /**
     * 初始化视图控件
     * @param view Fragment根视图
     */
    private void initView(View view){ // 初始化视图方法
        initChannelView(view); // 初始化开关控件
    } // initView方法结束

    /**
     * 初始化开关控件，设置监听器
     * @param view Fragment根视图
     */
    private void initChannelView(View view){ // 初始化开关控件方法
        radioTrace=view.findViewById(R.id.radioCursorTrace); // 查找光标追踪开关控件
        radioCursorMode = view.findViewById(R.id.radioCursorMode); // 查找光标标签跟随模式开关控件
        radioTrace.setOnToggleStateChangedListener(this::onClickTrace); // 设置追踪开关状态变更监听
        radioCursorMode.setOnToggleStateChangedListener(this::onClickMode); // 设置标签跟随模式开关状态变更监听
    } // initChannelView方法结束




    /**
     * 初始化RxBus事件监听，订阅缓存加载、远程指令和T值追踪事件
     */
    private void initControls(){ // 初始化控制监听方法
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(this::CommandToUI); // 订阅远程指令转UI事件
        RxBus.getInstance().getObservable(RxEnum.MSG_TVALUE_ENABLE).subscribe(consumerTValue); // 订阅T值追踪使能事件
    } // initControls方法结束

    /**
     * 处理远程指令转UI消息，同步光标追踪开关状态
     * @param o 命令转UI消息对象
     */
    private void CommandToUI(Object o) { // 远程指令处理方法
        CommandMsgToUI commandMsgToUI=(CommandMsgToUI) o; // 强制转换为命令转UI消息
        switch (commandMsgToUI.getFlag()) { // 根据消息标志分发
            case CommandMsgToUI.FLAG_CURSOR_SETTING_TRACE: // 光标追踪设置指令
                boolean enable=Boolean.parseBoolean(commandMsgToUI.getParam()); // 解析参数为布尔值
                radioTrace.setState(enable); // 设置追踪开关状态
                onClickTrace(radioTrace,enable); // 触发追踪开关点击逻辑
                break; // 追踪分支结束
        } // switch结束
    } // CommandToUI方法结束

    /**
     * T值追踪使能事件消费者，T值追踪开启时禁用光标追踪开关
     */
    private Consumer<Boolean> consumerTValue = new Consumer<Boolean>() { // T值追踪使能事件消费者
        @Override
        public void accept(@NonNull Boolean update) throws Exception { // 接收T值追踪使能事件
            Logger.i(TAG, "isTVtrace= " + MeasureManage.getInstance().isCursorTValueTrace()); // 打印T值追踪状态日志
            radioTrace.setEnabled(!MeasureManage.getInstance().isCursorTValueTrace()); // T值追踪开启时禁用光标追踪开关
        } // accept方法结束
    }; // consumerTValue定义结束


    /**
     * 缓存加载事件消费者，恢复开关状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载事件消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 恢复缓存状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutMeasureCommon, true); // 标记光标设置菜单缓存已加载
        } // accept方法结束
    }; // consumerLoadCache定义结束

    /**
     * 从缓存恢复开关状态并同步到光标管理器
     */
    private void setCache(){ // 从缓存恢复状态方法
        boolean b=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_CURSOR_SETTING_TRANCE); // 读取缓存的追踪开关状态
        radioTrace.setState(b); // 设置追踪开关UI状态
        CursorManage.getInstance().setEnableCursorTrance(b); // 同步光标管理器的追踪使能
        b = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_CURSOR_SETTING_MODE); // 读取缓存的标签跟随模式开关状态
        radioCursorMode.setState(b); // 设置标签跟随模式开关UI状态
        CursorManage.getInstance().setLabelNotFollowCursor(b); // 同步光标管理器的标签跟随模式
    } // setCache方法结束

    /**
     * 光标追踪开关点击处理，持久化状态并同步光标管理器
     * @param mSwitchBox 开关控件
     * @param b 开关状态（true=开启追踪）
     */
    private void onClickTrace(MSwitchBox mSwitchBox, boolean b) { // 追踪开关点击处理方法
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_CURSOR_SETTING_TRANCE,String.valueOf(b)); // 持久化追踪开关状态到缓存
        CursorManage.getInstance().setEnableCursorTrance(b); // 设置光标管理器的追踪使能
    } // onClickTrace方法结束

    /**
     * 光标标签跟随模式开关点击处理，持久化状态并同步光标管理器
     * @param mSwitchBox 开关控件
     * @param b 开关状态（true=标签不跟随光标）
     */
    private void onClickMode(MSwitchBox mSwitchBox, boolean b) { // 标签跟随模式开关点击处理方法
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_CURSOR_SETTING_MODE,String.valueOf(b)); // 持久化标签跟随模式状态到缓存
        CursorManage.getInstance().setLabelNotFollowCursor(b); // 设置光标管理器的标签跟随模式
    } // onClickMode方法结束

    /**
     * 获取光标详情数据（本页面无详情，返回null）
     * @return null
     */
    public IMeasureDetail getCursorDetail() { // 获取光标详情数据方法
        return null; // 本页面无详情数据，返回null
    } // getCursorDetail方法结束

    /**
     * 设置详情发送消息监听器（本页面空实现）
     * @param onDetailSendMsgListener 监听器
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置详情发送消息监听器（空实现）

    } // 空实现
} // TopLayoutCursorSetting类结束
