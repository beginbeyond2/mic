package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.nfc.cardemulation.HostNfcFService;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.math.MathDualWave;
import com.micsig.tbook.scope.math.MathExprWave;
import com.micsig.tbook.scope.math.MathFFTWave;
import com.micsig.tbook.scope.math.MathWave;
import com.micsig.tbook.scope.vertical.VerticalAxis;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainbottom.MainBottomMsgTimeBase;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.top.view.channel.TopViewChannelMultipleChoice;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.rxjava3.functions.Consumer;

/*
 * +=============================================================================+
 * |                       DialogSetChannelInfo                                   |
 * |-----------------------------------------------------------------------------|
 * | 模块定位 : 右侧滑菜单 -> 通道参数设置对话框                                     |
 * | 核心职责 : 显示和编辑通道的垂直档位、垂直位置、水平档位、水平位置参数；               |
 * |           支持模拟通道、参考通道、数学通道三种类型的参数展示与修改                   |
 * | 架构设计 : 继承 ConstraintLayout，包含4个 TopViewEdit 编辑控件；                   |
 * |           点击编辑控件时弹出浮层键盘，输入完成后通过 Command 下发参数变更指令          |
 * | 数据流向 : setContentInfo() 读取通道参数展示 → 用户点击编辑 → 浮层键盘输入 →         |
 * |           Command 下发变更指令 → 硬件执行 → 参数更新                              |
 * | 依赖关系 : ChannelFactory（通道信息工厂）、Command（参数下发指令）、                  |
 * |           TopDialogFloatKeyBoard（浮层键盘）、TBookUtil（数值格式化）、             |
 * |           RxBus（对话框开关）、CacheUtil（参数缓存）                               |
 * | 使用场景 : 在通道菜单中点击参数项时弹出，查看或修改通道的垂直/水平参数               |
 * +=============================================================================+
 */
public class DialogSetChannelInfo extends ConstraintLayout {

    private static final String TAG = "DialogSetChannelInfo";                   // 日志标签
    private final Context context;                                              // 上下文引用
    private ConstraintLayout rootViewGroup;                                     // 布局根视图组
    private OnDismissListener dismissListener;                                  // 对话框关闭回调监听器
    private TopViewEdit verScale, verPosition, horScale, horPosition;           // 垂直档位、垂直位置、水平档位、水平位置编辑控件
    private TextView chTitle;                                                   // 通道标题文本
    private int chIdx;                                                          // 当前操作的通道索引
    private TopDialogFloatKeyBoard dialogFloatKeyBoard;                         // 浮层软键盘

    /**
     * 对话框关闭监听接口
     * <p>对话框关闭时回传通道索引与修改后的参数映射</p>
     */
    public interface OnDismissListener {
        /** 对话框关闭时回调
         * @param chIdx   通道索引
         * @param infoMap 修改后的参数映射（键为资源ID，值为参数字符串）
         */
        void onDismiss(int chIdx, HashMap<Integer, String> infoMap);
    }

    /**
     * 单参数构造方法
     * @param context 上下文
     */
    public DialogSetChannelInfo(Context context) {
        this(context, null);                                                    // 委托给双参数构造
    }

    /**
     * 双参数构造方法
     * @param context 上下文
     * @param attrs   属性集
     */
    public DialogSetChannelInfo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);                                                // 委托给三参数构造
    }

    /**
     * 三参数构造方法（最终构造入口）
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogSetChannelInfo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);                                    // 调用父类构造
        this.context = context;                                                 // 保存上下文引用
        initView();                                                             // 初始化视图
        initControl();                                                          // 初始化控件（预留扩展）
    }

    /**
     * 初始化控件（预留扩展点，当前为空实现）
     */
    private void initControl() {

    }

    /**
     * 初始化视图控件与交互
     * <p>加载布局、设置外部区域触摸关闭、绑定编辑控件与点击监听器</p>
     */
    public void initView() {
        rootViewGroup = (ConstraintLayout) inflate(context, R.layout.dialog_set_channel_info, this); // 加载对话框布局
//        View outView = rootViewGroup.findViewById(R.id.outView);
//        outView.setOnClickListener(onClickListener);

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() { // 设置外部区域触摸监听
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();                                                         // 触摸外部区域则隐藏对话框
                return false;                                                   // 不消费事件，继续传递
            }
        });

        verScale = findViewById(R.id.ver_scale);                                // 获取垂直档位编辑控件
        verPosition = findViewById(R.id.ver_position);                          // 获取垂直位置编辑控件
        horScale = findViewById(R.id.hor_scale);                                // 获取水平档位编辑控件
        horPosition = findViewById(R.id.hor_position);                          // 获取水平位置编辑控件
        chTitle = findViewById(R.id.channel_title);                             // 获取通道标题控件

        verScale.setOnClickEditListener(onClickEditListener);                   // 设置垂直档位点击编辑监听
        verPosition.setOnClickEditListener(onClickEditListener);                // 设置垂直位置点击编辑监听
        horScale.setOnClickEditListener(onClickEditListener);                   // 设置水平档位点击编辑监听
        horPosition.setOnClickEditListener(onClickEditListener);                // 设置水平位置点击编辑监听
    }

