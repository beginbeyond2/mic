package com.micsig.tbook.tbookscope.wavezone.display; // 示波器波形显示区显示模块包

import android.graphics.Bitmap; // 导入位图类，用于创建和操作位图图像
import android.graphics.Canvas; // 导入画布类，用于在位图上执行绘图操作
import android.graphics.Color; // 导入颜色类，提供颜色常量和ARGB构造方法
import android.graphics.Paint; // 导入画笔类，控制绘图样式和混合模式
import android.graphics.PorterDuff; // 导入PorterDuff混合模式枚举
import android.graphics.PorterDuffXfermode; // 导入PorterDuff Xfermode，用于控制像素混合
import android.graphics.Rect; // 导入矩形类，定义绘图区域
import android.graphics.drawable.BitmapDrawable; // 导入位图Drawable，用于从资源获取位图

import com.chillingvan.canvasgl.ICanvasGL; // 导入OpenGL画布接口，用于GPU加速绘图
import com.chillingvan.canvasgl.util.Loggers; // 导入日志工具类
import com.micsig.tbook.scope.ScopeBase; // 导入示波器基类，提供屏幕宽度等基础参数
import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量管理器
import com.micsig.tbook.tbookscope.util.App; // 导入应用上下文工具类
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 导入工作模式管理器

/**
 * Created by liwb on 2017/6/19.
 * zoom区的遮罩层+波形
 */

/*
 * +===========================================================================================+
 * |                               WaveMaskLayer_YTZoom 类说明                                  |
 * +-------------------------------------------------------------------------------------------+
 * | 模块定位 : YT模式Zoom缩放窗口的遮罩层绘制与状态管理                                             |
 * | 核心职责 : 1.绘制半透明遮罩（中间透明窗口 + 两侧暗色遮罩）                                       |
 * |           2.绘制触发位置指示图标                                                              |
 * |           3.管理遮罩层中心X坐标(layerX)、窗口宽度(layerWidth)、触发位置(layerTimebaseX)           |
 * | 架构设计 : 单例模式（静态内部类持有者），使用Bitmap离屏绘制 + PorterDuff混合模式实现透明窗口效果     |
 * | 数据流向 : 外部设置layerX/layerWidth/layerTimebaseX → draw()绘制到bitmapLayer →                 |
 * |           draw(ICanvasGL)提交到OpenGL画布                                                    |
 * | 依赖关系 : ScopeBase（屏幕宽度）、GlobalVar（波形区像素宽度）、WorkModeManage（工作模式）、          |
 * |            ICanvasGL（OpenGL画布）、App（资源获取）                                             |
 * | 使用场景 : Zoom模式下在主窗口上叠加半透明遮罩，高亮显示Zoom窗口对应的波形区域                          |
 * +===========================================================================================+
 */
public class WaveMaskLayer_YTZoom {

    //region  单例
    private static class WaveMaskLayer_YTZoom_Holder { // 静态内部类持有者，实现懒加载单例
        private static final WaveMaskLayer_YTZoom instance = new WaveMaskLayer_YTZoom(); // 单例实例，类加载时创建
    }

    /**
     * 获取WaveMaskLayer_YTZoom单例实例
     *
     * @return WaveMaskLayer_YTZoom的唯一实例
     */
    public static final WaveMaskLayer_YTZoom getInstance() {
        return WaveMaskLayer_YTZoom_Holder.instance; // 返回静态内部类持有的单例实例
    }
    //endregion

    //region 事件接口
    /** Zoom遮罩层事件监听接口，定义遮罩层位置变化的回调 */
      public interface OnZoomEventListener {
        /**
         * 遮罩层X坐标发生变化时回调
         * @param offset 遮罩层坐标X移动量
         * */
        public void onLayerXChangeEvent(int offset);

        /**
         * 遮罩层宽度发生变化时回调
         *
         * @param layerWidth 遮罩层宽度
         */
      //  public void onLayerWidthChangeEvent(int layerWidth);

        /**
         * 遮罩层时基坐标X发生变化时回调
         *
         * @param layerTimebaseX 遮罩层时基坐标X
         */
      //  public void onLayerTimeBaseXChangeEvent(int layerTimebaseX);
    }
    //endregion

    //region 属性  layerX zoom窗口中心像素位置  layerWidth zoom窗口的宽  layerTimebaseX 主窗口触发位置
    int layerX = 800, layerWidth = 200 ,layerTimebaseX=800; // 遮罩层中心X坐标、窗口宽度、触发位置X坐标，默认值800/200/800
    private OnZoomEventListener onZoomEventListenerListener =null; // Zoom事件监听器引用，默认为空

    /**
     * 获取Zoom事件监听器
     *
     * @return 当前注册的Zoom事件监听器
     */
    public OnZoomEventListener getOnZoomEventListenerListener() {
        return onZoomEventListenerListener; // 返回当前事件监听器
    }

