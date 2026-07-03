package com.micsig.tbook.tbookscope.wavezone.display;  // 示波器波形显示区域的网格管理包

import android.graphics.Bitmap;  // 位图类，用于离屏绘制网格
import android.graphics.Canvas;  // 画布类，用于在Bitmap上绘制图形
import android.graphics.Color;  // 颜色工具类
import android.graphics.DashPathEffect;  // 虚线效果（本文件未使用，但保留导入）
import android.graphics.Paint;  // 画笔类，控制绘制样式
import android.graphics.PorterDuff;  // 混合模式枚举
import android.graphics.PorterDuffXfermode;  // 混合模式转换器
import android.graphics.RectF;  // 矩形区域类

import com.chillingvan.canvasgl.ICanvasGL;  // OpenGL画布接口
import com.micsig.base.Logger;  // 日志工具
import com.micsig.tbook.tbookscope.GlobalVar;  // 全局变量单例
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;  // 工作模式接口

import java.util.Arrays;  // 数组工具类，用于填充默认值

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │  模块定位：示波器XY模式网格绘制器                                            │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  核心职责：在XY模式（李萨如图形模式）下绘制示波器网格，包括十字线、             │
 * │           十字交叉点、全格线、边框等元素。网格大小固定不变。                    │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  架构设计：实现IWorkMode和IGrid接口，采用离屏Bitmap+Canvas方式绘制，         │
 * │           通过ICanvasGL将Bitmap渲染到OpenGL画布上。                           │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  数据流向：网格属性/亮度变更 → drawGrid() → Bitmap离屏绘制 →                │
 * │           draw(ICanvasGL) → OpenGL渲染到屏幕                                 │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  依赖关系：                                                                │
 * │           - IGrid：网格属性与绘制接口                                       │
 * │           - IWorkMode：工作模式切换接口                                     │
 * │           - GlobalVar：获取波形区域宽高像素值                                │
 * │           - WaveGrid_YT.getCrossLine()：计算十字线颜色                      │
 * │           - ICanvasGL：OpenGL画布，用于GPU加速渲染                           │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  使用场景：XY模式下由WaveGridManage调用draw()进行网格渲染。                   │
 * │           网格在构造时一次性创建，后续仅通过属性/亮度变更触发重绘。             │
 * └────────────────────────────────────────────────────────────────────────────┘
 *
 * Created by liwb on 2017/11/2.
 * XY的网络有个特点，它是始终不会变的。
 * 他与YT网络是一样的，都有那几个5个属性
 * 要按理说应该搞个接口，不过就这两个，手写一下算了。
 */

public class WaveGrid_XY implements IWorkMode, IGrid {  // 实现工作模式接口和网格接口

    //region 属性
    /** 起始亮度 */
    private final int BaseBright=40;  // 网格线最低亮度值，避免完全不可见
    private int GridAttr_Enum = GridAttr_CrossLine | GridAttr_CrossPoint | GridAttr_ALLPoint | GridAttr_Frame;  // 网格属性组合：十字线|十字点|全网点|边框
    private int GridLine_Bright = Color.argb(0x80, 0x80, 0x80, 0x80);  // 网格线颜色，默认半透明灰色


    /**
     * 刷新网格，重新绘制整个网格位图
     */
    @Override
    public void refresh() {
        drawGrid();  // 调用核心绘制方法重绘网格
    }

    /**
     * 设置垂直方向每格像素高度（XY模式下固定不变，空实现）
     *
     * @param heightDiv 每格像素高度
     */
    @Override
    public void setHeightDiv(int heightDiv) {

    }

    /**
     * 获取当前网格线属性标志
     *
     * @return 网格属性位掩码
     */
    @Override
    public int getGridLine_Attr() {
        return GridAttr_Enum;  // 返回当前网格属性组合值
    }

    /**
     * 设置网格线属性并立即重绘
     *
     * @param gridLine_Attr 网格属性位掩码
     */
    @Override
    public void setGridLine_Attr(int gridLine_Attr) {
        GridAttr_Enum = gridLine_Attr;  // 更新网格属性
        drawGrid();  // 属性变更后重绘
    }


    /**
     * 获取网格线亮度百分比
     *
     * @return 亮度百分比（0-100）
     */
    @Override
    public int getGridLine_Bright() {
//        return GridLine_Bright * 2 * 100 / 255 - BaseBright;
        return (int)((GridLine_Bright-BaseBright)/180.0f);  // 将RGB分量值转换为0-100的亮度百分比
    }

