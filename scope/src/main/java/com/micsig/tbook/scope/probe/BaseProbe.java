package com.micsig.tbook.scope.probe;


import android.util.Log;

import com.micsig.base.DoubleUtil;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.ScopeMessage;
import com.micsig.tbook.scope.Trigger.TriggerAction;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.probe.bean.BaseBean;
import com.micsig.tbook.scope.vertical.VerticalAxis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                BaseProbe - 探头抽象基类                                       ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Probe模块的探头抽象基类，位于probe包下，                                      ║
 * ║   定义探头的通用行为和属性，为具体探头类型提供统一的操作接口。                     ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 管理探头配置（BaseBean）                                                 ║
 * ║   2. 提供探头参数访问接口（比例、阻抗、带宽等）                                  ║
 * ║   3. 解析和处理探头命令                                                        ║
 * ║   4. 实现探头控制功能（自动零点、自动增益、待机等）                              ║
 * ║   5. 管理探头状态和事件通知                                                    ║
 * ║                                                                              ║
 * ║ 【继承体系】                                                                 ║
 * ║                          ┌─────────────────┐                                 ║
 * ║                          │    BaseProbe    │ ← 本类：探头抽象基类             ║
 * ║                          │   (abstract)    │                                 ║
 * ║                          └────────┬────────┘                                 ║
 * ║                                   │                                          ║
 * ║                    ┌──────────────┼──────────────┐                           ║
 * ║                    │              │              │                           ║
 * ║           ┌────────▼────────┐    │    ┌────────▼────────┐                   ║
 * ║           │   ProbeMDP      │    │    │  ProbeMSP500    │                   ║
 * ║           │  (差分探头)      │    │    │  (MSP500探头)   │                   ║
 * ║           └─────────────────┘    │    └─────────────────┘                   ║
 * ║                                  │                                          ║
 * ║                         其他探头子类...                                       ║
 * ║                                                                              ║
 * ║ 【探头架构图】                                                               ║
 * ║   ┌─────────────────────────────────────────────────────────────────────┐   ║
 * ║   │                         BaseProbe                                    │   ║
 * ║   │                                                                     │   ║
 * ║   │   ┌───────────────┐  ┌───────────────┐  ┌───────────────┐          │   ║
 * ║   │   │  配置管理     │  │  参数访问     │  │  命令处理     │          │   ║
 * ║   │   │  ─────────    │  │  ─────────    │  │  ─────────    │          │   ║
 * ║   │   │  - BaseBean   │  │  - 比例       │  │  - 解析命令   │          │   ║
 * ║   │   │  - infoMap    │  │  - 阻抗       │  │  - 发送命令   │          │   ║
 * ║   │   │               │  │  - 带宽       │  │  - 事件通知   │          │   ║
 * ║   │   │               │  │  - DA值       │  │               │          │   ║
 * ║   │   └───────────────┘  └───────────────┘  └───────────────┘          │   ║
 * ║   │                                                                     │   ║
 * ║   │   ┌───────────────┐  ┌───────────────┐  ┌───────────────┐          │   ║
 * ║   │   │  控制功能     │  │  状态管理     │  │  工具方法     │          │   ║
 * ║   │   │  ─────────    │  │  ─────────    │  │  ─────────    │          │   ║
 * ║   │   │  - 自动零点   │  │  - 通道索引   │  │  - 字节转换   │          │   ║
 * ║   │   │  - 自动增益   │  │  - 自动比例   │  │  - 数据解析   │          │   ║
 * ║   │   │  - 待机模式   │  │  - 自动零点   │  │               │          │   ║
 * ║   │   │  - DA输出     │  │               │  │               │          │   ║
 * ║   │   └───────────────┘  └───────────────┘  └───────────────┘          │   ║
 * ║   └─────────────────────────────────────────────────────────────────────┘   ║
 * ║                                                                              ║
 * ║ 【命令解析流程】                                                             ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 接收命令字节 │───▶│ 解析命令类型 │───▶│ 处理命令数据 │                   ║
 * ║   │ parserCommand│    │  switch-case │    │  更新状态    │                   ║
 * ║   └─────────────┘    └─────────────┘    └──────┬──────┘                   ║
 * ║                                                 │                          ║
 * ║                                                 ▼                          ║
 * ║                                        ┌─────────────┐                     ║
 * ║                                        │ 发送事件通知 │                     ║
 * ║                                        │ EventFactory│                     ║
 * ║                                        └─────────────┘                     ║
 * ║                                                                              ║
 * ║ 【支持的命令类型】                                                           ║
 * ║   ┌──────────────────┬────────────────────────────────────┐               ║
 * ║   │ 命令类型          │ 功能说明                            │               ║
 * ║   ├──────────────────┼────────────────────────────────────┤               ║
 * ║   │ TYPE_PROBE_RATE  │ 探头比例                            │               ║
 * ║   │ TYPE_PROBE_IMPED │ 探头阻抗                            │               ║
 * ║   │ TYPE_PROBE_UNIT  │ 探头单位                            │               ║
 * ║   │ TYPE_PROBE_RATE_DOT│ 比例点列表                        │               ║
 * ║   │ TYPE_PROBE_RATE_LIST│ 比例列表                         │               ║
 * ║   │ TYPE_BANDWIDTH   │ 带宽                                │               ║
 * ║   │ TYPE_PROBE_ALARM │ 报警                                │               ║
 * ║   │ TYPE_PROBE_ZERO  │ 零点                                │               ║
 * ║   │ TYPE_PROBE_ADJUST│ 增益调整                            │               ║
 * ║   └──────────────────┴────────────────────────────────────┘               ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. Channel创建通道时创建探头实例                                           ║
 * ║   2. 探头自动识别和配置加载                                                  ║
 * ║   3. 用户操作探头参数（比例、阻抗、零点等）                                    ║
 * ║   4. 探头命令解析和状态更新                                                  ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - probeRate使用synchronized保护                                           ║
 * ║   - bAutoZero使用volatile和synchronized保护                                 ║
 * ║   - 大部分方法非线程安全，需在主线程调用                                      ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - BaseBean: 探头配置数据类                                                ║
 * ║   - Channel: 通道管理类                                                     ║
 * ║   - EventFactory: 事件工厂，发送通知事件                                     ║
 * ║   - ScopeMessage: 示波器消息管理，发送探头命令                               ║
 * ║   - ProbeCommand: 探头命令构建工具类                                        ║
 * ║   - ProbeNotifyInfo: 探头通知信息类                                         ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public abstract class BaseProbe {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 基本属性
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 通道索引
     * 表示探头所属的通道编号（0-3）
     * 用于标识探头连接到哪个通道
     */
    protected int chIdx = 0;

    /**
     * 探头比例
     * 表示探头的衰减比例（如1X、10X、100X）
     * 取值范围：正数，通常为1.0、10.0、100.0等
     * 使用synchronized保护多线程访问
     */
    protected double probeRate = 1.0;

    /**
     * 零点值
     * 用于存储探头的零点校准值
     * 取值范围：short范围（-32768 ~ 32767）
     */
    protected int zeroVal = 0;

    /**
     * 设置通道索引
     *
     * @param chIdx 通道索引（0-3）
     */
    public void setChIdx(int chIdx){
        this.chIdx = chIdx;                                                         // 设置通道索引
    }

    /**
     * 获取通道索引
     *
     * @return 通道索引
     */
    public int getChIdx(){return chIdx;}                                            // 返回通道索引

    /**
     * 探头配置对象
     * 持有BaseBean引用，获取探头配置信息
     * 由子类通过构造方法传入
     */
    protected BaseBean baseBean;

    /**
     * 构造方法
     * 创建探头实例，初始化配置对象
     *
     * @param baseBean 探头配置对象
     */
    public BaseProbe(BaseBean baseBean){
        this.baseBean = baseBean;                                                   // 保存配置对象引用
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 探头基本信息方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取探头名称
     * 返回探头类型标识
     *
     * @return 探头类型字符串
     */
    public String getProbeName() {
        return getType();                                                           // 返回探头类型
    }

    /**
     * 获取探头型号名称
     * 返回探头前缀（如MSP500、MDP）
     *
     * @return 探头型号前缀
     */
    public String getModeName(){
        return baseBean.getPrefix();                                                // 返回配置中的前缀
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 探头类型和带宽方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 设置探头类型
     * 更新配置中的探头单位类型，并发送事件通知
     *
     * @param probeType 探头类型（VerticalAxis.PROBE_TYPE_*）
     */
    protected void setProbeType(int probeType){
        if(baseBean != null){                                                       // 检查配置对象
            baseBean.setProbeUnit(probeType);                                       // 设置探头单位类型
        }
        EventFactory.sendEvent(new EventBase(EventFactory.EVENT_PROBE_EVENT,chIdx),true); // 发送探头事件通知
    }

    /**
     * 获取探头类型
     * 返回探头的单位类型（电压/电流/功率等）
     *
     * @return 探头类型，默认为电压类型
     */
    public int getProbeType() {
        if(baseBean != null){                                                       // 检查配置对象
            return baseBean.getProbeUnit();                                         // 返回探头单位类型
        }
        return VerticalAxis.PROBE_TYPE_VOL;                                         // 默认返回电压类型
    }

    /**
     * 设置探头带宽
     * 更新配置中的带宽值，并发送事件通知
     *
     * @param bandWidth 带宽值（单位：Hz）
     */
    protected void setBandWidth(long bandWidth){
        if(baseBean != null){                                                       // 检查配置对象
            baseBean.setBandwidth(bandWidth);                                       // 设置带宽
            EventFactory.sendEvent(new EventBase(EventFactory.EVENT_PROBE_EVENT,chIdx),true); // 发送探头事件通知
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 阻抗管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 50Ω阻抗标志
     * true: 50Ω阻抗
     * false: 1MΩ阻抗
     */
    protected boolean bImped50 = false;

    /**
     * 设置阻抗类型
     * 切换50Ω/1MΩ阻抗，并发送命令到探头
     *
     * @param bImped50 true: 50Ω阻抗
     *                 false: 1MΩ阻抗
     */
    public void setImped50( boolean bImped50){
        if(bImped50 != this.bImped50) {                                             // 检查值是否变化
            this.bImped50 = bImped50;                                               // 更新阻抗标志
            sendCommand(ProbeCommand.probeImpedCommand(bImped50));                  // 发送阻抗切换命令
        }
    }

    /**
     * 获取阻抗类型
     *
     * @return true: 50Ω阻抗
     *         false: 1MΩ阻抗
     */
    public boolean isImped50(){
        return this.bImped50;                                                       // 返回阻抗标志
    }

    /**
     * 检查是否支持50Ω阻抗
     * 默认不支持，子类可重写
     *
     * @return true: 支持
     *         false: 不支持
     */
    public boolean isSupportImped50(){
        return false;                                                               // 默认不支持
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 探头特性查询方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 检查是否必须接适配器
     *
     * @return true: 必须接适配器
     *         false: 不需要
     */
    public boolean isMustAC(){
        if(baseBean != null) {                                                      // 检查配置对象
            return baseBean.isMustAc();                                             // 返回配置中的标志
        }
        return false;                                                               // 默认不需要
    }

    /**
     * 检查示波器阻抗是否为50Ω
     *
     * @return true: 示波器阻抗为50Ω
     *         false: 示波器阻抗为1MΩ
     */
    public boolean isScopeImpedence50(){
        if(baseBean != null) {                                                      // 检查配置对象
            return baseBean.isScopeImpedence50();                                   // 返回配置中的标志
        }
        return false;                                                               // 默认1MΩ
    }

    /**
     * 检查是否需要示波器调整
     *
     * @return true: 需要调整
     *         false: 不需要
     */
    public boolean isScopeAdjust(){
        if(baseBean != null){                                                       // 检查配置对象
            return baseBean.isScopeAdjust();                                        // 返回配置中的标志
        }
        return false;                                                               // 默认不需要
    }

    /**
     * 检查是否支持DA功能
     *
     * @return true: 支持DA
     *         false: 不支持
     */
    public boolean isDa(){
        if(baseBean != null){                                                       // 检查配置对象
            return baseBean.isDa();                                                 // 返回配置中的标志
        }
        return false;                                                               // 默认不支持
    }

    /**
     * 获取探头带宽
     *
     * @return 带宽值（单位：Hz）
     */
    public long getBandWidth(){
        if (baseBean!=null){                                                        // 检查配置对象
            return baseBean.getBandwidth();                                         // 返回配置中的带宽
        }
        return 0;                                                                   // 默认返回0
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 比例尺管理方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取比例尺名称列表
     *
     * @return 比例尺名称列表（如["1X", "10X", "100X"]）
     */
    public List<String> getScaleNames(){
        if (baseBean!=null){                                                        // 检查配置对象
            return baseBean.getScaleNames();                                        // 返回比例尺名称列表
        }
        return null;                                                                // 配置为空返回null
    }

    /**
     * 获取比例尺值列表
     *
     * @return 比例尺值列表（如[1.0, 10.0, 100.0]）
     */
    public List<Double> getScaleValues(){
        if (baseBean!=null){                                                        // 检查配置对象
            return baseBean.getScaleValues();                                       // 返回比例尺值列表
        }
        return null;                                                                // 配置为空返回null
    }

    /**
     * 清除比例尺列表
     * 用于重新加载比例配置
     */
    protected void clearScale(){
        if(baseBean != null){                                                       // 检查配置对象
            baseBean.clearScales();                                                 // 清除配置中的比例尺
        }
    }

    /**
     * 添加比例尺
     *
     * @param val 比例值
     */
    protected void addScale(double val){
        if(baseBean != null){                                                       // 检查配置对象
            baseBean.addScale(val);                                                 // 添加比例尺到配置
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 自动比例控制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 检查是否支持探头比例自动控制
     *
     * @return true: 支持
     *         false: 不支持
     */
    public boolean isSupportProbeRateCtrl(){
        if(baseBean != null){                                                       // 检查配置对象
            return baseBean.isAutoProbeRate();                                      // 返回配置中的标志
        }
        return false;                                                               // 默认不支持
    }

    /**
     * 清除自动比例列表
     */
    protected void clearAutoRate(){
        if(baseBean != null){                                                       // 检查配置对象
            baseBean.clearAutoProbeRate();                                          // 清除自动比例列表
        }
    }

    /**
     * 添加自动比例值
     *
     * @param val 比例值
     */
    protected void addAutoRate(int val){
        if(baseBean != null){                                                       // 检查配置对象
            baseBean.addAutoProbeRate(val);                                         // 添加自动比例值
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DA（数模转换）功能方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * DA输出值
     * 用于存储DA输出的数值
     * 取值范围：int范围
     */
    private int daValue = 0;

    /**
     * 获取DA输出值
     *
     * @return DA输出值
     */
    public int getDaValue(){
        return daValue;                                                             // 返回DA值
    }

    /**
     * 设置DA输出值
     * 更新DA值并通知通道更新
     *
     * @param val DA输出值
     */
    public void setDaValue(int val){
        daValue = val;                                                              // 更新DA值
        Channel channel = ChannelFactory.getDynamicChannel(this.chIdx);            // 获取通道实例
        if(channel != null){                                                        // 通道存在
            channel.changeProbeDa();                                                // 通知通道DA值变化
        }
    }

    /**
     * 移除探头
     * 清理探头资源，停止自动零点，重置DA值
     */
    public void remove(){
        if(bAutoZero) {                                                             // 正在自动零点
            zero(ProbeNotifyInfo.ZERO_FAIL1);                                       // 发送零点失败通知
        }
        setDaValue(0);                                                              // 重置DA值为0
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 自动比例控制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 自动比例控制标志
     * true: 启用自动比例控制
     * false: 禁用
     */
    boolean bAutoRateCtrl = false;

    /**
     * 检查是否启用自动比例控制
     *
     * @return true: 启用
     *         false: 禁用
     */
    public boolean isAutoRateCtrl(){
        return bAutoRateCtrl;                                                       // 返回自动比例控制标志
    }

    /**
     * 设置自动比例控制
     * 根据垂直档位自动调整探头比例
     *
     * @param bEnable true: 启用
     *                false: 禁用
     */
    public void setAutoRateCtrl(boolean bEnable){
        if(isSupportProbeRateCtrl()) {                                              // 检查是否支持
            bAutoRateCtrl = bEnable;                                                // 更新标志
            sendCommand(ProbeCommand.probeRateCtrlCommand(!bAutoRateCtrl));         // 发送命令（注意取反）
            if(bAutoRateCtrl){                                                      // 启用自动比例
                Channel channel = ChannelFactory.getDynamicChannel(chIdx);          // 获取通道实例
                if(channel != null){                                                // 通道存在
                    setVScaleVal(channel.getVScaleVal());                           // 根据当前档位设置比例
                }
            }
        }
    }

    /**
     * 根据垂直档位设置探头比例
     * 自动选择合适的探头比例
     *
     * @param vScaleVal 垂直档位值（单位：V/div）
     */
    public void setVScaleVal(double vScaleVal){
        if(bAutoRateCtrl){                                                          // 自动比例控制已启用
            if(baseBean != null){                                                   // 检查配置对象
                List<Integer> list = baseBean.getAutoProbeRate();                   // 获取自动比例列表
                List<Double> ls = getScaleValues();                                 // 获取比例值列表
                int i = 0;                                                          // 循环索引
                for(i=0;i<list.size() && i < ls.size();i++){                        // 遍历比例列表
                    double v = list.get(i) * ls.get(i)/1000;                        // 计算阈值电压
                    if(vScaleVal <= v){                                             // 档位值小于阈值
                        break;                                                      // 找到合适的比例
                    }
                }
                if(i < ls.size()){                                                  // 找到合适的比例
                    setProbeRate(ls.get(i));                                        // 设置探头比例
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 探头比例X接口方法（子类实现）
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取当前探头比例X的索引
     * 子类应重写此方法
     *
     * @return 比例索引，默认-1
     */
    public int getProbeXIndex(){
        return -1;                                                                  // 默认返回-1
    }

    /**
     * 获取探头比例X列表
     * 子类应重写此方法
     *
     * @return 比例名称列表，默认空列表
     */
    public List<String> getProbeX(){
        return new ArrayList<>();                                                   // 默认返回空列表
    }

    /**
     * 根据比例名称获取比例值
     * 子类应重写此方法
     *
     * @param probeX 比例名称
     * @return 比例值，默认0
     */
    public double getProbeXValue(String probeX){
        return 0;                                                                   // 默认返回0
    }

    /**
     * 获取当前探头比例X的名称
     * 子类应重写此方法
     *
     * @return 比例名称，默认空字符串
     */
    public String getProbeXName(){
        return "";                                                                  // 默认返回空字符串
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 字节转换工具方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 字节数组转int（大端序）
     * 从命令数据中解析4字节整数
     *
     * @param cmd 命令字节数组
     * @param offset 起始偏移量
     * @return 解析的int值
     */
    protected int byte2Int(byte [] cmd,int offset){
        int val = 0;                                                                // 初始化结果值
        val |= (cmd[offset] & 0xFF) << 24;                                          // 最高字节左移24位
        val |= (cmd[offset + 1] & 0xFF)<< 16;                                       // 次高字节左移16位
        val |= (cmd[offset + 2] & 0xFF)<< 8;                                        // 次低字节左移8位
        val |= (cmd[offset + 3] & 0xFF);                                            // 最低字节
        return val;                                                                 // 返回解析结果
    }

    /**
     * 字节数组转short（大端序）
     * 从命令数据中解析2字节短整数
     *
     * @param cmd 命令字节数组
     * @param offset 起始偏移量
     * @return 解析的short值
     */
    protected int byte2Short(byte [] cmd,int offset){
        short val = 0;                                                              // 初始化结果值
        val |= (cmd[offset] & 0xFF) << 8;                                           // 高字节左移8位
        val |= (cmd[offset + 1] & 0xFF);                                            // 低字节
        return val;                                                                 // 返回解析结果
    }

    /**
     * 字节数组转字符串形式的long
     * 从命令数据中解析ASCII编码的数字字符串
     *
     * @param cmd 命令字节数组
     * @param offset 起始偏移量
     * @param len 数据长度
     * @return 解析的long值
     */
    protected long byte2StringLong(byte [] cmd,int offset,int len){
        if(len > 0) {                                                               // 长度有效
            byte[] bytes = new byte[len + 1];                                       // 创建字节数组（多1字节用于字符串结束）
            System.arraycopy(cmd, offset, bytes, 0, len);                           // 复制数据
            String str = new String(bytes).trim().toUpperCase();                    // 转换为字符串并去除空格
            if(str.length() > 0) {                                                  // 字符串非空
                return Long.parseLong(str);                                         // 解析为long
            }
        }
        return 0;                                                                   // 默认返回0
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 事件通知方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 发送报警事件
     * 通知探头报警状态
     *
     * @param val 报警值
     */
    private void alarm(int val){
        EventFactory.sendEvent(new EventBase(EventFactory.EVENT_PROBE_ALARM,new ProbeNotifyInfo(chIdx,val)),true); // 发送报警事件
    }

    /**
     * 发送零点事件
     * 通知零点校准状态
     *
     * @param val 零点状态值
     */
    private void zero(int val){
        EventFactory.sendEvent(new EventBase(EventFactory.EVENT_PROBE_ZERO,new ProbeNotifyInfo(chIdx,val)),true); // 发送零点事件
        if(val != ProbeNotifyInfo.ZERO_ING){                                        // 零点操作完成
            if (bAutoZero) {                                                        // 正在自动零点
                bAutoZero = false;                                                  // 清除自动零点标志
            }
            sendCommand(ProbeCommand.QueryProbeZeroCommand());                      // 查询零点值
        }
    }

    /**
     * 发送增益事件
     * 通知增益调整状态
     *
     * @param val 增益值
     */
    protected void gain(int val){
        EventFactory.sendEvent(new EventBase(EventFactory.EVENT_PROBE_ZERO,new ProbeNotifyInfo(chIdx,val)),true); // 发送增益事件
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 命令解析方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 解析探头命令
     * 处理从探头MCU返回的命令数据
     * 
     * <p><b>命令格式：</b></p>
     * <pre>
     * ┌────────┬────────┬────────┬────────┬────────┐
     * │ 帧头   │ 类型   │ 长度   │ 数据   │ 校验   │
     * │ 0xAA   │ 1字节  │ 1字节  │ N字节  │ 1字节  │
     * └────────┴────────┴────────┴────────┴────────┘
     * </pre>
     * 
     * <p><b>支持的命令类型：</b></p>
     * <ul>
     *   <li>TYPE_PROBE_RATE: 探头比例</li>
     *   <li>TYPE_PROBE_IMPED: 探头阻抗</li>
     *   <li>TYPE_PROBE_UNIT: 探头单位</li>
     *   <li>TYPE_PROBE_RATE_DOT: 比例点列表</li>
     *   <li>TYPE_PROBE_RATE_LIST: 比例列表</li>
     *   <li>TYPE_BANDWIDTH: 带宽</li>
     *   <li>TYPE_PROBE_ALARM: 报警</li>
     *   <li>0xFF: 自动零点结果</li>
     * </ul>
     *
     * @param cmd 命令字节数组
     * @param offset 起始偏移量
     */
    public void parserCommand(byte [] cmd,int offset){
        if(cmd[offset] == (byte) 0xAA){                                             // 检查帧头
            byte type = cmd[offset + 1];                                            // 获取命令类型

            int len = cmd[offset + 2] & 0xFF;                                       // 获取数据长度
            switch (type){                                                           // 根据类型分发处理
                case ProbeCommand.TYPE_PROBE_RATE: {                                // 探头比例命令
                    int val = byte2Int(cmd, offset + 3);                            // 解析比例值
                    if(val != 0) {                                                  // 值有效
                        if ((val & (1 << 31)) != 0) {                               // 检查倒数标志位
                            val &= 0x7FFFFFFF;                                      // 清除标志位
                            setProbeRate(1.0 / val, false);                         // 设置倒数比例
                        } else {
                            setProbeRate(val, false);                               // 设置正数比例
                        }
                    }
                    break;
                }

                case ProbeCommand.TYPE_PROBE_IMPED: {                               // 探头阻抗命令

                    if (cmd[offset + 3] == 2) {                                     // 阻抗值为2表示50Ω
                        bImped50 = true;                                            // 设置50Ω标志
                    } else {
                        bImped50 = false;                                           // 设置1MΩ标志
                    }
                    Channel channel = ChannelFactory.getDynamicChannel(this.chIdx);  // 获取通道实例
                    if(channel != null){                                            // 通道存在
                        channel.setResistanceType(bImped50?Channel.RESISTANCE_50:Channel.RESISTANCE_1M); // 设置阻抗类型
                    }
                }
                    break;
                case ProbeCommand.TYPE_PROBE_UNIT:         //探头单位命令
                {
                    switch (cmd[offset + 3] & 0xFF){                                 // 根据单位值设置类型
                        case 0x76:                                                   // 'v' - 电压
                        case 0x56:                                                   // 'V' - 电压
                            setProbeType(VerticalAxis.PROBE_TYPE_VOL);              // 设置为电压类型
                            break;
                        case 0x41:                                                   // 'a' - 电流
                        case 0x61:                                                   // 'A' - 电流
                            setProbeType(VerticalAxis.PROBE_TYPE_CUR);              // 设置为电流类型
                            break;
                    }
                }
                    break;
                case ProbeCommand.TYPE_PROBE_RATE_DOT:                               // 比例点列表命令
                {
                    offset += 3;                                                    // 移动到数据起始位置
                    clearAutoRate();                                                // 清除旧的自动比例列表
                    int i = 0;                                                      // 循环计数器
                    while(i<len){                                                   // 遍历数据
                        int val = byte2Short(cmd,offset);                           // 解析比例点值
                        if(val != 0){                                               // 值有效
                            addAutoRate(val);                                       // 添加到自动比例列表
                        }
                        offset += 3;                                                // 移动到下一个数据点
                        i+= 3;                                                      // 更新计数器
                    }
                    EventFactory.sendEvent(new EventBase(EventFactory.EVENT_PROBE_EVENT, chIdx), true); // 发送事件通知
                }
                break;
                case ProbeCommand.TYPE_PROBE_RATE_LIST: {                           // 比例列表命令
                    offset += 3;                                                    // 移动到数据起始位置
                    int i = 0;                                                      // 循环计数器
                    clearScale();                                                   // 清除旧的比例列表
                    while (i < len) {                                               // 遍历数据
                        int val = byte2Int(cmd, offset);                            // 解析比例值
                        if (val != 0) {                                             // 值有效
                            if ((val & (1 << 31)) != 0) {                           // 检查倒数标志位
                                val &= 0x7FFFFFFF;                                  // 清除标志位
                                addScale(1.0/val);                                  // 添加倒数比例
                            } else {
                                addScale(val);                                      // 添加正数比例
                            }
                        }
                        offset += 5;                                                // 移动到下一个数据点
                        i += 5;                                                     // 更新计数器
                    }
                    EventFactory.sendEvent(new EventBase(EventFactory.EVENT_PROBE_EVENT, chIdx), true); // 发送事件通知
                }
                    break;
                case ProbeCommand.TYPE_BANDWIDTH:                                   // 带宽命令
                {
                    long bandwidth = byte2StringLong(cmd,offset+3,len);             // 解析带宽值（字符串形式）

                    if(bandwidth != 0) {                                            // 值有效
                        setBandWidth(bandwidth * 1000);                             // 设置带宽（kHz转Hz）
                    }
                }
                    break;
                case ProbeCommand.TYPE_PROBE_ALARM:                                 // 报警命令
                    alarm(cmd[offset + 3] & 0xFF);                                  // 发送报警事件
                    break;
                case (byte) 0xFF: {                                                 // 自动零点结果命令
                    boolean bSuccess = false;                                       // 成功标志
                    if (cmd[offset + 3] == 1) {                                     // 结果值为1表示成功
                        bSuccess = true;                                            // 设置成功标志
                    } else if (cmd[offset + 3] == 0) {                              // 结果值为0表示失败
                        bSuccess = false;                                           // 设置失败标志
                    }
                    if (bAutoZero) {                                                // 正在自动零点
                        bAutoZero = false;                                          // 清除自动零点标志
                        zero(bSuccess?ProbeNotifyInfo.ZERO_SUCCESS:ProbeNotifyInfo.ZERO_FAIL1); // 发送零点结果事件
                    }
                }
                default:                                                            // 其他命令类型
                    commandPrivate(cmd,offset);                                     // 调用私有命令处理
                    break;
            }
        }

    }

    /**
     * 处理私有命令
     * 处理特定探头的私有命令（零点、增益调整等）
     * 
     * <p><b>支持的私有命令：</b></p>
     * <ul>
     *   <li>TYPE_PROBE_ZERO: 零点命令</li>
     *   <li>TYPE_PROBE_ADJUST: 增益调整命令</li>
     * </ul>
     *
     * @param cmd 命令字节数组
     * @param offset 起始偏移量
     */
    protected void commandPrivate(byte[] cmd,int offset){

        if(cmd[offset] == (byte) 0xAA){                                             // 检查帧头
            byte type = cmd[offset + 1];                                            // 获取命令类型
            int len = cmd[offset + 2] & 0xFF;                                       // 获取数据长度
            switch (type) {
                case ProbeCommand.TYPE_PROBE_ZERO: {                                // 零点命令
                    if(len == 2) {                                                  // 长度为2：零点值
                        zeroVal = byte2Short(cmd, offset + 3);                      // 解析并保存零点值
                    }else if(len == 1){                                             // 长度为1：零点状态
                        zero(cmd[3 + offset]&0xFF);                                 // 发送零点状态事件

                    }
                    break;
                }
                case ProbeCommand.TYPE_PROBE_ADJUST:{                               // 增益调整命令
                    if(len == 1){                                                   // 长度为1：增益值
                        gain(cmd[3 + offset]&0xFF);                                 // 发送增益事件
                    }
                    break;
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 探头控制方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 恢复探头
     * 重新查询探头信息
     */
    public void resume(){
        sendCommand(ProbeCommand.probeCommand(ProbeCommand.TYPE_PROBE_INFO));       // 发送查询探头信息命令
    }

    /**
     * 设置默认参数
     * 查询探头版本信息
     */
    protected  void defaultParam(){
        sendCommand(ProbeCommand.QueryProbeVersion(),1000);                         // 延迟1秒查询探头版本
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 自动零点功能
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 自动零点标志
     * true: 正在进行自动零点
     * false: 未进行
     * 使用volatile保证多线程可见性
     */
    private volatile boolean  bAutoZero = false;

    /**
     * 执行自动零点
     * 启动探头的自动零点校准功能
     */
    public void autoZero(){
        synchronized (this) {                                                       // 同步保护
            bAutoZero = true;                                                       // 设置自动零点标志
        }
        sendCommand(ProbeCommand.probeAutoZeroCommand());                           // 发送自动零点命令
    }

    /**
     * 执行自动增益
     * 启动探头的自动增益调整功能
     */
    public void autoGain(){
        sendCommand(ProbeCommand.probeCommand(ProbeCommand.TYPE_PROBE_ADJUST));     // 发送自动增益命令
    }

    /**
     * 零点值增加1
     * 微调零点值
     */
    public void zeroAdd(){
        zeroVal++;                                                                  // 零点值加1
        sendCommand(ProbeCommand.probeZeroCommand(zeroVal));                        // 发送零点命令
    }

    /**
     * 零点值减少1
     * 微调零点值
     */
    public void zeroSub(){
        zeroVal--;                                                                  // 零点值减1
        sendCommand(ProbeCommand.probeZeroCommand(zeroVal));                        // 发送零点命令
    }

    /**
     * 零点值增加指定值
     * 微调零点值
     *
     * @param val 增加的值
     */
    public void zeroAdd( int val){
        zeroVal+= val;                                                              // 零点值增加
        sendCommand(ProbeCommand.probeZeroCommand(zeroVal));                        // 发送零点命令
    }

    /**
     * 设置待机模式
     *
     * @param bStandby true: 进入待机
     *                 false: 退出待机
     */
    public void standby(boolean bStandby){
        sendCommand(ProbeCommand.probeStandByCommand(bStandby?1:2));                // 发送待机命令（1:待机，2:唤醒）
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 探头比例方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取探头比例
     * 线程安全方法
     *
     * @return 探头比例值
     */
    public synchronized double getProbeRate(){
        return probeRate;                                                           // 返回探头比例
    }

    /**
     * 设置探头比例
     * 验证比例值有效性，并发送命令到探头
     *
     * @param probeRate 探头比例值
     */
    public void setProbeRate(double probeRate){
        if(baseBean != null && baseBean.getScaleIndex(probeRate) >= 0) {            // 检查比例值是否在支持列表中
            if(!DoubleUtil.FuzzyCompare(probeRate,this.probeRate)) {                // 检查值是否变化
                setProbeRate(probeRate, true);                                      // 更新比例值
                int val = 0;                                                        // 命令值
                if (probeRate >= 1) {                                               // 比例大于等于1
                    val = (int) (probeRate + 0.0000001);                            // 转换为整数
                } else {                                                            // 比例小于1
                    val = (int) (1.0 / probeRate + 0.0000001);                      // 转换为倒数
                    val |= 1 << 31;                                                 // 设置倒数标志位
                }
                sendCommand(ProbeCommand.probeRateCommand(val));                    // 发送比例命令
            }
        }
    }

    /**
     * 设置探头比例（内部方法）
     * 更新比例值并可选发送事件
     *
     * @param probeRate 探头比例值
     * @param bUser true: 用户操作，不发送事件
     *              false: 探头返回，发送事件
     */
    public void setProbeRate(double probeRate,boolean bUser){

        synchronized (this) {                                                       // 同步保护
            this.probeRate = probeRate;                                             // 更新比例值
        }
        if(!bUser){                                                                 // 非用户操作
            EventFactory.sendEvent(new EventBase(EventFactory.EVENT_CHANNEL_VSCALE,chIdx),true); // 发送垂直档位事件
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 命令发送方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 发送命令（无延迟）
     *
     * @param bytes 命令字节数组
     */
    protected void sendCommand(byte [] bytes){
        sendCommand(bytes,0);                                                       // 调用带延迟的方法，延迟为0
    }

    /**
     * 发送命令（带延迟）
     * 通过ScopeMessage发送命令到探头MCU
     *
     * @param bytes 命令字节数组
     * @param ms 延迟时间（毫秒）
     */
    protected void sendCommand(byte [] bytes,long ms){
        ScopeMessage scopeMessage = ScopeMessage.getInstance();                     // 获取消息管理实例
        scopeMessage.sendProbe(chIdx,bytes,ms);                                     // 发送探头命令
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 探头信息管理
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 探头信息映射表
     * 存储探头的类型、序列号、版本等信息
     */
    private Map<String, String> infoMap = null;

    /**
     * 设置探头信息映射表
     *
     * @param infoMap 信息映射表
     */
    public void setInfoMap(Map<String, String> infoMap) {
        this.infoMap = infoMap;                                                     // 保存信息映射表

    }

    /**
     * 根据键获取探头信息
     *
     * @param key 信息键
     * @return 信息值，未找到返回空字符串
     */
    public String getValue(String key){
        String s = "";                                                              // 初始化返回值
        if(infoMap != null){                                                        // 映射表存在
            if(infoMap.containsKey(key)){                                           // 键存在
                s = infoMap.get(key);                                               // 获取值
            }
        }
        return s;                                                                   // 返回信息值
    }

    /**
     * 获取探头类型
     *
     * @return 探头类型字符串
     */
    public String getType(){return getValue(PROBE_TYPE_KEY);}                       // 返回类型信息

    /**
     * 获取探头序列号
     *
     * @return 序列号字符串
     */
    public String getSN(){
        return getValue(PROBE_SN_KEY);                                              // 返回序列号
    }

    /**
     * 获取探头版本
     *
     * @return 版本字符串
     */
    public String getVersion(){
        return getValue(PROBE_VER_KEY);                                             // 返回版本信息
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 探头类型键
     * 用于从infoMap中获取探头类型
     */
    public static final String PROBE_TYPE_KEY = "TYPE";

    /**
     * 探头序列号键
     * 用于从infoMap中获取探头序列号
     */
    public static final String PROBE_SN_KEY = "SN";

    /**
     * 探头版本键
     * 用于从infoMap中获取探头版本
     */
    public static final String PROBE_VER_KEY = "VER";

    /**
     * 探头信息键数组
     * 包含所有探头信息键
     */
    public static final String [] PROBE_INFO_KEYS = {
            PROBE_TYPE_KEY,PROBE_SN_KEY,PROBE_VER_KEY
    };

    // ═══════════════════════════════════════════════════════════════════════════════
    // MCU数量方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 获取探头MCU数量
     * 默认返回1，多MCU探头（如MDP）应重写此方法
     *
     * @return MCU数量
     */
    public int getMcuNums(){
        return 1;                                                                   // 默认单MCU
    }
}
