package com.micsig.tbook.scope.fpga;                                                     // 包声明：FPGA模块

import com.micsig.tbook.scope.Calibrate.HwConfig;                                        // 导入HwConfig类：硬件配置类
import com.micsig.tbook.scope.Sample.SegmentSample;                                      // 导入SegmentSample类：分段采样管理类
import com.micsig.tbook.scope.Scope;                                                     // 导入Scope类：示波器核心类

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║        FPGAReg_SegmentFrame - 示波器FPGA分段帧寄存器类                          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   FPGA模块的分段帧寄存器类，位于fpga包下，                                      ║
 * ║   继承自FPGAReg基类，负责配置FPGA分段采样模式的帧间隔和帧数量。                  ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 设置帧间隔（相邻帧之间的时间间隔）                                        ║
 * ║   2. 设置帧数量（捕获的帧数量）                                               ║
 * ║   3. 自动计算帧间隔和帧数量                                                  ║
 * ║   4. 提供分段采样模式支持                                                    ║
 * ║   5. 继承FPGAReg基类的寄存器操作功能                                          ║
 * ║                                                                              ║
 * ║ 【架构位置】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        FPGA寄存器架构                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   ┌─────────────────┐         ┌─────────────────┐                   │ ║
 * ║   │   │    FPGAReg      │────────▶│FPGAReg_Segment  │                   │ ║
 * ║   │   │   (寄存器基类)   │         │    Frame        │                   │ ║
 * ║   │   └─────────────────┘         └────────┬────────┘                   │ ║
 * ║   │          │                             │                            │ ║
 * ║   │          ▼                             ▼                            │ ║
 * ║   │   ┌─────────────────┐         ┌─────────────────┐                   │ ║
 * ║   │   │  寄存器操作方法  │         │  FPGACommand    │                   │ ║
 * ║   │   │  (setVal/getVal) │         │   (命令管理)     │                   │ ║
 * ║   │   │  onCommand回调   │         │                 │                   │ ║
 * ║   │   └─────────────────┘         └─────────────────┘                   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【寄存器数据结构】                                                           ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        寄存器数据格式                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   寄存器ID：FPGA_SEGMENT_FRAME                                        │ ║
 * ║   │   数据长度：4字节（32位）                                              │ ║
 * ║   │   寄存器类型：写寄存器（用于配置分段采样参数）                            │ ║
 * ║   │                                                                      │ ║
 * ║   │   【位定义】                                                           │ ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐   │ ║
 * ║   │   │  位范围   │  位宽  │  参数名称      │  说明                  │   │ ║
 * ║   │   ├─────────────────────────────────────────────────────────────┤   │ ║
 * ║   │   │   0-7     │   8   │  frameSpace    │  帧间隔                │   │ ║
 * ║   │   │   8-31    │  24   │  frameNums     │  帧数量                │   │ ║
 * ║   │   └─────────────────────────────────────────────────────────────┘   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【分段采样说明】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        分段采样模式详解                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   【分段采样定义】                                                     │ ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐   │ ║
 * ║   │   │  分段采样：一种特殊的采样模式，用于捕获多个波形片段              │   │ ║
 * ║   │   │  - 将存储深度分成多个帧                                          │   │ ║
 * ║   │   │  - 每帧捕获一个波形片段                                          │   │ ║
 * ║   │   │  - 适用于捕获多个触发事件                                        │   │ ║
 * ║   │   │  - 用于分析多次触发事件的时间关系                                │   │ ║
 * ║   │   └─────────────────────────────────────────────────────────────┘   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【帧间隔说明】                                                       │ ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐   │ ║
 * ║   │   │  帧间隔：相邻帧之间的时间间隔                                    │   │ ║
 * ║   │   │  - 决定相邻帧的时间距离                                          │   │ ║
 * ║   │   │  - 影响帧的时间分布                                              │   │ ║
 * ║   │   │  - 根据存储深度自动计算                                          │   │ ║
 * ║   │   │                                                                 │   │ ║
 * ║   │   │  帧间隔计算：                                                    │   │ ║
 * ║   │   │  - 存储深度 <= 16M：帧间隔 = log2(zun)                           │   │ ║
 * ║   │   │  - 存储深度 > 16M：帧间隔 = 0x80 | k（k为倍数）                  │   │ ║
 * ║   │   └─────────────────────────────────────────────────────────────┘   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【帧数量说明】                                                       │ ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐   │ ║
 * ║   │   │  帧数量：捕获的帧数量                                            │   │ ║
 * ║   │   │  - 决定捕获多少个波形片段                                        │   │ ║
 * ║   │   │  - 根据存储深度和分段长度计算                                    │   │ ║
 * │   │   │  - 最大帧数：SEGMENT_MAX                                         │   │ ║
 * ║   │   │  - 用户可设置帧数：segmentNums + 1                               │   │ ║
 * ║   │   │                                                                 │   │ ║
 * ║   │   │  帧数量计算：                                                    │   │ ║
 * ║   │   │  - maxNums = SEGMENT_LENGTH / val                                │   │ ║
 * ║   │   │  - maxNums = maxNums * fpgaNums / channelSampOnCnt               │   │ ║
 * ║   │   │  - nums = segmentNums + 1                                        │   │ ║
 * ║   │   │  - nums = min(nums, maxNums, SEGMENT_MAX)                        │   │ ║
 * ║   │   └─────────────────────────────────────────────────────────────┘   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【分段采样应用】                                                     │ ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐   │ ║
 * ║   │   │  1. 捕获多次触发事件                                            │   │ ║
 * ║   │   │  2. 分析触发事件的时间关系                                      │   │ ║
 * ║   │   │  3. 观察信号的变化趋势                                          │   │ ║
 * ║   │   │  4. 记录长时间的信号变化                                        │   │ ║
 * ║   │   └─────────────────────────────────────────────────────────────┘   │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【使用流程】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        寄存器使用流程                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   分段采样模式触发                                                    │ ║
 * ║   │         │                                                             │ ║
 * ║   │         ▼                                                             │ ║
 * ║   │   FPGACommand.cmdSegment()                                           │ ║
 * ║   │         │                                                             │ ║
 * ║   │         ▼                                                             │ ║
 * ║   │   runCommand(fpgaIdx, FPGA_SEGMENT_FRAME)                            │ ║
 * ║   │         │                                                             │ ║
 * ║   │         ▼                                                             │ ║
 * ║   │   FPGAReg_SegmentFrame.onCommand()                                   │ ║
 * ║   │         │                                                             │ ║
 * ║   │         ▼                                                             │ ║
 * ║   │   自动计算帧间隔和帧数量                                              │ ║
 * ║   │   - 计算存储深度zun                                                   │ ║
 * ║   │   - 计算帧间隔frameSpace                                              │ ║
 * ║   │   - 计算最大帧数maxNums                                               │ ║
 * ║   │   - 计算实际帧数nums                                                  │ ║
 * ║   │         │                                                             │ ║
 * ║   │         ▼                                                             │ ║
 * ║   │   setFrameSpace(k)                                                   │ ║
 * ║   │   setFrameNums(nums)                                                 │ ║
 * ║   │         │                                                             │ ║
 * ║   │         ▼                                                             │ ║
 * ║   │   发送命令到FPGA                                                      │ ║
 * ║   │         │                                                             │ ║
 * ║   │         ▼                                                             │ ║
 * ║   │   FPGA配置分段采样参数                                                │ ║
 * ║   │         │                                                             │ ║
 * ║   │         ▼                                                             │ ║
 * ║   │   分段采样执行                                                        │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 继承模式：继承FPGAReg基类，复用寄存器操作功能                           ║
 * ║   - 回调模式：重写onCommand方法，自动计算参数                              ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - FPGAReg: FPGA寄存器基类                                               ║
 * ║   - FPGACommand: FPGA命令管理类，负责发送分段采样命令                      ║
 * ║   - SegmentSample: 分段采样管理类，管理分段采样参数                        ║
 * ║   - Scope: 示波器核心类，提供存储深度和通道信息                            ║
 * ║   - HwConfig: 硬件配置类，提供FPGA数量信息                                ║
 * ║                                                                              ║
 * ║ 【寄存器关系】                                                               ║
 * ║   - FPGA_SEGMENT_FRAME: 分段帧寄存器（本类）                              ║
 * ║   - FPGA_SAMP_MODE: 采样模式寄存器                                        ║
 * ║   - FPGA_ZUN_DEPTH: 存储深度寄存器                                        ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 分段采样模式配置：配置分段采样的帧间隔和帧数量                        ║
 * ║   2. 多次触发捕获：捕获多个触发事件的波形片段                              ║
 * ║   3. 时间关系分析：分析多次触发事件的时间关系                              ║
 * ║   4. 信号变化观察：观察信号的变化趋势                                      ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ║ 【创建日期】 2018-4-8                                                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 示波器FPGA分段帧寄存器类
 * 继承自FPGAReg基类，负责配置FPGA分段采样模式的帧间隔和帧数量
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>设置帧间隔（相邻帧之间的时间间隔）</li>
 *   <li>设置帧数量（捕获的帧数量）</li>
 *   <li>自动计算帧间隔和帧数量</li>
 *   <li>提供分段采样模式支持</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b></p>
 * <ul>
 *   <li>寄存器ID：FPGA_SEGMENT_FRAME</li>
 *   <li>数据长度：4字节（32位）</li>
 *   <li>寄存器类型：写寄存器（用于配置分段采样参数）</li>
 * </ul>
 *
 * <p><b>位定义：</b></p>
 * <ul>
 *   <li>位0-7：帧间隔（8位）</li>
 *   <li>位8-31：帧数量（24位）</li>
 * </ul>
 *
 * <p><b>分段采样说明：</b></p>
 * <pre>
 * 分段采样：一种特殊的采样模式，用于捕获多个波形片段
 * - 将存储深度分成多个帧
 * - 每帧捕获一个波形片段
 * - 适用于捕获多个触发事件
 * - 用于分析多次触发事件的时间关系
 *
 * 帧间隔：相邻帧之间的时间间隔
 * - 决定相邻帧的时间距离
 * - 影响帧的时间分布
 * - 根据存储深度自动计算
 *
 * 帧数量：捕获的帧数量
 * - 决定捕获多少个波形片段
 * - 根据存储深度和分段长度计算
 * - 最大帧数：SEGMENT_MAX
 * </pre>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 配置分段采样参数
 * FPGACommand fpgaCmd = FPGACommand.getInstance();
 * FPGAReg_SegmentFrame reg = (FPGAReg_SegmentFrame)
 *     fpgaCmd.getFPGAReg(fpgaIdx, FPGAReg.FPGA_SEGMENT_FRAME);
 *
 * // 手动设置帧间隔和帧数量
 * reg.setFrameSpace(10);  // 设置帧间隔为10
 * reg.setFrameNums(100);  // 设置帧数量为100
 *
 * // 发送命令到FPGA（会自动调用onCommand计算参数）
 * fpgaCmd.runCommand(fpgaIdx, FPGAReg.FPGA_SEGMENT_FRAME);
 *
 * // 内部操作：
 * // onCommand()方法会自动计算帧间隔和帧数量：
 * // 1. 计算存储深度zun
 * // 2. 根据存储深度计算帧间隔
 * // 3. 计算最大帧数
 * // 4. 计算实际帧数
 * // 5. 设置帧间隔和帧数量
 * </pre>
 *
 * @see FPGAReg
 * @see FPGACommand#cmdSegment(int)
 * @see SegmentSample
 * @see Scope
 * @see HwConfig
 */
