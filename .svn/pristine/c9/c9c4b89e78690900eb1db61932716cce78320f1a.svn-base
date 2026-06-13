package com.micsig.tbook.tbookscope.rightslipmenu.util;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Locale;

public class DataBean implements Serializable {
    private float G;
    private float b;
    private float c;
    private String d;
    private float e;
    private float f;

    public DataBean() {
    }

    public DataBean(float G, float b, float c, String d, float e, float f) {
        this.G = G;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    public float getG() {
        return G;
    }

    public void setG(float g) {
        G = g;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public float getC() {
        return c;
    }

    public void setC(float c) {
        this.c = c;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public float getE() {
        return e;
    }

    public void setE(float e) {
        this.e = e;
    }

    public float getF() {
        return f;
    }

    public void setF(float f) {
        this.f = f;
    }

    @Override
    public String toString() {
        return "DataBean{" +
                "G=" + G +
                ", b=" + b +
                ", c=" + c +
                ", d='" + d + '\'' +
                ", e=" + e +
                ", f=" + f +
                '}';
    }

    public String toCSV() {
        return String.format(Locale.CHINA, "%2.1f,%3.3f,%3.3f,%s,%3.3f,%3.3f", G, b, c, d, e, f);
    }

    //计算e,f
    public void compute() {
        e = 40 / (c - b);
        f = -(45 + b * 40 / (c - b));


        BigDecimal b = new BigDecimal(e);
        e = b.setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
        b = new BigDecimal(f);
        f = b.setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();

    }
}
