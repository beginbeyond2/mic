package com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob; // 多功能旋钮模块包

import android.graphics.Point; // 导入点类，用于计算中心坐标

import java.util.List; // 导入列表接口，用于存储父子节点

/**
 * Created by yangj on 2018/5/22.
 */

/**
 * +----------------------------------------------------------------------+
 * |                      外部按键节点模型                                  |
 * +----------------------------------------------------------------------+
 * | 模块定位：多功能旋钮模块的核心数据结构，描述示波器UI中每个可由          |
 * |           物理按键/旋钮操作的控件节点                                  |
 * +----------------------------------------------------------------------+
 * | 核心职责：                                                            |
 * |   1. 定义按键节点的类型常量（进度条、列表、对话框等）                  |
 * |   2. 定义对话框类型常量（测量延迟、相位、数字键盘等）                  |
 * |   3. 存储节点位置(x,y,w,h)、名称、类型、对话框等属性                  |
 * |   4. 构建父子节点树形结构，支持导航层级                               |
 * |   5. 提供点击坐标计算和包含检测功能                                    |
 * +----------------------------------------------------------------------+
 * | 架构设计：                                                            |
 * |   树形数据结构，每个节点可拥有父节点和子节点列表                       |
 * |   通过type字段区分不同操作类型（进度条、列表、对话框等）               |
 * |   通过dialog字段关联弹出对话框类型                                     |
 * +----------------------------------------------------------------------+
 * | 数据流向：                                                            |
 * |   ExternalKeysProtocol → ExternalKeysNode树 → 模拟点击/旋钮操作       |
 * +----------------------------------------------------------------------+
 * | 依赖关系：                                                            |
 * |   - android.graphics.Point (中心点计算)                                |
 * |   - java.util.List       (子节点列表)                                  |
 * +----------------------------------------------------------------------+
 * | 使用场景：                                                            |
 * |   物理旋钮旋转/按下时，根据当前焦点节点导航到目标节点并模拟点击        |
 * +----------------------------------------------------------------------+
 */
public class ExternalKeysNode {
    public static final String TYPE_NO_CLICK = "noClick";//不可点击类型，相对于一般的按键是直接模拟点击来说的
    public static final String TYPE_CLICK_IS_SUREBACK = "clickIsBack";//点击确定返回上一级类型
    public static final String TYPE_BRIGHTNESS_PROGRESS = "brightnessProgress";//波形亮度
    public static final String TYPE_TRIGGER_SENSITIVITY_PROGRESS = "triggerSensitivity";//波形亮度
    public static final String TYPE_INTENSITY_PROGRESS = "intensityProgress";//网格亮度
    public static final String TYPE_TOPCOUNT_PROGRESS = "topCount";//topDialogScale进度条
    public static final String TYPE_RIGHT_SLIP_CH_DELAY = "rightSlipChDelay";//模拟通道设置菜单delay弹窗中进度条
    public static final String TYPE_RIGHT_SLIP_CH_DELAY_UNIT = "rightSlipChDelayUnit";//模拟通道设置菜单delay弹窗中 移动光标位置
    public static final String TYPE_ALPHA_PROGRESS="TYPE_ALPHA_PROGRESS"; //显示透明度
    public static final String TYPE_PERSIST_ADJUST = "persistAdjust";//余辉时间
    public static final String TYPE_TRIGGER_TITLE = "triggerTitle";//触发菜单的滑动
    public static final String TYPE_MAIN_MENU_CHANNEL_MEASURE_COMMON="TYPE_MAIN_MENU_CHANNEL_MEASURE_COMMON"; //主菜单测量common通道
    public static final String TYPE_MAIN_MENU_CHANNEL_MEASURE_SETTING="TYPE_MAIN_MENU_CHANNEL_MEASURE_SETTING"; //主菜单测量设置通道
    public static final String TYPE_MAIN_MENU_CHANNEL_SAVE_WAVE="TYPE_MAIN_MENU_CHANNEL_SAVE_WAVE"; //主菜单保存波形通道
    public static final String TYPE_MAIN_MENU_CHANNEL_SAVE_CSV="TYPE_MAIN_MENU_CHANNEL_SAVE_CSV"; //主菜单保存CSV通道
    public static final String TYPE_MAIN_MENU_CHANNEL_AUTO_SAVE_CHANNEL="TYPE_MAIN_MENU_CHANNEL_AUTO_SAVE_CHANNEL"; //主菜单自动保存通道

