package com.micsig.tbook.tbookscope.main.maincenter; // 定义包路径,位于主界面中心区域的消息中心模块

/*
 * ========================================================================================
 *                                                                                  *
 *                     MainMsgCenterSegmented                                            *
 *                         分段采样消息数据类                                            *
 *                                                                                  *
 * ========================================================================================
 * 
 * 【模块定位】
 *   位于main.maincenter包中,作为示波器分段采样功能的消息载体,负责在分段采样模式下
 *   传递各种状态和参数信息,是分段采样功能的数据传输核心类
 * 
 * 【核心职责】
 *   1. 封装分段采样显示模式(单帧/拟合)
 *   2. 封装播放状态和速度参数
 *   3. 封装单帧/拟合起止帧信息
 *   4. 实现Cloneable接口支持对象克隆
 *   5. 提供统一的选择状态管理机制
 * 
 * 【架构设计】
 *   采用数据封装模式,所有属性均使用响应式包装类(RxBooleanWithSelect/RxIntWithSelect),
 *   支持选择状态标记,便于UI界面追踪数据变化。实现了Cloneable接口,支持深拷贝,
 *   方便创建消息副本。采用Builder模式风格,setter方法返回void但内部维护状态一致性。
 * 
 * 【数据流向】
 *   底层分段采样模块 → MainMsgCenterSegmented → UI显示层
 *   用户操作 → MainMsgCenterSegmented更新 → 通知观察者 → UI刷新
 *   数据流向为双向:既可接收底层采样数据,也可接收用户操作并传递到底层
 * 
 * 【依赖关系】
 *   依赖: SegmentedSingleBean(分段单帧数据)、RxBooleanWithSelect(响应式布尔)、RxIntWithSelect(响应式整型)
 *   被依赖: MainLayoutCenterSegmented、分段采样相关UI组件
 * 
 * 【使用场景】
 *   1. 用户切换分段采样显示模式(单帧/拟合)时传递状态
 *   2. 用户播放分段采样数据时传递播放状态和速度
 *   3. 用户选择单帧或设置拟合范围时传递帧信息
 *   4. 系统需要创建分段采样参数副本时进行对象克隆
 */

import com.micsig.tbook.scope.channel.SegmentedSingleBean; // 导入分段单帧数据Bean
import com.micsig.tbook.ui.bean.RxBooleanWithSelect; // 导入可选择的响应式布尔包装类
import com.micsig.tbook.ui.bean.RxIntWithSelect; // 导入可选择的响应式整型包装类


/**
 * 分段采样消息中心类
 * 
 * 用于封装分段采样功能的所有参数和状态,包括显示模式、播放状态、播放速度、
 * 帧数统计、单帧序号/大图标记以及单帧/拟合的起止帧信息。
 * 所有属性均采用响应式包装类,支持选择状态标记,便于UI追踪数据变化。
 * 
 * @author micsig
 * @version 1.0
 */
public class MainMsgCenterSegmented implements Cloneable {
    
    /** 显示模式: true为单帧模式, false为拟合模式 */ // 单帧为true,拟合为false
    private RxBooleanWithSelect display;
    
    /** 播放状态: true为正在播放, false为停止 */ // 是否正在播放
    private RxBooleanWithSelect playing;
    
    /** 播放速度: 控制分段采样数据的播放速率 */
    private RxIntWithSelect playSpeed;
    
    /** 帧数统计: 当前分段采样数据的帧总数 */
    private RxIntWithSelect count;
    
    /** 单帧序号标记: 用于标记当前选中的单帧序号 */
    private RxBooleanWithSelect singleOrder;
    
    /** 单帧大图标记: 用于标记是否显示单帧大图 */
    private RxBooleanWithSelect singleLarge;
    
    /** 当前单帧数据: 存储当前选中或显示的单帧详细信息 */
    private SegmentedSingleBean curSingleFrame;
    
    /** 拟合起始帧: 存储拟合显示模式的起始帧数据 */
    private SegmentedSingleBean fitStart;
    
    /** 拟合结束帧: 存储拟合显示模式的结束帧数据 */
    private SegmentedSingleBean fitEnd;

