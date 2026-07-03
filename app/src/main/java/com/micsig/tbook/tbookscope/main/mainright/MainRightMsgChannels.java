package com.micsig.tbook.tbookscope.main.mainright; // 定义包路径：示波器主界面右侧面板模块

import com.micsig.base.DoubleUtil; // 导入双精度工具类，用于浮点数比较
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类，提供通道常量定义
import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量类，存储示波器配置信息
import com.micsig.tbook.ui.bean.RxBooleanWithSelect; // 导入响应式布尔包装类，支持选中状态
import com.micsig.tbook.ui.wavezone.TChan; // 导入波形通道类（未使用，可考虑移除）

import java.util.ArrayList; // 导入ArrayList集合类（未使用，可考虑移除）
import java.util.Arrays; // 导入数组工具类（未使用，可考虑移除）

/**
 * <pre>
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                        MainRightMsgChannels                                  ║
 * ║                    右侧通道CH1-CH8消息数据管理类                              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   所属模块：示波器主界面 → 右侧面板 → 通道消息管理                            ║
 * ║   层级位置：业务逻辑层 - 数据模型层                                           ║
 * ╠─────────────────────────────────────────────────────────────────────────────╣
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理CH1-CH8共8个示波器通道的开关状态和选中状态                          ║
 * ║   2. 管理各通道的单位比例变化值（用于UI更新计算）                             ║
 * ║   3. 判断通道状态变化类型（开关状态变化 vs 档位比例变化）                     ║
 * ║   4. 提供统一的通道数据访问接口（通过索引动态访问）                          ║
 * ╠─────────────────────────────────────────────────────────────────────────────╣
 * ║ 【架构设计】                                                                 ║
 * ║   设计模式：数据传输对象模式（DTO）                                           ║
 * ║   数据结构：                                                                 ║
 * ║     - 8个RxBooleanWithSelect对象：通道开关+选中状态                          ║
 * ║     - 8个double标量：通道单位比例变化值                                       ║
 * ║     - 1个double数组：比例值数组引用（与上述8个标量共享内存）                 ║
 * ║     - 1个boolean标志：标识是否来自EventBus事件                               ║
 * ║     - 1个int计数：当前系统支持的通道数量                                      ║
 * ╠─────────────────────────────────────────────────────────────────────────────╣
 * ║ 【数据流向】                                                                 ║
 * ║   输入源：                                                                   ║
 * ║     1. 用户UI操作（点击通道开关）→ ChannelFactory → setCh()方法              ║
 * ║     2. 通道档位变更 → 比例计算逻辑 → setChScale()方法                         ║
 * ║     3. EventBus事件总线 → setFromEventBus(true)                              ║
 * ║   输出方向：                                                                 ║
 * ║     1. UI层读取状态 → getCh()/getChScale() → 界面更新                        ║
 * ║     2. 状态判断逻辑 → isChangeChState() → 决定UI更新策略                     ║
 * ║     3. 数据序列化 → toString() → 日志记录/调试输出                           ║
 * ╠─────────────────────────────────────────────────────────────────────────────╣
 * ║ 【依赖关系】                                                                 ║
 * ║   上游依赖（被谁调用）：                                                     ║
 * ║     - MainRightPresenter（右侧面板业务逻辑）                                 ║
 * ║     - ChannelController（通道控制器）                                        ║
 * ║     - EventBus事件总线（跨模块通信）                                         ║
 * ║   下游依赖（调用谁）：                                                       ║
 * ║     - GlobalVar.get().getChannelsCount() → 获取系统通道数配置                ║
 * ║     - RxBooleanWithSelect → 响应式布尔值包装类                              ║
 * ║     - DoubleUtil.compareTo() → 浮点数精确比较工具                            ║
 * ║     - ChannelFactory.CH1~CH8 → 通道索引常量                                 ║
 * ╠─────────────────────────────────────────────────────────────────────────────╣
 * ║ 【使用场景】                                                                 ║
 * ║   1. 用户点击通道开关按钮，UI层通过setCh()更新状态并触发波形显示/隐藏        ║
 * ║   2. 用户调整通道档位（如电压/格），系统通过setChScale()记录比例变化         ║
 * ║   3. 业务逻辑通过isChangeChState()判断是开关变化还是档位变化                ║
 * ║   4. 多通道批量操作时，通过getCh(chIdx)统一访问不同通道                      ║
 * ║   5. 系统支持4通道或8通道模式，通过channelCount适配                           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【创建信息】                                                                 ║
 * ║   创建者：yangj                                                              ║
 * ║   创建日期：2017/5/15                                                       ║
 * ║   最后修改：待更新                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * </pre>
 */
public class MainRightMsgChannels { // 定义右侧消息通道管理类

    // ==================== 通道状态字段 ====================
    
    private RxBooleanWithSelect ch1; // CH1通道状态对象（包含开关值和选中状态）
    private RxBooleanWithSelect ch2; // CH2通道状态对象（包含开关值和选中状态）
    private RxBooleanWithSelect ch3; // CH3通道状态对象（包含开关值和选中状态）
    private RxBooleanWithSelect ch4; // CH4通道状态对象（包含开关值和选中状态）
    private RxBooleanWithSelect ch5; // CH5通道状态对象（包含开关值和选中状态）
    private RxBooleanWithSelect ch6; // CH6通道状态对象（包含开关值和选中状态）
    private RxBooleanWithSelect ch7; // CH7通道状态对象（包含开关值和选中状态）
    private RxBooleanWithSelect ch8; // CH8通道状态对象（包含开关值和选中状态）

