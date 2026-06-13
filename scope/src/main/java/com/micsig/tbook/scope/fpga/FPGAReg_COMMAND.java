package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

/**
 * FPGA命令寄存器类 - 用于发送命令到FPGA并接收返回信息
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA命令通信</li>
 *   <li>设计模式：继承模式（继承FPGAReg基类）+ 双缓冲模式</li>
 *   <li>职责类型：命令通信管理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>发送命令参数到FPGA</li>
 *   <li>发送命令代码到FPGA</li>
 *   <li>发送命令计数和有效标志</li>
 *   <li>接收FPGA返回的同步头、列地址和帧数</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供统一的FPGA命令发送接口</li>
 *   <li>支持命令参数和命令代码分离</li>
 *   <li>支持命令计数用于调试和追踪</li>
 *   <li>接收FPGA返回的状态信息</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * 发送寄存器（4字节，32位）
 * │
 * ├── 位0-7：参数（Param）- 8位
 * │     └── 命令参数，用于传递命令相关的参数值
 * │
 * ├── 位8-15：命令代码（Cmd）- 8位
 * │     └── 命令代码，标识不同的FPGA命令
 * │
 * ├── 位16-30：命令计数（CmdCnt）- 15位
 * │     └── 命令计数器，用于命令追踪和调试
 * │
 * └── 位31：命令计数有效标志（CmdCntValid）- 1位
 *       └── 命令计数有效标志，1=有效，0=无效
 *
 * 接收寄存器（8字节，64位）
 * │
 * ├── 位0-15：同步头（SyncHeader）- 16位
 * │     └── 同步头，用于数据同步
 * │
 * ├── 位16-31：列地址（ColAddr）- 16位
 * │     └── 列地址，用于数据定位
 * │
 * └── 位32-63：帧数（FrameNums）- 32位
 *       └── 帧数，用于数据传输控制
 * </pre>
 *
 * <p><b>命令说明：</b>
 * <ul>
 *   <li>命令代码：标识不同的FPGA操作命令</li>
 *   <li>命令参数：传递命令相关的参数值</li>
 *   <li>命令计数：用于命令追踪和调试，可选功能</li>
 *   <li>命令计数有效标志：控制命令计数是否有效</li>
 * </ul>
 *
 * <p><b>双缓冲模式：</b>
 * <ul>
 *   <li>发送寄存器：用于发送命令到FPGA（4字节）</li>
 *   <li>接收寄存器（recvReg）：用于接收FPGA返回的信息（8字节）</li>
 *   <li>发送和接收使用不同的缓冲区</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>发送控制命令：如启动采集、停止采集等</li>
 *   <li>发送配置命令：如设置参数、切换模式等</li>
 *   <li>接收状态信息：如同步状态、帧数等</li>
 *   <li>调试追踪：使用命令计数追踪命令执行</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 * </ul>
 *
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>寄存器操作在FPGACommand中同步执行</li>
 *   <li>命令发送需要确保硬件稳定</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 创建命令寄存器实例
 * FPGAReg_COMMAND regCommand = new FPGAReg_COMMAND();
 *
 * // 设置命令参数
 * regCommand.setParam(0x01);
 *
 * // 设置命令代码
 * regCommand.setCmd(0x10);
 *
 * // 设置命令计数（可选）
 * regCommand.setCmdCnt(100);
 * regCommand.setCmdCntValid(1);
 *
 * // 发送命令到FPGA
 * fpgaCommand.sendCmd(regCommand);
 *
 * // 获取返回信息
 * int syncHeader = regCommand.getSyncHeader();
 * int colAddr = regCommand.getColAddr();
 * int frameNums = regCommand.getFrameNums();
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/26
 * @see FPGAReg FPGA寄存器基类
 * @see FPGACommand FPGA命令处理类
 */
public class FPGAReg_COMMAND extends FPGAReg {  // 继承FPGAReg基类，复用寄存器操作方法

    /**
     * 接收寄存器 - 用于存储从FPGA返回的信息
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>双缓冲模式：发送寄存器和接收寄存器分离</li>
     *   <li>接收寄存器存储从FPGA返回的状态信息</li>
     *   <li>地址为FPGA_COMMAND，大小为8字节</li>
     * </ul>
     */
    private FPGAReg recvReg;  // 接收寄存器，存储从FPGA返回的信息

    /**
     * 构造函数 - 初始化命令寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>寄存器地址：FPGA_COMMAND（命令寄存器）</li>
     *   <li>寄存器大小：4字节（32位）</li>
     *   <li>创建接收寄存器，大小为8字节</li>
     * </ul>
     *
     * <p><b>寄存器大小说明：</b>
     * <ul>
     *   <li>发送寄存器：4字节（参数+命令+计数+标志）</li>
     *   <li>接收寄存器：8字节（同步头+列地址+帧数）</li>
     * </ul>
     */
    public FPGAReg_COMMAND() {  // 构造函数
        // 调用父类构造函数，传入寄存器地址和大小
        // FPGA_COMMAND：命令寄存器地址
        // 4：寄存器大小为4字节（32位）
        super(FPGA_COMMAND, 4);  // 初始化命令寄存器，地址为FPGA_COMMAND，大小为4字节
        // 创建接收寄存器，用于存储从FPGA返回的信息
        // FPGA_COMMAND：命令寄存器地址
        // 4*2：接收寄存器大小为8字节（64位）
        recvReg = new FPGAReg(FPGA_COMMAND, 4*2);  // 创建接收寄存器，地址为FPGA_COMMAND，大小为8字节
    }

