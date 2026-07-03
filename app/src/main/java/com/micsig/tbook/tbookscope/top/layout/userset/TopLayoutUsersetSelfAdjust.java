package com.micsig.tbook.tbookscope.top.layout.userset; // 用户设置模块包声明

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.Button; // 导入按钮控件
import android.widget.TextView; // 导入文本视图控件

import androidx.annotation.Nullable; // 导入Nullable注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.base.Logger; // 导入日志工具
import com.micsig.tbook.scope.Calibrate.CalibrateService; // 导入校准服务
import com.micsig.tbook.scope.Calibrate.SelfCalibrate; // 导入自校准
import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入事件UI观察者
import com.micsig.tbook.scope.Scope; // 导入示波器核心
import com.micsig.tbook.scope.channel.Channel; // 导入通道
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 导入水平轴
import com.micsig.tbook.scope.vertical.VerticalAxis; // 导入垂直轴
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity
import com.micsig.tbook.tbookscope.MainMsgSlip; // 导入主消息滑出
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组
import com.micsig.tbook.tbookscope.R; // 导入资源引用
import com.micsig.tbook.tbookscope.main.dialog.DialogOk; // 导入确认对话框
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令消息到UI
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放
import com.micsig.tbook.tbookscope.tools.SaveManage; // 导入存储管理
import com.micsig.tbook.tbookscope.tools.ScreenControls; // 导入屏幕控制
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具
import com.micsig.tbook.tbookscope.util.DToast; // 导入自定义Toast
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 导入工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 导入工作模式管理
import com.micsig.tbook.ui.wavezone.IWave; // 导入波形接口
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道枚举

import java.util.ArrayList; // 导入动态数组

import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava Consumer

/*
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：用户设置 → 自动校准 → Fragment页面                               │
 * │ 核心职责：执行示波器自校准和自动零点校准                                    │
 * │ 架构设计：Fragment子类，通过EventBus监听校准开始/结束事件                   │
 * │ 数据流向：按钮点击 → 保存当前配置 → 执行校准 → 恢复配置 → 刷新界面        │
 * │ 依赖关系：SelfCalibrate、SaveManage、ScreenControls、Command、CacheUtil    │
 * │ 使用场景：用户设置界面中"自动校准"子页面                                   │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 自动校准Fragment，提供自校准和自动零点校准功能。
 * <p>
 * 校准流程：
 * <ol>
 *   <li>保存当前配置</li>
 *   <li>关闭菜单和Auto</li>
 *   <li>切换到YT模式</li>
 *   <li>锁定屏幕</li>
 *   <li>执行校准</li>
 *   <li>校准完成后恢复配置并刷新界面</li>
 * </ol>
 *
 * @author Administrator
 * @since 2017/4/11
 */
public class TopLayoutUsersetSelfAdjust extends Fragment {
    /** 保存当前配置的键名 */
    private static final String SAVE_CURSET_KEY = "TopLayoutUsersetSelfAdjust"; // 保存当前配置的键名

    /** 上下文对象 */
    private Context context; // 上下文对象
    /** 校准按钮 */
    private Button btnCalibration; // 校准按钮
    /** 自校准提示文本 */
    private TextView tipsSelfAdjust,tipsAdjustZero; // 自校准提示文本和零点校准提示文本
    /** 确认对话框 */
    private DialogOk dialogOk; // 确认对话框
    /** 是否正在校准 */
    private boolean isCalibration; // 是否正在校准

    /**
     * 创建Fragment视图，加载布局文件。
     *
     * @param inflater           布局填充器
     * @param container          父视图组
     * @param savedInstanceState 保存的实例状态
     * @return 创建的视图
     */
    @Nullable // 可空返回值注解
    @Override // 覆写创建视图方法
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建Fragment视图
        return inflater.inflate(R.layout.layout_usersetselfadjust, container, false); // 加载自动校准布局
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
     * 初始化控制逻辑，订阅RxBus和EventBus事件。
     */
    private void initControl() { // 初始化控制逻辑
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令消息
        EventFactory.addEventObserver(EventFactory.EVENT_SELF_CALIBRATE_BEGIN, eventUIObserver); // 订阅自校准开始事件
        EventFactory.addEventObserver(EventFactory.EVENT_SELF_CALIBRATE_END, eventUIObserver); // 订阅自校准结束事件
    }