    /**
     * 单位的变化比例，值为改变之前的数字除以改变之的数字
     * <p>
     * 说明：当通道的单位发生变化时（如从1V/div变为2V/div），该值记录变化比例
     * 用途：UI层根据该比例调整显示的数值，实现平滑过渡
     * </p>
     */
    private double ch1Scale; // CH1通道单位比例变化值（0表示无变化）
    private double ch2Scale; // CH2通道单位比例变化值（0表示无变化）
    private double ch3Scale; // CH3通道单位比例变化值（0表示无变化）
    private double ch4Scale; // CH4通道单位比例变化值（0表示无变化）
    private double ch5Scale; // CH5通道单位比例变化值（0表示无变化）
    private double ch6Scale; // CH6通道单位比例变化值（0表示无变化）
    private double ch7Scale; // CH7通道单位比例变化值（0表示无变化）
    private double ch8Scale; // CH8通道单位比例变化值（0表示无变化）

    private final double[] chxScale = {ch1Scale, ch2Scale, ch3Scale, ch4Scale, ch5Scale, ch6Scale, ch7Scale, ch8Scale}; // 比例值数组引用，用于通过索引访问（注意：数组元素与上述字段不共享内存，此数组初始化后各元素均为0）

    private boolean isFromEventBus = false; // 标识当前消息是否来自EventBus事件总线（用于区分事件来源）

    private int channelCount = GlobalVar.get().getChannelsCount(); // 当前系统支持的通道数量（4通道或8通道），从全局配置读取

    /**
     * 将所有通道的比例变化标志重置为0（表示无比例变化）
     * <p>
     * 使用场景：在设置通道开关状态之前，先清除所有比例变化标志，
     * 确保后续的状态判断能正确识别是开关变化还是比例变化
     * </p>
     */
    private void setAllChannelsScaleChangeFalse() { // 私有方法：清空所有通道比例变化值
        ch1Scale = 0; // 重置CH1比例变化值为0
        ch2Scale = 0; // 重置CH2比例变化值为0
        ch3Scale = 0; // 重置CH3比例变化值为0
        ch4Scale = 0; // 重置CH4比例变化值为0
        ch5Scale = 0; // 重置CH5比例变化值为0
        ch6Scale = 0; // 重置CH6比例变化值为0
        ch7Scale = 0; // 重置CH7比例变化值为0
        ch8Scale = 0; // 重置CH8比例变化值为0
//        Arrays.fill(chxScale, 0); // 备注代码：使用数组工具类批量填充（已注释，可能存在内存引用问题）
    }

    /**
     * 判断当前状态变化是否为通道开关状态变化
     * <p>
     * 判断逻辑：如果所有通道的比例变化值都为0，说明是开关状态变化；
     * 如果有任意一个通道的比例变化值不为0，说明是档位比例变化
     * </p>
     *
     * @return true表示是通道开关状态变化；false表示是档位比例变化
     */
    public boolean isChangeChState() { // 公开方法：判断是否为通道开关状态变化
        return !(isCh1ScaleChange() || isCh2ScaleChange() // 检查CH1-CH8是否有比例变化，取反得到是否为开关变化
                || isCh3ScaleChange() || isCh4ScaleChange() // 检查CH3、CH4是否有比例变化
                || isCh5ScaleChange() || isCh6ScaleChange() // 检查CH5、CH6是否有比例变化
                || isCh7ScaleChange() || isCh8ScaleChange() // 检查CH7、CH8是否有比例变化
        );
    }

    /**
     * 判断指定通道的比例是否发生变化
     * <p>
     * 根据通道索引判断该通道的单位比例是否发生了变化
     * </p>
     *
     * @param chIdx 通道索引，取值为ChannelFactory.CH1~CH8
     * @return true表示该通道比例已变化；false表示未变化
     */
    public boolean isChangeChScaleState(int chIdx) { // 公开方法：判断指定通道是否发生比例变化
        switch (chIdx) { // 根据通道索引进行分支判断
            case ChannelFactory.CH1: // 如果是CH1通道
                return isCh1ScaleChange(); // 返回CH1的比例变化状态
            case ChannelFactory.CH2: // 如果是CH2通道
                return isCh2ScaleChange(); // 返回CH2的比例变化状态
            case ChannelFactory.CH3: // 如果是CH3通道
                return isCh3ScaleChange(); // 返回CH3的比例变化状态
            case ChannelFactory.CH4: // 如果是CH4通道
                return isCh4ScaleChange(); // 返回CH4的比例变化状态
            case ChannelFactory.CH5: // 如果是CH5通道
                return isCh5ScaleChange(); // 返回CH5的比例变化状态
            case ChannelFactory.CH6: // 如果是CH6通道
                return isCh6ScaleChange(); // 返回CH6的比例变化状态
            case ChannelFactory.CH7: // 如果是CH7通道
                return isCh7ScaleChange(); // 返回CH7的比例变化状态
            case ChannelFactory.CH8: // 如果是CH8通道
                return isCh8ScaleChange(); // 返回CH8的比例变化状态
        }
        return false; // 如果索引无效，返回false（未变化）
    }

