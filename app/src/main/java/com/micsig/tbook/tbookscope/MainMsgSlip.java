package com.micsig.tbook.tbookscope;   // 示波器主应用包，包含滑动菜单消息类

/**
 * 滑动菜单消息数据类 - 封装滑动菜单的打开/关闭状态
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    滑动菜单消息架构                              │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │  ┌─────────────────┐                                           │
 * │  │   用户操作      │                                           │
 * │  │  (双击通道)      │                                           │
 * │  │                 │                                           │
 * │  │  onSmallDouble  │                                           │
 * │  │    Click()      │                                           │
 * │  └────────┬────────┘                                           │
 * │           │                                                     │
 * │           │ 创建MainMsgSlip                                     │
 * │           │ new MainMsgSlip(                                    │
 * │           │   MainViewGroup.RIGHTSLIP_CH1,                      │
 * │           │   true)                                             │
 * │           ▼                                                     │
 * │  ┌─────────────────────────────────────────────┐               │
 * │  │           MainMsgSlip                        │               │
 * │  │         (滑动菜单消息)                        │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  数据属性                            │   │               │
 * │  │  │  - slip: 滑动菜单类型                │   │               │
 * │  │  │  - open: 是否打开                    │   │               │
 * │  │  │  - isNormal: 是否常规布局            │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  └─────────────────────────────────────────────┘               │
 * │                       │                                       │
 * │                       │ RxBus.getInstance().post(              │
 * │                       │   RxEnum.MAIN_SLIP_FROM_OTHER,         │
 * │                       │   mainMsgSlip)                         │
 * │                       ▼                                       │
 * │           ┌─────────────────────┐                             │
 * │           │  RxBus订阅者         │                             │
 * │           │  (多个组件)          │                             │
 * │           │                     │                             │
 * │           │  - MainChannel      │                             │
 * │           │    VerticalScale    │                             │
 * │           │  - ExternalKeys     │                             │
 * │           │    Protocol         │                             │
 * │           │  - MainHolderRight  │                             │
 * │           │    Channels         │                             │
 * │           │  - MainViewGroup    │                             │
 * │           │                     │                             │
 * │           │  - 收到滑动消息      │                             │
 * │           │  - 判断slip类型      │                             │
 * │           │  - 判断open状态      │                             │
 * │           │  - 执行相应操作      │                             │
 * │           └─────────────────────┘                             │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * <h3>模块定位</h3>
 * <p>本类是滑动菜单系统的数据传输对象（DTO），用于在RxBus事件总线中传递滑动菜单的打开/关闭状态。
 * 封装了slip（滑动菜单类型）、open（是否打开）和isNormal（是否常规布局）三个关键参数，
 * 实现滑动菜单状态变化的解耦通信。</p>
 * 
 * <h3>核心职责</h3>
 * <ul>
 *   <li><b>数据封装</b>：封装滑动菜单的三个关键参数</li>
 *   <li><b>消息传递</b>：通过RxBus通知订阅者滑动菜单状态变化</li>
 *   <li><b>解耦通信</b>：实现滑动菜单状态变化的组件间解耦</li>
 * </ul>
 * 
 * <h3>滑动菜单类型说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              滑动菜单类型详解                                  │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  Slip注解定义：                                                │
 * │    @Retention(RetentionPolicy.SOURCE)                         │
 * │    public @interface Slip {                                   │
 * │    }                                                           │
 * │    - 编译时注解，用于类型检查                                  │
 * │    - 确保slip参数使用正确的常量值                              │
 * │                                                               │
 * │  滑动菜单类型常量（MainViewGroup定义）：                        │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  RIGHTSLIP_CH1                                │           │
 * │    │  - 通道1滑动菜单                              │           │
 * │    │  - 包含通道1的详细设置                       │           │
 * │    │                                             │           │
 * │    │  RIGHTSLIP_CH2                                │           │
 * │    │  - 通道2滑动菜单                              │           │
 * │    │  - 包含通道2的详细设置                       │           │
 * │    │                                             │           │
 * │    │  RIGHTSLIP_CH3                                │           │
 * │    │  - 通道3滑动菜单                              │           │
 * │    │  - 包含通道3的详细设置                       │           │
 * │    │                                             │           │
 * │    │  RIGHTSLIP_CH4                                │           │
 * │    │  - 通道4滑动菜单                              │           │
 * │    │  - 包含通道4的详细设置                       │           │
 * │    │                                             │           │
 * │    │  其他滑动菜单类型...                          │           │
 * │    │  - BOTTOMSLIP_QUICK                           │           │
 * │    │  - BOTTOMSLIP_SERIALBUSTXT                    │           │
 * │    │  - LEFTSLIP                                   │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                                                               │
 * │  滑动菜单功能：                                                │
 * │    - 通道设置：垂直刻度、耦合方式、探头衰减等                  │
 * │    - 快捷菜单：常用功能的快捷入口                              │
 * │    - 串行解码：串行总线解码配置                                │
 * │    - 左侧菜单：触发、测量、存储等功能                          │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>滑动菜单使用流程</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              滑动菜单使用流程                                  │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  Step 1: 用户触发滑动菜单                                      │
 * │    └─ 双击通道主控件（MainRightLayoutItemChannelMaster）       │
 * │    └─ 或点击其他触发区域                                        │
 * │                                                               │
 * │  Step 2: 创建MainMsgSlip消息                                  │
 * │    └─ new MainMsgSlip(                                        │
 * │         MainViewGroup.RIGHTSLIP_CH1,                          │
 * │         true,                                                 │
 * │         isNormal)                                             │
 * │      - slip: 滑动菜单类型                                      │
 * │      - open: 是否打开（true=打开，false=关闭）                 │
 * │      - isNormal: 是否常规布局（可选参数，默认true）            │
 * │                                                               │
 * │  Step 3: 发送RxBus事件                                        │
 * │    └─ RxBus.getInstance().post(                               │
 * │         RxEnum.MAIN_SLIP_FROM_OTHER,                          │
 * │         mainMsgSlip)                                          │
 * │      - 事件类型：MAIN_SLIP_FROM_OTHER                         │
 * │      - 消息对象：MainMsgSlip                                  │
 * │                                                               │
 * │  Step 4: 接收事件                                             │
 * │    └─ RxBus订阅者收到事件                                      │
 * │      - MainViewGroup: 控制滑动菜单显示/隐藏                   │
 * │      - MainChannelVerticalScale: 更新垂直刻度显示             │
 * │      - MainHolderRightChannels: 更新通道状态                  │
 * │      - ExternalKeysProtocol: 更新外部按键状态                 │
 * │                                                               │
 * │  Step 5: 处理滑动菜单状态                                      │
 * │    └─ 订阅者根据消息执行相应操作：                              │
 * │      - 判断slip类型确定滑动菜单类型                            │
 * │      - 判断open状态确定打开/关闭                               │
 * │      - 判断isNormal确定布局类型                                │
 * │      - 更新UI显示                                              │
 * │      - 同步组件状态                                            │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>订阅者处理示例</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              MainHolderRightChannels处理示例                   │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  // 订阅滑动菜单事件                                           │
 * │  RxBus.getInstance().getObservable(RxEnum.MAIN_SLIP_FROM_OTHER)│
 * │      .subscribe(consumerMainSlipToOther);                     │
 * │                                                               │
 * │  // 定义消费者                                                 │
 * │  private Consumer&lt;MainMsgSlip&gt; consumerMainSlipToOther =       │
 * │      new Consumer&lt;MainMsgSlip&gt;() {                              │
 * │    @Override                                                  │
 * │    public void accept(MainMsgSlip mainMsgSlip)                │
 * │        throws Exception {                                     │
 * │      if (mainMsgSlip.isOpen()) {                              │
 * │        int chSelect = CacheUtil.get().getInt(                 │
 * │            CacheUtil.MAIN_CENTER_CHANNELS_SELECT);            │
 * │        switch (mainMsgSlip.getSlip()) {                       │
 * │          case MainViewGroup.RIGHTSLIP_CH1: {                  │
 * │            setRightMasterSmall(TChan.Ch1);                    │
 * │            // 更新通道1状态                                    │
 * │            break;                                             │
 * │          }                                                     │
 * │          case MainViewGroup.RIGHTSLIP_CH2: {                  │
 * │            setRightMasterSmall(TChan.Ch2);                    │
 * │            // 更新通道2状态                                    │
 * │            break;                                             │
 * │          }                                                     │
 * │          // 其他通道处理...                                    │
 * │        }                                                       │
 * │      }                                                         │
 * │    }                                                           │
 * │  };                                                            │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>使用场景</h3>
 * <ul>
 *   <li><b>通道滑动菜单</b>：双击通道主控件打开通道设置菜单</li>
 *   <li><b>快捷菜单</b>：打开底部快捷功能菜单</li>
 *   <li><b>串行解码菜单</b>：打开串行总线解码配置菜单</li>
 *   <li><b>左侧菜单</b>：打开触发、测量、存储等功能菜单</li>
 *   <li><b>关闭菜单</b>：关闭当前打开的滑动菜单</li>
 * </ul>
 * 
 * <h3>依赖关系</h3>
 * <ul>
 *   <li>{@link MainViewGroup} - 主视图组，定义Slip注解和滑动菜单类型常量</li>
 *   <li>{@link MainViewGroup.Slip} - Slip注解，用于类型检查</li>
 *   <li>{@link com.micsig.tbook.tbookscope.rxjava.RxBus} - RxBus事件总线，传递消息</li>
 *   <li>{@link com.micsig.tbook.tbookscope.rxjava.RxEnum#MAIN_SLIP_FROM_OTHER} - 滑动菜单事件类型</li>
 *   <li>{@link com.micsig.tbook.tbookscope.main.dialog.MainChannelVerticalScale} - 通道垂直刻度对话框，订阅滑动菜单事件</li>
 *   <li>{@link com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysProtocol} - 外部按键协议，订阅滑动菜单事件</li>
 *   <li>{@link com.micsig.tbook.tbookscope.main.mainright.MainHolderRightChannels} - 通道右侧持有者，订阅滑动菜单事件</li>
 * </ul>
 * 
 * <h3>设计模式</h3>
 * <p>本类采用DTO（Data Transfer Object）设计模式，作为消息载体在组件间传递数据。
 * 封装了滑动菜单的三个关键参数，提供完整的getter/setter方法。
 * 配合RxBus事件总线，实现观察者模式的解耦通信。</p>
 * 
 * <h3>注意事项</h3>
 * <ul>
 *   <li><b>Slip注解</b>：slip参数必须使用MainViewGroup定义的常量值</li>
 *   <li><b>可变参数</b>：isNormal为可选参数，默认值为true</li>
 *   <li><b>状态判断</b>：订阅者需要判断open状态确定打开/关闭</li>
 *   <li><b>类型判断</b>：订阅者需要判断slip类型确定滑动菜单类型</li>
 * </ul>
 * 
 * @see MainViewGroup
 * @see MainViewGroup.Slip
 * @see com.micsig.tbook.tbookscope.rxjava.RxBus
 * @see com.micsig.tbook.tbookscope.rxjava.RxEnum#MAIN_SLIP_FROM_OTHER
 * @see com.micsig.tbook.tbookscope.main.dialog.MainChannelVerticalScale
 * @see com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysProtocol
 * @see com.micsig.tbook.tbookscope.main.mainright.MainHolderRightChannels
 * @author Micsig智能示波器团队
 * @version 2.0
 * @since 1.0
 */
