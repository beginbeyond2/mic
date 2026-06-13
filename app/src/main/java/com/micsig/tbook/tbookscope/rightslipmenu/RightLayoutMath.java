package com.micsig.tbook.tbookscope.rightslipmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.WaveData;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.math.MathDualWave;
import com.micsig.tbook.scope.math.MathExprWave;
import com.micsig.tbook.scope.math.MathFFTWave;
import com.micsig.tbook.scope.math.MathWave;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainMsgSlip;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainbottom.MainBottomMsgTimeBase;
import com.micsig.tbook.tbookscope.main.mainbottom.MainHolderBottom;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogChannelLabel;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogMathFFTPersist;
import com.micsig.tbook.tbookscope.rightslipmenu.dialog.DialogSelectColor;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplay;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplayFftInfo;
import com.micsig.tbook.tbookscope.top.popwindow.TopDialogNumberPicker;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFloatKeyBoard;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardfloat.TopDialogFullFloatKeyBoard;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardformula.KeyBoardFormulaUtil;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardformula.TopDialogFormulaKeyBoard;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.MSwitchBox;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.top.view.TopViewEdit;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.util.svg.SelectorUtil;
import com.micsig.tbook.ui.util.svg.SvgNodeInfo;
import com.micsig.tbook.ui.wavezone.TChan;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2017/5/3.
 */

public class RightLayoutMath extends ConstraintLayout {
    private static final String TAG = "RightLayoutMath";
    private Context context;
    private RightViewSelect mathChannelType;//区分当前选中:DoubleWave/FFT/AX+B/AdvanceMath
    private RelativeLayout dWLayout, fftLayout, axbLayout, aMLayout;//math type layout
    private ConstraintLayout contentZone;

    private RightViewSelect dWSource1, dWSource2, dWSymbol;//double wave
    private RightViewSelect fftType, fftWindow, fftSource, fftPersist;//fft
    private RightViewSelect axbSource;//axb
    private TextView axbUnit, axbA, axbB, fftPersistValue;
    private TextView aMUnit, aMFormula;
    private TextView aMVar1Number, aMVar1Power;
    private TextView aMVar2Number, aMVar2Power;
    private RightViewSelect mathVerticalDetail;
    private RightMsgMath msgMath;
    private TopDialogTextKeyBoard layoutTextKeyBoard;
    private TopDialogFloatKeyBoard layoutFloatKeyBoard;
    private TopDialogFullFloatKeyBoard layoutFullFloatKeyBoard;
    private TopDialogFormulaKeyBoard layoutFormulaKeyBoard;
    private TopDialogNumberPicker layoutNumberPicker;
    private DialogMathFFTPersist layoutMathFFTPersist;

    private LinearLayout llDisplaySwitch;
    private MSwitchBox mSwitchBox;
    private int mathChannelNumber = TChan.Math1;

    private ConstraintLayout clImgBtn;
    private Button btnTop, btnBottom;
    private ImageView ivBackground;

    private boolean isSingleMath = false;//是否为单独设置M1.....M8模式

    private MyHandler myHandler;
    private ConstraintLayout topChannelTitle, clTopChannelGroup;
    private TextView channelTitle;
    private View space;
    private TextView btnDeleteChannel, btnAddChannel;
    private RadioButton channelMath, channelRef, channelSerials;
    private boolean needUpdateMasterLocation = false;
    private String timeBase = "";
    private boolean isShowFftInfo = true;
    private ViewGroup rootView;
    private TopViewEdit chLabel, selectColor, forDoubleClick;
    private DialogChannelLabel dialogChannelLabel;
    private DialogSelectColor dialogSelectColor;

    public RightLayoutMath(Context context) {
        this(context, null);
    }

    public RightLayoutMath(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightLayoutMath(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs, defStyleAttr);
        initControl();
        myHandler = new MyHandler(RightLayoutMath.this);
    }

    @SuppressLint("SetTextI18n")
    private void initView(AttributeSet attrs, int defStyleAttr) {
        rootView = (ViewGroup) View.inflate(context, R.layout.layout_right_math, this);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RightLayoutMath);
        int mathIndex = ta.getInt(R.styleable.RightLayoutMath_mathChannelNumber, 1);
        boolean isNormal = ta.getBoolean(R.styleable.RightLayoutMath_isNormal, false);
        ta.recycle();
        mathChannelNumber = TChan.toMathChan(mathIndex);
        space = findViewById(R.id.space);
        topChannelTitle = findViewById(R.id.top_channel_title);
        btnDeleteChannel = findViewById(R.id.btn_delete_channel);
        channelTitle = findViewById(R.id.channel_title);
        llDisplaySwitch = findViewById(R.id.ll_display_switch);
        mSwitchBox = findViewById(R.id.channel_switch);
        clImgBtn = findViewById(R.id.cl_img_btn);
        btnTop = findViewById(R.id.btnTop);
        btnBottom = findViewById(R.id.btnBottom);
        ivBackground = findViewById(R.id.img_back_src);

        mathChannelType = (RightViewSelect) findViewById(R.id.math_channel_type);
        contentZone = findViewById(R.id.right_content_zone);
        dWLayout = (RelativeLayout) findViewById(R.id.doubleWaveLayout);
        fftLayout = (RelativeLayout) findViewById(R.id.fftLayout);
        axbLayout = (RelativeLayout) findViewById(R.id.axbLayout);
        aMLayout = (RelativeLayout) findViewById(R.id.advanceMathLayout);

        dWSource1 = (RightViewSelect) findViewById(R.id.doubleWaveSource1);
        dWSource2 = (RightViewSelect) findViewById(R.id.doubleWaveSource2);
        dWSymbol = (RightViewSelect) findViewById(R.id.doubleWaveSymbol);

        fftWindow = (RightViewSelect) findViewById(R.id.fftWindow);
        fftSource = (RightViewSelect) findViewById(R.id.fftSource);
        fftPersist = findViewById(R.id.fftPersist);

        fftType = (RightViewSelect) findViewById(R.id.fftType);
        fftPersistValue = findViewById(R.id.fftPersistValue);

        axbSource = (RightViewSelect) findViewById(R.id.axbSource);
        axbUnit = (TextView) findViewById(R.id.axbUnitDetail);
        axbA = (TextView) findViewById(R.id.axbADetail);
        axbB = (TextView) findViewById(R.id.axbBDetail);

        aMUnit = (TextView) findViewById(R.id.advanceMathUnitDetail);
        aMFormula = (TextView) findViewById(R.id.advanceMathFormula);
        aMVar1Number = (TextView) findViewById(R.id.advanceMathVar1Number);
        aMVar1Power = (TextView) findViewById(R.id.advanceMathVar1Power);
        aMVar2Number = (TextView) findViewById(R.id.advanceMathVar2Number);
        aMVar2Power = (TextView) findViewById(R.id.advanceMathVar2Power);
        chLabel = findViewById(R.id.chLabel);
        selectColor = findViewById(R.id.select_color);
        forDoubleClick = findViewById(R.id.for_doubleClick);

        mathVerticalDetail = (RightViewSelect) findViewById(R.id.mathVerticalBaseDetail);

        String[] channels = GlobalVar.get().getChannelsName();
        if (channels.length == 2) {
            dWSource1.setItemMarginTop(25);
            dWSource2.setItemMarginTop(25);
        }
        dWSource1.setArray(channels);
        dWSource2.setArray(channels);
        fftSource.setArray(channels);
        axbSource.setArray(channels);

        clTopChannelGroup = findViewById(R.id.cl_top_channel_group);
        channelMath = findViewById(R.id.channelMath);
        channelRef = findViewById(R.id.channelRef);
        channelSerials = findViewById(R.id.channelSerials);
        btnAddChannel = findViewById(R.id.btn_add_channel);
        channelMath.setOnClickListener(onClickListener);
        channelRef.setOnClickListener(onClickListener);
        channelSerials.setOnClickListener(onClickListener);
        btnAddChannel.setOnClickListener(onClickListener);

        mSwitchBox.setOnToggleStateChangedListener(onToggleStateChangedListener);
        mathChannelType.setOnItemClickListener(onRightSlipViewSelectItemClickListener);
        dWSource1.setOnItemClickListener(onRightSlipViewSelectItemClickListener);
        dWSource2.setOnItemClickListener(onRightSlipViewSelectItemClickListener);
        dWSymbol.setOnItemClickListener(onRightSlipViewSelectItemClickListener);
        fftWindow.setOnItemClickListener(onRightSlipViewSelectItemClickListener);
        fftSource.setOnItemClickListener(onRightSlipViewSelectItemClickListener);
        fftPersist.setOnItemClickListener(onRightSlipViewSelectItemClickListener);
        fftType.setOnItemClickListener(onRightSlipViewSelectItemClickListener);
        fftPersistValue.setOnClickListener(onClickListener);
        axbSource.setOnItemClickListener(onRightSlipViewSelectItemClickListener);
        axbUnit.setOnClickListener(onClickListener);
        axbA.setOnClickListener(onClickListener);
        axbB.setOnClickListener(onClickListener);
        aMUnit.setOnClickListener(onClickListener);
        aMFormula.setOnClickListener(onClickListener);
        aMVar1Number.setOnClickListener(onClickListener);
        aMVar1Power.setOnClickListener(onClickListener);
        aMVar2Number.setOnClickListener(onClickListener);
        aMVar2Power.setOnClickListener(onClickListener);
        mathVerticalDetail.setOnItemClickListener(onRightSlipViewSelectItemClickListener);
        btnTop.setOnClickListener(onClickListener);
        btnBottom.setOnClickListener(onClickListener);
        btnDeleteChannel.setOnClickListener(onClickListener);
        chLabel.setOnClickEditListener(onClickEditListener);
        selectColor.setOnClickEditListener(onClickEditListener);
        forDoubleClick.setOnClickEditListener(onClickEditListener);