    /**
     * 根据通道索引设置通道的开关状态
     * <p>
     * 提供统一的访问接口，避免调用方需要编写大量if-else分支代码
     * 内部会调用对应的setChX方法进行实际设置
     * </p>
     *
     * @param chIdx 通道索引，取值为ChannelFactory.CH1~CH8
     * @param select 通道开关状态，true表示开启，false表示关闭
     */
    public void setCh(int chIdx, boolean select) { // 公开方法：通过索引设置通道状态
        switch (chIdx) { // 根据通道索引进行分支处理
            case ChannelFactory.CH1: // 如果是CH1通道
                setCh1(select); // 调用CH1的设置方法
                break; // 退出switch
            case ChannelFactory.CH2: // 如果是CH2通道
                setCh2(select); // 调用CH2的设置方法
                break; // 退出switch
            case ChannelFactory.CH3: // 如果是CH3通道
                setCh3(select); // 调用CH3的设置方法
                break; // 退出switch
            case ChannelFactory.CH4: // 如果是CH4通道
                setCh4(select); // 调用CH4的设置方法
                break; // 退出switch
            case ChannelFactory.CH5: // 如果是CH5通道
                setCh5(select); // 调用CH5的设置方法
                break; // 退出switch
            case ChannelFactory.CH6: // 如果是CH6通道
                setCh6(select); // 调用CH6的设置方法
                break; // 退出switch
            case ChannelFactory.CH7: // 如果是CH7通道
                setCh7(select); // 调用CH7的设置方法
                break; // 退出switch
            case ChannelFactory.CH8: // 如果是CH8通道
                setCh8(select); // 调用CH8的设置方法
                break; // 退出switch
        }
    }

    /**
     * 根据通道索引获取通道状态对象
     * <p>
     * 提供统一的访问接口，支持通过循环或动态索引访问不同通道
     * 如果索引无效，默认返回CH1的状态对象（容错处理）
     * </p>
     *
     * @param chIdx 通道索引，取值为ChannelFactory.CH1~CH8
     * @return 对应通道的RxBooleanWithSelect状态对象，包含开关值和选中状态
     */
    public RxBooleanWithSelect getCh(int chIdx) { // 公开方法：通过索引获取通道状态对象
        switch (chIdx) { // 根据通道索引进行分支处理
            case ChannelFactory.CH1: // 如果是CH1通道
                return getCh1(); // 返回CH1的状态对象
            case ChannelFactory.CH2: // 如果是CH2通道
                return getCh2(); // 返回CH2的状态对象
            case ChannelFactory.CH3: // 如果是CH3通道
                return getCh3(); // 返回CH3的状态对象
            case ChannelFactory.CH4: // 如果是CH4通道
                return getCh4(); // 返回CH4的状态对象
            case ChannelFactory.CH5: // 如果是CH5通道
                return getCh5(); // 返回CH5的状态对象
            case ChannelFactory.CH6: // 如果是CH6通道
                return getCh6(); // 返回CH6的状态对象
            case ChannelFactory.CH7: // 如果是CH7通道
                return getCh7(); // 返回CH7的状态对象
            case ChannelFactory.CH8: // 如果是CH8通道
                return getCh8(); // 返回CH8的状态对象
            default: // 如果索引无效
                return getCh1(); // 默认返回CH1的状态对象（容错处理）
        }
    }

    /**
     * 根据通道索引设置通道的单位比例变化值
     * <p>
     * 当通道的档位发生变化时（如电压/格从1V变为2V），记录变化比例
     * 该比例用于UI层的数值计算和显示更新
     * </p>
     *
     * @param chIdx 通道索引，取值为ChannelFactory.CH1~CH8
     * @param scale 单位比例变化值，计算公式：变化前数值 / 变化后数值
     */
    public void setChScale(int chIdx, double scale) { // 公开方法：通过索引设置通道比例变化值
        switch (chIdx) { // 根据通道索引进行分支处理
            case ChannelFactory.CH1: // 如果是CH1通道
                setCh1Scale(scale); // 设置CH1的比例变化值
                break; // 退出switch
            case ChannelFactory.CH2: // 如果是CH2通道
                setCh2Scale(scale); // 设置CH2的比例变化值
                break; // 退出switch
            case ChannelFactory.CH3: // 如果是CH3通道
                setCh3Scale(scale); // 设置CH3的比例变化值
                break; // 退出switch
            case ChannelFactory.CH4: // 如果是CH4通道
                setCh4Scale(scale); // 设置CH4的比例变化值
                break; // 退出switch
            case ChannelFactory.CH5: // 如果是CH5通道
                setCh5Scale(scale); // 设置CH5的比例变化值
                break; // 退出switch
            case ChannelFactory.CH6: // 如果是CH6通道
                setCh6Scale(scale); // 设置CH6的比例变化值
                break; // 退出switch
            case ChannelFactory.CH7: // 如果是CH7通道
                setCh7Scale(scale); // 设置CH7的比例变化值
                break; // 退出switch
            case ChannelFactory.CH8: // 如果是CH8通道
                setCh8Scale(scale); // 设置CH8的比例变化值
                break; // 退出switch
        }
    }

    /**
     * 根据通道索引获取通道的单位比例变化值
     * <p>
     * 读取指定通道的比例变化值，用于判断该通道是否发生了档位变化
     * 如果索引无效，默认返回CH1的比例值（容错处理）
     * </p>
     *
     * @param chIdx 通道索引，取值为ChannelFactory.CH1~CH8
     * @return 对应通道的单位比例变化值，0表示无变化
     */
    public double getChScale(int chIdx) { // 公开方法：通过索引获取通道比例变化值
        switch (chIdx) { // 根据通道索引进行分支处理
            case ChannelFactory.CH1: // 如果是CH1通道
                return getCh1Scale(); // 返回CH1的比例变化值
            case ChannelFactory.CH2: // 如果是CH2通道
                return getCh2Scale(); // 返回CH2的比例变化值
            case ChannelFactory.CH3: // 如果是CH3通道
                return getCh3Scale(); // 返回CH3的比例变化值
            case ChannelFactory.CH4: // 如果是CH4通道
                return getCh4Scale(); // 返回CH4的比例变化值
            case ChannelFactory.CH5: // 如果是CH5通道
                return getCh5Scale(); // 返回CH5的比例变化值
            case ChannelFactory.CH6: // 如果是CH6通道
                return getCh6Scale(); // 返回CH6的比例变化值
            case ChannelFactory.CH7: // 如果是CH7通道
                return getCh7Scale(); // 返回CH7的比例变化值
            case ChannelFactory.CH8: // 如果是CH8通道
                return getCh8Scale(); // 返回CH8的比例变化值
            default: // 如果索引无效
                return getCh1Scale(); // 默认返回CH1的比例变化值（容错处理）
        }
    }

