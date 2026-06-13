package com.micsig.tbook.scope.probe;

import android.os.SystemClock;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                ProbeCommand - 探头命令构建工具类                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Probe模块的探头命令构建工具类，位于probe包下，                                ║
 * ║   负责构建探头通信协议命令，提供命令帧的组装和校验功能。                          ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 定义探头命令类型常量                                                     ║
 * ║   2. 构建各种探头控制命令                                                     ║
 * ║   3. 计算命令帧校验和                                                         ║
 * ║   4. 管理命令实例和重发计数                                                   ║
 * ║   5. 提供命令监听器接口                                                       ║
 * ║                                                                              ║
 * ║ 【命令帧格式】                                                               ║
 * ║   ┌────────┬────────┬────────┬────────┬────────┐                           ║
 * ║   │ 帧头   │ 类型   │ 长度   │ 数据   │ 校验   │                           ║
 * ║   │ 0xAA   │ 1字节  │ 1字节  │ N字节  │ 1字节  │                           ║
 * ║   └────────┴────────┴────────┴────────┴────────┘                           ║
 * ║                                                                              ║
 * ║ 【命令类型分类】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        命令类型分类                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【查询类命令】                                                      │ ║
 * ║   │   TYPE_PROBE_INFO (0x00)     - 查询探头信息                          │ ║
 * ║   │   TYPE_PROBE_VERSION (0xC4)  - 查询固件版本                          │ ║
 * ║   │   TYPE_PROBE_IMPED (0x31)    - 查询输入电阻                          │ ║
 * ║   │   TYPE_PROBE_RATE (0x04)     - 查询探头衰减比                        │ ║
 * ║   │   TYPE_PROBE_ZERO (0x05)     - 查询零点                              │ ║
 * ║   │                                                                      │ ║
 * ║   │   【控制类命令】                                                      │ ║
 * ║   │   TYPE_PROBE_RATE (0x04)     - 设置探头衰减比                        │ ║
 * ║   │   TYPE_PROBE_ZERO (0x05)     - 设置零点                              │ ║
 * ║   │   TYPE_PROBE_ADJUST (0x09)   - 自动补偿                              │ ║
 * ║   │   TYPE_PROBE_DAGAIN (0x0C)   - 增益控制                              │ ║
 * ║   │   TYPE_PROBE_IMPED (0x31)    - 设置输入电阻                          │ ║
 * ║   │   TYPE_PROBE_STANDBY (0xC8)  - 待机唤醒                              │ ║
 * ║   │                                                                      │ ║
 * ║   │   【配置类命令】                                                      │ ║
 * ║   │   TYPE_PROBE_RATE_LIST (0x0D) - 衰减比列表                          │ ║
 * ║   │   TYPE_PROBE_RATE_CTRL (0x10) - 比例控制                            │ ║
 * ║   │   TYPE_PROBE_RATE_DOT (0x11)  - 比例点列表                          │ ║
 * ║   │   TYPE_PROBE_VRANGE (0xA1)    - 垂直量程                            │ ║
 * ║   │   TYPE_PROBE_UNIT (0xA3)      - 探头单位                            │ ║
 * ║   │   TYPE_BANDWIDTH (0xA4)       - 带宽                                │ ║
 * ║   │                                                                      │ ║
 * ║   │   【升级类命令】                                                      │ ║
 * ║   │   TYPE_JUMPBOOT (0xC5)              - 跳转Bootloader                │ ║
 * ║   │   TYPE_PROBE_UPGRADE_BEGIN (0xC1)   - 开始升级                       │ ║
 * ║   │   TYPE_PROBE_UPGRADE_DATA (0xC2)    - 升级数据                       │ ║
 * ║   │   TYPE_PROBE_UPGRADE_END (0xC3)     - 升级结束                       │ ║
 * ║   │   TYPE_PROBE_UPGRADE_SS (0xC6)      - 升级SS                         │ ║
 * ║   │                                                                      │ ║
 * ║   │   【响应类命令】                                                      │ ║
 * ║   │   TYPE_PROBE_ACK (0xFE)    - 应答                                    │ ║
 * ║   │   TYPE_PROBE_ALARM (0xFA)  - 报警                                    │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【校验和计算】                                                               ║
 * ║   校验和 = ~(所有字节累加和) + 1                                             ║
 * ║   采用补码形式，确保数据完整性                                                ║
 * ║                                                                              ║
 * ║ 【使用流程】                                                                 ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 调用静态方法 │───▶│ 构建命令帧   │───▶│ 返回字节数组 │                   ║
 * ║   │ probeXxx()  │    │ (帧头+类型+  │    │  发送到探头  │                   ║
 * ║   └─────────────┘    │ 长度+数据+   │    └─────────────┘                   ║
 * ║                      │ 校验)        │                                      ║
 * ║                      └─────────────┘                                      ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. BaseProbe发送探头控制命令                                               ║
 * ║   2. 探头参数配置和查询                                                      ║
 * ║   3. 探头固件升级                                                            ║
 * ║   4. 探头状态监控                                                            ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 静态方法线程安全                                                         ║
 * ║   - 实例方法使用synchronized保护counter                                      ║
 * ║   - timestamp使用volatile保证可见性                                          ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - BaseProbe: 调用ProbeCommand构建命令                                     ║
 * ║   - ScopeMessage: 发送ProbeCommand实例到探头                                ║
 * ║   - SystemClock: 获取命令时间戳                                             ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class ProbeCommand {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 命令类型常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 查询探头信息命令
     * 用于获取探头的基本信息
     */
    public final static byte TYPE_PROBE_INFO = (byte) 0x00;

    /**
     * 探头衰减比命令
     * 用于查询和设置探头的衰减比例（1X、10X、100X等）
     */
    public final static byte TYPE_PROBE_RATE = (byte) 0x04;

    /**
     * 零点命令
     * 用于查询和设置探头的零点校准值
     */
    public final static byte TYPE_PROBE_ZERO = (byte) 0x05;

    /**
     * 自动补偿命令
     * 用于启动探头的自动补偿功能
     */
    public final static byte TYPE_PROBE_ADJUST = (byte) 0x09;

    /**
     * 增益命令
     * 用于控制探头的增益设置
     */
    public final static byte TYPE_PROBE_DAGAIN = (byte)0x0C;

    /**
     * 衰减比列表命令
     * 用于获取探头支持的衰减比列表
     */
    public final static byte TYPE_PROBE_RATE_LIST = (byte) 0x0D;

    /**
     * 比例控制命令
     * 用于启用/禁用探头比例自动控制
     */
    public final static byte TYPE_PROBE_RATE_CTRL = (byte) 0x10;

    /**
     * 比例点列表命令
     * 用于获取探头比例点列表
     */
    public final static byte TYPE_PROBE_RATE_DOT = (byte) 0x11;

    /**
     * 输入电阻命令
     * 用于查询和设置探头的输入电阻（50Ω/1MΩ）
     */
    public final static byte TYPE_PROBE_IMPED =  (byte) 0x31;

    /**
     * 垂直量程命令
     * 用于设置探头的垂直量程
     */
    public final static byte TYPE_PROBE_VRANGE = (byte) 0xA1;

    /**
     * 探头单位命令
     * 用于设置探头的测量单位（电压/电流等）
     */
    public final static byte TYPE_PROBE_UNIT = (byte) 0xA3;

    /**
     * 带宽命令
     * 用于查询和设置探头的带宽限制
     */
    public final static byte TYPE_BANDWIDTH = (byte) 0xA4;

    /**
     * 固件版本命令
     * 用于查询探头的固件版本号
     */
    public final static byte TYPE_PROBE_VERSION = (byte) 0xC4;

    /**
     * 跳转Bootloader命令
     * 用于跳转到Bootloader模式进行固件升级
     */
    public final static byte TYPE_JUMPBOOT = (byte) 0xC5;

    /**
     * 开始升级命令
     * 用于启动固件升级流程
     */
    public final static byte TYPE_PROBE_UPGRADE_BEGIN = (byte) 0xC1;

    /**
     * 升级数据命令
     * 用于发送固件升级数据包
     */
    public final static byte TYPE_PROBE_UPGRADE_DATA = (byte) 0xC2;

    /**
     * 升级结束命令
     * 用于结束固件升级流程
     */
    public final static byte TYPE_PROBE_UPGRADE_END = (byte) 0xC3;

    /**
     * 升级SS命令
     * 用于多MCU探头的升级控制
     */
    public final static byte TYPE_PROBE_UPGRADE_SS = (byte) 0xC6;

    /**
     * 待机唤醒命令
     * 用于控制探头进入待机模式或唤醒
     */
    public final static byte TYPE_PROBE_STANDBY = (byte) 0xC8;

    /**
     * 应答命令
     * 用于探头对命令的应答响应
     */
    public final static byte TYPE_PROBE_ACK = (byte) 0xFE;

    /**
     * 报警命令
     * 用于探头发送报警信息
     */
    public final static byte TYPE_PROBE_ALARM = (byte) 0xFA;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 校验和计算方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 计算校验和（公共方法）
     * 对指定范围的字节进行累加校验
     *
     * @param bytes 字节数组
     * @param offset 起始偏移量
     * @param len 计算长度
     * @return 累加校验和
     */
    public static byte checkSum_Command(byte[] bytes,int offset,int len){
        byte checkSum = 0;                                                          // 初始化校验和为0
        for(int i=0;i<len;i++)                                                      // 遍历指定范围
            checkSum += bytes[offset + i];                                          // 累加每个字节
        return checkSum;                                                            // 返回累加和
    }

    /**
     * 计算校验和（私有方法）
     * 计算命令帧的补码校验和
     * 
     * <p><b>计算公式：</b></p>
     * <pre>
     * checkSum = ~(所有字节累加和) + 1
     * </pre>
     * 
     * <p><b>计算范围：</b></p>
     * <ul>
     *   <li>从索引1开始（跳过帧头0xAA）</li>
     *   <li>到倒数第2个字节（跳过校验和字节本身）</li>
     * </ul>
     *
     * @param b 命令字节数组
     * @return 补码校验和
     */
    private static byte checkSum_Command(byte[] b){
        byte checkSum = 0;                                                          // 初始化校验和为0
        for(int i=1;i<b.length-1;i++)                                               // 遍历命令帧（跳过帧头和校验位）
            checkSum += b[i];                                                       // 累加每个字节
        checkSum = (byte) (~checkSum+1);                                            // 取补码（取反加1）
        return checkSum;                                                            // 返回校验和
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 基本命令构建方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构建基本命令帧
     * 生成无数据的基本命令（仅帧头+类型+长度+校验）
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┐
     * │ 0xAA  │ type   │ 0x00   │ 校验和 │
     * └────────┴────────┴────────┴────────┘
     * </pre>
     *
     * @param type 命令类型
     * @return 命令字节数组（4字节）
     */
    public static byte [] probeCommand(byte type){
        byte[] bytes = new byte[4];                                                 // 创建4字节数组
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=type;                                                              // 命令类型
        bytes[2]=(byte) 0x00;                                                       // 数据长度为0
        bytes[3]=checkSum_Command(bytes);                                           // 校验和
        return bytes;                                                               // 返回命令帧
    }

    /**
     * 构建方波输出命令
     * 用于探头校准时的方波输出控制
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┐
     * │ 0xAA  │ 0x09   │ 0x01   │ 数据   │ 校验和 │
     * └────────┴────────┴────────┴────────┴────────┘
     * </pre>
     *
     * @param bEnable true: 启用方波输出
     *                false: 禁用方波输出
     * @return 命令字节数组（8字节）
     */
    public static byte [] ProbeSquareWave(boolean bEnable){
        byte[] bytes = new byte[8];                                                 // 创建8字节数组
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=TYPE_PROBE_ADJUST;                                                 // 自动补偿命令类型
        bytes[2]=(byte) 0x01;                                                       // 数据长度为1
        bytes[3]=(byte) (bEnable ? 0x01 : 0x02);                                    // 数据：1=启用，2=禁用
        bytes[4]=checkSum_Command(bytes);                                           // 校验和
        return bytes;                                                               // 返回命令帧
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 查询命令构建方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 查询探头版本命令
     *
     * @return 命令字节数组
     */
    public static byte [] QueryProbeVersion(){return probeCommand(TYPE_PROBE_VERSION);} // 返回查询版本命令

    /**
     * 查询探头阻抗命令
     *
     * @return 命令字节数组
     */
    public static byte [] QueryProbeImpedCommand() { return probeCommand(TYPE_PROBE_IMPED);} // 返回查询阻抗命令

    /**
     * 查询探头比例命令
     *
     * @return 命令字节数组
     */
    public static byte [] QueryProbeRateCommand(){
        return probeCommand(TYPE_PROBE_RATE);                                       // 返回查询比例命令
    }

    /**
     * 查询探头零点命令
     *
     * @return 命令字节数组
     */
    public static byte [] QueryProbeZeroCommand(){
        return probeCommand(TYPE_PROBE_ZERO);                                       // 返回查询零点命令
    }

    /**
     * 查询探头DA增益命令
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┐
     * │ 0xAA  │ 0x0C   │ 0x01   │ 0x01   │ 校验和 │
     * └────────┴────────┴────────┴────────┴────────┘
     * </pre>
     *
     * @return 命令字节数组（8字节）
     */
    public static byte [] QueryProbeDAGainCommand(){
        byte[] bytes = new byte[8];                                                 // 创建8字节数组
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=TYPE_PROBE_DAGAIN;                                                 // 增益命令类型
        bytes[2]=(byte) 0x01;                                                       // 数据长度为1
        bytes[3]=(byte) 0x01;                                                       // 查询标志
        bytes[4]=checkSum_Command(bytes);                                           // 校验和
        return bytes;                                                               // 返回命令帧
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 设置命令构建方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构建设置DA增益命令
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┬────────┬────────┐
     * │ 0xAA  │ 0x0C   │ 0x03   │ 0x01   │ 高字节 │ 低字节 │ 校验和 │
     * └────────┴────────┴────────┴────────┴────────┴────────┴────────┘
     * </pre>
     *
     * @param val DA增益值
     * @return 命令字节数组（8字节）
     */
    public static byte[] probeDAGainCommand(int val){
        byte[] bytes = new byte[8];                                                 // 创建8字节数组
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=TYPE_PROBE_DAGAIN;                                                 // 增益命令类型
        bytes[2]=(byte) 0x03;                                                       // 数据长度为3
        bytes[3]=(byte) 0x01;                                                       // 设置标志
        bytes[4]=(byte) ((val >>> 8) & 0xFF);                                       // 增益值高字节
        bytes[5]=(byte) ((val) & 0xFF);                                             // 增益值低字节
        bytes[6]=checkSum_Command(bytes);                                           // 校验和
        return bytes;                                                               // 返回命令帧
    }

    /**
     * 构建设置零点命令（单值）
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┬────────┐
     * │ 0xAA  │ 0x05   │ 0x02   │ 高字节 │ 低字节 │ 校验和 │
     * └────────┴────────┴────────┴────────┴────────┴────────┘
     * </pre>
     *
     * @param val 零点值
     * @return 命令字节数组（8字节）
     */
    public static byte [] probeZeroCommand(int val){

        byte[] bytes = new byte[8];                                                 // 创建8字节数组
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=TYPE_PROBE_ZERO;                                                   // 零点命令类型
        bytes[2]=2;                                                                 // 数据长度为2
        bytes[3] =(byte) ((val>>>8) & 0xFF);                                        // 零点值高字节
        bytes[4] =(byte) (val & 0xFF);                                              // 零点值低字节
        bytes[5] = checkSum_Command(bytes);                                         // 校验和
        return bytes;                                                               // 返回命令帧
    }

    /**
     * 构建设置零点命令（多值）
     * 用于多MCU探头（如MDP）的零点设置
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┬────────┬────────┐
     * │ 0xAA  │ 0x05   │ 长度   │ 数量   │ 数据1  │ 数据2  │ 校验和 │
     * └────────┴────────┴────────┴────────┴────────┴────────┴────────┘
     * </pre>
     *
     * @param vals 零点值数组
     * @return 命令字节数组（对齐到4字节）
     */
    public static byte [] probeZeroCommand(int [] vals){
        int len = vals.length * 2 + 1 + 4;                                          // 计算数据长度
        len = (len + 3)/4;                                                          // 向上对齐到4字节
        len *= 4;                                                                   // 计算最终长度
        byte[] bytes = new byte[len];                                               // 创建字节数组
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=TYPE_PROBE_ZERO;                                                   // 零点命令类型
        bytes[2]=(byte) ((vals.length * 2 + 1) & 0xFF);                             // 数据长度
        bytes[3]=(byte) (vals.length & 0xFF);                                       // 零点值数量
        for(int i=0;i<vals.length;i++)                                              // 遍历零点值数组
        {
            bytes[4 + i * 2] =(byte) ((vals[i]>>>8) & 0xFF);                        // 零点值高字节
            bytes[5 + i * 2] =(byte) ((vals[i]) & 0xFF);                            // 零点值低字节
        }
        bytes[4 + vals.length * 2] = checkSum_Command(bytes);                       // 校验和
        return bytes;                                                               // 返回命令帧
    }

    /**
     * 构建设置阻抗命令
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┐
     * │ 0xAA  │ 0x31   │ 0x01   │ 数据   │ 校验和 │
     * └────────┴────────┴────────┴────────┴────────┘
     * </pre>
     * <p><b>数据值：</b></p>
     * <ul>
     *   <li>1: 1MΩ阻抗</li>
     *   <li>2: 50Ω阻抗</li>
     * </ul>
     *
     * @param bImped50 true: 50Ω阻抗
     *                 false: 1MΩ阻抗
     * @return 命令字节数组（8字节）
     */
    public static byte [] probeImpedCommand(boolean bImped50){
        byte[] bytes = new byte[8];                                                 // 创建8字节数组
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=TYPE_PROBE_IMPED;                                                  // 阻抗命令类型
        bytes[2]=(byte) 1;                                                          // 数据长度为1
        bytes[3]=(byte) (bImped50 ? 2 : 1);                                         // 数据：1=1MΩ，2=50Ω
        bytes[4]=checkSum_Command(bytes);                                           // 校验和

        return bytes;                                                               // 返回命令帧
    }

    /**
     * 构建比例控制命令
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┐
     * │ 0xAA  │ 0x10   │ 0x01   │ 数据   │ 校验和 │
     * └────────┴────────┴────────┴────────┴────────┘
     * </pre>
     * <p><b>数据值：</b></p>
     * <ul>
     *   <li>1: 禁用自动比例控制</li>
     *   <li>2: 启用自动比例控制</li>
     * </ul>
     *
     * @param bCtrl true: 启用自动比例控制
     *              false: 禁用自动比例控制
     * @return 命令字节数组（8字节）
     */
    public static byte [] probeRateCtrlCommand(boolean bCtrl){
        byte[] bytes = new byte[8];                                                 // 创建8字节数组
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=TYPE_PROBE_RATE_CTRL;                                              // 比例控制命令类型
        bytes[2]=(byte) 1;                                                          // 数据长度为1
        bytes[3]=(byte) (bCtrl ? 1 : 2);                                            // 数据：1=启用，2=禁用
        bytes[4]=checkSum_Command(bytes);                                           // 校验和
        return bytes;                                                               // 返回命令帧
    }

    /**
     * 构建自动零点命令
     * 启动探头的自动零点校准功能
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┐
     * │ 0xAA  │ 0x05   │ 0x01   │ 0x00   │ 校验和 │
     * └────────┴────────┴────────┴────────┴────────┘
     * </pre>
     *
     * @return 命令字节数组（8字节）
     */
    public static byte [] probeAutoZeroCommand(){
        byte[] bytes = new byte[8];                                                 // 创建8字节数组
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=TYPE_PROBE_ZERO;                                                   // 零点命令类型
        bytes[2]=(byte) 1;                                                          // 数据长度为1
        bytes[3]=(byte) 0;                                                          // 数据：0表示自动零点
        bytes[4]=checkSum_Command(bytes);                                           // 校验和

        return bytes;                                                               // 返回命令帧
    }

    /**
     * 构建设置探头比例命令
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┐
     * │ 0xAA  │ 0x04   │ 0x04   │ 字节3  │ 字节2  │ 字节1  │ 字节0  │ 校验和 │
     * └────────┴────────┴────────┴────────┴────────┴────────┴────────┴────────┘
     * </pre>
     * <p><b>数据编码：</b></p>
     * <ul>
     *   <li>比例 >= 1: 直接存储整数值</li>
     *   <li>比例 < 1: 存储倒数值，最高位置1</li>
     * </ul>
     *
     * @param val 比例值（编码后的int值）
     * @return 命令字节数组（8字节）
     */
    public static byte [] probeRateCommand(int val){

        byte[] bytes = new byte[8];                                                 // 创建8字节数组
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=TYPE_PROBE_RATE;                                                   // 比例命令类型
        bytes[2]=(byte) 4;                                                          // 数据长度为4
        bytes[3]=(byte) ((val >>>24) & 0xFF);                                       // 第3字节（最高位）
        bytes[4]=(byte) ((val >>>16) & 0xFF);                                       // 第2字节
        bytes[5]=(byte) ((val >>>8) & 0xFF);                                        // 第1字节
        bytes[6]=(byte) (val & 0xFF);                                               // 第0字节（最低位）
        bytes[7]=checkSum_Command(bytes);                                           // 校验和

        return bytes;                                                               // 返回命令帧
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 升级命令构建方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构建跳转Bootloader命令
     * 用于跳转到Bootloader模式进行固件升级
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┬────────┐
     * │ 0xAA  │ 0xC5   │ 0x02   │ 0x55   │ 0xAA   │ 校验和 │
     * └────────┴────────┴────────┴────────┴────────┴────────┘
     * </pre>
     *
     * @return 命令字节数组（8字节）
     */
    public static byte [] jumpBootCommand(){
        byte[] bytes=new byte[8];                                                   // 创建8字节数组
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=TYPE_JUMPBOOT;                                                     // 跳转Bootloader命令类型
        bytes[2]=(byte) 0x02;                                                       // 数据长度为2
        bytes[3]=(byte) 0x55;                                                       // 魔数第1字节
        bytes[4]=(byte) 0xAA;                                                       // 魔数第2字节
        bytes[5]=checkSum_Command(bytes);                                           // 校验和
        return bytes;                                                               // 返回命令帧
    }

    /**
     * 构建升级开始命令
     * 用于启动固件升级流程
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┬────────┬────────┐
     * │ 0xAA  │ 0xC1   │ 0x03   │ 高字节 │ 低字节 │  CRC   │ 校验和 │
     * └────────┴────────┴────────┴────────┴────────┴────────┴────────┘
     * </pre>
     *
     * @param n 固件数据包数量
     * @param crc 固件CRC校验值
     * @return 命令字节数组（8字节）
     */
    public static byte [] probeUpgradeBeginCommand(int n, byte crc){
        byte[] bytes=new byte[8];                                                   // 创建8字节数组
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=TYPE_PROBE_UPGRADE_BEGIN;                                          // 升级开始命令类型
        bytes[2]=(byte) 0x03;                                                       // 数据长度为3
        bytes[3]=(byte) ((n >>> 8) & 0xFF);                                         // 包数量高字节
        bytes[4]=(byte) (n & 0xFF);                                                 // 包数量低字节
        bytes[5]=crc;                                                               // CRC校验值
        bytes[6]=checkSum_Command(bytes);                                           // 校验和
        return bytes;                                                               // 返回命令帧
    }

    /**
     * 构建升级数据命令
     * 用于发送固件升级数据包
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┬────────┬────────┐
     * │ 0xAA  │ 0xC2   │ 长度   │ 序号高 │ 序号低 │ 数据   │ 校验和 │
     * └────────┴────────┴────────┴────────┴────────┴────────┴────────┘
     * </pre>
     *
     * @param n 数据包序号
     * @param datas 固件数据数组
     * @param offset 数据起始偏移量
     * @param len 数据长度
     * @return 命令字节数组（136字节）
     */
    public static byte [] probeUpgradeCommand(int n,byte[] datas,int offset,int len){
        byte[] bytes=new byte[136];                                                 // 创建136字节数组（固定长度）
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=TYPE_PROBE_UPGRADE_DATA;                                           // 升级数据命令类型
        bytes[2]=(byte) (len + 2);                                                  // 数据长度（+2为序号字节）
        bytes[3]=(byte) ((n >>> 8) & 0xFF);                                         // 序号高字节
        bytes[4]=(byte) (n & 0xFF);                                                 // 序号低字节
        for(int i=0;i<len;i++){                                                     // 遍历数据
            bytes[5 + i] = datas[offset + i];                                       // 复制数据字节
        }
        bytes[5 + len]=checkSum_Command(bytes);                                     // 校验和
        return bytes;                                                               // 返回命令帧
    }

    /**
     * 构建升级SS命令
     * 用于多MCU探头的升级控制
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┐
     * │ 0xAA  │ 0xC6   │ 0x01   │ MCU索引│ 校验和 │
     * └────────┴────────┴────────┴────────┴────────┘
     * </pre>
     *
     * @param mcuIdx MCU索引（0或1）
     * @return 命令字节数组（8字节）
     */
    public static byte [] probeUpgradeSSCommand(int mcuIdx){
        byte[] bytes=new byte[8];                                                   // 创建8字节数组
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=TYPE_PROBE_UPGRADE_SS;                                             // 升级SS命令类型
        bytes[2]=(byte) (1);                                                        // 数据长度为1
        bytes[3]=(byte) (mcuIdx & 0xFF);                                            // MCU索引
        bytes[4]=checkSum_Command(bytes);                                           // 校验和
        return bytes;                                                               // 返回命令帧
    }

    /**
     * 构建待机命令
     * 用于控制探头进入待机模式或唤醒
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┐
     * │ 0xAA  │ 0xC8   │ 0x01   │ 数据   │ 校验和 │
     * └────────┴────────┴────────┴────────┴────────┘
     * </pre>
     * <p><b>数据值：</b></p>
     * <ul>
     *   <li>1: 进入待机模式</li>
     *   <li>2: 唤醒</li>
     * </ul>
     *
     * @param val 控制值（1=待机，2=唤醒）
     * @return 命令字节数组（8字节）
     */
    public static byte [] probeStandByCommand(int val){
        byte[] bytes=new byte[8];                                                   // 创建8字节数组
        bytes[0]=(byte) 0xAA;                                                       // 帧头
        bytes[1]=TYPE_PROBE_STANDBY;                                                // 待机命令类型
        bytes[2]=(byte) (1);                                                        // 数据长度为1
        bytes[3]=(byte) (val & 0xFF);                                               // 控制值
        bytes[4]=checkSum_Command(bytes);                                           // 校验和
        return bytes;                                                               // 返回命令帧
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 通道索引
     * 标识命令所属的通道（0-3）
     */
    private int chIdx;

    /**
     * 命令字节数组
     * 存储完整的命令帧数据
     */
    private byte[] cmd;

    /**
     * 命令时间戳
     * 记录命令创建的时间（毫秒）
     * 使用volatile保证多线程可见性
     */
    private volatile long timestamp;

    /**
     * 重发计数器
     * 记录命令的重发次数
     * 使用synchronized保护多线程访问
     */
    private int counter;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造ProbeCommand实例
     * 创建命令对象并记录时间戳
     *
     * @param chIdx 通道索引
     * @param cmd 命令字节数组
     */
    public ProbeCommand(int chIdx,byte[] cmd){
        this.cmd = cmd;                                                             // 保存命令字节数组
        this.chIdx = chIdx;                                                         // 保存通道索引
        this.timestamp = SystemClock.elapsedRealtime();                             // 记录当前时间戳
        counter=1;                                                                 // 初始化重发计数为1
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // Getter和Setter方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取通道索引
     *
     * @return 通道索引
     */
    public int getChIdx() {
        return chIdx;                                                               // 返回通道索引
    }

    /**
     * 获取命令字节数组
     *
     * @return 命令字节数组
     */
    public byte[] getCmd() {
        return cmd;                                                                 // 返回命令字节数组
    }

    /**
     * 设置命令字节数组
     *
     * @param cmd 命令字节数组
     */
    public void setCmd(byte[] cmd){
        this.cmd = cmd;                                                             // 设置命令字节数组
    }

    /**
     * 获取命令时间戳
     *
     * @return 时间戳（毫秒）
     */
    public long getTimestamp() {
        return timestamp;                                                           // 返回时间戳
    }

    /**
     * 获取重发计数
     * 线程安全方法
     *
     * @return 重发计数
     */
    public synchronized int getCounter(){
        return counter;                                                             // 返回重发计数
    }

    /**
     * 增加重发计数
     * 线程安全方法
     */
    public synchronized void addCounter(){
        counter++;                                                                  // 重发计数加1
    }

    /**
     * 获取命令类型
     * 从命令字节数组中提取命令类型
     *
     * @return 命令类型字节
     */
    public byte getCmdType(){
        return cmd[1];                                                              // 返回命令类型（第2字节）
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 接口定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 探头命令监听器接口
     * 用于接收探头命令的回调通知
     */
    public interface ProbeCommandlistener{
        /**
         * 探头命令回调
         *
         * @param command ProbeCommand实例
         */
        void onProbeCommand(ProbeCommand command);
    }
}
