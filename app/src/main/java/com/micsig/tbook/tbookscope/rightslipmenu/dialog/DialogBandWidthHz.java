package com.micsig.tbook.tbookscope.rightslipmenu.dialog;


import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.ui.top.view.frequency.TopUtilBandWidthHz;
import com.micsig.tbook.ui.top.view.frequency.TopViewBandWidthHzLarge;
import com.micsig.tbook.ui.top.view.frequency.TopViewBandWidthHzSmall;
import com.micsig.tbook.ui.util.TBookUtil;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * @auother Liwb
 * @description:
 * @data:2022-3-8 10:07
 */

/*
 * +----------------------------------------------------------------------+
 * |                       DialogBandWidthHz                              |
 * |                      带宽频率设置对话框                                |
 * +----------------------------------------------------------------------+
 * | 模块定位: 右侧滑出菜单 -> 对话框子包                                    |
 * | 核心职责: 提供示波器带宽频率的滚轮式交互设置界面，                          |
 * |          支持大/小刻度滚轮联动调节，自动换算Hz/s/us/ns单位                 |
 * | 架构设计: 继承AbsoluteLayout的自定义对话框视图，                          |
 * |          内嵌TopViewBandWidthHzLarge/Small两个滚轮控件                   |
 * |          实现粗调+微调的双层刻度交互                                     |
 * | 数据流向: 外部调用setValue()传入Hz值 -> 用户滚轮调节 ->                   |
 * |          OnRulerChangedListener实时回调 ->                              |
 * |          OnDismissListener关闭时返回最终值                               |
 * | 依赖关系: RxBus(对话框开关/刻度变化事件), TopUtilBandWidthHz(频率换算),   |
 * |          TopViewBandWidthHzLarge/Small(滚轮控件), TBookUtil(格式化)     |
 * | 使用场景: 水平时基/采样率等频率参数设置菜单项弹出                         |
 * +----------------------------------------------------------------------+
 */
public class DialogBandWidthHz extends AbsoluteLayout {
    private Context context;  // 上下文引用
    private TextView head, show;  // head:对话框标题, show:当前频率值显示
    private TopViewBandWidthHzLarge topViewBandWidthHzLarge;  // 大刻度滚轮控件（粗调）
    private TopViewBandWidthHzSmall TopViewBandWidthHzSmall;  // 小刻度滚轮控件（微调）
    private OnDismissListener onDismissListener;  // 对话框关闭回调监听器

    private ViewGroup rootViewGroup;  // 根视图组

    private long timeMin = TopUtilBandWidthHz.DEFAULT_MIN_TIME;  // 时间最小值（ns单位）
    private long timeMax = TopUtilBandWidthHz.DEFAULT_MAX_TIME;  // 时间最大值（ns单位）

    /**
     * 对话框关闭监听接口。
     * <p>
     * 当用户点击对话框外部区域关闭时，回调onDismiss返回最终的频率字符串。
     */
    public interface OnDismissListener {
        /** 对话框关闭时回调，返回最终频率字符串 */
        void onDismiss(String result);
    }

    /**
     * 单参数构造方法，委托给两参数构造。
     *
     * @param context 上下文
     */
    public DialogBandWidthHz(Context context) {
        this(context, null);  // 委托给两参数构造
    }

