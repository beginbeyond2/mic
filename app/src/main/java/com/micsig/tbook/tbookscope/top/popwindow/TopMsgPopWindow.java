package com.micsig.tbook.tbookscope.top.popwindow; // 包声明：顶部弹出窗口模块

import com.micsig.tbook.ui.bean.RxBooleanWithSelect; // 导入带选择状态的布尔包装类

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：顶部弹出窗口 - 消息弹出窗口数据模型                      │
 * │ 核心职责：封装YT模式与SerialWord模式的选中状态及索引信息            │
 * │ 架构设计：纯数据模型类，配合TopLayoutPopWindow使用                │
 * │ 数据流向：TopLayoutPopWindow → TopMsgPopWindow → RxBus广播       │
 * │ 依赖关系：RxBooleanWithSelect                                    │
 * │ 使用场景：工作模式切换时传递当前菜单索引和模式状态                  │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2018/8/21.
 */

public class TopMsgPopWindow { // 顶部消息弹出窗口数据模型类
    private int checkIndex; // 当前选中的菜单项索引
    private RxBooleanWithSelect ytMode = new RxBooleanWithSelect(true);//是否是yt模式 // YT模式开关状态，默认开启
    private RxBooleanWithSelect serialWord = new RxBooleanWithSelect(false);//是否是serial word模式 // SerialWord模式开关状态，默认关闭

    /**
     * 构造函数：初始化消息弹出窗口
     * @param checkIndex 当前选中的菜单项索引
     * @param ytMode 是否为YT模式
     * @param serialWord 是否为SerialWord模式
     */
    public TopMsgPopWindow(int checkIndex, boolean ytMode, boolean serialWord) { // 构造函数
        this.checkIndex = checkIndex; // 设置选中索引
        this.ytMode = new RxBooleanWithSelect(ytMode); // 创建YT模式布尔包装对象
        this.serialWord = new RxBooleanWithSelect(serialWord); // 创建SerialWord模式布尔包装对象
    }

    /**
     * 获取当前选中的菜单项索引
     * @return 选中索引值
     */
    public int getCheckIndex() { // 获取选中索引
        return checkIndex; // 返回选中索引
    }

    /**
     * 设置当前选中的菜单项索引
     * @param checkIndex 要设置的索引值
     */
    public void setCheckIndex(int checkIndex) { // 设置选中索引
        this.checkIndex = checkIndex; // 赋值选中索引
    }

    /**
     * 获取YT模式的布尔包装对象
     * @return YT模式的RxBooleanWithSelect对象
     */
    public RxBooleanWithSelect getYtMode() { // 获取YT模式状态
        return ytMode; // 返回YT模式包装对象
    }

    /**
     * 设置YT模式状态，值变化时自动更新选中标记
     * @param ytMode 是否为YT模式
     */
    public void setYtMode(boolean ytMode) { // 设置YT模式状态
        if (this.ytMode.isValue() != ytMode) { // 判断新值与旧值是否不同
            this.ytMode.setValue(ytMode); // 更新YT模式值
            setAllUnSelect(); // 清除所有选中标记
            this.ytMode.setRxMsgSelect(true); // 将YT模式标记为选中
        }
    }

    /**
     * 获取SerialWord模式的布尔包装对象
     * @return SerialWord模式的RxBooleanWithSelect对象
     */
    public RxBooleanWithSelect getSerialWord() { // 获取SerialWord模式状态
        return serialWord; // 返回SerialWord模式包装对象
    }

    /**
     * 设置SerialWord模式状态，值变化时自动更新选中标记
     * @param serialWord 是否为SerialWord模式
     */
    public void setSerialWord(boolean serialWord) { // 设置SerialWord模式状态
        if (this.serialWord.isValue() != serialWord) { // 判断新值与旧值是否不同
            this.serialWord.setValue(serialWord); // 更新SerialWord模式值
            setAllUnSelect(); // 清除所有选中标记
            this.serialWord.setRxMsgSelect(true); // 将SerialWord模式标记为选中
        }
    }

    /**
     * 清除所有模式的选中标记（内部方法）
     */
    private void setAllUnSelect() { // 清除所有选中标记
        ytMode.setRxMsgSelect(false); // 取消YT模式选中标记
        serialWord.setRxMsgSelect(false); // 取消SerialWord模式选中标记
    }
}
