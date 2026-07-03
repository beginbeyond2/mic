package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment; // 包声明：ARINC429串行触发片段类所在包

import android.view.View; // 导入Android视图基类

import com.micsig.tbook.scope.Bus.ARINC429Bus; // 导入ARINC429总线类
import com.micsig.tbook.scope.Bus.IBus; // 导入总线接口
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类
import com.micsig.tbook.scope.channel.SerialChannel; // 导入串行通道类
import com.micsig.tbook.tbookscope.R; // 导入资源引用类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials; // 导入右侧滑出菜单串行布局类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials; // 导入右侧滑出菜单串行消息类
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具类
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger; // 导入顶部触发布局类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils; // 导入串行工具类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail; // 导入串行详情接口
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Label; // 导入ARINC429 Label详情Bean
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 导入进制位数常量接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘弹窗类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入顶部编辑视图类
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入顶部通道Bean类
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入顶部单选组视图类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道转换工具类

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位：顶部触发区域 → 串行总线触发 → ARINC429协议 → Label字段触发配置    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责：                                                                ║
 * ║    1. 管理ARINC429总线触发条件中Label字段的UI展示与交互                      ║
 * ║    2. 处理Label字段的输入（8进制数字键盘）、缓存读写                         ║
 * ║    3. 将Label字段变更同步到FPGA硬件和EventBus消息总线                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  架构设计：                                                                ║
 * ║    继承 TopLayoutTriggerSerialsBaseDetail（串行触发详情基类）                 ║
 * ║    实现 initView/getLayoutResId/getSerialsDetail/setConsumer/setCache       ║
 * ║    等模板方法，完成ARINC429 Label字段的完整生命周期管理                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向：                                                                ║
 * ║    UI输入 → CacheUtil缓存 → Command指令(FPGA) → ARINC429Bus(硬件总线)       ║
 * ║    外部消息 → setConsumer/setCommandData → UI更新 → 缓存同步 → 硬件下发     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系：                                                                ║
 * ║    - SerialsDetailArinc429Label：Label字段数据Bean                         ║
 * ║    - ARINC429Bus：ARINC429硬件总线通信                                      ║
 * ║    - Command：FPGA指令中间件                                                ║
 * ║    - CacheUtil：键值缓存读写                                                ║
 * ║    - TopDialogNumberKeyBoard：数字键盘弹窗                                  ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  使用场景：                                                                ║
 * ║    用户在触发设置中选择ARINC429串行总线触发模式，并选择"Label"子项时，        ║
 * ║    该Fragment负责Label字段值的显示、编辑和下发                               ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * ARINC429串行总线触发——Label字段配置Fragment
 * <p>
 * 负责ARINC429触发条件中Label字段的UI展示、用户交互、缓存同步和硬件下发。
 * Label字段固定使用8进制显示，3位限制。
 * </p>
 *
 * @author yangj
 * @since 2017/6/9
 */
public class TopLayoutTriggerSerialsArinc429Label extends TopLayoutTriggerSerialsBaseDetail { // 继承串行触发详情基类
    /** Label字段的编辑输入框视图 */
    private TopViewEdit arinc429LabelLabel; // Label字段编辑视图
    /** ARINC429 Label字段的详情数据Bean，用于消息传递 */
    private SerialsDetailArinc429Label msgArinc429Label; // ARINC429 Label字段消息Bean

    /**
     * 初始化视图控件，绑定Label字段的编辑框和点击监听器
     *
     * @param view Fragment的根视图
     */
    @Override // 标记重写父类方法
    protected void initView(View view) { // 初始化视图方法
        arinc429LabelLabel = (TopViewEdit) view.findViewById(R.id.arinc429LabelLabel); // 查找Label字段编辑框视图
        arinc429LabelLabel.setOnClickEditListener(onClickEditListener); // 设置编辑框点击监听器
        msgArinc429Label = new SerialsDetailArinc429Label(); // 创建ARINC429 Label消息Bean实例
        msgArinc429Label.setArinc429LabelLabel(DIGITS_8, arinc429LabelLabel.getText()); // 设置消息Bean的Label字段值（8进制）
        msgArinc429Label.setArinc429LabelLabelTitle(arinc429LabelLabel.getHead()); // 设置消息Bean的Label字段标题
    } // initView方法结束

    /**
     * 获取布局资源ID
     *
     * @return ARINC429 Label字段的布局资源ID
     */
    @Override // 标记重写父类方法
    protected int getLayoutResId() { // 获取布局资源ID方法
        return R.layout.layout_triggerserials_arinc429label; // 返回ARINC429 Label布局
    } // getLayoutResId方法结束

