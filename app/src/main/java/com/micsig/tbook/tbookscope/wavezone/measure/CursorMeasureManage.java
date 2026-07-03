package com.micsig.tbook.tbookscope.wavezone.measure;

import com.chillingvan.canvasgl.ICanvasGL;                            // OpenGL画布接口，用于GPU加速渲染
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;                // 工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;           // 工作模式管理器，获取当前工作模式

/**
 * Created by liwb on 2017/11/6.
 *
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                         CursorMeasureManage                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：光标测量管理器，根据工作模式将光标测量请求分发到对应子模块            ║
 * ║ 核心职责：                                                                  ║
 * ║   1. 持有CursorMeasure_XY和CursorMeasure_YT两个子模块实例                   ║
 * ║   2. 根据当前工作模式(YT/YTZOOM/XY)分发绘制和参数设置请求                    ║
 * ║   3. 统一管理行光标/列光标的可见性和通道颜色                                  ║
 * ║ 架构设计：                                                                  ║
 * ║   - 实现IWorkMode接口，支持工作模式切换                                      ║
 * ║   - 采用策略模式：根据WorkModeManage中的当前模式选择具体执行者               ║
 * ║   - 作为CursorMeasure_XY/YT的外观(Facade)，对外提供统一接口                 ║
 * ║ 数据流向：                                                                  ║
 * ║   外部调用 → 查询当前工作模式 → 分发到XY或YT子模块 → 子模块执行具体逻辑      ║
 * ║ 依赖关系：                                                                  ║
 * ║   - CursorMeasure_XY：XY模式下的光标测量实现                                ║
 * ║   - CursorMeasure_YT：YT模式下的光标测量实现                                ║
 * ║   - WorkModeManage：获取当前工作模式                                        ║
 * ║ 使用场景：                                                                  ║
 * ║   MeasureManage通过此类间接控制光标测量，实现工作模式隔离                     ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

public class CursorMeasureManage implements IWorkMode {
    private CursorMeasure_XY cursorMeasure_xy;                         // XY模式下的光标测量实例
    private CursorMeasure_YT cursorMeasure_yt;                         // YT模式下的光标测量实例

    /**
     * 构造函数：创建XY和YT两种模式的光标测量实例
     */
    public CursorMeasureManage() {
        cursorMeasure_xy = new CursorMeasure_XY();                     // 创建XY模式光标测量
        cursorMeasure_yt = new CursorMeasure_YT();                     // 创建YT模式光标测量
    }

    /**
     * 切换工作模式，将模式变更通知到对应的子模块
     * @param workMode 目标工作模式（YT/YTZOOM/XY）
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        switch (workMode) {                                             // 根据工作模式分发
            case WorkMode_YT:                                           // YT模式
            case WorkMode_YTZOOM:                                       // YT缩放模式
                cursorMeasure_yt.switchWorkMode(workMode);              // 通知YT子模块切换模式
                break;
            case WorkMode_XY:                                           // XY模式
                cursorMeasure_xy.switchWorkMode(workMode);              // 通知XY子模块切换模式
                break;
        }
    }

    /**
     * 根据当前工作模式在GL画布上绘制光标测量浮窗
     * @param canvas GL画布
     */
    public void draw(ICanvasGL canvas) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {          // 查询当前工作模式
            case WorkMode_YT:                                           // YT模式
            case WorkMode_YTZOOM:                                       // YT缩放模式
                cursorMeasure_yt.draw(canvas);                          // 使用YT子模块绘制
                break;
            case WorkMode_XY:                                           // XY模式
                cursorMeasure_xy.draw(canvas);                          // 使用XY子模块绘制
                break;
        }
    }

    /**
     * 设置光标测量的通道颜色（同时更新XY和YT子模块）
     * @param ChNo 通道号
     */
    public void setCursorChannelColor(int ChNo) {
        cursorMeasure_yt.setChannelId(ChNo);                            // 设置YT子模块的通道ID
        cursorMeasure_xy.setChannelId(ChNo);                            // 设置XY子模块的通道ID
    }

    /**
     * 根据当前工作模式设置行光标的可见性
     * @param rowVisible true表示行光标可见
     */
    public void setRowVisible(boolean rowVisible) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {          // 查询当前工作模式
            case WorkMode_YT:                                           // YT模式
            case WorkMode_YTZOOM:                                       // YT缩放模式
                cursorMeasure_yt.setRowCursorVisible(rowVisible);       // 设置YT子模块行光标可见性
                break;
            case WorkMode_XY:                                           // XY模式
                cursorMeasure_xy.setRowCursorVisible(rowVisible);       // 设置XY子模块行光标可见性
                break;
        }
    }

    /**
     * 根据当前工作模式设置列光标的可见性
     * @param colVisible true表示列光标可见
     */
    public void setColVisible(boolean colVisible) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {          // 查询当前工作模式
            case WorkMode_YT:                                           // YT模式
            case WorkMode_YTZOOM:                                       // YT缩放模式
                cursorMeasure_yt.setColCursorVisible(colVisible);       // 设置YT子模块列光标可见性
                break;
            case WorkMode_XY:                                           // XY模式
                cursorMeasure_xy.setColCursorVisible(colVisible);       // 设置XY子模块列光标可见性
                break;
        }
    }

    /**
     * 按指定工作模式设置行光标可见性（覆盖当前模式判断）
     * YT/YTZOOM模式下行光标强制不可见，XY模式下按参数设置
     * @param workMode 指定的工作模式
     * @param rowVisible true表示行光标可见
     */
    public void setRowVisible(int workMode, boolean rowVisible) {
        switch (workMode) {                                             // 根据指定的工作模式分发
            case WorkMode_YT:                                           // YT模式
            case WorkMode_YTZOOM:                                       // YT缩放模式
                cursorMeasure_yt.setRowCursorVisible(false);            // YT模式下强制关闭行光标
                break;
            case WorkMode_XY:                                           // XY模式
                cursorMeasure_xy.setRowCursorVisible(rowVisible);       // XY模式下按参数设置行光标
                break;
        }
    }

    /**
     * 按指定工作模式设置列光标可见性（覆盖当前模式判断）
     * YT/YTZOOM模式下列光标强制不可见，XY模式下按参数设置
     * @param workMode 指定的工作模式
     * @param colVisible true表示列光标可见
     */
    public void setColVisible(int workMode, boolean colVisible) {
        switch (workMode) {                                             // 根据指定的工作模式分发
            case WorkMode_YT:                                           // YT模式
            case WorkMode_YTZOOM:                                       // YT缩放模式
                cursorMeasure_yt.setColCursorVisible(false);            // YT模式下强制关闭列光标
                break;
            case WorkMode_XY:                                           // XY模式
                cursorMeasure_xy.setColCursorVisible(colVisible);       // XY模式下按参数设置列光标
                break;
        }
    }

    /**
     * 根据当前工作模式设置光标测量参数，并触发重绘
     * @param row1    行光标1的Y值
     * @param row2    行光标2的Y值
     * @param detaRow 行光标差值ΔY
     * @param col1    列光标1的X值
     * @param col2    列光标2的X值
     * @param detaCol 列光标差值ΔX
     * @param detaTCol 列光标差值倒数1/ΔX
     * @param S       面积值
     */
    public void setParam(String row1, String row2, String detaRow, String col1, String col2, String detaCol,
                         String detaTCol, String S) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {          // 查询当前工作模式
            case WorkMode_YT:                                           // YT模式
            case WorkMode_YTZOOM:                                       // YT缩放模式
                cursorMeasure_yt.setParam(row1, row2, detaRow, col1, col2, detaCol, detaTCol, S); // 设置YT子模块参数
                break;
            case WorkMode_XY:                                           // XY模式
                cursorMeasure_xy.setParam(row1, row2, detaRow, col1, col2, detaCol, detaTCol, S); // 设置XY子模块参数
                break;
        }
    }
}
