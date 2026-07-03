package com.micsig.tbook.tbookscope.wavezone.trigger; // // 触发电平线模块包

import android.content.Context; // // Android上下文
import android.graphics.Bitmap; // // 位图
import android.graphics.Canvas; // // 系统画布
import android.graphics.Paint; // // 画笔
import android.graphics.PorterDuff; // // 图层混合模式
import android.graphics.PorterDuffXfermode; // // 图层混合模式设置
import android.graphics.Rect; // // 矩形区域
import android.graphics.Typeface; // // 字体
import android.graphics.drawable.BitmapDrawable; // // 位图Drawable
import android.util.Log; // // Android日志

import com.chillingvan.canvasgl.ICanvasGL; // // OpenGL画布接口
import com.micsig.base.Logger; // // 自定义日志
import com.micsig.tbook.scope.ScopeBase; // // 示波器基础类（坐标换算）
import com.micsig.tbook.tbookscope.GlobalVar; // // 全局变量
import com.micsig.tbook.tbookscope.R; // // 资源ID
import com.micsig.tbook.tbookscope.tools.Tools; // // 工具类
import com.micsig.tbook.tbookscope.util.App; // // Application工具
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // // 工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // // 工作模式管理器
import com.micsig.tbook.ui.util.TBookUtil; // // TBook工具类
import com.micsig.tbook.ui.wavezone.IWave; // // 波形接口
import com.micsig.tbook.ui.wavezone.TChan; // // 通道常量与工具

import java.util.ArrayList; // // 动态数组
import java.util.Arrays; // // 数组工具

/*
 * +=============================================================================+
 * |                         DiscreetVoltageLine                                 |
 * +=============================================================================+
 * | 模块定位 : 预值电平线（触发电平线）的核心实现类                                 |
 * | 核心职责 : 管理预值电平线的状态、位置、绘制逻辑，支持单箭头/双箭头两种显示模式    |
 * | 架构设计 : 实现ITriggerLine接口，采用Bitmap离屏绘制+ICanvasGL纹理刷新方式渲染     |
 * | 数据流向 : 上层控制 → 设置属性 → draw()离屏绘制 → draw(ICanvasGL)上屏渲染       |
 * | 依赖关系 : ITriggerLine(接口)、ScopeBase(坐标换算)、TChan(通道)、               |
 * |            GlobalVar(全局配置)、WorkModeManage(工作模式)、Tools(资源读取)       |
 * | 使用场景 : 示波器触发电平线的交互与显示，支持8通道×12种图标状态的绘制           |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2017/5/17.
 * 预值电平
 */

public class DiscreetVoltageLine implements ITriggerLine { // // 预值电平线实现类，实现ITriggerLine接口
    private static final String TAG = "DiscreetVoltageLine"; // // 日志标签


    //region 初始化资源
    private static final int discreet_front_l = 0; // // 图标状态：前置+大箭头（激活+选中）
    private static final int discreet_front_l_down = 1; // // 图标状态：前置+大箭头+向下（超出下边界）
    private static final int discreet_front_l_up = 2; // // 图标状态：前置+大箭头+向上（超出上边界）
    private static final int discreet_front_normal = 3; // // 图标状态：前置+普通箭头（激活+非选中）
    private static final int discreet_front_normal_down = 4; // // 图标状态：前置+普通箭头+向下
    private static final int discreet_front_normal_up = 5; // // 图标状态：前置+普通箭头+向上
    private static final int discreet_l = 6; // // 图标状态：非前置+大箭头
    private static final int discreet_l_down = 7; // // 图标状态：非前置+大箭头+向下
    private static final int discreet_l_up = 8; // // 图标状态：非前置+大箭头+向上
    private static final int discreet_normal = 9; // // 图标状态：非前置+普通箭头
    private static final int discreet_normal_down = 10; // // 图标状态：非前置+普通箭头+向下
    private static final int discreet_normal_up = 11; // // 图标状态：非前置+普通箭头+向上
    //4个通道，加12种状态
    /** 各通道×12种图标状态的Bitmap资源数组 */
    private Bitmap resBmp[][] = new Bitmap[TChan.MaxLogicChan + 1][12]; // // 资源位图数组：[通道号][12种图标状态]

