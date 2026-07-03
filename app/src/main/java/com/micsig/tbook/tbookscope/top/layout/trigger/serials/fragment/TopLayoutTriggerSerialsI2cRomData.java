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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailArinc429Label; // 导入ARINC429标签详情Bean（未使用但保留import）
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailI2cRomData; // 导入I2C ROM数据触发详情Bean
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 导入进制常量接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入顶部可编辑视图组件
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道Bean，用于单选按钮组数据
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选按钮组视图
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道转换工具类

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: 顶部触发布局 → 串口触发 → I2C子条件 → ROM数据触发Fragment          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                 ║
 * ║    1. 提供I2C ROM数据(EEPROM读数据)触发的UI交互界面（条件+数据输入）           ║
 * ║    2. 管理条件和数据的缓存读写与FPGA指令下发                                  ║
 * ║    3. 将用户输入同步到I2CBus总线和Command命令通道                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  架构设计:                                                                 ║
 * ║    继承TopLayoutTriggerSerialsBaseDetail基类，实现模板方法模式                ║
 * ║    - initView(): 初始化条件单选组/数据编辑框和消息Bean                        ║
 * ║    - setCache(): 从缓存恢复状态并同步到FPGA                                  ║
 * ║    - setConsumer(): 响应右侧菜单消息，条件匹配时发送触发消息                  ║
 * ║    - onTextListener(): 处理数据文本变更                                      ║
 * ║    - onCheckListener(): 处理条件单选变更                                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向:                                                                 ║
 * ║    UI编辑框/单选组 → CacheUtil缓存 → SerialsDetailI2cRomData Bean          ║
 * ║                     → Command指令(FPGA) → I2CBus总线(软件解码)              ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系:                                                                 ║
 * ║    - TopLayoutTriggerSerialsBaseDetail (继承)                               ║
 * ║    - I2CBus / SerialChannel / ChannelFactory (总线与通道)                   ║
 * ║    - Command (FPGA指令下发)                                                 ║
 * ║    - CacheUtil (持久化缓存)                                                 ║
 * ║    - TopDialogNumberKeyBoard (数字键盘弹窗)                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  使用场景:                                                                 ║
 * ║    当用户在I2C触发模式下选择"ROM数据"子条件时，加载此Fragment                  ║
 * ║    对应I2C子条件索引值为7                                                   ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

public class TopLayoutTriggerSerialsI2cRomData extends TopLayoutTriggerSerialsBaseDetail { // I2C ROM数据触发Fragment，继承串口触发详情基类
    private TopViewRadioGroup i2cRomDataCondition; // ROM数据条件单选按钮组（如大于/小于/等于）
    private TopViewEdit i2cRomDataData; // ROM数据编辑框
    private SerialsDetailI2cRomData msgI2cRomData; // I2C ROM数据触发详情消息Bean

    /**
     * 初始化视图控件及消息Bean
     * @param view Fragment根视图
     */
    @Override
    protected void initView(View view) { // 重写基类方法，初始化视图控件
        i2cRomDataCondition = (TopViewRadioGroup) view.findViewById(R.id.i2cRomDataCondition); // 查找条件单选按钮组控件
        i2cRomDataData = (TopViewEdit) view.findViewById(R.id.i2cRomDataData); // 查找数据编辑框控件
        i2cRomDataCondition.setOnListener(onCheckChangedListener); // 为条件单选组设置变更监听
        i2cRomDataData.setOnClickEditListener(onClickEditListener); // 为数据编辑框设置点击监听
        msgI2cRomData = new SerialsDetailI2cRomData(); // 创建I2C ROM数据消息Bean实例
        msgI2cRomData.setI2cRomDataCondition(i2cRomDataCondition.getSelected()); // 设置条件选中项
        msgI2cRomData.setI2cRomDataData(DIGITS_16, i2cRomDataData.getText()); // 设置数据值（16进制）
        msgI2cRomData.setI2cRomDataConditionTitle(i2cRomDataCondition.getHead()); // 设置条件字段标题
        msgI2cRomData.setI2cRomDataDataTitle(i2cRomDataData.getHead()); // 设置数据字段标题
    }

