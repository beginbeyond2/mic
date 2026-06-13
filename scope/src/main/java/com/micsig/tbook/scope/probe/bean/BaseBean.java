package com.micsig.tbook.scope.probe.bean;


import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.micsig.base.DoubleUtil;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.probe.BaseProbe;
import com.micsig.tbook.scope.vertical.VerticalAxis;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                BaseBean - 探头配置基类                                        ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Probe模块的探头配置基类，位于probe.bean包下，                                ║
 * ║   定义探头的基本属性和行为，为具体探头类型提供统一的数据模型。                    ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 存储探头基本属性（类型、名称、带宽、阻抗等）                               ║
 * ║   2. 管理探头比例尺配置（scaleNames、scaleValues）                             ║
 * ║   3. 解析JSON配置文件                                                         ║
 * ║   4. 提供探头克隆功能                                                         ║
 * ║   5. 定义抽象方法创建探头实例                                                  ║
 * ║                                                                              ║
 * ║ 【继承体系】                                                                 ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │   Cloneable     │ ← 接口：支持对象克隆             ║
 * ║                          └────────┬────────┘                                 ║
 * ║                                   │                                          ║
 * ║                          ┌────────▼────────┐                                 ║
 * ║                          │    BaseBean     │ ← 本类：探头配置基类             ║
 * ║                          │   (abstract)    │                                 ║
 * ║                          └────────┬────────┘                                 ║
 * ║                                   │                                          ║
 * ║                    ┌──────────────┴──────────────┐                           ║
 * ║                    │                             │                           ║
 * ║           ┌────────▼────────┐         ┌────────▼────────┐                   ║
 * ║           │    MDPBean      │         │   MSP500Bean    │                   ║
 * ║           │  (差分探头配置)  │         │ (MSP500探头配置)│                   ║
 * ║           └─────────────────┘         └─────────────────┘                   ║
 * ║                                                                              ║
 * ║ 【探头配置架构】                                                             ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                         BaseBean                                    │   ║
 * ║   │                                                                     │   ║
 * ║   │   ┌─────────────────────┐    ┌─────────────────────┐               │   ║
 * ║   │   │   基本属性          │    │   比例尺配置         │               │   ║
 * ║   │   │   ─────────────     │    │   ─────────────     │               │   ║
 * ║   │   │   - prefix          │    │   - scaleNames[]    │               │   ║
 * ║   │   │   - className       │    │   - scaleValues[]   │               │   ║
 * ║   │   │   - probeType       │    │                     │               │   ║
 * ║   │   │   - name            │    │   示例：             │               │   ║
 * ║   │   │   - bandwidth       │    │   "1X"  → 1.0       │               │   ║
 * ║   │   │   - bMustAc         │    │   "10X" → 10.0      │               │   ║
 * ║   │   │   - bScopeImpedence50│    │   "100X"→ 100.0     │               │   ║
 * ║   │   └─────────────────────┘    └─────────────────────┘               │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【JSON配置解析流程】                                                         ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ JSON文件    │───▶│ parseJson() │───▶│ 属性赋值    │                   ║
 * ║   └─────────────┘    └─────────────┘    └─────────────┘                   ║
 * ║                                              │                              ║
 * ║                                              ▼                              ║
 * ║                                      ┌─────────────┐                       ║
 * ║                                      │ 比例尺解析  │                       ║
 * ║                                      └─────────────┘                       ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. BeanFactory创建探头配置实例                                             ║
 * ║   2. BaseProbe持有BaseBean引用获取配置信息                                   ║
 * ║   3. JSON配置文件解析加载探头参数                                             ║
 * ║   4. 探头克隆复制配置                                                        ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 所有公共方法使用synchronized关键字保护                                    ║
 * ║   - volatile修饰bMustAc和bScopeImpedence50保证可见性                         ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - BaseProbe: 持有BaseBean引用，获取探头配置                                ║
 * ║   - BeanFactory: 创建BaseBean实例                                           ║
 * ║   - VerticalAxis: 提供探头类型常量（PROBE_TYPE_*）                           ║
 * ║   - ChannelFactory: 提供探头字符串转换                                       ║
 * ║   - DoubleUtil: 提供浮点数模糊比较                                          ║
 * ║   - Gson库: JSON解析                                                        ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 原型模式：实现Cloneable接口，支持对象克隆                                 ║
 * ║   - 工厂方法模式：抽象方法createProbe()由子类实现                             ║
 * ║   - 模板方法模式：parseJson()定义解析流程骨架                                 ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public abstract class BaseBean implements Cloneable {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 基本属性
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 探头前缀
     * 用于标识探头型号系列，如"MSP500"、"MDP"等
     * 在JSON配置中对应"prefix"字段
     */
    private String prefix = "";

    /**
     * 探头类别
     * 用于区分探头大类，如"差分探头"、"有源探头"等
     * 在JSON配置中对应"probetype"字段
     */
    private String className = "";

    /**
     * 探头类型
     * 用于标识具体探头型号，如"MSP500"、"MDP1001"等
     * 在JSON配置中对应"probetype"字段
     */
    private String probeType = "";

    /**
     * 探头别名
     * 用户可自定义的探头显示名称
     * 在JSON配置中对应"name"字段
     */
    private String name = "";

    /**
     * 是否必须接适配器标志
     * true: 必须连接适配器才能使用
     * false: 可直接使用
     * 使用volatile保证多线程可见性
     * 在JSON配置中对应"mustac"字段
     */
    private volatile boolean bMustAc = false;

    /**
     * 探头带宽
     * 单位：Hz
     * 表示探头的频率响应范围
     * 在JSON配置中对应"bandwidth"字段
     */
    private long bandwidth = 0;

    /**
     * 示波器阻抗50Ω标志
     * true: 示波器输入阻抗为50Ω
     * false: 示波器输入阻抗为1MΩ
     * 使用volatile保证多线程可见性
     * 在JSON配置中对应"scope_impedence50"字段
     */
    private volatile boolean bScopeImpedence50 = false;

    /**
     * 示波器调整标志
     * true: 需要示波器进行特殊调整
     * false: 不需要特殊调整
     * 在JSON配置中对应"scope_adjust"字段
     */
    private boolean bScopeAdjust = false;

    /**
     * DA（数模转换）标志
     * true: 支持DA功能
     * false: 不支持DA功能
     * 在JSON配置中对应"da"字段
     */
    protected boolean bDa = false;

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 探头单位
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 探头单位类型
     * 取值范围：VerticalAxis.PROBE_TYPE_* 常量
     * - PROBE_TYPE_VOL: 电压单位（V）
     * - PROBE_TYPE_CUR: 电流单位（A）
     * - PROBE_TYPE_PWR: 功率单位（W）
     * - PROBE_TYPE_DB: 分贝单位（dB）
     * 默认值：PROBE_TYPE_VOL（电压）
     */
    private int probeUnit = VerticalAxis.PROBE_TYPE_VOL;

    /**
     * 获取探头单位类型
     *
     * @return 探头单位类型（VerticalAxis.PROBE_TYPE_*）
     */
    public int getProbeUnit(){
        return probeUnit;                                                           // 返回探头单位类型
    }

    /**
     * 设置探头单位类型
     *
     * @param probeUnit 探头单位类型（VerticalAxis.PROBE_TYPE_*）
     */
    public void setProbeUnit(int probeUnit){
        this.probeUnit = probeUnit;                                                 // 设置探头单位类型
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 自动探头比例
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 自动探头比例列表
     * 存储自动识别的探头比例值
     * 用于探头自动识别功能
     */
    private List<Integer> listAutoProbeRate = new ArrayList<>();

    /**
     * 默认构造方法
     * 初始化BaseBean实例
     */
    public BaseBean() {
        // 默认构造方法，无需初始化操作
    }

    /**
     * 获取探头前缀
     * 线程安全方法
     *
     * @return 探头前缀字符串
     */
    public synchronized String getPrefix(){
        return prefix;                                                              // 返回探头前缀
    }

    /**
     * 添加自动探头比例
     * 线程安全方法
     *
     * @param v 探头比例值
     */
    public synchronized void addAutoProbeRate(int v){
        listAutoProbeRate.add(v);                                                   // 添加比例值到列表
    }

    /**
     * 获取自动探头比例列表
     * 线程安全方法
     *
     * @return 自动探头比例列表
     */
    public synchronized List<Integer> getAutoProbeRate(){
        return listAutoProbeRate;                                                   // 返回自动探头比例列表
    }

    /**
     * 清除自动探头比例列表
     * 线程安全方法
     */
    public synchronized void clearAutoProbeRate(){
        listAutoProbeRate.clear();                                                  // 清空自动探头比例列表
    }

    /**
     * 检查是否有自动探头比例
     * 线程安全方法
     *
     * @return true: 有自动探头比例
     *         false: 无自动探头比例
     */
    public synchronized boolean isAutoProbeRate(){
        return listAutoProbeRate.size() > 0;                                        // 返回列表是否非空
    }

    /**
     * 获取探头类别
     * 线程安全方法
     *
     * @return 探头类别字符串
     */
    public synchronized String getClassName() {
        return className;                                                           // 返回探头类别
    }

    /**
     * 设置探头类别
     * 线程安全方法
     *
     * @param className 探头类别字符串
     */
    public synchronized void setClassName(String className) {
        this.className = className;                                                 // 设置探头类别
    }

    /**
     * 获取探头类型
     * 线程安全方法
     *
     * @return 探头类型字符串
     */
    public synchronized String getProbeType() {
        return probeType;                                                           // 返回探头类型
    }

    /**
     * 设置探头类型
     * 线程安全方法
     *
     * @param probeType 探头类型字符串
     */
    public synchronized void setProbeType(String probeType) {
        this.probeType = probeType;                                                 // 设置探头类型
    }

    /**
     * 获取探头别名
     * 线程安全方法
     *
     * @return 探头别名字符串
     */
    public synchronized String getName() {
        return name;                                                                // 返回探头别名
    }

    /**
     * 设置探头别名
     * 线程安全方法
     *
     * @param name 探头别名字符串
     */
    public synchronized void setName(String name) {
        this.name = name;                                                           // 设置探头别名
    }

    /**
     * 检查是否必须接适配器
     * 线程安全方法
     *
     * @return true: 必须接适配器
     *         false: 不需要适配器
     */
    public synchronized boolean isMustAc() {
        return bMustAc;                                                             // 返回必须接适配器标志
    }

    /**
     * 检查示波器阻抗是否为50Ω
     * 线程安全方法
     *
     * @return true: 阻抗为50Ω
     *         false: 阻抗为1MΩ
     */
    public synchronized boolean isScopeImpedence50(){
        return bScopeImpedence50;                                                   // 返回阻抗50Ω标志
    }

    /**
     * 检查是否需要示波器调整
     * 线程安全方法
     *
     * @return true: 需要调整
     *         false: 不需要调整
     */
    public synchronized boolean isScopeAdjust(){return bScopeAdjust;}               // 返回示波器调整标志

    /**
     * 检查是否支持DA功能
     * 线程安全方法
     *
     * @return true: 支持DA
     *         false: 不支持DA
     */
    public synchronized boolean isDa(){return bDa;}                                 // 返回DA功能标志

    /**
     * 获取探头带宽
     * 线程安全方法
     *
     * @return 探头带宽（单位：Hz）
     */
    public synchronized long getBandwidth(){return bandwidth;}                      // 返回探头带宽

    /**
     * 设置探头带宽
     * 线程安全方法
     *
     * @param bandwidth 探头带宽（单位：Hz）
     */
    public synchronized void setBandwidth(long bandwidth){
        this.bandwidth = bandwidth;                                                 // 设置探头带宽
    }

    /**
     * 获取比例尺名称列表
     * 线程安全方法
     *
     * @return 比例尺名称列表
     */
    public synchronized List<String> getScaleNames(){ return scaleNames;}           // 返回比例尺名称列表

    /**
     * 获取比例尺值列表
     * 线程安全方法
     *
     * @return 比例尺值列表
     */
    public synchronized List<Double> getScaleValues(){return scaleValues;}          // 返回比例尺值列表

    /**
     * 添加比例尺
     * 将值转换为显示名称并添加到列表
     * 线程安全方法
     *
     * <p><b>转换规则：</b></p>
     * <ul>
     *   <li>值 < 1: 转换为mX单位（毫）</li>
     *   <li>值 >= 1000: 转换为kX单位（千）</li>
     *   <li>其他: 使用X单位</li>
     * </ul>
     *
     * @param val 比例尺值
     */
    public synchronized void addScale(double val){
        String sVal= getXFromDouble(val);                                           // 将数值转换为显示字符串
        scaleNames.add(sVal);                                                       // 添加比例尺名称
        scaleValues.add(Double.valueOf(val));                                       // 添加比例尺值
    }

    /**
     * 将double值转换为X单位字符串
     * 私有静态工具方法
     *
     * <p><b>转换规则：</b></p>
     * <ul>
     *   <li>值 < 1: 乘以1000，单位为"mX"（毫）</li>
     *   <li>值 >= 1000: 除以1000，单位为"kX"（千）</li>
     *   <li>其他: 单位为"X"</li>
     * </ul>
     *
     * <p><b>格式化规则：</b></p>
     * <ul>
     *   <li>保留3-4位有效数字</li>
     *   <li>去除尾部多余的0</li>
     *   <li>去除尾部的小数点</li>
     * </ul>
     *
     * @param x 输入值
     * @return 格式化后的字符串（如"1X"、"10mX"、"1.5kX"）
     */
    private static String getXFromDouble(double x) {
        String unit = "";                                                           // 初始化单位字符串
        if (x < 1) {                                                                // 值小于1，使用毫单位
            x = x * 1000;                                                           // 转换为毫单位
            unit = "mX";                                                            // 设置单位为mX
        } else if (x >= 1000) {                                                     // 值大于等于1000，使用千单位
            x = x / 1000;                                                           // 转换为千单位
            unit = "kX";                                                            // 设置单位为kX
        } else {                                                                    // 值在1到1000之间
            unit = "X";                                                             // 设置单位为X
        }
        x += 0.001;                                                                 // 加0.001避免精度问题
        String s = String.valueOf(x);                                               // 转换为字符串
        if (s.length() >= 3) {                                                      // 字符串长度至少为3
            String substring = s.substring(0, 3);                                   // 截取前3位
            if (substring.contains(".") && s.length() >= 4) {                       // 包含小数点且长度>=4
                substring = s.substring(0, 4);                                      // 截取前4位
            }
            if (substring.contains(".")) {                                          // 包含小数点
                while (substring.endsWith("0")) {                                   // 循环去除尾部0
                    substring = substring.substring(0, substring.length() - 1);      // 去除最后一个0
                }
                if (substring.endsWith(".")) {                                      // 以小数点结尾
                    substring = substring.substring(0, substring.length() - 1);      // 去除小数点
                }
            }
            s = substring;                                                          // 更新字符串
        }
        return s + unit;                                                            // 返回带单位的字符串
    }

    /**
     * 清除比例尺列表
     * 线程安全方法
     */
    public synchronized void clearScales(){
        scaleNames.clear();                                                         // 清空比例尺名称列表
        scaleValues.clear();                                                        // 清空比例尺值列表
    }

    /**
     * 解析JSON配置
     * 从JSON对象中提取探头配置信息
     * 线程安全方法
     *
     * <p><b>JSON字段映射：</b></p>
     * <pre>
     * {
     *   "prefix": "探头前缀",
     *   "probetype": "探头类型",
     *   "name": "探头别名",
     *   "mustac": false,
     *   "scope_impedence50": false,
     *   "scope_adjust": false,
     *   "da": false,
     *   "bandwidth": 100000000,
     *   "unit": "V",
     *   "scalesname": ["1X", "10X", "100X"],
     *   "scalesvalue": [1.0, 10.0, 100.0]
     * }
     * </pre>
     *
     * @param jsonElement JSON元素对象
     */
    public synchronized void parseJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();                      // 获取JSON对象
        if (jsonObject != null) {                                                   // JSON对象不为空
            JsonElement element = jsonObject.get("prefix");                         // 获取prefix字段
            if(element != null){                                                    // 字段存在
                prefix = element.getAsString();                                     // 设置探头前缀
            }
            element = jsonObject.get("probetype");                                  // 获取probetype字段
            if (element != null) {                                                  // 字段存在
                probeType = element.getAsString();                                  // 设置探头类型
            }
            element = jsonObject.get("name");                                       // 获取name字段
            if (element != null) {                                                  // 字段存在
                name = element.getAsString();                                       // 设置探头别名
            }
            element = jsonObject.get("mustac");                                     // 获取mustac字段
            if (element != null) {                                                  // 字段存在
                bMustAc = element.getAsBoolean();                                   // 设置必须接适配器标志
            }
            element = jsonObject.get("scope_impedence50");                          // 获取scope_impedence50字段
            if (element != null) {                                                  // 字段存在
                bScopeImpedence50 = element.getAsBoolean();                         // 设置阻抗50Ω标志
            }
            element = jsonObject.get("scope_adjust");                               // 获取scope_adjust字段
            if(element != null){                                                    // 字段存在
                bScopeAdjust = element.getAsBoolean();                              // 设置示波器调整标志
            }
            element = jsonObject.get("da");                                         // 获取da字段
            if (element != null) {                                                  // 字段存在
                bDa = element.getAsBoolean();                                       // 设置DA功能标志
            }
            element =jsonObject.get("bandwidth");                                   // 获取bandwidth字段
            if (element!=null){                                                     // 字段存在
                bandwidth=element.getAsLong();                                      // 设置探头带宽
            }
            element =jsonObject.get("unit");                                        // 获取unit字段
            if (element!=null){                                                     // 字段存在
                String str = element.getAsString();                                 // 获取单位字符串
                for(int i=VerticalAxis.PROBE_TYPE_MIN;                              // 遍历所有探头类型
                            i<VerticalAxis.PROBE_TYPE_MAX;
                            i++){
                    if(ChannelFactory.getProbeString(i).equals(str)){               // 匹配探头类型字符串
                        probeUnit = i;                                              // 设置探头单位类型
                        break;                                                      // 跳出循环
                    }
                }
            }

            // 解析比例尺数组
            JsonElement[] elements = new JsonElement[2];                            // 创建JSON元素数组
            JsonArray[] arrays = new JsonArray[2];                                  // 创建JSON数组
            elements[0] = jsonObject.get("scalesname");                             // 获取scalesname字段
            elements[1] = jsonObject.get("scalesvalue");                            // 获取scalesvalue字段

            if (elements[0] != null && elements[1] != null) {                       // 两个字段都存在
                arrays[0] = elements[0].getAsJsonArray();                           // 获取比例尺名称数组
                arrays[1] = elements[1].getAsJsonArray();                           // 获取比例尺值数组
                int s = Math.min(arrays[0].size(), arrays[1].size());               // 取最小长度
                scaleNames.clear();                                                 // 清空比例尺名称列表
                scaleValues.clear();                                                // 清空比例尺值列表
                for (int i = 0; i < s; i++) {                                       // 遍历数组
                    scaleNames.add(arrays[0].get(i).getAsString());                 // 添加比例尺名称
                    scaleValues.add(arrays[1].get(i).getAsDouble());                // 添加比例尺值
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 抽象方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 创建探头实例
     * 抽象方法，由子类实现具体探头创建逻辑
     *
     * <p><b>设计模式：</b>工厂方法模式</p>
     * <p>子类根据自身类型创建对应的BaseProbe实例</p>
     *
     * @return BaseProbe探头实例
     */
    public abstract BaseProbe createProbe();

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 比例尺配置
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 比例尺名称列表
     * 存储探头比例的显示名称，如"1X"、"10X"、"100X"等
     * 与scaleValues一一对应
     */
    protected List<String> scaleNames = new ArrayList<>();

    /**
     * 比例尺值列表
     * 存储探头比例的实际数值，如1.0、10.0、100.0等
     * 与scaleNames一一对应
     */
    protected List<Double> scaleValues = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════════════════════
    // 比例尺查询方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取比例尺名称列表
     * 线程安全方法
     *
     * @return 比例尺名称列表
     */
    public synchronized List<String> getScaleName() {
        return scaleNames;                                                          // 返回比例尺名称列表
    }

    /**
     * 根据比例尺名称获取比例值
     * 线程安全方法
     *
     * @param key 比例尺名称（如"1X"、"10X"）
     * @return 比例值，未找到返回0
     */
    public synchronized double getScaleValue(String key) {
        double val = 0;                                                             // 初始化返回值

        int s = Math.min(scaleNames.size(),scaleValues.size());                     // 取最小长度

        for (int i = 0; i < s; i++) {                                               // 遍历比例尺列表
            String name = scaleNames.get(i);                                        // 获取比例尺名称
            if (name.equals(key)) {                                                 // 名称匹配
                val = scaleValues.get(i);                                           // 获取对应的值
                break;                                                              // 跳出循环
            }
        }
        return val;                                                                 // 返回比例值
    }

    /**
     * 根据比例值获取比例尺名称
     * 使用模糊比较匹配
     * 线程安全方法
     *
     * @param val 比例值
     * @return 比例尺名称，未找到返回空字符串
     */
    public synchronized String getScaleKey(double val) {
        String name = "";                                                           // 初始化返回值
        int s = Math.min(scaleNames.size(),scaleValues.size());                     // 取最小长度
        for (int i = 0; i < s; i++) {                                               // 遍历比例尺列表
            if (DoubleUtil.FuzzyCompare(val, scaleValues.get(i))) {                 // 模糊比较匹配
                name = scaleNames.get(i);                                           // 获取对应的名称
                break;                                                              // 跳出循环
            }
        }
        return name;                                                                // 返回比例尺名称
    }

    /**
     * 根据比例值获取比例尺索引
     * 使用模糊比较匹配
     * 线程安全方法
     *
     * @param val 比例值
     * @return 比例尺索引，未找到返回-1
     */
    public synchronized int  getScaleIndex(double val) {
        int idx = -1;                                                               // 初始化索引为-1
        int s = scaleValues.size();                                                 // 获取比例尺值列表大小
        for (int i = 0; i < s; i++) {                                               // 遍历比例尺值列表
            if (DoubleUtil.FuzzyCompare(val, scaleValues.get(i))) {                 // 模糊比较匹配
                idx = i;                                                            // 设置索引
                break;                                                              // 跳出循环
            }
        }
        return idx;                                                                 // 返回索引
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 克隆方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 克隆BaseBean实例
     * 实现深拷贝，复制比例尺列表
     *
     * <p><b>设计模式：</b>原型模式</p>
     * <p>通过克隆快速创建配置相同的探头实例</p>
     *
     * <p><b>深拷贝内容：</b></p>
     * <ul>
     *   <li>scaleNames: 创建新的ArrayList</li>
     *   <li>scaleValues: 创建新的ArrayList</li>
     * </ul>
     *
     * @return 克隆的BaseBean实例
     * @throws CloneNotSupportedException 克隆不支持异常
     */
    @NonNull
    @Override
    protected BaseBean clone() throws CloneNotSupportedException {
        BaseBean baseBean = (BaseBean)super.clone();                                // 调用父类克隆方法
        if(scaleNames != null && scaleNames.size() > 0){                            // 比例尺名称列表非空
            baseBean.scaleNames = new ArrayList<>(scaleNames);                      // 深拷贝比例尺名称列表
        }

        if(scaleValues != null && scaleValues.size() > 0){                          // 比例尺值列表非空
            baseBean.scaleValues = new ArrayList<>(scaleValues);                    // 深拷贝比例尺值列表
        }
        return baseBean;                                                            // 返回克隆实例
    }
}
