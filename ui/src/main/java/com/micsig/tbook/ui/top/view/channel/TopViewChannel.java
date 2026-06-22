package com.micsig.tbook.ui.top.view.channel; // 通道选择视图组件包 //

import android.annotation.SuppressLint; // Android注解：抑制资源类型检查警告 //
import android.content.Context; // Android上下文对象，用于访问资源和系统服务 //
import android.graphics.drawable.Drawable; // Android可绘制对象，用于设置按钮图标 //
import android.util.TypedValue; // Android类型值转换工具，用于尺寸单位转换 //
import android.view.Gravity; // Android布局重力常量，用于设置内容对齐方式 //
import android.view.View; // Android视图基类，所有UI组件的父类 //
import android.widget.LinearLayout; // Android线性布局，用于设置RadioButton布局参数 //
import android.widget.RadioButton; // Android单选按钮组件 //
import android.widget.RadioGroup; // Android单选按钮组容器 //

import com.micsig.tbook.ui.R; // 应用资源文件自动生成的R类 //
import com.micsig.tbook.ui.util.svg.SelectorUtil; // SVG选择器工具类，用于创建通道图标 //
import com.micsig.tbook.ui.util.svg.SvgNodeInfo; // SVG节点信息类，用于获取颜色配置 //
import com.micsig.tbook.ui.wavezone.TChan; // 通道工具类，用于通道索引转换 //

/*
 * ╔════════════════════════════════════════════════════════════════════════════════════════════════╗
 * ║                                    TopViewChannel.java                                         ║
 * ╠════════════════════════════════════════════════════════════════════════════════════════════════╣
 * ║  模块定位: 示波器顶部通道选择视图控制器                                                          ║
 * ║  核心职责: 管理示波器通道（CH/MATH/REF）的单选按钮组，提供通道切换和状态管理功能                    ║
 * ╠════════════════════════════════════════════════════════════════════════════════════════════════╣
 * ║  架构设计:                                                                                      ║
 * ║  ┌─────────────────────────────────────────────────────────────────────────────────────────┐   ║
 * ║  │                              TopViewChannel                                             │   ║
 * ║  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐              │   ║
 * ║  │  │   CH1-CH8   │    │  MATH1-8    │    │   REF1-8    │    │ RadioGroup  │              │   ║
 * ║  │  │  物理通道   │    │  数学通道   │    │  参考通道   │    │  单选按钮组 │              │   ║
 * ║  │  └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘              │   ║
 * ║  │           │                  │                  │                  │                    │   ║
 * ║  │           └──────────────────┴──────────────────┴──────────────────┘                    │   ║
 * ║  │                                      │                                                  │   ║
 * ║  │                                      ▼                                                  │   ║
 * ║  │                        ┌─────────────────────────┐                                      │   ║
 * ║  │                        │   onItemClickListener   │                                      │   ║
 * ║  │                        │     通道切换回调接口    │                                      │   ║
 * ║  │                        └─────────────────────────┘                                      │   ║
 * ║  └─────────────────────────────────────────────────────────────────────────────────────────┘   ║
 * ╠════════════════════════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向:                                                                                      ║
 * ║  1. 外部调用setData() → 加载通道名称和颜色配置                                                   ║
 * ║  2. updateView() → 创建RadioButton并添加到RadioGroup                                            ║
 * ║  3. 用户点击 → checkedChangeListener触发 → 回调onItemClickListener                              ║
 * ║  4. 外部调用setChecked() → 程序化选中指定通道                                                    ║
 * ╠════════════════════════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系:                                                                                      ║
 * ║  - SelectorUtil: 创建通道选中状态的图标                                                         ║
 * ║  - SvgNodeInfo: 获取通道颜色配置                                                                ║
 * ║  - TChan: 通道索引转换工具（UI索引 ↔ FPGA索引）                                                  ║
 * ║  - R.layout.view_channel: 通道视图布局文件                                                      ║
 * ╠════════════════════════════════════════════════════════════════════════════════════════════════╣
 * ║  使用示例:                                                                                      ║
 * ║  TopViewChannel channelView = new TopViewChannel(context);                                     ║
 * ║  channelView.setData(R.array.channel_names, R.array.channel_colors, listener);                 ║
 * ║  container.addView(channelView.getInflate());                                                  ║
 * ║  channelView.setChecked(TopViewChannel.CH1); // 选中通道1                                      ║
 * ╠════════════════════════════════════════════════════════════════════════════════════════════════╣
 * ║  版本历史:                                                                                      ║
 * ║  - v1.0.0 (2017/04/05): 初始版本，创建基础通道选择功能                                          ║
 * ║  - v1.1.0: 添加只读模式支持                                                                     ║
 * ║  - v1.2.0: 添加通道可见性控制功能                                                               ║
 * ╚════════════════════════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 示波器顶部通道选择视图控制器
 * <p>
 * 该类负责管理示波器的通道选择UI组件，支持24个通道（8个物理通道CH、8个数学通道MATH、8个参考通道REF）。
 * 通过RadioGroup实现单选功能，每个通道以RadioButton形式展示，带有颜色标识和选中状态图标。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>通道初始化和视图构建</li>
 *   <li>通道选中状态管理</li>
 *   <li>通道可见性控制</li>
 *   <li>通道颜色设置</li>
 *   <li>只读模式支持</li>
 * </ul>
 * </p>
 *
 * @author Administrator
 * @version 1.2.0
 * @since 2017/04/05
 */
