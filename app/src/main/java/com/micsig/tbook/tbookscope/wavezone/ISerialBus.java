package com.micsig.tbook.tbookscope.wavezone; // 波形显示区域包，包含示波器波形显示的核心组件，1

import java.nio.ByteBuffer; // Java字节缓冲区类，用于高效处理二进制数据，1

/**
 * 串行总线数据监听器接口 - 示波器串行总线解码数据的回调接口
 * 
 * 【模块定位】
 * - 所属模块：wavezone（波形显示区域模块）
 * - 核心职责：定义串行总线数据变化的回调方法，实现数据驱动的显示更新
 * - 架构层级：接口层，位于数据层和显示层之间
 * 
 * 【核心职责】
 * 1. 数据变化通知：当串行总线解码数据发生变化时，通过回调通知显示组件
 * 2. 标题变化通知：当串行总线类型或通道号发生变化时，通知更新显示标题
 * 3. 文本数据通知：当串行总线解码的文本数据发生变化时，通知显示组件
 * 
 * 【架构图】
 * ┌─────────────────────────────────────────────────────────────┐
 * │              SerialBusManage（串行总线管理器）                │
 * │                   (数据生产者)                               │
 * │  - 解码串行总线数据                                          │
 * │  - 解析协议格式                                              │
 * │  - 生成ByteBuffer数据                                        │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 回调通知
 * ┌─────────────────────────────────────────────────────────────┐
 * │                  ISerialBus接口                              │
 * │               (串行总线数据监听器)                            │
 * │  - OnTitleChange()  标题变化回调                             │
 * │  - OnDataChange()   数据变化回调                             │
 * │  - OnTxtDataChange() 文本数据变化回调                        │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 实现接口
 * ┌─────────────────────────────────────────────────────────────┐
 * │              SerialBus（串行总线显示类）                      │
 * │                   (数据消费者)                               │
 * │  - 接收ByteBuffer数据                                        │
 * │  - 绘制解码结果                                              │
 * │  - 更新显示标题                                              │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * 【依赖关系】
 * - 实现类：SerialBus（串行总线显示类）
 * - 调用方：SerialBusManage（串行总线管理器）
 * - 数据类型：ByteBuffer（字节缓冲区）
 * - 无外部库依赖
 * 
 * 【使用场景】
 * 1. 串行总线解码显示：当解码到新的串行总线数据时，通知显示组件更新
 * 2. 协议类型切换：当切换解码协议类型时，通知更新显示标题
 * 3. 文本模式显示：在文本模式下，通知显示解码的文本内容
 * 4. 实时数据更新：实时接收并显示解码数据流
 * 
 * 【数据流向】
 * 串行总线硬件 → FPGA解码 → SerialBusManage → ISerialBus回调 → SerialBus显示
 * 
 * 【回调时机】
 * 1. OnTitleChange：协议类型切换或通道号变化时触发
 * 2. OnDataChange：每次解码到新数据帧时触发
 * 3. OnTxtDataChange：文本模式下解码到文本数据时触发
 * 
 * 【性能考虑】
 * - ByteBuffer传递：使用ByteBuffer避免数据拷贝，提高性能
 * - 回调频率：根据解码速率实时触发，可能高频调用
 * - 线程安全：回调可能在解码线程中执行，需注意线程同步
 * 
 * 【注意事项】
 * 1. 实现类需要处理ByteBuffer的生命周期管理
 * 2. 回调方法中避免耗时操作，防止阻塞解码流程
 * 3. 需要处理数据缓冲区的边界情况
 * 4. 文本数据和二进制数据的处理逻辑不同
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/9/26
 * @see SerialBus
 * @see SerialBusManage
 * @see ByteBuffer
 */

