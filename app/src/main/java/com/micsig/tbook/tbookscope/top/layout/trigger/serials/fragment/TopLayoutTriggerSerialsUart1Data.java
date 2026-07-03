package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment; // 串口触发详情Fragment包声明

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位：UART串口触发Data1详情Fragment                                         ║
 * ║  核心职责：配置UART协议Data1触发条件（条件选择+数据编辑）                            ║
 * ║  架构设计：继承BaseDetail，实现UART Data1的具体触发逻辑                            ║
 * ║  数据流向：右侧菜单 → setConsumer → 更新位宽/进制/数据 → sendMsg → FPGA命令        ║
 * ║  依赖关系：BaseDetail / UartBus / Command / CacheUtil / SerialsDetailUart1Data   ║
 * ║  使用场景：UART触发模式下Data1子项的触发条件配置界面                                 ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

import android.view.View; // 导入View基类，所有UI组件的父类

import com.micsig.tbook.scope.Bus.IBus; // 导入总线接口，用于获取具体总线类型
import com.micsig.tbook.scope.Bus.UartBus; // 导入UART总线类，操作UART触发参数
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类，获取串口通道实例
import com.micsig.tbook.scope.channel.SerialChannel; // 导入串口通道类，管理串口总线
import com.micsig.tbook.tbookscope.R; // 导入R资源类，包含所有资源ID引用
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件，发送FPGA指令
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials; // 导入右侧串口布局常量类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials; // 导入右侧菜单串口消息类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsUart; // 导入右侧菜单UART消息详情类
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入PlaySound工具类，播放按键音效
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger; // 导入触发器顶层布局类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils; // 导入串口工具类，提供进制转换方法
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail; // 导入串口详情数据接口
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailUart1Data; // 导入UART Data1详情数据Bean
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类，读写键值对配置
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入自定义编辑框控件
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean类
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组控件
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道工具类，通道编号转换

/**
 * Created by yangj on 2017/6/9.
 *
 * UART串口触发Data1详情Fragment。
 * 负责配置UART协议中Data1字段的触发条件（条件选择+数据编辑），
 * 并将配置同步到FPGA命令和UartBus总线。
 */

public class TopLayoutTriggerSerialsUart1Data extends TopLayoutTriggerSerialsBaseDetail { // UART Data1触发详情Fragment，继承基类
    private TopViewEdit uart1DataEdit; // Data1数据编辑框
    private TopViewRadioGroup uart1DataCondition; // Data1条件选择单选组
    private SerialsDetailUart1Data msgUart1Data; // Data1详情数据Bean

    /**
     * 初始化界面控件，绑定视图和监听器。
     * @param view 根视图
     */
    @Override // 重写基类的initView抽象方法
    protected void initView(View view) { // 初始化界面控件
        uart1DataCondition = (TopViewRadioGroup) view.findViewById(R.id.uart1DataCondition); // 查找Data1条件单选组控件
        uart1DataEdit = (TopViewEdit) view.findViewById(R.id.uart1DataEdit); // 查找Data1数据编辑框控件
        uart1DataCondition.setOnListener(onCheckChangedListener); // 设置条件单选组变更监听器
        uart1DataEdit.setOnClickEditListener(onClickEditListener); // 设置数据编辑框点击监听器
        msgUart1Data = new SerialsDetailUart1Data(); // 创建Data1详情数据Bean实例
        msgUart1Data.setUart1DataCondition(uart1DataCondition.getSelected()); // 设置初始条件选中项
        msgUart1Data.setUart1DataEdit(digits, uart1DataEdit.getText()); // 设置初始数据编辑值
        msgUart1Data.setUart1DataConditionTitle(uart1DataCondition.getHead()); // 设置条件标题
        msgUart1Data.setUart1DataEditTitle(uart1DataEdit.getHead()); // 设置数据编辑标题
    }

    /**
     * 获取布局资源ID。
     * @return UART Data1布局资源ID
     */
    @Override // 重写基类的getLayoutResId抽象方法
    protected int getLayoutResId() { // 返回布局资源ID
        return R.layout.layout_triggerserials_uart1data; // 返回UART Data1布局
    }

