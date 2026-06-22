package com.micsig.tbook.tbookscope.wavezone.wave.wavedata; // 串口总线解码解析器所在包

import android.graphics.Canvas; // Android画布类，用于绘制解码图像
import android.graphics.Color; // 颜色常量类，提供YELLOW/RED等帧颜色
import android.graphics.Paint; // 画笔类，控制绘制样式（颜色、字体等）
import android.graphics.PorterDuff; // 图像混合模式类，用于DST_OUT/SRC_OVER等
import android.graphics.PorterDuffXfermode; // 图像混合模式包装类，配合Paint使用
import android.graphics.Rect; // 矩形类，用于定义标题栏擦除区域
import android.graphics.Typeface; // 字体类，用于设置MONOSPACE/SERIF等字体

import com.micsig.base.Logger; // 日志工具类，用于调试输出
import com.micsig.tbook.tbookscope.R; // 资源ID类，获取textColor等颜色资源
import com.micsig.tbook.tbookscope.tools.Tools; // 工具类，提供文本测量和计时功能
import com.micsig.tbook.tbookscope.util.App; // 应用上下文工具类，获取Resources
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类，读取ARINC429格式配置
import com.micsig.tbook.ui.wavezone.TChan; // 通道号转换类，将S1/S2转为序列号

import java.nio.ByteBuffer; // 字节缓冲区类，存放FPGA上传的原始数据
import java.util.ArrayList; // 动态数组类，存放解析结果列表
import java.util.List; // 列表接口，解析结果的通用类型

/**
 * ┌──────────────────────────────────────────────────────────────────────────────────┐
 * │                       SerialBusStructParse                                      │
 * ├──────────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：wavedata包 → 串口总线图像解码解析器（Holder单例）                         │
 * │ 核心职责：1. 从FPGA的ByteBuffer中解析7种串口总线协议的二进制数据为结构体列表          │
 * │          2. 在Canvas上绘制解析后的串口总线解码图像（菱形+文字）                     │
 * │          3. 支持UART/LIN/CAN/SPI/I2C/ARINC429/MIL-STD-1553B共7种总线协议        │
 * │ 架构设计：采用Holder单例模式，每种总线协议有独立的解析方法(getXxxStruct)和           │
 * │          绘制方法(drawParseXxxData)，通过toParse入口方法根据总线类型分发            │
 * │ 数据流：  FPGA → ByteBuffer → getXxxStruct解析 → List<XxxStruct> →             │
 * │          drawParseXxxData绘制 → Canvas显示                                      │
 * │ 依赖关系：SerialBusStruct（数据结构定义）、ICharacterEncoding（编码常量）、          │
 * │          Canvas/Paint（Android绘图）、CacheUtil（ARINC429格式配置）、              │
 * │          TChan（通道号转换）、Tools（文本测量）                                    │
 * │ 使用场景：示波器串口总线解码功能，用户选择总线类型后，FPGA采集信号并上传原始数据，      │
 * │          本类负责将原始数据解析为可读的协议帧并在屏幕上绘制解码结果                    │
 * ├──────────────────────────────────────────────────────────────────────────────────┤
 * │ 协议支持：                                                                       │
 * │   UART     → getUartStruct / drawParseUartData     通用异步收发器                 │
 * │   LIN      → getLinStruct / drawParseLinData       局域互联网络                   │
 * │   CAN      → getCanStruct / drawParseCanData       控制器局域网                   │
 * │   SPI      → getSpiStruct / drawParseSpiData       串行外设接口                   │
 * │   I2C      → getI2cStruct / drawParseI2cData       内部集成电路总线               │
 * │   ARINC429 → getArinc429Struct / drawParseArinc429Data 航空电子数字总线           │
 * │   1553B    → getMilSTD1553bStruct / drawParseMilSTD1553bData 军用航空数据总线     │
 * ├──────────────────────────────────────────────────────────────────────────────────┤
 * │ 关键算法：                                                                       │
 * │   1. 时间戳转换：FPGA时间戳 × Cycle(40000) / timeToPix → 像素坐标               │
 * │   2. 图像混合：使用PorterDuffXfermode(DST_OUT)擦除标题区域重叠部分               │
 * │   3. 编码转换：getEncoding方法支持Hex/Binary/Octal/Decimal/ASCII五种编码          │
 * │   4. 绘制元素：drawElement方法绘制菱形边框+文字的解码元素                         │
 * │   5. 9位数据拼接：UART的9bit数据需两次8字节包拼接（DataType_3标志位判断）         │
 * ├──────────────────────────────────────────────────────────────────────────────────┤
 * │ ByteBuffer数据包格式（每8字节一组）：                                              │
 * │   byte[0-1]：Reserve保留字段                                                     │
 * │   byte[2]  ：数据值（协议帧的有效载荷）                                            │
 * │   byte[3]  ：数据类型标识（bit7=过采样, 低4位=帧类型）                              │
 * │   byte[4-7]：时间戳（32位小端序，FPGA采样时间点）                                  │
 * └──────────────────────────────────────────────────────────────────────────────────┘
 *
 * 串口总线图像解码解析器，负责将FPGA上传的原始二进制数据解析为各协议的结构体列表，
 * 并在Canvas上绘制对应的解码图像。采用Holder内部类实现线程安全的懒加载单例。
 *
 * Created by liwb on 2017/10/18.
 */

public class SerialBusStructParse { // 串口总线图像解码解析器，负责解析7种串口协议并绘制解码图像
    /** 日志标签，用于Logcat调试输出，取值：固定字符串"SerialBusStructParse" */
    private static final String TAG = "SerialBusStructParse"; // 日志标签，用于Logcat调试输出
    /** FPGA时钟周期转换常量，值40000(4e4)，用于将FPGA时间戳转换为像素坐标：像素 = 时间戳 × Cycle / timeToPix */
    private static final double Cycle=4e4; // FPGA时钟周期转换常量，值40000，用于时间戳→像素坐标转换
    //region 单例
    /** Holder内部类，实现线程安全的懒加载单例模式，利用JVM类加载机制保证线程安全 */
    public static class SerialBusStructParseHolder { // Holder内部类，利用JVM类加载机制实现线程安全的懒加载单例
        /** 单例实例，取值：唯一的SerialBusStructParse对象 */
        public static final SerialBusStructParse instance = new SerialBusStructParse(); // 单例实例，JVM类加载时创建
    }

    /**
     * 获取SerialBusStructParse单例实例
     * @return SerialBusStructParse全局唯一实例
     * 业务意义：所有串口总线解码共用同一个解析器，避免重复创建对象
     */
    public static SerialBusStructParse getInstance() { // 获取SerialBusStructParse单例实例
        return SerialBusStructParseHolder.instance; // 通过Holder内部类获取单例
    }
    //endregion

    //region 变量

    /** 时间到像素的转换因子，由UI层传入，取值范围：正整数，单位：时间/50格 */
    private long timeToPix; // 时间到像素的转换因子，由UI层传入
    /** 绘制区域的起始X坐标（像素），取值范围：0~屏幕宽度，通常为标题栏右侧 */
    private int startX, endX; // 绘制区域的起始/结束X坐标（像素）
    /** 当前编码方式，取值范围：ICharacterEncoding常量（Hex=0x16/Binary=0x02/Octal=0x08/Decimal=0x10/ASCII=0xAC） */
    private int encoding; // 当前编码方式，取值范围：ICharacterEncoding常量
    /** 数据位宽，取值范围：4~9，UART为5~9位，SPI为4~16位，其他协议固定8位 */
    private int bits; // 数据位宽，取值范围：4~9
    /** 是否启用奇偶校验，取值范围：true=启用校验，false=不启用，仅UART使用 */
    private boolean checked; // 是否启用奇偶校验，仅UART使用

    /** 标题区域矩形，用于DST_OUT模式擦除标题栏重叠区域，取值范围：由titleLen决定宽度 */
    private Rect rectTitleLen = new Rect(); // 标题区域矩形，用于DST_OUT模式擦除标题栏重叠区域
    /** DST_OUT混合模式，用于擦除Canvas上标题栏区域的绘制内容 */
    private PorterDuffXfermode desoutMode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT); // DST_OUT混合模式，用于擦除标题栏区域
    /** SRC_OVER混合模式，默认的图像合成模式，新绘制内容覆盖在已有内容之上 */
    private PorterDuffXfermode srcOverMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER); // SRC_OVER混合模式，默认图像合成模式
    /** SRC混合模式，直接绘制源图像，用于绘制解码元素的主体内容 */
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC); // SRC混合模式，直接绘制源图像
    //endregion

    //region 解析数据列表
    /** UART协议解析结果列表，存放解析后的UartStruct结构体 */
    private List<SerialBusStruct.UartStruct> listUart; // UART协议解析结果列表
    /** LIN协议解析结果列表，存放解析后的LinStruct结构体 */
    private List<SerialBusStruct.LinStruct> listLin; // LIN协议解析结果列表
    /** CAN协议解析结果列表，存放解析后的CanStruct结构体 */
    private List<SerialBusStruct.CanStruct> listCan; // CAN协议解析结果列表
    /** SPI协议解析结果列表，存放解析后的SpiStruct结构体 */
    private List<SerialBusStruct.SpiStruct> listSpi; // SPI协议解析结果列表
    /** I2C协议解析结果列表，存放解析后的I2cStruct结构体 */
    private List<SerialBusStruct.I2cStruct> listI2c; // I2C协议解析结果列表
    /** ARINC429协议解析结果列表，存放解析后的Arinc429Struct结构体 */
    private List<SerialBusStruct.Arinc429Struct> list429; // ARINC429协议解析结果列表
    /** MIL-STD-1553B协议解析结果列表，存放解析后的MilSTD1553bStruct结构体 */
    private List<SerialBusStruct.MilSTD1553bStruct> list1553b; // MIL-STD-1553B协议解析结果列表

    /** 获取UART协议解析结果列表，@return UART结构体列表，可能为null */
    public List<SerialBusStruct.UartStruct> getListUart(){return listUart;} // 获取UART解析结果列表
    /** 获取LIN协议解析结果列表，@return LIN结构体列表，可能为null */
    public List<SerialBusStruct.LinStruct>  getListLin(){return listLin;} // 获取LIN解析结果列表
    /** 获取CAN协议解析结果列表，@return CAN结构体列表，可能为null */
    public List<SerialBusStruct.CanStruct>  getListCan(){return listCan;} // 获取CAN解析结果列表
    /** 获取SPI协议解析结果列表，@return SPI结构体列表，可能为null */
    public List<SerialBusStruct.SpiStruct>  getListSpi(){return listSpi;} // 获取SPI解析结果列表
    /** 获取I2C协议解析结果列表，@return I2C结构体列表，可能为null */
    public List<SerialBusStruct.I2cStruct>  getListI2c(){return listI2c;} // 获取I2C解析结果列表
    /** 获取ARINC429协议解析结果列表，@return ARINC429结构体列表，可能为null */
    public List<SerialBusStruct.Arinc429Struct> getList429(){return list429;} // 获取ARINC429解析结果列表
    /** 获取MIL-STD-1553B协议解析结果列表，@return 1553B结构体列表，可能为null */
    public List<SerialBusStruct.MilSTD1553bStruct> getList1553b(){return list1553b;} // 获取1553B解析结果列表
    //endregion


    /**
     * 串口总线解码的统一入口方法，根据总线类型分发到对应的解析和绘制方法。
     * 该方法是外部调用本类的唯一入口，完成"解析→存储→绘制"的完整流程。
     *
     * @param Ch           通道号（如"S1"、"S2"），用于ARINC429格式配置查询
     * @param titleLen     标题栏宽度（像素），用于擦除标题区域重叠的绘制内容
     * @param serialBusType 总线类型，取值范围：SerialBusStruct.SerialBusType常量
     *                     （UART=1, LIN=2, CAN=3, SPI=4, I2C=5, 429=6, 1553B=7）
     * @param canvas       Android画布，用于绘制解码图像
     * @param paint        画笔，控制绘制样式（颜色、字体大小等）
     * @param bytes        FPGA上传的原始ByteBuffer数据，每8字节为一组协议数据包
     * @param timeToPix    时间到像素的转换因子，用于将FPGA时间戳转换为屏幕像素坐标
     * @param encoding     编码方式，取值范围：ICharacterEncoding常量
     *                     （Hex=0x16, Binary=0x02, Octal=0x08, Decimal=0x10, ASCII=0xAC）
     * @param bits         数据位宽，取值范围：4~9，影响数据解析和显示位数
     * @param checked      是否启用奇偶校验，true=启用，false=不启用
     * @param startX       绘制区域起始X坐标（像素）
     * @param endX         绘制区域结束X坐标（像素）
     * 业务意义：UI层触发解码后，通过此方法完成从原始数据到屏幕显示的全流程
     */
    public void toParse(int Ch, int titleLen, int serialBusType, Canvas canvas, Paint paint, final ByteBuffer bytes, long timeToPix, int encoding, int bits, boolean checked, int startX, int endX) { // 串口总线解码统一入口方法
        if(bytes.limit() == bytes.capacity()) return; // 防御性检查：ByteBuffer未填充数据时直接返回，规避IndexOutOfBoundsException（Worktile #2816）
        this.timeToPix = timeToPix; // 保存时间到像素的转换因子，供各协议解析方法使用
        this.encoding = encoding; // 保存编码方式，供数据转换时使用
        this.bits = bits; // 保存数据位宽，供UART/SPI解析时使用
        this.checked = checked; // 保存奇偶校验开关，供UART的9bit数据拼接时使用
        this.startX = startX; // 保存绘制区域起始X坐标
        this.endX = endX; // 保存绘制区域结束X坐标
        switch (serialBusType) { // 根据总线类型分发到对应的解析和绘制流程
            case SerialBusStruct.SerialBusType_UART: { // UART通用异步收发器
//              Tools.beginTime(); // 调试用：计时开始
                List<SerialBusStruct.UartStruct> list = getUartStruct(bytes); // 解析UART二进制数据为结构体列表
                listUart=list; // 保存解析结果到成员变量，供外部getListUart()获取
//              Logger.i(TAG,String.format("解析耗时： %d ms",Tools.endTime())); // 调试用：输出解析耗时
//              Tools.beginTime(); // 调试用：计时开始
                if (encoding == ICharacterEncoding.ASCII) { // ASCII编码模式下使用衬线字体以更好显示字符
                    paint.setTypeface(Typeface.SERIF); // 设置衬线字体
                } // 结束ASCII编码字体设置
                    drawParseUartData(titleLen, canvas, paint, list); // 绘制UART解码图像
                paint.setTypeface(Typeface.MONOSPACE); // 恢复等宽字体，保证后续绘制对齐
//               Logger.i(TAG,String.format("绘制耗时：%d ms",Tools.endTime())); // 调试用：输出绘制耗时

            } // 结束UART case块
            break; // 结束UART分支
            case SerialBusStruct.SerialBusType_LIN: { // LIN局域互联网络
                paint.setTypeface(Typeface.MONOSPACE); // 设置等宽字体，保证数据对齐
                List<SerialBusStruct.LinStruct> list = getLinStruct(bytes); // 解析LIN二进制数据为结构体列表
                listLin=list; // 保存解析结果到成员变量
                drawParseLinData(titleLen, canvas, paint, list); // 绘制LIN解码图像
            } // 结束LIN case块
            break; // 结束LIN分支
            case SerialBusStruct.SerialBusType_CAN: { // CAN控制器局域网
//                bytes.clear(); // 调试用：清空缓冲区
//                byte[] bytess=new byte[]{...}; // 调试用：注入测试数据
//                bytes.put(bytess); // 调试用：填充测试数据
                paint.setTypeface(Typeface.MONOSPACE); // 设置等宽字体
                List<SerialBusStruct.CanStruct> list = getCanStruct(bytes); // 解析CAN二进制数据为结构体列表
                listCan=list; // 保存解析结果到成员变量
                drawParseCanData(titleLen, canvas, paint, list); // 绘制CAN解码图像
            } // 结束CAN case块
            break; // 结束CAN分支
            case SerialBusStruct.SerialBusType_SPI: { // SPI串行外设接口
                paint.setTypeface(Typeface.MONOSPACE); // 设置等宽字体
                List<SerialBusStruct.SpiStruct> list = getSpiStruct(bytes); // 解析SPI二进制数据为结构体列表
                listSpi=list; // 保存解析结果到成员变量
                drawParseSpiData(titleLen, canvas, paint, list); // 绘制SPI解码图像
            } // 结束SPI case块
            break; // 结束SPI分支
            case SerialBusStruct.SerialBusType_I2C: { // I2C内部集成电路总线
                paint.setTypeface(Typeface.MONOSPACE); // 设置等宽字体
                List<SerialBusStruct.I2cStruct> list = getI2cStruct(bytes); // 解析I2C二进制数据为结构体列表
                listI2c=list; // 保存解析结果到成员变量
                drawParseI2cData(titleLen, canvas, paint, list); // 绘制I2C解码图像
            } // 结束I2C case块
            break; // 结束I2C分支
            case SerialBusStruct.SerialBusType_429: { // ARINC429航空电子数字总线
                paint.setTypeface(Typeface.MONOSPACE); // 设置等宽字体
//                int format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT // 旧版：直接用S1/S2判断通道
//                        + (Ch.equals("S1") ? CacheUtil.S1 : CacheUtil.S2)); // 旧版通道判断
                int format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + TChan.toSerialNumber(Ch)); // 从缓存读取ARINC429格式配置，TChan将通道号转为序列号
                List<SerialBusStruct.Arinc429Struct> list = getArinc429Struct(bytes, format); // 解析ARINC429二进制数据，需传入格式类型
                list429=list; // 保存解析结果到成员变量
                drawParseArinc429Data(titleLen, canvas, paint, list); // 绘制ARINC429解码图像
            } // 结束ARINC429 case块
            break; // 结束ARINC429分支
            case SerialBusStruct.SerialBusType_1553B: { // MIL-STD-1553B军用航空数据总线
                paint.setTypeface(Typeface.MONOSPACE); // 设置等宽字体
                List<SerialBusStruct.MilSTD1553bStruct> list = getMilSTD1553bStruct(bytes); // 解析1553B二进制数据为结构体列表
                list1553b=list; // 保存解析结果到成员变量
                drawParseMilSTD1553bData(titleLen, canvas, paint, list); // 绘制1553B解码图像
            } // 结束1553B case块
            break; // 结束1553B分支
        } // 结束switch语句
    } // 结束toParse方法


    //region UART绘制

    /**
     * 解析UART协议的二进制数据为UartStruct结构体列表。
     * 遍历ByteBuffer中每8字节一组的数据包，根据byte[3]的数据类型标识，
     * 将FPGA上传的原始数据解析为UART协议帧（起始位、数据位、校验位、停止位）。
     *
     * UART数据包格式（每8字节）：
     *   byte[0-1]：Reserve保留字段
     *   byte[2]  ：数据值（帧的有效载荷）
     *   byte[3]  ：数据类型标识
     *     - bit7=1：过采样标记
     *     - 0x00：起始帧（DataType_BeginData）
     *     - 低3位：0x02=校验正确无停止位, 0x03=校验错误无停止位
     *     - 低3位：0x06=校验正确有停止位, 0x07=校验错误有停止位
     *     - bit3=1：9bit数据的前8位, bit3=0：9bit数据的第9位
     *   byte[4-7]：时间戳（32位小端序）
     *
     * @param bytes FPGA上传的原始ByteBuffer数据
     * @return 解析后的UartStruct列表，每个元素代表一个UART数据帧段
     * 业务意义：将FPGA采集的UART原始信号解码为可读的数据帧，用于屏幕显示和导出
     */
    private List<SerialBusStruct.UartStruct> getUartStruct(ByteBuffer bytes) { // 解析UART协议二进制数据为UartStruct结构体列表
        final int NoStopColor = Color.YELLOW; // 无停止位的帧颜色：黄色，表示帧未正常结束
        final int StopSuccessColor = App.get().getResources().getColor(R.color.textColor); // 停止位校验成功的帧颜色：主题文字色（灰白色）
        final int StopFailedColor = Color.RED; // 校验失败或停止位错误的帧颜色：红色，表示错误

        int uartLength = bits; // 数据位宽度，由UI界面传入，取值范围：5~9
        long tmp = this.timeToPix; // 时间到像素的转换因子，由UI界面传入
        boolean checked = this.checked; // 是否启用奇偶校验，由UI界面传入
        int encoding = this.encoding; // 编码方式，由UI界面传入
        int temData = 0; // 临时数据变量，用于9bit数据拼接（已弃用，保留兼容）
        List<SerialBusStruct.UartStruct> list = new ArrayList<>(); // 解析结果列表
        SerialBusStruct.UartStruct uartStruct = null; // 当前正在构建的UART结构体，跨数据包拼接
        for (int i = 0; i <= bytes.limit() - 8; i += 8) { // 遍历ByteBuffer，每8字节为一组数据包
            //过采样处理
            if ((bytes.get(i + 3) & 0x80) == 0x80) { // byte[3]的bit7=1表示过采样标记
                uartStruct = SerialBusStruct.getInstance().new UartStruct(); // 创建新的UART结构体
                uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接byte[4-7]为32位小端序时间戳
                uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp); // 将FPGA时间戳转换为像素坐标
                uartStruct.BeginX = uartStruct.EndX; // 过采样标记的起始和结束位置相同
                uartStruct.Data = "?"; // 过采样标记显示问号
                uartStruct.isBeginFrame=true; // 标记为帧起始
                list.add(uartStruct); // 将过采样标记加入结果列表
                continue; // 跳过当前数据包
            } // 结束过采样处理块

            if (bytes.get(i + 3) == SerialBusStruct.DataType_BeginData) { // byte[3]=0x00表示起始帧
//                if (uartStruct != null && uartStruct.DataType == SerialBusStruct.DataType_BeginData) {
//                    uartStruct.EndX = uartStruct.BeginX;
//                    uartStruct.Data = "?";
//                    list.add(uartStruct);
//                }
                uartStruct = SerialBusStruct.getInstance().new UartStruct(); // 创建新的UART结构体用于起始帧
                uartStruct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接byte[4-7]为32位小端序时间戳作为起始位置
                uartStruct.BeginX = (int) (uartStruct.BeginX * Cycle / tmp); // 将FPGA时间戳转换为像素坐标
                uartStruct.DataType = bytes.get(i + 3); // 保存数据类型标识
                uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0)); // 拼接byte[0-1]保留字段
                uartStruct.DataColor = StopFailedColor; // 默认设为校验失败颜色（红色），后续根据实际校验结果更新
                uartStruct.isBeginFrame=true; // 标记为帧起始
                continue; // 起始帧只记录位置，等待后续数据帧拼接
            } // 结束DataType_BeginData起始帧处理块
            switch (bytes.get(i + 3) & 0x07) { // 取byte[3]低3位判断帧类型（校验+停止位状态）
                case SerialBusStruct.UartStruct.DataType_CRCFailed_noStop: // 校验失败且无停止位
                case SerialBusStruct.UartStruct.DataType_CRCSucc_NoStop: { // 校验成功但无停止位
                    if (uartStruct == null) { // 防御性检查：若无起始帧则创建默认结构体
                        uartStruct = SerialBusStruct.getInstance().new UartStruct(); // 创建新的UART结构体
                        uartStruct.BeginX = 0; // 起始位置设为0
                        uartStruct.isBeginFrame=false; // 非帧起始
                    }
                    uartStruct.DataType=(byte)(bytes.get(i + 3) & 0x0F); // 保存低4位数据类型（含bit3的9bit标志）

                    if (uartLength <= 8) { // 数据位宽≤8位，无需9bit拼接，直接解析
//                        temData=bytes.get(i+2) & mask;
                        uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接byte[4-7]为32位小端序时间戳作为结束位置
                        uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp); // 将FPGA时间戳转换为像素坐标
                        uartStruct.DataType = (byte)(bytes.get(i + 3) & 0x07); // 保存低3位数据类型
                        int len = checked ? uartLength : uartLength - 1; // 计算显示位数：有校验则显示全部位，无校验减1（去掉校验位）
                        uartStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, len); // 将byte[2]数据值按编码方式转换
                        uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0)); // 拼接byte[0-1]保留字段
                        uartStruct.DataColor = ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_noStop) ? StopFailedColor : NoStopColor; // 校验失败=红色，无停止位=黄色
                        list.add(uartStruct); // 将完整的UART结构体加入结果列表
                    } else { // 数据位宽>8位（9bit模式），需要特殊处理
                        //奇偶校验，不进行拼成9bit
                        if (checked != true) { // 无奇偶校验时，9bit数据不拼接，直接显示低8位
//                            temData=bytes.get(i+2) & mask;
                            uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接byte[4-7]为32位小端序时间戳作为结束位置
                            uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp); // 将FPGA时间戳转换为像素坐标
                            uartStruct.DataType = (byte)(bytes.get(i + 3) & 0x07); // 保存低3位数据类型
                            uartStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, uartLength > 8 ? 8 : uartLength); // 显示低8位数据（9bit时截断为8位显示）
