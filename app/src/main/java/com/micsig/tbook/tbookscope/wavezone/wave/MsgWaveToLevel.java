package com.micsig.tbook.tbookscope.wavezone.wave;   // 示波器波形显示区域-波形子模块包，包含波形到电平消息类

/**
 * 波形到电平消息类 - 用于传递波形类型和通道信息
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    波形到电平消息类架构                          │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │  ┌─────────────────────────────────────────────┐               │
 * │  │         WaveManage_YT                        │               │
 * │  │       (YT模式波形管理类)                      │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  用户操作                             │   │               │
 * │  │  │    - 点击波形标签                     │   │               │
 * │  │  │    - 拖动波形                         │   │               │
 * │  │  │    - 选择通道                         │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  创建消息                             │   │               │
 * │  │  │    └─ new MsgWaveToLevel(            │   │               │
 * │  │  │        LEVELTYPE_TRIGGER,            │   │               │
 * │  │  │        TChan.Ch1                     │   │               │
 * │  │  │      )                               │   │               │
 * │  │  │                                     │   │               │
 * │  │  │  ┌─────────────────────────────┐   │   │               │
 * │  │  │  │  MsgWaveToLevel             │   │   │               │
 * │  │  │  │  (本类)                     │   │   │               │
 * │  │  │  │                             │   │   │               │
 * │  │  │  │  属性：                     │   │   │               │
 * │  │  │  │    - levelType: 电平类型    │   │   │               │
 * │  │  │  │    - curCh: 当前通道        │   │   │               │
 * │  │  │  │                             │   │   │               │
 * │  │  │  │  电平类型常量：             │   │   │               │
 * │  │  │  │    - LEVELTYPE_TRIGGER (0)  │   │   │               │
 * │  │  │  │    - LEVELTYPE_VALUE1 (1)   │   │   │               │
 * │  │  │  │    - LEVELTYPE_VALUE2 (2)   │   │   │               │
 * │  │  │  │    - LEVELTYPE_VALUE3 (3)   │   │   │               │
 * │  │  │  │    - LEVELTYPE_VALUE4 (4)   │   │   │               │
 * │  │  │  │                             │   │   │               │
 * │  │  │  │  功能：                     │   │   │               │
 * │  │  │  │    - 封装电平类型和通道     │   │   │               │
 * │  │  │  │    - 传递波形到电平信息     │   │   │               │
 * │  │  │  │    - 用于RxBus事件传递      │   │   │               │
 * │  │  │  └─────────────────────────────┘   │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  发送事件                             │   │               │
 * │  │  │    └─ RxBus.getInstance().post(      │   │               │
 * │  │  │        RxEnum.WAVE_TO_LEVEL,         │   │               │
 * │  │  │        msgWaveToLevel                │   │               │
 * │  │  │      )                               │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  接收事件                             │   │               │
 * │  │  │    └─ TriggerVoltageLine             │   │               │
 * │  │  │    └─ DiscreetVoltageLine            │   │               │
 * │  │  │    └─ 触发电平显示组件               │   │               │
 * │  │  │                                     │   │               │
 * │  │  │  处理逻辑：                          │   │               │
 * │  │  │    - 根据levelType判断电平类型       │   │               │
 * │  │  │    - 根据curCh判断通道               │   │               │
 * │  │  │    - 更新触发电平位置                │   │               │
 * │  │  │    - 更新电平值显示                  │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  └─────────────────────────────────────────────┘               │
 * │                                                                 │
 * │  ┌─────────────────────────────────────────────┐               │
 * │  │           电平类型说明                        │               │
 * │  ├─────────────────────────────────────────────┤               │
 * │  │                                             │               │
 * │  │  LEVELTYPE_TRIGGER (0):                     │               │
 * │  │    - 触发电平                                │               │
 * │  │    - 用于触发系统                            │               │
 * │  │    - 显示触发电压线                          │               │
 * │  │                                             │               │
 * │  │  LEVELTYPE_VALUE1 (1):                      │               │
 * │  │    - 电平值1                                 │               │
 * │  │    - 用于测量系统                            │               │
 * │  │    - 显示测量电平线                          │               │
 * │  │                                             │               │
 * │  │  LEVELTYPE_VALUE2 (2):                      │               │
 * │  │    - 电平值2                                 │               │
 * │  │    - 用于测量系统                            │               │
 * │  │    - 显示测量电平线                          │               │
 * │  │                                             │               │
 * │  │  LEVELTYPE_VALUE3 (3):                      │               │
 * │  │    - 电平值3                                 │               │
 * │  │    - 用于测量系统                            │               │
 * │  │    - 显示测量电平线                          │               │
 * │  │                                             │               │
 * │  │  LEVELTYPE_VALUE4 (4):                      │               │
 * │  │    - 电平值4                                 │               │
 * │  │    - 用于测量系统                            │               │
 * │  │    - 显示测量电平线                          │               │
 * │  │                                             │               │
 * │  └─────────────────────────────────────────────┘               │
 * │                                                                 │
 * │  ┌─────────────────────────────────────────────┐               │
 * │  │           使用场景                            │               │
 * │  ├─────────────────────────────────────────────┤               │
 * │  │                                             │               │
 * │  │  场景1: 触发电平调整                          │               │
 * │  │    └─ 用户拖动触发电压线                     │               │
 * │  │    └─ WaveManage_YT发送消息                 │               │
 * │  │    └─ TriggerVoltageLine接收消息            │               │
 * │  │    └─ 更新触发电平位置                       │               │
 * │  │                                             │               │
 * │  │  场景2: 测量电平调整                          │               │
 * │  │    └─ 用户拖动测量电平线                     │               │
 * │  │    └ WaveManage_YT发送消息                 │               │
 * │  │    └─ DiscreetVoltageLine接收消息           │               │
 * │  │    └─ 更新测量电平位置                       │               │
 * │  │                                             │               │
 * │  │  场景3: 通道切换                              │               │
 * │  │    └─ 用户选择不同通道                       │               │
 * │  │    └─ WaveManage_YT发送消息                 │               │
 * │  │    └─ 触发电平组件接收消息                   │               │
 * │  │    └─ 更新触发电平通道                       │               │
 * │  │                                             │               │
 * │  └─────────────────────────────────────────────┘               │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * <h3>模块定位</h3>
 * <p>本类是示波器波形显示区域的波形到电平消息类，属于wavezone.wave子模块。
 * 用于封装波形类型和通道信息，通过RxBus事件总线传递给触发电平显示组件，
 * 实现波形到电平的信息传递。</p>
 * 
 * <h3>核心职责</h3>
 * <ul>
 *   <li><b>电平类型封装</b>：封装电平类型（触发电平、测量电平1-4）</li>
 *   <li><b>通道信息封装</b>：封装当前通道信息（Ch1-Ch4）</li>
 *   <li><b>消息传递</b>：通过RxBus事件总线传递给触发电平显示组件</li>
 *   <li><b>解耦通信</b>：实现波形管理和触发电平显示之间的解耦</li>
 *   <li><b>数据传输对象</b>：作为DTO（Data Transfer Object）传递数据</li>
 * </ul>
 * 
 * <h3>波形到电平消息传递流程说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              波形到电平消息传递流程                             │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  Step 1: 用户操作                                              │
 * │    └─ 用户拖动波形或选择通道                                   │
 * │    └─ WaveManage_YT捕获用户操作                               │
 * │                                                               │
 * │  Step 2: 创建消息                                              │
 * │    └─ WaveManage_YT创建MsgWaveToLevel实例                     │
 * │    └─ 设置levelType（电平类型）                                │
 * │    └─ 设置curCh（当前通道）                                    │
 * │                                                               │
 * │  Step 3: 发送事件                                              │
 * │    └─ RxBus.getInstance().post(RxEnum.WAVE_TO_LEVEL, msg)     │
 * │    └─ 通过RxBus事件总线发送消息                                │
 * │                                                               │
 * │  Step 4: 接收事件                                              │
 * │    └─ TriggerVoltageLine订阅RxEnum.WAVE_TO_LEVEL事件          │
 * │    └─ DiscreetVoltageLine订阅RxEnum.WAVE_TO_LEVEL事件         │
 * │    └─ 触发电平显示组件接收消息                                 │
 * │                                                               │
 * │  Step 5: 处理消息                                              │
 * │    └─ 根据levelType判断电平类型                                │
 * │    └─ 根据curCh判断通道                                        │
 * │    └─ 更新触发电平位置                                         │
 * │    └─ 更新电平值显示                                           │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>电平类型说明</h3>
 * <table border="1">
 *   <tr><th>电平类型</th><th>常量值</th><th>说明</th><th>使用场景</th></tr>
 *   <tr><td>LEVELTYPE_TRIGGER</td><td>0</td><td>触发电平</td><td>触发系统，显示触发电压线</td></tr>
 *   <tr><td>LEVELTYPE_VALUE1</td><td>1</td><td>电平值1</td><td>测量系统，显示测量电平线</td></tr>
 *   <tr><td>LEVELTYPE_VALUE2</td><td>2</td><td>电平值2</td><td>测量系统，显示测量电平线</td></tr>
 *   <tr><td>LEVELTYPE_VALUE3</td><td>3</td><td>电平值3</td><td>测量系统，显示测量电平线</td></tr>
 *   <tr><td>LEVELTYPE_VALUE4</td><td>4</td><td>电平值4</td><td>测量系统，显示测量电平线</td></tr>
 * </table>
 * 
 * <h3>通道编号说明</h3>
 * <table border="1">
 *   <tr><th>通道编号</th><th>常量值</th><th>说明</th></tr>
 *   <tr><td>TChan.Ch1</td><td>0</td><td>通道1</td></tr>
 *   <tr><td>TChan.Ch2</td><td>1</td><td>通道2</td></tr>
 *   <tr><td>TChan.Ch3</td><td>2</td><td>通道3</td></tr>
 *   <tr><td>TChan.Ch4</td><td>3</td><td>通道4</td></tr>
 * </table>
 * 
 * <h3>使用场景</h3>
 * <ul>
 *   <li><b>触发电平调整</b>：用户拖动触发电压线，传递触发电平类型和通道信息</li>
 *   <li><b>测量电平调整</b>：用户拖动测量电平线，传递测量电平类型和通道信息</li>
 *   <li><b>通道切换</b>：用户选择不同通道，传递通道信息给触发电平组件</li>
 *   <li><b>RxBus事件传递</b>：作为事件消息对象，通过RxBus传递</li>
 *   <li><b>解耦通信</b>：实现波形管理和触发电平显示之间的解耦通信</li>
 * </ul>
 * 
 * <h3>依赖关系</h3>
 * <ul>
 *   <li>{@link com.micsig.tbook.tbookscope.wavezone.wave.WaveManage_YT} - YT模式波形管理类，创建并发送消息</li>
 *   <li>{@link com.micsig.tbook.tbookscope.wavezone.trigger.TriggerVoltageLine} - 触发电压线类，接收消息并更新触发电平</li>
 *   <li>{@link com.micsig.tbook.tbookscope.wavezone.trigger.DiscreetVoltageLine} - 测量电平线类，接收消息并更新测量电平</li>
 *   <li>{@link com.micsig.tbook.tbookscope.rxjava.RxBus} - RxBus事件总线，用于事件传递</li>
 *   <li>{@link com.micsig.tbook.tbookscope.rxjava.RxEnum} - RxEnum枚举，定义事件类型（WAVE_TO_LEVEL）</li>
 *   <li>{@link com.micsig.tbook.ui.wavezone.TChan} - 通道常量类，定义通道编号（Ch1-Ch4）</li>
 * </ul>
 * 
 * <h3>设计模式</h3>
 * <p>本类采用DTO模式（Data Transfer Object Pattern），作为数据传输对象，
 * 用于在不同组件之间传递数据，实现解耦通信。</p>
 * 
 * <h3>DTO模式说明</h3>
 * <pre>
 * ┌─────────────────────────────────────────┐
 * │  DTO模式（Data Transfer Object）         │
 * ├─────────────────────────────────────────┤
 * │                                         │
 * │  定义：                                  │
 * │    - 数据传输对象                        │
 * │    - 用于在不同层之间传递数据            │
 * │    - 只包含数据，不包含业务逻辑          │
 * │                                         │
 * │  特点：                                  │
 * │    - 简单的数据容器                      │
 * │    - 只有getter/setter方法               │
 * │    - 可序列化                            │
 * │    - 没有业务行为                        │
 * │                                         │
 * │  优点：                                  │
 * │    - 减少网络传输                        │
 * │    - 提高性能                            │
 * │    - 解耦组件                            │
 * │    - 易于维护                            │
 * │                                         │
 * │  本类应用：                              │
 * │    - 封装电平类型和通道信息              │
 * │    - 通过RxBus传递给触发电平组件         │
 * │    - 实现波形管理和触发电平显示的解耦    │
 * │                                         │
 * └─────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>注意事项</h3>
 * <ul>
 *   <li><b>电平类型范围</b>：levelType取值范围为0-4，对应5种电平类型</li>
 *   <li><b>通道编号范围</b>：curCh取值范围为0-3，对应Ch1-Ch4</li>
 *   <li><b>不可变对象</b>：建议在创建后不要修改属性值，保持不可变性</li>
 *   <li><b>线程安全</b>：本类不是线程安全的，在多线程环境下需要外部同步</li>
 *   <li><b>序列化</b>：如果需要跨进程传递，需要实现Serializable或Parcelable接口</li>
 * </ul>
 * 
 * @see com.micsig.tbook.tbookscope.wavezone.wave.WaveManage_YT
 * @see com.micsig.tbook.tbookscope.wavezone.trigger.TriggerVoltageLine
 * @see com.micsig.tbook.tbookscope.wavezone.trigger.DiscreetVoltageLine
 * @see com.micsig.tbook.tbookscope.rxjava.RxBus
 * @see com.micsig.tbook.tbookscope.rxjava.RxEnum
 * @see com.micsig.tbook.ui.wavezone.TChan
 * @author Micsig智能示波器团队
 * @version 2.0
 * @since 1.0
 */
