package com.micsig.tbook.tbookscope.wavezone.trigger; // 触发线模块包

import android.content.Context; // 导入上下文类
import android.graphics.Bitmap; // 导入Bitmap类，用于图片资源
import android.graphics.Canvas; // 导入Canvas类，用于绘制
import android.graphics.Paint; // 导入Paint类，用于画笔设置
import android.graphics.PorterDuff; // 导入PorterDuff混合模式
import android.graphics.PorterDuffXfermode; // 导入PorterDuffXfermode，用于图像混合
import android.graphics.Rect; // 导入Rect类，用于矩形区域
import android.graphics.Typeface; // 导入Typeface类，用于字体设置

import com.chillingvan.canvasgl.ICanvasGL; // 导入GLCanvas接口，用于OpenGL绘制
import com.micsig.tbook.scope.ScopeBase; // 导入示波器基础参数类
import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量类
import com.micsig.tbook.tbookscope.util.App; // 导入应用上下文工具类
import com.micsig.tbook.ui.util.svg.SvgManager; // 导入SVG管理器，用于创建SVG图片
import com.micsig.tbook.ui.util.svg.SvgNodeInfo; // 导入SVG节点信息类
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 导入工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 导入工作模式管理类
import com.micsig.tbook.ui.R; // 导入资源ID类
import com.micsig.tbook.ui.wavezone.IWave; // 导入波形线接口
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道号常量类

import java.util.ArrayList; // 导入ArrayList
import java.util.Arrays; // 导入Arrays工具类


/**
 * Created by liwb on 2017/5/12.
 */

/*
 * +=============================================================================+
 * |                        TriggerVoltageLine                                   |
 * +=============================================================================+
 * | 模块定位   : 示波器波形显示区域 - 触发电平线（垂直位置指示器）                |
 * | 核心职责   : 管理触发电平线的垂直位置、绘制、状态切换（选中/未选中/上沿/下沿） |
 * | 架构设计   : 实现 ITriggerLine 接口，支持三种显示模式（单线/双线/多线逻辑）   |
 * |            : 使用 Bitmap+Canvas 双缓冲绘制，通过 SVG 动态生成通道颜色图标     |
 * | 数据流向   : 外部设置Y偏移/位置 → 边界校验 → 更新currYPos → 重绘Bitmap →    |
 * |            : GLCanvas渲染显示                                               |
 * | 依赖关系   : ITriggerLine(接口), SvgManager, ScopeBase, GlobalVar,          |
 * |            : WorkModeManage, TChan, ICanvasGL                               |
 * | 使用场景   : 用户拖动触发电平线、切换触发电平通道、上下沿触发模式切换、       |
 * |            : 工作模式切换时电平线位置换算                                    |
 * +=============================================================================+
 */
public class TriggerVoltageLine implements ITriggerLine {
    private static final String TAG = "TriggerVoltageLine"; // 日志标签
    //资源图片
    /***
     * 第一维表示通道号，第二维表示图片类型，第一维的[0]不使用。从1表示与通道号一致
     */
    private Bitmap[][] resBmp = new Bitmap[TChan.MaxLogicChan + 2][6]; // 资源图片二维数组：[通道号][图片类型]
    private static final int trigger_ch = 0; // 普通态（未选中）
    private static final int trigger_ch_current = 1; // 当前选中态
    private static final int trigger_ch_down = 2; // 下沿未选中态
    private static final int trigger_ch_down_current = 3; // 下沿选中态
    private static final int trigger_ch_up = 4; // 上沿未选中态
    private static final int trigger_ch_up_current = 5; // 上沿选中态

    //高低
//    public static final int  VoltageLine_High=1;
//    public static final int  VoltageLine_Low=2;

    public static final int TriggerVoltageLine_Logic_Hight = 0x01; // 逻辑触发-高电平标志
    public static final int TriggerVoltageLine_Logic_Low = 0x02; // 逻辑触发-低电平标志
    public static final int TriggerVoltageLine_Logic_None = 0x03; // 逻辑触发-无（不参与）

