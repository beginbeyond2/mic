package com.micsig.tbook.tbookscope.wavezone.wave; // 包声明：串行总线管理类所属包路径

import android.content.Context; // 导入Android上下文类，用于获取应用资源和系统服务
import android.graphics.Canvas; // 导入Android画布类，用于2D图形绘制
import android.os.Handler; // 导入Android Handler类，用于线程间消息通信
import android.os.Message; // 导入Android消息类，用于Handler消息传递

import com.chillingvan.canvasgl.ICanvasGL; // 导入OpenGL画布接口，用于GPU加速渲染
import com.micsig.base.FilterThread; // 导入过滤线程基类，用于数据过滤处理
import com.micsig.base.Logger; // 导入日志工具类，用于调试信息输出
import com.micsig.tbook.scope.Data.SerialData; // 导入串行数据类，封装串行总线原始数据
import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类，定义事件基本结构
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂类，管理事件观察者
import com.micsig.tbook.scope.Scope; // 导入示波器核心类，提供全局访问入口
import com.micsig.tbook.scope.ScopeBase; // 导入示波器基类，提供坐标转换系数
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类，创建通道实例
import com.micsig.tbook.scope.channel.SerialChannel; // 导入串行通道类，管理串行数据采集
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载类，处理配置恢复
import com.micsig.tbook.tbookscope.config.ScopeConfig; // 导入示波器配置类，管理运行时配置
import com.micsig.tbook.tbookscope.main.maincenter.serialsword.ISerialsWord; // 导入串行字接口，定义串行字类型常量
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials; // 导入串行右侧菜单布局类，定义串行协议索引
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线类，实现响应式事件通信
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava枚举类，定义事件类型常量
import com.micsig.tbook.tbookscope.structdata.ExternalKeysCommand; // 导入外部按键命令类，定义MCU到ARM命令
import com.micsig.tbook.tbookscope.tools.Tools; // 导入工具类，提供通用工具方法
import com.micsig.tbook.tbookscope.util.App; // 导入应用工具类，获取全局Application上下文
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类，管理持久化配置
import com.micsig.tbook.tbookscope.wavezone.ISerialBus; // 导入串行总线接口，定义数据回调方法
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 导入工作模式接口，定义模式切换方法
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusStruct; // 导入串行总线数据结构类，定义协议数据结构
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStructParse; // 导入串行总线文本解析类，解析协议数据
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialImageBuffer; // 导入串行图像缓冲区类，存储图像数据
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialImageDoubleCache; // 导入串行图像双缓冲类，实现无锁数据交换
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialTxtBuffer; // 导入串行文本缓冲区类，存储解析后的文本数据
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道定义类，定义通道编号常量

import java.io.InputStream; // 导入输入流类，用于读取Assets资源文件
import java.nio.ByteBuffer; // 导入字节缓冲区类，高效处理二进制数据
import java.util.ArrayList; // 导入动态数组类，存储可变长度列表
import java.util.HashMap; // 导入哈希映射类，实现键值对快速查找
import java.util.List; // 导入列表接口，定义列表操作规范
import java.util.Map; // 导入映射接口，定义键值对操作规范
import java.util.Observable; // 导入可观察类，实现观察者模式被观察者
import java.util.Observer; // 导入观察者接口，实现观察者模式观察者
import java.util.concurrent.LinkedBlockingQueue; // 导入链式阻塞队列类，线程安全队列

import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口，处理异步事件


/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                        SerialBusManage 类说明文档                           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   串行总线管理类 - MHO系列示波器串行总线解码系统的核心管理组件                   │
 * │   负责管理4个串行总线通道（S1-S4）的数据接收、解析、缓存和显示                   │
 * │                                                                             │
 * │ 【核心职责】                                                                 │
 * │   1. 单例管理：通过静态内部类实现线程安全的单例模式                             │
 * │   2. 通道管理：管理4个SerialBus实例（S1-S4），支持独立配置和显示                 │
 * │   3. 数据接收：通过Observer模式接收串行数据更新事件                             │
 * │   4. 数据解析：使用FilterThread过滤线程处理原始字节数据                         │
 * │   5. UI更新：通过DrawSerialUIHandle Handler更新UI界面                          │
 * │   6. 协议支持：支持7种串行协议（UART/LIN/CAN/SPI/I2C/ARINC429/MIL-STD-1553B）   │
 * │   7. 双缓冲机制：使用SerialImageDoubleCache实现无锁数据交换                      │
 * │                                                                             │
 * │ 【架构设计】                                                                 │
 * │   ┌─────────────────────────────────────────────────────────────────┐       │
 * │   │                    SerialBusManage (单例)                        │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    实现接口层                              │   │       │
 * │   │  │  IWaveShowManage  │  ISerialBus  │  IWorkMode            │   │       │
 * │   │  │  (波形显示管理)    │  (串行总线)  │  (工作模式)            │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    核心组件层                              │   │       │
 * │   │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐   │   │       │
 * │   │  │  │ SerialBus   │  │ SerialBus   │  │ SerialBus       │   │   │       │
 * │   │  │  │    (S1)     │  │    (S2)     │  │ (S3)   (S4)     │   │   │       │
 * │   │  │  └─────────────┘  └─────────────┘  └─────────────────┘   │   │       │
 * │   │  │              mapSerialBus (HashMap管理)                   │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    数据处理层                              │   │       │
 * │   │  │  ┌──────────────┐  ┌──────────────┐  ┌────────────────┐ │   │       │
 * │   │  │  │ DataBean[]   │  │ FilterThread │  │ DrawSerialUI   │ │   │       │
 * │   │  │  │ (数据容器)    │  │ (过滤线程)    │  │ Handle(Handler)│ │   │       │
 * │   │  │  └──────────────┘  └──────────────┘  └────────────────┘ │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   │                              ↓                                   │       │
 * │   │  ┌──────────────────────────────────────────────────────────┐   │       │
 * │   │  │                    事件监听层                              │   │       │
 * │   │  │  ┌─────────────────────────────────────────────────────┐ │   │       │
 * │   │  │  │ eventUIObserver (Observer模式)                       │ │   │       │
 * │   │  │  │ 监听: EVENT_SERIAL_UPDATE / EVENT_SERIAL_TXT_UPDATE │ │   │       │
 * │   │  │  └─────────────────────────────────────────────────────┘ │   │       │
 * │   │  └──────────────────────────────────────────────────────────┘   │       │
 * │   └─────────────────────────────────────────────────────────────────┘       │
 * │                                                                             │
 * │ 【数据流向】                                                                 │
 * │   FPGA → SerialChannel → EventFactory → eventUIObserver → FilterThread     │
 * │        → DataBean → SerialImageDoubleCache → DrawSerialUIHandle            │
 * │        → OnDataChange → SerialBus → OpenGL渲染                              │
 * │                                                                             │
 * │ 【依赖关系】                                                                 │
 * │   上游依赖: SerialChannel(数据源) / EventFactory(事件分发) / RxBus(事件总线)  │
 * │   下游依赖: SerialBus(通道实例) / SerialImageDoubleCache(双缓冲)             │
 * │   横向依赖: ChannelFactory(通道工厂) / ScopeConfig(配置) / CacheUtil(缓存)    │
 * │                                                                             │
 * │ 【线程安全】                                                                 │
 * │   1. 单例模式：静态内部类实现，保证线程安全的延迟初始化                        │
 * │   2. 数据同步：使用synchronized(lock)保护DataBean数据访问                     │
 * │   3. 双缓冲机制：SerialImageDoubleCache实现无锁数据交换                       │
 * │   4. Handler机制：DrawSerialUIHandle确保UI更新在主线程执行                   │
 * │                                                                             │
 * │ 【使用示例】                                                                 │
 * │   // 获取单例实例                                                             │
 * │   SerialBusManage manage = SerialBusManage.getInstance();                   │
 * │   // 获取指定通道的SerialBus实例                                               │
 * │   SerialBus s1 = manage.getSerialBus(TChan.S1);                             │
 * │   // 获取串行文本缓冲区                                                        │
 * │   SerialTxtBuffer buffer = manage.getSerialTxtBuffer(TChan.S1);             │
 * │   // 切换工作模式                                                              │
 * │   manage.switchWorkMode(WorkMode.YT);                                        │
 * │                                                                             │
 * │ 【注意事项】                                                                 │
 * │   1. 该类为单例，全局只有一个实例                                              │
 * │   2. 数据更新通过Observer模式触发，不主动轮询                                   │
 * │   3. 双缓冲机制确保数据解析和UI渲染不冲突                                       │
 * │   4. 支持XY模式和YT模式切换                                                    │
 * │                                                                             │
 * │ 【作者信息】                                                                 │
 * │   Created by liwb on 2017/5/19                                               │
 * │   Last Modified: 2024                                                        │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 串行总线管理类
 * <p>
 * 核心职责：
 * 1. 管理4个串行总线通道（S1-S4）的生命周期和状态
 * 2. 接收并处理FPGA发送的串行总线解码数据
 * 3. 协调数据解析线程和UI渲染线程的数据交换
 * 4. 提供7种串行协议的数据访问接口
 * <p>
 * 设计模式：
 * - 单例模式：通过静态内部类实现线程安全的单例
 * - 观察者模式：通过Observer监听串行数据更新事件
 * - 双缓冲模式：使用SerialImageDoubleCache实现无锁数据交换
 * <p>
 * 实现接口：
 * - IWaveShowManage：波形显示管理接口，提供选择、偏移、绘制功能
 * - ISerialBus：串行总线接口，提供数据变更回调
 * - IWorkMode：工作模式接口，提供模式切换功能
 */