    /**
     * 判断当前消息是否来自EventBus事件总线
     * <p>
     * 用途：区分消息来源，避免事件循环触发
     * 场景：EventBus事件触发的更新不需要再次发送EventBus事件
     * </p>
     *
     * @return true表示来自EventBus；false表示来自UI或其他来源
     */
    public boolean isFromEventBus() { // 公开方法：判断是否来自EventBus
        return isFromEventBus; // 返回EventBus标志
    }

    /**
     * 设置消息来源标志
     * <p>
     * 当消息来自EventBus时设置为true，处理完成后应重置为false
     * 用于防止EventBus事件的循环触发
     * </p>
     *
     * @param fromEventBus true表示来自EventBus；false表示非EventBus来源
     */
    public void setFromEventBus(boolean fromEventBus) { // 公开方法：设置EventBus来源标志
        isFromEventBus = fromEventBus; // 更新EventBus标志
    }

    // ==================== CH1通道的Getter/Setter方法 ====================

    /**
     * 获取CH1通道的单位比例变化值
     *
     * @return CH1的比例变化值，0表示无变化
     */
    public double getCh1Scale() { // 公开方法：获取CH1比例变化值
        return ch1Scale; // 返回CH1的比例值
    }

    /**
     * 设置CH1通道的单位比例变化值
     *
     * @param ch1Scale 比例变化值，计算公式：变化前数值 / 变化后数值
     */
    public void setCh1Scale(double ch1Scale) { // 公开方法：设置CH1比例变化值
        this.ch1Scale = ch1Scale; // 更新CH1的比例值
    }

    // ==================== CH2通道的Getter/Setter方法 ====================

    /**
     * 获取CH2通道的单位比例变化值
     *
     * @return CH2的比例变化值，0表示无变化
     */
    public double getCh2Scale() { // 公开方法：获取CH2比例变化值
        return ch2Scale; // 返回CH2的比例值
    }

    /**
     * 设置CH2通道的单位比例变化值
     *
     * @param ch2Scale 比例变化值，计算公式：变化前数值 / 变化后数值
     */
    public void setCh2Scale(double ch2Scale) { // 公开方法：设置CH2比例变化值
        this.ch2Scale = ch2Scale; // 更新CH2的比例值
    }

    // ==================== CH3通道的Getter/Setter方法 ====================

    /**
     * 获取CH3通道的单位比例变化值
     *
     * @return CH3的比例变化值，0表示无变化
     */
    public double getCh3Scale() { // 公开方法：获取CH3比例变化值
        return ch3Scale; // 返回CH3的比例值
    }

    /**
     * 设置CH3通道的单位比例变化值
     *
     * @param ch3Scale 比例变化值，计算公式：变化前数值 / 变化后数值
     */
    public void setCh3Scale(double ch3Scale) { // 公开方法：设置CH3比例变化值
        this.ch3Scale = ch3Scale; // 更新CH3的比例值
    }

    // ==================== CH4通道的Getter/Setter方法 ====================

    /**
     * 获取CH4通道的单位比例变化值
     *
     * @return CH4的比例变化值，0表示无变化
     */
    public double getCh4Scale() { // 公开方法：获取CH4比例变化值
        return ch4Scale; // 返回CH4的比例值
    }

    /**
     * 设置CH4通道的单位比例变化值
     *
     * @param ch4Scale 比例变化值，计算公式：变化前数值 / 变化后数值
     */
    public void setCh4Scale(double ch4Scale) { // 公开方法：设置CH4比例变化值
        this.ch4Scale = ch4Scale; // 更新CH4的比例值
    }

    // ==================== CH5通道的Getter/Setter方法 ====================

    /**
     * 获取CH5通道的单位比例变化值
     *
     * @return CH5的比例变化值，0表示无变化
     */
    public double getCh5Scale() { // 公开方法：获取CH5比例变化值
        return ch5Scale; // 返回CH5的比例值
    }

    /**
     * 设置CH5通道的单位比例变化值
     *
     * @param ch5Scale 比例变化值，计算公式：变化前数值 / 变化后数值
     */
    public void setCh5Scale(double ch5Scale) { // 公开方法：设置CH5比例变化值
        this.ch5Scale = ch5Scale; // 更新CH5的比例值
    }

    // ==================== CH6通道的Getter/Setter方法 ====================

    /**
     * 获取CH6通道的单位比例变化值
     *
     * @return CH6的比例变化值，0表示无变化
     */
    public double getCh6Scale() { // 公开方法：获取CH6比例变化值
        return ch6Scale; // 返回CH6的比例值
    }

    /**
     * 设置CH6通道的单位比例变化值
     *
     * @param ch6Scale 比例变化值，计算公式：变化前数值 / 变化后数值
     */
    public void setCh6Scale(double ch6Scale) { // 公开方法：设置CH6比例变化值
        this.ch6Scale = ch6Scale; // 更新CH6的比例值
    }

    // ==================== CH7通道的Getter/Setter方法 ====================

    /**
     * 获取CH7通道的单位比例变化值
     *
     * @return CH7的比例变化值，0表示无变化
     */
    public double getCh7Scale() { // 公开方法：获取CH7比例变化值
        return ch7Scale; // 返回CH7的比例值
    }

