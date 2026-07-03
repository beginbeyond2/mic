package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.ICharacterEncoding;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStructParse;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                  SerialsWordUartSingleScreenTextView                         │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                │
 * │ UART串口总线整屏文本显示视图，负责多个UART数据帧的可视化呈现                   │
 * │                                                                             │
 * │ 【核心职责】                                                                │
 * │ 1. 显示多个UART数据帧的十六进制、二进制或ASCII格式数据                        │
 * │ 2. 支持多行数据的批量显示和每行固定数量显示                                   │
 * │ 3. 根据通道类型（S1/S2/S12）应用不同的颜色显示策略                            │
 * │                                                                             │
 * │ 【架构设计】                                                                │
 * │ 继承自Android View基类，采用自定义绘制方式实现数据可视化                      │
 * │ ┌──────────────────────────────────────────────────────────────┐             │
 * │ │  ┌──────┬──────┬──────┬──────┬──────┬──────┐ (第1行)         │             │
 * │ │  │ 0xFF │ 0xAB │ 0xCD │ 0xEF │ 0x01 │ ...  │                 │             │
 * │ │  ├──────┼──────┼──────┼──────┼──────┼──────┤ (第2行)         │             │
 * │ │  │ 0x12 │ 0x34 │ 0x56 │ 0x78 │ 0x9A │ ...  │                 │             │
 * │ │  └──────────────────────────────────────────┘                │             │
 * │ └──────────────────────────────────────────────────────────────┘             │
 * │                                                                             │
 * │ 【数据流向】                                                                │
 * │ ArrayList<UartStruct> -> setList() -> invalidate() -> onDraw()              │
 * │                                                                             │
 * │ 【依赖关系】                                                                │
 * │ - SerialBusTxtStruct.UartStruct: UART数据结构体                             │
 * │ - SerialBusTxtStructParse: 数据解析工具类                                    │
 * │ - Tools: 工具类，提供ASCII转换方法                                           │
 * │ - TChan: 通道颜色管理工具                                                    │
 * │                                                                             │
 * │ 【使用场景】                                                                │
 * │ 在UART串口解码功能中，用于显示整屏数据帧列表，支持多行显示和多种显示格式      │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class SerialsWordUartSingleScreenTextView extends View {
    private static final String TAG = "SerialsWordUartSingleScreenTextView"; // 日志标签
    public static final String TYPE_AXCII = "0";                       // ASCII显示类型常量
    public static final String TYPE_BIN = "00000000";                  // 二进制显示类型常量
    public static final String TYPE_HEX_NINE = "0F0";                  // 9位十六进制显示类型常量
    public static final String TYPE_HEX_OTHER = "FF";                  // 其他位十六进制显示类型常量

    private Context context;                                          // 上下文对象
    private Paint paint;                                              // 绘画画笔对象
    private int padding = 10;                                         // 左侧内边距
    private ArrayList<SerialBusTxtStruct.UartStruct> list = new ArrayList<>(); // UART数据列表
    private String showType = TYPE_HEX_OTHER;                         // 显示类型
    private int chType = ISerialsWord.TYPE_S1;                        // 通道类型
    private int countOfLine;                                          // 每行显示的数据个数
    private int showLine;                                             // 显示行数
    private int s1Color, s2Color;                                     // S1/S2通道颜色
    private int bits;                                                 // 数据位宽
    private int check;                                                // 校验位类型
    private int offset=0;                                             // Y轴偏移量
    private int rowHeight;                                            // 单行高度

    /**
     * 单参数构造函数
     * @param context 上下文对象
     */
    public SerialsWordUartSingleScreenTextView(Context context) {
        this(context, null);                                           // 调用双参数构造函数
    }

    /**
     * 双参数构造函数
     * @param context 上下文对象
     * @param attrs 属性集
     */
    public SerialsWordUartSingleScreenTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);                                       // 调用三参数构造函数
    }

    /**
     * 三参数构造函数（完整构造函数）
     * 初始化画笔、颜色、尺寸等属性
     * @param context 上下文对象
     * @param attrs 属性集
     * @param defStyleAttr 默认样式属性
     */
    public SerialsWordUartSingleScreenTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);                           // 调用父类构造函数
        this.context = context;                                       // 保存上下文引用
        paint = new Paint();                                          // 创建画笔对象
        paint.setTextSize(context.getResources().getDimension(R.dimen.textSizeCur)); // 设置文本大小
        paint.setAntiAlias(true);                                     // 启用抗锯齿
        paint.setTypeface(Typeface.SANS_SERIF);                       // 设置字体类型
        s1Color = getResources().getColor(com.micsig.tbook.ui.R.color.color_S1); // 获取S1颜色
        s2Color = getResources().getColor(com.micsig.tbook.ui.R.color.color_S2); // 获取S2颜色
        offset=-2;                                                    // 设置Y轴偏移量
        rowHeight = (int) context.getResources().getDimension(R.dimen.formHeightDetail); // 获取行高
    }

    /**
     * 设置UART数据列表并刷新视图
     * @param bits 数据位宽
     * @param check 校验位类型
     * @param showType 显示类型
     * @param chType 通道类型
     * @param countOfLine 每行显示的数据个数
     * @param showLine 显示行数
     * @param list UART数据列表
     */
    public void setList(int bits, int check, String showType, int chType, int countOfLine, int showLine, ArrayList<SerialBusTxtStruct.UartStruct> list) {
        this.bits = bits;                                             // 保存数据位宽
        this.check = check;                                           // 保存校验位类型
        this.showType = showType;                                     // 保存显示类型
        this.chType = chType;                                         // 保存通道类型
        this.countOfLine = countOfLine;                               // 保存每行数据个数
        this.showLine = showLine;                                     // 保存显示行数
        this.list = list;                                             // 保存UART数据列表
        invalidate();                                                 // 触发视图重绘
    }

    /**
     * 视图绘制方法
     * 负责绘制整屏UART数据帧的十六进制、二进制或ASCII格式数据
     * @param canvas 画布对象
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);                                          // 调用父类绘制方法
        if (list != null) {                                           // 数据列表不为空
            for (int row = 0; row < showLine; row++) {                 // 遍历每一行
                int baseY = row * rowHeight+offset;                   // 计算当前行基础Y坐标
                int textLeftWidth = showType.equals(TYPE_HEX_OTHER) || showType.equals(TYPE_HEX_NINE) ? 16 : showType.equals(TYPE_BIN) ? 95 : 10; // 计算左侧文本宽度
                int textLeftHeight = 11;                              // 设置左侧文本高度
                int textRightWidth = 10;                              // 设置右侧文本宽度
                int leftListWidth;                                    // 左侧列表宽度
                if (showType.equals(TYPE_HEX_OTHER)) {                // HEX_OTHER类型
                    leftListWidth = (int) getResources().getDimension(R.dimen.uart_hex_bitOther_width); // 获取HEX_OTHER宽度
                } else if (showType.equals(TYPE_HEX_NINE)) {          // HEX_NINE类型
                    leftListWidth = (int) getResources().getDimension(R.dimen.uart_hex_bit9_width); // 获取HEX_NINE宽度
                } else if (showType.equals(TYPE_BIN)) {               // BIN类型
                    leftListWidth = (int) getResources().getDimension(R.dimen.uart_bin_width); // 获取BIN宽度
                } else {                                               // ASCII类型
                    leftListWidth = GlobalVar.get().getMainWave().x;  // 使用波形区域宽度
                }
                for (int i = 0; i < countOfLine; i++) {               // 遍历当前行的每个数据
                    int xLeft = i * (textLeftWidth + (TYPE_AXCII.equals(showType) ? 10
                            : TYPE_HEX_NINE.equals(showType) ? 25
                            : TYPE_BIN.equals(showType) ? 20 : 15)) + padding; // 计算左侧X坐标
                    String s;                                          // 格式化后的数据字符串
                    int index = i + row * countOfLine;                // 计算数据在列表中的索引
                    if (index >= list.size()) break;                  // 索引超出列表范围则退出循环
                    int bitWidth = this.check == 0 ? this.bits : this.bits - 1; // 计算有效数据位宽
                    bitWidth += 5;                                    // 增加显示位宽
                    if (TYPE_BIN.equals(showType)) {                  // 二进制格式
//                        s = Integer.toBinaryString(list.get(index).Data & 0xFF);
//                        s = String.format("%08d", Integer.valueOf(s));
                        s = SerialBusTxtStructParse.getEncoding(ICharacterEncoding.Binary, list.get(index).Data, bitWidth); // 解析为二进制字符串
                    } else if (TYPE_HEX_OTHER.equals(showType)) {     // HEX_OTHER格式
//                        s = Integer.toHexString(list.get(index).Data & 0xFF).toUpperCase();
//                        s = s.length() < 2 ? "0" + s : s;
                        s = SerialBusTxtStructParse.getEncoding(ICharacterEncoding.Hex, list.get(index).Data, bitWidth); // 解析为十六进制字符串
                    } else if (TYPE_HEX_NINE.equals(showType)) {      // HEX_NINE格式
//                        s = Integer.toHexString(list.get(index).Data & 0xFFF).toUpperCase();
//                        s = s.length() < 2 ? "00" + s : (s.length() < 3 ? "0" + s : s);
                        s = SerialBusTxtStructParse.getEncoding(ICharacterEncoding.Hex, list.get(index).Data, 9); // 解析为9位十六进制字符串
                    } else {                                           // ASCII格式
                        s = Tools.getASCIIFromInt(list.get(index).Data, " "); // 转换为ASCII字符串
                    }
                    if (chType != ISerialsWord.TYPE_S12) {            // 非S12双通道模式
                        paint.setColor(list.get(index).Color);        // 设置数据颜色
                        canvas.drawText(s, xLeft, baseY + rowHeight / 2 + textLeftHeight / 2, paint); // 绘制左侧数据文本
                    } else {                                           // S12双通道模式
                        int color=TChan.getChannelColor(context, list.get(index).Ch); // 获取通道颜色
                        paint.setColor(color);                        // 设置通道颜色
                        paint.setAlpha(200);                          // 设置透明度
                        Rect textRect = Tools.getTextRect(s, paint);  // 测量文本边界
                        canvas.drawRect(xLeft, baseY + 2, xLeft + textRect.width() + 4, baseY + rowHeight - 2, paint); // 绘制背景矩形
                        paint.setColor(list.get(index).Color);        // 设置数据颜色
                        paint.setAlpha(255);                          // 设置透明度
                        canvas.drawText(s, xLeft, baseY + rowHeight / 2 + textLeftHeight / 2, paint); // 绘制左侧数据文本
                    }
                    if (!TYPE_AXCII.equals(showType)) {               // 非ASCII格式（显示右侧ASCII）
                        int xRight = i * (textRightWidth + 10) + padding + leftListWidth; // 计算右侧X坐标
                        if (chType != ISerialsWord.TYPE_S12) {        // 非S12双通道模式
                            paint.setColor(list.get(index).Color);    // 设置数据颜色
                            canvas.drawText(Tools.getASCIIFromInt(list.get(index).Data, "."), xRight, baseY + rowHeight / 2 + textLeftHeight / 2, paint); // 绘制右侧ASCII文本
                        } else {                                       // S12双通道模式
                            int color= TChan.getChannelColor(context, list.get(index).Ch); // 获取通道颜色
                            paint.setColor(color);                    // 设置通道颜色
                            paint.setAlpha(200);                      // 设置透明度
                            canvas.drawRect(xRight - 2, baseY + 2, xRight + 15, baseY + rowHeight - 2, paint); // 绘制背景矩形
                            paint.setColor(list.get(index).Color);    // 设置数据颜色
                            paint.setAlpha(255);                      // 设置透明度
                            canvas.drawText(Tools.getASCIIFromInt(list.get(index).Data, "."), xRight, baseY + rowHeight / 2 + textLeftHeight / 2, paint); // 绘制右侧ASCII文本
                        }
                    }
                }
            }
        }
    }
}