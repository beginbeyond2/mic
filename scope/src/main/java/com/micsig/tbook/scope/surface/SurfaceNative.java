package com.micsig.tbook.scope.surface;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                SurfaceNative - JNI原生Surface接口类                           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Surface模块的JNI桥接类，提供Java层与Native层（C/C++）之间的Surface操作接口。║
 * ║   封装了Android Surface的底层渲染功能，用于示波器波形数据的高效绘制。         ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理Android Surface生命周期                                             ║
 * ║   2. 提供JNI原生方法接口                                                      ║
 * ║   3. 支持多种绘制模式（普通、XY、位图）                                        ║
 * ║   4. 通过反射设置缓冲区数量                                                   ║
 * ║                                                                              ║
 * ║ 【JNI方法列表】                                                              ║
 * ║   ┌────────────────────────────────────────────────────────────────────┐    ║
 * ║   │  Native方法        │  功能说明                                     │    ║
 * ║   ├────────────────────────────────────────────────────────────────────┤    ║
 * ║   │  acquire           │  获取Surface，初始化Native窗口                 │    ║
 * ║   │  release           │  释放Surface，销毁Native窗口                   │    ║
 * ║   │  clear             │  清除Surface内容                              │    ║
 * ║   │  draw              │  绘制波形数据（通道+参数缓冲区）               │    ║
 * ║   │  drawXY            │  绘制XY模式波形（X+Y+参数缓冲区）              │    ║
 * ║   │  drawBitmap        │  绘制位图数据                                 │    ║
 * ║   └────────────────────────────────────────────────────────────────────┘    ║
 * ║                                                                              ║
 * ║ 【数据流向】                                                                 ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ Java层      │    │ SurfaceNative│    │ Native层    │                   ║
 * ║   │ ByteBuffer  │───▶│ drawSurface │───▶│ JNI C/C++   │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                              │                              ║
 * ║                                              ▼                              ║
 * ║                                      ┌─────────────┐                       ║
 * ║                                      │ ANativeWindow│ → GPU渲染            ║
 * ║                                      └─────────────┘                       ║
 * ║                                                                              ║
 * ║ 【绘制模式说明】                                                             ║
 * ║   1. 普通模式（YT模式）：时间-幅度波形显示                                   ║
 * ║      drawSurface(chBuffer, paramBuffer)                                      ║
 * ║                                                                              ║
 * ║   2. XY模式：李萨如图形显示                                                  ║
 * ║      drawXYSurface(xBuffer, yBuffer, paramBuffer)                            ║
 * ║                                                                              ║
 * ║   3. 位图模式：直接绘制图像数据                                              ║
 * ║      drawSurface(byteBuffer, offset, length)                                 ║
 * ║                                                                              ║
 * ║ 【反射机制】                                                                 ║
 * ║   使用Java反射调用Android隐藏API：                                          ║
 * ║   - Surface.setBufferCount(int)：设置缓冲区数量                             ║
 * ║   - 用于优化渲染性能（双缓冲/三缓冲）                                        ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 示波器波形实时渲染                                                      ║
 * ║   2. 多通道波形叠加显示                                                      ║
 * ║   3. XY模式李萨如图形                                                        ║
 * ║   4. 高速数据流渲染                                                          ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   部分方法使用synchronized关键字保护。                                       ║
 * ║   Native方法实现需要保证线程安全。                                           ║
 * ║   必须在OpenGL/渲染线程中调用绘制方法。                                      ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - Surface: Android原生Surface对象                                         ║
 * ║   - SurfaceTexture: Android纹理Surface                                      ║
 * ║   - ByteBuffer: NIO缓冲区，用于数据传递                                     ║
 * ║   - JNI Native层: C/C++实现                                                 ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018-5-16                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class SurfaceNative {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Native窗口句柄
     * 存储ANativeWindow指针（C/C++层）
     * 类型为long，可存储64位指针值
     */
    private long mWindow;

    /**
     * Android Surface对象
     * 用于图形渲染的目标Surface
     * 从SurfaceTexture创建
     */
    private Surface surface = null;

    /**
     * 有效标志
     * true: Surface已获取，可以进行绘制
     * false: Surface未获取或已释放
     */
    private boolean bValid = false;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造SurfaceNative实例（默认缓冲区数量）
     * 从SurfaceTexture创建Surface对象
     *
     * @param surfaceTexture Android SurfaceTexture，用于创建Surface
     */
    public SurfaceNative(SurfaceTexture surfaceTexture){
        surface = new Surface(surfaceTexture);                                      // 从SurfaceTexture创建Surface
    }

    /**
     * 构造SurfaceNative实例（指定缓冲区数量）
     * 从SurfaceTexture创建Surface对象，并通过反射设置缓冲区数量
     *
     * <p><b>反射调用说明：</b></p>
     * <ul>
     *   <li>调用android.view.Surface.setBufferCount(int)方法</li>
     *   <li>该方法是Android隐藏API，需要通过反射访问</li>
     *   <li>缓冲区数量影响渲染性能（双缓冲/三缓冲）</li>
     * </ul>
     *
     * @param surfaceTexture Android SurfaceTexture，用于创建Surface
     * @param bufferCount 缓冲区数量
     *                    2: 双缓冲（默认）
     *                    3: 三缓冲（更流畅，但延迟更高）
     *                    7: 多缓冲（示波器高速采集场景）
     */
    public SurfaceNative(SurfaceTexture surfaceTexture, int bufferCount){
        surface = new Surface(surfaceTexture);                                      // 从SurfaceTexture创建Surface
//      surface.setBufferCount(bufferCount);                                        // [已注释] 直接调用需要系统权限
        try {
            // 使用反射调用隐藏API: Surface.setBufferCount(int)
            Class<?> clazz = Class.forName("android.view.Surface");                 // 获取Surface类
            Method method = clazz.getDeclaredMethod("setBufferCount", int.class);   // 获取setBufferCount方法
            method.setAccessible(true);                                             // 设置可访问（绕过权限检查）
            method.invoke(surface, bufferCount);                                    // 调用方法设置缓冲区数量
        } catch (ClassNotFoundException e) {                                        // 类未找到异常
            e.printStackTrace();                                                    // 打印异常堆栈
        } catch (NoSuchMethodException e) {                                         // 方法未找到异常
            e.printStackTrace();                                                    // 打印异常堆栈
        } catch (InvocationTargetException e) {                                     // 调用目标异常
            e.printStackTrace();                                                    // 打印异常堆栈
        } catch (IllegalAccessException e) {                                        // 非法访问异常
            e.printStackTrace();                                                    // 打印异常堆栈
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Surface生命周期管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取Surface
     * 初始化Native层的ANativeWindow，准备进行渲染
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>检查Surface和尺寸有效性</li>
     *   <li>调用Native方法acquire初始化窗口</li>
     *   <li>设置有效标志</li>
     * </ol>
     *
     * @param width Surface宽度（像素）
     * @param height Surface高度（像素）
     */
    public void acquireSurface(int width, int height){
        if(surface != null && width > 0 && height > 0){                             // 检查Surface和尺寸有效性
            acquire(surface, width, height);                                        // 调用Native方法获取Surface
            bValid = true;                                                          // 设置有效标志
        }
    }

    /**
     * 释放Surface
     * 销毁Native层的ANativeWindow，释放资源
     *
     * <p><b>处理流程：</b></p>
     * <ol>
     *   <li>检查Surface存在</li>
     *   <li>清除有效标志</li>
     *   <li>调用Native方法release释放资源</li>
     * </ol>
     */
    public void releaseSurface(){
        if(surface != null){                                                        // 检查Surface是否存在
            bValid = false;                                                         // 清除有效标志
            release();                                                              // 调用Native方法释放Surface
        }
    }

    /**
     * 清除Surface内容
     * 清空Surface的显示内容
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     */
    public synchronized void clearSurface(){
        if (surface != null && bValid) {                                            // 检查Surface存在且有效
            clear();                                                                // 调用Native方法清除Surface
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 绘制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 绘制位图数据到Surface
     * 将ByteBuffer中的图像数据直接绘制到Surface
     *
     * <p><b>使用场景：</b></p>
     * <ul>
     *   <li>预览图像显示</li>
     *   <li>截图显示</li>
     *   <li>静态图像渲染</li>
     * </ul>
     * 
     * <p><b>线程安全：</b>使用synchronized保证多线程安全</p>
     *
     * @param byteBuffer 图像数据缓冲区，必须是直接缓冲区（DirectBuffer）
     * @param offset 数据偏移量（字节）
     * @param length 数据长度（字节）
     */
    public void drawSurface(ByteBuffer byteBuffer, int offset, int length){
        synchronized (this) {                                                       // 同步块，保护绘制操作
            if (surface != null && bValid && byteBuffer.isDirect()) {               // 检查Surface有效性和缓冲区类型
                drawBitmap(byteBuffer, offset, length);                             // 调用Native方法绘制位图
            }
        }
    }

    /**
     * 绘制波形数据到Surface（YT模式）
     * 将通道数据和参数数据绘制到Surface
     *
     * <p><b>数据格式：</b></p>
     * <ul>
     *   <li>chBuffer: 通道波形数据（灰度值数组）</li>
     *   <li>paramBuffer: 渲染参数（颜色、偏移、缩放等）</li>
     * </ul>
     *
     * @param chBuffer 通道数据缓冲区，必须是直接缓冲区
     * @param paramBuffer 参数缓冲区，必须是直接缓冲区
     */
    public void drawSurface(ByteBuffer chBuffer, ByteBuffer paramBuffer){
        if (surface != null && bValid && chBuffer.isDirect() && paramBuffer.isDirect()) { // 检查Surface和缓冲区有效性
            draw(chBuffer, paramBuffer);                                            // 调用Native方法绘制波形
        }
    }

    /**
     * 绘制XY模式波形到Surface
     * 将X通道和Y通道数据绘制为李萨如图形
     *
     * <p><b>XY模式说明：</b></p>
     * <ul>
     *   <li>X轴: 一个通道的信号</li>
     *   <li>Y轴: 另一个通道的信号</li>
     *   <li>用于观察两个信号的相位关系</li>
     *   <li>可显示李萨如图形</li>
     * </ul>
     *
     * @param xBuffer X轴数据缓冲区，必须是直接缓冲区
     * @param yBuffer Y轴数据缓冲区，必须是直接缓冲区
     * @param paramBuffer 参数缓冲区，必须是直接缓冲区
     */
    public void drawXYSurface(ByteBuffer xBuffer, ByteBuffer yBuffer, ByteBuffer paramBuffer){
        if (surface != null && bValid && xBuffer.isDirect() && yBuffer.isDirect() && paramBuffer.isDirect()) { // 检查所有缓冲区有效性
            drawXY(paramBuffer, xBuffer, yBuffer);                                  // 调用Native方法绘制XY波形
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // JNI Native方法声明
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * [JNI Native方法] 获取Surface
     * 在Native层初始化ANativeWindow
     *
     * @param surface Android Surface对象
     * @param width Surface宽度（像素）
     * @param height Surface高度（像素）
     */
    private native void acquire(Surface surface, int width, int height);

    /**
     * [JNI Native方法] 释放Surface
     * 在Native层销毁ANativeWindow
     */
    private native void release();

    /**
     * [JNI Native方法] 清除Surface
     * 清空Surface的显示内容
     */
    private native void clear();

    /**
     * [JNI Native方法] 绘制波形（YT模式）
     * 在Native层执行波形渲染
     *
     * @param chBuffer 通道数据缓冲区
     * @param paramBuffer 参数缓冲区
     */
    private native void draw(ByteBuffer chBuffer, ByteBuffer paramBuffer);

    /**
     * [JNI Native方法] 绘制XY模式波形
     * 在Native层执行XY模式渲染
     *
     * @param paramBuffer 参数缓冲区
     * @param xBuffer X轴数据缓冲区
     * @param yBuffer Y轴数据缓冲区
     */
    private native void drawXY(ByteBuffer paramBuffer, ByteBuffer xBuffer, ByteBuffer yBuffer);

    /**
     * [JNI Native方法] 绘制位图
     * 在Native层执行位图渲染
     *
     * @param byteBuffer 图像数据缓冲区
     * @param offset 数据偏移量
     * @param length 数据长度
     */
    private native void drawBitmap(ByteBuffer byteBuffer, int offset, int length);
}