        layoutTextKeyBoard = (TopDialogTextKeyBoard) ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard);
        layoutFloatKeyBoard = (TopDialogFloatKeyBoard) ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);
        layoutFullFloatKeyBoard = (TopDialogFullFloatKeyBoard) ((MainActivity) context).findViewById(R.id.dialogFullFloatKeyBoard);
        layoutFormulaKeyBoard = (TopDialogFormulaKeyBoard) ((MainActivity) context).findViewById(R.id.dialogFormulaKeyBoard);
        layoutNumberPicker = (TopDialogNumberPicker) ((MainActivity) context).findViewById(R.id.dialogNumberPicker);
        layoutMathFFTPersist = ((MainActivity) context).findViewById(R.id.dialogMathFFTPersist);

        channelTitle.setText("M" + TChan.toMathNumber(mathChannelNumber));
        initData();
        setControlColorByChIdx(mathChannelNumber);
    }

    private void setControlColorByChIdx(int chIdx) {
        channelTitle.setTextColor(TChan.getChannelColor(context, chIdx));
        mSwitchBox.setControlColorByChIdx(chIdx);
        mathChannelType.setControlColorByChIdx(chIdx);
        dWSource1.setControlColorByChIdx(chIdx);
        dWSource2.setControlColorByChIdx(chIdx);
        dWSymbol.setControlColorByChIdx(chIdx);
        fftWindow.setControlColorByChIdx(chIdx);
        fftSource.setControlColorByChIdx(chIdx);
        fftPersist.setControlColorByChIdx(chIdx);
        fftType.setControlColorByChIdx(chIdx);
        axbSource.setControlColorByChIdx(chIdx);
        mathVerticalDetail.setControlColorByChIdx(chIdx);
        channelMath.setTextColor(TChan.getChannelColor(context, chIdx));
//        selectColor.setEditColor(SvgNodeInfo.getAllBaseColor(chIdx));
        int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + chIdx);
        mathChannelType.setSelectIndex(mathType);
    }


    private void setOnlyControlColorByChIdx(int chIdx) {
        channelTitle.setTextColor(TChan.getChannelColor(context, chIdx));
        mSwitchBox.setControlColorByChIdx(chIdx);
        mathChannelType.setOnlyControlColorByChIdx(chIdx);
        dWSource1.setOnlyControlColorByChIdx(chIdx);
        dWSource2.setOnlyControlColorByChIdx(chIdx);
        dWSymbol.setOnlyControlColorByChIdx(chIdx);
        fftWindow.setOnlyControlColorByChIdx(chIdx);
        fftSource.setOnlyControlColorByChIdx(chIdx);
        fftPersist.setOnlyControlColorByChIdx(chIdx);
        fftType.setOnlyControlColorByChIdx(chIdx);
        axbSource.setOnlyControlColorByChIdx(chIdx);
        mathVerticalDetail.setOnlyControlColorByChIdx(chIdx);
        channelMath.setTextColor(TChan.getChannelColor(context, chIdx));
        int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + chIdx);
        mathChannelType.setSelectIndex(mathType);
    }

    private void initData() {
        msgMath = new RightMsgMath();
        msgMath.setMathCheck(mSwitchBox.isState());
        msgMath.setMathType(CacheUtil.MATHTYPE_DW);//TODO 后续去掉 用setMathTypeSelect代替
        msgMath.setMathTypeSelect(mathChannelType.getSelectItem());
        msgMath.setDwSource1(dWSource1.getSelectItem());
        msgMath.setDwSource2(dWSource2.getSelectItem());
        msgMath.setDwSymbol(dWSymbol.getSelectItem());
        msgMath.setFftType(fftType.getSelectItem().getText());
        msgMath.setFftSource(fftSource.getSelectItem());
        msgMath.setFftWindow(fftWindow.getSelectItem());
        msgMath.setFftPersist(fftPersist.getSelectItem());
        msgMath.setAxbUnit(axbUnit.getText().toString());
        msgMath.setAxbSource(axbSource.getSelectItem());
        msgMath.setAxbA(axbA.getText().toString());
        msgMath.setAxbB(axbB.getText().toString());
        msgMath.setAmUnit(aMUnit.getText().toString());
        msgMath.setAmFormula(aMFormula.getText().toString());
        msgMath.setAmVar1Number(aMVar1Number.getText().toString());
        msgMath.setAmVar1Power(aMVar1Power.getText().toString());
        msgMath.setAmVar2Number(aMVar2Number.getText().toString());
        msgMath.setAmVar2Power(aMVar2Power.getText().toString());
        msgMath.setMathChannelNumber(mathChannelNumber);
        msgMath.setLabel(chLabel.getText());
    }

    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_TIMEBASE).subscribe(consumerMainBottomTimeBase);
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOther);
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_SHOW_NORMAL_STATE).subscribe(consumerShowNormalState);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_OTHER_CHANNEL_CAN_ADD_MATH).subscribe(consumerOtherChannelCanAdd);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_VERTICAL_SCALE).subscribe(consumerChannelVscale);
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_DISPLAY).subscribe(consumerTopSlipTitle);
        EventFactory.addEventObserver(EventFactory.EVENT_MATH_WAVE_UPDATE, eventUIObserver);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_DELETE_OTHER_CHANNEL).subscribe(consumerDeleteChannel);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR).subscribe(consumerSelectColor);
        RxBus.getInstance().getObservable(RxEnum.MQ_MSG_MOUSE_CLICK_POSITION).subscribe(consumerMouseClick);
    }

    String[] mathTypeArray = App.get().getResources().getStringArray(R.array.math_channel_type);
    String[] sourceArray = App.get().getResources().getStringArray(R.array.channelsNameEight);
    String[] dWSymbolArray = App.get().getResources().getStringArray(R.array.mathSymbol);
    String[] fftTypeArray = App.get().getResources().getStringArray(R.array.mathFftType);
    String[] fftWindowArray = App.get().getResources().getStringArray(R.array.mathWindow);
    String[] fftPersistArray = App.get().getResources().getStringArray(R.array.mathFftPersist);

    private void setCache() {
        boolean isAddByUser = isAddByUser(mathChannelNumber);
        boolean mathCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChannelNumber);
        int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChannelNumber);
        String label = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_LABEL + mathChannelNumber);
        //DoubleWave
        int dwS1 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_DW_SOURCE1 + mathChannelNumber);
        int dwS2 = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_DW_SOURCE2 + mathChannelNumber);
        int dwSymbol = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_DW_SYMBOL + mathChannelNumber);
        //FFT
        int fftType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + mathChannelNumber);
        int fftSource = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_SOURCE + mathChannelNumber);
        int fftWindow = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_WINDOW + mathChannelNumber);
        int fftPersist = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_PERSIST + mathChannelNumber);
        String fftPersistValue = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_FFT_PERSIST_VALUE + mathChannelNumber);
        //AX+B
        String axbUnit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_UNIT + mathChannelNumber);
        int axbSource = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_AXB_SOURCE + mathChannelNumber);
        String axbA = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_A + mathChannelNumber);
        String axbB = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_B + mathChannelNumber);
        //AdvanceMath
        String amUnit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_UNIT + mathChannelNumber);
        String amFormula = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA + mathChannelNumber);
        String amVar1Number = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR1_NUMBER + mathChannelNumber);
        String amVar1Power = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR1_POWER + mathChannelNumber);
        String amVar2Number = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR2_NUMBER + mathChannelNumber);
        String amVar2Power = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR2_POWER + mathChannelNumber);

        //V-Scale Ref
        int mathVerticalIndex = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_VERTICALBASE + mathChannelNumber);

        mSwitchBox.setState(mathCheck);
        dWSource1.setSelectIndex(dwS1);
        dWSource2.setSelectIndex(dwS2);
        dWSymbol.setSelectIndex(dwSymbol);
        this.fftType.setSelectIndex(fftType);
        this.fftSource.setSelectIndex(fftSource);
        this.fftWindow.setSelectIndex(fftWindow);
        this.fftPersist.setSelectIndex(fftPersist);
        this.fftPersistValue.setText(fftPersistValue);
        setFftPersistValueEnable();
        this.axbUnit.setText(axbUnit);
        this.axbSource.setSelectIndex(axbSource);
        this.axbA.setText(axbA);
        this.axbB.setText(axbB);
        this.aMUnit.setText(amUnit);
        this.aMFormula.setText(amFormula);
        this.aMVar1Number.setText(amVar1Number);
        this.aMVar1Power.setText(amVar1Power);
        this.aMVar2Number.setText(amVar2Number);
        this.aMVar2Power.setText(amVar2Power);
        this.mathVerticalDetail.setSelectIndex(mathVerticalIndex);
        this.chLabel.setText(label);
        mathChannelType.setSelectIndex(mathType);

        WaveManage.get().setChannelLabel(mathChannelNumber, label);
        setChannelLabel(TChan.toFpgaChNo(mathChannelNumber), label);

        Command.get().getChannel().Display(TChan.toFpgaChNo(mathChannelNumber), mathCheck && isAddByUser, false);
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChannelNumber));
        if (mathChannel == null) return;
        mathChannel.setVerticalMode(mathVerticalIndex);
        MathDualWave dualWave = mathChannel.getMathDualWave();
        dualWave.setSource1(dwS1);
        dualWave.setSource2(dwS2);
        dualWave.setOperator(dwSymbol);
        MathFFTWave fftWave = mathChannel.getMathFFTWave();
        fftWave.setFFTType(fftType);
        fftWave.setSource(fftSource);
        fftWave.setFFTWindow(fftWindow);

        setScopeAxb(mathChannelNumber);

        MathExprWave mathExprWave = mathChannel.getMathExprWave();
        setScopeAM(mathChannelNumber);
        double var1 = Double.parseDouble(amVar1Number) * Math.pow(10, Double.parseDouble(amVar1Power));
        mathExprWave.setVar1(var1);
        double var2 = Double.parseDouble(amVar2Number) * Math.pow(10, Double.parseDouble(amVar2Power));
        mathExprWave.setVar2(var2);

        setShowMathTypeLayout(mathType);
        setCacheMathType(mathType, false, mathChannelNumber);

        Command.get().getMath().VRef(TChan.toFpgaChNo(mathChannelNumber), mathVerticalIndex, false);
        Command.get().getMath().Mode(TChan.toFpgaChNo(mathChannelNumber), mathType, false);

        Command.get().getMath_base().S1(TChan.toFpgaChNo(mathChannelNumber), dwS1, false);
        Command.get().getMath_base().S2(TChan.toFpgaChNo(mathChannelNumber), dwS2, false);
        Command.get().getMath_base().Operator(TChan.toFpgaChNo(mathChannelNumber), dwSymbol, false);

        Command.get().getMath_fft().Type(TChan.toFpgaChNo(mathChannelNumber), fftType, false);
        Command.get().getMath_fft().Source(TChan.toFpgaChNo(mathChannelNumber), fftSource, false);
        Command.get().getMath_fft().Window(TChan.toFpgaChNo(mathChannelNumber), fftWindow, false);

        Command.get().getMath_axb().Source(TChan.toFpgaChNo(mathChannelNumber), axbSource, false);
        Command.get().getMath_axb().A(TChan.toFpgaChNo(mathChannelNumber), axbA, false);
        Command.get().getMath_axb().B(TChan.toFpgaChNo(mathChannelNumber), axbB, false);
        Command.get().getMath_axb().Unit(TChan.toFpgaChNo(mathChannelNumber), axbUnit, false);

        Command.get().getMath_advanced().Expression(TChan.toFpgaChNo(mathChannelNumber), amFormula, false);
        Command.get().getMath_advanced().Var1(TChan.toFpgaChNo(mathChannelNumber), var1, false);
        Command.get().getMath_advanced().Var2(TChan.toFpgaChNo(mathChannelNumber), var2, false);
        Command.get().getMath_advanced().Unit(TChan.toFpgaChNo(mathChannelNumber), amUnit, false);

        String colorStr = CacheUtil.get().getString(CacheUtil.MAIN_CHANNEL_COLOR + mathChannelNumber);
        selectColor.setEditColor(colorStr);

        msgMath.setMathCheck(mathCheck);
        msgMath.setMathType(mathType);
        msgMath.setMathTypeSelect(new RightBeanSelect(mathType, mathTypeArray[mathType], mathCheck));
        msgMath.setDwSource1(new RightBeanSelect(dwS1, sourceArray[dwS1], true));
        msgMath.setDwSource2(new RightBeanSelect(dwS2, sourceArray[dwS2], true));
        msgMath.setDwSymbol(new RightBeanSelect(dwSymbol, dWSymbolArray[dwSymbol], true));
        msgMath.setFftType(fftTypeArray[fftType]);
        msgMath.setFftSource(new RightBeanSelect(fftSource, sourceArray[fftSource], true));
        msgMath.setFftWindow(new RightBeanSelect(fftWindow, fftWindowArray[fftWindow], true));
        msgMath.setFftPersist(new RightBeanSelect(fftPersist, fftPersistArray[fftPersist], true));
        msgMath.setAxbUnit(axbUnit);
        msgMath.setAxbSource(new RightBeanSelect(axbSource, sourceArray[axbSource], true));
        msgMath.setAxbA(axbA);
        msgMath.setAxbB(axbB);
        msgMath.setAmUnit(amUnit);
        msgMath.setAmFormula(amFormula);
        msgMath.setAmVar1Number(amVar1Number);
        msgMath.setAmVar1Power(amVar1Power);
        msgMath.setAmVar2Number(amVar2Number);
        msgMath.setAmVar2Power(amVar2Power);
        msgMath.setLabel(label);
        sendMsg();
    }

    private void setFftPersistValueEnable() {
        if (fftPersist.getSelectIndex() == 0 || fftPersist.getSelectIndex() == 1) {
            this.fftPersistValue.setEnabled(false);
        } else {
            this.fftPersistValue.setEnabled(true);
        }
    }

    /**
     * chan 对应通道 TChan.Math1---TChan.Math8
     */
    private void setScopeAxb(int chan) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(chan));
        if (mathChannel == null) return;
        String axbUnit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_UNIT + chan);
        int axbSource = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_AXB_SOURCE + chan);
        String axbA = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_A + chan);
        String axbB = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_B + chan);
        double a = TBookUtil.getDoubleFromM(axbA);
        double b = TBookUtil.getDoubleFromM(axbB);
        if (!TextUtils.isEmpty(this.axbA.getText().toString())) {
            a = TBookUtil.getDoubleFromM(this.axbA.getText().toString());
        }
        if (!TextUtils.isEmpty(this.axbB.getText().toString())) {
            b = TBookUtil.getDoubleFromM(this.axbB.getText().toString());
        }
        MathExprWave exprWave = mathChannel.getMathExprWave();
        exprWave.clearSource();
        exprWave.addSource(axbSource);
        mathChannel.setProbeStr(axbUnit);
        exprWave.setExprString("(" + a + ") * " + getChannelName(axbSource) + " + (" + b + ")");
    }


    /**
     * chan 对应通道 TChan.Math1---TChan.Math8
     */
    private void setScopeAM(int chan) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(chan));
        if (mathChannel == null) return;
        String amUnit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_UNIT + chan);
        String amFormula = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA + chan);
        String amVar1Number = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR1_NUMBER + chan);
        String amVar1Power = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR1_POWER + chan);
        String amVar2Number = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR2_NUMBER + chan);
        String amVar2Power = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_VAR2_POWER + chan);

        MathExprWave mathExprWave = mathChannel.getMathExprWave();
        mathExprWave.clearSource();
        if (amFormula.contains("ch1")) {
            mathExprWave.addSource(ChannelFactory.CH1);
        }
        if (amFormula.contains("ch2")) {
            mathExprWave.addSource(ChannelFactory.CH2);
        }
        if (amFormula.contains("ch3")) {
            mathExprWave.addSource(ChannelFactory.CH3);
        }
        if (amFormula.contains("ch4")) {
            mathExprWave.addSource(ChannelFactory.CH4);
        }
        if (amFormula.contains("ch5")) {
            mathExprWave.addSource(ChannelFactory.CH5);
        }
        if (amFormula.contains("ch6")) {
            mathExprWave.addSource(ChannelFactory.CH6);
        }
        if (amFormula.contains("ch7")) {
            mathExprWave.addSource(ChannelFactory.CH7);
        }
        if (amFormula.contains("ch8")) {
            mathExprWave.addSource(ChannelFactory.CH8);
        }
        mathChannel.setProbeStr(amUnit);
        mathExprWave.setVar1(Double.parseDouble(amVar1Number)
                * Math.pow(10, Double.parseDouble(amVar1Power)));
        mathExprWave.setVar2(Double.parseDouble(amVar2Number)
                * Math.pow(10, Double.parseDouble(amVar2Power)));
        amFormula = amFormula.replace(App.get().getResources().getString(R.string.key_formula_tb), handleTimeBase(MainHolderBottom.getCenterTimeBase()));
        amFormula = KeyBoardFormulaUtil.amFormulaToScope(amFormula, MainHolderBottom.getCenterTimeBase());
        mathExprWave.setExprString(amFormula);
    }

    /**
     * 该float数据是否有效
     */
    private boolean isFloatValid(float f) {
        return !(Float.isInfinite(f) || Float.isNaN(f));
    }

    private String getChannelName(int channelIndex) {
        switch (channelIndex) {
            case 1:
                return "ch2";
            case 2:
                return "ch3";
            case 3:
                return "ch4";
            case 4:
                return "ch5";
            case 5:
                return "ch6";
            case 6:
                return "ch7";
            case 7:
                return "ch8";
            default:
                return "ch1";
        }
    }

    private Consumer consumerLoadCache = new Consumer() {
        @Override
        public void accept(@NonNull Object o) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_RightLayoutMath, true);
        }
    };


    private void setCacheMathType(int type, boolean isFromEventBus, int channelNumber) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(channelNumber));
        if (mathChannel == null) return;
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_TYPE + channelNumber, String.valueOf(type));
        if (!isFromEventBus) {
            if (type == CacheUtil.MATHTYPE_DW) {
                mathChannel.setMathType(MathWave.MATH_DUALWAVE);
            } else if (type == CacheUtil.MATHTYPE_FFT) {
                mathChannel.setMathType(MathWave.MATH_FFTWAVE);
            } else if (type == CacheUtil.MATHTYPE_AXB) {
                mathChannel.setProbeStr(axbUnit.getText().toString());
                mathChannel.setMathType(MathWave.MATH_EXPR);
                setScopeAxb(channelNumber);
            } else if (type == CacheUtil.MATHTYPE_AM) {
                mathChannel.setProbeStr(aMUnit.getText().toString());
                mathChannel.setMathType(MathWave.MATH_EXPR);
                setScopeAM(channelNumber);
            }
        }
    }

    private void setShowMathTypeLayout(int mathType) {
        for (int i = 0; i < contentZone.getChildCount(); i++) {
            contentZone.getChildAt(i).setVisibility(GONE);
        }
        //dWLayout, fftLayout, axbLayout, aMLayout
        boolean selectFft = false;
        switch (mathType) {
            case CacheUtil.MATHTYPE_FFT:
                selectFft = true;
                fftLayout.setVisibility(VISIBLE);
                break;
            case CacheUtil.MATHTYPE_AXB:
                axbLayout.setVisibility(VISIBLE);
                break;
            case CacheUtil.MATHTYPE_AM:
                aMLayout.setVisibility(VISIBLE);
                break;
            default:
                dWLayout.setVisibility(VISIBLE);
                break;
        }
        ShowFFTMeasureInfo(selectFft);
    }

    private void setMathPos(int mathType) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChannelNumber));
        if(mathChannel == null) return;
        boolean change = false;
        if (mathChannel.getVerticalMode() == MathChannel.VERTICAL_MODE_SCREEN_CENTER) {
            mathChannel.setVerticalMode(MathChannel.VERTICAL_MODE_CH_ZERO);
            change = true;
        }
        setMathWaveVScaleId(mathType);
        mathChannel.setPos(Tools.getYTChannelPosition(mathChannelNumber));
        if (change) {
            mathChannel.setVerticalMode(MathChannel.VERTICAL_MODE_SCREEN_CENTER);
        }
    }

    private void setMathWaveVScaleId(int mathType) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChannelNumber));
        if (mathChannel == null) return;
        if (mathType == CacheUtil.MATHTYPE_AXB) {
            MathExprWave exprWave = mathChannel.getMathExprWave();
            exprWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_AXB_VSCALE_ID + mathChannelNumber));
        } else if (mathType == CacheUtil.MATHTYPE_DW) {
            MathDualWave dualWave = mathChannel.getMathDualWave();
            dualWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_DW_VSCALE_ID + mathChannelNumber));
        } else if (mathType == CacheUtil.MATHTYPE_AM) {
            MathExprWave exprWave = mathChannel.getMathExprWave();
            exprWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_AM_VSCALE_ID + mathChannelNumber));
        } else {
            if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + mathChannelNumber) == 1) {
                MathFFTWave mathFFTWave = mathChannel.getMathFFTWave();
                mathFFTWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_FFT_DB_VSCALE_ID + mathChannelNumber));
            } else {
                MathFFTWave mathFFTWave = mathChannel.getMathFFTWave();
                mathFFTWave.setVScaleId(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_MATH_FFT_RMS_VSCALE_ID + mathChannelNumber));
            }
        }
    }

    private void sendMsg() {
        RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_MATH, msgMath);
    }

    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() {
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception {
            switch (commandMsgToUI.getFlag()) {
                case CommandMsgToUI.FLAG_CHANNEL_DISPLAY: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    boolean isOpen = Boolean.parseBoolean(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    mSwitchBox.setState(isOpen);
                    onToggleStateChangedListener.onToggleStateChanged(mSwitchBox, mSwitchBox.isState());
                }
                break;
                case CommandMsgToUI.FLAG_MATH_BASE_OPERATOR: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    int index = Integer.parseInt(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    setShowMathTypeLayout(CacheUtil.MATHTYPE_DW);
                    dWSymbol.setSelectIndex(index);
                    selectItemIndex(dWSymbol.getId(), dWSymbol.getSelectItem());
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_MODE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    int index = Integer.parseInt(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    mathChannelType.setSelectIndex(index);
                    switch (index) {
                        case 0:
                            setShowMathTypeLayout(CacheUtil.MATHTYPE_DW);
                            setCacheMathType(CacheUtil.MATHTYPE_DW, false, mathChannelNumber);
                            setMathPos(CacheUtil.MATHTYPE_DW);
                            msgMath.setMathType(CacheUtil.MATHTYPE_DW);
                            break;
                        case 1:
                            setShowMathTypeLayout(CacheUtil.MATHTYPE_FFT);
                            setCacheMathType(CacheUtil.MATHTYPE_FFT, false, mathChannelNumber);
                            setMathPos(CacheUtil.MATHTYPE_FFT);
                            msgMath.setMathType(CacheUtil.MATHTYPE_FFT);
                            break;
                        case 2:
                            setShowMathTypeLayout(CacheUtil.MATHTYPE_AXB);
                            setCacheMathType(CacheUtil.MATHTYPE_AXB, false, mathChannelNumber);
                            setMathPos(CacheUtil.MATHTYPE_AXB);
                            msgMath.setMathType(CacheUtil.MATHTYPE_AXB);
                            break;
                        case 3:
                            setShowMathTypeLayout(CacheUtil.MATHTYPE_AM);
                            setCacheMathType(CacheUtil.MATHTYPE_AM, false, mathChannelNumber);
                            setMathPos(CacheUtil.MATHTYPE_AM);
                            msgMath.setMathType(CacheUtil.MATHTYPE_AM);
                            break;
                    }
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_BASE_S1:
                case CommandMsgToUI.FLAG_MATH_ADDS1:
                case CommandMsgToUI.FLAG_MATH_SUBS1:
                case CommandMsgToUI.FLAG_MATH_MULS1:
                case CommandMsgToUI.FLAG_MATH_DIVS1: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    int selectIndex = Integer.parseInt(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    dWSource1.setSelectIndex(selectIndex);
                    selectItemIndex(dWSource1.getId(), dWSource1.getSelectItem());
                    msgMath.setDwSource1(dWSource1.getSelectItem());
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_BASE_S2:
                case CommandMsgToUI.FLAG_MATH_ADDS2:
                case CommandMsgToUI.FLAG_MATH_SUBS2:
                case CommandMsgToUI.FLAG_MATH_MULS2:
                case CommandMsgToUI.FLAG_MATH_DIVS2: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    int selectIndex = Integer.parseInt(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    dWSource2.setSelectIndex(selectIndex);
                    selectItemIndex(dWSource2.getId(), dWSource2.getSelectItem());
                    msgMath.setDwSource2(dWSource2.getSelectItem());
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_FFTSOURCE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    int selectIndex = Integer.parseInt(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    fftSource.setSelectIndex(selectIndex);
                    selectItemIndex(fftSource.getId(), fftSource.getSelectItem());
                    msgMath.setFftSource(fftSource.getSelectItem());
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_FFTWINDOW: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    int selectIndex = Integer.parseInt(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    fftWindow.setSelectIndex(selectIndex);
                    selectItemIndex(fftWindow.getId(), fftWindow.getSelectItem());
                    msgMath.setFftWindow(fftWindow.getSelectItem());
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_FFTTYPE: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    int selectIndex = Integer.parseInt(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    fftType.setSelectIndex(selectIndex);
                    selectItemIndex(fftType.getId(), fftType.getSelectItem());
                    msgMath.setFftType(fftType.getSelectItem().getText());
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_AXB_Source: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    int selectIndex = Integer.parseInt(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    axbSource.setSelectIndex(selectIndex);
                    selectItemIndex(axbSource.getId(), axbSource.getSelectItem());
                    msgMath.setAxbSource(axbSource.getSelectItem());
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_AXB_A: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    String show = String.valueOf(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    axbA.setText(show);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AXB_A + mathChannelNumber, show);
                    setScopeAxb(mathChannelNumber);
                    msgMath.setAxbA(axbA.getText().toString());
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_AXB_B: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    String show = String.valueOf(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    axbB.setText(show);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AXB_B + mathChannelNumber, show);
                    setScopeAxb(mathChannelNumber);
                    msgMath.setAxbB(axbB.getText().toString());
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_AXB_UNIT: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    String result = String.valueOf(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    axbUnit.setText(result);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AXB_UNIT + mathChannelNumber, result);
                    setCacheMathType(CacheUtil.MATHTYPE_AXB, false, mathChannelNumber);
                    msgMath.setAxbUnit(result);
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_ADV_Express: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    String param = String.valueOf(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    aMFormula.setText(param);
                    setScopeAM(TChan.toUiChNo(chIndex));
                    msgMath.setAmFormula(aMFormula.getText().toString());
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_ADV_Var1: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    String param2 = String.valueOf(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    String[] param = param2.split("E");
                    aMVar1Number.setText(param[0]);
                    aMVar1Power.setText(param[1]);
                    msgMath.setAmVar1Number(aMVar1Number.getText().toString());
                    msgMath.setAmVar1Power(aMVar1Power.getText().toString());
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_ADV_Var2: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    String param2 = String.valueOf(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    String[] param = param2.split("E");
                    aMVar2Number.setText(param[0]);
                    aMVar2Power.setText(param[1]);
                    msgMath.setAmVar2Number(aMVar2Number.getText().toString());
                    msgMath.setAmVar2Power(aMVar2Power.getText().toString());
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_ADV_Unit: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    String param = String.valueOf(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    aMUnit.setText(param);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_UNIT + mathChannelNumber, param);
                    setCacheMathType(CacheUtil.MATHTYPE_AM, false, mathChannelNumber);
                    msgMath.setAmUnit(aMUnit.getText().toString());
                    sendMsg();
                }
                break;
                case CommandMsgToUI.FLAG_MATH_VREF: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    int param = Integer.parseInt(params[1]);
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    mathVerticalDetail.setSelectIndex(param);
                    onRightSlipViewSelectItemClickListener.onItemClick(mathVerticalDetail.getId(), mathVerticalDetail.getSelectItem());
                }
                break;
                case CommandMsgToUI.FLAG_MATH_LABEL: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    String result = params[1];
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    chLabel.setText(result);
                    msgMath.setLabel(result);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + mathChannelNumber, result);
                    WaveManage.get().setChannelLabel(mathChannelNumber, result);
                    setChannelLabel(chIndex, result);
                }
                break;
                case CommandMsgToUI.FLAG_MATH_LABEL_CLEAR: {
                    String[] params = commandMsgToUI.getParam().split(CommandMsgToUI.PARAM_SPLIT);
                    int chIndex = Integer.parseInt(params[0]);
                    String result = "";
//                    Logger.i(Command.TAG, "label clear");
                    if (chIndex != TChan.toFpgaChNo(mathChannelNumber)) return;
                    chLabel.setText(result);
                    msgMath.setLabel(result);
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + mathChannelNumber, result);
                    WaveManage.get().setChannelLabel(mathChannelNumber, result);
                    setChannelLabel(chIndex, result);
                }

            }
        }
    };

    private Consumer<MainBottomMsgTimeBase> consumerMainBottomTimeBase = new Consumer<MainBottomMsgTimeBase>() {
        @Override
        public void accept(MainBottomMsgTimeBase msgTimeBase) throws Exception {
            int chIdx = ChannelFactory.getChActivate();
            if (ChannelFactory.isMathCh(chIdx) && mathChannelNumber == chIdx + 1) {
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChannelNumber);
                if (mathChannelNumber == TChan.toUiChNo(chIdx) && mathType == CacheUtil.MATHTYPE_AM) {

                    timeBase = msgTimeBase.getTimeBase();
                    //时基变化处理  待验证
                    Command.get().getMath_advanced().Expression(TChan.toFpgaChNo(mathChannelNumber), aMFormula.getText().toString(), false);

                    setScopeAM(mathChannelNumber);
//                    sendMsg();
                }
            }
        }
    };

    /**
     * 数学通道关闭或打开
     */
    private Consumer<MainRightMsgOthers> consumerMainRightOther = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers mainRightMsgOthers) throws Exception {
            if (mSwitchBox.isState() == mainRightMsgOthers.getMath(mathChannelNumber).isValue()) {
                return;
            }
            boolean isOpen = mainRightMsgOthers.getMath(mathChannelNumber).isValue();
            mSwitchBox.setState(isOpen);
            Command.get().getChannel().Display(TChan.toFpgaChNo(mathChannelNumber), isOpen && isAddByUser(mathChannelNumber), false);
            msgMath.setMathCheck(mSwitchBox.isState());

            if (!CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChannelNumber)) {
                ShowFFTMeasureInfo(false);
            } else {
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChannelNumber);
                ShowFFTMeasureInfo(mathType == CacheUtil.MATHTYPE_FFT);
            }
        }
    };

    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void update(Object data) {
            if (((EventBase) data).getId() == EventFactory.EVENT_MATH_WAVE_UPDATE) {
                int mathNumber = (int) ((EventBase) data).getData();
                if(TChan.toUiChNo(mathNumber) != mathChannelNumber) return;
                String DC = "---";
                String Amp = "---";
                String Freq = "---";
                MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChannelNumber));
                if (mathChannel == null) return;
                int type = mathChannel.getMathFFTWave().getFFTType();
                if (MathFFTWave.FFT_TYPE_DB == type) {
                    // db
                    DC = String.format("%.2f", mathChannel.getFFTDCVal()) + "dB";
                    Amp = String.format("%.2f", mathChannel.getFFTMaxVal()) + "dB";
                } else if (MathFFTWave.FFT_TYPE_RMS == type) {
                    // 幅值 v
                    String unit = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE
                            + (TChan.toUiChNo(fftSource.getSelectIndex()))) == 0 ? "V" : "A";
                    DC = TBookUtil.getFourFromD_(mathChannel.getFFTDCVal()) + unit;
                    Amp = TBookUtil.getFourFromD_(mathChannel.getFFTMaxVal()) + unit;
                }
                Freq = TBookUtil.getFourFromD_(mathChannel.getFFTMaxIdxFreq()) + "Hz";
                MeasureManage.getInstance().getFftMeasure(TChan.toMathNumber(mathChannelNumber)).setData(DC, Amp, Freq);
            }
        }
    };

    private Consumer<String> consumerShowNormalState = new Consumer<String>() {
        @Override
        public void accept(String String) throws Exception {
            String[] params = String.split(CommandMsgToUI.PARAM_SPLIT);
            int channelNumber = Integer.parseInt(params[0]);
            boolean isNormal = Boolean.parseBoolean(params[1]);

            if (channelNumber != mathChannelNumber) return;
            if (isNormal) {//M1...M8布局样式
                llDisplaySwitch.setVisibility(View.VISIBLE);//switch 显示
                topChannelTitle.setVisibility(View.VISIBLE);//Mx delete按钮 显示
                space.setVisibility(View.GONE);//空占位
                clTopChannelGroup.setVisibility(View.GONE);//Math/Ref/Serial 不显示
                clImgBtn.setVisibility(View.VISIBLE);//显示挡位调节按钮
            } else {//Math/Ref/Serials共同显示布局样式
                llDisplaySwitch.setVisibility(View.INVISIBLE);//switch 不显示
                topChannelTitle.setVisibility(View.INVISIBLE);//Mx delete按钮 不显示
                space.setVisibility(View.GONE);//空占位
                clTopChannelGroup.setVisibility(View.VISIBLE);//Math/Ref/Serial 显示
                clImgBtn.setVisibility(View.INVISIBLE);//不显示挡位调节按钮
            }
            String label = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_LABEL + mathChannelNumber);
            chLabel.setText(label);
        }
    };

    private Consumer<String> consumerOtherChannelCanAdd = new Consumer<String>() {
        @Override
        public void accept(String available) throws Throwable {
            if (channelMath == null || channelRef == null || channelSerials == null) return;
            String[] params = available.split(CommandMsgToUI.PARAM_SPLIT);
            int mathSlideIndex = Integer.parseInt(params[0]);
            boolean mathAvailable = Boolean.parseBoolean(params[1]);
            boolean refAvailable = Boolean.parseBoolean(params[2]);
            boolean serialAvailable = Boolean.parseBoolean(params[3]);
            int mathChanNumber = TChan.Math1;
            switch (mathSlideIndex) {
                case MainViewGroup.RIGHTSLIP_MATH1:
                    mathChanNumber = TChan.Math1;
                    break;
                case MainViewGroup.RIGHTSLIP_MATH2:
                    mathChanNumber = TChan.Math2;
                    break;
                case MainViewGroup.RIGHTSLIP_MATH3:
                    mathChanNumber = TChan.Math3;
                    break;
                case MainViewGroup.RIGHTSLIP_MATH4:
                    mathChanNumber = TChan.Math4;
                    break;
                case MainViewGroup.RIGHTSLIP_MATH5:
                    mathChanNumber = TChan.Math5;
                    break;
                case MainViewGroup.RIGHTSLIP_MATH6:
                    mathChanNumber = TChan.Math6;
                    break;
                case MainViewGroup.RIGHTSLIP_MATH7:
                    mathChanNumber = TChan.Math7;
                    break;
                case MainViewGroup.RIGHTSLIP_MATH8:
                    mathChanNumber = TChan.Math8;
                    break;
            }
            if (mathChanNumber == mathChannelNumber) {
                channelRef.setEnabled(refAvailable);
                channelSerials.setEnabled(serialAvailable);
            } else {
                channelRef.setEnabled(true);
                channelSerials.setEnabled(true);
            }
            boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);
            boolean slowTimeBase = Tools.isSlowTimeBase();
            if (zoom && slowTimeBase) {
                channelSerials.setEnabled(false);
            }
        }
    };


    private Consumer<String> consumerChannelVscale = new Consumer<String>() {

        @Override
        public void accept(String adjustStr) throws Throwable {
            String[] param = adjustStr.split(CommandMsgToUI.PARAM_SPLIT);
            boolean isClickTop = Boolean.parseBoolean(param[0]);
            int chan = Integer.parseInt(param[1]);
            if (chan != mathChannelNumber) return;
            if (isClickTop) {
                ivBackground.setImageResource(R.drawable.svg_right_chx_button_u_88x174);
                msgMath.setUpClick(true);
                postChange();
            } else {
                ivBackground.setImageResource(R.drawable.svg_right_chx_button_d_88x174);
                msgMath.setUpClick(false);
                postChange();
            }
        }
    };

    private Consumer<TopMsgDisplay> consumerTopSlipTitle = new Consumer<TopMsgDisplay>() {
        @Override
        public void accept(TopMsgDisplay topMsgDisplay) throws Exception {
            if (topMsgDisplay != null && topMsgDisplay.getDisplayDetail() instanceof TopMsgDisplayFftInfo) {
                TopMsgDisplayFftInfo displayFftInfo = (TopMsgDisplayFftInfo) topMsgDisplay.getDisplayDetail();
//                boolean switchState = displayFftInfo.isShowFftInfo().isValue();
//                int mathIndex = displayFftInfo.getFftInfoIndex().getValue();
//                if (switchState) {
//                    isShowFftInfo = mathChannelNumber == TChan.toMathChan(mathIndex + 1);
//                } else {
                int index = displayFftInfo.getFftInfoIndex().getValue();
                if (index != 0) {
                    isShowFftInfo = mathChannelNumber == TChan.toMathChan(index);
                } else {
                    isShowFftInfo = false;
                }
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChannelNumber);
                ShowFFTMeasureInfo(mathType == CacheUtil.MATHTYPE_FFT);
            }
        }
    };

    private Consumer<Integer> consumerDeleteChannel = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Throwable {
            if (integer == mathChannelNumber) {
                btnDeleteChannel.performClick();
            }
        }
    };

    private void ShowFFTMeasureInfo(boolean isSelectFftMode) {
        boolean isFftMode = isSelectFftMode && mSwitchBox.isState() && isShowFftInfo;
        String DC = App.get().getResources().getString(R.string.rightMathMenuFftMeasure_DC);
        String Amp = App.get().getResources().getString(R.string.rightMathMenuFftMeasure_Amp);
        String Freq = App.get().getResources().getString(R.string.rightMathMenuFftMeasure_Freq);
        MeasureManage.getInstance().getFftMeasure(TChan.toMathNumber(mathChannelNumber)).setLabel(DC, Amp, Freq);
        MeasureManage.getInstance().getFftMeasure(TChan.toMathNumber(mathChannelNumber)).setVisible(isFftMode);
    }

    @SuppressLint("NonConstantResourceId")
    private void selectItemIndex(int viewId, RightBeanSelect item) {
        Tools.PrintControlsLocation("RightLayoutMath", rootView);
        MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChannelNumber));
        switch (viewId) {
            case R.id.doubleWaveSource1:
                if(mathChannel == null) return;
                setCacheMathType(CacheUtil.MATHTYPE_DW, false, mathChannelNumber);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_DW_SOURCE1 + mathChannelNumber, String.valueOf(item.getIndex()));
                mathChannel.getMathDualWave().setSource1(dWSource1.getSelectIndex());
                mathChannel.getMathDualWave().setOperator(dWSymbol.getSelectIndex());
                mathChannel.getMathDualWave().setSource2(dWSource2.getSelectIndex());
                msgMath.setDwSource1(item);
                break;
            case R.id.doubleWaveSource2:
                if(mathChannel == null) return;
                setCacheMathType(CacheUtil.MATHTYPE_DW, false, mathChannelNumber);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_DW_SOURCE2 + mathChannelNumber, String.valueOf(item.getIndex()));
                mathChannel.getMathDualWave().setSource1(dWSource1.getSelectIndex());
                mathChannel.getMathDualWave().setOperator(dWSymbol.getSelectIndex());
                mathChannel.getMathDualWave().setSource2(dWSource2.getSelectIndex());
                msgMath.setDwSource2(item);
                break;
            case R.id.doubleWaveSymbol:
                if(mathChannel == null) return;
                setCacheMathType(CacheUtil.MATHTYPE_DW, false, mathChannelNumber);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_DW_SYMBOL + mathChannelNumber, String.valueOf(item.getIndex()));
                mathChannel.getMathDualWave().setSource1(dWSource1.getSelectIndex());
                mathChannel.getMathDualWave().setOperator(dWSymbol.getSelectIndex());
                mathChannel.getMathDualWave().setSource2(dWSource2.getSelectIndex());
                msgMath.setDwSymbol(item);
                break;
            case R.id.fftWindow:
                if(mathChannel == null) return;
                setCacheMathType(CacheUtil.MATHTYPE_FFT, false, mathChannelNumber);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_FFT_WINDOW + mathChannelNumber, String.valueOf(item.getIndex()));
                mathChannel.getMathFFTWave().setFFTType(fftType.getSelectIndex());
                mathChannel.getMathFFTWave().setFFTWindow(fftWindow.getSelectIndex());
                mathChannel.getMathFFTWave().setSource(fftSource.getSelectIndex());
                msgMath.setFftWindow(item);
                break;
            case R.id.fftSource:
                if(mathChannel == null) return;
                setCacheMathType(CacheUtil.MATHTYPE_FFT, false, mathChannelNumber);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_FFT_SOURCE + mathChannelNumber, String.valueOf(item.getIndex()));
                mathChannel.getMathFFTWave().setFFTType(fftType.getSelectIndex());
                mathChannel.getMathFFTWave().setFFTWindow(fftWindow.getSelectIndex());
                mathChannel.getMathFFTWave().setSource(fftSource.getSelectIndex());
                msgMath.setFftSource(item);
                break;
            case R.id.fftPersist:
                if(mathChannel == null) return;
                setCacheMathType(CacheUtil.MATHTYPE_FFT, false, mathChannelNumber);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_FFT_PERSIST + mathChannelNumber, String.valueOf(item.getIndex()));
                mathChannel.getMathFFTWave().setFFTType(fftType.getSelectIndex());
                mathChannel.getMathFFTWave().setFFTWindow(fftWindow.getSelectIndex());
                mathChannel.getMathFFTWave().setSource(fftSource.getSelectIndex());
                setFftPersistValueEnable();
                msgMath.setFftWindow(item);
                msgMath.setFftPersist(fftPersist.getSelectItem());
                break;
            case R.id.axbSource:
                if(mathChannel == null) return;
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AXB_SOURCE + mathChannelNumber, String.valueOf(item.getIndex()));
                String axbUnit = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AXB_UNIT + mathChannelNumber);
                this.axbUnit.setText(axbUnit);
                msgMath.setAxbUnit(axbUnit);
                setCacheMathType(CacheUtil.MATHTYPE_AXB, false, mathChannelNumber);
                setScopeAxb(mathChannelNumber);
                msgMath.setAxbSource(item);
                break;
            case R.id.fftType:
                if(mathChannel == null) return;
                setCacheMathType(CacheUtil.MATHTYPE_FFT, false, mathChannelNumber);
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + mathChannelNumber, String.valueOf(item.getIndex()));
                mathChannel.getMathFFTWave().setFFTType(fftType.getSelectIndex());
                mathChannel.getMathFFTWave().setFFTWindow(fftWindow.getSelectIndex());
                mathChannel.getMathFFTWave().setSource(fftSource.getSelectIndex());
                setMathPos(CacheUtil.MATHTYPE_FFT);
                mathChannel.setProbeType(mathChannel.generateProbeType());
                msgMath.setFftType(item.getText());
                break;
            case R.id.mathVerticalBaseDetail:
                if(mathChannel == null) return;
                mathChannel.setVerticalMode(item.getIndex());
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_VERTICALBASE + mathChannelNumber, String.valueOf(item.getIndex()));
                break;
            case R.id.math_channel_type:
