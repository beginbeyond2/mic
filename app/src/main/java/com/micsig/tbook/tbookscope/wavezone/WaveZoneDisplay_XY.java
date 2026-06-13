package com.micsig.tbook.tbookscope.wavezone; // 包名：示波器波形显示区域组件

import android.content.Context; // 导入：Android上下文类
import android.graphics.Color; // 导入：Android颜色类
import android.os.Handler; // 导入：Android消息处理器
import android.os.Message; // 导入：Android消息类
import android.view.MotionEvent; // 导入：Android触摸事件类
import android.widget.TextView; // 导入：Android文本视图控件

import com.chillingvan.canvasgl.ICanvasGL; // 导入：OpenGL画布接口
import com.chillingvan.canvasgl.glview.texture.GLTextureView; // 导入：OpenGL纹理视图基类
import com.micsig.base.Logger; // 导入：日志工具类
import com.micsig.tbook.scope.channel.Channel; // 导入：通道数据模型
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入：通道工厂类
import com.micsig.tbook.tbookscope.GlobalVar; // 导入：全局变量管理
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入：主视图容器
import com.micsig.tbook.tbookscope.R; // 导入：资源文件引用
import com.micsig.tbook.ui.wavezone.IChan; // 导入：通道接口
import com.micsig.tbook.tbookscope.middleware.MiddleMain; // 导入：中间层主控制器
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入：RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入：RxJava事件枚举
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入：缓存工具类
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 导入：光标管理器
import com.micsig.tbook.tbookscope.wavezone.display.WaveGridManage; // 导入：波形网格管理器
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage; // 导入：测量管理器
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage; // 导入：波形管理器
import com.micsig.tbook.ui.util.TBookUtil; // 导入：工具类
import com.micsig.tbook.ui.wavezone.IWave; // 导入：波形接口
import com.micsig.tbook.ui.wavezone.TChan; // 导入：通道类型定义

/**
 * XY模式波形显示类 - 实现示波器的X-Y图形模式（李萨如图形）
 * 
 * <h3>架构定位</h3>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                        MainViewGroup                            │
 * │  ┌──────────────────────────────────────────────────────────┐  │
 * │  │              WaveZoneDisplay_XY (本类)                    │  │
 * │  │  ┌────────────────────────────────────────────────────┐  │  │
 * │  │  │              GLTextureView (OpenGL渲染)            │  │  │
 * │  │  └────────────────────────────────────────────────────┘  │  │
 * │  │                                                           │  │
 * │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │  │
 * │  │  │ WaveManage  │  │CursorManage │  │MeasureManage│     │  │
 * │  │  └─────────────┘  └─────────────┘  └─────────────┘     │  │
 * │  │         │                │                  │           │  │
 * │  │         └────────────────┴──────────────────┘           │  │
 * │  │                          │                              │  │
 * │  │                  ┌───────▼───────┐                      │  │
 * │  │                  │  onGLDraw()   │                      │  │
 * │  │                  └───────────────┘                      │  │
 * │  └──────────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>模块职责</h3>
 * <ul>
 *   <li><b>XY模式渲染</b>：实现X-Y图形模式，将两个通道信号分别映射到X轴和Y轴</li>
 *   <li><b>波形绘制</b>：通过OpenGL ES绘制李萨如图形波形</li>
 *   <li><b>光标管理</b>：支持XY模式下的光标测量功能</li>
 *   <li><b>触摸交互</b>：处理单指拖动、双指操作等手势事件</li>
 *   <li><b>工作模式切换</b>：实现IWorkMode接口，支持YT/XY模式切换</li>
 * </ul>
 * 
 * <h3>XY模式说明</h3>
 * <p>
 * XY模式是示波器的一种特殊显示模式，与常规的YT模式（时间-幅度）不同：
 * </p>
 * <ul>
 *   <li><b>YT模式</b>：X轴表示时间，Y轴表示幅度</li>
 *   <li><b>XY模式</b>：X轴表示通道1幅度，Y轴表示通道2幅度</li>
 * </ul>
 * <p>
 * 应用场景：
 * </p>
 * <ul>
 *   <li>李萨如图形分析：测量两个信号的频率比和相位差</li>
 *   <li>相位测量：通过图形形状判断相位关系</li>
 *   <li>频率比测量：通过图形的环数计算频率比</li>
 *   <li>I-V特性曲线：电流-电压特性分析</li>
 * </ul>
 * 
 * <h3>核心依赖</h3>
 * <pre>
 * ┌──────────────────┐
 * │ WaveZoneDisplay  │
 * │      _XY         │
 * └────────┬─────────┘
 *          │
 *    ┌─────┴─────┬──────────────┬──────────────┬──────────────┐
 *    │           │              │              │              │
 *    ▼           ▼              ▼              ▼              ▼
 * ┌────────┐ ┌────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
 * │WaveGrid│ │Cursor  │  │Measure   │  │  Wave    │  │WorkMode  │
 * │Manage  │ │Manage  │  │Manage    │  │  Manage  │  │Manage    │
 * └────────┘ └────────┘  └──────────┘  └──────────┘  └──────────┘
 *    │           │              │              │              │
 *    │           │              │              │              │
 *    ▼           ▼              ▼              ▼              ▼
 * 网格绘制    光标操作      测量显示      波形数据      模式切换
 * </pre>
 * 
 * <h3>触摸事件处理流程</h3>
 * <pre>
 * ACTION_DOWN → 选择对象（光标/通道）
 *      │
 *      ├─ 选择光标 → 设置高亮 → 发送滑动方向事件
 *      │
 *      └─ 选择通道 → 设置活动通道 → 发送滑动方向事件
 * 
 * ACTION_MOVE → 判断滑动方向 → 移动对象
 *      │
 *      ├─ MOVE_LEFTRIGHT → 水平移动
 *      │
 *      └─ MOVE_UPDOWN → 垂直移动
 * 
 * ACTION_UP → 完成移动操作
 * 
 * 多指操作：
 * ACTION_POINTER_DOWN → 记录基准点
 * ACTION_POINTER_UP → 切换跟踪模式
 * </pre>
 * 
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在MainViewGroup中创建XY模式显示视图
 * WaveZoneDisplay_XY xyDisplay = new WaveZoneDisplay_XY(context, mainViewGroup);
 * 
 * // 切换到XY模式
 * WorkModeManage.getInstance().switchWorkMode(IWorkMode.WorkMode_XY);
 * 
 * // 强制清屏
 * xyDisplay.forceClearGlCanvas();
 * }</pre>
 * 
 * <h3>注意事项</h3>
 * <ul>
 *   <li>XY模式下只支持通道1和通道2</li>
 *   <li>光标位置初始化与YT模式不同</li>
 *   <li>测量功能比YT模式少一个参数</li>
 *   <li>所有GL操作必须在GL线程中执行</li>
 * </ul>
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/10/30
 * @see IWorkMode 工作模式接口
 * @see GLTextureView OpenGL纹理视图基类
 * @see WaveManage 波形管理器
 * @see CursorManage 光标管理器
 * @see MeasureManage 测量管理器
 */