//                            uartStruct.Data=getEncoding(encoding,temData,uartLength>8?8:uartLength);
                            uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0)); // 拼接byte[0-1]保留字段
                            uartStruct.DataColor = ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_noStop) ? StopFailedColor : NoStopColor; // 校验失败=红色，无停止位=黄色
                            ; // 空语句（遗留代码，无实际作用）
                            list.add(uartStruct); // 将UART结构体加入结果列表
                        } else { // 有奇偶校验时，9bit数据需要两次8字节包拼接
                            if ((uartStruct.DataType & SerialBusStruct.UartStruct.DataType_3) == SerialBusStruct.UartStruct.DataType_3) { // bit3=1表示9bit数据的前8位（第一个包）
                                //拼字节
                                uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接byte[4-7]为32位小端序时间戳作为结束位置
                                uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp); // 将FPGA时间戳转换为像素坐标
                                uartStruct.DataType = (byte)(bytes.get(i + 3) & 0x07); // 保存低3位数据类型
                                uartStruct.Id = 0x000000FF & bytes.get(i + 2); // 取byte[2]作为9bit数据的低8位
                                uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0)); // 拼接byte[0-1]保留字段
                                uartStruct.DataColor = ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_noStop) ? StopFailedColor : NoStopColor; // 校验失败=红色，无停止位=黄色
                                ; // 空语句（遗留代码，无实际作用）

                            } else { // bit3=0表示9bit数据的第9位（第二个包），此时拼接完成
                                uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接byte[4-7]为32位小端序时间戳作为结束位置
                                uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp); // 将FPGA时间戳转换为像素坐标
                                uartStruct.DataType = (byte)(bytes.get(i + 3) & 0x07); // 保存低3位数据类型
                                uartStruct.Id |= 0x00000100 & (bytes.get(i + 2) << 8); // 将byte[2]左移8位后拼接到Id的第9位，完成9bit数据拼接
                                //uartStruct.Id = 0x000000FF & bytes.get(i + 2);
                                uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0)); // 拼接byte[0-1]保留字段
                                uartStruct.DataColor = ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_noStop) ? StopFailedColor : NoStopColor; // 校验失败=红色，无停止位=黄色
                                ; // 空语句（遗留代码，无实际作用）

                                uartStruct.Data = getEncoding(encoding, uartStruct.Id, uartLength); // 将拼接后的9bit完整数据按编码方式转换
                                if (uartStruct.BeginX < 0) { // 防御性检查：起始位置为负值时数据显示问号
                                    uartStruct.Data = "?"; // 起始位置异常，显示问号
                                }
                                list.add(uartStruct); // 9bit拼接完成，将UART结构体加入结果列表
                            }
                        }
                    }
                }
                break; // 结束无停止位分支处理
                case SerialBusStruct.UartStruct.DataType_CRCFailed_Stop: // 校验失败且有停止位
                case SerialBusStruct.UartStruct.DataType_CRCSucc_Stop: { // 校验成功且有停止位
                    //有停止位，不进行拼字节，停止位就是第9位。
                    if (uartStruct == null) { // 防御性检查：若无起始帧则创建默认结构体
                        uartStruct = SerialBusStruct.getInstance().new UartStruct(); // 创建新的UART结构体
                        uartStruct.BeginX = 0; // 起始位置设为0
                        uartStruct.isBeginFrame=false; // 非帧起始
                    }
                    uartStruct.DataType=(byte)(bytes.get(i + 3) & 0x07); // 保存低3位数据类型

                    if (uartLength <= 8) { // 数据位宽≤8位，无需9bit拼接，直接解析
                        uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接byte[4-7]为32位小端序时间戳作为结束位置
                        uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp); // 将FPGA时间戳转换为像素坐标
                        uartStruct.DataType = (byte)(bytes.get(i + 3) & 0x07); // 保存低3位数据类型
                        int len = checked ? uartLength : uartLength - 1; // 计算显示位数：有校验则显示全部位，无校验减1（去掉校验位）
                        uartStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, len); // 将byte[2]数据值按编码方式转换
                        uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0)); // 拼接byte[0-1]保留字段
                        if ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_Stop) { // 判断校验结果设置颜色
                            uartStruct.DataColor = StopFailedColor; // 校验失败=红色
                        } else {
                            uartStruct.DataColor = StopSuccessColor; // 校验成功=主题文字色（灰白色）
                        }
                        list.add(uartStruct); // 将完整的UART结构体加入结果列表
                    } else { // 数据位宽>8位（9bit模式），需要特殊处理

                        //奇偶校验，不拼字节
                        if (checked != true) { // 无奇偶校验时，9bit数据不拼接，直接显示低8位
                            uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接byte[4-7]为32位小端序时间戳作为结束位置
                            uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp); // 将FPGA时间戳转换为像素坐标
                            uartStruct.DataType =(byte)( bytes.get(i + 3) & 0x07); // 保存低3位数据类型
                            uartStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, uartLength > 8 ? 8 : uartLength); // 显示低8位数据（9bit时截断为8位显示）
                            uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0)); // 拼接byte[0-1]保留字段
                            if ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_Stop) { // 判断校验结果设置颜色
                                uartStruct.DataColor = StopFailedColor; // 校验失败=红色
                            } else {
                                uartStruct.DataColor = StopSuccessColor; // 校验成功=主题文字色（灰白色）
                            }
                            list.add(uartStruct); // 将UART结构体加入结果列表

                        } else { // 有奇偶校验时，9bit数据需要两次8字节包拼接
                            if ((uartStruct.DataType & SerialBusStruct.UartStruct.DataType_3) == SerialBusStruct.UartStruct.DataType_3) { // bit3=1表示9bit数据的前8位（第一个包）
                                //拼字节
                                uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接byte[4-7]为32位小端序时间戳作为结束位置
                                uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp); // 将FPGA时间戳转换为像素坐标
                                uartStruct.DataType = (byte)(bytes.get(i + 3) & 0x07); // 保存低3位数据类型
                                uartStruct.Id = 0x000000FF & bytes.get(i + 2); // 取byte[2]作为9bit数据的低8位
                                uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0)); // 拼接byte[0-1]保留字段
                                uartStruct.DataColor = ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_Stop) ? StopFailedColor : StopSuccessColor; // 校验失败=红色，校验成功=灰白色
                                //String.format("%03x", uartStruct.Id).toUpperCase();

                            } else { // bit3=0表示9bit数据的第9位（第二个包），此时拼接完成
                                uartStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接byte[4-7]为32位小端序时间戳作为结束位置
                                uartStruct.EndX = (int) (uartStruct.EndX * Cycle / tmp); // 将FPGA时间戳转换为像素坐标
                                uartStruct.DataType = (byte)(bytes.get(i + 3) & 0x07); // 保存低3位数据类型
                                uartStruct.Id |= 0x00000100 & (bytes.get(i + 2) << 8); // 将byte[2]左移8位后拼接到Id的第9位，完成9bit数据拼接
                                //uartStruct.Id = 0x000000FF & bytes.get(i + 2);
                                uartStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0)); // 拼接byte[0-1]保留字段
                                uartStruct.DataColor = ((bytes.get(i + 3) & 0x07) == SerialBusStruct.UartStruct.DataType_CRCFailed_Stop) ? StopFailedColor : StopSuccessColor; // 校验失败=红色，校验成功=灰白色
                                ; // 空语句（遗留代码，无实际作用）
                                uartStruct.Data = getEncoding(encoding, uartStruct.Id, uartLength); // 将拼接后的9bit完整数据按编码方式转换
                                if (uartStruct.BeginX < 0) { // 防御性检查：起始位置为负值时数据显示问号
                                    uartStruct.Data = "?"; // 起始位置异常，显示问号
                                }
                                list.add(uartStruct); // 9bit拼接完成，将UART结构体加入结果列表
                            }
                        }
                    }
                }
                break; // 结束有停止位分支处理

            } // 结束switch语句

        } // 结束for循环，遍历完所有8字节数据包

        //将最后一个添加上
        if (list.size() > 0 && uartStruct != null) // 列表非空且当前结构体存在
            if (uartStruct.hashCode() != list.get(list.size() - 1).hashCode()) { // 当前结构体尚未加入列表（hashCode不同说明是不同对象）
                uartStruct.Data = "?"; // 未完成帧显示问号
//                uartStruct.EndX = GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_YT) + 12;
                uartStruct.EndX = (this.endX - this.startX); // 结束位置设为绘制区域宽度
                list.add(uartStruct); // 将最后一个未完成帧加入结果列表
            }

        return list; // 返回解析后的UART结构体列表
    }

    /**
     * 绘制UART解码图像。
     * 先绘制中间基线，再逐个绘制解码元素（菱形+文字），最后擦除标题栏区域。
     *
     * @param titleLen 标题栏宽度（像素），用于擦除标题区域重叠的绘制内容
     * @param canvas   Android画布，用于绘制解码图像
     * @param paint    画笔，控制绘制样式
     * @param list     UART结构体列表，由getUartStruct解析得到
     * 业务意义：将解析后的UART数据帧以图形方式呈现在示波器屏幕上
     */
    private  void drawParseUartData(int titleLen, Canvas canvas, Paint paint, List<SerialBusStruct.UartStruct> list) { // 绘制UART解码图像
        paint.setXfermode(srcMode); // 设置SRC混合模式，直接绘制源图像
        canvas.drawLine(this.startX, canvas.getHeight() / 2, this.endX, canvas.getHeight() / 2, paint); // 绘制中间水平基线
        for (int i = 0; i < list.size(); i++) { // 遍历所有UART结构体
            if (i != 0 && list.get(i).BeginX == list.get(i - 1).BeginX) { // 跳过起始位置相同的重复帧（过采样标记）
                continue; // 跳过重复帧
            }
            drawElement(canvas, paint, list.get(i).BeginX + this.startX, list.get(i).EndX - list.get(i).BeginX, list.get(i).Data, list.get(i).DataColor,list.get(i).isBeginFrame); // 绘制单个解码元素：起始X+偏移，宽度=EndX-BeginX，数据值，颜色，是否帧起始
        }
        paint.setXfermode(desoutMode); // 切换到DST_OUT模式，用于擦除标题区域
        rectTitleLen.set(0, 0, titleLen, canvas.getHeight()); // 设置标题区域矩形
        canvas.drawRect(rectTitleLen, paint); // 擦除标题栏区域的绘制内容
    }


    //endregion

    //region Lin绘制

    /**
     * 解析LIN协议的二进制数据为LinStruct结构体列表。
     * LIN（Local Interconnect Network）协议帧结构：同步间隔→同步字段→标识符→数据→校验和
     *
     * @param bytes FPGA上传的原始ByteBuffer数据
     * @return 解析后的LinStruct列表，每个元素代表LIN协议的一个字段段
     * 业务意义：将FPGA采集的LIN总线信号解码为可读的协议帧字段
     */
    private List<SerialBusStruct.LinStruct> getLinStruct(ByteBuffer bytes) { // 解析LIN协议二进制数据为LinStruct结构体列表
        long tmp = this.timeToPix; // 时间到像素的转换因子
        int encoding = ICharacterEncoding.Hex; // LIN固定使用十六进制编码
        int bitWidth = 2 * 4; // 显示位宽=8位（2个十六进制字符×4位）

        List<SerialBusStruct.LinStruct> list = new ArrayList<>(); // LIN解析结果列表
        SerialBusStruct.LinStruct linStruct = null; // 当前正在构建的LIN结构体

        for (int i = 0; i <= bytes.limit() - 8; i += 8) { // 遍历ByteBuffer，每8字节为一组数据包
            if ((bytes.get(i + 3) & 0x80) == 0x80) { // 判断是否为过采样数据（最高位为1）
                linStruct = SerialBusStruct.getInstance().new LinStruct(); // 创建新的LIN结构体实例
                linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                linStruct.BeginX = linStruct.EndX; // 起始X等于结束X（单点帧）
                linStruct.Data = "?"; // 数据标记为未知
                linStruct.isBeginFrame=true; // 标记为起始帧
                list.add(linStruct); // 将结构体添加到列表
                continue; // 跳过后续处理，继续下一次循环
            } // 结束过采样数据处理块
            if (bytes.get(i + 3) == SerialBusStruct.DataType_BeginData) { // 判断是否为起始数据帧
//                if (linStruct != null && linStruct.DataType == SerialBusStruct.DataType_BeginData) {
//                    linStruct.EndX = linStruct.BeginX;
//                    linStruct.Data = "?";
//                    list.add(linStruct);
//                }
                linStruct = SerialBusStruct.getInstance().new LinStruct(); // 创建新的LIN结构体实例
                linStruct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析起始X坐标（4字节拼接）
                linStruct.BeginX = (int) (linStruct.BeginX * Cycle / tmp); // 将起始X坐标转换为像素坐标
                linStruct.DataType = bytes.get(i + 3); // 设置数据类型为起始数据帧
                linStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0)); // 解析保留字段
                linStruct.DataColor = Color.RED; // 设置数据颜色为红色（起始帧）
                linStruct.isBeginFrame=true; // 标记为起始帧
                continue; // 跳过后续处理，继续下一次循环
            } // 结束起始数据帧处理块
            switch (bytes.get(i + 3) & 0x0F) { // 根据数据类型低4位进行switch分支

                case SerialBusStruct.LinStruct.ID_ODD_Sucess: // ID校验成功
                    if (linStruct == null) { // 如果当前没有LIN结构体
                        linStruct = SerialBusStruct.getInstance().new LinStruct(); // 创建新的LIN结构体实例
                        linStruct.BeginX = 0; // 起始X坐标设为0
                        linStruct.isBeginFrame=false; // 标记为非起始帧
                    } // 结束linStruct空判断
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                    linStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为ID校验成功
                    linStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, bitWidth);  //Integer.toHexString(bytes.get(i + 2));
                    linStruct.DataColor = Color.YELLOW; // 设置数据颜色为黄色
                    list.add(linStruct); // 将结构体添加到列表
                    break; // 跳出ID_ODD_Sucess分支
                case SerialBusStruct.LinStruct.ID_ODD_Failed: // ID校验失败
                    if (linStruct == null) { // 如果当前没有LIN结构体
                        linStruct = SerialBusStruct.getInstance().new LinStruct(); // 创建新的LIN结构体实例
                        linStruct.BeginX = 0; // 起始X坐标设为0
                        linStruct.isBeginFrame=false; // 标记为非起始帧
                    } // 结束linStruct空判断
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                    linStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为ID校验失败
                    linStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, bitWidth); //Integer.toHexString(bytes.get(i + 2));
                    linStruct.DataColor = Color.RED; // 设置数据颜色为红色（失败）
                    list.add(linStruct); // 将结构体添加到列表
                    break; // 跳出ID_ODD_Failed分支
                case SerialBusStruct.LinStruct.Data_Stop: // 数据正常停止
                    if (linStruct == null) { // 如果当前没有LIN结构体
                        linStruct = SerialBusStruct.getInstance().new LinStruct(); // 创建新的LIN结构体实例
                        linStruct.BeginX = 0; // 起始X坐标设为0
                        linStruct.isBeginFrame=false; // 标记为非起始帧
                    } // 结束linStruct空判断
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                    linStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为数据正常停止
                    linStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, bitWidth); //Integer.toHexString(bytes.get(i + 2));
                    linStruct.DataColor =App.get().getResources().getColor(R.color.textColor); //Color.WHITE;
                    list.add(linStruct); // 将结构体添加到列表
                    break; // 跳出Data_Stop分支
                case SerialBusStruct.LinStruct.Data_NoStop: // 数据无停止位
                    if (linStruct == null) { // 如果当前没有LIN结构体
                        linStruct = SerialBusStruct.getInstance().new LinStruct(); // 创建新的LIN结构体实例
                        linStruct.BeginX = 0; // 起始X坐标设为0
                        linStruct.isBeginFrame=false; // 标记为非起始帧
                    } // 结束linStruct空判断
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                    linStruct.DataType = bytes.get(i + 3) &0x0F; // 设置数据类型为数据无停止位
                    linStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, bitWidth); //Integer.toHexString(bytes.get(i + 2));
                    linStruct.DataColor = Color.RED; // 设置数据颜色为红色（错误）
                    list.add(linStruct); // 将结构体添加到列表
                    break; // 跳出Data_NoStop分支
                case SerialBusStruct.LinStruct.Check_Success: // 校验和成功
                    if (linStruct == null) { // 如果当前没有LIN结构体
                        linStruct = SerialBusStruct.getInstance().new LinStruct(); // 创建新的LIN结构体实例
                        linStruct.BeginX = 0; // 起始X坐标设为0
                        linStruct.isBeginFrame=false; // 标记为非起始帧
                    } // 结束linStruct空判断
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                    linStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为校验和成功
                    linStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, bitWidth); //Integer.toHexString(bytes.get(i + 2));
                    linStruct.DataColor = Color.GREEN; //Color.rgb(250, 128, 10);
                    list.add(linStruct); // 将结构体添加到列表
                    break; // 跳出Check_Success分支
                case SerialBusStruct.LinStruct.Check_Failed: // 校验和失败
                    if (linStruct == null) { // 如果当前没有LIN结构体
                        linStruct = SerialBusStruct.getInstance().new LinStruct(); // 创建新的LIN结构体实例
                        linStruct.BeginX = 0; // 起始X坐标设为0
                        linStruct.isBeginFrame=false; // 标记为非起始帧
                    } // 结束linStruct空判断
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                    linStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为校验和失败
                    linStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, bitWidth); //Integer.toHexString(bytes.get(i + 2));
                    linStruct.DataColor = Color.RED; // 设置数据颜色为红色（失败）
                    list.add(linStruct); // 将结构体添加到列表
                    break; // 跳出Check_Failed分支
                case SerialBusStruct.LinStruct.SYNC_Success: // 同步字段成功
                    if (linStruct == null) { // 如果当前没有LIN结构体
                        linStruct = SerialBusStruct.getInstance().new LinStruct(); // 创建新的LIN结构体实例
                        linStruct.BeginX = 0; // 起始X坐标设为0
                        linStruct.isBeginFrame=false; // 标记为非起始帧
                    } // 结束linStruct空判断

                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                    linStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为同步字段成功
                    linStruct.Data = "SYNC"; // 数据显示为"SYNC"
                    linStruct.DataColor = Color.YELLOW; // 设置数据颜色为黄色
                    list.add(linStruct); // 将结构体添加到列表
                    break; // 跳出SYNC_Success分支
                case SerialBusStruct.LinStruct.SYNC_Failed: // 同步字段失败
                    if (linStruct == null) { // 如果当前没有LIN结构体
                        linStruct = SerialBusStruct.getInstance().new LinStruct(); // 创建新的LIN结构体实例
                        linStruct.BeginX = 0; // 起始X坐标设为0
                        linStruct.isBeginFrame=false; // 标记为非起始帧
                    } // 结束linStruct空判断
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                    linStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为同步字段失败
                    linStruct.Data = "SYNC"; // 数据显示为"SYNC"
                    linStruct.DataColor = Color.RED; // 设置数据颜色为红色（失败）
                    list.add(linStruct); // 将结构体添加到列表
                    break; // 跳出SYNC_Failed分支
                case SerialBusStruct.LinStruct.Wake_Success: // 唤醒信号成功
                    if (linStruct == null) { // 如果当前没有LIN结构体
                        linStruct = SerialBusStruct.getInstance().new LinStruct(); // 创建新的LIN结构体实例
                        linStruct.BeginX = 0; // 起始X坐标设为0
                        linStruct.isBeginFrame=false; // 标记为非起始帧
                    } // 结束linStruct空判断
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                    linStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为唤醒信号成功
                    linStruct.Data = "WAKE"; // 数据显示为"WAKE"
                    linStruct.DataColor = Color.BLUE; // 设置数据颜色为蓝色
                    list.add(linStruct); // 将结构体添加到列表
                    break; // 跳出Wake_Success分支
                case SerialBusStruct.LinStruct.Wake_Failed: // 唤醒信号失败
                    if (linStruct == null) { // 如果当前没有LIN结构体
                        linStruct = SerialBusStruct.getInstance().new LinStruct(); // 创建新的LIN结构体实例
                        linStruct.BeginX = 0; // 起始X坐标设为0
                        linStruct.isBeginFrame=false; // 标记为非起始帧
                    } // 结束linStruct空判断
                    linStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                    linStruct.EndX = (int) (linStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                    linStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为唤醒信号失败
                    linStruct.Data = "WUP"; // 数据显示为"WUP"
                    linStruct.DataColor = Color.RED; // 设置数据颜色为红色（失败）
                    list.add(linStruct); // 将结构体添加到列表
                    break; // 跳出Wake_Failed分支
                case SerialBusStruct.LinStruct.SYNC_Distance: // 同步间隔
//                    Logger.i(TAG,"同步间隔上升沿 bytes:"+SerialBusTxtStructParse.getDebugBytesToString(i,bytes));
                    if (linStruct!=null && linStruct.DataType==SerialBusStruct.DataType_BeginData){ // 如果当前结构体为起始数据帧类型
                        linStruct.Data="SYNC_Distance"; // 设置数据显示为"SYNC_Distance"
                    } // 结束同步间隔处理
                    break; // 跳出SYNC_Distance分支

            } // 结束switch分支
        } // 结束for循环

        //将最后一个添加上
        if (list.size() > 0 && linStruct != null) // 如果列表非空且当前结构体不为空
            if (linStruct.hashCode() != list.get(list.size() - 1).hashCode() && !"SYNC_Distance".equals(linStruct.Data)) { // 如果当前结构体与列表最后一个不同且不是同步间隔
                linStruct.Data = "?"; // 数据标记为未知
                linStruct.EndX = this.endX - this.startX; // 结束X坐标设为显示区域宽度
                list.add(linStruct); // 将最后一个结构体添加到列表
//                Logger.i(TAG,"type:"+linStruct.DataType+"  data:"+linStruct.Data);
            } // 结束最后一个结构体处理

        return list; // 返回LIN结构体列表
    } // 结束getLinStruct方法

    /**
     * 绘制LIN协议解码图像。
     * 先绘制中间基线，再逐个绘制解码元素，最后擦除标题栏区域。
     *
     * @param titleLen 标题栏宽度（像素）
     * @param canvas   Android画布
     * @param paint    画笔
     * @param list     LIN结构体列表，由getLinStruct解析得到
     * 业务意义：将LIN协议的各字段（同步间隔、同步、ID、数据、校验和）以图形方式呈现
     */
    private void drawParseLinData(int titleLen, Canvas canvas, Paint paint, List<SerialBusStruct.LinStruct> list) { // 绘制LIN协议解码图像
        paint.setXfermode(srcMode); // 设置画笔混合模式为源模式
        canvas.drawLine(this.startX, canvas.getHeight() / 2, this.endX, canvas.getHeight() / 2, paint); // 绘制中间基线
        for (int i = 0; i < list.size(); i++) { // 遍历所有LIN结构体
//            //如果开始的时候不是从头开始的，以“-”线补满
//            if (i == 0 && list.get(i).BeginX > 0) {
//                drawElement(canvas, paint, 0, list.get(i).BeginX, null, list.get(i).DataColor);
//            }
//            //如果结束的时候没有到头，以“-”线补满
//            if (i == list.size() - 1 && list.get(i).EndX < canvas.getWidth()) {
//                drawElement(canvas, paint, list.get(i).EndX, canvas.getWidth() - list.get(i).EndX, null, list.get(i).DataColor);
//            }
//            //如果在中间的帧，以“-”线补满
//            if (i > 0 && i < list.size()) {
//                drawElement(canvas, paint, list.get(i - 1).EndX, list.get(i).BeginX - list.get(i - 1).EndX, null, 0);
//            }

            drawElement(canvas, paint, list.get(i).BeginX + this.startX, list.get(i).EndX - list.get(i).BeginX, list.get(i).Data, list.get(i).DataColor,list.get(i).isBeginFrame); // 绘制单个LIN协议元素
        } // 结束遍历
        paint.setXfermode(desoutMode); // 设置画笔混合模式为擦除模式
        rectTitleLen.set(0, 0, titleLen, canvas.getHeight()); // 设置标题栏擦除区域
        canvas.drawRect(rectTitleLen, paint); // 擦除标题栏区域
    } // 结束drawParseLinData方法

    //endregion

    //region Can绘制

    /**
     * 解析CAN协议的二进制数据为CanStruct结构体列表。
     * CAN（Controller Area Network）协议帧结构：
     * 标准帧：起始→标准ID(11bit)→RTR→IDE→r0→DLC→数据→CRC→ACK→EOF
     * 扩展帧：起始→标准ID(11bit)→SRR→IDE→扩展ID(18bit)→RTR→r1→r0→DLC→数据→CRC→ACK→EOF
     *
     * @param bytes FPGA上传的原始ByteBuffer数据
     * @return 解析后的CanStruct列表，每个元素代表CAN协议的一个字段段
     * 业务意义：将FPGA采集的CAN总线信号解码为可读的协议帧字段
     */
    private List<SerialBusStruct.CanStruct> getCanStruct(ByteBuffer bytes) { // 解析CAN协议的二进制数据为CanStruct结构体列表
        final int IDcolor = Color.YELLOW; // ID字段颜色：黄色
        final int DLCcolor = Color.GREEN; // DLC字段颜色：绿色
        final int DataColor = App.get().getResources().getColor(R.color.textColor);//Color.WHITE; // 数据字段颜色：文本色
        final int CrcColor = Color.GREEN; // CRC字段颜色：绿色
        final int ErrorColor = Color.RED; // 错误字段颜色：红色
        final int OverLoadColor = Color.BLUE; // 过载字段颜色：蓝色
        final int ProblemColor = Color.RED; // 问题字段颜色：红色

        long tmp = this.timeToPix; //1000000l;  //时间转像素
        int encoding = ICharacterEncoding.Hex;  //也是界面传进来的
        int bitWidth = 8; // 位宽度设为8

        List<SerialBusStruct.CanStruct> list = new ArrayList<>(); // 创建CAN结构体列表
        int idShowUpTimes = 0;  //id出现的次数
        SerialBusStruct.CanStruct canStruct = null; // 当前CAN结构体引用
//        Logger.i(TAG,"timeToPix:"+timeToPix);
//Logger.i(TAG,"bytes:"+SerialBusTxtStructParse.getDebugBytesToString(bytes));
        for (int i = 0; i <= bytes.limit() - 8; i += 8) { // 每8字节为一组遍历字节流
            if ((bytes.get(i + 3) & 0x80) == 0x80) { // 判断是否为过采样数据（最高位为1）
                canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体实例
                canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                canStruct.BeginX = canStruct.EndX; // 起始X等于结束X（单点帧）
                canStruct.Data = "?"; // 数据标记为未知
                canStruct.isBeginFrame=true; // 标记为起始帧
                list.add(canStruct); // 将结构体添加到列表
                continue; // 跳过后续处理，继续下一次循环
            } // 结束过采样数据处理块
            if (bytes.get(i+3)==SerialBusStruct.DataType_BeginData){ // 判断是否为起始数据帧
//                if (canStruct!=null && canStruct.DataType==SerialBusStruct.DataType_BeginData){
//                    canStruct.EndX=canStruct.BeginX;
//                    canStruct.Data="?";
//                    list.add(canStruct);
//                }
                canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体实例
                canStruct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析起始X坐标（4字节拼接）
                canStruct.BeginX = (int) (canStruct.BeginX * Cycle / tmp); // 将起始X坐标转换为像素坐标
                canStruct.DataType = bytes.get(i + 3) ; // 设置数据类型为起始数据帧
                canStruct.DataColor = ProblemColor; // 设置数据颜色为红色（问题帧）
                canStruct.Reserve = (short) (bytes.get(i + 1) | bytes.get(i + 0)); // 解析保留字段
                canStruct.isBeginFrame=true; // 标记为起始帧
                idShowUpTimes = 0; // 重置ID出现次数
                continue; // 跳过后续处理，继续下一次循环
            } // 结束起始数据帧处理块
            switch (bytes.get(i + 3) & 0x0F) { // 根据数据类型低4位进行switch分支
                case SerialBusStruct.CanStruct.StandardID: { // 标准ID（11位）
                    if (canStruct == null) { // 如果当前没有CAN结构体
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体实例
                        canStruct.BeginX = 0; // 起始X坐标设为0
                        canStruct.Data = "?"; // 数据标记为未知
                        canStruct.DataColor = IDcolor; // 设置数据颜色为ID颜色（黄色）
                        canStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为标准ID
                        canStruct.ID = 0x000000FF & bytes.get(i + 2); // 解析标准ID值
                        canStruct.isBeginFrame=false; // 标记为非起始帧
                        idShowUpTimes = 1; // ID出现次数设为1
                        continue; // 跳过后续处理，继续下一次循环
                    } // 结束canStruct空判断
                    //标准ID最多出现2次
                    if ((canStruct.DataType & 0x0F) == SerialBusStruct.DataType_BeginData) { // 如果当前是起始数据帧类型
                        canStruct.DataType = bytes.get(i + 3) & 0x0F; // 更新数据类型为标准ID
                        canStruct.ID = 0x000000FF & bytes.get(i + 2); // 解析标准ID值
                        idShowUpTimes++; // ID出现次数加1
                    } else { // 否则，标准ID第二次出现
                        canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                        canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                        canStruct.DataType = bytes.get(i + 3) & 0x0F; // 更新数据类型为标准ID
                        if (!canStruct.Data.equals("?") && canStruct.isBeginFrame) // 如果数据不为"?"且是起始帧
                            canStruct.Data = getEncoding(encoding, canStruct.ID << 3 | (0x07 & bytes.get(i + 2)), 3 * 4); // 编码显示标准ID数据
                        canStruct.DataColor = IDcolor; // 设置数据颜色为ID颜色（黄色）
                        idShowUpTimes = 0; // 重置ID出现次数
                        list.add(canStruct); // 将结构体添加到列表
//                        debugCan(bytes,i,"StandardID",canStruct,list);
                    } // 结束标准ID第二次出现处理
                } // 结束StandardID case块
                break; // 跳出StandardID分支
                case SerialBusStruct.CanStruct.ExtendId: { // 扩展ID（29位）
                    if (canStruct == null) { // 如果当前没有CAN结构体
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体实例
                        canStruct.BeginX = 0; // 起始X坐标设为0
                        canStruct.Data = "?"; // 数据标记为未知
                        canStruct.DataColor = IDcolor; // 设置数据颜色为ID颜色（黄色）
                        canStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为扩展ID
                        canStruct.ID = 0x000000FF & bytes.get(i + 2); // 解析扩展ID第一字节
                        canStruct.isBeginFrame=false; // 标记为非起始帧
                        idShowUpTimes = 1; // ID出现次数设为1
                        continue; // 跳过后续处理，继续下一次循环
                    } // 结束canStruct空判断
                    switch (idShowUpTimes) { // 根据ID出现次数进行分支
                        case 0: // ID出现0次
                            //这个进不来，因为第一帧是标准帧
                            break; // 跳出case 0
                        case 1: // ID出现1次，拼接第二字节
                            canStruct.ID = (canStruct.ID << 8 | (bytes.get(i + 2) & 0x000000FF)); // 将ID左移8位并拼接当前字节
                            canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                            canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                            canStruct.DataType = bytes.get(i + 3) & 0x0F; // 更新数据类型为扩展ID
                            canStruct.DataColor = IDcolor; // 设置数据颜色为ID颜色（黄色）
                            idShowUpTimes++; // ID出现次数加1
                            break; // 跳出case 1
                        case 2: // ID出现2次，拼接第三字节
                            canStruct.ID = (canStruct.ID << 8 | (bytes.get(i + 2)& 0x000000FF)); // 将ID左移8位并拼接当前字节
                            canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                            canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                            canStruct.DataType = bytes.get(i + 3) & 0x0F; // 更新数据类型为扩展ID
                            canStruct.DataColor = IDcolor; // 设置数据颜色为ID颜色（黄色）
                            idShowUpTimes++; // ID出现次数加1
                            break; // 跳出case 2
                        case 3: // ID出现3次，拼接第四字节并完成扩展ID解析
                            canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                            canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                            if (!canStruct.Data.equals("?") && canStruct.isBeginFrame) // 如果数据不为"?"且是起始帧
                                canStruct.Data = getEncoding(encoding, (((canStruct.ID << 5) & 0xFFFFFFE0) | (bytes.get(i + 2) & 0x1F)), 8 * 4); // 编码显示扩展ID数据（29位）
                            canStruct.DataType = bytes.get(i + 3) & 0x0F; // 更新数据类型为扩展ID
                            canStruct.DataColor = IDcolor; // 设置数据颜色为ID颜色（黄色）
                            idShowUpTimes = 0; // 重置ID出现次数
                            list.add(canStruct); // 将结构体添加到列表
//                            debugCan(bytes,i,"ExtendId",canStruct,list);
                            break; // 跳出case 3
                    } // 结束idShowUpTimes switch
                } // 结束ExtendId case块
                break; // 跳出ExtendId分支
                case SerialBusStruct.CanStruct.DLC: { // 数据长度码（DLC）
                    if (canStruct == null) { // 如果当前没有CAN结构体
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体实例
                        canStruct.BeginX = 0; // 起始X坐标设为0
                        canStruct.Data="?"; // 数据标记为未知
                        canStruct.isBeginFrame=false; // 标记为非起始帧
                    } // 结束canStruct空判断
                    canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                    canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
//                    if (!canStruct.Data.equals("?") && canStruct.isBeginFrame)
                    int dlc = bytes.get(i + 2) & 0xF; // 解析DLC值（低4位）
                    if(dlc > 8) { // 如果DLC大于8，按CAN FD规范转换
                        switch (dlc){ // 根据DLC值进行CAN FD长度转换
                            case 9: dlc = 12;break; // DLC=9对应12字节
                            case 10: dlc = 16;break; // DLC=10对应16字节
                            case 11: dlc = 20;break; // DLC=11对应20字节
                            case 12: dlc = 24;break; // DLC=12对应24字节
                            case 13: dlc = 32;break; // DLC=13对应32字节
                            case 14: dlc = 48;break; // DLC=14对应48字节
                            case 15: dlc = 64;break; // DLC=15对应64字节
                            default: // 其他情况
                                dlc = 8; // 默认设为8字节
                                break; // 跳出default
                        } // 结束DLC转换switch
                    } // 结束DLC大于8判断
//                    canStruct.Data = getEncoding(encoding, dlc, 2 * 4);
                    canStruct.Data = "" + dlc; // 将DLC值转为字符串显示
                    canStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为DLC
                    canStruct.DataColor = DLCcolor; // 设置数据颜色为DLC颜色（绿色）
                    list.add(canStruct); // 将结构体添加到列表
//                    debugCan(bytes,i,"DLC",canStruct,list);
                } // 结束DLC case块
                break; // 跳出DLC分支
                case SerialBusStruct.CanStruct.DATA: { // 数据字段
                    canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体实例
                    if (list.size() > 0) { // 如果列表非空
                        canStruct.BeginX = list.get(list.size() - 1).EndX; // 起始X坐标设为上一个元素的结束X
                    } else { // 否则列表为空
                        canStruct.BeginX = 0; // 起始X坐标设为0
                        canStruct.isBeginFrame=false; // 标记为非起始帧
                    } // 结束列表非空判断
                    canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                    canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                    canStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为DATA
                    canStruct.DataColor = DataColor; // 设置数据颜色
                    canStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4);  //Integer.toHexString(bytes.get(i + 2));
                    list.add(canStruct); // 将结构体添加到列表
//                    debugCan(bytes,i,"Data",canStruct,list);
                } // 结束DATA case块
                break; // 跳出DATA分支
                case SerialBusStruct.CanStruct.CRC: { // CRC校验字段
                    if (canStruct == null || list.size() == 0) { // 如果当前没有CAN结构体或列表为空
                        //第一次接收数据为CRC
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体实例
                        canStruct.BeginX = 0; // 起始X坐标设为0
                        canStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为CRC
                        canStruct.ID = 0xFF & bytes.get(i + 2); // 解析CRC第一字节
                        canStruct.DataColor = ErrorColor; // 设置数据颜色为错误颜色（红色）
                        canStruct.isBeginFrame=false; // 标记为非起始帧
                    } else if (list.size() > 0) { // 否则如果列表非空
                        //正常的数据接收。
                        if ((canStruct.DataType & 0x0F) == SerialBusStruct.CanStruct.CRC) { // 如果当前已经是CRC类型
                            canStruct.DataType = bytes.get(i + 3) & 0x0F; // 更新数据类型为CRC
                            canStruct.DataColor = CrcColor; // 设置数据颜色为CRC颜色（绿色）
                            canStruct.ID = canStruct.ID << 8 | (0xFF & bytes.get(i + 2)); // 拼接CRC第二字节
                        } else { // 否则当前不是CRC类型
                            canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体实例
                            canStruct.BeginX = list.get(list.size() - 1).EndX; // 起始X坐标设为上一个元素的结束X
                            canStruct.DataType = bytes.get(i + 3) & 0x0F; // 设置数据类型为CRC
                            canStruct.ID = 0xFF & bytes.get(i + 2); // 解析CRC第一字节
                            canStruct.DataColor = CrcColor; // 设置数据颜色为CRC颜色（绿色）
                        } // 结束CRC类型判断
                    } else { // 否则（理论上不会进入此分支）
                        //第一次接收是CRC的，第二次还是CRC的接收进行拼字节
                        canStruct.DataType = bytes.get(i + 3) & 0x0F; // 更新数据类型为CRC
                        canStruct.DataColor = CrcColor; // 设置数据颜色为CRC颜色（绿色）
                        canStruct.ID = canStruct.ID << 8 | (0xFF & bytes.get(i + 2)); // 拼接CRC第二字节
                    } // 结束CRC接收处理


                } // 结束CRC case块
                break; // 跳出CRC分支
                case SerialBusStruct.CanStruct.ConfirmOver: { // 确认应答成功（ACK）
                    if (canStruct == null) { // 如果当前没有CAN结构体
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体实例
                        canStruct.BeginX = 0; // 起始X坐标设为0
                        canStruct.isBeginFrame=false; // 标记为非起始帧
                    } // 结束canStruct空判断
                    canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                    canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标

                    canStruct.DataColor = CrcColor; // 默认设置数据颜色为CRC颜色（绿色）
                    if((canStruct.ID & 0x04) != 0){ // 如果ID的第2位不为0，表示有错误
                        canStruct.DataColor = ErrorColor; // 设置数据颜色为错误颜色（红色）
                    } // 结束错误判断

                    int tmw = 16; // CRC显示位宽，默认16位
                    switch (canStruct.ID & 0x03){ // 根据ID低2位判断帧类型
                        case 0: // 标准帧
                            canStruct.ID >>= 9; // 右移9位获取标准帧CRC值
                            tmw = 16; // 标准帧CRC显示16位
                            break; // 跳出case 0
                        case 1: // 扩展帧类型1
                            canStruct.ID >>= 7; // 右移7位获取扩展帧CRC值
                            tmw = 20; // 扩展帧CRC显示20位
                            break; // 跳出case 1
                        case 2: // 扩展帧类型2
                            tmw = 24; // 扩展帧CRC显示24位
                            canStruct.ID >>= 3; // 右移3位获取扩展帧CRC值
                            break; // 跳出case 2
                    } // 结束帧类型判断
                    canStruct.Data = getEncoding(encoding, canStruct.ID, tmw) + "[A]"; //Integer.toHexString(canStruct.ID << 7 | (0x7F & bytes.get(i + 2))).toUpperCase() + "[~A]";
                    //canStruct.Data="CRC[~A]";

                    list.add(canStruct); // 将结构体添加到列表
//                    debugCan(bytes,i,"ConfirmOver",canStruct,list);
                } // 结束ConfirmOver case块
                break; // 跳出ConfirmOver分支
                case SerialBusStruct.CanStruct.NoConfirmOver: { // 确认应答未成功（NACK）
                    if (canStruct == null) { // 如果当前没有CAN结构体
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体实例
                        canStruct.BeginX = 0; // 起始X坐标设为0
                        canStruct.isBeginFrame=false; // 标记为非起始帧
                    } // 结束canStruct空判断
                    canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 从字节流中解析结束X坐标（4字节拼接）
                    canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp); // 将结束X坐标转换为像素坐标
                    canStruct.DataColor = CrcColor; // 默认设置数据颜色为CRC颜色（绿色）
                    if((canStruct.ID & 0x04) != 0){ // 如果ID的第2位不为0，表示有错误
                        canStruct.DataColor = ErrorColor; // 设置数据颜色为错误颜色（红色）
                    } // 结束错误判断
                    int tmw = 16; // CRC显示位宽，默认16位
                    switch (canStruct.ID & 0x03){ // 根据ID低2位判断帧类型
                        case 0: // 标准帧
                            canStruct.ID >>= 9; // 右移9位获取标准帧CRC值
                            tmw = 16; // 标准帧CRC显示16位
                            break; // 跳出case 0
                        case 1: // 扩展帧类型1
                            canStruct.ID >>= 7; // 右移7位获取扩展帧CRC值
                            tmw = 20; // 扩展帧CRC显示20位
                            break; // 跳出case 1
                        case 2: // 扩展帧类型2
                            tmw = 24; // 扩展帧CRC显示24位
                            canStruct.ID >>= 3; // 右移3位获取扩展帧CRC值
                            break; // 跳出case 2
                    } // 结束帧类型判断
                    canStruct.Data = getEncoding(encoding, canStruct.ID, tmw) + "[~A]"; //Integer.toHexString(canStruct.ID << 7 | (0x7F & bytes.get(i + 2))).toUpperCase() + "[A]";
                    //canStruct.Data="CRC";
//                    canStruct.DataColor = CrcColor;
                    list.add(canStruct); // 将结构体添加到列表
//                    debugCan(bytes,i,"NoConfirmOver",canStruct,list);
                } // 结束NoConfirmOver case块
                break; // 跳出NoConfirmOver分支
                case SerialBusStruct.CanStruct.Error: { // 错误帧分支
                    if (i - 8 + 3 < 0) { // 索引越界检查
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体
                        canStruct.BeginX = 0; // 起始X坐标设为0
                        if (list.size()<=0) canStruct.isBeginFrame=false; // 列表为空则标记非起始帧
                    } else if ((bytes.get(i - 8 + 3) & 0x0F) == SerialBusStruct.DataType_BeginData) { // 上一个数据包是开始帧
                        //上一个是开始帧，只进行赋值就可以了。
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体
                        if (list.size() > 0) { // 列表非空
                            canStruct.BeginX = list.get(list.size() - 1).EndX; // 起始X取上一个结构体的结束X
                        } else {canStruct.BeginX = 0;canStruct.isBeginFrame=false; } // 列表为空则起始X为0且标记非起始帧
                    }
                    if (canStruct == null) { // canStruct为空则创建默认结构体
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体
                        canStruct.BeginX = 0; // 起始X坐标设为0
                        canStruct.isBeginFrame=false; // 标记非起始帧
                    }
                    canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节得到结束时间戳
                    canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                    canStruct.DataColor = ErrorColor; // 设置错误帧颜色
                    canStruct.DataType = bytes.get(i + 3) & 0x0F; // 取低4位作为数据类型
                    canStruct.Data = "Err"; // 设置显示文本为"Err"
                    list.add(canStruct); // 将错误帧结构体加入列表
                    idShowUpTimes = 0; // 重置ID显示次数
//                    debugCan(bytes,i,"Error",canStruct,list);
                } // 结束Error case块
                break; // 跳出Error分支
                case SerialBusStruct.CanStruct.OverLoad: { // 过载帧分支
                    if (i - 8 + 3 < 0) { // 索引越界检查
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体
                        canStruct.BeginX = 0; // 起始X坐标设为0
                        if (list.size()<=0) canStruct.isBeginFrame=false; // 列表为空则标记非起始帧
                    } else if ((bytes.get(i - 8 + 3) & 0x0F) == SerialBusStruct.DataType_BeginData) { // 上一个数据包是开始帧
                        //上一个是开始帧，只进行赋值就可以了。
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体
                        if (list.size() > 0) { // 列表非空
                            canStruct.BeginX = list.get(list.size() - 1).EndX; // 起始X取上一个结构体的结束X
//                            if (list.get(list.size()-1).DataType==SerialBusStruct.CanStruct.CRC || list.get(list.size()-1).DataType==0){
//                                canStruct.BeginX=list.get(list.size()-1).BeginX;
//                            }
                        } else{ canStruct.BeginX = 0; canStruct.isBeginFrame=false;} // 列表为空则起始X为0且标记非起始帧
                    }
                    if (canStruct == null) { // canStruct为空则创建默认结构体
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体
                        canStruct.BeginX = 0; // 起始X坐标设为0
                        canStruct.isBeginFrame=false; // 标记非起始帧
                    }
                    canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节得到结束时间戳
                    canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                    canStruct.DataColor = OverLoadColor; // 设置过载帧颜色
                    canStruct.DataType = bytes.get(i + 3) & 0x0F; // 取低4位作为数据类型
                    canStruct.Data = "OverLoad"; // 设置显示文本为"OverLoad"
                    list.add(canStruct); // 将过载帧结构体加入列表
                    idShowUpTimes = 0; // 重置ID显示次数
//                    debugCan(bytes,i,"OverLoad",canStruct,list);
                } // 结束OverLoad case块
                break; // 跳出OverLoad分支
                case SerialBusStruct.CanStruct.Trouble: { // 干扰帧分支
                    if (i - 8 + 3 < 0) { // 索引越界检查
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体
                        canStruct.BeginX = 0; // 起始X坐标设为0
                        if (list.size()<=0) canStruct.isBeginFrame=false; // 列表为空则标记非起始帧
                    } else if ((bytes.get(i - 8 + 3) & 0x0F) != SerialBusStruct.DataType_BeginData) { // 上一个数据包不是开始帧
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体
                        if (list.size() > 0) { // 列表非空
                            canStruct.BeginX = list.get(list.size() - 1).EndX; // 起始X取上一个结构体的结束X
//                            if (list.get(list.size()-1).DataType==SerialBusStruct.CanStruct.CRC || list.get(list.size()-1).DataType==0){
//                                canStruct.BeginX=list.get(list.size()-1).BeginX;
//                            }
                        } else{ canStruct.BeginX = 0; canStruct.isBeginFrame=false;} // 列表为空则起始X为0且标记非起始帧
                    }
                    if (canStruct == null) { // canStruct为空则创建默认结构体
                        canStruct = SerialBusStruct.getInstance().new CanStruct(); // 创建新的CAN结构体
                        canStruct.BeginX = 0; // 起始X坐标设为0
                        canStruct.isBeginFrame=false; // 标记非起始帧
                    }

                    canStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节得到结束时间戳
                    canStruct.EndX = (int) (canStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                    canStruct.DataColor = ErrorColor; // 设置错误帧颜色
                    canStruct.DataType = bytes.get(i + 3) & 0x0F; // 取低4位作为数据类型
                    canStruct.Data = "Err"; // 设置显示文本为"Err"
                    list.add(canStruct); // 将干扰帧结构体加入列表
                    idShowUpTimes = 0; // 重置ID显示次数
//                    debugCan(bytes,i,"Trouble",canStruct,list);
                } // 结束Trouble case块
                break; // 跳出Trouble分支
            } // 结束switch语句


        } // 结束for循环

        //将最后一个添加上
        if (list.size() > 0 && canStruct != null) // 列表非空且canStruct不为null
            if (canStruct.hashCode() != list.get(list.size() - 1).hashCode()) { // 最后一个结构体与列表末尾不同
                canStruct.Data = "?"; // 设置显示文本为"?"
//                canStruct.EndX = GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_YT) + 12;
                canStruct.EndX = this.endX - this.startX; // 结束X设为绘制区域宽度
                list.add(canStruct); // 将最后一个结构体加入列表
            }
        return list; // 返回CAN结构体列表
    } // 结束getCanStruct方法

    /**
     * CAN协议调试方法，输出CAN结构体和原始字节数据到日志
     *
     * @param bytes     原始ByteBuffer数据
     * @param index     当前数据包的起始索引
     * @param tag       调试标签（如"StandardID"、"DLC"等）
     * @param canStruct 当前CAN结构体
     * @param list      已解析的CAN结构体列表
     * 业务意义：开发调试阶段用于验证CAN协议解析的正确性
     */
    private void debugCan(ByteBuffer bytes, int index, String tag, SerialBusStruct.CanStruct canStruct, List<SerialBusStruct.CanStruct> list) { // CAN协议调试方法
        if (canStruct.EndX - canStruct.BeginX > 10) { // 帧宽度大于10像素才输出日志
            Logger.i(TAG, tag + "  index:" + index + "  canStruct:" + canStruct.toString()); // 输出CAN结构体信息
            Logger.e(TAG, "bytes:" + SerialBusTxtStructParse.getDebugBytesToString(index, bytes)); // 输出原始字节数据
        }
    } // 结束debugCan方法
    /**
     * SPI协议调试方法，输出SPI结构体和原始字节数据到日志
     *
     * @param bytes     原始ByteBuffer数据
     * @param index     当前数据包的起始索引
     * @param tag       调试标签
     * @param spiStruct 当前SPI结构体
     * 业务意义：开发调试阶段用于验证SPI协议解析的正确性
     */
    private void debugSpi(ByteBuffer bytes,int index,String tag,SerialBusStruct.SpiStruct spiStruct){ // SPI协议调试方法
        if (spiStruct.EndX - spiStruct.BeginX > 10) { // 帧宽度大于10像素才输出日志
            Logger.i(TAG, tag + "  index:" + index + "  spiStruct:" + spiStruct.toString()); // 输出SPI结构体信息
            Logger.e(TAG, "bytes:" + SerialBusTxtStructParse.getDebugBytesToString(index, bytes)); // 输出原始字节数据
        }
    } // 结束debugSpi方法

    /**
     * 绘制CAN协议解码图像。
     * 先绘制中间基线，再逐个绘制解码元素，最后擦除标题栏区域。
     *
     * @param titleLen 标题栏宽度（像素）
     * @param canvas   Android画布
     * @param paint    画笔
     * @param list     CAN结构体列表，由getCanStruct解析得到
     * 业务意义：将CAN协议的各字段（ID/DLC/数据/CRC/ACK/错误帧等）以图形方式呈现
     */
    private void drawParseCanData(int titleLen, Canvas canvas, Paint paint, List<SerialBusStruct.CanStruct> list) { // 绘制CAN协议解码图像
        paint.setXfermode(srcMode); // 设置源覆盖混合模式
        canvas.drawLine(this.startX, canvas.getHeight() / 2, this.endX, canvas.getHeight() / 2, paint); // 绘制中间基线
        for (int i = 0; i < list.size(); i++) { // 遍历所有CAN结构体
            drawElement(canvas, paint, list.get(i).BeginX + this.startX, list.get(i).EndX - list.get(i).BeginX, list.get(i).Data, list.get(i).DataColor,list.get(i).isBeginFrame); // 绘制单个解码元素
        }
        paint.setXfermode(desoutMode); // 设置擦除混合模式
        rectTitleLen.set(0, 0, titleLen, canvas.getHeight()); // 设置标题栏矩形区域
        canvas.drawRect(rectTitleLen, paint); // 擦除标题栏区域
    } // 结束drawParseCanData方法

    // endregion

    //region SPI 绘制

    /**
     * 解析SPI协议的二进制数据为SpiStruct结构体列表。
     * SPI（Serial Peripheral Interface）协议帧结构：起始→数据位（4~16bit）→停止
     * 多字节数据通过BusData+LastBusData拼接
     *
     * @param bytes FPGA上传的原始ByteBuffer数据
     * @return 解析后的SpiStruct列表，每个元素代表SPI协议的一个数据段
     * 业务意义：将FPGA采集的SPI总线信号解码为可读的数据帧
     */
    private List<SerialBusStruct.SpiStruct> getSpiStruct(ByteBuffer bytes) { // 解析SPI协议数据
        final int CodeColor = App.get().getResources().getColor(R.color.textColor);//Color.WHITE; // 数据颜色
        int encoding = ICharacterEncoding.Hex;  //也是界面传进来的 // 编码方式默认十六进制


        long tmp = this.timeToPix;//400000l;  //时间转像素 // 时间到像素的转换因子
        int dataBit = this.bits; //8; // 数据位宽
        int dataBit_showTimes = 1;  //字节数据显示的次数。 // 字节数据显示次数计数器
        List<SerialBusStruct.SpiStruct> list = new ArrayList<>(); // SPI结构体列表
        SerialBusStruct.SpiStruct spiStruct = null; // 当前SPI结构体
        for (int i = 0; i <= bytes.limit() - 8; i += 8) { // 遍历字节数据，每次8字节
            //过采样处理
            if ((bytes.get(i + 3) & 0x80) == 0x80) { // 检查过采样标志位
                spiStruct = SerialBusStruct.getInstance().new SpiStruct(); // 创建新的SPI结构体
                spiStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节得到结束时间戳
                spiStruct.EndX = (int) (spiStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                spiStruct.BeginX = spiStruct.EndX; // 过采样时起始X等于结束X
                spiStruct.Data = "?"; // 设置显示文本为"?"
                spiStruct.isBeginFrame=true; // 标记为起始帧
                list.add(spiStruct); // 将过采样结构体加入列表
//                debugSpi(bytes,i,TAG,spiStruct);
                continue; // 跳过当前数据包
            }

            if (bytes.get(i+3)==SerialBusStruct.DataType_BeginData){ // 检查是否为开始帧
//                if (spiStruct!=null && spiStruct.DataType==SerialBusStruct.DataType_BeginData){
//                    spiStruct.EndX=spiStruct.BeginX;
//                    spiStruct.Data="?";
//                    list.add(spiStruct);
//                }
                spiStruct = SerialBusStruct.getInstance().new SpiStruct(); // 创建新的SPI结构体
                spiStruct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节得到起始时间戳
                spiStruct.BeginX = (int) (spiStruct.BeginX * Cycle / tmp); // 将时间戳转换为像素坐标
                spiStruct.Data = ""; // 初始化显示文本为空
                spiStruct.DataColor = CodeColor; // 设置数据颜色
                spiStruct.DataType=SerialBusStruct.DataType_BeginData; // 设置数据类型为开始帧
                spiStruct.isBeginFrame=true; // 标记为起始帧
                continue; // 跳过当前数据包，等待后续数据
            }

            if ((bytes.get(i + 3) & SerialBusStruct.SpiStruct.DataType_3bit_Stop) == SerialBusStruct.SpiStruct.DataType_3bit_Stop) { // 检查是否为停止帧
                if (spiStruct == null) { // spiStruct为空则创建默认结构体
                    spiStruct = SerialBusStruct.getInstance().new SpiStruct(); // 创建新的SPI结构体
                    spiStruct.Data = ""; // 初始化显示文本为空
                    spiStruct.BeginX = 0; // 起始X坐标设为0
                    spiStruct.isBeginFrame=false; // 标记非起始帧
                }
                spiStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节得到结束时间戳
                spiStruct.EndX = (int) (spiStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                spiStruct.DataType = SerialBusStruct.SpiStruct.DataType_3bit_Stop; // 设置数据类型为停止帧
                spiStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, dataBit);  //String.format("%02x", bytes.get(i + 2)).toUpperCase(); // 按编码方式转换数据
                spiStruct.DataColor = CodeColor; // 设置数据颜色
                list.add(spiStruct); // 将停止帧结构体加入列表
//                debugSpi(bytes,i,TAG,spiStruct);
            } else if ((bytes.get(i + 3) & SerialBusStruct.SpiStruct.DataType_2bit_LastBusData) == SerialBusStruct.SpiStruct.DataType_2bit_LastBusData) { // 检查是否为最后一包总线数据
                if (spiStruct == null) { // spiStruct为空则创建默认结构体
                    spiStruct = SerialBusStruct.getInstance().new SpiStruct(); // 创建新的SPI结构体
                    spiStruct.Data = ""; // 初始化显示文本为空
                    spiStruct.BeginX = 0; // 起始X坐标设为0
                    spiStruct.isBeginFrame=false; // 标记非起始帧
                }
                spiStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节得到结束时间戳
                spiStruct.EndX = (int) (spiStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                spiStruct.DataType = SerialBusStruct.SpiStruct.DataType_3bit_Stop; // 设置数据类型为停止帧
                if (dataBit == 4) // 数据位宽为4位
                    spiStruct.Data += getEncoding(encoding, bytes.get(i + 2) & 0x00FF, dataBit); // 按4位编码追加数据
                else spiStruct.Data += getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 8); // 按8位编码追加数据
                spiStruct.DataColor = CodeColor; // 设置数据颜色
                list.add(spiStruct); // 将最后一包数据结构体加入列表
//                debugSpi(bytes,i,TAG,spiStruct);
                dataBit_showTimes = 1; // 重置字节数据显示次数
//                Logger.i(TAG,"11合成包："+spiStruct.toString());
            } else if ((bytes.get(i + 3) & SerialBusStruct.SpiStruct.DataType_2bit_BusData) == SerialBusStruct.SpiStruct.DataType_2bit_BusData) { // 检查是否为中间总线数据
                if (spiStruct == null) { // spiStruct为空则创建默认结构体
                    spiStruct = SerialBusStruct.getInstance().new SpiStruct(); // 创建新的SPI结构体
                    spiStruct.Data = ""; // 初始化显示文本为空
                    spiStruct.BeginX = 0; // 起始X坐标设为0
                    spiStruct.isBeginFrame=false; // 标记非起始帧
                }
                spiStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节得到结束时间戳
                spiStruct.EndX = (int) (spiStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                spiStruct.DataType = SerialBusStruct.SpiStruct.DataType_3bit_Stop; // 设置数据类型为停止帧
                if (dataBit == 4) // 数据位宽为4位
                    spiStruct.Data += getEncoding(encoding, bytes.get(i + 2) & 0x00FF, dataBit); // 按4位编码追加数据
                else spiStruct.Data += getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 8); // 按8位编码追加数据
                spiStruct.DataColor = CodeColor; // 设置数据颜色
//                Logger.i(TAG,"dataBit:"+dataBit+"    01数据包:"+spiStruct.toString());
            }

        } // 结束for循环

        //将最后一个添加上
        if (list.size() > 0 && spiStruct != null) // 列表非空且spiStruct不为null
            if (spiStruct.hashCode() != list.get(list.size() - 1).hashCode()) { // 最后一个结构体与列表末尾不同
                spiStruct.Data = "?"; // 设置显示文本为"?"
//                spiStruct.EndX = GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_YT) + 12;
                spiStruct.EndX = this.endX - this.startX; // 结束X设为绘制区域宽度
                list.add(spiStruct); // 将最后一个结构体加入列表
            }
        return list; // 返回SPI结构体列表
    } // 结束getSpiStruct方法

    /**
     * 绘制SPI协议解码图像。
     * 先绘制中间基线，再逐个绘制解码元素，最后擦除标题栏区域。
     *
     * @param titleLen 标题栏宽度（像素）
     * @param canvas   Android画布
     * @param paint    画笔
     * @param list     SPI结构体列表，由getSpiStruct解析得到
     * 业务意义：将SPI协议的数据帧以图形方式呈现在示波器屏幕上
     */
    private void drawParseSpiData(int titleLen, Canvas canvas, Paint paint, List<SerialBusStruct.SpiStruct> list) { // 绘制SPI协议解码图像
        paint.setXfermode(srcMode); // 设置源覆盖混合模式
        canvas.drawLine(this.startX, canvas.getHeight() / 2, this.endX, canvas.getHeight() / 2, paint); // 绘制中间基线
        for (int i = 0; i < list.size(); i++) { // 遍历所有SPI结构体
            //如果开始的时候不是从头开始的，以“-”线补满
//            if (i == 0 && list.get(i).BeginX > 0) {
//                drawElement(canvas, paint, 0, list.get(i).BeginX, null, list.get(i).DataColor);
//            }
//            //如果结束的时候没有到头，以“-”线补满
//            if (i == list.size() - 1 && list.get(i).EndX < canvas.getWidth()) {
//                drawElement(canvas, paint, list.get(i).EndX, canvas.getWidth() - list.get(i).EndX, null, list.get(i).DataColor);
//            }
//
//            //如果在中间的帧，以“-”线补满
//            if (i > 0 && i < list.size()) {
//                drawElement(canvas, paint, list.get(i - 1).EndX, list.get(i).BeginX - list.get(i - 1).EndX, null, 0);
//            }
            drawElement(canvas, paint, list.get(i).BeginX + this.startX, list.get(i).EndX - list.get(i).BeginX, list.get(i).Data, list.get(i).DataColor,list.get(i).isBeginFrame); // 绘制单个解码元素
        }
        paint.setXfermode(desoutMode); // 设置擦除混合模式
        rectTitleLen.set(0, 0, titleLen, canvas.getHeight()); // 设置标题栏矩形区域
        canvas.drawRect(rectTitleLen, paint); // 擦除标题栏区域

    } // 结束drawParseSpiData方法
    //endregion

    //region I2C 绘制

    /**
     * 解析I2C协议的二进制数据为I2cStruct结构体列表。
     * I2C（Inter-Integrated Circuit）协议帧结构：
     * 起始条件→地址(7bit)+R/W(1bit)+ACK→数据(8bit)+ACK→停止条件
     *
     * @param bytes FPGA上传的原始ByteBuffer数据
     * @return 解析后的I2cStruct列表，每个元素代表I2C协议的一个字段段
     * 业务意义：将FPGA采集的I2C总线信号解码为可读的协议帧字段
     */
    private List<SerialBusStruct.I2cStruct> getI2cStruct(ByteBuffer bytes) { // 解析I2C协议数据
        final int DataColor = App.get().getResources().getColor(R.color.textColor);//Color.WHITE; // 数据颜色
        final int WriteColor = Color.YELLOW; // 写操作颜色
        final int ReadColor = Color.GREEN; // 读操作颜色

        long tmp = timeToPix;// 4000000l;  //时间转像素 // 时间到像素的转换因子
        int encoding = ICharacterEncoding.Hex;  //也是界面传进来的 // 编码方式默认十六进制
//        Logger.i("TAG", "bytes:" + SerialBusTxtStructParse.getDebugBytesToString(bytes));
        List<SerialBusStruct.I2cStruct> list = new ArrayList<>(); // I2C结构体列表
        SerialBusStruct.I2cStruct i2cStruct = null; // 当前I2C结构体
        for (int i = 0; i <= bytes.limit() - 8; i += 8) { // 遍历字节数据，每次8字节
            //过采样处理
            if ((bytes.get(i + 3) & 0x80) == 0x80) { // 检查过采样标志位
                i2cStruct = SerialBusStruct.getInstance().new I2cStruct(); // 创建新的I2C结构体
                i2cStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节得到结束时间戳
                i2cStruct.EndX = (int) (i2cStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                i2cStruct.BeginX = i2cStruct.EndX; // 过采样时起始X等于结束X
                i2cStruct.Data = "?"; // 设置显示文本为"?"
                i2cStruct.isBeginFrame=true; // 标记为起始帧
                list.add(i2cStruct); // 将过采样结构体加入列表
//                Logger.i("TAG", "过采样处理");
                continue; // 跳过当前数据包
            }

            if (bytes.get(i + 3) == SerialBusStruct.DataType_BeginData) { // 检查是否为I2C开始帧
//                if (i2cStruct != null && i2cStruct.DataType == SerialBusStruct.DataType_BeginData) {
//                    i2cStruct.EndX = i2cStruct.BeginX;
//                    i2cStruct.Data = "?";
//                    list.add(i2cStruct);
//                }
                i2cStruct = SerialBusStruct.getInstance().new I2cStruct(); // 创建新的I2C结构体
                i2cStruct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节得到起始时间戳
                i2cStruct.BeginX = (int) (i2cStruct.BeginX * Cycle / tmp); // 将时间戳转换为像素坐标
                i2cStruct.DataColor = Color.RED; // 设置开始帧颜色为红色
                i2cStruct.DataType = SerialBusStruct.DataType_BeginData; // 设置数据类型为开始帧
                i2cStruct.isBeginFrame=true; // 标记为起始帧
                continue; // 跳过当前数据包，等待后续数据
            }
            if ((bytes.get(i+3) & 0x08)==SerialBusStruct.I2cStruct.DataType_3bit_Stop){ // 检查是否为停止帧
                i2cStruct=null; // 清空当前I2C结构体引用
                continue; // 跳过当前数据包
            }
            switch (bytes.get(i + 3) & 0x03) { // 根据低2位判断I2C数据类型
                case SerialBusStruct.I2cStruct.DataType_1bit_Write: { // 写操作分支
                    if (i2cStruct == null) { // i2cStruct为空则创建默认结构体
                        i2cStruct = SerialBusStruct.getInstance().new I2cStruct(); // 创建新的I2C结构体
                        i2cStruct.BeginX = 0; // 起始X坐标设为0
                        i2cStruct.isBeginFrame=false; // 标记非起始帧
                    }
                    i2cStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节得到结束时间戳
                    i2cStruct.EndX = (int) (i2cStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                    i2cStruct.Data = "W:" + getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4); // String.format("%02x", bytes.get(i + 2)).toUpperCase(); // 拼接写操作显示文本
                    i2cStruct.ShortData = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4); // 设置短格式数据
                    if ((bytes.get(i + 3) & SerialBusStruct.I2cStruct.DataType_2bit_Response) == SerialBusStruct.I2cStruct.DataType_2bit_Response) { // 检查ACK响应
                        i2cStruct.Data += "[A]"; // 添加ACK标记
                    } else
                        i2cStruct.Data += "[~A]"; // 添加NACK标记
                    i2cStruct.DataType = bytes.get(i + 3) & 0x03; // 设置数据类型
                    i2cStruct.DataColor = WriteColor; // 设置写操作颜色
                    list.add(i2cStruct); // 将写操作结构体加入列表
                }
                break; // 跳出Write分支
                case SerialBusStruct.I2cStruct.DataType_1bit_Read: { // 读操作分支
                    if (i2cStruct == null) { // i2cStruct为空则创建默认结构体
                        i2cStruct = SerialBusStruct.getInstance().new I2cStruct(); // 创建新的I2C结构体
                        i2cStruct.BeginX = 0; // 起始X坐标设为0
                        i2cStruct.isBeginFrame=false; // 标记非起始帧
                    }
                    i2cStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节得到结束时间戳
                    i2cStruct.EndX = (int) (i2cStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                    i2cStruct.Data = "R:" + getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4); //String.format("%02x", bytes.get(i + 2)).toUpperCase(); // 拼接读操作显示文本
                    i2cStruct.ShortData = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4); // 设置短格式数据
                    if ((bytes.get(i + 3) & SerialBusStruct.I2cStruct.DataType_2bit_Response) == SerialBusStruct.I2cStruct.DataType_2bit_Response) { // 检查ACK响应
                        i2cStruct.Data += "[A]"; // 添加ACK标记
                    } else
                        i2cStruct.Data += "[~A]"; // 添加NACK标记
                    i2cStruct.DataType = bytes.get(i + 3) & 0x03; // 设置数据类型
                    i2cStruct.DataColor = ReadColor; // 设置读操作颜色
                    list.add(i2cStruct); // 将读操作结构体加入列表
                }
                break; // 跳出Read分支
                case SerialBusStruct.I2cStruct.DataType_1bit_Data: { // 数据分支
                    if (i2cStruct == null) { // i2cStruct为空则创建默认结构体
                        i2cStruct = SerialBusStruct.getInstance().new I2cStruct(); // 创建新的I2C结构体
                        i2cStruct.BeginX = 0; // 起始X坐标设为0
                        i2cStruct.isBeginFrame=false; // 标记非起始帧
                    }
                    i2cStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节得到结束时间戳
                    i2cStruct.EndX = (int) (i2cStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                    i2cStruct.Data = "D:" + getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4); //String.format("%02x", bytes.get(i + 2)).toUpperCase(); // 拼接数据显示文本
                    i2cStruct.ShortData = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4); // 设置短格式数据
                    if ((bytes.get(i + 3) & SerialBusStruct.I2cStruct.DataType_2bit_Response) == SerialBusStruct.I2cStruct.DataType_2bit_Response) { // 检查ACK响应
                        i2cStruct.Data += "[A]"; // 添加ACK标记
                    } else
                        i2cStruct.Data += "[~A]"; // 添加NACK标记
                    i2cStruct.DataType = bytes.get(i + 3) & 0x03; // 设置数据类型
                    i2cStruct.DataColor = DataColor; // 设置数据颜色
                    list.add(i2cStruct); // 将数据结构体加入列表
                }
                break; // 跳出Data分支
            } // 结束switch语句
        } // 结束for循环

        //将最后一个添加上
        if (list.size() > 0 && i2cStruct != null) // 列表非空且i2cStruct不为null
            if (i2cStruct.hashCode() != list.get(list.size() - 1).hashCode()) { // 最后一个结构体与列表末尾不同
                i2cStruct.Data = "?"; // 设置显示文本为"?"
                i2cStruct.EndX = this.endX - this.startX; // 结束X设为绘制区域宽度
//                Logger.i(TAG," i2c:"+i2cStruct.toString()+"  last i2c:"+list.get(list.size()-1).toString());
                list.add(i2cStruct); // 将最后一个结构体加入列表
            }
        return list; // 返回I2C结构体列表
    } // 结束getI2cStruct方法

    /**
     * 绘制I2C协议解码图像。
     * 与其他协议不同，I2C绘制使用带ShortData的drawElement重载方法，
     * 当空间不足时显示短格式数据（不含W:/R:/D:前缀）。
     *
     * @param titleLen 标题栏宽度（像素）
     * @param canvas   Android画布
     * @param paint    画笔
     * @param list     I2C结构体列表，由getI2cStruct解析得到
     * 业务意义：将I2C协议的各字段（写地址/读地址/数据+ACK/NACK）以图形方式呈现
     */
    private void drawParseI2cData(int titleLen, Canvas canvas, Paint paint, List<SerialBusStruct.I2cStruct> list) { // 绘制I2C协议解码图像
        paint.setXfermode(srcMode); // 设置源覆盖混合模式
        canvas.drawLine(this.startX, canvas.getHeight() / 2, this.endX, canvas.getHeight() / 2, paint); // 绘制中间基线
        for (int i = 0; i < list.size(); i++) { // 遍历所有I2C结构体
            //如果开始的时候不是从头开始的，以“-”线补满
//            if (i == 0 && list.get(i).BeginX > 0) {
//                drawElement(canvas, paint, 0, list.get(i).BeginX, null, list.get(i).DataColor);
//            }
//            //如果结束的时候没有到头，以“-”线补满
//            if (i == list.size() - 1 && list.get(i).EndX < canvas.getWidth()) {
//                drawElement(canvas, paint, list.get(i).EndX, canvas.getWidth() - list.get(i).EndX, null, list.get(i).DataColor);
//            }
//
//            //如果在中间的帧，以“-”线补满
//            if (i > 0 && i < list.size()) {
//                drawElement(canvas, paint, list.get(i - 1).EndX, list.get(i).BeginX - list.get(i - 1).EndX, null, 0);
//            }

            drawElement(canvas, paint, list.get(i).BeginX + this.startX, list.get(i).EndX - list.get(i).BeginX, list.get(i).Data, list.get(i).ShortData, list.get(i).DataColor,list.get(i).isBeginFrame); // 绘制单个解码元素（含短格式数据）
        }
        paint.setXfermode(desoutMode); // 设置擦除混合模式
        rectTitleLen.set(0, 0, titleLen, canvas.getHeight()); // 设置标题栏矩形区域
        canvas.drawRect(rectTitleLen, paint); // 擦除标题栏区域
    } // 结束drawParseI2cData方法
    //endregion


    //region 1553B 绘制

    /**
     * 解析MIL-STD-1553B协议的二进制数据为MilSTD1553bStruct结构体列表。
     * MIL-STD-1553B协议帧结构：
     * 命令字：同步头→远程地址(5bit)→T/R→子地址/模式(5bit)→数据字数/模式码(5bit)→奇偶校验
     * 数据字：同步头→数据(16bit)→奇偶校验
     *
     * @param bytes FPGA上传的原始ByteBuffer数据
     * @return 解析后的MilSTD1553bStruct列表，每个元素代表1553B协议的一个字段段
     * 业务意义：将FPGA采集的1553B军用航空总线信号解码为可读的协议帧字段
     */
    private List<SerialBusStruct.MilSTD1553bStruct> getMilSTD1553bStruct(ByteBuffer bytes) { // 解析1553B协议数据
        final int RemoteColor = Color.BLUE; // 远程终端颜色
        final int DataColor = App.get().getResources().getColor(R.color.textColor);//Color.WHITE; // 数据颜色
        final int CommandColor = Color.YELLOW; // 命令字颜色
        final int ErrorColor = Color.RED; // 错误颜色

        long tmp = this.timeToPix;// 2000000l;  //时间转像素 // 时间到像素的转换因子
        int encoding = this.encoding;//ICharacterEncoding.Binary;  //也是界面传进来的 // 编码方式

        List<SerialBusStruct.MilSTD1553bStruct> list = new ArrayList<>(); // 1553B结构体列表
        SerialBusStruct.MilSTD1553bStruct milSTD1553bStruct = null; // 当前1553B结构体
        for (int i = 0; i <= bytes.limit() - 8; i += 8) { // 遍历字节数据，每次8字节
            //过采样处理
            if ((bytes.get(i + 3) & 0x80) == 0x80) { // 检查过采样标志位
                milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct(); // 创建新的1553B结构体
                milSTD1553bStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算结束X坐标
                milSTD1553bStruct.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                milSTD1553bStruct.BeginX = milSTD1553bStruct.EndX; // 起始位置设为上一个结束位置
                milSTD1553bStruct.Data = "?"; // 数据标记为未知
                milSTD1553bStruct.isBeginFrame=true; // 标记为帧起始
                list.add(milSTD1553bStruct); // 将结构体添加到列表
                continue; // 跳过后续处理
            }
            if (bytes.get(i+3)==SerialBusStruct.DataType_BeginData){ // 判断是否为帧起始数据类型
//                if (milSTD1553bStruct!=null && milSTD1553bStruct.DataType==SerialBusStruct.DataType_BeginData){
//                    milSTD1553bStruct.EndX=milSTD1553bStruct.BeginX;
//                    milSTD1553bStruct.Data="";
//                    list.add(milSTD1553bStruct);
//                }
                    milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct(); // 创建新的1553B结构体
                    milSTD1553bStruct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算起始X坐标
                    milSTD1553bStruct.BeginX = (int) (milSTD1553bStruct.BeginX * Cycle / tmp); // 将时间戳转换为像素坐标
                    milSTD1553bStruct.DataColor = ErrorColor; // 设置数据颜色为错误色
                    milSTD1553bStruct.DataType=bytes.get(i+3); // 设置数据类型
                    milSTD1553bStruct.isBeginFrame=true; // 标记为帧起始
                continue; // 跳过后续处理
            }
            switch (bytes.get(i + 3) & 0x0F) { // 根据数据类型低4位进行分支处理

                case SerialBusStruct.MilSTD1553bStruct.DataType_RemoteAddr: { // 远程地址类型
                    if (milSTD1553bStruct == null) { // 如果结构体为空则创建
                        milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct(); // 创建新的1553B结构体
                        milSTD1553bStruct.BeginX = 0; // 起始位置设为0
                        milSTD1553bStruct.isBeginFrame=false; // 非帧起始
                    }
                    milSTD1553bStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算结束X坐标
                    milSTD1553bStruct.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                    milSTD1553bStruct.DataType = bytes.get(i + 3) &0x0F; // 设置数据类型为低4位
                    if (encoding == ICharacterEncoding.Binary) { // 如果编码方式为二进制
                        milSTD1553bStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 5); // 以5位二进制编码远程地址
                    } else {
                        milSTD1553bStruct.Data = getEncoding(encoding, bytes.get(i + 2) & 0x00FF, 2 * 4); // 以8位十六进制编码远程地址
                    }
                    //String.format("%02x", bytes.get(i + 2)).toUpperCase();
                    milSTD1553bStruct.DataColor = RemoteColor; // 设置数据颜色为远程地址颜色
                    list.add(milSTD1553bStruct); // 将远程地址结构体添加到列表

                    //添加< 为指令6，5做准备,结束为开始
                    milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct(); // 创建新结构体用于后续指令字段
                    milSTD1553bStruct.BeginX = list.get(list.size() - 1).EndX; // 起始位置设为上一个元素的结束位置
                }
                break;
                case SerialBusStruct.MilSTD1553bStruct.DataType_Command6bit: { // 命令字高6位类型
                    //9-14位 为高6位
                    if (milSTD1553bStruct == null) { // 如果结构体为空则创建
                        milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct(); // 创建新的1553B结构体
                        milSTD1553bStruct.BeginX = 0; // 起始位置设为0
                        milSTD1553bStruct.isBeginFrame=false; // 非帧起始
                    }
                    milSTD1553bStruct.Id = 0x0000003F & ((bytes.get(i + 2) & 0x3F)); // 取低6位作为命令字ID
                    milSTD1553bStruct.DataColor = CommandColor; // 设置数据颜色为命令字颜色
                    milSTD1553bStruct.DataType = bytes.get(i + 3)&0x0F; // 设置数据类型为低4位

                }
                break;
                case SerialBusStruct.MilSTD1553bStruct.DataType_Command5bit: { // 命令字低5位类型
                    //15-19位 为低5位
                    if (milSTD1553bStruct == null) { // 如果结构体为空则创建并直接添加
                        milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct(); // 创建新的1553B结构体
                        milSTD1553bStruct.BeginX = 0; // 起始位置设为0
                        milSTD1553bStruct.Data = "?"; // 数据标记为未知
                        milSTD1553bStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算结束X坐标
                        milSTD1553bStruct.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                        milSTD1553bStruct.DataColor = CommandColor; // 设置数据颜色为命令字颜色
                        milSTD1553bStruct.isBeginFrame=false; // 非帧起始
                        list.add(milSTD1553bStruct); // 添加到列表
                    } else {
                        milSTD1553bStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算结束X坐标
                        milSTD1553bStruct.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                        int id = (bytes.get(i + 1) & 0xC0) << 3 | ((bytes.get(i + 2) & 0x07)); // 计算低5位ID值
                        milSTD1553bStruct.Id = (0xFFFFFFE0 & milSTD1553bStruct.Id << 5) | id; // 将高6位左移5位后与低5位拼接
                        if (encoding == ICharacterEncoding.Binary) { // 如果编码方式为二进制
                            milSTD1553bStruct.Data = getEncoding(encoding, milSTD1553bStruct.Id, 11); // 以11位二进制编码命令字
                        } else {
                            milSTD1553bStruct.Data = getEncoding(encoding, milSTD1553bStruct.Id, 3 * 4); // 以12位十六进制编码命令字 //String.format("%03x", milSTD1553bStruct.Id).toUpperCase();
                        }
                        milSTD1553bStruct.DataColor = CommandColor; // 设置数据颜色为命令字颜色
                        milSTD1553bStruct.DataType = bytes.get(i + 3) &0x0F; // 设置数据类型为低4位
                        list.add(milSTD1553bStruct); // 添加到列表
                    }

                }
                break;
                case SerialBusStruct.MilSTD1553bStruct.DataType_Data1: { // 数据字高8位类型
                    if (milSTD1553bStruct == null) { // 如果结构体为空则创建
                        milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct(); // 创建新的1553B结构体
                        milSTD1553bStruct.BeginX = 0; // 起始位置设为0
                        milSTD1553bStruct.isBeginFrame=false; // 非帧起始
                    }
                    milSTD1553bStruct.Id = 0xFFFFFF00 & ((bytes.get(i + 2)) << 8); // 取数据字高8位并左移8位存入Id
                    milSTD1553bStruct.DataColor = DataColor; // 设置数据颜色
                    milSTD1553bStruct.DataType = bytes.get(i + 3)&0x0F; // 设置数据类型为低4位

                }
                break;
                case SerialBusStruct.MilSTD1553bStruct.DataType_Data2: { // 数据字低8位类型
                    if (milSTD1553bStruct == null) { // 如果结构体为空则创建并直接添加
                        milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct(); // 创建新的1553B结构体
                        milSTD1553bStruct.BeginX = 0; // 起始位置设为0
                        milSTD1553bStruct.Data = "?"; // 数据标记为未知
                        milSTD1553bStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算结束X坐标
                        milSTD1553bStruct.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                        milSTD1553bStruct.DataColor = DataColor; // 设置数据颜色
                        milSTD1553bStruct.isBeginFrame=false; // 非帧起始
                        list.add(milSTD1553bStruct); // 添加到列表
                    } else {
                        milSTD1553bStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算结束X坐标
                        milSTD1553bStruct.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标

                        milSTD1553bStruct.Id |= (bytes.get(i + 2) & 0xFF); // 将低8位与高8位拼接为完整16位数据字
                        milSTD1553bStruct.Data = getEncoding(encoding, milSTD1553bStruct.Id, 4 * 4); // 以16位十六进制编码数据字 //String.format("%04x", milSTD1553bStruct.Id).toUpperCase();
                        milSTD1553bStruct.DataColor = DataColor; // 设置数据颜色
                        milSTD1553bStruct.DataType = bytes.get(i + 3)&0x0F; // 设置数据类型为低4位
                        list.add(milSTD1553bStruct); // 添加到列表
                    }

                }
                break;
                case SerialBusStruct.MilSTD1553bStruct.DataType_OverSuccess: { // 帧成功结束类型
                    if (list.size() > 0) { // 如果列表非空
                        SerialBusStruct.MilSTD1553bStruct milSTD1553bStruct1 = list.get(list.size() - 1); // 获取最后一个元素
                        milSTD1553bStruct1.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算结束X坐标
                        milSTD1553bStruct1.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                        list.set(list.size() - 1, milSTD1553bStruct1); // 更新列表中最后一个元素
                    }
                }
                break;
                case SerialBusStruct.MilSTD1553bStruct.DataType_OverFailed: { // 帧失败结束类型
                    if (list.size() > 0) { // 如果列表非空
                        SerialBusStruct.MilSTD1553bStruct milSTD1553bStruct1 = list.get(list.size() - 1); // 获取最后一个元素
                        milSTD1553bStruct1.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算结束X坐标
                        milSTD1553bStruct1.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                        milSTD1553bStruct1.DataColor = ErrorColor; // 设置数据颜色为错误色（表示失败）
                        list.set(list.size() - 1, milSTD1553bStruct1); // 更新列表中最后一个元素
                    }
                }
                break;
                case SerialBusStruct.MilSTD1553bStruct.DataType_ManchesterCodeFailed: { // 曼彻斯特编码错误类型
//                    if (milSTD1553bStruct == null) {
                    milSTD1553bStruct = SerialBusStruct.getInstance().new MilSTD1553bStruct(); // 创建新的1553B结构体
                    milSTD1553bStruct.BeginX = 0; // 起始位置初始化为0
//                    }
                    if (list.size() > 0) { // 如果列表非空
                        milSTD1553bStruct.BeginX = list.get(list.size() - 1).EndX; // 起始位置设为上一个元素的结束位置
                    }else {
                        milSTD1553bStruct.isBeginFrame=false; // 非帧起始
                    }
                    milSTD1553bStruct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算结束X坐标
                    milSTD1553bStruct.EndX = (int) (milSTD1553bStruct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                    milSTD1553bStruct.DataColor = ErrorColor; // 设置数据颜色为错误色
                    milSTD1553bStruct.Data = "MANCH"; // 数据标记为曼彻斯特编码错误
                    list.add(milSTD1553bStruct); // 添加到列表
                }
                break;
            } // switch分支结束
        } // for循环结束

        //将最后一个添加上
        if (list.size() > 0 && milSTD1553bStruct != null) // 如果列表非空且结构体不为空
            if (milSTD1553bStruct.hashCode() != list.get(list.size() - 1).hashCode()) { // 如果最后一个结构体未被添加过
                milSTD1553bStruct.Data = "?"; // 数据标记为未知
//                milSTD1553bStruct.EndX = GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_YT) + 12;
                milSTD1553bStruct.EndX = this.endX - this.startX; // 结束位置设为波形区域宽度
                list.add(milSTD1553bStruct); // 添加到列表
            }
        return list; // 返回解析结果列表
    }

    /**
     * 绘制MIL-STD-1553B协议解码图像。
     * 先绘制中间基线，再逐个绘制解码元素，最后擦除标题栏区域。
     *
     * @param titleLen 标题栏宽度（像素）
     * @param canvas   Android画布
     * @param paint    画笔
     * @param list     1553B结构体列表，由getMilSTD1553bStruct解析得到
     * 业务意义：将1553B协议的各字段（远程地址/命令字/数据字）以图形方式呈现
     */
    private void drawParseMilSTD1553bData(int titleLen, Canvas canvas, Paint paint, List<SerialBusStruct.MilSTD1553bStruct> list) { // 绘制1553B协议解码图像
        paint.setXfermode(srcMode); // 设置画笔为源模式
        canvas.drawLine(this.startX, canvas.getHeight() / 2, this.endX, canvas.getHeight() / 2, paint); // 绘制中间基线
        for (int i = 0; i < list.size(); i++) { // 遍历所有解码元素
            //如果开始的时候不是从头开始的，以“-”线补满
//            if (i == 0 && list.get(i).BeginX > 0) {
//                drawElement(canvas, paint, 0, list.get(i).BeginX, null, list.get(i).DataColor);
//            }
//            //如果结束的时候没有到头，以“-”线补满
//            if (i == list.size() - 1 && list.get(i).EndX < canvas.getWidth()) {
//                drawElement(canvas, paint, list.get(i).EndX, canvas.getWidth() - list.get(i).EndX, null, list.get(i).DataColor);
//            }
//
//            //如果在中间的帧，以“-”线补满
//            if (i > 0 && i < list.size()) {
//                drawElement(canvas, paint, list.get(i - 1).EndX, list.get(i).BeginX - list.get(i - 1).EndX, null, 0);
//            }

            drawElement(canvas, paint, list.get(i).BeginX + this.startX, list.get(i).EndX - list.get(i).BeginX, list.get(i).Data, list.get(i).DataColor,list.get(i).isBeginFrame); // 绘制单个解码元素
        }
        paint.setXfermode(desoutMode); // 设置画笔为擦除模式
        rectTitleLen.set(0, 0, titleLen, canvas.getHeight()); // 设置标题栏矩形区域
        canvas.drawRect(rectTitleLen, paint); // 擦除标题栏区域
    }
    //endregion

    //region Arinc429 绘制

    /**
     * 解析ARINC429协议的二进制数据为Arinc429Struct结构体列表。
     * ARINC429协议帧结构（32bit）：
     * Label(8bit)→SDI(2bit)→Data(19bit或21bit或23bit)→SSM(2bit)→Parity(1bit)
     * 支持三种格式（Arinc429Type_1/2/3），区别在于SDI/SSM/Data的位宽分配
     *
     * @param bytes       FPGA上传的原始ByteBuffer数据
     * @param Arinc429Type ARINC429格式类型，取值范围：Arinc429Struct.Arinc429Type_1/2/3
     *                    Type_1：SDI(2bit)+Data(19bit)+SSM(2bit)
     *                    Type_2：Data(21bit)+SSM(2bit)
     *                    Type_3：Data(23bit)
     * @return 解析后的Arinc429Struct列表，每个元素代表ARINC429协议的一个字段段
     * 业务意义：将FPGA采集的ARINC429航空电子总线信号解码为可读的协议帧字段
     */
    private List<SerialBusStruct.Arinc429Struct> getArinc429Struct(ByteBuffer bytes, int Arinc429Type) { // 解析ARINC429协议数据
        final int LabelColor = Color.YELLOW; // Label字段颜色：黄色
        final int SDIColor = Color.BLUE; // SDI字段颜色：蓝色
        final int DataColor = App.get().getResources().getColor(R.color.textColor);//Color.WHITE; // Data字段颜色：白色
        final int ErrorColor = Color.RED; // 错误字段颜色：红色
        final int SSMColor = Color.GREEN; // SSM字段颜色：绿色

        long tmp = this.timeToPix;// 100000000l;  //时间转像素 // 时间到像素的转换因子
        int encoding = this.encoding;//ICharacterEncoding.Hex;  //也是界面传进来的 // 编码方式由界面传入


        List<SerialBusStruct.Arinc429Struct> list = new ArrayList<>(); // 创建ARINC429结构体列表
        SerialBusStruct.Arinc429Struct arinc429Struct = null; // 当前结构体引用
        for (int i = 0; i <= bytes.limit() - 8; i += 8) { // 每8字节为一组遍历数据
            //过采样处理
            if ((bytes.get(i + 3) & 0x80) == 0x80) { // 判断是否为过采样数据（最高位为1）
                arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct(); // 创建新的ARINC429结构体
                arinc429Struct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算结束X坐标
                arinc429Struct.EndX = (int) (arinc429Struct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                arinc429Struct.BeginX = arinc429Struct.EndX; // 起始位置等于结束位置
                arinc429Struct.Data = "?"; // 数据标记为未知
                arinc429Struct.isBeginFrame=true; // 标记为帧起始
                list.add(arinc429Struct); // 添加到列表
                continue; // 跳过后续处理
            }
            if (bytes.get(i+3)==SerialBusStruct.DataType_BeginData){ // 判断是否为帧起始数据类型
//                if (arinc429Struct!=null && arinc429Struct.DataType==SerialBusStruct.DataType_BeginData){
//                    arinc429Struct.EndX=arinc429Struct.BeginX;
//                    arinc429Struct.Data="?";
//                    list.add(arinc429Struct);
//                }
                arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct(); // 创建新的ARINC429结构体
                arinc429Struct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算起始X坐标
                arinc429Struct.BeginX = (int) (arinc429Struct.BeginX * Cycle / tmp); // 将时间戳转换为像素坐标
                arinc429Struct.DataColor = ErrorColor; // 设置数据颜色为错误色
                arinc429Struct.DataType=bytes.get(i+3); // 设置数据类型
                arinc429Struct.isBeginFrame=true; // 标记为帧起始
                continue; // 跳过后续处理
            }

            switch (bytes.get(i + 3) & 0x0F) { // 根据数据类型低4位进行分支处理
//                case SerialBusStruct.DataType_BeginData: {
//                    arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct();
//                    arinc429Struct.BeginX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4));
//                    arinc429Struct.BeginX = (int) (arinc429Struct.BeginX * Cycle / tmp);
//                    arinc429Struct.DataColor = ErrorColor;
//                }
//                break;
                case SerialBusStruct.Arinc429Struct.DataType_Label: { // Label字段类型
                    if (arinc429Struct == null) { // 如果结构体为空则创建
                        arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct(); // 创建新的ARINC429结构体
                        arinc429Struct.BeginX = 0; // 起始位置设为0
                        arinc429Struct.isBeginFrame=false; // 非帧起始
                    }
                    arinc429Struct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算结束X坐标
                    arinc429Struct.EndX = (int) (arinc429Struct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                    arinc429Struct.DataColor = LabelColor; // 设置数据颜色为Label颜色（黄色）
                    arinc429Struct.Data = getEncoding(ICharacterEncoding.Octal, bytes.get(i + 2) & 0x00FF, 3 * 4); // 以八进制编码Label字段 //String.format("%o", bytes.get(i + 2));
                    arinc429Struct.DataType = bytes.get(i + 3) &0x0F; // 设置数据类型为低4位
                    list.add(arinc429Struct); // 添加到列表

                    //为Data添加对像
                    arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct(); // 创建新结构体用于后续Data字段
                    arinc429Struct.BeginX = list.get(list.size() - 1).EndX; // 起始位置设为上一个元素的结束位置
                }
                break;
                case SerialBusStruct.Arinc429Struct.DataType_Data1: { // Data字段第1字节类型
                    if (arinc429Struct == null) { // 如果结构体为空则创建
                        arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct(); // 创建新的ARINC429结构体
                        arinc429Struct.BeginX = 0; // 起始位置设为0
                        arinc429Struct.isBeginFrame=false; // 非帧起始
                    }
                    arinc429Struct.Id = (0x000000FF & bytes.get(i + 2)); // 取Data1字节的低8位存入Id
                    arinc429Struct.DataColor = ErrorColor; // 设置数据颜色为错误色（Data1单独无法确定颜色）
                }
                break;
                case SerialBusStruct.Arinc429Struct.DataType_Data2: { // Data字段第2字节类型
                    if (arinc429Struct == null) { // 如果结构体为空则跳过
                        continue; // 跳过当前数据
                    }
                    arinc429Struct.Id = arinc429Struct.Id | ((0x000000FF & bytes.get(i + 2)) << 8); // 将Data2字节左移8位后拼接到Id
                    arinc429Struct.DataColor = ErrorColor; // 设置数据颜色为错误色（Data2单独无法确定颜色）
                }
                break;
                case SerialBusStruct.Arinc429Struct.DataType_Data3: { // Data字段第3字节类型
                    if (arinc429Struct == null) { // 如果结构体为空则创建并直接添加
                        arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct(); // 创建新的ARINC429结构体
                        arinc429Struct.BeginX = 0; // 起始位置设为0
                        arinc429Struct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算结束X坐标
                        arinc429Struct.EndX = (int) (arinc429Struct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                        arinc429Struct.Data = "?"; // 数据标记为未知
                        arinc429Struct.DataColor = DataColor; // 设置数据颜色
                        arinc429Struct.setDataFrameEnd(true); // 标记为数据帧结束
                        arinc429Struct.isBeginFrame=false; // 非帧起始
                        list.add(arinc429Struct); // 添加到列表
                    } else {
                        arinc429Struct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算结束X坐标
                        arinc429Struct.EndX = (int) (arinc429Struct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                        arinc429Struct.Id = arinc429Struct.Id | ((0x000000FF & bytes.get(i + 2)) << 16); // 将Data3字节左移16位后拼接到Id
                        arinc429Struct.DataColor = DataColor; // 设置数据颜色
                        arinc429Struct.setDataFrameEnd(true); // 标记为数据帧结束
                        list.add(arinc429Struct); // 添加到列表
                        //然后分析数据，赋值
                        //顺序 arinc429Struct.Id=data3+data2+data1
                        switch (Arinc429Type) { // 根据ARINC429格式类型进行分支处理
                            case SerialBusStruct.Arinc429Struct.Arinc429Type_1: { // Type1格式：SDI(2bit)+Data(19bit)+SSM(2bit)
                                String s = Integer.toBinaryString((arinc429Struct.Id & 0x03) | 0xFF0000); // 取低2位SDI并补齐位数
                                arinc429Struct.SDI = s.substring(s.length() - 2, s.length()) + " "; // 截取最后2位作为SDI字符串
                                s = Integer.toBinaryString((arinc429Struct.Id & 0x600000) >> 21 | 0xFF0000); // 取SSM位并右移21位后补齐位数
                                arinc429Struct.SSM = s.substring(s.length() - 2, s.length()) + " "; // 截取最后2位作为SSM字符串
                                int tem = (arinc429Struct.Id & 0xFC) >> 2; // 取Data低6位并右移2位
                                tem |= ((arinc429Struct.Id & 0xFF00) >> 2); // 拼接Data中间8位
                                tem |= (arinc429Struct.Id & 0x1F0000) >> 2; // 拼接Data高5位，共19位数据
                                if (encoding == ICharacterEncoding.Binary) { // 如果编码方式为二进制
                                    arinc429Struct.Data = getEncoding(encoding, tem, 19); // 以19位二进制编码数据
                                } else {
                                    arinc429Struct.Data = getEncoding(encoding, tem, 5 * 4); // 以20位十六进制编码数据 //String.format("%06x", tem).toUpperCase() + " ";
                                }
                                if ((arinc429Struct.Id & 0x800000) == 0x800000) { // 如果奇偶校验位为1（错误）
                                    arinc429Struct.SDIColor = ErrorColor; // SDI颜色设为错误色
                                    arinc429Struct.SSMColor = ErrorColor; // SSM颜色设为错误色
                                    arinc429Struct.DataColor = ErrorColor; // Data颜色设为错误色
                                } else {
                                    arinc429Struct.SDIColor = SDIColor; // SDI颜色设为蓝色
                                    arinc429Struct.SSMColor = SSMColor; // SSM颜色设为绿色
                                    arinc429Struct.DataColor = DataColor; // Data颜色设为白色
                                }
                            }
                            break;
                            case SerialBusStruct.Arinc429Struct.Arinc429Type_2: { // Type2格式：Data(21bit)+SSM(2bit)
                                //data3+data2+data1
                                String s = Integer.toBinaryString(((arinc429Struct.Id & 0x600000) >> 21) | 0xFF000000); // 取SSM位并右移21位后补齐位数
                                arinc429Struct.SSM = s.substring(s.length() - 2, s.length()) + " "; // 截取最后2位作为SSM字符串
                                int tem = arinc429Struct.Id & 0x1FFFFF; // 取低21位作为Data数据
                                if (encoding == ICharacterEncoding.Binary) { // 如果编码方式为二进制
                                    arinc429Struct.Data = getEncoding(encoding, tem, 21); // 以21位二进制编码数据
                                } else {
                                    arinc429Struct.Data = getEncoding(encoding, tem, 6 * 4); // 以24位十六进制编码数据 //String.format("%06x", tem).toUpperCase() + " ";
                                }
                                if ((arinc429Struct.Id & 0x800000) == 0x800000) { // 如果奇偶校验位为1（错误）
                                    arinc429Struct.SDIColor = ErrorColor; // SDI颜色设为错误色
                                    arinc429Struct.SSMColor = ErrorColor; // SSM颜色设为错误色
                                    arinc429Struct.DataColor = ErrorColor; // Data颜色设为错误色
                                } else {
                                    arinc429Struct.SDIColor = SDIColor; // SDI颜色设为蓝色
                                    arinc429Struct.SSMColor = SSMColor; // SSM颜色设为绿色
                                    arinc429Struct.DataColor = DataColor; // Data颜色设为白色
                                }
                            }
                            break;
                            case SerialBusStruct.Arinc429Struct.Arinc429Type_3: { // Type3格式：Data(23bit)，无SDI和SSM
                                //data3+data2+data1
                                int temId = (arinc429Struct.Id & 0x7FFFFF); // 取低23位作为Data数据
                                if (encoding == ICharacterEncoding.Binary) { // 如果编码方式为二进制
                                    arinc429Struct.Data = getEncoding(encoding, temId, 23); // 以23位二进制编码数据
                                } else {
                                    arinc429Struct.Data = getEncoding(encoding, temId, 6 * 4); // 以24位十六进制编码数据 //String.format("%06x", temId).toUpperCase();
                                }
                                if ((arinc429Struct.Id & 0x800000) == 0x800000) { // 如果奇偶校验位为1（错误）
                                    arinc429Struct.SDIColor = ErrorColor; // SDI颜色设为错误色
                                    arinc429Struct.SSMColor = ErrorColor; // SSM颜色设为错误色
                                    arinc429Struct.DataColor = ErrorColor; // Data颜色设为错误色
                                } else {
                                    arinc429Struct.SDIColor = SDIColor; // SDI颜色设为蓝色
                                    arinc429Struct.SSMColor = SSMColor; // SSM颜色设为绿色
                                    arinc429Struct.DataColor = DataColor; // Data颜色设为白色
                                }
                            }
                            break;
                        } // Arinc429Type分支结束

                    } // else分支结束
                }
                break;
                case SerialBusStruct.Arinc429Struct.DataType_Error: { // 错误类型
                    if (list.size() > 0 && list.get(list.size() - 1).isDataFrameEnd()) { // 如果上一帧数据已结束
                        arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct(); // 创建新的ARINC429结构体
                        arinc429Struct.BeginX = list.get(list.size() - 1).EndX; // 起始位置设为上一帧的结束位置
                    }
                    if (arinc429Struct == null) { // 如果结构体仍为空则创建
                        arinc429Struct = SerialBusStruct.getInstance().new Arinc429Struct(); // 创建新的ARINC429结构体
                        arinc429Struct.BeginX = 0; // 起始位置设为0
                        arinc429Struct.isBeginFrame=false; // 非帧起始
                    }


                    arinc429Struct.EndX = (0xff000000 & (bytes.get(i + 7) << 24)) | (0xff0000 & (bytes.get(i + 6) << 16)) | (0xff00 & (bytes.get(i + 5) << 8)) | (0xff & bytes.get(i + 4)); // 拼接4字节计算结束X坐标
                    arinc429Struct.EndX = (int) (arinc429Struct.EndX * Cycle / tmp); // 将时间戳转换为像素坐标
                    arinc429Struct.Data = "Err"; // 数据标记为错误
                    arinc429Struct.DataColor = ErrorColor; // 设置数据颜色为错误色
                    arinc429Struct.DataType = bytes.get(i + 3) &0x0F; // 设置数据类型为低4位
                    list.add(arinc429Struct); // 添加到列表

                }
                break;
            } // switch分支结束
        } // for循环结束

        //将最后一个添加上
        if (list.size() > 0 && arinc429Struct != null) // 如果列表非空且结构体不为空
            if (arinc429Struct.hashCode() != list.get(list.size() - 1).hashCode()) { // 如果最后一个结构体未被添加过
                arinc429Struct.Data = "?"; // 数据标记为未知
                arinc429Struct.DataColor = DataColor; // 设置数据颜色
//                arinc429Struct.EndX = GlobalVar.get().getWaveZoneWidth_Pix(IWorkMode.WorkMode_YT) + 12;
                arinc429Struct.EndX = this.endX - this.startX; // 结束位置设为波形区域宽度
                list.add(arinc429Struct); // 添加到列表
            }
        return list; // 返回解析结果列表
    }

    /**
     * 绘制ARINC429协议解码图像。
     * ARINC429的绘制较为特殊，需要分别以不同颜色绘制SDI/Data/SSM三个字段。
     * 先绘制中间基线，再逐个绘制解码元素，最后擦除标题栏区域。
     *
     * @param titleLen 标题栏宽度（像素）
     * @param canvas   Android画布
     * @param paint    画笔
     * @param list     ARINC429结构体列表，由getArinc429Struct解析得到
     * 业务意义：将ARINC429协议的各字段（Label/SDI/Data/SSM）以不同颜色图形方式呈现
     */
    private void drawParseArinc429Data(int titleLen, Canvas canvas, Paint paint, List<SerialBusStruct.Arinc429Struct> list) { // 绘制ARINC429协议解码图像
        paint.setXfermode(srcMode); // 设置画笔为源模式
        canvas.drawLine(this.startX, canvas.getHeight() / 2, this.endX, canvas.getHeight() / 2, paint); // 绘制中间基线
//        Logger.i(Command.TAG,"list size:"+list.size());
        for (int i = 0; i < list.size(); i++) { // 遍历所有解码元素
            //如果开始的时候不是从头开始的，以“-”线补满
//            if (i == 0 && list.get(i).BeginX > 0) {
//                drawElement(canvas, paint, 0, list.get(i).BeginX, null);
//            }
//            //如果结束的时候没有到头，以“-”线补满
//            if (i == list.size() - 1 && list.get(i).EndX < canvas.getWidth()) {
//                drawElement(canvas, paint, list.get(i).EndX, canvas.getWidth() - list.get(i).EndX, null);
//            }
//
//            //如果在中间的帧，以“-”线补满
//            if (i > 0 && i < list.size()) {
//                drawElement(canvas, paint, list.get(i - 1).EndX, list.get(i).BeginX - list.get(i - 1).EndX, null);
//            }

//            Logger.i(Command.TAG,"index to string:"+list.get(i).toString());
            drawElement(canvas, paint, list.get(i).BeginX + this.startX, list.get(i).EndX - list.get(i).BeginX, list.get(i)); // 绘制单个ARINC429解码元素（含SDI/Data/SSM多色绘制）
        }
        paint.setXfermode(desoutMode); // 设置画笔为擦除模式
        rectTitleLen.set(0, 0, titleLen, canvas.getHeight()); // 设置标题栏矩形区域
        canvas.drawRect(rectTitleLen, paint); // 擦除标题栏区域
    }

    //endregion


    //region 绘制<==>  公有函数

    /**
     * 绘制单个解码元素（菱形边框+文字）。
     * 根据元素宽度决定绘制方式：
     * - value=null：绘制水平线（帧间隔）
     * - value="?"或非帧起始：绘制简化菱形+文字
     * - width < mid*2：仅绘制竖线（空间不足）
     * - width >= mid*2：绘制完整菱形边框+文字
     * 使用PorterDuffXfermode(DST_OUT)擦除元素区域后再绘制，实现透明背景效果
     *
     * @param canvas       Android画布
     * @param paint        画笔
     * @param beginX       元素起始X坐标（像素）
     * @param width        元素宽度（像素）
     * @param value        显示值，null表示绘制线，"?"表示未知数据
     * @param textColor    文字颜色
     * @param isBeginFrame 是否为帧起始元素，影响菱形绘制方式
     * 业务意义：所有协议解码图像的基本绘制单元，菱形边框内显示解码数据
     */
    private  void drawElement(Canvas canvas, Paint paint, int beginX, int width, String value, int textColor,boolean isBeginFrame) { // 绘制单个解码元素（菱形边框+文字）
        final int ShowPixReVise = 2; // 像素修正值
        final int ShowTxtReVise = 6; // 文字修正值
        int mid = canvas.getHeight() / 2; // 画布中间Y坐标
        int color = paint.getColor(); // 保存当前画笔颜色
        if (value == null) { // 值为null时绘制横线
            //画线 -
            canvas.drawLine(beginX, mid, beginX + width, mid, paint); // 绘制水平横线
        } else if ("?".equals(value) || !isBeginFrame) { // 值为"?"或非帧起始元素时进入此分支

            Rect rect = Tools.getTextRect(value, paint); // 获取文字的边界矩形
            //int top = (canvas.getHeight() - rect.height()) / 2 + rect.height()-1;
            paint.setStyle(Paint.Style.FILL); // 设置画笔为填充模式
            paint.setTextAlign(Paint.Align.CENTER); // 设置文字居中对齐
            Paint.FontMetrics fontMetrics= paint.getFontMetrics(); // 获取字体度量信息
            float fTop=fontMetrics.top; // 字体顶部偏移
            float fBottom=fontMetrics.bottom; // 字体底部偏移

            int top=(int)(canvas.getHeight()/2-fTop/2-fBottom/2); // 计算文字垂直居中的Y坐标
            int left = (width - mid - rect.width()) / 2 + mid; // 计算文字水平起始位置

            if ((left + beginX + rect.width() > beginX + width) || ((beginX + mid) > (beginX + width - mid)) || ((beginX + mid) > (beginX + width))) { // 判断文字是否超出可用宽度
                paint.setXfermode(desoutMode); // 设置混合模式为清除目标区域
                rectTitleLen.set(beginX, 0, beginX, canvas.getHeight()); // 设置清除区域为竖线范围
                canvas.drawRect(rectTitleLen, paint); // 清除竖线区域
                paint.setXfermode(srcMode); // 恢复正常绘制模式

                canvas.drawLine(beginX, 0, beginX, canvas.getHeight(), paint); // 绘制竖线分隔符
            } else { // 文字未超出宽度，正常绘制菱形边框
                paint.setXfermode(desoutMode); // 设置混合模式为清除目标区域
                rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight()); // 设置清除区域为整个元素范围
                canvas.drawRect(rectTitleLen, paint); // 清除元素区域背景
                paint.setXfermode(srcMode); // 恢复正常绘制模式

                if (this.startX != beginX ) { // 判断是否为帧起始位置
                    // 画<
                    canvas.drawLine(beginX, mid, beginX + mid, 0, paint); // 绘制左上斜线
                    canvas.drawLine(beginX, mid + 1, beginX + mid, canvas.getHeight(), paint); // 绘制左下斜线
                } // 结束帧起始判断
                if ((beginX + width - mid) < (this.endX - mid)) { // 判断右侧是否不是帧末尾
                    // 画>
                    canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint); // 绘制右上斜线
                    canvas.drawLine(beginX + width, mid + 1, beginX + width - mid, canvas.getHeight(), paint); // 绘制右下斜线
                    //上下-
                    canvas.drawLine(beginX + mid, 1, beginX + width - mid, 1, paint); // 绘制上边横线
                    canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width - mid, canvas.getHeight() - 1, paint); // 绘制下边横线
                } else { // 右侧是帧末尾，横线延伸到右边界
                    //上下-
                    canvas.drawLine(beginX + mid, 1, beginX + width, 1, paint); // 绘制上边横线到右边界
                    canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width, canvas.getHeight() - 1, paint); // 绘制下边横线到右边界
                } // 结束右侧判断
                paint.setColor(textColor); // 设置文字颜色
                canvas.drawText(value, beginX + width / 2, top, paint); // 居中绘制文字
            } // 结束文字未超出分支
        } else if (width < mid * 2) { // 宽度不足绘制菱形，只画竖线
            //画 |  且补上空白的后线
            canvas.drawLine(beginX, 0, beginX, canvas.getHeight(), paint); // 绘制竖线分隔符
        } else if (width >= mid * 2) { // 宽度足够绘制完整菱形边框
            paint.setXfermode(desoutMode); // 设置混合模式为清除目标区域
            rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight()); // 设置清除区域为整个元素范围
            canvas.drawRect(rectTitleLen, paint); // 清除元素区域背景
            paint.setXfermode(srcMode); // 恢复正常绘制模式
            // 画<>
            // 画<
            canvas.drawLine(beginX, mid, beginX + mid, 0, paint); // 绘制左上斜线
            canvas.drawLine(beginX, mid + 1, beginX + mid, canvas.getHeight(), paint); // 绘制左下斜线
            // 画>
            canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint); // 绘制右上斜线
            canvas.drawLine(beginX + width, mid + 1, beginX + width - mid, canvas.getHeight(), paint); // 绘制右下斜线
            //上下-
            canvas.drawLine(beginX + mid, 1, beginX + width - mid, 1, paint); // 绘制上边横线
            canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width - mid, canvas.getHeight() - 1, paint); // 绘制下边横线
            //写字
            Rect rect = Tools.getTextRect(value, paint); // 获取文字边界矩形
            int top = (canvas.getHeight() - rect.height()) / 2 + rect.height(); // 计算文字垂直居中基线
            int left = (width - mid * 2 - rect.width()) / 2 + mid; // 计算文字水平起始位置
            paint.setColor(textColor); // 设置文字颜色
            if (left < mid / 2) { // 空间不足显示完整文字，降级显示"?"
                rect = Tools.getTextRect("?", paint); // 获取"?"的边界矩形
                left = (width - mid * 2 - rect.width()) / 2 + mid; // 重新计算"?"的水平位置
//                canvas.drawText("?", left + beginX, top, paint);
                canvas.drawText("?", beginX + width / 2, top, paint); // 居中绘制"?"
            } else { // 空间足够，正常绘制文字
//                canvas.drawText(value, left + beginX, top, paint);
                canvas.drawText(value, beginX + width / 2, top, paint); // 居中绘制完整值
            } // 结束文字绘制判断
        } // 结束宽度判断

        paint.setColor(color); // 恢复原始画笔颜色
    } // 结束drawElement方法

    /**
     * 绘制单个解码元素（菱形边框+文字），支持短格式数据。
     * 当空间不足显示完整value时，尝试显示ShortValue（不含前缀的简短数据）。
     * 绘制逻辑与drawElement(6参数)相同，增加了短格式数据的降级显示。
     *
     * @param canvas     Android画布
     * @param paint      画笔
     * @param beginX     元素起始X坐标（像素）
     * @param width      元素宽度（像素）
     * @param value      完整显示值（如"W:FF[A]"）
     * @param ShortValue 简写显示值（如"FF"），空间不足时使用
     * @param textColor  文字颜色
     * @param isBeginFrame 是否为帧起始元素
     * 业务意义：I2C协议专用，W:/R:/D:前缀占用空间，短格式保证关键数据可见
     */
    private  void drawElement(Canvas canvas, Paint paint, int beginX, int width, String value, String ShortValue, int textColor,boolean isBeginFrame) { // 带短格式值的绘制方法，I2C专用
        final int ShowPixReVise = 2; // 像素修正值
        final int ShowTxtReVise = 6; // 文字修正值
        int mid = canvas.getHeight() / 2; // 计算画布垂直中心位置
        int color = paint.getColor(); // 保存当前画笔颜色
        if (value == null) { // 值为null时绘制横线
            //画线 -
            canvas.drawLine(beginX, mid, beginX + width, mid, paint); // 绘制水平横线
        } // 结束null判断
        else if (value.equals("?") || !isBeginFrame) { // 值为"?"或非帧起始元素
            Rect rect = Tools.getTextRect(value, paint); // 获取文字边界矩形
//            int top = (canvas.getHeight() - rect.height()) / 2 + rect.height();
            paint.setStyle(Paint.Style.FILL); // 设置画笔为填充模式
            paint.setTextAlign(Paint.Align.CENTER); // 设置文字居中对齐
            Paint.FontMetrics fontMetrics= paint.getFontMetrics(); // 获取字体度量信息
            float fTop=fontMetrics.top; // 字体顶部偏移
            float fBottom=fontMetrics.bottom; // 字体底部偏移

            int top=(int)(canvas.getHeight()/2-fTop/2-fBottom/2); // 计算文字垂直居中的Y坐标
            int left = (width - mid - rect.width()) / 2 + mid; // 计算文字水平起始位置

            if ((left + beginX + rect.width() > beginX + width) || ((beginX + mid) > (beginX + width - mid)) || ((beginX + mid) > (beginX + width))) { // 判断文字是否超出可用宽度
                paint.setXfermode(desoutMode); // 设置混合模式为清除目标区域
                rectTitleLen.set(beginX, 0, beginX, canvas.getHeight()); // 设置清除区域为竖线范围
                canvas.drawRect(rectTitleLen, paint); // 清除竖线区域
                paint.setXfermode(srcMode); // 恢复正常绘制模式

                canvas.drawLine(beginX, 0, beginX, canvas.getHeight(), paint); // 绘制竖线分隔符
            } else { // 文字未超出宽度，正常绘制菱形边框
                paint.setXfermode(desoutMode); // 设置混合模式为清除目标区域
                rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight()); // 设置清除区域为整个元素范围
                canvas.drawRect(rectTitleLen, paint); // 清除元素区域背景
                paint.setXfermode(srcMode); // 恢复正常绘制模式

                if (this.startX != beginX ) { // 判断是否为帧起始位置
                    // 画<
                    canvas.drawLine(beginX, mid, beginX + mid, 0, paint); // 绘制左上斜线
                    canvas.drawLine(beginX, mid + 1, beginX + mid, canvas.getHeight(), paint); // 绘制左下斜线
                } // 结束帧起始判断
                if ((beginX + width - mid) < (this.endX - mid)) { // 判断右侧是否不是帧末尾
                    // 画>
                    canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint); // 绘制右上斜线
                    canvas.drawLine(beginX + width, mid + 1, beginX + width - mid, canvas.getHeight(), paint); // 绘制右下斜线
                    //上下-
                    canvas.drawLine(beginX + mid, 1, beginX + width - mid, 1, paint); // 绘制上边横线
                    canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width - mid, canvas.getHeight() - 1, paint); // 绘制下边横线
                } else { // 右侧是帧末尾，横线延伸到右边界
                    //上下-
                    canvas.drawLine(beginX + mid, 1, beginX + width, 1, paint); // 绘制上边横线到右边界
                    canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width, canvas.getHeight() - 1, paint); // 绘制下边横线到右边界
                } // 结束右侧判断
                paint.setColor(textColor); // 设置文字颜色
