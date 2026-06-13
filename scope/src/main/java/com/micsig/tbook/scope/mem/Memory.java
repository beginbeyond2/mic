package com.micsig.tbook.scope.mem;

import java.nio.ByteBuffer;

/**
 * 内存操作工具类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.mem（示波器内存管理模块）</li>
 *   <li>架构层级：基础设施层 - 内存操作</li>
 *   <li>设计模式：工具类模式 + JNI桥接</li>
 *   <li>职责类型：内存操作工具</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>提供高效的内存拷贝操作</li>
 *   <li>提供内存填充操作</li>
 *   <li>提供数据格式转换功能（16位转32位）</li>
 *   <li>提供内存同步功能</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>通过JNI调用本地代码实现高性能内存操作</li>
 *   <li>避免Java层面的循环操作，提高处理速度</li>
 *   <li>支持直接字节缓冲区的高效操作</li>
 *   <li>优化示波器波形数据处理性能</li>
 * </ul>
 * 
 * <p><b>JNI本地方法说明：</b>
 * <pre>
 * 本类通过JNI调用C/C++本地代码实现高性能内存操作：
 * 
 * ┌────────────────────────────────────────────────────────────────┐
 * │ Java方法        │ 本地方法        │ 功能                       │
 * ├─────────────────┼─────────────────┼────────────────────────────┤
 * │ Memcpy()        │ memcpy()        │ 内存拷贝                   │
 * │ Memset()        │ memset()        │ 内存填充                   │
 * │ Convert16to32() │ convert16to32() │ 16位转32位数据转换         │
 * │ Sync()          │ sync()          │ 内存同步                   │
 * └─────────────────┴─────────────────┴────────────────────────────┘
 * </pre>
 * 
 * <p><b>直接缓冲区说明：</b>
 * <pre>
 * 本类所有操作仅支持直接字节缓冲区：
 * 
 * 直接缓冲区：
 *   - 在堆外内存分配
 *   - JNI可以直接访问，无需拷贝
 *   - 适合大量数据操作
 *   - 示波器波形数据存储使用
 * 
 * 非直接缓冲区：
 *   - 在Java堆内存分配
 *   - JNI访问需要额外拷贝
 *   - 本类不支持此类缓冲区操作
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>波形数据从FPGA缓冲区拷贝到显示缓冲区</li>
 *   <li>波形数据缓冲区初始化和清空</li>
 *   <li>ADC原始数据格式转换</li>
 *   <li>多通道数据合并处理</li>
 * </ul>
 * 
 * <p><b>性能优势：</b>
 * <ul>
 *   <li>避免Java层面循环开销</li>
 *   <li>利用C/C++优化的内存操作函数</li>
 *   <li>支持SIMD指令加速（如果本地代码实现）</li>
 *   <li>减少GC压力</li>
 * </ul>
 * 
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>所有方法均为静态方法，无共享状态</li>
 *   <li>本地方法实现需保证线程安全</li>
 * </ul>
 * 
 * <p><b>使用示例：</b>
 * <pre>
 * // 分配直接缓冲区
 * ByteBuffer src = ByteBuffer.allocateDirect(1024);
 * ByteBuffer dst = ByteBuffer.allocateDirect(1024);
 * 
 * // 填充源缓冲区
 * Memory.Memset(src, 0, 1024, 0x55);
 * 
 * // 拷贝数据
 * Memory.Memcpy(dst, src, 1024);
 * 
 * // 同步内存
 * Memory.Sync();
 * </pre>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-4-11
 */
public class Memory {

    /**
     * 内存拷贝（简化版本）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从源缓冲区拷贝数据到目标缓冲区</li>
     *   <li>从缓冲区起始位置开始拷贝</li>
     *   <li>委托给带偏移参数的重载方法</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>波形数据整体拷贝</li>
     *   <li>缓冲区数据备份</li>
     * </ul>
     * 
     * @param dst 目标缓冲区（必须为直接缓冲区）
     * @param src 源缓冲区（必须为直接缓冲区）
     * @param length 拷贝长度（字节数）
     */
    public static void Memcpy(ByteBuffer dst,ByteBuffer src,int length){

        Memcpy(dst,0,src,0,length);
    }
    
