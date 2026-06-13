package com.micsig.tbook.tbookscope.wavezone.wave;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.config.ScopeConfig;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.ISerialBus;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.ICharacterEncoding;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusStruct;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusStructParse;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStructParse;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialImageBuffer;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialImageDoubleCache;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialTxtBuffer;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SerialBus - 串行总线解码显示类
 * 
 * <h2>一、架构定位</h2>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                     示波器波形显示系统                           │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐      │
 * │  │  数据采集层   │───▶│  数据解析层   │───▶│  波形显示层   │      │
 * │  │ (硬件/FPGA)  │    │(SerialBusStructParse)│  │ (SerialBus) │      │
 * │  └──────────────┘    └──────────────┘    └──────────────┘      │
 * │          │                   │                   │              │
 * │          ▼                   ▼                   ▼              │
 * │  ┌──────────────────────────────────────────────────────────┐  │
 * │  │              SerialImageDoubleCache (双缓冲)              │  │
 * │  └──────────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h2>二、模块职责</h2>
 * <ul>
 *   <li><b>核心职责</b>：串行总线协议解码与波形显示</li>
 *   <li><b>支持协议</b>：UART、LIN、CAN、SPI、I2C、ARINC429、MIL-STD-1553B（共7种）</li>
 *   <li><b>显示功能</b>：解码数据可视化、协议标题绘制、通道图标绘制</li>
 *   <li><b>位置管理</b>：垂直位置移动、工作模式切换（YT/YTZOOM）</li>
 *   <li><b>状态管理</b>：可见性控制、选中状态管理</li>
 * </ul>
 * 
 * <h2>三、串行总线解码显示流程</h2>
 * <pre>
 * ┌─────────────┐
 * │ 数据输入     │ ByteBuffer (原始字节数据)
 * └──────┬──────┘
 *        ▼
 * ┌─────────────┐
 * │OnDataChange │ 接收数据变更通知
 * └──────┬──────┘
 *        ▼
 * ┌─────────────┐
 * │线程池提交    │ ExecutorService.execute(toParseRunnable)
 * └──────┬──────┘
 *        ▼
 * ┌─────────────┐
 * │drawSerialBus│ 核心绘制方法
 * └──────┬──────┘
 *        ▼
 * ┌─────────────┐
 * │双缓冲读取    │ SerialImageDoubleCache.getLastCache()
 * └──────┬──────┘
 *        ▼
 * ┌─────────────┐
 * │协议解析      │ SerialBusStructParse.toParse()
 * └──────┬──────┘
 *        ▼
 * ┌─────────────┐
 * │Canvas绘制    │ 绘制到bmpData位图
 * └──────┬──────┘
 *        ▼
 * ┌─────────────┐
 * │GL渲染        │ ICanvasGL.drawBitmap()
 * └─────────────┘
 * </pre>
 * 
 * <h2>四、依赖关系</h2>
 * <pre>
 * SerialBus
 *   ├── 实现接口
 *   │   ├── IWave (波形接口：位置、颜色、可见性、选中状态)
 *   │   ├── ISerialBus (串行总线接口：数据变更通知)
 *   │   └── IWorkMode (工作模式接口：YT/YTZOOM切换)
 *   │
 *   ├── 核心依赖
 *   │   ├── SerialImageDoubleCache (双缓冲管理)
 *   │   ├── SerialBusStructParse (协议解析器)
 *   │   ├── SerialBusTxtStructParse (文本解析器)
 *   │   ├── SerialTxtBuffer (文本缓冲)
 *   │   └── WorkModeManage (工作模式管理)
 *   │
 *   └── 配置依赖
 *       ├── ScopeConfig (权限配置)
 *       ├── GlobalVar (全局变量)
 *       └── CacheUtil (缓存工具)
 * </pre>
 * 
 * <h2>五、设计模式</h2>
 * <ul>
 *   <li><b>观察者模式</b>：实现ISerialBus接口，响应数据变更事件</li>
 *   <li><b>策略模式</b>：根据serialBusType动态选择不同的协议解析策略</li>
 *   <li><b>双缓冲模式</b>：使用SerialImageDoubleCache避免绘制闪烁</li>
 *   <li><b>线程池模式</b>：使用ExecutorService异步处理解码任务</li>
 *   <li><b>模板方法模式</b>：drawSerialBus定义绘制流程骨架，子步骤可扩展</li>
 * </ul>
 * 
 * <h2>六、使用场景</h2>
 * <pre>
 * // 场景1：创建串行总线实例
 * SerialBus serialBus = new SerialBus();
 * serialBus.setLineNameId(TChan.S1);
 * serialBus.setColor(Color.YELLOW);
 * serialBus.setVisible(true);
 * 
 * // 场景2：设置UART参数
 * serialBus.setUartBits(8);                    // 数据位：8位
 * serialBus.setUartEncoding(ICharacterEncoding.Hex);  // 编码：十六进制
 * serialBus.setUartChecked(false);             // 校验：奇偶校验
 * 
 * // 场景3：垂直移动位置
 * serialBus.setY(350);  // 设置Y坐标
 * 
 * // 场景4：切换工作模式
 * serialBus.switchWorkMode(IWorkMode.WorkMode_YTZOOM);
 * 
 * // 场景5：获取解码数据
 * List&lt;SerialBusStruct.UartStruct&gt; uartList = serialBus.getImageBufferList(SerialBusStruct.UartStruct.class);
 * </pre>
 * 
 * <h2>七、线程安全说明</h2>
 * <ul>
 *   <li>使用synchronized(lock)保护Canvas绘制操作</li>
 *   <li>使用ExecutorService单线程池处理解码任务</li>
 *   <li>双缓冲机制确保读写分离</li>
 * </ul>
 * 
 * <h2>八、性能优化</h2>
 * <ul>
 *   <li>位图资源预加载：构造函数中加载resBmp[4][6]共24个图标</li>
 *   <li>纹理刷新优化：isChanageBitmap标志避免不必要的纹理刷新</li>
 *   <li>异步解码：线程池处理避免阻塞UI线程</li>
 * </ul>
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/9/26
 * @see IWave
 * @see ISerialBus
 * @see IWorkMode
 * @see SerialImageDoubleCache
 * @see SerialBusStructParse
 */
