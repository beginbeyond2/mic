package com.micsig.tbook.tbookscope.top.layout.display; // 显示模块布局包

import android.annotation.SuppressLint; // 导入抑制Lint警告注解
import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle类，用于保存和恢复Fragment状态
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.Button; // 导入按钮控件

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.scope.Display.Display; // 导入显示控制单例
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件类
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组
import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令发送单例
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令消息转UI消息
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入按键音效工具
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息发送监听器
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组控件
import com.micsig.tbook.ui.top.view.selectHorList.TopBeanHorizontal; // 导入水平列表选择项数据Bean
import com.micsig.tbook.ui.top.view.selectHorList.TopViewSelectHorListToHead; // 导入水平列表头部视图
import com.micsig.tbook.ui.top.view.selectHorList.TopViewSelectHorListToList; // 导入水平列表弹窗视图
import com.micsig.tbook.ui.util.TBookUtil; // 导入TBook工具类

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/*
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: Display布局模块 - 余辉(Persist)子页面Fragment                     ║
 * ║  核心职责: 管理Display-余辉页面的UI交互、命令下发和状态同步                    ║
 * ║  架构设计: 子Fragment，通过OnDetailSendMsgListener向父Fragment传递数据       ║
 * ║  数据流向: UI控件 → onCheckChanged/onDialogChanged → Command → 底层       ║
 * ║  依赖关系: Command、Display、RxBus、CacheUtil、TBookUtil等                 ║
 * ║  使用场景: 用户在Display-余辉页面修改余辉模式/清除/调节时间时携带数据           ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by Administrator on 2017/4/6.
 */

public class TopLayoutDisplayPersist extends Fragment { // 余辉显示子页面Fragment，管理常规/FFT余辉模式、清除和调节时间配置

    private Context context; // Fragment关联的Activity上下文
    private TopViewRadioGroup displayPersist, fftDisplayPersist; // 常规余辉模式单选组、FFT余辉模式单选组
    private TopViewSelectHorListToHead selectListToHead, fftSelectListToHead; // 常规/FFT调节时间头部视图
    private TopViewSelectHorListToList selectListToList, fftSelectListToList;//这里用两个实例避免设置值相互影响，不必要添加新的逻辑区分。 // 常规/FFT调节时间弹窗列表视图
    private Button clear, fftClear; // 常规/FFT余辉清除按钮

    private TopMsgDisplayPersist displayDetail; // 余辉显示详情数据模型
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息发送监听器
    private ViewGroup rootView; // 根视图组引用

