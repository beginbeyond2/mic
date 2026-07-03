package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包

import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava枚举定义

import java.util.Objects; // 对象工具类

/**
 * Created by yangj on 2018/1/19.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                          Command_Userset                                    |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 示波器用户设置命令处理模块                                         |
 * | 核心职责: 处理SCPI用户设置相关指令，包括存储深度、出厂重置、自校准、自动零点、 |
 * |          用户设置名称管理、设置保存/恢复等                                   |
 * | 架构设计: 命令模式，作为Command子模块，接收SCPI指令并通过RxBus通知UI层更新    |
 * | 数据流向: SCPI指令 → 本类(设置/查询) → RxBus → UI层                        |
 * | 依赖关系: Command, CommandMsgToUI, RxBus, RxEnum                            |
 * | 使用场景: 远程控制用户设置、保存/恢复配置时使用                              |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Userset {
    private int length; // 存储深度/长度设置
    private String names[] = new String[]{"", "", "", "", "", "", "", "", "", ""}; // 用户设置名称数组

    /**
     * 获取存储深度
     *
     * @return 存储深度值
     */
    public int getLength() {
        return length; // 返回存储深度
    }

    /**
     * 设置存储深度
     *
     * @param length      存储深度值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void setLength(int length, boolean isUpdateUI) {
        if (this.length == length) return; // 值未变化则直接返回
        this.length = length; // 保存存储深度
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_LENGTH); // 设置消息标志为存储深度
            msgToUI.setParam(String.valueOf(length)); // 设置存储深度参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 执行出厂重置
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void setFactoryReset(boolean isUpdateUI) {
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_FACTORYRESET); // 设置消息标志为出厂重置
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 执行自校准
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void setSelfAdjust(boolean isUpdateUI) {
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_SELFADJUST); // 设置消息标志为自校准
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 执行自动零点校准
     *
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void setAutoZero(boolean isUpdateUI) {
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_AutoZero); // 设置消息标志为自动零点
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }


    /**
     * 获取指定索引的用户设置名称
     *
     * @param index 设置索引
     * @return 设置名称字符串
     */
    public String getName(int index) {
        return names[index]; // 返回指定索引的名称
    }

    /**
     * 获取所有用户设置名称数组
     *
     * @return 名称字符串数组
     */
    public String[] getNames() {
        return names; // 返回名称数组
    }

    /**
     * 设置指定索引的用户设置名称
     *
     * @param index       设置索引
     * @param name        设置名称
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void setNames(int index, String name, boolean isUpdateUI) {
        if (Objects.equals(this.names[index], name)) return; // 名称未变化则直接返回
        this.names[index] = name; // 保存设置名称
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_NAME); // 设置消息标志为设置名称
            msgToUI.setParam(String.valueOf(index) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(name)); // 设置索引和名称参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 通过索引保存用户设置
     *
     * @param index       设置索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void setSave(int index, boolean isUpdateUI) {
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_SAVE); // 设置消息标志为保存设置
            msgToUI.setParam(String.valueOf(index)); // 设置索引参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 通过名称保存用户设置
     *
     * @param name        设置名称
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void setSave(String name, boolean isUpdateUI) {
        int index = -1; // 查找结果索引，-1表示未找到
        for (int i = 0; i < names.length; i++) { // 遍历名称数组
            if (names[i].equals(name)) { // 找到匹配的名称
                index = i; // 记录索引
                break; // 跳出循环
            }
        }
        setSave(index, isUpdateUI); // 调用索引保存方法
    }

    /**
     * 通过索引恢复用户设置
     *
     * @param index       设置索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void setRecovery(int index, boolean isUpdateUI) {
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_USERSET_RECOVERY); // 设置消息标志为恢复设置
            msgToUI.setParam(String.valueOf(index)); // 设置索引参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 通过名称恢复用户设置
     *
     * @param name        设置名称
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void setRecovery(String name, boolean isUpdateUI) {
        int index = -1; // 查找结果索引，-1表示未找到
        for (int i = 0; i < names.length; i++) { // 遍历名称数组
            if (names[i].equals(name)) { // 找到匹配的名称
                index = i; // 记录索引
                break; // 跳出循环
            }
        }
        setRecovery(index, isUpdateUI); // 调用索引恢复方法
    }
}