    /**
     * 设置CH7通道的单位比例变化值
     *
     * @param ch7Scale 比例变化值，计算公式：变化前数值 / 变化后数值
     */
    public void setCh7Scale(double ch7Scale) { // 公开方法：设置CH7比例变化值
        this.ch7Scale = ch7Scale; // 更新CH7的比例值
    }


    // ==================== CH8通道的Getter/Setter方法 ====================

    /**
     * 获取CH8通道的单位比例变化值
     *
     * @return CH8的比例变化值，0表示无变化
     */
    public double getCh8Scale() { // 公开方法：获取CH8比例变化值
        return ch8Scale; // 返回CH8的比例值
    }

    /**
     * 设置CH8通道的单位比例变化值
     *
     * @param ch8Scale 比例变化值，计算公式：变化前数值 / 变化后数值
     */
    public void setCh8Scale(double ch8Scale) { // 公开方法：设置CH8比例变化值
        this.ch8Scale = ch8Scale; // 更新CH8的比例值
    }

    // ==================== CH1-CH8比例变化判断方法 ====================

    /**
     * 判断CH1通道的比例是否发生变化
     * <p>
     * 使用DoubleUtil工具类进行精确浮点数比较，避免精度误差
     * </p>
     *
     * @return true表示比例已变化（值不为0）；false表示未变化（值为0）
     */
    public boolean isCh1ScaleChange() { // 公开方法：判断CH1比例是否变化
        return DoubleUtil.compareTo(ch1Scale, 0.0) != 0; // 使用工具类比较浮点数是否不等于0
    }

    /**
     * 判断CH2通道的比例是否发生变化
     * <p>
     * 使用DoubleUtil工具类进行精确浮点数比较，避免精度误差
     * </p>
     *
     * @return true表示比例已变化（值不为0）；false表示未变化（值为0）
     */
    public boolean isCh2ScaleChange() { // 公开方法：判断CH2比例是否变化
        return DoubleUtil.compareTo(ch2Scale, 0.0) != 0; // 使用工具类比较浮点数是否不等于0
    }

    /**
     * 判断CH3通道的比例是否发生变化
     * <p>
     * 使用DoubleUtil工具类进行精确浮点数比较，避免精度误差
     * </p>
     *
     * @return true表示比例已变化（值不为0）；false表示未变化（值为0）
     */
    public boolean isCh3ScaleChange() { // 公开方法：判断CH3比例是否变化
        return DoubleUtil.compareTo(ch3Scale, 0.0) != 0; // 使用工具类比较浮点数是否不等于0
    }

    /**
     * 判断CH4通道的比例是否发生变化
     * <p>
     * 使用DoubleUtil工具类进行精确浮点数比较，避免精度误差
     * </p>
     *
     * @return true表示比例已变化（值不为0）；false表示未变化（值为0）
     */
    public boolean isCh4ScaleChange() { // 公开方法：判断CH4比例是否变化
        return DoubleUtil.compareTo(ch4Scale, 0.0) != 0; // 使用工具类比较浮点数是否不等于0
    }

    /**
     * 判断CH5通道的比例是否发生变化
     * <p>
     * 使用DoubleUtil工具类进行精确浮点数比较，避免精度误差
     * </p>
     *
     * @return true表示比例已变化（值不为0）；false表示未变化（值为0）
     */
    public boolean isCh5ScaleChange() { // 公开方法：判断CH5比例是否变化
        return DoubleUtil.compareTo(ch5Scale, 0.0) != 0; // 使用工具类比较浮点数是否不等于0
    }

    /**
     * 判断CH6通道的比例是否发生变化
     * <p>
     * 使用DoubleUtil工具类进行精确浮点数比较，避免精度误差
     * </p>
     *
     * @return true表示比例已变化（值不为0）；false表示未变化（值为0）
     */
    public boolean isCh6ScaleChange() { // 公开方法：判断CH6比例是否变化
        return DoubleUtil.compareTo(ch6Scale, 0.0) != 0; // 使用工具类比较浮点数是否不等于0
    }

    /**
     * 判断CH7通道的比例是否发生变化
     * <p>
     * 使用DoubleUtil工具类进行精确浮点数比较，避免精度误差
     * </p>
     *
     * @return true表示比例已变化（值不为0）；false表示未变化（值为0）
     */
    public boolean isCh7ScaleChange() { // 公开方法：判断CH7比例是否变化
        return DoubleUtil.compareTo(ch7Scale, 0.0) != 0; // 使用工具类比较浮点数是否不等于0
    }

    /**
     * 判断CH8通道的比例是否发生变化
     * <p>
     * 使用DoubleUtil工具类进行精确浮点数比较，避免精度误差
     * </p>
     *
     * @return true表示比例已变化（值不为0）；false表示未变化（值为0）
     */
    public boolean isCh8ScaleChange() { // 公开方法：判断CH8比例是否变化
        return DoubleUtil.compareTo(ch8Scale, 0.0) != 0; // 使用工具类比较浮点数是否不等于0
    }

    // ==================== CH1通道状态设置方法 ====================

    /**
     * 设置CH1通道的开关状态
     * <p>
     * 执行流程：
     * 1. 清除所有通道的比例变化标志
     * 2. 如果状态对象为null，创建新对象
     * 3. 如果状态对象已存在，更新值并设置为选中状态
     * 4. 清除其他通道的选中状态，确保单选
     * </p>
     *
     * @param ch1 通道开关状态，true表示开启，false表示关闭
     */
    public void setCh1(boolean ch1) { // 公开方法：设置CH1通道状态
        setAllChannelsScaleChangeFalse(); // 步骤1：清除所有通道的比例变化标志
        if (this.ch1 == null) { // 步骤2：判断状态对象是否为null
            this.ch1 = new RxBooleanWithSelect(ch1); // 创建新的状态对象，传入初始值
        } else { // 步骤3：状态对象已存在
            this.ch1.setValue(ch1); // 更新通道开关值
            setAllUnSelect(); // 清除所有通道的选中状态
            this.ch1.setRxMsgSelect(true); // 将CH1设置为选中状态
        }
    }

