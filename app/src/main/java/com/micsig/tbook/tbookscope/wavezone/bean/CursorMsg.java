package com.micsig.tbook.tbookscope.wavezone.bean;   // 波形显示区域Bean包，包含数据传输对象类

import android.graphics.Rect;   // 导入Android矩形类，用于表示光标的位置区域

/**
 * 光标消息数据Bean类 - 存储光标的可见性和位置信息
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    光标消息数据Bean架构                          │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │  ┌─────────────────┐                                           │
 * │  │ CursorManage    │                                           │
 * │  │ (光标管理器)     │                                           │
 * │  └────────┬────────┘                                           │
 * │           │                                                     │
 * │           │ 创建/更新CursorMsg                                  │
 * │           │ setRow1Visible()                                    │
 * │           │ setRow1Position()                                   │
 * │           ▼                                                     │
 * │  ┌─────────────────────────────────────────────┐               │
 * │  │           CursorMsg                          │               │
 * │  │         (光标消息数据Bean)                    │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  可见性标志                          │   │               │
 * │  │  │  - row1Visible: 水平光标线1可见      │   │               │
 * │  │  │  - row2Visible: 水平光标线2可见      │   │               │
 * │  │  │  - col1Visible: 垂直光标线1可见      │   │               │
 * │  │  │  - col2Visible: 垂直光标线2可见      │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  位置矩形区域                        │   │               │
 * │  │  │  - row1Position: 水平光标线1位置     │   │               │
 * │  │  │  - row2Position: 水平光标线2位置     │   │               │
 * │  │  │  - col1Position: 垂直光标线1位置     │   │               │
 * │  │  │  - col2Position: 垂直光标线2位置     │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  └─────────────────────────────────────────────┘               │
 * │                       │                                       │
 * │                       ▼                                       │
 * │           ┌─────────────────────┐                             │
 * │           │  WaveZoneDisplay    │                             │
 * │           │  (波形显示区域)      │                             │
 * │           │  根据CursorMsg绘制  │                             │
 * │           └─────────────────────┘                             │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * <h3>模块定位</h3>
 * <p>本类是波形显示区域光标系统的数据传输对象（DTO），用于在光标管理器和
 * 波形显示区域之间传递光标的可见性和位置信息。这是一个纯数据类，
 * 不包含任何业务逻辑，只负责数据的存储和传递。</p>
 * 
 * <h3>核心职责</h3>
 * <ul>
 *   <li><b>可见性存储</b>：存储4条光标线的可见性状态</li>
 *   <li><b>位置存储</b>：存储4条光标线的位置矩形区域</li>
 *   <li><b>数据传递</b>：作为光标管理器和显示区域之间的数据载体</li>
 * </ul>
 * 
 * <h3>光标类型说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │                    光标类型详解                                │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  示波器光标系统包含4条光标线：                                  │
 * │                                                               │
 * │  ┌─────────────────────────────────────────────┐             │
 * │  │              波形显示区域                    │             │
 * │  │                                             │             │
 * │  │  col1Position    col2Position               │             │
 * │  │      │              │                       │             │
 * │  │      │    ΔT        │                       │             │
 * │  │      │←───→         │                       │             │
 * │  │      │              │                       │             │
 * │  │  ────┼──────────────┼────  row1Position     │             │
 * │  │      │              │      (水平光标线1)    │             │
 * │  │      │    ΔV        │                       │             │
 * │  │      │←───→         │                       │             │
 * │  │  ────┼──────────────┼────  row2Position     │             │
 * │  │      │              │      (水平光标线2)    │             │
 * │  │      │              │                       │             │
 * │  │  (垂直光标线1)  (垂直光标线2)                │             │
 * │  │                                             │             │
 * │  └─────────────────────────────────────────────┘             │
 * │                                                               │
 * │  光标线分类：                                                  │
 * │    - row（行）：水平光标线，用于测量电压/幅度差（ΔV）          │
 * │    - col（列）：垂直光标线，用于测量时间/频率差（ΔT）          │
 * │                                                               │
 * │  光标线编号：                                                  │
 * │    - row1/col1：第一条光标线（参考线）                        │
 * │    - row2/col2：第二条光标线（测量线）                        │
 * │                                                               │
 * │  测量功能：                                                    │
 * │    - 水平光标（row）：测量电压差、幅度差                       │
 * │    - 垂直光标（col）：测量时间差、频率                         │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>位置矩形说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              Rect位置矩形详解                                  │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  Rect矩形结构：                                                │
 * │    ┌───────────────────────────────────────┐                 │
 * │    │  left    top                          │                 │
 * │    │    ┌─────────────────────┐            │                 │
 * │    │    │                     │            │                 │
 * │    │    │     光标线区域      │  right     │                 │
 * │    │    │                     │            │                 │
 * │    │    └─────────────────────┘            │                 │
 * │    │                          bottom       │                 │
 * │    └───────────────────────────────────────┘                 │
 * │                                                               │
 * │  水平光标线（row）的Rect：                                     │
 * │    - left/right: 光标线的水平范围                             │
 * │    - top/bottom: 光标线的垂直位置（通常为细线）               │
 * │                                                               │
 * │  垂直光标线（col）的Rect：                                     │
 * │    - left/right: 光标线的水平位置（通常为细线）               │
 * │    - top/bottom: 光标线的垂直范围                             │
 * │                                                               │
 * │  设置方法参数：                                                │
 * │    setRow1Position(x, y, w, h)                                │
 * │      - x: left坐标                                            │
 * │      - y: top坐标                                             │
 * │      - w: 宽度（right = x + w）                               │
 * │      - h: 高度（bottom = y + h）                              │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>使用场景</h3>
 * <ul>
 *   <li><b>光标测量</b>：用户启用光标测量功能时，存储光标状态</li>
 *   <li><b>光标显示</b>：波形显示区域根据CursorMsg绘制光标线</li>
 *   <li><b>光标交互</b>：用户拖动光标线时，更新位置信息</li>
 *   <li><b>光标隐藏</b>：关闭光标测量时，设置可见性为false</li>
 * </ul>
 * 
 * <h3>依赖关系</h3>
 * <ul>
 *   <li>{@link Rect} - Android矩形类，用于表示位置区域</li>
 *   <li>{@link com.micsig.tbook.tbookscope.wavezone.display.CursorManage} - 光标管理器，创建和更新CursorMsg</li>
 *   <li>{@link com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_YT} - YT模式波形显示，使用CursorMsg绘制光标</li>
 *   <li>{@link com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_XY} - XY模式波形显示，使用CursorMsg绘制光标</li>
 * </ul>
 * 
 * <h3>数据流向</h3>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │              CursorMsg数据流向                               │
 * ├─────────────────────────────────────────────────────────────┤
 * │                                                             │
 * │  用户操作光标                                                │
 * │       │                                                     │
 * │       ▼                                                     │
 * │  CursorManage（光标管理器）                                  │
 * │       │                                                     │
 * │       │ 创建/更新CursorMsg                                  │
 * │       │ setRow1Visible(true)                                │
 * │       │ setRow1Position(x, y, w, h)                         │
 * │       ▼                                                     │
 * │  CursorMsg（数据Bean）                                       │
 * │       │                                                     │
 * │       │ 传递给显示区域                                       │
 * │       ▼                                                     │
 * │  WaveZoneDisplay（波形显示区域）                             │
 * │       │                                                     │
 * │       │ 根据CursorMsg绘制光标线                             │
 * │       │ if(row1Visible) drawRect(row1Position)              │
 * │       ▼                                                     │
 * │  屏幕显示光标                                                │
 * │                                                             │
 * └─────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * @see Rect
 * @see com.micsig.tbook.tbookscope.wavezone.display.CursorManage
 * @see com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_YT
 * @see com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_XY
 * @author Micsig智能示波器团队
 * @version 2.0
 * @since 1.0
 */
