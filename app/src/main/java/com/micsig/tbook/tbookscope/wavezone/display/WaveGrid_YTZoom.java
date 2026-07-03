package com.micsig.tbook.tbookscope.wavezone.display;  // 示波器波形显示区域的网格管理包

import android.graphics.Bitmap;  // 位图类，用于离屏绘制网格
import android.graphics.Canvas;  // 画布类，用于在Bitmap上绘制图形
import android.graphics.Color;  // 颜色工具类
import android.graphics.DashPathEffect;  // 虚线效果，用于绘制虚线格线
import android.graphics.Paint;  // 画笔类，控制绘制样式
import android.graphics.PorterDuff;  // 混合模式枚举
import android.graphics.PorterDuffXfermode;  // 混合模式转换器
import android.graphics.RectF;  // 浮点矩形区域类

import com.chillingvan.canvasgl.ICanvasGL;  // OpenGL画布接口
import com.micsig.tbook.scope.ScopeBase;  // 示波器基础配置类
import com.micsig.tbook.tbookscope.GlobalVar;  // 全局变量单例
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;  // 工作模式管理器

import java.util.Arrays;  // 数组工具类

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │  模块定位：示波器YT缩放模式网格绘制器                                        │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  核心职责：在YTZoom模式（时域缩放模式）下绘制缩放区域的网格，                │
 * │           包括十字线、十字交叉点、虚线格线、边框等元素。                      │
 * │           缩放区域的高度为主波形区域的1/4，宽度固定1800像素。                  │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  架构设计：实现IGrid接口（未实现IWorkMode），采用离屏Bitmap+Canvas方式绘制， │
 * │           通过ICanvasGL将Bitmap渲染到OpenGL画布上。                           │
 * │           位图宽度固定为1800像素，高度根据缩放比例动态计算。                   │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  数据流向：属性/高度变更 → drawGrid() → Bitmap离屏绘制 →                    │
 * │           draw(ICanvasGL) → OpenGL渲染到屏幕                                 │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  依赖关系：                                                                │
 * │           - IGrid：网格属性与绘制接口                                       │
 * │           - WaveGridManage：网格属性常量来源（GridAttr_*）                   │
 * │           - GlobalVar：获取波形区域宽度像素值                                │
 * │           - ScopeBase：获取垂直格数配置                                     │
 * │           - WorkModeManage：获取当前工作模式                                 │
 * │           - WaveGrid_YT.getCrossLine()：计算十字线颜色                      │
 * │           - ICanvasGL：OpenGL画布，用于GPU加速渲染                           │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  使用场景：YTZoom模式下由WaveGridManage调用draw()进行缩放区域网格渲染，       │
 * │           在设置面板中调整网格属性/亮度时同步更新。                            │
 * └────────────────────────────────────────────────────────────────────────────┘
 *
 * Created by liwb on 2017/6/16.
 */

public class WaveGrid_YTZoom implements IGrid {  // 实现网格接口（不需要实现IWorkMode，由WaveGridManage统一管理模式切换）

    //region 单例创建
//    private static class WaveGrid_YTZoom_Holder {
//        private static final WaveGrid_YTZoom instance = new WaveGrid_YTZoom();
//    }
//
//    public static final WaveGrid_YTZoom get() {
//        return WaveGrid_YTZoom_Holder.instance;
//    }
    //endregion

    //region 属性
    /** 起始亮度 */
    private final int BaseBright=40;  // 网格线最低亮度值，避免完全不可见
    private int GridAttr_Enum = WaveGridManage.GridAttr_CrossLine | WaveGridManage.GridAttr_CrossPoint | WaveGridManage.GridAttr_ALLPoint | WaveGridManage.GridAttr_Frame;  // 网格属性组合（引用WaveGridManage的常量）
    private int GridLine_Bright = Color.rgb(0x80, 0x80, 0x80);  // 网格线颜色，默认灰色（R=G=B=0x80）
    private static int zoomHeight = 1040 / 4;//默认  // 缩放区域高度，默认为主波形高度的1/4

    /**
     * 刷新网格，重新绘制整个网格位图
     */
    @Override
    public void refresh() {
        drawGrid();  // 调用核心绘制方法重绘网格
    }

