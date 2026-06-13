package com.micsig.tbook.scope.surface;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLES32;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.chillingvan.canvasgl.ICanvasGL;
import com.chillingvan.canvasgl.glcanvas.BasicTexture;
import com.chillingvan.canvasgl.glcanvas.GLPaint;
import com.chillingvan.canvasgl.glcanvas.RawTexture;
import com.chillingvan.canvasgl.textureFilter.BasicTextureFilter;
import com.micsig.base.FilterThread;
import com.micsig.tbook.scope.Data.IDataListener;
import com.micsig.tbook.scope.Display.Display;
import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.ChannelFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by zhuzh on 2018-4-2.
 */

public class SurfacePreview extends SurfaceTextureRenderer {

    private static final String TAG = "SurfacePreview";
    public static final int XY_WIDTH = 800;
    public static final int XY_HEIGHT = 800;
    public static final int LAYER_CH1 = 0;
    public static final int LAYER_CH2 = LAYER_CH1 + 1;
    public static final int LAYER_CH_MAX = LAYER_CH2 + 1;
    public static final int LAYER_MATH1 = LAYER_CH2 + 1;
    public static final int LAYER_MATH2 = LAYER_MATH1 + 1;
    public static final int LAYER_MATH3 = LAYER_MATH2 + 1;
    public static final int LAYER_MATH4 = LAYER_MATH3 + 1;
    public static final int LAYER_MATH5 = LAYER_MATH4 + 1;
    public static final int LAYER_MATH6 = LAYER_MATH5 + 1;
    public static final int LAYER_MATH7 = LAYER_MATH6 + 1;
    public static final int LAYER_MATH8 = LAYER_MATH7 + 1;
    public static final int LAYER_MATH_MAX = LAYER_MATH8 + 1;
    public static final int LAYER_REF1 = LAYER_MATH8 + 1;
    public static final int LAYER_REF2 = LAYER_REF1 + 1;
    public static final int LAYER_REF3 = LAYER_REF2 + 1;
    public static final int LAYER_REF4 = LAYER_REF3 + 1;
    public static final int LAYER_REF5 = LAYER_REF4 + 1;
    public static final int LAYER_REF6 = LAYER_REF5 + 1;
    public static final int LAYER_REF7 = LAYER_REF6 + 1;
    public static final int LAYER_REF8 = LAYER_REF7 + 1;
    public static final int LAYER_REF_MAX = LAYER_REF8 + 1;
    public static final int LAYER_XY   = LAYER_REF8 + 1;
    public static final int LAYER_MAX   = LAYER_XY + 1;

    //private static final int DATA_VALID = (1<<0);
    private static final int CLEAR_AFTRGLOW = (1<<1);
    private static final int REF_XY_VALID = (1<<2);
    private static final int CLEAR_WAVE = (1<<3);
    private static final int GET_WAVE = (1<<4);

    private static final int CLEAR_MATH_AFTRGLOW = (1 << 5);

    private static final int CLEAR_MATH_AFTRGLOW_MASK = (0xFF << 5);

    private int chIdx = 0;//波形移动过程中当前操作的通道号

    private volatile int mFlag = 0;


    private void setFlag(int flag){
        synchronized (this){
            mFlag |= flag;
        }
    }
    private void clearFlag(int flag){
        synchronized (this){
            mFlag &= ~flag;
        }
    }
    private boolean isFlag(int flag){
        synchronized (this){
            return (mFlag & flag) != 0;
        }
    }

    private SurfacePreviewHandler handler = null;


    public interface OnLayerTextureValidListener{
        void OnLayerTextureListener(List<LayerTexture> layerTexturelist);
    }


    private FilterThread rennderThread = new FilterThread(TAG);
    private HandlerThread mHandlerThread = new HandlerThread(TAG);
    private volatile boolean bDraw = false;

    private int [] idx = new int[LAYER_MATH_MAX];

    private volatile boolean bCCTEnable = false;
    private volatile boolean [] bAfterglow = new boolean[LAYER_MATH_MAX];
    private volatile long [] afterglowTime = new long[LAYER_MATH_MAX];
    private volatile long [] refreshTime = new long[LAYER_MATH_MAX];