public class CursorMsg {   // 光标消息数据Bean类：存储光标的可见性和位置信息

    /**
     * 第一条水平光标线可见性标志
     * 
     * <p>表示第一条水平光标线（row1）是否可见。
     * 水平光标线用于测量电压差或幅度差。</p>
     * 
     * <h4>取值说明</h4>
     * <ul>
     *   <li><b>true</b>: 光标线可见，在波形显示区域绘制</li>
     *   <li><b>false</b>: 光标线不可见，不绘制</li>
     * </ul>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>用户启用光标测量功能时设置为true</li>
     *   <li>用户关闭光标测量功能时设置为false</li>
     *   <li>只启用垂直光标测量时，水平光标可见性为false</li>
     * </ul>
     */
    private boolean row1Visible;   // 第一条水平光标线（row1）的可见性标志：true=可见，false=不可见
    
    /**
     * 第二条水平光标线可见性标志
     * 
     * <p>表示第二条水平光标线（row2）是否可见。
     * 水平光标线用于测量电压差或幅度差。</p>
     * 
     * <h4>取值说明</h4>
     * <ul>
     *   <li><b>true</b>: 光标线可见，在波形显示区域绘制</li>
     *   <li><b>false</b>: 光标线不可见，不绘制</li>
     * </ul>
     * 
     * <h4>测量原理</h4>
     * <p>两条水平光标线（row1和row2）配合使用，测量两点之间的电压差：
     * ΔV = V(row2) - V(row1)</p>
     */
    private boolean row2Visible;   // 第二条水平光标线（row2）的可见性标志：true=可见，false=不可见
    
