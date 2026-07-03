package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.content.Context;  // 导入上下文类
import android.graphics.Canvas;  // 导入画布类
import android.graphics.Color;  // 导入颜色类
import android.graphics.DashPathEffect;  // 导入虚线效果类
import android.graphics.Paint;  // 导入画笔类
import android.graphics.Rect;  // 导入矩形类
import android.graphics.Typeface;  // 导入字体类
import android.util.AttributeSet;  // 导入属性集类
import android.view.View;  // 导入视图基类

import androidx.annotation.Nullable;  // 导入可空注解

import com.micsig.tbook.scope.Bus.IBus;  // 导入总线接口
import com.micsig.tbook.scope.Bus.LinBus;  // 导入LIN总线类
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入通道工厂类
import com.micsig.tbook.scope.channel.SerialChannel;  // 导入串行通道类
import com.micsig.tbook.tbookscope.GlobalVar;  // 导入全局变量类
import com.micsig.tbook.tbookscope.R;  // 导入资源类
import com.micsig.tbook.tbookscope.tools.Tools;  // 导入工具类
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;  // 导入串行总线管理类
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;  // 导入串行总线文本结构类
import com.micsig.tbook.ui.util.TBookUtil;  // 导入TBook工具类
import com.micsig.tbook.ui.wavezone.TChan;  // 导入通道定义类

import java.util.ArrayList;  // 导入动态数组类

/*
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                     SerialsWordLinSingleScreenTextView                       │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                  │
 * │   串行总线协议数据显示模块 - LIN总线单屏多行数据文本视图组件                     │
 * │                                                                              │
 * │ 【核心职责】                                                                  │
 * │   1. 显示LIN总线多帧数据的列表视图                                             │
 * │   2. 以表格形式批量展示多帧数据的通道、时间、ID、数据、错误、校验、触发等信息   │
 * │   3. 支持指定显示行数限制                                                     │
 * │   4. 支持虚线分隔符绘制                                                       │
 * │   5. 根据LIN协议版本处理数据末尾校验位                                         │
 * │                                                                              │
 * │ 【架构设计】                                                                  │
 * │   继承自Android View基类，通过Canvas进行自定义绘制，采用列宽度固定布局方式      │
 * │   - SingleScreen模式：用于列表展示多帧数据，每帧单行显示，支持滚动显示         │
 * │   - SingleRow模式：用于详细展示单帧数据，数据可能多行显示                       │
 * │   - 特殊处理：非LIN1.3版本时需去除FPGA多传的校验位数据                         │
 * │                                                                              │
 * │ 【数据流向】                                                                  │
 * │   ArrayList<LinStruct> → setData() → invalidate() → onDraw()                 │
 * │   FPGA采集 → 解析为LinStruct列表 → 查询LIN版本 → 数据修正 → 视图批量渲染       │
 * │                                                                              │
 * │ 【依赖关系】                                                                  │
 * │   - SerialBusTxtStruct.LinStruct：LIN总线数据结构                            │
 * │   - LinBus：LIN总线配置对象，用于获取LIN版本                                  │
 * │   - ChannelFactory：通道工厂，用于获取串行通道                                │
 * │   - GlobalVar：获取主波形窗口尺寸                                             │
 * │   - Tools：文本矩形计算和字节转换工具                                         │
 * │   - TBookUtil：时间格式化工具                                                 │
 * │   - TChan：通道定义和转换工具                                                 │
 * │   - R.dimen：列宽度资源定义                                                   │
 * │                                                                              │
 * │ 【使用场景】                                                                  │
 * │   用于LIN总线解码列表界面，在列表区域批量展示多帧LIN数据的概览信息             │
 * │   注意：LIN1.3和其他版本的数据处理逻辑不同                                     │
 * └──────────────────────────────────────────────────────────────────────────────┘
 */
public class SerialsWordLinSingleScreenTextView extends View {
    private Context context;  // 上下文对象引用
    private Paint paint;  // 画笔对象，用于绘制文本和线条
    private DashPathEffect dashPathEffect;  // 虚线效果对象，用于绘制分隔虚线
    private int width;  // 视图总宽度
    private int rowHeight;  // 单行高度
    private ArrayList<SerialBusTxtStruct.LinStruct> list = new ArrayList<>();  // LIN总线数据列表
    private int showLine;  // 显示行数限制
    private boolean showMs = false;  // 是否显示毫秒时间（预留功能）
    private int offset = 0;  // 垂直偏移量，用于调整文本位置

    private int chWidth;  // 通道列宽度
    private int timeWidth;  // 时间列宽度
    private int idWidth;  // ID列宽度
    private int dataWidth;  // 数据列宽度
    private int crcWidth;  // CRC校验列宽度
    private int errorWidth;  // 错误列宽度
    private int TriggerWidth;  // 触发列宽度（注意：命名不规范，应为triggerWidth）

