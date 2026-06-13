package com.micsig.tbook.tbookscope.top.layout.save;

public class StopCondition {
    enum StopConditionType {
        NONE(0), TIME(1), AFTER_N_FRAME(2);

        private final int code;

        StopConditionType(int code) {
            this.code = code;
        }

        public static StopConditionType fromCode(int code) {
            for (StopConditionType stopConditionType : values()) {
                if (stopConditionType.code == code) {
                    return stopConditionType;
                }
            }
            return null;
        }
    }
    private StopConditionType type;
    private String value;

    StopCondition(StopConditionType type, String value) {
        this.type = type;
        this.value = value;
    }


    public static final class StopConditionBuilder{
        private StopConditionType type;
        private String value;


        public StopCondition build(){
            return new StopCondition(type,value);
        }

        public StopConditionBuilder setType(StopConditionType terminateCondition){
            this.type = terminateCondition;
            return this;
        }

        public StopConditionBuilder setValue(String value){
            this.value = value;
            return this;
        }


    }

    public StopConditionType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}


