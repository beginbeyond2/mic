package com.micsig.tbook.tbookscope.wavezone.wave.wavedata; // 串口总线解码数据包，包含协议解析、数据结构定义和缓存管理


import com.micsig.tbook.ui.wavezone.IWave; // 波形接口，定义通道基本操作
import com.micsig.tbook.ui.wavezone.TChan; // 通道号常量定义，S1=25, S2=26, S3=27, S4=28, MaxSerial=4

import java.nio.ByteBuffer; // NIO字节缓冲区，用于分配和管理FPGA原始二进制数据
import java.util.HashMap; // 哈希表，用于按Key(Cache1/Cache2)索引缓冲区

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                        SerialImageDoubleCache                                │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：wavedata包 → 串口图像双缓冲缓存管理器                              │
 * │ 核心职责：为每个串口通道(S1-S4)维护双缓冲(Cache1/Cache2)，支持图像渲染时      │
 * │          的乒乓缓冲切换，避免解析线程与绘制线程的数据竞争                      │
 * │ 架构设计：DCL(Double-Check Locking)单例模式，按通道索引数组管理多通道缓存，   │
 * │          每个通道包含两个SerialImageBuffer（Cache1/Cache2）构成乒乓缓冲对     │
 * │ 数据流：  FPGA二进制数据 → SerialBusStructParse解析 → SerialImageBuffer      │
 * │          → SerialImageDoubleCache(双缓冲存储) → Canvas绘制渲染               │
 * │ 依赖关系：依赖TChan(通道号定义)、SerialImageBuffer(缓冲区数据载体)           │
 * │          被SerialBusStructParse(解析器)写入、被Canvas渲染线程读取            │
 * │ 使用场景：串口总线解码图像渲染时，解析线程向空闲缓冲区写入数据，              │
 * │          绘制线程从非doing状态的缓冲区读取数据，实现无锁乒乓切换              │
 * └──────────────────────────────────────────────────────────────────────────────┘
 *
 * 串口图像双缓冲缓存管理器。采用DCL单例模式，为S1-S4共4个串口通道各维护
 * 一对双缓冲(Cache1/Cache2)，用于串口总线解码图像的乒乓缓冲切换。
 * 解析线程将数据写入空闲缓冲区（doing=false），绘制线程从非doing缓冲区读取，
 * 从而避免解析与渲染之间的数据竞争。
 */
public class SerialImageDoubleCache {

    /** 日志标签，用于Logcat调试输出 */
    private static final String TAG="SerialImageDoubleCache"; // 日志TAG，取值："SerialImageDoubleCache"

    /** 每个缓冲区的最大容量（字节），1MB = 1024*1024 = 1048576字节 */
    private static final int MaxCache=1024*1024; // 单缓冲区最大容量，取值：1048576(1MB)

    /** 双缓冲键名：缓冲区1，用于HashMap中的键标识 */
    private static final String Cache1="Cache1"; // 缓冲区1标识键，取值："Cache1"

    /** 双缓冲键名：缓冲区2，用于HashMap中的键标识 */
    private static final String Cache2="Cache2"; // 缓冲区2标识键，取值："Cache2"

    /** 多通道双缓冲数组，数组长度=MaxSerial(4)，每个元素是一个HashMap，
     *  包含Cache1和Cache2两个SerialImageBuffer，构成乒乓缓冲对 */
    private HashMap<String,SerialImageBuffer>[] cache=new HashMap[TChan.MaxSerial]; // 通道缓冲区数组，索引0=S1, 1=S2, 2=S3, 3=S4

    /** 每个通道最近一次写入的缓冲区引用，用于快速获取最新数据 */
    private SerialImageBuffer[] lastCache=new SerialImageBuffer[TChan.MaxSerial]; // 最近写入缓冲区数组，索引0=S1, 1=S2, 2=S3, 3=S4

    /** DCL单例同步锁对象，用于getInstance()的双重检查锁定 */
    private static final Object syncLock=new Object(); // 单例同步锁，保证多线程下只创建一个实例

    /** 单例实例引用，volatile语义由synchronized保证 */
    private static SerialImageDoubleCache instance; // 单例实例，初始null，首次调用getInstance()时创建

    /**
     * 获取SerialImageDoubleCache的单例实例（DCL双重检查锁定模式）。
     *
     * 功能：通过双重检查锁定(Double-Check Locking)确保多线程环境下只创建一个实例。
     *       第一次检查避免不必要的同步开销，第二次检查在锁内确保只有一个实例被创建。
     *
     * @return SerialImageDoubleCache单例实例
     */
    public static SerialImageDoubleCache getInstance() { // 获取单例实例
        if (instance==null) { // 第一次检查：实例是否为空，避免不必要的同步
            synchronized (syncLock){ // 加锁，保证只有一个线程能进入创建逻辑
                if (instance==null){ // 第二次检查：锁内再次判断，防止多个线程同时通过第一次检查
                    instance=new SerialImageDoubleCache(); // 创建单例实例
                }
            }
        }
        return instance; // 返回单例实例
    }

