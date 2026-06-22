package com.micsig.tbook.tbookscope.wavezone.wave.wavedata; // 串口总线解码数据包，包含协议解析、数据结构定义和缓存管理

import java.nio.ByteBuffer; // 导入ByteBuffer类，用于存储FPGA传来的二进制数据

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │                    SerialImageBuffer                            │
 * ├──────────────────────────────────────────────────────────────────┤
 * │ 模块定位：wavedata包 → 串口图像缓冲区数据载体                      │
 * │ 核心职责：封装串口总线解码后的图像绘制数据，支持双缓冲机制             │
 * │ 架构设计：作为SerialImageDoubleCache的缓冲单元，每个实例对应         │
 * │          一个通道的一帧图像数据                                     │
 * │ 数据流：  FPGA二进制数据 → SerialImageBuffer → Canvas绘制          │
 * │ 依赖关系：被SerialImageDoubleCache管理和调度                       │
 * │ 使用场景：串口总线解码图像渲染时，作为数据缓冲区在双缓存之间切换        │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * 串口图像缓冲区，封装一次串口总线解码的图像绘制所需数据。
 * 包含ByteBuffer原始数据、时间到像素的转换系数、绘制起止X坐标，
 * 以及处理状态标志（deal/doing），用于双缓冲切换控制。
 */
public class SerialImageBuffer {

    /** 同步锁对象，用于保护doing状态的线程安全访问 */
    private final Object lock=new Object(); // 同步锁，确保doing字段的读写原子性

    /** 缓冲区标识键，用于在HashMap中区分不同缓冲区（如"Cache1"/"Cache2"） */
    private String key; // 缓冲区键名，对应双缓冲中的Cache1或Cache2

    /** FPGA传来的原始二进制数据，包含串口总线解码后的像素级数据 */
    private ByteBuffer bytes; // 存储待解析和绘制的二进制协议数据

    /** 时间到像素的转换系数，用于将FPGA时间戳转换为屏幕X坐标 */
    private long timeToPix; // 时间-像素映射系数，值=采样率/屏幕宽度

    /** 绘制起始X坐标（像素），对应波形区域的左边界 */
    private int startX; // 绘制区域左边界X坐标

    /** 绘制结束X坐标（像素），对应波形区域的右边界 */
    private int endX; // 绘制区域右边界X坐标

    /** 数据是否已处理标志，true表示数据已解析可被新数据覆盖 */
    private boolean deal; // 已处理标志，true=已解析完毕可复用，false=正在使用

    /** 正在处理标志，true表示当前正在被解析线程使用 */
    private boolean doing; // 正在处理标志，true=解析线程正在操作此缓冲区

    /**
     * 构造函数，创建串口图像缓冲区实例。
     *
     * @param key       缓冲区标识键（"Cache1"或"Cache2"）
     * @param bytes     FPGA原始二进制数据
     * @param timeToPix 时间到像素的转换系数
     * @param startX    绘制起始X坐标
     * @param endX      绘制结束X坐标
     */
    public SerialImageBuffer(String key,ByteBuffer bytes,long timeToPix,int startX,int endX){
        this.key=key; // 初始化缓冲区标识键
        this.bytes=bytes; // 初始化二进制数据缓冲区
        this.timeToPix=timeToPix; // 初始化时间-像素转换系数
        this.startX=startX; // 初始化绘制起始X坐标
        this.endX=endX; // 初始化绘制结束X坐标
        this.deal=true; // 初始化为已处理状态，表示缓冲区可被新数据覆盖
        this.doing=false; // 初始化为非处理状态，表示无线程正在操作
    }

    /**
     * 获取缓冲区标识键。
     *
     * @return 缓冲区键名（"Cache1"或"Cache2"）
     */
    public String getKey() { // 获取缓冲区标识键
        return key; // 返回键名
    }

