package com.micsig.tbook.scope;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.micsig.base.Logger;
import com.micsig.tbook.scope.Data.SyncHeader;
import com.micsig.tbook.scope.Display.Display;
import com.micsig.tbook.scope.Event.EventBase;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.Sample.MemDepthFactory;
import com.micsig.tbook.scope.Sample.Sample;
import com.micsig.tbook.scope.Sample.SegmentSample;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.SegmentedSingleBean;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.vertical.VerticalAxis;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * 示波器冻结状态管理类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope（示波器核心模块）</li>
 *   <li>架构层级：业务逻辑层 - 状态管理</li>
 *   <li>设计模式：单例模式 + 观察者模式</li>
 *   <li>职责类型：冻结状态管理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>保存示波器停止时的完整状态快照</li>
 *   <li>支持多组冻结状态的存储和切换</li>
 *   <li>支持冻结状态的序列化和反序列化</li>
 *   <li>处理采样有效事件</li>
 *   <li>管理段采样的帧信息</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>在示波器停止时保存当前状态，便于后续分析</li>
 *   <li>支持多组波形数据的存储和回放</li>
 *   <li>支持配置保存和恢复</li>
 *   <li>协调运行状态和停止状态之间的数据传递</li>
 * </ul>
 * 
 * <p><b>冻结状态说明：</b>
 * <pre>
 * 示波器运行时：
 *   - 实时更新波形数据
 *   - 参数变化立即生效
 * 
 * 示波器停止时（冻结状态）：
 *   - 保持停止时刻的波形数据
 *   - 保存当时的采样参数
 *   - 支持波形缩放、平移等操作
 *   - 参数变化不影响已采集的波形
 * </pre>
 * 
 * <p><b>多组冻结状态管理：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────┐
 * │ syncHeader（同步头）作为不同冻结状态的标识                      │
 * │                                                                │
 * │ map[syncHeader1] → ScopeFrozenBean1 (第一组冻结状态)          │
 * │ map[syncHeader2] → ScopeFrozenBean2 (第二组冻结状态)          │
 * │ map[syncHeader3] → ScopeFrozenBean3 (第三组冻结状态)          │
 * │ ...                                                            │
 * └────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>类结构图：</b>
 * <pre>
 * ScopeFrozen (单例类, 实现Observer)
 *   │
 *   ├── ScopeFrozenBean (内部类 - 冻结状态数据)
 *   │     ├── 通道采样状态
 *   │     ├── 通道位置和垂直轴配置
 *   │     ├── 存储深度和采样率
 *   │     ├── 时基和时间位置
 *   │     └── 段采样信息
 *   │
 *   ├── Map&lt;Integer, ScopeFrozenBean&gt; (多组冻结状态)
 *   │
 *   └── 事件监听
 *         ├── EVENT_SAMPLE_VALID (采样有效)
 *         ├── EVENT_SYNCHEADER_CHANGE (同步头变化)
 *         ├── EVENT_SCOPE_STATE (示波器状态)
 *         ├── EVENT_FPGA_LOAD_OK (FPGA加载完成)
 *         └── EVENT_SYNCHEADER_ERROR (同步头错误)
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：Scope（示波器核心管理）</li>
 *   <li>依赖：Sample（采样管理）</li>
 *   <li>依赖：SegmentSample（段采样管理）</li>
 *   <li>依赖：SyncHeader（同步头管理）</li>
 *   <li>依赖：ChannelFactory（通道工厂）</li>
 *   <li>依赖：HorizontalAxis（水平轴管理）</li>
 *   <li>依赖：EventFactory（事件工厂）</li>
 * </ul>
 * 
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>使用静态内部类实现线程安全的单例</li>
 *   <li>关键方法使用synchronized保护</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018-7-18
 * @see Scope 示波器核心管理类
 * @see SyncHeader 同步头管理
 * @see SegmentSample 段采样管理
 */
public class ScopeFrozen implements Observer{

    /**
     * 日志标签
     */
    private static final String TAG = "ScopeFrozen";

    /**
     * 静态内部类实现线程安全的单例
     */
    private static class ScopeFrozenHolder {
        public static final ScopeFrozen instance = new ScopeFrozen();
    }

    /**
     * 获取单例实例
     * 
     * @return ScopeFrozen单例实例
     */
    public static ScopeFrozen getInstance() {
        return ScopeFrozen.ScopeFrozenHolder.instance;
    }

    /**
     * 冻结状态数据类
     * 
     * <p><b>核心职责：</b>
     * <ul>
     *   <li>存储示波器某一时刻的完整状态快照</li>
     *   <li>包含所有通道的采样状态和配置</li>
     *   <li>包含采样参数和时基信息</li>
     *   <li>包含段采样的帧信息</li>
     * </ul>
     * 
     * <p><b>存储内容：</b>
     * <pre>
     * ┌────────────────────────────────────────────────────────────────┐
     * │ 类别              │ 数据内容                                  │
     * ├───────────────────┼───────────────────────────────────────────┤
     * │ 通道状态          │ 采样状态、开启状态、位置、垂直轴配置      │
     * │ 采样参数          │ 存储深度、采样率、存储深度档位索引        │
     * │ 时基信息          │ 时基档位ID、时间位置                      │
     * │ 显示模式          │ 滚屏模式、慢时基模式                      │
     * │ 段采样            │ 启用状态、时间戳、帧号、帧数              │
     * │ 其他              │ 同步头、有效地址、有效性标志              │
     * └───────────────────┴───────────────────────────────────────────┘
     * </pre>
     */
    public class ScopeFrozenBean{
        
