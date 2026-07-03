package com.micsig.tbook.tbookscope.structdata; // 外部按键UI处理类所在包

import android.graphics.Rect; // 矩形区域类，用于描述视图位置和尺寸
import android.view.View; // Android基础视图类
import android.view.ViewGroup; // 视图容器基类
import android.widget.AbsoluteLayout; // 绝对布局，用于定位子视图
import android.widget.CheckBox; // 复选框控件

import com.micsig.tbook.scope.ScopeBase; // 示波器基础类，提供坐标转换系数等
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量单例，存储波形区域等共享数据
import com.micsig.tbook.tbookscope.MainViewGroup; // 主视图容器，管理所有子视图
import com.micsig.tbook.tbookscope.R; // 资源ID引用类
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterChannel; // 通道列表中心布局
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterSegmented; // 分段中心布局
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysProtocol; // 外部按键协议常量定义
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysSimulateClick; // 外部按键模拟点击工具
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具类，提供视图矩形获取等方法
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类，读写配置项
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 光标管理器，获取当前选中光标
import com.micsig.tbook.ui.main.AnimationView; // 动画视图，用于焦点高亮显示
import com.micsig.tbook.ui.util.TBookUtil; // TBook工具类
import com.micsig.tbook.ui.wavezone.TChan; // 通道常量定义，包含光标行列标识

/*
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：外部按键子系统 → UI层                                           │
 * │ 核心职责：将外部按键事件映射为界面交互效果（焦点高亮、模拟点击、拖拽等）       │
 * │ 架构设计：单例模式，作为外部按键逻辑层与UI视图层之间的桥梁                   │
 * │ 数据流向：ExternalKeysProtocol(协议) → ExternalKeysUI(本类) → View(视图)   │
 * │ 依赖关系：MainViewGroup, ExternalKeysSimulateClick, CursorManage, CacheUtil │
 * │ 使用场景：外部旋钮/按键操作时，界面焦点框跟随、模拟触屏点击、通道窗口联动    │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * Created by liwb on 2017/12/11.
 * 外部按键 ,界面处理部分
 */
public class ExternalKeysUI {

    //region 单例模式
    /** 单例持有者，利用静态内部类实现懒加载线程安全单例 */
    private static class ExternalKeysUIHolder {
        public static final ExternalKeysUI instance = new ExternalKeysUI(); // 静态常量单例实例
    }

    /**
     * 获取ExternalKeysUI单例实例
     * @return ExternalKeysUI唯一实例
     */
    public static ExternalKeysUI getInstance() {
        return ExternalKeysUI.ExternalKeysUIHolder.instance; // 返回静态内部类持有的单例
    }
    //endregion

    private ExternalKeysSimulateClick click = new ExternalKeysSimulateClick(); // 模拟点击执行器
    private MainViewGroup mainViewGroup; // 主视图容器引用
    private MainLayoutCenterChannel channelWindow; // 通道列表窗口引用
    private MainLayoutCenterSegmented segmentWindow; // 分段窗口引用
    private CheckBox channelBtn; // 通道切换按钮引用
    private AnimationView focusControl, focusControlBack; // 焦点框视图（前景/背景）
    private View zoomUpLayout; // 波形缩放布局引用
//    private TextureView textureView;

    /**
     * 设置主视图容器，并从中获取焦点控制视图的引用
     * @param mainViewGroup 主视图容器
     */
    public void setMainViewGroup(MainViewGroup mainViewGroup) {
        this.mainViewGroup = mainViewGroup; // 保存主视图容器引用
        focusControl = (AnimationView) mainViewGroup.findViewById(R.id.focusControl); // 查找前景焦点框视图
        focusControlBack = (AnimationView) mainViewGroup.findViewById(R.id.focusControlBack); // 查找背景焦点框视图
    }

    /**
     * 判断是否有对话框正在显示
     * @return true表示有对话框显示中
     */
    public boolean isDialogsShow() {
        return mainViewGroup.isDialogsShow(); // 委托主视图容器判断对话框显示状态
    }

    /**
     * 根据条件设置焦点框的可见性
     * 当有滑块、对话框、通道列表、分段布局或串口操作布局可见时，显示焦点框；否则隐藏
     * @param visible 是否期望可见
     */
    public void setFocusViewVisible(boolean visible) {
        if (visible && (mainViewGroup.isSlipShow() || mainViewGroup.isDialogsShow() // 可见且有滑块或对话框显示
                || mainViewGroup.getChannelsLayout().getVisibility()==View.VISIBLE // 或通道列表可见
                || mainViewGroup.getCenterSegmentedLayout().getVisibility()==View.VISIBLE // 或分段布局可见
                || mainViewGroup.getSerialWorkLayout().getVisibility()==View.VISIBLE) // 或串口操作布局可见
                ) {
            focusControl.setVisibility(View.VISIBLE); // 显示前景焦点框
        } else {
            focusControl.setVisibility(View.GONE); // 隐藏前景焦点框
            focusControlBack.setVisibility(View.GONE); // 同时隐藏背景焦点框
        }
    }