    //region  图片
    /**
     * 初始化各通道的SVG资源图片
     * 为每个通道（Ch1~Ch8 + 触发输入）生成6种状态图标
     */
    private void initResBmp() {
        resBmp[TChan.Ch1][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch1), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch1-普通态
        resBmp[TChan.Ch1][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch1), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch1-选中态
        resBmp[TChan.Ch1][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch1), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch1-下沿普通态
        resBmp[TChan.Ch1][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch1), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch1-下沿选中态
        resBmp[TChan.Ch1][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch1), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch1-上沿普通态
        resBmp[TChan.Ch1][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch1), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch1-上沿选中态

        resBmp[TChan.Ch2][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch2), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch2-普通态
        resBmp[TChan.Ch2][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch2), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch2-选中态
        resBmp[TChan.Ch2][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch2), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch2-下沿普通态
        resBmp[TChan.Ch2][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch2), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch2-下沿选中态
        resBmp[TChan.Ch2][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch2), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch2-上沿普通态
        resBmp[TChan.Ch2][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch2), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch2-上沿选中态

        resBmp[TChan.Ch3][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch3), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch3-普通态
        resBmp[TChan.Ch3][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch3), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch3-选中态
        resBmp[TChan.Ch3][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch3), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch3-下沿普通态
        resBmp[TChan.Ch3][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch3), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch3-下沿选中态
        resBmp[TChan.Ch3][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch3), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch3-上沿普通态
        resBmp[TChan.Ch3][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch3), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch3-上沿选中态

        resBmp[TChan.Ch4][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch4), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch4-普通态
        resBmp[TChan.Ch4][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch4), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch4-选中态
        resBmp[TChan.Ch4][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch4), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch4-下沿普通态
        resBmp[TChan.Ch4][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch4), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch4-下沿选中态
        resBmp[TChan.Ch4][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch4), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch4-上沿普通态
        resBmp[TChan.Ch4][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch4), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch4-上沿选中态

        resBmp[TChan.Ch5][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch5), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch5-普通态
        resBmp[TChan.Ch5][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch5), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch5-选中态
        resBmp[TChan.Ch5][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch5), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch5-下沿普通态
        resBmp[TChan.Ch5][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch5), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch5-下沿选中态
        resBmp[TChan.Ch5][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch5), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch5-上沿普通态
        resBmp[TChan.Ch5][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch5), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch5-上沿选中态

        resBmp[TChan.Ch6][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch6), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch6-普通态
        resBmp[TChan.Ch6][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch6), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch6-选中态
        resBmp[TChan.Ch6][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch6), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch6-下沿普通态
        resBmp[TChan.Ch6][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch6), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch6-下沿选中态
        resBmp[TChan.Ch6][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch6), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch6-上沿普通态
        resBmp[TChan.Ch6][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch6), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch6-上沿选中态

        resBmp[TChan.Ch7][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch7), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch7-普通态
        resBmp[TChan.Ch7][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch7), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch7-选中态
        resBmp[TChan.Ch7][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch7), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch7-下沿普通态
        resBmp[TChan.Ch7][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch7), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch7-下沿选中态
        resBmp[TChan.Ch7][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch7), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch7-上沿普通态
        resBmp[TChan.Ch7][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch7), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch7-上沿选中态

        resBmp[TChan.Ch8][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch8), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch8-普通态
        resBmp[TChan.Ch8][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch8), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch8-选中态
        resBmp[TChan.Ch8][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch8), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch8-下沿普通态
        resBmp[TChan.Ch8][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch8), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch8-下沿选中态
        resBmp[TChan.Ch8][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch8), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // Ch8-上沿普通态
        resBmp[TChan.Ch8][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch8), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // Ch8-上沿选中态

        resBmp[TChan.Ch8 + 1][trigger_ch] = SvgManager.createSvg(SvgNodeInfo.getTANormalPaths(), SvgNodeInfo.getTAColors(TChan.Ch8 + 1), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // 触发输入-普通态
        resBmp[TChan.Ch8 + 1][trigger_ch_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentPaths(), SvgNodeInfo.getTAColors(TChan.Ch8 + 1), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // 触发输入-选中态
        resBmp[TChan.Ch8 + 1][trigger_ch_down] = SvgManager.createSvg(SvgNodeInfo.getTANormalDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch8 + 1), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // 触发输入-下沿普通态
        resBmp[TChan.Ch8 + 1][trigger_ch_down_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentDownPaths(), SvgNodeInfo.getTAColors(TChan.Ch8 + 1), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // 触发输入-下沿选中态
        resBmp[TChan.Ch8 + 1][trigger_ch_up] = SvgManager.createSvg(SvgNodeInfo.getTANormalUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch8 + 1), SvgNodeInfo.TN_WIDTH, SvgNodeInfo.TN_HEIGHT); // 触发输入-上沿普通态
        resBmp[TChan.Ch8 + 1][trigger_ch_up_current] = SvgManager.createSvg(SvgNodeInfo.getTACurrentUpPaths(), SvgNodeInfo.getTAColors(TChan.Ch8 + 1), SvgNodeInfo.TC_WIDTH, SvgNodeInfo.TC_HEIGHT); // 触发输入-上沿选中态
    }
    //endregion


    //region 属性
    private boolean bActive = true; // 当前是否选中激活
    private final String VoltageLineType; // 电平线类型名称（如"Trigger"）
    private int channelId = TChan.Ch1; // 当前所属通道ID
    private double offsetY; // Y轴偏移量
    private int showMode = ITriggerLine.ShowMode_One; // 显示模式：单线/双线/多线逻辑
    private boolean visibleLine = false; // 是否显示电平线（横线）
    private boolean visible = true; // 电平线整体是否可见

    private final Context context = App.get().getApplicationContext(); // 应用上下文
    private final ArrayList<DiscreetVoltageLineInfoBean> listShowChannelInfo = new ArrayList<>(); // 显示通道信息列表

    private int[] TriggerVoltageLine_logic_state = new int[TChan.MaxLogicChan + 1]; // 逻辑触发状态数组（每个通道的逻辑触发状态）

    /**
     * 设置逻辑触发状态数组
     * @param logic_state 逻辑触发状态数组
     */
    public void setTriggerVoltageLine_logic_state(int[] logic_state) {
        this.TriggerVoltageLine_logic_state = logic_state; // 赋值状态数组
        draw(); // 重绘
    }

    /**
     * 当前显示的所有电平的显示位置合集， 保存的是1000对应的值
     */
    private double[] currYPos = new double[TChan.MaxLogicChan + 2]; // 各通道Y位置数组（FPGA坐标系）
    private int currYIndex = TChan.Ch1; // 当前操作通道索引
    private String text = ""; // 电平线上显示的文本（如电压值）

    private boolean isShowState = true; // 是否显示电平线状态

    //endregion

    private Bitmap[] bmp = new Bitmap[TChan.MaxLogicChan + 2]; // 各通道电平线图标Bitmap
    private Bitmap[] oldBmp = new Bitmap[TChan.MaxLogicChan + 2]; // 各通道旧Bitmap（用于纹理更新）

    private Canvas[] mCanvas = new Canvas[TChan.MaxLogicChan + 2]; // 各通道Canvas绘制对象
    private Bitmap bmpLine,oldBmpLine; // 横线Bitmap及其旧Bitmap
    private Canvas mCanvasLine; // 横线Canvas
    private final Paint paint; // 画笔对象
    private boolean isChanageBitmap = false; // Bitmap内容是否已变更（需要刷新纹理）
    private ICanvasGL canvasGL; // GLCanvas引用（用于纹理刷新）

    /**
     * 通知GLCanvas刷新纹理
     */
    public void onRefresh() {
        if (canvasGL != null) { // GLCanvas引用有效
            canvasGL.onRefreshTexture(); // 刷新纹理
        }
    }

    /**
     * 构造函数：初始化资源图片、参数、Bitmap缓冲和画笔
     * @param VoltageLineType 电平线类型名称
     */
    public TriggerVoltageLine(String VoltageLineType) {
        this.VoltageLineType = VoltageLineType; // 保存电平线类型
        initResBmp(); // 初始化SVG资源图片
        initPram(); // 初始化位置参数
        reInitBmp(); // 初始化Bitmap缓冲
        paint = new Paint(); // 创建画笔
        paint.setTextSize(20); // 设置文字大小
        paint.setTypeface(Typeface.DEFAULT_BOLD); // 设置粗体字体
        paint.setFlags(Paint.ANTI_ALIAS_FLAG); // 开启抗锯齿
        paint.setStrokeWidth(1); // 设置线宽
        draw(); // 执行初始绘制
    }

    /**
     * 初始化所有通道Y位置到屏幕中央
     */
    private void initPram() {
        Arrays.fill(currYPos, GlobalVar.get().getMainWave().y / 2); // 所有通道Y位置初始化为屏幕高度一半
    }

    //region  ITriggerLine接口处理
    /**
     * 获取线名称ID
     * @return 触发电平标识
     */
    public int getNameId() {
        return TChan.TriggerLevel; // 返回触发电平标识
    }

    /**
     * 获取电平线类型名称
     * @return 类型名称字符串
     */
    public String getName() {
        return this.VoltageLineType; // 返回类型名称
    }


    /**
     * 获取当前选中态的普通图标索引
     * @return 选中时返回trigger_ch_current，未选中时返回trigger_ch
     */
    private int getTrigger_ch1() {
        return bActive ? trigger_ch_current : trigger_ch; // 根据激活状态返回索引
    }

    /**
     * 获取当前选中态的下沿图标索引
     * @return 选中时返回trigger_ch_down_current，未选中时返回trigger_ch_down
     */
    private int getTrigger_ch1_down() {
        return bActive ? trigger_ch_down_current : trigger_ch_down; // 根据激活状态返回索引
    }

    /**
     * 获取当前选中态的上沿图标索引
     * @return 选中时返回trigger_ch_up_current，未选中时返回trigger_ch_up
     */
    private int getTrigger_ch1_up() {
        return bActive ? trigger_ch_up_current : trigger_ch_up; // 根据激活状态返回索引
    }

    /**
     * 设置电平线激活状态
     * @param bActive 是否激活
     */
    @Override
    public void setActive(boolean bActive) {
        this.bActive = bActive; // 设置激活状态
        draw(); // 重绘
    }

    /**
     * 获取电平线激活状态
     * @return 是否激活
     */
    @Override
    public boolean isActive() {
        return bActive; // 返回激活状态
    }

    /**
     * 设置所属通道ID
     * @param channelId 通道ID
     */
    @Override
    public void setChannelId(int channelId) {
        if (this.channelId == channelId) return; // 通道未变，无需处理
        this.channelId = channelId; // 更新通道ID
        draw(); // 重绘
    }

    /**
     * 获取所属通道ID
     * @return 通道ID
     */
    @Override
    public int getChannelId() {
        return this.channelId; // 返回通道ID
    }

    /**
     * 设置电平线整体可见性
     * @param visible 是否可见
     */
    @Override
    public void setVisible(boolean visible) {
        this.visible = visible; // 设置可见性
    }

    /**
     * 获取电平线整体可见性
     * @return 是否可见
     */
    @Override
    public boolean getVisible() {
        return visible; // 返回可见性
    }

    /**
     * 通过偏移量设置当前通道Y位置
     * 包含边界校验，超出波形区域时钳位
     * @param offsetY Y轴偏移量
     * @return true=位置被钳位（触碰边界），false=正常移动
     */
    @Override
    public boolean setOffsetY(double offsetY) {
        if (!visible) { // 电平线不可见
//            return false;
            return setOffsetYHide(offsetY); // 隐藏状态下仍更新位置
        }
        boolean change; // 是否触碰边界
        double temY = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY); // 计算新Y位置（FPGA坐标转UI坐标后减偏移）
        if (temY < 0) { // 超出顶部边界
            currYPos[currYIndex] = 0; // 钳位到0
            change = true; // 标记触碰边界
        } else if (GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) < temY) { // 超出底部边界
            currYPos[currYIndex] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff(); // 钳位到底部
            change = true; // 标记触碰边界
        } else { // 正常范围内
            this.offsetY = offsetY; // 保存偏移量
            currYPos[currYIndex] = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY) * ScopeBase.getToFPGACoff(); // 更新位置（UI坐标转FPGA坐标）
            change = false; // 未触碰边界
        }
        draw(); // 重绘
        return change; // 返回边界状态
    }

    /**
     * 隐藏状态下的偏移量设置（不触发绘制）
     * @param offsetY Y轴偏移量
     * @return true=位置被钳位，false=正常移动
     */
    private boolean setOffsetYHide(double offsetY) {
        boolean change = false; // 是否触碰边界
        double temY = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY); // 计算新Y位置
        if (temY < 0) { // 超出顶部
            currYPos[currYIndex] = 0; // 钳位到0
            change = true; // 标记触碰边界
        } else if (GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) < temY) { // 超出底部
            currYPos[currYIndex] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff(); // 钳位到底部
            change = true; // 标记触碰边界
        } else { // 正常范围
            this.offsetY = offsetY; // 保存偏移量
            currYPos[currYIndex] = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY) * ScopeBase.getToFPGACoff(); // 更新位置
        }
        return change; // 返回边界状态
    }

    /**
     * 直接设置当前通道Y位置（UI坐标系）
     * 包含边界校验
     * @param currY Y位置（UI像素坐标）
     * @return true=位置被钳位，false=正常设置
     */
    @Override
    public boolean setCurrY(double currY) {
        if (!visible) return false; // 不可见时不处理
        boolean change; // 是否触碰边界
        if (currY < 0) { // 超出顶部
            this.currYPos[currYIndex] = 0; // 钳位到0
            change = true; // 标记触碰边界
        } else if (currY > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // 超出底部
            this.currYPos[currYIndex] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff(); // 钳位到底部
            change = true; // 标记触碰边界
        } else { // 正常范围
            this.currYPos[currYIndex] = currY * ScopeBase.getToFPGACoff(); // UI坐标转FPGA坐标
            change = false; // 未触碰边界
        }
        draw(); // 重绘
        return change; // 返回边界状态
    }

    /**
     * 设置指定通道的Y位置（UI坐标系）
     * 包含边界校验
     * @param ch 通道索引
     * @param y Y位置（UI像素坐标）
     * @return true=位置被钳位，false=正常设置
     */
    @Override
    public boolean setOtherY(int ch, double y) {
        if (!visible) { // 不可见
//            return false;
            return setOtherYHide(ch, y); // 隐藏状态下仍更新位置
        }
        boolean change; // 是否触碰边界
        if (y < 0) { // 超出顶部
            currYPos[ch] = 0; // 钳位到0
            change = true; // 标记触碰边界
        } else if (y > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // 超出底部
            currYPos[ch] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff();; // 钳位到底部
            change = true; // 标记触碰边界
        } else { // 正常范围
            this.currYPos[ch] = y * ScopeBase.getToFPGACoff(); // UI坐标转FPGA坐标
            change = false; // 未触碰边界
        }
        draw(); // 重绘
        return change; // 返回边界状态
    }

    /**
     * 隐藏状态下设置指定通道Y位置（不触发绘制）
     * @param ch 通道索引
     * @param y Y位置（UI像素坐标）
     * @return true=位置被钳位，false=正常设置
     */
    private boolean setOtherYHide(int ch, double y) {
        boolean change = false; // 是否触碰边界
        if (y < 0) { // 超出顶部
            currYPos[ch] = 0; // 钳位到0
            change = true; // 标记触碰边界
        } else if (y > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // 超出底部
            currYPos[ch] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff(); // 钳位到底部
            change = true; // 标记触碰边界
        } else { // 正常范围
            this.currYPos[ch] = y * ScopeBase.getToFPGACoff(); // UI坐标转FPGA坐标
        }
        return change; // 返回边界状态
    }

    /**
     * 设置电平线上显示的文本
     * @param text 文本内容
     */
    @Override
    public void setText(String text) {
        this.text = text; // 更新文本
        draw(); // 重绘
    }

    /**
     * 获取电平线上显示的文本
     * @return 文本内容
     */
    @Override
    public String getText() {
        return text; // 返回文本
    }

    /**
     * 获取当前通道Y位置（UI坐标系）
     * @return Y位置（UI像素坐标）
     */
    @Override
    public double getCurrY() {
        return ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff()); // FPGA坐标转UI坐标
    }

    /**
     * 获取指定通道Y位置（UI坐标系）
     * @param ch 通道索引
     * @return Y位置（UI像素坐标）
     */
    @Override
    public double getOtherY(int ch) {
        return ScopeBase.changeAccuracy(currYPos[ch] * ScopeBase.getToUICoff()); // FPGA坐标转UI坐标
    }

    /**
     * 获取所有通道Y位置（UI坐标系）
     * @return Y位置数组
     */
    @Override
    public double[] getCurrYAll() {
        double[] temp = new double[TChan.MaxLogicChan + 2]; // 创建结果数组
        for (int i = 0; i < currYPos.length; i++) { // 遍历所有通道
            temp[i] = ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff()); // FPGA坐标转UI坐标
        }
        return temp; // 返回UI坐标数组
    }

    /**
     * 切换工作模式时按比例换算所有通道Y位置
     * @param workMode 目标工作模式
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        switch (workMode) { // 根据工作模式
            case IWorkMode.WorkMode_YT: // YT模式
                for (int i = 0; i < currYPos.length; i++) { // 遍历所有通道
                    this.currYPos[i] = this.currYPos[i] * GlobalVar.get().toYTCoef(); // 乘以YT模式换算系数
                }
                break;
            case IWorkMode.WorkMode_YTZOOM: // YT缩放模式
                for (int i = 0; i < currYPos.length; i++) { // 遍历所有通道
                    this.currYPos[i] = this.currYPos[i] * GlobalVar.get().toZoomCoef(); // 乘以缩放模式换算系数
                }
                break;
        }
        //  Logger.i(TAG,"switchworkMode draw() currYPos:"+this.currYPos[currYIndex]+" ch:"+currYIndex);
        draw(); // 重绘
    }

    /**
     * 设置是否显示电平线状态
     * @param show 是否显示
     */
    @Override
    public void setShowState(boolean show) {
        if (isShowState != show) { // 状态变化时才处理
            isShowState = show; // 更新显示状态
            draw(); // 重绘
        }
    }

    /**
     * 获取是否显示电平线状态
     * @return 是否显示
     */
    @Override
    public boolean getShowState() {
        return isShowState; // 返回显示状态
    }

    /**
     * 获取当前显示的通道信息列表
     * 根据显示模式（单线/双线/多线逻辑）生成不同数量的信息Bean
     * @return 通道信息列表
     */
    @Override
    public ArrayList<DiscreetVoltageLineInfoBean> getShowChannelInfo() {
        listShowChannelInfo.clear(); // 清空列表
        if (showMode == ITriggerLine.ShowMode_One) { // 单线模式
            DiscreetVoltageLineInfoBean bean = new DiscreetVoltageLineInfoBean(); // 创建信息Bean
            bean.ChannelId = channelId; // 设置通道ID
            bean.ShowMode = showMode; // 设置显示模式
            bean.VoltageLineChannelIndex = VoltageLine_Normal; // 设置电平线索引
            bean.VoltageLineName = VoltageLineType; // 设置电平线名称
            listShowChannelInfo.add(bean); // 添加到列表
        } else if (showMode == ITriggerLine.ShowMode_Two) { // 双线模式（高/低电平）
            DiscreetVoltageLineInfoBean bean = new DiscreetVoltageLineInfoBean(); // 创建高电平信息Bean
            bean.ChannelId = channelId; // 设置通道ID
            bean.ShowMode = showMode; // 设置显示模式
            bean.VoltageLineName = VoltageLineType; // 设置电平线名称
            bean.VoltageLineChannelIndex = VoltageLine_High; // 设置为高电平索引
            listShowChannelInfo.add(bean); // 添加高电平Bean

            bean = new DiscreetVoltageLineInfoBean(); // 创建低电平信息Bean
            bean.ChannelId = channelId; // 设置通道ID
            bean.ShowMode = showMode; // 设置显示模式
            bean.VoltageLineName = VoltageLineType; // 设置电平线名称
            bean.VoltageLineChannelIndex = VoltageLine_Low; // 设置为低电平索引
            listShowChannelInfo.add(bean); // 添加低电平Bean
        } else if (showMode == ITriggerLine.ShowMode_Three) { // 多线逻辑模式
//            for (int i = 1; i < 5; i++) {
//                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue;
//                DiscreetVoltageLineInfoBean bean = new DiscreetVoltageLineInfoBean();
//                bean.ChannelId = i;
//                bean.ShowMode = showMode;
//                bean.VoltageLineName = VoltageLineType;
//                bean.VoltageLineChannelIndex = VoltageLine_Normal;
//                listShowChannelInfo.add(bean);
//            }
            TChan.foreachChan((i) -> { // 遍历所有通道
                        DiscreetVoltageLineInfoBean bean = new DiscreetVoltageLineInfoBean(); // 创建信息Bean
                        bean.ChannelId = i; // 设置通道ID
                        bean.ShowMode = showMode; // 设置显示模式
                        bean.VoltageLineName = VoltageLineType; // 设置电平线名称
                        bean.VoltageLineChannelIndex = VoltageLine_Normal; // 设置为普通索引
                        listShowChannelInfo.add(bean); // 添加到列表
                    }, (chan) ->
                            TriggerVoltageLine_logic_state[chan] == TriggerVoltageLine_Logic_None // 过滤条件：逻辑状态为None的通道
            );

        }
        return listShowChannelInfo; // 返回通道信息列表
    }

    /**
     * 绘制到标准Canvas（已弃用，保留接口兼容）
     * @param canvas Canvas对象
     */
    @Override
    public void draw(Canvas canvas) {
//        synchronized (bmp) {
//            canvas.drawBitmap(bmpLine, 0, currYPos[currYIndex], null);
//            switch (this.showMode) {
//                case ShowMode_One:
//                    drawOne(canvas);
//                    break;
//                case ShowMode_Two:
//                    drawTwo(canvas);
//                    break;
//                case ShowMode_Three:
//                    drawThree(canvas);
//                    break;
//            }
//        }
    }

    /**
     * 绘制到GLCanvas
     * 根据显示模式分发到对应的绘制方法
     * @param canvas ICanvasGL对象
     */
    @Override
    public void draw(ICanvasGL canvas) {
        if (!isShowState) return; // 不显示状态时直接返回
        if (!visible) return; // 不可见时直接返回
        synchronized (bmp) { // 同步锁，防止并发修改
            canvasGL = canvas; // 保存GLCanvas引用
            // if (isChanageBitmap) Logger.i(TAG,"draw ICanvas!");
            if (isChanageBitmap) { // Bitmap内容已变更
                canvas.invalidateTextureContent(bmpLine,oldBmpLine); // 刷新横线纹理
                oldBmpLine = null; // 清除旧Bitmap引用
            }

            canvas.drawBitmap(bmpLine, 0, (int) Math.round(ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff()))); // 绘制横线
            switch (this.showMode) { // 根据显示模式分发
                case ShowMode_One: // 单线模式
                    drawOne(canvas); // 绘制单线
                    break;
                case ShowMode_Two: // 双线模式
                    drawTwo(canvas); // 绘制双线
                    break;
                case ShowMode_Three: // 多线逻辑模式
                    drawThree(canvas); // 绘制多线
                    break;
            }
            isChanageBitmap = false; // 重置变更标志
        }
    }

    /**
     * GLCanvas单线模式绘制
     * 绘制当前通道的电平线图标，处理边界情况（上沿/下沿图标）
     * @param canvas ICanvasGL对象
     */
    private void drawOne(ICanvasGL canvas) {

        //上下限的限制
        if (isChanageBitmap) { // Bitmap内容已变更
            canvas.invalidateTextureContent(bmp[currYIndex],oldBmp[currYIndex]); // 刷新图标纹理
            oldBmp[currYIndex] = null; // 清除旧Bitmap引用
        }
        if (ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff()) < resBmp[1][trigger_ch_up_current].getHeight()) { // 超出顶部边界
            int temY = resBmp[1][0].getHeight(); // 取图标高度
            temY = temY - (bmp[currYIndex].getHeight() - 1) / 2; // 计算居中偏移
            canvas.drawBitmap(bmp[currYIndex], 0, temY); // 绘制到顶部
        } else if (ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][trigger_ch_down_current].getHeight()) { // 超出底部边界
            int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // 计算底部位置
            temY = temY - (bmp[currYIndex].getHeight() - 1) / 2; // 计算居中偏移
            canvas.drawBitmap(bmp[currYIndex], 0, temY); // 绘制到底部
        } else { // 正常范围
            int temY = (int) Math.round(ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff())) - (bmp[currYIndex].getHeight() - 1) / 2; // 计算居中Y位置
            canvas.drawBitmap(bmp[currYIndex], 0, temY); // 正常绘制
        }
    }

    /**
     * GLCanvas双线模式绘制
     * 同时绘制高/低两条电平线，高电平不能低于低电平
     * @param canvas ICanvasGL对象
     */
    private void drawTwo(ICanvasGL canvas) {
        //高小于低，就跟着跑，反之也是如此
        //高电平在上，像素小，低电平在下，像素大
        if (currYPos[1] > currYPos[2]) { // 高电平位置低于低电平位置（像素值更大）
            if (currYIndex == 1) { // 当前操作的是高电平
                currYPos[2] = currYPos[1]; // 低电平跟随高电平
            } else { // 当前操作的是低电平
                currYPos[1] = currYPos[2]; // 高电平跟随低电平
            }
        }


        for (int i = 1; i < 3; i++) { // 遍历高/低电平
            if (isChanageBitmap){ // Bitmap内容已变更
                canvas.invalidateTextureContent(bmp[i],oldBmp[i]); // 刷新纹理
                oldBmp[i] = null; // 清除旧Bitmap引用
            }
            if (ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff()) < resBmp[i][getTrigger_ch1_up()].getHeight()) { // 超出顶部
                int temY = resBmp[1][0].getHeight(); // 取图标高度
                temY = temY - (bmp[i].getHeight() - 1) / 2; // 计算居中偏移
                canvas.drawBitmap(bmp[i], 0, temY); // 绘制到顶部
            } else if (ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][getTrigger_ch1_down()].getHeight()) { // 超出底部
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // 计算底部位置
                temY = temY - (bmp[i].getHeight() - 1) / 2; // 计算居中偏移
                canvas.drawBitmap(bmp[i], 0, temY); // 绘制到底部
            } else { // 正常范围
                int temY = (int) Math.round(ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff())) - (bmp[i].getHeight() - 1) / 2; // 计算居中Y位置
                canvas.drawBitmap(bmp[i], 0, temY); // 正常绘制
            }
        }

    }

    /**
     * GLCanvas多线逻辑模式绘制
     * 绘制所有参与逻辑触发的通道电平线，当前选中通道用大图标，其他通道用小图标
     * @param canvas ICanvasGL对象
     */
    private void drawThree(ICanvasGL canvas) {
//        for (int i = 1; i < TChan.MaxLogicChan + 1; i++) {
//            if (isChanageBitmap) canvas.invalidateTextureContent(bmp[i]);
//            if (currYPos[i] < resBmp[i][getTrigger_ch1_up()].getHeight()) {
//                int temY = resBmp[1][0].getHeight();
//                temY = temY - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY);
//            } else if (currYPos[i] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][getTrigger_ch1_down()].getHeight()) {
//                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                temY = temY - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY);
//            } else {
//                int temY = currYPos[i] - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY);
//            }
//        }
        TChan.foreachChan((i) -> { // 遍历所有有效通道
            if (isChanageBitmap){ // Bitmap内容已变更
                canvas.invalidateTextureContent(bmp[i],oldBmp[i]); // 刷新纹理
                oldBmp[i] = null; // 清除旧Bitmap引用
            }
            if (ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff()) < resBmp[i][getTrigger_ch1_up()].getHeight()) { // 超出顶部
                int temY = resBmp[1][0].getHeight(); // 取图标高度
                temY = temY - (bmp[i].getHeight() - 1) / 2; // 计算居中偏移
                canvas.drawBitmap(bmp[i], 0, temY); // 绘制到顶部
            } else if (ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][getTrigger_ch1_down()].getHeight()) { // 超出底部
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // 计算底部位置
                temY = temY - (bmp[i].getHeight() - 1) / 2; // 计算居中偏移
                canvas.drawBitmap(bmp[i], 0, temY); // 绘制到底部
            } else { // 正常范围
                int temY = (int) Math.round(ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff())) - (bmp[i].getHeight() - 1) / 2; // 计算居中Y位置
                canvas.drawBitmap(bmp[i], 0, temY); // 正常绘制
            }
        });


        int i = currYIndex; // 当前选中通道
        if (isChanageBitmap){ // Bitmap内容已变更
            canvas.invalidateTextureContent(bmp[i],oldBmp[i]); // 刷新当前选中通道纹理
            oldBmp[i] = null; // 清除旧Bitmap引用
        }
        if (ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff()) < resBmp[i][getTrigger_ch1_up()].getHeight()) { // 超出顶部
            int temY = resBmp[1][0].getHeight(); // 取图标高度
            temY = temY - (bmp[i].getHeight() - 1) / 2; // 计算居中偏移
            canvas.drawBitmap(bmp[i], 0, temY); // 绘制到顶部
        } else if (ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][getTrigger_ch1_down()].getHeight()) { // 超出底部
            int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // 计算底部位置
            temY = temY - (bmp[i].getHeight() - 1) / 2; // 计算居中偏移
            canvas.drawBitmap(bmp[i], 0, temY); // 绘制到底部
        } else { // 正常范围
            int temY = (int) Math.round(ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff())) - (bmp[i].getHeight() - 1) / 2; // 计算居中Y位置
            canvas.drawBitmap(bmp[i], 0, temY); // 正常绘制
        }


    }


    /**
     * 获取当前操作通道索引
     * @return 通道索引
     */
    @Override
    public int getCurrYIndex() {
        return currYIndex; // 返回当前通道索引
    }

    /***
     * 设置当前序号
     * @param currYIndex {@link IWave IWave.Ch1 - IWave.Ch4}
     *                   或者{@link ITriggerLine ITriggerLine.VoltageLine_High - ITriggerLine.VoltageLine_Low}
     */
    @Override
    public void setCurrYIndex(int currYIndex) {
        this.currYIndex = currYIndex; // 设置当前操作通道索引
        draw(); // 重绘
    }

    /**
     * 设置显示模式
     * @param showMode 显示模式（单线/双线/多线逻辑）
     */
    @Override
    public void setShowMode(int showMode) {
        this.showMode = showMode; // 设置显示模式

        draw(); // 重绘
    }

    /**
     * 获取显示模式
     * @return 显示模式
     */
    @Override
    public int getShowMode() {
        return this.showMode; // 返回显示模式
    }

    /**
     * 设置是否显示横线
     * @param visibleLine 是否显示横线
     */
    @Override
    public void setVisibleLine(boolean visibleLine) {
        if (visibleLine != this.visibleLine) { // 状态变化时才处理
            this.visibleLine = visibleLine; // 更新横线可见性
            draw(); // 重绘
        }
    }

    /**
     * 获取是否显示横线
     * @return 是否显示横线
     */
    @Override
    public boolean getVisibleLine() {
        return this.visibleLine; // 返回横线可见性
    }

    /**
     * 刷新电平线（重新初始化Bitmap和重绘）
     */
    @Override
    public void refresh() {
        reInitBmp(); // 重新初始化Bitmap缓冲
        draw(); // 重绘
    }

    private
    @IWorkMode.WorkMode
    int workMode = IWorkMode.WorkMode_None; // 当前工作模式缓存
    /**
     * 重新初始化Bitmap缓冲
     * 仅在工作模式变化时重新创建，保存旧Bitmap用于纹理更新
     */
    private void reInitBmp(){
        if(workMode != WorkModeManage.getInstance().getmWorkMode()){ // 工作模式发生变化
            workMode = WorkModeManage.getInstance().getmWorkMode(); // 更新工作模式缓存
            TChan.foreachChan((chan) -> { // 遍历所有通道
                oldBmp[chan] = bmp[chan]; // 保存旧Bitmap
                bmp[chan] = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(workMode), 60, Bitmap.Config.ARGB_8888); // 创建新Bitmap
                mCanvas[chan] = new Canvas(bmp[chan]); // 创建新Canvas
            });

            //For Trigger Input
            oldBmp[TChan.Ch8 + 1] = bmp[TChan.Ch8 + 1]; // 保存触发输入旧Bitmap
            bmp[TChan.Ch8 + 1] = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(workMode), 60, Bitmap.Config.ARGB_8888); // 创建触发输入新Bitmap
            mCanvas[TChan.Ch8 + 1] = new Canvas(bmp[TChan.Ch8 + 1]); // 创建触发输入新Canvas
            oldBmpLine = bmpLine; // 保存横线旧Bitmap
            bmpLine = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(workMode), 1, Bitmap.Config.ARGB_8888); // 创建横线新Bitmap
            mCanvasLine = new Canvas(bmpLine); // 创建横线新Canvas
        }

    }

