package com.micsig.tbook.ui.rightslipmenu;

import android.content.Context;

import com.micsig.tbook.ui.R;
import com.micsig.tbook.ui.bean.RxMsgSelect;



/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 *                          右侧滑菜单选择项数据模型
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * 【模块定位】
 * 右侧滑菜单选择列表的单项数据模型，封装选择项的索引、文本、选中状态和启用状态，
 * 继承自RxMsgSelect支持响应式消息传递。
 *
 * 【核心职责】
 * 1. 存储选择项索引和文本
 * 2. 管理选中状态
 * 3. 管理启用状态
 * 4. 判断是否为用户自定义项
 *
 * 【架构设计】
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                      RightBeanSelect                            │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  继承层: extends RxMsgSelect                                    │
 * │  数据层: index / text / simpleText                              │
 * │  状态层: check / enable                                         │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * 【数据流向】
 * 创建 → 设置属性 → 传递给Adapter → 显示 → 用户选择 → 更新状态
 *
 * 【依赖关系】
 * ┌──────────────┐     ┌──────────────────────┐
 * │ RxMsgSelect  │────>│ 响应式消息基类       │
 * └──────────────┘     └──────────────────────┘
 * ┌──────────────┐     ┌──────────────────────┐
 * │ R.string     │────>│ 字符串资源           │
 * └──────────────┘     └──────────────────────┘
 *
 * 【使用示例】
 * // 创建选择项
 * RightBeanSelect item = new RightBeanSelect(0, "1V", true);
 *
 * // 判断是否为用户自定义项
 * if (item.isUserDefine(context)) {
 *     // 处理用户自定义逻辑
 * }
 *
 * // 设置选中状态
 * item.setCheck(true);
 *
 * 【注意事项】
 * 1. index=-1表示占位项（不可见）
 * 2. 用户自定义项通过文本匹配R.string.serialsUserDefine判断
 *
 * @author yangj
 * @since 2017/5/3
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class RightBeanSelect extends RxMsgSelect  {
    // ═════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═════════════════════════════════════════════════════════════════════════════

    /** 项索引（-1表示占位项） */
    private int index;

    /** 显示文本 */
    private String text;

    /** 是否选中 */
    private boolean check;

    /** 是否启用 */
    private boolean enable;

    /** 简化文本（可选） */
    private String simpleText;

    // ═════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 构造方法
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 创建选择项数据对象。
     *
     * 【参数说明】
     * @param index 项索引
     * @param text  显示文本
     * @param check 是否选中
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public RightBeanSelect(int index, String text, boolean check) {
        this.index = index;
        this.text = text;
        this.check = check;
        // 默认启用
        this.enable = true;
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // 公共方法
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 判断是否为用户自定义项
     * ═══════════════════════════════════════════════════════════════════════════
     * 【功能说明】
     * 判断当前项是否为用户自定义项，通过文本匹配R.string.serialsUserDefine。
     *
     * 【参数说明】
     * @param context 上下文对象
     *
     * 【返回值】
     * @return true表示是用户自定义项
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public boolean isUserDefine(Context context) {
        return context.getString(R.string.serialsUserDefine).equals(text);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // Getter/Setter方法
    // ═════════════════════════════════════════════════════════════════════════════

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getSimpleText() {
        return simpleText;
    }

    public void setSimpleText(String simpleText) {
        this.simpleText = simpleText;
    }

    // ═════════════════════════════════════════════════════════════════════════════
    // Object方法重写
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * 字符串转换
     * @return 格式化的字符串表示
     */
    @Override
    public String toString() {
        return "RightBeanSelect{" +
                "index=" + index +
                ", text='" + text + '\'' +
                ", check=" + check +
                ", enable=" + enable +
                '}';
    }
}
