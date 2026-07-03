package com.micsig.tbook.tbookscope.wavezone.display;  // 光标显示实现类所在包

import android.content.Context;  // Android上下文
import android.graphics.Bitmap;  // 位图，用于绘制光标纹理
import android.graphics.Canvas;  // 画布，用于绘制光标图形
import android.graphics.Color;  // 颜色常量
import android.graphics.DashPathEffect;  // 虚线效果
import android.graphics.Paint;  // 画笔
import android.graphics.Path;  // 路径，用于绘制箭头标识
import android.graphics.PathEffect;  // 路径效果
import android.graphics.PorterDuff;  // 混合模式
import android.graphics.PorterDuffXfermode;  // 混合模式封装
import android.graphics.Rect;  // 矩形区域
import android.util.Log;  // 日志

import com.chillingvan.canvasgl.ICanvasGL;  // OpenGL画布接口
import com.micsig.base.Logger;  // 日志工具
import com.micsig.tbook.scope.ScopeBase;  // 示波器基础参数
import com.micsig.tbook.tbookscope.GlobalVar;  // 全局变量
import com.micsig.tbook.tbookscope.middleware.command.Command;  // 命令中间件
import com.micsig.tbook.tbookscope.tools.Tools;  // 通用工具类
import com.micsig.tbook.tbookscope.util.App;  // 应用上下文工具
import com.micsig.tbook.tbookscope.util.CacheUtil;  // 缓存工具
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;  // 工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;  // 工作模式管理
import com.micsig.tbook.ui.util.TBookUtil;  // UI工具
import com.micsig.tbook.ui.wavezone.IWave;  // 波形线接口
import com.micsig.tbook.ui.wavezone.TChan;  // 通道常量定义

/*
 * +=============================================================================+
 * |                   Cursor_impIWave — 光标线IWave实现类                        |
 * +=============================================================================+
 * | 模块定位：tbookscope.wavezone.display 显示层                                 |
 * | 核心职责：实现单条光标线（行光标/列光标）的绘制、移动、选中、颜色设置等       |
 * | 架构设计：实现IWave接口（波形线行为）和IWorkMode接口（工作模式切换），         |
 * |          由CursorManage统一管理多个光标实例                                    |
 * | 数据流向：CursorManage → Cursor_impIWave.setX/setY → 绘制位图 → OpenGL渲染   |
 * | 依赖关系：IWave, IWorkMode, ICanvasGL, CursorManage, Command, CacheUtil      |
 * | 使用场景：示波器光标线的创建、移动、选中高亮、虚线/实线切换、标识绘制          |
 * +=============================================================================+
 */

/**
 * Created by liwb on 2017/5/5.
 */

public class Cursor_impIWave implements IWave, IWorkMode {  // 实现IWave波形线接口和IWorkMode工作模式接口
    private static final String TAG = "Cursor_impIWave";  // 日志标签

    //region 类属性
    private Context context= App.get().getApplicationContext();  // 应用上下文
    private int cursorType;  // 光标类型（行光标1/2或列光标1/2）
    private int cursorWorkMode;  // 光标当前工作模式（YT/XY/YTZOOM）

    /** 图标到边界的距离 */
    private  int distance_l=100;  // 左侧图标距边界距离
    //右边图标距离=bmp.getwidth()+distance_l;
    private  int distance_r=distance_l;  // 右侧图标距边界距离（默认等于左侧距离+位图宽度）

    /**
     * 获取光标类型
     *
     * @return 光标类型值，如TChan.Cursor_row_1、TChan.Cursor_col_2等
     */
    public int getCursorType() {  // 获取光标类型
        return cursorType;  // 返回光标类型
    }

    //endregion

    //字体显示的高度
    private final int WordHeight = 21;  // 标识字体的显示高度（像素）
    /**
     * 标识宽度
     */
    private final int IdentifyWidth = WordHeight * 2;  // 标识箭头的宽度
    /**
     * 中心线位置
     */
    private final int centerLine = WordHeight / 2;  // 标识中心线的偏移量

    //region Iwave 接口属性
    private int nameID;  // 光标名称ID（与TChan常量对应）
    private long x;  // 列光标的水平像素位置
    private double y = 350;//1000对应的值  // 行光标的垂直像素位置（FPGA坐标系）
    private int color;  // 光标颜色
    private boolean selected;  // 是否被选中
    private OnSelectChangeEvent onSelectChangeEvent = null;  // 选中状态变化事件回调
    private OnMovingWaveEvent onMovingWaveEvent = null;  // 移动事件回调
    private boolean visible;  // 是否可见
    private Rect CursorRect;  // 光标可移动范围矩形
    private int currCh = 0;  // 当前关联的通道索引
    //endregion

    private Bitmap bmp,oldBmp;  // 当前位图和旧位图（用于纹理更新）
    private Canvas mCanvas;  // 绘制用画布
    private Path rowPathId;  // 行光标箭头路径
    private Path colPathId;  // 列光标箭头路径
    private boolean isChanageBitmap = true;  // 位图内容是否已改变（需要刷新纹理）