    public static final String TYPE_MAIN_MENU_CHANNEL_SAVE_BIN="TYPE_MAIN_MENU_CHANNEL_SAVE_BIN"; //主菜单保存BIN通道
    public static final String TYPE_MAIN_MENU_CHANNEL_CURSOR_COMMON="TYPE_MAIN_MENU_CHANNEL_CURSOR_COMMON"; //主菜单光标common通道
    public static final String TYPE_TRIGGER_VIDEO_LINE = "triggerVideoLine";//视频触发的line的显示
    public static final String TYPE_TRIGGER_VIDEO_STANDARD_FIRST = "triggerVideoStandardFirst";//视频触发的line的显示
    public static final String TYPE_RECEIVE_MSG = "receiveMsg"; //接收消息类型
    public static final String TYPE_REFRECALL_PROGRESS = "refRecallProgress";//参考通道选择列表
    public static final String TYPE_BACK_TO_BACK = "backToBack";//rightSerials中，uart下，baudRate的小键盘自定义数据确定之后的连续返回
    public static final String TYPE_CENTER_SEGMENT_PLAY = "centerSegmentPlay";//分段存储导航条的播放按键
    public static final String TYPE_CSV_LIST = "typeCsvList";//Ref设置页面CSV列表
    public static final String TYPE_SPINNER_LIST = "typeSpinnerList";// 文件/文件夹 选择列表

    public static final String TYPE_CAN_PERCENT = "canPercent";//rightSerials中，can下，baudRate的percent的外部按键旋转点击

    public static final String DIALOG_MEASUREDELAY = "DIALOG_MEASUREDELAY"; //测量延迟对话框
    public static final String DIALOG_MEASUREPHASE = "DIALOG_MEASUREPHASE"; //测量相位对话框
    public static final String DIALOG_TOPCOUNT = "DIALOG_TOPCOUNT"; //Top计数对话框
    public static final String DIALOG_TOPSCALE = "DIALOG_TOPSCALE"; //Top缩放对话框
    public static final String DIALOG_TEXTKEYBOARD = "DIALOG_TEXTKEYBOARD"; //文本键盘对话框
    public static final String DIALOG_NUMBERKEYBOARD = "DIALOG_NUMBERKEYBOARD"; //数字键盘对话框
    public static final String DIALOG_BAUDRATE = "DIALOG_BAUDRATE"; //波特率对话框
    public static final String DIALOG_CHANNELLABEL = "DIALOG_CHANNELLABEL"; //通道标签对话框
    public static final String DIALOG_BANDWIDTH = "DIALOG_BANDWIDTH"; //带宽对话框
    public static final String DIALOG_PROBEMULTIPLE = "DIALOG_PROBEMULTIPLE"; //探头倍率对话框
    public static final String DIALOG_REFRECALL = "DIALOG_REFRECALL"; //参考回调对话框
    public static final String DIALOG_AFTERGLOW = "DIALOG_AFTERGLOW"; //余辉对话框
    public static final String DIALOG_PROBE_INTERFACE="DIALOG_PROBE_INTERFACE"; //探头接口对话框
    public static final String DIALOG_MATH_PERSISTENCE="DIALOG_MATH_PERSISTENCE"; //数学运算余辉对话框
    public static final String DIALOG_LOAD_CSV="DIALOG_LOAD_CSV"; //加载CSV对话框
    public static final String DIALOG_SELECT_COLOR = "DIALOG_SELECT_COLOR"; //选择颜色对话框
    public static final String DIALOG_MEASURETVALUE = "DIALOG_MEASURETVALUE"; //测量T值对话框

