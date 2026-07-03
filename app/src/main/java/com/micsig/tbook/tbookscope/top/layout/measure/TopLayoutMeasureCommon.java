// ============================================================
//  模块定位：measure（测量功能模块）
//  文件路径：top/layout/measure/TopLayoutMeasureCommon.java
//  核心职责：常规测量页面的核心Fragment，管理测量项的选择/取消、硬件消息收发、测量数据显示
//  架构设计：Fragment + RxBus观察者模式，通过Consumer订阅各类事件，通过Command发送硬件指令
//  数据流向：用户操作 → selectListAdd/delMeasureItem → Command发送硬件指令 → EventUIObserver接收测量数据 → 更新UI
//  依赖关系：依赖MeasureAdapter/MeasureBean数据层、Command硬件指令层、RxBus事件总线、MeasureManage测量管理器
//  使用场景：测量功能常规页面，用户选择/取消测量项、配置延时/相位/TValue参数、查看测量结果
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.measure; // 声明该类所属的包路径

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle类

import android.util.Log; // 导入日志工具类
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.widget.Button; // 导入按钮控件
import android.widget.RadioButton; // 导入单选按钮控件

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类
import androidx.recyclerview.widget.GridLayoutManager; // 导入网格布局管理器
import androidx.recyclerview.widget.RecyclerView; // 导入RecyclerView

import com.micsig.base.Logger; // 导入日志工具
import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入UI事件观察者
import com.micsig.tbook.scope.ScopeBase; // 导入示波器基础类
import com.micsig.tbook.scope.channel.BaseChannel; // 导入通道基类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂
import com.micsig.tbook.scope.channel.RefChannel; // 导入参考通道类
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 导入水平轴类
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath; // 导入数学水平轴类
import com.micsig.tbook.scope.measure.Measure; // 导入测量类
import com.micsig.tbook.scope.measure.MeasureService; // 导入测量服务类
import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载消息
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组
import com.micsig.tbook.tbookscope.R; // 导入资源ID
import com.micsig.tbook.tbookscope.main.ExternalKeysMsgCursor; // 导入外部按键光标消息
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels; // 导入右侧通道消息
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers; // 导入右侧其他消息
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令工厂
import com.micsig.tbook.tbookscope.middleware.command.Command_Cursor; // 导入光标命令
import com.micsig.tbook.tbookscope.middleware.command.Command_Measure; // 导入测量命令
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight; // 导入参考通道消息
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.OnDetailSendMsgListener; // 导入详情发送消息监听接口
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogMeasureDelay; // 导入延时设置弹窗
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogMeasurePhase; // 导入相位设置弹窗
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogMeasureTValue; // 导入TValue设置弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 导入工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 导入工作模式管理
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 导入光标管理
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage; // 导入测量管理
import com.micsig.tbook.ui.MMainMenuChannel; // 导入多通道菜单控件
import com.micsig.tbook.ui.top.view.channel.TopViewChannel; // 导入通道视图
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具
import com.micsig.tbook.ui.util.TBookUtil; // 导入示波器工具
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道枚举

import java.util.ArrayList; // 导入动态数组
import java.util.Arrays; // 导入数组工具
import java.util.HashMap; // 导入哈希表
import java.util.List; // 导入列表接口
import java.util.Map; // 导入Map接口
import java.util.TreeMap; // 导入树形Map

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入消费者接口


/**
 * 常规测量Fragment - 测量功能的核心页面，负责测量项的选择管理和数据显示
 * Created by Administrator on 2017/4/5.
 */
public class TopLayoutMeasureCommon extends Fragment { // 继承Fragment作为常规测量页面容器
    private static final String TAG = "TopLayoutMeasure"; // 日志标签

    public static final String MEASURE_DATA_INIT = "----"; // 测量数据初始值（未获取到数据时显示）
    public static final int MEASURE_INDEX_DELAY = Measure.MeasureType.MEASURE_DELAY - Measure.MeasureType.MEASURE_FIRST;//延时测量项索引偏移量
    public static final int MEASURE_INDEX_PHASE = Measure.MeasureType.MEASURE_PHASE - Measure.MeasureType.MEASURE_FIRST;//相位测量项索引偏移量
    public static final int MEASURE_INDEX_COLVAL = Measure.MeasureType.MEASURE_COLV - Measure.MeasureType.MEASURE_FIRST;//列值测量项索引偏移量
    public static final int MEASURE_INDEX_TVALUE = Measure.MeasureType.MEASURE_TVALUE - Measure.MeasureType.MEASURE_FIRST;//TValue测量项索引偏移量

    private Context context; // 上下文对象
    /**
     * 此列中的数据，channelID的是从0始计算的<p></p>
     * 底层数据列表和此列表相同，channelId是从0始计算的
     */
    private ArrayList<MeasureBean> measureBeanList = new ArrayList<MeasureBean>(); // 所有可用测量项的数据列表
    //不显示的测量项
    private List<MeasureBean> hideMeasureBeanList = new ArrayList<>(); // 隐藏的测量项列表（如ColValQ等特殊项）
    /**
     * 已选择列表中的数据，由于是跟波形区相关的，所以channelId的是从1始的
     */
    private ArrayList<MeasureBean> measureSelectList = new ArrayList<MeasureBean>(); // 已选择的测量项列表


    int [] noArray = new int[GlobalVar.get().getMeasureItemCount()]; // 编号占用数组，用于分配空闲编号
    /**
     * 查找空闲编号
     * @return 可用的空闲编号，若无则返回数组长度
     */
    private int findFreeNumber(){ // 查找空闲的测量项编号
        Arrays.fill(noArray,0); // 清空编号占用标记
        for(MeasureBean bean:measureSelectList){ // 遍历已选测量项
            int no = bean.getNo(); // 获取当前项编号
            if(no >= 0 && no < noArray.length){ // 如果编号在有效范围内
                noArray[no] = 1; // 标记该编号已占用
            }
        }
        int i = 0; // 初始化查找索引
        for( i=0;i<noArray.length;i++){ // 遍历编号数组
            if(noArray[i] == 0){ // 找到第一个空闲位置
                break; // 跳出循环
            }
        }
        return i; // 返回找到的空闲编号（或数组末尾）
    }

