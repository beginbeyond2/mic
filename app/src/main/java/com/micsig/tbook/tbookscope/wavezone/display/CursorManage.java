package com.micsig.tbook.tbookscope.wavezone.display;


import android.graphics.Bitmap;                                    // 位图类，用于光标图标渲染
import android.graphics.drawable.BitmapDrawable;                   // 位图绘制容器
import android.util.Log;                                           // Android日志工具

import com.chillingvan.canvasgl.ICanvasGL;                         // OpenGL画布接口，用于GPU加速绘制
import com.micsig.base.Logger;                                     // 自定义日志工具
import com.micsig.tbook.scope.Event.EventBase;                     // 事件基类
import com.micsig.tbook.scope.Event.EventFactory;                  // 事件工厂，用于注册/分发事件
import com.micsig.tbook.scope.Event.EventUIObserver;               // 事件UI观察者接口
import com.micsig.tbook.scope.ScopeBase;                           // 示波器基础类，提供坐标转换系数
import com.micsig.tbook.scope.channel.ChannelFactory;              // 通道工厂，管理物理/数学/参考通道
import com.micsig.tbook.scope.channel.MathChannel;                 // 数学运算通道
import com.micsig.tbook.scope.channel.RefChannel;                  // 参考通道
import com.micsig.tbook.scope.horizontal.HorizontalAxis;           // 水平轴（时基）管理
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath;       // 数学通道的水平轴（FFT频率轴）
import com.micsig.tbook.scope.math.MathFFTWave;                    // FFT数学波形
import com.micsig.tbook.scope.math.MathWave;                       // 数学波形基类
import com.micsig.tbook.scope.measure.MeasureService;              // 测量服务，提供光标测量范围
import com.micsig.tbook.tbookscope.GlobalVar;                     // 全局变量，存储波形区域尺寸等
import com.micsig.tbook.tbookscope.LoadCache;                      // 缓存加载事件载体
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgChannel;  // 右侧滑菜单-通道消息
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgMath;     // 右侧滑菜单-数学运算消息
import com.micsig.tbook.tbookscope.rxjava.RxBus;                   // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;                  // RxJava事件枚举定义
import com.micsig.tbook.tbookscope.tools.Tools;                    // 工具类，提供时基/幅度到像素的转换
import com.micsig.tbook.tbookscope.util.App;                       // 应用上下文工具
import com.micsig.tbook.tbookscope.util.CacheUtil;                 // 缓存工具，持久化光标位置等配置
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;             // 工作模式接口（YT/XY/YTZOOM）
import com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_XY;    // XY波形区显示，提供滑动方向常量
import com.micsig.tbook.tbookscope.wavezone.WaveZoneDisplay_YT;    // YT波形区显示，提供滑动方向常量
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;          // 工作模式切换事件载体
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;        // 工作模式管理器
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage; // 测量管理器
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;       // 波形管理器，管理通道选择和位置
import com.micsig.tbook.ui.util.svg.SvgManager;                    // SVG渲染管理器
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;                   // SVG节点信息，光标图标的路径和颜色
import com.micsig.tbook.ui.wavezone.IWave;                         // 波形组件接口
import com.micsig.tbook.ui.wavezone.TChan;                         // 通道/光标类型常量定义

import io.reactivex.rxjava3.functions.Consumer;                    // RxJava消费者接口


/**
 * Created by liwb on 2017/11/6.
 * <p>
 * +===================================================================================================+
 * |                                          CursorManage                                            |
 * |                                           光标管理总控                                            |
 * +===================================================================================================+
 * |                                                                                                   |
 * | 【模块定位】                                                                                      |
 * |   示波器波形区域光标系统的总控制器，是光标功能对外暴露的唯一入口                                 |
 * |                                                                                                   |
 * | 【核心职责】                                                                                      |
 * |   1. 统一管理YT模式和XY模式下的光标创建、选择、移动、跟踪和测量                                 |
 * |   2. 根据当前工作模式（YT/XY/YTZOOM）将操作委托给对应的子管理器                                |
 * |   3. 处理光标跟踪逻辑（时基跟踪、幅度跟踪），确保光标随波形位置自动更新                       |
 * |   4. 管理光标图标资源（选中/未选中状态的位图）                                                  |
 * |   5. 订阅RxBus事件，响应缓存加载、工作模式切换、通道/数学运算变更                              |
 * |                                                                                                   |
 * | 【架构设计】                                                                                      |
 * |   采用门面模式（Facade）：CursorManage作为统一门面，内部委托给                                 |
 * |   CursorManage_YT和CursorManage_XY两个子管理器实现具体逻辑。                                    |
 * |   实现IWorkMode（工作模式切换）、ICursorManage（光标操作）、                                   |
 * |   IWaveControl（波形控制）三个接口。                                                             |
 * |                                                                                                   |
 * | 【数据流向】                                                                                      |
 * |   CacheUtil/RxBus → CursorManage → CursorManage_YT/XY →                                        |
 * |   Cursor_impIWave → Canvas绘制                                                                   |
 * |   光标位置变更 → CursorLabel → MeasureManage → 测量数据显示                                     |
 * |                                                                                                   |
 * | 【依赖关系】                                                                                      |
 * |   - CursorManage_YT：YT模式光标管理器                                                            |
 * |   - CursorManage_XY：XY模式光标管理器                                                            |
 * |   - WorkModeManage：工作模式管理器（判断当前YT/XY模式）                                         |
 * |   - WaveManage：波形管理器（获取当前通道和位置）                                                |
 * |   - MeasureService/MeasureManage：测量服务和测量管理                                             |
 * |   - CacheUtil：缓存工具（读取/保存光标位置配置）                                                |
 * |   - RxBus：事件总线（响应各种UI事件）                                                            |
 * |   - ChannelFactory：通道工厂（获取通道属性）                                                    |
 * |                                                                                                   |
 * | 【使用场景】                                                                                      |
 * |   - 用户通过UI操作显示/隐藏光标线                                                                |
 * |   - 用户通过触摸/旋钮选择并移动光标                                                              |
 * |   - 时基或幅度改变时自动跟踪光标位置                                                             |
 * |   - SCPI远程控制操作光标                                                                         |
 * |   - 加载缓存恢复光标位置                                                                         |
 * +===================================================================================================+
 */

public class CursorManage implements IWorkMode, ICursorManage, IWaveControl { // 实现工作模式、光标操作、波形控制三个接口
    private static final String TAG = "CursorManage";                        // 日志标签

    private CursorManage_XY cursorManage_xy;                                 // XY模式光标管理器实例
    private CursorManage_YT cursorManage_yt;                                 // YT模式光标管理器实例

    private int VerIndex = 0;                                                // 垂直光标选中索引：1=光标1, 2=光标2, 3=双光标跟踪
    private int HorIndex = 0;                                                // 水平光标选中索引：1=光标1, 2=光标2, 3=双光标跟踪
    private boolean enableCursorTrance = true;                                 // 是否启用光标跟踪功能，默认启用
    //光标改变，是否来自时基的改变
    private boolean CursorTrace = false;                                      // 光标跟踪标志，标识当前光标变化是否由时基/幅度改变引起
    //多光标移动时，改变位置，但是不改变X1，X2的间距。
    private boolean CursorTraceMultiMove = false;                              // 多光标跟踪移动标志，移动位置但保持间距不变
    //scpi操作的通道
    private int scpiChanIdx = -1;                                             // SCPI远程控制指定的通道索引，-1表示未指定

    private boolean isLabelNotFollowCursor = false;                          // 光标标签是否不跟随光标移动（固定位置）

    //region 单例创建
    private static class CursorManage_Holder {                               // 静态内部类实现懒加载单例
        private static final CursorManage instance = new CursorManage();     // 静态初始化单例实例
    }

    /**
     * 获取CursorManage单例实例
     *
     * @return CursorManage单例
     */
    public static final CursorManage getInstance() {
        return CursorManage_Holder.instance;                                 // 返回静态内部类持有的单例实例
    }
    //endregion


    //region  图片
    //通道，类型，状态
    public static final int Row1 = 0;                                        // 水平光标1的行索引
    public static final int Row2 = 1;                                        // 水平光标2的行索引
    public static final int Col1 = 2;                                        // 垂直光标1的列索引
    public static final int Col2 = 3;                                        // 垂直光标2的列索引
    public static final int Select = 0;                                      // 选中状态的位图索引
    public static final int NoSelect = 1;                                    // 未选中状态的位图索引
    private Bitmap[][] resBmp = new Bitmap[4][2];                            // 光标图标位图数组：[光标类型][选中/未选中]

    /**
     * 初始化光标图标资源位图
     * 为每种光标（行1/行2/列1/列2）创建选中/未选中两种状态的SVG渲染位图
     */
    private void initResBmp() {
        resBmp[Col1][Select] = SvgManager.createCursorSvg(                  // 创建垂直光标1-选中状态的位图
                SvgNodeInfo.CursorX1Path,                                    // X1光标SVG路径
                SvgNodeInfo.CursorSelectColor,                               // 选中状态颜色
                SvgNodeInfo.CURSOR_X_WIDTH,                                  // X光标宽度
                SvgNodeInfo.CURSOR_X_HEIGHT,                                 // X光标高度
                SvgNodeInfo.PATH_CURSOR_X_POLYGON,                           // X光标多边形路径
                SvgNodeInfo.CursorPolygonSelectColors                        // 选中多边形颜色
        );
        resBmp[Col1][NoSelect] = SvgManager.createCursorSvg(                // 创建垂直光标1-未选中状态的位图
                SvgNodeInfo.CursorX1Path,                                    // X1光标SVG路径
                SvgNodeInfo.CursorNoSelectColor,                             // 未选中状态颜色
                SvgNodeInfo.CURSOR_X_WIDTH,                                  // X光标宽度
                SvgNodeInfo.CURSOR_X_HEIGHT,                                 // X光标高度
                SvgNodeInfo.PATH_CURSOR_X_POLYGON,                           // X光标多边形路径
                SvgNodeInfo.CursorPolygonNoSelectColors                      // 未选中多边形颜色
        );
        resBmp[Col2][Select] = SvgManager.createCursorSvg(                  // 创建垂直光标2-选中状态的位图
                SvgNodeInfo.CursorX2Path,                                    // X2光标SVG路径
                SvgNodeInfo.CursorSelectColor,                               // 选中状态颜色
                SvgNodeInfo.CURSOR_X_WIDTH,                                  // X光标宽度
                SvgNodeInfo.CURSOR_X_HEIGHT,                                 // X光标高度
                SvgNodeInfo.PATH_CURSOR_X_POLYGON,                           // X光标多边形路径
                SvgNodeInfo.CursorPolygonSelectColors                        // 选中多边形颜色
        );
        resBmp[Col2][NoSelect] = SvgManager.createCursorSvg(                // 创建垂直光标2-未选中状态的位图
                SvgNodeInfo.CursorX2Path,                                    // X2光标SVG路径
                SvgNodeInfo.CursorNoSelectColor,                             // 未选中状态颜色
                SvgNodeInfo.CURSOR_X_WIDTH,                                  // X光标宽度
                SvgNodeInfo.CURSOR_X_HEIGHT,                                 // X光标高度
                SvgNodeInfo.PATH_CURSOR_X_POLYGON,                           // X光标多边形路径
                SvgNodeInfo.CursorPolygonNoSelectColors                      // 未选中多边形颜色
        );

        resBmp[Row1][Select] = SvgManager.createCursorSvg(                  // 创建水平光标1-选中状态的位图
                SvgNodeInfo.CursorY1Path,                                    // Y1光标SVG路径
                SvgNodeInfo.CursorSelectColor,                               // 选中状态颜色
                SvgNodeInfo.CURSOR_Y_WIDTH,                                  // Y光标宽度
                SvgNodeInfo.CURSOR_Y_HEIGHT,                                 // Y光标高度
                SvgNodeInfo.PATH_CURSOR_Y_POLYGON,                           // Y光标多边形路径
                SvgNodeInfo.CursorPolygonSelectColors                        // 选中多边形颜色
        );
        resBmp[Row1][NoSelect] = SvgManager.createCursorSvg(                // 创建水平光标1-未选中状态的位图
                SvgNodeInfo.CursorY1Path,                                    // Y1光标SVG路径
                SvgNodeInfo.CursorNoSelectColor,                             // 未选中状态颜色
                SvgNodeInfo.CURSOR_Y_WIDTH,                                  // Y光标宽度
                SvgNodeInfo.CURSOR_Y_HEIGHT,                                 // Y光标高度
                SvgNodeInfo.PATH_CURSOR_Y_POLYGON,                           // Y光标多边形路径
                SvgNodeInfo.CursorPolygonNoSelectColors                      // 未选中多边形颜色
        );

        resBmp[Row2][Select] = SvgManager.createCursorSvg(                  // 创建水平光标2-选中状态的位图
                SvgNodeInfo.CursorY2Path,                                    // Y2光标SVG路径
                SvgNodeInfo.CursorSelectColor,                               // 选中状态颜色
                SvgNodeInfo.CURSOR_Y_WIDTH,                                  // Y光标宽度
                SvgNodeInfo.CURSOR_Y_HEIGHT,                                 // Y光标高度
                SvgNodeInfo.PATH_CURSOR_Y_POLYGON,                           // Y光标多边形路径
                SvgNodeInfo.CursorPolygonSelectColors                        // 选中多边形颜色
        );
        resBmp[Row2][NoSelect] = SvgManager.createCursorSvg(                // 创建水平光标2-未选中状态的位图
                SvgNodeInfo.CursorY2Path,                                    // Y2光标SVG路径
                SvgNodeInfo.CursorNoSelectColor,                             // 未选中状态颜色
                SvgNodeInfo.CURSOR_Y_WIDTH,                                  // Y光标宽度
                SvgNodeInfo.CURSOR_Y_HEIGHT,                                 // Y光标高度
                SvgNodeInfo.PATH_CURSOR_Y_POLYGON,                           // Y光标多边形路径
                SvgNodeInfo.CursorPolygonNoSelectColors                      // 未选中多边形颜色
        );
    }
    //endregion