    /**
     * 设置Zoom事件监听器
     *
     * @param onZoomEventListenerListener 要注册的Zoom事件监听器
     */
    public void setOnZoomEventListenerListener(OnZoomEventListener onZoomEventListenerListener) {
        this.onZoomEventListenerListener = onZoomEventListenerListener; // 保存事件监听器引用
    }

    /**
     * 设置遮罩层中心X坐标，带边界限制（屏幕宽度±10像素）
     *
     * @param layerX 遮罩层中心X坐标值
     */
    public void setLayerX(long layerX) {
        if(layerX < -10) // 小于左边界-10
            this.layerX = -10; // 钳位到左边界
        else if(layerX > ScopeBase.getWidth()+10) // 大于右边界（屏幕宽度+10）
            this.layerX = ScopeBase.getWidth()+10; // 钳位到右边界
        else
            this.layerX = (int)layerX; // 在合法范围内，直接赋值
    }

    /**
     * 移动遮罩层中心X坐标，并触发重绘和事件通知
     *
     * @param offset X坐标偏移量（正数右移，负数左移）
     */
    public void layerX_move(int offset){
        this.layerX = layerX + offset; // 按偏移量移动遮罩层中心位置
        draw(); // 重新绘制遮罩层
        if (onZoomEventListenerListener !=null){ // 检查事件监听器是否已注册
            onZoomEventListenerListener.onLayerXChangeEvent(offset); // 通知监听器遮罩层X坐标变化
        }
    }

    /**
     * 设置遮罩层窗口宽度，最小值限制为2像素
     *
     * @param layerWidth 遮罩层窗口宽度值
     */
    public void setLayerWidth(int layerWidth) {
        if(layerWidth < 2) // 宽度小于最小值2
            this.layerWidth = 2; // 钳位到最小值
        else
            this.layerWidth = layerWidth; // 在合法范围内，直接赋值
    }

    /**
     * 设置触发位置X坐标，带边界限制（屏幕宽度±10像素）
     *
     * @param layerTimebaseX 触发位置X坐标值
     */
    public void setLayerTimebaseX(long layerTimebaseX) {
        if(layerTimebaseX < -10) // 小于左边界-10
            this.layerTimebaseX = -10; // 钳位到左边界
        else if(layerTimebaseX > ScopeBase.getWidth()+10) // 大于右边界（屏幕宽度+10）
            this.layerTimebaseX = ScopeBase.getWidth()+10; // 钳位到右边界
        else
            this.layerTimebaseX = (int)layerTimebaseX; // 在合法范围内，直接赋值
    }

//endregion


    private Bitmap bitmapLayer,oldBitmapLayer; // bitmapLayer:当前遮罩层位图; oldBitmapLayer:上一帧遮罩层位图（用于纹理更新）
    private Paint paint; // 画笔对象，用于设置混合模式和颜色
    private Canvas mCanvas; // 离屏画布，用于在bitmapLayer上绘制
    private boolean isChanageBitmap = false; // 遮罩层位图内容是否已变更标志
    private ICanvasGL canvasGL; // OpenGL画布引用，用于刷新纹理
    private int zoomH = 1040 / 4; // Zoom窗口高度（像素），默认1040/4=260

    /**
     * 刷新OpenGL纹理：当canvasGL不为空时通知其刷新纹理内容
     */
    public void onRefresh(){
        if(canvasGL != null){ // 检查OpenGL画布引用是否有效
            canvasGL.onRefreshTexture(); // 通知OpenGL画布刷新纹理
        }
    }
//    private Bitmap bitmapWave;
    private boolean isChanageWave = false; // 波形位图内容是否已变更标志（当前未使用）
    private Bitmap[] resBmp; // 触发位置图标资源数组：[0]中间图标、[1]左侧图标、[2]右侧图标

