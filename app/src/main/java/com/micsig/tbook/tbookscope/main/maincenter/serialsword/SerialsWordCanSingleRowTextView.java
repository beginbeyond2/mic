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
 * │                      SerialsWordCanSingleRowTextView                         │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                  │
 * │   串行总线协议数据显示模块 - CAN总线单行数据文本视图组件                        │
 * │                                                                              │
 * │ 【核心职责】                                                                  │
 * │   1. 显示CAN总线单帧数据的详细信息                                             │
 * │   2. 以表格形式展示通道、时间、ID、类型、DLC、数据、CRC、错误、触发等信息       │
 * │   3. 支持数据自动换行和多行显示                                               │
 * │   4. 支持虚线分隔符绘制                                                       │
 * │                                                                              │
 * │ 【架构设计】                                                                  │
 * │   继承自Android View基类，通过Canvas进行自定义绘制，采用列宽度固定布局方式      │
 * │   - SingleRow模式：用于详细展示单帧数据，数据可能多行显示                       │
 * │   - SingleScreen模式：用于列表展示多帧数据，每帧单行显示                        │
 * │                                                                              │
 * │ 【数据流向】                                                                  │
 * │   SerialBusTxtStruct.CanStruct → setData() → invalidate() → onDraw()         │
 * │   FPGA采集 → 解析为CanStruct → 视图渲染                                       │
 * │                                                                              │
 * │ 【依赖关系】                                                                  │
 * │   - SerialBusTxtStruct.CanStruct：CAN总线数据结构                            │
 * │   - GlobalVar：获取主波形窗口尺寸                                             │
 * │   - Tools：文本矩形计算工具                                                   │
 * │   - TBookUtil：时间格式化工具                                                 │
 * │   - R.dimen：列宽度资源定义                                                   │
 * │                                                                              │
 * │ 【使用场景】                                                                  │
 * │   用于CAN总线解码列表界面，当用户点击某一行数据时，在详细区域展示该帧的完整信息   │
 * └──────────────────────────────────────────────────────────────────────────────┘
 */
public class SerialsWordCanSingleRowTextView extends View {
    private Context context;  // 上下文对象引用
    private Paint paint;  // 画笔对象，用于绘制文本和线条
    private DashPathEffect dashPathEffect;  // 虚线效果对象，用于绘制分隔虚线
    private int width;  // 视图总宽度
    private int height;  // 单行高度
    private SerialBusTxtStruct.CanStruct bean;  // CAN总线数据结构对象
    private boolean showMs = false;  // 是否显示毫秒时间（预留功能）

    private int chWidth;  // 通道列宽度
    private int timeWidth;  // 时间列宽度
    private int idWidth;  // ID列宽度
    private int typeWidth;  // 类型列宽度
    private int dlcWidth;  // DLC（数据长度）列宽度
    private int dataWidth;  // 数据列宽度
    private int crcWidth;  // CRC校验列宽度
    private int errorWidth;  // 错误列宽度
    private int triggerWidth;  // 触发列宽度

    /**
     * 单参数构造函数
     * 
     * @param context 上下文对象，用于获取资源和创建视图
     */
    public SerialsWordCanSingleRowTextView(Context context) {
        this(context, null);  // 调用双参数构造函数，属性集为null
    }