public class MsgWaveToLevel {   // 波形到电平消息类：用于封装波形类型和通道信息，通过RxBus事件总线传递

    /**
     * 电平类型常量：触发电平
     * 
     * <p>触发电平类型，用于触发系统，显示触发电压线。</p>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>用户拖动触发电压线时使用</li>
     *   <li>触发系统需要更新触发电平位置时使用</li>
     *   <li>TriggerVoltageLine接收消息时判断电平类型</li>
     * </ul>
     * 
     * <h4>常量值</h4>
     * <table border="1">
     *   <tr><th>常量名</th><th>值</th><th>说明</th></tr>
     *   <tr><td>LEVELTYPE_TRIGGER</td><td>0</td><td>触发电平</td></tr>
     * </table>
     */
    public static final int LEVELTYPE_TRIGGER = 0;   // 电平类型常量：触发电平（值为0），用于触发系统
    
    /**
     * 电平类型常量：电平值1
     * 
     * <p>电平值1类型，用于测量系统，显示测量电平线。</p>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>用户拖动测量电平线1时使用</li>
     *   <li>测量系统需要更新测量电平位置时使用</li>
     *   <li>DiscreetVoltageLine接收消息时判断电平类型</li>
     * </ul>
     * 
     * <h4>常量值</h4>
     * <table border="1">
     *   <tr><th>常量名</th><th>值</th><th>说明</th></tr>
     *   <tr><td>LEVELTYPE_VALUE1</td><td>1</td><td>电平值1</td></tr>
     * </table>
     */
    public static final int LEVELTYPE_VALUE1 = 1;   // 电平类型常量：电平值1（值为1），用于测量系统
    
