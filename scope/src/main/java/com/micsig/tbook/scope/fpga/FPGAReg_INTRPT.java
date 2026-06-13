package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

/**
 * FPGA中断状态寄存器类 - 用于读取示波器FPGA的中断状态信息
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA状态读取</li>
 *   <li>设计模式：继承模式（继承FPGAReg基类）+ 双缓冲模式</li>
 *   <li>职责类型：硬件状态读取器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>读取FPGA中断状态</li>
 *   <li>检测设备完成中断</li>
 *   <li>检测自动触发完成中断</li>
 *   <li>检测刷新中断</li>
 *   <li>检测视频触发中断</li>
 *   <li>检测FPGA IO中断</li>
 *   <li>检测频率测量中断</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供FPGA中断状态的读取接口</li>
 *   <li>支持多种中断类型的检测</li>
 *   <li>封装中断状态的位操作</li>
 *   <li>支持事件驱动的系统响应</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * FPGA_INTRPT寄存器（4字节，32位，只读）
 * │
 * ├── 位2：设备完成中断标志（DevFinish）
 * │     └── 1=设备完成中断发生，0=未发生
 * │
 * ├── 位3：自动触发完成中断标志（AutoFinish）
 * │     └── 1=自动触发完成中断发生，0=未发生
 * │
 * ├── 位4：刷新中断标志（Refrsh）
 * │     └── 1=刷新中断发生，0=未发生
 * │
 * ├── 位5：视频触发中断标志（VideoTrig）
 * │     └── 1=视频触发中断发生，0=未发生
 * │
 * ├── 位7：FPGA IO中断标志（FpgaIO）
 * │     └── 1=FPGA IO中断发生，0=未发生
 * │
 * └── 位8：频率测量中断标志（Fre）
 *       └── 1=频率测量中断发生，0=未发生
 *
 * 注意：这是一个只读寄存器，使用双缓冲模式
 * </pre>
 *
 * <p><b>中断类型说明：</b>
 * <ul>
 *   <li>设备完成中断（DevFinish）：设备操作完成时触发</li>
 *   <li>自动触发完成中断（AutoFinish）：自动触发完成时触发</li>
 *   <li>刷新中断（Refrsh）：需要刷新显示时触发</li>
 *   <li>视频触发中断（VideoTrig）：视频触发事件时触发</li>
 *   <li>FPGA IO中断（FpgaIO）：FPGA IO操作完成时触发</li>
 *   <li>频率测量中断（Fre）：频率测量完成时触发</li>
 * </ul>
 *
 * <p><b>双缓冲模式：</b>
 * <ul>
 *   <li>发送寄存器：用于发送读取命令（地址为FPGA_INTRPT，大小为4字节）</li>
 *   <li>接收寄存器（recvReg）：用于存储从FPGA读取的中断状态数据</li>
 *   <li>构造函数第三个参数true表示这是一个读取类型的寄存器</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>中断轮询：定期检查中断状态</li>
 *   <li>事件响应：根据中断类型执行相应操作</li>
 *   <li>状态监控：监控FPGA运行状态</li>
 *   <li>调试诊断：分析中断触发原因</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 *   <li>依赖：InterruptHandler（中断处理类）</li>
 * </ul>
 *
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>寄存器操作在FPGACommand中同步执行</li>
 *   <li>中断状态读取通常在监控线程中周期执行</li>
 *   <li>val变量存储中断状态，需要确保读取一致性</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 创建中断状态寄存器实例
 * FPGAReg_INTRPT regIntrpt = new FPGAReg_INTRPT();
 *
 * // 发送读取命令到FPGA
 * fpgaCommand.sendCmd(regIntrpt);
 *
 * // 处理接收数据
 * regIntrpt.onRecv();
 *
 * // 检查中断状态
 * if (regIntrpt.isDevFinish()) {
 *     // 处理设备完成中断
 * }
 * if (regIntrpt.isAutoFinish()) {
 *     // 处理自动触发完成中断
 * }
 * if (regIntrpt.isRefrsh()) {
 *     // 处理刷新中断
 * }
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018/7/11
 * @see FPGAReg FPGA寄存器基类
 * @see FPGACommand FPGA命令处理类
 */