    private static final int measureSelectMaxNum = GlobalVar.get().getMeasureItemCount();//可选中的最大数量
    private static final int measureSpanCount = 16;//每行显示数量（待选测量项）
    private static final int measureSelectSpanCount = 14;//选中的每行显示数量（已选测量项）
    private int allMeasureNums = 27;//所有测量项总数
    private MeasureAdapter measureAdapter, measureSelectAdapter; // 待选和已选两个列表的适配器
    private Button btnClear; // 清空所有测量项的按钮
    private TopDialogMeasureDelay delayDialog; // 延时参数设置弹窗
    private TopDialogMeasurePhase phaseDialog; // 相位参数设置弹窗
    private TopDialogMeasureTValue tvalueDialog; // TValue参数设置弹窗
    private MMainMenuChannel viewChannel; // 通道选择菜单控件
    //Ch1--Ch8
    //Math1--Math8
    //R1--R8
    //S1--S4
    private boolean[] channelShow = { // 各通道的可见性数组（共28个槽位）
            false, false, false, false, false, false, false, false, // Ch1-Ch8
            false, false, false, false, false, false, false, false, // Math1-Math8
            false, false, false, false, false, false, false, false, // R1-R8
            false, false, false, false // S1-S4
    };
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
        return inflater.inflate(R.layout.layout_measure_common, container, false); // 填充常规测量布局
    }

    /**
     * 视图创建完成后的初始化
     * @param view 根视图
     * @param savedInstanceState 保存的状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        rootView = (ViewGroup) view; // 保存根视图引用
        initView(view); // 初始化视图组件
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化各子视图组件
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图方法
        initChannelView(view); // 初始化通道选择视图
        initMeasureView(view); // 初始化测量项列表视图
        initMeasureSelectView(view); // 初始化已选测量项列表视图
        Button button = (Button) view.findViewById(R.id.indicator); // 获取指示器按钮
        button.setVisibility(View.INVISIBLE); // 隐藏指示器按钮
        btnClear = (Button) view.findViewById(R.id.clear); // 获取清空按钮
        btnClear.setOnClickListener(onClickListener); // 设置清空按钮点击监听
        delayDialog = (TopDialogMeasureDelay) ((MainActivity) context).findViewById(R.id.dialogMeasureDelay); // 获取延时设置弹窗实例
        phaseDialog = (TopDialogMeasurePhase) ((MainActivity) context).findViewById(R.id.dialogMeasurePhase); // 获取相位设置弹窗实例
        tvalueDialog = (TopDialogMeasureTValue) ((MainActivity) context).findViewById(R.id.dialogMeasureTValue); // 获取TValue设置弹窗实例
    }

    /**
     * 初始化通道选择视图
     * @param view 根视图
     */
    private void initChannelView(View view) { // 初始化通道选择视图
        viewChannel = (MMainMenuChannel) view.findViewById(R.id.chanMeasureCommon); // 获取通道选择控件
        viewChannel.setChangeListener(onChannelItemClickListener, null); // 设置通道切换监听器
    }

    /**
     * 初始化待选测量项列表视图
     * @param view 根视图
     */
    private void initMeasureView(View view) { // 初始化待选测量项列表
        RecyclerView rvMeasureList = (RecyclerView) view.findViewById(R.id.measureList); // 获取待选测量项RecyclerView
        rvMeasureList.setLayoutManager(new GridLayoutManager(context, measureSpanCount)); // 设置网格布局（每行16个）
        String[] measures = getResources().getStringArray(R.array.measures); // 获取所有测量项名称数组
        allMeasureNums = measures.length; // 记录测量项总数
        for (int i = 0; i < measures.length; i++) { // 遍历所有测量项名称
            measureBeanList.add(new MeasureBean(i, measures[i], viewChannel.getSelectChannel() + 1, getMeasureDrawable()[i], false)); // 创建并添加测量项Bean
        }
        measureAdapter = new MeasureAdapter(context, measureBeanList); // 创建待选列表适配器
        measureAdapter.setItemClickListener(onMeasureItemClickListener); // 设置测量项点击监听
        rvMeasureList.setAdapter(measureAdapter); // 设置适配器到RecyclerView
    }

    /**
     * 获取所有测量项对应的图标资源ID数组
     * @return 图标资源ID数组
     */
    private int[] getMeasureDrawable() { // 获取测量项图标数组
        return new int[]{ // 返回所有测量项的图标资源ID
                R.drawable.ic_measure_period, // 周期图标
                R.drawable.ic_measure_frequency, // 频率图标
                R.drawable.ic_measure_raisetime, // 上升时间图标
                R.drawable.ic_measure_falltime, // 下降时间图标
                R.drawable.ic_measure_delay, // 延时图标
                R.drawable.ic_measure_pdc, // 正占空比图标
                R.drawable.ic_measure_ndc, // 负占空比图标
                R.drawable.ic_measure_ppw, // 正脉冲宽度图标
                R.drawable.ic_measure_npw, // 负脉冲宽度图标
                R.drawable.ic_measure_bpw, // 突发宽度图标
                R.drawable.ic_measure_pos, // 正超调图标
                R.drawable.ic_measure_nos, // 负超调图标
                R.drawable.ic_measure_phase, // 相位图标
                R.drawable.ic_measure_pkpk, // 峰峰值图标
                R.drawable.ic_measure_amp, // 幅值图标
                R.drawable.ic_measure_high, // 高电平图标
                R.drawable.ic_measure_low, // 低电平图标
                R.drawable.ic_measure_max, // 最大值图标
                R.drawable.ic_measure_min, // 最小值图标
                R.drawable.ic_measure_rms, // RMS均方根图标
                R.drawable.ic_measure_crms, // CRMS交流均方根图标
                R.drawable.ic_measure_mean, // 平均值图标
                R.drawable.ic_measure_cmean, // CMean交流平均值图标
                R.drawable.ic_measure_mean_ac, // ACRMS交流均方根图标
                R.drawable.ic_measure_risetime_rate, // 上升速率图标
                R.drawable.ic_measure_falltime_rate, // 下降速率图标
                R.drawable.ic_measure_tvolt, // TValue电压图标
        };
    }

    /**
     * 初始化已选测量项列表视图
     * @param view 根视图
     */
    private void initMeasureSelectView(View view) { // 初始化已选测量项列表
        RecyclerView rvMeasureSelectList = (RecyclerView) view.findViewById(R.id.measureSelectList); // 获取已选测量项RecyclerView
        rvMeasureSelectList.setLayoutManager(new GridLayoutManager(context, measureSelectSpanCount)); // 设置网格布局（每行14个）
        measureSelectList.clear(); // 清空已选列表
        measureSelectAdapter = new MeasureAdapter(context, measureSelectList); // 创建已选列表适配器
        measureSelectAdapter.setItemClickListener(onMeasureItemClickListener); // 设置点击监听
        rvMeasureSelectList.setAdapter(measureSelectAdapter); // 设置适配器
    }

    /**
     * 初始化RxBus事件订阅和观察者注册
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerMainRightChannels); // 订阅右侧通道变更事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers); // 订阅右侧其他变更事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_REF).subscribe(consumerRightRef); // 订阅参考通道变更事件
        RxBus.getInstance().getObservable(RxEnum.BOTTOMLAYOUT_ALLMEASURE).subscribe(consumerAllMeasure); // 订阅全测量开关事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMANDMEASUREOPEN_TO_UI).subscribe(consumerCommandMeasureOpenToUI); // 订阅测量打开命令
        RxBus.getInstance().getObservable(RxEnum.COMMANDMEASURECLOSE_TO_UI).subscribe(consumerCommandMeasureCloseToUI); // 订阅测量关闭命令

        RxBus.getInstance().getObservable(RxEnum.TOP_USER_SELFADJUST).subscribe(consumerUserSelfAdjust); // 订阅用户自调整事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_UPDATE_MEASURE_ITEM_POS).subscribe(consumerUpdateMeasureItemPos); // 订阅测量项位置更新
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_DELETE_MEASURE_ITEM_POS).subscribe(consumerDeleteMeasureItemPos); // 订阅测量项删除
        EventFactory.addEventObserver(EventFactory.EVENT_CH_MEASURE_UPDATE, eventUIObserver); // 注册模拟通道测量更新观察者
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_MEASURE_UPDATE, eventUIObserver); // 注册数学通道测量更新观察者
        EventFactory.addEventObserver(EventFactory.EVENT_REF_MEASURE_UPDATE, eventUIObserver); // 注册参考通道测量更新观察者
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor); // 订阅通道颜色选择事件
        setChannelShow(); // 更新通道显示状态
    }

    /**
     * 从缓存恢复测量项选择状态
     */
    private void setCache() { // 从缓存恢复状态
        //Cache中保存的channelId是从1始的，这是因为是从选择列表得的数据保存的
        String noStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_NO); // 获取编号缓存字符串
        String channelStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_CHANNEL); // 获取通道缓存字符串
        String positionStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_INDEX); // 获取索引缓存字符串
        int channelSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_CHANNEL_SELECT); // 获取通道选择索引

        ArrayList<String> nolList = StrUtil.getListFromString(noStrings,CacheUtil.MEASURE_SELECT_LIST_SLIP); // 解析编号列表
        ArrayList<String> channelList = StrUtil.getListFromString(channelStrings, CacheUtil.MEASURE_SELECT_LIST_SLIP); // 解析通道列表
        ArrayList<String> indexList = StrUtil.getListFromString(positionStrings, CacheUtil.MEASURE_SELECT_LIST_SLIP); // 解析索引列表

        Logger.i("TopLayoutMeasureCommon nolList= " + nolList); // 打印编号列表日志
        Logger.i("TopLayoutMeasureCommon channelList= " + channelList); // 打印通道列表日志
        Logger.i("TopLayoutMeasureCommon indexList= " + indexList); // 打印索引列表日志
        measureSelectList.clear(); // 清空已选列表
        delAll(); // 清空所有测量项
        for (int i = 0; i < channelList.size(); i++) { // 遍历缓存中的通道列表
            int index = Integer.parseInt(indexList.get(i)); // 解析测量项索引
            int channel = Integer.parseInt(channelList.get(i)); // 解析通道编号
            int no = -1; // 初始化编号为-1
            if(i < nolList.size()){ // 如果编号列表足够长
                no = Integer.parseInt(nolList.get(i)); // 解析编号
            }

            MeasureBean measureBean; // 测量项Bean引用
            if (index < 0) { // 如果索引为负（空测量项）
                measureBean = new MeasureBean(index, "", channel, 0); // 创建空测量项
            } else { // 正常测量项
                measureBean = measureBeanList.get(index); // 从列表获取对应测量项
            }
            MeasureBean measureSelect = new MeasureBean(measureBean.getIndex(), measureBean.getName(), channel, measureBean.getDrawableResId()); // 创建已选测量项副本
//                measureSelectList.add(measureSelect);
//                addMeasureItem(measureSelect.getChannel(), measureSelect.getIndex(), measureSelect.getName(), MEASURE_DATA_INIT);

            measureSelect.setNo(no); // 设置编号

            String[] delays = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_DELAY_DATA + measureSelect.getChannel()).split(CacheUtil.DELAY_SLIP); // 获取延时参数缓存
            delayDialog.setCache(Integer.parseInt(delays[0]), Integer.parseInt(delays[1]), Integer.parseInt(delays[2])); // 恢复延时弹窗缓存
            int phase = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_PHASE_DATA + measureSelect.getChannel()); // 获取相位参数缓存
            phaseDialog.setCache(phase); // 恢复相位弹窗缓存
            String[] tvalue = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_TVALUE_DATA + measureSelect.getChannel()).split(CacheUtil.DELAY_SLIP); // 获取TValue参数缓存

            double vol=0; int edgeOccurence=1; int cursorIndex=0; // 初始化TValue参数变量
            try { // 尝试解析TValue参数
                vol = Double.parseDouble(tvalue[0]); // 解析电压值
                edgeOccurence = Integer.parseInt(tvalue[1]); // 解析边沿出现次数
                cursorIndex = Integer.parseInt(tvalue[2]); // 解析光标索引
            }catch (Exception e){ // 解析异常
                e.printStackTrace(); // 打印异常堆栈
            }
            tvalueDialog.setCache(vol,edgeOccurence,cursorIndex); // 恢复TValue弹窗缓存

            if (measureBean.getIndex() == MEASURE_INDEX_DELAY) { // 如果是延时测量项
                if (selectListAdd(measureSelect, Integer.parseInt(delays[0]), Integer.parseInt(delays[1]), Integer.parseInt(delays[2]))) { // 添加延时测量项
                    sendHardwareMsg(measureSelect.getChannel() - 1, MEASURE_INDEX_DELAY, true, new int[]{Integer.parseInt(delays[0]), Integer.parseInt(delays[1]), Integer.parseInt(delays[2])}, null); // 发送硬件延时消息
                }
            } else if (measureBean.getIndex() == MEASURE_INDEX_PHASE) { // 如果是相位测量项
                if (selectListAdd(measureSelect, phase)) { // 添加相位测量项
                    sendHardwareMsg(measureSelect.getChannel() - 1, MEASURE_INDEX_PHASE, true, null, new int[]{phase}); // 发送硬件相位消息
                }
            } else if (measureBean.getIndex() == MEASURE_INDEX_TVALUE) { // 如果是TValue测量项
                if (selectListAdd(measureSelect, phase)) { // 添加TValue测量项
                    sendHardwareMsg(measureSelect.getChannel() - 1, MEASURE_INDEX_TVALUE, true,new int[]{edgeOccurence}, null, vol); // 发送硬件TValue消息
                }
                cursorTValueTrace(measureSelect,cursorIndex); // 设置TValue光标追踪
            }else { // 其他普通测量项
                if (selectListAdd(measureSelect)) { // 添加普通测量项
                    sendHardwareMsg(measureSelect.getChannel() - 1, measureBean.getIndex(), true, null, null); // 发送硬件消息
                }
            }
        }
        measureSelectAdapter.notifyDataSetChanged(); // 通知已选列表适配器数据变更

        viewChannel.setChangeListener(null, null); // 临时移除通道变更监听
        viewChannel.setChecked(channelSelect); // 设置通道选中状态
        viewChannel.setChangeListener(onChannelItemClickListener, null); // 恢复通道变更监听
        boolean ch1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch1); // 获取Ch1开启状态
        boolean ch2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch2); // 获取Ch2开启状态
        boolean ch3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch3); // 获取Ch3开启状态
        boolean ch4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch4); // 获取Ch4开启状态
        boolean ch5 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch5); // 获取Ch5开启状态
        boolean ch6 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch6); // 获取Ch6开启状态
        boolean ch7 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch7); // 获取Ch7开启状态
        boolean ch8 = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + TChan.Ch8); // 获取Ch8开启状态

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
            if (TChan.isMath(chan)) { // 如果是数学通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + chan); // 获取数学通道开启状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + chan); // 获取用户添加标记
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 设置可见性
            }
            if (TChan.isRef(chan)) { // 如果是参考通道
                boolean isCheck = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_REF_CHECK + chan); // 获取参考通道开启状态
                boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + chan); // 获取用户添加标记
                channelShow[TChan.toFpgaChNo(chan)] = isCheck && isAddByUser; // 设置可见性
            }
        });
        setChannelShow(); // 更新通道显示
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPMEASURE_SELECT); // 通知测量选择可见性变更
        addEmptyMeasure(); // 添加空测量项占位
    }

    /**
     * 添加空测量项占位，用于保持测量项位置对齐
     */
    private void addEmptyMeasure() { // 添加空测量项占位
        String channelStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_CHANNEL); // 获取通道缓存
        String positionStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_INDEX); // 获取索引缓存
        ArrayList<String> channelList = StrUtil.getListFromString(channelStrings, CacheUtil.MEASURE_SELECT_LIST_SLIP); // 解析通道列表
        ArrayList<String> indexList = StrUtil.getListFromString(positionStrings, CacheUtil.MEASURE_SELECT_LIST_SLIP); // 解析索引列表
        for (int i = 0; i < channelList.size() - 1; i++) { // 遍历除最后一个外的所有项
            int channel = Integer.parseInt(channelList.get(i)); // 解析通道编号
            int measureId = Integer.parseInt(indexList.get(i)); // 解析测量项ID
            if (channel < 0) { // 如果通道为负（空测量项）
                MeasureManage.MeasureItemStruct item = MeasureManage.getInstance().new MeasureItemStruct(-1,channel, measureId, "", ""); // 创建空测量项结构
                MeasureManage.getInstance().getMeasureItem().addEmptyMeasureItem(i, item); // 添加空测量项占位
            }
        }
        MeasureManage.getInstance().getMeasureItem().clearRowEmpty();//若一行都为空，则删除
    }

    /**
     * 将测量项添加到已选列表
     * @param measureBean 待添加的测量项
     * @param param 延时/相位/TValue参数
     * @return 是否添加成功
     */
    private boolean selectListAdd(MeasureBean measureBean, int... param) { // 添加测量项到已选列表（可变参数版）
        return selectListAdd(measureBean,0,param); // 委托给完整版方法
    }

    /**
     * 将测量项添加到已选列表（完整版）
     * @param measureBean 待添加的测量项
     * @param a TValue的电压值参数
     * @param param 延时/相位/TValue参数
     * @return 是否添加成功
     */
    private boolean selectListAdd(MeasureBean measureBean, double a, int... param){ // 添加测量项到已选列表

        Tools.PrintControlsLocation("TopLayoutMeasureCommon", rootView); // 打印控件位置调试信息
        //如果已选择列表中包含当前通道的延时相位，则界面上不做修改，只需返回true用以修改底层数据
        if ((param.length == 1 && measureBean.getIndex() == MEASURE_INDEX_PHASE)) { // 如果是相位且参数长度为1
            for (int i = measureSelectList.size() - 1; i >= 0; i--) { // 逆序遍历已选列表
                MeasureBean item = measureSelectList.get(i); // 获取当前项
                if (item.getIndex() == MEASURE_INDEX_PHASE && item.getChannel() == measureBean.getChannel()) { // 如果找到同通道的相位项
                    return true; // 返回true，表示底层需要更新但界面不变
                }
            }
        } else if ((param.length == 3 && measureBean.getIndex() == MEASURE_INDEX_DELAY)) { // 如果是延时且参数长度为3
            for (int i = measureSelectList.size() - 1; i >= 0; i--) { // 逆序遍历已选列表
                MeasureBean item = measureSelectList.get(i); // 获取当前项
                if (item.getIndex() == MEASURE_INDEX_DELAY && item.getChannel() == measureBean.getChannel()) { // 如果找到同通道的延时项
                    return true; // 返回true，表示底层需要更新但界面不变
                }
            }
        }

        if (measureSelectList.size() < measureSelectMaxNum) { // 如果未达到最大选择数量
            for (MeasureBean item : measureSelectList) { // 遍历已选列表
                if (item.equals(measureBean)) { // 如果已存在相同项
                    return item.getIndex() == MEASURE_INDEX_TVALUE // TValue允许同通道重复
                            && item.getChannel() == measureBean.getChannel(); // 返回是否为TValue同通道
                }
            }

            if(measureBean.getNo() < 0) { // 如果编号未分配
                int no = findFreeNumber(); // 查找空闲编号
                if (no >= 0 && no < measureSelectMaxNum) { // 如果编号有效
                    measureBean.setNo(no); // 分配编号
//                    Command.get().getMeasure().setListMeasureNo(measureBean.getChannel(), measureBean.getIndex(),no);
                }
            }
            measureSelectList.add(measureBean); // 添加到已选列表
            measureSelectAdapter.notifyDataSetChanged(); // 通知适配器更新
            // 添加
            addMeasureItem(measureBean.getNo(),measureBean.getChannel(), measureBean.getIndex(), measureBean.getName(), MEASURE_DATA_INIT); // 添加测量项到管理器
            saveMeasureSelect(); // 保存选择状态到缓存
            setStaticsItemEnable(measureBean.getChannel()-1,measureBean.getIndex(),true); // 启用统计项
            if (param.length == 0 ) { // 无额外参数（普通测量项）
                Command.get().getMeasure().Open(measureBean.getNo(),measureBean.getIndex(), measureBean.getChannel() - 1, 0, 0, 0,0, false); // 发送打开测量命令
            } else if (param.length == 1
                    && measureBean.getIndex() == MEASURE_INDEX_PHASE) { //相位
                Command.get().getMeasure().Open(measureBean.getNo(),measureBean.getIndex(), measureBean.getChannel() - 1, param[0], 0, 0, 0, false); // 发送打开相位命令
            } else if (param.length == 3
                    && measureBean.getIndex() == MEASURE_INDEX_DELAY) { //延时
                Command.get().getMeasure().Open(measureBean.getNo(),measureBean.getIndex(), measureBean.getChannel() - 1, param[0], param[1], param[2], 0, false); // 发送打开延时命令
            } else if (param.length == 1
                    && measureBean.getIndex() == MEASURE_INDEX_TVALUE){ // TValue
                Command.get().getMeasure().Open(measureBean.getNo(),measureBean.getIndex(), measureBean.getChannel() - 1, param[0], 0, 0, a,false); // 发送打开TValue命令
            }
            return true; // 添加成功
        }
        return false; // 达到最大数量，添加失败
    }

    /**
     * 保存已选测量项状态到缓存
     */
    private void saveMeasureSelect() { // 保存选择状态到缓存
        ArrayList<String> listIndex = new ArrayList<>(); // 索引列表
        ArrayList<String> listChannel = new ArrayList<>(); // 通道列表
        ArrayList<String> listNo = new ArrayList<>(); // 编号列表
//        for (MeasureBean item : measureSelectList) {
//            listIndex.add(String.valueOf(item.getIndex()));
//            listChannel.add(String.valueOf(item.getChannel()));
//        }

        List<MeasureManage.MeasureItemStruct> list = MeasureManage.getInstance().getMeasureItem().getMeasureList(); // 获取测量管理器中的列表
        for (MeasureManage.MeasureItemStruct itemStruct : list) { // 遍历测量项
            listIndex.add(String.valueOf(itemStruct.getMeasureId())); // 添加测量项ID
            listChannel.add(String.valueOf(itemStruct.getChannelId())); // 添加通道ID
            listNo.add(String.valueOf(itemStruct.getNo())); // 添加编号
        }
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_INDEX, StrUtil.getStringFromList(listIndex, CacheUtil.MEASURE_SELECT_LIST_SLIP)); // 保存索引到缓存
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_CHANNEL, StrUtil.getStringFromList(listChannel, CacheUtil.MEASURE_SELECT_LIST_SLIP)); // 保存通道到缓存
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_NO, StrUtil.getStringFromList(listNo, CacheUtil.MEASURE_SELECT_LIST_SLIP)); // 保存编号到缓存
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPMEASURE_SELECT); // 通知可见性变更
        updateMeasureBeanState(); // 更新测量项Bean的选中状态
    }


    /**
     * 更新待选测量项列表中的选中状态
     */
    private void updateMeasureBeanState() { // 更新测量项Bean状态
        ArrayList<String> listIndex = new ArrayList<>(); // 索引列表
        ArrayList<String> listChannel = new ArrayList<>(); // 通道列表
//        for (MeasureBean item : measureSelectList) {
//            listIndex.add(String.valueOf(item.getIndex()));
//            listChannel.add(String.valueOf(item.getChannel()));
//        }

        List<MeasureManage.MeasureItemStruct> list = MeasureManage.getInstance().getMeasureItem().getMeasureList(); // 获取测量管理器列表
        for (MeasureManage.MeasureItemStruct itemStruct : list) { // 遍历测量项
            listIndex.add(String.valueOf(itemStruct.getMeasureId())); // 添加测量项ID
            listChannel.add(String.valueOf(itemStruct.getChannelId())); // 添加通道ID
        }

        for (int i = 0; i < measureBeanList.size(); i++) { // 遍历待选列表
            measureBeanList.get(i).setSelect(false); // 重置为未选中
            measureBeanList.get(i).setChannel(viewChannel.getSelectChannel() + 1); // 设置为当前选中通道
        }
        for (int i= 0; i< listChannel.size(); i++) { // 遍历已选列表
            if (Integer.parseInt(listChannel.get(i)) < 0) continue; // 跳过空测量项
            if(Integer.parseInt(listChannel.get(i)) == TChan.toUiChNo(viewChannel.getSelectChannel())
                    && ChannelFactory.isChOpen(viewChannel.getSelectChannel())) { // 如果通道匹配且已开启
                measureBeanList.get(Integer.parseInt(listIndex.get(i))).setSelect(true); // 标记为选中
            }
        }
        measureAdapter.notifyDataSetChanged(); // 通知适配器更新
    }


    /**
     * 更新通道显示状态
     */
    private void setChannelShow() { // 设置通道显示状态
        viewChannel.setItemVisible(channelShow,true); // 设置通道可见性
        RxBus.getInstance().post(RxEnum.CONTROLS_VISIBLE_CHANGED, MainViewGroup.VISIBLE_TOPMEASURE_CHANNEL); // 通知通道可见性变更
        updateMeasureBeanState(); // 更新测量项Bean状态
    }

    /**
     * 设置详情发送消息监听器（空实现）
     * @param onDetailSendMsgListener 监听器
     */
    public void setOnDetailSendMsgListener(OnDetailSendMsgListener onDetailSendMsgListener) { // 设置详情消息监听器（空实现）

    }

    /**
     * 获取测量详情接口（返回null）
     * @return null
     */
    public IMeasureDetail getMeasureDetail() { // 获取测量详情
        return null; // 返回null
    }

    /**
     * 右侧通道变更消费者 - 处理通道开关状态变化
     */
    private Consumer<MainRightMsgChannels> consumerMainRightChannels = new Consumer<MainRightMsgChannels>() { // 通道变更消费者
        @Override
        public void accept(MainRightMsgChannels msgChannels) throws Exception { // 接收通道变更消息
            TChan.foreachChan(chan -> { // 遍历所有通道
                boolean isOpen = CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE + chan); // 获取通道开启状态
                channelShow[TChan.toFpgaChNo(chan)] = msgChannels.getCh(TChan.toFpgaChNo(chan)).isValue(); // 更新通道可见性
            });
            setChannelShow(); // 刷新通道显示
        }
    };

    /**
     * 右侧其他变更消费者 - 处理Math/Ref通道变化
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
        }
    };

    /**
     * 参考通道变更消费者 - 处理参考通道开关变化
     */
    private Consumer<RightMsgRefForEight> consumerRightRef = new Consumer<RightMsgRefForEight>() { // 参考通道消费者
        @Override
        public void accept(RightMsgRefForEight msgRef) throws Exception { // 接收参考通道消息
            //哪个通道变化 设置哪个通道
            boolean isAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_REF + msgRef.getRefChannelNumber()); // 获取用户添加标记
            channelShow[TChan.toFpgaChNo(msgRef.getRefChannelNumber())] = msgRef.getRefChecked().isValue() && isAddByUser; // 更新可见性
            setChannelShow(); // 刷新通道显示
        }
    };

    /**
     * 全测量开关消费者 - 处理AllMeasure开关变化
     */
    private Consumer<Boolean> consumerAllMeasure = new Consumer<Boolean>() { // 全测量开关消费者
        @Override
        public void accept(Boolean b) throws Exception { // 接收全测量开关状态
            int ch = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT); // 获取当前选中通道
            //设置对应通道的measureAll数据计算的打开与关闭，并关闭其他通道的数据
            for (int i = ChannelFactory.CH1; i < ChannelFactory.REF_MAX; i++) { // 遍历所有通道
                for (int j = 0; j < measureBeanList.size(); j++) { // 遍历所有测量项
                    MeasureBean bean = measureBeanList.get(j); // 获取测量项
                    String[] delays = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_DELAY_DATA + (i + 1)).split(CacheUtil.DELAY_SLIP); // 获取延时缓存
                    sendHardwareMsg(i, bean.getIndex(), b && i == ch // 仅当前选中通道打开
                            , new int[]{Integer.parseInt(delays[0]), Integer.parseInt(delays[1]), Integer.parseInt(delays[2])} // 延时参数
                            , new int[]{CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_PHASE_DATA + (i + 1))}); // 相位参数
                }
            }
            //打开对用的单项的测量数据
            for (int i = 0; i < measureSelectList.size(); i++) { // 遍历已选列表
                MeasureBean bean = measureSelectList.get(i); // 获取已选测量项
                if(bean.getIndex() == MeasureManage.IMeasure.MeasureId_TVALUE) { // 如果是TValue
                    String[] tvalue = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_TVALUE_DATA + bean.getChannel()).split(CacheUtil.DELAY_SLIP); // 获取TValue缓存

                    double vol; int edgeOccurence; int cursorIndex; // TValue参数变量
                    vol = Double.parseDouble(tvalue[0]); // 解析电压值
                    edgeOccurence = Integer.parseInt(tvalue[1]); // 解析边沿出现次数
                    cursorIndex = Integer.parseInt(tvalue[2]); // 解析光标索引
                    sendHardwareMsg(bean.getChannel() - 1, bean.getIndex(), true // 发送TValue硬件消息
                            , new int[]{edgeOccurence} // 延时参数
                            , new int[]{edgeOccurence},vol); // 相位参数和电压值
                    cursorTValueTrace(bean,cursorIndex); // 设置TValue光标追踪
                }else{ // 非TValue测量项
                    String[] delays = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_DELAY_DATA + (bean.getChannel())).split(CacheUtil.DELAY_SLIP); // 获取延时缓存
                    sendHardwareMsg(bean.getChannel() - 1, bean.getIndex(), true // 发送硬件消息
                            , new int[]{Integer.parseInt(delays[0]), Integer.parseInt(delays[1]), Integer.parseInt(delays[2])} // 延时参数
                            , new int[]{CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_PHASE_DATA + (bean.getChannel()))}); // 相位参数
                }
            }
        }
    };

    /**
     * 缓存加载消费者 - 接收缓存加载事件后恢复状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 恢复缓存状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutMeasureCommon, true); // 标记缓存已加载
        }
    };



    /**
     * 测量打开命令消费者 - 处理远程/命令打开测量项
     */
    private Consumer<Command_Measure.MeasureInfo> consumerCommandMeasureOpenToUI = new Consumer<Command_Measure.MeasureInfo>() { // 测量打开命令消费者
        @Override
        public void accept(Command_Measure.MeasureInfo measureInfo) throws Exception { // 接收打开命令
//            Logger.i(TAG,"measure.type:"+measureInfo.measureType);
            // 26为所有测量项
            checkAndUpdateSelectList(); // 检查并更新已选列表
            if (measureInfo.measureType >= 0 && measureInfo.measureType < allMeasureNums) { // 如果是UI可见的测量项
                addUIMeasureItem(measureInfo); // 添加UI测量项
            } else { // 如果是隐藏的测量项
                addHideMeasureItem(measureInfo); // 添加隐藏测量项
            }

        }
    };

    /**
     * 添加UI可见的测量项
     * @param measureInfo 测量项信息
     */
    private void addUIMeasureItem(Command_Measure.MeasureInfo measureInfo) { // 添加UI测量项
        MeasureBean measureBean = measureBeanList.get(measureInfo.measureType); // 获取对应的测量项Bean
        MeasureBean measureBean2 = new MeasureBean(measureBean.getIndex(), measureBean.getName(), measureInfo.measureChannel + 1, measureBean.getDrawableResId()); // 创建副本
        measureBean2.setNo(measureInfo.getNo()); // 设置编号
        if (measureSelectList.size() < GlobalVar.get().getMeasureItemCount()) { // 如果未达到最大数量
            for (MeasureBean item : measureSelectList) { // 遍历已选列表
                if (item.equals(measureBean2)) { // 如果已存在
                    if(measureInfo.measureType == MeasureManage.IMeasure.MeasureId_TVALUE){ // 如果是TValue
                        int[] param = {measureInfo.param1}; // 构造参数
                        sendHardwareMsg(item.getChannel() - 1, item.getIndex(), true, param, null,measureInfo.dparam1); // 发送硬件消息

                        cursorTValueTrace(measureBean2,measureInfo.param2); // 设置TValue光标追踪
                    }
                    return; // 已存在则返回
                }
            }
//            measureSelectList.add(measureBean2);
            // 添加
            addMeasureItem(measureBean2.getNo(),measureBean2.getChannel(), measureBean2.getIndex(), measureBean2.getName(), MEASURE_DATA_INIT); // 添加到管理器
            setStaticsItemEnable(measureBean2.getChannel()-1, measureBean2.getIndex(),true); // 启用统计项
//            Logger.i(TAG,"type:"+measureInfo.measureType);
            switch (measureInfo.measureType) { // 根据测量类型处理
                case 4: { // 延时测量
                    int selfPosition=measureInfo.param2; // 自身位置
                    int otherChannel=measureInfo.param1; // 对方通道
                    int otherPosition=measureInfo.param3; // 对方位置
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_DELAY_DATA + measureBean2.getChannel(), selfPosition + CacheUtil.DELAY_SLIP + otherChannel + CacheUtil.DELAY_SLIP + otherPosition); // 保存延时参数到缓存
                    if (selectListAdd(measureBean2, selfPosition, otherChannel, otherPosition)) { // 添加到已选列表
                        int[] param = {measureInfo.param2, measureInfo.param1, measureInfo.param3}; // 构造参数数组
                        sendHardwareMsg(measureBean2.getChannel() - 1, measureBean2.getIndex(), true, param, null); // 发送硬件消息
                    }
                }
                break; // 跳出
                case 12: { // 相位测量
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_PHASE_DATA + measureBean2.getChannel(), String.valueOf(measureInfo.param1)); // 保存相位参数到缓存
                    if (selectListAdd(measureBean2, measureInfo.param1)) { // 添加到已选列表
                        int[] param = {measureInfo.param1}; // 构造参数数组
                        sendHardwareMsg(measureBean2.getChannel() - 1, measureBean2.getIndex(), true, null, param); // 发送硬件消息
                    }
                }
                break; // 跳出
                case MeasureManage.IMeasure.MeasureId_TVALUE:{ // TValue测量
                    Log.d(TAG,"MeasureId_TVALUE:" + measureInfo.param2); // 打印TValue日志
                    int cursoridx = measureInfo.param2; // 获取光标索引

                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_TVALUE_DATA + measureBean2.getChannel(), measureInfo.dparam1 + CacheUtil.DELAY_SLIP + measureInfo.param1 + CacheUtil.DELAY_SLIP + cursoridx); // 保存TValue参数到缓存
                    if(selectListAdd(measureBean2,measureInfo.dparam1,measureInfo.param1)) { // 添加到已选列表
                        int[] param = {measureInfo.param1}; // 构造参数数组
                        sendHardwareMsg(measureBean2.getChannel() - 1, measureBean2.getIndex(), true, param, null, measureInfo.dparam1); // 发送硬件消息
                    }
                    cursorTValueTrace(measureBean2,cursoridx); // 设置TValue光标追踪

                }
                break; // 跳出


                default: { // 其他普通测量项
                    if (selectListAdd(measureBean2, new int[0])) { // 添加到已选列表
                        sendHardwareMsg(measureBean2.getChannel() - 1, measureBean2.getIndex(), true, null, null); // 发送硬件消息
                    }
                }
                break; // 跳出
            }
            measureSelectAdapter.notifyDataSetChanged(); // 通知适配器更新
            saveMeasureSelect(); // 保存选择状态
        }
    }

    /**
     * 添加隐藏的测量项（如ColValQ等不在UI显示的测量项）
     * @param measureInfo 测量项信息
     */
    private void addHideMeasureItem(Command_Measure.MeasureInfo measureInfo) { // 添加隐藏测量项
        switch (measureInfo.measureType) { // 根据测量类型处理
            case MeasureManage.IMeasure.MeasureId_ColValQ: { // ColValQ测量项
                MeasureBean bean = new MeasureBean(measureInfo.measureType, "", measureInfo.measureChannel, 0); // 创建隐藏测量项Bean
                hideMeasureBeanList.add(bean); // 添加到隐藏列表
                int[] param = {measureInfo.param1}; // 构造参数数组
                sendHardwareMsg(measureInfo.measureChannel, measureInfo.measureType, true, null, param); // 发送硬件消息
//                Logger.i(TAG, "23 add");
            }

        }
    }

    /**
     * 测量关闭命令消费者 - 处理远程/命令关闭测量项
     */
    private Consumer<Command_Measure.MeasureInfo> consumerCommandMeasureCloseToUI = new Consumer<Command_Measure.MeasureInfo>() { // 测量关闭命令消费者
        @Override
        public void accept(Command_Measure.MeasureInfo measureInfo) throws Exception { // 接收关闭命令
            if (measureInfo.flag == Command_Measure.MeasureInfo.FLAG_ClearAll) { // 如果是清除所有
                measureSelectList.clear(); // 清空已选列表
                measureSelectAdapter.notifyDataSetChanged(); // 通知适配器更新
                hideMeasureBeanList.clear(); // 清空隐藏列表
//                MeasureManage.getInstance().getMeasureItem().delAllMeasureItem();
                delAll(); // 清空所有测量项
                saveMeasureSelect(); // 保存状态
//                Logger.i(TAG,"clear all!");
            } else { // 关闭单个测量项
                if (measureInfo != null) { // 如果消息不为空
//                    Log.d(TAG,"measureInfo.measureChannel:" + measureInfo.measureChannel
//                            + ",measureInfo.measureType:" + measureInfo.measureType);

                    MeasureManage.getInstance().getMeasureItem().delMeasureItem(measureInfo.measureChannel+1, measureInfo.measureType); // 从管理器删除

                    try { // 尝试从列表中删除
                        int index = Tools.indexOf(measureSelectList, s -> s.getChannel() == measureInfo.measureChannel+1 && s.getIndex() == measureInfo.measureType); // 查找在已选列表中的位置
                        if(index >= 0) { // 如果找到
                            measureSelectList.remove(index); // 从已选列表删除
                        }
                        measureSelectAdapter.notifyDataSetChanged(); // 通知适配器更新
                        index = Tools.indexOf(hideMeasureBeanList, s -> s.getChannel() == measureInfo.measureChannel && s.getIndex() == measureInfo.measureType); // 查找在隐藏列表中的位置
                        if(index >= 0) { // 如果找到
                            hideMeasureBeanList.remove(index); // 从隐藏列表删除
                        }
                    }catch (Exception e){ // 异常处理

                    }

                    if(!isMeasureAll(measureInfo.measureChannel+1,measureInfo.measureType) // 如果不是AllMeasure
                            || measureInfo.measureType == MEASURE_INDEX_COLVAL) { // 或者是ColVal类型

//                        Log.d(TAG,"close ch:" + measureInfo.measureChannel + ",type:" + measureInfo.measureType);

                        sendHardwareMsg(measureInfo.measureChannel, measureInfo.measureType, false, null, null); // 发送关闭硬件消息
                    }


                    saveMeasureSelect(); // 保存状态
                    //hideMeasureBeanList.remove(new MeasureBean(measureInfo.measureType, "", measureInfo.measureChannel , 0));
//                    Logger.i(TAG,"clear item type:"+measureInfo.measureType);
                }
            }
        }
    };

    /**
     * 用户自调整消费者 - 自调整时清空所有测量项
     */
    private Consumer<Integer> consumerUserSelfAdjust = new Consumer<Integer>() { // 用户自调整消费者
        @Override
        public void accept(Integer integer) throws Exception { // 接收自调整事件
            if (measureSelectList.size() != 0) { // 如果有已选测量项
                onClickListener.onClick(btnClear); // 触发清空操作
            }
        }
    };

    /**
     * 测量项位置更新消费者 - 处理拖拽排序
     */
    private Consumer<String> consumerUpdateMeasureItemPos = new Consumer<String>() { // 测量项位置更新消费者
        @Override
        public void accept(String posStr) throws Throwable { // 接收位置更新消息
            String[] posInfo = posStr.split(","); // 分割位置信息
            int fromPos = Integer.parseInt(posInfo[0]); // 起始位置
            int toPos = Integer.parseInt(posInfo[1]); // 目标位置
            boolean isSwap = Boolean.parseBoolean(posInfo[2]); // 是否交换
//            if (isSwap) {
//                Tools.swapElement(measureSelectList, fromPos, toPos);
//            } else {
//                Tools.insertElement(measureSelectList, fromPos, toPos);
//            }
            updateSelectMeasureList(); // 更新已选列表顺序
            measureSelectAdapter.notifyDataSetChanged(); // 通知适配器更新
            saveMeasureSelect(); // 保存状态
        }
    };

    /**
     * 检查并更新已选列表，确保与测量管理器同步
     */
    private void checkAndUpdateSelectList() { // 检查并更新已选列表
        List<MeasureManage.MeasureItemStruct> validList = MeasureManage.getInstance().getMeasureItem().getValidMeasureList(); // 获取有效测量项列表
        if (measureSelectList.size() != validList.size()) { // 如果数量不一致
            updateSelectMeasureList(); // 更新已选列表
        }
    }

    /**
     * 从测量管理器同步更新已选测量项列表
     */
    private void updateSelectMeasureList() { // 更新已选测量项列表
        ArrayList<MeasureBean> tempList = new ArrayList<>(); // 临时列表
        List<MeasureManage.MeasureItemStruct> list = MeasureManage.getInstance().getMeasureItem().getValidMeasureList(); // 获取有效列表
        for (MeasureManage.MeasureItemStruct itemStruct : list) { // 遍历有效列表
            for (MeasureBean measureBean : measureSelectList) { // 遍历已选列表
                if (itemStruct.getChannelId() == measureBean.getChannel() && itemStruct.getMeasureId() == measureBean.getIndex()) { // 匹配通道和测量ID
                    tempList.add(measureBean); // 添加到临时列表
                    break; // 跳出内层循环
                }
            }
        }
        measureSelectList.clear(); // 清空已选列表
        measureSelectList.addAll(tempList); // 用临时列表替换
    }

    /**
     * 测量项删除消费者 - 处理拖拽删除
     */
    private Consumer<String> consumerDeleteMeasureItemPos = new Consumer<String>() { // 测量项删除消费者
        @Override
        public void accept(String deleteItemInfo) throws Throwable { // 接收删除消息
            if (StrUtil.isEmpty(deleteItemInfo)) return; // 空消息则返回
            Logger.i("TopLayoutMeasureCommon deleteItemInfo= " + deleteItemInfo); // 打印删除信息日志
            String[] strs = deleteItemInfo.split(" "); // 分割删除信息
            int channelId = Integer.parseInt(strs[0]); // 解析通道ID
            int measureId = Integer.parseInt(strs[1]); // 解析测量项ID
            measureSelectList.removeIf(bean -> bean.getChannel() < 0); // 移除所有空通道项
            MeasureBean temp = null; // 临时引用
            for (MeasureBean bean : measureSelectList) { // 遍历已选列表
                if (bean.getChannel() == channelId && bean.getIndex() == measureId) { // 匹配通道和测量ID
                    temp = bean; // 记录匹配项
                    break; // 跳出循环
                }
            }
            if (temp == null) return; // 未找到则返回
            int i = measureSelectList.indexOf(temp); // 获取在列表中的位置
            measureSelectList.remove(temp); // 从列表中移除
            measureSelectAdapter.notifyDataSetChanged(); // 通知适配器更新
            //删除
            delMeasureItem(temp.getChannel(), temp.getIndex(), i); // 从管理器删除
            saveMeasureSelect(); // 保存状态
            setStaticsItemEnable(temp.getChannel() - 1, temp.getIndex(), false); // 禁用统计项
            if (!isMeasureAll(temp.getChannel(), temp.getIndex())) { // 如果不是AllMeasure
                sendHardwareMsg(temp.getChannel() - 1, temp.getIndex(), false, null, null); // 发送关闭硬件消息
            }
        }
    };


    /**
     * 通道选择变更监听器
     */
    private TopViewChannel.onItemClickListener onChannelItemClickListener = new TopViewChannel.onItemClickListener() { // 通道选择监听器
        @Override
        public void checkChanged(int viewId, int checkedIndex,RadioButton radioButton) { // 通道切换回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_CHANNEL_SELECT, String.valueOf(checkedIndex)); // 保存通道选择到缓存
            saveMeasureSelect(); // 保存测量项状态
        }
    };



    /**
     * 判断指定通道的测量项是否属于AllMeasure
     * @param chIdx 通道索引（从1开始）
     * @param measureIdx 测量项索引
     * @return 是否属于AllMeasure
     */
    private boolean isMeasureAll(int chIdx,int measureIdx){ // 判断是否属于AllMeasure

        MeasureManage.AllMeasure allMeasure = MeasureManage.getInstance().getAllMeasure(); // 获取AllMeasure实例
        if (allMeasure.isVisible() && allMeasure.getChIdx() == chIdx) {//更新AllMeasure

            return measureIdx != MEASURE_INDEX_DELAY && measureIdx != MEASURE_INDEX_PHASE; // 延时和相位不属于AllMeasure
        }
        return false; // 不属于AllMeasure
    }

    /**
     * 清空按钮点击监听器
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 清空按钮监听器
        @Override
        public void onClick(View v) { // 点击事件处理
            PlaySound.getInstance().playButton(); // 播放按键音效
            int len = measureSelectList.size(); // 获取已选列表大小
            for (int i = 0; i < len; i++) { // 遍历所有已选测量项
                MeasureBean bean = measureSelectList.get(i); // 获取测量项
                if(!isMeasureAll(bean.getChannel(),bean.getIndex())) { // 如果不属于AllMeasure
                    sendHardwareMsg(bean.getChannel() - 1, bean.getIndex(), false, null, null); // 发送关闭硬件消息
                }
            }
            measureSelectList.clear(); // 清空已选列表
            measureSelectAdapter.notifyDataSetChanged(); // 通知适配器更新
            //清空所有
            delAll(); // 清空管理器中的所有测量项
            saveMeasureSelect(); // 保存状态

//            Measure measure = getHardwareMeasure(viewChannel.getSelectChannel());
//            if (measure != null) {
//                measure.MeasureCalc();
//            }
        }
    };

    /**
     * TValue确认监听器 - 处理TValue参数设置确认
     */
    private TopDialogMeasureTValue.OnSureListener onTValueSureListener = new TopDialogMeasureTValue.OnSureListener() { // TValue确认监听器
        @Override
        public void onSure(MeasureBean measureBean, double vol, int edgeOccurence, int cursorIndex) { // 确认回调
            PlaySound.getInstance().playButton(); // 播放按键音效

            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_TVALUE_DATA + TChan.toUiChNo(viewChannel.getSelectChannel()), vol + CacheUtil.DELAY_SLIP + edgeOccurence + CacheUtil.DELAY_SLIP + cursorIndex); // 保存TValue参数到缓存
            MeasureBean measureBean2 = new MeasureBean(measureBean.getIndex(), measureBean.getName(), viewChannel.getSelectChannel() + 1, R.drawable.ic_measure_tvolt); // 创建TValue测量项
            if (selectListAdd(measureBean2, vol,edgeOccurence)) { // 添加到已选列表
                sendHardwareMsg(viewChannel.getSelectChannel(), MEASURE_INDEX_TVALUE, true, new int[]{edgeOccurence}, null, vol); // 发送硬件消息
            }
            cursorTValueTrace(measureBean,cursorIndex); // 设置TValue光标追踪
        }
    };

    /**
     * 设置TValue光标追踪
     * @param measureBean 测量项
     * @param cursorIndex 光标索引（0=无追踪，1=X1，2=X2）
     */
    private void cursorTValueTrace(MeasureBean measureBean, int cursorIndex){ // 设置TValue光标追踪

        boolean bCursor = false; // 是否需要设置光标追踪
        int chIdx = measureBean.getChannel() - 1; // 获取通道索引（从0开始）
        int X1 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X1); // 获取X1光标通道
        int X2 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X2); // 获取X2光标通道

        Log.d(TAG,"chIdx:" + chIdx + ",cursorIndex:" + cursorIndex + ",X1:" + X1 + ",X2:" + X2); // 打印调试日志

        switch (cursorIndex){ // 根据光标索引处理
            case 1: // 追踪X1光标
                X1 = chIdx; // 设置X1为当前通道
                if(X2 == chIdx){ // 如果X2也是当前通道
                    X2 = -1; // 清除X2
                }
                bCursor = true; // 标记需要设置光标
                break; // 跳出
            case 2: // 追踪X2光标
                X2 = chIdx; // 设置X2为当前通道
                if(X1 == chIdx){ // 如果X1也是当前通道
                    X1 = -1; // 清除X1
                }
                bCursor = true; // 标记需要设置光标
                break; // 跳出
            default: // 不追踪
                if(X1 == chIdx){ // 如果X1是当前通道
                    X1 = -1; // 清除X1
                }
                if(X2 == chIdx){ // 如果X2是当前通道
                    X2 = -1; // 清除X2
                }
                break; // 跳出
        }