    /**
     * 构造函数：初始化光标图标资源、创建YT/XY子管理器、注册事件订阅
     */
    public CursorManage() {
        initResBmp();                                                        // 初始化光标图标位图资源
        cursorManage_xy = new CursorManage_XY(resBmp);                       // 创建XY模式光标管理器，传入位图资源
        cursorManage_yt = new CursorManage_YT(resBmp);                       // 创建YT模式光标管理器，传入位图资源
        cursorManage_xy.setOnMovingWaveEvent(onXYMovingWave);                // 注册XY模式光标移动事件回调
        cursorManage_xy.setOnSelectChangeEvent(onXYSelectChange);            // 注册XY模式光标选择变更事件回调
        cursorManage_yt.setOnMovingWaveEvent(onYTMovingWave);                // 注册YT模式光标移动事件回调
        cursorManage_yt.setOnSelectChangeEvent(onYTSelectChange);            // 注册YT模式光标选择变更事件回调
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);           // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange); // 订阅工作模式切换事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_CHANNEL).subscribe(consumerRightChannel);    // 订阅右侧通道菜单变更事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_MATH).subscribe(consumerRightMath);          // 订阅右侧数学运算菜单变更事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor); // 订阅通道选中颜色变更事件

        EventFactory.addEventObserver(EventFactory.EVENT_MEASURE_RANGE, eventUIObserver);                  // 注册测量范围变更事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE, OnChannelVscaleUserChange);       // 注册通道垂直档位变更事件观察者
    }

    /**
     * 测量范围变更事件观察者
     * 当测量范围变更时，更新光标可见性和测量范围数据
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {

            EventBase eventBase = (EventBase) data;                          // 将事件数据转换为EventBase
            if (eventBase.getId() == EventFactory.EVENT_MEASURE_RANGE) {      // 判断是否为测量范围变更事件
                if (MeasureService.isCursorRang()) {                          // 如果当前是光标测量范围模式
                    CursorManage.getInstance().setColVisible(true);           // 显示垂直光标
                }
                if (!MeasureManage.getInstance().isCursorTValueTrace()) {      // 如果光标T值不跟踪
                    MeasureService.setMeasureRange((int) cursorManage_yt.getCol1Position(),  // 设置测量范围为垂直光标1位置
                            (int) cursorManage_yt.getCol2Position());         // 到垂直光标2位置
                }
            }

        }
    };

    /**
     * 初始化方法（空实现，预留扩展）
     */
    public void init() {
    }

    /**
     * 获取光标跟踪状态
     *
     * @return true表示光标正在跟踪时基/幅度变化
     */
    public boolean isCursorTrace() {
        return CursorTrace;                                                  // 返回光标跟踪标志
    }

    /**
     * 设置光标跟踪状态
     *
     * @param cursorTrace true表示光标变化由时基/幅度改变引起
     */
    public void setCursorTrace(boolean cursorTrace) {
        if (enableCursorTrance == false) return;                               // 如果光标跟踪功能被禁用，直接返回
        if (WorkModeManage.getInstance().isXyMode()) return;                 // XY模式下不启用光标跟踪，直接返回
        this.CursorTrace = cursorTrace;                                      // 设置光标跟踪标志
//        Log.d("Tag.Debug", String.format("CursorManage.setTimebaseChange: %s",timebaseChange ));
//        try {
//            throw new Exception();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 获取多光标跟踪移动状态
     *
     * @return true表示多光标移动时保持间距不变
     */
    public boolean isCursorTraceMultiMove() {
        return CursorTraceMultiMove;                                         // 返回多光标跟踪移动标志
    }

    /**
     * 设置多光标跟踪移动状态
     *
     * @param cursorTraceMultiMove true表示移动时保持间距不变
     */
    public void setCursorTraceMultiMove(boolean cursorTraceMultiMove) {
        CursorTraceMultiMove = cursorTraceMultiMove;                         // 设置多光标跟踪移动标志
    }

    /**
     * 获取光标跟踪功能是否启用
     *
     * @return true表示光标跟踪功能已启用
     */
    public boolean isEnableCursorTrance() {
        return enableCursorTrance;                                           // 返回光标跟踪启用状态
    }

    /**
     * 获取光标标签是否不跟随光标
     *
     * @return true表示标签固定位置不跟随光标
     */
    public boolean isLabelNotFollowCursor() {
        return isLabelNotFollowCursor;                                       // 返回标签不跟随光标标志
    }

    /**
     * 设置光标跟踪功能是否启用
     *
     * @param enableCursorTrance true启用，false禁用
     */
    public void setEnableCursorTrance(boolean enableCursorTrance) {
        this.enableCursorTrance = enableCursorTrance;                        // 设置光标跟踪启用状态
    }

    /**
     * 设置光标标签是否不跟随光标移动
     *
     * @param isLabelFixed true表示标签固定位置，false表示跟随光标
     */
    public void setLabelNotFollowCursor(boolean isLabelFixed) {
        this.isLabelNotFollowCursor = isLabelFixed;                          // 设置标签不跟随标志
        MeasureService.forceMeasureRefresh();                                // 强制刷新测量数据显示
        cursorManage_yt.refresh();                                           // 刷新YT模式光标显示
        cursorManage_xy.refresh();                                           // 刷新XY模式光标显示
    }

    /**
     * 获取SCPI远程控制指定的通道索引
     *
     * @return 通道索引，-1表示未指定
     */
    public int getScpiChanIdx() {
        return scpiChanIdx;                                                  // 返回SCPI通道索引
    }

    /**
     * 设置SCPI远程控制指定的通道索引
     *
     * @param scpiChanIdx 通道索引
     */
    public void setScpiChanIdx(int scpiChanIdx) {
        this.scpiChanIdx = scpiChanIdx;                                      // 设置SCPI通道索引
    }

    /**
     * 通道垂直档位用户变更事件观察者
     * 当用户改变通道垂直档位时，临时启用光标跟踪以更新光标位置
     */
    private EventUIObserver OnChannelVscaleUserChange = new EventUIObserver() {
        @Override
        public void update(Object data) {
            CursorManage.getInstance().setCursorTrace(true);                 // 临时启用光标跟踪
            setXYData();                                                     // 更新光标测量数据
            CursorManage.getInstance().setCursorTrace(false);                // 关闭光标跟踪
        }
    };

    /**
     * 右侧数学运算菜单变更事件消费者
     * 当数学运算通道参数变更时，更新光标测量数据
     */
    private Consumer<RightMsgMath> consumerRightMath = new Consumer<RightMsgMath>() {
        @Override
        public void accept(RightMsgMath rightMsgMath) throws Exception {
            int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + rightMsgMath.getMathChannelNumber()); // 获取数学运算类型
//            if (mathType == CacheUtil.MATHTYPE_FFT || mathType == CacheUtil.MATHTYPE_AXB) {
            setXYData();                                                 // 更新光标测量数据
//            }
        }
    };

    /**
     * 右侧通道菜单变更事件消费者
     * 当通道参数变更时，更新光标测量数据
     */
    private Consumer<RightMsgChannel> consumerRightChannel = new Consumer<RightMsgChannel>() {
        @Override
        public void accept(RightMsgChannel rightMsgChannel) throws Exception {
            setXYData();                                                      // 更新光标测量数据
        }
    };

    /**
     * 缓存加载事件消费者
     * 当加载缓存时，从CacheUtil读取YT和XY模式的光标位置并设置到子管理器
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            double ytY1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION + TChan.Cursor_row_1); // 读取YT水平光标1位置
            double ytY2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_YT_CURSORH_POSITION + TChan.Cursor_row_2); // 读取YT水平光标2位置
            int ytX1 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_YT_CURSORV_POSITION + TChan.Cursor_col_1);       // 读取YT垂直光标1位置
            int ytX2 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_YT_CURSORV_POSITION + TChan.Cursor_col_2);       // 读取YT垂直光标2位置
            double xyY1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_XY_CURSORH_POSITION + TChan.Cursor_row_1); // 读取XY水平光标1位置
            double xyY2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_XY_CURSORH_POSITION + TChan.Cursor_row_2); // 读取XY水平光标2位置
            int xyX1 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_XY_CURSORV_POSITION + TChan.Cursor_col_1);       // 读取XY垂直光标1位置
            int xyX2 = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_XY_CURSORV_POSITION + TChan.Cursor_col_2);       // 读取XY垂直光标2位置
            ytY1 = ScopeBase.changeAccuracy(ytY1 * ScopeBase.getToUICoff()); // 将YT水平光标1从数据坐标转换为UI坐标
            ytY2 = ScopeBase.changeAccuracy(ytY2 * ScopeBase.getToUICoff()); // 将YT水平光标2从数据坐标转换为UI坐标

//            Logger.i(TAG, "consumerLoadCache() ==>"+" LoadCacheUtilCmd"
//                    +" ytY1:"+ytY1+" ytY2:"+ytY2+" ytX1:"+ytX1+" ytX2:"+ytX2
//                    +" xyY1:"+xyY1+" xyY2:"+xyY2+" xyX1:"+xyX1+" xyX2:"+xyX2
//                    +" isZoom:"+CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM));
            if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM)) { // 如果当前处于Zoom缩放模式
                ytY1 = ytY1 * GlobalVar.get().toYTCoef();                   // 将YT水平光标1位置乘以缩放系数
                ytY2 = ytY2 * GlobalVar.get().toYTCoef();                   // 将YT水平光标2位置乘以缩放系数
            }

            cursorManage_yt.setCursor(TChan.Cursor_row_1, ytY1);            // 设置YT水平光标1位置
            cursorManage_yt.setCursor(TChan.Cursor_row_2, ytY2);            // 设置YT水平光标2位置
            cursorManage_yt.setCursor(TChan.Cursor_col_1, ytX1);            // 设置YT垂直光标1位置
            cursorManage_yt.setCursor(TChan.Cursor_col_2, ytX2);            // 设置YT垂直光标2位置
            cursorManage_xy.setCursor(TChan.Cursor_row_1, xyY1);            // 设置XY水平光标1位置
            cursorManage_xy.setCursor(TChan.Cursor_row_2, xyY2);            // 设置XY水平光标2位置
            cursorManage_xy.setCursor(TChan.Cursor_col_1, xyX1);            // 设置XY垂直光标1位置
            cursorManage_xy.setCursor(TChan.Cursor_col_2, xyX2);            // 设置XY垂直光标2位置
        }
    };

    /**
     * 工作模式切换事件消费者
     * 当工作模式切换时，刷新光标显示并更新滑动方向
     */
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            cursorManage_yt.refresh();                                        // 刷新YT模式光标显示
            cursorManage_xy.refresh();                                        // 刷新XY模式光标显示
            if (WaveManage.get().getCurCh() == TChan.Ch1) {                  // 如果当前选中通道为CH1
                if (workModeBean.getNextWorkMode() == IWorkMode.WorkMode_XY) { // 如果切换到XY模式
                    setWaveZoneSlideDirectionAndLastObjCol(TChan.Ch1);       // 设置滑动方向为左右（X轴对应CH1）
                } else {                                                     // 否则为YT模式
                    setWaveZoneSlideDirectionAndLastObjRow(TChan.Ch1);       // 设置滑动方向为上下（Y轴对应CH1）
                }
            }
        }
    };

    /**
     * 切换工作模式，委托给对应子管理器
     *
     * @param workMode 目标工作模式（YT/YTZOOM/XY）
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        switch (workMode) {                                                  // 根据工作模式类型分发
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.switchWorkMode(workMode);                    // 委托给YT光标管理器切换模式
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.switchWorkMode(workMode);                    // 委托给XY光标管理器切换模式
                break;
        }
    }

    /**
     * 绘制光标到OpenGL画布
     * 根据当前工作模式委托给对应子管理器绘制
     *
     * @param canvas OpenGL画布
     */
    public void draw(ICanvasGL canvas) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.draw(canvas);                                // 委托YT光标管理器绘制
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.draw(canvas);                                // 委托XY光标管理器绘制
                break;
        }
    }

    /**
     * 获取垂直光标选中索引
     *
     * @return 垂直光标索引（0/1/2/3）
     */
    public int getVerIndex() {
        return VerIndex;                                                     // 返回垂直光标选中索引
    }

    /**
     * 获取水平光标选中索引
     *
     * @return 水平光标索引（0/1/2/3）
     */
    public int getHorIndex() {
        return HorIndex;                                                     // 返回水平光标选中索引
    }

    /**
     * 垂直方向的两条线中，切换三种状态的下一种状态<p/>
     * 三种状态：选中1，选中2，选中1和2
     */
    public void nextVerState() {
        VerIndex += 1;                                                       // 索引递增
        if (VerIndex > 3) VerIndex = 1;                                      // 超过3则循环回到1
        if (VerIndex == 1) {                                                 // 状态1：选中垂直光标1
            setSelectCursor(TChan.Cursor_col_1);                             // 选中垂直光标1
        } else if (VerIndex == 2) {                                          // 状态2：选中垂直光标2
            setSelectCursor(TChan.Cursor_col_2);                             // 选中垂直光标2
        } else if (VerIndex == 3) {                                          // 状态3：同时选中垂直光标1和2（跟踪模式）
            setCursorTracking(TChan.Cursor_col_3);                           // 设置为垂直双光标跟踪模式
        }
    }

    /**
     * 水平方向的两条线中，切换三种状态的下一种状态<p/>
     * 三种状态：选中1，选中2，选中1和2
     */
    public void nextHorState() {
        HorIndex += 1;                                                       // 索引递增
        if (HorIndex > 3) HorIndex = 1;                                      // 超过3则循环回到1
        if (HorIndex == 1) {                                                 // 状态1：选中水平光标1
            setSelectCursor(TChan.Cursor_row_1);                             // 选中水平光标1
        } else if (HorIndex == 2) {                                          // 状态2：选中水平光标2
            setSelectCursor(TChan.Cursor_row_2);                             // 选中水平光标2
        } else if (HorIndex == 3) {                                          // 状态3：同时选中水平光标1和2（跟踪模式）
            setCursorTracking(TChan.Cursor_row_3);                           // 设置为水平双光标跟踪模式
        }
    }

    /**
     * 设置水平方向上光标线的选中状态：选中1，选中2，选中1和2
     */
    public void setHorCursorSelected(int iWaveCursor) {
        if (TChan.Cursor_row_1 == iWaveCursor) {                             // 如果指定水平光标1
            HorIndex = 1;                                                    // 设置水平索引为1
            setSelectCursor(TChan.Cursor_row_1);                             // 选中水平光标1
        } else if (TChan.Cursor_row_2 == iWaveCursor) {                      // 如果指定水平光标2
            HorIndex = 2;                                                    // 设置水平索引为2
            setSelectCursor(TChan.Cursor_row_2);                             // 选中水平光标2
        } else if (TChan.Cursor_row_3 == iWaveCursor) {                      // 如果指定水平双光标跟踪
            HorIndex = 3;                                                    // 设置水平索引为3
            setSelectCursor(TChan.Cursor_row_3);                             // 选中水平双光标跟踪
        } else if (TChan.Cursor_row_4 == iWaveCursor) {                      // 如果指定水平全选
            setSelectCursor(TChan.Cursor_row_4);                             // 选中水平全选（所有水平光标）
        }
    }

    /**
     * 获得水平方向上光标线的选中状态：选中1，选中2，选中1和2
     */
    public int getHorCursorSelected() {
        if (HorIndex == 1) {                                                 // 索引1
            return TChan.Cursor_row_1;                                       // 返回水平光标1标识
        } else if (HorIndex == 2) {                                          // 索引2
            return TChan.Cursor_row_2;                                       // 返回水平光标2标识
        } else if (HorIndex == 3) {                                             // 索引3
            return TChan.Cursor_row_3;                                       // 返回水平双光标跟踪标识
        } else {                                                              // 其他情况
            return TChan.Cursor_row_4;                                       // 返回水平全选标识
        }
    }

    /**
     * 设置垂直方向上光标线的选中状态：选中1，选中2，选中1和2
     */
    public void setVerCursorSelected(int iWaveCursor) {
        if (TChan.Cursor_col_1 == iWaveCursor) {                             // 如果指定垂直光标1
            VerIndex = 1;                                                    // 设置垂直索引为1
            setSelectCursor(TChan.Cursor_col_1);                             // 选中垂直光标1
        } else if (TChan.Cursor_col_2 == iWaveCursor) {                      // 如果指定垂直光标2
            VerIndex = 2;                                                    // 设置垂直索引为2
            setSelectCursor(TChan.Cursor_col_2);                             // 选中垂直光标2
        } else if (TChan.Cursor_col_3 == iWaveCursor) {                      // 如果指定垂直双光标跟踪
            VerIndex = 3;                                                    // 设置垂直索引为3
            setSelectCursor(TChan.Cursor_col_3);                             // 选中垂直双光标跟踪
        } else if (TChan.Cursor_col_4 == iWaveCursor) {                      // 如果指定垂直全选
            setSelectCursor(TChan.Cursor_col_4);                             // 选中垂直全选（所有垂直光标）
        }
    }

    /**
     * 获得垂直方向上光标线的选中状态：选中1，选中2，选中1和2
     */
    public int getVerCursorSelected() {
        if (VerIndex == 1) {                                                 // 索引1
            return TChan.Cursor_col_1;                                       // 返回垂直光标1标识
        } else if (VerIndex == 2) {                                          // 索引2
            return TChan.Cursor_col_2;                                       // 返回垂直光标2标识
        } else if (VerIndex == 3) {                                             // 索引3
            return TChan.Cursor_col_3;                                       // 返回垂直双光标跟踪标识
        } else {                                                              // 其他情况
            return TChan.Cursor_col_4;                                       // 返回垂直全选标识
        }
    }

    /**
     * 根据当前状态移动垂直方向光标
     *
     * @param isRight 向右移动为true，向左为false
     * @param count   移动像素数
     */
    public void moveVerCursor(boolean isRight, int count) {
        if (VerIndex == 0) VerIndex = 1;                                     // 如果索引为0，默认选中垂直光标1
        if (VerIndex == 1) {                                                 // 索引1
            setSelectCursor(TChan.Cursor_col_1);                             // 选中垂直光标1
        } else if (VerIndex == 2) {                                          // 索引2
            setSelectCursor(TChan.Cursor_col_2);                             // 选中垂直光标2
        } else if (VerIndex == 3) {                                          // 索引3
            setCursorTracking(TChan.Cursor_col_3);                           // 设置为垂直双光标跟踪模式
        }
        if (isRight) {                                                       // 向右移动
            addPixMove(count);                                               // 正方向移动指定像素数
        } else {                                                             // 向左移动
            addPixMove(count * -1);                                          // 反方向移动指定像素数
        }
    }

    /**
     * 根据当前状态移动水平方向光标
     *
     * @param isRight 向右移动为true，向左为false
     * @param count   移动像素数
     */
    public void moveHorCursor(boolean isRight, int count) {
//        Logger.i(TAG,"isRight:"+isRight+" count:"+count+" HorIndex:"+HorIndex);
        if (HorIndex == 0) HorIndex = 1;                                     // 如果索引为0，默认选中水平光标1
        if (HorIndex == 1) {                                                 // 索引1
            setSelectCursor(TChan.Cursor_row_1);                             // 选中水平光标1
        } else if (HorIndex == 2) {                                          // 索引2
            setSelectCursor(TChan.Cursor_row_2);                             // 选中水平光标2
        } else if (HorIndex == 3) {                                          // 索引3
            setCursorTracking(TChan.Cursor_row_3);                           // 设置为水平双光标跟踪模式
        }
        if (isRight) {                                                       // 向右移动
            addPixMove(count);                                               // 正方向移动指定像素数
        } else {                                                             // 向左移动
            addPixMove(count * -1);                                          // 反方向移动指定像素数
        }
    }

    /**
     * 缩放模式移动垂直光标（双光标聚拢/扩散）
     *
     * @param isRight 向右为true
     * @param count   移动像素数
     */
    public void moveZoomVerCursor(boolean isRight, int count) {
        if (VerIndex == 0) VerIndex = 1;                                     // 如果索引为0，默认选中垂直光标1
        if (VerIndex == 1) {                                                 // 索引1
            setSelectCursor(TChan.Cursor_col_1);                             // 选中垂直光标1
        } else if (VerIndex == 2) {                                          // 索引2
            setSelectCursor(TChan.Cursor_col_2);                             // 选中垂直光标2
        } else if (VerIndex == 3) {                                          // 索引3
            setCursorTracking(TChan.Cursor_col_3);                           // 设置为垂直双光标跟踪模式
        }
        if (isRight) {                                                       // 向右
            zoomPixMove(count);                                              // 缩放移动（聚拢）
        } else {                                                             // 向左
            zoomPixMove(count * -1);                                         // 缩放移动（扩散）
        }
    }

    /**
     * 缩放模式移动水平光标（双光标聚拢/扩散）
     *
     * @param isRight 向右为true
     * @param count   移动像素数
     */
    public void moveZoomHorCursor(boolean isRight, int count) {
        if (HorIndex == 0) HorIndex = 1;                                     // 如果索引为0，默认选中水平光标1
        if (HorIndex == 1) {                                                 // 索引1
            setSelectCursor(TChan.Cursor_row_1);                             // 选中水平光标1
        } else if (HorIndex == 2) {                                          // 索引2
            setSelectCursor(TChan.Cursor_row_2);                             // 选中水平光标2
        } else if (HorIndex == 3) {                                          // 索引3
            setCursorTracking(TChan.Cursor_row_3);                           // 设置为水平双光标跟踪模式
        }
        if (isRight) {                                                       // 向右
            zoomPixMove(count);                                              // 缩放移动（聚拢）
        } else {                                                             // 向左
            zoomPixMove(count * -1);                                         // 缩放移动（扩散）
        }
    }

    /**
     * XY模式光标选择变更事件回调（空实现，预留扩展）
     */
    private IWave.OnSelectChangeEvent onXYSelectChange = new IWave.OnSelectChangeEvent() {
        @Override
        public void OnSelectChange(IWave iWave, boolean isSelect) {

        }
    };

    /**
     * YT模式光标选择变更事件回调（空实现，预留扩展）
     */
    private IWave.OnSelectChangeEvent onYTSelectChange = new IWave.OnSelectChangeEvent() {
        @Override
        public void OnSelectChange(IWave iWave, boolean isSelect) {

        }
    };

    /**
     * XY模式光标移动事件回调
     * 光标移动时更新测量数据
     */
    private IWave.OnMovingWaveEvent onXYMovingWave = new IWave.OnMovingWaveEvent() {
        @Override
        public void OnMovingWave(IWave iWave, long x, double y, boolean isSwitchWorkMode, boolean isFromEventBus) {
            setXYData();                                                      // 光标移动后更新测量数据
        }
    };

    /**
     * YT模式光标移动事件回调
     * 光标移动时更新测量数据
     */
    private IWave.OnMovingWaveEvent onYTMovingWave = new IWave.OnMovingWaveEvent() {
        @Override
        public void OnMovingWave(IWave iWave, long x, double y, boolean isSwitchWorkMode, boolean isFromEventBus) {
            setXYData();                                                     // 光标移动后更新测量数据
        }
    };

    /**
     * 根据当前工作模式更新光标测量数据
     * 当水平或垂直光标可见时，委托给对应子管理器更新数据
     */
    private void setXYData() {
        if (getRowVisible() || getColVisible()) {                             // 如果水平或垂直光标可见
            if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) { // XY模式
                cursorManage_xy.setData();                                   // 委托XY光标管理器更新数据
            } else {                                                         // YT/YTZOOM模式
                cursorManage_yt.setData();                                   // 委托YT光标管理器更新数据
            }
        }
    }

    /**
     * 当前通道移动时更新光标
     * XY模式下根据当前通道设置滑动方向，并更新测量数据
     */
    public void curChannelMove() {

        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_XY) { // XY模式
            int chIdx = WaveManage.get().getCurCh();                         // 获取当前选中通道
            if (chIdx == TChan.Ch1) {                                        // CH1对应X轴
                setWaveZoneSlideDirectionAndLastObjCol(TChan.Ch1);           // 设置滑动方向为左右
            } else if (chIdx == TChan.Ch2) {                                 // CH2对应Y轴
                setWaveZoneSlideDirectionAndLastObjRow(TChan.Ch2);           // 设置滑动方向为上下
            }
        }
        setXYData();                                                          // 更新测量数据

    }

    /**
     * 时基移动时更新光标测量数据
     */
    public void timeBaseMove() {
        setXYData();                                                         // 更新测量数据
    }

    /**
     * 初始化XY模式光标测量数据
     */
    public void initXY() {
        setXYData();                                                         // 更新测量数据
    }