    /**
     * 设置背景焦点框的可见性
     * @param visible true显示，false隐藏
     */
    public void setFocusControlBackVisible(boolean visible) {
        if (visible) {
            focusControlBack.setVisibility(View.VISIBLE); // 显示背景焦点框
        } else {
            focusControlBack.setVisibility(View.GONE); // 隐藏背景焦点框
        }
    }

    /**
     * 关闭模拟点击模式（切换为可见选择模式）
     */
    public void setVisibleSelectView() {
        click.setImitateClick(false); // 设置模拟点击标志为false，表示使用可见选择模式
    }

    /**
     * 判断当前是否处于模拟点击模式
     * @return true表示模拟点击模式，false表示可见选择模式
     */
    public boolean isVisibleSelectView() {
        return click.isImitateClick(); // 返回模拟点击标志状态
    }

    /**
     * 外部按键拖拽下移操作
     * @param rightWard true表示向右拖拽，false表示向左拖拽
     */
    public void onDrag_downMove(boolean rightWard) {
        checkChannelWindow(710, 300); // 检查通道窗口是否遮挡拖拽区域，若遮挡则隐藏
        if (rightWard) {
            click.drag_downMove(710, 300, 710, 301); // 向右拖拽：Y坐标+1
        } else {
            click.drag_downMove(710, 300, 710, 299); // 向左拖拽：Y坐标-1
        }
    }

    /**
     * 判断前景焦点框是否可见
     * @return true表示可见
     */
    public boolean isVisibleFocusControl() {
        return focusControl.getVisibility() == View.VISIBLE; // 前景焦点框可见性判断
    }

    /**
     * 判断背景焦点框是否可见
     * @return true表示可见
     */
    public boolean isVisibleFocusControlBack() {
        return focusControlBack.getVisibility() == View.VISIBLE; // 背景焦点框可见性判断
    }

    /**
     * 根据背景状态显示对应的焦点框位置和大小
     * 支持光标、通道列表、分段、缩放四种背景状态
     * @param backState 背景状态常量，定义在ExternalKeysProtocol中
     */
    public void showBackStateFocus(int backState) {
        Rect rect = null; // 焦点框目标矩形区域
        if (backState == ExternalKeysProtocol.BACKSTATE_CURSOR) { // 光标状态
            focusControlBack.setVisibility(View.VISIBLE); // 显示背景焦点框
            rect = getCurCursorRect(); // 获取当前光标所在矩形
        } else if (backState == ExternalKeysProtocol.BACKSTATE_CHLIST) { // 通道列表状态
//            if (focusControlBack.getVisibility() == View.VISIBLE) {
//                focusControlBack.setVisibility(View.GONE);
//            } else {
//                focusControlBack.setVisibility(View.VISIBLE);
//            }
            focusControlBack.setVisibility(View.VISIBLE); // 显示背景焦点框
            rect = getChListRect(); // 获取通道列表窗口矩形
            rect.set(rect.left-2,rect.top-2,rect.right+2,rect.bottom+2); // 矩形四周扩展2像素，留出焦点框边距
        }else if (backState==ExternalKeysProtocol.BACKSTATE_SEGMENT){ // 分段状态
//            if (focusControlBack.getVisibility()==View.VISIBLE){
//                focusControlBack.setVisibility(View.GONE);
//            }else {
//                focusControlBack.setVisibility(View.VISIBLE);
//            }

            focusControlBack.setVisibility(View.VISIBLE); // 显示背景焦点框
            rect=getSegmentRect(); // 获取分段窗口矩形
            rect.set(rect.left-2,rect.top-2,rect.right+2,rect.bottom+2); // 矩形四周扩展2像素，留出焦点框边距
        }
        else if (backState == ExternalKeysProtocol.BACKSTATE_ZOOMUP) { // 缩放状态
            focusControlBack.setVisibility(View.VISIBLE); // 显示背景焦点框
            rect = getZoomUpRect(); // 获取缩放布局矩形
        } else { // 未知状态
            focusControlBack.setVisibility(View.GONE); // 隐藏背景焦点框
        }
        if (rect != null) { // 矩形有效时，更新焦点框位置和尺寸
            focusControlBack.setX(rect.left); // 设置焦点框X坐标
            focusControlBack.setY(rect.top); // 设置焦点框Y坐标
            ViewGroup.LayoutParams layoutParams = focusControlBack.getLayoutParams(); // 获取焦点框布局参数
            layoutParams.width = rect.width(); // 设置焦点框宽度
            layoutParams.height = rect.height(); // 设置焦点框高度
            focusControlBack.setLayoutParams(layoutParams); // 应用更新后的布局参数
            focusControlBack.bringToFront(); // 将焦点框置于最前显示
        }
    }

