package com.micsig.tbook.scope.Data;


import static com.micsig.tbook.scope.math.MathNative.byteBufferToIntArray;

import android.annotation.SuppressLint;
import android.provider.Settings;

import com.micsig.base.Logger;
import com.micsig.base.Utils;
import com.micsig.tbook.scope.Calibrate.HwConfig;
import com.micsig.tbook.scope.Sample.MemDepthFactory;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.mem.Memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhuzh on 2018-3-27.
 */

public class WaveData extends DataBuffer {

    public static final int WAVE_HEADER_SIZE = 256;
    private static final int MAGICNUM = 0x20180411;
    public static final int VERSION = 2;
    public static final int VERSION_3 = 3;

    public static final int BUFFER_SIZE = 400 * 1000 * 4;//300K * int32

    public WaveData() {
        super(BUFFER_SIZE);
        setMagicNum(MAGICNUM);
        setVersion(VERSION_3);
        setBytesPerPoint(4);
        setWaveLength(0);
        setChNum(1);
        setIdx(0);
        setChannelCount(Scope.getInstance().getChNum());
        verification();
    }

    //幻数
    private void setMagicNum(int magicNum) {
        setVal(0, magicNum);
    }

    private int getMagicNum() {
        return getIntVal(0);
    }

    public static final int VERSION_IDX = 4;
    //版本
    public void setVersion(int version) {
        setVal(VERSION_IDX, version);
    }

    public int getVersion() {
        return getIntVal(VERSION_IDX);
    }

    //数据长度字节
    public void setWaveLength(int waveLength) {
        setVal(8, waveLength);
    }

    public int getWaveLength() {
        return getIntVal(8);
    }

    //每个采样点几个字节存储
    public void setBytesPerPoint(int bytes) {
        setVal(12, bytes);
    }

    public int getBytesPerPoint() {
        return getIntVal(12);
    }

    // 0 通道波形，1 数学双波形，2 FFT
    public static final int DYNAMIC_WAVE = 0;
    public static final int MATH_WAVE = 1;
    public static final int FFT_WAVE = 2;
    public void setWaveType(int waveType) {
        setVal(16, waveType);
    }

    public int getWaveType() {
        return getIntVal(16);
    }

    // startX
    public void setStartX(int startX) {
        setVal(20, startX);
    }

    public int getStartX() {
        return getIntVal(20);
    }

    // endX
    public void setEndX(int endX) {
        setVal(24, endX);
    }

    public int getEndX() {
        return getIntVal(24);
    }

    //SampRate
    public void setSampRate(double sampFre) {
        setVal(28, sampFre);
    }

    public double getSampRate() {
        return getDoubleVal(28);
    }

    //xPos
    public void setXPos(long xPos) {
        setVal(36, xPos);
    }

    public long getXPos() {
        return getLongVal(36);
    }

    //yPos
    private void setYPos(int yPos) {
        setVal(44, yPos);
    }

    private int getYPos() {
        return getIntVal(44);
    }

    //probeType
    public void setProbeType(int probeType) {
        setVal(48, probeType);
    }

    public int getProbeType() {
        return getIntVal(48);
    }

    //ProbeRate
    public void setProbeRate(double probeRate) {
        setVal(52, probeRate);
    }

    public double getProbeRate() {
        return getDoubleVal(52);
    }

    //V val
    public void setVScaleVal(double vScaleVal) {
        setVal(60, vScaleVal);
    }

    public double getVScaleVal() {
        return getDoubleVal(60);
    }

    //T val
    public void setTimeScaleVal(double timeScaleVal) {
        setVal(68, timeScaleVal);
    }

    public double getTimeScaleVal() {
        return getDoubleVal(68);
    }

    //总时间
    public void setTotalTime(double totalTime) {
        setVal(76, totalTime);
    }

    public double getTotalTime() {
        return getDoubleVal(76);
    }

    //一屏时间
    public void setOneScreenTime(double oneScreenTime) {
        setVal(84, oneScreenTime);
    }

    public double getOneScreenTime() {
        return getDoubleVal(84);
    }

    //chIdx
    public void setChIdx(int chIdx) {
        setVal(92, chIdx);
    }

    public int getChIdx() {
        return getIntVal(92);
    }

    //每个垂直像素幅值
    public void setVerticalPerPix(double val) {
        setVal(96, val);
    }

    public double getVerticalPerPix() {
        return getDoubleVal(96);
    }

    //ad pix
    public void setAdPix(double val) {
        setVal(104, val);
    }

    public double getAdPix() {
        return getDoubleVal(104);
    }

    //SampRate2display
    public void setSampRate2display(double sampFre) {
        setVal(112, sampFre);
    }

    public double getSampRate2display() {
        return getDoubleVal(112);
    }

    //MemDepth 注意：记录机器的存储标准，而不是具体的存储深度值
    public void setMemDepthSet(@MemDepthFactory.MEM_DEPTH int memDepth) {
        setVal(120, memDepth);
    }