public class MainMsgSlip {   // 滑动菜单消息数据类：封装滑动菜单的打开/关闭状态
    
    /**
     * 滑动菜单类型 - 使用@Slip注解确保类型正确
     * 
     * <p>滑动菜单类型，标识具体是哪个滑动菜单。
     * 使用@MainViewGroup.Slip注解进行类型检查，确保使用正确的常量值。</p>
     * 
     * <h4>取值范围</h4>
     * <table border="1">
     *   <tr><th>常量</th><th>说明</th></tr>
     *   <tr><td>RIGHTSLIP_CH1</td><td>通道1滑动菜单</td></tr>
     *   <tr><td>RIGHTSLIP_CH2</td><td>通道2滑动菜单</td></tr>
     *   <tr><td>RIGHTSLIP_CH3</td><td>通道3滑动菜单</td></tr>
     *   <tr><td>RIGHTSLIP_CH4</td><td>通道4滑动菜单</td></tr>
     *   <tr><td>BOTTOMSLIP_QUICK</td><td>底部快捷菜单</td></tr>
     *   <tr><td>BOTTOMSLIP_SERIALBUSTXT</td><td>串行解码菜单</td></tr>
     *   <tr><td>LEFTSLIP</td><td>左侧菜单</td></tr>
     * </table>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>通道设置：RIGHTSLIP_CH1/CH2/CH3/CH4</li>
     *   <li>快捷功能：BOTTOMSLIP_QUICK</li>
     *   <li>串行解码：BOTTOMSLIP_SERIALBUSTXT</li>
     *   <li>触发/测量/存储：LEFTSLIP</li>
     * </ul>
     */
    @MainViewGroup.Slip   // Slip注解：确保slip参数使用正确的常量值（编译时检查）
    private int slip;   // 滑动菜单类型：标识具体是哪个滑动菜单
    