    /**
     * 设置垂直方向每格像素高度，动态调整缩放区域高度并重绘
     *
     * @param heightDiv 每格的像素高度
     */
    @Override
    public void setHeightDiv(int heightDiv) {
        zoomHeight = (heightDiv * ScopeBase.getVerticalGridCnt()) / 4;  // 缩放高度=总波形高度/4（总高度=格高×垂直格数）
        reInitBmp();  // 重新初始化位图
        drawGrid();  // 重新绘制网格
    }

    /**
     * 获取当前网格线属性标志
     *
     * @return 网格属性位掩码
     */
    public int getGridLine_Attr() {
        return GridAttr_Enum;  // 返回当前网格属性组合值
    }

    /**
     * 设置网格线属性并立即重绘
     *
     * @param gridLine_Attr 网格属性位掩码
     */
    public void setGridLine_Attr(int gridLine_Attr) {
        GridAttr_Enum = gridLine_Attr;  // 更新网格属性
        drawGrid();  // 属性变更后重绘
    }

    /**
     * 获取网格线亮度百分比
     *
     * @return 亮度百分比（0-100）
     */
    public int getGridLine_Bright() {
//        return GridLine_Bright * 2 * 100 / 255 - BaseBright;
        return (int)((GridLine_Bright-BaseBright)/180.0f);  // 将RGB分量值转换为0-100的亮度百分比
    }

    /**
     * 设置网格线亮度百分比并重绘
     *
     * @param gridLine_Bright 亮度百分比（0-100）
     */
    public void setGridLine_Bright(int gridLine_Bright) {
//        Logger.i(Command.TAG,"i:"+gridLine_Bright);
        //int c=gridLine_Bright%256;
        int c=(int)(BaseBright+(180*gridLine_Bright*1.0/100));  // 将0-100的百分比映射到亮度范围[BaseBright, BaseBright+180]
//        int c = (gridLine_Bright + BaseBright) * 255 / 100 / 2;
        GridLine_Bright = Color.rgb(c, c, c);  // 设置灰度颜色（R=G=B=c）
        drawGrid();  // 亮度变更后重绘
    }
    //endregion

    private Bitmap bmp;  // 离屏位图，网格绘制到此Bitmap上
    private Bitmap bmpOld;  // 旧位图引用，用于OpenGL纹理更新时的对比
    private Canvas mCanvas;  // 与bmp关联的Canvas，用于在Bitmap上绘制
    private Paint paint;  // 画笔，控制绘制样式
    private boolean isChanageBitmap = false;  // 标记位图是否已变更，用于OpenGL纹理刷新判断
    private ICanvasGL canvasGL;  // 持有当前OpenGL画布引用，用于刷新纹理

    /**
     * 通知OpenGL画布刷新纹理内容
     */
    public void onRefresh(){
        if(canvasGL != null){  // 画布引用非空时
            canvasGL.onRefreshTexture();  // 刷新OpenGL纹理
        }
    }

    /**
     * 构造函数，创建画笔并初始化网格位图和绘制
     */
    public WaveGrid_YTZoom() {
        paint = new Paint();  // 创建画笔
        reInitBmp();  // 初始化位图
        drawGrid();  // 初始绘制网格
    }


    /**
     * 将网格位图绘制到OpenGL画布上
     *
     * @param canvas OpenGL画布
     */
    public void draw(ICanvasGL canvas) {
        synchronized (bmp) {  // 同步锁保护位图访问
            canvasGL = canvas;  // 保存OpenGL画布引用
            canvasGL.setSize(canvas.getWidth(), zoomHeight);  // 设置OpenGL画布大小为缩放区域尺寸
            if(isChanageBitmap) {  // 位图内容变更时
                canvas.invalidateTextureContent(bmp, bmpOld);  // 通知OpenGL纹理已失效，传入新旧位图对比
                bmpOld = null;  // 释放旧位图引用
            }
            RectF src = new RectF(0, 0, 1800, zoomHeight);  // 源矩形：位图全部区域（宽1800，高zoomHeight）
            RectF des = new RectF(0, 0, 1800, zoomHeight);  // 目标矩形：1:1映射
            canvas.drawBitmap(bmp, src, des);  // 将源区域绘制到目标区域
            //canvas.drawBitmap(bmp,new RectF(0,0,bmp.getWidth(),bmp.getHeight()),new RectF(0,0,bmp.getWidth(),bmp.getHeight()),isChanageBitmap);
            isChanageBitmap = false;  // 重置变更标志

        }
    }