    private volatile boolean bRunAutoAfterflow = false;
    private OnLayerTextureValidListener mlayerTextureValidListener;
    private BasicTextureFilter basicTextureFilter = new BasicTextureFilter();
    private AfterglowFilter [] afterglowFilter = {new AfterglowFilter(),new AfterglowFilter()};
    private Gray2RGBAFilter [] gray2RGBAFilter = {new Gray2RGBAFilter(0),new Gray2RGBAFilter(1)};

    private Gray2ColorTemperature[] gray2ColorTemperature = {new Gray2ColorTemperature(0),new Gray2ColorTemperature(1)};


//    private BasicTextureFilter gray2ColorFilter = gray2RGBAFilter;
    private IDataListener dataListener = null;


    private RawTexture [][] afterglowRawTexture = new RawTexture[LAYER_MATH_MAX][3];


    private LayerTexture[] layerTexture = new LayerTexture[LAYER_MAX];
    private List<LayerTexture> layerTextureList = new ArrayList<>();

    private static int maxChLayer = LAYER_CH_MAX;
    private static int maxMathLayer = LAYER_MATH_MAX;
    private static int maxRefLayer = LAYER_REF_MAX;

    public static void setMaxChLayer(int maxLayer){
        maxChLayer = LAYER_CH1 + maxLayer;
    }
    public static void setMaxMathLayer(int maxLayer){
        maxMathLayer = LAYER_MATH1 + maxLayer;
    }
    public static void setMaxRefLayer(int maxLayer){
        maxRefLayer = LAYER_REF1 + maxLayer;
    }


    public static boolean isVaildLayer(int layer){
        return layer >= 0 && layer < LAYER_MAX;
    }
    public SurfaceNative getSurfaceNative(int layer){
        if(isVaildLayer(layer)){
            return getLayerTexture(layer).getSurfaceNative();
        }
        return null;
    }

    public LayerTexture getLayerTexture(int layer){
        if(isVaildLayer(layer)){
            return layerTexture[layer];
        }
        return null;
    }

    public void setLightType(int lightType){

        //grayMapFilter.setlightType(lightType);
        //refreshWave();
    }
    public void setBrightness(int iVal){

        //grayMapFilter.setLight(iVal);
        //refreshWave();
    }
    public void setChZorder(int [] zorders){
        for (Gray2RGBAFilter f: gray2RGBAFilter) {
            f.setZorder(zorders);
        }
        for(Gray2ColorTemperature f:gray2ColorTemperature){
            f.setZorder(zorders);
        }

    }

    private void setChOffsetZeor(){
        SlideFinger.getInstance().reset();
        for (Gray2RGBAFilter f: gray2RGBAFilter) {
            f.setOffset(0,0);
        }
        for(Gray2ColorTemperature f:gray2ColorTemperature){
            f.setOffset(0,0);
        }
        if(bOffset){
            afterglowClear();
            bOffset = false;
        }
    }

    public void setZoom(boolean bZoom){
        for (Gray2RGBAFilter f: gray2RGBAFilter) {
            f.setZoom(bZoom);
        }
        for(Gray2ColorTemperature f:gray2ColorTemperature){
            f.setZoom(bZoom);
        }
    }

    public void setChIdx(int chIdx) {
        this.chIdx = chIdx;
    }

    public void setOffset(boolean bMath,int x,int y){
        if(bMath){
            int layerIdx = LAYER_MATH1;
            //ChannelFactory.MATH1  转对应的Index  LAYER_MATH1
            if (ChannelFactory.isMathCh(chIdx)) {
                layerIdx = chIdx - (ChannelFactory.MATH1 - LAYER_MATH1);
            }
            layerTexture[layerIdx].setOffset(-x,-y);
            for (Gray2RGBAFilter f: gray2RGBAFilter) {
                f.setOffset(x,0);
            }
            for(Gray2ColorTemperature f:gray2ColorTemperature){
                f.setOffset(x,0);
            }
        }else {
//            layerTexture[LAYER_MATH1].setOffset(-x,0);
            for (Gray2RGBAFilter f: gray2RGBAFilter) {
                f.setOffset(x,y);
            }
            for(Gray2ColorTemperature f:gray2ColorTemperature){
                f.setOffset(x,y);
            }
        }
        if(x  != 0 || y != 0) {
            bOffset = true;
            refreshWave();
        }
    }

