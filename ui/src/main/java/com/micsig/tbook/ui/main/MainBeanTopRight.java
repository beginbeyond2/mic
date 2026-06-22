package com.micsig.tbook.ui.main;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.wavezone.TChan;


/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 *                           顶部右侧显示项数据模型
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * 【模块定位】
 * 顶部右侧信息栏的数据载体，封装单个显示项的完整信息，包括通道标识、显示文本、
 * 下划线样式、颜色资源等属性，支持克隆操作。
 *
 * 【核心职责】
 * 1. 存储顶部右侧单个显示项的所有属性数据
 * 2. 根据通道号自动分配对应的颜色资源
 * 3. 提供克隆方法用于数据复制
 * 4. 管理显示项的可见性和变更状态
 *
 * 【架构设计】
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    MainBeanTopRight                             │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  属性层: channel / text / line / colorResId / visible          │
 * │  状态层: showNumber / changed / clickStart / clickEnd          │
 * │  操作层: clone() / getter / setter                             │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 【数据流向】
 * 外部创建 → 属性设置 → 传递给TopRightView → 渲染显示 → 用户交互 → 状态更新
 *
 * 【依赖关系】
 * ┌──────────────┐     ┌──────────────────────┐
 * │ TChan        │────>│ 通道常量定义         │
 * └──────────────┘     └──────────────────────┘
 * ┌──────────────┐     ┌──────────────────────┐
 * │ R.color      │────>│ 颜色资源定义         │
 * └──────────────┘     └──────────────────────┘
 *
 * 【使用示例】
 * // 创建通道1的显示项
 * MainBeanTopRight bean = new MainBeanTopRight(TChan.Ch1, "1.00V", LINE_BOTTOM);
 *
 * // 创建自定义颜色的显示项
 * MainBeanTopRight bean2 = new MainBeanTopRight("OFF", LINE_NULL, R.color.textColorGray);
 *
 * // 克隆显示项
 * MainBeanTopRight cloned = bean.clone();
 *
 * 【注意事项】
 * 1. channel字段：1~8表示通道1-8，0表示特殊项（如高低通道的另一个通道项）
 * 2. line字段控制下划线样式：LINE_NULL(无)、LINE_BOTTOM(底部)、LINE_TOP(顶部)
 * 3. 克隆方法会复制所有属性，包括visible和changed状态
 *
 * @author yangj
 * @since 2017/5/31
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class MainBeanTopRight  {
    // ═════════════════════════════════════════════════════════════════════════════
    // 常量定义 - 下划线样式
    // ═════════════════════════════════════════════════════════════════════════════

    /** 无下划线 */
    public static final int LINE_NULL = 0;

    /** 底部下划线 */
    public static final int LINE_BOTTOM = 1;

    /** 顶部下划线 */
    public static final int LINE_TOP = 2;

    // ═════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * 通道标识
     * 1 ~ 8: 通道1-8
     * 0: 第五项，比如：显示高低通道的另一个通道项、Serials的显示项
     */
    private int channel;

    /** 显示文本内容 */
    private String text;

    /** 下划线样式：LINE_NULL / LINE_BOTTOM / LINE_TOP */
    private int line;

    /** 颜色资源ID */
    private int colorResId;

    /** 点击区域起始位置（像素） */
    private int clickStart;

    /** 点击区域结束位置（像素） */
    private int clickEnd;

    /** 是否可见 */
    private boolean visible;

    /** 是否显示编号 */
    private boolean showNumber;

    /** 内容是否已变更 */
    private boolean changed;

    // ═════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法 - 自定义颜色
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 创建一个指定文本、下划线样式和颜色的显示项，通道号默认为0。
     *
     * 【参数说明】
     * @param text      显示的文本内容
     * @param line      下划线样式（LINE_NULL / LINE_BOTTOM / LINE_TOP）
     * @param colorResId 颜色资源ID
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public MainBeanTopRight(String text, int line, int colorResId) {
        // 初始化文本内容
        this.text = text;
        // 设置下划线样式
        this.line = line;
        // 设置颜色资源ID
        this.colorResId = colorResId;
        // 通道号默认为0（特殊项）
        this.channel = 0;
        // 默认显示编号
        showNumber = true;
        // 默认不可见
        visible = false;
        // 默认未变更
        changed = false;
    }


    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法 - 通道模式
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 创建一个指定通道的显示项，颜色根据通道号自动分配。
     *
     * 【参数说明】
     * @param channel 通道号（TChan.Ch1 ~ TChan.Ch8）
     * @param text    显示的文本内容
     * @param line    下划线样式（LINE_NULL / LINE_BOTTOM / LINE_TOP）
     *
     * 【颜色映射】
     * Ch1 -> R.color.colorCh1 (黄色)
     * Ch2 -> R.color.colorCh2 (蓝色)
     * Ch3 -> R.color.colorCh3 (紫色)
     * Ch4 -> R.color.colorCh4 (绿色)
     * Ch5 -> R.color.colorCh5 (橙色)
     * Ch6 -> R.color.colorCh6 (青色)
     * Ch7 -> R.color.colorCh7 (粉色)
     * Ch8 -> R.color.colorCh8 (棕色)
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public MainBeanTopRight(int channel, String text, int line) {
        // 设置通道号
        this.channel = channel;
        // 设置文本内容
        this.text = text;
        // 设置下划线样式
        this.line = line;
        // 默认显示编号
        showNumber = true;
        // 默认不可见
        visible = false;
        // 默认未变更
        changed = false;

        // 根据通道号分配颜色
        switch (channel) {
            case TChan.Ch1:
                // 通道1 - 黄色
                colorResId = R.color.colorCh1;
                break;
            case TChan.Ch2:
                // 通道2 - 蓝色
                colorResId = R.color.colorCh2;
                break;
            case TChan.Ch3:
                // 通道3 - 紫色
                colorResId = R.color.colorCh3;
                break;
            case TChan.Ch4:
                // 通道4 - 绿色
                colorResId = R.color.colorCh4;
                break;
            case TChan.Ch5:
                // 通道5 - 橙色
                colorResId = R.color.colorCh5;
                break;
            case TChan.Ch6:
                // 通道6 - 青色
                colorResId = R.color.colorCh6;
                break;
            case TChan.Ch7:
                // 通道7 - 粉色
                colorResId = R.color.colorCh7;
                break;
            case TChan.Ch8:
                // 通道8 - 棕色
                colorResId = R.color.colorCh8;
                break;
            default:
                // 默认颜色
                colorResId = R.color.colorChCommon;
                break;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 公共方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 克隆方法
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 创建当前对象的完整副本，复制所有属性值。
     *
     * 【返回值】
     * @return 新的MainBeanTopRight对象，包含与原对象相同的属性值
     *
     * 【使用场景】
     * 当需要保留原始数据的同时进行修改时使用
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public MainBeanTopRight clone() {
        // 创建新对象
        MainBeanTopRight m = new MainBeanTopRight(this.getText(), this.line, this.colorResId);
        // 复制文本
        m.text = this.text;
        // 复制下划线样式
        m.line = this.line;
        // 复制颜色资源ID
        m.colorResId = this.colorResId;
        // 复制通道号
        m.channel = this.channel;
        // 复制是否显示编号
        m.showNumber = showNumber;
        // 复制可见性
        m.visible = visible;
        // 复制变更状态
        m.changed = changed;
        // 返回克隆对象
        return m;
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // Getter / Setter 方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * 获取是否显示编号
     * @return true表示显示编号，false表示不显示
     */
    public boolean isShowNumber() {
        return showNumber;
    }

    /**
     * 设置是否显示编号
     * @param showNumber true表示显示编号，false表示不显示
     */
    public void setShowNumber(boolean showNumber) {
        this.showNumber = showNumber;
    }

    /**
     * 获取可见性
     * @return true表示可见，false表示不可见
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * 设置可见性
     * @param visible true表示可见，false表示不可见
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * 获取通道号
     * @return 通道号（1~8表示通道1-8，0表示特殊项）
     */
    public int getChannel() {
        return channel;
    }

    /**
     * 设置通道号
     * @param channel 通道号
     */
    public void setChannel(int channel) {
        this.channel = channel;
    }

    /**
     * 获取显示文本
     * @return 文本内容
     */
    public String getText() {
        return text;
    }

    /**
     * 设置显示文本
     * @param text 文本内容
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * 获取下划线样式
     * @return 下划线样式（LINE_NULL / LINE_BOTTOM / LINE_TOP）
     */
    public int getLine() {
        return line;
    }

    /**
     * 设置下划线样式
     * @param line 下划线样式
     */
    public void setLine(int line) {
        this.line = line;
    }

    /**
     * 获取颜色资源ID
     * @return 颜色资源ID
     */
    public int getColorResId() {
        return colorResId;
    }

    /**
     * 设置颜色资源ID
     * @param colorResId 颜色资源ID
     */
    public void setColorResId(int colorResId) {
        this.colorResId = colorResId;
    }

    /**
     * 获取点击区域起始位置
     * @return 点击区域起始位置（像素）
     */
    public int getClickStart() {
        return clickStart;
    }

    /**
     * 设置点击区域起始位置
     * @param clickStart 点击区域起始位置（像素）
     */
    public void setClickStart(int clickStart) {
        this.clickStart = clickStart;
    }

    /**
     * 获取点击区域结束位置
     * @return 点击区域结束位置（像素）
     */
    public int getClickEnd() {
        return clickEnd;
    }

    /**
     * 设置点击区域结束位置
     * @param clickEnd 点击区域结束位置（像素）
     */
    public void setClickEnd(int clickEnd) {
        this.clickEnd = clickEnd;
    }

    /**
     * 获取变更状态
     * @return true表示已变更，false表示未变更
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * 设置变更状态
     * @param changed true表示已变更，false表示未变更
     */
    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // Object方法重写
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 字符串转换
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 返回对象的字符串表示，包含所有属性信息，便于调试和日志输出。
     *
     * 【返回值】
     * @return 格式化的字符串表示
     * ═══════════════════════════════════════════════════════════════════════════
     */
    @Override
    public String toString() {
        return "MainBeanTopRight{" +
                "channel=" + channel +
                ", text='" + text + '\'' +
                ", line=" + line +
                ", colorResId=" + colorResId +
                ", clickStart=" + clickStart +
                ", clickEnd=" + clickEnd +
                ", visible=" + visible +
                ", showNumber=" + showNumber +
                ", changed=" + changed +
                '}';
    }
}
