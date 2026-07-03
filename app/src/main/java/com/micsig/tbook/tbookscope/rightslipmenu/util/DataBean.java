package com.micsig.tbook.tbookscope.rightslipmenu.util; // 数据Bean所属工具子包


import java.io.Serializable;   // 可序列化接口，支持对象持久化与传输
import java.math.BigDecimal;    // 高精度十进制运算，用于浮点数四舍五入
import java.util.Locale;        // 区域设置，用于格式化输出

/**
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │                         DataBean 数据传输对象                        │
 * ├──────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】右侧滑菜单(rightslipmenu)工具子包(util)中的数据载体类      │
 * │ 【核心职责】封装示波器测量数据的六个维度字段，支持序列化、CSV导出与       │
 * │            线性校准计算                                                │
 * │ 【架构设计】标准JavaBean模式，字段通过混淆命名(G/b/c/d/e/f)保护业务      │
 * │            含义；实现Serializable以支持跨组件/进程数据传递              │
 * │ 【数据流向】文件读取(UtilFile) → DataBean列表 → UI展示/图表绘制        │
 * │ 【依赖关系】依赖 java.io.Serializable、java.math.BigDecimal、         │
 * │            java.util.Locale；被 UtilFile 读取并实例化                 │
 * │ 【使用场景】存储单条示波器测量记录，批量加载后供右滑菜单数据列表展示     │
 * └──────────────────────────────────────────────────────────────────────┘
 */
public class DataBean implements Serializable { // 实现Serializable接口，支持对象序列化
    private float G;    // 测量维度G（如增益/幅度参数）
    private float b;    // 测量维度b（如偏移/截距参数）
    private float c;    // 测量维度c（如斜率/比例参数）
    private String d;   // 测量维度d（如标签/标识字符串）
    private float e;    // 计算维度e（由compute()方法根据b、c推导得出）
    private float f;    // 计算维度f（由compute()方法根据b、c、e推导得出）

    /**
     * 默认无参构造方法。
     * <p>创建所有字段为零值/空的DataBean实例，适用于后续通过setter逐字段赋值的场景。</p>
     */
    public DataBean() { // 无参构造，字段保持默认零值
    }

    /**
     * 全参数构造方法。
     * <p>一次性初始化所有六个数据维度，适用于从已知数据直接构建对象的场景。</p>
     *
     * @param G 测量维度G
     * @param b 测量维度b
     * @param c 测量维度c
     * @param d 测量维度d（字符串类型）
     * @param e 计算维度e
     * @param f 计算维度f
     */
    public DataBean(float G, float b, float c, String d, float e, float f) { // 全参构造
        this.G = G;     // 赋值维度G
        this.b = b;     // 赋值维度b
        this.c = c;     // 赋值维度c
        this.d = d;     // 赋值维度d
        this.e = e;     // 赋值维度e
        this.f = f;     // 赋值维度f
    }

    /**
     * 获取测量维度G的值。
     *
     * @return 维度G的浮点值
     */
    public float getG() { // 获取G
        return G;     // 返回字段G
    }

    /**
     * 设置测量维度G的值。
     *
     * @param g 要设置的维度G浮点值
     */
    public void setG(float g) { // 设置G
        G = g;       // 赋值字段G
    }

    /**
     * 获取测量维度b的值。
     *
     * @return 维度b的浮点值
     */
    public float getB() { // 获取b
        return b;     // 返回字段b
    }

    /**
     * 设置测量维度b的值。
     *
     * @param b 要设置的维度b浮点值
     */
    public void setB(float b) { // 设置b
        this.b = b;  // 赋值字段b
    }

    /**
     * 获取测量维度c的值。
     *
     * @return 维度c的浮点值
     */
    public float getC() { // 获取c
        return c;     // 返回字段c
    }

    /**
     * 设置测量维度c的值。
     *
     * @param c 要设置的维度c浮点值
     */
    public void setC(float c) { // 设置c
        this.c = c;  // 赋值字段c
    }

    /**
     * 获取测量维度d的值。
     *
     * @return 维度d的字符串值
     */
    public String getD() { // 获取d
        return d;     // 返回字段d
    }

    /**
     * 设置测量维度d的值。
     *
     * @param d 要设置的维度d字符串值
     */
    public void setD(String d) { // 设置d
        this.d = d;  // 赋值字段d
    }

    /**
     * 获取计算维度e的值。
     *
     * @return 维度e的浮点值
     */
    public float getE() { // 获取e
        return e;     // 返回字段e
    }

    /**
     * 设置计算维度e的值。
     *
     * @param e 要设置的维度e浮点值
     */
    public void setE(float e) { // 设置e
        this.e = e;  // 赋值字段e
    }

    /**
     * 获取计算维度f的值。
     *
     * @return 维度f的浮点值
     */
    public float getF() { // 获取f
        return f;     // 返回字段f
    }

    /**
     * 设置计算维度f的值。
     *
     * @param f 要设置的维度f浮点值
     */
    public void setF(float f) { // 设置f
        this.f = f;  // 赋值字段f
    }

    /**
     * 返回DataBean的字符串表示形式，包含所有字段名值对。
     * <p>用于调试日志输出，格式为 {@code DataBean{G=xx, b=xx, c=xx, d='xx', e=xx, f=xx}}。</p>
     *
     * @return 包含所有字段的格式化字符串
     */
    @Override
    public String toString() { // 重写toString方法
        return "DataBean{" + // 拼接类名前缀
                "G=" + G + // 拼接维度G
                ", b=" + b + // 拼接维度b
                ", c=" + c + // 拼接维度c
                ", d='" + d + '\'' + // 拼接维度d（带引号标识字符串）
                ", e=" + e + // 拼接维度e
                ", f=" + f + // 拼接维度f
                '}';         // 拼接类名后缀
    }

    /**
     * 将数据导出为CSV格式的字符串行。
     * <p>使用中国区域格式(Locale.CHINA)，各字段按逗号分隔，浮点字段指定精度：
     * G保留1位小数，b/c/e/f保留3位小数。输出格式与{@link #compute()}计算后的精度一致。</p>
     *
     * @return 符合CSV规范的逗号分隔字符串
     */
    public String toCSV() { // 导出为CSV格式字符串
        return String.format(Locale.CHINA, "%2.1f,%3.3f,%3.3f,%s,%3.3f,%3.3f", G, b, c, d, e, f); // 按指定精度格式化六个维度
    }

    //计算e,f
    /**
     * 根据已有的b和c字段，线性推导并更新e和f字段的值。
     * <p>计算公式：
     * <ul>
     *   <li>e = 40 / (c - b)</li>
     *   <li>f = -(45 + b * 40 / (c - b))</li>
     * </ul>
     * 计算完成后使用{@link BigDecimal}对e和f进行四舍五入，保留3位小数。</p>
     * <p><b>注意：</b>当c等于b时，除数为零会导致浮点无穷大(Infinity)，调用方应确保c != b。</p>
     */
    public void compute() { // 执行线性校准计算
        e = 40 / (c - b);                   // 计算维度e：40除以(c-b)
        f = -(45 + b * 40 / (c - b));       // 计算维度f：负的(45 + b * 40/(c-b))


        BigDecimal b = new BigDecimal(e);                       // 将e封装为BigDecimal以便精度控制
        e = b.setScale(3, BigDecimal.ROUND_HALF_UP).floatValue(); // 对e四舍五入保留3位小数并回写
        b = new BigDecimal(f);                                  // 将f封装为BigDecimal以便精度控制
        f = b.setScale(3, BigDecimal.ROUND_HALF_UP).floatValue(); // 对f四舍五入保留3位小数并回写

    }
}
