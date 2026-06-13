package com.micsig.tbook.tbookscope;   // 示波器主应用包，包含缓存加载消息类

/**
 * 缓存加载消息数据类 - 通知组件加载缓存配置
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    缓存加载消息架构                              │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │  ┌─────────────────┐                                           │
 * │  │   MainActivity  │                                           │
 * │  │  (主Activity)    │                                           │
 * │  │                 │                                           │
 * │  │  初始化完成      │                                           │
 * │  │  ────────────→ │                                           │
 * │  │                 │                                           │
 * │  │  RxBus事件发送  │                                           │
 * │  │  (2个事件)      │                                           │
 * │  └────────┬────────┘                                           │
 * │           │                                                     │
 * │           │ post(RxEnum.MAIN_LOAD_CACHE,                        │
 * │           │      new LoadCache())                               │
 * │           │                                                     │
 * │           │ post(RxEnum.MAIN_LOAD_CACHE_EX,                     │
 * │           │      new LoadCache())                               │
 * │           ▼                                                     │
 * │  ┌─────────────────────────────────────────────┐               │
 * │  │              LoadCache                       │               │
 * │  │         (缓存加载消息)                        │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  空类设计                            │   │               │
 * │  │  │  - 无属性：不携带数据               │   │               │
 * │  │  │  - 无方法：纯事件标识               │   │               │
 * │  │  │  - 作为RxBus事件载体                │   │               │
 * │  │  │  - 触发缓存加载流程                 │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  └─────────────────────────────────────────────┘               │
 * │                       │                                       │
 * │                       ▼                                       │
 * │           ┌─────────────────────┐                             │
 * │           │  RxBus订阅者         │                             │
 * │           │  (多个组件)          │                             │
 * │           │                     │                             │
 * │           │  - CursorManage     │                             │
 * │           │  - TopLayoutUser    │                             │
 * │           │    SetAuxOut        │                             │
 * │           │  - TopLayoutSave    │                             │
 * │           │    Picture          │                             │
 * │           │  - 其他组件...      │                             │
 * │           │                     │                             │
 * │           │  - 收到加载消息      │                             │
 * │           │  - 从CacheUtil读取  │                             │
 * │           │  - 恢复组件状态      │                             │
 * │           │  - 更新UI显示        │                             │
 * │           └─────────────────────┘                             │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * <h3>模块定位</h3>
 * <p>本类是缓存加载系统的数据传输对象（DTO），用于在MainActivity初始化完成后
 * 通过RxBus事件总线发送缓存加载通知。这是一个空类设计，只作为事件标识，
 * 不携带任何数据，通知订阅者从CacheUtil加载缓存配置并恢复状态。</p>
 * 
 * <h3>核心职责</h3>
 * <ul>
 *   <li><b>事件标识</b>：作为缓存加载事件的标识载体</li>
 *   <li><b>消息传递</b>：通过RxBus通知订阅者加载缓存</li>
 *   <li><b>状态恢复</b>：触发组件从CacheUtil恢复配置状态</li>
 * </ul>
 * 
 * <h3>缓存加载流程说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              缓存加载流程详解                                  │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  缓存加载时机：                                                │
 * │    - MainActivity初始化完成后                                  │
 * │    - 示波器启动时恢复上次配置                                  │
 * │    - 系统重启后恢复用户设置                                    │
 * │                                                               │
 * │  缓存加载流程：                                                │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  Step 1: MainActivity初始化                  │           │
 * │    │    - 创建MainViewGroup                       │           │
 * │    │    - 初始化各个组件                          │           │
 * │    │    - 初始化HorizontalAxis                    │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                       │                                       │
 * │                       ▼                                       │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  Step 2: 发送缓存加载事件                    │           │
 * │    │    - RxBus.getInstance().post(               │           │
 * │    │        RxEnum.MAIN_LOAD_CACHE,               │           │
 * │    │        new LoadCache())                      │           │
 * │    │                                             │           │
 * │    │    - RxBus.getInstance().post(               │           │
 * │    │        RxEnum.MAIN_LOAD_CACHE_EX,            │           │
 * │    │        new LoadCache())                      │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                       │                                       │
 * │                       ▼                                       │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  Step 3: 订阅者接收事件                      │           │
 * │    │    - CursorManage: 恢复光标位置             │           │
 * │    │    - TopLayoutUserSetAuxOut: 恢复AuxOut设置 │           │
 * │    │    - TopLayoutSavePicture: 恢复保存设置     │           │
 * │    │    - 其他组件: 恢复各自配置                 │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                       │                                       │
 * │                       ▼                                       │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  Step 4: 从CacheUtil读取缓存                 │           │
 * │    │    - CacheUtil.get().getDouble()             │           │
 * │    │    - CacheUtil.get().getInt()                │           │
 * │    │    - CacheUtil.get().getBoolean()            │           │
 * │    │    - CacheUtil.get().getString()             │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                       │                                       │
 * │                       ▼                                       │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  Step 5: 恢复组件状态                        │           │
 * │    │    - 设置光标位置                            │           │
 * │    │    - 更新UI显示                              │           │
 * │    │    - 恢复用户设置                            │           │
 * │    │    - 同步配置状态                            │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                                                               │
 * │  两个事件的区别：                                              │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  MAIN_LOAD_CACHE                             │           │
 * │    │  - 主要缓存加载事件                          │           │
 * │    │  - 触发大部分组件加载缓存                    │           │
 * │    │  - 用于恢复基础配置                          │           │
 * │    │                                             │           │
 * │    │  MAIN_LOAD_CACHE_EX                          │           │
 * │    │  - 扩展缓存加载事件                          │           │
 * │    │  - 触发部分组件加载扩展缓存                  │           │
 * │    │  - 用于恢复扩展配置                          │           │
 * │    │  - 在MAIN_LOAD_CACHE之后发送                 │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>订阅者缓存加载示例</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              CursorManage缓存加载示例                          │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  // 订阅缓存加载事件                                           │
 * │  RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE)    │
 * │      .subscribe(consumerLoadCache);                           │
 * │                                                               │
 * │  // 定义消费者                                                 │
 * │  private Consumer&lt;LoadCache&gt; consumerLoadCache =              │
 * │      new Consumer&lt;LoadCache&gt;() {                                │
 * │    @Override                                                  │
 * │    public void accept(LoadCache loadCache) throws Exception { │
 * │      // 从CacheUtil读取光标位置                                │
 * │      double ytY1 = CacheUtil.get().getDouble(                 │
 * │          CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION +            │
 * │              TChan.Cursor_row_1);                             │
 * │      double ytY2 = CacheUtil.get().getDouble(                 │
 * │          CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION +            │
 * │              TChan.Cursor_row_2);                             │
 * │      int ytX1 = CacheUtil.get().getInt(                       │
 * │          CacheUtil.MAIN_WAVE_YT_CURSORV_POSITION +            │
 * │              TChan.Cursor_col_1);                             │
 * │      int ytX2 = CacheUtil.get().getInt(                       │
 * │          CacheUtil.MAIN_WAVE_YT_CURSORV_POSITION +            │
 * │              TChan.Cursor_col_2);                             │
 * │                                                               │
 * │      // 转换坐标并恢复光标位置                                  │
 * │      ytY1 = ScopeBase.changeAccuracy(                         │
 * │          ytY1 * ScopeBase.getToUICoff());                     │
 * │      ytY2 = ScopeBase.changeAccuracy(                         │
 * │          ytY2 * ScopeBase.getToUICoff());                     │
 * │                                                               │
 * │      // 设置光标位置                                           │
 * │      cursorManage_yt.setHCursor(ytY1, ytY2);                  │
 * │      cursorManage_yt.setVCursor(ytX1, ytX2);                  │
 * │    }                                                           │
 * │  };                                                            │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>空类设计说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              空类设计原理                                      │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  设计目的：                                                    │
 * │    - 作为事件标识：通知组件加载缓存                            │
 * │    - 不携带数据：只需要事件类型，不需要额外信息                 │
 * │    - 简化设计：避免不必要的数据传递                            │
 * │                                                               │
 * │  设计优点：                                                    │
 * │    - 轻量级：无属性，内存占用小                                │
 * │    - 高效：无需数据封装和解析                                  │
 * │    - 简洁：代码简单，易于理解                                  │
 * │    - 灵活：可随时扩展属性（如需要）                            │
 * │                                                               │
 * │  适用场景：                                                    │
 * │    - 状态通知：只需要通知状态变化，不需要数据                  │
 * │    - 事件触发：触发其他组件执行操作                            │
 * │    - 缓存加载：通知组件从CacheUtil加载缓存                     │
 * │                                                               │
 * │  扩展建议：                                                    │
 * │    如果未来需要携带数据，可以添加属性：                        │
 * │      - 加载类型：基础缓存、扩展缓存                            │
 * │      - 加载时间：记录加载的时间戳                              │
 * │      - 加载状态：成功、失败、部分成功                          │
 * │      - 加载项列表：需要加载的缓存项                            │
 * │                                                               │
 * │  类似设计：                                                    │
 * │    - ActivityMsgOnStop：Activity停止消息                     │
 * │    - ActivityMsgResult：Activity结果消息（携带数据）          │
 * │    - YTZoomMsgDisplay：YT缩放显示消息（携带数据）             │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>使用场景</h3>
 * <ul>
 *   <li><b>初始化加载</b>：MainActivity初始化完成后发送加载事件</li>
 *   <li><b>配置恢复</b>：订阅者收到消息后从CacheUtil恢复配置</li>
 *   <li><b>状态同步</b>：多个组件同时加载缓存，保持状态一致</li>
 *   <li><b>解耦通信</b>：通过RxBus实现组件间解耦通信</li>
 * </ul>
 * 
 * <h3>依赖关系</h3>
 * <ul>
 *   <li>{@link MainActivity} - 主Activity，在初始化完成后发送LoadCache消息</li>
 *   <li>{@link com.micsig.tbook.tbookscope.rxjava.RxBus} - RxBus事件总线，传递消息</li>
 *   <li>{@link com.micsig.tbook.tbookscope.rxjava.RxEnum#MAIN_LOAD_CACHE} - 主要缓存加载事件类型</li>
 *   <li>{@link com.micsig.tbook.tbookscope.rxjava.RxEnum#MAIN_LOAD_CACHE_EX} - 扩展缓存加载事件类型</li>
 *   <li>{@link com.micsig.tbook.tbookscope.util.CacheUtil} - 缓存工具类，存储和读取配置</li>
 *   <li>{@link com.micsig.tbook.tbookscope.wavezone.display.CursorManage} - 光标管理器，订阅LoadCache事件恢复光标位置</li>
 *   <li>{@link com.micsig.tbook.tbookscope.top.layout.userset.TopLayoutUserSetAuxOut} - AuxOut设置布局，订阅LoadCache事件恢复设置</li>
 *   <li>{@link com.micsig.tbook.tbookscope.top.layout.save.TopLayoutSavePicture} - 保存图片布局，订阅LoadCache事件恢复设置</li>
 * </ul>
 * 
 * <h3>设计模式</h3>
 * <p>本类采用DTO（Data Transfer Object）设计模式的简化版本，作为消息载体在组件间传递数据。
 * 这是一个空类设计，只作为事件标识，不携带任何数据。配合RxBus事件总线，
 * 实现观察者模式的解耦通信，触发多个组件同时加载缓存配置。</p>
 * 
 * <h3>注意事项</h3>
 * <ul>
 *   <li><b>空类设计</b>：本类为空类，不包含任何属性和方法</li>
 *   <li><b>事件标识</b>：只作为RxBus事件的标识，不携带数据</li>
 *   <li><b>两个事件</b>：MAIN_LOAD_CACHE和MAIN_LOAD_CACHE_EX，先后发送</li>
 *   <li><b>订阅者处理</b>：订阅者需要从CacheUtil读取缓存数据</li>
 *   <li><b>扩展性</b>：如果需要携带数据，可以添加属性和方法</li>
 * </ul>
 * 
 * @see MainActivity
 * @see com.micsig.tbook.tbookscope.rxjava.RxBus
 * @see com.micsig.tbook.tbookscope.rxjava.RxEnum#MAIN_LOAD_CACHE
 * @see com.micsig.tbook.tbookscope.rxjava.RxEnum#MAIN_LOAD_CACHE_EX
 * @see com.micsig.tbook.tbookscope.util.CacheUtil
 * @see com.micsig.tbook.tbookscope.wavezone.display.CursorManage
 * @see com.micsig.tbook.tbookscope.top.layout.userset.TopLayoutUserSetAuxOut
 * @see com.micsig.tbook.tbookscope.top.layout.save.TopLayoutSavePicture
 * @author Micsig智能示波器团队
 * @version 2.0
 * @since 1.0
 */
public class LoadCache {   // 缓存加载消息数据类：通知组件加载缓存配置

}   // LoadCache类结束（空类设计：无属性、无方法，只作为事件标识）