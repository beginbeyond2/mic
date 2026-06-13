package com.micsig.tbook.tbookscope.wavezone.trigger;

import android.graphics.Canvas;
import android.util.Log;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SerialChannel;
import com.micsig.tbook.tbookscope.main.mainbottom.MainTopMsgRightGone;
import com.micsig.tbook.tbookscope.middleware.Tag;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.MTriggerLevel;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by liwb on 2017/5/15.
 */

public class VoltageLineManage implements IWorkMode {

    private static final String TAG = "VoltageLineManage";

    //region 单例创建
    public static class TriggerVoltageManageHolder {
        public static final VoltageLineManage instance = new VoltageLineManage();
    }

    public static VoltageLineManage getInstance() {
        return TriggerVoltageManageHolder.instance;
    }
    //endregion

    public static final String VoltageLineType_Trigger = "Trigger";
    public static final String VoltageLineType_Value1 = "Value1";
    public static final String VoltageLineType_Value2 = "Value2";
    public static final String VoltageLineType_Value3 = "Value3";
    public static final String VoltageLineType_Value4 = "Value4";

    //显示预值电平的通道
    private static final int ShowDiscreetVoltage_Value1 = 0x01;
    private static final int ShowDiscreetVoltage_Value2 = 0x02;
    private static final int ShowDiscreetVoltage_ALL = 0xFF;

    //region 属性
    private Map<String, ITriggerLine> mapVoltageLine;
    private String key;

    private  final String[] type=new String[]{VoltageLineType_Trigger,VoltageLineType_Value1,VoltageLineType_Value2,VoltageLineType_Value3,VoltageLineType_Value4};
    //    private int currShowDiscreetVoltage = ShowDiscreetVoltage_ALL;
    private String frontDiscreetVoltageLine = VoltageLineType_Value1;
    private ArrayList<DiscreetVoltageLineInfoBean> listDiscreetVoltageLineInfo = new ArrayList<>();
    private int listDiscreetInfoIndex = 0;
    private String CurrOptionVoltageLineType = VoltageLineType_Value1;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void init() {
    }

    public Map<String, ITriggerLine> getMapVoltageLine() {
        return mapVoltageLine;
    }

