// ============================================================
//  模块定位：sample（采样功能模块）
//  文件路径：top/layout/sample/TopLayoutSampleSegmented.java
//  核心职责：分段存储子页面Fragment，管理分段开关/数量/自定义/显示模式/拟合帧/排序等配置
//  架构设计：Fragment + RxBus观察者模式 + EventFactory事件观察者，通过Consumer订阅多种事件
//  数据流向：用户配置分段参数 → onCheckChanged/onTextChanged → Command发送硬件指令 → SegmentSample更新
//  依赖关系：依赖MSwitchBox/TopViewRadioGroup控件、SegmentSample分段存储、Command硬件指令、CacheUtil缓存
//  使用场景：采样功能分段存储子页面，用户配置分段存储的开关、段数、显示模式、拟合帧范围、排序
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.sample; // 声明该类所属的包路径

import android.annotation.SuppressLint; // 导入注解，抑制SetTextI18n警告
import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle类
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.TextView; // 导入文本视图控件

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入UI事件观察者
import com.micsig.tbook.scope.Sample.SegmentSample; // 导入分段存储类
import com.micsig.tbook.scope.Scope; // 导入示波器核心类
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载消息
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity
import com.micsig.tbook.tbookscope.R; // 导入资源ID
import com.micsig.tbook.tbookscope.main.mainbottom.MainBottomMsgTimeBase; // 导入底部时基消息
import com.micsig.tbook.tbookscope.main.maincenter.MainMsgCenterSegmented; // 导入中间分段消息
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels; // 导入右侧通道消息
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers; // 导入右侧其他消息
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令消息到UI
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgMath; // 导入数学通道消息
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 导入数字位数接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具
import com.micsig.tbook.tbookscope.util.Screen; // 导入屏幕工具
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage; // 导入测量管理
import com.micsig.tbook.ui.MSwitchBox; // 导入开关控件
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具
import com.micsig.tbook.ui.util.TBookUtil; // 导入示波器工具

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入消费者接口


/**
 * 分段存储Fragment - 管理分段存储的所有配置参数
 */
public class TopLayoutSampleSegmented extends Fragment { // 继承Fragment，分段存储子页面
    /** 临时最大分段数量（调试用） */
    private static final int TEMP_MAX_SEGMENT_NUMS = 30; // 临时最大分段数

    private Context context; // 上下文对象
    /** 分段存储开关 */
    private MSwitchBox rgState; // 分段开关
    /** 分段数量单选组 */
    private TopViewRadioGroup rgNumber; // 分段数量选择
    /** 自定义分段数量文本 */
    private TextView tvUserDefine; // 自定义数量文本
    /** 分段显示模式单选组 */
    private TopViewRadioGroup rgDisplay; // 显示模式选择
    /** 拟合起始帧文本 */
    private TextView tvFitStart; // 拟合起始帧
    /** 拟合结束帧文本 */
    private TextView tvFitEnd; // 拟合结束帧
    /** 分段排序单选组 */
    private TopViewRadioGroup rgOrder; // 排序选择

    /** 数字键盘弹窗 */
    private TopDialogNumberKeyBoard dialogNumberKeyBoard; // 数字键盘弹窗

    /** 分段存储消息对象 */
    private TopMsgSampleSegmented msgSampleSegmented; // 分段存储消息

