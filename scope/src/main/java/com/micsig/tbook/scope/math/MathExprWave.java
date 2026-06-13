package com.micsig.tbook.scope.math;

import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.vertical.VerticalAxis;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                MathExprWave - 数学表达式波形类                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Math模块的数学表达式波形类，位于math包下，                                  ║
 * ║   提供示波器的自定义数学表达式运算功能。                                       ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理用户自定义数学表达式字符串                                          ║
 * ║   2. 管理表达式中使用的源通道列表                                            ║
 * ║   3. 验证表达式语法有效性                                                    ║
 * ║   4. 管理表达式中的变量值（var1, var2）                                      ║
 * ║   5. 提供表达式运算结果的垂直轴参数                                          ║
 * ║                                                                              ║
 * ║ 【表达式语法】                                                               ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        表达式语法说明                                 │ ║
 * ║   │                                                                      │ ║
 * ║   │   【支持的运算符】                                                    │ ║
 * ║   │   + - * / %     加减乘除取模                                         │ ║
 * ║   │   ( )           括号                                                 │ ║
 * ║   │   sin cos tan   三角函数                                             │ ║
 * ║   │   sqrt log exp  数学函数                                             │ ║
 * ║   │                                                                      │ ║
 * ║   │   【支持的变量】                                                      │ ║
 * ║   │   CH1, CH2...   通道值                                               │ ║
 * ║   │   var1, var2     用户自定义变量                                      │ ║
 * ║   │                                                                      │ ║
 * ║   │   【示例表达式】                                                      │ ║
 * ║   │   CH1 + CH2          通道1加通道2                                    │ ║
 * ║   │   CH1 * var1         通道1乘以变量1                                  │ ║
 * ║   │   sqrt(CH1 * CH1 + CH2 * CH2)  向量和                                │ ║
 * ║   │   sin(CH1) + cos(CH2)  三角函数运算                                  │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【数据流】                                                                   ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 设置表达式  │───▶│ 验证表达式  │───▶│ 计算结果    │                   ║
 * ║   │ setExprStr  │    │ isExprValid │    │ MathNative  │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 自定义运算：用户输入任意数学表达式进行运算                             ║
 * ║   2. 复杂计算：支持三角函数、对数等高级运算                                 ║
 * ║   3. 参数化测量：通过变量实现参数化的测量功能                               ║
 * ║   4. 灵活分析：根据需要组合多个通道进行运算                                 ║
 * ║                                                                              ║
 * ║ 【继承关系】                                                                 ║
 * ║   MathWave (数学运算基类)                                                   ║
 * ║      └── MathDualWave (双通道数学运算类)                                    ║
 * ║             └── MathExprWave (数学表达式波形类)                             ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 部分方法使用synchronized保护                                            ║
 * ║   - 源通道列表操作需要同步                                                  ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - MathDualWave: 双通道数学运算基类                                        ║
 * ║   - MathChannel: 数学通道类                                                ║
 * ║   - MathNative: 数学运算Native方法                                         ║
 * ║   - MathExprError: 表达式错误类                                            ║
 * ║   - ChannelFactory: 通道工厂                                               ║
 * ║   - EventFactory: 事件工厂                                                 ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 数学表达式波形类
 * 继承自MathDualWave，提供自定义数学表达式的运算功能
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>表达式管理：设置和获取数学表达式字符串</li>
 *   <li>源通道管理：管理表达式中使用的通道列表</li>
 *   <li>表达式验证：验证表达式语法是否正确</li>
 *   <li>变量管理：设置和获取变量值（var1, var2）</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 创建表达式波形对象
 * MathExprWave exprWave = new MathExprWave(mathChannel);
 *
 * // 添加源通道
 * exprWave.addSource(ChannelFactory.CH1);
 * exprWave.addSource(ChannelFactory.CH2);
 *
 * // 设置表达式
 * exprWave.setExprString("CH1 + CH2");
 *
 * // 设置变量值
 * exprWave.setVar1(1.5);
 * exprWave.setVar2(2.0);
 * </pre>
 *
 * @see MathDualWave
 * @see MathChannel
 * @see MathExprError
 * @see MathNative
 */
public class MathExprWave extends MathDualWave {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════

    /** 数学表达式字符串，默认为"0" */
    private String exprString = "0";

    /** 表达式中使用的源通道索引列表 */
    private List<Integer> mathSources = new ArrayList<>();

    /** 表达式变化标志，true表示表达式已改变需要重新计算 */
    private boolean bExprChange = true;

    /** 用户自定义变量1 */
    private double var1 = 0;