        /**
         * 通道采样状态数组
         * 
         * <p><b>业务含义：</b>
         * <ul>
         *   <li>记录每个通道是否参与了采样</li>
         *   <li>索引对应通道ID</li>
         * </ul>
         */
        private boolean []chSamped = new boolean[ChannelFactory.CH_CNT];
        
        /**
         * 通道位置数组
         */
        private double [] chPos = new double[ChannelFactory.CH_CNT];
        
        /**
         * 通道位置修正值数组
         */
        private double [] chPosFix = new double[ChannelFactory.CH_CNT];
        
        /**
         * 通道垂直轴配置数组
         */
        private VerticalAxis [] chVertical = new VerticalAxis[ChannelFactory.CH_CNT];
        
        /**
         * 通道开启状态数组
         */
        private boolean[] chOpen = new boolean[ChannelFactory.CH_CNT];

        /**
         * Y轴缩放因子数组
         */
        private double [] YFactor = new double[ChannelFactory.CH_CNT];

        /**
         * 采样位置值数组
         */
        private int [] placeVal = new int[ChannelFactory.CH_CNT];

        /**
         * 存储深度
         */
        private int memDepth = 28000000;
        
        /**
         * 采样率
         */
        private double sampFre = 1000*1000*1000;
        
        /**
         * 滚屏模式标志
         */
        private boolean rool = false;
        
        /**
         * 慢时基模式标志
         */
        private boolean slowScale = false;
        
        /**
         * 有效地址
         */
        private int vaildAddr = 0;
        
        /**
         * 时基档位ID
         */
        private int timeScaleId = 0;
        
        /**
         * 时间位置
         */
        private long timePosOfView = 0;
        
        /**
         * 有效性标志
         */
        private boolean valid = false;
        
        /**
         * 存储深度档位索引
         */
        private int memDepthItemIdx = 0;
        
        /**
         * 同步头
         */
        private int syncHeader = 0;

        /**
         * 段采样启用标志
         */
        private boolean bSegmentEnable =false;
        
        /**
         * 段采样时间戳
         */
        private long segmentTimestamp = 0;
        
        /**
         * 段采样帧号
         */
        private int segmentFrameNo = 0;
        
        /**
         * 段采样帧数
         */
        private int segmentFrameNums = 0;

        /**
         * 构造函数
         * 
         * <p><b>功能说明：</b>
         * <ul>
         *   <li>初始化所有通道的垂直轴配置</li>
         *   <li>默认配置为1V档位</li>
         * </ul>
         */
        public ScopeFrozenBean(){
            for(int i=0;i<ChannelFactory.CH_CNT;i++){
                chVertical[i] = new VerticalAxis(VerticalAxis.DANG_1V,1.0,VerticalAxis.PROBE_TYPE_VOL,10);
            }
        }
        
        /**
         * 设置同步头
         * 
         * @param syncHeader 同步头值
         */
        public synchronized void setSyncHeader(int syncHeader){
            this.syncHeader = syncHeader;
        }
        
        /**
         * 获取同步头
         * 
         * @return 同步头值
         */
        public synchronized int getSyncHeader(){
            return syncHeader;
        }
        
        /**
         * 设置段采样启用状态
         * 
         * @param bEnable 启用状态
         */
        public synchronized void setSegmentEnable(boolean bEnable){
            bSegmentEnable = bEnable;
        }
        
        /**
         * 判断段采样是否启用
         * 
         * @return true表示已启用
         */
        public synchronized boolean isSegmentEnable(){

            return bSegmentEnable;
        }
        
        /**
         * 设置段采样时间戳
         * 
         * @param segmentTimestamp 时间戳
         */
        public synchronized void setSegmentTimestamp(long segmentTimestamp) {
            this.segmentTimestamp = segmentTimestamp;
        }

        /**
         * 设置段采样帧号
         * 
         * @param segmentFrameNo 帧号
         */
        public synchronized void setSegmentFrameNo(int segmentFrameNo) {
            this.segmentFrameNo = segmentFrameNo;
        }

        /**
         * 设置段采样帧数
         * 
         * @param segmentFrameNums 帧数
         */
        public synchronized void setSegmentFrameNums(int segmentFrameNums) {
            this.segmentFrameNums = segmentFrameNums;
        }

        /**
         * 获取段采样时间戳
         * 
         * @return 时间戳
         */
        public synchronized long getSegmentTimestamp(){
            return segmentTimestamp;
        }
        
        /**
         * 获取段采样帧号
         * 
         * @return 帧号
         */
        public synchronized int getSegmentFrameNo(){
            return segmentFrameNo;
        }
        
        /**
         * 获取段采样帧数
         * 
         * @return 帧数
         */
        public synchronized int getSegmentFrameNums(){
            return segmentFrameNums;
        }
        
        /**
         * 设置通道采样状态
         * 
         * @param chIdx 通道索引
         * @param bSamp 采样状态
         */
        public synchronized void setChSamped(int chIdx,boolean bSamp){
            if(ChannelFactory.isDynamicCh(chIdx)){
                chSamped[chIdx] = bSamp;
            }
        }

        /**
         * 判断通道是否参与采样
         * 
         * @param chIdx 通道索引
         * @return true表示参与了采样
         */
        public synchronized boolean isChSamped(int chIdx){
            boolean bRet = false;
            if(ChannelFactory.isDynamicCh(chIdx)){
                bRet = chSamped[chIdx];
            }
            return bRet;
        }

        /**
         * 获取通道位置
         * 
         * @param chIdx 通道索引
         * @return 位置值
         */
        public synchronized double getChPos(int chIdx) {
            if(ChannelFactory.isDynamicCh(chIdx)){
                return chPos[chIdx];
            }
            return 0;
        }