public class WaveZoneDisplay_XY extends GLTextureView implements IWorkMode {
    
    // ==================== 常量定义 ====================
    
    /** 日志标签：用于标识XY模式显示类的日志输出 */
    private static final String TAG = "WaveZoneDisplay_XY";
    
    // ==================== 成员变量 ====================
    
    /** Android上下文对象：用于访问资源和系统服务 */
    private Context context;
    
    /** 主视图容器：父容器，用于访问其他UI组件 */
    private MainViewGroup mainViewGroup;
    
    /** 性能统计：开始时间（毫秒） */
    private long startTime;
    
    /** 性能统计：结束时间（毫秒） */
    private long endTime;
    
    /** 性能统计：时间差（毫秒） */
    private long dt;
    
    /** 消息处理器：用于线程间通信，处理工作模式切换 */
    private Handler handler;
    
    // ==================== 构造函数 ====================
    
    /**
     * 构造函数：初始化XY模式波形显示视图
     * 
     * <p>主要完成以下工作：</p>
     * <ul>
     *   <li>调用父类构造函数初始化GLTextureView</li>
     *   <li>设置透明背景，实现波形叠加显示</li>
     *   <li>创建消息处理器，处理工作模式切换</li>
     *   <li>记录初始化时间，用于性能统计</li>
     * </ul>
     * 
     * @param context Android上下文对象，不能为null
     * @param mainViewGroup 主视图容器，用于访问父容器中的其他组件
     * 
     * @see GLTextureView#GLTextureView(Context)
     */
    public WaveZoneDisplay_XY(Context context, MainViewGroup mainViewGroup) {
        super(context); // 调用父类构造函数
        this.context = context; // 保存上下文引用
        this.mainViewGroup = mainViewGroup; // 保存主视图容器引用
        this.setBackgroundColor(Color.TRANSPARENT); // 设置背景透明，实现波形叠加效果
        this.setOpaque(false); // 设置视图不透明度为false，支持透明渲染
        startTime = System.currentTimeMillis(); // 记录初始化时间，用于性能统计

        // 创建消息处理器：处理工作模式切换的延迟消息
        handler = new Handler() {
            /**
             * 处理消息回调
             * 
             * @param msg 消息对象
             */
            @Override
            public void handleMessage(Message msg) {
                int w = WorkModeManage.getInstance().getmWorkMode(); // 获取当前工作模式
                switch (msg.what) {
                    case 0: { // 消息类型0：切换工作模式
                        WorkModeManage.getInstance().switchWorkMode(w); // 执行工作模式切换
                    }
                    break;
                    case 1: // 消息类型1：保留，暂未使用
                        break;
                }

                // super.handleMessage(msg); // 注释掉父类消息处理
            }
        };
    }
    
