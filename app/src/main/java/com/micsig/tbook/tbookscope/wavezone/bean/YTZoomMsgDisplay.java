package com.micsig.tbook.tbookscope.wavezone.bean;   // 波形显示区域Bean包，包含数据传输对象类


/**
 * YT模式缩放显示消息数据Bean类 - 控制YT缩放窗口的显示状态
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    YT缩放显示消息架构                            │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │  ┌─────────────────┐                                           │
 * │  │ WaveZoneDisplay │                                           │
 * │  │     _YT         │                                           │
 * │  │ (YT波形显示)     │                                           │
 * │  └────────┬────────┘                                           │
 * │           │                                                     │
 * │           │ 双指缩放手势                                        │
 * │           │ state判断                                           │
 * │           ▼                                                     │
 * │  ┌─────────────────────────────────────────────┐               │
 * │  │           RxBus                             │               │
 * │  │         (事件总线)                           │               │
 * │  │                                             │               │
 * │  │  post(WAVEZONE_DISPLAY_YTZOOM,              │               │
 * │  │       new YTZoomMsgDisplay(true/false))     │               │
 * │  └─────────────────────────────────────────────┘               │
 * │                       │                                       │
 * │                       ▼                                       │
 * │  ┌─────────────────────────────────────────────┐               │
 * │  │           YTZoomMsgDisplay                  │               │
 * │  │         (缩放显示消息Bean)                   │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  状态属性                            │   │               │
 * │  │  │  - isDisplay: 显示/隐藏标志          │   │               │
 * │  │  │  - isReloadLargeTimeScale: 重加载标志│   │               │
 * │  │  │  - isPlaySound: 播放声音标志         │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  └─────────────────────────────────────────────┘               │
 * │                       │                                       │
 * │                       ▼                                       │
 * │           ┌─────────────────────┐                             │
 * │           │  WaveZoneDisplay    │                             │
 * │           │     _YTZoom         │                             │
 * │           │  (YT缩放窗口)        │                             │
 * │           │  根据isDisplay显示  │                             │
 * │           └─────────────────────┘                             │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * <h3>模块定位</h3>
 * <p>本类是YT模式缩放显示系统的消息数据传输对象（DTO），用于在YT波形显示区域
 * 和YT缩放窗口之间传递显示状态信息。通过RxBus事件总线传递，实现组件间的解耦通信。</p>
 * 
 * <h3>核心职责</h3>
 * <ul>
 *   <li><b>显示控制</b>：控制YT缩放窗口的显示/隐藏状态</li>
 *   <li><b>时基重载</b>：标记是否需要重新加载大时基数据</li>
 *   <li><b>声音控制</b>：控制缩放操作时的声音反馈</li>
 * </ul>
 * 
 * <h3>YT缩放功能说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │                    YT缩放功能详解                              │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  YT缩放窗口是示波器的重要功能，用于放大显示波形细节：           │
 * │                                                               │
 * │  ┌─────────────────────────────────────────────┐             │
 * │  │              主波形窗口                      │             │
 * │  │                                             │             │
 * │  │  ┌───────────────────────────────────────┐ │             │
 * │  │  │  正常波形显示                         │ │             │
 * │  │  │                                       │ │             │
 * │  │  │  ┌───────────────┐                   │ │             │
 * │  │  │  │ 缩放区域      │                   │ │             │
 * │  │  │  │ (选中的区域)  │                   │ │             │
 * │  │  │  └───────────────┘                   │ │             │
 * │  │  │                                       │ │             │
 * │  │  └───────────────────────────────────────┘ │             │
 * │  └─────────────────────────────────────────────┘             │
 * │                                                               │
 * │  ┌─────────────────────────────────────────────┐             │
 * │  │              缩放窗口                        │             │
 * │  │  (WaveZoneDisplay_YTZoom)                   │             │
 * │  │                                             │             │
 * │  │  ┌───────────────────────────────────────┐ │             │
 * │  │  │  放大显示选中区域的波形               │ │             │
 * │  │  │                                       │ │             │
 * │  │  │  时间刻度放大                         │ │             │
 * │  │  │  波形细节清晰可见                     │ │             │
 * │  │  │                                       │ │             │
 * │  │  └───────────────────────────────────────┘ │             │
 * │  └─────────────────────────────────────────────┘             │
 * │                                                               │
 * │  触发方式：                                                    │
 * │    - 双指缩放手势：在主波形窗口上双指捏合/展开                 │
 * │    - state=0: 显示缩放窗口                                    │
 * │    - state=1: 隐藏缩放窗口                                    │
 * │                                                               │
 * │  功能特点：                                                    │
 * │    - 实时缩放：跟随双指手势实时调整缩放比例                    │
 * │    - 时间放大：只放大时间轴，保持电压轴不变                    │
 * │    - 细节观察：用于观察波形的细节部分                          │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>RxBus事件传递流程</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              RxBus事件传递流程                                 │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  Step 1: 用户双指缩放手势                                     │
 * │    └─ 在WaveZoneDisplay_YT上双指操作                          │
 * │                                                               │
 * │  Step 2: 状态判断                                             │
 * │    └─ 根据手势状态判断缩放窗口显示/隐藏                        │
 * │      - state=0: 显示缩放窗口                                  │
 * │      - state=1: 隐藏缩放窗口                                  │
 * │                                                               │
 * │  Step 3: 创建YTZoomMsgDisplay                                 │
 * │    └─ new YTZoomMsgDisplay(true)  // 显示                     │
 * │    └─ new YTZoomMsgDisplay(false) // 隐藏                     │
 * │                                                               │
 * │  Step 4: 发送RxBus事件                                        │
 * │    └─ RxBus.getInstance().post(                               │
 * │         RxEnum.WAVEZONE_DISPLAY_YTZOOM,                       │
 * │         ytZoomMsgDisplay)                                     │
 * │                                                               │
 * │  Step 5: 接收事件                                             │
 * │    └─ WaveZoneDisplay_YTZoom订阅事件                          │
 * │    └─ 根据isDisplay显示/隐藏缩放窗口                          │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>属性详解</h3>
 * <table border="1">
 *   <tr><th>属性</th><th>类型</th><th>默认值</th><th>说明</th></tr>
 *   <tr><td>isDisplay</td><td>boolean</td><td>构造参数</td><td>缩放窗口显示标志</td></tr>
 *   <tr><td>isReloadLargeTimeScale</td><td>boolean</td><td>false</td><td>重新加载大时基标志</td></tr>
 *   <tr><td>isPlaySound</td><td>boolean</td><td>true</td><td>播放声音标志</td></tr>
 * </table>
 * 
 * <h3>使用场景</h3>
 * <ul>
 *   <li><b>双指缩放</b>：用户双指手势触发缩放窗口显示/隐藏</li>
 *   <li><b>时基切换</b>：切换大时基时重新加载缩放数据</li>
 *   <li><b>声音反馈</b>：缩放操作时播放提示声音</li>
 *   <li><b>组件通信</b>：通过RxBus实现组件间解耦通信</li>
 * </ul>
 * 
 * <h3>依赖关系</h3>
 * <ul>
 *   <li>{@link com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_YT} - YT波形显示区域，发送YTZoomMsgDisplay消息</li>
 *   <li>{@link com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_YTZoom} - YT缩放窗口，接收YTZoomMsgDisplay消息</li>
 *   <li>{@link com.micsig.tbook.tbookscope.rxjava.RxBus} - RxBus事件总线，传递消息</li>
 *   <li>{@link com.micsig.tbook.tbookscope.rxjava.RxEnum#WAVEZONE_DISPLAY_YTZOOM} - 事件类型枚举</li>
 * </ul>
 * 
 * <h3>设计模式</h3>
 * <p>本类采用DTO（Data Transfer Object）设计模式，作为消息载体在组件间传递数据。
 * 配合RxBus事件总线，实现观察者模式的解耦通信。</p>
 * 
 * @see com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_YT
 * @see com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_YTZoom
 * @see com.micsig.tbook.tbookscope.rxjava.RxBus
 * @see com.micsig.tbook.tbookscope.rxjava.RxEnum#WAVEZONE_DISPLAY_YTZOOM
 * @author Micsig智能示波器团队
 * @version 2.0
 * @since 1.0
 */