public class TopViewChannel {

    // ================================ 通道索引常量定义 ================================
    // 物理通道索引常量 (CH1-CH8): 对应示波器的实际输入通道 //

    /** 物理通道1索引常量，值为0 */
    public static final int CH1 = 0; // 物理通道1，对应示波器第一个输入端口 //

    /** 物理通道2索引常量，值为1 */
    public static final int CH2 = CH1 + 1; // 物理通道2，对应示波器第二个输入端口 //

    /** 物理通道3索引常量，值为2 */
    public static final int CH3 = CH2 + 1; // 物理通道3，对应示波器第三个输入端口 //

    /** 物理通道4索引常量，值为3 */
    public static final int CH4 = CH3 + 1; // 物理通道4，对应示波器第四个输入端口 //

    /** 物理通道5索引常量，值为4 */
    public static final int CH5 = CH4 + 1; // 物理通道5，对应示波器第五个输入端口 //

    /** 物理通道6索引常量，值为5 */
    public static final int CH6 = CH5 + 1; // 物理通道6，对应示波器第六个输入端口 //

    /** 物理通道7索引常量，值为6 */
    public static final int CH7 = CH6 + 1; // 物理通道7，对应示波器第七个输入端口 //

    /** 物理通道8索引常量，值为7 */
    public static final int CH8 = CH7 + 1; // 物理通道8，对应示波器第八个输入端口 //

    // 数学通道索引常量 (MATH1-MATH8): 对应示波器的数学运算通道 //

    /** 数学通道1索引常量，值为8 */
    public static final int MATH1 = CH8 + 1; // 数学通道1，用于数学运算结果显示 //

    /** 数学通道2索引常量，值为9 */
    public static final int MATH2 = MATH1 + 1; // 数学通道2，用于数学运算结果显示 //

    /** 数学通道3索引常量，值为10 */
    public static final int MATH3 = MATH2 + 1; // 数学通道3，用于数学运算结果显示 //

    /** 数学通道4索引常量，值为11 */
    public static final int MATH4 = MATH3 + 1; // 数学通道4，用于数学运算结果显示 //

    /** 数学通道5索引常量，值为12 */
    public static final int MATH5 = MATH4 + 1; // 数学通道5，用于数学运算结果显示 //

    /** 数学通道6索引常量，值为13 */
    public static final int MATH6 = MATH5 + 1; // 数学通道6，用于数学运算结果显示 //

    /** 数学通道7索引常量，值为14 */
    public static final int MATH7 = MATH6 + 1; // 数学通道7，用于数学运算结果显示 //

    /** 数学通道8索引常量，值为15 */
    public static final int MATH8 = MATH7 + 1; // 数学通道8，用于数学运算结果显示 //

