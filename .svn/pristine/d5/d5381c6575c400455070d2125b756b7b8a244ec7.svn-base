package com.micsig.tbook.tbookscope.top.layout.save;

import java.time.LocalDateTime;
import java.util.List;


public class AutoSaveTaskCondition {

    private List<SaveType> saveType;

    private String savePath, saveFileName;

    private LocalDateTime startTime;

    private TimeInterval intervalTime = TimeInterval.ONE_MILLISECONDS;

    private SaveMode saveMode = SaveMode.CIRCULAR_SAVE;



    private StopCondition stopCondition;

    private String  suffixNum;

    private  List<Integer> selectedChannel;

    public enum TimeInterval {
         ONE_MILLISECONDS(0,100),TEN_SECOND(1, 10*1000), THIRTY_SECOND(2, 30*1000), ONE_MINUTE(3, 60*1000),
        THREE_MINUTE(4, 3*60*1000), TEN_MINUTE(5, 10*60*1000), THIRTY_MINUTE(6, 30*60*1000),ONE_HOUR(7, 60*60*1000), THREE_HOUR(8, 3*60*60*1000);

        private final int code;


        private final long symbol;

        public long getTime() {
            return symbol;
        }

        TimeInterval(int code, long symbol) {
            this.code = code;
            this.symbol = symbol;
        }

        public static TimeInterval fromCode(int code) {
            for (TimeInterval timeInterval : values()) {
                if (timeInterval.code == code) {
                    return timeInterval;
                }
            }
            return null;
        }
    }

    enum SaveMode {
        FULL_WHEN_STOP(0),
        CIRCULAR_SAVE(1);
        private final int code;

        SaveMode(int code) {
            this.code = code;
        }

        public static SaveMode fromCode(int code) {
            for (SaveMode saveMode : values()) {
                if (saveMode.code == code) {
                    return saveMode;
                }
            }
            return null;
        }
    }


    enum SaveType {
        WAV(0),
        CSV(1),
        BIN(2),
        PICTURE(3),
        SESSION(4);
        private final int code;

        SaveType(int code) {
            this.code = code;
        }

        public static SaveType fromCode(int code) {
            for (SaveType saveType : values()) {
                if (saveType.code == code) {
                    return saveType;
                }
            }
            return null;
        }
        int getCode(){
            return code;
        }
    }

    public static final class AutoSaveTaskConditionBuilder {
        private List<SaveType> saveTypes;
        private String savePath;
        private String saveFileName;
        private LocalDateTime startTime;
        private TimeInterval intervalTime;
        private SaveMode saveMode;
        private StopCondition stopCondition;

        private List<Integer> selectedChannel;

        private String suffixNum;
        public AutoSaveTaskConditionBuilder() {
        }

        public static AutoSaveTaskConditionBuilder anAutoSaveTaskCondition() {
            return new AutoSaveTaskConditionBuilder();
        }

        public AutoSaveTaskConditionBuilder withSaveChannels(List<Integer> saveChannels) {
            this.selectedChannel = saveChannels;
            return this;
        }
        public AutoSaveTaskConditionBuilder withSaveType(List<SaveType> saveType) {
            this.saveTypes = saveType;
            return this;
        }

        public AutoSaveTaskConditionBuilder withSavePath(String savePath) {
            this.savePath = savePath;
            return this;
        }

        public AutoSaveTaskConditionBuilder withSaveFileName(String saveFileName) {
            this.saveFileName = saveFileName;
            return this;
        }

        public AutoSaveTaskConditionBuilder withStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public AutoSaveTaskConditionBuilder withIntervalTime(TimeInterval intervalTime) {
            this.intervalTime = intervalTime;
            return this;
        }

        public AutoSaveTaskConditionBuilder withSaveMode(SaveMode saveMode) {
            this.saveMode = saveMode;
            return this;
        }

        public AutoSaveTaskConditionBuilder withStopCondition(StopCondition.StopConditionBuilder stopConditionBuilder) {
            this.stopCondition = stopConditionBuilder.build();
            return this;
        }

        public AutoSaveTaskConditionBuilder withSuffixNum(String suffixNum) {
            this.suffixNum = suffixNum;
            return this;
        }

        public AutoSaveTaskCondition build() {
            AutoSaveTaskCondition autoSaveTaskCondition = new AutoSaveTaskCondition();
            autoSaveTaskCondition.intervalTime = this.intervalTime;
            autoSaveTaskCondition.saveType = this.saveTypes;
            autoSaveTaskCondition.saveMode = this.saveMode;
            autoSaveTaskCondition.savePath = this.savePath;
            autoSaveTaskCondition.startTime = this.startTime;
            autoSaveTaskCondition.stopCondition = this.stopCondition;
            autoSaveTaskCondition.saveFileName = this.saveFileName;
            autoSaveTaskCondition.selectedChannel = this.selectedChannel;
            autoSaveTaskCondition.suffixNum = this.suffixNum;
            return autoSaveTaskCondition;
        }
    }

    public List<SaveType> getSaveType() {
        return saveType;
    }

    public String getSavePath() {
        return savePath;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public TimeInterval getIntervalTime() {
        return intervalTime;
    }

    public SaveMode getSaveMode() {
        return saveMode;
    }

    public String getSuffixNum() {
        return suffixNum;
    }


    public List<Integer> getSelectedChannel() {
        return selectedChannel;
    }

    public StopCondition getStopCondition() {
        return stopCondition;
    }
}



