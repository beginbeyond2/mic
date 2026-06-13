package com.micsig.tbook.scope.math;

import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.vertical.VerticalAxis;
import com.micsig.tbook.scope.vertical.VerticalAxisMathDual;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                MathDualWave - 双通道数学运算类                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Math模块的双通道数学运算类，位于math包下，                                  ║
 * ║   提供示波器的双通道数学运算功能（加、减、乘、除）。                           ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理双通道数学运算符（加、减、乘、除）                                   ║
 * ║   2. 管理运算源通道（源通道1、源通道2）                                      ║
 * ║   3. 管理数学通道的垂直轴参数                                                ║
 * ║   4. 生成运算结果的探头类型（V、A、W、V/V等）                                ║
 * ║   5. 发送数学通道事件                                                        ║
 * ║                                                                              ║
 * ║ 【运算类型】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        运算类型说明                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【加法运算】MATH_ADD (0)                                            │ ║
 * ║   │   - 运算：CH1 + CH2                                                  │ ║
 * ║   │   - 单位：V + V = V, A + A = A, 其他 = ?                             │ ║
 * ║   │                                                                      │ ║
 * ║   │   【减法运算】MATH_SUB (1)                                            │ ║
 * ║   │   - 运算：CH1 - CH2                                                  │ ║
 * ║   │   - 单位：V - V = V, A - A = A, 其他 = ?                             │ ║
 * ║   │                                                                      │ ║
 * ║   │   【乘法运算】MATH_MUL (2)                                            │ ║
 * ║   │   - 运算：CH1 × CH2                                                  │ ║
 * ║   │   - 单位：V × V = VV, A × A = AA, V × A = W                          │ ║
 * ║   │                                                                      │ ║
 * ║   │   【除法运算】MATH_DIV (3)                                            │ ║
 * ║   │   - 运算：CH1 ÷ CH2                                                  │ ║
 * ║   │   - 单位：V ÷ V = V/V, A ÷ A = A/A, V ÷ A = V/A, A ÷ V = A/V        │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【探头类型生成规则】                                                         ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        探头类型生成规则                               │ ║
 * ║   │                                                                      │ ║
 * ║   │   【加法/减法】                                                       │ ║
 * ║   │   - V + V = V (电压)                                                 │ ║
 * ║   │   - A + A = A (电流)                                                 │ ║
 * ║   │   - 其他组合 = ? (未知)                                              │ ║
 * ║   │                                                                      │ ║
 * ║   │   【乘法】                                                            │ ║
 * ║   │   - V × V = VV (电压平方)                                            │ ║
 * ║   │   - A × A = AA (电流平方)                                            │ ║
 * ║   │   - V × A = W (功率)                                                 │ ║
 * ║   │   - 其他组合 = ? (未知)                                              │ ║
 * ║   │                                                                      │ ║
 * ║   │   【除法】                                                            │ ║
 * ║   │   - V ÷ V = V/V (电压比)                                             │ ║
 * ║   │   - A ÷ A = A/A (电流比)                                             │ ║
 * ║   │   - V ÷ A = V/A (电阻)                                               │ ║
 * ║   │   - A ÷ V = A/V (电导)                                               │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【数据流】                                                                   ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 设置源通道  │───▶│ 设置运算符  │───▶│ 计算结果    │                   ║
 * ║   │ setSource   │    │ setOperator │    │ MathNative  │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 电压/电流加减运算：测量差分信号                                        ║
 * ║   2. 功率计算：电压 × 电流                                                 ║
 * ║   3. 电阻计算：电压 ÷ 电流                                                 ║
 * ║   4. 比值测量：电压比、电流比                                              ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - MathWave: 数学运算基类                                                 ║
 * ║   - MathChannel: 数学通道类                                                ║
 * ║   - MathNative: 数学运算Native方法                                         ║
 * ║   - VerticalAxisMathDual: 双通道数学垂直轴                                 ║
 * ║   - ChannelFactory: 通道工厂                                               ║
 * ║   - EventFactory: 事件工厂                                                 ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 双通道数学运算类
 * 继承自MathWave，提供双通道的加、减、乘、除运算功能
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>运算符管理：设置和获取运算符（加、减、乘、除）</li>
 *   <li>源通道管理：设置和获取运算的源通道</li>
 *   <li>垂直轴管理：管理数学通道的垂直档位</li>
 *   <li>探头类型生成：根据运算类型和源通道类型生成结果单位</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 创建双通道数学运算对象
 * MathDualWave mathDual = new MathDualWave(mathChannel);
 *
 * // 设置源通道
 * mathDual.setSource1(ChannelFactory.CH1);
 * mathDual.setSource2(ChannelFactory.CH2);
 *
 * // 设置运算符为乘法
 * mathDual.setOperator(MathNative.MATH_MUL);
 *
 * // 设置垂直档位
 * mathDual.setVScaleId(scaleId);
 * </pre>
 *
 * @see MathWave
 * @see MathChannel
 * @see MathNative
 * @see VerticalAxisMathDual
 */
