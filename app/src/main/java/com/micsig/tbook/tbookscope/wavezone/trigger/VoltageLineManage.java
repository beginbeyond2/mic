package com.micsig.tbook.tbookscope.wavezone.trigger; // 触发线模块包

import android.graphics.Canvas; // 导入Canvas类，用于绘制
import android.util.Log; // 导入Log类，用于日志输出

import com.chillingvan.canvasgl.ICanvasGL; // 导入GLCanvas接口，用于OpenGL绘制
import com.micsig.base.Logger; // 导入日志工具类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类
import com.micsig.tbook.scope.channel.SerialChannel; // 导入串行通道类
import com.micsig.tbook.tbookscope.main.mainbottom.MainTopMsgRightGone; // 导入顶部右侧隐藏消息类
import com.micsig.tbook.tbookscope.middleware.Tag; // 导入标签常量类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.Tools; // 导入工具类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 导入工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean; // 导入工作模式消息Bean
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage; // 导入波形管理类
import com.micsig.tbook.ui.MTriggerLevel; // 导入触发电平常量类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道号常量类

import java.util.ArrayList; // 导入ArrayList
import java.util.HashMap; // 导入HashMap
import java.util.List; // 导入List接口
import java.util.Map; // 导入Map接口

import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava Consumer接口


/**
 * Created by liwb on 2017/5/15.
 */

/*
 * +=============================================================================+
 * |                        VoltageLineManage                                    |
 * +=============================================================================+
 * | 模块定位   : 示波器波形显示区域 - 电平线管理器（统一管理触发电平线和预值电平线）|
 * | 核心职责   : 管理多条电平线(Trigger/Value1~4)的创建、激活、显示、绘制和合并  |
 * | 架构设计   : 单例模式，使用 HashMap 存储各类电平线，实现 IWorkMode 接口      |
 * |            : 支持预值电平线的合并显示和通道间同步                            |
 * | 数据流向   : 外部操作 → setActive/setDiscreetVoltageShowState →             |
 * |            : 电平线激活/可见性切换 → mergeDiscreetVoltageLine合并 →          |
 * |            : 按前后层级绘制到GLCanvas                                      |
 * | 依赖关系   : IWorkMode(接口), TriggerVoltageLine, DiscreetVoltageLine,      |
 * |            : ChannelFactory, RxBus, CacheUtil, WaveManage, TChan            |
 * | 使用场景   : 切换触发电平线通道、显示/隐藏预值电平线、预值电平线间导航、     |
 * |            : 工作模式切换时刷新所有电平线                                   |
 * +=============================================================================+
 */
public class VoltageLineManage implements IWorkMode {

    private static final String TAG = "VoltageLineManage"; // 日志标签

    //region 单例创建
    /**
     * 电平线管理器单例持有者（静态内部类实现懒加载单例）
     */
    public static class TriggerVoltageManageHolder {
        public static final VoltageLineManage instance = new VoltageLineManage(); // 单例实例
    }

    /**
     * 获取电平线管理器单例实例
     * @return VoltageLineManage单例
     */
    public static VoltageLineManage getInstance() {
        return TriggerVoltageManageHolder.instance; // 返回单例
    }
    //endregion

    public static final String VoltageLineType_Trigger = "Trigger"; // 触发电平线类型标识
    public static final String VoltageLineType_Value1 = "Value1"; // 预值电平线1类型标识
    public static final String VoltageLineType_Value2 = "Value2"; // 预值电平线2类型标识
    public static final String VoltageLineType_Value3 = "Value3"; // 预值电平线3类型标识
    public static final String VoltageLineType_Value4 = "Value4"; // 预值电平线4类型标识

    //显示预值电平的通道
    private static final int ShowDiscreetVoltage_Value1 = 0x01; // 显示预值电平1标志
    private static final int ShowDiscreetVoltage_Value2 = 0x02; // 显示预值电平2标志
    private static final int ShowDiscreetVoltage_ALL = 0xFF; // 显示所有预值电平标志

    //region 属性
    private Map<String, ITriggerLine> mapVoltageLine; // 电平线映射表（类型名 → 电平线对象）
    private String key; // 当前操作的电平线类型key

    private  final String[] type=new String[]{VoltageLineType_Trigger,VoltageLineType_Value1,VoltageLineType_Value2,VoltageLineType_Value3,VoltageLineType_Value4}; // 电平线类型数组（索引对应串行通道号）
//    private int currShowDiscreetVoltage = ShowDiscreetVoltage_ALL;
    private String frontDiscreetVoltageLine = VoltageLineType_Value1; // 最前显示的预值电平线类型（绘制在最上层）
    private ArrayList<DiscreetVoltageLineInfoBean> listDiscreetVoltageLineInfo = new ArrayList<>(); // 预值电平线信息列表
    private int listDiscreetInfoIndex = 0; // 当前预值电平线信息索引
    private String CurrOptionVoltageLineType = VoltageLineType_Value1; // 当前操作的预值电平线类型

    /**
     * 设置当前操作的电平线类型key
     * @param key 电平线类型标识
     */
    public void setKey(String key) {
        this.key = key; // 赋值key
    }

    /**
     * 获取当前操作的电平线类型key
     * @return 电平线类型标识
     */
    public String getKey() {
        return key; // 返回key
    }

    /**
     * 空初始化方法（预留扩展）
     */
    public void init() {
    }

    /**
     * 获取电平线映射表
     * @return 电平线映射表
     */
    public Map<String, ITriggerLine> getMapVoltageLine() {
        return mapVoltageLine; // 返回映射表
    }

