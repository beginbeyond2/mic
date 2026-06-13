package com.micsig.tbook.tbookscope.top.layout.factoryCalibration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.base.Logger;
import com.micsig.smart.PropertyManage;
import com.micsig.tbook.scope.Calibrate.CabteRegister;
import com.micsig.tbook.scope.Calibrate.CalibrateService;
import com.micsig.tbook.scope.Calibrate.FactorCalibrate;
import com.micsig.tbook.scope.Data.SaveRecoverySession;
import com.micsig.tbook.scope.Calibrate.MHO68v2.HW_MHO68V2;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Event.EventUIObserver;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.fpga.FPGACommand;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.vertical.VerticalAxis;
import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainMsgSlip;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.dialog.DialogOk;
import com.micsig.tbook.tbookscope.middleware.command.Command;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.SaveManage;
import com.micsig.tbook.tbookscope.tools.ScreenControls;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.util.DToast;
import com.micsig.tbook.ui.util.StrUtil;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by yangj on 2018/7/25.
 */

public class TopLayoutFactoryCalibration extends Fragment {
    private Context context;
    private Button btnTop;
    private Button btnReset;
    private Button btnDeliveryDate;
    private Button btnBandWidth;
    private TextView fpgatv,tvCabteTime;



    private TextView tvTopZeroTitle,tvTopZeroTitleDetail,tvNetip;
    private TextView tvCenterChGainTitle, tvCenterChGainTitleDetail;
    private TextView tvCenterChOffsetTitle, tvCenterChOffsetTitleDetail;

    private TextView tvCenterChCapTitle, tvCenterChCapTitleDetail;

    private TextView tvCenterChGainTitle_2, tvCenterChGainTitleDetail_2;
    private TextView tipsFactoryAdjust;
    private DialogOk dialogOk;
    private EditText editText;



    private String calibration, unCalibration;

    private Button btnTest;



    private List<TextView>tvTitles = new ArrayList<>();
    private List<TextView>tvDetails = new ArrayList<>();

    private CabteRegister cabteRegister;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_factorycalibration, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.context = getActivity();
        cabteRegister = CabteRegister.getInstance();
        initView(view);
        initControl();
        diaplayFpga();

    }
    /**
     * @return 获取所有有效的网卡
     */
    public static String[] getAllNetInterface() {
        ArrayList<String> availableInterface = new ArrayList<>();
        String[] interfaces = null;
        try {
            //获取本地设备的所有网络接口
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    // 过滤掉127段的ip地址
                    if (!"127.0.0.1".equals(ip)) {
                        if (ni.getName().substring(0, 3).equals("eth")) {//筛选出以太网
                            availableInterface.add(ni.getName() + ":" + ip);
                        }
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        int size = availableInterface.size();
        if (size > 0) {
            interfaces = new String[size];
            for (int i = 0; i < size; i++) {
                interfaces[i] = availableInterface.get(i);
            }
        }
        return interfaces;
    }

    private void initView(View view) {
        editText = (EditText) view.findViewById(R.id.editText1);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
            }
        });

        btnReset = (Button) view.findViewById(R.id.btnReset);

        btnTop = (Button) view.findViewById(R.id.topBtn);

        tvNetip = (TextView)view.findViewById(R.id.netip);
        tvTopZeroTitle = (TextView) view.findViewById(R.id.topZeroTitle);
        tvTopZeroTitleDetail = (TextView) view.findViewById(R.id.topZeroDetail);
        tvCenterChGainTitle = (TextView) view.findViewById(R.id.centerChGainTitle);
        tvCenterChGainTitleDetail = (TextView) view.findViewById(R.id.centerChGainTitleDetail);

        tvCenterChOffsetTitle = (TextView) view.findViewById(R.id.centerChOffsetTitle);
        tvCenterChOffsetTitleDetail = (TextView) view.findViewById(R.id.centerChOffsetTitleDetail);

        tvCenterChCapTitle = (TextView) view.findViewById(R.id.centerChCapTitle);
        tvCenterChCapTitleDetail = (TextView) view.findViewById(R.id.centerChCapTitleDetail);

        tvCenterChGainTitle_2 = (TextView) view.findViewById(R.id.centerChGainTitle_2);
        tvCenterChGainTitleDetail_2 = (TextView) view.findViewById(R.id.centerChGainTitleDetail_2);

        tipsFactoryAdjust = (TextView) getActivity().findViewById(R.id.tipsFactoryAdjust);

        fpgatv = (TextView)view.findViewById(R.id.fpgatv);
        tvCabteTime = view.findViewById(R.id.tvCabteTime);

        btnDeliveryDate = view.findViewById(R.id.btnDeliveryDate);
        btnDeliveryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                Date date = new Date();
                PropertyManage propertyManage = PropertyManage.getInstance();
                propertyManage.getProperty().setDeliveryDate(simpleDateFormat.format(date));
                propertyManage.commit();
                Toast.makeText(context,"设置后重启",Toast.LENGTH_LONG).show();

            }
        });

        btnBandWidth = view.findViewById(R.id.btnBandWidth);
        btnBandWidth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PropertyManage propertyManage = PropertyManage.getInstance();
                propertyManage.getProperty().setBandWidth((int)(Channel.MAX_BANDWIDTH/1000000));
                propertyManage.commit();
                Toast.makeText(context,"设置后重启",Toast.LENGTH_LONG).show();
            }
        });


        btnTest = view.findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FPGACommand fpgaCommand = FPGACommand.getInstance();