    /**
     * 第一条垂直光标线可见性标志
     * 
     * <p>表示第一条垂直光标线（col1）是否可见。
     * 垂直光标线用于测量时间差或频率。</p>
     * 
     * <h4>取值说明</h4>
     * <ul>
     *   <li><b>true</b>: 光标线可见，在波形显示区域绘制</li>
     *   <li><b>false</b>: 光标线不可见，不绘制</li>
     * </ul>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>用户启用光标测量功能时设置为true</li>
     *   <li>用户关闭光标测量功能时设置为false</li>
     *   <li>只启用水平光标测量时，垂直光标可见性为false</li>
     * </ul>
     */
    private boolean col1Visible;   // 第一条垂直光标线（col1）的可见性标志：true=可见，false=不可见
    
    /**
     * 第二条垂直光标线可见性标志
     * 
     * <p>表示第二条垂直光标线（col2）是否可见。
     * 垂直光标线用于测量时间差或频率。</p>
     * 
     * <h4>取值说明</h4>
     * <ul>
     *   <li><b>true</b>: 光标线可见，在波形显示区域绘制</li>
     *   <li><b>false</b>: 光标线不可见，不绘制</li>
     * </ul>
     * 
     * <h4>测量原理</h4>
     * <p>两条垂直光标线（col1和col2）配合使用，测量两点之间的时间差：
     * ΔT = T(col2) - T(col1)
     * 频率 = 1 / ΔT</p>
     */
    private boolean col2Visible;   // 第二条垂直光标线（col2）的可见性标志：true=可见，false=不可见

    /**
     * 第一条水平光标线的位置矩形区域
     * 
     * <p>存储第一条水平光标线（row1）在波形显示区域的位置。
     * 使用Android Rect类表示矩形区域。</p>
     * 
     * <h4>Rect属性说明</h4>
     * <ul>
     *   <li><b>left</b>: 光标线左边界x坐标</li>
     *   <li><b>top</b>: 光标线上边界y坐标</li>
     *   <li><b>right</b>: 光标线右边界x坐标</li>
     *   <li><b>bottom</b>: 光标线下边界y坐标</li>
     * </ul>
     * 
     * <h4>水平光标线特点</h4>
     * <p>水平光标线通常为细线，高度（bottom-top）较小，
     * 水平范围（right-left）覆盖整个波形显示宽度。</p>
     */
    private Rect row1Position=new Rect();   // 第一条水平光标线（row1）的位置矩形：初始化为空矩形
    
    /**
     * 第二条水平光标线的位置矩形区域
     * 
     * <p>存储第二条水平光标线（row2）在波形显示区域的位置。
     * 使用Android Rect类表示矩形区域。</p>
     * 
     * <h4>与row1Position的关系</h4>
     * <p>row1Position和row2Position配合使用，定义两个水平位置，
     * 用于测量两点之间的电压差。</p>
     */
    private Rect row2Position=new Rect();   // 第二条水平光标线（row2）的位置矩形：初始化为空矩形
    
