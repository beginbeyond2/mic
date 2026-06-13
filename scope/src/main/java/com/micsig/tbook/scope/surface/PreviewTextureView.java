package com.micsig.tbook.scope.surface;

import android.content.Context;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;

import com.micsig.tbook.scope.Data.DataFactory;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.math.MathService;

import java.nio.ByteBuffer;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                PreviewTextureView - 预览纹理视图类                            ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Surface模块的预览纹理视图类，实现TextureView.SurfaceTextureListener接口，   ║
 * ║   负责管理示波器波形预览的TextureView生命周期和数据接收。                      ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 监听TextureView的SurfaceTexture生命周期                                 ║
 * ║   2. 管理SurfaceDataRecv数据接收器                                           ║
 * ║   3. 处理预览尺寸变化                                                        ║
 * ║   4. 提供数据缓冲区访问接口                                                  ║
 * ║                                                                              ║
 * ║ 【实现接口】                                                                 ║
 * ║   TextureView.SurfaceTextureListener - Android纹理视图监听器                ║
 * ║                                                                              ║
 * ║ 【生命周期流程】                                                             ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                     TextureView 生命周期                            │   ║
 * ║   │                                                                     │   ║
 * ║   │  ┌──────────────────┐                                              │   ║
 * ║   │  │ onSurfaceTexture │ → Surface可用，创建SurfaceDataRecv           │   ║
 * ║   │  │   Available      │                                              │   ║
 * ║   │  └────────┬─────────┘                                              │   ║
 * ║   │           │                                                         │   ║
 * ║   │           ▼                                                         │   ║
 * ║   │  ┌──────────────────┐                                              │   ║
 * ║   │  │ onSurfaceTexture │ → Surface尺寸变化，更新预览高度              │   ║
 * ║   │  │   SizeChanged    │                                              │   ║
 * ║   │  └────────┬─────────┘                                              │   ║
 * ║   │           │                                                         │   ║
 * ║   │           ▼                                                         │   ║
 * ║   │  ┌──────────────────┐                                              │   ║
 * ║   │  │ onSurfaceTexture │ → 每帧更新（当前未使用）                      │   ║
 * ║   │  │    Updated       │                                              │   ║
 * ║   │  └────────┬─────────┘                                              │   ║
 * ║   │           │                                                         │   ║
 * ║   │           ▼                                                         │   ║
 * ║   │  ┌──────────────────┐                                              │   ║
 * ║   │  │ onSurfaceTexture │ → Surface销毁，释放SurfaceDataRecv           │   ║
 * ║   │  │   Destroyed      │                                              │   ║
 * ║   │  └──────────────────┘                                              │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【数据流向】                                                                 ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 硬件设备    │───▶│SurfaceDataRecv│───▶│ DataFactory │                   ║
 * ║   │ (FPGA/USB) │    │ (数据接收器) │    │ (数据工厂)  │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                              │                                              ║
 * ║                              ▼                                              ║
 * ║                      ┌─────────────┐                                       ║
 * ║                      │ TextureView │ → 波形显示                           ║
 * ║                      └─────────────┘                                       ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 示波器波形预览窗口                                                      ║
 * ║   2. 实时数据采集与显示                                                      ║
 * ║   3. 多窗口预览                                                              ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   回调方法在UI线程中调用，需要注意与数据线程的同步。                          ║
 * ║   SurfaceDataRecv内部实现了线程安全机制。                                     ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - TextureView: Android纹理视图，提供SurfaceTexture                        ║
 * ║   - SurfaceDataRecv: 数据接收器，处理波形数据                                ║
 * ║   - DataFactory: 数据工厂单例，管理数据监听器                                ║
 * ║   - ScopeBase: 示波器基类，提供全局配置                                      ║
 * ║   - MathService: 数学运算服务，用于刷新数学运算结果                          ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018-3-29                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class PreviewTextureView implements TextureView.SurfaceTextureListener {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 日志标签
     * 用于在Logcat中过滤和识别本类的日志输出
     */
    private static final String TAG = "PreviewTextureView";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 纹理视图引用
     * 用于设置SurfaceTexture监听器
     */
    private TextureView mTextureView;

    /**
     * 应用上下文
     * 用于创建SurfaceDataRecv等需要Context的组件
     */
    private Context mContext;

    /**
     * 数据接收器
     * 负责接收和处理波形数据
     * 在Surface可用时创建，在Surface销毁时释放
     */
    private SurfaceDataRecv surfaceDataRecv = null;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取数据缓冲区
     * 返回当前波形数据的ByteBuffer
     *
     * @return 波形数据缓冲区，如果surfaceDataRecv为null则返回null
     */
    public ByteBuffer getBuffer(){
        if(surfaceDataRecv != null){                                                // 检查数据接收器是否存在
            return surfaceDataRecv.getBuffer();                                     // 返回数据缓冲区
        }
        return null;                                                                // 返回null表示无数据
    }

    /**
     * 获取步长（每行字节数）
     * 返回波形数据的行跨度
     *
     * @return 步长值（像素），如果surfaceDataRecv为null则返回默认宽度
     */
    public int getStride(){
        if(surfaceDataRecv != null){                                                // 检查数据接收器是否存在
            return surfaceDataRecv.getStride();                                     // 返回步长值
        }
        return ScopeBase.getWidth();                                                // 返回默认宽度
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造预览纹理视图实例
     * 初始化TextureView并设置SurfaceTexture监听器
     *
     * @param context Android应用上下文
     * @param textureView 纹理视图，用于显示波形预览
     */
    public PreviewTextureView(Context context, TextureView textureView){
        mTextureView = textureView;                                                 // 保存纹理视图引用
        mContext = context;                                                         // 保存应用上下文
        mTextureView.setSurfaceTextureListener(this);                               // 设置SurfaceTexture监听器（本类实现该接口）
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公共方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置预览尺寸变化
     * 更新数据接收器的预览高度
     *
     * @param height 新的预览高度（像素）
     */
    public void setPreviewSizeChange(int height) {
        surfaceDataRecv.setPreViewHeight(null, height);                             // 设置预览高度（Surface参数为null）
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // SurfaceTextureListener接口实现 - 生命周期回调
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * SurfaceTexture可用回调
     * 当TextureView的SurfaceTexture创建完成时调用
     * 在此创建SurfaceDataRecv并设置数据监听器
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>打印日志记录Surface创建</li>
     *   <li>创建SurfaceDataRecv实例</li>
     *   <li>设置数据监听器为DataFactory单例</li>
     * </ol>
     *
     * @param surface 新创建的SurfaceTexture
     * @param width Surface宽度（像素）
     * @param height Surface高度（像素）
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        // 打印调试日志，记录Surface创建事件
        Log.d(TAG, "onSurfaceTextureAvailable() called with: surface = [" + surface + "], width = [" + width + "], height = [" + height + "]");
        
        // 创建数据接收器实例
        // 参数: 上下文, SurfaceTexture, 预览高度
        surfaceDataRecv = new SurfaceDataRecv(mContext, surface, height);
        
        // 设置数据监听器为DataFactory单例
        // DataFactory负责将数据分发给各个数据处理模块
        surfaceDataRecv.setDataListener(DataFactory.getInstance());
    }

    /**
     * SurfaceTexture尺寸变化回调
     * 当TextureView的尺寸发生变化时调用
     * 更新预览高度和转换比例，并刷新数学运算
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>更新数据接收器的预览高度</li>
     *   <li>更新ScopeBase的转换比例</li>
     *   <li>打印日志记录尺寸变化</li>
     *   <li>强制刷新数学运算结果</li>
     * </ol>
     *
     * @param surface 尺寸变化的SurfaceTexture
     * @param width 新的宽度（像素）
     * @param height 新的高度（像素）
     */
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // 更新数据接收器的预览高度
        surfaceDataRecv.setPreViewHeight(surface, height);
        
        // 更新全局转换比例
        // 用于将数据坐标转换为屏幕坐标
        ScopeBase.setConvertScale(height);
        
        // 打印调试日志，记录尺寸变化事件
        Log.d(TAG, "onSurfaceTextureSizeChanged() called with: surface = [" + surface + "], width = [" + width + "], height = [" + height + "]");
        
        // 强制刷新数学运算结果
        // 确保数学运算图层与新的尺寸同步
        MathService.forceMathRefresh();
    }

    /**
     * SurfaceTexture销毁回调
     * 当TextureView的SurfaceTexture即将销毁时调用
     * 释放SurfaceDataRecv资源
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>打印开始日志</li>
     *   <li>关闭并释放SurfaceDataRecv</li>
     *   <li>打印结束日志</li>
     *   <li>返回true表示已处理销毁</li>
     * </ol>
     *
     * @param surface 即将销毁的SurfaceTexture
     * @return true: 表示已处理销毁，可以释放SurfaceTexture
     *         false: 表示不释放SurfaceTexture（本实现始终返回true）
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        // 打印调试日志，记录销毁开始
        Log.d(TAG, "onSurfaceTextureDestroyed begin");
        
        // 检查数据接收器是否存在
        if(surfaceDataRecv != null){
            surfaceDataRecv.closeSurface();                                         // 关闭Surface，释放资源
            surfaceDataRecv = null;                                                 // 清空引用，帮助GC回收
        }
        
        // 打印调试日志，记录销毁结束
        Log.d(TAG, "onSurfaceTextureDestroyed end");
        
        return true;                                                                // 返回true，允许销毁SurfaceTexture
    }

    /**
     * SurfaceTexture更新回调
     * 当SurfaceTexture有新帧可用时调用
     * 当前实现为空，不处理帧更新事件
     *
     * <p><b>说明：</b>此回调在每帧更新时触发，频率较高。
     * 当前实现不需要处理帧更新事件，因此方法体为空。</p>
     *
     * @param surface 更新的SurfaceTexture
     */
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // 当前实现不需要处理帧更新事件
        // 方法体为空
    }
}
