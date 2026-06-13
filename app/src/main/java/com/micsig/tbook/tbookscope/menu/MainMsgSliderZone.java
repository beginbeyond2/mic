package com.micsig.tbook.tbookscope.menu;  // 定义包名：示波器菜单管理模块

/**
 * 主界面滑动区域定义类 - 定义触摸屏滑动操作的区域标识
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：tbookscope.menu（示波器菜单管理模块）</li>
 *   <li>架构层级：UI层 - 触摸事件处理</li>
 *   <li>设计模式：常量类 + 数据传输对象（DTO）</li>
 *   <li>职责类型：定义滑动区域常量、封装滑动区域状态</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>定义主界面各区域的滑动操作标识</li>
 *   <li>封装滑动区域的索引和使能状态</li>
 *   <li>为触摸事件处理提供区域判断依据</li>
 * </ul>
 * 
 * <p><b>滑动区域架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   主界面滑动区域布局                                                       │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │                    MENUSLIP_TOP（顶部区域）                       │   │
 * │   │                    触发：顶部菜单栏滑动                            │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   ┌──────────┐                                        ┌──────────────┐   │
 * │   │          │                                        │              │   │
 * │   │ MENUSLIP │                                        │   通道区域    │   │
 * │   │  _LEFT   │         波形显示区域                    │              │   │
 * │   │（左侧）   │                                        │ MENUSLIP_CHx │   │
 * │   │          │                                        │ MENUSLIP_MATHx│  │
 * │   │ 触发：   │                                        │ MENUSLIP_REFx │  │
 * │   │ 左侧菜单 │                                        │ MENUSLIP_Sx  │   │
 * │   │          │                                        │              │   │
 * │   └──────────┘                                        └──────────────┘   │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │                 MENUSLIP_BOTTOM（底部区域）                       │   │
 * │   │                    触发：底部菜单栏滑动                            │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>滑动区域常量说明：</b>
 * <pre>
 * 区域类型              常量名              数值    用途说明
 * ─────────────────────────────────────────────────────────────
 * 系统区域：
 *   左侧菜单            MENUSLIP_LEFT       0x01    滑动打开左侧设置菜单
 *   顶部菜单            MENUSLIP_TOP        0x02    滑动打开顶部功能菜单
 *   底部菜单            MENUSLIP_BOTTOM     0x03    滑动打开底部测量菜单
 * 
 * 模拟通道区域（CH1-CH8）：
 *   CH1通道            MENUSLIP_CH1        0x04    CH1通道位置滑动
 *   CH2通道            MENUSLIP_CH2        0x05    CH2通道位置滑动
 *   ...                ...                 ...     ...
 *   CH8通道            MENUSLIP_CH8        0x0B    CH8通道位置滑动
 * 
 * 数学通道区域（MATH1-MATH8）：
 *   MATH1通道          MENUSLIP_MATH1      0x0C    MATH1通道位置滑动
 *   MATH2通道          MENUSLIP_MATH2      0x0D    MATH2通道位置滑动
 *   ...                ...                 ...     ...
 *   MATH8通道          MENUSLIP_MATH8      0x13    MATH8通道位置滑动
 * 
 * 参考通道区域（REF1-REF8）：
 *   REF1通道           MENUSLIP_REF1       0x14    REF1通道位置滑动
 *   REF2通道           MENUSLIP_REF2       0x15    REF2通道位置滑动
 *   ...                ...                 ...     ...
 *   REF8通道           MENUSLIP_REF8       0x1B    REF8通道位置滑动
 * 
 * 串口通道区域（S1-S4）：
 *   S1通道             MENUSLIP_S1         0x1C    S1串口解码位置滑动
 *   S2通道             MENUSLIP_S2         0x1D    S2串口解码位置滑动
 *   S3通道             MENUSLIP_S3         0x1E    S3串口解码位置滑动
 *   S4通道             MENUSLIP_S4         0x1F    S4串口解码位置滑动
 * </pre>
 * 
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>触摸事件处理：判断触摸点所在的滑动区域</li>
 *   <li>手势识别：根据区域类型执行不同的滑动响应</li>
 *   <li>通道位置调整：在通道区域滑动时调整通道垂直位置</li>
 *   <li>菜单弹出：在系统区域滑动时弹出对应菜单</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>被依赖：MainMsgHandler（主界面消息处理器）</li>
 *   <li>被依赖：TouchEventHandler（触摸事件处理器）</li>
 *   <li>被依赖：GestureDetector（手势检测器）</li>
 * </ul>
 * 
 * @author liwb
 * @version 1.0
 * @since 2018/8/30
 * @see MainMsgHandler 主界面消息处理器
 */