    /**
     * 双参数构造函数
     * 
     * @param context 上下文对象，用于获取资源和创建视图
     * @param attrs 属性集，包含XML中定义的属性
     */
    public SerialsWordCanSingleRowTextView(Context context, @Nullable AttributeSet attrs) {
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
    public SerialsWordCanSingleRowTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造函数
        this.context = context;  // 保存上下文引用
        dashPathEffect = new DashPathEffect(new float[]{1, 1, 1, 1}, 0);  // 创建虚线效果：点-空-点-空模式
        paint = new Paint();  // 创建画笔对象
        paint.setTextSize(context.getResources().getDimension(R.dimen.textSizeCur));  // 设置文本大小为当前尺寸
        paint.setAntiAlias(true);  // 启用抗锯齿，使文本更平滑
        paint.setTypeface(Typeface.SANS_SERIF);  // 设置字体为无衬线字体
        width = GlobalVar.get().getMainWave().x;  // 从全局变量获取主波形窗口宽度作为视图宽度
        height = (int) context.getResources().getDimension(R.dimen.formHeightDetail);  // 从资源获取表单详细行高度

        chWidth = (int) getResources().getDimension(R.dimen.can_ch_width);  // 获取CAN通道列宽度
        timeWidth = (int) getResources().getDimension(R.dimen.can_time_width);  // 获取CAN时间列宽度
        idWidth = (int) getResources().getDimension(R.dimen.can_id_width);  // 获取CAN ID列宽度
        typeWidth = (int) getResources().getDimension(R.dimen.can_type_width);  // 获取CAN类型列宽度
        dlcWidth = (int) getResources().getDimension(R.dimen.can_dlc_width);  // 获取CAN DLC列宽度
        dataWidth = (int) getResources().getDimension(R.dimen.can_data_width);  // 获取CAN数据列宽度
        crcWidth = (int) getResources().getDimension(R.dimen.can_crc_width);  // 获取CAN CRC列宽度
        errorWidth = (int) getResources().getDimension(R.dimen.can_error_width);  // 获取CAN错误列宽度
        triggerWidth = (int) getResources().getDimension(R.dimen.can_trigger_width);  // 获取CAN触发列宽度
    }

    /**
     * 设置数据方法
     * 接收CAN总线数据结构并触发视图重绘
     * 
     * @param bean CAN总线数据结构对象，包含完整帧信息
     * @param showMs 是否显示毫秒时间格式（当前未使用）
     */
    public void setData(SerialBusTxtStruct.CanStruct bean, boolean showMs) {
        this.bean = bean;  // 保存数据对象引用
        this.showMs = showMs;  // 保存时间显示模式
        invalidate();  // 触发视图重绘，系统会调用onDraw方法
    }

