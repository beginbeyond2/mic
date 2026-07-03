package com.micsig.tbook.tbookscope.rightslipmenu;

import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.bean.RxIntWithSelect;
import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.wavezone.TChan;

/*
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                           RightMsgMath                                      ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 模块定位：右侧滑出菜单 - 数学通道消息封装类                                    ║
 * ║ 核心职责：封装数学通道的所有参数状态，用于UI与业务层通信                         ║
 * ║ 架构设计：纯数据消息Bean，配合RightLayoutMath使用，通过RxBus事件总线传递       ║
 * ║ 数据流向：RightLayoutMath → RxBus → 订阅方                                  ║
 * ║ 依赖关系：CacheUtil, RxBooleanWithSelect, RxIntWithSelect,                  ║
 * ║           RxStringWithSelect, RightBeanSelect, TChan                        ║
 * ║ 使用场景：数学通道参数（类型/源/窗函数/AX+B/高级数学等）变更时传递消息           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * Created by yangj on 2017/5/12.
 * 数学通道消息封装类
 * <p>
 * 封装数学通道的所有参数状态，包括：
 * - mathCheck：数学通道开关
 * - mathType：数学运算类型（双波形/FFT/AX+B/高级数学）
 * - dwSource1/dwSource2/dwSymbol：双波形运算参数
 * - fftType/fftSource/fftWindow/fftPersist：FFT运算参数
 * - axbUnit/axbSource/axbA/axbB：AX+B运算参数
 * - amUnit/amFormula/amVar1Number/amVar1Power/amVar2Number/amVar2Power：高级数学参数
 * - label：通道标签
 * </p>
 */
public class RightMsgMath {
    /** 数学类型常量：双波形运算 */
    public static final int MATHTYPE_DW = CacheUtil.MATHTYPE_DW;
    /** 数学类型常量：FFT运算 */
    public static final int MATHTYPE_FFT = CacheUtil.MATHTYPE_FFT;
    /** 数学类型常量：AX+B运算 */
    public static final int MATHTYPE_AXB = CacheUtil.MATHTYPE_AXB;
    /** 数学类型常量：高级数学运算 */
    public static final int MATHTYPE_AM = CacheUtil.MATHTYPE_AM;

    /** 数学通道开关状态（带选中标记） */
    private RxBooleanWithSelect mathCheck;
    /** 数学运算类型索引（带选中标记） */
    private RxIntWithSelect mathType;
    /** 数学运算类型选择项（带选中标记） */
    private RightBeanSelect mathTypeSelect;
    /** 双波形源1选择项 */
    private RightBeanSelect dwSource1;
    /** 双波形运算符选择项 */
    private RightBeanSelect dwSymbol;
    /** 双波形源2选择项 */
    private RightBeanSelect dwSource2;
    /** FFT类型文本（带选中标记） */
    private RxStringWithSelect fftType;
    /** FFT源选择项 */
    private RightBeanSelect fftSource;
    /** FFT窗函数选择项 */
    private RightBeanSelect fftWindow;
    /** FFT持久化选择项 */
    private RightBeanSelect fftPersist;
    /** AX+B单位文本（带选中标记） */
    private RxStringWithSelect axbUnit;
    /** AX+B源选择项 */
    private RightBeanSelect axbSource;
    /** AX+B系数A文本（带选中标记） */
    private RxStringWithSelect axbA;
    /** AX+B系数B文本（带选中标记） */
    private RxStringWithSelect axbB;
    /** 高级数学单位文本（带选中标记） */
    private RxStringWithSelect amUnit;
    /** 高级数学公式文本（带选中标记） */
    private RxStringWithSelect amFormula;
    /** 高级数学变量1数值文本（带选中标记） */
    private RxStringWithSelect amVar1Number;
    /** 高级数学变量1指数文本（带选中标记） */
    private RxStringWithSelect amVar1Power;
    /** 高级数学变量2数值文本（带选中标记） */
    private RxStringWithSelect amVar2Number;
    /** 高级数学变量2指数文本（带选中标记） */
    private RxStringWithSelect amVar2Power;
    /** 通道标签文本（带选中标记） */
    private RxStringWithSelect label;


    /** 数学通道编号，默认Math1 */
    private int mathChannelNumber = TChan.Math1;//默认Math1

    /** 是否为向上点击（垂直档位调节方向） */
    private boolean isUpClick;

    /**
     * 判断是否为向上点击调节
     * @return true=向上调节, false=向下调节
     */
    public boolean isUpClick() {
        return isUpClick;
    }

