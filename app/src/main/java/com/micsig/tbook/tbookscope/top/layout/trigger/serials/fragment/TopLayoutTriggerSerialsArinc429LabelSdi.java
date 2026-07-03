package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment; // ARINC429 Label+SDI触发条件Fragment所在包

import android.view.View; // 导入视图基类，用于Fragment视图操作

import com.micsig.tbook.scope.Bus.ARINC429Bus; // 导入ARINC429总线操作类，用于设置触发参数
import com.micsig.tbook.scope.Bus.IBus; // 导入总线接口常量，用于获取指定类型总线
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂，用于获取串口通道实例
import com.micsig.tbook.scope.channel.SerialChannel; // 导入串口通道类，用于访问总线对象
import com.micsig.tbook.tbookscope.R; // 导入资源ID常量类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件，用于发送FPGA触发指令
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials; // 导入右侧串口菜单常量，用于判断串口类型索引
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials; // 导入右侧串口消息类，用于接收串口切换事件
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效工具，用于按键音反馈
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger; // 导入触发器主容器，用于获取触发详情索引常量
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils; // 导入串口工具类，用于进制转换
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail; // 导入串口详情接口，用于返回触发详情数据
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429LabelSdi; // 导入ARINC429 Label+SDI数据模型
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 导入进制常量接口（DIGITS_2=二进制, DIGITS_8=八进制）
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘对话框，用于输入触发值
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具，用于读写SharedPreferences持久化数据
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入顶部可编辑视图组件
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean，用于单选回调
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入顶部单选组视图，用于单选监听回调
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道工具类，用于串口号与FPGA通道号转换

/**
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │          TopLayoutTriggerSerialsArinc429LabelSdi                   │
 * │            ARINC429 Label+SDI 触发条件配置Fragment                  │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：top.layout.trigger.serials.fragment → 串口触发子页面      │
 * │ 核心职责：配置ARINC429总线的Label(标签)+SDI(源目标标识)触发条件      │
 * │ 架构设计：继承BaseDetail，复用通用触发框架，仅实现差异化逻辑         │
 * │ 数据流向：UI输入→CacheUtil持久化→Command发送FPGA→ARINC429Bus同步    │
 * │ 依赖关系：SerialsDetailArinc429LabelSdi(数据模型)、                 │
 * │           TopDialogNumberKeyBoard(数字键盘)、CacheUtil(持久化)       │
 * │ 使用场景：Micsig示波器ARINC429协议解码时，按Label+SDI组合触发捕获    │
 * ├─────────────────────────────────────────────────────────────────────┤
 * │ 触发类型索引：RightLayoutSerials.SERIALS_M429 子项6                 │
 * │ Label：8进制，3位限制（0~511，ARINC429标准Label范围）              │
 * │ SDI：2进制，2位限制（0~3，ARINC429标准SDI范围）                    │
 * └─────────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/6/9.
 */

public class TopLayoutTriggerSerialsArinc429LabelSdi extends TopLayoutTriggerSerialsBaseDetail { // ARINC429 Label+SDI触发条件Fragment，继承串口触发基类
    private TopViewEdit arinc429LabelSdiLabel; // Label标签输入框，8进制3位
    private TopViewEdit arinc429LabelSdiSdi; // SDI源目标标识输入框，2进制2位
    private SerialsDetailArinc429LabelSdi msgArinc429LabelSdi; // ARINC429 Label+SDI触发数据模型

    /**
     * 初始化视图控件和数据模型
     * @param view Fragment根视图
     */
    @Override
    protected void initView(View view) { // 初始化视图方法，由基类onCreateView调用
        arinc429LabelSdiLabel = (TopViewEdit) view.findViewById(R.id.arinc429LabelSdiLabel); // 查找Label输入框控件
        arinc429LabelSdiSdi = (TopViewEdit) view.findViewById(R.id.arinc429LabelSdiSdi); // 查找SDI输入框控件
        arinc429LabelSdiLabel.setOnClickEditListener(onClickEditListener); // 设置Label输入框点击监听，弹出数字键盘
        arinc429LabelSdiSdi.setOnClickEditListener(onClickEditListener); // 设置SDI输入框点击监听，弹出数字键盘
        msgArinc429LabelSdi = new SerialsDetailArinc429LabelSdi(); // 创建ARINC429 Label+SDI数据模型实例
        msgArinc429LabelSdi.setArinc429LabelSdiLabel(IDigits.DIGITS_8, arinc429LabelSdiLabel.getText()); // 初始化Label字段（8进制，当前显示值）
        msgArinc429LabelSdi.setArinc429LabelSdiSdi(IDigits.DIGITS_2, arinc429LabelSdiSdi.getText()); // 初始化SDI字段（2进制，当前显示值）
        msgArinc429LabelSdi.setArinc429LabelSdiLabelTitle(arinc429LabelSdiLabel.getHead()); // 设置Label字段标题（如"Label"）
        msgArinc429LabelSdi.setArinc429LabelSdiSdiTitle(arinc429LabelSdiSdi.getHead()); // 设置SDI字段标题（如"SDI"）
    } // initView方法结束