public class MathDualWave extends MathWave{

    // ═══════════════════════════════════════════════════════════════════════════════
    // 日志标签
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 日志标签 */
    private static final String TAG = "MathDualWave";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 关联的数学通道对象 */
    protected MathChannel mathChannel;

    /** 运算符类型（加、减、乘、除） */
    private int Operator = MathNative.MATH_ADD;

    /** 源通道1索引 */
    private int srcChIdx1 = ChannelFactory.CH1;

    /** 源通道2索引 */
    private int srcChIdx2 = ChannelFactory.CH1;

    /** 双通道数学垂直轴管理对象 */
    private final VerticalAxisMathDual verticalAxisMathDual;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造MathDualWave实例（默认类型）
     *
     * @param channel 关联的数学通道对象
     */
    public MathDualWave(MathChannel channel){
        this(MATH_DUALWAVE,channel);                                                 // 调用完整构造函数
    }

    /**
     * 构造MathDualWave实例（指定类型）
     *
     * @param mathType 数学运算类型
     * @param channel 关联的数学通道对象
     */
    public MathDualWave(int mathType,MathChannel channel){
        super(mathType);                                                             // 调用父类构造函数
        mathChannel = channel;                                                       // 保存数学通道引用
        verticalAxisMathDual = new VerticalAxisMathDual();                          // 创建垂直轴管理对象
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 运算符管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取运算符类型
     *
     * @return 运算符类型
     * @see MathNative#MATH_ADD
     * @see MathNative#MATH_SUB
     * @see MathNative#MATH_MUL
     * @see MathNative#MATH_DIV
     */
    public int getOperator() {
        return Operator;                                                             // 返回运算符
    }

    /**
     * 设置运算符类型
     * 设置后会强制刷新数学通道
     *
     * @param operator 运算符类型
     */
    public void setOperator(int operator) {

        Operator = operator;                                                         // 设置运算符
        mathChannel.forceRefresh();                                                  // 强制刷新数学通道
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 源通道管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置源通道1
     * 设置后会触发通道采样变化和强制刷新
     *
     * @param chIdx 源通道索引
     */
    public void setSource1(int chIdx){
        srcChIdx1 = chIdx;                                                           // 设置源通道1
        mathChannel.chSampleChange();                                                // 通知通道采样变化
        mathChannel.forceRefresh();                                                  // 强制刷新数学通道
        sendEvent(EventFactory.EVENT_MATH_SOURCE,true);                              // 发送数学源事件
    }

    /**
     * 获取源通道1索引
     *
     * @return 源通道1索引
     */
    public int getSource1(){
        return srcChIdx1;                                                            // 返回源通道1
    }

    /**
     * 设置源通道2
     * 设置后会触发通道采样变化和强制刷新
     *
     * @param chIdx 源通道索引
     */
    public void setSource2(int chIdx) {
        srcChIdx2 = chIdx;                                                           // 设置源通道2
        mathChannel.chSampleChange();                                                // 通知通道采样变化
        mathChannel.forceRefresh();                                                  // 强制刷新数学通道
        sendEvent(EventFactory.EVENT_MATH_SOURCE,true);                              // 发送数学源事件
    }

    /**
     * 获取源通道2索引
     *
     * @return 源通道2索引
     */
    public int getSource2(){
        return srcChIdx2;                                                            // 返回源通道2
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 事件发送方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 发送事件（同步）
     *
     * @param eventId 事件ID
     */
    public void sendEvent(int eventId){
        EventFactory.sendEvent(new EventBase(eventId, mathChannel.getChId()), false); // 同步发送事件
    }

    /**
     * 发送事件
     *
     * @param eventId 事件ID
     * @param async true: 异步发送
     *              false: 同步发送
     */
    public void sendEvent(int eventId,boolean async){
        EventFactory.sendEvent(new EventBase(eventId, mathChannel.getChId()), async); // 发送事件
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 通道采样检查方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 检查指定通道是否参与数学运算
     *
     * @param chIdx 通道索引
     * @return true: 该通道参与运算
     *         false: 该通道不参与运算
     */
    @Override
    public boolean isChInSample(int chIdx) {
        return srcChIdx1 == chIdx || srcChIdx2 == chIdx;                             // 检查是否为源通道1或源通道2
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 垂直轴管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取垂直档位值
     *
     * @return 垂直档位值（伏/格）
     */
    public double getVScaleVal() {
        return verticalAxisMathDual.getScaleVal();                                   // 返回垂直档位值
    }

    /**
     * 根据档位值获取档位ID
     *
     * @param scaleVal 档位值
     * @return 档位ID
     */
    public int getVScaleVal(double scaleVal) {
        return verticalAxisMathDual.getScaleVal(scaleVal);                           // 返回档位ID
    }

    /**
     * 获取当前档位ID对应的档位值
     *
     * @return 档位值
     */
    public double getVScaleIdVal() {
        return verticalAxisMathDual.getScaleIdVal();                                 // 返回档位值
    }

    /**
     * 根据档位ID获取档位值
     *
     * @param scaleId 档位ID
     * @return 档位值
     */
    public double getVScaleIdVal(int scaleId) {
        return verticalAxisMathDual.getScaleIdVal(scaleId);                          // 返回档位值
    }

    /**
     * 根据档位值获取档位ID
     *
     * @param scaleVal 标准档位值
     * @return 档位ID
     */
    public int getVScaleId(double scaleVal) {
        return verticalAxisMathDual.getScaleId(scaleVal);                            // 返回档位ID
    }

    /**
     * 获取当前档位ID
     *
     * @return 档位ID
     */
    public int getVScaleId() {
        return verticalAxisMathDual.getScaleId();                                    // 返回档位ID
    }

    /**
     * 获取微调比例
     *
     * @return 微调比例
     */
    public double getFineScale() {
        return verticalAxisMathDual.getFineScale();                                  // 返回微调比例
    }

    /**
     * 获取最大档位ID
     *
     * @return 最大档位ID
     */
    public int getVScaleIdMax() {
        return VerticalAxisMathDual.DANG_DUAL_MAX;                                   // 返回最大档位ID
    }

    /**
     * 获取最小档位ID
     *
     * @return 最小档位ID
     */
    public int getVScaleIdMin() {
        return VerticalAxisMathDual.DANG_DUAL_MIN;                                   // 返回最小档位ID
    }

    /**
     * 设置档位ID
     * 设置后会强制刷新数学通道
     *
     * @param scaleId 档位ID
     */
    public void setVScaleId(int scaleId) {
        if (VerticalAxisMathDual.isValidScaleId(scaleId)) {                          // 档位ID有效
            verticalAxisMathDual.setScaleId(scaleId);                                // 设置档位ID
            mathChannel.forceRefresh();                                              // 强制刷新数学通道
        }
    }

    /**
     * 获取默认档位ID
     * 根据运算类型和源通道档位计算默认档位
     *
     * @return 默认档位ID
     */
    public int getDefaultVScaleId(){
        double _vScale;                                                               // 计算的档位值
        Channel src1 = ChannelFactory.getDynamicChannel(srcChIdx1);                 // 获取源通道1
        Channel src2 = ChannelFactory.getDynamicChannel(srcChIdx2);                 // 获取源通道2
        if (src1 == null || src2 == null) return VerticalAxisMathDual.DANG_DUAL_MAX; // 通道无效，返回最大档位

        // 根据运算类型计算档位值
        switch (Operator) {                                                          // 根据运算符类型
            case MathNative.MATH_DIV:                                                // 除法
                _vScale = src1.getVScaleVal()/src2.getVScaleVal();                  // 档位值 = 源1档位 / 源2档位
                break;
            case MathNative.MATH_MUL:                                                // 乘法
                _vScale = src1.getVScaleVal()*src2.getVScaleVal();                  // 档位值 = 源1档位 × 源2档位
                break;
            case MathNative.MATH_ADD:                                                // 加法
            case MathNative.MATH_SUB:                                                // 减法
            default:                                                                 // 默认
                _vScale = src1.getVScaleVal()+src2.getVScaleVal();                  // 档位值 = 源1档位 + 源2档位
                break;
        }
        return getVScaleVal(_vScale);                                                // 返回档位ID
    }

    /**
     * 根据档位值设置档位ID
     *
     * @param scaleIdVal 档位值
     */
    public void setVScaleVal(double scaleIdVal) {
        setVScaleId(verticalAxisMathDual.getScaleId(scaleIdVal));                    // 设置档位ID
    }

    /**
     * 设置微调比例
     *
     * @param fineScale 微调比例
     */
    public void setFineScale(double fineScale) {
        verticalAxisMathDual.setFineScale(fineScale);                                // 设置微调比例
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 探头类型生成方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 生成探头类型
     * 根据运算类型和源通道探头类型生成结果单位
     *
     * <p><b>生成规则：</b></p>
     * <ul>
     *   <li>加法/减法：V+V=V, A+A=A, 其他=?</li>
     *   <li>乘法：V×V=VV, A×A=AA, V×A=W</li>
     *   <li>除法：V÷V=V/V, A÷A=A/A, V÷A=V/A, A÷V=A/V</li>
     * </ul>
     *
     * @return 探头类型
     * @see VerticalAxis#PROBE_TYPE_VV
     * @see VerticalAxis#PROBE_TYPE_AA
     * @see VerticalAxis#PROBE_TYPE_W
     */
    public int generateProbeType() {
        int probeType = 255;                                                         // 默认探头类型
        Channel channel1 = ChannelFactory.getDynamicChannel(srcChIdx1);             // 获取源通道1
        Channel channel2 = ChannelFactory.getDynamicChannel(srcChIdx2);             // 获取源通道2
        if (channel1 == null || channel2 == null) return probeType;                 // 通道无效，返回默认值

        int isCur1 = channel1.getProbeType();                                        // 获取源通道1探头类型
        int isCur2 = channel2.getProbeType();                                        // 获取源通道2探头类型

        // 根据运算类型生成探头类型
        switch (Operator) {                                                          // 根据运算符类型
            case 0:// operate +                                                      // 加法
            case 1:// operate -                                                      // 减法
                if (isCur1 == isCur2) probeType = isCur1;                            // 相同类型，结果类型相同
                else  probeType = VerticalAxis.PROBE_TYPE_WENHAO;//"?"              // 不同类型，结果为问号
                break;
            case 2:// operate mul                                                    // 乘法
                if (isCur1 == 0 && isCur2 == 0) probeType = VerticalAxis.PROBE_TYPE_VV;//"VV" // V × V = VV
                else if (isCur1 == 1 && isCur2 == 1) probeType = VerticalAxis.PROBE_TYPE_AA;//"AA" // A × A = AA
                else if((isCur1== 0 && isCur2 == 1) || (isCur1== 1 && isCur2 == 0)) probeType = VerticalAxis.PROBE_TYPE_W;//"W" // V × A = W
                else probeType = VerticalAxis.PROBE_TYPE_WENHAO;//"?"              // 其他组合
                break;
            case 3:// operate div                                                    // 除法
            {
                if (isCur1 == 0 && isCur2 == 0) probeType = VerticalAxis.PROBE_TYPE_VTOV;//"V/V" // V ÷ V = V/V
                else if (isCur1 == 1 && isCur2 == 1) probeType = VerticalAxis.PROBE_TYPE_ATOA;//"A/A" // A ÷ A = A/A
                else if (isCur1 == 0 && isCur2 == 1) probeType = VerticalAxis.PROBE_TYPE_VA;//"V/A" // V ÷ A = V/A (电阻)
                else if (isCur1 == 1 && isCur2 == 0) probeType = VerticalAxis.PROBE_TYPE_AV;//"A/V" // A ÷ V = A/V (电导)
                else probeType = VerticalAxis.PROBE_TYPE_EMPTY;                      // 其他组合
                break;
            }
        }

        return probeType;                                                            // 返回探头类型
    }

    /**
     * 获取探头类型
     *
     * @return 探头类型
     */
    public int getProbeType(){
        return verticalAxisMathDual.getProbeType();                                  // 返回探头类型
    }

    /**
     * 设置探头类型
     *
     * @param probeType 探头类型
     */
    public void setProbeType(int probeType) {
        verticalAxisMathDual.setProbeType(probeType);                                // 设置探头类型
    }

    /**
     * 设置探头字符串
     *
     * @param probeStr 探头字符串
     */
    @Override
    public void setProbeStr(String probeStr) {
        verticalAxisMathDual.setProbeStr(probeStr);                                  // 设置探头字符串
    }

    /**
     * 获取探头字符串
     *
     * @return 探头字符串
     */
    @Override
    public String getProbeStr() {
        return verticalAxisMathDual.getProbeStr();                                   // 返回探头字符串
    }

}
