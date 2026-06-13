package com.micsig.tbook.tbookscope.scpi;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-24 18:10
 */
public class SCPI_Math_Sample {
//           new SCPICommandStruct( ":SAMPleACQuire:MATH:SRATe?","SCPI_Math_Sample","SRateQ"),
//            new SCPICommandStruct( ":SAMPLeACQuire:MATH:MDEPth?", "SCPI_Math_Sample","MDepthQ"),

    public static String SRateQ(SCPIParam param){

        int source = param.iParam1;//不确定是否为 1-8
        int mathChan = TChan.toMathChan(source);
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChan));
        if (mathChannel != null) {
            double d = mathChannel.getSampleRate2display();
            return String.valueOf(d);
        }
        return "";
    }
    public static String MDepthQ(SCPIParam param){

        int source = param.iParam1;//不确定是否为 1-8
        int mathChan = TChan.toMathChan(source);
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChan));
        if (mathChannel != null) {
            int i = mathChannel.getWaveLen();
            return String.valueOf(i);
        }
        return "";
    }

}
