// ============================================================
//  模块定位：measure（测量功能模块）
//  文件路径：top/layout/measure/MeasureBean.java
//  核心职责：测量项数据模型，封装单个测量项的属性信息（索引、名称、通道、图标等）
//  架构设计：JavaBean模式，继承RxMsgSelect，提供getter/setter访问器
//  数据流向：由MeasureAdapter读取展示，由TopLayoutMeasureCommon管理选中状态
//  依赖关系：继承RxMsgSelect，被MeasureAdapter、TopLayoutMeasureCommon等广泛使用
//  使用场景：测量项列表数据绑定、已选测量项管理、缓存序列化
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.measure; // 声明该类所属的包路径

import com.micsig.tbook.ui.bean.RxMsgSelect; // 导入Rx消息选择基类


/**
 * 测量项数据模型 - 封装单个测量项的所有属性信息
 * Created by yangj on 2017/4/27.
 */
public class MeasureBean extends RxMsgSelect  { // 继承RxMsgSelect，支持RxJava消息选择机制
    private int no = -1; // 测量项编号，用于硬件通信标识，-1表示未分配
    private int index; // 测量项索引，对应测量类型数组中的位置
    private String name; // 测量项名称，如"频率"、"周期"等
    /**
     *  1 - 9
     */
    private int channel; // 通道编号，1-9对应Ch1-Ch8及Math通道
    private int drawableResId; // 测量项图标资源ID
    private boolean isSelect; // 是否被选中添加到已选列表

    /**
     * 四参数构造函数 - 创建测量项（默认未选中）
     * @param MeasureIndex 测量项索引
     * @param name 测量项名称
     * @param channel 通道编号
     * @param drawableResId 图标资源ID
     */
    public MeasureBean(int MeasureIndex, String name, int channel, int drawableResId) { // 四参数构造函数
        this.index = MeasureIndex; // 保存测量项索引
        this.name = name; // 保存测量项名称
        this.channel = channel; // 保存通道编号
        this.drawableResId = drawableResId; // 保存图标资源ID
    }

    /**
     * 五参数构造函数 - 创建测量项（指定选中状态）
     * @param MeasureIndex 测量项索引
     * @param name 测量项名称
     * @param channel 通道编号
     * @param drawableResId 图标资源ID
     * @param isSelect 是否选中
     */
    public MeasureBean(int MeasureIndex, String name, int channel, int drawableResId, boolean isSelect) { // 五参数构造函数
        this.index = MeasureIndex; // 保存测量项索引
        this.name = name; // 保存测量项名称
        this.channel = channel; // 保存通道编号
        this.drawableResId = drawableResId; // 保存图标资源ID
        this.isSelect = isSelect; // 保存选中状态
    }

    /**
     * 获取测量项编号
     * @return 编号值，-1表示未分配
     */
    public int getNo(){return no;} // 获取测量项编号
    /**
     * 设置测量项编号
     * @param no 编号值
     */
    public void setNo(int no){ // 设置测量项编号
        this.no = no; // 保存编号值
    }

    /**
     * 获取测量项索引
     * @return 索引值
     */
    public int getIndex() { // 获取测量项索引
        return index; // 返回索引值
    }

    /**
     * 设置测量项索引
     * @param index 索引值
     */
    public void setIndex(int index) { // 设置测量项索引
        this.index = index; // 保存索引值
    }

    /**
     * 获取测量项名称
     * @return 名称字符串
     */
    public String getName() { // 获取测量项名称
        return name; // 返回名称
    }

    /**
     * 设置测量项名称
     * @param name 名称字符串
     */
    public void setName(String name) { // 设置测量项名称
        this.name = name; // 保存名称
    }

    /**
     * 获取通道编号
     * @return 通道编号
     */
    public int getChannel() { // 获取通道编号
        return channel; // 返回通道编号
    }

    /**
     * 设置通道编号
     * @param channel 通道编号
     */
    public void setChannel(int channel) { // 设置通道编号
        this.channel = channel; // 保存通道编号
    }

    /**
     * 获取图标资源ID
     * @return 图标资源ID
     */
    public int getDrawableResId() { // 获取图标资源ID
        return drawableResId; // 返回图标资源ID
    }

    /**
     * 设置图标资源ID
     * @param drawableResId 图标资源ID
     */
    public void setDrawableResId(int drawableResId) { // 设置图标资源ID
        this.drawableResId = drawableResId; // 保存图标资源ID
    }

    /**
     * 判断是否选中
     * @return 是否选中
     */
    public boolean isSelect() { // 判断是否选中
        return isSelect; // 返回选中状态
    }

    /**
     * 设置选中状态
     * @param select 是否选中
     */
    public void setSelect(boolean select) { // 设置选中状态
        isSelect = select; // 保存选中状态
    }

    /**
     * 判断两个测量项是否相等（基于索引和通道）
     * @param o 比较对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) { // 重写equals方法，判断两个测量项是否相同
        if (this == o) return true; // 如果是同一对象引用，返回true
        if (o == null || getClass() != o.getClass()) return false; // 如果为空或类型不同，返回false

        MeasureBean measureBean = (MeasureBean) o; // 强制转换为MeasureBean

        if (index != measureBean.index) return false; // 如果索引不同，返回false
        return channel == measureBean.channel; // 比较通道是否相同

    }

    /**
     * 返回测量项的字符串表示
     * @return 包含索引、名称、通道、选中状态和图标ID的字符串
     */
    @Override
    public String toString() { // 重写toString方法
        return "MeasureBean{" + // 返回格式化的字符串
                "index=" + index + // 包含索引
                ", name='" + name + '\'' + // 包含名称
                ", channel=" + channel + // 包含通道
                ", isSelect=" + isSelect + // 包含选中状态
                ", drawableResId=" + drawableResId + // 包含图标资源ID
                '}'; // 字符串结束
    }
}