//        if(X1 == X2){
//            new Throwable().printStackTrace();
//        }

        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X1,String.valueOf(X1)); // 保存X1到缓存
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X2,String.valueOf(X2)); // 保存X2到缓存


        if(bCursor){ // 如果需要设置光标追踪
            CursorManage cursorManage = CursorManage.getInstance(); // 获取光标管理器实例
            if(!cursorManage.getColVisible()){ // 如果垂直光标不可见
                RxBus.getInstance().post(RxEnum.EXTERNALKEYS_CURSOR, new ExternalKeysMsgCursor(false, ExternalKeysMsgCursor.TYPE_OPEN)); // 打开垂直光标
            }
            if(cursorManage.isCursorTrace()) { // 如果光标追踪已开启
//                cursorManage.setCursorTrace(false);
                Command.get().getCursor().Trace(false,true); // 关闭光标追踪
            }

            if(MeasureService.isCursorRang()){ // 如果光标范围模式开启
                Command.get().getMeasure_setting().Range(0, true); // 关闭光标范围模式
            }

        }
        //这里将光标追踪按钮置灰
        RxBus.getInstance().post(RxEnum.MSG_TVALUE_ENABLE, true); // 通知TValue光标追踪启用状态
    }

    /**
     * 重置TValue光标缓存
     * @param chId 通道ID（从0开始）
     */
    private void resetTValueCurSorCache(int chId) { // 重置TValue光标缓存

        String[] tValues = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_TVALUE_DATA + (chId + 1)).split(CacheUtil.DELAY_SLIP); // 获取TValue缓存
        int cursorIndex = Integer.parseInt(tValues[2]); // 解析光标索引
        int X1 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X1); // 获取X1缓存
        int X2 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X2); // 获取X2缓存
        Log.d(TAG,"cursorIndex:" + cursorIndex + ",X1:" + X1 + ",X2:" + X2 + ",chId:" + chId); // 打印调试日志
        switch (cursorIndex) { // 根据光标索引处理
            case 1: // X1追踪
                if (X1 == chId) { // 如果X1是当前通道
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X1, String.valueOf("-1")); // 清除X1
                }
                break; // 跳出
            case 2: // X2追踪
                if (X2 == chId) { // 如果X2是当前通道
                    CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X2, String.valueOf("-1")); // 清除X2
                }
                break; // 跳出
            default: // 不追踪