    /**
     * 获取布局资源ID
     * @return I2C ROM数据布局资源ID
     */
    @Override
    protected int getLayoutResId() { // 重写基类方法，返回布局资源ID
        return R.layout.layout_triggerserials_i2cromdata; // 返回I2C ROM数据布局
    }

    /**
     * 获取串口触发详情数据
     * @param detailFlag 详情标志位
     * @return I2C ROM数据触发详情Bean
     */
    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) { // 重写基类方法，获取串口触发详情
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_CONDITION + getSerialsNumber()); // 从缓存读取条件索引
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_DATA + getSerialsNumber()); // 从缓存读取数据值
        i2cRomDataCondition.setSelectedIndex(condition); // 设置条件单选组选中项
        msgI2cRomData.setI2cRomDataCondition(i2cRomDataCondition.getSelected()); // 设置条件到消息Bean
        msgI2cRomData.setI2cRomDataData(DIGITS_16, data); // 设置数据到消息Bean（16进制）
        return msgI2cRomData; // 返回消息Bean
    }

    /**
     * 响应右侧菜单消息，条件匹配时发送触发消息
     * @param rightMsgSerials 右侧串口菜单消息
     */
    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) { // 重写基类方法，处理右侧菜单消息
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1 // 判断是否为串口1且当前序号匹配
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_I2C // 判断串口类型是否为I2C
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + getSerialsNumber()) == 7) { // 判断I2C子条件是否为ROM数据(索引7)
            //当且仅当当前i2c列表选中的是该项时，才向外发送消息
            sendMsg(msgI2cRomData, rightMsgSerials.isFromEventBus()); // 发送I2C ROM数据触发消息
        }
    }

    /**
     * 从缓存恢复状态并同步到FPGA和总线
     */
    @Override
    protected void setCache() { // 重写基类方法，从缓存恢复数据
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_CONDITION + getSerialsNumber()); // 从缓存读取条件索引
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_DATA + getSerialsNumber()); // 从缓存读取数据值
        i2cRomDataCondition.setSelectedIndex(condition); // 设置条件单选组选中项
        i2cRomDataData.setText(data); // 设置数据编辑框文本

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber); // 将串口序号转换为FPGA通道号
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
        if(serialChannel == null) return; // 通道为空则直接返回
        I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C); // 获取I2C总线实例
        i2cBus.setTriggerRelation(getConditionValue(condition)); // 设置I2C触发关系条件（大于/小于/等于）
        i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_EEPROM_READ_DATA, toD(data, IDigits.DIGITS_16)); // 设置I2C触发数据1（EEPROM读数据模式）

        msgI2cRomData.setI2cRomDataCondition(i2cRomDataCondition.getSelected()); // 同步条件到消息Bean
        msgI2cRomData.setI2cRomDataData(DIGITS_16, data); // 同步数据到消息Bean
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发详情索引
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1) // 判断是否为S1触发详情且当前为S1
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2) // 判断是否为S2触发详情且当前为S2
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3) // 判断是否为S3触发详情且当前为S3
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4)) // 判断是否为S4触发详情且当前为S4
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_I2C // 判断右侧菜单是否选中I2C
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + getSerialsNumber()) == 7) { // 判断I2C子条件是否为ROM数据
            sendMsg(msgI2cRomData, false); // 发送触发消息（非EventBus来源）
        }
    }

    /**
     * 从外部命令设置触发数据（如EventBus远程同步）
     * @param data1 数据值
     * @param condition 条件索引
     * @param isFromEventBus 是否来自EventBus
     */
    public void setCommandData(int data1, int condition, boolean isFromEventBus) { // 外部命令设置触发数据
        String sData1 = SerialsUtils.getHexBinFromLong(data1, 2, DIGITS_16); // 将数据转为2位16进制字符串
        if (!i2cRomDataData.getText().equals(sData1)) { // 数据值有变化时
            onTextListener(sData1, isFromEventBus); // 触发数据文本变更监听
        }
        if (i2cRomDataCondition.getSelected().getIndex() != condition) { // 条件索引有变化时
            i2cRomDataCondition.setSelectedIndex(condition); // 设置条件选中项
            onCheckListener(i2cRomDataCondition, i2cRomDataCondition.getSelected(), isFromEventBus); // 触发条件变更监听
        }
    }

    /**
     * 单选按钮组变更监听，委托给onCheckListener处理
     * @param view 单选按钮组视图
     * @param item 选中的通道Bean
     */
    @Override
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) { // 重写基类方法，单选按钮变更监听
        onCheckListener(view, item, false); // 委托给onCheckListener处理（非EventBus来源）
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
            case R.id.i2cRomDataData: // 数据编辑框
                //该为16进制,2位限制
                dialogKeyBoard.setDecimalData(2, DIGITS_16, onDataListener); // 设置键盘为16进制2位，绑定数据监听
                break;
        }
    }

    /** 数据键盘关闭监听，将结果传递给文本变更监听 */
    private TopDialogNumberKeyBoard.OnDismissListener onDataListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 数据键盘关闭回调
        @Override
        public void onDismiss(String result) { // 键盘关闭时回调
            onTextListener(result, false); // 触发数据文本变更（非EventBus来源）
        }
    };

    /**
     * 处理数据文本变更，更新缓存/Bean/FPGA指令/总线
     * @param result 新文本值
     * @param isFromEventBus 是否来自EventBus
     */
    private void onTextListener(String result, boolean isFromEventBus) { // 数据文本变更处理方法
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_DATA + getSerialsNumber(), result); // 缓存数据值
        i2cRomDataData.setEdit(result); // 更新数据编辑框显示
        msgI2cRomData.setI2cRomDataData(DIGITS_16, result); // 更新消息Bean数据
        sendMsg(msgI2cRomData, isFromEventBus); // 发送触发消息
        int cmdCh=TChan.toFpgaChNo(serialsNumber); // 获取FPGA命令通道号
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 获取FPGA物理通道号
        Command.get().getTrigger_iic().setType(cmdCh, 7, 0, toD(result, IDigits.DIGITS_16), 0, i2cRomDataCondition.getSelected().getIndex(), false); // 下发I2C触发指令到FPGA（类型7=ROM数据）

        if (!isFromEventBus) { // 非EventBus来源时同步到软件解码总线
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道
            if(serialChannel == null) return; // 通道为空则返回
            I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C); // 获取I2C总线
            i2cBus.setTriggerData1(I2CBus.I2C_TRIGGER_EEPROM_READ_DATA, toD(result, IDigits.DIGITS_16)); // 设置I2C触发数据1（EEPROM读数据模式）
        }
    }

    /**
     * 处理条件单选变更，更新缓存/Bean/FPGA指令/总线
     * @param view 单选按钮组视图
     * @param item 选中的通道Bean
     * @param isFromEventBus 是否来自EventBus
     */
    private void onCheckListener(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus) { // 条件单选变更处理方法
        if (view.getId() == R.id.i2cRomDataCondition) { // 判断是否为条件单选组
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C_ROMDATA_CONDITION + getSerialsNumber(), String.valueOf(item.getIndex())); // 缓存条件索引
            msgI2cRomData.setI2cRomDataCondition(item); // 更新消息Bean条件
            sendMsg(msgI2cRomData, isFromEventBus); // 发送触发消息
            int cmdCh=TChan.toFpgaChNo(serialsNumber); // 获取FPGA命令通道号
            int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 获取FPGA物理通道号
            Command.get().getTrigger_iic().setType(cmdCh, 7, 0, Integer.parseInt(i2cRomDataData.getText(),16), 0, i2cRomDataCondition.getSelected().getIndex(), false); // 下发I2C触发指令到FPGA（类型7=ROM数据，数据按16进制解析）

            if (!isFromEventBus) { // 非EventBus来源时同步到软件解码总线
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道
                if(serialChannel == null) return; // 通道为空则返回
                I2CBus i2cBus = (I2CBus) serialChannel.getBus(IBus.I2C); // 获取I2C总线
                i2cBus.setTriggerRelation(getConditionValue(item.getIndex())); // 设置I2C触发关系条件
            }
        }
    }
}