    // ==================== 公共方法 ====================
    
    /**
     * 强制清屏：清除OpenGL画布内容
     * 
     * <p>通过GL线程队列提交清屏任务，清空画布为透明色。</p>
     * <p>使用场景：</p>
     * <ul>
     *   <li>切换工作模式时清除旧波形</li>
     *   <li>重置显示区域</li>
     *   <li>清除残留图像</li>
     * </ul>
     * 
     * <p><b>线程安全：</b>使用synchronized同步锁确保线程安全</p>
     * 
     * @see ICanvasGL#clearBuffer(int)
     */
    public void forceClearGlCanvas() {
        queueEvent(new Runnable() { // 提交任务到GL线程队列
            @Override
            public void run() {
                synchronized (this) { // 同步锁，确保线程安全
                    WaveZoneDisplay_XY.this.mCanvas.clearBuffer(Color.TRANSPARENT); // 清空画布为透明色
                }
            }
        });

    }

    // ==================== IWorkMode接口实现 ====================

    /**
     * 切换工作模式：实现IWorkMode接口
     * 
     * <p>当工作模式切换时，通知各个管理器进行相应的状态更新：</p>
     * <ol>
     *   <li>MeasureManage：切换测量显示模式</li>
     *   <li>WaveManage：切换波形绘制模式</li>
     *   <li>CursorManage：切换光标显示模式</li>
     * </ol>
     * 
     * <p><b>调用时机：</b></p>
     * <ul>
     *   <li>用户切换YT/XY模式时</li>
     *   <li>WorkModeManage状态变为Ready时</li>
     * </ul>
     * 
     * @param workMode 工作模式常量，取值范围：
     *                  <ul>
     *                    <li>{@link IWorkMode#WorkMode_YT} - YT模式（时间-幅度）</li>
     *                    <li>{@link IWorkMode#WorkMode_XY} - XY模式（李萨如图形）</li>
     *                  </ul>
     * 
     * @see IWorkMode#switchWorkMode(int)
     * @see MeasureManage#switchWorkMode(int)
     * @see WaveManage#switchWorkMode(int)
     * @see CursorManage#switchWorkMode(int)
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        MeasureManage.getInstance().switchWorkMode(workMode); // 切换测量管理器的工作模式
        WaveManage.get().switchWorkMode(workMode); // 切换波形管理器的工作模式
        CursorManage.getInstance().switchWorkMode(workMode); // 切换光标管理器的工作模式
    }


    // ==================== GLContinuousView生命周期方法 ====================

    /**
     * 初始化：GL视图初始化回调
     * 
     * <p>在GL上下文创建后调用，用于初始化OpenGL资源。</p>
     * <p>当前实现仅调用父类初始化方法，未添加额外逻辑。</p>
     * 
     * @see GLTextureView#init()
     */
    @Override
    protected void init() {
        super.init(); // 调用父类初始化方法
    }