public class MainMsgSliderZone {
    
    /** 滑动区域常量：左侧菜单区域 - 滑动打开左侧设置菜单 */
    public static final int MENUSLIP_LEFT = 0x01;  // 左侧菜单区域标识
    
    /** 滑动区域常量：顶部菜单区域 - 滑动打开顶部功能菜单 */
    public static final int MENUSLIP_TOP = MENUSLIP_LEFT + 1;  // 顶部菜单区域标识（0x02）
    
    /** 滑动区域常量：底部菜单区域 - 滑动打开底部测量菜单 */
    public static final int MENUSLIP_BOTTOM = MENUSLIP_TOP + 1;  // 底部菜单区域标识（0x03）
    
    /** 滑动区域常量：CH1通道区域 - 调整CH1垂直位置 */
    public static final int MENUSLIP_CH1 = MENUSLIP_BOTTOM + 1;  // CH1通道区域标识（0x04）
    
    /** 滑动区域常量：CH2通道区域 - 调整CH2垂直位置 */
    public static final int MENUSLIP_CH2 = MENUSLIP_CH1 + 1;  // CH2通道区域标识（0x05）
    
    /** 滑动区域常量：CH3通道区域 - 调整CH3垂直位置 */
    public static final int MENUSLIP_CH3 = MENUSLIP_CH2 + 1;  // CH3通道区域标识（0x06）
    
    /** 滑动区域常量：CH4通道区域 - 调整CH4垂直位置 */
    public static final int MENUSLIP_CH4 = MENUSLIP_CH3 + 1;  // CH4通道区域标识（0x07）
    
    /** 滑动区域常量：CH5通道区域 - 调整CH5垂直位置 */
    public static final int MENUSLIP_CH5 = MENUSLIP_CH4 + 1;  // CH5通道区域标识（0x08）
    
    /** 滑动区域常量：CH6通道区域 - 调整CH6垂直位置 */
    public static final int MENUSLIP_CH6 = MENUSLIP_CH5 + 1;  // CH6通道区域标识（0x09）
    
    /** 滑动区域常量：CH7通道区域 - 调整CH7垂直位置 */
    public static final int MENUSLIP_CH7 = MENUSLIP_CH6 + 1;  // CH7通道区域标识（0x0A）
    
    /** 滑动区域常量：CH8通道区域 - 调整CH8垂直位置 */
    public static final int MENUSLIP_CH8 = MENUSLIP_CH7 + 1;  // CH8通道区域标识（0x0B）
    
    /** 滑动区域常量：MATH1通道区域 - 调整MATH1垂直位置 */
    public static final int MENUSLIP_MATH1 = MENUSLIP_CH8 + 1;  // MATH1通道区域标识（0x0C）
    
    /** 滑动区域常量：MATH2通道区域 - 调整MATH2垂直位置 */
    public static final int MENUSLIP_MATH2 = MENUSLIP_MATH1 + 1;  // MATH2通道区域标识（0x0D）
    
    /** 滑动区域常量：MATH3通道区域 - 调整MATH3垂直位置 */
    public static final int MENUSLIP_MATH3 = MENUSLIP_MATH2 + 1;  // MATH3通道区域标识（0x0E）
    
    /** 滑动区域常量：MATH4通道区域 - 调整MATH4垂直位置 */
    public static final int MENUSLIP_MATH4 = MENUSLIP_MATH3 + 1;  // MATH4通道区域标识（0x0F）
    
    /** 滑动区域常量：MATH5通道区域 - 调整MATH5垂直位置 */
    public static final int MENUSLIP_MATH5 = MENUSLIP_MATH4 + 1;  // MATH5通道区域标识（0x10）
    
    /** 滑动区域常量：MATH6通道区域 - 调整MATH6垂直位置 */
    public static final int MENUSLIP_MATH6 = MENUSLIP_MATH5 + 1;  // MATH6通道区域标识（0x11）
    
    /** 滑动区域常量：MATH7通道区域 - 调整MATH7垂直位置 */
    public static final int MENUSLIP_MATH7 = MENUSLIP_MATH6 + 1;  // MATH7通道区域标识（0x12）
    
    /** 滑动区域常量：MATH8通道区域 - 调整MATH8垂直位置 */
    public static final int MENUSLIP_MATH8 = MENUSLIP_MATH7 + 1;  // MATH8通道区域标识（0x13）
    
    /** 滑动区域常量：REF1通道区域 - 调整REF1垂直位置 */
    public static final int MENUSLIP_REF1 = MENUSLIP_MATH8 + 1;  // REF1通道区域标识（0x14）
    
