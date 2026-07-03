package com.micsig.tbook.tbookscope.wavezone.display;  // 示波器波形显示区域的网格管理包

import android.graphics.Bitmap;  // 位图类，用于离屏绘制网格
import android.graphics.Canvas;  // 画布类，用于在Bitmap上绘制图形
import android.graphics.Color;  // 颜色工具类
import android.graphics.DashPathEffect;  // 虚线效果，用于绘制虚线格线
import android.graphics.Paint;  // 画笔类，控制绘制样式
import android.graphics.PorterDuff;  // 混合模式枚举
import android.graphics.PorterDuffXfermode;  // 混合模式转换器
import android.graphics.Rect;  // 矩形区域类（用于文字测量）
import android.graphics.RectF;  // 浮点矩形区域类
import android.util.Log;  // Android日志工具

import com.chillingvan.canvasgl.ICanvasGL;  // OpenGL画布接口
import com.micsig.base.Logger;  // 自定义日志工具
import com.micsig.tbook.scope.ScopeBase;  // 示波器基础配置类
import com.micsig.tbook.tbookscope.BuildConfig;  // 构建配置，用于判断测试版本
import com.micsig.tbook.tbookscope.GlobalVar;  // 全局变量单例
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;  // 工作模式接口

import java.util.Arrays;  // 数组工具类

/**
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │  模块定位：示波器YT模式网格绘制器                                            │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  核心职责：在YT模式（时域模式）和YTZoom模式（时域缩放模式）下绘制示波器网格，  │
 * │           包括十字线、十字交叉点、虚线格线等元素。网格大小根据工作模式和         │
 * │           垂直格数动态调整。                                                │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  架构设计：实现IWorkMode和IGrid接口，采用离屏Bitmap+Canvas方式绘制，         │
 * │           通过ICanvasGL将Bitmap渲染到OpenGL画布上。                           │
 * │           同时作为YT和YTZoom两种模式的网格实现，内部通过mWorkMode区分。        │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  数据流向：工作模式切换/属性变更 → drawGrid() → Bitmap离屏绘制 →             │
 * │           draw(ICanvasGL) → 根据模式选择源/目标矩形 → OpenGL渲染到屏幕        │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  依赖关系：                                                                │
 * │           - IGrid：网格属性与绘制接口                                       │
 * │           - IWorkMode：工作模式切换接口                                     │
 * │           - GlobalVar：获取波形区域宽高像素值和主波形高度                     │
 * │           - ScopeBase：获取垂直格数配置                                     │
 * │           - BuildConfig：判断是否为测试版本                                 │
 * │           - ICanvasGL：OpenGL画布，用于GPU加速渲染                           │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │  使用场景：YT/YTZoom模式下由WaveGridManage调用draw()进行网格渲染，           │
 * │           在模式切换时调用switchWorkMode()调整网格参数，                     │
 * │           在设置面板中调整网格属性/亮度。                                     │
 * └────────────────────────────────────────────────────────────────────────────┘
 *
 * Created by liwb on 2017/5/3.
 */

public class WaveGrid_YT implements IWorkMode, IGrid {  // 实现工作模式接口和网格接口
    private static final String TAG="WaveGrid_YT";  // 日志标签
    //region 属性
    /** 起始亮度 */
    private final int BaseBright=40;  // 网格线最低亮度值，避免完全不可见
    private int GridAttr_Enum = GridAttr_CrossLine | GridAttr_CrossPoint | GridAttr_ALLPoint | GridAttr_Frame;  // 网格属性组合：十字线|十字点|全网点|边框
    private int GridLine_Bright = Color.argb(0x00, 0x80, 0x80, 0x80);  // 网格线颜色，默认透明度为0的灰色（需通过亮度设置后可见）



    /**
     * 刷新网格，重新绘制整个网格位图
     */
    @Override
    public void refresh() {
        drawGrid();  // 调用核心绘制方法重绘网格
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
        return (int)((GridLine_Bright-BaseBright)/180.0f);  // 将RGB分量值转换为0-100的亮度百分比
   }