    private ICanvasGL canvasGL;  // OpenGL画布引用
    /**
     * 通知OpenGL画布刷新纹理
     */
    public void onRefresh(){  // 刷新纹理
        if(canvasGL != null){  // 画布非空时
            canvasGL.onRefreshTexture();  // 调用OpenGL纹理刷新
        }
    }
    private Bitmap[][] resBmp;  // 资源位图二维数组 [光标位置][选中/未选中]
    private boolean isShowResBmp;  // 是否显示资源位图标识

    /**
     * 构造光标线实例
     *
     * @param resBmp       资源位图二维数组，[光标位置索引][选中状态索引]
     * @param iCursorType  光标类型（TChan.Cursor_row_1/2, TChan.Cursor_col_1/2）
     * @param workMode     工作模式（YT/XY/YTZOOM）
     * @param isShowResBmp 是否显示资源位图标识
     */
    public Cursor_impIWave(Bitmap[][] resBmp, int iCursorType, int workMode,boolean isShowResBmp) {  // 构造函数
        distance_r=distance_l+resBmp[CursorManage.Row1][0].getWidth();  // 计算右侧图标距边界距离
        this.isShowResBmp=isShowResBmp;  // 保存是否显示资源位图标志

        this.resBmp = resBmp;  // 保存资源位图引用
        this.cursorType = iCursorType;  // 保存光标类型
        this.cursorWorkMode = workMode;  // 保存工作模式
        switch (iCursorType) {  // 根据光标类型创建不同尺寸的位图
            case TChan.Cursor_row_1:  // 行光标1
            case TChan.Cursor_row_2:  // 行光标2
                bmp = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(workMode), WordHeight, Bitmap.Config.ARGB_8888);  // 创建宽度=波形区宽度、高度=字体高度的位图
                break;  // 跳出
            case TChan.Cursor_col_1:  // 列光标1
            case TChan.Cursor_col_2:  // 列光标2
                bmp = Bitmap.createBitmap(WordHeight, GlobalVar.get().getWaveZoneHeight_Pix(workMode), Bitmap.Config.ARGB_8888);  // 创建宽度=字体高度、高度=波形区高度的位图
                break;  // 跳出
            default:  // 其他类型
                bmp = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(workMode), 1, Bitmap.Config.ARGB_8888);  // 创建1像素高的默认位图
                break;  // 跳出
        }

        mCanvas = new Canvas(bmp);  // 以位图创建画布

        CursorRect = new Rect();  // 创建光标范围矩形
        CursorRect.left = 0;  // 左边界为0
        CursorRect.top = 0;  // 上边界为0
        CursorRect.right = GlobalVar.get().getWaveZoneWidth_Pix(workMode);  // 右边界为波形区宽度
        CursorRect.bottom = GlobalVar.get().getWaveZoneHeight_Pix(workMode);  // 下边界为波形区高度

        rowPathId = new Path();  // 创建行光标箭头路径
        rowPathId.moveTo(0, centerLine);  // 移动到左中点
        rowPathId.lineTo(centerLine, 0);  // 画线到上中点
        rowPathId.lineTo(IdentifyWidth - centerLine - 1, 0);  // 画线到上右侧点
        rowPathId.lineTo(IdentifyWidth - 1, centerLine);  // 画线到右中点
        rowPathId.lineTo(IdentifyWidth - centerLine - 1, WordHeight - 1);  // 画线到下右侧点
        rowPathId.lineTo(centerLine, WordHeight - 1);  // 画线到下左侧点
        rowPathId.close();  // 关闭路径
        rowPathId.setFillType(Path.FillType.WINDING);  // 设置填充类型为缠绕

        colPathId = new Path();  // 创建列光标箭头路径
        colPathId.moveTo(centerLine, 0);  // 移动到上中点
        colPathId.lineTo(WordHeight - 1, centerLine);  // 画线到右中点
        colPathId.lineTo(WordHeight - 1, IdentifyWidth - centerLine - 1);  // 画线到右下点
        colPathId.lineTo(centerLine, IdentifyWidth - 1);  // 画线到中下点
        colPathId.lineTo(0, IdentifyWidth - centerLine - 1);  // 画线到左下点
        colPathId.lineTo(0, centerLine);  // 画线到左中点
        colPathId.close();  // 关闭路径
        colPathId.setFillType(Path.FillType.WINDING);  // 设置填充类型为缠绕

    }

    //region IWorkMode接口

    int preHeight = ScopeBase.getHeight();  // 保存前一次的高度
    /**
     * 切换工作模式时更新光标坐标和范围
     *
     * @param workMode 目标工作模式
     */
    @Override  // 覆写IWorkMode接口方法
    public void switchWorkMode(@WorkMode int workMode) {  // 切换工作模式

        this.cursorWorkMode = workMode;  // 更新当前工作模式
        if (TChan.isCursorRow2(this.nameID)) {  // 如果是行光标
//            Logger.i(TAG, "switchWorkMode() ==>"+" [1]"
//                    +" cursorId:0x"+Integer.toHexString(this.nameID)
//                    +" workMode:"+workMode+" y:"+this.y);
            switch (workMode) {  // 根据目标模式进行Y坐标转换
                case IWorkMode.WorkMode_YT:  // 切换到YT模式
                    this.y = this.getY() * GlobalVar.get().toYTCoef() * ScopeBase.getToFPGACoff();  // Y坐标按YT系数和FPGA系数转换
                    break;  // 跳出
                case IWorkMode.WorkMode_YTZOOM:  // 切换到YT缩放模式
                    this.y = this.getY() * GlobalVar.get().toZoomCoef() * ScopeBase.getToFPGACoff();  // Y坐标按缩放系数和FPGA系数转换
                    break;  // 跳出
            }


//            Logger.i(TAG, "switchWorkMode() ==>"+" [2]"
//                    +" cursorId:0x"+Integer.toHexString(this.nameID)
//                    +" workMode:"+workMode+" y:"+this.y);
        }
        double y = this.y;  // 获取当前Y坐标
        if (workMode == IWorkMode.WorkMode_YTZOOM) {  // 如果是YT缩放模式
            y = this.y * GlobalVar.get().toYTCoef();  // Y坐标再乘YT系数用于缓存
        }
        CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION + this.nameID, String.valueOf(y));  // 保存行光标位置到缓存
        CursorRect.right = GlobalVar.get().getWaveZoneWidth_Pix(workMode);  // 更新右边界
        CursorRect.bottom = GlobalVar.get().getWaveZoneHeight_Pix(workMode);  // 更新下边界
        draw();  // 重新绘制光标
    }

    //endregion

    //region
    private Paint p = new Paint();  // 画笔对象
    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);  // 清除混合模式
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);  // 源混合模式
    private PorterDuffXfermode dstMode = new PorterDuffXfermode(PorterDuff.Mode.DST);  // 目标混合模式
    private PorterDuffXfermode dstoutMode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);  // 目标出混合模式
    private PorterDuffXfermode srcOverMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);  // 源覆盖混合模式
    private PorterDuffXfermode srcIn = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);  // 源入混合模式
    private PorterDuffXfermode dstIn = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);  // 目标入混合模式

    /**
     * 内部绘制方法：绘制光标线（虚线/实线）和标识图标
     */
    private void draw() {  // 绘制光标线
        synchronized (bmp) {  // 同步锁定位图
            p.setXfermode(clearMode);  // 设置清除模式
            mCanvas.drawPaint(p);  // 清空画布
            p.setXfermode(srcMode);  // 设置源绘制模式
            p.setStrokeWidth(1);  // 设置线宽为1像素
            p.setColor(color);  // 设置画笔颜色
            p.setStyle(Paint.Style.STROKE);  // 设置描边样式
            if (!selected) {  // 未选中时绘制虚线
                PathEffect effects = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);  // 创建5像素间隔虚线效果
                p.setPathEffect(effects);  // 应用虚线效果
            } else {  // 选中时绘制实线
                p.setPathEffect(null);  // 清除路径效果（实线）
            }
            if (TChan.isCursorRow2(cursorType)) {  // 行光标：绘制水平线
                mCanvas.drawLine(0, centerLine, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), centerLine, p);  // 绘制水平光标线
            } else if (TChan.isCursorCol2(cursorType)) {  // 列光标：绘制垂直线
                mCanvas.drawLine(centerLine, 0, centerLine, GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()), p);  // 绘制垂直光标线
            }