    /**
     * 获取显示模式
     * 
     * @return 显示模式的响应式布尔值, true表示单帧模式, false表示拟合模式
     */
    public RxBooleanWithSelect isDisplay() {
        return display; // 返回显示模式的响应式包装对象
    }

    /**
     * 设置显示模式
     * 
     * 如果当前显示模式对象为null,则创建新的RxBooleanWithSelect对象;
     * 否则更新现有对象的值,并重置所有选择状态后标记当前属性为已选中。
     * 
     * @param display 显示模式值, true为单帧模式, false为拟合模式
     */
    public void setDisplay(boolean display) {
        if (this.display == null) { // 判断显示模式对象是否为null
            this.display = new RxBooleanWithSelect(display); // 创建新的响应式布尔对象
        } else { // 显示模式对象已存在
            this.display.setValue(display); // 更新显示模式值
            setAllUnSelect(); // 重置所有属性的选择状态为false
            this.display.setRxMsgSelect(true); // 标记显示模式属性为已选中
        }
    }

    /**
     * 获取播放状态
     * 
     * @return 播放状态的响应式布尔值, true表示正在播放, false表示停止
     */
    public RxBooleanWithSelect isPlaying() {
        return playing; // 返回播放状态的响应式包装对象
    }

    /**
     * 设置播放状态
     * 
     * 如果当前播放状态对象为null,则创建新的RxBooleanWithSelect对象;
     * 否则更新现有对象的值,并重置所有选择状态后标记当前属性为已选中。
     * 
     * @param playing 播放状态值, true为正在播放, false为停止
     */
    public void setPlaying(boolean playing) {
        if (this.playing == null) { // 判断播放状态对象是否为null
            this.playing = new RxBooleanWithSelect(playing); // 创建新的响应式布尔对象
        } else { // 播放状态对象已存在
            this.playing.setValue(playing); // 更新播放状态值
            setAllUnSelect(); // 重置所有属性的选择状态为false
            this.playing.setRxMsgSelect(true); // 标记播放状态属性为已选中
        }
    }

    /**
     * 获取播放速度
     * 
     * @return 播放速度的响应式整型值
     */
    public RxIntWithSelect getPlaySpeed() {
        return playSpeed; // 返回播放速度的响应式包装对象
    }

    /**
     * 设置播放速度
     * 
     * 如果当前播放速度对象为null,则创建新的RxIntWithSelect对象;
     * 否则更新现有对象的值,并重置所有选择状态后标记当前属性为已选中。
     * 
     * @param playSpeed 播放速度值
     */
    public void setPlaySpeed(int playSpeed) {
        if (this.playSpeed == null) { // 判断播放速度对象是否为null
            this.playSpeed = new RxIntWithSelect(playSpeed); // 创建新的响应式整型对象
        } else { // 播放速度对象已存在
            this.playSpeed.setValue(playSpeed); // 更新播放速度值
            setAllUnSelect(); // 重置所有属性的选择状态为false
            this.playSpeed.setRxMsgSelect(true); // 标记播放速度属性为已选中
        }
    }

    /**
     * 获取帧数统计
     * 
     * @return 帧数统计的响应式整型值
     */
    public RxIntWithSelect getCount() {
        return count; // 返回帧数统计的响应式包装对象
    }

    /**
     * 设置帧数统计
     * 
     * 如果当前帧数统计对象为null,则创建新的RxIntWithSelect对象;
     * 否则更新现有对象的值,并重置所有选择状态后标记当前属性为已选中。
     * 
     * @param count 帧数统计值
     */
    public void setCount(int count) {
        if (this.count == null) { // 判断帧数统计对象是否为null
            this.count = new RxIntWithSelect(count); // 创建新的响应式整型对象
        } else { // 帧数统计对象已存在
            this.count.setValue(count); // 更新帧数统计值
            setAllUnSelect(); // 重置所有属性的选择状态为false
            this.count.setRxMsgSelect(true); // 标记帧数统计属性为已选中
        }
    }