//endregion

    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR); // 清除混合模式
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC); // 源覆盖混合模式

    /**
     * 内部绘制方法
     * 清空画布后根据显示模式绘制电平线图标和横线
     */
    private void draw() {
        synchronized (bmp) { // 同步锁
            paint.setXfermode(clearMode); // 设置清除模式
            mCanvas[currYIndex].drawPaint(paint); // 清空当前通道画布
            mCanvasLine.drawPaint(paint); // 清空横线画布
            paint.setXfermode(srcMode); // 设置源覆盖模式
            switch (showMode) { // 根据显示模式
                case ITriggerLine.ShowMode_One: // 单线模式
                    drawOne(); // 绘制单线
                    break;
                case ITriggerLine.ShowMode_Two: // 双线模式
                    drawTwo(); // 绘制双线
                    break;
                case ITriggerLine.ShowMode_Three: // 多线逻辑模式
                    drawThree(); // 绘制多线
                    break;
            }
            isChanageBitmap = true; // 标记Bitmap已变更
            onRefresh(); // 通知GLCanvas刷新纹理
        }
    }


    /**
     * 单线模式内部绘制
     * 绘制横线和电平线图标，处理边界和文本显示
     */
    private void drawOne() {
        if (this.visibleLine) { // 显示横线
            //显示
            if(channelId == TChan.Ch8 + 1) { // 触发输入通道
                paint.setColor(context.getResources().getColor(R.color.colorChCommon)); // 使用通用颜色
            } else { // 普通通道
                paint.setColor(TChan.getChannelColor(context, channelId)); // 使用通道颜色
            }
            mCanvasLine.drawLine(0, 0, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), 0, paint); // 绘制横线
            if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) < resBmp[channelId][getTrigger_ch1_up()].getHeight()) { // 超出顶部
                int temY = resBmp[channelId][0].getHeight(); // 取图标高度
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_up()], // 绘制上沿图标
                        GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_up()].getWidth(), // X位置靠右
                        ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint); // Y位置居中
                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35); // 绘制文本
            } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getHeight()) { // 超出底部
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // 计算底部位置
                temY = temY - (bmp[channelId].getHeight() - 1) / 2; // 居中偏移
                temY = bmp[channelId].getHeight() - resBmp[channelId][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // 计算图标内偏移
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_down()], // 绘制下沿图标
                        GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getWidth(), // X位置靠右
                        temY, paint); // Y位置
                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 0); // 绘制文本
            } else { // 正常范围
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1()], // 绘制普通图标
                        GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1()].getWidth(), // X位置靠右
                        (bmp[currYIndex].getHeight() - resBmp[channelId][getTrigger_ch1()].getHeight()) / 2, paint); // Y位置居中
                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35); // 绘制文本
            }

        } else { // 不显示横线
            //不显示
            if(channelId == TChan.Ch8 + 1) { // 触发输入通道
                paint.setColor(context.getResources().getColor(R.color.colorChCommon)); // 使用通用颜色
            } else { // 普通通道
                paint.setColor(TChan.getChannelColor(context, channelId)); // 使用通道颜色
            }
            if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) < resBmp[channelId][getTrigger_ch1_up()].getHeight()) { // 超出顶部
                int temY = resBmp[channelId][0].getHeight(); // 取图标高度
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_up()].getWidth(), // 绘制上沿图标
                        ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint); // Y位置居中
            } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getHeight()) { // 超出底部
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // 计算底部位置
                temY = temY - (bmp[channelId].getHeight() - 1) / 2; // 居中偏移
                temY = bmp[channelId].getHeight() - resBmp[channelId][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // 计算图标内偏移
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getWidth(), // 绘制下沿图标
                        temY, paint); // Y位置
            } else { // 正常范围
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1()].getWidth(), // 绘制普通图标
                        (bmp[currYIndex].getHeight() - resBmp[channelId][getTrigger_ch1()].getHeight()) / 2, paint); // Y位置居中
            }

        }
    }

    /**
     * 双线模式内部绘制
     * 绘制横线、当前选中电平线和另一条电平线
     */
    private void drawTwo() {
        if (this.visibleLine) { // 显示横线
            paint.setColor(TChan.getChannelColor(context, channelId)); // 设置通道颜色
            mCanvasLine.drawLine(0, 0, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), 0, paint); // 绘制横线

            if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) < resBmp[channelId][getTrigger_ch1_up()].getHeight()) { // 超出顶部
                int temY = resBmp[1][0].getHeight(); // 取图标高度
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_up()].getWidth(), // 绘制上沿图标
                        ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint); // Y位置居中
                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35); // 绘制文本
            } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getHeight()) { // 超出底部
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // 计算底部位置
                temY = temY - (bmp[channelId].getHeight() - 1) / 2; // 居中偏移
                temY = bmp[channelId].getHeight() - resBmp[channelId][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // 图标内偏移
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getWidth(), // 绘制下沿图标
                        temY, paint); // Y位置
                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 0); // 绘制文本
            } else { // 正常范围
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1()].getWidth(), // 绘制普通图标
                        (bmp[currYIndex].getHeight() - resBmp[channelId][getTrigger_ch1()].getHeight()) / 2, paint); // Y位置居中
                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35); // 绘制文本
            }

            //如果高，就绘制低
            int temIndex; // 另一条电平线索引
            if (currYIndex == VoltageLine_High) { // 当前是高电平
                temIndex = VoltageLine_Low; // 则绘制低电平
            } else { // 当前是低电平
                temIndex = VoltageLine_High; // 则绘制高电平
            }
            paint.setXfermode(clearMode); // 设置清除模式
            mCanvas[temIndex].drawPaint(paint); // 清空另一条线画布
            paint.setXfermode(srcMode); // 设置源覆盖模式

            if (ScopeBase.changeAccuracy(this.currYPos[temIndex] * ScopeBase.getToUICoff()) < resBmp[channelId][trigger_ch_up].getHeight()) { // 超出顶部
                int temY = resBmp[1][0].getHeight(); // 取图标高度
                mCanvas[temIndex].drawBitmap(resBmp[channelId][trigger_ch_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch_up].getWidth(), // 绘制未选中上沿图标
                        ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint); // Y位置居中
                //drawText(35);
            } else if (ScopeBase.changeAccuracy(this.currYPos[temIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch_down].getHeight()) { // 超出底部
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // 计算底部位置
                temY = temY - (bmp[channelId].getHeight() - 1) / 2; // 居中偏移
                temY = bmp[channelId].getHeight() - resBmp[channelId][trigger_ch_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // 图标内偏移
                mCanvas[temIndex].drawBitmap(resBmp[channelId][trigger_ch_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch_down].getWidth(), // 绘制未选中下沿图标
                        temY, paint); // Y位置
                //drawText(0);
            } else { // 正常范围
                mCanvas[temIndex].drawBitmap(resBmp[channelId][trigger_ch], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch].getWidth(), // 绘制未选中普通图标
                        (bmp[temIndex].getHeight() - resBmp[channelId][trigger_ch].getHeight()) / 2, paint); // Y位置居中
                //drawText(35);
            }


        } else { // 不显示线条
            //不显示线条
            paint.setColor(TChan.getChannelColor(context, channelId)); // 设置通道颜色
            if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) < resBmp[channelId][getTrigger_ch1_up()].getHeight()) { // 超出顶部
                int temY = resBmp[1][0].getHeight(); // 取图标高度
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_up()].getWidth(), // 绘制选中上沿图标
                        ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint); // Y位置居中
            } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getHeight()) { // 超出底部
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // 计算底部位置
                temY = temY - (bmp[channelId].getHeight() - 1) / 2; // 居中偏移
                temY = bmp[channelId].getHeight() - resBmp[channelId][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // 图标内偏移
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1_down()].getWidth(), // 绘制选中下沿图标
                        temY, paint); // Y位置
            } else { // 正常范围
                mCanvas[currYIndex].drawBitmap(resBmp[channelId][getTrigger_ch1()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][getTrigger_ch1()].getWidth(), // 绘制选中普通图标
                        (bmp[currYIndex].getHeight() - resBmp[channelId][getTrigger_ch1()].getHeight()) / 2, paint); // Y位置居中
            }
            //如果不是当前的，就显示小图标
            int temIndex; // 另一条电平线索引
            if (currYIndex == VoltageLine_High) { // 当前是高电平
                temIndex = VoltageLine_Low; // 则绘制低电平
            } else { // 当前是低电平
                temIndex = VoltageLine_High; // 则绘制高电平
            }
            paint.setXfermode(clearMode); // 设置清除模式
            mCanvas[temIndex].drawPaint(paint); // 清空另一条线画布
            paint.setXfermode(srcMode); // 设置源覆盖模式
            if (ScopeBase.changeAccuracy(this.currYPos[temIndex] * ScopeBase.getToUICoff()) < resBmp[channelId][trigger_ch_up].getHeight()) { // 超出顶部
                int temY = resBmp[1][0].getHeight(); // 取图标高度
                mCanvas[temIndex].drawBitmap(resBmp[channelId][trigger_ch_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch_up].getWidth(), // 绘制未选中上沿图标
                        ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint); // Y位置居中

            } else if (ScopeBase.changeAccuracy(this.currYPos[temIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch_down].getHeight()) { // 超出底部
                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // 计算底部位置
                temY = temY - (bmp[channelId].getHeight() - 1) / 2; // 居中偏移
                temY = bmp[channelId].getHeight() - resBmp[channelId][trigger_ch_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // 图标内偏移
                mCanvas[temIndex].drawBitmap(resBmp[channelId][trigger_ch_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch_down].getWidth(), // 绘制未选中下沿图标
                        temY, paint); // Y位置

            } else { // 正常范围
                mCanvas[temIndex].drawBitmap(resBmp[channelId][trigger_ch], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][trigger_ch].getWidth(), // 绘制未选中普通图标
                        (bmp[temIndex].getHeight() - resBmp[channelId][trigger_ch].getHeight()) / 2, paint); // Y位置居中

            }

        }
    }

    /**
     * 多线逻辑模式内部绘制
     * 绘制所有参与逻辑触发的通道电平线，当前选中通道用大图标
     */
    private void drawThree() {
        if (this.visibleLine) { // 显示横线
            paint.setColor(TChan.getChannelColor(context, channelId)); // 设置通道颜色
            mCanvasLine.drawLine(0, 0, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), 0, paint); // 绘制横线

            for (int i = 1; i < TChan.MaxLogicChan + 1; i++) { // 遍历所有逻辑通道
                paint.setXfermode(clearMode); // 设置清除模式
                mCanvas[i].drawPaint(paint); // 清空画布
                paint.setXfermode(srcMode); // 设置源覆盖模式
                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue; // 跳过不参与的通道
                if (i == currYIndex) { // 当前选中通道
                    if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) < resBmp[currYIndex][getTrigger_ch1_up()].getHeight()) { // 超出顶部
                        int temY = resBmp[1][0].getHeight(); // 取图标高度
                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_up()].getWidth(), // 绘制选中上沿图标
                                ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint); // Y位置居中
                        drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35); // 绘制文本
                    } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getHeight()) { // 超出底部
                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // 计算底部位置
                        temY = temY - (bmp[channelId].getHeight() - 1) / 2; // 居中偏移
                        temY = bmp[channelId].getHeight() - resBmp[currYIndex][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // 图标内偏移
                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getWidth(), // 绘制选中下沿图标
                                temY, paint); // Y位置
                        drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 0); // 绘制文本
                    } else { // 正常范围
                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1()].getWidth(), // 绘制选中普通图标
                                (bmp[currYIndex].getHeight() - resBmp[currYIndex][getTrigger_ch1()].getHeight()) / 2, paint); // Y位置居中
                        drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35); // 绘制文本
                    }
                } else { // 非当前选中通道
                    if (ScopeBase.changeAccuracy(this.currYPos[i] * ScopeBase.getToUICoff()) < resBmp[i][trigger_ch_up].getHeight()) { // 超出顶部
                        int temY = resBmp[1][0].getHeight(); // 取图标高度
                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_up].getWidth(), // 绘制未选中上沿图标
                                ((bmp[i].getHeight() - 1) / 2 - temY), paint); // Y位置居中
                    } else if (ScopeBase.changeAccuracy(this.currYPos[i] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getHeight()) { // 超出底部
                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // 计算底部位置
                        temY = temY - (bmp[channelId].getHeight() - 1) / 2; // 居中偏移
                        temY = bmp[channelId].getHeight() - resBmp[i][trigger_ch_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // 图标内偏移
                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getWidth(), // 绘制未选中下沿图标
                                temY, paint); // Y位置
                    } else { // 正常范围
                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch].getWidth(), // 绘制未选中普通图标
                                (bmp[i].getHeight() - resBmp[i][trigger_ch].getHeight()) / 2, paint); // Y位置居中

                    }
                }
            }