    /**
     * 初始化所有通道的图标资源Bitmap
     * 将drawable资源加载到resBmp二维数组中
     */
    private void initRes() { // // 初始化图标资源，将各通道12种状态drawable加载为Bitmap
        resBmp[TChan.Ch1][discreet_front_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_front_l)).getBitmap(); // // Ch1 前置大箭头
        resBmp[TChan.Ch1][discreet_front_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_front_l_down)).getBitmap(); // // Ch1 前置大箭头向下
        resBmp[TChan.Ch1][discreet_front_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_front_l_up)).getBitmap(); // // Ch1 前置大箭头向上
        resBmp[TChan.Ch1][discreet_front_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_front_normal)).getBitmap(); // // Ch1 前置普通箭头
        resBmp[TChan.Ch1][discreet_front_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_front_normal_down)).getBitmap(); // // Ch1 前置普通箭头向下
        resBmp[TChan.Ch1][discreet_front_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_front_normal_up)).getBitmap(); // // Ch1 前置普通箭头向上
        resBmp[TChan.Ch1][discreet_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_l)).getBitmap(); // // Ch1 大箭头
        resBmp[TChan.Ch1][discreet_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_l_down)).getBitmap(); // // Ch1 大箭头向下
        resBmp[TChan.Ch1][discreet_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_l_up)).getBitmap(); // // Ch1 大箭头向上
        resBmp[TChan.Ch1][discreet_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_normal)).getBitmap(); // // Ch1 普通箭头
        resBmp[TChan.Ch1][discreet_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_normal_down)).getBitmap(); // // Ch1 普通箭头向下
        resBmp[TChan.Ch1][discreet_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch1_normal_up)).getBitmap(); // // Ch1 普通箭头向上

        resBmp[TChan.Ch2][discreet_front_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_front_l)).getBitmap(); // // Ch2 前置大箭头
        resBmp[TChan.Ch2][discreet_front_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_front_l_down)).getBitmap(); // // Ch2 前置大箭头向下
        resBmp[TChan.Ch2][discreet_front_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_front_l_up)).getBitmap(); // // Ch2 前置大箭头向上
        resBmp[TChan.Ch2][discreet_front_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_front_normal)).getBitmap(); // // Ch2 前置普通箭头
        resBmp[TChan.Ch2][discreet_front_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_front_normal_down)).getBitmap(); // // Ch2 前置普通箭头向下
        resBmp[TChan.Ch2][discreet_front_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_front_normal_up)).getBitmap(); // // Ch2 前置普通箭头向上
        resBmp[TChan.Ch2][discreet_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_l)).getBitmap(); // // Ch2 大箭头
        resBmp[TChan.Ch2][discreet_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_l_down)).getBitmap(); // // Ch2 大箭头向下
        resBmp[TChan.Ch2][discreet_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_l_up)).getBitmap(); // // Ch2 大箭头向上
        resBmp[TChan.Ch2][discreet_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_normal)).getBitmap(); // // Ch2 普通箭头
        resBmp[TChan.Ch2][discreet_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_normal_down)).getBitmap(); // // Ch2 普通箭头向下
        resBmp[TChan.Ch2][discreet_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch2_normal_up)).getBitmap(); // // Ch2 普通箭头向上

        resBmp[TChan.Ch3][discreet_front_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_front_l)).getBitmap(); // // Ch3 前置大箭头
        resBmp[TChan.Ch3][discreet_front_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_front_l_down)).getBitmap(); // // Ch3 前置大箭头向下
        resBmp[TChan.Ch3][discreet_front_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_front_l_up)).getBitmap(); // // Ch3 前置大箭头向上
        resBmp[TChan.Ch3][discreet_front_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_front_normal)).getBitmap(); // // Ch3 前置普通箭头
        resBmp[TChan.Ch3][discreet_front_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_front_normal_down)).getBitmap(); // // Ch3 前置普通箭头向下
        resBmp[TChan.Ch3][discreet_front_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_front_normal_up)).getBitmap(); // // Ch3 前置普通箭头向上
        resBmp[TChan.Ch3][discreet_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_l)).getBitmap(); // // Ch3 大箭头
        resBmp[TChan.Ch3][discreet_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_l_down)).getBitmap(); // // Ch3 大箭头向下
        resBmp[TChan.Ch3][discreet_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_l_up)).getBitmap(); // // Ch3 大箭头向上
        resBmp[TChan.Ch3][discreet_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_normal)).getBitmap(); // // Ch3 普通箭头
        resBmp[TChan.Ch3][discreet_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_normal_down)).getBitmap(); // // Ch3 普通箭头向下
        resBmp[TChan.Ch3][discreet_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch3_normal_up)).getBitmap(); // // Ch3 普通箭头向上

        resBmp[TChan.Ch4][discreet_front_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_front_l)).getBitmap(); // // Ch4 前置大箭头
        resBmp[TChan.Ch4][discreet_front_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_front_l_down)).getBitmap(); // // Ch4 前置大箭头向下
        resBmp[TChan.Ch4][discreet_front_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_front_l_up)).getBitmap(); // // Ch4 前置大箭头向上
        resBmp[TChan.Ch4][discreet_front_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_front_normal)).getBitmap(); // // Ch4 前置普通箭头
        resBmp[TChan.Ch4][discreet_front_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_front_normal_down)).getBitmap(); // // Ch4 前置普通箭头向下
        resBmp[TChan.Ch4][discreet_front_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_front_normal_up)).getBitmap(); // // Ch4 前置普通箭头向上
        resBmp[TChan.Ch4][discreet_l] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_l)).getBitmap(); // // Ch4 大箭头
        resBmp[TChan.Ch4][discreet_l_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_l_down)).getBitmap(); // // Ch4 大箭头向下
        resBmp[TChan.Ch4][discreet_l_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_l_up)).getBitmap(); // // Ch4 大箭头向上
        resBmp[TChan.Ch4][discreet_normal] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_normal)).getBitmap(); // // Ch4 普通箭头
        resBmp[TChan.Ch4][discreet_normal_down] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_normal_down)).getBitmap(); // // Ch4 普通箭头向下
        resBmp[TChan.Ch4][discreet_normal_up] = ((BitmapDrawable) App.get().getResources().getDrawable(R.drawable.discreet_ch4_normal_up)).getBitmap(); // // Ch4 普通箭头向上

        resBmp[TChan.Ch5][discreet_front_l] = Tools.readSvgBmp(R.drawable.discreet_ch5_front_l); // // Ch5 前置大箭头（SVG方式读取）
        resBmp[TChan.Ch5][discreet_front_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch5_front_l_down); // // Ch5 前置大箭头向下
        resBmp[TChan.Ch5][discreet_front_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch5_front_l_up); // // Ch5 前置大箭头向上
        resBmp[TChan.Ch5][discreet_front_normal] = Tools.readSvgBmp(R.drawable.discreet_ch5_front_normal); // // Ch5 前置普通箭头
        resBmp[TChan.Ch5][discreet_front_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch5_front_normal_down); // // Ch5 前置普通箭头向下
        resBmp[TChan.Ch5][discreet_front_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch5_front_normal_up); // // Ch5 前置普通箭头向上
        resBmp[TChan.Ch5][discreet_l] = Tools.readSvgBmp(R.drawable.discreet_ch5_l); // // Ch5 大箭头
        resBmp[TChan.Ch5][discreet_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch5_l_down); // // Ch5 大箭头向下
        resBmp[TChan.Ch5][discreet_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch5_l_up); // // Ch5 大箭头向上
        resBmp[TChan.Ch5][discreet_normal] = Tools.readSvgBmp(R.drawable.discreet_ch5_normal); // // Ch5 普通箭头
        resBmp[TChan.Ch5][discreet_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch5_normal_down); // // Ch5 普通箭头向下
        resBmp[TChan.Ch5][discreet_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch5_normal_up); // // Ch5 普通箭头向上

        resBmp[TChan.Ch6][discreet_front_l] = Tools.readSvgBmp(R.drawable.discreet_ch6_front_l); // // Ch6 前置大箭头
        resBmp[TChan.Ch6][discreet_front_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch6_front_l_down); // // Ch6 前置大箭头向下
        resBmp[TChan.Ch6][discreet_front_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch6_front_l_up); // // Ch6 前置大箭头向上
        resBmp[TChan.Ch6][discreet_front_normal] = Tools.readSvgBmp(R.drawable.discreet_ch6_front_normal); // // Ch6 前置普通箭头
        resBmp[TChan.Ch6][discreet_front_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch6_front_normal_down); // // Ch6 前置普通箭头向下
        resBmp[TChan.Ch6][discreet_front_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch6_front_normal_up); // // Ch6 前置普通箭头向上
        resBmp[TChan.Ch6][discreet_l] = Tools.readSvgBmp(R.drawable.discreet_ch6_l); // // Ch6 大箭头
        resBmp[TChan.Ch6][discreet_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch6_l_down); // // Ch6 大箭头向下
        resBmp[TChan.Ch6][discreet_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch6_l_up); // // Ch6 大箭头向上
        resBmp[TChan.Ch6][discreet_normal] = Tools.readSvgBmp(R.drawable.discreet_ch6_normal); // // Ch6 普通箭头
        resBmp[TChan.Ch6][discreet_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch6_normal_down); // // Ch6 普通箭头向下
        resBmp[TChan.Ch6][discreet_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch6_normal_up); // // Ch6 普通箭头向上

        resBmp[TChan.Ch7][discreet_front_l] = Tools.readSvgBmp(R.drawable.discreet_ch7_front_l); // // Ch7 前置大箭头
        resBmp[TChan.Ch7][discreet_front_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch7_front_l_down); // // Ch7 前置大箭头向下
        resBmp[TChan.Ch7][discreet_front_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch7_front_l_up); // // Ch7 前置大箭头向上
        resBmp[TChan.Ch7][discreet_front_normal] = Tools.readSvgBmp(R.drawable.discreet_ch7_front_normal); // // Ch7 前置普通箭头
        resBmp[TChan.Ch7][discreet_front_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch7_front_normal_down); // // Ch7 前置普通箭头向下
        resBmp[TChan.Ch7][discreet_front_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch7_front_normal_up); // // Ch7 前置普通箭头向上
        resBmp[TChan.Ch7][discreet_l] = Tools.readSvgBmp(R.drawable.discreet_ch7_l); // // Ch7 大箭头
        resBmp[TChan.Ch7][discreet_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch7_l_down); // // Ch7 大箭头向下
        resBmp[TChan.Ch7][discreet_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch7_l_up); // // Ch7 大箭头向上
        resBmp[TChan.Ch7][discreet_normal] = Tools.readSvgBmp(R.drawable.discreet_ch7_normal); // // Ch7 普通箭头
        resBmp[TChan.Ch7][discreet_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch7_normal_down); // // Ch7 普通箭头向下
        resBmp[TChan.Ch7][discreet_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch7_normal_up); // // Ch7 普通箭头向上

        resBmp[TChan.Ch8][discreet_front_l] = Tools.readSvgBmp(R.drawable.discreet_ch8_front_l); // // Ch8 前置大箭头
        resBmp[TChan.Ch8][discreet_front_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch8_front_l_down); // // Ch8 前置大箭头向下
        resBmp[TChan.Ch8][discreet_front_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch8_front_l_up); // // Ch8 前置大箭头向上
        resBmp[TChan.Ch8][discreet_front_normal] = Tools.readSvgBmp(R.drawable.discreet_ch8_front_normal); // // Ch8 前置普通箭头
        resBmp[TChan.Ch8][discreet_front_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch8_front_normal_down); // // Ch8 前置普通箭头向下
        resBmp[TChan.Ch8][discreet_front_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch8_front_normal_up); // // Ch8 前置普通箭头向上
        resBmp[TChan.Ch8][discreet_l] = Tools.readSvgBmp(R.drawable.discreet_ch8_l); // // Ch8 大箭头
        resBmp[TChan.Ch8][discreet_l_down] = Tools.readSvgBmp(R.drawable.discreet_ch8_l_down); // // Ch8 大箭头向下
        resBmp[TChan.Ch8][discreet_l_up] = Tools.readSvgBmp(R.drawable.discreet_ch8_l_up); // // Ch8 大箭头向上
        resBmp[TChan.Ch8][discreet_normal] = Tools.readSvgBmp(R.drawable.discreet_ch8_normal); // // Ch8 普通箭头
        resBmp[TChan.Ch8][discreet_normal_down] = Tools.readSvgBmp(R.drawable.discreet_ch8_normal_down); // // Ch8 普通箭头向下
        resBmp[TChan.Ch8][discreet_normal_up] = Tools.readSvgBmp(R.drawable.discreet_ch8_normal_up); // // Ch8 普通箭头向上

    }
    //endregion


    //region 属性
    /** 电平线类型名称（如Value1/Value2/Value3/Value4） */
    private String VoltageLineType; // // 电平线类型标识
    /** 显示模式，默认为单箭头模式 */
    private int showMode = ITriggerLine.ShowMode_One; // // 显示模式：One=单箭头，Two=双箭头
    /**
     * 电平线的显现性
     */
    private boolean visibleLine = false; // // 电平线横线是否可见
    /**
     * 整个控件的显现性
     */
    private boolean visible = true; // // 整个电平线控件是否可见
    /**
     * 当前显示电平的通道号
     */
    private int channelId = TChan.Ch1; // // 当前所属通道号，决定颜色
    /**
     * 当前显示的所有电平的显示位置合集
     */
    private final double[] currYPos = new double[TChan.MaxLogicChan + 1];//保存的是屏幕高度1000对应的值 // // 各通道电平线的Y坐标（FPGA坐标系）
    /**
     * 当前显示电平的序列号（429模式下为0,1高,2低，其他模式下为0,1,2,3,4）
     */
    private int currYIndex; // // 当前操作的电平线索引号
    /**
     * 电平线周围显示的数据
     */
    private String text = ""; // // 电平线旁显示的文本（数值）
    /**
     * 一般情况下，为false，表示当前操作的电平显示实心图标其他为空心；为true时，表示当前的图标也为空心
     */
    private boolean curYNull = false; // // 是否所有图标均为空心（无选中状态）

    /** 是否参与绘制显示 */
    private boolean isShowState = true; // // 电平线是否参与绘制

    /** 需要显示的通道信息列表 */
    private final ArrayList<DiscreetVoltageLineInfoBean> listShowChannelInfo = new ArrayList<>(); // // 显示通道信息Bean列表

    /** Application上下文 */
    private final Context context = App.get().getApplicationContext(); // // 获取Application上下文
    //endregion

    //region
    /**
     * 电平图标
     */
    private final Bitmap[] bmp = new Bitmap[TChan.MaxLogicChan + 1]; // // 各通道的离屏绘制Bitmap（图标）

    /** 上一次的Bitmap，用于纹理刷新对比 */
    private final Bitmap[] oldBmp = new Bitmap[TChan.MaxLogicChan + 1]; // // 各通道上一帧的Bitmap（用于纹理失效通知）

    /** 各通道的离屏Canvas */
    private final Canvas[] mCanvas = new Canvas[TChan.MaxLogicChan + 1]; // // 各通道的离屏Canvas
    /** 绘制用画笔 */
    private final Paint paint; // // 画笔对象
    /**
     * 电平线
     */
    private Bitmap bmpLine,oldBmpLine; // // 电平横线Bitmap及其上一帧
    /** 电平横线Canvas */
    private Canvas mCanvasLine; // // 电平横线的离屏Canvas
    /** Bitmap是否有变更，需要刷新纹理 */
    private boolean isChangeBitmap = false; // // 标记Bitmap是否已变更
    /** OpenGL画布引用 */
    private ICanvasGL canvasGL; // // 保存ICanvasGL引用，用于纹理刷新

    /**
     * 通知OpenGL刷新纹理内容
     * 在draw()完成后调用，确保离屏Bitmap变更同步到GPU纹理
     */
    public void onRefresh() { // // 通知OpenGL刷新纹理
        if (canvasGL != null) { // // 如果画布引用不为空
            canvasGL.onRefreshTexture(); // // 刷新纹理
        }
    }
    //endregion


    /** 逻辑高电平状态标记 */
    public static final int TriggerVoltageLine_Logic_Hight = 0x01; // // 逻辑状态：高电平
    /** 逻辑低电平状态标记 */
    public static final int TriggerVoltageLine_Logic_Low = 0x02; // // 逻辑状态：低电平
    /**
     * 该预值电平没有开打，不进行绘制，不进行统计
     */
    public static final int TriggerVoltageLine_Logic_None = 0x03; // // 逻辑状态：未启用（不绘制不统计）
    /**
     * 不进行绘制，在统计范围内。 有些预值电平与其它通道预值电平相同，则不进行绘制
     */
    public static final int TriggerVoltageLine_NoDraw = 0x04; // // 逻辑状态：不绘制但参与统计

    /** 各通道的逻辑电平状态数组 */
    private final int[] TriggerVoltageLine_logic_state = new int[TChan.MaxLogicChan + 1]; // // 各通道的触发电平线逻辑状态

    /**
     * 批量设置所有通道的逻辑电平状态
     * @param logic_state 逻辑状态数组，长度必须与内部数组一致
     */
    public void setTriggerVoltageLine_logic_state(int[] logic_state) { // // 批量设置逻辑电平状态
        if (TriggerVoltageLine_logic_state.length != logic_state.length) return; // // 长度不一致则直接返回
        for (int i = 0; i < logic_state.length; i++) { // // 逐个复制状态值
            TriggerVoltageLine_logic_state[i] = logic_state[i]; // // 赋值
        }
        //Logger.i(TAG,Arrays.toString(TriggerVoltageLine_logic_state)+",serialNo:"+this.VoltageLineType);
        draw(); // // 状态变更后重新绘制
    }

    /**
     * 设置指定通道的逻辑电平状态
     * @param chId 通道号
     * @param state 逻辑状态值
     */
    public void setTriggerVoltageLine_logic_state(int chId, int state) { // // 设置指定通道的逻辑电平状态
        TriggerVoltageLine_logic_state[chId] = state; // // 直接赋值
        //Logger.i(TAG,"arrays:"+ Arrays.toString(TriggerVoltageLine_logic_state)+",serialNo:"+this.VoltageLineType);
        draw(); // // 状态变更后重新绘制
    }

    /**
     * 批量设置逻辑电平状态：指定通道为高电平，其余为None
     * @param chId 需要设置为高电平的通道号可变参数
     */
    public void setTriggerVoltageLine_logic_states(int... chId) { // // 设置指定通道为高电平，其余为None
        Arrays.fill(TriggerVoltageLine_logic_state, TriggerVoltageLine_Logic_None); // // 先全部填充为None
        for (int i = 0; i < chId.length; i++) { // // 遍历指定通道
            TriggerVoltageLine_logic_state[chId[i]] = TriggerVoltageLine_Logic_Hight; // // 设置为高电平
        }
    }

    /**
     * 获取所有通道的逻辑电平状态数组
     * @return 逻辑电平状态数组
     */
    public int[] getTriggerVoltageLine_logic_state(){ // // 获取逻辑电平状态数组
        return TriggerVoltageLine_logic_state; // // 返回内部数组引用
    }

    /**
     * 构造方法，初始化电平线类型、资源、参数、Bitmap和画笔
     * @param VoltageLineType 电平线类型名称（Value1/Value2/Value3/Value4）
     */
    public DiscreetVoltageLine(String VoltageLineType) { // // 构造方法
        this.VoltageLineType = VoltageLineType; // // 保存电平线类型
        initRes(); // // 初始化图标资源
        initParam(); // // 初始化位置参数
        reInitBmp(); // // 初始化离屏Bitmap
        paint = new Paint(); // // 创建画笔
        paint.setTextSize(20); // // 设置文本大小20px
        paint.setTypeface(Typeface.DEFAULT_BOLD); // // 设置粗体字体
        paint.setFlags(Paint.ANTI_ALIAS_FLAG); // // 开启抗锯齿
        paint.setStrokeWidth(1); // // 设置线宽1px

        draw(); // // 初始绘制
    }

    /**
     * 初始化位置参数，将所有通道的Y坐标设为屏幕中央
     */
    private void initParam() { // // 初始化位置参数
        Arrays.fill(currYPos, GlobalVar.get().getMainWave().y / 2); // // 所有通道Y坐标初始化为屏幕高度的一半
    }

    //region ITriggerLine
    /**
     * 根据电平线类型获取对应的序列号ID
     * @return 序列号ID（S1~S4）
     */
    public int getNameId() { // // 获取电平线名称ID
        if (this.VoltageLineType.equals(VoltageLineManage.VoltageLineType_Value1)) { // // 如果是Value1
            return TChan.S1; // // 返回S1
        } else if (this.VoltageLineType.equals(VoltageLineManage.VoltageLineType_Value2)) { // // 如果是Value2
            return TChan.S2; // // 返回S2
        } else if (this.VoltageLineType.equals(VoltageLineManage.VoltageLineType_Value3)) { // // 如果是Value3
            return TChan.S3; // // 返回S3
        } else { // // 其他情况
            return TChan.S4; // // 返回S4
        }
    }

    /**
     * 获取电平线类型名称
     * @return 电平线类型名称字符串
     */
    public String getName() { // // 获取电平线名称
        return this.VoltageLineType; // // 返回类型名称
    }


    /** 电平线是否处于激活（前置/选中）状态 */
    private volatile boolean bActive = false; // // 激活状态标记，volatile保证线程可见性

    /**
     * 根据激活状态获取大箭头图标的资源索引
     * @return 前置大箭头或普通大箭头的索引
     */
    private int getDiscreet_l() { // // 获取大箭头图标索引
        return bActive ? discreet_front_l : discreet_l; // // 激活则返回前置样式，否则返回普通样式
    }

    /**
     * 根据激活状态获取大箭头+向下图标的资源索引
     * @return 前置或普通的大箭头向下索引
     */
    private int getDiscreet_l_down() { // // 获取大箭头向下图标索引
        return bActive ? discreet_front_l_down : discreet_l_down; // // 激活返回前置，否则普通
    }

    /**
     * 根据激活状态获取大箭头+向上图标的资源索引
     * @return 前置或普通的大箭头向上索引
     */
    private int getDiscreet_l_up() { // // 获取大箭头向上图标索引
        return bActive ? discreet_front_l_up : discreet_l_up; // // 激活返回前置，否则普通
    }

    /**
     * 根据激活状态获取普通箭头图标的资源索引
     * @return 前置或普通的普通箭头索引
     */
    private int getDiscreet_normal() { // // 获取普通箭头图标索引
        return bActive ? discreet_front_normal : discreet_normal; // // 激活返回前置，否则普通
    }

    /**
     * 根据激活状态获取普通箭头+向下图标的资源索引
     * @return 前置或普通的普通箭头向下索引
     */
    private int getDiscreet_normal_down() { // // 获取普通箭头向下图标索引
        return bActive ? discreet_front_normal_down : discreet_normal_down; // // 激活返回前置，否则普通
    }

    /**
     * 根据激活状态获取普通箭头+向上图标的资源索引
     * @return 前置或普通的普通箭头向上索引
     */
    private int getDiscreet_normal_up() { // // 获取普通箭头向上图标索引
        return bActive ? discreet_front_normal_up : discreet_normal_up; // // 激活返回前置，否则普通
    }

    /**
     * 设置电平线的激活状态，激活时显示前置图标样式
     * @param bActive true=激活（前置样式），false=非激活
     */
    @Override
    public void setActive(boolean bActive) { // // 设置激活状态
        this.bActive = bActive; // // 保存激活状态
        draw(); // // 激活状态变更后重新绘制

    }

    /**
     * 查询电平线是否处于激活状态
     * @return true=激活，false=非激活
     */
    @Override
    public boolean isActive() { // // 查询激活状态
        return bActive; // // 返回激活标记
    }

    /**
     * 设置当前所属通道号，通道号决定绘制颜色
     * @param channelId 通道号
     */
    @Override
    public void setChannelId(int channelId) { // // 设置通道号
        if (this.channelId == channelId) return; // // 通道号未变则直接返回
        this.channelId = channelId; // // 更新通道号
        draw(); // // 通道变更后重新绘制
    }

    /**
     * 获取当前所属通道号
     * @return 通道号
     */
    @Override
    public int getChannelId() { // // 获取通道号
        return channelId; // // 返回当前通道号
    }

    /**
     * 设置整个电平线控件的可见性
     * @param visible true=可见，false=不可见
     */
    @Override
    public void setVisible(boolean visible) { // // 设置控件可见性
        this.visible = visible; // // 保存可见性
    }

    /**
     * 查询整个电平线控件是否可见
     * @return true=可见，false=不可见
     */
    @Override
    public boolean getVisible() { // // 查询控件可见性
        return visible; // // 返回可见性标记
    }

    /**
     * 设置电平线横线的可见性
     * @param visibleLine true=显示横线，false=只显示图标
     */
    @Override
    public void setVisibleLine(boolean visibleLine) { // // 设置横线可见性
        if (this.visibleLine != visibleLine) { // // 值有变化才处理
            this.visibleLine = visibleLine; // // 更新横线可见性
            draw(); // // 可见性变更后重新绘制
        }
    }

    /**
     * 查询电平线横线是否可见
     * @return true=可见，false=不可见
     */
    @Override
    public boolean getVisibleLine() { // // 查询横线可见性
        return this.visibleLine; // // 返回横线可见性标记
    }

    /**
     * 设置显示模式（单箭头/双箭头）
     * @param showMode ShowMode_One或ShowMode_Two
     */
    @Override
    public void setShowMode(int showMode) { // // 设置显示模式
        if (this.showMode != showMode) { // // 值有变化才处理
            this.showMode = showMode; // // 更新显示模式
            draw(); // // 模式变更后重新绘制
        }
    }

    /**
     * 获取当前显示模式
     * @return 显示模式常量
     */
    @Override
    public int getShowMode() { // // 获取显示模式
        return this.showMode; // // 返回当前显示模式
    }

    /**
     * 获取当前操作的电平线索引号
     * @return 当前索引号
     */
    @Override
    public int getCurrYIndex() { // // 获取当前电平线索引号
        return currYIndex; // // 返回当前索引号
    }

    /***
     * 设置当前序号,当设为0时，并不会真正的设为序列号号0，而是使全部图标变成空突变
     * 为0的时候没有赋值，所以可以保留原来的选择。
     * @param currYIndex {@link IWave IWave.Ch1 - IWave.Ch4}
     */
    @Override
    public void setCurrYIndex(int currYIndex) { // // 设置当前操作电平线索引号
        Logger.i(TAG, "VoltageLineType:" + VoltageLineType + "," + "index:" + currYIndex + ",currYindex:" + this.currYIndex + ",curYNull:" + curYNull); // // 记录索引变更日志

        if (currYIndex == 0) { // // 索引为0表示取消选中

            curYNull = true; // // 标记所有图标为空心
            draw(); // // 重新绘制
        } else { // // 索引非0，正常设置
            curYNull = false; // // 取消空心标记
            this.currYIndex = currYIndex; // // 更新当前索引号
            draw(); // // 重新绘制

        }
    }

    /**
     * 根据偏移量调整当前电平线的Y位置
     * @param offsetY Y方向偏移量（像素，向上为正）
     * @return true=位置超过边界被截断，false=位置在正常范围内
     */
    @Override
    public boolean setOffsetY(double offsetY) { // // 根据偏移量调整Y位置
        if (!visible) { // // 如果控件不可见
//            return false;
            return setOffsetYHide(offsetY); // // 不可见时仍更新位置但不绘制
        }
        Log.d(TAG, "VoltageLineType:" + VoltageLineType + "," + "setOffsetY() called with: offsetY = [" + offsetY + "]"); // // 调试日志
        boolean change = false; // // 是否越界标志
        if (offsetY != 0) { // // 偏移量非0才处理
            double temY = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY); // // 计算新的Y坐标（UI坐标）
            if (temY < 0) { // // 超出上边界
                currYPos[currYIndex] = 0; // // 钳位到0
                change = true; // // 标记越界
            } else if (temY > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // // 超出下边界
                currYPos[currYIndex] =  GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff(); // // 钳位到底部
                change = true; // // 标记越界
            } else { // // 正常范围内
                currYPos[currYIndex] = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY) * ScopeBase.getToFPGACoff(); // // UI坐标转FPGA坐标保存
                change = false; // // 未越界
            }
            draw(); // // 位置变更后重新绘制
        }
        return change; // // 返回是否越界
    }

    /**
     * 控件不可见时，根据偏移量更新Y位置（不触发绘制）
     * @param offsetY Y方向偏移量
     * @return true=位置超过边界被截断，false=位置在正常范围内
     */
    private boolean setOffsetYHide(double offsetY) { // // 不可见模式下的偏移量调整
        Log.d(TAG, "VoltageLineType:" + VoltageLineType + "," + "setOffsetYHide() called with: offsetY = [" + offsetY + "]"); // // 调试日志
        boolean change = false; // // 是否越界标志
        if (offsetY != 0) { // // 偏移量非0才处理
            double temY = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY); // // 计算新的Y坐标（UI坐标）
            if (temY < 0) { // // 超出上边界
                currYPos[currYIndex] = 0; // // 钳位到0
                change = true; // // 标记越界
            } else if (temY > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // // 超出下边界
                currYPos[currYIndex] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff(); // // 钳位到底部
                change = true; // // 标记越界
            } else { // // 正常范围内
                currYPos[currYIndex] = ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff() - offsetY) * ScopeBase.getToFPGACoff(); // // UI坐标转FPGA坐标保存
            }
        }
        return change; // // 返回是否越界
    }

    /**
     * 直接设置当前索引电平线的Y坐标（屏幕实际像素位置）
     * @param currY Y坐标（屏幕像素值）
     * @return true=位置超过边界被截断，false=位置在正常范围内
     */
    @Override
    public boolean setCurrY(double currY) {//屏幕实际位置 // // 设置当前索引的Y坐标
        if (!visible) return false; // // 控件不可见则不处理
        Logger.d(TAG, "VoltageLineType:" + VoltageLineType + "," + "setCurrY() called with: ch = [" + currYIndex + "], y = [" + currY + "]," + VoltageLineType + "," + channelId); // // 调试日志

        boolean change = false; // // 是否越界标志
        if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) != currY) { // // 当前位置与目标位置不同才处理
            if (currY < 0) { // // 超出上边界
                this.currYPos[currYIndex] = 0; // // 钳位到0
                change = true; // // 标记越界
            } else if (currY > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // // 超出下边界
                this.currYPos[currYIndex] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff(); // // 钳位到底部（FPGA坐标）
                change = true; // // 标记越界
            } else { // // 正常范围内
                this.currYPos[currYIndex] = currY * ScopeBase.getToFPGACoff(); // // UI坐标转FPGA坐标保存
                change = false; // // 未越界
            }
            draw(); // // 位置变更后重新绘制
        }
        return change; // // 返回是否越界
    }

    /**
     * 设置指定通道电平线的Y坐标（屏幕实际像素位置）
     * @param ch 通道号
     * @param y Y坐标（屏幕像素值）
     * @return true=位置超过边界被截断，false=位置在正常范围内
     */
    @Override
    public boolean setOtherY(int ch, double y) {//y屏幕实际位置 // // 设置指定通道的Y坐标
        if (!visible) { // // 控件不可见时
//            return false;
            return setOtherYHide(ch, y); // // 不可见时仍更新位置但不绘制
        }
        Logger.d(TAG, "VoltageLineType:" + VoltageLineType + "," + "setOtherY() called with: ch = [" + ch + "], y = [" + y + "]," + VoltageLineType + "," + channelId); // // 调试日志

        boolean change = false; // // 是否越界标志
        if (ScopeBase.changeAccuracy(this.currYPos[ch] * ScopeBase.getToUICoff()) != y) { // // 当前位置与目标位置不同才处理
            if (y < 0) { // // 超出上边界
                this.currYPos[ch] = 0; // // 钳位到0
                change = true; // // 标记越界
            } else if (y > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // // 超出下边界
                this.currYPos[ch] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff(); // // 钳位到底部（FPGA坐标）
                change = true; // // 标记越界
            } else { // // 正常范围内
                this.currYPos[ch] = y * ScopeBase.getToFPGACoff(); // // UI坐标转FPGA坐标保存
                change = false; // // 未越界
            }
            draw(); // // 位置变更后重新绘制
        }
        return change; // // 返回是否越界
    }

    /**
     * 控件不可见时，设置指定通道的Y坐标（不触发绘制）
     * @param ch 通道号
     * @param y Y坐标（屏幕像素值）
     * @return true=位置超过边界被截断，false=位置在正常范围内
     */
    private boolean setOtherYHide(int ch, double y) { // // 不可见模式下设置指定通道Y坐标
        boolean change = false; // // 是否越界标志
        Log.d(TAG, "VoltageLineType:" + VoltageLineType + "," + "setOtherYHide() called with: ch = [" + ch + "], y = [" + y + "]"); // // 调试日志
        if (ScopeBase.changeAccuracy(this.currYPos[ch] * ScopeBase.getToUICoff()) != y) { // // 位置有变化才处理
            if (y < 0) { // // 超出上边界
                this.currYPos[ch] = 0; // // 钳位到0
                change = true; // // 标记越界
            } else if (y > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // // 超出下边界
                this.currYPos[ch] = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) * ScopeBase.getToFPGACoff(); // // 钳位到底部
                change = true; // // 标记越界
            } else { // // 正常范围内
                this.currYPos[ch] = y * ScopeBase.getToFPGACoff(); // // UI坐标转FPGA坐标保存
            }
        }
        return change; // // 返回是否越界
    }

    /**
     * 设置电平线旁显示的文本内容
     * @param text 要显示的文本
     */
    @Override
    public void setText(String text) { // // 设置电平线旁文本
        if (!this.text.equals(text)) { // // 文本内容有变化才处理
            this.text = text; // // 更新文本
            draw(); // // 文本变更后重新绘制
        }
    }

    /**
     * 获取电平线旁显示的文本内容
     * @return 当前文本
     */
    @Override
    public String getText() { // // 获取电平线旁文本
        return text; // // 返回当前文本
    }


    /**
     * 获取当前索引电平线的Y坐标（屏幕实际像素位置）
     * @return Y屏幕坐标
     */
    @Override
    public double getCurrY() { // // 获取当前索引的Y坐标
        return ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff()); // // FPGA坐标转UI坐标返回
    }

    /**
     * 获取指定通道电平线的Y坐标（屏幕实际像素位置）
     * @param ch 通道号
     * @return Y屏幕坐标
     */
    @Override
    public double getOtherY(int ch) { // // 获取指定通道的Y坐标
        return ScopeBase.changeAccuracy(currYPos[ch] * ScopeBase.getToUICoff()); // // FPGA坐标转UI坐标返回
    }

    /**
     * 获取所有通道电平线的Y坐标数组（屏幕实际像素位置）
     * @return Y坐标数组
     */
    @Override
    public double[] getCurrYAll() { // // 获取所有通道Y坐标
        double[] temp = new double[TChan.MaxLogicChan + 1]; // // 创建临时数组
        for (int i = 0; i < currYPos.length; i++) { // // 遍历所有通道
            temp[i] = ScopeBase.changeAccuracy(currYPos[i] * ScopeBase.getToUICoff()); // // 逐个转换FPGA坐标到UI坐标
        }
        return temp; // // 返回UI坐标数组
    }


    /**
     * 切换工作模式时，根据系数调整所有通道的Y坐标位置
     * @param workMode 目标工作模式（YT/YTZOOM）
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) { // // 切换工作模式
        switch (workMode) { // // 根据工作模式类型调整
            case IWorkMode.WorkMode_YT: // // 切换到YT模式
                for (int i = 0; i < currYPos.length; i++) { // // 遍历所有通道
                    currYPos[i] = currYPos[i] * GlobalVar.get().toYTCoef(); // // 乘以YT转换系数
                }
                break; // // 跳出
            case IWorkMode.WorkMode_YTZOOM: // // 切换到YT缩放模式
                for (int i = 0; i < currYPos.length; i++) { // // 遍历所有通道
                    currYPos[i] = currYPos[i] * GlobalVar.get().toZoomCoef(); // // 乘以缩放转换系数
                }
                break; // // 跳出

        }
        draw(); // // 模式切换后重新绘制
    }

    /**
     * 在系统Canvas上绘制电平线（已弃用，保留接口）
     * @param canvas 系统画布
     */
    @Override
    public void draw(Canvas canvas) { // // 系统Canvas绘制（已弃用）
//        synchronized (bmp) {
//            canvas.drawBitmap(bmpLine, 0, currYPos[currYIndex], null);
//            switch (this.showMode) {
//                case ShowMode_One:
//                    drawOne(canvas);
//                    break;
//                case ShowMode_Two:
//                    drawTwo(canvas);
//                    break;
//                //case ShowMode_Three:drawThree(canvas);break;
//            }
//        }
    }

    /**
     * 在OpenGL画布上绘制电平线（主要绘制入口）
     * 绘制电平横线，并根据显示模式绘制对应的图标
     * @param canvas OpenGL画布
     */
    @Override
    public void draw(ICanvasGL canvas) { // // OpenGL画布绘制（主要绘制入口）
        if (!isShowState) return; // // 不参与显示则直接返回
        if (!visible) return; // // 控件不可见则直接返回
        synchronized (bmp) { // // 同步锁，防止并发修改Bitmap
            canvasGL = canvas; // // 保存画布引用
            if (isChangeBitmap) { // // 如果Bitmap有变更
                canvas.invalidateTextureContent(bmpLine,oldBmpLine); // // 通知纹理失效，使用新旧Bitmap对比刷新
                oldBmpLine = null; // // 清空旧Bitmap引用
            }
            canvas.drawBitmap(bmpLine, 0, (int) Math.round(ScopeBase.changeAccuracy(currYPos[currYIndex] * ScopeBase.getToUICoff()))); // // 绘制电平横线到目标Y位置
            switch (this.showMode) { // // 根据显示模式绘制图标
                case ShowMode_One: // // 单箭头模式
                    drawOne(canvas); // // 绘制单箭头
                    break;
                case ShowMode_Two: // // 双箭头模式
                    drawTwo(canvas); // // 绘制双箭头
                    break;
                //case ShowMode_Three:drawThree(canvas);break;
            }
            isChangeBitmap = false; // // 重置变更标记
        }
    }

