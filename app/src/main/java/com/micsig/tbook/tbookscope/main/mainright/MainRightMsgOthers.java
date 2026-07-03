package com.micsig.tbook.tbookscope.main.mainright;

import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/5/15.
 */

/*
+======================================================================================+
|                              MainRightMsgOthers 类说明                               |
+======================================================================================+
| 模块定位: 示波器右侧面板消息数据模型                                                      |
|                                                                                      |
| 核心职责:                                                                            |
|   1. 管理8个Math通道(math1-math8)的开关状态                                            |
|   2. 管理8个Ref通道(ref1-ref8)的开关状态                                               |
|   3. 管理4个串行通道(s1-s4)的开关状态                                                   |
|   4. 管理4个串行通道的绘制标志(s1Draw-s4Draw)                                           |
|   5. 提供通道状态的统一访问接口                                                         |
|                                                                                      |
| 架构设计:                                                                            |
|   - 数据模型层(Model): 纯数据存储类,不包含业务逻辑                                       |
|   - 使用RxBooleanWithSelect实现响应式数据绑定                                           |
|   - 提供统一的getter/setter方法进行数据访问                                             |
|   - 提供批量操作方法简化通道管理                                                         |
|                                                                                      |
| 数据流向:                                                                            |
|   外部调用 -> setMath/setRef/setSerials -> 更新内部状态 -> 触发UI更新                    |
|   外部调用 -> getMath/getRef/getSerial -> 读取内部状态 -> 返回数据                       |
|                                                                                      |
| 依赖关系:                                                                            |
|   - RxBooleanWithSelect: UI响应式数据包装类                                             |
|   - CacheUtil: 缓存工具类,提供通道常量定义                                               |
|   - TChan: 通道定义类,提供Math/Ref通道标识                                              |
|                                                                                      |
| 使用场景:                                                                            |
|   1. 右侧面板显示Math/Ref/S1-S4通道列表                                                 |
|   2. 用户点击通道开关时更新状态                                                         |
|   3. 通道选择状态管理(单选模式)                                                          |
|   4. 串行通道的绘制控制                                                                |
+======================================================================================+
*/
public class MainRightMsgOthers {
    private static final String TAG = MainRightMsgOthers.class.getSimpleName(); // 类标签,用于日志输出

    private RxBooleanWithSelect math1, math2, math3, math4, math5, math6, math7, math8; // Math1-Math8通道状态对象
    private RxBooleanWithSelect ref1, ref2, ref3, ref4, ref5, ref6, ref7, ref8; // Ref1-Ref8通道状态对象
    private RxBooleanWithSelect s1, s2, s3, s4; // S1-S4串行通道状态对象

    private boolean s1Draw, s2Draw, s3Draw, s4Draw; // S1-S4串行通道绘制标志

    /**
     * 设置串行通道的绘制标志
     *
     * @param serialNumber 串行通道编号(CacheUtil.S1/S2/S3/S4)
     * @param serialDraw 是否绘制
     */
    public void setSerialsDraw(int serialNumber, boolean serialDraw) {
        switch (serialNumber) {
            case CacheUtil.S1: // 如果是S1通道
                setS1Draw(serialDraw); // 设置S1绘制标志
                break;
            case CacheUtil.S2: // 如果是S2通道
                setS2Draw(serialDraw); // 设置S2绘制标志
                break;
            case CacheUtil.S3: // 如果是S3通道
                setS3Draw(serialDraw); // 设置S3绘制标志
                break;
            case CacheUtil.S4: // 如果是S4通道
                setS4Draw(serialDraw); // 设置S4绘制标志
                break;
        }
    }

    /**
     * 获取串行通道的绘制标志
     *
     * @param serialNumber 串行通道编号(CacheUtil.S1/S2/S3/S4)
     * @return 是否绘制
     */
    public boolean getSerialsDraw(int serialNumber) {
        switch (serialNumber) {
            case CacheUtil.S1: // 如果是S1通道
                return isS1Draw(); // 返回S1绘制标志
            case CacheUtil.S2: // 如果是S2通道
                return isS2Draw(); // 返回S2绘制标志
            case CacheUtil.S3: // 如果是S3通道
                return isS3Draw(); // 返回S3绘制标志
            case CacheUtil.S4: // 如果是S4通道
                return isS4Draw(); // 返回S4绘制标志
            default: // 默认情况
                return isS1Draw(); // 返回S1绘制标志
        }
    }

