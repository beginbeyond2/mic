package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.tbook.scope.Calibrate.ProbeCalibrate;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.probe.BaseProbe;
import com.micsig.tbook.scope.vertical.VerticalAxis;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rightslipmenu.RightLayoutChannel;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.ScreenControls;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.util.TBookUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @auother Liwb
 * @description:
 * @data:2023-11-8 14:54
 */

/*
 * +----------------------------------------------------------------------+
 * |                    DialogProbeInterface                               |
 * |                     探头接口设置对话框                                 |
 * +----------------------------------------------------------------------+
 * | 模块定位: 右侧滑出菜单 -> 对话框子包                                    |
 * | 核心职责: 提供示波器探头接口参数的设置界面，                              |
 * |          显示探头型号/SN/校准按钮/带宽选择/探头类型选择，                  |
 * |          支持MSP/MRCP/MOIP/MDP等探头类型的差异化显示和校准操作            |
 * | 架构设计: 继承ConstraintLayout的自定义对话框视图，                        |
 * |          根据探头类型动态显示/隐藏带宽选择和校准功能，                     |
 * |          校准操作委托给ProbeCalibrate/BaseProbe执行                     |
 * | 数据流向: 外部调用show()传入通道和探头类型 -> 根据探头状态刷新UI ->       |
 * |          用户选择带宽/类型/校准 -> 通过RxBus或Channel API应用设置        |
 * | 依赖关系: RxBus(对话框开关/带宽变更事件), Channel/BaseProbe(通道/探头顶), |
 * |          ProbeCalibrate(探头校准), ScreenControls(屏幕锁定),            |
 * |          CacheUtil(带宽缓存), RightViewSelect(预设列表控件),            |
 * |          RightLayoutChannel(探头类型常量)                               |
 * | 使用场景: 通道菜单中检测到自动探头时弹出，用于查看探头信息和配置参数       |
 * +----------------------------------------------------------------------+
 */
public class DialogProbeInterface extends ConstraintLayout {
    private Context context;  // 上下文引用
    private ViewGroup rootView;  // 根视图组

    /**
     * 单参数构造方法，委托给两参数构造。
     *
     * @param context 上下文
     */
    public DialogProbeInterface(@NonNull Context context) {
        this(context,null);  // 委托给两参数构造
    }