public class SerialBusManage implements IWaveShowManage, ISerialBus, IWorkMode {
    
    // ==================== 常量定义区域 ====================
    
    /**
     * 日志标签
     * 用于Logcat日志输出，标识日志来源为SerialBusManage类
     */
    private static final String TAG = "SerialBusManage";

    /**
     * 串行总线通道S1常量
     * 对应TChan.S1，表示第一个串行总线通道
     */
    public static final int SerialBus_S1 = TChan.S1;

    /**
     * 串行总线通道S2常量
     * 对应TChan.S2，表示第二个串行总线通道
     */
    public static final int SerialBus_S2 = TChan.S2;

    /**
     * 串行总线通道S3常量
     * 对应TChan.S3，表示第三个串行总线通道
     */
    public static final int SerialBus_S3 = TChan.S3;

    /**
     * 串行总线通道S4常量
     * 对应TChan.S4，表示第四个串行总线通道
     */
    public static final int SerialBus_S4 = TChan.S4;

    // ==================== 单例模式实现 ====================
    
    /**
     * 单例持有者类（静态内部类实现）
     * <p>
     * 设计原理：
     * 利用Java类加载机制保证线程安全，静态内部类在首次访问时才会加载，
     * JVM保证类加载过程的线程安全性，从而实现延迟初始化的单例模式。
     * <p>
     * 优点：
     * 1. 线程安全：JVM保证类加载的线程安全
     * 2. 延迟初始化：只有在调用getInstance()时才创建实例
     * 3. 无锁设计：不需要synchronized关键字
     */
    public static class SerialBusManageHolder {
        /**
         * 单例实例
         * 静态final修饰，保证全局唯一且不可变
         */
        public static final SerialBusManage instance = new SerialBusManage();
    }

    /**
     * 获取单例实例
     * <p>
     * 访问方式：SerialBusManage.getInstance()
     * <p>
     * 线程安全：由静态内部类加载机制保证
     * 
     * @return SerialBusManage单例实例
     */
    public static SerialBusManage getInstance() {
        return SerialBusManageHolder.instance; // 返回静态内部类持有的单例实例
    }

    // ==================== 内部类定义 ====================
    
    /**
     * 数据传输对象（DTO）
     * <p>
     * 用于封装串行总线数据更新的相关信息，包括：
     * - 字节数据缓冲区：存储原始串行数据
     * - 时间到像素转换系数：用于将时间转换为屏幕像素位置
     * - 起始X坐标：数据在屏幕上的起始位置
     * - 结束X坐标：数据在屏幕上的结束位置
     * <p>
     * 设计目的：
     * 将分散的数据封装为对象，便于在FilterThread和Handler之间传递，
     * 同时支持数组索引访问，提高多通道数据管理效率。
     */
    public class DataBean {
        /**
         * 串行字节数据缓冲区
         * 容量：1MB (1024 * 1024字节)
         * 用途：存储从FPGA接收的原始串行总线解码数据
         */
        private ByteBuffer SerialByteBuffer = ByteBuffer.allocate(1024 * 1024);
        
        /**
         * 时间到像素转换系数
         * 单位：像素/秒
         * 用途：将时间值转换为屏幕像素位置，用于波形显示
         */
        private long timeToPix;
        
        /**
         * 数据起始X坐标
         * 单位：像素
         * 用途：标识数据在屏幕上的起始显示位置
         */
        private int startX, endX;
    }

    // ==================== 成员变量定义 ====================
    
    /**
     * 数据访问同步锁
     * 用途：保护DataBean数组的并发访问，防止数据竞争
     * 使用场景：serialBusUpdateParseToScreen()和DrawSerialUIHandle中
     */
    private Object lock = new Object();
    
