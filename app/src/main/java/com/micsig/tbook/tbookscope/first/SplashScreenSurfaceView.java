package com.micsig.tbook.tbookscope.first;   // 启动模块包：负责开机启动、权限申请、语言切换和启动画面显示

import android.content.Context;   // 导入Android上下文类，用于访问应用资源和加载图片
import android.graphics.Bitmap;   // 导入Android Bitmap类，用于存储和解码启动画面图片
import android.graphics.BitmapFactory;   // 导入Android BitmapFactory类，用于从资源解码Bitmap
import android.graphics.Canvas;   // 导入Android Canvas类，用于在Surface上绘制图形和文本
import android.graphics.Color;   // 导入Android Color类，提供颜色常量（BLACK、WHITE等）
import android.graphics.Paint;   // 导入Android Paint类，用于设置绘制样式（颜色、字号、抗锯齿等）
import android.graphics.PixelFormat;   // 导入Android PixelFormat类，用于设置Surface的像素格式
import android.graphics.Rect;   // 导入Android Rect类，用于定义图片绘制的源矩形和目标矩形
import android.os.Build;   // 导入Android Build类，用于获取SDK版本号
import android.os.SystemClock;   // 导入Android SystemClock类，用于获取系统启动后的经过时间
import android.util.AttributeSet;   // 导入Android AttributeSet类，用于XML属性解析
import android.view.SurfaceHolder;   // 导入Android SurfaceHolder类，用于管理Surface的绘制和生命周期
import android.view.SurfaceView;   // 导入Android SurfaceView类，提供独立绘图表面用于高性能渲染

import com.micsig.base.Logger;   // 导入自定义日志工具类，封装日志输出功能
import com.micsig.base.OEM;   // 导入OEM品牌定制工具类，用于加载OEM定制的启动画面图片
import com.micsig.tbook.hardware.HardwareProduct;   // 导入硬件产品类，用于识别产品型号
import com.micsig.tbook.tbookscope.GlobalVar;   // 导入全局变量类，提供屏幕尺寸等全局信息
import com.micsig.tbook.tbookscope.R;   // 导入资源类，访问raw资源中的默认启动画面图片


