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

import com.micsig.tbook.tbookscope.GlobalVar;  // 导入全局变量类
import com.micsig.tbook.tbookscope.R;  // 导入资源类
import com.micsig.tbook.tbookscope.tools.Tools;  // 导入工具类
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;  // 导入串行总线文本结构类
import com.micsig.tbook.ui.util.TBookUtil;  // 导入TBook工具类

/*
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                      SerialsWordI2cSingleRowTextView                         │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                  │
 * │   串行总线协议数据显示模块 - I2C总线单行数据文本视图组件                        │
 * │                                                                              │
 * │ 【核心职责】                                                                  │
 * │   1. 显示I2C总线单帧数据的详细信息                                             │
 * │   2. 以表格形式展示通道、时间、地址、数据、确认、触发、重启等信息               │
 * │   3. 支持数据自动换行和多行显示                                               │
 * │   4. 支持虚线分隔符绘制                                                       │
 * │   5. 突出显示ACK/NACK确认状态                                                 │
 * │                                                                              │
 * │ 【架构设计】                                                                  │
 * │   继承自Android View基类，通过Canvas进行自定义绘制，采用列宽度固定布局方式      │
 * │   - SingleRow模式：用于详细展示单帧数据，数据可能多行显示                       │
 * │   - SingleScreen模式：用于列表展示多帧数据，每帧单行显示                        │
 * │                                                                              │
 * │ 【数据流向】                                                                  │
 * │   SerialBusTxtStruct.I2cStruct → setData() → invalidate() → onDraw()         │
 * │   FPGA采集 → 解析为I2cStruct → 视图渲染                                       │
 * │                                                                              │
 * │ 【依赖关系】                                                                  │
 * │   - SerialBusTxtStruct.I2cStruct：I2C总线数据结构                            │
 * │   - GlobalVar：获取主波形窗口尺寸                                             │
 * │   - Tools：文本矩形计算工具                                                   │
 * │   - TBookUtil：时间格式化工具                                                 │
 * │   - R.dimen：列宽度资源定义                                                   │
 * │                                                                              │
 * │ 【使用场景】                                                                  │
 * │   用于I2C总线解码列表界面，当用户点击某一行数据时，在详细区域展示该帧的完整信息 │
 * └──────────────────────────────────────────────────────────────────────────────┘
 */
public class SerialsWordI2cSingleRowTextView extends View {
    private Context context;  // 上下文对象引用
    private Paint paint;  // 画笔对象，用于绘制文本和线条
    private DashPathEffect dashPathEffect;  // 虚线效果对象，用于绘制分隔虚线
    private int width;  // 视图总宽度
    private int height;  // 单行高度
    private SerialBusTxtStruct.I2cStruct bean;  // I2C总线数据结构对象
    private boolean showMs = false;  // 是否显示毫秒时间（预留功能）

    private int chWidth;  // 通道列宽度
    private int timeWidth;  // 时间列宽度
    private int addrWidth;  // 地址列宽度
    private int dataWidth;  // 数据列宽度
    private int confirmWidth;  // 确认列宽度
    private int triggerWidth;  // 触发列宽度
    private int restartWidth;  // 重启列宽度

    /**
     * 单参数构造函数
     * 
     * @param context 上下文对象，用于获取资源和创建视图
     */
    public SerialsWordI2cSingleRowTextView(Context context) {
        this(context, null);  // 调用双参数构造函数，属性集为null
    }

    /**
     * 双参数构造函数
     * 
     * @param context 上下文对象，用于获取资源和创建视图
     * @param attrs 属性集，包含XML中定义的属性
     */
    public SerialsWordI2cSingleRowTextView(Context context, @Nullable AttributeSet attrs) {
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
    public SerialsWordI2cSingleRowTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造函数
        this.context = context;  // 保存上下文引用
        dashPathEffect = new DashPathEffect(new float[]{1, 1, 1, 1}, 0);  // 创建虚线效果：点-空-点-空模式
        paint = new Paint();  // 创建画笔对象
        paint.setTextSize(context.getResources().getDimension(R.dimen.textSizeCur));  // 设置文本大小为当前尺寸
        paint.setAntiAlias(true);  // 启用抗锯齿，使文本更平滑
        paint.setTypeface(Typeface.SANS_SERIF);  // 设置字体为无衬线字体
        width = GlobalVar.get().getMainWave().x;  // 从全局变量获取主波形窗口宽度作为视图宽度
        height = (int) context.getResources().getDimension(R.dimen.formHeightDetail);  // 从资源获取表单详细行高度

        chWidth = (int) getResources().getDimension(R.dimen.i2c_ch_width);  // 获取I2C通道列宽度
        timeWidth = (int) getResources().getDimension(R.dimen.i2c_time_width);  // 获取I2C时间列宽度
        addrWidth = (int) getResources().getDimension(R.dimen.i2c_addr_width);  // 获取I2C地址列宽度
        dataWidth = (int) getResources().getDimension(R.dimen.i2c_data_width);  // 获取I2C数据列宽度
        confirmWidth = (int) getResources().getDimension(R.dimen.i2c_confirm_width);  // 获取I2C确认列宽度
        triggerWidth = (int) getResources().getDimension(R.dimen.i2c_trigger_width);  // 获取I2C触发列宽度
        restartWidth = (int) getResources().getDimension(R.dimen.i2c_restart_width);  // 获取I2C重启列宽度
    }