    /**
     * 两参数构造方法，委托给三参数构造。
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public DialogProbeInterface(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);  // 委托给三参数构造
    }

    /**
     * 三参数构造方法，初始化上下文并调用initView()加载布局。
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogProbeInterface(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造
        this.context=context;  // 保存上下文引用
        initView();  // 执行视图初始化
    }



    private View divider3,divider4;  // 分隔线3(校准区域上方) / 分隔线4(带宽区域上方)
    private TextView tvModelName,tvSN,lblCal,lblBandWidth;  // 型号名/SN/校准标签/带宽标签
    private Button btnCal;  // 校准按钮
    private RightViewSelect bandwidthSelect;  // 带宽选择列表
    private RightViewSelect probeType;  // 探头类型选择列表
    private Channel channel;  // 当前通道对象
    private  int ProbeType_Mode;  // 探头类型模式（MSP/MRCP/MOIP/MDP）
    private List<Bean> listBandWidth=new ArrayList<>();  // 带宽预设值列表

    /**
     * 初始化对话框布局和交互逻辑。
     * <p>
     * 加载dialog_probe_interface布局，设置外部区域点击关闭监听，
     * 初始化控件引用、事件监听和带宽预设值列表，然后隐藏对话框。
     */
    private void initView() {
        setClickable(true);  // 设置可点击，拦截触摸事件
        rootView= (ViewGroup) inflate(context, R.layout.dialog_probe_interface,this);  // 填充布局
        rootView.findViewById(R.id.outView).setOnTouchListener((v,e) -> {  // 设置外部区域触摸监听
//            Log.d("Tag.Debug", String.format("onClick: DialogProbeInterface" ));
            hide();      // 隐藏对话框
            return false;  // 不消费触摸事件
        });

        tvModelName=rootView.findViewById(R.id.modelName);  // 探头型号名称文本
        tvSN=rootView.findViewById(R.id.SN);  // 探头SN序列号文本
        btnCal=rootView.findViewById(R.id.btnCal);  // 校准按钮
        lblCal=rootView.findViewById(R.id.lblCal);  // 校准标签
        divider3=rootView.findViewById(R.id.divider3);  // 校准区域分隔线
        bandwidthSelect=rootView.findViewById(R.id.bandWidth);  // 带宽选择列表
        probeType=rootView.findViewById(R.id.ProbeType);  // 探头类型选择列表
        divider4=rootView.findViewById(R.id.divider4);  // 带宽区域分隔线
        lblBandWidth=rootView.findViewById(R.id.lblBandWidth);  // 带宽标签

        btnCal.setOnClickListener(this::OnBtnCalClick);  // 校准按钮点击监听
        bandwidthSelect.setOnItemClickListener(onItemClickListener);  // 带宽选择点击监听
        probeType.setOnItemClickListener(onItemClickListener);  // 探头类型选择点击监听


        listBandWidth.add(new Bean("500M",(long)500E6));  // 500MHz带宽预设
        listBandWidth.add(new Bean("400M",(long)400E6));  // 400MHz带宽预设
        listBandWidth.add(new Bean("300M",(long)300E6));  // 300MHz带宽预设
        listBandWidth.add(new Bean("200M",(long)200E6));  // 200MHz带宽预设
        listBandWidth.add(new Bean("150M",(long)150E6));  // 150MHz带宽预设
        listBandWidth.add(new Bean("100M",(long)100E6));  // 100MHz带宽预设
        listBandWidth.add(new Bean("50M",(long)50E6));  // 50MHz带宽预设
        listBandWidth.add(new Bean("30M",(long)30E6));  // 30MHz带宽预设
        listBandWidth.add(new Bean("5M",(long)5E6));  // 5MHz带宽预设


        hide();  // 默认隐藏对话框

    }

    /**
     * 带宽预设值数据Bean。
     * <p>
     * 包含显示文本和对应的Hz值。
     */
    class Bean{
        private String showTxt;  // 显示文本（如"500M"）
        private long value;  // Hz值（如500000000）
        public Bean(String showTxt,long value){
            this.showTxt=showTxt;  // 保存显示文本
            this.value=value;  // 保存Hz值
        }
    }

