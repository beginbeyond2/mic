package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment; // 串口触发详情Fragment包声明

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位：CAN串口触发RdId详情Fragment                                           ║
 * ║  核心职责：配置CAN协议远程帧+数据帧ID触发条件（ID编辑）                              ║
 * ║  架构设计：继承BaseDetail，实现CAN RdId的具体触发逻辑                               ║
 * ║  数据流向：右侧菜单 → setConsumer → 更新ID → sendMsg → FPGA命令                   ║
 * ║  依赖关系：BaseDetail / CanBus / Command / CacheUtil / SerialsDetailCanRdId     ║
 * ║  使用场景：CAN触发模式下远程帧+数据帧ID子项的触发条件配置界面                          ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

import android.view.View; // 导入View基类，所有UI组件的父类

import com.micsig.tbook.scope.Bus.CanBus; // 导入CAN总线类，操作CAN触发参数
import com.micsig.tbook.scope.Bus.IBus; // 导入总线接口，用于获取具体总线类型
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类，获取串口通道实例
import com.micsig.tbook.scope.channel.SerialChannel; // 导入串口通道类，管理串口总线
import com.micsig.tbook.tbookscope.R; // 导入R资源类，包含所有资源ID引用
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件，发送FPGA指令
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials; // 导入右侧串口布局常量类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials; // 导入右侧菜单串口消息类
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入PlaySound工具类，播放按键音效
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger; // 导入触发器顶层布局类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils; // 导入串口工具类，提供进制转换方法
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail; // 导入串口详情数据接口
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanRdId; // 导入CAN RdId详情数据Bean
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 导入数字键盘进制常量接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类，读写键值对配置
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入自定义编辑框控件
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean类
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组控件
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道工具类，通道编号转换

/**
 * Created by yangj on 2017/6/9.
 *
 * CAN串口触发RdId详情Fragment。
 * 负责配置CAN协议中远程帧+数据帧ID的触发条件（16进制ID编辑），
 * 并将配置同步到FPGA命令和CanBus总线。
 */

public class TopLayoutTriggerSerialsCanRdId extends TopLayoutTriggerSerialsBaseDetail { // CAN RdId触发详情Fragment，继承基类
    private TopViewEdit canRdIdEdit; // CAN远程帧+数据帧ID编辑框
    private SerialsDetailCanRdId msgCanRdId; // CAN RdId详情数据Bean

    /**
     * 初始化界面控件，绑定视图和监听器。
     * @param view 根视图
     */
    @Override // 重写基类的initView抽象方法
    protected void initView(View view) { // 初始化界面控件
        canRdIdEdit = (TopViewEdit) view.findViewById(R.id.canRdIdEdit); // 查找CAN RdId编辑框控件
        canRdIdEdit.setOnClickEditListener(onClickEditListener); // 设置编辑框点击监听器
        msgCanRdId = new SerialsDetailCanRdId(); // 创建CAN RdId详情数据Bean实例
        msgCanRdId.setCanRdIdEdit(DIGITS_16, canRdIdEdit.getText()); // 设置初始ID值（16进制）
        msgCanRdId.setCanRdIdEditTitle(canRdIdEdit.getHead()); // 设置ID编辑标题
    }

    /**
     * 获取布局资源ID。
     * @return CAN RdId布局资源ID
     */
    @Override // 重写基类的getLayoutResId抽象方法
    protected int getLayoutResId() { // 返回布局资源ID
        return R.layout.layout_triggerserials_canrdid; // 返回CAN RdId布局
    }