    public int getMemDepthSet() {
        return getIntVal(120);
    }

    //MemDepthItemIdx
    public void setMemDepthItemIdx(int memDepthItemIdx) {
        setVal(124, memDepthItemIdx);
    }

    public int getMemDepthItemIdx() {
        return getIntVal(124);
    }

    private static final int PROBE_STR_LEN = 12;
    private String probeStr = "";
    public void setProbeStr(String probeStr){

        if(probeStr != null && !probeStr.equals(this.probeStr)) {

            byte[] arr = new byte[PROBE_STR_LEN];
            byte[] bytes = probeStr.getBytes();
            for (int i = 0; i < PROBE_STR_LEN; i++) {
                if (i < bytes.length) {
                    arr[i] = bytes[i];
                }
            }
            if (bytes.length < PROBE_STR_LEN) {
                setVal(128, arr);
                this.probeStr = probeStr;
            }

        }
    }
    public String getProbeStr(){
        byte[] bytes = getBytesVal(128,PROBE_STR_LEN);
        return  new String(bytes);
    }

    public static final int WAVE_BIN = 0;
    public static final int SEGMENT_BIN = 1;

    public void setBinType(int type){
        setVal(140,type);
    }
    public int getBinType(){
        return getIntVal(140);
    }
    public void setPlaceVal(int val){
        setVal(144,val);
    }
    public int getPlaceVal(){
        return getIntVal(144);
    }

    public void setSegmentNums(int nums){
        setVal(148,nums);
    }
    public int getSegmentNums(){
        return getIntVal(148);
    }
    public void setSegmentLen(int len){
        setVal(152,len);
    }
    public int getSegmentLen(){
        return getIntVal(152);
    }


    public static final int CH_NUM_IDX = 156;
    public void setChNum(int n){
        setVal(CH_NUM_IDX,n);
    }
    public int getChNum(){
        return getIntVal(CH_NUM_IDX);
    }

    public void setIdx(int idx){
        setVal(160,idx);
    }

    public int getIdx(){
        return getIntVal(160);
    }

    public void setVerticalPerGridPixels(int val){
        setVal(164,val);
    }
    public int getVerticalPerGridPixels(){
        return getIntVal(164);
    }


    private static final int LABEL_STR_LEN = 32;

    public void setLabel(String label) {
        if (label == null) label = "";
        byte[] arr = new byte[LABEL_STR_LEN];
        byte[] bytes = label.getBytes();
        for (int i = 0; i < LABEL_STR_LEN; i++) {
            if (i < bytes.length) {
                arr[i] = bytes[i];
            }
        }
        //超过长度显示截取显示
//        if (bytes.length <= LABEL_STR_LEN) {
            setVal(170, arr);
            this.label = label;
//        }
    }

    public String getLabel(){
        byte[] bytes = getBytesVal(170,LABEL_STR_LEN);
        return  new String(bytes);
    }

    public double getYPosition(){
        if(version < VERSION_3){
            return getYPos();
        }
        return getDoubleVal(202);
    }
    public void setYPosition(double val){
        setVal(202,val);
        setYPos((int) Math.round(val));
    }

    public void setWaveLen(long val) {
        setVal(210, val);
    }

    public long getWaveLen() {
        return getLongVal(210);
    }

    public void setChannelCount(int val) { //代表几通道
        setVal(218, (byte) val);
    }

    public int getChannelCount() {
        return getByteVal(218);
    }

    //
    public double getWaveFactor(){
        return getVerticalPerPix() * getVerticalPerGridPixels() / getVScaleVal();
    }
    private boolean isRoll = false;

    public void setRoll(boolean bRoll) {
        this.isRoll = bRoll;
    }

    public boolean isRoll(){
        return isRoll;
    }

    private int syncHeader = 0;
    public synchronized void setSyncHeader(int syncHeader){
        this.syncHeader = syncHeader;
    }
    public synchronized int getSyncHeader(){
        return this.syncHeader;
    }


    public int getWaveVal(int idx) {
        idx = idx * 4 + WAVE_HEADER_SIZE;
        return getIntVal(idx);
    }

    public void setWaveVal(int idx, int val){
        idx = idx * 4 + WAVE_HEADER_SIZE;
        setVal(idx,val);
    }

    private String label = "";

//    public void setLabel(String label) {
//        this.label = label;
//    }
//
//    public String getLabel() {
//        return this.label;
//    }

    private int getUseBufferSize() {
        return BUFFER_SIZE - WAVE_HEADER_SIZE;
    }

    @Override
    public int write(ByteBuffer byteBuffer, int pos, int length) {
        if (length > 0 && length <= getUseBufferSize()) {

            Memory.Memcpy(getByteBuffer(), WAVE_HEADER_SIZE, byteBuffer, pos, length);
            setWaveLength(length);
            return length;
        }
        return 0;

    }

