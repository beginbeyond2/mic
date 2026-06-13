package com.micsig.tbook.tbookscope;   // 示波器主应用包，包含自定义布局类

import android.content.Context;   // 导入Android Context类，用于访问应用资源
import android.util.AttributeSet;   // 导入Android AttributeSet类，用于XML属性解析
import android.view.MotionEvent;   // 导入Android MotionEvent类，用于触摸事件处理
import android.view.View;   // 导入Android View类，用于视图操作
import android.widget.LinearLayout;   // 导入Android LinearLayout类，用于线性布局

import androidx.annotation.Nullable;   // 导入Nullable注解，用于标记可空参数

/**
 * 主界面右侧布局容器 - 自定义LinearLayout实现触摸事件拦截
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    右侧布局触摸事件处理架构                      │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │  ┌─────────────────────────────────────────────┐               │
 * │  │           MainRightLayout                    │               │
 * │  │         (右侧布局容器)                        │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  继承 LinearLayout                   │   │               │
 * │  │  │  - 线性布局容器                       │   │               │
 * │  │  │  - 包含通道主控件                     │   │               │
 * │  │  │  - 拦截触摸事件                       │   │               │
 * │  │  │  - 委托给监听器处理                   │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  OnInterceptListener接口             │   │               │
 * │  │  │  - onInterceptTouchEvent(ev)         │   │               │
 * │  │  │    拦截触摸事件                      │   │               │
 * │  │  │  - onTouch(v, event)                 │   │               │
 * │  │  │    处理触摸事件                      │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  └─────────────────────────────────────────────┘               │
 * │                       │                                       │
 * │                       │ setOnInterceptListener()               │
 * │                       │ 设置监听器                             │
 * │                       ▼                                       │
 * │           ┌─────────────────────┐                             │
 * │           │  MainHolderRight    │                             │
 * │           │    Channels         │                             │
 * │           │  (通道持有者)        │                             │
 * │           │                     │                             │
 * │           │  - 实现监听器接口    │                             │
 * │           │  - 处理触摸事件      │                             │
 * │           │  - 响应通道操作      │                             │
 * │           │                     │                             │
 * │           │  触摸事件处理流程：  │                             │
 * │           │    1. 拦截事件       │                             │
 * │           │    2. 判断事件类型   │                             │
 * │           │    3. 处理事件       │                             │
 * │           │    4. 返回处理结果   │                             │
 * │           └─────────────────────┘                             │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * <h3>模块定位</h3>
 * <p>本类是主界面右侧通道区域的自定义布局容器，继承自LinearLayout。
 * 通过监听器模式（OnInterceptListener）将触摸事件的处理逻辑委托给外部类（如MainHolderRightChannels），
 * 实现灵活的触摸事件拦截和处理机制。</p>
 * 
 * <h3>核心职责</h3>
 * <ul>
 *   <li><b>布局容器</b>：作为右侧通道区域的线性布局容器</li>
 *   <li><b>事件拦截</b>：拦截触摸事件，决定是否传递给子View</li>
 *   <li><b>事件处理</b>：处理触摸事件，响应通道操作</li>
 *   <li><b>事件委托</b>：通过监听器将事件处理委托给外部类</li>
 * </ul>
 * 
 * <h3>触摸事件拦截机制说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              触摸事件拦截机制详解                              │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  Android触摸事件传递流程：                                     │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  Activity                                     │           │
 * │    │  ─────────────→                               │           │
 * │    │  dispatchTouchEvent()                         │           │
 * │    │    - 分发触摸事件                              │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                       │                                       │
 * │                       ▼                                       │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  ViewGroup (MainRightLayout)                 │           │
 * │    │  ─────────────→                               │           │
 * │    │  dispatchTouchEvent()                         │           │
 * │    │    - 分发触摸事件                              │           │
 * │    │                                             │           │
 * │    │  onInterceptTouchEvent()                     │           │
 * │    │    - 拦截触摸事件                              │           │
 * │    │    - 返回true: 拦截事件                       │           │
 * │    │    - 返回false: 不拦截，传递给子View          │           │
 * │    │                                             │           │
 * │    │  onTouchEvent()                               │           │
 * │    │    - 处理触摸事件                              │           │
 * │    │    - 在拦截后调用                              │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                       │                                       │
 * │                       ▼                                       │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  Child View                                  │           │
 * │    │  (MainRightLayoutItemChannelMaster)          │           │
 * │    │  ─────────────→                               │           │
 * │    │  dispatchTouchEvent()                         │           │
 * │    │    - 分发触摸事件                              │           │
 * │    │                                             │           │
 * │    │  onTouchEvent()                               │           │
 * │    │    - 处理触摸事件                              │           │
 * │    │    - 在不拦截时调用                            │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                                                               │
 * │  MainRightLayout拦截流程：                                     │
 * │    Step 1: 触摸事件到达MainRightLayout                         │
 * │      └─ dispatchTouchEvent()被调用                            │
 * │                                                               │
 * │    Step 2: 调用onInterceptTouchEvent()                        │
 * │      └─ 判断是否有监听器                                        │
 * │      └─ 如果有监听器，调用监听器的onInterceptTouchEvent()      │
 * │      └─ 根据监听器返回值决定是否拦截                            │
 * │      └─ 返回true: 拦截事件，调用onTouchEvent()                 │
 * │      └─ 返回false: 不拦截，传递给子View                         │
 * │                                                               │
 * │    Step 3: 如果拦截，调用onTouchEvent()                        │
 * │      └─ 判断是否有监听器                                        │
 * │      └─ 如果有监听器，调用监听器的onTouch()                     │
 * │      └─ 返回监听器的处理结果                                    │
 * │      └─ 如果没有监听器，返回true（消费事件）                    │
 * │                                                               │
 * │  监听器模式优点：                                                │
 * │    - 解耦：将事件处理逻辑委托给外部类                            │
 * │    - 灵活：可以动态更换事件处理逻辑                              │
 * │    - 可扩展：可以在外部类中实现复杂的事件处理                    │
 * │    - 可维护：事件处理逻辑集中在外部类中                          │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>使用场景</h3>
 * <ul>
 *   <li><b>通道操作</b>：处理通道主控件的触摸事件（点击、长按、滑动）</li>
 *   <li><b>滑动菜单</b>：拦截触摸事件，触发滑动菜单显示/隐藏</li>
 *   <li><b>双击检测</b>：检测双击事件，打开通道详细设置</li>
 *   <li><b>手势识别</b>：识别复杂手势，执行相应操作</li>
 * </ul>
 * 
 * <h3>依赖关系</h3>
 * <ul>
 *   <li>{@link LinearLayout} - Android线性布局基类</li>
 *   <li>{@link OnInterceptListener} - 触摸事件拦截监听器接口</li>
 *   <li>{@link com.micsig.tbook.tbookscope.main.mainright.MainHolderRightChannels} - 通道持有者，实现监听器接口</li>
 *   <li>{@link com.micsig.tbook.tbookscope.main.mainright.MainRightLayoutItemChannelMaster} - 通道主控件，作为子View</li>
 * </ul>
 * 
 * <h3>设计模式</h3>
 * <p>本类采用监听器模式（Listener Pattern），将触摸事件的处理逻辑委托给外部类。
 * 通过OnInterceptListener接口定义事件处理规范，外部类实现接口并注册监听器，
 * 实现事件处理的解耦和灵活扩展。</p>
 * 
 * <h3>注意事项</h3>
 * <ul>
 *   <li><b>监听器注册</b>：使用前必须通过setOnInterceptListener()注册监听器</li>
 *   <li><b>事件拦截</b>：监听器返回true表示拦截事件，返回false表示不拦截</li>
 *   <li><b>事件消费</b>：onTouchEvent()默认返回true，表示消费事件</li>
 *   <li><b>事件顺序</b>：先调用onInterceptTouchEvent()，再调用onTouchEvent()</li>
 * </ul>
 * 
 * @see LinearLayout
 * @see OnInterceptListener
 * @see com.micsig.tbook.tbookscope.main.mainright.MainHolderRightChannels
 * @see com.micsig.tbook.tbookscope.main.mainright.MainRightLayoutItemChannelMaster
 * @author Micsig智能示波器团队
 * @version 2.0
 * @since 1.0
 */