    /**
     * 第一条垂直光标线的位置矩形区域
     * 
     * <p>存储第一条垂直光标线（col1）在波形显示区域的位置。
     * 使用Android Rect类表示矩形区域。</p>
     * 
     * <h4>垂直光标线特点</h4>
     * <p>垂直光标线通常为细线，宽度（right-left）较小，
     * 垂直范围（bottom-top）覆盖整个波形显示高度。</p>
     */
    private Rect col1Position=new Rect();   // 第一条垂直光标线（col1）的位置矩形：初始化为空矩形
    
    /**
     * 第二条垂直光标线的位置矩形区域
     * 
     * <p>存储第二条垂直光标线（col2）在波形显示区域的位置。
     * 使用Android Rect类表示矩形区域。</p>
     * 
     * <h4>与col1Position的关系</h4>
     * <p>col1Position和col2Position配合使用，定义两个垂直位置，
     * 用于测量两点之间的时间差。</p>
     */
    private Rect col2Position=new Rect();   // 第二条垂直光标线（col2）的位置矩形：初始化为空矩形

    /**
     * 获取第一条水平光标线的可见性
     * 
     * <p>返回第一条水平光标线（row1）是否可见。</p>
     * 
     * <h4>返回值说明</h4>
     * <ul>
     *   <li><b>true</b>: 光标线可见，已启用光标测量</li>
     *   <li><b>false</b>: 光标线不可见，未启用或已关闭</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <p>波形显示区域在绘制光标前调用此方法，判断是否需要绘制第一条水平光标线。</p>
     * 
     * @return 第一条水平光标线的可见性，true=可见，false=不可见
     */
    public boolean isRow1Visible() {   // 方法：获取第一条水平光标线的可见性
        return row1Visible;   // 返回row1Visible属性值
    }   // isRow1Visible方法结束

    /**
     * 设置第一条水平光标线的可见性
     * 
     * <p>设置第一条水平光标线（row1）是否可见。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>row1Visible</td><td>boolean</td><td>可见性标志，true=可见，false=不可见</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>用户启用水平光标测量时设置为true</li>
     *   <li>用户关闭水平光标测量时设置为false</li>
     *   <li>切换光标测量类型时更新可见性</li>
     * </ul>
     * 
     * @param row1Visible 第一条水平光标线的可见性，true=可见，false=不可见
     */
    public void setRow1Visible(boolean row1Visible) {   // 方法：设置第一条水平光标线的可见性
        this.row1Visible = row1Visible;   // 设置row1Visible属性值
    }   // setRow1Visible方法结束

    /**
     * 获取第二条水平光标线的可见性
     * 
     * <p>返回第二条水平光标线（row2）是否可见。</p>
     * 
     * <h4>返回值说明</h4>
     * <ul>
     *   <li><b>true</b>: 光标线可见，已启用光标测量</li>
     *   <li><b>false</b>: 光标线不可见，未启用或已关闭</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <p>波形显示区域在绘制光标前调用此方法，判断是否需要绘制第二条水平光标线。</p>
     * 
     * @return 第二条水平光标线的可见性，true=可见，false=不可见
     */
    public boolean isRow2Visible() {   // 方法：获取第二条水平光标线的可见性
        return row2Visible;   // 返回row2Visible属性值
    }   // isRow2Visible方法结束

    /**
     * 设置第二条水平光标线的可见性
     * 
     * <p>设置第二条水平光标线（row2）是否可见。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>row2Visible</td><td>boolean</td><td>可见性标志，true=可见，false=不可见</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>用户启用水平光标测量时设置为true</li>
     *   <li>用户关闭水平光标测量时设置为false</li>
     *   <li>切换光标测量类型时更新可见性</li>
     * </ul>
     * 
     * @param row2Visible 第二条水平光标线的可见性，true=可见，false=不可见
     */
    public void setRow2Visible(boolean row2Visible) {   // 方法：设置第二条水平光标线的可见性
        this.row2Visible = row2Visible;   // 设置row2Visible属性值
    }   // setRow2Visible方法结束

    /**
     * 获取第一条垂直光标线的可见性
     * 
     * <p>返回第一条垂直光标线（col1）是否可见。</p>
     * 
     * <h4>返回值说明</h4>
     * <ul>
     *   <li><b>true</b>: 光标线可见，已启用光标测量</li>
     *   <li><b>false</b>: 光标线不可见，未启用或已关闭</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <p>波形显示区域在绘制光标前调用此方法，判断是否需要绘制第一条垂直光标线。</p>
     * 
     * @return 第一条垂直光标线的可见性，true=可见，false=不可见
     */
    public boolean isCol1Visible() {   // 方法：获取第一条垂直光标线的可见性
        return col1Visible;   // 返回col1Visible属性值
    }   // isCol1Visible方法结束