//            drawId(this.selected,this.cursorType);
            drawId(cursorWorkMode, currCh, selected, cursorType);  // 绘制光标标识图标
            isChanageBitmap = true;  // 标记位图已改变
            onRefresh();  // 通知OpenGL刷新纹理
        }
    }

    /**
     * 绘制光标标识图标（使用资源位图方式）
     *
     * @param currWorkMode 当前工作模式
     * @param currCh       当前通道索引
     * @param selected     是否选中
     * @param currsorType  光标类型
     */
    private void drawId(int currWorkMode, int currCh, boolean selected, int currsorType) {  // 绘制资源位图标识
        int ch = 0;  // 通道索引
        if (currWorkMode == IWorkMode.WorkMode_XY) {  // XY模式下
            ch = 0;  // 通道固定为0
        } else {  // 非XY模式下
            ch = currCh;  // 使用当前通道
            if (TChan.isRef(currCh)) ch = TChan.R1;  // 参考通道映射为R1
//            else if(currCh >=IWave.S1) ch = currCh - 2;
        }
        int selectIndex = 0;  // 选中状态索引
        if (selected) {  // 选中状态
            selectIndex = CursorManage.Select;  // 使用选中状态索引
        } else {  // 未选中状态
            selectIndex = CursorManage.NoSelect;  // 使用未选中状态索引
        }

//        if (isShowResBmp==false) return;
        if (!CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_CURSOR_SETTING_MODE)) return;  // 如果光标设置模式未开启则返回

        p.setXfermode(srcMode);  // 设置源绘制模式
        switch (cursorType) {  // 根据光标类型绘制对应位置的标识
            case TChan.Cursor_row_1: {  // 行光标1：在左侧绘制标识
                mCanvas.save();  // 保存画布状态
                mCanvas.translate(distance_l, 0);  // 平移到左侧标识位置
                mCanvas.drawBitmap(resBmp[CursorManage.Row1][selectIndex], 0, (mCanvas.getHeight() - resBmp[CursorManage.Row1][selectIndex].getHeight()) / 2, p);  // 绘制行1标识位图（垂直居中）
                mCanvas.restore();  // 恢复画布状态
            }
            break;  // 跳出
            case TChan.Cursor_row_2: {  // 行光标2：在右侧绘制标识
                mCanvas.save();  // 保存画布状态
                mCanvas.translate(GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - distance_r, 0);  // 平移到右侧标识位置
                mCanvas.drawBitmap(resBmp[CursorManage.Row2][selectIndex], 0, (mCanvas.getHeight() - resBmp[CursorManage.Row2][selectIndex].getHeight()) / 2, p);  // 绘制行2标识位图（垂直居中）
                mCanvas.restore();  // 恢复画布状态
            }
            break;  // 跳出
            case TChan.Cursor_col_1: {  // 列光标1：在顶部绘制标识
                mCanvas.save();  // 保存画布状态
                mCanvas.translate(0, distance_l);  // 平移到顶部标识位置
                mCanvas.drawBitmap(resBmp[CursorManage.Col1][selectIndex], (mCanvas.getWidth() - resBmp[CursorManage.Col1][selectIndex].getWidth()) / 2, 0, p);  // 绘制列1标识位图（水平居中）
                mCanvas.restore();  // 恢复画布状态
            }
            break;  // 跳出
            case TChan.Cursor_col_2: {  // 列光标2：在底部绘制标识
                mCanvas.save();  // 保存画布状态
                mCanvas.translate(0, GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - distance_r);  // 平移到底部标识位置
                mCanvas.drawBitmap(resBmp[CursorManage.Col2][selectIndex], (mCanvas.getWidth() - resBmp[CursorManage.Col2][selectIndex].getWidth()) / 2, 0, p);  // 绘制列2标识位图（水平居中）
                mCanvas.restore();  // 恢复画布状态
            }
            break;  // 跳出
        }


    }

    /**
     * 修改光标高度范围（列光标使用）
     *
     * @param height 新的高度值
     */
    public void changeCursorH(int height) {  // 修改光标高度范围
        CursorRect.bottom = height;  // 更新矩形下边界
    }

    /**
     * 绘制光标标识（旧版文字方式，已弃用）
     *
     * @param selected    是否选中
     * @param cursorType  光标类型
     */
    private void drawId(boolean selected, int cursorType) {  // 旧版标识绘制方法
        p.setTextSize(10);  // 设置字体大小
        Rect rect = Tools.getTextRect("Y2", p);  // 获取文本矩形区域
        if (selected) {  // 选中状态
            switch (cursorType) {  // 根据光标类型绘制
                case TChan.Cursor_row_1: {  // 行光标1
                    mCanvas.save();  // 保存画布
                    mCanvas.translate(distance_l, 0);  // 平移到左侧
                    p.setStyle(Paint.Style.FILL);  // 设置填充样式
                    mCanvas.drawPath(rowPathId, p);  // 绘制箭头路径
                    p.setColor(Color.BLACK);  // 设置黑色文字
                    mCanvas.drawText("Y1", (IdentifyWidth - rect.width()) / 2, (bmp.getHeight() - rect.height()) / 2 + rect.height(), p);  // 绘制Y1文字
                    mCanvas.restore();  // 恢复画布
                }
                break;  // 跳出
                case TChan.Cursor_row_2: {  // 行光标2
                    mCanvas.save();  // 保存画布
                    mCanvas.translate(GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - distance_r, 0);  // 平移到右侧
                    p.setStyle(Paint.Style.FILL);  // 设置填充样式
                    mCanvas.drawPath(rowPathId, p);  // 绘制箭头路径
                    p.setColor(Color.BLACK);  // 设置黑色文字
                    mCanvas.drawText("Y2", (IdentifyWidth - rect.width()) / 2, (bmp.getHeight() - rect.height()) / 2 + rect.height(), p);  // 绘制Y2文字
                    mCanvas.restore();  // 恢复画布
                }
                break;  // 跳出
                case TChan.Cursor_col_1: {  // 列光标1
                    mCanvas.save();  // 保存画布
                    mCanvas.translate(0, distance_l);  // 平移到顶部
                    p.setStyle(Paint.Style.FILL);  // 设置填充样式
                    mCanvas.drawPath(colPathId, p);  // 绘制箭头路径
                    p.setColor(Color.BLACK);  // 设置黑色文字
                    mCanvas.drawText("X1", (WordHeight - Tools.getTextRect("X1", p).width()) / 2, (IdentifyWidth - rect.height()) / 2 + rect.height(), p);  // 绘制X1文字
                    mCanvas.restore();  // 恢复画布
                }
                break;  // 跳出
                case TChan.Cursor_col_2: {  // 列光标2
                    mCanvas.save();  // 保存画布
                    mCanvas.translate(0, GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - distance_r);  // 平移到底部
                    p.setStyle(Paint.Style.FILL);  // 设置填充样式
                    mCanvas.drawPath(colPathId, p);  // 绘制箭头路径
                    p.setColor(Color.BLACK);  // 设置黑色文字
                    mCanvas.drawText("X2", (WordHeight - Tools.getTextRect("X2", p).width()) / 2, (IdentifyWidth - rect.height()) / 2 + rect.height(), p);  // 绘制X2文字
                    mCanvas.restore();  // 恢复画布
                }
                break;  // 跳出
            }
        } else {  // 未选中状态
            p.setXfermode(dstoutMode);  // 设置目标出混合模式（用于镂空效果）
            p.setPathEffect(null);  // 清除路径效果
            p.setStyle(Paint.Style.FILL);  // 设置填充样式
            p.setColor(Color.RED);  // 临时颜色（混合模式下不影响最终效果）
            switch (cursorType) {  // 根据光标类型绘制
                case TChan.Cursor_row_1: {  // 行光标1
                    int x = distance_l;  // 左侧x坐标
                    mCanvas.drawRect(x, 0, x + IdentifyWidth, WordHeight - 1, p);  // 绘制镂空矩形
                    p.setXfermode(srcMode);  // 切换回源绘制模式
                    mCanvas.save();  // 保存画布
                    p.setPathEffect(null);  // 清除路径效果
                    mCanvas.translate(x, 0);  // 平移到标识位置
                    p.setStyle(Paint.Style.STROKE);  // 设置描边样式
                    p.setColor(color);  // 设置光标颜色
                    mCanvas.drawPath(rowPathId, p);  // 绘制箭头描边
                    p.setStyle(Paint.Style.FILL);  // 设置填充样式
                    mCanvas.drawText("Y1", (IdentifyWidth - rect.width()) / 2, rect.height() + (bmp.getHeight() - rect.height()) / 2, p);  // 绘制Y1文字
                    mCanvas.restore();  // 恢复画布
                }
                break;  // 跳出
                case TChan.Cursor_row_2: {  // 行光标2
                    int x = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - distance_r;  // 右侧x坐标
                    mCanvas.drawRect(x, 0, x + IdentifyWidth, WordHeight - 1, p);  // 绘制镂空矩形
                    p.setXfermode(srcMode);  // 切换回源绘制模式
                    mCanvas.save();  // 保存画布
                    p.setPathEffect(null);  // 清除路径效果
                    mCanvas.translate(x, 0);  // 平移到标识位置
                    p.setStyle(Paint.Style.STROKE);  // 设置描边样式
                    p.setColor(color);  // 设置光标颜色
                    mCanvas.drawPath(rowPathId, p);  // 绘制箭头描边
                    p.setStyle(Paint.Style.FILL);  // 设置填充样式
                    mCanvas.drawText("Y2", (IdentifyWidth - rect.width()) / 2, rect.height() + (bmp.getHeight() - rect.height()) / 2, p);  // 绘制Y2文字
                    mCanvas.restore();  // 恢复画布
                }
                break;  // 跳出
                case TChan.Cursor_col_1: {  // 列光标1
                    y = distance_l;  // 顶部y坐标
                    mCanvas.drawRect(0, Math.round(y), WordHeight - 1, Math.round(y) + IdentifyWidth, p);  // 绘制镂空矩形
                    p.setXfermode(srcMode);  // 切换回源绘制模式
                    mCanvas.save();  // 保存画布
                    p.setPathEffect(null);  // 清除路径效果
                    mCanvas.translate(0, Math.round(y));  // 平移到标识位置
                    p.setStyle(Paint.Style.STROKE);  // 设置描边样式
                    p.setColor(color);  // 设置光标颜色
                    mCanvas.drawPath(colPathId, p);  // 绘制箭头描边
                    p.setStyle(Paint.Style.FILL);  // 设置填充样式
                    mCanvas.drawText("X1", (WordHeight - Tools.getTextRect("X1", p).width()) / 2, (IdentifyWidth - rect.height()) / 2 + rect.height(), p);  // 绘制X1文字
                    mCanvas.restore();  // 恢复画布
                }
                break;  // 跳出
                case TChan.Cursor_col_2: {  // 列光标2
                    y = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()) - distance_r;  // 底部y坐标
                    mCanvas.drawRect(0, (int)Math.round(y), WordHeight - 1, (int)Math.round(y) + IdentifyWidth, p);  // 绘制镂空矩形
                    p.setXfermode(srcMode);  // 切换回源绘制模式
                    mCanvas.save();  // 保存画布
                    p.setPathEffect(null);  // 清除路径效果
                    mCanvas.translate(0, Math.round(y));  // 平移到标识位置
                    p.setStyle(Paint.Style.STROKE);  // 设置描边样式
                    p.setColor(color);  // 设置光标颜色
                    mCanvas.drawPath(colPathId, p);  // 绘制箭头描边
                    p.setStyle(Paint.Style.FILL);  // 设置填充样式
                    mCanvas.drawText("X2", (WordHeight - Tools.getTextRect("X2", p).width()) / 2, (IdentifyWidth - rect.height()) / 2 + rect.height(), p);  // 绘制X2文字
                    mCanvas.restore();  // 恢复画布
                }
                break;  // 跳出
            }
        }
    }

    /**
     * 移动线条偏移（IWave接口方法，此实现为空）
     *
     * @param offsetX 水平偏移量
     * @param offsetY 垂直偏移量
     */
    @Override  // 覆写IWave接口方法
    public void moveLine(int offsetX, int offsetY) {  // 移动线条（空实现）

    }

    /**
     * 在标准Canvas上绘制（IWave接口方法，已弃用，改用OpenGL方式）
     *
     * @param canvas 标准Android画布
     */
    @Override  // 覆写IWave接口方法
    public void draw(Canvas canvas) {  // 标准Canvas绘制（已弃用）
//        synchronized (bmp) {
//            if ((cursorType == IWave.Cursor_row_1 || cursorType == IWave.Cursor_row_2) && visible == true)
//                canvas.drawBitmap(bmp, 0, y, null);
//            if ((cursorType == IWave.Cursor_col_1 || cursorType == IWave.Cursor_col_2) && visible == true)
//                canvas.drawBitmap(bmp, x, 0, null);
//        }
    }

    /**
     * 在OpenGL画布上绘制光标位图
     *
     * @param canvas OpenGL画布
     */
    @Override  // 覆写IWave接口方法
    public void draw(ICanvasGL canvas) {  // OpenGL绘制光标
        synchronized (bmp) {  // 同步锁定位图

            canvasGL = canvas;  // 保存画布引用
            if (TChan.isCursorRow2(cursorType) && visible  // 行光标且可见
                    && (getY() >= CursorRect.top && getY() < CursorRect.bottom)) {  // Y坐标在有效范围内
                if (isChanageBitmap){  // 位图已改变
                    canvas.invalidateTextureContent(bmp,oldBmp);  // 使纹理内容失效（需要重绘）
                    oldBmp = null;  // 清空旧位图引用
                }

                canvas.drawBitmap(bmp, 0, (int) Math.round(getY()) - centerLine);  // 在Y位置绘制行光标位图
            }
            if (TChan.isCursorCol2(cursorType) && visible  // 列光标且可见
                    && (x >= CursorRect.left && x < CursorRect.right)) {  // X坐标在有效范围内
                if (isChanageBitmap) {  // 位图已改变
                    canvas.invalidateTextureContent(bmp,oldBmp);  // 使纹理内容失效（需要重绘）
                    oldBmp = null;  // 清空旧位图引用
                }
                canvas.drawBitmap(bmp, ((int) x - centerLine), 0);  // 在X位置绘制列光标位图
            }

            isChanageBitmap = false;  // 重置位图改变标志
        }
    }

    /**
     * 结果初始化矩形（IWave接口方法，此实现为空）
     */
    @Override  // 覆写IWave接口方法
    public void resultIniRect() {  // 结果初始化矩形（空实现）

    }

    /**
     * 设置光标名称ID
     *
     * @param nameId 名称ID，与TChan常量对应
     */
    @Override  // 覆写IWave接口方法
    public void setLineNameId(int nameId) {  // 设置光标名称ID
        this.nameID = nameId;  // 保存名称ID
    }

    /**
     * 获取光标名称ID
     *
     * @return 名称ID
     */
    @Override  // 覆写IWave接口方法
    public int getLineNameID() {  // 获取光标名称ID
        return this.nameID;  // 返回名称ID
    }

    /**
     * 获取列光标的X像素位置
     *
     * @return X像素位置
     */
    @Override  // 覆写IWave接口方法
    public long getX() {  // 获取X坐标
        return x;  // 返回X像素位置
    }

    /**
     * 获取行光标的Y像素位置（UI坐标系，经过精度校准）
     *
     * @return Y像素位置（UI坐标系）
     */
    @Override  // 覆写IWave接口方法
    public double getY() {  // 获取Y坐标
        return ScopeBase.changeAccuracy(y * ScopeBase.getToUICoff());  // FPGA坐标转UI坐标并校准精度
    }

    /**
     * 设置列光标的X像素位置，并同步缓存和硬件命令
     *
     * @param x 新的X像素位置
     */
    @Override  // 覆写IWave接口方法
    public void setX(long x) {  // 设置X坐标
//        Logger.i(TAG,"CursorSetX() ==>"
//                +" nameId:0x"+Integer.toHexString(this.nameID)+" Colx:"+x+" - "+this.x
//                +" left:"+CursorRect.left+" right:"+CursorRect.right
//                +" width:"+CursorRect.width()
//                +" CacheIsLoadParamComplete:"+CacheUtil.get().isLoadParamComplete()
//                +" CacheIsLoadComplete:"+CacheUtil.get().isLoadComplete());

        if (x <= CursorRect.left) {  // X超出左边界
            this.x = 0;  // 钳位到0
        } else if (x >= CursorRect.width()) {  // X超出右边界
            this.x = CursorRect.width() - 1;  // 钳位到右边界-1
        } else {  // X在有效范围内
            this.x = x;  // 直接赋值
        }
        if (TChan.isCursorCol2(cursorType)) {  // 列光标需要同步缓存和命令
            if (cursorWorkMode == IWorkMode.WorkMode_YT || cursorWorkMode == IWorkMode.WorkMode_YTZOOM) {  // YT或YT缩放模式
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSORV_POSITION + cursorType, String.valueOf(this.x));  // 保存YT模式列光标位置到缓存
            } else if (cursorWorkMode == IWorkMode.WorkMode_XY) {  // XY模式
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_XY_CURSORV_POSITION + cursorType, String.valueOf(this.x));  // 保存XY模式列光标位置到缓存
            }

            if (cursorType == TChan.Cursor_col_1) {  // 列光标1
                Command.get().getCursor().Cx1((int) this.x, false);  // 发送列光标1位置命令
            } else {  // 列光标2
                Command.get().getCursor().Cx2((int) this.x, false);  // 发送列光标2位置命令
            }
        }
        if (this.onMovingWaveEvent != null) {  // 如果注册了移动事件回调
            onMovingWaveEvent.OnMovingWave(this, this.x, getY(), false, false);  // 触发移动事件
        }

        onRefresh();  // 通知OpenGL刷新纹理

    }

    /**
     * 设置行光标的Y像素位置，并同步缓存和硬件命令
     *
     * @param y 新的Y像素位置（UI坐标系）
     */
    @Override  // 覆写IWave接口方法
    public void setY(double y) {  // 设置Y坐标
//        Logger.i(TAG,"CursorSetY() ==>"
//                +" nameId:0x"+Integer.toHexString(this.nameID)+" Rowy:"+y+" - "+this.y
//                +" cursorWorkMode:"+cursorWorkMode
//                +" top:"+CursorRect.top+" bottom:"+CursorRect.bottom
//                +" CacheIsLoadParamComplete:"+CacheUtil.get().isLoadParamComplete()
//                +" CacheIsLoadComplete:"+CacheUtil.get().isLoadComplete());

        if (y <= CursorRect.top) {  // Y超出上边界
            this.y = 0;  // 钳位到0
        } else if (y >= CursorRect.height()) {  // Y超出下边界
            this.y = CursorRect.height() - 1;  // 钳位到下边界-1
        } else {  // Y在有效范围内
            this.y = y;  // 直接赋值
        }
        this.y = this.y * ScopeBase.getToFPGACoff();  // UI坐标转FPGA坐标
        if (TChan.isCursorRow2(cursorType)) {  // 行光标需要同步缓存和命令
            if (cursorWorkMode == IWorkMode.WorkMode_YT) {  // YT模式
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION + cursorType, String.valueOf(this.y));  // 保存YT模式行光标位置到缓存
            } else if (cursorWorkMode == IWorkMode.WorkMode_XY) {  // XY模式
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_XY_CURSORH_POSITION + cursorType, String.valueOf(this.y));  // 保存XY模式行光标位置到缓存
            } else if (cursorWorkMode == IWorkMode.WorkMode_YTZOOM) {  // YT缩放模式
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION + cursorType, String.valueOf(this.y * GlobalVar.get().toYTCoef()));  // 保存缩放模式行光标位置（乘YT系数）到缓存

            }

            if (cursorType == TChan.Cursor_row_1) {  // 行光标1
                Command.get().getCursor().CY1(getY(), false);  // 发送行光标1位置命令
            } else {  // 行光标2
                Command.get().getCursor().CY2(getY(), false);  // 发送行光标2位置命令
            }
        }
        if (this.onMovingWaveEvent != null) {  // 如果注册了移动事件回调
            onMovingWaveEvent.OnMovingWave(this, x, getY(), false, false);  // 触发移动事件
        }
        onRefresh();  // 通知OpenGL刷新纹理
    }


    /**
     * 设置光标颜色，并重新绘制
     *
     * @param color 颜色值
     */
    @Override  // 覆写IWave接口方法
    public void setColor(int color) {  // 设置颜色
        this.color = color;  // 保存颜色值
        this.currCh = TChan.getColorToChannel(context,color);  // 根据颜色反查通道索引
        draw();  // 重新绘制光标
    }

    /**
     * 获取光标颜色
     *
     * @return 颜色值
     */
    @Override  // 覆写IWave接口方法
    public int getColor() {  // 获取颜色
        return color;  // 返回颜色值
    }

    /**
     * 设置选中状态，并重新绘制
     *
     * @param selected true表示选中，false表示取消选中
     */
    @Override  // 覆写IWave接口方法
    public void setSelected(boolean selected) {  // 设置选中状态
        this.selected = selected;  // 保存选中状态
        draw();  // 重新绘制光标（选中=实线，未选中=虚线）
        if (onSelectChangeEvent != null) onSelectChangeEvent.OnSelectChange(this, selected);  // 触发选中状态变化事件
    }

    /**
     * 获取选中状态
     *
     * @return true表示选中，false表示未选中
     */
    @Override  // 覆写IWave接口方法
    public boolean isSelected() {  // 获取选中状态
        return this.selected;  // 返回选中状态
    }

    /**
     * 设置选中状态变化事件回调
     *
     * @param onSelectChangeEvent 选中状态变化事件回调
     */
    @Override  // 覆写IWave接口方法
    public void setOnSelectChangeEvent(OnSelectChangeEvent onSelectChangeEvent) {  // 设置选中变化事件
        this.onSelectChangeEvent = onSelectChangeEvent;  // 保存回调引用
    }

    /**
     * 设置可见性，并刷新纹理
     *
     * @param visible true表示可见，false表示隐藏
     */
    @Override  // 覆写IWave接口方法
    public void setVisible(boolean visible) {  // 设置可见性
        this.visible = visible;  // 保存可见性
        onRefresh();  // 通知OpenGL刷新纹理
    }

    /**
     * 获取可见性
     *
     * @return true表示可见，false表示隐藏
     */
    @Override  // 覆写IWave接口方法
    public boolean getVisible() {  // 获取可见性
        return visible;  // 返回可见性
    }

    /**
     * 设置移动事件回调
     *
     * @param onMovingWaveEvent 移动事件回调
     */
    @Override  // 覆写IWave接口方法
    public void setOnMovingWaveEvent(OnMovingWaveEvent onMovingWaveEvent) {  // 设置移动事件回调
        this.onMovingWaveEvent = onMovingWaveEvent;  // 保存回调引用
    }

    //移动px个像素
    /**
     * 按指定像素数移动光标位置
     *
     * @param px 移动像素数，正数向右/下移动，负数向左/上移动
     */
    @Override  // 覆写IWave接口方法
    public void movePix(double px) {  // 按像素移动光标
//        Logger.i(TAG, "movePix() ==>"
//                +" x:"+this.x+" y:"+this.y+" px:"+px
//                +" CursorType:0x"+Integer.toHexString(cursorType)
//                +" nameId:0x"+Integer.toHexString(this.nameID));
        if (px > 0) {  // 正向移动（右/下）
            if (TChan.isCursorRow2(cursorType)) {  // 行光标向下移动
//                px = ScopeBase.changeAccuracy(px * ScopeBase.getToUICoff());
                if ((getY() + px) < GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode())) {  // 未超出下边界
                    setY(getY() + px);  // 设置新的Y位置
                }
            } else if (TChan.isCursorCol2(cursorType)) {  // 列光标向右移动
                if ((x + px) < GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode())) {  // 未超出右边界
                    setX(Math.round(x + px));  // 设置新的X位置
                }
            }
        } else {  // 反向移动（左/上）
            if (TChan.isCursorRow2(cursorType)) {  // 行光标向上移动
//                px = ScopeBase.changeAccuracy(px * ScopeBase.getToUICoff());
                if ((getY() + px) >= 0) {  // 未超出上边界
                    setY(getY() + px);  // 设置新的Y位置
                }
            } else if (TChan.isCursorCol2(cursorType)) {  // 列光标向左移动
                if ((x + px) >= 0) {  // 未超出左边界
                    setX(Math.round(x + px));  // 设置新的X位置
                }
            }
        }
    }

    /**
     * 重新初始化位图（在刷新时调用）
     */
    private void reInitBmp(){  // 重新初始化位图

        oldBmp = bmp;  // 保存旧位图引用（用于纹理更新）
        switch (this.cursorType) {  // 根据光标类型创建新位图
            case TChan.Cursor_row_1:  // 行光标1
            case TChan.Cursor_row_2:  // 行光标2
                bmp = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(this.cursorWorkMode), WordHeight, Bitmap.Config.ARGB_8888);  // 创建行光标位图
                break;  // 跳出
            case TChan.Cursor_col_1:  // 列光标1
            case TChan.Cursor_col_2:  // 列光标2
                bmp = Bitmap.createBitmap(WordHeight, GlobalVar.get().getWaveZoneHeight_Pix(this.cursorWorkMode), Bitmap.Config.ARGB_8888);  // 创建列光标位图
                break;  // 跳出
            default:  // 其他类型
                bmp = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(this.cursorWorkMode), 1, Bitmap.Config.ARGB_8888);  // 创建默认1像素高位图
                break;  // 跳出
        }
        mCanvas = new Canvas(bmp);  // 以新位图创建画布
        CursorRect.bottom = GlobalVar.get().getWaveZoneHeight_Pix(this.cursorWorkMode);  // 更新下边界
    }


    //endregion
    /**
     * 刷新光标（重新初始化位图并绘制）
     */
    public void refresh() {  // 刷新光标
        reInitBmp();  // 重新初始化位图
        draw();  // 重新绘制
    }
}
