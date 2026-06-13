package com.micsig.tbook.scope.probe;

import com.micsig.tbook.scope.probe.bean.BaseBean;
import com.micsig.tbook.scope.probe.bean.MDPBean;
import com.micsig.tbook.scope.probe.bean.MSP500Bean;
import com.micsig.tbook.scope.vertical.VerticalAxis;

import java.util.ArrayList;
import java.util.List;

public class ProbeMDP extends BaseProbe{

    public ProbeMDP(BaseBean baseBean){
        super(baseBean);
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

    private MDPBean getBean(){
        return (MDPBean) baseBean;
    }

    @Override
    protected  void defaultParam(){
        super.defaultParam();
//        sendCommand(ProbeCommand.probeCommand(ProbeCommand.TYPE_PROBE_RATE));
    }

    @Override
    public int getMcuNums() {
        return 2;
    }

    @Override
    public boolean isScopeImpedence50(){
        return true;
    }
}