    /**
     * 获取单帧序号标记
     * 
     * @return 单帧序号标记的响应式布尔值
     */
    public RxBooleanWithSelect isSingleOrder() {
        return singleOrder; // 返回单帧序号标记的响应式包装对象
    }

    /**
     * 设置单帧序号标记
     * 
     * 如果当前单帧序号标记对象为null,则创建新的RxBooleanWithSelect对象;
     * 否则更新现有对象的值,并重置所有选择状态后标记当前属性为已选中。
     * 
     * @param singleOrder 单帧序号标记值
     */
    public void setSingleOrder(boolean singleOrder) {
        if (this.singleOrder == null) { // 判断单帧序号标记对象是否为null
            this.singleOrder = new RxBooleanWithSelect(singleOrder); // 创建新的响应式布尔对象
        } else { // 单帧序号标记对象已存在
            this.singleOrder.setValue(singleOrder); // 更新单帧序号标记值
            setAllUnSelect(); // 重置所有属性的选择状态为false
            this.singleOrder.setRxMsgSelect(true); // 标记单帧序号标记属性为已选中
        }
    }

    /**
     * 获取单帧大图标记
     * 
     * @return 单帧大图标记的响应式布尔值
     */
    public RxBooleanWithSelect isSingleLarge() {
        return singleLarge; // 返回单帧大图标记的响应式包装对象
    }

    /**
     * 设置单帧大图标记
     * 
     * 如果当前单帧大图标记对象为null,则创建新的RxBooleanWithSelect对象;
     * 否则更新现有对象的值,并重置所有选择状态后标记当前属性为已选中。
     * 
     * @param singleLarge 单帧大图标记值
     */
    public void setSingleLarge(boolean singleLarge) {
        if (this.singleLarge == null) { // 判断单帧大图标记对象是否为null
            this.singleLarge = new RxBooleanWithSelect(singleLarge); // 创建新的响应式布尔对象
        } else { // 单帧大图标记对象已存在
            this.singleLarge.setValue(singleLarge); // 更新单帧大图标记值
            setAllUnSelect(); // 重置所有属性的选择状态为false
            this.singleLarge.setRxMsgSelect(true); // 标记单帧大图标记属性为已选中
        }
    }

    /**
     * 获取当前单帧数据
     * 
     * @return 当前单帧数据对象, 包含单帧的详细信息
     */
    public SegmentedSingleBean getCurSingleFrame() {
        return curSingleFrame; // 返回当前单帧数据对象
    }

    /**
     * 设置当前单帧数据
     * 
     * 如果当前单帧数据对象为null,则直接赋值;
     * 否则更新现有对象,并重置所有选择状态后标记当前属性为已选中。
     * 
     * @param curSingleFrame 当前单帧数据对象
     */
    public void setCurSingleFrame(SegmentedSingleBean curSingleFrame) {
        if (this.curSingleFrame == null) { // 判断当前单帧数据对象是否为null
            this.curSingleFrame = curSingleFrame; // 直接赋值新的单帧数据对象
        } else { // 当前单帧数据对象已存在
            this.curSingleFrame = curSingleFrame; // 更新单帧数据对象
            setAllUnSelect(); // 重置所有属性的选择状态为false
            this.curSingleFrame.setRxMsgSelect(true); // 标记当前单帧数据属性为已选中
        }
    }

    /**
     * 获取拟合起始帧数据
     * 
     * @return 拟合起始帧数据对象
     */
    public SegmentedSingleBean getFitStart() {
        return fitStart; // 返回拟合起始帧数据对象
    }

    /**
     * 设置拟合起始帧数据
     * 
     * 如果当前拟合起始帧对象为null,则直接赋值;
     * 否则更新现有对象,并重置所有选择状态后标记当前属性为已选中。
     * 
     * @param fitStart 拟合起始帧数据对象
     */
    public void setFitStart(SegmentedSingleBean fitStart) {
        if (this.fitStart == null) { // 判断拟合起始帧对象是否为null
            this.fitStart = fitStart; // 直接赋值新的拟合起始帧对象
        } else { // 拟合起始帧对象已存在
            this.fitStart = fitStart; // 更新拟合起始帧对象
            setAllUnSelect(); // 重置所有属性的选择状态为false
            this.fitStart.setRxMsgSelect(true); // 标记拟合起始帧属性为已选中
        }
    }