//                if (mathChannelType.getSelectItem().getIndex() == item.getIndex()) return;
                PlaySound.getInstance().playSlide();
                setCacheMathType(item.getIndex(), false, mathChannelNumber);
                setShowMathTypeLayout(item.getIndex());
                setMathPos(item.getIndex());
                msgMath.setMathType(item.getIndex());
                msgMath.setMathTypeSelect(item);
                Command.get().getMath().Mode(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false);
                break;
        }
        sendMsg();
    }

    private MSwitchBox.OnToggleStateChangedListener onToggleStateChangedListener = new MSwitchBox.OnToggleStateChangedListener() {
        @Override
        public void onToggleStateChanged(MSwitchBox view, boolean state) {
            if (view.getId() == R.id.channel_switch) {
                if (!mSwitchBox.isState()) {
                    ((MainActivity) context).getMainViewGroup().hideAllDialogSlip();
                }
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChannelNumber);
                ShowFFTMeasureInfo(mathType == CacheUtil.MATHTYPE_FFT);
                Command.get().getChannel().Display(TChan.toFpgaChNo(mathChannelNumber), state && isAddByUser(mathChannelNumber), false);
                CacheUtil.get().putMap(CacheUtil.MAIN_RIGHT_MATH + mathChannelNumber, String.valueOf(mSwitchBox.isState()));
                msgMath.setMathCheck(mSwitchBox.isState());
                sendMsg();
                if (!state) {
                    //关闭垂直调节按钮
                    RxBus.getInstance().post(RxEnum.MQ_MSG_FORCE_HIDE_VERTICAL_SCALE, true);
                }
//                这里暂时不需要移动MasterView位置
//                updateMaxChannelNumber();
//                if (needUpdateMasterLocation) {
//                    needUpdateMasterLocation = false;
//                    RxBus.getInstance().post(RxEnum.MQ_MSG_UPDATE_MASTER_LOCATION, mathChannelNumber);
//                }
            }
        }
    };

    private OnClickListener onClickListener = new OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            if (layoutTextKeyBoard == null) layoutTextKeyBoard = ((MainActivity) context).findViewById(R.id.dialogTextKeyBoard);
            if (layoutFloatKeyBoard == null) layoutFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);
            if (layoutFullFloatKeyBoard == null) layoutFullFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFullFloatKeyBoard);
            if (layoutFormulaKeyBoard == null) layoutFormulaKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFormulaKeyBoard);
            if (layoutNumberPicker == null) layoutNumberPicker = ((MainActivity) context).findViewById(R.id.dialogNumberPicker);
            if (layoutMathFFTPersist == null) layoutMathFFTPersist = ((MainActivity) context).findViewById(R.id.dialogMathFFTPersist);
            PlaySound.getInstance().playButton();
            switch (v.getId()) {
                case R.id.axbUnitDetail:
                    layoutTextKeyBoard.setDataEnglish(TopDialogTextKeyBoard.HANDLE_TYPE_MATH,
                            axbUnit.getText().toString(), 3, new TopDialogTextKeyBoard.OnDialogDismissListener() {
                        @Override
                        public void onDismiss(String result) {
                            axbUnit.setText(result);
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AXB_UNIT + mathChannelNumber, result);
                            setCacheMathType(CacheUtil.MATHTYPE_AXB, false, mathChannelNumber);
                            msgMath.setAxbUnit(result);
                            sendMsg();
                            Command.get().getMath_axb().Unit(TChan.toFpgaChNo(mathChannelNumber), result, false);
                        }
                    });
                    break;
                case R.id.axbADetail:
                    layoutFloatKeyBoard.setFloatData(axbA.getText().toString(), axbA, new TopDialogFloatKeyBoard.OnDismissListener() {
                        @Override
                        public void onDismiss(View fromView, String show) {
                            axbA.setText(show);
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AXB_A + mathChannelNumber, show);
                            setScopeAxb(mathChannelNumber);
                            msgMath.setAxbA(axbA.getText().toString());
                            sendMsg();
                            Command.get().getMath_axb().A(TChan.toFpgaChNo(mathChannelNumber), show, true);
                        }
                    });
                    break;
                case R.id.axbBDetail:
                    layoutFloatKeyBoard.setFloatData(axbB.getText().toString(), axbB, new TopDialogFloatKeyBoard.OnDismissListener() {
                        @Override
                        public void onDismiss(View fromView, String show) {
                            axbB.setText(show);
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AXB_B + mathChannelNumber, show);
                            setScopeAxb(mathChannelNumber);
                            msgMath.setAxbB(axbB.getText().toString());
                            sendMsg();
                            Command.get().getMath_axb().B(TChan.toFpgaChNo(mathChannelNumber), show, true);
                        }
                    });
                    break;
                case R.id.advanceMathUnitDetail:
                    layoutTextKeyBoard.setDataEnglish(TopDialogTextKeyBoard.HANDLE_TYPE_MATH,
                            aMUnit.getText().toString(), 3, new TopDialogTextKeyBoard.OnDialogDismissListener() {
                        @Override
                        public void onDismiss(String result) {
                            aMUnit.setText(result);
                            Command.get().getMath_advanced().Unit(TChan.toFpgaChNo(mathChannelNumber), aMUnit.getText().toString(), false);
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_UNIT + mathChannelNumber, result);
                            setCacheMathType(CacheUtil.MATHTYPE_AM, false, mathChannelNumber);
                            msgMath.setAmUnit(result);
                            sendMsg();
                        }
                    });
                    break;
                case R.id.advanceMathFormula:
                    if (timeBase.isEmpty()) {
                        timeBase = MainHolderBottom.getCenterTimeBase();
                    }
                    layoutFormulaKeyBoard.setData(aMFormula.getText().toString(), timeBase, new TopDialogFormulaKeyBoard.OnDismissListener() {
                        @Override
                        public void onDismiss(String show) {
                            aMFormula.setText(show);
                            Command.get().getMath_advanced().Expression(TChan.toFpgaChNo(mathChannelNumber), aMFormula.getText().toString(), false);
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA + mathChannelNumber, show);
                            setScopeAM(mathChannelNumber);
                            if (show.contains(getResources().getString(R.string.key_formula_diff)) && show.contains("ch")) {
                                // 微分时需要设置采样为平均
                                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE + mathChannelNumber, String.valueOf(true));
                                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET + mathChannelNumber, String.valueOf(true));
                            } else {
                                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE + mathChannelNumber, String.valueOf(false));
                            }
                            msgMath.setAmFormula(show);
                            sendMsg();
                        }
                    });
                    break;
                case R.id.advanceMathVar1Number:
                case R.id.advanceMathVar1Power:
                    layoutNumberPicker.setData(aMVar1Number.getText().toString(), aMVar1Power.getText().toString(), new TopDialogNumberPicker.OnDismissListener() {
                        @Override
                        public void onDismiss(String number, String power) {
                            aMVar1Number.setText(number);
                            aMVar1Power.setText(power);
                            double mVar1 = Double.parseDouble(aMVar1Number.getText().toString()) * Math.pow(10, Double.parseDouble(aMVar1Power.getText().toString()));
                            Command.get().getMath_advanced().Var1(TChan.toFpgaChNo(mathChannelNumber), mVar1, false);
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_VAR1_NUMBER + mathChannelNumber, number);
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_VAR1_POWER + mathChannelNumber, power);
                            setScopeAM(mathChannelNumber);
                            msgMath.setAmVar1Number(number);
                            msgMath.setAmVar1Power(power);
                            sendMsg();
                        }
                    });
                    break;
                case R.id.advanceMathVar2Number:
                case R.id.advanceMathVar2Power:
                    layoutNumberPicker.setData(aMVar2Number.getText().toString(), aMVar2Power.getText().toString(), new TopDialogNumberPicker.OnDismissListener() {
                        @Override
                        public void onDismiss(String number, String power) {
                            aMVar2Number.setText(number);
                            aMVar2Power.setText(power);
                            double mVar2 = Double.parseDouble(aMVar2Number.getText().toString()) * Math.pow(10, Double.parseDouble(aMVar2Power.getText().toString()));
                            Command.get().getMath_advanced().Var2(TChan.toFpgaChNo(mathChannelNumber), mVar2, false);
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_VAR2_NUMBER + mathChannelNumber, number);
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_VAR2_POWER + mathChannelNumber, power);
                            setScopeAM(mathChannelNumber);
                            msgMath.setAmVar2Number(number);
                            msgMath.setAmVar2Power(power);
                            sendMsg();
                        }
                    });
                    break;
                case R.id.fftPersistValue:
                    Tools.getViewRect(fftPersist);
                    layoutMathFFTPersist.setData(mathChannelNumber, fftPersistValue.getText().toString(), CacheUtil.RIGHT_SLIP_MATH_FFT_PERSIST_VALUE + mathChannelNumber, (result) -> {
                        fftPersistValue.setText(result);
                    });
                    break;
                case R.id.btnTop:
                    ivBackground.setImageResource(R.drawable.svg_right_chx_button_u_88x174);
                    msgMath.setUpClick(true);
                    postChange();
                    break;
                case R.id.btnBottom:
                    ivBackground.setImageResource(R.drawable.svg_right_chx_button_d_88x174);
                    msgMath.setUpClick(false);
                    postChange();
                    break;
                case R.id.btn_delete_channel:
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + mathChannelNumber, String.valueOf(false));
                    mSwitchBox.setState(false);
                    onToggleStateChangedListener.onToggleStateChanged(mSwitchBox, mSwitchBox.isState());