    // ==================== CH2通道状态设置方法 ====================

    /**
     * 设置CH2通道的开关状态
     * <p>
     * 执行流程：
     * 1. 清除所有通道的比例变化标志
     * 2. 如果状态对象为null，创建新对象
     * 3. 如果状态对象已存在，更新值并设置为选中状态
     * 4. 清除其他通道的选中状态，确保单选
     * </p>
     *
     * @param ch2 通道开关状态，true表示开启，false表示关闭
     */
    public void setCh2(boolean ch2) { // 公开方法：设置CH2通道状态
        setAllChannelsScaleChangeFalse(); // 步骤1：清除所有通道的比例变化标志
        if (this.ch2 == null) { // 步骤2：判断状态对象是否为null
            this.ch2 = new RxBooleanWithSelect(ch2); // 创建新的状态对象，传入初始值
        } else { // 步骤3：状态对象已存在
            this.ch2.setValue(ch2); // 更新通道开关值
            setAllUnSelect(); // 清除所有通道的选中状态
            this.ch2.setRxMsgSelect(true); // 将CH2设置为选中状态

        }
    }

    // ==================== CH3通道状态设置方法 ====================

    /**
     * 设置CH3通道的开关状态
     * <p>
     * 执行流程：
     * 1. 清除所有通道的比例变化标志
     * 2. 如果状态对象为null，创建新对象
     * 3. 如果状态对象已存在，更新值并设置为选中状态
     * 4. 清除其他通道的选中状态，确保单选
     * </p>
     *
     * @param ch3 通道开关状态，true表示开启，false表示关闭
     */
    public void setCh3(boolean ch3) { // 公开方法：设置CH3通道状态
        setAllChannelsScaleChangeFalse(); // 步骤1：清除所有通道的比例变化标志
        if (this.ch3 == null) { // 步骤2：判断状态对象是否为null
            this.ch3 = new RxBooleanWithSelect(ch3); // 创建新的状态对象，传入初始值
        } else { // 步骤3：状态对象已存在
            this.ch3.setValue(ch3); // 更新通道开关值
            setAllUnSelect(); // 清除所有通道的选中状态
            this.ch3.setRxMsgSelect(true); // 将CH3设置为选中状态
        }
    }

    // ==================== CH4通道状态设置方法 ====================

    /**
     * 设置CH4通道的开关状态
     * <p>
     * 执行流程：
     * 1. 清除所有通道的比例变化标志
     * 2. 如果状态对象为null，创建新对象
     * 3. 如果状态对象已存在，更新值并设置为选中状态
     * 4. 清除其他通道的选中状态，确保单选
     * </p>
     *
     * @param ch4 通道开关状态，true表示开启，false表示关闭
     */
    public void setCh4(boolean ch4) { // 公开方法：设置CH4通道状态
        setAllChannelsScaleChangeFalse(); // 步骤1：清除所有通道的比例变化标志
        if (this.ch4 == null) { // 步骤2：判断状态对象是否为null
            this.ch4 = new RxBooleanWithSelect(ch4); // 创建新的状态对象，传入初始值
        } else { // 步骤3：状态对象已存在
            this.ch4.setValue(ch4); // 更新通道开关值
            setAllUnSelect(); // 清除所有通道的选中状态
            this.ch4.setRxMsgSelect(true); // 将CH4设置为选中状态
        }
    }

    // ==================== CH5通道状态设置方法 ====================

    /**
     * 设置CH5通道的开关状态
     * <p>
     * 执行流程：
     * 1. 清除所有通道的比例变化标志
     * 2. 如果状态对象为null，创建新对象
     * 3. 如果状态对象已存在，更新值并设置为选中状态
     * 4. 清除其他通道的选中状态，确保单选
     * </p>
     *
     * @param ch5 通道开关状态，true表示开启，false表示关闭
     */
    public void setCh5(boolean ch5) { // 公开方法：设置CH5通道状态
        setAllChannelsScaleChangeFalse(); // 步骤1：清除所有通道的比例变化标志
        if (this.ch5 == null) { // 步骤2：判断状态对象是否为null
            this.ch5 = new RxBooleanWithSelect(ch5); // 创建新的状态对象，传入初始值
        } else { // 步骤3：状态对象已存在
            this.ch5.setValue(ch5); // 更新通道开关值
            setAllUnSelect(); // 清除所有通道的选中状态
            this.ch5.setRxMsgSelect(true); // 将CH5设置为选中状态
        }
    }

    // ==================== CH6通道状态设置方法 ====================

    /**
     * 设置CH6通道的开关状态
     * <p>
     * 执行流程：
     * 1. 清除所有通道的比例变化标志
     * 2. 如果状态对象为null，创建新对象
     * 3. 如果状态对象已存在，更新值并设置为选中状态
     * 4. 清除其他通道的选中状态，确保单选
     * </p>
     *
     * @param ch6 通道开关状态，true表示开启，false表示关闭
     */
    public void setCh6(boolean ch6) { // 公开方法：设置CH6通道状态
        setAllChannelsScaleChangeFalse(); // 步骤1：清除所有通道的比例变化标志
        if (this.ch6 == null) { // 步骤2：判断状态对象是否为null
            this.ch6 = new RxBooleanWithSelect(ch6); // 创建新的状态对象，传入初始值
        } else { // 步骤3：状态对象已存在
            this.ch6.setValue(ch6); // 更新通道开关值
            setAllUnSelect(); // 清除所有通道的选中状态
            this.ch6.setRxMsgSelect(true); // 将CH6设置为选中状态
        }
    }