//    private void setXYData() {
//        if (WorkModeManage.getInstance().getmWorkMode() == WorkMode_YT
//                || WorkModeManage.getInstance().getmWorkMode() == WorkMode_YTZOOM) {
//
//            int curCh= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE)+1;
//            if (curCh==IWave.S1){ //自由选择模式
//                curCh = WaveManage.get().getCurCh();
//            }
//            if (curCh == -1) return;
//            long curChY = WaveManage.get().getPositionY(curCh);
//            long curTimeX = 0;
//            double timeEvery = 0;
//            if ((curCh >= IWave.Ch1 && curCh <= IWave.Math) || (curCh >= IWave.S1 && curCh <= IWave.S2) ) {
//                if (ChannelFactory.isMath_FFT_Ch(curCh - IWave.Ch1 + ChannelFactory.CH1)) {
//                    HorizontalAxisMath horizontalAxisMath = ChannelFactory.getMathChannel().getHorizontalAxisMathFFT();
//                    curTimeX = ScopeBase.getWidth() / 2 - horizontalAxisMath.getXPosOfView();
//                    timeEvery = horizontalAxisMath.fftXScaleIdVal() / ScopeBase.getHorizonPerGridPixels();
//                } else {
//                    curTimeX = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix();
//                    timeEvery = HorizontalAxis.getInstance().getTimesPrePix();
//                }
//            } else if (curCh >= IWave.R1 && curCh <= IWave.R4) {
//                RefChannel refChannel = ChannelFactory.getRefChannel(curCh - IWave.Ch1 + ChannelFactory.CH1);
//                curTimeX = ScopeBase.getWidth() / 2 - refChannel.getTimePoseOfViewPix();
//                timeEvery = refChannel.getRefTimePerPix();
//            }
//
//            double chEvery = 0;
//            if (curCh >= IWave.Ch1 && curCh <= IWave.Ch4) {
//                chEvery = ChannelFactory.getDynamicChannel(curCh - 1).getVerticalPerPix();
//            } else if (curCh == IWave.Math) {
//                chEvery = ChannelFactory.getMathChannel().getVerticalPerPix();
//            } else if (curCh >= IWave.R1 && curCh <= IWave.R4) {
//                chEvery = ChannelFactory.getRefChannel(curCh - 1).getVerticalPerPix();
//            }
//
//            MeasureService.setMeasureRange((int)cursorManage_yt.getCol1Position(),
//                        (int)cursorManage_yt.getCol2Position());
//
//            double x1 = (cursorManage_yt.getCol1Position() - curTimeX) * timeEvery;
//            double x2 = (cursorManage_yt.getCol2Position() - curTimeX) * timeEvery;
//            double y1 = (curChY - cursorManage_yt.getRow1Position()) * chEvery;
//            double y2 = (curChY - cursorManage_yt.getRow2Position()) * chEvery;
//
//            double deltaY = Math.abs(y1 - y2);
//            double deltaX = Math.abs(x1 - x2);
//            String yUnit = ChannelFactory.getProbeType(curCh - IWave.Ch1 + ChannelFactory.CH1);
//            String xUnit;
//            if (ChannelFactory.isMath_FFT_Ch(ChannelFactory.getChActivate())) {
//                xUnit = "Hz";
//            } else {
//                xUnit = "s";
//            }
////            Logger.d(TAG,"curCh:" + curCh);
//            if((curCh >= IWave.S1 && curCh <= IWave.S2)) {
//                MeasureManage.getInstance().getCursorMeasure().setParam(
//                        null                            //单位：V(A)
//                        , null                         //单位：V(A)
//                        , null                      //单位：V(A)
//                        , String.valueOf(TBookUtil.getFourFromD_(x1) + xUnit)                             //单位：s
//                        , String.valueOf(TBookUtil.getFourFromD_(x2) + xUnit)                             //单位：s
//                        , String.valueOf(TBookUtil.getFourFromD_(deltaX) + xUnit)                         //单位：s
//                        , String.valueOf(TBookUtil.getFourFromD_(1.0 / deltaX) + (xUnit.equals("s") ? "Hz" : "s"))//单位：Hz
//                        , null                              //单位：V(A)/s
//                );
//            }else{
//                MeasureManage.getInstance().getCursorMeasure().setParam(
//                        String.valueOf(TBookUtil.getFourFromD_(y1) + yUnit)                              //单位：V(A)
//                        , String.valueOf(TBookUtil.getFourFromD_(y2) + yUnit)                            //单位：V(A)
//                        , String.valueOf(TBookUtil.getFourFromD_(deltaY) + yUnit)                        //单位：V(A)
//                        , String.valueOf(TBookUtil.getFourFromD_(x1) + xUnit)                             //单位：s
//                        , String.valueOf(TBookUtil.getFourFromD_(x2) + xUnit)                             //单位：s
//                        , String.valueOf(TBookUtil.getFourFromD_(deltaX) + xUnit)                         //单位：s
//                        , String.valueOf(TBookUtil.getFourFromD_(1.0 / deltaX) + (xUnit.equals("s") ? "Hz" : "s"))//单位：Hz
//                        , String.valueOf(TBookUtil.getFourFromD_(deltaY * 1.0 / deltaX) + yUnit + "/" + xUnit)//单位：V(A)/s
//                );
//            }
//            Command.get().getCursor().setCursorMeasureInfo(y1, y2, deltaY, x1, x2, deltaX, 1.0 / deltaX, deltaY * 1.0 / deltaX);
//        } else if (WorkModeManage.getInstance().getmWorkMode() == WorkMode_XY) {
//            long curChX = WaveManage.get().getPositionY(IWave.Ch1);
//            long curChY = WaveManage.get().getPositionY(IWave.Ch2);
//            double chEveryX = ChannelFactory.getDynamicChannel(ChannelFactory.CH1).getVerticalPerPix();
//            double chEveryY = ChannelFactory.getDynamicChannel(ChannelFactory.CH2).getVerticalPerPix();
//            double x1 = (cursorManage_xy.getCol1Position() - curChX) * chEveryX;
//            double x2 = (cursorManage_xy.getCol2Position() - curChX) * chEveryX;
//            double y1 = (curChY - cursorManage_xy.getRow1Position()) * chEveryY;
//            double y2 = (curChY - cursorManage_xy.getRow2Position()) * chEveryY;
//            double deltaY = Math.abs(y1 - y2);
//            double deltaX = Math.abs(x1 - x2);
//            String unitX = ChannelFactory.getProbeType(ChannelFactory.CH1);
//            String unitY = ChannelFactory.getProbeType(ChannelFactory.CH2);
//            MeasureManage.getInstance().getCursorMeasure().setParam(
//                    String.valueOf(TBookUtil.getFourFromD_(y1) + unitY)
//                    , String.valueOf(TBookUtil.getFourFromD_(y2) + unitY)
//                    , String.valueOf(TBookUtil.getFourFromD_(deltaY) + unitY)
//                    , String.valueOf(TBookUtil.getFourFromD_(x1) + unitX)
//                    , String.valueOf(TBookUtil.getFourFromD_(x2) + unitX)
//                    , String.valueOf(TBookUtil.getFourFromD_(deltaX) + unitX)
//                    , null, null
//            );
//            Command.get().getCursor().setCursorMeasureInfo(y1, y2, deltaY, x1, x2, deltaX, 0, 0);
//        }
//    }

    //region  光标数据

    /**
     * 获取水平光标是否可见
     *
     * @return true表示水平光标可见
     */
    public boolean getRowVisible() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                return cursorManage_yt.getRowVisible();                      // 返回YT模式水平光标可见性
            case WorkMode_XY:                                                // XY模式
                return cursorManage_xy.getRowVisible();                      // 返回XY模式水平光标可见性
        }
        return false;                                                        // 默认不可见
    }

    /**
     * 获取垂直光标是否可见
     *
     * @return true表示垂直光标可见
     */
    public boolean getColVisible() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                return cursorManage_yt.getColVisible();                      // 返回YT模式垂直光标可见性
            case WorkMode_XY:                                                // XY模式
                return cursorManage_xy.getColVisible();                      // 返回XY模式垂直光标可见性
        }
        return false;                                                        // 默认不可见
    }

    /**
     * 获取水平光标1的像素位置
     *
     * @return 水平光标1的Y坐标，-100表示不可用
     */
    public double getRow1Position() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                return cursorManage_yt.getRow1Position();                    // 返回YT模式水平光标1位置
            case WorkMode_XY:                                                // XY模式
                return cursorManage_xy.getRow1Position();                    // 返回XY模式水平光标1位置
        }
        return -100;                                                         // 默认返回无效值
    }

    /**
     * 获取水平光标2的像素位置
     *
     * @return 水平光标2的Y坐标，-100表示不可用
     */
    public double getRow2Position() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                return cursorManage_yt.getRow2Position();                    // 返回YT模式水平光标2位置
            case WorkMode_XY:                                                // XY模式
                return cursorManage_xy.getRow2Position();                    // 返回XY模式水平光标2位置
        }
        return -100;                                                         // 默认返回无效值
    }

    /**
     * 获取垂直光标1的像素位置
     *
     * @return 垂直光标1的X坐标，-100表示不可用
     */
    public long getCol1Position() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                return cursorManage_yt.getCol1Position();                    // 返回YT模式垂直光标1位置
            case WorkMode_XY:                                                // XY模式
                return cursorManage_xy.getCol1Position();                    // 返回XY模式垂直光标1位置
        }
        return -100;                                                         // 默认返回无效值
    }

    /**
     * 获取垂直光标2的像素位置
     *
     * @return 垂直光标2的X坐标，-100表示不可用
     */
    public long getCol2Position() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                return cursorManage_yt.getCol2Position();                    // 返回YT模式垂直光标2位置
            case WorkMode_XY:                                                // XY模式
                return cursorManage_xy.getCol2Position();                    // 返回XY模式垂直光标2位置
        }
        return -100;                                                         // 默认返回无效值
    }
    //endregion


    //region interface ICursorManage

    /**
     * 设置波形区滑动方向为上下，并指定最后操作对象为水平光标
     *
     * @param cursorRowObj 水平光标对象标识
     */
    private void setWaveZoneSlideDirectionAndLastObjRow(int cursorRowObj) {
        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, cursorRowObj); // 发布最后操作对象事件
        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN); // 发布滑动方向为上下
    }

    /**
     * 设置波形区滑动方向为左右，并指定最后操作对象为垂直光标
     *
     * @param cursorColObj 垂直光标对象标识
     */
    private void setWaveZoneSlideDirectionAndLastObjCol(int cursorColObj) {
        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, cursorColObj); // 发布最后操作对象事件
        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_LEFTRIGHT); // 发布滑动方向为左右
    }

    /**
     * 设置波形区滑动方向为上下，并指定最后操作对象为波形通道
     */
    private void setWaveZoneSlideDirectionAndLastObjToWaveChannel() {
        int chIdx = WaveManage.get().getCurCh();                             // 获取当前选中通道
        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, chIdx); // 发布最后操作对象为当前通道
        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_YT.MOVE_UPDOWN); // 发布滑动方向为上下
    }

    /**
     * 在XY模式下设置波形区滑动方向，根据通道号决定上下或左右
     */
    private void setWaveZoneSlideDirectionAndLastObjToWaveChannelInXYZone() {
        int chIdx = WaveManage.get().getCurCh();                             // 获取当前选中通道
        if (ChannelFactory.isDynamicCh(TChan.toFpgaChNo(chIdx))) {          // 如果是动态通道
            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, chIdx); // 发布最后操作对象
            if (TChan.Ch1 == chIdx || TChan.Ch3 == chIdx) {                 // CH1/CH3对应X轴
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_XY.MOVE_LEFTRIGHT); // 左右滑动
            } else if (TChan.Ch2 == chIdx || TChan.Ch4 == chIdx) {          // CH2/CH4对应Y轴
                RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, WaveZoneDisplay_XY.MOVE_UPDOWN); // 上下滑动
            }
        }
    }

    /**
     * 设置水平光标可见性（使用当前工作模式）
     *
     * @param visible true显示，false隐藏
     */
    @Override
    public void setRowVisible(boolean visible) {
        setRowVisible(WorkModeManage.getInstance().getmWorkMode(), visible); // 委托给带工作模式参数的重载方法
    }


    /**
     * 设置垂直光标可见性（使用当前工作模式）
     *
     * @param visible true显示，false隐藏
     */
    @Override
    public void setColVisible(boolean visible) {
        setColVisible(WorkModeManage.getInstance().getmWorkMode(), visible); // 委托给带工作模式参数的重载方法
    }

    /**
     * 设置水平光标可见性（指定工作模式）
     * 显示时自动选中水平光标1；隐藏时根据垂直光标状态切换选中对象
     *
     * @param workMode 工作模式
     * @param visible  true显示，false隐藏
     */
    public void setRowVisible(int workMode, boolean visible) {
        RxBus.getInstance().post(RxEnum.CURSOR_CHANGE_VISIBLE, new MsgCursorVisible(workMode != WorkMode_XY, false, visible)); // 发布光标可见性变更事件
        switch (workMode) {                                                  // 根据工作模式分发
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.setRowVisible(workMode, visible);            // 设置YT水平光标可见性
                cursorManage_yt.setRowMeasureVisible(visible);               // 设置YT水平测量标签可见性
                if (visible) {                                               // 如果显示水平光标
                    this.HorIndex = 1;                                       // 设置水平索引为1
                    cursorManage_yt.setSelectCursor(TChan.Cursor_row_1);     // 选中水平光标1
                    setWaveZoneSlideDirectionAndLastObjRow(TChan.Cursor_row_1); // 设置滑动方向为上下
                } else {                                                     // 如果隐藏水平光标
                    if (getColVisible() && (getCurrSelectCursor() == TChan.Cursor_col_1 || getCurrSelectCursor() == TChan.Cursor_col_2 || getCurrSelectCursor() == TChan.Cursor_col_3)) { // 垂直光标可见且选中了垂直光标
                        this.VerIndex = 1;                                   // 设置垂直索引为1
                        cursorManage_yt.setSelectCursor(TChan.Cursor_col_1); // 选中垂直光标1
                        setWaveZoneSlideDirectionAndLastObjCol(TChan.Cursor_col_1); // 设置滑动方向为左右
                    } else {                                                 // 垂直光标也不可见
                        setWaveZoneSlideDirectionAndLastObjToWaveChannel();   // 切换滑动对象为波形通道
                    }
                }
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.setRowVisible(workMode, visible);            // 设置XY水平光标可见性
                cursorManage_xy.setRowMeasureVisible(visible);               // 设置XY水平测量标签可见性
                if (visible) {                                               // 如果显示水平光标
                    this.HorIndex = 1;                                       // 设置水平索引为1
                    cursorManage_xy.setSelectCursor(TChan.Cursor_row_1);     // 选中水平光标1
                    setWaveZoneSlideDirectionAndLastObjRow(TChan.Cursor_row_1); // 设置滑动方向为上下
                } else {                                                     // 如果隐藏水平光标
                    if (getColVisible() && (getCurrSelectCursor() == TChan.Cursor_col_1 || getCurrSelectCursor() == TChan.Cursor_col_2 || getCurrSelectCursor() == TChan.Cursor_col_3)) { // 垂直光标可见且选中了垂直光标
                        this.VerIndex = 1;                                   // 设置垂直索引为1
                        cursorManage_xy.setSelectCursor(TChan.Cursor_col_1); // 选中垂直光标1
                        setWaveZoneSlideDirectionAndLastObjCol(TChan.Cursor_col_1); // 设置滑动方向为左右
                    } else {                                                 // 垂直光标也不可见
                        setWaveZoneSlideDirectionAndLastObjToWaveChannelInXYZone(); // XY模式下切换滑动对象
                    }
                }
                break;
        }
    }

    /**
     * 设置垂直光标可见性（指定工作模式）
     * 显示时自动选中垂直光标1；隐藏时根据水平光标状态切换选中对象
     *
     * @param workMode 工作模式
     * @param visible  true显示，false隐藏
     */
    public void setColVisible(int workMode, boolean visible) {
        RxBus.getInstance().post(RxEnum.CURSOR_CHANGE_VISIBLE, new MsgCursorVisible(workMode != WorkMode_XY, true, visible)); // 发布光标可见性变更事件
        switch (workMode) {                                                  // 根据工作模式分发
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.setColVisible(workMode, visible);            // 设置YT垂直光标可见性
                cursorManage_yt.setColMeasureVisible(visible);               // 设置YT垂直测量标签可见性
                if (visible) {                                               // 如果显示垂直光标
                    this.VerIndex = 1;                                       // 设置垂直索引为1
                    cursorManage_yt.setSelectCursor(TChan.Cursor_col_1);     // 选中垂直光标1
                    setWaveZoneSlideDirectionAndLastObjCol(TChan.Cursor_col_1); // 设置滑动方向为左右
                } else {                                                     // 如果隐藏垂直光标
                    if (getRowVisible() && ((getCurrSelectCursor() == TChan.Cursor_row_1 || getCurrSelectCursor() == TChan.Cursor_row_2))) { // 水平光标可见且选中了水平光标
                        this.HorIndex = 1;                                   // 设置水平索引为1
                        cursorManage_yt.setSelectCursor(TChan.Cursor_row_1); // 选中水平光标1
                        setWaveZoneSlideDirectionAndLastObjRow(TChan.Cursor_row_1); // 设置滑动方向为上下
                    } else {                                                 // 水平光标也不可见
                        setWaveZoneSlideDirectionAndLastObjToWaveChannel();   // 切换滑动对象为波形通道
                    }
                }
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.setColVisible(workMode, visible);            // 设置XY垂直光标可见性
                cursorManage_xy.setColMeasureVisible(visible);               // 设置XY垂直测量标签可见性
                if (visible) {                                               // 如果显示垂直光标
                    this.VerIndex = 1;                                       // 设置垂直索引为1
                    cursorManage_xy.setSelectCursor(TChan.Cursor_col_1);     // 选中垂直光标1
                    setWaveZoneSlideDirectionAndLastObjCol(TChan.Cursor_col_1); // 设置滑动方向为左右
                } else {                                                     // 如果隐藏垂直光标
                    if (getRowVisible() && ((getCurrSelectCursor() == TChan.Cursor_row_1 || getCurrSelectCursor() == TChan.Cursor_row_2))) { // 水平光标可见且选中了水平光标
                        this.HorIndex = 1;                                   // 设置水平索引为1
                        cursorManage_xy.setSelectCursor(TChan.Cursor_row_1); // 选中水平光标1
                        setWaveZoneSlideDirectionAndLastObjRow(TChan.Cursor_row_1); // 设置滑动方向为上下
                    } else {                                                 // 水平光标也不可见
                        setWaveZoneSlideDirectionAndLastObjToWaveChannelInXYZone(); // XY模式下切换滑动对象
                    }
                }
                break;
        }
        setXYData();                                                         // 更新测量数据
    }

    /**
     * 根据坐标选择光标
     *
     * @param x 按下X坐标
     * @param y 按下Y坐标
     * @return 选中的光标类型标识，0表示未选中
     */
    @Override
    public int selectCursor(int x, double y) {
        int i = 0;                                                           // 初始化选中结果
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                i = cursorManage_yt.selectCursor(x, y);                      // 委托YT光标管理器选择
                break;
            case WorkMode_XY:                                                // XY模式
                i = cursorManage_xy.selectCursor(x, y);                      // 委托XY光标管理器选择
                break;
        }
        return i;                                                            // 返回选中结果
    }

    /**
     * 移动光标标签偏移量
     *
     * @param offsetX X方向偏移
     * @param offsetY Y方向偏移
     */
    public void MoveLabel(int offsetX, int offsetY) {
        if (WorkModeManage.getInstance().getmWorkMode() != WorkMode_XY) {       // 非XY模式
            cursorManage_yt.MoveLabel(offsetX, offsetY);                      // 移动YT模式光标标签
        } else {                                                               // XY模式
            cursorManage_xy.MoveLabel(offsetX, offsetY);                      // 移动XY模式光标标签
        }
    }

    /**
     * 根据坐标和指定光标ID选择光标
     *
     * @param x        按下X坐标
     * @param y        按下Y坐标
     * @param cursorId 指定光标类型标识
     * @return 选中的光标类型标识，-1表示未选中
     */
    public int selectCursor(int x, int y, int cursorId) {
        int i = -1;                                                          // 初始化选中结果为-1（未选中）
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                i = cursorManage_yt.selectCursor(x, y, cursorId);             // 委托YT光标管理器选择
                break;
            case WorkMode_XY:                                                // XY模式
                i = cursorManage_xy.selectCursor(x, y, cursorId);             // 委托XY光标管理器选择
                break;
        }
        return i;                                                            // 返回选中结果
    }

    /**
     * 设置指定光标的高亮颜色
     *
     * @param index 光标标识
     */
    public void setSelectHighColor(int index) {
        cursorManage_yt.setSelectHighColor(index);                           // 设置YT模式光标高亮颜色
        cursorManage_xy.setSelectHighColor(index);                           // 设置XY模式光标高亮颜色
    }

    /**
     * 获取多光标选择结果
     *
     * @param x 按下X坐标
     * @param y 按下Y坐标
     * @return 多光标选择标识，-1表示未选中
     */
    public int getMultiCursorSelect(int x, int y) {
        if (selectCursor(x, y) != -1) {                                         // 如果选中了光标
            switch (WorkModeManage.getInstance().getmWorkMode()) {           // 获取当前工作模式
                case WorkMode_YT:                                            // YT模式
                case WorkMode_YTZOOM:                                        // YT缩放模式
                    return cursorManage_yt.getMultiCursorSelect(x, y);       // 委托YT光标管理器获取多光标选择
                case WorkMode_XY:                                            // XY模式
                    return cursorManage_xy.getMultiSelectCursor(x, y);       // 委托XY光标管理器获取多光标选择

            }
        }
        return -1;                                                           // 未选中返回-1
    }

    /**
     * 设置选中光标
     * 根据光标标识判断是单选、多选还是跟踪模式
     *
     * @param index 光标标识
     */
    @Override
    public void setSelectCursor(int index) {
        if (index == TChan.Cursor_all) {                                       // 全选所有光标
            switch (WorkModeManage.getInstance().getmWorkMode()) {            // 获取当前工作模式
                case WorkMode_YT:                                            // YT模式
                case WorkMode_YTZOOM:                                        // YT缩放模式
                    cursorManage_yt.setMultiSelectCursor(TChan.Cursor_col_1, TChan.Cursor_col_2, TChan.Cursor_row_1, TChan.Cursor_row_2); // YT模式全选4个光标

                    break;
                case WorkMode_XY:                                            // XY模式
                    cursorManage_xy.setMultiSelectCursor(TChan.Cursor_col_1, TChan.Cursor_col_2, TChan.Cursor_row_1, TChan.Cursor_row_2); // XY模式全选4个光标

                    break;
            }
            return;                                                          // 处理完毕，直接返回
        }
        if (index == TChan.Cursor_col_4 || index == TChan.Cursor_row_4 || index == TChan.Cursor_col_3 || index == TChan.Cursor_row_3) { // 多选/跟踪模式
            switch (WorkModeManage.getInstance().getmWorkMode()) {           // 获取当前工作模式
                case WorkMode_YT:                                            // YT模式
                case WorkMode_YTZOOM:                                        // YT缩放模式
                    if (index == TChan.Cursor_col_4 || index == TChan.Cursor_col_3) { // 垂直多选
                        cursorManage_yt.setMultiSelectCursor(TChan.Cursor_col_1, TChan.Cursor_col_2); // 同时选中垂直光标1和2
                    }
                    if (index == TChan.Cursor_row_4 || index == TChan.Cursor_row_3) // 水平多选
                    {
                        cursorManage_yt.setMultiSelectCursor(TChan.Cursor_row_1, TChan.Cursor_row_2); // 同时选中水平光标1和2
                    }
                    break;
                case WorkMode_XY:                                            // XY模式
                    if (index == TChan.Cursor_col_4 || index == TChan.Cursor_col_3) { // 垂直多选
                        cursorManage_xy.setMultiSelectCursor(TChan.Cursor_col_1, TChan.Cursor_col_2); // 同时选中垂直光标1和2
                    } else {                                                 // 水平多选
                        cursorManage_xy.setMultiSelectCursor(TChan.Cursor_row_1, TChan.Cursor_row_2); // 同时选中水平光标1和2
                    }
                    break;
            }
            return;                                                          // 处理完毕，直接返回
        }
        switch (index) {                                                     // 单选光标
            case TChan.Cursor_col_1:                                         // 垂直光标1
                VerIndex = 1;                                                // 设置垂直索引为1
                break;
            case TChan.Cursor_col_2:                                         // 垂直光标2
                VerIndex = 2;                                                // 设置垂直索引为2
                break;
            case TChan.Cursor_row_1:                                         // 水平光标1
                HorIndex = 1;                                                // 设置水平索引为1
                break;
            case TChan.Cursor_row_2:                                         // 水平光标2
                HorIndex = 2;                                                // 设置水平索引为2
                break;
            default:
                break;
        }
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.setSelectCursor(index);                      // 委托YT光标管理器设置选中
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.setSelectCursor(index);                      // 委托XY光标管理器设置选中
                break;
        }
    }

    /**
     * 移动选中光标（相对偏移量）
     *
     * @param offsetX X方向偏移像素数
     * @param offsetY Y方向偏移像素数
     */
    @Override
    public void moveSelectCursor(int offsetX, double offsetY) {
        if (isExecMoveForTracking(-offsetX, -offsetY, false) == false) return; // 检查是否允许移动（边界检查）
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.moveSelectCursor(offsetX, offsetY);          // 委托YT光标管理器移动
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.moveSelectCursor(offsetX, offsetY);          // 委托XY光标管理器移动
                break;
        }
    }

    /**
     * 移动多选光标（相对偏移量）
     *
     * @param offsetX X方向偏移像素数
     * @param offsetY Y方向偏移像素数
     */
    @Override
    public void moveMultiSelectCursor(int offsetX, int offsetY) {
        if (isExecMoveForTracking(-offsetX, -offsetY, false) == false) return; // 检查是否允许移动（边界检查）
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.moveMultiSelectCursor(offsetX, offsetY);     // 委托YT光标管理器移动多选光标
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.moveMultiSelectCursor(offsetX, offsetY);     // 委托XY光标管理器移动多选光标
                break;
        }
    }

    /**
     * 移动多选光标（带光标索引，支持跟踪模式）
     *
     * @param offsetX   X方向偏移像素数
     * @param offsetY   Y方向偏移像素数
     * @param cursorIdx 光标标识
     */
    public void moveMultiSelectCursor(int offsetX, int offsetY, int cursorIdx) {
        if (isExecMoveForTracking(-offsetX, -offsetY, false, cursorIdx) == false)
            return; // 检查是否允许移动（带光标索引的边界检查）
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                if (CursorManage.getInstance().isEnableCursorTrance()) {     // 如果光标跟踪功能启用
//                setCursorTrace(true);
                    setCursorTraceMultiMove(true);                           // 启用多光标跟踪移动（保持间距）
                    if (offsetX != 0 && cursorIdx == TChan.Cursor_col_3 || cursorIdx == TChan.Cursor_col_4) { // 垂直双光标跟踪移动
                        CursorManage.setCursorByTimebaseTrace(offsetX);      // 按时基跟踪移动垂直光标
                    }
                    if (offsetY != 0 && cursorIdx == TChan.Cursor_row_3 || cursorIdx == TChan.Cursor_row_4) { // 水平双光标跟踪移动
                        CursorManage.setCursorByScaleTrace(-offsetY);         // 按幅度跟踪移动水平光标
                    }
                    setCursorTrace(false);                                   // 关闭光标跟踪标志
                    setCursorTraceMultiMove(false);                           // 关闭多光标跟踪移动标志
                } else {                                                      // 光标跟踪功能未启用
                    cursorManage_yt.moveMultiSelectCursor(offsetX, offsetY); // 直接移动YT模式多选光标
                }
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.moveMultiSelectCursor(offsetX, offsetY);     // 委托XY光标管理器移动多选光标
                break;
        }
    }


    /**
     * 判断是否为自动切换通道模式
     *
     * @return true表示自动切换通道
     */
    public static boolean isAutoSwitchChannel() {
        return ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT + ChannelFactory.REF_CNT == CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE); // 通道总数等于光标源配置值
    }

    /**
     * 设置光标通道颜色
     *
     * @param ChNo 通道号
     */
    @Override
    public void setCursorChannelColor(int ChNo) {
        ChNo = TChan.Cursor_col_1;                                             // 强制设为垂直光标1标识
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.setCursorChannelColor(ChNo);                 // 设置YT光标通道颜色
                cursorManage_yt.setData();                                   // 更新YT测量数据

                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_yt.setCursorChannelColor(ChNo);                 // 设置YT光标通道颜色（XY模式也需要更新YT）
                cursorManage_xy.setCursorChannelColor(-1);                   // 设置XY光标通道颜色为-1（无特定通道）
                cursorManage_xy.setData();                                   // 更新XY测量数据

                break;
        }
    }

    /**
     * 是否选择自动光标
     *
     * @return true表示自动光标模式
     */
    public boolean isAutoCursor() {
        int sourceIdx = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE); // 获取光标源配置值
        return sourceIdx == 24;                                                // 24表示自动模式
    }

    /**
     * 获取光标通道索引
     *
     * @return 光标源配置值
     */
    public int getCursorChan() {
        int sourceIdx = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR_COMMON_SOURCE); // 获取光标源配置值
        return sourceIdx;                                                    // 返回光标通道索引
    }

    /**
     * 判断当前选中通道是否与光标通道相同
     *
     * @return true表示当前通道与光标通道一致
     */
    public boolean isSelectChanEqCursorChan() {
        boolean b = (TChan.toFpgaChNo(WaveManage.get().getCurCh())) == getCursorChan(); // 将当前通道号转换为FPGA通道号后比较
//        Log.d("Tag.Debug", String.format("CursorManage.isSelectChanEqCursorChan: curChan:%s ,curorChan:%s ,b:%s",WaveManage.get().getCurCh(),getSelectChan(),b ));
        return b;                                                            // 返回比较结果
    }

    /**
     * 判断SCPI通道是否与光标通道相同
     *
     * @return true表示SCPI通道与光标通道一致
     */
    public boolean isScpiChanEqCursorChan() {
        if (getScpiChanIdx() == -1) return false;                              // SCPI通道未指定，返回false
        boolean b = getScpiChanIdx() == getCursorChan();                        // 比较SCPI通道与光标通道
//        Log.d("Tag.Debug", String.format("CursorManage.isScpiChanEqCursorChan: %s,%s",getScpiChanIdx(),getCursorChan() ));

        return b;                                                            // 返回比较结果
    }


    /**
     * 判断光标通道是否为逻辑通道（模拟通道或非FFT数学通道）
     *
     * @return true表示光标通道是逻辑通道
     */
    private boolean isCursorLogic() {
        int chIdx = getCursorChan();                                          // 获取光标通道索引
        return isLogicChan(chIdx);                                           // 委托给isLogicChan判断
    }

    /**
     * 判断当前选中通道是否为逻辑通道
     *
     * @return true表示当前通道是逻辑通道
     */
    private boolean isCurLogic() {
        int chIdx = TChan.toFpgaChNo(WaveManage.get().getCurCh());           // 将当前通道号转换为FPGA通道号
        return isLogicChan(chIdx);                                           // 委托给isLogicChan判断
    }

    /**
     * 判断指定通道是否为逻辑通道（模拟通道CH1~CH8 或 非FFT数学通道）
     *
     * @param chIdx 通道索引
     * @return true表示是逻辑通道
     */
    private boolean isLogicChan(int chIdx) {
        boolean b1 = (chIdx >= ChannelFactory.CH1 && chIdx <= ChannelFactory.CH8); // 判断是否为模拟通道CH1~CH8
        boolean b2 = (chIdx >= ChannelFactory.MATH1 && chIdx <= ChannelFactory.MATH8 && ChannelFactory.getMathChannel(chIdx).getMathType() != MathWave.MATH_FFTWAVE); // 判断是否为非FFT数学通道
        return b1 || b2;                                                     // 模拟通道或非FFT数学通道都算逻辑通道
    }

    /**
     * 判断当前通道是否为串行通道
     *
     * @return true表示当前通道是串行通道
     */
    private boolean isSerialChan() {
        return (TChan.isSerial(WaveManage.get().getCurCh()));                // 使用TChan工具类判断
    }

    /**
     * 判断指定通道是否为FFT通道
     *
     * @param tChan 通道标识
     * @return true表示是FFT通道
     */
    public boolean isFFT(int tChan) {
        if (TChan.isRef(tChan)) {                                            // 如果是参考通道
            RefChannel ref = ChannelFactory.getRefChannel(TChan.toFpgaChNo(tChan)); // 获取参考通道对象
            if (ref.getRefType() == 2) {                                        // 参考类型为2表示FFT参考
                return true;                                                 // 是FFT通道
            }
        } else if (TChan.isMath(tChan)) {                                      // 如果是数学通道
            MathChannel math = ChannelFactory.getMathChannel(TChan.toFpgaChNo(tChan)); // 获取数学通道对象
            if (math.getMathType() == MathFFTWave.MATH_FFTWAVE) {              // 数学类型为FFT
                return true;                                                 // 是FFT通道
            }
        }
        return false;                                                        // 不是FFT通道
    }

    /**
     * 垂直光标是否跟踪
     * 1。自动，都移动
     * 2。不自动，锁定FFT ，当前FFT，都移动
     * 3。不自动，锁定非FFT，当前非FFT，都移动
     * 4.当前是串形通道，都移动
     *
     * @return true:跟踪  false:不跟踪
     */
    public boolean isColCursorTrace() {
        boolean bTrace1 = isAutoCursor();                                     // 条件1：自动光标模式
        boolean bTrace2 = isAutoCursor() == false && isCursorLogic() && isCurLogic(); // 条件2：非自动且光标和当前都是逻辑通道
        boolean bTrace3 = isAutoCursor() == false && isSelectChanEqCursorChan(); // 条件3：非自动且当前通道与光标通道一致
        boolean bTrace4 = isSerialChan();                                     // 条件4：当前是串行通道
        //Log.d("Tag.Debug", String.format("CursorManage.isColCursorTrace: %s,%s,%s,%s",bTrace1,bTrace2,bTrace3,bTrace4 ));
        return bTrace1 || bTrace2 || bTrace3 || bTrace4;                   // 任一条件满足则跟踪
    }

    /**
     * 水平光标是否跟踪
     *
     * @return true:跟踪  false:不跟踪
     */
    public boolean isRowCursorTrace() {
        boolean bTrace1 = CursorManage.getInstance().isAutoCursor();         // 条件1：自动光标模式
        boolean bTrace2 = CursorManage.getInstance().isSelectChanEqCursorChan(); // 条件2：当前通道与光标通道一致
        boolean bTrace3 = CursorManage.getInstance().isScpiChanEqCursorChan(); // 条件3：SCPI通道与光标通道一致
//        Log.d("Tag.Debug", String.format("CursorManage.isRowCursorTrace: bTrace1:%s,bTrace2:%s,bTrace3:%s",bTrace1,bTrace2,bTrace3 ));
        return bTrace1 || bTrace2 || bTrace3;                                // 任一条件满足则跟踪
    }

    /**
     * 光标移动完成回调
     * 取消所有光标的高亮显示
     */
    @Override
    public void moveFinish() {
        cursorManage_yt.CancelAllHightShow();                                // 取消YT模式所有光标高亮
        cursorManage_xy.CancelAllHightShow();                                // 取消XY模式所有光标高亮
    }
    //endregion

    //region interface IWaveControl

    /**
     * 选中光标增加1个像素位移
     */
    @Override
    public void addPixMove() {
        if (isExecMoveForTracking(1, 1, false) == false) return;             // 检查是否允许移动（边界检查）
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.addPixMove();                                // YT模式增加1像素
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.addPixMove();                                // XY模式增加1像素
                break;
        }
    }

    /**
     * 加减num个像素
     *
     * @param num 为正增加，为负减少
     */
    public void addPixMove(int num) {
        if (isExecMoveForTracking(num, num, false) == false) return;         // 检查是否允许移动（边界检查）
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.addPixMove(num);                             // YT模式增加/减少指定像素
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.addPixMove(num);                             // XY模式增加/减少指定像素
                break;
        }
    }

    /**
     * 如果是正数，则两条光标线向中间聚拢，如果是负数，则向两边扩散...
     */
    public void zoomPixMove(int num) {
        if (isExecMoveForTracking(num, num, true) == false) return;          // 检查是否允许移动（缩放模式边界检查）
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.zoomPixMove(num);                            // YT模式缩放移动
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.zoomPixMove(num);                            // XY模式缩放移动
                break;
        }
    }

    /**
     * 选中光标减少1个像素位移
     */
    @Override
    public void subPixMove() {
        if (isExecMoveForTracking(-1, -1, false) == false) return;           // 检查是否允许移动（边界检查）
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.subPixMove();                                // YT模式减少1像素
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.subPixMove();                                // XY模式减少1像素
                break;
        }
    }

    /**
     * 跟踪光标是否执行移动
     * 检查双光标跟踪模式下移动是否会越界
     *
     * @param offsetX X偏移位置 右偏移为正  左偏移为负
     * @param offsetY Y偏移位置  上偏移为负，下偏移为负
     * @param isZoom  是否是缩放操作
     * @return true允许移动，false不允许（会越界）
     */
    private boolean isExecMoveForTracking(int offsetX, double offsetY, boolean isZoom) {
        boolean b = false;                                                   // 默认不允许移动
//        Logger.i(TAG,"==========isRowSelect:"+isRowSelect()+" VerIndex:"+VerIndex+" HorIndex:"+HorIndex+"==========");
        int cursor2Reverse = isZoom ? -1 : 1;                                // 缩放模式下第2条光标反向移动
        if (isRowSelect() && HorIndex == 3) {                                // 水平双光标跟踪模式
            int minRow = 0;                                                  // 最小Y坐标
            int maxRow = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()); // 最大Y坐标
            double row1 = getRow1Position();                                 // 水平光标1当前位置
            double row2 = getRow2Position();                                 // 水平光标2当前位置
//            Logger.i(TAG,"minRow:"+minRow+" maxRow:"+maxRow+" row1:"+row1+" row2:"+row2+" offsetX:"+offsetY);
            if ((row1 + offsetY < maxRow && row2 + offsetY * cursor2Reverse < maxRow) // 检查是否不超过下边界
                    && (row1 + offsetY >= minRow && row2 + offsetY * cursor2Reverse >= minRow)) { // 检查是否不低于上边界
                b = true;                                                    // 在边界范围内，允许移动
            }
        } else if (!isRowSelect() && VerIndex == 3) {                        // 垂直双光标跟踪模式
            int minCol = 0;                                                  // 最小X坐标
            int maxCol = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()); // 最大X坐标
            long col1 = getCol1Position();                                   // 垂直光标1当前位置
            long col2 = getCol2Position();                                   // 垂直光标2当前位置