    //region IWorkMode接口
    private
    @WorkMode
    int mWorkMode = IWorkMode.WorkMode_YT; // 当前工作模式

    /**
     * 切换工作模式，通知所有电平线切换
     * @param workMode 目标工作模式
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) {
        if (mWorkMode == workMode) return; // 模式未变，无需处理
        this.mWorkMode = workMode; // 更新工作模式
//        mapVoltageLine.get(VoltageLineType_Trigger).switchWorkMode(workMode);
//        mapVoltageLine.get(VoltageLineType_Value1).switchWorkMode(workMode);
//        mapVoltageLine.get(VoltageLineType_Value2).switchWorkMode(workMode);
//        mapVoltageLine.get(VoltageLineType_Value3).switchWorkMode(workMode);
//        mapVoltageLine.get(VoltageLineType_Value4).switchWorkMode(workMode);
        for (Map.Entry value : mapVoltageLine.entrySet()) { // 遍历所有电平线
            ITriggerLine v = (ITriggerLine) value.getValue(); // 获取电平线对象
            v.switchWorkMode(workMode); // 通知切换工作模式
        }
    }

    //endregion

    /**
     * 设置当前激活的电平线
     * 非触发电平线时，使用当前预值电平线的key
     * @param key 电平线类型标识
     */
    public void setActive(String key) {
        if (!VoltageLineType_Trigger.equals(key)) { // 非触发电平线
            String s = getDiscreetVoltageLineCurKey(); // 获取当前预值电平线key
            if (s != null) { // key有效
                key = s; // 使用预值电平线key
            }
        }
        Log.d(TAG, "setActive() called with: key = [" + key + "]"); // 打印调试日志
        for (Map.Entry<String, ITriggerLine> entry : mapVoltageLine.entrySet()) { // 遍历所有电平线
            Logger.d(TAG, "" + entry.getKey()); // 打印电平线key
            if (key.equals(entry.getKey())) { // 匹配目标key
                entry.getValue().setActive(true); // 激活该电平线
            } else { // 非目标key
                entry.getValue().setActive(false); // 取消激活
            }
        }
    }

    /**
     * 遍历所有预值电平线执行操作
     * @param action 对每条预值电平线执行的操作
     * @param continuePredicate 返回true时跳过当前线（continue语义）
     */
    public void foreachDiscreetLine(java.util.function.Consumer<DiscreetVoltageLine> action, java.util.function.Predicate<DiscreetVoltageLine> continuePredicate)  {
        for(Map.Entry<String,ITriggerLine> entry:mapVoltageLine.entrySet()){ // 遍历所有电平线
            if (entry.getKey().equals(VoltageLineType_Trigger)) continue; // 跳过触发电平线
            DiscreetVoltageLine line= (DiscreetVoltageLine) mapVoltageLine.get(entry.getKey()); // 获取预值电平线对象
            if (continuePredicate.test(line)){ // 满足跳过条件
                continue; // 跳过
            }
            action.accept(line); // 执行操作
        }
    }

    /**
     * 根据串行通道序号获取电平线类型key
     * @param serialNo 串行通道序号（0=Trigger, 1=Value1, ...）
     * @return 电平线类型key
     */
    public String getSerialKey(int serialNo){
        return type[serialNo]; // 返回类型数组对应元素
    }

    /**
     * 根据串行序号获取电平线对象
     * @param serialNum 串行通道序号
     * @return 电平线对象，不存在则返回null
     */
    public ITriggerLine getVoltageLine(int serialNum){
        String mTypeKey= type[serialNum]; // 获取类型key
        return mapVoltageLine.getOrDefault(mTypeKey,null); // 从映射表获取，不存在返回null
    }

    /***
     * 返回指定类
     * @param key  触发电平：VoltageLineType_Trigger="Trigger"
     *              预值电平： VoltageLineType_Value="Value"
     * @return 返回类
     */
    public ITriggerLine getVoltageLine(String key) {
        this.key = key; // 保存当前操作的key
        if (mapVoltageLine.get(key) != null) { // 映射表中存在该key
            return mapVoltageLine.get(key); // 返回电平线对象
        } else { // 不存在
            return null; // 返回null
        }
    }


    /***
     * 返回指定类
     * @param curKey  预值电平： VoltageLineType_Value1="Value1" or VoltageLineType_Value2="Value2"
     * @return 返回类
     */
    public DiscreetVoltageLine getOtherValueLine(String curKey) {
        switch (key) { // 根据当前key切换到下一个预值电平线
            case VoltageLineType_Value1: // 当前是Value1
                key = VoltageLineType_Value2; // 切换到Value2
                break;
            case VoltageLineType_Value2: // 当前是Value2
                key = VoltageLineType_Value3; // 切换到Value3
                break;
            case VoltageLineType_Value3: // 当前是Value3
                key = VoltageLineType_Value4; // 切换到Value4
                break;
            case VoltageLineType_Value4: // 当前是Value4
                key = VoltageLineType_Value1; // 循环回Value1
                break;
            default: // 其他情况
                return null; // 返回null
        }
        if (mapVoltageLine.get(key) != null) { // 映射表中存在目标key
            return (DiscreetVoltageLine) mapVoltageLine.get(key); // 返回预值电平线对象
        } else { // 不存在
            return null; // 返回null
        }
    }