    /**
     * 设置网格线亮度百分比并重绘
     *
     * @param gridLine_Bright 亮度百分比（0-100）
     */
    @Override
    public void setGridLine_Bright(int gridLine_Bright) {
        int c=(int)(BaseBright+(180*gridLine_Bright*1.0/100));  // 将0-100的百分比映射到亮度范围[BaseBright, BaseBright+180]
        GridLine_Bright = Color.rgb(c, c, c);  // 设置灰度颜色（R=G=B=c）
        drawGrid();  // 亮度变更后重绘
    }
    //endregion

    private Bitmap bmp;  // 离屏位图，网格绘制到此Bitmap上
    private Bitmap bmpOld;  // 旧位图引用，用于OpenGL纹理更新时的对比
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
    private RectF bmpRect;  // 位图的矩形区域，用于绘制时的源/目标矩形

    /**
     *  需求说明1：YT模式下，绘制11格，左右各半格，十字线显示在中间
     *  修改说明1：YT模式下，绘制12格，显示的时候偏移半格，即50像素。正好得达需求
     *
     *  2。YT YTzoom进行分配：150：500
     *
     *  3。XY 的尺寸为，500*500
     */

    /** 添加一格（像素偏移量），0表示不额外添加 */
    private static final int AddOneGrid=0;  // 额外添加的格宽度（像素），当前为0不添加

    /**
     * 构造函数，创建YT模式的网格位图并初始化绘制
     */
    public WaveGrid_YT() {
        //temp 宽高应该在一个静态类中，直接获取到。
        bmp = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_YT)+AddOneGrid,  // 获取YT模式波形区宽度+额外格宽
                GlobalVar.get().getWaveZoneHeight_Pix(IWorkMode.WorkMode_YT), Bitmap.Config.ARGB_8888);  // 获取YT模式波形区高度，ARGB_8888格式
        mCanvas = new Canvas(bmp);  // 创建与Bitmap关联的Canvas
        paint = new Paint();  // 创建画笔

        bmpRect = new RectF(0, 0, bmp.getWidth() + 0, bmp.getHeight() + 0);  // 初始化位图矩形区域
        drawGrid();  // 初始绘制网格

    }

    //region 单例创建
