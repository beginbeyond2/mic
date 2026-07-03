// ============================================================
//  模块定位：measure（测量功能模块）
//  文件路径：top/layout/measure/TopLayoutMeasureSetting.java
//  核心职责：测量设置子页面Fragment，管理测量指示器、范围、阈值（高/中/低）参数配置
//  架构设计：Fragment + RxBus观察者模式，通过Consumer订阅通道变更事件
//  数据流向：用户修改阈值 → updateTxtChanged → Channel设置阈值 → MeasureService刷新测量
//  依赖关系：依赖ChannelFactory通道工厂、MeasureService测量服务、CacheUtil缓存、TBookUtil数值格式化
//  使用场景：测量功能设置页面，配置测量指示器开关、范围模式、阈值类型和数值
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.measure; // 声明该类所属的包路径

import android.annotation.SuppressLint; // 导入注解，抑制SetTextI18n警告
import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle类
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.Button; // 导入按钮控件
import android.widget.RadioButton; // 导入单选按钮控件
import android.widget.TextView; // 导入文本视图控件

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.base.Logger; // 导入日志工具
import com.micsig.tbook.scope.channel.BaseChannel; // 导入通道基类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂
import com.micsig.tbook.scope.channel.MathChannel; // 导入数学通道类
import com.micsig.tbook.scope.math.MathFFTWave; // 导入FFT波形类
import com.micsig.tbook.scope.measure.MeasureService; // 导入测量服务
import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载消息
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组
import com.micsig.tbook.tbookscope.R; // 导入资源ID
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels; // 导入右侧通道消息
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers; // 导入右侧其他消息
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令工厂
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令消息到UI
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgMath; // 导入数学通道消息
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight; // 导入参考通道消息
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情发送消息监听接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard; // 导入浮点键盘弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具
import com.micsig.tbook.tbookscope.wavezone.display.MsgCursorVisible; // 导入光标可见性消息
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage; // 导入测量管理
import com.micsig.tbook.ui.MMainMenuChannel; // 导入多通道菜单控件
import com.micsig.tbook.ui.MSwitchBox; // 导入开关控件
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.channel.TopViewChannel; // 导入通道视图
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具
import com.micsig.tbook.ui.util.TBookUtil; // 导入示波器工具
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道枚举

import java.util.ArrayList; // 导入动态数组

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入消费者接口


/**
 * 测量设置Fragment - 管理测量指示器、范围模式、阈值参数配置
 */
public class TopLayoutMeasureSetting extends Fragment { // 继承Fragment，测量设置子页面

    private static final String TAG = "TopLayoutMeasureSetting"; // 日志标签
    private Context context; // 上下文对象
    private MMainMenuChannel viewChannel; // 通道选择菜单控件
    private MSwitchBox checkIndicator; // 测量指示器开关
    private TopMsgMeasureSetting msgSetting; // 测量设置消息对象
    private TopViewRadioGroup radioRange, radioThreshold; // 范围和阈值单选组
    private TextView txtHigh,txtMiddle,txtLow,lblHigh,lblMiddle,lblLow; // 阈值文本和标签
    private TopDialogFloatKeyBoard dialogFloatKeyBoard; // 浮点键盘弹窗
    private Button btnReset; // 重置按钮
    //Ch1--Ch8
    //Math1--Math8
    //R1--R8
    //S1--S4
    private boolean[] channelShow = { // 各通道可见性数组
            false, false, false, false, false, false, false, false, // Ch1-Ch8
            false, false, false, false, false, false, false, false, // Math1-Math8
            false, false, false, false, false, false, false, false, // R1-R8
            false, false, false, false // S1-S4
    };
//    private List<Boolean> channelShowList = new ArrayList<>(
//            Collections.nCopies(
//                    ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT +
//                            ChannelFactory.REF_CNT + ChannelFactory.SERIAL_CNT, Boolean.FALSE)
//    );