    /**
     * 电平类型常量：电平值2
     * 
     * <p>电平值2类型，用于测量系统，显示测量电平线。</p>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>用户拖动测量电平线2时使用</li>
     *   <li>测量系统需要更新测量电平位置时使用</li>
     *   <li>DiscreetVoltageLine接收消息时判断电平类型</li>
     * </ul>
     * 
     * <h4>常量值</h4>
     * <table border="1">
     *   <tr><th>常量名</th><th>值</th><th>说明</th></tr>
     *   <tr><td>LEVELTYPE_VALUE2</td><td>2</td><td>电平值2</td></tr>
     * </table>
     */
    public static final int LEVELTYPE_VALUE2 = 2;   // 电平类型常量：电平值2（值为2），用于测量系统
    
    /**
     * 电平类型常量：电平值3
     * 
     * <p>电平值3类型，用于测量系统，显示测量电平线。</p>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>用户拖动测量电平线3时使用</li>
     *   <li>测量系统需要更新测量电平位置时使用</li>
     *   <li>DiscreetVoltageLine接收消息时判断电平类型</li>
     * </ul>
     * 
     * <h4>常量值</h4>
     * <table border="1">
     *   <tr><th>常量名</th><th>值</th><th>说明</th></tr>
     *   <tr><td>LEVELTYPE_VALUE3</td><td>3</td><td>电平值3</td></tr>
     * </table>
     */
    public static final int LEVELTYPE_VALUE3 = 3;   // 电平类型常量：电平值3（值为3），用于测量系统
    
