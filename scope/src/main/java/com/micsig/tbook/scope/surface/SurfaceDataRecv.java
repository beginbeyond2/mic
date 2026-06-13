package com.micsig.tbook.scope.surface;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.util.Log;

import com.chillingvan.canvasgl.glcanvas.RawTexture;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.IDataBuffer;
import com.micsig.tbook.scope.Data.IDataListener;
import com.micsig.tbook.scope.Display.Display;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.ScopeMessage;
import com.micsig.tbook.scope.Trigger.Trigger;
import com.micsig.tbook.scope.Trigger.TriggerFactory;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.IChannel;
import com.micsig.tbook.scope.channel.SerialChannel;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                SurfaceDataRecv - Surface数据接收器类                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Surface模块的核心数据接收器类，实现Observer观察者模式，                      ║
 * ║   负责管理示波器的波形显示Surface、处理事件响应、协调数据流。                  ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 创建和管理SurfacePreview渲染器                                          ║
 * ║   2. 创建和管理SurfaceDevice设备                                             ║
 * ║   3. 响应系统事件（通道开关、显示模式、余辉等）                               ║
 * ║   4. 管理图层Z序和偏移                                                       ║
 * ║   5. 控制余辉效果                                                            ║
 * ║   6. 处理采样状态变化                                                        ║
 * ║                                                                              ║
 * ║ 【实现接口】                                                                 ║
 * ║   Observer - Java观察者接口，用于接收事件通知                                ║
 * ║                                                                              ║
 * ║ 【架构位置】                                                                 ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                         SurfaceDataRecv                             │   ║
 * ║   │                              │                                      │   ║
 * ║   │              ┌───────────────┼───────────────┐                      │   ║
 * ║   │              ▼               ▼               ▼                      │   ║
 * ║   │      ┌─────────────┐ ┌─────────────┐ ┌─────────────┐               │   ║
 * ║   │      │SurfacePreview│ │SurfaceDevice│ │ EventFactory│               │   ║
 * ║   │      │  (渲染器)   │ │  (设备)     │ │  (事件)     │               │   ║
 * ║   │      └─────────────┘ └─────────────┘ └─────────────┘               │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【事件响应列表】                                                             ║
 * ║   ┌────────────────────────────────────────────────────────────────────┐    ║
 * ║   │ 事件名称                    │ 处理方法                             │    ║
 * ║   ├────────────────────────────────────────────────────────────────────┤    ║
 * ║   │ EVENT_CHANNEL_OPEN          │ 更新通道Z序和图层Z序                  │    ║
 * ║   │ EVENT_CHANNEL_ACTIVE        │ 设置数学/参考通道激活                 │    ║
 * ║   │ EVENT_DISPLAY_MODE          │ 切换显示模式（YT/XY）                 │    ║
 * ║   │ EVENT_DIAPLAY_WAVE_BRIGHTNESS│ 更新波形亮度                         │    ║
 * ║   │ EVENT_AFTERGLOW_*           │ 余辉相关控制                          │    ║
 * ║   │ EVENT_WAVE_CLEAR            │ 清除波形                              │    ║
 * ║   │ EVENT_SAMPLE_VALID          │ 采样有效性变化                        │    ║
 * ║   │ EVENT_SCOPE_STATE           │ 运行/停止状态变化                     │    ║
 * ║   │ EVENT_DISPLAY_CCT           │ 色温显示开关                          │    ║
 * ║   │ EVENT_TRIGGER_TYPE          │ 触发类型变化                          │    ║
 * ║   │ EVENT_TIME_SCALE            │ 时基变化                              │    ║
 * ║   │ EVENT_WAVE_OFFSET           │ 波形偏移                              │    ║
 * ║   │ EVENT_DISPLAY_BACKGROUND    │ 背景颜色                              │    ║
 * ║   └────────────────────────────────────────────────────────────────────┘    ║
 * ║                                                                              ║
 * ║ 【数据流向】                                                                 ║
 * ║   硬件设备 → SurfaceDevice → SurfacePreview → OpenGL渲染 → 屏幕            ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 示波器波形显示主控制器                                                  ║
 * ║   2. 多通道波形渲染管理                                                     ║
 * ║   3. 余辉效果控制                                                           ║
 * ║   4. 波形平移和缩放                                                         ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   事件回调在主线程执行，需要注意与渲染线程的同步。                            ║
 * ║   SurfacePreview内部实现了OpenGL线程安全机制。                               ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - SurfacePreview: 波形渲染器                                              ║
 * ║   - SurfaceDevice: 硬件设备管理                                             ║
 * ║   - EventFactory: 事件工厂，用于订阅和发送事件                               ║
 * ║   - ChannelFactory: 通道工厂，获取通道信息                                  ║
 * ║   - Display: 显示设置管理                                                   ║
 * ║   - Scope: 示波器状态管理                                                   ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018-3-27                                       ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class SurfaceDataRecv implements Observer {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 日志标签
     * 用于在Logcat中过滤和识别本类的日志输出
     */
    private static final String TAG = "SurfaceDataRecv";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 核心成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Surface预览渲染器
     * 负责OpenGL ES渲染波形数据
     */
    private SurfacePreview surfacePreview;

    /**
     * Surface设备管理器
     * 负责管理硬件设备和数据流
     */
    private SurfaceDevice mDevice;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造Surface数据接收器
     * 初始化设备、Surface和事件监听器
     *
     * <p><b>初始化流程：</b></p>
     * <ol>
     *   <li>创建SurfaceDevice设备管理器</li>
     *   <li>初始化SurfacePreview渲染器</li>
     *   <li>注册事件监听器</li>
     * </ol>
     *
     * @param context Android应用上下文
     * @param surface Surface对象（SurfaceTexture或Surface）
     * @param height 预览高度（像素）
     */
    public SurfaceDataRecv(Context context, Object surface, int height) {
        mDevice = new SurfaceDevice(context);                                       // 创建设备管理器
        initSurface(surface, mDevice.getDevCnt(), height);                          // 初始化Surface渲染器
        // 注册事件监听器 - 通道相关
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_OPEN, this);       // 通道打开事件
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_ACTIVE, this);     // 通道激活事件
        // 注册事件监听器 - 显示相关
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_MODE, this);       // 显示模式事件
        EventFactory.addEventObserver(EventFactory.EVENT_DIAPLAY_WAVE_BRIGHTNESS, this); // 波形亮度事件
        // 注册事件监听器 - 余辉相关
        EventFactory.addEventObserver(EventFactory.EVENT_AFTERGLOW_CLEAR, this);    // 清除余辉事件
        EventFactory.addEventObserver(EventFactory.EVENT_AFTERGLOW_TIME, this);     // 余辉时间事件
        EventFactory.addEventObserver(EventFactory.EVENT_AFTERGLOW_ENABLE, this);   // 余辉使能事件
        EventFactory.addEventObserver(EventFactory.EVENT_AFTERGLOW_MATH_CLEAR, this); // 数学通道余辉清除
        EventFactory.addEventObserver(EventFactory.EVENT_AFTERGLOW_MATH, this);     // 数学通道余辉设置
        // 注册事件监听器 - 波形相关
        EventFactory.addEventObserver(EventFactory.EVENT_WAVE_CLEAR, this);         // 清除波形事件
        EventFactory.addEventObserver(EventFactory.EVENT_WAVE_OFFSET, this);        // 波形偏移事件
        // 注册事件监听器 - 采样和状态相关
        EventFactory.addEventObserver(EventFactory.EVENT_SAMPLE_VALID, this);       // 采样有效事件
        EventFactory.addEventObserver(EventFactory.EVENT_SCOPE_STATE, this);        // 示波器状态事件
        // 注册事件监听器 - 其他
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_CCT, this);        // 色温显示事件
        EventFactory.addEventObserver(EventFactory.EVENT_TRIGGER_TYPE, this);       // 触发类型事件
        EventFactory.addEventObserver(EventFactory.EVENT_TIME_SCALE, this);         // 时基事件
        EventFactory.addEventObserver(EventFactory.EVENT_DISPLAY_BACKGROUND, this); // 背景颜色事件
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取RGBA数据缓冲区
     * 返回当前渲染的RGBA图像数据
     *
     * @return RGBA数据缓冲区，如果surfacePreview为null则返回null
     */
    public ByteBuffer getBuffer(){
        if(surfacePreview != null){                                                 // 检查渲染器是否存在
            return surfacePreview.getRgbaByffer();                                  // 返回RGBA缓冲区
        }
        return null;                                                                // 返回null
    }

    /**
     * 获取步长（每行字节数）
     * 返回波形数据的行跨度
     *
     * @return 步长值（像素），如果surfacePreview为null则返回默认宽度
     */
    public int getStride(){
        if(surfacePreview != null){                                                 // 检查渲染器是否存在
            return surfacePreview.getStride();                                      // 返回步长值
        }
        return ScopeBase.getWidth();                                                // 返回默认宽度
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Surface初始化方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 初始化Surface渲染器
     * 创建SurfacePreview并设置回调监听器
     *
     * <p><b>初始化流程：</b></p>
     * <ol>
     *   <li>设置最大通道/数学/参考图层数</li>
     *   <li>创建SurfacePreview实例</li>
     *   <li>设置SurfaceTexture回调</li>
     *   <li>设置数据监听器</li>
     *   <li>设置图层有效回调</li>
     *   <li>启动渲染器</li>
     * </ol>
     *
     * @param surface Surface对象
     * @param nums 设备数量
     * @param height 预览高度
     */
    private void initSurface(Object surface, int nums, int height) {
        // 打印调试日志
        Log.d(TAG, "initSurface() called with: surface = [" + surface + "], nums = [" + nums + "], height = [" + height + "]");
        // 设置最大图层数量
        SurfacePreview.setMaxChLayer(nums);                                         // 设置最大通道图层数
        SurfacePreview.setMaxMathLayer(ChannelFactory.getMathChNums());             // 设置最大数学图层数
        SurfacePreview.setMaxRefLayer(ChannelFactory.getRefChNums());               // 设置最大参考图层数

        // 创建SurfacePreview实例
        surfacePreview = new SurfacePreview(ScopeBase.getWidth(), height, surface, nums);

        // 设置SurfaceTexture回调 - 当SurfaceTexture创建时打开设备
        surfacePreview.setOnSurfaceTextureSet(new SurfaceTextureRenderer.OnSurfaceTextureSet() {
            @Override
            public void onSet(int idx, SurfaceTexture surfaceTexture, RawTexture surfaceTextureRelatedTexture) {
                if(mDevice != null) {                                               // 检查设备是否存在
                    // 打印调试日志
                    Log.d(TAG, "onSet() called with: idx = [" + idx + "], surfaceTexture = [" + surfaceTexture + "], RawTextureId = [" + surfaceTextureRelatedTexture.getId() + "]");
                    mDevice.openDevice(idx, surfaceTexture);                        // 打开指定索引的设备
                }
            }
        });

        // 设置数据监听器
        surfacePreview.setDataListener(dataListener);

        // 设置图层有效回调 - 当图层创建完成时初始化显示参数
        surfacePreview.setLayerTextureValidListener(new SurfacePreview.OnLayerTextureValidListener() {
            @Override
            public void OnLayerTextureListener(List<LayerTexture> layerTexturelist) {
                // 发送Surface创建完成事件
                EventBase eventBase = new EventBase(EventFactory.EVENT_SURFACE_CREATED, SurfaceDataRecv.this);
                EventFactory.sendEvent(eventBase);
                // 初始化显示参数
                UpdateLayerZorder();                                                // 更新图层Z序
                UpdateChZorder();                                                   // 更新通道Z序
                DisplayMode();                                                      // 设置显示模式
                DisplayBrightness();                                                // 设置波形亮度
                setAfterglow();                                                     // 设置余辉效果
                upAfterglowState();                                                 // 更新余辉状态
                CCT();                                                              // 设置色温
                setOffset(false, 0, 0);                                             // 重置通道偏移
                setOffset(true, 0, 0);                                              // 重置数学通道偏移
            }
        });

        // 启动渲染器
        surfacePreview.start();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公共方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置预览高度
     * 更新渲染器的尺寸
     *
     * @param surface Surface对象（可为null）
     * @param height 新的预览高度（像素）
     */
    public void setPreViewHeight(Object surface, int height) {
        surfacePreview.setSize(ScopeBase.getWidth(), height);                       // 设置渲染器尺寸
    }

    /**
     * 获取指定图层的SurfaceNative
     * 用于JNI层的数据传递
     *
     * @param layer 图层索引
     * @return SurfaceNative实例
     */
    public SurfaceNative getSurfaceNative(int layer){
        return surfacePreview.getSurfaceNative(layer);                              // 返回指定图层的SurfaceNative
    }

    /**
     * 关闭Surface
     * 释放所有资源，注销事件监听器
     *
     * <p><b>释放流程：</b></p>
     * <ol>
     *   <li>发送Surface销毁事件</li>
     *   <li>注销所有事件监听器</li>
     *   <li>关闭设备</li>
     *   <li>销毁渲染器</li>
     * </ol>
     */
    public void closeSurface() {
        // 发送Surface销毁事件
        EventBase eventBase = new EventBase(EventFactory.EVENT_SURFACE_DESTROYED, this);
        EventFactory.sendEvent(eventBase);

        // 注销事件监听器 - 通道相关
        EventFactory.delEventObserver(EventFactory.EVENT_CHANNEL_OPEN, this);
        EventFactory.delEventObserver(EventFactory.EVENT_CHANNEL_ACTIVE, this);
        // 注销事件监听器 - 显示相关
        EventFactory.delEventObserver(EventFactory.EVENT_DISPLAY_MODE, this);
        EventFactory.delEventObserver(EventFactory.EVENT_DIAPLAY_WAVE_BRIGHTNESS, this);
        // 注销事件监听器 - 余辉相关
        EventFactory.delEventObserver(EventFactory.EVENT_AFTERGLOW_CLEAR, this);
        EventFactory.delEventObserver(EventFactory.EVENT_AFTERGLOW_TIME, this);
        EventFactory.delEventObserver(EventFactory.EVENT_AFTERGLOW_ENABLE, this);
        // 注销事件监听器 - 波形相关
        EventFactory.delEventObserver(EventFactory.EVENT_WAVE_CLEAR, this);
        // 注销事件监听器 - 采样和状态相关
        EventFactory.delEventObserver(EventFactory.EVENT_SAMPLE_VALID, this);
        EventFactory.delEventObserver(EventFactory.EVENT_SCOPE_STATE, this);
        // 注销事件监听器 - 其他
        EventFactory.delEventObserver(EventFactory.EVENT_DISPLAY_CCT, this);
        EventFactory.delEventObserver(EventFactory.EVENT_TRIGGER_TYPE, this);
        EventFactory.delEventObserver(EventFactory.EVENT_SYNCHEADER_CHANGE, this);
        EventFactory.delEventObserver(EventFactory.EVENT_TIME_SCALE, this);
        EventFactory.delEventObserver(EventFactory.EVENT_AFTERGLOW_MATH_CLEAR, this);
        EventFactory.delEventObserver(EventFactory.EVENT_AFTERGLOW_MATH, this);
        EventFactory.delEventObserver(EventFactory.EVENT_WAVE_OFFSET, this);
        EventFactory.delEventObserver(EventFactory.EVENT_DISPLAY_BACKGROUND, this);

        // 关闭设备
        if (mDevice != null) {
            mDevice.closeDevice();                                                  // 关闭设备
            mDevice = null;                                                         // 清空引用
        }

        // 销毁渲染器
        if (surfacePreview != null) {
            surfacePreview.end();                                                   // 结束渲染器
            surfacePreview = null;                                                  // 清空引用
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 数据监听器
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 数据监听器引用
     * 用于接收波形数据
     */
    private IDataListener dataListener = null;

    /**
     * 设置数据监听器
     *
     * @param dataListener 数据监听器实例
     */
    public void setDataListener(IDataListener dataListener) {
        if(surfacePreview != null) {                                                // 检查渲染器是否存在
            surfacePreview.setDataListener(dataListener);                           // 设置渲染器的数据监听器
        }
        this.dataListener = dataListener;                                           // 保存引用
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Z序管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 更新通道Z序
     * 根据通道的zOrder属性排序，更新渲染器的通道顺序
     *
     * <p><b>算法流程：</b></p>
     * <ol>
     *   <li>获取所有动态通道</li>
     *   <li>按zOrder降序排序</li>
     *   <li>分配到两个渲染器组（CH1-CH4和CH5-CH8）</li>
     *   <li>更新渲染器的Z序数组</li>
     * </ol>
     */
    private void UpdateChZorder(){
        List<Channel> chList = new ArrayList<Channel>();                            // 创建通道列表
        int maxIdx = ChannelFactory.getMaxChIdx();                                  // 获取最大通道索引
        for(int i = ChannelFactory.CH1; i < maxIdx; i++){                           // 遍历所有通道
            chList.add(ChannelFactory.getDynamicChannel(i));                        // 添加到列表
        }
        // 按zOrder降序排序
        chList.sort(new Comparator<Channel>() {
            @Override
            public int compare(Channel lhs, Channel rhs) {
                if (lhs.getzOrder() < rhs.getzOrder()) return 1;                    // lhs排在后面
                else if (lhs.getzOrder() > rhs.getzOrder()) return -1;              // lhs排在前面
                else return 0;                                                      // 相等
            }
        });

        int []zorder = new int[ChannelFactory.CH_CNT];                              // Z序数组

        int n = 0, m = ChannelFactory.CH5;                                          // 两个组的起始索引
        for(int i = 0; i < chList.size(); i++){                                     // 遍历排序后的列表
            int id = chList.get(i).getChId();                                       // 获取通道ID
            if(id < ChannelFactory.CH5){                                            // CH1-CH4组
                zorder[n++] = id;                                                   // 分配到第一组
            }else{                                                                  // CH5-CH8组
                zorder[m++] = id;                                                   // 分配到第二组
            }
        }

        surfacePreview.setChZorder(zorder);                                         // 更新渲染器的Z序
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 偏移管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 更新偏移量
     * 根据手指滑动方向更新波形偏移
     */
    private void UpdateOffset(){
        SlideFinger slideFinger = SlideFinger.getInstance();                       // 获取滑动处理器单例
        int x = 0, y = 0;                                                           // 偏移量初始化
        switch (slideFinger.getSlideDirection() ){                                  // 根据滑动方向
            case SlideFinger.MOVE_LEFTRIGHT:                                        // 左右滑动
                x = Math.round(slideFinger.getHorizontalOffset());                  // 获取水平偏移
                break;
            case SlideFinger.MOVE_UPDOWN:                                           // 上下滑动
                y = Math.round(slideFinger.getVerticalOffset());                    // 获取垂直偏移
                break;
            default:                                                                // 其他
                break;
        }
        surfacePreview.setChIdx(slideFinger.getChIdx());                            // 设置当前通道索引
        setOffset(slideFinger.isMathCh(), x, y);                                    // 应用偏移
    }

    /**
     * 设置偏移量
     *
     * @param bMath 是否为数学通道
     *              true: 数学通道
     *              false: 普通通道
     * @param x X方向偏移量（像素）
     * @param y Y方向偏移量（像素）
     */
    private void setOffset(boolean bMath, int x, int y){
        surfacePreview.setZoom(Scope.getInstance().isZoom());                       // 设置缩放状态
        surfacePreview.setOffset(bMath, -x, y);                                     // 设置偏移（X方向取反）
    }

    /**
     * 更新图层Z序
     * 根据通道状态更新图层的可见性和Z序
     *
     * <p><b>处理逻辑：</b></p>
     * <ul>
     *   <li>遍历所有通道（物理通道+数学通道+参考通道）</li>
     *   <li>动态通道：根据打开状态更新可见性</li>
     *   <li>数学/参考通道：直接设置图层属性</li>
     * </ul>
     */
    private void UpdateLayerZorder(){
        IChannel channel;                                                           // 通道接口
        int []chMaxZorder = {-1, -1};                                               // 每组的最大Z序
        boolean []bVisiable = {false, false};                                       // 每组的可见性
        LayerTexture layerTexture;                                                  // 图层纹理
        int layer = SurfacePreview.LAYER_CH1;                                       // 图层索引

        // 遍历所有通道（CH1到REF8）
        for(int i = ChannelFactory.CH1; i <= ChannelFactory.REF8; i++){
            channel = ChannelFactory.getValidChannel(i);                            // 获取有效通道
            if(channel != null){                                                    // 通道存在
                if(i == 0){                                                         // CH1
                    chMaxZorder[0] = channel.getzOrder();                           // 初始化第一组Z序
                }
                if(ChannelFactory.isDynamicCh(i)) {                                 // 动态通道
                    if (channel.isOpen()) {                                         // 通道打开
                        int idx = i / 4;                                            // 计算组索引（0或1）
                        bVisiable[idx] = true;                                      // 设置可见
                        if(chMaxZorder[idx] < channel.getzOrder()){                 // 更新最大Z序
                            chMaxZorder[idx] = channel.getzOrder();
                        }
                    }
                }else{                                                              // 数学/参考通道
                    layer = SurfacePreview.LAYER_MATH1 + i - ChannelFactory.MATH1;  // 计算图层索引
                    layerTexture = surfacePreview.getLayerTexture(layer);           // 获取图层
                    if(layerTexture != null) {                                      // 图层存在
                        layerTexture.setZorder(channel.getzOrder());                // 设置Z序
                        layerTexture.setVisiable(channel.isOpen());                 // 设置可见性
                    }
                }
            }
        }

        // 更新物理通道图层的可见性和Z序
        for(int i = SurfacePreview.LAYER_CH1; i < SurfacePreview.LAYER_CH_MAX; i++){
            layerTexture = surfacePreview.getLayerTexture(i);                       // 获取图层
            if(layerTexture != null) {                                              // 图层存在
                int idx = i - SurfacePreview.LAYER_CH1;                             // 计算组索引
                layerTexture.setVisiable(bVisiable[idx]);                           // 设置可见性
                layerTexture.setZorder(chMaxZorder[idx]);                           // 设置Z序
            }
        }

        surfacePreview.UpdateLayerZorder();                                         // 更新渲染器的图层Z序
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 显示设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置显示模式
     * 切换YT模式和XY模式
     */
    private void DisplayMode(){
        surfacePreview.forceClearWave();                                            // 强制清除波形
        surfacePreview.getLayerTexture(SurfacePreview.LAYER_XY).setVisiable(Display.getInstance().isXYMode()); // 设置XY图层可见性
    }

    /**
     * 设置波形亮度
     */
    private void DisplayBrightness(){
        surfacePreview.setBrightness(Display.getInstance().getBrightness());        // 设置亮度值
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 清除方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 清除波形
     * 清除串口通道数据和渲染器波形
     */
    private void clear(){
        SerialChannel channel;                                                      // 串口通道
        IDataBuffer dataBuffer;                                                     // 数据缓冲区
        for (int i = 0; i < ChannelFactory.SERIAL_CNT; i++) {                       // 遍历所有串口通道
            channel = ChannelFactory.getSerialChannel(ChannelFactory.S1 + i);       // 获取串口通道

            if(channel != null && channel.isOpen()) {                               // 通道存在且打开
                dataBuffer = channel.dequeue();                                     // 取出数据缓冲区
                if (dataBuffer != null) {                                           // 缓冲区存在
                    dataBuffer.write(null, 0, 0);                                   // 清空数据
                    channel.enqueue(dataBuffer);                                    // 放回队列
                }
            }
        }
        EventFactory.sendEvent(EventFactory.EVENT_SERIAL_UPDATE);                   // 发送串口更新事件

        surfacePreview.clear();                                                     // 清除渲染器波形
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 余辉控制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 更新余辉状态
     * 根据运行状态和显示模式设置余辉效果
     */
    private void upAfterglowState(){
        Scope scope = Scope.getInstance();                                          // 获取示波器实例
        if(scope.isRun()){                                                          // 运行状态
            boolean bAfterglow = false;                                             // 余辉标志
            bAfterglow = Display.getInstance().getPersistType() != Display.PERSIST_TYPE_NONE; // 检查余辉类型
            if(scope.isInScrollMode() || scope.isInSlowScaleMode()){                // 滚动或慢速时基模式
                bAfterglow = false;                                                 // 禁用余辉
            }
            surfacePreview.setAfterglow(SurfacePreview.LAYER_CH1, bAfterglow);      // 设置第一组余辉
            surfacePreview.setAfterglow(SurfacePreview.LAYER_CH2, bAfterglow);      // 设置第二组余辉
        }
    }

    /**
     * 处理采样有效性变化
     *
     * @param isSample 是否有效采样
     */
    private void sampleValid(boolean isSample){
        ScopeMessage.getInstance().setClearWave(false);                             // 清除波形标志
        Scope scope = Scope.getInstance();                                          // 获取示波器实例

        if(!isSample){                                                              // 采样无效
            if(scope.isRun()) {                                                     // 运行状态
                surfacePreview.afterglowClear();                                    // 清除余辉
                surfacePreview.requestRender();                                     // 请求渲染
                ms_sleep(50);                                                       // 等待50ms
                surfacePreview.lockSampleCnt();                                     // 锁定采样计数
            }else{                                                                  // 停止状态
                ms_sleep(50);                                                       // 等待50ms
            }
            surfacePreview.setSample(scope.isRun());                                // 设置采样状态
        }
    }

    /**
     * 毫秒级休眠
     *
     * @param ms 休眠时间（毫秒）
     */
    private void ms_sleep(long ms){
        try {
            Thread.sleep(ms);                                                       // 线程休眠
        } catch (InterruptedException e) {                                          // 捕获中断异常
            e.printStackTrace();                                                    // 打印异常堆栈
        }
    }

    /**
     * 清除余辉
     */
    private void afterglowClear(){
        //if(Scope.getInstance().isRun())
        {
            surfacePreview.afterglowClear();                                        // 清除余辉
        }
    }

    /**
     * 清除数学通道余辉
     */
    private void afterglowMathClear(){
        surfacePreview.afterglowMathClear();                                        // 清除数学通道余辉
    }

    /**
     * 设置数学通道余辉
     * 根据FFT余辉类型设置数学通道的余辉效果
     */
    private void setAfterglowMath(){
        boolean bAfterglow = true;                                                  // 余辉使能标志
        long ms = 200;                                                              // 余辉时间（毫秒）
        Display display = Display.getInstance();                                    // 获取显示实例

        switch (display.getFftPersistType()){                                       // 根据FFT余辉类型
            default:                                                                // 默认
            case Display.PERSIST_TYPE_NONE:                                         // 无余辉
                bAfterglow = false;                                                 // 禁用余辉
                break;
            case Display.PERSIST_TYPE_AUTO:                                         // 自动余辉
                ms = 100;                                                           // 100ms
                break;
            case Display.PERSIST_TYPE_NORMAL:                                       // 普通余辉
                ms = display.getFftPersistAdjustTime();                             // 获取调整时间
                break;
            case Display.PERSIST_TYPE_INFINITE:                                     // 无限余辉
                ms = -1;                                                            // -1表示无限
                break;
        }

        // 遍历所有数学通道
        for(int i = ChannelFactory.MATH1; i < ChannelFactory.getMaxMathIdx(); i++){
            int n = i - ChannelFactory.MATH1;                                       // 计算图层索引
            if(ChannelFactory.isMath_FFT_Ch(i)){                                    // FFT通道
                surfacePreview.setAfterglowTime(SurfacePreview.LAYER_MATH1 + n, ms); // 设置余辉时间
                surfacePreview.setAfterglow(SurfacePreview.LAYER_MATH1 + n, bAfterglow); // 设置余辉使能
            }else{                                                                  // 非FFT通道
                surfacePreview.setAfterglow(SurfacePreview.LAYER_MATH1 + n, false); // 禁用余辉
            }
        }
    }

    /**
     * 设置物理通道余辉
     */
    private void setAfterglow(){
        boolean bAfterglow = true;                                                  // 余辉使能标志
        long ms = 100;                                                              // 余辉时间（毫秒）
        switch (Display.getInstance().getPersistType()){                            // 根据余辉类型
            default:                                                                // 默认
            case Display.PERSIST_TYPE_NONE:                                         // 无余辉
                bAfterglow = false;                                                 // 禁用余辉
                break;
            case Display.PERSIST_TYPE_AUTO:                                         // 自动余辉
                ms = 100;                                                           // 100ms
                break;
            case Display.PERSIST_TYPE_NORMAL:                                       // 普通余辉
                ms = Display.getInstance().getPersistAdjustTime();                  // 获取调整时间
                break;
            case Display.PERSIST_TYPE_INFINITE:                                     // 无限余辉
                ms = -1;                                                            // -1表示无限
                break;
        }
        surfacePreview.setAfterglow(SurfacePreview.LAYER_CH1, bAfterglow);          // 设置第一组余辉使能
        surfacePreview.setAfterglowTime(SurfacePreview.LAYER_CH1, ms);              // 设置第一组余辉时间
        surfacePreview.setAfterglow(SurfacePreview.LAYER_CH2, bAfterglow);          // 设置第二组余辉使能
        surfacePreview.setAfterglowTime(SurfacePreview.LAYER_CH2, ms);              // 设置第二组余辉时间
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 运行状态控制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 处理运行/停止状态变化
     */
    private void run_stop(){
        Scope scope = Scope.getInstance();                                          // 获取示波器实例
        if(scope.isRun()){                                                          // 运行状态
            mDevice.clear();                                                        // 清除设备缓冲区
            surfacePreview.setSample(true);                                         // 启用采样
            upAfterglowState();                                                     // 更新余辉状态
        }
        surfacePreview.setRunAutoAfterflow(scope.isRun());                          // 设置自动余辉
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 其他设置方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置色温显示
     */
    private void CCT(){
        surfacePreview.setCCTEnable(Display.getInstance().isCCT());                 // 设置色温使能
    }

    /**
     * 设置亮度类型
     * 根据触发类型设置亮度类型
     */
    private void setLightType(){
        int val = TriggerFactory.getTriggerType() == Trigger.TRIG_TYPE_VIDEO ? 1 : 0; // 视频触发返回1，否则0
        surfacePreview.setLightType(val);                                           // 设置亮度类型
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Observer接口实现 - 事件处理
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 事件更新回调
     * 处理各种系统事件
     *
     * @param observable 被观察对象
     * @param data 事件数据
     */
    @Override
    public void update(Observable observable, Object data) {
        EventBase e = (EventBase) data;                                             // 转换为事件基类
        if(e != null){                                                              // 事件有效
            switch(e.getId()){                                                      // 根据事件ID分发
                case EventFactory.EVENT_CHANNEL_ACTIVE:                             // 通道激活事件
                {
                    int chActivate = ChannelFactory.getChActivate();                // 获取激活通道
                    ChannelFactory.setMathActive(chActivate);                       // 设置数学通道激活
                    ChannelFactory.setRefActive(chActivate);                        // 设置参考通道激活
                    // 这里不需要break; 继续处理CHANNEL_OPEN
                }
                case EventFactory.EVENT_CHANNEL_OPEN:                               // 通道打开事件
                    UpdateChZorder();                                               // 更新通道Z序
                    UpdateLayerZorder();                                            // 更新图层Z序
                    afterglowClear();                                               // 清除余辉
                    break;

                case EventFactory.EVENT_DISPLAY_MODE:                               // 显示模式事件
                    DisplayMode();                                                  // 设置显示模式
                    break;

                case EventFactory.EVENT_DIAPLAY_WAVE_BRIGHTNESS:                    // 波形亮度事件
                    DisplayBrightness();                                            // 设置波形亮度
                    break;

                case EventFactory.EVENT_AFTERGLOW_CLEAR:                            // 清除余辉事件
                    afterglowClear();                                               // 清除余辉
                    break;

                case EventFactory.EVENT_AFTERGLOW_MATH_CLEAR:                       // 清除数学余辉事件
                    afterglowMathClear();                                           // 清除数学通道余辉
                    break;

                case EventFactory.EVENT_AFTERGLOW_MATH:                             // 数学余辉设置事件
                    setAfterglowMath();                                             // 设置数学通道余辉
                    afterglowMathClear();                                           // 清除数学通道余辉
                    upAfterglowState();                                             // 更新余辉状态
                    break;

                case EventFactory.EVENT_AFTERGLOW_TIME:                             // 余辉时间事件
                case EventFactory.EVENT_AFTERGLOW_ENABLE:                           // 余辉使能事件
                    setAfterglow();                                                 // 设置余辉
                    upAfterglowState();                                             // 更新余辉状态
                    break;

                case EventFactory.EVENT_WAVE_CLEAR:                                 // 清除波形事件
                    clear();                                                        // 清除波形
                    break;

                case EventFactory.EVENT_SAMPLE_VALID:                               // 采样有效事件
                {
                    boolean [] bSample = (boolean []) e.getData();                   // 获取采样数据
                    if(bSample != null && bSample[1]) {                             // 数据有效
                        sampleValid(bSample[2]);                                    // 处理采样有效性
                    }
                }
                    break;

                case EventFactory.EVENT_SCOPE_STATE:                                // 示波器状态事件
                    run_stop();                                                     // 处理运行/停止
                    break;

                case EventFactory.EVENT_DISPLAY_CCT:                                // 色温显示事件
                    CCT();                                                          // 设置色温
                    break;

                case EventFactory.EVENT_TRIGGER_TYPE:                               // 触发类型事件
                    setLightType();                                                 // 设置亮度类型
                    break;

                case EventFactory.EVENT_TIME_SCALE:                                 // 时基事件
                    upAfterglowState();                                             // 更新余辉状态
                    break;

                case EventFactory.EVENT_WAVE_OFFSET:                                // 波形偏移事件
                    UpdateOffset();                                                 // 更新偏移
                    break;

                case EventFactory.EVENT_DISPLAY_BACKGROUND:                         // 背景颜色事件
                {
                    Display display = Display.getInstance();                        // 获取显示实例
                    //surfacePreview.setBackgroundColor(display.isXYMode() ? Color.TRANSPARENT : display.getWaveBackground());
                    surfacePreview.setBackgroundColor(display.getWaveBackground());  // 设置背景颜色
                    break;
                }
            }
        }
    }
}