//                canvas.drawText("?", left + beginX, top, paint);
                canvas.drawText(value, beginX + width / 2, top, paint); // 居中绘制文字
            } // 结束文字未超出分支
        } else if (width < mid * 2) { // 宽度不足绘制菱形，只画竖线
//            Rect rect = Tools.getTextRect("?", paint);
//            int top = (canvas.getHeight() - rect.height()) / 2 + rect.height();
//            int left = (width - mid * 2 - rect.width()) / 2 + mid;
//            if (left < mid/2){
            //画 |  且补上空白的后线
            canvas.drawLine(beginX, 0, beginX, canvas.getHeight(), paint); // 绘制竖线分隔符
//                canvas.drawLine(beginX, mid, beginX + width, mid, paint);
//            }
//            else {
//                paint.setXfermode(desoutMode);
//                rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight());
//                canvas.drawRect(rectTitleLen, paint);
//                paint.setXfermode(srcMode);
//                // 画<>
//                // 画<
//                canvas.drawLine(beginX, mid, beginX + mid, 0, paint);
//                canvas.drawLine(beginX, mid, beginX + mid, canvas.getHeight() , paint);
//                // 画>
//                canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint);
//                canvas.drawLine(beginX + width, mid, beginX + width - mid, canvas.getHeight() , paint);
//                //上下-
//                canvas.drawLine(beginX + mid, 0, beginX + width - mid, 0, paint);
//                canvas.drawLine(beginX + mid, canvas.getHeight()-1 , beginX + width - mid, canvas.getHeight()-1 , paint);
//
//                rect = Tools.getTextRect("?", paint);
//                left = (width - mid * 2 - rect.width()) / 2 + mid;
//                paint.setColor(textColor);
//                canvas.drawText("?", left + beginX, top, paint);
//
//            }
        } else if (width >= mid * 2) { // 宽度足够绘制完整菱形边框
            paint.setXfermode(desoutMode); // 设置混合模式为清除目标区域
            rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight()); // 设置清除区域为整个元素范围
            canvas.drawRect(rectTitleLen, paint); // 清除元素区域背景
            paint.setXfermode(srcMode); // 恢复正常绘制模式
            // 画<>
            // 画<
            canvas.drawLine(beginX, mid, beginX + mid, 0, paint); // 绘制左上斜线
            canvas.drawLine(beginX, mid + 1, beginX + mid, canvas.getHeight(), paint); // 绘制左下斜线
            // 画>
            canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint); // 绘制右上斜线
            canvas.drawLine(beginX + width, mid + 1, beginX + width - mid, canvas.getHeight(), paint); // 绘制右下斜线
            //上下-
            canvas.drawLine(beginX + mid, 1, beginX + width - mid, 1, paint); // 绘制上边横线
            canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width - mid, canvas.getHeight() - 1, paint); // 绘制下边横线
            //写字
            Rect rect = Tools.getTextRect(value, paint); // 获取完整值的文字边界矩形
            int top = (canvas.getHeight() - rect.height()) / 2 + rect.height()-2; // 计算文字垂直居中基线（微调-2像素）
            int left = (width - mid * 2 - rect.width()) / 2 + mid; // 计算完整值的水平起始位置

            Rect shortRect = Tools.getTextRect(ShortValue, paint); // 获取短格式值的文字边界矩形
            int shortTop = (canvas.getHeight() - shortRect.height()) / 2 + shortRect.height(); // 计算短格式值垂直居中基线
            int shortLeft = (width - mid * 2 - shortRect.width()) / 2 + mid; // 计算短格式值的水平起始位置

            paint.setColor(textColor); // 设置文字颜色
            if (left > mid / 2) { // 空间足够显示完整值
//                canvas.drawText(value, left + beginX, top, paint);
                canvas.drawText(value, beginX + width / 2, top, paint); // 居中绘制完整值
            } else if (shortLeft > mid / 2) { // 空间不足显示完整值，但够显示短格式值
                Paint.Align al = paint.getTextAlign(); // 保存当前文字对齐方式
                paint.setTextAlign(Paint.Align.LEFT); // 设置左对齐绘制短格式值
                canvas.drawText(ShortValue, shortLeft + beginX, shortTop, paint); // 绘制短格式值
                paint.setTextAlign(al); // 恢复原始文字对齐方式
            } else { // 空间连短格式值都不够，降级显示"?"
                rect = Tools.getTextRect("?", paint); // 获取"?"的边界矩形
                left = (width - mid * 2 - rect.width()) / 2 + mid; // 重新计算"?"的水平位置
//                canvas.drawText("?", left + beginX, top, paint);
                canvas.drawText("?", beginX + width / 2, top, paint); // 居中绘制"?"
            } // 结束文字绘制判断
        } // 结束宽度判断

        paint.setColor(color); // 恢复原始画笔颜色
    } // 结束drawElement方法（带ShortValue）

    /**
     * 绘制单个ARINC429解码元素（菱形边框+多色文字）。
     * ARINC429协议需要分别以不同颜色显示SDI（蓝色）、Data（白色/红色）、SSM（绿色/红色），
     * 当Parity位(bit23)=1时所有字段显示红色表示校验错误。
     * 空间不足时降级显示"?"。
     *
     * @param canvas          Android画布
     * @param paint           画笔
     * @param beginX          元素起始X坐标（像素）
     * @param width           元素宽度（像素）
     * @param arinc429Struct  ARINC429结构体，包含SDI/Data/SSM及各自颜色
     * 业务意义：ARINC429协议专用，多色分段显示让用户快速识别各字段及校验状态
     */
    private  void drawElement(Canvas canvas, Paint paint, int beginX, int width, SerialBusStruct.Arinc429Struct arinc429Struct) { // ARINC429专用多色绘制方法
        final int ShowPixReVise = 2; // 像素修正值
        final int ShowTxtReVise = 6; // 文字修正值

        int mid = canvas.getHeight() / 2; // 计算画布垂直中心位置
        int color = paint.getColor(); // 保存当前画笔颜色
        if (arinc429Struct == null) { // 结构体为null时绘制横线
            //画线 -
            canvas.drawLine(beginX, mid, beginX + width, mid, paint); // 绘制水平横线
        } else if (arinc429Struct.Data.equals("?") /*|| !arinc429Struct.isBeginFrame*/) { // Data为"?"或非帧起始

            Rect rect = Tools.getTextRect(arinc429Struct.Data, paint); // 获取Data文字边界矩形
//            int top = (canvas.getHeight() - rect.height()) / 2 + rect.height();
            paint.setStyle(Paint.Style.FILL); // 设置画笔为填充模式
            paint.setTextAlign(Paint.Align.CENTER); // 设置文字居中对齐
            Paint.FontMetrics fontMetrics= paint.getFontMetrics(); // 获取字体度量信息
            float fTop=fontMetrics.top; // 字体顶部偏移
            float fBottom=fontMetrics.bottom; // 字体底部偏移

            int top=(int)(canvas.getHeight()/2-fTop/2-fBottom/2); // 计算文字垂直居中的Y坐标
            int left = (width - mid - rect.width()) / 2 + mid; // 计算文字水平起始位置
            if ((left + beginX + rect.width() > beginX + width) || ((beginX + mid) > (beginX + width - mid)) || ((beginX + mid) > (beginX + width))) { // 判断文字是否超出可用宽度
                paint.setXfermode(desoutMode); // 设置混合模式为清除目标区域
                rectTitleLen.set(beginX, 0, beginX, canvas.getHeight()); // 设置清除区域为竖线范围
                canvas.drawRect(rectTitleLen, paint); // 清除竖线区域
                paint.setXfermode(srcMode); // 恢复正常绘制模式

                canvas.drawLine(beginX, 0, beginX, canvas.getHeight(), paint); // 绘制竖线分隔符
            } else { // 文字未超出宽度，正常绘制菱形边框
                paint.setXfermode(desoutMode); // 设置混合模式为清除目标区域
                rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight()); // 设置清除区域为整个元素范围
                canvas.drawRect(rectTitleLen, paint); // 清除元素区域背景
                paint.setXfermode(srcMode); // 恢复正常绘制模式

                if (this.startX != beginX ) { // 判断是否为帧起始位置
                    // 画<
                    canvas.drawLine(beginX, mid, beginX + mid, 0, paint); // 绘制左上斜线
                    canvas.drawLine(beginX, mid + 1, beginX + mid, canvas.getHeight(), paint); // 绘制左下斜线
                } // 结束帧起始判断
                if ((beginX + width - mid) < (this.endX - mid)) { // 判断右侧是否不是帧末尾
                    // 画>
                    canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint); // 绘制右上斜线
                    canvas.drawLine(beginX + width, mid + 1, beginX + width - mid, canvas.getHeight(), paint); // 绘制右下斜线
                    //上下-
                    canvas.drawLine(beginX + mid, 1, beginX + width - mid, 1, paint); // 绘制上边横线
                    canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width - mid, canvas.getHeight() - 1, paint); // 绘制下边横线
                } else { // 右侧是帧末尾，横线延伸到右边界
                    //上下-
                    canvas.drawLine(beginX + mid, 1, beginX + width, 1, paint); // 绘制上边横线到右边界
                    canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width, canvas.getHeight() - 1, paint); // 绘制下边横线到右边界
                } // 结束右侧判断
                paint.setColor(arinc429Struct.DataColor); // 设置Data字段颜色