        /**
         * 设置通道位置
         * 
         * @param chIdx 通道索引
         * @param pos 位置值
         */
        public synchronized void setChPos(int chIdx,double pos) {
            if(ChannelFactory.isDynamicCh(chIdx)){
                chPos[chIdx] = pos;
            }
        }

        /**
         * 获取通道位置修正值
         * 
         * @param chIdx 通道索引
         * @return 位置修正值
         */
        public synchronized double getChPosFix(int chIdx) {
            if(ChannelFactory.isDynamicCh(chIdx)){
                return chPosFix[chIdx];
            }
            return 0;
        }

        /**
         * 设置通道位置修正值
         * 
         * @param chIdx 通道索引
         * @param posFix 位置修正值
         */
        public synchronized void setChPosFix(int chIdx,double posFix) {
            if(ChannelFactory.isDynamicCh(chIdx)){
                chPosFix[chIdx] = posFix;
            }
        }

        /**
         * 获取通道垂直轴配置
         * 
         * @param chIdx 通道索引
         * @return 垂直轴配置对象
         */
        public synchronized VerticalAxis getChVertical(int chIdx) {
            if(ChannelFactory.isDynamicCh(chIdx)){
                return chVertical[chIdx];
            }
            return null;
        }

        /**
         * 设置通道垂直轴配置
         * 
         * @param chIdx 通道索引
         * @param scaleId 档位ID
         * @param fine 微调值
         * @param probeType 探头类型
         * @param probeRate 探头比例
         */
        public synchronized void setChVertical(int chIdx,int scaleId,double fine,int probeType,double probeRate) {
            if(ChannelFactory.isDynamicCh(chIdx)){
                chVertical[chIdx].setFineScale(fine);
                chVertical[chIdx].setProbeRate(probeRate);
                chVertical[chIdx].setProbeType(probeType);
                chVertical[chIdx].setScaleId(scaleId);
            }
        }

        /**
         * 判断通道是否开启
         * 
         * @param chIdx 通道索引
         * @return true表示已开启
         */
        public synchronized boolean isChOpen(int chIdx) {
            if(ChannelFactory.isDynamicCh(chIdx)){
                return chOpen[chIdx];
            }
            return false;
        }

        /**
         * 设置通道开启状态
         * 
         * @param chIdx 通道索引
         * @param chOpen 开启状态
         */
        public synchronized void setChOpen(int chIdx,boolean chOpen) {
            if(ChannelFactory.isDynamicCh(chIdx)){
                this.chOpen[chIdx] = chOpen;
            }
        }

        /**
         * 获取存储深度
         * 
         * @return 存储深度（点）
         */
        public synchronized int getMemDepth() {
            return memDepth;
        }

        /**
         * 设置存储深度
         * 
         * @param memDepth 存储深度（点）
         */
        public synchronized void setMemDepth(int memDepth) {
            this.memDepth = memDepth;
        }

        /**
         * 获取采样率
         * 
         * @return 采样率（Hz）
         */
        public synchronized double getSampFre() {
            return sampFre;
        }

        /**
         * 设置采样率
         * 
         * @param sampFre 采样率（Hz）
         */
        public synchronized void setSampFre(double sampFre) {
            this.sampFre = sampFre;
        }

        /**
         * 判断是否为滚屏模式
         * 
         * @return true表示滚屏模式
         */
        public synchronized boolean isRool() {
            return rool;
        }

        /**
         * 设置滚屏模式标志
         * 
         * @param rool 滚屏模式标志
         */
        public synchronized void setRool(boolean rool) {
            this.rool = rool;
        }

        /**
         * 判断是否为慢时基模式
         * 
         * @return true表示慢时基模式
         */
        public synchronized boolean isSlowScale() {
            return slowScale;
        }


        /**
         * 设置慢时基模式标志
         * 
         * @param slowScale 慢时基模式标志
         */
        public synchronized void setSlowScale(boolean slowScale) {
            this.slowScale = slowScale;
        }

        /**
         * 获取有效地址
         * 
         * @return 有效地址
         */
        public synchronized int getVaildAddr() {
            return vaildAddr;
        }

        /**
         * 设置有效地址
         * 
         * <p><b>功能说明：</b>
         * <ul>
         *   <li>地址值与0x7FF进行与运算，保留低11位</li>
         * </ul>
         * 
         * @param vaildAddr 有效地址
         */
        public synchronized void setVaildAddr(int vaildAddr) {

            this.vaildAddr = vaildAddr & 0x7FF;
        }

        /**
         * 获取时基档位ID
         * 
         * @return 时基档位ID
         */
        public synchronized int getTimeScaleId() {
            return timeScaleId;
        }

        /**
         * 设置时基档位ID
         * 
         * @param timeScaleId 时基档位ID
         */
        public synchronized void setTimeScaleId(int timeScaleId) {
            this.timeScaleId = timeScaleId;
        }

        /**
         * 获取时间位置
         * 
         * @return 时间位置
         */
        public synchronized long getTimePosOfView() {
            return timePosOfView;
        }

        /**
         * 设置时间位置
         * 
         * @param timePosOfView 时间位置
         */
        public synchronized void setTimePosOfView(long timePosOfView) {
            this.timePosOfView = timePosOfView;
        }

        /**
         * 判断数据是否有效
         * 
         * @return true表示有效
         */
        public synchronized boolean isValid() {
            return valid;
        }

        /**
         * 设置有效性标志
         * 
         * @param valid 有效性标志
         */
        public synchronized void setValid(boolean valid) {

            this.valid = valid;
        }


        /**
         * 获取存储深度档位索引
         * 
         * @return 档位索引
         */
        public synchronized int getMemDepthItemIdx() {
            return memDepthItemIdx;
        }

