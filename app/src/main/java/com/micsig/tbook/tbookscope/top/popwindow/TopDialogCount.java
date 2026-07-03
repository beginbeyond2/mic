package com.micsig.tbook.tbookscope.top.popwindow; // 包声明：顶部弹出窗口模块

import android.content.Context; // 导入上下文类
import android.util.AttributeSet; // 导入属性集类
import android.view.MotionEvent; // 导入触摸事件类
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组类
import android.widget.AbsoluteLayout; // 导入绝对布局类
import android.widget.Button; // 导入按钮类
import android.widget.RadioButton; // 导入单选按钮类
import android.widget.SeekBar; // 导入滑动条类
import android.widget.TextView; // 导入文本视图类

import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入Rx事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入Rx枚举常量
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.ui.util.ScreenUtil; // 导入屏幕工具类

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：顶部弹出窗口 - 计数器对话框                               │
 * │ 核心职责：提供计数值的滑动条+按钮调节交互界面                        │
 * │ 架构设计：继承AbsoluteLayout，内嵌SeekBar和x1/x100倍率切换         │
 * │ 数据流向：setData → 用户调节 → onDismiss回调返回计数值              │
 * │ 依赖关系：SeekBar, RxBus, MainViewGroup                           │
 * │ 使用场景：采样计数、平均次数等数值调节                                │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/4/24.
 */

public class TopDialogCount extends AbsoluteLayout { // 计数器对话框，继承绝对布局
    private int curCount = 321; // 当前计数值，默认321
    private int maxCount = 65535; // 最大计数值，默认65535

    private Context context; // 上下文对象
    private TextView title, show; // 标题文本和显示值文本
    private RadioButton x1, x100; // x1倍率和x100倍率单选按钮
    private Button subtract, add; // 减一按钮和加一按钮
    private SeekBar seekBar; // 滑动条
    private OnDismissListener onDismissListener; // 关闭回调监听器
    private boolean isFromUser; // 标记是否来自用户操作

    private ViewGroup rootViewGroup; // 根视图组

    /**
     * 关闭回调接口，返回用户选择的计数值
     */
    public interface OnDismissListener { // 关闭回调接口
        /**
         * 对话框关闭时回调
         * @param result 用户选择的计数值
         */
        void onDismiss(int result); // 关闭时回调方法
    }

    /**
     * 单参数构造函数
     * @param context 上下文对象
     */
    public TopDialogCount(Context context) { // 单参数构造
        this(context, null); // 委托给双参数构造
    }

    /**
     * 双参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     */
    public TopDialogCount(Context context, AttributeSet attrs) { // 双参数构造
        this(context, attrs, 0); // 委托给三参数构造
    }

