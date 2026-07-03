package com.micsig.tbook.tbookscope.main.maincenter; // 定义包路径为主界面中心区域菜单命令模块

/**
 * @auother Liwb
 * @description: 中央菜单命令消息类
 * @data:2022-1-18 10:51
 */

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                     MainMsgCenterMenuCommand                                 ║
 * ║                         中央菜单命令消息载体类                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║【模块定位】                                                                   ║
 * ║  位于main.maincenter包中,作为中央菜单控制功能的命令消息载体                 ║
 * ║  用于传递菜单操作命令和相关参数                                              ║
 * ║【核心职责】                                                                   ║
 * ║  1. 定义菜单命令类型常量                                                      ║
 * ║  2. 封装当前命令类型(command)                                                 ║
 * ║  3. 携带命令相关参数(isOpenPercent50)                                        ║
 * ║  4. 提供getter/setter方法访问数据                                             ║
 * ║【架构设计】                                                                   ║
 * ║  采用命令消息模式(Command Message Pattern)                                   ║
 * ║  - 命令枚举: 使用静态常量定义各种命令类型                                    ║
 * ║  - 数据封装: command字段封装命令类型,isOpenPercent50携带参数                ║
 * ║  - 消息传递: 作为消息载体在不同组件间传递命令                                ║
 * ║【数据流向】                                                                   ║
 * ║  外部控制 → MainMsgCenterMenuCommand → RxBus → 中央菜单处理                  ║
 * ║  中央菜单 → 接收命令 → 执行对应操作                                          ║
 * ║【依赖关系】                                                                   ║
 * ║  使用方: MainLayoutCenterMenu(中央菜单布局)                                 ║
 * ║  发送方: 外部控制模块或远程命令                                              ║
 * ║【使用场景】                                                                   ║
 * ║  当需要控制中央菜单执行特定操作时,创建此命令消息                             ║
 * ║  例如: 远程命令、外部按键、自动化测试等场景                                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class MainMsgCenterMenuCommand {
    public static final int CommandAuto=0; // 自动设置命令常量,触发Auto功能
    public static final int CommandRunStop=1; // 运行/停止命令常量,切换RUN/STOP状态
    public static final int CommandSEQ=2; // 单次触发命令常量,执行Single功能
    public static final int Command50Percent=3; // 50%触发命令常量,打开/关闭50%触发对话框
    public static final int CommandCalibrationZero=4; // 零点校准命令常量,执行零点校准功能
    public static final int CommandReturnHome=5; // 返回主页命令常量,退出当前界面返回主页
    public static final int CommandSerialText=6; // 串口文本命令常量,控制串口文本显示

    private int command; // 当前命令类型,取值为上述命令常量之一
    public boolean isOpenPercent50=false; // 50%触发对话框开关标识,仅Command50Percent命令使用

    /**
     * 默认构造方法
     * 创建空命令消息对象
     */
    public MainMsgCenterMenuCommand(){}

    /**
     * 带参数构造方法
     * 创建命令消息对象并设置命令类型
     * @param command 命令类型(CommandAuto/CommandRunStop等)
     */
    public MainMsgCenterMenuCommand(int command){
        this.command=command; // 设置命令类型
    }

    /**
     * 获取命令类型
     * @return 命令类型值(0-6)
     */
    public int getCommand() {
        return command; // 返回命令类型
    }

    /**
     * 设置命令类型
     * @param command 命令类型值(0-6)
     */
    public void setCommand(int command) {
        this.command = command; // 更新命令类型
    }

    /**
     * toString方法
     * 用于调试输出和日志记录
     * @return 包含command的字符串表示
     */
    @Override
    public String toString() {
        return "command:"+command; // 返回命令类型字符串
    }
}