    /**
     * 设置命令参数 - 设置命令的参数值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置命令参数字段</li>
     *   <li>数据位置：位0-7（8位）</li>
     *   <li>用于传递命令相关的参数值</li>
     * </ul>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>参数值的具体含义由命令代码决定</li>
     *   <li>不同命令可能有不同的参数含义</li>
     * </ul>
     *
     * @param val 命令参数值（8位，0-255）
     */
    public void setParam(int val){  // 设置命令参数方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=0（位0开始）
        // 参数2：位宽度=8位（1字节）
        // 参数3：命令参数值
        setVal(0, 8, val);  // 设置位0-7的值，宽度为8位，值为val
    }

    /**
     * 设置命令代码 - 设置命令的代码值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置命令代码字段</li>
     *   <li>数据位置：位8-15（8位）</li>
     *   <li>用于标识不同的FPGA命令</li>
     * </ul>
     *
     * <p><b>命令代码说明：</b>
     * <ul>
     *   <li>不同的命令代码对应不同的FPGA操作</li>
     *   <li>命令代码由FPGA固件定义</li>
     * </ul>
     *
     * @param val 命令代码值（8位，0-255）
     */
    public void setCmd(int val){  // 设置命令代码方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=8（位8开始）
        // 参数2：位宽度=8位（1字节）
        // 参数3：命令代码值
        setVal(8, 8, val);  // 设置位8-15的值，宽度为8位，值为val
    }

    /**
     * 设置命令计数 - 设置命令计数器值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置命令计数字段</li>
     *   <li>数据位置：位16-30（15位）</li>
     *   <li>用于命令追踪和调试</li>
     * </ul>
     *
     * <p><b>命令计数说明：</b>
     * <ul>
     *   <li>命令计数器用于追踪命令执行顺序</li>
     *   <li>可用于调试和故障诊断</li>
     *   <li>需要配合setCmdCntValid使用</li>
     * </ul>
     *
     * @param val 命令计数值（15位，0-32767）
     */
    public void setCmdCnt(int val){  // 设置命令计数方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=16（位16开始）
        // 参数2：位宽度=15位
        // 参数3：命令计数值
        setVal(16, 15, val);  // 设置位16-30的值，宽度为15位，值为val
    }

    /**
     * 设置命令计数有效标志 - 设置命令计数是否有效
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>设置命令计数有效标志</li>
     *   <li>数据位置：位31（1位）</li>
     *   <li>控制命令计数是否有效</li>
     * </ul>
     *
     * <p><b>有效标志说明：</b>
     * <ul>
     *   <li>1：命令计数有效，FPGA使用命令计数</li>
     *   <li>0：命令计数无效，FPGA忽略命令计数</li>
     * </ul>
     *
     * @param val 有效标志值（1位，0或1）
     */
    public void setCmdCntValid(int val){  // 设置命令计数有效标志方法
        // 调用父类setVal方法，设置寄存器值
        // 参数1：起始位位置=31（位31）
        // 参数2：位宽度=1位
        // 参数3：有效标志值（0或1）
        setVal(31, 1, val);  // 设置位31的值，宽度为1位，值为val
    }

    /**
     * 获取接收寄存器 - 返回用于存储返回信息的接收寄存器
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回接收寄存器实例</li>
     *   <li>接收寄存器存储从FPGA返回的状态信息</li>
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
     * 获取同步头 - 从返回数据中提取同步头
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从接收寄存器读取同步头</li>
     *   <li>同步头位置：位0-15（16位）</li>
     *   <li>用于数据同步和校验</li>
     * </ul>
     *
     * <p><b>同步头说明：</b>
     * <ul>
     *   <li>同步头用于标识数据帧的起始</li>
     *   <li>通常为固定的魔数或标识符</li>
     *   <li>用于验证数据传输的正确性</li>
     * </ul>
     *
     * @return 同步头值（16位，0-65535）
     */
    public int getSyncHeader(){  // 获取同步头方法
        // 从接收寄存器读取同步头
        // recvReg.getVal(0)：获取第一个4字节数据
        // & 0xFFFF：提取低16位（同步头）
        return recvReg.getVal(0) & 0xFFFF;  // 返回同步头值（低16位）
    }

    /**
     * 获取列地址 - 从返回数据中提取列地址
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从接收寄存器读取列地址</li>
     *   <li>列地址位置：位16-31（16位）</li>
     *   <li>用于数据定位</li>
     * </ul>
     *
     * <p><b>列地址说明：</b>
     * <ul>
     *   <li>列地址用于定位数据在存储器中的位置</li>
     *   <li>通常用于波形数据的列定位</li>
     *   <li>配合帧数使用可实现完整数据定位</li>
     * </ul>
     *
     * @return 列地址值（16位，0-65535）
     */
    public int getColAddr(){  // 获取列地址方法
        // 从接收寄存器读取列地址
        // recvReg.getVal(0)：获取第一个4字节数据
        // >>> 16：右移16位，获取高16位
        // & 0xFFFF：提取16位列地址
        return (recvReg.getVal(0) >>> 16) & 0xFFFF;  // 返回列地址值（高16位）
    }

    /**
     * 获取帧数 - 从返回数据中提取帧数
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从接收寄存器读取帧数</li>
     *   <li>帧数位置：位32-63（32位）</li>
     *   <li>用于数据传输控制</li>
     * </ul>
     *
     * <p><b>帧数说明：</b>
     * <ul>
     *   <li>帧数表示数据帧的数量</li>
     *   <li>用于控制数据传输的流程</li>
     *   <li>可用于验证数据完整性</li>
     * </ul>
     *
     * @return 帧数值（32位）
     */
    public int getFrameNums(){  // 获取帧数方法
        // 从接收寄存器读取帧数
        // recvReg.getVal(1)：获取第二个4字节数据（帧数）
        return recvReg.getVal(1);  // 返回帧数值
    }
}