    /**
     * 两参数构造方法，委托给三参数构造。
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public DialogBandWidthHz(Context context, AttributeSet attrs) {
        this(context, attrs, 0);  // 委托给三参数构造
    }

    /**
     * 三参数构造方法，初始化上下文并调用initView()加载布局。
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogBandWidthHz(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造
        this.context = context;               // 保存上下文引用
        initView();                           // 执行视图初始化
    }

    //[50, 342]	608	170

    /**
     * 初始化对话框布局和交互逻辑。
     * <p>
     * 加载dialog_bandwidthhz布局，设置外部区域点击关闭监听，
     * 初始化大/小刻度滚轮控件及其变化监听，订阅RxBus刻度变化事件。
     */
    private void initView() {
        setClickable(true);  // 设置可点击，拦截触摸事件
        rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_bandwidthhz, this);  // 填充布局

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {  // 设置外部区域触摸监听
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                listener();  // 执行关闭前回调
                hide();      // 隐藏对话框
                return false;  // 不消费触摸事件
            }
        });
        head = (TextView) rootViewGroup.findViewById(R.id.head);  // 对话框标题文本
        show = (TextView) rootViewGroup.findViewById(R.id.show);  // 频率值显示文本
        topViewBandWidthHzLarge = (TopViewBandWidthHzLarge) rootViewGroup.findViewById(R.id.scaleLargeView);  // 大刻度滚轮
        topViewBandWidthHzLarge.setOnRulerChangedListener(onLargeRulerChangedListener);  // 大刻度变化监听
        TopViewBandWidthHzSmall = (TopViewBandWidthHzSmall) rootViewGroup.findViewById(R.id.scaleSmallView);  // 小刻度滚轮
        TopViewBandWidthHzSmall.setOnRulerChangedListener(onSmallRulerChangedListener);  // 小刻度变化监听

        hide();  // 默认隐藏对话框
        RxBus.getInstance().getObservable(RxEnum.DIALOG_SCALE_CHANGED).subscribe(consumerScaleChanged);  // 订阅刻度变化事件
    }

    /**
     * 执行关闭回调，将当前显示的频率值转换后通知监听器。
     * <p>
     * 读取显示文本中的频率值，进行ns级精度换算后回调onDismiss。
     */
    public void listener() {
        if (onDismissListener != null) {  // 如果设置了关闭监听器
            long ns = TBookUtil.getPsFromTime(TopUtilBandWidthHz.getSFromHz(show.getText().toString())) / 1000;  // 将Hz转为ns（皮秒/1000）
            onDismissListener.onDismiss(TopUtilBandWidthHz.getHzFromS(TBookUtil.getTime3FromPs(ns * 1000 * 10)));  // 换算后回调频率字符串
        }
    }

    /**
     * 显示对话框，并通过RxBus通知对话框打开事件。
     */
    public void show() {
        setVisibility(VISIBLE);  // 设置视图可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_BANDWIDTHHZ);  // 发送对话框打开事件
        Tools.PrintControlsLocation("DialogBandWidthHz",rootViewGroup);  // 打印控件位置调试信息
    }

    /**
     * 隐藏对话框，并通过RxBus通知对话框关闭事件。
     */
    public void hide() {
        setVisibility(GONE);  // 设置视图不可见且不占位
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_BANDWIDTHHZ);  // 发送对话框关闭事件
    }

    /**
     * 设置对话框数据并显示（Hz值+范围）。
     * <p>
     * 将Hz值转换为ns时间，与允许范围取交集后设置到滚轮控件。
     *
     * @param headString       对话框标题
     * @param hzValue          当前Hz频率值字符串
     * @param hzMin            允许的最小Hz值
     * @param hzMax            允许的最大Hz值
     * @param onDismissListener 关闭回调监听器
     */
    public void setValue(String headString, String hzValue, long hzMin, long hzMax, OnDismissListener onDismissListener) {
        long timeMin = hzMin * 10;  // 计算时间最小值（ns，乘10精度对齐）
        long timeMax = hzMax * 10;  // 计算时间最大值（ns，乘10精度对齐）
//        this.timeMin = Math.min(timeMin, TopUtilBandWidthHz.TIME_US2NS);
//        this.timeMax = Math.max(timeMax, TopUtilBandWidthHz.TIME_S2NS);
        this.timeMin = timeMin;  // 设置时间最小值
        this.timeMax = timeMax;  // 设置时间最大值
        long ns = TopUtilBandWidthHz.getNSFromValue(TopUtilBandWidthHz.getSFromHz(hzValue));  // 将Hz值转换为ns
        ns = ns < timeMin ? timeMin : ns;  // 不低于最小值
        ns = ns > timeMax ? timeMax : ns;  // 不超过最大值
        head.setText(headString);  // 设置标题文本
        setValue(ns, onDismissListener);  // 调用内部setValue设置值
    }

    /**
     * 内部方法：根据ns值设置滚轮控件并显示对话框。
     *
     * @param ns                时间值，单位ns
     *                          1us = 1000ns
     *                          1ms = 1000 * 1000ns
     *                          1s  = 1000 * 1000 * 1000ns
     * @param onDismissListener 关闭回调监听器
     */
    private void setValue(long ns, OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;  // 保存关闭监听器
        TopUtilBandWidthHz.ScaleValue scaleValue = new TopUtilBandWidthHz().createScaleValue();  // 创建刻度值对象
        TopUtilBandWidthHz.getValueFromNS(ns, scaleValue);  // 从ns值解析出刻度值（数值+单位+步进）
        show.setText(TopUtilBandWidthHz.getHzFromS(TBookUtil.getD3FromD(scaleValue.value) + scaleValue.itemUnit));  // 设置显示文本（Hz格式）
        topViewBandWidthHzLarge.setTimeRange(timeMin, timeMax);  // 设置大刻度滚轮范围
        TopViewBandWidthHzSmall.setTimeRange(timeMin, timeMax);  // 设置小刻度滚轮范围
        topViewBandWidthHzLarge.setValue(scaleValue.value, scaleValue.itemUnit, scaleValue.itemValue);  // 设置大刻度值
        TopViewBandWidthHzSmall.setValue(scaleValue.value, scaleValue.itemUnit, scaleValue.itemValue / 100);  // 设置小刻度值（步进为大刻度1/100）
        show();  // 显示对话框
    }

    /**
     * RxBus刻度变化事件消费者。
     * <p>
     * 处理物理旋钮/按键引起的刻度变化：
     * - ACTION_SCALE_FINISH: 确认选择，关闭对话框
     * - ACTION_SCALE_LARGE_LEFT/RIGHT: 大刻度左/右移动一步
     * - ACTION_SCALE_SMALL_LEFT/RIGHT: 小刻度左/右移动一步
     */
    private Consumer<Integer> consumerScaleChanged = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Exception {
            switch (integer) {  // 根据事件类型分发
                case TopUtilBandWidthHz.ACTION_SCALE_FINISH:  // 确认选择
                    listener();  // 执行关闭回调
                    hide();      // 隐藏对话框
                    break;
                case TopUtilBandWidthHz.ACTION_SCALE_LARGE_LEFT:  // 大刻度左移
                    topViewBandWidthHzLarge.moveLeftOneStep();  // 大刻度左移一步
                    break;
                case TopUtilBandWidthHz.ACTION_SCALE_LARGE_RIGHT:  // 大刻度右移
                    topViewBandWidthHzLarge.moveRightOneStep();  // 大刻度右移一步
                    break;
                case TopUtilBandWidthHz.ACTION_SCALE_SMALL_LEFT:  // 小刻度左移
                    TopViewBandWidthHzSmall.moveLeftOneStep();  // 小刻度左移一步
                    break;
                case TopUtilBandWidthHz.ACTION_SCALE_SMALL_RIGHT:  // 小刻度右移
                    TopViewBandWidthHzSmall.moveRightOneStep();  // 小刻度右移一步
                    break;
            }
        }
    };

    /**
     * 大刻度滚轮变化监听器。
     * <p>
     * 当大刻度值变化时：
     * 1. 根据当前单位和时间范围限制值（不低于timeMin，不超过timeMax）
     * 2. 更新显示文本
     * 3. 同步更新小刻度滚轮的值
     * 4. 触发关闭回调通知
     */
    private TopViewBandWidthHzLarge.OnRulerChangedListener onLargeRulerChangedListener = new TopViewBandWidthHzLarge.OnRulerChangedListener() {
        @Override
        public void rulerChanged(double value, String unit, double item) {
//            PlaySound.get().playButton();
            if (TopUtilBandWidthHz.UNIT_NS.equals(unit)) {  // ns单位，直接与timeMin比较
                value = Math.max(value, timeMin);  // 不低于最小值
            } else if (TopUtilBandWidthHz.UNIT_US.equals(unit)  // us单位，timeMin在1us~10us范围
                    && timeMin > TopUtilBandWidthHz.TIME_US2NS
                    && timeMin < 10 * TopUtilBandWidthHz.TIME_US2NS) {
                value = Math.max(value, 1.0 * timeMin / TopUtilBandWidthHz.TIME_US2NS);  // 换算为us后比较
            } else if (TopUtilBandWidthHz.UNIT_US.equals(unit)  // us单位，timeMin在10us~100us范围
                    && timeMin > 10 * TopUtilBandWidthHz.TIME_US2NS
                    && timeMin < 100 * TopUtilBandWidthHz.TIME_US2NS) {
                value = Math.max(value, 1.0 * timeMin / (10 * TopUtilBandWidthHz.TIME_US2NS));  // 换算为10us单位后比较
            } else if (TopUtilBandWidthHz.UNIT_US.equals(unit)  // us单位，timeMin在100us~1000us范围
                    && timeMin > 100 * TopUtilBandWidthHz.TIME_US2NS
                    && timeMin < 1000 * TopUtilBandWidthHz.TIME_US2NS) {
                value = Math.max(value, 1.0 * timeMin / (100 * TopUtilBandWidthHz.TIME_US2NS));  // 换算为100us单位后比较
            }

            if (TopUtilBandWidthHz.UNIT_S.equals(unit)) {  // s单位，不超过timeMax
                value = Math.min(value, 1.0 * timeMax / TopUtilBandWidthHz.TIME_S2NS);  // 换算为s后比较
            }
            show.setText(TopUtilBandWidthHz.getHzFromS(TBookUtil.getD3FromD(value) + unit));  // 更新显示文本
            TopViewBandWidthHzSmall.setValue(value, unit, item / 100);  // 同步小刻度值（步进为大刻度1/100）
            listener();  // 触发关闭回调通知
        }
    };

    /**
     * 小刻度滚轮变化监听器。
     * <p>
     * 当小刻度值变化时：
     * 1. 根据当前单位和时间范围限制值（不低于timeMin，不超过timeMax）
     * 2. 更新显示文本
     * 3. 同步更新大刻度滚轮的值
     * 4. 触发关闭回调通知
     */
    private TopViewBandWidthHzSmall.OnRulerChangedListener onSmallRulerChangedListener = new TopViewBandWidthHzSmall.OnRulerChangedListener() {
        @Override
        public void rulerChanged(String value, String unit, double item) {
//            PlaySound.get().playButton();
            if (TopUtilBandWidthHz.UNIT_NS.equals(unit)) {  // ns单位，直接与timeMin比较
                value = String.valueOf(Math.max(Double.parseDouble(value), timeMin));  // 不低于最小值
            } else if (TopUtilBandWidthHz.UNIT_US.equals(unit)  // us单位，timeMin在1us~10us范围
                    && timeMin > TopUtilBandWidthHz.TIME_US2NS
                    && timeMin < 10 * TopUtilBandWidthHz.TIME_US2NS) {
                value = String.valueOf(Math.max(Double.parseDouble(value), 1.0 * timeMin / TopUtilBandWidthHz.TIME_US2NS));  // 换算比较
            } else if (TopUtilBandWidthHz.UNIT_US.equals(unit)  // us单位，timeMin在10us~100us范围
                    && timeMin > 10 * TopUtilBandWidthHz.TIME_US2NS
                    && timeMin < 100 * TopUtilBandWidthHz.TIME_US2NS) {
                value = String.valueOf(Math.max(Double.parseDouble(value), 1.0 * timeMin / (10 * TopUtilBandWidthHz.TIME_US2NS)));  // 换算比较
            } else if (TopUtilBandWidthHz.UNIT_US.equals(unit)  // us单位，timeMin在100us~1000us范围
                    && timeMin > 100 * TopUtilBandWidthHz.TIME_US2NS
                    && timeMin < 1000 * TopUtilBandWidthHz.TIME_US2NS) {
                value = String.valueOf(Math.max(Double.parseDouble(value), 1.0 * timeMin / (100 * TopUtilBandWidthHz.TIME_US2NS)));  // 换算比较
            }
            if (TopUtilBandWidthHz.UNIT_S.equals(unit)) {  // s单位，不超过timeMax
                value = String.valueOf(Math.min(Double.parseDouble(value), 1.0 * timeMax / TopUtilBandWidthHz.TIME_S2NS));  // 换算比较
            }
            show.setText(TopUtilBandWidthHz.getHzFromS(TBookUtil.getD3FromD(Double.parseDouble(value)) + unit));  // 更新显示文本
            topViewBandWidthHzLarge.setValue(Double.parseDouble(value), unit, item * 100);  // 同步大刻度值（步进为小刻度100倍）
            listener();  // 触发关闭回调通知
        }
    };

    /**
     * 判断触摸事件是否落在对话框区域内。
     * <p>
     * 用于外部判断是否需要消费触摸事件，防止穿透。
     *
     * @param event 触摸事件
     * @return true=触摸点在对话框内，false=不在
     */
    public boolean contains(MotionEvent event){
        if (event.getAction()==MotionEvent.ACTION_DOWN){  // 只处理按下事件
            int x=(int)event.getRawX();  // 获取触摸点X坐标
            int y=(int)event.getRawY();  // 获取触摸点Y坐标
            Rect rect= Tools.getViewRect(this);  // 获取对话框在屏幕上的矩形区域
            if (rect.contains(x,y)){  // 触摸点在对话框内
                return true;  // 返回true
            }else {  // 触摸点不在对话框内
                return false;  // 返回false
            }
        }
        return false;  // 非按下事件返回false
    }
}