    /**
     * 获取通道列表窗口的屏幕矩形区域
     * 延迟初始化channelWindow引用
     * @return 通道列表窗口矩形
     */
    private Rect getChListRect() {
        if (channelWindow == null) { // 通道窗口引用未初始化
            channelWindow = (MainLayoutCenterChannel) mainViewGroup.findViewById(R.id.mainLayoutCenterChannels); // 从主视图查找通道窗口
        }
        return Tools.getViewRect(channelWindow); // 返回通道窗口的屏幕矩形
    }

    /**
     * 获取分段窗口的屏幕矩形区域
     * 延迟初始化segmentWindow引用
     * @return 分段窗口矩形
     */
    private Rect getSegmentRect(){
        if (segmentWindow==null){ // 分段窗口引用未初始化
            segmentWindow=(MainLayoutCenterSegmented) mainViewGroup.findViewById(R.id.mainLayoutCenterSegmented); // 从主视图查找分段窗口
        }
        return Tools.getViewRect(segmentWindow); // 返回分段窗口的屏幕矩形
    }

    /**
     * 获取波形缩放布局的屏幕矩形区域
     * 延迟初始化zoomUpLayout引用
     * @return 缩放布局矩形
     */
    private Rect getZoomUpRect() {
        if (zoomUpLayout == null) { // 缩放布局引用未初始化
            zoomUpLayout = (AbsoluteLayout) mainViewGroup.findViewById(R.id.middlebar_wave_zoom); // 从主视图查找缩放布局
        }
        return Tools.getViewRect(zoomUpLayout); // 返回缩放布局的屏幕矩形
    }