/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                                                                              │
 * │  SplashScreenSurfaceView - 启动画面SurfaceView                              │
 * │                                                                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   所属模块: first（启动模块）                                                │
 * │   所在层级: 视图层（View层）                                                 │
 * │   作用范围: 启动画面渲染与显示                                               │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                 │
 * │   1. 加载OEM启动画面图片（优先OEM定制，回退到默认资源）                      │
 * │   2. 在独立线程中绘制启动画面（避免阻塞主线程）                              │
 * │   3. 显示加载进度点动画（点号递增，1~6个点循环）                             │
 * │   4. 支持外部停止绘制（stop()方法）                                          │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                 │
 * │   继承自SurfaceView并实现SurfaceHolder.Callback接口。                        │
 * │   使用独立线程（drawThread）进行画面渲染，通过SurfaceHolder                 │
 * │   的lockCanvas/unlockCanvasAndPost机制实现双缓冲绘制。                      │
 * │                                                                              │
 * │   渲染循环设计：                                                              │
 * │   ┌─────────────────────────────────────────┐                               │
 * │   │  while(isRun) {                          │                               │
 * │   │    1. lockCanvas() 获取画布              │                               │
 * │   │    2. 绘制黑色背景                       │                               │
 * │   │    3. 绘制OEM启动画面图片                │                               │
 * │   │    4. 绘制加载进度点（. .. ... 等）      │                               │
 * │   │    5. unlockCanvasAndPost() 提交画布     │                               │
 * │   │    6. sleep(20ms) 控制帧率               │                               │
 * │   │  }                                       │                               │
 * │   └─────────────────────────────────────────┘                               │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                 │
 * │   构造函数 → SurfaceHolder初始化 → surfaceCreated() → drawThread.start()    │
 * │     → initView()（加载OEM图片/默认图片）→ 渲染循环：                        │
 * │       ├─ Canvas.drawColor(BLACK)        黑色背景                            │
 * │       ├─ Canvas.drawBitmap(bmp)         启动画面图片                        │
 * │       └─ Canvas.drawText(strLoad)       加载进度点                          │
 * │                                                                              │
 * │   stop()/surfaceDestroyed() → isRun=false → 渲染循环退出                    │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                 │
 * │   依赖: SurfaceView (Android高性能绘图视图基类)                              │
 * │   依赖: SurfaceHolder.Callback (Surface生命周期回调接口)                     │
 * │   依赖: OEM (品牌定制工具：加载OEM启动画面图片)                              │
 * │   依赖: GlobalVar (全局变量：获取屏幕尺寸)                                   │
 * │   依赖: Logger (自定义日志：记录Surface销毁事件)                             │
 * │   被依赖: SplashDialog (启动画面对话框：包含此SurfaceView)                   │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用示例】                                                                 │
 * │   // 在XML布局中使用                                                         │
 * │   &lt;com.micsig.tbook.tbookscope.first.SplashScreenSurfaceView               │
 * │       android:id="@+id/splash"                                               │
 * │       android:layout_width="match_parent"                                    │
 * │       android:layout_height="match_parent" /&gt;                               │
 * │                                                                              │
 * │   // 停止绘制                                                                │
 * │   splashScreenSurfaceView.stop();                                            │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【注意事项】                                                                 │
 * │   1. 绘制线程在surfaceCreated()时启动，surfaceDestroyed()时停止             │
 * │   2. lockCanvas()返回null时表示Surface尚未就绪，需sleep后重试               │
 * │   3. 帧率约50fps（20ms/帧），满足启动画面流畅显示需求                       │
 * │   4. 加载进度点每秒增加一个，6个点后循环（0~5秒对应1~6个点）               │
 * │   5. OEM图片优先加载，不存在时回退到R.raw.smart_oscilloscope_mho_10008      │
 * │   6. stop()方法仅设置isRun=false，不中断线程，线程会在下次循环判断时退出    │
 * └──────────────────────────────────────────────────────────────────────────────┘
 *
 * @see SurfaceView
 * @see SurfaceHolder.Callback
 * @see OEM
 * @see SplashDialog
 * @author Micsig智能示波器团队
 * @version 1.0
 * @since MHO Series Oscilloscope Software
 */
public class SplashScreenSurfaceView extends SurfaceView implements SurfaceHolder.Callback {   // 启动画面SurfaceView：在独立线程中绘制OEM启动画面和加载进度点动画

    /**
     * 日志标签 - 用于调试日志输出
     *
     * <p>TAG常量，用于Logger日志输出，标识日志来源为SplashScreenSurfaceView类。</p>
     */
    private static final String TAG = "SplashScreenSurfaceView";   // 日志标签：标识日志来源为SplashScreenSurfaceView类

    /**
     * Surface持有者 - 管理Surface的绘制和生命周期
     *
     * <p>SurfaceHolder对象，通过getHolder()获取，用于：</p>
     * <ul>
     *   <li>注册SurfaceHolder.Callback回调</li>
     *   <li>lockCanvas()/unlockCanvasAndPost()进行双缓冲绘制</li>
     *   <li>设置Surface像素格式</li>
     * </ul>
     */
    private SurfaceHolder holder ;   // Surface持有者：管理Surface的绘制操作和生命周期回调

    /**
     * 上下文对象 - 用于访问应用资源和加载图片
     *
     * <p>Context对象，在构造函数中传入，用于：</p>
     * <ul>
     *   <li>BitmapFactory.decodeResource()加载默认启动画面图片</li>
     *   <li>访问应用的raw资源</li>
     * </ul>
     */
    private Context context;   // 上下文对象：用于访问应用资源和加载默认启动画面图片