    /**
     * 是否打开 - 标识滑动菜单的打开/关闭状态
     * 
     * <p>标识滑动菜单是否打开。
     * true表示打开滑动菜单，false表示关闭滑动菜单。</p>
     * 
     * <h4>取值范围</h4>
     * <ul>
     *   <li><b>true</b>: 打开滑动菜单</li>
     *   <li><b>false</b>: 关闭滑动菜单</li>
     * </ul>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>打开菜单：用户双击通道主控件时设置为true</li>
     *   <li>关闭菜单：用户点击关闭按钮或切换菜单时设置为false</li>
     * </ul>
     */
    private boolean open;   // 是否打开：true=打开，false=关闭
    
    /**
     * 是否显示常规布局 - 标识滑动菜单的布局类型
     * 
     * <p>标识滑动菜单是否显示常规布局。
     * true表示显示常规布局，false表示显示特殊布局。
     * 默认值为true。</p>
     * 
     * <h4>取值范围</h4>
     * <ul>
     *   <li><b>true</b>: 显示常规布局（默认值）</li>
     *   <li><b>false</b>: 显示特殊布局</li>
     * </ul>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>常规布局：显示标准的滑动菜单布局</li>
     *   <li>特殊布局：显示定制化的滑动菜单布局</li>
     * </ul>
     */
    private boolean isNormal;   // 是否显示常规布局：true=常规布局，false=特殊布局，默认true

