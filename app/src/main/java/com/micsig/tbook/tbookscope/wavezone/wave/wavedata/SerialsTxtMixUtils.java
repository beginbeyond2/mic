package com.micsig.tbook.tbookscope.wavezone.wave.wavedata;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplay;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplayTxtMix;
import com.micsig.tbook.tbookscope.util.CacheUtil;

import java.util.HashMap;

import io.reactivex.rxjava3.functions.Consumer;

/**
 * Created by limh on 2024/8/9.
 */
public class SerialsTxtMixUtils {

    private static final HashMap<String, Boolean> serialsCheckMap = new HashMap<>();
    private static volatile SerialsTxtMixUtils instance = null;
    private static final String S1Key = "S1";
    private static final String S2Key = "S2";
    private static final String S3Key = "S3";
    private static final String S4Key = "S4";

    public HashMap<String, Boolean> getSerialsCheckMap() {
        checkAndUpdateMap();
        return serialsCheckMap;
    }

    public static SerialsTxtMixUtils getInstance() {
        if (instance == null) {
            synchronized (ChannelFactory.class) {
                if (instance == null) {
                    initControl();
                    instance = new SerialsTxtMixUtils();
                }
            }
        }
        return instance;
    }


    private static void initControl() {
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_DISPLAY).subscribe(consumerTopSlipTitle);
    }


    private static final Consumer<TopMsgDisplay> consumerTopSlipTitle = new Consumer<TopMsgDisplay>() {
        @Override
        public void accept(TopMsgDisplay topMsgDisplay) throws Exception {
            if (topMsgDisplay != null && topMsgDisplay.getDisplayDetail() instanceof TopMsgDisplayTxtMix) {
                //这里判断组合状态  S1&S2&S3&S4
                TopMsgDisplayTxtMix topMsgDisplayTxtMix = (TopMsgDisplayTxtMix) topMsgDisplay.getDisplayDetail();
                boolean s1Check = topMsgDisplayTxtMix.isS1Select();
                boolean s2Check = topMsgDisplayTxtMix.isS2Select();
                boolean s3Check = topMsgDisplayTxtMix.isS3Select();
                boolean s4Check = topMsgDisplayTxtMix.isS4Select();
                serialsCheckMap.put(S1Key, s1Check);
                serialsCheckMap.put(S2Key, s2Check);
                serialsCheckMap.put(S3Key, s3Check);
                serialsCheckMap.put(S4Key, s4Check);
                Logger.d("limh SerialsTxtMixUtils", "serialsCheckMap:" + serialsCheckMap.toString());
            }
        }
    };

    //判断并更新map
    private void checkAndUpdateMap() {
        boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S1);
        boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S2);
        boolean s3Check = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S3);
        boolean s4Check = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S4);
        if (Boolean.TRUE.equals(serialsCheckMap.get(S1Key)) != s1Check) {
            serialsCheckMap.put(S1Key, s1Check);
        }
        if (Boolean.TRUE.equals(serialsCheckMap.get(S2Key)) != s2Check) {
            serialsCheckMap.put(S2Key, s2Check);
        }
        if (Boolean.TRUE.equals(serialsCheckMap.get(S3Key)) != s3Check) {
            serialsCheckMap.put(S3Key, s3Check);
        }
        if (Boolean.TRUE.equals(serialsCheckMap.get(S4Key)) != s4Check) {
            serialsCheckMap.put(S4Key, s4Check);
        }
    }

}
