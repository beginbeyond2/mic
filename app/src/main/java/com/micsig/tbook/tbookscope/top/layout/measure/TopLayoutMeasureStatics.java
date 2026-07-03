// ============================================================
//  模块定位：measure（测量功能模块）
//  文件路径：top/layout/measure/TopLayoutMeasureStatics.java
//  核心职责：统计测量子页面Fragment，管理测量统计开关（全部/均值/最大/最小/偏差/计数）和统计弹窗显示
//  架构设计：Fragment + RxBus观察者模式，通过Consumer订阅缓存加载和硬件命令事件
//  数据流向：用户切换开关 → onStateChanged → Command发送硬件指令 → DialogMeasureStatics弹窗显示统计数据
//  依赖关系：依赖MSwitchBox开关控件、DialogMeasureStatics统计弹窗、Command硬件指令、CacheUtil缓存、MeasureManage测量管理
//  使用场景：测量功能统计子页面，用户开启/关闭统计项、重置统计数据、查看统计结果弹窗
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.measure; // 声明该类所属的包路径

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle类
import android.os.Handler; // 导入Handler异步消息处理
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.Button; // 导入按钮控件

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.base.Logger; // 导入日志工具
import com.micsig.tbook.scope.ScopeBase; // 导入示波器基础类
import com.micsig.tbook.scope.channel.BaseChannel; // 导入通道基类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂
import com.micsig.tbook.scope.measure.Measure; // 导入测量类
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载消息
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity
import com.micsig.tbook.tbookscope.R; // 导入资源ID
import com.micsig.tbook.tbookscope.main.dialog.DialogMeasureStatics; // 导入统计弹窗
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令工厂
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令消息到UI
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入事件枚举
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysProtocol; // 导入外部按键协议
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情发送消息监听接口
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage; // 导入测量管理
import com.micsig.tbook.ui.MSwitchBox; // 导入开关控件
import com.micsig.tbook.ui.util.ScreenUtil; // 导入屏幕工具
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入消费者接口


/**
 * 统计测量Fragment - 管理测量统计开关和统计弹窗显示
 */
public class TopLayoutMeasureStatics extends Fragment { // 继承Fragment，统计测量子页面
    private Context context; // 上下文对象

    private Button btnReset; // 重置按钮
    private MSwitchBox sbStaticsAll; // 全部统计开关
    private MSwitchBox sbStaticsMean; // 均值统计开关
    private MSwitchBox sbStaticsMax; // 最大值统计开关
    private MSwitchBox sbStaticsMin; // 最小值统计开关
    private MSwitchBox sbStaticsDelta; // 偏差统计开关
    private MSwitchBox sbStaticsCount; // 计数统计开关
    private DialogMeasureStatics dialogMeasurestatics; // 统计弹窗实例
    private TopMsgMeasureStatics msg=new TopMsgMeasureStatics(); // 统计消息对象
    /** 选中的测量项数量 */
    private int measureCount = 0;//选中的测量项

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
        return inflater.inflate(R.layout.layout_measure_statics, container, false); // 填充统计测量布局
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
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化视图组件
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图方法
        btnReset = (Button) view.findViewById(R.id.reset); // 获取重置按钮
        sbStaticsAll = (MSwitchBox) view.findViewById(R.id.measureStaticsAllDetail); // 获取全部统计开关
        sbStaticsMean = (MSwitchBox) view.findViewById(R.id.measureStaticsMeanDetail); // 获取均值统计开关
        sbStaticsMax = (MSwitchBox) view.findViewById(R.id.measureStaticsMaxDetail); // 获取最大值统计开关
        sbStaticsMin = (MSwitchBox) view.findViewById(R.id.measureStaticsMinDetail); // 获取最小值统计开关
        sbStaticsDelta = (MSwitchBox) view.findViewById(R.id.measureStaticsDeltaDetail); // 获取偏差统计开关
        sbStaticsCount = (MSwitchBox) view.findViewById(R.id.measureStaticsCountDetail); // 获取计数统计开关


