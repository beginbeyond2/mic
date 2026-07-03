package com.micsig.tbook.tbookscope.top.layout.userset; // 用户设置模块包声明

import android.annotation.SuppressLint; // 导入SuppressLint注解
import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle
import android.os.Handler; // 导入Handler
import android.util.Log; // 导入日志工具
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.RadioButton; // 导入单选按钮控件

import androidx.annotation.NonNull; // 导入NonNull注解
import androidx.annotation.Nullable; // 导入Nullable注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.base.Logger; // 导入日志工具
import com.micsig.tbook.hardware.HardwareProduct; // 导入硬件产品型号判断
import com.micsig.tbook.scope.Sample.Sample; // 导入采样控制
import com.micsig.tbook.scope.Scope; // 导入示波器核心
import com.micsig.tbook.scope.Trigger.TriggerCommon; // 导入触发通用
import com.micsig.tbook.scope.Trigger.TriggerFactory; // 导入触发工厂
import com.micsig.tbook.scope.fpga.FPGACommand; // 导入FPGA命令
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载事件
import com.micsig.tbook.tbookscope.R; // 导入资源引用
import com.micsig.tbook.tbookscope.main.dialog.DialogOkCancel; // 导入确认取消对话框
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令消息到UI
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组

import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava Consumer

/*
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：用户设置 → 触发/时钟输入输出 → Fragment页面                       │
 * │ 核心职责：管理触发输入/输出、时钟输入/输出、输入阻抗的切换和状态检测         │
 * │ 架构设计：Fragment子类，通过RxBus监听缓存加载和命令消息，使用定时检测机制   │
 * │ 数据流向：CacheUtil → 本类UI → Command中间件 → FPGA硬件                    │
 * │ 依赖关系：Scope、Sample、FPGACommand、TriggerCommon、RxBus、CacheUtil       │
 * │ 使用场景：用户设置界面中"触发/时钟输入输出"子页面                           │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 触发输入/输出与时钟输入/输出设置Fragment。
 * <p>
 * 管理三组单选控件：
 * <ul>
 *   <li>触发输入/输出 - 控制外部触发信号方向</li>
 *   <li>时钟输入/输出 - 控制参考时钟信号方向，带自动检测和弹窗提示</li>
 *   <li>输入阻抗 - 控制外部触发输入阻抗（仅MHO68V1隐藏）</li>
 * </ul>
 * 时钟切换时会启动定时检测机制，检测时钟信号状态并自动弹窗提示用户。
 *
 * @author limh
 * @since 2024-11-22
 */
public class TopLayoutUserSetAuxOut extends Fragment {

    /** 上下文对象 */
    private Context context; // 上下文对象
    /** 触发方向单选组 */
    private TopViewRadioGroup groupTrigger, groupClock, groupImped; // 触发、时钟、阻抗单选组
    /** 根视图组 */
    private ViewGroup rootViewGroup; // 根视图组
    /** 确认取消对话框 */
    private DialogOkCancel dialogOk; // 确认取消对话框
    /** Handler用于定时检测 */
    private final Handler handler = new Handler(); // Handler用于定时检测
    /** 定时检测Runnable */
    private Runnable runnable; // 定时检测Runnable
    /** 定时检测是否运行中 */
    private boolean isRunning = false; // 定时检测是否运行中
    /** 时钟输入检测间隔（2秒） */
    private static final int clockInDetectionTime = 2000;//2秒一检测，10秒一弹窗 // 时钟输入检测间隔
    /** 时钟输出检测间隔（30毫秒） */
    private static final int clockOutDetectionTime = 30;//30毫秒 // 时钟输出检测间隔

    /**
     * 创建Fragment视图，加载布局文件。
     *
     * @param inflater  布局填充器
     * @param container 父视图组
     * @param savedInstanceState 保存的实例状态
     * @return 创建的视图
     */
    @Override // 覆写创建视图方法
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建Fragment视图
        return inflater.inflate(R.layout.layout_usersetauxout, container, false); // 加载触发/时钟输入输出布局
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

        groupTrigger = view.findViewById(R.id.trigger); // 查找触发方向单选组
        groupClock = view.findViewById(R.id.clock); // 查找时钟方向单选组
        groupImped = view.findViewById(R.id.input_imped); // 查找输入阻抗单选组

        groupTrigger.setOnListener(onCheckChangedListener); // 设置触发单选组监听
        groupClock.setOnListener(onCheckChangedListener); // 设置时钟单选组监听
        groupImped.setOnListener(onCheckChangedListener); // 设置阻抗单选组监听