    /**
     * 三参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public TopDialogCount(Context context, AttributeSet attrs, int defStyleAttr) { // 三参数构造
        super(context, attrs, defStyleAttr); // 调用父类构造
        this.context = context; // 保存上下文
        init(); // 初始化视图
    }

    //[50, 387]	608	125
    /**
     * 初始化视图和事件监听
     */
    private void init() { // 初始化方法
        setClickable(true); // 设置可点击
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_count, this); // 加载布局

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() { // 设置外部区域触摸监听
            @Override
            public boolean onTouch(View v, MotionEvent event) { // 触摸事件回调
                if (onDismissListener != null) { // 判断回调不为空
                    onDismissListener.onDismiss(Integer.parseInt(show.getText().toString())); // 回调当前计数值
                }
                hide(); // 隐藏对话框
                return false; // 不消费触摸事件
            }
        });
        initView(rootViewGroup); // 初始化子视图

        hide(); // 初始隐藏
    }

    /**
     * 初始化子视图引用和事件监听
     * @param view 根视图
     */
    private void initView(View view) { // 初始化子视图方法
        title = (TextView) view.findViewById(R.id.title); // 获取标题文本
        show = (TextView) view.findViewById(R.id.show); // 获取显示值文本
        x1 = (RadioButton) view.findViewById(R.id.x1); // 获取x1倍率按钮
        x100 = (RadioButton) view.findViewById(R.id.x100); // 获取x100倍率按钮
        subtract = (Button) view.findViewById(R.id.subtract); // 获取减一按钮
        add = (Button) view.findViewById(R.id.add); // 获取加一按钮
        seekBar = (SeekBar) view.findViewById(R.id.progress); // 获取滑动条

        x1.setOnClickListener(onCheckedListener); // 设置x1按钮点击监听
        x100.setOnClickListener(onCheckedListener); // 设置x100按钮点击监听
        subtract.setOnClickListener(onCheckedListener); // 设置减一按钮点击监听
        add.setOnClickListener(onCheckedListener); // 设置加一按钮点击监听
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener); // 设置滑动条变化监听
    }

    /**
     * 显示对话框
     */
    public void show() { // 显示方法
        setVisibility(VISIBLE); // 设置可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_TOPCOUNT); // 发送对话框打开事件
        Tools.PrintControlsLocation("TopDialogCount",rootViewGroup); // 打印控件位置调试信息
    }

    /**
     * 隐藏对话框
     */
    public void hide() { // 隐藏方法
        setVisibility(GONE); // 设置不可见且不占空间
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_TOPCOUNT); // 发送对话框关闭事件
    }

    /**
     * 设置滑动条进度
     * @param i 进度值
     */
    public void setProgress(int i) { // 设置滑动条进度
        isFromUser = true; // 标记来自用户操作
        seekBar.setProgress(i); // 设置进度
    }

    /**
     * 获取滑动条当前进度
     * @return 进度值
     */
    public int getProgress() { // 获取滑动条进度
        return seekBar.getProgress(); // 返回当前进度
    }

    /**
     * 设置数据并显示对话框
     * @param titleString 标题字符串
     * @param curCount 当前计数值
     * @param maxCount 最大计数值
     * @param onDismissListener 关闭回调监听器
     */
    public void setData(String titleString, int curCount, int maxCount, OnDismissListener onDismissListener) { // 设置数据
        this.onDismissListener = onDismissListener; // 保存回调监听器
        this.curCount = curCount; // 保存当前计数值
        this.maxCount = maxCount; // 保存最大计数值
        title.setText(titleString); // 设置标题
        show.setText(String.valueOf(curCount)); // 显示当前计数值
        isFromUser = false; // 重置用户操作标记
        if (x1.isChecked()) { // 如果x1倍率选中
            seekBar.setMax(curCount < 100 ? 98 : 99); // 设置滑动条最大值
            seekBar.setProgress(curCount < 100 ? curCount - 1 : curCount % 100); // 设置滑动条进度
        } else if (x100.isChecked()) { // 如果x100倍率选中
            seekBar.setMax(maxCount / 100); // 设置滑动条最大值
            seekBar.setProgress(curCount / 100); // 设置滑动条进度
        }
        show(); // 显示对话框
    }

    /**
     * 按钮点击监听器，处理倍率切换和加减操作
     */
    private View.OnClickListener onCheckedListener = new View.OnClickListener() { // 按钮点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按钮音效
            ScreenUtil.getViewLocation(v); // 获取视图位置
            if (x1.isChecked() && x100.getId() == v.getId()) { // 从x1切换到x100
                x100.setChecked(true); // 选中x100
                x1.setChecked(false); // 取消x1
                isFromUser = false; // 重置用户操作标记
                seekBar.setMax(maxCount / 100); // 设置x100模式滑动条最大值
                seekBar.setProgress(curCount / 100); // 设置x100模式滑动条进度
            } else if (x100.isChecked() && x1.getId() == v.getId()) { // 从x100切换到x1
                x1.setChecked(true); // 选中x1
                x100.setChecked(false); // 取消x100
                isFromUser = false; // 重置用户操作标记
                seekBar.setMax(curCount < 100 ? 98 : 99); // 设置x1模式滑动条最大值
                seekBar.setProgress(curCount < 100 ? curCount - 1 : curCount % 100); // 设置x1模式滑动条进度
            } else if (add.getId() == v.getId()) { // 加一按钮
                isFromUser = true; // 标记来自用户操作
                if (x1.isChecked()) { // x1模式下
                    seekBar.setProgress(seekBar.getProgress() + 1); // 进度加一
                } else if (x100.isChecked()) { // x100模式下
                    seekBar.setProgress(seekBar.getProgress() + 1); // 进度加一
                }
            } else if (subtract.getId() == v.getId()) { // 减一按钮
                isFromUser = true; // 标记来自用户操作
                if (x1.isChecked()) { // x1模式下
                    seekBar.setProgress(seekBar.getProgress() - 1); // 进度减一
                } else if (x100.isChecked()) { // x100模式下
                    seekBar.setProgress(seekBar.getProgress() - 1); // 进度减一
                }
            }
        }
    };

    /**
     * 滑动条变化监听器，实时更新计数值
     */
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() { // 滑动条监听器
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { // 进度变化回调
            ScreenUtil.getViewLocation(seekBar); // 获取视图位置
            if (isFromUser || fromUser) { // 如果来自用户操作
                if (x1.isChecked()) { // x1模式下
                    if (curCount < 100) { // 如果当前计数小于100
                        curCount = progress + 1; // 直接用进度+1作为计数值
                    } else { // 如果当前计数大于等于100
                        curCount = curCount / 100 * 100 + progress; // 保留百位以上，加上个位和十位
                    }
                } else if (x100.isChecked()) { // x100模式下
                    curCount = curCount % 100 + progress * 100; // 保留个位和十位，加上百位以上
                }
            }
            if (curCount > maxCount) { // 如果超过最大值
                curCount = maxCount; // 限制为最大值
            }
            if (curCount == 0) { // 如果计数为0
                curCount = 1; // 最小值为1
            }
            show.setText(String.valueOf(curCount)); // 更新显示值

            if (onDismissListener != null) { // 判断回调不为空
                onDismissListener.onDismiss(Integer.parseInt(show.getText().toString())); // 回调当前计数值
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { // 开始触摸滑动条（空实现）

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { // 停止触摸滑动条（空实现）

        }
    };
}