    /**
     * 单参数构造函数
     * 
     * @param context 上下文对象，用于获取资源和创建视图
     */
    public SerialsWordLinSingleScreenTextView(Context context) {
        this(context, null);  // 调用双参数构造函数，属性集为null
    }

    /**
     * 双参数构造函数
     * 
     * @param context 上下文对象，用于获取资源和创建视图
     * @param attrs 属性集，包含XML中定义的属性
     */
    public SerialsWordLinSingleScreenTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);  // 调用三参数构造函数，默认样式为0
    }

    /**
     * 三参数构造函数（完整构造）
     * 初始化画笔、虚线效果、尺寸和列宽度参数
     * 
     * @param context 上下文对象，用于获取资源和创建视图
     * @param attrs 属性集，包含XML中定义的属性
     * @param defStyleAttr 默认样式属性
     */
    public SerialsWordLinSingleScreenTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造函数
        this.context = context;  // 保存上下文引用
        dashPathEffect = new DashPathEffect(new float[]{1, 1, 1, 1}, 0);  // 创建虚线效果：点-空-点-空模式
        paint = new Paint();  // 创建画笔对象
        paint.setTextSize(context.getResources().getDimension(R.dimen.textSizeCur));  // 设置文本大小为当前尺寸
        paint.setAntiAlias(true);  // 启用抗锯齿，使文本更平滑
        paint.setTypeface(Typeface.SANS_SERIF);  // 设置字体为无衬线字体
        width = GlobalVar.get().getMainWave().x;  // 从全局变量获取主波形窗口宽度作为视图宽度
        rowHeight = (int) context.getResources().getDimension(R.dimen.formHeightDetail);  // 从资源获取表单详细行高度
        offset = -2;  // 设置垂直偏移量为-2像素，微调文本位置

        chWidth = (int) getResources().getDimension(R.dimen.lin_ch_width);  // 获取LIN通道列宽度
        timeWidth = (int) getResources().getDimension(R.dimen.lin_time_width);  // 获取LIN时间列宽度
        idWidth = (int) getResources().getDimension(R.dimen.lin_id_width);  // 获取LIN ID列宽度
        dataWidth = (int) getResources().getDimension(R.dimen.lin_data_width);  // 获取LIN数据列宽度
        crcWidth = (int) getResources().getDimension(R.dimen.lin_crc_width);  // 获取LIN CRC列宽度
        errorWidth = (int) getResources().getDimension(R.dimen.lin_error_width);  // 获取LIN错误列宽度
        TriggerWidth = (int) getResources().getDimension(R.dimen.lin_trigger_width);  // 获取LIN触发列宽度
    }

    /**
     * 设置数据方法
     * 接收LIN总线数据列表并触发视图重绘
     * 
     * @param list LIN总线数据列表，包含多帧数据
     * @param showLine 显示行数限制，限制可见区域的数据行数
     * @param showMs 是否显示毫秒时间格式（当前未使用）
     */
    public void setData(ArrayList<SerialBusTxtStruct.LinStruct> list, int showLine, boolean showMs) {
        this.list = list;  // 保存数据列表引用
        this.showLine = showLine;  // 保存显示行数限制
        this.showMs = showMs;  // 保存时间显示模式
        invalidate();  // 触发视图重绘，系统会调用onDraw方法
    }

    /**
     * 绘制方法（核心渲染逻辑）
     * 将多帧LIN数据以列表形式绘制到Canvas上
     * 特殊处理：根据LIN版本决定是否去除数据末尾的校验位
     * 
     * @param canvas 画布对象，用于执行绘制操作
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);  // 调用父类绘制方法

        for (int i = 0, drawLine = 0; i < list.size() && i < showLine; i++) {  // 循环遍历数据列表，限制显示行数
            SerialBusTxtStruct.LinStruct bean = list.get(i);  // 获取当前帧数据对象
            String ch = bean.Ch;  // 获取通道名称（如"S1"）
            String time = TBookUtil.getStringFrom10us(bean.CurTime);  // 将10微秒单位时间转换为可读时间字符串
            String id = Tools.ByteToHexString((byte) bean.Id);  // 将ID转换为十六进制字符串
            //FIXME 非LIN1.3时FPGA传过来的data会在末尾多出来校验位数据，这里去掉。
            String data = bean.Data.trim();  // 获取数据字段并去除首尾空格
            int chan= TChan.findChanByValue(ch);  // 根据通道名称查找通道编号
            if (chan==-1) chan=TChan.S1;  // 如果未找到，默认使用S1通道
            chan=TChan.toFpgaChNo(chan);  // 转换为FPGA通道编号
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(chan);  // 从通道工厂获取串行通道对象
            if (serialChannel != null) {  // 如果串行通道存在
                LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN);  // 从串行通道获取LIN总线对象
                if (linBus != null && linBus.getLinType() != LinBus.LIN_TYPE_1_3) {  // 如果LIN总线存在且不是LIN1.3版本
                    if (data.length() > 2) {  // 如果数据长度大于2个字符
                        data = data.substring(0, data.length() - 3);  // 去除末尾3个字符（校验位数据）
                    }
                }
            }
            String error = String.valueOf(bean.Error);  // 获取错误码值
            String check = Tools.ByteToHexString((byte) bean.Check);  // 将校验值转换为十六进制字符串
            String trigger = bean.Trigger ? "Yes" : "";  // 判断是否触发帧，触发则显示"Yes"
            Rect chRect = Tools.getTextRect("S1", paint);  // 计算通道文本的矩形边界
            Rect timeRect = Tools.getTextRect(Tools.getTextFormat(time), paint);  // 计算时间文本的矩形边界
            Rect idRect = Tools.getTextRect(id, paint);  // 计算ID文本的矩形边界
            Rect dataRect = Tools.getTextRect(data, paint);  // 计算数据文本的矩形边界
            Rect errorRect = Tools.getTextRect(error, paint);  // 计算错误文本的矩形边界
            Rect checkRect = Tools.getTextRect(check, paint);  // 计算校验文本的矩形边界
            Rect triggerRect = Tools.getTextRect(trigger, paint);  // 计算触发文本的矩形边界
            int y = drawLine * rowHeight + rowHeight / 2 + chRect.height() / 2 + offset;  // 计算当前行的垂直位置
            paint.setColor(getResources().getColor(R.color.textColor));  // 设置画笔颜色为默认文本颜色
            canvas.drawText(ch, chWidth / 2 - chRect.width() / 2, y, paint);  // 在通道列中心位置绘制通道文本
            canvas.drawText(time, chWidth + timeWidth / 2 - timeRect.width() / 2, y, paint);  // 在时间列中心位置绘制时间文本
            canvas.drawText(id, chWidth + timeWidth + idWidth / 2 - idRect.width() / 2, y, paint);  // 在ID列中心位置绘制ID文本

            int curDrawLine = (data.length() - 1) / ISerialsWord.MAXCHAR_EACHROW_DATA_LIN + 1;  // 计算数据需要显示的行数（向上取整）
            for (int j = 0; j < curDrawLine; j++) {  // 循环绘制每一行数据
                if (j != curDrawLine - 1) {  // 如果不是最后一行（中间行）
                    canvas.drawText(data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_LIN
                            , (j + 1) * ISerialsWord.MAXCHAR_EACHROW_DATA_LIN)
                            , chWidth + timeWidth + idWidth + 15, y + j * rowHeight, paint);  // 绘制中间行数据片段
                } else {  // 如果是最后一行
                    canvas.drawText(data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_LIN)
                            , chWidth + timeWidth + idWidth + 15, y + j * rowHeight, paint);  // 绘制最后一行剩余数据
                    drawLine(canvas, y + j * rowHeight + 6);//最后一行数据下面画点线  // 在最后一行下方绘制虚线分隔符
                }
                drawLine++;  // 累加绘制行计数器
            }

            canvas.drawText(check, chWidth + timeWidth + idWidth + dataWidth + errorWidth + crcWidth / 2 - checkRect.width() / 2, y, paint);  // 在校验列中心位置绘制校验文本
            canvas.drawText(trigger, chWidth + timeWidth + idWidth + dataWidth + errorWidth + crcWidth + TriggerWidth / 2 - triggerRect.width() / 2, y, paint);  // 在触发列中心位置绘制触发文本
            paint.setColor(Color.RED);  // 设置画笔颜色为红色，用于突出显示错误信息
            canvas.drawText(error, chWidth + timeWidth + idWidth + dataWidth + errorWidth / 2 - errorRect.width() / 2, y, paint);  // 在错误列中心位置绘制错误文本（红色）
        }
    }


    /**
     * 绘制虚线方法
     * 在指定Y坐标绘制水平虚线分隔符
     * 
     * @param canvas 画布对象，用于执行绘制操作
     * @param y 虚线的Y坐标位置
     */
    private void drawLine(Canvas canvas, int y) {
        paint.setPathEffect(dashPathEffect);  // 设置虚线效果到画笔
        canvas.drawLine(0, y, width, y, paint);  // 从左边界到右边界绘制水平虚线
        paint.setPathEffect(null);  // 清除虚线效果，恢复普通绘制模式
    }
}