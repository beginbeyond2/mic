package com.micsig.tbook.scope.fpga;  // 包声明：FPGA寄存器管理模块

/**
 * FPGA自动电压测量状态寄存器类
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件控制层 - FPGA状态寄存器读取</li>
 *   <li>设计模式：继承FPGAReg基类，封装状态寄存器读取逻辑</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>读取FPGA自动测量的电压值（最大值/最小值）</li>
 *   <li>解析32位寄存器数据为16位最大值和最小值</li>
 *   <li>支持多通道独立测量（CH1-CH4）</li>
 *   <li>配合自动测量功能（Auto功能）使用</li>
 * </ul>
 *
 * <p><b>数据格式：</b>
 * <pre>
 * 32位寄存器数据结构：
 * ┌────────────────┬────────────────┐
 * │  高16位 (bit31-16)  │  低16位 (bit15-0)   │
 * │    最小值 (Min)      │    最大值 (Max)     │
 * └────────────────┴────────────────┘
 *
 * 每个通道独立寄存器地址：
 * - FPGA_AUTO_V1 (0xD2): 通道1电压测量值
 * - FPGA_AUTO_V2 (0xD3): 通道2电压测量值
 * - FPGA_AUTO_V3 (0xD4): 通道3电压测量值
 * - FPGA_AUTO_V4 (0xD5): 通道4电压测量值
 * </pre>
 *
 * <p><b>业务场景：</b>
 * <ol>
 *   <li>用户按下Auto键，触发自动测量</li>
 *   <li>FPGA自动测量信号的最大值和最小值</li>
 *   <li>FPGACommand检测到自动测量完成中断</li>
 *   <li>创建本类实例读取各通道电压测量结果</li>
 *   <li>解析数据并更新UI显示</li>
 * </ol>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：extends FPGAReg（FPGA寄存器基类）</li>
 *   <li>调用者：FPGACommand（FPGA命令管理类）</li>
 *   <li>关联：FPGAReg_STATUS_AUTO_CYCLE（周期测量寄存器）</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 创建寄存器实例（读取通道1的电压测量值）
 * FPGAReg_STATUS_AUTO_V statusAutoV = new FPGAReg_STATUS_AUTO_V(FPGAReg.FPGA_AUTO_V1);
 * statusAutoV.setFpgaIdx(0);  // 设置FPGA索引
 *
 * // 发送读取命令并接收数据
 * recvCommand(statusAutoV, statusAutoV.getRecvReg());
 * statusAutoV.onRecv();  // 处理接收数据
 *
 * // 获取测量结果
 * int maxValue = statusAutoV.getMaxVal();    // 获取最大值（16位）
 * int minValue = statusAutoV.getMinVal();    // 获取最小值（16位）
 * </pre>
 *
 * @author zhuzh
 * @since 2018-7-11
 * @see FPGAReg
 * @see FPGACommand
 */
public class FPGAReg_STATUS_AUTO_V extends FPGAReg {

    /**
     * 接收寄存器对象
     *
     * <p><b>业务含义：</b>
     * 用于存储从FPGA接收到的原始寄存器数据。由于FPGA寄存器读取需要先发送命令再接收数据，
     * 所以需要两个寄存器对象：父类用于发送命令，recvReg用于接收数据。
     *
     * <p><b>数据格式：</b>
     * - 字节序：小端模式（Little-Endian）
     * - 数据长度：4字节（32位）
     * - 格式：[Header(8字节)][Data(4字节)]
     */
    private FPGAReg recvReg;

    /**
     * 解析后的32位电压测量值
     *
     * <p><b>业务含义：</b>
     * 存储从FPGA接收并解析后的电压测量值。该值包含最大值和最小值两个16位数据。
     *
     * <p><b>数据结构：</b>
     * <pre>
     * bit31-16: 最小值（Min Value）
     * bit15-0:  最大值（Max Value）
     * </pre>
     *
     * <p><b>取值范围：</b>
     * - 初始值：0
     * - 有效范围：0x00000000 ~ 0xFFFFFFFF
     */
    private int val = 0;