        groupTrigger.setRadioButtonOnPromptState(1, true); // 触发"输出"选项需要二次确认
        groupClock.setRadioButtonOnPromptState(1, true); // 时钟"输入"选项需要二次确认

        dialogOk = getParentFragment().getActivity().findViewById(R.id.dialogOkCancel); // 获取确认取消对话框

        groupImped.setVisibility(HardwareProduct.isMHO68V1() ? View.GONE : View.VISIBLE); // MHO68V1隐藏阻抗选项
    }

    /**
     * 初始化控制逻辑，订阅RxBus事件。
     */
    private void initControl() { // 初始化控制逻辑
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令消息
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SYNC_EXTERNAL_TRIGGER_STATE).subscribe(consumerSyncExternalTriggerState); // 订阅外部触发状态同步
    }

    /** 单选组选中变化监听器 */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选组选中变化监听器
        @Override // 覆写点击回调
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 选中项变化回调
            onSelectChanged(view, item, false); // 处理选中变化
            if (view.getId() == R.id.clock) { // 如果是时钟单选组
                if (item.getIndex() == 0) num = 0;//时钟切换到输出时 // 切换到输出时重置计数器
            }
        }

        @Override // 覆写音效回调
        public void onClickSound(boolean isCheckedSuccess) { // 点击音效回调
            PlaySound.getInstance().playButton(); // 播放按钮音效
        }

        @Override // 覆写拦截提示回调
        public void onPrompt(TopViewRadioGroup view) { //点击操作 有拦截 // 需要二次确认的拦截回调
            if (view.getId() == groupTrigger.getId()) { // 触发方向需要确认
                showTriggerDialog(); // 显示触发确认对话框
            } else if (view.getId() == groupClock.getId()) { // 时钟方向需要确认
                checkClockInStatus(); // 检测时钟输入状态
            }
        }
    };


    /** 缓存运行/停止状态标记，0=正常模式 */
    int cacheRunStop = 0; // 缓存运行/停止状态标记

    /**
     * 从缓存恢复设置状态。
     */
    private void setCache() { // 从缓存恢复设置
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_TRIGGER); // 读取触发方向缓存
        groupTrigger.setSelectedIndex(triggerIndex); // 设置触发方向选中项
        onCheckChangedListener.onClick(groupTrigger, groupTrigger.getSelected()); // 触发选中变化

        int clockIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_CLOCK); // 读取时钟方向缓存
        //时钟启动后不保持之前状态，默认输出
        cacheRunStop  = CacheUtil.get().getBoolean(CacheUtil.MAIN_LEFT_RUNSTOP) ? 1 : 2; // 读取运行/停止状态，1=运行，2=停止
        groupClock.setSelectedIndex(0); // 时钟默认选中输出
        onCheckChangedListener.onClick(groupClock, groupClock.getSelected()); // 触发时钟选中变化

        Command.get().getMenu().aux_trigger(groupTrigger.getSelected().getIndex(), false); // 发送触发方向命令
        Command.get().getMenu().aux_clock(groupClock.getSelected().getIndex(), false); // 发送时钟方向命令
        Command.get().getMenu().aux_inputres(groupImped.getSelected().getIndex(),false); // 发送输入阻抗命令
        cacheRunStop = 0; // 恢复正常模式
    }

    /**
     * 处理单选组选中变化，更新硬件和缓存。
     *
     * @param view          触发变化的单选组
     * @param item          选中的项
     * @param isFromEventBus 是否来自EventBus
     */
    @SuppressLint("NonConstantResourceId") // 抑制非常量资源ID警告
    private void onSelectChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) { // 处理选中变化
        Tools.PrintControlsLocation("TopLayoutUserSetAuxOut", rootViewGroup); // 打印控件位置日志
        switch (view.getId()) { // 根据视图ID分发
            case R.id.trigger: { // 触发方向变化
                boolean isChecked = item.getIndex() == 1; // 判断是否为输入模式
                Sample.getInstance().setTriggerInOut(isChecked); // 设置采样触发输入输出方向
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET_TRIGGER, String.valueOf(item.getIndex())); // 缓存触发方向
                RxBus.getInstance().post(RxEnum.MQ_MSG_SYNC_EXTERNAL_TRIGGER_STATE, isChecked);//同步外部触发状态 // 发送外部触发状态同步事件
                Command.get().getMenu().aux_trigger(item.getIndex(), false); // 发送触发方向命令
                updateInputImpedState(item.getIndex()); // 更新输入阻抗使能状态
            }
            break; // 结束触发分支
            case R.id.clock: { // 时钟方向变化
                stopDetection(); // 停止定时检测
                boolean isChecked = item.getIndex() == 1; // 判断是否为输入模式
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_USERSET_CLOCK, String.valueOf(item.getIndex())); // 缓存时钟方向
                if(!isChecked) num = 0; // 切换到输出时重置计数器
                setClockInOut(isChecked); // 设置时钟输入输出
                Command.get().getMenu().aux_clock(item.getIndex(), false); // 发送时钟方向命令
            }
            break; // 结束时钟分支
            case R.id.input_imped: { // 输入阻抗变化
                TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon(); // 获取触发通用实例
                triggerCommon.setExtTriggerInputRes(item.getIndex()); // 设置外部触发输入阻抗
                Command.get().getMenu().aux_inputres(item.getIndex(),false); // 发送输入阻抗命令
            }
            break; // 结束阻抗分支
            default: // 默认分支
                break; // 无操作
        }
    }

    /**
     * 更新输入阻抗控件的使能状态。
     * <p>
     * 仅当触发方向为"输入"时，阻抗选项才可用。
     *
     * @param index 触发方向索引，1=输入
     */
    private void updateInputImpedState(int index) { // 更新输入阻抗使能状态
        groupImped.setEnabled(index == 1); // 仅输入模式时启用阻抗选项
        TriggerCommon triggerCommon = TriggerFactory.getInstance().getTriggerCommon(); // 获取触发通用实例
        if (index == 1) { // 如果是输入模式
            triggerCommon.setExtTriggerInputRes(groupImped.getSelected().getIndex()); // 设置当前选中的阻抗值
        }
    }

    /** 缓存加载事件消费者 */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载事件消费者
        @Override // 覆写accept方法
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 从缓存恢复设置
        }
    };

    /** 命令消息到UI消费者 */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令消息到UI消费者
        @Override // 覆写accept方法
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收命令消息
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发
                case CommandMsgToUI.FLAG_MENU_AUX_TRIGGER: { // 触发方向命令
                    int idx = Integer.parseInt(commandMsgToUI.getParam()); // 解析触发方向索引
                    groupTrigger.setSelectedIndex(idx); // 设置触发方向选中项
                    //onCheckChangedListener.onClick(groupTrigger, groupTrigger.getSelected());
                    onSelectChanged(groupTrigger,new TopBeanChannel(idx,groupTrigger.getSelectedString()),false); // 处理选中变化
                }
                break; // 结束触发命令分支
                case CommandMsgToUI.FLAG_MENU_AUX_CLOCK: { // 时钟方向命令
                    int idx = Integer.parseInt(commandMsgToUI.getParam()); // 解析时钟方向索引
                    if (idx==groupClock.getSelected().getIndex()) return; // 索引相同则跳过
                    groupClock.setSelectedIndex(idx); // 设置时钟方向选中项
                    switchClock(idx==1); // 切换时钟输入输出

//                    onCheckChangedListener.onPrompt(groupClock);
//                    onCheckChangedListener.onClick(groupClock, groupClock.getSelected());
//                    onSelectChanged(groupClock,new TopBeanChannel(idx,groupClock.getSelectedString()),false);
                }
                break; // 结束时钟命令分支
                case CommandMsgToUI.FLAG_MENU_AUX_INPUTRES:{ // 输入阻抗命令
                    if (groupTrigger.getSelected().getIndex()!=1) return; // 触发非输入模式则跳过
                    int idx = Integer.parseInt(commandMsgToUI.getParam()); // 解析阻抗索引
                    groupImped.setSelectedIndex(idx); // 设置阻抗选中项
                    onSelectChanged(groupImped,new TopBeanChannel(idx,groupImped.getSelectedString()),false); // 处理选中变化

                }break; // 结束阻抗命令分支
                default: // 默认分支
                    break; // 无操作
            }
        }
    };

    /**
     * 切换时钟输入输出方向，启动对应方向的检测。
     *
     * @param isIn true=输入，false=输出
     */
    private void switchClock(boolean isIn){ // 切换时钟输入输出
        stopDetection(); // 停止当前检测
        setScopeState(false); // 暂停示波器
        Sample.getInstance().setClkInOut(isIn); // 设置时钟输入输出方向
        Tools.sleep(200); // 等待200ms硬件响应
        startScheduledDetection(isIn); // 启动对应方向的定时检测
    }

    /** 外部触发状态同步消费者 */
    private Consumer<Boolean> consumerSyncExternalTriggerState = new Consumer<Boolean>() { // 外部触发状态同步消费者
        @Override // 覆写accept方法
        public void accept(Boolean aBoolean) throws Throwable { // 接收外部触发状态
            int newIndex = aBoolean ? 1 : 0; // 布尔值转索引
            if (newIndex == groupTrigger.getSelected().getIndex()) return; // 索引相同则跳过
            groupTrigger.setSelectedIndex(newIndex); // 设置触发方向选中项
            onCheckChangedListener.onClick(groupTrigger, groupTrigger.getSelected()); // 触发选中变化
        }
    };

    /**
     * 改变时钟方向选中项（不触发二次确认）。
     *
     * @param isIn true=输入，false=输出
     */
    private void changeClockSelect(boolean isIn) { // 改变时钟方向选中项
        int selectIndex = groupClock.getSelected().getIndex(); // 获取当前选中索引
        TopBeanChannel itemOut = new TopBeanChannel(0, ((RadioButton) (groupClock.getRadioGroup().getChildAt(0))).getText().toString()); // 创建输出项
        TopBeanChannel itemIn = new TopBeanChannel(1, ((RadioButton) (groupClock.getRadioGroup().getChildAt(1))).getText().toString()); // 创建输入项
        if (isIn && selectIndex != 1) { // 切换到输入且当前不是输入
            groupClock.setSelectedIndex(1); // 选中输入
            onSelectChanged(groupClock, itemIn, false); // 处理选中变化
        } else if (!isIn && selectIndex != 0) { // 切换到输出且当前不是输出
            groupClock.setSelectedIndex(0); // 选中输出
            onSelectChanged(groupClock, itemOut, false); // 处理选中变化
        }
    }

    /** 时钟检测计数器 */
    private int num = 0; // 时钟检测计数器

    /**
     * 检测时钟输入状态，根据FPGA反馈决定是否弹窗或恢复运行。
     */
    private void checkClockInStatus() { // 检测时钟输入状态
        boolean inStatus = Scope.getInstance().getFpgaClockInOutStatus(true); // 获取FPGA时钟输入状态
        Logger.d("limh", "checkInStatus= " + inStatus + ",nums= " + num + ",clockDialogIsShow= " + isClockDialogShow()); // 打印检测日志
        if(inStatus){ // 时钟输入信号正常
            if(!isScopeRun()){ // 示波器未运行
                setScopeState(true); // 恢复示波器运行
            }
            hideClockDialog(); // 隐藏时钟对话框
        }else{ // 时钟输入信号异常
            if(!isClockDialogShow() && num % 4 == 0){ // 对话框未显示且计数器为4的倍数
                setScopeState(false); // 暂停示波器
                showClockDialog(); // 显示时钟确认对话框
            }
        }
        startScheduledDetection(true); // 继续启动输入方向定时检测
        num++; // 递增计数器
    }

    /**
     * 检测时钟输出状态，根据FPGA反馈决定是否继续检测或恢复运行。
     */
    private void checkClockOutStatus() { // 检测时钟输出状态
        boolean outStatus = Scope.getInstance().getFpgaClockInOutStatus(false); // 获取FPGA时钟输出状态
        if (!outStatus) { // 输出状态异常
            startScheduledDetection(false); // 继续启动输出方向定时检测
        } else { // 输出状态正常
            setScopeState(true); // 恢复示波器运行
            stopDetection(); // 停止定时检测
        }
    }

    /** 对话框隐藏状态常量 */
    private static final int DIALOG_HIDE = 0; // 对话框隐藏状态
    /** 对话框显示触发确认常量 */
    private static final int DIALOG_TRIGGER_SHOW = 1; // 对话框显示触发确认
    /** 对话框显示时钟确认常量 */
    private static final int DIALOG_CLOCK_SHOW = 2; // 对话框显示时钟确认
    /** 当前对话框显示状态 */
    private int dialogIsShow = DIALOG_HIDE; // 当前对话框显示状态

    /**
     * 判断时钟确认对话框是否正在显示。
     *
     * @return true=正在显示
     */
    private boolean isClockDialogShow(){ // 判断时钟对话框是否显示
        return dialogIsShow == DIALOG_CLOCK_SHOW; // 比较状态常量
    }

    /**
     * 显示时钟确认对话框，提示用户时钟输入信号异常。
     */
    private void showClockDialog() { // 显示时钟确认对话框
        if(dialogIsShow == DIALOG_HIDE) { // 当前无对话框显示
            TopBeanChannel item0 = new TopBeanChannel(0, ((RadioButton) (groupClock.getRadioGroup().getChildAt(0))).getText().toString()); // 创建输出选项
            TopBeanChannel item1 = new TopBeanChannel(1, ((RadioButton) (groupClock.getRadioGroup().getChildAt(1))).getText().toString()); // 创建输入选项
            dialogOk.setData(groupClock, R.string.top_user_auxout_clock_tips, item1, item0, onOkCancelClickListener); // 设置对话框数据
            dialogIsShow = DIALOG_CLOCK_SHOW; // 标记时钟对话框显示中
        }
    }

    /**
     * 显示触发确认对话框，提示用户触发方向切换。
     */
    private void showTriggerDialog() { // 显示触发确认对话框
        if(dialogIsShow == DIALOG_HIDE) { // 当前无对话框显示
            TopBeanChannel item0 = new TopBeanChannel(0, ((RadioButton) (groupTrigger.getRadioGroup().getChildAt(0))).getText().toString()); // 创建输出选项
            TopBeanChannel item1 = new TopBeanChannel(1, ((RadioButton) (groupTrigger.getRadioGroup().getChildAt(1))).getText().toString()); // 创建输入选项
            dialogOk.setData(groupTrigger, R.string.top_user_auxout_trigger_tips, item1, item0, onOkCancelClickListener); // 设置对话框数据
            dialogIsShow = DIALOG_TRIGGER_SHOW; // 标记触发对话框显示中
        }
    }

    /**
     * 隐藏时钟确认对话框。
     */
    private void hideClockDialog(){ // 隐藏时钟确认对话框
        if(dialogIsShow == DIALOG_CLOCK_SHOW) { // 时钟对话框正在显示
            hideDialog(); // 隐藏对话框
        }
    }

    /**
     * 隐藏当前显示的对话框。
     */
    private void hideDialog(){ // 隐藏对话框
        if(dialogIsShow != DIALOG_HIDE) { // 有对话框正在显示
            dialogIsShow = DIALOG_HIDE; // 重置对话框状态
            if (dialogOk.isShow()) { // 对话框可见
                dialogOk.hide(); // 隐藏对话框
            }
        }
    }

    /**
     * 启动定时检测机制，根据方向选择不同的检测间隔。
     *
     * @param isIn true=输入方向（2秒间隔），false=输出方向（30毫秒间隔）
     */
    private void startScheduledDetection(boolean isIn) { // 启动定时检测
        isRunning = true; // 标记检测运行中
        runnable = () -> { // 创建检测Runnable
            if (isRunning) { // 检测仍在运行
                if (isIn) { // 输入方向
                    checkClockInStatus(); // 检测时钟输入状态
                } else { // 输出方向
                    checkClockOutStatus(); // 检测时钟输出状态
                }
            }
        };
        handler.removeCallbacksAndMessages(null); // 清除之前的回调
        handler.postDelayed(runnable, isIn ? clockInDetectionTime : clockOutDetectionTime); // 延迟执行检测
    }

    /**
     * 停止定时检测机制。
     */
    private void stopDetection() { // 停止定时检测
        Log.d("limh", "stopDetection() called"); // 打印停止检测日志
        hideClockDialog(); // 隐藏时钟对话框
        isRunning = false; // 标记检测停止
        if (runnable != null) { // Runnable不为空
            handler.removeCallbacks(runnable); // 移除回调
        }
    }

    /**
     * Fragment暂停时停止定时检测。
     */
    @Override // 覆写onPause方法
    public void onPause() { // Fragment暂停回调
        super.onPause(); // 调用父类方法
        stopDetection(); // 停止定时检测
    }

    /**
     * Fragment恢复时，若时钟为输入模式则启动检测。
     */
    @Override // 覆写onResume方法
    public void onResume() { // Fragment恢复回调
        super.onResume(); // 调用父类方法
        if (groupClock.getSelected().getIndex() == 1) { // 时钟为输入模式
            isRunning = true; // 标记检测运行中
            checkClockInStatus(); // 检测时钟输入状态
        }
    }

    /** 确认取消对话框点击监听器 */
    private DialogOkCancel.OnOkCancelClickListener onOkCancelClickListener = new DialogOkCancel.OnOkCancelClickListener() { // 确认取消对话框点击监听器
        @Override // 覆写确认点击回调
        public void onOkClick(View view, Object okData) { // 点击确认
            if (okData == null || view == null) return; // 数据为空则返回
            if (view.getId() == R.id.trigger) { // 触发对话框确认
                onSelectChanged(groupTrigger, (TopBeanChannel) okData, false); // 处理触发选中变化
            } else if (view.getId() == R.id.clock) { // 时钟对话框确认
                setScopeState(false); // 暂停示波器
                changeClockSelect(true); // 切换到时钟输入
                startScheduledDetection(true); // 启动输入方向定时检测
            }
        }

        @Override // 覆写取消点击回调
        public void onCancelClick(View view, Object cancelData) { // 点击取消
            if (cancelData == null || view == null) return; // 数据为空则返回
            if (view.getId() == R.id.trigger) { // 触发对话框取消
                //do nothing
            } else if (view.getId() == R.id.clock) { // 时钟对话框取消
                changeClockSelect(false); // 切换到时钟输出
                stopDetection(); // 停止定时检测
                setClockInOut(false); // 设置时钟输出
            }
        }

        @Override // 覆写对话框关闭回调
        public void onDialogClose(View view) { // 对话框关闭
            if (view == null) return; // 视图为空则返回
            if (view.getId() == R.id.clock // 时钟对话框关闭
                    || view.getId() == R.id.trigger) { // 或触发对话框关闭
                hideDialog(); // 隐藏对话框
            }
        }
    };

    /** 示波器运行状态标记 */
    boolean bScopeState = false; // 示波器运行状态标记
    /** 缓存标记 */
    boolean bCache = false; // 缓存标记

    /**
     * 设置示波器运行/停止状态。
     * <p>
     * 受cacheRunStop影响：若缓存标记为停止(2)，则不自动恢复运行。
     *
     * @param b true=运行，false=停止
     */
    private void setScopeState(boolean b){ // 设置示波器运行/停止状态
        Scope scope = Scope.getInstance(); // 获取示波器实例
        if(b){ // 请求运行
            if(!scope.isRun()) { // 当前未运行
                if(cacheRunStop  != 2) { // 缓存标记不是停止状态
                    FPGACommand.getInstance().resume(); // FPGA恢复采样
                    Command.get().getFunctionMenu().Run(true); // 发送运行命令
                }
            }
        }else{ // 请求停止
            if(scope.isRun()) { // 当前正在运行
                Command.get().getFunctionMenu().Stop(true); // 发送停止命令
            }
        }
        bScopeState = b; // 更新运行状态标记
    }

    /**
     * 获取示波器当前运行状态。
     *
     * @return true=运行中
     */
    private boolean isScopeRun(){ // 获取示波器运行状态
        return  bScopeState; // 返回运行状态标记
    }

    /**
     * 设置时钟输入/输出方向，并检测硬件反馈状态。
     * <p>
     * 设置后等待200ms，读取FPGA状态判断信号是否正常：
     * <ul>
     *   <li>正常：恢复示波器运行</li>
     *   <li>异常：启动定时检测</li>
     * </ul>
     *
     * @param isIn true=输入，false=输出
     */
    private void setClockInOut(boolean isIn) { // 设置时钟输入输出方向
        setScopeState(false); // 先暂停示波器
        Sample.getInstance().setClkInOut(isIn); // 设置时钟输入输出方向
        ms_sleep(200); // 等待200ms硬件响应
        boolean isInOutStatus = Scope.getInstance().getFpgaClockInOutStatus(isIn); // 获取FPGA时钟状态
        Logger.d("limh", "setClockIsIn= " + isIn + ",getInOutStatus= " + isInOutStatus); // 打印状态日志
        if (isInOutStatus) { // 时钟信号正常
            if(!isScopeRun()){ // 示波器未运行
                setScopeState(true); // 恢复示波器运行
            }
            if (!isIn) stopDetection(); // 输出方向则停止检测
        } else { // 时钟信号异常
            startScheduledDetection(isIn); // 启动定时检测
        }
    }

    /**
     * 线程休眠工具方法。
     *
     * @param ms 休眠毫秒数
     */
    void ms_sleep(long ms){ // 线程休眠
        try { // 尝试休眠
            Thread.sleep(ms); // 执行休眠
        } catch (InterruptedException e) { // 捕获中断异常
            e.printStackTrace(); // 打印异常堆栈
        }
    }

}