    /***
     * 返回类  使用前要保存最后调用过一次setKey(String key)函数，否则返回空
     * @return 电平线对象
     */
    public ITriggerLine getVoltageLine() {
        return this.getVoltageLine(this.key); // 使用保存的key获取电平线
    }
    //endregion

    /**
     * 构造函数：创建所有电平线对象并注册RxBus事件订阅
     */
    public VoltageLineManage() {
        mapVoltageLine = new HashMap<>(); // 初始化映射表
        TriggerVoltageLine voltage = new TriggerVoltageLine(VoltageLineType_Trigger); // 创建触发电平线
        mapVoltageLine.put(VoltageLineType_Trigger, voltage); // 存入映射表
        DiscreetVoltageLine discreet1 = new DiscreetVoltageLine(VoltageLineType_Value1); // 创建预值电平线1
        mapVoltageLine.put(VoltageLineType_Value1, discreet1); // 存入映射表
        DiscreetVoltageLine discreet2 = new DiscreetVoltageLine(VoltageLineType_Value2); // 创建预值电平线2
        mapVoltageLine.put(VoltageLineType_Value2, discreet2); // 存入映射表
        DiscreetVoltageLine discreet3 = new DiscreetVoltageLine(VoltageLineType_Value3); // 创建预值电平线3
        mapVoltageLine.put(VoltageLineType_Value3, discreet3); // 存入映射表
        DiscreetVoltageLine discreet4 = new DiscreetVoltageLine(VoltageLineType_Value4); // 创建预值电平线4
        mapVoltageLine.put(VoltageLineType_Value4, discreet4); // 存入映射表

        RxBus.getInstance().getObservable(RxEnum.MAIN_TOPRIGHT_GONE).subscribe(consumerTopRightGone); // 订阅顶部右侧隐藏事件
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(ConsumerWorkModeChange); // 订阅工作模式切换事件
    }

    /**
     * 顶部右侧隐藏事件消费者
     * 控制所有电平线的可见性
     */
    private Consumer<MainTopMsgRightGone> consumerTopRightGone = new Consumer<MainTopMsgRightGone>() {
        @Override
        public void accept(MainTopMsgRightGone mainTopMsgRightGone) throws Exception {
            setAllLineVisible(mainTopMsgRightGone.isVisible()); // 设置所有电平线可见性
        }
    };

    /**
     * 工作模式切换事件消费者
     * 刷新所有电平线并恢复串行通道缓存位置
     */
    private Consumer<WorkModeBean> ConsumerWorkModeChange = new Consumer<WorkModeBean>() {
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception {
            for (String key : mapVoltageLine.keySet()) { // 遍历所有电平线
                ITriggerLine line = mapVoltageLine.get(key); // 获取电平线对象
                line.refresh(); // 刷新电平线
                int idx = 0; // 串行通道索引
                switch (key) { // 根据电平线类型确定串行通道索引
                    case VoltageLineType_Value1: // 预值电平1
                        idx = ChannelFactory.S1; // 对应串行通道1
                        break;
                    case VoltageLineType_Value2: // 预值电平2
                        idx = ChannelFactory.S2; // 对应串行通道2
                        break;
                    case VoltageLineType_Value3: // 预值电平3
                        idx = ChannelFactory.S3; // 对应串行通道3
                        break;
                    case VoltageLineType_Value4: // 预值电平4
                        idx = ChannelFactory.S4; // 对应串行通道4
                        break;
                }
                SerialChannel channel = ChannelFactory.getSerialChannel(idx); // 获取串行通道对象
                if (idx != 0 && channel != null) { // 串行通道有效
                    channel.setPos(Tools.getLevelCache(CacheUtil.VALUE_CHANNEL + line.getChannelId())); // 从缓存恢复串行通道位置
                }
            }
        }
    };

    /**
     * 设置所有电平线的可见性
     * @param visible 是否可见
     */
    public void setAllLineVisible(boolean visible) {
        for (ITriggerLine line : mapVoltageLine.values()) { // 遍历所有电平线
            line.setVisible(visible); // 设置可见性
        }
    }


    //region 预值电平合并显示

