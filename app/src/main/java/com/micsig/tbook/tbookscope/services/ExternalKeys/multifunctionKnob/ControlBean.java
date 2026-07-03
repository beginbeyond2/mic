package com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob; // 多功能旋钮模块包

import android.graphics.Rect; // 导入矩形类，用于存储控件区域

/**
 * @auother Liwb
 * @description:
 * @data:2024-2-28 14:54
 */

/**
 * +----------------------------------------------------------------------+
 * |                      控件信息Bean类                                    |
 * +----------------------------------------------------------------------+
 * | 模块定位：多功能旋钮模块的数据模型，描述一个UI控件的位置、可见性和名称 |
 * +----------------------------------------------------------------------+
 * | 核心职责：                                                            |
 * |   1. 存储控件的矩形区域(Rect)                                         |
 * |   2. 存储控件的可见性状态                                             |
     * |   3. 存储控件的名称标识                                             |
 * +----------------------------------------------------------------------+
 * | 架构设计：                                                            |
 * |   纯数据Bean类，仅包含属性和getter/setter                             |
 * |   用于多功能旋钮模块描述目标控件的区域信息                             |
 * +----------------------------------------------------------------------+
 * | 数据流向：                                                            |
 * |   ExternalKeysNode → ControlBean → 旋钮焦点定位                      |
 * +----------------------------------------------------------------------+
 * | 依赖关系：                                                            |
 * |   - android.graphics.Rect (矩形区域描述)                              |
 * +----------------------------------------------------------------------+
 * | 使用场景：                                                            |
 * |   多功能旋钮导航时，记录每个可操作控件的位置和状态                    |
 * +----------------------------------------------------------------------+
 */
public class ControlBean {
    private Rect rect; // 控件的矩形区域（左上右下坐标）
    private boolean visible; // 控件是否可见
    private String name; // 控件名称标识

    /**
     * 构造函数：创建控件信息Bean
     *
     * @param name    控件名称
     * @param visible 控件是否可见
     * @param rect    控件的矩形区域
     */
    public ControlBean(String name,boolean visible,Rect rect){
        this.name=name; // 保存控件名称
        this.visible=visible; // 保存可见性
        this.rect=rect; // 保存矩形区域
    }

    /**
     * 获取控件的矩形区域
     *
     * @return 控件的矩形区域
     */
    public Rect getRect() {
        return rect; // 返回矩形区域
    }

    /**
     * 设置控件的矩形区域
     *
     * @param rect 控件的矩形区域
     */
    public void setRect(Rect rect) {
        this.rect = rect; // 设置矩形区域
    }

    /**
     * 获取控件是否可见
     *
     * @return true=可见，false=不可见
     */
    public boolean isVisible() {
        return visible; // 返回可见性
    }

    /**
     * 设置控件是否可见
     *
     * @param visible true=可见，false=不可见
     */
    public void setVisible(boolean visible) {
        this.visible = visible; // 设置可见性
    }

    /**
     * 获取控件名称
     *
     * @return 控件名称
     */
    public String getName() {
        return name; // 返回控件名称
    }

    /**
     * 设置控件名称
     *
     * @param name 控件名称
     */
    public void setName(String name) {
        this.name = name; // 设置控件名称
    }

    /**
     * 返回控件的字符串描述
     *
     * @return 包含rect、visible、name的字符串
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ControlBean{"); // 创建StringBuilder
        sb.append("rect=").append(rect); // 追加矩形区域
        sb.append(", visible=").append(visible); // 追加可见性
        sb.append(", name='").append(name).append('\''); // 追加名称
        sb.append('}'); // 追加结束括号
        return sb.toString(); // 返回字符串
    }
}
