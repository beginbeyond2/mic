package com.micsig.tbook.tbookscope.top.layout.display; // 显示模块布局包

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle类，用于保存和恢复Fragment状态
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.SeekBar; // 导入滑动条控件

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件类
import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令发送单例
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令消息转UI消息
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入按键音效工具
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息发送监听器
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 导入工作模式管理器
import com.micsig.tbook.tbookscope.wavezone.display.WaveGridManage; // 导入波形网格管理器
import com.micsig.tbook.ui.top.view.TopViewSeekBar; // 导入自定义滑动条视图
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组控件

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: Display布局模块 - 方格图(Graticule)子页面Fragment                ║
 * ║  核心职责: 管理Display-方格图页面的UI交互、命令下发和状态同步                  ║
 * ║  架构设计: 子Fragment，通过OnDetailSendMsgListener向父Fragment传递数据       ║
 * ║  数据流向: UI控件 → onCheckChanged/onProgressChanged → Command → 底层     ║
 * ║  依赖关系: Command、WaveGridManage、WorkModeManage、RxBus等               ║
 * ║  使用场景: 用户在Display-方格图页面修改显示模式或网格亮度时携带数据             ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by Administrator on 2017/4/6.
 */

public class TopLayoutDisplayGraticule extends Fragment { // 方格图显示子页面Fragment，管理方格图显示模式和网格亮度配置

    private Context context; // Fragment关联的Activity上下文
    private TopViewRadioGroup displayMode; // 方格图显示模式单选组
    private TopViewSeekBar displayIntensity; // 网格亮度滑动条

    private TopMsgDisplayGraticule displayDetail; // 方格图显示详情数据模型
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息发送监听器

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
        return inflater.inflate(R.layout.layout_displaygraticule, container, false); // 加载方格图布局文件
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
     * 初始化数据，从UI控件读取当前值并填充到详情数据模型
     */
    private void initData() { // 初始化数据
        displayDetail = new TopMsgDisplayGraticule(); // 创建方格图详情对象
        displayDetail.setDisplayMode(displayMode.getSelected()); // 设置显示模式
        displayDetail.setIntensity(displayIntensity.getProgress()); // 设置网格亮度
    }

    /**
     * 初始化视图组件，绑定单选组、滑动条和监听器
     * @param view Fragment的根视图
     */
    private void initView(View view) { // 初始化视图
        displayMode = (TopViewRadioGroup) view.findViewById(R.id.displayMode); // 获取显示模式单选组
        displayMode.setData(R.string.displayMode, R.array.displayMode, onCheckChangedListener); // 设置数据和监听器
        displayIntensity = (TopViewSeekBar) view.findViewById(R.id.chartStrength); // 获取网格亮度滑动条
        displayIntensity.setData(R.string.view_display_intensity, 100, 60, seekBarChangeListener); // 设置滑动条数据和监听器
    }

