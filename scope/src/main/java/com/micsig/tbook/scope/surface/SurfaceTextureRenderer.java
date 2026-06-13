package com.micsig.tbook.scope.surface;


import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.chillingvan.canvasgl.CanvasGL;
import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.ITextureRefresh;
import com.chillingvan.canvasgl.glcanvas.BasicTexture;
import com.chillingvan.canvasgl.glcanvas.RawTexture;
import com.chillingvan.canvasgl.glview.texture.GLViewRenderer;
import com.chillingvan.canvasgl.glview.texture.gles.EglContextWrapper;
import com.chillingvan.canvasgl.glview.texture.gles.GLThread;
import com.chillingvan.canvasgl.util.Loggers;
import com.micsig.tbook.scope.ScopeBase;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                SurfaceTextureRenderer - Surface纹理渲染器抽象类                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Surface模块的OpenGL纹理渲染器抽象基类，实现GLViewRenderer和ITextureRefresh接口，║
 * ║   负责管理GLThread渲染线程、SurfaceTexture生命周期和OpenGL绘制流程。          ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理GLThread OpenGL渲染线程                                             ║
 * ║   2. 创建和管理SurfaceTexture数组                                            ║
 * ║   3. 创建和管理RawTexture数组                                                ║
 * ║   4. 提供渲染生命周期回调                                                     ║
 * ║   5. 支持多SurfaceTexture输出                                                ║
 * ║                                                                              ║
 * ║ 【实现接口】                                                                 ║
 * ║   - GLViewRenderer: OpenGL视图渲染器接口                                     ║
 * ║   - ITextureRefresh: 纹理刷新接口                                            ║
 * ║                                                                              ║
 * ║ 【继承体系】                                                                 ║
 * ║                          ┌───────────────────────┐                           ║
 * ║                          │SurfaceTextureRenderer │ ← 本类：抽象渲染器         ║
 * ║                          └───────────┬───────────┘                           ║
 * ║                                      │                                       ║
 * ║                                      ▼                                       ║
 * ║                          ┌───────────────────────┐                           ║
 * ║                          │   SurfacePreview      │ ← 具体实现类              ║
 * ║                          └───────────────────────┘                           ║
 * ║                                                                              ║
 * ║ 【渲染流程】                                                                 ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │  start() → onSurfaceCreated() → onSurfaceChanged() → onDrawFrame() │   ║
 * ║   │                                                                     │   ║
 * ║   │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐            │   ║
 * ║   │  │ 创建Canvas  │───▶│ 创建纹理    │───▶│ 绘制帧      │            │   ║
 * ║   │  │ CanvasGL    │    │ RawTexture  │    │ onGLDraw    │            │   ║
 * ║   │  └─────────────┘    └─────────────┘    └─────────────┘            │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【SurfaceTexture创建流程】                                                   ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ RawTexture  │───▶│ prepare()   │───▶│SurfaceTexture│                   ║
 * ║   │ (纹理对象)  │    │ (分配显存)  │    │ (外部纹理)  │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                              │                              ║
 * ║                                              ▼                              ║
 * ║                                      ┌─────────────┐                       ║
 * ║                                      │ 回调通知    │ → onSurfaceTextureSet  ║
 * ║                                      └─────────────┘                       ║
 * ║                                                                              ║
 * ║ 【像素格式】                                                                 ║
 * ║   ┌────────────────────────────────────────────────────────────────────┐    ║
 * ║   │  格式常量              │  值  │  说明                               │    ║
 * ║   ├────────────────────────────────────────────────────────────────────┤    ║
 * ║   │  FORMAT_RGBA_8888     │  1   │  RGBA顺序，每像素4字节              │    ║
 * ║   │  FORMAT_BGRA_8888     │  5   │  BGRA顺序，每像素4字节（默认）      │    ║
 * ║   └────────────────────────────────────────────────────────────────────┘    ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 示波器波形渲染                                                          ║
 * ║   2. 多通道纹理输出                                                          ║
 * ║   3. 离屏渲染                                                                ║
 * ║   4. 实时视频处理                                                            ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   OpenGL操作在GLThread中执行，与UI线程隔离。                                  ║
 * ║   使用Handler将回调切换到UI线程。                                            ║
 * ║   使用synchronized保护共享资源。                                             ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - GLThread: OpenGL渲染线程                                                 ║
 * ║   - CanvasGL: OpenGL画布封装                                                 ║
 * ║   - RawTexture: 原始纹理对象                                                 ║
 * ║   - SurfaceTexture: Android外部纹理                                         ║
 * ║   - EglContextWrapper: EGL上下文包装器                                       ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018-4-2                                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public abstract class SurfaceTextureRenderer implements GLViewRenderer, ITextureRefresh {

    // ═══════════════════════════════════════════════════════════════════════════════
    // ITextureRefresh接口实现
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 纹理刷新回调
     * 当纹理需要刷新时调用，请求重新渲染
     */
    @Override
    public void onRefresh() {
        this.requestRender();                                                      // 请求重新渲染
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 日志标签
     * 用于在Logcat中过滤和识别本类的日志输出
     */
    private static final String TAG = "SurfaceTextureRenderer";

    /**
     * 像素格式常量：RGBA_8888
     * RGBA顺序，每像素4字节（红、绿、蓝、透明度）
     */
    public static final int FORMAT_RGBA_8888 = 1;

    /**
     * 像素格式常量：BGRA_8888
     * BGRA顺序，每像素4字节（蓝、绿、红、透明度）
     * 这是默认格式，与大多数硬件编码器兼容
     */
    public static final int FORMAT_BGRA_8888 = 5;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 核心成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * OpenGL渲染线程
     * 负责执行所有OpenGL ES操作
     * 与UI线程隔离，保证渲染性能
     */
    protected final GLThread mGLThread;

    /**
     * 渲染宽度（像素）
     */
    private int width;

    /**
     * 渲染高度（像素）
     */
    private int height;

    /**
     * 像素格式
     * 取值：FORMAT_RGBA_8888(1) 或 FORMAT_BGRA_8888(5)
     */
    private int format = FORMAT_BGRA_8888;

    /**
     * OpenGL画布封装
     * 提供高级绘图API
     */
    protected ICanvasGL mCanvas;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 外部共享纹理
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 外部共享纹理
     * 用于与其他OpenGL上下文共享纹理
     */
    private BasicTexture outsideSharedTexture;

    /**
     * 外部共享SurfaceTexture
     * 用于接收外部纹理数据
     */
    private SurfaceTexture outsideSharedSurfaceTexture;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 回调接口定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * SurfaceTexture设置回调接口
     * 当SurfaceTexture创建完成时通知外部
     */
    public interface OnSurfaceTextureSet {
        /**
         * SurfaceTexture设置回调
         *
         * @param idx SurfaceTexture索引
         * @param surfaceTexture 创建的SurfaceTexture
         * @param surfaceTextureRelatedTexture 关联的RawTexture
         */
        void onSet(int idx, SurfaceTexture surfaceTexture, RawTexture surfaceTextureRelatedTexture);
    }

    /**
     * SurfaceTexture设置回调监听器
     */
    private OnSurfaceTextureSet onSurfaceTextureSet;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 产出的纹理数组
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 产出的SurfaceTexture数组
     * 每个元素对应一个输出纹理
     */
    protected SurfaceTexture []producedSurfaceTexture;

    /**
     * 产出的RawTexture数组
     * 与producedSurfaceTexture一一对应
     */
    protected RawTexture []producedRawTexture;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 其他成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Android Handler
     * 用于将回调切换到UI线程
     */
    private Handler handler;

    /**
     * 启动标志
     * true: 渲染器已启动
     * false: 渲染器未启动或已停止
     */
    private boolean isStart;

    /**
     * 产出纹理目标类型
     * 默认值：GLES11Ext.GL_TEXTURE_EXTERNAL_OES（外部纹理）
     * 可选值：GLES20.GL_TEXTURE_2D（2D纹理）
     */
    private int producedTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

    /**
     * 背景颜色
     * 默认值：Color.TRANSPARENT（透明）
     */
    private int backgroundColor = Color.TRANSPARENT;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造SurfaceTexture渲染器（指定格式）
     * 使用默认尺寸和EGL上下文
     *
     * @param format 像素格式
     *               FORMAT_RGBA_8888(1): RGBA顺序
     *               FORMAT_BGRA_8888(5): BGRA顺序
     * @param surface Surface对象（Surface或SurfaceTexture）
     */
    public SurfaceTextureRenderer(int format, Object surface) {
        this(0, 0, EglContextWrapper.EGL_NO_CONTEXT_WRAPPER, format, surface, 1);   // 调用完整构造方法
    }

    /**
     * 构造SurfaceTexture渲染器（指定尺寸和格式）
     * 使用默认EGL上下文
     *
     * @param width 渲染宽度（像素）
     * @param height 渲染高度（像素）
     * @param format 像素格式
     * @param surface Surface对象
     * @param nums SurfaceTexture数量
     */
    public SurfaceTextureRenderer(int width, int height, int format, Object surface, int nums) {
        this(width, height, EglContextWrapper.EGL_NO_CONTEXT_WRAPPER, format, surface, nums); // 调用完整构造方法
    }

    /**
     * 构造SurfaceTexture渲染器（完整参数）
     * 创建GLThread并初始化所有成员变量
     *
     * <p><b>GLThread配置：</b></p>
     * <ul>
     *   <li>渲染模式：RENDERMODE_WHEN_DIRTY（按需渲染）</li>
     *   <li>EGL配置：RGBA8888 + 16位深度缓冲 + 2位模板缓冲</li>
     *   <li>共享EGL上下文：支持与其他OpenGL上下文共享资源</li>
     * </ul>
     *
     * @param width 渲染宽度（像素）
     * @param height 渲染高度（像素）
     * @param sharedEglContext 共享的EGL上下文包装器
     * @param format 像素格式
     * @param surface Surface对象
     * @param nums SurfaceTexture数量
     */
    public SurfaceTextureRenderer(int width, int height, EglContextWrapper sharedEglContext, int format, Object surface, int nums) {
        this.width = width;                                                         // 设置宽度
        this.height = height;                                                       // 设置高度
        this.format = format;                                                       // 设置像素格式

        producedSurfaceTexture = new SurfaceTexture[nums];                          // 创建SurfaceTexture数组
        producedRawTexture = new RawTexture[nums];                                  // 创建RawTexture数组

        // 创建GLThread渲染线程
        mGLThread = new GLThread.Builder()
                .setRenderMode(getRenderMode())                                     // 设置渲染模式（按需渲染）
                .setSharedEglContext(sharedEglContext)                             // 设置共享EGL上下文
                .setEGLConfigChooser(8, 8, 8, 8, 16, 2)                            // 设置EGL配置（RGBA8888 + 深度16 + 模板2）
                .setSurface(surface)                                                // 设置Surface
                .setRenderer(this)                                                  // 设置渲染器回调
                .createGLThread();                                                  // 创建GLThread实例
        handler = new Handler();                                                    // 创建Handler（用于UI线程回调）
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 属性访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取EGL上下文包装器
     *
     * @return EGL上下文包装器
     */
    public EglContextWrapper getEglContext(){
        return mGLThread.getEglContext();                                           // 返回GLThread的EGL上下文
    }

    /**
     * 获取渲染宽度
     *
     * @return 宽度（像素）
     */
    public int getWidth() {
        return width;                                                               // 返回宽度
    }

    /**
     * 获取渲染高度
     *
     * @return 高度（像素）
     */
    public int getHeight() {
        return height;                                                              // 返回高度
    }

    /**
     * 获取像素格式
     *
     * @return 像素格式值
     */
    public int getFormat() {
        return format;                                                              // 返回像素格式
    }

    /**
     * 设置产出纹理目标类型
     * 必须在start()之前调用
     *
     * @param producedTextureTarget 纹理目标类型
     *                              GLES20.GL_TEXTURE_2D: 2D纹理
     *                              GLES11Ext.GL_TEXTURE_EXTERNAL_OES: 外部纹理（默认）
     */
    public void setProducedTextureTarget(int producedTextureTarget) {
        this.producedTextureTarget = producedTextureTarget;                         // 设置纹理目标类型
    }

    /**
     * 设置GL上下文创建监听器
     * 必须在start()之前调用
     *
     * @param onCreateGLContextListener 上下文创建监听器
     */
    public void setOnCreateGLContextListener(GLThread.OnCreateGLContextListener onCreateGLContextListener) {
        mGLThread.setOnCreateGLContextListener(onCreateGLContextListener);          // 设置监听器
    }

    /**
     * 设置SurfaceTexture创建回调
     * 必须在start()之前调用
     *
     * @param onSurfaceTextureSet 回调监听器
     */
    public void setOnSurfaceTextureSet(OnSurfaceTextureSet onSurfaceTextureSet) {
        this.onSurfaceTextureSet = onSurfaceTextureSet;                             // 设置回调监听器
    }

    /**
     * 设置外部共享纹理
     * 用于与其他OpenGL上下文共享纹理数据
     *
     * @param outsideTexture 外部纹理对象
     * @param outsideSurfaceTexture 外部SurfaceTexture（可为null）
     */
    public void setSharedTexture(BasicTexture outsideTexture, @Nullable SurfaceTexture outsideSurfaceTexture) {
        this.outsideSharedTexture = outsideTexture;                                 // 设置外部纹理
        this.outsideSharedSurfaceTexture = outsideSurfaceTexture;                   // 设置外部SurfaceTexture
    }

    /**
     * 设置渲染尺寸
     *
     * @param width 新的宽度（像素）
     * @param height 新的高度（像素）
     */
    public void setSize(int width, int height) {
        this.width = width;                                                         // 更新宽度
        this.height = height;                                                       // 更新高度
        if (isStart) {                                                              // 如果已启动
            mGLThread.onWindowResize(width, height);                                // 通知GLThread尺寸变化
        }
    }

    /**
     * 获取背景颜色
     *
     * @return 背景颜色值
     */
    protected int getBackgroundColor(){
        return this.backgroundColor;                                                // 返回背景颜色
    }

    /**
     * 设置背景颜色
     *
     * @param backgroundColor 背景颜色值（如Color.TRANSPARENT）
     */
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;                                     // 设置背景颜色
        requestRender();                                                            // 请求重新渲染
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 生命周期方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 启动渲染器
     * 初始化GLThread并创建Surface
     *
     * <p><b>启动流程：</b></p>
     * <ol>
     *   <li>启动GLThread线程</li>
     *   <li>通知Surface创建</li>
     *   <li>设置窗口尺寸</li>
     *   <li>等待首帧渲染完成</li>
     * </ol>
     */
    public void start() {
        mGLThread.start();                                                          // 启动GLThread线程
        mGLThread.surfaceCreated();                                                 // 通知Surface创建
        mGLThread.onWindowResize(width, height);                                    // 设置窗口尺寸
        mGLThread.requestRenderAndWait();                                           // 请求渲染并等待完成
        isStart = true;                                                             // 设置启动标志
    }

    /**
     * 恢复渲染器
     * 在Activity.onResume时调用
     */
    public void onResume() {
        if(mGLThread != null) {                                                     // 检查GLThread是否存在
            mGLThread.onResume();                                                   // 恢复GLThread
        }
    }

    /**
     * 暂停渲染器
     * 在Activity.onPause时调用
     */
    public void onPause() {
        if(mGLThread != null) {                                                     // 检查GLThread是否存在
            mGLThread.onPause();                                                    // 暂停GLThread
        }
    }

    /**
     * 结束渲染器
     * 释放所有资源
     *
     * <p><b>释放流程：</b></p>
     * <ol>
     *   <li>请求GLThread退出并等待</li>
     *   <li>回收所有RawTexture</li>
     *   <li>释放所有SurfaceTexture</li>
     * </ol>
     */
    public void end() {
        if (mGLThread != null) {                                                    // 检查GLThread是否存在
            mGLThread.requestExitAndWait();                                         // 请求退出并等待
        }

        // 回收所有RawTexture
        for(int i = 0; i < producedRawTexture.length; i++){                         // 遍历RawTexture数组
            if(producedRawTexture[i] != null){                                      // 检查纹理是否存在
                producedRawTexture[i].recycle();                                    // 回收纹理资源
                producedRawTexture[i] = null;                                       // 清空引用
            }
        }

        // 释放所有SurfaceTexture
        for(int i = 0; i < producedSurfaceTexture.length; i++){                     // 遍历SurfaceTexture数组
            if(producedSurfaceTexture[i] != null){                                  // 检查SurfaceTexture是否存在
                producedSurfaceTexture[i].release();                                // 释放SurfaceTexture
                producedSurfaceTexture[i] = null;                                   // 清空引用
            }
        }
    }

    /**
     * 对象终结方法
     * 在垃圾回收时释放资源
     *
     * @throws Throwable 可能抛出的异常
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            end();                                                                  // 释放资源
        } finally {
            super.finalize();                                                       // 调用父类finalize
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GLViewRenderer接口实现
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Surface创建回调
     * 在OpenGL上下文创建后调用
     */
    @Override
    public void onSurfaceCreated() {
        Loggers.d("OffScreenCanvas", "onSurfaceCreated: ");                         // 打印调试日志
        mCanvas = new CanvasGL(this);                                               // 创建CanvasGL实例
    }

    /**
     * Surface尺寸变化回调
     * 在Surface尺寸改变时调用，创建或更新纹理
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>更新画布尺寸</li>
     *   <li>创建RawTexture（如果不存在）</li>
     *   <li>创建SurfaceTexture</li>
     *   <li>通过Handler回调通知外部</li>
     * </ol>
     *
     * @param width 新的宽度（像素）
     * @param height 新的高度（像素）
     */
    @Override
    public void onSurfaceChanged(int width, int height) {
        //Loggers.d("OffScreenCanvas", "onSurfaceChanged: ");
        Log.d(TAG, "onSurfaceChanged() called with: width = [" + width + "], height = [" + height + "]"); // 打印调试日志

        mCanvas.setSize(width, height);                                             // 更新画布尺寸
        
        // 遍历所有纹理，创建或更新
        for(int i = 0; i < producedRawTexture.length; i++){                         // 遍历RawTexture数组
              if (producedRawTexture[i] == null) {                                  // 纹理不存在，需要创建
                    // 创建RawTexture
                    // 参数: 宽度, 高度, 是否使用mipmap, 纹理目标类型
                    producedRawTexture[i] = new RawTexture(width, ScopeBase.getHeight(), false, producedTextureTarget);
                    
                    // 准备纹理（分配OpenGL资源）
                    if (!producedRawTexture[i].isLoaded()) {                        // 检查纹理是否已加载
                        producedRawTexture[i].prepare(mCanvas.getGlCanvas());       // 准备纹理
                    }
                    
                    // 创建SurfaceTexture，关联到RawTexture
                    producedSurfaceTexture[i] = new SurfaceTexture(producedRawTexture[i].getId());
                  
                  int finalI = i;                                                   // 最终索引（lambda需要final变量）
                  
                  // 通过Handler在UI线程中回调
                  handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (onSurfaceTextureSet != null) {                      // 检查回调是否存在
                              onSurfaceTextureSet.onSet(finalI, producedSurfaceTexture[finalI], producedRawTexture[finalI]); // 回调通知
                            }
                        }
                    });
              } else {                                                              // 纹理已存在，更新尺寸
                  producedRawTexture[i].setSize(width, ScopeBase.getHeight());      // 更新纹理尺寸
              }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 抽象方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 更新纹理图像
     * 子类实现具体的纹理更新逻辑
     */
    public abstract void updateTexImage();

    /**
     * 绘制前处理
     * 子类实现绘制前的准备工作
     *
     * @param canvas OpenGL画布
     * @return true: 已处理，跳过清屏
     *         false: 未处理，需要清屏
     */
    public abstract boolean onPreDraw(ICanvasGL canvas);

    /**
     * 帧绘制回调
     * 每帧渲染时调用
     *
     * <p><b>绘制流程：</b></p>
     * <ol>
     *   <li>调用onPreDraw进行预处理</li>
     *   <li>如果预处理返回false，清除缓冲区</li>
     *   <li>更新所有SurfaceTexture</li>
     *   <li>调用updateTexImage更新纹理</li>
     *   <li>调用onGLDraw执行实际绘制</li>
     * </ol>
     */
    @Override
    public void onDrawFrame() {
        if(!onPreDraw(mCanvas)) {                                                   // 调用预处理
            mCanvas.clearBuffer(backgroundColor);                                   // 清除缓冲区（使用背景颜色）
        }
        
        // 更新SurfaceTexture（非GL_TEXTURE_2D模式）
        if (producedTextureTarget != GLES20.GL_TEXTURE_2D){                         // 检查纹理目标类型
            for(SurfaceTexture surfaceTexture: producedSurfaceTexture){             // 遍历所有SurfaceTexture
                surfaceTexture.updateTexImage();                                    // 更新纹理图像
            }
        }
        
        updateTexImage();                                                           // 调用子类的纹理更新方法
        
        // 调用子类的绘制方法
        onGLDraw(mCanvas, producedSurfaceTexture, producedRawTexture, outsideSharedSurfaceTexture, outsideSharedTexture);
    }

    /**
     * 获取渲染模式
     *
     * @return 渲染模式
     *         GLThread.RENDERMODE_WHEN_DIRTY: 按需渲染（默认）
     *         GLThread.RENDERMODE_CONTINUOUSLY: 连续渲染
     */
    protected int getRenderMode() {
        return GLThread.RENDERMODE_WHEN_DIRTY;                                      // 返回按需渲染模式
    }

    /**
     * OpenGL绘制抽象方法
     * 子类实现具体的绘制逻辑
     *
     * @param canvas OpenGL画布
     * @param producedSurfaceTexture 产出的SurfaceTexture数组
     * @param producedRawTexture 产出的RawTexture数组
     * @param outsideSharedSurfaceTexture 外部共享SurfaceTexture（可为null）
     * @param outsideSharedTexture 外部共享纹理（可为null）
     */
    protected abstract void onGLDraw(ICanvasGL canvas, SurfaceTexture []producedSurfaceTexture, RawTexture []producedRawTexture, @Nullable SurfaceTexture outsideSharedSurfaceTexture, @Nullable BasicTexture outsideSharedTexture);

    // ═══════════════════════════════════════════════════════════════════════════════
    // 渲染控制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 向GLThread提交任务
     * 任务将在OpenGL线程中执行
     *
     * @param r 要执行的任务
     */
    public void queueEvent(Runnable r) {
        if(isStart) {                                                               // 检查是否已启动
            if (mGLThread == null) {                                                // 检查GLThread是否存在
                return;                                                             // 不存在则返回
            }
            mGLThread.queueEvent(r);                                                // 提交任务到GLThread
        }
    }

    /**
     * 请求渲染
     * 触发一次渲染帧
     */
    public void requestRender() {
        if (isStart && mGLThread != null) {                                         // 检查启动状态和GLThread存在
            mGLThread.requestRender();                                              // 请求渲染
        }
    }

    /**
     * 请求渲染并等待
     * 触发一次渲染帧，并等待渲染完成
     */
    public void requestRenderAndWait() {
        if(isStart) {                                                               // 检查是否已启动
            if (mGLThread != null) {                                                // 检查GLThread是否存在
                mGLThread.requestRenderAndWait();                                   // 请求渲染并等待
            }
        }
    }
}