    /**
     * 核心网格绘制方法，根据当前属性标志依次绘制各类网格元素
     */
    private void drawGrid() {
        synchronized (bmp) {  // 同步锁保护位图访问

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));  // 设置清除模式
            mCanvas.drawPaint(paint);  // 清空整个位图
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));  // 恢复正常绘制模式


            if ((GridAttr_Enum & WaveGridManage.GridAttr_CrossPoint) == WaveGridManage.GridAttr_CrossPoint) {  // 检查是否启用十字交叉点
                drawCrossPoint(mCanvas);  // 绘制十字交叉点
            }
            if ((GridAttr_Enum & WaveGridManage.GridAttr_ALLPoint) == WaveGridManage.GridAttr_ALLPoint) {  // 检查是否启用全格线
//                drawAllPoint(mCanvas);
                drawAllPoint_line(mCanvas);  // 绘制虚线格线（替代点阵方式）
            }
            if ((GridAttr_Enum & WaveGridManage.GridAttr_CrossLine) == WaveGridManage.GridAttr_CrossLine) {  // 检查是否启用十字线
                drawCrossLine(mCanvas);  // 绘制十字线
            }
            if ((GridAttr_Enum & WaveGridManage.GridAttr_Frame) == WaveGridManage.GridAttr_Frame) {  // 检查是否启用边框
                drawFrame(mCanvas);  // 绘制边框
            }
            isChanageBitmap = true;  // 标记位图已变更
        }
    }

    /**
     * 绘制十字线（水平中线+垂直中线），用点阵方式绘制，
     * 垂直线在小格位置绘制额外点标记
     *
     * @param canvas 绘制目标画布
     */
    private void drawCrossLine(Canvas canvas) {
        int zoomWidth = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());  // 获取当前模式下的波形区宽度

        paint.setColor(WaveGrid_YT.getCrossLine(GridLine_Bright));  // 设置十字线颜色（比网格线更亮）
        paint.setStrokeWidth(1);  // 设置点宽度为1
        //横线
        float[] rowPixs = new float[zoomWidth / 2 * 2];  // 水平中线点坐标数组（每2像素一个点，每个点2个float）
        Arrays.fill(rowPixs,Float.MAX_VALUE);  // 填充无效值
        for (int i = 0; i < zoomWidth; i += 2) {  // 每2个像素绘制一个点
            rowPixs[i] = i;  // 记录x坐标（利用索引i直接存储，奇数位存x）
            rowPixs[i + 1] = (float) zoomHeight / 2;  // 记录y坐标（固定为缩放区域高度的一半）
        }
        canvas.drawPoints(rowPixs, paint);  // 批量绘制水平中线上的所有点
        //纵线
        float[] colPixs = new float[(zoomHeight + zoomHeight / 5 * 2) * 2];  // 垂直中线点坐标数组（包含小格位置的额外点）
        Arrays.fill(colPixs,Float.MAX_VALUE);  // 填充无效值
        int index = 0;  // 点数组索引
        for (int i = 0; i < zoomHeight; i += 2) {  // 每2个像素绘制一个点
            if (i % 5 == 0) {  // 每5个像素位置（小格间距），绘制额外标记点
                colPixs[index] = (float)zoomWidth / 2 - 2;  // x坐标偏移-2
                index++;
                colPixs[index] = i;  // 记录y坐标
                index++;

                colPixs[index] = (float)zoomWidth / 2 + 2;  // x坐标偏移+2
                index++;
                colPixs[index] = i;  // 记录y坐标
                index++;
            }
            colPixs[index] = (float)zoomWidth / 2;  // 普通位置，x坐标为中心线
            index++;
            colPixs[index] = i;  // 记录y坐标
            index++;
        }
        canvas.drawPoints(colPixs, paint);  // 批量绘制垂直中线上的所有点

    }

    // 画小格点
    /**
     * 绘制十字交叉点（大格交叉处的点标记）
     *
     * @param canvas 绘制目标画布
     */
    private void drawCrossPoint(Canvas canvas) {
        int zoomWidth = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());  // 获取当前模式下的波形区宽度
        paint.setColor(GridLine_Bright);  // 设置点颜色为网格线亮度色
        paint.setStrokeWidth(1);  // 设置点宽度

        for (int i = 0; i < zoomWidth; i += 30) {  // 每隔30像素（水平小格间距）绘制一列交叉点
            float[] colPixs = new float[100/10 * 2];  // 每列10个点，每个点2个float
            Arrays.fill(colPixs,Float.MAX_VALUE);  // 填充无效值
            int index = 0;  // 点数组索引
            for (int j = 0; j < zoomHeight; j += zoomHeight / 10) {   //  150/10=15  // 垂直方向每隔1/10高度绘制一个点
                if(index >= colPixs.length) continue;  // 数组越界保护
                colPixs[index] = i;  // 记录x坐标
                colPixs[index + 1] = j;  // 记录y坐标
                index += 2;  // 索引前进2位
            }
            canvas.drawPoints(colPixs, paint);  // 批量绘制当前列的交叉点
        }

    }

    // 画大格线
    /**
     * 绘制大格线上的点阵（每2像素一个点，绘制所有垂直大格线）
     *
     * @param canvas 绘制目标画布
     */
    private void drawAllPoint(Canvas canvas) {
        int zoomWidth = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());  // 获取当前模式下的波形区宽度

        paint.setColor(GridLine_Bright);  // 设置点颜色
        paint.setStrokeWidth(1);  // 设置点宽度

        for (int i = 0; i <= zoomWidth; i += 150) {  // 每隔150像素（大格宽度）绘制一条垂直线
            float[] colPixs = new float[zoomHeight / 2 * 2];  // 垂直线点坐标数组
            Arrays.fill(colPixs,Float.MAX_VALUE);  // 填充无效值
            for (int j = 0; j < zoomHeight; j += 2) {  // 每2个像素绘制一个点
                colPixs[j] = i;  // 记录x坐标（固定为大格线位置）
                colPixs[j + 1] = j;  // 记录y坐标
            }
            canvas.drawPoints(colPixs, paint);  // 批量绘制当前垂直线上的所有点
        }
    }

    /**
     * 绘制虚线格线（用虚线绘制所有大格的水平和垂直分隔线）
     *
     * @param canvas 绘制目标画布
     */
    private void drawAllPoint_line(Canvas canvas){
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{1, 1, 1, 1}, 0);  // 创建虚线效果：1像素绘制、1像素间隔、1像素绘制、1像素间隔
        paint.setPathEffect(dashPathEffect);  // 应用虚线效果到画笔
        int zoomWidth = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());  // 获取当前模式下的波形区宽度
        for (int y = zoomHeight / 2; y < canvas.getHeight(); y += zoomHeight / 2) {  // 遍历垂直方向，每半高度绘制一条水平线
            canvas.drawLine(0,y,canvas.getWidth(),y,paint);  // 绘制水平虚线分隔线
        }
        for (int x = 150; x < canvas.getWidth(); x += 150) {  // 遍历水平方向，每150像素绘制一条垂直线
            canvas.drawLine(x, 0, x, canvas.getHeight(), paint);  // 绘制垂直虚线分隔线
        }
        paint.setPathEffect(null);  // 清除画笔的路径效果，避免影响后续绘制
    }

    /**
     * 绘制网格边框（青色矩形框线）
     *
     * @param canvas 绘制目标画布
     */
    private void drawFrame(Canvas canvas) {
        paint.setColor(0xFF008080);  // 设置边框颜色为青色（不透明）
        paint.setStrokeWidth(1);  // 设置线宽为1像素
        canvas.drawLine(0, 0, canvas.getWidth(), 0, paint);  // 绘制顶边框线
        canvas.drawLine(0, 0, 0, canvas.getHeight(), paint);  // 绘制左边框线
        canvas.drawLine(0, canvas.getHeight() - 1, canvas.getWidth(), canvas.getHeight() - 1, paint);  // 绘制底边框线（-1避免越界）
        canvas.drawLine(canvas.getWidth() - 1, 0, canvas.getWidth() - 1, canvas.getHeight(), paint);  // 绘制右边框线（-1避免越界）
    }

    /**
     * 重新初始化位图，根据当前缩放高度创建新的Bitmap和Canvas
     */
    private void reInitBmp() {
        bmpOld = bmp;  // 保存旧位图引用，用于后续纹理更新对比
        bmp = Bitmap.createBitmap(1800, zoomHeight, Bitmap.Config.ARGB_8888);  // 创建固宽1800、高度为zoomHeight的新位图
        mCanvas = new Canvas(bmp);  // 创建与新位图关联的Canvas
    }



}