    /**
     * 获取串口详情数据，从缓存中读取最新的条件和数据值。
     * @param detailFlag 详情标志位
     * @return UART Data1详情数据对象
     */
    @Override // 重写基类的getSerialsDetail抽象方法
    public ISerialsDetail getSerialsDetail(int detailFlag) { // 获取串口详情数据
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_CONDITION + getSerialsNumber()); // 从缓存读取条件索引
        String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + getSerialsNumber()); // 从缓存读取数据编辑值
        if (!edit.equals(msgUart1Data.getUart1DataEdit().getValue())) { // 如果编辑值与当前Bean不一致
            digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? DIGITS_2 : DIGITS_16; // 根据右侧显示设置确定进制
            uart1DataEdit.setText(edit); // 更新编辑框文本
            msgUart1Data.setUart1DataEdit(digits, uart1DataEdit.getText()); // 更新Bean中的编辑值
        }
        if (msgUart1Data.getUart1DataCondition().getIndex() != condition) { // 如果条件索引与当前Bean不一致
            uart1DataCondition.setSelectedIndex(condition); // 更新条件单选组选中项
            msgUart1Data.setUart1DataCondition(uart1DataCondition.getSelected()); // 更新Bean中的条件值
        }
        return msgUart1Data; // 返回详情数据Bean
    }

    /**
     * 处理右侧菜单串口消息，更新位宽、进制和数据。
     * @param rightMsgSerials 右侧菜单串口消息
     */
    @Override // 重写基类的setConsumer抽象方法
    protected void setConsumer(RightMsgSerials rightMsgSerials) { // 处理右侧菜单消息
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1 // 判断是否为串口1且编号匹配
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_UART) { // 判断是否为UART类型
            RightMsgSerialsUart detailsUart = (RightMsgSerialsUart) rightMsgSerials.getSerialsDetails(); // 获取UART消息详情
            bits = detailsUart.getIntBits(); // 更新数据位宽
            digits = detailsUart.getIntDigits(getContext()); // 更新当前进制
            if (uart1DataEdit != null) { // 编辑框不为空时处理
                String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + getSerialsNumber()); // 从缓存读取编辑值
                uart1DataEdit.setText(edit); // 更新编辑框文本
                msgUart1Data.setUart1DataEdit(digits, uart1DataEdit.getText()); // 更新Bean中的编辑值
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + getSerialsNumber()) == 4) { // 判断UART子项是否选中Data1
                    //当且仅当当前uart列表选中的是该项时，才向外发送消息
                    sendMsg(msgUart1Data, rightMsgSerials.isFromEventBus()); // 发送消息通知外部
                }
            }
        }
    }

    /**
     * 加载缓存数据，恢复界面状态并同步到FPGA和总线。
     */
    @Override // 重写基类的setCache抽象方法
    protected void setCache() { // 加载缓存数据
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_CONDITION + getSerialsNumber()); // 从缓存读取条件索引
        String edit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + getSerialsNumber()); // 从缓存读取编辑值
        digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + getSerialsNumber()) == 1 ? DIGITS_2 : DIGITS_16; // 确定进制
        uart1DataCondition.setSelectedIndex(condition); // 设置条件单选组选中项
        uart1DataEdit.setText(edit); // 设置编辑框文本

        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 获取串口编号对应的FPGA通道号
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
        if (serialChannel == null) return; // 通道为空则返回
        UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART); // 获取UART总线实例
        uartBus.setTriggerRelation(getConditionValue(condition)); // 设置触发条件关系
        uartBus.setTriggerData(UartBus.UART_TRIGGER_DATA1, toD(edit, digits)); // 设置Data1触发数据

        msgUart1Data.setUart1DataCondition(uart1DataCondition.getSelected()); // 更新Bean条件值
        msgUart1Data.setUart1DataEdit(digits, edit); // 更新Bean编辑值
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发器索引
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1) // 判断S1是否匹配
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2) // 判断S2是否匹配
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3) // 判断S3是否匹配
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4)) // 判断S4是否匹配
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_UART // 判断串口类型是否为UART
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + getSerialsNumber()) == 4) { // 判断UART子项是否为Data1
            sendMsg(msgUart1Data, false); // 发送消息通知外部
        }
    }

    /**
     * 从命令数据设置条件和数据值，用于外部同步。
     * @param condition 条件索引
     * @param data 数据值
     * @param isFromEventBus 是否来自EventBus事件
     */
    public void setCommandData(int condition, int data, boolean isFromEventBus) { // 设置命令数据
        String sData = SerialsUtils.getHexBinFromLong(data, digits == DIGITS_16 ? 2 : 9, digits); // 将数据转换为指定进制字符串
        if (!uart1DataEdit.getText().equals(sData)) { // 如果编辑框文本与转换后数据不一致
            onTextListener(sData, isFromEventBus); // 触发文本变更处理
        }
        if (uart1DataCondition.getSelected().getIndex() != condition) { // 如果条件索引不一致
            uart1DataCondition.setSelectedIndex(condition); // 更新条件选中项
            onCheckListener(uart1DataCondition, uart1DataCondition.getSelected(), isFromEventBus); // 触发条件变更处理
        }
    }

    /**
     * 处理单选组条件变更事件。
     * @param view 单选组控件
     * @param item 选中的通道项
     */
    @Override // 重写基类的setOnCheckChangedListener抽象方法
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) { // 条件变更处理
        onCheckListener(view, item, false); // 委托给onCheckListener处理
    }

    /**
     * 处理编辑框点击事件，弹出数字键盘。
     * @param v 被点击的编辑框
     * @param text 当前文本内容
     */
    @Override // 重写基类的setOnClickEditListener抽象方法
    protected void setOnClickEditListener(final TopViewEdit v, String text) { // 编辑框点击处理
        PlaySound.getInstance().playButton(); // 播放按键音效
        switch (v.getId()) { // 根据编辑框ID分支处理
            case R.id.uart1DataEdit: // Data1编辑框
                //该为16进制，两位限制//2进制9位
                dialogKeyBoard.setDecimalData(digits == DIGITS_16 ? 2 : 9, digits, onDataListener); // 设置键盘参数：16进制2位或2进制9位
                break; // 跳出switch
        }
    }

    /**
     * 数字键盘关闭监听器，将键盘输入结果传递给文本处理。
     */
    private TopDialogNumberKeyBoard.OnDismissListener onDataListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 键盘关闭监听器
        @Override // 重写onDismiss方法
        public void onDismiss(String result) { // 键盘关闭回调
            onTextListener(result, false); // 将结果传递给文本变更处理
        }
    };

    /**
     * 条件选择变更处理，更新缓存、Bean、FPGA命令和总线。
     * @param view 单选组控件
     * @param item 选中的通道项
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onCheckListener(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) { // 条件变更处理
        int cmdCh=TChan.toFpgaChNo(serialsNumber); // 获取FPGA命令通道号
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 获取FPGA总线通道号
        if (view.getId() == R.id.uart1DataCondition) { // 判断是否为Data1条件单选组
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_CONDITION + getSerialsNumber(), String.valueOf(item.getIndex())); // 保存条件索引到缓存
            msgUart1Data.setUart1DataCondition(item); // 更新Bean条件值
            sendMsg(msgUart1Data, isFromEventBus); // 发送消息通知外部
            Command.get().getTrigger_uart().setType(cmdCh, 4, item.getIndex(), toD(uart1DataEdit.getText(), digits), false); // 发送FPGA触发条件命令

            if (!isFromEventBus) { // 非EventBus事件时同步总线
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
                if (serialChannel == null) return; // 通道为空则返回
                UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART); // 获取UART总线实例
                uartBus.setTriggerRelation(getConditionValue(item.getIndex())); // 设置触发条件关系
            }
        }
    }

    /**
     * 数据文本变更处理，更新缓存、编辑框、Bean、FPGA命令和总线。
     * @param text 新的文本值
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onTextListener(String text, boolean isFromEventBus) { // 文本变更处理
        int cmdCh= TChan.toFpgaChNo(serialsNumber); // 获取FPGA命令通道号
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 获取FPGA总线通道号

        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART_1DATA_EDIT + getSerialsNumber(), text); // 保存编辑值到缓存
        uart1DataEdit.setEdit(text); // 更新编辑框显示
        msgUart1Data.setUart1DataEdit(digits, text); // 更新Bean编辑值
        sendMsg(msgUart1Data, isFromEventBus); // 发送消息通知外部
        Command.get().getTrigger_uart().setType(cmdCh, 4, uart1DataCondition.getSelected().getIndex(), toD(text, digits), false); // 发送FPGA触发数据命令

        if (!isFromEventBus) { // 非EventBus事件时同步总线
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
            if (serialChannel == null) return; // 通道为空则返回
            UartBus uartBus = (UartBus) serialChannel.getBus(IBus.UART); // 获取UART总线实例
            uartBus.setTriggerData(UartBus.UART_TRIGGER_DATA1, toD(text, digits)); // 设置Data1触发数据
        }
    }
}
