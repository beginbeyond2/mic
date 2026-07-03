package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment; // 串口触发详情Fragment包声明

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位：CAN串口触发IdData详情Fragment                                         ║
 * ║  核心职责：配置CAN协议ID+DLC+Data组合触发条件                                      ║
 * ║  架构设计：继承BaseDetail，实现CAN IdData的具体触发逻辑                             ║
 * ║  数据流向：右侧菜单 → setConsumer → 更新ID/DLC/Data → sendMsg → FPGA命令          ║
 * ║  依赖关系：BaseDetail / CanBus / Command / CacheUtil / SerialsDetailCanIdData    ║
 * ║  使用场景：CAN触发模式下ID+DLC+Data组合子项的触发条件配置界面                         ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

import android.view.View; // 导入View基类，所有UI组件的父类

import com.micsig.base.Logger; // 导入日志工具类，调试输出
import com.micsig.tbook.scope.Bus.CanBus; // 导入CAN总线类，操作CAN触发参数
import com.micsig.tbook.scope.Bus.IBus; // 导入总线接口，用于获取具体总线类型
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类，获取串口通道实例
import com.micsig.tbook.scope.channel.SerialChannel; // 导入串口通道类，管理串口总线
import com.micsig.tbook.tbookscope.R; // 导入R资源类，包含所有资源ID引用
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件，发送FPGA指令
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials; // 导入右侧串口布局常量类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials; // 导入右侧菜单串口消息类
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入PlaySound工具类，播放按键音效
import com.micsig.tbook.tbookscope.tools.Tools; // 导入Tools工具类，提供通用工具方法
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger; // 导入触发器顶层布局类
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.SerialsUtils; // 导入串口工具类，提供进制转换方法
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.ISerialsDetail; // 导入串口详情数据接口
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailCanIdData; // 导入CAN IdData详情数据Bean
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 导入数字键盘进制常量接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil; // 导入数字键盘工具类
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类，读写键值对配置
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入自定义编辑框控件
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean类
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组控件
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类，判空等
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道工具类，通道编号转换

import java.util.ArrayList; // 导入ArrayList动态数组类
import java.util.Arrays; // 导入Arrays数组工具类

/**
 * Created by yangj on 2017/6/9.
 *
 * CAN串口触发IdData详情Fragment。
 * 负责配置CAN协议中ID+DLC+Data组合触发条件，
 * 支持ID（16进制8位）、DLC（0-64离散值）、Data（16进制可变位宽）的编辑，
 * 并将配置同步到FPGA命令和CanBus总线。
 */

public class TopLayoutTriggerSerialsCanIdData extends TopLayoutTriggerSerialsBaseDetail { // CAN IdData触发详情Fragment，继承基类
    private SerialsDetailCanIdData msgCanIdData; // CAN IdData详情数据Bean
    private TopViewEdit canIdDataId; // CAN ID编辑框
    private TopViewEdit canIdDataDlc; // CAN DLC编辑框
    private TopViewEdit canIdDataData; // CAN Data编辑框

    /**
     * 初始化界面控件，绑定视图和监听器。
     * @param view 根视图
     */
    @Override // 重写基类的initView抽象方法
    protected void initView(View view) { // 初始化界面控件
        canIdDataId = (TopViewEdit) view.findViewById(R.id.canIdDataId); // 查找CAN ID编辑框控件
        canIdDataDlc = (TopViewEdit) view.findViewById(R.id.canIdDataDlc); // 查找CAN DLC编辑框控件
        canIdDataData = (TopViewEdit) view.findViewById(R.id.canIdDataData); // 查找CAN Data编辑框控件
        canIdDataId.setOnClickEditListener(onClickEditListener); // 设置ID编辑框点击监听器
        canIdDataDlc.setOnClickEditListener(onClickEditListener); // 设置DLC编辑框点击监听器
        canIdDataData.setOnClickEditListener(onClickEditListener); // 设置Data编辑框点击监听器
        msgCanIdData = new SerialsDetailCanIdData(); // 创建CAN IdData详情数据Bean实例
        msgCanIdData.setCanIdDataId(DIGITS_16, canIdDataId.getText()); // 设置初始ID值（16进制）
        msgCanIdData.setCanIdDataDlc(DIGITS_10, canIdDataDlc.getText()); // 设置初始DLC值（10进制）
        msgCanIdData.setCanIdDataData(DIGITS_16, canIdDataData.getText()); // 设置初始Data值（16进制）
        msgCanIdData.setCanIdDataIdTitle(canIdDataId.getHead()); // 设置ID编辑标题
        msgCanIdData.setCanIdDataDlcTitle(canIdDataDlc.getHead()); // 设置DLC编辑标题
        msgCanIdData.setCanIdDataDataTitle(canIdDataData.getHead()); // 设置Data编辑标题
    }

