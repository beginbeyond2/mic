package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment; // 串口触发详情Fragment包声明

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位：SPI串口触发Data详情Fragment                                           ║
 * ║  核心职责：配置SPI协议Data触发条件（数据编辑，支持0/1/X掩码）                        ║
 * ║  架构设计：继承BaseDetail，实现SPI Data的具体触发逻辑                              ║
 * ║  数据流向：右侧菜单 → setConsumer → 更新位宽/数据 → sendMsg → FPGA命令             ║
 * ║  依赖关系：BaseDetail / SpiBus / Command / CacheUtil / SerialsDetailSpiData     ║
 * ║  使用场景：SPI触发模式下Data子项的触发条件配置界面                                   ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

import android.view.View; // 导入View基类，所有UI组件的父类

import com.micsig.tbook.scope.Bus.IBus; // 导入总线接口，用于获取具体总线类型
import com.micsig.tbook.scope.Bus.SpiBus; // 导入SPI总线类，操作SPI触发参数
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类，获取串口通道实例
import com.micsig.tbook.scope.channel.SerialChannel; // 导入串口通道类，管理串口总线
import com.micsig.tbook.tbookscope.R; // 导入R资源类，包含所有资源ID引用
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件，发送FPGA指令
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials; // 导入右侧串口布局常量类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials; // 导入右侧菜单串口消息类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsSpi; // 导入右侧菜单SPI消息详情类
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入PlaySound工具类，播放按键音效
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger; // 导入触发器顶层布局类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils; // 导入串口工具类，提供进制转换方法
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail; // 导入串口详情数据接口
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailSpiData; // 导入SPI Data详情数据Bean
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 导入数字键盘进制常量接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil; // 导入数字键盘工具类，提供位宽转换方法
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类，读写键值对配置
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入自定义编辑框控件
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean类
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组控件
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类，判空等
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道工具类，通道编号转换

/**
 * Created by yangj on 2017/6/9.
 *
 * SPI串口触发Data详情Fragment。
 * 负责配置SPI协议中Data字段的触发条件（支持0/1/X掩码位的数据编辑），
 * 并将配置同步到FPGA命令和SpiBus总线。
 */

public class TopLayoutTriggerSerialsSpiData extends TopLayoutTriggerSerialsBaseDetail { // SPI Data触发详情Fragment，继承基类
    private SerialsDetailSpiData msgSpiData; // SPI Data详情数据Bean
    private TopViewEdit spiDataData; // SPI Data数据编辑框

    /**
     * 初始化界面控件，绑定视图和监听器。
     * @param view 根视图
     */
    @Override // 重写基类的initView抽象方法
    protected void initView(View view) { // 初始化界面控件
        spiDataData = (TopViewEdit) view.findViewById(R.id.spiDataData); // 查找SPI Data数据编辑框控件
        spiDataData.setOnClickEditListener(onClickEditListener); // 设置数据编辑框点击监听器
        msgSpiData = new SerialsDetailSpiData(); // 创建SPI Data详情数据Bean实例
        msgSpiData.setSpiDataData(DIGITS_2X, spiDataData.getText()); // 设置初始数据值（2进制带X掩码）
        msgSpiData.setSpiDataDataTitle(spiDataData.getHead()); // 设置数据编辑标题
    }

    /**
     * 获取布局资源ID。
     * @return SPI Data布局资源ID
     */
    @Override // 重写基类的getLayoutResId抽象方法
    protected int getLayoutResId() { // 返回布局资源ID
        return R.layout.layout_triggerserials_spidata; // 返回SPI Data布局
    }

