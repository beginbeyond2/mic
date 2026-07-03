package com.micsig.tbook.tbookscope.wavezone.trigger; // 触发线模块包

import android.graphics.Bitmap; // 导入Bitmap类，用于图片资源
import android.graphics.Canvas; // 导入Canvas类，用于绘制
import android.graphics.Color; // 导入Color类，用于颜色常量
import android.graphics.Paint; // 导入Paint类，用于画笔设置
import android.graphics.PorterDuff; // 导入PorterDuff混合模式
import android.graphics.PorterDuffXfermode; // 导入PorterDuffXfermode，用于图像混合
import android.graphics.Rect; // 导入Rect类，用于矩形区域

import com.chillingvan.canvasgl.ICanvasGL; // 导入GLCanvas接口，用于OpenGL绘制
import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量类
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 导入工作模式管理类
import com.micsig.tbook.ui.wavezone.IWave; // 导入波形线接口
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道号常量类

/**
 * Created by liwb on 2017/11/6.
 * 垂直通道基类
 */

/*
 * +=============================================================================+
 * |                       VerticalChannelBase                                   |
 * +=============================================================================+
 * | 模块定位   : 示波器波形显示区域 - 垂直通道线基类（水平方向位置指示器）        |
 * | 核心职责   : 提供垂直通道线的通用属性（位置、选中、可见性）和绘制逻辑         |
 * | 架构设计   : 实现 IWave 接口，作为 TriggerTimebase 等垂直线的父类           |
 * |            : 使用 Bitmap+Canvas 双缓冲绘制，支持选中/未选中态图标切换        |
 * | 数据流向   : 外部设置X位置 → draw()重绘Bitmap → GLCanvas渲染显示            |
 * | 依赖关系   : IWave(接口), GlobalVar, WorkModeManage, TChan, ICanvasGL       |
 * | 使用场景   : 作为触发时刻线(TriggerTimebase)的父类，提供通用位置管理和       |
 * |            : 绘制能力；也可被其他垂直方向指示线继承                          |
 * +=============================================================================+
 */
public class VerticalChannelBase implements IWave {
    private static final String TAG = "VerticalChannelBase"; // 日志标签
//    private static final Bitmap resBmp = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger)).getBitmap();
//    private static final Bitmap resBmpLeft = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger_l)).getBitmap();
//    private static final Bitmap resBmpRight = ((BitmapDrawable) App.get().getResources().getDrawable(com.micsig.tbook.ui.R.drawable.time_trigger_r)).getBitmap();

    private Bitmap[] resBmp; // 资源图片数组（6张：选中3张+未选中3张）
    private final int resBmpCenter = 0; // 选中态-中间图标索引
    private final int resBmpLeft = 1; // 选中态-左侧图标索引
    private final int resBmpRight = 2; // 选中态-右侧图标索引
    private final int resBmpNoCenter = 3; // 未选中态-中间图标索引
    private final int resBmpNoLeft = 4; // 未选中态-左侧图标索引
    private final int resBmpNoRight = 5; // 未选中态-右侧图标索引

    private IWave.OnMovingWaveEvent onMovingWaveEvent; // 波形移动事件回调
    private IWave.OnSelectChangeEvent onSelectChangeEvent; // 选中状态变化事件回调

    //region 属性
    private boolean selected; // 是否被选中
    private boolean visible = true; // 是否可见
    long x; // X位置（像素）
    double y; // Y位置（像素）
    private int LineNameID = TChan.TriggerTime; // 线名称ID

    /**
     * 默认构造函数
     */
    public VerticalChannelBase() {
    }

    /**
     * 设置线名称ID
     * @param lineNameID 线名称ID
     */
    public void setLineNameID(int lineNameID) {
        this.LineNameID = lineNameID; // 赋值线名称ID
    }

    /**
     * 获取线名称ID
     * @return 线名称ID
     */
    public int getLineNameID() {
        return LineNameID; // 返回线名称ID
    }

    /**
     * 获取X位置
     * @return X位置（像素）
     */
    public long getX() {
        return x; // 返回X位置
    }

    /**
     * 通过偏移量设置X位置
     * @param offsetX 偏移量（正数左移，负数右移）
     */
    public void setOffsetX(int offsetX) {
        setX(this.x - offsetX); // 用当前位置减去偏移量
    }

    /**
     * 设置X位置，触发重绘和移动事件
     * @param x 目标X位置（像素）
     */
    public void setX(long x) {
        this.x = x; // 更新X位置
        draw(); // 重绘
        if (onMovingWaveEvent != null) { // 注册了移动事件回调
            onMovingWaveEvent.OnMovingWave(this, x, y, false, false); // 触发移动事件（非事件总线来源）
        }
    }

