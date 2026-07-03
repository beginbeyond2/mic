package com.micsig.tbook.tbookscope.structdata; // 示波器结构化数据包

import android.os.Handler; // Android消息处理器
import android.os.Message; // Android消息对象
import android.os.SystemClock; // 系统时钟工具
import android.util.Log; // Android日志工具
import android.view.View; // Android视图基类
import android.widget.CheckBox; // 复选框控件
import android.widget.TextView; // 文本视图控件

import com.micsig.tbook.scope.channel.Channel; // 通道数据模型
import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂，获取通道实例
import com.micsig.tbook.scope.vertical.VerticalAxis; // 垂直轴刻度工具
import com.micsig.tbook.tbookscope.MainActivity; // 主Activity
import com.micsig.tbook.tbookscope.MainViewGroup; // 主视图容器
import com.micsig.tbook.tbookscope.R; // 资源ID
import com.micsig.tbook.tbookscope.main.ExKeysMsgRightCanPercent; // 右侧旋钮百分比消息
import com.micsig.tbook.tbookscope.main.ExternalKeysMsgChannel; // 外部按键通道消息
import com.micsig.tbook.tbookscope.main.ExternalKeysMsgCursor; // 外部按键光标消息
import com.micsig.tbook.tbookscope.main.ExternalKeysMsgLevel; // 外部按键触发电平消息
import com.micsig.tbook.tbookscope.main.ExternalKeysMsgTimeBase; // 外部按键时基消息
import com.micsig.tbook.tbookscope.main.ExternalKeysMsgVScale; // 外部按键垂直刻度消息
import com.micsig.tbook.tbookscope.main.mainbottom.MainHolderBottom; // 底部主菜单容器
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterChannel; // 中间通道布局
import com.micsig.tbook.tbookscope.main.maincenter.MainMsgCenterMenuCommand; // 中间菜单命令
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob.ExternalKeysManager; // 外部按键管理器
import com.micsig.tbook.tbookscope.top.popwindow.TopLayoutPopWindow; // 顶部弹出菜单窗口
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类
import com.micsig.tbook.tbookscope.util.DToast; // 自定义Toast工具
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 工作模式管理器
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 光标管理器
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase; // 触发时基管理器
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage; // 波形管理器
import com.micsig.tbook.ui.top.view.scale.TopUtilScale; // 顶部刻度工具
import com.micsig.tbook.ui.wavezone.TChan; // 通道编号常量

/**
 * Created by yangj on 2018/1/4.
 */

/*
 * +===================================================================================================+
 * |                                  ExternalKeysCommand                                              |
 * +===================================================================================================+
 * | 模块定位 : 示波器外部按键命令分发中心，属于 structdata 结构化数据层                               |
 * +---------------------------------------------------------------------------------------------------+
 * | 核心职责 : 1. 接收硬件按键/旋钮的操作指令，封装为结构化消息并通过 RxBus 广播到 UI 各模块            |
 * |           2. 查询当前 UI 状态（弹窗、侧滑菜单、对话框、光标等）供按键逻辑做条件判断               |
 * |           3. 控制侧滑菜单的打开/关闭，协调顶部/底部/右侧菜单的切换                              |
 * |           4. 管理光标的显示、切换、选中与移动操作                                                |
 * |           5. 管理触发电平、时基、通道垂直刻度的调节命令                                          |
 * |           6. 处理快捷功能键（Home/Seq/Auto/RunStop/截图/快速保存/缩放）                          |
 * +---------------------------------------------------------------------------------------------------+
 * | 架构设计 : 单例模式 + 命令模式。外部按键事件 → ExternalKeysCommand（命令封装）→ RxBus（事件总线）|
 * |           → 各 UI 模块订阅消费。Handler 用于延时操作和 Toast 消息调度。                          |
 * +---------------------------------------------------------------------------------------------------+
 * | 数据流向 : ExternalKeysManager → ExternalKeysCommand → RxBus → UI各模块                        |
 * |           ExternalKeysCommand ← MainViewGroup（查询UI状态）                                     |
 * +---------------------------------------------------------------------------------------------------+
 * | 依赖关系 : MainViewGroup      : 主视图容器，用于查询UI状态和控制侧滑菜单                          |
 * |           MainActivity       : 主Activity，初始化时注入                                         |
 * |           RxBus / RxEnum     : 事件总线与事件类型枚举                                            |
 * |           CursorManage       : 光标管理器，执行光标操作                                         |
 * |           WaveManage         : 波形管理器，执行通道位置移动                                      |
 * |           TriggerTimebase    : 触发时基管理器，执行时基位置移动                                  |
 * |           ChannelFactory     : 通道工厂，获取通道实例                                            |
 * |           WorkModeManage     : 工作模式管理器，判断当前模式                                      |
 * |           ExternalKeysManager: 外部按键管理器，控制通道选择框                                    |
 * |           TopLayoutPopWindow : 顶部弹出菜单窗口                                                 |
 * |           DToast / CacheUtil : Toast提示与缓存工具                                              |
 * +---------------------------------------------------------------------------------------------------+
 * | 使用场景 : 硬件旋钮/按键按下时，由 ExternalKeysManager 调用本类的对应方法分发命令；              |
 * |           UI 模块需要查询当前界面状态时，通过本类的查询方法获取。                                 |
 * +===================================================================================================+
 */
public class ExternalKeysCommand {
    public static final int MCUTOARM_RUNSTOP = 1; // 运行/停止按键常量
    public static final int MCUTOARM_SEQ = 2; // 单次触发按键常量
    public static final int MCUTOARM_AUTO = 3; // 自动设置按键常量
    public static final int MCUTOARM_SCREENCAPTURE = 4; // 截屏按键常量
    public static final int MCUTOARM_ZOOM = 5; // 缩放按键常量
    public static final int MCUTOARM_HOME = 6; // Home按键常量
    public static final int MCUTOARM_QUICKSAVE = 7; // 快速保存按键常量

    private static ExternalKeysCommand command; // 单例实例
    private MainViewGroup mainViewGroup; // 主视图容器引用
    private MainActivity mainActivity; // 主Activity引用
    private TopLayoutPopWindow topSlipMenuBar_Quick; // 顶部快捷菜单弹出窗口



    /**
     * 私有构造方法，实现单例模式，禁止外部实例化
     */
    private ExternalKeysCommand() {
    }

