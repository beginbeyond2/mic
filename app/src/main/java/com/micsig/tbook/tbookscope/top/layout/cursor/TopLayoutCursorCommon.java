package com.micsig.tbook.tbookscope.top.layout.cursor; // 光标常规设置Fragment所在包

import android.content.Context; // 导入上下文类，用于获取Activity和资源
import android.os.Bundle; // 导入Bundle，用于保存和恢复Fragment状态
import android.view.LayoutInflater; // 导入布局填充器，用于加载XML布局
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.RadioButton; // 导入单选按钮，用于通道选择回调

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.base.Logger; // 导入日志工具，用于调试输出
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂，用于获取通道激活状态
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件类，用于RxBus通知
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组，用于可见性变更通知
import com.micsig.tbook.tbookscope.R; // 导入资源ID常量类
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels; // 导入右侧通道消息，用于通道开关状态变更
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers; // 导入右侧其他消息，用于Math/Ref通道状态变更
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight; // 导入Ref通道消息，用于Ref通道开关变更
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入Rx事件总线，用于组件间通信
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx事件枚举常量
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效工具，用于按键音反馈
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情发送消息监听接口
import com.micsig.tbook.tbookscope.top.layout.measure.IMeasureDetail; // 导入测量详情接口
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具，用于读写SharedPreferences
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 导入光标管理器，用于设置光标通道颜色
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage; // 导入测量管理器，用于设置测量通道颜色
import com.micsig.tbook.ui.MMainMenuChannel; // 导入主菜单通道视图组件
import com.micsig.tbook.ui.top.view.channel.TopViewChannel; // 导入通道选择视图组件
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道工具类，用于通道号转换

import io.reactivex.rxjava3.annotations.NonNull; // 导入RxJava非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/**
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │                  TopLayoutCursorCommon                              │
 * │                光标常规设置Fragment                                  │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：top.layout.cursor → 光标常规子页面                        │
 * │ 核心职责：选择光标测量源通道（Ch1~Ch8/Math1~8/Ref1~8/默认）         │
 * │ 架构设计：Fragment子页面，通过通道选择器切换光标测量源               │
 * │ 数据流向：通道选择→CacheUtil持久化→CursorManage/MeasureManage同步   │
 * │ 依赖关系：TopViewChannel(通道选择器)、CursorManage(光标管理)、       │
 * │           MeasureManage(测量管理)、ChannelFactory(通道工厂)          │
 * │ 使用场景：Micsig示波器光标测量模式下，选择光标关联的信号源通道        │
 * └─────────────────────────────────────────────────────────────────────┘
 * @auother Liwb
 * @description:
 * @data:2023-9-6 9:01
 */
public class TopLayoutCursorCommon extends Fragment { // 光标常规设置Fragment

    private static final String TAG = "TopLayoutCursorCommon"; // 日志标签
    private Context context; // Activity上下文引用
    private MMainMenuChannel viewChannel; // 通道选择视图组件

    //Ch1--Ch8
    //Math1--Math8
    //R1--R8
    //default
    private boolean[] channelShow = { // 通道可见性数组，25个元素对应Ch1~8/Math1~8/Ref1~8/默认
            false, false, false, false, false, false, false, false, // Ch1~Ch8，默认全部不可见
            false, false, false, false, false, false, false, false, // Math1~Math8，默认全部不可见
            false, false, false, false, false, false, false, false, // Ref1~Ref8，默认全部不可见
            true // 默认通道（索引24），默认可见
    };

    /**
     * 创建Fragment视图，加载光标常规设置布局
     * @param inflater 布局填充器
     * @param container 父视图组
     * @param savedInstanceState 保存的实例状态
     * @return 光标常规设置根视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建Fragment视图回调
        return inflater.inflate(R.layout.layout_cursor_common, container, false); // 加载光标常规设置布局
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
        initChannelView(view); // 初始化通道选择视图
    } // initView方法结束

    /**
     * 初始化通道选择视图，设置数据源和监听器
     * @param view Fragment根视图
     */
    private void initChannelView(View view){ // 初始化通道选择视图方法
        viewChannel =  view.findViewById(R.id.chanCursorCommon); // 查找通道选择视图组件
        viewChannel.setData(R.array.popCursorArrayChannel, R.array.popCursorArrayChannelColor); // 设置通道名称和颜色数据源
        viewChannel.setChangeListener(onChannelItemClickListener, null); // 设置通道选择变更监听器
    } // initChannelView方法结束