    /**
     * 设置数据方法
     * 接收I2C总线数据结构并触发视图重绘
     * 
     * @param bean I2C总线数据结构对象，包含完整帧信息
     * @param showMs 是否显示毫秒时间格式（当前未使用）
     */
    public void setData(SerialBusTxtStruct.I2cStruct bean, boolean showMs) {
        this.bean = bean;  // 保存数据对象引用
        this.showMs = showMs;  // 保存时间显示模式
        invalidate();  // 触发视图重绘，系统会调用onDraw方法
    }

    /**
     * 绘制方法（核心渲染逻辑）
     * 将I2C帧数据以表格形式绘制到Canvas上
     * 
     * @param canvas 画布对象，用于执行绘制操作
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);  // 调用父类绘制方法
        if (bean == null) return;  // 如果数据为空，直接返回不绘制

        String ch = bean.Ch;  // 获取通道名称（如"S1"）
        String time = TBookUtil.getStringFrom10us(bean.CurTime);  // 将10微秒单位时间转换为可读时间字符串
        String addr = String.valueOf(bean.Addr);  // 获取I2C地址值
        String data = bean.Data.trim();  // 获取数据字段并去除首尾空格
        String confirm = bean.Confirm ? "" : "X";  // 判断ACK状态：确认时为空，NACK时显示"X"
        String trigger = bean.Trigger ? "Yes" : "";  // 判断是否触发帧，触发则显示"Yes"
        String restart = String.valueOf(bean.Reboot ? "Yes" : "");  // 判断是否重启帧，重启则显示"Yes"
        Rect chRect = Tools.getTextRect("S1", paint);  // 计算通道文本的矩形边界
        Rect timeRect = Tools.getTextRect(Tools.getTextFormat(time), paint);  // 计算时间文本的矩形边界
        Rect addrRect = Tools.getTextRect(addr, paint);  // 计算地址文本的矩形边界
        Rect dataRect = Tools.getTextRect(data, paint);  // 计算数据文本的矩形边界
        Rect confirmRect = Tools.getTextRect(confirm, paint);  // 计算确认文本的矩形边界
        Rect triggerRect = Tools.getTextRect(trigger, paint);  // 计算触发文本的矩形边界
        Rect restartRect = Tools.getTextRect(restart, paint);  // 计算重启文本的矩形边界
        paint.setColor(getResources().getColor(R.color.textColor));  // 设置画笔颜色为默认文本颜色
        int y = height / 2 + chRect.height() / 2;  // 计算文本垂直中心位置：行高的一半 + 文本高度的一半
        canvas.drawText(ch, chWidth / 2 - chRect.width() / 2, y, paint);  // 在通道列中心位置绘制通道文本
        canvas.drawText(time, chWidth + timeWidth / 2 - timeRect.width() / 2, y, paint);  // 在时间列中心位置绘制时间文本
        canvas.drawText(addr, chWidth + timeWidth + addrWidth / 2 - addrRect.width() / 2, y, paint);  // 在地址列中心位置绘制地址文本

        int curDrawLine = (data.length() - 1) / ISerialsWord.MAXCHAR_EACHROW_DATA_I2C + 1;  // 计算数据需要显示的行数（向上取整）
        for (int j = 0; j < curDrawLine; j++) {  // 循环绘制每一行数据
            if (j != curDrawLine - 1) {  // 如果不是最后一行（中间行）
                canvas.drawText(data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_I2C
                        , (j + 1) * ISerialsWord.MAXCHAR_EACHROW_DATA_I2C)
                        , chWidth + timeWidth + addrWidth + 15, y + j * height, paint);  // 绘制中间行数据片段
            } else {  // 如果是最后一行
                canvas.drawText(data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_I2C)
                        , chWidth + timeWidth + addrWidth + 15, y + j * height, paint);  // 绘制最后一行剩余数据
                drawLine(canvas, y + j * height + 6);//最后一行数据下面画点线  // 在最后一行下方绘制虚线分隔符
            }
        }

        canvas.drawText(trigger, chWidth + timeWidth + addrWidth + dataWidth + confirmWidth + triggerWidth / 2 - triggerRect.width() / 2, y, paint);  // 在触发列中心位置绘制触发文本
        canvas.drawText(restart, chWidth + timeWidth + addrWidth + dataWidth + confirmWidth + triggerWidth + restartWidth / 2 - restartRect.width() / 2, y, paint);  // 在重启列中心位置绘制重启文本
        paint.setColor(Color.RED);  // 设置画笔颜色为红色，用于突出显示NACK状态
        canvas.drawText(confirm, chWidth + timeWidth + addrWidth + dataWidth + confirmWidth / 2 - confirmRect.width() / 2, y, paint);  // 在确认列中心位置绘制确认文本（NACK时红色显示）
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