    // 参考通道索引常量 (REF1-REF8): 对应示波器的参考波形通道 //

    /** 参考通道1索引常量，值为16 */
    public static final int REF1 = MATH8 + 1; // 参考通道1，用于存储和显示参考波形 //

    /** 参考通道2索引常量，值为17 */
    public static final int REF2 = REF1 + 1; // 参考通道2，用于存储和显示参考波形 //

    /** 参考通道3索引常量，值为18 */
    public static final int REF3 = REF2 + 1; // 参考通道3，用于存储和显示参考波形 //

    /** 参考通道4索引常量，值为19 */
    public static final int REF4 = REF3 + 1; // 参考通道4，用于存储和显示参考波形 //

    /** 参考通道5索引常量，值为20 */
    public static final int REF5 = REF4 + 1; // 参考通道5，用于存储和显示参考波形 //

    /** 参考通道6索引常量，值为21 */
    public static final int REF6 = REF5 + 1; // 参考通道6，用于存储和显示参考波形 //

    /** 参考通道7索引常量，值为22 */
    public static final int REF7 = REF6 + 1; // 参考通道7，用于存储和显示参考波形 //

    /** 参考通道8索引常量，值为23 */
    public static final int REF8 = REF7 + 1; // 参考通道8，用于存储和显示参考波形 //

    // ================================ 成员变量定义 ================================

    /** Android上下文对象，用于访问资源、启动Activity等系统级操作 */
    private Context context; // 应用上下文，在构造函数中初始化 //

    /** 视图容器，通过inflate加载的布局根视图，包含RadioGroup */
    private View inflate; // 加载的布局视图，通过R.layout.view_channel加载 //

    /** 通道名称字符串数组，从资源文件加载，用于显示RadioButton的文字 */
    private String[] arrayString; // 通道显示名称数组，如{"CH1", "CH2", "MATH1"...} //

    /** 通道颜色整型数组，用于设置RadioButton的文字颜色，与通道一一对应 */
    private int[] arrayColor; // 通道颜色数组，每个通道对应一个颜色值 //

    /** 单选按钮组容器，包含所有通道的RadioButton，实现单选逻辑 */
    private RadioGroup radioGroup; // RadioGroup容器，管理所有通道RadioButton //

    /** 通道选中状态变化监听器，当用户切换通道时回调 */
    private onItemClickListener changeListener; // 选中状态变化回调接口 //

    // ================================ 接口定义 ================================

    /**
     * 通道选中状态变化监听接口
     * <p>
     * 当用户点击切换通道时，通过此接口回调通知外部调用者。
     * 外部类实现此接口以响应通道切换事件。
     * </p>
     */
    public interface onItemClickListener {
        /**
         * 通道选中状态变化回调方法
         *
         * @param viewId       触发变化的视图ID，即inflate视图的ID
         * @param checkedIndex 新选中的通道索引（0-23），对应CH1-REF8
         * @param radio        被选中的RadioButton实例，可用于进一步操作
         */
        void checkChanged(int viewId, int checkedIndex, RadioButton radio); // 选中状态变化回调方法 //
    }

    // ================================ 构造函数 ================================

    /**
     * 构造函数：初始化TopViewChannel实例
     * <p>
     * 创建通道选择视图控制器，初始化上下文并加载布局视图。
     * </p>
     *
     * @param context Android上下文对象，不能为null，用于资源访问和视图创建
     */
    public TopViewChannel(Context context) { // 构造函数，接收上下文参数 //
        this.context = context; // 保存上下文引用 //
        initView(); // 初始化视图，加载布局文件 //
    }

    // ================================ 公共方法 ================================