//                    resetDefaultColor(mathChannelNumber);
                    break;
                case R.id.channelMath:
                    setDisplayState();
                    break;
                case R.id.channelRef:
                    setDisplayState();
                    hideSlip();
                    RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_OTHER_CHANNEL, 1);
                    break;
                case R.id.channelSerials:
                    setDisplayState();
                    hideSlip();
                    RxBus.getInstance().post(RxEnum.MQ_MSG_SHOW_OTHER_CHANNEL, 2);
                    break;
                case R.id.btn_add_channel:
                    needUpdateMasterLocation = true;
                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + mathChannelNumber, String.valueOf(true));
                    mSwitchBox.setState(true);
                    onToggleStateChangedListener.onToggleStateChanged(mSwitchBox, mSwitchBox.isState());
                    if (mathChannelType.getSelectIndex() == 0) { //双波形
                        onRightSlipViewSelectItemClickListener.onItemClick(dWSource1.getId(), dWSource1.getSelectItem());
                    }
                    hideSlip();
                    break;
            }
        }
    };

    private void hideSlip() {
        int slipIndex = MainViewGroup.RIGHTSLIP_MATH1;
        switch (mathChannelNumber) {
            case TChan.Math1: slipIndex = MainViewGroup.RIGHTSLIP_MATH1;break;
            case TChan.Math2: slipIndex = MainViewGroup.RIGHTSLIP_MATH2;break;
            case TChan.Math3: slipIndex = MainViewGroup.RIGHTSLIP_MATH3;break;
            case TChan.Math4: slipIndex = MainViewGroup.RIGHTSLIP_MATH4;break;
            case TChan.Math5: slipIndex = MainViewGroup.RIGHTSLIP_MATH5;break;
            case TChan.Math6: slipIndex = MainViewGroup.RIGHTSLIP_MATH6;break;
            case TChan.Math7: slipIndex = MainViewGroup.RIGHTSLIP_MATH7;break;
            case TChan.Math8: slipIndex = MainViewGroup.RIGHTSLIP_MATH8;break;
        }
        RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(slipIndex, false));
    }

    private void checkMathAvailable() {
        int lastMathChan = getMaxMathChannelNumber();
        Logger.d(TAG, "lastMathChan = " + lastMathChan);
        if (TChan.isMath(lastMathChan + 1)) {
            MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(lastMathChan + 1));
            if (mathChannel == null) {
                channelMath.setEnabled(false);
            }
        } else {
            channelMath.setEnabled(false);
        }
    }

    public int getMaxMathChannelNumber() {
        final int[] maxMathChan = {TChan.Math1 - TChan.Ch1};
        TChan.foreachMath(mathChan -> {
            boolean mathCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChan);
//            if (mathCheck) {
            if (isAddByUser(mathChan)) {
                maxMathChan[0] = Math.max(maxMathChan[0], mathChan);
            }
        });
        return maxMathChan[0];
    }

    private void setDisplayState() {
        channelMath.setChecked(true);
        channelRef.setChecked(false);
        channelSerials.setChecked(false);
    }

    private RightViewSelect.OnItemClickListener onRightSlipViewSelectItemClickListener = new RightViewSelect.OnItemClickListener() {
        @Override
        public void onClickSound(boolean isCheckedSuccess) {
            PlaySound.getInstance().playButton();
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onItemClick(int viewId, RightBeanSelect item) {
            switch (viewId) {
                case R.id.doubleWaveSource1:
                    Command.get().getMath_base().S1(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false);
                    break;
                case R.id.doubleWaveSource2:
                    Command.get().getMath_base().S2(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false);
                    break;
                case R.id.doubleWaveSymbol:
                    Command.get().getMath_base().Operator(TChan.toFpgaChNo(mathChannelNumber), dWSymbol.getSelectItem().getIndex(), false);
                    break;
                case R.id.fftWindow:
                    Command.get().getMath_fft().Window(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false);
                    break;
                case R.id.fftSource:
                    Command.get().getMath_fft().Source(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false);
                    break;
                case R.id.axbSource:
                    Command.get().getMath_axb().Source(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false);
                    break;
                case R.id.fftType:
                    Command.get().getMath_fft().Type(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false);
                    break;
                case R.id.mathVerticalBaseDetail:
                    Command.get().getMath().VRef(TChan.toFpgaChNo(mathChannelNumber), item.getIndex(), false);
                    break;
            }
            selectItemIndex(viewId, item);
        }

        @Override
        public void onItemClickAfterRefreshUI(int viewId, boolean isCurClickForce) {
            if (!isCurClickForce) {
                MathChannel mathChannel = ChannelFactory.getMathChannel(TChan.toFpgaChNo(mathChannelNumber));
                if(mathChannel == null) return;
                if (viewId == dWSource1.getId() || viewId == dWSource2.getId()
                        || viewId == dWSymbol.getId()) {
                    setCacheMathType(CacheUtil.MATHTYPE_DW, false, mathChannelNumber);
                    mathChannel.getMathDualWave().setSource1(dWSource1.getSelectIndex());
                    mathChannel.getMathDualWave().setOperator(dWSymbol.getSelectIndex());
                    mathChannel.getMathDualWave().setSource2(dWSource2.getSelectIndex());
                } else if (viewId == fftType.getId() || viewId == fftWindow.getId() || viewId == fftSource.getId()) {

                    setCacheMathType(CacheUtil.MATHTYPE_FFT, false, mathChannelNumber);
                    mathChannel.getMathFFTWave().setFFTType(fftType.getSelectIndex());
                    mathChannel.getMathFFTWave().setFFTWindow(fftWindow.getSelectIndex());
                    mathChannel.getMathFFTWave().setSource(fftSource.getSelectIndex());

                } else if (viewId == axbSource.getId()) {
                    setCacheMathType(CacheUtil.MATHTYPE_AXB, false, mathChannelNumber);
                }
            }
            sendMsg();
        }

        @Override
        public void onItemClickBeforRefreshUI(int viewId) {

        }
    };

    private void postChange() {
        if (myHandler.hasMessages(HANDLE_MSG)) {
            myHandler.removeMessages(HANDLE_MSG);
        }
        myHandler.sendEmptyMessageDelayed(HANDLE_MSG, 200);
        RxBus.getInstance().post(RxEnum.MQ_MSG_CHANNEL_MATHX, msgMath);
    }


    private static final int HANDLE_MSG = 1;
    public static class MyHandler extends Handler {
        private final WeakReference<RightLayoutMath> weakReference;

        public MyHandler(RightLayoutMath layoutMath) {
            weakReference = new WeakReference<RightLayoutMath>(layoutMath);
        }

        @Override
        public void handleMessage(Message msg) {
            if (weakReference.get() != null) {
                if (msg.what == HANDLE_MSG) {
                    RightLayoutMath layoutMath = (RightLayoutMath) weakReference.get();
                    layoutMath.ivBackground.setImageResource(R.drawable.svg_right_chx_button_88x174);
                }
            }
        }
    }

    private void updateMaxChannelNumber() {
        AtomicInteger maxChan = new AtomicInteger(TChan.Math1 - 1);
        TChan.foreachMath(mathChan -> {
            boolean mathCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChan);
//            if (mathCheck) {
            if (isAddByUser(mathChan)) {
                maxChan.set(Math.max(maxChan.get(), mathChan));
            }
        });
        CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MAX_CHANNEL_NUMBER_MATH, maxChan.toString());
    }

    private String handleTimeBase(String timeBase) {
        timeBase = timeBase.contains("\n") ? timeBase.split("\n")[1] : timeBase;
        if (timeBase.isEmpty()) {
            timeBase = "1";
        } else {
            timeBase = (TBookUtil.getSFromTime(timeBase) + "").replaceAll(" ", "");
        }
//        Logger.d("limh", "timeBase= " + timeBase);
        return timeBase;
    }

    private boolean isAddByUser(int mathChan) {
        return CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_ADD_BY_USER_MATH + mathChan);
    }


    @SuppressLint("NonConstantResourceId")
    private final TopViewEdit.OnClickEditListener onClickEditListener = (v, text) -> {
        PlaySound.getInstance().playButton();
        switch (v.getId()) {
            case R.id.chLabel:
                if (dialogChannelLabel == null) {
                    dialogChannelLabel = (DialogChannelLabel) ((MainActivity) context).findViewById(R.id.dialogChannelLabel);
                }
                dialogChannelLabel.setData(mathChannelNumber, chLabel.getText()
                        , CacheUtil.RIGHT_SLIP_CH_LABEL_USERDEFINE + mathChannelNumber
                        , DialogChannelLabel.FROM_MATHREF
                        , result -> {
                            PlaySound.getInstance().playButton();
                            chLabel.setText(result);
                            msgMath.setLabel(result);
                            //Command.get().getChannel().Label(refChannelNumber - 1, result, false);
                            CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_LABEL + mathChannelNumber, result);
                            WaveManage.get().setChannelLabel(mathChannelNumber, result);
                            setChannelLabel(TChan.toFpgaChNo(mathChannelNumber), result);
                        }
                );
                break;
            case R.id.select_color:
                if (dialogSelectColor == null) {
                    dialogSelectColor = (DialogSelectColor) ((MainActivity)context).findViewById(R.id.dialogSelectColor);
                }
                dialogSelectColor.setData(DialogSelectColor.FROM_MATHREF, mathChannelNumber, (chIndex, colorStr) -> {
                    if (mathChannelNumber == chIndex) {
                        Logger.d(TAG, "选中的颜色值为：" + colorStr + " channelNum= " + chIndex);
                        selectColor.setEditColor(colorStr);
                    }
                });
                break;
            default:
                break;
        }
    };

    public void setChannelLabel(int chNo, String label) {
        MathChannel mathChannel = ChannelFactory.getMathChannel(chNo);
        if (mathChannel != null) {
            mathChannel.setLabel(label);
        }
    }

    private Consumer<String> consumerSelectColor = new Consumer<String>() {
        @Override
        public void accept(String colorInfo) throws Throwable {
            if (colorInfo.isEmpty()) return;
            Logger.i(TAG, "selectColorInfo= " + colorInfo);
            String[] info = colorInfo.split(";");
            int chIndex = Integer.parseInt(info[0]);
            String colorStr = info[1];
            if (chIndex == mathChannelNumber) {
                setOnlyControlColorByChIdx(chIndex);
                MeasureManage.getInstance().getFftMeasure(TChan.toMathNumber(mathChannelNumber)).setColor(SvgNodeInfo.getAllBaseColorInt(mathChannelNumber));
            }
        }
    };

    private void resetDefaultColor(int chIndex) {
        String colorDefault = SvgNodeInfo.getDefaultColor(chIndex);
        SvgNodeInfo.setChannelColor(chIndex, colorDefault);//改变颜色值
        CacheUtil.get().putMap(CacheUtil.MAIN_CHANNEL_COLOR + chIndex, colorDefault);
        selectColor.setEditColor(colorDefault);
        RxBus.getInstance().post(RxEnum.MQ_MSG_CHANNEL_SELECT_COLOR, chIndex + ";" + colorDefault);//发消息通知颜色值改变了
    }

    private Consumer<String> consumerMouseClick = new Consumer<String>() {
        @Override
        public void accept(String clickInfo) throws Throwable {
            String[] info = clickInfo.split(";");
            int chIdx = Integer.parseInt(info[0]);
            int clickPos = Integer.parseInt(info[1]);//0垂直档位  1垂直位置  2水平挡位  3水平位置
            Logger.d(TAG, "ClickInfo chidx= " + chIdx + " ,clickPos= " + clickPos);
            if (layoutFloatKeyBoard == null) {
                layoutFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFloatKeyBoard);
            }
            if (layoutFullFloatKeyBoard == null) {
                layoutFullFloatKeyBoard = ((MainActivity) context).findViewById(R.id.dialogFullFloatKeyBoard);
            }
            if (TChan.toUiChNo(chIdx) != mathChannelNumber) return;
            String unit = ChannelFactory.getProbeType(chIdx);
            MathChannel mathChannel = ChannelFactory.getMathChannel(chIdx);
            if (mathChannel == null) return;
            if (clickPos == 0) { //垂直档位
                double vScaleVal = mathChannel.getVScaleVal();
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx));
                if (mathType == CacheUtil.MATHTYPE_AXB || mathType == CacheUtil.MATHTYPE_AM) {
                    setChFullVScale(TBookUtil.getMFromDouble(vScaleVal) + unit, chIdx);
                } else {
                    setChVScale(TBookUtil.getMFromDouble(vScaleVal) + unit, chIdx);
                }
            }
            if (clickPos == 1) { //垂直位置
                double leftPos = Tools.getChannelPositionUI(TChan.toUiChNo(chIdx));
                int zoomHeight = (int) (ScopeBase.getNewHeight() * 0.75);
                double pos = (Tools.isZoom() ? zoomHeight : 1.0 * ScopeBase.getNewHeight()) / 2 - leftPos;
                String number = TBookUtil.getFourFromD_Trim0(pos * mathChannel.getVerticalPerPix());
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx));
                if (mathType == CacheUtil.MATHTYPE_AXB || mathType == CacheUtil.MATHTYPE_AM) {
                    setChFullVPosition(number + unit, chIdx);
                } else {
                    setChVPosition(number + unit, chIdx);
                }
            }
            if (clickPos == 3) { //水平位置
                int followCh = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_USERSET_REF_TIMEBASE);
                String timePosStr = "";
                if (mathChannel.getMathType() != MathWave.MATH_FFTWAVE) {
                    long timePos;
                    timePos = HorizontalAxis.getInstance().getTimePosOfView();
                    timePosStr = TBookUtil.getSFrom100Fs(timePos);
                    setChHPosition(timePosStr, chIdx);
                }
            }
        }
    };

    //水平位置
    public void setChHPosition(String nowTxt, int chIdx) {
        layoutFloatKeyBoard.bringToFront();

        String unit = ChannelFactory.getProbeType(chIdx);
        String txt = nowTxt.toString().replaceAll("(?:s|\\s)", "");
        layoutFloatKeyBoard.setFloatData(txt, forDoubleClick, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();
                double val = TBookUtil.getBigDoubleFromM(show);
                Command.get().getTimebase().Position(val, true);
            }
        });
    }

    //垂直位置
    private void setChVPosition(String nowTxt, int chIdx) {
        layoutFloatKeyBoard.bringToFront();

        String unit = ChannelFactory.getProbeType(chIdx);
        String txt = nowTxt.replace(unit, "").replace(" ", "");
        layoutFloatKeyBoard.setFloatData(txt, forDoubleClick, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();
                double val = TBookUtil.getDoubleFromM(show);
//                Logger.d("limh show= " + show + " unit= " + unit);
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx));
                switch (mathType) {
                    case CacheUtil.MATHTYPE_DW:
                        Command.get().getMath_base().Offset(chIdx, val, true);
                        break;
                    case CacheUtil.MATHTYPE_FFT:
                        Command.get().getMath_fft().Offset(chIdx, val, true);
                        break;
                    case CacheUtil.MATHTYPE_AXB:
//                        Logger.d("limh val= " + val);
                        Command.get().getMath_axb().Offset(chIdx, val, true);
                        break;
                    case CacheUtil.MATHTYPE_AM:
                        Command.get().getMath_advanced().Offset(chIdx, val, true);
                        break;
                }
            }
        });
    }

    //垂直位置
    private void setChFullVPosition(String nowTxt, int chIdx) {
        layoutFullFloatKeyBoard.bringToFront();

        String unit = ChannelFactory.getProbeType(chIdx);
        String txt = nowTxt.replace(unit, "").replace(" ", "");
        layoutFullFloatKeyBoard.setFloatData(txt, forDoubleClick, new TopDialogFullFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();
                double val = TBookUtil.getDoubleFromM(show);
//                Logger.d("limh show= " + show + " unit= " + unit);
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx));
                switch (mathType) {
                    case CacheUtil.MATHTYPE_DW:
                        Command.get().getMath_base().Offset(chIdx, val, true);
                        break;
                    case CacheUtil.MATHTYPE_FFT:
                        Command.get().getMath_fft().Offset(chIdx, val, true);
                        break;
                    case CacheUtil.MATHTYPE_AXB:
//                        Logger.d("limh val= " + val);
                        Command.get().getMath_axb().Offset(chIdx, val, true);
                        break;
                    case CacheUtil.MATHTYPE_AM:
                        Command.get().getMath_advanced().Offset(chIdx, val, true);
                        break;
                }
            }
        });
    }

    private void setChFullVScale(String nowTxt, int chIdx) {
        layoutFullFloatKeyBoard.bringToFront();

        String unit = ChannelFactory.getProbeType(chIdx);
        Logger.d(TAG, "setChFullVScale unit= " + unit + " ,nowTxt= " + nowTxt);
        String txt = nowTxt.replace(unit, "").replace(" ", "");
        layoutFullFloatKeyBoard.setFloatData_Extent(txt, forDoubleClick, new TopDialogFullFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();
                double d = TBookUtil.getDoubleFromM(show);
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx));
                switch (mathType) {
                    case CacheUtil.MATHTYPE_DW:
                        Command.get().getMath_base().Extent(chIdx, d, true);
                        break;
                    case CacheUtil.MATHTYPE_FFT:
                        Command.get().getMath_fft().Extent(chIdx, d, true);
                        break;
                    case CacheUtil.MATHTYPE_AXB:
                        Command.get().getMath_axb().Extent(chIdx, d, true);
                        break;
                    case CacheUtil.MATHTYPE_AM:
                        Command.get().getMath_advanced().Extent(chIdx, d, true);
                        break;
                }
            }
        });
    }

    private void setChVScale(String nowTxt, int chIdx) {
        layoutFloatKeyBoard.bringToFront();

        String unit = ChannelFactory.getProbeType(chIdx);
        Logger.d(TAG, "setChVScale unit= " + unit + " ,nowTxt= " + nowTxt);
        String txt = nowTxt.replace(unit, "").replace(" ", "");
        layoutFloatKeyBoard.setFloatData_Extent(txt, forDoubleClick, new TopDialogFloatKeyBoard.OnDismissListener() {
            @Override
            public void onDismiss(View fromView, String show) {
                PlaySound.getInstance().playButton();
                double d = TBookUtil.getDoubleFromM(show);
                int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + TChan.toUiChNo(chIdx));
                switch (mathType) {
                    case CacheUtil.MATHTYPE_DW:
                        Command.get().getMath_base().Extent(chIdx, d, true);
                        break;
                    case CacheUtil.MATHTYPE_FFT:
                        Command.get().getMath_fft().Extent(chIdx, d, true);
                        break;
                    case CacheUtil.MATHTYPE_AXB:
                        Command.get().getMath_axb().Extent(chIdx, d, true);
                        break;
                    case CacheUtil.MATHTYPE_AM:
                        Command.get().getMath_advanced().Extent(chIdx, d, true);
                        break;
                }
            }
        });
    }

}