    /**
     * 设置垂直档位调节方向
     * @param upClick true=向上调节, false=向下调节
     */
    public void setUpClick(boolean upClick) {
        isUpClick = upClick;
    }

    /**
     * 获取数学通道编号
     * @return 数学通道编号（TChan.Math1-Math8）
     */
    public int getMathChannelNumber() {
        return mathChannelNumber;
    }

    /**
     * 设置数学通道编号
     * @param mathChannelNumber 数学通道编号
     */
    public void setMathChannelNumber(int mathChannelNumber) {
        this.mathChannelNumber = mathChannelNumber;
    }

    /**
     * 获取数学通道开关状态
     * @return 带选中标记的开关状态
     */
    public RxBooleanWithSelect getMathCheck() {
        return mathCheck;
    }

    /**
     * 设置数学通道开关状态
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param mathCheck 开关状态值
     */
    public void setMathCheck(boolean mathCheck) {
        if (this.mathCheck == null) {                                                  // 首次创建对象
            this.mathCheck = new RxBooleanWithSelect(mathCheck);
        } else {                                                                       // 非首次，更新值并标记选中
            this.mathCheck.setValue(mathCheck);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.mathCheck.setRxMsgSelect(true);                                       // 标记当前字段为选中
        }
    }

    /**
     * 获取数学运算类型索引
     * @return 带选中标记的类型索引
     */
    public RxIntWithSelect getMathType() {
        return mathType;
    }

    /**
     * 设置数学运算类型索引
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param mathType 类型索引值
     */
    public void setMathType(int mathType) {
        if (this.mathType == null) {                                                   // 首次创建对象
            this.mathType = new RxIntWithSelect(mathType);
        } else {                                                                       // 非首次，更新值并标记选中
            this.mathType.setValue(mathType);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.mathType.setRxMsgSelect(true);                                        // 标记当前字段为选中
        }
    }

    /**
     * 获取数学运算类型选择项
     * @return 数学运算类型选择项
     */
    public RightBeanSelect getMathTypeSelect() {
        return mathTypeSelect;
    }

    /**
     * 设置数学运算类型选择项
     * <p>首次设置时直接赋值，后续更新值并标记为当前选中项</p>
     * @param mathTypeSelect 数学运算类型选择项
     */
    public void setMathTypeSelect(RightBeanSelect mathTypeSelect) {
        if (this.mathTypeSelect == null) {                                             // 首次赋值
            this.mathTypeSelect = mathTypeSelect;
        } else {                                                                       // 非首次，更新值并标记选中
            this.mathTypeSelect = mathTypeSelect;
            setAllUnSelect();                                                          // 清除所有选中标记
            this.mathTypeSelect.setRxMsgSelect(true);                                  // 标记当前字段为选中
        }
    }


    /**
     * 获取双波形源1选择项
     * @return 双波形源1选择项
     */
    public RightBeanSelect getDwSource1() {
        return dwSource1;
    }

    /**
     * 设置双波形源1选择项
     * <p>首次设置时直接赋值，后续更新值并标记为当前选中项</p>
     * @param dwSource1 双波形源1选择项
     */
    public void setDwSource1(RightBeanSelect dwSource1) {
        if (this.dwSource1 == null) {                                                  // 首次赋值
            this.dwSource1 = dwSource1;
        } else {                                                                       // 非首次，更新值并标记选中
            this.dwSource1 = dwSource1;
            setAllUnSelect();                                                          // 清除所有选中标记
            this.dwSource1.setRxMsgSelect(true);                                       // 标记当前字段为选中
        }
    }

    /**
     * 获取双波形运算符选择项
     * @return 双波形运算符选择项
     */
    public RightBeanSelect getDwSymbol() {
        return dwSymbol;
    }

    /**
     * 设置双波形运算符选择项
     * <p>首次设置时直接赋值，后续更新值并标记为当前选中项</p>
     * @param dwSymbol 双波形运算符选择项
     */
    public void setDwSymbol(RightBeanSelect dwSymbol) {
        if (this.dwSymbol == null) {                                                   // 首次赋值
            this.dwSymbol = dwSymbol;
        } else {                                                                       // 非首次，更新值并标记选中
            this.dwSymbol = dwSymbol;
            setAllUnSelect();                                                          // 清除所有选中标记
            this.dwSymbol.setRxMsgSelect(true);                                        // 标记当前字段为选中
        }
    }

    /**
     * 获取双波形源2选择项
     * @return 双波形源2选择项
     */
    public RightBeanSelect getDwSource2() {
        return dwSource2;
    }

