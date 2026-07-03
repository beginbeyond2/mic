package com.micsig.tbook.tbookscope.top.popwindow; // 包声明：顶部弹出窗口模块

import android.content.Context; // 导入上下文类
import android.util.AttributeSet; // 导入属性集类
import android.view.MotionEvent; // 导入触摸事件类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组类
import android.widget.RelativeLayout; // 导入相对布局类

import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入Rx事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx枚举常量
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.ui.TopViewNumberPicker; // 导入数字选择器视图

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：顶部弹出窗口 - 数字选择器对话框                          │
 * │ 核心职责：提供数字和幂次的滚动选择交互界面                          │
 * │ 架构设计：继承RelativeLayout的自定义对话框，内嵌TopViewNumberPicker │
 * │ 数据流向：外部setData → NumberPicker视图 → onDismiss回调          │
 * │ 依赖关系：TopViewNumberPicker, RxBus, MainViewGroup              │
 * │ 使用场景：公式键盘中的数字+幂次选择                                │
 * └──────────────────────────────────────────────────────────────────┘
 */
public class TopDialogNumberPicker extends RelativeLayout { // 数字选择器对话框，继承相对布局
    private static final String TAG = "TopDialogNumberPicker"; // 日志标签
    private Context context; // 上下文对象
    private TopViewNumberPicker numberPickerView; // 数字选择器视图
    private OnDismissListener onDismissListener; // 关闭回调监听器
    private String number, power; // 当前选中的数字和幂次
    private ViewGroup rootViewGroup; // 根视图组

    /**
     * 关闭回调接口，返回用户选择的数字和幂次
     */
    public interface OnDismissListener { // 关闭回调接口
        /**
         * 对话框关闭时回调
         * @param number 选中的数字字符串
         * @param power 选中的幂次字符串
         */
        void onDismiss(String number, String power); // 关闭时回调方法
    }

    /**
     * 单参数构造函数
     * @param context 上下文对象
     */
    public TopDialogNumberPicker(Context context) { // 单参数构造
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     */
    public TopDialogNumberPicker(Context context, AttributeSet attrs) { // 双参数构造
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopDialogNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) { // 三参数构造
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文
        init(); // 初始化视图
    }

    /**
     * 初始化视图和事件监听
     */
    private void init() { // 初始化方法
        setClickable(true); // 设置可点击
        rootViewGroup = (ViewGroup) inflate(context, R.layout.dialog_numberpicker, this); // 加载布局

        numberPickerView = (TopViewNumberPicker) rootViewGroup.findViewById(R.id.numberPickerView); // 获取数字选择器视图
        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() { // 设置外部区域触摸监听
            @Override
            public boolean onTouch(View v, MotionEvent event) { // 触摸事件回调
                onDismissListener.onDismiss(number, power); // 回调关闭监听，传递当前数字和幂次
                hide(); // 隐藏对话框
                return false; // 不消费触摸事件
            }
        });
        hide(); // 初始隐藏
    }

    /**
     * 设置数据并显示对话框
     * @param number 初始数字字符串
     * @param power 初始幂次字符串
     * @param onDismissListener 关闭回调监听器
     */
    public void setData(String number, String power, OnDismissListener onDismissListener) { // 设置数据
        this.number = number; // 保存数字
        this.power = power; // 保存幂次
        this.onDismissListener = onDismissListener; // 保存回调监听器
        show(); // 显示对话框
        numberPickerView.setData(this.number, this.power, onNumberPickerListener); // 设置选择器数据
    }

    /**
     * 显示对话框
     */
    public void show() { // 显示方法
        setVisibility(VISIBLE); // 设置可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_FORMULAKEYBOARD); // 发送对话框打开事件
        Tools.PrintControlsLocation("TopDialogNumBerPicker",rootViewGroup); // 打印控件位置调试信息
    }

    /**
     * 隐藏对话框
     */
    public void hide() { // 隐藏方法
        setVisibility(GONE); // 设置不可见且不占空间
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_FORMULAKEYBOARD); // 发送对话框关闭事件
    }

    /**
     * 数字选择器变化监听器
     */
    private TopViewNumberPicker.OnNumberPickerListener onNumberPickerListener = new TopViewNumberPicker.OnNumberPickerListener() { // 选择器监听器
        @Override
        public void onChangedShow(String s1, String s2) { // 选择值变化回调
            PlaySound.getInstance().playButton(); // 播放按钮音效
            TopDialogNumberPicker.this.number = s1; // 更新当前数字
            TopDialogNumberPicker.this.power = s2; // 更新当前幂次
        }
    };


    /**
     * 展开指定索引的选择项
     * @param index 要展开的项索引
     */
    public void openExpand(int index) { // 展开选择项
        numberPickerView.openExpand(index); // 委托给选择器视图展开
    }

    /**
     * 对指定索引的项加一或减一
     * @param index 操作的项索引
     * @param isAdd true为加一，false为减一
     */
    public void addOne(int index, boolean isAdd) { // 增减操作
        numberPickerView.addOne(index, isAdd); // 委托给选择器视图执行增减
    }
}
