package com.micsig.tbook.tbookscope.top.popwindow; // 包声明：顶部弹出窗口模块

import android.annotation.SuppressLint; // 导入抑制lint注解
import android.content.Context; // 导入上下文类
import android.util.AttributeSet; // 导入属性集类
import android.view.MotionEvent; // 导入触摸事件类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组类
import android.widget.AbsoluteLayout; // 导入绝对布局类
import android.widget.RadioButton; // 导入单选按钮类
import android.widget.TextView; // 导入文本视图类

import com.micsig.base.DoubleUtil; // 导入双精度工具类
import com.micsig.tbook.scope.ScopeBase; // 导入示波器基类
import com.micsig.tbook.scope.channel.Channel; // 导入通道类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类
import com.micsig.tbook.scope.measure.MeasureService; // 导入测量服务类
import com.micsig.tbook.scope.vertical.VerticalAxis; // 导入垂直轴类
import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量类
import com.micsig.tbook.tbookscope.MainActivity; // 导入主活动类
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令类
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogChannelLabel; // 导入通道标签对话框
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入Rx事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx枚举常量
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.layout.measure.MeasureBean; // 导入测量数据Bean
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard; // 导入浮点键盘对话框
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 导入数字位数接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘对话框
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 导入工作模式管理
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 导入光标管理
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage; // 导入波形管理
import com.micsig.tbook.ui.MSwitchBox; // 导入开关盒子控件
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入可编辑视图
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入单选组视图
import com.micsig.tbook.ui.util.ScreenUtil; // 导入屏幕工具类
import com.micsig.tbook.ui.util.TBookUtil; // 导入TBook工具类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道视图类

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：顶部弹出窗口 - T值测量对话框                             │
 * │ 核心职责：提供T值测量的电压值、边沿次数和光标选择交互界面            │
 * │ 架构设计：继承AbsoluteLayout，内嵌浮点键盘和数字键盘               │
 * │ 数据流向：setData → 用户编辑 → onSure回调返回测量参数              │
 * │ 依赖关系：TopDialogFloatKeyBoard, TopViewRadioGroup, MeasureBean  │
 * │ 使用场景：T值测量参数设置（电压阈值、边沿次数、光标位置）            │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/4/25.
 */

public class TopDialogMeasureTValue extends AbsoluteLayout { // T值测量对话框，继承绝对布局
    private Context context; // 上下文对象
    private RadioButton head; // 标题单选按钮
    private TopViewRadioGroup rValueCursor; // 光标选择单选组
    private TopViewEdit tvalue_voltage,tvalue_edge_occurence; // 电压值编辑框和边沿次数编辑框

    protected TopDialogNumberKeyBoard dialogKeyBoard; // 数字键盘对话框
    private TopDialogFloatKeyBoard dialogFloatKeyBoard; // 浮点键盘对话框
    private MeasureBean measureBean; // 测量数据Bean
    private OnSureListener onSureListener; // 确认回调监听器
    private ViewGroup rootViewGroup; // 根视图组

    /**
     * 确认回调接口，返回T值测量参数
     */
    public interface OnSureListener { // 确认回调接口
        /**
         * 确认时回调
         * @param measureBean 测量数据Bean
         * @param vol 电压值
         * @param edgeOccurence 边沿次数
         * @param cursorIndex 光标索引
         */
        void onSure(MeasureBean measureBean, double vol, int edgeOccurence, int cursorIndex); // 确认回调方法
    }

    /**
     * 单参数构造函数
     * @param context 上下文对象
     */
    public TopDialogMeasureTValue(Context context) { // 单参数构造
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     */
    public TopDialogMeasureTValue(Context context, AttributeSet attrs) { // 双参数构造
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopDialogMeasureTValue(Context context, AttributeSet attrs, int defStyleAttr) { // 三参数构造
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文
        initView(); // 初始化视图
    }

    /**
     * 初始化视图和事件监听
     */
    private void initView() { // 初始化视图方法
        setClickable(true); // 设置可点击
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_measuretvalue, this); // 加载布局

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() { // 设置外部区域触摸监听
            @Override
            public boolean onTouch(View v, MotionEvent event) { // 触摸事件回调
                hide(); // 隐藏对话框
                return false; // 不消费触摸事件
            }
        });
        head = (RadioButton) rootViewGroup.findViewById(R.id.head); // 获取标题单选按钮
        TextView sure = (TextView) rootViewGroup.findViewById(R.id.sure); // 获取确认按钮
        sure.setOnClickListener(onClickListener); // 设置确认按钮点击监听
        tvalue_voltage = rootViewGroup.findViewById(R.id.tvalue_voltage); // 获取电压值编辑框
        tvalue_edge_occurence = rootViewGroup.findViewById(R.id.tvalue_edge_occurence); // 获取边沿次数编辑框
        rValueCursor = findViewById(R.id.tvalue_cursor); // 获取光标选择单选组
        rValueCursor.setData(R.string.tValueCursor, R.array.tValueCursor, onCheckChangedListener); // 设置光标选择数据
        tvalue_voltage.setOnClickEditListener(onClickEditListener); // 设置电压编辑框点击监听
        tvalue_edge_occurence.setOnClickEditListener(onClickEditListener); // 设置边沿次数编辑框点击监听

        hide(); // 初始隐藏
    }