public class YTZoomMsgDisplay  {   // YT模式缩放显示消息数据Bean类：控制YT缩放窗口的显示状态
    
    /**
     * 缩放窗口显示标志
     * 
     * <p>控制YT缩放窗口是否显示。这是主要的状态属性，
     * 通过构造函数初始化，决定缩放窗口的显示/隐藏。</p>
     * 
     * <h4>取值说明</h4>
     * <ul>
     *   <li><b>true</b>: 显示缩放窗口，放大显示选中区域的波形</li>
     *   <li><b>false</b>: 隐藏缩放窗口，恢复正常显示</li>
     * </ul>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>双指缩放手势触发显示（state=0时设置为true）</li>
     *   <li>双指缩放手势触发隐藏（state=1时设置为false）</li>
     *   <li>WaveZoneDisplay_YTZoom根据此值控制窗口可见性</li>
     * </ul>
     * 
     * <h4>设置时机</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  isDisplay设置时机                       │
     * ├─────────────────────────────────────────┤
     * │                                         │
     * │  WaveZoneDisplay_YT中的判断逻辑：       │
     * │                                         │
     * │  if (state == 0) {                      │
     * │    // 显示缩放窗口                      │
     * │    RxBus.post(                          │
     * │      WAVEZONE_DISPLAY_YTZOOM,           │
     * │      new YTZoomMsgDisplay(true)         │
     * │    );                                   │
     * │  } else if (state == 1) {               │
     * │    // 隐藏缩放窗口                      │
     * │    RxBus.post(                          │
     * │      WAVEZONE_DISPLAY_YTZOOM,           │
     * │      new YTZoomMsgDisplay(false)        │
     * │    );                                   │
     * │  }                                      │
     * │                                         │
     * └─────────────────────────────────────────┘
     * </pre>
     */
    private boolean isDisplay;   // 缩放窗口显示标志：true=显示，false=隐藏
    
