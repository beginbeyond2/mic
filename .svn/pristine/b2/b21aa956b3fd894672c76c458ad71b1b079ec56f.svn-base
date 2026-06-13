package com.micsig.tbook.tbookscope.main.mainright;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Html;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.MainMsgSlip;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.menu.SliderZone;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.util.StrUtil;
import com.micsig.tbook.ui.wavezone.TChan;


public class MainRightLayoutItemSerialsMaster extends LinearLayout implements SliderZone.ISliderZone {
    private Context context;
    private boolean checked = true;
    private String text = "S1";
    private int chIndex = ChannelFactory.S1;
    private RelativeLayout bgChannelMaster;
    private TextView tvChText;
    private TextView tvBusType;
    private LinearLayout rlTextLayout;
    private LinearLayout llTextLayout1, llTextLayout2;
    private View spaceLine;
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private TextView tv4;
    private Button btnClick;
    private OnAllClickListener onAllClickListener;
    private SlidCloseChannelListener mSlideCloseListener;

    private int serialsType = RightLayoutSerials.SERIALS_UART;
    private int text1Ch = 1;//1-4
    private int text2Ch = 1;
    private int text3Ch = 1;
    private boolean isSelect = false;

    public interface OnAllClickListener {
        void onClick(MainRightLayoutItemSerialsMaster v);
    }

    public interface SlidCloseChannelListener {
        void onSlidCloseChannel(MainRightLayoutItemSerialsMaster v);
    }

    public void setOnAllClickListener(OnAllClickListener onAllClickListener) {
        this.onAllClickListener = onAllClickListener;
    }

    public void setOnSlidCloseChannelListener(SlidCloseChannelListener mSlideCloseListener) {
        this.mSlideCloseListener = mSlideCloseListener;
    }

    public MainRightLayoutItemSerialsMaster(Context context) {
        this(context, null);
    }

    public MainRightLayoutItemSerialsMaster(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainRightLayoutItemSerialsMaster(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.context = context;
        inflate(context, R.layout.layout_mainright_serials_master_for_eight, this);
        text = "S1";
        chIndex = ChannelFactory.S1;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MainRightLayoutItemSerialsMaster);
        checked = ta.getBoolean(R.styleable.MainRightLayoutItemSerialsMaster_checked, true);
        text = ta.getString(R.styleable.MainRightLayoutItemSerialsMaster_text);
        ta.recycle();
        initView();
        setChecked(checked);
    }

    private void initView() {
        bgChannelMaster = findViewById(R.id.bgChannelMaster);
        tvChText = (TextView) findViewById(R.id.channelText);
        tvBusType = (TextView) findViewById(R.id.serialsType);
        rlTextLayout = (LinearLayout) findViewById(R.id.textLayout);
        spaceLine = findViewById(R.id.space_line);
        llTextLayout1 = findViewById(R.id.textLayout1);
        llTextLayout2 = findViewById(R.id.textLayout2);
        tv1 = (TextView) findViewById(R.id.text1);
        tv2 = (TextView) findViewById(R.id.text2);
        tv3 = (TextView) findViewById(R.id.text3);
        tv4 = (TextView) findViewById(R.id.text4);
        btnClick = (Button) findViewById(R.id.btnClick);

       // btnClick.setOnClickListener(onClickListener);
        btnClick.setOnTouchListener(onTouchListener);

        tvChText.setText(text);
        if (text.contains("S1")) {
            tvChText.setTextColor(getResources().getColor(R.color.color_S1));
            chIndex = ChannelFactory.S1;
            tvBusType.setTextColor(getResources().getColor(R.color.color_S1));
        } else if (text.equals("S2")) {
            tvChText.setTextColor(getResources().getColor(R.color.color_S2));
            chIndex = ChannelFactory.S2;
            tvBusType.setTextColor(getResources().getColor(R.color.color_S2));
        } else if (text.equals("S3")) {
            tvChText.setTextColor(getResources().getColor(R.color.color_S3));
            chIndex = ChannelFactory.S3;
            tvBusType.setTextColor(getResources().getColor(R.color.color_S3));
        } else if (text.equals("S4")) {
            tvChText.setTextColor(getResources().getColor(R.color.color_S4));
            chIndex = ChannelFactory.S4;
            tvBusType.setTextColor(getResources().getColor(R.color.color_S4));
        }
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        if (checked) {
            tvChText.setVisibility(GONE);
            tvBusType.setVisibility(VISIBLE);
            rlTextLayout.setVisibility(VISIBLE);
        } else {
            tvChText.setVisibility(VISIBLE);
            tvBusType.setVisibility(GONE);
            rlTextLayout.setVisibility(GONE);
        }
    }