    /**
     * 绘制方法（核心渲染逻辑）
     * 将CAN帧数据以表格形式绘制到Canvas上
     * 
     * @param canvas 画布对象，用于执行绘制操作
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);  // 调用父类绘制方法
        if (bean == null) return;  // 如果数据为空，直接返回不绘制

//        Logger.i(Command.TAG,"onDraw:"+bean.toString());

        String ch = bean.Ch;  // 获取通道名称（如"S1"）
        String time = TBookUtil.getStringFrom10us(bean.CurTime);  // 将10微秒单位时间转换为可读时间字符串
        String id =bean.getId(); //Integer.toHexString(bean.ID).toUpperCase();  // 获取CAN帧ID（十六进制格式）
        String type = bean.getTypeEnum(); //(bean.TypeEnum== SerialBusTxtStruct.CanStruct.Type_STDID?"SFF":"EFF");  // 获取帧类型（标准帧/扩展帧）
//        String dlc = Integer.toHexString(bean.DLC).toUpperCase();
        String dlc = Integer.toString(bean.DLC);  // 获取数据长度（十进制格式）
        String data = bean.Data.trim();  // 获取数据字段并去除首尾空格
//        String crc = Integer.toHexString(bean.CRC).toUpperCase();
        String crc = String.format("%0" + bean.crc_w/4 + "x", bean.CRC).toUpperCase();  // 格式化CRC值，根据CRC宽度补零
        String error = (bean.getErrorEnum());  // 获取错误类型枚举字符串
        String trigger = bean.Trigger ? "Yes" : "";  // 判断是否触发帧，触发则显示"Yes"
        Rect chRect = Tools.getTextRect("S1", paint);  // 计算通道文本的矩形边界
        Rect timeRect = Tools.getTextRect(Tools.getTextFormat(time), paint);  // 计算时间文本的矩形边界
        Rect idRect = Tools.getTextRect(id, paint);  // 计算ID文本的矩形边界
        Rect typeRect = Tools.getTextRect(type, paint);  // 计算类型文本的矩形边界
        Rect dlcRect = Tools.getTextRect(dlc, paint);  // 计算DLC文本的矩形边界
        Rect dataRect = Tools.getTextRect(data, paint);  // 计算数据文本的矩形边界
        Rect crcRect = Tools.getTextRect(crc, paint);  // 计算CRC文本的矩形边界
        Rect errorRect = Tools.getTextRect(error, paint);  // 计算错误文本的矩形边界
        Rect triggerRect = Tools.getTextRect(trigger, paint);  // 计算触发文本的矩形边界
        paint.setColor(getResources().getColor(R.color.textColor));  // 设置画笔颜色为默认文本颜色
        int y = height / 2 + chRect.height() / 2;  // 计算文本垂直中心位置：行高的一半 + 文本高度的一半
        canvas.drawText(ch, chWidth / 2 - chRect.width() / 2, y, paint);  // 在通道列中心位置绘制通道文本
        canvas.drawText(time, chWidth + timeWidth / 2 - timeRect.width() / 2, y, paint);  // 在时间列中心位置绘制时间文本
        canvas.drawText(id, chWidth + timeWidth + idWidth / 2 - idRect.width() / 2, y, paint);  // 在ID列中心位置绘制ID文本
        canvas.drawText(type, chWidth + timeWidth + idWidth + typeWidth / 2 - typeRect.width() / 2, y, paint);  // 在类型列中心位置绘制类型文本
        canvas.drawText(dlc, chWidth + timeWidth + idWidth + typeWidth + dlcWidth / 2 - dlcRect.width() / 2, y, paint);  // 在DLC列中心位置绘制DLC文本

        int curDrawLine = (data.length() - 1) / ISerialsWord.MAXCHAR_EACHROW_DATA_CAN + 1;  // 计算数据需要显示的行数（向上取整）
        for (int j = 0; j < curDrawLine; j++) {  // 循环绘制每一行数据
            if (j != curDrawLine - 1) {  // 如果不是最后一行（中间行）
                String temp = data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_CAN
                        , (j + 1) * ISerialsWord.MAXCHAR_EACHROW_DATA_CAN);  // 提取当前行的完整数据片段
                String before = temp.substring(0, ISerialsWord.MAXCHAR_EACHROW_DATA_CAN / 2);  // 将数据分为前后两部分
                String after = temp.substring(ISerialsWord.MAXCHAR_EACHROW_DATA_CAN / 2);  // 提取后半部分数据
                canvas.drawText(before + "  " + after,
                        chWidth + timeWidth + idWidth + typeWidth + dlcWidth + 15, y + j * height, paint);  // 绘制中间行数据，前后部分间添加空格
            } else {  // 如果是最后一行
                String temp = data.substring(j * ISerialsWord.MAXCHAR_EACHROW_DATA_CAN);  // 提取最后一行的剩余数据
                if (temp.length() > ISerialsWord.MAXCHAR_EACHROW_DATA_CAN / 2) {  // 如果数据长度超过半行
                    String before = temp.substring(0, ISerialsWord.MAXCHAR_EACHROW_DATA_CAN / 2);  // 分割为前后两部分
                    String after = temp.substring(ISerialsWord.MAXCHAR_EACHROW_DATA_CAN / 2);  // 提取后半部分
                    temp = before + "  " + after;  // 合并前后部分，中间添加空格
                }
                canvas.drawText(temp,
                        chWidth + timeWidth + idWidth + typeWidth + dlcWidth + 15, y + j * height, paint);  // 绘制最后一行数据
                drawLine(canvas, y + j * height + 6);//最后一行数据下面画点线  // 在最后一行下方绘制虚线分隔符
            }
        }

        canvas.drawText(crc, chWidth + timeWidth + idWidth + typeWidth + dlcWidth + dataWidth + crcWidth / 2 - crcRect.width() / 2, y, paint);  // 在CRC列中心位置绘制CRC文本
        canvas.drawText(trigger, chWidth + timeWidth + idWidth + typeWidth + dlcWidth + dataWidth + crcWidth + errorWidth + triggerWidth / 2 - triggerRect.width() / 2, y, paint);  // 在触发列中心位置绘制触发文本
        paint.setColor(Color.RED);  // 设置画笔颜色为红色，用于突出显示错误信息
        canvas.drawText(error, chWidth + timeWidth + idWidth + typeWidth + dlcWidth + dataWidth + crcWidth + errorWidth / 2 - errorRect.width() / 2, y, paint);  // 在错误列中心位置绘制错误文本（红色）
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