    private boolean isCache = false; // 标记当前是否处于缓存恢复模式（缓存模式下不播放音效）

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
        return inflater.inflate(R.layout.layout_displaypersist, container, false); // 加载余辉布局文件
    }

    /**
     * 视图创建完成后的初始化操作
     * @param view Fragment的根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override // 重写onViewCreated方法
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取关联的Activity上下文
        rootView = (ViewGroup) view; // 保存根视图引用
        initView(view); // 初始化视图
        initData(); // 初始化数据
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化数据，从UI控件和缓存读取当前值并填充到详情数据模型
     */
    private void initData() { // 初始化数据
        displayDetail = new TopMsgDisplayPersist(); // 创建余辉详情对象

        displayDetail.setPersist(displayPersist.getSelected()); // 设置常规余辉模式
        displayDetail.setClear(false); // 设置常规清除标记为false
        int persistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_SELECT); // 获取缓存的常规调节时间索引
        selectListToList.setData(R.id.selectListToHead, R.array.displayPersistAdjust, selectListToListListener); // 设置常规调节时间列表数据
        selectListToList.setSelected(persistSelect); // 设置常规调节时间选中项
        displayDetail.setAdjust(selectListToList.getSelected()); // 设置常规调节时间详情

        displayDetail.setFftPersist(fftDisplayPersist.getSelected()); // 设置FFT余辉模式
        displayDetail.setFftClear(false); // 设置FFT清除标记为false
        int fftPersistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_SELECT); // 获取缓存的FFT调节时间索引
        fftSelectListToList.setData(R.id.fft_selectListToHead, R.array.mathFftPersistValue, selectListToListListener); // 设置FFT调节时间列表数据
        fftSelectListToList.setSelected(fftPersistSelect); // 设置FFT调节时间选中项
        displayDetail.setFftAdjust(fftSelectListToList.getSelected()); // 设置FFT调节时间详情
    }

    /**
     * 初始化视图组件，绑定单选组、按钮、水平列表和监听器
     * @param view Fragment的根视图
     */
    private void initView(View view) { // 初始化视图
        displayPersist = (TopViewRadioGroup) view.findViewById(R.id.displayPersist); // 获取常规余辉模式单选组
        displayPersist.setData(R.string.displayPersist, R.array.displayPersist, onPersistChangeListener); // 设置数据和监听器
        clear = (Button) view.findViewById(R.id.clear_persist); // 获取常规清除按钮
        clear.setOnClickListener(onClickListener); // 设置点击监听器
        selectListToHead = (TopViewSelectHorListToHead) view.findViewById(R.id.selectListToHead); // 获取常规调节时间头部视图
        selectListToHead.setData(R.string.view_display_adjust, R.string.view_horizontallist_show, selectListToHeadListener); // 设置头部视图数据
        selectListToList = (TopViewSelectHorListToList) ((MainActivity) context).findViewById(R.id.selectListToList); // 获取常规调节时间弹窗列表
        selectListToList.setData(R.id.selectListToHead, R.array.displayPersistAdjust, selectListToListListener); // 设置弹窗列表数据

        fftDisplayPersist = (TopViewRadioGroup) view.findViewById(R.id.fft_persist); // 获取FFT余辉模式单选组
        fftDisplayPersist.setData("FFT", R.array.displayPersist, onPersistChangeListener); // 设置数据和监听器
        fftClear = (Button) view.findViewById(R.id.fft_clear_persist); // 获取FFT清除按钮
        fftClear.setOnClickListener(onClickListener); // 设置点击监听器
        fftSelectListToHead = (TopViewSelectHorListToHead) view.findViewById(R.id.fft_selectListToHead); // 获取FFT调节时间头部视图
        fftSelectListToHead.setData(R.string.view_display_adjust, R.string.view_horizontallist_show, fftSelectListToHeadListener); // 设置头部视图数据
        fftSelectListToList = (TopViewSelectHorListToList) ((MainActivity) context).findViewById(R.id.fft_selectListToList); // 获取FFT调节时间弹窗列表
        fftSelectListToList.setData(R.id.fft_selectListToHead, R.array.mathFftPersistValue, selectListToListListener); // 设置弹窗列表数据
    }

    /**
     * 初始化事件控制，订阅缓存加载和命令转UI事件
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令转UI事件
    }

    /**
     * 从缓存恢复余辉页面的所有配置状态
     */
    private void setCache() { // 设置缓存
        isCache = true; // 标记为缓存恢复模式
        Display display = Display.getInstance(); // 获取Display单例

        int persist = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_PERSIST); // 获取缓存的常规余辉模式
        int select = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_SELECT); // 获取缓存的常规调节时间索引
        displayPersist.setSelectedIndex(persist); // 设置常规余辉模式选中项
        selectListToList.setData(R.id.selectListToHead, R.array.displayPersistAdjust, selectListToListListener); // 设置常规调节时间列表数据
        selectListToList.setSelected(select); // 设置常规调节时间选中项
        if (persist != 2) { // 非自定义模式
            selectListToHead.setText(""); // 清空头部文本
            selectListToHead.setEnabled(false); // 禁用头部视图
        } else { // 自定义模式
            selectListToHead.setText(selectListToList.getSelected().getText()); // 设置头部文本为选中项文本
            selectListToHead.setEnabled(true); // 启用头部视图
        }
        Command.get().getDisplay().Persist_Mode(persist, false); // 下发常规余辉模式命令
        Command.get().getDisplay().Persist_Adjust(select, false); // 下发常规调节时间命令
        display.setPersistType(persist); // 更新Display单例常规余辉类型
        display.setPersistAdjustTime((int) (TBookUtil.getSFromTime(selectListToList.getSelected().getText()) * 1000)); // 更新Display单例常规调节时间（秒转毫秒）
        displayDetail.setPersist(displayPersist.getSelected()); // 更新详情数据-常规余辉模式
        displayDetail.setAdjust(selectListToList.getSelected()); // 更新详情数据-常规调节时间

        int fftPersist = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_PERSIST); // 获取缓存的FFT余辉模式
        int fftSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_SELECT); // 获取缓存的FFT调节时间索引
        fftDisplayPersist.setSelectedIndex(fftPersist); // 设置FFT余辉模式选中项
        fftSelectListToList.setData(R.id.fft_selectListToHead, R.array.mathFftPersistValue, selectListToListListener); // 设置FFT调节时间列表数据
        fftSelectListToList.setSelected(fftSelect); // 设置FFT调节时间选中项
        if(fftPersist != 2) { // 非自定义模式
            fftSelectListToHead.setText(""); // 清空FFT头部文本
            fftSelectListToHead.setEnabled(false); // 禁用FFT头部视图
        } else { // 自定义模式
            fftSelectListToHead.setText(fftSelectListToList.getSelected().getText()); // 设置FFT头部文本
            fftSelectListToHead.setEnabled(true); // 启用FFT头部视图
        }
        Command.get().getDisplay().FftPersist_Mode(fftPersist, false); // 下发FFT余辉模式命令
        Command.get().getDisplay().FftPersist_Adjust(select, false); // 下发FFT调节时间命令
        display.setFftPersistType(fftPersist); // 更新Display单例FFT余辉类型
        display.setFftPersistAdjustTime((int) (TBookUtil.getSFromTime(fftSelectListToList.getSelected().getText()) * 1000)); // 更新Display单例FFT调节时间
        displayDetail.setFftPersist(fftDisplayPersist.getSelected()); // 更新详情数据-FFT余辉模式
        displayDetail.setFftAdjust(fftSelectListToList.getSelected()); // 更新详情数据-FFT调节时间

        sendMsg(false); // 发送消息
    }

    /**
     * 获取余辉显示详情数据
     * @return 余辉显示详情数据模型
     */
    public TopMsgDisplayPersist getDisplayDetail() { // 获取详情数据
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
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutDisplayPersist, true); // 标记余辉页面缓存已加载
        }
    };

    /**
     * 命令转UI事件消费者，处理底层命令回传的余辉UI更新
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令转UI消费者
        @Override // 重写accept方法
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 处理命令转UI事件
            switch (commandMsgToUI.getFlag()) { // 根据消息标志分发处理
                case CommandMsgToUI.FLAG_DISPLAY_PERSISTMODE: { // 常规余辉模式命令
                    int index = Integer.parseInt(commandMsgToUI.getParam()); // 解析模式索引
                    if (displayPersist.getSelected().getIndex() != index) { // UI与命令不一致
                        displayPersist.setSelectedIndex(index); // 同步UI选中项
                        onCheckChanged(displayPersist, displayPersist.getSelected(), false); // 触发切换处理
                    }
                }
                break; // 退出case
                case CommandMsgToUI.FLAG_DISPLAY_FFT_PERSISTMODE: { // FFT余辉模式命令
                    int index = Integer.parseInt(commandMsgToUI.getParam()); // 解析模式索引
                    if (fftDisplayPersist.getSelected().getIndex() != index) { // UI与命令不一致
                        fftDisplayPersist.setSelectedIndex(index); // 同步UI选中项
                        onCheckChanged(fftDisplayPersist, fftDisplayPersist.getSelected(), false); // 触发切换处理
                    }
                }
                break; // 退出case
                case CommandMsgToUI.FLAG_DISPLAY_PERSISTADJUST: { // 常规调节时间命令
                    int time = Integer.parseInt(commandMsgToUI.getParam()); // 解析时间值
                    int persistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_SELECT); // 获取缓存的调节时间索引
                    selectListToList.setData(R.id.selectListToHead, R.array.displayPersistAdjust, selectListToListListener); // 设置列表数据
                    selectListToList.setSelected(persistSelect); // 设置选中项
                    TopBeanHorizontal bean = selectListToList.getBean(time); // 根据时间值获取对应Bean
                    if (displayPersist.getSelected().getIndex() != 2) { // 非自定义模式
                        return; // 直接返回
                    }
                    if (bean.getIndex() != selectListToList.getSelected().getIndex()) { // Bean索引与当前选中不一致
                        onDialogChanged(R.id.selectListToHead, bean, false); // 触发弹窗变化处理
                    }
                }
                break; // 退出case
                case CommandMsgToUI.FLAG_DISPLAY_FFT_PERSISTADJUST: { // FFT调节时间命令
                    int time = Integer.parseInt(commandMsgToUI.getParam()); // 解析时间值
                    int fftPersistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_SELECT); // 获取缓存的FFT调节时间索引
                    fftSelectListToList.setData(R.id.fft_selectListToHead, R.array.mathFftPersistValue, selectListToListListener); // 设置列表数据
                    fftSelectListToList.setSelected(fftPersistSelect); // 设置选中项
                    TopBeanHorizontal bean = fftSelectListToList.getBean(time); // 根据时间值获取对应Bean
                    if (fftDisplayPersist.getSelected().getIndex() != 2) { // 非自定义模式
                        return; // 直接返回
                    }
                    if (bean.getIndex() != fftSelectListToList.getSelected().getIndex()) { // Bean索引与当前选中不一致
                        onDialogChanged(R.id.fft_selectListToHead, bean, false); // 触发弹窗变化处理
                    }
                }
                break; // 退出case
                case CommandMsgToUI.FLAG_DISPLAY_PERSIST_CLEAR: { // 常规清除命令
                    onClickListener.onClick(clear); // 模拟点击常规清除按钮
                }
                break; // 退出case
                case CommandMsgToUI.FLAG_DISPLAY_FFT_PERSIST_CLEAR: { // FFT清除命令
                    onClickListener.onClick(fftClear); // 模拟点击FFT清除按钮
                }
                break; // 退出case
            }
        }
    };

    /**
     * 按钮点击监听器，处理余辉清除操作
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 按钮点击监听器
        @SuppressLint("NonConstantResourceId") // 抑制非常量资源ID警告
        @Override // 重写onClick方法
        public void onClick(View v) { // 处理点击事件
            PlaySound.getInstance().playButton(); // 播放按键音效
            switch (v.getId()) { // 根据视图ID分发
                case R.id.clear_persist: // 常规清除按钮
                    Command.get().getDisplay().Persist_Clear(false); // 下发常规余辉清除命令
                    Display.getInstance().clearPersist(); // 清除常规余辉
                    break; // 退出case
                case R.id.fft_clear_persist: // FFT清除按钮
                    Command.get().getDisplay().FftPersist_Clear(false); // 下发FFT余辉清除命令
                    Display.getInstance().clearFftPersist(); // 清除FFT余辉
                    break; // 退出case
            }
        }
    };


    /**
     * 余辉模式单选组选中变化监听器，播放音效并触发选中变化处理
     */
    private TopViewRadioGroup.OnCheckChangedListener onPersistChangeListener = new TopViewRadioGroup.OnCheckChangedListener() { // 余辉模式变化监听器
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
     * 常规调节时间头部视图点击监听器，弹出调节时间选择列表
     */
    private TopViewSelectHorListToHead.OnClickListener selectListToHeadListener = new TopViewSelectHorListToHead.OnClickListener() { // 常规头部点击监听器
        @SuppressLint("NonConstantResourceId") // 抑制非常量资源ID警告
        @Override // 重写onClick方法
        public void onClick(View v) { // 处理点击事件
            PlaySound.getInstance().playButton(); // 播放按键音效
            int persistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_SELECT); // 获取缓存的常规调节时间索引
            selectListToList.setData(R.id.selectListToHead, R.array.displayPersistAdjust, selectListToListListener); // 设置列表数据
            selectListToList.setSelected(persistSelect); // 设置选中项
            selectListToList.show(376); // 弹出列表（宽度376px）
        }
    };

    /**
     * FFT调节时间头部视图点击监听器，弹出FFT调节时间选择列表
     */
    private TopViewSelectHorListToHead.OnClickListener fftSelectListToHeadListener = new TopViewSelectHorListToHead.OnClickListener() { // FFT头部点击监听器
        @SuppressLint("NonConstantResourceId") // 抑制非常量资源ID警告
        @Override // 重写onClick方法
        public void onClick(View v) { // 处理点击事件
            PlaySound.getInstance().playButton(); // 播放按键音效
            int fftPersistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_SELECT); // 获取缓存的FFT调节时间索引
            fftSelectListToList.setData(R.id.fft_selectListToHead, R.array.mathFftPersistValue, selectListToListListener); // 设置列表数据
            fftSelectListToList.setSelected(fftPersistSelect); // 设置选中项
            fftSelectListToList.show(376); // 弹出列表（宽度376px）
        }
    };

    /**
     * 调节时间弹窗列表变化监听器，处理列表选择、弹窗显示/隐藏事件
     */
    private TopViewSelectHorListToList.OnDialogChangedListener selectListToListListener = new TopViewSelectHorListToList.OnDialogChangedListener() { // 弹窗列表变化监听器
        @Override // 重写checkChanged方法
        public void checkChanged(int headViewId, TopBeanHorizontal item) { // 选择项变化回调
            onDialogChanged(headViewId, item, false); // 触发弹窗变化处理
        }

        @Override // 重写onShow方法
        public void onShow() { // 弹窗显示回调
            RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_AFTERGLOW); // 发送余辉弹窗打开事件
        }

        @Override // 重写onHide方法
        public void onHide() { // 弹窗隐藏回调
            RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_AFTERGLOW); // 发送余辉弹窗关闭事件
        }
    };

    /**
     * 处理余辉模式选中变化，根据模式更新UI和下发命令
     * @param view 触发变化的单选组视图
     * @param item 选中的通道数据Bean
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) { // 处理选中变化
        Tools.PrintControlsLocation("TopLayoutDisplayPersist", rootView); // 打印控件位置调试信息
        if (view.getId() == R.id.displayPersist) { //余辉选择 // 常规余辉模式变化
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_PERSIST, String.valueOf(item.getIndex())); // 缓存常规余辉模式索引
            if (item.getIndex() != 2) { // 非自定义模式
                selectListToHead.setText(""); // 清空头部文本
                selectListToHead.setEnabled(false); // 禁用头部视图
            } else { // 自定义模式
                int persistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_SELECT); // 获取缓存的调节时间索引
                selectListToList.setData(R.id.selectListToHead, R.array.displayPersistAdjust, selectListToListListener); // 设置列表数据
                selectListToList.setSelected(persistSelect); // 设置选中项
                selectListToHead.setText(selectListToList.getSelected().getText()); // 设置头部文本
                selectListToHead.setEnabled(true); // 启用头部视图
            }
            Command.get().getDisplay().Persist_Mode(item.getIndex(), false); // 下发常规余辉模式命令
            if (!isFromEventBus) { // 非EventBus来源
                Display display = Display.getInstance(); // 获取Display单例
                display.setPersistType(item.getIndex()); // 更新常规余辉类型
                if (item.getIndex() == 2) { // 自定义模式
                    display.setPersistAdjustTime((int) (TBookUtil.getSFromTime(selectListToList.getSelected().getText()) * 1000)); // 更新常规调节时间
                }
            }
            displayDetail.setPersist(item); // 更新详情数据-常规余辉模式
            if (item.getIndex() == 2) { // 自定义模式
                displayDetail.setAdjust(selectListToList.getSelected()); // 更新详情数据-常规调节时间
            }
            sendMsg(isFromEventBus); // 发送消息
        } else if (view.getId() == R.id.fft_persist) { //FFT 余辉选择 // FFT余辉模式变化
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_PERSIST, String.valueOf(item.getIndex())); // 缓存FFT余辉模式索引
            if (item.getIndex() != 2) { // 非自定义模式
                fftSelectListToHead.setText(""); // 清空FFT头部文本
                fftSelectListToHead.setEnabled(false); // 禁用FFT头部视图
            } else { // 自定义模式
                int fftPersistSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_SELECT); // 获取缓存的FFT调节时间索引
                fftSelectListToList.setData(R.id.fft_selectListToHead, R.array.mathFftPersistValue, selectListToListListener); // 设置列表数据
                fftSelectListToList.setSelected(fftPersistSelect); // 设置选中项
                fftSelectListToHead.setText(fftSelectListToList.getSelected().getText()); // 设置FFT头部文本
                fftSelectListToHead.setEnabled(true); // 启用FFT头部视图
            }
            Command.get().getDisplay().FftPersist_Mode(item.getIndex(), false); // 下发FFT余辉模式命令
            if (!isFromEventBus) { // 非EventBus来源
                Display display = Display.getInstance(); // 获取Display单例
                display.setFftPersistType(item.getIndex()); // 更新FFT余辉类型
                if (item.getIndex() == 2) { // 自定义模式
                    display.setFftPersistAdjustTime((int) (TBookUtil.getSFromTime(fftSelectListToList.getSelected().getText()) * 1000)); // 更新FFT调节时间
                }
            }
            displayDetail.setFftPersist(item); // 更新详情数据-FFT余辉模式
            if (item.getIndex() == 2) { // 自定义模式
                displayDetail.setFftAdjust(fftSelectListToList.getSelected()); // 更新详情数据-FFT调节时间
            }
            sendMsg(isFromEventBus); // 发送消息
        }
    }

    /**
     * 处理调节时间弹窗选择变化，更新UI和下发命令
     * @param headViewId 头部视图ID，区分常规/FFT
     * @param item 选中的水平列表数据Bean
     * @param isFromEventBus 是否来自EventBus事件
     */
    @SuppressLint("NonConstantResourceId") // 抑制非常量资源ID警告
    private void onDialogChanged(int headViewId, TopBeanHorizontal item, boolean isFromEventBus) { // 处理弹窗选择变化
        switch (headViewId) { // 根据头部视图ID分发
            case R.id.selectListToHead: // 常规调节时间
                if (!isCache) { // 非缓存恢复模式
                    PlaySound.getInstance().playButton(); // 播放按键音效
                }
                isCache = false; // 重置缓存标记
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_PERSIST_SELECT, String.valueOf(item.getIndex())); // 缓存常规调节时间索引
                Command.get().getDisplay().Persist_Adjust(TBookUtil.getMsFromTime(item.getText()), false); // 下发常规调节时间命令（毫秒）
                if (!isFromEventBus) { // 非EventBus来源
                    Display.getInstance().setPersistAdjustTime((int) (TBookUtil.getSFromTime(item.getText()) * 1000)); // 更新Display单例常规调节时间
                }
                selectListToHead.setText(item.getText()); // 更新头部文本
                displayDetail.setAdjust(item); // 更新详情数据
                sendMsg(isFromEventBus); // 发送消息
                break; // 退出case
            case R.id.fft_selectListToHead: // FFT调节时间
                if (!isCache) { // 非缓存恢复模式
                    PlaySound.getInstance().playButton(); // 播放按键音效
                }
                isCache = false; // 重置缓存标记
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_DISPLAY_FFT_PERSIST_SELECT, String.valueOf(item.getIndex())); // 缓存FFT调节时间索引
                Command.get().getDisplay().FftPersist_Adjust(TBookUtil.getMsFromTime(item.getText()), false); // 下发FFT调节时间命令（毫秒）
                if (!isFromEventBus) { // 非EventBus来源
                    Display.getInstance().setFftPersistAdjustTime((int) (TBookUtil.getSFromTime(item.getText()) * 1000)); // 更新Display单例FFT调节时间
                }
                fftSelectListToHead.setText(item.getText()); // 更新FFT头部文本
                displayDetail.setFftAdjust(item); // 更新详情数据
                sendMsg(isFromEventBus); // 发送消息
                break; // 退出case
        }
    }
}