    /**
     * 合并显示预值电平线
     * 将当前电平线与所有其他已显示的预值电平线合并，处理通道重叠
     * @param curLine 当前电平线类型key
     */
    private synchronized void mergeDiscreetVoltageLine(String curLine) {
//        Log.d(Tag.Debug, String.format("VoltageLineManage.mergeDiscreetVoltageLine: " ));
        ITriggerLine line= mapVoltageLine.get(curLine); // 获取当前电平线对象
        listDiscreetVoltageLineInfo.clear(); // 清空信息列表
        listDiscreetVoltageLineInfo.addAll(getVoltageLine(curLine).getShowChannelInfo()); // 添加当前电平线的通道信息
        for (Map.Entry<String, ITriggerLine> entry : mapVoltageLine.entrySet()) { // 遍历所有电平线
            if (entry.getKey().equals(VoltageLineType_Trigger)) continue; // 跳过触发电平线
            if (line==mapVoltageLine.get(entry.getValue())) continue; // 跳过自身
            if (getVoltageLine(entry.getKey()).getShowState() ==false) continue; // 跳过未显示的电平线

            List<DiscreetVoltageLineInfoBean> list = getVoltageLine(entry.getKey()).getShowChannelInfo(); // 获取该电平线的通道信息列表
            for (int j = 0; j < list.size(); j++){ // 遍历该电平线的每个通道
                    DiscreetVoltageLineInfoBean desBean = list.get(j); // 获取目标通道信息
                    DiscreetVoltageLine desLine = (DiscreetVoltageLine) mapVoltageLine.get(desBean.VoltageLineName); // 获取目标预值电平线
                    setDiscreetVoltageState(desBean, false); // 先设置为不绘制

                    int idx= Tools.indexOf(listDiscreetVoltageLineInfo,(src)->src.ChannelId==desBean.ChannelId
                            && src.VoltageLineChannelIndex == desBean.VoltageLineChannelIndex); // 在已合并列表中查找相同通道的Bean
                    DiscreetVoltageLineInfoBean srcBean=null; // 源Bean
                    DiscreetVoltageLine srcLine =null; // 源预值电平线
                    if (idx!=-1) { // 找到相同通道的Bean
                        srcBean = listDiscreetVoltageLineInfo.get(idx); // 获取源Bean
                        srcLine= (DiscreetVoltageLine) mapVoltageLine.get(srcBean.VoltageLineName); // 获取源预值电平线
                    }
                    if (idx==-1){ // 没有找到相同通道，直接添加
                        listDiscreetVoltageLineInfo.add(desBean); // 添加到合并列表
                    }else if ((srcBean.ShowMode == ITriggerLine.ShowMode_Two && desBean.ShowMode != ITriggerLine.ShowMode_Two && srcBean.ChannelId == desBean.ChannelId)){
//                        syncDiscreetLineSrcTwo(srcLine, desLine); // 源为双线模式，目标为单线模式，同步
                    }else if ((srcBean.ShowMode != ITriggerLine.ShowMode_Two && desBean.ShowMode == ITriggerLine.ShowMode_Two && srcBean.ChannelId == desBean.ChannelId)){
//                        syncDiscreetLineDesTwo(srcLine, desLine); // 源为单线模式，目标为双线模式，同步
                    }else if ((srcBean.ShowMode != ITriggerLine.ShowMode_Two && desBean.ShowMode != ITriggerLine.ShowMode_Two && srcBean.ChannelId == desBean.ChannelId)){
//                        syncDiscreetLine(srcLine, desLine); // 均为单线模式，同步
                    }
            }
        }

        for (int i = 0; i < listDiscreetVoltageLineInfo.size(); i++) { // 遍历合并后的列表
            DiscreetVoltageLineInfoBean bean=listDiscreetVoltageLineInfo.get(i); // 获取Bean
            setDiscreetVoltageState(bean, true); // 设置为绘制状态
//            Log.d(Tag.Debug, String.format("VoltageLineManage.mergeDiscreetVoltageLine line: %s",listDiscreetVoltageLineInfo.get(i) ));
        }
        listDiscreetInfoIndex = 0; // 重置索引
    }




    /**
     * 同步预值电平线（源为双线模式，目标为单线模式）
     * @param src 源预值电平线
     * @param des 目标预值电平线
     */
    private void syncDiscreetLineSrcTwo(DiscreetVoltageLine src, DiscreetVoltageLine des) {
        double high= WaveManage.get().getPositionYButWorkModeXY(src.getChannelId()) - Tools.getLevelCache(CacheUtil.VALUE_CHANNEL_H + src.getChannelId());; // 计算高电平位置
        double low =WaveManage.get().getPositionYButWorkModeXY(src.getChannelId()) - Tools.getLevelCache(CacheUtil.VALUE_CHANNEL + src.getChannelId());; // 计算低电平位置
        src.setCurrYIndex(ITriggerLine.VoltageLine_High); // 切换到高电平索引
        src.setOtherY(ITriggerLine.VoltageLine_High,high); // 设置高电平位置
        src.setCurrYIndex(ITriggerLine.VoltageLine_Low); // 切换到低电平索引
        src.setOtherY(ITriggerLine.VoltageLine_Low,low); // 设置低电平位置
        src.setVisibleLine(false); // 隐藏横线

        high=src.getOtherY(ITriggerLine.VoltageLine_High); // 获取源高电平位置
        low=src.getOtherY(ITriggerLine.VoltageLine_Low); // 获取源低电平位置
        if (des.getShowMode()!=ITriggerLine.ShowMode_Two){ // 目标非双线模式
            if (des.getOtherY(src.getChannelId())!=low){ // 目标低电平位置不同
                des.setOtherY(src.getChannelId(),low); // 同步低电平位置
            }
        }else { // 目标为双线模式
            if (des.getOtherY(ITriggerLine.VoltageLine_High)!=high
                && des.getChannelId()==src.getChannelId()){ // 目标高电平位置不同且同通道
                des.setOtherY(ITriggerLine.VoltageLine_High,high); // 同步高电平位置
            }
            if (des.getOtherY(ITriggerLine.VoltageLine_Low)!=low
                && des.getChannelId()==src.getChannelId()){ // 目标低电平位置不同且同通道
                des.setOtherY(ITriggerLine.VoltageLine_Low,low); // 同步低电平位置
            }
        }
    }

    /**
     * 同步预值电平线（目标为双线模式，源为单线模式）
     * @param src 源预值电平线
     * @param des 目标预值电平线
     */
    private void syncDiscreetLineDesTwo(DiscreetVoltageLine src, DiscreetVoltageLine des) {
        TChan.foreachChan((chan)->{ // 遍历所有通道
            if (des.getOtherY(ITriggerLine.VoltageLine_Low)!=src.getOtherY(chan)
                && src.getTriggerVoltageLine_logic_state()[chan]!=MTriggerLevel.TriggerLevel_Mode_work_Logic_None){ // 目标低电平位置不同且源通道参与逻辑
                des.setOtherY(ITriggerLine.VoltageLine_Low,src.getOtherY(chan)); // 同步位置
            }
        });
    }