    /**
     * 电平类型常量：电平值4
     * 
     * <p>电平值4类型，用于测量系统，显示测量电平线。</p>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>用户拖动测量电平线4时使用</li>
     *   <li>测量系统需要更新测量电平位置时使用</li>
     *   <li>DiscreetVoltageLine接收消息时判断电平类型</li>
     * </ul>
     * 
     * <h4>常量值</h4>
     * <table border="1">
     *   <tr><th>常量名</th><th>值</th><th>说明</th></tr>
     *   <tr><td>LEVELTYPE_VALUE4</td><td>4</td><td>电平值4</td></tr>
     * </table>
     */
    public static final int LEVELTYPE_VALUE4 = 4;   // 电平类型常量：电平值4（值为4），用于测量系统
    
    /**
     * 电平类型属性
     * 
     * <p>电平类型，取值范围为LEVELTYPE_TRIGGER（0）到LEVELTYPE_VALUE4（4），
     * 对应5种电平类型：触发电平、电平值1-4。</p>
     * 
     * <h4>取值范围</h4>
     * <table border="1">
     *   <tr><th>取值</th><th>常量</th><th>说明</th></tr>
     *   <tr><td>0</td><td>LEVELTYPE_TRIGGER</td><td>触发电平</td></tr>
     *   <tr><td>1</td><td>LEVELTYPE_VALUE1</td><td>电平值1</td></tr>
     *   <tr><td>2</td><td>LEVELTYPE_VALUE2</td><td>电平值2</td></tr>
     *   <tr><td>3</td><td>LEVELTYPE_VALUE3</td><td>电平值3</td></tr>
     *   <tr><td>4</td><td>LEVELTYPE_VALUE4</td><td>电平值4</td></tr>
     * </table>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>WaveManage_YT创建消息时设置电平类型</li>
     *   <li>TriggerVoltageLine接收消息时判断电平类型</li>
     *   <li>DiscreetVoltageLine接收消息时判断电平类型</li>
     * </ul>
     */
    /**
     * 值为：LEVELTYPE_TRIGGER、LEVELTYPE_VALUE1-4
     */
    private int levelType;   // 电平类型属性：存储电平类型（值为LEVELTYPE_TRIGGER或LEVELTYPE_VALUE1-4）
    