    /**
     * GL绘制：每帧绘制回调（约25FPS）
     * 
     * <p>核心绘制方法，在GL线程中每帧调用一次，负责绘制XY模式的波形显示。</p>
     * 
     * <h4>绘制流程：</h4>
     * <pre>
     * 1. 检查工作模式状态
     *    └─ 如果模式正在切换 → 发送延迟消息 → 返回
     * 
     * 2. 清空画布（透明背景）
     * 
     * 3. 绘制网格
     *    └─ XY模式网格与YT模式不同
     * 
     * 4. 绘制光标
     *    └─ 光标位置初始化与YT模式不同
     * 
     * 5. 绘制测量
     *    └─ XY模式测量参数比YT模式少
     * 
     * 6. 绘制波形
     *    └─ 只绘制通道1和通道2
     * 
     * 7. 标记加载完成
     * </pre>
     * 
     * <h4>线程安全：</h4>
     * <p>使用synchronized同步锁确保多线程访问安全</p>
     * 
     * <h4>性能优化：</h4>
     * <ul>
     *   <li>帧率控制在25FPS左右</li>
     *   <li>模式切换时跳过绘制，避免闪烁</li>
     *   <li>使用透明背景实现叠加效果</li>
     * </ul>
     * 
     * @param canvas OpenGL画布对象，用于绑定绘制命令
     * 
     * @see WaveGridManage#draw(int, ICanvasGL)
     * @see CursorManage#draw(ICanvasGL)
     * @see MeasureManage#draw(ICanvasGL)
     * @see WaveManage#draw(ICanvasGL)
     */
    @Override
    protected void onGLDraw(ICanvasGL canvas) {
        // 帧率：约25帧/秒

        synchronized (this) { // 同步锁，确保线程安全
            // 检查工作模式是否正在切换
            if (WorkModeManage.getInstance().isWorkModeChange()) {
                // 如果工作模式管理器已准备就绪
                if (WorkModeManage.getInstance().getWorkModeManageState() == WorkModeManage.WorkModeManage_Ready) {
                    handler.sendEmptyMessage(0); // 发送消息，触发工作模式切换
                    return; // 返回，不执行绘制
                }
                return; // 模式正在切换，跳过本次绘制
            }

            // ========== 开始绘制 ==========
            canvas.clearBuffer(Color.TRANSPARENT); // 清空画布为透明色
            WaveGridManage.getInstance().draw(IWorkMode.WorkMode_XY, canvas); // 绘制XY模式网格
            // 光标绘制：注意初始化位置与YT模式不同
            CursorManage.getInstance().draw(canvas); // 绘制光标
            // 测量绘制：XY模式只有光标测量，且参数比YT模式少
            MeasureManage.getInstance().draw(canvas); // 绘制测量信息
            // 波形绘制：XY模式只支持通道1和通道2
            WaveManage.get().draw(canvas); // 绘制波形
        }
        CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_WaveZoneDisplayManage, true); // 标记波形显示区域加载完成

    }

    // ==================== 触摸事件处理 ====================
    
    // 触摸事件相关成员变量
    /** 当前选中的对象索引：光标或通道编号 */
    int index = -1;    // 线号：-1表示未选中，正数表示选中的对象编号
    
    /** 上一个触摸点的X坐标：用于计算移动偏移量 */
    int oldX, oldY;    // 上一个点的坐标
    
    /** 滑动方向：-1未确定，1左右滑动，2上下滑动 */
    // 向一个方向滑动后，向另一个方向滑动就不管用
    int slideDirection = -1; // -1 为没有点击 1，左右，2上下
    
    /** 按下时的X坐标：用于判断滑动方向 */
    int downX, downY;
    
    /** 滑动判定阈值：超过此距离才判定为滑动（像素） */
    private final static int MOVESIZE = 10;
    
    /** 滑动方向常量：左右滑动 */
    public final static int MOVE_LEFTRIGHT = 1;
    
    /** 滑动方向常量：上下滑动 */
    public final static int MOVE_UPDOWN = 2;
    
    /** 微调文本显示控件：显示微调数值 */
    private TextView btnFineText;
    
    /**
     * 触摸事件处理：处理用户手势交互
     * 
     * <p>支持以下手势操作：</p>
     * <ul>
     *   <li><b>单指拖动</b>：移动光标或调整通道位置</li>
     *   <li><b>双指操作</b>：联动移动光标</li>
     *   <li><b>方向锁定</b>：滑动开始后锁定方向，避免误操作</li>
     * </ul>
     * 
     * <h4>触摸事件流程：</h4>
     * <pre>
     * ACTION_DOWN（按下）
     *   ├─ 选择光标 → 设置高亮 → 发送滑动方向事件
     *   └─ 选择通道 → 设置活动通道 → 发送滑动方向事件
     * 
     * ACTION_MOVE（移动）
     *   ├─ 判断滑动方向（左右/上下）
     *   ├─ 处理光标移动
     *   │   ├─ 单光标移动
     *   │   └─ 联动光标移动
     *   └─ 处理通道位置调整
     *       ├─ 通道1：调整X轴位置或零点偏移
     *       └─ 通道2：调整Y轴位置或零点偏移
     * 
     * ACTION_UP（抬起）
     *   └─ 完成光标移动操作
     * 
     * ACTION_POINTER_DOWN（多指按下）
     *   └─ 记录基准点
     * 
     * ACTION_POINTER_UP（多指抬起）
     *   └─ 切换光标跟踪模式
     * </pre>
     * 
     * <h4>光标类型说明：</h4>
     * <ul>
     *   <li>{@link TChan#Cursor_col_1} - 垂直光标1</li>
     *   <li>{@link TChan#Cursor_col_2} - 垂直光标2</li>
     *   <li>{@link TChan#Cursor_row_1} - 水平光标1</li>
     *   <li>{@link TChan#Cursor_row_2} - 水平光标2</li>
     *   <li>{@link TChan#Cursor_col_3} - 垂直光标3（联动）</li>
     *   <li>{@link TChan#Cursor_row_3} - 水平光标3（联动）</li>
     *   <li>{@link TChan#Cursor_col_4} - 垂直光标4</li>
     *   <li>{@link TChan#Cursor_row_4} - 水平光标4</li>
     *   <li>{@link TChan#Cursor_all} - 全部光标</li>
     *   <li>{@link TChan#SingleRect} - 单矩形光标</li>
     * </ul>
     * 
     * @param event 触摸事件对象，包含触摸点坐标、动作类型等信息
     * @return 始终返回true，表示事件已处理
     * 
     * @see CursorManage#selectCursor(int, int)
     * @see WaveManage#selectCursor(int, int)
     * @see #dealCursor(MotionEvent, int)
     * @see #cursorSingleMove(boolean, int, MotionEvent)
     * @see #cursorLinkMove(boolean, int, MotionEvent, int)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX(); // 获取当前触摸点X坐标
        int y = (int) event.getY(); // 获取当前触摸点Y坐标
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // ========== 单指按下事件 ==========
            case MotionEvent.ACTION_DOWN: {
                // 处理是否有对象被选择
                downX = oldX = (int) event.getX(); // 记录按下时的X坐标
                downY = oldY = (int) event.getY(); // 记录按下时的Y坐标
                slideDirection = -1; // 重置滑动方向为未确定

                // 尝试选择光标
                index = CursorManage.getInstance().selectCursor(downX, downY); // 检测是否点击到光标
                if (index > 0) { // 如果选中了光标
                    CursorManage.getInstance().setSelectCursor(index); // 设置当前选中的光标
                    CursorManage.getInstance().setSelectHighColor(index); // 设置光标高亮颜色
                    // 根据光标类型判断滑动方向
                    if (TChan.isCursorRow2(index)) { // 如果是水平光标（row类型）
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, MOVE_UPDOWN); // 发送上下滑动方向事件
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象事件
                    } else { // 如果是垂直光标（col类型）
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, MOVE_LEFTRIGHT); // 发送左右滑动方向事件
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象事件
                    }
                } else if ((index = WaveManage.get().selectCursor(x, y)) >= 0) { // 如果选中了波形通道
                    WaveManage.get().setSelectCursor(index); // 设置当前选中的通道
                    MiddleMain.getIns().getChanSelectorManage().setActivityChannel(IChan.toIChan(index-1)); // 设置活动通道
//                    RxBus.getInstance().post(RxEnum.MAINCENTER_CHANNEL_SELECT, new MainCenterMsgChannels(index)); // 注释掉的代码
                    RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象事件
                }
            }
            break;
            
            // ========== 单指移动事件 ==========
            case MotionEvent.ACTION_MOVE: {
                // 判断滑动方向（首次移动时）
                if (slideDirection == -1) { // 如果滑动方向未确定
                    // 判断是否为左右滑动
                    if (Math.abs((int) event.getX() - downX) > MOVESIZE) { // X方向移动超过阈值
                        slideDirection = MOVE_LEFTRIGHT; // 设置为左右滑动
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, slideDirection); // 发送滑动方向事件
                        // 如果当前选中的不是光标，则默认选择通道1
                        if (TChan.isCursor8(index)==false) { // 判断是否为有效光标
                            WaveManage.get().setSelectCursor(TChan.Ch1); // 默认选择通道1
                            index = TChan.Ch1; // 更新当前选中索引
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Ch1); // 发送最后操作对象事件
                        } else {
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象事件
                        }
                    } else if (Math.abs((int) event.getY() - downY) > MOVESIZE) { // Y方向移动超过阈值
                        slideDirection = MOVE_UPDOWN; // 设置为上下滑动
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, slideDirection); // 发送滑动方向事件
                        // 如果当前选中的不是光标，则默认选择通道2
                        if (TChan.isCursor8(index)==false) { // 判断是否为有效光标
                            WaveManage.get().setSelectCursor(TChan.Ch2); // 默认选择通道2
                            index = TChan.Ch2; // 更新当前选中索引
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, TChan.Ch2); // 发送最后操作对象事件
                        } else {
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象事件
                        }
                    }
                }
                
                // 处理双指光标联动
                dealCursor(event, slideDirection); // 处理双指光标联动逻辑
                
                // 获取微调参数
                boolean fine = TBookUtil.isFine(); // 是否启用微调模式
                int numFine = TBookUtil.getNumFine(); // 微调步进值
                
                // 根据选中的对象类型执行不同的移动逻辑
                switch (index) {
                    // ========== 单光标移动 ==========
                    case TChan.Cursor_col_1: // 垂直光标1
                    case TChan.Cursor_col_2: // 垂直光标2
                    case TChan.Cursor_row_1: // 水平光标1
                    case TChan.Cursor_row_2: // 水平光标2
                        cursorSingleMove(fine,numFine,event); // 执行单光标移动
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象事件
                        break;
                        
                    // ========== 联动光标移动 ==========
                    case TChan.Cursor_col_3: // 垂直光标3（联动）
                    case TChan.Cursor_row_3: // 水平光标3（联动）
                    case TChan.Cursor_row_4: // 水平光标4
                    case TChan.Cursor_col_4: // 垂直光标4
                    case TChan.Cursor_all: // 全部光标
                    case TChan.SingleRect: { // 单矩形光标
                        if (slideDirection==-1) break; // 如果滑动方向未确定，跳过
                        
                        // 特殊处理：某些光标只能沿特定方向移动
                        if ( (slideDirection==MOVE_UPDOWN && (index == TChan.Cursor_col_4 || index == TChan.SingleRect))
                                || (slideDirection==MOVE_LEFTRIGHT && (index == TChan.Cursor_row_4 || index == TChan.SingleRect))) {
                            // 移动光标标签
                            int offsetX = oldX - (int) event.getX(); // 计算X方向偏移量
                            int offsetY = oldY - (int) event.getY(); // 计算Y方向偏移量
                            CursorManage.getInstance().MoveLabel(offsetX, offsetY); // 移动光标标签位置
                        }else {
                            cursorLinkMove(fine, numFine, event,index); // 执行联动光标移动
                            RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION_LASTOBJECT, index); // 发送最后操作对象事件
                        }
                        oldX=(int)event.getX(); // 更新上一个触摸点X坐标
                        oldY=(int)event.getY(); // 更新上一个触摸点Y坐标
                    }break;
                    
                    // ========== 通道1位置调整 ==========
                    case TChan.Ch1: {
                        int offsetX = oldX - (int) event.getX(); // 计算X方向偏移量
                        int offsetY = oldY - (int) event.getY(); // 计算Y方向偏移量

                        int channelIndex = ChannelFactory.CH1; // 通道1索引
                        Channel channel = ChannelFactory.getDynamicChannel(channelIndex); // 获取通道1对象
                        // 判断是否启用零点调整模式
                        if (GlobalVar.get().isEnableChannelZero()) {
                            channel.setZero(channel.getZero() - offsetX); // 调整零点位置
                            // 更新微调文本显示
                            if (btnFineText == null) {
                                btnFineText = (TextView) mainViewGroup.findViewById(R.id.fineText); // 获取微调文本控件
                            }
                            btnFineText.setText(TBookUtil.getD4FromD(channel.getMoveZeroVal())); // 显示零点偏移值
                        } else {
                            WaveManage.get().setOffsetY(offsetX); // 调整波形Y轴偏移
                        }

//                        if (fine) { // 注释掉的微调逻辑
//                            if (Math.abs(offsetX) > numFine) {
//                                WaveManage.get().setOffsetY(offsetX / numFine);
//                                oldX = (int) event.getX();
//                            }
//                        } else {
//                            WaveManage.get().setOffsetY(offsetX);
                            oldX = (int) event.getX(); // 更新上一个触摸点X坐标
                            oldY = (int) event.getY(); // 更新上一个触摸点Y坐标
//                        }
                    }
                    break;
                    
                    // ========== 通道2位置调整 ==========
                    case TChan.Ch2: {
                        int offsetX = oldX - (int) event.getX(); // 计算X方向偏移量
                        int offsetY = oldY - (int) event.getY(); // 计算Y方向偏移量

                        int channelIndex = ChannelFactory.CH2; // 通道2索引
                        Channel channel = ChannelFactory.getDynamicChannel(channelIndex); // 获取通道2对象
                        // 判断是否启用零点调整模式
                        if (GlobalVar.get().isEnableChannelZero()) {
                            channel.setZero(channel.getZero() + offsetY); // 调整零点位置
                            // 更新微调文本显示
                            if (btnFineText == null) {
                                btnFineText = (TextView) mainViewGroup.findViewById(R.id.fineText); // 获取微调文本控件
                            }
                            btnFineText.setText(TBookUtil.getD4FromD(channel.getMoveZeroVal())); // 显示零点偏移值
                        } else {
                            WaveManage.get().setOffsetY(offsetY); // 调整波形Y轴偏移
                        }

//                        if (fine) { // 注释掉的微调逻辑
//                            if (Math.abs(offsetY) > numFine) {
//                                WaveManage.get().setOffsetY(offsetY / numFine);
//                                oldY = (int) event.getY();
//                            }
//                        } else {
//                            WaveManage.get().setOffsetY(offsetY);
                            oldX = (int) event.getX(); // 更新上一个触摸点X坐标
                            oldY = (int) event.getY(); // 更新上一个触摸点Y坐标
//                        }
                    }
                    break;
                }

            }
            break;
            
            // ========== 单指抬起事件 ==========
            case MotionEvent.ACTION_UP: {
                // 根据选中的光标类型执行完成操作
                switch (index) {
                    case TChan.Cursor_col_1: // 垂直光标1
                    case TChan.Cursor_col_2: // 垂直光标2
                    case TChan.Cursor_col_3: // 垂直光标3
                    case TChan.Cursor_row_1: // 水平光标1
                    case TChan.Cursor_row_2: // 水平光标2
                    case TChan.Cursor_row_3: // 水平光标3
                    case TChan.Cursor_col_4: // 垂直光标4
                    case TChan.Cursor_row_4: // 水平光标4
                    case TChan.Cursor_all: // 全部光标
                        CursorManage.getInstance().moveFinish(); // 完成光标移动操作
                        break;
                }
            }
            break;
            
            // ========== 多指按下事件 ==========
            case MotionEvent.ACTION_POINTER_DOWN: {
//                Logger.i(TAG,"down multi:"+index+" pointerCount:"+event.getPointerCount()); // 注释掉的日志
                downX = oldX = (int) event.getX(0); // 记录第一个触摸点的X坐标
                downY = oldY = (int) event.getY(0); // 记录第一个触摸点的Y坐标
//                int i = CursorManage.getInstance().getCurrSelectCursor(); // 注释掉的代码
//                if (event.getPointerCount() == 2 && i > 0) { // 注释掉的代码
//                    index = i; // 注释掉的代码
//                    CursorManage.getInstance().setCursorTracking(index); // 注释掉的代码
//                }
            }
            break;
            
            // ========== 多指抬起事件 ==========
            case MotionEvent.ACTION_POINTER_UP: {
                int id = event.getActionIndex(); // 获取抬起的触摸点索引
                // 更新基准点为剩余触摸点的坐标
                if (id == 0) { // 如果抬起的是第一个触摸点
                    downX = oldX = (int) event.getX(1); // 使用第二个触摸点作为基准
                    downY = oldY = (int) event.getY(1); // 使用第二个触摸点作为基准
                } else { // 如果抬起的是第二个触摸点
                    downX = oldX = (int) event.getX(0); // 使用第一个触摸点作为基准
                    downY = oldY = (int) event.getY(0); // 使用第一个触摸点作为基准
                }
                Logger.i(TAG, "up index:" + index); // 打印日志
                // 根据光标类型设置跟踪模式
                if (index == TChan.Cursor_row_1 || index == TChan.Cursor_row_2
                        || index == TChan.Cursor_col_1 || index == TChan.Cursor_col_2) {
                    CursorManage.getInstance().setSelectCursor(index); // 设置选中的光标
                    // 发送滑动方向事件
                    if (TChan.isCursorRow2(index)) // 判断是否为水平光标
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, MOVE_UPDOWN); // 发送上下滑动事件
                    else
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, MOVE_LEFTRIGHT); // 发送左右滑动事件
                } else if (index == TChan.Cursor_col_3 || index == TChan.Cursor_row_3) {
                    CursorManage.getInstance().setCursorTracking(index); // 设置光标跟踪模式
                    // 发送滑动方向事件
                    if (index == TChan.Cursor_row_3) { // 如果是水平光标3
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, MOVE_UPDOWN); // 发送上下滑动事件
                    } else { // 如果是垂直光标3
                        RxBus.getInstance().post(RxEnum.WAVEZONE_SLIDEDIRECTION, MOVE_LEFTRIGHT); // 发送左右滑动事件
                    }
                }
            }
            break;
        }
        return true; // 返回true，表示事件已处理
    }

    /**
     * 处理双指光标联动
     * 
     * <p>当检测到双指操作时，根据滑动方向切换到联动光标模式。</p>
     * 
     * <h4>联动逻辑：</h4>
     * <ul>
     *   <li>左右滑动 + 双指 → 切换到垂直光标3（联动模式）</li>
     *   <li>上下滑动 + 双指 → 切换到水平光标3（联动模式）</li>
     * </ul>
     * 
     * @param event 触摸事件对象
     * @param slideDirection 滑动方向（MOVE_LEFTRIGHT或MOVE_UPDOWN）
     * @return true表示已处理双指联动，false表示未处理
     * 
     * @see CursorManage#setCursorTracking(int)
     */
    private boolean dealCursor(MotionEvent event, int slideDirection) {
        if (event.getPointerCount() == 2) { // 检测是否为双指操作
            if (slideDirection == MOVE_LEFTRIGHT) { // 如果是左右滑动
                int i = CursorManage.getInstance().getCurrSelectCursor(); // 获取当前选中的光标
                if (i > 0 && CursorManage.getInstance().getColVisible()) { // 如果光标有效且垂直光标可见
                    index = TChan.Cursor_col_3; // 切换到垂直光标3（联动模式）
                    CursorManage.getInstance().setCursorTracking(index); // 设置光标跟踪模式
                    return true; // 返回已处理
                }
            } else if (slideDirection == MOVE_UPDOWN) { // 如果是上下滑动
                int i = CursorManage.getInstance().getCurrSelectCursor(); // 获取当前选中的光标
                if (i > 0 && CursorManage.getInstance().getRowVisible()) { // 如果光标有效且水平光标可见
                    index = TChan.Cursor_row_3; // 切换到水平光标3（联动模式）
                    CursorManage.getInstance().setCursorTracking(index); // 设置光标跟踪模式
                    return true; // 返回已处理
                }
            }
        }
        return false; // 返回未处理
    }

    /**
     * 光标单体滑动：移动单个光标
     * 
     * <p>根据触摸移动距离移动单个光标，支持微调模式。</p>
     * 
     * <h4>移动逻辑：</h4>
     * <ul>
     *   <li><b>普通模式</b>：直接根据偏移量移动光标</li>
     *   <li><b>微调模式</b>：偏移量超过微调步进值才移动，实现精细控制</li>
     * </ul>
     * 
     * <h4>适用光标类型：</h4>
     * <ul>
     *   <li>{@link TChan#Cursor_col_1} - 垂直光标1</li>
     *   <li>{@link TChan#Cursor_col_2} - 垂直光标2</li>
     *   <li>{@link TChan#Cursor_row_1} - 水平光标1</li>
     *   <li>{@link TChan#Cursor_row_2} - 水平光标2</li>
     * </ul>
     * 
     * @param fine 是否启用微调模式
     * @param numFine 微调步进值（像素）
     * @param event 触摸事件对象
     * 
     * @see CursorManage#moveSelectCursor(int, int)
     */
    private void cursorSingleMove(boolean fine,int numFine,MotionEvent event){
        int offsetX = oldX - (int) event.getX(); // 计算X方向偏移量
        int offsetY = oldY - (int) event.getY(); // 计算Y方向偏移量
        if (fine) { // 如果启用微调模式
            if (Math.abs(offsetX) > numFine) { // X方向偏移超过微调步进值
                CursorManage.getInstance().moveSelectCursor(offsetX / numFine, offsetY / numFine); // 移动光标（除以步进值）
                oldX = (int) event.getX(); // 更新上一个触摸点X坐标
            }
            if (Math.abs(offsetY) > numFine) { // Y方向偏移超过微调步进值
                CursorManage.getInstance().moveSelectCursor(offsetX / numFine, offsetY / numFine); // 移动光标（除以步进值）
                oldY = (int) event.getY(); // 更新上一个触摸点Y坐标
            }
        } else { // 普通模式
            CursorManage.getInstance().moveSelectCursor(offsetX, offsetY); // 直接移动光标
            oldX = (int) event.getX(); // 更新上一个触摸点X坐标
            oldY = (int) event.getY(); // 更新上一个触摸点Y坐标
        }
    }
    
    /**
     * 光标联动滑动：移动联动光标
     * 
     * <p>根据触摸移动距离移动联动光标，支持微调模式。</p>
     * <p>联动光标会同时移动多个光标，保持它们之间的相对位置关系。</p>
     * 
     * <h4>移动逻辑：</h4>
     * <ul>
     *   <li><b>普通模式</b>：直接根据偏移量移动联动光标</li>
     *   <li><b>微调模式</b>：偏移量超过微调步进值才移动，实现精细控制</li>
     * </ul>
     * 
     * <h4>适用光标类型：</h4>
     * <ul>
     *   <li>{@link TChan#Cursor_col_3} - 垂直光标3（联动）</li>
     *   <li>{@link TChan#Cursor_row_3} - 水平光标3（联动）</li>
     *   <li>{@link TChan#Cursor_col_4} - 垂直光标4</li>
     *   <li>{@link TChan#Cursor_row_4} - 水平光标4</li>
     * </ul>
     * 
     * @param fine 是否启用微调模式
     * @param numFine 微调步进值（像素）
     * @param event 触摸事件对象
     * @param cursorIdx 光标索引
     * 
     * @see CursorManage#moveMultiSelectCursor(int, int, int)
     */
    private void cursorLinkMove(boolean fine,int numFine,MotionEvent event,int cursorIdx){
        int offsetX = oldX - (int) event.getX(); // 计算X方向偏移量
        int offsetY = oldY - (int) event.getY(); // 计算Y方向偏移量
        if (fine) { // 如果启用微调模式
            if (Math.abs(offsetX) > numFine) { // X方向偏移超过微调步进值
                CursorManage.getInstance().moveMultiSelectCursor(offsetX / numFine, offsetY / numFine,cursorIdx); // 移动联动光标（除以步进值）
                oldX = (int) event.getX(); // 更新上一个触摸点X坐标
            }
            if (Math.abs(offsetY) > numFine) { // Y方向偏移超过微调步进值
                CursorManage.getInstance().moveMultiSelectCursor(offsetX / numFine, offsetY / numFine,cursorIdx); // 移动联动光标（除以步进值）
                oldY = (int) event.getY(); // 更新上一个触摸点Y坐标
            }
        } else { // 普通模式
            CursorManage.getInstance().moveMultiSelectCursor(offsetX, offsetY,cursorIdx); // 直接移动联动光标
            oldX = (int) event.getX(); // 更新上一个触摸点X坐标
            oldY = (int) event.getY(); // 更新上一个触摸点Y坐标
        }
    }
    
    // ==================== 触摸事件处理结束 ====================
}