    /**
     * 重新加载大时基标志
     * 
     * <p>标记是否需要重新加载大时基数据。当切换到大时基档位时，
     * 缩放窗口需要重新加载波形数据以适应新的时基。</p>
     * 
     * <h4>取值说明</h4>
     * <ul>
     *   <li><b>true</b>: 需要重新加载大时基数据</li>
     *   <li><b>false</b>: 不需要重新加载（默认值）</li>
     * </ul>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>切换到大时基档位时设置为true</li>
     *   <li>缩放窗口接收到此标志后重新加载波形数据</li>
     *   <li>确保缩放窗口显示与时基档位同步</li>
     * </ul>
     * 
     * <h4>大时基说明</h4>
     * <p>大时基通常指时基≥100ms/div的档位，此时波形数据量较大，
     * 需要特殊处理以确保缩放窗口的正确显示。</p>
     */
    private boolean isReloadLargeTimeScale = false;   // 重新加载大时基标志：默认false，切换大时基时设为true
    
    /**
     * 播放声音标志
     * 
     * <p>控制缩放操作时是否播放提示声音。默认启用声音反馈，
     * 提供更好的用户体验。</p>
     * 
     * <h4>取值说明</h4>
     * <ul>
     *   <li><b>true</b>: 播放声音（默认值）</li>
     *   <li><b>false</b>: 不播放声音</li>
     * </ul>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>缩放窗口显示/隐藏时播放提示音</li>
     *   <li>用户可在设置中关闭声音反馈</li>
     *   <li>提供操作反馈，增强用户体验</li>
     * </ul>
     * 
     * <h4>声音类型</h4>
     * <p>通常播放短促的提示音，告知用户缩放窗口状态已改变。</p>
     */
    private boolean isPlaySound = true;   // 播放声音标志：默认true，启用声音反馈

