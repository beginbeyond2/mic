package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

/**
 * FPGA DNA读取寄存器类 - 用于读取示波器FPGA芯片的唯一DNA标识
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器读取</li>
 *   <li>设计模式：继承模式（继承FPGAReg基类）+ 双缓冲模式</li>
 *   <li>职责类型：硬件标识读取器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>读取FPGA芯片的唯一DNA标识</li>
 *   <li>提供64位DNA值的组合读取</li>
 *   <li>管理接收缓冲区数据</li>
 *   <li>支持设备唯一性识别</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>获取设备唯一标识符用于设备管理</li>
 *   <li>支持设备授权和许可证验证</li>
 *   <li>支持设备追踪和溯源</li>
 *   <li>支持设备序列号生成</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * DNA读取寄存器（8字节，64位）
 * │
 * ├── 低32位（第一个4字节）
 * │     └── DNA低32位数据
 * │
 * └── 高32位（第二个4字节）
 *       └── DNA高32位数据
 *
 * 注意：这是一个只读寄存器，使用双缓冲模式
 * </pre>
 *
 * <p><b>DNA说明：</b>
 * <ul>
 *   <li>DNA是FPGA芯片出厂时烧录的唯一标识符</li>
 *   <li>每个FPGA芯片的DNA都是全球唯一的</li>
 *   <li>DNA通常为64位，由厂商保证唯一性</li>
 *   <li>可用于设备序列号、授权验证等场景</li>
 * </ul>
 *
 * <p><b>双缓冲模式：</b>
 * <ul>
 *   <li>发送寄存器：用于发送读取命令（地址为0，大小为8字节）</li>
 *   <li>接收寄存器（recvReg）：用于存储从FPGA读取的DNA数据</li>
 *   <li>构造函数第三个参数true表示这是一个读取类型的寄存器</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>设备启动：读取DNA用于设备初始化</li>
 *   <li>授权验证：验证设备是否有合法授权</li>
 *   <li>序列号生成：基于DNA生成设备序列号</li>
 *   <li>设备追踪：记录设备DNA用于追踪和管理</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 *   <li>依赖：LicenseManager（授权管理类）</li>
 * </ul>
 *
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>寄存器操作在FPGACommand中同步执行</li>
 *   <li>DNA读取通常在设备初始化时完成，无需并发保护</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 创建DNA读取寄存器实例
 * FPGAReg_DNA regDNA = new FPGAReg_DNA();
 *
 * // 发送读取命令到FPGA
 * fpgaCommand.sendCmd(regDNA);
 *
 * // 获取DNA值
 * long dna = regDNA.getDNA();
 * System.out.println("Device DNA: " + Long.toHexString(dna));
 *
 * // 或者直接获取接收寄存器
 * FPGAReg recvReg = regDNA.getRecvReg();
 * int lowDWord = recvReg.getVal(0);   // 低32位
 * int highDWord = recvReg.getVal(1);  // 高32位
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018
 * @see FPGAReg FPGA寄存器基类
 * @see FPGACommand FPGA命令处理类
 */
public class FPGAReg_DNA extends FPGAReg{  // 继承FPGAReg基类，复用寄存器操作方法

    /**
     * 接收寄存器 - 用于存储从FPGA读取的DNA数据
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>双缓冲模式：发送寄存器和接收寄存器分离</li>
     *   <li>接收寄存器存储从FPGA返回的DNA数据</li>
     *   <li>地址为0，大小为8字节（64位DNA）</li>
     * </ul>
     */
    private FPGAReg recvReg;  // 接收寄存器，存储从FPGA读取的DNA数据

    /**
     * 构造函数 - 初始化DNA读取寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置为读取类型寄存器</li>
     *   <li>寄存器地址：0（DNA读取地址）</li>
     *   <li>寄存器大小：8字节（64位DNA）</li>
     *   <li>第三个参数true表示这是一个读取类型的寄存器</li>
     * </ul>
     *
     * <p><b>双缓冲模式说明：</b>
     * <ul>
     *   <li>父类寄存器用于发送读取命令</li>
     *   <li>recvReg用于接收返回的DNA数据</li>
     *   <li>读取类型寄存器需要两个缓冲区</li>
     * </ul>
     */
    public FPGAReg_DNA() {  // 构造函数
        // 调用父类构造函数，设置寄存器地址、大小和类型
        // 参数1：寄存器地址=0（DNA读取地址）
        // 参数2：寄存器大小=8字节（64位DNA）
        // 参数3：true表示这是读取类型的寄存器
        super(0, 8, true);  // 初始化DNA读取寄存器，地址为0，大小为8字节，类型为读取
        // 创建接收寄存器，用于存储从FPGA读取的DNA数据
        recvReg = new FPGAReg(0, 8);  // 创建接收寄存器，地址为0，大小为8字节
    }

    /**
     * 获取DNA值 - 组合高低32位为64位DNA
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从接收寄存器读取DNA数据</li>
     *   <li>组合低32位和高32位为64位DNA值</li>
     *   <li>返回完整的64位DNA标识符</li>
     * </ul>
     *
     * <p><b>数据组合说明：</b>
     * <ul>
     *   <li>低32位：recvReg.getVal(0) - 第一个4字节数据</li>
     *   <li>高32位：recvReg.getVal(1) - 第二个4字节数据</li>
     *   <li>组合方式：(高32位 << 32) | 低32位</li>
     * </ul>
     *
     * <p><b>返回值说明：</b>
     * <ul>
     *   <li>返回64位DNA值，全球唯一</li>
     *   <li>DNA值可用于设备标识、授权验证等</li>
     * </ul>
     *
     * @return 64位DNA值（长整型）
     */
    public long getDNA(){  // 获取DNA值方法
        // 从接收寄存器读取高低32位，组合为64位DNA
        // recvReg.getVal(0)：获取低32位数据
        // recvReg.getVal(1)：获取高32位数据
        // 组合方式：(高32位 << 32) | 低32位
        return ((long)recvReg.getVal(0) << 32) | recvReg.getVal(1);  // 组合高低32位为64位DNA值
    }

    /**
     * 获取接收寄存器 - 返回用于存储DNA数据的接收寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回接收寄存器实例</li>
     *   <li>接收寄存器存储从FPGA读取的DNA数据</li>
     *   <li>用于FPGACommand填充接收数据</li>
     * </ul>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>FPGACommand在接收到数据后填充recvReg</li>
     *   <li>外部类可以直接访问接收寄存器</li>
     *   <li>用于调试和测试</li>
     * </ul>
     *
     * @return 接收寄存器实例（FPGAReg类型）
     */
    public FPGAReg getRecvReg(){  // 获取接收寄存器方法
        // 返回接收寄存器实例
        return recvReg;  // 返回接收寄存器
    }
}
