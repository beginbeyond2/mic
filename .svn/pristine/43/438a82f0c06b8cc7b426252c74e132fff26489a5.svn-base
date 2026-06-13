package com.micsig.tbook.tbookscope.top.popwindow.keyboardtext;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import com.keanbin.pinyinime.PinyinDecoderService;
import com.micsig.base.Logger;
import com.micsig.tbook.tbookscope.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yangj on 2017/12/6.
 */

public class PinyinIME {
    private Context context;
    private String mUsr_dict_file;

    public PinyinIME(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        mUsr_dict_file = context.getFileStreamPath("usr_dict.dat").getPath();
        try {
            context.openFileOutput("dummy", 0).close();
        } catch (IOException e) {
        }
        initPinyinEngine();
    }

    /**
     * 初始化拼音引擎
     */
    private void initPinyinEngine() {
        byte usr_dict[];
        usr_dict = new byte[mUsr_dict_file.length() + 256];

        AssetFileDescriptor afd = context.getResources().openRawResourceFd(R.raw.dict_pinyin);
        if (getUsrDictFileName(usr_dict)) {
            // JNI函数：打开解码器
            PinyinDecoderService.imOpenDecoderFd(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength(), usr_dict);
        }
        try {
            afd.close();
        } catch (IOException e) {
        }
    }

    private boolean getUsrDictFileName(byte usr_dict[]) {
        if (null == usr_dict) {
            return false;
        }
        for (int i = 0; i < mUsr_dict_file.length(); i++)
            usr_dict[i] = (byte) mUsr_dict_file.charAt(i);
        usr_dict[mUsr_dict_file.length()] = 0;
        return true;
    }

    /**
     * 获得输入拼音之后的生成词组
     *
     * @param pinyin 输入的拼音
     * @return
     */
    public List<String> getChoiceList(String pinyin) {
        if(pinyin.length() > 16){
            pinyin = pinyin.substring(0,16);
        }
        byte[] pyBuf = new byte[pinyin.length() + 1];


        for (int i = 0; i < pinyin.length(); i++)
            pyBuf[i] = (byte) pinyin.charAt(i);
        pyBuf[pinyin.length()] = 0;


        PinyinDecoderService.imResetSearch();

        int totalChoicesNum = PinyinDecoderService.imSearch(pyBuf, pinyin.length());
        int mFixedLen = PinyinDecoderService.imGetFixedLen();
        List<String> choiceList = PinyinDecoderService.imGetChoiceList(0, totalChoicesNum, mFixedLen);
        Logger.i("\nPinyinEngine:totalChoicesNum:" + totalChoicesNum + "\tmFixedLen:" + mFixedLen);
//        Logger.i("\nchoiceList:" + Arrays.toString(choiceList.toArray()));
        return choiceList;
    }

    /**
     * 获得点击汉字之后的联想词组
     *
     * @param predict 点击的汉字
     * @return
     */
    public List<String> getPredictList(String predict) {
        int totalPredictsNum = PinyinDecoderService.imGetPredictsNum(predict);
        List<String> predictList = PinyinDecoderService.imGetPredictList(0, totalPredictsNum);
        Logger.i("\ntotalPredictsNum:" + totalPredictsNum +
                "\npredictList:" + Arrays.toString(predictList.toArray()));
        return predictList;
    }

}
