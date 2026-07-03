package com.micsig.tbook.tbookscope.top.layout.trigger.serials.fragment; // 串口触发子条件Fragment包

import android.view.View; // 导入Android视图基类

import com.micsig.base.Logger; // 导入日志工具类
import com.micsig.tbook.scope.Action.XAction; // 导入XAction操作类
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
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean.SerialsDetailLinIdData; // 导入LIN ID+数据触发详情Bean
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits; // 导入进制常量接口
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil; // 导入键盘数值工具类
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard; // 导入数字键盘弹窗
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.ui.top.view.TopViewEdit; // 导入顶部可编辑视图组件
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道Bean，用于单选按钮组数据
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选按钮组视图
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道转换工具类

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: 顶部触发布局 → 串口触发 → LIN子条件 → ID+数据触发Fragment          ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  核心职责:                                                                 ║
 * ║    1. 提供LIN帧ID+数据触发的UI交互界面（ID+数据输入，ID支持X通配）             ║
 * ║    2. 根据帧ID自动匹配数据位数（4/8/16位）                                   ║
 * ║    3. 管理ID和数据的缓存读写与FPGA指令下发                                   ║
 * ║    4. 将用户输入同步到LinBus总线和Command命令通道                             ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  架构设计:                                                                 ║
 * ║    继承TopLayoutTriggerSerialsBaseDetail基类，实现模板方法模式                ║
 * ║    - initView(): 初始化ID/数据编辑框和消息Bean                               ║
 * ║    - setCache(): 从缓存恢复状态并同步到FPGA                                  ║
 * ║    - setConsumer(): 响应右侧菜单消息，条件匹配时发送触发消息                  ║
 * ║    - onTextListener(): 统一处理文本变更，含ID通配和数据位数匹配               ║
 * ║    - getBitFromId(): 根据帧ID计算数据位数                                   ║
 * ║    - getMatchFid(): 根据数据长度匹配帧ID                                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向:                                                                 ║
 * ║    UI编辑框 → CacheUtil缓存 → SerialsDetailLinIdData Bean                  ║
 * ║              → Command指令(FPGA) → LinBus总线(软件解码)                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系:                                                                 ║
 * ║    - TopLayoutTriggerSerialsBaseDetail (继承)                               ║
 * ║    - LinBus / SerialChannel / ChannelFactory (总线与通道)                   ║
 * ║    - Command (FPGA指令下发)                                                 ║
 * ║    - CacheUtil (持久化缓存)                                                 ║
 * ║    - TopDialogNumberKeyBoard / KeyBoardNumberUtil (键盘与数值工具)           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  使用场景:                                                                 ║
 * ║    当用户在LIN触发模式下选择"ID+数据"子条件时，加载此Fragment                  ║
 * ║    对应LIN子条件索引值为2，帧ID支持X通配符                                   ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

public class TopLayoutTriggerSerialsLinIdData extends TopLayoutTriggerSerialsBaseDetail { // LIN ID+数据触发Fragment，继承串口触发详情基类
    private TopViewEdit linIdDataId; // LIN帧ID编辑框
    private TopViewEdit linIdDataData; // LIN数据编辑框
    private SerialsDetailLinIdData msgLinIdData; // LIN ID+数据触发详情消息Bean

    /**
     * 初始化视图控件及消息Bean
     * @param view Fragment根视图
     */
    @Override
    protected void initView(View view) { // 重写基类方法，初始化视图控件
        linIdDataId = (TopViewEdit) view.findViewById(R.id.linIdDataId); // 查找帧ID编辑框控件
        linIdDataData = (TopViewEdit) view.findViewById(R.id.linIdDataData); // 查找数据编辑框控件
        linIdDataId.setOnClickEditListener(onClickEditListener); // 为帧ID编辑框设置点击监听
        linIdDataData.setOnClickEditListener(onClickEditListener); // 为数据编辑框设置点击监听
        msgLinIdData = new SerialsDetailLinIdData(); // 创建LIN ID+数据消息Bean实例
        msgLinIdData.setLinIdDataId(DIGITS_16X, linIdDataId.getText()); // 设置帧ID值（16进制含X通配）
        msgLinIdData.setLinIdDataData(DIGITS_16, linIdDataData.getText()); // 设置数据值（16进制）
        msgLinIdData.setLinIdDataIdTitle(linIdDataId.getHead()); // 设置帧ID字段标题
        msgLinIdData.setLinIdDataDataTitle(linIdDataData.getHead()); // 设置数据字段标题
    }