    /**
     * 创建Fragment视图
     * @param inflater 布局填充器
     * @param container 父容器
     * @param savedInstanceState 保存的状态
     * @return 填充后的视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建视图
        return inflater.inflate(R.layout.layout_sample_segmented, container, false); // 填充分段存储布局
    }

    /**
     * 视图创建完成后的初始化
     * @param view 根视图
     * @param savedInstanceState 保存的状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        initView(view); // 初始化视图
        initData(); // 初始化数据
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化视图组件
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图方法
        rgState = (MSwitchBox) view.findViewById(R.id.segmentedState); // 获取分段开关
        rgNumber = (TopViewRadioGroup) view.findViewById(R.id.segmentedNumber); // 获取分段数量单选组
        tvUserDefine = (TextView) view.findViewById(R.id.segmentedUsersetNumber); // 获取自定义数量文本
        rgDisplay = (TopViewRadioGroup) view.findViewById(R.id.segmentedDisplay); // 获取显示模式单选组
        tvFitStart = (TextView) view.findViewById(R.id.segmentedFitStart); // 获取拟合起始帧文本
        tvFitEnd = (TextView) view.findViewById(R.id.segmentedFitEnd); // 获取拟合结束帧文本
        rgOrder = (TopViewRadioGroup) view.findViewById(R.id.segmentedOrder); // 获取排序单选组

        dialogNumberKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard); // 获取数字键盘弹窗

        rgState.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置分段开关监听

        rgNumber.setData(R.string.sampleSegmentedNumber, R.array.sampleSegmentedNumber, onCheckChangedListener); // 设置分段数量选项
        tvUserDefine.setOnClickListener(onClickListener); // 设置自定义数量点击监听
        rgDisplay.setData(R.string.sampleSegmentedDisplay, R.array.sampleSegmentedDisplay, onCheckChangedListener); // 设置显示模式选项
        tvFitStart.setOnClickListener(onClickListener); // 设置拟合起始帧点击监听
        tvFitEnd.setOnClickListener(onClickListener); // 设置拟合结束帧点击监听
        rgOrder.setData(R.string.sampleSegmentedOrder, R.array.sampleSegmentedOrder, onCheckChangedListener); // 设置排序选项
    }

    /**
     * 初始化消息对象数据
     */
    private void initData() { // 初始化数据
        msgSampleSegmented = new TopMsgSampleSegmented(); // 创建分段存储消息
        msgSampleSegmented.setState(rgState.isState()); // 设置开关状态
        msgSampleSegmented.setNumber(rgNumber.getSelected()); // 设置数量选择
        msgSampleSegmented.setUserDefine(tvUserDefine.getText().toString()); // 设置自定义数量
        msgSampleSegmented.setDisplay(rgDisplay.getSelected()); // 设置显示模式
        msgSampleSegmented.setStart(tvFitStart.getText().toString()); // 设置起始帧
        msgSampleSegmented.setEnd(tvFitEnd.getText().toString()); // 设置结束帧
        msgSampleSegmented.setOrder(rgOrder.getSelected()); // 设置排序
    }

    /**
     * 初始化RxBus事件订阅和EventFactory观察者
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令到UI事件
        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_SEGMENTED).subscribe(consumerCenterLayoutSegmented); // 订阅中间分段消息
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLEDEPTH).subscribe(consumerSampleDepth); // 订阅记录长度消息
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels); // 订阅通道变更事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers); // 订阅右侧其他事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_MATH).subscribe(consumerRightMath); // 订阅数学通道事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE).subscribe(consumerSegmentedState); // 订阅分段状态事件
        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_TIMEBASE).subscribe(consumerMainBottomTimeBase); // 订阅时基变更事件
        EventFactory.addEventObserver(EventFactory.EVENT_SEGMENT_FRAMES, eventUIObserver); // 注册分段帧数事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_SEGMENT_TIMESTAMP, eventUIObserver); // 注册分段时间戳事件观察者
    }

    /**
     * 从缓存恢复分段存储状态
     */
    private void setCache() { // 从缓存恢复状态
        int state = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE); // 读取分段开关状态
        int number = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER); // 读取分段数量索引
        int userDefine = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER_USERDEFINE); // 读取自定义数量
        int display = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY); // 读取显示模式索引
        int fitStart = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_START); // 读取拟合起始帧
        int fitEnd = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_END); // 读取拟合结束帧
        int order = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_ORDER); // 读取排序索引

        rgState.setState(state==0); // 设置分段开关状态（0=开启）
        rgNumber.setSelectedIndex(number); // 设置分段数量选中项
        rgDisplay.setSelectedIndex(display); // 设置显示模式选中项
        tvFitStart.setText(String.valueOf(fitStart)); // 设置拟合起始帧文本
        tvFitEnd.setText(String.valueOf(fitEnd)); // 设置拟合结束帧文本
        rgOrder.setSelectedIndex(order); // 设置排序选中项
        setNumberDetail(); // 更新自定义数量显示
        setDisplayDetailVisible(); // 更新拟合帧/排序可见性

        SegmentSample segment = SegmentSample.getInstance(); // 获取分段存储实例
        segment.setSegmentEnable(rgState.isState()); // 设置分段启用状态

        segment.setSegmentNums(getSegmentNums()); // 设置分段数量
        segment.setSegmentDisplayType(rgDisplay.getSelected().getIndex()); // 设置显示模式
        segment.setFittingBegingFrame(fitStart - 1); // 设置拟合起始帧（0基索引）
        segment.setFittingEndFrame(fitEnd - 1); // 设置拟合结束帧（0基索引）

        msgSampleSegmented.setState(rgState.isState()); // 更新消息开关状态
        msgSampleSegmented.setNumber(rgNumber.getSelected()); // 更新消息数量
        msgSampleSegmented.setUserDefine(tvUserDefine.getText().toString()); // 更新消息自定义数量
        msgSampleSegmented.setDisplay(rgDisplay.getSelected()); // 更新消息显示模式
        msgSampleSegmented.setStart(tvFitStart.getText().toString()); // 更新消息起始帧
        msgSampleSegmented.setEnd(tvFitEnd.getText().toString()); // 更新消息结束帧
        msgSampleSegmented.setOrder(rgOrder.getSelected()); // 更新消息排序
        sendMsg(false); // 发送分段存储消息