//                fpgaCommand.resetBak(0);
//                fpgaCommand.cmdFpgaDotMatrix(1);
            }
        });





        dialogOk = (DialogOk) getActivity().findViewById(R.id.dialogOk);
        btnReset.setOnClickListener(onClickListener);
        btnTop.setOnClickListener(onClickListener);


        String[] strArray = getAllNetInterface();
        if(strArray != null) {
            tvNetip.setText(String.join(",", strArray));
        }





        tvTitles.addAll(Arrays.asList(tvTopZeroTitle,tvCenterChGainTitle,tvCenterChOffsetTitle,tvCenterChCapTitle,tvCenterChGainTitle_2));
        tvDetails.addAll(Arrays.asList(tvTopZeroTitleDetail,
                tvCenterChGainTitleDetail,tvCenterChOffsetTitleDetail,tvCenterChCapTitleDetail,tvCenterChGainTitleDetail_2));

        onRefresCalibrationState();
    }


    String toastStr = "";
    private void ms_sleep(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void onRefresCalibrationState(){
        List<String> list = cabteRegister.getCalibrationItems();
        for(int i=0;i<list.size();i++){
            String str  = "未校准";
            StringBuilder sb = new StringBuilder();
            if(cabteRegister.getCalibrationItemState(i,sb)){
                str = "已校准";
            }
            tvTitles.get(i).setText(list.get(i) + " " + str);
            tvDetails.get(i).setText(sb);
        }
    }




    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);
        EventFactory.addEventObserver(EventFactory.EVENT_FACTOR_CALIBRATE_BEGIN, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_FACTOR_CALIBRATE_ITEM_START, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_FACTOR_CALIBRATE_END, eventUIObserver);
        EventFactory.addEventObserver(EventFactory.EVENT_FPGA_LOAD_OK,eventUIObserver);
    }

    private void setCache() {

        onRefresCalibrationState();
    }

    private String getString(boolean b) {
        if (StrUtil.isEmpty(calibration)) {
            calibration = context.getString(R.string.calibration);
        }
        if (StrUtil.isEmpty(unCalibration)) {
            unCalibration = context.getString(R.string.unCalibration);
        }
        return b ? calibration : unCalibration;
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception {
            setCache();
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutFactoryCalibration, true);
        }
    };



    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
             if (v.getId() == btnTop.getId()) {
                 FactorCalibrate.getInstance().begin_upPard();
                 Logger.i("calibration:click");
                 RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.TOPSLIP, false));
            }else if(v.getId() == btnReset.getId()){
                 String userInput = editText.getText().toString();
                 // 处理用户输入
                 if(userInput.equalsIgnoreCase("micsig_123")) {
                     editText.setText("");
                     calibrationReset();
                     Toast.makeText(context,"清空校准数据完成",Toast.LENGTH_LONG).show();
                 }else{
                     Toast.makeText(context,"密码错误",Toast.LENGTH_LONG).show();
                 }
            }


        }
    };

    private void calibrationReset(){
        CabteRegister cabteRegister = CabteRegister.getInstance();
        FactorCalibrate factorCalibrate = FactorCalibrate.getInstance();
        cabteRegister.rstDefaultVal();
        cabteRegister.saveFactoryCalibrateParam();
        cabteRegister.clearCalibrationState();
        cabteRegister.saveCalibrationStateParam();
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                setCache();
            }
        },10000);
    }




    private EventUIObserver eventUIObserver = new EventUIObserver() {
        @Override
        public void update(Object data) {
            EventBase eventBase = (EventBase) data;
            if (eventBase.getId() == EventFactory.EVENT_FACTOR_CALIBRATE_BEGIN) {
                Logger.i("calibration:begin");
                Command.get().getMeasure().factoryCalibration();
                tipsFactoryAdjust.setVisibility(View.VISIBLE);
                //锁屏幕，锁键盘
                ScreenControls.getInstance().lockScreen(ScreenControls.LOCK_FACTORY_ADJUST);

            } else if (eventBase.getId() == EventFactory.EVENT_FACTOR_CALIBRATE_END) {
                tipsFactoryAdjust.setVisibility(View.GONE);
                Logger.i("calibration:end");
                //解锁屏幕，解锁键盘
                ScreenControls.getInstance().unLockScreen(ScreenControls.LOCK_FACTORY_ADJUST);

                Bundle bundle = (Bundle) eventBase.getData();
                int err = bundle.getInt(FactorCalibrate.ERROR_KEY);
                boolean b = err == 0;
                Logger.i("FactoryCalibration:" + (b ? "success" : "failed"));
                int count = FactorCalibrate.getJIaoZhunCnt();
                if (count > 0) {
                    String result = b ? "成功" : ("失败,\n错误代码:" + err);
                    String dialogMsg = "此次校准(";
                    for (int i = 0; i < count; i++) {
                        int calibrationId = FactorCalibrate.getCablicationID(i);
                        int id = FactorCalibrate.getCablicationID(i);
                        ArrayList<String> stringList =
                                bundle.getStringArrayList(CalibrateService.getInstance().getCalibrate(id).getTAG());
                        Logger.i("<<<========================================================================================");
                        if (stringList != null) {
                            for (String x : stringList
                            ) {
                                Logger.i(x);
                            }
                        }
                        Logger.i("========================================================================================>>>");
                        String symbol = i != 0 ? "、" : "";
                        switch (calibrationId) {
                            case CalibrateService.ZERO_CALIBRATE:

                                dialogMsg += symbol + "零点校准";
                                break;
                            case CalibrateService.CHCOEF_CALIBRATE: {

                                dialogMsg += symbol + "偏移量系数校准";
                            }
                                break;
                            case CalibrateService.CHGAIN_CALIBRATE: {
                                //档位很多，需要全部都校准成功才显示“已校准”

                                dialogMsg += symbol + "通道增益校准";
                            }
                                break;
                            case CalibrateService.CHGAIN_CALIBRATE_EX: {

                                dialogMsg += symbol + "通道增益校准";
                            }
                            break;
                            case CalibrateService.CHCAP_CALIBRATE: {

                                dialogMsg += symbol + "电容校准";
                            }
                                break;
                        }
                    }
                    onRefresCalibrationState();
                    dialogMsg += ")" + result;
                    dialogOk.setData(dialogMsg, null, null);
                    CabteRegister.getInstance().saveCalibrationStateParam();
                }
            } else if (eventBase.getId() == EventFactory.EVENT_FACTOR_CALIBRATE_ITEM_START) {
                int index = (int) eventBase.getData();
                Logger.i("calibration:" + index);
                String s = "";
                switch (index) {
                    case CalibrateService.ZERO_CALIBRATE:
                        s = "零点校准";
                        break;
                    case CalibrateService.CHCOEF_CALIBRATE:
                        s = "偏移量系数校准";
                        break;
                    case CalibrateService.CHGAIN_CALIBRATE_EX:
                    case CalibrateService.CHGAIN_CALIBRATE:
                        s = "通道增益校准";
                        break;
                    case CalibrateService.CHCAP_CALIBRATE:
                        s = "电容校准";
                        break;
                }
                tipsFactoryAdjust.setText(s + "正在进行中。。。");
            } else if (eventBase.getId() == EventFactory.EVENT_FPGA_LOAD_OK){

            }
        }
    };

    private void diaplayFpga(){
        fpgatv.postDelayed(new Runnable() {
            @Override
            public void run() {
                fpgatv.setText("fpga ver:" + Scope.fpgaVer
                        + ",\nT:" + Scope.fpgaTemperature1 + ","  + Scope.fpgaTemperature2 + "°C"
                        + ",\n" + Scope.fanSpeed[0] + "," + Scope.fanSpeed[1] + "," + Scope.fanSpeed[2] + "," + Scope.fanSpeed[3]
                        );

                tvCabteTime.setText("time:" + CabteRegister.getInstance().getCabteTime());

                String[] strArray = getAllNetInterface();
                if(strArray != null) {
                    tvNetip.setText(String.join(",", strArray));
                }

                diaplayFpga();
            }
        },3000);
    }



}
