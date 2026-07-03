package com.micsig.tbook.tbookscope.top.layout.trigger.serials; // 串行触发模块的根包声明

import com.micsig.tbook.tbookscope.top.layout.trigger.ITriggerDetail; // 触发详情接口
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail; // 串行详情数据接口
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 顶部通道Bean类

/**
 * ╔═══════════════════════════════════════════════════════════════════════════╗
 * ║                   TopMsgTriggerSerials（串行触发消息封装）                     ║
 * ╠═══════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位: serials/TopMsgTriggerSerials.java                               ║
 * ║ 核心职责: 封装串行触发的消息数据，包含选中的协议项和详情数据                       ║
 * ║ 架构设计: 实现ITriggerDetail接口，作为触发消息的载体                            ║
 * ║ 数据流向: TopLayoutTriggerSerials → OnDetailSendMsgListener → 上层触发页面    ║
 * ║ 依赖关系: 实现ITriggerDetail，聚合Serials和ISerialsDetail                    ║
 * ║ 使用场景: 串行触发条件变更时，封装当前选中协议和详情参数传递给上层                   ║
 * ╚═══════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2017/5/17.
 */

public class TopMsgTriggerSerials implements ITriggerDetail { // 串行触发消息封装类，实现ITriggerDetail接口
    private Serials serials; // 当前选中的串行协议项
    private ISerialsDetail serialsDetail; // 当前选中协议的详情数据

    /**
     * 克隆当前对象
     * @return 克隆后的对象
     * @throws CloneNotSupportedException 不支持克隆异常
     */
    @Override
    public Object clone() throws CloneNotSupportedException { // 重写clone方法
        return super.clone(); // 调用父类clone方法
    }

    /**
     * 获取当前选中的串行协议项
     * @return Serials对象
     */
    public Serials getSerials() { // 获取串行协议项的getter方法
        return serials; // 返回serials引用
    }

    /**
     * 设置当前选中的串行协议项，同时将RxMsgSelect标记为true
     * @param serials 要设置的串行协议项
     */
    public void setSerials(Serials serials) { // 设置串行协议项的setter方法
        this.serials = serials; // 保存serials引用
        this.serials.setRxMsgSelect(true); // 标记Rx消息已选择
    }

    /**
     * 获取触发源通道（串行触发无特定通道，返回null）
     * @return null
     */
    @Override
    public TopBeanChannel getTriggerSource() { // 获取触发源通道
        return null; // 串行触发无特定通道，返回null
    }

    /**
     * 获取当前串行协议的详情数据
     * @return ISerialsDetail详情数据接口
     */
    public ISerialsDetail getSerialsDetail() { // 获取详情数据的getter方法
        return serialsDetail; // 返回serialsDetail引用
    }

    /**
     * 设置当前串行协议的详情数据
     * @param serialsDetail 要设置的详情数据
     */
    public void setSerialsDetail(ISerialsDetail serialsDetail) { // 设置详情数据的setter方法
        this.serialsDetail = serialsDetail; // 保存serialsDetail引用
    }

    @Override
    public String toString() { // 重写toString方法
        return "TopMsgTriggerSerials{" + // 开始构建字符串
                "serials=" + serials + // 拼接serials字段
                ", serialsDetail=" + serialsDetail + // 拼接serialsDetail字段
                '}'; // 结束字符串构建
    }
}
