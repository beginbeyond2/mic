package com.micsig.tbook.tbookscope.main.mainright;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.util.App;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by yangj on 2017/6/5.
 */

public class ChannelLevelUtil {
    private static final int k = 1000;
    private static final double m = 0.001;
    private static final double u = m * m;

    //倍数
    private static String[] multiples;
    //档位
    private static String[] everys;

    public ChannelLevelUtil() {
        multiples = App.get().getResources().getStringArray(R.array.channelProbeTypeMultiple);
        everys = App.get().getResources().getStringArray(R.array.channelProbeEvery);
    }

    //每个格子代表的单位大小,返回的String不包含A/V
    public static String getUnit(int multipleIndex, int everyIndex) {
        String multiple = multiples[multipleIndex];
        String every = everys[everyIndex];
        return getUnit(multiple, every);
    }

    public static String getUnit(String multiple, String every) {
        int a = Integer.parseInt(multiple.replace("X", "").replace("m", "").replace("k", ""));
        int b = Integer.parseInt(every.replace("u", "").replace("m", "").replace("A", "").replace("V", ""));
        String d = "";
        if (every.contains("A")) {
            d = "A";
        } else if (every.contains("V")) {
            d = "V";
        } else {
            d = "";
        }
        double c = 1;
        if (multiple.contains("m")) {
            c = c * m;
        } else if (multiple.contains("k")) {
            c = c * k;
        }
        if (every.contains("u")) {
            c = c * u;
        } else if (every.contains("m")) {
            c = c * m;
        }
        double x = a * b * c;
        DecimalFormat df = new DecimalFormat("0", new DecimalFormatSymbols(Locale.CHINA));
        if (x >= k) {
            return df.format(x / k) + "k" + d;
        } else if (x >= 1) {
            if (String.valueOf(x).contains(".5")) {
                return x + d;
            }
            return df.format(x) + d;
        } else if (x >= m) {
            return df.format(x * k) + "m" + d;
        } else if (x >= u) {
            return df.format(x * k * k) + "u" + d;
        } else {
            return df.format(x * k * k * k) + "n" + d;
        }
    }
}