    /**
     * 获取串口详情数据，从缓存中读取最新的数据值。
     * @param detailFlag 详情标志位
     * @return SPI Data详情数据对象
     */
    @Override // 重写基类的getSerialsDetail抽象方法
    public ISerialsDetail getSerialsDetail(int detailFlag) { // 获取串口详情数据
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI_DATA + getSerialsNumber()); // 从缓存读取数据值
        msgSpiData.setSpiDataData(DIGITS_2X, data); // 更新Bean中的数据值
        return msgSpiData; // 返回详情数据Bean
    }

    /**
     * 处理右侧菜单串口消息，更新位宽和数据。
     * @param rightMsgSerials 右侧菜单串口消息
     */
    @Override // 重写基类的setConsumer抽象方法
    protected void setConsumer(RightMsgSerials rightMsgSerials) { // 处理右侧菜单消息
        int preDigits = 0; // 前次进制（未使用）
        if (serialsNumber == rightMsgSerials.getSerialsNumber() // 判断串口编号是否匹配
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_SPI) { // 判断是否为SPI类型
            RightMsgSerialsSpi detailsSpi = (RightMsgSerialsSpi) rightMsgSerials.getSerialsDetails(); // 获取SPI消息详情
            bits = detailsSpi.getIntBit(); // 更新数据位宽
            if (spiDataData != null) { // 编辑框不为空时处理
                String s = spiDataData.getText().replace(" ", ""); // 去除空格获取当前数据
                String d = KeyBoardNumberUtil.toBits(s, "X", bits); // 根据位宽补齐X掩码位
                String result = KeyBoardNumberUtil.reCalculateSpace(d, DIGITS_2); // 重新计算空格格式
                spiDataData.setText(result); // 更新编辑框文本
                int cmdCh= TChan.toFpgaChNo(serialsNumber); // 获取FPGA命令通道号
                Command.get().getTrigger_spi().Data(cmdCh, result,false); // 发送FPGA SPI数据命令

                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI_DATA + getSerialsNumber(), result); // 保存数据到缓存
                msgSpiData.setSpiDataData(DIGITS_2X, spiDataData.getText()); // 更新Bean中的数据值
                if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI + getSerialsNumber()) == 1) { // 判断SPI子项是否选中Data
                    //当且仅当当前spi列表选中的是该项时，才向外发送消息
                    sendMsg(msgSpiData, rightMsgSerials.isFromEventBus()); // 发送消息通知外部
                }
            }
        }
    }

    /**
     * 加载缓存数据，恢复界面状态并同步到FPGA和总线。
     */
    @Override // 重写基类的setCache抽象方法
    protected void setCache() { // 加载缓存数据
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI_DATA + getSerialsNumber()); // 从缓存读取数据值
        spiDataData.setText(data); // 设置编辑框文本

        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 获取串口编号对应的FPGA通道号
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
        if (serialChannel == null) return; // 通道为空则返回
        SpiBus spiBus = (SpiBus) serialChannel.getBus(IBus.SPI); // 获取SPI总线实例
        spiBus.setTriggerMask((int) toDLong(getMask(data), IDigits.DIGITS_2)); // 设置SPI触发掩码
        spiBus.setTriggerData((int) toDLong(getData(data), IDigits.DIGITS_2)); // 设置SPI触发数据

        int type=Command.get().getTrigger_spi().TypeQ(getSerialsNumber()-1); // 查询当前SPI触发类型
        if (type==1){ // 如果类型为Data触发
            Command.get().getTrigger_spi().Data(getSerialsNumber()-1,data,false); // 发送FPGA SPI数据命令
        }

        msgSpiData.setSpiDataData(DIGITS_2X, data); // 更新Bean数据值
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发器索引
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1) // 判断S1是否匹配
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2) // 判断S2是否匹配
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3) // 判断S3是否匹配
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4)) // 判断S4是否匹配
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_SPI // 判断串口类型是否为SPI
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI + getSerialsNumber()) == 1) { // 判断SPI子项是否为Data
            sendMsg(msgSpiData, false); // 发送消息通知外部
        }
    }

    /**
     * 从数据字符串中提取掩码部分（X位为0，其余为1）。
     * @param data 带掩码的数据字符串
     * @return 掩码字符串
     */
    private String getMask(String data) { // 获取掩码
        return SerialsUtils.getSpiMask(data); // 委托给SerialsUtils提取掩码
    }

    /**
     * 从数据字符串中提取数据部分（X位替换为0）。
     * @param data 带掩码的数据字符串
     * @return 数据字符串
     */
    private String getData(String data) { // 获取数据
        return SerialsUtils.getSpiData(data); // 委托给SerialsUtils提取数据
    }

    /**
     * 从命令数据设置数据值，用于外部同步。
     * @param data 数据字符串
     * @param isFromEventBus 是否来自EventBus事件
     */
    public void setCommandData(String data, boolean isFromEventBus) { // 设置命令数据
        String s = data.replace("0", "").replace("1", "") // 去除0和1
                .replace("X", "").replace("x", ""); // 去除X和x
        if (!StrUtil.isEmpty(s)) return; // 如果还有其他字符则数据无效，直接返回
        data = KeyBoardNumberUtil.reCalculateSpace(KeyBoardNumberUtil.toBits( // 根据位宽补齐X并重新计算空格
                data.replace(" ", ""), "X", bits), DIGITS_2X).trim(); // 去空格、补位、重算空格、去首尾空格
        if (!spiDataData.getText().equals(data)) { // 如果编辑框文本与新数据不一致
            onDataListener(data, isFromEventBus); // 触发数据变更处理
        }
    }

    /**
     * 处理单选组条件变更事件（SPI Data无条件选择，空实现）。
     * @param view 单选组控件
     * @param item 选中的通道项
     */
    @Override // 重写基类的setOnCheckChangedListener抽象方法
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) { // 条件变更处理（SPI Data无条件选择）

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
            case R.id.spiDataData: // SPI Data编辑框
                //只能输入0、1、X，右侧决定位数，可能需要直接操作edit而不是操作键盘上的text
                dialogKeyBoard.setDecimalData(msgSpiData.getSpiDataData().getValue(), bits, DIGITS_2X, onDataListener); // 设置键盘参数：当前值、位宽、2进制带X
                break; // 跳出switch
        }
    }

    /**
     * 数字键盘关闭监听器，将键盘输入结果传递给数据处理。
     */
    private TopDialogNumberKeyBoard.OnDismissListener onDataListener = new TopDialogNumberKeyBoard.OnDismissListener() { // 键盘关闭监听器
        @Override // 重写onDismiss方法
        public void onDismiss(String result) { // 键盘关闭回调
            onDataListener(result, false); // 将结果传递给数据处理方法
        }
    };

    /**
     * 数据变更处理，更新缓存、编辑框、Bean、FPGA命令和总线。
     * @param text 新的数据值
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onDataListener(String text, boolean isFromEventBus) { // 数据变更处理
        int cmdCh=TChan.toFpgaChNo(serialsNumber); // 获取FPGA命令通道号
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 获取FPGA总线通道号

        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI_DATA + getSerialsNumber(), text); // 保存数据到缓存
        spiDataData.setEdit(text); // 更新编辑框显示
        msgSpiData.setSpiDataData(DIGITS_2X, text); // 更新Bean数据值
        sendMsg(msgSpiData, isFromEventBus); // 发送消息通知外部
        long mask = toDLong(getMask(text), IDigits.DIGITS_2); // 计算掩码的10进制值
        long data = toDLong(getData(text), IDigits.DIGITS_2); // 计算数据的10进制值
        Command.get().getBus_spi().setType(cmdCh, 1, (int) mask, (int) data, false); // 发送SPI总线类型命令
        Command.get().getTrigger_spi().Type(cmdCh, 1,false); // 设置SPI触发类型为Data
        Command.get().getTrigger_spi().Data(cmdCh, text,false); // 发送FPGA SPI数据命令

        if (!isFromEventBus) { // 非EventBus事件时同步总线
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
            if (serialChannel == null) return; // 通道为空则返回
            SpiBus spiBus = (SpiBus) serialChannel.getBus(IBus.SPI); // 获取SPI总线实例
            spiBus.setTriggerMask((int) toDLong(getMask(text), IDigits.DIGITS_2)); // 设置SPI触发掩码
            spiBus.setTriggerData((int) toDLong(getData(text), IDigits.DIGITS_2)); // 设置SPI触发数据
        }
    }
}