    /**
     * 初始化RxBus事件监听，订阅通道变更和缓存加载事件
     */
    private void initControls(){ // 初始化控制监听方法
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels); // 订阅右侧通道开关变更事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers); // 订阅右侧Math/Ref通道变更事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_REF).subscribe(consumerRightRef); // 订阅Ref通道开关变更事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor); // 订阅通道颜色选择事件
    } // initControls方法结束


    /**
     * 缓存加载事件消费者，恢复通道选中状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载事件消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 恢复缓存状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutMeasureCommon, true); // 标记光标常规菜单缓存已加载
        } // accept方法结束
    }; // consumerLoadCache定义结束

    /**
     * 从缓存恢复通道选中状态并设置光标源
     */
    private void setCache(){ // 从缓存恢复状态方法
        int sourceIdx = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE); // 读取缓存中的通道源索引
        viewChannel.setChecked(sourceIdx); // 设置通道选择器选中项
        setSource(sourceIdx); // 设置光标源通道
    } // setCache方法结束

    /**
     * 设置光标测量源通道，更新光标和测量的通道颜色
     * @param sourceIndex 通道源索引（0~23为Ch/Math/Ref，24为默认/当前激活通道）
     */
    private void setSource(int sourceIndex){ // 设置光标源通道方法
        int ch = sourceIndex; // 初始化通道号为源索引
        if (sourceIndex == 24) { // 如果选择的是"默认"（索引24）
            ch = ChannelFactory.getChActivate(); // 获取当前激活的通道号
            if (!ChannelFactory.isChOpen(ch)) { // 如果激活通道未打开
                ch = -1; // 设置为无效通道号
            } // 通道打开判断结束
        } // 默认通道判断结束
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE, String.valueOf(sourceIndex)); // 持久化通道源索引到缓存
        CursorManage.getInstance().setCursorChannelColor(TChan.toUiChNo(ch)); // 设置光标管理器的通道颜色
        MeasureManage.getInstance().setCursorChannelColor(TChan.toUiChNo(ch)); // 设置测量管理器的通道颜色
    } // setSource方法结束

    /**
     * 通道选择变更监听器，切换光标测量源通道
     */
    private TopViewChannel.onItemClickListener onChannelItemClickListener = new TopViewChannel.onItemClickListener() { // 通道选择变更监听器
        @Override
        public void checkChanged(int viewId, int checkedIndex, RadioButton radioButton) { // 通道选择变更回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE, String.valueOf(checkedIndex)); // 持久化选中通道索引到缓存
            setSource(checkedIndex); // 设置光标源通道
        } // checkChanged方法结束
    }; // onChannelItemClickListener定义结束

    /**
     * 右侧通道开关变更消费者，更新Ch1~Ch8的可见性
     */
    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() { // 右侧通道开关变更消费者
        @Override
        public void accept(MainRightMsgChannels msgChannels) throws Exception { // 接收通道开关变更消息
            TChan.foreachChan(chan -> { // 遍历所有物理通道
                boolean isOpen = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + chan); // 读取通道打开状态
                channelShow[TChan.toFpgaChNo(chan)] = msgChannels.getCh(TChan.toFpgaChNo(chan)).isValue(); // 根据消息更新通道可见性
            }); // 遍历结束
            setChannelShow(); // 刷新通道选择器显示
        } // accept方法结束
    }; // consumerMainRightChannels定义结束

    /**
     * 右侧Math/Ref通道变更消费者，更新Math1~8和Ref1~8的可见性
     */
    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() { // 右侧Math/Ref通道变更消费者
        @Override
        public void accept(MainRightMsgOthers msgOthers) throws Exception { // 接收Math/Ref变更消息
            TChan.foreachMath(chan -> { // 遍历所有Math通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan); // 读取Math通道选中状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan); // 读取Math通道是否由用户添加
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 选中且用户添加时可见
            }); // Math遍历结束
            TChan.foreachRef(chan -> { // 遍历所有Ref通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan); // 读取Ref通道选中状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan); // 读取Ref通道是否由用户添加
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 选中且用户添加时可见
            }); // Ref遍历结束
            setChannelShow(); // 刷新通道选择器显示
        } // accept方法结束
    }; // consumerMainRightOthers定义结束

    /**
     * Ref通道开关变更消费者，更新指定Ref通道的可见性
     */
    private Consumer<RightMsgRefForEight> consumerRightRef = new Consumer<RightMsgRefForEight>() { // Ref通道开关变更消费者
        @Override
        public void accept(RightMsgRefForEight msgRef) throws Exception { // 接收Ref通道变更消息
            //哪个通道变化 设置哪个通道
            boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + msgRef.getRefChannelNumber()); // 读取Ref通道是否由用户添加
            channelShow[TChan.toFpgaChNo(msgRef.getRefChannelNumber())] = msgRef.getRefChecked().isValue() && isAddByUser; // 选中且用户添加时可见
            setChannelShow(); // 刷新通道选择器显示
        } // accept方法结束
    }; // consumerRightRef定义结束

    /**
     * 刷新通道选择器的可见性，并重新设置光标源通道
     */
    private void setChannelShow() { // 刷新通道可见性方法
        viewChannel.setItemVisible(channelShow,false); // 设置通道选择器各项的可见性（不触发监听）

        setSource(viewChannel.getSelectedIndex()); // 重新设置当前选中通道为光标源
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPMEASURE_CHANNEL); // 通知主界面通道可见性已变更
    } // setChannelShow方法结束


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


    /**
     * 通道颜色选择事件消费者，更新指定通道的颜色显示
     */
    private Consumer<String> consumerSelectColor = new Consumer<String>() { // 通道颜色选择事件消费者
        @Override
        public void accept(String colorInfo) throws Throwable { // 接收颜色选择消息
            if (colorInfo.isEmpty()) return; // 颜色信息为空则直接返回
            Logger.i(TAG, "selectColorInfo= " + colorInfo); // 打印颜色信息日志
            String[] info = colorInfo.split(";"); // 按";"分割颜色信息（格式：通道索引;颜色值）
            int chIndex = Integer.parseInt(info[0]); // 解析通道索引
            String colorStr = info[1]; // 获取颜色值字符串
            viewChannel.setChannelColor(chIndex, colorStr); // 设置通道选择器中指定通道的颜色
        } // accept方法结束
    }; // consumerSelectColor定义结束
} // TopLayoutCursorCommon类结束