    /**
     * 获取当前选中光标的屏幕矩形区域
     * 根据光标类型（竖直/水平）和缩放状态计算焦点框位置
     * @return 光标所在矩形区域，无选中光标时返回空矩形
     */
    private Rect getCurCursorRect() {
//        if (textureView == null) {
//            textureView = (TextureView) mainViewGroup.findViewById(R.id.textureView);
//        }
        if (zoomUpLayout == null) { // 缩放布局引用未初始化
            zoomUpLayout = (AbsoluteLayout) mainViewGroup.findViewById(R.id.middlebar_wave_zoom); // 从主视图查找缩放布局
        }
        int zoomHeight = Tools.getViewRect(zoomUpLayout).height(); // 获取缩放区域高度
//        Rect textureRect = Tools.getViewRect(textureView);
        int topHeight= mainViewGroup.findViewById(R.id.topstatus).getHeight(); // 获取顶部状态栏高度
        int leftBarWidth=0; // 左侧栏宽度，当前设为0
        int rectRight= GlobalVar.get().getMainWave().x+leftBarWidth; // 计算波形区域右边界X坐标
        int rectBottom=GlobalVar.get().getMainWave().y+topHeight; // 计算波形区域下边界Y坐标
        Rect textureRect = new Rect(leftBarWidth, topHeight, rectRight, rectBottom); // 构造波形显示区域矩形
        boolean isZoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM); // 读取缩放开关状态
        int selectCursor = CursorManage.getInstance().getCurrSelectCursor(); // 获取当前选中的光标类型
        int xStart = textureRect.left; // 波形区域左边界X
        int yStart = textureRect.top; // 波形区域上边界Y
        int x1 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_YT_CURSORV_POSITION + TChan.Cursor_col_1); // 读取竖直光标1的X位置
        int x2 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_YT_CURSORV_POSITION + TChan.Cursor_col_2); // 读取竖直光标2的X位置
        double y1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION + TChan.Cursor_row_1); // 读取水平光标1的Y位置
        double y2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION + TChan.Cursor_row_2); // 读取水平光标2的Y位置
        y1 = ScopeBase.changeAccuracy(y1 * ScopeBase.getToUICoff()); // 将光标1的Y坐标转换为UI像素坐标
        y2 = ScopeBase.changeAccuracy(y2 * ScopeBase.getToUICoff()); // 将光标2的Y坐标转换为UI像素坐标
        int halfWidth = 8; // 焦点框半宽，用于光标两侧扩展
        if (selectCursor == TChan.Cursor_col_1) {//竖直方向... 选中竖直光标1
            int top = isZoom ? textureRect.top + zoomHeight : textureRect.top; // 缩放时从缩放区域底部开始，否则从波形顶部开始
            return new Rect(x1 + xStart - halfWidth, top, x1 + xStart + halfWidth, textureRect.bottom); // 返回竖直光标1的焦点矩形
        } else if (selectCursor == TChan.Cursor_col_2) {//竖直方向... 选中竖直光标2
            int top = isZoom ? textureRect.top + zoomHeight : textureRect.top; // 缩放时从缩放区域底部开始
            return new Rect(x2 + xStart - halfWidth, top, x2 + xStart + halfWidth, textureRect.bottom); // 返回竖直光标2的焦点矩形
        } else if (selectCursor == TChan.Cursor_col_3) {//竖直方向... 选中竖直光标3（两光标间区域）
            int top = isZoom ? textureRect.top + zoomHeight : textureRect.top; // 缩放时从缩放区域底部开始
            return new Rect(x1 + xStart - halfWidth, top, x2 + xStart + halfWidth, textureRect.bottom); // 返回两竖直光标之间的焦点矩形
        } else if (selectCursor == TChan.Cursor_row_1) {//水平方向... 选中水平光标1
            int top = (int) Math.round(isZoom ? textureRect.top + zoomHeight + Tools.YT2Zoom(y1) : textureRect.top + y1); // 缩放时映射Y坐标到缩放区域
            return new Rect(xStart, top - halfWidth, textureRect.right, top + halfWidth); // 返回水平光标1的焦点矩形
        } else if (selectCursor == TChan.Cursor_row_2) {//水平方向... 选中水平光标2
            int top = (int) Math.round(isZoom ? textureRect.top + zoomHeight + Tools.YT2Zoom(y2) : textureRect.top + y2); // 缩放时映射Y坐标到缩放区域
            return new Rect(xStart, top - halfWidth, textureRect.right, top + halfWidth); // 返回水平光标2的焦点矩形
        } else if (selectCursor == TChan.Cursor_row_3) {//水平方向... 选中水平光标3（两光标间区域）
            int top = (int) Math.round(isZoom ? textureRect.top + zoomHeight + Tools.YT2Zoom(y1) : textureRect.top + y1); // 计算上光标Y位置
            int bottom = (int) Math.round(isZoom ? textureRect.top + zoomHeight + Tools.YT2Zoom(y2) : textureRect.top + y2); // 计算下光标Y位置
            return new Rect(xStart, top - halfWidth, textureRect.right, bottom + halfWidth); // 返回两水平光标之间的焦点矩形
        }
        return new Rect(0, 0, 0, 0); // 无匹配光标类型时返回空矩形
    }

    /**
     * 模拟点击，如果被channelWindow覆盖，则先隐藏之
     * @param x 点击X坐标
     * @param y 点击Y坐标
     */
    public void onClick(int x, int y) {
        click(x, y); // 调用内部点击方法（含通道窗口遮挡检查）
    }

    /**
     * 模拟点击，但是不隐藏channelWindow
     * @param x 点击X坐标
     * @param y 点击Y坐标
     */
    public void onClickForChannelWindow(int x, int y) {
        click.click(x, y, null); // 直接调用模拟点击，跳过通道窗口遮挡检查
    }


    /**
     * 内部点击实现：先检查通道窗口是否遮挡点击位置，若遮挡则隐藏，再执行模拟点击
     * @param x 点击X坐标
     * @param y 点击Y坐标
     */
    private void click(int x, int y) {
        checkChannelWindow(x, y); // 检查通道窗口是否遮挡点击位置
        click.click(x, y, null); // 执行模拟点击
    }

    /**
     * 检查通道窗口是否遮挡指定坐标点，若遮挡则隐藏通道窗口并取消按钮选中状态
     * @param x 检查点X坐标
     * @param y 检查点Y坐标
     */
    private void checkChannelWindow(int x, int y) {
        if (channelWindow == null || channelBtn == null) { // 通道窗口或按钮引用未初始化
            channelWindow = (MainLayoutCenterChannel) mainViewGroup.findViewById(R.id.mainLayoutCenterChannels); // 查找通道窗口
            channelBtn = (CheckBox) mainViewGroup.findViewById(R.id.current); // 查找通道切换按钮
        }
        if (channelWindow.getVisibility() == View.VISIBLE && channelWindow.containsPoint(x, y)) { // 通道窗口可见且包含目标点
            channelWindow.setVisibility(View.GONE); // 隐藏通道窗口
            channelBtn.setChecked(false); // 取消通道按钮选中状态
        }
    }
}