    private ViewGroup rootView; // 根视图引用

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
        this.rootView=container; // 保存父容器引用
        return inflater.inflate(R.layout.layout_measure_setting, container, false); // 填充测量设置布局
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
        initControls(); // 初始化事件控制
    }

    /**
     * 初始化视图组件
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图方法
        dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard); // 获取浮点键盘弹窗

        checkIndicator=view.findViewById(R.id.indicator); // 获取指示器开关
        radioRange =view.findViewById(R.id.range); // 获取范围单选组
        radioThreshold =view.findViewById(R.id.Threshold); // 获取阈值单选组
        txtHigh=view.findViewById(R.id.txtHigh); // 获取高阈值文本
        txtMiddle=view.findViewById(R.id.txtMiddle); // 获取中阈值文本
        txtLow=view.findViewById(R.id.txtLow); // 获取低阈值文本
        lblHigh=view.findViewById(R.id.lblHigh); // 获取高阈值标签
        lblMiddle=view.findViewById(R.id.lblMiddle); // 获取中阈值标签
        lblLow=view.findViewById(R.id.lblLow); // 获取低阈值标签
        btnReset=view.findViewById(R.id.btnReset); // 获取重置按钮

        checkIndicator.setOnToggleStateChangedListener(this::onToggleStateChanged); // 设置指示器开关监听
        radioRange.setData(R.string.measureSettingRange,R.array.measureSettingRange,onCheckChangedListener); // 设置范围单选组数据
        radioThreshold.setData(R.string.measureSettingThresholds,R.array.measureSettingThresholds,onCheckChangedListener); // 设置阈值单选组数据
        txtHigh.setOnClickListener(this::OnClickListener); // 设置高阈值点击监听
        txtMiddle.setOnClickListener(this::OnClickListener); // 设置中阈值点击监听
        txtLow.setOnClickListener(this::OnClickListener); // 设置低阈值点击监听
        btnReset.setOnClickListener(this::OnClickListener); // 设置重置按钮点击监听
        initChannelView(view); // 初始化通道选择视图

        msgSetting=new TopMsgMeasureSetting(); // 创建设置消息对象

    }

    /**
     * 初始化通道选择视图
     * @param view 根视图
     */
    private void initChannelView(View view){ // 初始化通道选择视图
        viewChannel = (MMainMenuChannel) view.findViewById(R.id.chanMeasureSetting); // 获取通道选择控件
        viewChannel.setChangeListener(onChannelItemClickListener, null); // 设置通道切换监听

    }

    /**
     * 初始化RxBus事件订阅
     */
    private void initControls(){ // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令到UI事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels); // 订阅通道变更事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_MATH).subscribe(consumerRightMath); // 订阅数学通道事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers); // 订阅其他变更事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_REF).subscribe(consumerRightRef); // 订阅参考通道事件
        RxBus.getInstance().getObservable(RxEnum.CURSOR_CHANGE_VISIBLE).subscribe(consumerCursorChangeVisible); // 订阅光标可见性事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor); // 订阅通道颜色事件
        RxBus.getInstance().getObservable(RxEnum.MSG_TVALUE_ENABLE).subscribe(consumerTValue); // 订阅TValue启用事件

    }

    /**
     * 发送测量设置消息到RxBus
     */
    private void sendMsg(){ // 发送设置消息
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_MEASURE_SETTING,msgSetting); // 通过RxBus发送设置消息
    }

    /**
     * 指示器开关状态变更处理
     * @param view 开关视图
     * @param state 新状态
     */
    private void onToggleStateChanged(MSwitchBox view, boolean state){ // 指示器开关状态变更
        PlaySound.getInstance().playButton(); // 播放按键音效
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_INDICATOR,String.valueOf(state)); // 保存状态到缓存

        MeasureManage.getInstance().getMeasureIndication().setEnable(state); // 设置指示器启用状态
        MeasureManage.getInstance().getMeasureItem().setSelectEnable(state); // 设置测量项选择启用
        Tools.PrintControlsLocation("Setting",rootView); // 打印控件位置调试信息
