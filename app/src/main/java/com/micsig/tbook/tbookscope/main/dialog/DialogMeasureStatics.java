package com.micsig.tbook.tbookscope.main.dialog;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.channel.BaseChannel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.measure.Measure;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.util.TimerUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.functions.Consumer;

/*
 * +===========================================================================+
 * |                        DialogMeasureStatics                               |
 * |                         测量统计对话框组件                                 |
 * +===========================================================================+
 * | 模块定位: 示波器主界面测量统计信息显示组件                                  |
 * | 核心职责: 实时显示测量项的统计值（当前值、平均值、最大值、最小值等）          |
 * | 架构设计: 继承ConstraintLayout，采用定时器刷新机制，响应RxJava事件          |
 * | 数据流向: MeasureManage -> DialogMeasureStatics -> DialogMeasureStaticsItem|
 * | 依赖关系: Context, MeasureManage, RxBus, TimerUtils, WorkModeManage        |
 * | 使用场景: 波形测量模式下显示各通道测量参数的统计数据                         |
 * +===========================================================================+
 */
public class DialogMeasureStatics extends ConstraintLayout {
    private Context context;  // 应用上下文
    private TimerUtils timer;  // 定时刷新器

    /**
     * 单参数构造函数
     * @param context 应用上下文
     */
    public DialogMeasureStatics(@NonNull Context context) {
        this(context, null);  // 调用双参数构造函数
    }

