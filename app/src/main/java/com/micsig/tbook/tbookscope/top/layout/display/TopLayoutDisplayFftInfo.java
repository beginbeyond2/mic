package com.micsig.tbook.tbookscope.top.layout.display; // 显示模块布局包

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle类，用于保存和恢复Fragment状态
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件类
import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入按键音效工具
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息发送监听器
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.MSwitchBox; // 导入自定义开关控件
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组控件
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: Display布局模块 - FFT Info子页面Fragment                         ║
 * ║  核心职责: 管理Display-FFT Info页面的UI交互、命令下发和状态同步               ║
 * ║  架构设计: 子Fragment，通过OnDetailSendMsgListener向父Fragment传递数据       ║
 * ║  数据流向: UI控件 → onCheckChanged → CacheUtil → sendMsg → 父Fragment    ║
 * ║  依赖关系: RxBus、CacheUtil、TopMsgDisplayFftInfo                         ║
 * ║  使用场景: 用户在Display-FFT Info页面修改FFT信息显示索引时携带数据            ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by limh on 2024/8/6.
 */

public class TopLayoutDisplayFftInfo extends Fragment { // FFT Info显示子页面Fragment，管理FFT信息显示索引配置

    private Context context; // Fragment关联的Activity上下文
    private TopViewRadioGroup rgFftInfo; // FFT信息显示索引单选组
//    private MSwitchBox switchBox; // FFT信息显示开关（已隐藏）
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息发送监听器
    private TopMsgDisplayFftInfo displayDetail; // FFT Info显示详情数据模型
    private ViewGroup rootView; // 根视图组引用

    /**
     * 创建Fragment视图，加载布局文件
     * @param inflater 布局填充器
     * @param container 父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的视图
     */
    @Nullable // 标记返回值可为空
    @Override // 重写onCreateView方法
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建Fragment视图
        return inflater.inflate(R.layout.layout_displayfftinfo, container, false); // 加载FFT Info布局文件
    }

    /**
     * 视图创建完成后的初始化操作
     * @param view Fragment的根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override // 重写onViewCreated方法
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取关联的Activity上下文
        initView(view); // 初始化视图
        initData(); // 初始化数据
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化视图组件，绑定FFT信息单选组和监听器
     * @param view Fragment的根视图
     */
    private void initView(View view) { // 初始化视图
        rootView = (ViewGroup) view; // 保存根视图引用
        rgFftInfo = (TopViewRadioGroup) view.findViewById(R.id.displayFftInfo); // 获取FFT信息单选组
        String[] fftInfoPre = getResources().getStringArray(R.array.frequencymeter); // 获取频率计选项数组
        String[] fftInfo = getResources().getStringArray(R.array.topLayoutDisplayFftInfo); // 获取FFT信息选项数组
        String[] fftInfoArray = StrUtil.add(fftInfoPre, fftInfo); // 合并两个数组
        rgFftInfo.setData(getString(R.string.topLayoutDisplayFftInfo), fftInfoArray, onCheckChangedListener); // 设置单选组数据和监听器

//        switchBox = (MSwitchBox) view.findViewById(R.id.switchFftInfo);
//        switchBox.setOnToggleStateChangedListener(onToggleStateChangedListener);
    }

    /**
     * 初始化数据，从UI控件读取当前值并填充到详情数据模型
     */
    private void initData() { // 初始化数据
        displayDetail = new TopMsgDisplayFftInfo(); // 创建FFT Info详情对象
        displayDetail.setFftInfoIndex(rgFftInfo.getSelected().getIndex()); // 设置FFT信息索引
//        displayDetail.setShowFftInfo(switchBox.isState());
    }

    /**
     * 初始化事件控制，订阅缓存加载事件
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件

    }

    /**
     * 设置详情消息发送监听器
     * @param onDetailSendMsgListener 监听器实例
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置消息发送监听器
        this.onDetailSendMsgListener = onDetailSendMsgListener; // 赋值监听器
    }

    /**
     * 获取FFT Info显示详情数据
     * @return FFT Info显示详情数据模型
     */
    public TopMsgDisplayFftInfo getDisplayDetail() { // 获取详情数据
        return displayDetail; // 返回详情数据
    }

    /**
     * 从缓存恢复FFT Info页面的配置状态
     */
    private void setCache() { // 设置缓存
        int index = CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_FFT_INFO_DISPLAY); // 获取缓存的FFT信息索引
        boolean showFftInfo = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH_FFT_INFO_DISPLAY_SWITCH); // 获取缓存的FFT信息显示开关
        rgFftInfo.setSelectedIndex(index); // 设置FFT信息选中项
//        switchBox.setState(showFftInfo);
//        rgFftInfo.setEnabled(showFftInfo);

        displayDetail.setFftInfoIndex(rgFftInfo.getSelected().getIndex()); // 更新详情数据-FFT信息索引
//        displayDetail.setShowFftInfo(switchBox.isState());

        sendMsg(false); // 发送消息
    }

    /**
     * 通过监听器发送消息到父Fragment
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void sendMsg(boolean isFromEventBus) { // 发送消息
        Tools.PrintControlsLocation("TopLayoutDisplayFftInfo", rootView); // 打印控件位置调试信息
        if (onDetailSendMsgListener != null) { // 监听器不为空
            onDetailSendMsgListener.onClick(this, isFromEventBus); // 调用监听器回调
        }
    }


    /**
     * 缓存加载事件消费者，恢复缓存状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override // 重写accept方法
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 处理缓存加载事件
            setCache(); // 恢复缓存状态
        }
    };


    /**
     * FFT信息单选组选中变化监听器，播放音效并更新缓存和详情数据
     */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选组变化监听器
        @Override // 重写onClickSound方法
        public void onClickSound(boolean isCheckedSuccess) { // 点击音效回调
            PlaySound.getInstance().playButton(); // 播放按键音效
        }

        @Override // 重写onPrompt方法
        public void onPrompt(TopViewRadioGroup view) { // 提示回调

        }

        @Override // 重写onClick方法
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 点击回调
            int index = CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_FFT_INFO_DISPLAY); // 获取当前缓存的FFT信息索引
            if (item.getIndex() == index) return; // 索引未变化则跳过
            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH_FFT_INFO_DISPLAY, String.valueOf(item.getIndex())); // 缓存新的FFT信息索引
            displayDetail.setFftInfoIndex(item.getIndex()); // 更新详情数据
            sendMsg(false); // 发送消息
        }
    };

//    //switch box hide
//    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
//        @Override
//        public void onToggleStateChanged(MSwitchBox view, boolean state) {
//            PlaySound.getInstance().playButton();
//            rgFftInfo.setEnabled(state);
//            CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH_FFT_INFO_DISPLAY_SWITCH, String.valueOf(state));
//            displayDetail.setShowFftInfo(state);
//            sendMsg(false);
//        }
//    };
}