    /**
     * 获取串行通道状态对象
     *
     * @param serialNumber 串行通道编号(CacheUtil.S1/S2/S3/S4)
     * @return 串行通道状态对象
     */
    public RxBooleanWithSelect getSerial(int serialNumber) {
        switch (serialNumber) {
            case CacheUtil.S1: // 如果是S1通道
                return getS1(); // 返回S1状态对象
            case CacheUtil.S2: // 如果是S2通道
                return getS2(); // 返回S2状态对象
            case CacheUtil.S3: // 如果是S3通道
                return getS3(); // 返回S3状态对象
            case CacheUtil.S4: // 如果是S4通道
                return getS4(); // 返回S4状态对象
            default: // 默认情况
                return getS1(); // 返回S1状态对象
        }
    }

    /**
     * 设置串行通道的开关状态
     *
     * @param serialNumber 串行通道编号(CacheUtil.S1/S2/S3/S4)
     * @param serialCheck 是否开启
     */
    public void setSerials(int serialNumber, boolean serialCheck) {
        switch (serialNumber) {
            case CacheUtil.S1: // 如果是S1通道
                setS1(serialCheck); // 设置S1状态
                break;
            case CacheUtil.S2: // 如果是S2通道
                setS2(serialCheck); // 设置S2状态
                break;
            case CacheUtil.S3: // 如果是S3通道
                setS3(serialCheck); // 设置S3状态
                break;
            case CacheUtil.S4: // 如果是S4通道
                setS4(serialCheck); // 设置S4状态
                break;
            default: // 默认情况
                setS1(serialCheck); // 设置S1状态
                break;
        }
    }

    /**
     * 获取Ref通道状态对象
     *
     * @param refChannel Ref通道标识(TChan.R1-R8)
     * @return Ref通道状态对象
     */
    public RxBooleanWithSelect getRef(int refChannel) {
        switch (refChannel) {
            case TChan.R1: // 如果是R1通道
                return getRef1(); // 返回R1状态对象
            case TChan.R2: // 如果是R2通道
                return getRef2(); // 返回R2状态对象
            case TChan.R3: // 如果是R3通道
                return getRef3(); // 返回R3状态对象
            case TChan.R4: // 如果是R4通道
                return getRef4(); // 返回R4状态对象
            case TChan.R5: // 如果是R5通道
                return getRef5(); // 返回R5状态对象
            case TChan.R6: // 如果是R6通道
                return getRef6(); // 返回R6状态对象
            case TChan.R7: // 如果是R7通道
                return getRef7(); // 返回R7状态对象
            case TChan.R8: // 如果是R8通道
                return getRef8(); // 返回R8状态对象
            default: // 默认情况
                return getRef1(); // 返回R1状态对象
        }
    }

    /**
     * 设置Ref通道的开关状态
     *
     * @param recChannel Ref通道标识(TChan.R1-R8)
     * @param refCheck 是否开启
     */
    public void setRef(int recChannel, boolean refCheck) {
        switch (recChannel) {
            case TChan.R1: // 如果是R1通道
                setRef1(refCheck); // 设置R1状态
                break;
            case TChan.R2: // 如果是R2通道
                setRef2(refCheck); // 设置R2状态
                break;
            case TChan.R3: // 如果是R3通道
                setRef3(refCheck); // 设置R3状态
                break;
            case TChan.R4: // 如果是R4通道
                setRef4(refCheck); // 设置R4状态
                break;
            case TChan.R5: // 如果是R5通道
                setRef5(refCheck); // 设置R5状态
                break;
            case TChan.R6: // 如果是R6通道
                setRef6(refCheck); // 设置R6状态
                break;
            case TChan.R7: // 如果是R7通道
                setRef7(refCheck); // 设置R7状态
                break;
            case TChan.R8: // 如果是R8通道
                setRef8(refCheck); // 设置R8状态
                break;
        }
    }

