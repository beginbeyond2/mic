package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

/**
 * FPGA风扇速度读取寄存器类 - 用于读取示波器风扇的实际转速
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
 *   <li>读取风扇的实际转速</li>
 *   <li>支持多风扇转速读取</li>
 *   <li>提供转速数据解析接口</li>
 *   <li>支持系统温度监控</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>监控风扇运行状态</li>
 *   <li>确保设备散热正常</li>
 *   <li>支持风扇故障检测</li>
 *   <li>提供系统健康状态信息</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * FPGA_FAN_SPEED寄存器（4字节，32位，只读）
 * │
 * ├── 位0-15：风扇1转速原始值（16位）
 * │     └── 实际转速 = 原始值 × 30 RPM
 * │
 * └── 位16-31：风扇2转速原始值（16位）
 *       └── 实际转速 = 原始值 × 30 RPM
 *
 * 注意：这是一个只读寄存器，使用双缓冲模式
 * </pre>
 *
 * <p><b>转速计算说明：</b>
 * <ul>
 *   <li>FPGA返回的转速为原始计数值</li>
 *   <li>实际转速 = 原始值 × 30 RPM</li>
 *   <li>乘数30由FPGA计数器频率决定</li>
 *   <li>转速单位为RPM（每分钟转数）</li>
 * </ul>
 *
 * <p><b>双缓冲模式：</b>
 * <ul>
 *   <li>发送寄存器：用于发送读取命令（地址为FPGA_FAN_SPEED，大小为4字节）</li>
 *   <li>接收寄存器（recvReg）：用于存储从FPGA读取的风扇转速数据</li>
 *   <li>构造函数第三个参数true表示这是一个读取类型的寄存器</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>系统监控：定期读取风扇转速</li>
 *   <li>故障检测：检测风扇是否停转</li>
 *   <li>温度管理：根据转速调整散热策略</li>
 *   <li>用户界面：显示风扇转速信息</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 *   <li>依赖：SystemMonitor（系统监控类）</li>
 * </ul>
 *
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>寄存器操作在FPGACommand中同步执行</li>
 *   <li>风扇转速读取通常在监控线程中周期执行</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 创建风扇速度读取寄存器实例
 * FPGAReg_FanSpeed regFanSpeed = new FPGAReg_FanSpeed();
 *
 * // 发送读取命令到FPGA
 * fpgaCommand.sendCmd(regFanSpeed);
 *
 * // 获取风扇转速
 * int[] fans = new int[2];
 * int fan1Speed = regFanSpeed.getFanSpeed(fans);
 * int fan2Speed = fans[1];
 *
 * System.out.println("Fan1: " + fan1Speed + " RPM");
 * System.out.println("Fan2: " + fan2Speed + " RPM");
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018
 * @see FPGAReg FPGA寄存器基类
 * @see FPGACommand FPGA命令处理类
 */
public class FPGAReg_FanSpeed extends FPGAReg{  // 继承FPGAReg基类，复用寄存器操作方法

    /**
     * 接收寄存器 - 用于存储从FPGA读取的风扇转速数据
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>双缓冲模式：发送寄存器和接收寄存器分离</li>
     *   <li>接收寄存器存储从FPGA返回的风扇转速数据</li>
     *   <li>地址为FPGA_FAN_SPEED，大小为4字节</li>
     * </ul>
     */
    private FPGAReg recvReg;  // 接收寄存器，存储从FPGA读取的风扇转速数据

    /**
     * 构造函数 - 初始化风扇速度读取寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置为读取类型寄存器</li>
     *   <li>寄存器地址：FPGA_FAN_SPEED（风扇速度寄存器）</li>
     *   <li>寄存器大小：4字节（32位）</li>
     *   <li>第三个参数true表示这是一个读取类型的寄存器</li>
     * </ul>
     *
     * <p><b>双缓冲模式说明：</b>
     * <ul>
     *   <li>父类寄存器用于发送读取命令</li>
     *   <li>recvReg用于接收返回的风扇转速数据</li>
     *   <li>读取类型寄存器需要两个缓冲区</li>
     * </ul>
     */
    public FPGAReg_FanSpeed() {  // 构造函数
        // 调用父类构造函数，设置寄存器地址、大小和类型
        // FPGA_FAN_SPEED：风扇速度寄存器地址
        // 4：寄存器大小为4字节（32位）
        // true：表示这是读取类型的寄存器
        super(FPGAReg.FPGA_FAN_SPEED, 4, true);  // 初始化风扇速度读取寄存器，地址为FPGA_FAN_SPEED，大小为4字节，类型为读取
        // 创建接收寄存器，用于存储从FPGA读取的风扇转速数据
        recvReg = new FPGAReg(FPGA_FAN_SPEED, 4);  // 创建接收寄存器，地址为FPGA_FAN_SPEED，大小为4字节
    }

    /**
     * 获取接收寄存器 - 返回用于存储风扇转速数据的接收寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回接收寄存器实例</li>
     *   <li>接收寄存器存储从FPGA读取的风扇转速数据</li>
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
     * 获取风扇转速 - 解析并返回风扇的实际转速
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从接收寄存器读取风扇转速原始值</li>
     *   <li>解析风扇1和风扇2的转速</li>
     *   <li>计算实际转速（原始值 × 30）</li>
     *   <li>将转速存储到传入的数组中</li>
     * </ul>
     *
     * <p><b>转速解析说明：</b>
     * <ul>
     *   <li>风扇1转速：低16位（位0-15）× 30 RPM</li>
     *   <li>风扇2转速：高16位（位16-31）× 30 RPM</li>
     *   <li>乘数30由FPGA计数器频率决定</li>
     * </ul>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>fans数组：用于存储两个风扇的转速</li>
     *   <li>fans[0]：风扇1转速（RPM）</li>
     *   <li>fans[1]：风扇2转速（RPM）</li>
     * </ul>
     *
     * <p><b>返回值说明：</b>
     * <ul>
     *   <li>返回风扇1的转速（与fans[0]相同）</li>
     *   <li>转速单位为RPM（每分钟转数）</li>
     * </ul>
     *
     * @param fans 风扇转速数组（fans[0]=风扇1转速，fans[1]=风扇2转速）
     * @return 风扇1转速（RPM）
     */
    public int getFanSpeed(int [] fans){  // 获取风扇转速方法
        // 从接收寄存器读取原始转速值
        int v = recvReg.getVal();  // 获取接收寄存器的值（32位原始转速数据）
        // 解析风扇1转速：低16位 × 30
        fans[0] = 30 * (v & 0xFFFF);  // 提取低16位，乘以30得到风扇1转速（RPM）
        // 解析风扇2转速：高16位 × 30
        fans[1] = 30 * ((v >>> 16) & 0xFFFF);  // 提取高16位，乘以30得到风扇2转速（RPM）
        // 返回风扇1转速
        return fans[0];  // 返回风扇1转速（RPM）
    }
}