    /**
     * 显示对话框，根据通道和探头类型刷新UI。
     * <p>
     * 仅当通道支持自动探头且探头已连接（SN非空）时才显示。
     * 根据探头类型（MSP/MRCP/MOIP/MDP）决定带宽选择和校准功能的可见性。
     *
     * @param channel       当前通道对象
     * @param probeType_Mode 探头类型模式
     * @return true=对话框已显示，false=不支持（未显示）
     */
    public boolean show(Channel channel,int probeType_Mode){
        this.ProbeType_Mode=probeType_Mode;  // 保存探头类型模式
        this.channel=channel;  // 保存通道对象
        bandwidthSelect.setControlColorByChIdx(channel.getChId());  // 根据通道索引设置带宽选择颜色
        probeType.setControlColorByChIdx(channel.getChId());  // 根据通道索引设置探头类型颜色

        BaseProbe baseProbe = channel.getProbe();  // 获取探头顶对象
        boolean isProbeInterface= channel != null && channel.isAutoProbe() && !StrUtil.isEmpty(channel.getProbe().getSN());  // 判断是否为有效的探头接口（自动探头且SN非空）
        if (baseProbe!=null && isProbeInterface){  // 探头有效且为探头接口
            refreshUI(baseProbe);  // 刷新探头信息UI
            btnCal.setVisibility(VISIBLE);  // 显示校准按钮
            lblCal.setVisibility(VISIBLE);  // 显示校准标签
            divider3.setVisibility(VISIBLE);  // 显示校准分隔线
            switch (probeType_Mode){  // 根据探头类型模式差异化显示
                case RightLayoutChannel.ProbeType_MSP:{  // MSP探头
                    refreshBandWidthUI((long) 500E6);  // MSP固定500MHz带宽
                    bandwidthSelect.setVisibility(GONE);  // 隐藏带宽选择
                    divider4.setVisibility(GONE);  // 隐藏带宽分隔线
                    lblBandWidth.setVisibility(GONE);  // 隐藏带宽标签
                }break;
                case RightLayoutChannel.ProbeType_MRCP:{  // MRCP探头
                    divider4.setVisibility(VISIBLE);  // 显示带宽分隔线
                    lblBandWidth.setVisibility(VISIBLE);  // 显示带宽标签
                    bandwidthSelect.setVisibility(VISIBLE);  // 显示带宽选择
                    refreshBandWidthUI(baseProbe.getBandWidth());  // 根据探头实际带宽刷新
                    if (baseProbe.isScopeAdjust()==false){  // MRCP不支持示波器调节时
                        btnCal.setVisibility(GONE);  // 隐藏校准按钮
                        lblCal.setVisibility(GONE);  // 隐藏校准标签
                        divider3.setVisibility(GONE);  // 隐藏校准分隔线
                    }
                }break;
                case RightLayoutChannel.ProbeType_MOIP:  // MOIP探头
                case RightLayoutChannel.probeType_MDP:  // MDP探头
                default:{  // 默认处理
                    divider4.setVisibility(VISIBLE);  // 显示带宽分隔线
                    lblBandWidth.setVisibility(VISIBLE);  // 显示带宽标签
                    bandwidthSelect.setVisibility(VISIBLE);  // 显示带宽选择

                    refreshBandWidthUI(baseProbe.getBandWidth());  // 根据探头实际带宽刷新
                }break;
            }
            String fb= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH+(channel.getChId()+1) ) ;  // 从缓存读取带宽选择值
            bandwidthSelect.setSelectText(fb);  // 设置带宽选中项

            setVisibility(VISIBLE);  // 设置视图可见
            this.post(()->{  // 延迟发送事件（等待布局完成）
                RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_PROBE_INTERFACE);  // 发送对话框打开事件
                Tools.PrintControlsLocation("dialogProbeInterface",rootView);  // 打印控件位置调试信息
            });

