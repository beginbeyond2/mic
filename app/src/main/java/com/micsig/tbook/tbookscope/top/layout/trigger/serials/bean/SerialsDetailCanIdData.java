package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean; // 串行详情Bean子包声明

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║           SerialsDetailCanIdData（CAN ID+数据触发详情）                       ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/bean/SerialsDetailCanIdData.java                         ║
 * ║ 核心职责: 封装CAN ID+数据组合触发条件的详情数据                                   ║
 * ║ 架构设计: 实现ISerialsDetail标记接口，持有3个DataBean字段(ID/DLC/Data)          ║
 * ║ 数据流向: 详情Fragment → 此Bean → Command下发                                ║
 * ║ 依赖关系: 聚合DataBean，实现ISerialsDetail                                    ║
 * ║ 使用场景: CAN ID+数据组合触发时传递ID、DLC和数据值                                ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailCanIdData implements ISerialsDetail { // CAN ID+数据触发详情，实现ISerialsDetail
    private DataBean canIdDataId; // CAN帧ID值
    private DataBean canIdDataDlc; // CAN帧DLC（数据长度码）值
    private DataBean canIdDataData; // CAN帧数据值
    private String canIdDataIdTitle; // ID字段标题
    private String canIdDataDlcTitle; // DLC字段标题
    private String canIdDataDataTitle; // 数据字段标题

    /**
     * 获取ID字段标题
     * @return 标题字符串
     */
    public String getCanIdDataIdTitle() { // 获取ID标题
        return canIdDataIdTitle; // 返回标题
    }

    /**
     * 设置ID字段标题
     * @param canIdDataIdTitle 标题字符串
     */
    public void setCanIdDataIdTitle(String canIdDataIdTitle) { // 设置ID标题
        this.canIdDataIdTitle = canIdDataIdTitle; // 赋值标题
    }

    /**
     * 获取DLC字段标题
     * @return 标题字符串
     */
    public String getCanIdDataDlcTitle() { // 获取DLC标题
        return canIdDataDlcTitle; // 返回标题
    }

    /**
     * 设置DLC字段标题
     * @param canIdDataDicTitle 标题字符串
     */
    public void setCanIdDataDlcTitle(String canIdDataDicTitle) { // 设置DLC标题
        this.canIdDataDlcTitle = canIdDataDicTitle; // 赋值标题
    }

    /**
     * 获取数据字段标题
     * @return 标题字符串
     */
    public String getCanIdDataDataTitle() { // 获取数据标题
        return canIdDataDataTitle; // 返回标题
    }

    /**
     * 设置数据字段标题
     * @param canIdDataDataTitle 标题字符串
     */
    public void setCanIdDataDataTitle(String canIdDataDataTitle) { // 设置数据标题
        this.canIdDataDataTitle = canIdDataDataTitle; // 赋值标题
    }

    /**
     * 获取CAN帧ID值
     * @return DataBean ID对象
     */
    public DataBean getCanIdDataId() { // 获取ID值
        return canIdDataId; // 返回DataBean
    }

    /**
     * 设置CAN帧ID值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setCanIdDataId(int digits, String value) { // 设置ID值
        if (this.canIdDataId == null) { // 如果DataBean为空
            this.canIdDataId = new DataBean(); // 懒初始化创建DataBean
        }
        this.canIdDataId.setDigits(digits); // 设置进制
        this.canIdDataId.setValue(value); // 设置值
    }

    /**
     * 获取CAN帧DLC值
     * @return DataBean DLC对象
     */
    public DataBean getCanIdDataDlc() { // 获取DLC值
        return canIdDataDlc; // 返回DataBean
    }

    /**
     * 设置CAN帧DLC值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setCanIdDataDlc(int digits, String value) { // 设置DLC值
        if (this.canIdDataDlc == null) { // 如果DataBean为空
            this.canIdDataDlc = new DataBean(); // 懒初始化创建DataBean
        }
        this.canIdDataDlc.setDigits(digits); // 设置进制
        this.canIdDataDlc.setValue(value); // 设置值
    }

    /**
     * 获取CAN帧数据值
     * @return DataBean数据对象
     */
    public DataBean getCanIdDataData() { // 获取数据值
        return canIdDataData; // 返回DataBean
    }

    /**
     * 设置CAN帧数据值（懒初始化）
     * @param digits 进制
     * @param value 值
     */
    public void setCanIdDataData(int digits, String value) { // 设置数据值
        if (this.canIdDataData == null) { // 如果DataBean为空
            this.canIdDataData = new DataBean(); // 懒初始化创建DataBean
        }
        this.canIdDataData.setDigits(digits); // 设置进制
        this.canIdDataData.setValue(value); // 设置值
    }

    @Override
    public String toString() { // 重写toString方法
        return "SerialsDetailCanIdData{" + // 开始构建字符串
                "canIdDataId='" + canIdDataId + '\'' + // 拼接ID字段
                ", canIdDataDlc='" + canIdDataDlc + '\'' + // 拼接DLC字段
                ", canIdDataData='" + canIdDataData + '\'' + // 拼接数据字段
                '}'; // 结束字符串构建
    }
}
