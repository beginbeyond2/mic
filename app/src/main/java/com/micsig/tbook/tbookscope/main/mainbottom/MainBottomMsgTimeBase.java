package com.micsig.tbook.tbookscope.main.mainbottom;

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                   MainBottomMsgTimeBase - 时基消息类                            │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                   │
 * │   底部时基控制栏的消息数据载体，用于传递时基档位信息和类型                         │
 * │                                                                              │
 * │ 【核心职责】                                                                   │
 * │   1. 存储时基类型（普通/FFT/参考）                                              │
 * │   2. 存储时基档位字符串值                                                      │
 * │   3. 标识消息来源是否为EventBus                                                │
 * │   4. 提供完整的getter/setter方法链                                             │
 * │                                                                              │
 * │ 【架构设计】                                                                   │
 * │   数据模型类，采用三个字段存储时基相关信息                                        │
 * │   支持三种时基类型：TYPE_NORMAL(普通YT模式)、TYPE_FFT(FFT模式)、TYPE_REF(参考)   │
 * │                                                                              │
 * │ 【数据流向】                                                                   │
 * │   MainHolderBottom → MainBottomMsgTimeBase → RxBus → 时基相关组件              │
 * │   用于同步时基档位状态到各订阅者                                                │
 * │                                                                              │
 * │ 【依赖关系】                                                                   │
 * │   被依赖：MainHolderBottom、时基显示组件                                        │
 * │   依赖：无                                                                    │
 * │                                                                              │
 * │ 【使用场景】                                                                   │
 * │   1. 时基档位切换时通知相关组件                                                │
 * │   2. FFT模式时基设置                                                          │
 * │   3. 参考通道时基同步                                                         │
 * │   4. 工作模式切换时同步时基信息                                                │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * Created by yangj on 2017/5/25.
 */

public class MainBottomMsgTimeBase {
    
    /** 普通时基类型常量，用于YT模式的时基设置 */ // 普通YT模式时基类型
    public static final int TYPE_NORMAL = 0;
    
    /** FFT时基类型常量，用于FFT模式的时基设置 */ // FFT模式时基类型
    public static final int TYPE_FFT = 1;
    
    /** 参考时基类型常量，用于参考通道的时基设置 */ // 参考通道时基类型
    public static final int TYPE_REF = 2;

    /** 消息来源标识，true表示来自EventBus，false表示来自其他源 */ // EventBus来源标识
    private boolean isFromEventBus;
    
    /** 时基类型，取值为TYPE_NORMAL、TYPE_FFT或TYPE_REF */ // 时基类型
    private int type;
    
    /** 时基档位字符串值，如"1ms"、"500ns"等 */ // 时基档位值
    private String timeBase;

    /**
     * 默认构造函数，初始化为普通时基类型和空时基值
     */
    public MainBottomMsgTimeBase() {
        timeBase = ""; // 初始化时基值为空字符串
        type = TYPE_NORMAL; // 初始化类型为普通时基
    }

    /**
     * 判断消息是否来自EventBus
     * 
     * @return boolean 来源标识，true表示来自EventBus，false表示其他源
     */
    public boolean isFromEventBus() {
        return isFromEventBus; // 返回EventBus来源标识
    }

    /**
     * 设置消息来源标识
     * 
     * @param fromEventBus 来源标识，true表示来自EventBus，false表示其他源
     */
    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus; // 设置EventBus来源标识
    }

    /**
     * 获取时基类型
     * 
     * @return int 时基类型，值为TYPE_NORMAL、TYPE_FFT或TYPE_REF
     */
    public int getType() {
        return type; // 返回时基类型
    }

    /**
     * 设置时基类型
     * 
     * @param type 时基类型，取值为TYPE_NORMAL、TYPE_FFT或TYPE_REF
     */
    public void setType(int type) {
        this.type = type; // 设置时基类型
    }

    /**
     * 获取时基档位字符串值
     * 
     * @return String 时基档位值，如"1ms"、"500ns"、"100Hz"等
     */
    public String getTimeBase() {
        return timeBase; // 返回时基档位值
    }

    /**
     * 设置时基档位字符串值
     * 
     * @param timeBase 时基档位值，普通模式为时间单位，FFT模式为频率单位
     */
    public void setTimeBase(String timeBase) {
        this.timeBase = timeBase; // 设置时基档位值
    }

    /**
     * 获取对象的字符串表示，用于调试和日志输出
     * 
     * @return String 包含类型和时基值的字符串描述
     */
    @Override
    public String toString() {
        return "MainBottomMsgTimeBase{" + // 构建toString字符串起始
                "type=" + type + // 添加类型信息
                ", timeBase='" + timeBase + '\'' + // 添加时基值信息
                '}'; // 结束toString字符串
    }
}