package com.micsig.tbook.tbookscope.top.layout.trigger; // 触发器配置模块所在包 //

import com.micsig.tbook.scope.Trigger.Trigger; // 触发器基类，定义触发器类型常量 //
import com.micsig.tbook.scope.Trigger.TriggerLogic; // 逻辑触发器类，定义逻辑状态常量 //
import com.micsig.tbook.scope.Trigger.TriggerVideo; // 视频触发器类，定义视频触发常量 //

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                     触发器参数映射工具类                                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块名称: TopMatchTrigger                                                   ║
 * ║  所属模块: 触发器配置模块                                                      ║
 * ║  创建时间: 2018/4/10                                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                    ║
 * ║  1. 提供UI视图索引与示波器内部索引的双向映射                                    ║
 * ║  2. 映射触发器类型（常规/边沿/脉宽/逻辑/N边沿/欠幅/斜率/超时/视频/串行）      ║
 * ║  3. 映射触发条件类型（小于/大于/等于/不等于/真/假）                            ║
 * ║  4. 映射逻辑触发器通道状态（高/低/忽略）                                      ║
 * ║  5. 映射视频触发器的触发模式和频率参数                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  架构设计:                                                                    ║
 * ║                                                                              ║
 * ║    ┌─────────────────────────────────────────────────────────────────┐       ║
 * ║    │                    TopMatchTrigger (工具类)                     │       ║
 * ║    │                     全部为静态方法                               │       ║
 * ║    └─────────────────────────────────────────────────────────────────┘       ║
 * ║                                   │                                          ║
 * ║              ┌────────────────────┼────────────────────┐                     ║
 * ║              │                    │                    │                     ║
 * ║              ▼                    ▼                    ▼                     ║
 * ║    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                 ║
 * ║    │  UI视图层     │    │  映射转换     │    │  硬件层      │                 ║
 * ║    ├──────────────┤    ├──────────────┤    ├──────────────┤                 ║
 * ║    │TopLayout     │    │ViewToScope   │    │Trigger       │                 ║
 * ║    │Trigger常量   │───▶│ScopeToView   │───▶│常量定义      │                 ║
 * ║    │(0~12)        │    │条件/逻辑/视频 │    │(1~9等)       │                 ║
 * ║    └──────────────┘    └──────────────┘    └──────────────┘                 ║
 * ║                                                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向:                                                                    ║
 * ║                                                                              ║
 * ║  UI操作 → ViewToScope() → 硬件参数                                           ║
 * ║  硬件事件 → ScopeToView() → UI更新                                           ║
 * ║                                                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系:                                                                    ║
 * ║  • Trigger          - 触发器基类，定义触发器类型常量                           ║
 * ║  • TriggerLogic     - 逻辑触发器类，定义逻辑状态常量                          ║
 * ║  • TriggerVideo     - 视频触发器类，定义视频触发常量                          ║
 * ║  • TopLayoutTrigger - 触发器布局类，定义UI视图索引常量                        ║
 * ║                                                                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  使用场景:                                                                    ║
 * ║  1. 用户在UI选择触发器类型时，转换为硬件可识别的类型码                         ║
 * ║  2. 硬件返回触发器状态时，转换为UI可显示的索引                                ║
 * ║  3. 条件、逻辑通道、视频参数等也需要类似的映射转换                             ║
 * ║                                                                              ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class TopMatchTrigger { // 触发器参数映射工具类 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: triggerTypeViewToScope
     * 方法描述: 将UI视图索引转换为示波器内部触发器类型码
     *
     * @param indexView UI视图中的触发器类型索引（0~12）
     * @return int 示波器内部触发器类型码，常规返回-1
     *
     * 功能说明:
     * UI索引与示波器类型码的映射关系：
     * 0(常规)→-1, 1(边沿)→EDGE, 2(脉宽)→PULSE, 3(逻辑)→LOGIC,
     * 4(N边沿)→NEDGE, 5(欠幅)→LOW_PULSE, 6(斜率)→SLOPE,
     * 7(超时)→TIMEOUT, 8(视频)→VIDEO, 9~12(S1~S4)→SERIAL1~4
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public static int triggerTypeViewToScope(int indexView) { // UI视图索引转示波器类型码 //
        switch (indexView) { // 根据UI索引分发 //
            case TopLayoutTrigger.DETAIL_COMMON:        // 常规触发器 //
                return -1; // 常规类型无对应硬件类型，返回-1 //
            case TopLayoutTrigger.DETAIL_EDGE:          // 边沿触发器 //
                return Trigger.TRIG_TYPE_EDGE; // 返回边沿触发器类型码 //
            case TopLayoutTrigger.DETAIL_PULSEWIDTH:    // 脉宽触发器 //
                return Trigger.TRIG_TYPE_PULSE; // 返回脉宽触发器类型码 //
            case TopLayoutTrigger.DETAIL_LOGIC:         // 逻辑触发器 //
                return Trigger.TRIG_TYPE_LOGIC; // 返回逻辑触发器类型码 //
            case TopLayoutTrigger.DETAIL_NEDGE:         // N边沿触发器 //
                return Trigger.TRIG_TYPE_NEDGE; // 返回N边沿触发器类型码 //
            case TopLayoutTrigger.DETAIL_RUNT:          // 欠幅触发器 //
                return Trigger.TRIG_TYPE_LOW_PULSE; // 返回欠幅（低脉冲）触发器类型码 //
            case TopLayoutTrigger.DETAIL_SLOPE:         // 斜率触发器 //
                return Trigger.TRIG_TYPE_SLOPE; // 返回斜率触发器类型码 //
            case TopLayoutTrigger.DETAIL_TIMEOUT:       // 超时触发器 //
                return Trigger.TRIG_TYPE_TIMEOUT; // 返回超时触发器类型码 //
            case TopLayoutTrigger.DETAIL_VIDEO:         // 视频触发器 //
                return Trigger.TRIG_TYPE_VIDEO; // 返回视频触发器类型码 //
            case TopLayoutTrigger.DETAIL_S1:            // 串行1触发器 //
                return Trigger.TRIG_TYPE_SERIAL1; // 返回串行1触发器类型码 //
            case TopLayoutTrigger.DETAIL_S2:            // 串行2触发器 //
                return Trigger.TRIG_TYPE_SERIAL2; // 返回串行2触发器类型码 //
            case TopLayoutTrigger.DETAIL_S3:            // 串行3触发器 //
                return Trigger.TRIG_TYPE_SERIAL3; // 返回串行3触发器类型码 //
            case TopLayoutTrigger.DETAIL_S4:            // 串行4触发器 //
                return Trigger.TRIG_TYPE_SERIAL4; // 返回串行4触发器类型码 //
            default: // 默认情况 //
                return -1; // 未知类型返回-1 //
        } // switch结束 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: triggerTypeScopeToView
     * 方法描述: 将示波器内部触发器类型码转换为UI视图索引
     *
     * @param indexScope 示波器内部触发器类型码
     * @return int UI视图中的触发器类型索引，默认返回常规(0)
     *
     * 功能说明:
     * 示波器类型码与UI索引的映射关系，与triggerTypeViewToScope互逆
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public static int triggerTypeScopeToView(int indexScope) { // 示波器类型码转UI视图索引 //
        switch (indexScope) { // 根据示波器类型码分发 //
            case Trigger.TRIG_TYPE_EDGE: // 边沿触发器 //
                return TopLayoutTrigger.DETAIL_EDGE;          // 返回边沿UI索引 //
            case Trigger.TRIG_TYPE_PULSE: // 脉宽触发器 //
                return TopLayoutTrigger.DETAIL_PULSEWIDTH;    // 返回脉宽UI索引 //
            case Trigger.TRIG_TYPE_LOGIC: // 逻辑触发器 //
                return TopLayoutTrigger.DETAIL_LOGIC;         // 返回逻辑UI索引 //
            case Trigger.TRIG_TYPE_NEDGE: // N边沿触发器 //
                return TopLayoutTrigger.DETAIL_NEDGE;         // 返回N边沿UI索引 //
            case Trigger.TRIG_TYPE_LOW_PULSE: // 欠幅触发器 //
                return TopLayoutTrigger.DETAIL_RUNT;          // 返回欠幅UI索引 //
            case Trigger.TRIG_TYPE_SLOPE: // 斜率触发器 //
                return TopLayoutTrigger.DETAIL_SLOPE;         // 返回斜率UI索引 //
            case Trigger.TRIG_TYPE_TIMEOUT: // 超时触发器 //
                return TopLayoutTrigger.DETAIL_TIMEOUT;       // 返回超时UI索引 //
            case Trigger.TRIG_TYPE_VIDEO: // 视频触发器 //
                return TopLayoutTrigger.DETAIL_VIDEO;         // 返回视频UI索引 //
            case Trigger.TRIG_TYPE_SERIAL1: // 串行1触发器 //
                return TopLayoutTrigger.DETAIL_S1;            // 返回串行1 UI索引 //
            case Trigger.TRIG_TYPE_SERIAL2: // 串行2触发器 //
                return TopLayoutTrigger.DETAIL_S2;            // 返回串行2 UI索引 //
            case Trigger.TRIG_TYPE_SERIAL3: // 串行3触发器 //
                return TopLayoutTrigger.DETAIL_S3;            // 返回串行3 UI索引 //
            case Trigger.TRIG_TYPE_SERIAL4: // 串行4触发器 //
                return TopLayoutTrigger.DETAIL_S4;            // 返回串行4 UI索引 //
            default: // 默认情况 //
                return TopLayoutTrigger.DETAIL_COMMON; // 返回常规UI索引 //
        } // switch结束 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: triggerConditionToScope
     * 方法描述: 将UI条件索引转换为示波器内部条件码
     *
     * @param condition UI条件索引（0~5）
     * @return int 示波器内部条件码
     *
     * 功能说明:
     * UI条件索引与示波器条件码的映射：
     * 0→小于, 1→大于, 2→等于, 3→不等于, 4→真, 5→假
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public static int triggerConditionToScope(int condition) { // UI条件索引转示波器条件码 //
        switch (condition) { // 根据UI条件索引分发 //
            case 0: // 小于 //
                return Trigger.TRIGGER_RELATION_LESS_THAN; // 返回小于条件码 //
            case 1: // 大于 //
                return Trigger.TRIGGER_RELATION_MORE_THAN; // 返回大于条件码 //
            case 2: // 等于 //
                return Trigger.TRIGGER_RELATION_EQUAL; // 返回等于条件码 //
            case 3: // 不等于 //
                return Trigger.TRIGGER_RELATION_NOT_EQUAL; // 返回不等于条件码 //
            case 4: // 真 //
                return Trigger.TRIGGER_RELATION_TRUE; // 返回真条件码 //
            case 5: // 假 //
                return Trigger.TRIGGER_RELATION_FALSE; // 返回假条件码 //
            default: // 默认情况 //
                return Trigger.TRIGGER_RELATION_LESS_THAN; // 默认返回小于条件码 //
        } // switch结束 //
    } // 方法结束 //


    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: triggerConditionFromScope
     * 方法描述: 将示波器内部条件码转换为UI条件索引
     *
     * @param scope 示波器内部条件码
     * @return int UI条件索引，默认返回0
     *
     * 功能说明:
     * 与triggerConditionToScope互逆的映射
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public static int triggerConditionFromScope(int scope) { // 示波器条件码转UI条件索引 //
        switch (scope) { // 根据示波器条件码分发 //
            case Trigger.TRIGGER_RELATION_LESS_THAN: // 小于 //
                return 0; // 返回UI索引0 //
            case Trigger.TRIGGER_RELATION_MORE_THAN: // 大于 //
                return 1; // 返回UI索引1 //
            case Trigger.TRIGGER_RELATION_EQUAL: // 等于 //
                return 2; // 返回UI索引2 //
            case Trigger.TRIGGER_RELATION_NOT_EQUAL: // 不等于 //
                return 3; // 返回UI索引3 //
            case Trigger.TRIGGER_RELATION_TRUE: // 真 //
                return 4; // 返回UI索引4 //
            case Trigger.TRIGGER_RELATION_FALSE: // 假 //
                return 5; // 返回UI索引5 //
            default: // 默认情况 //
                return 0; // 默认返回0 //
        } // switch结束 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: triggerLogicChToScope
     * 方法描述: 将UI逻辑通道索引转换为示波器内部逻辑状态码
     *
     * @param chIndex UI逻辑通道索引（0=高, 1=低, 2=忽略）
     * @return int 示波器内部逻辑状态码
     *
     * 功能说明:
     * UI索引与示波器逻辑状态码的映射：
     * 0→LOGIC_HIGH, 1→LOGIC_LOW, 2→LOGIC_NONE
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public static int triggerLogicChToScope(int chIndex) { // UI逻辑通道索引转示波器逻辑状态码 //
        switch (chIndex) { // 根据UI逻辑通道索引分发 //
            case 0: // 高电平 //
                return TriggerLogic.LOGIC_HIGH; // 返回高电平状态码 //
            case 1: // 低电平 //
                return TriggerLogic.LOGIC_LOW; // 返回低电平状态码 //
            case 2: // 忽略 //
                return TriggerLogic.LOGIC_NONE; // 返回忽略状态码 //
            default: // 默认情况 //
                return TriggerLogic.LOGIC_HIGH; // 默认返回高电平状态码 //
        } // switch结束 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: triggerLogicChFromScope
     * 方法描述: 将示波器内部逻辑状态码转换为UI逻辑通道索引
     *
     * @param scopeIndex 示波器内部逻辑状态码
     * @return int UI逻辑通道索引，默认返回0
     *
     * 功能说明:
     * 与triggerLogicChToScope互逆的映射
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public static int triggerLogicChFromScope(int scopeIndex) { // 示波器逻辑状态码转UI逻辑通道索引 //
        switch (scopeIndex) { // 根据示波器逻辑状态码分发 //
            case TriggerLogic.LOGIC_HIGH: // 高电平 //
                return 0; // 返回UI索引0 //
            case TriggerLogic.LOGIC_LOW: // 低电平 //
                return 1; // 返回UI索引1 //
            case TriggerLogic.LOGIC_NONE: // 忽略 //
                return 2; // 返回UI索引2 //
            default: // 默认情况 //
                return 0; // 默认返回0 //
        } // switch结束 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: triggerLogicRelationToScope
     * 方法描述: 将UI逻辑关系索引转换为示波器内部逻辑关系码
     *
     * @param relation UI逻辑关系索引（0=AND, 1=OR, 2=NAND, 3=NOR）
     * @return int 示波器内部逻辑关系码
     *
     * 功能说明:
     * UI索引与示波器逻辑关系码的映射：
     * 0→AND, 1→OR, 2→NAND, 3→NOR
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public static int triggerLogicRelationToScope(int relation) { // UI逻辑关系索引转示波器逻辑关系码 //
        switch (relation) { // 根据UI逻辑关系索引分发 //
            default: // 默认情况 //
            case 0: // AND逻辑 //
                return TriggerLogic.LOGIC_AND; // 返回AND逻辑关系码 //
            case 1: // OR逻辑 //
                return TriggerLogic.LOGIC_OR; // 返回OR逻辑关系码 //
            case 2: // NAND逻辑 //
                return TriggerLogic.LOGIC_NAND; // 返回NAND逻辑关系码 //
            case 3: // NOR逻辑 //
                return TriggerLogic.LOGIC_NOR; // 返回NOR逻辑关系码 //
        } // switch结束 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: triggerVideoTriggerToScope
     * 方法描述: 将UI视频触发模式索引转换为示波器内部视频触发码
     *
     * @param standard 视频标准索引（0~5）
     * @param trigger  UI视频触发模式索引
     * @return int 示波器内部视频触发码
     *
     * 功能说明:
     * 根据视频标准决定触发模式的映射方式：
     * - 标准0/1/2/4：直接使用trigger索引
     * - 标准3/5：映射为ALL_FIELDS/ALL_LINES/LINE
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public static int triggerVideoTriggerToScope(int standard, int trigger) { // UI视频触发模式索引转示波器视频触发码 //
        switch (standard) { // 根据视频标准分发 //
            case 0: // PAL标准 //
            case 1: // SECAM标准 //
            case 2: // NTSC标准 //
            case 4: // 720P标准 //
                return trigger; // 直接使用UI触发模式索引 //
            case 3: // 1080I标准 //
            case 5: // 1080P标准 //
                switch (trigger) { // 根据UI触发模式索引分发 //
                    case 0: // 所有场 //
                        return TriggerVideo.VIDEO_TRIGGER_ALL_FIELDS; // 返回所有场触发码 //
                    case 1: // 所有行 //
                        return TriggerVideo.VIDEO_TRIGGER_ALL_LINES; // 返回所有行触发码 //
                    case 2: // 指定行 //
                        return TriggerVideo.VIDEO_TRIGGER_LINE; // 返回指定行触发码 //
                } // 内层switch结束 //
                break; // 跳出 //
        } // 外层switch结束 //
        return TriggerVideo.VIDEO_TRIGGER_ALL_FIELDS; // 默认返回所有场触发码 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: triggerVideoTriggerFromScope
     * 方法描述: 将示波器内部视频触发码转换为UI视频触发模式索引
     *
     * @param standard    视频标准索引（0~5）
     * @param scopeTrigger 示波器内部视频触发码
     * @return int UI视频触发模式索引，默认返回0
     *
     * 功能说明:
     * 与triggerVideoTriggerToScope互逆的映射
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public static int triggerVideoTriggerFromScope(int standard, int scopeTrigger) { // 示波器视频触发码转UI视频触发模式索引 //
        switch (standard) { // 根据视频标准分发 //
            case 0: // PAL标准 //
            case 1: // SECAM标准 //
            case 2: // NTSC标准 //
            case 4: // 720P标准 //
                return scopeTrigger; // 直接使用示波器触发码作为UI索引 //
            case 3: // 1080I标准 //
            case 5: // 1080P标准 //
                switch (scopeTrigger) { // 根据示波器触发码分发 //
                    case TriggerVideo.VIDEO_TRIGGER_ALL_FIELDS: // 所有场 //
                        return 0; // 返回UI索引0 //
                    case TriggerVideo.VIDEO_TRIGGER_ALL_LINES: // 所有行 //
                        return 1; // 返回UI索引1 //
                    case TriggerVideo.VIDEO_TRIGGER_LINE: // 指定行 //
                        return 2; // 返回UI索引2 //
                } // 内层switch结束 //
                break; // 跳出 //
        } // 外层switch结束 //
        return 0; // 默认返回0 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: triggerVideoFrequencyToScope
     * 方法描述: 将UI视频频率索引转换为示波器内部视频频率码
     *
     * @param standard   视频标准索引（0~5）
     * @param frequency  UI视频频率索引
     * @return int 示波器内部视频频率码
     *
     * 功能说明:
     * 根据视频标准决定频率的映射方式：
     * - 标准0/1：固定50Hz
     * - 标准2：固定60Hz
     * - 标准3/4：0→60Hz, 1→50Hz
     * - 标准5：0→60Hz, 1→50Hz, 2→30Hz, 3→25Hz, 4→24Hz
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public static int triggerVideoFrequencyToScope(int standard, int frequency) { // UI视频频率索引转示波器视频频率码 //
        switch (standard) { // 根据视频标准分发 //
            case 0: // PAL标准 //
            case 1: // SECAM标准 //
                return TriggerVideo.VIDEO_FREQUENCY_50HZ; // PAL/SECAM固定50Hz //
            case 2: // NTSC标准 //
                return TriggerVideo.VIDEO_FREQUENCY_60HZ; // NTSC固定60Hz //
            case 3: // 1080I标准 //
            case 4: // 720P标准 //
                switch (frequency) { // 根据UI频率索引分发 //
                    case 0: // 60Hz //
                        return TriggerVideo.VIDEO_FREQUENCY_60HZ; // 返回60Hz频率码 //
                    case 1: // 50Hz //
                        return TriggerVideo.VIDEO_FREQUENCY_50HZ; // 返回50Hz频率码 //
                } // 内层switch结束 //
            case 5: // 1080P标准 //
                switch (frequency) { // 根据UI频率索引分发 //
                    case 0: // 60Hz //
                        return TriggerVideo.VIDEO_FREQUENCY_60HZ; // 返回60Hz频率码 //
                    case 1: // 50Hz //
                        return TriggerVideo.VIDEO_FREQUENCY_50HZ; // 返回50Hz频率码 //
                    case 2: // 30Hz //
                        return TriggerVideo.VIDEO_FREQUENCY_30HZ; // 返回30Hz频率码 //
                    case 3: // 25Hz //
                        return TriggerVideo.VIDEO_FREQUENCY_25HZ; // 返回25Hz频率码 //
                    case 4: // 24Hz //
                        return TriggerVideo.VIDEO_FREQUENCY_24HZ; // 返回24Hz频率码 //
                } // 内层switch结束 //
        } // 外层switch结束 //
        return TriggerVideo.VIDEO_FREQUENCY_60HZ; // 默认返回60Hz频率码 //
    } // 方法结束 //

    /**
     * ═══════════════════════════════════════════════════════════════════════════
     * 方法名称: triggerVideoFrequencyFromScope
     * 方法描述: 将示波器内部视频频率码转换为UI视频频率索引
     *
     * @param standard       视频标准索引（0~5）
     * @param scopeFrequency 示波器内部视频频率码
     * @return int UI视频频率索引，默认返回0
     *
     * 功能说明:
     * 与triggerVideoFrequencyToScope互逆的映射
     * ═══════════════════════════════════════════════════════════════════════════
     */
    public static int triggerVideoFrequencyFromScope(int standard, int scopeFrequency) { // 示波器视频频率码转UI视频频率索引 //
        switch (standard) { // 根据视频标准分发 //
            case 0: // PAL标准 //
            case 1: // SECAM标准 //
            case 2: // NTSC标准 //
                return 0; // 固定频率标准，返回索引0 //
            case 3: // 1080I标准 //
            case 4: // 720P标准 //
                switch (scopeFrequency) { // 根据示波器频率码分发 //
                    case TriggerVideo.VIDEO_FREQUENCY_60HZ: // 60Hz //
                        return 0; // 返回UI索引0 //
                    case TriggerVideo.VIDEO_FREQUENCY_50HZ: // 50Hz //
                        return 1; // 返回UI索引1 //
                } // 内层switch结束 //
            case 5: // 1080P标准 //
                switch (scopeFrequency) { // 根据示波器频率码分发 //
                    case TriggerVideo.VIDEO_FREQUENCY_60HZ: // 60Hz //
                        return 0; // 返回UI索引0 //
                    case TriggerVideo.VIDEO_FREQUENCY_50HZ: // 50Hz //
                        return 1; // 返回UI索引1 //
                    case TriggerVideo.VIDEO_FREQUENCY_30HZ: // 30Hz //
                        return 2; // 返回UI索引2 //
                    case TriggerVideo.VIDEO_FREQUENCY_25HZ: // 25Hz //
                        return 3; // 返回UI索引3 //
                    case TriggerVideo.VIDEO_FREQUENCY_24HZ: // 24Hz //
                        return 4; // 返回UI索引4 //
                } // 内层switch结束 //
        } // 外层switch结束 //
        return 0; // 默认返回0 //
    } // 方法结束 //
} // 类结束 //