    /**
     * 同步预值电平线（均为单线模式）
     * @param src 源预值电平线
     * @param des 目标预值电平线
     */
    private void syncDiscreetLine(DiscreetVoltageLine src, DiscreetVoltageLine des) {
        TChan.foreachChan((chan)->{ // 遍历所有通道
            double pCh = WaveManage.get().getPositionY(chan)-Tools.getLevelCache(CacheUtil.VALUE_CHANNEL+chan); // 计算源通道位置
            src.setOtherY(chan,pCh); // 设置源通道位置
            if (des.getOtherY(chan)!=src.getOtherY(chan)
                    && src.getTriggerVoltageLine_logic_state()[chan]!= MTriggerLevel.TriggerLevel_Mode_work_Logic_None){ // 目标位置不同且源通道参与逻辑
                des.setOtherY(chan,src.getOtherY(chan)); // 同步位置
            }
        });

    }


    //返回所有预值电平的通道
    /**
     * 获取所有预值电平线的通道信息并去重
     * 同一通道只保留当前操作的预值电平线
     * @param CurrOptionVoltageLineType 当前操作的预值电平线类型
     */
    private void getAllDiscreetVoltageLineChannelIdInS1S2(String CurrOptionVoltageLineType) {
        synchronized (listDiscreetVoltageLineInfo) { // 同步锁
            listDiscreetVoltageLineInfo.clear(); // 清空列表
            if (getVoltageLine(VoltageLineType_Value1).getShowState()) { // Value1已显示
                listDiscreetVoltageLineInfo = getVoltageLine(VoltageLineType_Value1).getShowChannelInfo(); // 添加Value1通道信息
            }
            if (getVoltageLine(VoltageLineType_Value2).getShowState()) { // Value2已显示
                listDiscreetVoltageLineInfo.addAll(getVoltageLine(VoltageLineType_Value2).getShowChannelInfo()); // 添加Value2通道信息
            }
            if (getVoltageLine(VoltageLineType_Value3).getShowState()) { // Value3已显示
                listDiscreetVoltageLineInfo.addAll(getVoltageLine(VoltageLineType_Value3).getShowChannelInfo()); // 添加Value3通道信息
            }
            if (getVoltageLine(VoltageLineType_Value4).getShowState()) { // Value4已显示
                listDiscreetVoltageLineInfo.addAll(getVoltageLine(VoltageLineType_Value4).getShowChannelInfo()); // 添加Value4通道信息
            }

            if (listDiscreetVoltageLineInfo.size() <= 0) { // 列表为空
                return; // 直接返回
            }
            //过滤重复的通道
            for (int i = 0; i < listDiscreetVoltageLineInfo.size(); i++) { // 遍历列表
                DiscreetVoltageLineInfoBean bean1 = listDiscreetVoltageLineInfo.get(i); // 获取当前Bean

                for (int j = listDiscreetVoltageLineInfo.size() - 1; j > i; j--) { // 从后向前遍历
                    DiscreetVoltageLineInfoBean bean2 = listDiscreetVoltageLineInfo.get(j); // 获取比较Bean

                    if (bean1.ShowMode == ITriggerLine.ShowMode_Two && bean2.ShowMode != ITriggerLine.ShowMode_Two && bean1.ChannelId == bean2.ChannelId) { // 当前双线，比较单线，同通道
                        if (bean1.VoltageLineName.equals(CurrOptionVoltageLineType)) { // 当前是操作线
                            setDiscreetVoltageState(listDiscreetVoltageLineInfo.get(j), false); // 隐藏比较线
                            listDiscreetVoltageLineInfo.remove(j); // 移除比较线
                        } else { // 比较线是操作线
                            setDiscreetVoltageState(listDiscreetVoltageLineInfo.get(i), false); // 隐藏当前线
                            listDiscreetVoltageLineInfo.remove(i); // 移除当前线
                        }
                        break; // 退出内层循环
                    }
                    if (bean2.ShowMode == ITriggerLine.ShowMode_Two && bean1.ShowMode != ITriggerLine.ShowMode_Two && bean2.ChannelId == bean1.ChannelId) { // 比较双线，当前单线，同通道
                        if (bean2.VoltageLineName.equals(CurrOptionVoltageLineType)) { // 比较线是操作线
                            setDiscreetVoltageState(listDiscreetVoltageLineInfo.get(i), false); // 隐藏当前线
                            listDiscreetVoltageLineInfo.remove(i); // 移除当前线
                        } else { // 当前线是操作线
                            setDiscreetVoltageState(listDiscreetVoltageLineInfo.get(j), false); // 隐藏比较线
                            listDiscreetVoltageLineInfo.remove(j); // 移除比较线
                        }
                        break; // 退出内层循环
                    }
                    if (bean1.ChannelId == bean2.ChannelId && bean1.ShowMode != ITriggerLine.ShowMode_Two && bean2.ShowMode != ITriggerLine.ShowMode_Two) { // 同通道，均为单线模式
                        if (bean1.VoltageLineName.equals(CurrOptionVoltageLineType)) { // 当前线是操作线
                            setDiscreetVoltageState(listDiscreetVoltageLineInfo.get(j), false); // 隐藏比较线
                            listDiscreetVoltageLineInfo.remove(j); // 移除比较线
                        } else { // 比较线是操作线
                            setDiscreetVoltageState(listDiscreetVoltageLineInfo.get(i), false); // 隐藏当前线
                            listDiscreetVoltageLineInfo.remove(i); // 移除当前线
                        }
                        break; // 退出内层循环
                    }
                }
            }
            //剩下都是要显示的
            for (int i = 0; i < listDiscreetVoltageLineInfo.size(); i++) { // 遍历剩余列表
                setDiscreetVoltageState(listDiscreetVoltageLineInfo.get(i), true); // 设置为绘制状态
            }
            listDiscreetInfoIndex = 0; // 重置索引
        }
    }