    public synchronized void UpdateLayerZorder(){
        Collections.sort(layerTextureList, new Comparator<LayerTexture>() {
            @Override
            public int compare(LayerTexture lhs, LayerTexture rhs) {
                if(lhs.getZorder() < rhs.getZorder()) return -1;
                else if(lhs.getZorder()>rhs.getZorder()) return 1;
                else return 0;
            }
        });
        LayerTexture layer1 = getLayerTexture(LAYER_CH1);
        LayerTexture layer2 = getLayerTexture(LAYER_CH2);
        if(layer1 != null && layer2 != null){
            if(layer1.getZorder() > layer2.getZorder()){
                gray2RGBAFilter[0].setTop(true);
                gray2RGBAFilter[1].setTop(false);
            }else{
                gray2RGBAFilter[0].setTop(false);
                gray2RGBAFilter[1].setTop(true);
            }
        }else{
            gray2RGBAFilter[0].setTop(true);
        }
        refreshWave();
    }

    private LayerTexture.OnFrameAvailableListener frameAvailableListener = new LayerTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(LayerTexture layerTexture) {
            if(bDraw) {
                rennderThread.run();
            }
        }
    };

    public void setLayerTextureValidListener(OnLayerTextureValidListener listener){
        mlayerTextureValidListener = listener;
    }

    private int otherCnt = 0;
    private int recvOtherCnt = 0;
    private synchronized int getOtherCnt(){
        return otherCnt;
    }
    private synchronized void upOtherCnt(){
        otherCnt = (otherCnt + 1) & 0xFFFF;
    }
    public SurfacePreview(int width, int height, Object surface,int nums){
        super(width,height,FORMAT_RGBA_8888,surface,nums);
        rennderThread.setRunnable(new Runnable() {
            @Override
            public void run() {
                upOtherCnt();
                refreshWave();
            }
        });

        handler = new SurfacePreviewHandler();
        mHandlerThread.start();

    }
    private void checkAfterglow(int i){
        if(isAfterglow(i)){
            long tmpTime = SystemClock.elapsedRealtime();
            if(tmpTime - refreshTime[i] > SurfacePreviewHandler.TIMER_MS){
                if(isRunAutoAfterflow()) {
                    autoAftrglow(i);
                }
            }
        }
    }

    class SurfacePreviewHandler extends Handler{
        private static final int MSG_AFTGRGLOW = 0x10001;
        private static final int MSG_CLEARWAVE = 0x10002;
        private static final int MSG_REFRESH_INIT = 0x10003;

        private static final int TIMER_MS = 100;
        private volatile boolean bClearWave = false;
        public void sendClearWave(){
            synchronized (this) {
                if (!hasMessages(MSG_CLEARWAVE)) {
                    sendEmptyMessageDelayed(MSG_CLEARWAVE, 100);
                    bClearWave = true;
                }
            }
        }
        public void removeClearWave(){
            synchronized (this) {
                if (bClearWave) {
                    removeMessages(MSG_CLEARWAVE);
                }
            }
        }
        public void InitRefreshWave(){
            if(!hasMessages(MSG_REFRESH_INIT)){
                sendEmptyMessageDelayed(MSG_REFRESH_INIT,50);
            }
        }
        public SurfacePreviewHandler(){
            super();
            sendEmptyMessageDelayed(MSG_AFTGRGLOW,TIMER_MS);
        }
        @Override
        public void handleMessage(Message msg) {

            if(msg.what == MSG_AFTGRGLOW){
                for(int i=LAYER_CH1;i<maxChLayer;i++){
                    checkAfterglow(i);
                }
                for(int i=LAYER_MATH1;i<maxMathLayer;i++){
                    checkAfterglow(i);
                }
                sendEmptyMessageDelayed(MSG_AFTGRGLOW,TIMER_MS);
            }else if(msg.what == MSG_CLEARWAVE){
                bClearWave = false;
                setFlag(CLEAR_WAVE);
                refreshWave();
            }else if(msg.what == MSG_REFRESH_INIT){
                if(drawCounter <= 0){
                    refreshWave();
                }
            }
        }
    }
    public synchronized void setRunAutoAfterflow(boolean bRunAutoAfterflow){
        this.bRunAutoAfterflow = bRunAutoAfterflow;

    }
    public synchronized boolean isRunAutoAfterflow(){
        return this.bRunAutoAfterflow;
    }
    public void InitDraw(){
        synchronized (this){
            if(!bDraw){
                bDraw = true;
                Log.d(TAG, "InitDraw() called");
            }
        }
    }

    @Override
    public void updateTexImage() {
        if(bDraw) {
            for (int i = LAYER_MATH1; i < maxMathLayer; i++) {
                if (layerTexture[i].isVisiable()) {
                    layerTexture[i].updateTexImage();
                }
            }
            for (int i = LAYER_REF1; i < maxRefLayer; i++) {
                if (layerTexture[i].isVisiable()) {
                    layerTexture[i].updateTexImage();
                }
            }
            if (layerTexture[LAYER_XY].isVisiable()) {
                layerTexture[LAYER_XY].updateTexImage();
            }
        }
    }

    private void InitChLayer(int width,int height){

        for(int i = LAYER_CH1;i < maxChLayer;i++){
            if(layerTexture[i] == null){
                layerTexture[i] = new LayerTexture(i);
                gray2RGBAFilter[i].setIndex(i);
                layerTexture[i].setTextureFilter(gray2RGBAFilter[i]);
                layerTexture[i].setLeft(0);
                layerTexture[i].setTop(0);
                layerTexture[i].setWidth(width);
                layerTexture[i].setHeight(height);
                producedSurfaceTexture[i].setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        handler.removeClearWave();
                        upChBmpCnt();
                        requestRender();
                    }
                },new Handler(mHandlerThread.getLooper()));
                layerTextureList.add(layerTexture[i]);
            }
            else {
                //Log.d("Tag.Debug", String.format("SurfacePreview.InitChLayer height= %d", height));
                layerTexture[i].setHeight(height);
            }
        }

    }

    private void InitStaticChLayer(int width,int height){
        for(int i = LAYER_MATH1;i <maxMathLayer;i++){
            if(layerTexture[i] == null){
                layerTexture[i] = new LayerTextureMath(width,height,i);
                layerTexture[i].prepare(mCanvas.getGlCanvas());
                layerTexture[i].setFrameAvailableListener(frameAvailableListener);
                layerTextureList.add(layerTexture[i]);
            }
            else {
                layerTexture[i].setHeight(height);
            }
        }


        for(int i=LAYER_REF1;i<maxRefLayer;i++){
            if(layerTexture[i] == null){
                layerTexture[i] = new LayerTexture(width,height,i);
                layerTexture[i].prepare(mCanvas.getGlCanvas());
                layerTexture[i].setFrameAvailableListener(frameAvailableListener);
                layerTextureList.add(layerTexture[i]);
            }
            else {
                layerTexture[i].setHeight(height);
            }
        }
    }

    private int chBmpCnt = 0;
    private synchronized void upChBmpCnt(){
        chBmpCnt = (chBmpCnt + 1) & Integer.MAX_VALUE;
    }
    public synchronized int getChBmpCnt(){
        return chBmpCnt;
    }

    private void InitXYlayer(int width, int height, int newHeight) {
        int layer = LAYER_XY;
        if (layerTexture[layer] == null) {
            layerTexture[layer] = new LayerTexture(width, height, layer);
            layerTexture[layer].prepare(mCanvas.getGlCanvas());
            layerTexture[layer].setLeft(501);
            layerTexture[layer].setTop((newHeight - XY_HEIGHT) / 2 + 1);
            layerTexture[layer].setWidth(width);
            layerTexture[layer].setHeight(height);
            layerTexture[layer].setFrameAvailableListener(frameAvailableListener);
        } else {
            layerTexture[layer].setTop((newHeight - XY_HEIGHT) / 2 + 1);
        }
    }

    private void clearAftrglowTexture(int i){
        for (int j = 0; j < 3; j++) {
            if(afterglowRawTexture[i][j] != null) {
                mCanvas.beginRenderTarget(afterglowRawTexture[i][j]);
                mCanvas.clearBuffer(0);
                mCanvas.endRenderTarget();
            }
        }
    }
    private void clearWave(){
//        Log.d(TAG, "clearWave() called");
        if(bDraw) {
            for(int i=LAYER_CH1;i<maxChLayer;i++) {
                clearAftrglowTexture(i);
                if (layerTexture[i] != null) {
                    layerTexture[i].setTexture(afterglowRawTexture[i][0]);
                    layerTexture[i].setSurfaceTexture(null);
                }
            }
            for(int i=LAYER_MATH1;i<maxMathLayer;i++){
                clearAftrglowTexture(i);
                if (layerTexture[i] != null) {
                    if (layerTexture[i].isVisiable()) {
                        layerTexture[i].clear();
                        layerTexture[i].setTexture(afterglowRawTexture[i][0]);
                    }
                }
            }
            if(layerTexture[LAYER_XY] != null) {
                if (layerTexture[LAYER_XY].isVisiable()) {
                    layerTexture[LAYER_XY].clear();
                }
            }
        }
    }
    public void forceClearWave(){
        Log.d(TAG, "forceClearWave() called");
        setFlag(CLEAR_WAVE);
        refreshWave();
    }

    private void createAfterglowTexture(int i,int width,int height){
        for (int j = 0; j < 3; j++) {
            if (afterglowRawTexture[i][j] == null) {
                afterglowRawTexture[i][j] = new RawTexture(width, height, false);
                if (!afterglowRawTexture[i][j].isLoaded()) {
                    afterglowRawTexture[i][j].prepare(mCanvas.getGlCanvas());
                }
            } else {
                afterglowRawTexture[i][j].setSize(width, height);
            }
            mCanvas.beginRenderTarget(afterglowRawTexture[i][j]);
            mCanvas.clearBuffer(0);
            mCanvas.endRenderTarget();
        }
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onSurfaceChanged(width, height);
        int rawHeight = ScopeBase.getHeight();
        for(int i=LAYER_CH1;i<maxChLayer;i++){
            createAfterglowTexture(i,width,rawHeight);
        }
        for(int i=LAYER_MATH1;i<maxMathLayer;i++){
            createAfterglowTexture(i,width,rawHeight);
        }
        InitChLayer(width,height);
        InitStaticChLayer(width,height);
        InitXYlayer(XY_WIDTH,XY_HEIGHT, height);
        if(mlayerTextureValidListener != null) {
            mlayerTextureValidListener.OnLayerTextureListener(layerTextureList);
        }

        initPixelBuffer(width,height);
        synchronized (this) {
            recvCnt = 0;
            recvOtherCnt = 0;
            Arrays.fill(drawNum, 0);
        }
        setFlag(CLEAR_WAVE | CLEAR_AFTRGLOW);
        InitDraw();
    }

    private void bindPixelBuffer() {

        GLES32.glBindBuffer(GLES32.GL_PIXEL_PACK_BUFFER, mPboIds.get(mIndex));
        GLES32.glReadPixels(0, 0, mRowStride / 4, getHeight(), GLES32.GL_RGBA, GLES32.GL_UNSIGNED_BYTE,0);
        mIndex  = (mIndex + 1) & 0x01;
        GLES32.glBindBuffer(GLES32.GL_PIXEL_PACK_BUFFER, mPboIds.get(mIndex));
        rgbaBuffer = (ByteBuffer) GLES32.glMapBufferRange(GLES32.GL_PIXEL_PACK_BUFFER, 0, mPboSize, GLES32.GL_MAP_READ_BIT);
        GLES32.glUnmapBuffer(GLES32.GL_PIXEL_PACK_BUFFER);
        GLES32.glBindBuffer(GLES32.GL_PIXEL_PACK_BUFFER, 0);

    }
    private void destroyPixelBuffers() {
        if (mPboIds != null) {
            GLES32.glDeleteBuffers(2, mPboIds);
            mPboIds = null;
        }
    }
    private void initPixelBuffer(int width, int height) {
        if (mPboIds != null) {
            destroyPixelBuffers();
        }
        if (mPboIds != null) {
            return;
        }


        final int align = 128;
        mRowStride = (width * 4 + (align - 1)) & -align;

        mPboSize = mRowStride * height;

        mPboIds = IntBuffer.allocate(2);
        GLES32.glGenBuffers(2, mPboIds);

        GLES32.glBindBuffer(GLES32.GL_PIXEL_PACK_BUFFER, mPboIds.get(0));
        GLES32.glBufferData(GLES32.GL_PIXEL_PACK_BUFFER, mPboSize, null, GLES32.GL_STATIC_READ);

        GLES32.glBindBuffer(GLES32.GL_PIXEL_PACK_BUFFER, mPboIds.get(1));
        GLES32.glBufferData(GLES32.GL_PIXEL_PACK_BUFFER, mPboSize, null, GLES32.GL_STATIC_READ);

        GLES32.glBindBuffer(GLES32.GL_PIXEL_PACK_BUFFER, 0);
    }
    private IntBuffer mPboIds;
    private int mPboSize;
    private int mRowStride;

    private  int mIndex = 0;

    private ByteBuffer rgbaBuffer;
    public ByteBuffer getRgbaByffer(){
        return rgbaBuffer;
    }
    public int getStride(){

        return mRowStride/4;
    }

    private void swap(int layer){
        idx[layer] =  (idx[layer] + 1) & 0x01;
    }
    private int getTextureId(int layer){
        return afterglowRawTexture[layer][idx[layer]].getId();
    }
    private int getTexture(int layer){
        return  GLES20.GL_TEXTURE1 + producedSurfaceTexture.length  + layer * 3 + idx[layer];
    }

    private RawTexture getRenderTargetRawTexture(int layer){
        return afterglowRawTexture[layer][(idx[layer]+1) & 0x01];
    }

    private void refreshWave(){
        if(bDraw) {
            requestRender();
        }
    }

    private int sampleCnt = 0;
    private boolean bSample = true;
    public synchronized void lockSampleCnt(){
        sampleCnt = getChBmpCnt();
    }
    public synchronized void setSample(boolean bSample){
        this.bSample = bSample;
    }
    public synchronized boolean isSample(){
        return bSample;
    }
    private volatile int clearAftrglowSync = 0;
    private volatile int []clearAftrglowMath = new int[LAYER_MATH_MAX - LAYER_MATH1];
    public void afterglowClear(){
        synchronized (this) {
            if (isAfterglow(LAYER_CH1)) {
                clearAftrglowSync = getChBmpCnt();
                setFlag(CLEAR_AFTRGLOW);
                if(!isSample()) {
                    refreshWave();
                }
            }
        }
    }
    public void afterglowMathClear(){
//        Log.d(TAG, "afterglowMathClear() called");
        synchronized (this){
            boolean bClear = false;
            for(int i=LAYER_MATH1;i<maxMathLayer;i++){
                int n = i - LAYER_MATH1;
                if(isAfterglow(i)){
                    clearAftrglowMath[n] = getLayerTexture(i).getDrawNum();

                    if(!isSample()) {
                        clearAftrglowMath[n] -= 1;
                        drawNum[n] = clearAftrglowMath[n];
                    }

                    setFlag(CLEAR_MATH_AFTRGLOW<<n);
                    bClear = true;
                }
            }
            if(bClear) {
                refreshWave();
            }
        }
    }



    private void autoAftrglow(final int layer){
        if(isAfterglow(layer)) {
            refreshWave();
        }
    }

    public void clear(){
        if(bDraw) {
            handler.sendClearWave();
        }
    }
    public void setCCTEnable(boolean bCCTEnalbe){
        synchronized (this) {
            this.bCCTEnable = bCCTEnalbe;
            for(int i=LAYER_CH1;i<maxChLayer;i++){
                if (bCCTEnable) {
                    layerTexture[i].setTextureFilter(gray2ColorTemperature[i]);
                } else {
                    layerTexture[i].setTextureFilter(gray2RGBAFilter[i]);
                }
            }
        }
        refreshWave();
    }
    private boolean isCCTEnable(){
        synchronized (this){

            return this.bCCTEnable;
        }
    }

    public void setDataListener(IDataListener dataListener){
        this.dataListener = dataListener;
    }

    public  boolean isAfterglow(int layer){
        synchronized (this) {
            return bAfterglow[layer];
        }
    }

    public void setAfterglow(int layer,boolean bAfterglow){
//        Log.d(TAG, "setAfterglow() called with: layer = [" + layer + "], bAfterglow = [" + bAfterglow + "]");

        boolean bChange = false;
        synchronized (this) {
            if (this.bAfterglow[layer] != bAfterglow) {
                this.bAfterglow[layer] = bAfterglow;
                bChange = true;
            }
        }
        if(bChange){
            if(layer  >= LAYER_CH1 && layer < maxChLayer) {
                this.afterglowClear();
            }else{
                this.afterglowMathClear();
            }
        }
    }


    public void setAfterglowTime(int layer,long ms){
        synchronized (this) {
            afterglowTime[layer] = ms;
        }
    }

    private float getAfterglowWeakenVal(int layer){
        float tmp = 0;
        synchronized (this) {
            if (afterglowTime[layer] < 0) {
                //无限余晖
                return 0.0f;
            } else if (afterglowTime[layer] == 0) {
                return 1.0f;
            }

            long nowTime = SystemClock.elapsedRealtime();
            tmp = 1.0f / afterglowTime[layer] * (nowTime - refreshTime[layer]);
            refreshTime[layer] = nowTime;
            if (tmp > 1.0f)
                tmp = 1.0f;
        }

//        Log.d(TAG,"layer:" + layer + ",tmp:" + tmp  + ",refreshTime:" + refreshTime[layer]);
        return tmp;
    }

    private void onClearAfterglow(ICanvasGL canvas,int layer){
        for (int i = 0; i < 2; i++){
            canvas.beginRenderTarget(afterglowRawTexture[layer][i]);
            canvas.clearBuffer(0);
            canvas.endRenderTarget();
        }

    }


    private void onAfterglow(ICanvasGL canvas,int i){
        float val = 0;

        val = getAfterglowWeakenVal(i);
        LayerTexture layer = getLayerTexture(i);
        RawTexture outTexture = getRenderTargetRawTexture(i);
        canvas.beginRenderTarget(outTexture);
        canvas.clearBuffer(0);
        afterglowFilter[0].setWeakenVal(val);
        afterglowFilter[0].setAfterglowId(getTextureId(i));
        afterglowFilter[0].setTexture(getTexture(i));
        canvas.drawSurfaceTexture(afterglowRawTexture[i][2],
                null, 0, 0, getWidth(), ScopeBase.getHeight(), afterglowFilter[0]);

        canvas.endRenderTarget();
        layer.setTexture(outTexture);
        layer.setSurfaceTexture(null);
        swap(i);
    }
    private void grayMap(ICanvasGL canvas, SurfaceTexture []producedSurfaceTexture, RawTexture []producedRawTexture){

//        int lId = LAYER_CH;
        for(int i = LAYER_CH1;i<maxChLayer;i++) {
            LayerTexture layer = getLayerTexture(i);
            canvas.beginRenderTarget(afterglowRawTexture[i][2]);
            canvas.clearBuffer(0);
            canvas.drawSurfaceTexture(producedRawTexture[i],producedSurfaceTexture[i],
                    0, 0, getWidth(), ScopeBase.getHeight(),basicTextureFilter);
            canvas.endRenderTarget();
            layer.setTexture(afterglowRawTexture[i][2]);
            layer.setSurfaceTexture(null);
        }
    }
    private void drawMath(ICanvasGL canvas,int i){
//        int lId = LAYER_MATH;

        LayerTexture layer = getLayerTexture(i);
        canvas.beginRenderTarget(afterglowRawTexture[i][2]);
        canvas.clearBuffer(0);
        canvas.drawSurfaceTexture(layer.getTexture(), layer.getSurfaceTexture(), 0, 0,
                getWidth(), ScopeBase.getHeight(), basicTextureFilter);
        canvas.endRenderTarget();
        layer.setTexture(afterglowRawTexture[i][2]);
    }

    @Override
    public boolean onPreDraw(ICanvasGL canvas) {
        Display display = Display.getInstance();
        if(display.isXYMode()
                && getBackgroundColor() != Color.TRANSPARENT){

            canvas.clearBuffer();
            LayerTexture layer = getLayerTexture(LAYER_XY);
            GLPaint paint = new GLPaint();
            paint.setColor(getBackgroundColor());
            canvas.drawRect(layer.left,
                    layer.top,
                    layer.left+layer.width-1,
                    layer.top+layer.height-1,
                    paint);
            return true;
        }
        return false;
    }

    private void onOverlap(){
        LayerTexture layer = getLayerTexture(LAYER_XY);
        if(layer.isVisiable()){
            layer.onDraw(mCanvas);
        }else{
            for (int i=0;i<layerTextureList.size();i++) {
                layer = layerTextureList.get(i);
                if(layer.isVisiable()){
                    layer.onDraw(mCanvas);
                }
            }
        }
    }
    private int drawCounter = -3;

    private boolean isLayerVisiable(int layer){
        LayerTexture l = getLayerTexture(layer);
        return l.isVisiable();
    }

    private volatile int []drawNum = new int[LAYER_MATH_MAX - LAYER_MATH1];

    private volatile int recvCnt = 0;

    private volatile boolean bOffset = false;


    @Override
    protected void onGLDraw(ICanvasGL canvas,
                            SurfaceTexture [] producedSurfaceTexture,
                            RawTexture [] producedRawTexture,
                            @Nullable SurfaceTexture outsideSharedSurfaceTexture,
                            @Nullable BasicTexture outsideSharedTexture) {


        try{
            if (dataListener != null && bDraw) {
                boolean b = false,bOther = false;
                int cnt = 0,xCnt;
                synchronized (this) {
                    cnt = getChBmpCnt();
                    xCnt = getOtherCnt();
                    if (isFlag(CLEAR_WAVE)) {
                        b = true;
                    }
                    if( cnt != recvCnt) {
                        if(sampleCnt > 0 && sampleCnt != cnt){
                            sampleCnt = -1;
                            b = true;
                        }
                        if(b) {
                            b = false;
                            clearFlag(CLEAR_WAVE);
                            clearWave();
                        }
                        setChOffsetZeor();
                        recvCnt = cnt;
                    }else{
                        if(recvOtherCnt != xCnt){
                            bOther = true;
                        }
                    }
                    recvOtherCnt = xCnt;

                }

                if(!b)
                {
                    boolean b1 = false;
                    boolean b2 = false;
                    grayMap(canvas, producedSurfaceTexture, producedRawTexture);
                    synchronized (this) {
                        if (isAfterglow(LAYER_CH1)) {
                            if (isFlag(CLEAR_AFTRGLOW)) {
                                if (cnt >= clearAftrglowSync || (clearAftrglowSync - cnt) > 0xFF ) {
                                    clearFlag(CLEAR_AFTRGLOW);
                                }
                                b1 = true;
                            }
                            if(isSample() || bOther) {
                                b2 = true;
                            }else if(!b1){
                                b1 = true;
                            }
                        }
                    }
                    if(b1){
                        for(int i=LAYER_CH1;i<maxChLayer;i++) {
                            onClearAfterglow(canvas, i);
                        }
//                        for(int i=LAYER_MATH1;i<maxMathLayer;i++){
//                            onClearAfterglow(canvas, i);
//                        }
                    }
                    if(b2){
                        for(int i=LAYER_CH1;i<maxChLayer;i++) {
                            onAfterglow(canvas, i);
                        }
                    }

                    for(int i=LAYER_MATH1;i<maxMathLayer;i++){
                        int m = i - LAYER_MATH1;
                        int n = getLayerTexture(i).getDrawNum();


                        int nn = 0;
                        synchronized (this) {
                            nn = drawNum[m];
                        }
                        if(nn != n){
                            drawMath(canvas,i);
                            if(isAfterglow(i)){
                                if (isFlag(CLEAR_MATH_AFTRGLOW << m)) {
                                    if (n >= clearAftrglowMath[m] || (clearAftrglowMath[m] - n) > 0xFF) {
                                        clearFlag(CLEAR_MATH_AFTRGLOW << m);
                                    } else if (!isLayerVisiable(i)) {
                                        clearFlag(CLEAR_MATH_AFTRGLOW << m);
                                    }
                                    //if(isSample()) {
                                        onClearAfterglow(canvas, i);
                                    //}
                                }
                                if(isSample() || bOther) {
                                    onAfterglow(canvas,i);
                                }
                            }
                            drawNum[m] = n;
                        }
                    }

                    if (drawCounter > 0) {
                        onOverlap();
                    } else {
                        clearWave();
                        drawCounter++;
                    }
                    if(drawCounter <= 0){
                        handler.InitRefreshWave();
                    }
                } else {
                    drawRefWave();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        bindPixelBuffer();
    }

    private void drawRefWave() {
        clearWave();
        for (int i = LAYER_REF1; i < maxRefLayer; i++) {
            LayerTexture layer = layerTextureList.get(i);
            if (layer.isVisiable()) {
                layer.onDraw(mCanvas);
            }
        }
    }

    private void releaseAfterglowTexture(int j){
        for (int i = 0; i < 3; i++) {
            if (afterglowRawTexture[j][i] != null) {
                afterglowRawTexture[j][i].recycle();
                afterglowRawTexture[j][i] = null;
            }
        }
    }
    @Override
    public void end() {
        super.end();
        for(int i = LAYER_CH1;i<maxChLayer;i++){
            releaseAfterglowTexture(i);
        }
        for(int i = LAYER_MATH1;i<maxMathLayer;i++){
            releaseAfterglowTexture(i);
        }

        for(int i=LAYER_MATH1;i<LAYER_MAX;i++){
            if(layerTexture[i] != null){
                layerTexture[i].end();
            }
            layerTexture[i] = null;
        }
        destroyPixelBuffers();
    }
}