//        RxBus.getInstance().post(RxEnum.MAINCENTER_SEGMENTED_VISIBLE, rgState.getSelected().getIndex() == 0);
        showSegment(); // 显示/隐藏分段测量
    }

    /**
     * 显示或隐藏分段测量指示器
     */
    private void showSegment() { // 显示/隐藏分段测量
        MeasureManage.SegmentMeasure segmentMeasure = MeasureManage.getInstance().getSegmentMeasure(); // 获取分段测量对象
        segmentMeasure.setVisible(rgState.isState()); // 设置可见性
    }

    /**
     * 事件观察者 - 监听分段帧数和时间戳变更事件
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() { // 事件观察者
        @SuppressLint("DefaultLocale")
        @Override
        public void update(Object data) { // 事件更新回调
            int eventId = ((EventBase) data).getId(); // 获取事件ID
            if (eventId == EventFactory.EVENT_SEGMENT_FRAMES) { // 分段帧数事件
                MeasureManage.SegmentMeasure segmentMeasure = MeasureManage.getInstance().getSegmentMeasure(); // 获取分段测量
                SegmentSample segmentSample = SegmentSample.getInstance(); // 获取分段存储
                String strTxt = ""; // 显示文本
                if (Scope.getInstance().isRun()) { // 如果示波器正在运行
                    int nums = segmentSample.getSegmentNums(); // 获取分段数量
                    if (nums > segmentSample.getMaxSegmentNums()) // 如果超过最大值
                        nums = segmentSample.getMaxSegmentNums(); // 限制为最大值
                    strTxt = "" + segmentSample.getSegmentFrames() + "/" + nums; // 显示当前帧/总帧数
                } else { // 示波器停止
                    Scope scope = Scope.getInstance(); // 获取示波器实例
                    int nums = scope.getSegmentFrameNums(); // 获取总帧数
                    int val = (scope.getSegmentFrameNo() + 1); // 当前帧号（1基）
                    if (nums <= 0) val = 0; // 无帧时显示0
                    strTxt = "" + val + "/" + nums; // 显示当前帧/总帧数
                }
                segmentMeasure.setText(strTxt); // 更新分段测量文本
            } else if (eventId == EventFactory.EVENT_SEGMENT_TIMESTAMP) { // 分段时间戳事件
//                Logger.i(Command.TAG,"nums:"+Scope.getInstance().getSegmentFrameNums());
                Scope scope = Scope.getInstance(); // 获取示波器实例

                setBeginFrame(0); // 重置起始帧为1
                setEndFrame(scope.getSegmentFrameNums()); // 设置结束帧为总帧数
                sendMsg(true); // 发送消息（EventBus来源）
            }
        }
    };


    /**
     * 获取分段数量
     * @return 分段数量值
     */
    private int getSegmentNums() { // 获取分段数量
        int segmentNum = 100; // 默认100段
        SegmentSample segmentSample = SegmentSample.getInstance(); // 获取分段存储实例
        segmentSample.setEnableMaxSegment(false); // 禁用最大分段
        switch (rgNumber.getSelected().getIndex()) { // 根据数量选择索引
            case 0: // 100段
                segmentNum = 100; // 设置100段
                break;
            case 1: // 1000段
                segmentNum = 1000; // 设置1000段
                break;
            case 2: // 最大段数
                segmentSample.setEnableMaxSegment(true); // 启用最大分段
                segmentNum = segmentSample.getMaxSegmentNums(); // 获取最大段数
                break;
            case 3: // 自定义
                segmentNum = Integer.parseInt(tvUserDefine.getText().toString()); // 解析自定义数量
                break;
        }
        return segmentNum; // 返回分段数量
    }

    /**
     * 设置自定义数量文本的显示和启用状态
     */
    private void setNumberDetail() { // 设置自定义数量详情
        int userDefine = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER_USERDEFINE); // 读取自定义数量缓存
        if (rgNumber.getSelected().getIndex() == 3) { // 如果选了自定义
            tvUserDefine.setText(String.valueOf(userDefine)); // 显示自定义数量
            tvUserDefine.setEnabled(true); // 启用编辑
        } else { // 非自定义
            tvUserDefine.setText("---"); // 显示占位符
            tvUserDefine.setEnabled(false); // 禁用编辑
        }
    }

    /**
     * 根据显示模式设置拟合帧和排序的可见性
     */
    private void setDisplayDetailVisible() { // 设置显示详情可见性
        if (rgDisplay.getSelected().getIndex() == 1) { // 拟合显示模式
            tvFitStart.setVisibility(View.VISIBLE); // 显示起始帧
            tvFitEnd.setVisibility(View.VISIBLE); // 显示结束帧
            rgOrder.setVisibility(View.INVISIBLE); // 隐藏排序
        } else { // 普通显示模式
            tvFitStart.setVisibility(View.INVISIBLE); // 隐藏起始帧
            tvFitEnd.setVisibility(View.INVISIBLE); // 隐藏结束帧
            rgOrder.setVisibility(View.VISIBLE); // 显示排序
        }
    }

    /**
     * 发送分段存储消息到RxBus
     * @param isFromEventBus 是否来自EventBus
     */
    private void sendMsg(boolean isFromEventBus) { // 发送分段存储消息
        msgSampleSegmented.setFromEventBus(isFromEventBus); // 设置来源标志
//        Logger.d("sendMsg() called with: TOPLAYOUT_SAMPLESEGMENTED = [" + msgSampleSegmented + "]");
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLESEGMENTED, msgSampleSegmented); // 通过RxBus发送消息
    }

    /**
     * 分段开关状态变更监听器
     */
    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() { // 分段开关监听器
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) { // 开关状态变更回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            onCheckChanged(view, state, false); // 触发变更处理
        }
    };

    /**
     * 缓存加载消费者 - 恢复分段存储状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 从缓存恢复状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSampleSegmented, true); // 标记分段存储页面缓存加载完成
        }
    };

    /**
     * 命令到UI消费者 - 处理硬件返回的分段存储命令
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令到UI消费者
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收命令消息
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发
                case CommandMsgToUI.FLAG_SAMPLE_IsOpenSegMent:{ // 分段开关命令
                    boolean isOpen = Boolean.parseBoolean(commandMsgToUI.getParam()); // 解析开关参数
                    rgState.setState(isOpen); // 设置开关状态
                    onCheckChanged(rgState, isOpen, false); // 触发变更处理
                }break;
                case CommandMsgToUI.FLAG_SAMPLE_SegmentedQTY:{ // 分段数量命令
                    int i=Integer.parseInt(commandMsgToUI.getParam()); // 解析数量参数
                    if (i==100){ // 100段
                        rgNumber.setSelectedIndex(0); // 选中100
                        onCheckChanged(rgNumber,rgNumber.getSelected(),true,false); // 触发变更
                    }else if (i==1000){ // 1000段
                        rgNumber.setSelectedIndex(1); // 选中1000
                        onCheckChanged(rgNumber,rgNumber.getSelected(),true,false); // 触发变更
                    }else{ // 其他数量
                        rgNumber.setSelectedIndex(3); // 选中自定义
                        if (i>10000) i=10000; // 限制最大10000
                        tvUserDefine.setText(String.valueOf(i)); // 更新自定义文本
                        onCheckChanged(rgNumber,rgNumber.getSelected(),true,false); // 触发数量变更
                        onTextChanged(tvUserDefine,String.valueOf(i),true,false); // 触发文本变更
                    }

                }break;
                case CommandMsgToUI.FLAG_SAMPLE_SegmentedDisplayType:{ // 显示模式命令
                    int index=Integer.parseInt(commandMsgToUI.getParam()); // 解析显示模式索引
                    rgDisplay.setSelectedIndex(index); // 设置选中项
                    onCheckChanged(rgDisplay,rgDisplay.getSelected(),true,false); // 触发变更
                }break;
                case CommandMsgToUI.FLAG_SAMPLE_SegmentedOrder:{ // 排序命令
                    int index=Integer.parseInt(commandMsgToUI.getParam()); // 解析排序索引
                    rgOrder.setSelectedIndex(index); // 设置选中项
                    onCheckChanged(rgOrder,rgOrder.getSelected(),true,false); // 触发变更
                }break;
                case CommandMsgToUI.FLAG_SAMPLE_SegmentedFra2:{ // 拟合起始帧命令
                    if (tvFitStart.getVisibility()==View.VISIBLE){ // 如果起始帧可见
                        int i=Integer.parseInt(commandMsgToUI.getParam()); // 解析帧号
                        tvFitStart.setText(String.valueOf(i)); // 更新起始帧文本
                        onTextChanged(tvFitStart,String.valueOf(i),true,false); // 触发文本变更
                    }
                }break;
                case CommandMsgToUI.FLAG_SAMPLE_SegmentedFra3:{ // 拟合结束帧命令
                    if (tvFitEnd.getVisibility()==View.VISIBLE){ // 如果结束帧可见
                        int i=Integer.parseInt(commandMsgToUI.getParam()); // 解析帧号
                        tvFitEnd.setText(String.valueOf(i)); // 更新结束帧文本
                        onTextChanged(tvFitEnd,String.valueOf(i),true,false); // 触发文本变更
                    }
                }break;
            }
        }
    };

    /**
     * 中间分段消息消费者 - 处理波形区分段选择变更
     */
    private Consumer<MainMsgCenterSegmented> consumerCenterLayoutSegmented = new Consumer<MainMsgCenterSegmented>() { // 中间分段消息消费者
        @Override
        public void accept(MainMsgCenterSegmented msgCenterSegmented) throws Exception { // 接收中间分段消息

            int display = msgCenterSegmented.isDisplay().isValue() ? 1 : 0; // 获取显示模式
            int order = msgCenterSegmented.isSingleOrder().isValue() ? 0 : 1; // 获取排序模式
            int fitStart = msgCenterSegmented.getFitStart().getFrameId(); // 获取拟合起始帧
            int fitEnd = msgCenterSegmented.getFitEnd().getFrameId(); // 获取拟合结束帧
            boolean changed = false; // 变更标志
            int maxSegmentNums = Scope.getInstance().getSegmentFrameNums(); // 获取最大帧数
            //int maxSegmentNums = TEMP_MAX_SEGMENT_NUMS;
            if (maxSegmentNums < 1) maxSegmentNums = 1; // 保证最少1帧
            if (fitStart > maxSegmentNums) fitStart = maxSegmentNums; // 限制起始帧
            if (fitEnd > maxSegmentNums) fitEnd = maxSegmentNums; // 限制结束帧
            if (rgDisplay.getSelected().getIndex() != display) { // 显示模式不同
                changed = true; // 标记变更
                rgDisplay.setSelectedIndex(display); // 更新显示模式
                onCheckChanged(rgDisplay, rgDisplay.getSelected(), false, false); // 触发变更
            }
            if (rgOrder.getSelected().getIndex() != order) { // 排序不同
//                Logger.d("accept() rgOrder.getSelected().getIndex() = [" + rgOrder.getSelected().getIndex() + "]");
                changed = true; // 标记变更
                rgOrder.setSelectedIndex(order); // 更新排序
                onCheckChanged(rgOrder, rgOrder.getSelected(), false, false); // 触发变更
            }
            if (StrUtil.isEmpty(tvFitStart.getText().toString()) || Integer.parseInt(tvFitStart.getText().toString()) != fitStart) { // 起始帧不同
                changed = true; // 标记变更
                tvFitStart.setText(String.valueOf(fitStart)); // 更新起始帧文本
                onTextChanged(tvFitStart, tvFitStart.getText().toString(), false, false); // 触发文本变更
            }
            if (StrUtil.isEmpty(tvFitEnd.getText().toString()) || Integer.parseInt(tvFitEnd.getText().toString()) != fitEnd) { // 结束帧不同
                changed = true; // 标记变更
                tvFitEnd.setText(String.valueOf(fitEnd)); // 更新结束帧文本
                onTextChanged(tvFitEnd, tvFitEnd.getText().toString(), false, false); // 触发文本变更
            }
            if (changed) { // 如果有变更
                sendMsg(false); // 发送分段存储消息
            }
        }
    };

    /**
     * 记录长度消息消费者 - 根据深度变更调整分段数量可用性
     */
    private Consumer<TopMsgSampleDepth> consumerSampleDepth = new Consumer<TopMsgSampleDepth>() { // 记录长度消息消费者
        @Override
        public void accept(TopMsgSampleDepth msgSampleDepth) throws Exception { // 接收记录长度消息
            setNumberEnable(msgSampleDepth.isFromEventBus()); // 更新分段数量可用性
        }
    };

    /**
     * 通道变更消费者 - 根据通道状态调整分段数量可用性
     */
    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() { // 通道变更消费者
        @Override
        public void accept(MainRightMsgChannels mainRightMsgChannels) throws Exception { // 接收通道变更消息
            if (mainRightMsgChannels.isChangeChState()) { // 如果通道状态有变化
                setNumberEnable(mainRightMsgChannels.isFromEventBus()); // 更新分段数量可用性
            }
        }
    };

    /**
     * 右侧其他消息消费者 - 根据数学通道状态调整分段数量可用性
     */
    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() { // 右侧其他消息消费者
        @Override
        public void accept(MainRightMsgOthers mainRightMsgOthers) throws Exception { // 接收右侧其他消息
            //TODO 数学多通道
            if (mainRightMsgOthers.getMath1().isRxMsgSelect()) { // 如果数学1通道被选中
                setNumberEnable(false); // 更新分段数量可用性
            }
        }
    };

    /**
     * 数学通道消息消费者 - 根据数学通道类型调整分段数量可用性
     */
    private Consumer<RightMsgMath> consumerRightMath = new Consumer<RightMsgMath>() { // 数学通道消息消费者
        @Override
        public void accept(RightMsgMath rightMsgMath) throws Exception { // 接收数学通道消息
            if (rightMsgMath.getMathType().isRxMsgSelect()) { // 如果数学类型被选中
                setNumberEnable(false); // 更新分段数量可用性
            }
        }
    };

    /**
     * 设置分段数量选项的可用性
     * @param isFromEventBus 是否来自EventBus
     */
    private void setNumberEnable(boolean isFromEventBus) { // 设置分段数量可用性
        if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_DEPTH) == 0) { // 如果深度为Auto
            rgNumber.setEnabled(true); // 全部启用
            sendMsg(isFromEventBus); // 发送消息
            return; // 返回
        }

        int maxSegmentNums = SegmentSample.getInstance().getMaxSegmentNums(); // 获取最大分段数
        if (maxSegmentNums < 1) maxSegmentNums = 1;//保证段数最少为1
        //int maxSegmentNums = TEMP_MAX_SEGMENT_NUMS;
        String[] array = context.getResources().getStringArray(R.array.sampleSegmentedNumber); // 获取分段数量选项数组
        if (maxSegmentNums < Integer.parseInt(array[0])) { // 最大分段数小于100
            if (rgNumber.getSelected().getIndex() == 0) { // 当前选了100
                rgNumber.setSelectedIndex(2); // 切换到最大
                onCheckChanged(rgNumber, rgNumber.getSelected(), false, isFromEventBus); // 触发变更
            }
            rgNumber.setEnabled(0, false); // 禁用100选项
        } else { // 最大分段数>=100
            rgNumber.setEnabled(0, true); // 启用100选项
        }
        if (maxSegmentNums < Integer.parseInt(array[1])) { // 最大分段数小于1000
            if (rgNumber.getSelected().getIndex() == 1) { // 当前选了1000
                rgNumber.setSelectedIndex(2); // 切换到最大
                onCheckChanged(rgNumber, rgNumber.getSelected(), false, isFromEventBus); // 触发变更
            }
            rgNumber.setEnabled(1, false); // 禁用1000选项
        } else { // 最大分段数>=1000
            rgNumber.setEnabled(1, true); // 启用1000选项
        }
        if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER_USERDEFINE) > maxSegmentNums) { // 自定义数量超限
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER_USERDEFINE, String.valueOf(maxSegmentNums)); // 限制为最大值
            setNumberDetail(); // 更新自定义数量显示
