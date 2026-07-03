package com.micsig.tbook.tbookscope.top.popwindow; // 包声明：顶部弹出窗口模块

import android.annotation.SuppressLint; // 导入抑制lint注解
import android.content.Context; // 导入上下文类
import android.provider.Settings; // 导入系统设置类
import android.util.AttributeSet; // 导入属性集类
import android.view.MotionEvent; // 导入触摸事件类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组类
import android.widget.AbsoluteLayout; // 导入绝对布局类
import android.widget.Button; // 导入按钮类
import android.widget.RadioButton; // 导入单选按钮类
import android.widget.TextView; // 导入文本视图类

import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量类
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入Rx事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx枚举常量
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.layout.measure.MeasureBean; // 导入测量数据Bean
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入单选组视图
import com.micsig.tbook.ui.util.ScreenUtil; // 导入屏幕工具类
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：顶部弹出窗口 - 延迟测量对话框                             │
 * │ 核心职责：提供延迟测量的通道选择和位置选择交互界面                    │
 * │ 架构设计：继承AbsoluteLayout，内嵌四个TopViewRadioGroup互斥选择    │
 * │ 数据流向：setData → 用户选择通道和位置 → onSure回调返回参数        │
 * │ 依赖关系：TopViewRadioGroup, MeasureBean, GlobalVar               │
 * │ 使用场景：延迟测量时选择自身位置、参考通道和参考位置                  │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/4/25.
 */

public class TopDialogMeasureDelay extends AbsoluteLayout { // 延迟测量对话框，继承绝对布局
    private Context context; // 上下文对象
    private RadioButton head; // 标题单选按钮
    private TopViewRadioGroup selfPosView, otherChView, otherMathView, otherPosView; // 自身位置、参考通道、参考Math、参考位置选择组
    private MeasureBean measureBean; // 测量数据Bean
    private OnSureListener onSureListener; // 确认回调监听器

    private ViewGroup rootViewGroup; // 根视图组
    private final int channelCount = GlobalVar.get().getChannelsCount(); // 通道数量

    /**
     * 确认回调接口，返回延迟测量参数
     */
    public interface OnSureListener { // 确认回调接口
        /**
         * 确认时回调
         * @param measureBean 测量数据Bean
         * @param selfChannel 自身通道名称
         * @param selfPosition 自身位置索引
         * @param otherChannel 参考通道索引
         * @param otherPosition 参考位置索引
         */
        void onSure(MeasureBean measureBean, String selfChannel, int selfPosition, int otherChannel, int otherPosition); // 确认回调方法
    }

    /**
     * 单参数构造函数
     * @param context 上下文对象
     */
    public TopDialogMeasureDelay(Context context) { // 单参数构造
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     */
    public TopDialogMeasureDelay(Context context, AttributeSet attrs) { // 双参数构造
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopDialogMeasureDelay(Context context, AttributeSet attrs, int defStyleAttr) { // 三参数构造
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文
        initView(); // 初始化视图
    }

    /**
     * 初始化视图和事件监听
     */
    private void initView() { // 初始化视图方法
        setClickable(true); // 设置可点击
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_measuredelay, this); // 加载布局

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() { // 设置外部区域触摸监听
            @Override
            public boolean onTouch(View v, MotionEvent event) { // 触摸事件回调
                hide(); // 隐藏对话框
                return false; // 不消费触摸事件
            }
        });

        head = (RadioButton) rootViewGroup.findViewById(R.id.head); // 获取标题单选按钮
        TextView sure = (Button) rootViewGroup.findViewById(R.id.sure); // 获取确认按钮（注意：声明为Button）
        selfPosView = (TopViewRadioGroup) rootViewGroup.findViewById(R.id.selfPosition); // 获取自身位置选择组
        selfPosView.setData(null, context.getResources().getStringArray(R.array.measureDelayPosition), onCheckChangedListener); // 设置自身位置数据
        otherChView = (TopViewRadioGroup) rootViewGroup.findViewById(R.id.otherChannel); // 获取参考通道选择组
        otherMathView = (TopViewRadioGroup) rootViewGroup.findViewById(R.id.otherMath); // 获取参考Math选择组
        String[] channel1 = GlobalVar.get().getChannelsName(); // 获取通道名称数组
        String[] channel2 = context.getResources().getStringArray(R.array.measureDelayToChannel); // 获取Math通道名称数组
        otherChView.setData(null, channel1, onCheckChangedListener); // 设置参考通道数据
        otherMathView.setData(null, channel2, onCheckChangedListener); // 设置参考Math数据
        otherPosView = (TopViewRadioGroup) rootViewGroup.findViewById(R.id.otherPosition); // 获取参考位置选择组
        otherPosView.setData(null, context.getResources().getStringArray(R.array.measureDelayPosition), onCheckChangedListener); // 设置参考位置数据
        sure.setOnClickListener(onClickListener); // 设置确认按钮点击监听

        hide(); // 初始隐藏
    }