    /**
     * 设置双波形源2选择项
     * <p>首次设置时直接赋值，后续更新值并标记为当前选中项</p>
     * @param dwSource2 双波形源2选择项
     */
    public void setDwSource2(RightBeanSelect dwSource2) {
        if (this.dwSource2 == null) {                                                  // 首次赋值
            this.dwSource2 = dwSource2;
        } else {                                                                       // 非首次，更新值并标记选中
            this.dwSource2 = dwSource2;
            setAllUnSelect();                                                          // 清除所有选中标记
            this.dwSource2.setRxMsgSelect(true);                                       // 标记当前字段为选中
        }
    }

    /**
     * 获取FFT类型文本
     * @return 带选中标记的FFT类型
     */
    public RxStringWithSelect getFftType() {
        return fftType;
    }

    /**
     * 设置FFT类型文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param fftType FFT类型文本
     */
    public void setFftType(String fftType) {
        if (this.fftType == null) {                                                    // 首次创建对象
            this.fftType = new RxStringWithSelect(fftType);
        } else {                                                                       // 非首次，更新值并标记选中
            this.fftType.setValue(fftType);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.mathType.setRxMsgSelect(true);                                        // 标记mathType为选中（注意：此处标记的是mathType而非fftType）
        }
    }

    /**
     * 获取FFT源选择项
     * @return FFT源选择项
     */
    public RightBeanSelect getFftSource() {
        return fftSource;
    }

    /**
     * 设置FFT源选择项
     * <p>首次设置时直接赋值，后续更新值并标记为当前选中项</p>
     * @param fftSource FFT源选择项
     */
    public void setFftSource(RightBeanSelect fftSource) {
        if (this.fftSource == null) {                                                  // 首次赋值
            this.fftSource = fftSource;
        } else {                                                                       // 非首次，更新值并标记选中
            this.fftSource = fftSource;
            setAllUnSelect();                                                          // 清除所有选中标记
            this.fftSource.setRxMsgSelect(true);                                       // 标记当前字段为选中
        }
    }

    /**
     * 获取FFT窗函数选择项
     * @return FFT窗函数选择项
     */
    public RightBeanSelect getFftWindow() {
        return fftWindow;
    }

    /**
     * 设置FFT窗函数选择项
     * <p>首次设置时直接赋值，后续更新值并标记为当前选中项</p>
     * @param fftWindow FFT窗函数选择项
     */
    public void setFftWindow(RightBeanSelect fftWindow) {
        if (this.fftWindow == null) {                                                  // 首次赋值
            this.fftWindow = fftWindow;
        } else {                                                                       // 非首次，更新值并标记选中
            this.fftWindow = fftWindow;
            setAllUnSelect();                                                          // 清除所有选中标记
            this.fftWindow.setRxMsgSelect(true);                                       // 标记当前字段为选中
        }
    }

    /**
     * 获取FFT持久化选择项
     * @return FFT持久化选择项
     */
    public RightBeanSelect getFftPersist(){return fftPersist;}

    /**
     * 设置FFT持久化选择项
     * <p>首次设置时直接赋值，后续更新值并标记为当前选中项</p>
     * @param fftPersist FFT持久化选择项
     */
    public void setFftPersist(RightBeanSelect fftPersist){
        if (this.fftPersist==null){                                                    // 首次赋值
            this.fftPersist=fftPersist;
        }else {                                                                        // 非首次，更新值并标记选中
            this.fftPersist=fftPersist;
            setAllUnSelect();                                                          // 清除所有选中标记
            this.fftPersist.setRxMsgSelect(true);                                      // 标记当前字段为选中
        }
    }

    /**
     * 获取AX+B单位文本
     * @return 带选中标记的AX+B单位
     */
    public RxStringWithSelect getAxbUnit() {
        return axbUnit;
    }

    /**
     * 设置AX+B单位文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param axbUnit AX+B单位文本
     */
    public void setAxbUnit(String axbUnit) {
        if (this.axbUnit == null) {                                                    // 首次创建对象
            this.axbUnit = new RxStringWithSelect(axbUnit);
        } else {                                                                       // 非首次，更新值并标记选中
            this.axbUnit.setValue(axbUnit);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.axbUnit.setRxMsgSelect(true);                                         // 标记当前字段为选中
        }
    }

    /**
     * 获取AX+B源选择项
     * @return AX+B源选择项
     */
    public RightBeanSelect getAxbSource() {
        return axbSource;
    }

    /**
     * 设置AX+B源选择项
     * <p>首次设置时直接赋值，后续更新值并标记为当前选中项</p>
     * @param axbSource AX+B源选择项
     */
    public void setAxbSource(RightBeanSelect axbSource) {
        if (this.axbSource == null) {                                                  // 首次赋值
            this.axbSource = axbSource;
        } else {                                                                       // 非首次，更新值并标记选中
            this.axbSource = axbSource;
            setAllUnSelect();                                                          // 清除所有选中标记
            this.axbSource.setRxMsgSelect(true);                                       // 标记当前字段为选中
        }
    }

