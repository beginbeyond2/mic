package com.micsig.tbook.tbookscope.top.layout.auto; // 自动设置布局Fragment所在包

import android.content.Context; // 导入上下文类，用于访问系统服务
import android.os.Bundle; // 导入Bundle类，用于保存和恢复Fragment状态
import android.view.LayoutInflater; // 导入布局填充器，用于加载XML布局
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.TextView; // 导入文本视图控件

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.scope.Auto.Auto; // 导入自动功能核心控制类
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载消息类
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity类
import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件，用于发送硬件指令
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令UI消息类，用于接收硬件回调
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入响应式消息总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入消息枚举定义
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入按键音效工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情消息发送监听器接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard; // 导入数字键盘弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.MSwitchBox; // 导入自定义开关控件
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道选项数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组控件
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：自动设置(Auto Set)详情页面Fragment                                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：提供自动设置界面的交互逻辑，包括通道开关、触发源、电平选择和电平值设置  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：Fragment子页面，嵌入TopLayoutAuto中，采用观察者模式响应缓存加载和命令 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：UI控件 → TopMsgAutoSet → OnDetailSendMsgListener → TopLayoutAuto │
 * │           RxBus(缓存/命令) → Consumer → UI更新 → Command → 硬件            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：TopMsgAutoSet(数据模型)、Command(指令)、Auto(控制)、CacheUtil(缓存)│
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：用户在顶部面板选择"自动设置"时展示此页面，配置自动触发参数           │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * Created by Administrator on 2017/4/11.
 */
public class TopLayoutAutoSet extends Fragment { // 自动设置详情页面Fragment
    private static final String TAG = "TopLayoutAutoSet"; // 日志标签

    private Context context; // 上下文对象，用于访问系统服务和资源
    private MSwitchBox rgOpenChannel; // 通道开启开关控件
    private TopViewRadioGroup rgTriggerSource; // 触发源单选组控件
    private TopViewRadioGroup rgLevelSelect; // 电平单位选择单选组控件（V/mV）
    private TextView tvLevelDetail; // 电平详细值文本显示控件
    private TopDialogTextKeyBoard dialogTextKeyBoard; // 数字键盘弹窗，用于输入电平值

    private TopMsgAutoSet msgAutoDetail; // 自动设置详情数据模型
    private OnDetailSendMsgListener onDetailSendMsgListener; // 详情消息发送监听器