    /**
     * 显示对话框
     */
    public void show() { // 显示方法
        setVisibility(VISIBLE); // 设置可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_MEASUREDELAY); // 发送对话框打开事件
        Tools.PrintControlsLocation("TopDialogMeasureDelay",rootViewGroup); // 打印控件位置调试信息
    }

    /**
     * 隐藏对话框
     */
    public void hide() { // 隐藏方法
        setVisibility(GONE); // 设置不可见且不占空间
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_MEASUREDELAY); // 发送对话框关闭事件
    }

    /**
     * 获取扩展按键位置，返回自身位置的索引
     * @return 自身位置索引
     */
    public int getExKeysPosition() { // 获取扩展按键位置
        return selfPosView.getSelected().getIndex(); // 返回自身位置选中索引
    }

    /**
     * 设置缓存值，恢复上次的选择状态
     * @param selfPosCache 自身位置缓存索引
     * @param otherChCache 参考通道缓存索引
     * @param otherPosCache 参考位置缓存索引
     */
    public void setCache(int selfPosCache, int otherChCache, int otherPosCache) { // 设置缓存值
        selfPosView.setSelectedIndex(selfPosCache); // 恢复自身位置选中
        if (otherChCache < channelCount) { // 如果参考通道索引在通道范围内
            otherChView.setSelectedIndex(otherChCache); // 选中对应通道
            otherMathView.clearCheck(); // 清除Math选中
        } else { // 如果参考通道索引在Math范围内
            otherChView.clearCheck(); // 清除通道选中
            otherMathView.setSelectedIndex(otherChCache - channelCount); // 选中对应Math（偏移通道数）
        }
        otherPosView.setSelectedIndex(otherPosCache); // 恢复参考位置选中
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
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 确认按钮监听器
        @Override
        public void onClick(View v) { // 点击回调
            hide(); // 隐藏对话框
            ScreenUtil.getViewLocation(v); // 获取视图位置
            if (onSureListener != null) { // 判断回调不为空
                int otherChannel = otherChView.getSelected() == null ? otherMathView.getSelected().getIndex() + channelCount : otherChView.getSelected().getIndex(); // 计算参考通道索引
                onSureListener.onSure(measureBean, head.getText().toString(), selfPosView.getSelected().getIndex(), otherChannel, otherPosView.getSelected().getIndex()); // 回调确认方法
            }
        }
    };


    /**
     * 单选组变化监听器，实现通道与Math互斥选择
     */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 单选组变化监听器
        @Override
        public void onClickSound(boolean isCheckedSuccess) { // 选中音效回调
            PlaySound.getInstance().playButton(); // 播放按钮音效
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) { // 提示回调（空实现）

        }

        @SuppressLint("NonConstantResourceId") // 抑制非常量资源ID警告
        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 点击回调
            switch (view.getId()) { // 根据视图ID分发
                case R.id.selfPosition: // 点击了自身位置选择组
                    break; // 无额外操作
                case R.id.otherChannel: // 点击了参考通道选择组
                    otherMathView.clearCheck(); // 清除Math选中（互斥）
                    break;
                case R.id.otherMath: // 点击了参考Math选择组
                    otherChView.clearCheck(); // 清除通道选中（互斥）
                    break;
                case R.id.otherPosition: // 点击了参考位置选择组
                    break; // 无额外操作
            }
        }
    };
}