public class MainRightLayout extends LinearLayout {   // 主界面右侧布局容器：自定义LinearLayout实现触摸事件拦截
    
    /**
     * Android上下文对象 - 用于访问应用资源和系统服务
     * 
     * <p>Context对象，用于访问应用资源、启动Activity、获取系统服务等。
     * 在构造函数中初始化，保存为成员变量以便后续使用。</p>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>访问应用资源（如字符串、颜色、尺寸等）</li>
     *   <li>获取系统服务（如LayoutInflater等）</li>
     *   <li>创建View对象</li>
     * </ul>
     */
    private Context context;   // Android上下文对象：用于访问应用资源和系统服务
    
    /**
     * 触摸事件拦截监听器 - 委托触摸事件处理逻辑
     * 
     * <p>OnInterceptListener监听器，用于委托触摸事件的处理逻辑。
     * 通过setOnInterceptListener()方法注册，在onInterceptTouchEvent()和onTouchEvent()中调用。</p>
     * 
     * <h4>监听器接口方法</h4>
     * <ul>
     *   <li><b>onInterceptTouchEvent(ev)</b>: 拦截触摸事件，决定是否传递给子View</li>
     *   <li><b>onTouch(v, event)</b>: 处理触摸事件，响应触摸操作</li>
     * </ul>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>MainHolderRightChannels实现监听器接口</li>
     *   <li>处理通道主控件的触摸事件</li>
     *   <li>响应滑动菜单、双击、手势等操作</li>
     * </ul>
     */
    private OnInterceptListener onInterceptListener;   // 触摸事件拦截监听器：委托触摸事件处理逻辑