        sbStaticsAll.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置全部开关监听
        sbStaticsMean.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置均值开关监听
        sbStaticsMax.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置最大值开关监听
        sbStaticsMin.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置最小值开关监听
        sbStaticsDelta.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置偏差开关监听
        sbStaticsCount.setOnToggleStateChangedListener(onToggleStateChangedListener); // 设置计数开关监听

        btnReset.setOnClickListener((view1)->{ // 重置按钮点击监听
            PlaySound.getInstance().playButton(); // 播放按键音效
            MeasureManage.getInstance().measureStaticReset(); // 重置统计数据
        });

        dialogMeasurestatics =(DialogMeasureStatics) ((MainActivity)this.context).findViewById(R.id.dialogMeasureStatics); // 获取统计弹窗实例
    }

    /**
     * 初始化RxBus事件订阅
     */
    private void initControl(){ // 初始化事件控制
//        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE_EX).subscribe(consumerLoadCacheEx); // 订阅缓存加载扩展事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令到UI事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_MEASURE_ITEM_COUNT).subscribe(consumerCommandMeasureOpenToUI); // 订阅测量项计数事件
    }

    /**
     * 从缓存恢复统计开关状态并发送硬件指令
     */
    private void setCache(){ // 从缓存恢复状态
        boolean all= CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL); // 读取全部统计缓存
        boolean mean=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MEAN); // 读取均值统计缓存
        boolean max=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MAX); // 读取最大值统计缓存
        boolean min=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_MIN); // 读取最小值统计缓存
        boolean delta=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_DELTA); // 读取偏差统计缓存
        boolean count=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_COUNT); // 读取计数统计缓存
        sbStaticsAll.setState(all); // 设置全部开关状态
        sbStaticsMean.setState(mean); // 设置均值开关状态
        sbStaticsMax.setState(max); // 设置最大值开关状态
        sbStaticsMin.setState(min); // 设置最小值开关状态
        sbStaticsDelta.setState(delta); // 设置偏差开关状态
        sbStaticsCount.setState(count); // 设置计数开关状态

        if (all==false){ // 如果全部统计未开启
            sbStaticsMean.setEnabled(false); // 禁用均值开关
            sbStaticsMax.setEnabled(false); // 禁用最大值开关
            sbStaticsMin.setEnabled(false); // 禁用最小值开关
            sbStaticsDelta.setEnabled(false); // 禁用偏差开关
            sbStaticsCount.setEnabled(false); // 禁用计数开关
        }
        onToggleStateChangedListener.onToggleStateChanged(sbStaticsMean,mean); // 触发均值状态变更
        onToggleStateChangedListener.onToggleStateChanged(sbStaticsMax,max); // 触发最大值状态变更
        onToggleStateChangedListener.onToggleStateChanged(sbStaticsMin,min); // 触发最小值状态变更
        onToggleStateChangedListener.onToggleStateChanged(sbStaticsDelta,delta); // 触发偏差状态变更
        onToggleStateChangedListener.onToggleStateChanged(sbStaticsCount,count); // 触发计数状态变更
        onToggleStateChangedListener.onToggleStateChanged(sbStaticsAll,all); // 触发全部状态变更

        Command.get().getMeasure_statistic().Display(all,false); // 发送全部统计显示命令
        Command.get().getMeasure_statistic().Mean(mean,false); // 发送均值统计命令
        Command.get().getMeasure_statistic().Max(max,false); // 发送最大值统计命令
        Command.get().getMeasure_statistic().Min(min,false); // 发送最小值统计命令
        Command.get().getMeasure_statistic().Dev(delta,false); // 发送偏差统计命令
        Command.get().getMeasure_statistic().Count(count,false); // 发送计数统计命令

        sendMsg(); // 发送统计消息
    }

    /**
     * 开关状态变更监听器
     */
    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() { // 开关状态变更监听器
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) { // 开关状态变更回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            onStateChanged(view, state, false); // 触发状态变更处理
        }
    };

    /**
     * 打印各控件位置信息（调试用）
     */
    private void showPosition(){ // 打印控件位置（调试用）
        new Handler().postDelayed(()->{ // 延迟2秒后执行
            Logger.i(ExternalKeysProtocol.Debug,"all:"+ ScreenUtil.getViewLocation(sbStaticsAll).toString()); // 打印全部开关位置
            Logger.i(ExternalKeysProtocol.Debug, "Mean:"+ScreenUtil.getViewLocation(sbStaticsMean).toString()); // 打印均值开关位置
            Logger.i(ExternalKeysProtocol.Debug, "max:"+ScreenUtil.getViewLocation(sbStaticsMax).toString()); // 打印最大值开关位置
            Logger.i(ExternalKeysProtocol.Debug, "min:"+ScreenUtil.getViewLocation(sbStaticsMin).toString()); // 打印最小值开关位置
            Logger.i(ExternalKeysProtocol.Debug, "delta:"+ScreenUtil.getViewLocation(sbStaticsDelta).toString()); // 打印偏差开关位置
            Logger.i(ExternalKeysProtocol.Debug, "count:"+ScreenUtil.getViewLocation(sbStaticsCount).toString()); // 打印计数开关位置
            Logger.i(ExternalKeysProtocol.Debug,"btn:"+ScreenUtil.getViewLocation(btnReset).toString()); // 打印重置按钮位置
        },2000); // 延迟2000毫秒
    }

    /**
     * 显示统计弹窗
     */
    private void showDialog(){ // 显示统计弹窗
        boolean isShowMean=sbStaticsMean.isState(); // 获取均值开关状态
        boolean isShowMax=sbStaticsMax.isState(); // 获取最大值开关状态
        boolean isShowMin=sbStaticsMin.isState(); // 获取最小值开关状态
        boolean isShowDelta=sbStaticsDelta.isState(); // 获取偏差开关状态
        boolean isShowCount=sbStaticsCount.isState(); // 获取计数开关状态
        dialogMeasurestatics.show(isShowMean,isShowMax,isShowMin,isShowDelta,isShowCount, measureCount, ScopeBase.getNewHeight()); // 显示统计弹窗
    }

    /**
     * 设置子开关的启用/禁用状态
     * @param enable true启用，false禁用
     */
    private void setDetaileEnable(boolean enable){ // 设置子开关启用状态
        sbStaticsMean.setEnabled(enable); // 设置均值开关启用状态
        sbStaticsMax.setEnabled(enable); // 设置最大值开关启用状态
        sbStaticsMin.setEnabled(enable); // 设置最小值开关启用状态
        sbStaticsDelta.setEnabled(enable); // 设置偏差开关启用状态
        sbStaticsCount.setEnabled(enable); // 设置计数开关启用状态
    }

    /**
     * 发送统计消息到RxBus
     */
    private void sendMsg(){ // 发送统计消息
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_MEASURE_STATICS,msg); // 通过RxBus发送统计消息
    }

    /**
     * 统计开关状态变更核心处理
     * @param view 触发变更的开关视图
     * @param state 新的开关状态
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onStateChanged(MSwitchBox view, boolean state, boolean isFromEventBus){ // 状态变更核心处理
        if (view.getId() == sbStaticsAll.getId()){ // 如果是全部统计开关
            MeasureManage.getInstance().measureStaticReset(); // 重置统计数据
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL,String.valueOf(state)); // 保存全部统计状态到缓存
            Command.get().getMeasure_statistic().Display(state,false); // 发送全部统计显示命令
            if (state){ // 如果开启全部统计
                showDialog(); // 显示统计弹窗
                setDetaileEnable(true); // 启用子开关
            }else { // 如果关闭全部统计
                dialogMeasurestatics.hide(); // 隐藏统计弹窗
                setDetaileEnable(false); // 禁用子开关
            }

        }else if (view.getId()==sbStaticsMean.getId()){ // 如果是均值统计开关
            boolean b=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL); // 读取全部统计状态
            if (b==false) return; // 全部统计未开启则不处理
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_STATIC_MEAN,String.valueOf(state)); // 保存均值统计状态到缓存
            Command.get().getMeasure_statistic().Mean(state,false); // 发送均值统计命令
            showDialog(); // 更新统计弹窗
        }else if (view.getId()==sbStaticsMax.getId()){ // 如果是最大值统计开关
            boolean b=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL); // 读取全部统计状态
            if (b==false) return; // 全部统计未开启则不处理
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_STATIC_MAX,String.valueOf(state)); // 保存最大值统计状态到缓存
            Command.get().getMeasure_statistic().Max(state,false); // 发送最大值统计命令
            showDialog(); // 更新统计弹窗
        }else if (view.getId()==sbStaticsMin.getId()){ // 如果是最小值统计开关
            boolean b=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL); // 读取全部统计状态
            if (b==false) return; // 全部统计未开启则不处理
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_STATIC_MIN,String.valueOf(state)); // 保存最小值统计状态到缓存
            Command.get().getMeasure_statistic().Min(state,false); // 发送最小值统计命令
            showDialog(); // 更新统计弹窗
        }else if (view.getId()==sbStaticsDelta.getId()){ // 如果是偏差统计开关
            boolean b=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL); // 读取全部统计状态
            if (b==false) return; // 全部统计未开启则不处理
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_STATIC_DELTA,String.valueOf(state)); // 保存偏差统计状态到缓存
            Command.get().getMeasure_statistic().Dev(state,false); // 发送偏差统计命令
            showDialog(); // 更新统计弹窗
        }else if (view.getId()==sbStaticsCount.getId()){ // 如果是计数统计开关
            boolean b=CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL); // 读取全部统计状态
            if (b==false) return; // 全部统计未开启则不处理
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_STATIC_COUNT,String.valueOf(state)); // 保存计数统计状态到缓存
            Command.get().getMeasure_statistic().Count(state,false); // 发送计数统计命令
            showDialog(); // 更新统计弹窗
        }
        sendMsg(); // 发送统计消息

    }

    /**
     * 根据通道ID获取硬件测量对象
     * @param chId 通道ID
     * @return 测量对象，若通道无效则返回null
     */
    private Measure getHardwareMeasure(int chId) { // 获取硬件测量对象
        BaseChannel baseChannel = null; // 初始化通道为空
        if (ChannelFactory.isDynamicCh(chId)) { // 如果是动态通道（模拟通道）
            baseChannel = ChannelFactory.getDynamicChannel(chId); // 获取动态通道
        } else if (ChannelFactory.isMathCh(chId)) { // 如果是数学通道
            baseChannel = ChannelFactory.getMathChannel(chId); // 获取数学通道
        } else if (ChannelFactory.isRefCh(chId)) { // 如果是参考通道
            baseChannel = ChannelFactory.getRefChannel(chId); // 获取参考通道
        }
        if (baseChannel != null) { // 如果通道有效
            return baseChannel.getMeasure(); // 返回通道的测量对象
        }
        return null; // 通道无效返回null
    }

    /**
     * 缓存加载消费者 - 恢复统计开关状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 从缓存恢复状态
        }
    };

    /**
     * 缓存加载扩展消费者 - 延迟1秒后恢复统计开关状态
     */
    private Consumer<LoadCache> consumerLoadCacheEx = new Consumer<LoadCache>() { // 缓存加载扩展消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载扩展事件
            new Handler().postDelayed(()->setCache(),1000); // 延迟1秒后恢复缓存状态
        }
    };

    /**
     * 命令到UI消费者 - 处理硬件返回的统计命令消息
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令到UI消费者
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收命令消息

            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发处理
                case CommandMsgToUI.FLAG_Measure_STAT_Display:{ // 统计显示命令
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam()); // 解析显示参数
                    sbStaticsAll.setState(b); // 设置全部开关状态
                    onToggleStateChangedListener.onToggleStateChanged(sbStaticsAll,b); // 触发状态变更
                }break;
                case CommandMsgToUI.FLAG_Measure_STAT_Reset:{ // 统计重置命令
                    MeasureManage.getInstance().measureStaticReset(); // 重置统计数据
                }break;
                case CommandMsgToUI.FLAG_Measure_STAT_Mean:{ // 均值统计命令
                    if (sbStaticsMean.isEnabled()==false) return; // 均值开关未启用则不处理
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam()); // 解析均值参数
                    sbStaticsMean.setState(b); // 设置均值开关状态
                    onToggleStateChangedListener.onToggleStateChanged(sbStaticsMean,b); // 触发状态变更
                }break;
                case CommandMsgToUI.FLAG_Measure_STAT_Max:{ // 最大值统计命令
                    if (sbStaticsMax.isEnabled()==false) return; // 最大值开关未启用则不处理
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam()); // 解析最大值参数
                    sbStaticsMax.setState(b); // 设置最大值开关状态
                    onToggleStateChangedListener.onToggleStateChanged(sbStaticsMax,b); // 触发状态变更
                }break;
                case CommandMsgToUI.FLAG_Measure_STAT_Min:{ // 最小值统计命令
                    if (sbStaticsMin.isEnabled()==false) return; // 最小值开关未启用则不处理
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam()); // 解析最小值参数
                    sbStaticsMin.setState(b); // 设置最小值开关状态
                    onToggleStateChangedListener.onToggleStateChanged(sbStaticsMin,b); // 触发状态变更
                }break;
                case CommandMsgToUI.FLAG_Measure_STAT_Dev:{ // 偏差统计命令
                    if (sbStaticsDelta.isEnabled()==false) return; // 偏差开关未启用则不处理
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam()); // 解析偏差参数
                    sbStaticsDelta.setState(b); // 设置偏差开关状态
                    onToggleStateChangedListener.onToggleStateChanged(sbStaticsDelta,b); // 触发状态变更
                }break;
                case CommandMsgToUI.FLAG_Measure_STAT_Count:{ // 计数统计命令
                    if (sbStaticsCount.isEnabled()==false) return; // 计数开关未启用则不处理
                    boolean b=Boolean.parseBoolean(commandMsgToUI.getParam()); // 解析计数参数
                    sbStaticsCount.setState(b); // 设置计数开关状态
                    onToggleStateChangedListener.onToggleStateChanged(sbStaticsCount,b); // 触发状态变更
                }break;
            }
        }
    };

    /**
     * 测量项计数消费者 - 处理测量项数量变更，更新统计弹窗
     */
    private Consumer<String> consumerCommandMeasureOpenToUI = new Consumer<String>() { // 测量项计数消费者
        @Override
        public void accept(String str) throws Exception { // 接收测量项计数字符串
            if(StrUtil.isEmpty(str)) return; // 字符串为空则不处理
            Logger.e("TopLayoutMeasureStatics", "measureStr= " + str); // 打印测量字符串日志
            measureCount = Integer.parseInt(str.split(CommandMsgToUI.PARAM_SPLIT)[1]); // 解析测量项数量
            if (sbStaticsAll.isState()) { // 如果全部统计已开启
                showDialog(); // 更新统计弹窗
            }
        }
    };


    /**
     * 设置详情发送消息监听器（空实现）
     * @param onDetailSendMsgListener 监听器
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置消息监听器（空实现）

    }

    /**
     * 获取测量详情接口（返回null）
     * @return null
     */
    public IMeasureDetail getMeasureDetail() { // 获取测量详情
        return null; // 返回null
    }
}