    /**
     * 内存填充（简化版本）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将缓冲区指定范围填充为0</li>
     *   <li>委托给带填充值参数的重载方法</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>缓冲区清零</li>
     *   <li>波形数据初始化</li>
     * </ul>
     * 
     * @param dst 目标缓冲区（必须为直接缓冲区）
     * @param offset 起始偏移量
     * @param length 填充长度（字节数）
     */
    public static void Memset(ByteBuffer dst,int offset,int length){
        Memset(dst,offset,length,0);
    }
    
    /**
     * 内存填充（完整版本）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将缓冲区指定范围填充为指定值</li>
     *   <li>仅支持直接缓冲区</li>
     *   <li>通过JNI调用本地方法实现</li>
     * </ul>
     * 
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>dst：目标缓冲区，必须为直接缓冲区</li>
     *   <li>offset：起始偏移量，相对于缓冲区起始位置</li>
     *   <li>length：填充长度，单位为字节</li>
     *   <li>val：填充值，每个字节填充此值</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>非直接缓冲区调用此方法不会执行任何操作</li>
     *   <li>offset + length 不能超过缓冲区容量</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>
     * ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
     * Memory.Memset(buffer, 0, 1024, 0xFF); // 填充为0xFF
     * </pre>
     * 
     * @param dst 目标缓冲区（必须为直接缓冲区）
     * @param offset 起始偏移量
     * @param length 填充长度（字节数）
     * @param val 填充值（0-255）
     */
    public static void Memset(ByteBuffer dst, int offset,int length,int val){
        if(dst.isDirect()){
            memset(dst,offset,length,val);
        }
    }
    
    /**
     * 内存拷贝（完整版本）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从源缓冲区拷贝数据到目标缓冲区</li>
     *   <li>支持指定源和目标的偏移量</li>
     *   <li>仅支持直接缓冲区</li>
     *   <li>通过JNI调用本地方法实现</li>
     * </ul>
     * 
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>dst：目标缓冲区，必须为直接缓冲区</li>
     *   <li>dstOffset：目标缓冲区偏移量</li>
     *   <li>src：源缓冲区，必须为直接缓冲区</li>
     *   <li>srcOffset：源缓冲区偏移量</li>
     *   <li>length：拷贝长度，单位为字节</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>任一缓冲区为非直接缓冲区时不会执行任何操作</li>
     *   <li>srcOffset + length 不能超过源缓冲区容量</li>
     *   <li>dstOffset + length 不能超过目标缓冲区容量</li>
     *   <li>源和目标缓冲区重叠时行为未定义</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>
     * ByteBuffer src = ByteBuffer.allocateDirect(2048);
     * ByteBuffer dst = ByteBuffer.allocateDirect(1024);
     * // 从src偏移100处拷贝1024字节到dst起始位置
     * Memory.Memcpy(dst, 0, src, 100, 1024);
     * </pre>
     * 
     * @param dst 目标缓冲区（必须为直接缓冲区）
     * @param dstOffset 目标缓冲区偏移量
     * @param src 源缓冲区（必须为直接缓冲区）
     * @param srcOffset 源缓冲区偏移量
     * @param length 拷贝长度（字节数）
     */
    public static void Memcpy(ByteBuffer dst,int dstOffset,ByteBuffer src,int srcOffset,int length){
        if(dst.isDirect() && src.isDirect()) {
            memcpy(dst, dstOffset, src, srcOffset, length);
        }
    }
    
