package com.micsig.tbook.tbookscope.middleware.command; // 命令模式中间件包

import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线，用于组件间通信
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举定义

/**
 * Created by liwb on 2018/1/17.
 * 主菜单-> 自动
 */
/*
 * +=============================================================================+
 * |                            Command_Auto 类                                   |
 * +=============================================================================+
 * | 【模块定位】                                                                 |
 * |   示波器自动设置命令模块，管理自动设置相关的通道开关、触发电平、触发源、       |
 * |   自动量程等参数的状态变更与UI同步                                            |
 * +-------------------------------------------------------------------------------+
 * | 【核心职责】                                                                 |
 * |   1. 管理自动设置中通道开关、触发电平、触发源的状态                           |
 * |   2. 管理自动量程（总量程/垂直/水平/触发电平量程）的开关状态                  |
 * |   3. 状态变更时通过RxBus通知UI层更新                                         |
 * +-------------------------------------------------------------------------------+
 * | 【架构设计】                                                                 |
 * |   简单状态管理类，每个属性提供set方法（含isUpdateUI控制）和query查询方法       |
 * |   set方法内部判断值是否变化，避免重复通知UI                                    |
 * +-------------------------------------------------------------------------------+
 * | 【数据流向】                                                                 |
 * |   UI/SCPI → set方法 → 修改状态 → 构建CommandMsgToUI → RxBus.post → UI更新   |
 * +-------------------------------------------------------------------------------+
 * | 【依赖关系】                                                                 |
 * |   - Command: 获取CommandMsgToUI消息载体                                      |
 * |   - RxBus: 事件总线，发送UI更新消息                                          |
 * |   - CommandMsgToUI: 消息载体，携带FLAG和参数                                 |
 * +-------------------------------------------------------------------------------+
 * | 【使用场景】                                                                 |
 * |   1. 用户在自动设置菜单中切换通道开关、触发源、量程等参数                     |
 * |   2. SCPI远程指令设置自动量程相关参数                                         |
 * +=============================================================================+
 */

public class Command_Auto {


    private int channelIndex; // 通道开关索引，0=开启，1=关闭
    private double level; // 自动触发电平值
    private int triggerSource; // 自动触发源索引
    private int range; // 自动量程开关，0=开启，1=关闭
    private int rangeVertical; // 垂直自动量程开关，0=开启，1=关闭
    private int rangeHorizoncal; // 水平自动量程开关，0=开启，1=关闭
    private int rangeLevel; // 触发电平自动量程开关，0=开启，1=关闭

    /**
     * 设置 主菜单-> 自动-> 自动设置-> 自动打开通道
     *
     * @param b          0： 开启  1：关闭
     * @param isUpdateUI 是否改变界面设置
     */
    public void setChannel(boolean b, boolean isUpdateUI) {
        if (b == (channelIndex == 0)) return; // 如果目标状态与当前状态一致，直接返回
        this.channelIndex = b ? 0 : 1; // 布尔值转为索引：true→0(开启)，false→1(关闭)
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_AUTO_CHANNEL); // 设置自动通道标志
            msgToUI.setParam(String.valueOf(channelIndex)); // 设置通道索引参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 查询自动通道开关状态
     * @return true=通道开启，false=通道关闭
     */
    public boolean setChannelQuery() {
        return this.channelIndex == 0; // 索引0表示通道开启
    }

    /**
     * 设置自动触发电平值
     * @param level 触发电平值
     * @param isUpdateUI 是否更新UI
     */
    public void setLevel(double level, boolean isUpdateUI) {
        this.level = level; // 保存触发电平值
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_AUTO_LEVEL); // 设置自动电平标志
            msgToUI.setParam(String.valueOf(level)); // 设置电平参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 查询自动触发电平值
     * @return 当前触发电平值
     */
    public double setLevelQuery() {
        return this.level; // 返回当前触发电平值
    }