    /**
     * 设置第一条垂直光标线的可见性
     * 
     * <p>设置第一条垂直光标线（col1）是否可见。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>col1Visible</td><td>boolean</td><td>可见性标志，true=可见，false=不可见</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>用户启用垂直光标测量时设置为true</li>
     *   <li>用户关闭垂直光标测量时设置为false</li>
     *   <li>切换光标测量类型时更新可见性</li>
     * </ul>
     * 
     * @param col1Visible 第一条垂直光标线的可见性，true=可见，false=不可见
     */
    public void setCol1Visible(boolean col1Visible) {   // 方法：设置第一条垂直光标线的可见性
        this.col1Visible = col1Visible;   // 设置col1Visible属性值
    }   // setCol1Visible方法结束

    /**
     * 获取第二条垂直光标线的可见性
     * 
     * <p>返回第二条垂直光标线（col2）是否可见。</p>
     * 
     * <h4>返回值说明</h4>
     * <ul>
     *   <li><b>true</b>: 光标线可见，已启用光标测量</li>
     *   <li><b>false</b>: 光标线不可见，未启用或已关闭</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <p>波形显示区域在绘制光标前调用此方法，判断是否需要绘制第二条垂直光标线。</p>
     * 
     * @return 第二条垂直光标线的可见性，true=可见，false=不可见
     */
    public boolean isCol2Visible() {   // 方法：获取第二条垂直光标线的可见性
        return col2Visible;   // 返回col2Visible属性值
    }   // isCol2Visible方法结束

    /**
     * 设置第二条垂直光标线的可见性
     * 
     * <p>设置第二条垂直光标线（col2）是否可见。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>col2Visible</td><td>boolean</td><td>可见性标志，true=可见，false=不可见</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>用户启用垂直光标测量时设置为true</li>
     *   <li>用户关闭垂直光标测量时设置为false</li>
     *   <li>切换光标测量类型时更新可见性</li>
     * </ul>
     * 
     * @param col2Visible 第二条垂直光标线的可见性，true=可见，false=不可见
     */
    public void setCol2Visible(boolean col2Visible) {   // 方法：设置第二条垂直光标线的可见性
        this.col2Visible = col2Visible;   // 设置col2Visible属性值
    }   // setCol2Visible方法结束

    /**
     * 获取第一条水平光标线的位置矩形
     * 
     * <p>返回第一条水平光标线（row1）在波形显示区域的位置矩形。</p>
     * 
     * <h4>返回值说明</h4>
     * <p>返回Android Rect对象，包含光标线的位置信息：
     * - left: 光标线左边界x坐标
     * - top: 光标线上边界y坐标
     * - right: 光标线右边界x坐标
     * - bottom: 光标线下边界y坐标</p>
     * 
     * <h4>调用时机</h4>
     * <p>波形显示区域在绘制第一条水平光标线时调用此方法，获取光标线的绘制位置。</p>
     * 
     * @return 第一条水平光标线的位置矩形（Rect对象）
     */
    public Rect getRow1Position() {   // 方法：获取第一条水平光标线的位置矩形
        return row1Position;   // 返回row1Position矩形对象
    }   // getRow1Position方法结束

