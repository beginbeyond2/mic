package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

import java.time.LocalDateTime; // 导入本地日期时间类
import java.util.List; // 导入列表接口

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: 保存功能模块 - 自动保存任务条件配置                         ║
 * ║  核心职责: 封装自动保存任务的全部配置参数（保存类型/路径/时间/模式等）    ║
 * ║  架构设计: 值对象模式 + Builder构建器模式，包含三个内部枚举             ║
 * ║  数据流向: UI配置 → AutoSaveTaskConditionBuilder → AutoSaveTaskCondition║
 * ║           → AutoSaveTaskManager                                      ║
 * ║  依赖关系: 依赖 StopCondition，被 AutoSaveTaskManager 使用             ║
 * ║  使用场景: 用户配置自动保存参数时，通过Builder构建条件对象传入管理器     ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */

/**
 * 自动保存任务条件配置类
 * <p>封装自动保存任务的所有配置参数，包括保存类型、路径、文件名、时间间隔、保存模式、停止条件等</p>
 * <p>使用Builder模式构建，确保参数完整性和不可变性</p>
 */
public class AutoSaveTaskCondition { // 自动保存任务条件配置类

    /** 保存类型列表（WAV/CSV/BIN/PICTURE/SESSION的组合） */
    private List<SaveType> saveType; // 保存类型列表

    /** 保存路径和文件名 */
    private String savePath, saveFileName; // 保存目录路径和文件名

    /** 任务开始时间 */
    private LocalDateTime startTime; // 自动保存任务的开始时间

    /** 保存时间间隔，默认1毫秒 */
    private TimeInterval intervalTime = TimeInterval.ONE_MILLISECONDS; // 保存间隔时间，默认1毫秒

    /** 保存模式，默认循环保存 */
    private SaveMode saveMode = SaveMode.CIRCULAR_SAVE; // 保存模式，默认循环保存


    /** 停止条件 */
    private StopCondition stopCondition; // 停止条件配置

    /** 文件名后缀序号 */
    private String  suffixNum; // 文件名后缀序号

    /** 选中的通道列表 */
    private  List<Integer> selectedChannel; // 选中的通道索引列表

    /**
     * 时间间隔枚举
     * <p>定义自动保存任务支持的时间间隔选项，从1毫秒到3小时</p>
     */
    public enum TimeInterval { // 时间间隔枚举
         ONE_MILLISECONDS(0,100),TEN_SECOND(1, 10*1000), THIRTY_SECOND(2, 30*1000), ONE_MINUTE(3, 60*1000), // 1毫秒、10秒、30秒、1分钟
        THREE_MINUTE(4, 3*60*1000), TEN_MINUTE(5, 10*60*1000), THIRTY_MINUTE(6, 30*60*1000),ONE_HOUR(7, 60*60*1000), THREE_HOUR(8, 3*60*60*1000); // 3分钟、10分钟、30分钟、1小时、3小时

        /** 类型编码 */
        private final int code; // 类型编码

        /** 时间间隔值（毫秒） */
        private final long symbol; // 时间间隔值（毫秒）

        /**
         * 获取时间间隔值（毫秒）
         * @return 时间间隔值（毫秒）
         */
        public long getTime() { // 获取时间间隔毫秒值
            return symbol; // 返回时间间隔值
        }

        /**
         * 枚举构造方法
         * @param code 类型编码
         * @param symbol 时间间隔值（毫秒）
         */
        TimeInterval(int code, long symbol) { // 枚举构造方法
            this.code = code; // 赋值类型编码
            this.symbol = symbol; // 赋值时间间隔值
        }

        /**
         * 根据编码值获取对应的时间间隔枚举
         * @param code 类型编码值
         * @return 对应的TimeInterval，未匹配则返回null
         */
        public static TimeInterval fromCode(int code) { // 根据编码获取枚举实例
            for (TimeInterval timeInterval : values()) { // 遍历所有枚举值
                if (timeInterval.code == code) { // 比较编码值
                    return timeInterval; // 返回匹配的枚举实例
                }
            }
            return null; // 未找到匹配则返回null
        }
    }

    /**
     * 保存模式枚举
     * <p>定义自动保存的两种模式：停机时全量保存、循环保存</p>
     */
    enum SaveMode { // 保存模式枚举
        FULL_WHEN_STOP(0), // 停机时全量保存
        CIRCULAR_SAVE(1); // 循环保存
        /** 类型编码 */
        private final int code; // 类型编码

        /**
         * 枚举构造方法
         * @param code 类型编码
         */
        SaveMode(int code) { // 枚举构造方法
            this.code = code; // 赋值类型编码
        }