    // ==================== CH7通道状态设置方法 ====================

    /**
     * 设置CH7通道的开关状态
     * <p>
     * 执行流程：
     * 1. 清除所有通道的比例变化标志
     * 2. 如果状态对象为null，创建新对象
     * 3. 如果状态对象已存在，更新值并设置为选中状态
     * 4. 清除其他通道的选中状态，确保单选
     * </p>
     *
     * @param ch7 通道开关状态，true表示开启，false表示关闭
     */
    public void setCh7(boolean ch7) { // 公开方法：设置CH7通道状态
        setAllChannelsScaleChangeFalse(); // 步骤1：清除所有通道的比例变化标志
        if (this.ch7 == null) { // 步骤2：判断状态对象是否为null
            this.ch7= new RxBooleanWithSelect(ch7); // 创建新的状态对象，传入初始值
        } else { // 步骤3：状态对象已存在
            this.ch7.setValue(ch7); // 更新通道开关值
            setAllUnSelect(); // 清除所有通道的选中状态
            this.ch7.setRxMsgSelect(true); // 将CH7设置为选中状态
        }
    }

    // ==================== CH8通道状态设置方法 ====================

    /**
     * 设置CH8通道的开关状态
     * <p>
     * 执行流程：
     * 1. 清除所有通道的比例变化标志
     * 2. 如果状态对象为null，创建新对象
     * 3. 如果状态对象已存在，更新值并设置为选中状态
     * 4. 清除其他通道的选中状态，确保单选
     * </p>
     *
     * @param ch8 通道开关状态，true表示开启，false表示关闭
     */
    public void setCh8(boolean ch8) { // 公开方法：设置CH8通道状态
        setAllChannelsScaleChangeFalse(); // 步骤1：清除所有通道的比例变化标志
        if (this.ch8 == null) { // 步骤2：判断状态对象是否为null
            this.ch8 = new RxBooleanWithSelect(ch8); // 创建新的状态对象，传入初始值
        } else { // 步骤3：状态对象已存在
            this.ch8.setValue(ch8); // 更新通道开关值
            setAllUnSelect(); // 清除所有通道的选中状态
            this.ch8.setRxMsgSelect(true); // 将CH8设置为选中状态
        }
    }

    // ==================== CH1-CH8通道状态获取方法 ====================

    /**
     * 获取CH1通道的状态对象
     *
     * @return CH1的RxBooleanWithSelect对象，包含开关值和选中状态
     */
    public RxBooleanWithSelect getCh1() { // 公开方法：获取CH1状态对象
        return ch1; // 返回CH1的状态对象
    }

    /**
     * 获取CH2通道的状态对象
     *
     * @return CH2的RxBooleanWithSelect对象，包含开关值和选中状态
     */
    public RxBooleanWithSelect getCh2() { // 公开方法：获取CH2状态对象
        return ch2; // 返回CH2的状态对象
    }

    /**
     * 获取CH3通道的状态对象
     *
     * @return CH3的RxBooleanWithSelect对象，包含开关值和选中状态
     */
    public RxBooleanWithSelect getCh3() { // 公开方法：获取CH3状态对象
        return ch3; // 返回CH3的状态对象
    }

    /**
     * 获取CH4通道的状态对象
     *
     * @return CH4的RxBooleanWithSelect对象，包含开关值和选中状态
     */
    public RxBooleanWithSelect getCh4() { // 公开方法：获取CH4状态对象
        return ch4; // 返回CH4的状态对象
    }

    /**
     * 获取CH5通道的状态对象
     *
     * @return CH5的RxBooleanWithSelect对象，包含开关值和选中状态
     */
    public RxBooleanWithSelect getCh5() { // 公开方法：获取CH5状态对象
        return ch5; // 返回CH5的状态对象
    }

    /**
     * 获取CH6通道的状态对象
     *
     * @return CH6的RxBooleanWithSelect对象，包含开关值和选中状态
     */
    public RxBooleanWithSelect getCh6(){ // 公开方法：获取CH6状态对象
        return ch6; // 返回CH6的状态对象
    }

    /**
     * 获取CH7通道的状态对象
     *
     * @return CH7的RxBooleanWithSelect对象，包含开关值和选中状态
     */
    public RxBooleanWithSelect getCh7(){ // 公开方法：获取CH7状态对象
        return ch7; // 返回CH7的状态对象
    }

    /**
     * 获取CH8通道的状态对象
     *
     * @return CH8的RxBooleanWithSelect对象，包含开关值和选中状态
     */
    public RxBooleanWithSelect getCh8(){ // 公开方法：获取CH8状态对象
        return ch8; // 返回CH8的状态对象
    }

    // ==================== 辅助方法 ====================