        /**
         * 设置存储深度档位索引
         * 
         * @param memDepthItemIdx 档位索引
         */
        public synchronized void setMemDepthItemIdx(int memDepthItemIdx) {
            this.memDepthItemIdx = memDepthItemIdx;
        }

        /**
         * 设置Y轴缩放因子
         * 
         * @param chIdx 通道索引
         * @param yFactor 缩放因子
         */
        public synchronized void setYFactor(int chIdx,double yFactor){
            this.YFactor[chIdx] = yFactor;
        }
        
        /**
         * 获取Y轴缩放因子
         * 
         * @param chIdx 通道索引
         * @return 缩放因子
         */
        public synchronized double getYFactor(int chIdx){
            return this.YFactor[chIdx];
        }

        /**
         * 设置采样位置值
         * 
         * @param chIdx 通道索引
         * @param placeVal 位置值
         */
        public synchronized void setPlaceVal(int chIdx,int placeVal){
            this.placeVal[chIdx] = placeVal;
        }
        
        /**
         * 获取采样位置值
         * 
         * @param chIdx 通道索引
         * @return 位置值
         */
        public synchronized int getPlaceVal(int chIdx){
            return this.placeVal[chIdx];
        }
        
        /**
         * 重置所有数据为默认值
         * 
         * <p><b>功能说明：</b>
         * <ul>
         *   <li>清空所有通道状态</li>
         *   <li>重置采样参数为默认值</li>
         *   <li>清空段采样信息</li>
         * </ul>
         */
        public synchronized void reset(){
            for (int i = 0; i < ChannelFactory.CH_CNT; i++) {
                chSamped[i] = false;
                chPos[i] = 0;
                chPosFix[i] = 0;
                chOpen[i] = false;
                YFactor[i] = 1;
                placeVal[i] = 0;
                setChVertical(i,VerticalAxis.DANG_1V,1.0,VerticalAxis.PROBE_TYPE_VOL,10);
            }
            memDepth = 28000000;
            memDepthItemIdx = 0;
            sampFre = 1000*1000*1000;
            rool = false;
            slowScale = false;
            vaildAddr = 0;
            timeScaleId = 0;
            timePosOfView = 0;
            segmentTimestamp = 0;
            segmentFrameNo = 0;
            segmentFrameNums = 0;
            bSegmentEnable = false;
            valid = false;
        }

        /**
         * 将数据序列化为JSON格式
         * 
         * <p><b>功能说明：</b>
         * <ul>
         *   <li>将所有状态数据写入Map</li>
         *   <li>用于配置保存</li>
         * </ul>
         * 
         * @param tmpmap 目标Map
         */
        public void toJson(Map<String,String> tmpmap){

            for (int i = 0; i < ChannelFactory.CH_CNT; i++) {
                tmpmap.put("chSamped_" + i, String.valueOf(chSamped[i]));
                tmpmap.put("chPos_" + i, String.valueOf(chPos[i]));
                tmpmap.put("chPosFix_" + i, String.valueOf(chPosFix[i]));
                tmpmap.put("chOpen_" + i, String.valueOf(chOpen[i]));
                tmpmap.put("YFactor_" + i, String.valueOf(YFactor[i]));
                tmpmap.put("placeVal_" + i, String.valueOf(placeVal[i]));
                tmpmap.put("Vertical_FineScale_" + i, String.valueOf(chVertical[i].getFineScale()));
                tmpmap.put("Vertical_ProbeRate_" + i, String.valueOf(chVertical[i].getProbeRate()));
                tmpmap.put("Vertical_ProbeType_" + i, String.valueOf(chVertical[i].getProbeType()));
                tmpmap.put("Vertical_ScaleId_" + i, String.valueOf(chVertical[i].getScaleId()));
            }
            tmpmap.put("syncHeader",String.valueOf(syncHeader));
            tmpmap.put("memDepth", String.valueOf(memDepth));
            tmpmap.put("memDepthItemIdx", String.valueOf(memDepthItemIdx));
            tmpmap.put("sampFre", String.valueOf(sampFre));
            tmpmap.put("rool", String.valueOf(rool));
            tmpmap.put("slowScale", String.valueOf(slowScale));
            tmpmap.put("vaildAddr", String.valueOf(vaildAddr));
            tmpmap.put("timeScaleId", String.valueOf(timeScaleId));
            tmpmap.put("timePosOfView", String.valueOf(timePosOfView));
            tmpmap.put("segmentTimestamp", String.valueOf(segmentTimestamp));
            tmpmap.put("segmentFrameNo", String.valueOf(segmentFrameNo));
            tmpmap.put("segmentFrameNums", String.valueOf(segmentFrameNums));
            tmpmap.put("bSegmentEnable", String.valueOf(bSegmentEnable));
            tmpmap.put("valid", String.valueOf(valid));
        }