    /**
     * 从事件总线设置X位置
     * 与setX的区别在于OnMovingWaveEvent的isFromEventBus参数为true
     * @param x 目标X位置（像素）
     */
    public void setXFromEventBus(long x) {
        this.x = x; // 更新X位置
        draw(); // 重绘
        if (onMovingWaveEvent != null) { // 注册了移动事件回调
            onMovingWaveEvent.OnMovingWave(this, x, y, false, true); // 触发移动事件（事件总线来源）
        }
    }

    /**
     * 刷新显示（仅重绘，不改变位置）
     */
    public void update() {
        draw(); // 重绘
    }

    /**
     * 获取Y位置
     * @return Y位置
     */
    public double getY() {
        return y; // 返回Y位置
    }

    /**
     * 设置Y位置
     * @param y 目标Y位置
     */
    public void setY(double y) {
        this.y = y; // 更新Y位置
    }

    /**
     * 设置颜色（空实现，子类可覆写）
     * @param color 颜色值
     */
    @Override
    public void setColor(int color) {

    }

    /**
     * 获取颜色（默认白色）
     * @return 颜色值
     */
    @Override
    public int getColor() {
        return Color.WHITE; // 返回白色
    }

    /**
     * 设置可见性
     * @param visible 是否可见
     */
    @Override
    public void setVisible(boolean visible) {
        this.visible = visible; // 更新可见性
    }

    /**
     * 获取可见性
     * @return 是否可见
     */
    @Override
    public boolean getVisible() {
        return visible; // 返回可见性
    }

    /**
     * 设置选中状态，选中时触发选中变化事件
     * @param selected 是否选中
     */
    @Override
    public void setSelected(boolean selected) {
        this.selected = selected; // 更新选中状态
        draw(); // 重绘
        if (selected) { // 被选中
            if (onSelectChangeEvent != null) onSelectChangeEvent.OnSelectChange(this, true); // 触发选中事件
        }
    }

    /**
     * 获取选中状态
     * @return 是否选中
     */
    @Override
    public boolean isSelected() {
        return this.selected; // 返回选中状态
    }

    /**
     * 设置波形移动事件回调
     * @param onMovingWaveEvent 移动事件回调
     */
    public void setOnMovingWaveEvent(IWave.OnMovingWaveEvent onMovingWaveEvent) {
        this.onMovingWaveEvent = onMovingWaveEvent; // 赋值回调
    }

    /**
     * 设置选中变化事件回调
     * @param onSelectChangeEvent 选中变化回调
     */
    public void setOnSelectChangeEvent(IWave.OnSelectChangeEvent onSelectChangeEvent) {
        this.onSelectChangeEvent = onSelectChangeEvent; // 赋值回调
    }
    //endregion

    private Bitmap bmp; // 绘制缓冲Bitmap
    private Canvas mCanvas; // 绘制Canvas
    private Paint paint; // 画笔
    protected boolean isChanageBitmap = false; // Bitmap内容是否已变更（需要刷新纹理）
    private ICanvasGL canvasGL; // GLCanvas引用（用于纹理刷新）

    /**
     * 通知GLCanvas刷新纹理
     */
    public void onRefresh(){
        if(canvasGL != null){ // GLCanvas引用有效
            canvasGL.onRefreshTexture(); // 刷新纹理
        }
    }

    /**
     * 初始化资源图片、创建缓冲Bitmap和画笔，设置初始位置
     * @param resBmp 资源图片数组（6张图标）
     */
    public void init(Bitmap[] resBmp) {
        this.resBmp = resBmp; // 保存资源图片引用
        bmp = Bitmap.createBitmap(resBmp[resBmpLeft].getWidth() + 10, resBmp[resBmpCenter].getHeight(), Bitmap.Config.ARGB_8888); // 创建缓冲Bitmap（宽度加10像素余量）
        mCanvas = new Canvas(bmp); // 创建Canvas
        paint = new Paint(); // 创建画笔
        x = GlobalVar.get().getMainWave().x / 2; // X初始位置为屏幕宽度一半
        y = 0; // Y初始位置为0
        draw(); // 执行初始绘制
    }


    /**
     * 移动线条（空实现，子类可覆写）
     * @param offsetX X偏移量
     * @param offsetY Y偏移量
     */
    @Override
    public void moveLine(int offsetX, int offsetY) {

    }

    /**
     * 绘制到标准Canvas（已弃用，保留接口兼容）
     * @param canvas Canvas对象
     */
    public void draw(Canvas canvas) {
//        synchronized (bmp) {
//            if (this.x <= resBmp[resBmpLeft].getWidth()) {
//                canvas.drawBitmap(bmp, 0, 0, null);
//            } else if (this.x >= GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[resBmpRight].getWidth()) {
//                canvas.drawBitmap(bmp, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[resBmpRight].getWidth(), 0, null);
//            } else {
//                canvas.drawBitmap(bmp, x - resBmp[resBmpCenter].getWidth() / 2, 0, null);
//            }
//
//        }
    }