    /**
     * 启动画面图片Bitmap - 存储加载的OEM或默认启动画面图片
     *
     * <p>Bitmap对象，存储从OEM定制路径或默认资源加载的启动画面图片。</p>
     * <ul>
     *   <li>优先从OEM.getSplashScreen()加载OEM定制图片</li>
     *   <li>OEM图片不存在时，从R.raw.smart_oscilloscope_mho_10008加载默认图片</li>
     *   <li>在initView()方法中初始化</li>
     * </ul>
     */
    private Bitmap bmp;   // 启动画面图片Bitmap：存储OEM定制或默认的启动画面图片

    /**
     * 绘制线程 - 在独立线程中执行启动画面的渲染循环
     *
     * <p>Thread对象，在构造函数中创建，在surfaceCreated()中启动。
     * 线程执行run Runnable中的渲染循环逻辑，通过isRun标志控制循环退出。</p>
     *
     * <h4>线程生命周期</h4>
     * <ul>
     *   <li>创建：构造函数中new Thread(run)</li>
     *   <li>启动：surfaceCreated()中drawThread.start()</li>
     *   <li>退出：isRun=false时渲染循环自然退出</li>
     * </ul>
     */
    private  Thread drawThread;   // 绘制线程：在独立线程中执行启动画面的渲染循环

    /**
     * gif动画绘制的开始X坐标 - 已废弃，当前未使用
     *
     * <p>初始值240，为早期gif动画绘制预留的X坐标。
     * 当前版本中启动画面图片全屏绘制，此变量未被使用。</p>
     *
     * @deprecated 当前版本中启动画面图片全屏绘制，此坐标未使用
     */
    private int x=240;   // gif动画绘制开始X坐标：已废弃，当前版本中启动画面全屏绘制

    /**
     * gif动画绘制的开始Y坐标 - 已废弃，当前未使用
     *
     * <p>初始值180，为早期gif动画绘制预留的Y坐标。
     * 当前版本中启动画面图片全屏绘制，此变量未被使用。</p>
     *
     * @deprecated 当前版本中启动画面图片全屏绘制，此坐标未使用
     */
    private int y =180;   // gif动画绘制开始Y坐标：已废弃，当前版本中启动画面全屏绘制

    /**
     * 渲染循环运行标志 - 控制绘制线程的渲染循环是否继续执行
     *
     * <p>boolean标志，初始值为true。当设置为false时，渲染循环在下一次迭代时退出。</p>
     *
     * <h4>状态变化</h4>
     * <ul>
     *   <li>初始值：true（构造时）</li>
     *   <li>stop()调用：设置为false</li>
     *   <li>surfaceDestroyed()：设置为false</li>
     * </ul>
     */
    private boolean isRun=true;   // 渲染循环运行标志：控制绘制线程的渲染循环是否继续执行

    /**
     * 加载进度点Y坐标 - 进度点文本的绘制位置（屏幕底部上方20像素）
     *
     * <p>进度点文本绘制在屏幕底部上方20像素处，用于显示应用加载状态。</p>
     *
     * <h4>计算方式</h4>
     * <p>dotY = 屏幕高度 - 20</p>
     */
    private int dotY=GlobalVar.get().getScreen().height()  -20;   // 加载进度点Y坐标：屏幕底部上方20像素处

    /**
     * 图片源矩形 - 定义启动画面图片的源区域（整个图片）
     *
     * <p>Rect对象，定义从bmp中截取的区域。在initView()中设置为
     * (0, 0, bmp.getWidth(), bmp.getHeight())，即整个图片区域。</p>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li>Canvas.drawBitmap(bmp, bmpSrc, ScreenDes, paint)中的源矩形参数</li>
     * </ul>
     */
    private Rect bmpSrc=new Rect();   // 图片源矩形：定义启动画面图片的截取区域（整个图片）

    /**
     * 屏幕目标矩形 - 定义启动画面图片的目标绘制区域（全屏）
     *
     * <p>Rect对象，从GlobalVar获取屏幕尺寸，定义启动画面图片的绘制目标区域。
     * 图片将从bmpSrc缩放绘制到ScreenDes区域，实现全屏显示。</p>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li>Canvas.drawBitmap(bmp, bmpSrc, ScreenDes, paint)中的目标矩形参数</li>
     * </ul>
     */
    private Rect ScreenDes=GlobalVar.get().getScreen();   // 屏幕目标矩形：定义启动画面图片的全屏绘制区域