public class SerialBus implements IWave, ISerialBus, IWorkMode {
    
    /** 日志标签 */
    private static final String TAG = "SerialBus";

    //region ==================== IWave接口属性 ====================
    
    /** 通道名称ID（TChan.S1/S2/S3/S4） */
    private int nameID;
    
    /** X轴位置（时间轴，像素坐标） */
    private long x;
    
    /** Y轴位置（屏幕实际像素位置） */
    private double y = 350;
    
    /** Y轴位置（1000对应的位置，用于FPGA坐标转换） */
    private double posY;
    
    /** 波形颜色 */
    private int color;
    
    /** 是否被选中 */
    private boolean selected;
    
    /** 选中状态变更事件监听器 */
    private OnSelectChangeEvent onSelectChangeEvent = null;
    
    /** 波形移动事件监听器 */
    private OnMovingWaveEvent onMovingWaveEvent = null;
    
    /** 是否可见 */
    private boolean visible;
    
    //endregion

    //region ==================== 绘图相关属性 ====================
    
    /** 数据位图（存储解码后的波形图像） */
    private Bitmap bmpData;
    
    /** 数据画布（用于绘制bmpData） */
    private Canvas canvasData;
    
    /** 画笔（用于绘制文本和线条） */
    private Paint mPaint;
    
    /** 位图是否已变更标志（用于优化纹理刷新） */
    private boolean isChanageBitmap = false;
    
    /** OpenGL画布引用（用于纹理刷新） */
    private ICanvasGL canvasGL;
    
    /**
     * 刷新纹理
     * <p>当位图数据更新后，通知OpenGL重新加载纹理</p>
     */
    public void onRefresh(){
        if(canvasGL != null){
            canvasGL.onRefreshTexture();
        }
    }
    
    //endregion

    //region ==================== 串行总线配置属性 ====================
    
    /** 串行总线类型（UART/LIN/CAN/SPI/I2C/ARINC429/MIL-STD-1553B） */
    private int serialBusType = SerialBusStruct.SerialBusType_UART;
    
    /** 原始字节数据缓冲区 */
    private ByteBuffer bytes = null;
    
    /** 时间到像素的转换系数 */
    private long timeToPix;
    
    /** 绘制起始X坐标 */
    private int startX, endX;
    
    /** 内容区域顶部坐标 */
    private int contentTop;
    
    /** 文本宽度测量矩形 */
    private Rect rectTextWidth = new Rect();
    
    /** 光标矩形（用于边界检测） */
    private Rect CursorRect;
    
    //endregion

    //region ==================== 线程与缓冲管理 ====================
    
    /** 线程同步锁 */
    private final Object lock=new Object();
    
    /** 解码任务Runnable */
    private SerialBusToParseRunnable toParseRunnable=new SerialBusToParseRunnable();
    
    /** 固定大小线程池（单线程，确保解码顺序） */
    final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
    
    /** 串行总线文本缓冲区 */
    private SerialTxtBuffer serialTxtBuffer = new SerialTxtBuffer();
    
    /** 串行总线结构解析器 */
    private SerialBusStructParse serialBusStructParse=new SerialBusStructParse();
    
    /**
     * 获取串行总线文本缓冲区
     * @return 文本缓冲区实例
     */
    public SerialTxtBuffer getSerialTxtBuffer() {
        return this.serialTxtBuffer;
    }
    
    //endregion

    //region ==================== 串行总线配置结构体 ====================
    
    /** UART配置结构体 */
    public SerialBusStruct.UartSettingStruct uartSettingStruct = SerialBusStruct.getInstance().new UartSettingStruct();
    
    /** LIN配置结构体 */
    public SerialBusStruct.LinSettingStruct linSettingStruct = SerialBusStruct.getInstance().new LinSettingStruct();
    
    /** CAN配置结构体 */
    public SerialBusStruct.CanSettingStruct canSettingStruct = SerialBusStruct.getInstance().new CanSettingStruct();
    
    /** SPI配置结构体 */
    public SerialBusStruct.SpiSettingStruct spiSettingStruct = SerialBusStruct.getInstance().new SpiSettingStruct();
    
