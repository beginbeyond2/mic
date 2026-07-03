package com.micsig.tbook.tbookscope.top.popwindow.keyboardtext; // 包声明：文本键盘子包

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：文本键盘 - 按键数据项模型                                │
 * │ 核心职责：定义文本键盘中每个按键的索引、文字和大小属性               │
 * │ 架构设计：数据模型类，包含SpecialKey接口定义特殊按键常量             │
 * │ 数据流向：由initKeyBoardData创建 → KeyBoardTextAdapter使用         │
 * │ 依赖关系：无外部依赖                                               │
 * │ 使用场景：文本键盘的按键布局数据载体                                │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/12/1.
 */

class KeyBoardTextItem { // 文本键盘按键数据项
    /**
     * 特殊按键常量接口
     */
    public interface SpecialKey { // 特殊按键常量接口
        String DELETE = "delete"; // 删除键标识
        String PLACEHOLDER = "placeholder"; // 占位键标识
        String ENTER = "Enter"; // 确认键标识（英文）
        String UPPER = "upper"; // 大写切换键标识
        String HIDE = "hide"; // 隐藏键标识（英文）
        String NUMBER = "123"; // 数字切换键标识
        String SPACE = "space"; // 空格键标识
        String SYMBOL = "!@#"; // 符号切换键标识
        String LANG_ENG = "Eng"; // 英文语言标识
        String ENTER_CN = "确认"; // 确认键标识（中文）
        String HIDE_CN = "隐藏"; // 隐藏键标识（中文）
        String LANG_CN = "中"; // 中文语言标识

        int INDEX_DELETE = 10; // 删除键索引
        int INDEX_PLACEHOLDER = 11; // 占位键索引
        int INDEX_ENTER = 21; // 确认键索引
        int INDEX_UPPER = 22; // 大写切换键索引
        int INDEX_HIDE = 33; // 隐藏键索引
        int INDEX_NUMBER = 34; // 数字切换键索引
        int INDEX_SPACE = 35; // 空格键索引
        int INDEX_SYMBOL = 36; // 符号切换键索引
        int INDEX_LANG = 37; // 语言切换键索引
    }

    private int index; // 按键索引
    private String word;//文字 // 按键文字
    private int size;//大小 // 按键占用格数

    /**
     * 构造函数
     * @param index 按键索引
     * @param word 按键文字
     * @param size 按键占用格数
     */
    public KeyBoardTextItem(int index, String word, int size) { // 构造函数
        this.index = index; // 设置索引
        this.word = word; // 设置文字
        this.size = size; // 设置大小
    }

    /**
     * 获取按键索引
     * @return 按键索引
     */
    public int getIndex() { // 获取索引
        return index; // 返回索引
    }

    /**
     * 设置按键索引
     * @param index 按键索引
     */
    public void setIndex(int index) { // 设置索引
        this.index = index; // 赋值索引
    }

    /**
     * 获取按键文字
     * @return 按键文字
     */
    public String getWord() { // 获取文字
        return word; // 返回文字
    }

    /**
     * 设置按键文字
     * @param word 按键文字
     */
    public void setWord(String word) { // 设置文字
        this.word = word; // 赋值文字
    }

    /**
     * 获取按键占用格数
     * @return 占用格数
     */
    public int getSize() { // 获取大小
        return size; // 返回大小
    }

    /**
     * 设置按键占用格数
     * @param size 占用格数
     */
    public void setSize(int size) { // 设置大小
        this.size = size; // 赋值大小
    }

    @Override
    public String toString() { // 转字符串方法
        return "KeyBoardTextItem{" + // 返回格式化字符串
                "" + index + // 索引
                ", '" + word + '\'' + // 文字
                ", " + size + // 大小
                '}'; // 结束
    }
}