    /**
     * 设置第一条水平光标线的位置
     * 
     * <p>设置第一条水平光标线（row1）在波形显示区域的位置。
     * 通过x、y、w、h参数设置矩形区域。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>x</td><td>int</td><td>矩形左边界坐标（left）</td></tr>
     *   <tr><td>y</td><td>int</td><td>矩形上边界坐标（top）</td></tr>
     *   <tr><td>w</td><td>int</td><td>矩形宽度（right = x + w）</td></tr>
     *   <tr><td>h</td><td>int</td><td>矩形高度（bottom = y + h）</td></tr>
     * </table>
     * 
     * <h4>位置计算公式</h4>
     * <pre>
     *   left = x
     *   top = y
     *   right = x + w
     *   bottom = y + h
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>用户拖动第一条水平光标线时更新位置</li>
     *   <li>光标管理器初始化光标位置时设置</li>
     *   <li>波形显示区域大小改变时重新计算位置</li>
     * </ul>
     * 
     * @param x 矩形左边界坐标（left）
     * @param y 矩形上边界坐标（top）
     * @param w 矩形宽度
     * @param h 矩形高度
     */
    public void setRow1Position(int x,int y,int w,int h) {   // 方法：设置第一条水平光标线的位置（通过x、y、w、h参数）
        this.row1Position.left =x;   // 设置矩形左边界：left = x
        this.row1Position.top=y;   // 设置矩形上边界：top = y
        this.row1Position.right=x+w;   // 设置矩形右边界：right = x + w
        this.row1Position.bottom=y+h;   // 设置矩形下边界：bottom = y + h
    }   // setRow1Position方法结束

    /**
     * 获取第二条水平光标线的位置矩形
     * 
     * <p>返回第二条水平光标线（row2）在波形显示区域的位置矩形。</p>
     * 
     * <h4>返回值说明</h4>
     * <p>返回Android Rect对象，包含光标线的位置信息：
     * - left: 光标线左边界x坐标
     * - top: 光标线上边界y坐标
     * - right: 光标线右边界x坐标
     * - bottom: 光标线下边界y坐标</p>
     * 
     * <h4>调用时机</h4>
     * <p>波形显示区域在绘制第二条水平光标线时调用此方法，获取光标线的绘制位置。</p>
     * 
     * @return 第二条水平光标线的位置矩形（Rect对象）
     */
    public Rect getRow2Position() {   // 方法：获取第二条水平光标线的位置矩形
        return row2Position;   // 返回row2Position矩形对象
    }   // getRow2Position方法结束

    /**
     * 设置第二条水平光标线的位置
     * 
     * <p>设置第二条水平光标线（row2）在波形显示区域的位置。
     * 通过x、y、w、h参数设置矩形区域。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>x</td><td>int</td><td>矩形左边界坐标（left）</td></tr>
     *   <tr><td>y</td><td>int</td><td>矩形上边界坐标（top）</td></tr>
     *   <tr><td>w</td><td>int</td><td>矩形宽度（right = x + w）</td></tr>
     *   <tr><td>h</td><td>int</td><td>矩形高度（bottom = y + h）</td></tr>
     * </table>
     * 
     * <h4>位置计算公式</h4>
     * <pre>
     *   left = x
     *   top = y
     *   right = x + w
     *   bottom = y + h
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>用户拖动第二条水平光标线时更新位置</li>
     *   <li>光标管理器初始化光标位置时设置</li>
     *   <li>波形显示区域大小改变时重新计算位置</li>
     * </ul>
     * 
     * @param x 矩形左边界坐标（left）
     * @param y 矩形上边界坐标（top）
     * @param w 矩形宽度
     * @param h 矩形高度
     */
    public void setRow2Position(int x,int y,int w,int h)   // 方法：设置第二条水平光标线的位置（通过x、y、w、h参数）
    {   // 方法体开始
        this.row2Position.left = x;   // 设置矩形左边界：left = x
        this.row2Position.top=y;   // 设置矩形上边界：top = y
        this.row2Position.right=x+w;   // 设置矩形右边界：right = x + w
        this.row2Position.bottom=y+h;   // 设置矩形下边界：bottom = y + h
    }   // setRow2Position方法结束

    /**
     * 获取第一条垂直光标线的位置矩形
     * 
     * <p>返回第一条垂直光标线（col1）在波形显示区域的位置矩形。</p>
     * 
     * <h4>返回值说明</h4>
     * <p>返回Android Rect对象，包含光标线的位置信息：
     * - left: 光标线左边界x坐标
     * - top: 光标线上边界y坐标
     * - right: 光标线右边界x坐标
     * - bottom: 光标线下边界y坐标</p>
     * 
     * <h4>调用时机</h4>
     * <p>波形显示区域在绘制第一条垂直光标线时调用此方法，获取光标线的绘制位置。</p>
     * 
     * @return 第一条垂直光标线的位置矩形（Rect对象）
     */
    public Rect getCol1Position() {   // 方法：获取第一条垂直光标线的位置矩形
        return col1Position;   // 返回col1Position矩形对象
    }   // getCol1Position方法结束

