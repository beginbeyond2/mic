package com.micsig.tbook.scope.probe;

import com.micsig.tbook.scope.probe.bean.BaseBean;
import com.micsig.tbook.scope.probe.bean.MSP500Bean;
import com.micsig.tbook.scope.vertical.VerticalAxis;

import java.util.ArrayList;
import java.util.List;

public class ProbeMSP500 extends BaseProbe{

    public ProbeMSP500(BaseBean baseBean){
        super(baseBean);
        if(baseBean.getScaleValues().size() > 0){
            probeRate = baseBean.getScaleValues().get(0);
        }
    }

    @Override
    public double getProbeRate() {
        return probeRate;
    }

    @Override
    public int getProbeXIndex() {
        return getBean().getScaleIndex(probeRate);
    }

    @Override
    public List<String> getProbeX() {
        return getBean().getScaleName();
    }

    @Override
    public double getProbeXValue(String probeX) {
        return getBean().getScaleValue(probeX);
    }

    @Override
    public String getProbeXName() {
        return getBean().getScaleKey(probeRate);
    }

    private MSP500Bean getBean(){
        return (MSP500Bean) baseBean;
    }

    @Override
    protected void defaultParam() {
    }

    @Override
    public void setProbeRate(double probeRate) {

    }
}