    /**
     * 构造函数：创建自动电压测量状态寄存器实例
     *
     * <p><b>功能说明：</b>
     * 初始化寄存器对象，创建发送命令寄存器和接收数据寄存器。
     *
     * <p><b>实现细节：</b>
     * <ol>
     *   <li>调用父类构造函数，创建发送命令寄存器（bRecv=true表示接收模式）</li>
     *   <li>创建独立的接收寄存器对象，用于存储FPGA返回的数据</li>
     * </ol>
     *
     * <p><b>参数说明：</b>
     * @param addr 寄存器地址（取值范围：FPGA_AUTO_V1~V4，即0xD2~0xD5）
     *             <ul>
     *               <li>0xD2 (FPGA_AUTO_V1): 通道1电压测量寄存器</li>
     *               <li>0xD3 (FPGA_AUTO_V2): 通道2电压测量寄存器</li>
     *               <li>0xD4 (FPGA_AUTO_V3): 通道3电压测量寄存器</li>
     *               <li>0xD5 (FPGA_AUTO_V4): 通道4电压测量寄存器</li>
     *             </ul>
     */
    public FPGAReg_STATUS_AUTO_V(int addr) {
        super(addr, 4, true);  // 调用父类构造函数：地址=addr，长度=4字节，接收模式=true
        recvReg = new FPGAReg(addr,4);  // 创建接收寄存器对象：用于存储FPGA返回的原始数据
    }

    /**
     * 获取接收寄存器对象
     *
     * <p><b>功能说明：</b>
     * 返回用于接收FPGA数据的寄存器对象。在FPGACommand中，需要先调用recvCommand发送命令，
     * 然后通过此方法获取接收寄存器来读取FPGA返回的数据。
     *
     * <p><b>使用场景：</b>
     * <pre>
     * // 典型调用流程：
     * FPGAReg_STATUS_AUTO_V statusAutoV = new FPGAReg_STATUS_AUTO_V(addr);
     * recvCommand(statusAutoV, statusAutoV.getRecvReg());  // 发送命令并接收数据
     * statusAutoV.onRecv();  // 处理接收到的数据
     * </pre>
     *
     * @return FPGAReg 接收寄存器对象，包含FPGA返回的原始数据
     */
    public FPGAReg getRecvReg(){
        return recvReg;
    }

    /**
     * 处理接收到的FPGA数据
     *
     * <p><b>功能说明：</b>
     * 从接收寄存器中读取32位电压测量值，并存储到val变量中。
     * 该方法在recvCommand之后调用，用于解析FPGA返回的原始数据。
     *
     * <p><b>实现逻辑：</b>
     * <ol>
     *   <li>从recvReg的偏移量0处读取32位整数</li>
     *   <li>存储到val变量，供后续getMinVal/getMaxVal方法使用</li>
     * </ol>
     *
     * <p><b>调用时机：</b>
     * 在FPGACommand检测到自动测量完成中断后，调用recvCommand接收数据，然后调用此方法解析数据。
     *
     * <p><b>数据格式：</b>
     * <pre>
     * recvReg数据格式：
     * [Header 8字节][Data 4字节]
     *                  └─ getVal(0)读取此处
     * </pre>
     */
    public void onRecv(){
        val = recvReg.getVal(0);  // 从接收寄存器的数据区读取32位整数（偏移量0）
    }

    /**
     * 获取指定索引的最小值（按字节提取）
     *
     * <p><b>功能说明：</b>
     * 从32位val中提取指定索引位置的最小值字节。该方法用于按字节访问最小值数据。
     *
     * <p><b>数据结构：</b>
     * <pre>
     * val的位分布：
     * ┌─────────┬─────────┬─────────┬─────────┐
     * │ Byte3   │ Byte2   │ Byte1   │ Byte0   │
     * │ Min[1]  │ Min[0]  │ Max[1]  │ Max[0]  │
     * │(bit31-24)│(bit23-16)│(bit15-8)│(bit7-0) │
     * └─────────┴─────────┴─────────┴─────────┘
     *
     * idx参数说明：
     * - idx=0: 提取bit23-16（Min的低字节）
     * - idx=1: 提取bit31-24（Min的高字节）
     * </pre>
     *
     * <p><b>实现算法：</b>
     * <ol>
     *   <li>计算移位位数：8 + idx*16</li>
     *   <li>右移val，使目标字节移到最低8位</li>
     *   <li>与0xFF进行与运算，提取最低8位</li>
     * </ol>
     *
     * @param idx 字节索引（取值范围：0~1）
     *            <ul>
     *              <li>0: 提取最小值的低字节（bit23-16）</li>
     *              <li>1: 提取最小值的高字节（bit31-24）</li>
     *            </ul>
     * @return byte 最小值的指定字节（取值范围：-128~127）
     */
    public byte getMinVal(int idx){
        return (byte) ((val>>>(8+idx*16)) & 0xFF);  // 无符号右移+掩码提取目标字节
    }

