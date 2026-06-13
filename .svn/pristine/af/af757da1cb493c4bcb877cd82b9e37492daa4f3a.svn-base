package com.micsig.tbook.ui.top.view.scale;

import com.micsig.tbook.ui.util.StrUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Created by yangj on 2017/4/14.
 */

public class TopUtilScale {
    /**
     * 大时间向左调节
     */
    public static final int ACTION_SCALE_LARGE_LEFT = -1;
    /**
     * 大时间向右调节
     */
    public static final int ACTION_SCALE_LARGE_RIGHT = 1;
    /**
     * 小时间向左调节
     */
    public static final int ACTION_SCALE_SMALL_LEFT = -2;
    /**
     * 小时间向右调节
     */
    public static final int ACTION_SCALE_SMALL_RIGHT = 2;
    /**
     * 保存且关闭
     */
    public static final int ACTION_SCALE_FINISH = 0;

    public static final long TIME_S2NS = 1000 * 1000 * 1000;
    public static final long TIME_MS2NS = 1000 * 1000;
    public static final long TIME_US2NS = 1000;

    public static final int TIME_MIN_INTERVAL = 8;
    public static final long DEFAULT_MIN_TIME = 8;//单位ns
    public static final long DEFAULT_MAX_TIME = 10 * TIME_S2NS;//单位ns

    public static final long TIME_COMMON_MIN = 200;
    public static final long TIME_COMMON_MAX = DEFAULT_MAX_TIME;
    public static final long TIME_PULSEWIDTH_MIN = DEFAULT_MIN_TIME;
    public static final long TIME_PULSEWIDTH_MAX = DEFAULT_MAX_TIME;
    public static final long TIME_LOGIC_MIN = DEFAULT_MIN_TIME;
    public static final long TIME_LOGIC_MAX = DEFAULT_MAX_TIME;
    public static final long TIME_NEDGE_MIN = DEFAULT_MIN_TIME;
    public static final long TIME_NEDGE_MAX = DEFAULT_MAX_TIME;
    public static final long TIME_RUNT_MIN = DEFAULT_MIN_TIME;
    public static final long TIME_RUNT_MAX = DEFAULT_MAX_TIME;
    public static final long TIME_SLOPE_MIN = DEFAULT_MIN_TIME;
    public static final long TIME_SLOPE_MAX = DEFAULT_MAX_TIME;
    public static final long TIME_TIMEOUT_MIN = DEFAULT_MIN_TIME;
    public static final long TIME_TIMEOUT_MAX = DEFAULT_MAX_TIME;

    public static final String UNIT_S = "s";
    public static final String UNIT_MS = "ms";
    public static final String UNIT_US = "μs";
    public static final String UNIT_NS = "ns";

    public static final double[] LARGE_ITEM_VALUES = {100, 1, 10, 100, 1, 10, 100, 1};
    public static final String[] LARGE_ITEM_UNITS = {UNIT_NS, UNIT_US, UNIT_US, UNIT_US, UNIT_MS, UNIT_MS, UNIT_MS, UNIT_S};
    public static final long[] LARGE_ITEM_NSS = {100, 100 * TIME_US2NS, 100 * 10 * TIME_US2NS, 100 * 100 * TIME_US2NS, 100 * TIME_MS2NS, 100 * 10 * TIME_MS2NS, 100 * 100 * TIME_MS2NS, 100 * TIME_S2NS};

    public static void getValueFromNS(long ns, ScaleValue scaleValue) {
        if (scaleValue == null) return;
        DecimalFormat df = new DecimalFormat("###0.00", new DecimalFormatSymbols(Locale.CHINA));
        double value;
        if (ns / TIME_S2NS >= 1) {
            value = Double.parseDouble(df.format(ns * 1.0 / TIME_S2NS));
            scaleValue.setValue(value, UNIT_S, 1);
        } else if (ns / TIME_MS2NS >= 1) {
            value = Double.parseDouble(df.format(ns * 1.0 / TIME_MS2NS));
            if (value / 100 >= 1) {
                scaleValue.setValue(value, UNIT_MS, 100);
            } else if (value / 10 >= 1) {
                scaleValue.setValue(value, UNIT_MS, 10);
            } else {
                scaleValue.setValue(value, UNIT_MS, 1);
            }
        } else if (ns / TIME_US2NS >= 1) {
            value = Double.parseDouble(df.format(ns * 1.0 / TIME_US2NS));
            if (value / 100 >= 1) {
                scaleValue.setValue(value, UNIT_US, 100);
            } else if (value / 10 >= 1) {
                scaleValue.setValue(value, UNIT_US, 10);
            } else {
                scaleValue.setValue(value, UNIT_US, 1);
            }
        } else {
            String d = df.format(ns * 1.0);
            value = Double.parseDouble(d);
            scaleValue.setValue(value, UNIT_NS, 100);
//            if (value / 100 >= 1) {
//                scaleValue.setValue(value, UNIT_NS, 100);
//            } else if (value / 10 >= 1) {
//                scaleValue.setValue(value, UNIT_NS, 10);
//            } else {
//                scaleValue.setValue(value, UNIT_NS, 1);
//            }
        }
    }

    public static long getNSFromValue(String value) {
        if (StrUtil.isEmpty(value)) return 0;
        try {
            value = value.toLowerCase();
            if (value.endsWith(UNIT_NS)) {
                return (long) (Double.valueOf(value.replace(UNIT_NS, "")) * 1);
            } else if (value.endsWith(UNIT_US)) {
                return (long) (Double.valueOf(value.replace(UNIT_US, "")) * TIME_US2NS);
            } else if (value.endsWith(UNIT_MS)) {
                return (long) (Double.valueOf(value.replace(UNIT_MS, "")) * TIME_MS2NS);
            } else {
                return (long) (Double.valueOf(value.replace(UNIT_S, "")) * TIME_S2NS);
            }
        } catch (Exception e) {
            return 0;
        }
    }

    public ScaleValue createScaleValue() {
        return new ScaleValue();
    }

    public class ScaleValue {
        public double value;
        public String itemUnit;
        public double itemValue;

        public void setValue(double value, String itemUnit, double itemValue) {
            this.value = value;
            this.itemUnit = itemUnit;
            this.itemValue = itemValue;
        }
    }

    public static long checkTime(long curNs, long minNs, long maxNs) {
        curNs = curNs < minNs ? minNs : curNs;
        curNs = curNs > maxNs ? maxNs : curNs;
        curNs = curNs - curNs % TIME_MIN_INTERVAL;
        return curNs;
    }
}