//                canvas.drawText("?", left + beginX, top, paint);
                canvas.drawText(arinc429Struct.Data, beginX + width / 2, top, paint); // 居中绘制Data文字
            } // 结束文字未超出分支
        } else if (width < mid * 2) { // 宽度不足绘制菱形，只画竖线
            //画 |  且补上空白的后线
//            canvas.drawLine(beginX, 0, beginX, canvas.getHeight(), paint);
//            canvas.drawLine(beginX, mid, beginX + width, mid, paint);
//            Rect rect = Tools.getTextRect("?", paint);
//            int top = (canvas.getHeight() - rect.height()) / 2 + rect.height();
//            int left = (width - mid * 2 - rect.width()) / 2 + mid;
//            if (left < mid/2){
            //画 |  且补上空白的后线
            canvas.drawLine(beginX, 0, beginX, canvas.getHeight(), paint); // 绘制竖线分隔符
//                canvas.drawLine(beginX, mid, beginX + width, mid, paint);
//            }
//            else {
//                paint.setXfermode(desoutMode);
//                rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight());
//                canvas.drawRect(rectTitleLen, paint);
//                paint.setXfermode(srcMode);
//                // 画<>
//                // 画<
//                canvas.drawLine(beginX, mid, beginX + mid, 0, paint);
//                canvas.drawLine(beginX, mid, beginX + mid, canvas.getHeight() , paint);
//                // 画>
//                canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint);
//                canvas.drawLine(beginX + width, mid, beginX + width - mid, canvas.getHeight() , paint);
//                //上下-
//                canvas.drawLine(beginX + mid, 0, beginX + width - mid, 0, paint);
//                canvas.drawLine(beginX + mid, canvas.getHeight()-1 , beginX + width - mid, canvas.getHeight()-1 , paint);
//
//                rect = Tools.getTextRect("?", paint);
//                left = (width - mid * 2 - rect.width()) / 2 + mid;
//                paint.setColor(arinc429Struct.DataColor);
//                canvas.drawText("?", left + beginX, top, paint);
//
//            }
        } else if (width >= mid * 2) { // 宽度足够绘制完整菱形边框
            paint.setXfermode(desoutMode); // 设置混合模式为清除目标区域
            rectTitleLen.set(beginX, 0, beginX + width, canvas.getHeight()); // 设置清除区域为整个元素范围
            canvas.drawRect(rectTitleLen, paint); // 清除元素区域背景
            paint.setXfermode(srcMode); // 恢复正常绘制模式
            // 画<>
            // 画<
            canvas.drawLine(beginX, mid, beginX + mid, 0, paint); // 绘制左上斜线
            canvas.drawLine(beginX, mid + 1, beginX + mid, canvas.getHeight(), paint); // 绘制左下斜线
            // 画>
            canvas.drawLine(beginX + width, mid, beginX + width - mid, 0, paint); // 绘制右上斜线
            canvas.drawLine(beginX + width, mid + 1, beginX + width - mid, canvas.getHeight(), paint); // 绘制右下斜线
            //上下-
            canvas.drawLine(beginX + mid, 1, beginX + width - mid, 1, paint); // 绘制上边横线
            canvas.drawLine(beginX + mid, canvas.getHeight() - 1, beginX + width - mid, canvas.getHeight() - 1, paint); // 绘制下边横线
            //写字
            String value = ""; // 拼接所有字段的完整文字
            int spaceWidth = 10; // 字段之间的间距宽度
            int times = 0; // 非Data字段的数量（用于计算间距）
            if (arinc429Struct.SDI != null) { // SDI字段非空
                value = arinc429Struct.SDI; // 赋值SDI
                times++; // 计数加1
            } // 结束SDI判断
            if (arinc429Struct.SSM != null) { // SSM字段非空
                value += arinc429Struct.SSM; // 拼接SSM
                times++; // 计数加1
            } // 结束SSM判断
            if (arinc429Struct.Data != null) { // Data字段非空
                value += arinc429Struct.Data; // 拼接Data
            } // 结束Data判断

            Rect rect = Tools.getTextRect(value, paint); // 获取拼接文字的边界矩形
            rect.set(rect.left, rect.top, rect.right + spaceWidth * times, rect.bottom); // 扩展矩形宽度以容纳字段间距
            int top = (canvas.getHeight() - rect.height()) / 2 + rect.height(); // 计算文字垂直居中基线
            int left = (width - mid * 2 - rect.width()) / 2 + mid; // 计算文字水平起始位置
            //paint.setColor(textColor);

            Paint.Align al = paint.getTextAlign(); // 保存当前文字对齐方式
            paint.setTextAlign(Paint.Align.LEFT); // 设置左对齐以逐段绘制多色文字
            if (left < mid / 2) { // 空间不足分段显示，降级显示"?"
                rect = Tools.getTextRect("?", paint); // 获取"?"的边界矩形
                left = (width - mid * 2 - rect.width()) / 2 + mid; // 重新计算"?"的水平位置
                paint.setColor(arinc429Struct.DataColor); // 使用Data颜色绘制"?"
                canvas.drawText("?", left + beginX, top, paint); // 绘制"?"
            } else { // 空间足够，分段绘制多色文字
                int temX = left + beginX; // 当前绘制X坐标
                if (arinc429Struct.SDI != null) { // 绘制SDI字段
                    value = arinc429Struct.SDI; // 获取SDI值
                    paint.setColor(arinc429Struct.SDIColor); // 设置SDI颜色（蓝色）
                    canvas.drawText(value, temX, top, paint); // 绘制SDI文字
                    temX += Tools.getTextRect(value, paint).width() + spaceWidth; // 移动X坐标到下一个字段位置
                } // 结束SDI绘制
                if (arinc429Struct.Data != null) { // 绘制Data字段
                    value = arinc429Struct.Data; // 获取Data值
                    paint.setColor(arinc429Struct.DataColor); // 设置Data颜色（白色/红色）
                    canvas.drawText(value, temX, top, paint); // 绘制Data文字
                    temX += Tools.getTextRect(value, paint).width() + spaceWidth; // 移动X坐标到下一个字段位置
                } // 结束Data绘制
                if (arinc429Struct.SSM != null) { // 绘制SSM字段
                    value = arinc429Struct.SSM; // 获取SSM值
                    paint.setColor(arinc429Struct.SSMColor); // 设置SSM颜色（绿色/红色）
                    canvas.drawText(value, temX, top, paint); // 绘制SSM文字
//                    temX += Tools.getTextRect(value, paint).width() + 10;
                } // 结束SSM绘制
            } // 结束空间判断
            paint.setTextAlign(al); // 恢复原始文字对齐方式
        } // 结束宽度判断

        paint.setColor(color); // 恢复原始画笔颜色
    } // 结束drawElement方法（ARINC429）




    /**
     * 获取扩展ASCII字符。
     * 对于0xA1~0xFF范围内的ID值，尝试映射为扩展ASCII字符（0xAD除外返回空字符）。
     *
     * @param id 字符编码值，取值范围：0x00~0xFF
     * @return 对应的扩展ASCII字符，不在范围内或0xAD返回'\0'
     * 业务意义：UART的ASCII编码模式下，支持显示扩展ASCII字符（如Latin-1补充字符）
     */
    static char getExtASCII(int id){ // 获取扩展ASCII字符
        char val = '\0'; // 默认返回空字符
        if(id>= 0xA1 && id <= 0xFF){ // 判断是否在扩展ASCII范围内
            switch (id){ // 根据id值分支处理
                case 0xAD: // 0xAD为软连字符，不显示
                    val = '\0'; // 返回空字符
                    break; // 跳出switch
                default: // 其他扩展ASCII字符
                    val = (char)id; // 直接转换为字符
                    break; // 跳出switch
            } // 结束switch
        } // 结束范围判断
        return val; // 返回结果字符
    } // 结束getExtASCII方法

    /**
     * 整型数据按指定编码方式转换为字符串。
     * 核心编码转换方法，被所有协议解析方法调用，将二进制数据转换为用户可读的字符串。
     * 支持5种编码方式：Hex（十六进制）、Binary（二进制）、Octal（八进制）、Decimal（十进制）、ASCII。
     *
     * 算法：通过 id | (1L << 63) 将数据扩展到64位Long的高位，然后使用Long的转换方法
     * 生成完整字符串，最后截取后width个字符，实现固定位宽输出。
     *
     * @param Coding   编码方式，取值范围：ICharacterEncoding常量
     *                 （Hex=0x16, Binary=0x02, Octal=0x08, Decimal=0x10, ASCII=0xAC）
     * @param id       待转换的整型数据，取值范围：0x00~0xFFFFFF（最大24位）
     * @param bitWidth 显示位宽（位数），影响输出字符串长度
     *                 Hex: width = ceil(bitWidth/4)，如8位→2字符，11位→3字符
     *                 Binary: width = bitWidth，如8位→8字符
     *                 Octal: width = ceil(bitWidth/4)
     *                 Decimal: width = bitWidth
     *                 ASCII: width = 1（可显示字符）或3（不可显示字符显示"..."）
     * @return 编码后的字符串，Hex/Octal/Decimal模式为大写
     * 业务意义：用户可在UI界面切换编码方式，此方法统一处理所有协议的数据显示格式
     */
    private static String getEncoding(int Coding, int id, int bitWidth) { // 编码转换核心方法
        String s = ""; // 转换后的字符串
        int width = 1; // 输出位宽，默认1
        Long data = id | (1L << 63); // 将id扩展到64位Long，高位设1保证转换后字符串长度固定
        switch (Coding) { // 根据编码方式分支处理
            case ICharacterEncoding.ASCII: { // ASCII编码模式
//                char s1 = ((id >= 48 && id <= 57) || (id >= 65 && id <= 90) || (id >= 97 && id <= 122)) ? (char) id : '\0';
                char s1 = ((id >= 32) && (id <= 126)) ? (char) id : '\0'; // 判断是否为可显示ASCII字符（32~126）
                if(id >= 0xA1){ // 判断是否在扩展ASCII范围内
                    s1 = getExtASCII(id); // 获取扩展ASCII字符
                } // 结束扩展ASCII判断
                if (s1 == '\0') { // 不可显示字符
                    s = "..........."; // 用省略号占位
                    width = 3; // 占3个字符宽度
                } else { // 可显示字符
                    s = String.valueOf(s1); // 转换为字符串
                    width = 1; // 占1个字符宽度
                } // 结束可显示判断
            } // 结束ASCII分支
            break; // 跳出switch
            case ICharacterEncoding.Binary: { // 二进制编码模式
                s = Long.toBinaryString(data); // 转换为二进制字符串
                width = bitWidth; // 位宽等于数据位宽
            } // 结束Binary分支
            break; // 跳出switch
            case ICharacterEncoding.Decimal: { // 十进制编码模式
                s = Long.toString(data); // 转换为十进制字符串
                width = bitWidth; // 位宽等于数据位宽
            } // 结束Decimal分支
            break; // 跳出switch
            case ICharacterEncoding.Hex: { // 十六进制编码模式
                s = Long.toHexString(data); // 转换为十六进制字符串
                width = (int) Math.ceil(bitWidth / 4.0f); // 位宽=数据位宽/4向上取整
            } // 结束Hex分支
            break; // 跳出switch
            case ICharacterEncoding.Octal: { // 八进制编码模式
                s = Long.toOctalString(data); // 转换为八进制字符串
                width = (int) Math.ceil(bitWidth / 4.0f); // 位宽=数据位宽/4向上取整
            } // 结束Octal分支
            break; // 跳出switch
        } // 结束switch
        if (Coding==ICharacterEncoding.ASCII){ // ASCII模式直接截取
            return s.substring(s.length() - width, s.length()); // 截取后width个字符
        } // 结束ASCII判断
        else { // 非ASCII模式，转大写后截取
            //取字串s的后width个字符输出
            return s.toUpperCase().substring(s.length() - width, s.length()); // 转大写并截取后width个字符
        } // 结束非ASCII判断
    } // 结束getEncoding方法
    //endregion

    public static void main(String[] arg){ // 主方法入口（测试用）

    } // 结束main方法
}
