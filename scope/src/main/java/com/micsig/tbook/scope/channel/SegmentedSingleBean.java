package com.micsig.tbook.scope.channel;  // 定义包名：示波器通道管理模块

import com.micsig.base.RxMsgSelect;  // 导入RxMsgSelect类：选择状态基类

/**
 * 分段存储单帧数据Bean - 分段采集帧信息封装
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.channel（示波器通道管理模块）</li>
 *   <li>架构层级：数据层 - 数据传输对象（DTO）</li>
 *   <li>设计模式：JavaBean模式 + 继承模式</li>
 *   <li>职责类型：分段存储帧信息封装、时间戳格式化</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>封装分段存储中单帧的帧ID和时间戳信息</li>
 *   <li>提供时间戳的格式化显示（纳秒转毫秒）</li>
 *   <li>支持选择状态标记（继承自RxMsgSelect）</li>
 * </ul>
 * 
 * <p><b>分段存储架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   分段存储（Segmented Memory）架构                                        │
 * │                                                                          │
 * │   采集过程：                                                              │
 * │   ┌─────────┐    触发    ┌─────────┐    触发    ┌─────────┐             │
 * │   │ 帧0    │ ─────────► │ 帧1    │ ─────────► │ 帧2    │             │
 * │   │ FrameId│            │ FrameId│            │ FrameId│             │
 * │   │ =0     │            │ =1     │            │ =2     │             │
 * │   │ timeMs │            │ timeMs │            │ timeMs │             │
 * │   └─────────┘            └─────────┘            └─────────┘             │
 * │                                                                          │
 * │   SegmentedSingleBean：                                                   │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │  FrameId        帧序号（0, 1, 2, ...）                           │   │
 * │   │  timeMs         时间戳（纳秒/毫秒）                               │   │
 * │   │  strTime        时间字符串（格式化显示）                          │   │
 * │   │  rxMsgSelect    选择状态（继承自RxMsgSelect）                     │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   使用场景：                                                              │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │  分段采集列表                                                    │   │
 * │   │  List&lt;SegmentedSingleBean&gt;                                    │   │
 * │   │      │                                                           │   │
 * │   │      ├── [0] SegmentedSingleBean(FrameId=0, timeMs=0ms)         │   │
 * │   │      ├── [1] SegmentedSingleBean(FrameId=1, timeMs=125.456ms)   │   │
 * │   │      ├── [2] SegmentedSingleBean(FrameId=2, timeMs=250.912ms)   │   │
 * │   │      └── ...                                                     │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>时间戳格式化：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   时间戳格式化示例                                                        │
 * │                                                                          │
 * │   输入：timeMs = 123456789（纳秒）                                        │
 * │                                                                          │
 * │   计算过程：                                                              │
 * │   1. 毫秒部分：123456789 / (1000 * 1000) = 123                          │
 * │   2. 纳秒余数：123456789 % 1000000 = 456789                              │
 * │   3. 补齐6位：456789 → "456789"                                          │
 * │   4. 组合结果：123.456789ms                                              │
 * │   5. 截断到9位：123.45678ms                                              │
 * │                                                                          │
 * │   输出：123.45678ms                                                       │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>继承关系：</b>
 * <pre>
 *   RxMsgSelect（选择状态基类）
 *       │
 *       └── SegmentedSingleBean（分段存储单帧数据Bean）
 *               │
 *               └── 继承rxMsgSelect选择状态
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：RxMsgSelect（选择状态基类）</li>
 * </ul>
 * 
 * @author Liwb
 * @version 1.0
 * @see RxMsgSelect 选择状态基类
 */
public class SegmentedSingleBean extends RxMsgSelect {  // 继承RxMsgSelect选择状态基类

    /** 帧ID：分段存储中的帧序号 */
    private int FrameId;  // 帧序号
    
    /** 时间戳（纳秒）：帧采集的时间戳，-1表示未设置 */
    private long timeMs = -1;  // 时间戳（纳秒）
    
    /** 时间字符串：格式化后的时间显示字符串 */
    private String strTime = null;  // 时间字符串

    /**
     * 默认构造方法
     * 
     * <p>创建一个空的分段存储单帧数据对象。
     */
    public SegmentedSingleBean(){

    }

    /**
     * 构造方法：使用帧ID和时间字符串
     * 
     * <p>适用于已有格式化时间字符串的场景。
     * 
     * @param frameId 帧序号
     * @param timeMs 时间字符串（格式化后的时间）
     */
    public SegmentedSingleBean(int frameId, String timeMs){
        FrameId = frameId;  // 设置帧序号
        strTime = timeMs;  // 设置时间字符串
    }

    /**
     * 构造方法：使用帧ID和时间戳（纳秒）
     * 
     * <p>适用于原始时间戳的场景，时间戳会被格式化显示。
     * 
     * @param frameId 帧序号
     * @param timeMs 时间戳（纳秒）
     */
    public SegmentedSingleBean(int frameId, long timeMs) {
        FrameId = frameId;  // 设置帧序号
        this.timeMs = timeMs;  // 设置时间戳
    }

    /**
     * 获取帧ID
     * 
     * @return 帧序号
     */
    public int getFrameId() {
        return FrameId;  // 返回帧序号
    }

    /**
     * 获取原始时间戳
     * 
     * @return 时间戳（纳秒），-1表示未设置
     */
    public long getTimestamp(){
        return timeMs;  // 返回时间戳
    }

    /**
     * 获取格式化的时间字符串
     * 
     * <p>优先返回纳秒时间戳格式化后的字符串，其次返回预设的时间字符串。
     * 
     * @return 格式化的时间字符串（如"123.45678ms"），未设置返回"---"
     */
    public String getTimeMs() {
        if(timeMs > 0) {  // 检查时间戳是否有效
            return getMsFromNs(timeMs);  // 将纳秒格式化为毫秒字符串
        }
        if(strTime != null){  // 检查是否有预设时间字符串
            return strTime;  // 返回预设时间字符串
        }
        return "---";  // 未设置返回默认字符串
    }

    /**
     * 转换为字符串表示
     * 
     * @return 包含帧ID和时间戳的字符串
     */
    @Override
    public String toString() {
        return "SegmentedSingleBean{" +  // 类名
                "FrameId=" + FrameId +  // 帧ID
                ", timeMs='" + timeMs + '\'' +  // 时间戳
                '}';  // 结束
    }

    /**
     * 将纳秒时间戳转换为毫秒格式字符串
     * 
     * <p>将纳秒时间戳转换为"xxx.xxxxxxms"格式的字符串。
     * 
     * <p><b>转换算法：</b>
     * <ol>
     *   <li>计算毫秒部分：ns / 1000000</li>
     *   <li>计算纳秒余数部分</li>
     *   <li>补齐纳秒部分到6位</li>
     *   <li>组合为"毫秒.纳秒ms"格式</li>
     *   <li>截断到最多9个字符</li>
     * </ol>
     * 
     * @param ns 纳秒时间戳
     * @return 格式化的时间字符串（如"123.456789ms"）
     */
    public static String getMsFromNs(long ns) {
        String value = String.valueOf(ns / (1000 * 1000));  // 计算毫秒部分
        String string = String.valueOf(ns);  // 获取完整纳秒字符串
        while (string.length() < 6) {  // 补齐到至少6位
            string = "0" + string;  // 前面补0
        }
        value = value + "." + string.substring(string.length() - 6);  // 组合毫秒和纳秒部分
        if (value.length() > 9) {  // 检查是否超过9个字符
            value = value.substring(0,9);  // 截断到9个字符
        }
        return value + "ms";  // 添加单位并返回
    }
}
