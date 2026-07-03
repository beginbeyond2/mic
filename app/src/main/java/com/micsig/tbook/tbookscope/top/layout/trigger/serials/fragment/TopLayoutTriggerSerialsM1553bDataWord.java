package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment; // 串口触发子条件Fragment包

import android.view.View; // 导入Android视图基类

import com.micsig.tbook.scope.Bus.IBus; // 导入总线接口定义
import com.micsig.tbook.scope.Bus.MILSTD1553BBus; // 导入MIL-STD-1553B总线操作类
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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailM1553bDataWord; // 导入M1553B数据字触发详情Bean
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入顶部可编辑视图组件
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道Bean，用于单选按钮组数据
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选按钮组视图
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道转换工具类

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: 顶部触发布局 → 串口触发 → M1553B子条件 → 数据字触发Fragment        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                 ║
 * ║    1. 提供MIL-STD-1553B数据字触发的UI交互界面（数据字输入）                    ║
 * ║    2. 支持2进制/16进制显示模式切换                                           ║
 * ║    3. 管理数据字的缓存读写与FPGA指令下发                                     ║
 * ║    4. 将用户输入同步到MILSTD1553BBus总线和Command命令通道                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  架构设计:                                                                 ║
 * ║    继承TopLayoutTriggerSerialsBaseDetail基类，实现模板方法模式                ║
 * ║    - initView(): 初始化数据字编辑框和消息Bean                                ║
 * ║    - setCache(): 从缓存恢复状态并同步到FPGA                                  ║
 * ║    - setConsumer(): 响应右侧菜单消息，条件匹配时发送触发消息                  ║
 * ║    - onTextListener(): 统一处理文本变更，更新缓存/Bean/FPGA                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向:                                                                 ║
 * ║    UI编辑框 → CacheUtil缓存 → SerialsDetailM1553bDataWord Bean             ║
 * ║              → Command指令(FPGA) → MILSTD1553BBus总线(软件解码)              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系:                                                                 ║
 * ║    - TopLayoutTriggerSerialsBaseDetail (继承)                               ║
 * ║    - MILSTD1553BBus / SerialChannel / ChannelFactory (总线与通道)            ║
 * ║    - Command (FPGA指令下发)                                                 ║
 * ║    - CacheUtil (持久化缓存)                                                 ║
 * ║    - TopDialogNumberKeyBoard (数字键盘弹窗)                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  使用场景:                                                                 ║
 * ║    当用户在M1553B触发模式下选择"数据字"子条件时，加载此Fragment                ║
 * ║    对应M1553B子条件索引值为5，支持2进制(16位)/16进制(4位)显示                  ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

public class TopLayoutTriggerSerialsM1553bDataWord extends TopLayoutTriggerSerialsBaseDetail { // M1553B数据字触发Fragment，继承串口触发详情基类
    private SerialsDetailM1553bDataWord msgM1553bDataWord; // M1553B数据字触发详情消息Bean
    private TopViewEdit m1553bDataWordData; // 数据字编辑框

    /**
     * 初始化视图控件及消息Bean
     * @param view Fragment根视图
     */
    @Override
    protected void initView(View view) { // 重写基类方法，初始化视图控件
        m1553bDataWordData = (TopViewEdit) view.findViewById(R.id.m1553bDataWordData); // 查找数据字编辑框控件
        m1553bDataWordData.setOnClickEditListener(onClickEditListener); // 为数据字编辑框设置点击监听
        msgM1553bDataWord = new SerialsDetailM1553bDataWord(); // 创建M1553B数据字消息Bean实例
        msgM1553bDataWord.setM1553bDataWordData(digits, m1553bDataWordData.getText()); // 设置数据字值（使用当前进制）
        msgM1553bDataWord.setM1553bDataWordDataTitle(m1553bDataWordData.getHead()); // 设置数据字字段标题
    }

    /**
     * 获取布局资源ID
     * @return M1553B数据字布局资源ID
     */
    @Override
    protected int getLayoutResId() { // 重写基类方法，返回布局资源ID
        return R.layout.layout_triggerserials_m1553bdataword; // 返回M1553B数据字布局
    }

