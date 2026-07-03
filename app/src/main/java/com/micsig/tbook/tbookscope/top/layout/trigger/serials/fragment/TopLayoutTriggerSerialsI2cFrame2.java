package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment; // 串口触发子条件Fragment包

import android.view.View; // 导入Android视图基类

import com.micsig.tbook.scope.Bus.I2CBus; // 导入I2C总线操作类
import com.micsig.tbook.scope.Bus.IBus; // 导入总线接口定义
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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cFrame2; // 导入I2C帧2触发详情Bean
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 导入进制常量接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入顶部可编辑视图组件
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道Bean，用于单选按钮组数据
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选按钮组视图
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道转换工具类

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: 顶部触发布局 → 串口触发 → I2C子条件 → 帧2触发Fragment              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                 ║
 * ║    1. 提供I2C帧2触发的UI交互界面（地址+数据1+数据2输入）                       ║
 * ║    2. 管理地址和两个数据的缓存读写与FPGA指令下发                              ║
 * ║    3. 将用户输入同步到I2CBus总线和Command命令通道                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  架构设计:                                                                 ║
 * ║    继承TopLayoutTriggerSerialsBaseDetail基类，实现模板方法模式                ║
 * ║    - initView(): 初始化地址/数据1/数据2编辑框和消息Bean                      ║
 * ║    - setCache(): 从缓存恢复状态并同步到FPGA                                  ║
 * ║    - setConsumer(): 响应右侧菜单消息，条件匹配时发送触发消息                  ║
 * ║    - onTextListener(): 统一处理文本变更，更新缓存/Bean/FPGA                   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向:                                                                 ║
 * ║    UI编辑框 → CacheUtil缓存 → SerialsDetailI2cFrame2 Bean                  ║
 * ║              → Command指令(FPGA) → I2CBus总线(软件解码)                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系:                                                                 ║
 * ║    - TopLayoutTriggerSerialsBaseDetail (继承)                               ║
 * ║    - I2CBus / SerialChannel / ChannelFactory (总线与通道)                   ║
 * ║    - Command (FPGA指令下发)                                                 ║
 * ║    - CacheUtil (持久化缓存)                                                 ║
 * ║    - TopDialogNumberKeyBoard (数字键盘弹窗)                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  使用场景:                                                                 ║
 * ║    当用户在I2C触发模式下选择"帧2"子条件时，加载此Fragment                     ║
 * ║    对应I2C子条件索引值为6                                                   ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

public class TopLayoutTriggerSerialsI2cFrame2 extends TopLayoutTriggerSerialsBaseDetail { // I2C帧2触发Fragment，继承串口触发详情基类
    private SerialsDetailI2cFrame2 msgI2cFrame2; // I2C帧2触发详情消息Bean
    private TopViewEdit i2cFrame2Addr; // 帧2地址编辑框
    private TopViewEdit i2cFrame2Data1; // 帧2数据1编辑框
    private TopViewEdit i2cFrame2Data2; // 帧2数据2编辑框

    /**
     * 初始化视图控件及消息Bean
     * @param view Fragment根视图
     */
    @Override
    protected void initView(View view) { // 重写基类方法，初始化视图控件
        i2cFrame2Addr = (TopViewEdit) view.findViewById(R.id.i2cFrame2Addr); // 查找地址编辑框控件
        i2cFrame2Data1 = (TopViewEdit) view.findViewById(R.id.i2cFrame2Data1); // 查找数据1编辑框控件
        i2cFrame2Data2 = (TopViewEdit) view.findViewById(R.id.i2cFrame2Data2); // 查找数据2编辑框控件
        i2cFrame2Addr.setOnClickEditListener(onClickEditListener); // 为地址编辑框设置点击监听
        i2cFrame2Data1.setOnClickEditListener(onClickEditListener); // 为数据1编辑框设置点击监听
        i2cFrame2Data2.setOnClickEditListener(onClickEditListener); // 为数据2编辑框设置点击监听
        msgI2cFrame2 = new SerialsDetailI2cFrame2(); // 创建I2C帧2消息Bean实例
        msgI2cFrame2.setI2cFrame2Addr(DIGITS_16, i2cFrame2Addr.getText()); // 设置地址值（16进制）
        msgI2cFrame2.setI2cFrame2Data1(DIGITS_16, i2cFrame2Data1.getText()); // 设置数据1值（16进制）
        msgI2cFrame2.setI2cFrame2Data2(DIGITS_16, i2cFrame2Data2.getText()); // 设置数据2值（16进制）
        msgI2cFrame2.setI2cFrame2AddrTitle(i2cFrame2Addr.getHead()); // 设置地址字段标题
        msgI2cFrame2.setI2cFrame2Data1Title(i2cFrame2Data1.getHead()); // 设置数据1字段标题
        msgI2cFrame2.setI2cFrame2Data2Title(i2cFrame2Data2.getHead()); // 设置数据2字段标题
    }