    public void updateBackground(boolean isSelect) {
        this.isSelect = isSelect;
        if (isSelect) {
            bgChannelMaster.setBackground(context.getResources().getDrawable(R.drawable.layer_button_select));
        } else {
            bgChannelMaster.setBackground(context.getResources().getDrawable(R.drawable.layer_button_disable));
        }
    }

    public boolean isSelect() {
        return isSelect;
    }

    public boolean isChecked() {
        return checked;
    }

    public String getName() {
        return text;
    }

    public void setName(String text) {
        this.text = text;
        tvChText.setText(text);
    }

    public void setSerialsType(String serialsType) {
        switch (chIndex) {
            case ChannelFactory.S1:
                serialsType = "① " + serialsType;
                break;
            case ChannelFactory.S2:
                serialsType = "② " + serialsType;
                break;
            case ChannelFactory.S3:
                serialsType = "③ " + serialsType;
                break;
            case ChannelFactory.S4:
                serialsType = "④ " + serialsType;
                break;
        }
        serialsType = serialsType.replaceAll("[\\r\\n]+", "");
        this.tvBusType.setText(serialsType);
    }

    public void setSerialsType(int serialsType) {
        this.serialsType = serialsType;
    }

    public int getSerialsType() {
        return this.serialsType;
    }

    /**
     * 设置除了spi、i2c模式以外的模式下，两个通道的通道号
     */
    public void setCommonCh(int ch) {
        this.text1Ch = ch;
    }

    public int getCommonCh() {
        return this.text1Ch;
    }

    public int getChIndex() {
        return chIndex;
    }

    /**
     * 设置i2c模式下，两个通道的通道号
     */
    public void setI2cCh(int text1Ch, int text2Ch) {
        this.text1Ch = text1Ch;
        this.text2Ch = text2Ch;
    }

    public int getI2cNo1() {
        return this.text1Ch;
    }

    public int getI2cNo2() {
        return this.text2Ch;
    }

    /**
     * 设置spi模式下，两个通道的通道号
     */
    public void setSpiCh(int text1Ch, int text2Ch, int text3Ch) {
        this.text1Ch = text1Ch;
        this.text2Ch = text2Ch;
        this.text3Ch = text3Ch;
    }

    public int getSpiNo1() {
        return this.text1Ch;
    }

    public int getSpiNo2() {
        return this.text2Ch;
    }

    public int getSpiNo3() {
        return this.text3Ch;
    }

    /**
     * 设置阈值电平值的显示，只有当当前显示的channel与当前改变的channel相同时，才会修改
     *
     * @param curCh     本次修改的channel
     * @param levelType 本次修改channel的px值
     */
    public void setCommonValueLevel(int curCh, int levelType, int levelMode) {
        String val = Tools.getChannelLevel(curCh, levelType, levelMode);
        switch (serialsType) {
            case RightLayoutSerials.SERIALS_UART:
            case RightLayoutSerials.SERIALS_LIN:
            case RightLayoutSerials.SERIALS_CAN:
            case RightLayoutSerials.SERIALS_M1553B:
                if (text1Ch == curCh) {
                    setSerialsTextLine1(val);
                }
                break;
            case RightLayoutSerials.SERIALS_SPI:
                if (text1Ch == curCh) {
                    setSerialsTextLine1(val);
                } else if (text2Ch == curCh) {
                    setSerialsTextLine2(val);
                } else if (text3Ch == curCh && tv3.getVisibility() == VISIBLE) {
                    setSerialsTextLine3(val);
                }
                break;
            case RightLayoutSerials.SERIALS_I2C:
                if (text1Ch == curCh) {
                    setSerialsTextLine1(val);
                } else if (text2Ch == curCh) {
                    setSerialsTextLine2(val);
                }
                break;
            case RightLayoutSerials.SERIALS_M429:
                if (text1Ch == curCh) {
                    if (levelType == Tools.LevelType_High) {
                        setSerialsTextLine1(val);
                    } else {
                        setSerialsTextLine2(val);
                    }
                }
                break;
        }
    }