    /**
     * 设置触摸事件拦截监听器
     * 
     * <p>设置OnInterceptListener监听器，用于委托触摸事件的处理逻辑。
     * 通常在MainHolderRightChannels初始化时调用，注册监听器实现事件处理。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>onInterceptListener</td><td>OnInterceptListener</td><td>触摸事件拦截监听器</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>MainHolderRightChannels初始化时调用</li>
     *   <li>在布局加载完成后注册监听器</li>
     * </ul>
     * 
     * <h4>使用示例</h4>
     * <pre>
     * MainRightLayout mainRightLayout = findViewById(R.id.mainRightLayout);
     * mainRightLayout.setOnInterceptListener(new OnInterceptListener() {
     *     @Override
     *     public boolean onInterceptTouchEvent(MotionEvent ev) {
     *         // 拦截逻辑
     *         return false; // 不拦截
     *     }
     *     
     *     @Override
     *     public boolean onTouch(View v, MotionEvent event) {
     *         // 处理逻辑
     *         return true; // 消费事件
     *     }
     * });
     * </pre>
     * 
     * @param onInterceptListener 触摸事件拦截监听器
     * @see OnInterceptListener
     */
    public void setOnInterceptListener(OnInterceptListener onInterceptListener) {   // 方法：设置触摸事件拦截监听器
        this.onInterceptListener = onInterceptListener;   // 设置onInterceptListener属性：传入的监听器对象
    }   // setOnInterceptListener方法结束

