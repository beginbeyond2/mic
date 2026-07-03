package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;
import com.micsig.tbook.ui.util.TBookUtil;

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                     SerialsWordSpiSingleRowTextView                         │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                │
 * │ SPI串口总线单行文本显示视图，负责单个SPI数据帧的可视化呈现                    │
 * │                                                                             │
 * │ 【核心职责】                                                                │
 * │ 1. 显示单个SPI数据帧的通道号、时间戳、数据内容和触发标志                      │
 * │ 2. 处理数据内容的多行显示逻辑                                                │
 * │ 3. 绘制分隔虚线以区分不同数据帧                                              │
 * │                                                                             │
 * │ 【架构设计】                                                                │
 * │ 继承自Android View基类，采用自定义绘制方式实现表格化数据显示                  │
 * │ ┌──────────────────────────────────────────────────────────────┐             │
 * │ │  ┌─────┬──────────┬───────────────────┬──────────┐          │             │
 * │ │  │ CH  │   Time   │       Data        │ Trigger  │          │             │
 * │ │  ├─────┼──────────┼───────────────────┼──────────┤          │             │
 * │ │  │ S1  │ 10.5ms   │ 0x01 0x02 0x03... │   Yes    │          │             │
 * │ │  └─────┴──────────┴───────────────────┴──────────┘          │             │
 * │ └──────────────────────────────────────────────────────────────┘             │
 * │                                                                             │
 * │ 【数据流向】                                                                │
 * │ SerialBusTxtStruct.SpiStruct -> setData() -> invalidate() -> onDraw()       │
 * │                                                                             │
 * │ 【依赖关系】                                                                │
 * │ - SerialBusTxtStruct.SpiStruct: SPI数据结构体                               │
 * │ - GlobalVar: 全局变量管理器，获取波形区域尺寸                                 │
 * │ - Tools: 工具类，提供文本测量方法                                            │
 * │ - TBookUtil: 时间格式化工具                                                  │
 * │                                                                             │
 * │ 【使用场景】                                                                │
 * │ 在串口解码功能中，用于显示单个SPI数据帧的详细信息，支持数据自动换行和虚线分隔  │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class SerialsWordSpiSingleRowTextView extends View {
    private Context context;                                          // 上下文对象
    private Paint paint;                                              // 绘画画笔对象
    private DashPathEffect dashPathEffect;                            // 虚线效果对象
    private int width;                                                // 视图宽度
    private int height;                                               // 视图高度
    private SerialBusTxtStruct.SpiStruct bean;                        // SPI数据结构体
    private boolean showMs = false;                                   // 是否显示毫秒标志
    private int chWidth;                                              // 通道列宽度
    private int timeWidth;                                            // 时间列宽度
    private int dataWidth;                                            // 数据列宽度
    private int triggerWidth;                                         // 触发列宽度

    /**
     * 单参数构造函数
     * @param context 上下文对象
     */
    public SerialsWordSpiSingleRowTextView(Context context) {
        this(context, null);                                           // 调用双参数构造函数
    }

    /**
     * 双参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     */
    public SerialsWordSpiSingleRowTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);                                       // 调用三参数构造函数
    }

    /**
     * 三参数构造函数（完整构造函数）
     * 初始化画笔、尺寸等属性
     * @param context 上下文对象
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public SerialsWordSpiSingleRowTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);                           // 调用父类构造函数
        this.context = context;                                       // 保存上下文引用
        paint = new Paint();                                          // 创建画笔对象
        dashPathEffect = new DashPathEffect(new float[]{1, 1, 1, 1}, 0); // 创建虚线效果
        paint.setTextSize(context.getResources().getDimension(R.dimen.textSizeCur)); // 设置文本大小
        paint.setAntiAlias(true);                                     // 启用抗锯齿
        paint.setTypeface(Typeface.SANS_SERIF);                       // 设置字体类型
        width = GlobalVar.get().getMainWave().x;                      // 获取波形区域宽度
        height = (int) context.getResources().getDimension(R.dimen.formHeightDetail); // 获取行高

        chWidth     = (int) getResources().getDimension(R.dimen.spi_ch_width);         // 获取通道列宽度
        timeWidth       = (int) getResources().getDimension(R.dimen.spi_time_width);     // 获取时间列宽度
        dataWidth       = (int) getResources().getDimension(R.dimen.spi_data_width);     // 获取数据列宽度
        triggerWidth= (int) getResources().getDimension(R.dimen.spi_trigger_width); // 获取触发列宽度
    }

    /**
     * 设置SPI数据并刷新视图
     * @param bean SPI数据结构体
     * @param showMs 是否显示毫秒
     */
    public void setData(SerialBusTxtStruct.SpiStruct bean, boolean showMs) {
        this.bean = bean;                                             // 保存SPI数据
        this.showMs = showMs;                                         // 保存显示毫秒标志
        invalidate();                                                 // 触发视图重绘
    }

    /**
     * 视图绘制方法
     * 负责绘制SPI数据帧的通道、时间、数据和触发信息
     * @param canvas 画布对象
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);                                          // 调用父类绘制方法
        if (bean == null) return;                                     // 数据为空则直接返回

        String ch = bean.Ch;                                          // 获取通道号
        String time = bean.Ch.equals(SerialBusTxtStruct.SpiStruct.FLAGShow_GroupData)
                ? SerialBusTxtStruct.SpiStruct.FLAGShow_GroupData
                : TBookUtil.getStringFrom10us(bean.CurTime);         // 获取格式化时间字符串
        String data = bean.Data.trim();                               // 获取数据并去除首尾空格
        String trigger = bean.Trigger ? "Yes" : "";                   // 获取触发标志
        Rect chRect = Tools.getTextRect("S1", paint);                 // 测量通道文本边界
        Rect timeRect = Tools.getTextRect(Tools.getTextFormat(time), paint); // 测量时间文本边界
        Rect dataRect = Tools.getTextRect(data, paint);              // 测量数据文本边界
        Rect triggerRect = Tools.getTextRect(trigger, paint);        // 测量触发文本边界
        paint.setColor(getResources().getColor(R.color.textColor)); // 设置文本颜色
        int y = height / 2 + chRect.height() / 2;                    // 计算文本绘制Y坐标
        canvas.drawText(ch, chWidth / 2 - chRect.width() / 2, y, paint); // 绘制通道文本（居中）
        canvas.drawText(time, chWidth + timeWidth / 2 - timeRect.width() / 2, y, paint); // 绘制时间文本（居中）

        int curDrawLine = (data.length() - 1) / ISerialsWord.MAXCHAR_EACHROW_DATA_SPI + 1; // 计算需要绘制的行数
        for (int j = 0; j < curDrawLine; j++) {                      // 遍历每一行
            if (j != curDrawLine - 1) {                              // 非最后一行
                canvas.drawText(data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_SPI
                        , (j + 1) * ISerialsWord.MAXCHAR_EACHROW_DATA_SPI)
                        , chWidth + timeWidth + 15, y + j * height, paint); // 绘制完整行数据
            } else {                                                  // 最后一行
                canvas.drawText(data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_SPI)
                        , chWidth + timeWidth + 15, y + j * height, paint); // 绘制剩余数据
                drawLine(canvas, y + j * height + 6);//最后一行数据下面画点线
                y += j * height;                                     // 更新Y坐标
            }
        }

        canvas.drawText(trigger, chWidth + timeWidth + dataWidth + triggerWidth / 2 - triggerRect.width() / 2, y, paint); // 绘制触发标志（居中）
        paint.setColor(Color.RED);                                    // 恢复画笔颜色
    }


    /**
     * 绘制虚线分隔线
     * @param canvas 画布对象
     * @param y 虚线的Y坐标
     */
    private void drawLine(Canvas canvas, int y) {
        paint.setPathEffect(dashPathEffect);                          // 设置虚线效果
        canvas.drawLine(0, y, width, y, paint);                       // 绘制虚线
        paint.setPathEffect(null);                                    // 清除虚线效果
    }
}