    public void setSerialsTextLine1(String text) {
        if (!StrUtil.isEmpty(text)) {
            text = text.replace("b/s", "");
//            tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 8);
//            Rect textRect = Tools.getTextRect(text, tv1.getPaint());
//            if (textRect.width() >= 45) {
//                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 8);
//            } else {
//                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 8);
//            }
            tv1.setVisibility(View.VISIBLE);
            tv1.setText(text);
        } else {
            tv1.setVisibility(View.GONE);
        }
    }

    public void setSerialsTextLine2(String text) {
        if (!StrUtil.isEmpty(text)) {
            text = text.replace("b/s", "");
            tv2.setVisibility(View.VISIBLE);
            tv2.setText(text);
        } else {
            tv2.setVisibility(View.GONE);
        }
    }

    public void setSerialsTextLine3(String text) {
        if (!StrUtil.isEmpty(text)) {
            text = text.replace("b/s", "");
            tv3.setVisibility(View.VISIBLE);
            tv3.setText(text);
        } else {
            tv3.setVisibility(View.GONE);
        }
    }

    public int getSerialsTextLine3Visible() {
        return tv3.getVisibility();
    }

    public String getSerialsTextLine1() {
        return tv1.getText().toString();
    }

    public String getSerialsTextLine2() {
        return tv2.getText().toString();
    }

    public String getSerialsTextLine3() {
        return tv3.getText().toString();
    }

    public void setSerialsText(String text1, String text2, String text3, String text4
            , int color1, int color2, int color3, int color4
            , boolean line1, boolean line2, boolean line3, boolean line4) {
        if (!StrUtil.isEmpty(text1)) {
            text1 = text1.replace("b/s", "");
//            tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 8);
//            Rect textRect = Tools.getTextRect(text1, tv1.getPaint());
//            if (textRect.width() >= 45) {
//                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 8);
//            } else {
//                tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, 8);
//            }
            if (line1) {
                tv1.setText(text1);
                tv1.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            } else {
                tv1.setText(text1);
                tv1.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
            }
            tv1.setTextColor(color1);
            tv1.setVisibility(View.VISIBLE);
        } else {
            tv1.setVisibility(View.GONE);
        }
        if (!StrUtil.isEmpty(text2)) {
            text2 = text2.replace("b/s", "");
            if (line2) {
                tv2.setText(text2);
                tv2.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            } else {
                tv2.setText(text2);
                tv2.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
            }
            tv2.setTextColor(color2);
            tv2.setVisibility(View.VISIBLE);
        } else {
            tv2.setVisibility(View.GONE);
        }
        if (!StrUtil.isEmpty(text3)) {
            text3 = text3.replace("b/s", "");
            if (line3) {
                tv3.setText(text3);
                tv3.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            } else {
                tv3.setText(text3);
                tv3.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
            }
            tv3.setTextColor(color3);
            tv3.setVisibility(View.VISIBLE);
        } else {
            tv3.setVisibility(View.GONE);
        }
        if (!StrUtil.isEmpty(text4)) {
            text4 = text4.replace("b/s", "");
            if (line4) {
                tv4.setText(Html.fromHtml("<u>" + text4 + "</u>"));
            } else {
                tv4.setText(text4);
            }
            tv4.setTextColor(color4);
            tv4.setVisibility(View.VISIBLE);
        } else {
            tv4.setVisibility(View.GONE);
        }

        if (tv1.getVisibility() != View.VISIBLE && tv2.getVisibility() != View.VISIBLE) {
            llTextLayout1.setVisibility(View.GONE);
        } else {
            llTextLayout1.setVisibility(View.VISIBLE);
        }

        if (tv3.getVisibility() != View.VISIBLE && tv4.getVisibility() != View.VISIBLE) {
            llTextLayout2.setVisibility(View.GONE);
            spaceLine.setVisibility(View.GONE);
        } else {
            llTextLayout2.setVisibility(View.VISIBLE);
            spaceLine.setVisibility(View.VISIBLE);
        }
    }