    /**
     * 构造函数，初始化所有串口通道的双缓冲区。
     *
     * 功能：遍历S1-S4共4个串口通道，为每个通道创建包含Cache1和Cache2
     *       两个SerialImageBuffer的HashMap，每个缓冲区分配1MB的ByteBuffer。
     *       初始状态下所有缓冲区的timeToPix=0、startX=0、endX=0，
     *       表示尚未填充有效数据。
     *
     * 业务意义：在示波器启动或串口解码功能初始化时，预分配所有通道的
     *           图像缓冲区内存，避免渲染时动态分配导致的卡顿。
     */
    public SerialImageDoubleCache(){ // 构造函数，初始化所有通道的双缓冲区
        TChan.foreachSerial((ch)->{ // 遍历S1到S4所有串口通道
            int idx=ch-TChan.S1; // 计算数组索引：S1→0, S2→1, S3→2, S4→3
            cache[idx]=new HashMap<>(); // 为当前通道创建缓冲区HashMap
            SerialImageBuffer buffer=new SerialImageBuffer(Cache1,ByteBuffer.allocate(MaxCache),0,0,0); // 创建Cache1缓冲区，分配1MB内存，初始参数均为0
            cache[idx].put(Cache1,buffer); // 将Cache1缓冲区存入当前通道的HashMap
            buffer=new SerialImageBuffer(Cache2,ByteBuffer.allocate(MaxCache),0,0,0); // 创建Cache2缓冲区，分配1MB内存，初始参数均为0
            cache[idx].put(Cache2,buffer); // 将Cache2缓冲区存入当前通道的HashMap
        });

    }

    /**
     * 获取指定通道最近一次写入的缓冲区。
     *
     * 功能：返回指定串口通道最近一次通过put方法写入的SerialImageBuffer引用。
     *       用于获取最新一帧解码图像数据，供渲染线程绘制。
     *
     * @param tChan 通道号，TChan.S1(25)~TChan.S4(28)为有效串口通道，
     *             非串口通道号时默认返回S1通道的最近缓冲区
     * @return 最近一次写入的SerialImageBuffer，若从未写入则返回null
     *
     * 业务意义：渲染线程通过此方法获取最新帧数据进行画面更新，
     *           确保显示的是最新解码结果而非过期数据。
     */
    public SerialImageBuffer getLastCache(int tChan) { // 获取最近写入的缓冲区
        if (TChan.isSerial(tChan)) { // 检查通道号是否为有效串口通道(S1-S4)
            int idx = tChan - TChan.S1; // 计算数组索引：S1→0, S2→1, S3→2, S4→3
            return lastCache[idx]; // 返回指定通道的最近缓冲区
        }
        return lastCache[0]; // 非串口通道号时，默认返回S1通道的最近缓冲区（容错处理）
    }

    /**
     * 获取指定通道的双缓冲HashMap。
     *
     * 功能：返回指定串口通道包含Cache1和Cache2两个SerialImageBuffer的HashMap，
     *       允许外部直接操作缓冲区（如查询状态、切换缓冲区等）。
     *
     * @param tChan 通道号，TChan.S1(25)~TChan.S4(28)为有效串口通道，
     *             非串口通道号时默认返回S1通道的缓冲区
     * @return 包含Cache1和Cache2两个键值对的HashMap，
     *         键为"Cache1"/"Cache2"，值为对应的SerialImageBuffer
     *
     * 业务意义：供解析线程和渲染线程直接访问双缓冲区，
     *           用于判断哪个缓冲区空闲可写入、哪个缓冲区正在使用中。
     */
    public HashMap<String, SerialImageBuffer> getCache(int tChan){ // 获取通道的双缓冲HashMap
        if (TChan.isSerial(tChan)){ // 检查通道号是否为有效串口通道(S1-S4)
            int idx=tChan-TChan.S1; // 计算数组索引：S1→0, S2→1, S3→2, S4→3
            return cache[idx]; // 返回指定通道的双缓冲HashMap
        }
        return cache[0]; // 非串口通道号时，默认返回S1通道的双缓冲HashMap（容错处理）
    }

