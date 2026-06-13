package com.micsig.tbook.tbookscope.top.layout.trigger;

import com.micsig.tbook.scope.Trigger.Trigger;
import com.micsig.tbook.scope.Trigger.TriggerLogic;
import com.micsig.tbook.scope.Trigger.TriggerVideo;

/**
 * Created by yangj on 2018/4/10.
 */

public class TopMatchTrigger {
    public static int triggerTypeViewToScope(int indexView) {
        switch (indexView) {
            case TopLayoutTrigger.DETAIL_COMMON:        //常规
                return -1;
            case TopLayoutTrigger.DETAIL_EDGE:          //边沿
                return Trigger.TRIG_TYPE_EDGE;
            case TopLayoutTrigger.DETAIL_PULSEWIDTH:    //脉宽
                return Trigger.TRIG_TYPE_PULSE;
            case TopLayoutTrigger.DETAIL_LOGIC:         //逻辑
                return Trigger.TRIG_TYPE_LOGIC;
            case TopLayoutTrigger.DETAIL_NEDGE:         //N边沿
                return Trigger.TRIG_TYPE_NEDGE;
            case TopLayoutTrigger.DETAIL_RUNT:          //欠幅
                return Trigger.TRIG_TYPE_LOW_PULSE;
            case TopLayoutTrigger.DETAIL_SLOPE:         //斜率
                return Trigger.TRIG_TYPE_SLOPE;
            case TopLayoutTrigger.DETAIL_TIMEOUT:       //超时
                return Trigger.TRIG_TYPE_TIMEOUT;
            case TopLayoutTrigger.DETAIL_VIDEO:         //视频
                return Trigger.TRIG_TYPE_VIDEO;
            case TopLayoutTrigger.DETAIL_S1:            //S1
                return Trigger.TRIG_TYPE_SERIAL1;
            case TopLayoutTrigger.DETAIL_S2:            //S2
                return Trigger.TRIG_TYPE_SERIAL2;
            case TopLayoutTrigger.DETAIL_S3:            //S1
                return Trigger.TRIG_TYPE_SERIAL3;
            case TopLayoutTrigger.DETAIL_S4:            //S2
                return Trigger.TRIG_TYPE_SERIAL4;
            default:
                return -1;
        }
    }

    public static int triggerTypeScopeToView(int indexScope) {
        switch (indexScope) {
            case Trigger.TRIG_TYPE_EDGE:
                return TopLayoutTrigger.DETAIL_EDGE;          //边沿
            case Trigger.TRIG_TYPE_PULSE:
                return TopLayoutTrigger.DETAIL_PULSEWIDTH;    //脉宽
            case Trigger.TRIG_TYPE_LOGIC:
                return TopLayoutTrigger.DETAIL_LOGIC;         //逻辑
            case Trigger.TRIG_TYPE_NEDGE:
                return TopLayoutTrigger.DETAIL_NEDGE;         //N边沿
            case Trigger.TRIG_TYPE_LOW_PULSE:
                return TopLayoutTrigger.DETAIL_RUNT;          //欠幅
            case Trigger.TRIG_TYPE_SLOPE:
                return TopLayoutTrigger.DETAIL_SLOPE;         //斜率
            case Trigger.TRIG_TYPE_TIMEOUT:
                return TopLayoutTrigger.DETAIL_TIMEOUT;       //超时
            case Trigger.TRIG_TYPE_VIDEO:
                return TopLayoutTrigger.DETAIL_VIDEO;         //视频
            case Trigger.TRIG_TYPE_SERIAL1:
                return TopLayoutTrigger.DETAIL_S1;            //S1
            case Trigger.TRIG_TYPE_SERIAL2:
                return TopLayoutTrigger.DETAIL_S2;            //S2
            case Trigger.TRIG_TYPE_SERIAL3:
                return TopLayoutTrigger.DETAIL_S3;            //S1
            case Trigger.TRIG_TYPE_SERIAL4:
                return TopLayoutTrigger.DETAIL_S4;            //S2
            default:
                return TopLayoutTrigger.DETAIL_COMMON;
        }
    }

    public static int triggerConditionToScope(int condition) {
        switch (condition) {
            case 0:
                return Trigger.TRIGGER_RELATION_LESS_THAN;
            case 1:
                return Trigger.TRIGGER_RELATION_MORE_THAN;
            case 2:
                return Trigger.TRIGGER_RELATION_EQUAL;
            case 3:
                return Trigger.TRIGGER_RELATION_NOT_EQUAL;
            case 4:
                return Trigger.TRIGGER_RELATION_TRUE;
            case 5:
                return Trigger.TRIGGER_RELATION_FALSE;
            default:
                return Trigger.TRIGGER_RELATION_LESS_THAN;
        }
    }