    /**
     * 16位数据转32位数据
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将16位数据转换为32位数据</li>
     *   <li>用于ADC原始数据格式转换</li>
     *   <li>仅支持直接缓冲区</li>
     *   <li>通过JNI调用本地方法实现</li>
     * </ul>
     * 
     * <p><b>转换逻辑：</b>
     * <pre>
     * ┌────────────────────────────────────────────────────────────────┐
     * │ 输入：16位数据（short类型）                                    │
     * │ 输出：32位数据（int类型）                                      │
     * │                                                                │
     * │ 转换公式：                                                     │
     *   output[i] = (input[i] + val) 扩展为32位                       │
     * │                                                                │
     * │ 用途：                                                         │
     * │   - ADC原始数据为16位，需要转换为32位进行处理或显示           │
     * │   - val参数用于偏移调整（如去除直流分量）                      │
     * └────────────────────────────────────────────────────────────────┘
     * </pre>
     * 
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>dst：目标缓冲区（32位），必须为直接缓冲区</li>
     *   <li>dstOffset：目标缓冲区偏移量（以32位元素为单位）</li>
     *   <li>src：源缓冲区（16位），必须为直接缓冲区</li>
     *   <li>srcOffset：源缓冲区偏移量（以16位元素为单位）</li>
     *   <li>val：偏移调整值</li>
     *   <li>length：转换元素个数</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>目标缓冲区容量需要是源缓冲区的两倍</li>
     *   <li>偏移量以元素为单位，不是字节</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>ADC原始数据后处理</li>
     *   <li>波形数据格式转换</li>
     *   <li>数据精度提升</li>
     * </ul>
     * 
     * @param dst 目标缓冲区（32位，必须为直接缓冲区）
     * @param dstOffset 目标缓冲区偏移量（元素单位）
     * @param src 源缓冲区（16位，必须为直接缓冲区）
     * @param srcOffset 源缓冲区偏移量（元素单位）
     * @param val 偏移调整值
     * @param length 转换元素个数
     */
    public static void Convert16to32(ByteBuffer dst,int dstOffset,ByteBuffer src,int srcOffset,int val,int length){
        if(dst.isDirect() && src.isDirect()){
            convert16to32(dst,dstOffset,src,srcOffset,val,length);
        }
    }
    
    /**
     * 内存同步
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>同步CPU缓存与内存</li>
     *   <li>确保所有挂起的内存写操作完成</li>
     *   <li>通过JNI调用本地方法实现</li>
     * </ul>
     * 
     * <p><b>设计目的：</b>
     * <ul>
     *   <li>在多线程或多进程环境下确保内存一致性</li>
     *   <li>在DMA操作前后确保数据同步</li>
     *   <li>在FPGA数据传输前后确保数据可见</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>FPGA写入波形数据后，同步确保数据可见</li>
     *   <li>多线程共享内存数据时</li>
     *   <li>关键数据写入后确保持久化</li>
     * </ul>
     * 
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>此操作可能有性能开销</li>
     *   <li>仅在必要时调用</li>
     * </ul>
     */
    public static void Sync(){
        sync();
    }

    /**
     * 本地方法：内存拷贝
     * 
     * <p><b>实现说明：</b>
     * <ul>
     *   <li>通过JNI调用C/C++实现的memcpy</li>
     *   <li>使用指针直接操作内存</li>
     * </ul>
     * 
     * @param dst 目标缓冲区
     * @param dstOffset 目标偏移量
     * @param src 源缓冲区
     * @param srcOffset 源偏移量
     * @param length 拷贝长度
     */
    private static native void memcpy(ByteBuffer dst,int dstOffset,ByteBuffer src,int srcOffset,int length);
    
    /**
     * 本地方法：内存填充
     * 
     * <p><b>实现说明：</b>
     * <ul>
     *   <li>通过JNI调用C/C++实现的memset</li>
     *   <li>使用指针直接操作内存</li>
     * </ul>
     * 
     * @param dst 目标缓冲区
     * @param dstOffset 起始偏移量
     * @param length 填充长度
     * @param val 填充值
     */
    private static native void memset(ByteBuffer dst,int dstOffset,int length,int val);
    
    /**
     * 本地方法：16位转32位数据转换
     * 
     * <p><b>实现说明：</b>
     * <ul>
     *   <li>通过JNI调用C/C++实现的数据转换</li>
     *   <li>可能使用SIMD指令优化</li>
     * </ul>
     * 
     * @param dst 目标缓冲区
     * @param dstOffset 目标偏移量
     * @param src 源缓冲区
     * @param srcOffset 源偏移量
     * @param val 偏移调整值
     * @param length 转换元素个数
     */
    private static native void convert16to32(ByteBuffer dst,int dstOffset,ByteBuffer src,int srcOffset,int val,int length);
    
    /**
     * 本地方法：内存同步
     * 
     * <p><b>实现说明：</b>
     * <ul>
     *   <li>通过JNI调用C/C++实现的内存屏障</li>
     *   <li>可能调用msync或类似系统调用</li>
     * </ul>
     */
    private static native void sync();
}