    /**
     * 获取指定通道中已完成解析显示的缓冲区（乒乓切换核心方法）。
     *
     * 功能：返回指定通道中当前未被解析线程占用（doing=false）的缓冲区，
     *       即已经完成数据填充、可供渲染线程安全读取的缓冲区。
     *       如果Cache1未被占用则返回Cache1，否则返回Cache2。
     *
     * 乒乓切换原理：解析线程在写入数据时将doing设为true，写入完成后设为false；
     *              渲染线程通过此方法获取doing=false的缓冲区进行绘制，
     *              确保不会读取到正在被写入的不完整数据。
     *
     * @param tChan 通道号，TChan.S1(25)~TChan.S4(28)为有效串口通道，
     *             非串口通道号时默认使用S1通道
     * @return 当前未被解析线程占用的SerialImageBuffer（doing=false），
     *         优先返回Cache1，Cache1被占用时返回Cache2
     *
     * 业务意义：渲染线程调用此方法获取可安全读取的缓冲区，
     *           实现解析与渲染的乒乓切换，保证画面不闪烁、数据不撕裂。
     */
    //返回已经解析显示完的缓冲区
    public SerialImageBuffer getCacheShowed(int tChan){ // 获取已完成解析的缓冲区（乒乓切换）
        if (TChan.isSerial(tChan)){ // 检查通道号是否为有效串口通道(S1-S4)
            int idx=tChan-TChan.S1; // 计算数组索引：S1→0, S2→1, S3→2, S4→3
            if (!cache[idx].get(Cache1).isDoing()){ // Cache1是否未被解析线程占用（doing=false）
                return cache[idx].get(Cache1); // Cache1空闲，返回Cache1供渲染线程读取
            }else { // Cache1正在被解析线程占用（doing=true）
                return cache[idx].get(Cache2); // 返回Cache2供渲染线程读取
            }
        }else { // 非串口通道号的容错处理
            if (!cache[0].get(Cache1).isDoing()){ // S1通道Cache1是否未被占用
                return cache[0].get(Cache1); // S1通道Cache1空闲，返回Cache1
            }else { // S1通道Cache1正在被占用
                return cache[0].get(Cache2); // 返回S1通道Cache2
            }
        }

    }

    /**
     * 按指定键名将缓冲区放入指定通道的缓存HashMap中。
     *
     * 功能：将SerialImageBuffer以指定的key存入指定通道的HashMap中，
     *       通常用于替换Cache1或Cache2对应的缓冲区实例。
     *
     * @param tChan              通道号，TChan.S1(25)~TChan.S4(28)
     * @param key                缓冲区键名，通常为"Cache1"或"Cache2"
     * @param serialImageBuffer  要存入的串口图像缓冲区实例
     * @return 当前SerialImageDoubleCache实例（支持链式调用）
     *
     * 业务意义：当需要替换某个通道的特定缓冲区（如重新分配内存）时使用，
     *           不更新lastCache引用。
     */
    public SerialImageDoubleCache put(int tChan,String key, SerialImageBuffer serialImageBuffer){ // 按键名存入缓冲区
        int idx=tChan-TChan.S1; // 计算数组索引：S1→0, S2→1, S3→2, S4→3
        cache[idx].put(key,serialImageBuffer); // 将缓冲区以指定key存入对应通道的HashMap
        return this; // 返回自身，支持链式调用
    }

    /**
     * 将缓冲区放入指定通道的缓存，并更新最近写入引用。
     *
     * 功能：将SerialImageBuffer以其自身的key存入指定通道的HashMap，
     *       同时更新lastCache数组中对应通道的最近写入引用。
     *       这是数据写入的主要入口方法。
     *
     * @param tChan              通道号，TChan.S1(25)~TChan.S4(28)
     * @param serialImageBuffer  要存入的串口图像缓冲区实例，其getKey()返回"Cache1"或"Cache2"
     * @return 当前SerialImageDoubleCache实例（支持链式调用）
     *
     * 业务意义：解析线程完成一帧数据解析后，将结果封装为SerialImageBuffer
     *           并通过此方法写入双缓冲区，同时更新lastCache以便渲染线程
     *           通过getLastCache()快速获取最新帧。
     */
    //添加
    public SerialImageDoubleCache put(int tChan, SerialImageBuffer serialImageBuffer){ // 存入缓冲区并更新最近引用
        int idx=tChan-TChan.S1; // 计算数组索引：S1→0, S2→1, S3→2, S4→3
        cache[idx].put(serialImageBuffer.getKey(),serialImageBuffer); // 以缓冲区自身的key(Cache1/Cache2)存入HashMap
        lastCache[idx]=serialImageBuffer; // 更新该通道的最近写入缓冲区引用
        return this; // 返回自身，支持链式调用
    }
}