    /**
     * 触摸事件拦截监听器接口 - 定义触摸事件处理规范
     * 
     * <p>OnInterceptListener接口，定义触摸事件拦截和处理的规范。
     * 外部类（如MainHolderRightChannels）实现此接口，注册到MainRightLayout，
     * 实现触摸事件的委托处理。</p>
     * 
     * <h4>接口方法</h4>
     * <table border="1">
     *   <tr><th>方法</th><th>参数</th><th>返回值</th><th>说明</th></tr>
     *   <tr><td>onInterceptTouchEvent</td><td>MotionEvent ev</td><td>boolean</td><td>拦截触摸事件</td></tr>
     *   <tr><td>onTouch</td><td>View v, MotionEvent event</td><td>boolean</td><td>处理触摸事件</td></tr>
     * </table>
     * 
     * <h4>实现示例</h4>
     * <pre>
     * public class MainHolderRightChannels implements OnInterceptListener {
     *     @Override
     *     public boolean onInterceptTouchEvent(MotionEvent ev) {
     *         // 拦截逻辑：判断是否需要拦截事件
     *         int action = ev.getAction();
     *         if (action == MotionEvent.ACTION_DOWN) {
     *             // 检测是否在特定区域
     *             return true; // 拦截事件
     *         }
     *         return false; // 不拦截
     *     }
     *     
     *     @Override
     *     public boolean onTouch(View v, MotionEvent event) {
     *         // 处理逻辑：响应触摸操作
     *         int action = event.getAction();
     *         switch (action) {
     *             case MotionEvent.ACTION_DOWN:
     *                 // 处理按下事件
     *                 break;
     *             case MotionEvent.ACTION_MOVE:
     *                 // 处理移动事件
     *                 break;
     *             case MotionEvent.ACTION_UP:
     *                 // 处理抬起事件
     *                 break;
     *         }
     *         return true; // 消费事件
     *     }
     * }
     * </pre>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>通道操作：处理通道主控件的触摸事件</li>
     *   <li>滑动菜单：拦截触摸事件，触发滑动菜单</li>
     *   <li>双击检测：检测双击事件，打开通道设置</li>
     *   <li>手势识别：识别复杂手势，执行相应操作</li>
     * </ul>
     * 
     * @see MainRightLayout#setOnInterceptListener(OnInterceptListener)
     * @see com.micsig.tbook.tbookscope.main.mainright.MainHolderRightChannels
     */
    public interface OnInterceptListener {   // 触摸事件拦截监听器接口：定义触摸事件处理规范
        
        /**
         * 拦截触摸事件 - 决定是否传递给子View
         * 
         * <p>拦截触摸事件，决定是否将事件传递给子View。
         * 返回true表示拦截事件，事件不会传递给子View，会调用onTouchEvent()处理。
         * 返回false表示不拦截事件，事件会传递给子View处理。</p>
         * 
         * <h4>参数说明</h4>
         * <table border="1">
         *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
         *   <tr><td>ev</td><td>MotionEvent</td><td>触摸事件对象，包含事件类型、坐标等信息</td></tr>
         * </table>
         * 
         * <h4>返回值说明</h4>
         * <ul>
         *   <li><b>true</b>: 拦截事件，不传递给子View</li>
         *   <li><b>false</b>: 不拦截事件，传递给子View</li>
         * </ul>
         * 
         * <h4>事件类型</h4>
         * <table border="1">
         *   <tr><th>事件类型</th><th>说明</th></tr>
         *   <tr><td>ACTION_DOWN</td><td>手指按下</td></tr>
         *   <tr><td>ACTION_MOVE</td><td>手指移动</td></tr>
         *   <tr><td>ACTION_UP</td><td>手指抬起</td></tr>
         *   <tr><td>ACTION_CANCEL</td><td>事件取消</td></tr>
         * </table>
         * 
         * @param ev 触摸事件对象
         * @return 是否拦截事件，true=拦截，false=不拦截
         */
        boolean onInterceptTouchEvent(MotionEvent ev);   // 方法：拦截触摸事件，决定是否传递给子View
        
        /**
         * 处理触摸事件 - 响应触摸操作
         * 
         * <p>处理触摸事件，响应触摸操作。
         * 在onInterceptTouchEvent()返回true（拦截事件）后调用。
         * 返回true表示消费事件，事件不会继续传递。
         * 返回false表示不消费事件，事件会继续传递。</p>
         * 
         * <h4>参数说明</h4>
         * <table border="1">
         *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
         *   <tr><td>v</td><td>View</td><td>触发触摸事件的View对象</td></tr>
         *   <tr><td>event</td><td>MotionEvent</td><td>触摸事件对象，包含事件类型、坐标等信息</td></tr>
         * </table>
         * 
         * <h4>返回值说明</h4>
         * <ul>
         *   <li><b>true</b>: 消费事件，事件不会继续传递</li>
         *   <li><b>false</b>: 不消费事件，事件会继续传递</li>
         * </ul>
         * 
         * @param v 触发触摸事件的View对象
         * @param event 触摸事件对象
         * @return 是否消费事件，true=消费，false=不消费
         */
        boolean onTouch(View v, MotionEvent event);   // 方法：处理触摸事件，响应触摸操作
    }   // OnInterceptListener接口结束