//            TChan.foreachChan((i) -> {
//                        paint.setXfermode(clearMode);
//                        mCanvas[i].drawPaint(paint);
//                        paint.setXfermode(srcMode);
////                      if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue;
//                        if (i == currYIndex) {
//                            if (this.currYPos[currYIndex] < resBmp[currYIndex][getTrigger_ch1_up()].getHeight()) {
//                                int temY = resBmp[1][0].getHeight();
//                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_up()].getWidth(),
//                                        ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint);
//                                drawText(this.currYPos[currYIndex], 35);
//                            } else if (this.currYPos[currYIndex] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getHeight()) {
//                                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                                temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                                temY = bmp[channelId].getHeight() - resBmp[currYIndex][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getWidth(),
//                                        temY, paint);
//                                drawText(this.currYPos[currYIndex], 0);
//                            } else {
//                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1()].getWidth(),
//                                        (bmp[currYIndex].getHeight() - resBmp[currYIndex][getTrigger_ch1()].getHeight()) / 2, paint);
//                                drawText(this.currYPos[currYIndex], 35);
//                            }
//                        } else {
//                            if (this.currYPos[i] < resBmp[i][trigger_ch_up].getHeight()) {
//                                int temY = resBmp[1][0].getHeight();
//                                mCanvas[i].drawBitmap(resBmp[i][trigger_ch_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_up].getWidth(),
//                                        ((bmp[i].getHeight() - 1) / 2 - temY), paint);
//                            } else if (this.currYPos[i] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getHeight()) {
//                                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                                temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                                temY = bmp[channelId].getHeight() - resBmp[i][trigger_ch_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                                mCanvas[i].drawBitmap(resBmp[i][trigger_ch_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getWidth(),
//                                        temY, paint);
//                            } else {
//                                mCanvas[i].drawBitmap(resBmp[i][trigger_ch], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch].getWidth(),
//                                        (bmp[i].getHeight() - resBmp[i][trigger_ch].getHeight()) / 2, paint);
//
//                            }
//                        }
//                    }, (i) ->
//                            TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None
//            );

        } else { // 不显示横线
            for (int i = 1; i < TChan.MaxLogicChan + 1; i++) { // 遍历所有逻辑通道
                paint.setXfermode(clearMode); // 设置清除模式
                mCanvas[i].drawPaint(paint); // 清空画布
                paint.setXfermode(srcMode); // 设置源覆盖模式
                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue; // 跳过不参与的通道
                if (i == currYIndex) { // 当前选中通道
                    if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) < resBmp[currYIndex][getTrigger_ch1_up()].getHeight()) { // 超出顶部
                        int temY = resBmp[1][0].getHeight(); // 取图标高度
                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_up()].getWidth(), // 绘制选中上沿图标
                                ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint); // Y位置居中

                    } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getHeight()) { // 超出底部
                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // 计算底部位置
                        temY = temY - (bmp[channelId].getHeight() - 1) / 2; // 居中偏移
                        temY = bmp[channelId].getHeight() - resBmp[currYIndex][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // 图标内偏移
                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getWidth(), // 绘制选中下沿图标
                                temY, paint); // Y位置

                    } else { // 正常范围
                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1()].getWidth(), // 绘制选中普通图标
                                (bmp[currYIndex].getHeight() - resBmp[currYIndex][getTrigger_ch1()].getHeight()) / 2, paint); // Y位置居中

                    }
                } else { // 非当前选中通道
                    if (ScopeBase.changeAccuracy(this.currYPos[i] * ScopeBase.getToUICoff()) < resBmp[i][trigger_ch_up].getHeight()) { // 超出顶部
                        int temY = resBmp[1][0].getHeight(); // 取图标高度
                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_up].getWidth(), // 绘制未选中上沿图标
                                ((bmp[i].getHeight() - 1) / 2 - temY), paint); // Y位置居中
                    } else if (ScopeBase.changeAccuracy(this.currYPos[i] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getHeight()) { // 超出底部
                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // 计算底部位置
                        temY = temY - (bmp[channelId].getHeight() - 1) / 2; // 居中偏移
                        temY = bmp[channelId].getHeight() - resBmp[i][trigger_ch_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // 图标内偏移
                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getWidth(), // 绘制未选中下沿图标
                                temY, paint); // Y位置
                    } else { // 正常范围
                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch].getWidth(), // 绘制未选中普通图标
                                (bmp[i].getHeight() - resBmp[i][trigger_ch].getHeight()) / 2, paint); // Y位置居中

                    }
                }
            }