    /**
     * 设置通道数据并初始化视图
     * <p>
     * 加载通道名称数组和颜色配置，创建RadioButton并添加到RadioGroup。
     * 此方法应在构造后首先调用，用于初始化通道选择器的内容。
     * </p>
     *
     * @param arrayResId      通道名称字符串数组资源ID，如R.array.channel_names
     * @param arrayColorResId 通道颜色数组资源ID（当前未使用，颜色从SvgNodeInfo获取）
     * @param changeListener  通道选中状态变化监听器，可为null
     */
    public void setData(int arrayResId, int arrayColorResId, onItemClickListener changeListener) { // 设置数据方法 //
        this.arrayString = context.getResources().getStringArray(arrayResId); // 从资源加载通道名称数组 //
        arrayColor = SvgNodeInfo.getColorsIntForView(); // 从SvgNodeInfo获取通道颜色数组 //
        this.changeListener = changeListener; // 保存监听器引用 //
        updateView(context); // 更新视图，创建RadioButton //
    }

    /**
     * 设置通道选中状态变化监听器
     * <p>
     * 允许在setData之后动态更换监听器。
     * </p>
     *
     * @param changeListener 新的监听器实例，可为null以移除监听
     */
    public void setChangeListener(onItemClickListener changeListener) { // 设置监听器方法 //
        this.changeListener = changeListener; // 更新监听器引用 //
    }

    /**
     * 根据索引获取指定位置的RadioButton
     * <p>
     * 返回RadioGroup中指定索引位置的RadioButton实例。
     * 索引顺序与添加顺序一致，即0对应第一个添加的通道。
     * </p>
     *
     * @param index RadioButton的索引位置，范围[0, radioGroup.getChildCount()-1]
     * @return 对应索引的RadioButton实例，索引越界时返回null
     */
    public RadioButton getShowViewIndex(int index) { // 获取指定索引的RadioButton //
        if (index >= 0 && index < radioGroup.getChildCount()) { // 检查索引是否在有效范围内 //
            return (RadioButton) radioGroup.getChildAt(index); // 返回指定索引的RadioButton //
        } else { // 索引越界 //
            return null; // 返回null表示无效索引 //
        }
    }