//        if (state==false){
//            MeasureManage.getInstance().getMeasureItem().cancelAllSelected();
//        }
    }

    /**
     * 通道选择变更监听器
     */
    private TopViewChannel.onItemClickListener onChannelItemClickListener = new TopViewChannel.onItemClickListener() { // 通道选择监听器
        @Override
        public void checkChanged(int viewId, int checkedIndex, RadioButton radioButton) { // 通道切换回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_CHANNEL_SELECT, String.valueOf(checkedIndex)); // 保存通道选择到缓存
            updateTextView(checkedIndex); // 更新阈值文本显示

        }
    };

    /**
     * 更新阈值文本视图显示
     * @param checkedIndex 选中的通道索引
     */
    public void updateTextView(int checkedIndex){ // 更新阈值文本视图
        checkedIndex = TChan.toUiChNo(checkedIndex); // 转换为UI通道编号
        txtHigh.setText(readTxtHigh()); // 更新高阈值文本
        txtMiddle.setText(readTxtMiddle()); // 更新中阈值文本
        txtLow.setText(readTxtLow()); // 更新低阈值文本
        lblHigh.setTextColor(TChan.getChannelColor(context,checkedIndex)); // 设置高阈值标签颜色
        lblMiddle.setTextColor(TChan.getChannelColor(context,checkedIndex)); // 设置中阈值标签颜色
        lblLow.setTextColor(TChan.getChannelColor(context,checkedIndex)); // 设置低阈值标签颜色
        radioThreshold.setPromptTxtColor(TChan.getChannelColor(context,checkedIndex)); // 设置阈值提示文字颜色

        int thresholdIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS); // 获取阈值类型索引
        //#2811 启动示波器之后测量指示中的设置值不对，传递参数有误导致获取对应的channel的buffer不对。
        checkedIndex = TChan.toFpgaChNo(checkedIndex); // 转换为FPGA通道编号
        updateParamToDevice(checkedIndex, thresholdIdx); // 更新参数到设备
    }

    /**
     * 单选组变更监听器
     */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选组变更监听器
        @Override
        public void onClickSound(boolean isCheckedSuccess) { // 选中音效回调
            PlaySound.getInstance().playButton(); // 播放按键音效
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) { // 提示回调（空实现）

        }

        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 选中回调
            onCheckChanged(view, item, false); // 触发变更处理
        }
    };

    /**
     * TValue启用消费者 - 处理TValue光标追踪状态变更
     */
    private Consumer<Boolean> consumerTValue = new Consumer<Boolean>() { // TValue启用消费者
        @Override
        public void accept(@androidx.annotation.NonNull Boolean update) throws Exception { // 接收TValue启用状态
            Logger.i(TAG, "isTVtrace= " + MeasureManage.getInstance().isCursorTValueTrace()); // 打印追踪状态日志
            radioRange.setEnabled(!MeasureManage.getInstance().isCursorTValueTrace()); // TValue追踪时禁用范围选择
        }
    };

    /**
     * 单选组变更核心处理
     * @param view 触发变更的视图
     * @param item 选中的项
     * @param isFormHardware 是否来自硬件
     */
    private void onCheckChanged(TopViewRadioGroup view,TopBeanChannel item,boolean isFormHardware){ // 单选组变更核心处理
        if (view.getId()==radioRange.getId()){ // 如果是范围单选组

            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_RANGE,String.valueOf(item.getIndex())); // 保存范围选择到缓存
            MeasureService.setCursorRang(item.getIndex() == 1); // 设置光标范围模式
            MeasureService.forceMeasureRefresh(); // 强制刷新测量
            Command.get().getMeasure_setting().Range(item.getIndex(),false); // 发送范围命令到硬件


        }else if (view.getId()==radioThreshold.getId()){ // 如果是阈值单选组
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS,String.valueOf(item.getIndex())); // 保存阈值选择到缓存
            txtHigh.setText(readTxtHigh()); // 更新高阈值文本
            txtMiddle.setText(readTxtMiddle()); // 更新中阈值文本
            txtLow.setText(readTxtLow()); // 更新低阈值文本

            int chIdx = viewChannel.getSelectChannel(); // 获取选中通道索引
            updateParamToDevice(chIdx,item.getIndex()); // 更新参数到设备
        }
        sendMsg(); // 发送设置消息
    }

    /**
     * 更新阈值参数到设备
     * @param chIdx 通道索引
     * @param thresholdIdx 阈值类型索引
     */
    private void updateParamToDevice(int chIdx,int thresholdIdx){ // 更新参数到设备
        BaseChannel channel = (BaseChannel) ChannelFactory.getValidChannel(chIdx); // 获取有效通道
        if(channel != null){ // 如果通道有效
            if(thresholdIdx == 1){ // 绝对值阈值模式
                channel.setAbsLower(TBookUtil.getDoubleFromM(getLow())); // 设置绝对下限
                channel.setAbsUpper(TBookUtil.getDoubleFromM(getHigh())); // 设置绝对上限
                channel.setAbsMiddle(TBookUtil.getDoubleFromM(getMiddle())); // 设置绝对中值
            }else{ // 百分比阈值模式
                channel.setLower(Integer.parseInt(getLow())); // 设置百分比下限
                channel.setUpper(Integer.parseInt(getHigh())); // 设置百分比上限
                channel.setMiddle(Integer.parseInt(getMiddle())); // 设置百分比中值
            }
            channel.setAbsEnable(thresholdIdx == 1); // 设置绝对值模式启用状态

        }
        MeasureService.forceMeasureRefresh(); // 强制刷新测量
    }

    /**
     * 重置所有通道的阈值为默认值
     */
    private void reset(){ // 重置阈值
        TChan.foreachAllChan((chan)->{ // 遍历所有通道
            int ch=TChan.toFpgaChNo(chan); // 转换为FPGA通道编号
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH + "0" + ch, String.valueOf(90)); // 保存百分比高阈值默认值90
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE + "0" + ch, String.valueOf(50)); // 保存百分比中阈值默认值50
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW + "0" + ch, String.valueOf(10)); // 保存百分比低阈值默认值10

            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH + "1" + ch, String.valueOf(1)); // 保存绝对值高阈值默认值1
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE + "1" + ch, String.valueOf(0)); // 保存绝对值中阈值默认值0
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW + "1" + ch, String.valueOf(-1)); // 保存绝对值低阈值默认值-1
        });
        radioThreshold.setSelectedIndex(0); // 重置阈值类型为百分比
        onCheckChanged(radioThreshold,new TopBeanChannel(0,""),false); // 触发阈值变更
        updateTxtChanged(txtHigh.getId(),txtHigh.getText().toString()); // 更新高阈值
        updateTxtChanged(txtMiddle.getId(),txtMiddle.getText().toString()); // 更新中阈值
        updateTxtChanged(txtLow.getId(),txtLow.getText().toString()); // 更新低阈值
        //control deal
        for(int chIdx=ChannelFactory.CH1;chIdx<ChannelFactory.S1;chIdx++) { // 遍历所有物理通道
            BaseChannel channel = (BaseChannel) ChannelFactory.getValidChannel(chIdx); // 获取有效通道
            if (channel != null) { // 如果通道有效
                channel.setLower(Integer.parseInt(getLow(chIdx))); // 设置百分比下限
                channel.setUpper(Integer.parseInt(getHigh(chIdx))); // 设置百分比上限
                channel.setMiddle(Integer.parseInt(getMiddle(chIdx))); // 设置百分比中值
            }
        }
        MeasureService.forceMeasureRefresh(); // 强制刷新测量
    }

    /**
     * 获取当前通道的高阈值缓存值
     * @return 高阈值字符串
     */
    private String getHigh(){ // 获取高阈值
        return CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH +getSaveThresholdsParam()); // 从缓存读取
    }

    /**
     * 获取当前通道的中阈值缓存值
     * @return 中阈值字符串
     */
    private String getMiddle(){ // 获取中阈值
        return CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam()); // 从缓存读取
    }

    /**
     * 获取当前通道的低阈值缓存值
     * @return 低阈值字符串
     */
    private String getLow(){ // 获取低阈值
        return CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW+getSaveThresholdsParam()); // 从缓存读取
    }

    /**
     * 获取指定通道的高阈值缓存值
     * @param chIdx 通道索引
     * @return 高阈值字符串
     */
    private String getHigh(int chIdx){ // 获取指定通道高阈值
        return CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH +getSaveThresholdsParam(chIdx)); // 从缓存读取
    }

    /**
     * 获取指定通道的中阈值缓存值
     * @param chIdx 通道索引
     * @return 中阈值字符串
     */
    private String getMiddle(int chIdx){ // 获取指定通道中阈值
        return CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam(chIdx)); // 从缓存读取
    }

    /**
     * 获取指定通道的低阈值缓存值
     * @param chIdx 通道索引
     * @return 低阈值字符串
     */
    private String getLow(int chIdx){ // 获取指定通道低阈值
        return CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW+getSaveThresholdsParam(chIdx)); // 从缓存读取
    }


    /**
     * 读取高阈值文本（含单位）
     * @return 带单位的高阈值字符串
     */
    private String readTxtHigh(){ // 读取高阈值文本
        return getHigh()+getUnit(); // 返回值+单位
    }

    /**
     * 读取中阈值文本（含单位）
     * @return 带单位的中阈值字符串
     */
    private String readTxtMiddle(){ // 读取中阈值文本
        return getMiddle()+getUnit(); // 返回值+单位
    }

    /**
     * 读取低阈值文本（含单位）
     * @return 带单位的低阈值字符串
     */
    private String readTxtLow(){ // 读取低阈值文本
        return getLow()+getUnit(); // 返回值+单位
    }

    /**
     * 视图点击事件分发
     * @param v 被点击的视图
     */
    private void OnClickListener(View v){ // 视图点击事件分发
        if (v.getId()==txtHigh.getId()){ // 如果点击高阈值
            openDialog(txtHigh); // 打开高阈值编辑弹窗
        }else if (v.getId()==txtMiddle.getId()){ // 如果点击中阈值
            openDialog(txtMiddle); // 打开中阈值编辑弹窗
        }else if (v.getId()==txtLow.getId()){ // 如果点击低阈值
            openDialog(txtLow); // 打开低阈值编辑弹窗
        }else if (v.getId()==btnReset.getId()){ // 如果点击重置按钮
            PlaySound.getInstance().playButton(); // 播放按键音效
            reset(); // 执行重置
        }
    }

    /**
     * 打开浮点键盘弹窗编辑阈值
     * @param txtView 被编辑的文本视图
     */
    private void openDialog(TextView txtView){ // 打开浮点键盘弹窗
        int thresholdsIdx= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS); // 获取阈值类型索引
        String txt= txtView.getText().toString().replaceAll("(?:A|V|%|dB|\\s)",""); // 去除单位字符
        if (thresholdsIdx==0){ // 百分比阈值模式
            dialogFloatKeyBoard.setFloatData_OnlyNum(txt, txtView, new TopDialogFloatKeyBoard.OnDismissListener() { // 设置纯数字键盘
                @Override
                public void onDismiss(View fromView, String show) { // 弹窗关闭回调
                    PlaySound.getInstance().playButton(); // 播放按键音效
                    int val =(int) TBookUtil.getDoubleFromM(show); // 解析输入值
                    val= getVerify(txtView,val); // 验证并修正值
                    show = TBookUtil.getPercent(Math.abs(val)); // 格式化为百分比
                    if (show.equals("")) { // 如果为空
                        show = "0 "; // 默认为0
                    }
                    updateTxtChanged(txtView.getId(),show); // 更新文本
                }
            });

        }else if (thresholdsIdx==1){ // 绝对值阈值模式
            boolean bShow = false; // 是否使用dB模式
            int chIdx= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_CHANNEL_SELECT); // 获取选中通道
            if(ChannelFactory.isMath_FFT_Ch(chIdx)){ // 如果是FFT数学通道
                MathChannel mathChannel = ChannelFactory.getMathChannel(chIdx); // 获取数学通道
                if(mathChannel != null){ // 如果通道有效
                    if(mathChannel.getMathFFTWave().getFFTType()== MathFFTWave.FFT_TYPE_DB){ // 如果FFT类型为dB
                        bShow = true; // 使用dB模式
                        dialogFloatKeyBoard.setFloatData_NoUnit(txt, txtView, new TopDialogFloatKeyBoard.OnDismissListener() { // 设置无单位键盘
                            @Override
                            public void onDismiss(View fromView, String show) { // 弹窗关闭回调
                                PlaySound.getInstance().playButton(); // 播放按键音效
                                double val = TBookUtil.getDoubleFromM(show); // 解析输入值
                                show = TBookUtil.getD3FromD(Math.abs(val)); // 格式化3位小数
                                if (val<0) show="-"+show; // 处理负号
                                if (show.equals("")) { // 如果为空
                                    show = "0 "; // 默认为0
                                }
                                show= String.valueOf((int)Double.parseDouble(show)); // 转换为整数
                                show= getVerify(txtView,show); // 验证并修正值
                                updateTxtChanged(txtView.getId(), show); // 更新文本
                            }
                        });
                    }
                }
            }
            if(!bShow){ // 非dB模式
                dialogFloatKeyBoard.setFloatData(txt, txtView, new TopDialogFloatKeyBoard.OnDismissListener() { // 设置带单位键盘
                    @Override
                    public void onDismiss(View fromView, String show) { // 弹窗关闭回调
                        PlaySound.getInstance().playButton(); // 播放按键音效
                        double val = TBookUtil.getDoubleFromM(show); // 解析输入值
                        show = TBookUtil.getMFromDouble(Math.abs(val)); // 格式化为带单位值
                        if (val < 0) show = "-" + show; // 处理负号
                        if (show.equals("")) { // 如果为空
                            show = "0 "; // 默认为0
                        }
                        show= getVerify(txtView,show); // 验证并修正值
                        updateTxtChanged(txtView.getId(), show); // 更新文本
                    }
                });
            }
        }

    }

    /**
     * 验证百分比阈值范围（整数版）
     * @param txtView 被验证的文本视图
     * @param val 输入值
     * @return 修正后的值
     */
    private int getVerify(TextView txtView,int val){ // 验证百分比阈值
        if (txtView.getId()==txtHigh.getId()){ // 如果是高阈值
            int low=Integer.parseInt( txtLow.getText().toString().replace("%","")); // 获取低阈值
            if (val<=low){ // 如果高阈值不大于低阈值
                val=low+1; // 修正为低阈值+1
            }
            int mid=Integer.parseInt(txtMiddle.getText().toString().replace("%","")); // 获取中阈值
            if (mid>val){ // 如果中阈值大于高阈值
                mid=val; // 修正中阈值
                txtMiddle.setText(mid+getUnit()); // 更新中阈值文本
            }
            return val; // 返回修正后的值
        }else if (txtView.getId()==txtLow.getId()){ // 如果是低阈值
            int high=Integer.parseInt( txtHigh.getText().toString().replace("%","")); // 获取高阈值
            if (val>=high){ // 如果低阈值不小于高阈值
                val= high-1; // 修正为高阈值-1
            }
            int mid=Integer.parseInt(txtMiddle.getText().toString().replace("%","")); // 获取中阈值
            if (mid<val){ // 如果中阈值小于低阈值
                mid=val; // 修正中阈值
                txtMiddle.setText(mid+getUnit()); // 更新中阈值文本
            }
            return val; // 返回修正后的值
        }else { // 如果是中阈值
            int high=Integer.parseInt( txtHigh.getText().toString().replace("%","")); // 获取高阈值
            int low=Integer.parseInt( txtLow.getText().toString().replace("%","")); // 获取低阈值
            if (val>high){ // 如果中阈值大于高阈值
                val=high; // 修正为高阈值
            }else if (val<low){ // 如果中阈值小于低阈值
                val=low; // 修正为低阈值
            }
            return val; // 返回修正后的值
        }
    }

    /**
     * 验证绝对值阈值范围（字符串版）
     * @param txtView 被验证的文本视图
     * @param val 输入值字符串
     * @return 修正后的值字符串
     */
    private String getVerify(TextView txtView,String val){ // 验证绝对值阈值
        String txt= val.replaceAll("(?:A|V|%|dB|\\s)",""); // 去除单位字符
        double value = TBookUtil.getDoubleFromM(txt); // 解析数值
        if(txtView.getId()==txtHigh.getId()){ // 如果是高阈值
            String td=txtLow.getText().toString().replaceAll("(?:A|V|%|dB|\\s)",""); // 获取低阈值文本
            double low=TBookUtil.getDoubleFromM(td); // 解析低阈值
            if (value<=low){ // 如果高阈值不大于低阈值
                val=TBookUtil.addMinUnit(td,1); // 修正为低阈值+1最小单位
            }
            value = TBookUtil.getDoubleFromM(val); // 重新解析
            String mid=txtMiddle.getText().toString().replaceAll("(?:A|V|%|dB|\\s)",""); // 获取中阈值文本
            double middle=TBookUtil.getDoubleFromM(mid); // 解析中阈值
            if (middle>value){ // 如果中阈值大于高阈值
                middle=value; // 修正中阈值
                String s = TBookUtil.getD3FromD_zf(middle); // 格式化
                txtMiddle.setText(s+getUnit()); // 更新中阈值文本
            }

            return val; // 返回修正后的值
        }else if (txtView.getId()==txtLow.getId()){ // 如果是低阈值
            String td=txtHigh.getText().toString().replaceAll("(?:A|V|%|dB|\\s)",""); // 获取高阈值文本
            double high=TBookUtil.getDoubleFromM(td); // 解析高阈值
            if (value>=high){ // 如果低阈值不小于高阈值
                val=TBookUtil.subMinUnit(td,1); // 修正为高阈值-1最小单位
            }

            value = TBookUtil.getDoubleFromM(val); // 重新解析
            String mid=txtMiddle.getText().toString().replaceAll("(?:A|V|%|dB|\\s)",""); // 获取中阈值文本
            double middle=TBookUtil.getDoubleFromM(mid); // 解析中阈值
            if (middle<value){ // 如果中阈值小于低阈值
                middle=value; // 修正中阈值
                String s = TBookUtil.getD3FromD_zf(middle); // 格式化
                txtMiddle.setText(s+getUnit()); // 更新中阈值文本
            }
            return val; // 返回修正后的值
        }else { // 如果是中阈值
            String th=txtHigh.getText().toString().replaceAll("(?:A|V|%|dB|\\s)",""); // 获取高阈值文本
            String tl=txtLow.getText().toString().replaceAll("(?:A|V|%|dB|\\s)",""); // 获取低阈值文本
            double high=TBookUtil.getDoubleFromM(th); // 解析高阈值
            double low=TBookUtil.getDoubleFromM(tl); // 解析低阈值
            double mid=TBookUtil.getDoubleFromM(val); // 解析中阈值
            if (mid>high){ // 如果中阈值大于高阈值
                val=th; // 修正为高阈值
            }else if (mid<low){ // 如果中阈值小于低阈值
                val=tl; // 修正为低阈值
            }

            return val; // 返回修正后的值
        }
    }

    /**
     * 更新阈值文本变更到缓存和设备
     * @param id 文本视图ID
     * @param result 新的阈值字符串
     */
    private void updateTxtChanged(int id,String result){ // 更新阈值文本变更
        if (StrUtil.isEmpty(result)) { // 如果结果为空
            result = "0"; // 默认为0
        }
        result= result.replaceAll("(?:A|V|%|dB|\\s)",""); // 去除单位字符

        int chIdx = viewChannel.getSelectChannel(); // 获取选中通道索引
        BaseChannel channel = (BaseChannel) ChannelFactory.getValidChannel(chIdx); // 获取有效通道
        if (id==txtHigh.getId()) { // 如果是高阈值
            txtHigh.setText(result + getUnit()); // 更新文本显示
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH+getSaveThresholdsParam() , result); // 保存到缓存

            if(channel != null){ // 如果通道有效
                if(channel.isAbsEnable()){ // 绝对值模式
                    channel.setAbsUpper(TBookUtil.getDoubleFromM(result)); // 设置绝对上限
                }else { // 百分比模式
                    channel.setUpper(Integer.parseInt(result)); // 设置百分比上限
                }
            }
        }else if (id==txtMiddle.getId()){ // 如果是中阈值
            txtMiddle.setText(result + getUnit()); // 更新文本显示
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam() , result); // 保存到缓存
            if(channel != null){ // 如果通道有效
                if(channel.isAbsEnable()){ // 绝对值模式
                    channel.setAbsMiddle(TBookUtil.getDoubleFromM(result)); // 设置绝对中值

                }else { // 百分比模式
                    channel.setMiddle(Integer.parseInt(result)); // 设置百分比中值
                }
            }
        }else if (id==txtLow.getId()){ // 如果是低阈值
            txtLow.setText(result + getUnit()); // 更新文本显示
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW+getSaveThresholdsParam(), result); // 保存到缓存
            if(channel != null){ // 如果通道有效
                if(channel.isAbsEnable()){ // 绝对值模式
                    channel.setAbsLower(TBookUtil.getDoubleFromM(result)); // 设置绝对下限
                }else { // 百分比模式
                    channel.setLower(Integer.parseInt(result)); // 设置百分比下限
                }
            }
        }

        //if middle value changed,reset middle
        String mid= CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam()); // 获取缓存中的中阈值
        String txtMid=txtMiddle.getText().toString().replaceAll("(?:A|V|%|dB|\\s)",""); // 获取当前显示的中阈值
        if (mid.equals(txtMid)==false){ // 如果中阈值发生了变化
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam() , txtMid); // 更新缓存
            if(channel != null){ // 如果通道有效
                if(channel.isAbsEnable()){ // 绝对值模式
                    channel.setAbsMiddle(TBookUtil.getDoubleFromM(txtMid)); // 设置绝对中值
                }else { // 百分比模式
                    channel.setMiddle(Integer.parseInt(txtMid)); // 设置百分比中值
                }
            }
        }
        MeasureService.forceMeasureRefresh(); // 强制刷新测量
    }

    /**
     * 获取缓存键的阈值参数后缀（当前通道）
     * @return 阈值类型+通道索引的字符串
     */
    private String getSaveThresholdsParam(){ // 获取缓存键参数后缀
        int chIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_CHANNEL_SELECT); // 获取通道选择
        int thresholdsIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS); // 获取阈值类型
        return ""+thresholdsIdx+chIdx; // 拼接参数后缀
    }

    /**
     * 获取缓存键的阈值参数后缀（指定通道，默认百分比模式）
     * @param chIdx 通道索引
     * @return 阈值类型+通道索引的字符串
     */
    private String getSaveThresholdsParam(int chIdx){ // 获取指定通道缓存键参数后缀
        int thresholdsIdx=0; // 默认百分比模式
        return ""+thresholdsIdx+chIdx; // 拼接参数后缀
    }

    /**
     * 获取缓存键的阈值参数后缀（指定通道和阈值类型）
     * @param chIdx 通道索引
     * @param thresholdsIdx 阈值类型索引
     * @return 阈值类型+通道索引的字符串
     */
    private String getSaveThresholdsParam(int chIdx,int thresholdsIdx){ // 获取指定通道和阈值类型缓存键参数后缀

        return ""+thresholdsIdx+chIdx; // 拼接参数后缀
    }

    /**
     * 获取当前阈值单位
     * @return 单位字符串（%或电压单位）
     */
    private String getUnit(){ // 获取阈值单位
        int thresholdsIdx= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS); // 获取阈值类型
        if (thresholdsIdx==0){ // 百分比模式
            return "%"; // 返回百分号
        }else { // 绝对值模式
            int chIdx= viewChannel.getSelectChannel(); // 获取选中通道
            String yUnit = ChannelFactory.getProbeType(chIdx); // 获取探头类型单位
            return yUnit; // 返回电压单位
        }

    }

    /**
     * 缓存加载消费者 - 恢复设置状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 恢复缓存状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutMeasureCommon, true); // 标记缓存已加载
        }
    };

    /**
     * 从缓存恢复设置状态
     */
    @SuppressLint("SetTextI18n") // 抑制SetTextI18n警告
    private void setCache(){ // 恢复缓存状态
        boolean indicator= CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_SETTING_INDICATOR); // 获取指示器状态
        int rangeIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_RANGE); // 获取范围索引
        int channelIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_CHANNEL_SELECT); // 获取通道选择
        int thresholdsIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS); // 获取阈值类型
        String high=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH+getSaveThresholdsParam()); // 获取高阈值
        String middle=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam()); // 获取中阈值
        String low=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW+getSaveThresholdsParam()); // 获取低阈值

        checkIndicator.setState(indicator); // 设置指示器状态
        radioRange.setSelectedIndex(rangeIdx); // 设置范围选择
        viewChannel.setChecked(channelIdx); // 设置通道选择
        radioThreshold.setSelectedIndex(thresholdsIdx); // 设置阈值类型
        txtHigh.setText(high+getUnit()); // 设置高阈值文本
        txtMiddle.setText(middle+getUnit()); // 设置中阈值文本
        txtLow.setText(low+getUnit()); // 设置低阈值文本

        boolean ch1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch1); // 获取Ch1状态
        boolean ch2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch2); // 获取Ch2状态
        boolean ch3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch3); // 获取Ch3状态
        boolean ch4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch4); // 获取Ch4状态
        boolean ch5 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch5); // 获取Ch5状态
        boolean ch6 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch6); // 获取Ch6状态
        boolean ch7 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch7); // 获取Ch7状态
        boolean ch8 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch8); // 获取Ch8状态

        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) { // 2通道型号
            channelShow[0] = ch1; // 设置Ch1可见性
            channelShow[1] = ch2; // 设置Ch2可见性
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) { // 4通道型号
            channelShow[0] = ch1; // 设置Ch1可见性
            channelShow[1] = ch2; // 设置Ch2可见性
            channelShow[2] = ch3; // 设置Ch3可见性
            channelShow[3] = ch4; // 设置Ch4可见性
        } else { // 8通道型号
            channelShow[0] = ch1; // 设置Ch1可见性
            channelShow[1] = ch2; // 设置Ch2可见性
            channelShow[2] = ch3; // 设置Ch3可见性
            channelShow[3] = ch4; // 设置Ch4可见性
            channelShow[4] = ch5; // 设置Ch5可见性
            channelShow[5] = ch6; // 设置Ch6可见性
            channelShow[6] = ch7; // 设置Ch7可见性
            channelShow[7] = ch8; // 设置Ch8可见性
        }
        TChan.foreachCh1ToR8(chan -> { // 遍历所有通道
            if (TChan.isMath(chan)) { // 数学通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan); // 获取数学通道状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan); // 获取用户添加标记
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 设置可见性
            }
            if (TChan.isRef(chan)) { // 参考通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan); // 获取参考通道状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan); // 获取用户添加标记
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 设置可见性
            }
        });
        setParam(); // 设置参数到设备
        setChannelShow(); // 更新通道显示
        MeasureManage.getInstance().getMeasureIndication().setEnable(indicator); // 设置指示器启用
        MeasureManage.getInstance().getMeasureItem().setSelectEnable(indicator); // 设置测量项选择启用
        MeasureService.setCursorRang(rangeIdx == 1); // 设置光标范围模式

        //init selected
        boolean b= isMeasureItemClickEnable(); // 检查测量项点击是否启用
        MeasureManage.MeasureItemStruct item=MeasureManage.getInstance().getMeasureItem().getSelectItem(); // 获取选中的测量项
        if (item==null || b==false){ // 如果无选中项或不可点击
            return; // 返回
        }
        channelIdx=item.getChannelId()-1; // 获取选中项的通道索引
        high=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_HIGH+getSaveThresholdsParam(channelIdx,thresholdsIdx)); // 获取选中通道高阈值
        middle=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_MIDDLE+getSaveThresholdsParam(channelIdx,thresholdsIdx)); // 获取选中通道中阈值
        low=CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SETTING_LOW+getSaveThresholdsParam(channelIdx,thresholdsIdx)); // 获取选中通道低阈值
        BaseChannel channel = (BaseChannel) ChannelFactory.getValidChannel(channelIdx); // 获取有效通道
        if(channel != null){ // 如果通道有效
            if(thresholdsIdx == 1){ // 绝对值模式
                channel.setAbsLower(TBookUtil.getDoubleFromM(low)); // 设置绝对下限
                channel.setAbsUpper(TBookUtil.getDoubleFromM(high)); // 设置绝对上限
                channel.setAbsMiddle(TBookUtil.getDoubleFromM(middle)); // 设置绝对中值
            }else{ // 百分比模式
                channel.setLower(Integer.parseInt(low)); // 设置百分比下限
                channel.setUpper(Integer.parseInt(high)); // 设置百分比上限
                channel.setMiddle(Integer.parseInt(middle)); // 设置百分比中值
            }
            channel.setAbsEnable(thresholdsIdx == 1); // 设置绝对值模式
        }
        MeasureService.forceMeasureRefresh(); // 强制刷新测量

        Command.get().getMeasure_setting().Range(rangeIdx,false); // 发送范围命令
    }

    /**
     * 将缓存中的阈值参数设置到各通道
     */
    private void setParam() { // 设置参数到设备
        int thresholdIdx = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS); // 获取阈值类型
        String channelStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_CHANNEL); // 获取通道缓存
        ArrayList<String> channelList = StrUtil.getListFromString(channelStrings, CacheUtil.MEASURE_SELECT_LIST_SLIP); // 解析通道列表
        for (int i = 0; i < channelList.size(); i++) { // 遍历通道列表
            int chIdx = Integer.parseInt(channelList.get(i)) - 1; // 解析通道索引
            BaseChannel channel = (BaseChannel) ChannelFactory.getValidChannel(chIdx); // 获取有效通道
            if (channel != null) { // 如果通道有效
                if (thresholdIdx == 1) { // 绝对值模式
                    channel.setAbsLower(TBookUtil.getDoubleFromM(getLow(chIdx))); // 设置绝对下限
                    channel.setAbsUpper(TBookUtil.getDoubleFromM(getHigh(chIdx))); // 设置绝对上限
                    channel.setAbsMiddle(TBookUtil.getDoubleFromM(getMiddle(chIdx))); // 设置绝对中值
                } else { // 百分比模式
                    channel.setLower(Integer.parseInt(getLow(chIdx))); // 设置百分比下限
                    channel.setUpper(Integer.parseInt(getHigh(chIdx))); // 设置百分比上限
                    channel.setMiddle(Integer.parseInt(getMiddle(chIdx))); // 设置百分比中值
                }
                channel.setAbsEnable(thresholdIdx == 1); // 设置绝对值模式
            }
        }
        MeasureService.forceMeasureRefresh(); // 强制刷新测量
    }

    /**
     * 检查测量项点击是否启用
     * @return 指示器是否启用
     */
    private boolean isMeasureItemClickEnable(){ // 检查测量项点击是否启用
        return CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_SETTING_INDICATOR); // 返回指示器启用状态
    }

    /**
     * 命令到UI消费者 - 处理远程命令
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令到UI消费者
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收命令消息

            switch (commandMsgToUI.getFlag()) { // 根据命令标志处理
                case CommandMsgToUI.FLAG_Measure_Setting_Range:{ // 范围设置命令
                    int range=Integer.parseInt(commandMsgToUI.getParam()); // 解析范围参数
                    radioRange.setSelectedIndex(range); // 设置范围选择
                    onCheckChangedListener.onClick(radioRange,radioRange.getSelected()); // 触发变更处理
                }break; // 跳出

            }
        }
    };

    /**
     * 通道变更消费者 - 处理通道开关变化
     */
    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() { // 通道变更消费者
        @Override
        public void accept(MainRightMsgChannels msgChannels) throws Exception { // 接收通道变更消息
            TChan.foreachChan(chan -> { // 遍历所有通道
                boolean isOpen = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + chan); // 获取通道状态
                channelShow[TChan.toFpgaChNo(chan)] = msgChannels.getCh(TChan.toFpgaChNo(chan)).isValue(); // 更新可见性
            });
            setChannelShow(); // 刷新通道显示
            txtHigh.setText(readTxtHigh()); // 更新高阈值文本
            txtMiddle.setText(readTxtMiddle()); // 更新中阈值文本
            txtLow.setText(readTxtLow()); // 更新低阈值文本
        }
    };

    /**
     * 数学通道变更消费者
     */
    private Consumer<RightMsgMath> consumerRightMath=new Consumer<RightMsgMath>() { // 数学通道消费者
        @Override
        public void accept(RightMsgMath rightMsgMath) throws Exception { // 接收数学通道消息
            txtHigh.setText(readTxtHigh()); // 更新高阈值文本
            txtMiddle.setText(readTxtMiddle()); // 更新中阈值文本
            txtLow.setText(readTxtLow()); // 更新低阈值文本
        }
    };

    /**
     * 其他变更消费者 - 处理Math/Ref通道变化
     */
    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() { // 其他变更消费者
        @Override
        public void accept(MainRightMsgOthers msgOthers) throws Exception { // 接收其他变更消息
            TChan.foreachMath(chan -> { // 遍历数学通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan); // 获取数学通道状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan); // 获取用户添加标记
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 更新可见性
            });
            TChan.foreachRef(chan -> { // 遍历参考通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan); // 获取参考通道状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan); // 获取用户添加标记
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 更新可见性
            });
            setChannelShow(); // 刷新通道显示
            txtHigh.setText(readTxtHigh()); // 更新高阈值文本
            txtMiddle.setText(readTxtMiddle()); // 更新中阈值文本
            txtLow.setText(readTxtLow()); // 更新低阈值文本
        }
    };

    /**
     * 参考通道变更消费者
     */
    private Consumer<RightMsgRefForEight> consumerRightRef = new Consumer<RightMsgRefForEight>() { // 参考通道消费者
        @Override
        public void accept(RightMsgRefForEight msgRef) throws Exception { // 接收参考通道消息
            boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + msgRef.getRefChannelNumber()); // 获取用户添加标记
            channelShow[TChan.toFpgaChNo(msgRef.getRefChannelNumber())] = msgRef.getRefChecked().isValue() && isAddByUser; // 更新可见性
            setChannelShow(); // 刷新通道显示
            txtHigh.setText(readTxtHigh()); // 更新高阈值文本
            txtMiddle.setText(readTxtMiddle()); // 更新中阈值文本
            txtLow.setText(readTxtLow()); // 更新低阈值文本
        }
    };

    /**
     * 光标可见性变更消费者
     */
    private  Consumer<MsgCursorVisible> consumerCursorChangeVisible = new Consumer<MsgCursorVisible>() { // 光标可见性消费者
        @Override
        public void accept(MsgCursorVisible msgCursorVisible) throws Exception { // 接收光标可见性消息
            if (msgCursorVisible.isVisible()==false && msgCursorVisible.isShu()){ // 如果垂直光标关闭
                radioRange.setSelectedIndex(0); // 重置范围为百分比
                onCheckChanged(radioRange,new TopBeanChannel(0,""),false); // 触发变更处理
            }
        }
    };


    /**
     * 更新通道显示状态
     */
    private void setChannelShow() { // 设置通道显示状态
        viewChannel.setItemVisible(channelShow,true); // 设置通道可见性

        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SETTING_CHANNEL_SELECT, String.valueOf(viewChannel.getSelectedIndex())); // 保存通道选择到缓存
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPMEASURE_CHANNEL); // 通知通道可见性变更
        updateTextView(viewChannel.getSelectChannel()); // 更新阈值文本显示
    }

    /**
     * 获取测量详情接口（返回null）
     * @return null
     */
    public IMeasureDetail getMeasureDetail() { // 获取测量详情
        return null; // 返回null
    }

    /**
     * 设置详情发送消息监听器（空实现）
     * @param onDetailSendMsgListener 监听器
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置消息监听器（空实现）

    }

    /**
     * 通道颜色选择消费者
     */
    private Consumer<String> consumerSelectColor = new Consumer<String>() { // 通道颜色消费者
        @Override
        public void accept(String colorInfo) throws Throwable { // 接收颜色变更消息
            if (colorInfo.isEmpty()) return; // 空消息返回
            Logger.i(TAG, "selectColorInfo= " + colorInfo); // 打印颜色信息日志
            String[] info = colorInfo.split(";"); // 分割颜色信息
            int chIndex = Integer.parseInt(info[0]); // 解析通道索引
            String colorStr = info[1]; // 获取颜色字符串
            viewChannel.setChannelColor(chIndex, colorStr); // 设置通道颜色
        }
    };


}