        /**
         * 根据编码值获取对应的保存模式枚举
         * @param code 类型编码值
         * @return 对应的SaveMode，未匹配则返回null
         */
        public static SaveMode fromCode(int code) { // 根据编码获取枚举实例
            for (SaveMode saveMode : values()) { // 遍历所有枚举值
                if (saveMode.code == code) { // 比较编码值
                    return saveMode; // 返回匹配的枚举实例
                }
            }
            return null; // 未找到匹配则返回null
        }
    }


    /**
     * 保存类型枚举
     * <p>定义自动保存支持的文件类型：WAV、CSV、BIN、PICTURE、SESSION</p>
     */
    enum SaveType { // 保存类型枚举
        WAV(0), // WAV波形文件
        CSV(1), // CSV数据文件
        BIN(2), // BIN二进制文件
        PICTURE(3), // 截图文件
        SESSION(4); // 会话文件
        /** 类型编码 */
        private final int code; // 类型编码

        /**
         * 枚举构造方法
         * @param code 类型编码
         */
        SaveType(int code) { // 枚举构造方法
            this.code = code; // 赋值类型编码
        }

        /**
         * 根据编码值获取对应的保存类型枚举
         * @param code 类型编码值
         * @return 对应的SaveType，未匹配则返回null
         */
        public static SaveType fromCode(int code) { // 根据编码获取枚举实例
            for (SaveType saveType : values()) { // 遍历所有枚举值
                if (saveType.code == code) { // 比较编码值
                    return saveType; // 返回匹配的枚举实例
                }
            }
            return null; // 未找到匹配则返回null
        }

        /**
         * 获取保存类型编码
         * @return 类型编码值
         */
        int getCode(){ // 获取类型编码
            return code; // 返回编码值
        }
    }

    /**
     * 自动保存任务条件Builder
     * <p>使用Builder模式构建AutoSaveTaskCondition实例，支持链式调用</p>
     */
    public static final class AutoSaveTaskConditionBuilder { // 自动保存任务条件构建器
        /** 保存类型列表 */
        private List<SaveType> saveTypes; // 保存类型列表
        /** 保存路径 */
        private String savePath; // 保存目录路径
        /** 保存文件名 */
        private String saveFileName; // 保存文件名
        /** 开始时间 */
        private LocalDateTime startTime; // 任务开始时间
        /** 时间间隔 */
        private TimeInterval intervalTime; // 保存间隔时间
        /** 保存模式 */
        private SaveMode saveMode; // 保存模式
        /** 停止条件 */
        private StopCondition stopCondition; // 停止条件

        /** 选中的通道列表 */
        private List<Integer> selectedChannel; // 选中的通道索引列表

        /** 后缀序号 */
        private String suffixNum; // 文件名后缀序号

        /**
         * Builder构造方法
         */
        public AutoSaveTaskConditionBuilder() { // Builder无参构造方法
        }

        /**
         * 创建Builder实例的静态工厂方法
         * @return 新的AutoSaveTaskConditionBuilder实例
         */
        public static AutoSaveTaskConditionBuilder anAutoSaveTaskCondition() { // 静态工厂方法创建Builder
            return new AutoSaveTaskConditionBuilder(); // 返回新Builder实例
        }

        /**
         * 设置保存通道列表
         * @param saveChannels 保存通道列表
         * @return Builder自身，支持链式调用
         */
        public AutoSaveTaskConditionBuilder withSaveChannels(List<Integer> saveChannels) { // 设置保存通道
            this.selectedChannel = saveChannels; // 赋值通道列表
            return this; // 返回Builder自身
        }

        /**
         * 设置保存类型列表
         * @param saveType 保存类型列表
         * @return Builder自身，支持链式调用
         */
        public AutoSaveTaskConditionBuilder withSaveType(List<SaveType> saveType) { // 设置保存类型
            this.saveTypes = saveType; // 赋值保存类型列表
            return this; // 返回Builder自身
        }

        /**
         * 设置保存路径
         * @param savePath 保存目录路径
         * @return Builder自身，支持链式调用
         */
        public AutoSaveTaskConditionBuilder withSavePath(String savePath) { // 设置保存路径
            this.savePath = savePath; // 赋值保存路径
            return this; // 返回Builder自身
        }

        /**
         * 设置保存文件名
         * @param saveFileName 保存文件名
         * @return Builder自身，支持链式调用
         */
        public AutoSaveTaskConditionBuilder withSaveFileName(String saveFileName) { // 设置保存文件名
            this.saveFileName = saveFileName; // 赋值文件名
            return this; // 返回Builder自身
        }

