package com.micsig.tbook.tbookscope.services.ExternalKeys.client;

public class TriggerValueVoltageLine {
    private boolean triggerLevelActive = true;
    private static TriggerValueVoltageLine triggerVoltageLine = null;
    public static TriggerValueVoltageLine getInstance(){
        if(triggerVoltageLine == null){
            triggerVoltageLine = new TriggerValueVoltageLine();
        }
        return triggerVoltageLine;
    }

    public interface ITriggerVoltageLineCallback{
        int OnTriggerLevelNums();
        int OnValueLevelNums();
        void switchTriggerValueLevel();
    }
    private ITriggerVoltageLineCallback triggerVoltageLineCallback = null;
    private TriggerValueVoltageLine(){

    }
    public void setTriggerVoltageLineCallback(ITriggerVoltageLineCallback triggerVoltageLineCallback){
        this.triggerVoltageLineCallback = triggerVoltageLineCallback;
    }
    public void setTriggerLevelActive(boolean triggerLevelActive){
        this.triggerLevelActive = triggerLevelActive;
    }

    public boolean isTriggerlevelActive(){
        return triggerLevelActive;
    }
    public void switchTriggerValueLevel(){
        if(triggerVoltageLineCallback != null){
            triggerVoltageLineCallback.switchTriggerValueLevel();
        }
    }
}