    /**
     * 构造函数 - 创建YT缩放显示消息
     * 
     * <p>创建YTZoomMsgDisplay实例，设置缩放窗口的显示状态。
     * 这是主要的构造方式，通过isDisplay参数控制窗口显示/隐藏。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>isDisplay</td><td>boolean</td><td>缩放窗口显示标志，true=显示，false=隐藏</td></tr>
     * </table>
     * 
     * <h4>构造示例</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  构造示例                               │
     * ├─────────────────────────────────────────┤
     * │                                         │
     * │  // 显示缩放窗口                        │
     * │  YTZoomMsgDisplay msgDisplay =          │
     * │    new YTZoomMsgDisplay(true);          │
     * │                                         │
     * │  // 隐藏缩放窗口                        │
     * │  YTZoomMsgDisplay msgDisplay =          │
     * │    new YTZoomMsgDisplay(false);         │
     * │                                         │
     * │  // 发送事件                            │
     * │  RxBus.getInstance().post(              │
     * │    RxEnum.WAVEZONE_DISPLAY_YTZOOM,      │
     * │    msgDisplay                           │
     * │  );                                     │
     * │                                         │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>WaveZoneDisplay_YT检测到双指缩放手势时创建</li>
     *   <li>根据手势状态判断显示/隐藏，传入相应的isDisplay值</li>
     *   <li>创建后立即通过RxBus发送给订阅者</li>
     * </ul>
     * 
     * @param isDisplay 缩放窗口显示标志，true=显示，false=隐藏
     * @see com.micsig.tbook.tbookscope.rxjava.RxBus#post(Object, Object)
     */
    public YTZoomMsgDisplay(boolean isDisplay) {   // 构造函数：创建YT缩放显示消息，设置显示状态
        this.isDisplay = isDisplay;   // 设置isDisplay属性：传入的显示标志值
    }   // 构造函数结束

    /**
     * 获取缩放窗口显示标志
     * 
     * <p>返回缩放窗口是否显示的状态。</p>
     * 
     * <h4>返回值说明</h4>
     * <ul>
     *   <li><b>true</b>: 缩放窗口应显示</li>
     *   <li><b>false</b>: 缩放窗口应隐藏</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <p>WaveZoneDisplay_YTZoom接收到YTZoomMsgDisplay消息后调用此方法，
     * 根据返回值控制缩放窗口的可见性。</p>
     * 
     * @return 缩放窗口显示标志，true=显示，false=隐藏
     */
    public boolean isDisplay() {   // 方法：获取缩放窗口显示标志
        return isDisplay;   // 返回isDisplay属性值
    }   // isDisplay方法结束

    /**
     * 设置缩放窗口显示标志
     * 
     * <p>设置缩放窗口的显示状态。通常在构造时设置，
     * 也可在运行时动态修改。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>display</td><td>boolean</td><td>显示标志，true=显示，false=隐藏</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>动态修改缩放窗口显示状态时调用</li>
     *   <li>响应用户设置变化时更新显示状态</li>
     * </ul>
     * 
     * @param display 缩放窗口显示标志，true=显示，false=隐藏
     */
    public void setDisplay(boolean display) {   // 方法：设置缩放窗口显示标志
        isDisplay = display;   // 设置isDisplay属性值
    }   // setDisplay方法结束