    /**
     * 亮度范围50-230之间
     * @param gridLine_Bright 亮度百分比（0-100）
     */
    @Override
    public void setGridLine_Bright(int gridLine_Bright) {
        //int c=gridLine_Bright%256;
//        int c = (gridLine_Bright + BaseBright) * 255 / 100 / 2;
        int c=(int)(BaseBright+(180*gridLine_Bright*1.0/100));  // 将0-100的百分比映射到亮度范围[BaseBright, BaseBright+180]
        GridLine_Bright = Color.rgb(c, c, c);  // 设置灰度颜色（R=G=B=c）
        drawGrid();  // 亮度变更后重绘
    }
    //endregion

    private Bitmap bmp;  // 离屏位图，网格绘制到此Bitmap上
    private Canvas mCanvas;  // 与bmp关联的Canvas，用于在Bitmap上绘制
    private Paint paint;  // 画笔，控制绘制样式
    /**
     * 图像是否改变，即图像像素是否改变
     */
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
     * 构造函数，创建XY模式的网格位图并初始化绘制
     */
    public WaveGrid_XY() {
        //temp 宽高应该在一个静态类中，直接获取到。
        bmp = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_XY),  // 获取XY模式波形区宽度
                GlobalVar.get().getWaveZoneHeight_Pix(IWorkMode.WorkMode_XY), Bitmap.Config.ARGB_8888);  // 获取XY模式波形区高度，ARGB_8888格式
        mCanvas = new Canvas(bmp);  // 创建与Bitmap关联的Canvas
        paint = new Paint();  // 创建画笔
        drawGrid();  // 初始绘制网格

    }

    //region interface IworkMode
    /**
     * 切换工作模式（XY模式下无需特殊处理，空实现）
     *
     * @param workMode 工作模式常量
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {

    }
    //endregion

    //region 单例创建
//    private static class WaveGrid_XY_Holder {
//        private static final WaveGrid_XY instance = new WaveGrid_XY();
//    }
//
//    public static final WaveGrid_XY getInstance() {
//        return WaveGrid_XY_Holder.instance;
//    }
    //endregion

    /**
     * 将网格位图绘制到OpenGL画布上
     *
     * @param canvas OpenGL画布
     */
    @Override
    public void draw(ICanvasGL canvas) {
        synchronized (bmp) {  // 同步锁保护位图访问，防止并发修改
            canvasGL = canvas;  // 保存OpenGL画布引用
            //canvas.drawBitmap(bmp,0,0,isChanageBitmap);
            if(isChanageBitmap) canvas.invalidateTextureContent(bmp,null);  // 位图内容变更时，通知OpenGL纹理已失效需更新
            canvas.drawBitmap(bmp, new RectF(0, 0, bmp.getWidth(), bmp.getHeight()), new RectF(0, 0, bmp.getWidth(), bmp.getHeight()));  // 将位图绘制到画布，源矩形=目标矩形（1:1映射）
            isChanageBitmap = false;  // 重置变更标志
        }
    }

    private int widthBigDiv = 100;  // 水平方向大格间距（像素）
    private int heightBigDiv = 100;  // 垂直方向大格间距（像素）
    private int widthSmallDiv = widthBigDiv/5;  // 水平方向小格间距，每大格分5小格
    private int heightSmallDiv = heightBigDiv/5;  // 垂直方向小格间距，每大格分5小格

    /**
     * 核心网格绘制方法，根据当前属性标志依次绘制各类网格元素
     */
    private void drawGrid() {
        synchronized (bmp) {  // 同步锁保护位图访问

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));  // 设置清除模式
            mCanvas.drawPaint(paint);  // 清空整个位图
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));  // 恢复正常绘制模式


            if ((GridAttr_Enum & GridAttr_CrossPoint) == GridAttr_CrossPoint) {  // 检查是否启用十字交叉点
                drawCrossPoint(mCanvas);  // 绘制十字交叉点
            }
            if ((GridAttr_Enum & GridAttr_ALLPoint) == GridAttr_ALLPoint) {  // 检查是否启用全格线
//                drawAllPoint(mCanvas);
                drawALLLine(mCanvas);  // 绘制全格线（替代点阵方式）
            }
            if ((GridAttr_Enum & GridAttr_CrossLine) == GridAttr_CrossLine) {  // 检查是否启用十字线
                drawCrossLine(mCanvas);  // 绘制十字线
            }
            if ((GridAttr_Enum & GridAttr_Frame) == GridAttr_Frame) {  // 检查是否启用边框
                drawFrame(mCanvas);  // 绘制边框
            }

            isChanageBitmap = true;  // 标记位图已变更
            onRefresh();  // 通知OpenGL刷新纹理
        }
    }

    //region 私有函数

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
        // FIXME: 2024/7/8  WorkTile:#2260 效果上看XY界面右下两个边比左上宽，故作此修改。
        canvas.drawLine(0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight(), paint);  // 绘制底边框线
        canvas.drawLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight(), paint);  // 绘制右边框线

    }

    //十字线
    /**
     * 绘制十字线（水平中线+垂直中线），用点阵方式绘制，
     * 大格交叉处4个点，小格交叉处2个点，其余位置单点
     *
     * @param canvas 绘制目标画布
     */
    private void drawCrossLine(Canvas canvas) {
        int rowPoint = canvas.getWidth() / 2;  // 水平中线所需的基准点数
        int rowpoint1 = 8 * 4 * 2; //18格 1条4条线，一条线2个点  // 大格交叉处的额外点数
        int rowpoint2 = 8 * 4;   //18格 1格4个点  // 小格交叉处的额外点数

        int colPoint = canvas.getHeight();  // 垂直中线所需的基准点数
        int colPoint1 = 8 * 4 * 2;  // 大格交叉处的额外点数
        int colPoint2 = 8 * 4;  // 小格交叉处的额外点数

        float[] rowPts = new float[(rowPoint + rowpoint1 + rowpoint2) * 2];  // 水平中线点坐标数组，每个点2个float(x,y)
        float[] colPts = new float[(colPoint + colPoint1 + colPoint2) * 2];  // 垂直中线点坐标数组
        Arrays.fill(rowPts,Float.MAX_VALUE);  // 填充默认值，Float.MAX_VALUE表示无效点（不会被绘制）
        Arrays.fill(colPts,Float.MAX_VALUE);  // 填充默认值
        paint.setColor(WaveGrid_YT.getCrossLine(GridLine_Bright));  // 设置十字线颜色（比网格线更亮）
        paint.setStrokeWidth(1);  // 设置点宽度为1
        //画横线
        int index = 0;  // 点数组索引
        for (int x = 0; x < canvas.getWidth(); x += 2) {  // 每2个像素绘制一个点
            //被50整除画4个点，被5整除画2个点。
            if (x % widthBigDiv == 0 && x % widthSmallDiv == 0) {  // 大格交叉处（同时被大小格间距整除）
                rowPts[index] = x;  // 记录x坐标
                index = index + 1;
                rowPts[index] = canvas.getHeight() / 2 - 2;  // y坐标偏移-2
                index += 1;

                rowPts[index] = x;  // 记录x坐标
                index += 1;
                rowPts[index] = canvas.getHeight() / 2 - 4;  // y坐标偏移-4
                index += 1;

                rowPts[index] = x;  // 记录x坐标
                index += 1;
                rowPts[index] = canvas.getHeight() / 2 + 2;  // y坐标偏移+2
                index += 1;

                rowPts[index] = x;  // 记录x坐标
                index += 1;
                rowPts[index] = canvas.getHeight() / 2 + 4;  // y坐标偏移+4
                index += 1;

            } else if (x % widthSmallDiv == 0) {  // 小格交叉处（仅被小格间距整除）
                rowPts[index] = x;  // 记录x坐标
                index += 1;
                rowPts[index] = canvas.getHeight() / 2 - 2;  // y坐标偏移-2
                index += 1;

                rowPts[index] = x;  // 记录x坐标
                index += 1;
                rowPts[index] = canvas.getHeight() / 2 + 2;  // y坐标偏移+2
                index += 1;
            }
            rowPts[index] = x;  // 普通位置，记录x坐标
            index = index + 1;
            rowPts[index] = canvas.getHeight() / 2;  // y坐标为中心线
            index = index + 1;
        }
        canvas.drawPoints(rowPts, paint);  // 批量绘制水平中线上的所有点

        //画坚线
        index = 0;  // 重置索引
        for (int y = 0; y < canvas.getHeight(); y += 2) {  // 每2个像素绘制一个点
            //被50整除画4个点，被5整除画2个点。
            if (y % heightBigDiv == 0 && y % heightSmallDiv == 0) {  // 大格交叉处
                colPts[index] = canvas.getWidth() / 2 - 2;  // x坐标偏移-2
                index = index + 1;
                colPts[index] = y;  // 记录y坐标
                index += 1;

                colPts[index] = canvas.getWidth() / 2 - 4;  // x坐标偏移-4
                index += 1;
                colPts[index] = y;  // 记录y坐标
                index += 1;

                colPts[index] = canvas.getWidth() / 2 + 2;  // x坐标偏移+2
                index += 1;
                colPts[index] = y;  // 记录y坐标
                index += 1;

                colPts[index] = canvas.getWidth() / 2 + 4;  // x坐标偏移+4
                index += 1;
                colPts[index] = y;  // 记录y坐标
                index += 1;

            } else if (y % heightSmallDiv == 0) {  // 小格交叉处
                colPts[index] = canvas.getWidth() / 2 - 2;  // x坐标偏移-2
                index += 1;
                colPts[index] = y;  // 记录y坐标
                index += 1;

                colPts[index] = canvas.getWidth() / 2 + 2;  // x坐标偏移+2
                index += 1;
                colPts[index] = y;  // 记录y坐标
                index += 1;
            }
            colPts[index] = canvas.getWidth() / 2;  // 普通位置，x坐标为中心线
            index = index + 1;
            colPts[index] = y;  // 记录y坐标
            index = index + 1;
        }
        canvas.drawPoints(colPts, paint);  // 批量绘制垂直中线上的所有点

    }

    //十字点
    /**
     * 绘制十字交叉点（大格交叉处的点标记）
     *
     * @param canvas 绘制目标画布
     */
    private void drawCrossPoint(Canvas canvas) {
        float[] pts = new float[canvas.getWidth() / widthBigDiv * canvas.getHeight() / heightBigDiv * 2];  // 计算交叉点数量，每个点2个float
        Arrays.fill(pts,Float.MAX_VALUE);  // 填充无效值
        paint.setColor(GridLine_Bright);  // 设置点颜色为网格线亮度色
        paint.setStrokeWidth(1);  // 设置点宽度

        int index = 0;  // 点数组索引
        for (int y = 0; y < canvas.getHeight(); y += heightBigDiv) {  // 遍历垂直方向大格位置
            for (int x = 0; x < canvas.getWidth(); x += widthBigDiv) {  // 遍历水平方向大格位置
                pts[index] = x;  // 记录交叉点x坐标
                index += 1;
                pts[index] = y;  // 记录交叉点y坐标
                index += 1;
            }
        }
        canvas.drawPoints(pts, paint);  // 批量绘制所有交叉点

    }

    //画所有点
    /**
     * 绘制所有格线交叉点（小格级别，仅绘制大格行/列上的点）
     *
     * @param canvas 绘制目标画布
     */
    private void drawAllPoint(Canvas canvas) {
        int rowPoint = canvas.getWidth() / widthSmallDiv * 2;  // 水平方向小格点数
        int colPoint = canvas.getHeight() / heightSmallDiv * 2;  // 垂直方向小格点数
        float[] pts = new float[rowPoint * colPoint];  // 总点数坐标数组
        Arrays.fill(pts,Float.MAX_VALUE);  // 填充无效值
        paint.setColor(GridLine_Bright);  // 设置点颜色
        paint.setStrokeWidth(1);  // 设置点宽度

        int index = 0;  // 点数组索引
        for (int y = 0; y < canvas.getHeight(); y += heightSmallDiv)  // 遍历垂直方向小格位置
            for (int x = 0; x < canvas.getWidth(); x += widthSmallDiv) {  // 遍历水平方向小格位置
                if (x % widthBigDiv == 0) {  // x处于大格线上
                    pts[index] = x;  // 记录x坐标
                    index += 1;
                    pts[index] = y;  // 记录y坐标
                    index += 1;
                }
                if (y % heightBigDiv == 0) {  // y处于大格线上
                    pts[index] = x;  // 记录x坐标
                    index += 1;
                    pts[index] = y;  // 记录y坐标
                    index += 1;
                }
            }

        canvas.drawPoints(pts, paint);  // 批量绘制所有格线交叉点
    }

    /**
     * 绘制全格线（用实线替代点阵，绘制所有大格的水平和垂直分隔线）
     *
     * @param canvas 绘制目标画布
     */
    private void drawALLLine(Canvas canvas){
        for(int y=heightBigDiv;y<canvas.getHeight();y+=heightBigDiv){  // 遍历垂直方向大格位置
            canvas.drawLine(0,y,canvas.getWidth(),y,paint);  // 绘制水平分隔线
        }
        for(int x=widthBigDiv;x<canvas.getWidth();x+=widthBigDiv){  // 遍历水平方向大格位置
            canvas.drawLine(x,0,x,canvas.getHeight(),paint);  // 绘制垂直分隔线
        }
    }
    //endregion
}