    @Override
    public int convert16to32(ByteBuffer byteBuffer, int pos, int length, int val) {

        int nums = length / 2;
        if (nums > 0 && nums * 4 <= getUseBufferSize()) {
            byteBuffer.clear();
            Memory.Convert16to32(getByteBuffer(), WAVE_HEADER_SIZE, byteBuffer, pos, val, nums);
            setWaveLength(nums);
            byteBuffer.limit(WAVE_HEADER_SIZE + nums * 4);
            return length;
        }
        return 0;
    }
    private int version = 0;
    @Override
    protected boolean verification() {
        version = getVersion();
        return (getMagicNum() == MAGICNUM) && (VERSION == version || version == VERSION_3);
    }

    @Override
    public int read(ByteBuffer byteBuffer) {
        return 0;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public boolean saveCSV(String pathName) {
        File file = new File(pathName);
        if (!file.exists() && Utils.isDiskAvaiable(file, 15*1024*1024)) {
            FileWriter fileWriter = null;
            try {
                if (file.createNewFile()) {
                    fileWriter = new FileWriter(file);
                    StringBuilder stringBuilder = new StringBuilder(10*1024*1024);

                    double v = getTimeScaleVal() * ScopeBase.getHorizonGridCnt() / 2 - getXPos() / 1e13;
                    int nums = getWaveLength();
                    double SampleInterval = (1.0 / getSampRate());
                    double vPixVal =  getVerticalPerPix();//每像素幅度值
                    int [] buf = byteBufferToIntArray(getDirectBuffer()); //？
                    if(buf != null) {

                        for (int i = 0; i < cvsItem.length; i++) {
                            stringBuilder.append(cvsItem[i]).append(",").append(getCSVItemVal(i,getChIdx())).append(",\n");
                        }
                        DecimalFormat decimalFormat = new DecimalFormat("0.00000000E00");
                        for (int i = 0; i < nums; i++) {
                            stringBuilder.append(decimalFormat.format(i * SampleInterval - v))
                                    .append(",")
                                    .append(decimalFormat.format(buf[i] * vPixVal)).append(",\n");
                        }
                    }

                    fileWriter.write(stringBuilder.toString());
                    fileWriter.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(fileWriter != null){
                    try {
                        fileWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Memory.Sync();
            }
            if(fileWriter != null){
                return true;
            }
        }
        return false;
    }

    private String getCSVHeader() {
        return "ProID,Info,,time,Value,\n";
    }

    public static String[] cvsItem = {
            "Model",                //0
            "Record Length",        //1
            "Sample Interval",      //2
            "Trigger Time",         //3
            "Source",               //4
            "Vertical Units",       //5
            "Vertical Scale",       //6
            "Vertical Offset",      //7
            "Horizontal Units",     //8
            "Horizontal Scale",     //9
            "Probe Atten",          //10
            "Vertical Resolution",  //11
            "StartX",               //12
            "EndX",                 //13
            "Label",                //14
            "",                     //15
            "",                     //16
            "",                     //17
            "",                     //18
            "",                     //19
            "TIME"                  //20
    };

    @SuppressLint("DefaultLocale")
    public String getCSVItemVal(int idx,int chIdx) {
        String strVal = "";
        switch (idx) {
            case 0: {
                Scope scope = Scope.getInstance();
                strVal = scope.getProduct() + "/" + scope.getSn();
            }
                break;
            case 1:
                strVal = String.valueOf(getWaveLength());
                break;
            case 2:
                strVal = String.valueOf(1.0 / getSampRate()) ;
                break;
            case 3:
//                if (isRoll) {
//                    strVal = "0";
//                } else {
                    strVal = String.valueOf(1e-13 * getXPos());
//                }
//                Logger.d("Trigger Time= " + String.valueOf(1e-13 * getXPos()) + " ,isRoll= " + isRoll);
                break;
            case 20:
            case 4:
                strVal = ChannelFactory.getChannelName(chIdx);
                break;
            case 5:
                strVal = String.valueOf(ChannelFactory.getProbeType(chIdx));
                break;
            case 6:
                strVal = String.valueOf(getVScaleVal());
                break;
            case 7:
                strVal = String.valueOf(getVerticalPerPix() * getYPosition() / HwConfig.getInstance().getWavFactor());
                break;
            case 8:
                if(getWaveType() == FFT_WAVE){
                    strVal = "Hz";
                }else{
                    strVal = "s";
                }
                break;
            case 9:
                strVal = String.valueOf(getTimeScaleVal());
                break;
            case 10:
                strVal = String.valueOf(getProbeRate());
                break;
            case 11:
                strVal = String.valueOf(getVerticalPerPix());
                break;
            case 12:
                strVal = String.valueOf(getStartX());
                break;
            case 13:
                strVal = String.valueOf(getEndX());
                break;
            case 14:
                strVal = ChannelFactory.getLabel(chIdx);
                break;
            default:
                strVal = "";
                break;
        }
        return strVal;
    }



    private static String getNowTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

}