    /**
     * 获取串口详情数据，从缓存中读取最新的ID值。
     * @param detailFlag 详情标志位
     * @return CAN RdId详情数据对象
     */
    @Override // 重写基类的getSerialsDetail抽象方法
    public ISerialsDetail getSerialsDetail(int detailFlag) { // 获取串口详情数据
        String rdId = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_RDID + getSerialsNumber()); // 从缓存读取ID值
        msgCanRdId.setCanRdIdEdit(DIGITS_16, rdId); // 更新Bean中的ID值
        return msgCanRdId; // 返回详情数据Bean
    }

    /**
     * 处理右侧菜单串口消息，当CAN类型且选中RdId时发送消息。
     * @param rightMsgSerials 右侧菜单串口消息
     */
    @Override // 重写基类的setConsumer抽象方法
    protected void setConsumer(RightMsgSerials rightMsgSerials) { // 处理右侧菜单消息
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1 // 判断是否为串口1且编号匹配
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_CAN // 判断是否为CAN类型
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + getSerialsNumber()) == 3) { // 判断CAN子项是否选中RdId
            //当且仅当当前can列表选中的是该项时，才向外发送消息
            sendMsg(msgCanRdId, rightMsgSerials.isFromEventBus()); // 发送消息通知外部
        }
    }

    /**
     * 加载缓存数据，恢复界面状态并同步到FPGA和总线。
     */
    @Override // 重写基类的setCache抽象方法
    protected void setCache() { // 加载缓存数据
        String rdId = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_RDID + getSerialsNumber()); // 从缓存读取ID值
        canRdIdEdit.setText(rdId); // 设置编辑框文本

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber); // 获取串口编号对应的FPGA通道号
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
        if(serialChannel == null) return; // 通道为空则返回
        CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN); // 获取CAN总线实例
        canBus.setFrameId(CanBus.CAN_TRIGGER_REMOTE_DATA_ID, toD(rdId, 16)); // 设置CAN远程帧+数据帧ID

        msgCanRdId.setCanRdIdEdit(DIGITS_16, rdId); // 更新Bean ID值
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发器索引
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1) // 判断S1是否匹配
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2) // 判断S2是否匹配
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3) // 判断S3是否匹配
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4)) // 判断S4是否匹配
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_CAN // 判断串口类型是否为CAN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + getSerialsNumber()) == 3) { // 判断CAN子项是否为RdId
            sendMsg(msgCanRdId, false); // 发送消息通知外部
        }
    }

    /**
     * 从命令数据设置ID值，用于外部同步。
     * @param id ID整数值
     * @param isFromEventBus 是否来自EventBus事件
     */
    public void setCommandData(int id, boolean isFromEventBus) { // 设置命令数据
        String sId = SerialsUtils.getHexBinFromLong(id, 8, DIGITS_16); // 将ID转换为8位16进制字符串
        if (!canRdIdEdit.getText().equals(sId)) { // 如果编辑框文本与转换后ID不一致
            onTextListener(sId, isFromEventBus); // 触发文本变更处理
        }
    }

    /**
     * 处理单选组条件变更事件（CAN RdId无条件选择，空实现）。
     * @param view 单选组控件
     * @param item 选中的通道项
     */
    @Override // 重写基类的setOnCheckChangedListener抽象方法
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) { // 条件变更处理（CAN RdId无条件选择）

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
            case R.id.canRdIdEdit: // CAN RdId编辑框
                //该为16进制,8位限制
                dialogKeyBoard.setDecimalData(8, DIGITS_16, onIdListener); // 设置键盘参数：8位16进制
                break; // 跳出switch
        }
    }

    /**
     * 数字键盘关闭监听器，将键盘输入结果传递给文本处理。
     */
    private TopDialogNumberKeyBoard.OnDismissListener onIdListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 键盘关闭监听器
        @Override // 重写onDismiss方法
        public void onDismiss(String result) { // 键盘关闭回调
            onTextListener(result, false); // 将结果传递给文本变更处理
        }
    };

    /**
     * ID文本变更处理，更新缓存、编辑框、Bean、FPGA命令和总线。
     * @param text 新的ID文本值
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onTextListener(String text, boolean isFromEventBus) { // 文本变更处理
        int val = (int) toDLong(text, IDigits.DIGITS_16); // 将16进制文本转换为10进制整数
        text = SerialsUtils.getHexBinFromInt(val, 8, IDigits.DIGITS_16); // 重新格式化为8位16进制字符串

        int cmdCh=TChan.toFpgaChNo(serialsNumber); // 获取FPGA命令通道号
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 获取FPGA总线通道号
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_RDID + getSerialsNumber(), text); // 保存ID到缓存
        canRdIdEdit.setEdit(text); // 更新编辑框显示
        msgCanRdId.setCanRdIdEdit(DIGITS_16, text); // 更新Bean ID值
        sendMsg(msgCanRdId, isFromEventBus); // 发送消息通知外部
        Command.get().getTrigger_can().setType(cmdCh, 3, toD(text, IDigits.DIGITS_16), 0, 0, false); // 发送FPGA CAN RdId触发命令

        if (!isFromEventBus) { // 非EventBus事件时同步总线
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
            if(serialChannel == null) return; // 通道为空则返回
            CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN); // 获取CAN总线实例
            canBus.setFrameId(CanBus.CAN_TRIGGER_REMOTE_DATA_ID, toD(text, IDigits.DIGITS_16)); // 设置CAN远程帧+数据帧ID
        }
    }
}
