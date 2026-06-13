package com.micsig.tbook.scope.surface;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glcanvas.GLCanvas;
import com.chillingvan.canvasgl.glcanvas.RawTexture;
import com.chillingvan.canvasgl.textureFilter.BasicTextureFilter;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                LayerTexture - 图层纹理类                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Surface模块的图层纹理管理类，封装OpenGL纹理和SurfaceTexture，               ║
 * ║   为示波器多图层显示提供纹理容器和渲染控制。                                   ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理RawTexture纹理对象                                                  ║
 * ║   2. 管理SurfaceTexture外部纹理输入                                          ║
 * ║   3. 支持图层位置、大小、偏移设置                                             ║
 * ║   4. 支持Z序（图层优先级）管理                                                ║
 * ║   5. 支持可见性控制                                                          ║
 * ║   6. 支持纹理滤镜                                                            ║
 * ║                                                                              ║
 * ║ 【图层渲染架构】                                                             ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                         LayerTexture                                │   ║
 * ║   │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐            │   ║
 * ║   │  │ RawTexture  │    │SurfaceTexture│    │BasicTextureFilter│        │   ║
 * ║   │  │ (纹理对象)  │    │ (外部纹理)  │    │ (纹理滤镜)  │            │   ║
 * ║   │  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘            │   ║
 * ║   │         │                  │                  │                   │   ║
 * ║   │         └──────────────────┼──────────────────┘                   │   ║
 * ║   │                            ▼                                      │   ║
 * ║   │                    ┌─────────────┐                               │   ║
 * ║   │                    │  onDraw()   │ → 绘制到画布                   │   ║
 * ║   │                    └─────────────┘                               │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【多图层叠加示意】                                                           ║
 * ║   ┌──────────────────────────────────────┐                                 ║
 * ║   │  Layer 3 (zorder=3) - 顶层           │                                 ║
 * ║   ├──────────────────────────────────────┤                                 ║
 * ║   │  Layer 2 (zorder=2)                  │                                 ║
 * ║   ├──────────────────────────────────────┤                                 ║
 * ║   │  Layer 1 (zorder=1)                  │                                 ║
 * ║   ├──────────────────────────────────────┤                                 ║
 * ║   │  Layer 0 (zorder=0) - 底层           │                                 ║
 * ║   └──────────────────────────────────────┘                                 ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 示波器多通道波形显示（每个通道一个图层）                                 ║
 * ║   2. 叠加显示（网格、测量值、菜单等）                                         ║
 * ║   3. 动态通道管理                                                            ║
 * ║   4. 图像处理管线                                                            ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   使用synchronized关键字保护可见性和偏移量的访问。                            ║
 * ║   使用volatile修饰共享状态变量。                                             ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - RawTexture: OpenGL纹理对象，存储图像数据                                 ║
 * ║   - SurfaceTexture: Android外部纹理，用于相机/视频输入                       ║
 * ║   - BasicTextureFilter: 纹理滤镜基类，用于图像处理                           ║
 * ║   - SurfaceNative: JNI原生渲染接口                                          ║
 * ║   - GLCanvas: OpenGL画布，用于纹理绘制                                       ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018-9-11                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class LayerTexture {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 回调接口定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 帧可用回调接口
     * 当SurfaceTexture有新帧可用时触发此回调
     */
    public interface OnFrameAvailableListener {
        /**
         * 帧可用回调方法
         *
         * @param layerTexture 触发回调的图层纹理对象
         */
        void onFrameAvailable(LayerTexture layerTexture);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 回调与位置
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 帧可用回调监听器
     * 当SurfaceTexture有新帧时通知外部
     */
    private OnFrameAvailableListener frameAvailableListener;

    /**
     * 图层左边距（像素）
     * 相对于父容器的X坐标起点
     */
    protected int left = 0;

    /**
     * 图层顶边距（像素）
     * 相对于父容器的Y坐标起点
     */
    protected int top = 0;

    /**
     * 图层宽度（像素）
     */
    protected int width = 0;

    /**
     * 图层高度（像素）
     */
    protected int height = 0;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 图层属性
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 图层类型
     * 用于标识图层的用途（如波形层、网格层、菜单层等）
     */
    private int layerType = 0;

    /**
     * OpenGL纹理对象
     * 存储图层的图像数据
     */
    private RawTexture texture = null;

    /**
     * Android SurfaceTexture
     * 用于接收外部纹理输入（如相机、视频解码器）
     */
    private SurfaceTexture surfaceTexture = null;

    /**
     * 纹理滤镜
     * 用于对纹理进行图像处理（如灰度转彩色、余辉效果等）
     * 默认使用BasicTextureFilter（无处理）
     */
    protected BasicTextureFilter basicTextureFilter = new BasicTextureFilter();

    /**
     * 可见性标志
     * true: 图层可见，参与绘制
     * false: 图层隐藏，不参与绘制
     * 使用volatile保证多线程可见性
     */
    protected volatile boolean bVisiable = false;

    /**
     * 有效标志
     * true: 纹理数据有效，可以绘制
     * false: 纹理数据无效，跳过绘制
     * 使用volatile保证多线程可见性
     */
    protected volatile boolean bVaild = false;

    /**
     * Z序（图层优先级）
     * 值越大，图层越靠上
     * 用于多图层叠加时的排序
     * 使用volatile保证多线程可见性
     */
    private volatile int zorder = 0;

    /**
     * 绘制计数
     * 记录已绘制的帧数，用于帧同步
     * 使用volatile保证多线程可见性
     */
    protected volatile int drawNum = 0;

    /**
     * 原生渲染接口
     * 用于JNI层的数据传递
     */
    private SurfaceNative surfaceNative;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 偏移
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * X方向偏移量（像素）
     * 用于波形平移功能
     */
    private int offsetX = 0;

    /**
     * Y方向偏移量（像素）
     * 用于波形平移功能
     */
    private int offsetY = 0;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 属性访问方法 - 回调与可见性
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置帧可用回调监听器
     *
     * @param frameAvailableListener 回调监听器，null表示移除监听
     */
    public void setFrameAvailableListener(OnFrameAvailableListener frameAvailableListener) {
        this.frameAvailableListener = frameAvailableListener;                       // 设置回调监听器
    }

    /**
     * 检查图层是否可见
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @return true: 可见
     *         false: 隐藏
     */
    public synchronized boolean isVisiable() {
        return bVisiable;                                                           // 返回可见性状态
    }

    /**
     * 设置图层可见性
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @param bVisiable 可见性状态
     *                  true: 可见
     *                  false: 隐藏
     */
    public synchronized void setVisiable(boolean bVisiable) {
        this.bVisiable = bVisiable;                                                 // 设置可见性状态
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 属性访问方法 - 其他属性
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取原生渲染接口
     *
     * @return 原生渲染接口实例，可能为null
     */
    public SurfaceNative getSurfaceNative() {
        return surfaceNative;                                                       // 返回原生渲染接口
    }

    /**
     * 获取图层类型
     *
     * @return 图层类型值
     */
    public int getLayerType() {
        return layerType;                                                           // 返回图层类型
    }

    /**
     * 获取Z序（图层优先级）
     *
     * @return Z序值，值越大图层越靠上
     */
    public int getZorder() {
        return zorder;                                                              // 返回Z序值
    }

    /**
     * 设置Z序（图层优先级）
     *
     * @param zorder Z序值，值越大图层越靠上
     */
    public void setZorder(int zorder) {
        this.zorder = zorder;                                                       // 设置Z序值
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造图层纹理实例（动态通道）
     * 创建一个有效的图层，但不创建纹理对象
     * 用于动态通道管理场景
     *
     * @param layerType 图层类型，用于标识图层用途
     */
    public LayerTexture(int layerType){
        bVaild = true;                                                              // 设置为有效状态
        this.layerType = layerType;                                                 // 设置图层类型
    }

    /**
     * 构造图层纹理实例（指定尺寸）
     * 创建指定尺寸的纹理对象
     *
     * @param width 纹理宽度（像素）
     * @param height 纹理高度（像素）
     * @param layerType 图层类型，用于标识图层用途
     */
    public LayerTexture(int width, int height, int layerType){
        this.layerType = layerType;                                                 // 设置图层类型
        this.width = width;                                                         // 设置宽度
        this.height = height;                                                       // 设置高度
        // 创建RawTexture对象
        // 参数1: 宽度
        // 参数2: 高度
        // 参数3: 是否使用mipmap
        // 参数4: 纹理目标类型（GL_TEXTURE_EXTERNAL_OES用于外部纹理）
        texture = new RawTexture(width, height, false, GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 属性访问方法 - 偏移
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取X方向偏移量
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @return X方向偏移量（像素）
     */
    public synchronized int getOffsetX(){
        return offsetX;                                                             // 返回X偏移量
    }

    /**
     * 获取Y方向偏移量
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @return Y方向偏移量（像素）
     */
    public synchronized int getOffsetY(){
        return offsetY;                                                             // 返回Y偏移量
    }

    /**
     * 设置偏移量
     * 用于实现波形平移功能
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @param x X方向偏移量（像素）
     * @param y Y方向偏移量（像素）
     */
    public synchronized void setOffset(int x, int y){
        this.offsetX = x;                                                           // 设置X偏移量
        this.offsetY = y;                                                           // 设置Y偏移量
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 属性访问方法 - 位置与尺寸
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取左边距
     *
     * @return 左边距（像素）
     */
    public int getLeft() {
        return left;                                                                // 返回左边距
    }

    /**
     * 设置左边距
     *
     * @param left 左边距（像素）
     */
    public void setLeft(int left) {
        this.left = left;                                                           // 设置左边距
    }

    /**
     * 获取顶边距
     *
     * @return 顶边距（像素）
     */
    public int getTop() {
        return top;                                                                 // 返回顶边距
    }

    /**
     * 设置顶边距
     *
     * @param top 顶边距（像素）
     */
    public void setTop(int top) {
        this.top = top;                                                             // 设置顶边距
    }

    /**
     * 获取宽度
     *
     * @return 宽度（像素）
     */
    public int getWidth() {
        return width;                                                               // 返回宽度
    }

    /**
     * 设置宽度
     *
     * @param width 宽度（像素）
     */
    public void setWidth(int width) {
        this.width = width;                                                         // 设置宽度
    }

    /**
     * 获取高度
     *
     * @return 高度（像素）
     */
    public int getHeight() {
        return height;                                                              // 返回高度
    }

    /**
     * 设置高度
     *
     * @param height 高度（像素）
     */
    public void setHeight(int height) {
        this.height = height;                                                       // 设置高度
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 属性访问方法 - 纹理
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置纹理对象
     *
     * @param texture RawTexture纹理对象
     */
    public void setTexture(RawTexture texture){
        this.texture = texture;                                                     // 设置纹理对象
    }

    /**
     * 获取纹理对象
     *
     * @return RawTexture纹理对象，可能为null
     */
    public RawTexture getTexture(){
        return this.texture;                                                        // 返回纹理对象
    }

    /**
     * 获取SurfaceTexture
     *
     * @return SurfaceTexture对象，可能为null
     */
    public SurfaceTexture getSurfaceTexture(){
        return this.surfaceTexture;                                                 // 返回SurfaceTexture
    }

    /**
     * 设置SurfaceTexture
     *
     * @param surfaceTexture SurfaceTexture对象
     */
    public void setSurfaceTexture(SurfaceTexture surfaceTexture){
        this.surfaceTexture = surfaceTexture;                                       // 设置SurfaceTexture
    }

    /**
     * 设置纹理滤镜
     *
     * @param basicTextureFilter 纹理滤镜对象
     */
    public void setTextureFilter(BasicTextureFilter basicTextureFilter) {
        this.basicTextureFilter = basicTextureFilter;                               // 设置纹理滤镜
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 纹理管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 修改RawTexture高度
     * 调整纹理尺寸（当前实现为空）
     *
     * @param height 新的高度（像素）
     */
    public void changeRawTextureHeight(int height) {
        //if (texture != null && texture.getHeight() != height) texture.setSize(width, S);
        // [已注释] 原始实现：调整纹理尺寸
    }

    /**
     * 准备纹理资源
     * 初始化纹理对象和SurfaceTexture，设置帧可用回调
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>检查并准备RawTexture</li>
     *   <li>创建SurfaceTexture</li>
     *   <li>创建SurfaceNative</li>
     *   <li>设置帧可用回调</li>
     * </ol>
     *
     * @param canvas OpenGL画布，用于纹理准备
     */
    public void prepare(GLCanvas canvas){
        if (!texture.isLoaded()) {                                                   // 检查纹理是否已加载
            texture.prepare(canvas);                                                 // 准备纹理（分配OpenGL资源）
        }
        surfaceTexture = new SurfaceTexture(texture.getId());                       // 创建SurfaceTexture，关联纹理ID

        surfaceNative = new SurfaceNative(surfaceTexture);                          // 创建原生渲染接口
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {           // 帧可用回调
                bVaild = true;                                                       // 设置有效标志
                if(frameAvailableListener != null){                                  // 检查是否有外部监听器
                    setDrawNum();                                                    // 更新绘制计数
                    setOffset(0, 0);                                                 // 重置偏移量
                    frameAvailableListener.onFrameAvailable(LayerTexture.this);      // 通知外部监听器
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 绘制计数方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 增加绘制计数
     * 使用位运算防止溢出
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     */
    private synchronized void setDrawNum(){
        drawNum = (drawNum + 1) & Integer.MAX_VALUE;                                // 计数加1，与MAX_VALUE位与防止溢出
    }

    /**
     * 获取绘制计数
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @return 绘制计数，取值范围：0 ~ Integer.MAX_VALUE
     */
    public synchronized int getDrawNum(){
        return drawNum;                                                             // 返回绘制计数
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 资源释放方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 结束图层
     * 释放纹理资源
     */
    public void end(){
        if (texture != null) {                                                      // 检查纹理是否存在
            texture.recycle();                                                      // 回收纹理资源
            texture = null;                                                         // 清空引用
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 绘制与更新方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 绘制图层
     * 将纹理绘制到OpenGL画布上
     *
     * <p><b>绘制区域：</b></p>
     * <pre>
     *   左上角: (left, top)
     *   右下角: (left + width, top + height)
     * </pre>
     *
     * @param canvas OpenGL画布
     */
    public void onDraw(ICanvasGL canvas){
        if(bVaild && texture != null) {                                             // 检查有效性和纹理存在
            // 绘制SurfaceTexture到指定区域
            // 参数: 纹理, SurfaceTexture, 左, 上, 右, 下, 滤镜
            canvas.drawSurfaceTexture(texture, surfaceTexture, left, top, left + width, top + height, basicTextureFilter);
        }
    }

    /**
     * 更新纹理图像
     * 将SurfaceTexture的最新帧更新到OpenGL纹理
     * 必须在OpenGL线程中调用
     */
    public void updateTexImage(){
        if(bVaild && surfaceTexture != null){                                       // 检查有效性和SurfaceTexture存在
            surfaceTexture.updateTexImage();                                        // 更新纹理图像
        }
    }

    /**
     * 清除图层
     * 更新纹理并标记为无效
     */
    public void clear(){
        updateTexImage();                                                           // 更新纹理图像
        bVaild = false;                                                             // 标记为无效
    }

}
