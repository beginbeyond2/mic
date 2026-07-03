package com.micsig.tbook.tbookscope.services.ExternalKeys.multifunctionKnob;

import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.micsig.base.Logger;
import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Sample.MemDepthFactory;
import com.micsig.tbook.scope.Sample.SegmentSample;
import com.micsig.tbook.scope.Trigger.TriggerLogic;
import com.micsig.tbook.scope.Trigger.TriggerPulseWidth;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.IChannel;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainMsgSlip;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.config.ScopeConfig;
import com.micsig.tbook.tbookscope.main.mainbottom.MainBottomMsgQuick;
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterChannel;
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterMenu;
import com.micsig.tbook.tbookscope.main.maincenter.MainLayoutCenterSegmented;
import com.micsig.tbook.tbookscope.main.maincenter.MainMsgCenterSegmented;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgChannels;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgRefForEight;
import com.micsig.tbook.tbookscope.tools.SaveManage;
import com.micsig.tbook.tbookscope.top.layout.save.TopLayoutSave;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerLogic;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTriggerPulsewidth;
import com.micsig.tbook.tbookscope.top.layout.userset.TopLayoutUserset;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogMeasureTValue;
import com.micsig.tbook.ui.wavezone.IChan;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.mq.MQEnum;
import com.micsig.tbook.tbookscope.middleware.mq.msg.MsgChActiveChange;
import com.micsig.tbook.tbookscope.rightslipmenu.RightLayoutChannel;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgChannel;
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgMath;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogBandWidth;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsM429;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsSpi;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerialsUart;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxBusRegister;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysProtocol;
import com.micsig.tbook.tbookscope.struct.ExternalKeysMsg_ToMCU;
import com.micsig.tbook.tbookscope.structdata.ExternalKeysCommand;
import com.micsig.tbook.tbookscope.structdata.ExternalKeysUI;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.auto.TopLayoutAuto;
import com.micsig.tbook.tbookscope.top.layout.auto.TopMsgAuto;
import com.micsig.tbook.tbookscope.top.layout.auto.TopMsgAutoRange;
import com.micsig.tbook.tbookscope.top.layout.auto.TopMsgAutoSet;
import com.micsig.tbook.tbookscope.top.layout.cursor.TopLayoutCursor;
import com.micsig.tbook.tbookscope.top.layout.display.TopLayoutDisplay;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplay;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplayPersist;
import com.micsig.tbook.tbookscope.top.layout.measure.TopLayoutMeasure;
import com.micsig.tbook.tbookscope.top.layout.measure.TopMsgFrequencyMeter;
import com.micsig.tbook.tbookscope.top.layout.measure.TopMsgMeasure;
import com.micsig.tbook.tbookscope.top.layout.measure.TopMsgMeasureSetting;
import com.micsig.tbook.tbookscope.top.layout.measure.TopMsgMeasureStatics;
import com.micsig.tbook.tbookscope.top.layout.sample.TopLayoutSample;
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSample;
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSampleDepth;
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSampleMode;
import com.micsig.tbook.tbookscope.top.layout.sample.TopMsgSampleSegmented;
import com.micsig.tbook.tbookscope.top.layout.save.TopLayoutSaveStore;
import com.micsig.tbook.tbookscope.top.layout.save.TopMsgSaveStore;
import com.micsig.tbook.tbookscope.top.layout.save.TopMsgSaveSegments;
import com.micsig.tbook.tbookscope.top.layout.save.TopMsgSaveWave;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger;
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTrigger;
import com.micsig.tbook.tbookscope.top.layout.trigger.serials.TopMsgTriggerSerials;
import com.micsig.tbook.tbookscope.top.layout.userset.TopMsgUserset;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogCount;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogMeasureDelay;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogMeasurePhase;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogNumberPicker;
import com.micsig.tbook.tbookscope.top.popwindow.TopLayoutPopWindow;
import com.micsig.tbook.tbookscope.top.popwindow.TopMsgPopWindow;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.MMainMenuChannel;
import com.micsig.tbook.ui.main.AnimationView;
import com.micsig.tbook.ui.top.view.TopViewSeekBar;
import com.micsig.tbook.ui.top.view.title.TopViewHorScroll;
import com.micsig.tbook.ui.top.view.title.TopViewTitleWithScroll;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * +====================================================================================+
 * | 模块定位: 示波器外部按键/多功能旋钮管理器                                              |
 * | 核心职责: 管理示波器UI上所有菜单区域的焦点导航、旋钮旋转/按压事件处理                    |
 * | 架构设计: 单例模式，基于树形节点(ExternalKeysNode)的焦点导航系统                         |
 * | 数据流向: 外部按键协议 → ExternalKeysManager → ExternalKeysUI → UI控件                  |
 * |          RxBus消息 → initControl()注册观察者 → 更新节点状态                              |
 * | 依赖关系: ExternalKeysNode(节点模型), ExternalKeysNodeUtil(节点工厂),                    |
 * |          ExternalKeysUI(UI模拟点击), ExternalKeysCommand(命令分发),                      |
 * |          MainViewGroup(主视图容器), CacheUtil(配置缓存), RxBus(消息总线)                 |
 * | 使用场景: 用户通过外部旋钮旋转/按压控制示波器菜单时，由本类统一管理焦点移动和事件分发       |
 * +====================================================================================+
 *
 * <p>焦点区域划分:
 * <ul>
 *   <li>topList - 顶部菜单区域(触发/采样/存储/测量/自动/显示/光标等)</li>
 *   <li>rightChxList - 右侧通道菜单区域(CH1~CH8模拟通道)</li>
 *   <li>rightMathxList - 右侧数学运算菜单区域(Math1~Math8)</li>
 *   <li>rightRefxList - 右侧参考波形菜单区域(Ref1~Ref8)</li>
 *   <li>rightSxList - 右侧串行总线菜单区域(S1~S4)</li>
 *   <li>bottomList - 底部菜单区域</li>
 *   <li>channelsList - 中心通道选择区域</li>
 *   <li>segmentedList - 分段存储导航区域</li>
 *   <li>okCancelList/okList - 对话框确认/取消按钮区域</li>
 *   <li>serialsWordList - 串行总线数据字区域</li>
 *   <li>autoMotiveList - 汽车诊断区域</li>
 * </ul>
 *
 * <p>导航模式:
 * <ul>
 *   <li>moveUp/moveDown - 上下方向移动焦点(支持跨层级搜索最近节点)</li>
 *   <li>moveLeft/moveRight - 左右方向移动焦点(同级循环)</li>
 *   <li>moveInto - 进入子菜单/确认选择</li>
 *   <li>moveBack - 返回上级菜单</li>
 * </ul>
 *
 * Created by yangj on 2018/5/23.
 */

public class ExternalKeysManager {
    private static final String TAG = "ExternalKeysManager";

    private static final int LIST_HEAD = 0;

    private MainViewGroup mainViewGroup; // 主视图容器引用
    private List<ExternalKeysNode> topList = new ArrayList<>(); // 节点列表: topList
    private List<ExternalKeysNode> rightCh1List = new ArrayList<>(); // 节点列表: rightCh1List
    private List<ExternalKeysNode> rightCh2List = new ArrayList<>(); // 节点列表: rightCh2List
    private List<ExternalKeysNode> rightCh3List = new ArrayList<>(); // 节点列表: rightCh3List
    private List<ExternalKeysNode> rightCh4List = new ArrayList<>(); // 节点列表: rightCh4List
    private List<ExternalKeysNode> rightCh5List = new ArrayList<>(); // 节点列表: rightCh5List
    private List<ExternalKeysNode> rightCh6List = new ArrayList<>(); // 节点列表: rightCh6List
    private List<ExternalKeysNode> rightCh7List = new ArrayList<>(); // 节点列表: rightCh7List
    private List<ExternalKeysNode> rightCh8List = new ArrayList<>(); // 节点列表: rightCh8List
    private List<ExternalKeysNode> rightMath1List = new ArrayList<>(); // 节点列表: rightMath1List
    private List<ExternalKeysNode> rightMath2List = new ArrayList<>(); // 节点列表: rightMath2List
    private List<ExternalKeysNode> rightMath3List = new ArrayList<>(); // 节点列表: rightMath3List
    private List<ExternalKeysNode> rightMath4List = new ArrayList<>(); // 节点列表: rightMath4List
    private List<ExternalKeysNode> rightMath5List = new ArrayList<>(); // 节点列表: rightMath5List
    private List<ExternalKeysNode> rightMath6List = new ArrayList<>(); // 节点列表: rightMath6List
    private List<ExternalKeysNode> rightMath7List = new ArrayList<>(); // 节点列表: rightMath7List
    private List<ExternalKeysNode> rightMath8List = new ArrayList<>(); // 节点列表: rightMath8List
    private List<ExternalKeysNode> rightRef1List = new ArrayList<>(); // 节点列表: rightRef1List
    private List<ExternalKeysNode> rightRef2List = new ArrayList<>(); // 节点列表: rightRef2List
    private List<ExternalKeysNode> rightRef3List = new ArrayList<>(); // 节点列表: rightRef3List
    private List<ExternalKeysNode> rightRef4List = new ArrayList<>(); // 节点列表: rightRef4List
    private List<ExternalKeysNode> rightRef5List = new ArrayList<>(); // 节点列表: rightRef5List
    private List<ExternalKeysNode> rightRef6List = new ArrayList<>(); // 节点列表: rightRef6List
    private List<ExternalKeysNode> rightRef7List = new ArrayList<>(); // 节点列表: rightRef7List
    private List<ExternalKeysNode> rightRef8List = new ArrayList<>(); // 节点列表: rightRef8List
    private List<ExternalKeysNode> rightS1List = new ArrayList<>(); // 节点列表: rightS1List
    private List<ExternalKeysNode> rightS2List = new ArrayList<>(); // 节点列表: rightS2List

    private List<ExternalKeysNode> rightS3List = new ArrayList<>(); // 节点列表: rightS3List
    private List<ExternalKeysNode> rightS4List = new ArrayList<>(); // 节点列表: rightS4List
    private List<ExternalKeysNode> bottomList = new ArrayList<>(); // 节点列表: bottomList
    private List<ExternalKeysNode> channelsList = new ArrayList<>(); // 节点列表: channelsList
    private List<ExternalKeysNode> okCancelList = new ArrayList<>(); // 节点列表: okCancelList
    private List<ExternalKeysNode> okList = new ArrayList<>(); // 节点列表: okList
    private List<ExternalKeysNode> serialsWordList = new ArrayList<>(); // 节点列表: serialsWordList
    private List<ExternalKeysNode> autoMotiveList = new ArrayList<>(); // 节点列表: autoMotiveList
    private List<ExternalKeysNode> segmentedList = new ArrayList<>(); // 节点列表: segmentedList
    private int topIndex, rightCh1Index, rightCh2Index, rightCh3Index, rightCh4Index, rightCh5Index, rightCh6Index, rightCh7Index, rightCh8Index;
    private int rightMath1Index, rightMath2Index, rightMath3Index, rightMath4Index, rightMath5Index, rightMath6Index, rightMath7Index, rightMath8Index;
    private int rightRef1Index, rightRef2Index, rightRef3Index, rightRef4Index, rightRef5Index, rightRef6Index, rightRef7Index, rightRef8Index;
    private int rightS1Index, rightS2Index, rightS3Index, rightS4Index, bottomIndex;
    private int channelsIndex, okCancelIndex, okIndex, serialsWordIndex, autoMotiveIndex,segmentedIndex;
    private AnimationView focusView; // 焦点框动画视图引用
    private boolean isFirst = true; // 标记为首次初始化

    public static final String LISTTYPE_TOP = "LISTTYPE_TOP";
    public static final String LISTTYPE_RIGHT_CH1 = "LISTTYPE_RIGHT_CH1";
    public static final String LISTTYPE_RIGHT_CH2 = "LISTTYPE_RIGHT_CH2";
    public static final String LISTTYPE_RIGHT_CH3 = "LISTTYPE_RIGHT_CH3";
    public static final String LISTTYPE_RIGHT_CH4 = "LISTTYPE_RIGHT_CH4";
    public static final String LISTTYPE_RIGHT_CH5 = "LISTTYPE_RIGHT_CH5";
    public static final String LISTTYPE_RIGHT_CH6 = "LISTTYPE_RIGHT_CH6";
    public static final String LISTTYPE_RIGHT_CH7 = "LISTTYPE_RIGHT_CH7";
    public static final String LISTTYPE_RIGHT_CH8 = "LISTTYPE_RIGHT_CH8";
    public static final String LISTTYPE_RIGHT_MATH1 = "LISTTYPE_RIGHT_MATH1";
    public static final String LISTTYPE_RIGHT_MATH2 = "LISTTYPE_RIGHT_MATH2";
    public static final String LISTTYPE_RIGHT_MATH3 = "LISTTYPE_RIGHT_MATH3";
    public static final String LISTTYPE_RIGHT_MATH4 = "LISTTYPE_RIGHT_MATH4";
    public static final String LISTTYPE_RIGHT_MATH5 = "LISTTYPE_RIGHT_MATH5";
    public static final String LISTTYPE_RIGHT_MATH6 = "LISTTYPE_RIGHT_MATH6";
    public static final String LISTTYPE_RIGHT_MATH7 = "LISTTYPE_RIGHT_MATH7";
    public static final String LISTTYPE_RIGHT_MATH8 = "LISTTYPE_RIGHT_MATH8";
    public static final String LISTTYPE_RIGHT_REF1 = "LISTTYPE_RIGHT_REF";
    public static final String LISTTYPE_RIGHT_REF2 = "LISTTYPE_RIGHT_REF2";
    public static final String LISTTYPE_RIGHT_REF3 = "LISTTYPE_RIGHT_REF3";
    public static final String LISTTYPE_RIGHT_REF4 = "LISTTYPE_RIGHT_REF4";
    public static final String LISTTYPE_RIGHT_REF5 = "LISTTYPE_RIGHT_REF5";
    public static final String LISTTYPE_RIGHT_REF6 = "LISTTYPE_RIGHT_REF6";
    public static final String LISTTYPE_RIGHT_REF7 = "LISTTYPE_RIGHT_REF7";
    public static final String LISTTYPE_RIGHT_REF8 = "LISTTYPE_RIGHT_REF8";
    public static final String LISTTYPE_RIGHT_S1 = "LISTTYPE_RIGHT_S1";
    public static final String LISTTYPE_RIGHT_S2 = "LISTTYPE_RIGHT_S2";
    public static final String LISTTYPE_RIGHT_S3 = "LISTTYPE_RIGHT_S3";
    public static final String LISTTYPE_RIGHT_S4 = "LISTTYPE_RIGHT_S4";
    public static final String LISTTYPE_BOTTOM = "LISTTYPE_BOTTOM";
    public static final String LISTTYPE_CHANNELS = "LISTTYPE_CHANNELS";
    public static final String LISTTYPE_OKCANCEL = "LISTTYPE_OKCANCEL";
    public static final String LISTTYPE_OK = "LISTTYPE_OK";
    public static final String LISTTYPE_SERIALS_WORD = "LISTTYPE_SERIALS_WORD";
    public static final String LISTTYPE_AUTO_MOTIVE = "LISTTYPE_AUTO_MOTIVE";

    public static final String LISTTYPE_SEGMENTED = "LISTTYPE_SEGMENTED";
    private static int mathRefBusOffset = -7;
    public static int finalMathRefBusOffset = -7;
    public static int topSlipOffset = 36;

    @StringDef({
            LISTTYPE_TOP,
            LISTTYPE_RIGHT_CH1,
            LISTTYPE_RIGHT_CH2,
            LISTTYPE_RIGHT_CH3,
            LISTTYPE_RIGHT_CH4,
            LISTTYPE_RIGHT_CH5,
            LISTTYPE_RIGHT_CH6,
            LISTTYPE_RIGHT_CH7,
            LISTTYPE_RIGHT_CH8,
            LISTTYPE_RIGHT_MATH1,
            LISTTYPE_RIGHT_MATH2,
            LISTTYPE_RIGHT_MATH3,
            LISTTYPE_RIGHT_MATH4,
            LISTTYPE_RIGHT_MATH5,
            LISTTYPE_RIGHT_MATH6,
            LISTTYPE_RIGHT_MATH7,
            LISTTYPE_RIGHT_MATH8,
            LISTTYPE_RIGHT_REF1,
            LISTTYPE_RIGHT_REF2,
            LISTTYPE_RIGHT_REF3,
            LISTTYPE_RIGHT_REF4,
            LISTTYPE_RIGHT_REF5,
            LISTTYPE_RIGHT_REF6,
            LISTTYPE_RIGHT_REF7,
            LISTTYPE_RIGHT_REF8,
            LISTTYPE_RIGHT_S1,
            LISTTYPE_RIGHT_S2,
            LISTTYPE_RIGHT_S3,
            LISTTYPE_RIGHT_S4,
            LISTTYPE_BOTTOM,
            LISTTYPE_CHANNELS,
            LISTTYPE_OKCANCEL,
            LISTTYPE_OK,
            LISTTYPE_SERIALS_WORD,
            LISTTYPE_AUTO_MOTIVE,
            LISTTYPE_SEGMENTED
    })

    @Retention(RetentionPolicy.SOURCE)
    public @interface ListType {
    }

    //region 单例�?
    private static ExternalKeysManager manager;

    /**
     * 获取ExternalKeysManager单例实例
     * @return ExternalKeysManager单例
     */
    public static ExternalKeysManager get() {
        if (manager == null) { // 懒加载创建单例
            manager = new ExternalKeysManager(); // 创建单例实例
        }
        return manager; // 返回单例实例
    }

    private ExternalKeysManager() {
        initControl(); // 初始化控件观察者
    }
    //endregion

    //region 外部调用方法
    /**
     * 初始化管理器，绑定主视图容器并重建所有区域的节点列表
     * @param mainViewGroup 主视图容器，用于访问UI控件和判断当前显示状态
     */
    public void init(MainViewGroup mainViewGroup) {
        this.mainViewGroup = mainViewGroup; // 保存主视图容器引用

        topList.clear(); // 清空顶部菜单节点列表
        rightCh1List.clear(); // 清空右侧CH1通道节点列表
        rightCh2List.clear(); // 清空右侧CH2通道节点列表
        rightCh3List.clear(); // 清空右侧CH3通道节点列表
        rightCh4List.clear(); // 清空右侧CH4通道节点列表
        rightCh5List.clear(); // 清空右侧CH5通道节点列表
        rightCh6List.clear(); // 清空右侧CH6通道节点列表
        rightCh7List.clear(); // 清空右侧CH7通道节点列表
        rightCh8List.clear(); // 清空右侧CH8通道节点列表
        rightMath1List.clear(); // 清空右侧Math1节点列表
        rightMath2List.clear(); // 清空右侧Math2节点列表
        rightMath3List.clear(); // 清空右侧Math3节点列表
        rightMath4List.clear(); // 清空右侧Math4节点列表
        rightMath5List.clear(); // 清空右侧Math5节点列表
        rightMath6List.clear(); // 清空右侧Math6节点列表
        rightMath7List.clear(); // 清空右侧Math7节点列表
        rightMath8List.clear(); // 清空右侧Math8节点列表
        rightRef1List.clear(); // 清空右侧Ref1节点列表
        rightRef2List.clear(); // 清空右侧Ref2节点列表
        rightRef3List.clear(); // 清空右侧Ref3节点列表
        rightRef4List.clear(); // 清空右侧Ref4节点列表
        rightRef5List.clear(); // 清空右侧Ref5节点列表
        rightRef6List.clear(); // 清空右侧Ref6节点列表
        rightRef7List.clear(); // 清空右侧Ref7节点列表
        rightRef8List.clear(); // 清空右侧Ref8节点列表
        rightS1List.clear(); // 清空右侧S1串行总线节点列表
        rightS2List.clear(); // 清空右侧S2串行总线节点列表
        rightS3List.clear(); // 清空右侧S3串行总线节点列表
        rightS4List.clear(); // 清空右侧S4串行总线节点列表
        bottomList.clear(); // 清空底部菜单节点列表
        channelsList.clear(); // 清空通道选择节点列表
        okCancelList.clear(); // 清空确认取消节点列表
        okList.clear(); // 清空确认节点列表
        serialsWordList.clear(); // 清空串行数据字节点列表
        autoMotiveList.clear(); // 清空汽车诊断节点列表
        segmentedList.clear(); // 清空分段存储节点列表
        topList.addAll(ExternalKeysNodeUtil.getTopSlipNode()); // 加载顶部菜单节点
        rightCh1List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH1通道节点
        rightCh2List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH2通道节点
        rightCh3List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH3通道节点
        rightCh4List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH4通道节点
        rightCh5List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH5通道节点
        rightCh6List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH6通道节点
        rightCh7List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH7通道节点
        rightCh8List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH8通道节点
        rightMath1List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math1), ExternalKeysManager.finalMathRefBusOffset));
        rightMath2List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math2), ExternalKeysManager.finalMathRefBusOffset));
        rightMath3List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math3), ExternalKeysManager.finalMathRefBusOffset));
        rightMath4List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math4), ExternalKeysManager.finalMathRefBusOffset));
        rightMath5List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math5), ExternalKeysManager.finalMathRefBusOffset));
        rightMath6List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math6), ExternalKeysManager.finalMathRefBusOffset));
        rightMath7List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math7), ExternalKeysManager.finalMathRefBusOffset));
        rightMath8List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math8), ExternalKeysManager.finalMathRefBusOffset));
        rightRef1List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R1), ExternalKeysManager.finalMathRefBusOffset));
        rightRef2List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R2), ExternalKeysManager.finalMathRefBusOffset));
        rightRef3List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R3), ExternalKeysManager.finalMathRefBusOffset));
        rightRef4List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R4), ExternalKeysManager.finalMathRefBusOffset));
        rightRef5List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R5), ExternalKeysManager.finalMathRefBusOffset));
        rightRef6List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R6), ExternalKeysManager.finalMathRefBusOffset));
        rightRef7List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R7), ExternalKeysManager.finalMathRefBusOffset));
        rightRef8List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R8), ExternalKeysManager.finalMathRefBusOffset));
        rightS1List.addAll(ExternalKeysNodeUtil.getRightSlipSerialsNode(CacheUtil.S1, ExternalKeysManager.finalMathRefBusOffset));
        rightS2List.addAll(ExternalKeysNodeUtil.getRightSlipSerialsNode(CacheUtil.S2, ExternalKeysManager.finalMathRefBusOffset));
        rightS3List.addAll(ExternalKeysNodeUtil.getRightSlipSerialsNode(CacheUtil.S3, ExternalKeysManager.finalMathRefBusOffset));
        rightS4List.addAll(ExternalKeysNodeUtil.getRightSlipSerialsNode(CacheUtil.S4, ExternalKeysManager.finalMathRefBusOffset));
        bottomList.addAll(ExternalKeysNodeUtil.getBottomSlipNode()); // 加载底部菜单节点
        channelsList.addAll(ExternalKeysNodeUtil.getCenterChannelsNode()); // 加载中心通道节点
        okCancelList.addAll(ExternalKeysNodeUtil.getOkCancelNode()); // 加载确认取消按钮节点
        okList.addAll(ExternalKeysNodeUtil.getOkNode()); // 加载确认按钮节点
        serialsWordList.addAll(ExternalKeysNodeUtil.getSerialsWordNode()); // 加载串行数据字节点
        autoMotiveList.addAll(ExternalKeysNodeUtil.getAutoMotiveNode()); // 加载汽车诊断节点
        segmentedList.addAll(ExternalKeysNodeUtil.getCenterSegmentedNode()); // 加载分段存储节点

        topIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP); // 从缓存读取顶部菜单选中索引
        if(topIndex > 7){ // 顶部菜单索引越界检查
            topIndex = 0; // 初始化索引为0
        }
        rightCh1Index = 0; // 初始化索引为0
        rightCh2Index = 0; // 初始化索引为0
        rightCh3Index = 0; // 初始化索引为0
        rightCh4Index = 0; // 初始化索引为0
        rightCh5Index = 0; // 初始化索引为0
        rightCh6Index = 0; // 初始化索引为0
        rightCh7Index = 0; // 初始化索引为0
        rightCh8Index = 0; // 初始化索引为0
        rightMath1Index = 0; // 初始化索引为0
        rightMath2Index = 0; // 初始化索引为0
        rightMath3Index = 0; // 初始化索引为0
        rightMath4Index = 0; // 初始化索引为0
        rightMath5Index = 0; // 初始化索引为0
        rightMath6Index = 0; // 初始化索引为0
        rightMath7Index = 0; // 初始化索引为0
        rightMath8Index = 0; // 初始化索引为0
        rightRef1Index = 0; // 初始化索引为0
        rightRef2Index = 0; // 初始化索引为0
        rightRef3Index = 0; // 初始化索引为0
        rightRef4Index = 0; // 初始化索引为0
        rightRef5Index = 0; // 初始化索引为0
        rightRef6Index = 0; // 初始化索引为0
        rightRef7Index = 0; // 初始化索引为0
        rightRef8Index = 0; // 初始化索引为0
        rightS1Index = 0; // 初始化索引为0
        rightS2Index = 0; // 初始化索引为0
        rightS3Index = 0; // 初始化索引为0
        rightS4Index = 0; // 初始化索引为0
        bottomIndex = 0; // 初始化索引为0
        channelsIndex = 0; // 初始化索引为0
        okCancelIndex = 0; // 初始化索引为0
        okIndex = 0; // 初始化索引为0
        serialsWordIndex = 0; // 初始化索引为0
        autoMotiveIndex = 0; // 初始化索引为0
        setListItemVisible(); // 更新所有列表节点可见性

        isFirst = true; // 标记为首次初始化
        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)) {//主要用于恢复初始设置�?
            setViewPlace(topList, topIndex, true); // 更新焦点框位置
        } else {
            topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
        }
    }

    private TopViewSeekBar brightnessBar,triggerSensitivityBar; // 进度条控件引用
    private TopViewSeekBar intensityBar,alphaBar; // 进度条控件引用
    private TopDialogCount dialogTopCount; // 计数对话框引用
    private DialogBandWidth dialogBandWidth; // 带宽对话框引用
    private TopDialogFloatKeyBoard dialogFloatKey; // 浮点数键盘弹窗引用
    private TopViewTitleWithScroll topSlipTitle; // 顶部标题滚动控件引用
    private TopViewTitleWithScroll triggerTitle; // 顶部标题滚动控件引用
    private MMainMenuChannel chanMeasureCommon, chanMeasureSetting, chanSaveWave, chanSaveCsv, chanCursorCommon,chanAutoSave; // 主菜单通道引用

    /**
     * 当前预选框是大键盘的母节点时，需要该dialog显示才返回true，否则为false...
     * 其他预选框则始终返回true...
     */
    private boolean isIntoShowTextKeyBoardIfCurNodeContainTextKey(List<ExternalKeysNode> list) {
        if (Objects.equals(list.get(list.get(LIST_HEAD).getCurListSelect()).getDialog(), ExternalKeysNode.DIALOG_TEXTKEYBOARD)) {
            return mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD);
        }
        return true; // 返回true
    }


    /**
     * 用户点击屏幕上的px，py点，预�?�框相应的变�?
     */
    public void userClick(int px, int py) {
        Logger.d(TAG, "userClick() called with: px = [" + px + "], py = [" + py + "]"); // 调试日志输出
        if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_OKCANCEL)) { // 判断指定弹窗是否显示
            for (int i = 0; i < okCancelList.size(); i++) { // 遍历节点列表
                if (okCancelList.get(i).isContain(px, py) && okCancelList.get(i).isVisible()) {
                    okCancelIndex = i;
                    okCancelList.get(LIST_HEAD).setCurListSelect(okCancelIndex); // 设置列表选中项
                    break; // 跳出循环
                }
            }
        } else if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_OK)) {
            for (int i = 0; i < okList.size(); i++) { // 遍历节点列表
                if (okList.get(i).isContain(px, py) && okList.get(i).isVisible()) {
                    okIndex = i;
                    okList.get(LIST_HEAD).setCurListSelect(okIndex); // 设置列表选中项
                    break; // 跳出循环
                }
            }
        }else if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CENTER_SEGMENTED_VISIBLE)  && layoutSegmented.containsPoint(px, py)){ // 从缓存读取布尔值
            boolean isFit= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY)==1?true:false; // 从缓存读取整数值
            if (layoutSegmented == null) { // 懒加载分段存储布局
                layoutSegmented = (MainLayoutCenterSegmented) mainViewGroup.findViewById(R.id.mainLayoutCenterSegmented); // 获取分段存储布局控件
            }
            for (int i = 0; i < segmentedList.size(); i++) { // 遍历节点列表
                if (segmentedList.get(i).isContain(px, py) && segmentedList.get(i).isVisible()) {
                    segmentedIndex = i;
                    segmentedList.get(LIST_HEAD).setCurListSelect(segmentedIndex); // 设置列表选中项
                    break; // 跳出循环
                }
            }
        }
        else if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)) { // 判断顶部菜单是否显示
            if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                for (int i = 0; i < topList.size(); i++) { // 遍历节点列表
                    if (topList.get(i).isContain(px, py) && topList.get(i).isVisible()) {
                        topIndex = i;
                        topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                List<ExternalKeysNode> list = getTopBaseList();
                while (list.get(list.get(LIST_HEAD).getCurListSelect()).getChildNodes() != null
                        && isIntoShowTextKeyBoardIfCurNodeContainTextKey(list)) {
                    ExternalKeysNode node = list.get(list.get(LIST_HEAD).getCurListSelect());
                    if (node.getChildNodes() == null) {
                        break; // 跳出循环
                    } else {
                        list = node.getChildNodes();
                    }
                }
                boolean find = false; // 是否找到焦点节点
                do { // 循环查找下一个可见节点
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                            topList = list;
                            topIndex = i;
                            find = true; // 标记已找到
                            break; // 跳出循环
                        }
                    }
                    list = list.get(LIST_HEAD).getParentNodes();
                    if (list == null) {
                        break; // 跳出循环
                    }
                } while (!find);
                topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1)) { // 判断CH1通道菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBEMULTIPLE) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_BANDWIDTH)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBE_INTERFACE)    ) {
                Logger.i(Command.TAG,"MainViewGroup dialog open"); // 信息日志输出
                for (int i = 0; i < rightCh1List.size(); i++) { // 遍历节点列表
                    if (rightCh1List.get(i).isContain(px, py) && rightCh1List.get(i).isVisible()) {
                        rightCh1Index = i;
                        rightCh1List.get(LIST_HEAD).setCurListSelect(rightCh1Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                List<ExternalKeysNode> list = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH1);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightCh1List = list;
                        rightCh1Index = i;
                        rightCh1List.get(LIST_HEAD).setCurListSelect(rightCh1Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2)) { // 判断CH2通道菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBEMULTIPLE) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_BANDWIDTH)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBE_INTERFACE) ) {
                for (int i = 0; i < rightCh2List.size(); i++) { // 遍历节点列表
                    if (rightCh2List.get(i).isContain(px, py) && rightCh2List.get(i).isVisible()) {
                        rightCh2Index = i;
                        rightCh2List.get(LIST_HEAD).setCurListSelect(rightCh2Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                List<ExternalKeysNode> list = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH2);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && rightCh2List.get(i).isVisible()) {
                        rightCh2List = list;
                        rightCh2Index = i;
                        rightCh2List.get(LIST_HEAD).setCurListSelect(rightCh2Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3)) { // 判断CH3通道菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBEMULTIPLE) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_BANDWIDTH)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBE_INTERFACE) ) {
                for (int i = 0; i < rightCh3List.size(); i++) { // 遍历节点列表
                    if (rightCh3List.get(i).isContain(px, py) && rightCh3List.get(i).isVisible()) {
                        rightCh3Index = i;
                        rightCh3List.get(LIST_HEAD).setCurListSelect(rightCh3Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                List<ExternalKeysNode> list = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH3);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightCh3List = list;
                        rightCh3Index = i;
                        rightCh3List.get(LIST_HEAD).setCurListSelect(rightCh3Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4)) { // 判断CH4通道菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBEMULTIPLE) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_BANDWIDTH)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBE_INTERFACE) ) {
                for (int i = 0; i < rightCh4List.size(); i++) { // 遍历节点列表
                    if (rightCh4List.get(i).isContain(px, py) && rightCh4List.get(i).isVisible()) {
                        rightCh4Index = i;
                        rightCh4List.get(LIST_HEAD).setCurListSelect(rightCh4Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                List<ExternalKeysNode> list = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH4);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightCh4List = list;
                        rightCh4Index = i;
                        rightCh4List.get(LIST_HEAD).setCurListSelect(rightCh4Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5)) { // 判断CH5通道菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBEMULTIPLE) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_BANDWIDTH)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBE_INTERFACE)) {
                for (int i = 0; i < rightCh5List.size(); i++) { // 遍历节点列表
                    if (rightCh5List.get(i).isContain(px, py) && rightCh5List.get(i).isVisible()) {
                        rightCh5Index = i;
                        rightCh5List.get(LIST_HEAD).setCurListSelect(rightCh5Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                List<ExternalKeysNode> list = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH5);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightCh5List = list;
                        rightCh5Index = i;
                        rightCh5List.get(LIST_HEAD).setCurListSelect(rightCh5Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6)) { // 判断CH6通道菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBEMULTIPLE) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_BANDWIDTH)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBE_INTERFACE)) {
                for (int i = 0; i < rightCh6List.size(); i++) { // 遍历节点列表
                    if (rightCh6List.get(i).isContain(px, py) && rightCh6List.get(i).isVisible()) {
                        rightCh6Index = i;
                        rightCh6List.get(LIST_HEAD).setCurListSelect(rightCh6Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                List<ExternalKeysNode> list = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH6);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightCh6List = list;
                        rightCh6Index = i;
                        rightCh6List.get(LIST_HEAD).setCurListSelect(rightCh6Index); // 设置列表选中项
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7)) { // 判断CH7通道菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBEMULTIPLE) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_BANDWIDTH)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBE_INTERFACE)) {
                for (int i = 0; i < rightCh7List.size(); i++) { // 遍历节点列表
                    if (rightCh7List.get(i).isContain(px, py) && rightCh7List.get(i).isVisible()) {
                        rightCh7Index = i;
                        rightCh7List.get(LIST_HEAD).setCurListSelect(rightCh7Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                List<ExternalKeysNode> list = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH7);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightCh7List = list;
                        rightCh7Index = i;
                        rightCh7List.get(LIST_HEAD).setCurListSelect(rightCh7Index); // 设置列表选中项
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8)) { // 判断CH8通道菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBEMULTIPLE) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_BANDWIDTH)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_PROBE_INTERFACE)) {
                for (int i = 0; i < rightCh8List.size(); i++) { // 遍历节点列表
                    if (rightCh8List.get(i).isContain(px, py) && rightCh8List.get(i).isVisible()) {
                        rightCh8Index = i;
                        rightCh8List.get(LIST_HEAD).setCurListSelect(rightCh8Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                List<ExternalKeysNode> list = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH8);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightCh8List = list;
                        rightCh8Index = i;
                        rightCh8List.get(LIST_HEAD).setCurListSelect(rightCh8Index); // 设置列表选中项
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1)) { // 判断Math1菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FORMULAKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_NUMBERPICKER)) {
                for (int i = 0; i < rightMath1List.size(); i++) { // 遍历节点列表
                    if (rightMath1List.get(i).isContain(px, py) && rightMath1List.get(i).isVisible()) {
                        rightMath1Index = i;
                        rightMath1List.get(LIST_HEAD).setCurListSelect(rightMath1Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math1); // 从缓存读取整数值
                List<ExternalKeysNode> list = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH1);
                boolean isContains = false; // 是否找到包含点击位置的节点
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(i);
                        list = node.getChildNodes();
                        rightMath1List = list;
                        rightMath1Index = list.get(LIST_HEAD).getCurListSelect();
                        setViewPlace(rightMath1List, rightMath1Index, false); // 更新焦点框位置
                        isContains = true; // 标记为已找到
                        break; // 跳出循环
                    }
                }
                if (!isContains) {
                    if (list.get(mathType + 5).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(mathType + 5);
                        list = node.getChildNodes();
                        rightMath1List = list;
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                                list = list.get(i).getChildNodes();
                                rightMath1List = list;
                                rightMath1Index = list.get(LIST_HEAD).getCurListSelect();
                                break; // 跳出循环
                            }
                        }
                    }
                }
                rightMath1List.get(LIST_HEAD).setCurListSelect(rightMath1Index); // 设置列表选中项
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2)) { // 判断Math2菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FORMULAKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_NUMBERPICKER)) {
                for (int i = 0; i < rightMath2List.size(); i++) { // 遍历节点列表
                    if (rightMath2List.get(i).isContain(px, py) && rightMath2List.get(i).isVisible()) {
                        rightMath2Index = i;
                        rightMath2List.get(LIST_HEAD).setCurListSelect(rightMath2Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math2); // 从缓存读取整数值
                List<ExternalKeysNode> list = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH2);
                boolean isContains = false; // 是否找到包含点击位置的节点
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(i);
                        list = node.getChildNodes();
                        rightMath2List = list;
                        rightMath2Index = list.get(LIST_HEAD).getCurListSelect();
                        isContains = true; // 标记为已找到
                        break; // 跳出循环
                    }
                }
                if (!isContains) {
                    if (list.get(mathType + 5).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(mathType + 5);
                        list = node.getChildNodes();
                        rightMath2List = list;
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                                list = list.get(i).getChildNodes();
                                rightMath2List = list;
                                rightMath2Index = list.get(LIST_HEAD).getCurListSelect();
                                break; // 跳出循环
                            }
                        }
                    }
                }
                rightMath2List.get(LIST_HEAD).setCurListSelect(rightMath2Index); // 设置列表选中项
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3)) { // 判断Math3菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FORMULAKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_NUMBERPICKER)) {
                for (int i = 0; i < rightMath3List.size(); i++) { // 遍历节点列表
                    if (rightMath3List.get(i).isContain(px, py) && rightMath3List.get(i).isVisible()) {
                        rightMath3Index = i;
                        rightMath3List.get(LIST_HEAD).setCurListSelect(rightMath3Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math3); // 从缓存读取整数值
                List<ExternalKeysNode> list = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH3);
                boolean isContains = false; // 是否找到包含点击位置的节点
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(i);
                        list = node.getChildNodes();
                        rightMath3List = list;
                        rightMath3Index = list.get(LIST_HEAD).getCurListSelect();
                        isContains = true; // 标记为已找到
                        break; // 跳出循环
                    }
                }
                if (!isContains) {
                    if (list.get(mathType + 5).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(mathType + 5);
                        list = node.getChildNodes();
                        rightMath3List = list;
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                                list = list.get(i).getChildNodes();
                                rightMath3List = list;
                                rightMath3Index = list.get(LIST_HEAD).getCurListSelect();
                                break; // 跳出循环
                            }
                        }
                    }
                }
                rightMath3List.get(LIST_HEAD).setCurListSelect(rightMath3Index); // 设置列表选中项
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4)) { // 判断Math4菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FORMULAKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_NUMBERPICKER)) {
                for (int i = 0; i < rightMath4List.size(); i++) { // 遍历节点列表
                    if (rightMath4List.get(i).isContain(px, py) && rightMath4List.get(i).isVisible()) {
                        rightMath4Index = i;
                        rightMath4List.get(LIST_HEAD).setCurListSelect(rightMath4Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math4); // 从缓存读取整数值
                List<ExternalKeysNode> list = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH4);
                boolean isContains = false; // 是否找到包含点击位置的节点
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(i);
                        list = node.getChildNodes();
                        rightMath4List = list;
                        rightMath4Index = list.get(LIST_HEAD).getCurListSelect();
                        isContains = true; // 标记为已找到
                        break; // 跳出循环
                    }
                }
                if (!isContains) {
                    if (list.get(mathType + 5).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(mathType + 5);
                        list = node.getChildNodes();
                        rightMath4List = list;
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                                list = list.get(i).getChildNodes();
                                rightMath4List = list;
                                rightMath4Index = list.get(LIST_HEAD).getCurListSelect();
                                break; // 跳出循环
                            }
                        }
                    }
                }
                rightMath4List.get(LIST_HEAD).setCurListSelect(rightMath4Index); // 设置列表选中项
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5)) { // 判断Math5菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FORMULAKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_NUMBERPICKER)) {
                for (int i = 0; i < rightMath5List.size(); i++) { // 遍历节点列表
                    if (rightMath5List.get(i).isContain(px, py) && rightMath5List.get(i).isVisible()) {
                        rightMath5Index = i;
                        rightMath5List.get(LIST_HEAD).setCurListSelect(rightMath5Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math5); // 从缓存读取整数值
                List<ExternalKeysNode> list = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH5);
                boolean isContains = false; // 是否找到包含点击位置的节点
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(i);
                        list = node.getChildNodes();
                        rightMath5List = list;
                        rightMath5Index = list.get(LIST_HEAD).getCurListSelect();
                        isContains = true; // 标记为已找到
                        break; // 跳出循环
                    }
                }
                if (!isContains) {
                    if (list.get(mathType + 5).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(mathType + 5);
                        list = node.getChildNodes();
                        rightMath5List = list;
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                                list = list.get(i).getChildNodes();
                                rightMath5List = list;
                                rightMath5Index = list.get(LIST_HEAD).getCurListSelect();
                                break; // 跳出循环
                            }
                        }
                    }
                }
                rightMath5List.get(LIST_HEAD).setCurListSelect(rightMath5Index); // 设置列表选中项
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6)) { // 判断Math6菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FORMULAKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_NUMBERPICKER)) {
                for (int i = 0; i < rightMath6List.size(); i++) { // 遍历节点列表
                    if (rightMath6List.get(i).isContain(px, py) && rightMath6List.get(i).isVisible()) {
                        rightMath6Index = i;
                        rightMath6List.get(LIST_HEAD).setCurListSelect(rightMath6Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math6); // 从缓存读取整数值
                List<ExternalKeysNode> list = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH6);
                boolean isContains = false; // 是否找到包含点击位置的节点
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(i);
                        list = node.getChildNodes();
                        rightMath6List = list;
                        rightMath6Index = list.get(LIST_HEAD).getCurListSelect();
                        isContains = true; // 标记为已找到
                        break; // 跳出循环
                    }
                }
                if (!isContains) {
                    if (list.get(mathType + 5).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(mathType + 5);
                        list = node.getChildNodes();
                        rightMath6List = list;
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                                list = list.get(i).getChildNodes();
                                rightMath6List = list;
                                rightMath6Index = list.get(LIST_HEAD).getCurListSelect();
                                break; // 跳出循环
                            }
                        }
                    }
                }
                rightMath6List.get(LIST_HEAD).setCurListSelect(rightMath6Index); // 设置列表选中项
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7)) { // 判断Math7菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FORMULAKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_NUMBERPICKER)) {
                for (int i = 0; i < rightMath7List.size(); i++) { // 遍历节点列表
                    if (rightMath7List.get(i).isContain(px, py) && rightMath7List.get(i).isVisible()) {
                        rightMath7Index = i;
                        rightMath7List.get(LIST_HEAD).setCurListSelect(rightMath7Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math7); // 从缓存读取整数值
                List<ExternalKeysNode> list = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH7);
                boolean isContains = false; // 是否找到包含点击位置的节点
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(i);
                        list = node.getChildNodes();
                        rightMath7List = list;
                        rightMath7Index = list.get(LIST_HEAD).getCurListSelect();
                        isContains = true; // 标记为已找到
                        break; // 跳出循环
                    }
                }
                if (!isContains) {
                    if (list.get(mathType + 5).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(mathType + 5);
                        list = node.getChildNodes();
                        rightMath7List = list;
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                                list = list.get(i).getChildNodes();
                                rightMath7List = list;
                                rightMath7Index = list.get(LIST_HEAD).getCurListSelect();
                                break; // 跳出循环
                            }
                        }
                    }
                }
                rightMath7List.get(LIST_HEAD).setCurListSelect(rightMath7Index); // 设置列表选中项
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8)) { // 判断Math8菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FORMULAKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_NUMBERPICKER)) {
                for (int i = 0; i < rightMath8List.size(); i++) { // 遍历节点列表
                    if (rightMath8List.get(i).isContain(px, py) && rightMath8List.get(i).isVisible()) {
                        rightMath8Index = i;
                        rightMath8List.get(LIST_HEAD).setCurListSelect(rightMath8Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.Math8); // 从缓存读取整数值
                List<ExternalKeysNode> list = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH8);
                boolean isContains = false; // 是否找到包含点击位置的节点
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(i);
                        list = node.getChildNodes();
                        rightMath8List = list;
                        rightMath8Index = list.get(LIST_HEAD).getCurListSelect();
                        isContains = true; // 标记为已找到
                        break; // 跳出循环
                    }
                }
                if (!isContains) {
                    if (list.get(mathType + 5).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(mathType + 5);
                        list = node.getChildNodes();
                        rightMath8List = list;
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                                list = list.get(i).getChildNodes();
                                rightMath8List = list;
                                rightMath8Index = list.get(LIST_HEAD).getCurListSelect();
                                break; // 跳出循环
                            }
                        }
                    }
                }
                rightMath8List.get(LIST_HEAD).setCurListSelect(rightMath8Index); // 设置列表选中项
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1)) { // 判断Ref1菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_REFRECALL) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)) {
                for (int i = 0; i < rightRef1List.size(); i++) { // 遍历节点列表
                    if (rightRef1List.get(i).isContain(px, py) && rightRef1List.get(i).isVisible()) {
                        rightRef1Index = i;
                        rightRef1List.get(LIST_HEAD).setCurListSelect(rightRef1Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_LOAF_REF_CSV)) {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF1);
                rightRef1List = list.get(6).getChildNodes().get(2).getChildNodes();
                rightRef1Index = 0; // 初始化索引为0
                rightRef1List.get(LIST_HEAD).setCurListSelect(rightRef1Index); // 设置列表选中项
            } else {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF1);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightRef1List = list;
                        rightRef1Index = i;
                        rightRef1List.get(LIST_HEAD).setCurListSelect(rightRef1Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2)) { // 判断Ref2菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_REFRECALL) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)) {
                for (int i = 0; i < rightRef2List.size(); i++) { // 遍历节点列表
                    if (rightRef2List.get(i).isContain(px, py) && rightRef2List.get(i).isVisible()) {
                        rightRef2Index = i;
                        rightRef2List.get(LIST_HEAD).setCurListSelect(rightRef2Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_LOAF_REF_CSV)) {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF2);
                rightRef2List = list.get(6).getChildNodes().get(2).getChildNodes();
                rightRef2Index = 0; // 初始化索引为0
                rightRef2List.get(LIST_HEAD).setCurListSelect(rightRef2Index); // 设置列表选中项
            } else {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF2);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightRef2List = list;
                        rightRef2Index = i;
                        rightRef2List.get(LIST_HEAD).setCurListSelect(rightRef2Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3)) { // 判断Ref3菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_REFRECALL) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)) {
                for (int i = 0; i < rightRef3List.size(); i++) { // 遍历节点列表
                    if (rightRef3List.get(i).isContain(px, py) && rightRef3List.get(i).isVisible()) {
                        rightRef3Index = i;
                        rightRef3List.get(LIST_HEAD).setCurListSelect(rightRef3Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_LOAF_REF_CSV)) {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF3);
                rightRef3List = list.get(6).getChildNodes().get(2).getChildNodes();
                rightRef3Index = 0; // 初始化索引为0
                rightRef3List.get(LIST_HEAD).setCurListSelect(rightRef3Index); // 设置列表选中项
            } else {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF3);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightRef3List = list;
                        rightRef3Index = i;
                        rightRef3List.get(LIST_HEAD).setCurListSelect(rightRef3Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4)) { // 判断Ref4菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_REFRECALL) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)) {
                for (int i = 0; i < rightRef4List.size(); i++) { // 遍历节点列表
                    if (rightRef4List.get(i).isContain(px, py) && rightRef4List.get(i).isVisible()) {
                        rightRef4Index = i;
                        rightRef4List.get(LIST_HEAD).setCurListSelect(rightRef4Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_LOAF_REF_CSV)) {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF4);
                rightRef4List = list.get(6).getChildNodes().get(2).getChildNodes();
                rightRef4Index = 0; // 初始化索引为0
                rightRef4List.get(LIST_HEAD).setCurListSelect(rightRef4Index); // 设置列表选中项
            } else {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF4);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightRef4List = list;
                        rightRef4Index = i;
                        rightRef4List.get(LIST_HEAD).setCurListSelect(rightRef4Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5)) { // 判断Ref5菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_REFRECALL) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)) {
                for (int i = 0; i < rightRef5List.size(); i++) { // 遍历节点列表
                    if (rightRef5List.get(i).isContain(px, py) && rightRef5List.get(i).isVisible()) {
                        rightRef5Index = i;
                        rightRef5List.get(LIST_HEAD).setCurListSelect(rightRef5Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_LOAF_REF_CSV)) {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF5);
                rightRef5List = list.get(6).getChildNodes().get(2).getChildNodes();
                rightRef5Index = 0; // 初始化索引为0
                rightRef5List.get(LIST_HEAD).setCurListSelect(rightRef5Index); // 设置列表选中项
            } else {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF5);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightRef5List = list;
                        rightRef5Index = i;
                        rightRef5List.get(LIST_HEAD).setCurListSelect(rightRef5Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6)) { // 判断Ref6菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_REFRECALL) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)) {
                for (int i = 0; i < rightRef6List.size(); i++) { // 遍历节点列表
                    if (rightRef6List.get(i).isContain(px, py) && rightRef6List.get(i).isVisible()) {
                        rightRef6Index = i;
                        rightRef6List.get(LIST_HEAD).setCurListSelect(rightRef6Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_LOAF_REF_CSV)) {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF6);
                rightRef6List = list.get(6).getChildNodes().get(2).getChildNodes();
                rightRef6Index = 0; // 初始化索引为0
                rightRef6List.get(LIST_HEAD).setCurListSelect(rightRef6Index); // 设置列表选中项
            } else {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF6);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightRef6List = list;
                        rightRef6Index = i;
                        rightRef6List.get(LIST_HEAD).setCurListSelect(rightRef6Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7)) { // 判断Ref7菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_REFRECALL) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)) {
                for (int i = 0; i < rightRef7List.size(); i++) { // 遍历节点列表
                    if (rightRef7List.get(i).isContain(px, py) && rightRef7List.get(i).isVisible()) {
                        rightRef7Index = i;
                        rightRef7List.get(LIST_HEAD).setCurListSelect(rightRef7Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_LOAF_REF_CSV)) {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF7);
                rightRef7List = list.get(6).getChildNodes().get(2).getChildNodes();
                rightRef7Index = 0; // 初始化索引为0
                rightRef7List.get(LIST_HEAD).setCurListSelect(rightRef7Index); // 设置列表选中项
            } else {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF7);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightRef7List = list;
                        rightRef7Index = i;
                        rightRef7List.get(LIST_HEAD).setCurListSelect(rightRef7Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8)) { // 判断Ref8菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_REFRECALL) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_TEXTKEYBOARD)
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_CHANNELLABEL)) {
                for (int i = 0; i < rightRef8List.size(); i++) { // 遍历节点列表
                    if (rightRef8List.get(i).isContain(px, py) && rightRef8List.get(i).isVisible()) {
                        rightRef8Index = i;
                        rightRef8List.get(LIST_HEAD).setCurListSelect(rightRef8Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_LOAF_REF_CSV)) {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF8);
                rightRef8List = list.get(6).getChildNodes().get(2).getChildNodes();
                rightRef8Index = 0; // 初始化索引为0
                rightRef8List.get(LIST_HEAD).setCurListSelect(rightRef8Index); // 设置列表选中项
            } else {
                List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF8);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible()) {
                        rightRef8List = list;
                        rightRef8Index = i;
                        rightRef8List.get(LIST_HEAD).setCurListSelect(rightRef8Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1)) { // 判断S1串行总线菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_BAUDRATE) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_NUMBERKEYBOARD)) {
                for (int i = 0; i < rightS1List.size(); i++) { // 遍历节点列表
                    if (rightS1List.get(i).isContain(px, py) && rightS1List.get(i).isVisible()) {
                        rightS1Index = i;
                        rightS1List.get(LIST_HEAD).setCurListSelect(rightS1Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                int serialsType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1);; // 从缓存读取整数值
                List<ExternalKeysNode> list = getRightSerialsBaseList(CacheUtil.S1);
                boolean isContains = false; // 是否找到包含点击位置的节点
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(i);
                        list = node.getChildNodes();
                        rightS1List = list;
                        rightS1Index = list.get(LIST_HEAD).getCurListSelect();
                        isContains = true; // 标记为已找到
                        break; // 跳出循环
                    }
                }
                if (!isContains) {
                    if (list.get(serialsType + 5).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(serialsType + 5);
                        list = node.getChildNodes();
                        rightS1List = list;
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                                list = list.get(i).getChildNodes();
                                rightS1List = list;
                                rightS1Index = list.get(LIST_HEAD).getCurListSelect();
                                break; // 跳出循环
                            }
                        }
                    }
                }
                rightS1List.get(LIST_HEAD).setCurListSelect(rightS1Index); // 设置列表选中项
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2)) { // 判断S2串行总线菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_BAUDRATE) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_NUMBERKEYBOARD)) {
                for (int i = 0; i < rightS2List.size(); i++) { // 遍历节点列表
                    if (rightS2List.get(i).isContain(px, py) && rightS2List.get(i).isVisible()) {
                        rightS2Index = i;
                        rightS2List.get(LIST_HEAD).setCurListSelect(rightS2Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                int serialsType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2);; // 从缓存读取整数值
                List<ExternalKeysNode> list = getRightSerialsBaseList(CacheUtil.S2);
                boolean isContains = false; // 是否找到包含点击位置的节点
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(i);
                        list = node.getChildNodes();
                        rightS2List = list;
                        rightS2Index = list.get(LIST_HEAD).getCurListSelect();
                        isContains = true; // 标记为已找到
                        break; // 跳出循环
                    }
                }
                if (!isContains) {
                    if (list.get(serialsType + 5).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(serialsType + 5);
                        list = node.getChildNodes();
                        rightS2List = list;
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                                list = list.get(i).getChildNodes();
                                rightS2List = list;
                                rightS2Index = list.get(LIST_HEAD).getCurListSelect();
                                break; // 跳出循环
                            }
                        }
                    }
                }
                rightS2List.get(LIST_HEAD).setCurListSelect(rightS2Index); // 设置列表选中项
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3)) { // 判断S3串行总线菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_BAUDRATE) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_NUMBERKEYBOARD)) {
                for (int i = 0; i < rightS3List.size(); i++) { // 遍历节点列表
                    if (rightS3List.get(i).isContain(px, py) && rightS3List.get(i).isVisible()) {
                        rightS3Index = i;
                        rightS3List.get(LIST_HEAD).setCurListSelect(rightS3Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                int serialsType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3);; // 从缓存读取整数值
                List<ExternalKeysNode> list = getRightSerialsBaseList(CacheUtil.S3);
                boolean isContains = false; // 是否找到包含点击位置的节点
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(i);
                        list = node.getChildNodes();
                        rightS3List = list;
                        rightS3Index = list.get(LIST_HEAD).getCurListSelect();
                        isContains = true; // 标记为已找到
                        break; // 跳出循环
                    }
                }
                if (!isContains) {
                    if (list.get(serialsType + 5).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(serialsType + 5);
                        list = node.getChildNodes();
                        rightS3List = list;
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                                list = list.get(i).getChildNodes();
                                rightS3List = list;
                                rightS3Index = list.get(LIST_HEAD).getCurListSelect();
                                break; // 跳出循环
                            }
                        }
                    }
                }
                rightS3List.get(LIST_HEAD).setCurListSelect(rightS3Index); // 设置列表选中项
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4)) { // 判断S4串行总线菜单是否显示
            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_BAUDRATE) // 判断指定弹窗是否显示
                    || mainViewGroup.isDialogShow(MainViewGroup.DIALOG_NUMBERKEYBOARD)) {
                for (int i = 0; i < rightS4List.size(); i++) { // 遍历节点列表
                    if (rightS4List.get(i).isContain(px, py) && rightS4List.get(i).isVisible()) {
                        rightS4Index = i;
                        rightS4List.get(LIST_HEAD).setCurListSelect(rightS4Index); // 设置列表选中项
                        break; // 跳出循环
                    }
                }
            } else {
                int serialsType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4);; // 从缓存读取整数值
                List<ExternalKeysNode> list = getRightSerialsBaseList(CacheUtil.S4);
                boolean isContains = false; // 是否找到包含点击位置的节点
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(i);
                        list = node.getChildNodes();
                        rightS4List = list;
                        rightS4Index = list.get(LIST_HEAD).getCurListSelect();
                        isContains = true; // 标记为已找到
                        break; // 跳出循环
                    }
                }
                if (!isContains) {
                    if (list.get(serialsType + 5).getChildNodes() != null) {
                        ExternalKeysNode node = list.get(serialsType + 5);
                        list = node.getChildNodes();
                        rightS4List = list;
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).isContain(px, py) && list.get(i).isVisible() && list.get(i).getChildNodes() != null) {
                                list = list.get(i).getChildNodes();
                                rightS4List = list;
                                rightS4Index = list.get(LIST_HEAD).getCurListSelect();
                                break; // 跳出循环
                            }
                        }
                    }
                }
                rightS4List.get(LIST_HEAD).setCurListSelect(rightS4Index); // 设置列表选中项
            }
        } else if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) { // 从缓存读取布尔值
            for (int i = 0; i < serialsWordList.size(); i++) { // 遍历节点列表
                if (serialsWordList.get(i).isContain(px, py) && serialsWordList.get(i).isVisible()) {
                    serialsWordIndex = i;
                    serialsWordList.get(LIST_HEAD).setCurListSelect(serialsWordIndex); // 设置列表选中项
                    break; // 跳出循环
                }
            }
        }
    }

    /**
     * 在NumberPicker弹窗中旋转旋钮时改变选中项的值
     * @param isClickUp true表示向上点击(值减小)，false表示向下点击(值增大)
     */
    public void changeNumberPickerItem(boolean isClickUp) {
        if (showViewIfGone()) { // 如果焦点框不可见则显示并返回
            return; // 直接返回
        }
        if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_NUMBERPICKER)) { // 判断指定弹窗是否显示
            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1)) { // 判断Math1菜单是否显示
                rightMath1List.get(LIST_HEAD).setCurListSelect(rightMath1Index); // 设置列表选中项
                TopDialogNumberPicker numberPicker = (TopDialogNumberPicker) mainViewGroup.getDialog(MainViewGroup.DIALOG_NUMBERPICKER); // 获取弹窗实例
                if (rightMath1Index == 0 || rightMath1Index == 6) {
                    numberPicker.addOne(rightMath1Index, false);
                } else {
                    numberPicker.addOne(rightMath1Index, !isClickUp);
                }
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2)) { // 判断Math2菜单是否显示
                rightMath2List.get(LIST_HEAD).setCurListSelect(rightMath2Index); // 设置列表选中项
                TopDialogNumberPicker numberPicker = (TopDialogNumberPicker) mainViewGroup.getDialog(MainViewGroup.DIALOG_NUMBERPICKER); // 获取弹窗实例
                if (rightMath2Index == 0 || rightMath2Index == 6) {
                    numberPicker.addOne(rightMath2Index, false);
                } else {
                    numberPicker.addOne(rightMath2Index, !isClickUp);
                }
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3)) { // 判断Math3菜单是否显示
                rightMath3List.get(LIST_HEAD).setCurListSelect(rightMath3Index); // 设置列表选中项
                TopDialogNumberPicker numberPicker = (TopDialogNumberPicker) mainViewGroup.getDialog(MainViewGroup.DIALOG_NUMBERPICKER); // 获取弹窗实例
                if (rightMath3Index == 0 || rightMath3Index == 6) {
                    numberPicker.addOne(rightMath3Index, false);
                } else {
                    numberPicker.addOne(rightMath3Index, !isClickUp);
                }
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4)) { // 判断Math4菜单是否显示
                rightMath4List.get(LIST_HEAD).setCurListSelect(rightMath4Index); // 设置列表选中项
                TopDialogNumberPicker numberPicker = (TopDialogNumberPicker) mainViewGroup.getDialog(MainViewGroup.DIALOG_NUMBERPICKER); // 获取弹窗实例
                if (rightMath4Index == 0 || rightMath4Index == 6) {
                    numberPicker.addOne(rightMath4Index, false);
                } else {
                    numberPicker.addOne(rightMath4Index, !isClickUp);
                }
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5)) { // 判断Math5菜单是否显示
                rightMath5List.get(LIST_HEAD).setCurListSelect(rightMath5Index); // 设置列表选中项
                TopDialogNumberPicker numberPicker = (TopDialogNumberPicker) mainViewGroup.getDialog(MainViewGroup.DIALOG_NUMBERPICKER); // 获取弹窗实例
                if (rightMath5Index == 0 || rightMath5Index == 6) {
                    numberPicker.addOne(rightMath5Index, false);
                } else {
                    numberPicker.addOne(rightMath5Index, !isClickUp);
                }
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6)) { // 判断Math6菜单是否显示
                rightMath6List.get(LIST_HEAD).setCurListSelect(rightMath6Index); // 设置列表选中项
                TopDialogNumberPicker numberPicker = (TopDialogNumberPicker) mainViewGroup.getDialog(MainViewGroup.DIALOG_NUMBERPICKER); // 获取弹窗实例
                if (rightMath6Index == 0 || rightMath6Index == 6) {
                    numberPicker.addOne(rightMath6Index, false);
                } else {
                    numberPicker.addOne(rightMath6Index, !isClickUp);
                }
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7)) { // 判断Math7菜单是否显示
                rightMath7List.get(LIST_HEAD).setCurListSelect(rightMath7Index); // 设置列表选中项
                TopDialogNumberPicker numberPicker = (TopDialogNumberPicker) mainViewGroup.getDialog(MainViewGroup.DIALOG_NUMBERPICKER); // 获取弹窗实例
                if (rightMath7Index == 0 || rightMath7Index == 6) {
                    numberPicker.addOne(rightMath7Index, false);
                } else {
                    numberPicker.addOne(rightMath7Index, !isClickUp);
                }
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8)) { // 判断Math8菜单是否显示
                rightMath8List.get(LIST_HEAD).setCurListSelect(rightMath8Index); // 设置列表选中项
                TopDialogNumberPicker numberPicker = (TopDialogNumberPicker) mainViewGroup.getDialog(MainViewGroup.DIALOG_NUMBERPICKER); // 获取弹窗实例
                if (rightMath8Index == 0 || rightMath8Index == 6) {
                    numberPicker.addOne(rightMath8Index, false);
                } else {
                    numberPicker.addOne(rightMath8Index, !isClickUp);
                }
            }
        }
    }

    /**
     * 在浮点数键盘弹窗中旋转旋钮时改变当前单位的数值
     * @param isClickUp true表示向上点击(值减小)，false表示向下点击(值增大)
     */
    public void changeNumberKeyboardItem(boolean isClickUp) {
        if (showViewIfGone()) { // 如果焦点框不可见则显示并返回
            return; // 直接返回
        }
        if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_FLOATKEYBOARD)) { // 判断指定弹窗是否显示
            TopDialogFloatKeyBoard floatKeyBoard = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取弹窗实例
            floatKeyBoard.changeNowUnitNumber(1, !isClickUp);
        }
    }

    /**
     * 计算两点之间的欧几里得距离
     * @param p1 第一个点
     * @param p2 第二个点
     * @return 两点之间的距离
     */
    private double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(Math.abs(p1.x - p2.x), 2) + Math.pow(Math.abs(p1.y - p2.y), 2)); // 计算欧几里得距离
    }

    /**
     * moveUpDown方法
     */
    private List<ExternalKeysNode> moveUpDown(List<ExternalKeysNode> parentList, ExternalKeysNode curnode, boolean bDown, int[] curIdx) {
        return moveUpDown(parentList, curnode, bDown, curIdx, true, true);
    }

    /**
     * moveUpDown方法
     */
    private List<ExternalKeysNode> moveUpDown(List<ExternalKeysNode> parentList, ExternalKeysNode curnode, boolean bDown, int[] curIdx
            , boolean bChildNode, boolean bDialog) {
        List<ExternalKeysNode> nearestlist = null;
        ExternalKeysNode node = null;
        List<ExternalKeysNode> list = parentList;
        Point p = curnode.getCenterPoint(); // 获取当前节点的中心坐标
        double dis = Double.MAX_VALUE; // 初始化最小距离为最大值
        double val = 0;

        int bottom = 0;
        int top = p.y;
        int tmp = 0;

        do { // 循环查找下一个可见节点
//            Logger.d(TAG,p.toString());
            for (int i = 0; i < list.size(); i++) {
                node = list.get(i); // 获取当前遍历的节点
//                Logger.d(TAG,"" + i + "," + node.getName() + "," + node.getCenterPoint() + ",top:" + top + ",bottom:" + bottom);
                if (node.isVisible()) {
                    tmp = node.getCenterPoint().y;
                    if (tmp > bottom) {
                        bottom = tmp;
                    }
                    if (tmp < top) {
                        top = tmp;
                    }

                    if (bDown) {
                        if (node.getCenterPoint().y <= (p.y + 5)) {
                            continue; // 跳过此节点
                        }
                    } else {
                        if (node.getCenterPoint().y >= (p.y - 5)) {
                            continue; // 跳过此节点
                        }
                    }
                    val = distance(node.getCenterPoint(), p); // 计算到当前节点的距离

                    if (val < dis) {
                        dis = val; // 更新最小距离
                        curIdx[0] = i; // 记录最近节点索引
                        nearestlist = list; // 记录最近节点所在列表
                    }
                }
            }
            boolean bTmp = false;
            if (bChildNode) {
                node = list.get(list.get(LIST_HEAD).getCurListSelect());

                Logger.d(TAG, "bDialog :" + bDialog + " ," + node.toString()); // 调试日志输出
                if (bDialog || (node.getDialog() == null)) {
                    if (node.getChildNodes() == null) {
                        if (nearestlist != null) {
                            break; // 跳出循环
                        }
                        nearestlist = null;
                        dis = Double.MAX_VALUE;
                        list = parentList; // 重置搜索起点为父列表
                        if (bDown) {
                            p.y = top - 6;
                        } else {
                            p.y = bottom + 6;
                        }

                    } else {
                        list = node.getChildNodes();
                    }
                    bTmp = true;
                }

            }
            if (!bTmp) {
                if (nearestlist != null) {
                    break; // 跳出循环
                }
                nearestlist = null;
                dis = Double.MAX_VALUE;
                list = parentList; // 重置搜索起点为父列表
                if (bDown) {
                    p.y = top - 6;
                } else {
                    p.y = bottom + 6;
                }
            }
        } while (true);

        return nearestlist;
    }


    /**
     * 旋钮向上旋转，焦点上移
     * @param count 旋转步数
     */
    public void moveUp(int count) {
        moveUpDown(false, count);
    }

    /**
     * 旋钮上下旋转的核心处理逻辑，根据当前显示的菜单区域分发到对应的焦点移动处理
     * @param bDown true为向下旋转，false为向上旋转
     * @param count 旋转步数
     */
    private void moveUpDown(boolean bDown, int count) {
        if (showViewIfGone()) { // 如果焦点框不可见则显示并返回
            return; // 直接返回
        }
        int[] curIdx = new int[1]; // 创建索引输出参数
        if(topIndex >= topList.size()){ // 顶部菜单索引越界检查
            return; // 直接返回
        }

        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)) { // 判断顶部菜单是否显示
            if (ExternalKeysNode.TYPE_BRIGHTNESS_PROGRESS.equals(topList.get(topIndex).getType())) { // 判断是否为亮度进度条节点
                if (brightnessBar == null) { // 懒加载亮度进度条
                    brightnessBar = (TopViewSeekBar) mainViewGroup.findViewById(R.id.brightness); // 获取亮度进度条控件
                }
                isReceiveMsgNode = true; // 标记为接收消息节点
                brightnessBar.setProgress(brightnessBar.getProgress() + (bDown ? 1 : -1)); // 设置亮度进度值
            } else if(ExternalKeysNode.TYPE_TRIGGER_SENSITIVITY_PROGRESS.equals(topList.get(topIndex).getType())){ // 判断是否为触发灵敏度进度条节点
                if(triggerSensitivityBar == null){
                    triggerSensitivityBar = (TopViewSeekBar)  mainViewGroup.findViewById(R.id.triggerSensitivity);
                }
                isReceiveMsgNode = true; // 标记为接收消息节点
                triggerSensitivityBar.setProgress(triggerSensitivityBar.getProgress() + (bDown ? 1 : -1)); // 设置触发灵敏度进度值
            }else if (ExternalKeysNode.TYPE_INTENSITY_PROGRESS.equals(topList.get(topIndex).getType())) { // 判断是否为网格亮度进度条节点
                if (intensityBar == null) { // 懒加载网格亮度进度条
                    intensityBar = (TopViewSeekBar) mainViewGroup.findViewById(R.id.chartStrength); // 获取网格亮度进度条控件
                }
                isReceiveMsgNode = true; // 标记为接收消息节点
                intensityBar.setProgress(intensityBar.getProgress() + (bDown ? 1 : -1)); // 设置网格亮度进度值
            }else if (ExternalKeysNode.TYPE_ALPHA_PROGRESS.equals(topList.get(topIndex).getType())){ // 判断是否为透明度进度条节点
                if (alphaBar == null) { // 懒加载透明度进度条
                    alphaBar = (TopViewSeekBar) mainViewGroup.findViewById(R.id.transparency); // 获取透明度进度条控件
                }
                isReceiveMsgNode = true; // 标记为接收消息节点
                alphaBar.setProgress(alphaBar.getProgress() + (bDown ? 1 : -1)); // 设置透明度进度值
            }
            else if (ExternalKeysNode.TYPE_PERSIST_ADJUST.equals(topList.get(topIndex).getType())) { // 判断是否为余辉调节节点
//                isReceiveMsgNode = true;
//                ExternalKeysUI.getInstance().onClick(topList.get(topIndex).getX() + topList.get(topIndex).getW() / 2 + 100, topList.get(topIndex).getY() + topList.get(topIndex).getH() / 2);
                moveBack(); // 返回上级菜单
            } else if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(topList.get(topIndex).getType())) { // 判断是否为计数进度条节点
                dialogTopCount = (TopDialogCount) mainViewGroup.getDialog(MainViewGroup.DIALOG_TOPCOUNT); // 获取计数对话框
                dialogTopCount.setProgress(dialogTopCount.getProgress() + (bDown ? 1 : -1));
            } else {

                if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                    topList = moveUpDown(topList, topList.get(topIndex), bDown, curIdx); // 搜索最近焦点节点
                } else {
                    topList = moveUpDown(getTopBaseList(), topList.get(topIndex), bDown, curIdx, true, false); // 搜索最近焦点节点
                }

                topIndex = curIdx[0]; // 更新焦点索引
                setViewPlace(topList, topIndex, false); // 更新焦点框位置
            }

        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1)) { // 判断CH1通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh1List.get(rightCh1Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + (bDown ? 1 : -1)); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh1List.get(rightCh1Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (bDown ? count : -count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh1List.get(rightCh1Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(bDown ? 1 : -1, bDown); // 切换浮点数光标位置
            } else {
                if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                    rightCh1List = moveUpDown(rightCh1List, rightCh1List.get(rightCh1Index), bDown, curIdx,false,false); // 搜索最近焦点节点
                } else {
                    rightCh1List = moveUpDown(getRightChBaseList(MainViewGroup.RIGHTSLIP_CH1), rightCh1List.get(rightCh1Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                }
                rightCh1Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightCh1List, rightCh1Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2)) { // 判断CH2通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh2List.get(rightCh2Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + (bDown ? 1 : -1)); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh2List.get(rightCh2Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (bDown ? count : -count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh2List.get(rightCh2Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(bDown ? 1 : -1, bDown); // 切换浮点数光标位置
            } else {
                if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                    rightCh2List = moveUpDown(rightCh2List, rightCh2List.get(rightCh2Index), bDown, curIdx,false,false); // 搜索最近焦点节点
                } else {
                    rightCh2List = moveUpDown(getRightChBaseList(MainViewGroup.RIGHTSLIP_CH2), rightCh2List.get(rightCh2Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                }
                rightCh2Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightCh2List, rightCh2Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3)) { // 判断CH3通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh3List.get(rightCh3Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + (bDown ? 1 : -1)); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh3List.get(rightCh3Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (bDown ? count : -count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh3List.get(rightCh3Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(bDown ? 1 : -1, bDown); // 切换浮点数光标位置
            } else {
                if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                    rightCh3List = moveUpDown(rightCh3List, rightCh3List.get(rightCh3Index), bDown, curIdx,false,false); // 搜索最近焦点节点
                } else {
                    rightCh3List = moveUpDown(getRightChBaseList(MainViewGroup.RIGHTSLIP_CH3), rightCh3List.get(rightCh3Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                }
                rightCh3Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightCh3List, rightCh3Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4)) { // 判断CH4通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh4List.get(rightCh4Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + (bDown ? 1 : -1)); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh4List.get(rightCh4Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (bDown ? count : -count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh4List.get(rightCh4Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(bDown ? 1 : -1, bDown); // 切换浮点数光标位置
            } else {
                if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                    rightCh4List = moveUpDown(rightCh4List, rightCh4List.get(rightCh4Index), bDown, curIdx,false,false); // 搜索最近焦点节点
                } else {
                    rightCh4List = moveUpDown(getRightChBaseList(MainViewGroup.RIGHTSLIP_CH4), rightCh4List.get(rightCh4Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                }
                rightCh4Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightCh4List, rightCh4Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5)) { // 判断CH5通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh5List.get(rightCh5Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + (bDown ? 1 : -1)); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh5List.get(rightCh5Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (bDown ? count : -count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh5List.get(rightCh5Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(bDown ? 1 : -1, bDown); // 切换浮点数光标位置
            } else {
                if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                    rightCh5List = moveUpDown(rightCh5List, rightCh5List.get(rightCh5Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                } else {
                    rightCh5List = moveUpDown(getRightChBaseList(MainViewGroup.RIGHTSLIP_CH5), rightCh5List.get(rightCh5Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                }
                rightCh5Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightCh5List, rightCh5Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6)) { // 判断CH6通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh6List.get(rightCh6Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + (bDown ? 1 : -1)); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh6List.get(rightCh6Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (bDown ? count : -count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh6List.get(rightCh6Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(bDown ? 1 : -1, bDown); // 切换浮点数光标位置
            } else {
                if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                    rightCh6List = moveUpDown(rightCh6List, rightCh6List.get(rightCh6Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                } else {
                    rightCh6List = moveUpDown(getRightChBaseList(MainViewGroup.RIGHTSLIP_CH6), rightCh6List.get(rightCh6Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                }
                rightCh6Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightCh6List, rightCh6Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7)) { // 判断CH7通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh7List.get(rightCh7Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + (bDown ? 1 : -1)); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh7List.get(rightCh7Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (bDown ? count : -count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh7List.get(rightCh7Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(bDown ? 1 : -1, bDown); // 切换浮点数光标位置
            } else {
                if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                    rightCh7List = moveUpDown(rightCh7List, rightCh7List.get(rightCh7Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                } else {
                    rightCh7List = moveUpDown(getRightChBaseList(MainViewGroup.RIGHTSLIP_CH7), rightCh7List.get(rightCh7Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                }
                rightCh7Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightCh7List, rightCh7Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8)) { // 判断CH8通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh8List.get(rightCh8Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + (bDown ? 1 : -1)); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh8List.get(rightCh8Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (bDown ? count : -count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh8List.get(rightCh8Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(bDown ? 1 : -1, bDown); // 切换浮点数光标位置
            } else {
                if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                    rightCh8List = moveUpDown(rightCh8List, rightCh8List.get(rightCh8Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                } else {
                    rightCh8List = moveUpDown(getRightChBaseList(MainViewGroup.RIGHTSLIP_CH8), rightCh8List.get(rightCh8Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                }
                rightCh8Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightCh8List, rightCh8Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1)) { // 判断Math1菜单是否显示
            if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                rightMath1List = moveUpDown(rightMath1List, rightMath1List.get(rightMath1Index), bDown, curIdx); // 搜索最近焦点节点
                rightMath1Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightMath1List, rightMath1Index, false); // 更新焦点框位置
            } else {

                if (bDown) {
                    do { // 循环查找下一个可见节点
                        rightMath1Index = rightMath1Index == rightMath1List.size() - 1 ? 0 : rightMath1Index + 1;
                    } while (!rightMath1List.get(rightMath1Index).isVisible()); // 跳过不可见节点
                } else {
                    do { // 循环查找下一个可见节点
                        rightMath1Index = rightMath1Index == 0 ? rightMath1List.size() - 1 : rightMath1Index - 1;
                    } while (!rightMath1List.get(rightMath1Index).isVisible()); // 跳过不可见节点
                }
                setViewPlace(rightMath1List, rightMath1Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2)) { // 判断Math2菜单是否显示
            if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                rightMath2List = moveUpDown(rightMath2List, rightMath2List.get(rightMath2Index), bDown, curIdx); // 搜索最近焦点节点
                rightMath2Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightMath2List, rightMath2Index, false); // 更新焦点框位置
            } else {

                if (bDown) {
                    do { // 循环查找下一个可见节点
                        rightMath2Index = rightMath2Index == rightMath2List.size() - 1 ? 0 : rightMath2Index + 1;
                    } while (!rightMath2List.get(rightMath2Index).isVisible()); // 跳过不可见节点
                } else {
                    do { // 循环查找下一个可见节点
                        rightMath2Index = rightMath2Index == 0 ? rightMath2List.size() - 1 : rightMath2Index - 1;
                    } while (!rightMath2List.get(rightMath2Index).isVisible()); // 跳过不可见节点
                }
                setViewPlace(rightMath2List, rightMath2Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3)) { // 判断Math3菜单是否显示
            if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                rightMath3List = moveUpDown(rightMath3List, rightMath3List.get(rightMath3Index), bDown, curIdx); // 搜索最近焦点节点
                rightMath3Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightMath3List, rightMath3Index, false); // 更新焦点框位置
            } else {

                if (bDown) {
                    do { // 循环查找下一个可见节点
                        rightMath3Index = rightMath3Index == rightMath3List.size() - 1 ? 0 : rightMath3Index + 1;
                    } while (!rightMath3List.get(rightMath3Index).isVisible()); // 跳过不可见节点
                } else {
                    do { // 循环查找下一个可见节点
                        rightMath3Index = rightMath3Index == 0 ? rightMath3List.size() - 1 : rightMath3Index - 1;
                    } while (!rightMath3List.get(rightMath3Index).isVisible()); // 跳过不可见节点
                }
                setViewPlace(rightMath3List, rightMath3Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4)) { // 判断Math4菜单是否显示
            if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                rightMath4List = moveUpDown(rightMath4List, rightMath4List.get(rightMath4Index), bDown, curIdx); // 搜索最近焦点节点
                rightMath4Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightMath4List, rightMath4Index, false); // 更新焦点框位置
            } else {

                if (bDown) {
                    do { // 循环查找下一个可见节点
                        rightMath4Index = rightMath4Index == rightMath4List.size() - 1 ? 0 : rightMath4Index + 1;
                    } while (!rightMath4List.get(rightMath4Index).isVisible()); // 跳过不可见节点
                } else {
                    do { // 循环查找下一个可见节点
                        rightMath4Index = rightMath4Index == 0 ? rightMath4List.size() - 1 : rightMath4Index - 1;
                    } while (!rightMath4List.get(rightMath4Index).isVisible()); // 跳过不可见节点
                }
                setViewPlace(rightMath4List, rightMath4Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5)) { // 判断Math5菜单是否显示
            if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                rightMath5List = moveUpDown(rightMath5List, rightMath5List.get(rightMath5Index), bDown, curIdx); // 搜索最近焦点节点
                rightMath5Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightMath5List, rightMath5Index, false); // 更新焦点框位置
            } else {

                if (bDown) {
                    do { // 循环查找下一个可见节点
                        rightMath5Index = rightMath5Index == rightMath5List.size() - 1 ? 0 : rightMath5Index + 1;
                    } while (!rightMath5List.get(rightMath5Index).isVisible()); // 跳过不可见节点
                } else {
                    do { // 循环查找下一个可见节点
                        rightMath5Index = rightMath5Index == 0 ? rightMath5List.size() - 1 : rightMath5Index - 1;
                    } while (!rightMath5List.get(rightMath5Index).isVisible()); // 跳过不可见节点
                }
                setViewPlace(rightMath5List, rightMath5Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6)) { // 判断Math6菜单是否显示
            if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                rightMath6List = moveUpDown(rightMath6List, rightMath6List.get(rightMath6Index), bDown, curIdx); // 搜索最近焦点节点
                rightMath6Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightMath6List, rightMath6Index, false); // 更新焦点框位置
            } else {

                if (bDown) {
                    do { // 循环查找下一个可见节点
                        rightMath6Index = rightMath6Index == rightMath6List.size() - 1 ? 0 : rightMath6Index + 1;
                    } while (!rightMath6List.get(rightMath6Index).isVisible()); // 跳过不可见节点
                } else {
                    do { // 循环查找下一个可见节点
                        rightMath6Index = rightMath6Index == 0 ? rightMath6List.size() - 1 : rightMath6Index - 1;
                    } while (!rightMath6List.get(rightMath6Index).isVisible()); // 跳过不可见节点
                }
                setViewPlace(rightMath6List, rightMath6Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7)) { // 判断Math7菜单是否显示
            if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                rightMath7List = moveUpDown(rightMath7List, rightMath7List.get(rightMath7Index), bDown, curIdx); // 搜索最近焦点节点
                rightMath7Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightMath7List, rightMath7Index, false); // 更新焦点框位置
            } else {

                if (bDown) {
                    do { // 循环查找下一个可见节点
                        rightMath7Index = rightMath7Index == rightMath7List.size() - 1 ? 0 : rightMath7Index + 1;
                    } while (!rightMath7List.get(rightMath7Index).isVisible()); // 跳过不可见节点
                } else {
                    do { // 循环查找下一个可见节点
                        rightMath7Index = rightMath7Index == 0 ? rightMath7List.size() - 1 : rightMath7Index - 1;
                    } while (!rightMath7List.get(rightMath7Index).isVisible()); // 跳过不可见节点
                }
                setViewPlace(rightMath7List, rightMath7Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8)) { // 判断Math8菜单是否显示
            if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                rightMath8List = moveUpDown(rightMath8List, rightMath8List.get(rightMath8Index), bDown, curIdx); // 搜索最近焦点节点
                rightMath8Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightMath8List, rightMath8Index, false); // 更新焦点框位置
            } else {

                if (bDown) {
                    do { // 循环查找下一个可见节点
                        rightMath8Index = rightMath8Index == rightMath8List.size() - 1 ? 0 : rightMath8Index + 1;
                    } while (!rightMath8List.get(rightMath8Index).isVisible()); // 跳过不可见节点
                } else {
                    do { // 循环查找下一个可见节点
                        rightMath8Index = rightMath8Index == 0 ? rightMath8List.size() - 1 : rightMath8Index - 1;
                    } while (!rightMath8List.get(rightMath8Index).isVisible()); // 跳过不可见节点
                }
                setViewPlace(rightMath8List, rightMath8Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1)) { // 判断Ref1菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef1List.get(rightRef1Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(bDown ? ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R1 : ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R1, 1); // 发送参考回调移动命令
                setViewPlace(rightRef1List, rightRef1Index, false); // 更新焦点框位置
            } else {
                rightRef1List = moveUpDown(getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF1), rightRef1List.get(rightRef1Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                rightRef1Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightRef1List, rightRef1Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2)) { // 判断Ref2菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef2List.get(rightRef2Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(bDown ? ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R2 : ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R2, 2); // 发送参考回调移动命令
                setViewPlace(rightRef2List, rightRef2Index, false); // 更新焦点框位置
            } else {
                rightRef2List = moveUpDown(getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF2), rightRef2List.get(rightRef2Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                rightRef2Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightRef2List, rightRef2Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3)) { // 判断Ref3菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef3List.get(rightRef3Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(bDown ? ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R3 : ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R3, 3); // 发送参考回调移动命令
                setViewPlace(rightRef3List, rightRef3Index, false); // 更新焦点框位置
            } else {
                rightRef3List = moveUpDown(getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF3), rightRef3List.get(rightRef3Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                rightRef3Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightRef3List, rightRef3Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4)) { // 判断Ref4菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef4List.get(rightRef4Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(bDown ? ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R4 : ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R4, 4); // 发送参考回调移动命令
                setViewPlace(rightRef4List, rightRef4Index, false); // 更新焦点框位置
            } else {
                rightRef4List = moveUpDown(getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF4), rightRef4List.get(rightRef4Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                rightRef4Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightRef4List, rightRef4Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5)) { // 判断Ref5菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef5List.get(rightRef5Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(bDown ? ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R5 : ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R5, 5); // 发送参考回调移动命令
                setViewPlace(rightRef5List, rightRef5Index, false); // 更新焦点框位置
            } else {
                rightRef5List = moveUpDown(getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF5), rightRef5List.get(rightRef5Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                rightRef5Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightRef5List, rightRef5Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6)) { // 判断Ref6菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef6List.get(rightRef6Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(bDown ? ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R6 : ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R6, 6); // 发送参考回调移动命令
                setViewPlace(rightRef6List, rightRef6Index, false); // 更新焦点框位置
            } else {
                rightRef6List = moveUpDown(getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF6), rightRef6List.get(rightRef6Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                rightRef6Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightRef6List, rightRef6Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7)) { // 判断Ref7菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef7List.get(rightRef7Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(bDown ? ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R7 : ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R7, 7); // 发送参考回调移动命令
                setViewPlace(rightRef7List, rightRef7Index, false); // 更新焦点框位置
            } else {
                rightRef7List = moveUpDown(getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF7), rightRef7List.get(rightRef7Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                rightRef7Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightRef7List, rightRef7Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8)) { // 判断Ref8菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef8List.get(rightRef8Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(bDown ? ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R8 : ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R8, 8); // 发送参考回调移动命令
                setViewPlace(rightRef8List, rightRef8Index, false); // 更新焦点框位置
            } else {
                rightRef8List = moveUpDown(getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF8), rightRef8List.get(rightRef8Index), bDown, curIdx, false, false); // 搜索最近焦点节点
                rightRef8Index = curIdx[0]; // 更新焦点索引
                setViewPlace(rightRef8List, rightRef8Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1)) { // 判断S1串行总线菜单是否显示

            if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示

                rightS1List = moveUpDown(rightS1List, rightS1List.get(rightS1Index), bDown, curIdx, false, false); // 搜索最近焦点节点
            } else {
                rightS1List = moveUpDown(getRightSerialsBaseList(CacheUtil.S1), rightS1List.get(rightS1Index), bDown, curIdx, true, false); // 搜索最近焦点节点
            }

            rightS1Index = curIdx[0]; // 更新焦点索引
            setViewPlace(rightS1List, rightS1Index, false); // 更新焦点框位置

        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2)) { // 判断S2串行总线菜单是否显示

            if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                rightS2List = moveUpDown(rightS2List, rightS2List.get(rightS2Index), bDown, curIdx, false, false); // 搜索最近焦点节点
            } else {
                rightS2List = moveUpDown(getRightSerialsBaseList(CacheUtil.S2), rightS2List.get(rightS2Index), bDown, curIdx, true, false); // 搜索最近焦点节点
            }

            rightS2Index = curIdx[0]; // 更新焦点索引
            setViewPlace(rightS2List, rightS2Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3)) { // 判断S3串行总线菜单是否显示

            if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                rightS3List = moveUpDown(rightS3List, rightS3List.get(rightS3Index), bDown, curIdx, false, false); // 搜索最近焦点节点
            } else {
                rightS3List = moveUpDown(getRightSerialsBaseList(CacheUtil.S3), rightS3List.get(rightS3Index), bDown, curIdx, true, false); // 搜索最近焦点节点
            }
            rightS3Index = curIdx[0]; // 更新焦点索引
            setViewPlace(rightS3List, rightS3Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4)) { // 判断S4串行总线菜单是否显示

            if (mainViewGroup.isDialogsShow()) { // 判断是否有弹窗显示
                rightS4List = moveUpDown(rightS4List, rightS4List.get(rightS4Index), bDown, curIdx, false, false); // 搜索最近焦点节点
            } else {
                rightS4List = moveUpDown(getRightSerialsBaseList(CacheUtil.S4), rightS4List.get(rightS4Index), bDown, curIdx, true, false); // 搜索最近焦点节点
            }
            rightS4Index = curIdx[0]; // 更新焦点索引
            setViewPlace(rightS4List, rightS4Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.BOTTOMSLIP)) { // 判断底部菜单是否显示
//            bottomList = moveUpDown(getRightChBaseList(MainViewGroup.RIGHTSLIP_CH1),rightCh1List.get(rightCh1Index),bDown,curIdx);
//            bottomIndex = curIdx[0];
//            setViewPlace(bottomList, bottomIndex, false);
        } else if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) { // 从缓存读取布尔值
            serialsWordList = moveUpDown(serialsWordList, serialsWordList.get(serialsWordIndex), bDown, curIdx); // 搜索最近焦点节点
            serialsWordIndex = curIdx[0]; // 更新焦点索引
            setViewPlace(serialsWordList, serialsWordIndex, false); // 更新焦点框位置
        }
    }

    /**
     * 旋钮向下旋转，焦点下移
     * @param count 旋转步数
     */
    public void moveDown(int count) {

        moveUpDown(true, count);
    }

    /**
     * 旋钮向右旋转，焦点右移(对于进度条类型则增大值)
     * @param count 旋转步数
     */
    public void moveRight(int count) {
        if (showViewIfGone()) { // 如果焦点框不可见则显示并返回
            return; // 直接返回
        }
        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)) { // 判断顶部菜单是否显示
            if (ExternalKeysNode.TYPE_BRIGHTNESS_PROGRESS.equals(topList.get(topIndex).getType())) { // 判断是否为亮度进度条节点
                if (brightnessBar == null) { // 懒加载亮度进度条
                    brightnessBar = (TopViewSeekBar) mainViewGroup.findViewById(R.id.brightness); // 获取亮度进度条控件
                }
                isReceiveMsgNode = true; // 标记为接收消息节点
                brightnessBar.setProgress(brightnessBar.getProgress() + 1); // 设置亮度进度值
            }else if(ExternalKeysNode.TYPE_TRIGGER_SENSITIVITY_PROGRESS.equals(topList.get(topIndex).getType())){ // 判断是否为触发灵敏度进度条节点
                if(triggerSensitivityBar == null){
                    triggerSensitivityBar = (TopViewSeekBar)  mainViewGroup.findViewById(R.id.triggerSensitivity);
                }
                isReceiveMsgNode = true; // 标记为接收消息节点
                triggerSensitivityBar.setProgress(triggerSensitivityBar.getProgress() + 1); // 设置触发灵敏度进度值
            }
            else if (ExternalKeysNode.TYPE_INTENSITY_PROGRESS.equals(topList.get(topIndex).getType())) { // 判断是否为网格亮度进度条节点
                if (intensityBar == null) { // 懒加载网格亮度进度条
                    intensityBar = (TopViewSeekBar) mainViewGroup.findViewById(R.id.chartStrength); // 获取网格亮度进度条控件
                }
                isReceiveMsgNode = true; // 标记为接收消息节点
                intensityBar.setProgress(intensityBar.getProgress() + 1); // 设置网格亮度进度值
            }else if (ExternalKeysNode.TYPE_ALPHA_PROGRESS.equals(topList.get(topIndex).getType())){ // 判断是否为透明度进度条节点
                if (alphaBar == null) { // 懒加载透明度进度条
                    alphaBar = (TopViewSeekBar) mainViewGroup.findViewById(R.id.transparency); // 获取透明度进度条控件
                }
                isReceiveMsgNode = true; // 标记为接收消息节点
                alphaBar.setProgress(alphaBar.getProgress() + 1); // 设置透明度进度值
            }
            else if (ExternalKeysNode.TYPE_PERSIST_ADJUST.equals(topList.get(topIndex).getType())) { // 判断是否为余辉调节节点
                isReceiveMsgNode = true; // 标记为接收消息节点
                ExternalKeysUI.getInstance().onClick(topList.get(topIndex).getX() + topList.get(topIndex).getW() / 2 + 300, topList.get(topIndex).getY() + topList.get(topIndex).getH() / 2); // 模拟点击UI控件
            } else if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(topList.get(topIndex).getType())) { // 判断是否为计数进度条节点
                dialogTopCount = (TopDialogCount) mainViewGroup.getDialog(MainViewGroup.DIALOG_TOPCOUNT); // 获取计数对话框
                dialogTopCount.setProgress(dialogTopCount.getProgress() + 1);
            } else if (ExternalKeysNode.TYPE_SPINNER_LIST.equals(topList.get(topIndex).getType())) { // 判断是否为下拉列表节点
                String abPathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_ABSOLUTE_PATH + CacheUtil.WAVE_TYPE_WAV);//绝对路径
                ArrayList<String> abPathCacheList = StrUtil.getListFromString(abPathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP);
                int index = CacheUtil.get().getOtherInt(CacheUtil.TOP_SLIP_SPINNER_ITEM_INDEX) + 1;
                int itemCount = abPathCacheList.size();
                if (index >= itemCount) {
                    index = 0;
                }
                topList.get(topIndex).setPlace(topIndex, 154, 386 + index * 60, 600, 60); // 设置节点位置和尺寸
                setViewPlace(topList, topIndex, false); // 更新焦点框位置
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SPINNER_ITEM_INDEX, String.valueOf(index)); // 写入缓存映射
            } else {
                do { // 循环查找下一个可见节点
                    topIndex = topIndex == topList.size() - 1 ? 0 : topIndex + 1;
                } while (!topList.get(topIndex).isVisible()); // 跳过不可见节点
                setViewPlace(topList, topIndex, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1)) { // 判断CH1通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh1List.get(rightCh1Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh1List.get(rightCh1Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh1List.get(rightCh1Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(1, true); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh1Index = rightCh1Index == rightCh1List.size() - 1 ? 0 : rightCh1Index + 1;
                } while (!rightCh1List.get(rightCh1Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh1List, rightCh1Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2)) { // 判断CH2通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh2List.get(rightCh2Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh2List.get(rightCh2Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh2List.get(rightCh2Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(1, true); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh2Index = rightCh2Index == rightCh2List.size() - 1 ? 0 : rightCh2Index + 1;
                } while (!rightCh2List.get(rightCh2Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh2List, rightCh2Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3)) { // 判断CH3通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh3List.get(rightCh3Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh3List.get(rightCh3Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh3List.get(rightCh3Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(1, true); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh3Index = rightCh3Index == rightCh3List.size() - 1 ? 0 : rightCh3Index + 1;
                } while (!rightCh3List.get(rightCh3Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh3List, rightCh3Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4)) { // 判断CH4通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh4List.get(rightCh4Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh4List.get(rightCh4Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh4List.get(rightCh4Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(1, true); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh4Index = rightCh4Index == rightCh4List.size() - 1 ? 0 : rightCh4Index + 1;
                } while (!rightCh4List.get(rightCh4Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh4List, rightCh4Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5)) { // 判断CH5通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh5List.get(rightCh5Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh5List.get(rightCh5Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh5List.get(rightCh5Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(1, true); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh5Index = rightCh5Index == rightCh5List.size() - 1 ? 0 : rightCh5Index + 1;
                } while (!rightCh5List.get(rightCh5Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh5List, rightCh5Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6)) { // 判断CH6通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh6List.get(rightCh6Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh6List.get(rightCh6Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh6List.get(rightCh6Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(1, true); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh6Index = rightCh6Index == rightCh6List.size() - 1 ? 0 : rightCh6Index + 1;
                } while (!rightCh6List.get(rightCh6Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh6List, rightCh6Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7)) { // 判断CH7通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh7List.get(rightCh7Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh7List.get(rightCh7Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh7List.get(rightCh7Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(1, true); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh7Index = rightCh7Index == rightCh7List.size() - 1 ? 0 : rightCh7Index + 1;
                } while (!rightCh7List.get(rightCh7Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh7List, rightCh7Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8)) { // 判断CH8通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh8List.get(rightCh8Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() + 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh8List.get(rightCh8Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() + (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh8List.get(rightCh8Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(1, true); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh8Index = rightCh8Index == rightCh8List.size() - 1 ? 0 : rightCh8Index + 1;
                } while (!rightCh8List.get(rightCh8Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh8List, rightCh8Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1)) { // 判断Math1菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath1Index = rightMath1Index == rightMath1List.size() - 1 ? 0 : rightMath1Index + 1;
            } while (!rightMath1List.get(rightMath1Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath1List, rightMath1Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2)) { // 判断Math2菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath2Index = rightMath2Index == rightMath2List.size() - 1 ? 0 : rightMath2Index + 1;
            } while (!rightMath2List.get(rightMath2Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath2List, rightMath2Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3)) { // 判断Math3菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath3Index = rightMath3Index == rightMath3List.size() - 1 ? 0 : rightMath3Index + 1;
            } while (!rightMath3List.get(rightMath3Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath3List, rightMath3Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4)) { // 判断Math4菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath4Index = rightMath4Index == rightMath4List.size() - 1 ? 0 : rightMath4Index + 1;
            } while (!rightMath4List.get(rightMath4Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath4List, rightMath4Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5)) { // 判断Math5菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath5Index = rightMath5Index == rightMath5List.size() - 1 ? 0 : rightMath5Index + 1;
            } while (!rightMath5List.get(rightMath5Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath5List, rightMath5Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6)) { // 判断Math6菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath6Index = rightMath6Index == rightMath6List.size() - 1 ? 0 : rightMath6Index + 1;
            } while (!rightMath6List.get(rightMath6Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath6List, rightMath6Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7)) { // 判断Math7菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath7Index = rightMath7Index == rightMath7List.size() - 1 ? 0 : rightMath7Index + 1;
            } while (!rightMath7List.get(rightMath7Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath7List, rightMath7Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8)) { // 判断Math8菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath8Index = rightMath8Index == rightMath8List.size() - 1 ? 0 : rightMath8Index + 1;
            } while (!rightMath8List.get(rightMath8Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath8List, rightMath8Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1)) { // 判断Ref1菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef1List.get(rightRef1Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R1, 1); // 发送参考回调移动命令
                setViewPlace(rightRef1List, rightRef1Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef1List.get(rightRef1Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R1) + 1; // 从缓存读取整数值
                if (index >= mainViewGroup.getRightRef(TChan.R1).getRecyclerView().getAdapter().getItemCount()) {
                    index = 0;
                }
                mainViewGroup.getRightRef(TChan.R1).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef1Index = rightRef1Index == rightRef1List.size() - 1 ? 0 : rightRef1Index + 1;
                } while (!rightRef1List.get(rightRef1Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef1List, rightRef1Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2)) { // 判断Ref2菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef2List.get(rightRef2Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R2, 1); // 发送参考回调移动命令
                setViewPlace(rightRef2List, rightRef2Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef2List.get(rightRef2Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R2) + 1; // 从缓存读取整数值
                if (index >= mainViewGroup.getRightRef(TChan.R2).getRecyclerView().getAdapter().getItemCount()) {
                    index = 0;
                }
                mainViewGroup.getRightRef(TChan.R2).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef2Index = rightRef2Index == rightRef2List.size() - 1 ? 0 : rightRef2Index + 1;
                } while (!rightRef2List.get(rightRef2Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef2List, rightRef2Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3)) { // 判断Ref3菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef3List.get(rightRef3Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R3, 1); // 发送参考回调移动命令
                setViewPlace(rightRef3List, rightRef3Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef3List.get(rightRef3Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R3) + 1; // 从缓存读取整数值
                if (index >= mainViewGroup.getRightRef(TChan.R3).getRecyclerView().getAdapter().getItemCount()) {
                    index = 0;
                }
                mainViewGroup.getRightRef(TChan.R3).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef3Index = rightRef3Index == rightRef3List.size() - 1 ? 0 : rightRef3Index + 1;
                } while (!rightRef3List.get(rightRef3Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef3List, rightRef3Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4)) { // 判断Ref4菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef4List.get(rightRef4Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R4, 1); // 发送参考回调移动命令
                setViewPlace(rightRef4List, rightRef4Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef4List.get(rightRef4Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R4) + 1; // 从缓存读取整数值
                if (index >= mainViewGroup.getRightRef(TChan.R4).getRecyclerView().getAdapter().getItemCount()) {
                    index = 0;
                }
                mainViewGroup.getRightRef(TChan.R4).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef4Index = rightRef4Index == rightRef4List.size() - 1 ? 0 : rightRef4Index + 1;
                } while (!rightRef4List.get(rightRef4Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef4List, rightRef4Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5)) { // 判断Ref5菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef5List.get(rightRef5Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R5, 1); // 发送参考回调移动命令
                setViewPlace(rightRef5List, rightRef5Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef5List.get(rightRef5Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R5) + 1; // 从缓存读取整数值
                if (index >= mainViewGroup.getRightRef(TChan.R5).getRecyclerView().getAdapter().getItemCount()) {
                    index = 0;
                }
                mainViewGroup.getRightRef(TChan.R5).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef5Index = rightRef5Index == rightRef5List.size() - 1 ? 0 : rightRef5Index + 1;
                } while (!rightRef5List.get(rightRef5Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef5List, rightRef5Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6)) { // 判断Ref6菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef6List.get(rightRef6Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R6, 1); // 发送参考回调移动命令
                setViewPlace(rightRef6List, rightRef6Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef6List.get(rightRef6Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R6) + 1; // 从缓存读取整数值
                if (index >= mainViewGroup.getRightRef(TChan.R6).getRecyclerView().getAdapter().getItemCount()) {
                    index = 0;
                }
                mainViewGroup.getRightRef(TChan.R6).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef6Index = rightRef6Index == rightRef6List.size() - 1 ? 0 : rightRef6Index + 1;
                } while (!rightRef6List.get(rightRef6Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef6List, rightRef6Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7)) { // 判断Ref7菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef7List.get(rightRef7Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R7, 1); // 发送参考回调移动命令
                setViewPlace(rightRef7List, rightRef7Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef7List.get(rightRef7Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R7) + 1; // 从缓存读取整数值
                if (index >= mainViewGroup.getRightRef(TChan.R7).getRecyclerView().getAdapter().getItemCount()) {
                    index = 0;
                }
                mainViewGroup.getRightRef(TChan.R7).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef7Index = rightRef7Index == rightRef7List.size() - 1 ? 0 : rightRef7Index + 1;
                } while (!rightRef7List.get(rightRef7Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef7List, rightRef7Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8)) { // 判断Ref8菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef8List.get(rightRef8Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_DOWN + CommandMsgToUI.PARAM_SPLIT + TChan.R8, 1); // 发送参考回调移动命令
                setViewPlace(rightRef8List, rightRef8Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef8List.get(rightRef8Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R8) + 1; // 从缓存读取整数值
                if (index >= mainViewGroup.getRightRef(TChan.R8).getRecyclerView().getAdapter().getItemCount()) {
                    index = 0;
                }
                mainViewGroup.getRightRef(TChan.R8).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef8Index = rightRef8Index == rightRef8List.size() - 1 ? 0 : rightRef8Index + 1;
                } while (!rightRef8List.get(rightRef8Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef8List, rightRef8Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1)) { // 判断S1串行总线菜单是否显示
            do { // 循环查找下一个可见节点
                rightS1Index = rightS1Index == rightS1List.size() - 1 ? 0 : rightS1Index + 1;
            } while (!rightS1List.get(rightS1Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightS1List, rightS1Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2)) { // 判断S2串行总线菜单是否显示
            do { // 循环查找下一个可见节点
                rightS2Index = rightS2Index == rightS2List.size() - 1 ? 0 : rightS2Index + 1;
            } while (!rightS2List.get(rightS2Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightS2List, rightS2Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3)) { // 判断S3串行总线菜单是否显示
            do { // 循环查找下一个可见节点
                rightS3Index = rightS3Index == rightS3List.size() - 1 ? 0 : rightS3Index + 1;
            } while (!rightS3List.get(rightS3Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightS3List, rightS3Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4)) { // 判断S4串行总线菜单是否显示
            do { // 循环查找下一个可见节点
                rightS4Index = rightS4Index == rightS4List.size() - 1 ? 0 : rightS4Index + 1;
            } while (!rightS4List.get(rightS4Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightS4List, rightS4Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.BOTTOMSLIP)) { // 判断底部菜单是否显示
            do { // 循环查找下一个可见节点
                bottomIndex = bottomIndex == bottomList.size() - 1 ? 0 : bottomIndex + 1;
            } while (!bottomList.get(bottomIndex).isVisible()); // 跳过不可见节点
            setViewPlace(bottomList, bottomIndex, false); // 更新焦点框位置
        } else if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) { // 从缓存读取布尔值
            do { // 循环查找下一个可见节点
                serialsWordIndex = serialsWordIndex == serialsWordList.size() - 1 ? 0 : serialsWordIndex + 1;
            } while (!serialsWordList.get(serialsWordIndex).isVisible()); // 跳过不可见节点
            updateSerialClearButton(); // 更新串行清除按钮位置
            setViewPlace(serialsWordList, serialsWordIndex, false); // 更新焦点框位置
        }
    }

    /**
     * 旋钮向左旋转，焦点左移(对于进度条类型则减小值)
     * @param count 旋转步数
     */
    public void moveLeft(int count) {
        if (showViewIfGone()) { // 如果焦点框不可见则显示并返回
            return; // 直接返回
        }
        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)) { // 判断顶部菜单是否显示
            if (ExternalKeysNode.TYPE_BRIGHTNESS_PROGRESS.equals(topList.get(topIndex).getType())) { // 判断是否为亮度进度条节点
                if (brightnessBar == null) { // 懒加载亮度进度条
                    brightnessBar = (TopViewSeekBar) mainViewGroup.findViewById(R.id.brightness); // 获取亮度进度条控件
                }
                isReceiveMsgNode = true; // 标记为接收消息节点
                brightnessBar.setProgress(brightnessBar.getProgress() - 1); // 设置亮度进度值
            } else if(ExternalKeysNode.TYPE_TRIGGER_SENSITIVITY_PROGRESS.equals(topList.get(topIndex).getType())){ // 判断是否为触发灵敏度进度条节点
                if(triggerSensitivityBar == null){
                    triggerSensitivityBar = (TopViewSeekBar)  mainViewGroup.findViewById(R.id.triggerSensitivity);
                }
                isReceiveMsgNode = true; // 标记为接收消息节点
                triggerSensitivityBar.setProgress(triggerSensitivityBar.getProgress() - 1); // 设置触发灵敏度进度值
            }
            else if (ExternalKeysNode.TYPE_INTENSITY_PROGRESS.equals(topList.get(topIndex).getType())) { // 判断是否为网格亮度进度条节点
                if (intensityBar == null) { // 懒加载网格亮度进度条
                    intensityBar = (TopViewSeekBar) mainViewGroup.findViewById(R.id.chartStrength); // 获取网格亮度进度条控件
                }
                isReceiveMsgNode = true; // 标记为接收消息节点
                intensityBar.setProgress(intensityBar.getProgress() - 1); // 设置网格亮度进度值
            }else if (ExternalKeysNode.TYPE_ALPHA_PROGRESS.equals(topList.get(topIndex).getType())){ // 判断是否为透明度进度条节点
                if (alphaBar == null) { // 懒加载透明度进度条
                    alphaBar = (TopViewSeekBar) mainViewGroup.findViewById(R.id.transparency); // 获取透明度进度条控件
                }
                isReceiveMsgNode = true; // 标记为接收消息节点
                alphaBar.setProgress(alphaBar.getProgress() - 1); // 设置透明度进度值
            }
            else if (ExternalKeysNode.TYPE_PERSIST_ADJUST.equals(topList.get(topIndex).getType())) { // 判断是否为余辉调节节点
                isReceiveMsgNode = true; // 标记为接收消息节点
                ExternalKeysUI.getInstance().onClick(topList.get(topIndex).getX() + topList.get(topIndex).getW() / 2 - 300, topList.get(topIndex).getY() + topList.get(topIndex).getH() / 2); // 模拟点击UI控件
            } else if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(topList.get(topIndex).getType())) { // 判断是否为计数进度条节点
                dialogTopCount = (TopDialogCount) mainViewGroup.getDialog(MainViewGroup.DIALOG_TOPCOUNT); // 获取计数对话框
                dialogTopCount.setProgress(dialogTopCount.getProgress() - 1);
            } else if (ExternalKeysNode.TYPE_SPINNER_LIST.equals(topList.get(topIndex).getType())) { // 判断是否为下拉列表节点
                String abPathCacheStr = CacheUtil.get().getOtherString(CacheUtil.TOP_SLIP_SAVE_WAVE_ABSOLUTE_PATH + CacheUtil.WAVE_TYPE_WAV);//绝对路径
                ArrayList<String> abPathCacheList = StrUtil.getListFromString(abPathCacheStr, CacheUtil.WAVE_STORE_PATH_SLIP);
                int index = CacheUtil.get().getOtherInt(CacheUtil.TOP_SLIP_SPINNER_ITEM_INDEX) - 1;
                int itemCount = abPathCacheList.size();
                if (index < 0) {
                    index = itemCount - 1;
                }
                topList.get(topIndex).setPlace(topIndex, 154, 386 + index * 60, 600, 60); // 设置节点位置和尺寸
                setViewPlace(topList, topIndex, false); // 更新焦点框位置
                CacheUtil.get().putOtherMap(CacheUtil.TOP_SLIP_SPINNER_ITEM_INDEX, String.valueOf(index)); // 写入缓存映射
            } else {
                do { // 循环查找下一个可见节点
                    topIndex = topIndex == 0 ? topList.size() - 1 : topIndex - 1;
                } while (!topList.get(topIndex).isVisible()); // 跳过不可见节点
                setViewPlace(topList, topIndex, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1)) { // 判断CH1通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh1List.get(rightCh1Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() - 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh1List.get(rightCh1Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() - (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh1List.get(rightCh1Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(-1, false); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh1Index = rightCh1Index == 0 ? rightCh1List.size() - 1 : rightCh1Index - 1;
                } while (!rightCh1List.get(rightCh1Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh1List, rightCh1Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2)) { // 判断CH2通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh2List.get(rightCh2Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() - 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh2List.get(rightCh2Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() - (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh2List.get(rightCh2Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(-1, false); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh2Index = rightCh2Index == 0 ? rightCh2List.size() - 1 : rightCh2Index - 1;
                } while (!rightCh2List.get(rightCh2Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh2List, rightCh2Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3)) { // 判断CH3通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh3List.get(rightCh3Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() - 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh3List.get(rightCh3Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() - (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh3List.get(rightCh3Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(-1, false); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh3Index = rightCh3Index == 0 ? rightCh3List.size() - 1 : rightCh3Index - 1;
                } while (!rightCh3List.get(rightCh3Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh3List, rightCh3Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4)) { // 判断CH4通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh4List.get(rightCh4Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() - 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh4List.get(rightCh4Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() - (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh4List.get(rightCh4Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(-1, false); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh4Index = rightCh4Index == 0 ? rightCh4List.size() - 1 : rightCh4Index - 1;
                } while (!rightCh4List.get(rightCh4Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh4List, rightCh4Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5)) { // 判断CH5通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh5List.get(rightCh5Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() - 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh5List.get(rightCh5Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() - (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh5List.get(rightCh5Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(-1, false); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh5Index = rightCh5Index == 0 ? rightCh5List.size() - 1 : rightCh5Index - 1;
                } while (!rightCh5List.get(rightCh5Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh5List, rightCh5Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6)) { // 判断CH6通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh6List.get(rightCh6Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() - 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh6List.get(rightCh6Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() - (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh6List.get(rightCh6Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(-1, false); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh6Index = rightCh6Index == 0 ? rightCh6List.size() - 1 : rightCh6Index - 1;
                } while (!rightCh6List.get(rightCh6Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh6List, rightCh6Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7)) { // 判断CH7通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh7List.get(rightCh7Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() - 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh7List.get(rightCh7Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() - (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh7List.get(rightCh7Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(-1, false); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh7Index = rightCh7Index == 0 ? rightCh7List.size() - 1 : rightCh7Index - 1;
                } while (!rightCh7List.get(rightCh7Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh7List, rightCh7Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8)) { // 判断CH8通道菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh8List.get(rightCh8Index).getType())) { // 判断是否为计数进度条节点
                dialogBandWidth = (DialogBandWidth) mainViewGroup.getDialog(MainViewGroup.DIALOG_BANDWIDTH); // 获取带宽对话框
                dialogBandWidth.setProgress(dialogBandWidth.getProgress() - 1); // 设置带宽进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh8List.get(rightCh8Index).getType())) { // 判断是否为通道延迟进度条节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.setProgress(dialogFloatKey.getProgress() - (dialogFloatKey.getIsPType() ? count * 4 : count)); // 设置浮点数进度值
            } else if (ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh8List.get(rightCh8Index).getType())) { // 判断是否为通道延迟单位节点
                dialogFloatKey = (TopDialogFloatKeyBoard) mainViewGroup.getDialog(MainViewGroup.DIALOG_FLOATKEYBOARD); // 获取浮点数键盘弹窗
                dialogFloatKey.changeSelection(-1, false); // 切换浮点数光标位置
            } else {
                do { // 循环查找下一个可见节点
                    rightCh8Index = rightCh8Index == 0 ? rightCh8List.size() - 1 : rightCh8Index - 1;
                } while (!rightCh8List.get(rightCh8Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightCh8List, rightCh8Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1)) { // 判断Math1菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath1Index = rightMath1Index == 0 ? rightMath1List.size() - 1 : rightMath1Index - 1;
            } while (!rightMath1List.get(rightMath1Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath1List, rightMath1Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2)) { // 判断Math2菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath2Index = rightMath2Index == 0 ? rightMath2List.size() - 1 : rightMath2Index - 1;
            } while (!rightMath2List.get(rightMath2Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath2List, rightMath2Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3)) { // 判断Math3菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath3Index = rightMath3Index == 0 ? rightMath3List.size() - 1 : rightMath3Index - 1;
            } while (!rightMath3List.get(rightMath3Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath3List, rightMath3Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4)) { // 判断Math4菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath4Index = rightMath4Index == 0 ? rightMath4List.size() - 1 : rightMath4Index - 1;
            } while (!rightMath4List.get(rightMath4Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath4List, rightMath4Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5)) { // 判断Math5菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath5Index = rightMath5Index == 0 ? rightMath5List.size() - 1 : rightMath5Index - 1;
            } while (!rightMath5List.get(rightMath5Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath5List, rightMath5Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6)) { // 判断Math6菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath6Index = rightMath6Index == 0 ? rightMath6List.size() - 1 : rightMath6Index - 1;
            } while (!rightMath6List.get(rightMath6Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath6List, rightMath6Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7)) { // 判断Math7菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath7Index = rightMath7Index == 0 ? rightMath7List.size() - 1 : rightMath7Index - 1;
            } while (!rightMath7List.get(rightMath7Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath7List, rightMath7Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8)) { // 判断Math8菜单是否显示
            do { // 循环查找下一个可见节点
                rightMath8Index = rightMath8Index == 0 ? rightMath8List.size() - 1 : rightMath8Index - 1;
            } while (!rightMath8List.get(rightMath8Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightMath8List, rightMath8Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1)) { // 判断Ref1菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef1List.get(rightRef1Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R1, 1); // 发送参考回调移动命令
                setViewPlace(rightRef1List, rightRef1Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef1List.get(rightRef1Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R1) - 1; // 从缓存读取整数值
                if (index < 0) {
                    index = mainViewGroup.getRightRef(TChan.R1).getRecyclerView().getAdapter().getItemCount() - 1;
                }
                mainViewGroup.getRightRef(TChan.R1).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef1Index = rightRef1Index == 0 ? rightRef1List.size() - 1 : rightRef1Index - 1;
                } while (!rightRef1List.get(rightRef1Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef1List, rightRef1Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2)) { // 判断Ref2菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef2List.get(rightRef2Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R2, 1); // 发送参考回调移动命令
                setViewPlace(rightRef2List, rightRef2Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef2List.get(rightRef2Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R2) - 1; // 从缓存读取整数值
                if (index < 0) {
                    index = mainViewGroup.getRightRef(TChan.R2).getRecyclerView().getAdapter().getItemCount() - 1;
                }
                mainViewGroup.getRightRef(TChan.R2).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef2Index = rightRef2Index == 0 ? rightRef2List.size() - 1 : rightRef2Index - 1;
                } while (!rightRef2List.get(rightRef2Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef2List, rightRef2Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3)) { // 判断Ref3菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef3List.get(rightRef3Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R3, 1); // 发送参考回调移动命令
                setViewPlace(rightRef3List, rightRef3Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef3List.get(rightRef3Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R3) - 1; // 从缓存读取整数值
                if (index < 0) {
                    index = mainViewGroup.getRightRef(TChan.R3).getRecyclerView().getAdapter().getItemCount() - 1;
                }
                mainViewGroup.getRightRef(TChan.R3).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef3Index = rightRef3Index == 0 ? rightRef3List.size() - 1 : rightRef3Index - 1;
                } while (!rightRef3List.get(rightRef3Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef3List, rightRef3Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4)) { // 判断Ref4菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef4List.get(rightRef4Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R4, 1); // 发送参考回调移动命令
                setViewPlace(rightRef4List, rightRef4Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef4List.get(rightRef4Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R4) - 1; // 从缓存读取整数值
                if (index < 0) {
                    index = mainViewGroup.getRightRef(TChan.R4).getRecyclerView().getAdapter().getItemCount() - 1;
                }
                mainViewGroup.getRightRef(TChan.R4).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef4Index = rightRef4Index == 0 ? rightRef4List.size() - 1 : rightRef4Index - 1;
                } while (!rightRef4List.get(rightRef4Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef4List, rightRef4Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5)) { // 判断Ref5菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef5List.get(rightRef5Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R5, 1); // 发送参考回调移动命令
                setViewPlace(rightRef5List, rightRef5Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef5List.get(rightRef5Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R5) - 1; // 从缓存读取整数值
                if (index < 0) {
                    index = mainViewGroup.getRightRef(TChan.R5).getRecyclerView().getAdapter().getItemCount() - 1;
                }
                mainViewGroup.getRightRef(TChan.R5).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef5Index = rightRef5Index == 0 ? rightRef5List.size() - 1 : rightRef5Index - 1;
                } while (!rightRef5List.get(rightRef5Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef5List, rightRef5Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6)) { // 判断Ref6菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef6List.get(rightRef6Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R6, 1); // 发送参考回调移动命令
                setViewPlace(rightRef6List, rightRef6Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef6List.get(rightRef6Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R6) - 1; // 从缓存读取整数值
                if (index < 0) {
                    index = mainViewGroup.getRightRef(TChan.R6).getRecyclerView().getAdapter().getItemCount() - 1;
                }
                mainViewGroup.getRightRef(TChan.R6).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef6Index = rightRef6Index == 0 ? rightRef6List.size() - 1 : rightRef6Index - 1;
                } while (!rightRef6List.get(rightRef6Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef6List, rightRef6Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7)) { // 判断Ref7菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef7List.get(rightRef7Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R7, 1); // 发送参考回调移动命令
                setViewPlace(rightRef7List, rightRef7Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef7List.get(rightRef7Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R7) - 1; // 从缓存读取整数值
                if (index < 0) {
                    index = mainViewGroup.getRightRef(TChan.R7).getRecyclerView().getAdapter().getItemCount() - 1;
                }
                mainViewGroup.getRightRef(TChan.R7).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef7Index = rightRef7Index == 0 ? rightRef7List.size() - 1 : rightRef7Index - 1;
                } while (!rightRef7List.get(rightRef7Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef7List, rightRef7Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8)) { // 判断Ref8菜单是否显示
            if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef8List.get(rightRef8Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_UP + CommandMsgToUI.PARAM_SPLIT + TChan.R8, 1); // 发送参考回调移动命令
                setViewPlace(rightRef8List, rightRef8Index, false); // 更新焦点框位置
            } else if (ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef8List.get(rightRef8Index).getType())) { // 判断是否为CSV列表节点
                int index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R8) - 1; // 从缓存读取整数值
                if (index < 0) {
                    index = mainViewGroup.getRightRef(TChan.R8).getRecyclerView().getAdapter().getItemCount() - 1;
                }
                mainViewGroup.getRightRef(TChan.R8).scrollToPositionAndGetView(index);
            } else {
                do { // 循环查找下一个可见节点
                    rightRef8Index = rightRef8Index == 0 ? rightRef8List.size() - 1 : rightRef8Index - 1;
                } while (!rightRef8List.get(rightRef8Index).isVisible()); // 跳过不可见节点
                setViewPlace(rightRef8List, rightRef8Index, false); // 更新焦点框位置
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1)) { // 判断S1串行总线菜单是否显示
            do { // 循环查找下一个可见节点
                rightS1Index = rightS1Index == 0 ? rightS1List.size() - 1 : rightS1Index - 1;
            } while (!rightS1List.get(rightS1Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightS1List, rightS1Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2)) { // 判断S2串行总线菜单是否显示
            do { // 循环查找下一个可见节点
                rightS2Index = rightS2Index == 0 ? rightS2List.size() - 1 : rightS2Index - 1;
            } while (!rightS2List.get(rightS2Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightS2List, rightS2Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3)) { // 判断S3串行总线菜单是否显示
            do { // 循环查找下一个可见节点
                rightS3Index = rightS3Index == 0 ? rightS3List.size() - 1 : rightS3Index - 1;
            } while (!rightS3List.get(rightS3Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightS3List, rightS3Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4)) { // 判断S4串行总线菜单是否显示
            do { // 循环查找下一个可见节点
                rightS4Index = rightS4Index == 0 ? rightS4List.size() - 1 : rightS4Index - 1;
            } while (!rightS4List.get(rightS4Index).isVisible()); // 跳过不可见节点
            setViewPlace(rightS4List, rightS4Index, false); // 更新焦点框位置
        } else if (mainViewGroup.isSlipShow(MainViewGroup.BOTTOMSLIP)) { // 判断底部菜单是否显示
            do { // 循环查找下一个可见节点
                bottomIndex = bottomIndex == 0 ? bottomList.size() - 1 : bottomIndex - 1;
            } while (!bottomList.get(bottomIndex).isVisible()); // 跳过不可见节点
            setViewPlace(bottomList, bottomIndex, false); // 更新焦点框位置
        } else if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) { // 从缓存读取布尔值
            do { // 循环查找下一个可见节点
                serialsWordIndex = serialsWordIndex == 0 ? serialsWordList.size() - 1 : serialsWordIndex - 1;
            } while (!serialsWordList.get(serialsWordIndex).isVisible()); // 跳过不可见节点

            updateSerialClearButton(); // 更新串行清除按钮位置
            setViewPlace(serialsWordList, serialsWordIndex, false); // 更新焦点框位置
        }
    }

    /**
     * 更新串行总线数据字区域清除按钮的位置
     */
    private void updateSerialClearButton(){
        serialsWordList.get(serialsWordList.size() - 1).setPlace(3, 1649, 1054, 120, 60); // 设置节点位置和尺寸
    }

    /**
     * 在通道选择区域左右移动焦点
     * @param isRight true为向右移动，false为向左移动
     * @param count 移动步数
     */
    public void moveChannelsSelect(boolean isRight, int count) {
        if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1) {//xy模式下不能�?�择通道
            return; // 直接返回
        }
        if (channelsLayout == null) { // 懒加载通道布局
            channelsLayout = (MainLayoutCenterChannel) mainViewGroup.findViewById(R.id.mainLayoutCenterChannels); // 获取通道布局控件
        }
        if (channelsCheckBox == null) { // 懒加载通道复选框
            channelsCheckBox = (CheckBox) mainViewGroup.findViewById(R.id.current); // 获取通道复选框控件
        }
        if (channelsLayout.setDialogVisible(View.VISIBLE) == View.VISIBLE) { // 设置通道对话框可见性
            if (!channelsCheckBox.isChecked()) {
                channelsCheckBox.setChecked(true); // 设置通道复选框选中状态
            }
            if (showViewIfGone(ExternalKeysProtocol.BACKSTATE_CHLIST)) {
                return; // 直接返回
            }
            mainViewGroup.hideAllSlip(); // 隐藏所有侧滑菜单
            showViewIfGone(ExternalKeysProtocol.BACKSTATE_CHLIST);
            do { // 循环查找下一个可见节点
                if (isRight) {
                    channelsIndex = channelsIndex == channelsList.size() - 1 ? 0 : channelsIndex + 1;
                } else {
                    channelsIndex = channelsIndex == 0 ? channelsList.size() - 1 : channelsIndex - 1;
                }
            } while (!channelsList.get(channelsIndex).isVisible()); // 跳过不可见节点
            setViewPlace(channelsList, channelsIndex, false); // 更新焦点框位置
        }
    }

    /**
     * 确认选中当前通道并触发通道点击事件
     */
    public void moveIntoChannelsSelect() {
        if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1) {//xy模式下不能�?�择通道
            return; // 直接返回
        }
        if (channelsLayout == null) { // 懒加载通道布局
            channelsLayout = (MainLayoutCenterChannel) mainViewGroup.findViewById(R.id.mainLayoutCenterChannels); // 获取通道布局控件
        }
        if (channelsCheckBox == null) { // 懒加载通道复选框
            channelsCheckBox = (CheckBox) mainViewGroup.findViewById(R.id.current); // 获取通道复选框控件
        }
        if (channelsLayout.setDialogVisible(View.VISIBLE) == View.VISIBLE) { // 设置通道对话框可见性
            if (!channelsCheckBox.isChecked()) {
                channelsCheckBox.setChecked(true); // 设置通道复选框选中状态
            }
            if (showViewIfGone(ExternalKeysProtocol.BACKSTATE_CHLIST)) {
                ExternalKeysUI.getInstance().setFocusControlBackVisible(false); // 设置焦点框返回按钮可见性
                return; // 直接返回
            }
            mainViewGroup.hideAllSlip(); // 隐藏所有侧滑菜单
            showViewIfGone(ExternalKeysProtocol.BACKSTATE_CHLIST);
            ExternalKeysUI.getInstance().onClickForChannelWindow( // 在通道窗口中模拟点击
                    channelsList.get(channelsIndex).getX() + channelsList.get(channelsIndex).getW() / 2
                    , channelsList.get(channelsIndex).getY() + channelsList.get(channelsIndex).getH() / 2);
        }
    }

    ///select into backState
    /**
     * 如果焦点框不可见，则根据backState显示对应区域的焦点
     * @param backState 返回状态标识(通道列表/分段存储)
     * @return true表示焦点框之前不可见且已恢复显示，false表示焦点框已可见
     */
    private boolean showViewIfGone(int backState){
        if (focusView == null) { // 懒加载焦点框控件
            focusView = (AnimationView) mainViewGroup.findViewById(R.id.focusControl); // 获取焦点框控件
        }
        if (focusView.getVisibility() == View.VISIBLE) {
            return false; // 返回false
        } else {
            switch (backState) { // switch分支选择
                case ExternalKeysProtocol.BACKSTATE_CHLIST:{
                    if (channelsLayout == null) { // 懒加载通道布局
                        channelsLayout = (MainLayoutCenterChannel) mainViewGroup.findViewById(R.id.mainLayoutCenterChannels); // 获取通道布局控件
                    }
                    if (channelsLayout.getVisibility() == View.VISIBLE) {
                        setViewPlace(channelsList, channelsIndex, false); // 更新焦点框位置
                    }
                }break;
                case ExternalKeysProtocol.BACKSTATE_SEGMENT:{
                    if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CENTER_SEGMENTED_VISIBLE)) { // 从缓存读取布尔值
                        setViewPlace(segmentedList, segmentedIndex, false); // 更新焦点框位置
                    }
                }break;
            }
            return true; // 返回true
        }
    }

    /**
     * 退出通道选择区域，隐藏焦点框
     */
    public void moveBackChannelsSelect() {
        if (channelsLayout == null) { // 懒加载通道布局
            channelsLayout = (MainLayoutCenterChannel) mainViewGroup.findViewById(R.id.mainLayoutCenterChannels); // 获取通道布局控件
        }
        if (channelsCheckBox == null) { // 懒加载通道复选框
            channelsCheckBox = (CheckBox) mainViewGroup.findViewById(R.id.current); // 获取通道复选框控件
        }
        if (channelsCheckBox.isChecked()) {
            channelsCheckBox.setChecked(false); // 设置通道复选框选中状态
            channelsLayout.setDialogVisible(View.GONE); // 设置通道对话框可见性
            if (focusView == null) { // 懒加载焦点框控件
                focusView = (AnimationView) mainViewGroup.findViewById(R.id.focusControl); // 获取焦点框控件
            }
            Logger.i("ExternalKeysManager,focusControl,moveBackChannelsSelect:" + View.GONE); // 信息日志输出
            setFocusViewVisible(View.GONE); // 设置焦点框可见性
        }
    }

    /**
     * 确认选中分段存储区域的当前项，进入子菜单或触发点击
     */
    public void moveIntoSegment() {
        if (showViewIfGone(ExternalKeysProtocol.BACKSTATE_SEGMENT)) {
            ExternalKeysUI.getInstance().setFocusControlBackVisible(false); // 设置焦点框返回按钮可见性
            return; // 直接返回
        }
        if (!ExternalKeysNode.TYPE_NO_CLICK.equals(segmentedList.get(segmentedIndex).getType())) { // 判断是否为不可点击类型节点
            ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                    segmentedList.get(segmentedIndex).getX() + segmentedList.get(segmentedIndex).getW() / 2
                    , segmentedList.get(segmentedIndex).getY() + segmentedList.get(segmentedIndex).getH() / 2);

        }
        if (layoutSegmented == null) { // 懒加载分段存储布局
            layoutSegmented = mainViewGroup.getCenterSegmentedLayout();
        }
        if ((layoutSegmented.isPlay() && layoutSegmented.getType() != MainLayoutCenterSegmented.TYPE_FIT
                && ExternalKeysNode.DIALOG_NUMBERKEYBOARD.equals(segmentedList.get(segmentedIndex).getDialog()))) {
            segmentedList.get(LIST_HEAD).setCurListSelect(segmentedIndex); // 设置列表选中项
            setViewPlace(segmentedList, segmentedIndex, true); // 更新焦点框位置
        } else if (segmentedList.get(segmentedIndex).getChildNodes() != null
                && segmentedList.get(segmentedIndex).getChildNodes().size() != 0) {
//            if (segmentedList.get(segmentedIndex).getDialog()==ExternalKeysNode.DIALOG_NUMBERKEYBOARD){
//                return;
//            }
            segmentedList.get(LIST_HEAD).setCurListSelect(segmentedIndex); // 设置列表选中项
            segmentedList = segmentedList.get(segmentedIndex).getChildNodes(); // 进入子菜单节点
            segmentedIndex = segmentedList.get(LIST_HEAD).getCurListSelect();
            setViewPlace(segmentedList, segmentedIndex, true); // 更新焦点框位置
        } else {
            segmentedList.get(LIST_HEAD).setCurListSelect(segmentedIndex); // 设置列表选中项
            setViewPlace(segmentedList, segmentedIndex, true); // 更新焦点框位置
        }
    }
    /**
     * 在分段存储区域左右移动焦点
     * @param isRight true为向右移动，false为向左移动
     * @param count 移动步数
     */
    public void moveSegment(boolean isRight, int count) {
        if (layoutSegmented == null) { // 懒加载分段存储布局
            layoutSegmented = (MainLayoutCenterSegmented) mainViewGroup.findViewById(R.id.mainLayoutCenterSegmented); // 获取分段存储布局控件
        }
        if(segmentedIndex >= segmentedList.size()){
            return; // 直接返回
        }
        if (isRight) {

            if (ExternalKeysNode.DIALOG_NUMBERKEYBOARD.equals(segmentedList.get(segmentedIndex).getDialog())
                    && segmentedList.get(segmentedIndex).getName().startsWith("Single")) {
                layoutSegmented.moveCurFrame(count); // 移动分段存储当前帧
            } else if (ExternalKeysNode.TYPE_CENTER_SEGMENT_PLAY.equals(segmentedList.get(segmentedIndex).getType()) // 判断是否为分段播放类型节点
                    && layoutSegmented.isPlay()) {
                layoutSegmented.changePlaySpeed(isRight); // 改变分段存储播放速度
            } else {
                do { // 循环查找下一个可见节点
                    segmentedIndex = segmentedIndex == segmentedList.size() - 1 ? 0 : segmentedIndex + 1;
                } while (!segmentedList.get(segmentedIndex).isVisible()); // 跳过不可见节点
            }
            setViewPlace(segmentedList, segmentedIndex, false); // 更新焦点框位置
        } else {
            if (ExternalKeysNode.DIALOG_NUMBERKEYBOARD.equals(segmentedList.get(segmentedIndex).getDialog())
                    && segmentedList.get(segmentedIndex).getName().startsWith("Single")) {
                layoutSegmented.moveCurFrame(count * -1); // 移动分段存储当前帧
            } else if (ExternalKeysNode.TYPE_CENTER_SEGMENT_PLAY.equals(segmentedList.get(segmentedIndex).getType()) // 判断是否为分段播放类型节点
                    && layoutSegmented.isPlay()) {
                layoutSegmented.changePlaySpeed(isRight); // 改变分段存储播放速度
            } else {
                do { // 循环查找下一个可见节点
                    segmentedIndex = segmentedIndex == 0 ? segmentedList.size() - 1 : segmentedIndex - 1;
                } while (!segmentedList.get(segmentedIndex).isVisible()); // 跳过不可见节点
            }
            setViewPlace(segmentedList, segmentedIndex, false); // 更新焦点框位置
        }
    }
    /**
     * moveSegment方法
     */
    public void moveSegment(int  key,boolean isRight, int count) {
        if (layoutSegmented == null) { // 懒加载分段存储布局
            layoutSegmented = (MainLayoutCenterSegmented) mainViewGroup.findViewById(R.id.mainLayoutCenterSegmented); // 获取分段存储布局控件
        }
        if (isForceDisplaycFocusView()) return;

        if(segmentedIndex >= segmentedList.size()){
            return; // 直接返回
        }
        if (isRight) {
            if (ExternalKeysNode.DIALOG_NUMBERKEYBOARD.equals(segmentedList.get(segmentedIndex).getDialog())
                    && segmentedList.get(segmentedIndex).getName().startsWith("Single")) {
                layoutSegmented.moveCurFrame(count); // 移动分段存储当前帧
            } else if (ExternalKeysNode.TYPE_CENTER_SEGMENT_PLAY.equals(segmentedList.get(segmentedIndex).getType()) // 判断是否为分段播放类型节点
                    && layoutSegmented.isPlay()) {
                layoutSegmented.changePlaySpeed(isRight); // 改变分段存储播放速度
            } else {
                if (key==ExternalKeysProtocol.KEY_SELECT_DOWN){ //下移
                    int[] curIdx=new int[1]; // 创建索引输出参数
                    segmentedList=moveUpDown(segmentedList, segmentedList.get(segmentedIndex), true, curIdx,false,false); // 搜索最近焦点节点
                    segmentedIndex=curIdx[0]; // 更新焦点索引
                }else { //右移
                    do { // 循环查找下一个可见节点
                        segmentedIndex = segmentedIndex == segmentedList.size() - 1 ? 0 : segmentedIndex + 1;
                    } while (!segmentedList.get(segmentedIndex).isVisible()); // 跳过不可见节点
                }
            }
            setViewPlace(segmentedList, segmentedIndex, false); // 更新焦点框位置
        } else {
            if (ExternalKeysNode.DIALOG_NUMBERKEYBOARD.equals(segmentedList.get(segmentedIndex).getDialog())
                    && segmentedList.get(segmentedIndex).getName().startsWith("Single")) {
                layoutSegmented.moveCurFrame(count * -1); // 移动分段存储当前帧
            } else if (ExternalKeysNode.TYPE_CENTER_SEGMENT_PLAY.equals(segmentedList.get(segmentedIndex).getType()) // 判断是否为分段播放类型节点
                    && layoutSegmented.isPlay()) {
                layoutSegmented.changePlaySpeed(isRight); // 改变分段存储播放速度
            } else {
                if (key==ExternalKeysProtocol.KEY_SELECT_UP ){
                    int[] curIdx=new int[1]; // 创建索引输出参数
                    segmentedList=moveUpDown(segmentedList, segmentedList.get(segmentedIndex), false, curIdx,false,false); // 搜索最近焦点节点
                    segmentedIndex=curIdx[0]; // 更新焦点索引
                }else {
                    do { // 循环查找下一个可见节点
                        segmentedIndex = segmentedIndex == 0 ? segmentedList.size() - 1 : segmentedIndex - 1;
                    } while (!segmentedList.get(segmentedIndex).isVisible()); // 跳过不可见节点
                }
            }
            setViewPlace(segmentedList, segmentedIndex, false); // 更新焦点框位置
        }
    }

    /**
     * 判断是否需要强制显示焦点视图
     * @return true表示焦点框不可见需要强制显示，false表示焦点框已可见
     */
    private boolean isForceDisplaycFocusView(){
        if (focusView==null){
            focusView = (AnimationView) mainViewGroup.findViewById(R.id.focusControl); // 获取焦点框控件
        }
        if (focusView.getVisibility()==View.GONE ) {
            setViewPlace(segmentedList, segmentedIndex, false); // 更新焦点框位置
            return true; // 返回true
        }
        return false; // 返回false
    }

    /**
     * 在对话框(确认/取消)区域左右移动焦点
     * @param isRight true为向右移动，false为向左移动
     * @param count 移动步数
     */
    public void moveDialogOkSelect(boolean isRight, int count) {
        if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_OKCANCEL)) { // 判断指定弹窗是否显示
            if (showViewIfGone()) { // 如果焦点框不可见则显示并返回
                return; // 直接返回
            }
            do { // 循环查找下一个可见节点
                if (isRight) {
                    okCancelIndex = okCancelIndex == okCancelList.size() - 1 ? 0 : okCancelIndex + 1;
                } else {
                    okCancelIndex = okCancelIndex == 0 ? okCancelList.size() - 1 : okCancelIndex - 1;
                }
            } while (!okCancelList.get(okCancelIndex).isVisible()); // 跳过不可见节点
            setViewPlace(okCancelList, okCancelIndex, false); // 更新焦点框位置
        } else if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_OK)) {
            if (showViewIfGone()) { // 如果焦点框不可见则显示并返回
                return; // 直接返回
            }
            okIndex = 0; // 初始化索引为0
            setViewPlace(okList, okIndex, false); // 更新焦点框位置
        }
    }

    /**
     * 确认选中对话框(确认/取消)的当前按钮
     */
    public void moveIntoDialogOkSelect() {
        if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_OKCANCEL)) { // 判断指定弹窗是否显示
            if (showViewIfGone()) { // 如果焦点框不可见则显示并返回
                return; // 直接返回
            }
            okCancelList.get(LIST_HEAD).setCurListSelect(okCancelIndex); // 设置列表选中项
            ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                    okCancelList.get(okCancelIndex).getX() + okCancelList.get(okCancelIndex).getW() / 2
                    , okCancelList.get(okCancelIndex).getY() + okCancelList.get(okCancelIndex).getH() / 2);
        } else if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_OK)) {
            if (showViewIfGone()) { // 如果焦点框不可见则显示并返回
                return; // 直接返回
            }
            okList.get(LIST_HEAD).setCurListSelect(okIndex); // 设置列表选中项
            ExternalKeysUI.getInstance().onClick(okList.get(okIndex).getX() + okList.get(okIndex).getW() / 2, okList.get(okIndex).getY() + okList.get(okIndex).getH() / 2); // 模拟点击UI控件
        }
    }

    /**
     * 是否是接收moveLeft、moveRight、moveInto等传递过来的消息
     */
    private boolean isReceiveMsgNode = false; // 取消消息接收标记

    /**
     * 旋钮按压确认：进入子菜单、触发点击或执行确认操作
     * 根据当前焦点节点的类型(TYPE_NO_CLICK/TYPE_CLICK_IS_SUREBACK等)执行不同逻辑
     */
    public void moveInto() {
        if (showViewIfGone()) { // 如果焦点框不可见则显示并返回
            return; // 直接返回
        }
        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)) { // 判断顶部菜单是否显示
            if (ExternalKeysNode.TYPE_RECEIVE_MSG.equals(topList.get(topIndex).getType()) // 判断是否为消息接收类型节点
                    || ExternalKeysNode.TYPE_TRIGGER_TITLE.equals(topList.get(topIndex).getType())) { // 判断是否为触发菜单滑动类型节点
                isReceiveMsgNode = true; // 标记为接收消息节点
            }

            if (ExternalKeysNode.TYPE_BRIGHTNESS_PROGRESS.equals(topList.get(topIndex).getType())) { // 判断是否为亮度进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            }else if(ExternalKeysNode.TYPE_TRIGGER_SENSITIVITY_PROGRESS.equals(topList.get(topIndex).getType())){ // 判断是否为触发灵敏度进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            }
            else if (ExternalKeysNode.TYPE_INTENSITY_PROGRESS.equals(topList.get(topIndex).getType())) { // 判断是否为网格亮度进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            }else if (ExternalKeysNode.TYPE_ALPHA_PROGRESS.equals(topList.get(topIndex).getType())){ // 判断是否为透明度进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            }
            else if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(topList.get(topIndex).getType())) { // 判断是否为计数进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_PERSIST_ADJUST.equals(topList.get(topIndex).getType())) { // 判断是否为余辉调节节点
                moveBack(); // 返回上级菜单
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(topList.get(topIndex).getType())) { // 判断是否为点击确认返回类型节点
                topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick(topList.get(topIndex).getX() + topList.get(topIndex).getW() / 2, topList.get(topIndex).getY() + topList.get(topIndex).getH() / 2); // 模拟点击UI控件
            } else if (ExternalKeysNode.TYPE_SPINNER_LIST.equals(topList.get(topIndex).getType()) && "SaveWavSpinnerList".equals(topList.get(topIndex).getName())) { // 判断是否为下拉列表节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
//                topList.get(topIndex).setPlace(topIndex, 154, 386, 600, 60);
//                setViewPlace(topList, topIndex, true);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(topList.get(topIndex).getType())) { // 判断是否为不可点击类型节点
                    topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                    ExternalKeysUI.getInstance().onClick(topList.get(topIndex).getX() + topList.get(topIndex).getW() / 2, topList.get(topIndex).getY() + topList.get(topIndex).getH() / 2); // 模拟点击UI控件
                }
                if (topList.get(topIndex).getChildNodes() != null && topList.get(topIndex).getChildNodes().size() != 0
                        && hasChildrenInTopTriggerSerialsCanIdDataData()
                        && hasChildrenInTopMeasureDelayAndPhase()) {
                    topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                    topList = topList.get(topIndex).getChildNodes(); // 进入子菜单节点
                    topIndex = topList.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(topList, topIndex, true); // 更新焦点框位置
                } else {
                    topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1)) { // 判断CH1通道菜单是否显示
            if (ExternalKeysNode.TYPE_RECEIVE_MSG.equals(rightCh1List.get(rightCh1Index).getType())) { // 判断是否为消息接收类型节点
                isReceiveMsgNode = true; // 标记为接收消息节点
            }
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh1List.get(rightCh1Index).getType()) // 判断是否为计数进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh1List.get(rightCh1Index).getType()) // 判断是否为通道延迟进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh1List.get(rightCh1Index).getType()) // 判断是否为通道延迟单位节点
            ) {
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightCh1List.get(rightCh1Index).getType())) { // 判断是否为点击确认返回类型节点
                rightCh1List.get(LIST_HEAD).setCurListSelect(rightCh1Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightCh1List.get(rightCh1Index).getX() + rightCh1List.get(rightCh1Index).getW() / 2
                        , rightCh1List.get(rightCh1Index).getY() + rightCh1List.get(rightCh1Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightCh1List.get(rightCh1Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightCh1List.get(rightCh1Index).getX() + rightCh1List.get(rightCh1Index).getW() / 2
                            , rightCh1List.get(rightCh1Index).getY() + rightCh1List.get(rightCh1Index).getH() / 2);
                }
                if (rightCh1List.get(rightCh1Index).getChildNodes() != null && rightCh1List.get(rightCh1Index).getChildNodes().size() != 0) {
                    if (rightCh1List.get(rightCh1Index).getParentNodes()==null){
                        rightCh1List.get(LIST_HEAD).setCurListSelect(rightCh1Index); // 设置列表选中项
                        rightCh1List = rightCh1List.get(rightCh1Index).getChildNodes(); // 进入子菜单节点
                        if (rightCh1Index>=rightCh1List.size()|| rightCh1Index<0) rightCh1Index=0;
                        rightCh1Index = rightCh1List.get(rightCh1Index).getCurListSelect();
                        setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
                    }else{
                        rightCh1List= rightCh1List.get(rightCh1Index).getChildNodes(); // 进入子菜单节点
                        rightCh1Index=rightCh1List.get(LIST_HEAD).getIndex();
                        setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
                    }
                } else {
                    rightCh1List.get(LIST_HEAD).setCurListSelect(rightCh1Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2)) { // 判断CH2通道菜单是否显示
            if (ExternalKeysNode.TYPE_RECEIVE_MSG.equals(rightCh2List.get(rightCh2Index).getType())) { // 判断是否为消息接收类型节点
                isReceiveMsgNode = true; // 标记为接收消息节点
            }
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh2List.get(rightCh2Index).getType()) // 判断是否为计数进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh2List.get(rightCh2Index).getType()) // 判断是否为通道延迟进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh2List.get(rightCh2Index).getType()) // 判断是否为通道延迟单位节点
            ) {
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightCh2List.get(rightCh2Index).getType())) { // 判断是否为点击确认返回类型节点
                rightCh2List.get(LIST_HEAD).setCurListSelect(rightCh2Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightCh2List.get(rightCh2Index).getX() + rightCh2List.get(rightCh2Index).getW() / 2
                        , rightCh2List.get(rightCh2Index).getY() + rightCh2List.get(rightCh2Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightCh2List.get(rightCh2Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightCh2List.get(rightCh2Index).getX() + rightCh2List.get(rightCh2Index).getW() / 2
                            , rightCh2List.get(rightCh2Index).getY() + rightCh2List.get(rightCh2Index).getH() / 2);
                }
                if (rightCh2List.get(rightCh2Index).getChildNodes() != null
                        && rightCh2List.get(rightCh2Index).getChildNodes().size() != 0) {
                    if (rightCh2List.get(rightCh2Index).getParentNodes()==null){
                        rightCh2List.get(LIST_HEAD).setCurListSelect(rightCh2Index); // 设置列表选中项
                        rightCh2List = rightCh2List.get(rightCh2Index).getChildNodes(); // 进入子菜单节点
                        if (rightCh2Index>=rightCh2List.size()|| rightCh2Index<0) rightCh2Index=0;
                        rightCh2Index = rightCh2List.get(rightCh2Index).getCurListSelect();
                        setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
                    }else{
                        rightCh2List= rightCh2List.get(rightCh2Index).getChildNodes(); // 进入子菜单节点
                        rightCh2Index=rightCh2List.get(LIST_HEAD).getIndex();
                        setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
                    }
                } else {
                    rightCh2List.get(LIST_HEAD).setCurListSelect(rightCh2Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3)) { // 判断CH3通道菜单是否显示
            if (ExternalKeysNode.TYPE_RECEIVE_MSG.equals(rightCh3List.get(rightCh3Index).getType())) { // 判断是否为消息接收类型节点
                isReceiveMsgNode = true; // 标记为接收消息节点
            }
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh3List.get(rightCh3Index).getType()) // 判断是否为计数进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh3List.get(rightCh3Index).getType()) // 判断是否为通道延迟进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh3List.get(rightCh3Index).getType()) // 判断是否为通道延迟单位节点
            ) {
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightCh3List.get(rightCh3Index).getType())) { // 判断是否为点击确认返回类型节点
                rightCh3List.get(LIST_HEAD).setCurListSelect(rightCh3Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightCh3List.get(rightCh3Index).getX() + rightCh3List.get(rightCh3Index).getW() / 2
                        , rightCh3List.get(rightCh3Index).getY() + rightCh3List.get(rightCh3Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightCh3List.get(rightCh3Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightCh3List.get(rightCh3Index).getX() + rightCh3List.get(rightCh3Index).getW() / 2
                            , rightCh3List.get(rightCh3Index).getY() + rightCh3List.get(rightCh3Index).getH() / 2);
                }
                if (rightCh3List.get(rightCh3Index).getChildNodes() != null
                        && rightCh3List.get(rightCh3Index).getChildNodes().size() != 0) {
                    if (rightCh3List.get(rightCh3Index).getParentNodes()==null){
                        rightCh3List.get(LIST_HEAD).setCurListSelect(rightCh3Index); // 设置列表选中项
                        rightCh3List = rightCh3List.get(rightCh3Index).getChildNodes(); // 进入子菜单节点
                        if (rightCh3Index>=rightCh3List.size()|| rightCh3Index<0) rightCh3Index=0;
                        rightCh3Index = rightCh3List.get(rightCh3Index).getCurListSelect();
                        setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
                    }else{
                        rightCh3List= rightCh3List.get(rightCh3Index).getChildNodes(); // 进入子菜单节点
                        rightCh3Index=rightCh3List.get(LIST_HEAD).getIndex();
                        setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
                    }
                } else {
                    rightCh3List.get(LIST_HEAD).setCurListSelect(rightCh3Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4)) { // 判断CH4通道菜单是否显示
            if (ExternalKeysNode.TYPE_RECEIVE_MSG.equals(rightCh4List.get(rightCh4Index).getType())) { // 判断是否为消息接收类型节点
                isReceiveMsgNode = true; // 标记为接收消息节点
            }
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh4List.get(rightCh4Index).getType()) // 判断是否为计数进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh4List.get(rightCh4Index).getType()) // 判断是否为通道延迟进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh4List.get(rightCh4Index).getType()) // 判断是否为通道延迟单位节点
            ) {
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightCh4List.get(rightCh4Index).getType())) { // 判断是否为点击确认返回类型节点
                rightCh4List.get(LIST_HEAD).setCurListSelect(rightCh4Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightCh4List.get(rightCh4Index).getX() + rightCh4List.get(rightCh4Index).getW() / 2
                        , rightCh4List.get(rightCh4Index).getY() + rightCh4List.get(rightCh4Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightCh4List.get(rightCh4Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightCh4List.get(rightCh4Index).getX() + rightCh4List.get(rightCh4Index).getW() / 2
                            , rightCh4List.get(rightCh4Index).getY() + rightCh4List.get(rightCh4Index).getH() / 2);
                }
                if (rightCh4List.get(rightCh4Index).getChildNodes() != null
                        && rightCh4List.get(rightCh4Index).getChildNodes().size() != 0) {
                    if (rightCh4List.get(rightCh4Index).getParentNodes()==null){
                        rightCh4List.get(LIST_HEAD).setCurListSelect(rightCh4Index); // 设置列表选中项
                        rightCh4List = rightCh4List.get(rightCh4Index).getChildNodes(); // 进入子菜单节点
                        if (rightCh4Index>=rightCh4List.size()|| rightCh4Index<0) rightCh4Index=0;
                        rightCh4Index = rightCh4List.get(rightCh4Index).getCurListSelect();
                        setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
                    }else{
                        rightCh4List= rightCh4List.get(rightCh4Index).getChildNodes(); // 进入子菜单节点
                        rightCh4Index=rightCh4List.get(LIST_HEAD).getIndex();
                        setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
                    }
                } else {
                    rightCh4List.get(LIST_HEAD).setCurListSelect(rightCh4Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5)) { // 判断CH5通道菜单是否显示
            if (ExternalKeysNode.TYPE_RECEIVE_MSG.equals(rightCh5List.get(rightCh5Index).getType())) { // 判断是否为消息接收类型节点
                isReceiveMsgNode = true; // 标记为接收消息节点
            }
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh5List.get(rightCh5Index).getType()) // 判断是否为计数进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh5List.get(rightCh5Index).getType()) // 判断是否为通道延迟进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh5List.get(rightCh5Index).getType()) // 判断是否为通道延迟单位节点
            ) {
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightCh5List.get(rightCh5Index).getType())) { // 判断是否为点击确认返回类型节点
                rightCh5List.get(LIST_HEAD).setCurListSelect(rightCh5Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightCh5List.get(rightCh5Index).getX() + rightCh5List.get(rightCh5Index).getW() / 2
                        , rightCh5List.get(rightCh5Index).getY() + rightCh5List.get(rightCh5Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightCh5List.get(rightCh5Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightCh5List.get(rightCh5Index).getX() + rightCh5List.get(rightCh5Index).getW() / 2
                            , rightCh5List.get(rightCh5Index).getY() + rightCh5List.get(rightCh5Index).getH() / 2);
                }
                if (rightCh5List.get(rightCh5Index).getChildNodes() != null
                        && rightCh5List.get(rightCh5Index).getChildNodes().size() != 0) {
                    if (rightCh5List.get(rightCh5Index).getParentNodes() == null) {
                        rightCh5List.get(LIST_HEAD).setCurListSelect(rightCh5Index); // 设置列表选中项
                        rightCh5List = rightCh5List.get(rightCh5Index).getChildNodes(); // 进入子菜单节点
                        if (rightCh5Index >= rightCh5List.size() || rightCh5Index < 0)
                            rightCh5Index = 0; // 初始化索引为0
                        rightCh5Index = rightCh5List.get(rightCh5Index).getCurListSelect();
                        setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
                    } else {
                        rightCh5List = rightCh5List.get(rightCh5Index).getChildNodes(); // 进入子菜单节点
                        rightCh5Index = rightCh5List.get(LIST_HEAD).getIndex();
                        setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
                    }
                } else {
                    rightCh5List.get(LIST_HEAD).setCurListSelect(rightCh5Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6)) { // 判断CH6通道菜单是否显示
            if (ExternalKeysNode.TYPE_RECEIVE_MSG.equals(rightCh6List.get(rightCh6Index).getType())) { // 判断是否为消息接收类型节点
                isReceiveMsgNode = true; // 标记为接收消息节点
            }
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh6List.get(rightCh6Index).getType()) // 判断是否为计数进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh6List.get(rightCh6Index).getType()) // 判断是否为通道延迟进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh6List.get(rightCh6Index).getType()) // 判断是否为通道延迟单位节点
            ) {
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightCh6List.get(rightCh6Index).getType())) { // 判断是否为点击确认返回类型节点
                rightCh6List.get(LIST_HEAD).setCurListSelect(rightCh6Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightCh6List.get(rightCh6Index).getX() + rightCh6List.get(rightCh6Index).getW() / 2
                        , rightCh6List.get(rightCh6Index).getY() + rightCh6List.get(rightCh6Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightCh6List.get(rightCh6Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightCh6List.get(rightCh6Index).getX() + rightCh6List.get(rightCh6Index).getW() / 2
                            , rightCh6List.get(rightCh6Index).getY() + rightCh6List.get(rightCh6Index).getH() / 2);
                }
                if (rightCh6List.get(rightCh6Index).getChildNodes() != null
                        && rightCh6List.get(rightCh6Index).getChildNodes().size() != 0) {
                    if (rightCh6List.get(rightCh6Index).getParentNodes() == null) {
                        rightCh6List.get(LIST_HEAD).setCurListSelect(rightCh6Index); // 设置列表选中项
                        rightCh6List = rightCh6List.get(rightCh6Index).getChildNodes(); // 进入子菜单节点
                        if (rightCh6Index >= rightCh6List.size() || rightCh6Index < 0)
                            rightCh6Index = 0; // 初始化索引为0
                        rightCh6Index = rightCh6List.get(rightCh6Index).getCurListSelect();
                        setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
                    } else {
                        rightCh6List = rightCh6List.get(rightCh6Index).getChildNodes(); // 进入子菜单节点
                        rightCh6Index = rightCh6List.get(LIST_HEAD).getIndex();
                        setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
                    }
                } else {
                    rightCh6List.get(LIST_HEAD).setCurListSelect(rightCh6Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7)) { // 判断CH7通道菜单是否显示
            if (ExternalKeysNode.TYPE_RECEIVE_MSG.equals(rightCh7List.get(rightCh7Index).getType())) { // 判断是否为消息接收类型节点
                isReceiveMsgNode = true; // 标记为接收消息节点
            }
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh7List.get(rightCh7Index).getType()) // 判断是否为计数进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh7List.get(rightCh7Index).getType()) // 判断是否为通道延迟进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh7List.get(rightCh7Index).getType()) // 判断是否为通道延迟单位节点
            ) {
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightCh7List.get(rightCh7Index).getType())) { // 判断是否为点击确认返回类型节点
                rightCh7List.get(LIST_HEAD).setCurListSelect(rightCh7Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightCh7List.get(rightCh7Index).getX() + rightCh7List.get(rightCh7Index).getW() / 2
                        , rightCh7List.get(rightCh7Index).getY() + rightCh7List.get(rightCh7Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightCh7List.get(rightCh7Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightCh7List.get(rightCh7Index).getX() + rightCh7List.get(rightCh7Index).getW() / 2
                            , rightCh7List.get(rightCh7Index).getY() + rightCh7List.get(rightCh7Index).getH() / 2);
                }
                if (rightCh7List.get(rightCh7Index).getChildNodes() != null
                        && rightCh7List.get(rightCh7Index).getChildNodes().size() != 0) {
                    if (rightCh7List.get(rightCh7Index).getParentNodes() == null) {
                        rightCh7List.get(LIST_HEAD).setCurListSelect(rightCh7Index); // 设置列表选中项
                        rightCh7List = rightCh7List.get(rightCh7Index).getChildNodes(); // 进入子菜单节点
                        if (rightCh7Index >= rightCh7List.size() || rightCh7Index < 0)
                            rightCh7Index = 0; // 初始化索引为0
                        rightCh7Index = rightCh7List.get(rightCh7Index).getCurListSelect();
                        setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
                    } else {
                        rightCh7List = rightCh7List.get(rightCh7Index).getChildNodes(); // 进入子菜单节点
                        rightCh7Index = rightCh7List.get(LIST_HEAD).getIndex();
                        setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
                    }
                } else {
                    rightCh7List.get(LIST_HEAD).setCurListSelect(rightCh7Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8)) { // 判断CH8通道菜单是否显示
            if (ExternalKeysNode.TYPE_RECEIVE_MSG.equals(rightCh8List.get(rightCh8Index).getType())) { // 判断是否为消息接收类型节点
                isReceiveMsgNode = true; // 标记为接收消息节点
            }
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh8List.get(rightCh8Index).getType()) // 判断是否为计数进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh8List.get(rightCh8Index).getType()) // 判断是否为通道延迟进度条节点
                    || ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh8List.get(rightCh8Index).getType()) // 判断是否为通道延迟单位节点
            ) {
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightCh8List.get(rightCh8Index).getType())) { // 判断是否为点击确认返回类型节点
                rightCh8List.get(LIST_HEAD).setCurListSelect(rightCh8Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightCh8List.get(rightCh8Index).getX() + rightCh8List.get(rightCh8Index).getW() / 2
                        , rightCh8List.get(rightCh8Index).getY() + rightCh8List.get(rightCh8Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightCh8List.get(rightCh8Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightCh8List.get(rightCh8Index).getX() + rightCh8List.get(rightCh8Index).getW() / 2
                            , rightCh8List.get(rightCh8Index).getY() + rightCh8List.get(rightCh8Index).getH() / 2);
                }
                if (rightCh8List.get(rightCh8Index).getChildNodes() != null
                        && rightCh8List.get(rightCh8Index).getChildNodes().size() != 0) {
                    if (rightCh8List.get(rightCh8Index).getParentNodes() == null) {
                        rightCh8List.get(LIST_HEAD).setCurListSelect(rightCh8Index); // 设置列表选中项
                        rightCh8List = rightCh8List.get(rightCh8Index).getChildNodes(); // 进入子菜单节点
                        if (rightCh8Index >= rightCh8List.size() || rightCh8Index < 0)
                            rightCh8Index = 0; // 初始化索引为0
                        rightCh8Index = rightCh8List.get(rightCh8Index).getCurListSelect();
                        setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
                    } else {
                        rightCh8List = rightCh8List.get(rightCh8Index).getChildNodes(); // 进入子菜单节点
                        rightCh8Index = rightCh8List.get(LIST_HEAD).getIndex();
                        setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
                    }
                } else {
                    rightCh8List.get(LIST_HEAD).setCurListSelect(rightCh8Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1)) { // 判断Math1菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightMath1List.get(rightMath1Index).getType())) { // 判断是否为计数进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightMath1List.get(rightMath1Index).getType())) { // 判断是否为点击确认返回类型节点
                rightMath1List.get(LIST_HEAD).setCurListSelect(rightMath1Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightMath1List.get(rightMath1Index).getX() + rightMath1List.get(rightMath1Index).getW() / 2
                        , rightMath1List.get(rightMath1Index).getY() + rightMath1List.get(rightMath1Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightMath1List.get(rightMath1Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightMath1List.get(rightMath1Index).getX() + rightMath1List.get(rightMath1Index).getW() / 2
                            , rightMath1List.get(rightMath1Index).getY() + rightMath1List.get(rightMath1Index).getH() / 2);
                }
                if (rightMath1List.get(rightMath1Index).getChildNodes() != null
                        && rightMath1List.get(rightMath1Index).getChildNodes().size() != 0) {
                    rightMath1List = rightMath1List.get(rightMath1Index).getChildNodes(); // 进入子菜单节点
                    rightMath1Index = rightMath1List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightMath1List, rightMath1Index, true); // 更新焦点框位置
                } else {
                    rightMath1List.get(LIST_HEAD).setCurListSelect(rightMath1Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2)) { // 判断Math2菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightMath2List.get(rightMath2Index).getType())) { // 判断是否为计数进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightMath2List.get(rightMath2Index).getType())) { // 判断是否为点击确认返回类型节点
                rightMath2List.get(LIST_HEAD).setCurListSelect(rightMath2Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightMath2List.get(rightMath2Index).getX() + rightMath2List.get(rightMath2Index).getW() / 2
                        , rightMath2List.get(rightMath2Index).getY() + rightMath2List.get(rightMath2Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightMath2List.get(rightMath2Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightMath2List.get(rightMath2Index).getX() + rightMath2List.get(rightMath2Index).getW() / 2
                            , rightMath2List.get(rightMath2Index).getY() + rightMath2List.get(rightMath2Index).getH() / 2);
                }
                if (rightMath2List.get(rightMath2Index).getChildNodes() != null
                        && rightMath2List.get(rightMath2Index).getChildNodes().size() != 0) {
                    rightMath2List = rightMath2List.get(rightMath2Index).getChildNodes(); // 进入子菜单节点
                    rightMath2Index = rightMath2List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightMath2List, rightMath2Index, true); // 更新焦点框位置
                } else {
                    rightMath2List.get(LIST_HEAD).setCurListSelect(rightMath2Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3)) { // 判断Math3菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightMath3List.get(rightMath3Index).getType())) { // 判断是否为计数进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightMath3List.get(rightMath3Index).getType())) { // 判断是否为点击确认返回类型节点
                rightMath3List.get(LIST_HEAD).setCurListSelect(rightMath3Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightMath3List.get(rightMath3Index).getX() + rightMath3List.get(rightMath3Index).getW() / 2
                        , rightMath3List.get(rightMath3Index).getY() + rightMath3List.get(rightMath3Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightMath3List.get(rightMath3Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightMath3List.get(rightMath3Index).getX() + rightMath3List.get(rightMath3Index).getW() / 2
                            , rightMath3List.get(rightMath3Index).getY() + rightMath3List.get(rightMath3Index).getH() / 2);
                }
                if (rightMath3List.get(rightMath3Index).getChildNodes() != null
                        && rightMath3List.get(rightMath3Index).getChildNodes().size() != 0) {
                    rightMath3List = rightMath3List.get(rightMath3Index).getChildNodes(); // 进入子菜单节点
                    rightMath3Index = rightMath3List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightMath3List, rightMath3Index, true); // 更新焦点框位置
                } else {
                    rightMath3List.get(LIST_HEAD).setCurListSelect(rightMath3Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4)) { // 判断Math4菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightMath4List.get(rightMath4Index).getType())) { // 判断是否为计数进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightMath4List.get(rightMath4Index).getType())) { // 判断是否为点击确认返回类型节点
                rightMath4List.get(LIST_HEAD).setCurListSelect(rightMath4Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightMath4List.get(rightMath4Index).getX() + rightMath4List.get(rightMath4Index).getW() / 2
                        , rightMath4List.get(rightMath4Index).getY() + rightMath4List.get(rightMath4Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightMath4List.get(rightMath4Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightMath4List.get(rightMath4Index).getX() + rightMath4List.get(rightMath4Index).getW() / 2
                            , rightMath4List.get(rightMath4Index).getY() + rightMath4List.get(rightMath4Index).getH() / 2);
                }
                if (rightMath4List.get(rightMath4Index).getChildNodes() != null
                        && rightMath4List.get(rightMath4Index).getChildNodes().size() != 0) {
                    rightMath4List = rightMath4List.get(rightMath4Index).getChildNodes(); // 进入子菜单节点
                    rightMath4Index = rightMath4List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightMath4List, rightMath4Index, true); // 更新焦点框位置
                } else {
                    rightMath4List.get(LIST_HEAD).setCurListSelect(rightMath4Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5)) { // 判断Math5菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightMath5List.get(rightMath5Index).getType())) { // 判断是否为计数进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightMath5List.get(rightMath5Index).getType())) { // 判断是否为点击确认返回类型节点
                rightMath5List.get(LIST_HEAD).setCurListSelect(rightMath5Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightMath5List.get(rightMath5Index).getX() + rightMath5List.get(rightMath5Index).getW() / 2
                        , rightMath5List.get(rightMath5Index).getY() + rightMath5List.get(rightMath5Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightMath5List.get(rightMath5Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightMath5List.get(rightMath5Index).getX() + rightMath5List.get(rightMath5Index).getW() / 2
                            , rightMath5List.get(rightMath5Index).getY() + rightMath5List.get(rightMath5Index).getH() / 2);
                }
                if (rightMath5List.get(rightMath5Index).getChildNodes() != null
                        && rightMath5List.get(rightMath5Index).getChildNodes().size() != 0) {
                    rightMath5List = rightMath5List.get(rightMath5Index).getChildNodes(); // 进入子菜单节点
                    rightMath5Index = rightMath5List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightMath5List, rightMath5Index, true); // 更新焦点框位置
                } else {
                    rightMath5List.get(LIST_HEAD).setCurListSelect(rightMath5Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6)) { // 判断Math6菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightMath6List.get(rightMath6Index).getType())) { // 判断是否为计数进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightMath6List.get(rightMath6Index).getType())) { // 判断是否为点击确认返回类型节点
                rightMath6List.get(LIST_HEAD).setCurListSelect(rightMath6Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightMath6List.get(rightMath6Index).getX() + rightMath6List.get(rightMath6Index).getW() / 2
                        , rightMath6List.get(rightMath6Index).getY() + rightMath6List.get(rightMath6Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightMath6List.get(rightMath6Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightMath6List.get(rightMath6Index).getX() + rightMath6List.get(rightMath6Index).getW() / 2
                            , rightMath6List.get(rightMath6Index).getY() + rightMath6List.get(rightMath6Index).getH() / 2);
                }
                if (rightMath6List.get(rightMath6Index).getChildNodes() != null
                        && rightMath6List.get(rightMath6Index).getChildNodes().size() != 0) {
                    rightMath6List = rightMath6List.get(rightMath6Index).getChildNodes(); // 进入子菜单节点
                    rightMath6Index = rightMath6List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightMath6List, rightMath6Index, true); // 更新焦点框位置
                } else {
                    rightMath6List.get(LIST_HEAD).setCurListSelect(rightMath6Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7)) { // 判断Math7菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightMath7List.get(rightMath7Index).getType())) { // 判断是否为计数进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightMath7List.get(rightMath7Index).getType())) { // 判断是否为点击确认返回类型节点
                rightMath7List.get(LIST_HEAD).setCurListSelect(rightMath7Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightMath7List.get(rightMath7Index).getX() + rightMath7List.get(rightMath7Index).getW() / 2
                        , rightMath7List.get(rightMath7Index).getY() + rightMath7List.get(rightMath7Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightMath7List.get(rightMath7Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightMath7List.get(rightMath7Index).getX() + rightMath7List.get(rightMath7Index).getW() / 2
                            , rightMath7List.get(rightMath7Index).getY() + rightMath7List.get(rightMath7Index).getH() / 2);
                }
                if (rightMath7List.get(rightMath7Index).getChildNodes() != null
                        && rightMath7List.get(rightMath7Index).getChildNodes().size() != 0) {
                    rightMath7List = rightMath7List.get(rightMath7Index).getChildNodes(); // 进入子菜单节点
                    rightMath7Index = rightMath7List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightMath7List, rightMath7Index, true); // 更新焦点框位置
                } else {
                    rightMath7List.get(LIST_HEAD).setCurListSelect(rightMath7Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8)) { // 判断Math8菜单是否显示
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightMath8List.get(rightMath8Index).getType())) { // 判断是否为计数进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightMath8List.get(rightMath8Index).getType())) { // 判断是否为点击确认返回类型节点
                rightMath8List.get(LIST_HEAD).setCurListSelect(rightMath8Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightMath8List.get(rightMath8Index).getX() + rightMath8List.get(rightMath8Index).getW() / 2
                        , rightMath8List.get(rightMath8Index).getY() + rightMath8List.get(rightMath8Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightMath8List.get(rightMath8Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightMath8List.get(rightMath8Index).getX() + rightMath8List.get(rightMath8Index).getW() / 2
                            , rightMath8List.get(rightMath8Index).getY() + rightMath8List.get(rightMath8Index).getH() / 2);
                }
                if (rightMath8List.get(rightMath8Index).getChildNodes() != null
                        && rightMath8List.get(rightMath8Index).getChildNodes().size() != 0) {
                    rightMath8List = rightMath8List.get(rightMath8Index).getChildNodes(); // 进入子菜单节点
                    rightMath8Index = rightMath8List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightMath8List, rightMath8Index, true); // 更新焦点框位置
                } else {
                    rightMath8List.get(LIST_HEAD).setCurListSelect(rightMath8Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1)) { // 判断Ref1菜单是否显示
            File[] files = SaveManage.getInstance().getFliesFromCurRef(Tools.SaveDir_CSVWAVE);
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightRef1List.get(rightRef1Index).getType()) // 判断是否为计数进度条节点
                    || (files.length == 0 && ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef1List.get(rightRef1Index).getType()))) { // 判断是否为CSV列表节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef1List.get(rightRef1Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_FINISH + CommandMsgToUI.PARAM_SPLIT + TChan.R1, 1); // 发送参考回调移动命令
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightRef1List.get(rightRef1Index).getType())) { // 判断是否为点击确认返回类型节点
                rightRef1List.get(LIST_HEAD).setCurListSelect(rightRef1Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightRef1List.get(rightRef1Index).getX() + rightRef1List.get(rightRef1Index).getW() / 2
                        , rightRef1List.get(rightRef1Index).getY() + rightRef1List.get(rightRef1Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightRef1List.get(rightRef1Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightRef1List.get(rightRef1Index).getX() + rightRef1List.get(rightRef1Index).getW() / 2
                            , rightRef1List.get(rightRef1Index).getY() + rightRef1List.get(rightRef1Index).getH() / 2);
                }
                if (rightRef1List.get(rightRef1Index).getChildNodes() != null
                        && rightRef1List.get(rightRef1Index).getChildNodes().size() != 0) {
                    rightRef1List = rightRef1List.get(rightRef1Index).getChildNodes(); // 进入子菜单节点
                    rightRef1Index = rightRef1List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightRef1List, rightRef1Index, true); // 更新焦点框位置
                } else {
                    rightRef1List.get(LIST_HEAD).setCurListSelect(rightRef1Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2)) { // 判断Ref2菜单是否显示
            File[] files = SaveManage.getInstance().getFliesFromCurRef(Tools.SaveDir_CSVWAVE);
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightRef2List.get(rightRef2Index).getType()) // 判断是否为计数进度条节点
                    || (files.length == 0 && ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef2List.get(rightRef2Index).getType()))) { // 判断是否为CSV列表节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef2List.get(rightRef2Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_FINISH + CommandMsgToUI.PARAM_SPLIT + TChan.R2, 1); // 发送参考回调移动命令
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightRef2List.get(rightRef2Index).getType())) { // 判断是否为点击确认返回类型节点
                rightRef2List.get(LIST_HEAD).setCurListSelect(rightRef2Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightRef2List.get(rightRef2Index).getX() + rightRef2List.get(rightRef2Index).getW() / 2
                        , rightRef2List.get(rightRef2Index).getY() + rightRef2List.get(rightRef2Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightRef2List.get(rightRef2Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightRef2List.get(rightRef2Index).getX() + rightRef2List.get(rightRef2Index).getW() / 2
                            , rightRef2List.get(rightRef2Index).getY() + rightRef2List.get(rightRef2Index).getH() / 2);
                }
                if (rightRef2List.get(rightRef2Index).getChildNodes() != null
                        && rightRef2List.get(rightRef2Index).getChildNodes().size() != 0) {
                    rightRef2List = rightRef2List.get(rightRef2Index).getChildNodes(); // 进入子菜单节点
                    rightRef2Index = rightRef2List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightRef2List, rightRef2Index, true); // 更新焦点框位置
                } else {
                    rightRef2List.get(LIST_HEAD).setCurListSelect(rightRef2Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3)) { // 判断Ref3菜单是否显示
            File[] files = SaveManage.getInstance().getFliesFromCurRef(Tools.SaveDir_CSVWAVE);
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightRef3List.get(rightRef3Index).getType()) // 判断是否为计数进度条节点
                    || (files.length == 0 && ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef3List.get(rightRef3Index).getType()))) { // 判断是否为CSV列表节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef3List.get(rightRef3Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_FINISH + CommandMsgToUI.PARAM_SPLIT + TChan.R3, 1); // 发送参考回调移动命令
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightRef3List.get(rightRef3Index).getType())) { // 判断是否为点击确认返回类型节点
                rightRef3List.get(LIST_HEAD).setCurListSelect(rightRef3Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightRef3List.get(rightRef3Index).getX() + rightRef3List.get(rightRef3Index).getW() / 2
                        , rightRef3List.get(rightRef3Index).getY() + rightRef3List.get(rightRef3Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightRef3List.get(rightRef3Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightRef3List.get(rightRef3Index).getX() + rightRef3List.get(rightRef3Index).getW() / 2
                            , rightRef3List.get(rightRef3Index).getY() + rightRef3List.get(rightRef3Index).getH() / 2);
                }
                if (rightRef3List.get(rightRef3Index).getChildNodes() != null
                        && rightRef3List.get(rightRef3Index).getChildNodes().size() != 0) {
                    rightRef3List = rightRef3List.get(rightRef3Index).getChildNodes(); // 进入子菜单节点
                    rightRef3Index = rightRef3List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightRef3List, rightRef3Index, true); // 更新焦点框位置
                } else {
                    rightRef3List.get(LIST_HEAD).setCurListSelect(rightRef3Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4)) { // 判断Ref4菜单是否显示
            File[] files = SaveManage.getInstance().getFliesFromCurRef(Tools.SaveDir_CSVWAVE);
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightRef4List.get(rightRef4Index).getType()) // 判断是否为计数进度条节点
                    || (files.length == 0 && ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef4List.get(rightRef4Index).getType()))) { // 判断是否为CSV列表节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef4List.get(rightRef4Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_FINISH + CommandMsgToUI.PARAM_SPLIT + TChan.R4, 1); // 发送参考回调移动命令
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightRef4List.get(rightRef4Index).getType())) { // 判断是否为点击确认返回类型节点
                rightRef4List.get(LIST_HEAD).setCurListSelect(rightRef4Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightRef4List.get(rightRef4Index).getX() + rightRef4List.get(rightRef4Index).getW() / 2
                        , rightRef4List.get(rightRef4Index).getY() + rightRef4List.get(rightRef4Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightRef4List.get(rightRef4Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightRef4List.get(rightRef4Index).getX() + rightRef4List.get(rightRef4Index).getW() / 2
                            , rightRef4List.get(rightRef4Index).getY() + rightRef4List.get(rightRef4Index).getH() / 2);
                }
                if (rightRef4List.get(rightRef4Index).getChildNodes() != null
                        && rightRef4List.get(rightRef4Index).getChildNodes().size() != 0) {
                    rightRef4List = rightRef4List.get(rightRef4Index).getChildNodes(); // 进入子菜单节点
                    rightRef4Index = rightRef4List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightRef4List, rightRef4Index, true); // 更新焦点框位置
                } else {
                    rightRef4List.get(LIST_HEAD).setCurListSelect(rightRef4Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5)) { // 判断Ref5菜单是否显示
            File[] files = SaveManage.getInstance().getFliesFromCurRef(Tools.SaveDir_CSVWAVE);
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightRef5List.get(rightRef5Index).getType()) // 判断是否为计数进度条节点
                    || (files.length == 0 && ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef5List.get(rightRef5Index).getType()))) { // 判断是否为CSV列表节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef5List.get(rightRef5Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_FINISH + CommandMsgToUI.PARAM_SPLIT + TChan.R5, 1); // 发送参考回调移动命令
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightRef5List.get(rightRef5Index).getType())) { // 判断是否为点击确认返回类型节点
                rightRef5List.get(LIST_HEAD).setCurListSelect(rightRef5Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightRef5List.get(rightRef5Index).getX() + rightRef5List.get(rightRef5Index).getW() / 2
                        , rightRef5List.get(rightRef5Index).getY() + rightRef5List.get(rightRef5Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightRef5List.get(rightRef5Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightRef5List.get(rightRef5Index).getX() + rightRef5List.get(rightRef5Index).getW() / 2
                            , rightRef5List.get(rightRef5Index).getY() + rightRef5List.get(rightRef5Index).getH() / 2);
                }
                if (rightRef5List.get(rightRef5Index).getChildNodes() != null
                        && rightRef5List.get(rightRef5Index).getChildNodes().size() != 0) {
                    rightRef5List = rightRef5List.get(rightRef5Index).getChildNodes(); // 进入子菜单节点
                    rightRef5Index = rightRef5List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightRef5List, rightRef5Index, true); // 更新焦点框位置
                } else {
                    rightRef5List.get(LIST_HEAD).setCurListSelect(rightRef5Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6)) { // 判断Ref6菜单是否显示
            File[] files = SaveManage.getInstance().getFliesFromCurRef(Tools.SaveDir_CSVWAVE);
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightRef6List.get(rightRef6Index).getType()) // 判断是否为计数进度条节点
                    || (files.length == 0 && ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef6List.get(rightRef6Index).getType()))) { // 判断是否为CSV列表节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef6List.get(rightRef6Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_FINISH + CommandMsgToUI.PARAM_SPLIT + TChan.R6, 1); // 发送参考回调移动命令
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightRef6List.get(rightRef6Index).getType())) { // 判断是否为点击确认返回类型节点
                rightRef6List.get(LIST_HEAD).setCurListSelect(rightRef6Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightRef6List.get(rightRef6Index).getX() + rightRef6List.get(rightRef6Index).getW() / 2
                        , rightRef6List.get(rightRef6Index).getY() + rightRef6List.get(rightRef6Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightRef6List.get(rightRef6Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightRef6List.get(rightRef6Index).getX() + rightRef6List.get(rightRef6Index).getW() / 2
                            , rightRef6List.get(rightRef6Index).getY() + rightRef6List.get(rightRef6Index).getH() / 2);
                }
                if (rightRef6List.get(rightRef6Index).getChildNodes() != null
                        && rightRef6List.get(rightRef6Index).getChildNodes().size() != 0) {
                    rightRef6List = rightRef6List.get(rightRef6Index).getChildNodes(); // 进入子菜单节点
                    rightRef6Index = rightRef6List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightRef6List, rightRef6Index, true); // 更新焦点框位置
                } else {
                    rightRef6List.get(LIST_HEAD).setCurListSelect(rightRef6Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7)) { // 判断Ref7菜单是否显示
            File[] files = SaveManage.getInstance().getFliesFromCurRef(Tools.SaveDir_CSVWAVE);
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightRef7List.get(rightRef7Index).getType()) // 判断是否为计数进度条节点
                    || (files.length == 0 && ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef7List.get(rightRef7Index).getType()))) { // 判断是否为CSV列表节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef7List.get(rightRef7Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_FINISH + CommandMsgToUI.PARAM_SPLIT + TChan.R7, 1); // 发送参考回调移动命令
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightRef7List.get(rightRef7Index).getType())) { // 判断是否为点击确认返回类型节点
                rightRef7List.get(LIST_HEAD).setCurListSelect(rightRef7Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightRef7List.get(rightRef7Index).getX() + rightRef7List.get(rightRef7Index).getW() / 2
                        , rightRef7List.get(rightRef7Index).getY() + rightRef7List.get(rightRef7Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightRef7List.get(rightRef7Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightRef7List.get(rightRef7Index).getX() + rightRef7List.get(rightRef7Index).getW() / 2
                            , rightRef7List.get(rightRef7Index).getY() + rightRef7List.get(rightRef7Index).getH() / 2);
                }
                if (rightRef7List.get(rightRef7Index).getChildNodes() != null
                        && rightRef7List.get(rightRef7Index).getChildNodes().size() != 0) {
                    rightRef7List = rightRef7List.get(rightRef7Index).getChildNodes(); // 进入子菜单节点
                    rightRef7Index = rightRef7List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightRef7List, rightRef7Index, true); // 更新焦点框位置
                } else {
                    rightRef7List.get(LIST_HEAD).setCurListSelect(rightRef7Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8)) { // 判断Ref8菜单是否显示
            File[] files = SaveManage.getInstance().getFliesFromCurRef(Tools.SaveDir_CSVWAVE);
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightRef8List.get(rightRef8Index).getType()) // 判断是否为计数进度条节点
                    || (files.length == 0 && ExternalKeysNode.TYPE_CSV_LIST.equals(rightRef8List.get(rightRef8Index).getType()))) { // 判断是否为CSV列表节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_REFRECALL_PROGRESS.equals(rightRef8List.get(rightRef8Index).getType())) { // 判断是否为参考回调进度条节点
                ExternalKeysCommand.get().moveRefRecall(ExternalKeysNode.ACTION_REFRECALL_FINISH + CommandMsgToUI.PARAM_SPLIT + TChan.R8, 1); // 发送参考回调移动命令
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightRef8List.get(rightRef8Index).getType())) { // 判断是否为点击确认返回类型节点
                rightRef8List.get(LIST_HEAD).setCurListSelect(rightRef8Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightRef8List.get(rightRef8Index).getX() + rightRef8List.get(rightRef8Index).getW() / 2
                        , rightRef8List.get(rightRef8Index).getY() + rightRef8List.get(rightRef8Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightRef8List.get(rightRef8Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightRef8List.get(rightRef8Index).getX() + rightRef8List.get(rightRef8Index).getW() / 2
                            , rightRef8List.get(rightRef8Index).getY() + rightRef8List.get(rightRef8Index).getH() / 2);
                }
                if (rightRef8List.get(rightRef8Index).getChildNodes() != null
                        && rightRef8List.get(rightRef8Index).getChildNodes().size() != 0) {
                    rightRef8List = rightRef8List.get(rightRef8Index).getChildNodes(); // 进入子菜单节点
                    rightRef8Index = rightRef8List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightRef8List, rightRef8Index, true); // 更新焦点框位置
                } else {
                    rightRef8List.get(LIST_HEAD).setCurListSelect(rightRef8Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1)) { // 判断S1串行总线菜单是否显示
            if (ExternalKeysNode.TYPE_RECEIVE_MSG.equals(rightS1List.get(rightS1Index).getType())) { // 判断是否为消息接收类型节点
                isReceiveMsgNode = true; // 标记为接收消息节点
            }

            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightS1List.get(rightS1Index).getType())) { // 判断是否为计数进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightS1List.get(rightS1Index).getType())) { // 判断是否为点击确认返回类型节点
                rightS1List.get(LIST_HEAD).setCurListSelect(rightS1Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightS1List.get(rightS1Index).getX() + rightS1List.get(rightS1Index).getW() / 2
                        , rightS1List.get(rightS1Index).getY() + rightS1List.get(rightS1Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightS1List.get(rightS1Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightS1List.get(rightS1Index).getX() + rightS1List.get(rightS1Index).getW() / 2
                            , rightS1List.get(rightS1Index).getY() + rightS1List.get(rightS1Index).getH() / 2);
                    rightS1List.get(LIST_HEAD).setCurListSelect(rightS1Index); // 设置列表选中项
                }
                if (rightS1List.get(rightS1Index).getChildNodes() != null
                        && rightS1List.get(rightS1Index).getChildNodes().size() != 0) {

                    rightS1List = rightS1List.get(rightS1Index).getChildNodes(); // 进入子菜单节点
                    rightS1Index = rightS1List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightS1List, rightS1Index, true); // 更新焦点框位置
                } else {
                    rightS1List.get(LIST_HEAD).setCurListSelect(rightS1Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2)) { // 判断S2串行总线菜单是否显示
            if (ExternalKeysNode.TYPE_RECEIVE_MSG.equals(rightS2List.get(rightS2Index).getType())) { // 判断是否为消息接收类型节点
                isReceiveMsgNode = true; // 标记为接收消息节点
            }

            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightS2List.get(rightS2Index).getType())) { // 判断是否为计数进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightS2List.get(rightS2Index).getType())) { // 判断是否为点击确认返回类型节点
                rightS2List.get(LIST_HEAD).setCurListSelect(rightS2Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightS2List.get(rightS2Index).getX() + rightS2List.get(rightS2Index).getW() / 2
                        , rightS2List.get(rightS2Index).getY() + rightS2List.get(rightS2Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightS2List.get(rightS2Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightS2List.get(rightS2Index).getX() + rightS2List.get(rightS2Index).getW() / 2
                            , rightS2List.get(rightS2Index).getY() + rightS2List.get(rightS2Index).getH() / 2);
                    rightS2List.get(LIST_HEAD).setCurListSelect(rightS2Index); // 设置列表选中项
                }
                if (rightS2List.get(rightS2Index).getChildNodes() != null
                        && rightS2List.get(rightS2Index).getChildNodes().size() != 0) {

                    rightS2List = rightS2List.get(rightS2Index).getChildNodes(); // 进入子菜单节点
                    rightS2Index = rightS2List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightS2List, rightS2Index, true); // 更新焦点框位置
                } else {
                    rightS2List.get(LIST_HEAD).setCurListSelect(rightS2Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3)) { // 判断S3串行总线菜单是否显示
            if (ExternalKeysNode.TYPE_RECEIVE_MSG.equals(rightS3List.get(rightS3Index).getType())) { // 判断是否为消息接收类型节点
                isReceiveMsgNode = true; // 标记为接收消息节点
            }
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightS3List.get(rightS3Index).getType())) { // 判断是否为计数进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightS3List.get(rightS3Index).getType())) { // 判断是否为点击确认返回类型节点
                rightS3List.get(LIST_HEAD).setCurListSelect(rightS3Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightS3List.get(rightS3Index).getX() + rightS3List.get(rightS3Index).getW() / 2
                        , rightS3List.get(rightS3Index).getY() + rightS3List.get(rightS3Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightS3List.get(rightS3Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightS3List.get(rightS3Index).getX() + rightS3List.get(rightS3Index).getW() / 2
                            , rightS3List.get(rightS3Index).getY() + rightS3List.get(rightS3Index).getH() / 2);
                    rightS3List.get(LIST_HEAD).setCurListSelect(rightS3Index); // 设置列表选中项
                }
                if (rightS3List.get(rightS3Index).getChildNodes() != null
                        && rightS3List.get(rightS3Index).getChildNodes().size() != 0) {

                    rightS3List = rightS3List.get(rightS3Index).getChildNodes(); // 进入子菜单节点
                    rightS3Index = rightS3List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightS3List, rightS3Index, true); // 更新焦点框位置
                } else {
                    rightS3List.get(LIST_HEAD).setCurListSelect(rightS3Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4)) { // 判断S4串行总线菜单是否显示
            if (ExternalKeysNode.TYPE_RECEIVE_MSG.equals(rightS4List.get(rightS4Index).getType())) { // 判断是否为消息接收类型节点
                isReceiveMsgNode = true; // 标记为接收消息节点
            }
            if (ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightS4List.get(rightS4Index).getType())) { // 判断是否为计数进度条节点
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            } else if (ExternalKeysNode.TYPE_CLICK_IS_SUREBACK.equals(rightS4List.get(rightS4Index).getType())) { // 判断是否为点击确认返回类型节点
                rightS4List.get(LIST_HEAD).setCurListSelect(rightS4Index); // 设置列表选中项
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        rightS4List.get(rightS4Index).getX() + rightS4List.get(rightS4Index).getW() / 2
                        , rightS4List.get(rightS4Index).getY() + rightS4List.get(rightS4Index).getH() / 2);
            } else {
                if (!ExternalKeysNode.TYPE_NO_CLICK.equals(rightS4List.get(rightS4Index).getType())) { // 判断是否为不可点击类型节点
                    ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                            rightS4List.get(rightS4Index).getX() + rightS4List.get(rightS4Index).getW() / 2
                            , rightS4List.get(rightS4Index).getY() + rightS4List.get(rightS4Index).getH() / 2);
                    rightS4List.get(LIST_HEAD).setCurListSelect(rightS4Index); // 设置列表选中项
                }
                if (rightS4List.get(rightS4Index).getChildNodes() != null
                        && rightS4List.get(rightS4Index).getChildNodes().size() != 0) {

                    rightS4List = rightS4List.get(rightS4Index).getChildNodes(); // 进入子菜单节点
                    rightS4Index = rightS4List.get(LIST_HEAD).getCurListSelect();
                    setViewPlace(rightS4List, rightS4Index, true); // 更新焦点框位置
                } else {
                    rightS4List.get(LIST_HEAD).setCurListSelect(rightS4Index); // 设置列表选中项
                }
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.BOTTOMSLIP)) { // 判断底部菜单是否显示
            if (!ExternalKeysNode.TYPE_NO_CLICK.equals(bottomList.get(bottomIndex).getType())) { // 判断是否为不可点击类型节点
                ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                        bottomList.get(bottomIndex).getX() + bottomList.get(bottomIndex).getW() / 2
                        , bottomList.get(bottomIndex).getY() + bottomList.get(bottomIndex).getH() / 2);
            }
            if (bottomList.get(bottomIndex).getChildNodes() != null
                    && bottomList.get(bottomIndex).getChildNodes().size() != 0) {
                bottomList = bottomList.get(bottomIndex).getChildNodes(); // 进入子菜单节点
                bottomIndex = bottomList.get(LIST_HEAD).getCurListSelect();
                setViewPlace(bottomList, bottomIndex, true); // 更新焦点框位置
            } else {
                bottomList.get(LIST_HEAD).setCurListSelect(bottomIndex); // 设置列表选中项
            }
        } else if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) { // 从缓存读取布尔值
            ExternalKeysUI.getInstance().onClick( // 模拟点击UI控件
                    serialsWordList.get(serialsWordIndex).getX() + serialsWordList.get(serialsWordIndex).getW() / 2
                    , serialsWordList.get(serialsWordIndex).getY() + serialsWordList.get(serialsWordIndex).getH() / 2);
        }
    }

    /**
     * 旋钮按压返回：返回上级菜单或关闭当前弹窗
     */
    public void moveBack() {
//        if (showViewIfGone()) {
//            return;
//        }
        if(topIndex >= topList.size()){ // 顶部菜单索引越界检查
            return; // 直接返回
        }
        if (isTopCountProgress() || isChxCountProgress() || isChxDelay() || isChxDelayUnit()) {
            moveBackNoClick(); // 返回上级菜单(不触发点击)
        } else {
            if (!closeDialog()) {
                moveBackNoClick(); // 返回上级菜单(不触发点击)
            }
        }

    }

    /**
     * 判断顶部菜单当前焦点是否为计数进度条类型
     * @return true表示当前焦点为计数进度条
     */
    private boolean isTopCountProgress() {
        return mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) && ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(topList.get(topIndex).getType()); // 判断顶部菜单是否显示
    }

    /**
     * 判断当前显示的右侧通道菜单焦点是否为带宽进度条类型
     * @return true表示当前焦点为带宽进度条
     */
    private boolean isChxCountProgress() {
        return (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) && ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh1List.get(rightCh1Index).getType())) // 判断CH1通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) && ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh2List.get(rightCh2Index).getType())) // 判断CH2通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) && ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh3List.get(rightCh3Index).getType())) // 判断CH3通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) && ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh4List.get(rightCh4Index).getType())) // 判断CH4通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) && ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh5List.get(rightCh5Index).getType())) // 判断CH5通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) && ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh6List.get(rightCh6Index).getType())) // 判断CH6通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) && ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh7List.get(rightCh7Index).getType())) // 判断CH7通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) && ExternalKeysNode.TYPE_TOPCOUNT_PROGRESS.equals(rightCh8List.get(rightCh8Index).getType())); // 判断CH8通道菜单是否显示
    }

    /**
     * 判断当前显示的右侧通道菜单焦点是否为延迟进度条类型
     * @return true表示当前焦点为延迟进度条
     */
    private boolean isChxDelay() {
        return (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh1List.get(rightCh1Index).getType())) // 判断CH1通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh2List.get(rightCh2Index).getType())) // 判断CH2通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh3List.get(rightCh3Index).getType())) // 判断CH3通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh4List.get(rightCh4Index).getType())) // 判断CH4通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh5List.get(rightCh5Index).getType())) // 判断CH5通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh6List.get(rightCh6Index).getType())) // 判断CH6通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh7List.get(rightCh7Index).getType())) // 判断CH7通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY.equals(rightCh8List.get(rightCh8Index).getType())); // 判断CH8通道菜单是否显示
    }

    /**
     * 判断当前显示的右侧通道菜单焦点是否为延迟单位选择类型
     * @return true表示当前焦点为延迟单位选择
     */
    private boolean isChxDelayUnit() {
        return (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh1List.get(rightCh1Index).getType())) // 判断CH1通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh2List.get(rightCh2Index).getType())) // 判断CH2通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh3List.get(rightCh3Index).getType())) // 判断CH3通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh4List.get(rightCh4Index).getType())) // 判断CH4通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh5List.get(rightCh5Index).getType())) // 判断CH5通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh6List.get(rightCh6Index).getType())) // 判断CH6通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh7List.get(rightCh7Index).getType())) // 判断CH7通道菜单是否显示
                || (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) && ExternalKeysNode.TYPE_RIGHT_SLIP_CH_DELAY_UNIT.equals(rightCh8List.get(rightCh8Index).getType())); // 判断CH8通道菜单是否显示
    }

    /**
     * @return 如果有dialog显示并成功关闭，则返回true
     */
    public boolean closeDialog() {

        if (ExternalKeysUI.getInstance().isDialogsShow()) {
            ExternalKeysUI.getInstance().onClick(1, 1); // 模拟点击UI控件
            return true; // 返回true
        }
        return false; // 返回false
    }

    /**
     * 返回上级菜单但不触发点击事件(用于进度条等不需要点击确认的场景)
     */
    public void moveBackNoClick() {
        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)) { // 判断顶部菜单是否显示
            if (topList.get(topIndex).getParentNodes() != null && topList.get(topIndex).getParentNodes().size() != 0) {
                int parentIndex = topList.get(topIndex).getParentNode().getIndex();
                topList = topList.get(topIndex).getParentNodes();
                topIndex = parentIndex;
                setViewPlace(topList, topIndex, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.TOPSLIP);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1)) { // 判断CH1通道菜单是否显示
            if (rightCh1List.get(rightCh1Index).getParentNodes() != null
                    && rightCh1List.get(rightCh1Index).getParentNodes().size() != 0) {
                int parentIndex = rightCh1List.get(rightCh1Index).getParentNode().getIndex();
                rightCh1List = rightCh1List.get(rightCh1Index).getParentNodes();
                rightCh1Index = parentIndex;
                setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_CH1);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2)) { // 判断CH2通道菜单是否显示
            if (rightCh2List.get(rightCh2Index).getParentNodes() != null
                    && rightCh2List.get(rightCh2Index).getParentNodes().size() != 0) {
                int parentIndex = rightCh2List.get(rightCh2Index).getParentNode().getIndex();
                rightCh2List = rightCh2List.get(rightCh2Index).getParentNodes();
                rightCh2Index = parentIndex;
                setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_CH2);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3)) { // 判断CH3通道菜单是否显示
            if (rightCh3List.get(rightCh3Index).getParentNodes() != null
                    && rightCh3List.get(rightCh3Index).getParentNodes().size() != 0) {
                int parentIndex = rightCh3List.get(rightCh3Index).getParentNode().getIndex();
                rightCh3List = rightCh3List.get(rightCh3Index).getParentNodes();
                rightCh3Index = parentIndex;
                setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_CH3);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4)) { // 判断CH4通道菜单是否显示
            if (rightCh4List.get(rightCh4Index).getParentNodes() != null
                    && rightCh4List.get(rightCh4Index).getParentNodes().size() != 0) {
                int parentIndex = rightCh4List.get(rightCh4Index).getParentNode().getIndex();
                rightCh4List = rightCh4List.get(rightCh4Index).getParentNodes();
                rightCh4Index = parentIndex;
                setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_CH4);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5)) { // 判断CH5通道菜单是否显示
            if (rightCh5List.get(rightCh5Index).getParentNodes() != null
                    && rightCh5List.get(rightCh5Index).getParentNodes().size() != 0) {
                int parentIndex = rightCh5List.get(rightCh5Index).getParentNode().getIndex();
                rightCh5List = rightCh5List.get(rightCh5Index).getParentNodes();
                rightCh5Index = parentIndex;
                setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_CH5);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6)) { // 判断CH6通道菜单是否显示
            if (rightCh6List.get(rightCh6Index).getParentNodes() != null
                    && rightCh6List.get(rightCh6Index).getParentNodes().size() != 0) {
                int parentIndex = rightCh6List.get(rightCh6Index).getParentNode().getIndex();
                rightCh6List = rightCh6List.get(rightCh6Index).getParentNodes();
                rightCh6Index = parentIndex;
                setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_CH6);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7)) { // 判断CH7通道菜单是否显示
            if (rightCh7List.get(rightCh7Index).getParentNodes() != null
                    && rightCh7List.get(rightCh7Index).getParentNodes().size() != 0) {
                int parentIndex = rightCh7List.get(rightCh7Index).getParentNode().getIndex();
                rightCh7List = rightCh7List.get(rightCh7Index).getParentNodes();
                rightCh7Index = parentIndex;
                setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_CH7);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8)) { // 判断CH8通道菜单是否显示
            if (rightCh8List.get(rightCh8Index).getParentNodes() != null
                    && rightCh8List.get(rightCh8Index).getParentNodes().size() != 0) {
                int parentIndex = rightCh8List.get(rightCh8Index).getParentNode().getIndex();
                rightCh8List = rightCh8List.get(rightCh8Index).getParentNodes();
                rightCh8Index = parentIndex;
                setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_CH8);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1)) { // 判断Math1菜单是否显示
            if (rightMath1List.get(rightMath1Index).getParentNodes() != null
                    && rightMath1List.get(rightMath1Index).getParentNodes().size() != 0) {
                int parentIndex = rightMath1List.get(rightMath1Index).getParentNode().getIndex();
                rightMath1List = rightMath1List.get(rightMath1Index).getParentNodes();
                rightMath1Index = parentIndex;
                setViewPlace(rightMath1List, rightMath1Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_MATH1);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2)) { // 判断Math2菜单是否显示
            if (rightMath2List.get(rightMath2Index).getParentNodes() != null
                    && rightMath2List.get(rightMath2Index).getParentNodes().size() != 0) {
                int parentIndex = rightMath2List.get(rightMath2Index).getParentNode().getIndex();
                rightMath2List = rightMath2List.get(rightMath2Index).getParentNodes();
                rightMath2Index = parentIndex;
                setViewPlace(rightMath2List, rightMath2Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_MATH2);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3)) { // 判断Math3菜单是否显示
            if (rightMath3List.get(rightMath3Index).getParentNodes() != null
                    && rightMath3List.get(rightMath3Index).getParentNodes().size() != 0) {
                int parentIndex = rightMath3List.get(rightMath3Index).getParentNode().getIndex();
                rightMath3List = rightMath3List.get(rightMath3Index).getParentNodes();
                rightMath3Index = parentIndex;
                setViewPlace(rightMath3List, rightMath3Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_MATH3);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4)) { // 判断Math4菜单是否显示
            if (rightMath4List.get(rightMath4Index).getParentNodes() != null
                    && rightMath4List.get(rightMath4Index).getParentNodes().size() != 0) {
                int parentIndex = rightMath4List.get(rightMath4Index).getParentNode().getIndex();
                rightMath4List = rightMath4List.get(rightMath4Index).getParentNodes();
                rightMath4Index = parentIndex;
                setViewPlace(rightMath4List, rightMath4Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_MATH4);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5)) { // 判断Math5菜单是否显示
            if (rightMath5List.get(rightMath5Index).getParentNodes() != null
                    && rightMath5List.get(rightMath5Index).getParentNodes().size() != 0) {
                int parentIndex = rightMath5List.get(rightMath5Index).getParentNode().getIndex();
                rightMath5List = rightMath5List.get(rightMath5Index).getParentNodes();
                rightMath5Index = parentIndex;
                setViewPlace(rightMath5List, rightMath5Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_MATH5);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6)) { // 判断Math6菜单是否显示
            if (rightMath6List.get(rightMath6Index).getParentNodes() != null
                    && rightMath6List.get(rightMath6Index).getParentNodes().size() != 0) {
                int parentIndex = rightMath6List.get(rightMath6Index).getParentNode().getIndex();
                rightMath6List = rightMath6List.get(rightMath6Index).getParentNodes();
                rightMath6Index = parentIndex;
                setViewPlace(rightMath6List, rightMath6Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_MATH6);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7)) { // 判断Math7菜单是否显示
            if (rightMath7List.get(rightMath7Index).getParentNodes() != null
                    && rightMath7List.get(rightMath7Index).getParentNodes().size() != 0) {
                int parentIndex = rightMath7List.get(rightMath7Index).getParentNode().getIndex();
                rightMath7List = rightMath7List.get(rightMath7Index).getParentNodes();
                rightMath7Index = parentIndex;
                setViewPlace(rightMath7List, rightMath7Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_MATH7);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8)) { // 判断Math8菜单是否显示
            if (rightMath8List.get(rightMath8Index).getParentNodes() != null
                    && rightMath8List.get(rightMath8Index).getParentNodes().size() != 0) {
                int parentIndex = rightMath8List.get(rightMath8Index).getParentNode().getIndex();
                rightMath8List = rightMath8List.get(rightMath8Index).getParentNodes();
                rightMath8Index = parentIndex;
                setViewPlace(rightMath8List, rightMath8Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_MATH8);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1)) { // 判断Ref1菜单是否显示
            if (rightRef1List.get(rightRef1Index).getParentNodes() != null
                    && rightRef1List.get(rightRef1Index).getParentNodes().size() != 0) {
                int parentIndex = rightRef1List.get(rightRef1Index).getParentNode().getIndex();
                rightRef1List = rightRef1List.get(rightRef1Index).getParentNodes();
                rightRef1Index = parentIndex;
                setViewPlace(rightRef1List, rightRef1Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_REF1);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2)) { // 判断Ref2菜单是否显示
            if (rightRef2List.get(rightRef2Index).getParentNodes() != null
                    && rightRef2List.get(rightRef2Index).getParentNodes().size() != 0) {
                int parentIndex = rightRef2List.get(rightRef2Index).getParentNode().getIndex();
                rightRef2List = rightRef2List.get(rightRef2Index).getParentNodes();
                rightRef2Index = parentIndex;
                setViewPlace(rightRef2List, rightRef2Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_REF2);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3)) { // 判断Ref3菜单是否显示
            if (rightRef3List.get(rightRef3Index).getParentNodes() != null
                    && rightRef3List.get(rightRef3Index).getParentNodes().size() != 0) {
                int parentIndex = rightRef3List.get(rightRef3Index).getParentNode().getIndex();
                rightRef3List = rightRef3List.get(rightRef3Index).getParentNodes();
                rightRef3Index = parentIndex;
                setViewPlace(rightRef3List, rightRef3Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_REF3);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4)) { // 判断Ref4菜单是否显示
            if (rightRef4List.get(rightRef4Index).getParentNodes() != null
                    && rightRef4List.get(rightRef4Index).getParentNodes().size() != 0) {
                int parentIndex = rightRef4List.get(rightRef4Index).getParentNode().getIndex();
                rightRef4List = rightRef4List.get(rightRef4Index).getParentNodes();
                rightRef4Index = parentIndex;
                setViewPlace(rightRef4List, rightRef4Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_REF4);
            }
        }
        else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5)) { // 判断Ref5菜单是否显示
            if (rightRef5List.get(rightRef5Index).getParentNodes() != null
                    && rightRef5List.get(rightRef5Index).getParentNodes().size() != 0) {
                int parentIndex = rightRef5List.get(rightRef5Index).getParentNode().getIndex();
                rightRef5List = rightRef5List.get(rightRef5Index).getParentNodes();
                rightRef5Index = parentIndex;
                setViewPlace(rightRef5List, rightRef5Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_REF5);
            }
        }
        else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6)) { // 判断Ref6菜单是否显示
            if (rightRef6List.get(rightRef6Index).getParentNodes() != null
                    && rightRef6List.get(rightRef6Index).getParentNodes().size() != 0) {
                int parentIndex = rightRef6List.get(rightRef6Index).getParentNode().getIndex();
                rightRef6List = rightRef6List.get(rightRef6Index).getParentNodes();
                rightRef6Index = parentIndex;
                setViewPlace(rightRef6List, rightRef6Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_REF6);
            }
        }
        else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7)) { // 判断Ref7菜单是否显示
            if (rightRef7List.get(rightRef7Index).getParentNodes() != null
                    && rightRef7List.get(rightRef7Index).getParentNodes().size() != 0) {
                int parentIndex = rightRef7List.get(rightRef7Index).getParentNode().getIndex();
                rightRef7List = rightRef7List.get(rightRef7Index).getParentNodes();
                rightRef7Index = parentIndex;
                setViewPlace(rightRef7List, rightRef7Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_REF7);
            }
        }
        else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8)) { // 判断Ref8菜单是否显示
            if (rightRef8List.get(rightRef8Index).getParentNodes() != null
                    && rightRef8List.get(rightRef8Index).getParentNodes().size() != 0) {
                int parentIndex = rightRef8List.get(rightRef8Index).getParentNode().getIndex();
                rightRef8List = rightRef8List.get(rightRef8Index).getParentNodes();
                rightRef8Index = parentIndex;
                setViewPlace(rightRef8List, rightRef8Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_REF8);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1)) { // 判断S1串行总线菜单是否显示
            if (rightS1List.get(rightS1Index).getParentNodes() != null
                    && rightS1List.get(rightS1Index).getParentNodes().size() != 0) {
                int parentIndex = rightS1List.get(rightS1Index).getParentNode().getIndex();
                rightS1List = rightS1List.get(rightS1Index).getParentNodes();
                rightS1Index = parentIndex;
                setViewPlace(rightS1List, rightS1Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_S1);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2)) { // 判断S2串行总线菜单是否显示
            if (rightS2List.get(rightS2Index).getParentNodes() != null
                    && rightS2List.get(rightS2Index).getParentNodes().size() != 0) {
                int parentIndex = rightS2List.get(rightS2Index).getParentNode().getIndex();
                rightS2List = rightS2List.get(rightS2Index).getParentNodes();
                rightS2Index = parentIndex;
                setViewPlace(rightS2List, rightS2Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_S2);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3)) { // 判断S3串行总线菜单是否显示
            if (rightS3List.get(rightS3Index).getParentNodes() != null
                    && rightS3List.get(rightS3Index).getParentNodes().size() != 0) {
                int parentIndex = rightS3List.get(rightS3Index).getParentNode().getIndex();
                rightS3List = rightS3List.get(rightS3Index).getParentNodes();
                rightS3Index = parentIndex;
                setViewPlace(rightS3List, rightS3Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_S3);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4)) { // 判断S4串行总线菜单是否显示
            if (rightS4List.get(rightS4Index).getParentNodes() != null
                    && rightS4List.get(rightS4Index).getParentNodes().size() != 0) {
                int parentIndex = rightS4List.get(rightS4Index).getParentNode().getIndex();
                rightS4List = rightS4List.get(rightS4Index).getParentNodes();
                rightS4Index = parentIndex;
                setViewPlace(rightS4List, rightS4Index, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.RIGHTSLIP_S4);
            }
        } else if (mainViewGroup.isSlipShow(MainViewGroup.BOTTOMSLIP)) { // 判断底部菜单是否显示
            if (bottomList.get(bottomIndex).getParentNodes() != null
                    && bottomList.get(bottomIndex).getParentNodes().size() != 0) {
                int parentIndex = bottomList.get(bottomIndex).getParentNode().getIndex();
                bottomList = bottomList.get(bottomIndex).getParentNodes();
                bottomIndex = parentIndex;
                setViewPlace(bottomList, bottomIndex, true); // 更新焦点框位置
            } else {
                mainViewGroup.hideSlip(MainViewGroup.BOTTOMSLIP);
            }
        }else if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CENTER_SEGMENTED_VISIBLE)) { // 从缓存读取布尔值
            if (segmentedList.get(segmentedIndex).getParentNodes() != null
                    && segmentedList.get(segmentedIndex).getParentNodes().size() != 0) {
                int parentIndex = segmentedList.get(segmentedIndex).getParentNode().getIndex();
                segmentedList = segmentedList.get(segmentedIndex).getParentNodes();
                segmentedIndex = parentIndex;
                setViewPlace(segmentedList, segmentedIndex, true); // 更新焦点框位置
            } else {
                if (focusView != null) {
                    setFocusViewVisible(View.GONE); // 设置焦点框可见性
                }
            }
        }else if (mainViewGroup.getChannelsLayout().getVisibility()==View.VISIBLE){
            if (channelsList.get(channelsIndex).getParentNodes() != null
                    && channelsList.get(channelsIndex).getParentNodes().size() != 0) {
                int parentIndex = channelsList.get(channelsIndex).getParentNode().getIndex();
                channelsList = channelsList.get(channelsIndex).getParentNodes();
                channelsIndex = parentIndex;
                setViewPlace(channelsList, channelsIndex, true); // 更新焦点框位置
            } else {
                if (focusView != null) {
                    setFocusViewVisible(View.GONE); // 设置焦点框可见性
                }
            }
        }
    }

    /**
     * 根据列表类型显示对应区域的焦点位置
     * @param listType 列表类型标识(@ListType)
     */
    public void showViewPlace(@ListType String listType) {
        switch (listType) { // switch分支选择
            case LISTTYPE_TOP:
                setViewPlace(topList, topIndex, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_CH1:
                setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_CH2:
                setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_CH3:
                setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_CH4:
                setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_CH5:
                setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_CH6:
                setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_CH7:
                setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_CH8:
                setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_MATH1:
                setViewPlace(rightMath1List, rightMath1Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_MATH2:
                setViewPlace(rightMath2List, rightMath2Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_MATH3:
                setViewPlace(rightMath3List, rightMath3Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_MATH4:
                setViewPlace(rightMath4List, rightMath4Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_MATH5:
                setViewPlace(rightMath5List, rightMath5Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_MATH6:
                setViewPlace(rightMath6List, rightMath6Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_MATH7:
                setViewPlace(rightMath7List, rightMath7Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_MATH8:
                setViewPlace(rightMath8List, rightMath8Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_REF1:
                setViewPlace(rightRef1List, rightRef1Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_REF2:
                setViewPlace(rightRef2List, rightRef2Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_REF3:
                setViewPlace(rightRef3List, rightRef3Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_REF4:
                setViewPlace(rightRef4List, rightRef4Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_REF5:
                setViewPlace(rightRef5List, rightRef5Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_REF6:
                setViewPlace(rightRef6List, rightRef6Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_REF7:
                setViewPlace(rightRef7List, rightRef7Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_REF8:
                setViewPlace(rightRef8List, rightRef8Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_S1:
                setViewPlace(rightS1List, rightS1Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_S2:
                setViewPlace(rightS2List, rightS2Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_S3:
                setViewPlace(rightS3List, rightS3Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_RIGHT_S4:
                setViewPlace(rightS4List, rightS4Index, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_BOTTOM:
                setViewPlace(bottomList, bottomIndex, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_CHANNELS:
                setViewPlace(channelsList, channelsIndex, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_OKCANCEL:
                setViewPlace(okCancelList, okCancelIndex, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_OK:
                setViewPlace(okList, okIndex, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_SERIALS_WORD:
                setViewPlace(serialsWordList, serialsWordIndex, true); // 更新焦点框位置
                break; // 跳出循环
            case LISTTYPE_AUTO_MOTIVE:
//                setViewPlace(autoMotiveList, autoMotiveIndex, true);
                break; // 跳出循环
            case LISTTYPE_SEGMENTED:
                setViewPlace(segmentedList, segmentedIndex, true); // 更新焦点框位置
                break; // 跳出循环
        }
    }

    /**
     * getSegmentedList方法
     */
    public List<ExternalKeysNode> getSegmentedList() {
        return segmentedList;
    }

    /**
     * getRightSerialsList方法
     */
    public List<ExternalKeysNode> getRightSerialsList(int serialsNumber) {
        switch (serialsNumber) { // switch分支选择
            case CacheUtil.S2:
                return rightS2List;
            case CacheUtil.S3:
                return rightS3List;
            case CacheUtil.S4:
                return rightS4List;
            case CacheUtil.S1:
            default: // 默认分支
                return rightS1List;
        }
    }

    /**
     * 根据串行总线编号获取对应的右侧列表索引
     * @param serialsNumber 串行总线编号(S1~S4)
     * @return 对应的串行总线列表索引(0~3)
     */
    public int getRightSerialsListIndex(int serialsNumber) {
        switch (serialsNumber) { // switch分支选择
            case CacheUtil.S2:
                return rightS2Index;
            case CacheUtil.S3:
                return rightS3Index;
            case CacheUtil.S4:
                return rightS4Index;
            case CacheUtil.S1:
            default: // 默认分支
                return rightS1Index;
        }
    }
    //endregion

    //region private

    //region 变化监听
    /**
     * 初始化控件观察者，注册RxBus消息监听
     * 监听各种UI事件(弹窗显示/隐藏、通道参数变更、触发模式变更等)并更新节点状态
     */
    private void initControl() {
        //slip弹出
        RxBus.getInstance().getObservable(RxEnum.MAIN_SLIP_TO_OTHER).subscribe(consumerMainSlip); // 订阅侧滑菜单切换事件
        //各级菜单变化
        RxBus.getInstance().getObservable(RxEnum.TOPSLIP_TITLE).subscribe(consumerTopSlipTitle); // 订阅顶部菜单标题变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPSLIP_SAVE_SEGMENT).subscribe(consumerTopSlipSegment); // 订阅存储分段变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_MEASURE).subscribe(consumerTopLayoutMeasure); // 订阅测量布局变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_MEASURE_SETTING).subscribe(consumerTopLayoutMeasureSetting); // 订阅测量设置布局变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_MEASURE_STATICS).subscribe(consumerTopLayoutMeasureStatics); // 订阅测量统计布局变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAVE).subscribe(consumerTopLayoutSave); // 订阅保存布局变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLE).subscribe(consumerTopLayoutSample); // 订阅采样布局变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLEMODE).subscribe(consumerTopLayoutSampleMode); // 订阅采样模式变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLEDEPTH).subscribe(consumerTopLayoutSampleDepth); // 订阅存储深度变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED).subscribe(consumerTopLayoutSampleSegmented); // 订阅分段采样变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPSLIP_SAVE_SEGMENT).subscribe(consumerTopLayoutSaveSegments); // 订阅保存分段变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_DISPLAY).subscribe(consumerTopLayoutDisplay); // 订阅显示布局变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_TRIGGER).subscribe(consumerTopLayoutTrigger); // 订阅触发布局变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_AUTO).subscribe(consumerTopLayoutAuto); // 订阅自动布局变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_USERSET).subscribe(consumerTopLayoutUserSet); // 订阅用户设置布局变更事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS).subscribe(consumerRightLayoutSerial); // 订阅串行总线布局变更事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_CHANNEL).subscribe(consumerRightLayoutChannel); // 订阅通道布局变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLEMODE).subscribe(consumerTopLayoutSampleMode); // 订阅采样模式变更事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLEDEPTH).subscribe(consumerTopLayoutSampleDepth); // 订阅存储深度变更事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_MATH).subscribe(consumerRightLayoutMath); // 订阅数学运算布局变更事件
        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_QUICKENABLE).subscribe(consumerMainBottomQuickEnable); // 订阅底部快捷启用事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED).subscribe(consumerTopLayoutSampleSegmented); // 订阅分段采样变更事件
        //channel变化
        RxBus.getInstance().dealObservable(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,this::OnChanActiveChange); // 订阅通道激活状态变更事件
        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_CHANNEL_MOVE).subscribe(consumerMainCenterChannelMove); // 订阅中心通道移动事件
        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_CHANNEL_VISIBLE_LAYOUTTOBTN).subscribe(consumerChannelVisible); // 订阅通道可见性变更事件
        //centerSegmented变化...
        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_SEGMENTED_VISIBLE).subscribe(consumerMainCenterSegmentedVisible); // 订阅分段存储可见性事件
        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_SEGMENTED).subscribe(consumerMainCenterSegmentedSelect); // 订阅分段存储选择事件
        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_SEGMENTED_MOVE).subscribe(consumerMainCenterSegmentedMove); // 订阅分段存储移动事件

        //centerMenu变化
        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_MENU_MOVE).subscribe(consumerMainCenterMenuMove); // 订阅中心菜单移动事件
        //各项选择变化
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_CHANNELS).subscribe(consumerChannelChange); // 订阅右侧通道变更事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerChannelChange); // 订阅右侧其他通道变更事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_REF).subscribe(consumerChannelChange); // 订阅参考波形布局变更事件
        RxBus.getInstance().getObservable(RxEnum.CONTROLS_VISIBLE_CHANGED).subscribe(consumerVisibleChange); // 订阅控件可见性变更事件
        RxBus.getInstance().getObservable(RxEnum.KEYBOARD_FORMULA_ENABLE).subscribe(consumerKeyboardFormulaEnable); // 订阅公式键盘启用事件
        RxBus.getInstance().getObservable(RxEnum.CENTER_SERIALSWORD_VISIBLE).subscribe(consumerSerialWordVisible); // 订阅串行数据字可见性事件

        RxBus.getInstance().getObservable(RxEnum.DIALOG_OPEN).subscribe(consumerDialogOpen); // 订阅弹窗打开事件
        RxBus.getInstance().getObservable(RxEnum.DIALOG_CLOSE).subscribe(consumerDialogClose); // 订阅弹窗关闭事件

        //Math/Ref/Serials页面状态变化 （是单独状态 还是 共同设置 页面）
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SHOW_NORMAL_STATE).subscribe(consumerShowNormalState); // 订阅正常状态显示事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_SERIALS).subscribe(consumerSerialsCanAdd); // 订阅串行总线可添加状态事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_REF).subscribe(consumerRefCanAdd); // 订阅参考波形可添加状态事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_MATH).subscribe(consumerMathCanAdd); // 订阅数学运算可添加状态事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_UPDATE_CSV_ITEM_POS).subscribe(updateSCsvSelectViewPlace); // 订阅CSV项位置更新事件

        //波形区域变化时，Math/Ref/Bus 对应旋钮位置的偏移量变化
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_UPDATE_SELECT_RECTANGLE).subscribe(consumerMathRefBusOffset); // 订阅选择区域偏移更新事件

        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_UPDATE_SERIALS_DETAIL_VISIBLE).subscribe(consumerUpdateSerialsVisible); // 订阅串行详情可见性更新事件

        EventFactory.addEventObserver(EventFactory.EVENT_AUTO_STOP, eventUIObserver); // 注册事件观察者


        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SYNC_EXTERNAL_TRIGGER_STATE).subscribe(consumerSyncExternalTriggerState); // 订阅外部触发状态同步事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SYNC_EXTERNAL_AUTOSAVE_STATE).subscribe(consumerSyncExternalAutoSaveState); // 订阅自动保存状态同步事件
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SAVE_OR_INVOKE_SUFFIX_NUM_UPDATE).subscribe(consumerSaveNameSuffixState); // 订阅保存/调用后缀号更新事件
    }




    private EventUIObserver eventUIObserver = new EventUIObserver() { // 事件UI观察者
        @Override
        /**
         * update方法
         */
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_AUTO_STOP) {
                topList = getTopBaseList(); // 获取顶部菜单基础列表
                topIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP); // 从缓存读取顶部菜单选中索引
                topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                int topIndex2=mainViewGroup.getMainMenu().getSecondMenuIdx();
                if(topIndex < topList.size()) {
                    topList = topList.get(topIndex).getChildNodes(); // 进入子菜单节点
                    topList.get(LIST_HEAD).setCurListSelect(topIndex2); // 设置列表选中项
                    topIndex = topIndex2;
                }
            }
        }
    };
    private Consumer<MainMsgSlip> consumerMainSlip = new Consumer<MainMsgSlip>() { // RxBus消息消费者
        @Override
        public void accept(MainMsgSlip mainMsgSlip) throws Exception {
            if (mainMsgSlip != null && mainMsgSlip.isOpen()
                    && mainMsgSlip.getSlip() == MainViewGroup.TOPSLIP) {
                setDelayChannelList(); // 更新延迟通道列表
                //setChannelList();
            }
            if (isFirst && mainMsgSlip != null && mainMsgSlip.isOpen()
                    && mainMsgSlip.getSlip() == MainViewGroup.TOPSLIP) {
                isFirst = false; // 取消首次初始化标记
                topList = getTopBaseList(); // 获取顶部菜单基础列表
                topIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP); // 从缓存读取顶部菜单选中索引
                if(topIndex > 7){ // 顶部菜单索引越界检查
                    topIndex = 0; // 初始化索引为0
                }
                topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
            }
            if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT) // 从缓存读取布尔值
                    && mainMsgSlip.isOpen() && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                if (mainMsgSlip.getSlip() == MainViewGroup.TOPSLIP) {
                    setViewPlace(topList, topIndex, true); // 更新焦点框位置
                } else if (mainMsgSlip.getSlip() == MainViewGroup.RIGHTSLIP_S1) {
                    setViewPlace(rightS1List, rightS1Index, true); // 更新焦点框位置
                } else if (mainMsgSlip.getSlip() == MainViewGroup.RIGHTSLIP_S2) {
                    setViewPlace(rightS2List, rightS2Index, true); // 更新焦点框位置
                } else if (mainMsgSlip.getSlip() == MainViewGroup.RIGHTSLIP_S3) {
                    setViewPlace(rightS3List, rightS3Index, true); // 更新焦点框位置
                } else if (mainMsgSlip.getSlip() == MainViewGroup.RIGHTSLIP_S4) {
                    setViewPlace(rightS4List, rightS4Index, true); // 更新焦点框位置
                }
            }
        }
    };

    //用户点击之后的节点应该是在点击反应的子列表层
    private Consumer<TopMsgPopWindow> consumerTopSlipTitle = new Consumer<TopMsgPopWindow>() { // RxBus消息消费者
        @Override
        public void accept(@NonNull TopMsgPopWindow msgPopWindow) throws Exception {
            List<ExternalKeysNode> topBaseList = getTopBaseList();
            boolean ytMode = msgPopWindow.getYtMode().isRxMsgSelect();
            boolean serialWord = msgPopWindow.getSerialWord().isRxMsgSelect();
            for (int i = 0; i < topBaseList.size(); i++) { // 遍历节点列表
                if (ytMode) {
                    if (i != TopLayoutPopWindow.DETAIL_DISPLAY) {
                        topBaseList.get(i).setVisible(msgPopWindow.getYtMode().isValue());//xy模式下，只有display选项管用
                    }
                }
                if (serialWord) {
                    if(i == TopLayoutPopWindow.DETAIL_TRIGGER) {
                        List<ExternalKeysNode> triggerList = topBaseList.get(i).getChildNodes();
                        for (int j = 0; j < triggerList.size(); j++) {//serial word模式下，trigger选项下，只有s1、s2管用
                            if (j != TopLayoutTrigger.DETAIL_S1
                                    && j != TopLayoutTrigger.DETAIL_S2
                                    && j != TopLayoutTrigger.DETAIL_S3
                                    && j != TopLayoutTrigger.DETAIL_S4) {
                                triggerList.get(j).setVisible(!msgPopWindow.getSerialWord().isValue());
                            }
                        }
                    } else if(i == TopLayoutPopWindow.DETAIL_DISPLAY) {
                        List<ExternalKeysNode> disPlayList = topBaseList.get(i).getChildNodes();
                        for (int j = 0; j < disPlayList.size(); j++) {//serial word模式下，display选项下，只有Txt Mix管用
                            if (j != TopLayoutDisplay.DETAIL_TXT_MIX) {
                                disPlayList.get(j).setVisible(!msgPopWindow.getSerialWord().isValue());
                            }
                        }
                    } else {
                        topBaseList.get(i).setVisible(!msgPopWindow.getSerialWord().isValue());//serial word模式下，只有trigger/display选项管用
                    }
                }
            }

            if (!isReceiveMsgNode && msgPopWindow.getYtMode().isValue()) {//只有当yt模式，该设置才生�?
                topList = getTopBaseList(); // 获取顶部菜单基础列表
                if (msgPopWindow.getCheckIndex()<topList.size()) {
                    topIndex = msgPopWindow.getCheckIndex();
                    topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                    topList = topList.get(topIndex).getChildNodes(); // 进入子菜单节点
                    topIndex = topList.get(LIST_HEAD).getCurListSelect();
                }
            }
            if (!isReceiveMsgNode && msgPopWindow.getSerialWord().isValue()) {
                topList = getTopBaseList(); // 获取顶部菜单基础列表
                topIndex = msgPopWindow.getCheckIndex();
                topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                topList = topList.get(topIndex).getChildNodes(); // 进入子菜单节点
                if (topList.get(LIST_HEAD).getCurListSelect() != TopLayoutTrigger.DETAIL_S1
                        && topList.get(LIST_HEAD).getCurListSelect() != TopLayoutTrigger.DETAIL_S2) {
                    topList.get(LIST_HEAD).setCurListSelect(TopLayoutTrigger.DETAIL_S1); // 设置列表选中项
                }
                topIndex = topList.get(LIST_HEAD).getCurListSelect();
            }

            if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                setViewPlace(topList, topIndex, true); // 更新焦点框位置
            } else {
                topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
            }
            isReceiveMsgNode = false; // 取消消息接收标记
        }
    };

    private Consumer<TopMsgSaveSegments> consumerTopSlipSegment = new Consumer<TopMsgSaveSegments>() { // RxBus消息消费者
        @Override
        public void accept(TopMsgSaveSegments msgSaveSegments) throws Exception {
            List<ExternalKeysNode> saveBinList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_SAVE).getChildNodes()
                    .get(TopLayoutSave.DETAIL_STORE).getChildNodes()
                    .get(TopLayoutSaveStore.DETAIL_BIN).getChildNodes();
            int index = Tools.indexOf(saveBinList, s -> s.getName().equalsIgnoreCase("All Segments"));
            if (index != -1) {//找到 所有段 对应的旋钮index
                saveBinList.get(index).setVisible(msgSaveSegments.isVisibleSegments());
            }

            int saveIndex = saveBinList.get(LIST_HEAD).getCurListSelect();
            while (!saveBinList.get(saveIndex).isVisible()) {
                saveIndex = saveIndex != 0 ? saveIndex - 1 : saveBinList.size() - 1;
            }
            if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                    && Objects.equals(saveBinList.get(LIST_HEAD).getName(), topList.get(LIST_HEAD).getName())
                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                setViewPlace(saveBinList, saveIndex, true); // 更新焦点框位置
            } else {
                saveBinList.get(LIST_HEAD).setCurListSelect(saveIndex); // 设置列表选中项
            }
        }
    };

    private Consumer<TopMsgMeasure> consumerTopLayoutMeasure = new Consumer<TopMsgMeasure>() { // RxBus消息消费者
        @Override
        public void accept(TopMsgMeasure topMsgMeasure) throws Exception {
            if (topMsgMeasure.getMeasureTitle().isRxMsgSelect()) {
                if (!isReceiveMsgNode) {
                    List<ExternalKeysNode> measureList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_MEASURE).getChildNodes();
                    int autoIndex = topMsgMeasure.getMeasureTitle().getIndex();
                    measureList.get(LIST_HEAD).setCurListSelect(autoIndex); // 设置列表选中项
                    if (!topMsgMeasure.isFromEventBus()) {
                        topList = measureList.get(autoIndex).getChildNodes(); // 进入子菜单节点
                        topIndex = topList.get(LIST_HEAD).getCurListSelect();
                    }
                }
            }

            if (topMsgMeasure.getMeasureDetail() instanceof TopMsgFrequencyMeter) {
                boolean enableFreqCounter = isEnableFreqCounter();
                List<ExternalKeysNode> topBaseList = getTopBaseList();
                ExternalKeysNode freqCounterNode = topBaseList.get(TopLayoutPopWindow.DETAIL_MEASURE).getChildNodes()
                        .get(TopLayoutMeasure.DETAIL_FREQUENCY);
                if (enableFreqCounter) {
                    freqCounterNode.setChildNodes(ExternalKeysNodeUtil.getTopMeasureCounterNodeList(freqCounterNode, freqCounterNode.getParentNode().getChildNodes(), topSlipOffset)); // 设置子节点列表
                } else {
                    freqCounterNode.setChildNodes(null); // 设置子节点列表
                }
            }
            isReceiveMsgNode = false; // 取消消息接收标记
        }

        /**
         * isEnableFreqCounter方法
         */
        private boolean isEnableFreqCounter() {
            return ScopeConfig.getConfig().isEnableFreqCounter() || App.IsDebug();
        }
    };

    private Consumer<TopMsgMeasureSetting> consumerTopLayoutMeasureSetting=new Consumer<TopMsgMeasureSetting>() { // RxBus消息消费者
        @Override
        public void accept(TopMsgMeasureSetting topMsgMeasureSetting) throws Exception {
           List<ExternalKeysNode> settingList=  getTopBaseList().get(TopLayoutPopWindow.DETAIL_MEASURE).getChildNodes().get(TopLayoutMeasure.DETAIL_SETTING).getChildNodes();
           int typeIdx= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_SETTING_THRESHOLDS); // 从缓存读取整数值
           if (typeIdx==0) {
               for(int i=2;i<=4;i++){
                   settingList.get(settingList.size() - i).getChildNodes().get(3).setVisible(false); // 设置不可见
                   settingList.get(settingList.size() - i).getChildNodes().get(4).setVisible(false); // 设置不可见
                   settingList.get(settingList.size() - i).getChildNodes().get(8).setVisible(false); // 设置不可见
                   settingList.get(settingList.size() - i).getChildNodes().get(9).setVisible(false); // 设置不可见
                   settingList.get(settingList.size() - i).getChildNodes().get(13).setVisible(false); // 设置不可见
                   settingList.get(settingList.size() - i).getChildNodes().get(17).setVisible(false); // 设置不可见
                   settingList.get(settingList.size() - i).getChildNodes().get(18).setVisible(false); // 设置不可见
               }

           }else {
               for(int i=2;i<=4;i++) {
                   settingList.get(settingList.size() - i).getChildNodes().get(3).setVisible(true); // 设置可见
                   settingList.get(settingList.size() - i).getChildNodes().get(4).setVisible(true); // 设置可见
                   settingList.get(settingList.size() - i).getChildNodes().get(8).setVisible(true); // 设置可见
                   settingList.get(settingList.size() - i).getChildNodes().get(9).setVisible(true); // 设置可见
                   settingList.get(settingList.size() - i).getChildNodes().get(13).setVisible(true); // 设置可见
                   settingList.get(settingList.size() - i).getChildNodes().get(17).setVisible(true); // 设置可见
                   settingList.get(settingList.size() - i).getChildNodes().get(18).setVisible(true); // 设置可见
               }
           }
        }
    };
    private Consumer<TopMsgMeasureStatics> consumerTopLayoutMeasureStatics=new Consumer<TopMsgMeasureStatics>() { // RxBus消息消费者
        @Override
        public void accept(TopMsgMeasureStatics topMsgMeasureStatics) throws Exception {

            List<ExternalKeysNode> staticsList=  getTopBaseList().get(TopLayoutPopWindow.DETAIL_MEASURE).getChildNodes().get(TopLayoutMeasure.DETAIL_STATICS).getChildNodes();
            boolean all= CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_MEASURE_STATIC_ALL); // 从缓存读取布尔值
            staticsList.get(2).setVisible(all);
            staticsList.get(3).setVisible(all);
            staticsList.get(4).setVisible(all);
            staticsList.get(5).setVisible(all);
            staticsList.get(6).setVisible(all);

        }
    };

    private Consumer<TopMsgSaveStore> consumerTopLayoutSave = new Consumer<TopMsgSaveStore>() { // RxBus消息消费者
        @Override
        public void accept(TopMsgSaveStore topMsgSave) throws Exception {
//            List<ExternalKeysNode> topBaseList = getTopBaseList();
//            if (topBaseList.isEmpty()) return;
//            if (topMsgSave.getSaveTitle().isRxMsgSelect()) {
//                if (!isReceiveMsgNode) {
//                    List<ExternalKeysNode> saveList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_SAVE).getChildNodes()
//                            .get(TopLayoutSave.DETAIL_STORE).getChildNodes();
//                    int autoIndex = topMsgSave.getSaveTitle().getIndex();
//                    saveList.get(LIST_HEAD).setCurListSelect(autoIndex);
//                    if (!topMsgSave.isFromEventBus()) {
//                        topList = saveList.get(autoIndex).getChildNodes();
//                        topIndex = topList.get(LIST_HEAD).getCurListSelect();
//                    }
//                }
//            }
//            if (topMsgSave.getSaveDetail() instanceof TopMsgSaveWave) {
//                TopMsgSaveWave msgSaveWave = (TopMsgSaveWave) topMsgSave.getSaveDetail();
//                boolean[] saveTypeEnable = msgSaveWave.getSaveTypeEnable();
//                List<ExternalKeysNode> saveWaveList = getTopBaseList()
//                        .get(TopLayoutPopWindow.DETAIL_SAVE).getChildNodes()
//                        .get(TopLayoutSaveStore.DETAIL_WAV).getChildNodes();
//                saveWaveList.get(27).setVisible(saveTypeEnable[0]);
//                saveWaveList.get(28).setVisible(saveTypeEnable[1]);
//                saveWaveList.get(29).setVisible(saveTypeEnable[2]);
//                if (Objects.equals(topList.get(LIST_HEAD).getName(), saveWaveList.get(LIST_HEAD).getName())) {
//                    while (!topList.get(topIndex).isVisible()) {
//                        topIndex++;
//                    }
//                    if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)
//                            && focusView != null && focusView.getVisibility() == View.VISIBLE) {
//                        setViewPlace(topList, topIndex, true);
//                    } else {
//                        topList.get(LIST_HEAD).setCurListSelect(topIndex);
//                    }
//                }
//            }
            isReceiveMsgNode = false; // 取消消息接收标记
        }
    };

    private Consumer<TopMsgSample> consumerTopLayoutSample = new Consumer<TopMsgSample>() { // RxBus消息消费者
        @Override
        public void accept(TopMsgSample topMsgSample) throws Exception {
            if (topMsgSample.getSampleTitle().isRxMsgSelect()) {
                if (!isReceiveMsgNode) {
                    List<ExternalKeysNode> sampleList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_SAMPLE).getChildNodes();
                    int autoIndex = topMsgSample.getSampleTitle().getIndex();
                    sampleList.get(LIST_HEAD).setCurListSelect(autoIndex); // 设置列表选中项
                    if (!topMsgSample.isFromEventBus()) {
                        topList = sampleList.get(autoIndex).getChildNodes(); // 进入子菜单节点
                        topIndex = topList.get(LIST_HEAD).getCurListSelect();
                    }
                }
            }
            isReceiveMsgNode = false; // 取消消息接收标记
        }
    };

    private Consumer<TopMsgDisplay> consumerTopLayoutDisplay = new Consumer<TopMsgDisplay>() { // RxBus消息消费者
        @Override
        public void accept(@NonNull TopMsgDisplay topMsgDisplay) throws Exception {
            if (!isReceiveMsgNode) {
                if (topMsgDisplay.getDisplayTitle().isRxMsgSelect()) {
                    List<ExternalKeysNode> displayList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_DISPLAY).getChildNodes();
                    int displayIndex = topMsgDisplay.getDisplayTitle().getIndex();
                    displayList.get(LIST_HEAD).setCurListSelect(displayIndex); // 设置列表选中项
                    if (!topMsgDisplay.isFromEventBus()) {
                        topList = displayList.get(displayIndex).getChildNodes(); // 进入子菜单节点
                        if (topList != null) {
                            topIndex = topList.get(LIST_HEAD).getCurListSelect();
                        }
                    }
                }
            }
            isReceiveMsgNode = false; // 取消消息接收标记
            if (topMsgDisplay.getDisplayDetail() instanceof TopMsgDisplayPersist) {
                TopMsgDisplayPersist msgDisplayPersist = (TopMsgDisplayPersist) topMsgDisplay.getDisplayDetail();
                List<ExternalKeysNode> persistList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_DISPLAY).getChildNodes()
                        .get(TopLayoutDisplay.DETAIL_PERSIST).getChildNodes();
                //persist->adjust is visible or not

                for (int i = 0; i < persistList.size(); i++) { // 遍历节点列表
                    if (persistList.get(i).getName().equals("adjust")) {
                        persistList.get(i).setVisible(msgDisplayPersist.getPersist().getIndex() == 2);
                    }
                    if (persistList.get(i).getName().equals("fftAdjust")) {
                        persistList.get(i).setVisible(msgDisplayPersist.getFftPersist().getIndex() == 2);
                    }
                }
            }
        }
    };

    private Consumer<TopMsgTrigger> consumerTopLayoutTrigger = new Consumer<TopMsgTrigger>() { // RxBus消息消费者
        @Override
        public void accept(@NonNull TopMsgTrigger topMsgTrigger) throws Exception {
            if (!isReceiveMsgNode) {
                if (topMsgTrigger.getTriggerTitle().isRxMsgSelect()) {//当前为点击title发�?�的消息
                    List<ExternalKeysNode> triggerList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes();
                    int triggerIndex = topMsgTrigger.getTriggerTitle().getIndex();
                    triggerList.get(LIST_HEAD).setCurListSelect(triggerIndex); // 设置列表选中项
//                    if (!topMsgTrigger.isFromEventBus()) {
                    topList = triggerList.get(triggerIndex).getChildNodes(); // 进入子菜单节点
                    topIndex = topList.get(LIST_HEAD).getCurListSelect();
//                    }
                } else {
                    if (topMsgTrigger.getTriggerDetail() instanceof TopMsgTriggerSerials) {
                        TopMsgTriggerSerials msgTriggerSerials = (TopMsgTriggerSerials) topMsgTrigger.getTriggerDetail();
                        if (msgTriggerSerials.getSerials().isRxMsgSelect()) {//当前为TopTriggerSerials的子项�?�择
                            List<ExternalKeysNode> serialsList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes()
                                    .get(topMsgTrigger.getTriggerTitle().getIndex()).getChildNodes();
                            int serialsIndex = msgTriggerSerials.getSerials().getId();
                            serialsList.get(LIST_HEAD).setCurListSelect(serialsIndex); // 设置列表选中项
                            if (!topMsgTrigger.isFromEventBus()) {
                                if (serialsList.get(serialsIndex).getChildNodes() != null) {
                                    topList = serialsList.get(serialsIndex).getChildNodes(); // 进入子菜单节点
                                    topIndex = topList.get(LIST_HEAD).getCurListSelect();
                                }
                            }
                        }
                    }
                    if (topMsgTrigger.getTriggerDetail() instanceof TopMsgTriggerPulsewidth) {
                        TopMsgTriggerPulsewidth msgTriggerPulseWidth = (TopMsgTriggerPulsewidth) topMsgTrigger.getTriggerDetail();
                        List<ExternalKeysNode> pulseWidthList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes()
                                .get(TopLayoutTrigger.DETAIL_PULSEWIDTH).getChildNodes();
                        int listSize = pulseWidthList.size();
                        switch (msgTriggerPulseWidth.getCondition().getIndex()) { // switch分支选择
                            case TriggerPulseWidth.PW_RELATION_LESSER:
                                pulseWidthList.get(listSize - 1).setVisible(false); // 设置不可见
                                pulseWidthList.get(listSize - 2).setVisible(true); // 设置可见
                                pulseWidthList.get(listSize - 3).setVisible(false); // 设置不可见
                                break; // 跳出循环
                            case TriggerPulseWidth.PW_RELATION_GREATER:
                                pulseWidthList.get(listSize - 1).setVisible(true); // 设置可见
                                pulseWidthList.get(listSize - 2).setVisible(false); // 设置不可见
                                pulseWidthList.get(listSize - 3).setVisible(false); // 设置不可见
                                break; // 跳出循环
                            case TriggerPulseWidth.PW_RELATION_EQUAL:
                                pulseWidthList.get(listSize - 1).setVisible(false); // 设置不可见
                                pulseWidthList.get(listSize - 2).setVisible(false); // 设置不可见
                                pulseWidthList.get(listSize - 3).setVisible(true); // 设置可见
                                break; // 跳出循环
                            case TriggerPulseWidth.PW_RELATION_NOT_EQUAL:
                                pulseWidthList.get(listSize - 1).setVisible(true); // 设置可见
                                pulseWidthList.get(listSize - 2).setVisible(true); // 设置可见
                                pulseWidthList.get(listSize - 3).setVisible(false); // 设置不可见
                                break; // 跳出循环
                        }
                    }
                    if (topMsgTrigger.getTriggerDetail() instanceof TopMsgTriggerLogic) {
                        TopMsgTriggerLogic msgTriggerLogic = (TopMsgTriggerLogic) topMsgTrigger.getTriggerDetail();
                        List<ExternalKeysNode> triggerLogicList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes()
                                .get(TopLayoutTrigger.DETAIL_LOGIC).getChildNodes();
                        int listSize = triggerLogicList.size();
                        switch (msgTriggerLogic.getCondition().getIndex()) { // switch分支选择
                            case TriggerLogic.LOGIC_RELATION_LESSER:
                                triggerLogicList.get(listSize - 1).setVisible(false); // 设置不可见
                                triggerLogicList.get(listSize - 2).setVisible(true); // 设置可见
                                triggerLogicList.get(listSize - 3).setVisible(false); // 设置不可见
                                break; // 跳出循环
                            case TriggerLogic.LOGIC_RELATION_GREATER:
                                triggerLogicList.get(listSize - 1).setVisible(true); // 设置可见
                                triggerLogicList.get(listSize - 2).setVisible(false); // 设置不可见
                                triggerLogicList.get(listSize - 3).setVisible(false); // 设置不可见
                                break; // 跳出循环
                            case TriggerLogic.LOGIC_RELATION_EQUAL:
                                triggerLogicList.get(listSize - 1).setVisible(false); // 设置不可见
                                triggerLogicList.get(listSize - 2).setVisible(false); // 设置不可见
                                triggerLogicList.get(listSize - 3).setVisible(true); // 设置可见
                                break; // 跳出循环
                            case TriggerLogic.LOGIC_RELATION_NOT_EQUAL:
                                triggerLogicList.get(listSize - 1).setVisible(true); // 设置可见
                                triggerLogicList.get(listSize - 2).setVisible(true); // 设置可见
                                triggerLogicList.get(listSize - 3).setVisible(false); // 设置不可见
                                break; // 跳出循环
                            case TriggerLogic.LOGIC_RELATION_TRUE:
                            case TriggerLogic.LOGIC_RELATION_FALSE:
                                triggerLogicList.get(listSize - 1).setVisible(false); // 设置不可见
                                triggerLogicList.get(listSize - 2).setVisible(false); // 设置不可见
                                triggerLogicList.get(listSize - 3).setVisible(false); // 设置不可见
                                break; // 跳出循环
                        }
                    }

                }
            }
            isReceiveMsgNode = false; // 取消消息接收标记
        }
    };

    private Consumer<TopMsgAuto> consumerTopLayoutAuto = new Consumer<TopMsgAuto>() { // RxBus消息消费者
        @Override
        public void accept(@NonNull TopMsgAuto topMsgAuto) throws Exception {
            if (topMsgAuto.getAutoTitle().isRxMsgSelect()) {
                if (!isReceiveMsgNode) {
                    List<ExternalKeysNode> autoList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_AUTO).getChildNodes();
                    int autoIndex = topMsgAuto.getAutoTitle().getIndex();
                    autoList.get(LIST_HEAD).setCurListSelect(autoIndex); // 设置列表选中项
                    if (!topMsgAuto.isFromEventBus()) {
                        topList = autoList.get(autoIndex).getChildNodes(); // 进入子菜单节点
                        topIndex = topList.get(LIST_HEAD).getCurListSelect();
                    }
                }
            } else {
                if (topMsgAuto.getAutoDetail() instanceof TopMsgAutoRange) {
                    //autoRange中，vertical、horizontal、level三个控件的预选框显现�?
                    boolean rangeEnable = ((TopMsgAutoRange) topMsgAuto.getAutoDetail()).getRange().isValue();
                    List<ExternalKeysNode> rangeList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_AUTO).getChildNodes()
                            .get(TopLayoutAuto.DETAIL_RANGE).getChildNodes();
                    for (int i = 1; i < rangeList.size(); i++) {
                        rangeList.get(i).setVisible(rangeEnable);
                    }

                    if (Objects.equals(topList.get(LIST_HEAD).getName(), rangeList.get(LIST_HEAD).getName())) {
                        while (!topList.get(topIndex).isVisible()) {
                            topIndex = topIndex != 0 ? topIndex - 1 : topList.size() - 1;
                        }
                        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(topList, topIndex, true); // 更新焦点框位置
                        } else {
                            topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                        }
                    }
                } else if (topMsgAuto.getAutoDetail() instanceof TopMsgAutoSet) {
                    //autoSet中，ThresholdLevel控件的预选框显现�?
                    boolean autoChEnable = ((TopMsgAutoSet) topMsgAuto.getAutoDetail()).getOpenChannel().isValue();
                    List<ExternalKeysNode> autoSetList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_AUTO).getChildNodes()
                            .get(TopLayoutAuto.DETAIL_SET).getChildNodes();
                    for (int i = 1; i <= 3; i++) {
                        autoSetList.get(i).setVisible(autoChEnable);
                    }

                    if (Objects.equals(topList.get(LIST_HEAD).getName(), autoSetList.get(LIST_HEAD).getName())) {
                        while (!topList.get(topIndex).isVisible()) {
                            topIndex = topIndex != 0 ? topIndex - 1 : topList.size() - 1;
                        }
                        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(topList, topIndex, true); // 更新焦点框位置
                        } else {
                            topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                        }
                    }
                }
            }
            isReceiveMsgNode = false; // 取消消息接收标记
        }
    };

    private Consumer<TopMsgUserset> consumerTopLayoutUserSet = new Consumer<TopMsgUserset>() { // RxBus消息消费者
        @Override
        public void accept(@NonNull TopMsgUserset topMsgUserset) throws Exception {
            if (!isReceiveMsgNode) {
                if (topMsgUserset.getUsersetTitle().isRxMsgSelect()) {
                    List<ExternalKeysNode> list = getTopBaseList();
                    if(TopLayoutPopWindow.DETAIL_USERSET < list.size()) {
                        List<ExternalKeysNode> usersetList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_USERSET).getChildNodes();
                        //Log.d("Tag.Debug", String.format("ExternalKeysManager.accept: %s",usersetList ));
                        int usersetIndex = topMsgUserset.getUsersetTitle().getIndex();
                        usersetList.get(LIST_HEAD).setCurListSelect(usersetIndex); // 设置列表选中项
                        topList = usersetList.get(usersetIndex).getChildNodes(); // 进入子菜单节点
                        topIndex = topList.get(LIST_HEAD).getCurListSelect();
                    }
                }
            }
            isReceiveMsgNode = false; // 取消消息接收标记
        }
    };

    private Consumer<RightMsgSerials> consumerRightLayoutSerial = new Consumer<RightMsgSerials>() { // RxBus消息消费者
        @Override
        public void accept(@NonNull RightMsgSerials rightMsgSerials) throws Exception {
            if (rightMsgSerials.getSerialsType().isRxMsgSelect()) {
                setTriggerSerialsType(rightMsgSerials.getSerialsNumber());
                if (!isReceiveMsgNode) {
                    if (rightMsgSerials.isSerials1()) {
                        List<ExternalKeysNode> s1List = getRightSerialsBaseList(CacheUtil.S1);
                        int s1Index = rightMsgSerials.getSerialsType().getIndex() + 5;
                        s1List.get(LIST_HEAD).setCurListSelect(s1Index); // 设置列表选中项
                        if (!rightMsgSerials.isFromEventBus()) {
                            rightS1List = s1List.get(s1Index).getChildNodes(); // 进入子菜单节点
                            rightS1Index = rightS1List.get(LIST_HEAD).getCurListSelect();
                        }
                    } else if (rightMsgSerials.isSerials2()) {
                        List<ExternalKeysNode> s2List = getRightSerialsBaseList(CacheUtil.S2);
                        int s2Index = rightMsgSerials.getSerialsType().getIndex() + 5;
                        s2List.get(LIST_HEAD).setCurListSelect(s2Index); // 设置列表选中项
                        if (!rightMsgSerials.isFromEventBus()) {
                            rightS2List = s2List.get(s2Index).getChildNodes(); // 进入子菜单节点
                            rightS2Index = rightS2List.get(LIST_HEAD).getCurListSelect();
                        }
                    } else if (rightMsgSerials.isSerials3()) {
                        List<ExternalKeysNode> s3List = getRightSerialsBaseList(CacheUtil.S3);
                        int s3Index = rightMsgSerials.getSerialsType().getIndex() + 5;
                        s3List.get(LIST_HEAD).setCurListSelect(s3Index); // 设置列表选中项
                        if (!rightMsgSerials.isFromEventBus()) {
                            rightS3List = s3List.get(s3Index).getChildNodes(); // 进入子菜单节点
                            rightS3Index = rightS3List.get(LIST_HEAD).getCurListSelect();
                        }
                    } else if (rightMsgSerials.isSerials4()) {
                        List<ExternalKeysNode> s4List = getRightSerialsBaseList(CacheUtil.S4);
                        int s4Index = rightMsgSerials.getSerialsType().getIndex() + 5;
                        s4List.get(LIST_HEAD).setCurListSelect(s4Index); // 设置列表选中项
                        if (!rightMsgSerials.isFromEventBus()) {
                            rightS4List = s4List.get(s4Index).getChildNodes(); // 进入子菜单节点
                            rightS4Index = rightS4List.get(LIST_HEAD).getCurListSelect();
                        }
                    }
                }
            } else {
                if (rightMsgSerials.getSerialsDetails() instanceof RightMsgSerialsSpi) {
                    //rightSerialsSpi中，csSwitch的切换导致cs和csLow的显现�?�改�?
                    RightMsgSerialsSpi msgSerialsSpi = (RightMsgSerialsSpi) rightMsgSerials.getSerialsDetails();
                    boolean csSwitch = msgSerialsSpi.getCsSwitch().isValue();
                    if (rightMsgSerials.isSerials1()) {
                        List<ExternalKeysNode> spiList = getRightSerialsBaseList(CacheUtil.S1).
                                get(RightLayoutSerials.SERIALS_SPI + 5).getChildNodes();
                        setRightSerialsSpiCs(CacheUtil.S1);
                        if (Objects.equals(rightS1List.get(LIST_HEAD).getName(), spiList.get(LIST_HEAD).getName())) {
                            while (!rightS1List.get(rightS1Index).isVisible()) {
                                rightS1Index = rightS1Index != 0 ? rightS1Index - 1 : rightS1List.size() - 1;
                            }
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1) // 判断S1串行总线菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightS1List, rightS1Index, true); // 更新焦点框位置
                            } else {
                                rightS1List.get(LIST_HEAD).setCurListSelect(rightS1Index); // 设置列表选中项
                            }
                        }
                    } else if (rightMsgSerials.isSerials2()) {
                        List<ExternalKeysNode> spiList = getRightSerialsBaseList(CacheUtil.S2).
                                get(RightLayoutSerials.SERIALS_SPI + 5).getChildNodes();
                        setRightSerialsSpiCs(CacheUtil.S2);
                        if (Objects.equals(rightS2List.get(LIST_HEAD).getName(), spiList.get(LIST_HEAD).getName())) {
                            while (!rightS2List.get(rightS2Index).isVisible()) {
                                rightS2Index = rightS2Index != 0 ? rightS2Index - 1 : rightS2List.size() - 1;
                            }
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2) // 判断S2串行总线菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightS2List, rightS2Index, true); // 更新焦点框位置
                            } else {
                                rightS2List.get(LIST_HEAD).setCurListSelect(rightS2Index); // 设置列表选中项
                            }
                        }
                    } else if (rightMsgSerials.isSerials3()) {
                        List<ExternalKeysNode> spiList = getRightSerialsBaseList(CacheUtil.S3).
                                get(RightLayoutSerials.SERIALS_SPI + 5).getChildNodes();
                        setRightSerialsSpiCs(CacheUtil.S3);
                        if (Objects.equals(rightS3List.get(LIST_HEAD).getName(), spiList.get(LIST_HEAD).getName())) {
                            while (!rightS3List.get(rightS3Index).isVisible()) {
                                rightS3Index = rightS3Index != 0 ? rightS3Index - 1 : rightS3List.size() - 1;
                            }
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3) // 判断S3串行总线菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightS3List, rightS3Index, true); // 更新焦点框位置
                            } else {
                                rightS3List.get(LIST_HEAD).setCurListSelect(rightS3Index); // 设置列表选中项
                            }
                        }
                    } else if (rightMsgSerials.isSerials4()) {
                        List<ExternalKeysNode> spiList = getRightSerialsBaseList(CacheUtil.S4).
                                get(RightLayoutSerials.SERIALS_SPI + 5).getChildNodes();
                        setRightSerialsSpiCs(CacheUtil.S4);
                        if (Objects.equals(rightS4List.get(LIST_HEAD).getName(), spiList.get(LIST_HEAD).getName())) {
                            while (!rightS4List.get(rightS4Index).isVisible()) {
                                rightS4Index = rightS4Index != 0 ? rightS4Index - 1 : rightS4List.size() - 1;
                            }
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4) // 判断S4串行总线菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightS4List, rightS4Index, true); // 更新焦点框位置
                            } else {
                                rightS4List.get(LIST_HEAD).setCurListSelect(rightS4Index); // 设置列表选中项
                            }
                        }
                    }
                }
            }
            int typeIndex = CacheUtil.S1;
            if (rightMsgSerials.isSerials1()) {
                typeIndex = CacheUtil.S1;
            } else if (rightMsgSerials.isSerials2()) {
                typeIndex = CacheUtil.S2;
            } else if (rightMsgSerials.isSerials3()) {
                typeIndex = CacheUtil.S3;
            } else if (rightMsgSerials.isSerials4()) {
                typeIndex = CacheUtil.S4;
            }
            int serialsType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + typeIndex); // 从缓存读取整数值
            if (rightMsgSerials.getSerialsDetails() instanceof RightMsgSerialsUart
                    || rightMsgSerials.getSerialsDetails() instanceof RightMsgSerialsSpi
                    || rightMsgSerials.getSerialsDetails() instanceof RightMsgSerialsM429) {
                if (rightMsgSerials.getSerialsType().getIndex() == serialsType) {
                    setTriggerSerialsChildren(typeIndex);
                }
            }
            if (rightMsgSerials.getSerialsType().getIndex() == serialsType) {
                setTriggerSerialsNumberKeyBoard(typeIndex);
            }
            isReceiveMsgNode = false; // 取消消息接收标记
        }
    };

    /**
     * 设置通道探头相关事件的节点可见性和位置
     * @param msgChannel 通道消息对象
     * @param temList 当前通道节点列表
     * @param chIdx 通道索引
     */
    private void setEventProbe(RightMsgChannel msgChannel,List<ExternalKeysNode> temList,int chIdx){
        if (msgChannel.isFromEventBus()){
            int probeType= RightLayoutChannel.getProbeType(chIdx);
            switch (probeType){ // switch分支选择
                case RightLayoutChannel.ProbeType_NONE:{
                    temList.get(7).setVisible(true); // 设置可见
                    temList.get(8).setVisible(true); // 设置可见
                    temList.get(9).setVisible(true); // 设置可见

                    temList.get(10).setVisible(false); // 设置不可见

                    temList.get(11).setVisible(false); // 设置不可见
                    temList.get(12).setVisible(false); // 设置不可见
                    temList.get(13).setVisible(false); // 设置不可见
                    temList.get(14).setVisible(false); // 设置不可见
                }break;

                case RightLayoutChannel.ProbeType_MSP:{
                    temList.get(7).setVisible(false); // 设置不可见
                    temList.get(8).setVisible(false); // 设置不可见
                    temList.get(9).setVisible(false); // 设置不可见

                    temList.get(10).setVisible(true); // 设置可见

                    temList.get(11).setVisible(false); // 设置不可见
                    temList.get(12).setVisible(false); // 设置不可见
                    temList.get(13).setVisible(false); // 设置不可见
                    temList.get(14).setVisible(false); // 设置不可见
                }break;
                case RightLayoutChannel.ProbeType_MRCP:{
                    temList.get(7).setVisible(false); // 设置不可见
                    temList.get(8).setVisible(false); // 设置不可见
                    temList.get(9).setVisible(false); // 设置不可见

                    temList.get(10).setVisible(false); // 设置不可见

                    temList.get(11).setVisible(false); // 设置不可见
                    temList.get(12).setVisible(false); // 设置不可见
                    temList.get(13).setVisible(false); // 设置不可见
                    temList.get(14).setVisible(true); // 设置可见
                }break;
                case RightLayoutChannel.ProbeType_MOIP:
                case RightLayoutChannel.probeType_MDP:{
                    temList.get(7).setVisible(false); // 设置不可见
                    temList.get(8).setVisible(false); // 设置不可见
                    temList.get(9).setVisible(false); // 设置不可见

                    temList.get(10).setVisible(false); // 设置不可见

                    temList.get(11).setVisible(true); // 设置可见
                    temList.get(12).setVisible(true); // 设置可见
                    temList.get(13).setVisible(true); // 设置可见
                    temList.get(14).setVisible(false); // 设置不可见
                }break;


            }
        }
    }
    private Consumer<RightMsgChannel> consumerRightLayoutChannel = new Consumer<RightMsgChannel>() { // RxBus消息消费者
        @Override
        public void accept(RightMsgChannel msgChannel) throws Exception {
            List<ExternalKeysNode> temList;
            switch (msgChannel.getChannelNumber()) { // switch分支选择
                case TChan.Ch1: {
//                    getRightChBaseList(MainViewGroup.RIGHTSLIP_CH1).get(12)//带宽的输入框的显现�?�设�?
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 1 || msgChannel.getBandWidth().getIndex() == 2);
                    temList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH1); // 获取右侧通道基础列表
//                    temList.get(16)//200M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(17)//20M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
                    temList.get(20)//输入框
                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(temList.size() - 2).setVisible(msgChannel.getFineSwitch().isValue());
                    setEventProbe(msgChannel, temList, ChannelFactory.CH1);
                    if (!rightCh1List.get(rightCh1Index).isVisible()) {
                        rightCh1Index--;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
                        } else {
                            rightCh1List.get(LIST_HEAD).setCurListSelect(rightCh1Index); // 设置列表选中项
                        }
                    }
                }
                break; // 跳出循环
                case TChan.Ch2: {
//                    getRightChBaseList(MainViewGroup.RIGHTSLIP_CH2).get(12)//带宽的输入框的显现�?�设�?
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 1 || msgChannel.getBandWidth().getIndex() == 2);
                    temList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH2); // 获取右侧通道基础列表
//                    temList.get(16)//200M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(17)//20M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
                    temList.get(20)//输入框
                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(temList.size() - 2).setVisible(msgChannel.getFineSwitch().isValue());
                    setEventProbe(msgChannel, temList, ChannelFactory.CH2);
                    if (!rightCh2List.get(rightCh2Index).isVisible()) {
                        rightCh2Index--;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
                        } else {
                            rightCh2List.get(LIST_HEAD).setCurListSelect(rightCh2Index); // 设置列表选中项
                        }
                    }
                }
                break; // 跳出循环
                case TChan.Ch3: {
//                    getRightChBaseList(MainViewGroup.RIGHTSLIP_CH3).get(12)//带宽的输入框的显现�?�设�?
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 1 || msgChannel.getBandWidth().getIndex() == 2);
                    temList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH3); // 获取右侧通道基础列表
//                    temList.get(16)//200M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(17)//20M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
                    temList.get(20)//输入框
                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(temList.size() - 2).setVisible(msgChannel.getFineSwitch().isValue());
                    setEventProbe(msgChannel, temList, ChannelFactory.CH3);
                    if (!rightCh3List.get(rightCh3Index).isVisible()) {
                        rightCh3Index--;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
                        } else {
                            rightCh3List.get(LIST_HEAD).setCurListSelect(rightCh3Index); // 设置列表选中项
                        }
                    }
                }
                break; // 跳出循环
                case TChan.Ch4:
//                    getRightChBaseList(MainViewGroup.RIGHTSLIP_CH4).get(12)//带宽的输入框的显现�?�设�?
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 1 || msgChannel.getBandWidth().getIndex() == 2);

                    temList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH4); // 获取右侧通道基础列表
//                    temList.get(16)//200M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(17)//20M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
                    temList.get(20)//输入框
                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(temList.size() - 2).setVisible(msgChannel.getFineSwitch().isValue());
                    setEventProbe(msgChannel, temList, ChannelFactory.CH4);
                    if (!rightCh4List.get(rightCh4Index).isVisible()) {
                        rightCh4Index--;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
                        } else {
                            rightCh4List.get(LIST_HEAD).setCurListSelect(rightCh4Index); // 设置列表选中项
                        }
                    }
                    break; // 跳出循环
                case TChan.Ch5:
//                    getRightChBaseList(MainViewGroup.CHANNEL_CH5).get(12)//带宽的输入框的显现�?�设�?
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 1 || msgChannel.getBandWidth().getIndex() == 2);

                    temList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH5); // 获取右侧通道基础列表
//                    temList.get(16)//200M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(17)//20M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
                    temList.get(20)//输入框
                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(temList.size() - 2).setVisible(msgChannel.getFineSwitch().isValue());
                    setEventProbe(msgChannel, temList, ChannelFactory.CH5);
                    if (!rightCh5List.get(rightCh4Index).isVisible()) {
                        rightCh5Index--;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
                        } else {
                            rightCh5List.get(LIST_HEAD).setCurListSelect(rightCh5Index); // 设置列表选中项
                        }
                    }
                    break; // 跳出循环
                case TChan.Ch6:
//                    getRightChBaseList(MainViewGroup.CHANNEL_CH6).get(12)//带宽的输入框的显现?设?
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 1 || msgChannel.getBandWidth().getIndex() == 2);

                    temList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH6); // 获取右侧通道基础列表
//                    temList.get(16)//200M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(17)//20M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
                    temList.get(20)//输入框
                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(temList.size() - 2).setVisible(msgChannel.getFineSwitch().isValue());
                    setEventProbe(msgChannel, temList, ChannelFactory.CH6);
                    if (!rightCh6List.get(rightCh6Index).isVisible()) {
                        rightCh6Index--;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
                        } else {
                            rightCh6List.get(LIST_HEAD).setCurListSelect(rightCh6Index); // 设置列表选中项
                        }
                    }
                    break; // 跳出循环
                case TChan.Ch7:
//                    getRightChBaseList(MainViewGroup.CHANNEL_CH7).get(12)//带宽的输入框的显现?设?
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 1 || msgChannel.getBandWidth().getIndex() == 2);

                    temList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH7); // 获取右侧通道基础列表
//                    temList.get(16)//200M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(17)//20M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
                    temList.get(20)//输入框
                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(temList.size() - 2).setVisible(msgChannel.getFineSwitch().isValue());
                    setEventProbe(msgChannel, temList, ChannelFactory.CH7);
                    if (!rightCh7List.get(rightCh7Index).isVisible()) {
                        rightCh7Index--;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
                        } else {
                            rightCh7List.get(LIST_HEAD).setCurListSelect(rightCh7Index); // 设置列表选中项
                        }
                    }
                    break; // 跳出循环
                case TChan.Ch8:
//                    getRightChBaseList(MainViewGroup.CHANNEL_CH8).get(12)//带宽的输入框的显现?设?
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 1 || msgChannel.getBandWidth().getIndex() == 2);

                    temList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH8); // 获取右侧通道基础列表
//                    temList.get(16)//200M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(17)//20M
//                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
//                                    || msgChannel.getBandWidth().getIndex() == 4);
                    temList.get(20)//输入框
                            .setVisible(msgChannel.getBandWidth().getIndex() == 3 //High、Low
                                    || msgChannel.getBandWidth().getIndex() == 4);
//                    temList.get(temList.size() - 2).setVisible(msgChannel.getFineSwitch().isValue());
                    setEventProbe(msgChannel, temList, ChannelFactory.CH8);
                    if (!rightCh8List.get(rightCh8Index).isVisible()) {
                        rightCh8Index--;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
                        } else {
                            rightCh8List.get(LIST_HEAD).setCurListSelect(rightCh8Index); // 设置列表选中项
                        }
                    }
                    break; // 跳出循环
            }
        }
    };

    private Consumer<TopMsgSampleMode> consumerTopLayoutSampleMode = new Consumer<TopMsgSampleMode>() { // RxBus消息消费者
        @Override
        public void accept(TopMsgSampleMode msgSample) throws Exception {
            //设置sample中的两个调节按钮的显现�?�，以及sample选项的显现�??
            boolean visible = msgSample.getSample().getIndex() == 1 || msgSample.getSample().getIndex() == 2;
            List<ExternalKeysNode> curList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_SAMPLE).getChildNodes()
                    .get(TopLayoutSample.DETAIL_MODE).getChildNodes();
            curList.get(1).setVisible(msgSample.getSampleEnable()[1]);
            curList.get(2).setVisible(msgSample.getSampleEnable()[2]);
            curList.get(3).setVisible(msgSample.getSampleEnable()[3]);
            curList.get(4).setVisible(visible);
//            Logger.i("topList:" + topList);
//            Logger.i("topIndex:" + topIndex);
//            Logger.i("curList:" + curList);
            if (Objects.equals(topList, curList) && !topList.get(topIndex).isVisible()) {
                topIndex = 0; // 初始化索引为0
                if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                        && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                    setViewPlace(topList, topIndex, true); // 更新焦点框位置
                } else {
                    topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                }
            }
        }
    };

    private Consumer<TopMsgSampleDepth> consumerTopLayoutSampleDepth = new Consumer<TopMsgSampleDepth>() { // RxBus消息消费者
        @Override
        public void accept(TopMsgSampleDepth msgSampleDepth) throws Exception {
            int depthNumber = MemDepthFactory.getMemDepth().getMemDepthItemName().size();
            ExternalKeysNode depthNode = getTopBaseList().get(TopLayoutPopWindow.DETAIL_SAMPLE).getChildNodes()
                    .get(TopLayoutSample.DETAIL_DEPTH);
            if (depthNode.getChildNodes().size() != depthNumber) {
                List<ExternalKeysNode> sampleDepthNodeList = ExternalKeysNodeUtil.getTopSampleDepthNodeList(depthNode
                        , depthNode.getParentNode().getChildNodes(), topSlipOffset);
                depthNode.setChildNodes(sampleDepthNodeList); // 设置子节点列表
                if (Objects.equals(topList.get(LIST_HEAD).getName(), depthNode.getChildNodes().get(LIST_HEAD).getName())) {
                    topIndex = LIST_HEAD;
                    if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                            && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                        setViewPlace(topList, topIndex, true); // 更新焦点框位置
                    } else {
                        topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                    }
                }
            }
        }
    };

    private Consumer<TopMsgSaveSegments> consumerTopLayoutSaveSegments = new Consumer<TopMsgSaveSegments>() { // RxBus消息消费者
        @Override
        public void accept(TopMsgSaveSegments topMsgSaveSegments) throws Exception {
//            boolean b = topMsgSaveSegments.isVisibleSegments();
//            List<ExternalKeysNode> childNodes = getTopBaseList().get(TopLayoutPopWindow.DETAIL_SAVE).getChildNodes()
//                    .get(TopLayoutSave.DETAIL_STORE).getChildNodes()
//                    .get(TopLayoutSaveStore.DETAIL_BIN).getChildNodes();
//            int index = Tools.indexOf(childNodes, s -> s.getName().equalsIgnoreCase("All Segments"));
//            if (index != -1) {
//                childNodes.get(index).setVisible(b);
//            }
        }
    };

    private Consumer<TopMsgSampleSegmented> consumerTopLayoutSampleSegmented=new Consumer<TopMsgSampleSegmented>() { // RxBus消息消费者
        @Override
        public void accept(TopMsgSampleSegmented msgSegmented) throws Exception {

            List<ExternalKeysNode> childNodes = getTopBaseList().get(TopLayoutPopWindow.DETAIL_SAMPLE)
                    .getChildNodes().get(TopLayoutSample.DETAIL_SEGMENTED).getChildNodes();
            boolean bTrue= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY)==0; // 从缓存读取整数值
            childNodes.get(childNodes.size()-4).setVisible(!bTrue);
            childNodes.get(childNodes.size()-3).setVisible(!bTrue);
            childNodes.get(childNodes.size()-2).setVisible(bTrue);
            childNodes.get(childNodes.size()-1).setVisible(bTrue);

            int SegNumIndex= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_NUMBER); // 从缓存读取整数值
            if (SegNumIndex==3){
                childNodes.get(5).setVisible(true); // 设置可见
            }else{
                childNodes.get(5).setVisible(false); // 设置不可见
            }

            int maxSegmentNums = SegmentSample.getInstance().getMaxSegmentNums();
            String[] array = App.get().getResources().getStringArray(R.array.sampleSegmentedNumber);
            if (maxSegmentNums < Integer.parseInt(array[0])){
                childNodes.get(1).setVisible(false); // 设置不可见
            }else {
                childNodes.get(1).setVisible(true); // 设置可见
            }
            if (maxSegmentNums < Integer.parseInt(array[1])){
                childNodes.get(2).setVisible(false); // 设置不可见
            }else {
                childNodes.get(2).setVisible(true); // 设置可见
            }


            while (topIndex >= topList.size() || (topIndex >= 0 && !topList.get(topIndex).isVisible()))
            {
                topIndex--;
            }
            if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                setViewPlace(topList, topIndex, true); // 更新焦点框位置
            } else {
                topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
            }
        }
    };

    private Consumer<RightMsgMath> consumerRightLayoutMath = new Consumer<RightMsgMath>() { // RxBus消息消费者
        @Override
        public void accept(RightMsgMath msgMath) throws Exception {
            int mathType = msgMath.getMathType().getValue(); // 获取数学运算类型
            int mathChannel = msgMath.getMathChannelNumber();
            List<ExternalKeysNode> rightMathBaseList = null;
            switch (mathChannel) { // switch分支选择
                case TChan.Math1:
                    rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH1); // 获取右侧数学运算基础列表
                    break; // 跳出循环
                case TChan.Math2:
                    rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH2); // 获取右侧数学运算基础列表
                    break; // 跳出循环
                case TChan.Math3:
                    rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH3); // 获取右侧数学运算基础列表
                    break; // 跳出循环
                case TChan.Math4:
                    rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH4); // 获取右侧数学运算基础列表
                    break; // 跳出循环
                case TChan.Math5:
                    rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH5); // 获取右侧数学运算基础列表
                    break; // 跳出循环
                case TChan.Math6:
                    rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH6); // 获取右侧数学运算基础列表
                    break; // 跳出循环
                case TChan.Math7:
                    rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH7); // 获取右侧数学运算基础列表
                    break; // 跳出循环
                case TChan.Math8:
                    rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH8); // 获取右侧数学运算基础列表
                    break; // 跳出循环
            }
            if (rightMathBaseList == null) return;
//            int curMathType = CacheUtil.MATHTYPE_AXB;
//            for (int i = 0; i < rightMathBaseList.size(); i++) {
//                if (ExternalKeysNode.TYPE_NO_CLICK.equals(rightMathBaseList.get(i).getType())) {
//                    curMathType = i;
//                    break;
//                }
//            }
//            if (mathType != curMathType) {
            List<ExternalKeysNode> willLayoutPlaceList;
            if (mathType == CacheUtil.MATHTYPE_DW) {
                willLayoutPlaceList = ExternalKeysNodeUtil.getRightSlipMathShowDWLayoutPlaceList();
            }
/*            else if (mathType == CacheUtil.MATHTYPE_FFT) {
                willLayoutPlaceList = ExternalKeysNodeUtil.getRightSlipMathShowFftLayoutPlaceList();
                boolean b = (msgMath.getFftPersist().getIndex() == 0 || msgMath.getFftPersist().getIndex() == 1);
                ExternalKeysNode node= rightMathBaseList.get(6).getChildNodes().get(17);
                if (b){
                    node.setVisible(false); // 设置不可见
                }else {
                    node.setVisible(true); // 设置可见
                }
            } */
            else if (mathType == CacheUtil.MATHTYPE_AXB) {
                willLayoutPlaceList = ExternalKeysNodeUtil.getRightSlipMathShowAxbLayoutPlaceList();
            } else {
                willLayoutPlaceList = ExternalKeysNodeUtil.getRightSlipMathShowAdvanceLayoutPlaceList();
            }
            for (int i = 0; i < rightMathBaseList.size(); i++) { // 遍历节点列表
                ExternalKeysNode node = rightMathBaseList.get(i);
                rightMathBaseList.get(i).setPlace(node.getIndex(), node.getX(), node.getY(), node.getW(), node.getH()); // 设置节点位置和尺寸
                rightMathBaseList.get(i).setType(node.getType()); // 设置节点类型
            }
//            }
        }
    };

    private Consumer<MainBottomMsgQuick> consumerMainBottomQuickEnable = new Consumer<MainBottomMsgQuick>() { // RxBus消息消费者
        @Override
        public void accept(MainBottomMsgQuick msgQuick) throws Exception {
            bottomList.get(0).setVisible(msgQuick.getEnable()[0]);
            bottomList.get(1).setVisible(msgQuick.getEnable()[1]);
            bottomList.get(2).setVisible(msgQuick.getEnable()[2]);
            bottomList.get(3).setVisible(msgQuick.getEnable()[3]);
            bottomList.get(4).setVisible(msgQuick.getEnable()[4]);
//            bottomList.get(5).setVisible(msgQuick.getEnable()[5]);
        }
    };
//    private Consumer<TopMsgSampleSegmented> consumerTopLayoutSampleSegmented = new Consumer<TopMsgSampleSegmented>() {
//        @Override
//        public void accept(TopMsgSampleSegmented msgSegmented) throws Exception {
////            boolean isXYMode = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1;
////            boolean isSerialsTxt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);
////            boolean isRoll = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ROLL) == 0
////                    && Tools.isSlowTimeBase();
////            boolean isState = !isXYMode && !isSerialsTxt && !isRoll;
////
////            int maxSegmentNums = SegmentSample.getInstance().getMaxSegmentNums();
////            String[] segmentNumArray = App.get().getResources().getStringArray(R.array.sampleSegmentedNumber);
////
////            boolean visibleNumber = msgSegmented.getNumber().getIndex() == 3;
////            boolean visibleDisplayDetail = msgSegmented.getDisplay().getIndex() == 1;
////
////            List<ExternalKeysNode> curList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_SAMPLE)
////                    .getChildNodes().get(TopLayoutSample.DETAIL_SEGMENTED).getChildNodes();
////            curList.get(0).setVisible(isState);
////            curList.get(1).setVisible(isState);
////            curList.get(2).setVisible(maxSegmentNums >= Integer.parseInt(segmentNumArray[0]));
////            curList.get(3).setVisible(maxSegmentNums >= Integer.parseInt(segmentNumArray[1]));
////            curList.get(6).setVisible(visibleNumber);
////            curList.get(9).setVisible(visibleDisplayDetail);
////            curList.get(10).setVisible(visibleDisplayDetail);
////            curList.get(11).setVisible(!visibleDisplayDetail);
////            curList.get(12).setVisible(!visibleDisplayDetail);
////            if (!topList.get(topIndex).isVisible()) {
//////                topIndex -= 1;
//////                if (topIndex < 0) {
//////                    topIndex = topList.size() - 1;
//////                }
////                if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)
////                        && focusView != null && focusView.getVisibility() == View.VISIBLE) {
////                    setViewPlace(topList, topIndex, true);
////                } else {
////                    topList.get(LIST_HEAD).setCurListSelect(topIndex);
////                }
////            }
//        }
//    };

    /**
     * 通道激活状态变更回调处理
     * @param obj 通道激活变更消息对象
     */
    private void OnChanActiveChange(Object obj) {
        MQEnum mqEnum= RxBusRegister.parseMqEnum(obj);
        setCenterChannels();
        if (mqEnum!=MQEnum.CH_ACTIVE)return;

        IChan chNo = ((MsgChActiveChange)obj).getChan();
        String MsgType = "0";
        switch (chNo) { // switch分支选择
            case Math1:
            case Math2:
            case Math3:
            case Math4:
            case Math5:
            case Math6:
            case Math7:
            case Math8:
                MsgType = ExternalKeysMsg_ToMCU.TYPE_MATH;
                break; // 跳出循环
            case R1:
            case R2:
            case R3:
            case R4:
            case R5:
            case R6:
            case R7:
            case R8:
                MsgType = ExternalKeysMsg_ToMCU.TYPE_REF;
                break; // 跳出循环
            case S1:
            case S2:
            case S3:
            case S4:
                MsgType = ExternalKeysMsg_ToMCU.TYPE_SERIAL;
                break; // 跳出循环
        }
        if (!MsgType.equals("0")) {
            RxBus.getInstance().post(RxEnum.EXTERNALKEY_TOMCU, new ExternalKeysMsg_ToMCU(MsgType,
                    ExternalKeysMsg_ToMCU.STATE_LED_ON));
        }

    }

    private Consumer<Integer> consumerMainCenterChannelMove = new Consumer<Integer>() { // RxBus消息消费者
        @Override
        public void accept(Integer integer) throws Exception {
            setCenterChannels();
        }
    };

    private Consumer<Integer> consumerChannelVisible = new Consumer<Integer>() { // RxBus消息消费者
        @Override
        public void accept(Integer integer) throws Exception {
            setCenterChannels();
        }
    };

    private Consumer<Boolean> consumerMainCenterSegmentedVisible = new Consumer<Boolean>() { // RxBus消息消费者
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            if (aBoolean) {
                if (focusView != null && focusView.getVisibility() == View.VISIBLE
                        && !mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)) { // 判断顶部菜单是否显示
                    setViewPlace(segmentedList, segmentedIndex, true); // 更新焦点框位置
                }
            } else {
                if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                    setFocusViewVisible(View.GONE); // 设置焦点框可见性
                }
                ExternalKeysUI.getInstance().setFocusControlBackVisible(false); // 设置焦点框返回按钮可见性
            }
        }
    };
    private Consumer<MainMsgCenterSegmented> consumerMainCenterSegmentedSelect = new Consumer<MainMsgCenterSegmented>() { // RxBus消息消费者
        @Override
        public void accept(MainMsgCenterSegmented msgCenterSegmented) throws Exception {
//            if (msgCenterSegmented.isPlaying().isValue() && msgCenterSegmented.getCurSingleFrame().isRxMsgSelect()) {
//                return;
//            } else {
//                setCenterSegmented();
//            }
            if (msgCenterSegmented.isDisplay().isRxMsgSelect()
                    || msgCenterSegmented.isSingleLarge().isRxMsgSelect()) {
                setCenterSegmented();
            }
        }
    };
    private Consumer<Integer> consumerMainCenterSegmentedMove = new Consumer<Integer>() { // RxBus消息消费者
        @Override
        public void accept(Integer integer) throws Exception {
            setCenterSegmented();
        }
    };

    private Consumer<Integer> consumerMainCenterMenuMove = new Consumer<Integer>() { // RxBus消息消费者
        @Override
        public void accept(Integer integer) throws Exception {
            setCenterMenu();
        }
    };

    private boolean bDelaySetChannelList = false;
    private synchronized  void setDelayChannelList(){ // 更新延迟通道列表
        if(!bDelaySetChannelList){
            bDelaySetChannelList = true;
            new Handler().postDelayed(()->{
                bDelaySetChannelList = false;
                setChannelList();
            },500);
        }
    }

    private Consumer consumerChannelChange = new Consumer() {
        @Override
        public void accept(Object o) throws Exception {
            if (o instanceof MainRightMsgChannels || o instanceof MainRightMsgOthers || o instanceof RightMsgRefForEight) {

                setDelayChannelList(); // 更新延迟通道列表
            }
        }
    };

    private Consumer<Integer> consumerVisibleChange = new Consumer<Integer>() { // RxBus消息消费者
        @Override
        public void accept(Integer integer) throws Exception {
            switch (integer) { // switch分支选择
                case MainViewGroup.VISIBLE_TOPMEASURE_SELECT:
                    setMeasureDelList();
                    break; // 跳出循环
                case MainViewGroup.VISIBLE_TOPTRIGGER_RUNT_HIGH:
                case MainViewGroup.VISIBLE_TOPTRIGGER_RUNT_LOW:
                    setTriggerRunt();
                    break; // 跳出循环
                case MainViewGroup.VISIBLE_TOPTRIGGER_SLOPE_HIGH:
                case MainViewGroup.VISIBLE_TOPTRIGGER_SLOPE_LOW:
                    setTriggerSlope();
                    break; // 跳出循环
                case MainViewGroup.VISIBLE_TOPTRIGGER_VIDEO:
                    setTriggerVideo();
                    if(null != topList
                            && topIndex < topList.size()
                            && null != topList.get(topIndex)
                            && null != topList.get(topIndex).getParentNode()) {
                        topList = topList.get(topIndex).getParentNode().getChildNodes();
                    }
                    break; // 跳出循环
                case MainViewGroup.VISIBLE_RIGHTSERIALS1_LIN:
                case MainViewGroup.VISIBLE_RIGHTSERIALS1_CAN:
                    setRightSerialsTextView(CacheUtil.S1);
                    break; // 跳出循环
                case MainViewGroup.VISIBLE_RIGHTSERIALS2_LIN:
                case MainViewGroup.VISIBLE_RIGHTSERIALS2_CAN:
                    setRightSerialsTextView(CacheUtil.S2);
                    break; // 跳出循环
                case MainViewGroup.VISIBLE_RIGHTSERIALS3_LIN:
                case MainViewGroup.VISIBLE_RIGHTSERIALS3_CAN:
                    setRightSerialsTextView(CacheUtil.S3);
                    break; // 跳出循环
                case MainViewGroup.VISIBLE_RIGHTSERIALS4_LIN:
                case MainViewGroup.VISIBLE_RIGHTSERIALS4_CAN:
                    setRightSerialsTextView(CacheUtil.S4);
                    break; // 跳出循环
                case MainViewGroup.VISIBLE_TOPMEASURE_CHANNEL:
//                    setChannelList();
                    setDelayChannelList(); // 更新延迟通道列表
                    break; // 跳出循环
            }
        }
    };

    private Consumer<boolean[]> consumerKeyboardFormulaEnable = new Consumer<boolean[]>() { // RxBus消息消费者
        @Override
        public void accept(boolean[] booleans) throws Exception {
            List<ExternalKeysNode> rightMathBaseList = null;
            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1)) { // 判断Math1菜单是否显示
                rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH1); // 获取右侧数学运算基础列表
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2)) { // 判断Math2菜单是否显示
                rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH2); // 获取右侧数学运算基础列表
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3)) { // 判断Math3菜单是否显示
                rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH3); // 获取右侧数学运算基础列表
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4)) { // 判断Math4菜单是否显示
                rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH4); // 获取右侧数学运算基础列表
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5)) { // 判断Math5菜单是否显示
                rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH5); // 获取右侧数学运算基础列表
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6)) { // 判断Math6菜单是否显示
                rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH6); // 获取右侧数学运算基础列表
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7)) { // 判断Math7菜单是否显示
                rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH7); // 获取右侧数学运算基础列表
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8)) { // 判断Math8菜单是否显示
                rightMathBaseList = getRightMathBaseList(MainViewGroup.RIGHTSLIP_MATH8); // 获取右侧数学运算基础列表
            }

            if (rightMathBaseList != null) {
                List<ExternalKeysNode> formulaNodes = rightMathBaseList.get(CacheUtil.MATHTYPE_AM + 5).getChildNodes().get(0).getChildNodes();//多了 Math Ref Serials +按钮 Delete
                for (int i = 0; i < booleans.length; i++) {
                    formulaNodes.get(i + 2).setVisible(booleans[i]);
                }
            }
        }
    };

//    private Consumer<Boolean> consumerSerialswordVisible = new Consumer<Boolean>() {
//        @Override
//        public void accept(Boolean aBoolean) throws Exception {
//            if (ScopeConfig.getConfig().isATO()) {
//                List<ExternalKeysNode> bottomBaseList = getBottomBaseList();
//                bottomBaseList.get(bottomBaseList.size() - 1).setVisible(!aBoolean);
//                bottomBaseList.get(bottomBaseList.size() - 2).setVisible(!aBoolean);
//            }
//        }
//    };

    /**
     * 更新探头接口对话框的节点可见性和位置
     * @param rightSlipNo 右侧滑出菜单编号
     */
    private void updateDialogProbeInterface(int rightSlipNo){
        if (mainViewGroup.isSlipShow(rightSlipNo)==false)return;

        List<ExternalKeysNode> listCh1 = getRightChBaseList(rightSlipNo);
        int idx= Tools.indexOf(listCh1,(node)->node.getName().equalsIgnoreCase("info")); //mdp 11
        int mcrpIdx= Tools.indexOf(listCh1,(node)->node.getName().equalsIgnoreCase("McrpInfo")); //mcrp
        int probeIdx= Tools.indexOf(listCh1,(node)->node.getName().equalsIgnoreCase("probeCal")); //msp
        List<ExternalKeysNode> dialogProbeInterface=null;
        ExternalKeysNode parentNode=null;
        int index=0;
        if (idx!=-1 && listCh1.get(idx).isVisible()) {
            parentNode=listCh1.get(idx);
            dialogProbeInterface = listCh1.get(idx).getChildNodes();
            dialogProbeInterface.clear();
            parentNode.setDialog(ExternalKeysNode.DIALOG_PROBE_INTERFACE); // 设置关联对话框类型
            index=idx;

        }else if (probeIdx!=-1 && listCh1.get(probeIdx).isVisible()){
            parentNode=listCh1.get(probeIdx);
            dialogProbeInterface=listCh1.get(probeIdx).getChildNodes();
            dialogProbeInterface.clear();
            parentNode.setDialog(ExternalKeysNode.DIALOG_PROBE_INTERFACE); // 设置关联对话框类型
            index=probeIdx;
        }else if (mcrpIdx!=-1 && listCh1.get(mcrpIdx).isVisible()){
            Log.d("Tag.Debug", String.format("ExternalKeysManager.updateDialogProbeInterface: %d",mcrpIdx ));
            parentNode=listCh1.get(mcrpIdx);
            dialogProbeInterface=listCh1.get(mcrpIdx).getChildNodes();
            dialogProbeInterface.clear();
            parentNode.setDialog(ExternalKeysNode.DIALOG_PROBE_INTERFACE); // 设置关联对话框类型
            index=mcrpIdx;
        }



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && dialogProbeInterface!=null) {
            List<ExternalKeysNode> finalDialogProbeInterface = dialogProbeInterface;
            ExternalKeysNode finalParentNode = parentNode;
            Tools.getViewRectByButton(mainViewGroup.getDialogProbeInterface().getRootView(),(rect, name)->{
                ExternalKeysNode node=new ExternalKeysNode();
                node.setPlace(finalDialogProbeInterface.size(),rect.left,rect.top,rect.width(),rect.height()); // 设置节点位置和尺寸
                node.setName(name); // 设置节点名称
                if (name.contains("Hz")) {
                    node.setType(ExternalKeysNode.TYPE_CLICK_IS_SUREBACK); // 设置节点类型
                }
                node.setParentNode(finalParentNode); // 设置父节点
                node.setParentNodes(listCh1);
                finalDialogProbeInterface.add(node);

            });
            //
            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1)) { // 判断CH1通道菜单是否显示
                rightCh1List = listCh1;
                rightCh1Index = index;
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2)) { // 判断CH2通道菜单是否显示
                rightCh2List = listCh1;
                rightCh2Index = index;
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3)) { // 判断CH3通道菜单是否显示
                rightCh3List = listCh1;
                rightCh3Index = index;
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4)) { // 判断CH4通道菜单是否显示
                rightCh4List = listCh1;
                rightCh4Index = index;
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5)) { // 判断CH5通道菜单是否显示
                rightCh5List = listCh1;
                rightCh5Index = index;
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6)) { // 判断CH6通道菜单是否显示
                rightCh6List = listCh1;
                rightCh6Index = index;
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7)) { // 判断CH7通道菜单是否显示
                rightCh7List = listCh1;
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8)) { // 判断CH8通道菜单是否显示
                rightCh8List = listCh1;
            }
        }


    }
    /**
     * 设置探头接口对话框中带宽计数的节点位置
     * @param rightSlipNo 右侧滑出菜单编号
     */
    private void setDialogProbeInterfaceBandWidthCount(int rightSlipNo){
        int count= mainViewGroup.getDialogProbeInterface().getListBandWidthCount();
        List<ExternalKeysNode> listCh1 = getRightChBaseList(rightSlipNo);
        int idx= Tools.indexOf(listCh1,(node)->node.getName().equalsIgnoreCase("info"));
        int probeIdx= Tools.indexOf(listCh1,(node)->node.getName().equalsIgnoreCase("probeCal"));
        List<ExternalKeysNode> list;
        if (idx!=-1 && listCh1.get(idx).isVisible()){
            list= listCh1.get(idx).getChildNodes();
        }else if (probeIdx!=-1 && listCh1.get(probeIdx).isVisible()){
            list=listCh1.get(probeIdx).getChildNodes();
        }else {
            return; // 直接返回
        }

        for(int i=0;i<9;i++){
            if (i<count) {
                list.get(i + 3).setVisible(true); // 设置可见
            }else {
                list.get(i+3).setVisible(false); // 设置不可见
            }
        }


    }

    private Consumer<Integer> consumerDialogOpen = new Consumer<Integer>() { // RxBus消息消费者
        @Override
        public void accept(Integer integer) throws Exception {
            int measureCommonPreCount = ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT + ChannelFactory.REF_CNT;
            switch (integer) { // switch分支选择
                case MainViewGroup.DIALOG_MEASUREDELAY:
                    TopDialogMeasureDelay dialogMeasureDelay = (TopDialogMeasureDelay) mainViewGroup.getDialog(MainViewGroup.DIALOG_MEASUREDELAY); // 获取弹窗实例
                    //找到Delay对应的位置：measureCommonPreIndex + 4
                    topList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_MEASURE).getChildNodes()
                            .get(TopLayoutMeasure.DETAIL_COMMON).getChildNodes()
                            .get(measureCommonPreCount + 4).getChildNodes();
                    topList.get(LIST_HEAD).setCurListSelect(dialogMeasureDelay.getExKeysPosition()); // 设置列表选中项
                    topIndex = topList.get(LIST_HEAD).getCurListSelect();
                    if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                            && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                        setViewPlace(topList, topIndex, true); // 更新焦点框位置
                    } else {
                        topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_MEASUREPHASE:
                    TopDialogMeasurePhase dialogMeasurePhase = (TopDialogMeasurePhase) mainViewGroup.getDialog(MainViewGroup.DIALOG_MEASUREPHASE); // 获取弹窗实例
                    //找到Phase对应的位置：measureCommonPreIndex + 12
                    topList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_MEASURE).getChildNodes()
                            .get(TopLayoutMeasure.DETAIL_COMMON).getChildNodes()
                            .get(measureCommonPreCount + 12).getChildNodes();
                    topList.get(LIST_HEAD).setCurListSelect(dialogMeasurePhase.getExKeysPosition()); // 设置列表选中项
                    topIndex = topList.get(LIST_HEAD).getCurListSelect();
                    if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                            && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                        setViewPlace(topList, topIndex, true); // 更新焦点框位置
                    } else {
                        topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_MEASURE_TVALUE:
                    TopDialogMeasureTValue dialogMeasureTValue = (TopDialogMeasureTValue) mainViewGroup.getDialog(MainViewGroup.DIALOG_MEASURE_TVALUE); // 获取弹窗实例
                    //找到Phase对应的位置：measureCommonPreIndex + 12
                    topList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_MEASURE).getChildNodes()
                            .get(TopLayoutMeasure.DETAIL_COMMON).getChildNodes()
                            .get(measureCommonPreCount + 26).getChildNodes();
                    topList.get(LIST_HEAD).setCurListSelect(dialogMeasureTValue.getExKeysPosition()); // 设置列表选中项
                    topIndex = topList.get(LIST_HEAD).getCurListSelect();
                    if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                            && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                        setViewPlace(topList, topIndex, true); // 更新焦点框位置
                    } else {
                        topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_TOPCOUNT:
                    if (topList.get(topIndex).getChildNodes() != null) {
                        topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                        topList = topList.get(topIndex).getChildNodes(); // 进入子菜单节点
                        topIndex = topList.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(topList, topIndex, true); // 更新焦点框位置
                        } else {
                            topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                        }
                    }
//                    topList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes()
//                            .get(TopLayoutTrigger.DETAIL_NEDGE).getChildNodes();
//                    topList = topList.get(topList.size() - 1).getChildNodes();
//                    topIndex = topList.get(LIST_HEAD).getCurListSelect();
//                    if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
//                        setViewPlace(topList, topIndex, true);
//                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_TOPSCALE:
                    //外部处理过了
                    break; // 跳出循环
                case MainViewGroup.DIALOG_TEXTKEYBOARD:
                    if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                            && topIndex < topList.size()
                            && topList.get(topIndex).getChildNodes() != null) {
                        topList = topList.get(topIndex).getChildNodes(); // 进入子菜单节点
                        topIndex = topList.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(topList, topIndex, true); // 更新焦点框位置
                        } else {
                            topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                            && rightCh1List.get(rightCh1Index).getChildNodes() != null) {
                        rightCh1List = rightCh1List.get(rightCh1Index).getChildNodes(); // 进入子菜单节点
                        rightCh1Index = rightCh1List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
                        } else {
                            rightCh1List.get(LIST_HEAD).setCurListSelect(rightCh1Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                            && rightCh2List.get(rightCh2Index).getChildNodes() != null) {
                        rightCh2List = rightCh2List.get(rightCh2Index).getChildNodes(); // 进入子菜单节点
                        rightCh2Index = rightCh2List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
                        } else {
                            rightCh2List.get(LIST_HEAD).setCurListSelect(rightCh2Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                            && rightCh3List.get(rightCh3Index).getChildNodes() != null) {
                        rightCh3List = rightCh3List.get(rightCh3Index).getChildNodes(); // 进入子菜单节点
                        rightCh3Index = rightCh3List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
                        } else {
                            rightCh3List.get(LIST_HEAD).setCurListSelect(rightCh3Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                            && rightCh4List.get(rightCh4Index).getChildNodes() != null) {
                        rightCh4List = rightCh4List.get(rightCh4Index).getChildNodes(); // 进入子菜单节点
                        rightCh4Index = rightCh4List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
                        } else {
                            rightCh4List.get(LIST_HEAD).setCurListSelect(rightCh4Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                            && rightCh5List.get(rightCh5Index).getChildNodes() != null) {
                        rightCh5List = rightCh5List.get(rightCh5Index).getChildNodes(); // 进入子菜单节点
                        rightCh5Index = rightCh5List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
                        } else {
                            rightCh5List.get(LIST_HEAD).setCurListSelect(rightCh5Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                            && rightCh6List.get(rightCh6Index).getChildNodes() != null) {
                        rightCh6List = rightCh6List.get(rightCh6Index).getChildNodes(); // 进入子菜单节点
                        rightCh6Index = rightCh6List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
                        } else {
                            rightCh6List.get(LIST_HEAD).setCurListSelect(rightCh6Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                            && rightCh7List.get(rightCh7Index).getChildNodes() != null) {
                        rightCh7List = rightCh7List.get(rightCh7Index).getChildNodes(); // 进入子菜单节点
                        rightCh7Index = rightCh7List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
                        } else {
                            rightCh7List.get(LIST_HEAD).setCurListSelect(rightCh7Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                            && rightCh8List.get(rightCh8Index).getChildNodes() != null) {
                        rightCh8List = rightCh8List.get(rightCh8Index).getChildNodes(); // 进入子菜单节点
                        rightCh8Index = rightCh8List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
                        } else {
                            rightCh8List.get(LIST_HEAD).setCurListSelect(rightCh8Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1) // 判断Math1菜单是否显示
                            && rightMath1List.get(rightMath1Index).getChildNodes() != null) {
                        rightMath1List = rightMath1List.get(rightMath1Index).getChildNodes(); // 进入子菜单节点
                        rightMath1Index = rightMath1List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1) // 判断Math1菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath1List, rightMath1Index, true); // 更新焦点框位置
                        } else {
                            rightMath1List.get(LIST_HEAD).setCurListSelect(rightMath1Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2) // 判断Math2菜单是否显示
                            && rightMath2List.get(rightMath2Index).getChildNodes() != null) {
                        rightMath2List = rightMath2List.get(rightMath2Index).getChildNodes(); // 进入子菜单节点
                        rightMath2Index = rightMath2List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2) // 判断Math2菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath2List, rightMath2Index, true); // 更新焦点框位置
                        } else {
                            rightMath2List.get(LIST_HEAD).setCurListSelect(rightMath2Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3) // 判断Math3菜单是否显示
                            && rightMath3List.get(rightMath3Index).getChildNodes() != null) {
                        rightMath3List = rightMath3List.get(rightMath3Index).getChildNodes(); // 进入子菜单节点
                        rightMath3Index = rightMath3List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3) // 判断Math3菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath3List, rightMath3Index, true); // 更新焦点框位置
                        } else {
                            rightMath3List.get(LIST_HEAD).setCurListSelect(rightMath3Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4) // 判断Math4菜单是否显示
                            && rightMath4List.get(rightMath4Index).getChildNodes() != null) {
                        rightMath4List = rightMath4List.get(rightMath4Index).getChildNodes(); // 进入子菜单节点
                        rightMath4Index = rightMath4List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4) // 判断Math4菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath4List, rightMath4Index, true); // 更新焦点框位置
                        } else {
                            rightMath4List.get(LIST_HEAD).setCurListSelect(rightMath4Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5) // 判断Math5菜单是否显示
                            && rightMath5List.get(rightMath5Index).getChildNodes() != null) {
                        rightMath5List = rightMath5List.get(rightMath5Index).getChildNodes(); // 进入子菜单节点
                        rightMath5Index = rightMath5List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5) // 判断Math5菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath5List, rightMath5Index, true); // 更新焦点框位置
                        } else {
                            rightMath5List.get(LIST_HEAD).setCurListSelect(rightMath5Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6) // 判断Math6菜单是否显示
                            && rightMath6List.get(rightMath6Index).getChildNodes() != null) {
                        rightMath6List = rightMath6List.get(rightMath6Index).getChildNodes(); // 进入子菜单节点
                        rightMath6Index = rightMath6List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6) // 判断Math6菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath6List, rightMath6Index, true); // 更新焦点框位置
                        } else {
                            rightMath6List.get(LIST_HEAD).setCurListSelect(rightMath6Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7) // 判断Math7菜单是否显示
                            && rightMath7List.get(rightMath7Index).getChildNodes() != null) {
                        rightMath7List = rightMath7List.get(rightMath7Index).getChildNodes(); // 进入子菜单节点
                        rightMath7Index = rightMath7List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7) // 判断Math7菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath7List, rightMath7Index, true); // 更新焦点框位置
                        } else {
                            rightMath7List.get(LIST_HEAD).setCurListSelect(rightMath7Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8) // 判断Math8菜单是否显示
                            && rightMath8List.get(rightMath8Index).getChildNodes() != null) {
                        rightMath8List = rightMath8List.get(rightMath8Index).getChildNodes(); // 进入子菜单节点
                        rightMath8Index = rightMath8List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8) // 判断Math8菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath8List, rightMath8Index, true); // 更新焦点框位置
                        } else {
                            rightMath8List.get(LIST_HEAD).setCurListSelect(rightMath8Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 判断Ref1菜单是否显示
                            && rightRef1List.get(rightRef1Index).getChildNodes() != null) {
                        rightRef1List = rightRef1List.get(rightRef1Index).getChildNodes(); // 进入子菜单节点
                        rightRef1Index = rightRef1List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 判断Ref1菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef1List, rightRef1Index, true); // 更新焦点框位置
                        } else {
                            rightRef1List.get(LIST_HEAD).setCurListSelect(rightRef1Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 判断Ref2菜单是否显示
                            && rightRef2List.get(rightRef2Index).getChildNodes() != null) {
                        rightRef2List = rightRef2List.get(rightRef2Index).getChildNodes(); // 进入子菜单节点
                        rightRef2Index = rightRef2List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 判断Ref2菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef2List, rightRef2Index, true); // 更新焦点框位置
                        } else {
                            rightRef2List.get(LIST_HEAD).setCurListSelect(rightRef2Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 判断Ref3菜单是否显示
                            && rightRef3List.get(rightRef3Index).getChildNodes() != null) {
                        rightRef3List = rightRef3List.get(rightRef3Index).getChildNodes(); // 进入子菜单节点
                        rightRef3Index = rightRef3List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 判断Ref3菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef3List, rightRef3Index, true); // 更新焦点框位置
                        } else {
                            rightRef3List.get(LIST_HEAD).setCurListSelect(rightRef3Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 判断Ref4菜单是否显示
                            && rightRef4List.get(rightRef4Index).getChildNodes() != null) {
                        rightRef4List = rightRef4List.get(rightRef4Index).getChildNodes(); // 进入子菜单节点
                        rightRef4Index = rightRef4List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 判断Ref4菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef4List, rightRef4Index, true); // 更新焦点框位置
                        } else {
                            rightRef4List.get(LIST_HEAD).setCurListSelect(rightRef4Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 判断Ref5菜单是否显示
                            && rightRef5List.get(rightRef5Index).getChildNodes() != null) {
                        rightRef5List = rightRef5List.get(rightRef5Index).getChildNodes(); // 进入子菜单节点
                        rightRef5Index = rightRef5List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 判断Ref5菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef5List, rightRef5Index, true); // 更新焦点框位置
                        } else {
                            rightRef5List.get(LIST_HEAD).setCurListSelect(rightRef5Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 判断Ref6菜单是否显示
                            && rightRef6List.get(rightRef6Index).getChildNodes() != null) {
                        rightRef6List = rightRef6List.get(rightRef6Index).getChildNodes(); // 进入子菜单节点
                        rightRef6Index = rightRef6List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 判断Ref6菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef6List, rightRef6Index, true); // 更新焦点框位置
                        } else {
                            rightRef6List.get(LIST_HEAD).setCurListSelect(rightRef6Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 判断Ref7菜单是否显示
                            && rightRef7List.get(rightRef7Index).getChildNodes() != null) {
                        rightRef7List = rightRef7List.get(rightRef7Index).getChildNodes(); // 进入子菜单节点
                        rightRef7Index = rightRef7List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 判断Ref7菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef7List, rightRef7Index, true); // 更新焦点框位置
                        } else {
                            rightRef7List.get(LIST_HEAD).setCurListSelect(rightRef7Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 判断Ref8菜单是否显示
                            && rightRef8List.get(rightRef8Index).getChildNodes() != null) {
                        rightRef8List = rightRef8List.get(rightRef8Index).getChildNodes(); // 进入子菜单节点
                        rightRef8Index = rightRef8List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 判断Ref8菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef8List, rightRef8Index, true); // 更新焦点框位置
                        } else {
                            rightRef8List.get(LIST_HEAD).setCurListSelect(rightRef8Index); // 设置列表选中项
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_NUMBERKEYBOARD:
                    if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CENTER_SEGMENTED_VISIBLE) // 从缓存读取布尔值
                            && segmentedList.get(segmentedIndex).getChildNodes() != null){
                        segmentedList = segmentedList.get(segmentedIndex).getChildNodes(); // 进入子菜单节点
                        if(segmentedList.get(LIST_HEAD).getChildNodes() != null){
                            //可能有两层
                            segmentedList = segmentedList.get(LIST_HEAD).getChildNodes(); // 获取子节点列表
                        }
                        segmentedIndex = segmentedList.get(LIST_HEAD).getCurListSelect();
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(segmentedList, segmentedIndex, true); // 更新焦点框位置
                        } else {
                            segmentedList.get(LIST_HEAD).setCurListSelect(segmentedIndex); // 设置列表选中项
                        }
                    }else if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) && topIndex < topList.size()) { // 判断顶部菜单是否显示
                        if (topList.get(topIndex).getChildNodes() != null) {
                            topList = topList.get(topIndex).getChildNodes(); // 进入子菜单节点
                            topIndex = topList.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(topList, topIndex, true); // 更新焦点框位置
                            } else {
                                topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1)) { // 判断S1串行总线菜单是否显示
                        if (rightS1List.get(rightS1Index).getChildNodes() != null) {
                            rightS1List = rightS1List.get(rightS1Index).getChildNodes(); // 进入子菜单节点
                            rightS1Index = rightS1List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1) // 判断S1串行总线菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightS1List, rightS1Index, true); // 更新焦点框位置
                            } else {
                                rightS1List.get(LIST_HEAD).setCurListSelect(rightS1Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2)) { // 判断S2串行总线菜单是否显示
                        if (rightS2List.get(rightS2Index).getChildNodes() != null) {
                            rightS2List = rightS2List.get(rightS2Index).getChildNodes(); // 进入子菜单节点
                            rightS2Index = rightS2List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2) // 判断S2串行总线菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightS2List, rightS2Index, true); // 更新焦点框位置
                            } else {
                                rightS2List.get(LIST_HEAD).setCurListSelect(rightS2Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3)) { // 判断S3串行总线菜单是否显示
                        if (rightS3List.get(rightS3Index).getChildNodes() != null) {
                            rightS3List = rightS3List.get(rightS3Index).getChildNodes(); // 进入子菜单节点
                            rightS3Index = rightS3List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3) // 判断S3串行总线菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightS3List, rightS3Index, true); // 更新焦点框位置
                            } else {
                                rightS3List.get(LIST_HEAD).setCurListSelect(rightS3Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4)) { // 判断S4串行总线菜单是否显示
                        if (rightS4List.get(rightS4Index).getChildNodes() != null) {
                            rightS4List = rightS4List.get(rightS4Index).getChildNodes(); // 进入子菜单节点
                            rightS4Index = rightS4List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4) // 判断S4串行总线菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightS4List, rightS4Index, true); // 更新焦点框位置
                            } else {
                                rightS4List.get(LIST_HEAD).setCurListSelect(rightS4Index); // 设置列表选中项
                            }
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_MATH_FFT_PERSIST: {
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1)) { // 判断Math1菜单是否显示
                        if (rightMath1List.get(rightMath1Index).getChildNodes() != null) {
                            rightMath1List = rightMath1List.get(rightMath1Index).getChildNodes(); // 进入子菜单节点
                            rightMath1Index = rightMath1List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1) // 判断Math1菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath1List, rightMath1Index, true); // 更新焦点框位置
                            } else {
                                rightMath1List.get(LIST_HEAD).setCurListSelect(rightMath1Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2)) { // 判断Math2菜单是否显示
                        if (rightMath2List.get(rightMath2Index).getChildNodes() != null) {
                            rightMath2List = rightMath2List.get(rightMath2Index).getChildNodes(); // 进入子菜单节点
                            rightMath2Index = rightMath2List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2) // 判断Math2菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath2List, rightMath2Index, true); // 更新焦点框位置
                            } else {
                                rightMath2List.get(LIST_HEAD).setCurListSelect(rightMath2Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3)) { // 判断Math3菜单是否显示
                        if (rightMath3List.get(rightMath3Index).getChildNodes() != null) {
                            rightMath3List = rightMath3List.get(rightMath3Index).getChildNodes(); // 进入子菜单节点
                            rightMath3Index = rightMath3List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3) // 判断Math3菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath3List, rightMath3Index, true); // 更新焦点框位置
                            } else {
                                rightMath3List.get(LIST_HEAD).setCurListSelect(rightMath3Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4)) { // 判断Math4菜单是否显示
                        if (rightMath4List.get(rightMath4Index).getChildNodes() != null) {
                            rightMath4List = rightMath4List.get(rightMath4Index).getChildNodes(); // 进入子菜单节点
                            rightMath4Index = rightMath4List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4) // 判断Math4菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath4List, rightMath4Index, true); // 更新焦点框位置
                            } else {
                                rightMath4List.get(LIST_HEAD).setCurListSelect(rightMath4Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5)) { // 判断Math5菜单是否显示
                        if (rightMath5List.get(rightMath5Index).getChildNodes() != null) {
                            rightMath5List = rightMath5List.get(rightMath5Index).getChildNodes(); // 进入子菜单节点
                            rightMath5Index = rightMath5List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5) // 判断Math5菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath5List, rightMath5Index, true); // 更新焦点框位置
                            } else {
                                rightMath5List.get(LIST_HEAD).setCurListSelect(rightMath5Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6)) { // 判断Math6菜单是否显示
                        if (rightMath6List.get(rightMath6Index).getChildNodes() != null) {
                            rightMath6List = rightMath6List.get(rightMath6Index).getChildNodes(); // 进入子菜单节点
                            rightMath6Index = rightMath6List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6) // 判断Math6菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath6List, rightMath6Index, true); // 更新焦点框位置
                            } else {
                                rightMath6List.get(LIST_HEAD).setCurListSelect(rightMath6Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7)) { // 判断Math7菜单是否显示
                        if (rightMath7List.get(rightMath7Index).getChildNodes() != null) {
                            rightMath7List = rightMath7List.get(rightMath7Index).getChildNodes(); // 进入子菜单节点
                            rightMath7Index = rightMath7List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7) // 判断Math7菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath7List, rightMath7Index, true); // 更新焦点框位置
                            } else {
                                rightMath7List.get(LIST_HEAD).setCurListSelect(rightMath7Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8)) { // 判断Math8菜单是否显示
                        if (rightMath8List.get(rightMath8Index).getChildNodes() != null) {
                            rightMath8List = rightMath8List.get(rightMath8Index).getChildNodes(); // 进入子菜单节点
                            rightMath8Index = rightMath8List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8) // 判断Math8菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath8List, rightMath8Index, true); // 更新焦点框位置
                            } else {
                                rightMath8List.get(LIST_HEAD).setCurListSelect(rightMath8Index); // 设置列表选中项
                            }
                        }
                    }
                }break;
                case MainViewGroup.DIALOG_SELECT_COLOR:
                case MainViewGroup.DIALOG_FLOATKEYBOARD:
                case MainViewGroup.DIALOG_FORMULAKEYBOARD:
                case MainViewGroup.DIALOG_NUMBERPICKER:
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1) // 判断Math1菜单是否显示
                            && rightMath1List.size() > rightMath1Index
                            && rightMath1List.get(rightMath1Index).getChildNodes() != null
                    ) {
                        rightMath1List = rightMath1List.get(rightMath1Index).getChildNodes(); // 进入子菜单节点
                        rightMath1Index = rightMath1List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1) // 判断Math1菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath1List, rightMath1Index, true); // 更新焦点框位置
                        } else {
                            rightMath1List.get(LIST_HEAD).setCurListSelect(rightMath1Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2) // 判断Math2菜单是否显示
                            && rightMath2List.size() > rightMath2Index
                            && rightMath2List.get(rightMath2Index).getChildNodes() != null
                    ) {
                        rightMath2List = rightMath2List.get(rightMath2Index).getChildNodes(); // 进入子菜单节点
                        rightMath2Index = rightMath2List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2) // 判断Math2菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath2List, rightMath2Index, true); // 更新焦点框位置
                        } else {
                            rightMath2List.get(LIST_HEAD).setCurListSelect(rightMath2Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3) // 判断Math3菜单是否显示
                            && rightMath3List.size() > rightMath3Index
                            && rightMath3List.get(rightMath3Index).getChildNodes() != null
                    ) {
                        rightMath3List = rightMath3List.get(rightMath3Index).getChildNodes(); // 进入子菜单节点
                        rightMath3Index = rightMath3List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3) // 判断Math3菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath3List, rightMath3Index, true); // 更新焦点框位置
                        } else {
                            rightMath3List.get(LIST_HEAD).setCurListSelect(rightMath3Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4) // 判断Math4菜单是否显示
                            && rightMath4List.size() > rightMath4Index
                            && rightMath4List.get(rightMath4Index).getChildNodes() != null
                    ) {
                        rightMath4List = rightMath4List.get(rightMath4Index).getChildNodes(); // 进入子菜单节点
                        rightMath4Index = rightMath4List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4) // 判断Math4菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath4List, rightMath4Index, true); // 更新焦点框位置
                        } else {
                            rightMath4List.get(LIST_HEAD).setCurListSelect(rightMath4Index); // 设置列表选中项
                        }

                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5) // 判断Math5菜单是否显示
                            && rightMath5List.size() > rightMath5Index
                            && rightMath5List.get(rightMath5Index).getChildNodes() != null
                    ) {
                        rightMath5List = rightMath5List.get(rightMath5Index).getChildNodes(); // 进入子菜单节点
                        rightMath5Index = rightMath5List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5) // 判断Math5菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath5List, rightMath5Index, true); // 更新焦点框位置
                        } else {
                            rightMath5List.get(LIST_HEAD).setCurListSelect(rightMath5Index); // 设置列表选中项
                        }

                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6) // 判断Math6菜单是否显示
                            && rightMath6List.size() > rightMath6Index
                            && rightMath6List.get(rightMath6Index).getChildNodes() != null
                    ) {
                        rightMath6List = rightMath6List.get(rightMath6Index).getChildNodes(); // 进入子菜单节点
                        rightMath6Index = rightMath6List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6) // 判断Math6菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath6List, rightMath6Index, true); // 更新焦点框位置
                        } else {
                            rightMath6List.get(LIST_HEAD).setCurListSelect(rightMath6Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7) // 判断Math7菜单是否显示
                            && rightMath7List.size() > rightMath7Index
                            && rightMath7List.get(rightMath7Index).getChildNodes() != null

                    ) {
                        rightMath7List = rightMath7List.get(rightMath7Index).getChildNodes(); // 进入子菜单节点
                        rightMath7Index = rightMath7List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7) // 判断Math7菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath7List, rightMath7Index, true); // 更新焦点框位置
                        } else {
                            rightMath7List.get(LIST_HEAD).setCurListSelect(rightMath7Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8) // 判断Math8菜单是否显示
                            && rightMath8List.size() > rightMath8Index
                            && rightMath8List.get(rightMath8Index).getChildNodes() != null
                    ) {
                        rightMath8List = rightMath8List.get(rightMath8Index).getChildNodes(); // 进入子菜单节点
                        rightMath8Index = rightMath8List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8) // 判断Math8菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath8List, rightMath8Index, true); // 更新焦点框位置
                        } else {
                            rightMath8List.get(LIST_HEAD).setCurListSelect(rightMath8Index); // 设置列表选中项
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1)) { // 判断CH1通道菜单是否显示
                        if (rightCh1List.get(rightCh1Index).getChildNodes() != null) {
                            rightCh1List = rightCh1List.get(rightCh1Index).getChildNodes(); // 进入子菜单节点
                            rightCh1Index = rightCh1List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
                            } else {
                                rightCh1List.get(LIST_HEAD).setCurListSelect(rightCh1Index); // 设置列表选中项
                            }
                        }
                    }else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2)){ // 判断CH2通道菜单是否显示
                        if (rightCh2List.get(rightCh2Index).getChildNodes() != null) {
                            rightCh2List = rightCh2List.get(rightCh2Index).getChildNodes(); // 进入子菜单节点
                            rightCh2Index = rightCh2List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
                            } else {
                                rightCh2List.get(LIST_HEAD).setCurListSelect(rightCh2Index); // 设置列表选中项
                            }
                        }
                    }else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3)){ // 判断CH3通道菜单是否显示
                        if (rightCh3List.get(rightCh3Index).getChildNodes() != null) {
                            rightCh3List = rightCh3List.get(rightCh3Index).getChildNodes(); // 进入子菜单节点
                            rightCh3Index = rightCh3List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
                            } else {
                                rightCh3List.get(LIST_HEAD).setCurListSelect(rightCh3Index); // 设置列表选中项
                            }
                        }
                    }else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4)){ // 判断CH4通道菜单是否显示
                        if (rightCh4List.get(rightCh4Index).getChildNodes() != null) {
                            rightCh4List = rightCh4List.get(rightCh4Index).getChildNodes(); // 进入子菜单节点
                            rightCh4Index = rightCh4List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
                            } else {
                                rightCh4List.get(LIST_HEAD).setCurListSelect(rightCh4Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5)) { // 判断CH5通道菜单是否显示
                        if (rightCh5List.get(rightCh5Index).getChildNodes() != null) {
                            rightCh5List = rightCh5List.get(rightCh5Index).getChildNodes(); // 进入子菜单节点
                            rightCh5Index = rightCh5List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
                            } else {
                                rightCh5List.get(LIST_HEAD).setCurListSelect(rightCh5Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6)) { // 判断CH6通道菜单是否显示
                        if (rightCh6List.get(rightCh6Index).getChildNodes() != null) {
                            rightCh6List = rightCh6List.get(rightCh6Index).getChildNodes(); // 进入子菜单节点
                            rightCh6Index = rightCh6List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
                            } else {
                                rightCh6List.get(LIST_HEAD).setCurListSelect(rightCh6Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7)) { // 判断CH7通道菜单是否显示
                        if (rightCh7List.get(rightCh7Index).getChildNodes() != null) {
                            rightCh7List = rightCh7List.get(rightCh7Index).getChildNodes(); // 进入子菜单节点
                            rightCh7Index = rightCh7List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
                            } else {
                                rightCh7List.get(LIST_HEAD).setCurListSelect(rightCh7Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8)) { // 判断CH8通道菜单是否显示
                        if (rightCh8List.get(rightCh8Index).getChildNodes() != null) {
                            rightCh8List = rightCh8List.get(rightCh8Index).getChildNodes(); // 进入子菜单节点
                            rightCh8Index = rightCh8List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
                            } else {
                                rightCh8List.get(LIST_HEAD).setCurListSelect(rightCh8Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)){ // 判断顶部菜单是否显示
                        if (topList.get(topIndex).getChildNodes() != null) {
                            topList = topList.get(topIndex).getChildNodes(); // 进入子菜单节点
                            topIndex = topList.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(topList, topIndex, true); // 更新焦点框位置
                            } else {
                                topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1)) { // 判断Ref1菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef1List.get(rightRef1Index).getDialog())) {
                            rightRef1List = rightRef1List.get(rightRef1Index).getChildNodes(); // 进入子菜单节点
                            rightRef1Index = rightRef1List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 判断Ref1菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef1List, rightRef1Index, true); // 更新焦点框位置
                            } else {
                                rightRef1List.get(LIST_HEAD).setCurListSelect(rightRef1Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2)) { // 判断Ref2菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef2List.get(rightRef2Index).getDialog())) {
                            rightRef2List = rightRef2List.get(rightRef2Index).getChildNodes(); // 进入子菜单节点
                            rightRef2Index = rightRef2List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 判断Ref2菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef2List, rightRef2Index, true); // 更新焦点框位置
                            } else {
                                rightRef2List.get(LIST_HEAD).setCurListSelect(rightRef2Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3)) { // 判断Ref3菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef3List.get(rightRef3Index).getDialog())) {
                            rightRef3List = rightRef3List.get(rightRef3Index).getChildNodes(); // 进入子菜单节点
                            rightRef3Index = rightRef3List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 判断Ref3菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef3List, rightRef3Index, true); // 更新焦点框位置
                            } else {
                                rightRef3List.get(LIST_HEAD).setCurListSelect(rightRef3Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4)) { // 判断Ref4菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef4List.get(rightRef4Index).getDialog())) {
                            rightRef4List = rightRef4List.get(rightRef4Index).getChildNodes(); // 进入子菜单节点
                            rightRef4Index = rightRef4List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 判断Ref4菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef4List, rightRef4Index, true); // 更新焦点框位置
                            } else {
                                rightRef4List.get(LIST_HEAD).setCurListSelect(rightRef4Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5)) { // 判断Ref5菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef5List.get(rightRef5Index).getDialog())) {
                            rightRef5List = rightRef5List.get(rightRef5Index).getChildNodes(); // 进入子菜单节点
                            rightRef5Index = rightRef5List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 判断Ref5菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef5List, rightRef5Index, true); // 更新焦点框位置
                            } else {
                                rightRef5List.get(LIST_HEAD).setCurListSelect(rightRef5Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6)) { // 判断Ref6菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef6List.get(rightRef6Index).getDialog())) {
                            rightRef6List = rightRef6List.get(rightRef6Index).getChildNodes(); // 进入子菜单节点
                            rightRef6Index = rightRef6List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 判断Ref6菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef6List, rightRef6Index, true); // 更新焦点框位置
                            } else {
                                rightRef6List.get(LIST_HEAD).setCurListSelect(rightRef6Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7)) { // 判断Ref7菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef7List.get(rightRef7Index).getDialog())) {
                            rightRef7List = rightRef7List.get(rightRef7Index).getChildNodes(); // 进入子菜单节点
                            rightRef7Index = rightRef7List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 判断Ref7菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef7List, rightRef7Index, true); // 更新焦点框位置
                            } else {
                                rightRef7List.get(LIST_HEAD).setCurListSelect(rightRef7Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8)) { // 判断Ref8菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef8List.get(rightRef8Index).getDialog())) {
                            rightRef8List = rightRef8List.get(rightRef8Index).getChildNodes(); // 进入子菜单节点
                            rightRef8Index = rightRef8List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 判断Ref8菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef8List, rightRef8Index, true); // 更新焦点框位置
                            } else {
                                rightRef8List.get(LIST_HEAD).setCurListSelect(rightRef8Index); // 设置列表选中项
                            }
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_BAUDRATE:
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1)) { // 判断S1串行总线菜单是否显示
                        if (ExternalKeysNode.DIALOG_BAUDRATE.equals(rightS1List.get(rightS1Index).getDialog())) {
                            //确保进来的时候的当前节点是波特率的点击框
                            rightS1List = rightS1List.get(rightS1Index).getChildNodes(); // 进入子菜单节点
                            rightS1Index = rightS1List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1) // 判断S1串行总线菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightS1List, rightS1Index, true); // 更新焦点框位置
                            } else {
                                rightS1List.get(LIST_HEAD).setCurListSelect(rightS1Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2)) { // 判断S2串行总线菜单是否显示
                        if (ExternalKeysNode.DIALOG_BAUDRATE.equals(rightS2List.get(rightS2Index).getDialog())) {
                            //确保进来的时候的当前节点是波特率的点击框
                            rightS2List = rightS2List.get(rightS2Index).getChildNodes(); // 进入子菜单节点
                            rightS2Index = rightS2List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2) // 判断S2串行总线菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightS2List, rightS2Index, true); // 更新焦点框位置
                            } else {
                                rightS2List.get(LIST_HEAD).setCurListSelect(rightS2Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3)) { // 判断S3串行总线菜单是否显示
                        if (ExternalKeysNode.DIALOG_BAUDRATE.equals(rightS3List.get(rightS3Index).getDialog())) {
                            //确保进来的时候的当前节点是波特率的点击框
                            rightS3List = rightS3List.get(rightS3Index).getChildNodes(); // 进入子菜单节点
                            rightS3Index = rightS3List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3) // 判断S3串行总线菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightS3List, rightS3Index, true); // 更新焦点框位置
                            } else {
                                rightS3List.get(LIST_HEAD).setCurListSelect(rightS3Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4)) { // 判断S4串行总线菜单是否显示
                        if (ExternalKeysNode.DIALOG_BAUDRATE.equals(rightS4List.get(rightS4Index).getDialog())) {
                            //确保进来的时候的当前节点是波特率的点击框
                            rightS4List = rightS4List.get(rightS4Index).getChildNodes(); // 进入子菜单节点
                            rightS4Index = rightS4List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4) // 判断S4串行总线菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightS4List, rightS4Index, true); // 更新焦点框位置
                            } else {
                                rightS4List.get(LIST_HEAD).setCurListSelect(rightS4Index); // 设置列表选中项
                            }
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_BANDWIDTH:
                case MainViewGroup.DIALOG_PROBEMULTIPLE:
                case MainViewGroup.DIALOG_PROBE_INTERFACE:
                    if (MainViewGroup.DIALOG_PROBE_INTERFACE==integer){

                        updateDialogProbeInterface(MainViewGroup.RIGHTSLIP_CH1);
                        updateDialogProbeInterface(MainViewGroup.RIGHTSLIP_CH2);
                        updateDialogProbeInterface(MainViewGroup.RIGHTSLIP_CH3);
                        updateDialogProbeInterface(MainViewGroup.RIGHTSLIP_CH4);
                        updateDialogProbeInterface(MainViewGroup.RIGHTSLIP_CH5);
                        updateDialogProbeInterface(MainViewGroup.RIGHTSLIP_CH6);
                        updateDialogProbeInterface(MainViewGroup.RIGHTSLIP_CH7);
                        updateDialogProbeInterface(MainViewGroup.RIGHTSLIP_CH8);
//                        setDialogProbeInterfaceBandWidthCount(MainViewGroup.RIGHTSLIP_CH1);
//                        setDialogProbeInterfaceBandWidthCount(MainViewGroup.RIGHTSLIP_CH2);
//                        setDialogProbeInterfaceBandWidthCount(MainViewGroup.RIGHTSLIP_CH3);
//                        setDialogProbeInterfaceBandWidthCount(MainViewGroup.RIGHTSLIP_CH4);
                    }

                    //Log.d("Tag.Debug", String.format("ExternalKeysManager.accept: %d,%s",rightCh1Index,rightCh1List ));
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1)) { // 判断CH1通道菜单是否显示
                        if (rightCh1List.get(rightCh1Index).getChildNodes() != null) {
                            rightCh1List = rightCh1List.get(rightCh1Index).getChildNodes(); // 进入子菜单节点
                            rightCh1Index = rightCh1List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
                            } else {
                                rightCh1List.get(LIST_HEAD).setCurListSelect(rightCh1Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2)) { // 判断CH2通道菜单是否显示
                        if (rightCh2List.get(rightCh2Index).getChildNodes() != null) {
                            rightCh2List = rightCh2List.get(rightCh2Index).getChildNodes(); // 进入子菜单节点
                            rightCh2Index = rightCh2List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
                            } else {
                                rightCh2List.get(LIST_HEAD).setCurListSelect(rightCh2Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3)) { // 判断CH3通道菜单是否显示
                        if (rightCh3List.get(rightCh3Index).getChildNodes() != null) {
                            rightCh3List = rightCh3List.get(rightCh3Index).getChildNodes(); // 进入子菜单节点
                            rightCh3Index = rightCh3List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
                            } else {
                                rightCh3List.get(LIST_HEAD).setCurListSelect(rightCh3Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4)) { // 判断CH4通道菜单是否显示
                        if (rightCh4List.get(rightCh4Index).getChildNodes() != null) {
                            rightCh4List = rightCh4List.get(rightCh4Index).getChildNodes(); // 进入子菜单节点
                            rightCh4Index = rightCh4List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
                            } else {
                                rightCh4List.get(LIST_HEAD).setCurListSelect(rightCh4Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5)) { // 判断CH5通道菜单是否显示
                        if (rightCh5List.get(rightCh5Index).getChildNodes() != null) {
                            rightCh5List = rightCh5List.get(rightCh5Index).getChildNodes(); // 进入子菜单节点
                            rightCh5Index = rightCh5List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
                            } else {
                                rightCh5List.get(LIST_HEAD).setCurListSelect(rightCh5Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6)) { // 判断CH6通道菜单是否显示
                        if (rightCh6List.get(rightCh6Index).getChildNodes() != null) {
                            rightCh6List = rightCh6List.get(rightCh6Index).getChildNodes(); // 进入子菜单节点
                            rightCh6Index = rightCh6List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
                            } else {
                                rightCh6List.get(LIST_HEAD).setCurListSelect(rightCh6Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7)) { // 判断CH7通道菜单是否显示
                        if (rightCh7List.get(rightCh7Index).getChildNodes() != null) {
                            rightCh7List = rightCh7List.get(rightCh7Index).getChildNodes(); // 进入子菜单节点
                            rightCh7Index = rightCh7List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
                            } else {
                                rightCh7List.get(LIST_HEAD).setCurListSelect(rightCh7Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8)) { // 判断CH8通道菜单是否显示
                        if (rightCh8List.get(rightCh8Index).getChildNodes() != null) {
                            rightCh8List = rightCh8List.get(rightCh8Index).getChildNodes(); // 进入子菜单节点
                            rightCh8Index = rightCh8List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
                            } else {
                                rightCh8List.get(LIST_HEAD).setCurListSelect(rightCh8Index); // 设置列表选中项
                            }
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_CHANNELLABEL:
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1)) { // 判断CH1通道菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightCh1List.get(rightCh1Index).getDialog())) {
                            rightCh1List = rightCh1List.get(rightCh1Index).getChildNodes(); // 进入子菜单节点
                            rightCh1Index = rightCh1List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
                            } else {
                                rightCh1List.get(LIST_HEAD).setCurListSelect(rightCh1Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2)) { // 判断CH2通道菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightCh2List.get(rightCh2Index).getDialog())) {
                            rightCh2List = rightCh2List.get(rightCh2Index).getChildNodes(); // 进入子菜单节点
                            rightCh2Index = rightCh2List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
                            } else {
                                rightCh2List.get(LIST_HEAD).setCurListSelect(rightCh2Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3)) { // 判断CH3通道菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightCh3List.get(rightCh3Index).getDialog())) {
                            rightCh3List = rightCh3List.get(rightCh3Index).getChildNodes(); // 进入子菜单节点
                            rightCh3Index = rightCh3List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
                            } else {
                                rightCh3List.get(LIST_HEAD).setCurListSelect(rightCh3Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4)) { // 判断CH4通道菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightCh4List.get(rightCh4Index).getDialog())) {
                            rightCh4List = rightCh4List.get(rightCh4Index).getChildNodes(); // 进入子菜单节点
                            rightCh4Index = rightCh4List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
                            } else {
                                rightCh4List.get(LIST_HEAD).setCurListSelect(rightCh4Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5)) { // 判断CH5通道菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightCh5List.get(rightCh5Index).getDialog())) {
                            rightCh5List = rightCh5List.get(rightCh5Index).getChildNodes(); // 进入子菜单节点
                            rightCh5Index = rightCh5List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
                            } else {
                                rightCh5List.get(LIST_HEAD).setCurListSelect(rightCh5Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6)) { // 判断CH6通道菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightCh6List.get(rightCh6Index).getDialog())) {
                            rightCh6List = rightCh6List.get(rightCh6Index).getChildNodes(); // 进入子菜单节点
                            rightCh6Index = rightCh6List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
                            } else {
                                rightCh6List.get(LIST_HEAD).setCurListSelect(rightCh6Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7)) { // 判断CH7通道菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightCh7List.get(rightCh7Index).getDialog())) {
                            rightCh7List = rightCh7List.get(rightCh7Index).getChildNodes(); // 进入子菜单节点
                            rightCh7Index = rightCh7List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
                            } else {
                                rightCh7List.get(LIST_HEAD).setCurListSelect(rightCh7Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8)) { // 判断CH8通道菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightCh8List.get(rightCh8Index).getDialog())) {
                            rightCh8List = rightCh8List.get(rightCh8Index).getChildNodes(); // 进入子菜单节点
                            rightCh8Index = rightCh8List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
                            } else {
                                rightCh8List.get(LIST_HEAD).setCurListSelect(rightCh8Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1)) { // 判断Math1菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightMath1List.get(rightMath1Index).getDialog())) {
                            rightMath1List = rightMath1List.get(rightMath1Index).getChildNodes(); // 进入子菜单节点
                            rightMath1Index = rightCh8List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1) // 判断Math1菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath1List, rightMath1Index, true); // 更新焦点框位置
                            } else {
                                rightMath1List.get(LIST_HEAD).setCurListSelect(rightMath1Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2)) { // 判断Math2菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightMath2List.get(rightMath2Index).getDialog())) {
                            rightMath2List = rightMath2List.get(rightMath2Index).getChildNodes(); // 进入子菜单节点
                            rightMath2Index = rightMath2List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2) // 判断Math2菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath2List, rightMath2Index, true); // 更新焦点框位置
                            } else {
                                rightMath2List.get(LIST_HEAD).setCurListSelect(rightMath2Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3)) { // 判断Math3菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightMath3List.get(rightMath3Index).getDialog())) {
                            rightMath3List = rightMath3List.get(rightMath3Index).getChildNodes(); // 进入子菜单节点
                            rightMath3Index = rightMath3List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3) // 判断Math3菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath3List, rightMath3Index, true); // 更新焦点框位置
                            } else {
                                rightMath3List.get(LIST_HEAD).setCurListSelect(rightMath3Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4)) { // 判断Math4菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightMath4List.get(rightMath4Index).getDialog())) {
                            rightMath4List = rightMath4List.get(rightMath4Index).getChildNodes(); // 进入子菜单节点
                            rightMath4Index = rightMath4List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4) // 判断Math4菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath4List, rightMath4Index, true); // 更新焦点框位置
                            } else {
                                rightMath4List.get(LIST_HEAD).setCurListSelect(rightMath4Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5)) { // 判断Math5菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightMath5List.get(rightMath5Index).getDialog())) {
                            rightMath5List = rightMath5List.get(rightMath5Index).getChildNodes(); // 进入子菜单节点
                            rightMath5Index = rightMath5List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5) // 判断Math5菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath5List, rightMath5Index, true); // 更新焦点框位置
                            } else {
                                rightMath5List.get(LIST_HEAD).setCurListSelect(rightMath5Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6)) { // 判断Math6菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightMath6List.get(rightMath6Index).getDialog())) {
                            rightMath6List = rightMath6List.get(rightMath6Index).getChildNodes(); // 进入子菜单节点
                            rightMath6Index = rightMath6List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6) // 判断Math6菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath6List, rightMath6Index, true); // 更新焦点框位置
                            } else {
                                rightMath6List.get(LIST_HEAD).setCurListSelect(rightMath6Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7)) { // 判断Math7菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightMath7List.get(rightMath7Index).getDialog())) {
                            rightMath7List = rightMath7List.get(rightMath7Index).getChildNodes(); // 进入子菜单节点
                            rightMath7Index = rightMath7List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7) // 判断Math7菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath7List, rightMath7Index, true); // 更新焦点框位置
                            } else {
                                rightMath7List.get(LIST_HEAD).setCurListSelect(rightMath7Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8)) { // 判断Math8菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightMath8List.get(rightMath8Index).getDialog())) {
                            rightMath8List = rightMath8List.get(rightMath8Index).getChildNodes(); // 进入子菜单节点
                            rightMath8Index = rightMath8List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8) // 判断Math8菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightMath8List, rightMath8Index, true); // 更新焦点框位置
                            } else {
                                rightMath8List.get(LIST_HEAD).setCurListSelect(rightMath8Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1)) { // 判断Ref1菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef1List.get(rightRef1Index).getDialog())) {
                            rightRef1List = rightRef1List.get(rightRef1Index).getChildNodes(); // 进入子菜单节点
                            rightRef1Index = rightRef1List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 判断Ref1菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef1List, rightRef1Index, true); // 更新焦点框位置
                            } else {
                                rightRef1List.get(LIST_HEAD).setCurListSelect(rightRef1Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2)) { // 判断Ref2菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef2List.get(rightRef2Index).getDialog())) {
                            rightRef2List = rightRef2List.get(rightRef2Index).getChildNodes(); // 进入子菜单节点
                            rightRef2Index = rightRef2List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 判断Ref2菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef2List, rightRef2Index, true); // 更新焦点框位置
                            } else {
                                rightRef2List.get(LIST_HEAD).setCurListSelect(rightRef2Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3)) { // 判断Ref3菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef3List.get(rightRef3Index).getDialog())) {
                            rightRef3List = rightRef3List.get(rightRef3Index).getChildNodes(); // 进入子菜单节点
                            rightRef3Index = rightRef3List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 判断Ref3菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef3List, rightRef3Index, true); // 更新焦点框位置
                            } else {
                                rightRef3List.get(LIST_HEAD).setCurListSelect(rightRef3Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4)) { // 判断Ref4菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef4List.get(rightRef4Index).getDialog())) {
                            rightRef4List = rightRef4List.get(rightRef4Index).getChildNodes(); // 进入子菜单节点
                            rightRef4Index = rightRef4List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 判断Ref4菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef4List, rightRef4Index, true); // 更新焦点框位置
                            } else {
                                rightRef4List.get(LIST_HEAD).setCurListSelect(rightRef4Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5)) { // 判断Ref5菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef5List.get(rightRef5Index).getDialog())) {
                            rightRef5List = rightRef5List.get(rightRef5Index).getChildNodes(); // 进入子菜单节点
                            rightRef5Index = rightRef5List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 判断Ref5菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef5List, rightRef5Index, true); // 更新焦点框位置
                            } else {
                                rightRef5List.get(LIST_HEAD).setCurListSelect(rightRef5Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6)) { // 判断Ref6菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef6List.get(rightRef6Index).getDialog())) {
                            rightRef6List = rightRef6List.get(rightRef6Index).getChildNodes(); // 进入子菜单节点
                            rightRef6Index = rightRef6List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 判断Ref6菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef6List, rightRef6Index, true); // 更新焦点框位置
                            } else {
                                rightRef6List.get(LIST_HEAD).setCurListSelect(rightRef6Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7)) { // 判断Ref7菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef7List.get(rightRef7Index).getDialog())) {
                            rightRef7List = rightRef7List.get(rightRef7Index).getChildNodes(); // 进入子菜单节点
                            rightRef7Index = rightRef7List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 判断Ref7菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef7List, rightRef7Index, true); // 更新焦点框位置
                            } else {
                                rightRef7List.get(LIST_HEAD).setCurListSelect(rightRef7Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8)) { // 判断Ref8菜单是否显示
                        if (ExternalKeysNode.DIALOG_CHANNELLABEL.equals(rightRef8List.get(rightRef8Index).getDialog())) {
                            rightRef8List = rightRef8List.get(rightRef8Index).getChildNodes(); // 进入子菜单节点
                            rightRef8Index = rightRef8List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 判断Ref8菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef8List, rightRef8Index, true); // 更新焦点框位置
                            } else {
                                rightRef8List.get(LIST_HEAD).setCurListSelect(rightRef8Index); // 设置列表选中项
                            }
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_LOAF_REF_CSV:
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1)) { // 判断Ref1菜单是否显示
                        ArrayList<Integer> channelInCsv = mainViewGroup.getRightRef(TChan.R1).getChannelInCsv();
                        if (channelInCsv.size() <= 0) return;
                        List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF1);
                        rightRef1List = list.get(6).getChildNodes().get(2).getChildNodes();
                        rightRef1Index = 0; // 初始化索引为0
                        rightRef1List.get(LIST_HEAD).setCurListSelect(rightRef1Index); // 设置列表选中项
                        for (int i = 8; i < rightRef1List.size() - 1; i++) {
                            rightRef1List.get(i).setVisible(false); // 设置不可见
                        }
                        for (int i = 0; i < channelInCsv.size(); i++) {
                            rightRef1List.get(channelInCsv.get(i) + 8).setVisible(true); // 设置可见
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2)) { // 判断Ref2菜单是否显示
                        ArrayList<Integer> channelInCsv = mainViewGroup.getRightRef(TChan.R2).getChannelInCsv();
                        if (channelInCsv.size() <= 0) return;
                        List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF2);
                        rightRef2List = list.get(6).getChildNodes().get(2).getChildNodes();
                        rightRef2Index = 0; // 初始化索引为0
                        rightRef2List.get(LIST_HEAD).setCurListSelect(rightRef2Index); // 设置列表选中项
                        for (int i = 8; i < rightRef2List.size() - 1; i++) {
                            rightRef2List.get(i).setVisible(false); // 设置不可见
                        }
                        for (int i = 0; i < channelInCsv.size(); i++) {
                            rightRef2List.get(channelInCsv.get(i) + 8).setVisible(true); // 设置可见
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3)) { // 判断Ref3菜单是否显示
                        ArrayList<Integer> channelInCsv = mainViewGroup.getRightRef(TChan.R3).getChannelInCsv();
                        if (channelInCsv.size() <= 0) return;
                        List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF3);
                        rightRef3List = list.get(6).getChildNodes().get(2).getChildNodes();
                        rightRef3Index = 0; // 初始化索引为0
                        rightRef3List.get(LIST_HEAD).setCurListSelect(rightRef3Index); // 设置列表选中项
                        for (int i = 8; i < rightRef3List.size() - 1; i++) {
                            rightRef3List.get(i).setVisible(false); // 设置不可见
                        }
                        for (int i = 0; i < channelInCsv.size(); i++) {
                            rightRef3List.get(channelInCsv.get(i) + 8).setVisible(true); // 设置可见
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4)) { // 判断Ref4菜单是否显示
                        ArrayList<Integer> channelInCsv = mainViewGroup.getRightRef(TChan.R4).getChannelInCsv();
                        if (channelInCsv.size() <= 0) return;
                        List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF4);
                        rightRef4List = list.get(6).getChildNodes().get(2).getChildNodes();
                        rightRef4Index = 0; // 初始化索引为0
                        rightRef4List.get(LIST_HEAD).setCurListSelect(rightRef4Index); // 设置列表选中项
                        for (int i = 8; i < rightRef4List.size() - 1; i++) {
                            rightRef4List.get(i).setVisible(false); // 设置不可见
                        }
                        for (int i = 0; i < channelInCsv.size(); i++) {
                            rightRef4List.get(channelInCsv.get(i) + 8).setVisible(true); // 设置可见
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5)) { // 判断Ref5菜单是否显示
                        ArrayList<Integer> channelInCsv = mainViewGroup.getRightRef(TChan.R5).getChannelInCsv();
                        if (channelInCsv.size() <= 0) return;
                        List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF5);
                        rightRef5List = list.get(6).getChildNodes().get(2).getChildNodes();
                        rightRef5Index = 0; // 初始化索引为0
                        rightRef5List.get(LIST_HEAD).setCurListSelect(rightRef5Index); // 设置列表选中项
                        for (int i = 8; i < rightRef5List.size() - 1; i++) {
                            rightRef5List.get(i).setVisible(false); // 设置不可见
                        }
                        for (int i = 0; i < channelInCsv.size(); i++) {
                            rightRef5List.get(channelInCsv.get(i) + 8).setVisible(true); // 设置可见
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6)) { // 判断Ref6菜单是否显示
                        ArrayList<Integer> channelInCsv = mainViewGroup.getRightRef(TChan.R6).getChannelInCsv();
                        if (channelInCsv.size() <= 0) return;
                        List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF6);
                        rightRef6List = list.get(6).getChildNodes().get(2).getChildNodes();
                        rightRef6Index = 0; // 初始化索引为0
                        rightRef6List.get(LIST_HEAD).setCurListSelect(rightRef6Index); // 设置列表选中项
                        for (int i = 8; i < rightRef6List.size() - 1; i++) {
                            rightRef6List.get(i).setVisible(false); // 设置不可见
                        }
                        for (int i = 0; i < channelInCsv.size(); i++) {
                            rightRef6List.get(channelInCsv.get(i) + 8).setVisible(true); // 设置可见
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7)) { // 判断Ref7菜单是否显示
                        ArrayList<Integer> channelInCsv = mainViewGroup.getRightRef(TChan.R7).getChannelInCsv();
                        if (channelInCsv.size() <= 0) return;
                        List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF7);
                        rightRef7List = list.get(6).getChildNodes().get(2).getChildNodes();
                        rightRef7Index = 0; // 初始化索引为0
                        rightRef7List.get(LIST_HEAD).setCurListSelect(rightRef7Index); // 设置列表选中项
                        for (int i = 8; i < rightRef7List.size() - 1; i++) {
                            rightRef7List.get(i).setVisible(false); // 设置不可见
                        }
                        for (int i = 0; i < channelInCsv.size(); i++) {
                            rightRef7List.get(channelInCsv.get(i) + 8).setVisible(true); // 设置可见
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8)) { // 判断Ref8菜单是否显示
                        ArrayList<Integer> channelInCsv = mainViewGroup.getRightRef(TChan.R8).getChannelInCsv();
                        if (channelInCsv.size() <= 0) return;
                        List<ExternalKeysNode> list = getRightRefBaseList(MainViewGroup.RIGHTSLIP_REF8);
                        rightRef8List = list.get(6).getChildNodes().get(2).getChildNodes();
                        rightRef8Index = 0; // 初始化索引为0
                        rightRef8List.get(LIST_HEAD).setCurListSelect(rightRef8Index); // 设置列表选中项
                        for (int i = 8; i < rightRef8List.size() - 1; i++) {
                            rightRef8List.get(i).setVisible(false); // 设置不可见
                        }
                        for (int i = 0; i < channelInCsv.size(); i++) {
                            rightRef8List.get(channelInCsv.get(i) + 8).setVisible(true); // 设置可见
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_REFRECALL:
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1)) { // 判断Ref1菜单是否显示
                        if (rightRef1List.get(rightRef1Index).getChildNodes() != null) {
                            rightRef1List = rightRef1List.get(rightRef1Index).getChildNodes(); // 进入子菜单节点
                            rightRef1Index = rightRef1List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 判断Ref1菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef1List, rightRef1Index, true); // 更新焦点框位置
                            } else {
                                rightRef1List.get(LIST_HEAD).setCurListSelect(rightRef1Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2)) { // 判断Ref2菜单是否显示
                        if (rightRef2List.get(rightRef2Index).getChildNodes() != null) {
                            rightRef2List = rightRef2List.get(rightRef2Index).getChildNodes(); // 进入子菜单节点
                            rightRef2Index = rightRef2List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 判断Ref2菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef2List, rightRef2Index, true); // 更新焦点框位置
                            } else {
                                rightRef2List.get(LIST_HEAD).setCurListSelect(rightRef2Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3)) { // 判断Ref3菜单是否显示
                        if (rightRef3List.get(rightRef5Index).getChildNodes() != null) {
                            rightRef3List = rightRef3List.get(rightRef5Index).getChildNodes(); // 进入子菜单节点
                            rightRef5Index = rightRef3List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 判断Ref3菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef3List, rightRef5Index, true); // 更新焦点框位置
                            } else {
                                rightRef3List.get(LIST_HEAD).setCurListSelect(rightRef5Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4)) { // 判断Ref4菜单是否显示
                        if (rightRef4List.get(rightRef4Index).getChildNodes() != null) {
                            rightRef4List = rightRef4List.get(rightRef4Index).getChildNodes(); // 进入子菜单节点
                            rightRef4Index = rightRef4List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 判断Ref4菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef4List, rightRef4Index, true); // 更新焦点框位置
                            } else {
                                rightRef4List.get(LIST_HEAD).setCurListSelect(rightRef4Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5)) { // 判断Ref5菜单是否显示
                        if (rightRef5List.get(rightRef5Index).getChildNodes() != null) {
                            rightRef5List = rightRef5List.get(rightRef5Index).getChildNodes(); // 进入子菜单节点
                            rightRef5Index = rightRef5List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 判断Ref5菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef5List, rightRef5Index, true); // 更新焦点框位置
                            } else {
                                rightRef5List.get(LIST_HEAD).setCurListSelect(rightRef5Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6)) { // 判断Ref6菜单是否显示
                        if (rightRef6List.get(rightRef6Index).getChildNodes() != null) {
                            rightRef6List = rightRef6List.get(rightRef6Index).getChildNodes(); // 进入子菜单节点
                            rightRef6Index = rightRef6List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 判断Ref6菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef6List, rightRef6Index, true); // 更新焦点框位置
                            } else {
                                rightRef6List.get(LIST_HEAD).setCurListSelect(rightRef6Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7)) { // 判断Ref7菜单是否显示
                        if (rightRef7List.get(rightRef7Index).getChildNodes() != null) {
                            rightRef7List = rightRef7List.get(rightRef7Index).getChildNodes(); // 进入子菜单节点
                            rightRef7Index = rightRef7List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 判断Ref7菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef7List, rightRef7Index, true); // 更新焦点框位置
                            } else {
                                rightRef7List.get(LIST_HEAD).setCurListSelect(rightRef7Index); // 设置列表选中项
                            }
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8)) { // 判断Ref8菜单是否显示
                        if (rightRef8List.get(rightRef8Index).getChildNodes() != null) {
                            rightRef8List = rightRef8List.get(rightRef8Index).getChildNodes(); // 进入子菜单节点
                            rightRef8Index = rightRef8List.get(LIST_HEAD).getCurListSelect();
                            if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 判断Ref8菜单是否显示
                                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightRef8List, rightRef8Index, true); // 更新焦点框位置
                            } else {
                                rightRef8List.get(LIST_HEAD).setCurListSelect(rightRef8Index); // 设置列表选中项
                            }
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_AFTERGLOW:
                     List<ExternalKeysNode> list = getTopBaseList().get(TopLayoutPopWindow.DETAIL_DISPLAY).getChildNodes()
                            .get(TopLayoutDisplay.DETAIL_PERSIST).getChildNodes().get(5).getChildNodes();
                     list.get(0).setVisible(true); // 设置可见
                     list=getTopBaseList().get(TopLayoutPopWindow.DETAIL_SAMPLE).getChildNodes()
                             .get(TopLayoutSample.DETAIL_MODE).getChildNodes().get(4).getChildNodes();
                     list.get(0).setVisible(true); // 设置可见

                    if (topList.get(topIndex).getChildNodes() != null) {
                        topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                        topList = topList.get(topIndex).getChildNodes(); // 进入子菜单节点
                        topIndex = topList.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(topList, topIndex, true); // 更新焦点框位置
                        } else {
                            topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_OKCANCEL:
                    if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                        setViewPlace(okCancelList, okCancelIndex, true); // 更新焦点框位置
                    } else {
                        okCancelList.get(LIST_HEAD).setCurListSelect(okCancelIndex); // 设置列表选中项
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_OK:
                    if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                        setViewPlace(okList, okIndex, true); // 更新焦点框位置
                    } else {
                        okList.get(LIST_HEAD).setCurListSelect(okIndex); // 设置列表选中项
                    }
                case MainViewGroup.DIALOG_MENUHALF:
                    break; // 跳出循环
            }
        }
    };

    private Consumer<Integer> consumerDialogClose = new Consumer<Integer>() { // RxBus消息消费者
        @Override
        public void accept(Integer integer) throws Exception {
            switch (integer) { // switch分支选择
                case MainViewGroup.DIALOG_TOPCOUNT:
                    topList = getTopBaseList(); // 获取顶部菜单基础列表
                    topIndex = topList.get(LIST_HEAD).getCurListSelect();
                    if (topIndex == TopLayoutPopWindow.DETAIL_TRIGGER) {
                        for (int i = 0; i < 2; i++) {
                            topList = topList.get(topIndex).getChildNodes(); // 进入子菜单节点
                            topIndex = topList.get(LIST_HEAD).getCurListSelect();
                        }
                    }
                    if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                            && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                        setViewPlace(topList, topIndex, true); // 更新焦点框位置
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_MEASURE_TVALUE:
                case MainViewGroup.DIALOG_MEASUREDELAY:
                case MainViewGroup.DIALOG_MEASUREPHASE:
                case MainViewGroup.DIALOG_TEXTKEYBOARD:

                    if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                            && topIndex < topList.size()
                            && topList.get(topIndex).getParentNode() != null) {
                        int index = topList.get(topIndex).getParentNode().getIndex();
                        topList = topList.get(topIndex).getParentNodes();
                        topIndex = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(topList, topIndex, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                            && rightCh1List.get(rightCh1Index).getParentNode() != null) {
                        int index = rightCh1List.get(rightCh1Index).getParentNode().getIndex();
                        rightCh1List = rightCh1List.get(rightCh1Index).getParentNodes();
                        rightCh1Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                            && rightCh2List.get(rightCh2Index).getParentNode() != null) {
                        int index = rightCh2List.get(rightCh2Index).getParentNode().getIndex();
                        rightCh2List = rightCh2List.get(rightCh2Index).getParentNodes();
                        rightCh2Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                            && rightCh3List.get(rightCh3Index).getParentNode() != null) {
                        int index = rightCh3List.get(rightCh3Index).getParentNode().getIndex();
                        rightCh3List = rightCh3List.get(rightCh3Index).getParentNodes();
                        rightCh3Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                            && rightCh4List.get(rightCh4Index).getParentNode() != null) {
                        int index = rightCh4List.get(rightCh4Index).getParentNode().getIndex();
                        rightCh4List = rightCh4List.get(rightCh4Index).getParentNodes();
                        rightCh4Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                            && rightCh5List.get(rightCh5Index).getParentNode() != null) {
                        int index = rightCh5List.get(rightCh5Index).getParentNode().getIndex();
                        rightCh5List = rightCh5List.get(rightCh5Index).getParentNodes();
                        rightCh5Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                            && rightCh6List.get(rightCh6Index).getParentNode() != null) {
                        int index = rightCh6List.get(rightCh6Index).getParentNode().getIndex();
                        rightCh6List = rightCh6List.get(rightCh6Index).getParentNodes();
                        rightCh6Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                            && rightCh7List.get(rightCh7Index).getParentNode() != null) {
                        int index = rightCh7List.get(rightCh7Index).getParentNode().getIndex();
                        rightCh7List = rightCh7List.get(rightCh7Index).getParentNodes();
                        rightCh7Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                            && rightCh8List.get(rightCh8Index).getParentNode() != null) {
                        int index = rightCh8List.get(rightCh8Index).getParentNode().getIndex();
                        rightCh8List = rightCh8List.get(rightCh8Index).getParentNodes();
                        rightCh8Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1) // 判断Math1菜单是否显示
                            && rightMath1List.get(rightMath1Index).getParentNode() != null) {
                        int index = rightMath1List.get(rightMath1Index).getParentNode().getIndex();
                        rightMath1List = rightMath1List.get(rightMath1Index).getParentNodes();
                        rightMath1Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1) // 判断Math1菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath1List, rightMath1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2) // 判断Math2菜单是否显示
                            && rightMath2List.get(rightMath2Index).getParentNode() != null) {
                        int index = rightMath2List.get(rightMath2Index).getParentNode().getIndex();
                        rightMath2List = rightMath2List.get(rightMath2Index).getParentNodes();
                        rightMath2Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2) // 判断Math2菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath2List, rightMath2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3) // 判断Math3菜单是否显示
                            && rightMath3List.get(rightMath3Index).getParentNode() != null) {
                        int index = rightMath3List.get(rightMath3Index).getParentNode().getIndex();
                        rightMath3List = rightMath3List.get(rightMath3Index).getParentNodes();
                        rightMath3Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3) // 判断Math3菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath3List, rightMath3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4) // 判断Math4菜单是否显示
                            && rightMath4List.get(rightMath4Index).getParentNode() != null) {
                        int index = rightMath4List.get(rightMath4Index).getParentNode().getIndex();
                        rightMath4List = rightMath4List.get(rightMath4Index).getParentNodes();
                        rightMath4Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4) // 判断Math4菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath4List, rightMath4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5) // 判断Math5菜单是否显示
                            && rightMath5List.get(rightMath5Index).getParentNode() != null) {
                        int index = rightMath5List.get(rightMath5Index).getParentNode().getIndex();
                        rightMath5List = rightMath5List.get(rightMath5Index).getParentNodes();
                        rightMath5Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5) // 判断Math5菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath5List, rightMath5Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6) // 判断Math6菜单是否显示
                            && rightMath6List.get(rightMath6Index).getParentNode() != null) {
                        int index = rightMath6List.get(rightMath6Index).getParentNode().getIndex();
                        rightMath6List = rightMath6List.get(rightMath6Index).getParentNodes();
                        rightMath6Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6) // 判断Math6菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath6List, rightMath6Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7) // 判断Math7菜单是否显示
                            && rightMath7List.get(rightMath7Index).getParentNode() != null) {
                        int index = rightMath7List.get(rightMath7Index).getParentNode().getIndex();
                        rightMath7List = rightMath7List.get(rightMath7Index).getParentNodes();
                        rightMath7Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7) // 判断Math7菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath7List, rightMath7Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8) // 判断Math8菜单是否显示
                            && rightMath8List.get(rightMath8Index).getParentNode() != null) {
                        int index = rightMath8List.get(rightMath8Index).getParentNode().getIndex();
                        rightMath8List = rightMath8List.get(rightMath8Index).getParentNodes();
                        rightMath8Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8) // 判断Math8菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath8List, rightMath8Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 判断Ref1菜单是否显示
                            && rightRef1List.get(rightRef1Index).getParentNode() != null) {
                        int index = rightRef1List.get(rightRef1Index).getParentNode().getIndex();
                        rightRef1List = rightRef1List.get(rightRef1Index).getParentNodes();
                        rightRef1Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 判断Ref1菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef1List, rightRef1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 判断Ref2菜单是否显示
                            && rightRef2List.get(rightRef2Index).getParentNode() != null) {
                        int index = rightRef2List.get(rightRef2Index).getParentNode().getIndex();
                        rightRef2List = rightRef2List.get(rightRef2Index).getParentNodes();
                        rightRef2Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 判断Ref2菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef2List, rightRef2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 判断Ref3菜单是否显示
                            && rightRef3List.get(rightRef3Index).getParentNode() != null) {
                        int index = rightRef3List.get(rightRef3Index).getParentNode().getIndex();
                        rightRef3List = rightRef3List.get(rightRef3Index).getParentNodes();
                        rightRef3Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 判断Ref3菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef3List, rightRef3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 判断Ref4菜单是否显示
                            && rightRef4List.get(rightRef4Index).getParentNode() != null) {
                        int index = rightRef4List.get(rightRef4Index).getParentNode().getIndex();
                        rightRef4List = rightRef4List.get(rightRef4Index).getParentNodes();
                        rightRef4Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 判断Ref4菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef4List, rightRef4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 判断Ref5菜单是否显示
                            && rightRef5List.get(rightRef5Index).getParentNode() != null) {
                        int index = rightRef5List.get(rightRef5Index).getParentNode().getIndex();
                        rightRef5List = rightRef5List.get(rightRef5Index).getParentNodes();
                        rightRef5Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 判断Ref5菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef5List, rightRef5Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 判断Ref6菜单是否显示
                            && rightRef6List.get(rightRef6Index).getParentNode() != null) {
                        int index = rightRef6List.get(rightRef6Index).getParentNode().getIndex();
                        rightRef6List = rightRef6List.get(rightRef6Index).getParentNodes();
                        rightRef6Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 判断Ref6菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef6List, rightRef6Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 判断Ref7菜单是否显示
                            && rightRef7List.get(rightRef7Index).getParentNode() != null) {
                        int index = rightRef7List.get(rightRef7Index).getParentNode().getIndex();
                        rightRef7List = rightRef7List.get(rightRef7Index).getParentNodes();
                        rightRef7Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 判断Ref7菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef7List, rightRef7Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 判断Ref8菜单是否显示
                            && rightRef8List.get(rightRef8Index).getParentNode() != null) {
                        int index = rightRef8List.get(rightRef8Index).getParentNode().getIndex();
                        rightRef8List = rightRef8List.get(rightRef8Index).getParentNodes();
                        rightRef8Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 判断Ref8菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef8List, rightRef8Index, true); // 更新焦点框位置
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_TOPSCALE:
                    if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                            && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                        setViewPlace(topList, topIndex, true); // 更新焦点框位置
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_NUMBERKEYBOARD:
                    if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CENTER_SEGMENTED_VISIBLE) // 从缓存读取布尔值
                            && segmentedList.get(segmentedIndex).getParentNodes() != null){
                        int index = segmentedList.get(segmentedIndex).getParentNode().getIndex();
                        segmentedList = segmentedList.get(segmentedIndex).getParentNodes();
                        segmentedIndex = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(segmentedList, segmentedIndex, true); // 更新焦点框位置
                        }
                    }else if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                            && topList.get(topIndex).getParentNodes() != null) {
                        int index = topList.get(topIndex).getParentNode().getIndex();
                        topList = topList.get(topIndex).getParentNodes();
                        topIndex = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(topList, topIndex, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1) // 判断S1串行总线菜单是否显示
                            && rightS1List.get(rightS1Index).getParentNodes() != null) {
                        int index = rightS1List.get(rightS1Index).getParentNode().getIndex();
                        rightS1List = rightS1List.get(rightS1Index).getParentNodes();
                        rightS1Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1) // 判断S1串行总线菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightS1List, rightS1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2) // 判断S2串行总线菜单是否显示
                            && rightS2List.get(rightS2Index).getParentNodes() != null) {
                        int index = rightS2List.get(rightS2Index).getParentNode().getIndex();
                        rightS2List = rightS2List.get(rightS2Index).getParentNodes();
                        rightS2Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2) // 判断S2串行总线菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightS2List, rightS2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3) // 判断S3串行总线菜单是否显示
                            && rightS3List.get(rightS3Index).getParentNodes() != null) {
                        int index = rightS3List.get(rightS3Index).getParentNode().getIndex();
                        rightS3List = rightS3List.get(rightS3Index).getParentNodes();
                        rightS3Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3) // 判断S3串行总线菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightS3List, rightS3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4) // 判断S4串行总线菜单是否显示
                            && rightS4List.get(rightS4Index).getParentNodes() != null) {
                        int index = rightS4List.get(rightS4Index).getParentNode().getIndex();
                        rightS4List = rightS4List.get(rightS4Index).getParentNodes();
                        rightS4Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4) // 判断S4串行总线菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightS4List, rightS4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 判断Ref1菜单是否显示
                            && rightRef1List.get(rightRef1Index).getParentNode() != null) {
                        int index = rightRef1List.get(rightRef1Index).getParentNode().getIndex();
                        rightRef1List = rightRef1List.get(rightRef1Index).getParentNodes();
                        rightRef1Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 判断Ref1菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef1List, rightRef1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 判断Ref2菜单是否显示
                            && rightRef2List.get(rightRef2Index).getParentNode() != null) {
                        int index = rightRef2List.get(rightRef2Index).getParentNode().getIndex();
                        rightRef2List = rightRef2List.get(rightRef2Index).getParentNodes();
                        rightRef2Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 判断Ref2菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef2List, rightRef2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 判断Ref3菜单是否显示
                            && rightRef3List.get(rightRef3Index).getParentNode() != null) {
                        int index = rightRef3List.get(rightRef3Index).getParentNode().getIndex();
                        rightRef3List = rightRef3List.get(rightRef3Index).getParentNodes();
                        rightRef3Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 判断Ref3菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef3List, rightRef3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 判断Ref4菜单是否显示
                            && rightRef4List.get(rightRef4Index).getParentNode() != null) {
                        int index = rightRef4List.get(rightRef4Index).getParentNode().getIndex();
                        rightRef4List = rightRef4List.get(rightRef4Index).getParentNodes();
                        rightRef4Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 判断Ref4菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef4List, rightRef4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 判断Ref5菜单是否显示
                            && rightRef5List.get(rightRef5Index).getParentNode() != null) {
                        int index = rightRef5List.get(rightRef5Index).getParentNode().getIndex();
                        rightRef5List = rightRef5List.get(rightRef5Index).getParentNodes();
                        rightRef5Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 判断Ref5菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef5List, rightRef5Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 判断Ref6菜单是否显示
                            && rightRef6List.get(rightRef6Index).getParentNode() != null) {
                        int index = rightRef6List.get(rightRef6Index).getParentNode().getIndex();
                        rightRef6List = rightRef6List.get(rightRef6Index).getParentNodes();
                        rightRef6Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 判断Ref6菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef6List, rightRef6Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 判断Ref7菜单是否显示
                            && rightRef7List.get(rightRef7Index).getParentNode() != null) {
                        int index = rightRef7List.get(rightRef7Index).getParentNode().getIndex();
                        rightRef7List = rightRef7List.get(rightRef7Index).getParentNodes();
                        rightRef7Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 判断Ref7菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef7List, rightRef7Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 判断Ref8菜单是否显示
                            && rightRef8List.get(rightRef8Index).getParentNode() != null) {
                        int index = rightRef8List.get(rightRef8Index).getParentNode().getIndex();
                        rightRef8List = rightRef8List.get(rightRef8Index).getParentNodes();
                        rightRef8Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 判断Ref8菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef8List, rightRef8Index, true); // 更新焦点框位置
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_SELECT_COLOR:
                case MainViewGroup.DIALOG_FLOATKEYBOARD:
                case MainViewGroup.DIALOG_FORMULAKEYBOARD:
                case MainViewGroup.DIALOG_NUMBERPICKER:
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1) // 判断Math1菜单是否显示
                            && rightMath1List.size() > rightMath1Index
                            && rightMath1List.get(rightMath1Index).getParentNodes() != null
                    ) {
                        int index = rightMath1List.get(rightMath1Index).getParentNode().getIndex();
                        rightMath1List = rightMath1List.get(rightMath1Index).getParentNodes();
                        rightMath1Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1) // 判断Math1菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath1List, rightMath1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2) // 判断Math2菜单是否显示
                            && rightMath2List.size() > rightMath2Index
                            && rightMath2List.get(rightMath2Index).getParentNodes() != null) {
                        int index = rightMath2List.get(rightMath2Index).getParentNode().getIndex();
                        rightMath2List = rightMath2List.get(rightMath2Index).getParentNodes();
                        rightMath2Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2) // 判断Math2菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath2List, rightMath2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3) // 判断Math3菜单是否显示
                            && rightMath3List.size() > rightMath3Index
                            && rightMath3List.get(rightMath3Index).getParentNodes() != null) {
                        int index = rightMath3List.get(rightMath3Index).getParentNode().getIndex();
                        rightMath3List = rightMath3List.get(rightMath3Index).getParentNodes();
                        rightMath3Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3) // 判断Math3菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath3List, rightMath3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4) // 判断Math4菜单是否显示
                            && rightMath4List.size() > rightMath4Index
                            && rightMath4List.get(rightMath4Index).getParentNodes() != null) {
                        int index = rightMath4List.get(rightMath4Index).getParentNode().getIndex();
                        rightMath4List = rightMath4List.get(rightMath4Index).getParentNodes();
                        rightMath4Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4) // 判断Math4菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath4List, rightMath4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5) // 判断Math5菜单是否显示
                            && rightMath5List.size() > rightMath5Index
                            && rightMath5List.get(rightMath5Index).getParentNodes() != null) {
                        int index = rightMath5List.get(rightMath5Index).getParentNode().getIndex();
                        rightMath5List = rightMath5List.get(rightMath5Index).getParentNodes();
                        rightMath5Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5) // 判断Math5菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath5List, rightMath5Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6) // 判断Math6菜单是否显示
                            && rightMath6List.size() > rightMath6Index
                            && rightMath6List.get(rightMath6Index).getParentNodes() != null) {
                        int index = rightMath6List.get(rightMath6Index).getParentNode().getIndex();
                        rightMath6List = rightMath6List.get(rightMath6Index).getParentNodes();
                        rightMath6Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6) // 判断Math6菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath6List, rightMath6Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7) // 判断Math7菜单是否显示
                            && rightMath7List.size() > rightMath7Index
                            && rightMath7List.get(rightMath7Index).getParentNodes() != null) {
                        int index = rightMath7List.get(rightMath7Index).getParentNode().getIndex();
                        rightMath7List = rightMath7List.get(rightMath7Index).getParentNodes();
                        rightMath7Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7) // 判断Math7菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath7List, rightMath7Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8) // 判断Math8菜单是否显示
                            && rightMath8List.size() > rightMath8Index
                            && rightMath8List.get(rightMath8Index).getParentNodes() != null
                    ) {
                        int index = rightMath8List.get(rightMath8Index).getParentNode().getIndex();
                        rightMath8List = rightMath8List.get(rightMath8Index).getParentNodes();
                        rightMath8Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8) // 判断Math8菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath8List, rightMath8Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                        && rightCh1List.get(rightCh1Index).getParentNodes()!=null){

                        int index = rightCh1List.get(rightCh1Index).getParentNode().getIndex();
                        rightCh1List = rightCh1List.get(rightCh1Index).getParentNodes();
                        rightCh1Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
                        }
                    }else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                            && rightCh2List.get(rightCh2Index).getParentNodes()!=null){
                        int index = rightCh2List.get(rightCh2Index).getParentNode().getIndex();
                        rightCh2List = rightCh2List.get(rightCh2Index).getParentNodes();
                        rightCh2Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
                        }
                    }else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                            && rightCh3List.get(rightCh3Index).getParentNodes()!=null){
                        int index = rightCh3List.get(rightCh3Index).getParentNode().getIndex();
                        rightCh3List = rightCh3List.get(rightCh3Index).getParentNodes();
                        rightCh3Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
                        }
                    }else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                            && rightCh4List.get(rightCh4Index).getParentNodes()!=null){
                        int index = rightCh4List.get(rightCh4Index).getParentNode().getIndex();
                        rightCh4List = rightCh4List.get(rightCh4Index).getParentNodes();
                        rightCh4Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                            && rightCh5List.get(rightCh5Index).getParentNode() != null) {
                        int index = rightCh5List.get(rightCh5Index).getParentNode().getIndex();
                        rightCh5List = rightCh5List.get(rightCh5Index).getParentNodes();
                        rightCh5Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                            && rightCh6List.get(rightCh6Index).getParentNode() != null) {
                        int index = rightCh6List.get(rightCh6Index).getParentNode().getIndex();
                        rightCh6List = rightCh6List.get(rightCh6Index).getParentNodes();
                        rightCh6Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                            && rightCh7List.get(rightCh7Index).getParentNode() != null) {
                        int index = rightCh7List.get(rightCh7Index).getParentNode().getIndex();
                        rightCh7List = rightCh7List.get(rightCh7Index).getParentNodes();
                        rightCh7Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                            && rightCh8List.get(rightCh8Index).getParentNode() != null) {
                        int index = rightCh8List.get(rightCh8Index).getParentNode().getIndex();
                        rightCh8List = rightCh8List.get(rightCh8Index).getParentNodes();
                        rightCh8Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                            && topList.get(topIndex).getParentNodes() != null) {
                        int index = topList.get(topIndex).getParentNode().getIndex();
                        topList = topList.get(topIndex).getParentNodes();
                        topIndex = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(topList, topIndex, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 判断Ref1菜单是否显示
                            && rightRef1List.get(rightRef1Index).getParentNode() != null) {
                        int index = rightRef1List.get(rightRef1Index).getParentNode().getIndex();
                        rightRef1List = rightRef1List.get(rightRef1Index).getParentNodes();
                        rightRef1Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 判断Ref1菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef1List, rightRef1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 判断Ref2菜单是否显示
                            && rightRef2List.get(rightRef2Index).getParentNode() != null) {
                        int index = rightRef2List.get(rightRef2Index).getParentNode().getIndex();
                        rightRef2List = rightRef2List.get(rightRef2Index).getParentNodes();
                        rightRef2Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 判断Ref2菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef2List, rightRef2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 判断Ref3菜单是否显示
                            && rightRef3List.get(rightRef3Index).getParentNode() != null) {
                        int index = rightRef3List.get(rightRef3Index).getParentNode().getIndex();
                        rightRef3List = rightRef3List.get(rightRef3Index).getParentNodes();
                        rightRef3Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 判断Ref3菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef3List, rightRef3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 判断Ref4菜单是否显示
                            && rightRef4List.get(rightRef4Index).getParentNode() != null) {
                        int index = rightRef4List.get(rightRef4Index).getParentNode().getIndex();
                        rightRef4List = rightRef4List.get(rightRef4Index).getParentNodes();
                        rightRef4Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 判断Ref4菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef4List, rightRef4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 判断Ref5菜单是否显示
                            && rightRef5List.get(rightRef5Index).getParentNode() != null) {
                        int index = rightRef5List.get(rightRef5Index).getParentNode().getIndex();
                        rightRef5List = rightRef5List.get(rightRef5Index).getParentNodes();
                        rightRef5Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 判断Ref5菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef5List, rightRef5Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 判断Ref6菜单是否显示
                            && rightRef6List.get(rightRef6Index).getParentNode() != null) {
                        int index = rightRef6List.get(rightRef6Index).getParentNode().getIndex();
                        rightRef6List = rightRef6List.get(rightRef6Index).getParentNodes();
                        rightRef6Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 判断Ref6菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef6List, rightRef6Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 判断Ref7菜单是否显示
                            && rightRef7List.get(rightRef7Index).getParentNode() != null) {
                        int index = rightRef7List.get(rightRef7Index).getParentNode().getIndex();
                        rightRef7List = rightRef7List.get(rightRef7Index).getParentNodes();
                        rightRef7Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 判断Ref7菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef7List, rightRef7Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 判断Ref8菜单是否显示
                            && rightRef8List.get(rightRef8Index).getParentNode() != null) {
                        int index = rightRef8List.get(rightRef8Index).getParentNode().getIndex();
                        rightRef8List = rightRef8List.get(rightRef8Index).getParentNodes();
                        rightRef8Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 判断Ref8菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef8List, rightRef8Index, true); // 更新焦点框位置
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_BAUDRATE:
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1) // 判断S1串行总线菜单是否显示
                            && rightS1List.get(rightS1Index).getParentNodes() != null) {
                        int index = rightS1List.get(rightS1Index).getParentNode().getIndex();
                        rightS1List = rightS1List.get(rightS1Index).getParentNodes();
                        rightS1Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1) // 判断S1串行总线菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightS1List, rightS1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2) // 判断S2串行总线菜单是否显示
                            && rightS2List.get(rightS2Index).getParentNodes() != null) {
                        int index = rightS2List.get(rightS2Index).getParentNode().getIndex();
                        rightS2List = rightS2List.get(rightS2Index).getParentNodes();
                        rightS2Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2) // 判断S2串行总线菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightS2List, rightS2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3) // 判断S3串行总线菜单是否显示
                            && rightS3List.get(rightS3Index).getParentNodes() != null) {
                        int index = rightS3List.get(rightS3Index).getParentNode().getIndex();
                        rightS3List = rightS3List.get(rightS3Index).getParentNodes();
                        rightS3Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3) // 判断S3串行总线菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightS3List, rightS3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4) // 判断S4串行总线菜单是否显示
                            && rightS4List.get(rightS4Index).getParentNodes() != null) {
                        int index = rightS4List.get(rightS4Index).getParentNode().getIndex();
                        rightS4List = rightS4List.get(rightS4Index).getParentNodes();
                        rightS4Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4) // 判断S4串行总线菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightS4List, rightS4Index, true); // 更新焦点框位置
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_CHANNELLABEL:
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                            && rightCh1List.get(rightCh1Index).getParentNodes() != null) {
                        int index = rightCh1List.get(rightCh1Index).getParentNode().getIndex();
                        rightCh1List = rightCh1List.get(rightCh1Index).getParentNodes();
                        rightCh1Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                            && rightCh2List.get(rightCh2Index).getParentNodes() != null) {
                        int index = rightCh2List.get(rightCh2Index).getParentNode().getIndex();
                        rightCh2List = rightCh2List.get(rightCh2Index).getParentNodes();
                        rightCh2Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                            && rightCh3List.get(rightCh3Index).getParentNodes() != null) {
                        int index = rightCh3List.get(rightCh3Index).getParentNode().getIndex();
                        rightCh3List = rightCh3List.get(rightCh3Index).getParentNodes();
                        rightCh3Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                            && rightCh4List.get(rightCh4Index).getParentNodes() != null) {
                        int index = rightCh4List.get(rightCh4Index).getParentNode().getIndex();
                        rightCh4List = rightCh4List.get(rightCh4Index).getParentNodes();
                        rightCh4Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                            && rightCh5List.get(rightCh5Index).getParentNodes() != null) {
                        int index = rightCh5List.get(rightCh5Index).getParentNode().getIndex();
                        rightCh5List = rightCh5List.get(rightCh5Index).getParentNodes();
                        rightCh5Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                            && rightCh6List.get(rightCh6Index).getParentNodes() != null) {
                        int index = rightCh6List.get(rightCh6Index).getParentNode().getIndex();
                        rightCh6List = rightCh6List.get(rightCh6Index).getParentNodes();
                        rightCh6Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
                        }
                    }else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                            && rightCh7List.get(rightCh7Index).getParentNodes() != null) {
                        int index = rightCh7List.get(rightCh7Index).getParentNode().getIndex();
                        rightCh7List = rightCh7List.get(rightCh7Index).getParentNodes();
                        rightCh7Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                            && rightCh8List.get(rightCh8Index).getParentNodes() != null) {
                        int index = rightCh8List.get(rightCh8Index).getParentNode().getIndex();
                        rightCh8List = rightCh8List.get(rightCh8Index).getParentNodes();
                        rightCh8Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1) // 判断Math1菜单是否显示
                            && rightMath1List.get(rightMath1Index).getParentNodes() != null) {
                        int index = rightMath1List.get(rightMath1Index).getParentNode().getIndex();
                        rightMath1List = rightMath1List.get(rightMath1Index).getParentNodes();
                        rightMath1Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightMath1List, rightMath1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2) // 判断Math2菜单是否显示
                            && rightMath2List.get(rightMath2Index).getParentNodes() != null) {
                        int index = rightMath2List.get(rightMath2Index).getParentNode().getIndex();
                        rightMath2List = rightMath2List.get(rightMath2Index).getParentNodes();
                        rightMath2Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightMath2List, rightMath2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3) // 判断Math3菜单是否显示
                            && rightMath3List.get(rightMath3Index).getParentNodes() != null) {
                        int index = rightMath3List.get(rightMath3Index).getParentNode().getIndex();
                        rightMath3List = rightMath3List.get(rightMath3Index).getParentNodes();
                        rightMath3Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightMath3List, rightMath3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4) // 判断Math4菜单是否显示
                            && rightMath4List.get(rightMath4Index).getParentNodes() != null) {
                        int index = rightMath4List.get(rightMath4Index).getParentNode().getIndex();
                        rightMath4List = rightMath4List.get(rightMath4Index).getParentNodes();
                        rightMath4Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightMath4List, rightMath4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5) // 判断Math5菜单是否显示
                            && rightMath5List.get(rightMath5Index).getParentNodes() != null) {
                        int index = rightMath5List.get(rightMath5Index).getParentNode().getIndex();
                        rightMath5List = rightMath5List.get(rightMath5Index).getParentNodes();
                        rightMath5Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightMath5List, rightMath5Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6) // 判断Math6菜单是否显示
                            && rightMath6List.get(rightMath6Index).getParentNodes() != null) {
                        int index = rightMath6List.get(rightMath6Index).getParentNode().getIndex();
                        rightMath6List = rightMath6List.get(rightMath6Index).getParentNodes();
                        rightMath6Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightMath6List, rightMath6Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7) // 判断Math7菜单是否显示
                            && rightMath7List.get(rightMath7Index).getParentNodes() != null) {
                        int index = rightMath7List.get(rightMath7Index).getParentNode().getIndex();
                        rightMath7List = rightMath7List.get(rightMath7Index).getParentNodes();
                        rightMath7Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightMath7List, rightMath7Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8) // 判断Math8菜单是否显示
                            && rightMath8List.get(rightMath8Index).getParentNodes() != null) {
                        int index = rightMath8List.get(rightMath8Index).getParentNode().getIndex();
                        rightMath8List = rightMath8List.get(rightMath8Index).getParentNodes();
                        rightMath8Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightMath8List, rightMath8Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 判断Ref1菜单是否显示
                            && rightRef1List.get(rightRef1Index).getParentNodes() != null) {
                        int index = rightRef1List.get(rightRef1Index).getParentNode().getIndex();
                        rightRef1List = rightRef1List.get(rightRef1Index).getParentNodes();
                        rightRef1Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightRef1List, rightRef1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 判断Ref2菜单是否显示
                            && rightRef2List.get(rightRef2Index).getParentNodes() != null) {
                        int index = rightRef2List.get(rightRef2Index).getParentNode().getIndex();
                        rightRef2List = rightRef2List.get(rightRef2Index).getParentNodes();
                        rightRef2Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightRef2List, rightRef2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 判断Ref3菜单是否显示
                            && rightRef3List.get(rightRef3Index).getParentNodes() != null) {
                        int index = rightRef3List.get(rightRef3Index).getParentNode().getIndex();
                        rightRef3List = rightRef3List.get(rightRef3Index).getParentNodes();
                        rightRef3Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightRef3List, rightRef3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 判断Ref4菜单是否显示
                            && rightRef4List.get(rightRef4Index).getParentNodes() != null) {
                        int index = rightRef4List.get(rightRef4Index).getParentNode().getIndex();
                        rightRef4List = rightRef4List.get(rightRef4Index).getParentNodes();
                        rightRef4Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightRef4List, rightRef4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 判断Ref5菜单是否显示
                            && rightRef5List.get(rightRef5Index).getParentNodes() != null) {
                        int index = rightRef5List.get(rightRef5Index).getParentNode().getIndex();
                        rightRef5List = rightRef5List.get(rightRef5Index).getParentNodes();
                        rightRef5Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightRef5List, rightRef5Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 判断Ref6菜单是否显示
                            && rightRef6List.get(rightRef6Index).getParentNodes() != null) {
                        int index = rightRef6List.get(rightRef6Index).getParentNode().getIndex();
                        rightRef6List = rightRef6List.get(rightRef6Index).getParentNodes();
                        rightRef6Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightRef6List, rightRef6Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 判断Ref7菜单是否显示
                            && rightRef7List.get(rightRef7Index).getParentNodes() != null) {
                        int index = rightRef7List.get(rightRef7Index).getParentNode().getIndex();
                        rightRef7List = rightRef7List.get(rightRef7Index).getParentNodes();
                        rightRef7Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightRef7List, rightRef7Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 判断Ref8菜单是否显示
                            && rightRef8List.get(rightRef8Index).getParentNodes() != null) {
                        int index = rightRef8List.get(rightRef8Index).getParentNode().getIndex();
                        rightRef8List = rightRef8List.get(rightRef8Index).getParentNodes();
                        rightRef8Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightRef8List, rightRef8Index, true); // 更新焦点框位置
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_PROBE_INTERFACE:
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                            && rightCh1List.get(rightCh1Index).getParentNodes() != null) {
                        int index = rightCh1List.get(rightCh1Index).getParentNode().getIndex();
                        rightCh1List = rightCh1List.get(rightCh1Index).getParentNodes();
                        rightCh1Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                            && rightCh2List.get(rightCh2Index).getParentNodes() != null) {
                        int index = rightCh2List.get(rightCh2Index).getParentNode().getIndex();
                        rightCh2List = rightCh2List.get(rightCh2Index).getParentNodes();
                        rightCh2Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                            && rightCh3List.get(rightCh3Index).getParentNodes() != null) {
                        int index = rightCh3List.get(rightCh3Index).getParentNode().getIndex();
                        rightCh3List = rightCh3List.get(rightCh3Index).getParentNodes();
                        rightCh3Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                            && rightCh4List.get(rightCh4Index).getParentNodes() != null) {
                        int index = rightCh4List.get(rightCh4Index).getParentNode().getIndex();
                        rightCh4List = rightCh4List.get(rightCh4Index).getParentNodes();
                        rightCh4Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                            && rightCh5List.get(rightCh5Index).getParentNodes() != null) {
                        int index = rightCh5List.get(rightCh5Index).getParentNode().getIndex();
                        rightCh5List = rightCh5List.get(rightCh5Index).getParentNodes();
                        rightCh5Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                            && rightCh6List.get(rightCh6Index).getParentNodes() != null) {
                        int index = rightCh6List.get(rightCh6Index).getParentNode().getIndex();
                        rightCh6List = rightCh6List.get(rightCh6Index).getParentNodes();
                        rightCh6Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
                        }
                    }else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                            && rightCh7List.get(rightCh7Index).getParentNodes() != null) {
                        int index = rightCh7List.get(rightCh7Index).getParentNode().getIndex();
                        rightCh7List = rightCh7List.get(rightCh7Index).getParentNodes();
                        rightCh7Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                            && rightCh8List.get(rightCh8Index).getParentNodes() != null) {
                        int index = rightCh8List.get(rightCh8Index).getParentNode().getIndex();
                        rightCh8List = rightCh8List.get(rightCh8Index).getParentNodes();
                        rightCh8Index = index;
                        if (focusView != null && focusView.getVisibility()== View.VISIBLE) {
                            setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_MATH_FFT_PERSIST: {
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1) // 判断Math1菜单是否显示
                            && rightMath1List.get(rightMath1Index).getParentNodes() != null) {
                        int index = rightMath1List.get(rightMath1Index).getParentNode().getIndex();
                        rightMath1List = rightMath1List.get(rightMath1Index).getParentNodes();
                        rightMath1Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath1List, rightMath1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2) // 判断Math2菜单是否显示
                            && rightMath2List.get(rightMath2Index).getParentNodes() != null) {
                        int index = rightMath2List.get(rightMath2Index).getParentNode().getIndex();
                        rightMath2List = rightMath2List.get(rightMath2Index).getParentNodes();
                        rightMath2Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath2List, rightMath2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3) // 判断Math3菜单是否显示
                            && rightMath3List.get(rightMath3Index).getParentNodes() != null) {
                        int index = rightMath3List.get(rightMath3Index).getParentNode().getIndex();
                        rightMath3List = rightMath3List.get(rightMath3Index).getParentNodes();
                        rightMath3Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath3List, rightMath3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4) // 判断Math4菜单是否显示
                            && rightMath4List.get(rightMath4Index).getParentNodes() != null) {
                        int index = rightMath4List.get(rightMath4Index).getParentNode().getIndex();
                        rightMath4List = rightMath4List.get(rightMath4Index).getParentNodes();
                        rightMath4Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath4List, rightMath4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5) // 判断Math5菜单是否显示
                            && rightMath5List.get(rightMath5Index).getParentNodes() != null) {
                        int index = rightMath5List.get(rightMath5Index).getParentNode().getIndex();
                        rightMath5List = rightMath5List.get(rightMath5Index).getParentNodes();
                        rightMath5Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath5List, rightMath5Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6) // 判断Math6菜单是否显示
                            && rightMath6List.get(rightMath6Index).getParentNodes() != null) {
                        int index = rightMath6List.get(rightMath6Index).getParentNode().getIndex();
                        rightMath6List = rightMath6List.get(rightMath6Index).getParentNodes();
                        rightMath6Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath6List, rightMath6Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7) // 判断Math7菜单是否显示
                            && rightMath7List.get(rightMath7Index).getParentNodes() != null) {
                        int index = rightMath7List.get(rightMath7Index).getParentNode().getIndex();
                        rightMath7List = rightMath7List.get(rightMath7Index).getParentNodes();
                        rightMath7Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath7List, rightMath7Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8) // 判断Math8菜单是否显示
                            && rightMath8List.get(rightMath8Index).getParentNodes() != null) {
                        int index = rightMath8List.get(rightMath8Index).getParentNode().getIndex();
                        rightMath8List = rightMath8List.get(rightMath8Index).getParentNodes();
                        rightMath8Index = index;
                        if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightMath8List, rightMath8Index, true); // 更新焦点框位置
                        }
                    }
                }
                break; // 跳出循环
                case MainViewGroup.DIALOG_BANDWIDTH:
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1)) { // 判断CH1通道菜单是否显示
                        rightCh1List = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH1); // 获取右侧通道基础列表
                        rightCh1Index = rightCh1List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2)) { // 判断CH2通道菜单是否显示
                        rightCh2List = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH2); // 获取右侧通道基础列表
                        rightCh2Index = rightCh2List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3)) { // 判断CH3通道菜单是否显示
                        rightCh3List = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH3); // 获取右侧通道基础列表
                        rightCh3Index = rightCh3List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4)) { // 判断CH4通道菜单是否显示
                        rightCh4List = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH4); // 获取右侧通道基础列表
                        rightCh4Index = rightCh4List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5)) { // 判断CH5通道菜单是否显示
                        rightCh5List = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH5); // 获取右侧通道基础列表
                        rightCh5Index = rightCh5List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                           setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6)) { // 判断CH6通道菜单是否显示
                        rightCh6List = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH6); // 获取右侧通道基础列表
                        rightCh6Index = rightCh6List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7)) { // 判断CH7通道菜单是否显示
                        rightCh7List = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH7); // 获取右侧通道基础列表
                        rightCh7Index = rightCh7List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8)) { // 判断CH8通道菜单是否显示
                        rightCh8List = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH8); // 获取右侧通道基础列表
                        rightCh8Index = rightCh8List.get(LIST_HEAD).getCurListSelect();
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                                setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
                            }
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_PROBEMULTIPLE:
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                            && rightCh1List.get(rightCh1Index).getParentNodes() != null) {
                        int index = rightCh1List.get(rightCh1Index).getParentNode().getIndex();
                        rightCh1List = rightCh1List.get(rightCh1Index).getParentNodes();
                        rightCh1Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1) // 判断CH1通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh1List, rightCh1Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                            && rightCh2List.get(rightCh2Index).getParentNodes() != null) {
                        int index = rightCh2List.get(rightCh2Index).getParentNode().getIndex();
                        rightCh2List = rightCh2List.get(rightCh2Index).getParentNodes();
                        rightCh2Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2) // 判断CH2通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh2List, rightCh2Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                            && rightCh3List.get(rightCh3Index).getParentNodes() != null) {
                        int index = rightCh3List.get(rightCh3Index).getParentNode().getIndex();
                        rightCh3List = rightCh3List.get(rightCh3Index).getParentNodes();
                        rightCh3Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3) // 判断CH3通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh3List, rightCh3Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                            && rightCh4List.get(rightCh4Index).getParentNodes() != null) {
                        int index = rightCh4List.get(rightCh4Index).getParentNode().getIndex();
                        rightCh4List = rightCh4List.get(rightCh4Index).getParentNodes();
                        rightCh4Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4) // 判断CH4通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh4List, rightCh4Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                            && rightCh5List.get(rightCh5Index).getParentNode() != null) {
                        int index = rightCh5List.get(rightCh5Index).getParentNode().getIndex();
                        rightCh5List = rightCh5List.get(rightCh5Index).getParentNodes();
                        rightCh5Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5) // 判断CH5通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh5List, rightCh5Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                            && rightCh6List.get(rightCh6Index).getParentNode() != null) {
                        int index = rightCh6List.get(rightCh6Index).getParentNode().getIndex();
                        rightCh6List = rightCh6List.get(rightCh6Index).getParentNodes();
                        rightCh6Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6) // 判断CH6通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh6List, rightCh6Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                            && rightCh7List.get(rightCh7Index).getParentNode() != null) {
                        int index = rightCh7List.get(rightCh7Index).getParentNode().getIndex();
                        rightCh7List = rightCh7List.get(rightCh7Index).getParentNodes();
                        rightCh7Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7) // 判断CH7通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh7List, rightCh7Index, true); // 更新焦点框位置
                        }
                    } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                            && rightCh8List.get(rightCh8Index).getParentNode() != null) {
                        int index = rightCh8List.get(rightCh8Index).getParentNode().getIndex();
                        rightCh8List = rightCh8List.get(rightCh8Index).getParentNodes();
                        rightCh8Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8) // 判断CH8通道菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightCh8List, rightCh8Index, true); // 更新焦点框位置
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_LOAF_REF_CSV:
                case MainViewGroup.DIALOG_REFRECALL:
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 判断Ref1菜单是否显示
                            && rightRef1List.get(rightRef1Index).getParentNodes() != null) {
                        int index = rightRef1List.get(rightRef1Index).getParentNode().getIndex();
                        rightRef1List = rightRef1List.get(rightRef1Index).getParentNodes();
                        rightRef1Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1) // 判断Ref1菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef1List, rightRef1Index, true); // 更新焦点框位置
                        }
                    }
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 判断Ref2菜单是否显示
                            && rightRef2List.get(rightRef2Index).getParentNodes() != null) {
                        int index = rightRef2List.get(rightRef2Index).getParentNode().getIndex();
                        rightRef2List = rightRef2List.get(rightRef2Index).getParentNodes();
                        rightRef2Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2) // 判断Ref2菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE
                        ) {
                            setViewPlace(rightRef2List, rightRef2Index, true); // 更新焦点框位置
                        }
                    }
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 判断Ref3菜单是否显示
                            && rightRef3List.get(rightRef3Index).getParentNodes() != null) {
                        int index = rightRef3List.get(rightRef3Index).getParentNode().getIndex();
                        rightRef3List = rightRef3List.get(rightRef3Index).getParentNodes();
                        rightRef3Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3) // 判断Ref3菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef3List, rightRef3Index, true); // 更新焦点框位置
                        }
                    }
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 判断Ref4菜单是否显示
                            && rightRef4List.get(rightRef4Index).getParentNodes() != null) {
                        int index = rightRef4List.get(rightRef4Index).getParentNode().getIndex();
                        rightRef4List = rightRef4List.get(rightRef4Index).getParentNodes();
                        rightRef4Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4) // 判断Ref4菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef4List, rightRef4Index, true); // 更新焦点框位置
                        }
                    }
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 判断Ref5菜单是否显示
                            && rightRef5List.get(rightRef5Index).getParentNodes() != null) {
                        int index = rightRef5List.get(rightRef5Index).getParentNode().getIndex();
                        rightRef5List = rightRef5List.get(rightRef5Index).getParentNodes();
                        rightRef5Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5) // 判断Ref5菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef5List, rightRef5Index, true); // 更新焦点框位置
                        }
                    }
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 判断Ref6菜单是否显示
                            && rightRef6List.get(rightRef6Index).getParentNodes() != null) {
                        int index = rightRef6List.get(rightRef6Index).getParentNode().getIndex();
                        rightRef6List = rightRef6List.get(rightRef6Index).getParentNodes();
                        rightRef6Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6) // 判断Ref6菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef6List, rightRef6Index, true); // 更新焦点框位置
                        }
                    }
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 判断Ref7菜单是否显示
                            && rightRef7List.get(rightRef7Index).getParentNodes() != null) {
                        int index = rightRef7List.get(rightRef7Index).getParentNode().getIndex();
                        rightRef7List = rightRef7List.get(rightRef7Index).getParentNodes();
                        rightRef7Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7) // 判断Ref7菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef7List, rightRef7Index, true); // 更新焦点框位置
                        }
                    }
                    if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 判断Ref8菜单是否显示
                            && rightRef8List.get(rightRef8Index).getParentNodes() != null) {
                        int index = rightRef8List.get(rightRef8Index).getParentNode().getIndex();
                        rightRef8List = rightRef8List.get(rightRef8Index).getParentNodes();
                        rightRef8Index = index;
                        if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8) // 判断Ref8菜单是否显示
                                && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                            setViewPlace(rightRef8List, rightRef8Index, true); // 更新焦点框位置
                        }
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_AFTERGLOW:
                    topList = topList.get(topIndex).getParentNodes();
                    topIndex = topList.get(LIST_HEAD).getCurListSelect();
                    if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                            && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                        setViewPlace(topList, topIndex, true); // 更新焦点框位置
                    }
                    List<ExternalKeysNode> list= getTopBaseList().get(TopLayoutPopWindow.DETAIL_DISPLAY).getChildNodes()
                            .get(TopLayoutDisplay.DETAIL_PERSIST).getChildNodes().get(5).getChildNodes();
                    list.get(0).setVisible(false); // 设置不可见
                    list=getTopBaseList().get(TopLayoutPopWindow.DETAIL_SAMPLE).getChildNodes()
                            .get(TopLayoutSample.DETAIL_MODE).getChildNodes().get(4).getChildNodes();
                    list.get(0).setVisible(false); // 设置不可见
                    break; // 跳出循环
                case MainViewGroup.DIALOG_OKCANCEL:
                case MainViewGroup.DIALOG_OK:
                    if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                        setFocusViewVisible(View.GONE); // 设置焦点框可见性
                    }
                    break; // 跳出循环
                case MainViewGroup.DIALOG_MENUHALF:
                    break; // 跳出循环

            }
        }
    };

    private Consumer<String> consumerShowNormalState = new Consumer<String>() { //显示对应的设置页面
        @Override
        public void accept(String String) throws Exception {
            String[] params = String.split(CommandMsgToUI.PARAM_SPLIT);
            int channelNumber = Integer.parseInt(params[0]);
            boolean isNormal = Boolean.parseBoolean(params[1]);
            if (TChan.isMath(channelNumber)) {
                setRightMathState(channelNumber, isNormal);
            } else if (TChan.isRef(channelNumber)) {
                setRightRefState(channelNumber, isNormal);
            } else if (TChan.isSerial(channelNumber)) {
                setRightSerialsState(channelNumber, isNormal);
            }
        }
    };

    private Consumer<String> consumerSerialsCanAdd = new Consumer<String>() { //点击的时候 右下角点击的时候发的消息
        @Override
        public void accept(String available) throws Throwable {
            String[] params = available.split(CommandMsgToUI.PARAM_SPLIT);
            int serialsSlideIndex = Integer.parseInt(params[0]);
            boolean mathAvailable = Boolean.parseBoolean(params[1]);
            boolean refAvailable = Boolean.parseBoolean(params[2]);
            boolean serialAvailable = Boolean.parseBoolean(params[3]);
            int curSerialsNumber = CacheUtil.S1;
            switch (serialsSlideIndex) { // switch分支选择
                case MainViewGroup.RIGHTSLIP_S1:
                    curSerialsNumber = CacheUtil.S1;
                    break; // 跳出循环
                case MainViewGroup.RIGHTSLIP_S2:
                    curSerialsNumber = CacheUtil.S2;
                    break; // 跳出循环
                case MainViewGroup.RIGHTSLIP_S3:
                    curSerialsNumber = CacheUtil.S3;
                    break; // 跳出循环
                case MainViewGroup.RIGHTSLIP_S4:
                    curSerialsNumber = CacheUtil.S4;
                    break; // 跳出循环
            }
            List<ExternalKeysNode> rightSxBaseList = getRightSerialsBaseList(curSerialsNumber);
            rightSxBaseList.get(0).setVisible(mathAvailable); //math
            rightSxBaseList.get(1).setVisible(refAvailable);//ref
            rightSxBaseList.get(2).setVisible(serialAvailable);//serials
            rightSxBaseList.get(3).setVisible(false);//delete 按钮
            rightSxBaseList.get(4).setVisible(true);//+ 按钮
            rightSxBaseList.get(12).setVisible(false);//switch 按钮
        }
    };

    private Consumer<String> consumerRefCanAdd = new Consumer<String>() { //点击的时候 右下角点击的时候发的消息
        @Override
        public void accept(String available) throws Throwable {
            String[] params = available.split(CommandMsgToUI.PARAM_SPLIT);
            int refSlideIndex = Integer.parseInt(params[0]);
            boolean mathAvailable = Boolean.parseBoolean(params[1]);
            boolean refAvailable = Boolean.parseBoolean(params[2]);
            boolean serialAvailable = Boolean.parseBoolean(params[3]);

            List<ExternalKeysNode> rightRefBaseList = getRightRefBaseList(refSlideIndex);
            rightRefBaseList.get(0).setVisible(mathAvailable); //math
            rightRefBaseList.get(1).setVisible(refAvailable);//ref
            rightRefBaseList.get(2).setVisible(serialAvailable);//serials
            rightRefBaseList.get(3).setVisible(false);//delete 按钮
            rightRefBaseList.get(4).setVisible(true);//+ 按钮
            rightRefBaseList.get(10).setVisible(false);//switch 按钮
        }
    };


    private Consumer<String> consumerMathCanAdd = new Consumer<String>() { //点击的时候 右下角点击的时候发的消息
        @Override
        public void accept(String available) throws Throwable {
            String[] params = available.split(CommandMsgToUI.PARAM_SPLIT);
            int mathSlideIndex = Integer.parseInt(params[0]);
            boolean mathAvailable = Boolean.parseBoolean(params[1]);
            boolean refAvailable = Boolean.parseBoolean(params[2]);
            boolean serialAvailable = Boolean.parseBoolean(params[3]);

            List<ExternalKeysNode> rightMathBaseList = getRightMathBaseList(mathSlideIndex);
            rightMathBaseList.get(0).setVisible(mathAvailable);//math
            rightMathBaseList.get(1).setVisible(refAvailable);//ref
            rightMathBaseList.get(2).setVisible(serialAvailable);//serials
            rightMathBaseList.get(3).setVisible(false);//delete 按钮
            rightMathBaseList.get(4).setVisible(true);//+ 按钮
            rightMathBaseList.get(11).setVisible(false);//switch 按钮
        }
    };

    private final Consumer<String> updateSCsvSelectViewPlace = new Consumer<String>() {
        @Override
        public void accept(String params) throws Throwable {
            String[] values = params.split(CommandMsgToUI.PARAM_SPLIT);
            int refChan = Integer.parseInt(values[0]);
            int posY = Integer.parseInt(values[1]);
            int index = Integer.parseInt(values[2]);
            switch (refChan) { // switch分支选择
                case TChan.R1:
                    rightRef1List.get(rightRef1Index).setPlace(rightRef1Index, 1230, posY, 494, 54); // 设置节点位置和尺寸
                    setViewPlace(rightRef1List, rightRef1Index, false); // 更新焦点框位置
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R1, String.valueOf(index));
                    break; // 跳出循环
                case TChan.R2:
                    rightRef2List.get(rightRef2Index).setPlace(rightRef2Index, 1230, posY, 494, 54); // 设置节点位置和尺寸
                    setViewPlace(rightRef2List, rightRef2Index, false); // 更新焦点框位置
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R2, String.valueOf(index));
                    break; // 跳出循环
                case TChan.R3:
                    rightRef3List.get(rightRef3Index).setPlace(rightRef3Index, 1230, posY, 494, 54); // 设置节点位置和尺寸
                    setViewPlace(rightRef3List, rightRef3Index, false); // 更新焦点框位置
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R3, String.valueOf(index));
                    break; // 跳出循环
                case TChan.R4:
                    rightRef4List.get(rightRef4Index).setPlace(rightRef4Index, 1230, posY, 494, 54); // 设置节点位置和尺寸
                    setViewPlace(rightRef4List, rightRef4Index, false); // 更新焦点框位置
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R4, String.valueOf(index));
                    break; // 跳出循环
                case TChan.R5:
                    rightRef5List.get(rightRef5Index).setPlace(rightRef5Index, 1230, posY, 494, 54); // 设置节点位置和尺寸
                    setViewPlace(rightRef5List, rightRef5Index, false); // 更新焦点框位置
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R5, String.valueOf(index));
                    break; // 跳出循环
                case TChan.R6:
                    rightRef6List.get(rightRef6Index).setPlace(rightRef6Index, 1230, posY, 494, 54); // 设置节点位置和尺寸
                    setViewPlace(rightRef6List, rightRef6Index, false); // 更新焦点框位置
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R6, String.valueOf(index));
                    break; // 跳出循环
                case TChan.R7:
                    rightRef7List.get(rightRef7Index).setPlace(rightRef7Index, 1230, posY, 494, 54); // 设置节点位置和尺寸
                    setViewPlace(rightRef7List, rightRef7Index, false); // 更新焦点框位置
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R7, String.valueOf(index));
                    break; // 跳出循环
                case TChan.R8:
                    rightRef8List.get(rightRef8Index).setPlace(rightRef8Index, 1230, posY, 494, 54); // 设置节点位置和尺寸
                    setViewPlace(rightRef8List, rightRef8Index, false); // 更新焦点框位置
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_REF_CSV_INDEX + TChan.R8, String.valueOf(index));
                    break; // 跳出循环
            }
        }
    };


    //endregion

    /**
     * 设置串行总线节点的可见性和位置状态
     * @param channelNumber 通道编号
     * @param isNormal true为正常状态，false为关闭状态
     */
    private void setRightSerialsState(int channelNumber, boolean isNormal) {
        List<ExternalKeysNode> rightSxBaseList = getRightSerialsBaseList(TChan.toSerialNumber(channelNumber));
        if (isNormal) {//S1...S4显示模式
            rightSxBaseList.get(0).setVisible(false); //math
            rightSxBaseList.get(1).setVisible(false);//ref
            rightSxBaseList.get(2).setVisible(false);//serials
            rightSxBaseList.get(3).setVisible(true);//delete 按钮
            rightSxBaseList.get(4).setVisible(false);//+ 按钮
            rightSxBaseList.get(12).setVisible(true);//switch 按钮
        } else { //Math/Ref/Serials共同显示模式
            rightSxBaseList.get(0).setVisible(true); //math
            rightSxBaseList.get(1).setVisible(true);//ref
            rightSxBaseList.get(2).setVisible(true);//serials
            rightSxBaseList.get(3).setVisible(false);//delete 按钮
            rightSxBaseList.get(4).setVisible(true);//+ 按钮
            rightSxBaseList.get(12).setVisible(false);//switch 按钮
        }
    }

    /**
     * 设置参考波形节点的可见性和位置状态
     * @param channelNumber 通道编号
     * @param isNormal true为正常状态，false为关闭状态
     */
    private void setRightRefState(int channelNumber, boolean isNormal) {

        int slipIndex = MainViewGroup.RIGHTSLIP_REF1;
        switch (channelNumber) { // switch分支选择
            case TChan.R1:
                slipIndex = MainViewGroup.RIGHTSLIP_REF1;
                break; // 跳出循环
            case TChan.R2:
                slipIndex = MainViewGroup.RIGHTSLIP_REF2;
                break; // 跳出循环
            case TChan.R3:
                slipIndex = MainViewGroup.RIGHTSLIP_REF3;
                break; // 跳出循环
            case TChan.R4:
                slipIndex = MainViewGroup.RIGHTSLIP_REF4;
                break; // 跳出循环
            case TChan.R5:
                slipIndex = MainViewGroup.RIGHTSLIP_REF5;
                break; // 跳出循环
            case TChan.R6:
                slipIndex = MainViewGroup.RIGHTSLIP_REF6;
                break; // 跳出循环
            case TChan.R7:
                slipIndex = MainViewGroup.RIGHTSLIP_REF7;
                break; // 跳出循环
            case TChan.R8:
                slipIndex = MainViewGroup.RIGHTSLIP_REF8;
                break; // 跳出循环
            default: // 默认分支
                slipIndex = MainViewGroup.RIGHTSLIP_REF1;
                break; // 跳出循环
        }
        List<ExternalKeysNode> rightRefBaseList = getRightRefBaseList(slipIndex);
        if (isNormal) {//R1...R4显示模式
            rightRefBaseList.get(0).setVisible(false); //math
            rightRefBaseList.get(1).setVisible(false);//ref
            rightRefBaseList.get(2).setVisible(false);//serials
            rightRefBaseList.get(3).setVisible(true);//delete 按钮
            rightRefBaseList.get(4).setVisible(false);//+ 按钮
            RefChannel refChannel = ChannelFactory.getRefChannel(TChan.toFpgaChNo(channelNumber));
            if (refChannel != null && refChannel.getRefType() != WaveData.FFT_WAVE) {
                rightRefBaseList.get(7).setVisible(true);//delay 按钮
            } else {
                rightRefBaseList.get(7).setVisible(false);//delay 按钮
            }
            rightRefBaseList.get(10).setVisible(true);//switch 按钮
            rightRefBaseList.get(11).setVisible(true);//imgTop 按钮
            rightRefBaseList.get(12).setVisible(true);//imgBottom 按钮

        } else { //Math/Ref/Serials共同显示模式
            rightRefBaseList.get(0).setVisible(true); //math
            rightRefBaseList.get(1).setVisible(true);//ref
            rightRefBaseList.get(2).setVisible(true);//serials
            rightRefBaseList.get(3).setVisible(false);//delete 按钮
            rightRefBaseList.get(4).setVisible(true);//+ 按钮
            rightRefBaseList.get(7).setVisible(false);//delay 按钮
            rightRefBaseList.get(10).setVisible(false);//switch 按钮
            rightRefBaseList.get(11).setVisible(false);//imgTop 按钮
            rightRefBaseList.get(12).setVisible(false);//imgBottom 按钮
        }
    }


    /**
     * 设置数学运算通道节点的可见性和位置状态
     * @param channelNumber 通道编号
     * @param isNormal true为正常状态，false为关闭状态
     */
    private void setRightMathState(int channelNumber, boolean isNormal) {
        int slipIndex = MainViewGroup.RIGHTSLIP_MATH1;
        switch (channelNumber) { // switch分支选择
            case TChan.Math1:
                slipIndex = MainViewGroup.RIGHTSLIP_MATH1;
                break; // 跳出循环
            case TChan.Math2:
                slipIndex = MainViewGroup.RIGHTSLIP_MATH2;
                break; // 跳出循环
            case TChan.Math3:
                slipIndex = MainViewGroup.RIGHTSLIP_MATH3;
                break; // 跳出循环
            case TChan.Math4:
                slipIndex = MainViewGroup.RIGHTSLIP_MATH4;
                break; // 跳出循环
            case TChan.Math5:
                slipIndex = MainViewGroup.RIGHTSLIP_MATH5;
                break; // 跳出循环
            case TChan.Math6:
                slipIndex = MainViewGroup.RIGHTSLIP_MATH6;
                break; // 跳出循环
            case TChan.Math7:
                slipIndex = MainViewGroup.RIGHTSLIP_MATH7;
                break; // 跳出循环
            case TChan.Math8:
                slipIndex = MainViewGroup.RIGHTSLIP_MATH8;
                break; // 跳出循环
            default: // 默认分支
                slipIndex = MainViewGroup.RIGHTSLIP_MATH1;
                break; // 跳出循环
        }
        int mathNumber = TChan.toMathNumber(channelNumber);
        List<ExternalKeysNode> rightMathBaseList = getRightMathBaseList(slipIndex);
        if (isNormal) {//M1...M8显示模式
            rightMathBaseList.get(0).setVisible(false);//math
            rightMathBaseList.get(1).setVisible(false);//ref
            rightMathBaseList.get(2).setVisible(false);//serials
            rightMathBaseList.get(3).setVisible(true);//delete 按钮
            rightMathBaseList.get(4).setVisible(false);//+ 按钮

            rightMathBaseList.get(10).setVisible(true);//label
            rightMathBaseList.get(11).setVisible(true);//switch 按钮
            rightMathBaseList.get(12).setVisible(true);//imgTop 按钮
            rightMathBaseList.get(13).setVisible(true);//imgBottom 按钮
        } else { //Math/Ref/Serials共同显示模式
            rightMathBaseList.get(0).setVisible(true); //math
            rightMathBaseList.get(1).setVisible(true);//ref
            rightMathBaseList.get(2).setVisible(true);//serials
            rightMathBaseList.get(3).setVisible(false);//delete 按钮
            rightMathBaseList.get(4).setVisible(true);//+ 按钮

            rightMathBaseList.get(10).setVisible(true);//label
            rightMathBaseList.get(11).setVisible(false);//switch 按钮
            rightMathBaseList.get(12).setVisible(false);//imgTop 按钮
            rightMathBaseList.get(13).setVisible(false);//imgBottom 按钮
        }
    }

    /**
     * 设置view位置
     */
    private void setViewPlace(List<ExternalKeysNode> curList, int curIndex, boolean saveSelect) { // 更新焦点框位置
//        Logger.d("setViewPlace:curIndex:" + curIndex + ",curList:" + curList);
//        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) && curList.get(curIndex).getParentNode() == null) {
//            if (topSlipTitle == null) {
//                topSlipTitle = (TopViewTitleWithScroll) mainViewGroup.findViewById(R.id.topPopTitleWithHead);
//            }
//            topSlipTitle.moveOnlyScroll(curIndex);
//            RadioGroup group = (RadioGroup) ((TopViewHorScroll) topSlipTitle.getChildAt(0)).getChildAt(0);
//            int[] local = new int[2];
//            for (int i = 0; i < group.getChildCount(); i++) {
//                group.getChildAt(i).getLocationOnScreen(local);
//                curList.get(i).setX(local[0]);
//            }
//        }
        //triggerTitle列表被�?�择时，可能滑动
        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                && curIndex < curList.size()
                && ExternalKeysNode.TYPE_TRIGGER_TITLE.equals(curList.get(curIndex).getType())) { // 判断是否为触发菜单滑动类型节点
            if (triggerTitle == null) {
                triggerTitle = (TopViewTitleWithScroll) mainViewGroup.findViewById(R.id.triggerTitle);
            }
            triggerTitle.moveOnlyScroll(curIndex);
            RadioGroup group = (RadioGroup) ((TopViewHorScroll) triggerTitle.getChildAt(0)).getChildAt(0);
            int[] local = new int[2];
            for (int i = 0; i < curList.size(); i++) { // 遍历节点列表
                group.getChildAt(i).getLocationOnScreen(local);
                curList.get(i).setX(local[0]);
            }
        }
        setMainMenuChannel(curList, curIndex);


        if (focusView == null) { // 懒加载焦点框控件
            focusView = (AnimationView) mainViewGroup.findViewById(R.id.focusControl); // 获取焦点框控件
        }
        if (saveSelect) {
            curList.get(LIST_HEAD).setCurListSelect(curIndex); // 设置列表选中项
        }
        setFocusViewVisible(View.VISIBLE); // 设置焦点框可见性
        focusView.setX(curList.get(curIndex).getX());
        focusView.setY(curList.get(curIndex).getY());
        ViewGroup.LayoutParams layoutParams = focusView.getLayoutParams();
        layoutParams.width = curList.get(curIndex).getW();
        layoutParams.height = curList.get(curIndex).getH();
        focusView.setLayoutParams(layoutParams);
//        Log.d(TAG, "setViewPlace isVisible: "+(focusView.getVisibility()==View.VISIBLE));
//        Log.d(TAG, "setViewPlace x: "+focusView.getX()+",top:"+focusView.getY()+",width:"+focusView.getWidth()+",height:"+focusView.getHeight());
//        Log.d(TAG, "setViewPlace node:"+curList.get(curIndex).toString());
//        try {
//            throw new Exception();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    /**
     * 设置主菜单通道区域(测量/存储/光标)的焦点位置和可见性
     * @param curList 当前节点列表
     * @param curIndex 当前焦点索引
     */
    private void setMainMenuChannel(List<ExternalKeysNode> curList, int curIndex){
        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                && ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_MEASURE_COMMON.equals(curList.get(curIndex).getType())) {
            if (chanMeasureCommon == null) {
                chanMeasureCommon =  mainViewGroup.findViewById(R.id.chanMeasureCommon);
            }
            chanMeasureCommon.moveOnlyScroll(curIndex);
            RadioGroup group =  chanMeasureCommon.getRadioGroup();
            int[] local = new int[2];
            int idx=-1;
            for (int i = 0; i < curList.size(); i++) { // 遍历节点列表
                if (curList.get(i).getType()==ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_MEASURE_COMMON){
                    idx++;
                    group.getChildAt(idx).getLocationOnScreen(local);
                    curList.get(i).setX(local[0] - 15);
                }
            }
        }

        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                && ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_MEASURE_SETTING.equals(curList.get(curIndex).getType())) {
            if (chanMeasureSetting == null) {
                chanMeasureSetting =  mainViewGroup.findViewById(R.id.chanMeasureSetting);
            }
            chanMeasureSetting.moveOnlyScroll(curIndex-3);
            RadioGroup group =  chanMeasureSetting.getRadioGroup();
            int[] local = new int[2];
            int idx=-1;
            for (int i = 0; i < curList.size(); i++) { // 遍历节点列表
                if (curList.get(i).getType()==ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_MEASURE_SETTING){
                    idx++;
                    group.getChildAt(idx).getLocationOnScreen(local);
                    curList.get(i).setX(local[0] - 15);
                }
            }
        }

        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                && ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_SAVE_WAVE.equals(curList.get(curIndex).getType())) {
            if (chanSaveWave == null) {
                chanSaveWave = mainViewGroup.findViewById(R.id.chanSaveWave);
            }
            chanSaveWave.moveOnlyScroll(curIndex);
            RadioGroup group = chanSaveWave.getRadioGroup();
            int[] local = new int[2];
            int idx = -1;
            for (int i = 0; i < curList.size(); i++) { // 遍历节点列表
                if (ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_SAVE_WAVE.equals(curList.get(i).getType())) {
                    idx++;
                    if (group.getChildAt(idx) != null) {
                        group.getChildAt(idx).getLocationOnScreen(local);
                        curList.get(i).setX(local[0] - 15);
                    }
                }
            }
        }

        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                && ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_SAVE_CSV.equals(curList.get(curIndex).getType())) {
            if (chanSaveCsv == null) {
                chanSaveCsv = mainViewGroup.findViewById(R.id.chanSaveCsv);
            }
            chanSaveCsv.moveOnlyScroll(curIndex);
            LinearLayout views = chanSaveCsv.getCheckBoxs();
            int[] local = new int[2];
            int idx = -1;
            for (int i = 0; i < curList.size(); i++) { // 遍历节点列表
                if (ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_SAVE_CSV.equals(curList.get(i).getType())) {
                    idx++;
                    views.getChildAt(idx).getLocationOnScreen(local);
                    curList.get(i).setX(local[0] - 15);
                }
            }
        }

        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                && ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_AUTO_SAVE_CHANNEL.equals(curList.get(curIndex).getType())) {
            if (chanAutoSave == null) {
                chanAutoSave = mainViewGroup.findViewById(R.id.chanAutoSave);
            }
            chanAutoSave.moveOnlyScroll(curIndex);
            LinearLayout views = chanAutoSave.getCheckBoxs();
            int[] local = new int[2];
            int idx = -1;
            for (int i = 0; i < curList.size(); i++) { // 遍历节点列表
                if (ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_AUTO_SAVE_CHANNEL.equals(curList.get(i).getType())) {
                    idx++;
                    views.getChildAt(idx).getLocationOnScreen(local);
                    curList.get(i).setX(local[0] - 15);
                }
            }
        }

        if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                && ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_CURSOR_COMMON.equals(curList.get(curIndex).getType())) {
            if (chanCursorCommon == null) {
                chanCursorCommon =  mainViewGroup.findViewById(R.id.chanCursorCommon);
            }

            chanCursorCommon.moveOnlyScroll(curIndex);
            RadioGroup group =  chanCursorCommon.getRadioGroup();
            int[] local = new int[2];
            int idx=-1;
            for (int i = 0; i < curList.size(); i++) {//curList.size()= 8+8+8+4=28 ; group.getChildCount() = 8+8+8+1(Auto) = 25
                if (curList.get(i).getType() == ExternalKeysNode.TYPE_MAIN_MENU_CHANNEL_CURSOR_COMMON) {
                    idx++;
                    if (null == group.getChildAt(idx)) continue;
                    group.getChildAt(idx).getLocationOnScreen(local);
                    curList.get(i).setX(local[0] -15);
                }
            }
            while (curList.size() > group.getChildCount()) {
                curList.remove(curList.size() - 1);
            }
        }

    }

    /**
     * 设置焦点框的可见性
     * @param visible 可见性状态(View.VISIBLE/GONE/INVISIBLE)
     */
    public void setFocusViewVisible(int visible){ // 设置焦点框可见性
        if (focusView == null) { // 懒加载焦点框控件
            focusView = (AnimationView) mainViewGroup.findViewById(R.id.focusControl); // 获取焦点框控件
        }
        focusView.setVisibility(visible); // 设置焦点框可见性
        focusView.bringToFront();
    }

    /**
     * 判断焦点框是否可见
     * @return true表示焦点框可见
     */
    public boolean isFocusViewVisible() {
        if (focusView == null) { // 懒加载焦点框控件
            focusView = (AnimationView) mainViewGroup.findViewById(R.id.focusControl); // 获取焦点框控件
        }
        return focusView.getVisibility() == View.VISIBLE;
    }

    /**
     * 当当前状态是没有显示时，则显示focusView
     *
     * @return 从gone变为visible则返回true
     */
    public boolean showViewIfGone() {
        if (focusView == null) { // 懒加载焦点框控件
            focusView = (AnimationView) mainViewGroup.findViewById(R.id.focusControl); // 获取焦点框控件
        }
        if (focusView.getVisibility() == View.VISIBLE) {
            return false; // 返回false
        } else {

            if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_OKCANCEL)) { // 判断指定弹窗是否显示
                setViewPlace(okCancelList, okCancelIndex, false); // 更新焦点框位置
            } else if (mainViewGroup.isDialogShow(MainViewGroup.DIALOG_OK)) {
                setViewPlace(okList, okIndex, false); // 更新焦点框位置
            } else if (CacheUtil.get().getBoolean(CacheUtil.MAIN_CENTER_SEGMENTED_VISIBLE)) { // 从缓存读取布尔值
                setViewPlace(segmentedList, segmentedIndex, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)) { // 判断顶部菜单是否显示
                setViewPlace(topList, topIndex, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH1)) { // 判断CH1通道菜单是否显示
                setViewPlace(rightCh1List, rightCh1Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH2)) { // 判断CH2通道菜单是否显示
                setViewPlace(rightCh2List, rightCh2Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH3)) { // 判断CH3通道菜单是否显示
                setViewPlace(rightCh3List, rightCh3Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH4)) { // 判断CH4通道菜单是否显示
                setViewPlace(rightCh4List, rightCh4Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH5)) { // 判断CH5通道菜单是否显示
                setViewPlace(rightCh5List, rightCh5Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH6)) { // 判断CH6通道菜单是否显示
                setViewPlace(rightCh6List, rightCh6Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH7)) { // 判断CH7通道菜单是否显示
                setViewPlace(rightCh7List, rightCh7Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_CH8)) { // 判断CH8通道菜单是否显示
                setViewPlace(rightCh8List, rightCh8Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH1)) { // 判断Math1菜单是否显示
                setViewPlace(rightMath1List, rightMath1Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH2)) { // 判断Math2菜单是否显示
                setViewPlace(rightMath2List, rightMath2Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH3)) { // 判断Math3菜单是否显示
                setViewPlace(rightMath3List, rightMath3Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH4)) { // 判断Math4菜单是否显示
                setViewPlace(rightMath4List, rightMath4Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH5)) { // 判断Math5菜单是否显示
                setViewPlace(rightMath5List, rightMath5Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH6)) { // 判断Math6菜单是否显示
                setViewPlace(rightMath6List, rightMath6Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH7)) { // 判断Math7菜单是否显示
                setViewPlace(rightMath7List, rightMath7Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_MATH8)) { // 判断Math8菜单是否显示
                setViewPlace(rightMath8List, rightMath8Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF1)) { // 判断Ref1菜单是否显示
                setViewPlace(rightRef1List, rightRef1Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF2)) { // 判断Ref2菜单是否显示
                setViewPlace(rightRef2List, rightRef2Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF3)) { // 判断Ref3菜单是否显示
                setViewPlace(rightRef3List, rightRef3Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF4)) { // 判断Ref4菜单是否显示
                setViewPlace(rightRef4List, rightRef4Index, false); // 更新焦点框位置
            } else if(mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF5)) { // 判断Ref5菜单是否显示
                setViewPlace(rightRef5List, rightRef5Index, false); // 更新焦点框位置
            } else if(mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF6)) { // 判断Ref6菜单是否显示
                setViewPlace(rightRef6List, rightRef6Index, false); // 更新焦点框位置
            } else if(mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF7)) { // 判断Ref7菜单是否显示
                setViewPlace(rightRef7List, rightRef7Index, false); // 更新焦点框位置
            } else if(mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_REF8)) { // 判断Ref8菜单是否显示
                setViewPlace(rightRef8List, rightRef8Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S1)) { // 判断S1串行总线菜单是否显示
                setViewPlace(rightS1List, rightS1Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S2)) { // 判断S2串行总线菜单是否显示
                setViewPlace(rightS2List, rightS2Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S3)) { // 判断S3串行总线菜单是否显示
                setViewPlace(rightS3List, rightS3Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.RIGHTSLIP_S4)) { // 判断S4串行总线菜单是否显示
                setViewPlace(rightS4List, rightS4Index, false); // 更新焦点框位置
            } else if (mainViewGroup.isSlipShow(MainViewGroup.BOTTOMSLIP)) { // 判断底部菜单是否显示
                setViewPlace(bottomList, bottomIndex, false); // 更新焦点框位置
            } else if (CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT)) { // 从缓存读取布尔值
                setViewPlace(serialsWordList, serialsWordIndex, false); // 更新焦点框位置
            } else {
                if (channelsLayout == null) { // 懒加载通道布局
                    channelsLayout = (MainLayoutCenterChannel) mainViewGroup.findViewById(R.id.mainLayoutCenterChannels); // 获取通道布局控件
                }
                if (channelsLayout.getVisibility() == View.VISIBLE) {
                    setViewPlace(channelsList, channelsIndex, false); // 更新焦点框位置
                }
            }
            return true; // 返回true
        }
    }

    /**
     * getTopBaseList方法
     */
    private List<ExternalKeysNode> getTopBaseList() {
        List<ExternalKeysNode> list = topList;
        ExternalKeysNode node = topList.get(LIST_HEAD);
        while (node.getParentNode() != null) {
            list = node.getParentNodes();
            node = node.getParentNode();
        }
        return list;
    }

    /**
     * @param channel MainViewGroup.RIGHTSLIP_CH1<p/>
     *                MainViewGroup.RIGHTSLIP_CH2<p/>
     *                MainViewGroup.RIGHTSLIP_CH3<p/>
     *                MainViewGroup.RIGHTSLIP_CH4
     */
    private List<ExternalKeysNode> getRightChBaseList(int channel) {
        List<ExternalKeysNode> list = null;
        ExternalKeysNode node = null;
        switch (channel) { // switch分支选择
            case MainViewGroup.RIGHTSLIP_CH1:
                list = rightCh1List;
                node = rightCh1List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_CH2:
                list = rightCh2List;
                node = rightCh2List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_CH3:
                list = rightCh3List;
                node = rightCh3List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_CH4:
                list = rightCh4List;
                node = rightCh4List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_CH5:
                list = rightCh5List;
                node = rightCh5List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_CH6:
                list = rightCh6List;
                node = rightCh6List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_CH7:
                list = rightCh7List;
                node = rightCh7List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_CH8:
                list = rightCh8List;
                node = rightCh8List.get(0);
                break; // 跳出循环
        }
        if (node != null) {
            while (node.getParentNode() != null) {
                list = node.getParentNodes();
                node = node.getParentNode();
            }
        }
        return list;
    }

    /**
     * getRightMathBaseList方法
     */
    private List<ExternalKeysNode> getRightMathBaseList(int slipIndex) {
        List<ExternalKeysNode> list = null;
        ExternalKeysNode node = null;
        switch (slipIndex) { // switch分支选择
            case MainViewGroup.RIGHTSLIP_MATH1:
                list = rightMath1List;
                node = rightMath1List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_MATH2:
                list = rightMath2List;
                node = rightMath2List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_MATH3:
                list = rightMath3List;
                node = rightMath3List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_MATH4:
                list = rightMath4List;
                node = rightMath4List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_MATH5:
                list = rightMath5List;
                node = rightMath5List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_MATH6:
                list = rightMath6List;
                node = rightMath6List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_MATH7:
                list = rightMath7List;
                node = rightMath7List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_MATH8:
                list = rightMath8List;
                node = rightMath8List.get(0);
                break; // 跳出循环
        }
        if (node != null) {
            while (node.getParentNode() != null) {
                list = node.getParentNodes();
                node = node.getParentNode();
            }
        }
        return list;
    }

    /**
     * getRightRefBaseList方法
     */
    private List<ExternalKeysNode> getRightRefBaseList(int slipIndex) {
        List<ExternalKeysNode> list = null;
        ExternalKeysNode node = null;
        switch (slipIndex) { // switch分支选择
            case MainViewGroup.RIGHTSLIP_REF1:
                list = rightRef1List;
                node = rightRef1List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_REF2:
                list = rightRef2List;
                node = rightRef2List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_REF3:
                list = rightRef3List;
                node = rightRef3List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_REF4:
                list = rightRef4List;
                node = rightRef4List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_REF5:
                list = rightRef5List;
                node = rightRef5List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_REF6:
                list = rightRef6List;
                node = rightRef6List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_REF7:
                list = rightRef7List;
                node = rightRef7List.get(0);
                break; // 跳出循环
            case MainViewGroup.RIGHTSLIP_REF8:
                list = rightRef8List;
                node = rightRef8List.get(0);
                break; // 跳出循环
        }
        if (node != null) {
            while (node.getParentNode() != null) {
                list = node.getParentNodes();
                node = node.getParentNode();
            }
        }
        return list;
    }

    /**
     * getRightSerialsBaseList方法
     */
    private List<ExternalKeysNode> getRightSerialsBaseList(int serialsNumber) {
        List<ExternalKeysNode> list;
        ExternalKeysNode node;
        switch (serialsNumber) { // switch分支选择
            case CacheUtil.S2:
                list = rightS2List;
                node = rightS2List.get(0);
                break; // 跳出循环
            case CacheUtil.S3:
                list = rightS3List;
                node = rightS3List.get(0);
                break; // 跳出循环
            case CacheUtil.S4:
                list = rightS4List;
                node = rightS4List.get(0);
                break; // 跳出循环
            case CacheUtil.S1:
            default: // 默认分支
                list = rightS1List;
                node = rightS1List.get(0);
                break; // 跳出循环
        }
        while (node.getParentNode() != null) {
            list = node.getParentNodes();
            node = node.getParentNode();
        }
        return list;
    }

    /**
     * getBottomBaseList方法
     */
    private List<ExternalKeysNode> getBottomBaseList() {
        List<ExternalKeysNode> list = bottomList;
        ExternalKeysNode node = bottomList.get(0);
        while (node.getParentNode() != null) {
            list = node.getParentNodes();
            node = node.getParentNode();
        }
        return list;
    }

    /**
     * getAutoMotiveBaseList方法
     */
    private List<ExternalKeysNode> getAutoMotiveBaseList() {
        List<ExternalKeysNode> list = autoMotiveList;
        ExternalKeysNode node = autoMotiveList.get(0);
        while (node.getParentNode() != null) {
            list = node.getParentNodes();
            node = node.getParentNode();
        }
        return list;
    }
    /**
     * getSegmentedBaseList方法
     */
    private List<ExternalKeysNode> getSegmentedBaseList() {
        List<ExternalKeysNode> list = segmentedList;
        ExternalKeysNode node = segmentedList.get(LIST_HEAD);
        while (node.getParentNode() != null) {
            list = node.getParentNodes();
            node = node.getParentNode();
        }
        return list;
    }


    //region 各种变化设置
    /**
     * 根据当前配置更新所有列表节点的可见性
     */
    private void setListItemVisible() {
        setChannelList();
        setMeasureDelList();
        setTriggerRunt();
        setTriggerSlope();
        setTriggerVideo();
        setTriggerSerialsType(CacheUtil.S1);
        setTriggerSerialsType(CacheUtil.S2);
        setTriggerSerialsType(CacheUtil.S3);
        setTriggerSerialsType(CacheUtil.S4);
        setTriggerSerialsChildren(CacheUtil.S1);
        setTriggerSerialsChildren(CacheUtil.S2);
        setTriggerSerialsChildren(CacheUtil.S3);
        setTriggerSerialsChildren(CacheUtil.S4);
        setTriggerSerialsNumberKeyBoard(CacheUtil.S1);
        setTriggerSerialsNumberKeyBoard(CacheUtil.S2);
        setTriggerSerialsNumberKeyBoard(CacheUtil.S3);
        setTriggerSerialsNumberKeyBoard(CacheUtil.S4);
        setRightMathNumberKeyBoard();
        setRightSerialsTextView(CacheUtil.S1);
        setRightSerialsTextView(CacheUtil.S2);
        setRightSerialsTextView(CacheUtil.S3);
        setRightSerialsTextView(CacheUtil.S4);
        //setRightSerialsNumberKeyBoard();
        setRightSerialsSpiCs(CacheUtil.S1);
        setRightSerialsSpiCs(CacheUtil.S2);
        setRightSerialsSpiCs(CacheUtil.S3);
        setRightSerialsSpiCs(CacheUtil.S4);
        setCenterChannels();
        setScopeConfig();
    }

    /**
     * 根据示波器硬件配置(ScopeConfig)更新节点可见性(通道数、数学运算数、参考波形数等)
     */
    private void setScopeConfig() {
        if (!ScopeConfig.getConfig().isEnableFreqCounter() && !App.IsDebug()) {
            List<ExternalKeysNode> topBaseList = getTopBaseList();
            List<ExternalKeysNode> childNodes = topBaseList.get(TopLayoutPopWindow.DETAIL_FREQUENCYMETER).getChildNodes();
            for (int i = 0; i < childNodes.size(); i++) {
                childNodes.get(i).setVisible(i == 0);
            }
        }
        if (!ScopeConfig.getConfig().isEnableAutoRange() && !App.IsDebug()) {
            List<ExternalKeysNode> topBaseList = getTopBaseList();
            List<ExternalKeysNode> childNodes = topBaseList.get(TopLayoutPopWindow.DETAIL_AUTO).getChildNodes()
                    .get(TopLayoutAuto.DETAIL_RANGE).getChildNodes();
            for (int i = 0; i < childNodes.size(); i++) {
                childNodes.get(i).setVisible(i == 1);
            }
        }
        if (!ScopeConfig.getConfig().isEnableHighLowFilter() && !App.IsDebug()) {
            List<ExternalKeysNode> rightCh1BaseList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH1);
            rightCh1BaseList.get(11).setVisible(false); // 设置不可见
            rightCh1BaseList.get(12).setVisible(false); // 设置不可见
            List<ExternalKeysNode> rightCh2BaseList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH2);
            rightCh2BaseList.get(11).setVisible(false); // 设置不可见
            rightCh2BaseList.get(12).setVisible(false); // 设置不可见
            List<ExternalKeysNode> rightCh3BaseList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH3);
            rightCh3BaseList.get(11).setVisible(false); // 设置不可见
            rightCh3BaseList.get(12).setVisible(false); // 设置不可见
            List<ExternalKeysNode> rightCh4BaseList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH4);
            rightCh4BaseList.get(11).setVisible(false); // 设置不可见
            rightCh4BaseList.get(12).setVisible(false); // 设置不可见
            List<ExternalKeysNode> rightCh5BaseList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH5);
            rightCh5BaseList.get(11).setVisible(false); // 设置不可见
            rightCh5BaseList.get(12).setVisible(false); // 设置不可见
            List<ExternalKeysNode> rightCh6BaseList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH6);
            rightCh6BaseList.get(11).setVisible(false); // 设置不可见
            rightCh6BaseList.get(12).setVisible(false); // 设置不可见
            List<ExternalKeysNode> rightCh7BaseList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH7);
            rightCh7BaseList.get(11).setVisible(false); // 设置不可见
            rightCh7BaseList.get(12).setVisible(false); // 设置不可见
            List<ExternalKeysNode> rightCh8BaseList = getRightChBaseList(MainViewGroup.RIGHTSLIP_CH8);
            rightCh8BaseList.get(11).setVisible(false); // 设置不可见
            rightCh8BaseList.get(12).setVisible(false); // 设置不可见
        }
    }

    private MainLayoutCenterChannel channelsLayout; // 中心通道布局引用
    private CheckBox channelsCheckBox; // 复选框控件引用
    private MainLayoutCenterMenu centerMenuLayout; // 中心菜单布局引用

    /**
     * 更新中心通道选择区域节点的位置和可见性
     */
    private void setCenterChannels() {
        if (channelsLayout == null) { // 懒加载通道布局
            channelsLayout = (MainLayoutCenterChannel) mainViewGroup.findViewById(R.id.mainLayoutCenterChannels); // 获取通道布局控件
        }
        int showCount = channelsLayout.getCurChannelShowCount();
        int showIndex = channelsLayout.getCurChannelShowIndex();
        boolean[] channelShow = channelsLayout.getChannelShow();
        int finalShowIndex = 0;
        List<ExternalKeysNode> list = ExternalKeysNodeUtil.getCenterChannelsNode(channelsLayout);
        for (int i = 0; i < list.size(); i++) {
            if (i < showIndex && channelShow[i]) {
                finalShowIndex++;
            }
            list.get(i).setVisible(i < showCount);
        }
        list.get(LIST_HEAD).setCurListSelect(finalShowIndex);
        channelsList.clear(); // 清空通道选择节点列表
        channelsList.addAll(list);
        channelsIndex = finalShowIndex;
        if (channelsLayout.getVisibility() == View.VISIBLE &&
                focusView != null && focusView.getVisibility() == View.VISIBLE) {
            setViewPlace(channelsList, channelsIndex, true); // 更新焦点框位置
        } else {
            if (focusView != null && !mainViewGroup.isRightSlipMathShow()) {
                setFocusViewVisible(View.GONE); // 设置焦点框可见性
                Logger.i("ExternalKeysManager,focusControl,setCenterChannels:" + View.GONE); // 信息日志输出
            }
            channelsList.get(LIST_HEAD).setCurListSelect(channelsIndex); // 设置列表选中项
        }
    }


    private MainLayoutCenterSegmented layoutSegmented; // 分段存储布局引用

    /**
     * 更新分段存储导航区域节点的位置和可见性
     */
    private void setCenterSegmented() {
        if (layoutSegmented == null) { // 懒加载分段存储布局
            layoutSegmented = mainViewGroup.getCenterSegmentedLayout();
        }
        int positionX = (int) layoutSegmented.getX();
        int positionY = (int) layoutSegmented.getY();
        List<ExternalKeysNode> list;
        if (CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_DISPLAY) == 0) { // 从缓存读取整数值
            if (layoutSegmented.getType() == MainLayoutCenterSegmented.TYPE_SINGLE_SMALL) {
                list = ExternalKeysNodeUtil.getCenterSegmentedSingleSmallNodeList();
            } else {
                list = ExternalKeysNodeUtil.getCenterSegmentedSingleLargeNodeList();
            }
        } else {
            list = ExternalKeysNodeUtil.getCenterSegmentedFitNodeList();
        }
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setX(positionX + list.get(i).getX());
            list.get(i).setY(positionY + list.get(i).getY());
            if (list.get(i).getChildNodes() != null
                    && !Objects.equals(list.get(i).getDialog(), ExternalKeysNode.DIALOG_NUMBERKEYBOARD)) {
                ExternalKeysNode childNode = list.get(i).getChildNodes().get(LIST_HEAD);
                childNode.setX(positionX + childNode.getX());
                childNode.setY(positionY + childNode.getY());
            }
        }
        if (segmentedList.get(LIST_HEAD).getParentNode() != null
                && Objects.equals(segmentedList.get(LIST_HEAD).getParentNode().getDialog(), ExternalKeysNode.DIALOG_NUMBERKEYBOARD)) {
            segmentedList = list;
            segmentedList = segmentedList.get(2).getChildNodes(); // 获取子节点列表
            if(segmentedList.get(LIST_HEAD).getChildNodes() != null){
                segmentedList = segmentedList.get(LIST_HEAD).getChildNodes(); // 获取子节点列表
            }
            return; // 直接返回
        } else {
            segmentedList = list;
            setCenterSegmentedNumberKeyBoard();
        }
        if (layoutSegmented.getVisibility() == View.VISIBLE
                && !mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)) { // 判断顶部菜单是否显示
            if (focusView != null && focusView.getVisibility() == View.VISIBLE) {
                setViewPlace(segmentedList, segmentedIndex, true); // 更新焦点框位置
            } else {
                if (focusView != null) {
                    setFocusViewVisible(View.GONE); // 设置焦点框可见性
                    Logger.i(Command.TAG,"ExternalKeysManager,focusControl,setCenterSegmented:" + View.GONE); // 信息日志输出

                }
            }
        }
        segmentedList.get(LIST_HEAD).setCurListSelect(segmentedIndex); // 设置列表选中项
    }
    /**
     * 更新分段存储数字键盘区域节点的位置
     */
    private void setCenterSegmentedNumberKeyBoard() {
        List<ExternalKeysNode> segmentedBaseList = getSegmentedBaseList();
        for (int i = 0; i < segmentedList.size(); i++) { // 遍历节点列表
            if (Objects.equals(segmentedBaseList.get(i).getDialog(), ExternalKeysNode.DIALOG_NUMBERKEYBOARD)) {
                List<ExternalKeysNode> numKeyList = segmentedBaseList.get(i).getChildNodes();
                for (int j = 0; j < numKeyList.size(); j++) {
                    numKeyList.get(j).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, j));
                }
            } else {
                if (segmentedBaseList.get(i).getChildNodes() != null) {
                    List<ExternalKeysNode> childNodes = segmentedBaseList.get(i).getChildNodes();
                    for (int j = 0; j < childNodes.size(); j++) {
                        if (Objects.equals(childNodes.get(j).getDialog(), ExternalKeysNode.DIALOG_NUMBERKEYBOARD)) {
                            List<ExternalKeysNode> numKeyList = childNodes.get(j).getChildNodes();
                            for (int k = 0; k < numKeyList.size(); k++) {
                                numKeyList.get(k).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_10, k));
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * 更新中心菜单区域节点的位置和可见性
     */
    private void setCenterMenu() {
//        if (centerMenuLayout == null) {
//            centerMenuLayout = (MainLayoutCenterMenu) mainViewGroup.findViewById(R.id.mainLayoutCenterMenu);
//        }
//        int positionX = (int) centerMenuLayout.getX();
//        int positionY = (int) centerMenuLayout.getY();
//        List<ExternalKeysNode> tmpList = ExternalKeysNodeUtil.getBottomSlipNode().get(4).getChildNodes();
//        List<ExternalKeysNode> centerMenuList = getBottomBaseList().get(4).getChildNodes();
//        for (int i = 0; i < centerMenuList.size(); i++) {
//            centerMenuList.get(i).setX(positionX + tmpList.get(i).getX());
//            centerMenuList.get(i).setY(positionY + tmpList.get(i).getY());
//        }
//        bottomList = centerMenuList;
//        bottomIndex = centerMenuList.get(LIST_HEAD).getCurListSelect();
//        if (centerMenuLayout.getVisibility() == View.VISIBLE &&
//                focusView != null && focusView.getVisibility() == View.VISIBLE) {
//            setViewPlace(bottomList, bottomIndex, true);
//        } else {
//            if (focusView != null) {
//                setFocusViewVisible(View.GONE);
//                Logger.i("ExternalKeysManager,focusControl,setCenterChannels:" + View.GONE);
//            }
//            bottomList.get(LIST_HEAD).setCurListSelect(bottomIndex);
//        }
    }

    /**
     * 设置串行总线文本视图区域节点的位置
     * @param serialsNumber 串行总线编号
     */
    private void setRightSerialsTextView(int serialsNumber) {
        List<ExternalKeysNode> list = getRightSerialsBaseList(serialsNumber);
        List<ExternalKeysNode> serialsLins = list.get(RightLayoutSerials.SERIALS_LIN + 1).getChildNodes();
        int linIndex = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_BAUDRATE + serialsNumber); // 从缓存读取整数值
//        serialsLins.get(serialsLins.size() - 1).setVisible(linIndex == 3);
        List<ExternalKeysNode> serialsCans = list.get(RightLayoutSerials.SERIALS_CAN + 1).getChildNodes();
        int canIndex = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_BAUDRATE + serialsNumber); // 从缓存读取整数值
//        serialsCans.get(serialsCans.size() - 1).setVisible(canIndex == 9);
    }

    /**
     * layout on triggerSerialsType，拼接topSlipTrigger下的s1、s2的列表种�?
     */
    private void setTriggerSerialsType(int serialsNumber) {
        List<ExternalKeysNode> topSlipNodes = ExternalKeysNodeUtil.getTopSlipNode();
        //变更本次修改的列�?
        int triggerDetailIndex = TopLayoutTrigger.DETAIL_S1;
        if (serialsNumber == CacheUtil.S1) {
            triggerDetailIndex = TopLayoutTrigger.DETAIL_S1;
        } else if (serialsNumber == CacheUtil.S2) {
            triggerDetailIndex = TopLayoutTrigger.DETAIL_S2;
        } else if (serialsNumber == CacheUtil.S3) {
            triggerDetailIndex = TopLayoutTrigger.DETAIL_S3;
        } else if (serialsNumber == CacheUtil.S4) {
            triggerDetailIndex = TopLayoutTrigger.DETAIL_S4;
        }
        int serials = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber); // 从缓存读取整数值
        List<ExternalKeysNode> childNodes = topSlipNodes.get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes()
                .get(triggerDetailIndex).getChildNodes().get(serials).getChildNodes();
        List<ExternalKeysNode> parents = getTopBaseList().get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes();
        parents.get(triggerDetailIndex).setChildNodes(childNodes); // 设置子节点列表
        for (int i = 0; i < childNodes.size(); i++) {
            childNodes.get(i).setParentNode(parents.get(triggerDetailIndex)); // 设置父节点
            childNodes.get(i).setParentNodes(parents);
        }
        //如果当前预�?�框位置正好在改变的列表上，则修改当前的预�?�框位置为修改后列表的�?�中�?
        ExternalKeysNode parentNode = topList.get(LIST_HEAD).getParentNode();
        if (parentNode != null && ExternalKeysNode.TYPE_TRIGGER_TITLE.equals(parentNode.getType()) // 判断是否为触发菜单滑动类型节点
                && ((parentNode.getIndex() == TopLayoutTrigger.DETAIL_S1 && (serialsNumber == CacheUtil.S1))
                || (parentNode.getIndex() == TopLayoutTrigger.DETAIL_S2 && (serialsNumber == CacheUtil.S2))
                || (parentNode.getIndex() == TopLayoutTrigger.DETAIL_S3 && (serialsNumber == CacheUtil.S3))
                || (parentNode.getIndex() == TopLayoutTrigger.DETAIL_S4 && (serialsNumber == CacheUtil.S4)))) {
            topList = topList.get(LIST_HEAD).getParentNode().getChildNodes();
            topIndex = topList.get(LIST_HEAD).getCurListSelect();
            if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                setViewPlace(topList, topIndex, true); // 更新焦点框位置
            }
        } else if (parentNode != null) {
            //如果当前预�?�框位置正好在改变的列表的子列表上，则修改当前的预�?�框位置为修改后列表的�?�中�?
            parentNode = parentNode.getParentNode();
            if (parentNode != null && ExternalKeysNode.TYPE_TRIGGER_TITLE.equals(parentNode.getType()) // 判断是否为触发菜单滑动类型节点
                    && ((parentNode.getIndex() == TopLayoutTrigger.DETAIL_S1 && (serialsNumber == CacheUtil.S1))
                    || (parentNode.getIndex() == TopLayoutTrigger.DETAIL_S2 && (serialsNumber == CacheUtil.S2))
                    || (parentNode.getIndex() == TopLayoutTrigger.DETAIL_S3 && (serialsNumber == CacheUtil.S3))
                    || (parentNode.getIndex() == TopLayoutTrigger.DETAIL_S4 && (serialsNumber == CacheUtil.S4)))) {
                topList = topList.get(LIST_HEAD).getParentNode().getParentNode().getChildNodes();
                topIndex = topList.get(LIST_HEAD).getCurListSelect();
                if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                        && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                    setViewPlace(topList, topIndex, true); // 更新焦点框位置
                }
            }
        }
    }

    /**
     * layout on triggerSerialsChildren，修改topSlipTrigger下的s1、s2的列表中单项的显现�??
     */
    private void setTriggerSerialsChildren(int serialsNumber) {
        int triggerDetailIndex = TopLayoutTrigger.DETAIL_S1;
        if (serialsNumber == CacheUtil.S1) {
            triggerDetailIndex = TopLayoutTrigger.DETAIL_S1;
        } else if (serialsNumber == CacheUtil.S2) {
            triggerDetailIndex = TopLayoutTrigger.DETAIL_S2;
        } else if (serialsNumber == CacheUtil.S3) {
            triggerDetailIndex = TopLayoutTrigger.DETAIL_S3;
        } else if (serialsNumber == CacheUtil.S4) {
            triggerDetailIndex = TopLayoutTrigger.DETAIL_S4;
        }
        List<ExternalKeysNode> serialsList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes().get(triggerDetailIndex).getChildNodes();
        int serialsType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber); // 从缓存读取整数值
        switch (serialsType) { // switch分支选择
            case RightLayoutSerials.SERIALS_UART:
                int uartCheck = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_CHECK + serialsNumber); // 从缓存读取整数值
                int uartBit = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_BITS + serialsNumber); // 从缓存读取整数值
                serialsList.get(2).setVisible(uartBit != 4);
                serialsList.get(3).setVisible(uartBit == 4);
                serialsList.get(4).setVisible(uartBit == 4);
                serialsList.get(5).setVisible(uartBit == 4);
                serialsList.get(6).setVisible(uartCheck != 0);
                break; // 跳出循环
            case RightLayoutSerials.SERIALS_SPI:
                boolean csSwitch = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CSSWITCH + serialsNumber); // 从缓存读取布尔值
                serialsList.get(0).setVisible(csSwitch);
                break; // 跳出循环
            case RightLayoutSerials.SERIALS_M429:
                int m429Format = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_FORMAT + serialsNumber); // 从缓存读取整数值
                switch (m429Format) { // switch分支选择
                    case 0:
                        serialsList.get(3).setVisible(false); // 设置不可见
                        serialsList.get(5).setVisible(false); // 设置不可见
                        serialsList.get(6).setVisible(false); // 设置不可见
                        serialsList.get(8).setVisible(false); // 设置不可见
                        break; // 跳出循环
                    case 1:
                        serialsList.get(3).setVisible(false); // 设置不可见
                        serialsList.get(5).setVisible(true); // 设置可见
                        serialsList.get(6).setVisible(false); // 设置不可见
                        serialsList.get(8).setVisible(true); // 设置可见
                        break; // 跳出循环
                    default: // 默认分支
                        serialsList.get(3).setVisible(true); // 设置可见
                        serialsList.get(5).setVisible(true); // 设置可见
                        serialsList.get(6).setVisible(true); // 设置可见
                        serialsList.get(8).setVisible(true); // 设置可见
                        break; // 跳出循环
                }
                break; // 跳出循环
        }
        //如果当前预选框位置正好在改变的列表上，则修改当前的预选框位置使之可用
        int curListSelect = serialsList.get(LIST_HEAD).getCurListSelect();
        while (!serialsList.get(curListSelect).isVisible()) {
            curListSelect = curListSelect != 0 ? curListSelect - 1 : serialsList.size() - 1;
        }
        serialsList.get(LIST_HEAD).setCurListSelect(curListSelect); // 设置列表选中项

        if (topList.get(LIST_HEAD).getParentNodes() != null && topList.get(LIST_HEAD).getParentNodes() == serialsList) {
            topList = serialsList;
            topIndex = curListSelect;
        }

        ExternalKeysNode parentNode = topList.get(LIST_HEAD).getParentNode();
        if (parentNode != null && ExternalKeysNode.TYPE_TRIGGER_TITLE.equals(parentNode.getType()) // 判断是否为触发菜单滑动类型节点
                && ((parentNode.getIndex() == TopLayoutTrigger.DETAIL_S1 && (serialsNumber == CacheUtil.S1))
                || (parentNode.getIndex() == TopLayoutTrigger.DETAIL_S2 && (serialsNumber == CacheUtil.S2))
                || (parentNode.getIndex() == TopLayoutTrigger.DETAIL_S3 && (serialsNumber == CacheUtil.S3))
                || (parentNode.getIndex() == TopLayoutTrigger.DETAIL_S4 && (serialsNumber == CacheUtil.S4)))) {
            topList = topList.get(LIST_HEAD).getParentNode().getChildNodes();
            while (!((topList.size() - 1 >= topIndex) && topList.get(topIndex).isVisible())) {
                topIndex = topIndex != 0 ? topIndex - 1 : topList.size() - 1;
            }
            if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                    && focusView != null && focusView.getVisibility() == View.VISIBLE
                    && mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP)) { // 判断顶部菜单是否显示
                setViewPlace(topList, topIndex, true); // 更新焦点框位置
            } else {
                topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
            }
        }
    }

    /**
     * 设置topTriggerSerials下的数字键盘弹出框中的按键的显现�?
     */
    private void setTriggerSerialsNumberKeyBoard(int serialsNumber) {
//        int serialsNumber = isSerials1 ? RightMsgSerials.SERIALS_S1 : RightMsgSerials.SERIALS_S2;

        int triggerDetailIndex = TopLayoutTrigger.DETAIL_S1;
        if (serialsNumber == CacheUtil.S1) {
            triggerDetailIndex = TopLayoutTrigger.DETAIL_S1;
        } else if (serialsNumber == CacheUtil.S2) {
            triggerDetailIndex = TopLayoutTrigger.DETAIL_S2;
        } else if (serialsNumber == CacheUtil.S3) {
            triggerDetailIndex = TopLayoutTrigger.DETAIL_S3;
        } else if (serialsNumber == CacheUtil.S4) {
            triggerDetailIndex = TopLayoutTrigger.DETAIL_S4;
        }
//        List<ExternalKeysNode> topBaseList = getTopBaseList();
//        ExternalKeysNode triggerNode = topBaseList.get(TopLayoutPopWindow.DETAIL_TRIGGER);
//        List<ExternalKeysNode> triggerChild = triggerNode.getChildNodes();
//        ExternalKeysNode triggerDetail = triggerChild.get(triggerDetailIndex);
//        List<ExternalKeysNode> serialsList = triggerDetail.getChildNodes();
        List<ExternalKeysNode> serialsList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes().get(triggerDetailIndex).getChildNodes();
        int serialsType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + serialsNumber); // 从缓存读取整数值
        switch (serialsType) { // switch分支选择
            case RightLayoutSerials.SERIALS_UART: {
                int uartDisplay = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_DISPLAY + serialsNumber); // 从缓存读取整数值
                int digits = uartDisplay == 1 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                for (int i = 2; i <= 5; i++) {
                    List<ExternalKeysNode> nodeList = serialsList.get(i).getChildNodes();
                    List<ExternalKeysNode> numKeyList = nodeList.get(nodeList.size() - 1).getChildNodes();
                    for (int j = 0; j < numKeyList.size(); j++) {
                        numKeyList.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                    }
                    setListSelectVisible(numKeyList);
                }
            }
            break; // 跳出循环
            case RightLayoutSerials.SERIALS_LIN: {
                int digits = IDigits.DIGITS_16X;
                List<ExternalKeysNode> numKeyList10 = serialsList.get(1).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList10.size(); j++) {
                    numKeyList10.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList10);
                List<ExternalKeysNode> numKeyList20 = serialsList.get(2).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList20.size(); j++) {
                    numKeyList20.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList20);
                List<ExternalKeysNode> numKeyList21 = serialsList.get(2).getChildNodes().get(1).getChildNodes();
                for (int j = 0; j < numKeyList21.size(); j++) {
                    numKeyList21.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList21);
            }
            break; // 跳出循环
            case RightLayoutSerials.SERIALS_CAN: {
//                Log.d("Tag.Debug", String.format("setTriggerSerialsNumberKeyBoard: %d",1 ));
//                int digits = IDigits.DIGITS_16;
//                List<ExternalKeysNode> numKeyList10 = serialsList.get(1).getChildNodes().get(0).getChildNodes();
//                for (int j = 0; j < numKeyList10.size(); j++) {
//                    numKeyList10.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
//                }
//                setListSelectVisible(numKeyList10);
//                List<ExternalKeysNode> numKeyList20 = serialsList.get(2).getChildNodes().get(0).getChildNodes();
//                for (int j = 0; j < numKeyList20.size(); j++) {
//                    numKeyList20.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
//                }
//                setListSelectVisible(numKeyList20);
//                List<ExternalKeysNode> numKeyList30 = serialsList.get(3).getChildNodes().get(0).getChildNodes();
//                for (int j = 0; j < numKeyList30.size(); j++) {
//                    numKeyList30.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
//                }
//                setListSelectVisible(numKeyList30);
//                List<ExternalKeysNode> numKeyList40 = serialsList.get(4).getChildNodes().get(0).getChildNodes();
//                for (int j = 0; j < numKeyList40.size(); j++) {
//                    numKeyList40.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
//                }
//                setListSelectVisible(numKeyList40);
//                List<ExternalKeysNode> numKeyList41 = serialsList.get(4).getChildNodes().get(1).getChildNodes();
//                for (int j = 0; j < numKeyList41.size(); j++) {
//                    numKeyList41.get(j).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_0_8, j));
//                }
//                setListSelectVisible(numKeyList41);
//                List<ExternalKeysNode> numKeyList42 = serialsList.get(4).getChildNodes().get(2).getChildNodes();
//                for (int j = 0; j < numKeyList42.size(); j++) {
//                    numKeyList42.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
//                }
//                setListSelectVisible(numKeyList42);
            }
            break; // 跳出循环
            case RightLayoutSerials.SERIALS_SPI: {
                List<ExternalKeysNode> numKeyList = serialsList.get(1).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList.size(); j++) {
                    numKeyList.get(j).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_2X, j));
                }
                setListSelectVisible(numKeyList);
            }
            break; // 跳出循环
            case RightLayoutSerials.SERIALS_I2C: {
                int digits = IDigits.DIGITS_16;
                List<ExternalKeysNode> numKeyList40 = serialsList.get(4).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList40.size(); j++) {
                    numKeyList40.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList40);
                List<ExternalKeysNode> numKeyList50 = serialsList.get(5).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList50.size(); j++) {
                    numKeyList50.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList50);
                List<ExternalKeysNode> numKeyList51 = serialsList.get(5).getChildNodes().get(1).getChildNodes();
                for (int j = 0; j < numKeyList51.size(); j++) {
                    numKeyList51.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList51);
                List<ExternalKeysNode> numKeyList60 = serialsList.get(6).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList60.size(); j++) {
                    numKeyList60.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList60);
                List<ExternalKeysNode> numKeyList61 = serialsList.get(6).getChildNodes().get(1).getChildNodes();
                for (int j = 0; j < numKeyList61.size(); j++) {
                    numKeyList61.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList61);
                List<ExternalKeysNode> numKeyList62 = serialsList.get(6).getChildNodes().get(2).getChildNodes();
                for (int j = 0; j < numKeyList62.size(); j++) {
                    numKeyList62.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList62);
                List<ExternalKeysNode> numKeyList74 = serialsList.get(7).getChildNodes().get(4).getChildNodes();
                for (int j = 0; j < numKeyList74.size(); j++) {
                    numKeyList74.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList74);
                List<ExternalKeysNode> numKeyList80 = serialsList.get(8).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList80.size(); j++) {
                    numKeyList80.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList80);
                List<ExternalKeysNode> numKeyList81 = serialsList.get(8).getChildNodes().get(1).getChildNodes();
                for (int j = 0; j < numKeyList81.size(); j++) {
                    numKeyList81.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList81);
            }
            break; // 跳出循环
            case RightLayoutSerials.SERIALS_M429: {
                int m429Display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_DISPLAY + serialsNumber); // 从缓存读取整数值
                int digits = m429Display == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                List<ExternalKeysNode> numKeyList20 = serialsList.get(2).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList20.size(); j++) {
                    numKeyList20.get(j).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_8, j));
                }
                setListSelectVisible(numKeyList20);
                List<ExternalKeysNode> numKeyList30 = serialsList.get(3).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList30.size(); j++) {
                    numKeyList30.get(j).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_2, j));
                }
                setListSelectVisible(numKeyList30);
                List<ExternalKeysNode> numKeyList40 = serialsList.get(4).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList40.size(); j++) {
                    numKeyList40.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList40);
                List<ExternalKeysNode> numKeyList50 = serialsList.get(5).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList50.size(); j++) {
                    numKeyList50.get(j).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_2, j));
                }
                setListSelectVisible(numKeyList50);
                List<ExternalKeysNode> numKeyList60 = serialsList.get(6).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList60.size(); j++) {
                    numKeyList60.get(j).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_8, j));
                }
                setListSelectVisible(numKeyList60);
                List<ExternalKeysNode> numKeyList61 = serialsList.get(6).getChildNodes().get(1).getChildNodes();
                for (int j = 0; j < numKeyList61.size(); j++) {
                    numKeyList61.get(j).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_2, j));
                }
                setListSelectVisible(numKeyList61);
                List<ExternalKeysNode> numKeyList70 = serialsList.get(7).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList70.size(); j++) {
                    numKeyList70.get(j).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_8, j));
                }
                setListSelectVisible(numKeyList70);
                List<ExternalKeysNode> numKeyList71 = serialsList.get(7).getChildNodes().get(1).getChildNodes();
                for (int j = 0; j < numKeyList71.size(); j++) {
                    numKeyList71.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList71);
                List<ExternalKeysNode> numKeyList80 = serialsList.get(8).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList80.size(); j++) {
                    numKeyList80.get(j).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_8, j));
                }
                setListSelectVisible(numKeyList80);
                List<ExternalKeysNode> numKeyList81 = serialsList.get(8).getChildNodes().get(1).getChildNodes();
                for (int j = 0; j < numKeyList81.size(); j++) {
                    numKeyList81.get(j).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_2, j));
                }
                setListSelectVisible(numKeyList81);
            }
            break; // 跳出循环
            case RightLayoutSerials.SERIALS_M1553B: {
                int m429Display = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_DISPLAY + serialsNumber); // 从缓存读取整数值
                int digits = m429Display == 0 ? IDigits.DIGITS_2 : IDigits.DIGITS_16;
                List<ExternalKeysNode> numKeyList20 = serialsList.get(2).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList20.size(); j++) {
                    numKeyList20.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList20);
                List<ExternalKeysNode> numKeyList30 = serialsList.get(3).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList30.size(); j++) {
                    numKeyList30.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList30);
                List<ExternalKeysNode> numKeyList50 = serialsList.get(5).getChildNodes().get(0).getChildNodes();
                for (int j = 0; j < numKeyList50.size(); j++) {
                    numKeyList50.get(j).setVisible(KeyBoardNumberUtil.isEnabled(digits, j));
                }
                setListSelectVisible(numKeyList50);
            }
            break; // 跳出循环
        }
    }

    /**
     * 设置RightMath下的数字键盘弹出框中的按键的显现�?
     */
    private void setRightMathNumberKeyBoard() {

    }

    /**
     * 设置rightSerials下的数字键盘弹出框中的按键的显现�?
     */
    private void setRightSerialsNumberKeyBoard() {
        int indexUartDialog;
        int indexLinDialog;
        int indexCanDialog;
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_2) {
            indexUartDialog = 12;
            indexLinDialog = 8;
            indexCanDialog = 17;
        } else {
            indexUartDialog = 14;
            indexLinDialog = 10;
            indexCanDialog = 19;
        }

        List<ExternalKeysNode> rightS1BaseList = getRightSerialsBaseList(CacheUtil.S1);
        List<ExternalKeysNode> numKeyList = rightS1BaseList.get(RightLayoutSerials.SERIALS_UART + 1)
                .getChildNodes().get(indexUartDialog).getChildNodes().get(10).getChildNodes();
        for (int i = 0; i < numKeyList.size(); i++) { // 遍历节点列表
            numKeyList.get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_BAUDRATE, i));
        }
        List<ExternalKeysNode> numKeyList2 = rightS1BaseList.get(RightLayoutSerials.SERIALS_LIN + 1)
                .getChildNodes().get(indexLinDialog).getChildNodes();
        for (int i = 0; i < numKeyList2.size(); i++) {
            numKeyList2.get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_BAUDRATE, i));
        }
        List<ExternalKeysNode> numKeyList3 = rightS1BaseList.get(RightLayoutSerials.SERIALS_CAN + 1)
                .getChildNodes().get(indexCanDialog).getChildNodes();
        for (int i = 0; i < numKeyList3.size(); i++) {
            numKeyList3.get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_BAUDRATE, i));
        }

        List<ExternalKeysNode> rightS2BaseList = getRightSerialsBaseList(CacheUtil.S2);
        List<ExternalKeysNode> numKeyList4 = rightS2BaseList.get(RightLayoutSerials.SERIALS_UART + 1)
                .getChildNodes().get(indexUartDialog).getChildNodes().get(10).getChildNodes();
        for (int i = 0; i < numKeyList4.size(); i++) {
            numKeyList4.get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_BAUDRATE, i));
        }
        List<ExternalKeysNode> numKeyList5 = rightS2BaseList.get(RightLayoutSerials.SERIALS_LIN + 1)
                .getChildNodes().get(indexLinDialog).getChildNodes();
        for (int i = 0; i < numKeyList5.size(); i++) {
            numKeyList5.get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_BAUDRATE, i));
        }
        List<ExternalKeysNode> numKeyList6 = rightS2BaseList.get(RightLayoutSerials.SERIALS_CAN + 1)
                .getChildNodes().get(indexCanDialog).getChildNodes();
        for (int i = 0; i < numKeyList6.size(); i++) {
            numKeyList6.get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_BAUDRATE, i));
        }

        List<ExternalKeysNode> rightS3BaseList = getRightSerialsBaseList(CacheUtil.S3);
        List<ExternalKeysNode> numKeyList7 = rightS3BaseList.get(RightLayoutSerials.SERIALS_UART + 1)
                .getChildNodes().get(indexUartDialog).getChildNodes().get(10).getChildNodes();
        for (int i = 0; i < numKeyList7.size(); i++) {
            numKeyList7.get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_BAUDRATE, i));
        }
        List<ExternalKeysNode> numKeyList8 = rightS3BaseList.get(RightLayoutSerials.SERIALS_LIN + 1)
                .getChildNodes().get(indexLinDialog).getChildNodes();
        for (int i = 0; i < numKeyList8.size(); i++) {
            numKeyList8.get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_BAUDRATE, i));
        }
        List<ExternalKeysNode> numKeyList9 = rightS3BaseList.get(RightLayoutSerials.SERIALS_CAN + 1)
                .getChildNodes().get(indexCanDialog).getChildNodes();
        for (int i = 0; i < numKeyList9.size(); i++) {
            numKeyList9.get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_BAUDRATE, i));
        }

        List<ExternalKeysNode> rightS4BaseList = getRightSerialsBaseList(CacheUtil.S4);
        List<ExternalKeysNode> numKeyList10 = rightS4BaseList.get(RightLayoutSerials.SERIALS_UART + 1)
                .getChildNodes().get(indexUartDialog).getChildNodes().get(10).getChildNodes();
        for (int i = 0; i < numKeyList10.size(); i++) {
            numKeyList10.get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_BAUDRATE, i));
        }
        List<ExternalKeysNode> numKeyList11 = rightS4BaseList.get(RightLayoutSerials.SERIALS_LIN + 1)
                .getChildNodes().get(indexLinDialog).getChildNodes();
        for (int i = 0; i < numKeyList11.size(); i++) {
            numKeyList11.get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_BAUDRATE, i));
        }
        List<ExternalKeysNode> numKeyList12 = rightS4BaseList.get(RightLayoutSerials.SERIALS_CAN + 1)
                .getChildNodes().get(indexCanDialog).getChildNodes();
        for (int i = 0; i < numKeyList12.size(); i++) {
            numKeyList12.get(i).setVisible(KeyBoardNumberUtil.isEnabled(IDigits.DIGITS_BAUDRATE, i));
        }

    }

    /**
     * 设置SPI串行总线片选(CS)的节点位置和可见性
     * @param serialsNumber 串行总线编号
     */
    private void setRightSerialsSpiCs(int serialsNumber) {
        List<ExternalKeysNode> spiList = getRightSerialsBaseList(serialsNumber).
                get(RightLayoutSerials.SERIALS_SPI + 5).getChildNodes();
        boolean csSwitch = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CSSWITCH + serialsNumber); // 从缓存读取布尔值
        Logger.d("spiList.size= " + spiList.size());
        if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_4) {
            for (int i = 13; i <= 18; i++) { // CLK 2+4, Data 2+4
                spiList.get(i).setVisible(csSwitch);
            }
        } else if (GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8) {
            for (int i = 21; i <= 30; i++) {//前面有：CLK 2+8, Data 2+8
                spiList.get(i).setVisible(csSwitch);
            }
        }
    }

    /**
     * 设置list的预选项为可见的选项
     */
    private void setListSelectVisible(List<ExternalKeysNode> list) {
        if (!list.get(list.get(LIST_HEAD).getCurListSelect()).isVisible()) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).isVisible()) {
                    list.get(LIST_HEAD).setCurListSelect(i);
                    break; // 跳出循环
                }
            }
        }
    }

    /**
     * layout on triggerVideo
     */
    private void setTriggerVideo() {
        List<ExternalKeysNode> videos = ExternalKeysNodeUtil.getTopSlipNode().get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes().get(TopLayoutTrigger.DETAIL_VIDEO).getChildNodes();
        int standard = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_STANDARD); // 从缓存读取整数值
        int trigger = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_VIDEO_TRIGGER + standard); // 从缓存读取整数值
        //设置第三行的排列
        int standardFirst = 0;
        for (int i = 0; i < videos.size(); i++) {
            if (ExternalKeysNode.TYPE_TRIGGER_VIDEO_STANDARD_FIRST.equals(videos.get(i).getType())) { // 判断是否为视频触发标准类型节点
                standardFirst = i;
                break; // 跳出循环
            }
        }
        videos.addAll(videos.get(standardFirst + standard).getChildNodes());
        for (int i = 0; i < standardFirst; i++) {
            videos.get(standardFirst + i).setChildNodes(null); // 设置子节点列表
        }
        List<ExternalKeysNode> parents = getTopBaseList().get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes();
        parents.get(TopLayoutTrigger.DETAIL_VIDEO).setChildNodes(videos); // 设置子节点列表
        for (int i = 0; i < videos.size(); i++) {
            videos.get(i).setParentNode(parents.get(TopLayoutTrigger.DETAIL_VIDEO)); // 设置父节点
            videos.get(i).setParentNodes(parents);
        }
        //设置line的显现�??
        for (int i = 0; i < videos.size(); i++) {
            if (ExternalKeysNode.TYPE_TRIGGER_VIDEO_LINE.equals(videos.get(i).getType())) { // 判断是否为视频触发行类型节点
                if (standard == 3 || standard == 5) {
                    videos.get(i).setVisible(trigger == 2);
                } else {
                    videos.get(i).setVisible(trigger == 4);
                }
                break; // 跳出循环
            }
        }
    }

    /**
     * timeRange on TriggerSlope
     */
    private void setTriggerSlope() {
        List<ExternalKeysNode> slopes = getTopBaseList().get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes().get(TopLayoutTrigger.DETAIL_SLOPE).getChildNodes();
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SLOPE_CONDITION); // 从缓存读取整数值
        slopes.get(slopes.size() - 2).setVisible(condition == 0 || condition == 2);
        slopes.get(slopes.size() - 1).setVisible(condition == 1 || condition == 2);
    }

    /**
     * timeRange on TriggerRunt
     */
    private void setTriggerRunt() {
        List<ExternalKeysNode> runts = getTopBaseList().get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes().get(TopLayoutTrigger.DETAIL_RUNT).getChildNodes();
        int condition = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_RUNT_CONDITION); // 从缓存读取整数值
        runts.get(runts.size() - 2).setVisible(condition == 0 || condition == 2);
        runts.get(runts.size() - 1).setVisible(condition == 1 || condition == 2);
    }

    /**
     * channelList on measure and save
     */
    private void setChannelList() {
        Log.e(TAG, "setChannelList: ---------------------------------------------");
        //ch1...ch8 + math1...math8 + r1...r8
        boolean[] channelShow = {
                false, false, false, false, false, false, false, false,
                false, false, false, false, false, false, false, false,
                false, false, false, false, false, false, false, false,
        };

        boolean[] channelShowForBin = {
                false, false, false, false, false, false, false, false,
        };
        TChan.foreachChan(chan -> {
            IChannel channel = ChannelFactory.getValidChannel(TChan.toFpgaChNo(chan));
            if (channel != null && TChan.toFpgaChNo(chan) < channelShow.length) {
                channelShow[TChan.toFpgaChNo(chan)] = channel.isOpen();
                channelShowForBin[TChan.toFpgaChNo(chan)] = channel.isOpen();
            }
        });

        TChan.foreachMath(mathChan -> {
            IChannel channel = ChannelFactory.getValidChannel(TChan.toFpgaChNo(mathChan));
            if (channel != null && TChan.toFpgaChNo(mathChan) < channelShow.length) {
                channelShow[TChan.toFpgaChNo(mathChan)] = channel.isOpen();
            }
        });

        TChan.foreachRef(refChan -> {
            IChannel channel = ChannelFactory.getValidChannel(TChan.toFpgaChNo(refChan));
            if (channel != null && TChan.toFpgaChNo(refChan) < channelShow.length) {
                channelShow[TChan.toFpgaChNo(refChan)] = channel.isOpen();
            }
        });

        boolean isList = false;
        int offsetX=30;//20
        int interval=30;
        int saveChannelX = 0;

        List<ExternalKeysNode> list = getTopBaseList();

        List<ExternalKeysNode> measureList = ExternalKeysNodeUtil.getTopSlipNode().get(TopLayoutPopWindow.DETAIL_MEASURE).getChildNodes()
                .get(TopLayoutMeasure.DETAIL_COMMON).getChildNodes();

        //measureChannelList update 测量常规
        List<ExternalKeysNode> curMeasureList = list.get(TopLayoutPopWindow.DETAIL_MEASURE).getChildNodes()
                .get(TopLayoutMeasure.DETAIL_COMMON).getChildNodes();

        //saveChannelList update 保存波形
        List<ExternalKeysNode> curSaveList = list.get(TopLayoutPopWindow.DETAIL_SAVE).getChildNodes()
                .get(TopLayoutSave.DETAIL_STORE).getChildNodes()
                .get(TopLayoutSaveStore.DETAIL_WAV).getChildNodes();

        List<ExternalKeysNode> curCsvList = list.get(TopLayoutPopWindow.DETAIL_SAVE).getChildNodes()
                .get(TopLayoutSave.DETAIL_STORE).getChildNodes()
                .get(TopLayoutSaveStore.DETAIL_CSV).getChildNodes();

        List<ExternalKeysNode> autoSaveWavList = list.get(TopLayoutPopWindow.DETAIL_SAVE).getChildNodes()
                .get(TopLayoutSave.DETAIL_AUTOSAVE).getChildNodes();

        List<ExternalKeysNode> curBinList = list.get(TopLayoutPopWindow.DETAIL_SAVE).getChildNodes()
                .get(TopLayoutSave.DETAIL_STORE).getChildNodes()
                .get(TopLayoutSaveStore.DETAIL_BIN).getChildNodes();

        // measure setting update 测量设置
        List<ExternalKeysNode> measureSettingList = list.get(TopLayoutPopWindow.DETAIL_MEASURE).getChildNodes()
                .get(TopLayoutMeasure.DETAIL_SETTING).getChildNodes();

        // cursor common update 光标常规
        List<ExternalKeysNode> cursorCommonList = list.get(TopLayoutPopWindow.DETAIL_CURSOR).getChildNodes()
                .get(TopLayoutCursor.DETAIL_COMMON).getChildNodes();

        int finalX= 0;
        for (int i = 0, offX = 0; i < channelShow.length; i++) {
            measureList.get(i).setVisible(channelShow[i]);
            if (channelShow[i]){
                measureList.get(i).setX(offsetX);
                offsetX+=measureList.get(i).getW()+interval;
            }

            //measure common list
            curMeasureList.get(i).setVisible(measureList.get(i).isVisible());
            curMeasureList.get(i).setX(measureList.get(i).getX());
            if(measureList.get(i).isVisible()) {
                finalX = measureList.get(i).getX();
                Logger.i("setChannelList finalx= " + finalX); // 信息日志输出
            }
            curMeasureList.get(i).setY(measureList.get(i).getY());
            if (!curMeasureList.get(curMeasureList.get(LIST_HEAD).getCurListSelect()).isVisible()) {
                curMeasureList.get(LIST_HEAD).setCurListSelect(0); // 设置列表选中项
                while (!curMeasureList.get(curMeasureList.get(LIST_HEAD).getCurListSelect()).isVisible()) {
                    curMeasureList.get(LIST_HEAD).setCurListSelect(curMeasureList.get(LIST_HEAD).getCurListSelect() + 1); // 设置列表选中项
                }
            }


            //save wave list
            if (i < curSaveList.size()) {
                curSaveList.get(i).setVisible(measureList.get(i).isVisible());
                curSaveList.get(i).setX(measureList.get(i).getX());

//                if (i == channelShow.length - 1) { //最后一个
//                    int type = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_TYPE);
//                    curSaveList.get(channelShow.length).setVisible(type == 1);
//                    Logger.i("setChannelList usefinalx= " + finalX + " ,xxx= " + (finalX + curSaveList.get(channelShow.length).getW() + interval) + " ,type= " + type);
//                    curSaveList.get(channelShow.length).setX(finalX + curSaveList.get(channelShow.length).getW() + interval);
//                }

                if (!curSaveList.get(curSaveList.get(LIST_HEAD).getCurListSelect()).isVisible()) {
                    curSaveList.get(LIST_HEAD).setCurListSelect(0); // 设置列表选中项
                    while (!curSaveList.get(curSaveList.get(LIST_HEAD).getCurListSelect()).isVisible()) {
                        curSaveList.get(LIST_HEAD).setCurListSelect(curSaveList.get(LIST_HEAD).getCurListSelect() + 1); // 设置列表选中项
                    }
                }
            }

            // save csv list
            if (i < channelShow.length) {
                curCsvList.get(i).setVisible(measureList.get(i).isVisible());
                curCsvList.get(i).setX(measureList.get(i).getX());
                if (i == channelShow.length - 1) { //最后一个
                    curCsvList.get(channelShow.length).setVisible(true); // 设置可见
                    curCsvList.get(channelShow.length).setX(finalX + curCsvList.get(channelShow.length).getW() + interval);
                }
                if (!curCsvList.get(curCsvList.get(LIST_HEAD).getCurListSelect()).isVisible()) {
                    curCsvList.get(LIST_HEAD).setCurListSelect(0); // 设置列表选中项
                    while (!curCsvList.get(curCsvList.get(LIST_HEAD).getCurListSelect()).isVisible()) {
                        curCsvList.get(LIST_HEAD).setCurListSelect(curCsvList.get(LIST_HEAD).getCurListSelect() + 1); // 设置列表选中项
                    }
                }
            }

            if (i < channelShow.length) {
                autoSaveWavList.get(i).setVisible(measureList.get(i).isVisible());
                autoSaveWavList.get(i).setX(measureList.get(i).getX());
                if (i == channelShow.length - 1) { //最后一个
                    autoSaveWavList.get(channelShow.length).setVisible(true); // 设置可见
                    autoSaveWavList.get(channelShow.length).setX(finalX + autoSaveWavList.get(channelShow.length).getW() + interval);
                }
                if (!autoSaveWavList.get(autoSaveWavList.get(LIST_HEAD).getCurListSelect()).isVisible()) {
                    autoSaveWavList.get(LIST_HEAD).setCurListSelect(0); // 设置列表选中项
                    while (!autoSaveWavList.get(autoSaveWavList.get(LIST_HEAD).getCurListSelect()).isVisible()) {
                        autoSaveWavList.get(LIST_HEAD).setCurListSelect(autoSaveWavList.get(LIST_HEAD).getCurListSelect() + 1); // 设置列表选中项
                    }
                }
            }

            // save bin list
            if (i < channelShowForBin.length) {
                curBinList.get(i).setVisible(measureList.get(i).isVisible());
                curBinList.get(i).setX(measureList.get(i).getX());
                if (!curBinList.get(curBinList.get(LIST_HEAD).getCurListSelect()).isVisible()) {
                    curBinList.get(LIST_HEAD).setCurListSelect(0); // 设置列表选中项
                    while (!curBinList.get(curBinList.get(LIST_HEAD).getCurListSelect()).isVisible()) {
                        curBinList.get(LIST_HEAD).setCurListSelect(curBinList.get(LIST_HEAD).getCurListSelect() + 1); // 设置列表选中项
                    }
                }
            }

            //measure setting list
            if ((i + 3) < measureSettingList.size()) {

                measureSettingList.get(i + 3).setVisible(measureList.get(i).isVisible());
                measureSettingList.get(i + 3).setX(measureList.get(i).getX());
//            if (!channelShow[i]) {
//                offX -= (measureList.get(i).getW()+20);
//            }
                if (!measureSettingList.get(measureSettingList.get(LIST_HEAD).getCurListSelect()).isVisible()) {
                    measureSettingList.get(LIST_HEAD).setCurListSelect(0); // 设置列表选中项
                    while (!measureSettingList.get(measureSettingList.get(LIST_HEAD).getCurListSelect()).isVisible()) {
                        measureSettingList.get(LIST_HEAD).setCurListSelect(measureSettingList.get(LIST_HEAD).getCurListSelect() + 1); // 设置列表选中项
                    }
                }
            }

            //cursor common list
            cursorCommonList.get(i).setVisible(measureList.get(i).isVisible());
            cursorCommonList.get(i).setX(measureList.get(i).getX());
            cursorCommonList.get(i).setW(cursorCommonList.get(i).getW());
//            if (i == cursorCommonList.size() - 1) { // default
//                cursorCommonList.get(cursorCommonList.size() - 1).setX(offsetX - cursorCommonList.get(i).getW() - interval - 15);
//            }
            if (!cursorCommonList.get(cursorCommonList.get(LIST_HEAD).getCurListSelect()).isVisible()) {
                cursorCommonList.get(LIST_HEAD).setCurListSelect(0); // 设置列表选中项
                while (!cursorCommonList.get(cursorCommonList.get(LIST_HEAD).getCurListSelect()).isVisible()) {
                    cursorCommonList.get(LIST_HEAD).setCurListSelect(cursorCommonList.get(LIST_HEAD).getCurListSelect() + 1); // 设置列表选中项
                }
            }

//            isList = Objects.equals(curMeasureList, topList) || Objects.equals(curSaveList, topList)
//                    || Objects.equals(measureSettingList,topList) || Objects.equals(cursorCommonList,topList);

            isList = Objects.equals(curMeasureList, topList)
                    || Objects.equals(curSaveList, topList)
                    || Objects.equals(curCsvList, topList)
                    || Objects.equals(curBinList, topList)
                    || Objects.equals(measureSettingList, topList)
                    || Objects.equals(cursorCommonList, topList);
        }

        cursorCommonList.get(cursorCommonList.size() -1).setX(offsetX + interval); //cursor default

        
        if (isList && !topList.get(topIndex).isVisible()) {//当前预�?�框正好位于刚删除的控件位置时的处理
            topIndex = 0; // 初始化索引为0
            while (!topList.get(topIndex).isVisible()) {
                topIndex++;
            }
            if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                setViewPlace(topList, topIndex, true); // 更新焦点框位置
            } else {
                topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
            }
        }

//        setTxtMixList();
    }

    /**
     * delMeasureList
     */
    private void setMeasureDelList() {
        List<ExternalKeysNode> measureList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_MEASURE).getChildNodes()
                .get(TopLayoutMeasure.DETAIL_COMMON).getChildNodes();
        String positionStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_INDEX);
        int maxSelectCount = GlobalVar.get().getMeasureItemCount();//20
        ArrayList<String> indexList = StrUtil.getListFromString(positionStrings, CacheUtil.MEASURE_SELECT_LIST_SLIP);

        String[] allMeasures = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.measures);
        int channelCount = ChannelFactory.CH_CNT + ChannelFactory.MATH_CNT + ChannelFactory.REF_CNT;

        int startIndex = allMeasures.length + channelCount;
        int endIndex = startIndex + maxSelectCount + 1; //20为delList
        for (int i = startIndex; i < endIndex; i++) {
            if (i >= startIndex + indexList.size() && i < endIndex) {
                measureList.get(i).setVisible(false); // 设置不可见
            } else {
//                measureList.get(i).setVisible(true);
                measureList.get(i).setVisible(false); // 设置不可见
            }
        }
        measureList.get(measureList.size() - 1).setVisible(true);//清除 按钮
        if (!topList.get(topIndex).isVisible()) {
            topIndex--;
            if (mainViewGroup.isSlipShow(MainViewGroup.TOPSLIP) // 判断顶部菜单是否显示
                    && focusView != null && focusView.getVisibility() == View.VISIBLE) {
                setViewPlace(topList, topIndex, true); // 更新焦点框位置
            } else {
                topList.get(LIST_HEAD).setCurListSelect(topIndex); // 设置列表选中项
            }
        }
    }

    /**
     * layout on Display TxtMix
     */
    private void setTxtMixList() {
        List<ExternalKeysNode> topBaseList = getTopBaseList();
        List<ExternalKeysNode> disPlayList = topBaseList.get(TopLayoutPopWindow.DETAIL_DISPLAY).getChildNodes();
        ExternalKeysNode txtMixNode = disPlayList.get(TopLayoutDisplay.DETAIL_TXT_MIX);

        boolean s1Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1); // 从缓存读取布尔值
        boolean s2Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2); // 从缓存读取布尔值
        boolean s3Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3); // 从缓存读取布尔值
        boolean s4Open = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4); // 从缓存读取布尔值
        boolean[] openList = {s1Open, s2Open, s3Open, s4Open};
        if (!s1Open && !s2Open && !s3Open && !s4Open) {
            //这个时候
            txtMixNode.setVisible(false); // 设置不可见
            txtMixNode.setChildNodes(null); // 设置子节点列表
        } else {
            txtMixNode.setVisible(true); // 设置可见
            List<ExternalKeysNode> txtMixList = txtMixNode.getChildNodes();
            if (txtMixList == null) {
                txtMixList = ExternalKeysNodeUtil.getTopDisplayTxtMixDetailNodeList(txtMixNode, disPlayList, topSlipOffset);
                txtMixNode.setChildNodes(txtMixList); // 设置子节点列表
            }
//            if (txtMixList == null) return;
            for (int i = 0; i < txtMixList.size(); i++) { // 遍历节点列表
                txtMixList.get(i).setVisible(openList[i]);
            }
        }
    }

    /**
     * 如果是上面菜单中，触发页面下，Serials页面下，can页面下，IdData页面下，data框的点击，则�?判断dlc框�?�来确定是否有子列表;
     * 只有是当前判断的框且dlc值为0时，是没有子列表返回false的，否则返回true;
     */
    private boolean hasChildrenInTopTriggerSerialsCanIdDataData() {
        if (topIndex != 2) return true;
        if (topList.size() != 3) return true;
        if (topList.get(LIST_HEAD).getParentNode() == null) return true;
        if (topList.get(LIST_HEAD).getParentNode().getIndex() != 4) return true;
        if (topList.get(LIST_HEAD).getParentNode().getParentNode() == null) return true;
        ExternalKeysNode triggerNode = topList.get(LIST_HEAD).getParentNode().getParentNode();
        List<ExternalKeysNode> triggerList = getTopBaseList().get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes();
        if (triggerNode.getName().equals(triggerList.get(TopLayoutTrigger.DETAIL_S1).getName())) {
            int dlc = Integer.parseInt(CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC + 1));
            return dlc != 0;
        } else if (triggerNode.getName().equals(triggerList.get(TopLayoutTrigger.DETAIL_S2).getName())) {
            int dlc = Integer.parseInt(CacheUtil.get().getString(CacheUtil.TOP_SLIP_TRIGGER_SERIALS_CAN_IDDATA_DLC + 2));
            return dlc != 0;
        }
        return true; // 返回true
    }

    /**
     * 当�?�道全部关闭时，或�?�择的�?�道为参考波形时，测量中相位功能与延时功能的弹框被禁�?
     *
     * @return 禁用返回false；正常为true
     */
    private boolean hasChildrenInTopMeasureDelayAndPhase() {
        boolean bRet = true;
        if (ExternalKeysNode.DIALOG_MEASUREDELAY.equals(topList.get(topIndex).getDialog())
                || ExternalKeysNode.DIALOG_MEASUREPHASE.equals(topList.get(topIndex).getDialog())
                || ExternalKeysNode.DIALOG_MEASURETVALUE.equals(topList.get(topIndex).getDialog())) {
            if (!ChannelFactory.isChOpen(ChannelFactory.getChActivate())) {
                bRet = false;
            } else if ((ChannelFactory.isRefCh(CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_CHANNEL_SELECT)))) { // 从缓存读取整数值
                bRet = false;
            } else if (isMeasureDelaySelect() || isMeasurePhaseSelect() || isMeasureTValueSelect()) {
                return false; // 返回false
            }
        }
        return bRet;
    }


    /**
     * isMeasureDelaySelect方法
     */
    private boolean isMeasureDelaySelect() {
        boolean isDelaySelect = false;
        String channelStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_CHANNEL);
        String positionStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_INDEX);
        int channelSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_CHANNEL_SELECT); // 从缓存读取整数值
        ArrayList<String> channelList = StrUtil.getListFromString(channelStrings, CacheUtil.MEASURE_SELECT_LIST_SLIP);
        ArrayList<String> indexList = StrUtil.getListFromString(positionStrings, CacheUtil.MEASURE_SELECT_LIST_SLIP);
        for (int i = 0; i < channelList.size(); i++) { // 遍历节点列表
            if(channelSelect + 1 == Integer.parseInt(channelList.get(i))) {
                if(Integer.parseInt(indexList.get(i)) == 4) { //延迟
                    isDelaySelect = true;
                    break; // 跳出循环
                }
            }
        }
        return isDelaySelect;
    }

    /**
     * isMeasurePhaseSelect方法
     */
    private boolean isMeasurePhaseSelect() {
        boolean isPhaseSelect = false;
        String channelStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_CHANNEL);
        String positionStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_INDEX);
        int channelSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_CHANNEL_SELECT); // 从缓存读取整数值
        ArrayList<String> channelList = StrUtil.getListFromString(channelStrings, CacheUtil.MEASURE_SELECT_LIST_SLIP);
        ArrayList<String> indexList = StrUtil.getListFromString(positionStrings, CacheUtil.MEASURE_SELECT_LIST_SLIP);
        for (int i = 0; i < channelList.size(); i++) { // 遍历节点列表
            if(channelSelect + 1 == Integer.parseInt(channelList.get(i))) {
                if(Integer.parseInt(indexList.get(i)) == 12) { //相位
                    isPhaseSelect = true;
                    break; // 跳出循环
                }
            }
        }
        return isPhaseSelect;
    }

    /**
     * isMeasureTValueSelect方法
     */
    private boolean isMeasureTValueSelect() {
        boolean isTValueSelect = false;
        String channelStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_CHANNEL);
        String positionStrings = CacheUtil.get().getString(CacheUtil.TOP_SLIP_MEASURE_SELECT_LIST_INDEX);
        int channelSelect = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_MEASURE_CHANNEL_SELECT); // 从缓存读取整数值
        ArrayList<String> channelList = StrUtil.getListFromString(channelStrings, CacheUtil.MEASURE_SELECT_LIST_SLIP);
        ArrayList<String> indexList = StrUtil.getListFromString(positionStrings, CacheUtil.MEASURE_SELECT_LIST_SLIP);
        for (int i = 0; i < channelList.size(); i++) { // 遍历节点列表
            if(channelSelect + 1 == Integer.parseInt(channelList.get(i))) {
                if(Integer.parseInt(indexList.get(i)) == 26) { //TValue
                    isTValueSelect = true;
                    break; // 跳出循环
                }
            }
        }
        return isTValueSelect;
    }


    private Consumer<Boolean> consumerSyncExternalTriggerState = new Consumer<Boolean>() { // RxBus消息消费者
        @Override
        public void accept(Boolean aBoolean) throws Throwable {
            //true 显示1M 50Ω  false不显示
            List<ExternalKeysNode> topBaseList = getTopBaseList();
            ExternalKeysNode userSetAutOut = topBaseList.get(TopLayoutPopWindow.DETAIL_USERSET).getChildNodes()
                    .get(TopLayoutUserset.DETAIL_USERSETAUXOUT);
            if(!HardwareProduct.isMHO68V1()) {
                userSetAutOut.getChildNodes().get(4).setVisible(aBoolean);
                userSetAutOut.getChildNodes().get(5).setVisible(aBoolean);
            }

            ExternalKeysNode trigEdgeNode = topBaseList.get(TopLayoutPopWindow.DETAIL_TRIGGER).getChildNodes()
                    .get(TopLayoutTrigger.DETAIL_EDGE);
            List<ExternalKeysNode> trigEdgeList = trigEdgeNode.getChildNodes();
            String[] couples = App.get().getResources().getStringArray(com.micsig.tbook.ui.R.array.triggerCouple);
            for (int i = trigEdgeList.size() - 1; i >= trigEdgeList.size() - couples.length; i--) {//外部触发时触发耦合置灰
                trigEdgeList.get(i).setVisible(!aBoolean);
            }
        }
    };

    private Consumer<Integer> consumerSyncExternalAutoSaveState = new Consumer<Integer>() { // RxBus消息消费者
        @Override
        public void accept(Integer aInt) throws Throwable {
            //true 显示1M 50Ω  false不显示
            List<ExternalKeysNode> topBaseList = getTopBaseList();
            ExternalKeysNode autoSave = topBaseList.get(TopLayoutPopWindow.DETAIL_SAVE).getChildNodes()
                    .get(TopLayoutSave.DETAIL_AUTOSAVE);
            Log.d(TAG, "accept: " + autoSave);
            if(aInt==0){
                autoSave.getChildNodes().get(34).setVisible(false); // 设置不可见
                autoSave.getChildNodes().get(35).setVisible(false); // 设置不可见
                autoSave.getChildNodes().get(36).setVisible(false); // 设置不可见
                autoSave.getChildNodes().get(37).setVisible(false); // 设置不可见
                autoSave.getChildNodes().get(38).setVisible(false); // 设置不可见
                autoSave.getChildNodes().get(39).setVisible(false); // 设置不可见
                autoSave.getChildNodes().get(40).setVisible(false); // 设置不可见
            }
            if(aInt==1){
                autoSave.getChildNodes().get(34).setVisible(true); // 设置可见
                autoSave.getChildNodes().get(35).setVisible(true); // 设置可见
                autoSave.getChildNodes().get(36).setVisible(true); // 设置可见
                autoSave.getChildNodes().get(37).setVisible(true); // 设置可见
                autoSave.getChildNodes().get(38).setVisible(true); // 设置可见
                autoSave.getChildNodes().get(39).setVisible(true); // 设置可见
                autoSave.getChildNodes().get(40).setVisible(false); // 设置不可见
            }
            if(aInt==2){
                autoSave.getChildNodes().get(34).setVisible(false); // 设置不可见
                autoSave.getChildNodes().get(35).setVisible(false); // 设置不可见
                autoSave.getChildNodes().get(36).setVisible(false); // 设置不可见
                autoSave.getChildNodes().get(37).setVisible(false); // 设置不可见
                autoSave.getChildNodes().get(38).setVisible(false); // 设置不可见
                autoSave.getChildNodes().get(39).setVisible(false); // 设置不可见
                autoSave.getChildNodes().get(40).setVisible(true); // 设置可见
            }
        }
    };

    //endregion

    //endregion


    private Consumer<Integer> consumerMathRefBusOffset = new Consumer<Integer>() { // RxBus消息消费者
        @Override
        public void accept(Integer offset) throws Throwable {
            mathRefBusOffset = offset;
            finalMathRefBusOffset = mathRefBusOffset;
            allChannelListUpdate(); // 更新所有通道列表
        }
    };

    /**
     * allChannelListUpdate方法
     */
    private void allChannelListUpdate() { // 更新所有通道列表
        rightCh1List.clear(); // 清空右侧CH1通道节点列表
        rightCh2List.clear(); // 清空右侧CH2通道节点列表
        rightCh3List.clear(); // 清空右侧CH3通道节点列表
        rightCh4List.clear(); // 清空右侧CH4通道节点列表
        rightCh5List.clear(); // 清空右侧CH5通道节点列表
        rightCh6List.clear(); // 清空右侧CH6通道节点列表
        rightCh7List.clear(); // 清空右侧CH7通道节点列表
        rightCh8List.clear(); // 清空右侧CH8通道节点列表
        rightMath1List.clear(); // 清空右侧Math1节点列表
        rightMath2List.clear(); // 清空右侧Math2节点列表
        rightMath3List.clear(); // 清空右侧Math3节点列表
        rightMath4List.clear(); // 清空右侧Math4节点列表
        rightMath5List.clear(); // 清空右侧Math5节点列表
        rightMath6List.clear(); // 清空右侧Math6节点列表
        rightMath7List.clear(); // 清空右侧Math7节点列表
        rightMath8List.clear(); // 清空右侧Math8节点列表
        rightRef1List.clear(); // 清空右侧Ref1节点列表
        rightRef2List.clear(); // 清空右侧Ref2节点列表
        rightRef3List.clear(); // 清空右侧Ref3节点列表
        rightRef4List.clear(); // 清空右侧Ref4节点列表
        rightRef5List.clear(); // 清空右侧Ref5节点列表
        rightRef6List.clear(); // 清空右侧Ref6节点列表
        rightRef7List.clear(); // 清空右侧Ref7节点列表
        rightRef8List.clear(); // 清空右侧Ref8节点列表
        rightS1List.clear(); // 清空右侧S1串行总线节点列表
        rightS2List.clear(); // 清空右侧S2串行总线节点列表
        rightS3List.clear(); // 清空右侧S3串行总线节点列表
        rightS4List.clear(); // 清空右侧S4串行总线节点列表
        rightCh1List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH1通道节点
        rightCh2List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH2通道节点
        rightCh3List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH3通道节点
        rightCh4List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH4通道节点
        rightCh5List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH5通道节点
        rightCh6List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH6通道节点
        rightCh7List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH7通道节点
        rightCh8List.addAll(ExternalKeysNodeUtil.getRightSlipChannelNode()); // 加载右侧CH8通道节点
        rightMath1List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math1), ExternalKeysManager.finalMathRefBusOffset));
        rightMath2List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math2), ExternalKeysManager.finalMathRefBusOffset));
        rightMath3List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math3), ExternalKeysManager.finalMathRefBusOffset));
        rightMath4List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math4), ExternalKeysManager.finalMathRefBusOffset));
        rightMath5List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math5), ExternalKeysManager.finalMathRefBusOffset));
        rightMath6List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math6), ExternalKeysManager.finalMathRefBusOffset));
        rightMath7List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math7), ExternalKeysManager.finalMathRefBusOffset));
        rightMath8List.addAll(ExternalKeysNodeUtil.getRightSlipMathNode(TChan.toMathNumber(TChan.Math8), ExternalKeysManager.finalMathRefBusOffset));
        rightRef1List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R1), ExternalKeysManager.finalMathRefBusOffset));
        rightRef2List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R2), ExternalKeysManager.finalMathRefBusOffset));
        rightRef3List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R3), ExternalKeysManager.finalMathRefBusOffset));
        rightRef4List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R4), ExternalKeysManager.finalMathRefBusOffset));
        rightRef5List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R5), ExternalKeysManager.finalMathRefBusOffset));
        rightRef6List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R6), ExternalKeysManager.finalMathRefBusOffset));
        rightRef7List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R7), ExternalKeysManager.finalMathRefBusOffset));
        rightRef8List.addAll(ExternalKeysNodeUtil.getRightSlipRefNode(TChan.toRefNumber(TChan.R8), ExternalKeysManager.finalMathRefBusOffset));
        rightS1List.addAll(ExternalKeysNodeUtil.getRightSlipSerialsNode(CacheUtil.S1, ExternalKeysManager.finalMathRefBusOffset));
        rightS2List.addAll(ExternalKeysNodeUtil.getRightSlipSerialsNode(CacheUtil.S2, ExternalKeysManager.finalMathRefBusOffset));
        rightS3List.addAll(ExternalKeysNodeUtil.getRightSlipSerialsNode(CacheUtil.S3, ExternalKeysManager.finalMathRefBusOffset));
        rightS4List.addAll(ExternalKeysNodeUtil.getRightSlipSerialsNode(CacheUtil.S4, ExternalKeysManager.finalMathRefBusOffset));

        rightCh1Index = 0; // 初始化索引为0
        rightCh2Index = 0; // 初始化索引为0
        rightCh3Index = 0; // 初始化索引为0
        rightCh4Index = 0; // 初始化索引为0
        rightCh5Index = 0; // 初始化索引为0
        rightCh6Index = 0; // 初始化索引为0
        rightCh7Index = 0; // 初始化索引为0
        rightCh8Index = 0; // 初始化索引为0
        rightMath1Index = 0; // 初始化索引为0
        rightMath2Index = 0; // 初始化索引为0
        rightMath3Index = 0; // 初始化索引为0
        rightMath4Index = 0; // 初始化索引为0
        rightMath5Index = 0; // 初始化索引为0
        rightMath6Index = 0; // 初始化索引为0
        rightMath7Index = 0; // 初始化索引为0
        rightMath8Index = 0; // 初始化索引为0
        rightRef1Index = 0; // 初始化索引为0
        rightRef2Index = 0; // 初始化索引为0
        rightRef3Index = 0; // 初始化索引为0
        rightRef4Index = 0; // 初始化索引为0
        rightRef5Index = 0; // 初始化索引为0
        rightRef6Index = 0; // 初始化索引为0
        rightRef7Index = 0; // 初始化索引为0
        rightRef8Index = 0; // 初始化索引为0
        rightS1Index = 0; // 初始化索引为0
        rightS2Index = 0; // 初始化索引为0
        rightS3Index = 0; // 初始化索引为0
        rightS4Index = 0; // 初始化索引为0
    }

    private Consumer<Boolean> consumerSerialWordVisible = new Consumer<Boolean>() { // RxBus消息消费者
        @Override
        public void accept(Boolean aBoolean) throws Exception {
            if (aBoolean) { //文本页面
                finalMathRefBusOffset = -3;
            } else {
                finalMathRefBusOffset = mathRefBusOffset;
            }
            allChannelListUpdate(); // 更新所有通道列表
        }
    };

    private Consumer<Boolean> consumerUpdateSerialsVisible = new Consumer<Boolean>() { // RxBus消息消费者
        @Override
        public void accept(Boolean aBoolean) throws Throwable {
            serialsWordList.clear(); // 清空串行数据字节点列表
            serialsWordList.addAll(ExternalKeysNodeUtil.getSerialsWordNode()); // 加载串行数据字节点
        }
    };

    private Consumer<String> consumerSaveNameSuffixState = new Consumer<String>() { // RxBus消息消费者
        @Override
        public void accept(String suffixState) throws Throwable {
            String[] params = suffixState.split(CacheUtil.WAVE_STORE_PATH_SLIP);
            String type = params[0];
            boolean state = Boolean.parseBoolean(params[1]);
            List<ExternalKeysNode> topSaveList = getTopBaseList().
                    get(TopLayoutPopWindow.DETAIL_SAVE).getChildNodes()
                    .get(TopLayoutSave.DETAIL_STORE).getChildNodes();
            Logger.i("SaveNameSuffixState detail_index= " + suffixState + " ,type= " + type + " ,state= " + state); // 信息日志输出
            switch (type) { // switch分支选择
                case CacheUtil.WAVE_TYPE_WAV:
                    List<ExternalKeysNode> saveWavList = topSaveList.get(TopLayoutSaveStore.DETAIL_WAV).getChildNodes();
                    int wavNameAddIndex = Tools.indexOf(saveWavList, s -> s.getName().equalsIgnoreCase("WavSuffixNum"));
                    if (wavNameAddIndex != -1) {
                        saveWavList.get(wavNameAddIndex).setVisible(state);
                    }
                    break; // 跳出循环
                case CacheUtil.WAVE_TYPE_CSV:
                    List<ExternalKeysNode> saveCsvList = topSaveList.get(TopLayoutSaveStore.DETAIL_CSV).getChildNodes();
                    int csvNameAddIndex = Tools.indexOf(saveCsvList, s -> s.getName().equalsIgnoreCase("CsvSuffixNum"));
                    if (csvNameAddIndex != -1) {
                        saveCsvList.get(csvNameAddIndex).setVisible(state);
                    }
                    break; // 跳出循环
                case CacheUtil.WAVE_TYPE_BIN:
                    List<ExternalKeysNode> saveBinList = topSaveList.get(TopLayoutSaveStore.DETAIL_BIN).getChildNodes();
                    int binNameAddIndex = Tools.indexOf(saveBinList, s -> s.getName().equalsIgnoreCase("BinSuffixNum"));
                    if (binNameAddIndex != -1) {
                        saveBinList.get(binNameAddIndex).setVisible(state);
                    }
                    break; // 跳出循环
                case CacheUtil.SAVE_TYPE_SETTING:
                    List<ExternalKeysNode> saveSettingList = topSaveList.get(TopLayoutSaveStore.DETAIL_SETTING).getChildNodes();
                    int settingNameAddIndex = Tools.indexOf(saveSettingList, s -> s.getName().equalsIgnoreCase("SettingSuffixNum"));
                    if (settingNameAddIndex != -1) {
                        saveSettingList.get(settingNameAddIndex).setVisible(state);
                    }
                    break; // 跳出循环
                case CacheUtil.SAVE_TYPE_PICTURE:
                    //do nothing
                    break; // 跳出循环
                case CacheUtil.SAVE_TYPE_SESSION:
                    List<ExternalKeysNode> saveSessionList = topSaveList.get(TopLayoutSaveStore.DETAIL_SESSION).getChildNodes();
                    int sessionNameAddIndex = Tools.indexOf(saveSessionList, s -> s.getName().equalsIgnoreCase("SessionSuffixNum"));
                    if (sessionNameAddIndex != -1) {
                        saveSessionList.get(sessionNameAddIndex).setVisible(state);
                    }
                    break; // 跳出循环
                default: // 默认分支
                    break; // 跳出循环
            }
        }
    };


}