    /**
     * 构造函数（单参数） - 创建右侧布局容器
     * 
     * <p>创建MainRightLayout实例，只传入Context参数。
     * 调用三参数构造函数，传入null作为attrs参数。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>context</td><td>Context</td><td>Android上下文对象</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>在代码中动态创建MainRightLayout时调用</li>
     *   <li>不通过XML布局文件创建时使用</li>
     * </ul>
     * 
     * <h4>构造示例</h4>
     * <pre>
     * MainRightLayout mainRightLayout = new MainRightLayout(context);
     * </pre>
     * 
     * @param context Android上下文对象
     */
    public MainRightLayout(Context context) {   // 构造函数（单参数）：创建右侧布局容器，只传入Context参数
        this(context, null);   // 调用三参数构造函数：传入null作为attrs参数
    }   // 构造函数结束

    /**
     * 构造函数（双参数） - 创建右侧布局容器（XML加载）
     * 
     * <p>创建MainRightLayout实例，传入Context和AttributeSet参数。
     * 调用三参数构造函数，传入0作为defStyleAttr参数。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>context</td><td>Context</td><td>Android上下文对象</td></tr>
     *   <tr><td>attrs</td><td>AttributeSet (@Nullable)</td><td>XML属性集合，可能为null</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>通过XML布局文件创建MainRightLayout时调用</li>
     *   <li>系统自动调用，解析XML属性</li>
     * </ul>
     * 
     * <h4>XML示例</h4>
     * <pre>
     * &lt;com.micsig.tbook.tbookscope.MainRightLayout
     *     android:id="@+id/mainRightLayout"
     *     android:layout_width="match_parent"
     *     android:layout_height="wrap_content"
     *     android:orientation="vertical" /&gt;
     * </pre>
     * 
     * @param context Android上下文对象
     * @param attrs XML属性集合，可能为null
     */
    public MainRightLayout(Context context, @Nullable AttributeSet attrs) {   // 构造函数（双参数）：创建右侧布局容器，传入Context和AttributeSet参数
        this(context, attrs, 0);   // 调用三参数构造函数：传入0作为defStyleAttr参数
    }   // 构造函数结束

    /**
     * 构造函数（三参数） - 创建右侧布局容器（完整构造）
     * 
     * <p>创建MainRightLayout实例，传入Context、AttributeSet和defStyleAttr参数。
     * 这是完整的构造函数，调用父类构造函数并初始化成员变量。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>context</td><td>Context</td><td>Android上下文对象</td></tr>
     *   <tr><td>attrs</td><td>AttributeSet (@Nullable)</td><td>XML属性集合，可能为null</td></tr>
     *   <tr><td>defStyleAttr</td><td>int</td><td>默认样式属性</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>通过XML布局文件创建MainRightLayout时调用</li>
     *   <li>系统自动调用，解析XML属性和样式</li>
     * </ul>
     * 
     * <h4>初始化流程</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  初始化流程                             │
     * ├─────────────────────────────────────────┤
     * │                                         │
     * │  Step 1: 调用父类构造函数               │
     * │    └─ super(context, attrs, defStyleAttr)│
     * │    └─ 初始化LinearLayout                │
     * │                                         │
     * │  Step 2: 初始化成员变量                 │
     * │    └─ this.context = context            │
     * │    └─ 保存Context对象                   │
     * │                                         │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * @param context Android上下文对象
     * @param attrs XML属性集合，可能为null
     * @param defStyleAttr 默认样式属性
     */
    public MainRightLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {   // 构造函数（三参数）：创建右侧布局容器，传入完整参数
        super(context, attrs, defStyleAttr);   // 调用父类LinearLayout构造函数：初始化LinearLayout
        this.context = context;   // 初始化context属性：保存传入的Context对象
    }   // 构造函数结束