    /**
     * 渲染循环Runnable - 定义绘制线程的执行逻辑
     *
     * <p>Runnable对象，定义了启动画面的渲染循环逻辑：</p>
     * <ol>
     *   <li>调用initView()加载启动画面图片</li>
     *   <li>创建Paint画笔对象，设置白色、50号字体、粗体、抗锯齿</li>
     *   <li>进入渲染循环（while(isRun)）</li>
     *   <li>锁定画布 → 绘制黑色背景 → 绘制启动画面图片 → 绘制加载进度点 → 提交画布</li>
     *   <li>每帧休眠20ms（约50fps）</li>
     * </ol>
     *
     * <h4>加载进度点动画说明</h4>
     * <p>进度点根据系统启动后的经过时间计算：</p>
     * <ul>
     *   <li>每秒增加一个点</li>
     *   <li>6秒后循环（count % 6）</li>
     *   <li>显示效果：. → .. → ... → .... → ..... → ...... → .（循环）</li>
     * </ul>
     */
    private Runnable run = new Runnable() {   // 渲染循环Runnable：定义绘制线程的执行逻辑
        @Override
        public void run() {   // 绘制线程的run方法：执行启动画面的渲染循环
            initView();   // 初始化视图：加载OEM启动画面图片或默认资源图片
            Paint paint = new Paint();   // 创建画笔：用于绘制文本和图片
            paint.setColor(Color.WHITE);   // 设置画笔颜色：白色，用于绘制加载进度点
            paint.setTextSize(50);   // 设置字号：50像素，确保进度点清晰可见
            paint.setFakeBoldText(true);   // 启用粗体：使进度点更加醒目
            paint.setAntiAlias(true);   // 启用抗锯齿：使文本绘制更加平滑
            long startTime = SystemClock.elapsedRealtime();   // 记录开始时间：用于计算加载进度点的数量


            while (isRun) {   // 渲染循环：isRun为true时持续绘制
                Canvas canvas = holder.lockCanvas();   // 锁定画布：获取Canvas对象进行绘制，返回null表示Surface尚未就绪
                if (canvas==null){   // 判断：画布是否获取失败（Surface未就绪）
                    try {   // 尝试休眠
                        Thread.sleep(20);   // 休眠20ms：等待Surface就绪后重试
                        continue;   // 跳过本次循环：不进行绘制操作
                    } catch (InterruptedException e) {   // 捕获中断异常
                        e.printStackTrace();   // 打印异常堆栈
                    }   // 异常捕获结束
                }   // 画布获取失败判断结束
                canvas.drawColor(Color.BLACK);   // 绘制黑色背景：清除上一帧内容，填充黑色背景
                if (bmp!=null) {   // 判断：启动画面图片是否加载成功
                    canvas.drawBitmap(bmp, bmpSrc,ScreenDes, paint);   // 绘制启动画面图片：从bmpSrc源矩形缩放绘制到ScreenDes目标矩形
                }   // 图片加载判断结束
                long count = (SystemClock.elapsedRealtime() - startTime) / 1000;   // 计算经过秒数：用于确定进度点数量
                String strLoad = "";   // 初始化进度点字符串
                count %= 6;   // 取模6：实现6秒循环，0~5秒对应1~6个点
                for(int i=0;i<=count;i++){   // 循环：根据经过秒数生成对应数量的点
                    strLoad += ".";   // 追加一个点号
                }   // 循环结束
                canvas.drawText(strLoad,10,dotY,paint);   // 绘制进度点文本：在屏幕底部左侧绘制加载进度点

                holder.unlockCanvasAndPost(canvas);   // 提交画布：将绘制内容显示到屏幕上，并释放画布锁
                try {   // 尝试休眠
                    Thread.sleep(20);   // 休眠20ms：控制帧率约50fps，避免CPU占用过高
                } catch (InterruptedException e) {   // 捕获中断异常
                    e.printStackTrace();   // 打印异常堆栈
                }   // 异常捕获结束
            }   // 渲染循环结束
        }   // run方法结束
    };   // 渲染循环Runnable定义结束

