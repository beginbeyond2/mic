package com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob;

import com.micsig.smart.Property;
import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Sample.MemDepthFactory;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.measure.Measure;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.config.ScopeConfig;
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterChannel;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.util.StrUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║  模块定位：多功能旋钮(MultifunctionKnob)外部按键节点构建工具类              ║
 * ║  核心职责：为示波器UI各菜单层级构建按键/旋钮导航节点(ExternalKeysNode)树      ║
 * ║  架构设计：纯静态工具类，通过树形节点结构描述示波器菜单的层级与布局           ║
 * ║  数据流向：ExternalKeysManager → ExternalKeysNodeUtil(本类) → ExternalKeysNode ║
 * ║  依赖关系：GlobalVar / CacheUtil / ChannelFactory / MemDepthFactory / App    ║
 * ║  使用场景：多功能旋钮在各级菜单间导航时，获取当前菜单的节点列表               ║
 * ║            支持顶部滑动栏、右侧滑动栏、底部滑动栏、中心区域等多种菜单布局      ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 *
 * Created by yangj on 2018/5/22.
 */

public class ExternalKeysNodeUtil {

    private static final String TAG = "ExternalKeysNodeUtil";
    //region Public

    /**
     * 获取顶部滑动栏节点列表（对外接口），包含Measure/Save/Cursor/Sample/Display/Trigger/Auto/UserSet等主菜单
     */
    public static List<ExternalKeysNode> getTopSlipNode() {
        return getTopSlipNodeList(ExternalKeysManager.topSlipOffset);  // 返回顶部滑动栏节点列表
    }

    /**
     * 获取右侧滑动栏-通道节点列表（对外接口），用于通道参数配置界面
     */
    public static List<ExternalKeysNode> getRightSlipChannelNode() {
        return getRightSlipChannelNodeList(ExternalKeysManager.topSlipOffset);  // 返回右侧通道节点列表
    }

    /**
     * 获取右侧滑动栏-数学运算节点列表（对外接口），用于Math通道配置界面
     */
    public static List<ExternalKeysNode> getRightSlipMathNode(int mathNumber, int offset) {
        return getRightSlipMathNodeList(mathNumber, offset);  // 返回右侧Math节点列表
    }

    /**
     * 获取右侧滑动栏-参考波形节点列表（对外接口），用于Ref通道配置界面
     */
    public static List<ExternalKeysNode> getRightSlipRefNode(int refNumber, int offset) {
        return getRightSlipRefNodeList(refNumber, offset);  // 返回右侧Ref节点列表
    }

    /**
     * 获取右侧滑动栏-串行总线节点列表（对外接口），用于串行协议解码配置界面
     */
    public static List<ExternalKeysNode> getRightSlipSerialsNode(int serialsNumber, int offset) {
        return getRightSlipSerialsDetailNodeList(serialsNumber, offset);  // 返回右侧串行总线节点列表
    }

    /**
     * 获取底部滑动栏节点列表（对外接口）
     */
    public static List<ExternalKeysNode> getBottomSlipNode() {
        return getBottomSlipNodeList();  // 返回底部滑动栏节点列表
    }

    /**
     * 获取中心区域通道节点列表（对外接口），用于通道快捷操作
     */
    public static List<ExternalKeysNode> getCenterChannelsNode() {
        return getCenterChannelsNodeList();  // 返回中心区域通道节点列表
    }

    /**
     * 获取中心区域通道节点列表（对外接口），用于通道快捷操作
     */
    public static List<ExternalKeysNode> getCenterChannelsNode(MainLayoutCenterChannel channelLayout) {
        return getCenterChannelsNodeList(channelLayout);  // 返回中心区域通道节点列表
    }

    /**
     * 获取"确定/取消"按钮节点列表（对外接口）
     */
    public static List<ExternalKeysNode> getOkCancelNode() {
        return getOkCancelNodeList();  // 返回确定/取消节点列表
    }

    /**
     * 获取"确定"按钮节点列表（对外接口）
     */
    public static List<ExternalKeysNode> getOkNode() {
        return getOkNodeList();  // 返回确定节点列表
    }

    /**
     * 获取串行总线协议字节点列表（对外接口）
     */
    public static List<ExternalKeysNode> getSerialsWordNode() {
        return getSerialsWordNodeList(ExternalKeysManager.topSlipOffset);  // 返回串行总线字节点列表
    }

    /**
     * 获取汽车总线节点列表（对外接口）
     */
    public static List<ExternalKeysNode> getAutoMotiveNode() {
        return getAutoMotiveNodeList();  // 返回汽车总线节点列表
    }

    /**
     * 获取中心区域分段存储节点列表（对外接口）
     */
    public static List<ExternalKeysNode> getCenterSegmentedNode() {
        return getCenterSegmentedNodeList();  // 返回分段存储节点列表
    }
    //endregion

    //region TopSlip
    /**
     * 构建顶部滑动栏所有主菜单节点：Measure/Save/Cursor/Sample/Display/Trigger/Auto/UserSet/Frequency等
     */
    private static List<ExternalKeysNode> getTopSlipNodeList(int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode measure = new ExternalKeysNode();  // 创建测量节点
        measure.setPlace(list.size(), 0, 40 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        measure.setName("measure");  // 设置节点名称
        measure.setChildNodes(getTopMeasureDetailNodeList(measure, list, topSlipOffset));  // 设置子节点列表
        measure.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP));  // 设置当前选中项
        measure.setParentNode(null);  // 父节点置空(根节点)
        measure.setParentNodes(null);  // 父节点列表置空(根节点)
        measure.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(measure);  // 添加节点到列表

        ExternalKeysNode saveAndInvoke = new ExternalKeysNode();  // 创建保存/调出节点
        saveAndInvoke.setPlace(list.size(), 145, 40 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        saveAndInvoke.setName("save/invoke");  // 设置节点名称
        saveAndInvoke.setChildNodes(getTopSaveDetailNodeList(saveAndInvoke, list, topSlipOffset));  // 设置子节点列表
        saveAndInvoke.setParentNode(null);  // 父节点置空(根节点)
        saveAndInvoke.setParentNodes(null);  // 父节点列表置空(根节点)
        saveAndInvoke.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(saveAndInvoke);  // 添加节点到列表

        ExternalKeysNode cursor = new ExternalKeysNode();  // 创建光标节点
        cursor.setPlace(list.size(), 290, 40 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        cursor.setName("cursor");  // 设置节点名称
        cursor.setChildNodes(getTopCursorNodeList(cursor, list, topSlipOffset));  // 设置子节点列表
        cursor.setParentNode(null);  // 父节点置空(根节点)
        cursor.setParentNodes(null);  // 父节点列表置空(根节点)
        cursor.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(cursor);  // 添加节点到列表

        ExternalKeysNode sample = new ExternalKeysNode();  // 创建采样节点
        sample.setPlace(list.size(), 435, 40 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        sample.setName("sample");  // 设置节点名称
        sample.setChildNodes(getTopSampleNodeList(sample, list, topSlipOffset));  // 设置子节点列表
        sample.setParentNode(null);  // 父节点置空(根节点)
        sample.setParentNodes(null);  // 父节点列表置空(根节点)
        sample.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(sample);  // 添加节点到列表

        ExternalKeysNode display = new ExternalKeysNode();  // 创建显示节点
        display.setPlace(list.size(), 580, 40 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        display.setName("display");  // 设置节点名称
        display.setChildNodes(getTopDisplayDetailNodeList(display, list, topSlipOffset));  // 设置子节点列表
        display.setParentNode(null);  // 父节点置空(根节点)
        display.setParentNodes(null);  // 父节点列表置空(根节点)
        display.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(display);  // 添加节点到列表

        ExternalKeysNode trigger = new ExternalKeysNode();  // 创建触发节点
        trigger.setPlace(list.size(), 725, 40 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        trigger.setName("trigger");  // 设置节点名称
        trigger.setChildNodes(getTopTriggerDetailNodeList(trigger, list, topSlipOffset));  // 设置子节点列表
        trigger.setParentNode(null);  // 父节点置空(根节点)
        trigger.setParentNodes(null);  // 父节点列表置空(根节点)
        trigger.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(trigger);  // 添加节点到列表

        ExternalKeysNode auto = new ExternalKeysNode();  // 创建自动节点
        auto.setPlace(list.size(), 870, 40 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        auto.setName("auto");  // 设置节点名称
        auto.setChildNodes(getTopAutoDetailNodeList(auto, list, topSlipOffset));  // 设置子节点列表
        auto.setParentNode(null);  // 父节点置空(根节点)
        auto.setParentNodes(null);  // 父节点列表置空(根节点)
        auto.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(auto);  // 添加节点到列表


        ExternalKeysNode userSet = new ExternalKeysNode();  // 创建用户设置节点
        userSet.setPlace(list.size(), 1015, 40 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        userSet.setName("userSet");  // 设置节点名称
        userSet.setChildNodes(getTopUserSetDetailNodeList(userSet, list, topSlipOffset));  // 设置子节点列表
        userSet.setParentNode(null);  // 父节点置空(根节点)
        userSet.setParentNodes(null);  // 父节点列表置空(根节点)
        userSet.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(userSet);  // 添加节点到列表

        ExternalKeysNode frequency = new ExternalKeysNode();  // 创建频率计节点
        frequency.setPlace(list.size(), 501, 40 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        frequency.setName("frequency");  // 设置节点名称
        frequency.setChildNodes(getTopFrequencyDetailNodeList(frequency, list, topSlipOffset));  // 设置子节点列表
        frequency.setParentNode(null);  // 父节点置空(根节点)
        frequency.setParentNodes(null);  // 父节点列表置空(根节点)
        frequency.setVisible(false);  // 设为不可见
        frequency.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(frequency);  // 添加节点到列表

        if (GlobalVar.get().isFactoryCalibration()) {  // 判断是否出厂校准模式
            ExternalKeysNode factoryCalibration = new ExternalKeysNode();  // 创建出厂校准节点
            factoryCalibration.setPlace(list.size(), 1015 + 145, 40 + topSlipOffset, 145, 70);  // 设置位置和尺寸
            factoryCalibration.setName("factoryCalibration");  // 设置节点名称
            factoryCalibration.setChildNodes(getTopUserSetDetailNodeList(factoryCalibration, list, topSlipOffset));  // 设置子节点列表
            factoryCalibration.setParentNode(null);  // 父节点置空(根节点)
            factoryCalibration.setParentNodes(null);  // 父节点列表置空(根节点)
            factoryCalibration.setVisible(false);  // 设为不可见
            factoryCalibration.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
            list.add(factoryCalibration);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }
    //endregion

    //region TopMeasure
    /**
     * 构建Measure菜单的二级子菜单节点：Common(常见参数)/Statics(统计)/Counter(计数器)/Setting(设置)
     */
    private static List<ExternalKeysNode> getTopMeasureDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] strings = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.measure);  // 从资源文件获取字符串数组

        ExternalKeysNode common = new ExternalKeysNode();  // 创建通用节点
        common.setPlace(list.size(), 0, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        common.setName(strings[0]);  // 设置节点名称
        common.setParentNode(parent);  // 设置父节点
        common.setParentNodes(parents);  // 设置父节点列表
        common.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE));  // 设置当前选中项
        common.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        common.setChildNodes(getTopMeasureCommonNodeList(common, list, topSlipOffset));  // 设置子节点列表
        list.add(common);  // 添加节点到列表

        ExternalKeysNode statics = new ExternalKeysNode();  // 创建统计节点
        statics.setPlace(list.size(), 145, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        statics.setName(strings[1]);  // 设置节点名称
        statics.setParentNode(parent);  // 设置父节点
        statics.setParentNodes(parents);  // 设置父节点列表
//        statics.setVisible(false);
        statics.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        statics.setChildNodes(getTopMeasureStaticsDetailNodeList(statics, list, topSlipOffset));  // 设置子节点列表
        list.add(statics);  // 添加节点到列表

        ExternalKeysNode counter = new ExternalKeysNode();  // 创建计数器节点
        counter.setPlace(list.size(), 290, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        counter.setName(strings[2]);  // 设置节点名称
        counter.setParentNode(parent);  // 设置父节点
        counter.setParentNodes(parents);  // 设置父节点列表
        counter.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        counter.setChildNodes(getTopMeasureCounterNodeList(counter, list, topSlipOffset));  // 设置子节点列表
        list.add(counter);  // 添加节点到列表

        ExternalKeysNode setting = new ExternalKeysNode();  // 创建设置节点
        setting.setPlace(list.size(), 435, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        setting.setName(strings[3]);  // 设置节点名称
        setting.setParentNode(parent);  // 设置父节点
        setting.setParentNodes(parents);  // 设置父节点列表
//        setting.setVisible(false);
        setting.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        setting.setChildNodes(getTopMeasureSettingNodeList(setting, list, topSlipOffset));  // 设置子节点列表
        list.add(setting);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Measure-Common子菜单节点：通道选择行 + 所有测量参数项 + 已添加测量项 + 清除按钮
     */
    private static List<ExternalKeysNode> getTopMeasureCommonNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        //24 -> ch1-ch8 + Math1-Math8 + R1-R8
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {  // 判断是否为8通道型号
            int count = ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT + ChannelFactory.REF_CNT;  // 物理通道数量
            for (int i = 0; i < count; i++) {  // 遍历通道创建节点
                ExternalKeysNode channel = new ExternalKeysNode();  // 创建通道节点
                int width = 97;//channel
                if (i >= ChannelFactory.CH_CNT && i < ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT) {  // 判断是否为Math通道范围
                    width = 87;//Math
                } else if (i >= ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT) {  // 物理通道数量
                    width = 82;//Ref
                }
                channel.setPlace(list.size(), 30 + 127 * i, 215 + topSlipOffset, width, 50);  // 设置位置和尺寸
                channel.setName("channelIndex:" + i);  // 设置节点名称
                channel.setParentNode(parent);  // 设置父节点
                channel.setParentNodes(parents);  // 设置父节点列表
                channel.setType(ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_MEASURE_COMMON);  // 类型:主菜单-通道测量通用
                list.add(channel);  // 添加节点到列表
            }
        } else {  // 否则
            // 9 -> ch1-ch4 + MATH + r1-r4
            for (int i = 0, x = 11; i < 9; i++) {  // 循环创建节点
                ExternalKeysNode channel = new ExternalKeysNode();  // 创建通道节点
                int width = i <= 3 ? 97 : (i == 4 ? 114 : 82);  // 定义整型变量
                channel.setPlace(list.size(), 30 + 127 * i, 215 + topSlipOffset, width, 50);  // 设置位置和尺寸
                channel.setName("channelIndex:" + i);  // 设置节点名称
//            x =11+ 127*i;
                channel.setParentNode(parent);  // 设置父节点
                channel.setParentNodes(parents);  // 设置父节点列表
                channel.setType(ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_MEASURE_COMMON);  // 类型:主菜单-通道测量通用
                list.add(channel);  // 添加节点到列表
            }
        }

        String[] allMeasures = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.measures);  // 从资源文件获取字符串数组
        for (int i = 0; i < allMeasures.length; i++) {  // 遍历选项创建节点
            int y = i < 16 ? 302 + topSlipOffset : 387 + topSlipOffset;  // 定义整型变量
            int x = 34 + 110 * (i % 16) + (i % 16) / 3;//根据坐标打印，间隔110 110 111循环显示，每两个110之后一个111即每三个一组。
            ExternalKeysNode measure = new ExternalKeysNode();  // 创建测量节点
//            measure.setPlace(list.size(), x, y, 86, 84);
            measure.setPlace(list.size(), x, y, 84, 74);  // 设置位置和尺寸
            measure.setName(allMeasures[i]);  // 设置节点名称
            measure.setParentNode(parent);  // 设置父节点
            measure.setParentNodes(parents);  // 设置父节点列表
            measure.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
            if (i == 4) {//Delay
                measure.setChildNodes(getMeasureDelayNodeList(measure, list));  // 设置子节点列表
                measure.setDialog(ExternalKeysNode.DIALOG_MEASUREDELAY);  // 关联弹窗:测量延时
            } else if (i == 12) {//Phase
                measure.setChildNodes(getMeasurePhaseNodeList(measure, list));  // 设置子节点列表
                measure.setDialog(ExternalKeysNode.DIALOG_MEASUREPHASE);  // 关联弹窗:测量相位
            } else if (i == 26) {
                measure.setChildNodes(getMeasureTValueNodeList(measure, list));  // 设置子节点列表
                measure.setDialog(ExternalKeysNode.DIALOG_MEASURETVALUE);  // 关联弹窗:测量T值
            }

            list.add(measure);  // 添加节点到列表
        }

        int maxSelectCount = GlobalVar.get().getMeasureItemCount();  // 获取测量项数量
        for (int i = 0; i < maxSelectCount; i++) {  // 遍历通道创建节点
            ExternalKeysNode delMeasure = new ExternalKeysNode();  // 创建节点节点
            int x = 34 + 112 * (i % 14) - (i % 14) / 4;//根据坐标打印，间隔112 112 112 111循环显示，每三个112之后一个111即每四个一组。
            int y = 510 + topSlipOffset + 85 * (i / 14);  // 定义整型变量
            delMeasure.setPlace(list.size(), x, y, 84, 74);  // 设置位置和尺寸
            delMeasure.setName("delList:" + i);  // 设置节点名称
            delMeasure.setParentNode(parent);  // 设置父节点
            delMeasure.setVisible(false);  // 设为不可见
            delMeasure.setParentNodes(parents);  // 设置父节点列表
            list.add(delMeasure);  // 添加节点到列表
        }

        ExternalKeysNode clearMeasures = new ExternalKeysNode();  // 创建清除测量节点
        clearMeasures.setPlace(list.size(), 1640, 438, 120, 60);  // 设置位置和尺寸
        clearMeasures.setName("clearMeasures");  // 设置节点名称
        clearMeasures.setParentNode(parent);  // 设置父节点
        clearMeasures.setVisible(true);  // 设为可见
        clearMeasures.setParentNodes(parents);  // 设置父节点列表
        list.add(clearMeasures);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Measure-Statics统计子菜单节点：All/Reset/Mean/Max/Min/Dev/Count
     */
    private static List<ExternalKeysNode> getTopMeasureStaticsDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode all = new ExternalKeysNode();  // 创建全部节点
        all.setPlace(list.size(), 120, 205 + topSlipOffset, 72, 52);  // 设置位置和尺寸
        all.setName("all");  // 设置节点名称
        all.setParentNode(parent);  // 设置父节点
        all.setParentNodes(parents);  // 设置父节点列表
        list.add(all);  // 添加节点到列表

        ExternalKeysNode reset = new ExternalKeysNode();  // 创建复位节点
        reset.setPlace(list.size(), 1630, 205 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        reset.setName("reset");  // 设置节点名称
        reset.setParentNode(parent);  // 设置父节点
        reset.setParentNodes(parents);  // 设置父节点列表
        list.add(reset);  // 添加节点到列表

        ExternalKeysNode mean = new ExternalKeysNode();  // 创建均值节点
        mean.setPlace(list.size(), 120, 267 + topSlipOffset, 72, 52);  // 设置位置和尺寸
        mean.setName("mean");  // 设置节点名称
        mean.setParentNode(parent);  // 设置父节点
        mean.setParentNodes(parents);  // 设置父节点列表
        list.add(mean);  // 添加节点到列表

        ExternalKeysNode max = new ExternalKeysNode();  // 创建最大值节点
        max.setPlace(list.size(), 312, 267 + topSlipOffset, 72, 52);  // 设置位置和尺寸
        max.setName("max");  // 设置节点名称
        max.setParentNode(parent);  // 设置父节点
        max.setParentNodes(parents);  // 设置父节点列表
        list.add(max);  // 添加节点到列表

        ExternalKeysNode min = new ExternalKeysNode();  // 创建最小值节点
        min.setPlace(list.size(), 504, 267 + topSlipOffset, 72, 52);  // 设置位置和尺寸
        min.setName("min");  // 设置节点名称
        min.setParentNode(parent);  // 设置父节点
        min.setParentNodes(parents);  // 设置父节点列表
        list.add(min);  // 添加节点到列表

        ExternalKeysNode dev = new ExternalKeysNode();  // 创建标准差节点
        dev.setPlace(list.size(), 696, 267 + topSlipOffset, 72, 52);  // 设置位置和尺寸
        dev.setName("dev");  // 设置节点名称
        dev.setParentNode(parent);  // 设置父节点
        dev.setParentNodes(parents);  // 设置父节点列表
        list.add(dev);  // 添加节点到列表

        ExternalKeysNode count = new ExternalKeysNode();  // 创建计数节点
        count.setPlace(list.size(), 888, 267 + topSlipOffset, 72, 52);  // 设置位置和尺寸
        count.setName("count");  // 设置节点名称
        count.setParentNode(parent);  // 设置父节点
        count.setParentNodes(parents);  // 设置父节点列表
        list.add(count);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Measure-Counter计数器子菜单节点：频率计通道列表
     */
    public static List<ExternalKeysNode> getTopMeasureCounterNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels1 = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.frequencymeter);  // 从资源文件获取字符串数组
        String[] channels2 = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] channels = StrUtil.add(channels1, channels2);  // 合并两个字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode channel = new ExternalKeysNode();  // 创建通道节点
            channel.setParentNode(parent);  // 设置父节点
            channel.setParentNodes(parents);  // 设置父节点列表
            channel.setPlace(list.size(), 149 + i * 120, 205 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            channel.setName(channels[i]);  // 设置节点名称
            list.add(channel);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Measure-Setting设置子菜单节点：指示器/屏幕/光标/通道/类型/阈值/清除
     */
    public static List<ExternalKeysNode> getTopMeasureSettingNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode indicator = new ExternalKeysNode();  // 创建指示器节点
        indicator.setPlace(list.size(), 187, 211 + topSlipOffset, 72, 36);  // 设置位置和尺寸
        indicator.setName("indicator");  // 设置节点名称
        indicator.setParentNode(parent);  // 设置父节点
        indicator.setParentNodes(parents);  // 设置父节点列表
        list.add(indicator);  // 添加节点到列表

        ExternalKeysNode screen = new ExternalKeysNode();  // 创建屏幕节点
        screen.setPlace(list.size(), 883, 200 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        screen.setName("screen");  // 设置节点名称
        screen.setParentNode(parent);  // 设置父节点
        screen.setParentNodes(parents);  // 设置父节点列表
        list.add(screen);  // 添加节点到列表

        ExternalKeysNode cursor = new ExternalKeysNode();  // 创建光标节点
        cursor.setPlace(list.size(), 1003, 200 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        cursor.setName("cursor");  // 设置节点名称
        cursor.setParentNode(parent);  // 设置父节点
        cursor.setParentNodes(parents);  // 设置父节点列表
        list.add(cursor);  // 添加节点到列表

        //channel
        //24 -> ch1-ch8 + Math1-Math8 + R1-R8
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {  // 判断是否为8通道型号
            int count = ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT + ChannelFactory.REF_CNT;  // 物理通道数量
            for (int i = 0, x = 11; i < count; i++) {  // 遍历通道创建节点
                ExternalKeysNode channel = new ExternalKeysNode();  // 创建通道节点
                int width = 97;//channel
                if (i >= ChannelFactory.CH_CNT && i < ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT) {  // 判断是否为Math通道范围
                    width = 87;//Math
                } else if (i >= ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT) {  // 物理通道数量
                    width = 82;//Ref
                }
                channel.setPlace(list.size(), 30 + 127 * i, 303 + topSlipOffset, width, 50);  // 设置位置和尺寸
                channel.setName("channelIndex:" + i);  // 设置节点名称
//            x =11+ 127*i;
                channel.setParentNode(parent);  // 设置父节点
                channel.setParentNodes(parents);  // 设置父节点列表
                channel.setType(ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_MEASURE_SETTING);  // 类型:主菜单-通道测量设置
                list.add(channel);  // 添加节点到列表
            }
        } else {  // 否则
            //9 -> ch1-ch4 + MATH + R1-R4
            for (int i = 0, x = 11; i < 9; i++) {  // 循环创建节点
                ExternalKeysNode channel = new ExternalKeysNode();  // 创建通道节点
                int width = i <= 3 ? 97 : (i == 4 ? 114 : 82);  // 定义整型变量
                channel.setPlace(list.size(), 30 + 127 * i, 303 + topSlipOffset, width, 50);  // 设置位置和尺寸
                channel.setName("channelIndex:" + i);  // 设置节点名称
//            x =11+ 127*i;
                channel.setParentNode(parent);  // 设置父节点
                channel.setParentNodes(parents);  // 设置父节点列表
                channel.setType(ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_MEASURE_SETTING);  // 类型:主菜单-通道测量设置
                list.add(channel);  // 添加节点到列表
            }
        }

        ExternalKeysNode type1 = new ExternalKeysNode();  // 创建百分比类型节点
        type1.setPlace(list.size(), 173, 373 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        type1.setName("%");  // 设置节点名称
        type1.setParentNode(parent);  // 设置父节点
        type1.setParentNodes(parents);  // 设置父节点列表
        list.add(type1);  // 添加节点到列表

        ExternalKeysNode type2 = new ExternalKeysNode();  // 创建绝对值类型节点
        type2.setPlace(list.size(), 293, 373 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        type2.setName("abs");  // 设置节点名称
        type2.setParentNode(parent);  // 设置父节点
        type2.setParentNodes(parents);  // 设置父节点列表
        list.add(type2);  // 添加节点到列表

        ExternalKeysNode high = new ExternalKeysNode();  // 创建上限节点
        high.setPlace(list.size(), 699, 373 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        high.setName("high");  // 设置节点名称
        high.setParentNode(parent);  // 设置父节点
        high.setParentNodes(parents);  // 设置父节点列表
        high.setChildNodes(getFloatKeyBoardNodeList(high, list));  // 设置子节点列表
        high.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(high);  // 添加节点到列表

        ExternalKeysNode mid = new ExternalKeysNode();  // 创建中值节点
        mid.setPlace(list.size(), 919, 373 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        mid.setName("mid");  // 设置节点名称
        mid.setParentNode(parent);  // 设置父节点
        mid.setParentNodes(parents);  // 设置父节点列表
        mid.setChildNodes(getFloatKeyBoardNodeList(mid, list));  // 设置子节点列表
        mid.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(mid);  // 添加节点到列表

        ExternalKeysNode low = new ExternalKeysNode();  // 创建下限节点
        low.setPlace(list.size(), 1139, 373 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        low.setName("low");  // 设置节点名称
        low.setParentNode(parent);  // 设置父节点
        low.setParentNodes(parents);  // 设置父节点列表
        low.setChildNodes(getFloatKeyBoardNodeList(low, list));  // 设置子节点列表
        low.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(low);  // 添加节点到列表

        ExternalKeysNode clear = new ExternalKeysNode();  // 创建清除节点
        clear.setPlace(list.size(), 1599, 373 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        clear.setName("clear");  // 设置节点名称
        clear.setParentNode(parent);  // 设置父节点
        clear.setParentNodes(parents);  // 设置父节点列表
        list.add(clear);  // 添加节点到列表

        return list;  // 返回节点列表
    }
    //endregion

    //region TopSave
    /**
     * 构建Save菜单的二级子菜单节点：Save(保存)/Invoke(调出)/AutoSave(自动保存)
     */
    private static List<ExternalKeysNode> getTopSaveDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] strings = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.save);  // 从资源文件获取字符串数组
        ExternalKeysNode save = new ExternalKeysNode();  // 创建保存节点
        save.setPlace(list.size(), 0, 146, 145, 70);  // 设置位置和尺寸
        save.setName(strings[0]);  // 设置节点名称
        save.setParentNode(parent);  // 设置父节点
        save.setParentNodes(parents);  // 设置父节点列表
        save.setCurListSelect(CacheUtil.get().getOtherInt(CacheUtil.TOP_SLIP_SAVE_STORE));  // 设置当前选中项
        save.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        save.setChildNodes(getTopSaveStoreDetails(save, list));  // 设置子节点列表
        list.add(save);  // 添加节点到列表

        ExternalKeysNode invoke = new ExternalKeysNode();  // 创建调出节点
        invoke.setPlace(list.size(), 145, 146, 145, 70);  // 设置位置和尺寸
        invoke.setName(strings[1]);  // 设置节点名称
        invoke.setParentNode(parent);  // 设置父节点
        invoke.setParentNodes(parents);  // 设置父节点列表
        invoke.setCurListSelect(CacheUtil.get().getOtherInt(CacheUtil.TOP_SLIP_INVOKE_STORE));  // 设置当前选中项
        invoke.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        invoke.setChildNodes(getTopInvokeDetails(invoke, list));  // 设置子节点列表
        list.add(invoke);  // 添加节点到列表

        ExternalKeysNode autoSave = new ExternalKeysNode();  // 创建自动保存节点
        autoSave.setPlace(list.size(), 290, 146, 145, 70);  // 设置位置和尺寸
        autoSave.setName(strings[2]);  // 设置节点名称
        autoSave.setParentNode(parent);  // 设置父节点
        autoSave.setParentNodes(parents);  // 设置父节点列表
        autoSave.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        autoSave.setChildNodes(getTopAutoSaveDetails(invoke, list));  // 设置子节点列表
        list.add(autoSave);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Cursor菜单的二级子菜单节点：CursorCommon(光标通用)/CursorSetting(光标设置)
     */
    private static List<ExternalKeysNode> getTopCursorNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode common = new ExternalKeysNode();  // 创建通用节点
        common.setPlace(list.size(), 0, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        common.setName("cursorCommon");  // 设置节点名称
        common.setParentNode(parent);  // 设置父节点
        common.setParentNodes(parents);  // 设置父节点列表
        common.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR));  // 设置当前选中项
        common.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        common.setChildNodes(getTopCursorCommonNodeList(common, list, topSlipOffset));  // 设置子节点列表
        list.add(common);  // 添加节点到列表

        ExternalKeysNode setting = new ExternalKeysNode();  // 创建设置节点
        setting.setPlace(list.size(), 146, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        setting.setName("cursorSetting");  // 设置节点名称
        setting.setParentNode(parent);  // 设置父节点
        setting.setParentNodes(parents);  // 设置父节点列表
        setting.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_CURSOR));  // 设置当前选中项
        setting.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        setting.setChildNodes(getTopCursorSettingNodeList(setting, list, topSlipOffset));  // 设置子节点列表
        list.add(setting);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Cursor-Common子菜单节点：通道选择行(含Auto选项)
     */
    private static List<ExternalKeysNode> getTopCursorCommonNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        int count = ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT + ChannelFactory.REF_CNT;  // 物理通道数量
        for (int i = 0, x = 11; i <= count; i++) {  // 遍历通道创建节点
            ExternalKeysNode channel = new ExternalKeysNode();  // 创建通道节点
            int width = 97;//channel
            if (i >= ChannelFactory.CH_CNT && i < ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT) {  // 判断是否为Math通道范围
                width = 87;//Math
            } else if (i >= ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT && i != count) {  // 物理通道数量
                width = 82;//Ref
            } else if (i == count) {
                width = 105;//Auto
            }
            channel.setPlace(list.size(), 30 + 127 * i, 215 + topSlipOffset, width, 50);  // 设置位置和尺寸
            channel.setName("channelIndex:" + i);  // 设置节点名称
//            x =11+ 127*i;
            channel.setParentNode(parent);  // 设置父节点
            channel.setParentNodes(parents);  // 设置父节点列表
            channel.setType(ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_CURSOR_COMMON);  // 类型:主菜单-通道光标通用
            list.add(channel);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Save-Store保存子菜单节点：Wave/CSV/BIN/Setting/Picture/Session
     */
    public static List<ExternalKeysNode> getTopSaveStoreDetails(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] strings = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.topSaveType);  // 从资源文件获取字符串数组

        ExternalKeysNode wave = new ExternalKeysNode();  // 创建波形节点
        wave.setPlace(list.size(), 0, 216, 145, 70);  // 设置位置和尺寸
        wave.setName(strings[0]);  // 设置节点名称
        wave.setParentNode(parent);  // 设置父节点
        wave.setParentNodes(parents);  // 设置父节点列表
        wave.setCurListSelect(CacheUtil.get().getOtherInt(CacheUtil.TOP_SLIP_SAVE_STORE));  // 设置当前选中项
        wave.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        wave.setChildNodes(getTopSaveWaveNodeList(wave, list));  // 设置子节点列表
        list.add(wave);  // 添加节点到列表


        ExternalKeysNode csv = new ExternalKeysNode();  // 创建CSV节点
        csv.setPlace(list.size(), 145, 216, 145, 70);  // 设置位置和尺寸
        csv.setName(strings[1]);  // 设置节点名称
        csv.setParentNode(parent);  // 设置父节点
        csv.setParentNodes(parents);  // 设置父节点列表
        csv.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        csv.setChildNodes(getTopSaveCsvNodeList(csv, list));  // 设置子节点列表
        list.add(csv);  // 添加节点到列表


        ExternalKeysNode bin = new ExternalKeysNode();  // 创建BIN节点
        bin.setPlace(list.size(), 290, 216, 145, 70);  // 设置位置和尺寸
        bin.setName(strings[2]);  // 设置节点名称
        bin.setParentNode(parent);  // 设置父节点
        bin.setParentNodes(parents);  // 设置父节点列表
        bin.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        bin.setChildNodes(getTopSaveBinNodeList(bin, list));  // 设置子节点列表
        list.add(bin);  // 添加节点到列表


        ExternalKeysNode setting = new ExternalKeysNode();  // 创建设置节点
        setting.setPlace(list.size(), 435, 216, 145, 70);  // 设置位置和尺寸
        setting.setName(strings[3]);  // 设置节点名称
        setting.setParentNode(parent);  // 设置父节点
        setting.setParentNodes(parents);  // 设置父节点列表
        setting.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        setting.setChildNodes(getTopSaveSettingDetailNodeList(setting, list));  // 设置子节点列表
        list.add(setting);  // 添加节点到列表

        ExternalKeysNode picture = new ExternalKeysNode();  // 创建图片节点
        picture.setPlace(list.size(), 580, 216, 145, 70);  // 设置位置和尺寸
        picture.setName(strings[4]);  // 设置节点名称
        picture.setParentNode(parent);  // 设置父节点
        picture.setParentNodes(parents);  // 设置父节点列表
        picture.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        picture.setChildNodes(getTopSavePictureDetailNodeList(picture, list));  // 设置子节点列表
        list.add(picture);  // 添加节点到列表

        ExternalKeysNode session = new ExternalKeysNode();  // 创建会话节点
        session.setPlace(list.size(), 725, 216, 145, 70);  // 设置位置和尺寸
        session.setName(strings[5]);  // 设置节点名称
        session.setParentNode(parent);  // 设置父节点
        session.setParentNodes(parents);  // 设置父节点列表
        session.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        session.setChildNodes(getTopSaveSessionDetailNodeList(session, list));  // 设置子节点列表
        list.add(session);  // 添加节点到列表


        return list;  // 返回节点列表
    }

    /**
     * 构建Invoke调出子菜单节点：Wave/CSV/Setting/Session
     */
    public static List<ExternalKeysNode> getTopInvokeDetails(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] strings = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.topInvokeType);  // 从资源文件获取字符串数组

        ExternalKeysNode wave = new ExternalKeysNode();  // 创建波形节点
        wave.setPlace(list.size(), 0, 216, 145, 70);  // 设置位置和尺寸
        wave.setName(strings[0]);  // 设置节点名称
        wave.setParentNode(parent);  // 设置父节点
        wave.setParentNodes(parents);  // 设置父节点列表
        wave.setCurListSelect(CacheUtil.get().getOtherInt(CacheUtil.TOP_SLIP_INVOKE_STORE));  // 设置当前选中项
        wave.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        wave.setChildNodes(getTopInvokeWavDetailNodeList(wave, list));  // 设置子节点列表
        list.add(wave);  // 添加节点到列表

        ExternalKeysNode csv = new ExternalKeysNode();  // 创建CSV节点
        csv.setPlace(list.size(), 145, 216, 145, 70);  // 设置位置和尺寸
        csv.setName(strings[1]);  // 设置节点名称
        csv.setParentNode(parent);  // 设置父节点
        csv.setParentNodes(parents);  // 设置父节点列表
        csv.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        csv.setChildNodes(getTopInvokeCsvDetailNodeList(csv, list));  // 设置子节点列表
        list.add(csv);  // 添加节点到列表

        ExternalKeysNode setting = new ExternalKeysNode();  // 创建设置节点
        setting.setPlace(list.size(), 290, 216, 145, 70);  // 设置位置和尺寸
        setting.setName(strings[2]);  // 设置节点名称
        setting.setParentNode(parent);  // 设置父节点
        setting.setParentNodes(parents);  // 设置父节点列表
        setting.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        setting.setChildNodes(getTopInvokeSettingDetailNodeList(setting, list));  // 设置子节点列表
        list.add(setting);  // 添加节点到列表

        ExternalKeysNode session = new ExternalKeysNode();  // 创建会话节点
        session.setPlace(list.size(), 435, 216, 145, 70);  // 设置位置和尺寸
        session.setName(strings[3]);  // 设置节点名称
        session.setParentNode(parent);  // 设置父节点
        session.setParentNodes(parents);  // 设置父节点列表
        session.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        session.setChildNodes(getTopInvokeSessionDetailNodeList(session, list));  // 设置子节点列表
        list.add(session);  // 添加节点到列表

        return list;  // 返回节点列表
    }


    /**
     * 构建AutoSave自动保存子菜单节点：通道选择/时间范围/停止条件/间隔/模式/类型/路径/文件名
     */
    public static List<ExternalKeysNode> getTopAutoSaveDetails(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] stopConditions = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.stopCondition);  // 从资源文件获取字符串数组
        String[] intervalTimes = App.get().getResources().getStringArray(R.array.intervalTime);  // 从资源文件获取字符串数组
        String[] saveModes = App.get().getResources().getStringArray(R.array.autoSaveMode);  // 从资源文件获取字符串数组
        String[] saveTypes = App.get().getResources().getStringArray(R.array.saveType);  // 从资源文件获取字符串数组
        int count = ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT + ChannelFactory.REF_CNT;  // 物理通道数量
        for (int i = 0, x = 11; i <= count; i++) {  // 遍历通道创建节点
            ExternalKeysNode channel = new ExternalKeysNode();  // 创建通道节点
            int width = 97;//channel
            if (i >= ChannelFactory.CH_CNT && i < ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT) {  // 判断是否为Math通道范围
                width = 87;//Math
            } else if (i >= ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT && i < count) {  // 物理通道数量
                width = 82;//Ref
            }
            channel.setPlace(list.size(), 30 + 127 * i, 235, width, 50);  // 设置位置和尺寸
            channel.setName("SaveCsv,channelIndex:" + i);  // 设置节点名称
//            x += width;
            channel.setParentNode(parent);  // 设置父节点
            channel.setParentNodes(parents);  // 设置父节点列表
            channel.setType(ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_AUTO_SAVE_CHANNEL);  // 类型:主菜单-通道自动保存
            list.add(channel);  // 添加节点到列表
        }
        ExternalKeysNode startYearInput = new ExternalKeysNode();  // 创建起始年输入节点
        startYearInput.setPlace(list.size(), 161, 306, 60, 54);  // 设置位置和尺寸
        startYearInput.setName("StartYearInput");  // 设置节点名称
        startYearInput.setParentNode(parent);  // 设置父节点
        startYearInput.setParentNodes(parents);  // 设置父节点列表
        startYearInput.setChildNodes(getNumberKeyBoardNodeList(startYearInput, list, false));  // 设置子节点列表
        for (int i = 0; i < startYearInput.getChildNodes().size(); i++) {  // 循环创建节点
            startYearInput.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        startYearInput.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        startYearInput.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(startYearInput);  // 添加节点到列表

        ExternalKeysNode startMonthInput = new ExternalKeysNode();  // 创建起始月输入节点
        startMonthInput.setPlace(list.size(), 271, 306, 50, 54);  // 设置位置和尺寸
        startMonthInput.setName("startMonthInput");  // 设置节点名称
        startMonthInput.setParentNode(parent);  // 设置父节点
        startMonthInput.setParentNodes(parents);  // 设置父节点列表
        startMonthInput.setChildNodes(getNumberKeyBoardNodeList(startMonthInput, list, false));  // 设置子节点列表
        for (int i = 0; i < startMonthInput.getChildNodes().size(); i++) {  // 循环创建节点
            startMonthInput.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        startMonthInput.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        startMonthInput.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(startMonthInput);  // 添加节点到列表

        ExternalKeysNode startDayInput = new ExternalKeysNode();  // 创建起始日输入节点
        startDayInput.setPlace(list.size(), 371, 306, 50, 54);  // 设置位置和尺寸
        startDayInput.setName("startDayInput");  // 设置节点名称
        startDayInput.setParentNode(parent);  // 设置父节点
        startDayInput.setParentNodes(parents);  // 设置父节点列表
        startDayInput.setChildNodes(getNumberKeyBoardNodeList(startDayInput, list, false));  // 设置子节点列表
        for (int i = 0; i < startDayInput.getChildNodes().size(); i++) {  // 循环创建节点
            startDayInput.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        startDayInput.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        startDayInput.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(startDayInput);  // 添加节点到列表

        ExternalKeysNode startHourInput = new ExternalKeysNode();  // 创建起始时输入节点
        startHourInput.setPlace(list.size(), 471, 306, 50, 54);  // 设置位置和尺寸
        startHourInput.setName("startHourInput");  // 设置节点名称
        startHourInput.setParentNode(parent);  // 设置父节点
        startHourInput.setParentNodes(parents);  // 设置父节点列表
        startHourInput.setChildNodes(getNumberKeyBoardNodeList(startHourInput, list, false));  // 设置子节点列表
        for (int i = 0; i < startHourInput.getChildNodes().size(); i++) {  // 循环创建节点
            startHourInput.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        startHourInput.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        startHourInput.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(startHourInput);  // 添加节点到列表


        ExternalKeysNode startMinuteInput = new ExternalKeysNode();  // 创建起始分输入节点
        startMinuteInput.setPlace(list.size(), 571, 306, 50, 54);  // 设置位置和尺寸
        startMinuteInput.setName("startMinuteInput");  // 设置节点名称
        startMinuteInput.setParentNode(parent);  // 设置父节点
        startMinuteInput.setParentNodes(parents);  // 设置父节点列表
        startMinuteInput.setChildNodes(getNumberKeyBoardNodeList(startHourInput, list, false));  // 设置子节点列表
        for (int i = 0; i < startMinuteInput.getChildNodes().size(); i++) {  // 循环创建节点
            startMinuteInput.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        startMinuteInput.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        startMinuteInput.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(startMinuteInput);  // 添加节点到列表

        ExternalKeysNode startSecondInput = new ExternalKeysNode();  // 创建起始秒输入节点
        startSecondInput.setPlace(list.size(), 671, 306, 50, 54);  // 设置位置和尺寸
        startSecondInput.setName("startSecondInput");  // 设置节点名称
        startSecondInput.setParentNode(parent);  // 设置父节点
        startSecondInput.setParentNodes(parents);  // 设置父节点列表
        startSecondInput.setChildNodes(getNumberKeyBoardNodeList(startHourInput, list, false));  // 设置子节点列表
        for (int i = 0; i < startSecondInput.getChildNodes().size(); i++) {  // 循环创建节点
            startSecondInput.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        startSecondInput.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        startSecondInput.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(startSecondInput);  // 添加节点到列表

        for (int i = 0; i <= 2; i++) {  // 循环创建节点
            ExternalKeysNode stopCondition = new ExternalKeysNode();  // 创建停止条件节点
            stopCondition.setParentNode(parent);  // 设置父节点
            stopCondition.setParentNodes(parents);  // 设置父节点列表
            stopCondition.setPlace(list.size(), 161 + i * 120, 370, 120, 60);  // 设置位置和尺寸
            stopCondition.setName(stopConditions[i]);  // 设置节点名称
            list.add(stopCondition);  // 添加节点到列表
        }

        ExternalKeysNode stopYearInput = new ExternalKeysNode();  // 创建停止年输入节点
        stopYearInput.setPlace(list.size(), 537, 372, 60, 54);  // 设置位置和尺寸
        stopYearInput.setName("stopYearInput");  // 设置节点名称
        stopYearInput.setParentNode(parent);  // 设置父节点
        stopYearInput.setParentNodes(parents);  // 设置父节点列表
        stopYearInput.setChildNodes(getNumberKeyBoardNodeList(stopYearInput, list, false));  // 设置子节点列表
        for (int i = 0; i < stopYearInput.getChildNodes().size(); i++) {  // 循环创建节点
            stopYearInput.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        stopYearInput.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        stopYearInput.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(stopYearInput);  // 添加节点到列表

        ExternalKeysNode stopMonthInput = new ExternalKeysNode();  // 创建停止月输入节点
        stopMonthInput.setPlace(list.size(), 647, 372, 50, 54);  // 设置位置和尺寸
        stopMonthInput.setName("startMonthInput");  // 设置节点名称
        stopMonthInput.setParentNode(parent);  // 设置父节点
        stopMonthInput.setParentNodes(parents);  // 设置父节点列表
        stopMonthInput.setChildNodes(getNumberKeyBoardNodeList(stopMonthInput, list, false));  // 设置子节点列表
        for (int i = 0; i < stopMonthInput.getChildNodes().size(); i++) {  // 循环创建节点
            stopMonthInput.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        stopMonthInput.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        stopMonthInput.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(stopMonthInput);  // 添加节点到列表

        ExternalKeysNode stopDayInput = new ExternalKeysNode();  // 创建停止日输入节点
        stopDayInput.setPlace(list.size(), 747, 372, 50, 54);  // 设置位置和尺寸
        stopDayInput.setName("stopDayInput");  // 设置节点名称
        stopDayInput.setParentNode(parent);  // 设置父节点
        stopDayInput.setParentNodes(parents);  // 设置父节点列表
        stopDayInput.setChildNodes(getNumberKeyBoardNodeList(stopDayInput, list, false));  // 设置子节点列表
        for (int i = 0; i < stopDayInput.getChildNodes().size(); i++) {  // 循环创建节点
            stopDayInput.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        stopDayInput.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        stopDayInput.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(stopDayInput);  // 添加节点到列表

        ExternalKeysNode stopHourInput = new ExternalKeysNode();  // 创建停止时输入节点
        stopHourInput.setPlace(list.size(), 847, 372, 50, 54);  // 设置位置和尺寸
        stopHourInput.setName("stopHourInput");  // 设置节点名称
        stopHourInput.setParentNode(parent);  // 设置父节点
        stopHourInput.setParentNodes(parents);  // 设置父节点列表
        stopHourInput.setChildNodes(getNumberKeyBoardNodeList(stopHourInput, list, false));  // 设置子节点列表
        for (int i = 0; i < stopHourInput.getChildNodes().size(); i++) {  // 循环创建节点
            stopHourInput.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        stopHourInput.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        stopHourInput.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(stopHourInput);  // 添加节点到列表


        ExternalKeysNode stopMinuteInput = new ExternalKeysNode();  // 创建停止分输入节点
        stopMinuteInput.setPlace(list.size(), 947, 372, 50, 54);  // 设置位置和尺寸
        stopMinuteInput.setName("stopMinuteInput");  // 设置节点名称
        stopMinuteInput.setParentNode(parent);  // 设置父节点
        stopMinuteInput.setParentNodes(parents);  // 设置父节点列表
        stopMinuteInput.setChildNodes(getNumberKeyBoardNodeList(startHourInput, list, false));  // 设置子节点列表
        for (int i = 0; i < stopMinuteInput.getChildNodes().size(); i++) {  // 循环创建节点
            stopMinuteInput.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        stopMinuteInput.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        stopMinuteInput.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(stopMinuteInput);  // 添加节点到列表

        ExternalKeysNode stopSecondInput = new ExternalKeysNode();  // 创建停止秒输入节点
        stopSecondInput.setPlace(list.size(), 1047, 372, 50, 54);  // 设置位置和尺寸
        stopSecondInput.setName("startSecondInput");  // 设置节点名称
        stopSecondInput.setParentNode(parent);  // 设置父节点
        stopSecondInput.setParentNodes(parents);  // 设置父节点列表
        stopSecondInput.setChildNodes(getNumberKeyBoardNodeList(startHourInput, list, false));  // 设置子节点列表
        for (int i = 0; i < stopSecondInput.getChildNodes().size(); i++) {  // 循环创建节点
            stopSecondInput.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        stopSecondInput.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        stopSecondInput.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(stopSecondInput);  // 添加节点到列表

        ExternalKeysNode nFramge = new ExternalKeysNode();  // 创建帧数节点
        nFramge.setPlace(list.size(), 547, 372, 120, 54);  // 设置位置和尺寸
        nFramge.setName("nFrame");  // 设置节点名称
        nFramge.setParentNode(parent);  // 设置父节点
        nFramge.setParentNodes(parents);  // 设置父节点列表
        nFramge.setChildNodes(getNumberKeyBoardNodeList(nFramge, list, false));  // 设置子节点列表
        for (int i = 0; i < nFramge.getChildNodes().size(); i++) {  // 循环创建节点
            nFramge.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        nFramge.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(nFramge);  // 添加节点到列表


        for (int i = 0; i <= 8; i++) {  // 循环创建节点
            ExternalKeysNode intervalTime = new ExternalKeysNode();  // 创建间隔时间节点
            intervalTime.setParentNode(parent);  // 设置父节点
            intervalTime.setParentNodes(parents);  // 设置父节点列表
            intervalTime.setPlace(list.size(), 161 + i * 120, 440, 120, 60);  // 设置位置和尺寸
            intervalTime.setName(intervalTimes[i]);  // 设置节点名称
            list.add(intervalTime);  // 添加节点到列表
        }

        for (int i = 0; i <= 1; i++) {  // 循环创建节点
            ExternalKeysNode saveMode = new ExternalKeysNode();  // 创建保存模式节点
            saveMode.setParentNode(parent);  // 设置父节点
            saveMode.setParentNodes(parents);  // 设置父节点列表
            saveMode.setPlace(list.size(), 161 + i * 120, 510, 120, 60);  // 设置位置和尺寸
            saveMode.setName(saveModes[i]);  // 设置节点名称
            list.add(saveMode);  // 添加节点到列表
        }

        for (int i = 0; i <= 4; i++) {  // 循环创建节点
            ExternalKeysNode saveType = new ExternalKeysNode();  // 创建保存类型节点
            saveType.setParentNode(parent);  // 设置父节点
            saveType.setParentNodes(parents);  // 设置父节点列表
            saveType.setPlace(list.size(), 160 + i * 125, 580, 120, 60);  // 设置位置和尺寸
            saveType.setName(saveTypes[i]);  // 设置节点名称
            list.add(saveType);  // 添加节点到列表
        }


        ExternalKeysNode spinner = new ExternalKeysNode();  // 创建下拉列表节点
        spinner.setPlace(list.size(), 159, 650, 600, 60);  // 设置位置和尺寸
        spinner.setName("autoSaveSpinner");  // 设置节点名称
        spinner.setParentNode(parent);  // 设置父节点
        spinner.setParentNodes(parents);  // 设置父节点列表
        list.add(spinner);  // 添加节点到列表

        ExternalKeysNode browse = new ExternalKeysNode();  // 创建浏览节点
        browse.setPlace(list.size(), 809, 650, 120, 60);  // 设置位置和尺寸
        browse.setName("autoSaveBrowse");  // 设置节点名称
        browse.setParentNode(parent);  // 设置父节点
        browse.setParentNodes(parents);  // 设置父节点列表
        browse.setChildNodes(null);  // 子节点置空
        list.add(browse);  // 添加节点到列表


        ExternalKeysNode saveNameInput = new ExternalKeysNode();  // 创建文件名输入节点
        saveNameInput.setPlace(list.size(), 159, 722, 360, 54);  // 设置位置和尺寸
        saveNameInput.setName("autoSaveNameInput");  // 设置节点名称
        saveNameInput.setParentNode(parent);  // 设置父节点
        saveNameInput.setParentNodes(parents);  // 设置父节点列表
        saveNameInput.setChildNodes(getTextKeyBoardNodeList(saveNameInput, list));  // 设置子节点列表
        saveNameInput.setDialog(ExternalKeysNode.DIALOG_TEXTKEYBOARD);  // 关联弹窗:文本键盘
        list.add(saveNameInput);  // 添加节点到列表


        ExternalKeysNode suffixNum = new ExternalKeysNode();  // 创建后缀序号节点
        suffixNum.setPlace(list.size(), 809, 722, 120, 54);  // 设置位置和尺寸
        suffixNum.setName("autoSaveCsvSuffixNum");  // 设置节点名称
        suffixNum.setParentNode(parent);  // 设置父节点
        suffixNum.setParentNodes(parents);  // 设置父节点列表
        suffixNum.setChildNodes(getNumberKeyBoardNodeList(suffixNum, list, false));  // 设置子节点列表
        for (int i = 0; i < suffixNum.getChildNodes().size(); i++) {  // 循环创建节点
            suffixNum.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        suffixNum.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(suffixNum);  // 添加节点到列表

        ExternalKeysNode saveNameConfirm = new ExternalKeysNode();  // 创建保存确认节点
        saveNameConfirm.setPlace(list.size(), 979, 722, 120, 54);  // 设置位置和尺寸
        saveNameConfirm.setName("autoSaveStart");  // 设置节点名称
        saveNameConfirm.setParentNode(parent);  // 设置父节点
        saveNameConfirm.setParentNodes(parents);  // 设置父节点列表
        list.add(saveNameConfirm);  // 添加节点到列表

        return list;  // 返回节点列表

    }

    /**
     * 构建Invoke-Wave波形调出详情节点：Spinner/Browse/Invoke/ShowFileOnly
     */
    private static List<ExternalKeysNode> getTopInvokeWavDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode spinner = new ExternalKeysNode();  // 创建下拉列表节点
        spinner.setPlace(list.size(), 154, 308, 600, 60);  // 设置位置和尺寸
        spinner.setName("InvokeWavSpinner");  // 设置节点名称
        spinner.setParentNode(parent);  // 设置父节点
        spinner.setParentNodes(parents);  // 设置父节点列表
        list.add(spinner);  // 添加节点到列表

        ExternalKeysNode browse = new ExternalKeysNode();  // 创建浏览节点
        browse.setPlace(list.size(), 804, 308, 120, 60);  // 设置位置和尺寸
        browse.setName("InvokeWavBrowse");  // 设置节点名称
        browse.setParentNode(parent);  // 设置父节点
        browse.setParentNodes(parents);  // 设置父节点列表
        browse.setChildNodes(null);  // 子节点置空
        list.add(browse);  // 添加节点到列表

        ExternalKeysNode invoke = new ExternalKeysNode();  // 创建调出节点
        invoke.setPlace(list.size(), 974, 308, 120, 60);  // 设置位置和尺寸
        invoke.setName("InvokeWav");  // 设置节点名称
        invoke.setParentNode(parent);  // 设置父节点
        invoke.setParentNodes(parents);  // 设置父节点列表
        list.add(invoke);  // 添加节点到列表

        ExternalKeysNode showFileOnly = new ExternalKeysNode();  // 创建仅显示文件节点
        showFileOnly.setPlace(list.size(), 1268, 316, 40, 40);  // 设置位置和尺寸
        showFileOnly.setName("InvokeWavShowFileOnly");  // 设置节点名称
        showFileOnly.setParentNode(parent);  // 设置父节点
        showFileOnly.setParentNodes(parents);  // 设置父节点列表
        list.add(showFileOnly);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Invoke-CSV调出详情节点：Spinner/Browse/Invoke/ShowFileOnly
     */
    private static List<ExternalKeysNode> getTopInvokeCsvDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode spinner = new ExternalKeysNode();  // 创建下拉列表节点
        spinner.setPlace(list.size(), 154, 308, 600, 60);  // 设置位置和尺寸
        spinner.setName("InvokeCsvSpinner");  // 设置节点名称
        spinner.setParentNode(parent);  // 设置父节点
        spinner.setParentNodes(parents);  // 设置父节点列表
        list.add(spinner);  // 添加节点到列表

        ExternalKeysNode browse = new ExternalKeysNode();  // 创建浏览节点
        browse.setPlace(list.size(), 804, 308, 120, 60);  // 设置位置和尺寸
        browse.setName("InvokeCsvBrowse");  // 设置节点名称
        browse.setParentNode(parent);  // 设置父节点
        browse.setParentNodes(parents);  // 设置父节点列表
        browse.setChildNodes(null);  // 子节点置空
        list.add(browse);  // 添加节点到列表

        ExternalKeysNode invoke = new ExternalKeysNode();  // 创建调出节点
        invoke.setPlace(list.size(), 974, 308, 120, 60);  // 设置位置和尺寸
        invoke.setName("InvokeCsv");  // 设置节点名称
        invoke.setParentNode(parent);  // 设置父节点
        invoke.setParentNodes(parents);  // 设置父节点列表
        list.add(invoke);  // 添加节点到列表

        ExternalKeysNode showFileOnly = new ExternalKeysNode();  // 创建仅显示文件节点
        showFileOnly.setPlace(list.size(), 1268, 316, 40, 40);  // 设置位置和尺寸
        showFileOnly.setName("InvokeCsvShowFileOnly");  // 设置节点名称
        showFileOnly.setParentNode(parent);  // 设置父节点
        showFileOnly.setParentNodes(parents);  // 设置父节点列表
        list.add(showFileOnly);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Invoke-Setting设置调出详情节点：Spinner/Browse/Invoke/ShowFileOnly
     */
    private static List<ExternalKeysNode> getTopInvokeSettingDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode spinner = new ExternalKeysNode();  // 创建下拉列表节点
        spinner.setPlace(list.size(), 154, 308, 600, 60);  // 设置位置和尺寸
        spinner.setName("InvokeSettingSpinner");  // 设置节点名称
        spinner.setParentNode(parent);  // 设置父节点
        spinner.setParentNodes(parents);  // 设置父节点列表
        list.add(spinner);  // 添加节点到列表

        ExternalKeysNode browse = new ExternalKeysNode();  // 创建浏览节点
        browse.setPlace(list.size(), 804, 308, 120, 60);  // 设置位置和尺寸
        browse.setName("InvokeSettingBrowse");  // 设置节点名称
        browse.setParentNode(parent);  // 设置父节点
        browse.setParentNodes(parents);  // 设置父节点列表
        browse.setChildNodes(null);  // 子节点置空
        list.add(browse);  // 添加节点到列表

        ExternalKeysNode invoke = new ExternalKeysNode();  // 创建调出节点
        invoke.setPlace(list.size(), 974, 308, 120, 60);  // 设置位置和尺寸
        invoke.setName("InvokeSetting");  // 设置节点名称
        invoke.setParentNode(parent);  // 设置父节点
        invoke.setParentNodes(parents);  // 设置父节点列表
        list.add(invoke);  // 添加节点到列表

        ExternalKeysNode showFileOnly = new ExternalKeysNode();  // 创建仅显示文件节点
        showFileOnly.setPlace(list.size(), 1268, 316, 40, 40);  // 设置位置和尺寸
        showFileOnly.setName("InvokeSettingShowFileOnly");  // 设置节点名称
        showFileOnly.setParentNode(parent);  // 设置父节点
        showFileOnly.setParentNodes(parents);  // 设置父节点列表
        list.add(showFileOnly);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Invoke-Session会话调出详情节点：Spinner/Browse/Invoke/ShowFileOnly
     */
    private static List<ExternalKeysNode> getTopInvokeSessionDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode spinner = new ExternalKeysNode();  // 创建下拉列表节点
        spinner.setPlace(list.size(), 154, 308, 600, 60);  // 设置位置和尺寸
        spinner.setName("InvokeSessionSpinner");  // 设置节点名称
        spinner.setParentNode(parent);  // 设置父节点
        spinner.setParentNodes(parents);  // 设置父节点列表
        list.add(spinner);  // 添加节点到列表

        ExternalKeysNode browse = new ExternalKeysNode();  // 创建浏览节点
        browse.setPlace(list.size(), 804, 308, 120, 60);  // 设置位置和尺寸
        browse.setName("InvokeSessionBrowse");  // 设置节点名称
        browse.setParentNode(parent);  // 设置父节点
        browse.setParentNodes(parents);  // 设置父节点列表
        browse.setChildNodes(null);  // 子节点置空
        list.add(browse);  // 添加节点到列表

        ExternalKeysNode invoke = new ExternalKeysNode();  // 创建调出节点
        invoke.setPlace(list.size(), 974, 308, 120, 60);  // 设置位置和尺寸
        invoke.setName("InvokeSession");  // 设置节点名称
        invoke.setParentNode(parent);  // 设置父节点
        invoke.setParentNodes(parents);  // 设置父节点列表
        list.add(invoke);  // 添加节点到列表

        ExternalKeysNode showFileOnly = new ExternalKeysNode();  // 创建仅显示文件节点
        showFileOnly.setPlace(list.size(), 1268, 316, 40, 40);  // 设置位置和尺寸
        showFileOnly.setName("InvokeSessionShowFileOnly");  // 设置节点名称
        showFileOnly.setParentNode(parent);  // 设置父节点
        showFileOnly.setParentNodes(parents);  // 设置父节点列表
        list.add(showFileOnly);  // 添加节点到列表

        return list;  // 返回节点列表
    }


    /**
     * 构建Cursor-Setting光标设置节点：追踪/固定标签
     */
    private static List<ExternalKeysNode> getTopCursorSettingNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode trace = new ExternalKeysNode();  // 创建追踪节点
        trace.setPlace(list.size(), 170, 207 + topSlipOffset, 73, 36);  // 设置位置和尺寸
        trace.setName("cursorTrace");  // 设置节点名称
        trace.setParentNode(parent);  // 设置父节点
        trace.setParentNodes(parents);  // 设置父节点列表
        list.add(trace);  // 添加节点到列表

        ExternalKeysNode fixLabel = new ExternalKeysNode();  // 创建固定标签节点
        fixLabel.setPlace(list.size(), 396, 243, 72, 36);  // 设置位置和尺寸
        fixLabel.setName("fixLabel");  // 设置节点名称
        fixLabel.setParentNode(parent);  // 设置父节点
        fixLabel.setParentNodes(parents);  // 设置父节点列表
        list.add(fixLabel);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Save-Wave波形保存节点：通道选择/Spinner/Browse/文件名/序号/确认/存入Ref
     */
    private static List<ExternalKeysNode> getTopSaveWaveNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        int count = ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT + ChannelFactory.REF_CNT;  // 物理通道数量
        for (int i = 0, x = 11; i < count; i++) {  // 遍历通道创建节点
            ExternalKeysNode channel = new ExternalKeysNode();  // 创建通道节点
            int width = 97;//channel
            if (i >= ChannelFactory.CH_CNT && i < ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT) {  // 判断是否为Math通道范围
                width = 87;//Math
            } else if (i >= ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT && i < count) {  // 物理通道数量
                width = 82;//Ref
            }
            channel.setPlace(list.size(), 30 + 127 * i, 305, width, 50);  // 设置位置和尺寸
            channel.setName("SaveWave,channelIndex:" + i);  // 设置节点名称
//            x += width;
            channel.setParentNode(parent);  // 设置父节点
            channel.setParentNodes(parents);  // 设置父节点列表
            channel.setType(ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_SAVE_WAVE);  // 类型:主菜单-通道保存波形
            list.add(channel);  // 添加节点到列表
        }


        ExternalKeysNode spinner = new ExternalKeysNode();  // 创建下拉列表节点
        spinner.setPlace(list.size(), 154, 386, 600, 60);  // 设置位置和尺寸
        spinner.setName("wavSpinner");  // 设置节点名称
        spinner.setParentNode(parent);  // 设置父节点
        spinner.setParentNodes(parents);  // 设置父节点列表
        spinner.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        spinner.setChildNodes(getSaveWavSpinnerDetailList(spinner, list));  // 设置子节点列表
        list.add(spinner);  // 添加节点到列表

        ExternalKeysNode browse = new ExternalKeysNode();  // 创建浏览节点
        browse.setPlace(list.size(), 804, 386, 120, 60);  // 设置位置和尺寸
        browse.setName("wavBrowse");  // 设置节点名称
        browse.setParentNode(parent);  // 设置父节点
        browse.setParentNodes(parents);  // 设置父节点列表
        browse.setChildNodes(null);  // 子节点置空
        list.add(browse);  // 添加节点到列表


        ExternalKeysNode saveNameInput = new ExternalKeysNode();  // 创建文件名输入节点
        saveNameInput.setPlace(list.size(), 154, 458, 360, 54);  // 设置位置和尺寸
        saveNameInput.setName("saveNameInput");  // 设置节点名称
        saveNameInput.setParentNode(parent);  // 设置父节点
        saveNameInput.setParentNodes(parents);  // 设置父节点列表
        saveNameInput.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        saveNameInput.setChildNodes(getTextKeyBoardNodeList(saveNameInput, list));  // 设置子节点列表
        saveNameInput.setDialog(ExternalKeysNode.DIALOG_TEXTKEYBOARD);  // 关联弹窗:文本键盘
        list.add(saveNameInput);  // 添加节点到列表


        ExternalKeysNode checkFnAdd = new ExternalKeysNode();  // 创建文件名后缀开关节点
        checkFnAdd.setPlace(list.size(), 554, 465, 40, 40);  // 设置位置和尺寸
        checkFnAdd.setName("wavFileNameAdd");  // 设置节点名称
        checkFnAdd.setParentNode(parent);  // 设置父节点
        checkFnAdd.setParentNodes(parents);  // 设置父节点列表
        list.add(checkFnAdd);  // 添加节点到列表


        ExternalKeysNode suffixNum = new ExternalKeysNode();  // 创建后缀序号节点
        suffixNum.setPlace(list.size(), 804, 458, 120, 54);  // 设置位置和尺寸
        suffixNum.setName("WavSuffixNum");  // 设置节点名称
        suffixNum.setParentNode(parent);  // 设置父节点
        suffixNum.setParentNodes(parents);  // 设置父节点列表
        suffixNum.setChildNodes(getNumberKeyBoardNodeList(suffixNum, list, false));  // 设置子节点列表
        for (int i = 0; i < suffixNum.getChildNodes().size(); i++) {  // 循环创建节点
            suffixNum.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        suffixNum.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        suffixNum.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(suffixNum);  // 添加节点到列表

        ExternalKeysNode saveNameConfirm = new ExternalKeysNode();  // 创建保存确认节点
        saveNameConfirm.setPlace(list.size(), 974, 458, 120, 54);  // 设置位置和尺寸
        saveNameConfirm.setName("saveNameConfirm");  // 设置节点名称
        saveNameConfirm.setParentNode(parent);  // 设置父节点
        saveNameConfirm.setParentNodes(parents);  // 设置父节点列表
        list.add(saveNameConfirm);  // 添加节点到列表


        for (int i = 0; i < GlobalVar.get().getChannelsCount(); i++) {  // 遍历通道创建节点
            ExternalKeysNode saveTo = new ExternalKeysNode();  // 创建节点节点
            saveTo.setPlace(list.size(), 154 + 120 * i, 524, 120, 60);  // 设置位置和尺寸
            saveTo.setName("R" + (i + 1));  // 设置节点名称
            saveTo.setParentNode(parent);  // 设置父节点
            saveTo.setParentNodes(parents);  // 设置父节点列表
            list.add(saveTo);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Save-Wave的Spinner下拉列表节点
     */
    private static List<ExternalKeysNode> getSaveWavSpinnerDetailList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode spinnerItem = new ExternalKeysNode();  // 创建节点节点
        spinnerItem.setParentNode(parent);  // 设置父节点
        spinnerItem.setParentNodes(parents);  // 设置父节点列表
        spinnerItem.setPlace(list.size(), 154, 386, 600, 60);  // 设置位置和尺寸
        spinnerItem.setName("SaveWavSpinnerList");  // 设置节点名称
        spinnerItem.setType(ExternalKeysNode.TYPE_SPINNER_LIST);  // 类型:下拉列表
        list.add(spinnerItem);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Save-CSV保存节点：通道选择/Spinner/Browse/文件名/序号/确认
     */
    private static List<ExternalKeysNode> getTopSaveCsvNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        int count = ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT + ChannelFactory.REF_CNT;  // 物理通道数量
        for (int i = 0, x = 11; i <= count; i++) {  // 遍历通道创建节点
            ExternalKeysNode channel = new ExternalKeysNode();  // 创建通道节点
            int width = 97;//channel
            if (i >= ChannelFactory.CH_CNT && i < ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT) {  // 判断是否为Math通道范围
                width = 87;//Math
            } else if (i >= ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT && i < count) {  // 物理通道数量
                width = 82;//Ref
            }
            channel.setPlace(list.size(), 30 + 127 * i, 305, width, 50);  // 设置位置和尺寸
            channel.setName("SaveCsv,channelIndex:" + i);  // 设置节点名称
//            x += width;
            channel.setParentNode(parent);  // 设置父节点
            channel.setParentNodes(parents);  // 设置父节点列表
            channel.setType(ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_SAVE_CSV);  // 类型:主菜单-通道保存CSV
            list.add(channel);  // 添加节点到列表
        }


        ExternalKeysNode spinner = new ExternalKeysNode();  // 创建下拉列表节点
        spinner.setPlace(list.size(), 154, 386, 600, 60);  // 设置位置和尺寸
        spinner.setName("csvSpinner");  // 设置节点名称
        spinner.setParentNode(parent);  // 设置父节点
        spinner.setParentNodes(parents);  // 设置父节点列表
        list.add(spinner);  // 添加节点到列表

        ExternalKeysNode browse = new ExternalKeysNode();  // 创建浏览节点
        browse.setPlace(list.size(), 804, 386, 120, 60);  // 设置位置和尺寸
        browse.setName("csvBrowse");  // 设置节点名称
        browse.setParentNode(parent);  // 设置父节点
        browse.setParentNodes(parents);  // 设置父节点列表
        browse.setChildNodes(null);  // 子节点置空
        list.add(browse);  // 添加节点到列表


        ExternalKeysNode saveNameInput = new ExternalKeysNode();  // 创建文件名输入节点
        saveNameInput.setPlace(list.size(), 154, 458, 360, 54);  // 设置位置和尺寸
        saveNameInput.setName("saveNameInput");  // 设置节点名称
        saveNameInput.setParentNode(parent);  // 设置父节点
        saveNameInput.setParentNodes(parents);  // 设置父节点列表
        saveNameInput.setChildNodes(getTextKeyBoardNodeList(saveNameInput, list));  // 设置子节点列表
        saveNameInput.setDialog(ExternalKeysNode.DIALOG_TEXTKEYBOARD);  // 关联弹窗:文本键盘
        list.add(saveNameInput);  // 添加节点到列表


        ExternalKeysNode checkFnAdd = new ExternalKeysNode();  // 创建文件名后缀开关节点
        checkFnAdd.setPlace(list.size(), 554, 465, 40, 40);  // 设置位置和尺寸
        checkFnAdd.setName("csvFileNameAdd");  // 设置节点名称
        checkFnAdd.setParentNode(parent);  // 设置父节点
        checkFnAdd.setParentNodes(parents);  // 设置父节点列表
        list.add(checkFnAdd);  // 添加节点到列表


        ExternalKeysNode suffixNum = new ExternalKeysNode();  // 创建后缀序号节点
        suffixNum.setPlace(list.size(), 804, 458, 120, 54);  // 设置位置和尺寸
        suffixNum.setName("CsvSuffixNum");  // 设置节点名称
        suffixNum.setParentNode(parent);  // 设置父节点
        suffixNum.setParentNodes(parents);  // 设置父节点列表
        suffixNum.setChildNodes(getNumberKeyBoardNodeList(suffixNum, list, false));  // 设置子节点列表
        for (int i = 0; i < suffixNum.getChildNodes().size(); i++) {  // 循环创建节点
            suffixNum.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        suffixNum.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(suffixNum);  // 添加节点到列表

        ExternalKeysNode saveNameConfirm = new ExternalKeysNode();  // 创建保存确认节点
        saveNameConfirm.setPlace(list.size(), 974, 458, 120, 54);  // 设置位置和尺寸
        saveNameConfirm.setName("saveNameConfirm");  // 设置节点名称
        saveNameConfirm.setParentNode(parent);  // 设置父节点
        saveNameConfirm.setParentNodes(parents);  // 设置父节点列表
        list.add(saveNameConfirm);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Save-BIN二进制保存节点：通道选择/Spinner/Browse/AllSegments/文件名/序号/确认
     */
    private static List<ExternalKeysNode> getTopSaveBinNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        int count = ChannelFactory.CH_CNT;  // 物理通道数量
        for (int i = 0, x = 11; i < count; i++) {  // 遍历通道创建节点
            ExternalKeysNode channel = new ExternalKeysNode();  // 创建通道节点
            int width = 97;//channel
            channel.setPlace(list.size(), 30 + 127 * i, 305, width, 50);  // 设置位置和尺寸
            channel.setName("SaveBin,channelIndex:" + i);  // 设置节点名称
//            x += width;
            channel.setParentNode(parent);  // 设置父节点
            channel.setParentNodes(parents);  // 设置父节点列表
            channel.setType(ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_SAVE_BIN);  // 类型:主菜单-通道保存BIN
            list.add(channel);  // 添加节点到列表
        }


        ExternalKeysNode spinner = new ExternalKeysNode();  // 创建下拉列表节点
        spinner.setPlace(list.size(), 154, 386, 600, 60);  // 设置位置和尺寸
        spinner.setName("binSpinner");  // 设置节点名称
        spinner.setParentNode(parent);  // 设置父节点
        spinner.setParentNodes(parents);  // 设置父节点列表
        list.add(spinner);  // 添加节点到列表

        ExternalKeysNode browse = new ExternalKeysNode();  // 创建浏览节点
        browse.setPlace(list.size(), 804, 386, 120, 60);  // 设置位置和尺寸
        browse.setName("binBrowse");  // 设置节点名称
        browse.setParentNode(parent);  // 设置父节点
        browse.setParentNodes(parents);  // 设置父节点列表
        browse.setChildNodes(null);  // 子节点置空
        list.add(browse);  // 添加节点到列表


        ExternalKeysNode allSegment = new ExternalKeysNode();  // 创建全部段节点
        allSegment.setPlace(list.size(), 1650, 386, 120, 60);  // 设置位置和尺寸
        allSegment.setName("All Segments");  // 设置节点名称
        allSegment.setParentNode(parent);  // 设置父节点
        allSegment.setParentNodes(parents);  // 设置父节点列表
        allSegment.setChildNodes(null);  // 子节点置空
        list.add(allSegment);  // 添加节点到列表


        ExternalKeysNode saveNameInput = new ExternalKeysNode();  // 创建文件名输入节点
        saveNameInput.setPlace(list.size(), 154, 458, 360, 54);  // 设置位置和尺寸
        saveNameInput.setName("saveNameInput");  // 设置节点名称
        saveNameInput.setParentNode(parent);  // 设置父节点
        saveNameInput.setParentNodes(parents);  // 设置父节点列表
        saveNameInput.setChildNodes(getTextKeyBoardNodeList(saveNameInput, list));  // 设置子节点列表
        saveNameInput.setDialog(ExternalKeysNode.DIALOG_TEXTKEYBOARD);  // 关联弹窗:文本键盘
        list.add(saveNameInput);  // 添加节点到列表


        ExternalKeysNode checkFnAdd = new ExternalKeysNode();  // 创建文件名后缀开关节点
        checkFnAdd.setPlace(list.size(), 554, 465, 40, 40);  // 设置位置和尺寸
        checkFnAdd.setName("binFileNameAdd");  // 设置节点名称
        checkFnAdd.setParentNode(parent);  // 设置父节点
        checkFnAdd.setParentNodes(parents);  // 设置父节点列表
        list.add(checkFnAdd);  // 添加节点到列表


        ExternalKeysNode suffixNum = new ExternalKeysNode();  // 创建后缀序号节点
        suffixNum.setPlace(list.size(), 804, 458, 120, 54);  // 设置位置和尺寸
        suffixNum.setName("BinSuffixNum");  // 设置节点名称
        suffixNum.setParentNode(parent);  // 设置父节点
        suffixNum.setParentNodes(parents);  // 设置父节点列表
        suffixNum.setChildNodes(getNumberKeyBoardNodeList(suffixNum, list, false));  // 设置子节点列表
        for (int i = 0; i < suffixNum.getChildNodes().size(); i++) {  // 循环创建节点
            suffixNum.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        suffixNum.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(suffixNum);  // 添加节点到列表

        ExternalKeysNode saveNameConfirm = new ExternalKeysNode();  // 创建保存确认节点
        saveNameConfirm.setPlace(list.size(), 974, 458, 120, 54);  // 设置位置和尺寸
        saveNameConfirm.setName("saveNameConfirm");  // 设置节点名称
        saveNameConfirm.setParentNode(parent);  // 设置父节点
        saveNameConfirm.setParentNodes(parents);  // 设置父节点列表
        list.add(saveNameConfirm);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Save-Setting设置保存节点：Spinner/Browse/文件名/序号/确认
     */
    private static List<ExternalKeysNode> getTopSaveSettingDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode spinner = new ExternalKeysNode();  // 创建下拉列表节点
        spinner.setPlace(list.size(), 154, 308, 600, 60);  // 设置位置和尺寸
        spinner.setName("SettingSpinner");  // 设置节点名称
        spinner.setParentNode(parent);  // 设置父节点
        spinner.setParentNodes(parents);  // 设置父节点列表
        list.add(spinner);  // 添加节点到列表

        ExternalKeysNode browse = new ExternalKeysNode();  // 创建浏览节点
        browse.setPlace(list.size(), 804, 308, 120, 60);  // 设置位置和尺寸
        browse.setName("SettingBrowse");  // 设置节点名称
        browse.setParentNode(parent);  // 设置父节点
        browse.setParentNodes(parents);  // 设置父节点列表
        browse.setChildNodes(null);  // 子节点置空
        list.add(browse);  // 添加节点到列表


        ExternalKeysNode saveNameInput = new ExternalKeysNode();  // 创建文件名输入节点
        saveNameInput.setPlace(list.size(), 154, 380, 360, 54);  // 设置位置和尺寸
        saveNameInput.setName("saveNameInput");  // 设置节点名称
        saveNameInput.setParentNode(parent);  // 设置父节点
        saveNameInput.setParentNodes(parents);  // 设置父节点列表
        saveNameInput.setChildNodes(getTextKeyBoardNodeList(saveNameInput, list));  // 设置子节点列表
        saveNameInput.setDialog(ExternalKeysNode.DIALOG_TEXTKEYBOARD);  // 关联弹窗:文本键盘
        list.add(saveNameInput);  // 添加节点到列表


        ExternalKeysNode checkFnAdd = new ExternalKeysNode();  // 创建文件名后缀开关节点
        checkFnAdd.setPlace(list.size(), 554, 387, 40, 40);  // 设置位置和尺寸
        checkFnAdd.setName("SettingFileNameAdd");  // 设置节点名称
        checkFnAdd.setParentNode(parent);  // 设置父节点
        checkFnAdd.setParentNodes(parents);  // 设置父节点列表
        list.add(checkFnAdd);  // 添加节点到列表


        ExternalKeysNode suffixNum = new ExternalKeysNode();  // 创建后缀序号节点
        suffixNum.setPlace(list.size(), 804, 380, 120, 54);  // 设置位置和尺寸
        suffixNum.setName("SettingSuffixNum");  // 设置节点名称
        suffixNum.setParentNode(parent);  // 设置父节点
        suffixNum.setParentNodes(parents);  // 设置父节点列表
        suffixNum.setChildNodes(getNumberKeyBoardNodeList(suffixNum, list, false));  // 设置子节点列表
        for (int i = 0; i < suffixNum.getChildNodes().size(); i++) {  // 循环创建节点
            suffixNum.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        suffixNum.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(suffixNum);  // 添加节点到列表

        ExternalKeysNode saveNameConfirm = new ExternalKeysNode();  // 创建保存确认节点
        saveNameConfirm.setPlace(list.size(), 974, 380, 120, 54);  // 设置位置和尺寸
        saveNameConfirm.setName("saveNameConfirm");  // 设置节点名称
        saveNameConfirm.setParentNode(parent);  // 设置父节点
        saveNameConfirm.setParentNodes(parents);  // 设置父节点列表
        list.add(saveNameConfirm);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Save-Session会话保存节点：Spinner/Browse/文件名/序号/确认
     */
    private static List<ExternalKeysNode> getTopSaveSessionDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode spinner = new ExternalKeysNode();  // 创建下拉列表节点
        spinner.setPlace(list.size(), 154, 308, 600, 60);  // 设置位置和尺寸
        spinner.setName("SessionSpinner");  // 设置节点名称
        spinner.setParentNode(parent);  // 设置父节点
        spinner.setParentNodes(parents);  // 设置父节点列表
        list.add(spinner);  // 添加节点到列表

        ExternalKeysNode browse = new ExternalKeysNode();  // 创建浏览节点
        browse.setPlace(list.size(), 804, 308, 120, 60);  // 设置位置和尺寸
        browse.setName("SessionBrowse");  // 设置节点名称
        browse.setParentNode(parent);  // 设置父节点
        browse.setParentNodes(parents);  // 设置父节点列表
        browse.setChildNodes(null);  // 子节点置空
        list.add(browse);  // 添加节点到列表


        ExternalKeysNode saveNameInput = new ExternalKeysNode();  // 创建文件名输入节点
        saveNameInput.setPlace(list.size(), 154, 380, 360, 54);  // 设置位置和尺寸
        saveNameInput.setName("saveNameInput");  // 设置节点名称
        saveNameInput.setParentNode(parent);  // 设置父节点
        saveNameInput.setParentNodes(parents);  // 设置父节点列表
        saveNameInput.setChildNodes(getTextKeyBoardNodeList(saveNameInput, list));  // 设置子节点列表
        saveNameInput.setDialog(ExternalKeysNode.DIALOG_TEXTKEYBOARD);  // 关联弹窗:文本键盘
        list.add(saveNameInput);  // 添加节点到列表


        ExternalKeysNode checkFnAdd = new ExternalKeysNode();  // 创建文件名后缀开关节点
        checkFnAdd.setPlace(list.size(), 554, 387, 40, 40);  // 设置位置和尺寸
        checkFnAdd.setName("SessionFileNameAdd");  // 设置节点名称
        checkFnAdd.setParentNode(parent);  // 设置父节点
        checkFnAdd.setParentNodes(parents);  // 设置父节点列表
        list.add(checkFnAdd);  // 添加节点到列表


        ExternalKeysNode suffixNum = new ExternalKeysNode();  // 创建后缀序号节点
        suffixNum.setPlace(list.size(), 804, 380, 120, 54);  // 设置位置和尺寸
        suffixNum.setName("SessionSuffixNum");  // 设置节点名称
        suffixNum.setParentNode(parent);  // 设置父节点
        suffixNum.setParentNodes(parents);  // 设置父节点列表
        suffixNum.setChildNodes(getNumberKeyBoardNodeList(suffixNum, list, false));  // 设置子节点列表
        for (int i = 0; i < suffixNum.getChildNodes().size(); i++) {  // 循环创建节点
            suffixNum.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        suffixNum.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(suffixNum);  // 添加节点到列表

        ExternalKeysNode saveNameConfirm = new ExternalKeysNode();  // 创建保存确认节点
        saveNameConfirm.setPlace(list.size(), 974, 380, 120, 54);  // 设置位置和尺寸
        saveNameConfirm.setName("saveNameConfirm");  // 设置节点名称
        saveNameConfirm.setParentNode(parent);  // 设置父节点
        saveNameConfirm.setParentNodes(parents);  // 设置父节点列表
        list.add(saveNameConfirm);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Save-Picture图片保存节点：Spinner/Browse/文件名/序号/确认/时间戳/反色/缩略图
     */
    private static List<ExternalKeysNode> getTopSavePictureDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode spinner = new ExternalKeysNode();  // 创建下拉列表节点
        spinner.setPlace(list.size(), 154, 308, 600, 60);  // 设置位置和尺寸
        spinner.setName("SessionSpinner");  // 设置节点名称
        spinner.setParentNode(parent);  // 设置父节点
        spinner.setParentNodes(parents);  // 设置父节点列表
        list.add(spinner);  // 添加节点到列表

        ExternalKeysNode browse = new ExternalKeysNode();  // 创建浏览节点
        browse.setPlace(list.size(), 804, 308, 120, 60);  // 设置位置和尺寸
        browse.setName("SessionBrowse");  // 设置节点名称
        browse.setParentNode(parent);  // 设置父节点
        browse.setParentNodes(parents);  // 设置父节点列表
        browse.setChildNodes(null);  // 子节点置空
        list.add(browse);  // 添加节点到列表

        ExternalKeysNode saveNameInput = new ExternalKeysNode();  // 创建文件名输入节点
        saveNameInput.setPlace(list.size(), 154, 380, 360, 54);  // 设置位置和尺寸
        saveNameInput.setName("SaveNameInput");  // 设置节点名称
        saveNameInput.setParentNode(parent);  // 设置父节点
        saveNameInput.setParentNodes(parents);  // 设置父节点列表
        saveNameInput.setChildNodes(getTextKeyBoardNodeList(saveNameInput, list));  // 设置子节点列表
        saveNameInput.setDialog(ExternalKeysNode.DIALOG_TEXTKEYBOARD);  // 关联弹窗:文本键盘
        list.add(saveNameInput);  // 添加节点到列表


        ExternalKeysNode checkFnAdd = new ExternalKeysNode();  // 创建文件名后缀开关节点
        checkFnAdd.setPlace(list.size(), 554, 387, 40, 40);  // 设置位置和尺寸
        checkFnAdd.setName("PictureFileNameAdd");  // 设置节点名称
        checkFnAdd.setParentNode(parent);  // 设置父节点
        checkFnAdd.setParentNodes(parents);  // 设置父节点列表
        list.add(checkFnAdd);  // 添加节点到列表


        ExternalKeysNode suffixNum = new ExternalKeysNode();  // 创建后缀序号节点
        suffixNum.setPlace(list.size(), 804, 380, 120, 54);  // 设置位置和尺寸
        suffixNum.setName("PictureSuffixNum");  // 设置节点名称
        suffixNum.setParentNode(parent);  // 设置父节点
        suffixNum.setParentNodes(parents);  // 设置父节点列表
        suffixNum.setChildNodes(getNumberKeyBoardNodeList(suffixNum, list, false));  // 设置子节点列表
        for (int i = 0; i < suffixNum.getChildNodes().size(); i++) {  // 循环创建节点
            suffixNum.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        suffixNum.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(suffixNum);  // 添加节点到列表

        ExternalKeysNode saveNameConfirm = new ExternalKeysNode();  // 创建保存确认节点
        saveNameConfirm.setPlace(list.size(), 974, 380, 120, 54);  // 设置位置和尺寸
        saveNameConfirm.setName("saveNameConfirm");  // 设置节点名称
        saveNameConfirm.setParentNode(parent);  // 设置父节点
        saveNameConfirm.setParentNodes(parents);  // 设置父节点列表
        list.add(saveNameConfirm);  // 添加节点到列表

        ExternalKeysNode timeStamp = new ExternalKeysNode();  // 创建时间戳节点
        timeStamp.setParentNode(parent);  // 设置父节点
        timeStamp.setParentNodes(parents);  // 设置父节点列表
        timeStamp.setPlace(list.size(), 148, 443, 72, 36);  // 设置位置和尺寸
        timeStamp.setName("capture:timeStamp");  // 设置节点名称
        list.add(timeStamp);  // 添加节点到列表

        ExternalKeysNode invert = new ExternalKeysNode();  // 创建反色节点
        invert.setParentNode(parent);  // 设置父节点
        invert.setParentNodes(parents);  // 设置父节点列表
        invert.setPlace(list.size(), 396, 443, 72, 36);  // 设置位置和尺寸
        invert.setName("capture:invert");  // 设置节点名称
        list.add(invert);  // 添加节点到列表

        ExternalKeysNode thumbnail = new ExternalKeysNode();  // 创建缩略图节点
        thumbnail.setParentNode(parent);  // 设置父节点
        thumbnail.setParentNodes(parents);  // 设置父节点列表
        thumbnail.setPlace(list.size(), 644, 443, 72, 36);  // 设置位置和尺寸
        thumbnail.setName("capture:thumbnail");  // 设置节点名称
        list.add(thumbnail);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    //endregion

    //region TopSample
    /**
     * 构建Sample菜单的二级子菜单节点：Mode(模式)/Depth(存储深度)/Segmented(分段)
     */
    private static List<ExternalKeysNode> getTopSampleNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        String[] strings = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.sample);  // 从资源文件获取字符串数组
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode mode = new ExternalKeysNode();  // 创建模式节点
        mode.setPlace(list.size(), 0, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        mode.setName(strings[0]);  // 设置节点名称
        mode.setParentNode(parent);  // 设置父节点
        mode.setParentNodes(parents);  // 设置父节点列表
        mode.setChildNodes(getTopSampleModeNodeList(mode, list, topSlipOffset));  // 设置子节点列表
        mode.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE));  // 设置当前选中项
        mode.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(mode);  // 添加节点到列表
        ExternalKeysNode depth = new ExternalKeysNode();  // 创建深度节点
        depth.setPlace(list.size(), 145, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        depth.setName(strings[1]);  // 设置节点名称
        depth.setParentNode(parent);  // 设置父节点
        depth.setParentNodes(parents);  // 设置父节点列表
        depth.setChildNodes(getTopSampleDepthNodeList(depth, list, topSlipOffset));  // 设置子节点列表
        depth.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(depth);  // 添加节点到列表
        ExternalKeysNode segmented = new ExternalKeysNode();  // 创建分段存储节点
        segmented.setPlace(list.size(), 290, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        segmented.setName(strings[2]);  // 设置节点名称
        segmented.setParentNode(parent);  // 设置父节点
        segmented.setParentNodes(parents);  // 设置父节点列表
        segmented.setChildNodes(getTopSampleSegmentedNodeList(segmented, list, topSlipOffset));  // 设置子节点列表
//        segmented.setVisible(false);
        segmented.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(segmented);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Sample-Mode采样模式节点：模式选项/详细调节
     */
    private static List<ExternalKeysNode> getTopSampleModeNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        String[] strings = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.sampleMode);  // 从资源文件获取字符串数组
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < strings.length; i++) {  // 遍历选项创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setPlace(list.size(), 149 + 120 * i, 205 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            node.setName(strings[i]);  // 设置节点名称
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE));  // 设置当前选中项
            list.add(node);  // 添加节点到列表
        }

        ExternalKeysNode selectHead = new ExternalKeysNode();  // 创建调节选择节点
        selectHead.setPlace(list.size(), 745, 205 + topSlipOffset, 150, 60);  // 设置位置和尺寸
        selectHead.setName("sampleModeDetail");  // 设置节点名称
        selectHead.setChildNodes(getTopSampleModeDetailNodeList(selectHead, list, topSlipOffset));  // 设置子节点列表
        selectHead.setParentNode(parent);  // 设置父节点
        selectHead.setParentNodes(parents);  // 设置父节点列表
        list.add(selectHead);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Sample-Mode采样模式详细调节节点：滑动条
     */
    private static List<ExternalKeysNode> getTopSampleModeDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode index = new ExternalKeysNode();  // 创建索引节点节点
        index.setPlace(list.size(), 0, 270 + topSlipOffset, 1800, 62);  // 设置位置和尺寸
        index.setName("sampleAdjustDetail");  // 设置节点名称
        index.setType(ExternalKeysNode.TYPE_PERSIST_ADJUST);  // 类型:余晖调节滑动条
        index.setParentNode(parent);  // 设置父节点
        index.setParentNodes(parents);  // 设置父节点列表
        list.add(index);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Sample-Depth存储深度节点：深度选项列表
     */
    public static List<ExternalKeysNode> getTopSampleDepthNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        int depthNumber = MemDepthFactory.getMemDepth().getMemDepthItemName().size();  // 获取存储深度信息
        for (int i = 0; i < depthNumber; i++) {  // 遍历选项创建节点
            ExternalKeysNode depth = new ExternalKeysNode();  // 创建深度节点
            depth.setParentNode(parent);  // 设置父节点
            depth.setParentNodes(parents);  // 设置父节点列表
            depth.setPlace(list.size(), 149 + 155 * i, 205 + topSlipOffset, 155, 60);  // 设置位置和尺寸
            depth.setName("depth:" + i);  // 设置节点名称
            list.add(depth);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Sample-Segmented分段存储节点：开关/段数/显示模式/起止段/顺序
     */
    private static List<ExternalKeysNode> getTopSampleSegmentedNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        String[] state = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.sampleSegmentedState);  // 从资源文件获取字符串数组
        String[] number = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.sampleSegmentedNumber);  // 从资源文件获取字符串数组
        String[] display = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.sampleSegmentedDisplay);  // 从资源文件获取字符串数组
        String[] order = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.sampleSegmentedOrder);  // 从资源文件获取字符串数组
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
//        for (int i = 0; i < state.length; i++) {
//            ExternalKeysNode node = new ExternalKeysNode();
//            node.setParentNode(parent);
//            node.setParentNodes(parents);
//            node.setPlace(list.size(), 142 + 60 * i, 137, 60, 35);
//            node.setName("state:" + i);
//            if (i == 0) {
//                node.setCurListSelect(4);
//            }
//            list.add(node);
//        }
        {
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 169, 205 + topSlipOffset, 72, 40);  // 设置位置和尺寸
            node.setName("segmented storage");  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < number.length; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 552 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            node.setName("number:" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }
        ExternalKeysNode numberDetail = new ExternalKeysNode();  // 创建数值详情节点
        numberDetail.setParentNode(parent);  // 设置父节点
        numberDetail.setParentNodes(parents);  // 设置父节点列表
        numberDetail.setPlace(list.size(), 1072, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        numberDetail.setChildNodes(getNumberKeyBoardNodeList(numberDetail, list, false));  // 设置子节点列表
        for (int i = 0; i < numberDetail.getChildNodes().size(); i++) {  // 循环创建节点
            numberDetail.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        numberDetail.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        numberDetail.setName("numberDetail");  // 设置节点名称
        list.add(numberDetail);  // 添加节点到列表
        for (int i = 0; i < display.length; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 169 + 120 * i, 275 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            node.setName("display:" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }
        ExternalKeysNode start = new ExternalKeysNode();  // 创建起始节点
        start.setParentNode(parent);  // 设置父节点
        start.setParentNodes(parents);  // 设置父节点列表
        start.setPlace(list.size(), 458, 275 + topSlipOffset, 122, 60);  // 设置位置和尺寸
        start.setChildNodes(getNumberKeyBoardNodeList(start, list, false));  // 设置子节点列表
        for (int i = 0; i < start.getChildNodes().size(); i++) {  // 循环创建节点
            start.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        start.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        start.setName("start");  // 设置节点名称
        list.add(start);  // 添加节点到列表
        ExternalKeysNode end = new ExternalKeysNode();  // 创建结束节点
        end.setParentNode(parent);  // 设置父节点
        end.setParentNodes(parents);  // 设置父节点列表
        end.setPlace(list.size(), 608, 275 + topSlipOffset, 122, 60);  // 设置位置和尺寸
        end.setChildNodes(getNumberKeyBoardNodeList(end, list, false));  // 设置子节点列表
        for (int i = 0; i < end.getChildNodes().size(); i++) {  // 循环创建节点
            end.getChildNodes().get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, i));  // 设置可见性
        }
        end.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        end.setName("end");  // 设置节点名称
        list.add(end);  // 添加节点到列表
        for (int i = 0; i < order.length; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 550 + 120 * i, 275 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            node.setName("order:" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }
    //endregion

    //region TopDisplay
    /**
     * 构建Display菜单的二级子菜单节点：Common/Waveform/Graticule/Persist/FFTInfo/TxtMix
     */
    private static List<ExternalKeysNode> getTopDisplayDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] strings = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.display);  // 从资源文件获取字符串数组

        ExternalKeysNode common = new ExternalKeysNode();  // 创建通用节点
        common.setPlace(list.size(), 0, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        common.setName(strings[0]);  // 设置节点名称
        common.setParentNode(parent);  // 设置父节点
        common.setParentNodes(parents);  // 设置父节点列表
        common.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY));  // 设置当前选中项
        common.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        common.setChildNodes(getTopDisplayCommonDetailNodeList(common, list, topSlipOffset));  // 设置子节点列表
        list.add(common);  // 添加节点到列表

        ExternalKeysNode waveform = new ExternalKeysNode();  // 创建节点节点
        waveform.setPlace(list.size(), 145, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        waveform.setName(strings[1]);  // 设置节点名称
        waveform.setParentNode(parent);  // 设置父节点
        waveform.setParentNodes(parents);  // 设置父节点列表
        waveform.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        waveform.setChildNodes(getTopDisplayWaveformDetailNodeList(waveform, list, topSlipOffset));  // 设置子节点列表
        list.add(waveform);  // 添加节点到列表

        ExternalKeysNode graticule = new ExternalKeysNode();  // 创建网格节点
        graticule.setPlace(list.size(), 290, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        graticule.setName(strings[2]);  // 设置节点名称
        graticule.setParentNode(parent);  // 设置父节点
        graticule.setParentNodes(parents);  // 设置父节点列表
        graticule.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        graticule.setChildNodes(getTopDisplayGraticuleDetailNodeList(graticule, list, topSlipOffset));  // 设置子节点列表
        list.add(graticule);  // 添加节点到列表

        ExternalKeysNode persist = new ExternalKeysNode();  // 创建余晖节点
        persist.setPlace(list.size(), 435, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        persist.setName(strings[3]);  // 设置节点名称
        persist.setParentNode(parent);  // 设置父节点
        persist.setParentNodes(parents);  // 设置父节点列表
        persist.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        persist.setChildNodes(getTopDisplayPersistDetailNodeList(persist, list, topSlipOffset));  // 设置子节点列表
        list.add(persist);  // 添加节点到列表

        ExternalKeysNode fftInfo = new ExternalKeysNode();  // 创建FFT信息节点
        fftInfo.setPlace(list.size(), 580, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        fftInfo.setName(strings[4]);  // 设置节点名称
        fftInfo.setParentNode(parent);  // 设置父节点
        fftInfo.setParentNodes(parents);  // 设置父节点列表
        fftInfo.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        fftInfo.setChildNodes(getTopDisplayFftInfoDetailNodeList(fftInfo, list, topSlipOffset));  // 设置子节点列表
        list.add(fftInfo);  // 添加节点到列表

        ExternalKeysNode txtMix = new ExternalKeysNode();  // 创建文本混显节点
        txtMix.setPlace(list.size(), 725, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        txtMix.setName(strings[5]);  // 设置节点名称
        txtMix.setParentNode(parent);  // 设置父节点
        txtMix.setParentNodes(parents);  // 设置父节点列表
        txtMix.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        txtMix.setChildNodes(getTopDisplayTxtMixDetailNodeList(txtMix, list, topSlipOffset));  // 设置子节点列表
        list.add(txtMix);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Display-Common通用显示节点：水平参考/时基/时间位置/缩放/滚动/CCT/透明度
     */
    private static List<ExternalKeysNode> getTopDisplayCommonDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] horRefs = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.displayHorRef);  // 从资源文件获取字符串数组
        String[] timeBases = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.displayTimebase);  // 从资源文件获取字符串数组
        String[] enable = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.displayEnable);  // 从资源文件获取字符串数组
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode horRef = new ExternalKeysNode();  // 创建水平参考节点
            horRef.setParentNode(parent);  // 设置父节点
            horRef.setParentNodes(parents);  // 设置父节点列表
            horRef.setPlace(list.size(), 179 + i * 120, 231, 120, 60);  // 设置位置和尺寸
            horRef.setName(horRefs[i]);  // 设置节点名称
            list.add(horRef);  // 添加节点到列表
        }
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode timeBase = new ExternalKeysNode();  // 创建时基节点
            timeBase.setParentNode(parent);  // 设置父节点
            timeBase.setParentNodes(parents);  // 设置父节点列表
            timeBase.setPlace(list.size(), 675 + i * 120, 231, 120, 60);  // 设置位置和尺寸
            timeBase.setName(timeBases[i]);  // 设置节点名称
            list.add(timeBase);  // 添加节点到列表
        }

        ExternalKeysNode timePos = new ExternalKeysNode();  // 创建时间位置节点
        timePos.setParentNode(parent);  // 设置父节点
        timePos.setParentNodes(parents);  // 设置父节点列表
        timePos.setPlace(list.size(), 1171, 231, 120, 60);  // 设置位置和尺寸
        timePos.setName("timePos");  // 设置节点名称
        timePos.setChildNodes(getFloatKeyBoardNodeList(timePos, list));  // 设置子节点列表
        timePos.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(timePos);  // 添加节点到列表

        ExternalKeysNode scale = new ExternalKeysNode();  // 创建缩放节点
        scale.setParentNode(parent);  // 设置父节点
        scale.setParentNodes(parents);  // 设置父节点列表
        scale.setPlace(list.size(), 179, 331, 72, 36);  // 设置位置和尺寸
        scale.setName("scale");  // 设置节点名称
        list.add(scale);  // 添加节点到列表

        ExternalKeysNode roll = new ExternalKeysNode();  // 创建滚动节点
        roll.setParentNode(parent);  // 设置父节点
        roll.setParentNodes(parents);  // 设置父节点列表
        roll.setPlace(list.size(), 675, 331, 72, 36);  // 设置位置和尺寸
        roll.setName("roll");  // 设置节点名称
        list.add(roll);  // 添加节点到列表

        ExternalKeysNode cct = new ExternalKeysNode();  // 创建CCT节点
        cct.setParentNode(parent);  // 设置父节点
        cct.setParentNodes(parents);  // 设置父节点列表
        cct.setPlace(list.size(), 1171, 331, 72, 36);  // 设置位置和尺寸
        cct.setName("cct");  // 设置节点名称
        list.add(cct);  // 添加节点到列表


        ExternalKeysNode brightness = new ExternalKeysNode();  // 创建亮度节点
        brightness.setPlace(list.size(), 40, 270 + topSlipOffset, 700, 60);  // 设置位置和尺寸
        brightness.setName("CommonAlpha");  // 设置节点名称
        brightness.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        brightness.setChildNodes(getTopDisplayCommonAlphaDetailNodeList(brightness, list, topSlipOffset));  // 设置子节点列表
        brightness.setParentNode(parent);  // 设置父节点
        brightness.setParentNodes(parents);  // 设置父节点列表
        brightness.setVisible(false);  // 设为不可见
        list.add(brightness);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Display-Waveform波形显示节点：绘制类型/背景/亮度进度条
     */
    private static List<ExternalKeysNode> getTopDisplayWaveformDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] types = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.displayDrawType);  // 从资源文件获取字符串数组
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode type = new ExternalKeysNode();  // 创建节点节点
            type.setPlace(list.size(), 149 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            type.setName(types[i]);  // 设置节点名称
            type.setParentNode(parent);  // 设置父节点
            type.setParentNodes(parents);  // 设置父节点列表
            list.add(type);  // 添加节点到列表
        }

        types = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.displayBackground);  // 从资源文件获取字符串数组
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode type = new ExternalKeysNode();  // 创建节点节点
            type.setPlace(list.size(), 625 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            type.setName(types[i]);  // 设置节点名称
            type.setParentNode(parent);  // 设置父节点
            type.setParentNodes(parents);  // 设置父节点列表
            list.add(type);  // 添加节点到列表
        }

        ExternalKeysNode brightness = new ExternalKeysNode();  // 创建亮度节点
        brightness.setPlace(list.size(), 960, 195 + topSlipOffset, 680, 60);  // 设置位置和尺寸
        brightness.setName("brightnessProgress");  // 设置节点名称
        brightness.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        brightness.setChildNodes(getTopDisplayWaveformBrightnessDetailNodeList(brightness, list, topSlipOffset));  // 设置子节点列表
        brightness.setParentNode(parent);  // 设置父节点
        brightness.setParentNodes(parents);  // 设置父节点列表
        list.add(brightness);  // 添加节点到列表

//        ExternalKeysNode brightnessAdd = new ExternalKeysNode();
//        brightnessAdd.setPlace(list.size(), 584, 139, 50, 30);
//        brightnessAdd.setName("brightnessAdd");
//        brightnessAdd.setParentNode(parent);
//        brightnessAdd.setParentNodes(parents);
//        list.add(brightnessAdd);

        return list;  // 返回节点列表


    }

    /**
     * 构建触发灵敏度进度条节点
     */
    private static List<ExternalKeysNode> getTopTriggeCommonTriggerSensitivityDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode number = new ExternalKeysNode();  // 创建数字键节点
        number.setPlace(list.size(), 1105, 195 + topSlipOffset, 480, 60);  // 设置位置和尺寸
        number.setName("progress1");  // 设置节点名称
        number.setType(ExternalKeysNode.TYPE_TRIGGER_SENSITIVITY_PROGRESS);  // 类型:触发灵敏度进度条
        number.setParentNode(parent);  // 设置父节点
        number.setParentNodes(parents);  // 设置父节点列表
        list.add(number);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建波形亮度进度条节点
     */
    private static List<ExternalKeysNode> getTopDisplayWaveformBrightnessDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode number = new ExternalKeysNode();  // 创建数字键节点
        number.setPlace(list.size(), 1095, 195 + topSlipOffset, 480, 60);  // 设置位置和尺寸
        number.setName("progress");  // 设置节点名称
        number.setType(ExternalKeysNode.TYPE_BRIGHTNESS_PROGRESS);  // 类型:亮度进度条
        number.setParentNode(parent);  // 设置父节点
        number.setParentNodes(parents);  // 设置父节点列表
        list.add(number);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建通用透明度进度条节点
     */
    private static List<ExternalKeysNode> getTopDisplayCommonAlphaDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode number = new ExternalKeysNode();  // 创建数字键节点
        number.setPlace(list.size(), 185, 270 + topSlipOffset, 480, 60);  // 设置位置和尺寸
        number.setName("progress");  // 设置节点名称
        number.setType(ExternalKeysNode.TYPE_ALPHA_PROGRESS);  // 类型:透明度进度条
        number.setParentNode(parent);  // 设置父节点
        number.setParentNodes(parents);  // 设置父节点列表
        list.add(number);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Display-Graticule网格显示节点：网格类型/网格线亮度
     */
    private static List<ExternalKeysNode> getTopDisplayGraticuleDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        for (int i = 0; i < 4; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setPlace(list.size(), 149 + i * 120, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            node.setName("graticule" + i);  // 设置节点名称
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            list.add(node);  // 添加节点到列表
        }

        ExternalKeysNode intensity = new ExternalKeysNode();  // 创建网格线亮度节点
        intensity.setPlace(list.size(), 794, 195 + topSlipOffset, 660, 60);  // 设置位置和尺寸
        intensity.setName("intensity");  // 设置节点名称
        intensity.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        intensity.setChildNodes(getTopDisplayGraticuleIntensityDetailNodeList(intensity, list, topSlipOffset));  // 设置子节点列表
        intensity.setParentNode(parent);  // 设置父节点
        intensity.setParentNodes(parents);  // 设置父节点列表
        list.add(intensity);  // 添加节点到列表

//        ExternalKeysNode intensityAdd = new ExternalKeysNode();
//        intensityAdd.setPlace(list.size(), 629, 138, 50, 30);
//        intensityAdd.setName("intensityAdd");
//        intensityAdd.setParentNode(parent);
//        intensityAdd.setParentNodes(parents);
//        list.add(intensityAdd);

        return list;  // 返回节点列表
    }

    /**
     * 构建网格线亮度进度条节点
     */
    private static List<ExternalKeysNode> getTopDisplayGraticuleIntensityDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode number = new ExternalKeysNode();  // 创建数字键节点
        number.setPlace(list.size(), 910, 195 + topSlipOffset, 480, 60);  // 设置位置和尺寸
        number.setName("progress");  // 设置节点名称
        number.setType(ExternalKeysNode.TYPE_INTENSITY_PROGRESS);  // 类型:网格线亮度进度条
        number.setParentNode(parent);  // 设置父节点
        number.setParentNodes(parents);  // 设置父节点列表
        list.add(number);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Display-Persist余晖显示节点：余晖类型/清除/调节/FFT余晖
     */
    private static List<ExternalKeysNode> getTopDisplayPersistDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] types = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.displayPersist);  // 从资源文件获取字符串数组
        for (int i = 0; i < types.length; i++) {  // 遍历选项创建节点
            ExternalKeysNode type = new ExternalKeysNode();  // 创建节点节点
            type.setPlace(list.size(), 149 + i * 120, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            type.setName(types[i]);  // 设置节点名称
            type.setParentNode(parent);  // 设置父节点
            type.setParentNodes(parents);  // 设置父节点列表
            list.add(type);  // 添加节点到列表
        }
        ExternalKeysNode clear = new ExternalKeysNode();  // 创建清除节点
        clear.setPlace(list.size(), 689, 195 + topSlipOffset, 154, 60);  // 设置位置和尺寸
        clear.setName("clear");  // 设置节点名称
        clear.setParentNode(parent);  // 设置父节点
        clear.setParentNodes(parents);  // 设置父节点列表
        list.add(clear);  // 添加节点到列表
        ExternalKeysNode selectHead = new ExternalKeysNode();  // 创建调节选择节点
        selectHead.setPlace(list.size(), 952, 195 + topSlipOffset, 150, 60);  // 设置位置和尺寸
        selectHead.setName("adjust");  // 设置节点名称
        selectHead.setChildNodes(getTopDisplayPersistSelectDetailNodeList(selectHead, list, topSlipOffset));  // 设置子节点列表
        selectHead.setParentNode(parent);  // 设置父节点
        selectHead.setParentNodes(parents);  // 设置父节点列表
        list.add(selectHead);  // 添加节点到列表

        for (int i = 0; i < types.length; i++) {  // 遍历选项创建节点
            ExternalKeysNode type = new ExternalKeysNode();  // 创建节点节点
            type.setPlace(list.size(), 149 + i * 120, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            type.setName("fft" + types[i]);  // 设置节点名称
            type.setParentNode(parent);  // 设置父节点
            type.setParentNodes(parents);  // 设置父节点列表
            list.add(type);  // 添加节点到列表
        }
        ExternalKeysNode fftClear = new ExternalKeysNode();  // 创建FFT清除节点
        fftClear.setPlace(list.size(), 689, 265 + topSlipOffset, 154, 60);  // 设置位置和尺寸
        fftClear.setName("fftClear");  // 设置节点名称
        fftClear.setParentNode(parent);  // 设置父节点
        fftClear.setParentNodes(parents);  // 设置父节点列表
        list.add(fftClear);  // 添加节点到列表
        ExternalKeysNode fftSelectHead = new ExternalKeysNode();  // 创建FFT调节节点
        fftSelectHead.setPlace(list.size(), 952, 265 + topSlipOffset, 150, 60);  // 设置位置和尺寸
        fftSelectHead.setName("fftAdjust");  // 设置节点名称
        fftSelectHead.setChildNodes(getTopDisplayPersistSelectDetailNodeList(fftSelectHead, list, topSlipOffset));  // 设置子节点列表
        fftSelectHead.setParentNode(parent);  // 设置父节点
        fftSelectHead.setParentNodes(parents);  // 设置父节点列表
        list.add(fftSelectHead);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Display-FFTInfo节点：FFT信息通道列表
     */
    private static List<ExternalKeysNode> getTopDisplayFftInfoDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] fftInfoPre = App.get().getResources().getStringArray(R.array.frequencymeter);  // 从资源文件获取字符串数组
        String[] maths = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.topLayoutDisplayFftInfo);  // 从资源文件获取字符串数组
        String[] mathInfo = StrUtil.add(fftInfoPre, maths);  // 合并两个字符串数组

//        ExternalKeysNode disPlay = new ExternalKeysNode();
//        disPlay.setPlace(list.size(), 161, 195 + topSlipOffset, 72, 60);
//        disPlay.setName("showDisplay");
//        disPlay.setParentNode(parent);
//        disPlay.setParentNodes(parents);
//        list.add(disPlay);

        for (int i = 0; i < mathInfo.length; i++) {  // 循环创建节点
            ExternalKeysNode type = new ExternalKeysNode();  // 创建节点节点
            type.setPlace(list.size(), 149 + i * 120, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            type.setName(mathInfo[i]);  // 设置节点名称
            type.setParentNode(parent);  // 设置父节点
            type.setParentNodes(parents);  // 设置父节点列表
            list.add(type);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Display-TxtMix文本混显节点：串行总线S1~Sn
     */
    public static List<ExternalKeysNode> getTopDisplayTxtMixDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 1; i <= ChannelFactory.SERIAL_CNT; i++) {  // 遍历通道创建节点
            ExternalKeysNode type = new ExternalKeysNode();  // 创建节点节点
            type.setPlace(list.size(), 161 + (i - 1) * 200, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            type.setName("S" + i);  // 设置节点名称
            type.setParentNode(parent);  // 设置父节点
            type.setParentNodes(parents);  // 设置父节点列表
            list.add(type);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建余晖调节滑动条节点
     */
    private static List<ExternalKeysNode> getTopDisplayPersistSelectDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode index = new ExternalKeysNode();  // 创建索引节点节点
        index.setPlace(list.size(), 0, 340 + topSlipOffset, 1800, 62);  // 设置位置和尺寸
        index.setName("adjustDetail");  // 设置节点名称
        index.setType(ExternalKeysNode.TYPE_PERSIST_ADJUST);  // 类型:余晖调节滑动条
        index.setParentNode(parent);  // 设置父节点
        index.setParentNodes(parents);  // 设置父节点列表
        list.add(index);  // 添加节点到列表
        return list;  // 返回节点列表
    }
    //endregion

    //region TopTrigger
    /**
     * 构建Trigger菜单的二级子菜单节点：Common/Edge/Pulse/Logic/NEdge/Runt/Slope/Timeout/Video/Serials1~4
     */
    private static List<ExternalKeysNode> getTopTriggerDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] strings = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.trigger);  // 从资源文件获取字符串数组

        ExternalKeysNode common = new ExternalKeysNode();  // 创建通用节点
        common.setParentNode(parent);  // 设置父节点
        common.setParentNodes(parents);  // 设置父节点列表
        common.setPlace(list.size(), 10, 110 + topSlipOffset, 135, 70);  // 设置位置和尺寸
        common.setName(strings[0]);  // 设置节点名称
        common.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER));  // 设置当前选中项
        common.setType(ExternalKeysNode.TYPE_TRIGGER_TITLE);  // 类型:触发标题
        common.setChildNodes(getTopTriggerCommonDetailNodeList(common, list, topSlipOffset));  // 设置子节点列表
        list.add(common);  // 添加节点到列表

        ExternalKeysNode edge = new ExternalKeysNode();  // 创建边沿节点
        edge.setParentNode(parent);  // 设置父节点
        edge.setParentNodes(parents);  // 设置父节点列表
        edge.setPlace(list.size(), 145, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        edge.setName(strings[1]);  // 设置节点名称
        edge.setType(ExternalKeysNode.TYPE_TRIGGER_TITLE);  // 类型:触发标题
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {  // 判断是否为8通道型号
            edge.setChildNodes(getTopTriggerEdgeDetailEightNodeList(edge, list, topSlipOffset));  // 设置子节点列表
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) {  // 获取通道数量
            edge.setChildNodes(getTopTriggerEdgeDetailNodeList(edge, list, topSlipOffset));  // 设置子节点列表
        }
        list.add(edge);  // 添加节点到列表

        ExternalKeysNode pulse = new ExternalKeysNode();  // 创建脉宽节点
        pulse.setParentNode(parent);  // 设置父节点
        pulse.setParentNodes(parents);  // 设置父节点列表
        pulse.setPlace(list.size(), 290, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        pulse.setName(strings[2]);  // 设置节点名称
        pulse.setType(ExternalKeysNode.TYPE_TRIGGER_TITLE);  // 类型:触发标题
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {  // 判断是否为8通道型号
            pulse.setChildNodes(getTopTriggerPulseDetailEightNodeList(pulse, list, topSlipOffset));  // 设置子节点列表
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) {  // 获取通道数量
            pulse.setChildNodes(getTopTriggerPulseDetailNodeList(pulse, list, topSlipOffset));  // 设置子节点列表
        }
        list.add(pulse);  // 添加节点到列表

        ExternalKeysNode logic = new ExternalKeysNode();  // 创建逻辑节点
        logic.setParentNode(parent);  // 设置父节点
        logic.setParentNodes(parents);  // 设置父节点列表
        logic.setPlace(list.size(), 435, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        logic.setName(strings[3]);  // 设置节点名称
        logic.setType(ExternalKeysNode.TYPE_TRIGGER_TITLE);  // 类型:触发标题
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {  // 判断是否为8通道型号
            logic.setChildNodes(getTopTriggerLogicDetailEightChannelNodeList(logic, list, topSlipOffset));  // 设置子节点列表
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) {  // 获取通道数量
            logic.setChildNodes(getTopTriggerLogicDetailFourChannelNodeList(logic, list, topSlipOffset));  // 设置子节点列表
        } else {  // 否则
            logic.setChildNodes(getTopTriggerLogicDetailDoubleChannelNodeList(logic, list, topSlipOffset));  // 设置子节点列表
        }
        list.add(logic);  // 添加节点到列表

        ExternalKeysNode nEdge = new ExternalKeysNode();  // 创建N边沿节点
        nEdge.setParentNode(parent);  // 设置父节点
        nEdge.setParentNodes(parents);  // 设置父节点列表
        nEdge.setPlace(list.size(), 580, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        nEdge.setName(strings[4]);  // 设置节点名称
        nEdge.setType(ExternalKeysNode.TYPE_TRIGGER_TITLE);  // 类型:触发标题
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {  // 判断是否为8通道型号
            nEdge.setChildNodes(getTopTriggerNEdgeDetailEightNodeList(nEdge, list, topSlipOffset));  // 设置子节点列表
        } else {  // 否则
            nEdge.setChildNodes(getTopTriggerNEdgeDetailNodeList(nEdge, list, topSlipOffset));  // 设置子节点列表
        }
        list.add(nEdge);  // 添加节点到列表

        ExternalKeysNode runt = new ExternalKeysNode();  // 创建矮脉冲节点
        runt.setParentNode(parent);  // 设置父节点
        runt.setParentNodes(parents);  // 设置父节点列表
        runt.setPlace(list.size(), 725, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        runt.setName(strings[5]);  // 设置节点名称
        runt.setType(ExternalKeysNode.TYPE_TRIGGER_TITLE);  // 类型:触发标题
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {  // 判断是否为8通道型号
            runt.setChildNodes(getTopTriggerRuntDetailEightNodeList(runt, list, topSlipOffset));  // 设置子节点列表
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) {  // 获取通道数量
            runt.setChildNodes(getTopTriggerRuntDetailNodeList(runt, list, topSlipOffset));  // 设置子节点列表
        }
        list.add(runt);  // 添加节点到列表

        ExternalKeysNode slope = new ExternalKeysNode();  // 创建斜率节点
        slope.setParentNode(parent);  // 设置父节点
        slope.setParentNodes(parents);  // 设置父节点列表
        slope.setPlace(list.size(), 870, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        slope.setName(strings[6]);  // 设置节点名称
        slope.setType(ExternalKeysNode.TYPE_TRIGGER_TITLE);  // 类型:触发标题
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {  // 判断是否为8通道型号
            slope.setChildNodes(getTopTriggerSlopeDetailEightNodeList(slope, list, topSlipOffset));  // 设置子节点列表
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) {  // 获取通道数量
            slope.setChildNodes(getTopTriggerSlopeDetailNodeList(slope, list, topSlipOffset));  // 设置子节点列表
        }
        list.add(slope);  // 添加节点到列表

        ExternalKeysNode timeout = new ExternalKeysNode();  // 创建超时节点
        timeout.setParentNode(parent);  // 设置父节点
        timeout.setParentNodes(parents);  // 设置父节点列表
        timeout.setPlace(list.size(), 1015, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        timeout.setName(strings[7]);  // 设置节点名称
        timeout.setType(ExternalKeysNode.TYPE_TRIGGER_TITLE);  // 类型:触发标题
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {  // 判断是否为8通道型号
            timeout.setChildNodes(getTopTriggerTimeoutDetailEightNodeList(timeout, list, topSlipOffset));  // 设置子节点列表
        } else {  // 否则
            timeout.setChildNodes(getTopTriggerTimeoutDetailNodeList(timeout, list, topSlipOffset));  // 设置子节点列表
        }
        list.add(timeout);  // 添加节点到列表

        ExternalKeysNode video = new ExternalKeysNode();  // 创建视频节点
        video.setParentNode(parent);  // 设置父节点
        video.setParentNodes(parents);  // 设置父节点列表
        video.setPlace(list.size(), 1160, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        video.setName(strings[8]);  // 设置节点名称
        video.setType(ExternalKeysNode.TYPE_TRIGGER_TITLE);  // 类型:触发标题
        video.setChildNodes(getTopTriggerVideoDetailNodeList(video, list, topSlipOffset));  // 设置子节点列表
        list.add(video);  // 添加节点到列表

        ExternalKeysNode serials1 = new ExternalKeysNode();  // 创建串行1节点
        serials1.setParentNode(parent);  // 设置父节点
        serials1.setParentNodes(parents);  // 设置父节点列表
        serials1.setPlace(list.size(), 1305, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        serials1.setName(strings[9]);  // 设置节点名称
        serials1.setType(ExternalKeysNode.TYPE_TRIGGER_TITLE);  // 类型:触发标题
        serials1.setChildNodes(getTopTriggerSerialsDetailNodeList(serials1, list, topSlipOffset));  // 设置子节点列表
        list.add(serials1);  // 添加节点到列表

        ExternalKeysNode serials2 = new ExternalKeysNode();  // 创建串行2节点
        serials2.setParentNode(parent);  // 设置父节点
        serials2.setParentNodes(parents);  // 设置父节点列表
        serials2.setPlace(list.size(), 1450, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        serials2.setName(strings[10]);  // 设置节点名称
        serials2.setType(ExternalKeysNode.TYPE_TRIGGER_TITLE);  // 类型:触发标题
        serials2.setChildNodes(getTopTriggerSerialsDetailNodeList(serials2, list, topSlipOffset));  // 设置子节点列表
        list.add(serials2);  // 添加节点到列表

        ExternalKeysNode serials3 = new ExternalKeysNode();  // 创建串行3节点
        serials3.setParentNode(parent);  // 设置父节点
        serials3.setParentNodes(parents);  // 设置父节点列表
        serials3.setPlace(list.size(), 1595, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        serials3.setName(strings[11]);  // 设置节点名称
        serials3.setType(ExternalKeysNode.TYPE_TRIGGER_TITLE);  // 类型:触发标题
        serials3.setChildNodes(getTopTriggerSerialsDetailNodeList(serials3, list, topSlipOffset));  // 设置子节点列表
        list.add(serials3);  // 添加节点到列表

        ExternalKeysNode serials4 = new ExternalKeysNode();  // 创建串行4节点
        serials4.setParentNode(parent);  // 设置父节点
        serials4.setParentNodes(parents);  // 设置父节点列表
        serials4.setPlace(list.size(), 1740, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        serials4.setName(strings[12]);  // 设置节点名称
        serials4.setType(ExternalKeysNode.TYPE_TRIGGER_TITLE);  // 类型:触发标题
        serials4.setChildNodes(getTopTriggerSerialsDetailNodeList(serials4, list, topSlipOffset));  // 设置子节点列表
        list.add(serials4);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Common通用触发节点：释抑时间/触发模式/触发灵敏度
     */
    private static List<ExternalKeysNode> getTopTriggerCommonDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] strings = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerMode);  // 从资源文件获取字符串数组
        ExternalKeysNode holdOffTime = new ExternalKeysNode();  // 创建释抑时间节点
        holdOffTime.setParentNode(parent);  // 设置父节点
        holdOffTime.setParentNodes(parents);  // 设置父节点列表
        holdOffTime.setPlace(list.size(), 146, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        holdOffTime.setName("holdOffTime");  // 设置节点名称
        list.add(holdOffTime);  // 添加节点到列表
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode model = new ExternalKeysNode();  // 创建节点节点
            model.setParentNode(parent);  // 设置父节点
            model.setParentNodes(parents);  // 设置父节点列表
            model.setPlace(list.size(), 536 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            model.setName(strings[i]);  // 设置节点名称
            list.add(model);  // 添加节点到列表
        }
        ExternalKeysNode triggerSensitivity = new ExternalKeysNode();  // 创建触发灵敏度节点
        triggerSensitivity.setPlace(list.size(), 920, 195 + topSlipOffset, 680, 60);  // 设置位置和尺寸
        triggerSensitivity.setName("TriggerSensitivityProgress");  // 设置节点名称
        triggerSensitivity.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        triggerSensitivity.setChildNodes(getTopTriggeCommonTriggerSensitivityDetailNodeList(triggerSensitivity, list, topSlipOffset));  // 设置子节点列表
        triggerSensitivity.setParentNode(parent);  // 设置父节点
        triggerSensitivity.setParentNodes(parents);  // 设置父节点列表
        list.add(triggerSensitivity);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Edge边沿触发节点(4通道)：源/边沿/耦合
     */
    private static List<ExternalKeysNode> getTopTriggerEdgeDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] sources = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] edges = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerEdge);  // 从资源文件获取字符串数组
        String[] couples = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerCouple);  // 从资源文件获取字符串数组
        for (int i = 0; i < sources.length; i++) {  // 循环创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 161 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            source.setName(sources[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < edges.length; i++) {  // 循环创建节点
            ExternalKeysNode edge = new ExternalKeysNode();  // 创建边沿节点
            edge.setParentNode(parent);  // 设置父节点
            edge.setParentNodes(parents);  // 设置父节点列表
            edge.setPlace(list.size(), 1269 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            edge.setName(edges[i]);  // 设置节点名称
            list.add(edge);  // 添加节点到列表
        }
        for (int i = 0; i < couples.length; i++) {  // 循环创建节点
            ExternalKeysNode couple = new ExternalKeysNode();  // 创建节点节点
            couple.setParentNode(parent);  // 设置父节点
            couple.setParentNodes(parents);  // 设置父节点列表
            couple.setPlace(list.size(), 161 + 120 * i, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            couple.setName(couples[i]);  // 设置节点名称
            list.add(couple);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Edge边沿触发节点(8通道)：源(含外触发)/边沿/耦合
     */
    private static List<ExternalKeysNode> getTopTriggerEdgeDetailEightNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] sources = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] extTrigger = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.edgeExternalTrigger);  // 从资源文件获取字符串数组
        String[] finalSources = StrUtil.add(sources, extTrigger);  // 合并两个字符串数组
        String[] edges = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerEdge);  // 从资源文件获取字符串数组
        String[] couples = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerCouple);  // 从资源文件获取字符串数组
        for (int i = 0; i < finalSources.length; i++) {  // 循环创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 161 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            source.setName(finalSources[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < edges.length; i++) {  // 循环创建节点
            ExternalKeysNode edge = new ExternalKeysNode();  // 创建边沿节点
            edge.setParentNode(parent);  // 设置父节点
            edge.setParentNodes(parents);  // 设置父节点列表
            edge.setPlace(list.size(), 161 + 120 * i, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            edge.setName(edges[i]);  // 设置节点名称
            list.add(edge);  // 添加节点到列表
        }
        for (int i = 0; i < couples.length; i++) {  // 循环创建节点
            ExternalKeysNode couple = new ExternalKeysNode();  // 创建节点节点
            couple.setParentNode(parent);  // 设置父节点
            couple.setParentNodes(parents);  // 设置父节点列表
            couple.setPlace(list.size(), 761 + 120 * i, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            couple.setName(couples[i]);  // 设置节点名称
            list.add(couple);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Pulse脉宽触发节点(4通道)：源/极性/条件/详细
     */
    private static List<ExternalKeysNode> getTopTriggerPulseDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] polars = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerPulsewidthPolar);  // 从资源文件获取字符串数组
        String[] conditions = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerPulsewidthCondition);  // 从资源文件获取字符串数组
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 161 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < polars.length; i++) {  // 循环创建节点
            ExternalKeysNode polar = new ExternalKeysNode();  // 创建极性节点
            polar.setParentNode(parent);  // 设置父节点
            polar.setParentNodes(parents);  // 设置父节点列表
            polar.setPlace(list.size(), 1289 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            polar.setName(polars[i]);  // 设置节点名称
            list.add(polar);  // 添加节点到列表
        }
        for (int i = 0; i < conditions.length; i++) {  // 循环创建节点
            ExternalKeysNode condition = new ExternalKeysNode();  // 创建条件节点
            condition.setParentNode(parent);  // 设置父节点
            condition.setParentNodes(parents);  // 设置父节点列表
            condition.setPlace(list.size(), 161 + 120 * i, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            condition.setName(conditions[i]);  // 设置节点名称
            list.add(condition);  // 添加节点到列表
        }
        ExternalKeysNode detail = new ExternalKeysNode();  // 创建详细设置节点
        detail.setParentNode(parent);  // 设置父节点
        detail.setParentNodes(parents);  // 设置父节点列表
        detail.setPlace(list.size(), 741, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        detail.setName("detail");  // 设置节点名称
        list.add(detail);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Pulse脉宽触发节点(8通道)：源/极性/条件/详细/时间范围
     */
    private static List<ExternalKeysNode> getTopTriggerPulseDetailEightNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] polars = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerPulsewidthPolar);  // 从资源文件获取字符串数组
        String[] conditions = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerPulsewidthCondition);  // 从资源文件获取字符串数组
        int conditionSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_PULSEWIDTH_CONDITION);  // 从缓存读取整数值

        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 161 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < polars.length; i++) {  // 循环创建节点
            ExternalKeysNode polar = new ExternalKeysNode();  // 创建极性节点
            polar.setParentNode(parent);  // 设置父节点
            polar.setParentNodes(parents);  // 设置父节点列表
            polar.setPlace(list.size(), 161 + 120 * i, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            polar.setName(polars[i]);  // 设置节点名称
            list.add(polar);  // 添加节点到列表
        }
        for (int i = 0; i < conditions.length; i++) {  // 循环创建节点
            ExternalKeysNode condition = new ExternalKeysNode();  // 创建条件节点
            condition.setParentNode(parent);  // 设置父节点
            condition.setParentNodes(parents);  // 设置父节点列表
            condition.setPlace(list.size(), 161 + 120 * i, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            condition.setName(conditions[i]);  // 设置节点名称
            list.add(condition);  // 添加节点到列表
        }
        ExternalKeysNode detail = new ExternalKeysNode();  // 创建详细设置节点
        detail.setParentNode(parent);  // 设置父节点
        detail.setParentNodes(parents);  // 设置父节点列表
        detail.setPlace(list.size(), 681, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        detail.setName("detail");  // 设置节点名称
        detail.setVisible(conditionSelect == 2);  // 设置可见性
        list.add(detail);  // 添加节点到列表

        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode time = new ExternalKeysNode();  // 创建时间节点
            time.setParentNode(parent);  // 设置父节点
            time.setParentNodes(parents);  // 设置父节点列表
            time.setPlace(list.size(), 950 + 269 * i, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            time.setName("time:" + (i == 0 ? "maxTime" : "minTime"));  // 设置节点名称
            if (i == 0) {  // 第一项特殊处理
                time.setVisible(conditionSelect == 0 || conditionSelect == 3);  // 设置可见性
            } else {  // 否则
                time.setVisible(conditionSelect == 1 || conditionSelect == 3);  // 设置可见性
            }
            list.add(time);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Logic逻辑触发节点(8通道)：各通道逻辑选择/逻辑运算/条件/详细/时间
     */
    private static List<ExternalKeysNode> getTopTriggerLogicDetailEightChannelNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] triggerChs = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerCh);  // 从资源文件获取字符串数组
        String[] logics = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerLogic);  // 从资源文件获取字符串数组
        String[] conditions = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerLogicCondition);  // 从资源文件获取字符串数组
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        int conditionSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_LOGIC_CONDITION);  // 从缓存读取整数值

        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            for (int j = 0; j < triggerChs.length; j++) {  // 循环创建节点
                ExternalKeysNode ch = new ExternalKeysNode();  // 创建通道逻辑节点
                ch.setParentNode(parent);  // 设置父节点
                ch.setParentNodes(parents);  // 设置父节点列表
                int x = i % 2 == 0 ? 161 + 120 * j : 897 + 120 * j;  // 定义整型变量
                int y = i / 2 * 70 + 195 + topSlipOffset;  // 定义整型变量
                ch.setPlace(list.size(), x, y, 120, 60);  // 设置位置和尺寸
                ch.setName(channels[j]);  // 设置节点名称
                list.add(ch);  // 添加节点到列表
            }
        }

        for (int i = 0; i < 4; i++) {  // 循环创建节点
            ExternalKeysNode logic = new ExternalKeysNode();  // 创建逻辑节点
            logic.setParentNode(parent);  // 设置父节点
            logic.setParentNodes(parents);  // 设置父节点列表
            logic.setPlace(list.size(), 161 + 120 * i, 475 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            logic.setName(logics[i]);  // 设置节点名称
            list.add(logic);  // 添加节点到列表
        }
        for (int i = 0; i < 6; i++) {  // 循环创建节点
            ExternalKeysNode condition = new ExternalKeysNode();  // 创建条件节点
            condition.setParentNode(parent);  // 设置父节点
            condition.setParentNodes(parents);  // 设置父节点列表
            condition.setPlace(list.size(), 161 + 120 * i, 545 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            condition.setName(conditions[i]);  // 设置节点名称
            list.add(condition);  // 添加节点到列表
        }

        ExternalKeysNode detail = new ExternalKeysNode();  // 创建详细设置节点
        detail.setParentNode(parent);  // 设置父节点
        detail.setParentNodes(parents);  // 设置父节点列表
        detail.setPlace(list.size(), 921, 545 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        detail.setName("detail");  // 设置节点名称
        detail.setVisible(conditionSelect == 2);  // 设置可见性
        list.add(detail);  // 添加节点到列表

        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode time = new ExternalKeysNode();  // 创建时间节点
            time.setParentNode(parent);  // 设置父节点
            time.setParentNodes(parents);  // 设置父节点列表
            time.setPlace(list.size(), 1190 + 269 * i, 545 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            time.setName("time:" + (i == 0 ? "maxTime" : "minTime"));  // 设置节点名称
            if (i == 0) {  // 第一项特殊处理
                time.setVisible(conditionSelect == 0 || conditionSelect == 3);  // 设置可见性
            } else {  // 否则
                time.setVisible(conditionSelect == 1 || conditionSelect == 3);  // 设置可见性
            }
            list.add(time);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Logic逻辑触发节点(4通道)：4通道逻辑选择/逻辑运算/条件/详细
     */
    private static List<ExternalKeysNode> getTopTriggerLogicDetailFourChannelNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerCh);  // 从资源文件获取字符串数组
        String[] logics = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerLogic);  // 从资源文件获取字符串数组
        String[] conditions = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerLogicCondition);  // 从资源文件获取字符串数组
        for (int i = 0; i < 3; i++) {  // 循环创建节点
            ExternalKeysNode ch1 = new ExternalKeysNode();  // 创建通道1逻辑节点
            ch1.setParentNode(parent);  // 设置父节点
            ch1.setParentNodes(parents);  // 设置父节点列表
            ch1.setPlace(list.size(), 161 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            ch1.setName(channels[i]);  // 设置节点名称
            list.add(ch1);  // 添加节点到列表
        }
        for (int i = 0; i < 3; i++) {  // 循环创建节点
            ExternalKeysNode ch2 = new ExternalKeysNode();  // 创建通道2逻辑节点
            ch2.setParentNode(parent);  // 设置父节点
            ch2.setParentNodes(parents);  // 设置父节点列表
            ch2.setPlace(list.size(), 837 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            ch2.setName(channels[i]);  // 设置节点名称
            list.add(ch2);  // 添加节点到列表
        }
        for (int i = 0; i < 3; i++) {  // 循环创建节点
            ExternalKeysNode ch3 = new ExternalKeysNode();  // 创建通道3逻辑节点
            ch3.setParentNode(parent);  // 设置父节点
            ch3.setParentNodes(parents);  // 设置父节点列表
            ch3.setPlace(list.size(), 161 + 120 * i, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            ch3.setName(channels[i]);  // 设置节点名称
            list.add(ch3);  // 添加节点到列表
        }
        for (int i = 0; i < 3; i++) {  // 循环创建节点
            ExternalKeysNode ch4 = new ExternalKeysNode();  // 创建通道4逻辑节点
            ch4.setParentNode(parent);  // 设置父节点
            ch4.setParentNodes(parents);  // 设置父节点列表
            ch4.setPlace(list.size(), 837 + 120 * i, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            ch4.setName(channels[i]);  // 设置节点名称
            list.add(ch4);  // 添加节点到列表
        }
        for (int i = 0; i < 4; i++) {  // 循环创建节点
            ExternalKeysNode logic = new ExternalKeysNode();  // 创建逻辑节点
            logic.setParentNode(parent);  // 设置父节点
            logic.setParentNodes(parents);  // 设置父节点列表
            logic.setPlace(list.size(), 161 + 120 * i, 371 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            logic.setName(logics[i]);  // 设置节点名称
            list.add(logic);  // 添加节点到列表
        }
        for (int i = 0; i < 6; i++) {  // 循环创建节点
            ExternalKeysNode condition = new ExternalKeysNode();  // 创建条件节点
            condition.setParentNode(parent);  // 设置父节点
            condition.setParentNodes(parents);  // 设置父节点列表
            condition.setPlace(list.size(), 837 + 120 * i, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            condition.setName(conditions[i]);  // 设置节点名称
            list.add(condition);  // 添加节点到列表
        }
        ExternalKeysNode detail = new ExternalKeysNode();  // 创建详细设置节点
        detail.setParentNode(parent);  // 设置父节点
        detail.setParentNodes(parents);  // 设置父节点列表
        detail.setPlace(list.size(), 1657, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        detail.setName("detail");  // 设置节点名称
        list.add(detail);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Logic逻辑触发节点(双通道)：通道/逻辑/条件/详细
     */
    private static List<ExternalKeysNode> getTopTriggerLogicDetailDoubleChannelNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerCh);  // 从资源文件获取字符串数组
        String[] logics = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerLogic);  // 从资源文件获取字符串数组
        String[] conditions = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerLogicCondition);  // 从资源文件获取字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode ch1 = new ExternalKeysNode();  // 创建通道1逻辑节点
            ch1.setParentNode(parent);  // 设置父节点
            ch1.setParentNodes(parents);  // 设置父节点列表
            ch1.setPlace(list.size(), 117 + 55 * i, 77 + topSlipOffset, 55, 35);  // 设置位置和尺寸
            ch1.setName(channels[i]);  // 设置节点名称
            list.add(ch1);  // 添加节点到列表
        }
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode ch2 = new ExternalKeysNode();  // 创建通道2逻辑节点
            ch2.setParentNode(parent);  // 设置父节点
            ch2.setParentNodes(parents);  // 设置父节点列表
            ch2.setPlace(list.size(), 339 + 55 * i, 77 + topSlipOffset, 55, 35);  // 设置位置和尺寸
            ch2.setName(channels[i]);  // 设置节点名称
            list.add(ch2);  // 添加节点到列表
        }
        for (int i = 0; i < logics.length; i++) {  // 循环创建节点
            ExternalKeysNode logic = new ExternalKeysNode();  // 创建逻辑节点
            logic.setParentNode(parent);  // 设置父节点
            logic.setParentNodes(parents);  // 设置父节点列表
            logic.setPlace(list.size(), 117 + 55 * i, 124 + topSlipOffset, 55, 35);  // 设置位置和尺寸
            logic.setName(logics[i]);  // 设置节点名称
            list.add(logic);  // 添加节点到列表
        }
        for (int i = 0; i < conditions.length; i++) {  // 循环创建节点
            ExternalKeysNode condition = new ExternalKeysNode();  // 创建条件节点
            condition.setParentNode(parent);  // 设置父节点
            condition.setParentNodes(parents);  // 设置父节点列表
            condition.setPlace(list.size(), 117 + 55 * i, 171 + topSlipOffset, 55, 35);  // 设置位置和尺寸
            condition.setName(conditions[i]);  // 设置节点名称
            list.add(condition);  // 添加节点到列表
        }
        ExternalKeysNode detail = new ExternalKeysNode();  // 创建详细设置节点
        detail.setParentNode(parent);  // 设置父节点
        detail.setParentNodes(parents);  // 设置父节点列表
        detail.setPlace(list.size(), 548, 167 + topSlipOffset, 100, 40);  // 设置位置和尺寸
        detail.setName("detail");  // 设置节点名称
        list.add(detail);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-NEdge N边沿触发节点(4通道)：源/N边沿斜率/时间/计数详情
     */
    private static List<ExternalKeysNode> getTopTriggerNEdgeDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] nEdgeSlopes = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerNEdgeSlope);  // 从资源文件获取字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 161 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < nEdgeSlopes.length; i++) {  // 循环创建节点
            ExternalKeysNode nEdgeSlope = new ExternalKeysNode();  // 创建N边沿斜率节点
            nEdgeSlope.setParentNode(parent);  // 设置父节点
            nEdgeSlope.setParentNodes(parents);  // 设置父节点列表
            nEdgeSlope.setPlace(list.size(), 837 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            nEdgeSlope.setName(nEdgeSlopes[i]);  // 设置节点名称
            list.add(nEdgeSlope);  // 添加节点到列表
        }
        ExternalKeysNode time = new ExternalKeysNode();  // 创建时间节点
        time.setParentNode(parent);  // 设置父节点
        time.setParentNodes(parents);  // 设置父节点列表
        time.setPlace(list.size(), 1273, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        time.setName("time");  // 设置节点名称
        list.add(time);  // 添加节点到列表
        ExternalKeysNode nEdgeDetail = new ExternalKeysNode();  // 创建N边沿详细节点
        nEdgeDetail.setParentNode(parent);  // 设置父节点
        nEdgeDetail.setParentNodes(parents);  // 设置父节点列表
        nEdgeDetail.setChildNodes(getTopDialogCountDetailNodeList(nEdgeDetail, list));  // 设置子节点列表
        nEdgeDetail.setDialog(ExternalKeysNode.DIALOG_TOPCOUNT);  // 关联弹窗:计数弹窗
        nEdgeDetail.setPlace(list.size(), 1593, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        nEdgeDetail.setName("nEdgeDetail");  // 设置节点名称
        list.add(nEdgeDetail);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-NEdge N边沿触发节点(8通道)：源/N边沿斜率/时间/计数详情
     */
    private static List<ExternalKeysNode> getTopTriggerNEdgeDetailEightNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] nEdgeSlopes = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerNEdgeSlope);  // 从资源文件获取字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 161 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < nEdgeSlopes.length; i++) {  // 循环创建节点
            ExternalKeysNode nEdgeSlope = new ExternalKeysNode();  // 创建N边沿斜率节点
            nEdgeSlope.setParentNode(parent);  // 设置父节点
            nEdgeSlope.setParentNodes(parents);  // 设置父节点列表
            nEdgeSlope.setPlace(list.size(), 161 + 120 * i, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            nEdgeSlope.setName(nEdgeSlopes[i]);  // 设置节点名称
            list.add(nEdgeSlope);  // 添加节点到列表
        }
        ExternalKeysNode time = new ExternalKeysNode();  // 创建时间节点
        time.setParentNode(parent);  // 设置父节点
        time.setParentNodes(parents);  // 设置父节点列表
        time.setPlace(list.size(), 641, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        time.setName("time");  // 设置节点名称
        list.add(time);  // 添加节点到列表
        ExternalKeysNode nEdgeDetail = new ExternalKeysNode();  // 创建N边沿详细节点
        nEdgeDetail.setParentNode(parent);  // 设置父节点
        nEdgeDetail.setParentNodes(parents);  // 设置父节点列表
        nEdgeDetail.setChildNodes(getTopDialogCountDetailNodeList(nEdgeDetail, list));  // 设置子节点列表
        nEdgeDetail.setDialog(ExternalKeysNode.DIALOG_TOPCOUNT);  // 关联弹窗:计数弹窗
        nEdgeDetail.setPlace(list.size(), 1001, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        nEdgeDetail.setName("nEdgeDetail");  // 设置节点名称
        list.add(nEdgeDetail);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Runt矮脉冲触发节点(8通道)：源/极性/条件/时间范围
     */
    private static List<ExternalKeysNode> getTopTriggerRuntDetailEightNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] polars = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerRuntPolar);  // 从资源文件获取字符串数组
        String[] conditions = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerRuntCondition);  // 从资源文件获取字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 161 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < polars.length; i++) {  // 循环创建节点
            ExternalKeysNode polar = new ExternalKeysNode();  // 创建极性节点
            polar.setParentNode(parent);  // 设置父节点
            polar.setParentNodes(parents);  // 设置父节点列表
            polar.setPlace(list.size(), 161 + 120 * i, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            polar.setName(polars[i]);  // 设置节点名称
            list.add(polar);  // 添加节点到列表
        }
        for (int i = 0; i < conditions.length; i++) {  // 循环创建节点
            ExternalKeysNode condition = new ExternalKeysNode();  // 创建条件节点
            condition.setParentNode(parent);  // 设置父节点
            condition.setParentNodes(parents);  // 设置父节点列表
            condition.setPlace(list.size(), 161 + 120 * i, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            condition.setName(conditions[i]);  // 设置节点名称
            list.add(condition);  // 添加节点到列表
        }
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode time = new ExternalKeysNode();  // 创建时间节点
            time.setParentNode(parent);  // 设置父节点
            time.setParentNodes(parents);  // 设置父节点列表
            time.setPlace(list.size(), 790 + 269 * i, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            time.setName("time:" + (i == 0 ? "maxTime" : "minTime"));  // 设置节点名称
            list.add(time);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Runt矮脉冲触发节点(4通道)：源/极性/条件/时间范围
     */
    private static List<ExternalKeysNode> getTopTriggerRuntDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] polars = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerRuntPolar);  // 从资源文件获取字符串数组
        String[] conditions = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerRuntCondition);  // 从资源文件获取字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 161 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < polars.length; i++) {  // 循环创建节点
            ExternalKeysNode polar = new ExternalKeysNode();  // 创建极性节点
            polar.setParentNode(parent);  // 设置父节点
            polar.setParentNodes(parents);  // 设置父节点列表
            polar.setPlace(list.size(), 1269 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            polar.setName(polars[i]);  // 设置节点名称
            list.add(polar);  // 添加节点到列表
        }
        for (int i = 0; i < conditions.length; i++) {  // 循环创建节点
            ExternalKeysNode condition = new ExternalKeysNode();  // 创建条件节点
            condition.setParentNode(parent);  // 设置父节点
            condition.setParentNodes(parents);  // 设置父节点列表
            condition.setPlace(list.size(), 161 + 120 * i, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            condition.setName(conditions[i]);  // 设置节点名称
            list.add(condition);  // 添加节点到列表
        }
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode time = new ExternalKeysNode();  // 创建时间节点
            time.setParentNode(parent);  // 设置父节点
            time.setParentNodes(parents);  // 设置父节点列表
            time.setPlace(list.size(), 1268 + 240 * i, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            time.setName("time:" + (i == 0 ? "maxTime" : "minTime"));  // 设置节点名称
            list.add(time);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Slope斜率触发节点(8通道)：源/边沿/条件/时间范围
     */
    private static List<ExternalKeysNode> getTopTriggerSlopeDetailEightNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] edges = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerSlopeEdge);  // 从资源文件获取字符串数组
        String[] conditions = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerSlopeCondition);  // 从资源文件获取字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 161 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < edges.length; i++) {  // 循环创建节点
            ExternalKeysNode edge = new ExternalKeysNode();  // 创建边沿节点
            edge.setParentNode(parent);  // 设置父节点
            edge.setParentNodes(parents);  // 设置父节点列表
            edge.setPlace(list.size(), 161 + 120 * i, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            edge.setName(edges[i]);  // 设置节点名称
            list.add(edge);  // 添加节点到列表
        }
        for (int i = 0; i < conditions.length; i++) {  // 循环创建节点
            ExternalKeysNode condition = new ExternalKeysNode();  // 创建条件节点
            condition.setParentNode(parent);  // 设置父节点
            condition.setParentNodes(parents);  // 设置父节点列表
            condition.setPlace(list.size(), 161 + 120 * i, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            condition.setName(conditions[i]);  // 设置节点名称
            list.add(condition);  // 添加节点到列表
        }
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode time = new ExternalKeysNode();  // 创建时间节点
            time.setParentNode(parent);  // 设置父节点
            time.setParentNodes(parents);  // 设置父节点列表
            time.setPlace(list.size(), 670 + 269 * i, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            time.setName("time:" + (i == 0 ? "maxTime" : "minTime"));  // 设置节点名称
            list.add(time);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Slope斜率触发节点(4通道)：源/边沿/条件/时间范围
     */
    private static List<ExternalKeysNode> getTopTriggerSlopeDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] edges = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerSlopeEdge);  // 从资源文件获取字符串数组
        String[] conditions = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerSlopeCondition);  // 从资源文件获取字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 161 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < edges.length; i++) {  // 循环创建节点
            ExternalKeysNode edge = new ExternalKeysNode();  // 创建边沿节点
            edge.setParentNode(parent);  // 设置父节点
            edge.setParentNodes(parents);  // 设置父节点列表
            edge.setPlace(list.size(), 1269 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            edge.setName(edges[i]);  // 设置节点名称
            list.add(edge);  // 添加节点到列表
        }
        for (int i = 0; i < conditions.length; i++) {  // 循环创建节点
            ExternalKeysNode condition = new ExternalKeysNode();  // 创建条件节点
            condition.setParentNode(parent);  // 设置父节点
            condition.setParentNodes(parents);  // 设置父节点列表
            condition.setPlace(list.size(), 161 + 120 * i, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            condition.setName(conditions[i]);  // 设置节点名称
            list.add(condition);  // 添加节点到列表
        }
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode time = new ExternalKeysNode();  // 创建时间节点
            time.setParentNode(parent);  // 设置父节点
            time.setParentNodes(parents);  // 设置父节点列表
            time.setPlace(list.size(), 1268 + 240 * i, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            time.setName("time:" + (i == 0 ? "maxTime" : "minTime"));  // 设置节点名称
            list.add(time);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Timeout超时触发节点：源/极性/详细
     */
    private static List<ExternalKeysNode> getTopTriggerTimeoutDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        int polarsStartPx;
        int detailStartPx;
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) {  // 判断是否为4通道型号
            polarsStartPx = 857;
            detailStartPx = 1433;
        } else {  // 否则
            polarsStartPx = 309;
            detailStartPx = 505;
        }
        String[] polars = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerTimeoutPolar);  // 从资源文件获取字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 161 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < polars.length; i++) {  // 循环创建节点
            ExternalKeysNode polar = new ExternalKeysNode();  // 创建极性节点
            polar.setParentNode(parent);  // 设置父节点
            polar.setParentNodes(parents);  // 设置父节点列表
            polar.setPlace(list.size(), polarsStartPx + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            polar.setName(polars[i]);  // 设置节点名称
            list.add(polar);  // 添加节点到列表
        }
        ExternalKeysNode detail = new ExternalKeysNode();  // 创建详细设置节点
        detail.setParentNode(parent);  // 设置父节点
        detail.setParentNodes(parents);  // 设置父节点列表
        detail.setPlace(list.size(), detailStartPx, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        detail.setName("detail");  // 设置节点名称
        list.add(detail);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Timeout超时触发节点(8通道)：源/极性/详细
     */
    private static List<ExternalKeysNode> getTopTriggerTimeoutDetailEightNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] polars = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerTimeoutPolar);  // 从资源文件获取字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 161 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < polars.length; i++) {  // 循环创建节点
            ExternalKeysNode polar = new ExternalKeysNode();  // 创建极性节点
            polar.setParentNode(parent);  // 设置父节点
            polar.setParentNodes(parents);  // 设置父节点列表
            polar.setPlace(list.size(), 161 + 120 * i, 275 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            polar.setName(polars[i]);  // 设置节点名称
            list.add(polar);  // 添加节点到列表
        }
        ExternalKeysNode detail = new ExternalKeysNode();  // 创建详细设置节点
        detail.setParentNode(parent);  // 设置父节点
        detail.setParentNodes(parents);  // 设置父节点列表
        detail.setPlace(list.size(), 761, 275 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        detail.setName("detail");  // 设置节点名称
        list.add(detail);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Video视频触发节点：源/极性/标准(6种制式)
     */
    private static List<ExternalKeysNode> getTopTriggerVideoDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] polars = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerVideoPolar);  // 从资源文件获取字符串数组
        String[] standards = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerVideoStandard);  // 从资源文件获取字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 141 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < polars.length; i++) {  // 循环创建节点
            ExternalKeysNode polar = new ExternalKeysNode();  // 创建极性节点
            polar.setParentNode(parent);  // 设置父节点
            polar.setParentNodes(parents);  // 设置父节点列表
            if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {  // 判断是否为8通道型号
                polar.setPlace(list.size(), 1341 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            } else {  // 否则
                polar.setPlace(list.size(), 1289 + 120 * i, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            }
            polar.setName(polars[i]);  // 设置节点名称
            list.add(polar);  // 添加节点到列表
        }
        ExternalKeysNode model0 = new ExternalKeysNode();  // 创建制式0节点
        model0.setParentNode(parent);  // 设置父节点
        model0.setParentNodes(parents);  // 设置父节点列表
        model0.setPlace(list.size(), 141, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        model0.setName(standards[0]);  // 设置节点名称
        model0.setType(ExternalKeysNode.TYPE_TRIGGER_VIDEO_STANDARD_FIRST);  // 类型:视频触发标准(首个)
        model0.setChildNodes(getTopTriggerVideoModel012NodeList(parent, parents, list, topSlipOffset));  // 设置子节点列表
        list.add(model0);  // 添加节点到列表
        ExternalKeysNode model1 = new ExternalKeysNode();  // 创建制式1节点
        model1.setParentNode(parent);  // 设置父节点
        model1.setParentNodes(parents);  // 设置父节点列表
        model1.setPlace(list.size(), 261, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        model1.setName(standards[1]);  // 设置节点名称
        model1.setChildNodes(getTopTriggerVideoModel012NodeList(parent, parents, list, topSlipOffset));  // 设置子节点列表
        list.add(model1);  // 添加节点到列表
        ExternalKeysNode model2 = new ExternalKeysNode();  // 创建制式2节点
        model2.setParentNode(parent);  // 设置父节点
        model2.setParentNodes(parents);  // 设置父节点列表
        model2.setPlace(list.size(), 381, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        model2.setName(standards[2]);  // 设置节点名称
        model2.setChildNodes(getTopTriggerVideoModel012NodeList(parent, parents, list, topSlipOffset));  // 设置子节点列表
        list.add(model2);  // 添加节点到列表
        ExternalKeysNode model3 = new ExternalKeysNode();  // 创建制式3节点
        model3.setParentNode(parent);  // 设置父节点
        model3.setParentNodes(parents);  // 设置父节点列表
        model3.setPlace(list.size(), 501, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        model3.setName(standards[3]);  // 设置节点名称
        model3.setChildNodes(getTopTriggerVideoModel3NodeList(parent, parents, list, topSlipOffset));  // 设置子节点列表
        list.add(model3);  // 添加节点到列表
        ExternalKeysNode model4 = new ExternalKeysNode();  // 创建制式4节点
        model4.setParentNode(parent);  // 设置父节点
        model4.setParentNodes(parents);  // 设置父节点列表
        model4.setPlace(list.size(), 621, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        model4.setName(standards[4]);  // 设置节点名称
        model4.setChildNodes(getTopTriggerVideoModel4NodeList(parent, parents, list, topSlipOffset));  // 设置子节点列表
        list.add(model4);  // 添加节点到列表
        ExternalKeysNode model5 = new ExternalKeysNode();  // 创建制式5节点
        model5.setParentNode(parent);  // 设置父节点
        model5.setParentNodes(parents);  // 设置父节点列表
        model5.setPlace(list.size(), 741, 265 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        model5.setName(standards[5]);  // 设置节点名称
        model5.setChildNodes(getTopTriggerVideoModel5NodeList(parent, parents, list, topSlipOffset));  // 设置子节点列表
        list.add(model5);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建视频触发制式0/1/2节点：触发条件/行号
     */
    private static List<ExternalKeysNode> getTopTriggerVideoModel012NodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, List<ExternalKeysNode> lineParents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] strings = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerVideoTriggerMore);  // 从资源文件获取字符串数组
        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode trigger = new ExternalKeysNode();  // 创建触发节点
            trigger.setParentNode(parent);  // 设置父节点
            trigger.setParentNodes(parents);  // 设置父节点列表
            trigger.setPlace(list.size() + 12, 141 + 120 * i, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            trigger.setName(strings[i]);  // 设置节点名称
            list.add(trigger);  // 添加节点到列表
        }
        ExternalKeysNode line = new ExternalKeysNode();  // 创建行号节点
        line.setParentNode(parent);  // 设置父节点
        line.setParentNodes(parents);  // 设置父节点列表
        line.setPlace(list.size() + 12, 967, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        line.setName("line");  // 设置节点名称
        line.setType(ExternalKeysNode.TYPE_TRIGGER_VIDEO_LINE);  // 类型:视频触发行号
        line.setChildNodes(getTopDialogCountDetailNodeList(line, lineParents));  // 设置子节点列表
        line.setDialog(ExternalKeysNode.DIALOG_TOPCOUNT);  // 关联弹窗:计数弹窗
        list.add(line);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建视频触发制式3节点：触发条件/行号/频率
     */
    private static List<ExternalKeysNode> getTopTriggerVideoModel3NodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, List<ExternalKeysNode> lineParents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] triggers = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerVideoTriggerLess);  // 从资源文件获取字符串数组
        String[] frequencys = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerVideoFrequencyLess);  // 从资源文件获取字符串数组
        for (int i = 0; i < 3; i++) {  // 循环创建节点
            ExternalKeysNode trigger = new ExternalKeysNode();  // 创建触发节点
            trigger.setParentNode(parent);  // 设置父节点
            trigger.setParentNodes(parents);  // 设置父节点列表
            trigger.setPlace(list.size() + 12, 141 + 120 * i, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            trigger.setName(triggers[i]);  // 设置节点名称
            list.add(trigger);  // 添加节点到列表
        }
        ExternalKeysNode line = new ExternalKeysNode();  // 创建行号节点
        line.setParentNode(parent);  // 设置父节点
        line.setParentNodes(parents);  // 设置父节点列表
        line.setPlace(list.size() + 12, 967, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        line.setName("line");  // 设置节点名称
        line.setType(ExternalKeysNode.TYPE_TRIGGER_VIDEO_LINE);  // 类型:视频触发行号
        line.setChildNodes(getTopDialogCountDetailNodeList(line, lineParents));  // 设置子节点列表
        line.setDialog(ExternalKeysNode.DIALOG_TOPCOUNT);  // 关联弹窗:计数弹窗
        list.add(line);  // 添加节点到列表
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode hz = new ExternalKeysNode();  // 创建节点节点
            hz.setParentNode(parent);  // 设置父节点
            hz.setParentNodes(parents);  // 设置父节点列表
            hz.setPlace(list.size() + 12, 141 + 120 * i, 405 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            hz.setName(frequencys[i]);  // 设置节点名称
            list.add(hz);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建视频触发制式4节点：触发条件/行号/频率
     */
    private static List<ExternalKeysNode> getTopTriggerVideoModel4NodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, List<ExternalKeysNode> lineParents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] triggers = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerVideoTriggerMore);  // 从资源文件获取字符串数组
        String[] frequencys = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerVideoFrequencyLess);  // 从资源文件获取字符串数组
        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode trigger = new ExternalKeysNode();  // 创建触发节点
            trigger.setParentNode(parent);  // 设置父节点
            trigger.setParentNodes(parents);  // 设置父节点列表
            trigger.setPlace(list.size() + 12, 141 + 120 * i, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            trigger.setName(triggers[i]);  // 设置节点名称
            list.add(trigger);  // 添加节点到列表
        }
        ExternalKeysNode line = new ExternalKeysNode();  // 创建行号节点
        line.setParentNode(parent);  // 设置父节点
        line.setParentNodes(parents);  // 设置父节点列表
        line.setPlace(list.size() + 12, 967, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        line.setName("line");  // 设置节点名称
        line.setType(ExternalKeysNode.TYPE_TRIGGER_VIDEO_LINE);  // 类型:视频触发行号
        line.setChildNodes(getTopDialogCountDetailNodeList(line, lineParents));  // 设置子节点列表
        line.setDialog(ExternalKeysNode.DIALOG_TOPCOUNT);  // 关联弹窗:计数弹窗
        list.add(line);  // 添加节点到列表
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode hz = new ExternalKeysNode();  // 创建节点节点
            hz.setParentNode(parent);  // 设置父节点
            hz.setParentNodes(parents);  // 设置父节点列表
            hz.setPlace(list.size() + 12, 141 + 120 * i, 405 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            hz.setName(frequencys[i]);  // 设置节点名称
            list.add(hz);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建视频触发制式5节点：触发条件/行号/频率
     */
    private static List<ExternalKeysNode> getTopTriggerVideoModel5NodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, List<ExternalKeysNode> lineParents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] triggers = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerVideoTriggerLess);  // 从资源文件获取字符串数组
        String[] frequencys = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerVideoFrequencyMore);  // 从资源文件获取字符串数组
        for (int i = 0; i < 3; i++) {  // 循环创建节点
            ExternalKeysNode trigger = new ExternalKeysNode();  // 创建触发节点
            trigger.setParentNode(parent);  // 设置父节点
            trigger.setParentNodes(parents);  // 设置父节点列表
            trigger.setPlace(list.size() + 12, 141 + 120 * i, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            trigger.setName(triggers[i]);  // 设置节点名称
            list.add(trigger);  // 添加节点到列表
        }
        ExternalKeysNode line = new ExternalKeysNode();  // 创建行号节点
        line.setParentNode(parent);  // 设置父节点
        line.setParentNodes(parents);  // 设置父节点列表
        line.setPlace(list.size() + 12, 967, 335 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        line.setName("line");  // 设置节点名称
        line.setType(ExternalKeysNode.TYPE_TRIGGER_VIDEO_LINE);  // 类型:视频触发行号
        line.setChildNodes(getTopDialogCountDetailNodeList(line, lineParents));  // 设置子节点列表
        line.setDialog(ExternalKeysNode.DIALOG_TOPCOUNT);  // 关联弹窗:计数弹窗
        list.add(line);  // 添加节点到列表
        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode hz = new ExternalKeysNode();  // 创建节点节点
            hz.setParentNode(parent);  // 设置父节点
            hz.setParentNodes(parents);  // 设置父节点列表
            hz.setPlace(list.size() + 12, 141 + 120 * i, 405 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            hz.setName(frequencys[i]);  // 设置节点名称
            list.add(hz);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Trigger-Serials串行触发节点：UART/LIN/CAN/SPI/I2C/429/1553B子协议列表
     */
    private static List<ExternalKeysNode> getTopTriggerSerialsDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode uart = new ExternalKeysNode();  // 创建UART节点
        uart.setParentNode(parent);  // 设置父节点
        uart.setParentNodes(parents);  // 设置父节点列表
        uart.setPlace(list.size(), 0, 0, 0, 0);  // 设置位置和尺寸
        uart.setName("uart");  // 设置节点名称
        uart.setChildNodes(getTopTriggerSerialsUartDetailNodeList(parent, parents, topSlipOffset));  // 设置子节点列表
        list.add(uart);  // 添加节点到列表

        ExternalKeysNode lin = new ExternalKeysNode();  // 创建LIN节点
        lin.setParentNode(parent);  // 设置父节点
        lin.setParentNodes(parents);  // 设置父节点列表
        lin.setPlace(list.size(), 0, 0, 0, 0);  // 设置位置和尺寸
        lin.setName("lin");  // 设置节点名称
        lin.setChildNodes(getTopTriggerSerialsLinDetailNodeList(parent, parents, topSlipOffset));  // 设置子节点列表
        list.add(lin);  // 添加节点到列表

        ExternalKeysNode can = new ExternalKeysNode();  // 创建CAN节点
        can.setParentNode(parent);  // 设置父节点
        can.setParentNodes(parents);  // 设置父节点列表
        can.setPlace(list.size(), 0, 0, 0, 0);  // 设置位置和尺寸
        can.setName("can");  // 设置节点名称
        can.setChildNodes(getTopTriggerSerialsCanDetailNodeList(parent, parents, topSlipOffset));  // 设置子节点列表
        list.add(can);  // 添加节点到列表

        ExternalKeysNode spi = new ExternalKeysNode();  // 创建SPI节点
        spi.setParentNode(parent);  // 设置父节点
        spi.setParentNodes(parents);  // 设置父节点列表
        spi.setPlace(list.size(), 0, 0, 0, 0);  // 设置位置和尺寸
        spi.setName("spi");  // 设置节点名称
        spi.setChildNodes(getTopTriggerSerialsSpiDetailNodeList(parent, parents, topSlipOffset));  // 设置子节点列表
        list.add(spi);  // 添加节点到列表

        ExternalKeysNode i2c = new ExternalKeysNode();  // 创建I2C节点
        i2c.setParentNode(parent);  // 设置父节点
        i2c.setParentNodes(parents);  // 设置父节点列表
        i2c.setPlace(list.size(), 0, 0, 0, 0);  // 设置位置和尺寸
        i2c.setName("i2c");  // 设置节点名称
        i2c.setChildNodes(getTopTriggerSerialsI2cDetailNodeList(parent, parents, topSlipOffset));  // 设置子节点列表
        list.add(i2c);  // 添加节点到列表

        ExternalKeysNode m429 = new ExternalKeysNode();  // 创建ARINC429节点
        m429.setParentNode(parent);  // 设置父节点
        m429.setParentNodes(parents);  // 设置父节点列表
        m429.setPlace(list.size(), 0, 0, 0, 0);  // 设置位置和尺寸
        m429.setName("429");  // 设置节点名称
        m429.setChildNodes(getTopTriggerSerialsM429DetailNodeList(parent, parents, topSlipOffset));  // 设置子节点列表
        list.add(m429);  // 添加节点到列表

        ExternalKeysNode m1553b = new ExternalKeysNode();  // 创建1553B节点
        m1553b.setParentNode(parent);  // 设置父节点
        m1553b.setParentNodes(parents);  // 设置父节点列表
        m1553b.setPlace(list.size(), 0, 0, 0, 0);  // 设置位置和尺寸
        m1553b.setName("1553b");  // 设置节点名称
        m1553b.setChildNodes(getTopTriggerSerialsM1553bDetailNodeList(parent, parents, topSlipOffset));  // 设置子节点列表
        list.add(m1553b);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建串行触发-UART详情节点：波特率/数据位/停止位/校验等配置
     */
    private static List<ExternalKeysNode> getTopTriggerSerialsUartDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] uarts = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerSerialsUART);  // 从资源文件获取字符串数组
        String[] conditionStr = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerSerialsUARTCondition);  // 从资源文件获取字符串数组
        int serialsNumber = 1;  // 定义整型变量
        if (parent.getIndex() == 9) {  // 根据父节点索引判断串行总线编号
            serialsNumber = 1;
        } else if (parent.getIndex() == 10) {
            serialsNumber = 2;
        } else if (parent.getIndex() == 11) {
            serialsNumber = 3;
        } else if (parent.getIndex() == 12) {
            serialsNumber = 4;
        }
        for (int i = 0; i < 7; i++) {  // 循环创建节点
            int x = 15 + 225 * (i % 7);  // 定义整型变量
            int y = i < 7 ? 200 + topSlipOffset : (i < 10 ? 131 + topSlipOffset : 181 + topSlipOffset);  // 定义整型变量
            ExternalKeysNode item = new ExternalKeysNode();  // 创建条目节点
            item.setParentNode(parent);  // 设置父节点
            item.setParentNodes(parents);  // 设置父节点列表
            item.setPlace(list.size(), x, y, 195, 60);  // 设置位置和尺寸
            item.setName(uarts[i]);  // 设置节点名称
            item.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
            if (i == 0) {  // 第一项特殊处理
                item.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_UART + serialsNumber));  // 设置当前选中项
            } else if (i == 2 || i == 3 || i == 4 || i == 5) {
                List<ExternalKeysNode> conditions = new ArrayList<>();  // 创建节点列表
                for (int j = 0; j < 4; j++) {  // 循环创建节点
                    ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                    node.setParentNode(item);  // 设置父节点
                    node.setParentNodes(list);  // 设置父节点列表
                    node.setPlace(conditions.size(), 126 + 120 * j, 275 + topSlipOffset, 120, 60);  // 设置位置和尺寸
                    node.setName(conditionStr[j]);  // 设置节点名称
                    conditions.add(node);
                }
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(conditions.size(), 828, 278 + topSlipOffset, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, conditions, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node.setName("data");  // 设置节点名称
                conditions.add(node);

                item.setChildNodes(conditions);  // 设置子节点列表
            }
            list.add(item);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建串行触发-LIN详情节点：波特率/条件/ID/数据
     */
    private static List<ExternalKeysNode> getTopTriggerSerialsLinDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] lins = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerSerialsLIN);  // 从资源文件获取字符串数组
        int serialsNumber = parent.getIndex() == 9 ? 1 : 2;  // 定义整型变量
        for (int i = 0; i < 5; i++) {//现在triggerSerialsLIN是5个选项
            int x = 15 + 225 * (i % 8);  // 定义整型变量
            int y = i < 8 ? 200 + topSlipOffset : (i < 10 ? 270 + topSlipOffset : 181 + topSlipOffset);//目前容量一行最多8个，否则换行后Y变
            ExternalKeysNode item = new ExternalKeysNode();  // 创建条目节点
            item.setParentNode(parent);  // 设置父节点
            item.setParentNodes(parents);  // 设置父节点列表
            item.setPlace(list.size(), x, y, 195, 60);  // 设置位置和尺寸
            item.setName(lins[i]);  // 设置节点名称
            item.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
            if (i == 0) {  // 第一项特殊处理
                item.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_LIN + serialsNumber));  // 设置当前选中项
            } else if (i == 1) {
                List<ExternalKeysNode> frameIds = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(frameIds.size(), 130, 311, 350, 54);  // 设置位置和尺寸
                node.setName("id");  // 设置节点名称
                node.setChildNodes(getNumberKeyBoardNodeList(node, frameIds, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                frameIds.add(node);

                item.setChildNodes(frameIds);  // 设置子节点列表
            } else if (i == 2) {
                List<ExternalKeysNode> idDatas = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(idDatas.size(), 130, 311, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, idDatas, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node.setName("id");  // 设置节点名称
                idDatas.add(node);
                ExternalKeysNode node2 = new ExternalKeysNode();  // 创建第二个节点节点
                node2.setParentNode(item);  // 设置父节点
                node2.setParentNodes(list);  // 设置父节点列表
                node2.setPlace(idDatas.size(), 620, 311, 350, 54);  // 设置位置和尺寸
                node2.setChildNodes(getNumberKeyBoardNodeList(node2, idDatas, false));  // 设置子节点列表
                node2.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node2.setName("data");  // 设置节点名称
                idDatas.add(node2);

                item.setChildNodes(idDatas);  // 设置子节点列表
            }
            list.add(item);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建串行触发-CAN详情节点：波特率/ID类型/ID/Data
     */
    private static List<ExternalKeysNode> getTopTriggerSerialsCanDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] cans = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerSerialsCAN);  // 从资源文件获取字符串数组
        int serialsNumber = parent.getIndex() == 9 ? 1 : 2;  // 定义整型变量
        for (int i = 0; i < 9; i++) {  // 循环创建节点
            int x = 15 + 225 * (i % 8);  // 定义整型变量
            int y = i < 8 ? 200 + topSlipOffset : (i < 10 ? 270 + topSlipOffset : 181 + topSlipOffset);  // 定义整型变量
            ExternalKeysNode item = new ExternalKeysNode();  // 创建条目节点
            item.setParentNode(parent);  // 设置父节点
            item.setParentNodes(parents);  // 设置父节点列表
            item.setPlace(list.size(), x, y, 195, 60);  // 设置位置和尺寸
            item.setName(cans[i]);  // 设置节点名称
            item.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
            if (i == 0) {  // 第一项特殊处理
                item.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN + serialsNumber));  // 设置当前选中项
            } else if (i == 1 || i == 2 || i == 3) {
                List<ExternalKeysNode> nodes = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(nodes.size(), 130, 381, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, nodes, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node.getChildNodes().get(3).setVisible(false);  // 设为不可见
                node.getChildNodes().get(4).setVisible(false);  // 设为不可见
                node.getChildNodes().get(5).setVisible(false);  // 设为不可见
                node.getChildNodes().get(17).setVisible(false);  // 设为不可见
                node.getChildNodes().get(19).setVisible(false);  // 设为不可见
                nodes.add(node);

                item.setChildNodes(nodes);  // 设置子节点列表
            } else if (i == 4) {
                List<ExternalKeysNode> idDatas = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(idDatas.size(), 130, 381, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, idDatas, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node.setName("id");  // 设置节点名称
                node.getChildNodes().get(3).setVisible(false);  // 设为不可见
                node.getChildNodes().get(4).setVisible(false);  // 设为不可见
                node.getChildNodes().get(5).setVisible(false);  // 设为不可见
                node.getChildNodes().get(17).setVisible(false);  // 设为不可见
                node.getChildNodes().get(19).setVisible(false);  // 设为不可见
                idDatas.add(node);
                ExternalKeysNode node2 = new ExternalKeysNode();  // 创建第二个节点节点
                node2.setParentNode(item);  // 设置父节点
                node2.setParentNodes(list);  // 设置父节点列表
                node2.setPlace(idDatas.size(), 620, 381, 350, 54);  // 设置位置和尺寸
                node2.setChildNodes(getNumberKeyBoardNodeList(node2, idDatas, false));  // 设置子节点列表
                node2.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node2.setName("dic");  // 设置节点名称
                node2.getChildNodes().get(3).setVisible(false);  // 设为不可见
                node2.getChildNodes().get(4).setVisible(false);  // 设为不可见
                node2.getChildNodes().get(5).setVisible(false);  // 设为不可见
                node2.getChildNodes().get(17).setVisible(false);  // 设为不可见
                node2.getChildNodes().get(19).setVisible(false);  // 设为不可见
                idDatas.add(node2);
                ExternalKeysNode node3 = new ExternalKeysNode();  // 创建第三个节点节点
                node3.setParentNode(item);  // 设置父节点
                node3.setParentNodes(list);  // 设置父节点列表
                node3.setPlace(idDatas.size(), 1110, 381, 350, 54);  // 设置位置和尺寸
                node3.setChildNodes(getNumberKeyBoardNodeList(node3, idDatas, false));  // 设置子节点列表
                node3.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node3.setName("data");  // 设置节点名称
                node3.getChildNodes().get(3).setVisible(false);  // 设为不可见
                node3.getChildNodes().get(4).setVisible(false);  // 设为不可见
                node3.getChildNodes().get(5).setVisible(false);  // 设为不可见
                node3.getChildNodes().get(17).setVisible(false);  // 设为不可见
                node3.getChildNodes().get(19).setVisible(false);  // 设为不可见
                idDatas.add(node3);

                item.setChildNodes(idDatas);  // 设置子节点列表
            }
            list.add(item);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建串行触发-SPI详情节点：时钟/数据
     */
    private static List<ExternalKeysNode> getTopTriggerSerialsSpiDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] spis = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerSerialsSPI);  // 从资源文件获取字符串数组
        int serialsNumber = parent.getIndex() == 9 ? 1 : 2;  // 定义整型变量
        for (int i = 0; i < 3; i++) {  // 循环创建节点
            int x = 15 + 225 * (i % 5);  // 定义整型变量
            int y = i < 5 ? 200 + topSlipOffset : (i < 10 ? 167 + topSlipOffset : 181 + topSlipOffset);  // 定义整型变量
            ExternalKeysNode item = new ExternalKeysNode();  // 创建条目节点
            item.setParentNode(parent);  // 设置父节点
            item.setParentNodes(parents);  // 设置父节点列表
            item.setPlace(list.size(), x, y, 195, 60);  // 设置位置和尺寸
            item.setName(spis[i]);  // 设置节点名称
            item.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
            if (i == 0) {  // 第一项特殊处理
                item.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_SPI + serialsNumber));  // 设置当前选中项
            } else if (i == 1) {
                List<ExternalKeysNode> datas = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(datas.size(), 130, 311, 600, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, datas, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                datas.add(node);

                item.setChildNodes(datas);  // 设置子节点列表
            }
            list.add(item);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建串行触发-I2C详情节点：地址/数据/条件
     */
    private static List<ExternalKeysNode> getTopTriggerSerialsI2cDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] i2cs = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerSerialsI2C);  // 从资源文件获取字符串数组
        String[] conditions = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerSerialsUARTCondition);  // 从资源文件获取字符串数组
        int serialsNumber = parent.getIndex() == 9 ? 1 : 2;  // 定义整型变量
        for (int i = 0; i < 9; i++) {  // 循环创建节点
            int x = 15 + 225 * (i % 8);  // 定义整型变量
            int y = i < 8 ? 200 + topSlipOffset : (i < 10 ? 270 + topSlipOffset : 181 + topSlipOffset);  // 定义整型变量
            ExternalKeysNode item = new ExternalKeysNode();  // 创建条目节点
            item.setParentNode(parent);  // 设置父节点
            item.setParentNodes(parents);  // 设置父节点列表
            item.setPlace(list.size(), x, y, 195, 60);  // 设置位置和尺寸
            item.setName(i2cs[i]);  // 设置节点名称
            item.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
            if (i == 0) {  // 第一项特殊处理
                item.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_I2C + serialsNumber));  // 设置当前选中项
            } else if (i == 4) {
                List<ExternalKeysNode> noAcks = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(noAcks.size(), 130, 381, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, noAcks, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                noAcks.add(node);

                item.setChildNodes(noAcks);  // 设置子节点列表
            } else if (i == 5) {
                List<ExternalKeysNode> frame1s = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(frame1s.size(), 130, 381, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, frame1s, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node.setName("addr");  // 设置节点名称
                frame1s.add(node);
                ExternalKeysNode node2 = new ExternalKeysNode();  // 创建第二个节点节点
                node2.setParentNode(item);  // 设置父节点
                node2.setParentNodes(list);  // 设置父节点列表
                node2.setPlace(frame1s.size(), 620, 381, 350, 54);  // 设置位置和尺寸
                node2.setChildNodes(getNumberKeyBoardNodeList(node2, frame1s, false));  // 设置子节点列表
                node2.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node2.setName("data");  // 设置节点名称
                frame1s.add(node2);

                item.setChildNodes(frame1s);  // 设置子节点列表
            } else if (i == 6) {
                List<ExternalKeysNode> frame2s = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(frame2s.size(), 130, 381, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, frame2s, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node.setName("addr");  // 设置节点名称
                frame2s.add(node);
                ExternalKeysNode node2 = new ExternalKeysNode();  // 创建第二个节点节点
                node2.setParentNode(item);  // 设置父节点
                node2.setParentNodes(list);  // 设置父节点列表
                node2.setPlace(frame2s.size(), 620, 381, 350, 54);  // 设置位置和尺寸
                node2.setChildNodes(getNumberKeyBoardNodeList(node2, frame2s, false));  // 设置子节点列表
                node2.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node2.setName("data1");  // 设置节点名称
                frame2s.add(node2);
                ExternalKeysNode node3 = new ExternalKeysNode();  // 创建第三个节点节点
                node3.setParentNode(item);  // 设置父节点
                node3.setParentNodes(list);  // 设置父节点列表
                node3.setPlace(frame2s.size(), 1110, 381, 350, 54);  // 设置位置和尺寸
                node3.setChildNodes(getNumberKeyBoardNodeList(node3, frame2s, false));  // 设置子节点列表
                node3.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node3.setName("data2");  // 设置节点名称
                frame2s.add(node3);

                item.setChildNodes(frame2s);  // 设置子节点列表
            } else if (i == 7) {
                List<ExternalKeysNode> romDatas = new ArrayList<>();  // 创建节点列表
                for (int j = 0; j < 4; j++) {  // 循环创建节点
                    ExternalKeysNode condition = new ExternalKeysNode();  // 创建条件节点
                    condition.setParentNode(item);  // 设置父节点
                    condition.setParentNodes(list);  // 设置父节点列表
                    condition.setPlace(romDatas.size(), 126 + 120 * j, 381, 120, 60);  // 设置位置和尺寸
                    condition.setName(conditions[j]);  // 设置节点名称
                    romDatas.add(condition);
                }
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(romDatas.size(), 746, 384, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, romDatas, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node.setName("data1");  // 设置节点名称
                romDatas.add(node);

                item.setChildNodes(romDatas);  // 设置子节点列表
            } else if (i == 8) {
                List<ExternalKeysNode> writeFrames = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(writeFrames.size(), 130, 381, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, writeFrames, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node.setName("addr");  // 设置节点名称
                writeFrames.add(node);
                ExternalKeysNode node2 = new ExternalKeysNode();  // 创建第二个节点节点
                node2.setParentNode(item);  // 设置父节点
                node2.setParentNodes(list);  // 设置父节点列表
                node2.setPlace(writeFrames.size(), 620, 381, 350, 54);  // 设置位置和尺寸
                node2.setChildNodes(getNumberKeyBoardNodeList(node2, writeFrames, false));  // 设置子节点列表
                node2.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node2.setName("data");  // 设置节点名称
                writeFrames.add(node2);

                item.setChildNodes(writeFrames);  // 设置子节点列表
            }
            list.add(item);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建串行触发-ARINC429详情节点：Label/SDI/Data/SSM等配置
     */
    private static List<ExternalKeysNode> getTopTriggerSerialsM429DetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] m429s = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerSerialsARINC429);  // 从资源文件获取字符串数组
        int serialsNumber = parent.getIndex() == 9 ? 1 : 2;  // 定义整型变量
        for (int i = 0; i < m429s.length; i++) {  // 循环创建节点
            int x = 15 + 225 * (i % 8);  // 定义整型变量
            int y = i < 8 ? 200 + topSlipOffset : (i < 16 ? 270 + topSlipOffset : 181 + topSlipOffset);  // 定义整型变量
            ExternalKeysNode item = new ExternalKeysNode();  // 创建条目节点
            item.setParentNode(parent);  // 设置父节点
            item.setParentNodes(parents);  // 设置父节点列表
            item.setPlace(list.size(), x, y, 195, 60);  // 设置位置和尺寸
            item.setName(m429s[i]);  // 设置节点名称
            item.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
            if (i == 0) {  // 第一项特殊处理
                item.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M429 + serialsNumber));  // 设置当前选中项
            } else if (i == 2) {
                List<ExternalKeysNode> labels = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(labels.size(), 130, 381, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, labels, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                labels.add(node);

                item.setChildNodes(labels);  // 设置子节点列表
            } else if (i == 3) {
                List<ExternalKeysNode> sdis = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(sdis.size(), 130, 381, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, sdis, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                sdis.add(node);

                item.setChildNodes(sdis);  // 设置子节点列表
            } else if (i == 4) {
                List<ExternalKeysNode> datas = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(datas.size(), 130, 381, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, datas, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                datas.add(node);

                item.setChildNodes(datas);  // 设置子节点列表
            } else if (i == 5) {
                List<ExternalKeysNode> ssms = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(ssms.size(), 130, 381, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, ssms, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                ssms.add(node);

                item.setChildNodes(ssms);  // 设置子节点列表
            } else if (i == 6) {
                List<ExternalKeysNode> labelSdis = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(labelSdis.size(), 130, 381, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, labelSdis, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node.setName("label");  // 设置节点名称
                labelSdis.add(node);
                ExternalKeysNode node2 = new ExternalKeysNode();  // 创建第二个节点节点
                node2.setParentNode(item);  // 设置父节点
                node2.setParentNodes(list);  // 设置父节点列表
                node2.setPlace(labelSdis.size(), 620, 381, 350, 54);  // 设置位置和尺寸
                node2.setChildNodes(getNumberKeyBoardNodeList(node2, labelSdis, false));  // 设置子节点列表
                node2.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node2.setName("sdi");  // 设置节点名称
                labelSdis.add(node2);

                item.setChildNodes(labelSdis);  // 设置子节点列表
            } else if (i == 7) {
                List<ExternalKeysNode> labelDatas = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(labelDatas.size(), 130, 381, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, labelDatas, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node.setName("label");  // 设置节点名称
                labelDatas.add(node);
                ExternalKeysNode node2 = new ExternalKeysNode();  // 创建第二个节点节点
                node2.setParentNode(item);  // 设置父节点
                node2.setParentNodes(list);  // 设置父节点列表
                node2.setPlace(labelDatas.size(), 620, 381, 350, 54);  // 设置位置和尺寸
                node2.setChildNodes(getNumberKeyBoardNodeList(node2, labelDatas, false));  // 设置子节点列表
                node2.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node2.setName("data");  // 设置节点名称
                labelDatas.add(node2);

                item.setChildNodes(labelDatas);  // 设置子节点列表
            } else if (i == 8) {
                List<ExternalKeysNode> labelSsms = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(labelSsms.size(), 130, 381, 350, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, labelSsms, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node.setName("label");  // 设置节点名称
                labelSsms.add(node);
                ExternalKeysNode node2 = new ExternalKeysNode();  // 创建第二个节点节点
                node2.setParentNode(item);  // 设置父节点
                node2.setParentNodes(list);  // 设置父节点列表
                node2.setPlace(labelSsms.size(), 620, 381, 350, 54);  // 设置位置和尺寸
                node2.setChildNodes(getNumberKeyBoardNodeList(node2, labelSsms, false));  // 设置子节点列表
                node2.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node2.setName("ssm");  // 设置节点名称
                labelSsms.add(node2);

                item.setChildNodes(labelSsms);  // 设置子节点列表
            }
            list.add(item);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建串行触发-1553B详情节点：命令字/RT地址/数据字
     */
    private static List<ExternalKeysNode> getTopTriggerSerialsM1553bDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] m1553bs = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerSerials1553B);  // 从资源文件获取字符串数组
        int serialsNumber = parent.getIndex() == 9 ? 1 : 2;  // 定义整型变量
        for (int i = 0; i < 8; i++) {  // 循环创建节点
            int x = 15 + 225 * (i % 8);  // 定义整型变量
            int y = i < 8 ? 200 + topSlipOffset : (i < 16 ? 167 + topSlipOffset : 181 + topSlipOffset);  // 定义整型变量
            ExternalKeysNode item = new ExternalKeysNode();  // 创建条目节点
            item.setParentNode(parent);  // 设置父节点
            item.setParentNodes(parents);  // 设置父节点列表
            item.setPlace(list.size(), x, y, 195, 60);  // 设置位置和尺寸
            item.setName(m1553bs[i]);  // 设置节点名称
            item.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
            if (i == 0) {  // 第一项特殊处理
                item.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_M1553B + serialsNumber));  // 设置当前选中项
            } else if (i == 2) {
                List<ExternalKeysNode> csWords = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(csWords.size(), 160, 311, 400, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, csWords, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                csWords.add(node);

                item.setChildNodes(csWords);  // 设置子节点列表
            } else if (i == 3) {
                List<ExternalKeysNode> rtAddrs = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(rtAddrs.size(), 160, 311, 400, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, rtAddrs, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                rtAddrs.add(node);

                item.setChildNodes(rtAddrs);  // 设置子节点列表
            } else if (i == 5) {
                List<ExternalKeysNode> dataWords = new ArrayList<>();  // 创建节点列表
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setParentNode(item);  // 设置父节点
                node.setParentNodes(list);  // 设置父节点列表
                node.setPlace(dataWords.size(), 160, 311, 400, 54);  // 设置位置和尺寸
                node.setChildNodes(getNumberKeyBoardNodeList(node, dataWords, false));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                dataWords.add(node);

                item.setChildNodes(dataWords);  // 设置子节点列表
            }
            list.add(item);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }
    //endregion

    //region TopAuto
    /**
     * 构建Auto菜单的二级子菜单节点：AutoSet(自动设置)/AutoRange(自动量程)
     */
    private static List<ExternalKeysNode> getTopAutoDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] autos = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.auto);  // 从资源文件获取字符串数组

        ExternalKeysNode autoSet = new ExternalKeysNode();  // 创建自动设置节点
        autoSet.setParentNode(parent);  // 设置父节点
        autoSet.setParentNodes(parents);  // 设置父节点列表
        autoSet.setPlace(list.size(), 0, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        autoSet.setName(autos[0]);  // 设置节点名称
        autoSet.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_AUTO));  // 设置当前选中项
        autoSet.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        autoSet.setChildNodes(getTopAutoSetDetailNodeList(autoSet, list, topSlipOffset));  // 设置子节点列表
        list.add(autoSet);  // 添加节点到列表

        ExternalKeysNode autoRange = new ExternalKeysNode();  // 创建自动量程节点
        autoRange.setParentNode(parent);  // 设置父节点
        autoRange.setParentNodes(parents);  // 设置父节点列表
        autoRange.setPlace(list.size(), 145, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        autoRange.setName(autos[1]);  // 设置节点名称
        autoRange.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        autoRange.setChildNodes(getTopAutoRangeDetailNodeList(autoRange, list, topSlipOffset));  // 设置子节点列表
        list.add(autoRange);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Auto-Set自动设置节点：通道开启/电平/触发源
     */
    private static List<ExternalKeysNode> getTopAutoSetDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] opens = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.autoSettingOpenChannel);  // 从资源文件获取字符串数组
        String[] levels = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.autoSettingLevelSelect);  // 从资源文件获取字符串数组
        String[] sources = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.autoSettingTriggerSource);  // 从资源文件获取字符串数组
        ExternalKeysNode openChannel = new ExternalKeysNode();  // 创建通道开启节点
        openChannel.setPlace(list.size(), 181, 205 + topSlipOffset, 72, 36);  // 设置位置和尺寸
        openChannel.setName("TopAutoSetOpenChannel");  // 设置节点名称
        openChannel.setParentNode(parent);  // 设置父节点
        openChannel.setParentNodes(parents);  // 设置父节点列表
        list.add(openChannel);  // 添加节点到列表
        ExternalKeysNode levelDetail = new ExternalKeysNode();  // 创建电平详情节点
        levelDetail.setPlace(list.size(), 489, 200 + topSlipOffset, 120, 55);  // 设置位置和尺寸
        levelDetail.setName("TopAutoSet:levelDetail");  // 设置节点名称
        levelDetail.setParentNode(parent);  // 设置父节点
        levelDetail.setParentNodes(parents);  // 设置父节点列表
        levelDetail.setChildNodes(getTextKeyBoardNodeList(levelDetail, list));  // 设置子节点列表
        levelDetail.setDialog(ExternalKeysNode.DIALOG_TEXTKEYBOARD);  // 关联弹窗:文本键盘
        list.add(levelDetail);  // 添加节点到列表
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode levelSelect = new ExternalKeysNode();  // 创建电平选择节点
            levelSelect.setPlace(list.size(), 625 + i * 120, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            levelSelect.setName("TopAutoSet" + levels[i]);  // 设置节点名称
            levelSelect.setParentNode(parent);  // 设置父节点
            levelSelect.setParentNodes(parents);  // 设置父节点列表
            list.add(levelSelect);  // 添加节点到列表
        }
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setPlace(list.size(), 1141 + i * 120, 195 + topSlipOffset, 120, 60);  // 设置位置和尺寸
            source.setName("TopAutoSet" + sources[i]);  // 设置节点名称
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            list.add(source);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Auto-Range自动量程节点：范围/垂直/水平/电平
     */
    private static List<ExternalKeysNode> getTopAutoRangeDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] selects = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.autoRangeSelect);  // 从资源文件获取字符串数组
        ExternalKeysNode range = new ExternalKeysNode();  // 创建范围节点
        range.setParentNode(parent);  // 设置父节点
        range.setParentNodes(parents);  // 设置父节点列表
        range.setPlace(list.size(), 151, 209 + topSlipOffset, 72, 36);  // 设置位置和尺寸
        range.setName("autoRange");  // 设置节点名称
        list.add(range);  // 添加节点到列表
        ExternalKeysNode vertical = new ExternalKeysNode();  // 创建垂直节点
        vertical.setParentNode(parent);  // 设置父节点
        vertical.setParentNodes(parents);  // 设置父节点列表
        vertical.setPlace(list.size(), 459, 209 + topSlipOffset, 72, 36);  // 设置位置和尺寸
        vertical.setName("autoRangeVertical");  // 设置节点名称
        list.add(vertical);  // 添加节点到列表
        ExternalKeysNode horizontal = new ExternalKeysNode();  // 创建水平节点
        horizontal.setParentNode(parent);  // 设置父节点
        horizontal.setParentNodes(parents);  // 设置父节点列表
        horizontal.setPlace(list.size(), 767, 205 + topSlipOffset, 72, 36);  // 设置位置和尺寸
        horizontal.setName("autoRangeHorizontal");  // 设置节点名称
        list.add(horizontal);  // 添加节点到列表
        ExternalKeysNode level = new ExternalKeysNode();  // 创建电平节点
        level.setParentNode(parent);  // 设置父节点
        level.setParentNodes(parents);  // 设置父节点列表
        level.setPlace(list.size(), 1075, 205 + topSlipOffset, 72, 36);  // 设置位置和尺寸
        level.setName("autoRangeLevel");  // 设置节点名称
        list.add(level);  // 添加节点到列表
        return list;  // 返回节点列表
    }
    //endregion

    //region TopFrequency
    /**
     * 构建Frequency频率计节点：频率计通道列表
     */
    private static List<ExternalKeysNode> getTopFrequencyDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels1 = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.frequencymeter);  // 从资源文件获取字符串数组
        String[] channels2 = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] channels = StrUtil.add(channels1, channels2);  // 合并两个字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode channel = new ExternalKeysNode();  // 创建通道节点
            channel.setParentNode(parent);  // 设置父节点
            channel.setParentNodes(parents);  // 设置父节点列表
            channel.setPlace(list.size(), 152 + i * 70, 110 + topSlipOffset, 70, 35);  // 设置位置和尺寸
            channel.setName(channels[i]);  // 设置节点名称
            list.add(channel);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }
    //endregion

    //region TopUserSet
    /**
     * 构建UserSet用户设置节点：出厂复位/自校准/无线键盘/AuxOut/参考时基
     */
    private static List<ExternalKeysNode> getTopUserSetDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] sets = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.setting);  // 从资源文件获取字符串数组
        String[] setsVisible = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.settingVisible);  // 从资源文件获取字符串数组

//        ExternalKeysNode depth = new ExternalKeysNode();
//        depth.setParentNode(parent);
//        depth.setParentNodes(parents);
//        depth.setPlace(list.size(), 1, 84, 90, 42);
//        depth.setName(sets[0]);
//        depth.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET));
//        depth.setVisible(Boolean.parseBoolean(setsVisible[0]));
//        depth.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);
//        depth.setChildNodes(getTopUserSetDepthDetailNodeList(depth, list));
//        list.add(depth);

        ExternalKeysNode factoryReset = new ExternalKeysNode();  // 创建出厂复位节点
        factoryReset.setParentNode(parent);  // 设置父节点
        factoryReset.setParentNodes(parents);  // 设置父节点列表
        factoryReset.setPlace(list.size(), 0, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        factoryReset.setName(sets[0]);  // 设置节点名称
        factoryReset.setCurListSelect(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET));  // 设置当前选中项
        factoryReset.setVisible(Boolean.parseBoolean(setsVisible[0]));  // 设置可见性
        factoryReset.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        factoryReset.setChildNodes(getTopUserSetFactoryResetDetailNodeList(factoryReset, list, topSlipOffset));  // 设置子节点列表
        list.add(factoryReset);  // 添加节点到列表

        ExternalKeysNode selfAdjust = new ExternalKeysNode();  // 创建自校准节点
        selfAdjust.setParentNode(parent);  // 设置父节点
        selfAdjust.setParentNodes(parents);  // 设置父节点列表
        selfAdjust.setPlace(list.size(), 145, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        selfAdjust.setName(sets[1]);  // 设置节点名称
        selfAdjust.setVisible(Boolean.parseBoolean(setsVisible[1]));  // 设置可见性
        selfAdjust.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        selfAdjust.setChildNodes(getTopUserSetSelfAdjustDetailNodeList(selfAdjust, list, topSlipOffset));  // 设置子节点列表
        list.add(selfAdjust);  // 添加节点到列表

        ExternalKeysNode saveRecovery = new ExternalKeysNode();  // 创建保存/恢复节点
        saveRecovery.setParentNode(parent);  // 设置父节点
        saveRecovery.setParentNodes(parents);  // 设置父节点列表
        saveRecovery.setPlace(list.size(), 290, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        saveRecovery.setName(sets[2]);  // 设置节点名称
        saveRecovery.setVisible(false);  // 设为不可见
        saveRecovery.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        saveRecovery.setChildNodes(getTopUserSetWrielessKeyboardNodeList(saveRecovery, list, topSlipOffset));  // 设置子节点列表
        list.add(saveRecovery);  // 添加节点到列表
//        ExternalKeysNode capture = new ExternalKeysNode();
//        capture.setParentNode(parent);
//        capture.setParentNodes(parents);
//        capture.setPlace(list.size(), 91, 84, 90, 42);
//        capture.setName(sets[4]);
//        capture.setVisible(Boolean.parseBoolean(setsVisible[4]));
//        capture.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);
////        capture.setChildNodes(getTopSavePictureDetailNodeList(capture, list));
//        list.add(capture);

        ExternalKeysNode auxOut = new ExternalKeysNode();  // 创建辅助输出节点
        auxOut.setParentNode(parent);  // 设置父节点
        auxOut.setParentNodes(parents);  // 设置父节点列表
        int startX = 290;  // 定义整型变量
        auxOut.setPlace(list.size(), startX, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        auxOut.setName(sets[3]);  // 设置节点名称
        auxOut.setVisible(Boolean.parseBoolean(setsVisible[3]));  // 设置可见性
        auxOut.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        auxOut.setChildNodes(getTopUserSetAuxOutDetailNodeList(auxOut, list, topSlipOffset));  // 设置子节点列表
        list.add(auxOut);  // 添加节点到列表

        ExternalKeysNode refTimeBase = new ExternalKeysNode();  // 创建参考时基节点
        refTimeBase.setParentNode(parent);  // 设置父节点
        refTimeBase.setParentNodes(parents);  // 设置父节点列表
        refTimeBase.setPlace(list.size(), 435, 110 + topSlipOffset, 145, 70);  // 设置位置和尺寸
        refTimeBase.setName(sets[4]);  // 设置节点名称
        refTimeBase.setVisible(Boolean.parseBoolean(setsVisible[4]));  // 设置可见性
        refTimeBase.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        refTimeBase.setChildNodes(getTopUserSetRefTimeBaseDetailNodeList(refTimeBase, list));  // 设置子节点列表
        list.add(refTimeBase);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建用户设置-参考时基节点：时基选项列表
     */
    private static List<ExternalKeysNode> getTopUserSetRefTimeBaseDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] userSetRefTimeBase = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.userSetRefTimeBase);  // 从资源文件获取字符串数组
        for (int j = 0; j < userSetRefTimeBase.length; j++) {  // 遍历选项创建节点
            ExternalKeysNode timeBase = new ExternalKeysNode();  // 创建时基节点
            timeBase.setParentNode(parent);  // 设置父节点
            timeBase.setParentNodes(parents);  // 设置父节点列表
            int x = 161 + 130 * j;  // 定义整型变量
            timeBase.setPlace(list.size(), x, 231, 130, 60);  // 设置位置和尺寸
            timeBase.setName("RefTimeBase:" + userSetRefTimeBase[j]);  // 设置节点名称
            list.add(timeBase);  // 添加节点到列表
        }

//        ExternalKeysNode timePos = new ExternalKeysNode();
//        timePos.setParentNode(parent);
//        timePos.setParentNodes(parents);
//        timePos.setPlace(list.size(), 636, 231, 120, 60);
//        timePos.setName("timePos");
//        timePos.setChildNodes(getFloatKeyBoardNodeList(timePos, list));
//        timePos.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);
//        list.add(timePos);

        return list;  // 返回节点列表
    }


    /**
     * 构建用户设置-AuxOut辅助输出节点：Trigger/Clock/输入阻抗
     */
    private static List<ExternalKeysNode> getTopUserSetAuxOutDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] auxOUts = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.userSetAuxOut);  // 从资源文件获取字符串数组
        String[] impedType = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.channelImped);  // 从资源文件获取字符串数组
        for (int j = 0; j < auxOUts.length; j++) {  // 循环创建节点
            ExternalKeysNode trigger = new ExternalKeysNode();  // 创建触发节点
            trigger.setParentNode(parent);  // 设置父节点
            trigger.setParentNodes(parents);  // 设置父节点列表
            int x = 161 + 120 * j;  // 定义整型变量
            int y = 195 + topSlipOffset;  // 定义整型变量
            trigger.setPlace(list.size(), x, y, 120, 60);  // 设置位置和尺寸
            trigger.setName("Trigger:" + auxOUts[j]);  // 设置节点名称
            list.add(trigger);  // 添加节点到列表
        }

        for (int j = 0; j < auxOUts.length; j++) {  // 循环创建节点
            ExternalKeysNode clock = new ExternalKeysNode();  // 创建节点节点
            clock.setParentNode(parent);  // 设置父节点
            clock.setParentNodes(parents);  // 设置父节点列表
            int x = 657 + 120 * j;  // 定义整型变量
            int y = 195 + topSlipOffset;  // 定义整型变量
            clock.setPlace(list.size(), x, y, 120, 60);  // 设置位置和尺寸
            clock.setName("Clock:" + auxOUts[j]);  // 设置节点名称
            list.add(clock);  // 添加节点到列表
        }
        if (!HardwareProduct.isMHO68V1()) {  // 判断是否MHO68V1型号
            for (int j = 0; j < impedType.length; j++) {  // 循环创建节点
                ExternalKeysNode imped = new ExternalKeysNode();  // 创建输入阻抗节点
                imped.setParentNode(parent);  // 设置父节点
                imped.setParentNodes(parents);  // 设置父节点列表
                int x = 1153 + 120 * j;  // 定义整型变量
                int y = 195 + topSlipOffset;  // 定义整型变量
                imped.setPlace(list.size(), x, y, 120, 60);  // 设置位置和尺寸
                imped.setName("InputImped:" + impedType[j]);  // 设置节点名称
                list.add(imped);  // 添加节点到列表
            }
        }

        return list;  // 返回节点列表
    }

    /**
     * 构建用户设置-存储深度节点：深度选项列表
     */
    private static List<ExternalKeysNode> getTopUserSetDepthDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        int depthNumber = MemDepthFactory.getMemDepth().getMemDepthItemName().size();  // 获取存储深度信息
        for (int i = 0; i < depthNumber; i++) {  // 遍历选项创建节点
            ExternalKeysNode depth = new ExternalKeysNode();  // 创建深度节点
            depth.setParentNode(parent);  // 设置父节点
            depth.setParentNodes(parents);  // 设置父节点列表
            depth.setPlace(list.size(), 132 + 100 * i, 137, 100, 35);  // 设置位置和尺寸
            depth.setName("depth:" + i);  // 设置节点名称
            list.add(depth);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建用户设置-出厂复位节点：确认按钮
     */
    private static List<ExternalKeysNode> getTopUserSetFactoryResetDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode sure = new ExternalKeysNode();  // 创建确认节点
        sure.setParentNode(parent);  // 设置父节点
        sure.setParentNodes(parents);  // 设置父节点列表
        sure.setPlace(list.size(), 550, 205 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        sure.setName("sure");  // 设置节点名称
        list.add(sure);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建用户设置-自校准节点：确认按钮
     */
    private static List<ExternalKeysNode> getTopUserSetSelfAdjustDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode sure = new ExternalKeysNode();  // 创建确认节点
        sure.setParentNode(parent);  // 设置父节点
        sure.setParentNodes(parents);  // 设置父节点列表
        sure.setPlace(list.size(), 550, 205 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        sure.setName("sure");  // 设置节点名称
        list.add(sure);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建用户设置-无线键盘节点：确认按钮
     */
    private static List<ExternalKeysNode> getTopUserSetWrielessKeyboardNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode sure = new ExternalKeysNode();  // 创建确认节点
        sure.setParentNode(parent);  // 设置父节点
        sure.setParentNodes(parents);  // 设置父节点列表
        sure.setPlace(list.size(), 550, 205 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        sure.setName("sure");  // 设置节点名称
        list.add(sure);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    //endregion


    //region Measure延时
    /**
     * 构建Measure-TValue时间值对话框节点：垂直值/边沿出现次数/光标索引/确认
     */
    private static List<ExternalKeysNode> getMeasureTValueNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode verticalValue = new ExternalKeysNode();  // 创建垂直值节点
        verticalValue.setParentNode(parent);  // 设置父节点
        verticalValue.setParentNodes(parents);  // 设置父节点列表
        verticalValue.setPlace(list.size(), 852, 395, 230, 54);  // 设置位置和尺寸
        verticalValue.setName("TValueVertical");  // 设置节点名称
        verticalValue.setChildNodes(getFloatKeyBoardNodeList(verticalValue, list));  // 设置子节点列表
        verticalValue.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(verticalValue);  // 添加节点到列表

        ExternalKeysNode edgeOccurence = new ExternalKeysNode();  // 创建边沿出现次数节点
        edgeOccurence.setParentNode(parent);  // 设置父节点
        edgeOccurence.setParentNodes(parents);  // 设置父节点列表
        edgeOccurence.setPlace(list.size(), 852, 469, 230, 54);  // 设置位置和尺寸
        edgeOccurence.setName("TValueEdgeOccurence");  // 设置节点名称
        edgeOccurence.setChildNodes(getIntKeyBoardNodeList(edgeOccurence, list));  // 设置子节点列表
        edgeOccurence.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(edgeOccurence);  // 添加节点到列表

        for (int i = 0; i < 3; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 852 + 120 * i, 543, 120, 60);  // 设置位置和尺寸
            node.setName("TValueCursorIndex");  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        ExternalKeysNode sure = new ExternalKeysNode();  // 创建确认节点
        sure.setParentNode(parent);  // 设置父节点
        sure.setParentNodes(parents);  // 设置父节点列表
        sure.setPlace(list.size(), 900, 618, 120, 60);  // 设置位置和尺寸
        sure.setName("TvalueSure");  // 设置节点名称
        sure.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
        list.add(sure);  // 添加节点到列表

        return list;  // 返回节点列表
    }



    //region Measure延时
    /**
     * 构建Measure-Delay延时对话框节点：四行通道选择/确认
     */
    private static List<ExternalKeysNode> getMeasureDelayNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        for (int i = 0; i < 4; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 550 + 120 * i, 315, 120, 60);  // 设置位置和尺寸
            node.setName("DelayLine0Child");  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        int channels = GlobalVar.get().getChannelsCount();  // 获取通道数量
        for (int i = 0; i < channels; i++) {  // 遍历通道创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 550 + 120 * i, 395, 120, 60);  // 设置位置和尺寸
            node.setName("DelayLine1Child");  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < ChannelFactory.MATH_CNT; i++) {  // 遍历通道创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 550 + 120 * i, 475, 120, 60);  // 设置位置和尺寸
            node.setName("DelayLine2Child");  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 4; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 550 + 120 * i, 555, 120, 60);  // 设置位置和尺寸
            node.setName("DelayLine3Child");  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        ExternalKeysNode sure = new ExternalKeysNode();  // 创建确认节点
        sure.setParentNode(parent);  // 设置父节点
        sure.setParentNodes(parents);  // 设置父节点列表
        sure.setPlace(list.size(), 900, 630, 120, 60);  // 设置位置和尺寸
        sure.setName("DelaySure");  // 设置节点名称
        sure.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
        list.add(sure);  // 添加节点到列表

        return list;  // 返回节点列表
    }
    //endregion

    //region Measure相位
    /**
     * 构建Measure-Phase相位对话框节点：两行通道选择/确认
     */
    private static List<ExternalKeysNode> getMeasurePhaseNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        int channels = GlobalVar.get().getChannelsCount();  // 获取通道数量
        for (int i = 0; i < channels; i++) {  // 遍历通道创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 480 + 120 * i, 395, 120, 60);  // 设置位置和尺寸
            node.setName("PhaseLine0Child");  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < ChannelFactory.MATH_CNT; i++) {  // 遍历通道创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 480 + 120 * i, 475, 120, 60);  // 设置位置和尺寸
            node.setName("PhaseLine1Child");  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        ExternalKeysNode sure = new ExternalKeysNode();  // 创建确认节点
        sure.setParentNode(parent);  // 设置父节点
        sure.setParentNodes(parents);  // 设置父节点列表
        sure.setPlace(list.size(), channels == 4 ? 710 : 900, channels == 4 ? 470 : 550, 120, 60);  // 设置位置和尺寸
        sure.setName("PhaseSure");  // 设置节点名称
        sure.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
        list.add(sure);  // 添加节点到列表

        return list;  // 返回节点列表
    }
    //endregion

    //region TopDialogCount
    /**
     * 构建计数弹窗节点：左右选择/进度条/加减按钮
     */
    private static List<ExternalKeysNode> getTopDialogCountDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode x1 = new ExternalKeysNode();  // 创建左选择1节点
        x1.setParentNode(parent);  // 设置父节点
        x1.setParentNodes(parents);  // 设置父节点列表
        x1.setPlace(list.size(), 1036, 617, 120, 60);  // 设置位置和尺寸
        x1.setName("line1Left");  // 设置节点名称
        list.add(x1);  // 添加节点到列表
        ExternalKeysNode x100 = new ExternalKeysNode();  // 创建右选择1节点
        x100.setParentNode(parent);  // 设置父节点
        x100.setParentNodes(parents);  // 设置父节点列表
        x100.setPlace(list.size(), 1172, 617, 120, 60);  // 设置位置和尺寸
        x100.setName("line1Right");  // 设置节点名称
        list.add(x100);  // 添加节点到列表
        ExternalKeysNode sub = new ExternalKeysNode();  // 创建左选择2节点
        sub.setParentNode(parent);  // 设置父节点
        sub.setParentNodes(parents);  // 设置父节点列表
        sub.setPlace(list.size(), 508, 693, 120, 60);  // 设置位置和尺寸
        sub.setName("line2Left");  // 设置节点名称
        list.add(sub);  // 添加节点到列表
        ExternalKeysNode progress = new ExternalKeysNode();  // 创建进度条节点
        progress.setParentNode(parent);  // 设置父节点
        progress.setParentNodes(parents);  // 设置父节点列表
        progress.setPlace(list.size(), 652, 693, 360, 60);  // 设置位置和尺寸
        progress.setName("progress");  // 设置节点名称
        progress.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        progress.setChildNodes(getTopDialogCountProgressDetailNodeList(progress, list));  // 设置子节点列表
        list.add(progress);  // 添加节点到列表

        ExternalKeysNode add = new ExternalKeysNode();  // 创建右选择2节点
        add.setParentNode(parent);  // 设置父节点
        add.setParentNodes(parents);  // 设置父节点列表
        add.setPlace(list.size(), 1036, 693, 120, 60);  // 设置位置和尺寸
        add.setName("line2Right");  // 设置节点名称
        list.add(add);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建计数弹窗进度条节点
     */
    private static List<ExternalKeysNode> getTopDialogCountProgressDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode number = new ExternalKeysNode();  // 创建数字键节点
        number.setParentNode(parent);  // 设置父节点
        number.setParentNodes(parents);  // 设置父节点列表
        number.setPlace(list.size(), 652, 703, 360, 40);  // 设置位置和尺寸
        number.setName("progress");  // 设置节点名称
        number.setType(ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS);  // 类型:计数弹窗进度条
        list.add(number);  // 添加节点到列表
        return list;  // 返回节点列表
    }
    //endregion

    //region TopDialogScale
    /**
     * 构建刻度弹窗节点：大刻度/小刻度
     */
    private static List<ExternalKeysNode> getTopDialogScaleNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode largeScale = new ExternalKeysNode();  // 创建大刻度节点
        largeScale.setParentNode(parent);  // 设置父节点
        largeScale.setParentNodes(parents);  // 设置父节点列表
        largeScale.setPlace(list.size(), 60, 422, 584, 80);  // 设置位置和尺寸
        largeScale.setName("largeScale");  // 设置节点名称
        list.add(largeScale);  // 添加节点到列表
        ExternalKeysNode smallScale = new ExternalKeysNode();  // 创建小刻度节点
        smallScale.setParentNode(parent);  // 设置父节点
        smallScale.setParentNodes(parents);  // 设置父节点列表
        smallScale.setPlace(list.size(), 240, 352, 400, 60);  // 设置位置和尺寸
        smallScale.setName("smallScale");  // 设置节点名称
        list.add(smallScale);  // 添加节点到列表
        return list;  // 返回节点列表
    }
    //endregion

    //region 拼音、字母全键盘
    /**
     * 构建文本输入全键盘节点：4行字母键+功能键(隐藏/数字/空格/符号/语言)
     */
    private static List<ExternalKeysNode> getTextKeyBoardNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        //全键盘第1行
        for (int i = 0; i < 11; i++) {  // 循环创建节点
            ExternalKeysNode key = new ExternalKeysNode();  // 创建按键节点
            key.setParentNode(parent);  // 设置父节点
            key.setParentNodes(parents);  // 设置父节点列表
            key.setPlace(list.size(), 15 + 173 * i, 840, 162, 80);  // 设置位置和尺寸
            if (i == 4) {  // Delay项特殊处理
                key.setW(162);  // 设置宽度
            } else if (i > 4) {
                key.setX(key.getX() - 1);  // 设置X坐标
            }
            key.setName("TextKeyLine0Child:" + i);  // 设置节点名称
            list.add(key);  // 添加节点到列表
        }

        {//全键盘第2行
            for (int i = 0; i < 9; i++) {  // 循环创建节点
                ExternalKeysNode key = new ExternalKeysNode();  // 创建按键节点
                key.setParentNode(parent);  // 设置父节点
                key.setParentNodes(parents);  // 设置父节点列表
                key.setPlace(list.size(), 101 + 173 * i, 930, 162, 80);  // 设置位置和尺寸
                key.setName("TextKeyLine1Child:" + i);  // 设置节点名称
                list.add(key);  // 添加节点到列表
            }
            ExternalKeysNode key = new ExternalKeysNode();  // 创建按键节点
            key.setParentNode(parent);  // 设置父节点
            key.setParentNodes(parents);  // 设置父节点列表
            key.setPlace(list.size(), 1656, 930, 249, 80);  // 设置位置和尺寸
            key.setName("TextKeyEnter");  // 设置节点名称
            key.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
            list.add(key);  // 添加节点到列表
        }

        //全键盘第3行
        for (int i = 0; i < 11; i++) {  // 循环创建节点
            ExternalKeysNode key = new ExternalKeysNode();  // 创建按键节点
            key.setParentNode(parent);  // 设置父节点
            key.setParentNodes(parents);  // 设置父节点列表
            key.setPlace(list.size(), 15 + 173 * i, 1020, 162, 80);  // 设置位置和尺寸
            if (i == 4) {  // Delay项特殊处理
                key.setW(162);  // 设置宽度
            } else if (i > 4) {
                key.setX(key.getX() - 1);  // 设置X坐标
            }
            key.setName("TextKeyLine2Child:" + i);  // 设置节点名称
            list.add(key);  // 添加节点到列表
        }

        {//全键盘第4行
            ExternalKeysNode hide = new ExternalKeysNode();  // 创建隐藏键节点
            hide.setParentNode(parent);  // 设置父节点
            hide.setParentNodes(parents);  // 设置父节点列表
            hide.setPlace(list.size(), 15, 1110, 249, 80);  // 设置位置和尺寸
            hide.setName("TextKeyHide");  // 设置节点名称
            list.add(hide);  // 添加节点到列表
            ExternalKeysNode number = new ExternalKeysNode();  // 创建数字键节点
            number.setParentNode(parent);  // 设置父节点
            number.setParentNodes(parents);  // 设置父节点列表
            number.setPlace(list.size(), 274, 1110, 249, 80);  // 设置位置和尺寸
            number.setName("TextKeyNumber");  // 设置节点名称
            list.add(number);  // 添加节点到列表
            ExternalKeysNode space = new ExternalKeysNode();  // 创建空格键节点
            space.setParentNode(parent);  // 设置父节点
            space.setParentNodes(parents);  // 设置父节点列表
            space.setPlace(list.size(), 533, 1110, 854, 80);  // 设置位置和尺寸
            space.setName("TextKeySpace");  // 设置节点名称
            list.add(space);  // 添加节点到列表
            ExternalKeysNode symbol = new ExternalKeysNode();  // 创建符号键节点
            symbol.setParentNode(parent);  // 设置父节点
            symbol.setParentNodes(parents);  // 设置父节点列表
            symbol.setPlace(list.size(), 1397, 1110, 249, 80);  // 设置位置和尺寸
            symbol.setName("TextKeySymbol");  // 设置节点名称
            list.add(symbol);  // 添加节点到列表
            ExternalKeysNode lang = new ExternalKeysNode();  // 创建语言键节点
            lang.setParentNode(parent);  // 设置父节点
            lang.setParentNodes(parents);  // 设置父节点列表
            lang.setPlace(list.size(), 1656, 1110, 249, 80);  // 设置位置和尺寸
            lang.setName("TextKeyLanguage");  // 设置节点名称
            list.add(lang);  // 添加节点到列表
        }

        //全键盘拼音预选行 计划使用b旋钮操作
//        ExternalKeysNode candidates = new ExternalKeysNode();
//        candidates.setParentNode(parent);
//        candidates.setParentNodes(parents);
//        candidates.setPlace(list.size(), 0, 205, 800, 29);
//        candidates.setName("candidates");
//        list.add(candidates);
        return list;  // 返回节点列表
    }
    //endregion

    //region 用于进制或波特率输入的数字小键盘
    /**
     * 构建数字输入小键盘节点(用于进制/波特率输入)：4行数字键+确认
     */
    private static List<ExternalKeysNode> getNumberKeyBoardNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, boolean isRight) {
        int tmp = isRight ? 0 : 63;  // 定义整型变量
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        for (int i = 0; i < 6; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 15 + 94 * i, 842, 80, 54);  // 设置位置和尺寸
            node.setName("NumberKeyLine0Child" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 6; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 15 + 94 * i, 842 + 68 * 1, 80, 54);  // 设置位置和尺寸
            node.setName("NumberKeyLine1Child" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 15 + 94 * i, 842 + 68 * 2, 80, 54);  // 设置位置和尺寸
            node.setName("NumberKeyLine2Child" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 15 + 94 * i, 842 + 68 * 3, 80, 54);  // 设置位置和尺寸
            node.setName("NumberKeyLine3Child" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        {
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 485, 978, 80, 122);  // 设置位置和尺寸
            node.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
            node.setName("Enter");  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        return list;  // 返回节点列表
    }
    //endregion


    /**
     * 构建整数输入键盘节点：4行数字键(部分隐藏)+确认
     */
    private static List<ExternalKeysNode> getIntKeyBoardNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 0, 86, 60);  // 设置位置和尺寸
            node.setName("NumberKeyLine0Child" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 1, 86, 60);  // 设置位置和尺寸
            node.setName("NumberKeyLine1Child" + i);  // 设置节点名称
            if (i == 3) {  // 第四项特殊处理
                node.setVisible(false);  // 设为不可见
            }
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 2, 86, 60);  // 设置位置和尺寸
            node.setName("NumberKeyLine2Child" + i);  // 设置节点名称
            if (i == 3) {  // 第四项特殊处理
                node.setVisible(false);  // 设为不可见
            }
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 3, 86, 59);  // 设置位置和尺寸
            if (i == 1 || i == 3) {  // 第二项特殊处理
                node.setVisible(false);  // 设为不可见
            }
            if (i == 4) {  // Delay项特殊处理
                node.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
                node.setName("Enter");  // 设置节点名称
            } else {  // 否则
                node.setName("NumberKeyLine3Child" + i);  // 设置节点名称
            }
            list.add(node);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }


    //region 用于输入带正负号的小数输入的数字小键盘
    /**
     * 构建浮点数输入键盘节点(带正负号)：4行数字键+确认
     */
    private static List<ExternalKeysNode> getFloatKeyBoardNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 0, 86, 60);  // 设置位置和尺寸
            node.setName("NumberKeyLine0Child" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 1, 86, 60);  // 设置位置和尺寸
            node.setName("NumberKeyLine1Child" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 2, 86, 60);  // 设置位置和尺寸
            node.setName("NumberKeyLine2Child" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 3, 86, 59);  // 设置位置和尺寸
            if (i == 4) {  // Delay项特殊处理
                node.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
                node.setName("Enter");  // 设置节点名称
            } else {  // 否则
                node.setName("NumberKeyLine3Child" + i);  // 设置节点名称
            }
            list.add(node);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建完整浮点数输入键盘节点(6列)：4行数字键+确认
     */
    private static List<ExternalKeysNode> getFullFloatKeyBoardNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < 6; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 0, 86, 60);  // 设置位置和尺寸
            node.setName("NumberKeyLine0Child" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 6; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 1, 86, 60);  // 设置位置和尺寸
            node.setName("NumberKeyLine1Child" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 6; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 2, 86, 60);  // 设置位置和尺寸
            node.setName("NumberKeyLine2Child" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 6; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 3, 86, 59);  // 设置位置和尺寸
            if (i == 5) {  // 第六项特殊处理
                node.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
                node.setName("Enter");  // 设置节点名称
            } else {  // 否则
                node.setName("NumberKeyLine3Child" + i);  // 设置节点名称
            }
            list.add(node);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }


    /**
     * 构建延时浮点数输入键盘节点：4行数字键+确认+单位切换
     */
    private static List<ExternalKeysNode> getDelayFloatKeyBoardNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode inPut = new ExternalKeysNode();  // 创建节点节点
        inPut.setParentNode(parent);  // 设置父节点
        inPut.setParentNodes(parents);  // 设置父节点列表
        inPut.setPlace(list.size(), 17, 723, 494, 72);  // 设置位置和尺寸
        inPut.setChildNodes(getInputNumberNodeList(inPut, list));  // 设置子节点列表
        inPut.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        inPut.setName("llInput");  // 设置节点名称
        list.add(inPut);  // 添加节点到列表

        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 0, 86, 60);  // 设置位置和尺寸
            node.setName("NumberKeyLine0Child" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 1, 86, 60);  // 设置位置和尺寸
            node.setName("NumberKeyLine1Child" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 2, 86, 60);  // 设置位置和尺寸
            node.setName("NumberKeyLine2Child" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 17 + 102 * i, 811 + 76 * 3, 86, 59);  // 设置位置和尺寸
            if (i == 4) {  // Delay项特殊处理
                node.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
                node.setName("Enter");  // 设置节点名称
            } else {  // 否则
                node.setName("NumberKeyLine3Child" + i);  // 设置节点名称
            }
            list.add(node);  // 添加节点到列表
        }

        ExternalKeysNode progress = new ExternalKeysNode();  // 创建进度条节点
        progress.setParentNode(parent);  // 设置父节点
        progress.setParentNodes(parents);  // 设置父节点列表
        progress.setPlace(list.size(), 17, 1056, 494, 60);  // 设置位置和尺寸
        progress.setName("progress");  // 设置节点名称
        progress.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        progress.setVisible(false);//由于增加了旋钮按位调整数值功能，所以不需要显示滑动条了。
        progress.setChildNodes(getTopDialogFloatKeyBoardProgressDetailNodeList(progress, list));  // 设置子节点列表
        list.add(progress);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建数值输入节点
     */
    private static List<ExternalKeysNode> getInputNumberNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode number = new ExternalKeysNode();  // 创建数字键节点
        number.setParentNode(parent);  // 设置父节点
        number.setParentNodes(parents);  // 设置父节点列表
        number.setPlace(list.size(), 17, 723, 200, 72);  // 设置位置和尺寸
        number.setName("delayUnit");  // 设置节点名称
        number.setType(ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT);  // 类型:TYPE_RIGHT_SLIP_CH_DELAY_UNIT
        list.add(number);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建浮点数键盘进度条节点
     */
    private static List<ExternalKeysNode> getTopDialogFloatKeyBoardProgressDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode number = new ExternalKeysNode();  // 创建数字键节点
        number.setParentNode(parent);  // 设置父节点
        number.setParentNodes(parents);  // 设置父节点列表
        number.setPlace(list.size(), 17, 1027, 494, 40);  // 设置位置和尺寸
        number.setName("progress");  // 设置节点名称
        number.setType(ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY);  // 类型:TYPE_RIGHT_SLIP_CH_DELAY
        list.add(number);  // 添加节点到列表
        return list;  // 返回节点列表
    }
    //endregion

    //region 高级数学中，用于公式的输入的键盘
    /**
     * 构建公式输入键盘节点：4行运算符/数字键+功能键
     */
    private static List<ExternalKeysNode> getFormulaKeyBoardNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode leftSelection = new ExternalKeysNode();  // 创建节点节点
        leftSelection.setParentNode(parent);  // 设置父节点
        leftSelection.setParentNodes(parents);  // 设置父节点列表
        leftSelection.setPlace(list.size(), 861, 610, 80, 54);  // 设置位置和尺寸
        leftSelection.setName("leftSelection");  // 设置节点名称
        list.add(leftSelection);  // 添加节点到列表
        ExternalKeysNode rightSelection = new ExternalKeysNode();  // 创建节点节点
        rightSelection.setParentNode(parent);  // 设置父节点
        rightSelection.setParentNodes(parents);  // 设置父节点列表
        rightSelection.setPlace(list.size(), 955, 610, 80, 54);  // 设置位置和尺寸
        rightSelection.setName("rightSelection");  // 设置节点名称
        list.add(rightSelection);  // 添加节点到列表

        for (int row = 0; row < 3; row++) {  // 循环创建节点
            for (int column = 0; column < 5; column++) {  // 循环创建节点
                ExternalKeysNode keysNode = new ExternalKeysNode();  // 创建节点节点
                keysNode.setParentNode(parent);  // 设置父节点
                keysNode.setParentNodes(parents);  // 设置父节点列表
                keysNode.setPlace(list.size(), 15 + 94 * column, 706 + 68 * row, 80, 54);  // 设置位置和尺寸
                keysNode.setName("FormulaKeyBoard,LeftTop:" + row + "," + column);  // 设置节点名称
                list.add(keysNode);  // 添加节点到列表
            }
        }
        for (int row = 0; row < 3; row++) {  // 循环创建节点
            for (int column = 0; column < 5; column++) {  // 循环创建节点
                ExternalKeysNode keysNode = new ExternalKeysNode();  // 创建节点节点
                keysNode.setParentNode(parent);  // 设置父节点
                keysNode.setParentNodes(parents);  // 设置父节点列表
                keysNode.setPlace(list.size(), 15 + 94 * column, 910 + 68 * row, 80, 54);  // 设置位置和尺寸
                keysNode.setName("FormulaKeyBoard,LeftBottom:" + row + "," + column);  // 设置节点名称
                list.add(keysNode);  // 添加节点到列表
            }
        }
        for (int row = 0; row < 2; row++) {  // 循环创建节点
            for (int column = 0; column < 6; column++) {  // 循环创建节点
                ExternalKeysNode keysNode = new ExternalKeysNode();  // 创建节点节点
                keysNode.setParentNode(parent);  // 设置父节点
                keysNode.setParentNodes(parents);  // 设置父节点列表
                keysNode.setPlace(list.size(), 485 + 94 * column, 706 + 68 * row, 80, 54);  // 设置位置和尺寸
                keysNode.setName("FormulaKeyBoard,RightTop:" + row + "," + column);  // 设置节点名称

                if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) {  // 条件判断
                    if (row == 0 && (column == 2 || column == 3)) {  // 条件判断
                        keysNode.setVisible(false);  // 设为不可见
                    }
                    if (row == 1 && (column == 0 || column == 1 || column == 2 || column == 3)) {  // 条件判断
                        keysNode.setVisible(false);  // 设为不可见
                    }
                }

                if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) {  // 判断是否为4通道型号
                    if (row == 1 && (column == 0 || column == 1 || column == 2 || column == 3)) {  // 条件判断
                        keysNode.setVisible(false);  // 设为不可见
                    }
                }

                if (row == 0 && column == 4) { //空占位
                    keysNode.setVisible(false);  // 设为不可见
                }

//                if (row == 0 && column == 4) {
//                    keysNode.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);
//                    keysNode.setName(keysNode.getName() + ",enter");
//                } else if (row == 1 && column == 0) {
//                    if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) {
//                        keysNode.setVisible(false);
//                    }
//                } else if (row == 1 && column == 1) {
//                    if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) {
//                        keysNode.setVisible(false);
//                    }
//                }

                list.add(keysNode);  // 添加节点到列表
            }
        }
        for (int row = 0; row < 4; row++) {  // 循环创建节点
            for (int column = 0; column < 6; column++) {  // 循环创建节点
                ExternalKeysNode keysNode = new ExternalKeysNode();  // 创建节点节点
                keysNode.setParentNode(parent);  // 设置父节点
                keysNode.setParentNodes(parents);  // 设置父节点列表
                keysNode.setPlace(list.size(), 485 + 94 * column, 842 + 68 * row, 80, 54);  // 设置位置和尺寸
                keysNode.setName("FormulaKeyBoard,RightBottom:" + row + "," + column);  // 设置节点名称

                if (row == 2 && column == 5) {  // 条件判断
                    keysNode.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
                    keysNode.setName(keysNode.getName() + ",enter");  // 设置节点名称
                }
                list.add(keysNode);  // 添加节点到列表
            }
        }
        return list;  // 返回节点列表
    }
    //endregion

    //region 高级数学中，用于选择数字的dialog
    /**
     * 构建NumberPicker数值选择器节点：上下箭头+数字行+确认
     */
    private static List<ExternalKeysNode> getNumberPickerPosNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
        node.setParentNode(parent);  // 设置父节点
        node.setParentNodes(parents);  // 设置父节点列表
        node.setPlace(list.size(), 210, 570, 72, 72);  // 设置位置和尺寸
        node.setName("NumberPicker:" + list.size());  // 设置节点名称
        node.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(node);  // 添加节点到列表
        ExternalKeysNode node2 = new ExternalKeysNode();  // 创建第二个节点节点
        node2.setParentNode(parent);  // 设置父节点
        node2.setParentNodes(parents);  // 设置父节点列表
        node2.setPlace(list.size(), 210 + 72 * 1, 570, 72, 72);  // 设置位置和尺寸
        node2.setName("NumberPicker:" + list.size());  // 设置节点名称
        node2.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(node2);  // 添加节点到列表
        ExternalKeysNode node3 = new ExternalKeysNode();  // 创建第三个节点节点
        node3.setParentNode(parent);  // 设置父节点
        node3.setParentNodes(parents);  // 设置父节点列表
        node3.setPlace(list.size(), 210 + 72 * 2, 570, 72, 72);  // 设置位置和尺寸
        node3.setName("NumberPicker:" + list.size());  // 设置节点名称
        node3.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(node3);  // 添加节点到列表
        ExternalKeysNode node4 = new ExternalKeysNode();  // 创建节点节点
        node4.setParentNode(parent);  // 设置父节点
        node4.setParentNodes(parents);  // 设置父节点列表
        node4.setPlace(list.size(), 210 + 72 * 3, 570, 72, 72);  // 设置位置和尺寸
        node4.setName("NumberPicker:" + list.size());  // 设置节点名称
        node4.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(node4);  // 添加节点到列表
        ExternalKeysNode node5 = new ExternalKeysNode();  // 创建节点节点
        node5.setParentNode(parent);  // 设置父节点
        node5.setParentNodes(parents);  // 设置父节点列表
        node5.setPlace(list.size(), 210 + 72 * 4, 570, 72, 72);  // 设置位置和尺寸
        node5.setName("NumberPicker:" + list.size());  // 设置节点名称
        node5.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(node5);  // 添加节点到列表
        ExternalKeysNode node6 = new ExternalKeysNode();  // 创建节点节点
        node6.setParentNode(parent);  // 设置父节点
        node6.setParentNodes(parents);  // 设置父节点列表
        node6.setPlace(list.size(), 210 + 72 * 5, 570, 72, 72);  // 设置位置和尺寸
        node6.setName("NumberPicker:" + list.size());  // 设置节点名称
        node6.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(node6);  // 添加节点到列表
        ExternalKeysNode node7 = new ExternalKeysNode();  // 创建节点节点
        node7.setParentNode(parent);  // 设置父节点
        node7.setParentNodes(parents);  // 设置父节点列表
        node7.setPlace(list.size(), 210 + 72 * 6 + 16, 570, 72, 72);  // 设置位置和尺寸
        node7.setName("NumberPicker:" + list.size());  // 设置节点名称
        node7.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(node7);  // 添加节点到列表
        ExternalKeysNode node8 = new ExternalKeysNode();  // 创建节点节点
        node8.setParentNode(parent);  // 设置父节点
        node8.setParentNodes(parents);  // 设置父节点列表
        node8.setPlace(list.size(), 210 + 72 * 7 + 16, 570, 72, 72);  // 设置位置和尺寸
        node8.setName("NumberPicker:" + list.size());  // 设置节点名称
        node8.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(node8);  // 添加节点到列表
        return list;  // 返回节点列表
    }
    //endregion

    //region RightSlipChannel
    /**
     * 构建右侧滑动栏-通道节点列表：通道开关/耦合/探头/带宽/标尺/反相/标签
     */
    private static List<ExternalKeysNode> getRightSlipChannelNodeList(int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] couples = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.channelCouple);  // 从资源文件获取字符串数组
        String[] probeTypes = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.channelProbeType);  // 从资源文件获取字符串数组
        String[] bandWidths = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.channelBandWidth);  // 从资源文件获取字符串数组
        ExternalKeysNode check = new ExternalKeysNode();  // 创建节点节点
        check.setPlace(list.size(), 1489, 72 + topSlipOffset, 64, 32);  // 设置位置和尺寸
        check.setName("check");  // 设置节点名称
        list.add(check);  // 添加节点到列表
        ExternalKeysNode invert = new ExternalKeysNode();  // 创建反色节点
        invert.setPlace(list.size(), 1489, 166 + topSlipOffset, 64, 32);  // 设置位置和尺寸
        invert.setName("invert");  // 设置节点名称
        list.add(invert);  // 添加节点到列表

        ExternalKeysNode topImg = new ExternalKeysNode();  // 创建节点节点
        topImg.setPlace(list.size(), 1682, 49 + topSlipOffset, 88, 87);  // 设置位置和尺寸
        topImg.setName("topImg");  // 设置节点名称
        list.add(topImg);  // 添加节点到列表
        ExternalKeysNode bottomImg = new ExternalKeysNode();  // 创建节点节点
        bottomImg.setPlace(list.size(), 1682, 134 + topSlipOffset, 88, 87);  // 设置位置和尺寸
        bottomImg.setName("bottomImg");  // 设置节点名称
        list.add(bottomImg);  // 添加节点到列表

        for (int i = 0; i < 3; i++) {  // 循环创建节点
            ExternalKeysNode couple = new ExternalKeysNode();  // 创建节点节点
            couple.setPlace(list.size(), 1420 + 121 * i, 271 + topSlipOffset, 108, 54);  // 设置位置和尺寸
            couple.setName(couples[i]);  // 设置节点名称
            list.add(couple);  // 添加节点到列表
        }

        //common
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode probeType = new ExternalKeysNode();  // 创建节点节点
            probeType.setPlace(list.size(), 1420 + 108 * i, 379 + topSlipOffset, 108, 54);  // 设置位置和尺寸
            probeType.setName(probeTypes[i]);  // 设置节点名称
            list.add(probeType);  // 添加节点到列表
        }
        ExternalKeysNode probeMultiple = new ExternalKeysNode();  // 创建节点节点
        probeMultiple.setPlace(list.size(), 1661, 379 + topSlipOffset, 108, 54);  // 设置位置和尺寸
        probeMultiple.setName("probeMultiple");  // 设置节点名称
        probeMultiple.setChildNodes(getRightDialogProbeMultipleNodeList(probeMultiple, list, topSlipOffset));  // 设置子节点列表
        probeMultiple.setDialog(ExternalKeysNode.DIALOG_PROBEMULTIPLE);  // 关联弹窗
        list.add(probeMultiple);  // 添加节点到列表

        //msp500
        ExternalKeysNode probeCal = new ExternalKeysNode();  // 创建节点节点
        probeCal.setPlace(list.size(), 1662, 396 + topSlipOffset, 108, 54);  // 设置位置和尺寸
        probeCal.setName("probeCal");  // 设置节点名称
//        probeCal.setChildNodes(getRightDialogProbeInterfaceNodeList(probeCal, list));
//        probeCal.setDialog(ExternalKeysNode.DIALOG_PROBE_INTERFACE);
        list.add(probeCal);  // 添加节点到列表

        //mdp700
        ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
        node.setPlace(list.size(), 1420, 390 + topSlipOffset, 108, 54);  // 设置位置和尺寸
        node.setName("probe20X");  // 设置节点名称
        list.add(node);  // 添加节点到列表
        ExternalKeysNode node1 = new ExternalKeysNode();  // 创建节点节点
        node1.setPlace(list.size(), 1528, 400 + topSlipOffset, 108, 54);  // 设置位置和尺寸
        node1.setName("probe200X");  // 设置节点名称
        list.add(node1);  // 添加节点到列表
        ExternalKeysNode info = new ExternalKeysNode();  // 创建节点节点
        info.setPlace(list.size(), 1661, 4390 + topSlipOffset, 108, 54);  // 设置位置和尺寸
        info.setName("info");  // 设置节点名称
        info.setChildNodes(getRightDialogProbeInterfaceNodeList(info, list));  // 设置子节点列表
        info.setDialog(ExternalKeysNode.DIALOG_PROBE_INTERFACE);  // 关联弹窗
        list.add(info);  // 添加节点到列表

        // mcrp
        ExternalKeysNode mcrpInfo = new ExternalKeysNode();  // 创建节点节点
        mcrpInfo.setPlace(list.size(), 1662, 399 + topSlipOffset, 108, 54);  // 设置位置和尺寸
        mcrpInfo.setName("McrpInfo");  // 设置节点名称
        mcrpInfo.setChildNodes(getRightDialogProbeInterfaceNodeList(mcrpInfo, list));  // 设置子节点列表
        mcrpInfo.setDialog(ExternalKeysNode.DIALOG_PROBE_INTERFACE);  // 关联弹窗
        list.add(mcrpInfo);  // 添加节点到列表

        //BandWidth
        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode bandWidthType = new ExternalKeysNode();  // 创建节点节点
            bandWidthType.setPlace(list.size(), 1420 + (i % 3) * 122, i < 3 ? 494 + topSlipOffset : 562 + topSlipOffset, 108, 54);  // 设置位置和尺寸
            bandWidthType.setName(bandWidths[i]);  // 设置节点名称
            list.add(bandWidthType);  // 添加节点到列表
        }
        ExternalKeysNode bandWidthNumber = new ExternalKeysNode();  // 创建节点节点
        bandWidthNumber.setPlace(list.size(), 1665, 562 + topSlipOffset, 106, 54);  // 设置位置和尺寸
        bandWidthNumber.setName("bandWidthNumber");  // 设置节点名称
//        bandWidthNumber.setChildNodes(getTopDialogCountDetailNodeList(bandWidthNumber, list));
        bandWidthNumber.setDialog(ExternalKeysNode.DIALOG_TOPCOUNT);  // 关联弹窗:计数弹窗
        list.add(bandWidthNumber);  // 添加节点到列表

        for (int i = 0; i < 2; i++) {// 阻抗 去掉了，放到了 耦合方式 里
            ExternalKeysNode verBase = new ExternalKeysNode();  // 创建节点节点
            verBase.setPlace(list.size(), 1550 + i * 108, 635 + topSlipOffset, 108, 54);  // 设置位置和尺寸
            verBase.setName("Imped:" + i);  // 设置节点名称
            list.add(verBase);  // 添加节点到列表
        }

        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode verBase = new ExternalKeysNode();  // 创建节点节点
            verBase.setPlace(list.size(), 1550 + i * 108, 706 + topSlipOffset, 108, 54);  // 设置位置和尺寸
            verBase.setName("verBase:" + i);  // 设置节点名称
            list.add(verBase);  // 添加节点到列表
        }
        ExternalKeysNode label = new ExternalKeysNode();  // 创建节点节点
        label.setPlace(list.size(), 1535, 768 + topSlipOffset, 230, 54);  // 设置位置和尺寸
        label.setName("label");  // 设置节点名称
        label.setChildNodes(getRightDialogLabelNodeList(label, list, topSlipOffset));  // 设置子节点列表
        label.setDialog(ExternalKeysNode.DIALOG_CHANNELLABEL);  // 关联弹窗
        list.add(label);  // 添加节点到列表

        ExternalKeysNode delay = new ExternalKeysNode();  // 创建节点节点
        delay.setPlace(list.size(), 1535, 830 + topSlipOffset, 230, 54);  // 设置位置和尺寸
        delay.setName("delay");  // 设置节点名称
        delay.setChildNodes(getDelayFloatKeyBoardNodeList(delay, list));  // 设置子节点列表
        delay.getChildNodes().get(14).setVisible(false);  // 设为不可见
        delay.getChildNodes().get(18).setVisible(false);  // 设为不可见
        delay.getChildNodes().get(19).setVisible(false);  // 设为不可见
        delay.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(delay);  // 添加节点到列表

        ExternalKeysNode position = new ExternalKeysNode();  // 创建节点节点
        position.setPlace(list.size(), 1535, 892 + topSlipOffset, 230, 54);  // 设置位置和尺寸
        position.setName("position");  // 设置节点名称
        position.setChildNodes(getFloatKeyBoardNodeList(position, list));  // 设置子节点列表
        position.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(position);  // 添加节点到列表

        ExternalKeysNode offset = new ExternalKeysNode();  // 创建节点节点
        offset.setPlace(list.size(), 1535, 954 + topSlipOffset, 230, 54);  // 设置位置和尺寸
        offset.setName("offset");  // 设置节点名称
        offset.setChildNodes(getFloatKeyBoardNodeList(offset, list));  // 设置子节点列表
        offset.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(offset);  // 添加节点到列表

        ExternalKeysNode fineExtent = new ExternalKeysNode();  // 创建节点节点
        fineExtent.setPlace(list.size(), 1535, 1016 + topSlipOffset, 140, 54);  // 设置位置和尺寸
        fineExtent.setName("fineExtent");  // 设置节点名称
        fineExtent.setChildNodes(getFloatKeyBoardNodeList(fineExtent, list));  // 设置子节点列表
        fineExtent.getChildNodes().get(3).setVisible(false);  // 设为不可见
        fineExtent.getChildNodes().get(4).setVisible(false);  // 设为不可见
//        fineExtent.getChildNodes().get(8).setVisible(false);
        fineExtent.getChildNodes().get(9).setVisible(false);  // 设为不可见
        fineExtent.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD); //map: mainviewgroup.DIALOG_FLOATKEYBOARD
        list.add(fineExtent);  // 添加节点到列表

        ExternalKeysNode fineSwitch = new ExternalKeysNode();  // 创建节点节点
        fineSwitch.setPlace(list.size(), 1706, 1016 + topSlipOffset, 64, 54);  // 设置位置和尺寸
        fineSwitch.setName("fineSwitch");  // 设置节点名称
        list.add(fineSwitch);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建探头倍率对话框节点：探头倍率选项
     */
    private static List<ExternalKeysNode> getRightDialogProbeMultipleNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < 22; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
            node.setPlace(list.size(), (int) (1418 + 92 * (i % 4)), 70 + topSlipOffset + (i / 4) * 68, 78, 54);  // 设置位置和尺寸
            node.setName("ProbeMultipleChild:index:" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }

        ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
        node.setParentNode(parent);  // 设置父节点
        node.setParentNodes(parents);  // 设置父节点列表
        node.setPlace(list.size(), 1602, 410 + topSlipOffset, 170, 54);  // 设置位置和尺寸
        node.setName("customProbeMultipleChild");  // 设置节点名称
        node.setChildNodes(getFloatKeyBoardNodeList(node, list));  // 设置子节点列表
        node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        node.getChildNodes().get(3).setVisible(false);  // 设为不可见
        node.getChildNodes().get(4).setVisible(false);  // 设为不可见
        node.getChildNodes().get(8).setVisible(false);  // 设为不可见
        node.getChildNodes().get(9).setVisible(false);  // 设为不可见
        node.getChildNodes().get(13).setVisible(false);  // 设为不可见
        list.add(node);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建探头接口对话框节点：接口类型选项
     */
    private static List<ExternalKeysNode> getRightDialogProbeInterfaceNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode manual = new ExternalKeysNode();  // 创建节点节点
        manual.setParentNode(parent);  // 设置父节点
        manual.setParentNodes(parents);  // 设置父节点列表
        manual.setPlace(list.size(), 1533, 179, 120, 60);  // 设置位置和尺寸
        manual.setName("manual");  // 设置节点名称
        list.add(manual);  // 添加节点到列表

        ExternalKeysNode auto = new ExternalKeysNode();  // 创建自动节点
        auto.setParentNode(parent);  // 设置父节点
        auto.setParentNodes(parents);  // 设置父节点列表
        auto.setPlace(list.size(), 1653, 179, 120, 60);  // 设置位置和尺寸
        auto.setName("auto");  // 设置节点名称
        list.add(auto);  // 添加节点到列表

        ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
        node.setParentNode(parent);  // 设置父节点
        node.setParentNodes(parents);  // 设置父节点列表
        node.setPlace(list.size(), 1641, 289, 132, 60);  // 设置位置和尺寸
        node.setName("cal");  // 设置节点名称
        list.add(node);  // 添加节点到列表
        for (int i = 0; i < 9; i++) {  // 循环创建节点
            node = new ExternalKeysNode();
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            int y = i / 3 * 84 + 416;  // 定义整型变量
            node.setPlace(list.size(), 1377 + i % 3 * 136, y, 120, 68);  // 设置位置和尺寸
            node.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
            node.setName("bandWidth" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }
        return list;  // 返回节点列表


    }


    /**
     * 构建通道标签对话框节点：标签名输入+颜色选择
     */
    private static List<ExternalKeysNode> getRightDialogLabelNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int topSlipOffset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode node1 = new ExternalKeysNode();  // 创建节点节点
        node1.setParentNode(parent);  // 设置父节点
        node1.setParentNodes(parents);  // 设置父节点列表
        node1.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
        node1.setPlace(list.size(), 1419, 70 + topSlipOffset, 170, 54);  // 设置位置和尺寸
        node1.setName("LabelChild:index:0");  // 设置节点名称
        list.add(node1);  // 添加节点到列表
        ExternalKeysNode node2 = new ExternalKeysNode();  // 创建第二个节点节点
        node2.setParentNode(parent);  // 设置父节点
        node2.setParentNodes(parents);  // 设置父节点列表
        node2.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        node2.setChildNodes(getTextKeyBoardNodeList(node2, list));  // 设置子节点列表
        node2.setDialog(ExternalKeysNode.DIALOG_TEXTKEYBOARD);  // 关联弹窗:文本键盘
        node2.setPlace(list.size(), 1603, 70 + topSlipOffset, 170, 54);  // 设置位置和尺寸
        node2.setName("LabelChild:index:1");  // 设置节点名称
        list.add(node2);  // 添加节点到列表
        for (int i = 0, add = 0; i < 18; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
            add = i % 2 == 0 ? 0 : 1;
            node.setPlace(list.size(), (int) (1419 + 92 * (i % 4)), 138 + (i / 4) * 68 + topSlipOffset, 78, 54);  // 设置位置和尺寸
            node.setName("LabelChild:index:" + (i + 2));  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }
    //endregion

    //region RightSlipMath
    /**
     * 构建右侧滑动栏-Math运算节点列表：运算类型/源通道/垂直参数
     */
    private static List<ExternalKeysNode> getRightSlipMathNodeList(int mathNumber, int offset) {
        boolean isFourChannels = GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4;  // 获取通道数量
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        offset = -7;
        ExternalKeysNode math = new ExternalKeysNode();  // 创建节点节点
        math.setPlace(list.size(), 982, 216 + offset, 185, 70);  // 设置位置和尺寸
        math.setName("Math-Math" + mathNumber);  // 设置节点名称
        math.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(math);  // 添加节点到列表

        ExternalKeysNode ref = new ExternalKeysNode();  // 创建节点节点
        ref.setPlace(list.size(), 1167, 216 + offset, 185, 70);  // 设置位置和尺寸
        ref.setName("Math-Ref" + mathNumber);  // 设置节点名称
        ref.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(ref);  // 添加节点到列表

        ExternalKeysNode serialBus = new ExternalKeysNode();  // 创建节点节点
        serialBus.setPlace(list.size(), 1352, 216 + offset, 185, 70);  // 设置位置和尺寸
        serialBus.setName("Math-SerialBus" + mathNumber);  // 设置节点名称
        serialBus.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(serialBus);  // 添加节点到列表

        ExternalKeysNode delete = new ExternalKeysNode();  // 创建节点节点
        delete.setPlace(list.size(), 1665, 227 + offset, 108, 48);  // 设置位置和尺寸
        delete.setName("Math-Delete" + mathNumber);  // 设置节点名称
        delete.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        delete.setVisible(false);  // 设为不可见
        list.add(delete);  // 添加节点到列表

        ExternalKeysNode add = new ExternalKeysNode();  // 创建右选择2节点
        add.setPlace(list.size(), 1719, 224 + offset, 54, 54);  // 设置位置和尺寸
        add.setName("Math-Add" + mathNumber);  // 设置节点名称
        add.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(add);  // 添加节点到列表

        ExternalKeysNode doubleWave = new ExternalKeysNode();  // 创建节点节点
        doubleWave.setPlace(list.size(), 1020, 316 + offset, 108, 54);  // 设置位置和尺寸
        doubleWave.setName("mathDoubleWave");  // 设置节点名称
        add.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        doubleWave.setChildNodes(getRightSlipMathDoubleWaveNodeList(doubleWave, list, offset));  // 设置子节点列表
        list.add(doubleWave);  // 添加节点到列表

        ExternalKeysNode fft = new ExternalKeysNode();  // 创建节点节点
        fft.setPlace(list.size(), 1020, 384 + offset, 108, 54);  // 设置位置和尺寸
        fft.setName("mathFft");  // 设置节点名称
        add.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        fft.setChildNodes(getRightSlipMathFftNodeList(fft, list, offset));  // 设置子节点列表
        list.add(fft);  // 添加节点到列表

        if (GlobalVar.get().isMathAxbVisible()) {  // 条件判断
            ExternalKeysNode axb = new ExternalKeysNode();  // 创建节点节点
            axb.setPlace(list.size(), 1020, 452 + offset, 108, 54);  // 设置位置和尺寸
            axb.setName("mathAxb");  // 设置节点名称
            add.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
            axb.setChildNodes(getRightSlipMathAxbNodeList(axb, list, offset));  // 设置子节点列表
            list.add(axb);  // 添加节点到列表
        }

        ExternalKeysNode advanceMath = new ExternalKeysNode();  // 创建节点节点
        advanceMath.setPlace(list.size(), 1020, 520 + offset, 108, 54);  // 设置位置和尺寸
        advanceMath.setName("mathAdvance");  // 设置节点名称
        add.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        advanceMath.setChildNodes(getRightSlipMathAdvanceNodeList(advanceMath, list, offset));  // 设置子节点列表
        list.add(advanceMath);  // 添加节点到列表

        ExternalKeysNode selectColor = new ExternalKeysNode();  // 创建节点节点
        selectColor.setPlace(list.size(), 1011, 630, 129, 54);  // 设置位置和尺寸
        selectColor.setName("MathSelectColor");  // 设置节点名称
        selectColor.setChildNodes(getMathRefSelectColorDialogNodeList(selectColor, list));  // 设置子节点列表
        selectColor.setDialog(ExternalKeysNode.DIALOG_SELECT_COLOR);  // 关联弹窗
        list.add(selectColor);  // 添加节点到列表

        ExternalKeysNode mathLabel = new ExternalKeysNode();  // 创建节点节点
        mathLabel.setPlace(list.size(), 1010, 737, 128, 54);  // 设置位置和尺寸
        mathLabel.setName("mathLabel");  // 设置节点名称
        mathLabel.setDialog(ExternalKeysNode.DIALOG_CHANNELLABEL);  // 关联弹窗
        mathLabel.setChildNodes(getMathRefRightDialogLabelNodeList(mathLabel, list, offset));  // 设置子节点列表
        list.add(mathLabel);  // 添加节点到列表

        ExternalKeysNode mathCheck = new ExternalKeysNode();  // 创建节点节点
        mathCheck.setPlace(list.size(), 1038, 844, 72, 36);  // 设置位置和尺寸
        mathCheck.setName("mathCheck");  // 设置节点名称
        mathCheck.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(mathCheck);  // 添加节点到列表

        ExternalKeysNode imgTop = new ExternalKeysNode();  // 创建节点节点
        imgTop.setPlace(list.size(), 1027, 902 + offset, 96, 96);  // 设置位置和尺寸
        imgTop.setName("imgTop" + mathNumber);  // 设置节点名称
        imgTop.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(imgTop);  // 添加节点到列表

        ExternalKeysNode imgBottom = new ExternalKeysNode();  // 创建节点节点
        imgBottom.setPlace(list.size(), 1027, 998 + offset, 96, 96);  // 设置位置和尺寸
        imgBottom.setName("imgBottom" + mathNumber);  // 设置节点名称
        imgBottom.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(imgBottom);  // 添加节点到列表

//        ExternalKeysNode vertical = new ExternalKeysNode();
//        vertical.setPlace(list.size(), 481, 452, 254, 59);
//        vertical.setName("mathVertical");
//        vertical.setType(ExternalKeysNode.TYPE_NO_CLICK);
//        vertical.setChildNodes(getRightSlipMathVerticalNodeList(vertical, list));
//        list.add(vertical);

        ExternalKeysNode mathVertical = new ExternalKeysNode();  // 创建节点节点
        mathVertical.setPlace(list.size(), 1556, 1042 + offset, 108, 54);  // 设置位置和尺寸
        mathVertical.setName("Center" + mathNumber);  // 设置节点名称
        mathVertical.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(mathVertical);  // 添加节点到列表

        ExternalKeysNode mathZero = new ExternalKeysNode();  // 创建节点节点
        mathZero.setPlace(list.size(), 1556 + 108, 1042 + offset, 108, 54);  // 设置位置和尺寸
        mathZero.setName("Zero" + mathNumber);  // 设置节点名称
        mathZero.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(mathZero);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    //暂时不用
    /**
     * 构建Math-DW布局位置节点列表
     */
    public static List<ExternalKeysNode> getRightSlipMathShowDWLayoutPlaceList() {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode mathCheck = new ExternalKeysNode();  // 创建节点节点
        mathCheck.setPlace(list.size(), 1716, 87, 72, 42);  // 设置位置和尺寸
        mathCheck.setName("mathCheck");  // 设置节点名称
        list.add(mathCheck);  // 添加节点到列表
        ExternalKeysNode doubleWave = new ExternalKeysNode();  // 创建节点节点
        doubleWave.setPlace(list.size(), 1343, 139, 455, 450);  // 设置位置和尺寸
        doubleWave.setName("mathDoubleWave");  // 设置节点名称
        doubleWave.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(doubleWave);  // 添加节点到列表
        ExternalKeysNode fft = new ExternalKeysNode();  // 创建节点节点
        fft.setPlace(list.size(), 1343, 588, 455, 64);  // 设置位置和尺寸
        fft.setName("mathFft");  // 设置节点名称
        list.add(fft);  // 添加节点到列表
        if (GlobalVar.get().isMathAxbVisible()) {  // 条件判断
            ExternalKeysNode axb = new ExternalKeysNode();  // 创建节点节点
            axb.setPlace(list.size(), 1343, 651, 455, 64);  // 设置位置和尺寸
            axb.setName("mathAxb");  // 设置节点名称
            list.add(axb);  // 添加节点到列表
        }
        ExternalKeysNode advanceMath = new ExternalKeysNode();  // 创建节点节点
        advanceMath.setPlace(list.size(), 1343, 716, 455, 64);  // 设置位置和尺寸
        advanceMath.setName("mathAdvance");  // 设置节点名称
        list.add(advanceMath);  // 添加节点到列表
        ExternalKeysNode vertical = new ExternalKeysNode();  // 创建垂直节点
        vertical.setPlace(list.size(), 1343, 1039, 455, 60);  // 设置位置和尺寸
        vertical.setName("mathVertical");  // 设置节点名称
        vertical.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        vertical.setChildNodes(getRightSlipMathVerticalNodeList(vertical, list));  // 设置子节点列表
        list.add(vertical);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    //暂时不用
    /**
     * 构建Math-FFT布局位置节点列表
     */
    public static List<ExternalKeysNode> getRightSlipMathShowFftLayoutPlaceList() {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode mathCheck = new ExternalKeysNode();  // 创建节点节点
        mathCheck.setPlace(list.size(), 1716, 87, 72, 42);  // 设置位置和尺寸
        mathCheck.setName("mathCheck");  // 设置节点名称
        list.add(mathCheck);  // 添加节点到列表
        ExternalKeysNode doubleWave = new ExternalKeysNode();  // 创建节点节点
        doubleWave.setPlace(list.size(), 1343, 139, 455, 64);  // 设置位置和尺寸
        doubleWave.setName("mathDoubleWave");  // 设置节点名称
        list.add(doubleWave);  // 添加节点到列表
        ExternalKeysNode fft = new ExternalKeysNode();  // 创建节点节点
        fft.setPlace(list.size(), 1343, 203, 455, 638);  // 设置位置和尺寸
        fft.setName("mathFft");  // 设置节点名称
        fft.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(fft);  // 添加节点到列表
        if (GlobalVar.get().isMathAxbVisible()) {  // 条件判断
            ExternalKeysNode axb = new ExternalKeysNode();  // 创建节点节点
            axb.setPlace(list.size(), 1343, 845, 455, 64);  // 设置位置和尺寸
            axb.setName("mathAxb");  // 设置节点名称
            list.add(axb);  // 添加节点到列表
        }
        ExternalKeysNode advanceMath = new ExternalKeysNode();  // 创建节点节点
        advanceMath.setPlace(list.size(), 1343, 909, 455, 64);  // 设置位置和尺寸
        advanceMath.setName("mathAdvance");  // 设置节点名称
        list.add(advanceMath);  // 添加节点到列表
        ExternalKeysNode vertical = new ExternalKeysNode();  // 创建垂直节点
        vertical.setPlace(list.size(), 1343, 1039, 455, 60);  // 设置位置和尺寸
        vertical.setName("mathVertical");  // 设置节点名称
        vertical.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        vertical.setChildNodes(getRightSlipMathVerticalNodeList(vertical, list));  // 设置子节点列表
        list.add(vertical);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    //暂时不用
    /**
     * 构建Math-AxB布局位置节点列表
     */
    public static List<ExternalKeysNode> getRightSlipMathShowAxbLayoutPlaceList() {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode mathCheck = new ExternalKeysNode();  // 创建节点节点
        mathCheck.setPlace(list.size(), 1716, 87, 72, 42);  // 设置位置和尺寸
        mathCheck.setName("mathCheck");  // 设置节点名称
        list.add(mathCheck);  // 添加节点到列表
        ExternalKeysNode doubleWave = new ExternalKeysNode();  // 创建节点节点
        doubleWave.setPlace(list.size(), 1343, 139, 455, 64);  // 设置位置和尺寸
        doubleWave.setName("mathDoubleWave");  // 设置节点名称
        list.add(doubleWave);  // 添加节点到列表
        ExternalKeysNode fft = new ExternalKeysNode();  // 创建节点节点
        fft.setPlace(list.size(), 1343, 203, 455, 64);  // 设置位置和尺寸
        fft.setName("mathFft");  // 设置节点名称
        list.add(fft);  // 添加节点到列表
        if (GlobalVar.get().isMathAxbVisible()) {  // 条件判断
            ExternalKeysNode axb = new ExternalKeysNode();  // 创建节点节点
            axb.setPlace(list.size(), 1343, 267, 455, 457);  // 设置位置和尺寸
            axb.setName("mathAxb");  // 设置节点名称
            axb.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
            list.add(axb);  // 添加节点到列表
        }
        ExternalKeysNode advanceMath = new ExternalKeysNode();  // 创建节点节点
        advanceMath.setPlace(list.size(), 1343, 728, 455, 64);  // 设置位置和尺寸
        advanceMath.setName("mathAdvance");  // 设置节点名称
        list.add(advanceMath);  // 添加节点到列表
        ExternalKeysNode vertical = new ExternalKeysNode();  // 创建垂直节点
        vertical.setPlace(list.size(), 1343, 1039, 455, 60);  // 设置位置和尺寸
        vertical.setName("mathVertical");  // 设置节点名称
        vertical.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        vertical.setChildNodes(getRightSlipMathVerticalNodeList(vertical, list));  // 设置子节点列表
        list.add(vertical);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    //暂时不用
    /**
     * 构建Math-Advance高级运算布局位置节点列表
     */
    public static List<ExternalKeysNode> getRightSlipMathShowAdvanceLayoutPlaceList() {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode mathCheck = new ExternalKeysNode();  // 创建节点节点
        mathCheck.setPlace(list.size(), 1716, 87, 72, 42);  // 设置位置和尺寸
        mathCheck.setName("mathCheck");  // 设置节点名称
        list.add(mathCheck);  // 添加节点到列表
        ExternalKeysNode doubleWave = new ExternalKeysNode();  // 创建节点节点
        doubleWave.setPlace(list.size(), 1343, 139, 455, 64);  // 设置位置和尺寸
        doubleWave.setName("mathDoubleWave");  // 设置节点名称
        list.add(doubleWave);  // 添加节点到列表
        ExternalKeysNode fft = new ExternalKeysNode();  // 创建节点节点
        fft.setPlace(list.size(), 1343, 203, 455, 64);  // 设置位置和尺寸
        fft.setName("mathFft");  // 设置节点名称
        list.add(fft);  // 添加节点到列表
        if (GlobalVar.get().isMathAxbVisible()) {  // 条件判断
            ExternalKeysNode axb = new ExternalKeysNode();  // 创建节点节点
            axb.setPlace(list.size(), 1343, 267, 455, 64);  // 设置位置和尺寸
            axb.setName("mathAxb");  // 设置节点名称
            list.add(axb);  // 添加节点到列表
        }
        ExternalKeysNode advanceMath = new ExternalKeysNode();  // 创建节点节点
        advanceMath.setPlace(list.size(), 1343, 331, 455, 498);  // 设置位置和尺寸
        advanceMath.setName("mathAdvance");  // 设置节点名称
        advanceMath.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(advanceMath);  // 添加节点到列表
        ExternalKeysNode vertical = new ExternalKeysNode();  // 创建垂直节点
        vertical.setPlace(list.size(), 1343, 1039, 455, 60);  // 设置位置和尺寸
        vertical.setName("mathVertical");  // 设置节点名称
        vertical.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        vertical.setChildNodes(getRightSlipMathVerticalNodeList(vertical, list));  // 设置子节点列表
        list.add(vertical);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Math-双波形运算节点：源A/源B选择
     */
    private static List<ExternalKeysNode> getRightSlipMathDoubleWaveNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int offset) {
        boolean isFourChannels = GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4;  // 获取通道数量
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] symbols = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.mathSymbol);  // 从资源文件获取字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source1 = new ExternalKeysNode();  // 创建节点节点
            source1.setParentNode(parent);  // 设置父节点
            source1.setParentNodes(parents);  // 设置父节点列表
//            int y = isFourChannels ? 143 + 49 * i : 123 + 65 * i;
            source1.setPlace(list.size(), 1298 + 122 * (i % 4), 317 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
            source1.setName(channels[i]);  // 设置节点名称
            list.add(source1);  // 添加节点到列表
        }
        for (int i = 0; i < symbols.length; i++) {  // 循环创建节点
            ExternalKeysNode symbol = new ExternalKeysNode();  // 创建符号键节点
            symbol.setParentNode(parent);  // 设置父节点
            symbol.setParentNodes(parents);  // 设置父节点列表
            symbol.setPlace(list.size(), 1385 + 82 * i, 467 + offset, 54, 54);  // 设置位置和尺寸
            symbol.setName(symbols[i]);  // 设置节点名称
            list.add(symbol);  // 添加节点到列表
        }
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source2 = new ExternalKeysNode();  // 创建节点节点
            source2.setParentNode(parent);  // 设置父节点
            source2.setParentNodes(parents);  // 设置父节点列表
//            int y = isFourChannels ? 143 + 49 * i : 123 + 65 * i;
            source2.setPlace(list.size(), 1298 + 122 * (i % 4), 549 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
            source2.setName(channels[i]);  // 设置节点名称
            list.add(source2);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Math-FFT运算节点：源/窗函数/阈值
     */
    private static List<ExternalKeysNode> getRightSlipMathFftNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int offset) {
        boolean isFourChannels = GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4;  // 获取通道数量
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] types = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.mathFftType);  // 从资源文件获取字符串数组
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] windows = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.mathWindow);  // 从资源文件获取字符串数组
        for (int i = 0; i < types.length; i++) {  // 遍历选项创建节点
            ExternalKeysNode type = new ExternalKeysNode();  // 创建节点节点
            type.setParentNode(parent);  // 设置父节点
            type.setParentNodes(parents);  // 设置父节点列表
            type.setPlace(list.size(), 1556 + 108 * i, 316 + offset, 108, 54);  // 设置位置和尺寸
            type.setName(types[i]);  // 设置节点名称
            list.add(type);  // 添加节点到列表
        }
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 1298 + 122 * (i % 4), 400 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < windows.length; i++) {  // 循环创建节点
            ExternalKeysNode window = new ExternalKeysNode();  // 创建节点节点
            window.setParentNode(parent);  // 设置父节点
            window.setParentNodes(parents);  // 设置父节点列表
//            int y = isFourChannels ? (i < 2 ? 285 : 334) : (i < 2 ? 217 : 262);
            int y = i < 2 ? 511 : 595;  // 定义整型变量
            window.setPlace(list.size(), 1298 + 122 * (i % 4), 552 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
            window.setName(windows[i]);  // 设置节点名称
            list.add(window);  // 添加节点到列表
        }
 /*       for (int i = 0; i < 4; i++) {
            ExternalKeysNode persistence=new ExternalKeysNode();  // 创建节点节点
            persistence.setParentNode(parent);  // 设置父节点
            persistence.setParentNodes(parents);  // 设置父节点列表
            int y=i<2?679:763;  // 定义整型变量
            persistence.setPlace(list.size(), 1298 + 122 * (i % 4), 637, 108, 54);  // 设置位置和尺寸
            persistence.setName("persistence" + i);  // 设置节点名称
            if (i == 3) {  // 第四项特殊处理
                persistence.setChildNodes(getRightDialogMathPersistenceNodeList(persistence, list));  // 设置子节点列表
                persistence.setDialog(ExternalKeysNode.DIALOG_MATH_PERSISTENCE);  // 关联弹窗
            }
            list.add(persistence);  // 添加节点到列表
        }*/
        return list;  // 返回节点列表
    }

    /**
     * 构建Math-余晖对话框节点
     */
    private static List<ExternalKeysNode> getRightDialogMathPersistenceNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < 6; i++) {  // 循环创建节点
            ExternalKeysNode persistence = new ExternalKeysNode();  // 创建节点节点
            persistence.setParentNode(parent);  // 设置父节点
            persistence.setParentNodes(parents);  // 设置父节点列表
            int y = i < 4 ? 686 : 770;  // 定义整型变量
            persistence.setPlace(list.size(), 1298 + 122 * (i % 4), 715 + 68 * (i / 4), 108, 54);  // 设置位置和尺寸
            persistence.setName("persistenceParam" + i);  // 设置节点名称
            persistence.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
            list.add(persistence);  // 添加节点到列表
        }

        return list;  // 返回节点列表
    }

    /**
     * 构建Math-AxB运算节点：源A/源B/运算符
     */
    private static List<ExternalKeysNode> getRightSlipMathAxbNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int offset) {
        boolean isFourChannels = GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4;  // 获取通道数量
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        ExternalKeysNode unit = new ExternalKeysNode();  // 创建节点节点
        unit.setParentNode(parent);  // 设置父节点
        unit.setParentNodes(parents);  // 设置父节点列表
        unit.setPlace(list.size(), 1542, 316 + offset, 230, 54);  // 设置位置和尺寸
        unit.setChildNodes(getTextKeyBoardNodeList(unit, list));  // 设置子节点列表
        unit.setDialog(ExternalKeysNode.DIALOG_TEXTKEYBOARD);  // 关联弹窗:文本键盘
        unit.setName("axbUnit");  // 设置节点名称
        list.add(unit);  // 添加节点到列表
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 1298 + 122 * (i % 4), 400 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
            source.setName("axb:" + channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        ExternalKeysNode a = new ExternalKeysNode();  // 创建节点节点
        a.setParentNode(parent);  // 设置父节点
        a.setParentNodes(parents);  // 设置父节点列表
        int y = isFourChannels ? 485 : 552;  // 定义整型变量
        a.setPlace(list.size(), 1542, y + offset, 230, 54);  // 设置位置和尺寸
        a.setChildNodes(getFullFloatKeyBoardNodeList(a, list));  // 设置子节点列表
        a.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        a.setName("axbA");  // 设置节点名称
        list.add(a);  // 添加节点到列表
        ExternalKeysNode b = new ExternalKeysNode();  // 创建节点节点
        b.setParentNode(parent);  // 设置父节点
        b.setParentNodes(parents);  // 设置父节点列表
        y = isFourChannels ? 569 : 636;
        b.setPlace(list.size(), 1542, y + offset, 230, 54);  // 设置位置和尺寸
        b.setChildNodes(getFullFloatKeyBoardNodeList(b, list));  // 设置子节点列表
        b.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        b.setName("axbB");  // 设置节点名称
        list.add(b);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Math-高级运算节点：运算表达式
     */
    public static List<ExternalKeysNode> getRightSlipMathAdvanceNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int offset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode amFormula = new ExternalKeysNode();  // 创建节点节点
        amFormula.setParentNode(parent);  // 设置父节点
        amFormula.setParentNodes(parents);  // 设置父节点列表
        amFormula.setPlace(list.size(), 1421, 316 + offset, 352, 82);  // 设置位置和尺寸
        amFormula.setChildNodes(getFormulaKeyBoardNodeList(amFormula, list));  // 设置子节点列表
        amFormula.setName("amFormula");  // 设置节点名称
        list.add(amFormula);  // 添加节点到列表
        ExternalKeysNode amVar1Number = new ExternalKeysNode();  // 创建节点节点
        amVar1Number.setParentNode(parent);  // 设置父节点
        amVar1Number.setParentNodes(parents);  // 设置父节点列表
        amVar1Number.setPlace(list.size(), 1421, 428 + offset, 230, 54);  // 设置位置和尺寸
        amVar1Number.setChildNodes(getNumberPickerPosNodeList(amVar1Number, list));  // 设置子节点列表
        amVar1Number.setName("amVar1Number");  // 设置节点名称
        list.add(amVar1Number);  // 添加节点到列表
        ExternalKeysNode amVar1Power = new ExternalKeysNode();  // 创建节点节点
        amVar1Power.setParentNode(parent);  // 设置父节点
        amVar1Power.setParentNodes(parents);  // 设置父节点列表
        amVar1Power.setPlace(list.size(), 1665, 428 + offset, 108, 54);  // 设置位置和尺寸
        amVar1Power.setChildNodes(getNumberPickerPosNodeList(amVar1Power, list));  // 设置子节点列表
        amVar1Power.setName("amVar1Power");  // 设置节点名称
        list.add(amVar1Power);  // 添加节点到列表
        ExternalKeysNode amVar2Number = new ExternalKeysNode();  // 创建节点节点
        amVar2Number.setParentNode(parent);  // 设置父节点
        amVar2Number.setParentNodes(parents);  // 设置父节点列表
        amVar2Number.setPlace(list.size(), 1421, 512 + offset, 230, 54);  // 设置位置和尺寸
        amVar2Number.setChildNodes(getNumberPickerPosNodeList(amVar2Number, list));  // 设置子节点列表
        amVar2Number.setName("amVar2Number");  // 设置节点名称
        list.add(amVar2Number);  // 添加节点到列表
        ExternalKeysNode amVar2Power = new ExternalKeysNode();  // 创建节点节点
        amVar2Power.setParentNode(parent);  // 设置父节点
        amVar2Power.setParentNodes(parents);  // 设置父节点列表
        amVar2Power.setPlace(list.size(), 1665, 512 + offset, 108, 54);  // 设置位置和尺寸
        amVar2Power.setChildNodes(getNumberPickerPosNodeList(amVar2Power, list));  // 设置子节点列表
        amVar2Power.setName("amVar2Power");  // 设置节点名称
        list.add(amVar2Power);  // 添加节点到列表
        ExternalKeysNode amUnit = new ExternalKeysNode();  // 创建节点节点
        amUnit.setParentNode(parent);  // 设置父节点
        amUnit.setParentNodes(parents);  // 设置父节点列表
        amUnit.setPlace(list.size(), 1543, 580 + offset, 230, 54);  // 设置位置和尺寸
        amUnit.setChildNodes(getTextKeyBoardNodeList(amUnit, list));  // 设置子节点列表
        amUnit.setName("amUnit");  // 设置节点名称
        list.add(amUnit);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建Math-垂直参数节点：标尺/偏移
     */
    private static List<ExternalKeysNode> getRightSlipMathVerticalNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < 2; i++) {  // 循环创建节点
            ExternalKeysNode type = new ExternalKeysNode();  // 创建节点节点
            type.setParentNode(parent);  // 设置父节点
            type.setParentNodes(parents);  // 设置父节点列表
            type.setPlace(list.size(), 1556 + i * 108, 1043, 108, 54);  // 设置位置和尺寸
            type.setName("mathVertical:" + i);  // 设置节点名称
            list.add(type);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }
    //endregion

    //region RightSlipRef
    /**
     * 构建右侧滑动栏-Ref参考波形节点列表：开关/标尺/偏移/颜色/标签/调出
     */
    private static List<ExternalKeysNode> getRightSlipRefNodeList(int refNumber, int offset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        offset = -7;
        ExternalKeysNode math = new ExternalKeysNode();  // 创建节点节点
        math.setPlace(list.size(), 982, 216 + offset, 185, 70);  // 设置位置和尺寸
        math.setName("Ref-Math" + refNumber);  // 设置节点名称
        math.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(math);  // 添加节点到列表

        ExternalKeysNode ref = new ExternalKeysNode();  // 创建节点节点
        ref.setPlace(list.size(), 1167, 216 + offset, 185, 70);  // 设置位置和尺寸
        ref.setName("Ref-Ref" + refNumber);  // 设置节点名称
        ref.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(ref);  // 添加节点到列表

        ExternalKeysNode serialBus = new ExternalKeysNode();  // 创建节点节点
        serialBus.setPlace(list.size(), 1352, 216 + offset, 185, 70);  // 设置位置和尺寸
        serialBus.setName("Ref-SerialBus" + refNumber);  // 设置节点名称
        serialBus.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(serialBus);  // 添加节点到列表

        ExternalKeysNode delete = new ExternalKeysNode();  // 创建节点节点
        delete.setPlace(list.size(), 1665, 227 + offset, 108, 48);  // 设置位置和尺寸
        delete.setName("Ref-Delete" + refNumber);  // 设置节点名称
        delete.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        delete.setVisible(false);  // 设为不可见
        list.add(delete);  // 添加节点到列表

        ExternalKeysNode add = new ExternalKeysNode();  // 创建右选择2节点
        add.setPlace(list.size(), 1719, 224 + offset, 54, 54);  // 设置位置和尺寸
        add.setName("Ref-Add" + refNumber);  // 设置节点名称
        add.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(add);  // 添加节点到列表

        ExternalKeysNode wav = new ExternalKeysNode();  // 创建节点节点
        wav.setPlace(list.size(), 1020, 309, 108, 54);  // 设置位置和尺寸
        wav.setName("WAV" + refNumber);  // 设置节点名称
        wav.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        wav.setDialog(ExternalKeysNode.DIALOG_REFRECALL);  // 关联弹窗
        wav.setChildNodes(getRightDialogRefRecallListNode(wav, list, refNumber, offset));  // 设置子节点列表
        list.add(wav);  // 添加节点到列表

        ExternalKeysNode csv = new ExternalKeysNode();  // 创建CSV节点
        csv.setPlace(list.size(), 1020, 377, 108, 54);  // 设置位置和尺寸
        csv.setName("CSV" + refNumber);  // 设置节点名称
        csv.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        csv.setChildNodes(getRightSlipRefCsvListNode(csv, list, refNumber, offset));  // 设置子节点列表
        list.add(csv);  // 添加节点到列表

        ExternalKeysNode phaseDelay = new ExternalKeysNode();  // 创建节点节点
        phaseDelay.setPlace(list.size(), 1010, 523, 128, 54);  // 设置位置和尺寸
        phaseDelay.setName("RefPhaseDelay");  // 设置节点名称
        phaseDelay.setChildNodes(getFloatKeyBoardNodeList(phaseDelay, list));  // 设置子节点列表
        phaseDelay.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(phaseDelay);  // 添加节点到列表

        ExternalKeysNode selectColor = new ExternalKeysNode();  // 创建节点节点
        selectColor.setPlace(list.size(), 1011, 630, 128, 54);  // 设置位置和尺寸
        selectColor.setName("RefSelectColor");  // 设置节点名称
        selectColor.setChildNodes(getMathRefSelectColorDialogNodeList(selectColor, list));  // 设置子节点列表
        selectColor.setDialog(ExternalKeysNode.DIALOG_SELECT_COLOR);  // 关联弹窗
        list.add(selectColor);  // 添加节点到列表

        ExternalKeysNode refLabel = new ExternalKeysNode();  // 创建节点节点
        refLabel.setPlace(list.size(), 1010, 737, 128, 54);  // 设置位置和尺寸
        refLabel.setName("RefLabel");  // 设置节点名称
        refLabel.setDialog(ExternalKeysNode.DIALOG_CHANNELLABEL);  // 关联弹窗
        refLabel.setChildNodes(getMathRefRightDialogLabelNodeList(refLabel, list, offset));  // 设置子节点列表
        list.add(refLabel);  // 添加节点到列表

        ExternalKeysNode refCheck = new ExternalKeysNode();  // 创建节点节点
        refCheck.setPlace(list.size(), 1038, 844, 72, 36);  // 设置位置和尺寸
        refCheck.setName("RefCheck");  // 设置节点名称
        refCheck.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(refCheck);  // 添加节点到列表

        ExternalKeysNode imgTop = new ExternalKeysNode();  // 创建节点节点
        imgTop.setPlace(list.size(), 1027, 902 + offset, 96, 96);  // 设置位置和尺寸
        imgTop.setName("imgTop" + refNumber);  // 设置节点名称
        imgTop.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(imgTop);  // 添加节点到列表

        ExternalKeysNode imgBottom = new ExternalKeysNode();  // 创建节点节点
        imgBottom.setPlace(list.size(), 1027, 998 + offset, 96, 96);  // 设置位置和尺寸
        imgBottom.setName("imgBottom" + refNumber);  // 设置节点名称
        imgBottom.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(imgBottom);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Ref-标签对话框节点：标签名+颜色
     */
    private static List<ExternalKeysNode> getMathRefRightDialogLabelNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int offset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode node1 = new ExternalKeysNode();  // 创建节点节点
        node1.setParentNode(parent);  // 设置父节点
        node1.setParentNodes(parents);  // 设置父节点列表
        node1.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
        node1.setPlace(list.size(), 1196, 316 + offset, 170, 54);  // 设置位置和尺寸
        node1.setName("LabelChild:index:0");  // 设置节点名称
        list.add(node1);  // 添加节点到列表
        ExternalKeysNode node2 = new ExternalKeysNode();  // 创建第二个节点节点
        node2.setParentNode(parent);  // 设置父节点
        node2.setParentNodes(parents);  // 设置父节点列表
        node2.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        node2.setChildNodes(getTextKeyBoardNodeList(node2, list));  // 设置子节点列表
        node2.setDialog(ExternalKeysNode.DIALOG_TEXTKEYBOARD);  // 关联弹窗:文本键盘
        node2.setPlace(list.size(), 1380, 316 + offset, 170, 54);  // 设置位置和尺寸
        node2.setName("LabelChild:index:1");  // 设置节点名称
        list.add(node2);  // 添加节点到列表
        for (int i = 0, add = 0; i < 18; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
            add = i % 2 == 0 ? 0 : 1;
            node.setPlace(list.size(), (int) (1196 + 92 * (i % 4)), 384 + (i / 4) * 68 + offset, 78, 54);  // 设置位置和尺寸
            node.setName("LabelChild:index:" + (i + 2));  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Ref-颜色选择对话框节点：颜色选项
     */
    private static List<ExternalKeysNode> getMathRefSelectColorDialogNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < 4; i++) {  // 循环创建节点
            for (int j = 0; j < 4; j++) {  // 循环创建节点
                ExternalKeysNode item = new ExternalKeysNode();  // 创建条目节点
                item.setParentNode(parent);  // 设置父节点
                item.setParentNodes(parents);  // 设置父节点列表
                item.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
                int x = 1209 + 148 * j - j / 3;  // 定义整型变量
                int y = 322 + 100 * i;  // 定义整型变量
                item.setPlace(list.size(), x, y, 104, 60);  // 设置位置和尺寸
                item.setName("SelectColor row= " + i + " ,col= " + j);  // 设置节点名称
                list.add(item);  // 添加节点到列表
            }
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建Ref-调出列表对话框节点：文件列表
     */
    private static List<ExternalKeysNode> getRightDialogRefRecallListNode(ExternalKeysNode parent, List<ExternalKeysNode> parents, int refNumber, int offset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode spinner = new ExternalKeysNode();  // 创建下拉列表节点
        spinner.setPlace(list.size(), 1205, 306, 400, 60);  // 设置位置和尺寸
        spinner.setName("RefWavSpinner" + refNumber);  // 设置节点名称
        spinner.setParentNode(parent);  // 设置父节点
        spinner.setParentNodes(parents);  // 设置父节点列表
        list.add(spinner);  // 添加节点到列表

        ExternalKeysNode browse = new ExternalKeysNode();  // 创建浏览节点
        browse.setPlace(list.size(), 1645, 306, 120, 60);  // 设置位置和尺寸
        browse.setName("RefWavBrowse" + refNumber);  // 设置节点名称
        browse.setParentNode(parent);  // 设置父节点
        browse.setParentNodes(parents);  // 设置父节点列表
        browse.setChildNodes(null);  // 子节点置空
        list.add(browse);  // 添加节点到列表

        ExternalKeysNode recalls = new ExternalKeysNode();  // 创建节点节点
        recalls.setParentNode(parent);  // 设置父节点
        recalls.setParentNodes(parents);  // 设置父节点列表
        recalls.setPlace(list.size(), 1220, 380, 510, 710);  // 设置位置和尺寸
        recalls.setName("RefWavRecallList" + refNumber);  // 设置节点名称
        recalls.setType(ExternalKeysNode.TYPE_REFRECALL_PROGRESS);  // 类型:TYPE_REFRECALL_PROGRESS
        list.add(recalls);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Ref-CSV文件列表节点
     */
    private static List<ExternalKeysNode> getRightSlipRefCsvListNode(ExternalKeysNode parent, List<ExternalKeysNode> parents, int refNumber, int offset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        ExternalKeysNode spinner = new ExternalKeysNode();  // 创建下拉列表节点
        spinner.setPlace(list.size(), 1205, 306, 400, 60);  // 设置位置和尺寸
        spinner.setName("RefCsvSpinner" + refNumber);  // 设置节点名称
        spinner.setParentNode(parent);  // 设置父节点
        spinner.setParentNodes(parents);  // 设置父节点列表
        list.add(spinner);  // 添加节点到列表

        ExternalKeysNode browse = new ExternalKeysNode();  // 创建浏览节点
        browse.setPlace(list.size(), 1645, 306, 120, 60);  // 设置位置和尺寸
        browse.setName("RefCsvBrowse" + refNumber);  // 设置节点名称
        browse.setParentNode(parent);  // 设置父节点
        browse.setParentNodes(parents);  // 设置父节点列表
        browse.setChildNodes(null);  // 子节点置空
        list.add(browse);  // 添加节点到列表


        ExternalKeysNode recalls = new ExternalKeysNode();  // 创建节点节点
        recalls.setParentNode(parent);  // 设置父节点
        recalls.setParentNodes(parents);  // 设置父节点列表
        recalls.setPlace(list.size(), 1220, 380, 510, 710);  // 设置位置和尺寸
        recalls.setName("RefCsvlist" + refNumber);  // 设置节点名称
        recalls.setChildNodes(getRightSlipCsvDialogNode(recalls, list, refNumber, offset));  // 设置子节点列表
        recalls.setType(ExternalKeysNode.TYPE_CSV_LIST);  // 类型:TYPE_CSV_LIST
        recalls.setDialog(ExternalKeysNode.DIALOG_LOAD_CSV);  // 关联弹窗
        list.add(recalls);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建Ref-CSV文件对话框节点
     */
    private static List<ExternalKeysNode> getRightSlipCsvDialogNode(ExternalKeysNode parent, List<ExternalKeysNode> parents, int refNumber, int offset) {
//        File[] files = SaveManage.getInstance().getFliesFromCurRef(Tools.SaveDir_CSVWAVE);
        offset = 0;//弹窗相对屏幕居中，不需要偏移
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < ChannelFactory.getRefChNums(); i++) {  // 遍历通道创建节点
            ExternalKeysNode topRef = new ExternalKeysNode();  // 创建节点节点
            topRef.setParentNode(parent);  // 设置父节点
            topRef.setParentNodes(parents);  // 设置父节点列表
            topRef.setPlace(list.size(), 310 + 170 * i, 395 + offset, 120, 50);  // 设置位置和尺寸
            topRef.setName("TopRef" + (i + 1));  // 设置节点名称
            list.add(topRef);  // 添加节点到列表
        }

        for (int i = 0; i < ChannelFactory.getChNums(); i++) {  // 遍历通道创建节点
            ExternalKeysNode bottomCh = new ExternalKeysNode();  // 创建节点节点
            bottomCh.setParentNode(parent);  // 设置父节点
            bottomCh.setParentNodes(parents);  // 设置父节点列表
            bottomCh.setPlace(list.size(), 310 + 170 * i, 499 + offset, 90, 50);  // 设置位置和尺寸
            bottomCh.setName("BottomCh" + (i + 1));  // 设置节点名称
            list.add(bottomCh);  // 添加节点到列表
        }

        for (int i = 0; i < ChannelFactory.getChNums(); i++) {  // 遍历通道创建节点
            ExternalKeysNode bottomMath = new ExternalKeysNode();  // 创建节点节点
            bottomMath.setParentNode(parent);  // 设置父节点
            bottomMath.setParentNodes(parents);  // 设置父节点列表
            bottomMath.setPlace(list.size(), 310 + 170 * i, 570 + offset, 80, 50);  // 设置位置和尺寸
            bottomMath.setName("BottomMath" + (i + 1));  // 设置节点名称
            list.add(bottomMath);  // 添加节点到列表
        }

        for (int i = 0; i < ChannelFactory.getChNums(); i++) {  // 遍历通道创建节点
            ExternalKeysNode bottomRef = new ExternalKeysNode();  // 创建节点节点
            bottomRef.setParentNode(parent);  // 设置父节点
            bottomRef.setParentNodes(parents);  // 设置父节点列表
            bottomRef.setPlace(list.size(), 308 + 170 * i, 641 + offset, 80, 50);  // 设置位置和尺寸
            bottomRef.setName("BottomRef" + (i + 1));  // 设置节点名称
            list.add(bottomRef);  // 添加节点到列表
        }

        ExternalKeysNode btnOk = new ExternalKeysNode();  // 创建节点节点
        btnOk.setParentNode(parent);  // 设置父节点
        btnOk.setParentNodes(parents);  // 设置父节点列表
        btnOk.setPlace(list.size(), 882, 745, 155 + offset, 60);  // 设置位置和尺寸
        btnOk.setName("btnOk");  // 设置节点名称
        list.add(btnOk);  // 添加节点到列表

        return list;  // 返回节点列表
    }


    //endregion

    //region RightSlipSerials
    /**
     * 构建右侧滑动栏-串行总线节点列表：UART/LIN/CAN/SPI/I2C/429/1553B
     */
    private static List<ExternalKeysNode> getRightSlipSerialsDetailNodeList(int serialsNumber, int offset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        offset = -7;
//        ExternalKeysNode serialsCheck = new ExternalKeysNode();
//        serialsCheck.setPlace(list.size(), 1703, 94, 72, 42);
//        serialsCheck.setName("serialsCheck" + serialsNumber);
//        serialsCheck.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);
//        serialsCheck.setCurListSelect(CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber) + 1);
//        list.add(serialsCheck);

        ExternalKeysNode math = new ExternalKeysNode();  // 创建节点节点
        math.setPlace(list.size(), 982, 216 + offset, 185, 70);  // 设置位置和尺寸
        math.setName("Serials-Math" + serialsNumber);  // 设置节点名称
        math.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(math);  // 添加节点到列表

        ExternalKeysNode ref = new ExternalKeysNode();  // 创建节点节点
        ref.setPlace(list.size(), 1167, 216 + offset, 185, 70);  // 设置位置和尺寸
        ref.setName("Serials-Ref" + serialsNumber);  // 设置节点名称
        ref.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(ref);  // 添加节点到列表

        ExternalKeysNode serialBus = new ExternalKeysNode();  // 创建节点节点
        serialBus.setPlace(list.size(), 1352, 216 + offset, 185, 70);  // 设置位置和尺寸
        serialBus.setName("Serials-SerialBus" + serialsNumber);  // 设置节点名称
        serialBus.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(serialBus);  // 添加节点到列表


        ExternalKeysNode delete = new ExternalKeysNode();  // 创建节点节点
        delete.setPlace(list.size(), 1664, 227 + offset, 108, 48);  // 设置位置和尺寸
        delete.setName("Serials-delete" + serialsNumber);  // 设置节点名称
        delete.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        delete.setVisible(false);  // 设为不可见
        list.add(delete);  // 添加节点到列表

        ExternalKeysNode add = new ExternalKeysNode();  // 创建右选择2节点
        add.setPlace(list.size(), 1718, 224 + offset, 54, 54);  // 设置位置和尺寸
        add.setName("Serials-Add" + serialsNumber);  // 设置节点名称
        add.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(add);  // 添加节点到列表

        ExternalKeysNode uart = new ExternalKeysNode();  // 创建UART节点
        uart.setPlace(list.size(), 1020, 316 + offset, 108, 54);  // 设置位置和尺寸
        uart.setName("rightSlipSerialsUart" + serialsNumber);  // 设置节点名称
        uart.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        uart.setChildNodes(getRightSlipSerialsUartDetailNodeList(uart, list, offset));  // 设置子节点列表
        list.add(uart);  // 添加节点到列表

        ExternalKeysNode lin = new ExternalKeysNode();  // 创建LIN节点
        lin.setPlace(list.size(), 1020, 384 + offset, 108, 54);  // 设置位置和尺寸
        lin.setName("rightSlipSerialsLin" + serialsNumber);  // 设置节点名称
        lin.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        lin.setChildNodes(getRightSlipSerialsLinDetailNodeList(lin, list, offset));  // 设置子节点列表
        list.add(lin);  // 添加节点到列表

        ExternalKeysNode can = new ExternalKeysNode();  // 创建CAN节点
        can.setPlace(list.size(), 1020, 452 + offset, 108, 54);  // 设置位置和尺寸
        can.setName("rightSlipSerialsCan" + serialsNumber);  // 设置节点名称
        can.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        can.setChildNodes(getRightSlipSerialsCanDetailNodeList(can, list, offset));  // 设置子节点列表
        list.add(can);  // 添加节点到列表

        ExternalKeysNode spi = new ExternalKeysNode();  // 创建SPI节点
        spi.setPlace(list.size(), 1020, 520 + offset, 108, 54);  // 设置位置和尺寸
        spi.setName("rightSlipSerialsSpi" + serialsNumber);  // 设置节点名称
        spi.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        spi.setChildNodes(getRightSlipSerialsSpiDetailNodeList(spi, list, offset));  // 设置子节点列表
        list.add(spi);  // 添加节点到列表

        ExternalKeysNode i2c = new ExternalKeysNode();  // 创建I2C节点
        i2c.setPlace(list.size(), 1020, 588 + offset, 108, 54);  // 设置位置和尺寸
        i2c.setName("rightSlipSerialsI2c" + serialsNumber);  // 设置节点名称
        i2c.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        i2c.setChildNodes(getRightSlipSerialsI2cDetailNodeList(i2c, list, offset));  // 设置子节点列表
        list.add(i2c);  // 添加节点到列表

        ExternalKeysNode m429 = new ExternalKeysNode();  // 创建ARINC429节点
        m429.setPlace(list.size(), 1020, 656 + offset, 108, 54);  // 设置位置和尺寸
        m429.setName("rightSlipSerialsM429" + serialsNumber);  // 设置节点名称
        m429.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        m429.setChildNodes(getRightSlipSerialsM429DetailNodeList(m429, list, offset));  // 设置子节点列表
        list.add(m429);  // 添加节点到列表

        ExternalKeysNode m1553b = new ExternalKeysNode();  // 创建1553B节点
        m1553b.setPlace(list.size(), 1020, 724 + offset, 108, 54);  // 设置位置和尺寸
        m1553b.setName("rightSlipSerialsM1553b" + serialsNumber);  // 设置节点名称
        m1553b.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        m1553b.setChildNodes(getRightSlipSerialsM1553bDetailNodeList(m1553b, list, offset));  // 设置子节点列表
        list.add(m1553b);  // 添加节点到列表

        ExternalKeysNode serialsCheck = new ExternalKeysNode();  // 创建节点节点
        serialsCheck.setPlace(list.size(), 1038, 892 + offset, 72, 36);  // 设置位置和尺寸
        serialsCheck.setName("serialsCheck" + serialsNumber);  // 设置节点名称
        serialsCheck.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        list.add(serialsCheck);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建串行总线-UART节点：波特率/数据位/停止位/校验
     */
    private static List<ExternalKeysNode> getRightSlipSerialsUartDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int offset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] levels = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsUartIdle);  // 从资源文件获取字符串数组
        String[] checks = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsUartCheck);  // 从资源文件获取字符串数组
        String[] bits = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsUartBits);  // 从资源文件获取字符串数组
        String[] displays = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsUartDisplay);  // 从资源文件获取字符串数组
        String[] baudRates = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsUartBaudRate);  // 从资源文件获取字符串数组
        for (int i = 0; i < channels.length; i++) { //108 54 item的宽和高 增量为间距
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 1297 + 122 * (i % 4), 317 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < levels.length; i++) {  // 循环创建节点
            ExternalKeysNode level = new ExternalKeysNode();  // 创建电平节点
            level.setParentNode(parent);  // 设置父节点
            level.setParentNodes(parents);  // 设置父节点列表
            level.setPlace(list.size(), 1555 + 108 * i, 469 + offset, 108, 54);  // 设置位置和尺寸
            level.setName(levels[i]);  // 设置节点名称
            list.add(level);  // 添加节点到列表
        }
        for (int i = 0; i < checks.length; i++) {  // 循环创建节点
            ExternalKeysNode check = new ExternalKeysNode();  // 创建节点节点
            check.setParentNode(parent);  // 设置父节点
            check.setParentNodes(parents);  // 设置父节点列表
            check.setPlace(list.size(), 1419 + 122 * i, 553 + offset, 108, 54);  // 设置位置和尺寸
            check.setName(checks[i]);  // 设置节点名称
            list.add(check);  // 添加节点到列表
        }
        ExternalKeysNode bit1 = new ExternalKeysNode();  // 创建节点节点
        bit1.setParentNode(parent);  // 设置父节点
        bit1.setParentNodes(parents);  // 设置父节点列表
        bit1.setPlace(list.size(), 1663, 637 + offset, 108, 54);  // 设置位置和尺寸
        bit1.setName(bits[0]);  // 设置节点名称
        list.add(bit1);  // 添加节点到列表
        for (int i = 0; i < bits.length - 1; i++) {  // 循环创建节点
            ExternalKeysNode bit = new ExternalKeysNode();  // 创建节点节点
            bit.setParentNode(parent);  // 设置父节点
            bit.setParentNodes(parents);  // 设置父节点列表
            bit.setPlace(list.size(), 1297 + 122 * (i % 4), 705 + (i / 4) * 68 + offset, 108, 54);  // 设置位置和尺寸
            bit.setName(bits[i + 1]);  // 设置节点名称
            list.add(bit);  // 添加节点到列表
        }

        for (int i = 0; i < baudRates.length; i++) {  // 循环创建节点
            ExternalKeysNode baudRate = new ExternalKeysNode();  // 创建节点节点
            baudRate.setParentNode(parent);  // 设置父节点
            baudRate.setParentNodes(parents);  // 设置父节点列表
            baudRate.setPlace(list.size(), 1297 + 122 * (i % 4), 789 + (i / 4) * 68 + offset, 108, 54);  // 设置位置和尺寸
            baudRate.setName(baudRates[i]);  // 设置节点名称
            list.add(baudRate);  // 添加节点到列表
        }
        ExternalKeysNode userDefine = new ExternalKeysNode();  // 创建节点节点
        userDefine.setParentNode(parent);  // 设置父节点
        userDefine.setParentNodes(parents);  // 设置父节点列表
        userDefine.setChildNodes(getNumberKeyBoardNodeList(userDefine, list, true));  // 设置子节点列表
        userDefine.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        userDefine.setPlace(list.size(), 1541, 925 + offset, 230, 54);  // 设置位置和尺寸
        userDefine.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
        userDefine.setName("userDefine");  // 设置节点名称
        userDefine.getChildNodes().get(9).setVisible(false);  // 设为不可见
        userDefine.getChildNodes().get(10).setVisible(false);  // 设为不可见
        userDefine.getChildNodes().get(15).setVisible(false);  // 设为不可见
        userDefine.getChildNodes().get(16).setVisible(false);  // 设为不可见
        userDefine.getChildNodes().get(19).setVisible(false);  // 设为不可见
        userDefine.getChildNodes().get(20).setVisible(false);  // 设为不可见
        userDefine.getChildNodes().get(21).setVisible(false);  // 设为不可见
        list.add(userDefine);  // 添加节点到列表

        for (int i = 0; i < displays.length; i++) {  // 循环创建节点
            ExternalKeysNode display = new ExternalKeysNode();  // 创建显示节点
            display.setParentNode(parent);  // 设置父节点
            display.setParentNodes(parents);  // 设置父节点列表
            display.setPlace(list.size(), 1419 + 122 * i, 1009 + offset, 108, 54);  // 设置位置和尺寸
            display.setName(displays[i]);  // 设置节点名称
            list.add(display);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    private static List<ExternalKeysNode> getRightDialogBaudRateDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < 11; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 1312 + (i % 4) * 122, 920 + (i / 4) * 68, 108, 54);  // 设置位置和尺寸
            node.setName("baudRateChild:index" + i);  // 设置节点名称
            if (i != 10) {  // 条件判断
                node.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK);  // 类型:点击确认返回
            } else {  // 否则
                node.setPlace(list.size(), 1556, 1056, 230, 54);  // 设置位置和尺寸
                node.setType(ExternalKeysNode.TYPE_RECEIVE_MSG);  // 类型:接收消息
                node.setChildNodes(getNumberKeyBoardNodeList(node, list, true));  // 设置子节点列表
                node.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
                node.getChildNodes().get(9).setVisible(false);  // 设为不可见
                node.getChildNodes().get(10).setVisible(false);  // 设为不可见
                node.getChildNodes().get(15).setVisible(false);  // 设为不可见
                node.getChildNodes().get(16).setVisible(false);  // 设为不可见
                node.getChildNodes().get(19).setVisible(false);  // 设为不可见
                node.getChildNodes().get(20).setVisible(false);  // 设为不可见
                node.getChildNodes().get(21).setVisible(false);  // 设为不可见
            }
            list.add(node);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建串行总线-LIN节点：波特率/其他配置
     */
    private static List<ExternalKeysNode> getRightSlipSerialsLinDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int offset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] linTypes = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsLinType);  // 从资源文件获取字符串数组
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] levels = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsLinIdle);  // 从资源文件获取字符串数组
        String[] baudRates = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsLinBaudRate);  // 从资源文件获取字符串数组
        for (int i = 0; i < linTypes.length; i++) {  // 循环创建节点
            ExternalKeysNode linType = new ExternalKeysNode();  // 创建节点节点
            linType.setParentNode(parent);  // 设置父节点
            linType.setParentNodes(parents);  // 设置父节点列表
            linType.setPlace(list.size(), 1419 + 122 * i, 317 + offset, 108, 54);  // 设置位置和尺寸
            linType.setName(linTypes[i]);  // 设置节点名称
            list.add(linType);  // 添加节点到列表
        }
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 1297 + 122 * (i % 4), 401 + (i / 4) * 68 + offset, 108, 54);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < levels.length; i++) {  // 循环创建节点
            ExternalKeysNode level = new ExternalKeysNode();  // 创建电平节点
            level.setParentNode(parent);  // 设置父节点
            level.setParentNodes(parents);  // 设置父节点列表
            level.setPlace(list.size(), 1562 + 108 * i, 553 + offset, 108, 54);  // 设置位置和尺寸
            level.setName(levels[i]);  // 设置节点名称
            list.add(level);  // 添加节点到列表
        }
        for (int i = 0; i < baudRates.length; i++) {  // 循环创建节点
            ExternalKeysNode baudRate = new ExternalKeysNode();  // 创建节点节点
            baudRate.setParentNode(parent);  // 设置父节点
            baudRate.setParentNodes(parents);  // 设置父节点列表
            baudRate.setPlace(list.size(), 1297 + 122 * i, 637 + offset, 108, 54);  // 设置位置和尺寸
            baudRate.setName(baudRates[i]);  // 设置节点名称
            list.add(baudRate);  // 添加节点到列表
        }
        ExternalKeysNode userDefine = new ExternalKeysNode();  // 创建节点节点
        userDefine.setParentNode(parent);  // 设置父节点
        userDefine.setParentNodes(parents);  // 设置父节点列表
        userDefine.setChildNodes(getNumberKeyBoardNodeList(userDefine, list, true));  // 设置子节点列表
        userDefine.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        userDefine.setPlace(list.size(), 1541, 705 + offset, 230, 54);  // 设置位置和尺寸
        userDefine.setName("userDefine");  // 设置节点名称
        userDefine.getChildNodes().get(9).setVisible(false);  // 设为不可见
        userDefine.getChildNodes().get(10).setVisible(false);  // 设为不可见
        userDefine.getChildNodes().get(15).setVisible(false);  // 设为不可见
        userDefine.getChildNodes().get(16).setVisible(false);  // 设为不可见
        userDefine.getChildNodes().get(19).setVisible(false);  // 设为不可见
        userDefine.getChildNodes().get(20).setVisible(false);  // 设为不可见
        userDefine.getChildNodes().get(21).setVisible(false);  // 设为不可见
        list.add(userDefine);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建串行总线-CAN节点：波特率/ID类型/数据
     */
    private static List<ExternalKeysNode> getRightSlipSerialsCanDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int offset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] signals = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsCanSignal);  // 从资源文件获取字符串数组
        String[] bits = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsCanBaudRate);  // 从资源文件获取字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 1297 + 122 * (i % 4), 317 + (i / 4) * 68 + offset, 108, 54);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 2; i < signals.length + 2; i++) {  // 循环创建节点
            ExternalKeysNode signal = new ExternalKeysNode();  // 创建节点节点
            signal.setParentNode(parent);  // 设置父节点
            signal.setParentNodes(parents);  // 设置父节点列表
            signal.setPlace(list.size(), 1297 + 122 * (i % 4), i < 4 ? 469 + offset : 537 + offset, 108, 54);  // 设置位置和尺寸
            signal.setName(signals[i - 2]);  // 设置节点名称
            list.add(signal);  // 添加节点到列表
        }

        ExternalKeysNode brPercent = new ExternalKeysNode();  // 创建节点节点
        brPercent.setParentNode(parent);  // 设置父节点
        brPercent.setParentNodes(parents);  // 设置父节点列表
        brPercent.setChildNodes(getNumberKeyBoardNodeList(brPercent, list, true));  // 设置子节点列表
        brPercent.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        brPercent.setPlace(list.size(), 1663, 621 + offset, 108, 54);  // 设置位置和尺寸
        brPercent.setName("brPercent");  // 设置节点名称
        brPercent.getChildNodes().get(3).setVisible(false);  // 设为不可见
        brPercent.getChildNodes().get(4).setVisible(false);  // 设为不可见
        brPercent.getChildNodes().get(5).setVisible(false);  // 设为不可见
        brPercent.getChildNodes().get(9).setVisible(false);  // 设为不可见
        brPercent.getChildNodes().get(10).setVisible(false);  // 设为不可见
        brPercent.getChildNodes().get(15).setVisible(false);  // 设为不可见
        brPercent.getChildNodes().get(16).setVisible(false);  // 设为不可见
        brPercent.getChildNodes().get(19).setVisible(false);  // 设为不可见
        brPercent.getChildNodes().get(20).setVisible(false);  // 设为不可见
        brPercent.getChildNodes().get(21).setVisible(false);  // 设为不可见
        list.add(brPercent);  // 添加节点到列表

        for (int i = 0; i < 3; i++) {  // 循环创建节点
            ExternalKeysNode br = new ExternalKeysNode();  // 创建节点节点
            br.setParentNode(parent);  // 设置父节点
            br.setParentNodes(parents);  // 设置父节点列表
            br.setPlace(list.size(), 1297 + (i * 122), 689 + offset, 108, 54);  // 设置位置和尺寸
            br.setName("baudRate");  // 设置节点名称
            list.add(br);  // 添加节点到列表
        }
        ExternalKeysNode brDefault = new ExternalKeysNode();  // 创建节点节点
        brDefault.setParentNode(parent);  // 设置父节点
        brDefault.setParentNodes(parents);  // 设置父节点列表
        brDefault.setChildNodes(getNumberKeyBoardNodeList(brDefault, list, true));  // 设置子节点列表
        brDefault.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        brDefault.setPlace(list.size(), 1663, 689 + offset, 108, 54);  // 设置位置和尺寸
        brDefault.setName("brDefault");  // 设置节点名称
        brDefault.getChildNodes().get(9).setVisible(false);  // 设为不可见
        brDefault.getChildNodes().get(10).setVisible(false);  // 设为不可见
        brDefault.getChildNodes().get(15).setVisible(false);  // 设为不可见
        brDefault.getChildNodes().get(16).setVisible(false);  // 设为不可见
        brDefault.getChildNodes().get(19).setVisible(false);  // 设为不可见
        brDefault.getChildNodes().get(20).setVisible(false);  // 设为不可见
        brDefault.getChildNodes().get(21).setVisible(false);  // 设为不可见
        list.add(brDefault);  // 添加节点到列表

        if (ScopeConfig.getConfig().isBusEnable(Property.BUS_CAN_FD)) {  // 条件判断
            ExternalKeysNode fdBrPercent = new ExternalKeysNode();  // 创建节点节点
            fdBrPercent.setParentNode(parent);  // 设置父节点
            fdBrPercent.setParentNodes(parents);  // 设置父节点列表
            fdBrPercent.setChildNodes(getNumberKeyBoardNodeList(fdBrPercent, list, true));  // 设置子节点列表
            fdBrPercent.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
            fdBrPercent.setPlace(list.size(), 1663, 773 + offset, 108, 54);  // 设置位置和尺寸
            fdBrPercent.setName("fdBrPercent");  // 设置节点名称
            fdBrPercent.getChildNodes().get(3).setVisible(false);  // 设为不可见
            fdBrPercent.getChildNodes().get(4).setVisible(false);  // 设为不可见
            fdBrPercent.getChildNodes().get(5).setVisible(false);  // 设为不可见
            fdBrPercent.getChildNodes().get(9).setVisible(false);  // 设为不可见
            fdBrPercent.getChildNodes().get(10).setVisible(false);  // 设为不可见
            fdBrPercent.getChildNodes().get(15).setVisible(false);  // 设为不可见
            fdBrPercent.getChildNodes().get(16).setVisible(false);  // 设为不可见
            fdBrPercent.getChildNodes().get(19).setVisible(false);  // 设为不可见
            fdBrPercent.getChildNodes().get(20).setVisible(false);  // 设为不可见
            fdBrPercent.getChildNodes().get(21).setVisible(false);  // 设为不可见
            list.add(fdBrPercent);  // 添加节点到列表

            for (int i = 0; i < 3; i++) {  // 循环创建节点
                ExternalKeysNode fdBr = new ExternalKeysNode();  // 创建节点节点
                fdBr.setParentNode(parent);  // 设置父节点
                fdBr.setParentNodes(parents);  // 设置父节点列表
                fdBr.setPlace(list.size(), 1297 + (i * 122), 841 + offset, 108, 54);  // 设置位置和尺寸
                fdBr.setName("fdBr");  // 设置节点名称
                list.add(fdBr);  // 添加节点到列表
            }
            ExternalKeysNode fdBrDefault = new ExternalKeysNode();  // 创建节点节点
            fdBrDefault.setParentNode(parent);  // 设置父节点
            fdBrDefault.setParentNodes(parents);  // 设置父节点列表
            fdBrDefault.setChildNodes(getNumberKeyBoardNodeList(fdBrDefault, list, true));  // 设置子节点列表
            fdBrDefault.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
            fdBrDefault.setPlace(list.size(), 1663, 841 + offset, 108, 54);  // 设置位置和尺寸
            fdBrDefault.setName("brDefault");  // 设置节点名称
            fdBrDefault.getChildNodes().get(9).setVisible(false);  // 设为不可见
            fdBrDefault.getChildNodes().get(10).setVisible(false);  // 设为不可见
            fdBrDefault.getChildNodes().get(15).setVisible(false);  // 设为不可见
            fdBrDefault.getChildNodes().get(16).setVisible(false);  // 设为不可见
            fdBrDefault.getChildNodes().get(19).setVisible(false);  // 设为不可见
            fdBrDefault.getChildNodes().get(20).setVisible(false);  // 设为不可见
            fdBrDefault.getChildNodes().get(21).setVisible(false);  // 设为不可见
            list.add(fdBrDefault);  // 添加节点到列表

            ExternalKeysNode Iso = new ExternalKeysNode();  // 创建节点节点
            Iso.setParentNode(parent);  // 设置父节点
            Iso.setParentNodes(parents);  // 设置父节点列表
            Iso.setPlace(list.size(), 1562, 925 + offset, 108, 54);  // 设置位置和尺寸
            Iso.setName("Iso");  // 设置节点名称
            list.add(Iso);  // 添加节点到列表

            ExternalKeysNode noIso = new ExternalKeysNode();  // 创建节点节点
            noIso.setParentNode(parent);  // 设置父节点
            noIso.setParentNodes(parents);  // 设置父节点列表
            noIso.setPlace(list.size(), 1670, 925 + offset, 108, 54);  // 设置位置和尺寸
            noIso.setName("noIso");  // 设置节点名称
            list.add(noIso);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建串行总线-SPI节点：时钟/数据配置
     */
    private static List<ExternalKeysNode> getRightSlipSerialsSpiDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int offset) {
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] bits = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsSpiBits);  // 从资源文件获取字符串数组
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode clockCheckLeft = new ExternalKeysNode();  // 创建节点节点
        clockCheckLeft.setParentNode(parent);  // 设置父节点
        clockCheckLeft.setParentNodes(parents);  // 设置父节点列表
        clockCheckLeft.setPlace(list.size(), 1555, 317 + offset, 108, 54);  // 设置位置和尺寸
        clockCheckLeft.setName("clockCheckLeft");  // 设置节点名称
        list.add(clockCheckLeft);  // 添加节点到列表
        ExternalKeysNode clockCheckRight = new ExternalKeysNode();  // 创建节点节点
        clockCheckRight.setParentNode(parent);  // 设置父节点
        clockCheckRight.setParentNodes(parents);  // 设置父节点列表
        clockCheckRight.setPlace(list.size(), 1663, 317 + offset, 108, 54);  // 设置位置和尺寸
        clockCheckRight.setName("clockCheckRight");  // 设置节点名称
        list.add(clockCheckRight);  // 添加节点到列表
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode clock = new ExternalKeysNode();  // 创建节点节点
            clock.setParentNode(parent);  // 设置父节点
            clock.setParentNodes(parents);  // 设置父节点列表
            clock.setPlace(list.size(), 1297 + 122 * (i % 4), 385 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
            clock.setName(channels[i]);  // 设置节点名称
            list.add(clock);  // 添加节点到列表
        }
        ExternalKeysNode dataCheckLeft = new ExternalKeysNode();  // 创建节点节点
        dataCheckLeft.setParentNode(parent);  // 设置父节点
        dataCheckLeft.setParentNodes(parents);  // 设置父节点列表
        dataCheckLeft.setPlace(list.size(), 1555, 537 + offset, 108, 54);  // 设置位置和尺寸
        dataCheckLeft.setName("dataCheckLeft");  // 设置节点名称
        list.add(dataCheckLeft);  // 添加节点到列表
        ExternalKeysNode dataCheckRight = new ExternalKeysNode();  // 创建节点节点
        dataCheckRight.setParentNode(parent);  // 设置父节点
        dataCheckRight.setParentNodes(parents);  // 设置父节点列表
        dataCheckRight.setPlace(list.size(), 1663, 537 + offset, 108, 54);  // 设置位置和尺寸
        dataCheckRight.setName("dataCheckRight");  // 设置节点名称
        list.add(dataCheckRight);  // 添加节点到列表
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode data = new ExternalKeysNode();  // 创建节点节点
            data.setParentNode(parent);  // 设置父节点
            data.setParentNodes(parents);  // 设置父节点列表
            data.setPlace(list.size(), 1297 + 122 * (i % 4), 605 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
            data.setName(channels[i]);  // 设置节点名称
            list.add(data);  // 添加节点到列表
        }
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4 || GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {  // 判断是否为8通道型号
            ExternalKeysNode csSwitch = new ExternalKeysNode();  // 创建节点节点
            csSwitch.setParentNode(parent);  // 设置父节点
            csSwitch.setParentNodes(parents);  // 设置父节点列表
            csSwitch.setPlace(list.size(), 1290, 757 + offset, 72, 54);  // 设置位置和尺寸
            csSwitch.setName("csSwitch");  // 设置节点名称
            list.add(csSwitch);  // 添加节点到列表
            ExternalKeysNode csCheckLeft = new ExternalKeysNode();  // 创建节点节点
            csCheckLeft.setParentNode(parent);  // 设置父节点
            csCheckLeft.setParentNodes(parents);  // 设置父节点列表
            csCheckLeft.setPlace(list.size(), 1555, 757 + offset, 108, 54);  // 设置位置和尺寸
            csCheckLeft.setName("csCheckLeft");  // 设置节点名称
            list.add(csCheckLeft);  // 添加节点到列表
            ExternalKeysNode csCheckRight = new ExternalKeysNode();  // 创建节点节点
            csCheckRight.setParentNode(parent);  // 设置父节点
            csCheckRight.setParentNodes(parents);  // 设置父节点列表
            csCheckRight.setPlace(list.size(), 1663, 757 + offset, 108, 54);  // 设置位置和尺寸
            csCheckRight.setName("csCheckRight");  // 设置节点名称
            list.add(csCheckRight);  // 添加节点到列表
            for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
                ExternalKeysNode cs = new ExternalKeysNode();  // 创建节点节点
                cs.setParentNode(parent);  // 设置父节点
                cs.setParentNodes(parents);  // 设置父节点列表
                cs.setPlace(list.size(), 1297 + 122 * (i % 4), 825 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
                cs.setName(channels[i]);  // 设置节点名称
                list.add(cs);  // 添加节点到列表
            }
            ExternalKeysNode bit1 = new ExternalKeysNode();  // 创建节点节点
            bit1.setParentNode(parent);  // 设置父节点
            bit1.setParentNodes(parents);  // 设置父节点列表
            bit1.setPlace(list.size(), 1663, 977 + offset, 108, 54);  // 设置位置和尺寸
            bit1.setName(bits[0]);  // 设置节点名称
            list.add(bit1);  // 添加节点到列表
            for (int i = 0; i < bits.length - 1; i++) {  // 循环创建节点
                ExternalKeysNode bit = new ExternalKeysNode();  // 创建节点节点
                bit.setParentNode(parent);  // 设置父节点
                bit.setParentNodes(parents);  // 设置父节点列表
                bit.setPlace(list.size(), 1297 + 122 * (i % 4), 1045 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
                bit.setName(bits[i + 1]);  // 设置节点名称
                list.add(bit);  // 添加节点到列表
            }
        } else {  // 否则
            ExternalKeysNode bit1 = new ExternalKeysNode();  // 创建节点节点
            bit1.setParentNode(parent);  // 设置父节点
            bit1.setParentNodes(parents);  // 设置父节点列表
            bit1.setPlace(list.size(), 1663, 977 + offset, 108, 54);  // 设置位置和尺寸
            bit1.setName(bits[0]);  // 设置节点名称
            list.add(bit1);  // 添加节点到列表
            for (int i = 0; i < bits.length - 1; i++) {  // 循环创建节点
                ExternalKeysNode bit = new ExternalKeysNode();  // 创建节点节点
                bit.setParentNode(parent);  // 设置父节点
                bit.setParentNodes(parents);  // 设置父节点列表
                bit.setPlace(list.size(), 1297 + 122 * (i % 4), 1045 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
                bit.setName(bits[i + 1]);  // 设置节点名称
                list.add(bit);  // 添加节点到列表
            }
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建串行总线-I2C节点：地址/数据配置
     */
    private static List<ExternalKeysNode> getRightSlipSerialsI2cDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int offset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode data = new ExternalKeysNode();  // 创建节点节点
            data.setParentNode(parent);  // 设置父节点
            data.setParentNodes(parents);  // 设置父节点列表
            data.setPlace(list.size(), 1297 + 122 * (i % 4), 317 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
            data.setName(channels[i]);  // 设置节点名称
            list.add(data);  // 添加节点到列表
        }
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode clock = new ExternalKeysNode();  // 创建节点节点
            clock.setParentNode(parent);  // 设置父节点
            clock.setParentNodes(parents);  // 设置父节点列表
            clock.setPlace(list.size(), 1297 + 122 * (i % 4), 469 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
            clock.setName(channels[i]);  // 设置节点名称
            list.add(clock);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建串行总线-ARINC429节点
     */
    private static List<ExternalKeysNode> getRightSlipSerialsM429DetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int offset) {
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] formats = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsM429Format);  // 从资源文件获取字符串数组
        String[] displays = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsM429Display);  // 从资源文件获取字符串数组
        String[] baudRates = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsM429BaudRate);  // 从资源文件获取字符串数组
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 1297 + 122 * (i % 4), 317 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < formats.length; i++) {  // 循环创建节点
            ExternalKeysNode format = new ExternalKeysNode();  // 创建节点节点
            format.setParentNode(parent);  // 设置父节点
            format.setParentNodes(parents);  // 设置父节点列表
            format.setPlace(list.size(), 1299 + 162 * i, 469 + offset, 148, 54);  // 设置位置和尺寸
            format.setName(formats[i]);  // 设置节点名称
            list.add(format);  // 添加节点到列表
        }
        for (int i = 0; i < displays.length; i++) {  // 循环创建节点
            ExternalKeysNode display = new ExternalKeysNode();  // 创建显示节点
            display.setParentNode(parent);  // 设置父节点
            display.setParentNodes(parents);  // 设置父节点列表
            display.setPlace(list.size(), 1555 + 108 * i, 553 + offset, 108, 54);  // 设置位置和尺寸
            display.setName(displays[i]);  // 设置节点名称
            list.add(display);  // 添加节点到列表
        }
        for (int i = 0; i < baudRates.length; i++) {  // 循环创建节点
            ExternalKeysNode baudRate = new ExternalKeysNode();  // 创建节点节点
            baudRate.setParentNode(parent);  // 设置父节点
            baudRate.setParentNodes(parents);  // 设置父节点列表
            baudRate.setPlace(list.size(), 1555 + 108 * i, 637 + offset, 108, 54);  // 设置位置和尺寸
            baudRate.setName(baudRates[i]);  // 设置节点名称
            list.add(baudRate);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建串行总线-1553B节点
     */
    private static List<ExternalKeysNode> getRightSlipSerialsM1553bDetailNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents, int offset) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        String[] displays = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.serialsM1553bDisplay);  // 从资源文件获取字符串数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode source = new ExternalKeysNode();  // 创建触发源节点
            source.setParentNode(parent);  // 设置父节点
            source.setParentNodes(parents);  // 设置父节点列表
            source.setPlace(list.size(), 1297 + 122 * (i % 4), 317 + 68 * (i / 4) + offset, 108, 54);  // 设置位置和尺寸
            source.setName(channels[i]);  // 设置节点名称
            list.add(source);  // 添加节点到列表
        }
        for (int i = 0; i < displays.length; i++) {  // 循环创建节点
            ExternalKeysNode display = new ExternalKeysNode();  // 创建显示节点
            display.setParentNode(parent);  // 设置父节点
            display.setParentNodes(parents);  // 设置父节点列表
            display.setPlace(list.size(), 1555 + 108 * i, 469 + offset, 108, 54);  // 设置位置和尺寸
            display.setName(displays[i]);  // 设置节点名称
            list.add(display);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }
    //endregion

    //region BottomSlip
    /**
     * 构建底部滑动栏节点列表
     */
    private static List<ExternalKeysNode> getBottomSlipNodeList() {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
//        ExternalKeysNode screen = new ExternalKeysNode();
//        screen.setPlace(list.size(), 49, 1104, 138, 92);
//        screen.setName("screen");
//        list.add(screen);
//        ExternalKeysNode zoom = new ExternalKeysNode();
//        zoom.setPlace(list.size(), 235, 1104, 138, 92);
//        zoom.setName("zoom");
//        list.add(zoom);
//        ExternalKeysNode highRefresh = new ExternalKeysNode();
//        highRefresh.setPlace(list.size(), 250, 541, 69, 55);
//        highRefresh.setName("highRefresh");
//        list.add(highRefresh);
        ExternalKeysNode fullMeasure = new ExternalKeysNode();  // 创建节点节点
        fullMeasure.setPlace(list.size(), 49, 1128, 100, 68);  // 设置位置和尺寸
        fullMeasure.setName("fullMeasure");  // 设置节点名称
        list.add(fullMeasure);  // 添加节点到列表

        ExternalKeysNode serialsWord = new ExternalKeysNode();  // 创建节点节点
        serialsWord.setPlace(list.size(), 197, 1128, 100, 68);  // 设置位置和尺寸
        serialsWord.setName("serialsWord");  // 设置节点名称
        list.add(serialsWord);  // 添加节点到列表

//        ExternalKeysNode centerMenu = new ExternalKeysNode();
//        centerMenu.setPlace(list.size(), 450, 541, 69, 55);
////        centerMenu.setChildNodes(getCenterMenuNodeList(centerMenu, list));
//        centerMenu.setName("centerMenu");
//        list.add(centerMenu);

        ExternalKeysNode segment = new ExternalKeysNode();  // 创建节点节点
        segment.setPlace(list.size(), 345, 1128, 100, 68);  // 设置位置和尺寸
        segment.setName("segment");  // 设置节点名称
        list.add(segment);  // 添加节点到列表

        ExternalKeysNode adjustZero = new ExternalKeysNode();  // 创建节点节点
        adjustZero.setPlace(list.size(), 493, 1128, 100, 68);  // 设置位置和尺寸
        adjustZero.setName("adjustZero");  // 设置节点名称
        list.add(adjustZero);  // 添加节点到列表


        ExternalKeysNode home = new ExternalKeysNode();  // 创建节点节点
        home.setPlace(list.size(), 641, 1128, 100, 68);  // 设置位置和尺寸
        home.setName("home");  // 设置节点名称
        list.add(home);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    //界面中央菜单
    /**
     * 构建中心菜单节点列表
     */
    private static List<ExternalKeysNode> getCenterMenuNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < 5; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 10, 10 + 59 * i, 65, 59);  // 设置位置和尺寸
            if (i == 3) {  // 第四项特殊处理
                node.setChildNodes(getDialogMenuHalfNodeList(node, list));  // 设置子节点列表
            }
            node.setName("centerMenu:index" + i);  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    //界面中央50%页面
    /**
     * 构建半屏对话框菜单节点列表
     */
    private static List<ExternalKeysNode> getDialogMenuHalfNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < 4; i++) {  // 循环创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 154 + 76 * i, 398, 76, 77);  // 设置位置和尺寸
            node.setName("DialogMenuHalf:index" + list.size());  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }
        String[] channels = GlobalVar.get().getChannelsName();  // 获取通道名称数组
        for (int i = 0; i < channels.length; i++) {  // 遍历通道创建节点
            ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
            node.setParentNode(parent);  // 设置父节点
            node.setParentNodes(parents);  // 设置父节点列表
            node.setPlace(list.size(), 485 + 82 * (i % 2), i < 2 ? 398 : 439, 60, 36);  // 设置位置和尺寸
            node.setName("DialogMenuHalf:index" + list.size());  // 设置节点名称
            list.add(node);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    //endregion

    //region 界面中央通道选择框
    /**
     * 构建中心区域通道节点列表
     */
    private static List<ExternalKeysNode> getCenterChannelsNodeList() {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        for (int i = 0; i < 9; i++) {  // 循环创建节点
            ExternalKeysNode channel = new ExternalKeysNode();  // 创建通道节点
            channel.setPlace(list.size(), 15 + 101 * i, 15, 86, 60);  // 设置位置和尺寸
            channel.setName("Channel:index" + i);  // 设置节点名称
            list.add(channel);  // 添加节点到列表
        }
        return list;  // 返回节点列表
    }

    /**
     * 构建中心区域通道节点列表
     */
    private static List<ExternalKeysNode> getCenterChannelsNodeList(MainLayoutCenterChannel channelLayout) {
        List<ControlBean> rects = channelLayout.getChanRect();


        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        String[] arrays = App.get().getResources().getStringArray(R.array.popArrayAllChannel);  // 从资源文件获取字符串数组
        int positionX = (int) channelLayout.getX();  // 定义整型变量
        int positionY = (int) channelLayout.getY();  // 定义整型变量

        for (int i = 0; i < arrays.length; i++) {  // 循环创建节点
            ExternalKeysNode channel = new ExternalKeysNode();  // 创建通道节点
            int x = positionX + 22 + 106 * (i % 8);  // 定义整型变量
            int y = positionY + 12 + 68 * (i / 8);  // 定义整型变量
            channel.setPlace(list.size(), x, y, 90, 60);  // 设置位置和尺寸
            channel.setVisible(rects.get(i).isVisible());  // 设置可见性
            channel.setName("CenterChannelIndex:" + i);  // 设置节点名称
            list.add(channel);  // 添加节点到列表
        }

//        for(int i=0;i<rects.size();i++){
//            ExternalKeysNode channel = new ExternalKeysNode();
//            Rect rect=rects.get(i).getRect();
//            String name=rects.get(i).getName();
//            boolean visible=rects.get(i).isVisible();
//            channel.setPlace(list.size(), rect.left,rect.top,rect.width(),rect.height());
//            channel.setName(name);
//            channel.setVisible(visible);
//            list.add(channel);
//        }
        return list;  // 返回节点列表
    }

    /**
     * 构建"确定/取消"按钮节点列表
     */
    private static List<ExternalKeysNode> getOkCancelNodeList() {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode ok = new ExternalKeysNode();  // 创建节点节点
        ok.setPlace(list.size(), 980, 620, 155, 60);  // 设置位置和尺寸
        ok.setName("dialogOkCancelOk");  // 设置节点名称
        list.add(ok);  // 添加节点到列表
        ExternalKeysNode cancel = new ExternalKeysNode();  // 创建节点节点
        cancel.setPlace(list.size(), 785, 620, 155, 60);  // 设置位置和尺寸
        cancel.setName("dialogOkCancelCancel");  // 设置节点名称
        list.add(cancel);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建"确定"按钮节点列表
     */
    private static List<ExternalKeysNode> getOkNodeList() {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode ok = new ExternalKeysNode();  // 创建节点节点
        ok.setPlace(list.size(), 880, 620, 160, 60);  // 设置位置和尺寸
        ok.setName("dialogOkCancelOk");  // 设置节点名称
        list.add(ok);  // 添加节点到列表
        return list;  // 返回节点列表
    }
    //endregion

    //region
    /**
     * 构建串行总线协议字节点列表
     */
    private static List<ExternalKeysNode> getSerialsWordNodeList(int topSlipOffset) {
        //显示个数
        int showNums = getSerialsOpenState();  // 定义整型变量
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        if (showNums != 0) {  // 条件判断
            int width = 1800 / showNums;  // 定义整型变量
            for (int i = 0; i < showNums; i++) {  // 循环创建节点
                ExternalKeysNode node = new ExternalKeysNode();  // 创建节点节点
                node.setPlace(list.size(), width * i, 40 + topSlipOffset, width, 70);  // 设置位置和尺寸
                list.add(node);  // 添加节点到列表
            }
        }

        ExternalKeysNode clear = new ExternalKeysNode();  // 创建清除节点
        clear.setPlace(list.size(), 1649, 1047 + topSlipOffset, 120, 60);  // 设置位置和尺寸
        clear.setName("clear");  // 设置节点名称
        list.add(clear);  // 添加节点到列表
        return list;  // 返回节点列表
    }


    private static int getSerialsOpenState() {
        int finalShowNUms = 0;  // 定义整型变量

        boolean s1Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S1);  // 定义布尔变量
        boolean s2Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S2);  // 定义布尔变量
        boolean s3Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S3);  // 定义布尔变量
        boolean s4Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S4);  // 定义布尔变量

        int tempNums = 0;  // 定义整型变量
        if (s1Select) {  // 条件判断
            tempNums++;
        }
        if (s2Select) {  // 条件判断
            tempNums++;
        }
        if (s3Select) {  // 条件判断
            tempNums++;
        }
        if (s4Select) {  // 条件判断
            tempNums++;
        }
        int mixShow = tempNums > 1 ? 1 : 0;//是否显示组合项

        boolean s1Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);  // 定义布尔变量
        boolean s2Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);  // 定义布尔变量
        boolean s3Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);  // 定义布尔变量
        boolean s4Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);  // 定义布尔变量
        if (s1Open) {  // 条件判断
            finalShowNUms++;
        }
        if (s2Open) {  // 条件判断
            finalShowNUms++;
        }
        if (s3Open) {  // 条件判断
            finalShowNUms++;
        }
        if (s4Open) {  // 条件判断
            finalShowNUms++;
        }
        finalShowNUms += mixShow;
        return finalShowNUms;  // 返回结果
    }

    //endregion

    /**
     * 构建汽车总线节点列表
     */
    private static List<ExternalKeysNode> getAutoMotiveNodeList() {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表

        return list;  // 返回节点列表
    }

    //region CenterSegmented
    /**
     * 构建中心区域分段存储节点列表
     */
    private static List<ExternalKeysNode> getCenterSegmentedNodeList() {
        return getCenterSegmentedSingleSmallNodeList();  // 返回结果
    }

    //small
    /**
     * 构建分段存储-单段小布局节点列表
     */
    public static List<ExternalKeysNode> getCenterSegmentedSingleSmallNodeList() {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode display = new ExternalKeysNode();  // 创建显示节点
        display.setPlace(list.size(), 117, 25, 72, 36);  // 设置位置和尺寸
        display.setName("SingleSmallDisplay");  // 设置节点名称
        list.add(display);  // 添加节点到列表

        ExternalKeysNode play = new ExternalKeysNode();  // 创建节点节点
        play.setPlace(list.size(), 117, 66, 64, 64);  // 设置位置和尺寸
        play.setName("SingleSmallPlay");  // 设置节点名称
        play.setChildNodes(getSegmentedSinglePlayNodeList(play, list));  // 设置子节点列表
        play.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(play);  // 添加节点到列表

        ExternalKeysNode curFrameSmall = new ExternalKeysNode();  // 创建节点节点
        curFrameSmall.setPlace(list.size(), 3, 168, 220, 60);  // 设置位置和尺寸
        curFrameSmall.setName("SingleSmallCurFrameSmall");  // 设置节点名称
        curFrameSmall.setChildNodes(getSegmentedSingleSmallBeanNodeList(curFrameSmall, list));  // 设置子节点列表
        curFrameSmall.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(curFrameSmall);  // 添加节点到列表

        ExternalKeysNode smallSlip = new ExternalKeysNode();  // 创建节点节点
        smallSlip.setPlace(list.size(), 3, 276, 220, 32);  // 设置位置和尺寸
        smallSlip.setName("SingleSmallSmallSlip");  // 设置节点名称
        list.add(smallSlip);  // 添加节点到列表

        return list;  // 返回节点列表
    }

    /**
     * 构建分段存储-单段播放节点列表
     */
    public static List<ExternalKeysNode> getSegmentedSinglePlayNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode bean = new ExternalKeysNode();  // 创建节点节点
        bean.setPlace(list.size(), 117 + 5, 64 + 5, 54, 54);  // 设置位置和尺寸
        bean.setName("SinglePlay");  // 设置节点名称
        bean.setType(ExternalKeysNode.TYPE_CENTER_SEGMENT_PLAY);  // 类型:TYPE_CENTER_SEGMENT_PLAY
        bean.setParentNode(parent);  // 设置父节点
        bean.setParentNodes(parents);  // 设置父节点列表
        list.add(bean);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建分段存储-单段小布局Bean节点列表
     */
    public static List<ExternalKeysNode> getSegmentedSingleSmallBeanNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode bean = new ExternalKeysNode();  // 创建节点节点
        bean.setPlace(list.size(), 32, 168, 162, 60);  // 设置位置和尺寸
        bean.setName("SingleSmallCurFrameSmallBean");  // 设置节点名称
        bean.setChildNodes(getNumberKeyBoardNodeList(bean, list, false));  // 设置子节点列表
        bean.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        bean.setParentNode(parent);  // 设置父节点
        bean.setParentNodes(parents);  // 设置父节点列表
        list.add(bean);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    //large
    /**
     * 构建分段存储-单段大布局节点列表
     */
    public static List<ExternalKeysNode> getCenterSegmentedSingleLargeNodeList() {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode display = new ExternalKeysNode();  // 创建显示节点
        display.setPlace(list.size(), 117, 25, 72, 36);  // 设置位置和尺寸
        display.setName("SingleLargeDisplay");  // 设置节点名称
        list.add(display);  // 添加节点到列表
        ExternalKeysNode play = new ExternalKeysNode();  // 创建节点节点
        play.setPlace(list.size(), 117, 66, 64, 64);  // 设置位置和尺寸
        play.setName("SingleLargePlay");  // 设置节点名称
        play.setChildNodes(getSegmentedSinglePlayNodeList(play, list));  // 设置子节点列表
        play.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(play);  // 添加节点到列表
        ExternalKeysNode curFrameLarge = new ExternalKeysNode();  // 创建节点节点
        curFrameLarge.setPlace(list.size(), 32, 400, 162, 62);  // 设置位置和尺寸
        curFrameLarge.setName("SingleLargeCurFrameLarge");  // 设置节点名称
        curFrameLarge.setChildNodes(getSegmentedSingleLargeBeanNodeList(curFrameLarge, list));  // 设置子节点列表
        curFrameLarge.setType(ExternalKeysNode.TYPE_NO_CLICK);  // 类型:不可点击(仅展示)
        list.add(curFrameLarge);  // 添加节点到列表
        ExternalKeysNode largeSlip = new ExternalKeysNode();  // 创建节点节点
        largeSlip.setPlace(list.size(), 3, 742, 220, 32);  // 设置位置和尺寸
        largeSlip.setName("SingleLargeLargeSlip");  // 设置节点名称
        list.add(largeSlip);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    /**
     * 构建分段存储-单段大布局Bean节点列表
     */
    public static List<ExternalKeysNode> getSegmentedSingleLargeBeanNodeList(ExternalKeysNode parent, List<ExternalKeysNode> parents) {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode bean = new ExternalKeysNode();  // 创建节点节点
        bean.setPlace(list.size(), 32, 160, 162, 544);  // 设置位置和尺寸
        bean.setName("SingleLargeCurFrameLargeBean");  // 设置节点名称
        bean.setChildNodes(getNumberKeyBoardNodeList(bean, list, false));  // 设置子节点列表
        bean.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        bean.setParentNode(parent);  // 设置父节点
        bean.setParentNodes(parents);  // 设置父节点列表
        list.add(bean);  // 添加节点到列表
        return list;  // 返回节点列表
    }

    //fit
    /**
     * 构建分段存储-适配布局节点列表
     */
    public static List<ExternalKeysNode> getCenterSegmentedFitNodeList() {
        List<ExternalKeysNode> list = new ArrayList<>();  // 创建节点列表
        ExternalKeysNode display = new ExternalKeysNode();  // 创建显示节点
        display.setPlace(list.size(), 117, 25, 72, 36); //759 107
        display.setName("FitDisplay");  // 设置节点名称
        list.add(display);  // 添加节点到列表
        ExternalKeysNode fitStart = new ExternalKeysNode();  // 创建节点节点
        fitStart.setPlace(list.size(), 32, 122, 160, 62);  // 设置位置和尺寸
        fitStart.setName("FitFitStart");  // 设置节点名称
        fitStart.setChildNodes(getNumberKeyBoardNodeList(fitStart, list, false));  // 设置子节点列表
        fitStart.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(fitStart);  // 添加节点到列表
        ExternalKeysNode fitEnd = new ExternalKeysNode();  // 创建节点节点
        fitEnd.setPlace(list.size(), 32, 225, 160, 62);  // 设置位置和尺寸
        fitEnd.setName("FitFitEnd");  // 设置节点名称
        fitEnd.setChildNodes(getNumberKeyBoardNodeList(fitEnd, list, false));  // 设置子节点列表
        fitEnd.setDialog(ExternalKeysNode.DIALOG_NUMBERKEYBOARD);  // 关联弹窗:数字键盘
        list.add(fitEnd);  // 添加节点到列表
        return list;  // 返回节点列表
    }
    //endregion
}