public class FPGAReg_SegmentFrame extends FPGAReg {                                       // 类声明：FPGA分段帧寄存器类，继承FPGAReg


    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造函数
     * 初始化FPGA分段帧寄存器
     *
     * <p><b>初始化参数：</b></p>
     * <ul>
     *   <li>寄存器ID：FPGA_SEGMENT_FRAME</li>
     *   <li>数据长度：4字节（32位）</li>
     * </ul>
     *
     * <p><b>寄存器结构：</b></p>
     * <ul>
     *   <li>位0-7：帧间隔（8位）</li>
     *   <li>位8-31：帧数量（24位）</li>
     * </ul>
     *
     * <p><b>使用说明：</b></p>
     * <ul>
     *   <li>创建寄存器对象后可以手动设置帧间隔和帧数量</li>
     *   <li>也可以通过onCommand方法自动计算参数</li>
     *   <li>然后通过FPGACommand发送命令到FPGA</li>
     *   <li>FPGA配置分段采样参数并执行分段采样</li>
     * </ul>
     *
     * <p><b>分段采样说明：</b></p>
     * <ul>
     *   <li>分段采样：捕获多个波形片段</li>
     *   <li>帧间隔：相邻帧之间的时间间隔</li>
     *   <li>帧数量：捕获的帧数量</li>
     * </ul>
     */
    public FPGAReg_SegmentFrame(){                                                       // 构造方法：初始化FPGA分段帧寄存器
        super(FPGAReg.FPGA_SEGMENT_FRAME,4);                                             // 调用父类构造函数，设置寄存器ID和数据长度4字节
    }                                                                                    // 构造方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 分段帧参数设置
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置帧间隔
     * 设置相邻帧之间的时间间隔
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>设置相邻帧之间的时间间隔</li>
     *   <li>决定帧的时间分布</li>
     *   <li>影响分段采样的时间分辨率</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b></p>
     * <pre>
     * 位0-7：设置为帧间隔值val（8位）
     *
     * 帧间隔值范围：
     * - 最小值：0
     * - 最大值：255
     * - 实际值：根据存储深度自动计算
     * </pre>
     *
     * <p><b>帧间隔计算：</b></p>
     * <pre>
     * 存储深度 <= 16M：
     * - 帧间隔 = log2(zun)
     * - k = 0, val = 1
     * - while(val < zun) { val *= 2; k++; }
     * - setFrameSpace(k)
     *
     * 存储深度 > 16M：
     * - 帧间隔 = 0x80 | k
     * - k = 1
     * - while(k * SEGMENT_16M < zun) { k++; }
     * - setFrameSpace(1 << 7 | k)
     * </pre>
     *
     * <p><b>应用场景：</b></p>
     * <ul>
     *   <li>分段采样模式配置</li>
     *   <li>调整帧的时间分布</li>
     *   <li>优化分段采样性能</li>
     * </ul>
     *
     * @param val 帧间隔值（8位，范围0-255）
     */
    public void setFrameSpace(int val){                                                  // 公有方法：设置帧间隔
        setVal(0,8,val);                                                                 // 设置字0的位0-7为帧间隔值val（8位）
    }                                                                                    // 方法结束

