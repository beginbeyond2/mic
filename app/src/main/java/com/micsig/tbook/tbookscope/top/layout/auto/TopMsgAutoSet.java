package com.micsig.tbook.tbookscope.top.layout.auto; // 自动功能详情数据类所在包

import com.micsig.tbook.ui.bean.RxBooleanWithSelect; // 导入带选中状态的布尔响应式数据Bean
import com.micsig.tbook.ui.bean.RxIntWithSelect; // 导入带选中状态的整型响应式数据Bean
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道选项数据Bean

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：自动设置(Auto Set)详情数据模型                                      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：封装自动设置页面的所有参数数据，支持响应式选中状态管理                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：数据模型层，实现IAutoDetail标记接口，采用响应式数据绑定模式            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：TopLayoutAutoSet → TopMsgAutoSet → TopMsgAuto → RxBus           │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：实现IAutoDetail接口，使用RxBooleanWithSelect/RxIntWithSelect       │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：当自动设置页面参数变化时，封装数据并通过消息总线传递给其他模块          │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/5/16.
 */

public class TopMsgAutoSet implements IAutoDetail { // 自动设置详情数据模型，实现IAutoDetail标记接口

    private RxBooleanWithSelect openChannel; // 是否开启通道的响应式布尔值，带选中状态
    private TopBeanChannel triggerSource; // 触发源选项数据，包含索引和显示文本
    private TopBeanChannel levelSelect; // 电平选择选项数据，决定电平单位（V/mV）
    private RxIntWithSelect levelDetail;//最大值99999 // 电平详细值（单位mV），最大值99999，带选中状态

    /**
     * 获取通道开启状态
     *
     * @return 通道开启状态的响应式布尔值，包含值和选中状态
     */
    public RxBooleanWithSelect getOpenChannel() { // 获取通道开启状态
        return openChannel; // 返回通道开启状态的响应式数据
    }

    /**
     * 设置通道开启状态，同时更新选中状态
     *
     * @param openChannel 是否开启通道，true为开启
     */
    public void setOpenChannel(boolean openChannel) { // 设置通道开启状态
        if (this.openChannel == null) { // 判断当前对象是否为空
            this.openChannel = new RxBooleanWithSelect(openChannel); // 首次赋值，创建新的响应式布尔对象
        } else { // 对象已存在
            this.openChannel.setValue(openChannel); // 更新布尔值
            setAllUnSelect(); // 将所有字段设为未选中状态
            this.openChannel.setRxMsgSelect(true); // 将当前字段设为选中状态，标识本次变更的字段
        }
    }

    /**
     * 获取触发源选项
     *
     * @return 触发源选项数据Bean
     */
    public TopBeanChannel getTriggerSource() { // 获取触发源选项
        return triggerSource; // 返回触发源选项数据
    }

    /**
     * 设置触发源选项，同时更新选中状态
     *
     * @param triggerSource 触发源选项数据Bean
     */
    public void setTriggerSource(TopBeanChannel triggerSource) { // 设置触发源选项
        if (this.triggerSource == null) { // 判断当前对象是否为空
            this.triggerSource = triggerSource; // 首次赋值，直接引用
        } else { // 对象已存在
            this.triggerSource = triggerSource; // 更新触发源引用
            setAllUnSelect(); // 将所有字段设为未选中状态
            this.triggerSource.setRxMsgSelect(true); // 将当前字段设为选中状态
        }
    }

    /**
     * 获取电平选择选项
     *
     * @return 电平选择选项数据Bean
     */
    public TopBeanChannel getLevelSelect() { // 获取电平选择选项
        return levelSelect; // 返回电平选择选项数据
    }

    /**
     * 设置电平选择选项，同时更新选中状态
     *
     * @param levelSelect 电平选择选项数据Bean
     */
    public void setLevelSelect(TopBeanChannel levelSelect) { // 设置电平选择选项
        if (this.levelSelect == null) { // 判断当前对象是否为空
            this.levelSelect = levelSelect; // 首次赋值，直接引用
        } else { // 对象已存在
            this.levelSelect = levelSelect; // 更新电平选择引用
            setAllUnSelect(); // 将所有字段设为未选中状态
            this.levelSelect.setRxMsgSelect(true); // 将当前字段设为选中状态
        }
    }

    /**
     * 获取电平详细值
     *
     * @return 电平详细值的响应式整型，包含值和选中状态
     */
    public RxIntWithSelect getLevelDetail() { // 获取电平详细值
        return levelDetail; // 返回电平详细值的响应式数据
    }

    /**
     * 设置电平详细值，同时更新选中状态
     *
     * @param levelDetail 电平详细值（单位mV），范围1~99999
     */
    public void setLevelDetail(int levelDetail) { // 设置电平详细值
        if (this.levelDetail == null) { // 判断当前对象是否为空
            this.levelDetail = new RxIntWithSelect(levelDetail); // 首次赋值，创建新的响应式整型对象
        } else { // 对象已存在
            this.levelDetail.setValue(levelDetail); // 更新整型值
            setAllUnSelect(); // 将所有字段设为未选中状态
            this.levelDetail.setRxMsgSelect(true); // 将当前字段设为选中状态
        }
    }

    /**
     * 将所有字段的选中状态设为false
     * 用于在设置某个字段前，先清除其他字段的选中状态，确保只有当前变更的字段被标记
     */
    private void setAllUnSelect() { // 将所有字段设为未选中状态
        openChannel.setRxMsgSelect(false); // 通道开启状态设为未选中
        triggerSource.setRxMsgSelect(false); // 触发源设为未选中
        levelSelect.setRxMsgSelect(false); // 电平选择设为未选中
        levelDetail.setRxMsgSelect(false); // 电平详细值设为未选中
    }

    /**
     * 返回对象的字符串表示，用于调试和日志输出
     *
     * @return 包含所有字段值的字符串
     */
    @Override // 覆写Object的toString方法
    public String toString() { // 返回对象的字符串表示
        return "TopMsgAutoSet{" + // 返回类名和左花括号
                "openChannel=" + openChannel + // 拼接通道开启状态
                ", triggerSource=" + triggerSource + // 拼接触发源选项
                ", levelSelect=" + levelSelect + // 拼接电平选择选项
                ", levelDetail='" + levelDetail + '\'' + // 拼接电平详细值
                '}'; // 拼接右花括号
    }
}