//            Logger.i(TAG,"minCol:"+minCol+" maxCol:"+maxCol+" col1:"+col1+" col2:"+col2+" offsetY:"+offsetX);
            if ((col1 + offsetX < maxCol && col2 + offsetX * cursor2Reverse < maxCol) // 检查是否不超过右边界
                    && (col1 + offsetX >= minCol && col2 + offsetX * cursor2Reverse >= minCol)) { // 检查是否不低于左边界
                b = true;                                                    // 在边界范围内，允许移动
            }
        } else {                                                             // 非跟踪模式
            b = true;                                                        // 非跟踪模式默认允许移动
        }
        return b;                                                            // 返回是否允许移动
    }

    /**
     * 跟踪光标是否执行移动（带光标索引）
     * 检查指定光标在双光标跟踪模式下移动是否会越界
     *
     * @param offsetX   X偏移位置
     * @param offsetY   Y偏移位置
     * @param isZoom    是否是缩放操作
     * @param cursorIdx 光标标识
     * @return true允许移动，false不允许
     */
    private boolean isExecMoveForTracking(int offsetX, int offsetY, boolean isZoom, int cursorIdx) {
        boolean b = false;                                                   // 默认不允许移动
//        Logger.i(TAG,"==========isRowSelect:"+isRowSelect()+" VerIndex:"+VerIndex+" HorIndex:"+HorIndex+"==========");
        int cursor2Reverse = isZoom ? -1 : 1;                                // 缩放模式下第2条光标反向移动
        if (isRowSelect() && (HorIndex == 3 || cursorIdx == TChan.Cursor_row_3 || cursorIdx == TChan.Cursor_row_4)) { // 水平双光标跟踪
            int minRow = 0;                                                  // 最小Y坐标
            int maxRow = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()); // 最大Y坐标
            double row1 = getRow1Position();                                 // 水平光标1当前位置
            double row2 = getRow2Position();                                 // 水平光标2当前位置
//            Logger.i(TAG,"minRow:"+minRow+" maxRow:"+maxRow+" row1:"+row1+" row2:"+row2+" offsetX:"+offsetY);
            if ((row1 + offsetY < maxRow && row2 + offsetY * cursor2Reverse < maxRow) // 检查下边界
                    && (row1 + offsetY >= minRow && row2 + offsetY * cursor2Reverse >= minRow)) { // 检查上边界
                b = true;                                                    // 在边界范围内，允许移动
            }
        } else if (!isRowSelect() && (VerIndex == 3 || cursorIdx == TChan.Cursor_col_3 || cursorIdx == TChan.Cursor_col_4)) { // 垂直双光标跟踪
            int minCol = 0;                                                  // 最小X坐标
            int maxCol = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()); // 最大X坐标
            long col1 = getCol1Position();                                   // 垂直光标1当前位置
            long col2 = getCol2Position();                                   // 垂直光标2当前位置
