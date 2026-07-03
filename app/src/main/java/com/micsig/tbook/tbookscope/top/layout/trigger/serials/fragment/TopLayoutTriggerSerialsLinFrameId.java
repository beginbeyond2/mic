package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment; // 串口触发子条件Fragment包

import android.view.View; // 导入Android视图基类

import com.micsig.tbook.scope.Bus.IBus; // 导入总线接口定义
import com.micsig.tbook.scope.Bus.LinBus; // 导入LIN总线操作类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂，用于获取串口通道
import com.micsig.tbook.scope.channel.SerialChannel; // 导入串口通道类
import com.micsig.tbook.tbookscope.R; // 导入资源ID常量类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件，用于下发FPGA指令
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials; // 导入右侧串口菜单布局常量
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials; // 导入右侧串口消息实体
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入按键音效工具类
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger; // 导入顶部触发布局常量
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils; // 导入串口工具类，用于数值格式转换
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail; // 导入串口触发详情接口
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailLinFrameId; // 导入LIN帧ID触发详情Bean
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入顶部可编辑视图组件
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道Bean，用于单选按钮组数据
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选按钮组视图
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道转换工具类

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: 顶部触发布局 → 串口触发 → LIN子条件 → 帧ID触发Fragment             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                 ║
 * ║    1. 提供LIN帧ID触发的UI交互界面（帧ID输入）                                 ║
 * ║    2. 管理帧ID的缓存读写与FPGA指令下发                                       ║
 * ║    3. 将用户输入同步到LinBus总线和Command命令通道                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  架构设计:                                                                 ║
 * ║    继承TopLayoutTriggerSerialsBaseDetail基类，实现模板方法模式                ║
 * ║    - initView(): 初始化帧ID编辑框和消息Bean                                  ║
 * ║    - setCache(): 从缓存恢复状态并同步到FPGA                                  ║
 * ║    - setConsumer(): 响应右侧菜单消息，条件匹配时发送触发消息                  ║
 * ║    - onTextListener(): 统一处理文本变更，更新缓存/Bean/FPGA                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向:                                                                 ║
 * ║    UI编辑框 → CacheUtil缓存 → SerialsDetailLinFrameId Bean                 ║
 * ║              → Command指令(FPGA) → LinBus总线(软件解码)                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系:                                                                 ║
 * ║    - TopLayoutTriggerSerialsBaseDetail (继承)                               ║
 * ║    - LinBus / SerialChannel / ChannelFactory (总线与通道)                   ║
 * ║    - Command (FPGA指令下发)                                                 ║
 * ║    - CacheUtil (持久化缓存)                                                 ║
 * ║    - TopDialogNumberKeyBoard (数字键盘弹窗)                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  使用场景:                                                                 ║
 * ║    当用户在LIN触发模式下选择"帧ID"子条件时，加载此Fragment                    ║
 * ║    对应LIN子条件索引值为1，帧ID最大值为3F                                    ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

public class TopLayoutTriggerSerialsLinFrameId extends TopLayoutTriggerSerialsBaseDetail { // LIN帧ID触发Fragment，继承串口触发详情基类
    private TopViewEdit linFrameIdEditEdit; // LIN帧ID编辑框
    private SerialsDetailLinFrameId msgLinFrameId; // LIN帧ID触发详情消息Bean

    /**
     * 初始化视图控件及消息Bean
     * @param view Fragment根视图
     */
    @Override
    protected void initView(View view) { // 重写基类方法，初始化视图控件
        linFrameIdEditEdit = (TopViewEdit) view.findViewById(R.id.linFrameIdEditEdit); // 查找帧ID编辑框控件
        linFrameIdEditEdit.setOnClickEditListener(onClickEditListener); // 为帧ID编辑框设置点击监听
        msgLinFrameId = new SerialsDetailLinFrameId(); // 创建LIN帧ID消息Bean实例
        msgLinFrameId.setLinFrameIdEditEdit(DIGITS_16, linFrameIdEditEdit.getText()); // 设置帧ID值（16进制）
        msgLinFrameId.setLinFrameIdEditEditTitle(linFrameIdEditEdit.getHead()); // 设置帧ID字段标题
    }

    /**
     * 获取布局资源ID
     * @return LIN帧ID布局资源ID
     */
    @Override
    protected int getLayoutResId() { // 重写基类方法，返回布局资源ID
        return R.layout.layout_triggerserials_linframeid; // 返回LIN帧ID布局
    }

