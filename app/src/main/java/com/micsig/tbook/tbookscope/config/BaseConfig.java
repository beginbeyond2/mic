package com.micsig.tbook.tbookscope.config;

import com.micsig.smart.Property;
import com.micsig.smart.PropertyManage;
import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.BuildConfig;
import com.micsig.tbook.scope.Bus.IBus;
import com.micsig.tbook.scope.Display.Display;
import com.micsig.tbook.scope.Sample.MemDepthFactory;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.vertical.VerticalAxis;

/**
 * Created by zhuzh on 2018-12-5.
 */

public abstract class BaseConfig implements IConfig {

    private final String TAG = "BaseConfig";
    private boolean bEnableFreqCounter = false;
    private boolean bEnableHighLowFilter = false;
    private boolean bEnableAutoRange = false;
    private boolean bDeliveryDate = true;
    private boolean bAutomotive = false;

    protected abstract int getMinVerticalGear();
    protected abstract int getMaxVerticalGear();
    protected abstract int getMinHorizontalGear();
    protected abstract int getMaxHorizontalGear();



    public  BaseConfig(){
        PropertyManage propertyManage = PropertyManage.getInstance();
        propertyManage.update();
        Property property = propertyManage.getProperty();
        setVerticalGear(getMinVerticalGear(),getMaxVerticalGear());
        setHorizontalGear(getMinHorizontalGear(),getMaxHorizontalGear());
        bDeliveryDate = property.isDeliveryDate();
        if(BuildConfig.DEBUG){
            bDeliveryDate = false;
        }
        if(property.isValid() && !BuildConfig.DEBUG){
            setMemDepth(property.getMemDepth());
            setMaxBandWidth(property.getBandWidth()*1000*1000);
            setHighRefresh(property.getHighRefresh());
            for (int i = 0; i < Property.BUS_CNT; i++) {
                setBusEnable(getBusType(i), property.isEnableBus(i));
            }
            bEnableFreqCounter = property.isEnableFreqCounter();
            bEnableHighLowFilter = property.isHighLowPassFilter();
            bEnableAutoRange = property.isEnableAutoRange();
            bAutomotive = property.isEnableAutomotive();
        }
        else
        {
            if(BuildConfig.DEBUG)
            {

                setMemDepth(MemDepthFactory.getDefaultMemDepth());
                setMaxBandWidth(Channel.MAX_BANDWIDTH);
                setHighRefresh(0);
                for (int i = 0; i < Property.BUS_CNT; i++) {
                    setBusEnable(getBusType(i), true);
                }
                bEnableFreqCounter = true;
                bEnableHighLowFilter = true;
                bEnableAutoRange = true;
            }else{
                setMaxBandWidth(70e6);
            }
        }

        String hw = property.getHwVersion();
        if(hw == null){
            property.setHwVersion("1");
            propertyManage.commit();
        }
    }

    private int getBusType(int idx){

        int busType = -1;
        switch (idx){
            case Property.BUS_UART:  //：串型总线
                busType = IBus.UART;
                break;
            case Property.BUS_LIN:
                busType = IBus.LIN;
                break;
            case Property.BUS_SPI:
                busType = IBus.SPI;
                break;
            case Property.BUS_CAN:
                busType = IBus.CAN;
                break;
            case Property.BUS_I2C:
                busType = IBus.I2C;
                break;
            case Property.BUS_1553B:
                busType = IBus.MILSTD1553B;
                break;
            case Property.BUS_429:
                busType = IBus.ARINC429;
                break;
            case Property.BUS_CAN_FD:
                busType = IBus.CAN_FD;
                break;
        }
        return busType;
    }
    public void setHighRefresh(int highRefresh){
        Display.setHighRefreshCounter(highRefresh);
    }

    //配置最大存储深度
    public void setMemDepth(int memDepth){
        if(memDepth > MemDepthFactory.getDefaultMemDepth() || memDepth == 0){
            memDepth =  MemDepthFactory.getDefaultMemDepth();
        }
        MemDepthFactory.setMemDepth(memDepth);
    }
    protected int getMemDepth(){
        return MemDepthFactory.getMemDepthSet();
    }
    //配置垂直方向 最大最小档位
    public void setVerticalGear(int minGear,int maxGear){
        VerticalAxis.setMinGear(minGear);
        VerticalAxis.setMaxGear(maxGear);
    }
    //配置水平方向 最大最小档位
    public void setHorizontalGear(int minGear,int maxGear){
        HorizontalAxis.setMinGear(minGear);
        HorizontalAxis.setMaxGear(maxGear);
    }
    //配置最大带宽
    public void setMaxBandWidth(double maxBandWidth){
        Channel.setMaxBandWidth(maxBandWidth);
    }
    //配置串行总线
    public void setBusEnable(int busType,boolean bEnable){
        IBus.setBusEnable(busType,bEnable);
    }

    @Override
    public boolean isEnableFreqCounter() {
        return bEnableFreqCounter;
    }

    @Override
    public boolean isBusEnable(int busType) {
        int busTypes = -1;
        switch (busType){
            case Property.BUS_UART:  //：串型总线
                busTypes = IBus.UART;
                break;
            case Property.BUS_LIN:
                busTypes = IBus.LIN;
                break;
            case Property.BUS_SPI:
                busTypes = IBus.SPI;
                break;
            case Property.BUS_CAN:
                busTypes = IBus.CAN;
                break;
            case Property.BUS_I2C:
                busTypes = IBus.I2C;
                break;
            case Property.BUS_1553B:
                busTypes = IBus.MILSTD1553B;
                break;
            case Property.BUS_429:
                busTypes = IBus.ARINC429;
                break;
            case Property.BUS_CAN_FD:
                busTypes = IBus.CAN_FD;
                break;
        }
        return IBus.isBusEnable(busTypes);
    }

    @Override
    public boolean isEnableHighLowFilter() {
        return bEnableHighLowFilter;
    }
    @Override
    public boolean isEnableAutoRange() {
        return bEnableAutoRange;
    }

    @Override
    public boolean isDeliveryDate() {
        return bDeliveryDate;
    }

    @Override
    public boolean isBusEnable() {

        for(int i=Property.BUS_UART;i<Property.BUS_CNT;i++){
            if(isBusEnable(i)){
                return true;
            }
        }
        return false;
    }
}
