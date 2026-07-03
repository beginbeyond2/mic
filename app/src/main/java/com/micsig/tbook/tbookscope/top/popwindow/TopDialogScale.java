package com.micsig.tbook.tbookscope.top.popwindow; // 包声明：顶部弹出窗口模块

import android.content.Context; // 导入上下文类
import android.util.AttributeSet; // 导入属性集类
import android.view.MotionEvent; // 导入触摸事件类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组类
import android.widget.AbsoluteLayout; // 导入绝对布局类
import android.widget.TextView; // 导入文本视图类

import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量类
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入Rx事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx枚举常量
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.ui.top.view.scale.TopUtilScale; // 导入刻度工具类
import com.micsig.tbook.ui.top.view.scale.TopViewScaleLarge; // 导入大刻度视图
import com.micsig.tbook.ui.top.view.scale.TopViewScaleSmall; // 导入小刻度视图
import com.micsig.tbook.ui.util.TBookUtil; // 导入TBook工具类

import io.reactivex.rxjava3.functions.Consumer; // 导入Rx消费者接口


/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：顶部弹出窗口 - 刻度选择对话框                            │
 * │ 核心职责：提供时间刻度的大/小刻度尺滚动选择交互界面                  │
 * │ 架构设计：继承AbsoluteLayout，内嵌大小刻度尺视图，通过RxBus通信     │
 * │ 数据流向：setValue → 刻度尺视图 → rulerChanged回调 → listener回调  │
 * │ 依赖关系：TopViewScaleLarge, TopViewScaleSmall, TopUtilScale      │
 * │ 使用场景：时基刻度调节、水平刻度参数设置                            │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by Administrator on 2017/4/11.
 */
public class TopDialogScale extends AbsoluteLayout { // 刻度选择对话框，继承绝对布局
    private Context context; // 上下文对象
    private TextView head, show; // 标题文本和显示值文本
    private TopViewScaleLarge topViewScaleLarge; // 大刻度尺视图
    private TopViewScaleSmall topViewScaleSmall; // 小刻度尺视图
    private OnDismissListener onDismissListener; // 关闭回调监听器

    private ViewGroup rootViewGroup; // 根视图组
    private long timeMin = TopUtilScale.DEFAULT_MIN_TIME; // 时间最小值，默认值
    private long timeMax = TopUtilScale.DEFAULT_MAX_TIME; // 时间最大值，默认值

    /**
     * 关闭回调接口，返回用户选择的刻度结果
     */
    public interface OnDismissListener { // 关闭回调接口
        /**
         * 对话框关闭时回调
         * @param result 用户选择的刻度结果字符串
         */
        void onDismiss(String result); // 关闭时回调方法
    }

    /**
     * 单参数构造函数
     * @param context 上下文对象
     */
    public TopDialogScale(Context context) { // 单参数构造
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     */
    public TopDialogScale(Context context, AttributeSet attrs) { // 双参数构造
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopDialogScale(Context context, AttributeSet attrs, int defStyleAttr) { // 三参数构造
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文
        initView(); // 初始化视图
    }

    //[50, 342]	608	170
    /**
     * 初始化视图和事件监听
     */
    private void initView() { // 初始化视图方法
        setClickable(true); // 设置可点击
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_scale, this); // 加载布局

        rootViewGroup.findViewById(R.id.outView).setOnClickListener((v)->{ // 设置外部区域点击监听
            listener(); // 触发关闭回调
            hide(); // 隐藏对话框
        });
        head = (TextView) rootViewGroup.findViewById(R.id.head); // 获取标题文本视图
        show = (TextView) rootViewGroup.findViewById(R.id.show); // 获取显示值文本视图
        topViewScaleLarge = (TopViewScaleLarge) rootViewGroup.findViewById(R.id.scaleLargeView); // 获取大刻度尺视图
        topViewScaleLarge.setOnRulerChangedListener(onLargeRulerChangedListener); // 设置大刻度尺变化监听
        topViewScaleSmall = (TopViewScaleSmall) rootViewGroup.findViewById(R.id.scaleSmallView); // 获取小刻度尺视图
        topViewScaleSmall.setOnRulerChangedListener(onSmallRulerChangedListener); // 设置小刻度尺变化监听

        hide(); // 初始隐藏
        RxBus.getInstance().getObservable(RxEnum.DIALOG_SCALE_CHANGED).subscribe(consumerScaleChanged); // 订阅刻度变化事件
    }

    /**
     * 触发关闭回调，将当前显示值转换为对齐最小间隔后的时间字符串
     */
    public void listener() { // 触发关闭回调方法
        if (onDismissListener != null) { // 判断回调不为空
            int minInterval = GlobalVar.get().getTimeMinInterval(); // 获取时间最小间隔
            long ns = TBookUtil.getPsFromTime(show.getText().toString()) / 1000; // 将显示文本转换为纳秒
            ns = ns - ns % minInterval; // 对齐到最小间隔
            onDismissListener.onDismiss(TBookUtil.getTime3FromPs(ns * 1000 * 10)); // 转换为时间字符串并回调
        }
    }

    /**
     * 显示对话框
     */
    public void show() { // 显示方法
        setVisibility(VISIBLE); // 设置可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_TOPSCALE); // 发送对话框打开事件
        Tools.PrintControlsLocation("TopDialogScale",rootViewGroup); // 打印控件位置调试信息
    }

    /**
     * 隐藏对话框
     */
    public void hide() { // 隐藏方法
        setVisibility(GONE); // 设置不可见且不占空间
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_TOPSCALE); // 发送对话框关闭事件
    }