    /**
     * 初始化视图控件，绑定按钮点击监听器。
     *
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图
        btnCalibration = (Button) view.findViewById(R.id.adjust); // 查找校准按钮
        tipsSelfAdjust = (TextView) getActivity().findViewById(R.id.tipsSelfAdjust); // 查找自校准提示文本
        tipsAdjustZero=(TextView)getActivity().findViewById(R.id.tipsAdjustZero); // 查找零点校准提示文本
        dialogOk = (DialogOk) getActivity().findViewById(R.id.dialogOk); // 查找确认对话框
        btnCalibration.setOnClickListener(onClickListener); // 设置校准按钮点击监听
    }

    /**
     * 发送自校准消息到RxBus。
     */
    private void sendMsg() { // 发送自校准消息
        RxBus.getInstance().post(RxEnum.TOP_USER_SELFADJUST, 1); // 发送自校准事件
    }

    /** 命令消息到UI消费者 */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令消息到UI消费者
        @Override // 覆写accept方法
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收命令消息
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发
                case CommandMsgToUI.FLAG_USERSET_SELFADJUST: // 自校准命令
                    startCalibration(false); // 启动自校准
                    break; // 结束分支
                case CommandMsgToUI.FLAG_USERSET_AutoZero: // 自动零点校准命令
                    int chIdx = ChannelFactory.getChActivate(); // 获取当前激活通道索引
                    if(ChannelFactory.isDynamicCh(chIdx)) { // 是动态通道
                        Channel channel = ChannelFactory.getDynamicChannel(chIdx); // 获取动态通道实例
                        activateChIdx = chIdx; // 保存通道索引
                        vScaleVal = channel.getVScaleVal()/channel.getProbeRate(); // 计算实际垂直挡位值
                        startCalibration(true); // 启动零点校准
                    }
                    break; // 结束分支
            }
        }
    };

    /** 校准按钮点击监听器 */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 校准按钮点击监听器
        @Override // 覆写onClick方法
        public void onClick(View v) { // 点击校准按钮
            PlaySound.getInstance().playButton(); // 播放按钮音效
            startCalibration(false); // 启动自校准
        }
    };


    /** 激活通道索引 */
    private int activateChIdx = -1; // 激活通道索引
    /** 垂直挡位值 */
    private double vScaleVal = 0; // 垂直挡位值
    /** 是否为零点校准模式 */
    private boolean bSelfZeroCalibration = false; // 是否为零点校准模式

    /**
     * 启动校准流程。
     * <p>
     * 流程：保存当前配置 → 关闭菜单 → 切换YT模式 → 锁屏 → 执行校准。
     *
     * @param selfZeroCalibration true=零点校准，false=自校准
     */
    public void startCalibration(boolean selfZeroCalibration) { // 启动校准
        if (!isCalibration) { // 当前未在校准中
            try { // 尝试保存当前配置
                SaveManage.getInstance().saveUserSet(SAVE_CURSET_KEY, CacheUtil.get().getCacheMap(), null); // 保存当前配置
            } catch (InterruptedException e) { // 捕获中断异常
                e.printStackTrace(); // 打印异常堆栈
            }
            Command.get().getUserset().setSelfAdjust(false); // 发送自校准命令
            RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.TOPSLIP, false));//关闭TOPSLIP菜单 // 关闭顶部滑出菜单
            RxBus.getInstance().post(RxEnum.MAINLEFT_TO_MENU_AUTO, false);//关闭Auto // 关闭Auto
            if (WorkModeManage.getInstance().getmWorkMode() != IWorkMode.WorkMode_YT) {//切换为YT模式 // 非YT模式则切换
                WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_YT, false); // 切换到YT模式
            }
            this.isCalibration = true; // 标记校准中
            CacheUtil.get().putMap(CacheUtil.SAVE_TEMP_IS_CALIBRATION,String.valueOf(isCalibration)); // 缓存校准状态
            //启动校准
            //启动自校准需要锁屏和锁按键
            Logger.i("启动自校准"); // 打印启动日志
            sendMsg(); // 发送自校准消息
            if (selfZeroCalibration){ // 零点校准模式
                tipsAdjustZero.setVisibility(View.VISIBLE); // 显示零点校准提示
                tipsSelfAdjust.setVisibility(View.GONE); // 隐藏自校准提示
            }else { // 自校准模式
                tipsSelfAdjust.setVisibility(View.VISIBLE); // 显示自校准提示
                tipsAdjustZero.setVisibility(View.GONE); // 隐藏零点校准提示
            }
            ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
            screenControls.lockScreen(ScreenControls.LOCK_SELF_ADJUST);//锁定屏幕 // 锁定屏幕
            bSelfZeroCalibration = selfZeroCalibration; // 保存校准模式

            SelfCalibrate.getInstance().setSelfZeroCalibrate(selfZeroCalibration,activateChIdx,vScaleVal); // 设置校准参数
            SelfCalibrate.getInstance().begin(); // 开始校准
        }
    }

    /** 锁屏标记 */
    private volatile boolean bLockScreen = false; // 锁屏标记
    /** 锁外部按键标记 */
    private volatile boolean bLockExternalKey = false; // 锁外部按键标记

    /**
     * 校准完成处理，恢复配置并刷新界面。
     * <p>
     * 根据校准结果显示成功/失败对话框，恢复之前保存的配置。
     *
     * @param eventBase 校准结束事件数据
     */
    public void finishedSelfCalibration(final EventBase eventBase) { // 校准完成处理
        getView().post(new Runnable() { // 在UI线程执行
            @Override // 覆写run方法
            public void run() { // UI线程执行体
                Bundle bundle = (Bundle) eventBase.getData(); // 获取事件数据Bundle
                int err = bundle.getInt(SelfCalibrate.ERROR_KEY); // 获取错误码
                if (bSelfZeroCalibration) { // 零点校准模式
                    tipsAdjustZero.setVisibility(View.GONE); // 隐藏零点校准提示
                    tipsSelfAdjust.setVisibility(View.GONE); // 隐藏自校准提示
                    if (err==0){ // 零点校准成功
                        dialogOk.setData(getResources().getString(R.string.msgAutoZeroSuccess),null,onOkClickListener); // 显示成功对话框
                    }else{ // 零点校准失败
                        dialogOk.setData(getResources().getString(R.string.msgAutoZeroFailed),null,onOkClickListener); // 显示失败对话框
                    }
                }
                else // 自校准模式
                {
                    tipsAdjustZero.setVisibility(View.GONE); // 隐藏零点校准提示
                    tipsSelfAdjust.setVisibility(View.GONE); // 隐藏自校准提示
                    if (err == 0) { // 自校准成功
                        Logger.i("finished self cablibrate, sucessful!"); // 打印成功日志
                        dialogOk.setData(R.string.msgSelfAdjustSuccess, null, onOkClickListener); // 显示成功对话框
                    } else { // 自校准失败
                        Logger.i("finished self cablibrate, err!!!!, errcode=" + err); // 打印失败日志
                        dialogOk.setData(String.format(getString(R.string.msgSelfAdjustFail), String.valueOf(err)), null, onOkClickListener); // 显示失败对话框
                    }
                }
                ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
                screenControls.unLockScreen(ScreenControls.LOCK_SELF_ADJUST); // 解锁屏幕
                //ScreenControls.getInstance().handler.sendEmptyMessage(ScreenControls.getInstance().MSG_UnLOCKSCREEN);
                isCalibration = false; // 标记校准结束
                CacheUtil.get().putMap(CacheUtil.SAVE_TEMP_IS_CALIBRATION,String.valueOf(isCalibration)); // 缓存校准状态
                Scope.getInstance().enableCommand(false); // 禁用命令发送
                CacheUtil.get().initStateCacheLoad(); // 初始化默认缓存状态
                try { // 尝试恢复配置
                    ((MainActivity) context).preMainLoadCahceProcess(); // 预加载缓存处理
                    if (!SaveManage.getInstance().loadUserSet(SAVE_CURSET_KEY, CacheUtil.get().getCacheMap())) { // 加载之前保存的配置
                        //配置载入失败则清空配置载入默认配置值
                        CacheUtil.get().clearCacheMap(); // 清除缓存
                        HorizontalAxis horizontalAxis = HorizontalAxis.getInstance(); // 获取水平轴实例
                        horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD, HorizontalAxis.TSI_2mS); // 设置时基为2ms
                        horizontalAxis.setTimePosOfView(HorizontalAxis.WPI_STANDARD, 0); // 设置时基位置为0
                        horizontalAxis.correctTimePose(); // 修正时基位置
//                        ExternalKeysProtocol.closeShift();
                        DToast.get().show("File is not exist!Load default config!!"); // 显示文件不存在提示
                    }

                    //刷新界面
                    TChan.foreachAllChan((chan)->{ // 遍历所有通道
                        Channel channel = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(chan)); // 获取动态通道
                        if(channel != null) { // 通道不为空
                            for (int j = VerticalAxis.getMinGear(); j <= VerticalAxis.getMaxGear(); j++) { // 遍历所有挡位
                                CacheUtil.get().putMapInForce(CacheUtil.MAIN_WAVE_CH_Y_ZERO_POSITION + chan + j,String.valueOf(channel.getZero())); // 缓存零点位置
                            }
                        }
                    });
//                    int pos = 0;
//                    Channel channel;
//                    for(int i = IWave.Ch1; i<=IWave.Ch4; i++){
//                        channel = ChannelFactory.getDynamicChannel(i-IWave.Ch1);
//                        if(channel != null) {
//                            for (int j = VerticalAxis.getMinGear(); j <= VerticalAxis.getMaxGear(); j++) {
//                                CacheUtil.get().putMapInForce(CacheUtil.MAIN_WAVE_CH_Y_ZERO_POSITION + i + j,String.valueOf(channel.getZero()));
//                            }
//                        }
//                    }

                    ((MainActivity) context).updateMainLoadCaheProcess(false); // 更新缓存加载处理
                    ((MainActivity) context).postMainLoadCacheProcess(); // 后置缓存加载处理
                } catch (Exception e) { // 捕获异常
                    e.printStackTrace(); // 打印异常堆栈
                }finally { // 最终处理
                    Scope.getInstance().enableCommand(true); // 恢复命令发送
                }

                for (int i = 0; i < SelfCalibrate.getJIaoZhunCnt(); i++) { // 遍历校准项
                    int id = SelfCalibrate.getCablicationID(i); // 获取校准ID
                    ArrayList<String> stringList = // 获取校准数据列表
                            bundle.getStringArrayList(CalibrateService.getInstance().getCalibrate(id).getTAG()); // 从Bundle获取校准数据
                    Logger.i("<<<========================================================================================"); // 打印分隔线
                    if (stringList != null) { // 数据不为空
                        for (String x : stringList // 遍历校准数据
                        ) {
                            Logger.i(x); // 打印校准数据
                        }
                    }
                }
                Logger.i("========================================================================================>>>"); // 打印分隔线
            }
        });
    }

    /** 确认对话框OK点击监听器（空实现） */
    private DialogOk.OnOkClickListener onOkClickListener = new DialogOk.OnOkClickListener() { // 确认对话框OK点击监听器
        @Override // 覆写onClick方法
        public void onClick(View v, Object data) { // 点击OK

        }
    };

    /** 事件UI观察者，监听自校准开始/结束事件 */
    private EventUIObserver eventUIObserver = new EventUIObserver() { // 事件UI观察者
        @Override // 覆写update方法
        public void update(Object data) { // 接收事件数据
            EventBase eventBase = (EventBase) data; // 转换为EventBase
            if (eventBase.getId() == EventFactory.EVENT_SELF_CALIBRATE_BEGIN) { // 自校准开始事件
                Command.get().getMeasure().factoryCalibration(); // 执行出厂校准
                startCalibration(bSelfZeroCalibration); // 启动校准
            } else if (eventBase.getId() == EventFactory.EVENT_SELF_CALIBRATE_END) { // 自校准结束事件
                finishedSelfCalibration(eventBase); // 处理校准完成
                isCalibration = false; // 标记校准结束
            }
        }
    };
}