//            Logger.i(TAG,"minCol:"+minCol+" maxCol:"+maxCol+" col1:"+col1+" col2:"+col2+" offsetY:"+offsetX);
            if ((col1 + offsetX < maxCol && col2 + offsetX * cursor2Reverse < maxCol) // 检查右边界
                    && (col1 + offsetX >= minCol && col2 + offsetX * cursor2Reverse >= minCol)) { // 检查左边界
                b = true;                                                    // 在边界范围内，允许移动
            }
        } else {                                                             // 非跟踪模式
            b = true;                                                        // 非跟踪模式默认允许移动
        }
        return b;                                                            // 返回是否允许移动
    }

    /**
     * 判断当前是否选中了水平光标
     *
     * @return true表示选中了水平光标
     */
    private boolean isRowSelect() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                return cursorManage_yt.isRowSelect();                        // 返回YT模式水平光标选中状态
            case WorkMode_XY:                                                // XY模式
                return cursorManage_xy.isRowSelect();                        // 返回XY模式水平光标选中状态
        }
        return false;                                                        // 默认未选中
    }

    /**
     * 初始化垂直光标位置到1/4和3/4处
     */
    @Override
    public void initCursorX() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.initCursorX();                               // 委托YT光标管理器初始化X位置
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.initCursorX();                               // 委托XY光标管理器初始化X位置
                break;
        }
    }

    /**
     * 初始化水平光标位置到1/4和3/4处
     */
    @Override
    public void initCursorY() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.initCursorY();                               // 委托YT光标管理器初始化Y位置
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.initCursorY();                               // 委托XY光标管理器初始化Y位置
                break;
        }
    }

    /**
     * 设置指定类型光标的位置
     *
     * @param cursorType 光标类型标识
     * @param position   目标位置
     */
    @Override
    public void setCursor(int cursorType, double position) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.setCursor(cursorType, position);             // 委托YT光标管理器设置位置
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.setCursor(cursorType, position);             // 委托XY光标管理器设置位置
                break;
        }
    }

    /**
     * 设置指定类型光标的偏移位置
     *
     * @param cursorType 光标类型标识
     * @param offset     偏移像素数
     */
    public void setCursorOffsetPos(int cursorType, int offset) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.setCursorOffsetPos(cursorType, offset);      // 委托YT光标管理器设置偏移
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.setCursorOffsetPos(cursorType, offset);      // 委托XY光标管理器设置偏移
                break;
        }
    }

    /**
     * 获取指定类型光标的位置
     *
     * @param cursorType 光标类型标识
     * @return 光标位置，0表示未找到
     */
    @Override
    public double getCursor(int cursorType) {
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                cursorManage_yt.getCursor(cursorType);                       // 委托YT光标管理器获取位置
                break;
            case WorkMode_XY:                                                // XY模式
                cursorManage_xy.getCursor(cursorType);                       // 委托XY光标管理器获取位置
                break;
        }
        return 0;                                                            // 默认返回0
    }

    /**
     * 设置双光标跟踪模式
     * 根据光标类型同时选中两条水平或垂直光标
     *
     * @param CurrChNo 光标类型标识（col_3或row_3）
     */
    @Override
    public void setCursorTracking(int CurrChNo) {
        if (CurrChNo == TChan.Cursor_col_3) {                                // 垂直双光标跟踪
            VerIndex = 3;                                                    // 设置垂直索引为3
            switch (WorkModeManage.getInstance().getmWorkMode()) {           // 获取当前工作模式
                case WorkMode_YT:                                            // YT模式
                case WorkMode_YTZOOM:                                        // YT缩放模式
                    cursorManage_yt.setMultiSelectCursor(TChan.Cursor_col_1, TChan.Cursor_col_2); // 同时选中垂直光标1和2
                    break;
                case WorkMode_XY:                                            // XY模式
                    cursorManage_xy.setMultiSelectCursor(TChan.Cursor_col_1, TChan.Cursor_col_2); // 同时选中垂直光标1和2
                    break;
            }
        } else if (CurrChNo == TChan.Cursor_row_3) {                         // 水平双光标跟踪
            HorIndex = 3;                                                    // 设置水平索引为3
            switch (WorkModeManage.getInstance().getmWorkMode()) {           // 获取当前工作模式
                case WorkMode_YT:                                            // YT模式
                case WorkMode_YTZOOM:                                        // YT缩放模式
                    cursorManage_yt.setMultiSelectCursor(TChan.Cursor_row_1, TChan.Cursor_row_2); // 同时选中水平光标1和2
                    break;
                case WorkMode_XY:                                            // XY模式
                    cursorManage_xy.setMultiSelectCursor(TChan.Cursor_row_1, TChan.Cursor_row_2); // 同时选中水平光标1和2
                    break;
            }
        }
    }

    /**
     * 获取当前选中的光标类型标识
     *
     * @return 光标类型标识，0表示未选中
     */
    @Override
    public int getCurrSelectCursor() {
        switch (WorkModeManage.getInstance().getmWorkMode()) {               // 获取当前工作模式
            case WorkMode_YT:                                                // YT模式
            case WorkMode_YTZOOM:                                            // YT缩放模式
                return cursorManage_yt.getCurrSelectCursor();                // 返回YT模式当前选中光标
            case WorkMode_XY:                                                // XY模式
                return cursorManage_xy.getCurrSelectCursor();                // 返回XY模式当前选中光标

        }
        return 0;                                                            // 默认返回0
    }

    /**
     * 修改波形区域高度
     *
     * @param height 新的高度值
     */
    public void changeHeight(int height) {
//        cursorManage_xy.changeHeight(height);
        cursorManage_yt.changeHeight(height);                                // 更新YT模式光标高度
//        CursorManage.getInstance().initCursorY();
    }

    //endregion

    /**
     * 光标跟踪之时基，触发时刻
     * 当垂直光标可见且跟踪功能启用时，将光标位置按缓存中存储的时间值重新定位
     */
    public static void setCursorByTimebaseTrace() {
        try {
            if (CursorManage.getInstance().getColVisible() == false) return;   // 垂直光标不可见，直接返回
            if (getInstance().enableCursorTrance == false) return;             // 光标跟踪功能未启用，直接返回
            if (WorkModeManage.getInstance().isXyMode()) return;             // XY模式不适用，直接返回
            if (CursorManage.getInstance().isColCursorTrace() == false) return; // 垂直光标不需要跟踪，直接返回
            //if (CursorManage.getInstance().isAutoCursor()==false &&  CursorManage.getInstance().isSelectChanEqCursorChan()==false) return;
            double x1 = Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1)); // 读取垂直光标1的时间位置
            double x2 = Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2)); // 读取垂直光标2的时间位置
            if (CursorManage.getInstance().isFFT(WaveManage.get().getCurCh())) { // 如果当前通道是FFT
                x1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1_HZ); // 使用频率值
                x2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2_HZ); // 使用频率值
            }
            long pix1 = Tools.TimebaseToPix(TChan.toFpgaChNo(WaveManage.get().getCurCh()), x1); // 将时间值转换为像素位置
            long pix2 = Tools.TimebaseToPix(TChan.toFpgaChNo(WaveManage.get().getCurCh()), x2); // 将时间值转换为像素位置


            //Log.d("Tag.Debug", String.format("CursorManage.setCursorByTimebaseTrace: x1:%s, x2:%s  ,pix1:%s,pix2:%s ,curCh:%s",x1,x2,pix1,pix2,WaveManage.get().getCurCh() ));

            CursorManage.getInstance().setCursor(TChan.Cursor_col_1, (int) pix1); // 设置垂直光标1到计算出的像素位置
            CursorManage.getInstance().setCursor(TChan.Cursor_col_2, (int) pix2); // 设置垂直光标2到计算出的像素位置