    /** I2C配置结构体 */
    public SerialBusStruct.I2cSettingStruct i2cSettingStruct = SerialBusStruct.getInstance().new I2cSettingStruct();
    
    /** ARINC429配置结构体 */
    public SerialBusStruct.Arinc429SettingStruct arinc429SettingStruct = SerialBusStruct.getInstance().new Arinc429SettingStruct();
    
    /** MIL-STD-1553B配置结构体 */
    public SerialBusStruct.MilSID1553bSettingStruct milSID1553bSettingStruct = SerialBusStruct.getInstance().new MilSID1553bSettingStruct();

    //region ==================== UART配置方法 ====================
    
    /**
     * 设置UART校验位
     * @param checked false=奇偶校验，true=无校验
     */
    public void setUartChecked(boolean checked) {
        uartSettingStruct.setChecked(checked);
    }

    /**
     * 设置UART数据位
     * @param bits 数据位（5-9位）
     */
    public void setUartBits(int bits) {
        uartSettingStruct.setUartLength(bits);
    }

    /**
     * 设置UART编码方式
     * @param encoding 编码方式（Hex/ASCII/DEC等）
     * @see ICharacterEncoding
     */
    public void setUartEncoding(int encoding) {
        uartSettingStruct.setEncoding(encoding);
    }
    //endregion

    //region ==================== LIN配置方法 ====================
    
    /**
     * 设置LIN编码方式
     * @param encoding 编码方式（Hex/ASCII/DEC等）
     */
    public void setLinEncoding(int encoding) {
        linSettingStruct.setEncoding(encoding);
        this.encoding = linSettingStruct.getEncoding();
    }
    //endregion

    //region ==================== CAN配置方法 ====================
    
    /**
     * 设置CAN编码方式
     * @param encoding 编码方式（Hex/ASCII/DEC等）
     */
    public void setCanEncoding(int encoding) {
        canSettingStruct.setEncoding(encoding);
        this.encoding = canSettingStruct.getEncoding();
    }
    //endregion

    //region ==================== SPI配置方法 ====================
    
    /**
     * 设置SPI编码方式
     * @param encoding 编码方式（Hex/ASCII/DEC等）
     */
    public void setSPIEncoding(int encoding) {
        spiSettingStruct.setEncoding(encoding);
        this.encoding = spiSettingStruct.getEncoding();
    }

    /**
     * 设置SPI数据位
     * @param bits 数据位
     */
    public void setSpiBits(int bits) {
        spiSettingStruct.setDataBit(bits);
    }
    //endregion

    //region ==================== I2C配置方法 ====================
    
    /**
     * 设置I2C编码方式
     * @param encoding 编码方式（Hex/ASCII/DEC等）
     */
    public void setI2CEncoding(int encoding) {
        i2cSettingStruct.setEncoding(encoding);
        this.encoding = i2cSettingStruct.getEncoding();
    }
    //endregion

    //region ==================== ARINC429配置方法 ====================
    
    /**
     * 设置ARINC429编码方式
     * @param encoding 编码方式（Hex/ASCII/DEC等）
     */
    public void set429Encoding(int encoding) {
        arinc429SettingStruct.setEncoding(encoding);
        this.encoding = arinc429SettingStruct.getEncoding();
    }
    //endregion

    //region ==================== MIL-STD-1553B配置方法 ====================
    
    /**
     * 设置MIL-STD-1553B编码方式
     * @param encoding 编码方式（Hex/ASCII/DEC等）
     */
    public void set1553bEncoding(int encoding) {
        milSID1553bSettingStruct.setEncoding(encoding);
        this.encoding = milSID1553bSettingStruct.getEncoding();
    }
    //endregion

    /** 校验位设置：false=奇偶校验，true=无校验 */
    private boolean checked = false;
    
    /** 数据位设置（5-9位） */
    private int bits = 8;
    
    /** 显示文本编码方式（Hex/ASCII/DEC等） */
    private int encoding = ICharacterEncoding.Hex;

    //endregion

    //region ==================== 位图资源管理 ====================
    
    /**
     * 通道图标位图资源数组
     * <p>第一维：通道索引（S1/S2/S3/S4，对应TChan.S1-TChan.S1到TChan.S4-TChan.S1）</p>
     * <p>第二维：图标状态（0=普通, 1=普通按下, 2=普通抬起, 3=选中, 4=选中按下, 5=选中抬起）</p>
     */
    private Bitmap[][] resBmp = new Bitmap[TChan.MaxSerial][6];
    