    /**
     * 编辑框点击监听器，弹出对应键盘
     */
    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() { // 编辑框点击监听器
        @Override
        public void onClickEdit(TopViewEdit v, String text) { // 编辑框点击回调
            PlaySound.getInstance().playButton(); // 播放按钮音效
            if(dialogFloatKeyBoard == null) { // 如果浮点键盘未初始化
                dialogFloatKeyBoard =  ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard); // 从主活动获取浮点键盘
            }
            if (v.getId() == tvalue_voltage.getId()) { // 如果点击的是电压编辑框
                dialogFloatKeyBoard.setFloatData_Offset(tvalue_voltage.getText().replace("A", "") // 弹出浮点键盘，移除A单位
                                .replace("V", "").replace(" ", ""), // 移除V单位和空格
                        true, tvalue_voltage, new TopDialogFloatKeyBoard.OnDismissListener() { // 设置浮点键盘数据
                    @Override
                    public void onDismiss(View fromView, String show) { // 浮点键盘关闭回调
                        PlaySound.getInstance().playButton(); // 播放按钮音效
                        tvalue_voltage.setText(show); // 更新电压编辑框文本
                    }
                });

            } else if (v.getId() == tvalue_edge_occurence.getId()) { // 如果点击的是边沿次数编辑框
                dialogFloatKeyBoard.setNumberData(tvalue_edge_occurence.getText().trim(), true, 7, tvalue_edge_occurence, new TopDialogFloatKeyBoard.OnDismissListener() { // 弹出数字键盘
                    @Override
                    public void onDismiss(View fromView, String result) { // 数字键盘关闭回调
                        PlaySound.getInstance().playButton(); // 播放按钮音效
                        tvalue_edge_occurence.setText(result); // 更新边沿次数编辑框文本
                    }
                });
            }
        }
    };

    /**
     * 显示对话框
     */
    public void show() { // 显示方法
        setVisibility(VISIBLE); // 设置可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_MEASURE_TVALUE); // 发送对话框打开事件
        Tools.PrintControlsLocation("TopDialogMeasureTValue",rootViewGroup); // 打印控件位置调试信息
    }

    /**
     * 隐藏对话框
     */
    public void hide() { // 隐藏方法
        setVisibility(GONE); // 设置不可见且不占空间
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_MEASURE_TVALUE); // 发送对话框关闭事件
    }

    /**
     * 获取扩展按键位置（预留接口）
     * @return 位置索引，当前固定返回0
     */
    public int getExKeysPosition() { // 获取扩展按键位置
        //return otherChView.getSelected() == null ? otherMathView.getSelected().getIndex() + channelCount : otherChView.getSelected().getIndex();
        return 0; // 固定返回0
    }

    /**
     * 设置缓存值，恢复上次的参数状态
     * @param vol 电压值
     * @param edgeOccurence 边沿次数
     * @param cursorIndex 光标索引
     */
    public void setCache(double vol, int edgeOccurence, int cursorIndex) { // 设置缓存值
        tvalue_voltage.setText(String.valueOf(vol)); // 恢复电压值
        tvalue_edge_occurence.setText(String.valueOf(edgeOccurence)); // 恢复边沿次数
        rValueCursor.setSelectedIndex(cursorIndex); // 恢复光标选中索引
    }

    /**
     * 设置数据并显示对话框
     * @param selfChannel 自身通道名称
     * @param measureBean 测量数据Bean
     * @param onSureListener 确认回调监听器
     */
    public void setData(String selfChannel, MeasureBean measureBean, OnSureListener onSureListener) { // 设置数据
        this.measureBean = measureBean; // 保存测量Bean
        this.onSureListener = onSureListener; // 保存确认回调
        head.setText(selfChannel); // 设置标题为通道名称
        show(); // 显示对话框
    }

    /**
     * 确认按钮点击监听器
     */
    private OnClickListener onClickListener = new OnClickListener() { // 确认按钮监听器
        @Override
        public void onClick(View v) { // 点击回调
            hide(); // 隐藏对话框
            ScreenUtil.getViewLocation(v); // 获取视图位置
            if (onSureListener != null) { // 判断回调不为空
                onSureListener.onSure(measureBean, // 回调确认方法
                        TBookUtil.getDoubleFromM(tvalue_voltage.getText()), // 将电压文本转为double
                        (int) TBookUtil.getDoubleFromM(tvalue_edge_occurence.getText()), // 将边沿次数文本转为int
                        rValueCursor.getSelected().getIndex() // 获取光标选中索引
                );
            }
        }
    };


    /**
     * 单选组变化监听器
     */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选组变化监听器
        @Override
        public void onClickSound(boolean isCheckedSuccess) { // 选中音效回调
            PlaySound.getInstance().playButton(); // 播放按钮音效
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) { // 提示回调（空实现）

        }

        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 点击回调
            onCheckChanged(view, item, false); // 委托给onCheckChanged处理
        }
    };


    /**
     * 单选组选中变化处理
     * @param view 单选组视图
     * @param item 选中的项
     * @param isFormHardware 是否来自硬件
     */
    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFormHardware) { // 选中变化处理
        if (view.getId() == rValueCursor.getId()) { // 如果是光标选择组
            rValueCursor.setSelectedIndex(item.getIndex()); // 更新光标选中索引
        }
    }


}
