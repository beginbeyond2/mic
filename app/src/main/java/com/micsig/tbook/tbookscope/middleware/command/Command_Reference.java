package com.micsig.tbook.tbookscope.middleware.command;


import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by liwb on 2018/1/12.
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

    private boolean display;
    private boolean[] displayRef = new boolean[TChan.S1];
    private double[] hscaleRef = new double[TChan.S1];
    private double[] vscaleRef = new double[TChan.S1];
    private double[] position=new double[TChan.S1];
    private double[] hPosition=new double[TChan.S1];
    private double[] sRate=new double[4];
    private double[] mDepth=new double[4];

    /**
     * 打开或关闭REF功能
     */
    public void Display(int refIndex, boolean display, boolean isUpdateUI) {
//        if (this.display == display) return;
        this.display = display;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_REF_DISPLAY);
            String param = String.valueOf(refIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display);
            msgToUI.setParam(String.valueOf(display));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询REF功能打开或关闭
     */
    public boolean DisplayQ() {
        return display;
    }

    /**
     * 打开或关闭指定的参考通道
     *
     * @param source {REF1| REF2| REF3| REF4}
     */
    public void Enable(int source, boolean display, boolean isUpdateUI) {
//        if (displayRef[source] == display) return;
        displayRef[source] = display;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_REF_ENABLE);
            String param = String.valueOf(source) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(display);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 查询指定的参考通道打开或关闭
     */
    public boolean EnableQ(int source) {
        return displayRef[source];
    }

    /**
     * 设置参考通道的水平档位
     *
     * @param source {REF1| REF2| REF3| REF4}
     * @param scale  1ns~1ks
     */
    public void Hscale(int source, double scale, boolean isUpdateUI) {
//        if (hscaleRef[source] == scale) return;

        hscaleRef[source] = scale;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_REF_Hscal);
            String param = String.valueOf(source) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(scale);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }

    }

    /**
     * 设置参考通道的水平档位
     */
    public void Plus_Hscale(int source, int plus, boolean isUpdateUI) {
        if (plus == 1) {
            hscaleRef[source] += 1;
        } else if (plus == -1) {
            hscaleRef[source] -= 1;
        }
        Hscale(source, hscaleRef[source], isUpdateUI);
    }

    /**
     * 查询参考通道的水平档位
     */
    public double HscaleQ(int source) {
        return hscaleRef[source];
    }

    /**
     * 设置参考通道的垂直档位
     */
    public void Vscale(int source, double scale, boolean isUpdateUI) {
//        if (vscaleRef[source] == scale) return;

        vscaleRef[source] = scale;
        RefChannel refChannel = ChannelFactory.getRefChannel(source);
        scale=scale / refChannel.getRefProbeRate();
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_REF_Vscal);
            String param = String.valueOf(source) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(scale);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 设置参考通道的垂直档位
     */
    public void Plus_Vscale(int source, int plus, boolean isUpdateUI) {
        if (plus == 1) {
            vscaleRef[source] += 1;
        } else if (plus == -1) {
            vscaleRef[source] -= 1;
        }
        Vscale(source, vscaleRef[source], isUpdateUI);
    }

    /**
     * 查询参考通道的垂直档位
     */
    public double VscaleQ(int source) {
        return vscaleRef[source];
    }

    /**
     * 选择当前参考通道
     */
    public void Current(int source, boolean isUpdateUI) {
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_REF_Current_Channel);
            String param = String.valueOf(source);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /**
     * 选择当前参考通道
     */
    public void Plus_Hoffset(int source, int plus, boolean isUpdateUI) {
        switch (source) {
            case 1:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
            case 2:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
            case 3:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
            case 4:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
        }
        if (isUpdateUI) {
        }
    }

    /**
     * 选择当前参考通道
     */
    public void Plus_Voffset(int source, int plus, boolean isUpdateUI) {
        switch (source) {
            case 1:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
            case 2:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
            case 3:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
            case 4:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
        }
        if (isUpdateUI) {
        }
    }

    /**
     * 设置垂直偏移
     */
    public void Position(int source, double position, boolean isUpdateUI) {
        //档位转像素
        RefChannel ch= ChannelFactory.getRefChannel(source);
        double d= 0;
        if (ch != null) {
            d = ch.getVerticalPerPix();
        }
        int offsetPix= GlobalVar.get().getMainWave().y/2 - (int)(position/d);

        this.position[source]=position;
        if (isUpdateUI) {
            CursorManage.getInstance().setScpiChanIdx(source+1);
            CursorManage.getInstance().setCursorTrace(true);
            WaveManage.get().setPositionY(source + 1, ((int) offsetPix));
            CursorManage.setCursorByScaleTrace();
            CursorManage.getInstance().setCursorTrace(false);
        }
    }

    public double PositionQ(int source,boolean isUpdateUI){
        if (ChannelFactory.isRefCh(source)) {
            RefChannel ch = ChannelFactory.getRefChannel(source);
            if (ch != null) {
                double d = ch.getVerticalPerPix();
                return (GlobalVar.get().getMainWave().y / 2 - WaveManage.get().getPositionY(source+1)) * d;
            }
        }
        return 0;
    }
    /**
     * 设置水平偏移
     */
    public void Timebase_Position(int source, double position, boolean isUpdateUI) {
        this.hPosition[source]=position;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_REF_Timebase_Position);
            String param = String.valueOf(source) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(position);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public double Timebase_PositionQ(int source,boolean isUpdateUI){
        return hPosition[source];
    }

    /**
     * 设置水平偏移
     */
    public void Plus_Timebase_Position(int source, int plus, boolean isUpdateUI) {
        switch (source) {
            case 1:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
            case 2:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
            case 3:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
            case 4:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
        }
        if (isUpdateUI) {
        }
    }

    /**
     * 设置垂直偏移
     */
    public void Plus_position(int source, int plus, boolean isUpdateUI) {
        switch (source) {
            case 1:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
            case 2:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
            case 3:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
            case 4:
                if (plus == 1) {
                } else if (plus == -1) {
                }
                break;
        }
        if (isUpdateUI) {
        }
    }

    public String REF_SRateQ(int source){
        RefChannel refChannel = ChannelFactory.getRefChannel(ChannelFactory.REF1+source);
        if (refChannel!=null) {
            double d = refChannel.getSampleRate2display();
            return String.valueOf(d);
        }
        return "";
    }
    public String REF_MDepthQ(int source){
        RefChannel refChannel = ChannelFactory.getRefChannel(ChannelFactory.REF1+source);
        if (refChannel!=null) {
            int i = refChannel.getWaveLen();
            return String.valueOf(i);
        }
        return "";
    }

}