    /**
     * 构造函数
     * <p>初始化位图资源、画布、画笔、光标矩形等</p>
     */
    public SerialBus() {
        // 加载S1通道图标（6种状态：普通/按下/抬起/选中/选中按下/选中抬起）
        resBmp[TChan.S1-TChan.S1][0] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s1_n);
        resBmp[TChan.S1-TChan.S1][1] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s1_n_down);
        resBmp[TChan.S1-TChan.S1][2] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s1_n_up);
        resBmp[TChan.S1-TChan.S1][3] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s1_s);
        resBmp[TChan.S1-TChan.S1][4] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s1_s_down);
        resBmp[TChan.S1-TChan.S1][5] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s1_s_up);

        // 加载S2通道图标（6种状态）
        resBmp[TChan.S2-TChan.S1][0] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s2_n);
        resBmp[TChan.S2-TChan.S1][1] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s2_n_down);
        resBmp[TChan.S2-TChan.S1][2] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s2_n_up);
        resBmp[TChan.S2-TChan.S1][3] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s2_s);
        resBmp[TChan.S2-TChan.S1][4] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s2_s_down);
        resBmp[TChan.S2-TChan.S1][5] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s2_s_up);

        // 加载S3通道图标（6种状态）
        resBmp[TChan.S3-TChan.S1][0] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s3_n);
        resBmp[TChan.S3-TChan.S1][1] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s3_n_down);
        resBmp[TChan.S3-TChan.S1][2] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s3_n_up);
        resBmp[TChan.S3-TChan.S1][3] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s3_s);
        resBmp[TChan.S3-TChan.S1][4] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s3_s_down);
        resBmp[TChan.S3-TChan.S1][5] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s3_s_up);

        // 加载S4通道图标（6种状态）
        resBmp[TChan.S4-TChan.S1][0] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s4_n);
        resBmp[TChan.S4-TChan.S1][1] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s4_n_down);
        resBmp[TChan.S4-TChan.S1][2] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s4_n_up);
        resBmp[TChan.S4-TChan.S1][3] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s4_s);
        resBmp[TChan.S4-TChan.S1][4] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s4_s_down);
        resBmp[TChan.S4-TChan.S1][5] = Tools.readSvgBmp(com.micsig.tbook.ui.R.drawable.s4_s_up);

        // 初始化数据位图（宽度=波形区域宽度，高度=25像素）
        bmpData = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_YT), 25, Bitmap.Config.ARGB_8888);
        
        // 创建画布并绑定到位图
        canvasData = new Canvas(bmpData);
        
        // 初始化画笔
        mPaint = new Paint();
        mPaint.setTextSize(20);              // 设置文本大小
        mPaint.setAntiAlias(true);           // 启用抗锯齿
        mPaint.setDither(true);              // 启用抖动
        mPaint.setTextAlign(Paint.Align.CENTER);  // 文本居中对齐

        // 初始化光标矩形（用于边界检测）
        CursorRect = new Rect();
        CursorRect.left = 0;
        CursorRect.top = 0;
        CursorRect.right = GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode());
        CursorRect.bottom = GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode());
    }

    /**
     * 触发绘制
     * <p>强制绘制最后一帧数据并刷新纹理</p>
     */
    private void draw() {
        forceDrawLastData(this.nameID);  // 强制绘制最后一帧数据
        isChanageBitmap = true;           // 标记位图已变更
        onRefresh();                      // 刷新纹理
    }


    /**
     * 从配置结构体同步参数到本地变量
     * <p>根据串行总线类型，从对应的配置结构体中读取参数</p>
     * @param serialBusType 串行总线类型
     */
    private void structToParam(int serialBusType) {
        switch (serialBusType) {
            case SerialBusStruct.SerialBusType_UART: {
                // UART：仅在运行状态下更新参数
                if (Scope.getInstance().isRun()) {
                    this.encoding = uartSettingStruct.getEncoding();  // 获取编码方式
                    this.bits = uartSettingStruct.getUartLength();    // 获取数据位
                    this.checked = uartSettingStruct.isChecked();    // 获取校验位设置
                }
            }
            break;
            case SerialBusStruct.SerialBusType_LIN: {
                // LIN：更新编码方式
                this.encoding = linSettingStruct.getEncoding();
            }
            break;
            case SerialBusStruct.SerialBusType_CAN: {
                // CAN：更新编码方式
                this.encoding = canSettingStruct.getEncoding();
            }
            break;
            case SerialBusStruct.SerialBusType_SPI: {
                // SPI：仅在运行状态下更新参数
                if (Scope.getInstance().isRun()) {
                    this.encoding = spiSettingStruct.getEncoding();  // 获取编码方式
                    this.bits = spiSettingStruct.getDataBit();       // 获取数据位
                }
            }
            break;
            case SerialBusStruct.SerialBusType_I2C: {
                // I2C：更新编码方式
                this.encoding = i2cSettingStruct.getEncoding();
            }
            break;
            case SerialBusStruct.SerialBusType_429: {
                // ARINC429：更新编码方式
                this.encoding = arinc429SettingStruct.getEncoding();
            }
            break;
            case SerialBusStruct.SerialBusType_1553B: {
                // MIL-STD-1553B：更新编码方式
                this.encoding = milSID1553bSettingStruct.getEncoding();
            }
            break;
        }
    }
    
    //endregion

    //region ==================== ISerialBus接口实现 ====================
    
    /** 清除模式（PorterDuff.Mode.CLEAR） */
    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    
    /** 源模式（PorterDuff.Mode.SRC） */
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

    /**
     * 标题变更回调
     * <p>当串行总线类型变更时触发，重新解析数据</p>
     * 
     * @param chNo 通道号
     * @param serialBusType 串行总线类型
     */
    @Override
    public void OnTitleChange(int chNo, int serialBusType) {
        // 模拟数据：如果存在未处理的字节数据，将其标记为未处理
        if (bytes != null && bytes.limit() != 0 && bytes.capacity() != 0) {
            SerialImageBuffer sib = SerialImageDoubleCache.getInstance().getLastCache(chNo);  // 获取最后一帧缓存
            if (sib!=null) {
                sib.setDeal(false);  // 标记为未处理
                SerialImageDoubleCache.getInstance().put(chNo, sib);  // 放回缓存
            }else {
                bytes.limit(0);  // 清空缓冲区
            }
        }
        this.serialBusType = serialBusType;  // 更新串行总线类型
        toParseRunnable.chNo=chNo;           // 设置通道号
        fixedThreadPool.execute(toParseRunnable);  // 提交解码任务
    }

    /**
     * 数据变更回调
     * <p>当接收到新的字节数据时触发，异步解析数据</p>
     * 
     * @param chNo 通道号
     * @param bytes 原始字节数据
     * @param timeToPix 时间到像素的转换系数
     * @param startX 绘制起始X坐标
     * @param endX 绘制结束X坐标
     */
    @Override
    public void OnDataChange(int chNo, ByteBuffer bytes, long timeToPix, int startX, int endX) {
        this.bytes = bytes;                  // 保存字节数据
        toParseRunnable.chNo=chNo;           // 设置通道号
        fixedThreadPool.execute(toParseRunnable);  // 提交解码任务到线程池
    }

    /**
     * 强制绘制最后一帧数据
     * <p>从双缓冲中获取最后一帧数据并重新绘制</p>
     * 
     * @param iwaveCh 波形通道号
     */
    public void forceDrawLastData(int iwaveCh){
        // 模拟数据：获取最后一帧缓存并标记为未处理
        SerialImageBuffer sib = SerialImageDoubleCache.getInstance().getLastCache(iwaveCh);
        if (sib!=null) {
            sib.setDeal(false);  // 标记为未处理
            SerialImageDoubleCache.getInstance().put(iwaveCh, sib);  // 放回缓存
        }
        toParseRunnable.chNo=iwaveCh;  // 设置通道号
        fixedThreadPool.execute(toParseRunnable);  // 提交解码任务
    }


    /**
     * 文本数据变更回调（暂未实现）
     * 
     * @param chNo 通道号
     * @param bytes 文本字节数据
     */
    @Override
    public void OnTxtDataChange(int chNo, ByteBuffer bytes) {
        // 暂未实现
    }

    /**
     * 绘制串行总线波形
     * <p>核心绘制方法，解析数据并绘制到位图</p>
     * 
     * <h3>处理流程：</h3>
     * <ol>
     *   <li>从配置结构体同步参数</li>
     *   <li>检查可见性</li>
     *   <li>生成标题名称</li>
     *   <li>检查权限</li>
     *   <li>清空画布或解析数据</li>
     *   <li>绘制标题和通道图标</li>
     *   <li>刷新纹理</li>
     * </ol>
     * 
     * @param chNo 通道号
     */
    private void drawSerialBus(int chNo) {

        structToParam(serialBusType);  // 从配置结构体同步参数
        
        if (this.visible) {  // 仅在可见时绘制
            String titleName = "";  // 标题名称
            
            // 根据串行总线类型生成标题
            switch (serialBusType) {
                case SerialBusStruct.SerialBusType_UART:
                    titleName = TChan.getChannelName(chNo) + " UART";  // UART标题
                    break;
                case SerialBusStruct.SerialBusType_LIN:
                    titleName = TChan.getChannelName(chNo) + " LIN";   // LIN标题
                    break;
                case SerialBusStruct.SerialBusType_CAN:
                    titleName = TChan.getChannelName(chNo) + " CAN";   // CAN标题
                    break;
                case SerialBusStruct.SerialBusType_SPI:
                    titleName = TChan.getChannelName(chNo) + " SPI";   // SPI标题
                    break;
                case SerialBusStruct.SerialBusType_I2C:
                    titleName = TChan.getChannelName(chNo) + " I2C";   // I2C标题
                    break;
                case SerialBusStruct.SerialBusType_429:
                    titleName = TChan.getChannelName(chNo) + " 429";   // ARINC429标题
                    break;
                case SerialBusStruct.SerialBusType_1553B:
                    titleName = TChan.getChannelName(chNo) + " 1553B"; // MIL-STD-1553B标题
                    break;
            }

            synchronized (lock) {  // 同步锁保护绘制操作
                int titleLen = getTextWidth(titleName) + 16;  // 计算标题宽度（文本宽度+16像素边距）
                boolean noPermission = (!ScopeConfig.getConfig().isBusEnable(serialBusType) && !App.IsDebug());  // 检查权限
                
                if (bytes == null || bytes.limit() == 0 || bytes.capacity() == 0 || noPermission) {
                    // 无数据或无权限：清空画布并绘制占位线
                    
                    mPaint.setXfermode(clearMode);         // 设置清除模式
                    canvasData.drawPaint(mPaint);          // 清空画布
                    mPaint.setXfermode(srcMode);           // 恢复源模式
                    
                    if (startX < titleLen) startX = titleLen;  // 确保起始位置不小于标题长度
                    if (endX < startX) endX = startX;          // 确保结束位置不小于起始位置
                    if (startX == endX) endX = GlobalVar.get().getScreen().width();  // 如果起始等于结束，使用屏幕宽度
                    
                    if (!noPermission)  // 有权限时绘制占位线
                        canvasData.drawLine(startX, bmpData.getHeight() / 2, endX, bmpData.getHeight() / 2, mPaint);
                } else {
                    // 有数据：解析并绘制
                    
                    int ch = this.getLineNameID();  // 获取通道名称ID
                    
                    // 遍历双缓冲中的所有缓存帧
                    for (SerialImageBuffer sib : SerialImageDoubleCache.getInstance().getCache(chNo).values()) {
                        if (sib.isDeal() || sib.getBytes() == null) continue;  // 跳过已处理或空数据
                        
                        sib.setDoing(true);  // 标记正在处理
                        
                        mPaint.setXfermode(clearMode);     // 设置清除模式
                        canvasData.drawPaint(mPaint);     // 清空画布
                        mPaint.setXfermode(srcMode);      // 恢复源模式
                        
                        ByteBuffer bytes = sib.getBytes();      // 获取字节数据
                        long timeToPix = sib.getTimeToPix();     // 获取时间到像素转换系数
                        int startX = sib.getStartX();            // 获取起始X坐标
                        int endX = sib.getEndX();               // 获取结束X坐标
                        
                        // 调用解析器解析数据并绘制
                        serialBusStructParse.toParse(ch, titleLen, serialBusType, canvasData, mPaint, bytes, timeToPix, encoding, bits, checked, startX, endX);
                        
                        sib.setDeal(true);   // 标记已处理
                        sib.setDoing(false); // 标记处理完成
                        SerialImageDoubleCache.getInstance().put(chNo, sib.getKey(), sib);  // 放回缓存
                    }
                }
                
                // 绘制标题文本（描边效果）
                mPaint.setXfermode(srcMode);           // 设置源模式
                mPaint.setStrokeWidth(4.0f);           // 设置描边宽度
                mPaint.setColor(Color.BLACK);          // 设置描边颜色（黑色）
                mPaint.setStyle(Paint.Style.STROKE);   // 设置描边样式
                float x = (float) titleLen / 2;        // 计算标题X坐标（居中）
                float y = (float) ((bmpData.getHeight() - Tools.getTextRect(titleName, mPaint).height()) / 2 + Tools.getTextRect(titleName, mPaint).height());  // 计算标题Y坐标（垂直居中）
                canvasData.drawText(titleName, x, y, mPaint);  // 绘制标题描边
                
                // 绘制标题文本（填充效果）
                mPaint.setStrokeWidth(0);              // 清除描边宽度
                mPaint.setColor(this.color);           // 设置填充颜色（波形颜色）
                mPaint.setStyle(Paint.Style.FILL);     // 设置填充样式
                canvasData.drawText(titleName, x, y, mPaint);  // 绘制标题填充
                
                // 绘制通道图标
                canvasData.drawBitmap(resBmp[chNo - TChan.S1][selected ? 3 : 0], 0, (bmpData.getHeight() - resBmp[chNo - TChan.S1][3].getHeight()) / 2, mPaint);
                
                isChanageBitmap = true;  // 标记位图已变更
                onRefresh();             // 刷新纹理
            }
        }
    }

    /**
     * 将字节数据放入队列
     * <p>将字节数据添加到文本缓冲区，并触发文本解析</p>
     * 
     * @param bytes 字节数据
     * @throws InterruptedException 线程中断异常
     */
    public void putBytesToQueue(ByteBuffer bytes) throws InterruptedException {
        serialTxtBuffer.putBytesToQueue(bytes);  // 将字节放入队列
        boolean b = SerialBusTxtStructParse.getInstance().getParsing(this.getLineNameID());  // 检查是否正在解析

        // 如果未在解析，则触发解析
        {
            String chName=TChan.getChannelName(this.getLineNameID());  // 获取通道名称
            SerialBusTxtStructParse.getInstance().toParseByRunable(chName, serialTxtBuffer, this.serialBusType, encoding, bits, checked);  // 触发文本解析
        }
    }
    //endregion

    /**
     * 获取串行总线类型
     * @return 串行总线类型
     */
    public @SerialBusStruct.SerialBusType int getSerialBusType(){
        return serialBusType;
    }

    /**
     * 获取文本宽度
     * @param text 文本内容
     * @return 文本宽度（像素）
     */
    private int getTextWidth(String text) {
        mPaint.getTextBounds(text, 0, text.length(), rectTextWidth);  // 测量文本边界
        int w = rectTextWidth.width();   // 获取宽度
        int h = rectTextWidth.height();  // 获取高度
        return w;
    }

    /**
     * 获取位图高度
     * @return 位图高度（像素）
     */
    public int getHeight() {
        return bmpData.getHeight();
    }

    //region ==================== IWave接口实现 ====================

    /**
     * 移动波形线（暂未实现）
     * 
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     */
    @Override
    public void moveLine(int offsetX, int offsetY) {
        // 暂未实现
    }

    /**
     * 绘制波形（Canvas方式，暂未实现）
     * 
     * @param canvas 画布
     */
    @Override
    public void draw(Canvas canvas) {
        // 暂未实现
    }

    /**
     * 绘制波形（OpenGL方式）
     * <p>将位图绘制到OpenGL画布上</p>
     * 
     * @param canvas OpenGL画布
     */
    @Override
    public void draw(ICanvasGL canvas) {
        if (visible) {  // 仅在可见时绘制
            synchronized (lock) {  // 同步锁保护
                canvasGL = canvas;  // 保存画布引用
                if(isChanageBitmap){  // 位图已变更时刷新纹理
                    canvas.invalidateTextureContent(bmpData,null);  // 刷新纹理内容
                }
                canvas.drawBitmap(bmpData, 0, (int) Math.round(this.getPosY() * ScopeBase.getToUICoff()));  // 绘制位图
                isChanageBitmap = false;  // 清除变更标志
            }

        }
    }

    /**
     * 结果初始化矩形（暂未实现）
     */
    @Override
    public void resultIniRect() {
        // 暂未实现
    }

    /**
     * 设置通道名称ID
     * 
     * @param nameId 通道名称ID（TChan.S1/S2/S3/S4）
     */
    @Override
    public void setLineNameId(int nameId) {
        this.nameID = nameId;
    }

    /**
     * 获取通道名称ID
     * @return 通道名称ID
     */
    @Override
    public int getLineNameID() {
        return this.nameID;
    }

    /**
     * 获取X轴位置
     * @return X轴位置（像素）
     */
    @Override
    public long getX() {
        return this.x;
    }

    /**
     * 获取Y轴位置
     * @return Y轴位置（像素）
     */
    @Override
    public double getY() {
        return this.y;
    }

    /**
     * 设置X轴位置
     * <p>设置X坐标并触发重绘</p>
     * 
     * @param x X轴位置（像素）
     */
    @Override
    public void setX(long x) {
        this.x = x;    // 设置X坐标
        draw();        // 触发重绘
    }

    /**
     * 设置Y轴位置
     * <p>设置Y坐标，进行边界检查，更新缓存，触发重绘和移动事件</p>
     * 
     * <h3>处理流程：</h3>
     * <ol>
     *   <li>边界检查：确保Y坐标在有效范围内</li>
     *   <li>坐标转换：将像素坐标转换为FPGA坐标</li>
     *   <li>缓存更新：保存位置到缓存</li>
     *   <li>触发重绘</li>
     *   <li>触发移动事件</li>
     * </ol>
     * 
     * @param y Y轴位置（像素）
     */
    @Override
    public void setY(double y) {
        // 边界检查：确保Y坐标在有效范围内
        if (y <= CursorRect.left) {
            this.y = 0;  // 小于等于左边界，设置为0
        } else if (y >= CursorRect.height() - bmpData.getHeight()) {
            this.y = CursorRect.height() - bmpData.getHeight();  // 大于等于下边界，设置为最大值
        } else {
            this.y = y;  // 在有效范围内，直接设置
        }
        
        // 坐标转换：将像素坐标转换为FPGA坐标
        if(ScopeBase.getToFPGACoff() == 1.0) {
            ScopeBase.setConvertScale(CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_ZONE_HEIGHT));  // 设置转换比例
        }
        this.posY = this.y * ScopeBase.getToFPGACoff();  // 计算FPGA坐标
        
        // 判断是否为缩放模式
        boolean isZoom = WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM;
        
        // 保存位置到缓存（缩放模式需要乘以系数）
        CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_SERIAL_Y_POSITION + getLineNameID(), String.valueOf(isZoom ? this.posY * GlobalVar.get().toYTCoef() : this.posY));
        
        draw();  // 触发重绘
        
        // 触发移动事件
        if (this.onMovingWaveEvent != null) {
            onMovingWaveEvent.OnMovingWave(this, x, this.y, false, false);
        }
    }

    /**
     * 获取Y轴位置（FPGA坐标）
     * @return Y轴位置（FPGA坐标）
     */
    public double getPosY() {
        return posY;
    }

    /**
     * 设置波形颜色
     * 
     * @param color 颜色值
     */
    @Override
    public void setColor(int color) {
        this.color = color;       // 设置颜色
        mPaint.setColor(this.color);  // 更新画笔颜色
        draw();                    // 触发重绘
    }

    /**
     * 获取波形颜色
     * @return 颜色值
     */
    @Override
    public int getColor() {
        return this.color;
    }

    /**
     * 设置可见性
     * 
     * @param visible 是否可见
     */
    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;  // 设置可见性
        draw();                  // 触发重绘
    }

    /**
     * 获取可见性
     * @return 是否可见
     */
    @Override
    public boolean getVisible() {
        return this.visible;
    }

    /**
     * 设置选中状态
     * 
     * @param selected 是否选中
     */
    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;  // 设置选中状态
        draw();                     // 触发重绘
    }

    /**
     * 获取选中状态
     * @return 是否选中
     */
    @Override
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * 移动像素
     * <p>根据像素偏移量移动波形位置</p>
     * 
     * @param px 像素偏移量
     */
    @Override
    public void movePix(double px) {
        double y = getPosY() * ScopeBase.getToUICoff();  // 获取当前Y坐标（UI坐标）
        px = px * ScopeBase.getToUICoff();               // 转换偏移量为UI坐标
        setY(y + px);                                    // 设置新的Y坐标
    }

    /**
     * 设置选中状态变更事件监听器（暂未实现）
     * 
     * @param onSelectChangeEvent 选中状态变更事件
     */
    @Override
    public void setOnSelectChangeEvent(OnSelectChangeEvent onSelectChangeEvent) {
        // 暂未实现
    }

    /**
     * 设置波形移动事件监听器
     * 
     * @param onMovingWaveEvent 波形移动事件
     */
    @Override
    public void setOnMovingWaveEvent(OnMovingWaveEvent onMovingWaveEvent) {
        this.onMovingWaveEvent = onMovingWaveEvent;
    }

    //endregion


    //region ==================== IWorkMode接口实现 ====================

    /**
     * 切换工作模式
     * <p>在YT和YTZOOM模式之间切换，重新计算Y坐标</p>
     * 
     * <h3>处理流程：</h3>
     * <ol>
     *   <li>从缓存读取Y坐标</li>
     *   <li>根据工作模式计算新的Y坐标</li>
     *   <li>更新FPGA坐标</li>
     *   <li>更新光标矩形尺寸</li>
     * </ol>
     * 
     * @param workMode 工作模式（YT/YTZOOM）
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        // 从缓存读取Y坐标并转换为UI坐标
        double y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_SERIAL_Y_POSITION + getLineNameID());
        y = y * ScopeBase.getToUICoff();
        
        switch (workMode) {
            case IWorkMode.WorkMode_YT: {
                // YT模式：直接使用缓存的Y坐标
                this.y = y;
            }
            break;
            case IWorkMode.WorkMode_YTZOOM: {
                // YTZOOM模式：计算缩放后的Y坐标（12为图标高度）
                this.y = (y + 12) * GlobalVar.get().toZoomCoef() - 12;
            }
            break;
        }
        
        // 更新FPGA坐标
        this.posY = this.y * ScopeBase.getToFPGACoff();
        
        // 更新光标矩形尺寸
        CursorRect.right = GlobalVar.get().getWaveZoneWidth_Pix(workMode);
        CursorRect.bottom = GlobalVar.get().getWaveZoneHeight_Pix(workMode);
    }

    //endregion

    /**
     * 串行总线解码任务Runnable
     * <p>在线程池中执行解码任务</p>
     */
    class SerialBusToParseRunnable implements Runnable{
        /** 通道号 */
        private int chNo;
        
        @Override
        public void run() {
            drawSerialBus(chNo);  // 执行绘制
        }
    }


    /**
     * 获得图像解码数据（带深度复制）
     * <p>根据类型参数返回对应的解码数据列表</p>
     * 
     * @param t 数据类型（SerialBusStruct.UartStruct等）
     * @return 解码数据列表
     * @param <T> 数据类型泛型
     */
    public <T> List<T> getImageBufferList(Class<T> t){
       try {
           if (t.isAssignableFrom(SerialBusStruct.UartStruct.class)) {
               // UART数据
               return (List<T>) Tools.jsonToArrayList(serialBusStructParse.getListUart(),SerialBusStruct.UartStruct.class);
           } else if (t.isAssignableFrom(SerialBusStruct.LinStruct.class)) {
               // LIN数据
               List<SerialBusStruct.LinStruct> l=serialBusStructParse.getListLin();
               return (List<T>)Tools.jsonToArrayList(l,SerialBusStruct.LinStruct.class);
           } else if (t.isAssignableFrom(SerialBusStruct.CanStruct.class)) {
               // CAN数据
                return (List<T>) Tools.jsonToArrayList(serialBusStructParse.getListCan(),SerialBusStruct.CanStruct.class);

           } else if (t.isAssignableFrom(SerialBusStruct.SpiStruct.class)) {
               // SPI数据
               return (List<T>) Tools.jsonToArrayList(serialBusStructParse.getListSpi(),SerialBusStruct.SpiStruct.class);
           } else if (t.isAssignableFrom(SerialBusStruct.I2cStruct.class)) {
               // I2C数据
               return (List<T>) Tools.jsonToArrayList(serialBusStructParse.getListI2c(), SerialBusStruct.I2cStruct.class);
           } else if (t.isAssignableFrom(SerialBusStruct.Arinc429Struct.class)) {
               // ARINC429数据
               return (List<T>) Tools.jsonToArrayList(serialBusStructParse.getList429(),SerialBusStruct.Arinc429Struct.class);
           } else {
               // MIL-STD-1553B数据
               return (List<T>) Tools.jsonToArrayList(serialBusStructParse.getList1553b(),SerialBusStruct.MilSTD1553bStruct.class);
           }
       }catch (Exception e){
           e.printStackTrace();
           return null;
       }

    }


    /**
     * 变更矩形高度
     * <p>重新计算UI位置，更新点击选中位置</p>
     */
    public void changeRectHeight() {
        switchWorkMode(WorkModeManage.getInstance().getmWorkMode());  // 重新计算UI位置，更新点击选中位置
   }

}