package com.micsig.tbook.scope.probe;

import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.ScopeMessage;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                ProbeUpgrade - 探头固件升级线程类                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Probe模块的探头固件升级线程类，位于probe包下，                                ║
 * ║   继承Thread，实现探头固件的在线升级功能（OTA）。                               ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理探头固件升级流程                                                     ║
 * ║   2. 实现升级状态机（5个状态）                                                 ║
 * ║   3. 发送升级命令和处理应答                                                   ║
 * ║   4. 支持多MCU探头升级                                                        ║
 * ║   5. 发送升级进度事件                                                         ║
 * ║                                                                              ║
 * ║ 【升级状态机】                                                               ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        升级状态机                                     │ ║
 * ║   │                                                                      │ ║
 * ║   │   ┌────────┐                                                         │ ║
 * ║   │   │ State 0│ ──── SS命令 ────▶ ┌────────┐                           │ ║
 * ║   │   │(多MCU) │                   │ State 1│                           │ ║
 * ║   │   └────────┘                   │跳转Boot│                           │ ║
 * ║   │                                └───┬────┘                           │ ║
 * ║   │                                    │                                │ ║
 * ║   │                                    ▼                                │ ║
 * ║   │                              ┌────────┐                             │ ║
 * ║   │                              │ State 2│                             │ ║
 * ║   │                              │开始升级 │                             │ ║
 * ║   │                              └───┬────┘                             │ ║
 * ║   │                                  │                                  │ ║
 * ║   │                                  ▼                                  │ ║
 * ║   │                          ┌────────────┐                             │ ║
 * ║   │                          │   State 3  │◀─────┐                      │ ║
 * ║   │                          │ 发送升级数据 │      │                      │ ║
 * ║   │                          └─────┬──────┘      │                      │ ║
 * ║   │                                │             │                      │ ║
 * ║   │                                ▼             │                      │ ║
 * ║   │                          ┌────────────┐      │                      │ ║
 * ║   │                          │ 包序号+1   │──────┘                      │ ║
 * ║   │                          │ (未完成)   │                             │ ║
 * ║   │                          └─────┬──────┘                             │ ║
 * ║   │                                │ (完成)                             │ ║
 * ║   │                                ▼                                    │ ║
 * ║   │                          ┌────────┐                                 │ ║
 * ║   │                          │ State 4│                                 │ ║
 * ║   │                          │结束升级 │                                 │ ║
 * ║   │                          └───┬────┘                                 │ ║
 * ║   │                              │                                      │ ║
 * ║   │                              ▼                                      │ ║
 * ║   │                        ┌──────────┐                                 │ ║
 * ║   │                        │ MCU索引-1│                                 │ ║
 * ║   │                        │(多MCU继续)│                                 │ ║
 * ║   │                        └──────────┘                                 │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【升级流程】                                                                 ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 读取固件文件 │───▶│ 跳转Bootloader│───▶│ 发送升级数据 │                   ║
 * ║   │ (.bin)      │    │ 模式        │    │ (128字节/包)│                   ║
 * ║   └─────────────┘    └─────────────┘    └──────┬──────┘                   ║
 * ║                                                 │                          ║
 * ║                                                 ▼                          ║
 * ║                                        ┌─────────────┐                     ║
 * ║                                        │ 结束升级     │                     ║
 * ║                                        │ 重启探头     │                     ║
 * ║                                        └─────────────┘                     ║
 * ║                                                                              ║
 * ║ 【多MCU升级】                                                                ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                      多MCU探头升级流程                                │ ║
 * ║   │                                                                      │ ║
 * ║   │   MDP差分探头有2个MCU，需要分别升级：                                 │ ║
 * ║   │                                                                      │ ║
 * ║   │   1. 发送SS命令选择MCU1 (mcuIdx=1)                                   │ ║
 * ║   │   2. 跳转Bootloader                                                 │ ║
 * ║   │   3. 发送MCU1固件数据                                                │ ║
 * ║   │   4. 结束MCU1升级                                                    │ ║
 * ║   │   5. 发送SS命令选择MCU2 (mcuIdx=0)                                   │ ║
 * ║   │   6. 跳转Bootloader                                                 │ ║
 * ║   │   7. 发送MCU2固件数据                                                │ ║
 * ║   │   8. 结束MCU2升级                                                    │ ║
 * ║   │   9. 升级完成                                                        │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 用户手动升级探头固件                                                   ║
 * ║   2. 自动检测并升级新版本固件                                               ║
 * ║   3. 探头固件Bug修复                                                        ║
 * ║   4. 探头功能升级                                                           ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 所有成员变量使用volatile保证可见性                                      ║
 * ║   - 状态变更使用synchronized保护                                            ║
 * ║   - 使用wait/notify进行线程同步                                             ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - ProbeCommand: 构建升级命令                                             ║
 * ║   - ProbeUtils: 读取固件文件                                               ║
 * ║   - ProbeUpgradeInfo: 升级进度信息                                         ║
 * ║   - ScopeMessage: 发送命令到探头                                           ║
 * ║   - EventFactory: 发送升级事件                                             ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class ProbeUpgrade extends Thread {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 升级数据块大小
     * 每个升级数据包包含128字节的固件数据
     */
    private final static int BLOCK_SIZE = 128;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 通道索引
     * 标识待升级探头所属的通道（0-3）
     * 使用volatile保证多线程可见性
     */
    public volatile int chIdx;

    /**
     * 固件数据数组
     * 存储从文件读取的固件二进制数据
     * 使用volatile保证多线程可见性
     */
    public volatile byte [] datas;

    /**
     * 版本代码
     * 标识固件版本号
     * 使用volatile保证多线程可见性
     */
    private volatile int verCode = 0;

    /**
     * MCU数量
     * 探头包含的MCU数量（1=单MCU，2=双MCU如MDP）
     * 使用volatile保证多线程可见性
     */
    private volatile int mcuNums = 0;

    /**
     * 硬件版本字符串
     * 标识探头的硬件版本，用于查找对应的固件文件
     * 使用volatile保证多线程可见性
     */
    private volatile String hwVer = "";

    /**
     * 当前MCU索引
     * 用于多MCU探头的升级进度跟踪
     * 取值范围：mcuNums ~ 1（倒序升级）
     * 使用volatile保证多线程可见性
     */
    private volatile int mcuIdx = 0;

    /**
     * 运行标志
     * true: 线程运行中
     * false: 线程应退出
     * 使用volatile保证多线程可见性
     */
    private volatile boolean bRun = false;

    /**
     * 状态机当前状态
     * 0: 发送SS命令（多MCU探头）
     * 1: 跳转Bootloader
     * 2: 开始升级
     * 3: 发送升级数据
     * 4: 结束升级
     * 使用volatile保证多线程可见性
     */
    private volatile int state = 0;

    /**
     * 是否已跳转到Bootloader标志
     * true: 已在Bootloader模式
     * false: 在应用程序模式
     * 使用volatile保证多线程可见性
     */
    private volatile boolean isBoot = false;

    /**
     * 固件总包数
     * 固件数据按128字节分块的总包数
     * 使用volatile保证多线程可见性
     */
    private volatile int totalPkgs = 0;

    /**
     * 当前包序号
     * 正在发送的升级数据包序号（从0开始）
     * 使用volatile保证多线程可见性
     */
    private volatile int curPkg = 0;

    /**
     * 重试计数器
     * 记录当前状态的重试次数
     * 超过5次则判定为升级失败
     * 使用volatile保证多线程可见性
     */
    private volatile int counter = 0;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造ProbeUpgrade实例并启动升级线程
     * 初始化升级参数并立即启动升级流程
     *
     * @param chIdx 通道索引（0-3）
     * @param hwVer 硬件版本字符串（用于查找固件文件）
     * @param verCode 版本代码
     * @param mcuNums MCU数量（1=单MCU，2=双MCU）
     * @param isBoot 是否已在Bootloader模式
     */
    public ProbeUpgrade(int chIdx,String hwVer,int verCode,int mcuNums,boolean isBoot){
        super();                                                                    // 调用父类Thread构造方法
        Log.d("zhuzh", "ProbeUpgrade() called with: chIdx = [" + chIdx + "], hwVer = [" + hwVer + "], verCode = [" + verCode + "], mcuNums = [" + mcuNums + "], isBoot = [" + isBoot + "]"); // 打印调试日志
        this.chIdx = chIdx;                                                         // 设置通道索引
        this.hwVer = hwVer;                                                         // 设置硬件版本
        this.verCode = verCode;                                                     // 设置版本代码
        this.mcuNums = mcuNums;                                                     // 设置MCU数量
        this.isBoot = isBoot;                                                       // 设置Bootloader标志
        mcuIdx = mcuNums;                                                           // 初始化MCU索引（从最后一个MCU开始）
        state = 0;                                                                  // 初始化状态为0
        bRun = true;                                                                // 设置运行标志
        start();                                                                    // 启动升级线程
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 事件发送方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 发送升级事件
     * 通过EventFactory发送升级进度通知
     *
     * @param info 升级信息对象
     */
    private void sendEvent(ProbeUpgradeInfo info){
        Logger.d("zhuzh", "sendEvent() called with: info = [" + info + "]");        // 打印调试日志
        EventFactory.sendEvent(new EventBase(EventFactory.EVENT_PROBE_UPGRADE, info),true); // 发送升级事件
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 线程主方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 线程主方法
     * 实现升级状态机的主循环
     * 
     * <p><b>状态机流程：</b></p>
     * <ol>
     *   <li>State 0: 发送SS命令选择MCU（多MCU探头）</li>
     *   <li>State 1: 跳转Bootloader</li>
     *   <li>State 2: 开始升级，读取固件文件</li>
     *   <li>State 3: 发送升级数据包</li>
     *   <li>State 4: 结束升级</li>
     * </ol>
     */
    @Override
    public void run() {
        super.run();                                                                // 调用父类run方法
        if(mcuNums == 1){                                                           // 单MCU探头
            state = 1;                                                              // 直接跳转到State 1
        }
        if(isBoot){                                                                 // 已在Bootloader模式
            state = 2;                                                              // 直接跳转到State 2
//            mcuIdx = 1;
        }
        counter = 0;                                                                // 初始化重试计数器
        boolean bAbort = false;                                                     // 中止标志
        sendEvent(new ProbeUpgradeInfo(chIdx,ProbeUpgradeInfo.UPGRADE_BEGIN,0));   // 发送升级开始事件
        while (bRun){                                                               // 主循环
            synchronized (this){                                                    // 同步保护
                Log.d("zhuzh","state:" + state + ",mcuIdx:" + mcuIdx);              // 打印状态日志
                switch (state){                                                     // 状态机
                    case 0:                                                         // State 0: 发送SS命令
                        sendCommand(ProbeCommand.probeUpgradeSSCommand(mcuIdx - 1)); // 发送SS命令选择MCU
                        break;
                    case 1:                                                         // State 1: 跳转Bootloader
                        sendCommand(ProbeCommand.jumpBootCommand());                // 发送跳转Bootloader命令
                        break;
                    case 2:                                                         // State 2: 开始升级
                        ms_sleep(1000);                                             // 延迟1秒
                        onUpgradeBegin();                                           // 执行开始升级
                        break;
                    case 3:                                                         // State 3: 发送升级数据
                        onUpgrade();                                                // 发送升级数据包
                        break;
                    case 4:                                                         // State 4: 结束升级
                        ms_sleep(2000);                                             // 延迟2秒
                        onUpgradeEnd();                                             // 执行结束升级
                        break;
                }
                try {
                    this.wait(ProbeFactory.PROBE_TIMEOUT_MS * (ProbeFactory.PROBE_SEND_MAX + 1) ); // 等待应答或超时
                } catch (InterruptedException e) {
                    e.printStackTrace();                                            // 打印异常堆栈
                }
                counter++;                                                          // 重试计数器加1
                if(counter > 5){                                                    // 重试超过5次
                    if(state == 1){                                                 // State 1失败
                        state = 2;                                                  // 尝试直接进入State 2
                        counter = 0;                                                // 重置计数器
                    }else{                                                          // 其他状态失败
                        bAbort = true;                                              // 设置中止标志
                        break;                                                      // 跳出循环
                    }
                }
            }
        }
        int val = 100;                                                              // 升级结果值（100=成功）
        if((bRun && bAbort)){                                                       // 升级中止
            val = -1;                                                               // 设置失败值
        }else{                                                                      // 升级成功
            sendCommand(ProbeCommand.probeCommand(ProbeCommand.TYPE_PROBE_INFO), 1000); // 发送查询探头信息命令
        }
        sendEvent(new ProbeUpgradeInfo(chIdx,ProbeUpgradeInfo.UPGRADE_END,val));   // 发送升级结束事件
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 工具方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 线程休眠
     * 使当前线程休眠指定的毫秒数
     *
     * @param ms 休眠时间（毫秒）
     */
    public void ms_sleep(long ms){
        try {
            Thread.sleep(ms);                                                       // 线程休眠
        } catch (InterruptedException e) {
            e.printStackTrace();                                                    // 打印异常堆栈
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 状态控制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 结束升级
     * 设置结束标志并唤醒等待线程
     */
    public void onEnd(){
        synchronized (this) {                                                       // 同步保护
            mcuNums = 0;                                                            // 清零MCU数量
            bRun = false;                                                           // 清除运行标志
            this.notifyAll();                                                       // 唤醒所有等待线程
        }
    }

    /**
     * 设置状态机状态
     * 线程安全方法，重置重试计数器并唤醒等待线程
     *
     * @param state 新状态值
     */
    private void setState(int state){
        synchronized (this){                                                        // 同步保护
            this.state = state;                                                     // 设置新状态
            counter = 0;                                                            // 重置重试计数器
            this.notifyAll();                                                       // 唤醒等待线程
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 应答处理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 处理跳转Bootloader命令应答
     * 当应答值在0x01-0x0F之间时，表示跳转成功
     *
     * @param bytes 应答数据
     * @param offset 数据偏移量
     * @param len 数据长度
     */
    public void AckJumpBootCommand(byte[] bytes,int offset,int len){
        if(bytes[offset] > 0 && bytes[offset] < 0x0f ) {                            // 应答值有效
            setState(2);                                                            // 跳转到State 2
        }
    }

    /**
     * 处理开始升级命令应答
     * 当应答值为0时，表示可以开始发送数据
     *
     * @param bytes 应答数据
     * @param offset 数据偏移量
     * @param len 数据长度
     */
    public void AckUpgradeBegin(byte[] bytes,int offset,int len){
        if(bytes[offset] == 0) {                                                    // 应答值为0，表示成功
            setState(3);                                                            // 跳转到State 3
        }
    }

    /**
     * 处理升级数据命令应答
     * 当应答包序号匹配且状态为0时，继续发送下一包
     *
     * @param bytes 应答数据
     * @param offset 数据偏移量
     * @param len 数据长度
     */
    public void AckUpgrade(byte[] bytes,int offset,int len){
        int pkg = (bytes[offset + 0] << 8) | (bytes[offset + 1]);                   // 解析应答包序号
        if(pkg == curPkg+1) {                                                       // 包序号匹配
            if (bytes[offset + 2] == 0) {                                           // 状态为0，表示成功
                synchronized (this) {                                               // 同步保护
                    curPkg++;                                                       // 当前包序号加1
                }
                if (curPkg < totalPkgs) {                                           // 还有数据包未发送
                    setState(3);                                                    // 继续发送下一包
                } else {                                                            // 所有数据包已发送
                    setState(4);                                                    // 跳转到State 4
                }
            }
        }
    }

    /**
     * 处理结束升级命令应答
     * 当应答值为0时，MCU升级完成
     * 多MCU探头继续升级下一个MCU
     *
     * @param bytes 应答数据
     * @param offset 数据偏移量
     * @param len 数据长度
     */
    public void AckUpgradeEnd(byte[] bytes,int offset,int len){
        if(bytes[offset] == 0) {                                                    // 应答值为0，表示成功
            synchronized (this) {                                                   // 同步保护
                mcuIdx--;                                                           // MCU索引减1
            }
            if (mcuIdx > 0) {                                                       // 还有MCU未升级
                setState(0);                                                        // 回到State 0，升级下一个MCU
            } else {                                                                // 所有MCU升级完成
                onEnd();                                                            // 结束升级
            }
        }else {                                                                     // 应答值非0，失败
            synchronized (this) {                                                   // 同步保护
                this.notifyAll();                                                   // 唤醒等待线程，重试
            }
        }
    }

    /**
     * 处理SS命令应答
     * 当应答值为0时，MCU选择成功
     *
     * @param bytes 应答数据
     * @param offset 数据偏移量
     * @param len 数据长度
     */
    public void AckUpgradeSS(byte[] bytes,int offset,int len){
        if(bytes[offset] == 0) {                                                    // 应答值为0，表示成功
            setState(1);                                                            // 跳转到State 1
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 升级处理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 开始升级处理
     * 读取固件文件，计算总包数和CRC校验值，发送开始升级命令
     */
    public void onUpgradeBegin(){
        Logger.d("zhuzh","hwVer:" + hwVer +",verCode:" + verCode + ",mcuIdx:" + mcuIdx); // 打印调试日志
        datas = ProbeUtils.readBin(hwVer,verCode,mcuIdx);                          // 读取固件文件
        if(datas != null) {                                                         // 固件数据读取成功
            totalPkgs = datas.length / BLOCK_SIZE;                                 // 计算总包数
            curPkg = 0;                                                             // 初始化当前包序号
            byte crc = 0;                                                           // CRC校验值
            for (byte data : datas) {                                               // 遍历固件数据
                crc += data;                                                        // 累加计算CRC
            }
            sendCommand(ProbeCommand.probeUpgradeBeginCommand(totalPkgs, crc));    // 发送开始升级命令
        }
    }

    /**
     * 发送升级数据包
     * 发送当前数据包并更新进度
     */
    public void onUpgrade(){
        sendCommand(ProbeCommand.probeUpgradeCommand(curPkg + 1,datas,curPkg * BLOCK_SIZE,BLOCK_SIZE)); // 发送升级数据包
        int progress = 0;                                                           // 进度值
        double n = 100.0 / mcuNums;                                                 // 每个MCU的进度占比
        progress = (int) ((mcuNums - mcuIdx) * n + curPkg * n / totalPkgs);         // 计算总进度
        sendEvent(new ProbeUpgradeInfo(chIdx,ProbeUpgradeInfo.UPGRADE_DATA,progress)); // 发送进度事件
    }

    /**
     * 结束升级处理
     * 发送结束升级命令
     */
    public void onUpgradeEnd(){
        sendCommand(ProbeCommand.probeCommand(ProbeCommand.TYPE_PROBE_UPGRADE_END)); // 发送结束升级命令

    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 命令发送方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 发送命令（无延迟）
     *
     * @param bytes 命令字节数组
     */
    private void sendCommand(byte [] bytes){
        ScopeMessage scopeMessage = ScopeMessage.getInstance();                     // 获取消息管理实例
        scopeMessage.sendProbe(chIdx,bytes);                                        // 发送命令到探头
    }

    /**
     * 发送命令（带延迟）
     *
     * @param bytes 命令字节数组
     * @param ms 延迟时间（毫秒）
     */
    private void sendCommand(byte [] bytes,long ms){
        ScopeMessage scopeMessage = ScopeMessage.getInstance();                     // 获取消息管理实例
        scopeMessage.sendProbe(chIdx,bytes,ms);                                     // 发送命令到探头（带延迟）
    }
}