    /**
     * 串行总线实例映射表
     * Key: 通道编号 (TChan.S1/S2/S3/S4)
     * Value: SerialBus实例
     * 用途：管理4个串行总线通道的实例
     */
    private Map<Integer, SerialBus> mapSerialBus = new HashMap<>();
    
    /**
     * 数据传输对象数组
     * 长度：TChan.MaxSerial（最大串行通道数，通常为4）
     * 用途：存储每个通道的数据缓冲区，支持索引快速访问
     * 索引计算：dataBean[ch - TChan.S1]
     */
    private DataBean[] dataBean = new DataBean[TChan.MaxSerial];
    
    /**
     * UI绘制Handler
     * 用途：在主线程中更新串行总线UI显示
     * 接收消息：SerialBus_S1/S2/S3/S4，触发对应通道的数据更新
     */
    private DrawSerialUIHandle drawSerialUIHandle;
    
    /**
     * 强制刷新UI回调接口列表
     * 用途：存储需要强制刷新UI的回调接口实例
     * 使用场景：clearSerialBusTxtBuffer()中通知UI刷新
     */
    private List<IForceRefreshUI> listForceRefreshUI = new ArrayList<>();
    
    /**
     * 应用上下文
     * 用途：获取应用资源、颜色等系统服务
     * 初始化：从App.get()获取全局Application上下文
     */
    private Context context = App.get().getApplicationContext();

    // ==================== 构造函数 ====================
    
    /**
     * 构造函数
     * <p>
     * 初始化流程：
     * 1. 初始化DataBean数组，为每个串行通道创建数据容器
     * 2. 创建SerialBus实例并配置初始属性（位置、可见性、颜色）
     * 3. 注册Observer监听串行数据更新事件
     * 4. 初始化UI绘制Handler
     * 5. 订阅RxBus事件（缓存加载、MCU命令）
     * 6. 启动FilterThread过滤线程
     * <p>
     * 注意：构造函数为private，只能通过getInstance()获取单例
     */
    public SerialBusManage() {
        // 初始化DataBean数组：为每个串行通道创建数据容器
        TChan.foreachSerial((ch) -> {
            int idx = ch - TChan.S1; // 计算数组索引：S1=0, S2=1, S3=2, S4=3
            dataBean[idx] = new DataBean(); // 创建数据传输对象实例
        });

        // 初始化SerialBus实例：为每个串行通道创建并配置SerialBus
        TChan.foreachSerial((ch) -> {
            SerialBus serialBus = new SerialBus(); // 创建SerialBus实例
            serialBus.setLineNameId(ch); // 设置通道编号标识
            // 从缓存读取Y位置并转换为UI坐标
            serialBus.setY(CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_SERIAL_Y_POSITION + ch) * ScopeBase.getToUICoff());
            serialBus.setVisible(true); // 默认设置为可见
            serialBus.setColor(TChan.getChannelColor(context, ch)); // 设置通道颜色
            mapSerialBus.put(ch, serialBus); // 存入映射表
        });

        // 注册Observer监听器：监听串行数据更新事件
        EventFactory.addEventObserver(EventFactory.EVENT_SERIAL_UPDATE, eventUIObserver); // 监听图像数据更新
        EventFactory.addEventObserver(EventFactory.EVENT_SERIAL_TXT_UPDATE, eventUIObserver); // 监听文本数据更新

        // 初始化UI绘制Handler
        drawSerialUIHandle = new DrawSerialUIHandle();
        
        // 初始化RxBus订阅
        initControl();