//                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X1, String.valueOf("-1"));
//                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X2, String.valueOf("-1"));
                break; // 跳出
        }
        RxBus.getInstance().post(RxEnum.MSG_TVALUE_ENABLE, true); // 通知TValue光标追踪启用状态
    }


    /**
     * 相位确认监听器 - 处理相位参数设置确认
     */
    private TopDialogMeasurePhase.OnSureListener onPhaseSureListener = new TopDialogMeasurePhase.OnSureListener() { // 相位确认监听器
        @Override
        public void onSure(MeasureBean measureBean, String selfChannel, int otherChannelIndex) { // 确认回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_PHASE_DATA + (viewChannel.getSelectChannel() + 1), String.valueOf(otherChannelIndex)); // 保存相位参数到缓存
            MeasureBean measureBean2 = new MeasureBean(measureBean.getIndex(), measureBean.getName(), viewChannel.getSelectChannel() + 1, R.drawable.ic_measure_phase); // 创建相位测量项
            if (selectListAdd(measureBean2, otherChannelIndex)) { // 添加到已选列表
                sendHardwareMsg(viewChannel.getSelectChannel(), MEASURE_INDEX_PHASE, true, null, new int[]{otherChannelIndex}); // 发送硬件消息
            }
        }
    };

    /**
     * 延时确认监听器 - 处理延时参数设置确认
     */
    private TopDialogMeasureDelay.OnSureListener onDelaySureListener = new TopDialogMeasureDelay.OnSureListener() { // 延时确认监听器
        @Override
        public void onSure(MeasureBean measureBean, String selfChannel, int selfPosition, int otherChannel, int otherPosition) { // 确认回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_MEASURE_DELAY_DATA + (viewChannel.getSelectChannel() + 1), selfPosition + CacheUtil.DELAY_SLIP + otherChannel + CacheUtil.DELAY_SLIP + otherPosition); // 保存延时参数到缓存
            MeasureBean measureBean2 = new MeasureBean(measureBean.getIndex(), measureBean.getName(), viewChannel.getSelectChannel() + 1, R.drawable.ic_measure_delay); // 创建延时测量项
            if (selectListAdd(measureBean2, selfPosition, otherChannel, otherPosition)) { // 添加到已选列表
                sendHardwareMsg(viewChannel.getSelectChannel(), MEASURE_INDEX_DELAY, true, new int[]{selfPosition, otherChannel, otherPosition}, null); // 发送硬件消息
            }
        }
    };

    /**
     * 测量项点击监听器 - 处理待选/已选列表的点击事件
     */
    private MeasureAdapter.OnItemClickListener onMeasureItemClickListener = new MeasureAdapter.OnItemClickListener() { // 测量项点击监听器
        @Override
        public void onClick(MeasureAdapter adapter, MeasureBean measureBean) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            checkAndUpdateSelectList(); // 检查并更新已选列表
            String[] measureStrings = context.getResources().getStringArray(R.array.measures); // 获取测量项名称数组
            RadioButton radioButton = viewChannel.getSelectedRadioButton(); // 获取选中的通道单选按钮
            if (adapter.hashCode() == measureAdapter.hashCode()) { // 如果点击的是待选列表
                if (measureBean.isSelect()) { // 如果测量项已选中（点击取消）
                    measureSelectAdapter.getItemClickListener().onClick(measureSelectAdapter, measureBean); // 委托给已选列表处理
                } else { // 未选中（点击添加）
                    int chIdx = viewChannel.getSelectChannel(); // 获取当前选中通道索引
                    if (!channelShow[chIdx]) return; // 如果通道不可见则返回
                    measureBean.setChannel(chIdx); // 设置测量项通道
                    if (measureStrings[MEASURE_INDEX_DELAY].equals(measureBean.getName())) {//延时
                        if (ChannelFactory.isRefCh(chIdx)) return; // 参考通道不支持延时
                        String[] delays = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_DELAY_DATA + (chIdx + 1)).split(CacheUtil.DELAY_SLIP); // 获取延时缓存
                        delayDialog.setCache(Integer.parseInt(delays[0]), Integer.parseInt(delays[1]), Integer.parseInt(delays[2])); // 设置延时弹窗缓存
                        delayDialog.setData(radioButton.getText().toString(), measureBean, onDelaySureListener); // 设置延时弹窗数据
                    } else if (measureStrings[MEASURE_INDEX_PHASE].equals(measureBean.getName())) {//相位
                        if (ChannelFactory.isRefCh(chIdx)) return; // 参考通道不支持相位
                        String phase = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_PHASE_DATA + (chIdx + 1)); // 获取相位缓存
                        phaseDialog.setCache(Integer.parseInt(phase)); // 设置相位弹窗缓存
                        phaseDialog.setData(radioButton.getText().toString(), measureBean, onPhaseSureListener); // 设置相位弹窗数据
                    } else if (measureStrings[MEASURE_INDEX_TVALUE].equals(measureBean.getName())) {//TValue
                        String[] tValues = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_TVALUE_DATA + (chIdx + 1)).split(CacheUtil.DELAY_SLIP); // 获取TValue缓存
                        tvalueDialog.setCache(Double.parseDouble(tValues[0]), Integer.parseInt(tValues[1]), Integer.parseInt(tValues[2])); // 设置TValue弹窗缓存
                        tvalueDialog.setData(radioButton.getText().toString(), measureBean, onTValueSureListener); // 设置TValue弹窗数据
                    } else { // 普通测量项
                        MeasureBean measureBean2 = new MeasureBean(measureBean.getIndex(), measureBean.getName(), chIdx + 1, measureBean.getDrawableResId()); // 创建测量项副本
                        if (selectListAdd(measureBean2)) { // 添加到已选列表
                            sendHardwareMsg(chIdx, measureBean.getIndex(), true, null, null); // 发送硬件消息
                        }
                    }
                }
            } else if (adapter.hashCode() == measureSelectAdapter.hashCode()) { // 如果点击的是已选列表（取消选择）
                int i = measureSelectList.indexOf(measureBean); // 获取在列表中的位置
                measureSelectList.remove(measureBean); // 从列表中移除
                measureSelectAdapter.notifyDataSetChanged(); // 通知适配器更新
                //删除
                delMeasureItem(measureBean.getChannel(), measureBean.getIndex(), i); // 从管理器删除
                saveMeasureSelect(); // 保存状态
                setStaticsItemEnable(measureBean.getChannel() - 1, measureBean.getIndex(), false); // 禁用统计项
//                Command.get().getMeasure().Clear(i, false);
                if(!isMeasureAll(measureBean.getChannel(),measureBean.getIndex())) { // 如果不属于AllMeasure
                    sendHardwareMsg(measureBean.getChannel() - 1, measureBean.getIndex(), false, null, null); // 发送关闭硬件消息
                }
            }
        }
    };

    /**
     * 发送硬件测量消息（无电压参数版本）
     * @param chId 通道ID（从0开始）
     * @param measureId 测量项ID
     * @param isOpen 是否打开
     * @param delay 延时参数数组
     * @param phase 相位参数数组
     */
    private void sendHardwareMsg(int chId, int measureId, boolean isOpen, int[] delay, int[] phase){ // 发送硬件消息（无电压参数）
        sendHardwareMsg(chId,measureId,isOpen,delay,phase,0); // 委托给完整版方法
    }

    /**
     * 发送硬件测量消息（完整版）
     * @param chId 通道ID（从0开始）
     * @param measureId 测量项ID
     * @param isOpen 是否打开
     * @param delay 延时参数数组
     * @param phase 相位参数数组
     * @param v TValue电压值
     */
    private void sendHardwareMsg(int chId, int measureId, boolean isOpen, int[] delay, int[] phase,double v) { // 发送硬件消息（完整版）
//        Log.d(TAG, "sendHardwareMsg() called with: chId = [" + chId + "], measureId = [" + measureId + "], isOpen = [" + isOpen + "], delay = [" + delay + "], phase = [" + phase + "], v = [" + v + "]");
        boolean bImmed = false; // 是否需要立即刷新

        Measure measure = getHardwareMeasure(chId); // 获取硬件测量对象
//        Logger.i(TAG," measure: " + (measure!=null?"true":"false")+",measureId:"+measureId+",chId:"+chId);
        if (measure != null) { // 如果测量对象有效
            if (measureId == MEASURE_INDEX_DELAY) { // 延时测量
                if (isOpen) { // 打开
                    if (delay != null) { // 有延时参数
                        measure.MeasureDelay(delay[0], delay[1], delay[2]); // 设置延时参数
                        measure.MeasureDelayEnable(true); // 启用延时测量
                    }
                } else { // 关闭
                    measure.MeasureDelayEnable(false); // 禁用延时测量
                }
            } else if (measureId == MEASURE_INDEX_PHASE) { // 相位测量
                if (isOpen) { // 打开
                    if (phase != null) { // 有相位参数
                        measure.MeasurePhase(phase[0]); // 设置相位参数
                        measure.MeasurePhaseEnable(true); // 启用相位测量
                    }
                } else { // 关闭
                    measure.MeasurePhaseEnable(false); // 禁用相位测量
                }
            } else if (measureId == MEASURE_INDEX_COLVAL) { // ColVal测量
                if (isOpen) { // 打开
                    if (phase != null) { // 有参数
                        measure.MeasureCol(phase[0]); // 设置ColVal参数
                    }
                }
            }else if(measureId == MEASURE_INDEX_TVALUE){ // TValue测量
                if (isOpen) { // 打开
                    if (delay != null) { // 有参数
                        measure.setTValue(v,delay[0]); // 设置TValue参数
                    }
                } else { // 关闭
                    resetTValueCurSorCache(chId); // 重置TValue光标缓存
                }
                bImmed = true; // TValue需要立即刷新
            }
            measure.MeasureItemEnable(measureId + Measure.MeasureType.MEASURE_FIRST, isOpen); // 启用/禁用测量项
            if (ChannelFactory.isDynamicCh(chId)) { // 模拟通道
                MeasureService.forceChMeasureRefresh(); // 强制刷新模拟通道测量
            } else if (ChannelFactory.isMathCh(chId)) { // 数学通道
                MeasureService.forceMathMeasureRefresh(); // 强制刷新数学通道测量
            } else if (ChannelFactory.isRefCh(chId)) { // 参考通道
                MeasureService.forceRefMeasureRefresh(); // 强制刷新参考通道测量
            }
            if(bImmed){ // 如果需要立即刷新
                MeasureService.forceImmedMeasure(chId); // 强制立即刷新测量
            }
        }
    }

    /**
     * 获取硬件测量对象
     * @param chId 通道ID（从0开始）
     * @return Measure对象，通道无效时返回null
     */
    private Measure getHardwareMeasure(int chId) { // 获取硬件测量对象
        BaseChannel baseChannel = null; // 通道基类引用
        if (ChannelFactory.isDynamicCh(chId)) { // 模拟通道
            baseChannel = ChannelFactory.getDynamicChannel(chId); // 获取模拟通道
        } else if (ChannelFactory.isMathCh(chId)) { // 数学通道
            baseChannel = ChannelFactory.getMathChannel(chId); // 获取数学通道
        } else if (ChannelFactory.isRefCh(chId)) { // 参考通道
            baseChannel = ChannelFactory.getRefChannel(chId); // 获取参考通道
        }
        if (baseChannel != null) { // 如果通道有效
            return baseChannel.getMeasure(); // 返回测量对象
        }
        return null; // 通道无效返回null
    }

    /**
     * 设置统计项启用/禁用状态
     * @param chIdx 通道索引（从0开始）
     * @param measureId 测量项ID
     * @param bEnable 是否启用
     */
    private void setStaticsItemEnable(int chIdx,int measureId,boolean bEnable){ // 设置统计项启用状态

        Measure measure = getHardwareMeasure(chIdx); // 获取硬件测量对象
        if(measure != null){ // 如果测量对象有效
            measure.setStaticsItemEnable(measureId + Measure.MeasureType.MEASURE_PERIOD,bEnable); // 设置统计项启用状态
        }
    }

    /**
     * 添加测量项到管理器
     * @param no 编号
     * @param channelId 通道ID
     * @param measureId 测量项ID
     * @param measureName 测量项名称
     * @param data 测量数据
     */
    private void addMeasureItem(int no,int channelId, int measureId, String measureName, String data) { // 添加测量项到管理器
        MeasureManage.MeasureItemStruct item = MeasureManage.getInstance().new MeasureItemStruct(no,channelId, measureId, measureName, data); // 创建测量项结构
        MeasureManage.getInstance().getMeasureItem().addMeasureItem(item); // 添加到管理器

    }

    /**
     * 更新测量项数据
     * @param no 编号
     * @param channelId 通道ID
     * @param measureId 测量项ID
     * @param measureName 测量项名称
     * @param data 测量数据
     */
    void updateMeasureItem(int no,int channelId, int measureId, String measureName, String data) { // 更新测量项数据
        MeasureManage.MeasureItemStruct item = MeasureManage.getInstance().new MeasureItemStruct(no,channelId, measureId, measureName, data); // 创建测量项结构
        MeasureManage.getInstance().getMeasureItem().updateMeasureItem(item); // 更新管理器中的数据
    }

    /**
     * 更新测量指示器位置
     */
    void updateMeasureIndication(){ // 更新测量指示器
        int chIdx = 0; // 通道索引
        int idx = 0; // 测量项索引
        Measure measure=null; // 测量对象
        MeasureManage.MeasureIndication measureIndication = MeasureManage.getInstance().getMeasureIndication(); // 获取测量指示器

        for (int i = 0; i < measureSelectList.size(); i++) {//更新SelectMeasure
            MeasureBean bean = measureSelectList.get(i); // 获取已选测量项
            measure = getHardwareMeasure(bean.getChannel() - 1); // 获取硬件测量对象
            idx = bean.getIndex() + Measure.MeasureType.MEASURE_FIRST; // 计算完整测量项索引
            chIdx = bean.getChannel()-1; // 获取通道索引
            if(measureIndication.getChIdx() == chIdx // 如果通道匹配
                    && measureIndication.getMeasureId() == idx) { // 且测量项匹配
                break; // 跳出循环
            }
        }
        if(com.micsig.tbook.scope.Display.Display.getInstance().isXYMode()){ // 如果是XY模式
            measure = null; // XY模式下不显示测量指示
        }
        if(measure != null // 测量对象有效
                && ChannelFactory.isChOpen(chIdx) // 通道已开启
                && measure.isMeasureItemValid(idx)){ // 测量项有效
            for (int j = 0; j < MeasureManage.MeasureIndication.MEASURE_INDICATION_MAX; j++) { // 遍历指示器位置
                int v = measure.getIndication(idx, j); // 获取指示位置值
                measureIndication.setVisiable(j, v >= 0); // 设置可见性
                measureIndication.setPos(j, v); // 设置位置
            }
        }else{ // 无效情况
            measureIndication.setVisiable(false); // 隐藏指示器
        }

    }

    /**
     * 删除单个测量项
     * @param channelId 通道ID
     * @param measureId 测量项ID
     * @param index 在列表中的位置
     */
    private void delMeasureItem(int channelId, int measureId, int index) { // 删除测量项
        MeasureManage.MeasureItem measureItem = MeasureManage.getInstance().getMeasureItem(); // 获取测量项管理器
        if(measureItem == null || channelId < 0 || measureId < 0) return; // 参数校验
//        List<MeasureManage.MeasureItemStruct> measureStructList = measureItem.getMeasureList();
//        for (int i= 0; i< measureStructList.size(); i++) {
//            MeasureManage.MeasureItemStruct measureItemStruct = measureStructList.get(i);
//            if(measureItemStruct.getChannelId() == channelId && measureItemStruct.getMeasureId() == measureId) {
//                measureItem.delMeasureItem(channelId, measureId);
//                break;
//            }
//        }
        measureItem.delMeasureItem(channelId, measureId); // 从管理器删除
        MeasureManage.MeasureItemStruct measureItemStruct = measureItem.getMeasureItemStruct(channelId, measureId); // 获取删除的测量项结构


//        int effectiveCount = 0;//有效测量项的个数
//        int newIndex = 0;//最终需要删除index
//        MeasureManage.MeasureItemStruct measureItemStruct = null;
//        for (int i = 0; i < measureStructList.size(); i++) {
//            measureItemStruct = measureItem.getMeasureList().get(i);
//            newIndex = i;
//            if (measureItemStruct.getChannelId() >= 0) {
//                effectiveCount++;
//                if (effectiveCount == index + 1) {
//                    break;
//                }
//            }
//        }
//        measureItem.delMeasureItem(newIndex);
        MeasureManage.MeasureIndication measureIndication = MeasureManage.getInstance().getMeasureIndication(); // 获取测量指示器
        if(measureIndication != null){ // 如果指示器有效
            measureIndication.setVisiable(false); // 隐藏指示器
            if(measureItemStruct != null && measureIndication.getChIdx() == (measureItemStruct.getChannelId() - 1)
                && measureIndication.getMeasureId() == (measureItemStruct.getMeasureId() + Measure.MeasureType.MEASURE_FIRST)){ // 如果匹配当前指示的测量项
                measureIndication.setMeasureIndication(-1,-1); // 重置指示器
            }
        }
        Command.get().getMeasure().Clear(index, false); // 发送清除命令
    }

    /**
     * 清空所有测量项
     */
    private void delAll() { // 清空所有测量项

        ChannelFactory.forEachCh(channel -> { // 遍历所有通道
            Measure measure = getHardwareMeasure(channel.getChId()); // 获取硬件测量对象
            if(measure != null){ // 如果测量对象有效
                measure.getMeasureStatics().clear(); // 清空统计数据
            }
        });
        MeasureManage.getInstance().getMeasureItem().delAllMeasureItem(); // 清空管理器中的所有测量项
        Command.get().getMeasure().ClearAllItem(false); // 发送清除所有命令
        MeasureManage.getInstance().getMeasureIndication().setVisiable(false); // 隐藏指示器
        MeasureManage.getInstance().getMeasureIndication().setMeasureIndication(-1,-1); // 重置指示器
    }


    /**
     * 测量数据更新UI观察者 - 接收硬件测量数据更新事件
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() { // 测量数据更新观察者
        @Override
        public void update(Object data) { // 接收更新事件
            EventBase eventBase = (EventBase) data; // 转换为事件基类
            int id = eventBase.getId(); // 获取事件ID

            if (id == EventFactory.EVENT_CH_MEASURE_UPDATE // 模拟通道测量更新
                    || id == EventFactory.EVENT_MATH_MEASURE_UPDATE // 数学通道测量更新
                    || id == EventFactory.EVENT_REF_MEASURE_UPDATE) { // 参考通道测量更新

                int chidx = -1; // 通道索引


                CursorManage cursorManage = CursorManage.getInstance(); // 获取光标管理器
                WorkModeManage workModeManage = WorkModeManage.getInstance(); // 获取工作模式管理器
                int X1 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X1); // 获取X1缓存
                int X2 = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_TVALUE_X2); // 获取X2缓存

//                Log.d(TAG,"X1:" + X1 + ",X2:" + X2);

                for (int i = 0; i < measureSelectList.size(); i++) {//更新SelectMeasure
                    MeasureBean bean = measureSelectList.get(i); // 获取已选测量项
                    chidx = bean.getChannel() - 1; // 计算通道索引
                    long curTimeX = 0; // 当前时间X坐标
                    double timeEvery = 0; // 每像素时间
//                    int chIdx = TChan.toFpgaChNo(chidx);
                    if (ChannelFactory.isDynamicCh(chidx) // 模拟通道
                            || ChannelFactory.isMathCh(chidx) // 数学通道
                            || ChannelFactory.isSerialCh(chidx)) { // 串行通道
                        if (ChannelFactory.isMath_FFT_Ch(chidx)) { // FFT数学通道
                            HorizontalAxisMath horizontalAxisMath = ChannelFactory.getMathChannel(chidx).getHorizontalAxisMathFFT(); // 获取FFT水平轴
                            curTimeX = ScopeBase.getWidth() / 2 - horizontalAxisMath.getXPosOfView(); // 计算当前时间X
                            timeEvery = horizontalAxisMath.fftXScaleIdVal() / ScopeBase.getHorizonPerGridPixels(); // 计算每像素时间
                        } else { // 非FFT通道
                            curTimeX = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix(); // 计算当前时间X
                            timeEvery = HorizontalAxis.getInstance().getTimesPrePix(); // 计算每像素时间
                        }
                    } else if (ChannelFactory.isRefCh(chidx)) { // 参考通道
                        RefChannel refChannel = ChannelFactory.getRefChannel(chidx); // 获取参考通道
                        curTimeX = ScopeBase.getWidth() / 2 - refChannel.getTimePoseOfViewPix(); // 计算当前时间X
                        timeEvery = refChannel.getRefTimePerPix(); // 计算每像素时间
                    }

                    Measure measure = getHardwareMeasure(chidx); // 获取硬件测量对象
                    int idx = bean.getIndex() + Measure.MeasureType.MEASURE_FIRST; // 计算完整测量项索引

                    if (ChannelFactory.isChOpen(chidx) && measure != null // 通道开启且测量对象有效
                            && measure.isMeasureItemValid(idx)) { // 测量项有效

                        if((ChannelFactory.isDynamicCh(chidx) && id != EventFactory.EVENT_CH_MEASURE_UPDATE) // 事件类型不匹配
                                || (ChannelFactory.isMathCh(chidx) && id != EventFactory.EVENT_MATH_MEASURE_UPDATE)
                                || (ChannelFactory.isRefCh(chidx) && id != EventFactory.EVENT_REF_MEASURE_UPDATE)
                        ){
                            continue; // 跳过不匹配的事件
                        }

                        float itemVal = measure.getMeasureItemVal(idx); // 获取测量值
                        if(idx == Measure.MeasureType.MEASURE_TVALUE){ // 如果是TValue
//                            if(!cursorManage.isCursorTrace()
//                                    && cursorManage.getColVisible()
//                                    && (workModeManage.getmWorkMode() == IWorkMode.WorkMode_YT
//                                    || workModeManage.getmWorkMode() == IWorkMode.WorkMode_YTZOOM
//                            )
//                                ){
//                                Log.d(TAG,"X1:" + X1 + ",chidx:" + chidx +",X2:" + X2 + ",itemVal:" + itemVal);
                                if(X1 == chidx){ // 如果X1追踪当前通道

                                    cursorManage.setCursor(TChan.Cursor_col_1, itemVal); // 设置X1光标位置
                                }
                                if(X2 == chidx){ // 如果X2追踪当前通道
                                    cursorManage.setCursor(TChan.Cursor_col_2, itemVal); // 设置X2光标位置
                                }
//                            }

                            itemVal = (float) ((itemVal - curTimeX) * timeEvery); // 转换为时间值

//                            Log.d(TAG,"---itemVal:" + itemVal);
                        }


                        Command.get().getMeasure().setListMeasureValue(bean.getChannel(), bean.getIndex(), itemVal, true); // 更新命令层测量值
                        updateMeasureItem(bean.getNo(),bean.getChannel(), bean.getIndex(), bean.getName(), // 更新UI测量项数据
                                clippingProcess(addUnit(chidx, bean.getIndex(), itemVal), // 添加单位并处理削波
                                        idx, measure.getClipping())); // 传入测量项ID和削波状态
                    } else { // 测量项无效
                        Command.get().getMeasure().setListMeasureValue(bean.getChannel(), bean.getIndex(), 0,false); // 标记无效
                        updateMeasureItem(bean.getNo(),bean.getChannel(), bean.getIndex(), bean.getName(), MEASURE_DATA_INIT); // 显示初始值
                    }
                }
                if(measureSelectList.size() > 0) { // 如果有已选测量项
                    updateMeasureIndication(); // 更新测量指示器
                }
//                Logger.i(TAG," hidemeasurebeanlist size:"+hideMeasureBeanList.size());


                for (int i = 0; i < hideMeasureBeanList.size(); i++) { // 遍历隐藏测量项列表
                    int ch = hideMeasureBeanList.get(i).getChannel(); // 获取通道
                    int idx = hideMeasureBeanList.get(i).getIndex() + Measure.MeasureType.MEASURE_PERIOD; // 获取测量项索引
                    Measure measure = getHardwareMeasure(ch); // 获取硬件测量对象
//                    Logger.i(TAG,"ch:"+ch+",measure:"+(measure!=null?"true":"false")+",valid:"+measure.isMeasureItemValid(idx));
                    if (ChannelFactory.isChOpen(ch) && measure != null // 通道开启且测量对象有效
                            && measure.isMeasureItemValid(idx)) { // 测量项有效
                        if((ChannelFactory.isDynamicCh(chidx) && id != EventFactory.EVENT_CH_MEASURE_UPDATE) // 事件类型不匹配
                                || (ChannelFactory.isMathCh(chidx) && id != EventFactory.EVENT_MATH_MEASURE_UPDATE)
                                || (ChannelFactory.isRefCh(chidx) && id != EventFactory.EVENT_REF_MEASURE_UPDATE)
                        ){
                            continue; // 跳过不匹配的事件
                        }
                        if(measure.isMeasureItemValid(idx)){ // 再次确认有效
                            float itemVal = measure.getMeasureItemVal(idx); // 获取测量值

                            Command.get().getMeasure().setListMeasureValueEx(ch, idx - Measure.MeasureType.MEASURE_PERIOD, itemVal,true); // 更新扩展测量值
                        }else{ // 无效
                            Command.get().getMeasure().setListMeasureValueEx(ch, idx - Measure.MeasureType.MEASURE_PERIOD, 0,false); // 标记无效
                        }
                    }
                }
                setAllMeasureData(); // 更新AllMeasure数据
            }
        }
    };

    /**
     * 更新AllMeasure显示数据
     */
    private void setAllMeasureData(){ // 设置AllMeasure数据
        if (MeasureManage.getInstance().getAllMeasure().isVisible()) {//更新AllMeasure
            List<String> values = new ArrayList<>(); // 测量值列表
            int ch = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT); // 获取当前选中通道
            Measure measure = getHardwareMeasure(ch); // 获取硬件测量对象
            for (MeasureBean bean : measureBeanList) { // 遍历所有测量项
                if (bean.getIndex() == MEASURE_INDEX_DELAY // 跳过延时
                        || bean.getIndex() == MEASURE_INDEX_PHASE // 跳过相位
                        || bean.getIndex() == MEASURE_INDEX_TVALUE) {//跳过TValue，延时、相位没有AllMeasure
                    continue; // 跳过
                }
                if (measure != null && measure.isMeasureItemValid(bean.getIndex() + Measure.MeasureType.MEASURE_PERIOD)) { // 测量项有效
                    float itemVal = measure.getMeasureItemVal(bean.getIndex() + Measure.MeasureType.MEASURE_PERIOD); // 获取测量值
                    values.add(clippingProcess(addUnit(ch, bean.getIndex(), itemVal), // 添加单位并处理削波
                            bean.getIndex() + Measure.MeasureType.MEASURE_PERIOD,
                            measure.getClipping())); // 传入削波状态
                } else { // 无效
                    values.add(MEASURE_DATA_INIT); // 显示初始值
                }
            }
            MeasureManage.getInstance().getAllMeasure().setMeasureData(values); // 设置AllMeasure数据
        }
    }

    /**
     * 为测量值添加单位
     * @param ch 通道索引
     * @param measureType 测量类型
     * @param val 测量值
     * @return 带单位的测量值字符串
     */
    private String addUnit(int ch, int measureType, float val) { // 添加单位
        switch (measureType) { // 根据测量类型添加单位
            case MeasureManage.IMeasure.MeasureId_Freq: // 频率
                return TBookUtil.getFourFromD_(val) + "Hz"; // Hz单位
            case MeasureManage.IMeasure.MeasureId_DutyAdd: // 正占空比
            case MeasureManage.IMeasure.MeasureId_DutySub: // 负占空比
            case MeasureManage.IMeasure.MeasureId_ROV: // 上升超调
            case MeasureManage.IMeasure.MeasureId_FOV: // 下降超调
                //%不显示m、k、M等前缀，保留2位小数
                return TBookUtil.getPoint2FromD_noscale(val * 100) + "%"; // 百分比单位
            case MeasureManage.IMeasure.MeasureId_Phase: // 相位
                String d = TBookUtil.getFourFromD_(val); // 格式化数值
                if ("-0 f".equals(d) || "0 f".equals(d)) { // 处理-0情况
                    d = "0"; // 重置为0
                }
                return d + "°"; // 度单位
            case MeasureManage.IMeasure.MeasureId_Period: // 周期
            case MeasureManage.IMeasure.MeasureId_RiseTime: // 上升时间
            case MeasureManage.IMeasure.MeasureId_FallTime: // 下降时间
            case MeasureManage.IMeasure.MeasureId_Delay: // 延时
            case MeasureManage.IMeasure.MeasureId_WidthAdd: // 正脉宽
            case MeasureManage.IMeasure.MeasureId_WidthSub: // 负脉宽
            case MeasureManage.IMeasure.MeasureId_BurstW: // 突发宽度
            case MeasureManage.IMeasure.MeasureId_TVALUE: // TValue
                return TBookUtil.getFourFromD_(val) + "s"; // 秒单位
            case MeasureManage.IMeasure.MeasureId_PKPK: // 峰峰值
            case MeasureManage.IMeasure.MeasureId_Amp: // 幅值
            case MeasureManage.IMeasure.MeasureId_High: // 高电平
            case MeasureManage.IMeasure.MeasureId_Low: // 低电平
            case MeasureManage.IMeasure.MeasureId_Max: // 最大值
            case MeasureManage.IMeasure.MeasureId_Min: // 最小值
            case MeasureManage.IMeasure.MeasureId_RMS: // RMS
            case MeasureManage.IMeasure.MeasureId_CRMS: // CRMS
            case MeasureManage.IMeasure.MeasureId_Mean: // 平均值
            case MeasureManage.IMeasure.MeasureId_CMean: // CMean
            case MeasureManage.IMeasure.MeasureId_ACRMS: // ACRMS
                return TBookUtil.getFourFromD_(val) + ChannelFactory.getProbeType(ch); // 电压单位（含探头比例）
            case MeasureManage.IMeasure.MeasureId_PostitiveRate: // 上升速率
            case MeasureManage.IMeasure.MeasureId_NegativeRate: // 下降速率
                return TBookUtil.getFourFromD_(val) + ChannelFactory.getProbeType(ch) + "/s"; // 电压/秒单位
        }
        return ""; // 未知类型返回空字符串
    }

    /**
     * 削波处理 - 根据削波状态为测量值添加问号标记
     * @param sData 测量值字符串
     * @param itemId 测量项完整ID
     * @param clipping 削波状态（0=正常，1=正削，2=负削，3=双削，4=无波）
     * @return 处理后的字符串
     */
    private String clippingProcess(String sData, int itemId, int clipping) { // 削波处理
        switch (clipping) { // 根据削波状态处理
            case 1://正削
                switch (itemId) { // 正削时某些项不标记
                    case Measure.MeasureType.MEASURE_NEGATIVE_OVERSHOOT: // 负超调不受正削影响
                    case Measure.MeasureType.MEASURE_LOW: // 低电平不受正削影响
                    case Measure.MeasureType.MEASURE_MIN: // 最小值不受正削影响
                        break; // 不添加标记
                    default: // 其他项
                        if (!sData.equals(MEASURE_DATA_INIT)) sData += "?"; // 添加问号标记
                        break; // 跳出
                }
                break; // 跳出
            case 2://负削
                switch (itemId) { // 负削时某些项不标记
                    case Measure.MeasureType.MEASURE_POSITIVE_OVERSHOOT: // 正超调不受负削影响
                    case Measure.MeasureType.MEASURE_HIGH: // 高电平不受负削影响
                    case Measure.MeasureType.MEASURE_MAX: // 最大值不受负削影响
                        break; // 不添加标记
                    default: // 其他项
                        if (!sData.equals(MEASURE_DATA_INIT)) sData += "?"; // 添加问号标记
                        break; // 跳出
                }
                break; // 跳出
            case 3://双削
                if (!sData.equals(MEASURE_DATA_INIT)) sData += "?"; // 所有项都标记
                break; // 跳出
            case 4://无波
                if (!sData.equals(MEASURE_DATA_INIT)) sData += "?"; // 所有项都标记
                break; // 跳出
            case 0://正常
            default: // 默认
                break; // 不处理
        }
        return sData; // 返回处理后的字符串
    }

    /**
     * 通道颜色选择消费者 - 处理通道颜色变更
     */
    private Consumer<String> consumerSelectColor = new Consumer<String>() { // 通道颜色选择消费者
        @Override
        public void accept(String colorInfo) throws Throwable { // 接收颜色变更消息
            if (colorInfo.isEmpty()) return; // 空消息返回
            Logger.i(TAG, "selectColorInfo= " + colorInfo); // 打印颜色信息日志
            String[] info = colorInfo.split(";"); // 分割颜色信息
            int chIndex = Integer.parseInt(info[0]); // 解析通道索引
            String colorStr = info[1]; // 获取颜色字符串
            viewChannel.setChannelColor(chIndex, colorStr); // 设置通道颜色
            measureAdapter.updateColors(); // 更新待选列表颜色
            measureSelectAdapter.updateColors(); // 更新已选列表颜色
        }
    };

}