    /**
     * 构造函数 - 创建滑动菜单消息
     * 
     * <p>创建MainMsgSlip实例，封装滑动菜单的三个关键参数。
     * isNormal为可选参数，使用可变参数实现，默认值为true。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>slip</td><td>int (@Slip)</td><td>滑动菜单类型，必须使用MainViewGroup定义的常量</td></tr>
     *   <tr><td>open</td><td>boolean</td><td>是否打开，true=打开，false=关闭</td></tr>
     *   <tr><td>isNormal</td><td>boolean...</td><td>是否常规布局（可选），默认true</td></tr>
     * </table>
     * 
     * <h4>可变参数处理</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  isNormal可变参数处理                   │
     * ├─────────────────────────────────────────┤
     * │                                         │
     * │  if (isNormal.length > 0) {             │
     * │    // 有传入参数                        │
     * │    this.isNormal = isNormal[0];         │
     * │  } else {                               │
     * │    // 无传入参数                        │
     * │    this.isNormal = true; // 默认值      │
     * │  }                                       │
     * │                                         │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>构造示例</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  构造示例                               │
     * ├─────────────────────────────────────────┤
     * │                                         │
     * │  // 打开通道1滑动菜单（常规布局）        │
     * │  new MainMsgSlip(                       │
     * │    MainViewGroup.RIGHTSLIP_CH1,         │
     * │    true                                 │
     * │  );                                     │
     * │                                         │
     * │  // 打开通道2滑动菜单（特殊布局）        │
     * │  new MainMsgSlip(                       │
     * │    MainViewGroup.RIGHTSLIP_CH2,         │
     * │    true,                                │
     * │    false                                │
     * │  );                                     │
     * │                                         │
     * │  // 关闭通道1滑动菜单                    │
     * │  new MainMsgSlip(                       │
     * │    MainViewGroup.RIGHTSLIP_CH1,         │
     * │    false                                │
     * │  );                                     │
     * │                                         │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>用户双击通道主控件时创建</li>
     *   <li>用户点击快捷菜单按钮时创建</li>
     *   <li>用户切换滑动菜单时创建</li>
     *   <li>创建后立即通过RxBus发送给订阅者</li>
     * </ul>
     * 
     * @param slip 滑动菜单类型，必须使用MainViewGroup定义的常量
     * @param open 是否打开，true=打开，false=关闭
     * @param isNormal 是否常规布局（可选参数），默认true
     * @see MainViewGroup.Slip
     */
    public MainMsgSlip(@MainViewGroup.Slip int slip, boolean open, boolean... isNormal) {   // 构造函数：创建滑动菜单消息，封装三个关键参数
        this.slip = slip;   // 设置slip属性：传入的滑动菜单类型值
        this.open = open;   // 设置open属性：传入的打开状态值
        
        if (isNormal.length > 0) {   // 判断：isNormal可变参数是否有传入值
            this.isNormal = isNormal[0];   // 有传入值：使用传入的第一个值作为isNormal
        } else {   // isNormal可变参数判断结束，无传入值
            this.isNormal = true;   // 无传入值：使用默认值true
        }   // isNormal可变参数处理结束
    }   // 构造函数结束