        /**
         * 从JSON格式反序列化数据
         * 
         * <p><b>功能说明：</b>
         * <ul>
         *   <li>从Map读取所有状态数据</li>
         *   <li>用于配置恢复</li>
         * </ul>
         * 
         * @param tmpmap 源Map
         */
        public void fromJson( HashMap<String,String> tmpmap){
            for (int i = 0; i < ChannelFactory.CH_CNT; i++) {
                chSamped[i] = getBoolean(tmpmap,"chSamped_" + i);
                chPos[i] = getDouble(tmpmap,"chPos_" + i);
                chPosFix[i] = getDouble(tmpmap,"chPosFix_" + i);
                chOpen[i] = getBoolean(tmpmap,"chOpen_" + i);
                YFactor[i] = getDouble(tmpmap,"YFactor_" + i);;
                placeVal[i] = getInteger(tmpmap,"placeVal_" + i);
                setChVertical(i,
                        getInteger(tmpmap,"Vertical_ScaleId_" + i),
                        getDouble(tmpmap,"Vertical_FineScale_" + i),
                        getInteger(tmpmap,"Vertical_ProbeType_" + i),
                        getDouble(tmpmap,"Vertical_ProbeRate_" + i));
            }
            memDepth = getInteger(tmpmap,"memDepth");
            memDepthItemIdx = getInteger(tmpmap,"memDepthItemIdx");
            sampFre = getDouble(tmpmap,"sampFre");
            rool = getBoolean(tmpmap,"rool");
            slowScale = getBoolean(tmpmap,"slowScale");
            vaildAddr = getInteger(tmpmap,"vaildAddr");
            timeScaleId = getInteger(tmpmap,"timeScaleId");
            timePosOfView = getLong(tmpmap,"timePosOfView");
            segmentTimestamp = getLong(tmpmap,"segmentTimestamp");
            segmentFrameNo = getInteger(tmpmap,"segmentFrameNo");
            segmentFrameNums = getInteger(tmpmap,"segmentFrameNums");
            bSegmentEnable = getBoolean(tmpmap,"bSegmentEnable");
            valid = getBoolean(tmpmap,"valid");
        }



        /**
         * 转换为字符串表示
         * 
         * @return 字符串表示
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ScopeFrozenBean{");
            for (int i = 0; i < ChannelFactory.CH_CNT; i++) {
                sb.append("ch" + i + ":{");
                sb.append("chSamped=" + isChSamped(i));
                sb.append(",chOpen=" + isChOpen(i));
                sb.append(",chPos=" + getChPos(i));
                sb.append(",chPosFix=" + getChPosFix(i));
                sb.append("}");
            }
            sb.append(", memDepth=" + getMemDepth());
            sb.append(", sampFre=" + getSampFre());
            sb.append(", rool=" + isRool());
            sb.append(", slowScale=" + isSlowScale());
            sb.append(", vaildAddr=" + getVaildAddr());
            sb.append(", timeScaleId=" + getTimeScaleId());
            sb.append(", timePosOfView=" + getTimePosOfView());
            sb.append(", valid=" + isValid());
            sb.append(", memDepthItemIdx=" + getMemDepthItemIdx());
            sb.append(", syncHeader=" + getSyncHeader());
            sb.append(", bSegmentEnable=" + isSegmentEnable());
            sb.append(", segmentTimestamp=" + getSegmentTimestamp());
            sb.append(", segmentFrameNo=" + getSegmentFrameNo());
            sb.append(", segmentFrameNums=" + getSegmentFrameNums());
            sb.append("}");
            return sb.toString();
        }
    }


    /**
     * 从Map获取布尔值
     */
    private boolean getBoolean(HashMap<String,String> map,String key){
        return getBoolean(map,key,false);
    }
    
    /**
     * 从Map获取布尔值（带默认值）
     */
    private boolean getBoolean(HashMap<String,String> map,String key,boolean def){
        String s = map.get(key);
        if(s != null && s.length() > 0){
            return Boolean.parseBoolean(s);
        }
        return def;
    }
    
    /**
     * 从Map获取双精度值
     */
    private double getDouble(HashMap<String,String> map,String key){
        return getDouble(map,key,0);
    }
    