    /**
     * 当前通道属性
     * 
     * <p>当前通道，取值范围为TChan.Ch1（0）到TChan.Ch4（3），
     * 对应4个通道：Ch1-Ch4。</p>
     * 
     * <h4>取值范围</h4>
     * <table border="1">
     *   <tr><th>取值</th><th>常量</th><th>说明</th></tr>
     *   <tr><td>0</td><td>TChan.Ch1</td><td>通道1</td></tr>
     *   <tr><td>1</td><td>TChan.Ch2</td><td>通道2</td></tr>
     *   <tr><td>2</td><td>TChan.Ch3</td><td>通道3</td></tr>
     *   <tr><td>3</td><td>TChan.Ch4</td><td>通道4</td></tr>
     * </table>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>WaveManage_YT创建消息时设置当前通道</li>
     *   <li>TriggerVoltageLine接收消息时判断通道</li>
     *   <li>DiscreetVoltageLine接收消息时判断通道</li>
     * </ul>
     */
    /**
     * 值为：IWave.Ch1 - IWave.Ch4
     */
    private int curCh;   // 当前通道属性：存储当前通道（值为TChan.Ch1-Ch4）

    /**
     * 构造函数 - 创建波形到电平消息实例
     * 
     * <p>构造函数，创建波形到电平消息实例，设置电平类型和当前通道。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>levelType</td><td>int</td><td>电平类型，取值范围为0-4（LEVELTYPE_TRIGGER到LEVELTYPE_VALUE4）</td></tr>
     *   <tr><td>curCh</td><td>int</td><td>当前通道，取值范围为0-3（TChan.Ch1到TChan.Ch4）</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>WaveManage_YT创建消息时调用</li>
     *   <li>用户拖动波形或选择通道时调用</li>
     *   <li>需要传递波形到电平信息时调用</li>
     * </ul>
     * 
     * <h4>使用示例</h4>
     * <pre>
     * // 创建触发电平消息（通道1）
     * MsgWaveToLevel msg = new MsgWaveToLevel(MsgWaveToLevel.LEVELTYPE_TRIGGER, TChan.Ch1);
     * 
     * // 创建测量电平1消息（通道2）
     * MsgWaveToLevel msg = new MsgWaveToLevel(MsgWaveToLevel.LEVELTYPE_VALUE1, TChan.Ch2);
     * </pre>
     * 
     * <h4>注意事项</h4>
     * <ul>
     *   <li><b>参数范围</b>：levelType取值范围为0-4，curCh取值范围为0-3</li>
     *   <li><b>不可变性</b>：建议创建后不要修改属性值，保持不可变性</li>
     * </ul>
     * 
     * @param levelType 电平类型，取值范围为0-4（LEVELTYPE_TRIGGER到LEVELTYPE_VALUE4）
     * @param curCh 当前通道，取值范围为0-3（TChan.Ch1到TChan.Ch4）
     */
    public MsgWaveToLevel(int levelType, int curCh) {   // 构造函数：创建波形到电平消息实例，接收电平类型和当前通道参数
        this.levelType = levelType;   // 设置电平类型：将参数levelType赋值给属性levelType
        this.curCh = curCh;   // 设置当前通道：将参数curCh赋值给属性curCh
    }   // 构造函数结束