    /**
     * 获取滑动菜单类型
     * 
     * <p>返回滑动菜单的类型。</p>
     * 
     * <h4>返回值说明</h4>
     * <p>返回滑动菜单类型，使用@Slip注解标记。</p>
     * 
     * <h4>调用时机</h4>
     * <p>订阅者收到MainMsgSlip消息后调用此方法，判断滑动菜单类型。</p>
     * 
     * @return 滑动菜单类型（@Slip注解标记）
     */
    public int getSlip() {   // 方法：获取滑动菜单类型
        return slip;   // 返回slip属性值
    }   // getSlip方法结束

    /**
     * 设置滑动菜单类型
     * 
     * <p>设置滑动菜单的类型。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>slip</td><td>int (@Slip)</td><td>滑动菜单类型，必须使用MainViewGroup定义的常量</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>动态修改滑动菜单类型时调用</li>
     *   <li>通常在构造时设置，很少需要动态修改</li>
     * </ul>
     * 
     * @param slip 滑动菜单类型，必须使用MainViewGroup定义的常量
     */
    public void setSlip(@MainViewGroup.Slip int slip) {   // 方法：设置滑动菜单类型
        this.slip = slip;   // 设置slip属性值
    }   // setSlip方法结束

    /**
     * 获取是否打开状态
     * 
     * <p>返回滑动菜单是否打开。</p>
     * 
     * <h4>返回值说明</h4>
     * <ul>
     *   <li><b>true</b>: 滑动菜单已打开</li>
     *   <li><b>false</b>: 滑动菜单已关闭</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <p>订阅者收到MainMsgSlip消息后调用此方法，判断滑动菜单是否打开。</p>
     * 
     * @return 是否打开，true=打开，false=关闭
     */
    public boolean isOpen() {   // 方法：获取是否打开状态
        return open;   // 返回open属性值
    }   // isOpen方法结束

    /**
     * 设置是否打开状态
     * 
     * <p>设置滑动菜单是否打开。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>open</td><td>boolean</td><td>是否打开，true=打开，false=关闭</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>动态修改打开状态时调用</li>
     *   <li>通常在构造时设置，很少需要动态修改</li>
     * </ul>
     * 
     * @param open 是否打开，true=打开，false=关闭
     */
    public void setOpen(boolean open) {   // 方法：设置是否打开状态
        this.open = open;   // 设置open属性值
    }   // setOpen方法结束

    /**
     * 获取是否常规布局状态
     * 
     * <p>返回滑动菜单是否显示常规布局。</p>
     * 
     * <h4>返回值说明</h4>
     * <ul>
     *   <li><b>true</b>: 显示常规布局</li>
     *   <li><b>false</b>: 显示特殊布局</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <p>订阅者收到MainMsgSlip消息后调用此方法，判断滑动菜单布局类型。</p>
     * 
     * @return 是否常规布局，true=常规布局，false=特殊布局
     */
    public boolean isNormal() {   // 方法：获取是否常规布局状态
        return isNormal;   // 返回isNormal属性值
    }   // isNormal方法结束

    /**
     * 设置是否常规布局状态
     * 
     * <p>设置滑动菜单是否显示常规布局。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>normal</td><td>boolean</td><td>是否常规布局，true=常规布局，false=特殊布局</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>动态修改布局类型时调用</li>
     *   <li>通常在构造时设置，很少需要动态修改</li>
     * </ul>
     * 
     * @param normal 是否常规布局，true=常规布局，false=特殊布局
     */
    public void setNormal(boolean normal) {   // 方法：设置是否常规布局状态
        isNormal = normal;   // 设置isNormal属性值（注意：属性名是isNormal，参数名是normal）
    }   // setNormal方法结束

    /**
     * 获取字符串表示 - 用于调试和日志输出
     * 
     * <p>返回MainMsgSlip对象的字符串表示，包含三个关键参数。
     * 主要用于调试、日志输出和状态追踪。</p>
     * 
     * <h4>返回格式</h4>
     * <pre>
     * MainMsgSlip{slip=1, open=true, isNormal=true}
     * </pre>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>调试时打印消息状态</li>
     *   <li>日志记录滑动菜单状态</li>
     *   <li>追踪RxBus事件传递</li>
     * </ul>
     * 
     * @return 字符串表示，格式为"MainMsgSlip{slip=值, open=值, isNormal=值}"
     */
    @Override   // 重写Object类的toString方法
    public String toString() {   // 方法：获取字符串表示，用于调试和日志输出
        return "MainMsgSlip{" +   // 返回字符串：类名 + 开括号
                "slip=" + slip +   // 添加slip属性值
                ", open=" + open +   // 添加open属性值
                ", isNormal=" + isNormal +   // 添加isNormal属性值
                '}';   // 添加闭括号
    }   // toString方法结束
}   // MainMsgSlip类结束