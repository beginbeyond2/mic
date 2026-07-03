package com.micsig.tbook.tbookscope.wavezone.measure;  // 光标测量模块包，包含XY模式光标测量实现

import android.graphics.Color;  // 颜色工具类，用于构造RGB颜色值
import android.graphics.PorterDuff;  // PorterDuff混合模式枚举，用于图形合成
import android.graphics.PorterDuffXfermode;  // 图层混合模式封装类，控制绘制时像素合成方式


/**
 * Created by liwb on 2017/11/6.
 */

/*
 * +=============================================================================================================+
 * |                                          CursorMeasure_XY 类                                                 |
 * +=============================================================================================================+
 * | 模块定位 : 示波器光标测量子系统 -> XY模式光标测量绘制器                                                      |
 * | 核心职责 : 在XY工作模式下绘制光标测量信息面板，显示X/Y坐标值及差值                                            |
 * | 架构设计 : 继承自CurSorMeasureBase，重写draw()方法，采用简化绘制策略                                          |
 * |          （仅绘制行列光标数据，不绘制ΔX频率和面积S）                                                          |
 * | 数据流向 : 外部通过setParam()设置测量参数 -> 基类存储参数 -> draw()触发重绘                                    |
 * | 依赖关系 : 继承 CurSorMeasureBase（光标测量基类）                                                            |
 * |          依赖 android.graphics（Canvas/Bitmap/Paint绘制体系）                                                |
 * | 使用场景 : 示波器切换至XY模式时，用于在波形区域叠加显示光标测量的坐标及差值信息                                  |
 * +=============================================================================================================+
 */
public class CursorMeasure_XY extends CurSorMeasureBase {  // 继承光标测量基类，实现XY模式的绘制逻辑

    private PorterDuffXfermode clearMode=new PorterDuffXfermode(PorterDuff.Mode.CLEAR);  // 清除混合模式，用于擦除位图内容
    private PorterDuffXfermode srcMode=new PorterDuffXfermode(PorterDuff.Mode.SRC);  // 源覆盖混合模式，用于直接绘制新内容

    /**
     * 绘制XY模式下的光标测量信息面板。
     * <p>
     * 重写基类的draw()方法，采用简化绘制策略：
     * 使用CLEAR模式擦除整个位图，再切换到SRC模式绘制内容。
     * 根据行列光标的可见性组合，分别绘制对应的测量数据。
     * 与基类draw()相比，XY模式不绘制1/ΔX频率和面积S。
     * </p>
     */
    @Override
    protected void draw() {
        //super.draw();  // 不调用基类draw()，XY模式使用自身简化绘制逻辑
        synchronized (bmp) {  // 同步锁保护位图对象，防止并发绘制导致画面异常
            p.setXfermode(clearMode);  // 设置画笔为清除模式，后续绘制将擦除已有像素
            mCanvas.drawPaint(p);  // 用清除模式画笔填充整个画布，实现擦除效果
            p.setXfermode(srcMode);  // 切换画笔为源覆盖模式，后续绘制将直接写入新像素
            p.setColor(Color.rgb(200,200,200));  // 设置画笔颜色为浅灰色(RGB 200,200,200)
            if (rowCursorVisible && colCursorVisible) {  // 行光标和列光标均可见时
                int y = drawRow();  // 绘制行光标测量数据(Y1/Y2/ΔY)，返回当前Y坐标位置
                y = drawCol(y);  // 从上一行末尾继续绘制列光标测量数据(X1/X2/ΔX)
//                y=drawDeltaX(y);  // XY模式下不绘制1/ΔX频率信息
//                drawS(y);  // XY模式下不绘制面积S

            } else if (rowCursorVisible) {  // 仅行光标可见时
                drawRow();  // 只绘制行光标测量数据(Y1/Y2/ΔY)
            } else if (colCursorVisible) {  // 仅列光标可见时
                int y=drawCol(0);  // 从Y=0起始绘制列光标测量数据(X1/X2/ΔX)
//                drawDeltaX(y);  // XY模式下不绘制1/ΔX频率信息
            }
            isChanageBitmap = true;  // 标记位图内容已变更，通知纹理刷新
            onRefresh();  // 通知GL画布刷新纹理内容，使绘制结果可见
        }
    }


}