    /**
     * 从Map获取双精度值（带默认值）
     */
    private double getDouble(HashMap<String,String> map,String key,double def){
        String s = map.get(key);
        if(s != null && s.length() > 0){
            try {
                return Double.parseDouble(s);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return def;
    }
    
    /**
     * 从Map获取整数值
     */
    private int getInteger(HashMap<String,String> map,String key){
        return getInteger(map,key,0);
    }
    
    /**
     * 从Map获取整数值（带默认值）
     */
    private int getInteger(HashMap<String,String> map,String key,int def){
        String s = map.get(key);
        if(s != null && s.length() > 0){
            try {
                return Integer.parseInt(s);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return def;
    }
    
    /**
     * 从Map获取长整数值
     */
    private long getLong(HashMap<String,String> map,String key){
        return getLong(map,key,0);
    }
    
    /**
     * 从Map获取长整数值（带默认值）
     */
    private long getLong(HashMap<String,String> map,String key,long def){
        String s = map.get(key);
        if(s != null && s.length() > 0){
            try {
                return Long.parseLong(s);
            }catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        return def;
    }
    
    /**
     * Gson实例
     */
    private static final Gson gson = new Gson();
    
    /**
     * Map类型定义
     */
    private static final Type mapType = new TypeToken<HashMap<String,String>>(){}.getType();
    
    /**
     * List类型定义
     */
    private static final Type listType = new TypeToken<List<Long>>(){}.getType();
    
    /**
     * 将冻结状态序列化为JSON字符串
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将当前冻结状态转换为JSON格式</li>
     *   <li>包含段采样帧时间戳列表</li>
     * </ul>
     * 
     * @return JSON字符串
     */
    public synchronized String toJson(){
        Map<String,String> tmpmap = new HashMap<>();
        tmpmap.put("bSegmentEnableFrameNo",String.valueOf(bSegmentEnableFrameNo));
        getScopeBean().toJson(tmpmap);
        Scope scope = Scope.getInstance();
        List<SegmentedSingleBean> list = scope.getAllFrameTimestamp();
        List<Long> lls = new ArrayList<>();
        for (SegmentedSingleBean sbean:list) {
            lls.add(sbean.getTimestamp());
        }

        tmpmap.put("frameTimestamp",gson.toJson(lls,listType));

        return gson.toJson(tmpmap,mapType);
    }

    /**
     * MSS同步头
     */
    private int mssSyncHeader = -1;
    
    /**
     * 从JSON字符串反序列化冻结状态
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>从JSON格式恢复冻结状态</li>
     *   <li>恢复段采样帧时间戳列表</li>
     * </ul>
     * 
     * @param json JSON字符串
     */
    public synchronized void fromJson(String json){
        HashMap<String,String> tmpmap = gson.fromJson(json,mapType);
        int sh = getInteger(tmpmap,"syncHeader");
        mssSyncHeader = 0x20251015;
        bSegmentEnableFrameNo = getBoolean(tmpmap,"bSegmentEnableFrameNo");
        ScopeFrozenBean bean = allocBean(sh);
        bean.fromJson(tmpmap);

        map.forEach((key,value)->{
            value.fromJson(tmpmap);
        });

        Scope scope = Scope.getInstance();
        scope.clearSegmentTimestamp();
        String str = tmpmap.get("frameTimestamp");
        if(str != null
                && str.length() > 0){
            List<Long> list = gson.fromJson(str,listType);
            for (Long t:list) {
                scope.addSegmentTimestamp(t);
            }
        }
    }

    /**
     * 冻结状态Map
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>Key：同步头（标识不同的冻结状态）</li>
     *   <li>Value：冻结状态数据</li>
     * </ul>
     */
    private Map<Integer,ScopeFrozenBean > map = new HashMap<>();

    /**
     * 段采样帧号启用标志
     */
    private volatile boolean bSegmentEnableFrameNo = false;
    
    /**
     * 判断段采样帧号是否启用
     */
    private synchronized boolean isSegmentEnableFrameNo(){
        return bSegmentEnableFrameNo;
    }
    
    /**
     * 设置段采样帧号启用标志
     */
    private synchronized void setSegmentEnableFrameNo(boolean bSegmentEnableFrameNo){
        this.bSegmentEnableFrameNo = bSegmentEnableFrameNo;
    }

    /**
     * 私有构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>创建默认的冻结状态Bean</li>
     *   <li>注册事件监听</li>
     * </ul>
     */
    private ScopeFrozen(){
        ScopeFrozenBean bean = new ScopeFrozenBean();
        bean.setSyncHeader(syncHeader);
        map.put(syncHeader,bean);

        EventFactory.addEventObserver(EventFactory.EVENT_SAMPLE_VALID,this);
        EventFactory.addEventObserver(EventFactory.EVENT_SYNCHEADER_CHANGE,this);
        EventFactory.addEventObserver(EventFactory.EVENT_SCOPE_STATE,this);
        EventFactory.addEventObserver(EventFactory.EVENT_FPGA_LOAD_OK,this);
        EventFactory.addEventObserver(EventFactory.EVENT_SYNCHEADER_ERROR,this);
    }

    /**
     * 当前同步头
     */
    private int syncHeader = 0;


    /**
     * 分配新的冻结状态Bean
     * 
     * @param syncHeader 同步头
     * @return 新创建的冻结状态Bean
     */
    private ScopeFrozenBean allocBean(int syncHeader){
        this.syncHeader = syncHeader;
        ScopeFrozenBean bean = new ScopeFrozenBean();
        bean.setSyncHeader(syncHeader);
        map.put(syncHeader,bean);

        return bean;
    }
    
    /**
     * 设置当前同步头
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>切换到指定同步头对应的冻结状态</li>
     *   <li>如果不存在则创建新的</li>
     * </ul>
     * 
     * @param syncHeader 同步头
     */
    public synchronized void setSyncHeader(int syncHeader){
        ScopeFrozenBean bean = map.get(syncHeader);
        if(bean == null){
            bean = allocBean(syncHeader);
        }
        doFrozenEx(true,bean);
        Log.d(TAG, "setSyncHeader: " + syncHeader);
    }
    
    /**
     * 获取当前冻结状态Bean
     * 
     * @return 当前冻结状态Bean
     */
    private synchronized ScopeFrozenBean getScopeBean(){
        return getScopeBean(syncHeader);
    }
    
    /**
     * 获取指定同步头的冻结状态Bean
     * 
     * @param syncHeader 同步头
     * @return 冻结状态Bean
     */
    public synchronized ScopeFrozenBean getScopeBean(int syncHeader){
        ScopeFrozenBean bean = map.get(syncHeader);
        if(bean == null){
            Log.e(TAG,"getScopeBean : " + syncHeader);
            bean = allocBean(syncHeader);
            doFrozenEx(true,bean);
        }else{
            this.syncHeader = syncHeader;
        }
        return bean;
    }

    /**
     * 判断段采样是否启用
     * 
     * @return true表示已启用
     */
    public boolean isSegmentEnable(){
        return getScopeBean().isSegmentEnable();
    }


    /**
     * 设置段采样帧数
     * 
     * @param segmentFrameNums 帧数
     */
    public void setSegmentFrameNums(int segmentFrameNums) {
        getScopeBean().setSegmentFrameNums(segmentFrameNums);
    }

    /**
     * 获取同步头
     * 
     * @return 同步头
     */
    public int getSyncHeader(){
        return getScopeBean().getSyncHeader();
    }
    
    /**
     * 获取段采样时间戳
     * 
     * @return 时间戳
     */
    public long getSegmentTimestamp(){
        return getScopeBean().getSegmentTimestamp();
    }
    
    /**
     * 获取段采样帧号
     * 
     * @return 帧号
     */
    public int getSegmentFrameNo(){
        return getScopeBean().getSegmentFrameNo();
    }
    
    /**
     * 获取段采样帧数
     * 
     * @return 帧数
     */
    public int getSegmentFrameNums(){
        return getScopeBean().getSegmentFrameNums();
    }
    
    /**
     * 判断通道是否参与采样
     * 
     * @param chIdx 通道索引
     * @return true表示参与了采样
     */
    public boolean isChSamped(int chIdx){
        boolean bRet = false;
        if(ChannelFactory.isDynamicCh(chIdx)){
            bRet = getScopeBean().isChSamped(chIdx);
        }
        return bRet;
    }

    /**
     * 获取通道位置
     */
    public double getChPos(int chIdx) {
        if(ChannelFactory.isDynamicCh(chIdx)){
            return getScopeBean().getChPos(chIdx);
        }
        return 0;
    }


    /**
     * 获取通道位置修正值
     */
    public double getChPosFix(int chIdx) {
        if(ChannelFactory.isDynamicCh(chIdx)){
            return getScopeBean().getChPosFix(chIdx);
        }
        return 0;
    }



    /**
     * 获取通道垂直轴配置
     */
    public VerticalAxis getChVertical(int chIdx) {
        if(ChannelFactory.isDynamicCh(chIdx)){
            return getScopeBean().getChVertical(chIdx);
        }
        return null;
    }



    /**
     * 判断通道是否开启
     */
    public boolean isChOpen(int chIdx) {
        if(ChannelFactory.isDynamicCh(chIdx)){
            return getScopeBean().isChOpen(chIdx);
        }
        return false;
    }

    /**
     * 获取存储深度
     */
    public int getMemDepth() {
        return getScopeBean().getMemDepth();
    }


    /**
     * 获取采样率
     */
    public double getSampFre() {
        return getScopeBean().getSampFre();
    }


    /**
     * 判断是否为滚屏模式
     */
    public boolean isRool() {
        return getScopeBean().isRool();
    }

    /**
     * 判断是否为慢时基模式
     */
    public boolean isSlowScale() {
        return getScopeBean().isSlowScale();
    }


    /**
     * 获取有效地址
     */
    public int getVaildAddr() {
        return getScopeBean().getVaildAddr();
    }

    /**
     * 获取时基档位ID
     */
    public int getTimeScaleId() {
        return getScopeBean().getTimeScaleId();
    }



    /**
     * 获取时间位置
     */
    public long getTimePosOfView() {
        return getScopeBean().getTimePosOfView();
    }


    /**
     * 判断数据是否有效
     */
    public boolean isValid() {
        return getScopeBean().isValid();
    }


    /**
     * 获取存储深度档位索引
     */
    public int getMemDepthItemIdx() {
        return getScopeBean().getMemDepthItemIdx();
    }


    /**
     * 获取Y轴缩放因子
     */
    public double getYFactor(int chIdx){
        return getScopeBean().getYFactor(chIdx);
    }

    /**
     * 设置采样位置值
     */
    public void setPlaceVal(int chIdx,int placeVal){
        getScopeBean().setPlaceVal(chIdx,placeVal);
    }
    
    /**
     * 获取采样位置值
     */
    public int getPlaceVal(int chIdx){
        return getScopeBean().getPlaceVal(chIdx);
    }


    /**
     * 执行冻结操作（带参数）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>更新冻结状态的有效性</li>
     *   <li>更新段采样启用状态</li>
     *   <li>处理MSS同步头补丁</li>
     * </ul>
     * 
     * @param bean 冻结状态Bean
     * @param val 参数数组[同步头, 有效地址]
     */
    public void doFrozenEx(ScopeFrozenBean bean,int [] val){
        bean.setValid(bean.getSyncHeader() == val[0]);
        bean.setSegmentEnable(SegmentSample.getInstance().isSegmentEnable());
        if(mssSyncHeader == 0x20251015){
            mssSyncHeader = val[0];
        }
        if(mssSyncHeader != val[0]) {
            bean.setVaildAddr(val[1]);
        }
    }

    /**
     * 执行冻结操作（内部方法）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>运行状态：保存所有通道状态和采样参数</li>
     *   <li>停止状态：仅更新段采样信息</li>
     * </ul>
     * 
     * <p><b>运行状态下保存的数据：</b>
     * <ul>
     *   <li>通道采样状态、开启状态、位置、垂直轴配置</li>
     *   <li>采样率、存储深度、存储深度档位</li>
     *   <li>时基档位、时间位置</li>
     *   <li>滚屏模式、慢时基模式</li>
     *   <li>段采样信息</li>
     * </ul>
     * 
     * @param bRun 是否为运行状态
     * @param bean 冻结状态Bean
     */
    private void doFrozenEx(boolean bRun,ScopeFrozenBean bean){

        Scope scope = Scope.getInstance();
        SyncHeader syncHeader = SyncHeader.getInstance(0);
        if(bRun) {
            mssSyncHeader = -1;
            ChannelFactory.forEachCh(channel -> {
                int i = channel.getChId();
                bean.setChSamped(i, scope.isChannelInSample(i,bRun));
                bean.setChOpen(i, ChannelFactory.isChOpen(i));
                bean.setChPos(i, channel.getPos());
                bean.setChPosFix(i, channel.getPosFix());
                bean.setChVertical(i, channel.getVScaleId()
                        , channel.getFineScale()
                        , channel.getProbeType(), channel.getProbeRate());
                bean.setYFactor(i,channel.getYFactor());
                bean.setPlaceVal(i,channel.getPlaceVal());
            });

            scope.clearSegmentTimestamp();
            bean.setSampFre(scope.getSampleRate(bRun));
            bean.setMemDepthItemIdx(MemDepthFactory.getMemDepth().getMemDepthItem());
            bean.setMemDepth(MemDepthFactory.getSampleMemDepth());
            bean.setTimeScaleId(HorizontalAxis.getInstance().getTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD));
            bean.setTimePosOfView(HorizontalAxis.getInstance().getTimePosOfView(HorizontalAxis.WPI_STANDARD));
            bean.setRool(scope.isInScrollMode());
            bean.setSlowScale(scope.isInSlowScaleMode());
            bean.setVaildAddr(0);
            bean.setSegmentEnable(SegmentSample.getInstance().isSegmentEnable());
            bean.setSegmentFrameNo(syncHeader.getSegmentFrameNums()-1);
            setSegmentFrameNums(0);
            bean.setSegmentTimestamp(0);
            SegmentSample.getInstance().setSegmentFrames(0);
            setSegmentEnableFrameNo(false);
        }else{
            bean.setSegmentEnable(SegmentSample.getInstance().isSegmentEnable());
            bean.setSegmentTimestamp(syncHeader.getSegmentTimestamp());
        }
    }

    /**
     * 执行冻结操作（根据同步头）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据同步头获取对应的冻结状态</li>
     *   <li>更新有效性标志</li>
     *   <li>如果无效则重新保存状态</li>
     * </ul>
     * 
     * @param header 同步头对象
     */
    private synchronized void doFrozen(SyncHeader header){

        int val = header.getHeader();
        ScopeFrozenBean bean = map.get(header.getHeader());

        if(bean != null){
            map.clear();
            bean.setValid(bean.getSyncHeader() == val);
            doFrozenEx(!bean.isValid(),bean);
            map.put(val,bean);
        }
    }
    
    /**
     * 计算帧号
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>段采样启用时计算当前帧号</li>
     *   <li>首次调用时设置帧号为最后一帧</li>
     * </ul>
     * 
     * @param framenums 帧数
     */
    public void calcFrameNo(int framenums){
        ScopeFrozenBean bean = getScopeBean();
        if(bean.isSegmentEnable()) {
            SegmentSample segmentSample = SegmentSample.getInstance();
            if (!isSegmentEnableFrameNo()) {
                int frameNo = framenums - 1;
                setSegmentEnableFrameNo(true);
                bean.setSegmentFrameNo(frameNo);
                Scope scope = Scope.getInstance();
                segmentSample.setFrameNo(frameNo, scope.isZoom() && scope.isInSlowScaleMode() && scope.isSegmentEnable());
            }
        }
    }
    
    /**
     * 事件更新回调
     * 
     * <p><b>处理事件：</b>
     * <ul>
     *   <li>EVENT_SAMPLE_VALID：采样有效事件，执行冻结操作</li>
     *   <li>EVENT_SYNCHEADER_ERROR：同步头错误事件</li>
     *   <li>EVENT_SCOPE_STATE：示波器状态变化事件</li>
     *   <li>EVENT_FPGA_LOAD_OK：FPGA加载完成事件</li>
     * </ul>
     * 
     * @param observable 被观察对象
     * @param data 事件数据
     */
    @Override
    public void update(Observable observable, Object data) {
        EventBase eventBase = (EventBase)data;
        if(eventBase != null){


            Scope scope = Scope.getInstance();

            if(eventBase.getId() == EventFactory.EVENT_SAMPLE_VALID){
                boolean [] bData = (boolean[])eventBase.getData();
                int idx = 0;
                if(bData != null){
                    idx = bData[1] ? 0 : 1;
                }
                SyncHeader header = SyncHeader.getInstance(idx);

                if(header.isValid()) {
                    Sample sample = Sample.getInstance();
                    boolean bGetSegmentTimestamp = false;

                    if (header.isSample()) {
                        doFrozen(header);
                        if(header.isLastSample()){
                            bGetSegmentTimestamp = true;
                        }else{
                            scope.sendSampleState(Sample.SAMPLE_TRANSIENT);
                        }
                    } else {
                        if(sample.isTransient()){
                            scope.sendSampleState(Sample.SAMPLE_TRANSIENT_DIAPLAY);
                        }
                        if (header.isLastSample()) {
                            if (scope.isSingle()) {
                                doFrozen(header);
                                if (scope.isRun()) {
                                    scope.setRun(false);
                                }
                            }
                        }
                        bGetSegmentTimestamp = true;
                        if(getScopeBean().isSegmentEnable()){
                            SegmentSample segmentSample = SegmentSample.getInstance();
                            int frameNo = 0;
                            if(isSegmentEnableFrameNo()) {
                                frameNo = header.getFrameNo();
                                frameNo -= 1;

                                if (segmentSample.getSegmentDisplayType() == SegmentSample.SEGMENT_DISPLAY_FITTING) {
                                    frameNo += segmentSample.getFittingBegingFrame();
                                } else {
                                    frameNo += segmentSample.getFrameNo();
                                }
                                getScopeBean().setSegmentFrameNo(frameNo);
                            }
                        }
                    }
                    if(bGetSegmentTimestamp){

                        if (getScopeBean().isSegmentEnable() && getScopeBean().getSegmentFrameNums() == 0) {
                            if (!header.isSegmentTimestamp()) {
                                setSegmentEnableFrameNo(false);
                                scope.segmentTimTimestamp();
                            }
                        }
                    }

                }else{
                    Logger.d(TAG,"header no valid");
                }

            }else if(eventBase.getId() == EventFactory.EVENT_SYNCHEADER_ERROR){
                Display display = Display.getInstance();
                display.setDrawType(display.getDrawType());
            }else if(eventBase.getId() == EventFactory.EVENT_SCOPE_STATE){

            }else if(eventBase.getId() == EventFactory.EVENT_FPGA_LOAD_OK){

            }
        }
    }

}