    //region IWorkMode接口
    private
    @WorkMode
    int mWorkMode = IWorkMode.WorkMode_YT;

    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        if (mWorkMode == workMode) return;
        this.mWorkMode = workMode;
//        mapVoltageLine.get(VoltageLineType_Trigger).switchWorkMode(workMode);
//        mapVoltageLine.get(VoltageLineType_Value1).switchWorkMode(workMode);
//        mapVoltageLine.get(VoltageLineType_Value2).switchWorkMode(workMode);
//        mapVoltageLine.get(VoltageLineType_Value3).switchWorkMode(workMode);
//        mapVoltageLine.get(VoltageLineType_Value4).switchWorkMode(workMode);
        for (Map.Entry value : mapVoltageLine.entrySet()) {
            ITriggerLine v = (ITriggerLine) value.getValue();
            v.switchWorkMode(workMode);
        }
    }

    //endregion

    public void setActive(String key) {
        if (!VoltageLineType_Trigger.equals(key)) {
            String s = getDiscreetVoltageLineCurKey();
            if (s != null) {
                key = s;
            }
        }
        Log.d(TAG, "setActive() called with: key = [" + key + "]");
        for (Map.Entry<String, ITriggerLine> entry : mapVoltageLine.entrySet()) {
            Logger.d(TAG, "" + entry.getKey());
            if (key.equals(entry.getKey())) {
                entry.getValue().setActive(true);
            } else {
                entry.getValue().setActive(false);
            }
        }
    }

    public void foreachDiscreetLine(java.util.function.Consumer<DiscreetVoltageLine> action, java.util.function.Predicate<DiscreetVoltageLine> continuePredicate)  {
        for(Map.Entry<String,ITriggerLine> entry:mapVoltageLine.entrySet()){
            if (entry.getKey().equals(VoltageLineType_Trigger)) continue;
            DiscreetVoltageLine line= (DiscreetVoltageLine) mapVoltageLine.get(entry.getKey());
            if (continuePredicate.test(line)){
                continue;
            }
            action.accept(line);
        }
    }

    public String getSerialKey(int serialNo){
        return type[serialNo];
    }
    public ITriggerLine getVoltageLine(int serialNum){
        String mTypeKey= type[serialNum];
        return mapVoltageLine.getOrDefault(mTypeKey,null);
    }
    /***
     * 返回指定类
     * @param key  触发电平：VoltageLineType_Trigger="Voltage"
     *              预值电平： VoltageLineType_Value="Value"
     * @return 返回类
     */
    public ITriggerLine getVoltageLine(String key) {
        this.key = key;
        if (mapVoltageLine.get(key) != null) {
            return mapVoltageLine.get(key);
        } else {
            return null;
        }
    }


    /***
     * 返回指定类
     * @param curKey  预值电平： VoltageLineType_Value1="Value1" or VoltageLineType_Value2="Value2"
     * @return 返回类
     */
    public DiscreetVoltageLine getOtherValueLine(String curKey) {
        switch (key) {
            case VoltageLineType_Value1:
                key = VoltageLineType_Value2;
                break;
            case VoltageLineType_Value2:
                key = VoltageLineType_Value3;
                break;
            case VoltageLineType_Value3:
                key = VoltageLineType_Value4;
                break;
            case VoltageLineType_Value4:
                key = VoltageLineType_Value1;
                break;
            default:
                return null;
        }
        if (mapVoltageLine.get(key) != null) {
            return (DiscreetVoltageLine) mapVoltageLine.get(key);
        } else {
            return null;
        }
    }

    /***
     * 返回类  使用前要保存最后调用过一次setKey(String key)函数，否则返回空
     * @return
     */
    public ITriggerLine getVoltageLine() {
        return this.getVoltageLine(this.key);
    }
    //endregion

    public VoltageLineManage() {
        mapVoltageLine = new HashMap<>();
        TriggerVoltageLine voltage = new TriggerVoltageLine(VoltageLineType_Trigger);
        mapVoltageLine.put(VoltageLineType_Trigger, voltage);
        DiscreetVoltageLine discreet1 = new DiscreetVoltageLine(VoltageLineType_Value1);
        mapVoltageLine.put(VoltageLineType_Value1, discreet1);
        DiscreetVoltageLine discreet2 = new DiscreetVoltageLine(VoltageLineType_Value2);
        mapVoltageLine.put(VoltageLineType_Value2, discreet2);
        DiscreetVoltageLine discreet3 = new DiscreetVoltageLine(VoltageLineType_Value3);
        mapVoltageLine.put(VoltageLineType_Value3, discreet3);
        DiscreetVoltageLine discreet4 = new DiscreetVoltageLine(VoltageLineType_Value4);
        mapVoltageLine.put(VoltageLineType_Value4, discreet4);

        RxBus.getInstance().getObservable(RxEnum.MAIN_TOPRIGHT_GONE).subscribe(consumerTopRightGone);
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(ConsumerWorkModeChange);
    }

    private Consumer<MainTopMsgRightGone> consumerTopRightGone = new Consumer<MainTopMsgRightGone>() {
        @Override
        public void accept(MainTopMsgRightGone mainTopMsgRightGone) throws Exception {
            setAllLineVisible(mainTopMsgRightGone.isVisible());
        }
    };

    private Consumer<WorkModeBean> ConsumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            for (String key : mapVoltageLine.keySet()) {
                ITriggerLine line = mapVoltageLine.get(key);
                line.refresh();
                int idx = 0;
                switch (key) {
                    case VoltageLineType_Value1:
                        idx = ChannelFactory.S1;
                        break;
                    case VoltageLineType_Value2:
                        idx = ChannelFactory.S2;
                        break;
                    case VoltageLineType_Value3:
                        idx = ChannelFactory.S3;
                        break;
                    case VoltageLineType_Value4:
                        idx = ChannelFactory.S4;
                        break;
                }
                SerialChannel channel = ChannelFactory.getSerialChannel(idx);
                if (idx != 0 && channel != null) {
                    channel.setPos(Tools.getLevelCache(CacheUtil.VALUE_CHANNEL + line.getChannelId()));
                }
            }
        }
    };

    public void setAllLineVisible(boolean visible) {
        for (ITriggerLine line : mapVoltageLine.values()) {
            line.setVisible(visible);
        }
    }


    //region 预值电平合并显示

    private synchronized void mergeDiscreetVoltageLine(String curLine) {
//        Log.d(Tag.Debug, String.format("VoltageLineManage.mergeDiscreetVoltageLine: " ));
        ITriggerLine line= mapVoltageLine.get(curLine);
        listDiscreetVoltageLineInfo.clear();
        listDiscreetVoltageLineInfo.addAll(getVoltageLine(curLine).getShowChannelInfo());
        for (Map.Entry<String, ITriggerLine> entry : mapVoltageLine.entrySet()) {
            if (entry.getKey().equals(VoltageLineType_Trigger)) continue;
            if (line==mapVoltageLine.get(entry.getValue())) continue;
            if (getVoltageLine(entry.getKey()).getShowState() ==false) continue;

            List<DiscreetVoltageLineInfoBean> list = getVoltageLine(entry.getKey()).getShowChannelInfo();
            for (int j = 0; j < list.size(); j++){
                    DiscreetVoltageLineInfoBean desBean = list.get(j);
                    DiscreetVoltageLine desLine = (DiscreetVoltageLine) mapVoltageLine.get(desBean.VoltageLineName);
                    setDiscreetVoltageState(desBean, false);

                    int idx= Tools.indexOf(listDiscreetVoltageLineInfo,(src)->src.ChannelId==desBean.ChannelId
                            && src.VoltageLineChannelIndex == desBean.VoltageLineChannelIndex);
                    DiscreetVoltageLineInfoBean srcBean=null;
                    DiscreetVoltageLine srcLine =null;
                    if (idx!=-1) {
                        srcBean = listDiscreetVoltageLineInfo.get(idx);
                        srcLine= (DiscreetVoltageLine) mapVoltageLine.get(srcBean.VoltageLineName);
                    }
                    if (idx==-1){
                        listDiscreetVoltageLineInfo.add(desBean);
                    }else if ((srcBean.ShowMode == ITriggerLine.ShowMode_Two && desBean.ShowMode != ITriggerLine.ShowMode_Two && srcBean.ChannelId == desBean.ChannelId)){
//                        syncDiscreetLineSrcTwo(srcLine, desLine);
                    }else if ((srcBean.ShowMode != ITriggerLine.ShowMode_Two && desBean.ShowMode == ITriggerLine.ShowMode_Two && srcBean.ChannelId == desBean.ChannelId)){
//                        syncDiscreetLineDesTwo(srcLine, desLine);
                    }else if ((srcBean.ShowMode != ITriggerLine.ShowMode_Two && desBean.ShowMode != ITriggerLine.ShowMode_Two && srcBean.ChannelId == desBean.ChannelId)){
//                        syncDiscreetLine(srcLine, desLine);
                    }
            }
        }

        for (int i = 0; i < listDiscreetVoltageLineInfo.size(); i++) {
            DiscreetVoltageLineInfoBean bean=listDiscreetVoltageLineInfo.get(i);
            setDiscreetVoltageState(bean, true);
//            Log.d(Tag.Debug, String.format("VoltageLineManage.mergeDiscreetVoltageLine line: %s",listDiscreetVoltageLineInfo.get(i) ));
        }
        listDiscreetInfoIndex = 0;
    }




    private void syncDiscreetLineSrcTwo(DiscreetVoltageLine src, DiscreetVoltageLine des) {
        double high= WaveManage.get().getPositionYButWorkModeXY(src.getChannelId()) - Tools.getLevelCache(CacheUtil.VALUE_CHANNEL_H + src.getChannelId());;
        double low =WaveManage.get().getPositionYButWorkModeXY(src.getChannelId()) - Tools.getLevelCache(CacheUtil.VALUE_CHANNEL + src.getChannelId());;
        src.setCurrYIndex(ITriggerLine.VoltageLine_High);
        src.setOtherY(ITriggerLine.VoltageLine_High,high);
        src.setCurrYIndex(ITriggerLine.VoltageLine_Low);
        src.setOtherY(ITriggerLine.VoltageLine_Low,low);
        src.setVisibleLine(false);

        high=src.getOtherY(ITriggerLine.VoltageLine_High);
        low=src.getOtherY(ITriggerLine.VoltageLine_Low);
        if (des.getShowMode()!=ITriggerLine.ShowMode_Two){
            if (des.getOtherY(src.getChannelId())!=low){
                des.setOtherY(src.getChannelId(),low);
            }
        }else {
            if (des.getOtherY(ITriggerLine.VoltageLine_High)!=high
                && des.getChannelId()==src.getChannelId()){
                des.setOtherY(ITriggerLine.VoltageLine_High,high);
            }
            if (des.getOtherY(ITriggerLine.VoltageLine_Low)!=low
                && des.getChannelId()==src.getChannelId()){
                des.setOtherY(ITriggerLine.VoltageLine_Low,low);
            }
        }
    }

    private void syncDiscreetLineDesTwo(DiscreetVoltageLine src, DiscreetVoltageLine des) {
        TChan.foreachChan((chan)->{
            if (des.getOtherY(ITriggerLine.VoltageLine_Low)!=src.getOtherY(chan)
                && src.getTriggerVoltageLine_logic_state()[chan]!=MTriggerLevel.TriggerLevel_Mode_work_Logic_None){
                des.setOtherY(ITriggerLine.VoltageLine_Low,src.getOtherY(chan));
            }
        });
    }

    private void syncDiscreetLine(DiscreetVoltageLine src, DiscreetVoltageLine des) {
        TChan.foreachChan((chan)->{
            double pCh = WaveManage.get().getPositionY(chan)-Tools.getLevelCache(CacheUtil.VALUE_CHANNEL+chan);
            src.setOtherY(chan,pCh);
            if (des.getOtherY(chan)!=src.getOtherY(chan)
                    && src.getTriggerVoltageLine_logic_state()[chan]!= MTriggerLevel.TriggerLevel_Mode_work_Logic_None){
                des.setOtherY(chan,src.getOtherY(chan));
            }
        });

    }


    //返回所有预值电平的通道
    private void getAllDiscreetVoltageLineChannelIdInS1S2(String CurrOptionVoltageLineType) {
        synchronized (listDiscreetVoltageLineInfo) {
            listDiscreetVoltageLineInfo.clear();
            if (getVoltageLine(VoltageLineType_Value1).getShowState()) {
                listDiscreetVoltageLineInfo = getVoltageLine(VoltageLineType_Value1).getShowChannelInfo();
            }
            if (getVoltageLine(VoltageLineType_Value2).getShowState()) {
                listDiscreetVoltageLineInfo.addAll(getVoltageLine(VoltageLineType_Value2).getShowChannelInfo());
            }
            if (getVoltageLine(VoltageLineType_Value3).getShowState()) {
                listDiscreetVoltageLineInfo.addAll(getVoltageLine(VoltageLineType_Value3).getShowChannelInfo());
            }
            if (getVoltageLine(VoltageLineType_Value4).getShowState()) {
                listDiscreetVoltageLineInfo.addAll(getVoltageLine(VoltageLineType_Value4).getShowChannelInfo());
            }

            if (listDiscreetVoltageLineInfo.size() <= 0) {
                return;
            }
            //过滤重复的通道
            for (int i = 0; i < listDiscreetVoltageLineInfo.size(); i++) {
                DiscreetVoltageLineInfoBean bean1 = listDiscreetVoltageLineInfo.get(i);

                for (int j = listDiscreetVoltageLineInfo.size() - 1; j > i; j--) {
                    DiscreetVoltageLineInfoBean bean2 = listDiscreetVoltageLineInfo.get(j);

                    if (bean1.ShowMode == ITriggerLine.ShowMode_Two && bean2.ShowMode != ITriggerLine.ShowMode_Two && bean1.ChannelId == bean2.ChannelId) {
                        if (bean1.VoltageLineName.equals(CurrOptionVoltageLineType)) {
                            setDiscreetVoltageState(listDiscreetVoltageLineInfo.get(j), false);
                            listDiscreetVoltageLineInfo.remove(j);
                        } else {
                            setDiscreetVoltageState(listDiscreetVoltageLineInfo.get(i), false);
                            listDiscreetVoltageLineInfo.remove(i);
                        }
                        break;
                    }
                    if (bean2.ShowMode == ITriggerLine.ShowMode_Two && bean1.ShowMode != ITriggerLine.ShowMode_Two && bean2.ChannelId == bean1.ChannelId) {
                        if (bean2.VoltageLineName.equals(CurrOptionVoltageLineType)) {
                            setDiscreetVoltageState(listDiscreetVoltageLineInfo.get(i), false);
                            listDiscreetVoltageLineInfo.remove(i);
                        } else {
                            setDiscreetVoltageState(listDiscreetVoltageLineInfo.get(j), false);
                            listDiscreetVoltageLineInfo.remove(j);
                        }
                        break;
                    }
                    if (bean1.ChannelId == bean2.ChannelId && bean1.ShowMode != ITriggerLine.ShowMode_Two && bean2.ShowMode != ITriggerLine.ShowMode_Two) {
                        if (bean1.VoltageLineName.equals(CurrOptionVoltageLineType)) {
                            setDiscreetVoltageState(listDiscreetVoltageLineInfo.get(j), false);
                            listDiscreetVoltageLineInfo.remove(j);
                        } else {
                            setDiscreetVoltageState(listDiscreetVoltageLineInfo.get(i), false);
                            listDiscreetVoltageLineInfo.remove(i);
                        }
                        break;
                    }
                }
            }
            //剩下都是要显示的
            for (int i = 0; i < listDiscreetVoltageLineInfo.size(); i++) {
                setDiscreetVoltageState(listDiscreetVoltageLineInfo.get(i), true);
            }
            listDiscreetInfoIndex = 0;
        }
    }

    /**
     * 设置阈值电 是否显示
     *
     * @param infoBean
     * @param drawChannel
     */
    private void setDiscreetVoltageState(DiscreetVoltageLineInfoBean infoBean, boolean drawChannel) {
        int state = DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight;
        if (drawChannel == false) state = DiscreetVoltageLine.TriggerVoltageLine_NoDraw;
        if(infoBean.ShowMode == ITriggerLine.ShowMode_Two){
            ((DiscreetVoltageLine) getVoltageLine(infoBean.VoltageLineName)).setTriggerVoltageLine_logic_state(infoBean.VoltageLineChannelIndex, state);
        }else {
            ((DiscreetVoltageLine) getVoltageLine(infoBean.VoltageLineName)).setTriggerVoltageLine_logic_state(infoBean.ChannelId, state);
        }
    }

    public DiscreetVoltageLineInfoBean getCurrDisCreetVoltageLineInfo() {
        synchronized (listDiscreetVoltageLineInfo) {
            if (listDiscreetInfoIndex >= listDiscreetVoltageLineInfo.size() || listDiscreetInfoIndex < 0)
                return null;
            return listDiscreetVoltageLineInfo.get(listDiscreetInfoIndex);
        }
    }

    public boolean isFirstVoltageLine() {
        return listDiscreetInfoIndex == 0;
    }

    public boolean isLastVoltageLine() {
        return listDiscreetInfoIndex == (listDiscreetVoltageLineInfo.size() - 1);
    }

    public int getDiscreetVoltageLineInS1S2Count() {
        return listDiscreetVoltageLineInfo.size();
    }

    public String getDiscreetVoltageLineCurKey() {
        synchronized (listDiscreetVoltageLineInfo) {
            if (listDiscreetInfoIndex >= 0 && listDiscreetInfoIndex < listDiscreetVoltageLineInfo.size()) {
                DiscreetVoltageLineInfoBean bean = listDiscreetVoltageLineInfo.get(listDiscreetInfoIndex);
                return bean.VoltageLineName;
            }
        }
        return null;
    }

    public DiscreetVoltageLineInfoBean setPreDiscreetVoltageLineInS1S2() {
        synchronized (listDiscreetVoltageLineInfo) {
            if (listDiscreetVoltageLineInfo.size() <= 0) return null;
            listDiscreetInfoIndex--;
            if (listDiscreetInfoIndex < 0)
                listDiscreetInfoIndex = listDiscreetVoltageLineInfo.size() - 1;

            DiscreetVoltageLineInfoBean bean = listDiscreetVoltageLineInfo.get(listDiscreetInfoIndex);
            setDiscreetVoltageFrontShow(bean.VoltageLineName);
            if (bean.ShowMode == ITriggerLine.ShowMode_Two) {
                getVoltageLine(bean.VoltageLineName).setCurrYIndex(bean.VoltageLineChannelIndex);
            } else {
                getVoltageLine(bean.VoltageLineName).setCurrYIndex(bean.ChannelId);
            }
//          Logger.i(TAG, "预值电平大小：" + listDiscreetVoltageLineInfo.size());
//        Logger.i(TAG,"id:"+bean.ChannelId+" 通道号："+bean.VoltageLineName+" 类型 ："+bean.VoltageLineChannelIndex+" showMode:"+bean.ShowMode);
            if (bean.VoltageLineName.equals(VoltageLineType_Value1)) {
                getVoltageLine(VoltageLineType_Value2).setCurrYIndex(0);
            } else if (bean.VoltageLineName.equals(VoltageLineType_Value2)) {
                getVoltageLine(VoltageLineType_Value1).setCurrYIndex(0);
            }
            getVoltageLine(VoltageLineType_Value1).setVisibleLine(false);
            getVoltageLine(VoltageLineType_Value2).setVisibleLine(false);

            Logger.d(TAG, "Pre:" + bean.toString());
            return bean;
        }
    }

    public DiscreetVoltageLineInfoBean setNextDiscreetVoltageLineInS1S2() {
        synchronized (listDiscreetVoltageLineInfo) {
            if (listDiscreetVoltageLineInfo.size() <= 0) return null;
            listDiscreetInfoIndex++;
            if (listDiscreetInfoIndex >= listDiscreetVoltageLineInfo.size())
                listDiscreetInfoIndex = 0;
            DiscreetVoltageLineInfoBean bean = listDiscreetVoltageLineInfo.get(listDiscreetInfoIndex);
            setDiscreetVoltageFrontShow(bean.VoltageLineName);
            if (bean.ShowMode == ITriggerLine.ShowMode_Two) {
                getVoltageLine(bean.VoltageLineName).setCurrYIndex(bean.VoltageLineChannelIndex);
            } else {
                getVoltageLine(bean.VoltageLineName).setCurrYIndex(bean.ChannelId);
            }

            if (bean.VoltageLineName.equals(VoltageLineType_Value1)) {
                getVoltageLine(VoltageLineType_Value2).setCurrYIndex(0);
            } else if (bean.VoltageLineName.equals(VoltageLineType_Value2)) {
                getVoltageLine(VoltageLineType_Value1).setCurrYIndex(0);
            }

            Logger.d(TAG, "Next:" + bean.toString());
            return bean;
        }
    }

    /**
     * 把s1，s2的图标统一成一套....
     *
     * @return
     */
    public DiscreetVoltageLineInfoBean setCurrDiscreetVoltageLineInS1S2() {
        synchronized (listDiscreetVoltageLineInfo) {
            if (listDiscreetVoltageLineInfo.size() <= 0) return null;
            if (listDiscreetInfoIndex >= listDiscreetVoltageLineInfo.size())
                listDiscreetInfoIndex = 0;
            else if (listDiscreetInfoIndex < 0)
                listDiscreetInfoIndex = listDiscreetVoltageLineInfo.size() - 1;


            DiscreetVoltageLineInfoBean bean = listDiscreetVoltageLineInfo.get(listDiscreetInfoIndex);
            setDiscreetVoltageFrontShow(bean.VoltageLineName);
            if (bean.ShowMode == ITriggerLine.ShowMode_Two) {
                getVoltageLine(bean.VoltageLineName).setCurrYIndex(bean.VoltageLineChannelIndex);
            } else {
                getVoltageLine(bean.VoltageLineName).setCurrYIndex(bean.ChannelId);
            }

//            if (bean.VoltageLineName.equals(VoltageLineType_Value1)) {
//                getVoltageLine(VoltageLineType_Value2).setCurrYIndex(bean.ChannelId);
//            } else if (bean.VoltageLineName.equals(VoltageLineType_Value2)) {
//                getVoltageLine(VoltageLineType_Value1).setCurrYIndex(bean.ChannelId);
//            }
            return bean;
        }
    }

    //endregion


    private void setDiscreetVoltageFrontShow(String discreetVoltageLineType) {
        frontDiscreetVoltageLine = discreetVoltageLineType;
    }


    /**
     * 设置预值电平显示通道
     *
     * @param VoltageLineType 参数以VoltageLineType_Value1，VoltageLineType_Value2，
     *                        例：两个都显示调用setDiscreetVoltageShowState(VoltageLineType_Value1+VoltageLineType_Value2)
     */
    public void setDiscreetVoltageShowState(String VoltageLineType, int CurLineIdx) {
//        Logger.d("limh", "VoltageLineType= " + VoltageLineType + " CurLineIdx= " + CurLineIdx);
        this.CurrOptionVoltageLineType =type[CurLineIdx];
//        if (VoltageLineType.contains(VoltageLineType_Value1) && VoltageLineType.contains(VoltageLineType_Value2)) {
//            currShowDiscreetVoltage = ShowDiscreetVoltage_ALL;
//            getVoltageLine(VoltageLineType_Value1).setShowState(true);
//            getVoltageLine(VoltageLineType_Value2).setShowState(true);
//        } else if (VoltageLineType.contains(VoltageLineType_Value1)) {
//            currShowDiscreetVoltage = ShowDiscreetVoltage_Value1;
//            getVoltageLine(VoltageLineType_Value1).setShowState(true);
//            getVoltageLine(VoltageLineType_Value2).setShowState(false);
//        } else if (VoltageLineType.contains(VoltageLineType_Value2)) {
//            currShowDiscreetVoltage = ShowDiscreetVoltage_Value2;
//            getVoltageLine(VoltageLineType_Value1).setShowState(false);
//            getVoltageLine(VoltageLineType_Value2).setShowState(true);
//        } else {
//            getVoltageLine(VoltageLineType_Value1).setShowState(false);
//            getVoltageLine(VoltageLineType_Value2).setShowState(false);
//        }

        getVoltageLine(VoltageLineType_Value1).setShowState(false);
        getVoltageLine(VoltageLineType_Value2).setShowState(false);
        getVoltageLine(VoltageLineType_Value3).setShowState(false);
        getVoltageLine(VoltageLineType_Value4).setShowState(false);
        if (VoltageLineType.contains(VoltageLineType_Value1)) {
            getVoltageLine(VoltageLineType_Value1).setShowState(true);
        }
        if (VoltageLineType.contains(VoltageLineType_Value2)) {
            getVoltageLine(VoltageLineType_Value2).setShowState(true);
        }
        if (VoltageLineType.contains(VoltageLineType_Value3)) {
            getVoltageLine(VoltageLineType_Value3).setShowState(true);
        }
        if (VoltageLineType.contains(VoltageLineType_Value4)) {
            getVoltageLine(VoltageLineType_Value4).setShowState(true);
        }
        mergeDiscreetVoltageLine(this.CurrOptionVoltageLineType);
//        getAllDiscreetVoltageLineChannelIdInS1S2(CurrOptionVoltageLineType);
    }


    public void draw(Canvas canvas) {
    }

    public void draw(ICanvasGL canvas) {
        mapVoltageLine.get(VoltageLineType_Trigger).draw(canvas);
        if (frontDiscreetVoltageLine.equals(VoltageLineType_Value1)) {
            mapVoltageLine.get(VoltageLineType_Value2).draw(canvas);
            mapVoltageLine.get(VoltageLineType_Value3).draw(canvas);
            mapVoltageLine.get(VoltageLineType_Value4).draw(canvas);
            mapVoltageLine.get(VoltageLineType_Value1).draw(canvas);
        } else if (frontDiscreetVoltageLine.equals(VoltageLineType_Value2)) {
            mapVoltageLine.get(VoltageLineType_Value1).draw(canvas);
            mapVoltageLine.get(VoltageLineType_Value3).draw(canvas);
            mapVoltageLine.get(VoltageLineType_Value4).draw(canvas);
            mapVoltageLine.get(VoltageLineType_Value2).draw(canvas);
        } else if (frontDiscreetVoltageLine.equals(VoltageLineType_Value3)) {
            mapVoltageLine.get(VoltageLineType_Value1).draw(canvas);
            mapVoltageLine.get(VoltageLineType_Value2).draw(canvas);
            mapVoltageLine.get(VoltageLineType_Value4).draw(canvas);
            mapVoltageLine.get(VoltageLineType_Value3).draw(canvas);
        } else if (frontDiscreetVoltageLine.equals(VoltageLineType_Value4)) {
            mapVoltageLine.get(VoltageLineType_Value1).draw(canvas);
            mapVoltageLine.get(VoltageLineType_Value2).draw(canvas);
            mapVoltageLine.get(VoltageLineType_Value3).draw(canvas);
            mapVoltageLine.get(VoltageLineType_Value4).draw(canvas);
        }
    }

    public void refresh() {
        for (String key : mapVoltageLine.keySet()) {
            ITriggerLine line = mapVoltageLine.get(key);
            if (line != null) {
                line.refresh();
            }
        }
    }

}