//                onTextChanged(tvUserDefine, String.valueOf(maxSegmentNums), false, msgSampleDepth.isFromEventBus());
        }

        if(!isFromEventBus) { // 非EventBus来源
            SegmentSample segmentSample = SegmentSample.getInstance(); // 获取分段存储实例
            if (segmentSample.isSegmentEnable()) { // 如果分段存储已启用
                segmentSample.setSegmentNums(getSegmentNums()); // 更新分段数量
            }
        }
        sendMsg(isFromEventBus); // 发送分段存储消息
    }

    /**
     * 分段状态消息消费者 - 根据XY/滚动/串行模式控制分段开关可用性
     */
    private Consumer<TopMsgSegmentedState> consumerSegmentedState = new Consumer<TopMsgSegmentedState>() { // 分段状态消息消费者
        @Override
        public void accept(TopMsgSegmentedState msgSegmentedState) throws Exception { // 接收分段状态消息
            boolean isXYMode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1; // 是否XY模式
            boolean isSerialsTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT); // 是否串行总线文本
            boolean isRoll = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ROLL) == 0 // 是否滚动模式
                    && Tools.isSlowTimeBase(); // 且为慢时基
            if (!isXYMode && !isSerialsTxt && !isRoll) { // 非XY/非串行/非滚动
                rgState.setEnabled(true); // 启用分段开关
                sendMsg(msgSegmentedState.isFromEventBus()); // 发送消息
            } else { // XY/串行/滚动模式
                if (rgState.isState()) { // 如果分段已开启
                    rgState.setState(false); // 关闭分段
                    onCheckChanged(rgState, rgState.isState(), msgSegmentedState.isFromEventBus()); // 触发变更
                }
                rgState.setEnabled(false); // 禁用分段开关
                sendMsg(msgSegmentedState.isFromEventBus()); // 发送消息
            }
        }
    };

    /**
     * 时基变更消费者 - 更新分段数量可用性和分段状态
     */
    private Consumer<MainBottomMsgTimeBase> consumerMainBottomTimeBase = new Consumer<MainBottomMsgTimeBase>() { // 时基变更消费者
        @Override
        public void accept(MainBottomMsgTimeBase msgTimeBase) throws Exception { // 接收时基变更消息
            setNumberEnable(msgTimeBase.isFromEventBus()); // 更新分段数量可用性
            RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLESEGMENTED_STATE, new TopMsgSegmentedState(msgTimeBase.isFromEventBus())); // 发送分段状态消息
        }
    };

    /**
     * 单选组变更监听器
     */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选组变更监听器
        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 选中回调
            onCheckChanged(view, item, true, false); // 触发变更处理
        }

        @Override
        public void onClickSound(boolean isCheckedSuccess) { // 选中音效回调
            PlaySound.getInstance().playButton(); // 播放按键音效
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) { // 提示回调（空实现）

        }
    };

    /**
     * 文本点击监听器 - 弹出数字键盘
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 文本点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            Screen.getViewLocation(v); // 获取视图位置
            if (v.getId() == tvUserDefine.getId()) { // 自定义数量
                dialogNumberKeyBoard.setDecimalData(tvUserDefine.getText().toString(), 8, IDigits.DIGITS_10 // 设置数字键盘数据
                        , new TopDialogNumberKeyBoard.OnDismissListener() { // 键盘关闭监听
                            @Override
                            public void onDismiss(String result) { // 关闭回调
                                result = TBookUtil.getNumRemovePreZero(result); // 去除前导零
                                onTextChanged(tvUserDefine, result, true, false); // 触发文本变更
                            }
                        });
            } else if (v.getId() == tvFitStart.getId()) { // 拟合起始帧
                dialogNumberKeyBoard.setDecimalData(tvFitStart.getText().toString(), 8, IDigits.DIGITS_10 // 设置数字键盘数据
                        , new TopDialogNumberKeyBoard.OnDismissListener() { // 键盘关闭监听
                            @Override
                            public void onDismiss(String result) { // 关闭回调
                                result = TBookUtil.getNumRemovePreZero(result); // 去除前导零
                                onTextChanged(tvFitStart, result, true, false); // 触发文本变更
                            }
                        });
            } else if (v.getId() == tvFitEnd.getId()) { // 拟合结束帧
                dialogNumberKeyBoard.setDecimalData(tvFitEnd.getText().toString(), 8, IDigits.DIGITS_10 // 设置数字键盘数据
                        , new TopDialogNumberKeyBoard.OnDismissListener() { // 键盘关闭监听
                            @Override
                            public void onDismiss(String result) { // 关闭回调
                                result = TBookUtil.getNumRemovePreZero(result); // 去除前导零
                                onTextChanged(tvFitEnd, result, true, false); // 触发文本变更
                            }
                        });
            }
        }
    };

    /**
     * 分段开关状态变更处理
     * @param view 开关视图
     * @param state 新状态
     * @param isFromEventBus 是否来自EventBus
     */
    private void onCheckChanged(MSwitchBox view, boolean state, boolean isFromEventBus)
    { // 分段开关变更处理
        if (view.getId() == rgState.getId()) { // 如果是分段开关
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE, String.valueOf(state?0:1)); // 保存开关状态到缓存

            SegmentSample.getInstance().setSegmentEnable(state); // 设置分段启用状态
            msgSampleSegmented.setState(state); // 更新消息开关状态
            sendMsg(isFromEventBus); // 发送分段存储消息
            showSegment(); // 显示/隐藏分段测量
        }
    }

    /**
     * 单选组变更处理（数量/显示/排序）
     * @param view 触发变更的视图
     * @param item 选中的项
     * @param isSendMsg 是否发送消息
     * @param isFromEventBus 是否来自EventBus
     */
    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isSendMsg, boolean isFromEventBus) { // 单选组变更处理
        if (view.getId() == rgNumber.getId()) { // 分段数量
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER, String.valueOf(item.getIndex())); // 保存数量索引到缓存
            setNumberDetail(); // 更新自定义数量显示

            SegmentSample.getInstance().setSegmentNums(getSegmentNums()); // 更新分段数量
            msgSampleSegmented.setNumber(item); // 更新消息数量
            if (isSendMsg) { // 如果需要发送消息
                sendMsg(isFromEventBus); // 发送分段存储消息
            }
        } else if (view.getId() == rgDisplay.getId()) { // 显示模式
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY, String.valueOf(item.getIndex())); // 保存显示模式到缓存

            SegmentSample.getInstance().setSegmentDisplayType(rgDisplay.getSelected().getIndex()); // 更新显示模式
            setDisplayDetailVisible(); // 更新拟合帧/排序可见性
            msgSampleSegmented.setDisplay(item); // 更新消息显示模式
            if (isSendMsg) { // 如果需要发送消息
                sendMsg(isFromEventBus); // 发送分段存储消息
            }
        } else if (view.getId() == rgOrder.getId()) { // 排序
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_ORDER, String.valueOf(item.getIndex())); // 保存排序到缓存
            msgSampleSegmented.setOrder(item); // 更新消息排序
            if (isSendMsg) { // 如果需要发送消息
                sendMsg(isFromEventBus); // 发送分段存储消息
            }
        }
    }

    /**
     * 文本变更处理（自定义数量/拟合帧）
     * @param textView 触发变更的文本视图
     * @param result 新的文本值
     * @param isSendMsg 是否发送消息
     * @param isFromEventBus 是否来自EventBus
     */
    private void onTextChanged(TextView textView, String result, boolean isSendMsg, boolean isFromEventBus) { // 文本变更处理

        int maxSegmentNums = 0; // 最大分段数

        if (textView.getId() == tvUserDefine.getId()) { // 自定义数量
            maxSegmentNums = SegmentSample.getInstance().getMaxSegmentNums(); // 获取最大分段数
            if (Integer.parseInt(result) < 1) { // 如果小于1
                result = String.valueOf(1); // 最小为1
            }
        } else { // 拟合帧
            maxSegmentNums = Scope.getInstance().getSegmentFrameNums(); // 获取总帧数
            if (maxSegmentNums < 1) maxSegmentNums = 1; // 保证最少1帧
        }

        if (Integer.parseInt(result) > maxSegmentNums) { // 如果超过最大值
            result = String.valueOf(maxSegmentNums); // 限制为最大值
        }

        if (textView.getId() == tvUserDefine.getId()) { // 自定义数量
            tvUserDefine.setText(result); // 更新文本
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER_USERDEFINE, result); // 保存到缓存

            SegmentSample segmentSample = SegmentSample.getInstance(); // 获取分段存储实例
            segmentSample.setEnableMaxSegment(false); // 禁用最大分段
            segmentSample.setSegmentNums(Integer.parseInt(result)); // 设置分段数量

            msgSampleSegmented.setUserDefine(result); // 更新消息自定义数量
            if (isSendMsg) { // 如果需要发送消息
                sendMsg(isFromEventBus); // 发送分段存储消息
            }
        } else if (textView.getId() == tvFitStart.getId()) { // 拟合起始帧
            int startFrame = Integer.parseInt(result); // 解析起始帧
            int endFrame = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_END); // 读取结束帧
            if (endFrame < startFrame) { // 结束帧小于起始帧
                endFrame = startFrame; // 结束帧等于起始帧
                setEndFrame(endFrame); // 更新结束帧
            }
            setBeginFrame(startFrame); // 更新起始帧
            if (isSendMsg) { // 如果需要发送消息
                sendMsg(isFromEventBus); // 发送分段存储消息
            }
        } else if (textView.getId() == tvFitEnd.getId()) { // 拟合结束帧
            int endFrame = Integer.parseInt(result); // 解析结束帧
            int startFrame = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_START); // 读取起始帧
            if (startFrame > endFrame) { // 起始帧大于结束帧
                startFrame = endFrame; // 起始帧等于结束帧
                setBeginFrame(startFrame); // 更新起始帧
            }
            setEndFrame(endFrame); // 更新结束帧
            if (isSendMsg) { // 如果需要发送消息
                sendMsg(isFromEventBus); // 发送分段存储消息
            }
        }
    }

    /**
     * 设置拟合起始帧
     * @param beginFrame 起始帧号（1基）
     */
    private void setBeginFrame(int beginFrame) { // 设置拟合起始帧
        beginFrame -= 1; // 转换为0基索引
        if (beginFrame < 0) beginFrame = 0; // 保证不小于0
        String result = "" + (beginFrame + 1); // 转回1基显示
//        Logger.d("setBeginFrame:"+tvFitStart.getText() + "," + beginFrame);
        tvFitStart.setText(result); // 更新起始帧文本
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_START, result); // 保存到缓存

        SegmentSample.getInstance().setFittingBegingFrame(beginFrame); // 设置拟合起始帧（0基）
        msgSampleSegmented.setStart(result); // 更新消息起始帧
    }

    /**
     * 设置拟合结束帧
     * @param endFrame 结束帧号（1基）
     */
    private void setEndFrame(int endFrame) { // 设置拟合结束帧
//        Logger.i(Command.TAG,"endFrame:"+endFrame);
        endFrame -= 1; // 转换为0基索引
        if (endFrame < 0) endFrame = 0; // 保证不小于0
        String result = "" + (endFrame + 1); // 转回1基显示
        tvFitEnd.setText(result); // 更新结束帧文本
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY_FIT_END, result); // 保存到缓存

        SegmentSample.getInstance().setFittingEndFrame(endFrame); // 设置拟合结束帧（0基）
        msgSampleSegmented.setEnd(result); // 更新消息结束帧
    }
}