    /**
     * 构造函数 - 初始化SurfaceView和绘制线程
     *
     * <p>在XML布局 inflate 时调用，执行以下初始化操作：</p>
     * <ol>
     *   <li>调用父类构造函数</li>
     *   <li>保存Context引用</li>
     *   <li>获取SurfaceHolder并注册回调</li>
     *   <li>创建绘制线程</li>
     *   <li>设置Surface像素格式为RGBA_8888</li>
     * </ol>
     *
     * <h4>初始化流程</h4>
     * <pre>
     * 构造函数(context, attrs)
     *   │
     *   ├─ super(context, attrs)        父类初始化
     *   ├─ this.context = context       保存上下文
     *   ├─ holder = getHolder()         获取SurfaceHolder
     *   ├─ holder.addCallback(this)     注册生命周期回调
     *   ├─ drawThread = new Thread(run) 创建绘制线程
     *   └─ holder.setFormat(RGBA_8888)  设置像素格式
     * </pre>
     *
     * @param context 上下文对象，用于访问应用资源
     * @param attrs XML属性集，从布局文件中解析的属性
     */
    public SplashScreenSurfaceView(Context context, AttributeSet attrs){   // 构造函数：初始化SurfaceView、注册回调并创建绘制线程
        super(context,attrs);   // 调用父类构造函数：初始化SurfaceView
        this.context=context;   // 保存上下文引用：用于后续加载默认启动画面图片
        holder = getHolder();// 电影的播放器   // 获取SurfaceHolder：用于管理Surface的绘制操作
        holder.addCallback(this);   // 注册生命周期回调：将当前对象注册为SurfaceHolder.Callback，接收Surface生命周期事件
        drawThread=new Thread(run);   // 创建绘制线程：使用run Runnable创建线程，但尚未启动
        holder.setFormat(PixelFormat.RGBA_8888);   // 设置像素格式：RGBA_8888为32位像素格式，支持透明度，确保启动画面色彩准确
    }   // 构造函数结束

    /**
     * 初始化视图 - 加载启动画面图片
     *
     * <p>在绘制线程启动时调用，加载启动画面图片。加载策略：</p>
     * <ol>
     *   <li>优先从OEM定制路径加载启动画面图片（OEM.getSplashScreen()）</li>
     *   <li>OEM图片不存在时，从应用raw资源加载默认图片（R.raw.smart_oscilloscope_mho_10008）</li>
     *   <li>图片加载成功后，设置bmpSrc源矩形为整个图片区域</li>
     * </ol>
     *
     * <h4>图片加载策略</h4>
     * <pre>
     * initView()
     *   │
     *   ├─ bmp = OEM.getSplashScreen()    尝试加载OEM定制图片
     *   │
     *   ├─ bmp == null（OEM图片不存在）
     *   │   ├─ 创建BitmapFactory.Options
     *   │   │   ├─ inPreferredConfig = ARGB_8888  32位像素格式
     *   │   │   └─ inScaled = false               不缩放
     *   │   └─ bmp = BitmapFactory.decodeResource(R.raw.smart_oscilloscope_mho_10008)
     *   │
     *   └─ bmp != null（图片加载成功）
     *       └─ bmpSrc.set(0, 0, bmp.getWidth(), bmp.getHeight())  设置源矩形
     * </pre>
     *
     * <h4>注意事项</h4>
     * <ul>
     *   <li>OEM图片路径：/private/oem/smart_oscilloscope.png</li>
     *   <li>默认图片资源：R.raw.smart_oscilloscope_mho_10008</li>
     *   <li>inScaled=false确保图片不被系统自动缩放，保持原始尺寸</li>
     * </ul>
     */
    private void initView() {   // 初始化视图：加载OEM定制启动画面图片或默认资源图片
        bmp = OEM.getSplashScreen();   // 尝试加载OEM定制图片：从/private/oem/smart_oscilloscope.png加载
        if(bmp == null){   // 判断：OEM定制图片是否不存在
            BitmapFactory.Options op = new BitmapFactory.Options();   // 创建BitmapFactory选项：用于配置图片解码参数
            op.inPreferredConfig = Bitmap.Config.ARGB_8888;   // 设置像素格式：ARGB_8888为32位像素格式，支持透明度
            op.inScaled = false;   // 禁用自动缩放：保持图片原始尺寸，避免系统根据DPI缩放
            int id = R.raw.smart_oscilloscope_mho_10008;   // 默认启动画面资源ID：MHO系列示波器的默认启动画面
            bmp = BitmapFactory.decodeResource(context.getResources(), id, op);   // 从资源解码图片：使用配置的选项从raw资源加载默认启动画面
        }   // OEM图片不存在判断结束
        if(bmp != null) {   // 判断：图片是否加载成功
            bmpSrc.set(0, 0, bmp.getWidth(), bmp.getHeight());   // 设置源矩形：覆盖整个图片区域，用于后续drawBitmap的源参数
        }   // 图片加载成功判断结束
    }   // initView方法结束