    public static int triggerConditionFromScope(int scope) {
        switch (scope) {
            case Trigger.TRIGGER_RELATION_LESS_THAN:
                return 0;
            case Trigger.TRIGGER_RELATION_MORE_THAN:
                return 1;
            case Trigger.TRIGGER_RELATION_EQUAL:
                return 2;
            case Trigger.TRIGGER_RELATION_NOT_EQUAL:
                return 3;
            case Trigger.TRIGGER_RELATION_TRUE:
                return 4;
            case Trigger.TRIGGER_RELATION_FALSE:
                return 5;
            default:
                return 0;
        }
    }

    public static int triggerLogicChToScope(int chIndex) {
        switch (chIndex) {
            case 0:
                return TriggerLogic.LOGIC_HIGH;
            case 1:
                return TriggerLogic.LOGIC_LOW;
            case 2:
                return TriggerLogic.LOGIC_NONE;
            default:
                return TriggerLogic.LOGIC_HIGH;
        }
    }

    public static int triggerLogicChFromScope(int scopeIndex) {
        switch (scopeIndex) {
            case TriggerLogic.LOGIC_HIGH:
                return 0;
            case TriggerLogic.LOGIC_LOW:
                return 1;
            case TriggerLogic.LOGIC_NONE:
                return 2;
            default:
                return 0;
        }
    }

    public static int triggerLogicRelationToScope(int relation) {
        switch (relation) {
            default:
            case 0:
                return TriggerLogic.LOGIC_AND;
            case 1:
                return TriggerLogic.LOGIC_OR;
            case 2:
                return TriggerLogic.LOGIC_NAND;
            case 3:
                return TriggerLogic.LOGIC_NOR;
        }
    }

    public static int triggerVideoTriggerToScope(int standard, int trigger) {
        switch (standard) {
            case 0:
            case 1:
            case 2:
            case 4:
                return trigger;
            case 3:
            case 5:
                switch (trigger) {
                    case 0:
                        return TriggerVideo.VIDEO_TRIGGER_ALL_FIELDS;
                    case 1:
                        return TriggerVideo.VIDEO_TRIGGER_ALL_LINES;
                    case 2:
                        return TriggerVideo.VIDEO_TRIGGER_LINE;
                }
                break;
        }
        return TriggerVideo.VIDEO_TRIGGER_ALL_FIELDS;
    }

    public static int triggerVideoTriggerFromScope(int standard, int scopeTrigger) {
        switch (standard) {
            case 0:
            case 1:
            case 2:
            case 4:
                return scopeTrigger;
            case 3:
            case 5:
                switch (scopeTrigger) {
                    case TriggerVideo.VIDEO_TRIGGER_ALL_FIELDS:
                        return 0;
                    case TriggerVideo.VIDEO_TRIGGER_ALL_LINES:
                        return 1;
                    case TriggerVideo.VIDEO_TRIGGER_LINE:
                        return 2;
                }
                break;
        }
        return 0;
    }

    public static int triggerVideoFrequencyToScope(int standard, int frequency) {
        switch (standard) {
            case 0:
            case 1:
                return TriggerVideo.VIDEO_FREQUENCY_50HZ;
            case 2:
                return TriggerVideo.VIDEO_FREQUENCY_60HZ;
            case 3:
            case 4:
                switch (frequency) {
                    case 0:
                        return TriggerVideo.VIDEO_FREQUENCY_60HZ;
                    case 1:
                        return TriggerVideo.VIDEO_FREQUENCY_50HZ;
                }
            case 5:
                switch (frequency) {
                    case 0:
                        return TriggerVideo.VIDEO_FREQUENCY_60HZ;
                    case 1:
                        return TriggerVideo.VIDEO_FREQUENCY_50HZ;
                    case 2:
                        return TriggerVideo.VIDEO_FREQUENCY_30HZ;
                    case 3:
                        return TriggerVideo.VIDEO_FREQUENCY_25HZ;
                    case 4:
                        return TriggerVideo.VIDEO_FREQUENCY_24HZ;
                }
        }
        return TriggerVideo.VIDEO_FREQUENCY_60HZ;
    }

    public static int triggerVideoFrequencyFromScope(int standard, int scopeFrequency) {
        switch (standard) {
            case 0:
            case 1:
            case 2:
                return 0;
            case 3:
            case 4:
                switch (scopeFrequency) {
                    case TriggerVideo.VIDEO_FREQUENCY_60HZ:
                        return 0;
                    case TriggerVideo.VIDEO_FREQUENCY_50HZ:
                        return 1;
                }
            case 5:
                switch (scopeFrequency) {
                    case TriggerVideo.VIDEO_FREQUENCY_60HZ:
                        return 0;
                    case TriggerVideo.VIDEO_FREQUENCY_50HZ:
                        return 1;
                    case TriggerVideo.VIDEO_FREQUENCY_30HZ:
                        return 2;
                    case TriggerVideo.VIDEO_FREQUENCY_25HZ:
                        return 3;
                    case TriggerVideo.VIDEO_FREQUENCY_24HZ:
                        return 4;
                }
        }
        return 0;
    }
}
