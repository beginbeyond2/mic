package com.micsig.tbook.scope.Data;

import android.util.Log;

import com.micsig.tbook.scope.Sample.MemDepthFactory;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.vertical.VerticalAxis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadCsv {
    private static final String TAG = "LoadCsv";
    public class CsvInfo{
        public int chIdx = -1;
        public double verticalScale = 0;
        public double verticalOffset = 0;
        public double verticalResolution = 0;

        public double probeRate = 1;

        public int probeType = VerticalAxis.PROBE_TYPE_CUSTOM;
        public String probeStr = "";
        public long waveLength  = 0;
        public double triggerTime = 0;
        public double timeScale = 0;
        public double sampleInterval = 0;
        public String HorizontalUnits="";

        public int StartX;
        public int EndX;
        public boolean bValid = false;
        private String Label = "";
        public CsvInfo(){

        }

        @Override
        public String toString() {
            return "CsvInfo{" +
                    "chIdx=" + chIdx +
                    ", verticalScale=" + verticalScale +
                    ", verticalOffset=" + verticalOffset +
                    ", verticalResolution=" + verticalResolution +
                    ", probeRate=" + probeRate +
                    ", probeType=" + probeType +
                    ", probeStr=" + probeStr +
                    ", waveLength=" + waveLength +
                    ", triggerTime=" + triggerTime +
                    ", timeScale=" + timeScale +
                    ", sampleInterval=" + sampleInterval +
                    ", HorizontalUnits='" + HorizontalUnits + '\'' +
                    ", bValid=" + bValid +
                    ", Label=" + Label +
                    '}';
        }
    }
    private ArrayList<CsvInfo> csvInfos = new ArrayList<>();
    public interface ILoadCsvProgress{
        void onProgress(int val);
    }
    int progressVal = -1;
    boolean bFinish = false;
    FileReader fileReader = null;
    BufferedReader bufferedReader = null;
    ILoadCsvProgress loadCsvProgress;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    public LoadCsv(){

    }

    public boolean isFinish(){
        return bFinish;
    }

    public void setLoadCsvProgress(ILoadCsvProgress progress){
        this.loadCsvProgress = progress;
    }
    public int getChNums(){
        return csvInfos.size();
    }
    public ArrayList<Integer> getCsvInfos(){
        ArrayList<Integer> chs = new ArrayList<>();
        for(CsvInfo c:csvInfos){
            if(c.bValid){
                Log.d(TAG,c.toString());
                chs.add(c.chIdx);
            }
        }
        return chs;
    }

    public boolean load(String pathName){
        boolean b = false;
        File file = new File(pathName);
        if(file.exists()){
            close();
            try {
                csvInfos.clear();
                fileReader = new FileReader(file);
                bufferedReader = new BufferedReader(fileReader);
                b = parseCsvHeader(bufferedReader) && csvInfos.size() > 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return b;
    }

    public void loadToRef(Map<Integer, Integer> refmap) {
        bFinish = false;
        executorService.execute(() -> {
            long tmpmask = 0;
            Map<Integer, RefChannel> chmap = new HashMap<>();
            Map<Integer, WaveData> wavmap = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : refmap.entrySet()) {
                RefChannel refChannel = ChannelFactory.getRefChannel(entry.getKey());
                if (refChannel != null) {
                    chmap.put(entry.getValue(), refChannel);
                    wavmap.put(entry.getValue(), (WaveData) refChannel.obtain());
                }
            }
            onLoadProgress(0);
            CsvInfo c = null;
            long maxLength = 0;
            for (int i = 0; i < COLS; i++) {
                if ((rowmask & (1L << i)) != 0) {
                    c = csvInfos.get(i);
                    if (c.bValid && chmap.containsKey(c.chIdx)) {
                        tmpmask |= 1L << i;
                        WaveData waveData = wavmap.get(c.chIdx);
                        if (waveData != null) {
                            waveData.clear();
                            double v = c.verticalScale / ScopeBase.getVerticalPerGridPixels();
                            setWaveData(waveData,
                                    c.chIdx,
                                    c.timeScale,
                                    1 / c.sampleInterval,
                                    (long) (c.triggerTime / 1e-13),
                                    c.verticalResolution,
                                    c.probeType,
                                    c.probeStr,
                                    c.probeRate,
                                    c.verticalScale,
                                    (c.verticalOffset / v),
                                    c.waveLength,
                                    c.StartX,
                                    c.EndX,
                                    c.Label,
                                    c.HorizontalUnits
                            );
                            if (maxLength < c.waveLength) {
                                maxLength = c.waveLength;
                            }
                        }
                    }
                }
            }

            try {
                String line = null;
                double[][] VALS = new double[COLS][ROWS];
                int row = 0;
                int n = 0;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] tmp = line.split(",");
                    for (int i = 0; i < tmp.length; i++) {
                        if ((tmpmask & (1L << i)) != 0) {
                            c = csvInfos.get(i);
                            if (n + i < c.waveLength) {
                                String vstr = tmp[i].trim();
                                if (!vstr.isEmpty()) {
                                    VALS[i][row] = Double.parseDouble(vstr);
                                }
                            }
                        }
                    }
                    row++;
                    if (row >= ROWS) {
                        for (int i = 0; i < COLS; i++) {
                            if ((tmpmask & (1L << i)) != 0) {
                                c = csvInfos.get(i);
                                WaveData waveData = wavmap.get(c.chIdx);
                                if (waveData != null) {
                                    for (int j = 0; j < row; j++) {
                                        int xi = n + j;
                                        if (c.waveLength > xi) {
                                            waveData.setWaveVal(xi, (int) (Math.round(VALS[i][j] / c.verticalResolution)));
                                        }
                                    }
                                }
                            }
                        }
                        n += row;
                        row = 0;
                    }
                    onLoadProgress((int) ((n + row) * 100 / maxLength));
                }
                if (row > 0) {
                    for (int i = 0; i < COLS; i++) {
                        if ((tmpmask & (1L << i)) != 0) {
                            c = csvInfos.get(i);
                            WaveData waveData = wavmap.get(c.chIdx);
                            if (waveData != null) {
                                for (int j = 0; j < row; j++) {
                                    int xi = n + j;
                                    if (c.waveLength > xi) {
                                        waveData.setWaveVal(xi, (int) (Math.round(VALS[i][j] / c.verticalResolution)));
                                    }
                                }
                            }
                        }
                    }
                    n += row;
                }
                Log.d(TAG, "n:" + n);

            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Integer v : refmap.values()) {
                RefChannel refChannel = chmap.get(v);
                WaveData waveData = wavmap.get(v);
                if (refChannel != null && waveData != null) {
                    refChannel.loadWave();
                    refChannel.recycle(waveData);
                }
            }
            close();
            onLoadProgress(100);
            bFinish = true;
        });
    }
    private static final int ROWS = 10000;
    private long rowmask = 0;
    private int COLS = 0;
    private static final String TIME = WaveData.cvsItem[20];

    private boolean parseCsvHeader(BufferedReader reader) throws IOException {
        String line = null;
        rowmask = 0;
        COLS = 0;
        while ((line = reader.readLine()) != null){
            String[] tmp = line.split(",");
            if(tmp.length > 0){
                int i = 0;
                tmp[0] = tmp[0].trim();
                for(i=0;i<WaveData.cvsItem.length;i++){
                    if(tmp[0].equalsIgnoreCase(WaveData.cvsItem[i])){
                        break;
                    }
                }
                if(COLS < tmp.length){
                    COLS = tmp.length;
                }
                CsvInfo c = null;
                for(int j=0;j<tmp.length;j++){
                    tmp[j] = tmp[j].trim();
                    if(j >= csvInfos.size()){
                        c = new CsvInfo();
                        csvInfos.add(c);
                    }else{
                        c = csvInfos.get(j);
                    }

                    if(!tmp[j].isEmpty()){
                        if(!tmp[j].equalsIgnoreCase(tmp[0])){
                            rowmask |= 1L << j;
                            parseItem(c,i,tmp[j]);
                        }
                    }
                }
                if(tmp[0].startsWith(TIME)){
                    return true;
                }
            }
        }
        return false;
    }
    private long str2long(String s){
        s = s.replaceAll("[^-0-9]","").trim();
        if(s.isEmpty() || s.equals("-")){
            return 0;
        }
        return Long.parseLong(s);
    }
    private double str2double(String s){
        s = s.replaceAll("[^0-9.eE\\-+]","").trim();
        if(s.isEmpty()){
            return 0;
        }
        return Double.parseDouble(s);
    }
    private void setWaveData(WaveData waveData,
                      int chIdx,
                      double timeScaleVal,
                      double sampleRate,
                      long xPos,
                      double verticalPerPix,
                      int probeType,
                      String probeStr,
                      double probeRate,
                      double vScaleVal,
                      double yPos,
                      long waveLength,
                             int startX,
                             int endX,
                             String label,
                             String HorizontalUnits
    ){
        waveData.setWaveType(!ChannelFactory.isDynamicCh(chIdx) ? WaveData.MATH_WAVE : WaveData.DYNAMIC_WAVE);
        if (HorizontalUnits.equalsIgnoreCase("Hz")) {
            waveData.setWaveType(WaveData.FFT_WAVE);
        }
        waveData.setTimeScaleVal(timeScaleVal);
        waveData.setChIdx(chIdx);
        waveData.setStartX(startX);
        waveData.setEndX(endX);
        waveData.setSampRate(sampleRate);
        waveData.setSampRate2display(sampleRate);
        waveData.setAdPix(verticalPerPix);
        waveData.setXPos(xPos);
        waveData.setMemDepthSet(MemDepthFactory.getDefaultMemDepth());
        waveData.setMemDepthItemIdx(0);
        waveData.setBinType(WaveData.WAVE_BIN);

        waveData.setSegmentNums(1);
        waveData.setSegmentLen((int)waveLength);
        waveData.setVerticalPerGridPixels(ScopeBase.getVerticalPerGridPixels());

        waveData.setPlaceVal(0);
        waveData.setProbeRate(probeRate);
        waveData.setProbeType(probeType);
        waveData.setProbeStr(probeStr);
        waveData.setVScaleVal(vScaleVal);
        waveData.setVerticalPerPix(verticalPerPix);
        waveData.setYPosition(yPos);
        waveData.setRoll(false);
        waveData.setWaveLength((int)waveLength);

        waveData.setLabel(label);
    }
    private void parseItem(CsvInfo c,int i,String s){

        switch (i){
            case 1: //Record Length
                c.waveLength = str2long(s);
                break;
            case 2: //Sample Interval
                c.sampleInterval = str2double(s);
                break;
            case 3: //Trigger Time
                c.triggerTime = str2double(s);
                break;
            case 4: //Source
                for (int k = ChannelFactory.CH1; k < ChannelFactory.REF_MAX; k++) {
                    if (s.contains(ChannelFactory.getChannelName(k))) {
                        c.chIdx = k;
                        break;
                    }
                }
                break;
            case 5: //Vertical Units
                for (int k = VerticalAxis.PROBE_TYPE_MIN; k < VerticalAxis.PROBE_TYPE_MAX; k++) {
                    String str = ChannelFactory.getProbeString(k);
                    if (s.equalsIgnoreCase(str)) {
                        c.probeType = k;
                        break;
                    }
                }
                if (c.probeType == VerticalAxis.PROBE_TYPE_CUSTOM) {
                    c.probeStr = s;
                }
                break;
            case 6: //Vertical Scale
                c.verticalScale = str2double(s);
                break;
            case 7: //Vertical Offset
                c.verticalOffset = str2double(s);
                break;
            case 8: //Horizontal Units
                if(s.contains("s")){
                    c.HorizontalUnits = "s";
                }else {
                    c.HorizontalUnits = "Hz";
                }
                break;
            case 9: //Horizontal Scale
                c.timeScale = str2double(s);
                break;
            case 10: //Probe Atten
                c.probeRate = str2double(s);
                break;
            case 11: //Vertical Resolution
                c.verticalResolution = str2double(s);
                break;
            case 12:
                c.StartX = (int)str2long(s);
                break;
            case 13:
                c.EndX = (int)str2long(s);
                break;
            case 14:
                c.Label = s;
                break;
            case 20: //TIME
                c.bValid = true;
                break;
        }
    }


    private void close(){
        if(fileReader != null){
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileReader = null;
            bFinish = false;
        }
    }


    private void onLoadProgress(int val){
        if(progressVal != val) {
            progressVal = val;
            if (loadCsvProgress != null) {
                loadCsvProgress.onProgress(val);
            }
        }
    }


    public static final boolean onTest(){
        if(Scope.getInstance().isUI()) {
            LoadCsv loadCsv = new LoadCsv();
            loadCsv.setLoadCsvProgress(new ILoadCsvProgress() {
                @Override
                public void onProgress(int val) {
                    Log.d(TAG,"val:" + val);
                }
            });
            boolean b = loadCsv.load("/storage/emulated/0/Oscilloscope/csvwave/pp_2506230005.csv");
            Log.d(TAG, "chNums:" + loadCsv.getChNums() + ",b:" + b);

            ArrayList<Integer> xx = loadCsv.getCsvInfos();
            for(int c:xx){
                Log.d(TAG,"ch" + c);
            }

            Map<Integer, Integer> map = new HashMap<>();
            for (int i = 0; i < 8; i++) {
                map.put(ChannelFactory.REF1 + i, ChannelFactory.CH1 + i);
                ChannelFactory.chClose(ChannelFactory.REF1 + i);
            }
            loadCsv.loadToRef(map);

            for (int i = 0; i < 8; i++) {
                ChannelFactory.chOpen(ChannelFactory.REF1 + i);
            }
            return true;
        }else{
            return false;
        }
    }

}