    /**
     * 获取串口触发详情数据
     * @param detailFlag 详情标志位
     * @return LIN帧ID触发详情Bean
     */
    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) { // 重写基类方法，获取串口触发详情
        String linFrameIdEdit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_FRAMEID + getSerialsNumber()); // 从缓存读取帧ID值
        msgLinFrameId.setLinFrameIdEditEdit(DIGITS_16, linFrameIdEdit); // 设置帧ID到消息Bean（16进制）
        return msgLinFrameId; // 返回消息Bean
    }

    /**
     * 响应右侧菜单消息，条件匹配时发送触发消息
     * @param rightMsgSerials 右侧串口菜单消息
     */
    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) { // 重写基类方法，处理右侧菜单消息
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1 // 判断是否为串口1且当前序号匹配
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_LIN // 判断串口类型是否为LIN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + getSerialsNumber()) == 1) { // 判断LIN子条件是否为帧ID(索引1)
            //当且仅当当前lin列表选中的是该项时，才向外发送消息
            sendMsg(msgLinFrameId, rightMsgSerials.isFromEventBus()); // 发送LIN帧ID触发消息
        }
    }

    /**
     * 从缓存恢复状态并同步到FPGA和总线
     */
    @Override
    protected void setCache() { // 重写基类方法，从缓存恢复数据
        String linFrameIdEdit = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_FRAMEID + getSerialsNumber()); // 从缓存读取帧ID
        linFrameIdEditEdit.setText(linFrameIdEdit); // 设置帧ID编辑框文本

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber); // 将串口序号转换为FPGA通道号
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
        if(serialChannel == null) return; // 通道为空则直接返回
        LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN); // 获取LIN总线实例
        linBus.setFrameId(LinBus.LIN_TRIGGER_FRAME_ID, toD(linFrameIdEdit, 16)); // 设置LIN触发帧ID

        msgLinFrameId.setLinFrameIdEditEdit(DIGITS_16, linFrameIdEdit); // 同步帧ID到消息Bean
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发详情索引
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1) // 判断是否为S1触发详情且当前为S1
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2) // 判断是否为S2触发详情且当前为S2
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3) // 判断是否为S3触发详情且当前为S3
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4)) // 判断是否为S4触发详情且当前为S4
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_LIN // 判断右侧菜单是否选中LIN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + getSerialsNumber()) == 1) { // 判断LIN子条件是否为帧ID
            sendMsg(msgLinFrameId, false); // 发送触发消息（非EventBus来源）
        }
    }

    /**
     * 从外部命令设置触发数据（如EventBus远程同步）
     * @param id 帧ID值
     * @param isFromEventBus 是否来自EventBus
     */
    public void setCommandData(int id, boolean isFromEventBus) { // 外部命令设置触发数据
        String sId = SerialsUtils.getHexBinFromLong(id, 2, DIGITS_16); // 将帧ID转为2位16进制字符串
        if (!linFrameIdEditEdit.getText().equals(sId)) { // 帧ID值有变化时
            onTextListener(sId, isFromEventBus); // 触发文本变更监听
        }
    }

    /**
     * 单选按钮组变更监听（本Fragment无单选按钮，空实现）
     * @param view 单选按钮组视图
     * @param item 选中的通道Bean
     */
    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) { // 重写基类方法，单选按钮变更监听（空实现）

    }

    /**
     * 编辑框点击监听，弹出数字键盘
     * @param v 被点击的编辑框
     * @param text 当前文本
     */
    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) { // 重写基类方法，编辑框点击监听
        PlaySound.getInstance().playButton(); // 播放按键音效
        switch (v.getId()) { // 根据编辑框ID分发
            case R.id.linFrameIdEditEdit: // 帧ID编辑框
                //该为16进制,两位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_16, onIdListener); // 设置键盘为16进制2位，绑定帧ID监听
                break;
        }
    }

    /** 帧ID键盘关闭监听，将结果传递给文本变更监听 */
    private TopDialogNumberKeyBoard.OnDismissListener onIdListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 帧ID键盘关闭回调
        @Override
        public void onDismiss(String result) { // 键盘关闭时回调
            onTextListener(result, false); // 触发帧ID文本变更（非EventBus来源）
        }
    };

    /**
     * 统一处理文本变更，更新缓存/Bean/FPGA指令/总线
     * @param text 新文本值
     * @param isFromEventBus 是否来自EventBus
     */
    private void onTextListener(String text, boolean isFromEventBus) { // 文本变更统一处理方法
        int cmdCh=TChan.toFpgaChNo(serialsNumber); // 获取FPGA命令通道号
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 获取FPGA物理通道号

        text = (text.charAt(0) - '0') > 3 ? "3F" : text;//此结果最大值为3F
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_FRAMEID + getSerialsNumber(), text); // 缓存帧ID值
        linFrameIdEditEdit.setEdit(text); // 更新帧ID编辑框显示
        msgLinFrameId.setLinFrameIdEditEdit(DIGITS_16, text); // 更新消息Bean帧ID
        sendMsg(msgLinFrameId, isFromEventBus); // 发送触发消息
        Command.get().getTrigger_lin().setType(cmdCh, 1, toD(text, 16), 0, false); // 下发LIN触发指令到FPGA（类型1=帧ID）
        if (!isFromEventBus) { // 非EventBus来源时同步到软件解码总线
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道
            if(serialChannel == null) return; // 通道为空则返回
            LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN); // 获取LIN总线
            linBus.setFrameId(LinBus.LIN_TRIGGER_FRAME_ID, toD(text, 16)); // 设置LIN触发帧ID
        }
    }
}
