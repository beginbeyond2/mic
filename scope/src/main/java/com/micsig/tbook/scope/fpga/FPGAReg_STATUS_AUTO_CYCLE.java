package com.micsig.tbook.scope.fpga;  // 包声明：FPGA寄存器管理模块

/**
 * FPGA自动周期测量状态寄存器类
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
 *   <li>读取FPGA自动测量的周期值（频率/周期）</li>
 *   <li>提供接收寄存器对象供FPGACommand读取数据</li>
 *   <li>支持多通道独立测量（CH1-CH4）</li>
 *   <li>配合自动测量功能（Auto功能）使用</li>
 * </ul>
 *
 * <p><b>数据格式：</b>
 * <pre>
 * 32位寄存器数据结构：
 * ┌────────────────────────────────────────┐
 * │        32位周期测量值 (Cycle Value)      │
 * │         (bit31-0: 周期原始数据)          │
 * └────────────────────────────────────────┘
 *
 * 每个通道独立寄存器地址：
 * - FPGA_STATUS_AUTO_CYCLE_1 (0x86): 通道1周期测量值
 * - FPGA_STATUS_AUTO_CYCLE_2 (0x87): 通道2周期测量值
 * - FPGA_STATUS_AUTO_CYCLE_3 (0x88): 通道3周期测量值
 * - FPGA_STATUS_AUTO_CYCLE_4 (0x89): 通道4周期测量值
 *
 * 周期值说明：
 * - 该值为FPGA测量的信号周期原始数据
 * - 需要根据时基设置转换为实际时间值
 * - 可通过周期计算频率：Frequency = 1 / Cycle
 * </pre>
 *
 * <p><b>业务场景：</b>
 * <ol>
 *   <li>用户按下Auto键，触发自动测量</li>
 *   <li>FPGA自动测量信号的电压值和周期值</li>
 *   <li>FPGACommand检测到自动测量完成中断</li>
 *   <li>创建本类实例读取各通道周期测量结果</li>
 *   <li>通过getRecvReg()获取接收寄存器数据</li>
 *   <li>解析数据并更新UI显示（频率、周期等）</li>
 * </ol>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：extends FPGAReg（FPGA寄存器基类）</li>
 *   <li>调用者：FPGACommand（FPGA命令管理类）</li>
 *   <li>关联：FPGAReg_STATUS_AUTO_V（电压测量寄存器）</li>
 *   <li>协作：FPGA_Status.FpgaAuto（自动测量结果对象）</li>
 * </ul>
 *
 * <p><b>与FPGAReg_STATUS_AUTO_V的区别：</b>
 * <pre>
 * ┌──────────────────────┬──────────────────────────┬─────────────────────┐
 * │       寄存器类型      │        数据内容          │      用途           │
 * ├──────────────────────┼──────────────────────────┼─────────────────────┤
 * │ FPGAReg_STATUS_AUTO_V│  最大值/最小值（16位）    │  电压测量、Vpp计算  │
 * │ FPGAReg_STATUS_AUTO_ │  周期值（32位）          │  周期测量、频率计算 │
 * │ CYCLE                │                          │                     │
 * └──────────────────────┴──────────────────────────┴─────────────────────┘
 *
 * 注意：周期寄存器没有提供getMinVal/getMaxVal等解析方法，
 *       数据解析由FPGACommand直接通过recvReg.getVal()完成。
 * </pre>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 创建寄存器实例（读取通道1的周期测量值）
 * FPGAReg_STATUS_AUTO_CYCLE statusAutoCycle =
 *     new FPGAReg_STATUS_AUTO_CYCLE(FPGAReg.FPGA_STATUS_AUTO_CYCLE_1);
 * statusAutoCycle.setFpgaIdx(0);  // 设置FPGA索引
 *
 * // 发送读取命令并接收数据
 * recvCommand(statusAutoCycle, statusAutoCycle.getRecvReg());
 *
 * // 获取周期测量结果（通过接收寄存器）
 * int cycleValue = statusAutoCycle.getRecvReg().getVal(0);  // 读取32位周期值
 * </pre>
 *
 * @author zhuzh
 * @since 2018-7-11
 * @see FPGAReg
 * @see FPGACommand
 * @see FPGAReg_STATUS_AUTO_V
 */
public class FPGAReg_STATUS_AUTO_CYCLE extends FPGAReg{

