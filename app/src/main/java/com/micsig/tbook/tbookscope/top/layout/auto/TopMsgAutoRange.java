package com.micsig.tbook.tbookscope.top.layout.auto; // 自动功能详情数据类所在包

import com.micsig.tbook.ui.bean.RxBooleanWithSelect; // 导入带选中状态的布尔响应式数据Bean

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：自动量程(Auto Range)详情数据模型                                    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：封装自动量程页面的所有开关参数数据，支持响应式选中状态管理              │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：数据模型层，实现IAutoDetail标记接口，采用响应式数据绑定模式            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：TopLayoutAutoRange → TopMsgAutoRange → TopMsgAuto → RxBus       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：实现IAutoDetail接口，使用RxBooleanWithSelect响应式数据Bean          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：当自动量程页面开关状态变化时，封装数据并通过消息总线传递给其他模块      │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/5/16.
 */

public class TopMsgAutoRange implements IAutoDetail { // 自动量程详情数据模型，实现IAutoDetail标记接口

    private RxBooleanWithSelect range; // 量程自动开关的响应式布尔值
    private RxBooleanWithSelect vertical; // 垂直方向自动开关的响应式布尔值
    private RxBooleanWithSelect horizontal; // 水平方向自动开关的响应式布尔值
    private RxBooleanWithSelect level; // 电平自动开关的响应式布尔值

    /**
     * 获取量程自动开关状态
     *
     * @return 量程自动开关的响应式布尔值，包含值和选中状态
     */
    public RxBooleanWithSelect getRange() { // 获取量程自动开关状态
        return range; // 返回量程自动开关的响应式数据
    }

    /**
     * 设置量程自动开关状态，同时更新选中状态
     *
     * @param range 是否开启量程自动，true为开启
     */
    public void setRange(boolean range) { // 设置量程自动开关状态
        if (this.range == null) { // 判断当前对象是否为空
            this.range = new RxBooleanWithSelect(range); // 首次赋值，创建新的响应式布尔对象
        } else { // 对象已存在
            this.range.setValue(range); // 更新布尔值
            setAllUnSelect(); // 将所有字段设为未选中状态
            this.range.setRxMsgSelect(true); // 将当前字段设为选中状态
        }
    }

    /**
     * 获取垂直方向自动开关状态
     *
     * @return 垂直方向自动开关的响应式布尔值，包含值和选中状态
     */
    public RxBooleanWithSelect getVertical() { // 获取垂直方向自动开关状态
        return vertical; // 返回垂直方向自动开关的响应式数据
    }

    /**
     * 设置垂直方向自动开关状态，同时更新选中状态
     *
     * @param vertical 是否开启垂直方向自动，true为开启
     */
    public void setVertical(boolean vertical) { // 设置垂直方向自动开关状态
        if (this.vertical == null) { // 判断当前对象是否为空
            this.vertical = new RxBooleanWithSelect(vertical); // 首次赋值，创建新的响应式布尔对象
        } else { // 对象已存在
            this.vertical.setValue(vertical); // 更新布尔值
            setAllUnSelect(); // 将所有字段设为未选中状态
            this.vertical.setRxMsgSelect(true); // 将当前字段设为选中状态
        }
    }

    /**
     * 获取水平方向自动开关状态
     *
     * @return 水平方向自动开关的响应式布尔值，包含值和选中状态
     */
    public RxBooleanWithSelect getHorizontal() { // 获取水平方向自动开关状态
        return horizontal; // 返回水平方向自动开关的响应式数据
    }

    /**
     * 设置水平方向自动开关状态，同时更新选中状态
     *
     * @param horizontal 是否开启水平方向自动，true为开启
     */
    public void setHorizontal(boolean horizontal) { // 设置水平方向自动开关状态
        if (this.horizontal == null) { // 判断当前对象是否为空
            this.horizontal = new RxBooleanWithSelect(horizontal); // 首次赋值，创建新的响应式布尔对象
        } else { // 对象已存在
            this.horizontal.setValue(horizontal); // 更新布尔值
            setAllUnSelect(); // 将所有字段设为未选中状态
            this.horizontal.setRxMsgSelect(true); // 将当前字段设为选中状态
        }
    }

    /**
     * 获取电平自动开关状态
     *
     * @return 电平自动开关的响应式布尔值，包含值和选中状态
     */
    public RxBooleanWithSelect getLevel() { // 获取电平自动开关状态
        return level; // 返回电平自动开关的响应式数据
    }

    /**
     * 设置电平自动开关状态，同时更新选中状态
     *
     * @param level 是否开启电平自动，true为开启
     */
    public void setLevel(boolean level) { // 设置电平自动开关状态
        if (this.level == null) { // 判断当前对象是否为空
            this.level = new RxBooleanWithSelect(level); // 首次赋值，创建新的响应式布尔对象
        } else { // 对象已存在
            this.level.setValue(level); // 更新布尔值
            setAllUnSelect(); // 将所有字段设为未选中状态
            this.level.setRxMsgSelect(true); // 将当前字段设为选中状态
        }
    }

    /**
     * 将所有字段的选中状态设为false
     * 用于在设置某个字段前，先清除其他字段的选中状态，确保只有当前变更的字段被标记
     */
    private void setAllUnSelect() { // 将所有字段设为未选中状态
        range.setRxMsgSelect(false); // 量程开关设为未选中
        vertical.setRxMsgSelect(false); // 垂直方向开关设为未选中
        horizontal.setRxMsgSelect(false); // 水平方向开关设为未选中
        level.setRxMsgSelect(false); // 电平开关设为未选中
    }

    /**
     * 返回对象的字符串表示，用于调试和日志输出
     *
     * @return 包含所有字段值的字符串
     */
    @Override // 覆写Object的toString方法
    public String toString() { // 返回对象的字符串表示
        return "TopMsgAutoRange{" + // 返回类名和左花括号
                "range=" + range + // 拼接量程开关状态
                ", vertical=" + vertical + // 拼接垂直方向开关状态
                ", horizontal=" + horizontal + // 拼接水平方向开关状态
                ", level=" + level + // 拼接电平开关状态
                '}'; // 拼接右花括号
    }
}