    /**
     * 获取串行触发详情数据，从缓存读取Label字段值并设置到消息Bean中
     *
     * @param detailFlag 详情标识，用于区分不同的详情类型
     * @return 包含Label字段信息的串行详情对象
     */
    @Override // 标记重写父类方法
    public ISerialsDetail getSerialsDetail(int detailFlag) { // 获取串行详情方法
        String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABEL + getSerialsNumber()); // 从缓存读取Label字段值
        msgArinc429Label.setArinc429LabelLabel(DIGITS_8, label); // 设置消息Bean的Label字段值（8进制）
        return msgArinc429Label; // 返回ARINC429 Label消息Bean
    } // getSerialsDetail方法结束

    /**
     * 处理右侧菜单串行消息的消费，当串行号和类型匹配且选中Label项时发送消息
     *
     * @param rightMsgSerials 右侧菜单发送的串行消息
     */
    @Override // 标记重写父类方法
    protected void setConsumer(RightMsgSerials rightMsgSerials) { // 消费右侧消息方法
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1 // 判断是否为串行1且串行号匹配S1
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_M429 // 判断串行类型是否为ARINC429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 2) { // 判断429子项是否为Label
            //当且仅当当前429列表选中的是该项时，才向外发送消息
            sendMsg(msgArinc429Label, rightMsgSerials.isFromEventBus()); // 发送Label字段变更消息
        } // 条件判断结束
    } // setConsumer方法结束

    /**
     * 从缓存恢复Label字段状态，同步到UI和FPGA硬件
     */
    @Override // 标记重写父类方法
    protected void setCache() { // 设置缓存方法
        String label = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABEL + getSerialsNumber()); // 从缓存读取Label字段值
        arinc429LabelLabel.setText(label); // 更新编辑框显示文本

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber); // 将串行号转换为FPGA通道号
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串行通道实例
        if(serialChannel == null) return; // 通道为空则直接返回
        ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429); // 从通道获取ARINC429总线
        a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL, toD(label, IDigits.DIGITS_8)); // 向总线设置Label触发值

        msgArinc429Label.setArinc429LabelLabel(DIGITS_8, label); // 设置消息Bean的Label字段值（8进制）
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发通道索引
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1) // 判断是否为S1通道且触发详情为S1
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2) // 判断是否为S2通道且触发详情为S2
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3) // 判断是否为S3通道且触发详情为S3
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4)) // 判断是否为S4通道且触发详情为S4
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_M429 // 判断串行类型是否为ARINC429
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + getSerialsNumber()) == 2) { // 判断429子项是否为Label
            sendMsg(msgArinc429Label, false); // 发送Label字段消息（非EventBus来源）
        } // 触发条件判断结束
    } // setCache方法结束

    /**
     * 从外部命令设置Label字段值，当值与当前不同时触发更新
     *
     * @param label          Label字段的整数值
     * @param isFromEventBus 是否来自EventBus消息
     */
    public void setCommandData(int label, boolean isFromEventBus) { // 设置命令数据方法
        String sLabel = SerialsUtils.getHexBinFromLong(label, 3, DIGITS_8); // 将整数值转换为8进制3位字符串
        if (!arinc429LabelLabel.getText().equals(sLabel)) { // 判断新值是否与当前值不同
            onTextListener(sLabel, isFromEventBus); // 值不同则触发文本变更监听器
        } // 值比较判断结束
    } // setCommandData方法结束

    /**
     * 设置单选按钮变更监听器（ARINC429 Label模式无单选项，空实现）
     *
     * @param view 单选组视图
     * @param item 通道Bean项
     */
    @Override // 标记重写父类方法
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) { // 单选变更监听方法

    } // 空实现

    /**
     * 处理编辑框点击事件，弹出8进制数字键盘供用户输入Label值
     *
     * @param v    被点击的编辑框视图
     * @param text 当前编辑框文本
     */
    @Override // 标记重写父类方法
    protected void setOnClickEditListener(final TopViewEdit v, String text) { // 编辑框点击监听方法
        PlaySound.getInstance().playButton(); // 播放按钮点击音效
        switch (v.getId()) { // 根据视图ID分支处理
            case R.id.arinc429LabelLabel: // Label字段编辑框被点击
                //该为8进制,3位限制
                dialogKeyBoard.setDecimalData(3, DIGITS_8, onLabelListener); // 设置数字键盘为8进制3位限制
                break; // 跳出switch
        } // switch结束
    } // setOnClickEditListener方法结束

    /** Label字段数字键盘关闭监听器，将键盘输入结果传递给文本监听器 */
    private TopDialogNumberKeyBoard.OnDismissListener onLabelListener = new TopDialogNumberKeyBoard.OnDismissListener() { // Label键盘关闭监听器
        @Override // 标记重写接口方法
        public void onDismiss(String result) { // 键盘关闭回调方法
            onTextListener(result, false); // 将键盘输入结果传递给文本监听器（非EventBus来源）
        } // onDismiss方法结束
    }; // onLabelListener定义结束

    /**
     * Label字段文本变更处理核心方法，同步更新缓存、UI、FPGA指令和硬件总线
     *
     * @param text           新的Label字段文本值
     * @param isFromEventBus 是否来自EventBus消息
     */
    private void onTextListener(String text, boolean isFromEventBus) { // 文本变更监听方法
        int cmdCh=TChan.toFpgaChNo(serialsNumber); // 将串行号转换为FPGA通道编号
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 将串行号转换为FPGA通道号

        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429_LABEL + getSerialsNumber(), text); // 将新值写入缓存
        arinc429LabelLabel.setEdit(text); // 更新编辑框显示文本
        msgArinc429Label.setArinc429LabelLabel(DIGITS_8, text); // 同步更新消息Bean（8进制）
        sendMsg(msgArinc429Label, isFromEventBus); // 发送Label字段变更消息
        Command.get().getTrigger_m429().setType(cmdCh, 2, toD(text, 8), 0, 0, 0, false); // 下发ARINC429 Label触发类型指令到FPGA

        if (!isFromEventBus) { // 非EventBus来源时才向硬件总线写入
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串行通道实例
            if(serialChannel == null) return; // 通道为空则直接返回
            ARINC429Bus a429Bus = (ARINC429Bus) serialChannel.getBus(IBus.ARINC429); // 从通道获取ARINC429总线
            a429Bus.setLabel(ARINC429Bus.ARINC429_TRIGGER_LABEL, toD(text, IDigits.DIGITS_8)); // 向总线设置Label触发值
        } // EventBus来源判断结束
    } // onTextListener方法结束
} // 类定义结束