    /**
     * 设置时间到像素的转换系数。
     *
     * @param timeToPix 时间-像素映射系数
     */
    public void setTimeToPix(long timeToPix) { // 设置时间-像素转换系数
        this.timeToPix = timeToPix; // 更新转换系数
    }

    /**
     * 设置绘制起始X坐标。
     *
     * @param startX 绘制区域左边界X坐标
     */
    public void setStartX(int startX) { // 设置绘制起始X坐标
        this.startX = startX; // 更新起始X坐标
    }

    /**
     * 设置绘制结束X坐标。
     *
     * @param endX 绘制区域右边界X坐标
     */
    public void setEndX(int endX) { // 设置绘制结束X坐标
        this.endX = endX; // 更新结束X坐标
    }

    /**
     * 设置二进制数据缓冲区。
     *
     * @param bytes FPGA原始二进制数据
     */
    public void setBytes(ByteBuffer bytes){ // 设置二进制数据
        this.bytes=bytes; // 更新数据缓冲区引用
    }

    /**
     * 获取二进制数据缓冲区。
     *
     * @return ByteBuffer实例
     */
    public ByteBuffer getBytes() { // 获取二进制数据
        return bytes; // 返回数据缓冲区
    }

    /**
     * 获取时间到像素的转换系数。
     *
     * @return 时间-像素映射系数
     */
    public long getTimeToPix() { // 获取时间-像素转换系数
        return timeToPix; // 返回转换系数
    }

    /**
     * 获取绘制起始X坐标。
     *
     * @return 绘制区域左边界X坐标
     */
    public int getStartX() { // 获取绘制起始X坐标
        return startX; // 返回起始X坐标
    }

    /**
     * 获取绘制结束X坐标。
     *
     * @return 绘制区域右边界X坐标
     */
    public int getEndX() { // 获取绘制结束X坐标
        return endX; // 返回结束X坐标
    }

    /**
     * 是否已经处理过数据。
     *
     * @return true:已处理，缓冲区可被新数据覆盖; false:未处理，数据仍在使用中
     */
    public boolean isDeal() { // 查询已处理标志
        return this.deal; // 返回处理状态
    }

    /**
     * 设置处理状态。
     *
     * @param deal true表示已处理完毕，false表示未处理
     */
    public void setDeal(boolean deal) { // 设置已处理标志
//        synchronized (lock) { // 原设计有同步保护，目前注释掉
            this.deal = deal; // 更新处理状态
//        }
    }

    /**
     * 查询是否正在处理数据（线程安全）。
     *
     * @return true:正在处理中; false:空闲
     */
    public boolean isDoing() { // 查询正在处理标志（线程安全）
        synchronized (lock) { // 加锁保证读取原子性
            return doing; // 返回处理中状态
        }
    }

    /**
     * 设置正在处理状态（线程安全）。
     *
     * @param doing true表示开始处理，false表示处理完毕
     */
    public void setDoing(boolean doing) { // 设置正在处理标志（线程安全）
        synchronized (lock) { // 加锁保证写入原子性
            this.doing = doing; // 更新处理中状态
        }
    }

    /**
     * 获取同步锁对象，供外部进行更复杂的同步控制。
     *
     * @return 锁对象
     */
    public Object Lock(){ // 获取同步锁对象
        return lock; // 返回锁引用
    }

    /**
     * 返回缓冲区的字符串描述，用于调试日志输出。
     *
     * @return 包含key、bytes、timeToPix、startX、endX、deal的描述字符串
     */
    @Override
    public String toString() { // 生成调试用字符串描述
        return // 返回拼接的描述字符串
            " key:"+ key+ // 缓冲区标识键
           "  bytes:"+ bytes+ // 二进制数据缓冲区信息
           "  timeToPix:"+timeToPix+ // 时间-像素转换系数
           "  startX:"+startX+ // 绘制起始X坐标
           "  endX:"+endX+ // 绘制结束X坐标
           "  deal:"+deal; // 已处理标志
    }
}