    /**
     * 获取串口触发详情数据
     * @param detailFlag 详情标志位
     * @return M1553B数据字触发详情Bean
     */
    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) { // 重写基类方法，获取串口触发详情
        String dataWord = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + getSerialsNumber()); // 从缓存读取数据字值
        int digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16; // 根据显示模式确定进制
        msgM1553bDataWord.setM1553bDataWordData(digits, dataWord); // 设置数据字到消息Bean
        return msgM1553bDataWord; // 返回消息Bean
    }

    /**
     * 响应右侧菜单消息，条件匹配时发送触发消息
     * @param rightMsgSerials 右侧串口菜单消息
     */
    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) { // 重写基类方法，处理右侧菜单消息
        if (serialsNumber == rightMsgSerials.getSerialsNumber() // 判断串口序号是否匹配
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_M1553B) { // 判断串口类型是否为M1553B
            digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16; // 根据显示模式更新进制
            if (m1553bDataWordData != null) { // 编辑框已初始化
                String dataWord = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + getSerialsNumber()); // 从缓存读取数据字
                m1553bDataWordData.setText(dataWord); // 更新编辑框文本
                msgM1553bDataWord.setM1553bDataWordData(digits, m1553bDataWordData.getText()); // 更新消息Bean数据字
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + getSerialsNumber()) == 5) { // 判断M1553B子条件是否为数据字(索引5)
                    //当且仅当当前1554b列表选中的是该项时，才向外发送消息
                    sendMsg(msgM1553bDataWord, rightMsgSerials.isFromEventBus()); // 发送M1553B数据字触发消息
                }
            }
        }
    }

    /**
     * 从缓存恢复状态并同步到FPGA和总线
     */
    @Override
    protected void setCache() { // 重写基类方法，从缓存恢复数据
        String dataWord = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + getSerialsNumber()); // 从缓存读取数据字
        digits = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + getSerialsNumber()) == 0 ? DIGITS_2 : DIGITS_16; // 根据显示模式确定进制
        m1553bDataWordData.setText(dataWord); // 设置数据字编辑框文本

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber); // 将串口序号转换为FPGA通道号
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
        if (serialChannel == null) return; // 通道为空则直接返回
        MILSTD1553BBus m1553bBus = (MILSTD1553BBus) serialChannel.getBus(IBus.MILSTD1553B); // 获取M1553B总线实例
        m1553bBus.setData(toD(dataWord, digits)); // 设置M1553B数据字

        msgM1553bDataWord.setM1553bDataWordData(digits, dataWord); // 同步数据字到消息Bean
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发详情索引
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1) // 判断是否为S1触发详情且当前为S1
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2) // 判断是否为S2触发详情且当前为S2
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3) // 判断是否为S3触发详情且当前为S3
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4)) // 判断是否为S4触发详情且当前为S4
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_M1553B // 判断右侧菜单是否选中M1553B
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + getSerialsNumber()) == 5) { // 判断M1553B子条件是否为数据字
            sendMsg(msgM1553bDataWord, false); // 发送触发消息（非EventBus来源）
        }
    }

    /**
     * 从外部命令设置触发数据（如EventBus远程同步）
     * @param dataWord 数据字值
     * @param isFromEventBus 是否来自EventBus
     */
    public void setCommandData(int dataWord, boolean isFromEventBus) { // 外部命令设置触发数据
        String sDataWord = SerialsUtils.getHexBinFromLong(dataWord, digits == DIGITS_16 ? 4 : 16, digits); // 将数据字转为对应进制字符串（16进制4位或2进制16位）
        if (!m1553bDataWordData.getText().equals(sDataWord)) { // 数据字值有变化时
            onTextListener(sDataWord, isFromEventBus); // 触发文本变更监听
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
            case R.id.m1553bDataWordData: // 数据字编辑框
                //该为2进制,16位限制//16进制，4位
                dialogKeyBoard.setDecimalData(digits == DIGITS_16 ? 4 : 16, digits, onDataListener); // 根据进制设置键盘位数（16进制4位或2进制16位），绑定数据字监听
                break;
        }
    }

    /** 数据字键盘关闭监听，将结果传递给文本变更监听 */
    private TopDialogNumberKeyBoard.OnDismissListener onDataListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 数据字键盘关闭回调
        @Override
        public void onDismiss(String result) { // 键盘关闭时回调
            onTextListener(result, false); // 触发数据字文本变更（非EventBus来源）
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
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B_DATAWORD + getSerialsNumber(), text); // 缓存数据字值
        m1553bDataWordData.setEdit(text); // 更新数据字编辑框显示
        msgM1553bDataWord.setM1553bDataWordData(digits, text); // 更新消息Bean数据字
        sendMsg(msgM1553bDataWord, isFromEventBus); // 发送触发消息
        Command.get().getTrigger_m1553B().setType(cmdCh, 5, 0, 0, toD(text, digits), false); // 下发M1553B触发指令到FPGA（类型5=数据字）

        if (!isFromEventBus) { // 非EventBus来源时同步到软件解码总线
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道
            if (serialChannel == null) return; // 通道为空则返回
            MILSTD1553BBus m1553bBus = (MILSTD1553BBus) serialChannel.getBus(IBus.MILSTD1553B); // 获取M1553B总线
            m1553bBus.setData(toD(text, digits)); // 设置M1553B数据字
        }
    }
}