    /**
     * 获取电平类型
     * 
     * <p>获取电平类型，返回值为LEVELTYPE_TRIGGER（0）到LEVELTYPE_VALUE4（4），
     * 对应5种电平类型。</p>
     * 
     * <h4>返回值说明</h4>
     * <table border="1">
     *   <tr><th>返回值</th><th>常量</th><th>说明</th></tr>
     *   <tr><td>0</td><td>LEVELTYPE_TRIGGER</td><td>触发电平</td></tr>
     *   <tr><td>1</td><td>LEVELTYPE_VALUE1</td><td>电平值1</td></tr>
     *   <tr><td>2</td><td>LEVELTYPE_VALUE2</td><td>电平值2</td></tr>
     *   <tr><td>3</td><td>LEVELTYPE_VALUE3</td><td>电平值3</td></tr>
     *   <tr><td>4</td><td>LEVELTYPE_VALUE4</td><td>电平值4</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>TriggerVoltageLine接收消息后调用，判断电平类型</li>
     *   <li>DiscreetVoltageLine接收消息后调用，判断电平类型</li>
     *   <li>需要获取电平类型信息时调用</li>
     * </ul>
     * 
     * <h4>使用示例</h4>
     * <pre>
     * MsgWaveToLevel msg = new MsgWaveToLevel(MsgWaveToLevel.LEVELTYPE_TRIGGER, TChan.Ch1);
     * int levelType = msg.getLevelType(); // 返回0（LEVELTYPE_TRIGGER）
     * </pre>
     * 
     * @return 电平类型，取值范围为0-4（LEVELTYPE_TRIGGER到LEVELTYPE_VALUE4）
     */
    public int getLevelType() {   // 方法：获取电平类型，返回电平类型属性值
        return levelType;   // 返回电平类型：返回属性levelType的值
    }   // getLevelType方法结束

    /**
     * 设置电平类型
     * 
     * <p>设置电平类型，参数值为LEVELTYPE_TRIGGER（0）到LEVELTYPE_VALUE4（4），
     * 对应5种电平类型。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>levelType</td><td>int</td><td>电平类型，取值范围为0-4（LEVELTYPE_TRIGGER到LEVELTYPE_VALUE4）</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>需要修改电平类型时调用</li>
     *   <li>不建议在创建后修改，保持不可变性</li>
     * </ul>
     * 
     * <h4>使用示例</h4>
     * <pre>
     * MsgWaveToLevel msg = new MsgWaveToLevel(MsgWaveToLevel.LEVELTYPE_TRIGGER, TChan.Ch1);
     * msg.setLevelType(MsgWaveToLevel.LEVELTYPE_VALUE1); // 修改为电平值1
     * </pre>
     * 
     * <h4>注意事项</h4>
     * <ul>
     *   <li><b>参数范围</b>：levelType取值范围为0-4</li>
     *   <li><b>不可变性</b>：不建议修改，保持不可变性</li>
     * </ul>
     * 
     * @param levelType 电平类型，取值范围为0-4（LEVELTYPE_TRIGGER到LEVELTYPE_VALUE4）
     */
    public void setLevelType(int levelType) {   // 方法：设置电平类型，接收电平类型参数
        this.levelType = levelType;   // 设置电平类型：将参数levelType赋值给属性levelType
    }   // setLevelType方法结束