    /**
     * 获取ExternalKeysCommand的单例实例
     * <p>采用懒加载方式，首次调用时创建实例</p>
     *
     * @return ExternalKeysCommand单例对象
     */
    public static ExternalKeysCommand get() {
        if (command == null) { // 判断单例是否已创建
            command = new ExternalKeysCommand(); // 创建单例实例
        }
        return command; // 返回单例实例
    }

    /**
     * 初始化方法，注入MainActivity和MainViewGroup引用，并创建Handler
     *
     * @param mainActivity  主Activity实例
     * @param mainViewGroup 主视图容器实例
     */
    public void init(MainActivity mainActivity, MainViewGroup mainViewGroup) {
        this.mainActivity = mainActivity; // 保存主Activity引用
        this.mainViewGroup = mainViewGroup; // 保存主视图容器引用
        this.handler = new Handler(); // 创建新的Handler实例
    }

    /**
     * 查询用户是否正在触摸屏幕
     *
     * @return true表示用户正在触摸屏幕，false表示未触摸
     */
    public boolean isUserTouch() {
        return mainViewGroup.isUserTouch(); // 委托给MainViewGroup查询触摸状态
    }

    /**
     * 查询指定对话框是否正在显示
     *
     * @param dialog 对话框类型标识，使用@MainViewGroup.Dialog注解
     * @return true表示该对话框正在显示，false表示未显示
     */
    public boolean isDialogShow(@MainViewGroup.Dialog int dialog) {
        return mainViewGroup.isDialogShow(dialog); // 委托给MainViewGroup查询对话框显示状态
    }

    /**
     * 查询中间分段布局是否正在显示
     *
     * @return true表示中间分段布局可见，false表示不可见
     */
    public boolean isDialogSegmentShow(){
        return mainViewGroup.getCenterSegmentedLayout().getVisibility()== View.VISIBLE; // 判断中间分段布局的可见性
    }

    /**
     * 查询通道列表布局是否正在显示
     *
     * @return true表示通道列表布局可见，false表示不可见
     */
    public boolean isDialogChListShow(){
        return mainViewGroup.getChannelsLayout().getVisibility()==View.VISIBLE; // 判断通道列表布局的可见性
    }

    /**
     * 查询右侧布局是否为电平菜单
     *
     * @return true表示右侧布局是电平菜单，false表示不是
     */
    public boolean isRightLayoutLevel() {
        return mainViewGroup.isLevelMenu(); // 委托给MainViewGroup查询是否为电平菜单
    }

    /**
     * 隐藏右侧电平菜单
     */
    public void hideRightLayoutLevel() {
        mainViewGroup.hideLevelMenu(); // 委托给MainViewGroup隐藏电平菜单
    }

    /**
     * 进入Y-T模式下的串口文本输入界面
     * <p>通过RxBus发送菜单命令，切换到串口文本输入模式</p>
     */
    public void EnterToYt(){
        MainMsgCenterMenuCommand command= new MainMsgCenterMenuCommand(MainMsgCenterMenuCommand.CommandSerialText); // 创建串口文本命令
        RxBus.getInstance().post(RxEnum.MainLeft_To_Menu_Command,command); // 通过事件总线发送命令
    }

    /**
     * 查询全键盘拼音预选框是否正在显示
     *
     * @return true表示预选框正在显示，false表示未显示
     */
    public boolean isKeyboradCandidatesWordShow() {
        return mainViewGroup.isKeyboradCandidatesWordShow(); // 委托给MainViewGroup查询预选框显示状态
    }

    /**
     * 查询确认/取消对话框是否可见
     *
     * @return true表示确认/取消对话框可见，false表示不可见
     */
    public boolean isDialogOkVisible() {
        return mainViewGroup.isDialogOkVisible(); // 委托给MainViewGroup查询对话框可见性
    }

    /**
     * 隐藏中间菜单和中间半屏布局
     */
    public void hideCenterMenuAndCenterHalf() {
        mainViewGroup.hideCenterMenuAndCenterHalf(); // 委托给MainViewGroup隐藏中间菜单和半屏
    }

    private CheckBox cursorH, cursorV; // 水平光标和垂直光标的复选框引用

    /**
     * 查询光标是否显示
     *
     * @param isHor 水平方向true，垂直false
     * @return true表示该方向光标已选中（显示），false表示未选中（隐藏）
     */
    public boolean isCursorShow(boolean isHor) {
        if (isHor) { // 判断是否查询水平光标
            if (cursorH == null) { // 水平光标复选框引用为空则查找
                cursorH = (CheckBox) mainViewGroup.findViewById(R.id.cursorH); // 查找水平光标复选框
            }
            return cursorH.isChecked(); // 返回水平光标是否选中
        } else { // 查询垂直光标
            if (cursorV == null) { // 垂直光标复选框引用为空则查找
                cursorV = (CheckBox) mainViewGroup.findViewById(R.id.cursorV); // 查找垂直光标复选框
            }
            return cursorV.isChecked(); // 返回垂直光标是否选中
        }
    }