    /**
     * 获取布局资源ID
     * @return I2C帧2布局资源ID
     */
    @Override
    protected int getLayoutResId() { // 重写基类方法，返回布局资源ID
        return R.layout.layout_triggerserials_i2cframe2; // 返回I2C帧2布局
    }

    /**
     * 获取串口触发详情数据
     * @param detailFlag 详情标志位
     * @return I2C帧2触发详情Bean
     */
    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) { // 重写基类方法，获取串口触发详情
        String addr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_ADDR + getSerialsNumber()); // 从缓存读取地址值
        String data1 = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA1 + getSerialsNumber()); // 从缓存读取数据1值
        String data2 = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA2 + getSerialsNumber()); // 从缓存读取数据2值
        msgI2cFrame2.setI2cFrame2Addr(DIGITS_16, addr); // 设置地址到消息Bean（16进制）
        msgI2cFrame2.setI2cFrame2Data1(DIGITS_16, data1); // 设置数据1到消息Bean（16进制）
        msgI2cFrame2.setI2cFrame2Data2(DIGITS_16, data2); // 设置数据2到消息Bean（16进制）
        return msgI2cFrame2; // 返回消息Bean
    }

    /**
     * 响应右侧菜单消息，条件匹配时发送触发消息
     * @param rightMsgSerials 右侧串口菜单消息
     */
    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) { // 重写基类方法，处理右侧菜单消息
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1 // 判断是否为串口1且当前序号匹配
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_I2C // 判断串口类型是否为I2C
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + getSerialsNumber()) == 6) { // 判断I2C子条件是否为帧2(索引6)
            //当且仅当当前i2c列表选中的是该项时，才向外发送消息
            sendMsg(msgI2cFrame2, rightMsgSerials.isFromEventBus()); // 发送I2C帧2触发消息
        }
    }

    /**
     * 从缓存恢复状态并同步到FPGA和总线
     */
    @Override
    protected void setCache() { // 重写基类方法，从缓存恢复数据
        String addr = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_ADDR + getSerialsNumber()); // 从缓存读取地址
        String data1 = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA1 + getSerialsNumber()); // 从缓存读取数据1
        String data2 = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA2 + getSerialsNumber()); // 从缓存读取数据2
        i2cFrame2Addr.setText(addr); // 设置地址编辑框文本
        i2cFrame2Data1.setText(data1); // 设置数据1编辑框文本
        i2cFrame2Data2.setText(data2); // 设置数据2编辑框文本

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber); // 将串口序号转换为FPGA通道号
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
        if(serialChannel == null) return; // 通道为空则直接返回
        I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C); // 获取I2C总线实例
        i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_FRAME2, toD(addr, IDigits.DIGITS_16)); // 设置I2C触发地址（帧2模式）
        i2cBus.setTriggerData1(toD(data1, IDigits.DIGITS_16)); // 设置I2C触发数据1
        i2cBus.setTriggerData2(toD(data2, IDigits.DIGITS_16)); // 设置I2C触发数据2

        msgI2cFrame2.setI2cFrame2Addr(DIGITS_16, addr); // 同步地址到消息Bean
        msgI2cFrame2.setI2cFrame2Data1(DIGITS_16, data1); // 同步数据1到消息Bean
        msgI2cFrame2.setI2cFrame2Data2(DIGITS_16, data2); // 同步数据2到消息Bean
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发详情索引
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1) // 判断是否为S1触发详情且当前为S1
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2) // 判断是否为S2触发详情且当前为S2
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3) // 判断是否为S3触发详情且当前为S3
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4)) // 判断是否为S4触发详情且当前为S4
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_I2C // 判断右侧菜单是否选中I2C
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + getSerialsNumber()) == 6) { // 判断I2C子条件是否为帧2
            sendMsg(msgI2cFrame2, false); // 发送触发消息（非EventBus来源）
        }
    }

    /**
     * 从外部命令设置触发数据（如EventBus远程同步）
     * @param addr 地址值
     * @param data1 数据1值
     * @param data2 数据2值
     * @param isFromEventBus 是否来自EventBus
     */
    public void setCommandData(int addr, int data1, int data2, boolean isFromEventBus) { // 外部命令设置触发数据
        String sAddr = SerialsUtils.getHexBinFromLong(addr, 2, DIGITS_16); // 将地址转为2位16进制字符串
        String sData1 = SerialsUtils.getHexBinFromLong(data1, 2, DIGITS_16); // 将数据1转为2位16进制字符串
        String sData2 = SerialsUtils.getHexBinFromLong(data2, 2, DIGITS_16); // 将数据2转为2位16进制字符串
        if (!i2cFrame2Addr.getText().equals(sAddr)) { // 地址值有变化时
            onTextListener(i2cFrame2Addr, sAddr, isFromEventBus); // 触发地址变更监听
        }
        if (!i2cFrame2Data1.getText().equals(sData1)) { // 数据1值有变化时
            onTextListener(i2cFrame2Data1, sData1, isFromEventBus); // 触发数据1变更监听
        }
        if (!i2cFrame2Data2.getText().equals(sData2)) { // 数据2值有变化时
            onTextListener(i2cFrame2Data2, sData2, isFromEventBus); // 触发数据2变更监听
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
            case R.id.i2cFrame2Addr: // 地址编辑框
                //该为16进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_16, onAddrListener); // 设置键盘为16进制2位，绑定地址监听
                break;
            case R.id.i2cFrame2Data1: // 数据1编辑框
                //该为16进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_16, onData1Listener); // 设置键盘为16进制2位，绑定数据1监听
                break;
            case R.id.i2cFrame2Data2: // 数据2编辑框
                //该为16进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_16, onData2Listener); // 设置键盘为16进制2位，绑定数据2监听
                break;
        }
    }

    /** 地址键盘关闭监听，将结果传递给文本变更监听 */
    private TopDialogNumberKeyBoard.OnDismissListener onAddrListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 地址键盘关闭回调
        @Override
        public void onDismiss(String result) { // 键盘关闭时回调
            onTextListener(i2cFrame2Addr, result, false); // 触发地址文本变更（非EventBus来源）
        }
    };

    /** 数据1键盘关闭监听，将结果传递给文本变更监听 */
    private TopDialogNumberKeyBoard.OnDismissListener onData1Listener = new TopDialogNumberKeyBoard.OnDismissListener() { // 数据1键盘关闭回调
        @Override
        public void onDismiss(String result) { // 键盘关闭时回调
            onTextListener(i2cFrame2Data1, result, false); // 触发数据1文本变更（非EventBus来源）
        }
    };

    /** 数据2键盘关闭监听，将结果传递给文本变更监听 */
    private TopDialogNumberKeyBoard.OnDismissListener onData2Listener = new TopDialogNumberKeyBoard.OnDismissListener() { // 数据2键盘关闭回调
        @Override
        public void onDismiss(String result) { // 键盘关闭时回调
            onTextListener(i2cFrame2Data2, result, false); // 触发数据2文本变更（非EventBus来源）
        }
    };

    /**
     * 统一处理文本变更，更新缓存/Bean/FPGA指令/总线
     * @param view 变更的编辑框视图
     * @param result 新文本值
     * @param isFromEventBus 是否来自EventBus
     */
    private void onTextListener(TopViewEdit view, String result, boolean isFromEventBus) { // 文本变更统一处理方法
        int cmdCh=TChan.toFpgaChNo(serialsNumber); // 获取FPGA命令通道号
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 获取FPGA物理通道号
        if (view.getId() == i2cFrame2Addr.getId()) { // 判断是否为地址编辑框
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_ADDR + getSerialsNumber(), result); // 缓存地址值
            i2cFrame2Addr.setEdit(result); // 更新地址编辑框显示
            msgI2cFrame2.setI2cFrame2Addr(DIGITS_16, result); // 更新消息Bean地址
            sendMsg(msgI2cFrame2, isFromEventBus); // 发送触发消息
            Command.get().getTrigger_iic().setType(cmdCh, 6, toD(i2cFrame2Addr.getText(), IDigits.DIGITS_16), toD(i2cFrame2Data1.getText(), IDigits.DIGITS_16), toD(i2cFrame2Data2.getText(), IDigits.DIGITS_16), 0, false); // 下发I2C触发指令到FPGA（类型6=帧2）

            if (!isFromEventBus) { // 非EventBus来源时同步到软件解码总线
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道
                if(serialChannel == null) return; // 通道为空则返回
                I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C); // 获取I2C总线
                i2cBus.setTriggerAddrs(I2CBus.I2C_TRIGGER_FRAME2, toD(result, IDigits.DIGITS_16)); // 设置I2C触发地址（帧2模式）
            }
        } else if (view.getId() == i2cFrame2Data1.getId()) { // 判断是否为数据1编辑框
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA1 + getSerialsNumber(), result); // 缓存数据1值
            i2cFrame2Data1.setEdit(result); // 更新数据1编辑框显示
            msgI2cFrame2.setI2cFrame2Data1(DIGITS_16, result); // 更新消息Bean数据1
            sendMsg(msgI2cFrame2, isFromEventBus); // 发送触发消息
            Command.get().getTrigger_iic().setType(cmdCh, 6, toD(i2cFrame2Addr.getText(), IDigits.DIGITS_16), toD(i2cFrame2Data1.getText(), IDigits.DIGITS_16), toD(i2cFrame2Data2.getText(), IDigits.DIGITS_16), 0, false); // 下发I2C触发指令到FPGA（类型6=帧2）

            if (!isFromEventBus) { // 非EventBus来源时同步到软件解码总线
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道
                if(serialChannel == null) return; // 通道为空则返回
                I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C); // 获取I2C总线
                i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_FRAME2, toD(result, IDigits.DIGITS_16)); // 设置I2C触发数据1（帧2模式）
            }
        } else if (view.getId() == i2cFrame2Data2.getId()) { // 判断是否为数据2编辑框
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_FRAME2_DATA2 + getSerialsNumber(), result); // 缓存数据2值
            i2cFrame2Data2.setEdit(result); // 更新数据2编辑框显示
            msgI2cFrame2.setI2cFrame2Data2(DIGITS_16, result); // 更新消息Bean数据2
            sendMsg(msgI2cFrame2, isFromEventBus); // 发送触发消息
            Command.get().getTrigger_iic().setType(cmdCh, 6, toD(i2cFrame2Addr.getText(), IDigits.DIGITS_16), toD(i2cFrame2Data1.getText(), IDigits.DIGITS_16), toD(i2cFrame2Data2.getText(), IDigits.DIGITS_16), 0, false); // 下发I2C触发指令到FPGA（类型6=帧2）

            if (!isFromEventBus) { // 非EventBus来源时同步到软件解码总线
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道
                if(serialChannel == null) return; // 通道为空则返回
                I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C); // 获取I2C总线
                i2cBus.setTriggerData2(toD(result, IDigits.DIGITS_16)); // 设置I2C触发数据2
            }
        }
    }
}