    /**
     * 获取拟合结束帧数据
     * 
     * @return 拟合结束帧数据对象
     */
    public SegmentedSingleBean getFitEnd() {
        return fitEnd; // 返回拟合结束帧数据对象
    }

    /**
     * 设置拟合结束帧数据
     * 
     * 如果当前拟合结束帧对象为null,则直接赋值;
     * 否则更新现有对象,并重置所有选择状态后标记当前属性为已选中。
     * 
     * @param fitEnd 拟合结束帧数据对象
     */
    public void setFitEnd(SegmentedSingleBean fitEnd) {
        if (this.fitEnd == null) { // 判断拟合结束帧对象是否为null
            this.fitEnd = fitEnd; // 直接赋值新的拟合结束帧对象
        } else { // 拟合结束帧对象已存在
            this.fitEnd = fitEnd; // 更新拟合结束帧对象
            setAllUnSelect(); // 重置所有属性的选择状态为false
            this.fitEnd.setRxMsgSelect(true); // 标记拟合结束帧属性为已选中
        }
    }

    /**
     * 重置所有属性的选择状态为false
     * 
     * 将所有响应式包装类的选择状态标记为false,用于在更新某个属性时,
     * 先清除所有选择标记,然后单独标记当前更新的属性为已选中。
     * 这样UI层可以根据选择状态判断哪些属性发生了变化。
     */
    private void setAllUnSelect() {
        display.setRxMsgSelect(false); // 重置显示模式的选择状态
        playing.setRxMsgSelect(false); // 重置播放状态的选择状态
        playSpeed.setRxMsgSelect(false); // 重置播放速度的选择状态
        count.setRxMsgSelect(false); // 重置帧数统计的选择状态
        singleOrder.setRxMsgSelect(false); // 重置单帧序号标记的选择状态
        singleLarge.setRxMsgSelect(false); // 重置单帧大图标记的选择状态
        if (curSingleFrame != null) { // 判断当前单帧数据对象是否为null
            curSingleFrame.setRxMsgSelect(false); // 重置当前单帧数据的选择状态
        }
        if (fitStart != null) { // 判断拟合起始帧对象是否为null
            fitStart.setRxMsgSelect(false); // 重置拟合起始帧的选择状态
        }
        if (fitEnd != null) { // 判断拟合结束帧对象是否为null
            fitEnd.setRxMsgSelect(false); // 重置拟合结束帧的选择状态
        }
    }

    /**
     * 克隆当前对象
     * 
     * 实现Cloneable接口,支持对象克隆功能,创建当前对象的浅拷贝副本。
     * 
     * @return 当前对象的克隆副本, 如果克隆失败则返回null
     */
    @Override
    public Object clone() {
        try { // 尝试克隆对象
            return super.clone(); // 调用父类的clone方法进行浅拷贝
        } catch (CloneNotSupportedException e) { // 捕获克隆不支持异常
            e.printStackTrace(); // 打印异常堆栈信息
        }
        return null; // 克隆失败时返回null
    }

    /**
     * 返回对象的字符串表示
     * 
     * 将所有属性值拼接为字符串格式,便于调试和日志输出。
     * 
     * @return 包含所有属性值的字符串表示
     */
    @Override
    public String toString() {
        return "MainMsgCenterSegmented{" + // 类名前缀
                "display=" + display + // 显示模式属性
                ", playing=" + playing + // 播放状态属性
                ", playSpeed=" + playSpeed + // 播放速度属性
                ", count=" + count + // 帧数统计属性
                ", singleOrder=" + singleOrder + // 单帧序号标记属性
                ", singleLarge=" + singleLarge + // 单帧大图标记属性
                ", curSingleFrame=" + curSingleFrame + // 当前单帧数据属性
                ", fitStart=" + fitStart + // 拟合起始帧属性
                ", fitEnd=" + fitEnd + // 拟合结束帧属性
                '}'; // 类名后缀
    }
}