//    private void drawOne(Canvas canvas) {
//        for (int i = 1; i < 5; i++) {
//            if (currYPos[i] < resBmp[1][0].getHeight()) {
//                int temY = resBmp[1][0].getHeight();
//                temY = temY - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY, null);
//            } else if (currYPos[i] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight()) {
//                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                temY = temY - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY, null);
//            } else {
//                int temY = currYPos[i] - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY, null);
//            }
//        }
//    }

    /**
     * 单箭头模式下的OpenGL画布绘制
     * 先绘制非当前通道的图标，再绘制当前通道的图标（确保当前通道在最上层）
     * @param canvas OpenGL画布
     */
    private void drawOne(ICanvasGL canvas) { // // 单箭头模式绘制
//        for (int i = 1; i < TChan.MaxLogicChan + 1; i++) {
//            if (i == currYIndex) continue;
//            drawToCanvasGL(canvas, i);
//        }
        TChan.foreachChan((i) -> { // // 遍历所有通道
                    drawToCanvasGL(canvas, i); // // 绘制每个通道的图标
                },
                (chIdx) -> chIdx == currYIndex // // 过滤掉当前索引通道（最后绘制）
        );
        drawToCanvasGL(canvas, currYIndex); // // 最后绘制当前索引通道（最上层）
    }