    /**
     * 拦截触摸事件 - 决定是否传递给子View
     * 
     * <p>拦截触摸事件，决定是否将事件传递给子View。
     * 通过监听器模式，将拦截逻辑委托给OnInterceptListener处理。
     * 如果监听器返回true，则拦截事件；否则不拦截，传递给子View。</p>
     * 
     * <h4>拦截流程</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  拦截流程                               │
     * ├─────────────────────────────────────────┤
     * │                                         │
     * │  Step 1: 判断是否有监听器               │
     * │    └─ if (onInterceptListener != null)  │
     * │                                         │
     * │  Step 2: 调用监听器拦截方法             │
     * │    └─ boolean b = onInterceptListener   │
     * │        .onInterceptTouchEvent(ev)       │
     * │                                         │
     * │  Step 3: 判断拦截结果                   │
     * │    └─ if (b) {                          │
     * │        return b; // 拦截事件            │
     * │      }                                   │
     * │                                         │
     * │  Step 4: 调用父类拦截方法               │
     * │    └─ return super.onInterceptTouchEvent(ev)│
     * │    └─ 默认不拦截                        │
     * │                                         │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>ev</td><td>MotionEvent</td><td>触摸事件对象，包含事件类型、坐标等信息</td></tr>
     * </table>
     * 
     * <h4>返回值说明</h4>
     * <ul>
     *   <li><b>true</b>: 拦截事件，不传递给子View，会调用onTouchEvent()处理</li>
     *   <li><b>false</b>: 不拦截事件，传递给子View处理</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>在dispatchTouchEvent()中被调用</li>
     *   <li>每次触摸事件都会调用此方法</li>
     * </ul>
     * 
     * @param ev 触摸事件对象
     * @return 是否拦截事件，true=拦截，false=不拦截
     * @see OnInterceptListener#onInterceptTouchEvent(MotionEvent)
     */
    @Override   // 重写LinearLayout的onInterceptTouchEvent方法
    public boolean onInterceptTouchEvent(MotionEvent ev) {   // 方法：拦截触摸事件，决定是否传递给子View
        if (onInterceptListener != null) {   // 判断：是否有监听器
            boolean b = onInterceptListener.onInterceptTouchEvent(ev);   // 调用监听器拦截方法：获取拦截结果
            if (b) {   // 判断：监听器是否返回true（拦截事件）
                return b;   // 返回true：拦截事件，不传递给子View
            }   // if判断结束
        }   // 监听器判断结束
        return super.onInterceptTouchEvent(ev);   // 调用父类拦截方法：默认不拦截，传递给子View
    }   // onInterceptTouchEvent方法结束

    /**
     * 处理触摸事件 - 响应触摸操作
     * 
     * <p>处理触摸事件，响应触摸操作。
     * 通过监听器模式，将处理逻辑委托给OnInterceptListener处理。
     * 如果有监听器，返回监听器的处理结果；否则返回true（消费事件）。</p>
     * 
     * <h4>处理流程</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  处理流程                               │
     * ├─────────────────────────────────────────┤
     * │                                         │
     * │  Step 1: 判断是否有监听器               │
     * │    └─ if (onInterceptListener != null)  │
     * │                                         │
     * │  Step 2: 调用监听器处理方法             │
     * │    └─ return onInterceptListener        │
     * │        .onTouch(this, event)            │
     * │    └─ 返回监听器的处理结果              │
     * │                                         │
     * │  Step 3: 如果没有监听器                 │
     * │    └─ return true                       │
     * │    └─ 默认消费事件                      │
     * │                                         │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>event</td><td>MotionEvent</td><td>触摸事件对象，包含事件类型、坐标等信息</td></tr>
     * </table>
     * 
     * <h4>返回值说明</h4>
     * <ul>
     *   <li><b>true</b>: 消费事件，事件不会继续传递</li>
     *   <li><b>false</b>: 不消费事件，事件会继续传递</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>在onInterceptTouchEvent()返回true（拦截事件）后调用</li>
     *   <li>在拦截事件后处理触摸操作</li>
     * </ul>
     * 
     * @param event 触摸事件对象
     * @return 是否消费事件，true=消费，false=不消费
     * @see OnInterceptListener#onTouch(View, MotionEvent)
     */
    @Override   // 重写LinearLayout的onTouchEvent方法
    public boolean onTouchEvent(MotionEvent event) {   // 方法：处理触摸事件，响应触摸操作
        if (onInterceptListener != null) {   // 判断：是否有监听器
           return onInterceptListener.onTouch(this, event);   // 调用监听器处理方法：返回监听器的处理结果
        }   // 监听器判断结束
        return true;   // 返回true：默认消费事件，事件不会继续传递
    }   // onTouchEvent方法结束
}   // MainRightLayout类结束