//    @SuppressLint("NonConstantResourceId")
//    private final View.OnClickListener onClickListener = v -> {
//        switch (v.getId()) {
//            case R.id.outView:
//                hide();
//                break;
//        }
//    };

    /**
     * 显示对话框
     * <p>设置可见性并通过 RxBus 发送对话框打开事件</p>
     */
    public void show() {
        setVisibility(VISIBLE);                                                 // 设置对话框可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_SET_CHANNEL_INFO); // 发送对话框打开事件
        Tools.PrintControlsLocation(TAG, rootViewGroup);                        // 打印控件位置调试信息
    }

    /**
     * 隐藏对话框
     * <p>设置不可见、清除内容信息、通过 RxBus 发送对话框关闭事件</p>
     */
    public void hide() {
        setVisibility(GONE);                                                    // 设置对话框不可见
        clearContentInfo();                                                     // 清除编辑框内容
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_SET_CHANNEL_INFO); // 发送对话框关闭事件
    }


    /**
     * 设置对话框数据并显示
     * @param chIdx          通道索引
     * @param dismissListener 关闭回调监听器
     */
    public void setData(int chIdx, OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;                                 // 保存回调监听器
        this.chIdx = chIdx;                                                     // 保存通道索引
        setContentInfo(chIdx);                                                  // 填充通道参数信息
        show();                                                                 // 显示对话框
    }

    /**
     * 清除所有编辑框内容
     */
    private void clearContentInfo() {
        chTitle.setText("");                                                    // 清空通道标题
        verScale.setText("");                                                   // 清空垂直档位
        verPosition.setText("");                                                // 清空垂直位置
        horScale.setText("");                                                   // 清空水平档位
        horPosition.setText("");                                                // 清空水平位置
    }

    /**
     * 根据通道类型填充参数信息
     * <p>分别处理模拟通道、参考通道、数学通道的参数展示</p>
     * @param chIdx 通道索引
     */
    private void setContentInfo(int chIdx) {
        String chName = ChannelFactory.getChannelName(chIdx);                   // 获取通道名称
        chTitle.setTextColor(SvgNodeInfo.getAllBaseColorInt(TChan.toUiChNo(chIdx))); // 设置标题颜色为通道颜色
        chTitle.setText(chName);                                                // 显示通道名称

        verScale.setVisibility(View.VISIBLE);                                   // 显示垂直档位编辑框
        verPosition.setVisibility(View.VISIBLE);                                // 显示垂直位置编辑框
        horScale.setVisibility(View.VISIBLE);                                   // 显示水平档位编辑框
        horPosition.setVisibility(View.VISIBLE);                                // 显示水平位置编辑框

        if (ChannelFactory.isDynamicCh(chIdx)) { //模拟通道
            setChInfo(chIdx);                                                   // 填充模拟通道参数
        } else if (ChannelFactory.isRefCh(chIdx)) {//参考通道
            setRefInfo(chIdx);                                                  // 填充参考通道参数
        } else if (ChannelFactory.isMathCh(chIdx)) {//数学通道
            serMathInfo(chIdx);                                                 // 填充数学通道参数
        }
    }

    /**
     * 填充数学通道的参数信息
     * @param chIdx 数学通道索引
     */
    private void serMathInfo(int chIdx) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(chIdx);         // 获取数学通道对象
        if (mathChannel == null || !mathChannel.isOpen()) return;               // 通道不存在或未打开则返回
        //垂直档位
        String unit = ChannelFactory.getProbeType(chIdx);                       // 获取通道单位类型
        double vScaleVal = mathChannel.getVScaleVal();                          // 获取垂直档位值
        verScale.setText(TBookUtil.getMFromDouble(vScaleVal) + unit);           // 显示垂直档位值（带单位）

        //垂直位置
        double leftPos = Tools.getChannelPositionUI(TChan.toUiChNo(chIdx));     // 获取通道垂直位置（UI坐标）
        int zoomHeight = (int) (ScopeBase.getNewHeight() * 0.75);              // 计算缩放模式下的高度
        double pos = (Tools.isZoom() ? zoomHeight : 1.0 * ScopeBase.getNewHeight()) / 2 - leftPos; // 计算垂直偏移量
        String number = TBookUtil.getFourFromD_Trim0(pos * mathChannel.getVerticalPerPix()); // 格式化位置值
        verPosition.setText(number + unit);                                     // 显示垂直位置值（带单位）

        //水平档位
        String timeBaseScale;                                                   // 水平时基档位字符串
        if (ChannelFactory.isMath_FFT_Ch(chIdx)) {                              // 如果是FFT数学通道
            timeBaseScale = TBookUtil.getMFromDouble(mathChannel.getHorizontalAxisMathFFT().fftXScaleIdVal()) + "Hz"; // 显示FFT频率档位
        } else {                                                                // 非FFT数学通道
            timeBaseScale = CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE); // 获取普通时基档位
        }
        horScale.setText(timeBaseScale);                                        // 显示水平档位值


        //水平位置
        long timePos = HorizontalAxis.getInstance().getTimePosOfView();         // 获取水平位置时间偏移
        if (ChannelFactory.isMath_FFT_Ch(chIdx)) {                              // FFT通道无水平位置
            horPosition.setVisibility(View.GONE);                               // 隐藏水平位置编辑框
        } else {                                                                // 非FFT通道
            horPosition.setText(TBookUtil.getSFrom100Fs(timePos));              // 显示水平位置时间值
        }
    }

    /**
     * 填充模拟通道的参数信息
     * @param chIdx 模拟通道索引
     */
    private void setChInfo(int chIdx) {
        Channel channel = ChannelFactory.getDynamicChannel(chIdx);              // 获取模拟通道对象
        if (channel == null || !channel.isOpen()) return;                       // 通道不存在或未打开则返回

        //垂直档位
        double chxVal = channel.getVScaleVal();                                 // 获取垂直档位值
        String unit = Tools.getChanProbeTypeUnit(channel) == 0 ? "V" : "A";    // 根据探头类型确定单位（V/A）
        verScale.setText(TBookUtil.getMFromDouble(chxVal) + unit);             // 显示垂直档位值（带单位）

        //垂直位置
        double leftPos = Tools.getChannelPositionUI(TChan.toUiChNo(chIdx));     // 获取通道垂直位置（UI坐标）
        int zoomHeight = (int) (ScopeBase.getNewHeight() * 0.75);              // 计算缩放模式下的高度
        double pos = (Tools.isZoom() ? zoomHeight : 1.0 * ScopeBase.getNewHeight()) / 2 - leftPos; // 计算垂直偏移量
        String number = TBookUtil.getFourFromD_Trim0(pos * channel.getVerticalPerPix()); // 格式化位置值
        verPosition.setText(number + unit);                                     // 显示垂直位置值（带单位）

        //水平档位
        String timeBaseScale = CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE); // 获取普通时基档位
        horScale.setText(timeBaseScale);                                        // 显示水平档位值

        //水平位置
        long timePos = HorizontalAxis.getInstance().getTimePosOfView();         // 获取水平位置时间偏移
        horPosition.setText(TBookUtil.getSFrom100Fs(timePos));                  // 显示水平位置时间值
    }


    /**
     * 填充参考通道的参数信息
     * @param chIdx 参考通道索引
     */
    private void setRefInfo(int chIdx) {
        RefChannel refChannel = ChannelFactory.getRefChannel(chIdx);            // 获取参考通道对象
        if (refChannel == null || !refChannel.isOpen()) return;                 // 通道不存在或未打开则返回

        //垂直档位
        double extent = refChannel.getVScaleIdVal();                            // 获取参考通道垂直档位值
        String unit = ChannelFactory.getProbeType(chIdx);                       // 获取通道单位类型
        verScale.setText(TBookUtil.getMFromDouble(extent) + unit);              // 显示垂直档位值（带单位）

        //垂直位置
        double leftPos = Tools.getChannelPositionUI(TChan.toUiChNo(chIdx));     // 获取通道垂直位置（UI坐标）
        int zoomHeight = (int) (ScopeBase.getNewHeight() * 0.75);              // 计算缩放模式下的高度
        double pos = (Tools.isZoom() ? zoomHeight : 1.0 * ScopeBase.getNewHeight()) / 2 - leftPos; // 计算垂直偏移量
        String number = TBookUtil.getFourFromD_Trim0(pos * refChannel.getVerticalPerPix()); // 格式化位置值
        verPosition.setText(number + unit);                                     // 显示垂直位置值（带单位）

        //水平档位
        String timeBase = CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + TChan.toUiChNo(chIdx)); // 获取参考通道时基档位
        horScale.setText(timeBase);                                             // 显示水平档位值

        //水平位置
        int followCh = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE); // 获取参考通道时基跟随设置
        String timePosStr = "";                                                 // 水平位置时间字符串
        if (refChannel.getRefType() != WaveData.FFT_WAVE) {                    // 非FFT参考波形
            long timePos;                                                       // 时间偏移值
            if (followCh == 0) { //跟随模拟通道
                timePos = HorizontalAxis.getInstance().getTimePosOfView();      // 获取模拟通道水平位置
            } else {                                                            // 不跟随
                timePos = refChannel.getTimePosOfView();                        // 获取参考通道自身水平位置
            }
            timePosStr = TBookUtil.getSFrom100Fs(timePos);                     // 格式化时间值
        } else {                                                                // FFT参考波形
            horPosition.setVisibility(View.GONE);                               // 隐藏水平位置编辑框
        }
        horPosition.setText(timePosStr);                                        // 显示水平位置时间值
    }


    /**
     * 编辑控件点击监听器
     * <p>点击任一编辑控件时，播放音效并弹出浮层键盘进行对应参数编辑</p>
     */
    private TopViewEdit.OnClickEditListener onClickEditListener = new TopViewEdit.OnClickEditListener() {
        @Override
        public void onClickEdit(TopViewEdit v, String text) {
            PlaySound.getInstance().playButton();                               // 播放按钮音效
            if (dialogFloatKeyBoard == null) {                                  // 如果浮层键盘未初始化
                dialogFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard); // 从 Activity 获取浮层键盘
            }
            if (v.getId() == verScale.getId()) {                                // 点击垂直档位
                setChVScale(verScale);                                          // 弹出键盘编辑垂直档位
            } else if (v.getId() == verPosition.getId()) {                      // 点击垂直位置
                setChVPosition(verPosition);                                    // 弹出键盘编辑垂直位置
            } else if (v.getId() == horScale.getId()) {                         // 点击水平档位
                setChHScale(horScale);                                          // 弹出键盘编辑水平档位
            } else if (v.getId() == horPosition.getId()) {                      // 点击水平位置
                setChHPosition(horPosition);                                    // 弹出键盘编辑水平位置
            }
        }
    };

    //水平位置

    /**
     * 弹出浮层键盘编辑水平位置参数
     * @param topViewEdit 水平位置编辑控件
     */
    private void setChHPosition(TopViewEdit topViewEdit) {
        dialogFloatKeyBoard.bringToFront();                                     // 将键盘提升到最前层
        String txt = topViewEdit.getText().replace("s", "").replace(" ", "");   // 去除单位与空格
        dialogFloatKeyBoard.setFloatData(txt, topViewEdit, new TopDialogFloatKeyBoard.OnDismissListener() { // 设置键盘数据
            @Override
            public void onDismiss(View fromView, String show) {
                Logger.d("limh hposition= " + show);                            // 记录日志
                PlaySound.getInstance().playButton();                           // 播放按钮音效
                topViewEdit.setText(show + "s");                                // 显示输入值（带秒单位）

                double val = TBookUtil.getDoubleFromM(show);                    // 将输入字符串转为数值
                Command.get().getTimebase().Position(val, true);                // 下发水平位置变更指令
            }
        });
    }

    //水平档位

    /**
     * 弹出浮层键盘编辑水平档位参数
     * @param topViewEdit 水平档位编辑控件
     */
    private void setChHScale(TopViewEdit topViewEdit) {
        dialogFloatKeyBoard.bringToFront();                                     // 将键盘提升到最前层
        String txt = topViewEdit.getText().replace("s", "").replace(" ", "");   // 去除单位与空格
        dialogFloatKeyBoard.setFloatData(txt, topViewEdit, new TopDialogFloatKeyBoard.OnDismissListener() { // 设置键盘数据
            @Override
            public void onDismiss(View fromView, String show) {
                Logger.d("limh hScale= " + show);                               // 记录日志
                PlaySound.getInstance().playButton();                           // 播放按钮音效
                topViewEdit.setText(show + "s");                                // 显示输入值（带秒单位）

                double val = TBookUtil.getDoubleFromM(show);                    // 将输入字符串转为数值
                Command.get().getTimebase().Extent(-1, val, true);              // 下发水平档位变更指令
            }
        });
    }

    //垂直位置

    /**
     * 弹出浮层键盘编辑垂直位置参数
     * <p>根据通道类型（模拟/参考/数学）分别下发不同的垂直位置变更指令</p>
     * @param topViewEdit 垂直位置编辑控件
     */
    private void setChVPosition(TopViewEdit topViewEdit) {
        dialogFloatKeyBoard.bringToFront();                                     // 将键盘提升到最前层

        String txt;                                                             // 键盘初始文本
        if (ChannelFactory.isMathCh(chIdx)) {                                   // 数学通道
            String unit = ChannelFactory.getProbeType(chIdx);                   // 获取数学通道单位
            txt = topViewEdit.getText().replace(unit, "").replace(" ", "");     // 去除单位与空格
        } else {                                                                // 模拟/参考通道
            txt = topViewEdit.getText().replace("V", "").replace("A", "").replace(" ", ""); // 去除V/A单位与空格
        }

        dialogFloatKeyBoard.setFloatData(txt, topViewEdit, new TopDialogFloatKeyBoard.OnDismissListener() { // 设置键盘数据
            @Override
            public void onDismiss(View fromView, String show) {
                Logger.d("limh vposition= " + show);                            // 记录日志
                PlaySound.getInstance().playButton();                           // 播放按钮音效
                String unit = ChannelFactory.getProbeType(chIdx);               // 获取通道单位
                topViewEdit.setText(show + unit);                               // 显示输入值（带单位）

                double val = TBookUtil.getDoubleFromM(show);                    // 将输入字符串转为数值
                if (ChannelFactory.isDynamicCh(chIdx)) {                        // 模拟通道
                    Command.get().getChannel().Position(chIdx, val, true);      // 下发模拟通道垂直位置指令
                } else if (ChannelFactory.isRefCh(chIdx)) {                     // 参考通道
                    Command.get().getReference().Position(chIdx, val, true);    // 下发参考通道垂直位置指令
                } else if (ChannelFactory.isMathCh(chIdx)) {                    // 数学通道
                    int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx)); // 获取数学通道子类型
                    switch (mathType) {                                          // 根据子类型分发指令
                        case CacheUtil.MATHTYPE_DW:                             // 双波运算
                            Command.get().getMath_base().Offset(chIdx, val, true); // 下发基础数学偏移指令
                            break;                                              // 结束DW分支
                        case CacheUtil.MATHTYPE_FFT:                            // FFT运算
                            Command.get().getMath_fft().Offset(chIdx, val, true); // 下发FFT偏移指令
                            break;                                              // 结束FFT分支
                        case CacheUtil.MATHTYPE_AXB:                            // A×B运算
                            Command.get().getMath_axb().Offset(chIdx, val, true); // 下发AXB偏移指令
                            break;                                              // 结束AXB分支
                        case CacheUtil.MATHTYPE_AM:                             // 高级数学运算
                            Command.get().getMath_advanced().Offset(chIdx, val, true); // 下发高级数学偏移指令
                            break;                                              // 结束AM分支
                    }
                }
            }
        });

    }

    //垂直档位

    /**
     * 弹出浮层键盘编辑垂直档位参数
     * <p>根据通道类型（模拟/参考/数学）分别下发不同的垂直档位变更指令</p>
     * @param topViewEdit 垂直档位编辑控件
     */
    private void setChVScale(TopViewEdit topViewEdit) {
        dialogFloatKeyBoard.bringToFront();                                     // 将键盘提升到最前层

        String txt;                                                             // 键盘初始文本
        if (ChannelFactory.isMathCh(chIdx)) {                                   // 数学通道
            String unit = ChannelFactory.getProbeType(chIdx);                   // 获取数学通道单位
            txt = topViewEdit.getText().replace(unit, "").replace(" ", "");     // 去除单位与空格
        } else {                                                                // 模拟/参考通道
            txt = topViewEdit.getText().replace("V", "").replace("A", "").replace(" ", ""); // 去除V/A单位与空格
        }
        dialogFloatKeyBoard.setFloatData_Extent(txt, topViewEdit, new TopDialogFloatKeyBoard.OnDismissListener() { // 设置键盘数据（档位模式）
            @Override
            public void onDismiss(View fromView, String show) {
                Logger.d("limh vscale= " + show);                               // 记录日志
                PlaySound.getInstance().playButton();                           // 播放按钮音效
//                Channel channel = ChannelFactory.getDynamicChannel(chIdx);
//                String unit = channel.getProbeType() == VerticalAxis.PROBE_TYPE_VOL ? "V" : "A";
                String unit = ChannelFactory.getProbeType(chIdx);               // 获取通道单位
                topViewEdit.setText(show + unit);                               // 显示输入值（带单位）
                double d = TBookUtil.getDoubleFromM(show);                      // 将输入字符串转为数值

                if (ChannelFactory.isDynamicCh(chIdx)){                         // 模拟通道
                    Command.get().getChannel().Extent(chIdx, d, true);          // 下发模拟通道垂直档位指令
                } else if (ChannelFactory.isRefCh(chIdx)) {                     // 参考通道
                    Command.get().getReference().Vscale(chIdx, d, true);        // 下发参考通道垂直档位指令
                } else if (ChannelFactory.isMathCh(chIdx)) {                    // 数学通道
                    int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx)); // 获取数学通道子类型
                    switch (mathType) {                                          // 根据子类型分发指令
                        case CacheUtil.MATHTYPE_DW:                             // 双波运算
                            Command.get().getMath_base().Extent(chIdx, d, true); // 下发基础数学档位指令
                            break;                                              // 结束DW分支
                        case CacheUtil.MATHTYPE_FFT:                            // FFT运算
                            Command.get().getMath_fft().Extent(chIdx, d, true); // 下发FFT档位指令
                            break;                                              // 结束FFT分支
                        case CacheUtil.MATHTYPE_AXB:                            // A×B运算
                            Command.get().getMath_axb().Extent(chIdx, d, true); // 下发AXB档位指令
                            break;                                              // 结束AXB分支
                        case CacheUtil.MATHTYPE_AM:                             // 高级数学运算
                            Command.get().getMath_advanced().Extent(chIdx, d, true); // 下发高级数学档位指令
                            break;                                              // 结束AM分支
                    }
                }
            }
        });
    }


    /**
     * 返回有效档位值
     * <p>将输入值限制在通道允许的垂直档位范围内</p>
     * @param input   用户输入的档位值
     * @param channel 通道对象
     * @return 限制在有效范围内的档位值
     */
    private double getVerticalRange(double input, Channel channel) {
        double min = 0;                                                         // 最小值
        double max = 0;                                                         // 最大值

        min = VerticalAxis.getScaleIdValById(VerticalAxis.getMinGear()) * channel.getProbeRate(); // 计算最小档位（含探头倍率）
        max = VerticalAxis.getScaleIdValById(VerticalAxis.getMaxGear());        // 获取最大档位值
        if (channel.getResistanceType() == Channel.RESISTANCE_50) {             // 50欧姆阻抗
            max = VerticalAxis.getScaleIdValById(VerticalAxis.DANG_1V);         // 最大1V
        }
        max *= channel.getProbeRate();                                          // 最大值乘以探头倍率

        if (input < min) {                                                      // 低于最小值
            input = min;                                                        // 限制为最小值
        }
        if (input > max) {                                                      // 高于最大值
            input = max;                                                        // 限制为最大值
        }
        return input;                                                           // 返回限制后的值
    }

}