    /**
     * 设置第一条垂直光标线的位置
     * 
     * <p>设置第一条垂直光标线（col1）在波形显示区域的位置。
     * 通过x、y、w、h参数设置矩形区域。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>x</td><td>int</td><td>矩形左边界坐标（left）</td></tr>
     *   <tr><td>y</td><td>int</td><td>矩形上边界坐标（top）</td></tr>
     *   <tr><td>w</td><td>int</td><td>矩形宽度（right = x + w）</td></tr>
     *   <tr><td>h</td><td>int</td><td>矩形高度（bottom = y + h）</td></tr>
     * </table>
     * 
     * <h4>位置计算公式</h4>
     * <pre>
     *   left = x
     *   top = y
     *   right = x + w
     *   bottom = y + h
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>用户拖动第一条垂直光标线时更新位置</li>
     *   <li>光标管理器初始化光标位置时设置</li>
     *   <li>波形显示区域大小改变时重新计算位置</li>
     * </ul>
     * 
     * @param x 矩形左边界坐标（left）
     * @param y 矩形上边界坐标（top）
     * @param w 矩形宽度
     * @param h 矩形高度
     */
    public void setCol1Position(int x,int y,int w,int h) {   // 方法：设置第一条垂直光标线的位置（通过x、y、w、h参数）
        this.col1Position.left = x;   // 设置矩形左边界：left = x
        this.col1Position.top=y;   // 设置矩形上边界：top = y
        this.col1Position.right=x+w;   // 设置矩形右边界：right = x + w
        this.col1Position.bottom=y+h;   // 设置矩形下边界：bottom = y + h
    }   // setCol1Position方法结束

    /**
     * 获取第二条垂直光标线的位置矩形
     * 
     * <p>返回第二条垂直光标线（col2）在波形显示区域的位置矩形。</p>
     * 
     * <h4>返回值说明</h4>
     * <p>返回Android Rect对象，包含光标线的位置信息：
     * - left: 光标线左边界x坐标
     * - top: 光标线上边界y坐标
     * - right: 光标线右边界x坐标
     * - bottom: 光标线下边界y坐标</p>
     * 
     * <h4>调用时机</h4>
     * <p>波形显示区域在绘制第二条垂直光标线时调用此方法，获取光标线的绘制位置。</p>
     * 
     * @return 第二条垂直光标线的位置矩形（Rect对象）
     */
    public Rect getCol2Position() {   // 方法：获取第二条垂直光标线的位置矩形
        return col2Position;   // 返回col2Position矩形对象
    }   // getCol2Position方法结束

    /**
     * 设置第二条垂直光标线的位置
     * 
     * <p>设置第二条垂直光标线（col2）在波形显示区域的位置。
     * 通过x、y、w、h参数设置矩形区域。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>x</td><td>int</td><td>矩形左边界坐标（left）</td></tr>
     *   <tr><td>y</td><td>int</td><td>矩形上边界坐标（top）</td></tr>
     *   <tr><td>w</td><td>int</td><td>矩形宽度（right = x + w）</td></tr>
     *   <tr><td>h</td><td>int</td><td>矩形高度（bottom = y + h）</td></tr>
     * </table>
     * 
     * <h4>位置计算公式</h4>
     * <pre>
     *   left = x
     *   top = y
     *   right = x + w
     *   bottom = y + h
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>用户拖动第二条垂直光标线时更新位置</li>
     *   <li>光标管理器初始化光标位置时设置</li>
     *   <li>波形显示区域大小改变时重新计算位置</li>
     * </ul>
     * 
     * @param x 矩形左边界坐标（left）
     * @param y 矩形上边界坐标（top）
     * @param w 矩形宽度
     * @param h 矩形高度
     */
    public void setCol2Position(int x,int y,int w,int h) {   // 方法：设置第二条垂直光标线的位置（通过x、y、w、h参数）
        this.col2Position.left = x;   // 设置矩形左边界：left = x
        this.col2Position.top=y;   // 设置矩形上边界：top = y
        this.col2Position.right=x+w;   // 设置矩形右边界：right = x + w
        this.col2Position.bottom=y+h;   // 设置矩形下边界：bottom = y + h
    }   // setCol2Position方法结束
}   // CursorMsg类结束