    /**
     * 创建Fragment视图，加载布局文件
     *
     * @param inflater  布局填充器
     * @param container 父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的视图
     */
    @Nullable // 标注返回值可为空
    @Override // 覆写Fragment的onCreateView方法
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建视图
        return inflater.inflate(R.layout.layout_autosetting, container, false); // 加载自动设置布局文件
    }

    /**
     * 视图创建完成后的初始化操作
     *
     * @param view               Fragment的根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override // 覆写Fragment的onViewCreated方法
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        context = getActivity(); // 获取Activity上下文
        initView(view); // 初始化视图控件
        initData(); // 初始化数据模型
        initControl(); // 初始化事件监听和消息订阅
    }

    /**
     * 初始化数据模型，将UI控件当前状态同步到数据模型
     */
    private void initData() { // 初始化数据
        msgAutoDetail = new TopMsgAutoSet(); // 创建自动设置详情数据模型
        msgAutoDetail.setOpenChannel(rgOpenChannel.isState()); // 同步通道开启状态到数据模型
        msgAutoDetail.setTriggerSource(rgTriggerSource.getSelected()); // 同步触发源选中项到数据模型
        msgAutoDetail.setLevelSelect(rgLevelSelect.getSelected()); // 同步电平选择选中项到数据模型
        msgAutoDetail.setLevelDetail(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_LEVELDETAIL)); // 从缓存读取电平详细值并同步
    }

    /**
     * 初始化视图控件，绑定布局中的UI元素和事件监听器
     *
     * @param view Fragment的根视图
     */
    private void initView(View view) { // 初始化视图控件
        rgOpenChannel = (MSwitchBox) view.findViewById(R.id.openChannelDetail); // 查找通道开启开关控件
        rgOpenChannel.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置开关状态变化监听器
        rgTriggerSource = (TopViewRadioGroup) view.findViewById(R.id.triggerSource); // 查找触发源单选组控件
        rgTriggerSource.setData(R.string.autoSettingTriggerSource, R.array.autoSettingTriggerSource, onCheckChangeListener); // 设置触发源数据（标题、选项数组、监听器）
        rgLevelSelect = (TopViewRadioGroup) view.findViewById(R.id.levelSelect); // 查找电平选择单选组控件
        rgLevelSelect.setData(null, getResources().getStringArray(R.array.autoSettingLevelSelect), onCheckChangeListener); // 设置电平选择数据（无标题、选项数组、监听器）
        tvLevelDetail = (TextView) view.findViewById(R.id.levelDetail); // 查找电平详细值文本控件
        tvLevelDetail.setOnClickListener(onClickListener); // 设置电平值点击监听器，点击弹出键盘

        dialogTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard); // 从主Activity获取数字键盘弹窗
    }

    /**
     * 初始化消息订阅，监听缓存加载和硬件命令回调
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载消息
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅硬件命令回调消息
    }

    /**
     * 从缓存恢复UI状态，同步到硬件和数据模型
     */
    private void setCache() { // 从缓存恢复状态
        int channel = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_CHANNEL); // 读取通道开启缓存值（0=开启，1=关闭）
        int source = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_SOURCE); // 读取触发源索引缓存值
        int levelSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_LEVELSELECT); // 读取电平选择索引缓存值
        int levelmV = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_LEVELDETAIL); // 读取电平详细值缓存值（单位mV）

        rgOpenChannel.setState(channel == 0); // 设置通道开关状态（0=开启）
        rgTriggerSource.setSelectedIndex(source); // 设置触发源选中索引
        rgLevelSelect.setSelectedIndex(levelSelect); // 设置电平选择选中索引
        if (levelSelect == 0) { // 电平单位为V时
            tvLevelDetail.setText(getVFromMV()); // 将mV转换为V显示
        } else { // 电平单位为mV时
            tvLevelDetail.setText(String.valueOf(levelmV)); // 直接显示mV值
        }
        if (channel == 1) { // 通道关闭时
            rgLevelSelect.setEnabled(false); // 禁用电平选择控件
            tvLevelDetail.setEnabled(false); // 禁用电平值文本控件
            tvLevelDetail.setTextColor(getResources().getColor(R.color.textColorNewTopViewDisable)); // 设置禁用态文字颜色
//            tvLevelDetail.setBackgroundResource(R.drawable.bg_edittext_unclickable); // 被注释掉的调试代码
        } else { // 通道开启时
            rgLevelSelect.setEnabled(true); // 启用电平选择控件
            tvLevelDetail.setEnabled(true); // 启用电平值文本控件
//            tvLevelDetail.setBackgroundResource(R.drawable.bg_edittext); // 被注释掉的调试代码
            tvLevelDetail.setTextColor(getResources().getColor(R.color.textColor)); // 设置正常态文字颜色
        }

        Command.get().getAuto().setSource(source, false); // 通过命令中间件设置触发源（不发送到硬件）
        Command.get().getAuto().setLevel(Double.parseDouble(getVFromMV()), false); // 通过命令中间件设置电平值（不发送到硬件）
        Command.get().getAuto().setChannel(channel == 0, false); // 通过命令中间件设置通道状态（不发送到硬件）

        Auto.getInstance().setAutoChannelEnable(channel == 0); // 设置自动功能通道使能状态
        Auto.getInstance().setAutoTriggerSource(source); // 设置自动功能触发源
        Auto.getInstance().setAutoThresholdLevel(Float.parseFloat(getVFromMV())); // 设置自动功能阈值电平

        msgAutoDetail.setOpenChannel(rgOpenChannel.isState()); // 同步通道状态到数据模型
        msgAutoDetail.setTriggerSource(rgTriggerSource.getSelected()); // 同步触发源到数据模型
        msgAutoDetail.setLevelSelect(rgLevelSelect.getSelected()); // 同步电平选择到数据模型
        msgAutoDetail.setLevelDetail(levelmV); // 同步电平详细值到数据模型
        sendMsg(false); // 发送消息通知父级（非EventBus来源）
    }

    /**
     * 将mV值转换为V字符串显示
     * 例如：1000mV → "1"，1500mV → "1.500"
     *
     * @return 转换后的V值字符串
     */
    private String getVFromMV() { // 将mV转换为V字符串
        int levelmV = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_LEVELDETAIL); // 从缓存读取电平值（mV）

        String levelV = String.valueOf(levelmV / 1000); // 计算整数部分（V）
        if (levelmV % 1000 != 0) { // 存在小数部分
            String number = String.valueOf(levelmV % 1000); // 获取小数部分的mV值
            while (number.length() < 3) { // 补齐到3位小数
                number = "0" + number; // 前面补零
            }
            levelV = levelV + "." + number; // 拼接整数和小数部分
        }
        return levelV; // 返回V值字符串
    }

    /**
     * 获取自动设置详情数据模型
     *
     * @return 自动设置详情数据模型实例
     */
    public TopMsgAutoSet getMsgAutoDetail() { // 获取自动设置详情数据模型
        return msgAutoDetail; // 返回数据模型
    }

    /**
     * 设置详情消息发送监听器
     *
     * @param onDetailSendMsgListener 详情消息发送监听器实例
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置详情消息发送监听器
        this.onDetailSendMsgListener = onDetailSendMsgListener; // 赋值监听器引用
    }

    /**
     * 通过监听器发送消息通知父级页面
     *
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void sendMsg(boolean isFromEventBus) { // 发送消息通知父级
        if (onDetailSendMsgListener != null) { // 监听器不为空
            onDetailSendMsgListener.onClick(this, isFromEventBus); // 调用监听器回调
        }
    }

    /**
     * 缓存加载消息消费者，当收到缓存加载事件时恢复UI状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消息消费者
        @Override // 覆写Consumer的accept方法
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 处理缓存加载消息
            setCache(); // 从缓存恢复UI状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutAutoSet, true); // 标记本页面缓存已加载完成
        }
    };

    /**
     * 硬件命令回调消息消费者，处理来自硬件的自动设置参数变更通知
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令回调消息消费者
        @Override // 覆写Consumer的accept方法
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 处理命令回调消息
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发处理
                case CommandMsgToUI.FLAG_AUTO_CHANNEL: // 通道状态变更命令
                    int channel = Integer.parseInt(commandMsgToUI.getParam()); // 解析通道参数
                    if (rgOpenChannel.isState() != (channel == 0)) { // 当前UI状态与命令不一致
                        rgOpenChannel.setState(channel == 0); // 更新通道开关状态
                        onToggleStateChangedListener.onToggleStateChanged(rgOpenChannel, rgOpenChannel.isState()); // 触发开关状态变化回调
                    }
                    break; // 结束通道处理
                case CommandMsgToUI.FLAG_AUTO_LEVEL: // 电平值变更命令
                    int param = (int) (Double.parseDouble(commandMsgToUI.getParam()) * 1000); // 将V值转换为mV
                    if (param<1) param=1; // 限制最小值为1mV
                    if (param>99999) param=99999; // 限制最大值为99999mV
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SET_LEVELDETAIL,String.valueOf(param)); // 更新电平值缓存
                    if (rgLevelSelect.getSelected().getIndex() == 0) { // 电平单位为V时
                        tvLevelDetail.setText(getVFromMV()); // 转换为V显示
                    } else { // 电平单位为mV时
                        tvLevelDetail.setText(String.valueOf(param)); // 直接显示mV值
                    }
                    onTextChanged(tvLevelDetail, tvLevelDetail.getText().toString(), false); // 触发电平值变化处理
                    break; // 结束电平处理
                case CommandMsgToUI.FLAG_AUTO_SOURCE: // 触发源变更命令
                    Integer sourceIndex = Integer.valueOf(commandMsgToUI.getParam()); // 解析触发源索引
                    if (rgTriggerSource.getSelected().getIndex() != sourceIndex) { // 当前UI状态与命令不一致
                        rgTriggerSource.setSelectedIndex(sourceIndex); // 更新触发源选中索引
                        onCheckChanged(rgTriggerSource, rgTriggerSource.getSelected(), false); // 触发选中项变化回调
                    }
                    break; // 结束触发源处理
            }
        }
    };

    /**
     * 通道开关状态变化监听器，处理通道开启/关闭的联动逻辑
     */
    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() { // 开关状态变化监听器
        @Override // 覆写onToggleStateChanged方法
        public void onToggleStateChanged(MSwitchBox view, boolean state) { // 开关状态变化回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            boolean isFromEventBus = false; // 标记非EventBus来源
            if (view.getId() == R.id.openChannelDetail) { // 判断是通道开关控件
                if (!state) { // 通道关闭
                    rgLevelSelect.setEnabled(false); // 禁用电平选择控件
                    tvLevelDetail.setEnabled(false); // 禁用电平值文本控件
                    tvLevelDetail.setTextColor(getResources().getColor(R.color.textColorNewTopViewDisable)); // 设置禁用态文字颜色
//                    tvLevelDetail.setBackgroundResource(R.drawable.bg_edittext_unclickable); // 被注释掉的调试代码
                } else { // 通道开启
                    rgLevelSelect.setEnabled(true); // 启用电平选择控件
                    tvLevelDetail.setEnabled(true); // 启用电平值文本控件
                    tvLevelDetail.setTextColor(getResources().getColor(R.color.textColor)); // 设置正常态文字颜色
//                    tvLevelDetail.setBackgroundResource(R.drawable.bg_edittext); // 被注释掉的调试代码
                }

                Command.get().getAuto().setChannel(state, false); // 通过命令中间件设置通道状态（不发送到硬件）
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SET_CHANNEL, String.valueOf(state ? 0 : 1)); // 更新通道缓存值
                if (!isFromEventBus) { // 非EventBus来源
                    Auto.getInstance().setAutoChannelEnable(state); // 设置自动功能通道使能
                }
                msgAutoDetail.setOpenChannel(state); // 更新数据模型中的通道状态
                sendMsg(isFromEventBus); // 发送消息通知父级
            }
        }
    };

    /**
     * 单选组选中项变化监听器，处理触发源和电平选择的切换逻辑
     */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangeListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选组变化监听器
        @Override // 覆写onClickSound方法
        public void onClickSound(boolean isCheckedSuccess) { // 选中音效回调
            PlaySound.getInstance().playButton(); // 播放按键音效
        }

        @Override // 覆写onPrompt方法
        public void onPrompt(TopViewRadioGroup view) { // 提示回调（未使用）

        }

        @Override // 覆写onClick方法
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 选中项点击回调
            onCheckChanged(view, item, false); // 委托给onCheckChanged处理
        }
    };

    /**
     * 电平值文本点击监听器，点击后弹出数字键盘输入电平值
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 点击监听器
        @Override // 覆写onClick方法
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            int levelMV = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_LEVELDETAIL); // 从缓存读取当前电平值（mV）
            if (rgLevelSelect.getSelected().getIndex() == 0) { // 电平单位为V时
                dialogTextKeyBoard.setDataDouble(Double.parseDouble(getVFromMV()), 2, 3, onDialogDismissListener); // 弹出双精度键盘（2位整数，3位小数）
            } else { // 电平单位为mV时
                dialogTextKeyBoard.setData(String.valueOf(levelMV), TopDialogTextKeyBoard.INPUT_TYPE_NUMBER_INT, 5, onDialogDismissListener); // 弹出整数键盘（最多5位）
            }
        }
    };

    /**
     * 数字键盘弹窗关闭监听器，键盘关闭后更新电平值
     */
    private TopDialogTextKeyBoard.OnDialogDismissListener onDialogDismissListener = new TopDialogTextKeyBoard.OnDialogDismissListener() { // 键盘弹窗关闭监听器
        @Override // 覆写onDismiss方法
        public void onDismiss(String result) { // 弹窗关闭回调
            onTextChanged(tvLevelDetail, result, false); // 将键盘输入结果更新到电平值
        }
    };

    /**
     * 单选组选中项变化处理，处理触发源和电平选择的业务逻辑
     *
     * @param view           触发变化的单选组视图
     * @param item           新选中的选项数据
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) { // 选中项变化处理
        if (view.getId() == R.id.triggerSource) { // 触发源变化
            Command.get().getAuto().setSource(item.getIndex(), false); // 通过命令中间件设置触发源（不发送到硬件）
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SET_SOURCE, String.valueOf(item.getIndex())); // 更新触发源缓存
            if (!isFromEventBus) { // 非EventBus来源
                Auto.getInstance().setAutoTriggerSource(item.getIndex()); // 设置自动功能触发源
            }
            msgAutoDetail.setTriggerSource(item); // 更新数据模型中的触发源
            sendMsg(isFromEventBus); // 发送消息通知父级
        } else if (view.getId() == R.id.levelSelect) { // 电平选择变化
            //切换时，只是改变了数据的单位，实际数据并没有变化
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SET_LEVELSELECT, String.valueOf(item.getIndex())); // 更新电平选择缓存
            int levelMV = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO_SET_LEVELDETAIL); // 从缓存读取电平值（mV）
            if (item.getIndex() == 0) { // 切换到V单位
                tvLevelDetail.setText(getVFromMV()); // 转换为V显示
            } else { // 切换到mV单位
                tvLevelDetail.setText(String.valueOf(levelMV)); // 直接显示mV值
            }
        }
    }

    /**
     * 电平值文本变化处理，更新缓存、硬件和数据模型
     *
     * @param tv             文本视图控件
     * @param result         新的文本值
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onTextChanged(TextView tv, String result, boolean isFromEventBus) { // 文本变化处理
        if (tv.getId() == tvLevelDetail.getId()) { // 判断是电平值文本控件
            if (StrUtil.isEmpty(result)) { // 输入为空
                result = "0"; // 默认设为0
            }
            double d = Double.parseDouble(result); // 将输入解析为双精度数
            int levelMV; // 电平值（mV）
            if (rgLevelSelect.getSelected().getIndex() == 0) { // 电平单位为V时
                levelMV = (int) (d * 1000); // 将V转换为mV
            } else { // 电平单位为mV时
                levelMV = (int) d; // 直接取整
            }
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_AUTO_SET_LEVELDETAIL, String.valueOf(levelMV)); // 更新电平值缓存
            Command.get().getAuto().setLevel(Double.parseDouble(getVFromMV()), false); // 通过命令中间件设置电平值（不发送到硬件）
            if (!isFromEventBus) { // 非EventBus来源
                Auto.getInstance().setAutoThresholdLevel(Float.parseFloat(getVFromMV())); // 设置自动功能阈值电平
            }
            if (rgLevelSelect.getSelected().getIndex() == 0) { // 电平单位为V时
                tvLevelDetail.setText(getVFromMV()); // 转换为V显示
            } else { // 电平单位为mV时
                tvLevelDetail.setText(String.valueOf(levelMV)); // 直接显示mV值
            }
            msgAutoDetail.setLevelDetail(levelMV); // 更新数据模型中的电平详细值
            sendMsg(isFromEventBus); // 发送消息通知父级
        }
    }
}