    /**
     * 接收寄存器对象
     *
     * <p><b>业务含义：</b>
     * 用于存储从FPGA接收到的原始周期测量数据。由于FPGA寄存器读取需要先发送命令再接收数据，
     * 所以需要两个寄存器对象：父类用于发送命令，recvReg用于接收数据。
     *
     * <p><b>数据格式：</b>
     * - 字节序：小端模式（Little-Endian）
     * - 数据长度：4字节（32位）
     * - 格式：[Header(8字节)][Data(4字节)]
     * - 内容：32位周期测量原始值
     *
     * <p><b>数据解析：</b>
     * 与FPGAReg_STATUS_AUTO_V不同，本类不提供专门的解析方法（如getMinVal/getMaxVal），
     * 数据解析由FPGACommand直接通过recvReg.getVal()读取32位周期值，然后根据时基设置
     * 转换为实际时间值或频率值。
     *
     * <p><b>取值范围：</b>
     * - 初始值：未初始化（等待FPGA返回数据）
     * - 有效范围：0x00000000 ~ 0xFFFFFFFF（32位无符号整数）
     */
    private FPGAReg recvReg;

    /**
     * 构造函数：创建自动周期测量状态寄存器实例
     *
     * <p><b>功能说明：</b>
     * 初始化寄存器对象，创建发送命令寄存器和接收数据寄存器。
     *
     * <p><b>实现细节：</b>
     * <ol>
     *   <li>调用父类构造函数，创建发送命令寄存器（bRecv=true表示接收模式）</li>
     *   <li>创建独立的接收寄存器对象，用于存储FPGA返回的周期数据</li>
     * </ol>
     *
     * <p><b>参数说明：</b>
     * @param addr 寄存器地址（取值范围：FPGA_STATUS_AUTO_CYCLE_1~4，即0x86~0x89）
     *             <ul>
     *               <li>0x86 (FPGA_STATUS_AUTO_CYCLE_1): 通道1周期测量寄存器</li>
     *               <li>0x87 (FPGA_STATUS_AUTO_CYCLE_2): 通道2周期测量寄存器</li>
     *               <li>0x88 (FPGA_STATUS_AUTO_CYCLE_3): 通道3周期测量寄存器</li>
     *               <li>0x89 (FPGA_STATUS_AUTO_CYCLE_4): 通道4周期测量寄存器</li>
     *             </ul>
     *
     * <p><b>设计说明：</b>
     * 本类与FPGAReg_STATUS_AUTO_V的设计不同：
     * <ul>
     *   <li>FPGAReg_STATUS_AUTO_V：提供onRecv()和getMinVal()/getMaxVal()方法进行数据解析</li>
     *   <li>FPGAReg_STATUS_AUTO_CYCLE：仅提供getRecvReg()方法，数据解析由调用者完成</li>
     * </ul>
     * 这种设计差异的原因：
     * <ul>
     *   <li>电压值需要解析最大值和最小值两个16位数据</li>
     *   <li>周期值是单一32位数据，直接读取即可</li>
     * </ul>
     */
    public FPGAReg_STATUS_AUTO_CYCLE(int addr) {
        super(addr, 4, true);  // 调用父类构造函数：地址=addr，长度=4字节，接收模式=true
        recvReg = new FPGAReg(addr,4);  // 创建接收寄存器对象：用于存储FPGA返回的周期数据
    }

    /**
     * 获取接收寄存器对象
     *
     * <p><b>功能说明：</b>
     * 返回用于接收FPGA数据的寄存器对象。在FPGACommand中，需要先调用recvCommand发送命令，
     * 然后通过此方法获取接收寄存器来读取FPGA返回的周期数据。
     *
     * <p><b>使用场景：</b>
     * <pre>
     * // 典型调用流程（在FPGACommand中）：
     * FPGAReg_STATUS_AUTO_CYCLE statusAutoCycle =
     *     new FPGAReg_STATUS_AUTO_CYCLE(FPGAReg.FPGA_STATUS_AUTO_CYCLE_1 + i);
     * statusAutoCycle.setFpgaIdx(fpgaIdx);  // 设置FPGA索引
     * recvCommand(statusAutoCycle, statusAutoCycle.getRecvReg());  // 发送命令并接收数据
     *
     * // 数据读取（在后续处理中）：
     * int cycleValue = statusAutoCycle.getRecvReg().getVal(0);  // 读取32位周期值
     * </pre>
     *
     * <p><b>数据读取方式：</b>
     * 与FPGAReg_STATUS_AUTO_V不同，本类不提供onRecv()方法，数据读取方式为：
     * <ol>
     *   <li>调用recvCommand(statusAutoCycle, statusAutoCycle.getRecvReg())</li>
     *   <li>直接通过getRecvReg().getVal(0)读取32位周期值</li>
     *   <li>由FPGACommand根据时基设置转换为实际时间值</li>
     * </ol>
     *
     * <p><b>返回值说明：</b>
     * @return FPGAReg 接收寄存器对象，包含FPGA返回的32位周期测量原始数据
     *         <ul>
     *           <li>数据长度：4字节（32位）</li>
     *           <li>数据格式：小端模式（Little-Endian）</li>
     *           <li>数据内容：周期测量原始值（需要根据时基转换）</li>
     *         </ul>
     */
    public FPGAReg getRecvReg(){
        return recvReg;  // 返回接收寄存器对象，供FPGACommand读取周期数据
    }

}