    private OnTouchListener onTouchListener = new OnTouchListener() {
        private View curView;
        private int oldX, oldY;
        private Handler handler = new Handler();
        private int clickCount = 0;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    this.oldX = (int) motionEvent.getX();
                    this.oldY = (int) motionEvent.getY();
                    this.curView = view;
//                    OnMouseDown(view);
                    break;
                case MotionEvent.ACTION_UP:
                    float offsetX = (float) (motionEvent.getX() - oldX);
                    float offsetY = (float) (motionEvent.getY() - oldY);
                    if (offsetY > 10 && (offsetX <= 5 || offsetY / offsetX >= 1.0f)) {
//                        CloseChannel(view);//关闭通道
                        RxBus.getInstance().post(RxEnum.MQ_MSG_DELETE_OTHER_CHANNEL, TChan.toUiChNo(getChIndex()));//删除通道
                        break;
                    }
                    clickCount++;
                    int timeout = 300;
                    Rect rect = Tools.getViewRect(view);
                    if (curView != null && curView == view && (Math.abs(offsetY) < 10 && Math.abs(offsetX) < 10) /*rect.contains((int)motionEvent.getRawX(), (int) motionEvent.getRawY())*/) {
                        OnMouseUp(view);
                        handler.postDelayed(() -> {
                            if (clickCount == 1) {
                            } else if (clickCount > 1) {
                                onDoubleMouseUp(view);
                            }
                            handler.removeCallbacksAndMessages(null);//清空handler延时
                            clickCount = 0;//计数清零
                        }, timeout);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
            return false;
        }

        private void CloseChannel(View view) {
            int serialNo = RightMsgSerials.SERIALS_S1;
            if (chIndex == ChannelFactory.S1) {
                serialNo = RightMsgSerials.SERIALS_S1;
            } else if (chIndex == ChannelFactory.S2) {
                serialNo = RightMsgSerials.SERIALS_S2;
            } else if (chIndex == ChannelFactory.S3) {
                serialNo = RightMsgSerials.SERIALS_S3;
            } else {
                serialNo = RightMsgSerials.SERIALS_S4;
            }
            if (mSlideCloseListener != null) {
                mSlideCloseListener.onSlidCloseChannel(MainRightLayoutItemSerialsMaster.this);
            }
            RxBus.getInstance().post(RxEnum.RIGHTLAYOUT_SERIALS_CLOSE, serialNo);

        }

        private void OnMouseUp(View view) {
            onClickListener.onClick(view);
        }

      private void onDoubleMouseUp(View view) {
        //双击打开
          openSlip(view);
      }

    };

    private void openSlip(View view) {
//        if (ViewUtils.getInstance().isFastDoubleClick(btnClick.getId())) {
//        DToast.get().show("双击");
        boolean zoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);
        boolean slowTimeBase = Tools.isSlowTimeBase();
        if (zoom && slowTimeBase) {
            return;
        }
        //关闭垂直调节按钮
        RxBus.getInstance().post(RxEnum.MQ_MSG_FORCE_HIDE_VERTICAL_SCALE, true);
        if (chIndex == ChannelFactory.S1) {
            RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_S1, true));
        } else if (chIndex == ChannelFactory.S2) {
            RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_S2, true));
        } else if (chIndex == ChannelFactory.S3) {
            RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_S3, true));
        } else if (chIndex == ChannelFactory.S4) {
            RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.RIGHTSLIP_S4, true));
        }
//        }
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == btnClick.getId()) {
                onAllClickListener.onClick(MainRightLayoutItemSerialsMaster.this);
            }
        }
    };

    //region    integerface
    private Rect availableSliderRect = null;

    @Override
    public Rect getAvailableSliderRect() {
        availableSliderRect = Tools.getViewRect(this);
        if (this.checked) {
            Bitmap bmp = Tools.readSvgBmp(R.drawable.svg_right_serialbus); //((BitmapDrawable) getResources().getDrawable(R.drawable.svg_right_serialbus)).getBitmap();
            int offsetY = (this.getHeight() - bmp.getHeight()) / 2;
            availableSliderRect.set(availableSliderRect.left, availableSliderRect.top + offsetY, availableSliderRect.right, availableSliderRect.bottom - offsetY);
        } else {
            Bitmap bmp = Tools.readSvgBmp(R.drawable.svg_right_ch1234_close); //((BitmapDrawable) getResources().getDrawable(R.drawable.svg_right_ch1234_close)).getBitmap();
            int offsetY = (this.getHeight() - bmp.getHeight()) / 2;
            availableSliderRect.set(availableSliderRect.left, availableSliderRect.top + offsetY, availableSliderRect.right, availableSliderRect.bottom - offsetY);
        }
        return availableSliderRect;
    }
    //endregion

}