    /**
     * 设置自动触发源
     * @param source 触发源索引
     * @param isUpdateUI 是否更新UI
     */
    public void setSource(int source, boolean isUpdateUI) {
        if (this.triggerSource == source) return; // 如果触发源未变化，直接返回
        this.triggerSource = source; // 保存触发源索引
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_AUTO_SOURCE); // 设置自动触发源标志
            msgToUI.setParam(String.valueOf(source)); // 设置触发源参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 查询自动触发源
     * @return 当前触发源索引
     */
    public int setSourceQuery() {
        return this.triggerSource; // 返回当前触发源索引
    }

//     new SCPICommandStruct(":AUTO:RANge","SCPI_Auto","Range"),//设置自动量程
//            new SCPICommandStruct(":AUTO:RANge?","SCPI_Auto","RangeQ"),//查询自动量程
//            new SCPICommandStruct(":AUTO:RANge:VERtical","SCPI_Auto","Range_Vertical"),//设置自动垂直
//            new SCPICommandStruct(":AUTO:RANge:VERtical?","SCPI_Auto","Range_VerticalQ"),//查询自动垂直
//            new SCPICommandStruct(":AUTO:RANge:HORizoncal","SCPI_Auto","Range_Horizoncal"),//设置自动水平
//            new SCPICommandStruct(":AUTO:RANge:HORizoncal?","SCPI_Auto","Range_HorizoncalQ"),//查询自动水平
//            new SCPICommandStruct(":AUTO:RANge:LEVEl","SCPI_Auto","Range_Level"),//设置自动量程
//            new SCPICommandStruct(":AUTO:RANge:LEVEl?","SCPI_Auto","Range_LevelQ"),//查询自动量程

    /**
     * 设置自动量程开关
     * @param b true=开启自动量程，false=关闭
     * @param isUpdateUI 是否更新UI
     */
    public void range(boolean b, boolean isUpdateUI) {
        if ((range == 0) == b) return; // 如果目标状态与当前状态一致，直接返回
        this.range = b ? 0 : 1; // 布尔值转为索引：true→0(开启)，false→1(关闭)
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_AUTO_RANGE); // 设置自动量程标志
            msgToUI.setParam(String.valueOf(range)); // 设置量程参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 查询自动量程开关状态
     * @return true=自动量程开启，false=关闭
     */
    public boolean rangeQuery() {
        return this.range == 0; // 索引0表示自动量程开启
    }

    /**
     * 设置垂直自动量程开关
     * @param b true=开启垂直自动量程，false=关闭
     * @param isUpdateUI 是否更新UI
     */
    public void rangeVertical(boolean b, boolean isUpdateUI) {
        if ((rangeVertical == 0) == b) return; // 如果目标状态与当前状态一致，直接返回
        this.rangeVertical = b ? 0 : 1; // 布尔值转为索引：true→0(开启)，false→1(关闭)
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_AUTO_RANGEVERTICAL); // 设置垂直自动量程标志
            msgToUI.setParam(String.valueOf(rangeVertical)); // 设置垂直量程参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 查询垂直自动量程开关状态
     * @return true=垂直自动量程开启，false=关闭
     */
    public boolean rangeVerticalQuery() {
        return this.rangeVertical == 0; // 索引0表示垂直自动量程开启
    }

    /**
     * 设置水平自动量程开关
     * @param b true=开启水平自动量程，false=关闭
     * @param isUpdateUI 是否更新UI
     */
    public void rangeHorizoncal(boolean b, boolean isUpdateUI) {
        if ((rangeHorizoncal == 0) == b) return; // 如果目标状态与当前状态一致，直接返回
        this.rangeHorizoncal = b ? 0 : 1; // 布尔值转为索引：true→0(开启)，false→1(关闭)
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_AUTO_RANGEHORIZONTAL); // 设置水平自动量程标志
            msgToUI.setParam(String.valueOf(rangeHorizoncal)); // 设置水平量程参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 查询水平自动量程开关状态
     * @return true=水平自动量程开启，false=关闭
     */
    public boolean rangeHorizoncalQuery() {
        return this.rangeHorizoncal == 0; // 索引0表示水平自动量程开启
    }

    /**
     * 设置触发电平自动量程开关
     * @param b true=开启触发电平自动量程，false=关闭
     * @param isUpdateUI 是否更新UI
     */
    public void rangeLevel(boolean b, boolean isUpdateUI) {
        if ((rangeLevel == 0) == b) return; // 如果目标状态与当前状态一致，直接返回
        this.rangeLevel = b ? 0 : 1; // 布尔值转为索引：true→0(开启)，false→1(关闭)
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取消息载体
            msgToUI.setFlag(CommandMsgToUI.FLAG_AUTO_RANGELEVEL); // 设置触发电平量程标志
            msgToUI.setParam(String.valueOf(rangeLevel)); // 设置触发电平量程参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送UI更新消息
        }
    }

    /**
     * 查询触发电平自动量程开关状态
     * @return true=触发电平自动量程开启，false=关闭
     */
    public boolean rangeLevelQuery() {
        return this.rangeLevel == 0; // 索引0表示触发电平自动量程开启
    }
}