    /**
     * 获取Math通道状态对象
     *
     * @param mathChannel Math通道标识(TChan.Math1-Math8)
     * @return Math通道状态对象
     */
    public RxBooleanWithSelect getMath(int mathChannel) {
        switch (mathChannel) {
            case TChan.Math1: // 如果是Math1通道
                return getMath1(); // 返回Math1状态对象
            case TChan.Math2: // 如果是Math2通道
                return getMath2(); // 返回Math2状态对象
            case TChan.Math3: // 如果是Math3通道
                return getMath3(); // 返回Math3状态对象
            case TChan.Math4: // 如果是Math4通道
                return getMath4(); // 返回Math4状态对象
            case TChan.Math5: // 如果是Math5通道
                return getMath5(); // 返回Math5状态对象
            case TChan.Math6: // 如果是Math6通道
                return getMath6(); // 返回Math6状态对象
            case TChan.Math7: // 如果是Math7通道
                return getMath7(); // 返回Math7状态对象
            case TChan.Math8: // 如果是Math8通道
                return getMath8(); // 返回Math8状态对象
            default: // 默认情况
                return getMath1(); // 返回Math1状态对象
        }
    }

    /**
     * 设置Math通道的开关状态
     *
     * @param mathChannel Math通道标识(TChan.Math1-Math8)
     * @param mathCheck 是否开启
     */
    public void setMath(int mathChannel, boolean mathCheck) {
        switch (mathChannel) {
            case TChan.Math1: // 如果是Math1通道
                setMath1(mathCheck); // 设置Math1状态
                break;
            case TChan.Math2: // 如果是Math2通道
                setMath2(mathCheck); // 设置Math2状态
                break;
            case TChan.Math3: // 如果是Math3通道
                setMath3(mathCheck); // 设置Math3状态
                break;
            case TChan.Math4: // 如果是Math4通道
                setMath4(mathCheck); // 设置Math4状态
                break;
            case TChan.Math5: // 如果是Math5通道
                setMath5(mathCheck); // 设置Math5状态
                break;
            case TChan.Math6: // 如果是Math6通道
                setMath6(mathCheck); // 设置Math6状态
                break;
            case TChan.Math7: // 如果是Math7通道
                setMath7(mathCheck); // 设置Math7状态
                break;
            case TChan.Math8: // 如果是Math8通道
                setMath8(mathCheck); // 设置Math8状态
                break;
            default: // 默认情况
                setMath1(mathCheck); // 设置Math1状态
                break;
        }
    }

    /**
     * 获取Math1通道状态对象
     *
     * @return Math1通道状态对象
     */
    public RxBooleanWithSelect getMath1() {
        return math1; // 返回Math1状态对象
    }

    /**
     * 设置Math1通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param math 是否开启
     */
    public void setMath1(boolean math) {
        if (this.math1 == null) { // 如果math1对象为空
            this.math1 = new RxBooleanWithSelect(math); // 创建新的RxBooleanWithSelect对象
        } else { // 如果math1对象不为空
            this.math1.setValue(math); // 设置Math1的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.math1.setRxMsgSelect(true); // 设置Math1为选中状态
        }
    }

    /**
     * 获取Math2通道状态对象
     *
     * @return Math2通道状态对象
     */
    public RxBooleanWithSelect getMath2() {
        return math2; // 返回Math2状态对象
    }

    /**
     * 设置Math2通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param math 是否开启
     */
    public void setMath2(boolean math) {
        if (this.math2 == null) { // 如果math2对象为空
            this.math2 = new RxBooleanWithSelect(math); // 创建新的RxBooleanWithSelect对象
        } else { // 如果math2对象不为空
            this.math2.setValue(math); // 设置Math2的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.math2.setRxMsgSelect(true); // 设置Math2为选中状态
        }
    }

    /**
     * 获取Math3通道状态对象
     *
     * @return Math3通道状态对象
     */
    public RxBooleanWithSelect getMath3() {
        return math3; // 返回Math3状态对象
    }

    /**
     * 设置Math3通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param math 是否开启
     */
    public void setMath3(boolean math) {
        if (this.math3 == null) { // 如果math3对象为空
            this.math3 = new RxBooleanWithSelect(math); // 创建新的RxBooleanWithSelect对象
        } else { // 如果math3对象不为空
            this.math3.setValue(math); // 设置Math3的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.math3.setRxMsgSelect(true); // 设置Math3为选中状态
        }
    }