    /**
     * 设置阈值电平是否显示
     *
     * @param infoBean 电平线信息Bean
     * @param drawChannel 是否绘制该通道
     */
    private void setDiscreetVoltageState(DiscreetVoltageLineInfoBean infoBean, boolean drawChannel) {
        int state = DiscreetVoltageLine.TriggerVoltageLine_Logic_Hight; // 默认状态为高电平
        if (drawChannel == false) state = DiscreetVoltageLine.TriggerVoltageLine_NoDraw; // 不绘制时设置为NoDraw
        if(infoBean.ShowMode == ITriggerLine.ShowMode_Two){ // 双线模式
            ((DiscreetVoltageLine) getVoltageLine(infoBean.VoltageLineName)).setTriggerVoltageLine_logic_state(infoBean.VoltageLineChannelIndex, state); // 按电平线索引设置状态
        }else { // 单线模式
            ((DiscreetVoltageLine) getVoltageLine(infoBean.VoltageLineName)).setTriggerVoltageLine_logic_state(infoBean.ChannelId, state); // 按通道ID设置状态
        }
    }

    /**
     * 获取当前预值电平线信息
     * @return 当前索引处的预值电平线信息Bean，索引越界返回null
     */
    public DiscreetVoltageLineInfoBean getCurrDisCreetVoltageLineInfo() {
        synchronized (listDiscreetVoltageLineInfo) { // 同步锁
            if (listDiscreetInfoIndex >= listDiscreetVoltageLineInfo.size() || listDiscreetInfoIndex < 0) // 索引越界
                return null; // 返回null
            return listDiscreetVoltageLineInfo.get(listDiscreetInfoIndex); // 返回当前索引处的Bean
        }
    }

    /**
     * 判断是否在第一个预值电平线
     * @return 索引是否为0
     */
    public boolean isFirstVoltageLine() {
        return listDiscreetInfoIndex == 0; // 判断索引是否为0
    }

    /**
     * 判断是否在最后一个预值电平线
     * @return 索引是否为最后一个
     */
    public boolean isLastVoltageLine() {
        return listDiscreetInfoIndex == (listDiscreetVoltageLineInfo.size() - 1); // 判断索引是否为最后
    }

    /**
     * 获取预值电平线总数
     * @return 预值电平线信息列表大小
     */
    public int getDiscreetVoltageLineInS1S2Count() {
        return listDiscreetVoltageLineInfo.size(); // 返回列表大小
    }

    /**
     * 获取当前预值电平线的类型key
     * @return 当前预值电平线类型key，索引越界返回null
     */
    public String getDiscreetVoltageLineCurKey() {
        synchronized (listDiscreetVoltageLineInfo) { // 同步锁
            if (listDiscreetInfoIndex >= 0 && listDiscreetInfoIndex < listDiscreetVoltageLineInfo.size()) { // 索引有效
                DiscreetVoltageLineInfoBean bean = listDiscreetVoltageLineInfo.get(listDiscreetInfoIndex); // 获取当前Bean
                return bean.VoltageLineName; // 返回类型key
            }
        }
        return null; // 索引越界返回null
    }

    /**
     * 切换到上一个预值电平线
     * 索引循环递减，更新当前通道索引和前后显示层级
     * @return 当前预值电平线信息Bean
     */
    public DiscreetVoltageLineInfoBean setPreDiscreetVoltageLineInS1S2() {
        synchronized (listDiscreetVoltageLineInfo) { // 同步锁
            if (listDiscreetVoltageLineInfo.size() <= 0) return null; // 列表为空返回null
            listDiscreetInfoIndex--; // 索引递减
            if (listDiscreetInfoIndex < 0) // 索引小于0
                listDiscreetInfoIndex = listDiscreetVoltageLineInfo.size() - 1; // 循环到最后一个

            DiscreetVoltageLineInfoBean bean = listDiscreetVoltageLineInfo.get(listDiscreetInfoIndex); // 获取当前Bean
            setDiscreetVoltageFrontShow(bean.VoltageLineName); // 设置为最前显示
            if (bean.ShowMode == ITriggerLine.ShowMode_Two) { // 双线模式
                getVoltageLine(bean.VoltageLineName).setCurrYIndex(bean.VoltageLineChannelIndex); // 设置电平线索引
            } else { // 单线模式
                getVoltageLine(bean.VoltageLineName).setCurrYIndex(bean.ChannelId); // 设置通道索引
            }
//          Logger.i(TAG, "预值电平大小：" + listDiscreetVoltageLineInfo.size());
//        Logger.i(TAG,"id:"+bean.ChannelId+" 通道号："+bean.VoltageLineName+" 类型 ："+bean.VoltageLineChannelIndex+" showMode:"+bean.ShowMode);
            if (bean.VoltageLineName.equals(VoltageLineType_Value1)) { // 当前是Value1
                getVoltageLine(VoltageLineType_Value2).setCurrYIndex(0); // 重置Value2索引
            } else if (bean.VoltageLineName.equals(VoltageLineType_Value2)) { // 当前是Value2
                getVoltageLine(VoltageLineType_Value1).setCurrYIndex(0); // 重置Value1索引
            }
            getVoltageLine(VoltageLineType_Value1).setVisibleLine(false); // 隐藏Value1横线
            getVoltageLine(VoltageLineType_Value2).setVisibleLine(false); // 隐藏Value2横线

            Logger.d(TAG, "Pre:" + bean.toString()); // 打印调试日志
            return bean; // 返回当前Bean
        }
    }