    /**
     * 双参数构造函数
     * @param context 应用上下文
     * @param attrs 属性集
     */
    public DialogMeasureStatics(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);  // 调用三参数构造函数
    }

    /**
     * 完整构造函数
     * 初始化视图、定时器和控制逻辑
     * @param context 应用上下文
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogMeasureStatics(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造函数
        this.context = context;  // 保存上下文引用
        initView();  // 初始化视图组件
        timer = new TimerUtils(new TimeEvent());  // 创建定时器实例
        timer.setExecOne(false);  // 设置定时器重复执行
        timer.setIntervalMs(100);  // 设置刷新间隔为100毫秒

        initControl();  // 初始化事件控制
    }

    private View mainView;  // 主视图引用
    private List<DialogMeasureStaticsItem> listTxtView = new ArrayList<>();  // 测量项视图列表
    private LinearLayout listView;  // 列表容器
    private DialogMeasureStaticsItem title;  // 标题项
    private TextView line1,line2,line3,line4,line5,line6,line_title;  // 分隔线视图
    private List<TextView> lineList=new ArrayList<>();  // 分隔线列表

    /**
     * 初始化视图组件
     * 加载布局文件并初始化各子视图
     */
    private void initView() {
        View inflate = inflate(context, R.layout.dialog_measure_statics, this);  // 加载布局文件
        mainView = inflate;  // 保存主视图引用
        listView = inflate.findViewById(R.id.listView);  // 获取列表容器
        title = inflate.findViewById(R.id.title);  // 获取标题视图

        // 遍历创建测量项视图
        for (int i = 0; i < GlobalVar.get().getMeasureItemCount(); i++) {  // 循环创建测量项
            DialogMeasureStaticsItem item = new DialogMeasureStaticsItem(context);  // 创建测量项实例
            item.OnTxtClickEvent = this::OnClickListener;  // 设置点击事件回调
            listView.addView(item);  // 添加到列表容器
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) item.getLayoutParams();  // 获取布局参数
            params.width = 164;  // 设置固定宽度
            item.setLayoutParams(params);  // 应用布局参数
            listTxtView.add(item);  // 添加到视图列表
        }

        title.setTitle();  // 设置标题文本
        title.setTitleHide();  // 隐藏标题

        // 初始化分隔线视图引用
        line1=inflate.findViewById(R.id.row1);  // 获取第1行分隔线
        line2=inflate.findViewById(R.id.row2);  // 获取第2行分隔线
        line3=inflate.findViewById(R.id.row3);  // 获取第3行分隔线
        line4=inflate.findViewById(R.id.row4);  // 获取第4行分隔线
        line5=inflate.findViewById(R.id.row5);  // 获取第5行分隔线
        line6=inflate.findViewById(R.id.row6);  // 获取第6行分隔线
        line_title=inflate.findViewById(R.id.line_title);  // 获取标题分隔线
        // 将分隔线添加到列表
        lineList.add(line1);  // 添加第1行
        lineList.add(line2);  // 添加第2行
        lineList.add(line3);  // 添加第3行
        lineList.add(line4);  // 添加第4行
        lineList.add(line5);  // 添加第5行
        lineList.add(line6);  // 添加第6行

    }

    /**
     * 测量项点击事件处理
     * @param view 被点击的视图
     */
    private void OnClickListener(View view) {

        if (isMeasureItemClickEnable() == false) return;  // 检查点击是否可用，不可用则直接返回
        int idx = Tools.indexOf(listTxtView, (v) -> v.equals(view));  // 查找点击项的索引
        if (idx < 0) return;  // 索引无效则返回
        MeasureManage.getInstance().getMeasureItem().setSelectItem(idx);  // 设置选中项

    }

    /**
     * 检查测量项点击是否可用
     * @return true表示可用，false表示不可用
     */
    private boolean isMeasureItemClickEnable() {
        return CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_SETTING_INDICATOR);  // 从缓存读取配置
    }

    /**
     * 初始化事件控制
     * 订阅工作模式变化和串口显示事件
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange);  // 订阅工作模式变化事件
        RxBus.getInstance().getObservable(RxEnum.CENTER_SERIALSWORD_VISIBLE).subscribe(consumerSerialswordVisible);  // 订阅串口显示事件
        EventFactory.addEventObserver(EventFactory.EVENT_SELF_CALIBRATE_BEGIN, eventUIObserver);  // 添加自校准开始事件监听
        EventFactory.addEventObserver(EventFactory.EVENT_SELF_CALIBRATE_END, eventUIObserver);  // 添加自校准结束事件监听
    }

    /**
     * 定时器事件处理器
     * 定时触发UI刷新
     */
    class TimeEvent implements TimerUtils.TimeOutEvent {
        @Override
        public void onTimeOut() {
            ((MainActivity) (DialogMeasureStatics.this.context)).runOnUiThread(() -> {  // 在主线程执行
                startRefresh();  // 开始刷新视图
            });
        }
    }

    /**
     * 工作模式变化消费者
     * 根据工作模式切换对话框显示状态
     */
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            if (workModeBean.getNextWorkMode() == IWorkMode.WorkMode_XY) {  // XY模式下隐藏
//                setVisibility(INVISIBLE);
                setShowDialog(false);  // 隐藏对话框
            } else {  // 非XY模式
                boolean isShowDialog = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);  // 读取显示配置
                if (isShowDialog) {
//                    setVisibility(VISIBLE);
                    setShowDialog(true);  // 显示对话框
                } else {
//                    setVisibility(INVISIBLE);
                    setShowDialog(false);  // 隐藏对话框
                }
            }
        }
    };

    /**
     * 串口显示消费者
     * 根据串口显示状态切换对话框显示
     */
    private Consumer<Boolean> consumerSerialswordVisible = new Consumer<Boolean>() {
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            if (aBoolean) {  // 串口显示时隐藏
//                setVisibility(INVISIBLE);
                setShowDialog(false);  // 隐藏对话框
            } else {  // 串口隐藏时
                boolean isShowDialog = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);  // 读取显示配置
                if (isShowDialog) {
//                    setVisibility(VISIBLE);
                    setShowDialog(true);  // 显示对话框
                } else {
//                    setVisibility(INVISIBLE);
                    setShowDialog(false);  // 隐藏对话框
                }
            }
        }
    };

    /**
     * 自校准事件观察者
     * 在自校准期间隐藏对话框
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;  // 转换事件数据
            if (eventBase.getId() == EventFactory.EVENT_SELF_CALIBRATE_BEGIN) {  // 自校准开始
//                setVisibility(INVISIBLE);
                setShowDialog(false);  // 隐藏对话框
            } else if (eventBase.getId() == EventFactory.EVENT_SELF_CALIBRATE_END) {  // 自校准结束
                boolean isShowDialog = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL);  // 读取显示配置
                if (isShowDialog) {
//                    setVisibility(VISIBLE);
                    setShowDialog(true);  // 显示对话框
                } else {
//                    setVisibility(INVISIBLE);
                    setShowDialog(false);  // 隐藏对话框
                }
            }
        }
    };


    /**
     * 获取硬件测量对象
     * @param chId 通道ID
     * @return Measure对象，找不到则返回null
     */
    private Measure getHardwareMeasure(int chId) {
        BaseChannel baseChannel = null;  // 基础通道引用
        if (ChannelFactory.isDynamicCh(chId)) {  // 判断是否为动态通道
            baseChannel = ChannelFactory.getDynamicChannel(chId);  // 获取动态通道
        } else if (ChannelFactory.isMathCh(chId)) {  // 判断是否为数学运算通道
            baseChannel = ChannelFactory.getMathChannel(chId);  // 获取数学通道
        } else if (ChannelFactory.isRefCh(chId)) {  // 判断是否为参考通道
            baseChannel = ChannelFactory.getRefChannel(chId);  // 获取参考通道
        }
        if (baseChannel != null) {  // 通道有效
            return baseChannel.getMeasure();  // 返回测量对象
        }
        return null;  // 返回空
    }

    /**
     * 开启定时刷新
     */
    public void open() {
        timer.start();  // 启动定时器
    }

    /**
     * 关闭定时刷新
     */
    public void close() {
        timer.stop();  // 停止定时器
    }

    /**
     * 清空所有文本内容
     */
    private void clearTextContent() {
        for (int i = 0; i < listTxtView.size(); i++) {  // 遍历所有测量项
            listTxtView.get(i).clearTxt();  // 清空文本
        }
    }

    /**
     * 为测量值添加单位
     * @param ch 通道号
     * @param measureType 测量类型
     * @param val 测量值
     * @return 带单位的字符串
     */
    private String addUnit(int ch, int measureType, float val) {
        switch (measureType) {  // 根据测量类型添加单位
            case MeasureManage.IMeasure.MeasureId_Freq:  // 频率
                return TBookUtil.getFourFromD_(val) + "Hz";  // 返回带Hz单位的值
            case MeasureManage.IMeasure.MeasureId_DutyAdd:  // 占空比（正脉宽）
            case MeasureManage.IMeasure.MeasureId_DutySub:  // 占空比（负脉宽）
            case MeasureManage.IMeasure.MeasureId_ROV:  // 上升过冲
            case MeasureManage.IMeasure.MeasureId_FOV:  // 下降过冲
                //%不显示m、k、M等前缀，保留2位小数
                return TBookUtil.getPoint2FromD_noscale(val * 100) + "%";  // 返回带%单位的值
            case MeasureManage.IMeasure.MeasureId_Phase:  // 相位
                String d = TBookUtil.getFourFromD_(val);  // 格式化数值
                if ("-0f".equals(d) || "0f".equals(d)) {  // 处理特殊值
                    d = "0";  // 重置为0
                }
                return d + "°";  // 返回带度单位的值
            case MeasureManage.IMeasure.MeasureId_Period:  // 周期
            case MeasureManage.IMeasure.MeasureId_RiseTime:  // 上升时间
            case MeasureManage.IMeasure.MeasureId_FallTime:  // 下降时间
            case MeasureManage.IMeasure.MeasureId_Delay:  // 延迟
            case MeasureManage.IMeasure.MeasureId_WidthAdd:  // 正脉宽
            case MeasureManage.IMeasure.MeasureId_WidthSub:  // 负脉宽
            case MeasureManage.IMeasure.MeasureId_BurstW:  // 突发脉宽
            case MeasureManage.IMeasure.MeasureId_TVALUE:  // T值
                return TBookUtil.getFourFromD_(val) + "s";  // 返回带秒单位的值
            case MeasureManage.IMeasure.MeasureId_PKPK:  // 峰峰值
            case MeasureManage.IMeasure.MeasureId_Amp:  // 幅值
            case MeasureManage.IMeasure.MeasureId_High:  // 高电平
            case MeasureManage.IMeasure.MeasureId_Low:  // 低电平
            case MeasureManage.IMeasure.MeasureId_Max:  // 最大值
            case MeasureManage.IMeasure.MeasureId_Min:  // 最小值
            case MeasureManage.IMeasure.MeasureId_RMS:  // 有效值
            case MeasureManage.IMeasure.MeasureId_CRMS:  // 交流有效值
            case MeasureManage.IMeasure.MeasureId_Mean:  // 平均值
            case MeasureManage.IMeasure.MeasureId_CMean:  // 交流平均值
            case MeasureManage.IMeasure.MeasureId_ACRMS:  // 交流+直流有效值
                return TBookUtil.getFourFromD_(val) + ChannelFactory.getProbeType(ch);  // 返回带探头类型单位的值
            case MeasureManage.IMeasure.MeasureId_PostitiveRate:  // 正跳变率
            case MeasureManage.IMeasure.MeasureId_NegativeRate:  // 负跳变率
                return TBookUtil.getFourFromD_(val) + ChannelFactory.getProbeType(ch) + "/s";  // 返回带单位/秒的值
        }
        return "";  // 默认返回空字符串
    }

    /**
     * 开始刷新数据
     */
    private void startRefresh() {
        updateView();  // 更新视图
    }

    /**
     * 更新视图显示
     * 刷新测量项数据
     */
    private void updateView() {
        List<MeasureManage.MeasureItemStruct> list = MeasureManage.getInstance().getMeasureItem().getValidMeasureList();  // 获取有效测量项列表
        title.UpdateParamVisible();  // 更新标题参数可见性
        for (int i = 0; i < listTxtView.size(); i++) {  // 遍历所有测量项视图
            DialogMeasureStaticsItem txt = listTxtView.get(i);  // 获取当前项
            if (i >= list.size()) {  // 超出有效项数量
                txt.clearTxt();  // 清空文本
                txt.setBackground(null);  // 清除背景
                txt.setVisibility(GONE);  // 隐藏视图
                continue;  // 继续下一项
            }
            txt.setVisibility(VISIBLE);  // 显示视图
            txt.UpdateParamVisible();  // 更新参数可见性
            MeasureManage.MeasureItemStruct bean = list.get(i);  // 获取测量项数据
            txt.UpdateView(bean,isMeasureItemClickEnable());  // 更新视图内容
        }


        if (list.size()>0){  // 有测量项时显示标题线
            line_title.setVisibility(VISIBLE);  // 显示标题线
        }else {  // 无测量项时隐藏标题线
            line_title.setVisibility(GONE);  // 隐藏标题线
        }
    }

    /**
     * 显示测量统计对话框
     * @param isShowMean 是否显示平均值
     * @param isShowMax 是否显示最大值
     * @param isShowMin 是否显示最小值
     * @param isShowDelta 是否显示差值
     * @param isShowCount 是否显示计数
     * @param measureCount 测量项数量
     * @param height 高度
     */
    public void show(boolean isShowMean, boolean isShowMax, boolean isShowMin, boolean isShowDelta, boolean isShowCount, int measureCount, int height) {
        updateView();  // 更新视图
        int sum = 1;  // 计算显示行数，基础1行
        if (isShowMean) sum++;  // 显示平均值则加1
        if (isShowMax) sum++;  // 显示最大值则加1
        if (isShowMin) sum++;  // 显示最小值则加1
        if (isShowDelta) sum++;  // 显示差值则加1
        if (isShowCount) sum++;  // 显示计数则加1

        // 设置分隔线可见性
        for(int i=0;i<lineList.size();i++){  // 遍历分隔线列表
            if (sum>i){  // 在显示范围内
                lineList.get(i).setVisibility(VISIBLE);  // 显示分隔线
                int color=i%2==0? Color.TRANSPARENT:Color.rgb(54,59,64);  // 交替设置背景色
                lineList.get(i).setBackgroundColor(color);  // 应用背景色
            }else {  // 超出显示范围
                lineList.get(i).setVisibility(GONE);  // 隐藏分隔线
            }

        }

        close();  // 关闭定时刷新
        open();  // 开启定时刷新
        //Log.d("Tag.Debug", String.format("DialogMeasureStatics.show: %d",getHeight() ));

        // 40是标题高
        int titleHeight = measureCount == 0 ? 2 : 40;  // 计算标题高度
        int offSet = 1;//位置变化的补偿值
//        if (height == 1040) {
//            offSet = 1;
//        } else if (height == 1000) {
//            offSet = 2;
//        }
        this.setY(1040 - titleHeight - sum * 34 - offSet);  // 设置Y坐标位置
        setShowDialog(true);  // 显示对话框
    }

    /**
     * 隐藏测量统计对话框
     */
    public void hide() {
        close();  // 关闭定时刷新
        setShowDialog(false);  // 隐藏对话框
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_MEASURE_STATICS);  // 发送对话框关闭事件
    }

    /**
     * 设置对话框显示状态
     * @param b true表示显示，false表示隐藏
     */
    private void setShowDialog(boolean b){
        boolean visible = WorkModeManage.getInstance().isXyMode();  // 检查是否为XY模式
        boolean isSerialsTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);  // 检查串口文本显示状态
        boolean result= b && (visible==false && isSerialsTxt==false);  // 计算最终显示结果
        setVisibility(result?View.VISIBLE:View.GONE);  // 设置可见性
        if (result){  // 显示时发送事件
            RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_MEASURE_STATICS);  // 发送对话框打开事件
        }
    }

}