    /**
     * 获取Math4通道状态对象
     *
     * @return Math4通道状态对象
     */
    public RxBooleanWithSelect getMath4() {
        return math4; // 返回Math4状态对象
    }

    /**
     * 设置Math4通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param math 是否开启
     */
    public void setMath4(boolean math) {
        if (this.math4 == null) { // 如果math4对象为空
            this.math4 = new RxBooleanWithSelect(math); // 创建新的RxBooleanWithSelect对象
        } else { // 如果math4对象不为空
            this.math4.setValue(math); // 设置Math4的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.math4.setRxMsgSelect(true); // 设置Math4为选中状态
        }
    }


    /**
     * 获取Math5通道状态对象
     *
     * @return Math5通道状态对象
     */
    public RxBooleanWithSelect getMath5() {
        return math5; // 返回Math5状态对象
    }

    /**
     * 设置Math5通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param math 是否开启
     */
    public void setMath5(boolean math) {
        if (this.math5 == null) { // 如果math5对象为空
            this.math5 = new RxBooleanWithSelect(math); // 创建新的RxBooleanWithSelect对象
        } else { // 如果math5对象不为空
            this.math5.setValue(math); // 设置Math5的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.math5.setRxMsgSelect(true); // 设置Math5为选中状态
        }
    }

    /**
     * 获取Math6通道状态对象
     *
     * @return Math6通道状态对象
     */
    public RxBooleanWithSelect getMath6() {
        return math6; // 返回Math6状态对象
    }

    /**
     * 设置Math6通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param math 是否开启
     */
    public void setMath6(boolean math) {
        if (this.math6 == null) { // 如果math6对象为空
            this.math6 = new RxBooleanWithSelect(math); // 创建新的RxBooleanWithSelect对象
        } else { // 如果math6对象不为空
            this.math6.setValue(math); // 设置Math6的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.math6.setRxMsgSelect(true); // 设置Math6为选中状态
        }
    }

    /**
     * 获取Math7通道状态对象
     *
     * @return Math7通道状态对象
     */
    public RxBooleanWithSelect getMath7() {
        return math7; // 返回Math7状态对象
    }

    /**
     * 设置Math7通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param math 是否开启
     */
    public void setMath7(boolean math) {
        if (this.math7 == null) { // 如果math7对象为空
            this.math7 = new RxBooleanWithSelect(math); // 创建新的RxBooleanWithSelect对象
        } else { // 如果math7对象不为空
            this.math7.setValue(math); // 设置Math7的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.math7.setRxMsgSelect(true); // 设置Math7为选中状态
        }
    }

    /**
     * 获取Math8通道状态对象
     *
     * @return Math8通道状态对象
     */
    public RxBooleanWithSelect getMath8() {
        return math8; // 返回Math8状态对象
    }

    /**
     * 设置Math8通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param math 是否开启
     */
    public void setMath8(boolean math) {
        if (this.math8 == null) { // 如果math8对象为空
            this.math8 = new RxBooleanWithSelect(math); // 创建新的RxBooleanWithSelect对象
        } else { // 如果math8对象不为空
            this.math8.setValue(math); // 设置Math8的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.math8.setRxMsgSelect(true); // 设置Math8为选中状态
        }
    }

    /**
     * 获取S1串行通道状态对象
     *
     * @return S1串行通道状态对象
     */
    public RxBooleanWithSelect getS1() {
        return s1; // 返回S1状态对象
    }

    /**
     * 设置S1串行通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param s1 是否开启
     */
    public void setS1(boolean s1) {
        if (this.s1 == null) { // 如果s1对象为空
            this.s1 = new RxBooleanWithSelect(s1); // 创建新的RxBooleanWithSelect对象
        } else { // 如果s1对象不为空
            this.s1.setValue(s1); // 设置S1的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.s1.setRxMsgSelect(true); // 设置S1为选中状态
        }
    }

    /**
     * 获取S2串行通道状态对象
     *
     * @return S2串行通道状态对象
     */
    public RxBooleanWithSelect getS2() {
        return s2; // 返回S2状态对象
    }