    /**
     * 绘制到GLCanvas
     * 根据X位置选择左/中/右图标绘制，支持纹理刷新
     * @param canvas ICanvasGL对象
     */
    public void draw(ICanvasGL canvas) {
        if (!visible) return; // 不可见时直接返回
        synchronized (bmp) { // 同步锁，防止并发修改
            canvasGL = canvas; // 保存GLCanvas引用
            if (isChanageBitmap) canvas.invalidateTextureContent(bmp,null); // Bitmap内容已变更，刷新纹理
            if (this.x < 0) { // X位置在屏幕左侧外
                canvas.drawBitmap(bmp, 0, 0); // 绘制到最左端
            } else if (this.x > GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode())) { // X位置在屏幕右侧外
                canvas.drawBitmap(bmp, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()) - resBmp[resBmpRight].getWidth(),
                        0); // 绘制到最右端
            } else { // X位置在屏幕内
                canvas.drawBitmap(bmp, (int) x - resBmp[resBmpCenter].getWidth() / 2, 0); // 居中绘制
            }
            isChanageBitmap = false; // 重置变更标志
        }
    }

    /**
     * 重置初始化矩形区域（空实现，子类可覆写）
     */
    @Override
    public void resultIniRect() {

    }

    /**
     * 设置线名称ID（IWave接口方法）
     * @param nameId 线名称ID
     */
    @Override
    public void setLineNameId(int nameId) {
        this.LineNameID = nameId; // 更新线名称ID
    }


    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR); // 清除混合模式
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC); // 源覆盖混合模式

    /**
     * 内部绘制方法
     * 清空缓冲Bitmap后根据选中状态和X位置绘制对应图标
     */
    private void draw() {
        synchronized (bmp) { // 同步锁
            paint.setXfermode(clearMode); // 设置清除模式
            mCanvas.drawPaint(paint); // 清空画布
            paint.setXfermode(srcMode); // 设置源覆盖模式
            if (this.selected == true) { // 选中状态
                if (this.x < 0) { // X位置在屏幕左侧外
                    mCanvas.drawBitmap(resBmp[resBmpLeft], 0, 0, paint); // 绘制选中态左侧图标
                } else if (this.x > GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode())) { // X位置在屏幕右侧外
                    mCanvas.drawBitmap(resBmp[resBmpRight], 0, 0, paint); // 绘制选中态右侧图标
                } else { // X位置在屏幕内
                    mCanvas.drawBitmap(resBmp[resBmpCenter], 0, 0, paint); // 绘制选中态中间图标
                }
            } else { // 未选中状态
                if (this.x < 0) { // X位置在屏幕左侧外
                    mCanvas.drawBitmap(resBmp[resBmpNoLeft], 0, 0, paint); // 绘制未选中态左侧图标
                } else if (this.x > GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode())) { // X位置在屏幕右侧外
                    mCanvas.drawBitmap(resBmp[resBmpNoRight], 0, 0, paint); // 绘制未选中态右侧图标
                } else { // X位置在屏幕内
                    mCanvas.drawBitmap(resBmp[resBmpNoCenter], 0, 0, paint); // 绘制未选中态中间图标
                }
            }
            isChanageBitmap = true; // 标记Bitmap已变更
            onRefresh(); // 通知GLCanvas刷新纹理
        }

    }

    /**
     * 通过像素增量移动X位置
     * @param px 移动像素量（正数右移，负数左移）
     */
    @Override
    public void movePix(double px) {
        setX(Math.round(x + px)); // 当前位置加上偏移量后设置
    }

    private Rect rect = new Rect(); // 点击检测矩形缓存

    /**
     * 判断指定坐标是否在垂直通道线的点击区域内
     * @param x 点击X坐标
     * @param y 点击Y坐标
     * @return 是否在点击区域内
     */
    public boolean selectCursor(int x, int y) {
        boolean selected; // 是否在点击区域内
        //rect.set(0, this.y - resBmp[resBmpCenter].getHeight() / 2, bmp.getWidth(), bmp.getHeight() + this.y - resBmp[resBmpCenter].getHeight() / 2);
        rect.set(((int) this.x) - resBmp[resBmpLeft].getWidth() / 2, 0, bmp.getWidth() + ((int) this.x) - resBmp[resBmpLeft].getWidth() / 2, bmp.getHeight()); // 设置检测矩形区域（以X位置为中心，全高度）
        selected = rect.contains(x, y); // 判断坐标是否在矩形内
        return selected; // 返回判断结果
    }

}