//    private void drawTwo(Canvas canvas) {
//        //高小于低，就跟着跑，反之也是如此
//        //高电平在上，像素小，低电平在下，像素大
//        if (currYPos[1] > currYPos[2]) {
//            //System.out.println("向下 currY1:"+currYPos[1]+"  currY2:"+currYPos[2]);
//            if (currYIndex == 1) currYPos[2] = currYPos[1];
//            else currYPos[1] = currYPos[2];
//        }
//
//
//        for (int i = 1; i < 3; i++) {
//            if (currYPos[i] < resBmp[1][0].getHeight()) {
//                int temY = resBmp[1][0].getHeight();
//                temY = temY - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY, null);
//            } else if (currYPos[i] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight()) {
//                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                temY = temY - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY, null);
//            } else {
//                int temY = currYPos[i] - (bmp[i].getHeight() - 1) / 2;
//                canvas.drawBitmap(bmp[i], 0, temY, null);
//            }
//        }
//
//    }

    /**
     * 模式2的跟随
     */
    private void twoFollow() { // // 双箭头模式下高低电平跟随逻辑
        //高小于低，就跟着跑，反之也是如此
        //高电平在上，像素小，低电平在下，像素大
        if (currYPos[1] > currYPos[2]) { // // 如果高电平（索引1）位置低于低电平（索引2）
            //System.out.println("向下 currY1:"+currYPos[1]+"  currY2:"+currYPos[2]);
            if (currYIndex == 1) { // // 如果当前拖动的是高电平
                currYPos[2] = currYPos[1]; // // 低电平跟随高电平
            } else { // // 如果当前拖动的是低电平
                currYPos[1] = currYPos[2]; // // 高电平跟随低电平
            }
        }

    }

    /**
     * 双箭头模式下的OpenGL画布绘制
     * 先执行跟随逻辑，再绘制非当前通道和当前通道的图标
     * @param canvas OpenGL画布
     */
    private void drawTwo(ICanvasGL canvas) { // // 双箭头模式绘制
        //高小于低，就跟着跑，反之也是如此
        //高电平在上，像素小，低电平在下，像素大
//        if (currYPos[1] > currYPos[2]) {
//            //System.out.println("向下 currY1:"+currYPos[1]+"  currY2:"+currYPos[2]);
//            if (currYIndex == 1) {
//                currYPos[2] = currYPos[1];
//            } else {
//                currYPos[1] = currYPos[2];
//            }
//        }
        twoFollow(); // // 执行高低电平跟随逻辑
        for (int i = 1; i < 5; i++) { // // 遍历前4个通道
            if (i == currYIndex) continue; // // 跳过当前索引通道
            drawToCanvasGL(canvas, i); // // 绘制非当前通道图标
        }
        drawToCanvasGL(canvas, currYIndex); // // 最后绘制当前索引通道（最上层）

    }

    /**
     * 将指定通道的电平线图标绘制到OpenGL画布
     * 处理越界情况：超出上边界显示向上箭头，超出下边界显示向下箭头
     * @param canvas OpenGL画布
     * @param chId 通道号
     */
    private void drawToCanvasGL(ICanvasGL canvas, int chId) { // // 绘制指定通道图标到CanvasGL
//        Logger.i(TAG,"name:"+VoltageLineType+"  Id:"+chId+" pos:"+currYPos[chId]);
        if (isChangeBitmap) { // // 如果Bitmap有变更
            canvas.invalidateTextureContent(bmp[chId], oldBmp[chId]); // // 通知纹理失效
            oldBmp[chId] = null; // // 清空旧Bitmap引用
        }

        if (currYPos[chId] < 0) { // // 超出上边界
            int temY = resBmp[1][0].getHeight(); // // 计算顶部显示位置
            temY = temY - (bmp[chId].getHeight() - 1) / 2; // // 居中对齐

            canvas.drawBitmap(bmp[chId], 0, temY); // // 在顶部绘制图标
        } else if (ScopeBase.changeAccuracy(currYPos[chId] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // // 超出下边界
            int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // // 计算底部显示位置
            temY = temY - (bmp[chId].getHeight() - 1) / 2; // // 居中对齐

            canvas.drawBitmap(bmp[chId], 0, temY); // // 在底部绘制图标
        } else { // // 正常范围内
            int temY = (int) Math.round(ScopeBase.changeAccuracy(currYPos[chId] * ScopeBase.getToUICoff())) - (bmp[chId].getHeight() - 1) / 2; // // 计算居中Y坐标

            canvas.drawBitmap(bmp[chId], 0, temY); // // 在目标位置绘制图标
        }
    }

    /**
     * 设置电平线是否参与绘制显示
     * @param show true=显示，false=隐藏
     */
    @Override
    public void setShowState(boolean show) { // // 设置是否参与显示
        if (isShowState != show) { // // 值有变化才处理
            isShowState = show; // // 更新显示状态
            draw(); // // 状态变更后重新绘制
        }
    }

    /**
     * 查询电平线是否参与绘制显示
     * @return true=显示，false=隐藏
     */
    @Override
    public boolean getShowState() { // // 查询是否参与显示
        return isShowState; // // 返回显示状态
    }

    /**
     * 获取所有需要显示的通道电平线信息列表
     * 根据当前显示模式和逻辑状态，生成DiscreetVoltageLineInfoBean列表
     * @return 通道显示信息列表
     */
    @Override
    public ArrayList<DiscreetVoltageLineInfoBean> getShowChannelInfo() { // // 获取显示通道信息列表
        listShowChannelInfo.clear(); // // 清空旧列表
        if (showMode == ITriggerLine.ShowMode_One) { // // 单箭头模式
//            for (int i = 1; i < TChan.MaxLogicChan + 1; i++) {
//                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue;
//                DiscreetVoltageLineInfoBean bean = new DiscreetVoltageLineInfoBean();
//                bean.ChannelId = i;
//                bean.ShowMode = showMode;
//                bean.VoltageLineName = VoltageLineType;
//                bean.VoltageLineChannelIndex = VoltageLine_Normal;
//                listShowChannelInfo.add(bean);
//            }
            TChan.foreachChan((i) -> { // // 遍历所有有效通道
                        DiscreetVoltageLineInfoBean bean = new DiscreetVoltageLineInfoBean(); // // 创建信息Bean
                        bean.ChannelId = i; // // 设置通道号
                        bean.ShowMode = showMode; // // 设置显示模式
                        bean.VoltageLineName = VoltageLineType; // // 设置电平线名称
                        bean.VoltageLineChannelIndex = VoltageLine_Normal; // // 设置为普通索引
                        listShowChannelInfo.add(bean); // // 添加到列表
                    },
                    (chIdx) -> TriggerVoltageLine_logic_state[chIdx] == TriggerVoltageLine_Logic_None // // 过滤掉None状态的通道
            );
        } else if (showMode == ITriggerLine.ShowMode_Two) { // // 双箭头模式
            DiscreetVoltageLineInfoBean bean = new DiscreetVoltageLineInfoBean(); // // 创建高电平信息Bean
            bean.ChannelId = channelId; // // 设置通道号
            bean.ShowMode = showMode; // // 设置显示模式
            bean.VoltageLineName = VoltageLineType; // // 设置电平线名称
            bean.VoltageLineChannelIndex = VoltageLine_High; // // 设置为高电平索引
            listShowChannelInfo.add(bean); // // 添加到列表

            bean = new DiscreetVoltageLineInfoBean(); // // 创建低电平信息Bean
            bean.ChannelId = channelId; // // 设置通道号
            bean.ShowMode = showMode; // // 设置显示模式
            bean.VoltageLineName = VoltageLineType; // // 设置电平线名称
            bean.VoltageLineChannelIndex = VoltageLine_Low; // // 设置为低电平索引
            listShowChannelInfo.add(bean); // // 添加到列表
        }


        return listShowChannelInfo; // // 返回信息列表
    }

    /** 当前工作模式缓存 */
    private @IWorkMode.WorkMode int workMode = IWorkMode.WorkMode_None; // // 工作模式缓存，用于检测模式切换

    /**
     * 重新初始化离屏Bitmap
     * 当工作模式变化时，根据新的波形区域尺寸重新创建Bitmap和Canvas
     */
    private void reInitBmp(){ // // 重新初始化离屏Bitmap
        if(workMode != WorkModeManage.getInstance().getmWorkMode()){ // // 工作模式有变化
            workMode = WorkModeManage.getInstance().getmWorkMode(); // // 更新缓存的工作模式
            for (int i = 0; i < bmp.length; i++) { // // 遍历所有通道
                oldBmp[i] = bmp[i]; // // 保存旧Bitmap用于纹理刷新
                bmp[i] = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(workMode), 60, Bitmap.Config.ARGB_8888); // // 创建新的图标Bitmap（宽度=波形区宽度，高度=60px）
                mCanvas[i] = new Canvas(bmp[i]); // // 创建对应的Canvas
            }
            oldBmpLine = bmpLine; // // 保存旧的横线Bitmap
            bmpLine = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(workMode), 1, Bitmap.Config.ARGB_8888); // // 创建新的横线Bitmap（1px高）
            mCanvasLine = new Canvas(bmpLine); // // 创建对应的Canvas
        }

    }

    /**
     * 刷新电平线，重新初始化Bitmap并重绘
     * 在波形区域尺寸变化时调用
     */
    @Override
    public void refresh() { // // 刷新电平线
        reInitBmp(); // // 重新初始化Bitmap
        draw(); // // 重新绘制
    }

    //endregion

    //region 私有
    /**
     * 离屏绘制电平线到bmp和bmpLine
     * 使用PorterDuff.Mode.CLEAR清空画布，然后根据显示模式绘制图标和横线
     */
    private void draw() { // // 离屏绘制核心方法
        synchronized (bmp) { // // 同步锁，防止并发修改
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); // // 设置画笔为清空模式
//            for (int i = 0; i < 5; i++) {
//                mCanvas[i].drawPaint(paint);
//                mCanvasLine.drawPaint(paint);
//            }
            TChan.foreachChan((i) -> { // // 遍历所有有效通道
                mCanvas[i].drawPaint(paint); // // 清空图标Canvas
                mCanvasLine.drawPaint(paint); // // 清空横线Canvas
            });
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC)); // // 恢复画笔为正常绘制模式
//            Logger.d(TAG, "showMode:" + showMode);
            switch (showMode) { // // 根据显示模式选择绘制方式
                case ITriggerLine.ShowMode_One: // // 单箭头模式
                    drawOne(); // // 绘制单箭头
                    break;
                case ITriggerLine.ShowMode_Two: // // 双箭头模式
                    twoFollow(); // // 执行高低电平跟随
                    drawTwo(); // // 绘制双箭头
                    break;
                //case ITriggerLine.ShowMode_Three:drawThree();break;
            }
            isChangeBitmap = true; // // 标记Bitmap已变更
            onRefresh(); // // 通知OpenGL刷新纹理
        }
    }

    /**
     * 单箭头模式下的离屏绘制
     * 先绘制横线，再绘制各通道的图标
     */
    private void drawOne() { // // 单箭头模式离屏绘制
        if (this.visibleLine) { // // 如果需要显示横线
            paint.setColor(TChan.getChannelColor(context, channelId)); // // 设置横线颜色为通道颜色
            mCanvasLine.drawLine(0, 0, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), 0, paint); // // 绘制横线（从左到右）
//            for (int i = 1; i < TChan.MaxLogicChan + 1; i++) {
//                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue;
//                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_NoDraw) continue;
//                if (i == currYIndex && !curYNull) {
//                    if (this.currYPos[currYIndex] < 0) {
//                        int temY = resBmp[1][0].getHeight();
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_up()].getWidth(),
//                                (float) ((bmp[i].getHeight() - 1) / 2 - temY), paint);
//                        drawText(this.currYPos[currYIndex], 35);
//                    } else if (this.currYPos[currYIndex] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
//                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                        temY = bmp[channelId].getHeight() - resBmp[i][getDiscreet_l_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_down()].getWidth(),
//                                temY, paint);
//                        drawText(this.currYPos[currYIndex], 0);
//                    } else {
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l()].getWidth(),
//                                (float) (bmp[currYIndex].getHeight() - resBmp[currYIndex][getDiscreet_l()].getHeight()) / 2, paint);
//                        drawText(this.currYPos[currYIndex], 35);
//                    }
//                } else {
//                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//                    mCanvas[i].drawPaint(paint);
//                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
//
//                    if (this.currYPos[i] < 0) {
//                        int temY = resBmp[1][0].getHeight();
//                        mCanvas[i].drawBitmap(resBmp[i][discreet_l_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_up].getWidth(),
//                                (float) ((bmp[i].getHeight() - 1) / 2 - temY), paint);
//                    } else if (this.currYPos[i] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
//                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                        temY = bmp[channelId].getHeight() - resBmp[i][discreet_l_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                        mCanvas[i].drawBitmap(resBmp[i][discreet_l_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_down].getWidth(),
//                                temY, paint);
//                    } else {
//                        mCanvas[i].drawBitmap(resBmp[i][discreet_l], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l].getWidth(),
//                                (float) (bmp[i].getHeight() - resBmp[i][discreet_l].getHeight()) / 2, paint);
//
//                    }
//                }
//            }
            TChan.foreachChan((i) -> { // // 遍历所有有效通道
                        if (i == currYIndex && !curYNull) { // // 当前索引通道且非空心状态
                            if (this.currYPos[currYIndex] < 0) { // // 超出上边界
                                int temY = resBmp[1][0].getHeight(); // // 获取顶部偏移
                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_up()].getWidth(), // // 绘制前置大箭头向上图标
                                        ((bmp[i].getHeight() - 1) / 2 - temY), paint); // // 居中对齐绘制
                                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35); // // 绘制数值文本
                            } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // // 超出下边界
                                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // // 计算底部偏移
                                temY = temY - (bmp[channelId].getHeight() - 1) / 2; // // 居中对齐
                                temY = bmp[channelId].getHeight() - resBmp[i][getDiscreet_l_down()].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // // 计算图标在bmp内的Y坐标
                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_down()].getWidth(), // // 绘制前置大箭头向下图标
                                        temY, paint); // // 绘制到计算位置
                                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 0); // // 绘制数值文本（底部位置）
                            } else { // // 正常范围内
                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l()].getWidth(), // // 绘制前置大箭头图标
                                        (bmp[currYIndex].getHeight() - resBmp[currYIndex][getDiscreet_l()].getHeight()) / 2, paint); // // 居中对齐绘制
                                drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35); // // 绘制数值文本
                            }
                        } else { // // 非当前索引通道
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); // // 设置清空模式
                            mCanvas[i].drawPaint(paint); // // 清空该通道Canvas
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC)); // // 恢复正常绘制模式

                            if (this.currYPos[i] < 0) { // // 超出上边界
                                int temY = resBmp[1][0].getHeight(); // // 获取顶部偏移
                                mCanvas[i].drawBitmap(resBmp[i][discreet_l_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_up].getWidth(), // // 绘制大箭头向上图标（非前置）
                                        ((bmp[i].getHeight() - 1) / 2 - temY), paint); // // 居中对齐绘制
                            } else if (ScopeBase.changeAccuracy(this.currYPos[i] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // // 超出下边界
                                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // // 计算底部偏移
                                temY = temY - (bmp[channelId].getHeight() - 1) / 2; // // 居中对齐
                                temY = bmp[channelId].getHeight() - resBmp[i][discreet_l_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // // 计算图标Y坐标
                                mCanvas[i].drawBitmap(resBmp[i][discreet_l_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_down].getWidth(), // // 绘制大箭头向下图标（非前置）
                                        temY, paint); // // 绘制到计算位置
                            } else { // // 正常范围内
                                mCanvas[i].drawBitmap(resBmp[i][discreet_l], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l].getWidth(), // // 绘制大箭头图标（非前置）
                                        (bmp[i].getHeight() - resBmp[i][discreet_l].getHeight()) / 2, paint); // // 居中对齐绘制

                            }
                        }
                    },
                    (i) -> TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None || TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_NoDraw // // 过滤掉None和NoDraw状态的通道
            );
        } else { // // 不显示横线
//            for (int i = 1; i < TChan.MaxLogicChan + 1; i++) {
//                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None) continue;
//                if (TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_NoDraw) continue;
//                if (i == currYIndex && !curYNull) {
//                    if (this.currYPos[currYIndex] < 0) {
//                        int temY = resBmp[1][0].getHeight();
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_up()].getWidth(),
//                                ((bmp[i].getHeight() - 1) / 2 - temY), paint);
//
//                    } else if (this.currYPos[currYIndex] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
//                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                        temY = bmp[channelId].getHeight() - resBmp[i][discreet_l_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_down()].getWidth(),
//                                temY, paint);
//
//                    } else {
//                        mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l()].getWidth(),
//                                (bmp[currYIndex].getHeight() - resBmp[currYIndex][getDiscreet_l()].getHeight()) / 2, paint);
//
//                    }
//                } else {
//                    if (this.currYPos[i] < 0) {
//                        int temY = resBmp[1][0].getHeight();
//                        mCanvas[i].drawBitmap(resBmp[i][discreet_l_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_up].getWidth(),
//                                ((bmp[i].getHeight() - 1) / 2 - temY), paint);
//                    } else if (this.currYPos[i] > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {
//                        int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight();
//                        temY = temY - (bmp[channelId].getHeight() - 1) / 2;
//                        temY = bmp[channelId].getHeight() - resBmp[i][discreet_l_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()));
//                        mCanvas[i].drawBitmap(resBmp[i][discreet_l_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_down].getWidth(),
//                                temY, paint);
//                    } else {
//                        mCanvas[i].drawBitmap(resBmp[i][discreet_l], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l].getWidth(),
//                                (bmp[i].getHeight() - resBmp[i][discreet_l].getHeight()) / 2, paint);
//
//                    }
//                }
//            }
            TChan.foreachChan((i) -> { // // 遍历所有有效通道
                        if (i == currYIndex && !curYNull) { // // 当前索引通道且非空心状态
                            if (this.currYPos[currYIndex] < 0) { // // 超出上边界
                                int temY = resBmp[1][0].getHeight(); // // 获取顶部偏移
                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_up()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_up()].getWidth(), // // 绘制前置大箭头向上图标
                                        ((bmp[i].getHeight() - 1) / 2 - temY), paint); // // 居中对齐绘制

                            } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // // 超出下边界
                                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // // 计算底部偏移
                                temY = temY - (bmp[channelId].getHeight() - 1) / 2; // // 居中对齐
                                temY = bmp[channelId].getHeight() - resBmp[i][discreet_l_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // // 计算图标Y坐标
                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l_down()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l_down()].getWidth(), // // 绘制前置大箭头向下图标
                                        temY, paint); // // 绘制到计算位置

                            } else { // // 正常范围内
                                mCanvas[currYIndex].drawBitmap(resBmp[currYIndex][getDiscreet_l()], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[currYIndex][getDiscreet_l()].getWidth(), // // 绘制前置大箭头图标
                                        (bmp[currYIndex].getHeight() - resBmp[currYIndex][getDiscreet_l()].getHeight()) / 2, paint); // // 居中对齐绘制
                            }
                        } else { // // 非当前索引通道
                            if (this.currYPos[i] < 0) { // // 超出上边界
                                int temY = resBmp[1][0].getHeight(); // // 获取顶部偏移
                                mCanvas[i].drawBitmap(resBmp[i][discreet_l_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_up].getWidth(), // // 绘制大箭头向上图标（非前置）
                                        ((bmp[i].getHeight() - 1) / 2 - temY), paint); // // 居中对齐绘制
                            } else if (ScopeBase.changeAccuracy(this.currYPos[i] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // // 超出下边界
                                int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // // 计算底部偏移
                                temY = temY - (bmp[channelId].getHeight() - 1) / 2; // // 居中对齐
                                temY = bmp[channelId].getHeight() - resBmp[i][discreet_l_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // // 计算图标Y坐标
                                mCanvas[i].drawBitmap(resBmp[i][discreet_l_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l_down].getWidth(), // // 绘制大箭头向下图标（非前置）
                                        temY, paint); // // 绘制到计算位置
                            } else { // // 正常范围内
                                mCanvas[i].drawBitmap(resBmp[i][discreet_l], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[i][discreet_l].getWidth(), // // 绘制大箭头图标（非前置）
                                        (bmp[i].getHeight() - resBmp[i][discreet_l].getHeight()) / 2, paint); // // 居中对齐绘制

                            }
                        }
                    },
                    (i) -> TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_Logic_None || TriggerVoltageLine_logic_state[i] == TriggerVoltageLine_NoDraw // // 过滤掉None和NoDraw状态的通道
            );

        }
    }

    /**
     * 双箭头模式下的离屏绘制
     * 同时绘制高电平和低电平的图标，处理越界和前置/非前置样式
     */
    private void drawTwo() { // // 双箭头模式离屏绘制
        if (this.visibleLine) { // // 如果需要显示横线
            paint.setColor(TChan.getChannelColor(context, channelId)); // // 设置横线颜色为通道颜色
            mCanvasLine.drawLine(0, 0, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), 0, paint); // // 绘制横线

            //绘制当前显示电平的图标
            int front_up, front_down, front; // // 前置图标的向上/向下/正常状态索引
            if (!curYNull) { // // 非空心状态
                if (currYIndex == VoltageLine_High) { // // 当前为高电平
                    front_up = getDiscreet_normal_up(); // // 高电平前置向上图标
                    front_down = getDiscreet_normal_down(); // // 高电平前置向下图标
                    front = getDiscreet_normal(); // // 高电平前置正常图标
                } else { // // 当前为低电平
                    front_up = getDiscreet_l_up(); // // 低电平前置向上图标
                    front_down = getDiscreet_l_down(); // // 低电平前置向下图标
                    front = getDiscreet_l(); // // 低电平前置正常图标
                }
            } else { // // 空心状态
                if (currYIndex == VoltageLine_High) { // // 当前为高电平
                    front_up = discreet_normal_up; // // 高电平非前置向上图标
                    front_down = discreet_normal_down; // // 高电平非前置向下图标
                    front = discreet_normal; // // 高电平非前置正常图标
                } else { // // 当前为低电平
                    front_up = discreet_l_up; // // 低电平非前置向上图标
                    front_down = discreet_l_down; // // 低电平非前置向下图标
                    front = discreet_l; // // 低电平非前置正常图标
                }
            }

            //绘制
            if (TriggerVoltageLine_logic_state[currYIndex] != TriggerVoltageLine_NoDraw) { // // 当前索引通道需要绘制

                if (this.currYPos[currYIndex] < 0) { // // 超出上边界
                    int temY = resBmp[1][0].getHeight(); // // 获取顶部偏移
                    mCanvas[currYIndex].drawBitmap(resBmp[channelId][front_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][front_up].getWidth(), // // 绘制前置向上图标
                            ((bmp[channelId].getHeight() - 1) / 2 - temY), paint); // // 居中对齐绘制
                    drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35); // // 绘制数值文本
                } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // // 超出下边界
                    int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // // 计算底部偏移
                    temY = temY - (bmp[channelId].getHeight() - 1) / 2; // // 居中对齐
                    temY = bmp[channelId].getHeight() - resBmp[channelId][front_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // // 计算图标Y坐标
                    mCanvas[currYIndex].drawBitmap(resBmp[channelId][front_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][front_down].getWidth(), // // 绘制前置向下图标
                            temY, paint); // // 绘制到计算位置
                    drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 0); // // 绘制数值文本（底部位置）
                } else { // // 正常范围内
                    mCanvas[currYIndex].drawBitmap(resBmp[channelId][front], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][front].getWidth(), // // 绘制前置正常图标
                            (bmp[currYIndex].getHeight() - resBmp[channelId][front].getHeight()) / 2, paint); // // 居中对齐绘制
                    drawText((int) Math.round(ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff())), 35); // // 绘制数值文本
                }
            }
            //绘制另一个非当前显示电平的图标
            int temIndex; // // 另一个电平的索引号
            int back_up, back_down, back; // // 非前置图标的向上/向下/正常状态索引
            if (currYIndex == VoltageLine_High) { // // 当前是高电平
                temIndex = VoltageLine_Low; // // 另一个是低电平
                back_up = discreet_l_up; // // 低电平非前置向上
                back_down = discreet_l_down; // // 低电平非前置向下
                back = discreet_l; // // 低电平非前置正常
            } else { // // 当前是低电平
                temIndex = VoltageLine_High; // // 另一个是高电平
                back_up = discreet_normal_up; // // 高电平非前置向上
                back_down = discreet_normal_down; // // 高电平非前置向下
                back = discreet_normal; // // 高电平非前置正常

            }

            //需要绘制
            if (TriggerVoltageLine_logic_state[temIndex] != TriggerVoltageLine_NoDraw) { // // 另一个电平需要绘制

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); // // 设置清空模式
                mCanvas[temIndex].drawPaint(paint); // // 清空该通道Canvas
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC)); // // 恢复正常绘制模式

                if (this.currYPos[temIndex] < 0) { // // 超出上边界
                    int temY = resBmp[1][0].getHeight(); // // 获取顶部偏移
                    mCanvas[temIndex].drawBitmap(resBmp[channelId][back_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][back_up].getWidth(), // // 绘制非前置向上图标
                            ((bmp[channelId].getHeight() - 1) / 2 - temY), paint); // // 居中对齐绘制
                    //drawText(35);
                } else if (ScopeBase.changeAccuracy(this.currYPos[temIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // // 超出下边界
                    int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // // 计算底部偏移
                    temY = temY - (bmp[channelId].getHeight() - 1) / 2; // // 居中对齐
                    temY = bmp[channelId].getHeight() - resBmp[channelId][front_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // // 计算图标Y坐标
                    mCanvas[temIndex].drawBitmap(resBmp[channelId][back_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][back_down].getWidth(), // // 绘制非前置向下图标
                            temY, paint); // // 绘制到计算位置
                    //drawText(0);
                } else { // // 正常范围内
                    mCanvas[temIndex].drawBitmap(resBmp[channelId][back], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][back].getWidth(), // // 绘制非前置正常图标
                            (bmp[temIndex].getHeight() - resBmp[channelId][back].getHeight()) / 2, paint); // // 居中对齐绘制
                    //drawText(35);
                }
            }

        } else { //
            //不显示线条

            //绘制当前显示电平的图标
            int front_up, front_down, front; // // 前置图标的向上/向下/正常状态索引
            if (!curYNull) { // // 非空心状态
                if (currYIndex == VoltageLine_High) { // // 当前为高电平
                    front_up = getDiscreet_normal_up(); // // 高电平前置向上图标
                    front_down = getDiscreet_normal_down(); // // 高电平前置向下图标
                    front = getDiscreet_normal(); // // 高电平前置正常图标
                } else { // // 当前为低电平
                    front_up = getDiscreet_l_up(); // // 低电平前置向上图标
                    front_down = getDiscreet_l_down(); // // 低电平前置向下图标
                    front = getDiscreet_l(); // // 低电平前置正常图标
                }
            } else { // // 空心状态
                if (currYIndex == VoltageLine_High) { // // 当前为高电平
                    front_up = discreet_normal_up; // // 高电平非前置向上
                    front_down = discreet_normal_down; // // 高电平非前置向下
                    front = discreet_normal; // // 高电平非前置正常
                } else { // // 当前为低电平
                    front_up = discreet_l_up; // // 低电平非前置向上
                    front_down = discreet_l_down; // // 低电平非前置向下
                    front = discreet_l; // // 低电平非前置正常
                }
            }
            //需要绘制
            if (TriggerVoltageLine_logic_state[currYIndex] != TriggerVoltageLine_NoDraw) { // // 当前索引通道需要绘制

                paint.setColor(TChan.getChannelColor(context, channelId)); // // 设置图标颜色
                if (this.currYPos[currYIndex] < 0) { // // 超出上边界
                    int temY = resBmp[1][0].getHeight(); // // 获取顶部偏移
                    mCanvas[currYIndex].drawBitmap(resBmp[channelId][front_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][front_up].getWidth(), // // 绘制前置向上图标
                            ((bmp[channelId].getHeight() - 1) / 2 - temY), paint); // // 居中对齐绘制
                } else if (ScopeBase.changeAccuracy(this.currYPos[currYIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // // 超出下边界
                    int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // // 计算底部偏移
                    temY = temY - (bmp[channelId].getHeight() - 1) / 2; // // 居中对齐
                    temY = bmp[channelId].getHeight() - resBmp[channelId][front_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // // 计算图标Y坐标
                    mCanvas[currYIndex].drawBitmap(resBmp[channelId][front_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][front_down].getWidth(), // // 绘制前置向下图标
                            temY, paint); // // 绘制到计算位置
                } else { // // 正常范围内
                    mCanvas[currYIndex].drawBitmap(resBmp[channelId][front], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][front].getWidth(), // // 绘制前置正常图标
                            (bmp[currYIndex].getHeight() - resBmp[channelId][front].getHeight()) / 2, paint); // // 居中对齐绘制
                }
            }
            //绘制另一个非当前显示电平的图标
            int temIndex; // // 另一个电平的索引号
            int back_up, back_down, back; // // 非前置图标的向上/向下/正常状态索引
            if (currYIndex == VoltageLine_High) { // // 当前是高电平
                temIndex = VoltageLine_Low; // // 另一个是低电平
                back_up = discreet_l_up; // // 低电平非前置向上
                back_down = discreet_l_down; // // 低电平非前置向下
                back = discreet_l; // // 低电平非前置正常
            } else { // // 当前是低电平
                temIndex = VoltageLine_High; // // 另一个是高电平
                back_up = discreet_normal_up; // // 高电平非前置向上
                back_down = discreet_normal_down; // // 高电平非前置向下
                back = discreet_normal; // // 高电平非前置正常
            }
            //需要绘制
            if (TriggerVoltageLine_logic_state[temIndex] != TriggerVoltageLine_NoDraw) { // // 另一个电平需要绘制

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); // // 设置清空模式
                mCanvas[temIndex].drawPaint(paint); // // 清空该通道Canvas
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC)); // // 恢复正常绘制模式
                if (this.currYPos[temIndex] < 0) { // // 超出上边界
                    int temY = resBmp[1][0].getHeight(); // // 获取顶部偏移
                    mCanvas[temIndex].drawBitmap(resBmp[channelId][back_up], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][back_up].getWidth(), // // 绘制非前置向上图标
                            ((bmp[channelId].getHeight() - 1) / 2 - temY), paint); // // 居中对齐绘制

                } else if (ScopeBase.changeAccuracy(this.currYPos[temIndex] * ScopeBase.getToUICoff()) > GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) { // // 超出下边界
                    int temY = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[1][2].getHeight(); // // 计算底部偏移
                    temY = temY - (bmp[channelId].getHeight() - 1) / 2; // // 居中对齐
                    temY = bmp[channelId].getHeight() - resBmp[channelId][front_down].getHeight() - (temY + bmp[channelId].getHeight() - GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())); // // 计算图标Y坐标
                    mCanvas[temIndex].drawBitmap(resBmp[channelId][back_down], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][back_down].getWidth(), // // 绘制非前置向下图标
                            temY, paint); // // 绘制到计算位置

                } else { // // 正常范围内
                    mCanvas[temIndex].drawBitmap(resBmp[channelId][back], GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[channelId][back].getWidth(), // // 绘制非前置正常图标
                            (bmp[temIndex].getHeight() - resBmp[channelId][back].getHeight()) / 2, paint); // // 居中对齐绘制

                }
            }

        }

    }

    /***
     * 绘制文字
     * @param  currY 当前电平线的Y坐标
     * @param drawTextY 绘制的起始Y坐标
     */
    private void drawText(int currY, int drawTextY) { // // 绘制数值文本
        if (currY <= 50) { // // 靠近顶部
            drawTextY = 35; // // 文本绘制在图标下方
        } else if (currY >= GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - 50) { // // 靠近底部
            drawTextY = 0; // // 文本绘制在图标上方
        } else { // // 正常位置
            drawTextY = 35; // // 文本绘制在图标下方
        }
        int x; // // 文本X坐标
        Rect rect; // // 文本边界矩形
        rect = getTextHeight(text); // // 获取文本的边界矩形
        x = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - rect.width() - 5; // // 计算文本X坐标（右对齐，留5px边距）
        drawTextY = drawTextY + rect.height(); // // 计算文本Y坐标（加上文本高度）
        mCanvas[currYIndex].drawText(text, x, drawTextY, paint); // // 在离屏Canvas上绘制文本

    }

    /**
     * 获取文本的边界矩形（宽高）
     * @param text 要测量的文本
     * @return 文本的边界矩形
     */
    private Rect getTextHeight(String text) { // // 测量文本边界矩形
        Rect rect = new Rect(); // // 创建矩形对象
        paint.getTextBounds(text, 0, text.length(), rect); // // 测量文本边界
        int w = rect.width(); // // 获取文本宽度（未使用）
        int h = rect.height(); // // 获取文本高度（未使用）
        return rect; // // 返回边界矩形
    }
    //endregion

}