    /**
     * 设置刻度值并显示对话框
     * @param headString 标题字符串
     * @param value 当前刻度值字符串
     * @param timeMin 时间最小值（纳秒）
     * @param timeMax 时间最大值（纳秒）
     * @param onDismissListener 关闭回调监听器
     */
    public void setValue(String headString, String value, long timeMin, long timeMax, OnDismissListener onDismissListener) { // 设置刻度值
        this.timeMin = Math.min(timeMin, TopUtilScale.TIME_US2NS); // 限制最小值不超过1微秒
        this.timeMax = Math.max(timeMax, TopUtilScale.TIME_S2NS); // 限制最大值不小于1秒
        long ns = TopUtilScale.getNSFromValue(value); // 将值字符串转换为纳秒
        ns = ns < timeMin ? timeMin : ns; // 限制不低于最小值
        ns = ns > timeMax ? timeMax : ns; // 限制不高于最大值
        head.setText(headString); // 设置标题
        setValue(ns, onDismissListener); // 调用内部设置方法
    }

    /**
     * 内部设置刻度值方法
     * @param ns 纳秒值
     * @param onDismissListener 关闭回调监听器
     */
    private void setValue(long ns, OnDismissListener onDismissListener) { // 内部设置刻度值
        this.onDismissListener = onDismissListener; // 保存回调监听器
        TopUtilScale.ScaleValue scaleValue = new TopUtilScale().createScaleValue(); // 创建刻度值对象
        TopUtilScale.getValueFromNS(ns, scaleValue); // 从纳秒值解析刻度值
        show.setText(TBookUtil.getD3FromD(scaleValue.value) + scaleValue.itemUnit); // 显示格式化后的值和单位
        topViewScaleLarge.setTimeRange(timeMin, timeMax); // 设置大刻度尺时间范围
        topViewScaleSmall.setTimeRange(timeMin, timeMax); // 设置小刻度尺时间范围
        topViewScaleLarge.setValue(scaleValue.value, scaleValue.itemUnit, scaleValue.itemValue); // 设置大刻度尺当前值
        topViewScaleSmall.setValue(scaleValue.value, scaleValue.itemUnit, scaleValue.itemValue / 100); // 设置小刻度尺当前值（精度1/100）
        show(); // 显示对话框
    }

    /**
     * 刻度变化事件消费者，处理按钮操作和完成事件
     */
    private Consumer<Integer> consumerScaleChanged = new Consumer<Integer>() { // 刻度变化消费者
        @Override
        public void accept(Integer integer) throws Exception { // 消费事件
            switch (integer) { // 根据事件类型分发
                case TopUtilScale.ACTION_SCALE_FINISH: // 刻度选择完成
                    listener(); // 触发关闭回调
                    hide(); // 隐藏对话框
                    break;
                case TopUtilScale.ACTION_SCALE_LARGE_LEFT: // 大刻度尺左移
                    topViewScaleLarge.moveLeftOneStep(); // 大刻度尺左移一步
                    break;
                case TopUtilScale.ACTION_SCALE_LARGE_RIGHT: // 大刻度尺右移
                    topViewScaleLarge.moveRightOneStep(); // 大刻度尺右移一步
                    break;
                case TopUtilScale.ACTION_SCALE_SMALL_LEFT: // 小刻度尺左移
                    topViewScaleSmall.moveLeftOneStep(); // 小刻度尺左移一步
                    break;
                case TopUtilScale.ACTION_SCALE_SMALL_RIGHT: // 小刻度尺右移
                    topViewScaleSmall.moveRightOneStep(); // 小刻度尺右移一步
                    break;
            }
        }
    };

    /**
     * 大刻度尺变化监听器
     */
    private TopViewScaleLarge.OnRulerChangedListener onLargeRulerChangedListener = new TopViewScaleLarge.OnRulerChangedListener() { // 大刻度尺监听器
        @Override
        public void rulerChanged(double value, String unit, double item) { // 刻度值变化回调
//            PlaySound.get().playButton();
            if (TopUtilScale.UNIT_NS.equals(unit)) { // 如果单位是纳秒
                value = Math.max(value, timeMin); // 限制不低于最小值
            }
            if (TopUtilScale.UNIT_S.equals(unit)) { // 如果单位是秒
                value = Math.min(value, timeMax / TopUtilScale.TIME_S2NS); // 限制不高于最大值（转换为秒）
            }
            show.setText(TBookUtil.getD3FromD(value) + unit); // 更新显示值
            topViewScaleSmall.setValue(value, unit, item / 100); // 同步更新小刻度尺
            listener(); // 触发关闭回调
        }
    };

    /**
     * 小刻度尺变化监听器
     */
    private TopViewScaleSmall.OnRulerChangedListener onSmallRulerChangedListener = new TopViewScaleSmall.OnRulerChangedListener() { // 小刻度尺监听器
        @Override
        public void rulerChanged(String value, String unit, double item) { // 刻度值变化回调
//            PlaySound.get().playButton();
            if (TopUtilScale.UNIT_NS.equals(unit)) { // 如果单位是纳秒
                value = String.valueOf(Math.max(Double.parseDouble(value), timeMin)); // 限制不低于最小值
            }
            if (TopUtilScale.UNIT_S.equals(unit)) { // 如果单位是秒
                value = String.valueOf(Math.min(Double.parseDouble(value), timeMax / TopUtilScale.TIME_S2NS)); // 限制不高于最大值
            }
            show.setText(TBookUtil.getD3FromD(Double.parseDouble(value)) + unit); // 更新显示值
            topViewScaleLarge.setValue(Double.parseDouble(value), unit, item * 100); // 同步更新大刻度尺
            listener(); // 触发关闭回调
        }
    };
}