    /**
     * 初始化事件控制，订阅缓存加载和命令转UI事件
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令转UI事件
    }

    /**
     * 从缓存恢复方格图页面的配置状态
     */
    private void setCache() { // 设置缓存
        int mode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_GRATICULE_MODE); // 获取缓存的显示模式
        int intensity = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_GRATICULE_INTENSITY); // 获取缓存的网格亮度

        displayMode.setSelectedIndex(mode); // 设置显示模式选中项
        displayIntensity.setProgress(intensity); // 设置网格亮度进度

        onCheckChanged(displayMode, displayMode.getSelected(), false); // 触发显示模式切换处理
        onProgressChanged(displayIntensity, intensity, false); // 触发亮度变化处理
    }

    /**
     * 获取方格图显示详情数据
     * @return 方格图显示详情数据模型
     */
    public TopMsgDisplayGraticule getDisplayDetail() { // 获取详情数据
        return displayDetail; // 返回详情数据
    }

    /**
     * 设置详情消息发送监听器
     * @param onDetailSendMsgListener 监听器实例
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置消息发送监听器
        this.onDetailSendMsgListener = onDetailSendMsgListener; // 赋值监听器
    }

    /**
     * 通过监听器发送消息到父Fragment
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void sendMsg(boolean isFromEventBus) { // 发送消息
        if (onDetailSendMsgListener != null) { // 监听器不为空
            onDetailSendMsgListener.onClick(this, isFromEventBus); // 调用监听器回调
        }
    }

    /**
     * 缓存加载事件消费者，恢复缓存状态并标记加载完成
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override // 重写accept方法
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 处理缓存加载事件
            setCache(); // 恢复缓存状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutDisplayGraticule, true); // 标记方格图页面缓存已加载
        }
    };

    /**
     * 命令转UI事件消费者，处理底层命令回传的方格图UI更新
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令转UI消费者
        @Override // 重写accept方法
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 处理命令转UI事件
            switch (commandMsgToUI.getFlag()) { // 根据消息标志分发处理
                case CommandMsgToUI.FLAG_DISPLAY_GRATICULE: // 方格图模式命令
                    int modeIndex = Integer.parseInt(commandMsgToUI.getParam()); // 解析模式索引
                    if (displayMode.getSelected().getIndex() != modeIndex) { // UI与命令不一致
                        displayMode.setSelectedIndex(modeIndex); // 同步UI选中项
                        onCheckChanged(displayMode, displayMode.getSelected(), false); // 触发切换处理
                    }
                    break; // 退出case
                case CommandMsgToUI.FLAG_DISPLAY_INTENSITY: // 网格亮度命令
                    int intensity = Integer.parseInt(commandMsgToUI.getParam()); // 解析亮度值
                    if (displayIntensity.getProgress() != intensity) { // UI与命令不一致
                        displayIntensity.setProgress(intensity); // 同步UI进度
                        onProgressChanged(displayIntensity, displayIntensity.getProgress(), false); // 触发亮度变化处理
                    }
            }
        }
    };


    /**
     * 显示模式单选组选中变化监听器，播放音效并触发选中变化处理
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
            onCheckChanged(view, item, false); // 触发选中变化处理
        }
    };

    /**
     * 网格亮度滑动条变化监听器
     */
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() { // 滑动条变化监听器
        @Override // 重写onProgressChanged方法
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { // 进度变化回调
            TopLayoutDisplayGraticule.this.onProgressChanged(displayIntensity, progress, false); // 触发亮度变化处理
        }

        @Override // 重写onStartTrackingTouch方法
        public void onStartTrackingTouch(SeekBar seekBar) { // 开始触摸滑动条

        }

        @Override // 重写onStopTrackingTouch方法
        public void onStopTrackingTouch(SeekBar seekBar) { // 停止触摸滑动条

        }
    };

    /**
     * 处理显示模式选中变化，根据模式索引设置网格线属性
     * @param view 触发变化的单选组视图
     * @param item 选中的通道数据Bean
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) { // 处理选中变化
        if (view.getId() == R.id.displayMode) { // 显示模式单选组
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_GRATICULE_MODE, String.valueOf(item.getIndex())); // 缓存显示模式索引
            switch (item.getIndex()) { // 根据模式索引设置网格线属性
                case 0: // 全网格（十字线+全点+十字点+边框）
                    WaveGridManage.getInstance().setGridLine_Attr(WaveGridManage.GridAttr_CrossLine | WaveGridManage.GridAttr_ALLPoint |
                            WaveGridManage.GridAttr_CrossPoint | WaveGridManage.GridAttr_Frame); // 设置全网格属性
                    break; // 退出case
                case 1: // 仅十字点+边框
                    WaveGridManage.getInstance().setGridLine_Attr(WaveGridManage.GridAttr_CrossPoint | WaveGridManage.GridAttr_Frame); // 设置十字点+边框属性
                    break; // 退出case
                case 2: // 十字点+十字线+边框
                    WaveGridManage.getInstance().setGridLine_Attr(WaveGridManage.GridAttr_CrossPoint | WaveGridManage.GridAttr_CrossLine
                            | WaveGridManage.GridAttr_Frame); // 设置十字点+十字线+边框属性
                    break; // 退出case
                case 3: // 仅边框
                    WaveGridManage.getInstance().setGridLine_Attr(WaveGridManage.GridAttr_Frame); // 设置仅边框属性
                    break; // 退出case
            }
            WorkModeManage.getInstance().refresh(); // 刷新工作模式（重绘网格）
            Command.get().getDisplay().Graticule(item.getIndex(), false); // 下发方格图模式命令
            displayDetail.setDisplayMode(item); // 更新详情数据
            sendMsg(isFromEventBus); // 发送消息
        }
    }

    /**
     * 处理网格亮度变化，更新缓存并下发命令
     * @param seekBar 触发变化的滑动条视图
     * @param progress 新的亮度值
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onProgressChanged(TopViewSeekBar seekBar, int progress, boolean isFromEventBus) { // 处理亮度变化
        if (seekBar.getId() == displayIntensity.getId()) { // 网格亮度滑动条
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_GRATICULE_INTENSITY, String.valueOf(progress)); // 缓存亮度值
            WaveGridManage.getInstance().setGridLine_Bright(progress); // 设置网格亮度
            Command.get().getDisplay().Intensity(progress, false); // 下发亮度命令
        }
    }
}