    /**
     * 构造方法：初始化触发图标资源、画笔，创建遮罩层位图并执行首次绘制
     */
    public WaveMaskLayer_YTZoom() {
        resBmp = new Bitmap[3]; // 创建3个元素的位图数组
        resBmp[0] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger)).getBitmap(); // 加载触发位置中间图标
        resBmp[1] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger_l)).getBitmap(); // 加载触发位置左侧图标（触发点超出左边界时使用）
        resBmp[2] = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger_r)).getBitmap(); // 加载触发位置右侧图标（触发点超出右边界时使用）

        paint = new Paint(); // 创建画笔对象
        reInitBmp(); // 初始化/重新创建遮罩层位图和离屏画布
        draw(); // 执行首次遮罩层绘制
    }

    /**
     * 绘制遮罩层到离屏位图：
     * 1.用PorterDuff.CLEAR清除旧内容
     * 2.用半透明颜色填充整个位图作为遮罩底色
     * 3.用PorterDuff.SRC_OUT在Zoom窗口区域挖出透明孔洞
     * 4.绘制触发位置指示图标
     * 5.标记位图已变更并刷新纹理
     */
    public void draw() {
        synchronized (bitmapLayer) { // 对遮罩层位图加锁，防止并发访问
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); // 设置画笔为清除模式，将擦除已有像素
            mCanvas.drawPaint(paint); // 用清除模式填充整个画布，擦除所有内容
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC)); // 设置画笔为源模式，直接绘制新像素
            paint.setColor(Color.argb(90, 60, 128, 128)); // 设置半透明暗青色（ARGB：alpha=90, R=60, G=128, B=128）
            mCanvas.drawRect(new Rect(0, 0, bitmapLayer.getWidth(), bitmapLayer.getHeight()), paint); // 用半透明暗青色填充整个遮罩层作为底色
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)); // 设置画笔为SRC_OUT模式，在已有内容上挖出透明区域
            paint.setColor(Color.argb(0, 0, 0, 0)); // 设置完全透明颜色（alpha=0），配合SRC_OUT实现"挖孔"效果

            mCanvas.drawRect(new Rect(layerX-layerWidth/2, 0, layerX + layerWidth/2, bitmapLayer.getHeight()), paint); // 在Zoom窗口区域绘制透明矩形，形成"窗口"效果

            if(layerTimebaseX < 0) // 触发位置在屏幕左侧之外
                mCanvas.drawBitmap(resBmp[1],0,0,null); // 在左边界绘制左侧触发图标
            else if(layerTimebaseX > ScopeBase.getWidth()) // 触发位置在屏幕右侧之外
                mCanvas.drawBitmap(resBmp[2],ScopeBase.getWidth()-resBmp[2].getWidth(),0,null); // 在右边界绘制右侧触发图标
            else // 触发位置在屏幕范围内
                mCanvas.drawBitmap(resBmp[0],layerTimebaseX-resBmp[0].getWidth()/2,0,null); // 在触发位置居中绘制中间触发图标
            isChanageBitmap = true; // 标记遮罩层位图内容已变更
            onRefresh(); // 通知刷新OpenGL纹理
        }
    }


//    public void setBitmapWave(int[] points) {
//        synchronized (bitmapWave) {
//            bitmapWave.setPixels(points, 0, bitmapWave.getWidth(), 0, 0, bitmapWave.getWidth(), bitmapWave.getHeight());
//            isChanageWave = true;
//        }
//    }

    /**
     * 将遮罩层位图绘制到OpenGL画布上，如位图有变更则刷新纹理
     *
     * @param canvas OpenGL画布接口，用于GPU加速绘图
     */
    public void draw(ICanvasGL canvas) {
//        synchronized (bitmapWave) {
//            //canvas.drawBitmap(bitmapWave, 0, 0, isChanageWave);
//            isChanageWave = false;
//        }
        synchronized (bitmapLayer) { // 对遮罩层位图加锁，防止并发访问
            canvasGL = canvas; // 保存OpenGL画布引用
            if(isChanageBitmap){ // 检查遮罩层位图是否已变更
                canvas.invalidateTextureContent(bitmapLayer,oldBitmapLayer); // 通知画布纹理内容已失效，需更新
                oldBitmapLayer = null; // 清空旧位图引用，允许GC回收
            }
            Loggers.d("limh", "zoom maskLayer height= " + bitmapLayer.getHeight()); // 输出调试日志：遮罩层位图高度
            canvas.drawBitmap(bitmapLayer, 0, 0); // 将遮罩层位图绘制到OpenGL画布的(0,0)位置
            isChanageBitmap = false; // 重置位图变更标志
        }


    }

    /**
     * 修改Zoom窗口高度，重新初始化位图并重绘
     *
     * @param zoomH 新的Zoom窗口高度（像素）
     */
    public void changeZoomH(int zoomH) {
        this.zoomH = zoomH; // 更新Zoom窗口高度
        reInitBmp(); // 根据新高度重新创建位图和画布
        draw(); // 重新绘制遮罩层
    }

    /**
     * 重新初始化遮罩层位图和离屏画布
     * 根据当前工作模式获取波形区宽度，创建指定尺寸的ARGB_8888位图
     */
    private void reInitBmp() {
        oldBitmapLayer = bitmapLayer; // 保存旧位图引用，用于纹理更新对比
        bitmapLayer = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), zoomH, Bitmap.Config.ARGB_8888); // 根据工作模式获取波形区宽度，创建新位图
//        bitmapWave = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), zoomH, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bitmapLayer); // 创建离屏画布，绑定到新创建的遮罩层位图
    }


}