    public static final int ACTION_REFRECALL_UP = -1; //参考回调操作：上移
    public static final int ACTION_REFRECALL_FINISH = 0; //参考回调操作：完成
    public static final int ACTION_REFRECALL_DOWN = 1; //参考回调操作：下移

    private String name; // 节点名称
    private int index; // 节点索引（在同级节点中的序号）
    private int x, y, w, h; // 节点位置和尺寸：左上角坐标(x,y)和宽高(w,h)
    private String type; // 节点类型（对应TYPE_*常量）
    private String dialog; // 关联的对话框类型（对应DIALOG_*常量）
    private ExternalKeysNode parentNode; // 父节点引用
    private List<ExternalKeysNode> parentNodes; // 父节点列表（支持多父节点）
    private List<ExternalKeysNode> childNodes; // 子节点列表

    /**
     * 当前列表中的选中项的保存，只在list的第一个选项中存取
     */
    private int curListSelect; // 当前列表选中项索引
    /**
     * 当前item的显现性
     */
    private boolean visible = true; // 节点是否可见，默认可见

    /**
     * 默认构造函数
     */
    public ExternalKeysNode() {
    }

    /**
     * 设置节点名称（链式调用）
     *
     * @param name 节点名称
     * @return 当前节点实例，支持链式调用
     */
    public ExternalKeysNode setName(String name) {
        this.name = name; // 设置名称
        return this; // 返回当前实例
    }

    /**
     * 设置节点位置和尺寸
     *
     * @param index 节点索引
     * @param x     左上角X坐标
     * @param y     左上角Y坐标
     * @param w     宽度
     * @param h     高度
     */
    public void setPlace(int index, int x, int y, int w, int h) {
        this.index = index; // 设置索引
        this.x = x; // 设置X坐标
        this.y = y; // 设置Y坐标
        this.w = w; // 设置宽度
        this.h = h; // 设置高度
    }

    /**
     * 获取节点的中心点坐标
     * 用于模拟点击时确定触摸位置
     *
     * @return 节点中心点
     */
    public Point getCenterPoint(){
        return new Point(x+w/2,y+h/2); // 计算中心点：左上角 + 宽高的一半
    }

    /**
     * 检测指定点是否在节点区域内
     * 用于判断触摸/点击事件是否落在当前节点上
     *
     * @param px 点的X坐标
     * @param py 点的Y坐标
     * @return true=点在节点区域内，false=不在
     */
    public boolean isContain(int px, int py) {
        return px >= x && px <= (x + w) && py >= y && py <= (y + h); // 判断点是否在矩形区域内
    }

    /**
     * 获取关联的对话框类型
     *
     * @return 对话框类型字符串（对应DIALOG_*常量）
     */
    public String getDialog() {
        return dialog; // 返回对话框类型
    }

    /**
     * 设置关联的对话框类型
     *
     * @param dialog 对话框类型字符串（对应DIALOG_*常量）
     */
    public void setDialog(String dialog) {
        this.dialog = dialog; // 设置对话框类型
    }

    /**
     * 设置父节点
     *
     * @param parentNode 父节点引用
     */
    public void setParentNode(ExternalKeysNode parentNode) {
        this.parentNode = parentNode; // 设置父节点
    }

    /**
     * 设置父节点列表
     *
     * @param parentNodes 父节点列表
     */
    public void setParentNodes(List<ExternalKeysNode> parentNodes) {
        this.parentNodes = parentNodes; // 设置父节点列表
    }

    /**
     * 设置子节点列表
     *
     * @param childNodes 子节点列表
     */
    public void setChildNodes(List<ExternalKeysNode> childNodes) {
        this.childNodes = childNodes; // 设置子节点列表
    }