    /**
     * 获取AX+B系数A文本
     * @return 带选中标记的系数A
     */
    public RxStringWithSelect getAxbA() {
        return axbA;
    }

    /**
     * 设置AX+B系数A文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param axbA 系数A文本
     */
    public void setAxbA(String axbA) {
        if (this.axbA == null) {                                                       // 首次创建对象
            this.axbA = new RxStringWithSelect(axbA);
        } else {                                                                       // 非首次，更新值并标记选中
            this.axbA.setValue(axbA);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.axbA.setRxMsgSelect(true);                                            // 标记当前字段为选中
        }
    }

    /**
     * 获取AX+B系数B文本
     * @return 带选中标记的系数B
     */
    public RxStringWithSelect getAxbB() {
        return axbB;
    }

    /**
     * 设置AX+B系数B文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param axbB 系数B文本
     */
    public void setAxbB(String axbB) {
        if (this.axbB == null) {                                                       // 首次创建对象
            this.axbB = new RxStringWithSelect(axbB);
        } else {                                                                       // 非首次，更新值并标记选中
            this.axbB.setValue(axbB);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.axbB.setRxMsgSelect(true);                                            // 标记当前字段为选中
        }
    }

    /**
     * 获取高级数学单位文本
     * @return 带选中标记的高级数学单位
     */
    public RxStringWithSelect getAmUnit() {
        return amUnit;
    }

    /**
     * 设置高级数学单位文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param amUnit 高级数学单位文本
     */
    public void setAmUnit(String amUnit) {
        if (this.amUnit == null) {                                                     // 首次创建对象
            this.amUnit = new RxStringWithSelect(amUnit);
        } else {                                                                       // 非首次，更新值并标记选中
            this.amUnit.setValue(amUnit);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.amUnit.setRxMsgSelect(true);                                          // 标记当前字段为选中
        }
    }

    /**
     * 获取高级数学公式文本
     * @return 带选中标记的高级数学公式
     */
    public RxStringWithSelect getAmFormula() {
        return amFormula;
    }

    /**
     * 设置高级数学公式文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param amFormula 高级数学公式文本
     */
    public void setAmFormula(String amFormula) {
        if (this.amFormula == null) {                                                  // 首次创建对象
            this.amFormula = new RxStringWithSelect(amFormula);
        } else {                                                                       // 非首次，更新值并标记选中
            this.amFormula.setValue(amFormula);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.amFormula.setRxMsgSelect(true);                                       // 标记当前字段为选中
        }
    }

    /**
     * 获取高级数学变量1数值文本
     * @return 带选中标记的变量1数值
     */
    public RxStringWithSelect getAmVar1Number() {
        return amVar1Number;
    }

    /**
     * 设置高级数学变量1数值文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param amVar1Number 变量1数值文本
     */
    public void setAmVar1Number(String amVar1Number) {
        if (this.amVar1Number == null) {                                               // 首次创建对象
            this.amVar1Number = new RxStringWithSelect(amVar1Number);
        } else {                                                                       // 非首次，更新值并标记选中
            this.amVar1Number.setValue(amVar1Number);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.amVar1Number.setRxMsgSelect(true);                                    // 标记当前字段为选中
        }
    }

    /**
     * 获取高级数学变量1指数文本
     * @return 带选中标记的变量1指数
     */
    public RxStringWithSelect getAmVar1Power() {
        return amVar1Power;
    }

    /**
     * 设置高级数学变量1指数文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param amVar1Power 变量1指数文本
     */
    public void setAmVar1Power(String amVar1Power) {
        if (this.amVar1Power == null) {                                                // 首次创建对象
            this.amVar1Power = new RxStringWithSelect(amVar1Power);
        } else {                                                                       // 非首次，更新值并标记选中
            this.amVar1Power.setValue(amVar1Power);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.amVar1Power.setRxMsgSelect(true);                                     // 标记当前字段为选中
        }
    }

    /**
     * 获取高级数学变量2数值文本
     * @return 带选中标记的变量2数值
     */
    public RxStringWithSelect getAmVar2Number() {
        return amVar2Number;
    }