    /**
     * 设置S2串行通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param s2 是否开启
     */
    public void setS2(boolean s2) {
        if (this.s2 == null) { // 如果s2对象为空
            this.s2 = new RxBooleanWithSelect(s2); // 创建新的RxBooleanWithSelect对象
        } else { // 如果s2对象不为空
            this.s2.setValue(s2); // 设置S2的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.s2.setRxMsgSelect(true); // 设置S2为选中状态
        }
    }

    /**
     * 获取S3串行通道状态对象
     *
     * @return S3串行通道状态对象
     */
    public RxBooleanWithSelect getS3() {
        return s3; // 返回S3状态对象
    }

    /**
     * 设置S3串行通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param s3 是否开启
     */
    public void setS3(boolean s3) {
        if (this.s3 == null) { // 如果s3对象为空
            this.s3 = new RxBooleanWithSelect(s3); // 创建新的RxBooleanWithSelect对象
        } else { // 如果s3对象不为空
            this.s3.setValue(s3); // 设置S3的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.s3.setRxMsgSelect(true); // 设置S3为选中状态
        }
    }

    /**
     * 获取S4串行通道状态对象
     *
     * @return S4串行通道状态对象
     */
    public RxBooleanWithSelect getS4() {
        return s4; // 返回S4状态对象
    }

    /**
     * 设置S4串行通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param s4 是否开启
     */
    public void setS4(boolean s4) {
        if (this.s4 == null) { // 如果s4对象为空
            this.s4 = new RxBooleanWithSelect(s4); // 创建新的RxBooleanWithSelect对象
        } else { // 如果s4对象不为空
            this.s4.setValue(s4); // 设置S4的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.s4.setRxMsgSelect(true); // 设置S4为选中状态
        }
    }


    /**
     * 获取Ref1通道状态对象
     *
     * @return Ref1通道状态对象
     */
    public RxBooleanWithSelect getRef1() {
        return ref1; // 返回Ref1状态对象
    }

    /**
     * 设置Ref1通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param ref1 是否开启
     */
    public void setRef1(boolean ref1) {
        if (this.ref1 == null) { // 如果ref1对象为空
            this.ref1 = new RxBooleanWithSelect(ref1); // 创建新的RxBooleanWithSelect对象
        } else { // 如果ref1对象不为空
            this.ref1.setValue(ref1); // 设置Ref1的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.ref1.setRxMsgSelect(true); // 设置Ref1为选中状态
        }
    }

    /**
     * 获取Ref2通道状态对象
     *
     * @return Ref2通道状态对象
     */
    public RxBooleanWithSelect getRef2() {
        return ref2; // 返回Ref2状态对象
    }

    /**
     * 设置Ref2通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param ref2 是否开启
     */
    public void setRef2(boolean ref2) {
        if (this.ref2 == null) { // 如果ref2对象为空
            this.ref2 = new RxBooleanWithSelect(ref2); // 创建新的RxBooleanWithSelect对象
        } else { // 如果ref2对象不为空
            this.ref2.setValue(ref2); // 设置Ref2的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.ref2.setRxMsgSelect(true); // 设置Ref2为选中状态
        }
    }

    /**
     * 获取Ref3通道状态对象
     *
     * @return Ref3通道状态对象
     */
    public RxBooleanWithSelect getRef3() {
        return ref3; // 返回Ref3状态对象
    }

    /**
     * 设置Ref3通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param ref3 是否开启
     */
    public void setRef3(boolean ref3) {
        if (this.ref3 == null) { // 如果ref3对象为空
            this.ref3 = new RxBooleanWithSelect(ref3); // 创建新的RxBooleanWithSelect对象
        } else { // 如果ref3对象不为空
            this.ref3.setValue(ref3); // 设置Ref3的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.ref3.setRxMsgSelect(true); // 设置Ref3为选中状态
        }
    }

    /**
     * 获取Ref4通道状态对象
     *
     * @return Ref4通道状态对象
     */
    public RxBooleanWithSelect getRef4() {
        return ref4; // 返回Ref4状态对象
    }

    /**
     * 设置Ref4通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param ref4 是否开启
     */
    public void setRef4(boolean ref4) {
        if (this.ref4 == null) { // 如果ref4对象为空
            this.ref4 = new RxBooleanWithSelect(ref4); // 创建新的RxBooleanWithSelect对象
        } else { // 如果ref4对象不为空
            this.ref4.setValue(ref4); // 设置Ref4的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.ref4.setRxMsgSelect(true); // 设置Ref4为选中状态
        }
    }

