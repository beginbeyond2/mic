package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包

import android.util.Log; // Android日志工具

import com.micsig.tbook.scope.channel.BaseChannel; // 基础通道抽象类
import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂
import com.micsig.tbook.scope.measure.Measure; // 测量数据接口
import com.micsig.tbook.scope.measure.MeasureStaticsBean; // 测量统计数据Bean
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava枚举定义
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage; // 测量管理器
import com.micsig.tbook.ui.wavezone.IWave; // 波形接口
import com.micsig.tbook.ui.wavezone.TChan; // 通道常量定义

/**
 * @auother Liwb
 * @description:
 * @data:2022-12-1 11:41
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                       Command_Measure_Statistic                             |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 示波器测量统计命令处理模块                                         |
 * | 核心职责: 处理SCPI测量统计相关指令，包括统计开关、平均值/最大值/最小值/       |
 * |          偏差/计数开关及查询，以及统计数据视图查询                           |
 * | 架构设计: 命令模式，作为Command子模块，接收SCPI指令并通过RxBus通知UI层更新    |
 * | 数据流向: SCPI指令 → 本类(设置/查询) → RxBus → UI层;                       |
 * |          查询时从MeasureManage/Measure/MeasureStaticsBean读取统计数据         |
 * | 依赖关系: Command, CommandMsgToUI, RxBus, RxEnum, MeasureManage,             |
 * |           ChannelFactory, Measure, MeasureStaticsBean                        |
 * | 使用场景: 远程控制测量统计功能、查询统计结果时使用                           |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Measure_Statistic {

//     new SCPICommandStruct(":MEASure:STATistic:DISPlay","SCPI_Measure_Statistic","Display"),//打开或关闭统计功能
//     new SCPICommandStruct(":MEASure:STATistic:DISPlay?","SCPI_Measure_Statistic","DisplayQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:RESet","SCPI_Measure_Statistic","Reset"),//重新统计
//     new SCPICommandStruct(":MEASure:STATistic:MEAN","SCPI_Measure_Statistic","Mean"),//打开或关闭平均值
//     new SCPICommandStruct(":MEASure:STATistic:MEAN?","SCPI_Measure_Statistic","MeanQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:MAX","SCPI_Measure_Statistic","Max"),//打开或关闭最大值
//     new SCPICommandStruct(":MEASure:STATistic:MAX?","SCPI_Measure_Statistic","MaxQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:MIN","SCPI_Measure_Statistic","Min"),//打开或关闭最小值
//     new SCPICommandStruct(":MEASure:STATistic:MIN?","SCPI_Measure_Statistic","MinQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:DEV","SCPI_Measure_Statistic","Dev"),//打开或关闭delta
//     new SCPICommandStruct(":MEASure:STATistic:DEV?","SCPI_Measure_Statistic","DevQ"),//查询打开状态
//     new SCPICommandStruct(":MEASure:STATistic:COUNt","SCPI_Measure_Statistic","count"),//打开或关闭平均值
//     new SCPICommandStruct(":MEASure:STATistic:COUNt?","SCPI_Measure_Statistic","countQ"),//查询打开状态


    private boolean display; // 统计功能是否显示/开启
    private boolean mean; // 平均值是否开启
    private boolean max; // 最大值是否开启
    private boolean min; // 最小值是否开启
    private boolean dev; // 偏差值是否开启
    private boolean count; // 计数是否开启

    /**
     * 设置统计功能的显示开关
     *
     * @param bDisplay    是否开启统计显示
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Display(boolean bDisplay, boolean isUpdateUI) {
        Log.d(Command.TAG, "Display: "+bDisplay); // 打印日志，记录统计显示状态
        this.display = bDisplay; // 保存统计显示状态
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_STAT_Display); // 设置消息标志为统计显示
            String param = String.valueOf(bDisplay); // 将显示状态转为字符串参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询统计功能的显示状态
     *
     * @return 统计功能是否开启
     */
    public boolean DisplayQ() {
        return this.display; // 返回统计显示状态
    }

    /**
     * 重置统计数据（重新统计）
     */
    public void Reset() {
        CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
        msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_STAT_Reset); // 设置消息标志为统计重置
        RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
    }

    /**
     * 设置平均值统计开关
     *
     * @param bMean       是否开启平均值统计
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Mean(boolean bMean, boolean isUpdateUI) {
        this.mean = bMean; // 保存平均值开关状态
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_STAT_Mean); // 设置消息标志为平均值
            String param = String.valueOf(bMean); // 将平均值状态转为字符串参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询平均值统计开关状态
     *
     * @return 平均值是否开启
     */
    public boolean MeanQ() {
        return this.mean; // 返回平均值开关状态
    }

    /**
     * 设置最大值统计开关
     *
     * @param bMax        是否开启最大值统计
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Max(boolean bMax, boolean isUpdateUI) {
        this.max = bMax; // 保存最大值开关状态
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_STAT_Max); // 设置消息标志为最大值
            String param = String.valueOf(bMax); // 将最大值状态转为字符串参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询最大值统计开关状态
     *
     * @return 最大值是否开启
     */
    public boolean MaxQ() {
        return this.max; // 返回最大值开关状态
    }

    /**
     * 设置最小值统计开关
     *
     * @param bMin        是否开启最小值统计
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Min(boolean bMin, boolean isUpdateUI) {
        this.min = bMin; // 保存最小值开关状态
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_STAT_Min); // 设置消息标志为最小值
            String param = String.valueOf(bMin); // 将最小值状态转为字符串参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询最小值统计开关状态
     *
     * @return 最小值是否开启
     */
    public boolean MinQ() {
        return this.min; // 返回最小值开关状态
    }

    /**
     * 设置偏差统计开关
     *
     * @param bDev        是否开启偏差统计
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Dev(boolean bDev, boolean isUpdateUI) {
        this.dev = bDev; // 保存偏差开关状态
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_STAT_Dev); // 设置消息标志为偏差
            String param = String.valueOf(bDev); // 将偏差状态转为字符串参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询偏差统计开关状态
     *
     * @return 偏差是否开启
     */
    public boolean DevQ() {
        return this.dev; // 返回偏差开关状态
    }

    /**
     * 设置计数统计开关
     *
     * @param bCount      是否开启计数统计
     * @param isUpdateUI  是否同步更新UI界面
     */
    public void Count(boolean bCount, boolean isUpdateUI) {
        this.count = bCount; // 保存计数开关状态
        if (isUpdateUI) { // 判断是否需要更新UI
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_STAT_Count); // 设置消息标志为计数
            String param = String.valueOf(bCount); // 将计数状态转为字符串参数
            msgToUI.setParam(param); // 设置消息参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 通过RxBus发送消息通知UI层
        }
    }

    /**
     * 查询计数统计开关状态
     *
     * @return 计数是否开启
     */
    public boolean CountQ() {
        return this.count; // 返回计数开关状态
    }


    /**
     * 查询指定通道和测量项的全部统计数据（当前值、平均值、最大值、最小值、偏差、计数）
     *
     * @param itemIndex 测量项索引
     * @param chIndex   通道索引（0起始）
     * @return 全部统计数据的逗号分隔字符串
     */
    public String ViewQ( int itemIndex,int chIndex) {
        return query_viewQ(chIndex + 1, itemIndex, "ALL"); // 通道索引+1后查询全部统计项
    }

    /**
     * 查询指定通道和测量项的平均值
     *
     * @param itemIndex 测量项索引
     * @param chIndex   通道索引（0起始）
     * @return 平均值字符串
     */
    public String Mean_ViewQ(int itemIndex,int chIndex) {
        return query_viewQ(chIndex + 1, itemIndex, "Mean"); // 通道索引+1后查询平均值
    }

    /**
     * 查询指定通道和测量项的最大值
     *
     * @param itemIndex 测量项索引
     * @param chIndex   通道索引（0起始）
     * @return 最大值字符串
     */
    public String Max_ViewQ(int itemIndex,int chIndex) {
        return query_viewQ(chIndex + 1, itemIndex, "Max"); // 通道索引+1后查询最大值
    }

    /**
     * 查询指定通道和测量项的最小值
     *
     * @param itemIndex 测量项索引
     * @param chIndex   通道索引（0起始）
     * @return 最小值字符串
     */
    public String Min_ViewQ(int itemIndex,int chIndex) {
        return query_viewQ(chIndex + 1, itemIndex, "Min"); // 通道索引+1后查询最小值
    }

    /**
     * 查询指定通道和测量项的偏差值
     *
     * @param itemIndex 测量项索引
     * @param chIndex   通道索引（0起始）
     * @return 偏差值字符串
     */
    public String Dev_ViewQ( int itemIndex,int chIndex) {
        return query_viewQ(chIndex + 1, itemIndex, "Dev"); // 通道索引+1后查询偏差值
    }

    /**
     * 查询指定通道和测量项的计数值
     *
     * @param itemIndex 测量项索引
     * @param chIndex   通道索引（0起始）
     * @return 计数值字符串
     */
    public String Count_ViewQ( int itemIndex,int chIndex) {
        return query_viewQ(chIndex + 1, itemIndex, "Count"); // 通道索引+1后查询计数值
    }

    /**
     * 查询指定通道和测量项的当前值
     *
     * @param itemIndex 测量项索引
     * @param chIndex   通道索引（0起始）
     * @return 当前值字符串
     */
    public String Current_ViewQ( int itemIndex,int chIndex) {
        return query_viewQ(chIndex + 1, itemIndex, "Current"); // 通道索引+1后查询当前值
    }

    /**
     * 内部查询方法，根据通道索引、测量项索引和结果类型查询统计数据
     *
     * @param chIndex      通道索引（1起始）
     * @param itemIndex    测量项索引
     * @param result_item  结果类型：ALL/Mean/Max/Min/Dev/Count/Current
     * @return 查询结果字符串，无数据时返回"NONE"
     */
    private String query_viewQ(int chIndex, int itemIndex, String result_item) {
        for (int i = 0; i < MeasureManage.getInstance().getMeasureItem().getMeasureList().size(); i++) { // 遍历所有测量项
            MeasureManage.MeasureItemStruct item = MeasureManage.getInstance().getMeasureItem().getMeasureList().get(i); // 获取当前测量项
            int iWaveCh = item.getChannelId(); // 获取测量项所属通道ID
            if (TChan.isRef(iWaveCh)) continue; // 跳过参考通道
            int measureId = item.getMeasureId(); // 获取测量项ID
            if (measureId != itemIndex || iWaveCh != chIndex) continue; // 不是指定的测量项目和通道，继续查下一个

            String measureName = item.getMeasureName(); // 获取测量项名称
            Measure measure = getHardwareMeasure(iWaveCh - 1); // 获取硬件测量对象
            MeasureStaticsBean bean = measure.getMeasureStatics(measureId + 16); // 获取统计数据Bean
            if(bean!=null && bean.getNums()>0) { // 统计数据不为空且有计数
                switch (result_item) { // 根据结果类型返回对应数据
                    case "ALL": { // 查询全部统计项
                        StringBuilder sb = new StringBuilder(); // 构建结果字符串
                        sb.append(bean.getVal()); // 拼接当前值
                        sb.append(","); // 分隔符
                        sb.append(bean.getAverageVal()); // 拼接平均值
                        sb.append(","); // 分隔符
                        sb.append(bean.getMaxVal()); // 拼接最大值
                        sb.append(","); // 分隔符
                        sb.append(bean.getMinVal()); // 拼接最小值
                        sb.append(","); // 分隔符
                        sb.append(bean.getMqdVal()); // 拼接偏差值
                        sb.append(","); // 分隔符
                        sb.append(bean.getNums()); // 拼接计数值
                        return sb.toString(); // 返回全部统计数据
                    }
                    case "Mean": // 查询平均值
                        return String.valueOf(bean.getAverageVal()); // 返回平均值
                    case "Max": // 查询最大值
                        return String.valueOf(bean.getMaxVal()); // 返回最大值
                    case "Min": // 查询最小值
                        return String.valueOf(bean.getMinVal()); // 返回最小值
                    case "Dev": // 查询偏差值
                        return String.valueOf(bean.getMqdVal()); // 返回偏差值
                    case "Count": // 查询计数值
                        return String.valueOf(bean.getNums()); // 返回计数值
                    case "Current": // 查询当前值
                        return String.valueOf(bean.getVal()); // 返回当前值

                }
            }else { // 统计数据为空
                switch (result_item) { // 根据结果类型返回NONE
                    case "ALL": { // 查询全部统计项时，全部返回NONE
                        StringBuilder sb = new StringBuilder(); // 构建结果字符串
                        sb.append("NONE"); // 当前值NONE
                        sb.append(","); // 分隔符
                        sb.append("NONE"); // 平均值NONE
                        sb.append(","); // 分隔符
                        sb.append("NONE"); // 最大值NONE
                        sb.append(","); // 分隔符
                        sb.append("NONE"); // 最小值NONE
                        sb.append(","); // 分隔符
                        sb.append("NONE"); // 偏差值NONE
                        sb.append(","); // 分隔符
                        sb.append("NONE"); // 计数值NONE
                        return sb.toString(); // 返回全部NONE
                    }
                    case "Mean": // 平均值
                        return String.valueOf("NONE"); // 返回NONE
                    case "Max": // 最大值
                        return String.valueOf("NONE"); // 返回NONE
                    case "Min": // 最小值
                        return String.valueOf("NONE"); // 返回NONE
                    case "Dev": // 偏差值
                        return String.valueOf("NONE"); // 返回NONE
                    case "Count": // 计数值
                        return String.valueOf("NONE"); // 返回NONE
                    case "Current": // 当前值
                        return String.valueOf("NONE"); // 返回NONE

                }
            }
        }
        return "NONE"; // 未找到匹配的测量项，返回NONE
    }

    /**
     * 根据通道ID获取硬件测量对象
     *
     * @param chId 通道ID（0起始）
     * @return Measure对象，若通道不存在则返回null
     */
    private Measure getHardwareMeasure(int chId) {
        BaseChannel baseChannel = null; // 基础通道引用
        if (ChannelFactory.isDynamicCh(chId)) { // 判断是否为动态通道
            baseChannel = ChannelFactory.getDynamicChannel(chId); // 获取动态通道
        } else if (ChannelFactory.isMathCh(chId)) { // 判断是否为数学通道
            baseChannel = ChannelFactory.getMathChannel(chId); // 获取数学通道
        } else if (ChannelFactory.isRefCh(chId)) { // 判断是否为参考通道
            baseChannel = ChannelFactory.getRefChannel(chId); // 获取参考通道
        }
        if (baseChannel != null) { // 通道存在
            return baseChannel.getMeasure(); // 返回通道的测量对象
        }
        return null; // 通道不存在，返回null
    }
}