    /** 滑动区域常量：REF2通道区域 - 调整REF2垂直位置 */
    public static final int MENUSLIP_REF2 = MENUSLIP_REF1 + 1;  // REF2通道区域标识（0x15）
    
    /** 滑动区域常量：REF3通道区域 - 调整REF3垂直位置 */
    public static final int MENUSLIP_REF3 = MENUSLIP_REF2 + 1;  // REF3通道区域标识（0x16）
    
    /** 滑动区域常量：REF4通道区域 - 调整REF4垂直位置 */
    public static final int MENUSLIP_REF4 = MENUSLIP_REF3 + 1;  // REF4通道区域标识（0x17）
    
    /** 滑动区域常量：REF5通道区域 - 调整REF5垂直位置 */
    public static final int MENUSLIP_REF5 = MENUSLIP_REF4 + 1;  // REF5通道区域标识（0x18）
    
    /** 滑动区域常量：REF6通道区域 - 调整REF6垂直位置 */
    public static final int MENUSLIP_REF6 = MENUSLIP_REF5 + 1;  // REF6通道区域标识（0x19）
    
    /** 滑动区域常量：REF7通道区域 - 调整REF7垂直位置 */
    public static final int MENUSLIP_REF7 = MENUSLIP_REF6 + 1;  // REF7通道区域标识（0x1A）
    
    /** 滑动区域常量：REF8通道区域 - 调整REF8垂直位置 */
    public static final int MENUSLIP_REF8 = MENUSLIP_REF7 + 1;  // REF8通道区域标识（0x1B）

    /** 滑动区域常量：S1串口解码区域 - 调整S1解码位置 */
    public static final int MENUSLIP_S1 = MENUSLIP_REF8 + 1;  // S1串口区域标识（0x1C）
    
    /** 滑动区域常量：S2串口解码区域 - 调整S2解码位置 */
    public static final int MENUSLIP_S2 = MENUSLIP_S1 + 1;  // S2串口区域标识（0x1D）
    
    /** 滑动区域常量：S3串口解码区域 - 调整S3解码位置 */
    public static final int MENUSLIP_S3 = MENUSLIP_S2 + 1;  // S3串口区域标识（0x1E）
    
    /** 滑动区域常量：S4串口解码区域 - 调整S4解码位置 */
    public static final int MENUSLIP_S4 = MENUSLIP_S3 + 1;  // S4串口区域标识（0x1F）

    /** 菜单索引：标识当前滑动区域对应的菜单或通道 */
    private int menuIndex;  // 菜单/通道索引，对应上述常量值
    
    /** 滑动使能标志：true表示允许滑动操作，false表示禁止滑动 */
    private boolean enableSlip;  // 滑动使能标志

    /**
     * 构造方法：初始化滑动区域配置
     * 
     * <p>创建滑动区域对象，指定区域索引和滑动使能状态。
     * 
     * <p><b>使用示例：</b>
     * <pre>
     * // 创建CH1通道的滑动区域，启用滑动
     * MainMsgSliderZone zone = new MainMsgSliderZone(MENUSLIP_CH1, true);
     * 
     * // 创建左侧菜单的滑动区域，禁用滑动
     * MainMsgSliderZone zone = new MainMsgSliderZone(MENUSLIP_LEFT, false);
     * </pre>
     * 
     * @param menuIndex 滑动区域索引，使用MENUSLIP_*常量
     * @param enableSlip 滑动使能标志，true表示允许滑动，false表示禁止滑动
     */
    public MainMsgSliderZone(int menuIndex, boolean enableSlip) {
        this.menuIndex = menuIndex;  // 保存菜单/通道索引
        this.enableSlip = enableSlip;  // 保存滑动使能标志
    }

    /**
     * 获取菜单/通道索引
     * 
     * <p>返回当前滑动区域对应的菜单或通道索引。
     * 返回值为MENUSLIP_*常量之一。
     * 
     * @return 菜单/通道索引
     */
    public int getMenuIndex() {
        return menuIndex;  // 返回菜单/通道索引
    }

    /**
     * 检查滑动是否使能
     * 
     * <p>返回当前区域的滑动操作是否被允许。
     * 
     * <p><b>返回值说明：</b>
     * <ul>
     *   <li>true: 允许在该区域执行滑动操作</li>
     *   <li>false: 禁止在该区域执行滑动操作</li>
     * </ul>
     * 
     * @return 滑动使能标志
     */
    public boolean isEnableSlip() {
        return enableSlip;  // 返回滑动使能标志
    }
}