        /**
         * 设置任务开始时间
         * @param startTime 开始时间
         * @return Builder自身，支持链式调用
         */
        public AutoSaveTaskConditionBuilder withStartTime(LocalDateTime startTime) { // 设置开始时间
            this.startTime = startTime; // 赋值开始时间
            return this; // 返回Builder自身
        }

        /**
         * 设置保存时间间隔
         * @param intervalTime 时间间隔枚举
         * @return Builder自身，支持链式调用
         */
        public AutoSaveTaskConditionBuilder withIntervalTime(TimeInterval intervalTime) { // 设置时间间隔
            this.intervalTime = intervalTime; // 赋值时间间隔
            return this; // 返回Builder自身
        }

        /**
         * 设置保存模式
         * @param saveMode 保存模式枚举
         * @return Builder自身，支持链式调用
         */
        public AutoSaveTaskConditionBuilder withSaveMode(SaveMode saveMode) { // 设置保存模式
            this.saveMode = saveMode; // 赋值保存模式
            return this; // 返回Builder自身
        }

        /**
         * 设置停止条件（通过Builder构建）
         * @param stopConditionBuilder 停止条件Builder
         * @return Builder自身，支持链式调用
         */
        public AutoSaveTaskConditionBuilder withStopCondition(StopCondition.StopConditionBuilder stopConditionBuilder) { // 设置停止条件
            this.stopCondition = stopConditionBuilder.build(); // 通过Builder构建停止条件并赋值
            return this; // 返回Builder自身
        }

        /**
         * 设置文件名后缀序号
         * @param suffixNum 后缀序号字符串
         * @return Builder自身，支持链式调用
         */
        public AutoSaveTaskConditionBuilder withSuffixNum(String suffixNum) { // 设置后缀序号
            this.suffixNum = suffixNum; // 赋值后缀序号
            return this; // 返回Builder自身
        }

        /**
         * 构建AutoSaveTaskCondition实例
         * @return 构建完成的AutoSaveTaskCondition对象
         */
        public AutoSaveTaskCondition build() { // 构建AutoSaveTaskCondition实例
            AutoSaveTaskCondition autoSaveTaskCondition = new AutoSaveTaskCondition(); // 创建新实例
            autoSaveTaskCondition.intervalTime = this.intervalTime; // 赋值时间间隔
            autoSaveTaskCondition.saveType = this.saveTypes; // 赋值保存类型
            autoSaveTaskCondition.saveMode = this.saveMode; // 赋值保存模式
            autoSaveTaskCondition.savePath = this.savePath; // 赋值保存路径
            autoSaveTaskCondition.startTime = this.startTime; // 赋值开始时间
            autoSaveTaskCondition.stopCondition = this.stopCondition; // 赋值停止条件
            autoSaveTaskCondition.saveFileName = this.saveFileName; // 赋值文件名
            autoSaveTaskCondition.selectedChannel = this.selectedChannel; // 赋值选中通道
            autoSaveTaskCondition.suffixNum = this.suffixNum; // 赋值后缀序号
            return autoSaveTaskCondition; // 返回构建完成的实例
        }
    }

    /**
     * 获取保存类型列表
     * @return 保存类型列表
     */
    public List<SaveType> getSaveType() { // 获取保存类型列表
        return saveType; // 返回保存类型列表
    }

    /**
     * 获取保存路径
     * @return 保存目录路径
     */
    public String getSavePath() { // 获取保存路径
        return savePath; // 返回保存路径
    }

    /**
     * 获取保存文件名
     * @return 保存文件名
     */
    public String getSaveFileName() { // 获取保存文件名
        return saveFileName; // 返回文件名
    }

    /**
     * 获取任务开始时间
     * @return 开始时间
     */
    public LocalDateTime getStartTime() { // 获取开始时间
        return startTime; // 返回开始时间
    }

    /**
     * 获取保存时间间隔
     * @return 时间间隔枚举
     */
    public TimeInterval getIntervalTime() { // 获取时间间隔
        return intervalTime; // 返回时间间隔
    }

    /**
     * 获取保存模式
     * @return 保存模式枚举
     */
    public SaveMode getSaveMode() { // 获取保存模式
        return saveMode; // 返回保存模式
    }

    /**
     * 获取文件名后缀序号
     * @return 后缀序号字符串
     */
    public String getSuffixNum() { // 获取后缀序号
        return suffixNum; // 返回后缀序号
    }


    /**
     * 获取选中的通道列表
     * @return 通道索引列表
     */
    public List<Integer> getSelectedChannel() { // 获取选中通道列表
        return selectedChannel; // 返回通道列表
    }

    /**
     * 获取停止条件
     * @return 停止条件对象
     */
    public StopCondition getStopCondition() { // 获取停止条件
        return stopCondition; // 返回停止条件
    }
}