//    private static class WaveGrid_YT_Holder {
//        private static final WaveGrid_YT instance = new WaveGrid_YT();
//    }
//
//    public static final WaveGrid_YT getInstance() {
//        return WaveGrid_YT_Holder.instance;
//    }
    //endregion

    //region IWorkMode接口实现

    //1100*650 ,显示11格，以十字线为中心，左右各半格，实计1200*650画12格，偏移半格显示
    private
    @WorkMode
    int mWorkMode;  // 当前工作模式，用于区分YT和YTZoom的绘制方式
    private int widthBigDiv = 150; //50  // 水平方向大格间距（像素），YT模式默认150
    private int heightBigDiv = 100; //50  // 垂直方向大格间距（像素），YT模式默认100
    private int widthSmallDiv = widthBigDiv/5; // 5为显示5个刻度  // 水平方向小格间距，每大格分5小格
    private int heightSmallDiv = heightBigDiv/5; // 5为显示5个刻度  // 垂直方向小格间距，每大格分5小格

    /**
     * 设置垂直方向每格像素高度，动态调整网格位图大小
     *
     * @param heightDiv 每格的像素高度
     */
    @Override
    public void setHeightDiv(int heightDiv) {

        bmpOld = bmp;  // 保存旧位图引用，用于后续纹理更新对比
        bmp = Bitmap.createScaledBitmap(bmp, bmp.getWidth(), heightDiv * ScopeBase.getVerticalGridCnt(), true);  // 按新高度缩放位图（高度=格高×垂直格数）
        mCanvas.setBitmap(bmp);  // 更新Canvas关联的位图
        bmpRect.union(0, 0, bmp.getWidth(), bmp.getHeight());  // 扩展位图矩形区域到新尺寸
        heightBigDiv = heightDiv;  // 更新垂直大格间距
        heightSmallDiv = heightDiv / 5;  // 更新垂直小格间距
        drawGrid();  // 重新绘制网格
    }

    /**
     * 切换工作模式，根据模式调整网格的水平和垂直格间距参数并重绘
     *
     * @param workMode 工作模式常量，取值为 WorkMode_YT / WorkMode_YTZOOM / WorkMode_XY
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        this.mWorkMode = workMode;  // 保存当前工作模式
        synchronized (bmp) {  // 同步锁保护位图访问
//            int newWidth=GlobalVar.get().getWaveZoneWidth_Pix(workMode);
//            int newHeight=GlobalVar.get().getWaveZoneHeight_Pix(workMode);
//            if (workMode==IWorkMode.WorkMode_YT || workMode==IWorkMode.WorkMode_YTZOOM)
//            {
//                newWidth+=AddOneGrid;
//            }
//            bmp.reconfigure(newWidth,newHeight, Bitmap.Config.ARGB_8888);
//            bmpRect.set(0,0,newWidth,newHeight);
//            //bmp.setHeight(400);
//            mCanvas.setBitmap(bmp);
            switch (workMode) {  // 根据工作模式调整格间距
                case IWorkMode.WorkMode_YT:  // YT模式
                case IWorkMode.WorkMode_YTZOOM:  // YT缩放模式
                    widthBigDiv = 150;  // 水平大格间距150像素
                    heightBigDiv = GlobalVar.get().getMainWave().y / ScopeBase.getVerticalGridCnt();  // 垂直大格间距=主波形高度/垂直格数
                    widthSmallDiv = widthBigDiv/5;  // 水平小格间距
                    heightSmallDiv = heightBigDiv/5;  // 垂直小格间距
                    break;
//                case IWorkMode.WorkMode_YTZOOM:
//                    widthBigDiv = 150;
//                    heightBigDiv = 75;
//                    widthSmallDiv = widthBigDiv/5;
//                    heightSmallDiv = heightBigDiv/5;
//
//                    break;
                case IWorkMode.WorkMode_XY:  // XY模式（虽由YT网格处理但使用XY的格间距）
                    widthBigDiv = 50;  // 水平大格间距50像素
                    heightBigDiv = 50;  // 垂直大格间距50像素
                    widthSmallDiv = 10;  // 水平小格间距10像素
                    heightSmallDiv = 10;  // 垂直小格间距10像素
                    break;
            }
            drawGrid();  // 模式变更后重绘网格
            isChanageBitmap = true;  // 标记位图已变更
            onRefresh();  // 通知OpenGL刷新纹理
        }
    }

    //endregion


    /**
     * 将网格位图绘制到标准Canvas上（非OpenGL方式）
     *
     * @param canvas 标准Canvas画布
     */
    public void drawGrid(Canvas canvas) {
        synchronized (bmp) {  // 同步锁保护位图访问
            canvas.drawBitmap(bmp, 0, 0, null);  // 将位图绘制到Canvas的(0,0)位置
        }
    }

    /**
     * 将网格位图绘制到OpenGL画布上，根据当前工作模式选择不同的源/目标矩形
     * - YTZoom模式：将位图压缩到3/4高度显示（zoom效果）
     * - YT模式：1:1绘制
     * - 其他模式：按位图矩形绘制
     *
     * @param canvas OpenGL画布
     */
    @Override
    public void draw(ICanvasGL canvas) {
        synchronized (bmp) {  // 同步锁保护位图访问
            canvasGL = canvas;  // 保存OpenGL画布引用
            //canvas.drawBitmap(bmp,0,0,isChanageBitmap);
            int bottom = mCanvas.getHeight();  // 获取网格位图的高度
            if(canvasGL.getHeight() != bottom) {  // 如果OpenGL画布高度与网格位图高度不一致
                canvasGL.setSize(canvas.getWidth(), bottom);  // 调整OpenGL画布大小
            }
            if (isChanageBitmap){  // 位图内容变更时
                canvas.invalidateTextureContent(bmp,bmpOld);  // 通知OpenGL纹理已失效，传入新旧位图对比
                bmpOld = null;  // 释放旧位图引用
//                Log.d("20250901","canvasGL:" + canvas + "bmp:" + bmp);
            }
            switch (this.mWorkMode){  // 根据当前工作模式选择绘制策略
                case IWorkMode.WorkMode_YTZOOM:{  // YT缩放模式
                    RectF src=new RectF(0,0,1800,bottom);  // 源矩形：位图左半部分（0~1800像素宽）
                    RectF des = new RectF(0, 0, 1800, bottom * 3 >> 2);  // 目标矩形：高度压缩为3/4（右移2位=除4，再乘3）
                    canvas.drawBitmap(bmp, src, des);  // 将源区域绘制到压缩后的目标区域

                }break;
                case IWorkMode.WorkMode_YT:{  // YT标准模式
                    RectF src=new RectF(0,0,1800,bottom);  // 源矩形：位图左半部分
                    RectF des=new RectF(0,0,1800,bottom);  // 目标矩形：1:1映射
                    canvas.drawBitmap(bmp, src, des);  // 将源区域绘制到目标区域

                }break;
                default: {  // 其他模式（如XY）
                    canvas.drawBitmap(bmp, bmpRect, bmpRect);  // 按位图矩形1:1绘制

                }break;
            }

            isChanageBitmap = false;  // 重置变更标志
        }
    }


    /**
     * 在网格上绘制测试版本水印文字（仅测试版和调试版生效）
     */
    private void drawTesting(){

        if(BuildConfig.APP_TESTING || BuildConfig.DEBUG) {  // 判断是否为测试版或调试版
            String str = "内部测试版";  // 水印文字
            if(BuildConfig.APP_DEVELOP){  // 判断是否为开发分支
                str +="(开发分支)";  // 追加开发分支标识
            }else{
                str +="(主分支)";  // 追加主分支标识
            }
            Paint p = new Paint();  // 创建画笔
            p.setColor(Color.RED);  // 设置水印颜色为红色
            p.setAntiAlias(true);  // 启用抗锯齿
            p.setAlpha(200);  // 设置半透明
            p.setTextSize(36);  // 设置字号

            Rect rect = new Rect();  // 创建矩形用于测量文字
            p.getTextBounds(str, 0, str.length(), rect);  // 测量文字边界
            int w = rect.width();  // 获取文字宽度
            int h = rect.height();  // 获取文字高度
            mCanvas.drawText(str, (bmp.getWidth() - w) >> 1, h, p);  // 在位图水平居中位置绘制水印文字
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

//            drawTesting();  // 绘制测试水印（已注释）


            if ((GridAttr_Enum & GridAttr_CrossPoint) == GridAttr_CrossPoint) {  // 检查是否启用十字交叉点
                drawCrossPoint(mCanvas);  // 绘制十字交叉点
            }
            if ((GridAttr_Enum & GridAttr_ALLPoint) == GridAttr_ALLPoint) {  // 检查是否启用全格线
//                drawAllPoint(mCanvas);
                drawAllPoint_line(mCanvas);  // 绘制虚线格线（替代点阵方式）
            }
            if ((GridAttr_Enum & GridAttr_Frame) == GridAttr_Frame) {  // 检查是否启用边框
//                drawFrame(mCanvas);  // 边框绘制（已注释，YT模式不绘制边框）
            }
            if ((GridAttr_Enum & GridAttr_CrossLine) == GridAttr_CrossLine) {  // 检查是否启用十字线
                drawCrossLine(mCanvas);  // 绘制十字线
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
        canvas.drawLine(0, canvas.getHeight() - 1, canvas.getWidth(), canvas.getHeight() - 1, paint);  // 绘制底边框线（-1避免越界）
        canvas.drawLine(canvas.getWidth() - 1, 0, canvas.getWidth() - 1, canvas.getHeight(), paint);  // 绘制右边框线（-1避免越界）

    }

    /**
     * 根据网格线亮度计算十字线颜色（十字线比网格线更亮，RGB各加35）
     *
     * @param gridLine_Bright 网格线颜色值
     * @return 十字线颜色值
     */
    public static int getCrossLine(int gridLine_Bright){
        int r = Color.red(gridLine_Bright) + 35;  // 红色分量加35
        int g = Color.green(gridLine_Bright) + 35;  // 绿色分量加35
        int b = Color.blue(gridLine_Bright) + 35;  // 蓝色分量加35
//        Logger.d("limh", "getCrossLine r= " + r + ", g= " + g + ", b= " + b);
        return Color.rgb(r, g, b);  // 组合为RGB颜色值返回
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
        int rowpoint1 = 18 * 4 * 2; //18格 1条4条线，一条线2个点  // 大格交叉处的额外点数
        int rowpoint2 = 18 * 4;   //18格 1格4个点  // 小格交叉处的额外点数

        int colPoint = canvas.getHeight();  // 垂直中线所需的基准点数
        int colPoint1 = 14 * 4 * 2;  // 大格交叉处的额外点数
        int colPoint2 = 14 * 4;  // 小格交叉处的额外点数

        float[] rowPts = new float[(rowPoint + rowpoint1 + rowpoint2) * 2];  // 水平中线点坐标数组，每个点2个float(x,y)
        float[] colPts = new float[(colPoint + colPoint1 + colPoint2) * 2];  // 垂直中线点坐标数组
        Arrays.fill(rowPts,Float.MAX_VALUE);  // 填充默认值，Float.MAX_VALUE表示无效点（不会被绘制）
        Arrays.fill(colPts,Float.MAX_VALUE);  // 填充默认值
        paint.setColor(getCrossLine(GridLine_Bright));  // 设置十字线颜色（比网格线更亮）
        paint.setStrokeWidth(1);  // 设置点宽度为1
        //画横线
        int index = 0;  // 点数组索引
        for (int x = 2; x < canvas.getWidth(); x += 2) {  // 每2个像素绘制一个点，从2开始
            //被50整除画4个点，被5整除画2个点。
            if (x % widthBigDiv == 0 && x % widthSmallDiv == 0) {  // 大格交叉处（同时被大小格间距整除）
                rowPts[index] = x;  // 记录x坐标
                index = index + 1;
                rowPts[index] = (canvas.getHeight() >> 1) - 2;  // y坐标偏移-2（右移1位=除2，取中心）
                index += 1;

                rowPts[index] = x;  // 记录x坐标
                index += 1;
                rowPts[index] = (canvas.getHeight() >> 1) - 4;  // y坐标偏移-4
                index += 1;

                rowPts[index] = x;  // 记录x坐标
                index += 1;
                rowPts[index] = (canvas.getHeight() >> 1) + 2;  // y坐标偏移+2
                index += 1;

                rowPts[index] = x;  // 记录x坐标
                index += 1;
                rowPts[index] = (canvas.getHeight() >> 1) + 4;  // y坐标偏移+4
                index += 1;

            } else if (x % widthSmallDiv == 0) {  // 小格交叉处（仅被小格间距整除）
                rowPts[index] = x;  // 记录x坐标
                index += 1;
                rowPts[index] = (canvas.getHeight() >> 1) - 2;  // y坐标偏移-2
                index += 1;

                rowPts[index] = x;  // 记录x坐标
                index += 1;
                rowPts[index] = (canvas.getHeight() >> 1) + 2;  // y坐标偏移+2
                index += 1;
            }
            rowPts[index] = x;  // 普通位置，记录x坐标
            index = index + 1;
            rowPts[index] = canvas.getHeight() >> 1;  // y坐标为中心线
            index = index + 1;
        }
        canvas.drawPoints(rowPts, paint);  // 批量绘制水平中线上的所有点

        //画坚线
        index = 0;  // 重置索引
        for (int y = 2; y < canvas.getHeight(); y += 2) {  // 每2个像素绘制一个点，从2开始
            //被50整除画4个点，被5整除画2个点。
            if (y % heightBigDiv == 0 && y % heightSmallDiv == 0) {  // 大格交叉处
                colPts[index] = (canvas.getWidth() >> 1) - 2;  // x坐标偏移-2
                index = index + 1;
                colPts[index] = y;  // 记录y坐标
                index += 1;

                colPts[index] = (canvas.getWidth() >> 1) - 4;  // x坐标偏移-4
                index += 1;
                colPts[index] = y;  // 记录y坐标
                index += 1;

                colPts[index] = (canvas.getWidth() >> 1) + 2;  // x坐标偏移+2
                index += 1;
                colPts[index] = y;  // 记录y坐标
                index += 1;

                colPts[index] = (canvas.getWidth() >> 1) + 4;  // x坐标偏移+4
                index += 1;
                colPts[index] = y;  // 记录y坐标
                index += 1;

            } else if (y % heightSmallDiv == 0) {  // 小格交叉处
                colPts[index] = (canvas.getWidth() >> 1) - 2;  // x坐标偏移-2
                index += 1;
                colPts[index] = y;  // 记录y坐标
                index += 1;

                colPts[index] = (canvas.getWidth() >> 1) + 2;  // x坐标偏移+2
                index += 1;
                colPts[index] = y;  // 记录y坐标
                index += 1;
            }
            colPts[index] = canvas.getWidth() >> 1;  // 普通位置，x坐标为中心线
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
        float[] pts = new float[( canvas.getWidth() / widthBigDiv * canvas.getHeight() / heightBigDiv * 2)];  // 计算交叉点数量，每个点2个float
        Arrays.fill(pts,Float.MAX_VALUE);  // 填充无效值
        paint.setColor(GridLine_Bright);  // 设置点颜色为网格线亮度色
        paint.setStrokeWidth(1);  // 设置点宽度

        int index = 0;  // 点数组索引
        for (int y = heightBigDiv; y < canvas.getHeight(); y += heightBigDiv) {  // 遍历垂直方向大格位置（从第一格开始，不含边缘）
            for (int x = widthBigDiv; x < canvas.getWidth(); x += widthBigDiv) {  // 遍历水平方向大格位置
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
     * 绘制所有格线交叉点（小格级别，仅绘制大格行或大格列上的点）
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
        for (int y = heightSmallDiv; y < canvas.getHeight(); y += heightSmallDiv) {  // 遍历垂直方向小格位置
            for (int x = widthSmallDiv; x < canvas.getWidth(); x += widthSmallDiv) {  // 遍历水平方向小格位置
                if (x % widthBigDiv == 0) {  // x处于大格线上
                    pts[index] = x;  // 记录x坐标
                    index += 1;
                    pts[index] = y;  // 记录y坐标
                    index += 1;
                } else if (y % heightBigDiv == 0) {  // y处于大格线上
                    pts[index] = x;  // 记录x坐标
                    index += 1;
                    pts[index] = y;  // 记录y坐标
                    index += 1;
                }
            }
        }
        canvas.drawPoints(pts, paint);  // 批量绘制所有格线交叉点

    }

    /**
     * 绘制虚线格线（用虚线替代点阵，绘制所有大格的水平和垂直分隔线）
     *
     * @param canvas 绘制目标画布
     */
    private void drawAllPoint_line(Canvas canvas){
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{1, 1, 1, 1}, 0);  // 创建虚线效果：1像素绘制、1像素间隔、1像素绘制、1像素间隔
        paint.setPathEffect(dashPathEffect);  // 应用虚线效果到画笔
        for (int y = heightBigDiv; y < canvas.getHeight(); y += heightBigDiv) {  // 遍历垂直方向大格位置
            canvas.drawLine(0, y, canvas.getWidth(), y, paint);  // 绘制水平虚线分隔线
        }
        for (int x = widthBigDiv; x < canvas.getWidth(); x += widthBigDiv) {  // 遍历水平方向大格位置
            canvas.drawLine(x, 0, x, canvas.getHeight(), paint);  // 绘制垂直虚线分隔线
        }
        paint.setPathEffect(null);  // 清除画笔的路径效果，避免影响后续绘制
    }
    //endregion
}