    /**
     * 获取Ref5通道状态对象
     *
     * @return Ref5通道状态对象
     */
    public RxBooleanWithSelect getRef5() {
        return ref5; // 返回Ref5状态对象
    }

    /**
     * 设置Ref5通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param ref5 是否开启
     */
    public void setRef5(boolean ref5) {
        if (this.ref5 == null) { // 如果ref5对象为空
            this.ref5 = new RxBooleanWithSelect(ref5); // 创建新的RxBooleanWithSelect对象
        } else { // 如果ref5对象不为空
            this.ref5.setValue(ref5); // 设置Ref5的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.ref5.setRxMsgSelect(true); // 设置Ref5为选中状态
        }
    }

    /**
     * 获取Ref6通道状态对象
     *
     * @return Ref6通道状态对象
     */
    public RxBooleanWithSelect getRef6() {
        return ref6; // 返回Ref6状态对象
    }

    /**
     * 设置Ref6通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param ref6 是否开启
     */
    public void setRef6(boolean ref6) {
        if (this.ref6 == null) { // 如果ref6对象为空
            this.ref6 = new RxBooleanWithSelect(ref6); // 创建新的RxBooleanWithSelect对象
        } else { // 如果ref6对象不为空
            this.ref6.setValue(ref6); // 设置Ref6的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.ref6.setRxMsgSelect(true); // 设置Ref6为选中状态
        }
    }

    /**
     * 获取Ref7通道状态对象
     *
     * @return Ref7通道状态对象
     */
    public RxBooleanWithSelect getRef7() {
        return ref7; // 返回Ref7状态对象
    }

    /**
     * 设置Ref7通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param ref7 是否开启
     */
    public void setRef7(boolean ref7) {
        if (this.ref7 == null) { // 如果ref7对象为空
            this.ref7 = new RxBooleanWithSelect(ref7); // 创建新的RxBooleanWithSelect对象
        } else { // 如果ref7对象不为空
            this.ref7.setValue(ref7); // 设置Ref7的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.ref7.setRxMsgSelect(true); // 设置Ref7为选中状态
        }
    }

    /**
     * 获取Ref8通道状态对象
     *
     * @return Ref8通道状态对象
     */
    public RxBooleanWithSelect getRef8() {
        return ref8; // 返回Ref8状态对象
    }

    /**
     * 设置Ref8通道的开关状态
     * 采用单选模式:设置某个通道时,取消其他所有通道的选择状态
     *
     * @param ref8 是否开启
     */
    public void setRef8(boolean ref8) {
        if (this.ref8 == null) { // 如果ref8对象为空
            this.ref8 = new RxBooleanWithSelect(ref8); // 创建新的RxBooleanWithSelect对象
        } else { // 如果ref8对象不为空
            this.ref8.setValue(ref8); // 设置Ref8的值
            setAllUnSelect(); // 取消所有通道的选择状态
            this.ref8.setRxMsgSelect(true); // 设置Ref8为选中状态
        }
    }


    /**
     * 判断S1串行通道是否需要绘制
     *
     * @return true表示需要绘制,false表示不需要绘制
     */
    public boolean isS1Draw() {
        return s1Draw; // 返回S1绘制标志
    }

    /**
     * 设置S1串行通道的绘制标志
     *
     * @param s1Draw 是否绘制
     */
    public void setS1Draw(boolean s1Draw) {
        this.s1Draw = s1Draw; // 设置S1绘制标志
    }

    /**
     * 判断S2串行通道是否需要绘制
     *
     * @return true表示需要绘制,false表示不需要绘制
     */
    public boolean isS2Draw() {
        return s2Draw; // 返回S2绘制标志
    }

    /**
     * 设置S2串行通道的绘制标志
     *
     * @param s2Draw 是否绘制
     */
    public void setS2Draw(boolean s2Draw) {
        this.s2Draw = s2Draw; // 设置S2绘制标志
    }

    /**
     * 判断S3串行通道是否需要绘制
     *
     * @return true表示需要绘制,false表示不需要绘制
     */
    public boolean isS3Draw() {
        return s3Draw; // 返回S3绘制标志
    }