public class FPGAReg_INTRPT extends FPGAReg {  // 继承FPGAReg基类，复用寄存器操作方法

    /**
     * 接收寄存器 - 用于存储从FPGA读取的中断状态数据
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>双缓冲模式：发送寄存器和接收寄存器分离</li>
     *   <li>接收寄存器存储从FPGA返回的中断状态数据</li>
     *   <li>地址为FPGA_INTRPT，大小为4字节</li>
     * </ul>
     */
    private FPGAReg recvReg;  // 接收寄存器，存储从FPGA读取的中断状态数据

    /**
     * 中断状态值 - 存储解析后的中断状态
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>存储从接收寄存器读取的中断状态值</li>
     *   <li>用于后续的中断状态检测方法</li>
     *   <li>在onRecv方法中更新</li>
     * </ul>
     */
    private int val = 0;  // 中断状态值，存储解析后的中断状态

    /**
     * 构造函数 - 初始化中断状态寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置为读取类型寄存器</li>
     *   <li>寄存器地址：FPGA_INTRPT（中断状态寄存器）</li>
     *   <li>寄存器大小：4字节（32位）</li>
     *   <li>第三个参数true表示这是一个读取类型的寄存器</li>
     * </ul>
     *
     * <p><b>双缓冲模式说明：</b>
     * <ul>
     *   <li>父类寄存器用于发送读取命令</li>
     *   <li>recvReg用于接收返回的中断状态数据</li>
     *   <li>读取类型寄存器需要两个缓冲区</li>
     * </ul>
     */
    public FPGAReg_INTRPT() {  // 构造函数
        // 调用父类构造函数，设置寄存器地址、大小和类型
        // FPGA_INTRPT：中断状态寄存器地址
        // 4：寄存器大小为4字节（32位）
        // true：表示这是读取类型的寄存器
        super(FPGA_INTRPT, 4, true);  // 初始化中断状态寄存器，地址为FPGA_INTRPT，大小为4字节，类型为读取
        // 创建接收寄存器，用于存储从FPGA读取的中断状态数据
        recvReg = new FPGAReg(FPGA_INTRPT, 4);  // 创建接收寄存器，地址为FPGA_INTRPT，大小为4字节
    }

    /**
     * 获取接收寄存器 - 返回用于存储中断状态数据的接收寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回接收寄存器实例</li>
     *   <li>接收寄存器存储从FPGA读取的中断状态数据</li>
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

    /**
     * 接收数据处理 - 从接收寄存器读取并存储中断状态值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从接收寄存器读取中断状态值</li>
     *   <li>将中断状态值存储到val变量</li>
     *   <li>为后续的中断状态检测方法提供数据</li>
     * </ul>
     *
     * <p><b>调用时机：</b>
     * <ul>
     *   <li>FPGACommand接收数据后调用此方法</li>
     *   <li>在检查中断状态之前必须调用此方法</li>
     *   <li>通常在数据接收回调中调用</li>
     * </ul>
     */
    public void onRecv(){  // 接收数据处理方法
        // 从接收寄存器读取中断状态值
        // recvReg.getVal(0)：获取第一个4字节数据（中断状态）
        val = recvReg.getVal(0);  // 将中断状态值存储到val变量
    }

    /**
     * 检测设备完成中断 - 检查设备完成中断是否发生
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查位2是否为1</li>
     *   <li>返回设备完成中断状态</li>
     * </ul>
     *
     * <p><b>中断说明：</b>
     * <ul>
     *   <li>设备完成中断：设备操作完成时触发</li>
     *   <li>可能表示采集完成、配置完成等</li>
     * </ul>
     *
     * @return true=设备完成中断发生，false=未发生
     */
    public boolean isDevFinish(){  // 检测设备完成中断方法
        // 检查位2是否为1
        // val & (1<<2)：提取位2的值
        // != 0：判断是否为1（中断发生）
        return (val & (1 << 2)) != 0;  // 返回设备完成中断状态
    }

