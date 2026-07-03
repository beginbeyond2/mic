package com.micsig.tbook.tbookscope.scpi;

/**
 * Created by liwb on 2018/1/11.
 */

/*******************************************************************************
 *   +-----------------------------------------------------------------------+   *
 *   |                      SCPIParam - SCPI参数数据结构                       |   *
 *   +-----------------------------------------------------------------------+   *
 *   |  模块定位: SCPI命令解析层的参数传递载体                                    |   *
 *   |  核心职责: 封装SCPI命令解析后的输入参数与输出结果，供命令分发与执行方法使用        |   *
 *   |  架构设计: 纯数据POJO，作为C层JNI回调与Java层命令处理方法之间的数据桥梁           |   *
 *   |  数据流向: C层JNI(scpiCommand) → 解析填充SCPIParam → Java层SCPI_xxx方法      |   *
 *   |  依赖关系: 被SCPICommandDeal、所有SCPI_xxx命令处理类引用                      |   *
 *   |  使用场景: 每条SCPI命令解析后创建/复用，传递参数到对应处理方法，返回查询结果       |   *
 *   +-----------------------------------------------------------------------+   *
 ******************************************************************************/
public class SCPIParam {

    /** 命令在scpi_commands数组中的索引，-1表示解析错误 */
    public int commandIndex; // 命令索引

    /** 字符串参数1，如通道名"CH1"、触发类型"EDGE"等 */
    public String sParam1; // 字符串参数1
    /** 字符串参数2，如耦合方式"DC"、边沿类型"RISE"等 */
    public String sParam2; // 字符串参数2
    /** 字符串参数3 */
    public String sParam3; // 字符串参数3
    /** 字符串参数4 */
    public String sParam4; // 字符串参数4
    /** 字符串参数5 */
    public String sParam5; // 字符串参数5
    /** 字符串参数6 */
    public String sParam6; // 字符串参数6

    /** 布尔参数1，如通道开关ON/OFF、反相等 */
    public boolean bParam1; // 布尔参数1
    /** 布尔参数2 */
    public boolean bParam2; // 布尔参数2
    /** 布尔参数3 */
    public boolean bParam3; // 布尔参数3
    /** 布尔参数4 */
    public boolean bParam4; // 布尔参数4
    /** 布尔参数5 */
    public boolean bParam5; // 布尔参数5
    /** 布尔参数6 */
    public boolean bParam6; // 布尔参数6

    /** 整型参数1，如通道编号、采样次数等 */
    public int iParam1; // 整型参数1
    /** 整型参数2 */
    public int iParam2; // 整型参数2
    /** 整型参数3 */
    public int iParam3; // 整型参数3
    /** 整型参数4 */
    public int iParam4; // 整型参数4
    /** 整型参数5 */
    public int iParam5; // 整型参数5
    /** 整型参数6 */
    public int iParam6; // 整型参数6

    /** 双精度参数1，如触发电平、垂直档位等浮点数值 */
    public double dParam1; // 双精度参数1
    /** 双精度参数2 */
    public double dParam2; // 双精度参数2
    /** 双精度参数3 */
    public double dParam3; // 双精度参数3
    /** 双精度参数4 */
    public double dParam4; // 双精度参数4
    /** 双精度参数5 */
    public double dParam5; // 双精度参数5
    /** 双精度参数6 */
    public double dParam6; // 双精度参数6

    /** 字符串结果参数，用于查询命令返回字符串结果 */
    public String sResultParam1; // 字符串结果参数
    /** 布尔结果参数，用于查询命令返回布尔结果 */
    public boolean bResultParam1; // 布尔结果参数
    /** 整型结果参数，用于查询命令返回整型结果 */
    public int iResultParam1; // 整型结果参数
    /** 双精度结果参数，用于查询命令返回双精度结果 */
    public double dResultParam1; // 双精度结果参数

    /**
     * 清除所有参数数据，将所有字段重置为默认值。
     * 在每次解析新SCPI命令前调用，确保参数不会被上一条命令的残留值污染。
     * 字符串置null，布尔置false，数值置-1（表示未设置）。
     */
    public void clearData() {
        commandIndex = -1; // 重置命令索引为-1（无效）
        sParam1 = null; // 清空字符串参数1
        sParam2 = null; // 清空字符串参数2
        sParam3 = null; // 清空字符串参数3
        sParam4 = null; // 清空字符串参数4
        sParam5 = null; // 清空字符串参数5
        sParam6 = null; // 清空字符串参数6
        bParam1 = false; // 重置布尔参数1
        bParam2 = false; // 重置布尔参数2
        bParam3 = false; // 重置布尔参数3
        bParam4 = false; // 重置布尔参数4
        bParam5 = false; // 重置布尔参数5
        bParam6 = false; // 重置布尔参数6
        iParam1 = -1; // 重置整型参数1为-1
        iParam2 = -1; // 重置整型参数2为-1
        iParam3 = -1; // 重置整型参数3为-1
        iParam4 = -1; // 重置整型参数4为-1
        iParam5 = -1; // 重置整型参数5为-1
        iParam6 = -1; // 重置整型参数6为-1
        dParam1 = -1; // 重置双精度参数1为-1
        dParam2 = -1; // 重置双精度参数2为-1
        dParam3 = -1; // 重置双精度参数3为-1
        dParam4 = -1; // 重置双精度参数4为-1
        dParam5 = -1; // 重置双精度参数5为-1
        dParam6 = -1; // 重置双精度参数6为-1
        sResultParam1 = null; // 清空字符串结果参数
        bResultParam1 = false; // 重置布尔结果参数
        iResultParam1 = -1; // 重置整型结果参数为-1
        dResultParam1 = -1; // 重置双精度结果参数为-1
    }
}