    /**
     * 获取布局资源ID
     * @return LIN ID+数据布局资源ID
     */
    @Override
    protected int getLayoutResId() { // 重写基类方法，返回布局资源ID
        return R.layout.layout_triggerserials_liniddata; // 返回LIN ID+数据布局
    }

    /**
     * 获取串口触发详情数据
     * @param detailFlag 详情标志位
     * @return LIN ID+数据触发详情Bean
     */
    @Override
    public ISerialsDetail getSerialsDetail(int detailFlag) { // 重写基类方法，获取串口触发详情
        String id = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_ID + getSerialsNumber()); // 从缓存读取帧ID值
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA + getSerialsNumber()); // 从缓存读取数据值
        String dataID = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID + getSerialsNumber());//帧ID有没有输入X
        if(dataID.contains("X")){ // 如果帧ID包含X通配符
            msgLinIdData.setLinIdDataId(DIGITS_16X, dataID); // 使用含X的原始帧ID
        } else { // 帧ID不包含X通配符
            msgLinIdData.setLinIdDataId(DIGITS_16X, id); // 使用普通帧ID
        }
        msgLinIdData.setLinIdDataData(DIGITS_16, data); // 设置数据到消息Bean（16进制）
        return msgLinIdData; // 返回消息Bean
    }

    /**
     * 响应右侧菜单消息，条件匹配时发送触发消息
     * @param rightMsgSerials 右侧串口菜单消息
     */
    @Override
    protected void setConsumer(RightMsgSerials rightMsgSerials) { // 重写基类方法，处理右侧菜单消息
        if (rightMsgSerials.isSerials1() && serialsNumber==CacheUtil.S1 // 判断是否为串口1且当前序号匹配
                && rightMsgSerials.getSerialsType().getIndex() == RightLayoutSerials.SERIALS_LIN // 判断串口类型是否为LIN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + getSerialsNumber()) == 2) { // 判断LIN子条件是否为ID+数据(索引2)
            //当且仅当当前lin列表选中的是该项时，才向外发送消息
            sendMsg(msgLinIdData, rightMsgSerials.isFromEventBus()); // 发送LIN ID+数据触发消息
        }
    }

    /**
     * 从缓存恢复状态并同步到FPGA和总线
     */
    @Override
    protected void setCache() { // 重写基类方法，从缓存恢复数据
        String id = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_ID + getSerialsNumber()); // 从缓存读取帧ID
        String data = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA + getSerialsNumber()); // 从缓存读取数据
        String dataID = CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID + getSerialsNumber()); // 从缓存读取含X的帧ID
        if (dataID.contains("X")) { // 如果帧ID包含X通配符
            linIdDataId.setText(dataID); // 显示含X的帧ID
        } else { // 帧ID不包含X通配符
            linIdDataId.setText(id); // 显示普通帧ID
        }
        linIdDataData.setText(data); // 设置数据编辑框文本

        int fpgaCh= TChan.toFpgaBySerialNumber(serialsNumber); // 将串口序号转换为FPGA通道号
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
        if (serialChannel == null) return; // 通道为空则直接返回
        LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN); // 获取LIN总线实例
        linBus.setFrameId(LinBus.LIN_TRIGGER_ID_AND_DATA, getMatchFid(id, data)); // 设置LIN触发帧ID（ID+数据模式，自动匹配）
        linBus.setData(toDLong(data, 16)); // 设置LIN触发数据

        msgLinIdData.setLinIdDataId(DIGITS_16X, id); // 同步帧ID到消息Bean
        msgLinIdData.setLinIdDataData(DIGITS_16, data); // 同步数据到消息Bean
        int triggerIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 获取当前触发详情索引
        if (((triggerIndex == TopLayoutTrigger.DETAIL_S1 && serialsNumber==CacheUtil.S1) // 判断是否为S1触发详情且当前为S1
                || (triggerIndex == TopLayoutTrigger.DETAIL_S2 && serialsNumber==CacheUtil.S2) // 判断是否为S2触发详情且当前为S2
                || (triggerIndex == TopLayoutTrigger.DETAIL_S3 && serialsNumber==CacheUtil.S3) // 判断是否为S3触发详情且当前为S3
                || (triggerIndex == TopLayoutTrigger.DETAIL_S4 && serialsNumber==CacheUtil.S4)) // 判断是否为S4触发详情且当前为S4
                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + getSerialsNumber()) == RightLayoutSerials.SERIALS_LIN // 判断右侧菜单是否选中LIN
                && CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + getSerialsNumber()) == 2) { // 判断LIN子条件是否为ID+数据
            sendMsg(msgLinIdData, false); // 发送触发消息（非EventBus来源）
        }
    }

    /**
     * 从外部命令设置触发数据（如EventBus远程同步）
     * @param id 帧ID值
     * @param data 数据值
     * @param isFromEventBus 是否来自EventBus
     */
    public void setCommandData(int id, long data, boolean isFromEventBus) { // 外部命令设置触发数据
        String sId = SerialsUtils.getHexBinFromLong(id, 2, DIGITS_16); // 将帧ID转为2位16进制字符串
        String sData = SerialsUtils.getHexBinFromLong(data, 16, DIGITS_16); // 将数据转为16位16进制字符串
        if (!linIdDataId.getText().equals(sId)) { // 帧ID值有变化时
            onTextListener(linIdDataId, sId, sId, isFromEventBus); // 触发帧ID变更监听
        }
        if (!linIdDataData.getText().equals(sData)) { // 数据值有变化时
            onTextListener(linIdDataData, sData, sData, isFromEventBus); // 触发数据变更监听
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
            case R.id.linIdDataId: // 帧ID编辑框
                //该为16进制,两位限制
                dialogKeyBoard.setOriginalDecimalData(2, DIGITS_16X, onIdListener); // 设置键盘为16进制含X通配2位，绑定帧ID监听
                break;
            case R.id.linIdDataData: // 数据编辑框
                //该为16进制,四位限制
                dialogKeyBoard.setOriginalDecimalData(getBitFromId(linIdDataId.getText()), DIGITS_16, onDataListener); // 设置键盘位数根据帧ID动态确定，绑定数据监听
                break;
        }
    }

    /**
     * 根据帧ID计算数据位数
     * @param id 帧ID字符串
     * @return 数据位数（4/8/16）
     */
    private int getBitFromId(String id) { // 根据帧ID计算数据位数
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 将串口序号转换为FPGA通道号
        int bit = 4; // 默认数据位数为4位（2字节）
        SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道实例
        if (serialChannel == null) return bit; // 通道为空则返回默认值
        LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN); // 获取LIN总线实例
        if (linBus.getLinType() == LinBus.LIN_TYPE_1_3) { // 判断是否为LIN 1.3协议
            if (id.charAt(0) - '0' == 0 || id.charAt(0) - '0' == 1) { // ID首字符为0或1
                bit = 4; // 数据位数为4位（2字节）
            } else if (id.charAt(0) - '0' == 2) { // ID首字符为2
                bit = 8; // 数据位数为8位（4字节）
            } else if (id.charAt(0) - '0' == 3 || id.contains("X")) { // ID首字符为3或包含X通配
                bit = 16; // 数据位数为16位（8字节）
            }
        } else { // 非LIN 1.3协议
            bit = 16; // 数据位数为16位（8字节）
        }
        return bit; // 返回数据位数
    }

    /**
     * 根据数据长度匹配帧ID
     * @param id 帧ID字符串
     * @param data 数据字符串
     * @return 匹配后的帧ID整数值
     */
    private int getMatchFid(String id, String data) { // 根据数据长度匹配帧ID
        int dataLength = data.replace(" ", "").trim().length(); // 计算数据去除空格后的长度
        if (id.contains("X")) { // 如果帧ID包含X通配符
            if (dataLength <= 4) { // 数据长度不超过4位
                id = "1F"; // 匹配帧ID为1F（2字节数据）
            } else if (dataLength <= 8) { // 数据长度不超过8位
                id = "2F"; // 匹配帧ID为2F（4字节数据）
            } else { // 数据长度超过8位
                id = "3F"; // 匹配帧ID为3F（8字节数据）
            }
        }
        return toD(id, 16); // 将帧ID转为十进制整数返回
    }

    /** 帧ID键盘关闭监听，将结果传递给文本变更监听 */
    TopDialogNumberKeyBoard.OnOriginalDismissListener onIdListener = new TopDialogNumberKeyBoard.OnOriginalDismissListener() { // 帧ID键盘关闭回调（含原始结果）
        @Override
        public void onOriginalDismiss(String result, String originalResult) { // 键盘关闭时回调
            onTextListener(linIdDataId, result, originalResult, false); // 触发帧ID文本变更（非EventBus来源）
        }
    };

    /** 数据键盘关闭监听，将结果传递给文本变更监听 */
    TopDialogNumberKeyBoard.OnOriginalDismissListener onDataListener = new TopDialogNumberKeyBoard.OnOriginalDismissListener() { // 数据键盘关闭回调（含原始结果）
        @Override
        public void onOriginalDismiss(String result, String originalResult) { // 键盘关闭时回调
            onTextListener(linIdDataData, result, originalResult, false); // 触发数据文本变更（非EventBus来源）
        }
    };

    /**
     * 统一处理文本变更，更新缓存/Bean/FPGA指令/总线
     * @param view 变更的编辑框视图
     * @param text 格式化后的文本值
     * @param originalResult 原始输入值
     * @param isFromEventBus 是否来自EventBus
     */
    private void onTextListener(TopViewEdit view, String text, String originalResult, boolean isFromEventBus) { // 文本变更统一处理方法
        int cmdCh=TChan.toFpgaChNo(serialsNumber); // 获取FPGA命令通道号
        int fpgaCh=TChan.toFpgaBySerialNumber(serialsNumber); // 获取FPGA物理通道号

        int type = 2;// 2代表帧ID和数据触发，3代表单独数据触发
        if (view.getId() == linIdDataId.getId()) { // 判断是否为帧ID编辑框
            int bit = 4; // 默认数据位数
            if (text.contains("X")) { // 帧ID包含X通配符
                text = "X"; // 将文本设为X
                bit = 16;//帧ID设置为X时候，数据可设置8个字节
                type =3; // 切换为单独数据触发类型
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID + getSerialsNumber(), text); // 缓存含X的帧ID
            } else { // 帧ID不包含X通配符
                text = (text.charAt(0) - '0') > 3 ? "3F" : text;//此结果最大值为3F
                bit = getBitFromId(text); // 根据帧ID计算数据位数
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_DATADATA_ID + getSerialsNumber(), ""); // 清除含X帧ID缓存
            }
            linIdDataId.setEdit(text); // 更新帧ID编辑框显示
            String data = linIdDataData.getText().replace(" ", ""); // 获取当前数据值并去除空格
            if (data.length() < bit) { // 当前数据位数不足时
                data = KeyBoardNumberUtil.reCalculateSpace( // 重新计算空格格式
                        linIdDataData.getText().replace(" ", ""), // 去除空格的数据
                        KeyBoardNumberUtil.DIGITS_16).trim(); // 按16进制重新格式化
            } else { // 当前数据位数超出时
                data = KeyBoardNumberUtil.reCalculateSpace( // 重新计算空格格式
                        KeyBoardNumberUtil.toBits( // 截取到位数
                                linIdDataData.getText().replace(" ", "") // 去除空格的数据
                                , bit), KeyBoardNumberUtil.DIGITS_16).trim(); // 按16进制重新格式化
            }
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_ID + getSerialsNumber(), Long.toHexString(getMatchFid(text, data)).toUpperCase()); // 缓存匹配后的帧ID（16进制大写）
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA + getSerialsNumber(), data); // 缓存数据值
            linIdDataData.setEdit(data); // 更新数据编辑框显示
            msgLinIdData.setLinIdDataId(DIGITS_16, text); // 更新消息Bean帧ID
            msgLinIdData.setLinIdDataData(DIGITS_16, data); // 更新消息Bean数据
            sendMsg(msgLinIdData, isFromEventBus); // 发送触发消息
            //这里需要根据 data length匹配id
            Command.get().getTrigger_lin().setType(cmdCh, type, getMatchFid(linIdDataId.getText(), data), toDLong(linIdDataData.getText(), IDigits.DIGITS_16), false); // 下发LIN触发指令到FPGA
            SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道
            if (serialChannel == null) return; // 通道为空则返回
            LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN); // 获取LIN总线
            if (!isFromEventBus) { // 非EventBus来源时同步到软件解码总线
                if (type == 3) { // 单独数据触发类型
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_DATA); // 设置触发类型为数据触发
                    linBus.setFrameId(LinBus.LIN_TRIGGER_DATA, getMatchFid(text, data)); // 设置帧ID（数据触发模式）
                } else { // 帧ID+数据触发类型
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_ID_AND_DATA); // 设置触发类型为ID+数据触发
                    linBus.setFrameId(LinBus.LIN_TRIGGER_ID_AND_DATA, getMatchFid(text, data)); // 设置帧ID（ID+数据触发模式）
                }
                linBus.setData(toDLong(linIdDataData.getText(), IDigits.DIGITS_16)); // 设置LIN触发数据
                linBus.setDataLength(data.replace(" ", "").length()); // 设置数据长度
            }
        } else if (view.getId() == linIdDataData.getId()) { // 判断是否为数据编辑框
            String linIdD = linIdDataId.getText(); // 获取当前帧ID
            if (linIdD.contains("X")) { // 帧ID包含X通配符
                type = 3; // 切换为单独数据触发类型
            }
            if (originalResult.replace(" ", "").length() < 4) {//最少2个字节
                originalResult = KeyBoardNumberUtil.reCalculateSpace(KeyBoardNumberUtil.toBits( // 补齐到最少4位
                        originalResult.replace(" ", "") // 去除空格的原始结果
                        , 4), IDigits.DIGITS_16); // 按16进制重新格式化
            }
            linIdDataData.setEdit(originalResult); // 更新数据编辑框显示
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN_IDDATA_DATA + getSerialsNumber(), originalResult); // 缓存数据值
            msgLinIdData.setLinIdDataData(DIGITS_16, originalResult); // 更新消息Bean数据
            sendMsg(msgLinIdData, isFromEventBus); // 发送触发消息
            Command.get().getTrigger_lin().setType(cmdCh, type, getMatchFid(linIdD, originalResult), toDLong(linIdDataData.getText(), IDigits.DIGITS_16), false); // 下发LIN触发指令到FPGA
            if (!isFromEventBus) { // 非EventBus来源时同步到软件解码总线
                SerialChannel serialChannel = ChannelFactory.getSerialChannel(fpgaCh); // 获取串口通道
                if (serialChannel == null) return; // 通道为空则返回
                LinBus linBus = (LinBus) serialChannel.getBus(IBus.LIN); // 获取LIN总线
                linBus.setData(toDLong(linIdDataData.getText(), IDigits.DIGITS_16)); // 设置LIN触发数据
                if (type == 3) { // 单独数据触发类型
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_DATA); // 设置触发类型为数据触发
                    linBus.setFrameId(LinBus.LIN_TRIGGER_DATA, getMatchFid(linIdD, linIdDataData.getText())); // 设置帧ID（数据触发模式）
                } else { // 帧ID+数据触发类型
                    linBus.setTriggerType(LinBus.LIN_TRIGGER_ID_AND_DATA); // 设置触发类型为ID+数据触发
                    linBus.setFrameId(LinBus.LIN_TRIGGER_ID_AND_DATA, getMatchFid(linIdD, linIdDataData.getText())); // 设置帧ID（ID+数据触发模式）
                }
                linBus.setDataLength(originalResult.replace(" ", "").length()); // 设置数据长度
            }
        }
    }
}