    /**
     * Surface创建回调 - 启动绘制线程
     *
     * <p>当SurfaceView的Surface创建完成时调用。此时Surface已就绪，
     * 可以开始绘制操作，因此启动绘制线程开始渲染启动画面。</p>
     *
     * <h4>调用时机</h4>
     * <ul>
     *   <li>SurfaceView首次附加到Window时</li>
     *   <li>Surface从销毁状态重新创建时</li>
     * </ul>
     *
     * @param holder Surface持有者对象
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {   // Surface创建回调：Surface就绪后启动绘制线程
        drawThread.start();   // 启动绘制线程：开始执行run Runnable中的渲染循环
    }   // surfaceCreated方法结束

    /**
     * Surface尺寸变更回调 - 当前未使用
     *
     * <p>当Surface的尺寸或格式发生变化时调用。当前版本中未实现任何逻辑，
     * 因为启动画面图片通过bmpSrc和ScreenDes矩形自动缩放适配。</p>
     *
     * @param holder Surface持有者对象
     * @param format 新的像素格式
     * @param width 新的宽度
     * @param height 新的高度
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {   // Surface尺寸变更回调：当前未使用，启动画面通过矩形自动缩放适配

    }   // surfaceChanged方法结束

    /**
     * 停止绘制 - 设置渲染循环退出标志
     *
     * <p>通过设置isRun=false，使渲染循环在下一次迭代时退出。
     * 此方法由SplashDialog在关闭启动画面时调用，用于停止动画渲染。</p>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li>SplashDialog.closeDialog()中调用，关闭启动画面时停止动画</li>
     * </ul>
     *
     * <h4>注意事项</h4>
     * <ul>
     *   <li>此方法不会立即中断线程，线程会在下一次while(isRun)判断时退出</li>
     *   <li>最迟20ms后线程退出（一帧的休眠时间）</li>
     * </ul>
     */
    public void stop(){   // 停止绘制：设置isRun=false，使渲染循环在下一次迭代时退出
        isRun=false;   // 设置运行标志为false：渲染循环将在下一次while判断时退出
    }   // stop方法结束

    /**
     * Surface销毁回调 - 停止渲染循环
     *
     * <p>当SurfaceView的Surface即将被销毁时调用。设置isRun=false
     * 停止渲染循环，避免在Surface销毁后继续绘制导致异常。</p>
     *
     * <h4>调用时机</h4>
     * <ul>
     *   <li>SurfaceView从Window分离时</li>
     *   <li>Activity销毁时</li>
     * </ul>
     *
     * @param holder Surface持有者对象
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {   // Surface销毁回调：停止渲染循环，避免Surface销毁后继续绘制
        isRun = false;   // 设置运行标志为false：停止渲染循环
        Logger.d(TAG,"surfaceDestroyed");   // 记录日志：输出Surface销毁事件，便于调试
    }   // surfaceDestroyed方法结束


}   // SplashScreenSurfaceView类结束