        // 配置FilterThread过滤线程：设置数据解析任务
        filterThread.setRunnable(new Runnable() {
            @Override
            public void run() {
                // 线程执行体：调用串行总线数据解析方法
                // Thread.currentThread().setName("SerialBusParse"); // 可选：设置线程名称
                serialBusUpdateParseToScreen(); // 执行数据解析并更新到屏幕
            }
        });
    }

    // ==================== 初始化方法 ====================
    
    /**
     * 初始化RxBus订阅
     * <p>
     * 订阅事件：
     * 1. MAIN_LOAD_CACHE：缓存加载完成事件，触发配置恢复
     * 2. MAIN_LOAD_CACHE_EX：扩展缓存加载事件
     * 3. MCUTOARM：MCU到ARM命令事件
     * <p>
     * 使用RxJava响应式编程，实现松耦合的事件通信
     */
    private void initControl() {
        // 订阅缓存加载事件：恢复串行总线配置
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        // 订阅扩展缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE_EX).subscribe(consumerLoadCache);
        // 订阅MCU命令事件：处理运行/停止命令
        RxBus.getInstance().getObservable(RxEnum.MCUTOARM).subscribe(consumerMcuToArm);
    }

    /**
     * 初始化方法（空实现）
     * <p>
     * 预留的初始化入口，当前为空实现
     * 可能用于外部显式初始化场景
     */
    public void init() {
        // 空实现，预留扩展
    }

    // ==================== RxJava消费者定义 ====================
    
    /**
     * 缓存加载消费者
     * <p>
     * 响应MAIN_LOAD_CACHE和MAIN_LOAD_CACHE_EX事件
     * 触发串行总线配置恢复（Y位置等）
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCache(); // 调用缓存设置方法
        }
    };

    /**
     * 设置缓存配置
     * <p>
     * 功能：
     * 1. 从缓存恢复每个串行通道的Y位置
     * 2. 更新SerialBus实例的Y坐标
     * 3. 同步更新SerialChannel的Y位置
     * <p>
     * 注意：仅在非XY模式下执行配置恢复
     */
    public void setCache() {
        // 遍历所有串行通道
        TChan.foreachSerial((ch) -> {
            // 检查是否处于XY模式（XY模式下不设置Y位置）
            if (!Scope.getInstance().isInXYMode()) {
                // 从缓存读取Y位置（原始值）
                double s1Y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_SERIAL_Y_POSITION + ch);
                // 转换为UI坐标
                s1Y = s1Y * ScopeBase.getToUICoff();
                // 设置SerialBus的Y位置
                setPositionY(ch, s1Y);
                // 获取FPGA通道编号
                int fpgaCh = TChan.toFpgaChNo(ch);
                // 获取SerialChannel实例
                SerialChannel chan = ChannelFactory.getSerialChannel(fpgaCh);
                if (chan != null) {
                    // 同步更新SerialChannel的Y位置
                    chan.setPos(s1Y);
                }
            }
        });
    }

    /**
     * MCU命令消费者
     * <p>
     * 响应MCU到ARM的命令事件
     * 当前处理：MCUTOARM_RUNSTOP（运行/停止命令）
     */
    private Consumer<Integer> consumerMcuToArm = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Exception {
            switch (integer) {
                case ExternalKeysCommand.MCUTOARM_RUNSTOP:
                    // 处理运行/停止命令
                    Logger.i(TAG, "run|stop!");
                    break;
            }
        }
    };

    // ==================== 通道访问方法 ====================
    
    /**
     * 获取指定通道的SerialBus实例
     * <p>
     * 遍历mapSerialBus查找匹配的通道实例
     * 
     * @param chNo 通道编号（TChan.S1/S2/S3/S4）
     * @return SerialBus实例，未找到返回null
     */
    public SerialBus getSerialBus(int chNo) {
        // 遍历所有SerialBus实例
        for (SerialBus c : mapSerialBus.values()) {
            // 检查通道编号是否匹配
            if (c.getLineNameID() == chNo) {
                return c; // 返回匹配的实例
            }
        }
        return null; // 未找到返回null
    }

    // ==================== 串型文本相关方法 ====================
    
    /**
     * 返回串形文件数据缓冲区
     * <p>
     * 根据通道编号获取对应的SerialTxtBuffer实例
     * 支持S12组合通道和独立通道
     * 
     * @param chNo_ISerialsWord 通道编号或类型标识
     *         - ISerialsWord.TYPE_S12：S1和S2组合通道
     *         - TChan.S1/S2/S3/S4：独立通道
     * @return SerialTxtBuffer实例，无效参数返回null
     */
    public SerialTxtBuffer getSerialTxtBuffer(int chNo_ISerialsWord) {
        // 处理S12组合通道
        if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
            SerialBus serialbus = mapSerialBus.get(TChan.S1); // 获取S1通道
            return serialbus.getSerialTxtBuffer(); // 返回S1的文本缓冲区
        } else if (TChan.isSerial(chNo_ISerialsWord)) {
            // 处理独立串行通道
            SerialBus serialbus = mapSerialBus.get(chNo_ISerialsWord); // 获取指定通道
            return serialbus.getSerialTxtBuffer(); // 返回文本缓冲区
        }
        return null; // 无效参数返回null
    }

    /**
     * 强制刷新UI回调接口
     * <p>
     * 定义UI强制刷新的回调方法
     * 用于在清除文本缓冲区后通知UI刷新
     */
    public interface IForceRefreshUI {
        /**
         * 强制刷新UI回调
         * 当文本缓冲区被清除时触发
         */
        void onForceRefreshUI();
    }

    /**
     * 添加强制刷新UI回调
     * 
     * @param onForceRefreshUI 强制刷新回调实例
     */
    public void AddForceRefresh(IForceRefreshUI onForceRefreshUI) {
        listForceRefreshUI.add(onForceRefreshUI); // 添加到回调列表
    }

    /**
     * 清除所有串行总线文本缓冲区
     * <p>
     * 功能：
     * 1. 中断所有文本解析任务
     * 2. 清除S1-S4四个通道的文本缓冲区
     * 3. 通知所有注册的UI刷新回调
     * <p>
     * 注意：清除前会等待100ms确保解析任务完成
     */
    public void clearSerialBusTxtBuffer() {
        // 中断所有通道的文本解析任务
        SerialBusTxtStructParse.getInstance().InterruptedParse(-1);
        // 等待解析任务完成
        Tools.sleep(100);
        // 清除S1-S4四个通道的文本缓冲区
        mapSerialBus.get(TChan.S1).getSerialTxtBuffer().clearAll();
        mapSerialBus.get(TChan.S2).getSerialTxtBuffer().clearAll();
        mapSerialBus.get(TChan.S3).getSerialTxtBuffer().clearAll();
        mapSerialBus.get(TChan.S4).getSerialTxtBuffer().clearAll();
        // 通知所有UI刷新回调
        for (int i = 0; i < listForceRefreshUI.size(); i++) {
            listForceRefreshUI.get(i).onForceRefreshUI();
        }
    }

    /**
     * 获取串行文本缓冲区队列
     * <p>
     * 根据协议类型和通道编号获取对应的文本数据队列
     * 支持7种串行协议：UART、LIN、CAN、SPI、I2C、ARINC429、MIL-STD-1553B
     * 
     * @param chNo_ISerialsWord 通道编号或类型标识
     *         - ISerialsWord.TYPE_S12：S1和S2组合通道
     *         - TChan.S1/S2/S3/S4：独立通道
     * @param index_RightLayoutSerials 协议类型索引（RightLayoutSerials常量）
     *         - SERIALS_UART：UART协议
     *         - SERIALS_LIN：LIN协议
     *         - SERIALS_CAN：CAN协议
     *         - SERIALS_SPI：SPI协议
     *         - SERIALS_I2C：I2C协议
     *         - SERIALS_M429：ARINC429协议
     *         - SERIALS_M1553B：MIL-STD-1553B协议
     * @param isAll 是否返回全部数据队列
     *         - true：返回全部数据队列（用于导出）
     *         - false：返回当前屏数据队列（用于显示）
     * @return LinkedBlockingQueue数据队列，无效参数返回null
     */
    public LinkedBlockingQueue getSerialTxtBufferQueue(int chNo_ISerialsWord, int index_RightLayoutSerials, boolean isAll) {
        // 获取协议属性索引
        int propertyIndex = RightLayoutSerials.getPropertyIndex(index_RightLayoutSerials);
        // 检查索引有效性
        if (propertyIndex == -1) return null;
        // 检查协议是否启用（调试模式跳过检查）
        if (ScopeConfig.getConfig().isBusEnable(propertyIndex) == false && App.IsDebug() == false)
            return null;
        
        // 根据协议类型返回对应队列
        switch (index_RightLayoutSerials) {
            case RightLayoutSerials.SERIALS_UART: {
                // UART协议
                if (isAll) {
                    // 返回全部数据
                    if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getUartS1S2QueueTotal();
                    } else {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getUartQueueTotal();
                    }
                } else {
                    // 返回当前屏数据
                    if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getUartS1S2ListScreen();
                    } else {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getUartListScreen();
                    }
                }
            }
            case RightLayoutSerials.SERIALS_LIN: {
                // LIN协议
                if (isAll) {
                    if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getLinS1S2QueueTotal();
                    } else {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getLinQueueTotal();
                    }
                } else {
                    if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getLinS1S2ListScreen();
                    } else {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getLinListScreen();
                    }
                }
            }
            case RightLayoutSerials.SERIALS_CAN: {
                // CAN协议
                if (isAll) {
                    if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getCanS1S2QueueTotal();
                    } else {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getCanQueueTotal();
                    }
                } else {
                    if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getCanS1S2ListScreen();
                    } else {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getCanListScreen();
                    }
                }
            }
            case RightLayoutSerials.SERIALS_SPI: {
                // SPI协议
                if (isAll) {
                    if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getSpiS1S2QueueTotal();
                    } else {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getSpiQueueTotal();
                    }
                } else {
                    if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getSpiS1S2ListScreen();
                    } else {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getSpiListScreen();
                    }
                }
            }
            case RightLayoutSerials.SERIALS_I2C: {
                // I2C协议
                if (isAll) {
                    if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getI2cS1S2QueueTotal();
                    } else {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getI2cQueueTotal();
                    }
                } else {
                    if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getI2cS1S2ListScreen();
                    } else {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getI2cListScreen();
                    }
                }
            }
            case RightLayoutSerials.SERIALS_M429: {
                // ARINC429协议
                if (isAll) {
                    if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getArinc429S1S2QueueTotal();
                    } else {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getArinc429QueueTotal();
                    }
                } else {
                    if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getArinc429S1S2ListScreen();
                    } else {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getArinc429ListScreen();
                    }
                }
            }
            case RightLayoutSerials.SERIALS_M1553B: {
                // MIL-STD-1553B协议
                if (isAll) {
                    if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getMilstd1553bS1S2QueueTotal();
                    } else {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getMilstd1553bQueueTotal();
                    }
                } else {
                    if (chNo_ISerialsWord == ISerialsWord.TYPE_S12) {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getMilstd1553bS1S2ListScreen();
                    } else {
                        return getSerialTxtBuffer(chNo_ISerialsWord).getMilstd1553bListScreen();
                    }
                }
            }
        }
        return null; // 无效协议类型返回null
    }

    /**
     * 返回图像缓冲区队列
     * <p>
     * 获取指定通道的图像数据队列，用于波形显示
     * 
     * @param serialNum 串行通道编号（TChan.S1/S2/S3/S4）
     * @param t 数据结构类型（如SerialBusStruct.UartStruct.class）
     * @param <T> 泛型类型，支持各种串行协议数据结构
     * @return 数据列表，包含指定类型的图像数据
     */
    public <T> List<T> getSerialImgBufferQueue(int serialNum, Class<T> t) {
        return (mapSerialBus.get(serialNum).getImageBufferList(t)); // 返回图像缓冲区列表
    }

    /**
     * 返回通道类型
     * <p>
     * 根据串行总线类型编码返回对应的数据结构Class对象
     * 
     * @param serialNum 串行通道编号（TChan.S1/S2/S3/S4）
     * @return 数据结构Class对象
     *         - UartStruct.class：UART协议
     *         - LinStruct.class：LIN协议
     *         - CanStruct.class：CAN协议
     *         - SpiStruct.class：SPI协议
     *         - I2cStruct.class：I2C协议
     *         - Arinc429Struct.class：ARINC429协议
     *         - MilSTD1553bStruct.class：MIL-STD-1553B协议
     */
    public Class getSerialBusType(int serialNum) {
        // 获取串行总线类型编码
        int type = mapSerialBus.get(serialNum).getSerialBusType();
        // 根据类型返回对应的Class对象
        switch (type) {
            case SerialBusStruct.SerialBusType_UART:
                return SerialBusStruct.UartStruct.class; // UART协议
            case SerialBusStruct.SerialBusType_LIN:
                return SerialBusStruct.LinStruct.class; // LIN协议
            case SerialBusStruct.SerialBusType_CAN:
                return SerialBusStruct.CanStruct.class; // CAN协议
            case SerialBusStruct.SerialBusType_SPI:
                return SerialBusStruct.SpiStruct.class; // SPI协议
            case SerialBusStruct.SerialBusType_I2C:
                return SerialBusStruct.I2cStruct.class; // I2C协议
            case SerialBusStruct.SerialBusType_429:
                return SerialBusStruct.Arinc429Struct.class; // ARINC429协议
            // case SerialBusStruct.SerialBusType_1553B:return SerialBusStruct.MilSTD1553bStruct.class;
            default:
                return SerialBusStruct.MilSTD1553bStruct.class; // 默认返回MIL-STD-1553B
        }
    }

    // ==================== 操作方法 ====================
    
    /**
     * 设置通道可见性
     * <p>
     * 功能：
     * 1. 检查用户是否手动添加了该串行总线
     * 2. 根据用户设置和传入的visible参数决定最终可见性
     * 3. 更新对应SerialBus实例的可见状态
     * 
     * @param ChNo 通道编号（TChan.S1/S2/S3/S4）
     * @param visible 是否可见
     */
    public void setVisible(int ChNo, boolean visible) {
        // 从缓存读取用户是否手动添加了该串行总线
        boolean sAddByUser = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_SERIALS + TChan.toSerialNumber(ChNo));
        // 计算最终可见性：visible && 用户已添加
        boolean temp = visible && sAddByUser;
        // 遍历查找并更新可见性
        for (SerialBus c : mapSerialBus.values()) {
            if (c.getLineNameID() == ChNo) {
                c.setVisible(temp); // 设置可见性
            }
        }
    }

    /**
     * 移动选中通道的像素偏移
     * <p>
     * 功能：移动当前选中的串行总线通道的水平位置
     * 用途：响应用户拖拽操作，调整波形显示位置
     * 
     * @param px 像素偏移量（正值右移，负值左移）
     */
    public void movePix(int px) {
        // 遍历所有串行总线实例
        for (Integer i : mapSerialBus.keySet()) {
            SerialBus tem = mapSerialBus.get(i);
            // 只移动选中的通道
            if (tem.isSelected()) {
                tem.movePix(px); // 移动指定像素
            }
        }
    }

    /**
     * 增加像素移动（向右移动1像素）
     * <p>
     * 功能：将选中的串行总线通道向右移动1像素
     * 用途：响应用户按键操作（如右箭头键）
     */
    public void addPixMov() {
        // 遍历所有串行总线实例
        for (Integer i : mapSerialBus.keySet()) {
            SerialBus tem = mapSerialBus.get(i);
            // 只移动选中的通道
            if (tem.isSelected()) {
                tem.movePix(1); // 向右移动1像素
            }
        }
    }

    /**
     * 减少像素移动（向左移动1像素）
     * <p>
     * 功能：将选中的串行总线通道向左移动1像素
     * 用途：响应用户按键操作（如左箭头键）
     */
    public void subPixMove() {
        // 遍历所有串行总线实例
        for (Integer i : mapSerialBus.keySet()) {
            SerialBus tem = mapSerialBus.get(i);
            // 只移动选中的通道
            if (tem.isSelected()) {
                tem.movePix(-1); // 向左移动1像素
            }
        }
    }

    /**
     * 选择光标位置的通道
     * <p>
     * 功能：
     * 1. 清除所有通道的选中状态
     * 2. 检测点击位置是否在某个通道的范围内
     * 3. 设置命中通道为选中状态
     * 
     * @param x 点击位置的X坐标（像素）
     * @param y 点击位置的Y坐标（像素）
     * @return 选中的通道编号，无选中返回-1
     */
    public int selectCursor(int x, int y) {
        // 第一步：清除所有通道的选中状态
        for (SerialBus c : mapSerialBus.values()) {
            c.setSelected(false); // 取消选中
        }
        // 第二步：检测点击位置是否命中某个通道
        for (SerialBus c : mapSerialBus.values()) {
            // 检查Y坐标是否在通道范围内，且通道可见
            if (y >= c.getY() && y <= c.getY() + c.getHeight() && c.getVisible()) {
                c.setSelected(true); // 设置选中
                return c.getLineNameID(); // 返回通道编号
            }
        }
        return -1; // 未命中任何通道
    }

    /**
     * 设置选中通道
     * <p>
     * 功能：根据通道编号设置选中状态
     * 用途：程序化选择某个通道
     * 
     * @param ChNo 要选中的通道编号（TChan.S1/S2/S3/S4）
     */
    public void setSelectCursor(int ChNo) {
        // 遍历所有串行总线实例
        for (Integer i : mapSerialBus.keySet()) {
            SerialBus tem = mapSerialBus.get(i);
            if (tem.getLineNameID() == ChNo) {
                tem.setSelected(true); // 设置选中
            } else {
                tem.setSelected(false); // 取消选中
            }
        }
    }

    // ==================== IWorkMode接口实现 ====================
    
    /**
     * 切换工作模式
     * <p>
     * 功能：通知所有串行总线实例切换工作模式
     * 支持模式：YT模式、XY模式等
     * 
     * @param workMode 工作模式常量（@WorkMode注解）
     *         - WorkMode.YT：YT模式（时间-幅度显示）
     *         - WorkMode.XY：XY模式（X-Y显示）
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        // 遍历所有串行总线实例
        for (SerialBus c : mapSerialBus.values()) {
            c.switchWorkMode(workMode); // 切换工作模式
        }
    }

    // ==================== IWaveShowManage接口实现 ====================
    
    /**
     * 获取当前选中的通道编号
     * <p>
     * 功能：遍历所有通道，返回第一个选中通道的编号
     * 
     * @return 选中的通道编号（TChan.S1/S2/S3/S4），无选中返回-1
     */
    @Override
    public int isSelected() {
        // 遍历所有串行总线实例
        for (SerialBus c : mapSerialBus.values()) {
            // 检查是否选中
            if (c.isSelected()) {
                return c.getLineNameID(); // 返回选中通道编号
            }
        }
        return -1; // 无选中通道
    }

    /**
     * 设置选中通道的Y偏移
     * <p>
     * 功能：垂直移动选中的串行总线通道
     * 用途：响应用户垂直拖拽操作
     * 
     * @param offsetY Y方向偏移量（像素，正值向上移动）
     */
    @Override
    public void setOffsetY(int offsetY) {
        // 遍历所有串行总线实例
        for (SerialBus c : mapSerialBus.values()) {
            // 只移动选中的通道
            if (c.isSelected() == true) {
                c.setY(c.getY() - offsetY); // 更新Y位置（减法因为屏幕坐标系Y轴向下）
            }
        }
    }

    /**
     * 设置指定通道的Y位置
     * <p>
     * 功能：直接设置指定通道的Y坐标
     * 用途：从缓存恢复位置或程序化设置位置
     * 
     * @param serialsId 串行通道编号（TChan.S1/S2/S3/S4）
     * @param position Y坐标位置（像素）
     */
    public void setPositionY(int serialsId, double position) {
        // 遍历所有串行总线实例
        for (SerialBus c : mapSerialBus.values()) {
            // 查找匹配的通道
            if (c.getLineNameID() == serialsId) {
                c.setY(position); // 设置Y位置
                break; // 找到后退出循环
            }
        }
    }

    /**
     * 绘制到Canvas（2D绘制）
     * <p>
     * 功能：使用Android Canvas绘制串行总线波形
     * 当前为空实现，实际绘制使用OpenGL
     * 
     * @param canvas Android Canvas对象
     */
    @Override
    public void draw(Canvas canvas) {
        // 空实现，实际绘制使用OpenGL
    }

    /**
     * 绘制到OpenGL Canvas
     * <p>
     * 功能：使用OpenGL渲染所有串行总线波形
     * 这是实际的绘制方法，使用GPU加速渲染
     * 
     * @param canvas OpenGL Canvas对象
     */
    @Override
    public void draw(ICanvasGL canvas) {
        // 遍历所有串行总线实例
        for (SerialBus c : mapSerialBus.values()) {
            c.draw(canvas); // 绘制每个通道
        }
    }

    /**
     * 从Assets加载测试数据
     * <p>
     * 功能：从Assets目录读取测试用的串行数据文件
     * 用途：开发和调试阶段加载模拟数据
     * 
     * @param fileName Assets中的文件名
     * @return ByteBuffer包含文件内容，失败返回null
     */
    public ByteBuffer getFromAssets(String fileName) {
        String result = ""; // 初始化结果字符串（未使用）
        try {
            // 打开Assets文件输入流
            InputStream in = App.get().getResources().getAssets().open(fileName);
            // 获取文件字节数
            int lenght = in.available();
            // 创建字节数组
            byte[] buffer = new byte[lenght];
            // 读取文件内容到字节数组
            in.read(buffer);
            // 将字节数组包装为ByteBuffer并返回
            return ByteBuffer.wrap(buffer);
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常堆栈
        }
        return null; // 失败返回null
    }

    // ==================== ISerialBus接口实现 ====================
    
    /**
     * 标题变更回调
     * <p>
     * 功能：当串行总线协议类型变更时触发
     * 用途：通知SerialBus实例更新显示标题
     * 
     * @param chNo 通道编号（TChan.S1/S2/S3/S4）
     * @param serialBusType 串行总线类型（UART/LIN/CAN/SPI/I2C/ARINC429/MIL-STD-1553B）
     */
    @Override
    public void OnTitleChange(int chNo, int serialBusType) {
        // 获取对应的SerialBus实例
        SerialBus serialBus = mapSerialBus.get(chNo);
        // 检查实例是否存在
        if (serialBus == null) return;
        // 调用SerialBus的标题变更方法
        serialBus.OnTitleChange(chNo, serialBusType);
    }

    /**
     * 数据变更回调
     * <p>
     * 功能：当串行总线数据更新时触发
     * 用途：将新数据传递给SerialBus实例进行解析和显示
     * 
     * @param chNo 通道编号（TChan.S1/S2/S3/S4）
     * @param bytes 字节数据缓冲区
     * @param timeToPix 时间到像素转换系数
     * @param startX 数据起始X坐标
     * @param endX 数据结束X坐标
     */
    @Override
    public void OnDataChange(int chNo, ByteBuffer bytes, long timeToPix, int startX, int endX) {
        // 获取对应的SerialBus实例
        SerialBus serialbus = mapSerialBus.get(chNo);
        // 检查实例是否存在
        if (serialbus == null) return;
        // 调用SerialBus的数据变更方法
        serialbus.OnDataChange(chNo, bytes, timeToPix, startX, endX);
    }

    /**
     * 文本数据变更回调（异步版本）
     * <p>
     * 功能：当串行总线文本数据更新时触发
     * 当前为空实现
     * 
     * @param chNo 通道编号（TChan.S1/S2/S3/S4）
     * @param bytes 文本字节数据缓冲区
     */
    @Override
    public void OnTxtDataChange(int chNo, ByteBuffer bytes) {
        // 空实现，文本数据通过OnTxtDataChangeSync处理
    }

    /**
     * 文本数据变更回调（同步版本）
     * <p>
     * 功能：同步处理串行总线文本数据
     * 用途：将文本数据放入SerialBus的处理队列
     * 
     * @param chNo 通道编号（TChan.S1/S2/S3/S4）
     * @param bytes 文本字节数据缓冲区
     */
    public void OnTxtDataChangeSync(int chNo, ByteBuffer bytes) {
        // 获取对应的SerialBus实例
        SerialBus serialbus = mapSerialBus.get(chNo);
        try {
            // 将字节数据放入处理队列
            serialbus.putBytesToQueue(bytes);
        } catch (InterruptedException e) {
            e.printStackTrace(); // 打印中断异常
        }
    }

    // ==================== Observer数据接收 ====================
    
    /**
     * 过滤线程实例
     * <p>
     * 用途：在独立线程中处理串行数据解析
     * 线程名："BUS"
     * 任务：调用serialBusUpdateParseToScreen()方法
     */
    private FilterThread filterThread = new FilterThread("BUS");

    /**
     * 事件UI观察者
     * <p>
     * 功能：监听串行数据更新事件并触发相应处理
     * <p>
     * 监听事件：
     * 1. EVENT_SERIAL_UPDATE：串行图像数据更新事件
     *    - 触发FilterThread执行数据解析
     * 2. EVENT_SERIAL_TXT_UPDATE：串行文本数据更新事件
     *    - 直接处理文本数据
     * <p>
     * 数据流向：
     * EventFactory → eventUIObserver → FilterThread/OnTxtDataChangeSync
     */
    private Observer eventUIObserver = new Observer() {
        @Override
        public void update(Observable observable, Object data) {
            // 将数据转换为EventBase对象
            EventBase eventBase = (EventBase) data;
            // 根据事件类型分发处理
            switch (eventBase.getId()) {
                case EventFactory.EVENT_SERIAL_UPDATE:
                    // 串行图像数据更新：触发FilterThread执行
                    filterThread.run();
                    break;
                case EventFactory.EVENT_SERIAL_TXT_UPDATE: {
                    // 串行文本数据更新：直接处理
                    // 获取SerialChannel对象
                    SerialChannel sc = (SerialChannel) eventBase.getData();
                    // 获取SerialData对象
                    SerialData sd = (SerialData) sc.obtain();
                    // 转换为UI通道编号
                    int ch = TChan.toUiChNo(sc.getChId());
                    // 检查数据是否有效
                    if (sd != null) {
                        // 同步处理文本数据
                        OnTxtDataChangeSync(ch, sd.getByteBuffer());
                        // 回收SerialData对象
                        sc.recycle(sd);
                    }
                }
                break;
            }
        }
    };

    /**
     * 串行总线数据解析并更新到屏幕
     * <p>
     * 功能：
     * 1. 从SerialChannel获取原始串行数据
     * 2. 将数据复制到DataBean缓冲区
     * 3. 将数据添加到双缓冲图像缓存
     * 4. 发送Handler消息触发UI更新
     * <p>
     * 执行线程：FilterThread（"BUS"线程）
     * <p>
     * 数据流向：
     * SerialChannel → DataBean → SerialImageDoubleCache → DrawSerialUIHandler
     * <p>
     * 线程安全：
     * 使用synchronized(lock)保护DataBean数据访问
     */
    private void serialBusUpdateParseToScreen() {
        // 注释掉的旧代码：单独处理S1和S2通道（已废弃）
//        if (ChannelFactory.isChOpen(ChannelFactory.S1) == true ) {
//            SerialChannel sc = ChannelFactory.getSerialChannel(ChannelFactory.S1);
//            SerialData sd = (SerialData) sc.obtain();
//            if (sd != null) {
//                synchronized (lock) {
//                        serialByteBufferS1.clear();
//                        sd.getByteBuffer().position(0);
//                        serialByteBufferS1.put(sd.getByteBuffer());
//                        serialByteBufferS1.limit(sd.getByteBuffer().limit());
//                        timeToPixS1 = sd.getTimePrePix();
//                        startXS1 = sd.getStartX();
//                        endXS1 = sd.getEndX();
//
//                        addSerialImageBuffer(TChan.S1,serialByteBufferS1,startXS1,endXS1,timeToPixS1);
////                    Logger.i(TAG,"接收数据S1："+ SerialBusTxtStructParse.getDebugBytesToString(serialByteBufferS1,0,8));
//                }
//            }
//            if (sc!=null && sd != null) sc.recycle(sd);
//            drawSerialUIHandle.sendEmptyMessage(SerialBus_IMGS1);
//        }
//
//        if (ChannelFactory.isChOpen(ChannelFactory.S2) == true ) {
//            SerialChannel sc = ChannelFactory.getSerialChannel(ChannelFactory.S2);
//            SerialData sd = (SerialData) sc.obtain();
//            if (sd != null) {
//                synchronized (lock) {
//                        serialByteBufferS2.clear();
//                        sd.getByteBuffer().position(0);
//                        serialByteBufferS2.put(sd.getByteBuffer());
//                        serialByteBufferS2.limit(sd.getByteBuffer().limit());
//                        timeToPixS2 = sd.getTimePrePix();
//                        startXS2 = sd.getStartX();
//                        endXS2 = sd.getEndX();
//                        addSerialImageBuffer(TChan.S2,serialByteBufferS2,startXS2,endXS2,timeToPixS2);
////                    Logger.i(TAG,"接收数据S2："+ SerialBusTxtStructParse.getDebugBytesToString(serialByteBufferS2,0,8));
//                }
//            }
//            if (sc!=null && sd != null) sc.recycle(sd);
//            drawSerialUIHandle.sendEmptyMessage(SerialBus_IMGS2);
//        }
        
        // 新代码：使用TChan.foreachSerial遍历所有串行通道
        TChan.foreachSerial((serialCh) -> {
            // 检查通道是否开启
            if (ChannelFactory.isChOpen(TChan.toFpgaChNo(serialCh)) == true) {
                // 获取SerialChannel实例
                SerialChannel sc = ChannelFactory.getSerialChannel(TChan.toFpgaChNo(serialCh));
                // 获取SerialData对象
                SerialData sd = (SerialData) sc.obtain();
                // 检查数据是否有效
                if (sd != null) {
                    // 同步块：保护DataBean数据访问
                    synchronized (lock) {
                        // 清空DataBean的字节缓冲区
                        dataBean[serialCh - TChan.S1].SerialByteBuffer.clear();
                        // 重置SerialData的ByteBuffer位置
                        sd.getByteBuffer().position(0);
                        // 将SerialData的数据复制到DataBean
                        dataBean[serialCh - TChan.S1].SerialByteBuffer.put(sd.getByteBuffer());
                        // 设置DataBean的字节缓冲区限制
                        dataBean[serialCh - TChan.S1].SerialByteBuffer.limit(sd.getByteBuffer().limit());
                        // 设置时间到像素转换系数
                        dataBean[serialCh - TChan.S1].timeToPix = sd.getTimePrePix();
                        // 设置起始X坐标
                        dataBean[serialCh - TChan.S1].startX = sd.getStartX();
                        // 设置结束X坐标
                        dataBean[serialCh - TChan.S1].endX = sd.getEndX();
                        // 将数据添加到双缓冲图像缓存
                        addSerialImageBuffer(serialCh, dataBean[serialCh - TChan.S1]);
//                    Logger.i(TAG,"接收数据S2："+ SerialBusTxtStructParse.getDebugBytesToString(serialByteBufferS2,0,8));
                    }
                }
                // 回收SerialData对象
                if (sc != null && sd != null) sc.recycle(sd);
                // 发送Handler消息触发UI更新
                drawSerialUIHandle.sendEmptyMessage(serialCh);
            }
        });
    }

    /**
     * 添加串行图像数据到双缓冲
     * <p>
     * 功能：
     * 1. 从双缓冲获取当前显示缓冲区
     * 2. 将DataBean数据复制到缓冲区
     * 3. 设置缓冲区的元数据（时间系数、坐标范围）
     * 4. 将缓冲区放回双缓冲
     * <p>
     * 双缓冲机制：
     * 使用SerialImageDoubleCache实现无锁数据交换，避免解析线程和渲染线程冲突
     * 
     * @param iwaveCh 波形通道编号（TChan.S1/S2/S3/S4）
     * @param dataBean 数据传输对象，包含字节数据和元数据
     */
    private void addSerialImageBuffer(int iwaveCh, DataBean dataBean) {
        // 检查数据有效性
        if (dataBean.SerialByteBuffer != null && dataBean.SerialByteBuffer.limit() != 0 && dataBean.SerialByteBuffer.capacity() != 0) {
            // 从双缓冲获取当前显示缓冲区
            SerialImageBuffer sib = SerialImageDoubleCache.getInstance().getCacheShowed(iwaveCh);
            // 获取缓冲区的ByteBuffer
            ByteBuffer bb = sib.getBytes();
            // 重置DataBean的ByteBuffer位置
            dataBean.SerialByteBuffer.position(0);
            // 清空目标缓冲区
            bb.clear();
            // 复制数据
            bb.put(dataBean.SerialByteBuffer);
            // 设置缓冲区限制
            bb.limit(dataBean.SerialByteBuffer.limit());
            // 重置位置
            bb.position(0);
            // 设置ByteBuffer
            sib.setBytes(bb);
            // 设置结束X坐标
            sib.setEndX(dataBean.endX);
            // 设置起始X坐标
            sib.setStartX(dataBean.startX);
            // 设置时间到像素转换系数
            sib.setTimeToPix(dataBean.timeToPix);
            // 标记为未处理
            sib.setDeal(false);
            // 将缓冲区放回双缓冲
            SerialImageDoubleCache.getInstance().put(iwaveCh, sib);
//            Logger.i(TAG,"iwaveCh:"+iwaveCh+" sib:"+sib.toString());
//             Logger.i(TAG,"CH:"+iwaveCh+" ,key:"+sib.getKey()+" ,bytes:"+SerialBusTxtStructParse.getDebugBytesToString(sib.getBytes(),0,8));
        }
    }

    // ==================== 内部Handler类 ====================
    
    /**
     * 绘制串行UI Handler
     * <p>
     * 功能：在主线程中处理串行总线UI更新
     * <p>
     * 消息类型：
     * - SerialBus_S1：更新S1通道
     * - SerialBus_S2：更新S2通道
     * - SerialBus_S3：更新S3通道
     * - SerialBus_S4：更新S4通道
     * <p>
     * 线程安全：
     * 使用synchronized(lock)保护DataBean数据访问
     */
    class DrawSerialUIHandle extends Handler {
        /**
         * 处理消息
         * <p>
         * 根据消息类型更新对应通道的串行总线显示
         * 
         * @param msg Message对象，what字段标识通道编号
         */
        @Override
        public void handleMessage(Message msg) {
            // 注释掉的旧代码：单独处理S1和S2通道（已废弃）
//                case SerialBus_S1: {
//                    synchronized (lock) {
//                        DataBean bean= dataBean[msg.what-TChan.S1];
//                        OnDataChange(msg.what, serialByteBufferS1, timeToPixS1, startXS1, endXS1);
//                    }
//                }
//                break;
//                case SerialBus_S2: {
//                    synchronized (lock) {
//                        OnDataChange(msg.what, serialByteBufferS2, timeToPixS2, startXS2, endXS2);
//                    }
//                }
//                break;
            
            // 新代码：统一处理所有通道
            switch (msg.what) {
                case SerialBus_S1: // S1通道
                case SerialBus_S2: // S2通道
                case SerialBus_S3: // S3通道
                case SerialBus_S4: {
                    // 同步块：保护DataBean数据访问
                    synchronized (lock) {
                        // 获取对应的DataBean
                        DataBean bean = dataBean[msg.what - TChan.S1];
                        // 调用数据变更回调
                        OnDataChange(msg.what, bean.SerialByteBuffer, bean.timeToPix, bean.startX, bean.endX);
                    }
                }
                break;
                // 注释掉的旧代码：文本数据处理（已废弃）
//                case SerialBus_TXTS1: {
//                }
//                break;
//                case SerialBus_TXTS2: {
//                }
//                break;
            }
        }
    }

    /**
     * 更改串行矩形高度
     * <p>
     * 功能：通知所有串行总线实例更新显示矩形高度
     * 用途：响应屏幕尺寸变化或配置变更
     */
    public void changeSerialRect() {
        // 遍历所有串行总线实例
        for (SerialBus c : mapSerialBus.values()) {
            c.changeRectHeight(); // 更新矩形高度
        }
    }
}