    /**
     * 切换到下一个预值电平线
     * 索引循环递增，更新当前通道索引和前后显示层级
     * @return 当前预值电平线信息Bean
     */
    public DiscreetVoltageLineInfoBean setNextDiscreetVoltageLineInS1S2() {
        synchronized (listDiscreetVoltageLineInfo) { // 同步锁
            if (listDiscreetVoltageLineInfo.size() <= 0) return null; // 列表为空返回null
            listDiscreetInfoIndex++; // 索引递增
            if (listDiscreetInfoIndex >= listDiscreetVoltageLineInfo.size()) // 索引超出范围
                listDiscreetInfoIndex = 0; // 循环到第一个
            DiscreetVoltageLineInfoBean bean = listDiscreetVoltageLineInfo.get(listDiscreetInfoIndex); // 获取当前Bean
            setDiscreetVoltageFrontShow(bean.VoltageLineName); // 设置为最前显示
            if (bean.ShowMode == ITriggerLine.ShowMode_Two) { // 双线模式
                getVoltageLine(bean.VoltageLineName).setCurrYIndex(bean.VoltageLineChannelIndex); // 设置电平线索引
            } else { // 单线模式
                getVoltageLine(bean.VoltageLineName).setCurrYIndex(bean.ChannelId); // 设置通道索引
            }

            if (bean.VoltageLineName.equals(VoltageLineType_Value1)) { // 当前是Value1
                getVoltageLine(VoltageLineType_Value2).setCurrYIndex(0); // 重置Value2索引
            } else if (bean.VoltageLineName.equals(VoltageLineType_Value2)) { // 当前是Value2
                getVoltageLine(VoltageLineType_Value1).setCurrYIndex(0); // 重置Value1索引
            }

            Logger.d(TAG, "Next:" + bean.toString()); // 打印调试日志
            return bean; // 返回当前Bean
        }
    }

    /**
     * 设置当前预值电平线（将s1、s2的图标统一成一套）
     * 索引钳位后更新当前通道索引和前后显示层级
     * @return 当前预值电平线信息Bean
     */
    public DiscreetVoltageLineInfoBean setCurrDiscreetVoltageLineInS1S2() {
        synchronized (listDiscreetVoltageLineInfo) { // 同步锁
            if (listDiscreetVoltageLineInfo.size() <= 0) return null; // 列表为空返回null
            if (listDiscreetInfoIndex >= listDiscreetVoltageLineInfo.size()) // 索引超出范围
                listDiscreetInfoIndex = 0; // 重置为0
            else if (listDiscreetInfoIndex < 0) // 索引小于0
                listDiscreetInfoIndex = listDiscreetVoltageLineInfo.size() - 1; // 重置为最后一个


            DiscreetVoltageLineInfoBean bean = listDiscreetVoltageLineInfo.get(listDiscreetInfoIndex); // 获取当前Bean
            setDiscreetVoltageFrontShow(bean.VoltageLineName); // 设置为最前显示
            if (bean.ShowMode == ITriggerLine.ShowMode_Two) { // 双线模式
                getVoltageLine(bean.VoltageLineName).setCurrYIndex(bean.VoltageLineChannelIndex); // 设置电平线索引
            } else { // 单线模式
                getVoltageLine(bean.VoltageLineName).setCurrYIndex(bean.ChannelId); // 设置通道索引
            }

//            if (bean.VoltageLineName.equals(VoltageLineType_Value1)) {
//                getVoltageLine(VoltageLineType_Value2).setCurrYIndex(bean.ChannelId);
//            } else if (bean.VoltageLineName.equals(VoltageLineType_Value2)) {
//                getVoltageLine(VoltageLineType_Value1).setCurrYIndex(bean.ChannelId);
//            }
            return bean; // 返回当前Bean
        }
    }

    //endregion


    /**
     * 设置最前显示的预值电平线类型
     * @param discreetVoltageLineType 预值电平线类型
     */
    private void setDiscreetVoltageFrontShow(String discreetVoltageLineType) {
        frontDiscreetVoltageLine = discreetVoltageLineType; // 更新最前显示类型
    }