//            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();                                             // 打印异常堆栈
        }
    }

    /**
     * delta标签移动 - 按偏移量移动垂直光标
     * 移动后保持光标间距不变，并更新缓存中的光标时间值
     *
     * @param offsetX X方向偏移像素数
     */
    public static void setCursorByTimebaseTrace(int offsetX) {
        try {
            if (CursorManage.getInstance().getColVisible() == false) return;   // 垂直光标不可见，直接返回
            if (getInstance().enableCursorTrance == false) return;             // 光标跟踪功能未启用，直接返回
            if (WorkModeManage.getInstance().isXyMode()) return;             // XY模式不适用，直接返回
            if (CursorManage.getInstance().isColCursorTrace() == false) return; // 垂直光标不需要跟踪，直接返回
            //if (CursorManage.getInstance().isAutoCursor()==false &&  CursorManage.getInstance().isSelectChanEqCursorChan()==false) return;
            double x1 = Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1)); // 读取垂直光标1的时间位置
            double x2 = Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2)); // 读取垂直光标2的时间位置
            double deltaX = Math.abs(x1 - x2);                                   // 计算两个光标之间的时间间距
            if (CursorManage.getInstance().isFFT(WaveManage.get().getCurCh())) { // 如果当前通道是FFT
                x1 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1_HZ); // 使用频率值
                x2 = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2_HZ); // 使用频率值
                deltaX = Math.abs(x1 - x2);                                      // 重新计算FFT频率间距
            }

            long curTimeX = 0;                                               // 当前时间零点像素偏移
            double timeEvery = 0;                                            // 每像素对应的时间值
            int chIdx = TChan.toFpgaChNo(WaveManage.get().getCurCh());        // 获取当前通道的FPGA编号
            if (ChannelFactory.isDynamicCh(chIdx)                            // 动态通道
                    || ChannelFactory.isMathCh(chIdx)                        // 数学通道
                    || ChannelFactory.isSerialCh(chIdx)) {                  // 串行通道
                if (ChannelFactory.isMath_FFT_Ch(chIdx)) {                   // FFT数学通道
                    HorizontalAxisMath horizontalAxisMath = ChannelFactory.getMathChannel(chIdx).getHorizontalAxisMathFFT(); // 获取FFT水平轴
                    curTimeX = ScopeBase.getWidth() / 2 - horizontalAxisMath.getXPosOfView(); // 计算屏幕中心到视图偏移
                    timeEvery = horizontalAxisMath.fftXScaleIdVal() / ScopeBase.getHorizonPerGridPixels(); // 计算每像素频率值
                } else {                                                     // 普通通道
                    curTimeX = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix(); // 计算屏幕中心到视图偏移
                    timeEvery = HorizontalAxis.getInstance().getTimesPrePix(); // 计算每像素时间值
                }
            } else if (ChannelFactory.isRefCh(chIdx)) {                      // 参考通道
                RefChannel refChannel = ChannelFactory.getRefChannel(chIdx); // 获取参考通道对象
                curTimeX = ScopeBase.getWidth() / 2 - refChannel.getTimePoseOfViewPix(); // 计算屏幕中心到视图偏移
                timeEvery = refChannel.getRefTimePerPix();                   // 计算每像素时间值
            }

            //Log.d("Tag.Debug", String.format("CursorManage.setCursorByTimebaseTrace: offsetX:%s, chEvery:%s ,x1:%s ,x2:%s",offsetX,timeEvery,x1,x2 ));
            x1 = (x1 - offsetX * timeEvery);                                      // 按偏移量更新光标1时间值
            x2 = (x2 - offsetX * timeEvery);                                       // 按偏移量更新光标2时间值

            long pix1 = Tools.TimebaseToPix(TChan.toFpgaChNo(WaveManage.get().getCurCh()), x1); // 将更新后的时间值转换为像素位置
            long pix2 = Tools.TimebaseToPix(TChan.toFpgaChNo(WaveManage.get().getCurCh()), x2); // 将更新后的时间值转换为像素位置


            //Log.d("Tag.Debug", String.format("CursorManage.setCursorByTimebaseTrace: offsetX:%s,pix1:%s,pix2:%s",offsetX,pix1,pix2 ));
            //Log.d("Tag.Debug", String.format("CursorManage.setCursorByTimebaseTrace: x1:%s,x2:%s ,delta:%s",x1,x2,deltaX ));
            if (CursorManage.getInstance().isFFT(WaveManage.get().getCurCh())) { // FFT通道
                if ((Math.abs(pix1 - pix2) * timeEvery) <= deltaX || !GlobalVar.get().isContainMainWaveX((int) pix1, (int) pix2)) { // 检查间距未缩小或光标超出波形区
                    //Log.d("Tag.Debug", String.format("CursorManage.setCursorByTimebaseTrace: save" ));
                    CursorManage.getInstance().setCursorTrace(true);         // 启用光标跟踪
                    CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1_HZ, String.valueOf(x1)); // 保存FFT光标1频率值
                    CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2_HZ, String.valueOf(x2)); // 保存FFT光标2频率值
                }
            } else {                                                          // 非FFT通道
                if ((Math.abs(pix1 - pix2) * timeEvery) <= deltaX || !GlobalVar.get().isContainMainWaveX((int) pix1, (int) pix2)) { // 检查间距未缩小或光标超出波形区
                    CursorManage.getInstance().setCursorTrace(true);         // 启用光标跟踪
                    CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X1, String.valueOf(x1)); // 保存光标1时间值
                    CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_X2, String.valueOf(x2)); // 保存光标2时间值
                }
            }

            CursorManage.getInstance().setCursor(TChan.Cursor_col_1, (int) pix1); // 设置垂直光标1到计算出的像素位置
            CursorManage.getInstance().setCursor(TChan.Cursor_col_2, (int) pix2); // 设置垂直光标2到计算出的像素位置
            CursorManage.getInstance().setCursorTrace(false);                // 关闭光标跟踪