    /**
     * 设置帧数量
     * 设置捕获的帧数量
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>设置捕获的帧数量</li>
     *   <li>决定捕获多少个波形片段</li>
     *   <li>影响分段采样的数据量</li>
     * </ul>
     *
     * <p><b>寄存器位设置：</b></p>
     * <pre>
     * 位8-31：设置为帧数量值val（24位）
     *
     * 帧数量值范围：
     * - 最小值：1
     * - 最大值：2^24 - 1 = 16777215
     * - 实际最大值：SEGMENT_MAX
     * </pre>
     *
     * <p><b>帧数量计算：</b></p>
     * <pre>
     * 帧数量计算公式：
     * maxNums = SEGMENT_LENGTH / val
     * maxNums = maxNums * fpgaNums / channelSampOnCnt
     * nums = segmentNums + 1
     * nums = min(nums, maxNums, SEGMENT_MAX)
     *
     * 参数说明：
     * - SEGMENT_LENGTH：分段长度（固定值）
     * - val：帧间隔对应的值
     * - fpgaNums：FPGA数量
     * - channelSampOnCnt：通道采样数量
     * - segmentNums：用户设置的段数
     * - SEGMENT_MAX：最大帧数限制
     * </pre>
     *
     * <p><b>应用场景：</b></p>
     * <ul>
     *   <li>分段采样模式配置</li>
     *   <li>调整捕获的帧数量</li>
     *   <li>优化分段采样数据量</li>
     * </ul>
     *
     * @param val 帧数量值（24位，范围1-SEGMENT_MAX）
     */
    public void setFrameNums(int val){                                                   // 公有方法：设置帧数量
        setVal(8,24,val);                                                                // 设置字0的位8-31为帧数量值val（24位）
    }                                                                                    // 方法结束

