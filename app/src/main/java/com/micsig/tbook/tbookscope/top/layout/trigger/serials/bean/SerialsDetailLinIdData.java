package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║            SerialsDetailLinIdData（LIN ID+数据触发详情）                      ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailLinIdData.java                           ║
 * ║ 核心职责: 封装LIN ID+数据触发条件的详情数据                                     ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有2个DataBean字段（ID+数据）             ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: LIN总线ID+数据联合触发时同时传递标识符和数据值                          ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailLinIdData implements ISerialsDetail { // LIN ID+数据触发详情，实现ISerialsDetail
    private DataBean linIdDataId; // LIN标识符值
    private DataBean linIdDataData; // LIN数据值
    private String linIdDataIdTitle; // 标识符字段标题
    private String linIdDataDataTitle; // 数据字段标题

    /**
     * 获取标识符字段标题
     * @return 标题字符串
     */
    public String getLinIdDataIdTitle() { // 获取标识符标题
        return linIdDataIdTitle; // 返回标题
    }

    /**
     * 设置标识符字段标题
     * @param linIdDataIdTitle 标题字符串
     */
    public void setLinIdDataIdTitle(String linIdDataIdTitle) { // 设置标识符标题
        this.linIdDataIdTitle = linIdDataIdTitle; // 赋值标题
    }

    /**
     * 获取数据字段标题
     * @return 标题字符串
     */
    public String getLinIdDataDataTitle() { // 获取数据标题
        return linIdDataDataTitle; // 返回标题
    }

    /**
     * 设置数据字段标题
     * @param linIdDataDataTitle 标题字符串
     */
    public void setLinIdDataDataTitle(String linIdDataDataTitle) { // 设置数据标题
        this.linIdDataDataTitle = linIdDataDataTitle; // 赋值标题
    }

    /**
     * 获取LIN标识符值
     * @return DataBean 标识符对象
     */
    public DataBean getLinIdDataId() { // 获取标识符值
        return linIdDataId; // 返回DataBean
    }

    /**
     * 设置LIN标识符值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setLinIdDataId(int digits, String value) { // 设置标识符值
        if (this.linIdDataId == null) { // 如果DataBean为空
            this.linIdDataId = new DataBean(); // 懒初始化创建DataBean
        }
        this.linIdDataId.setDigits(digits); // 设置进制
        this.linIdDataId.setValue(value); // 设置值
    }

    /**
     * 获取LIN数据值
     * @return DataBean 数据对象
     */
    public DataBean getLinIdDataData() { // 获取数据值
        return linIdDataData; // 返回DataBean
    }

    /**
     * 设置LIN数据值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setLinIdDataData(int digits, String value) { // 设置数据值
        if (this.linIdDataData == null) { // 如果DataBean为空
            this.linIdDataData = new DataBean(); // 懒初始化创建DataBean
        }
        this.linIdDataData.setDigits(digits); // 设置进制
        this.linIdDataData.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailLinIdData{" + // 开始构建字符串
                "linIdDataId='" + linIdDataId + '\'' + // 拼接标识符字段
                ", linIdDataData='" + linIdDataData + '\'' + // 拼接数据字段
                '}'; // 结束字符串构建
    }
}