    /**
     * 设置S3串行通道的绘制标志
     *
     * @param s3Draw 是否绘制
     */
    public void setS3Draw(boolean s3Draw) {
        this.s3Draw = s3Draw; // 设置S3绘制标志
    }

    /**
     * 判断S4串行通道是否需要绘制
     *
     * @return true表示需要绘制,false表示不需要绘制
     */
    public boolean isS4Draw() {
        return s4Draw; // 返回S4绘制标志
    }

    /**
     * 设置S4串行通道的绘制标志
     *
     * @param s4Draw 是否绘制
     */
    public void setS4Draw(boolean s4Draw) {
        this.s4Draw = s4Draw; // 设置S4绘制标志
    }

    /**
     * 取消所有通道的选择状态
     * 用于实现单选模式:当选择某个通道时,先取消所有通道的选择,再选中当前通道
     */
    public void setAllUnSelect() {
        math1.setRxMsgSelect(false); // 取消Math1的选择状态
        math2.setRxMsgSelect(false); // 取消Math2的选择状态
        math3.setRxMsgSelect(false); // 取消Math3的选择状态
        math4.setRxMsgSelect(false); // 取消Math4的选择状态
        math5.setRxMsgSelect(false); // 取消Math5的选择状态
        math6.setRxMsgSelect(false); // 取消Math6的选择状态
        math7.setRxMsgSelect(false); // 取消Math7的选择状态
        math8.setRxMsgSelect(false); // 取消Math8的选择状态
        ref1.setRxMsgSelect(false); // 取消Ref1的选择状态
        ref2.setRxMsgSelect(false); // 取消Ref2的选择状态
        ref3.setRxMsgSelect(false); // 取消Ref3的选择状态
        ref4.setRxMsgSelect(false); // 取消Ref4的选择状态
        ref5.setRxMsgSelect(false); // 取消Ref5的选择状态
        ref6.setRxMsgSelect(false); // 取消Ref6的选择状态
        ref7.setRxMsgSelect(false); // 取消Ref7的选择状态
        ref8.setRxMsgSelect(false); // 取消Ref8的选择状态
        s1.setRxMsgSelect(false); // 取消S1的选择状态
        s2.setRxMsgSelect(false); // 取消S2的选择状态
        s3.setRxMsgSelect(false); // 取消S3的选择状态
        s4.setRxMsgSelect(false); // 取消S4的选择状态
    }

    /**
     * 重写toString方法,输出所有通道的状态信息
     * 用于调试和日志输出
     *
     * @return 包含所有通道状态的字符串
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(); // 创建StringBuilder对象
        sb.append("MainRightMsgOthers{"); // 添加类名前缀
        sb.append("math1=").append(math1).append(", "); // 添加math1状态
        sb.append("math2=").append(math2).append(", "); // 添加math2状态
        sb.append("math3=").append(math3).append(", "); // 添加math3状态
        sb.append("math4=").append(math4).append(", "); // 添加math4状态
        sb.append("math5=").append(math5).append(", "); // 添加math5状态
        sb.append("math6=").append(math6).append(", "); // 添加math6状态
        sb.append("math7=").append(math7).append(", "); // 添加math7状态
        sb.append("math8=").append(math8).append(", "); // 添加math8状态
        sb.append("ref1=").append(ref1).append(", "); // 添加ref1状态
        sb.append("ref2=").append(ref2).append(", "); // 添加ref2状态
        sb.append("ref3=").append(ref3).append(", "); // 添加ref3状态
        sb.append("ref4=").append(ref4).append(", "); // 添加ref4状态
        sb.append("ref5=").append(ref5).append(", "); // 添加ref5状态
        sb.append("ref6=").append(ref6).append(", "); // 添加ref6状态
        sb.append("ref7=").append(ref7).append(", "); // 添加ref7状态
        sb.append("ref8=").append(ref8).append(", "); // 添加ref8状态
        sb.append("s1=").append(s1).append(", "); // 添加s1状态
        sb.append("s2=").append(s2).append(", "); // 添加s2状态
        sb.append("s3=").append(s3).append(", "); // 添加s3状态
        sb.append("s4=").append(s4); // 添加s4状态
        sb.append("}"); // 添加类名后缀
        return sb.toString(); // 返回完整的状态字符串
    }
}