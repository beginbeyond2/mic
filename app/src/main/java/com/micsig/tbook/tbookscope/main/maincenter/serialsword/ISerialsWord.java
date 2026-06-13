package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

public interface ISerialsWord {
    public static final int TYPE_S1 = TChan.S1;
    public static final int TYPE_S2 = TChan.S2;
    public static final int TYPE_S3 = TChan.S3;
    public static final int TYPE_S4 = TChan.S4;
    public static final int TYPE_S12 = TChan.S1 + TChan.S2 + TChan.S3 + TChan.S4;

    /**
     * lin的data数据中，每行最多能显示下的字符数
     */
    public static final int MAXCHAR_EACHROW_DATA_LIN = 42;

    /**
     * can的data数据中，每行最多能显示下的字符数
     */
    public static final int MAXCHAR_EACHROW_DATA_CAN = 48;

    /**
     * spi的data数据中，每行最多能显示下的字符数
     */
    public static final int MAXCHAR_EACHROW_DATA_SPI = 130;

    /**
     * i2c的data数据中，每行最多能显示下的字符数
     */
    public static final int MAXCHAR_EACHROW_DATA_I2C = 42;

    /**
     * 429的data数据中，每行最多能显示下的字符数
     */
    public static final int MAXCHAR_EACHROW_DATA_429 = 35;

    /**
     * 1553b的data数据中，每行最多能显示下的字符数
     */
    public static final int MAXCHAR_EACHROW_DATA_1553B = 35;
}