    /**
     * 获取布局资源ID
     * @return 布局资源ID
     */
    @Override
    protected int getLayoutResId() { // 获取Fragment布局资源ID的方法
        return R.layout.layout_triggerserials_arinc429labelsdi; // 返回ARINC429 Label+SDI触发布局
    } // getLayoutResId方法结束

    /**
     * 获取串口触发详情数据
     * @param detailFlag 详情标志位
     * @return ARINC429 Label+SDI触发数据
     */
    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) { // 获取当前触发详情数据的方法
        String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_LABEL + getSerialsNumber()); // 从缓存读取Label值
        String sdi = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_SDI + getSerialsNumber()); // 从缓存读取SDI值
        msgArinc429LabelSdi.setArinc429LabelSdiLabel(IDigits.DIGITS_8, label); // 更新模型中的Label字段
        msgArinc429LabelSdi.setArinc429LabelSdiSdi(IDigits.DIGITS_2, sdi); // 更新模型中的SDI字段
        return msgArinc429LabelSdi; // 返回填充后的数据模型
    } // getSerialsDetail方法结束

    /**
     * 处理右侧串口消息，条件匹配时向外发送触发数据
     * @param rightMsgSerials 右侧串口切换消息
     */
    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) { // 消费右侧串口消息的方法
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1 // 判断：是串口1的消息且当前Fragment对应串口1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_M429 // 且串口类型是ARINC429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 6) { // 且429子项选中的是第6项(Label+SDI)
            //当且仅当当前429列表选中的是该项时，才向外发送消息
            sendMsg(msgArinc429LabelSdi, rightMsgSerials.isFromEventBus()); // 条件全部满足，发送触发数据消息
        } // if条件判断结束
    } // setConsumer方法结束

    /**
     * 从缓存恢复UI状态并同步到FPGA硬件
     */
    @Override
    protected void setCache() { // 从缓存恢复数据并同步硬件的方法
        String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_LABEL + getSerialsNumber()); // 读取缓存的Label值
        String sdi = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_SDI + getSerialsNumber()); // 读取缓存的SDI值
        arinc429LabelSdiLabel.setText(label); // 设置Label输入框显示值
        arinc429LabelSdiSdi.setText(sdi); // 设置SDI输入框显示值

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber); // 将串口号转换为FPGA通道号
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取对应FPGA通道的串口通道实例
        if(serialChannel == null) return; // 通道不存在则直接返回
        ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429); // 从串口通道获取ARINC429总线对象
        a429Bus.setSdi(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI, toD(sdi, IDigits.DIGITS_2)); // 设置ARINC429总线的SDI触发值（2进制转10进制）
        a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI, toD(label, IDigits.DIGITS_8)); // 设置ARINC429总线的Label触发值（8进制转10进制）

        msgArinc429LabelSdi.setArinc429LabelSdiLabel(IDigits.DIGITS_8, label); // 同步更新数据模型中的Label
        msgArinc429LabelSdi.setArinc429LabelSdiSdi(IDigits.DIGITS_2, sdi); // 同步更新数据模型中的SDI
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发详情页索引
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1) // 判断：触发页是串口1且当前Fragment对应串口1
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2) // 或触发页是串口2且当前Fragment对应串口2
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3) // 或触发页是串口3且当前Fragment对应串口3
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4)) // 或触发页是串口4且当前Fragment对应串口4
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_M429 // 且右侧菜单选中ARINC429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 6) { // 且429子项选中第6项(Label+SDI)
            sendMsg(msgArinc429LabelSdi, false); // 当前页面处于激活状态，发送触发数据消息
        } // if条件判断结束
    } // setCache方法结束

    /**
     * 通过指令数据设置Label和SDI值（用于EventBus远程同步）
     * @param label Label值（整数）
     * @param sdi SDI值（整数）
     * @param isFromEventBus 是否来自EventBus远程事件
     */
    public void setCommandData(int label, int sdi, boolean isFromEventBus) { // 设置指令数据的方法，用于远程同步
        String sLabel = SerialsUtils.getHexBinFromLong(label, 3, DIGITS_8); // 将Label整数值转为8进制3位字符串
        String sSdi = SerialsUtils.getHexBinFromLong(sdi, 2, DIGITS_2); // 将SDI整数值转为2进制2位字符串
        if (!arinc429LabelSdiLabel.getText().equals(sLabel)) { // 如果Label值与当前显示不同
            onTextListener(arinc429LabelSdiLabel, sLabel, isFromEventBus); // 触发Label变更监听，更新UI和硬件
        } // Label变更判断结束
        if (!arinc429LabelSdiSdi.getText().equals(sSdi)) { // 如果SDI值与当前显示不同
            onTextListener(arinc429LabelSdiSdi, sSdi, isFromEventBus); // 触发SDI变更监听，更新UI和硬件
        } // SDI变更判断结束
    } // setCommandData方法结束

    /**
     * 单选组变更监听（本Fragment无单选组，空实现）
     * @param view 单选组视图
     * @param item 选中的通道项
     */
    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) { // 单选组变更监听，本场景无单选项

    } // 空实现

    /**
     * 输入框点击监听，弹出对应进制的数字键盘
     * @param v 被点击的编辑框
     * @param text 当前文本值
     */
    @Override
    protected void setOnClickEditListener(final TopViewEdit v, String text) { // 输入框点击回调方法
        PlaySound.getInstance().playButton(); // 播放按键音效
        switch (v.getId()) { // 根据点击的控件ID分发
            case R.id.arinc429LabelSdiLabel: // 点击Label输入框
                //该为8进制,3位限制
                dialogKeyBoard.setDecimalData(3, DIGITS_8, onLabelListener); // 弹出8进制数字键盘，3位限制，监听Label输入结果
                break; // Label分支结束
            case R.id.arinc429LabelSdiSdi: // 点击SDI输入框
                //该为2进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_2, onSdiListener); // 弹出2进制数字键盘，2位限制，监听SDI输入结果
                break; // SDI分支结束
        } // switch结束
    } // setOnClickEditListener方法结束

    /**
     * Label输入框键盘关闭监听器
     */
    private TopDialogNumberKeyBoard.OnDismissListener onLabelListener = new TopDialogNumberKeyBoard.OnDismissListener() { // Label键盘关闭监听器
        @Override
        public void onDismiss(String result) { // 键盘关闭回调
            onTextListener(arinc429LabelSdiLabel, result, false); // 将输入结果传递给文本变更监听
        } // onDismiss方法结束
    }; // onLabelListener定义结束

    /**
     * SDI输入框键盘关闭监听器
     */
    private TopDialogNumberKeyBoard.OnDismissListener onSdiListener = new TopDialogNumberKeyBoard.OnDismissListener() { // SDI键盘关闭监听器
        @Override
        public void onDismiss(String result) { // 键盘关闭回调
            onTextListener(arinc429LabelSdiSdi, result, false); // 将输入结果传递给文本变更监听

        } // onDismiss方法结束
    }; // onSdiListener定义结束

    /**
     * 文本变更核心处理方法，更新UI、缓存、数据模型、FPGA指令和总线
     * @param view 变更的编辑框
     * @param text 新文本值
     * @param isFromEventBus 是否来自EventBus远程事件
     */
    private void onTextListener(TopViewEdit view, String text, boolean isFromEventBus) { // 文本变更核心处理方法
        int cmdCh=TChan.toFpgaChNo(serialsNumber); // 获取命令通道号（0基索引）
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 获取FPGA通道号（1基索引）

        if (view.getId() == arinc429LabelSdiLabel.getId()) { // 如果变更的是Label输入框
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_LABEL + getSerialsNumber(), text); // 持久化Label值到缓存
            arinc429LabelSdiLabel.setEdit(text); // 更新Label输入框显示
            msgArinc429LabelSdi.setArinc429LabelSdiLabel(IDigits.DIGITS_8, text); // 更新数据模型中的Label
            sendMsg(msgArinc429LabelSdi, isFromEventBus); // 发送触发数据消息
            Command.get().getTrigger_m429().setType(serialsNumber, 6, toD(arinc429LabelSdiLabel.getText(), IDigits.DIGITS_8), toD(arinc429LabelSdiSdi.getText(), 2), 0, 0, false); // 发送FPGA触发指令：类型6=Label+SDI，传入Label和SDI的十进制值

            if (!isFromEventBus) { // 非EventBus来源时同步硬件总线
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
                if(serialChannel == null) return; // 通道不存在则返回
                ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429); // 获取ARINC429总线对象
                a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI, toD(text, 8)); // 设置总线Label触发值（8进制转10进制）
            } // 硬件同步结束
        } else if (view.getId() == arinc429LabelSdiSdi.getId()) { // 如果变更的是SDI输入框
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABELSDI_SDI + getSerialsNumber(), text); // 持久化SDI值到缓存
            arinc429LabelSdiSdi.setEdit(text); // 更新SDI输入框显示
            msgArinc429LabelSdi.setArinc429LabelSdiSdi(IDigits.DIGITS_2, text); // 更新数据模型中的SDI
            sendMsg(msgArinc429LabelSdi, isFromEventBus); // 发送触发数据消息
            Command.get().getTrigger_m429().setType(cmdCh, 6, toD(arinc429LabelSdiLabel.getText(), IDigits.DIGITS_8), toD(arinc429LabelSdiSdi.getText(), 2), 0, 0, false); // 发送FPGA触发指令：类型6=Label+SDI，传入Label和SDI的十进制值

            if (!isFromEventBus) { // 非EventBus来源时同步硬件总线
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
                if(serialChannel == null) return; // 通道不存在则返回
                ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429); // 获取ARINC429总线对象
                a429Bus.setSdi(ARINC429Bus.ARINC429_TRIGGER_LABEL_SDI, toD(text, IDigits.DIGITS_2)); // 设置总线SDI触发值（2进制转10进制）
            } // 硬件同步结束
        } // Label/SDI分支判断结束
    } // onTextListener方法结束
} // TopLayoutTriggerSerialsArinc429LabelSdi类结束
