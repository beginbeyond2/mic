package com.micsig.tbook.tbookscope.top.popwindow.keyboardtext; // 包声明：文本键盘子包

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：文本键盘 - 候选词数据项模型                              │
 * │ 核心职责：定义拼音候选词的索引、文字和选中状态属性                   │
 * │ 架构设计：数据模型类，由TopDialogCandidatesWord使用                 │
 * │ 数据流向：PinyinIME返回候选词 → 创建Item → CandidatesWordAdapter显示│
 * │ 依赖关系：无外部依赖                                               │
 * │ 使用场景：拼音输入法候选词列表的数据载体                            │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/12/29.
 */

public class KeyBoardCandidatesItem { // 候选词数据项
    private int index; // 候选词索引
    private String text; // 候选词文字
    private boolean select; // 是否选中

    /**
     * 构造函数
     * @param index 候选词索引
     * @param text 候选词文字
     * @param select 是否选中
     */
    public KeyBoardCandidatesItem(int index, String text, boolean select) { // 构造函数
        this.index = index; // 设置索引
        this.text = text; // 设置文字
        this.select = select; // 设置选中状态
    }

    /**
     * 获取候选词索引
     * @return 索引值
     */
    public int getIndex() { // 获取索引
        return index; // 返回索引
    }

    /**
     * 设置候选词索引
     * @param index 索引值
     */
    public void setIndex(int index) { // 设置索引
        this.index = index; // 赋值索引
    }

    /**
     * 获取候选词文字
     * @return 文字内容
     */
    public String getText() { // 获取文字
        return text; // 返回文字
    }

    /**
     * 设置候选词文字
     * @param text 文字内容
     */
    public void setText(String text) { // 设置文字
        this.text = text; // 赋值文字
    }

    /**
     * 获取是否选中
     * @return true表示选中
     */
    public boolean isSelect() { // 获取选中状态
        return select; // 返回选中状态
    }

    /**
     * 设置是否选中
     * @param select 选中状态
     */
    public void setSelect(boolean select) { // 设置选中状态
        this.select = select; // 赋值选中状态
    }

    @Override
    public String toString() { // 转字符串方法
        return "KeyBoardCandidatesItem{" + // 返回格式化字符串
                "index=" + index + // 索引
                ", text='" + text + '\'' + // 文字
                ", select=" + select + // 选中状态
                '}'; // 结束
    }
}