    /**
     * 获取布局资源ID。
     * @return CAN IdData布局资源ID
     */
    @Override // 重写基类的getLayoutResId抽象方法
    protected int getLayoutResId() { // 返回布局资源ID
        return R.layout.layout_triggerserials_caniddata; // 返回CAN IdData布局
    }

    /**
     * 获取串口详情数据，从缓存中读取最新的ID、DLC和Data值。
     * @param detailFlag 详情标志位
     * @return CAN IdData详情数据对象
     */
    @Override // 重写基类的getSerialsDetail抽象方法
    public ISerialsDetail getSerialsDetail(int detailFlag) { // 获取串口详情数据
        String id = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_ID + getSerialsNumber()); // 从缓存读取ID值
        String dlc = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC + getSerialsNumber()); // 从缓存读取DLC值
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA + getSerialsNumber()); // 从缓存读取Data值
        msgCanIdData.setCanIdDataId(DIGITS_16, id); // 更新Bean中的ID值
        msgCanIdData.setCanIdDataDlc(DIGITS_10, dlc); // 更新Bean中的DLC值
        msgCanIdData.setCanIdDataData(DIGITS_16, data); // 更新Bean中的Data值
        return msgCanIdData; // 返回详情数据Bean
    }

    /**
     * 处理右侧菜单串口消息，当CAN类型且选中IdData时发送消息。
     * @param rightMsgSerials 右侧菜单串口消息
     */
    @Override // 重写基类的setConsumer抽象方法
    protected void setConsumer(RightMsgSerials rightMsgSerials) { // 处理右侧菜单消息
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1 // 判断是否为串口1且编号匹配
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_CAN // 判断是否为CAN类型
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + getSerialsNumber()) == 4) { // 判断CAN子项是否选中IdData
            //当且仅当当前can列表选中的是该项时，才向外发送消息
            sendMsg(msgCanIdData, rightMsgSerials.isFromEventBus()); // 发送消息通知外部
        }
    }

    /**
     * 加载缓存数据，恢复界面状态并同步到FPGA和总线。
     */
    @Override // 重写基类的setCache抽象方法
    protected void setCache() { // 加载缓存数据
        String id = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_ID + getSerialsNumber()); // 从缓存读取ID值
        String dlc = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC + getSerialsNumber()); // 从缓存读取DLC值
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA + getSerialsNumber()); // 从缓存读取Data值
        canIdDataId.setText(id); // 设置ID编辑框文本
        canIdDataDlc.setText(dlc); // 设置DLC编辑框文本
        canIdDataData.setText(data); // 设置Data编辑框文本

        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 获取串口编号对应的FPGA通道号
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
        if(serialChannel == null) return; // 通道为空则返回
        CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN); // 获取CAN总线实例
        canBus.setFrameId(CanBus.CAN_TRIGGER_ID_AND_DATA, toD(id, 16)); // 设置CAN帧ID
        canBus.setDlc(SerialsUtils.getCanDlcFromShow(dlc)); // 设置CAN DLC值
        canBus.setData(toDLong(data, 16)); // 设置CAN Data值

        int type = Command.get().getTrigger_can().getType(getSerialsNumber() - 1); // 查询当前CAN触发类型
        if (type == 4) { // 如果类型为IdData触发
            Command.get().getTrigger_can().setType(getSerialsNumber() - 1, type, Tools.HexStringToInt(id), Integer.parseInt(dlc), toDLong(data, 16), false); // 发送FPGA CAN IdData触发命令
        }

        msgCanIdData.setCanIdDataId(DIGITS_16, id); // 更新Bean ID值
        msgCanIdData.setCanIdDataDlc(DIGITS_10, dlc); // 更新Bean DLC值
        msgCanIdData.setCanIdDataData(DIGITS_16, data); // 更新Bean Data值
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发器索引
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1) // 判断S1是否匹配
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2) // 判断S2是否匹配
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3) // 判断S3是否匹配
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4)) // 判断S4是否匹配
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_CAN // 判断串口类型是否为CAN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + getSerialsNumber()) == 4) { // 判断CAN子项是否为IdData
            sendMsg(msgCanIdData, false); // 发送消息通知外部
        }
    }

    /**
     * 从命令数据设置ID、DLC和Data值，用于外部同步。
     * @param id ID整数值
     * @param dlc DLC长度值
     * @param data Data长整数值
     * @param isFromEventBus 是否来自EventBus事件
     */
    public void setCommandData(int id, int dlc, long data, boolean isFromEventBus) { // 设置命令数据
        String sId = SerialsUtils.getHexBinFromLong(id, 8, DIGITS_16); // 将ID转换为8位16进制字符串
        dlc = Math.min(64, dlc); // 限制DLC最大值为64
        dlc = Math.max(0, dlc); // 限制DLC最小值为0
        String sDlc = String.valueOf(dlc); // 将DLC转换为字符串
        int bits = dlc >= 8 ? 8 : dlc; // 计算Data位宽：DLC>=8时为8字节，否则为DLC值
        String sData = SerialsUtils.getHexBinFromLong(data, bits * 2, DIGITS_16); // 将Data转换为指定位宽16进制字符串
        if (!canIdDataId.getText().equals(sId)) { // 如果ID编辑框文本与转换后ID不一致
            onTextListener(canIdDataId, sId, isFromEventBus); // 触发ID文本变更处理
        }
        if (!canIdDataDlc.getText().equals(sDlc)) { // 如果DLC编辑框文本与转换后DLC不一致
            onTextListener(canIdDataDlc, sDlc, isFromEventBus); // 触发DLC文本变更处理
        }
        if (!canIdDataData.getText().equals(sData)) { // 如果Data编辑框文本与转换后Data不一致
            onTextListener(canIdDataData, sData, isFromEventBus); // 触发Data文本变更处理
        }
    }

    /**
     * 处理单选组条件变更事件（CAN IdData无条件选择，空实现）。
     * @param view 单选组控件
     * @param item 选中的通道项
     */
    @Override // 重写基类的setOnCheckChangedListener抽象方法
    protected void setOnCheckChangedListener(TopViewRadioGroup view, TopBeanChannel item) { // 条件变更处理（CAN IdData无条件选择）

    }

    /**
     * 处理编辑框点击事件，根据不同编辑框弹出对应的数字键盘。
     * @param v 被点击的编辑框
     * @param text 当前文本内容
     */
    @Override // 重写基类的setOnClickEditListener抽象方法
    protected void setOnClickEditListener(final TopViewEdit v, String text) { // 编辑框点击处理
        PlaySound.getInstance().playButton(); // 播放按键音效
        switch (v.getId()) { // 根据编辑框ID分支处理
            case R.id.canIdDataId: // CAN ID编辑框
                //该为16进制,8位限制
                dialogKeyBoard.setDecimalData(8, DIGITS_16, onIdListener); // 设置键盘参数：8位16进制
                break; // 跳出switch
            case R.id.canIdDataDlc: // CAN DLC编辑框
//                //只能输入0-8这9个数字
//                dialogKeyBoard.setDecimalData(1, DIGITS_0_8, onDlcListener);
                //需要显示0,1,2,3,4,5,6,7,8,12,16,20,24,32,48,64,这16个数
                dialogKeyBoard.setDecimalData(2, IDigits.DIGITS_16, true, onDlcListener); // 设置键盘参数：2位16进制，启用特殊DLC模式
                break; // 跳出switch
            case R.id.canIdDataData: // CAN Data编辑框
                //该为16进制,12位限制
                int bit = Integer.parseInt(canIdDataDlc.getText()); // 获取DLC值确定Data位宽
                if (bit != 0) { // DLC不为0时才弹出键盘
                    bit = Math.min(bit, 8); // 限制最大8字节
                    dialogKeyBoard.setDecimalData(bit * 2, DIGITS_16, onDataListener); // 设置键盘参数：位宽*2位16进制
                }
                break; // 跳出switch
        }
    }

    /**
     * ID数字键盘关闭监听器，将键盘输入结果传递给文本处理。
     */
    private TopDialogNumberKeyBoard.OnDismissListener onIdListener = new TopDialogNumberKeyBoard.OnDismissListener() { // ID键盘关闭监听器
        @Override // 重写onDismiss方法
        public void onDismiss(String result) { // 键盘关闭回调
            onTextListener(canIdDataId, result, false); // 将结果传递给ID文本变更处理
        }
    };

    /**
     * DLC数字键盘关闭监听器，将键盘输入结果映射到最近的合法DLC值后传递给文本处理。
     */
    private TopDialogNumberKeyBoard.OnDismissListener onDlcListener = new TopDialogNumberKeyBoard.OnDismissListener() { // DLC键盘关闭监听器
        @Override // 重写onDismiss方法
        public void onDismiss(String result) { // 键盘关闭回调
            //需要显示0,1,2,3,4,5,6,7,8,12,16,20,24,32,48,64,这16个数
            Logger.i("onDlcListener Result=" + result); // 打印DLC输入结果日志
            try { // 尝试解析DLC值
                int temp = Integer.parseInt(result); // 将输入解析为整数
                temp = findNearResult(temp); // 查找最近的合法DLC值
                result = String.valueOf(temp); // 转换回字符串
            } catch (Exception e) { // 解析异常处理
                Logger.e("onDlcListener result=" + result + " can not parse int"); // 打印错误日志
                e.printStackTrace(); // 打印异常堆栈
            } finally { // 无论是否异常都执行
                onTextListener(canIdDataDlc, result, false); // 将结果传递给DLC文本变更处理
            }
        }
    };


    /**
     * 查找与输入值最接近的合法DLC值。
     * 合法DLC值为：0,1,2,3,4,5,6,7,8,12,16,20,24,32,48,64。
     * @param temp 输入的DLC值
     * @return 最接近的合法DLC值
     */
    private int findNearResult(int temp) { // 查找最近合法DLC值
        ArrayList<Integer> limitList = new ArrayList<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 12, 16, 20, 24, 32, 48, 64)); // 合法DLC值列表
        if (temp >= limitList.get(limitList.size() - 1)) { // 如果输入值大于等于最大合法值64
            return limitList.get(limitList.size() - 1); // 返回最大值64
        }
        for (int i = 0; i < limitList.size() - 1; i++) { // 遍历合法值列表
            int left = limitList.get(i); // 当前合法值
            int right = limitList.get(i + 1); // 下一个合法值
            if (left <= temp && right > temp) { // 输入值在当前区间内
                if (Math.abs(left - temp) <= Math.abs(temp - right)) { // 距离左值更近或相等
                    temp = left; // 取左值
                } else { // 距离右值更近
                    temp = right; // 取右值
                }
                break; // 找到后跳出循环
            }
        }
        return temp; // 返回最近的合法DLC值
    }

    /**
     * Data数字键盘关闭监听器，将键盘输入结果传递给文本处理。
     */
    private TopDialogNumberKeyBoard.OnDismissListener onDataListener = new TopDialogNumberKeyBoard.OnDismissListener() { // Data键盘关闭监听器
        @Override // 重写onDismiss方法
        public void onDismiss(String result) { // 键盘关闭回调
            onTextListener(canIdDataData, result, false); // 将结果传递给Data文本变更处理
        }
    };

    /**
     * 文本变更处理，根据不同编辑框分别处理ID、DLC、Data的变更。
     * @param view 被修改的编辑框
     * @param text 新的文本值
     * @param isFromEventBus 是否来自EventBus事件
     */
    private void onTextListener(TopViewEdit view, String text, boolean isFromEventBus) { // 文本变更处理
        int cmdCh= TChan.toFpgaChNo(serialsNumber); // 获取FPGA命令通道号
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 获取FPGA总线通道号
        if (view.getId() == canIdDataId.getId()) { // 判断是否为ID编辑框
            int val = (int) toDLong(text, IDigits.DIGITS_16); // 将16进制文本转换为10进制整数
            text = SerialsUtils.getHexBinFromInt(val, 8, IDigits.DIGITS_16); // 重新格式化为8位16进制字符串

            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_ID + getSerialsNumber(), text); // 保存ID到缓存
            canIdDataId.setEdit(text); // 更新ID编辑框显示
            msgCanIdData.setCanIdDataId(DIGITS_16, text); // 更新Bean ID值
            sendMsg(msgCanIdData, isFromEventBus); // 发送消息通知外部
            int id = toD(canIdDataId.getText(), IDigits.DIGITS_16); // 获取ID的10进制值
            int dlc = Integer.parseInt(canIdDataDlc.getText());//toD(canIdDataDlc.getText(), IDigits.DIGITS_16);
            long data = toDLong(canIdDataData.getText(), IDigits.DIGITS_16); // 获取Data的10进制值
            Command.get().getTrigger_can().setType(cmdCh, 4, id, dlc, data, false); // 发送FPGA CAN IdData触发命令

            if (!isFromEventBus) { // 非EventBus事件时同步总线
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
                if(serialChannel == null) return; // 通道为空则返回
                CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN); // 获取CAN总线实例
                canBus.setFrameId(CanBus.CAN_TRIGGER_ID_AND_DATA, toD(text, IDigits.DIGITS_16)); // 设置CAN帧ID
            }
        } else if (view.getId() == canIdDataDlc.getId()) { // 判断是否为DLC编辑框
            if (StrUtil.isEmpty(text)) return;//没选DLC，保持现有的
            int bit = Integer.parseInt(text); // 解析DLC值为整数
            bit = Math.min(bit, 8); // 限制最大8字节
            String sData = KeyBoardNumberUtil.reCalculateSpace( // 根据新DLC重新计算Data格式
                    KeyBoardNumberUtil.toBits( // 补齐位宽
                            canIdDataData.getText().replace(" ", "") // 去空格获取当前Data
                            , bit * 2), KeyBoardNumberUtil.DIGITS_16).trim(); // 重算空格并去首尾空格
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC + getSerialsNumber(), text); // 保存DLC到缓存
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA + getSerialsNumber(), sData); // 保存更新后的Data到缓存
            canIdDataData.setText(sData); // 更新Data编辑框文本
            canIdDataDlc.setEdit(text); // 更新DLC编辑框显示
            msgCanIdData.setCanIdDataData(DIGITS_16, sData); // 更新Bean Data值
            msgCanIdData.setCanIdDataDlc(DIGITS_10, text); // 更新Bean DLC值
            sendMsg(msgCanIdData, isFromEventBus); // 发送消息通知外部
            int id = toD(canIdDataId.getText(), IDigits.DIGITS_16); // 获取ID的10进制值
            int dlc = Integer.parseInt(canIdDataDlc.getText());  //toD(canIdDataDlc.getText(), IDigits.DIGITS_16);
            long lData = toDLong(canIdDataData.getText(), IDigits.DIGITS_16); // 获取Data的10进制值
            Command.get().getTrigger_can().setType(cmdCh, 4, id, dlc, lData, false); // 发送FPGA CAN IdData触发命令

            if (!isFromEventBus) { // 非EventBus事件时同步总线
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
                if(serialChannel == null) return; // 通道为空则返回
                CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN); // 获取CAN总线实例
                canBus.setDlc(SerialsUtils.getCanDlcFromShow(text)); // 设置CAN DLC值
                canBus.setData(toDLong(sData, IDigits.DIGITS_16)); // 设置CAN Data值
            }
        } else if (view.getId() == canIdDataData.getId()) { // 判断是否为Data编辑框
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DATA + getSerialsNumber(), text); // 保存Data到缓存
            canIdDataData.setEdit(text); // 更新Data编辑框显示
            msgCanIdData.setCanIdDataData(DIGITS_16, text); // 更新Bean Data值
            sendMsg(msgCanIdData, isFromEventBus); // 发送消息通知外部
            int id = toD(canIdDataId.getText(), IDigits.DIGITS_16); // 获取ID的10进制值
            int dlc = Integer.parseInt(canIdDataDlc.getText()); //toD(canIdDataDlc.getText(), IDigits.DIGITS_16);
            long data = toDLong(canIdDataData.getText(), IDigits.DIGITS_16); // 获取Data的10进制值
            Command.get().getTrigger_can().setType(cmdCh, 4, id, dlc, data, false); // 发送FPGA CAN IdData触发命令

            if (!isFromEventBus) { // 非EventBus事件时同步总线
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
                if(serialChannel == null) return; // 通道为空则返回
                CanBus canBus = (CanBus) serialChannel.getBus(IBus.CAN); // 获取CAN总线实例
                canBus.setData(toDLong(text, IDigits.DIGITS_16)); // 设置CAN Data值
            }
        }
    }
}