    /** 用户自定义变量2 */
    private double var2 = 0;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 构造MathExprWave实例
     *
     * @param channel 关联的数学通道对象
     */
    public MathExprWave(MathChannel channel) {
        super(MATH_EXPR, channel);                                                   // 调用父类构造函数，设置类型为表达式类型
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 源通道管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 添加源通道到表达式
     * 线程安全方法
     * 添加后会触发通道采样变化和强制刷新
     *
     * @param chIdx 通道索引
     */
    public synchronized void addSource(int chIdx) {
        mathSources.add(chIdx);                                                      // 添加通道索引到列表
        mathChannel.chSampleChange();                                                // 通知通道采样变化
        mathChannel.forceRefresh();                                                  // 强制刷新数学通道
        sendEvent(EventFactory.EVENT_MATH_SOURCE,true);                              // 发送数学源事件
    }

    /**
     * 清除所有源通道
     * 线程安全方法
     * 清除后会触发通道采样变化和强制刷新
     */
    public synchronized void clearSource() {
        mathSources.clear();                                                         // 清空源通道列表
        mathChannel.chSampleChange();                                                // 通知通道采样变化
        mathChannel.forceRefresh();                                                  // 强制刷新数学通道
        sendEvent(EventFactory.EVENT_MATH_SOURCE,true);                              // 发送数学源事件
    }

    /**
     * 获取源通道列表
     *
     * @return 源通道索引列表
     */
    public List<Integer> getMathSources() {
        return mathSources;                                                          // 返回源通道列表
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 表达式验证方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 验证表达式是否有效
     * 静态方法，可直接调用
     *
     * @param exprString 待验证的表达式字符串
     * @return MathExprError对象，包含验证结果和错误信息
     */
    public static MathExprError isExprValid(String exprString){
        //Logger.d("isExprValid() called with: exprString = [" + exprString + "]");
        return MathNative.mathExprValid(exprString);                                 // 调用Native方法验证表达式
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 表达式管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置数学表达式字符串
     * 设置前会验证表达式有效性，只有有效的表达式才会被设置
     *
     * @param exprString 数学表达式字符串
     */
    public void setExprString(String exprString) {
        //exprString = "!1";
        MathExprError mathExprError = isExprValid(exprString);                       // 验证表达式有效性
        if (mathExprError.isSuccess() && !this.exprString.equals(exprString)) {      // 表达式有效且与当前表达式不同
            this.exprString = exprString;                                            // 设置新表达式
            mathChannel.forceRefresh();                                              // 强制刷新数学通道
            setExprChange(true);                                                     // 标记表达式已改变
        }
    }

    /**
     * 设置表达式变化标志
     *
     * @param bExprChange true: 表达式已改变
     *                    false: 表达式未改变
     */
    public void setExprChange(boolean bExprChange){
        this.bExprChange = bExprChange;                                              // 设置变化标志
    }

    /**
     * 检查表达式是否已改变
     *
     * @return true: 表达式已改变
     *         false: 表达式未改变
     */
    public boolean isExprChange(){
        return bExprChange;                                                          // 返回变化标志
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 变量管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取变量1的值
     *
     * @return 变量1的值
     */
    public double getVar1() {
        return var1;                                                                 // 返回变量1
    }

    /**
     * 设置变量1的值
     *
     * @param var1 变量1的值
     */
    public void setVar1(double var1) {
        this.var1 = var1;                                                            // 设置变量1
    }

    /**
     * 获取变量2的值
     *
     * @return 变量2的值
     */
    public double getVar2() {
        return var2;                                                                 // 返回变量2
    }

    /**
     * 设置变量2的值
     *
     * @param var2 变量2的值
     */
    public void setVar2(double var2) {
        this.var2 = var2;                                                            // 设置变量2
    }

    /**
     * 获取数学表达式字符串
     *
     * @return 数学表达式字符串
     */
    public String getExprString() {
        return this.exprString;                                                      // 返回表达式字符串
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 重写父类方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 检查指定通道是否参与数学运算
     * 重写父类方法，检查通道是否在源通道列表中
     *
     * @param chIdx 通道索引
     * @return true: 该通道参与运算
     *         false: 该通道不参与运算
     */
    @Override
    public synchronized boolean isChInSample(int chIdx) {
        for (int i = 0; i < mathSources.size(); i++) {                               // 遍历源通道列表
            if (mathSources.get(i).equals(chIdx)) {                                  // 找到匹配的通道
                return true;                                                         // 返回true
            }
        }
        return false;                                                                // 未找到，返回false
    }

    /**
     * 生成探头类型
     * 重写父类方法，表达式运算结果始终返回自定义类型
     *
     * @return 探头类型（PROBE_TYPE_CUSTOM）
     */
    @Override
    public int generateProbeType() {
        return VerticalAxis.PROBE_TYPE_CUSTOM;                                       // 返回自定义探头类型
    }

    /**
     * 获取默认档位ID
     * 重写父类方法，根据源通道档位计算默认档位
     * 计算方式：所有源通道档位值之和
     *
     * @return 默认档位ID
     */
    @Override
    public int getDefaultVScaleId() {
        double _vScale = 0;                                                          // 初始化档位值
        int chIdx = 0;                                                               // 通道索引
        Channel src = null;                                                          // 通道对象

        if(mathSources.size() > 0){                                                  // 有源通道
            for(int i=0;i<mathSources.size();i++)                                    // 遍历所有源通道
            {
                chIdx = mathSources.get(i);                                          // 获取通道索引
                src = ChannelFactory.getDynamicChannel(chIdx);                       // 获取动态通道
                if(src != null){                                                     // 通道有效
                    _vScale += src.getVScaleVal();                                   // 累加档位值
                }
            }
        }else{                                                                       // 无源通道
            _vScale = 1;                                                             // 默认档位值为1
        }
        return getVScaleVal(_vScale);                                                // 返回档位ID
    }

    /**
     * 将最大值转换为档位ID
     *
     * @param val 最大值
     * @return 档位ID
     */
    public int maxVal2VScaleId(double val){
        return getVScaleVal(val);                                                    // 返回档位ID
    }
}