    // ═══════════════════════════════════════════════════════════════════════════════
    // 重写方法 - 命令回调
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 命令回调方法
     * 重写父类方法，自动计算帧间隔和帧数量
     *
     * <p><b>功能说明：</b></p>
     * <ul>
     *   <li>重写父类onCommand方法</li>
     *   <li>自动计算帧间隔和帧数量</li>
     *   <li>根据存储深度和分段采样参数计算</li>
     *   <li>设置帧间隔和帧数量到寄存器</li>
     * </ul>
     *
     * <p><b>计算流程：</b></p>
     * <pre>
     * 1. 获取Scope和SegmentSample实例
     * 2. 计算存储深度zun
     * 3. 根据存储深度计算帧间隔：
     *    - 存储深度 <= 16M：帧间隔 = log2(zun)
     *    - 存储深度 > 16M：帧间隔 = 0x80 | k
     * 4. 计算最大帧数maxNums：
     *    - maxNums = SEGMENT_LENGTH / val
     *    - maxNums = maxNums * fpgaNums / channelSampOnCnt
     * 5. 计算实际帧数nums：
     *    - nums = segmentNums + 1
     *    - nums = min(nums, maxNums, SEGMENT_MAX)
     * 6. 设置帧间隔和帧数量到寄存器
     * </pre>
     *
     * <p><b>帧间隔计算详解：</b></p>
     * <pre>
     * 存储深度 <= 16M：
     * - k = 0, val = 1
     * - while(val < zun) { val *= 2; k++; }
     * - setFrameSpace(k)
     * - maxNums = SEGMENT_LENGTH / val
     *
     * 存储深度 > 16M：
     * - k = 1
     * - while(k * SEGMENT_16M < zun) { k++; }
     * - setFrameSpace(1 << 7 | k)
     * - maxNums = SEGMENT_LENGTH / (k * SEGMENT_16M)
     * </pre>
     *
     * <p><b>帧数量计算详解：</b></p>
     * <pre>
     * maxNums = maxNums * fpgaNums / channelSampOnCnt
     * nums = segmentNums + 1
     * if(nums < 1 || nums > maxNums) { nums = maxNums; }
     * if(nums > SEGMENT_MAX) { nums = SEGMENT_MAX; }
     * setFrameNums(nums)
     * </pre>
     *
     * <p><b>参数说明：</b></p>
     * <ul>
     *   <li>zun：存储深度</li>
     *   <li>SEGMENT_16M：16M分段长度常量</li>
     *   <li>SEGMENT_LENGTH：分段长度</li>
     *   <li>SEGMENT_MAX：最大帧数</li>
     *   <li>fpgaNums：FPGA数量</li>
     *   <li>channelSampOnCnt：通道采样数量</li>
     *   <li>segmentNums：用户设置的段数</li>
     * </ul>
     *
     * <p><b>应用场景：</b></p>
     * <ul>
     *   <li>分段采样模式配置</li>
     *   <li>自动计算分段采样参数</li>
     *   <li>优化分段采样性能</li>
     * </ul>
     */
    @Override                                                                            // 注解：重写父类方法
    public void onCommand() {                                                            // 公有方法：命令回调，自动计算帧间隔和帧数量
        Scope scope = Scope.getInstance();                                               // 获取Scope实例：示波器核心类
        SegmentSample segmentSample = SegmentSample.getInstance();                       // 获取SegmentSample实例：分段采样管理类
        int maxNums = 0;                                                                 // 初始化最大帧数变量maxNums为0

        long zun =  scope.zunMemDepth(scope.isRun(true));                                // 计算存储深度zun：根据运行状态获取存储深度
        if(zun <= SegmentSample.SEGMENT_16M){                                            // 条件判断：如果存储深度小于等于16M
            int k = 0;                                                                   // 初始化帧间隔指数k为0
            int val = 1;                                                                 // 初始化帧间隔值val为1
            while( val < zun){                                                           // 循环：计算log2(zun)，直到val >= zun
                val *= 2;                                                                // val乘以2，计算下一个2的幂次
                k++;                                                                     // k加1，记录指数值
            }                                                                            // 循环结束
            setFrameSpace(k);                                                            // 设置帧间隔为k（log2(zun)）
            maxNums = (int) (SegmentSample.SEGMENT_LENGTH / val);                         // 计算最大帧数：分段长度除以帧间隔值
        } else {                                                                         // 条件分支：如果存储深度大于16M
            int k = 1;                                                                   // 初始化倍数k为1
            while (((long) k * SegmentSample.SEGMENT_16M) < zun) {                       // 循环：计算存储深度是16M的多少倍
                k++;                                                                     // k加1，增加倍数
            }                                                                            // 循环结束
            setFrameSpace(1 << 7 | k);                                                   // 设置帧间隔：最高位（位7）置1，低7位为倍数k
            maxNums = (int) (SegmentSample.SEGMENT_LENGTH / (k * SegmentSample.SEGMENT_16M)); // 计算最大帧数：分段长度除以(k * 16M)
        }                                                                                // 条件判断结束
        maxNums = maxNums * HwConfig.getInstance().getFpgaNums() / scope.getChannelSampOnCnt(scope.isRun(true)); // 计算最大帧数：乘以FPGA数量，除以通道采样数量

        int nums = segmentSample.getSegmentNums()+1;                                     // 计算实际帧数：用户设置的段数加1

        if(nums < 1  || nums > maxNums){                                                 // 条件判断：如果帧数小于1或大于最大帧数
            nums = maxNums;                                                              // 设置帧数为最大帧数
        }                                                                                // 条件判断结束
        if(nums > SegmentSample.SEGMENT_MAX){                                            // 条件判断：如果帧数大于最大帧数限制
            nums = SegmentSample.SEGMENT_MAX;                                            // 设置帧数为最大帧数限制SEGMENT_MAX
        }                                                                                // 条件判断结束
        setFrameNums(nums);                                                              // 设置帧数量为计算后的帧数nums
    }                                                                                    // 方法结束
}                                                                                        // 类结束