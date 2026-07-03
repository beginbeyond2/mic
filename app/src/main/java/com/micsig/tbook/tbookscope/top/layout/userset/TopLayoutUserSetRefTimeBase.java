package com.micsig.tbook.tbookscope.top.layout.userset; // 用户设置模块包声明

import android.annotation.SuppressLint; // 导入SuppressLint注解
import android.content.ContentUris; // 导入ContentUris
import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle
import android.os.Handler; // 导入Handler
import android.util.Log; // 导入日志工具
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.RadioButton; // 导入单选按钮控件
import android.widget.TextView; // 导入文本视图控件

import androidx.annotation.NonNull; // 导入NonNull注解
import androidx.annotation.Nullable; // 导入Nullable注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.base.Logger; // 导入日志工具
import com.micsig.tbook.hardware.HardwareProduct; // 导入硬件产品型号判断
import com.micsig.tbook.scope.Data.WaveData; // 导入波形数据
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂
import com.micsig.tbook.scope.Sample.Sample; // 导入采样控制
import com.micsig.tbook.scope.Scope; // 导入示波器核心
import com.micsig.tbook.scope.ScopeBase; // 导入示波器基类
import com.micsig.tbook.scope.Trigger.TriggerCommon; // 导入触发通用
import com.micsig.tbook.scope.Trigger.TriggerFactory; // 导入触发工厂
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂
import com.micsig.tbook.scope.channel.RefChannel; // 导入参考通道
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 导入水平轴
import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity
import com.micsig.tbook.tbookscope.R; // 导入资源引用
import com.micsig.tbook.tbookscope.main.dialog.DialogOkCancel; // 导入确认取消对话框
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令消息到UI
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard; // 导入浮动键盘对话框
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase; // 导入触发时基
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组
import com.micsig.tbook.ui.util.TBookUtil; // 导入TBook工具

import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava Consumer

/*
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：用户设置 → 参考波形时基调节 → Fragment页面                        │
 * │ 核心职责：管理参考波形时基的跟随/独立模式切换                               │
 * │ 架构设计：Fragment子类，通过RxBus监听缓存加载事件                          │
 * │ 数据流向：CacheUtil → 本类UI → HorizontalAxis → 波形渲染                   │
 * │ 依赖关系：HorizontalAxis、CacheUtil、RxBus                                 │
 * │ 使用场景：用户设置界面中"参考波形时基调节"子页面                            │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 参考波形时基调节Fragment。
 * <p>
 * 管理参考波形时基模式：
 * <ul>
 *   <li>跟随 - 参考波形时基跟随主通道时基变化</li>
 *   <li>独立 - 参考波形时基独立设置</li>
 * </ul>
 *
 * @author limh
 * @since 2025-10-20
 */
public class TopLayoutUserSetRefTimeBase extends Fragment {

    /** 日志标签 */
    private static final String TAG = "TopLayoutUserSetRefTimeBase"; // 日志标签
    /** 上下文对象 */
    private Context context; // 上下文对象
    /** 根视图组 */
    private ViewGroup rootViewGroup; // 根视图组
    /** 时基模式单选组 */
    private TopViewRadioGroup groupTimeBase; // 时基模式单选组

    /**
     * 创建Fragment视图，加载布局文件。
     *
     * @param inflater           布局填充器
     * @param container          父视图组
     * @param savedInstanceState 保存的实例状态
     * @return 创建的视图
     */
    @Override // 覆写创建视图方法
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建Fragment视图
        return inflater.inflate(R.layout.layout_usersetreftimebase, container, false); // 加载参考时基布局
    }

    /**
     * 视图创建完成后的初始化。
     *
     * @param view               创建的视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override // 覆写视图创建完成方法
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        initView(view); // 初始化视图
        initControl(); // 初始化控制逻辑
    }

    /**
     * 初始化视图控件，绑定监听器。
     *
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图
        rootViewGroup = (ViewGroup) view; // 保存根视图组引用
        groupTimeBase = view.findViewById(R.id.ref_timebase); // 查找时基模式单选组
        groupTimeBase.setOnListener(onCheckChangedListener); // 设置选中变化监听
    }

    /**
     * 初始化控制逻辑，订阅RxBus事件。
     */
    private void initControl() { // 初始化控制逻辑
//        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
    }

    /** 单选组选中变化监听器 */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选组选中变化监听器
        @Override // 覆写点击回调
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 选中项变化回调
            onSelectChanged(view, item, false); // 处理选中变化
        }

        @Override // 覆写音效回调
        public void onClickSound(boolean isCheckedSuccess) { // 点击音效回调
            PlaySound.getInstance().playButton(); // 播放按钮音效
        }

        @Override // 覆写拦截提示回调
        public void onPrompt(TopViewRadioGroup view) { // 拦截提示回调（本页面无拦截项）
        }
    };


    /**
     * 从缓存恢复时基模式设置。
     * <p>
     * 延迟800ms执行，确保其他初始化完成后再设置。
     */
    private void setCache() { // 从缓存恢复时基设置
        groupTimeBase.postDelayed(()->{ // 延迟800ms执行
            int timeBaseIndex = CacheUtil.get().isRefTimebase() ? 0 : 1;//CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE); // 判断参考时基是否跟随模式
            groupTimeBase.setSelectedIndex(timeBaseIndex); // 设置选中项
            onCheckChangedListener.onClick(groupTimeBase, groupTimeBase.getSelected()); // 触发选中变化
        },800); // 延迟800毫秒
    }

    /**
     * 处理单选组选中变化，更新水平轴时基跟随模式。
     *
     * @param view          触发变化的单选组
     * @param item          选中的项
     * @param isFromEventBus 是否来自EventBus
     */
    @SuppressLint("NonConstantResourceId") // 抑制非常量资源ID警告
    private void onSelectChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) { // 处理选中变化
        Tools.PrintControlsLocation("TopLayoutUserSetRefTimeBase", rootViewGroup); // 打印控件位置日志
        switch (view.getId()) { // 根据视图ID分发
            case R.id.ref_timebase: // 参考时基变化
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE) == item.getIndex()) return; // 索引相同则跳过
                HorizontalAxis.getInstance().setScaleFollowingCh(item.getIndex() == 0); // 设置时基是否跟随通道，0=跟随
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE, String.valueOf(item.getIndex())); // 缓存时基模式
                RxBus.getInstance().post(RxEnum.MQ_MSG_USER_SET_TIMEBASE, item.getIndex()); // 发送时基设置事件
                break; // 结束参考时基分支
            default: // 默认分支
                break; // 无操作
        }
    }

    /** 缓存加载事件消费者 */
    private final Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载事件消费者
        @Override // 覆写accept方法
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 从缓存恢复设置
        }
    };

/*    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {//TODO 待SCPI指令接入
                default:
                    break;
            }
        }
    };*/

    /**
     * 线程休眠工具方法。
     *
     * @param ms 休眠毫秒数
     */
    void ms_sleep(long ms) { // 线程休眠
        try { // 尝试休眠
            Thread.sleep(ms); // 执行休眠
        } catch (InterruptedException e) { // 捕获中断异常
            e.printStackTrace(); // 打印异常堆栈
        }
    }

}