public interface ISerialBus {
    /**
     * 标题变化回调方法 - 通知串行总线显示标题需要更新
     * 
     * 【功能说明】
     * 当串行总线的类型或通道号发生变化时，通知显示组件更新标题
     * 
     * 【参数说明】
     * @param chNo 通道号，标识哪个通道的串行总线标题发生变化
     *             取值范围：通道索引，通常为1-4
     * @param SerialBusType 串行总线类型，标识解码协议类型
     *                      取值范围：协议类型枚举值（如UART、SPI、I2C等）
     * 
     * 【调用时机】
     * - 协议类型切换时触发
     * - 通道号变化时触发
     * - 初始化显示时触发
     * 
     * 【使用示例】
     * @Override
     * public void OnTitleChange(int chNo, int SerialBusType) {
     *     String title = getProtocolTitle(SerialBusType);
     *     updateChannelTitle(chNo, title);
     * }
     */
    void OnTitleChange(int chNo,int SerialBusType); // 标题变化回调，参数为通道号和串行总线类型，1
    
    /**
     * 数据变化回调方法 - 通知串行总线解码数据需要更新显示
     * 
     * 【功能说明】
     * 当串行总线解码到新的数据帧时，通知显示组件更新显示内容
     * 
     * 【参数说明】
     * @param chNo 通道号，标识哪个通道的串行总线数据发生变化
     *             取值范围：通道索引，通常为1-4
     * @param bytes 解码数据的字节缓冲区，包含完整的解码数据帧
     *               数据格式：根据协议类型不同，包含不同的数据结构
     * @param timeToPix 时间戳到像素的转换值，用于定位数据在时间轴上的位置
     *                  取值范围：像素坐标范围内的长整型数
     * @param startX 数据起始X坐标，标识数据在屏幕上的起始位置
     *               取值范围：屏幕坐标范围内的整数
     * @param endX 数据结束X坐标，标识数据在屏幕上的结束位置
     *             取值范围：屏幕坐标范围内的整数
     * 
     * 【调用时机】
     * - 每次解码到新数据帧时触发
     * - 数据刷新周期内触发
     * - 实时解码过程中持续触发
     * 
     * 【数据格式】
     * ByteBuffer中的数据格式根据协议类型不同：
     * - UART：包含起始位、数据位、停止位等信息
     * - SPI：包含时钟、数据、片选等信息
     * - I2C：包含地址、数据、应答等信息
     * 
     * 【使用示例】
     * @Override
     * public void OnDataChange(int chNo, ByteBuffer bytes, long timeToPix, int startX, int endX) {
     *     SerialImageBuffer buffer = new SerialImageBuffer(bytes);
     *     drawDecodedData(chNo, buffer, startX, endX);
     * }
     */
    void OnDataChange(int chNo,ByteBuffer bytes,long timeToPix,int startX,int endX); // 数据变化回调，参数为通道号、字节缓冲区、时间戳、起始和结束坐标，1
    
    /**
     * 文本数据变化回调方法 - 通知串行总线文本解码数据需要更新显示
     * 
     * 【功能说明】
     * 在文本模式下，当串行总线解码到文本数据时，通知显示组件更新文本显示
     * 
     * 【参数说明】
     * @param chNo 通道号，标识哪个通道的串行总线文本数据发生变化
     *             取值范围：通道索引，通常为1-4
     * @param bytes 文本解码数据的字节缓冲区，包含解码后的文本内容
     *               数据格式：UTF-8或其他编码格式的文本数据
     * 
     * 【调用时机】
     * - 文本模式下解码到文本数据时触发
     * - 文本缓冲区更新时触发
     * - 文本显示刷新时触发
     * 
     * 【数据格式】
     * ByteBuffer中的数据为解码后的文本内容：
     * - 包含完整的文本字符串
     * - 使用UTF-8或其他编码格式
     * - 可能包含换行符等控制字符
     * 
     * 【使用示例】
     * @Override
     * public void OnTxtDataChange(int chNo, ByteBuffer bytes) {
     *     String text = parseTextFromBuffer(bytes);
     *     updateTextDisplay(chNo, text);
     * }
     */
    void OnTxtDataChange(int chNo,ByteBuffer bytes); // 文本数据变化回调，参数为通道号和文本字节缓冲区，1
} // 结束ISerialBus接口定义，1