    /**
     * 获取当前选中的RadioButton实例
     * <p>
     * 遍历RadioGroup查找当前被选中的RadioButton。
     * 通过比较RadioButton的ID与RadioGroup的选中ID来确定选中项。
     * </p>
     *
     * @return 当前选中的RadioButton实例，无选中项时返回null
     */
    public RadioButton getSelectedRadioButton() { // 获取选中的RadioButton //
        for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton //
            if (radioGroup.getChildAt(i).getId() == radioGroup.getCheckedRadioButtonId()) { // 检查是否为选中项 //
                return ((RadioButton) radioGroup.getChildAt(i)); // 返回选中的RadioButton //
            }
        }
        return null; // 无选中项，返回null //
    }

    /**
     * 获取当前选中通道的索引
     * <p>
     * 返回当前选中RadioButton在RadioGroup中的位置索引。
     * 索引从0开始，对应通道顺序。
     * </p>
     *
     * @return 当前选中通道的索引（0-23），无选中项时返回-1
     */
    public int getSelectedIndex() { // 获取选中索引 //
        for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton //
            if (radioGroup.getChildAt(i).getId() == radioGroup.getCheckedRadioButtonId()) { // 检查是否为选中项 //
                return i; // 返回选中项的索引 //
            }
        }
        return -1; // 无选中项，返回-1 //
    }

    /**
     * 设置指定索引的通道为选中状态
     * <p>
     * 程序化选中指定索引的RadioButton，触发选中状态变化。
     * 会触发OnCheckedChangeListener回调。
     * </p>
     *
     * @param checkedIndex 要选中的通道索引，范围[0, radioGroup.getChildCount()-1]
     */
    public void setChecked(int checkedIndex) { // 设置选中状态 //
        for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton //
            if (i == checkedIndex) { // 找到目标索引 //
                radioGroup.check(radioGroup.getChildAt(i).getId()); // 设置该RadioButton为选中状态 //
            }
        }
    }

    /**
     * 获取加载的视图容器
     * <p>
     * 返回通过inflate加载的根视图，用于添加到父布局中。
     * </p>
     *
     * @return 视图容器实例，包含RadioGroup及其子视图
     */
    public View getInflate() { // 获取视图容器 //
        return inflate; // 返回加载的布局视图 //
    }

    /**
     * 设置通道可见性（正向选择模式）
     * <p>
     * 根据可见性数组设置各通道的显示/隐藏状态。
     * 当当前选中的通道被隐藏时，自动选中第一个可见通道（从前往后查找）。
     * 在切换选中项时临时禁用监听器，避免触发不必要的回调。
     * </p>
     *
     * @param visible 可见性布尔数组，true表示显示，false表示隐藏，数组长度应与通道数一致
     */
    private void setItemVisible_forwardSelected(boolean[] visible) { // 设置可见性（正向选择） //
        boolean changedSelect = false; // 标记是否需要切换选中项 //
        for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton //
            radioGroup.getChildAt(i).setVisibility(visible[i] ? View.VISIBLE : View.GONE); // 根据数组设置可见性 //
            if (((RadioButton) radioGroup.getChildAt(i)).isChecked() && !visible[i]) { // 当前选中项被隐藏 //
                changedSelect = true; // 标记需要切换选中项 //
            }
        }
        if (changedSelect) { // 需要切换选中项 //
            for (int i = 0; i < radioGroup.getChildCount(); i++) { // 从前往后遍历 //
                if (radioGroup.getChildAt(i).getVisibility() == View.VISIBLE) { // 找到第一个可见项 //
                    onItemClickListener itemClickListener = changeListener; // 临时保存监听器 //
                    changeListener = null; // 临时禁用监听器，避免触发回调 //
                    radioGroup.check(radioGroup.getChildAt(i).getId()); // 选中第一个可见项 //
                    changeListener = itemClickListener; // 恢复监听器 //
                    break; // 找到后退出循环 //
                }
            }
        }
    }

    /**
     * 设置通道可见性（反向选择模式）
     * <p>
     * 根据可见性数组设置各通道的显示/隐藏状态。
     * 当当前选中的通道被隐藏时，自动选中最后一个可见通道（从后往前查找）。
     * 在切换选中项时临时禁用监听器，避免触发不必要的回调。
     * </p>
     *
     * @param visible 可见性布尔数组，true表示显示，false表示隐藏，数组长度应与通道数一致
     */
    private void setItemVisible_reverseSelected(boolean[] visible) { // 设置可见性（反向选择） //
        boolean changedSelect = false; // 标记是否需要切换选中项 //
        for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton //
            radioGroup.getChildAt(i).setVisibility(visible[i] ? View.VISIBLE : View.GONE); // 根据数组设置可见性 //
            if (((RadioButton) radioGroup.getChildAt(i)).isChecked() && !visible[i]) { // 当前选中项被隐藏 //
                changedSelect = true; // 标记需要切换选中项 //
            }
        }
        if (changedSelect) { // 需要切换选中项 //
            for (int i = radioGroup.getChildCount() - 1; i >= 0; i--) { // 从后往前遍历 //
                if (radioGroup.getChildAt(i).getVisibility() == View.VISIBLE) { // 找到最后一个可见项 //
                    onItemClickListener itemClickListener = changeListener; // 临时保存监听器 //
                    changeListener = null; // 临时禁用监听器，避免触发回调 //
                    radioGroup.check(radioGroup.getChildAt(i).getId()); // 选中最后一个可见项 //
                    changeListener = itemClickListener; // 恢复监听器 //
                    break; // 找到后退出循环 //
                }
            }
        }
    }

    /**
     * 设置通道可见性
     * <p>
     * 根据可见性数组设置各通道的显示/隐藏状态，并指定自动选择模式。
     * 当当前选中的通道被隐藏时，根据isForwardSelected参数决定选择方向。
     * </p>
     *
     * @param visible          可见性布尔数组，true表示显示，false表示隐藏
     * @param isForwardSelected true: 正向选择（选中第一个可见项）；false: 反向选择（选中最后一个可见项）
     */
    public void setItemVisible(boolean[] visible, boolean isForwardSelected) { // 设置通道可见性 //
        if (isForwardSelected) { // 正向选择模式 //
            setItemVisible_forwardSelected(visible); // 调用正向选择方法 //
        } else { // 反向选择模式 //
            setItemVisible_reverseSelected(visible); // 调用反向选择方法 //
        }
    }

    /**
     * 设置指定通道的颜色
     * <p>
     * 更新指定通道的文字颜色和图标颜色。
     * 通过TChan工具类将通道索引转换为视图索引，然后更新对应的RadioButton。
     * </p>
     *
     * @param chIndex  通道索引（FPGA通道编号）
     * @param colorStr 颜色字符串（当前未使用，颜色从SvgNodeInfo获取）
     */
    public void setChannelColor(int chIndex, String colorStr) { // 设置通道颜色 //
        int viewIndex = TChan.toFpgaChNo(chIndex); // 将通道索引转换为视图索引 //
        RadioButton radioButton = getShowViewIndex(viewIndex); // 获取对应的RadioButton //
        if (radioButton == null) return; // RadioButton不存在，直接返回 //
        arrayColor = SvgNodeInfo.getColorsIntForView(); // 重新获取颜色数组 //
        radioButton.setTextColor(arrayColor[viewIndex]); // 设置文字颜色 //
        setBtnDrawable(radioButton, viewIndex); // 更新图标 //
    }

    /**
     * 获取当前选中的通道索引
     * <p>
     * 返回当前选中RadioButton在RadioGroup中的位置。
     * 注意：返回值范围是0到通道总数，可能等于通道总数（未找到匹配项）。
     * </p>
     *
     * @return 当前选中的通道索引（0-通道总数），未找到时返回通道总数
     * @deprecated 建议使用 {@link #getSelectedIndex()} 方法，该方法在未找到时返回-1更合理
     */
    public int getSelectChannel() { // 获取选中通道索引 //
        int i = 0; // 初始化循环变量 //
        for (i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton //
            if (radioGroup.getChildAt(i).getId() == radioGroup.getCheckedRadioButtonId()) { // 检查是否为选中项 //
                break; // 找到选中项，退出循环 //
            }
        }
        return i; // 返回索引，可能等于通道总数 //
    }

    /**
     * 获取RadioGroup实例
     * <p>
     * 返回内部的RadioGroup对象，用于外部直接操作。
     * </p>
     *
     * @return RadioGroup实例
     */
    public RadioGroup getRadioGroup() { // 获取RadioGroup //
        return radioGroup; // 返回RadioGroup实例 //
    }

    // ================================ 私有方法 ================================

    /**
     * 初始化视图
     * <p>
     * 从布局文件加载视图，但不创建RadioButton。
     * RadioButton的创建在updateView方法中完成。
     * </p>
     */
    private void initView() { // 初始化视图 //
        inflate = View.inflate(context, R.layout.view_channel, null); // 加载布局文件 //
    }

    /**
     * 更新视图内容
     * <p>
     * 根据arrayString数组创建RadioButton并添加到RadioGroup。
     * 设置RadioButton的样式、颜色、图标等属性。
     * 默认选中第一个通道。
     * </p>
     *
     * @param context Android上下文对象
     */
    private void updateView(Context context) { // 更新视图 //
        radioGroup = (RadioGroup) inflate.findViewById(R.id.radioGroupMeasure); // 获取RadioGroup //
        radioGroup.setPadding(10, 0, 0, 1); // 设置内边距 //
        for (int i = radioGroup.getChildCount() - 1; i >= 0; i--) { // 从后往前移除所有子视图 //
            radioGroup.removeView(radioGroup.getChildAt(i)); // 移除子视图 //
        }
        for (int i = 0; i < arrayString.length; i++) { // 遍历通道名称数组 //
            RadioButton radioButton = new RadioButton(context); // 创建RadioButton //
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 90); // 创建布局参数，高度90像素 //
            layoutParams.setMarginEnd(30); // 设置右边距30像素 //
            int r = 30; // 右内边距 //
            int tb = 0; // 上下内边距 //
            radioButton.setPadding(0, tb, r, tb); // 设置内边距 //
            radioButton.setLayoutParams(layoutParams); // 应用布局参数 //
            radioButton.setGravity(Gravity.CENTER); // 设置文字居中 //
            radioButton.setText(arrayString[i]); // 设置显示文字 //
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20); // 设置文字大小20像素 //
            radioButton.setTextColor(arrayColor[i]); // 设置文字颜色 //
            radioButton.setBackground(null); // 清除背景 //
            radioButton.setButtonDrawable(null); // 清除默认单选按钮图标 //
            setBtnDrawable(radioButton, i); // 设置自定义通道图标 //
            radioButton.setCompoundDrawablePadding(8); // 设置图标与文字间距 //
            radioGroup.addView(radioButton); // 添加到RadioGroup //
            if (i == 0) { // 第一个通道 //
                radioGroup.check(radioButton.getId()); // 默认选中第一个 //
            }
        }
        radioGroup.setOnCheckedChangeListener(checkedChangeListener); // 设置选中状态变化监听器 //
    }

    /**
     * 设置RadioButton的通道图标
     * <p>
     * 使用SelectorUtil创建通道选中状态的图标，显示在RadioButton左侧。
     * 图标大小为21x21像素。
     * </p>
     *
     * @param radioButton 目标RadioButton实例
     * @param index       通道索引，用于确定图标颜色
     */
    private void setBtnDrawable(RadioButton radioButton, int index) { // 设置按钮图标 //
        Drawable drawable = SelectorUtil.createCheckedDrawable(TChan.toUiChNo(index)); // 创建通道图标 //
        drawable.setBounds(0, 0, 21, 21); // 设置图标边界 //
        radioButton.setCompoundDrawables(drawable, null, null, null); // 设置左侧图标 //
    }

    // ================================ 内部监听器 ================================

    /**
     * RadioGroup选中状态变化监听器
     * <p>
     * 当用户点击切换通道时触发，遍历找到新选中的RadioButton并回调监听器。
     * </p>
     */
    private RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener() { // 选中状态变化监听器 //
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) { // 选中状态变化回调 //
            for (int i = 0; i < group.getChildCount(); i++) { // 遍历所有RadioButton //
                RadioButton radioButton = (RadioButton) group.getChildAt(i); // 获取RadioButton //
                if (checkedId == radioButton.getId()) { // 检查是否为选中项 //
                    if (changeListener != null) { // 监听器不为空 //
                        changeListener.checkChanged(inflate.getId(), i, radioButton); // 回调监听器 //
                    }
                }
            }
        }
    };

    /**
     * 设置只读模式
     * <p>
     * 控制通道选择器的可交互状态。在只读模式下：
     * <ul>
     *   <li>未选中的通道：禁用并变灰</li>
     *   <li>选中的通道：保持高亮但不可点击</li>
     * </ul>
     * </p>
     *
     * @param enabled true: 启用正常模式，可自由切换通道；false: 启用只读模式，仅显示当前选中通道
     */
    @SuppressLint("ResourceType") // 抑制资源类型检查警告 //
    public void setReadOnly(boolean enabled) { // 设置只读模式 //
        for (int i = 0; i < radioGroup.getChildCount(); i++) { // 遍历所有RadioButton //
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i); // 获取RadioButton //
            if (!enabled) { // 只读模式 //
                if (!radioButton.isChecked()) { // 未选中的通道 //
                    radioButton.setTextColor(context.getResources().getColor(R.color.textColorNewTopViewDisable)); // 设置禁用颜色 //
                    radioButton.setEnabled(false); // 禁用RadioButton //
                } else { // 选中的通道 //
                    radioButton.setTextColor(context.getResources().getColorStateList(R.drawable.selector_text_color)); // 设置选中状态颜色 //
                    radioButton.setClickable(false); // 禁止点击 //
                }
            } else { // 正常模式 //
                radioButton.setTextColor(context.getResources().getColorStateList(R.drawable.selector_text_color)); // 恢复正常颜色 //
                radioButton.setEnabled(true); // 启用RadioButton //
                radioButton.setClickable(true); // 允许点击 //
            }
        }
    }
}