            return true;  // 对话框已显示
        }
        return false;  // 不支持，未显示

    }

    /**
     * 隐藏对话框，并通过RxBus通知对话框关闭事件。
     */
    public void hide(){
        setVisibility(GONE);  // 设置视图不可见且不占位
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_PROBE_INTERFACE);  // 发送对话框关闭事件
    }

    /**
     * 获取根视图组。
     *
     * @return 根视图组
     */
    public ViewGroup getRootView(){
        return rootView;  // 返回根视图
    }

    /**
     * 获取带宽选择的选中项数量。
     *
     * @return 选中项数量
     */
    public int getListBandWidthCount(){
        return bandwidthSelect.getSelectCount();  // 返回选中项数量
    }

    /**
     * 刷新探头信息UI。
     * <p>
     * 更新型号名称、SN序列号，以及探头类型选择的状态。
     *
     * @param baseProbe 探头顶对象
     */
    private void refreshUI(BaseProbe baseProbe){
        tvModelName.setText("Model:"+baseProbe.getProbeName());  // 设置型号名称
        tvSN.setText("SN:"+baseProbe.getSN());  // 设置SN序列号


        BaseProbe bp= channel.getProbe();  // 获取当前通道的探头顶
        if (bp!=null) {  // 探头非空
            setProbeTypeEnable(bp.isSupportProbeRateCtrl());  // 设置探头类型是否可选
            if (bp.isSupportProbeRateCtrl()) {  // 支持速率控制
                boolean isAuto = bp.isAutoRateCtrl();  // 获取当前是否自动速率
                probeType.setSelectIndex(isAuto ? 1 : 0);  // 自动选中索引1，手动选中索引0
            }
        }
    }

    /**
     * 根据探头最大带宽刷新带宽选择列表。
     * <p>
     * 只显示不超过探头最大带宽的选项。如果没有可用选项则隐藏带宽选择区域。
     *
     * @param maxBandWidth 探头最大带宽（Hz）
     */
    private void refreshBandWidthUI(long maxBandWidth){
        List<String> list=new ArrayList<>();  // 可用带宽选项列表
        for(int i=0;i<listBandWidth.size();i++){  // 遍历所有预设带宽
            if (listBandWidth.get(i).value <= (maxBandWidth)){  // 不超过最大带宽
                list.add(listBandWidth.get(i).showTxt+"Hz");  // 添加到可用列表
            }
        }
        if (list.size() == 0) {  // 没有可用选项
            bandwidthSelect.setVisibility(GONE);  // 隐藏带宽选择
            return;  // 返回
        } else {  // 有可用选项
            bandwidthSelect.setVisibility(VISIBLE);  // 显示带宽选择
        }
        bandwidthSelect.setArray(list.toArray(new String[0]));  // 设置带宽选项数组
        bandwidthSelect.clearSelect();  // 清除选中状态

        String fb= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH+(channel.getChId()+1));  // 从缓存读取之前的选择
        if (StrUtil.isEmpty(fb)) {  // 缓存为空
            if (list.size() > 0) {  // 有可用选项
                bandwidthSelect.setSelectIndex(0);  // 默认选中第一个
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH + (channel.getChId() + 1), list.get(0));  // 缓存默认值
            }
        }else {  // 缓存非空
            bandwidthSelect.setSelectText(fb);  // 恢复之前的选择
        }
    }

    /**
     * 设置探头类型选择的启用状态。
     * <p>
     * 注意：当前实现中setProbeTypeEnable始终禁用探头类型选择（先启用再禁用），
     * 可能是预留接口，暂时强制禁用。
     *
     * @param b 是否启用（实际未生效）
     */
    private void setProbeTypeEnable(boolean b){
        probeType.setEnabled(b);  // 设置启用状态
        probeType.setEnabled(false);  // 强制禁用（覆盖上面的设置）
    }


    /**
     * 校准按钮点击事件处理。
     * <p>
     * 根据探头类型模式分发到对应的校准流程：
     * - MDP: 执行autoZero校准
     * - MSP: 执行MSP500校准（需要切换到YT模式和50mV档位）
     * - MOIP: 执行autoGain校准
     *
     * @param view 被点击的按钮视图
     */
    private void OnBtnCalClick(View view) {
        switch (this.ProbeType_Mode){  // 根据探头类型模式分发
            case RightLayoutChannel.probeType_MDP:{  // MDP探头
                calMDP700();  // 执行MDP校准
            }break;
            case RightLayoutChannel.ProbeType_MSP:{  // MSP探头
                calMSP500();  // 执行MSP校准
            }break;
            case RightLayoutChannel.ProbeType_MOIP:{  // MOIP探头
                claMOIP();  // 执行MOIP校准
            }break;
        }

    }

    /**
     * 执行MOIP探头自动增益校准。
     * <p>
     * 调用探头的autoGain方法并锁定屏幕。
     */
    private void claMOIP() {
        BaseProbe probe = channel.getProbe();  // 获取探头顶对象
        probe.autoGain();  // 执行自动增益
        lockScreen();  // 锁定屏幕（防止校准期间误操作）
    }

    /**
     * 执行MDP700探头自动零点校准。
     * <p>
     * 调用探头的autoZero方法并锁定屏幕。
     */
    private void calMDP700(){
        BaseProbe probe = channel.getProbe();  // 获取探头顶对象
        probe.autoZero();  // 执行自动零点校准
        lockScreen();  // 锁定屏幕
    }

    /**
     * 执行MSP500探头校准。
     * <p>
     * 需要先隐藏所有对话框、切换到YT模式和50mV档位，
     * 然后启动探头校准流程并锁定屏幕。
     */
    private  void calMSP500(){
        double[] param = {channel.getChId(), VerticalAxis.DANG_50mV};  // 校准参数：通道ID + 50mV档位
        BaseProbe baseProbe = channel.getProbe();  // 获取探头顶对象
        if(baseProbe != null && baseProbe.isDa()) {  // 探头有效且支持DA
            ProbeCalibrate probeCalibrate = ProbeCalibrate.getInstance();  // 获取校准单例
            if (!probeCalibrate.isCalibrate()) {  // 当前未在校准中
                ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();  // 隐藏所有对话框
                if (WorkModeManage.getInstance().isYTMode()==false) {  // 非YT模式
                    WorkModeManage.getInstance().setWorkMode(IWorkMode.WorkMode_YT, false);  // 切换到YT模式
                }
                ProbeCalibrate.getInstance().begin(param);  // 开始校准
                lockScreen();  // 锁定屏幕
            }
        }
    }

    /**
     * 锁定屏幕，防止校准期间用户误操作。
     * <p>
     * 使用ScreenControls的LOCK_PROBE标志按通道位锁定。
     */
    private void lockScreen() {
        if(channel != null) {  // 通道非空
            ScreenControls screenControls = ScreenControls.getInstance();  // 获取屏幕控制单例
            screenControls.lockScreen(ScreenControls.LOCK_PROBE << channel.getChId());  // 按通道位锁定屏幕
        }
    }

    /**
     * 预设列表项点击监听器。
     * <p>
     * 将点击事件委托给onItemClick方法处理。
     */
    private RightViewSelect.OnItemClickListener onItemClickListener =new RightViewSelect.OnItemClickListener() {
        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            DialogProbeInterface.this.onItemClick(viewId, item, false);  // 委托给外部方法处理
        }

        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            // 按键音效回调（未使用）
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {
            // 点击后UI刷新回调（未使用）
        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {
            // 点击前UI刷新回调（未使用）
        }
    };

    /**
     * 处理列表项点击事件。
     * <p>
     * - 带宽选择：将选中的带宽值应用到通道，通过RxBus通知带宽变更，然后关闭对话框
     * - 探头类型选择：根据选中项设置探头的自动/手动速率控制模式
     *
     * @param viewId        点击的视图ID
     * @param item          选中的数据项
     * @param isFromEventBus 是否来自EventBus（当前未使用）
     */
    private void onItemClick(int viewId, RightBeanSelect item, boolean isFromEventBus){
        if (viewId==bandwidthSelect.getId()) {  // 带宽选择项被点击
            String fb = bandwidthSelect.getSelectItem().getText().toString();  // 获取选中的带宽文本
//        double hz = TBookUtil.getMHzFromHz(fb);
            int bandWidthType = Channel.BANDWIDTH_TYPE_LOWPASS;  // 带宽类型：低通滤波
            channel.setBandWidthType(bandWidthType, (TBookUtil.getMHzFromHz(fb) * 1000 * 1000));  // 设置通道带宽类型和值
            RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_CHANNEL_BANDWIDTH, fb+","+channel.getChId());  // 通知带宽变更事件
            hide();  // 隐藏对话框
        }else if (viewId==probeType.getId()){  // 探头类型项被点击
            BaseProbe bp=channel.getProbe();  // 获取探头顶对象
            if (item.getIndex()==0){  // 选中"手动"模式
                if (bp!=null) {
                    bp.setAutoRateCtrl(false);  // 设置为手动速率控制
                }
            }else {  // 选中"自动"模式
                if (bp!=null) {
                    bp.setAutoRateCtrl(true);  // 设置为自动速率控制
                }
            }
        }
    }
}