    /**
     * 检测自动触发完成中断 - 检查自动触发完成中断是否发生
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查位3是否为1</li>
     *   <li>返回自动触发完成中断状态</li>
     * </ul>
     *
     * <p><b>中断说明：</b>
     * <ul>
     *   <li>自动触发完成中断：自动触发模式完成时触发</li>
     *   <li>表示自动触发已经成功触发采集</li>
     * </ul>
     *
     * @return true=自动触发完成中断发生，false=未发生
     */
    public boolean isAutoFinish(){  // 检测自动触发完成中断方法
        // 检查位3是否为1
        // val & (1<<3)：提取位3的值
        // != 0：判断是否为1（中断发生）
        return (val & (1 << 3)) != 0;  // 返回自动触发完成中断状态
    }

    /**
     * 检测刷新中断 - 检查刷新中断是否发生
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查位4是否为1</li>
     *   <li>返回刷新中断状态</li>
     * </ul>
     *
     * <p><b>中断说明：</b>
     * <ul>
     *   <li>刷新中断：需要刷新显示时触发</li>
     *   <li>表示有新的数据需要显示</li>
     * </ul>
     *
     * @return true=刷新中断发生，false=未发生
     */
    public boolean isRefrsh(){  // 检测刷新中断方法
        // 检查位4是否为1
        // val & (1<<4)：提取位4的值
        // != 0：判断是否为1（中断发生）
        return (val & (1 << 4)) != 0;  // 返回刷新中断状态
    }

    /**
     * 检测视频触发中断 - 检查视频触发中断是否发生
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查位5是否为1</li>
     *   <li>返回视频触发中断状态</li>
     * </ul>
     *
     * <p><b>中断说明：</b>
     * <ul>
     *   <li>视频触发中断：视频触发事件时触发</li>
     *   <li>用于视频信号的特殊触发处理</li>
     * </ul>
     *
     * @return true=视频触发中断发生，false=未发生
     */
    public boolean isVideoTrig(){  // 检测视频触发中断方法
        // 检查位5是否为1
        // val & (1<<5)：提取位5的值
        // != 0：判断是否为1（中断发生）
        return (val & (1 << 5)) != 0;  // 返回视频触发中断状态
    }

    /**
     * 检测FPGA IO中断 - 检查FPGA IO中断是否发生
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查位7是否为1</li>
     *   <li>返回FPGA IO中断状态</li>
     * </ul>
     *
     * <p><b>中断说明：</b>
     * <ul>
     *   <li>FPGA IO中断：FPGA IO操作完成时触发</li>
     *   <li>可能表示数据传输完成、命令执行完成等</li>
     * </ul>
     *
     * @return true=FPGA IO中断发生，false=未发生
     */
    public boolean isFpgaIO(){  // 检测FPGA IO中断方法
        // 检查位7是否为1
        // val & (1<<7)：提取位7的值
        // != 0：判断是否为1（中断发生）
        return (val & (1 << 7)) != 0;  // 返回FPGA IO中断状态
    }

    /**
     * 检测频率测量中断 - 检查频率测量中断是否发生
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>检查位8是否为1</li>
     *   <li>返回频率测量中断状态</li>
     * </ul>
     *
     * <p><b>中断说明：</b>
     * <ul>
     *   <li>频率测量中断：频率测量完成时触发</li>
     *   <li>表示频率测量数据已经准备好</li>
     * </ul>
     *
     * @return true=频率测量中断发生，false=未发生
     */
    public boolean isFre(){  // 检测频率测量中断方法
        // 检查位8是否为1
        // val & (1<<8)：提取位8的值
        // != 0：判断是否为1（中断发生）
        return (val & (1 << 8)) != 0;  // 返回频率测量中断状态
    }
}
