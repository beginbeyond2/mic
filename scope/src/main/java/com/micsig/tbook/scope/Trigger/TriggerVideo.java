package com.micsig.tbook.scope.Trigger;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                   TriggerVideo - 视频触发器类                                ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Trigger模块的视频触发器实现类，专门用于视频信号触发采集。                   ║
 * ║   视频触发是一种专用触发方式，适用于分析和调试视频信号。                      ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理视频极性（正极性/负极性）                                           ║
 * ║   2. 管理视频标准（PAL/SECAM/NTSC/720P/1080I/1080P）                        ║
 * ║   3. 管理视频触发方式（奇数场/偶数场/所有场/所有行/指定行）                  ║
 * ║   4. 管理视频频率（60Hz/50Hz/30Hz/25Hz/24Hz）                               ║
 * ║   5. 管理触发行号                                                            ║
 * ║   6. 提供视频参数计算功能                                                    ║
 * ║                                                                              ║
 * ║ 【继承体系】                                                                 ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │   Trigger       │ ← 抽象基类                      ║
 * ║                          │   (abstract)    │                                 ║
 * ║                          └────────┬────────┘                                 ║
 * ║                                   │                                          ║
 * ║         ┌───────────┬─────────────┼─────────────┬───────────┐               ║
 * ║         │           │             │             │           │               ║
 * ║         ▼           ▼             ▼             ▼           ▼               ║
 * ║   ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐         ║
 * ║   │Edge      │ │PulseWidth│ │Logic     │ │TimeOut   │ │Video     │         ║
 * ║   │边沿触发  │ │脉宽触发  │ │逻辑触发  │ │超时触发  │ │视频触发  │         ║
 * ║   │(单源)    │ │(单源)    │ │(多源)    │ │(单源)    │ │(单源)    │         ║
 * ║   └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘         ║
 * ║                                              ↑                              ║
 * ║                                            本类                             ║
 * ║                                                                              ║
 * ║ 【视频信号说明】                                                             ║
 * ║   视频信号是一种复杂的模拟信号，包含同步脉冲、消隐期和有效图像数据。          ║
 * ║   示波器视频触发可以在特定的场或行上触发采集，便于分析视频信号。              ║
 * ║                                                                              ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                     视频信号结构示意图                               │   ║
 * ║   │                                                                     │   ║
 * ║   │   一帧（Frame）= 两场（Field）                                      │   ║
 * ║   │                                                                     │   ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐  │   ║
 * ║   │   │                    奇数场（Odd Field）                      │  │   ║
 * ║   │   │  ┌─────────────────────────────────────────────────────┐   │  │   ║
 * ║   │   │  │ 行1  │ 行2  │ 行3  │ ... │ 行312 │ 行313 │ ...    │   │  │   ║
 * ║   │   │  └─────────────────────────────────────────────────────┘   │  │   ║
 * ║   │   └─────────────────────────────────────────────────────────────┘  │   ║
 * ║   │                                                                     │   ║
 * ║   │   ┌─────────────────────────────────────────────────────────────┐  │   ║
 * ║   │   │                    偶数场（Even Field）                     │  │   ║
 * ║   │   │  ┌─────────────────────────────────────────────────────┐   │  │   ║
 * ║   │   │  │ 行1  │ 行2  │ 行3  │ ... │ 行312 │ 行313 │ ...    │   │  │   ║
 * ║   │   │  └─────────────────────────────────────────────────────┘   │  │   ║
 * ║   │   └─────────────────────────────────────────────────────────────┘  │   ║
 * ║   │                                                                     │   ║
 * ║   │   隔行扫描：奇数场 + 偶数场 = 一帧完整图像                          │   ║
 * ║   │   逐行扫描：每帧只有一场，包含所有行                                │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【视频标准说明】                                                             ║
 * ║   ┌────────────────┬──────────┬──────────┬──────────────────────────────┐  ║
 * ║   │ 标准           │ 总行数   │ 帧率     │ 说明                         │  ║
 * ║   ├────────────────┼──────────┼──────────┼──────────────────────────────┤  ║
 * ║   │ PAL            │ 625      │ 25Hz     │ 欧洲标准，隔行扫描           │  ║
 * ║   │ SECAM          │ 625      │ 25Hz     │ 法国/俄罗斯标准，隔行扫描    │  ║
 * ║   │ NTSC           │ 525      │ 30Hz     │ 美国/日本标准，隔行扫描      │  ║
 * ║   │ 720P           │ 750      │ 60Hz     │ 高清标准，逐行扫描           │  ║
 * ║   │ 1080I          │ 1125     │ 30Hz     │ 全高清标准，隔行扫描         │  ║
 * ║   │ 1080P          │ 1125     │ 60Hz     │ 全高清标准，逐行扫描         │  ║
 * ║   └────────────────┴──────────┴──────────┴──────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【视频触发方式说明】                                                         ║
 * ║   ┌────────────────┬────────────────────────────────────────────────────┐  ║
 * ║   │ 触发方式       │ 说明                                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 奇数场(ODD)    │ 在奇数场的场同步信号上触发                          │  ║
 * ║   │                │ 适用于分析奇数场内容                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 偶数场(EVEN)   │ 在偶数场的场同步信号上触发                          │  ║
 * ║   │                │ 适用于分析偶数场内容                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 所有场(ALL)    │ 在所有场的场同步信号上触发                          │  ║
 * ║   │                │ 适用于分析任意场内容                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 所有行(LINES)  │ 在所有行的行同步信号上触发                          │  ║
 * ║   │                │ 适用于分析行同步信号                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 指定行(LINE)   │ 在指定行号的行同步信号上触发                        │  ║
 * ║   │                │ 适用于分析特定行内容                                │  ║
 * ║   └────────────────┴────────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【视频极性说明】                                                             ║
 * ║   ┌────────────────┬────────────────────────────────────────────────────┐  ║
 * ║   │ 极性           │ 说明                                                │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 正极性(POS)    │ 同步脉冲为低电平，有效视频为高电平                  │  ║
 * ║   │                │ 大多数视频信号使用正极性                            │  ║
 * ║   ├────────────────┼────────────────────────────────────────────────────┤  ║
 * ║   │ 负极性(NEG)    │ 同步脉冲为高电平，有效视频为低电平                  │  ║
 * ║   │                │ 部分特殊视频信号使用负极性                          │  ║
 * ║   └────────────────┴────────────────────────────────────────────────────┘  ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   → Trigger: 继承触发器基类                                                 ║
 * ║   → TriggerFactory: 工厂类，管理触发器实例                                  ║
 * ║                                                                              ║
 * ║ 【使用场景】                                                                 ║
 * ║   1. 用户选择视频触发类型时，TriggerFactory创建TriggerVideo实例            ║
 * ║   2. 用户设置视频标准（PAL/NTSC/720P/1080P等）时                            ║
 * ║   3. 用户设置触发方式（奇数场/偶数场/指定行）时                              ║
 * ║   4. 用户设置触发行号时                                                     ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 模板方法模式（Template Method）：继承Trigger的抽象方法                 ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   触发参数未加同步保护，建议在UI线程操作。                                   ║
 * ║                                                                              ║
 * ║ 【作者】 Created by zhuzh on 2018-6-1                                        ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TriggerVideo extends Trigger {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 视频极性常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 视频极性：正极性
     * 同步脉冲为低电平，有效视频为高电平
     * 大多数视频信号使用此极性
     */
    public static final int VIDEO_POLARITY_POSITIVE = 0;

    /**
     * 视频极性：负极性
     * 同步脉冲为高电平，有效视频为低电平
     * 部分特殊视频信号使用此极性
     */
    public static final int VIDEO_POLARITY_NEGATIVE = 1;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 视频标准常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 视频标准：PAL制式
     * 625行，25Hz帧率
     * 欧洲标准，隔行扫描
     */
    public static final int VIDEO_STANDARD_625_PAL = 0;

    /**
     * 视频标准：SECAM制式
     * 625行，25Hz帧率
     * 法国/俄罗斯标准，隔行扫描
     */
    public static final int VIDEO_STANDARD_SECAM = 1;

    /**
     * 视频标准：NTSC制式
     * 525行，30Hz帧率
     * 美国/日本标准，隔行扫描
     */
    public static final int VIDEO_STANDARD_525NTSC = 2;

    /**
     * 视频标准：720P高清
     * 750行，60Hz帧率
     * 高清标准，逐行扫描
     */
    public static final int VIDEO_STANDARD_720P = 3;

    /**
     * 视频标准：1080I全高清隔行
     * 1125行，30Hz帧率
     * 全高清标准，隔行扫描
     */
    public static final int VIDEO_STANDARD_1080I = 4;

    /**
     * 视频标准：1080P全高清逐行
     * 1125行，60Hz帧率
     * 全高清标准，逐行扫描
     */
    public static final int VIDEO_STANDARD_1080P = 5;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 视频触发方式常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 视频触发方式：奇数场触发
     * 在奇数场的场同步信号上触发
     * 适用于分析奇数场内容
     */
    public static final int VIDEO_TRIGGER_ODD_FIELDS = 0;

    /**
     * 视频触发方式：偶数场触发
     * 在偶数场的场同步信号上触发
     * 适用于分析偶数场内容
     */
    public static final int VIDEO_TRIGGER_EVEN_FIELDS = 1;

    /**
     * 视频触发方式：所有场触发
     * 在所有场的场同步信号上触发
     * 适用于分析任意场内容
     */
    public static final int VIDEO_TRIGGER_ALL_FIELDS = 2;

    /**
     * 视频触发方式：所有行触发
     * 在所有行的行同步信号上触发
     * 适用于分析行同步信号
     */
    public static final int VIDEO_TRIGGER_ALL_LINES = 3;

    /**
     * 视频触发方式：指定行触发
     * 在指定行号的行同步信号上触发
     * 需要配合line参数使用
     */
    public static final int VIDEO_TRIGGER_LINE = 4;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 视频频率常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 视频频率：60Hz
     * 用于高清视频标准（720P/1080P）
     * 注意：这五个值可以变，界面调用用的名字而非值
     */
    public static final int VIDEO_FREQUENCY_60HZ = 0;

    /**
     * 视频频率：50Hz
     * 用于PAL/SECAM标准
     */
    public static final int VIDEO_FREQUENCY_50HZ = 1;

    /**
     * 视频频率：30Hz
     * 用于NTSC/1080I标准
     */
    public static final int VIDEO_FREQUENCY_30HZ = 2;

    /**
     * 视频频率：25Hz
     * 用于PAL/SECAM标准
     */
    public static final int VIDEO_FREQUENCY_25HZ = 3;

    /**
     * 视频频率：24Hz
     * 用于电影帧率
     */
    public static final int VIDEO_FREQUENCY_24HZ = 4;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 当前视频极性
     * 取值：VIDEO_POLARITY_POSITIVE 或 VIDEO_POLARITY_NEGATIVE
     * 默认值：VIDEO_POLARITY_POSITIVE（正极性）
     */
    private int polarity = VIDEO_POLARITY_POSITIVE;

    /**
     * 当前视频标准
     * 取值：VIDEO_STANDARD_625_PAL、VIDEO_STANDARD_SECAM、VIDEO_STANDARD_525NTSC、
     *       VIDEO_STANDARD_720P、VIDEO_STANDARD_1080I、VIDEO_STANDARD_1080P
     * 默认值：VIDEO_STANDARD_625_PAL（PAL制式）
     */
    private int standard = VIDEO_STANDARD_625_PAL;

    /**
     * 当前视频触发方式
     * 取值：VIDEO_TRIGGER_ODD_FIELDS、VIDEO_TRIGGER_EVEN_FIELDS、
     *       VIDEO_TRIGGER_ALL_FIELDS、VIDEO_TRIGGER_ALL_LINES、VIDEO_TRIGGER_LINE
     * 默认值：VIDEO_TRIGGER_ODD_FIELDS（奇数场触发）
     */
    private int videoTrigger = VIDEO_TRIGGER_ODD_FIELDS;

    /**
     * 当前视频频率
     * 取值：VIDEO_FREQUENCY_60HZ、VIDEO_FREQUENCY_50HZ、VIDEO_FREQUENCY_30HZ、
     *       VIDEO_FREQUENCY_25HZ、VIDEO_FREQUENCY_24HZ
     * 默认值：VIDEO_FREQUENCY_60HZ（60Hz）
     */
    private int videoFrequency = VIDEO_FREQUENCY_60HZ;

    /**
     * 触发行号
     * 当videoTrigger为VIDEO_TRIGGER_LINE时有效
     * 取值范围：1 ~ 总行数（根据视频标准确定）
     * 默认值：1（第一行）
     */
    private int line = 1;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造视频触发器实例
     * 调用父类Trigger构造方法设置触发类型为视频触发
     *
     * <p><b>初始化流程：</b>
     * <ol>
     *   <li>调用父类Trigger构造方法，设置触发类型为TRIG_TYPE_VIDEO</li>
     * </ol>
     *
     * @example
     *   TriggerVideo trigger = new TriggerVideo();
     */
    public TriggerVideo() {
        super(Trigger.TRIG_TYPE_VIDEO);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 视频极性访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前视频极性
     *
     * @return 视频极性
     *         VIDEO_POLARITY_POSITIVE (0): 正极性
     *         VIDEO_POLARITY_NEGATIVE (1): 负极性
     */
    public int getPolarity() {
        return polarity;
    }

    /**
     * 设置视频极性
     * 更新视频极性并发送FPGA消息
     *
     * @param polarity 视频极性
     *                 VIDEO_POLARITY_POSITIVE: 正极性
     *                 VIDEO_POLARITY_NEGATIVE: 负极性
     *
     * @note 设置后会发送FPGA_CMD_TRIG命令
     */
    public void setPolarity(int polarity) {
        this.polarity = polarity;
        triggerParamChange();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 视频标准访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前视频标准
     *
     * @return 视频标准
     *         VIDEO_STANDARD_625_PAL (0): PAL制式
     *         VIDEO_STANDARD_SECAM (1): SECAM制式
     *         VIDEO_STANDARD_525NTSC (2): NTSC制式
     *         VIDEO_STANDARD_720P (3): 720P高清
     *         VIDEO_STANDARD_1080I (4): 1080I全高清隔行
     *         VIDEO_STANDARD_1080P (5): 1080P全高清逐行
     */
    public int getStandard() {
        return standard;
    }

    /**
     * 设置视频标准
     * 更新视频标准并发送FPGA消息
     *
     * @param standard 视频标准
     *                 VIDEO_STANDARD_625_PAL: PAL制式
     *                 VIDEO_STANDARD_SECAM: SECAM制式
     *                 VIDEO_STANDARD_525NTSC: NTSC制式
     *                 VIDEO_STANDARD_720P: 720P高清
     *                 VIDEO_STANDARD_1080I: 1080I全高清隔行
     *                 VIDEO_STANDARD_1080P: 1080P全高清逐行
     *
     * @note 设置后会发送FPGA_CMD_TRIG命令
     */
    public void setStandard(int standard) {
        this.standard = standard;
        triggerParamChange();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 视频触发方式访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前视频触发方式
     *
     * @return 视频触发方式
     *         VIDEO_TRIGGER_ODD_FIELDS (0): 奇数场触发
     *         VIDEO_TRIGGER_EVEN_FIELDS (1): 偶数场触发
     *         VIDEO_TRIGGER_ALL_FIELDS (2): 所有场触发
     *         VIDEO_TRIGGER_ALL_LINES (3): 所有行触发
     *         VIDEO_TRIGGER_LINE (4): 指定行触发
     */
    public int getVideoTrigger() {
        return videoTrigger;
    }

    /**
     * 设置视频触发方式
     * 更新视频触发方式并发送FPGA消息
     *
     * @param videoTrigger 视频触发方式
     *                     VIDEO_TRIGGER_ODD_FIELDS: 奇数场触发
     *                     VIDEO_TRIGGER_EVEN_FIELDS: 偶数场触发
     *                     VIDEO_TRIGGER_ALL_FIELDS: 所有场触发
     *                     VIDEO_TRIGGER_ALL_LINES: 所有行触发
     *                     VIDEO_TRIGGER_LINE: 指定行触发
     *
     * @note 设置后会发送FPGA_CMD_TRIG命令
     */
    public void setVideoTrigger(int videoTrigger) {
        this.videoTrigger = videoTrigger;
        triggerParamChange();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 视频参数计算方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 根据视频标准获取总行数
     * 不同的视频标准有不同的总行数
     *
     * <p><b>各标准总行数：</b>
     * <ul>
     *   <li>PAL/SECAM: 625行</li>
     *   <li>NTSC: 525行</li>
     *   <li>720P: 750行</li>
     *   <li>1080I/1080P: 1125行</li>
     * </ul>
     *
     * @param std 视频标准
     *            VIDEO_STANDARD_625_PAL、VIDEO_STANDARD_SECAM、VIDEO_STANDARD_525NTSC、
     *            VIDEO_STANDARD_720P、VIDEO_STANDARD_1080I、VIDEO_STANDARD_1080P
     *
     * @return 视频标准的总行数
     */
    public int triggerVideoTotalLineCnt(int std) {
        switch(std) {
            case VIDEO_STANDARD_625_PAL:
            case VIDEO_STANDARD_SECAM:
                return 625;
            case VIDEO_STANDARD_525NTSC:
                return 525;
            case VIDEO_STANDARD_720P:
                return 750;
            case VIDEO_STANDARD_1080I:
            case VIDEO_STANDARD_1080P:
            default:
                return 1125;
        }
    }

    /**
     * 获取当前视频标准的总行数
     * 使用当前设置的standard值调用triggerVideoTotalLineCnt(int)
     *
     * @return 当前视频标准的总行数
     */
    public int triggerVideoTotalLineCnt() {
        return triggerVideoTotalLineCnt(standard);
    }

    /**
     * 计算行与行之间的间隔时间
     * 根据视频频率和总行数计算每行的时间间隔
     *
     * <p><b>计算公式：</b>
     * <pre>
     *   行间隔时间 = (1秒 / 帧率) / 总行数 / 4ns时钟周期
     *   即：(1000*1000*1000/4) / 帧率 / 总行数
     * </pre>
     *
     * <p><b>计算步骤：</b>
     * <ol>
     *   <li>根据videoFrequency确定帧率（60/50/30/25/24）</li>
     *   <li>获取当前视频标准的总行数</li>
     *   <li>计算行间隔时间（以4ns时钟为基准）</li>
     * </ol>
     *
     * @return 行与行之间的间隔时间（以4ns时钟周期为单位）
     */
    public int triggerVideoTimeFieldLine() {
        int cnt = 60;
        switch(videoFrequency) {
            case VIDEO_FREQUENCY_60HZ:
                cnt = 60;
                break;
            case VIDEO_FREQUENCY_50HZ:
                cnt = 50;
                break;
            case VIDEO_FREQUENCY_30HZ:
                cnt = 30;
                break;
            case VIDEO_FREQUENCY_25HZ:
                cnt = 25;
                break;
            case VIDEO_FREQUENCY_24HZ:
                cnt = 24;
                break;
            default:
        }
        int line = triggerVideoTotalLineCnt();
        return (1000*1000*1000/4)/cnt/line;
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 视频频率访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前视频频率
     *
     * @return 视频频率
     *         VIDEO_FREQUENCY_60HZ (0): 60Hz
     *         VIDEO_FREQUENCY_50HZ (1): 50Hz
     *         VIDEO_FREQUENCY_30HZ (2): 30Hz
     *         VIDEO_FREQUENCY_25HZ (3): 25Hz
     *         VIDEO_FREQUENCY_24HZ (4): 24Hz
     */
    public int getVideoFrequency() {
        return videoFrequency;
    }

    /**
     * 设置视频频率
     * 更新视频频率并发送FPGA消息
     *
     * @param videoFrequency 视频频率
     *                       VIDEO_FREQUENCY_60HZ: 60Hz
     *                       VIDEO_FREQUENCY_50HZ: 50Hz
     *                       VIDEO_FREQUENCY_30HZ: 30Hz
     *                       VIDEO_FREQUENCY_25HZ: 25Hz
     *                       VIDEO_FREQUENCY_24HZ: 24Hz
     *
     * @note 设置后会发送FPGA_CMD_TRIG命令
     */
    public void setVideoFrequency(int videoFrequency) {
        this.videoFrequency = videoFrequency;
        triggerParamChange();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 触发行号访问方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取触发行号
     * 当videoTrigger为VIDEO_TRIGGER_LINE时有效
     *
     * @return 触发行号（1-based）
     */
    public int getLine() {
        return line;
    }

    /**
     * 设置触发行号
     * 更新触发行号并发送FPGA消息
     *
     * @param line 触发行号（1-based）
     *             取值范围：1 ~ 总行数（根据视频标准确定）
     *
     * @note 设置后会发送FPGA_CMD_TRIG命令
     */
    public void setLine(int line) {
        this.line = line;
        triggerParamChange();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 抽象方法实现 - 继承自Trigger
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取触发源对应的触发电平数量
     * 视频触发不需要触发电平
     *
     * @return 始终返回0，表示不需要触发电平
     */
    @Override
    public int getSrcTriggerLevelCnt() {
        return 0;
    }

    /**
     * 获取触发源数量
     * 视频触发只支持单触发源
     *
     * @return 始终返回1，表示单触发源
     */
    @Override
    public int getTriggerSourceCnt() {
        return 1;
    }

    /**
     * 判断是否支持多触发源
     * 视频触发只支持单触发源
     *
     * @return 始终返回false，表示只支持单触发源
     */
    @Override
    protected boolean isMultitriggerSource() {
        return false;
    }

}