//            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();                                             // 打印异常堆栈
        }
    }


    /**
     * 光标跟踪之幅度，按缓存中的幅度值重新定位水平光标
     * 当水平光标可见且跟踪功能启用时执行
     */
    public static void setCursorByScaleTrace() {
        try {
//            Log.d("Tag.Debug", String.format("CursorManage.setCursorByScaleTrace: begin" ));
            if (CursorManage.getInstance().getRowVisible() == false) return;   // 水平光标不可见，直接返回
            if (getInstance().enableCursorTrance == false) return;             // 光标跟踪功能未启用，直接返回
            if (CursorManage.getInstance().isRowCursorTrace() == false) return; // 水平光标不需要跟踪，直接返回

            if (WorkModeManage.getInstance().isXyMode()) return;             // XY模式不适用，直接返回
            if (TChan.isSerial(WaveManage.get().getCurCh())) return;         // 串行通道不适用，直接返回

            int chIdx = TChan.toFpgaChNo(WaveManage.get().getCurCh());        // 获取当前通道的FPGA编号
            if (CursorManage.getInstance().isScpiChanEqCursorChan()) {        // 如果SCPI指定了通道
                chIdx = CursorManage.getInstance().getScpiChanIdx();           // 使用SCPI指定的通道
                CursorManage.getInstance().setScpiChanIdx(-1);               // 重置SCPI通道索引
            }

            double y1 = Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y1)); // 读取水平光标1的幅度值
            double y2 = Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y2)); // 读取水平光标2的幅度值
            double pix1 = Tools.ScaleToPix(chIdx/*TChan.toFpgaChNo(WaveManage.get().getCurCh())*/, y1); // 将幅度值转换为像素位置
            double pix2 = Tools.ScaleToPix(chIdx/*TChan.toFpgaChNo(WaveManage.get().getCurCh())*/, y2); // 将幅度值转换为像素位置

            CursorManage.getInstance().setCursor(TChan.Cursor_row_1, (int) pix1); // 设置水平光标1到计算出的像素位置
            CursorManage.getInstance().setCursor(TChan.Cursor_row_2, (int) pix2); // 设置水平光标2到计算出的像素位置
            //Log.d("Tag.Debug", String.format("CursorManage.setCursorByScaleTrace: %s,%s",pix1,pix2 ));
        } catch (Exception e) {
            e.printStackTrace();                                             // 打印异常堆栈
        }
    }

    /**
     * 光标跟踪之幅度 - 按偏移量移动水平光标
     * 移动后保持光标间距不变，并更新缓存中的光标幅度值
     *
     * @param offsetY Y方向偏移像素数
     */
    public static void setCursorByScaleTrace(int offsetY) {
        try {
            if (CursorManage.getInstance().getRowVisible() == false) return;   // 水平光标不可见，直接返回
            if (getInstance().enableCursorTrance == false) return;             // 光标跟踪功能未启用，直接返回
//            if (CursorManage.getInstance().isRowCursorTrace()==false)  return;

            if (WorkModeManage.getInstance().isXyMode()) return;             // XY模式不适用，直接返回
            if (TChan.isSerial(WaveManage.get().getCurCh())) return;         // 串行通道不适用，直接返回

            int chIdx = TChan.toFpgaChNo(WaveManage.get().getCurCh());        // 获取当前通道的FPGA编号
            if (isAutoSwitchChannel() == false) {                                // 非自动切换通道模式
                chIdx = CursorManage.getInstance().getCursorChan();            // 使用光标指定通道
            }
            if (CursorManage.getInstance().isScpiChanEqCursorChan()) {        // 如果SCPI指定了通道
                chIdx = CursorManage.getInstance().getScpiChanIdx();           // 使用SCPI指定的通道
                CursorManage.getInstance().setScpiChanIdx(-1);               // 重置SCPI通道索引
            }

            double y1 = Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y1)); // 读取水平光标1的幅度值
            double y2 = Double.parseDouble(CacheUtil.get().getString(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y2)); // 读取水平光标2的幅度值
            double deltaY = Math.abs(y1 - y2);                                   // 计算两个光标之间的幅度间距

            double chEvery = 0;                                                // 每像素对应的幅度值
            if (ChannelFactory.isDynamicCh(chIdx)) {                         // 动态通道
                chEvery = ChannelFactory.getDynamicChannel(chIdx).getVerticalPerPix(); // 获取垂直方向每像素幅度值
            } else if (ChannelFactory.isMathCh(chIdx)) {                     // 数学通道
                chEvery = ChannelFactory.getMathChannel(chIdx).getVerticalPerPix(); // 获取垂直方向每像素幅度值
            } else if (ChannelFactory.isRefCh(chIdx)) {                      // 参考通道
                chEvery = ChannelFactory.getRefChannel(chIdx).getVerticalPerPix(); // 获取垂直方向每像素幅度值
            }
            y1 = (y1 - offsetY * chEvery);                                        // 按偏移量更新光标1幅度值
            y2 = (y2 - offsetY * chEvery);                                         // 按偏移量更新光标2幅度值

            double pix1 = Tools.ScaleToPix(chIdx/*TChan.toFpgaChNo(WaveManage.get().getCurCh())*/, y1); // 将更新后的幅度值转换为像素位置
            double pix2 = Tools.ScaleToPix(chIdx/*TChan.toFpgaChNo(WaveManage.get().getCurCh())*/, y2); // 将更新后的幅度值转换为像素位置

            if ((Math.abs(pix1 - pix2) * chEvery) <= deltaY || !GlobalVar.get().isContainMainWaveY((int) pix1, (int) pix2)) { // 检查间距未缩小或光标超出波形区
                CursorManage.getInstance().setCursorTrace(true);             // 启用光标跟踪
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y1, String.valueOf(y1)); // 保存光标1幅度值
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CURSOR_POSITION_Y2, String.valueOf(y2)); // 保存光标2幅度值
            }
            CursorManage.getInstance().setCursor(TChan.Cursor_row_1, (int) pix1); // 设置水平光标1到计算出的像素位置
            CursorManage.getInstance().setCursor(TChan.Cursor_row_2, (int) pix2); // 设置水平光标2到计算出的像素位置
            CursorManage.getInstance().setCursorTrace(false);                // 关闭光标跟踪
        } catch (Exception e) {
            e.printStackTrace();                                             // 打印异常堆栈
        }
    }

    /**
     * 通道选中颜色变更事件消费者
     * 当通道选中颜色变更时，刷新光标显示
     */
    private Consumer<String> consumerSelectColor = new Consumer<String>() {
        @Override
        public void accept(String colorInfo) throws Throwable {
            cursorManage_xy.refresh();                                        // 刷新XY模式光标显示
            cursorManage_yt.refresh();                                        // 刷新YT模式光标显示
        }
    };
}