    /**
     * 清除所有通道的选中状态
     * <p>
     * 功能：将所有通道的rxMsgSelect标志设置为false
     * 使用场景：在设置某个通道为选中状态之前，先清除所有通道的选中状态，
     * 确保同一时刻只有一个通道处于选中状态（单选模式）
     * 适配逻辑：根据系统配置的通道数量（4通道或8通道）进行不同的处理
     * </p>
     */
    private void setAllUnSelect() { // 私有方法：清除所有通道选中状态
        if (ch1!=null) ch1.setRxMsgSelect(false); // 如果CH1状态对象存在，清除其选中状态
        if (ch2!=null) ch2.setRxMsgSelect(false); // 如果CH2状态对象存在，清除其选中状态
        if (channelCount == GlobalVar.CHANNEL_COUNT_4) { // 如果系统配置为4通道模式
            if (ch3!=null) ch3.setRxMsgSelect(false); // 清除CH3的选中状态（如果存在）
            if (ch4!=null) ch4.setRxMsgSelect(false); // 清除CH4的选中状态（如果存在）
        } else if (channelCount == GlobalVar.CHANNEL_COUNT_8) { // 如果系统配置为8通道模式
            if (ch3!=null) ch3.setRxMsgSelect(false); // 清除CH3的选中状态（如果存在）
            if (ch4!=null) ch4.setRxMsgSelect(false); // 清除CH4的选中状态（如果存在）
            if (ch5!=null) ch5.setRxMsgSelect(false); // 清除CH5的选中状态（如果存在）
            if (ch6!=null) ch6.setRxMsgSelect(false); // 清除CH6的选中状态（如果存在）
            if (ch7!=null) ch7.setRxMsgSelect(false); // 清除CH7的选中状态（如果存在）
            if (ch8!=null) ch8.setRxMsgSelect(false); // 清除CH8的选中状态（如果存在）
        }
    }

    /**
     * 批量设置CH2-CH8通道的单位比例变化值（不包括CH1）
     * <p>
     * 使用场景：当需要同时设置多个通道的比例值时使用
     * 注意：此方法不设置CH1的比例值，CH1需要单独设置
     * </p>
     *
     * @param scale 统一的比例变化值
     */
    public void setAllChScale(double scale) { // 公开方法：批量设置CH2-CH8的比例值
        setCh2Scale(scale); // 设置CH2的比例值
        setCh3Scale(scale); // 设置CH3的比例值
        setCh4Scale(scale); // 设置CH4的比例值
        setCh5Scale(scale); // 设置CH5的比例值
        setCh6Scale(scale); // 设置CH6的比例值
        setCh7Scale(scale); // 设置CH7的比例值
        setCh8Scale(scale); // 设置CH8的比例值
    }

    /**
     * 检测所有通道中是否有任意一个通道的状态发生变化
     * <p>
     * 判断逻辑：如果所有通道的比例变化值都为0，说明是开关状态变化；
     * 如果有任意一个通道的比例变化值不为0，说明是档位比例变化
     * 方法命名：isChangeChXState表示"是否是通道X状态变化"，返回true表示状态变化（开关），false表示比例变化
     * </p>
     *
     * @return true表示是通道开关状态变化；false表示是档位比例变化
     */
    public boolean isChangeChXState() { // 公开方法：判断是否有通道状态变化
        return !(isChXScaleChange(0) || isChXScaleChange(1) // 检查索引0-3的通道比例变化
                || isChXScaleChange(2) || isChXScaleChange(3) // 继续检查索引2-3
                || isChXScaleChange(4) || isChXScaleChange(5) // 检查索引4-5的通道比例变化
                || isChXScaleChange(6) || isChXScaleChange(7)); // 检查索引6-7的通道比例变化
    }

    /**
     * 判断指定索引的通道比例是否发生变化
     * <p>
     * 通过数组索引访问比例值，支持动态索引访问
     * 边界检查：如果索引越界（<0或>=数组长度），返回false
     * 注意：此方法访问的是chxScale数组，而非独立的chXScale字段
     * </p>
     *
     * @param chIndex 通道索引，取值范围0-7（对应CH1-CH8）
     * @return true表示比例已变化；false表示未变化或索引越界
     */
    public boolean isChXScaleChange(int chIndex) { // 公开方法：判断指定索引的通道比例是否变化
        if (chIndex >= chxScale.length || chIndex < 0) return false; // 边界检查：索引越界则返回false
        return DoubleUtil.compareTo(chxScale[chIndex], 0.0) != 0; // 使用工具类比较浮点数是否不等于0
    }

    /**
     * 重写toString方法，输出所有通道的状态信息
     * <p>
     * 用途：用于日志记录、调试输出和问题排查
     * 格式：MainRightMsgChannels{ch1=..., ch2=..., ..., isFromEventBus=...}
     * </p>
     *
     * @return 包含所有通道状态信息的字符串
     */
    @Override
    public String toString() { // 重写Object类的toString方法
        StringBuilder sb = new StringBuilder("MainRightMsgChannels{"); // 创建StringBuilder，添加类名前缀
        sb.append("ch1=").append(ch1); // 添加CH1状态信息
        sb.append(", ch2=").append(ch2); // 添加CH2状态信息
        sb.append(", ch3=").append(ch3); // 添加CH3状态信息
        sb.append(", ch4=").append(ch4); // 添加CH4状态信息
        sb.append(", ch5=").append(ch5); // 添加CH5状态信息
        sb.append(", ch6=").append(ch6); // 添加CH6状态信息
        sb.append(", ch7=").append(ch7); // 添加CH7状态信息
        sb.append(", ch8=").append(ch8); // 添加CH8状态信息
        sb.append(", ch1Scale=").append(ch1Scale); // 添加CH1比例变化值
        sb.append(", ch2Scale=").append(ch2Scale); // 添加CH2比例变化值
        sb.append(", ch3Scale=").append(ch3Scale); // 添加CH3比例变化值
        sb.append(", ch4Scale=").append(ch4Scale); // 添加CH4比例变化值
        sb.append(", ch5Scale=").append(ch5Scale); // 添加CH5比例变化值
        sb.append(", ch6Scale=").append(ch6Scale); // 添加CH6比例变化值
        sb.append(", ch7Scale=").append(ch7Scale); // 添加CH7比例变化值
        sb.append(", ch8Scale=").append(ch8Scale); // 添加CH8比例变化值
        sb.append(", isFromEventBus=").append(isFromEventBus); // 添加EventBus来源标志
        sb.append('}'); // 添加类名后缀
        return sb.toString(); // 返回完整的字符串表示

    }
}