//            TChan.foreachChan((i) -> {
//                paint.setXfermode(clearMode);
//                mCanvas[i].drawPaint(paint);
//                paint.setXfermode(srcMode);
////                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue;
//                if (i == currYIndex) {
//                    if (this.currYPos[currYIndex] < resBmp[currYIndex][getTrigger_ch1_up()].getHeight()) {
//                        int temY = resBmp[1][0].getHeight();
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_up()].getWidth(),
//                                ((bmp[currYIndex].getHeight() - 1) / 2 - temY), paint);
//
//                    } else if (this.currYPos[currYIndex] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getHeight()) {
//                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                        temY = bmp[channelId].getHeight() - resBmp[currYIndex][getTrigger_ch1_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1_down()].getWidth(),
//                                temY, paint);
//
//                    } else {
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getTrigger_ch1()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getTrigger_ch1()].getWidth(),
//                                (bmp[currYIndex].getHeight() - resBmp[currYIndex][getTrigger_ch1()].getHeight()) / 2, paint);
//
//                    }
//                } else {
//                    if (this.currYPos[i] < resBmp[i][trigger_ch_up].getHeight()) {
//                        int temY = resBmp[1][0].getHeight();
//                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_up].getWidth(),
//                                ((bmp[i].getHeight() - 1) / 2 - temY), paint);
//                    } else if (this.currYPos[i] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getHeight()) {
//                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                        temY = bmp[channelId].getHeight() - resBmp[i][trigger_ch_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch_down].getWidth(),
//                                temY, paint);
//                    } else {
//                        mCanvas[i].drawBitmap(resBmp[i][trigger_ch], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][trigger_ch].getWidth(),
//                                (bmp[i].getHeight() - resBmp[i][trigger_ch].getHeight()) / 2, paint);
//
//                    }
//                }
//            }, (i) -> TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None);
        }
    }

    /***
     * 绘制文字
     * @param currY 当前Y位置（UI坐标）
     * @param drawTextY 绘制的起始Y坐标
     */
    private void drawText(int currY, int drawTextY) {
        int temp = 35; // 默认文本偏移量
//        if (channelId == TChan.Ch8 + 1) {
//            temp = 70;
//        }
        if (currY <= 50) { // 位置接近顶部
            drawTextY = temp; // 文本偏移到下方
        } else if (currY >= GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - 50) { // 位置接近底部
            drawTextY = 0; // 文本偏移到上方
        } else { // 正常范围
            drawTextY = temp; // 文本偏移到下方
        }
        int x; // 文本X坐标
        Rect rect; // 文本矩形区域
        rect = getTextHeight(text); // 获取文本尺寸
        x = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - rect.width() - 5; // X位置靠右减5像素
        drawTextY = drawTextY + rect.height(); // Y偏移加上文本高度
        mCanvas[currYIndex].drawText(text, x, drawTextY, paint); // 绘制文本
    }

    private Rect rect = new Rect(); // 文本测量矩形缓存

    /**
     * 测量文本宽高
     * @param text 待测量的文本
     * @return 文本边界矩形
     */
    private Rect getTextHeight(String text) {
        paint.getTextBounds(text, 0, text.length(), rect); // 测量文本边界
        int w = rect.width(); // 获取宽度
        int h = rect.height(); // 获取高度
        return rect; // 返回矩形
    }


}