    /**
     * 设置高级数学变量2数值文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param amVar2Number 变量2数值文本
     */
    public void setAmVar2Number(String amVar2Number) {
        if (this.amVar2Number == null) {                                               // 首次创建对象
            this.amVar2Number = new RxStringWithSelect(amVar2Number);
        } else {                                                                       // 非首次，更新值并标记选中
            this.amVar2Number.setValue(amVar2Number);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.amVar2Number.setRxMsgSelect(true);                                    // 标记当前字段为选中
        }
    }

    /**
     * 获取高级数学变量2指数文本
     * @return 带选中标记的变量2指数
     */
    public RxStringWithSelect getAmVar2Power() {
        return amVar2Power;
    }

    /**
     * 设置高级数学变量2指数文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param amVar2Power 变量2指数文本
     */
    public void setAmVar2Power(String amVar2Power) {
        if (this.amVar2Power == null) {                                                // 首次创建对象
            this.amVar2Power = new RxStringWithSelect(amVar2Power);
        } else {                                                                       // 非首次，更新值并标记选中
            this.amVar2Power.setValue(amVar2Power);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.amVar2Power.setRxMsgSelect(true);                                     // 标记当前字段为选中
        }
    }

    /**
     * 获取通道标签文本
     * @return 带选中标记的标签文本
     */
    public RxStringWithSelect getLabel() {
        return label;
    }

    /**
     * 设置通道标签文本
     * <p>首次设置时创建对象，后续更新值并标记为当前选中项</p>
     * @param label 标签文本
     */
    public void setLabel(String label) {
        if (this.label == null) {                                                      // 首次创建对象
            this.label = new RxStringWithSelect(label);
        } else {                                                                       // 非首次，更新值并标记选中
            this.label.setValue(label);
            setAllUnSelect();                                                          // 清除所有选中标记
            this.label.setRxMsgSelect(true);                                           // 标记当前字段为选中
        }
    }

    /**
     * 清除所有字段的选中标记
     * <p>在每次属性变更前调用，确保只有变更的字段被标记为选中</p>
     */
    private void setAllUnSelect() {
        mathType.setRxMsgSelect(false);                                                // 清除数学类型选中标记
        dwSource1.setRxMsgSelect(false);                                               // 清除双波形源1选中标记
        dwSymbol.setRxMsgSelect(false);                                                // 清除双波形运算符选中标记
        dwSource2.setRxMsgSelect(false);                                               // 清除双波形源2选中标记
        fftType.setRxMsgSelect(false);                                                 // 清除FFT类型选中标记
        fftSource.setRxMsgSelect(false);                                               // 清除FFT源选中标记
        fftWindow.setRxMsgSelect(false);                                               // 清除FFT窗函数选中标记
        fftPersist.setRxMsgSelect(false);                                              // 清除FFT持久化选中标记
        axbUnit.setRxMsgSelect(false);                                                 // 清除AX+B单位选中标记
        axbSource.setRxMsgSelect(false);                                               // 清除AX+B源选中标记
        axbA.setRxMsgSelect(false);                                                    // 清除AX+B系数A选中标记
        axbB.setRxMsgSelect(false);                                                    // 清除AX+B系数B选中标记
        amUnit.setRxMsgSelect(false);                                                  // 清除高级数学单位选中标记
        amFormula.setRxMsgSelect(false);                                               // 清除高级数学公式选中标记
        amVar1Number.setRxMsgSelect(false);                                            // 清除变量1数值选中标记
        amVar1Power.setRxMsgSelect(false);                                             // 清除变量1指数选中标记
        amVar2Number.setRxMsgSelect(false);                                            // 清除变量2数值选中标记
        amVar2Power.setRxMsgSelect(false);                                             // 清除变量2指数选中标记
        label.setRxMsgSelect(false);                                                   // 清除标签选中标记
    }

    /**
     * 返回对象的字符串表示
     * @return 包含所有数学通道参数的字符串
     */
    @Override
    public String toString() {
        return "RightMsgMath{" +
                "mathCheck=" + mathCheck +
                ", mathType=" + mathType +
                ", dwSource1=" + dwSource1 +
                ", dwSymbol=" + dwSymbol +
                ", dwSource2=" + dwSource2 +
                ", fftType=" + fftType +
                ", fftSource=" + fftSource +
                ", fftWindow=" + fftWindow +
                ", axbUnit=" + axbUnit +
                ", axbSource=" + axbSource +
                ", axbA=" + axbA +
                ", axbB=" + axbB +
                ", amUnit=" + amUnit +
                ", amFormula=" + amFormula +
                ", amVar1Number=" + amVar1Number +
                ", amVar1Power=" + amVar1Power +
                ", amVar2Number=" + amVar2Number +
                ", amVar2Power=" + amVar2Power +
                ", label=" + label +
                '}';
    }
}