    /**
     * 获取节点类型
     *
     * @return 节点类型字符串（对应TYPE_*常量）
     */
    public String getType() {
        return type; // 返回节点类型
    }

    /**
     * 设置节点类型
     *
     * @param type 节点类型字符串（对应TYPE_*常量）
     */
    public void setType(String type) {
        this.type = type; // 设置节点类型
    }

    /**
     * 设置节点左上角X坐标
     *
     * @param x X坐标
     */
    public void setX(int x) {
        this.x = x; // 设置X坐标
    }

    /**
     * 设置节点左上角Y坐标
     *
     * @param y Y坐标
     */
    public void setY(int y) {
        this.y = y; // 设置Y坐标
    }

    /**
     * 设置节点宽度
     *
     * @param w 宽度
     */
    public void setW(int w) {
        this.w = w; // 设置宽度
    }

    /**
     * 设置节点高度
     *
     * @param h 高度
     */
    public void setH(int h) {
        this.h = h; // 设置高度
    }

    /**
     * 获取节点是否可见
     *
     * @return true=可见，false=不可见
     */
    public boolean isVisible() {
        return visible; // 返回可见性
    }

    /**
     * 设置节点是否可见
     *
     * @param visible true=可见，false=不可见
     */
    public void setVisible(boolean visible) {
        this.visible = visible; // 设置可见性
    }

    /**
     * 设置当前列表选中项索引
     *
     * @param curListSelect 选中项索引
     */
    public void setCurListSelect(int curListSelect) {
        this.curListSelect = curListSelect; // 设置选中项索引
    }

    /**
     * 获取当前列表选中项索引
     *
     * @return 选中项索引
     */
    public int getCurListSelect() {
        return curListSelect; // 返回选中项索引
    }

    /**
     * 获取节点名称
     *
     * @return 节点名称
     */
    public String getName() {
        return name; // 返回名称
    }

    /**
     * 获取节点索引
     *
     * @return 节点索引
     */
    public int getIndex() {
        return index; // 返回索引
    }

    /**
     * 获取节点左上角X坐标
     *
     * @return X坐标
     */
    public int getX() {
        return x; // 返回X坐标
    }

    /**
     * 获取节点左上角Y坐标
     *
     * @return Y坐标
     */
    public int getY() {
        return y; // 返回Y坐标
    }

    /**
     * 获取节点宽度
     *
     * @return 宽度
     */
    public int getW() {
        return w; // 返回宽度
    }

    /**
     * 获取节点高度
     *
     * @return 高度
     */
    public int getH() {
        return h; // 返回高度
    }

    /**
     * 获取父节点
     *
     * @return 父节点引用
     */
    public ExternalKeysNode getParentNode() {
        return parentNode; // 返回父节点
    }

    /**
     * 获取父节点列表
     *
     * @return 父节点列表
     */
    public List<ExternalKeysNode> getParentNodes() {
        return parentNodes; // 返回父节点列表
    }

    /**
     * 获取子节点列表
     *
     * @return 子节点列表
     */
    public List<ExternalKeysNode> getChildNodes() {
        return childNodes; // 返回子节点列表
    }

    /**
     * 返回节点的字符串描述
     *
     * @return 包含index、name、visible、curListSelect、type、dialog、位置信息的字符串
     */
    @Override
    public String toString() {
        return "ExternalKeysNode{" + // 构建字符串
                "index=" + index + // 追加索引
                ", name='" + name + '\'' + // 追加名称
                ", visible=" + visible + // 追加可见性
                ", curListSelect="+ curListSelect+ // 追加列表选中项
                ", type = " + type + // 追加节点类型
                ", dialog = " + dialog + // 追加对话框类型
                ", x=" + x + // 追加X坐标
                ", y=" + y + // 追加Y坐标
                ", w=" + w + // 追加宽度
                ", h=" + h + // 追加高度
                '}'; // 结束括号
    }
}