    /**
     * 获取指定索引的最大值（按字节提取）
     *
     * <p><b>功能说明：</b>
     * 从32位val中提取指定索引位置的最大值字节。该方法用于按字节访问最大值数据。
     *
     * <p><b>数据结构：</b>
     * <pre>
     * val的位分布：
     * ┌─────────┬─────────┬─────────┬─────────┐
     * │ Byte3   │ Byte2   │ Byte1   │ Byte0   │
     * │ Min[1]  │ Min[0]  │ Max[1]  │ Max[0]  │
     * │(bit31-24)│(bit23-16)│(bit15-8)│(bit7-0) │
     * └─────────┴─────────┴─────────┴─────────┘
     *
     * idx参数说明：
     * - idx=0: 提取bit7-0（Max的低字节）
     * - idx=1: 提取bit15-8（Max的高字节）
     * </pre>
     *
     * <p><b>实现算法：</b>
     * <ol>
     *   <li>计算移位位数：idx*16</li>
     *   <li>右移val，使目标字节移到最低8位</li>
     *   <li>与0xFF进行与运算，提取最低8位</li>
     * </ol>
     *
     * @param idx 字节索引（取值范围：0~1）
     *            <ul>
     *              <li>0: 提取最大值的低字节（bit7-0）</li>
     *              <li>1: 提取最大值的高字节（bit15-8）</li>
     *            </ul>
     * @return byte 最大值的指定字节（取值范围：-128~127）
     */
    public byte getMaxVal(int idx){
        return (byte) ((val>>>(idx*16)) & 0xFF);  // 无符号右移+掩码提取目标字节
    }

    /**
     * 获取最小值（16位整数）
     *
     * <p><b>功能说明：</b>
     * 从32位val中提取16位最小值。该方法返回完整的最小值数据，而非单个字节。
     *
     * <p><b>数据结构：</b>
     * <pre>
     * val的位分布：
     * ┌────────────────┬────────────────┐
     * │  bit31-16      │  bit15-0       │
     * │  最小值(Min)    │  最大值(Max)   │
     * └────────────────┴────────────────┘
     *           └─ getMinVal()提取此处
     * </pre>
     *
     * <p><b>实现算法：</b>
     * <ol>
     *   <li>无符号右移16位，将高16位移到低16位</li>
     *   <li>与0xFFFF进行与运算，提取低16位</li>
     *   <li>强制转换为short类型（16位有符号整数）</li>
     * </ol>
     *
     * <p><b>业务意义：</b>
     * 该值代表自动测量检测到的信号最小电压值（ADC原始值），需要根据垂直档位和探头衰减系数
     * 转换为实际电压值。
     *
     * @return int 最小值（16位有符号整数，取值范围：-32768~32767）
     *             <p>注意：虽然返回类型是int，但实际值范围在short范围内
     */
    public int getMinVal(){
        return (short)((val >>> 16) & 0xFFFF);  // 提取高16位并转换为short
    }

    /**
     * 获取最大值（16位整数）
     *
     * <p><b>功能说明：</b>
     * 从32位val中提取16位最大值。该方法返回完整的最大值数据，而非单个字节。
     *
     * <p><b>数据结构：</b>
     * <pre>
     * val的位分布：
     * ┌────────────────┬────────────────┐
     * │  bit31-16      │  bit15-0       │
     * │  最小值(Min)    │  最大值(Max)   │
     * └────────────────┴────────────────┘
     *                              └─ getMaxVal()提取此处
     * </pre>
     *
     * <p><b>实现算法：</b>
     * <ol>
     *   <li>与0xFFFF进行与运算，提取低16位</li>
     *   <li>强制转换为short类型（16位有符号整数）</li>
     * </ol>
     *
     * <p><b>业务意义：</b>
     * 该值代表自动测量检测到的信号最大电压值（ADC原始值），需要根据垂直档位和探头衰减系数
     * 转换为实际电压值。
     *
     * @return int 最大值（16位有符号整数，取值范围：-32768~32767）
     *             <p>注意：虽然返回类型是int，但实际值范围在short范围内
     */
    public int getMaxVal(){
        return (short)(val & 0xFFFF);  // 提取低16位并转换为short
    }

}