    /**
     * 获取重新加载大时基标志
     * 
     * <p>返回是否需要重新加载大时基数据的状态。</p>
     * 
     * <h4>返回值说明</h4>
     * <ul>
     *   <li><b>true</b>: 需要重新加载大时基数据</li>
     *   <li><b>false</b>: 不需要重新加载</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <p>WaveZoneDisplay_YTZoom接收到消息后调用此方法，
     * 根据返回值决定是否重新加载波形数据。</p>
     * 
     * @return 重新加载大时基标志，true=需要重载，false=不需要
     */
    public boolean isReloadLargeTimeScale() {   // 方法：获取重新加载大时基标志
        return isReloadLargeTimeScale;   // 返回isReloadLargeTimeScale属性值
    }   // isReloadLargeTimeScale方法结束

    /**
     * 设置重新加载大时基标志
     * 
     * <p>设置是否需要重新加载大时基数据。当切换到大时基档位时，
     * 需要设置为true以触发数据重载。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>reloadLargeTimeScale</td><td>boolean</td><td>重载标志，true=需要重载，false=不需要</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>切换到大时基档位时设置为true</li>
     *   <li>缩放窗口数据加载完成后重置为false</li>
     * </ul>
     * 
     * @param reloadLargeTimeScale 重新加载大时基标志，true=需要重载，false=不需要
     */
    public void setReloadLargeTimeScale(boolean reloadLargeTimeScale) {   // 方法：设置重新加载大时基标志
        isReloadLargeTimeScale = reloadLargeTimeScale;   // 设置isReloadLargeTimeScale属性值
    }   // setReloadLargeTimeScale方法结束

    /**
     * 获取播放声音标志
     * 
     * <p>返回缩放操作时是否播放声音的状态。</p>
     * 
     * <h4>返回值说明</h4>
     * <ul>
     *   <li><b>true</b>: 播放声音</li>
     *   <li><b>false</b>: 不播放声音</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <p>缩放窗口显示/隐藏时调用此方法，根据返回值决定是否播放提示音。</p>
     * 
     * @return 播放声音标志，true=播放，false=不播放
     */
    public boolean isPlaySound() {   // 方法：获取播放声音标志
        return isPlaySound;   // 返回isPlaySound属性值
    }   // isPlaySound方法结束

    /**
     * 设置播放声音标志
     * 
     * <p>设置缩放操作时是否播放声音。可根据用户设置动态调整。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>playSound</td><td>boolean</td><td>播放声音标志，true=播放，false=不播放</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>用户在设置中关闭声音反馈时设置为false</li>
     *   <li>用户在设置中启用声音反馈时设置为true</li>
     * </ul>
     * 
     * @param playSound 播放声音标志，true=播放，false=不播放
     */
    public void setPlaySound(boolean playSound) {   // 方法：设置播放声音标志
        isPlaySound = playSound;   // 设置isPlaySound属性值
    }   // setPlaySound方法结束

    /**
     * 获取字符串表示 - 用于调试和日志输出
     * 
     * <p>返回YTZoomMsgDisplay对象的字符串表示，包含isDisplay状态。
     * 主要用于调试、日志输出和状态追踪。</p>
     * 
     * <h4>返回格式</h4>
     * <pre>
     * YTZoomMsgDisplay{isDisplay=true}   // 显示状态
     * YTZoomMsgDisplay{isDisplay=false}  // 隐藏状态
     * </pre>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>调试时打印消息状态</li>
     *   <li>日志记录缩放窗口显示状态</li>
     *   <li>追踪RxBus事件传递</li>
     * </ul>
     * 
     * @return 字符串表示，格式为"YTZoomMsgDisplay{isDisplay=值}"
     */
    @Override   // 重写Object类的toString方法
    public String toString() {   // 方法：获取字符串表示，用于调试和日志输出
        return "YTZoomMsgDisplay{" +   // 返回字符串：类名 + 开括号
                "isDisplay=" + isDisplay +   // 添加isDisplay属性值
                '}';   // 添加闭括号
    }   // toString方法结束
}   // YTZoomMsgDisplay类结束