    /**
     * 设置预值电平显示通道
     *
     * @param VoltageLineType 参数以VoltageLineType_Value1，VoltageLineType_Value2，
     *                        例：两个都显示调用setDiscreetVoltageShowState(VoltageLineType_Value1+VoltageLineType_Value2)
     * @param CurLineIdx 当前操作的预值电平线索引
     */
    public void setDiscreetVoltageShowState(String VoltageLineType, int CurLineIdx) {
//        Logger.d("limh", "VoltageLineType= " + VoltageLineType + " CurLineIdx= " + CurLineIdx);
        this.CurrOptionVoltageLineType =type[CurLineIdx]; // 根据索引获取当前操作的预值电平线类型
//        if (VoltageLineType.contains(VoltageLineType_Value1) && VoltageLineType.contains(VoltageLineType_Value2)) {
//            currShowDiscreetVoltage = ShowDiscreetVoltage_ALL;
//            getVoltageLine(VoltageLineType_Value1).setShowState(true);
//            getVoltageLine(VoltageLineType_Value2).setShowState(true);
//        } else if (VoltageLineType.contains(VoltageLineType_Value1)) {
//            currShowDiscreetVoltage = ShowDiscreetVoltage_Value1;
//            getVoltageLine(VoltageLineType_Value1).setShowState(true);
//            getVoltageLine(VoltageLineType_Value2).setShowState(false);
//        } else if (VoltageLineType.contains(VoltageLineType_Value2)) {
//            currShowDiscreetVoltage = ShowDiscreetVoltage_Value2;
//            getVoltageLine(VoltageLineType_Value1).setShowState(false);
//            getVoltageLine(VoltageLineType_Value2).setShowState(true);
//        } else {
//            getVoltageLine(VoltageLineType_Value1).setShowState(false);
//            getVoltageLine(VoltageLineType_Value2).setShowState(false);
//        }

        getVoltageLine(VoltageLineType_Value1).setShowState(false); // 隐藏Value1
        getVoltageLine(VoltageLineType_Value2).setShowState(false); // 隐藏Value2
        getVoltageLine(VoltageLineType_Value3).setShowState(false); // 隐藏Value3
        getVoltageLine(VoltageLineType_Value4).setShowState(false); // 隐藏Value4
        if (VoltageLineType.contains(VoltageLineType_Value1)) { // 包含Value1
            getVoltageLine(VoltageLineType_Value1).setShowState(true); // 显示Value1
        }
        if (VoltageLineType.contains(VoltageLineType_Value2)) { // 包含Value2
            getVoltageLine(VoltageLineType_Value2).setShowState(true); // 显示Value2
        }
        if (VoltageLineType.contains(VoltageLineType_Value3)) { // 包含Value3
            getVoltageLine(VoltageLineType_Value3).setShowState(true); // 显示Value3
        }
        if (VoltageLineType.contains(VoltageLineType_Value4)) { // 包含Value4
            getVoltageLine(VoltageLineType_Value4).setShowState(true); // 显示Value4
        }
        mergeDiscreetVoltageLine(this.CurrOptionVoltageLineType); // 合并显示预值电平线
//        getAllDiscreetVoltageLineChannelIdInS1S2(CurrOptionVoltageLineType);
    }


    /**
     * 绘制到标准Canvas（空实现）
     * @param canvas Canvas对象
     */
    public void draw(Canvas canvas) {
    }

    /**
     * 绘制到GLCanvas
     * 先绘制触发电平线，再按前后层级绘制预值电平线（最前的最后绘制以覆盖其他线）
     * @param canvas ICanvasGL对象
     */
    public void draw(ICanvasGL canvas) {
        mapVoltageLine.get(VoltageLineType_Trigger).draw(canvas); // 绘制触发电平线
        if (frontDiscreetVoltageLine.equals(VoltageLineType_Value1)) { // Value1最前显示
            mapVoltageLine.get(VoltageLineType_Value2).draw(canvas); // 先绘制Value2（底层）
            mapVoltageLine.get(VoltageLineType_Value3).draw(canvas); // 绘制Value3
            mapVoltageLine.get(VoltageLineType_Value4).draw(canvas); // 绘制Value4
            mapVoltageLine.get(VoltageLineType_Value1).draw(canvas); // 最后绘制Value1（顶层）
        } else if (frontDiscreetVoltageLine.equals(VoltageLineType_Value2)) { // Value2最前显示
            mapVoltageLine.get(VoltageLineType_Value1).draw(canvas); // 先绘制Value1（底层）
            mapVoltageLine.get(VoltageLineType_Value3).draw(canvas); // 绘制Value3
            mapVoltageLine.get(VoltageLineType_Value4).draw(canvas); // 绘制Value4
            mapVoltageLine.get(VoltageLineType_Value2).draw(canvas); // 最后绘制Value2（顶层）
        } else if (frontDiscreetVoltageLine.equals(VoltageLineType_Value3)) { // Value3最前显示
            mapVoltageLine.get(VoltageLineType_Value1).draw(canvas); // 先绘制Value1（底层）
            mapVoltageLine.get(VoltageLineType_Value2).draw(canvas); // 绘制Value2
            mapVoltageLine.get(VoltageLineType_Value4).draw(canvas); // 绘制Value4
            mapVoltageLine.get(VoltageLineType_Value3).draw(canvas); // 最后绘制Value3（顶层）
        } else if (frontDiscreetVoltageLine.equals(VoltageLineType_Value4)) { // Value4最前显示
            mapVoltageLine.get(VoltageLineType_Value1).draw(canvas); // 先绘制Value1（底层）
            mapVoltageLine.get(VoltageLineType_Value2).draw(canvas); // 绘制Value2
            mapVoltageLine.get(VoltageLineType_Value3).draw(canvas); // 绘制Value3
            mapVoltageLine.get(VoltageLineType_Value4).draw(canvas); // 最后绘制Value4（顶层）
        }
    }

    /**
     * 刷新所有电平线
     */
    public void refresh() {
        for (String key : mapVoltageLine.keySet()) { // 遍历所有电平线
            ITriggerLine line = mapVoltageLine.get(key); // 获取电平线对象
            if (line != null) { // 电平线有效
                line.refresh(); // 刷新电平线
            }
        }
    }

}
