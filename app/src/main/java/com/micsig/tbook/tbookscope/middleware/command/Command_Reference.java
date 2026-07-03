package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包


import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂
import com.micsig.tbook.scope.channel.RefChannel; // 参考通道
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava枚举定义
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 光标管理
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage; // 波形管理
import com.micsig.tbook.ui.wavezone.IWave; // 波形接口
import com.micsig.tbook.ui.wavezone.TChan; // 通道常量定义

/**
 * Created by liwb on 2018/1/12.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                          Command_Reference                                  |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 示波器参考通道(REF)命令处理模块                                    |
 * | 核心职责: 处理SCPI参考通道相关指令，包括REF功能开关、通道开关、水平/垂直档位  |
 * |          设置与查询、当前通道选择、偏移设置与查询、采样率/存储深度查询等       |
 * | 架构设计: 命令模式，作为Command子模块，接收SCPI指令并通过RxBus通知UI层更新    |
 * | 数据流向: SCPI指令 → 本类(设置/查询) → RxBus → UI层;                        |
 * |          垂直偏移时直接操作CursorManage和WaveManage                           |
 * | 依赖关系: Command, CommandMsgToUI, RxBus, RxEnum, ChannelFactory,            |
 * |           RefChannel, CursorManage, WaveManage, GlobalVar, TChan             |
 * | 使用场景: 远程控制参考通道显示/参数、查询参考通道信息时使用                   |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Reference {
//     new SCPICommandStruct(":REFerence:DISPlay","SCPI_Reference","Display"),//打开或关闭REF功能
//            new SCPICommandStruct(":REFerence:DISPlay?","SCPI_Reference","DisplayQ"),//查询REF功能打开或关闭
//            new SCPICommandStruct(":REFerence:ENABle","SCPI_Reference","Enable"),//打开或关闭指定的参考通道
//            new SCPICommandStruct(":REFerence:ENABle?","SCPI_Reference","EnableQ"),//查询指定的参考通道打开或关闭
//            new SCPICommandStruct(":REFerence:HSCale","SCPI_Reference","Hscale"),//设置参考通道的水平档位
//            new SCPICommandStruct(":REFerence:PLUS:HSCale","SCPI_Reference","Plus_Hscale"),//设置参考通道的水平档位
//            new SCPICommandStruct(":REFerence:HSCale?","SCPI_Reference","HscaleQ"),//查询参考通道的水平档位
//            new SCPICommandStruct(":REFerence:VSCale","SCPI_Reference","Vscale"),//设置参考通道的垂直档位
//            new SCPICommandStruct(":REFerence:PLUS:VSCale","SCPI_Reference","Plus_Vscale"),//设置参考通道的垂直档位
//            new SCPICommandStruct(":REFerence:VSCale?","SCPI_Reference","VscaleQ"),//查询参考通道的垂直档位
//            new SCPICommandStruct(":REFerence:CURRent","SCPI_Reference","Current"),//选择当前参考通道
//            new SCPICommandStruct(":REFerence:PLUS:HOFFset","SCPI_Reference","Plus_Hoffset"),//选择当前参考通道
//            new SCPICommandStruct(":REFerence:PLUS:VOFFset","SCPI_Reference","Plus_Voffset"),//选择当前参考通道
//    //1.1新添加 2016.12.8
//            new SCPICommandStruct(":REFerence:POSition","SCPI_Reference","Position"),//设置垂直偏移
//            new SCPICommandStruct(":REFerence:POSition?","SCPI_Reference","PositionQ"),//查询垂直偏移
//            new SCPICommandStruct(":REFerence:TIMebase:POSition","SCPI_Reference","Timebase_Position"),//设置水平偏移
//            new SCPICommandStruct(":REFerence:TIMebase:POSition?","SCPI_Reference","Timebase_PositionQ"),//查询水平偏移
//            new SCPICommandStruct(":REFerence:PLUS:TIMebase:POSition","SCPI_Reference","Plus_Timebase_Position"),//设置水平偏移
//            new SCPICommandStruct(":REFerence:PLUS:POSition","SCPI_Reference","Plus_position"),//设置垂直偏移
//            new SCPICommandStruct(":ACQuire:REF:SRATe?","SCPI_Reference","REF_SRateQ"),//查询采样率
//            new SCPICommandStruct(":ACQuire:REF:MDEPth?","SCPI_Reference","REF_MDepthQ"),//查询存储深度

    private boolean display; // REF功能是否开启
    private boolean[] displayRef = new boolean[TChan.S1]; // 各参考通道的显示状态数组
    private double[] hscaleRef = new double[TChan.S1]; // 各参考通道的水平档位数组
    private double[] vscaleRef = new double[TChan.S1]; // 各参考通道的垂直档位数组
    private double[] position=new double[TChan.S1]; // 各参考通道的垂直偏移数组
    private double[] hPosition=new double[TChan.S1]; // 各参考通道的水平偏移数组
    private double[] sRate=new double[4]; // 参考通道采样率数组
    private double[] mDepth=new double[4]; // 参考通道存储深度数组

    /**
     * 打开或关闭REF功能
     *
     * @param refIndex    参考通道索引
     * @param display     是否显示REF功能
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Display(int refIndex, boolean display, boolean isUpdateUI) {
//        if (this.display == display) return;
        this.display = display; // 保存REF显示状态
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_REF_DISPLAY); // 设置消息标志为REF显示
            String param = String.valueOf(refIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display); // 构建参数
            msgToUI.setParam(String.valueOf(display)); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询REF功能打开或关闭
     *
     * @return REF功能是否开启
     */
    public boolean DisplayQ() {
        return display; // 返回REF显示状态
    }

    /**
     * 打开或关闭指定的参考通道
     *
     * @param source      参考通道索引 {REF1| REF2| REF3| REF4}
     * @param display     是否显示该参考通道
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Enable(int source, boolean display, boolean isUpdateUI) {
//        if (displayRef[source] == display) return;
        displayRef[source] = display; // 保存指定参考通道的显示状态
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_REF_ENABLE); // 设置消息标志为REF通道开关
            String param = String.valueOf(source) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display); // 构建参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询指定的参考通道打开或关闭
     *
     * @param source 参考通道索引
     * @return 该参考通道是否开启
     */
    public boolean EnableQ(int source) {
        return displayRef[source]; // 返回指定参考通道的显示状态
    }

    /**
     * 设置参考通道的水平档位
     *
     * @param source      参考通道索引 {REF1| REF2| REF3| REF4}
     * @param scale       水平档位值 1ns~1ks
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Hscale(int source, double scale, boolean isUpdateUI) {
//        if (hscaleRef[source] == scale) return;

        hscaleRef[source] = scale; // 保存水平档位值
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_REF_Hscal); // 设置消息标志为REF水平档位
            String param = String.valueOf(source) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(scale); // 构建参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }

    }

    /**
     * 递增/递减设置参考通道的水平档位
     *
     * @param source      参考通道索引
     * @param plus        1为加一档，-1为减一档
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Plus_Hscale(int source, int plus, boolean isUpdateUI) {
        if (plus == 1) { // 加一档
            hscaleRef[source] += 1; // 水平档位索引加1
        } else if (plus == -1) { // 减一档
            hscaleRef[source] -= 1; // 水平档位索引减1
        }
        Hscale(source, hscaleRef[source], isUpdateUI); // 调用Hscale设置新档位
    }

    /**
     * 查询参考通道的水平档位
     *
     * @param source 参考通道索引
     * @return 水平档位值
     */
    public double HscaleQ(int source) {
        return hscaleRef[source]; // 返回水平档位值
    }

    /**
     * 设置参考通道的垂直档位
     *
     * @param source      参考通道索引
     * @param scale       垂直档位值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Vscale(int source, double scale, boolean isUpdateUI) {
//        if (vscaleRef[source] == scale) return;

        vscaleRef[source] = scale; // 保存垂直档位值
        RefChannel refChannel = ChannelFactory.getRefChannel(source); // 获取参考通道对象
        scale=scale / refChannel.getRefProbeRate(); // 除以探头比率换算实际档位
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_REF_Vscal); // 设置消息标志为REF垂直档位
            String param = String.valueOf(source) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(scale); // 构建参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 递增/递减设置参考通道的垂直档位
     *
     * @param source      参考通道索引
     * @param plus        1为加一档，-1为减一档
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Plus_Vscale(int source, int plus, boolean isUpdateUI) {
        if (plus == 1) { // 加一档
            vscaleRef[source] += 1; // 垂直档位索引加1
        } else if (plus == -1) { // 减一档
            vscaleRef[source] -= 1; // 垂直档位索引减1
        }
        Vscale(source, vscaleRef[source], isUpdateUI); // 调用Vscale设置新档位
    }

    /**
     * 查询参考通道的垂直档位
     *
     * @param source 参考通道索引
     * @return 垂直档位值
     */
    public double VscaleQ(int source) {
        return vscaleRef[source]; // 返回垂直档位值
    }

    /**
     * 选择当前参考通道
     *
     * @param source      参考通道索引
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Current(int source, boolean isUpdateUI) {
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_REF_Current_Channel); // 设置消息标志为当前REF通道
            String param = String.valueOf(source); // 构建参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 递增/递减设置参考通道的水平偏移
     *
     * @param source      参考通道索引
     * @param plus        1为加，-1为减
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Plus_Hoffset(int source, int plus, boolean isUpdateUI) {
        switch (source) { // 根据参考通道索引处理
            case 1: // REF1通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
            case 2: // REF2通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
            case 3: // REF3通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
            case 4: // REF4通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
        }
        if (isUpdateUI) { // 判断是否需要更新UI（当前未实现）
        }
    }

    /**
     * 递增/递减设置参考通道的垂直偏移
     *
     * @param source      参考通道索引
     * @param plus        1为加，-1为减
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Plus_Voffset(int source, int plus, boolean isUpdateUI) {
        switch (source) { // 根据参考通道索引处理
            case 1: // REF1通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
            case 2: // REF2通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
            case 3: // REF3通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
            case 4: // REF4通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
        }
        if (isUpdateUI) { // 判断是否需要更新UI（当前未实现）
        }
    }

    /**
     * 设置垂直偏移
     *
     * @param source      参考通道索引
     * @param position    垂直偏移值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Position(int source, double position, boolean isUpdateUI) {
        //档位转像素
        RefChannel ch= ChannelFactory.getRefChannel(source); // 获取参考通道对象
        double d= 0; // 每像素对应的垂直值
        if (ch != null) { // 通道存在
            d = ch.getVerticalPerPix(); // 获取每像素垂直值
        }
        int offsetPix= GlobalVar.get().getMainWave().y/2 - (int)(position/d); // 将偏移值转换为像素偏移

        this.position[source]=position; // 保存垂直偏移值
        if (isUpdateUI) { // 判断是否需要更新UI
            CursorManage.getInstance().setScpiChanIdx(source+1); // 设置SCPI通道索引
            CursorManage.getInstance().setCursorTrace(true); // 启用光标追踪
            WaveManage.get().setPositionY(source + 1, ((int) offsetPix)); // 设置波形垂直位置
            CursorManage.setCursorByScaleTrace(); // 根据档位追踪设置光标
            CursorManage.getInstance().setCursorTrace(false); // 关闭光标追踪
        }
    }

    /**
     * 查询垂直偏移
     *
     * @param source      参考通道索引
     * @param isUpdateUI  是否同步更新UI界面
     * @return 垂直偏移值
     */
    public double PositionQ(int source,boolean isUpdateUI){
        if (ChannelFactory.isRefCh(source)) { // 判断是否为参考通道
            RefChannel ch = ChannelFactory.getRefChannel(source); // 获取参考通道对象
            if (ch != null) { // 通道存在
                double d = ch.getVerticalPerPix(); // 获取每像素垂直值
                return (GlobalVar.get().getMainWave().y / 2 - WaveManage.get().getPositionY(source+1)) * d; // 像素偏移转换为实际偏移值
            }
        }
        return 0; // 非参考通道返回0
    }

    /**
     * 设置水平偏移
     *
     * @param source      参考通道索引
     * @param position    水平偏移值
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Timebase_Position(int source, double position, boolean isUpdateUI) {
        this.hPosition[source]=position; // 保存水平偏移值
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_REF_Timebase_Position); // 设置消息标志为REF水平偏移
            String param = String.valueOf(source) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(position); // 构建参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询水平偏移
     *
     * @param source      参考通道索引
     * @param isUpdateUI  是否同步更新UI界面
     * @return 水平偏移值
     */
    public double Timebase_PositionQ(int source,boolean isUpdateUI){
        return hPosition[source]; // 返回水平偏移值
    }

    /**
     * 递增/递减设置参考通道的水平偏移
     *
     * @param source      参考通道索引
     * @param plus        1为加，-1为减
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Plus_Timebase_Position(int source, int plus, boolean isUpdateUI) {
        switch (source) { // 根据参考通道索引处理
            case 1: // REF1通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
            case 2: // REF2通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
            case 3: // REF3通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
            case 4: // REF4通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
        }
        if (isUpdateUI) { // 判断是否需要更新UI（当前未实现）
        }
    }

    /**
     * 递增/递减设置参考通道的垂直偏移
     *
     * @param source      参考通道索引
     * @param plus        1为加，-1为减
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Plus_position(int source, int plus, boolean isUpdateUI) {
        switch (source) { // 根据参考通道索引处理
            case 1: // REF1通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
            case 2: // REF2通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
            case 3: // REF3通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
            case 4: // REF4通道
                if (plus == 1) { // 加
                } else if (plus == -1) { // 减
                }
                break;
        }
        if (isUpdateUI) { // 判断是否需要更新UI（当前未实现）
        }
    }

    /**
     * 查询参考通道的采样率
     *
     * @param source 参考通道索引
     * @return 采样率字符串
     */
    public String REF_SRateQ(int source){
        RefChannel refChannel = ChannelFactory.getRefChannel(ChannelFactory.REF1+source); // 获取参考通道对象
        if (refChannel!=null) { // 通道存在
            double d = refChannel.getSampleRate2display(); // 获取显示用采样率
            return String.valueOf(d); // 返回采样率字符串
        }
        return ""; // 通道不存在返回空字符串
    }

    /**
     * 查询参考通道的存储深度
     *
     * @param source 参考通道索引
     * @return 存储深度（波形长度）字符串
     */
    public String REF_MDepthQ(int source){
        RefChannel refChannel = ChannelFactory.getRefChannel(ChannelFactory.REF1+source); // 获取参考通道对象
        if (refChannel!=null) { // 通道存在
            int i = refChannel.getWaveLen(); // 获取波形长度（存储深度）
            return String.valueOf(i); // 返回存储深度字符串
        }
        return ""; // 通道不存在返回空字符串
    }

}