    /**
     * 打开或关闭光标
     *
     * @param isHor 水平方向true，垂直false
     * @param type  本次操作类型，在ExternalKeysMsgCursor类中定义
     */
    public void clickCursor(boolean isHor, int type) {
//        if (mainViewGroup.isSlipShow(MainViewGroup.BOTTOMSLIP)) {
//            mainViewGroup.hideSlip(MainViewGroup.BOTTOMSLIP);
//        }
//        mainViewGroup.hideAllDialogSlip();
//        ExternalKeysUI.getInstance().onClick(isHor ? 600 : 650, 550);
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_CURSOR, new ExternalKeysMsgCursor(isHor, type)); // 通过事件总线发送光标点击消息
    }

    /**
     * 切换光标到下一个状态
     *
     * @param isHor 水平方向true，垂直false
     */
    public void switchCursor(boolean isHor) {
        mainViewGroup.hideAllDialogSlip(); // 先隐藏所有弹窗和侧滑菜单
        if (isHor) { // 判断是否切换水平光标
            CursorManage.getInstance().nextHorState(); // 切换到水平光标的下一个状态
        } else { // 切换垂直光标
            CursorManage.getInstance().nextVerState(); // 切换到垂直光标的下一个状态
        }
    }

    /**
     * 设置光标选中项
     *
     * @param isHor        水平方向true，垂直false
     * @param iWaveCursor  光标索引编号
     */
    public void setCursorSelected(boolean isHor, int iWaveCursor) {
        mainViewGroup.hideAllDialogSlip(); // 先隐藏所有弹窗和侧滑菜单
        if (isHor) { // 判断是否设置水平光标选中
            CursorManage.getInstance().setHorCursorSelected(iWaveCursor); // 设置水平光标选中项
        } else { // 设置垂直光标选中
            CursorManage.getInstance().setVerCursorSelected(iWaveCursor); // 设置垂直光标选中项
        }
    }

    /**
     * 获取当前选中的光标索引
     *
     * @param isHor 水平方向true，垂直false
     * @return 当前选中的光标索引编号
     */
    public int getCursorSelected(boolean isHor) {
        if (isHor) { // 判断是否查询水平光标
            return CursorManage.getInstance().getHorCursorSelected(); // 返回水平光标选中索引
        } else { // 查询垂直光标
            return CursorManage.getInstance().getVerCursorSelected(); // 返回垂直光标选中索引
        }
    }

    /**
     * 移动光标
     *
     * @param isHor   水平方向true，垂直false
     * @param isRight 顺时针（向右）移动true，逆时针（向左）移动false
     * @param count   移动次数（步数）
     */
    public void moveCursor(boolean isHor, boolean isRight, int count) {
        mainViewGroup.hideAllDialogSlip(); // 先隐藏所有弹窗和侧滑菜单
        if (isHor) { // 判断是否移动水平光标
            CursorManage.getInstance().moveHorCursor(isRight, count); // 移动水平光标
        } else { // 移动垂直光标
            CursorManage.getInstance().moveVerCursor(isRight, count); // 移动垂直光标
        }
    }

    /**
     * 移动聚拢光标（缩放模式下的光标）
     *
     * @param isHor   水平方向true，垂直false
     * @param isRight 聚拢方向true，展开方向false
     * @param count   移动次数（步数）
     */
    public void moveZoomCursor(boolean isHor, boolean isRight, int count) {
        mainViewGroup.hideAllDialogSlip(); // 先隐藏所有弹窗和侧滑菜单
        if (isHor) { // 判断是否移动水平聚拢光标
            CursorManage.getInstance().moveZoomHorCursor(isRight, count); // 移动水平聚拢光标
        } else { // 移动垂直聚拢光标
            CursorManage.getInstance().moveZoomVerCursor(isRight, count); // 移动垂直聚拢光标
        }
    }

    /**
     * 多功能按钮操作TopDialogScale（顶部刻度调节对话框）
     *
     * @param isSmall true表示位于上侧的微调，false表示位于下侧的粗调
     * @param isRight true表示向右滑动（增大），false表示向左滑动（减小）
     * @param count   操作次数
     */
    public void moveTopDialogScale(boolean isSmall, boolean isRight, int count) {
        if (isSmall) { // 微调模式
            if (isRight) { // 微调向右
                RxBus.getInstance().post(RxEnum.DIALOG_SCALE_CHANGED, TopUtilScale.ACTION_SCALE_SMALL_RIGHT); // 发送微调右滑事件
            } else { // 微调向左
                RxBus.getInstance().post(RxEnum.DIALOG_SCALE_CHANGED, TopUtilScale.ACTION_SCALE_SMALL_LEFT); // 发送微调左滑事件
            }
        } else { // 粗调模式
            if (isRight) { // 粗调向右
                RxBus.getInstance().post(RxEnum.DIALOG_SCALE_CHANGED, TopUtilScale.ACTION_SCALE_LARGE_RIGHT); // 发送粗调右滑事件
            } else { // 粗调向左
                RxBus.getInstance().post(RxEnum.DIALOG_SCALE_CHANGED, TopUtilScale.ACTION_SCALE_LARGE_LEFT); // 发送粗调左滑事件
            }
        }
    }

    /**
     * 多功能下旋钮操作refRecall（参考波形召回）
     *
     * @param refRecallFlag 操作标识字符串：<p/>
     *                      {ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R1} 向上移动<p/>
     *                      {ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R1} 向下移动<p/>
     *                      {ExternalKeysNode.ACTION_REFRECALL_FINISH + CommandMsgToUI.PARAM_SPLIT + TChan.R1} 选择并退出
     * @param count         操作次数
     */
    public void moveRefRecall(String refRecallFlag, int count) {
        RxBus.getInstance().post(RxEnum.DIALOG_REFRECALL_CHANGED, refRecallFlag); // 通过事件总线发送参考波形召回操作事件
    }

    /**
     * 多功能下键操作全键盘时的拼音预选框
     *
     * @param flag  操作标识：<p/>
     *              {TopDialogCandidatesWord.ACTION_CANDIDATES_RIGHT} 向右移动<p/>
     *              {TopDialogCandidatesWord.ACTION_CANDIDATES_LEFT}  向左移动<p/>
     *              {TopDialogCandidatesWord.ACTION_CANDIDATES_FINISH} 选择并退出
     * @param count 操作次数
     */
    public void moveKeyBoardCandidatesWord(int flag, int count) {
        RxBus.getInstance().post(RxEnum.DIALOG_CANDIDATE_CHANGED, flag); // 通过事件总线发送拼音预选框操作事件
    }

    /**
     * 移动串口文本候选词列表
     *
     * @param count 移动步数（正数向下，负数向上）
     */
    public void moveSerialsWordList(int count) {
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_SERIALSWORD, count); // 通过事件总线发送串口候选词移动事件
    }

    /**
     * 移动右侧旋钮百分比调节
     *
     * @param isS1  true表示S1旋钮，false表示S2旋钮
     * @param isAdd true表示增加，false表示减少
     * @param isTop true表示上侧微调，false表示下侧粗调
     * @param count 调节步数
     */
    public void moveRightCanPercent(boolean isS1, boolean isAdd, boolean isTop, int count) {
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_RIGHTCAN_PERCENT // 通过事件总线发送右侧旋钮百分比调节事件
                , new ExKeysMsgRightCanPercent(isS1, isAdd, isTop, count)); // 封装旋钮百分比消息参数
    }

    /**
     * 查询是否有任意侧滑菜单正在显示
     * <p>检查顶部、底部、所有通道/数学/参考/串口侧滑菜单</p>
     *
     * @return true表示至少有一个侧滑菜单正在显示，false表示全部隐藏
     */
    public boolean isSlip() {
        return mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 检查顶部侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.BOTTOMSLIP) // 检查底部侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 检查CH1右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 检查CH2右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 检查CH3右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 检查CH4右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 检查CH5右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 检查CH6右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 检查CH7右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 检查CH8右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1) // 检查MATH1右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2) // 检查MATH2右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3) // 检查MATH3右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4) // 检查MATH4右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5) // 检查MATH5右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6) // 检查MATH6右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7) // 检查MATH7右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8) // 检查MATH8右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 检查REF1右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 检查REF2右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 检查REF3右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 检查REF4右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 检查REF5右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 检查REF6右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 检查REF7右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 检查REF8右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1) // 检查S1右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2) // 检查S2右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3) // 检查S3右侧侧滑菜单
                || mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4); // 检查S4右侧侧滑菜单
    }

    /**
     * 查询是否有任意对话框正在显示
     *
     * @return true表示有对话框显示，false表示没有
     */
    public boolean isDialog(){
        return mainViewGroup.isDialogsShow(); // 委托给MainViewGroup查询对话框显示状态
    }

    /**
     * 查询指定侧滑菜单是否正在显示
     *
     * @param slip 侧滑菜单类型标识，使用@MainViewGroup.Slip注解
     * @return true表示该侧滑菜单正在显示，false表示未显示
     */
    public boolean isSlipShow(int slip) {
        return mainViewGroup.isSlipShow(slip); // 委托给MainViewGroup查询指定侧滑菜单状态
    }

    /**
     * 切换顶部侧滑菜单的显示状态
     * <p>如果顶部侧滑菜单未显示则打开，已显示则关闭</p>
     */
    public void openTopSlip() {
        if (!mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)) { // 顶部侧滑菜单未显示
            mainViewGroup.hideAllDialogSlip(); // 先隐藏所有弹窗和侧滑菜单
            mainViewGroup.openSlip(MainViewGroup.TOPSLIP); // 打开顶部侧滑菜单
        } else { // 顶部侧滑菜单已显示
            mainViewGroup.hideSlip(MainViewGroup.TOPSLIP); // 关闭顶部侧滑菜单
        }
    }

    /**
     * 切换底部侧滑菜单的显示状态
     * <p>如果底部侧滑菜单已显示则关闭，未显示则打开</p>
     * <p>当串口文本模式开启时，不允许打开底部侧滑菜单</p>
     */
    public void openBottomSlip() {
        if (mainViewGroup.isSlipShow(MainViewGroup.BOTTOMSLIP)) { // 底部侧滑菜单已显示
            mainViewGroup.hideSlip(MainViewGroup.BOTTOMSLIP); // 关闭底部侧滑菜单
        } else { // 底部侧滑菜单未显示
            boolean isSerialsTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT); // 查询串口文本模式是否开启
            if (isSerialsTxt) return; // 串口文本模式开启时不允许打开底部侧滑菜单
            mainViewGroup.hideAllDialogSlip(); // 先隐藏所有弹窗和侧滑菜单
            mainViewGroup.openSlip(MainViewGroup.BOTTOMSLIP); // 打开底部侧滑菜单
        }
    }

    /**
     * 点击测量按钮
     */
    public void ClickMeasureBtn(){
        mainViewGroup.OnBtnMeasureClick(); // 委托给MainViewGroup处理测量按钮点击
    }

    /**
     * 点击通道列表按钮
     */
    public void ClickChannelList(){
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_CHANNEL_LIST_CLICK,new Object()); // 通过事件总线发送通道列表点击事件

    }

    /**
     * 点击测量菜单，切换顶部侧滑菜单到测量页面
     * <p>如果顶部侧滑菜单未打开，则先切换到测量页面再打开；如果已打开且当前是测量页面则关闭，否则切换到测量页面</p>
     */
    public void clickMeasureMenu() {
        if (topSlipMenuBar_Quick == null) { // 顶部菜单引用为空则查找
            topSlipMenuBar_Quick = (TopLayoutPopWindow) mainViewGroup.findViewById(R.id.topSlipMenuBar_Quick); // 查找顶部快捷菜单
        }
        if (!topSlipMenuBar_Quick.isCurYTMode()) { // 当前不是Y-T模式则不处理
            return; // 直接返回
        }
        mainViewGroup.hideAllDialogsButDialogOkCancel(); // 隐藏除确认/取消对话框外的所有对话框
        if (!mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)) { // 顶部侧滑菜单未显示
            //如果通道选择框显示则关闭之
            ExternalKeysManager.get().moveBackChannelsSelect(); // 关闭通道选择框
            mainViewGroup.hideAllDialogSlip(); // 隐藏所有弹窗和侧滑菜单
            //上菜单页面菜单选中measure页面
            topSlipMenuBar_Quick.showLayoutMeasure(); // 切换顶部菜单到测量页面
            new Handler().postDelayed(new Runnable() { // 延时200ms后打开顶部侧滑菜单
                @Override
                public void run() {
                    //由于是按键反应，所以打开上菜单的同时需要把预选框也选中上菜单
                    mainViewGroup.openSlip(MainViewGroup.TOPSLIP); // 打开顶部侧滑菜单
                }
            }, 200);//延时是给菜单的内部切换预留个时间完成
        } else { // 顶部侧滑菜单已显示
            if (topSlipMenuBar_Quick.isShowLayoutMeasure()) { // 当前已经是测量页面
                mainViewGroup.hideSlip(MainViewGroup.TOPSLIP); // 关闭顶部侧滑菜单
            } else { // 当前不是测量页面
                topSlipMenuBar_Quick.showLayoutMeasure(); // 切换到测量页面
                ExternalKeysManager.get().showViewPlace(ExternalKeysManager.LISTTYPE_TOP); // 显示顶部预选框位置
            }
        }
    }

    /**
     * 点击触发菜单，切换顶部侧滑菜单到触发页面
     * <p>如果顶部侧滑菜单未打开，则先切换到触发页面再打开；如果已打开且当前是触发页面则关闭，否则切换到触发页面</p>
     */
    public void clickTriggerMenu() {
        if (topSlipMenuBar_Quick == null) { // 顶部菜单引用为空则查找
            topSlipMenuBar_Quick = (TopLayoutPopWindow) mainViewGroup.findViewById(R.id.topSlipMenuBar_Quick); // 查找顶部快捷菜单
        }
        if (!topSlipMenuBar_Quick.isCurYTMode()) { // 当前不是Y-T模式则不处理
            return; // 直接返回
        }
        mainViewGroup.hideAllDialogsButDialogOkCancel(); // 隐藏除确认/取消对话框外的所有对话框
        if (!mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)) { // 顶部侧滑菜单未显示
            //如果通道选择框显示则关闭之
            ExternalKeysManager.get().moveBackChannelsSelect(); // 关闭通道选择框
            mainViewGroup.hideAllDialogSlip(); // 隐藏所有弹窗和侧滑菜单
            //上菜单页面菜单选中trigger页面
            topSlipMenuBar_Quick.showLayoutTrigger(); // 切换顶部菜单到触发页面
            new Handler().postDelayed(new Runnable() { // 延时200ms后打开顶部侧滑菜单
                @Override
                public void run() {
                    //由于是按键反应，所以打开上菜单的同时需要把预选框也选中上菜单
                    mainViewGroup.openSlip(MainViewGroup.TOPSLIP); // 打开顶部侧滑菜单
                }
            }, 200);//延时是给菜单的内部切换预留个时间完成
        } else { // 顶部侧滑菜单已显示
            if (topSlipMenuBar_Quick.isShowLayoutTrigger()) { // 当前已经是触发页面
                mainViewGroup.hideSlip(MainViewGroup.TOPSLIP); // 关闭顶部侧滑菜单
            } else { // 当前不是触发页面
                topSlipMenuBar_Quick.showLayoutTrigger(); // 切换到触发页面
                ExternalKeysManager.get().showViewPlace(ExternalKeysManager.LISTTYPE_TOP); // 显示顶部预选框位置
            }
        }
    }

    /**
     * 点击触发边沿切换
     * <p>XY模式下不执行此操作</p>
     */
    public void clickTriggerEdge(){
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) return; // XY模式下不处理
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_EDGE,true); // 通过事件总线发送触发边沿切换事件
    }

    /**
     * 切换触发通用模式
     * <p>XY模式下不执行此操作</p>
     */
    public void changeTriggerCommonMode() {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) return; // XY模式下不处理
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_MODE, new Object()); // 通过事件总线发送触发模式切换事件
    }

    /**
     * 强制触发一次
     * <p>XY模式下不执行此操作</p>
     */
    public void forceTrigger() {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) return; // XY模式下不处理
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_FORCE, new Object()); // 通过事件总线发送强制触发事件
    }

    /**
     * 切换触发源通道
     * <p>XY模式下不执行此操作</p>
     *
     * @param isTop true表示向上切换触发源，false表示向下切换触发源
     */
    public void clickTriggerSource(boolean isTop) {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) return; // XY模式下不处理
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_LEVEL, // 通过事件总线发送触发电平消息
                isTop ? new ExternalKeysMsgLevel(ExternalKeysMsgLevel.TYPE_TRIGGER_SOURCEUP, 1) // 向上切换触发源
                        : new ExternalKeysMsgLevel(ExternalKeysMsgLevel.TYPE_TRIGGER_SOURCEDOWN, 1)); // 向下切换触发源
    }

    /**
     * 将触发电平移至中心位置
     * <p>XY模式下不执行此操作</p>
     */
    public void clickTriggerLevelCenter() {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) return; // XY模式下不处理
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_LEVEL, // 通过事件总线发送触发电平消息
                new ExternalKeysMsgLevel(ExternalKeysMsgLevel.TYPE_TRIGGER_MOVECENTER, 1)); // 触发电平移至中心
    }

    /**
     * 移动触发电平
     * <p>XY模式下不执行此操作</p>
     *
     * @param isUp  true表示向上移动，false表示向下移动
     * @param count 移动步数
     */
    public void moveTriggerLevel(final boolean isUp, int count) {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) return; // XY模式下不处理
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_LEVEL, // 通过事件总线发送触发电平消息
                isUp ? new ExternalKeysMsgLevel(ExternalKeysMsgLevel.TYPE_TRIGGER_MOVEUP, count) // 向上移动触发电平
                        : new ExternalKeysMsgLevel(ExternalKeysMsgLevel.TYPE_TRIGGER_MOVEDOMN, count)); // 向下移动触发电平
    }

    /**
     * 切换数值通道源
     * <p>XY模式下不执行此操作</p>
     *
     * @param isTop true表示向上切换数值通道源，false表示向下切换数值通道源
     */
    public void clickValueSource(boolean isTop) {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) return; // XY模式下不处理
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_LEVEL, // 通过事件总线发送电平消息
                isTop ? new ExternalKeysMsgLevel(ExternalKeysMsgLevel.TYPE_VALUE_SOURCEUP, 1) // 向上切换数值通道源
                        : new ExternalKeysMsgLevel(ExternalKeysMsgLevel.TYPE_VALUE_SOURCEDOWN, 1)); // 向下切换数值通道源
    }

    /**
     * 将数值电平移至中心位置
     * <p>XY模式下不执行此操作</p>
     */
    public void clickValueLevelCenter() {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) return; // XY模式下不处理
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_LEVEL, // 通过事件总线发送电平消息
                new ExternalKeysMsgLevel(ExternalKeysMsgLevel.TYPE_VALUE_MOVECENTER, 1)); // 数值电平移至中心
    }

    /**
     * 移动数值电平
     * <p>XY模式下不执行此操作</p>
     *
     * @param isUp  true表示向上移动，false表示向下移动
     * @param count 移动步数
     */
    public void moveValueLevel(final boolean isUp, int count) {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) return; // XY模式下不处理
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_LEVEL, // 通过事件总线发送电平消息
                isUp ? new ExternalKeysMsgLevel(ExternalKeysMsgLevel.TYPE_VALUE_MOVEUP, count) // 向上移动数值电平
                        : new ExternalKeysMsgLevel(ExternalKeysMsgLevel.TYPE_VALUE_MOVEDOMN, count)); // 向下移动数值电平
    }


    /**
     * 通道游标操作
     */
    public void channelVernier(){
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_VERNIER, true); // 通过事件总线发送通道游标事件
    }

    /**
     * 水平游标操作
     */
    public void timeVernier(){
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_HORIZONTAL_VERNIER, true); // 通过事件总线发送水平游标事件
    }


    /**
     * 点击右侧指定物理通道的侧滑菜单
     *
     * @param channel ExternalKeysMsgChannel.CH1--ExternalKeysMsgChannel.CH8
     */
    public void clickRightChannel(int channel) {
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_CHANNEL, new ExternalKeysMsgChannel(channel)); // 通过事件总线发送通道点击消息
        //mainRight切换为channel页面
        RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_CHANNELS); // 切换右侧菜单到通道页面

    }

    /**
     * 点击右侧指定数学通道的侧滑菜单
     * TODO 匹配新的math key协议
     *
     * @param mathChannel ExternalKeysMsgChannel.MATH1--ExternalKeysMsgChannel.MATH8
     */
    public void clickRightMath(int mathChannel) {
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_CHANNEL, new ExternalKeysMsgChannel(mathChannel)); // 通过事件总线发送数学通道点击消息
        //mainRight切换为other页面
        RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 切换右侧菜单到其他页面
    }

    /**
     * 点击右侧指定参考通道的侧滑菜单
     * TODO 匹配新的ref key协议
     *
     * @param refChannel ExternalKeysMsgChannel.REF1--ExternalKeysMsgChannel.REF8
     */
    public void clickRightRef(int refChannel) {
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_CHANNEL, new ExternalKeysMsgChannel(refChannel)); // 通过事件总线发送参考通道点击消息
        //mainRight切换为other页面
        RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 切换右侧菜单到其他页面
    }

    /**
     * 点击右侧指定串口通道的侧滑菜单
     *
     * @param serialsNumber 串口编号 1 - 4
     */
    public void clickRightSerials(int serialsNumber) {
        int keysIndex = ExternalKeysMsgChannel.S1; // 默认为S1
        if (serialsNumber == 1) { // 串口编号1
            keysIndex = ExternalKeysMsgChannel.S1; // 映射为S1
        } else if (serialsNumber == 2) { // 串口编号2
            keysIndex = ExternalKeysMsgChannel.S2; // 映射为S2
        } else if (serialsNumber == 3) { // 串口编号3
            keysIndex = ExternalKeysMsgChannel.S3; // 映射为S3
        } else if (serialsNumber == 4) { // 串口编号4
            keysIndex = ExternalKeysMsgChannel.S4; // 映射为S4
        }
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_CHANNEL, new ExternalKeysMsgChannel(keysIndex)); // 通过事件总线发送串口通道点击消息
        //mainRight切换为other页面
        RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 切换右侧菜单到其他页面
    }

    //打开 添加数学通道 菜单
    /**
     * 打开添加数学通道菜单
     */
    public void clickAddRightMath() {
        RxBus.getInstance().post(RxEnum.MQ_MSG_OPEN_ADD_CHANNEL_MENU, 0);//tabIndex=0，选中数学通道标签页
    }

    //打开 添加参考通道 菜单
    /**
     * 打开添加参考通道菜单
     */
    public void clickAddRightRef() {
        RxBus.getInstance().post(RxEnum.MQ_MSG_OPEN_ADD_CHANNEL_MENU, 1);//tabIndex=1，选中参考通道标签页
    }

    //打开 添加串口通道 菜单
    /**
     * 打开添加串口通道菜单
     */
    public void clickAddRightSerials() {
        RxBus.getInstance().post(RxEnum.MQ_MSG_OPEN_ADD_CHANNEL_MENU, 2);//tabIndex=2，选中串口通道标签页
    }

    /**
     * 点击Home键，回到主界面
     */
    public void clickHome() {
        RxBus.getInstance().post(RxEnum.MCUTOARM, MCUTOARM_HOME); // 通过事件总线发送Home按键事件
    }

    /**
     * 点击单次触发键
     */
    public void clickSeq() {
        RxBus.getInstance().post(RxEnum.MCUTOARM, MCUTOARM_SEQ); // 通过事件总线发送单次触发按键事件
    }

    /**
     * 点击自动设置键
     */
    public void clickAuto() {
        RxBus.getInstance().post(RxEnum.MCUTOARM, MCUTOARM_AUTO); // 通过事件总线发送自动设置按键事件
    }

    /**
     * 点击运行/停止键
     */
    public void clickRunStop() {
        RxBus.getInstance().post(RxEnum.MCUTOARM, MCUTOARM_RUNSTOP); // 通过事件总线发送运行/停止按键事件
    }

    /**
     * 点击截屏键
     */
    public void clickScreenCapture() {
        RxBus.getInstance().post(RxEnum.MCUTOARM, MCUTOARM_SCREENCAPTURE); // 通过事件总线发送截屏按键事件
    }

    /**
     * 点击快速保存键
     */
    public void clickQuickSave() {
        RxBus.getInstance().post(RxEnum.MCUTOARM, MCUTOARM_QUICKSAVE); // 通过事件总线发送快速保存按键事件
    }

    /**
     * 点击缩放键
     * <p>XY模式下缩放功能不可用</p>
     */
    public void clickZoom() {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) {//xy模式下，zoom功能不能使用
            return; // XY模式下不处理
        }
        RxBus.getInstance().post(RxEnum.MCUTOARM, MCUTOARM_ZOOM); // 通过事件总线发送缩放按键事件
    }

    /**
     * 查询串口文本候选词界面是否可见
     *
     * @return true表示串口文本候选词界面可见，false表示不可见
     */
    public boolean isSerialsWordVisible(){
        return CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT); // 从缓存中查询串口文本模式状态
    }

    /**
     * 移动时基位置（水平偏移）
     * <p>XY模式下不执行此操作</p>
     *
     * @param isRight true表示向右移动，false表示向左移动
     * @param count   移动像素数
     * @return true表示操作成功，false表示当前模式不支持
     */
    public boolean moveTimeBasePosition(boolean isRight, int count) {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) return false; // XY模式下不支持
        if (isRight) { // 向右移动
            TriggerTimebase.getInstance().movePix(count); // 正向移动时基位置
        } else { // 向左移动
            TriggerTimebase.getInstance().movePix(count * -1); // 反向移动时基位置
        }
        return true; // 操作成功
    }

    /**
     * 将时基位置重置到50%中心位置
     * <p>XY模式下不执行此操作</p>
     *
     * @return true表示操作成功，false表示当前模式不支持
     */
    public boolean moveTimeBasePosition50Percent() {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) return false; // XY模式下不支持
        TriggerTimebase.getInstance().rstX_50percentt(); // 重置时基位置到50%
        return true; // 操作成功
    }

    /**
     * 调整时基刻度（水平扫速）
     *
     * @param isLeft true表示减小（向左），false表示增大（向右）
     * @param count  调整步数
     */
    public void moveTimeBaseScale(boolean isLeft, int count) {
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_TIMEBASE, new ExternalKeysMsgTimeBase(isLeft, count)); // 通过事件总线发送时基刻度调整消息
    }

    private MainLayoutCenterChannel channels; // 中间通道布局引用

    /**
     * 打开当前选中通道的右侧侧滑菜单
     */
    public void openRightCurSlip() {
        if (channels == null) { // 通道布局引用为空则查找
            channels = (MainLayoutCenterChannel) mainViewGroup.findViewById(R.id.mainLayoutCenterChannels); // 查找中间通道布局
        }
        int index = channels.getChannelSelectIndex(); // 获取当前选中通道的索引
        openRightSlip(index + 1); // 索引+1后打开对应的右侧侧滑菜单
    }

    /**
     * 根据通道索引打开对应的右侧侧滑菜单
     * <p>如果该侧滑菜单已显示则关闭，否则关闭其他所有侧滑菜单后打开</p>
     *
     * @param index 通道索引（使用TChan常量：Ch1-Ch8, Math1-Math8, R1-R8, S1-S2）
     */
    public void openRightSlip(int index) {
        if (index != -1) { // 索引有效
            @MainViewGroup.Slip int slip = MainViewGroup.RIGHTSLIP_CH1; // 默认为CH1侧滑菜单
            switch (index) { // 根据通道索引匹配侧滑菜单类型
                case TChan.Ch1: // CH1通道
                    slip = MainViewGroup.RIGHTSLIP_CH1; // 匹配CH1侧滑菜单
                    break;
                case TChan.Ch2: // CH2通道
                    slip = MainViewGroup.RIGHTSLIP_CH2; // 匹配CH2侧滑菜单
                    break;
                case TChan.Ch3: // CH3通道
                    slip = MainViewGroup.RIGHTSLIP_CH3; // 匹配CH3侧滑菜单
                    break;
                case TChan.Ch4: // CH4通道
                    slip = MainViewGroup.RIGHTSLIP_CH4; // 匹配CH4侧滑菜单
                    break;
                case TChan.Ch5: // CH5通道
                    slip = MainViewGroup.RIGHTSLIP_CH5; // 匹配CH5侧滑菜单
                    break;
                case TChan.Ch6: // CH6通道
                    slip = MainViewGroup.RIGHTSLIP_CH6; // 匹配CH6侧滑菜单
                    break;
                case TChan.Ch7: // CH7通道
                    slip = MainViewGroup.RIGHTSLIP_CH7; // 匹配CH7侧滑菜单
                    break;
                case TChan.Ch8: // CH8通道
                    slip = MainViewGroup.RIGHTSLIP_CH8; // 匹配CH8侧滑菜单
                    break;
                case TChan.Math1: // MATH1数学通道
                    slip = MainViewGroup.RIGHTSLIP_MATH1; // 匹配MATH1侧滑菜单
                    break;
                case TChan.Math2: // MATH2数学通道
                    slip = MainViewGroup.RIGHTSLIP_MATH2; // 匹配MATH2侧滑菜单
                    break;
                case TChan.Math3: // MATH3数学通道
                    slip = MainViewGroup.RIGHTSLIP_MATH3; // 匹配MATH3侧滑菜单
                    break;
                case TChan.Math4: // MATH4数学通道
                    slip = MainViewGroup.RIGHTSLIP_MATH4; // 匹配MATH4侧滑菜单
                    break;
                case TChan.Math5: // MATH5数学通道
                    slip = MainViewGroup.RIGHTSLIP_MATH5; // 匹配MATH5侧滑菜单
                    break;
                case TChan.Math6: // MATH6数学通道
                    slip = MainViewGroup.RIGHTSLIP_MATH6; // 匹配MATH6侧滑菜单
                    break;
                case TChan.Math7: // MATH7数学通道
                    slip = MainViewGroup.RIGHTSLIP_MATH7; // 匹配MATH7侧滑菜单
                    break;
                case TChan.Math8: // MATH8数学通道
                    slip = MainViewGroup.RIGHTSLIP_MATH8; // 匹配MATH8侧滑菜单
                    break;

                case TChan.R1: // REF1参考通道
                    slip = MainViewGroup.RIGHTSLIP_REF1; // 匹配REF1侧滑菜单
                    break;
                case TChan.R2: // REF2参考通道
                    slip = MainViewGroup.RIGHTSLIP_REF2; // 匹配REF2侧滑菜单
                    break;
                case TChan.R3: // REF3参考通道
                    slip = MainViewGroup.RIGHTSLIP_REF3; // 匹配REF3侧滑菜单
                    break;
                case TChan.R4: // REF4参考通道
                    slip = MainViewGroup.RIGHTSLIP_REF4; // 匹配REF4侧滑菜单
                    break;
                case TChan.R5: // REF5参考通道
                    slip = MainViewGroup.RIGHTSLIP_REF5; // 匹配REF5侧滑菜单
                    break;
                case TChan.R6: // REF6参考通道
                    slip = MainViewGroup.RIGHTSLIP_REF6; // 匹配REF6侧滑菜单
                    break;
                case TChan.R7: // REF7参考通道
                    slip = MainViewGroup.RIGHTSLIP_REF7; // 匹配REF7侧滑菜单
                    break;
                case TChan.R8: // REF8参考通道
                    slip = MainViewGroup.RIGHTSLIP_REF8; // 匹配REF8侧滑菜单
                    break;
                case TChan.S1: // S1串口通道
                    slip = MainViewGroup.RIGHTSLIP_S1; // 匹配S1侧滑菜单
                    break;
                case TChan.S2: // S2串口通道
                    slip = MainViewGroup.RIGHTSLIP_S2; // 匹配S2侧滑菜单
                    break;
            }
            if (mainViewGroup.isSlipShow(slip)) { // 该侧滑菜单已显示
                mainViewGroup.hideSlip(slip); // 关闭该侧滑菜单
            } else { // 该侧滑菜单未显示
                mainViewGroup.hideAllDialogSlip(); // 先隐藏所有弹窗和侧滑菜单
                mainViewGroup.openSlip(slip); // 打开目标侧滑菜单
            }
        }
    }

    /**
     * 移动当前通道的垂直位置
     *
     * @param isDown true表示向下移动，false表示向上移动
     * @param count  移动像素数
     */
    public void moveChannelPosition(boolean isDown, int count) {
        CursorManage.getInstance().setCursorTrace(true); // 开启光标跟踪模式
        if (isDown) { // 向下移动
            WaveManage.get().movePix(count * -1); // 反向移动波形像素位置
        } else { // 向上移动
            WaveManage.get().movePix(count); // 正向移动波形像素位置
        }
        CursorManage.setCursorByScaleTrace(); // 根据刻度跟踪更新光标位置
        CursorManage.getInstance().setCursorTrace(false); // 关闭光标跟踪模式
    }

    private ExternalKeysMsgVScale msgVScale = new ExternalKeysMsgVScale(); // 垂直刻度消息对象，复用以减少GC



    /**
     * 调整当前通道的垂直刻度（V/div）
     * <p>会检查当前刻度是否已达到最大/最小档位限制</p>
     *
     * @param isAdd true表示增大刻度，false表示减小刻度
     * @param count 调整步数
     */
    public void moveChannelScale(boolean isAdd,int count) {
        Log.d("TAG","调整-------------------------m" +isAdd + "c" + count); // 输出调试日志

        if (channels == null) { // 通道布局引用为空则查找
            channels = (MainLayoutCenterChannel) mainViewGroup.findViewById(R.id.mainLayoutCenterChannels); // 查找中间通道布局
        }

        int index = channels.getChannelSelectIndex(); // 获取当前选中通道的索引
        if (index == -1) return; // 无选中通道则直接返回

        Channel channel = ChannelFactory.getDynamicChannel(index); // 通过通道工厂获取通道实例
        if(channel != null){ // 通道实例有效
            double v = channel.getVScaleVal()/channel.getProbeRate(); // 计算当前实际V/div值（考虑探头比）
            if( isAdd && v >= VerticalAxis.getScaleIdValById(channel.getMaxGear())){ // 增大且已达到最大档位
                return; // 已到最大档位，不再增大
            }else if (!isAdd && v <= VerticalAxis.getScaleIdValById(channel.getMinGear())){ // 减小且已达到最小档位
                return; // 已到最小档位，不再减小
            }
        }

        msgVScale.setChIndex(index); // 设置消息中的通道索引
        msgVScale.setAdd(isAdd); // 设置消息中的增减方向
        msgVScale.setCount(count); // 设置消息中的调整步数
        RxBus.getInstance().post(RxEnum.EXTERNALKEYS_VSCALE, msgVScale); // 通过事件总线发送垂直刻度调整消息
        if (TChan.isChan(TChan.toUiChNo(index))) { // 判断是否为物理通道
            RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_CHANNELS); // 切换右侧菜单到通道页面
        } else if (TChan.isMathToR8(TChan.toUiChNo(index))) { // 判断是否为数学/参考通道
            RxBus.getInstance().post(RxEnum.SLIPRIGHTMENU, MainHolderBottom.SLIPRIGHTMENU_OTHERS); // 切换右侧菜单到其他页面
        }
        if (ChannelFactory.isChActivate(index)) { // 通道已激活
            CursorManage.getInstance().setCursorTrace(true); // 开启光标跟踪模式
            CursorManage.getInstance().curChannelMove(); // 移动当前通道的光标
            CursorManage.getInstance().setCursorTrace(false); // 关闭光标跟踪模式
        }
    }

    /**
     * 切换锁屏回到主页的Toast提示
     * <p>通过Handler发送消息来显示或隐藏Toast，避免在非UI线程操作UI</p>
     *
     * @param isOpen true表示打开Toast提示，false表示关闭Toast提示
     */
    public void changeGoHomeToast(boolean isOpen) {
        handler.sendEmptyMessage(isOpen ? MSG_OPENTOAST : MSG_CLOSETOAST); // 根据参数发送打开或关闭Toast消息
//        if (isOpen) {
//            DToast.get().show(R.string.msgForGoHomeWhenLockScreen);
//        } else {
//            DToast.get().hide();
//        }
    }

    private TextView tvMsgGoHome; // 回到主页提示文本视图
    private static final int MSG_OPENTOAST = 55; // 打开Toast的消息常量
    private static final int MSG_CLOSETOAST = 56; // 关闭Toast的消息常量

    private static final int MSG_SCALE_DELAY = 0x20241125; // 刻度延时消息常量
    /**
     * Handler消息处理器，处理Toast显示/隐藏和刻度延时调整
     * <p>MSG_OPENTOAST: 显示锁屏回到主页提示，3秒后自动关闭；若在此期间收到关闭消息则直接回到主页</p>
     * <p>MSG_CLOSETOAST: 隐藏Toast</p>
     * <p>MSG_SCALE_DELAY: 延时执行刻度调整</p>
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg); // 调用父类处理
            switch (msg.what){ // 根据消息类型分发处理
                case MSG_OPENTOAST:{ // 打开Toast消息
                    if (handler.hasMessages(MSG_CLOSETOAST)) { // 已有待关闭的消息（用户再次按下）
                        DToast.get().hide(); // 隐藏Toast
                        ExternalKeysCommand.get().clickHome(); // 直接执行回到主页操作
                    } else { // 首次按下
                        DToast.get().show(R.string.msgForGoHomeWhenLockScreen); // 显示锁屏回到主页提示
                        sendEmptyMessageDelayed(MSG_CLOSETOAST, 3000); // 3秒后自动关闭Toast
                    }
                }
                break; // 结束MSG_OPENTOAST处理
                case MSG_CLOSETOAST: // 关闭Toast消息
                    DToast.get().hide(); // 隐藏Toast
                    break; // 结束MSG_CLOSETOAST处理
                case MSG_SCALE_DELAY: // 刻度延时消息
                    Log.d("handler","调整-------------------------scale delay"); // 输出调试日志
                    moveChannelScale(false,0); // 执行刻度调整（count=0表示仅刷新）
                    break; // 结束MSG_SCALE_DELAY处理
            }

        }
    };
}