    /**
     * 获取当前通道
     * 
     * <p>获取当前通道，返回值为TChan.Ch1（0）到TChan.Ch4（3），
     * 对应4个通道。</p>
     * 
     * <h4>返回值说明</h4>
     * <table border="1">
     *   <tr><th>返回值</th><th>常量</th><th>说明</th></tr>
     *   <tr><td>0</td><td>TChan.Ch1</td><td>通道1</td></tr>
     *   <tr><td>1</td><td>TChan.Ch2</td><td>通道2</td></tr>
     *   <tr><td>2</td><td>TChan.Ch3</td><td>通道3</td></tr>
     *   <tr><td>3</td><td>TChan.Ch4</td><td>通道4</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>TriggerVoltageLine接收消息后调用，判断通道</li>
     *   <li>DiscreetVoltageLine接收消息后调用，判断通道</li>
     *   <li>需要获取通道信息时调用</li>
     * </ul>
     * 
     * <h4>使用示例</h4>
     * <pre>
     * MsgWaveToLevel msg = new MsgWaveToLevel(MsgWaveToLevel.LEVELTYPE_TRIGGER, TChan.Ch1);
     * int curCh = msg.getCurCh(); // 返回0（TChan.Ch1）
     * </pre>
     * 
     * @return 当前通道，取值范围为0-3（TChan.Ch1到TChan.Ch4）
     */
    public int getCurCh() {   // 方法：获取当前通道，返回当前通道属性值
        return curCh;   // 返回当前通道：返回属性curCh的值
    }   // getCurCh方法结束

    /**
     * 设置当前通道
     * 
     * <p>设置当前通道，参数值为TChan.Ch1（0）到TChan.Ch4（3），
     * 对应4个通道。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>curCh</td><td>int</td><td>当前通道，取值范围为0-3（TChan.Ch1到TChan.Ch4）</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>需要修改通道时调用</li>
     *   <li>不建议在创建后修改，保持不可变性</li>
     * </ul>
     * 
     * <h4>使用示例</h4>
     * <pre>
     * MsgWaveToLevel msg = new MsgWaveToLevel(MsgWaveToLevel.LEVELTYPE_TRIGGER, TChan.Ch1);
     * msg.setCurCh(TChan.Ch2); // 修改为通道2
     * </pre>
     * 
     * <h4>注意事项</h4>
     * <ul>
     *   <li><b>参数范围</b>：curCh取值范围为0-3</li>
     *   <li><b>不可变性</b>：不建议修改，保持不可变性</li>
     * </ul>
     * 
     * @param curCh 当前通道，取值范围为0-3（TChan.Ch1到TChan.Ch4）
     */
    public void setCurCh(int curCh) {   // 方法：设置当前通道，接收当前通道参数
        this.curCh = curCh;   // 设置当前通道：将参数curCh赋值给属性curCh
    }   // setCurCh方法结束

    /**
     * 获取字符串表示
     * 
     * <p>获取消息对象的字符串表示，用于调试和日志输出。</p>
     * 
     * <h4>返回格式</h4>
     * <pre>
     * MsgWaveToLevel{levelType=0, curCh=1}
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>调试时调用，查看消息内容</li>
     *   <li>日志输出时调用，记录消息信息</li>
     *   <li>需要查看消息对象状态时调用</li>
     * </ul>
     * 
     * <h4>使用示例</h4>
     * <pre>
     * MsgWaveToLevel msg = new MsgWaveToLevel(MsgWaveToLevel.LEVELTYPE_TRIGGER, TChan.Ch1);
     * String str = msg.toString(); // 返回"MsgWaveToLevel{levelType=0, curCh=0}"
     * Logger.d(TAG, "Message: " + str);
     * </pre>
     * 
     * @return 字符串表示，格式为"MsgWaveToLevel{levelType=X, curCh=Y}"
     */
    @Override   // 重写注解：重写Object类的toString方法
    public String toString() {   // 方法：获取字符串表示，返回消息对象的字符串表示
        return "MsgWaveToLevel{" +   // 返回字符串：开始构建字符串，包含类名
                "levelType=" + levelType +   // 添加电平类型：将levelType属性值添加到字符串
                ", curCh=" + curCh +   // 添加当前通道：将curCh属性值添加到字符串
                '}';   // 结束字符串：添加结束括号
    }   // toString方法结束
}   // MsgWaveToLevel类结束