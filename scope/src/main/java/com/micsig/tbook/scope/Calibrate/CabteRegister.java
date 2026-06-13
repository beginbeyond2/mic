package com.micsig.tbook.scope.Calibrate;  // 定义包名：示波器校准功能模块

import android.content.Context;  // 导入Context类：Android上下文
import android.os.Handler;  // 导入Handler类：线程间消息处理
import android.os.HandlerThread;  // 导入HandlerThread类：带消息队列的后台线程
import android.os.Message;  // 导入Message类：消息封装
import android.os.SystemClock;  // 导入SystemClock类：系统时钟
import android.util.Log;  // 导入Log类：Android日志输出工具

import androidx.annotation.NonNull;  // 导入NonNull注解：非空参数注解

import com.chillingvan.canvasgl.BuildConfig;  // 导入BuildConfig类：构建配置
import com.micsig.base.DoubleUtil;  // 导入DoubleUtil类：双精度工具类
import com.micsig.base.Logger;  // 导入Logger类：基础日志工具
import com.micsig.base.Utils;  // 导入Utils类：基础工具类
import com.micsig.tbook.hardware.Hardware;  // 导入Hardware类：硬件操作接口
import com.micsig.tbook.scope.Scope;  // 导入Scope类：示波器核心管理
import com.micsig.tbook.scope.ScopeBase;  // 导入ScopeBase类：示波器基类
import com.micsig.tbook.scope.channel.Channel;  // 导入Channel类：通道管理
import com.micsig.tbook.scope.channel.ChannelFactory;  // 导入ChannelFactory类：通道工厂
import com.micsig.tbook.scope.mem.Memory;  // 导入Memory类：内存管理
import com.micsig.tbook.scope.vertical.VerticalAxis;  // 导入VerticalAxis类：垂直轴管理

import java.io.File;  // 导入File类：文件操作
import java.io.IOException;  // 导入IOException类：IO异常
import java.io.PrintWriter;  // 导入PrintWriter类：打印输出
import java.nio.ByteBuffer;  // 导入ByteBuffer类：字节缓冲区
import java.nio.ByteOrder;  // 导入ByteOrder类：字节序
import java.nio.charset.StandardCharsets;  // 导入StandardCharsets类：标准字符集
import java.text.SimpleDateFormat;  // 导入SimpleDateFormat类：日期格式化
import java.util.Arrays;  // 导入Arrays类：数组操作工具
import java.util.Date;  // 导入Date类：日期类
import java.util.List;  // 导入List接口：列表接口

/**
 * 校准寄存器管理器 - 校准数据访问与存储门面类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate（示波器校准功能模块）</li>
 *   <li>架构层级：数据访问层 - 校准数据门面</li>
 *   <li>设计模式：单例模式 + 门面模式 + 代理模式</li>
 *   <li>职责类型：校准数据访问、异步存储管理、HW代理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>封装HW硬件操作类的访问，提供统一的校准数据接口</li>
 *   <li>管理校准数据的异步存储（出厂/用户/状态）</li>
 *   <li>提供通道系数、零点、增益、电容等校准参数的读写接口</li>
 *   <li>校准系数的备份与恢复</li>
 * </ul>
 * 
 * <p><b>架构层次：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   校准数据访问架构                                                        │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │                      CabteRegister（门面类）                      │   │
 * │   │                                                                   │   │
 * │   │   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │   │
 * │   │   │ 通道系数    │  │ 零点/增益   │  │ 电容/偏移   │             │   │
 * │   │   │ get/set     │  │ get/set     │  │ get/set     │             │   │
 * │   │   │ ChannelCoef │  │ ChannelZero │  │ ChCap/AdOff │             │   │
 * │   │   └─────────────┘  └─────────────┘  └─────────────┘             │   │
 * │   │                                                                   │   │
 * │   │   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │   │
 * │   │   │ 出厂校准    │  │ 用户校准    │  │ 校准状态    │             │   │
 * │   │   │ saveFactory │  │ saveUser    │  │ saveState   │             │   │
 * │   │   │ Calibrate   │  │ Calibrate   │  │             │             │   │
 * │   │   └─────────────┘  └─────────────┘  └─────────────┘             │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                              │                                           │
 * │                              │ 代理访问                                  │
 * │                              ▼                                           │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │                         HW（硬件抽象层）                          │   │
 * │   │                                                                   │   │
 * │   │   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │   │
 * │   │   │ E2PROM      │  │ 校准系数    │  │ 序列化/     │             │   │
 * │   │   │ 读/写       │  │ 内存管理    │  │ 反序列化    │             │   │
 * │   │   └─────────────┘  └─────────────┘  └─────────────┘             │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                              │                                           │
 * │                              │ 硬件操作                                  │
 * │                              ▼                                           │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │                      Hardware（硬件驱动层）                       │   │
 * │   │                      E2PROM / FPGA / ADC                         │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>异步存储机制：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   异步存储流程                                                            │
 * │                                                                          │
 * │   调用线程                          HandlerThread（CabteRegister）       │
 * │       │                                       │                          │
 * │       │ saveFactoryCalibrateParam()           │                          │
 * │       │─────────────────────────────────────►│                          │
 * │       │  发送 SAVE_FACTORY_PARAM 消息         │                          │
 * │       │                                       │                          │
 * │       │ 返回（不阻塞）                         │ handleMessage()          │
 * │       │                                       │     │                    │
 * │       │                                       │     ▼                    │
 * │       │                                       │ saveFactoryCalibrate()   │
 * │       │                                       │ saveUserCalibrate()      │
 * │       │                                       │     │                    │
 * │       │                                       │     ▼                    │
 * │       │                                       │ hw.saveFactoryCalibration│
 * │       │                                       │ hw.saveUserCalibration   │
 * │       │                                       │     │                    │
 * │       │                                       │     ▼                    │
 * │       │                                       │ E2PROM写入完成           │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>校准数据类型：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   校准数据类型                                                            │
 * │                                                                          │
 * │   通道系数（ChannelCoef）：                                               │
 * │   ├── 索引：[通道][档位][系数索引]                                        │
 * │   ├── 类型：float                                                        │
 * │   └── 用途：电压测量的增益系数                                            │
 * │                                                                          │
 * │   通道零点（ChannelZero）：                                               │
 * │   ├── 索引：[通道][档位][PGA]                                             │
 * │   ├── 类型：float                                                        │
 * │   └── 用途：零点偏移校准                                                  │
 * │                                                                          │
 * │   通道增益（ChGain）：                                                    │
 * │   ├── 索引：[通道][阻抗][电压][模式][AD]                                  │
 * │   ├── 类型：int                                                          │
 * │   └── 用途：PGA增益校准                                                  │
 * │                                                                          │
 * │   AD偏移（AdOffset）：                                                    │
 * │   ├── 索引：[通道][模式][AD索引]                                         │
 * │   ├── 类型：int                                                          │
 * │   └── 用途：ADC偏移校准                                                  │
 * │                                                                          │
 * │   通道电容（ChCapacitance）：                                             │
 * │   ├── 索引：[通道][档位]                                                 │
 * │   ├── 类型：int                                                          │
 * │   └── 用途：电容补偿校准                                                  │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：HW（硬件抽象层，校准数据存储）</li>
 *   <li>依赖：HwConfig（硬件配置，获取HW实例）</li>
 *   <li>依赖：HandlerThread（异步存储线程）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @see HW 硬件操作抽象基类
 * @see HwConfig 硬件配置
 */
public class CabteRegister {

    /** 日志标签 */
    private static final String TAG="CabteRegister";  // 日志输出标签

    /** 硬件操作对象引用 */
    private HW hw;  // HW硬件操作实例

    /**
     * 获取HW硬件操作对象
     * 
     * @return HW实例
     */
    public HW getHw(){
        return hw;  // 返回HW实例
    }

    /**
     * 清除校准状态
     * 
     * <p>清除所有校准项目的完成状态。
     */
    public void clearCalibrationState(){
        hw.clearCalibrationState();  // 调用HW清除校准状态
    }

    /**
     * 设置校准状态
     * 
     * @param idx 校准项索引
     * @param bState true表示已完成，false表示未完成
     */
    public void setCalibrationState(int idx,boolean bState){

        hw.setCalibrationState(idx,bState);  // 调用HW设置校准状态
    }

    /**
     * 设置校准数据版本号
     * 
     * @param v 版本号
     */
    public void setVer(int v){
        hw.setVer(HW.CODE_VER);  // 设置为当前代码版本号
    }

    /**
     * 检查校准状态
     * 
     * @param idx 校准项索引
     * @return true表示已完成，false表示未完成
     */
    public boolean isCalibrationState(int idx){
        return hw.isCalibrationState(idx);  // 返回校准状态
    }

    /**
     * 保存校准状态
     * 
     * <p>将校准状态写入E2PROM。
     */
    private void saveCalibrationState(){
        hw.saveCalibrationState();  // 调用HW保存校准状态
    }

    /**
     * 检查是否完成顶部校准
     * 
     * @return true表示已完成，false表示未完成
     */
    public boolean isTopCalibration(){
        return hw.isTopCalibration();  // 返回顶部校准状态
    }

    /**
     * 获取校准项目列表
     * 
     * @return 校准项目名称列表
     */
    public List<String> getCalibrationItems(){
        return hw.getCalibrationItems();  // 返回校准项目列表
    }

    /**
     * 获取校准项状态
     * 
     * @param idx 校准项索引
     * @param sb 用于存储状态描述
     * @return true表示已完成，false表示未完成
     */
    public boolean getCalibrationItemState(int idx,StringBuilder sb){
        return hw.getCalibrationItemState(idx,sb);  // 返回校准项状态
    }

    /**
     * 异步保存校准状态参数
     * 
     * <p>发送消息到后台线程保存校准状态。
     */
    public synchronized void saveCalibrationStateParam(){

        if(!mHandler.hasMessages(SAVE_STATE_PARAM)){  // 检查是否有待处理的消息
            mHandler.sendEmptyMessage(SAVE_STATE_PARAM);  // 发送保存状态消息
        }
    }

    /**
     * 加载校准状态
     * 
     * <p>从E2PROM加载校准状态到内存。
     */
    public synchronized void loadCalibrationState(){
        hw.loadCalibrationState();  // 调用HW加载校准状态
    }

    /** 单例实例：使用volatile保证多线程可见性 */
    private static volatile CabteRegister instance = null;  // 单例实例引用


    /**
     * 获取单例实例（带Context初始化）
     * 
     * <p>使用双重检查锁定模式确保线程安全。
     * 首次调用时需要传入Context进行初始化。
     * 
     * @param context Android上下文
     * @return CabteRegister单例实例
     */
    public static CabteRegister getInstance(Context context) {
        if (instance == null) {  // 第一次检查
            synchronized (CabteRegister.class) {  // 同步锁
                if (instance == null && context != null) {  // 第二次检查
                    instance = new CabteRegister(context);  // 创建实例
                    instance.verifyAll();  // 验证所有校准数据

                }
            }
        }
        return instance;  // 返回实例
    }

    /**
     * 获取单例实例（无参数）
     * 
     * <p>需要在调用getInstance(Context)之后使用。
     * 
     * @return CabteRegister单例实例
     */
    public static CabteRegister getInstance() {
        return instance;  // 返回实例
    }

    /** Android上下文引用 */
    private Context mContext;  // Android上下文

    /** 后台处理线程：用于异步保存校准数据 */
    private HandlerThread handlerThread;  // Handler线程
    
    /** 消息处理器：处理保存消息 */
    private Handler mHandler;  // Handler消息处理器

    // ==================== 消息类型常量 ====================
    
    /** 保存出厂校准参数消息 */
    private static final int SAVE_FACTORY_PARAM = 0x1001;  // 保存出厂校准
    
    /** 保存用户校准参数消息 */
    private static final int SAVE_USER_PARAM = SAVE_FACTORY_PARAM + 1;  // 保存用户校准
    
    /** 保存校准状态参数消息 */
    private static final int SAVE_STATE_PARAM = SAVE_USER_PARAM + 1;  // 保存校准状态

    /**
     * 私有构造方法：初始化校准寄存器管理器
     * 
     * <p>加载校准数据，创建后台保存线程。
     * 
     * @param context Android上下文
     */
    private CabteRegister(Context context){

        mContext = context;  // 保存上下文引用
        hw = HwConfig.getInstance().getHW();  // 获取HW实例
        hw.loadCalibration();  // 加载校准数据
        hw.loadCalibrationState();  // 加载校准状态
        handlerThread = new HandlerThread("CabteRegister");  // 创建后台线程
        handlerThread.start();  // 启动线程
        mHandler = new Handler(handlerThread.getLooper()){  // 创建消息处理器
            @Override
            public void handleMessage(@NonNull Message msg) {  // 消息处理回调
                super.handleMessage(msg);  // 调用父类方法
                switch (msg.what){  // 根据消息类型分发
                    case SAVE_FACTORY_PARAM:  // 保存出厂校准
                        saveFactoryCalibrate();  // 保存出厂校准数据
                        saveUserCalibrate();  // 同时保存用户校准数据
                        break;  // 结束case
                    case SAVE_USER_PARAM:  // 保存用户校准
                        saveUserCalibrate();  // 保存用户校准数据
                        break;  // 结束case
                    case SAVE_STATE_PARAM:  // 保存校准状态
                        saveCalibrationState();  // 保存校准状态
                        break;  // 结束case
                }
            }
        };
    }

    // ==================== 通道系数读写接口 ====================

    /**
     * 获取通道校准系数
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引
     * @param idx 系数索引
     * @return 校准系数值
     */
    public float getChannelCoef(int chIdx,int dang,int idx){
        return hw.getChannelCoef(chIdx,dang,idx);  // 返回通道系数
    }

    /**
     * 设置通道校准系数
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引
     * @param idx 系数索引
     * @param val 系数值
     */
    public void setChannelCoef(int chIdx,int dang,int idx,float val){
        hw.setChannelCoef(chIdx,dang,idx,val);  // 设置通道系数
    }


    // ==================== 通道零点读写接口 ====================

    /**
     * 设置通道零点校准值
     * 
     * @param chIdx 通道索引
     * @param dwIdx 档位索引
     * @param pga PGA值
     * @param val 零点校准值
     */
    public synchronized void setChannelZero(int chIdx,int dwIdx,int pga,float val){
        hw.setChannelZero(chIdx,dwIdx,pga,val);  // 设置通道零点
    }

    /**
     * 获取通道零点校准值
     * 
     * @param chIdx 通道索引
     * @param dwIdx 档位索引
     * @param pga PGA值
     * @return 零点校准值
     */
    public synchronized float getChannelZero(int chIdx,int dwIdx,int pga) {
        return hw.getChannelZero(chIdx,dwIdx,pga);  // 返回通道零点
    }



    // ==================== AD偏移读写接口 ====================

    /**
     * 设置AD偏移值
     * 
     * @param chIdx 通道索引
     * @param chmode 通道模式
     * @param adIdx AD索引
     * @param val 偏移值
     */
    public synchronized void setAdOffset(int chIdx,int chmode,int adIdx,int val){
        hw.setAdOffset(chIdx,chmode,adIdx,val);  // 设置AD偏移
    }

    /**
     * 获取AD偏移值
     * 
     * @param chIdx 通道索引
     * @param chmode 通道模式
     * @param adIdx AD索引
     * @return 偏移值
     */
    public synchronized int getADOffset(int chIdx,int chmode,int adIdx){
        return hw.getAdOffset(chIdx,chmode,adIdx);  // 返回AD偏移
    }

    // ==================== 通道增益读写接口 ====================

    /**
     * 获取通道增益值
     * 
     * @param chIdx 通道索引
     * @param resistanceType 阻抗类型
     * @param vIdx 电压索引
     * @param chMode 通道模式
     * @param adIdx AD索引
     * @return 增益值
     */
    public synchronized int getChGain(int chIdx,int resistanceType,int vIdx,int chMode,int adIdx){
        return hw.getChGain(chIdx,resistanceType,vIdx,chMode,adIdx);  // 返回通道增益
    }

    /**
     * 设置通道增益值
     * 
     * @param chIdx 通道索引
     * @param resistanceType 阻抗类型
     * @param vIdx 电压索引
     * @param chMode 通道模式
     * @param adIdx AD索引
     * @param val 增益值
     */
    public synchronized void setChGain(int chIdx,int resistanceType,int vIdx,int chMode,int adIdx,int val){
        hw.setChGain(chIdx,resistanceType,vIdx,chMode,adIdx,val);  // 设置通道增益
    }


    // ==================== 通道电容读写接口 ====================

    /**
     * 获取通道电容高值
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引
     * @return 电容高值
     */
   public int getChCapacitanceHigh(int chIdx,int dang){
        return hw.getChCapacitanceHigh(chIdx,dang);  // 返回通道电容高值
   }

   /**
    * 设置通道电容高值
    * 
    * @param chIdx 通道索引
    * @param dang 档位索引
    * @param val 电容高值
    */
   public void setChCapacitanceHigh(int chIdx,int dang,int val){
        hw.setChCapacitanceHigh(chIdx,dang,val);  // 设置通道电容高值
   }

    // ==================== 校准数据保存接口 ====================

    /**
     * 异步保存出厂校准参数
     * 
     * <p>发送消息到后台线程保存出厂和用户校准数据。
     * 
     * @return true表示消息已发送
     */
    public synchronized boolean saveFactoryCalibrateParam(){
        if(mHandler.hasMessages(SAVE_FACTORY_PARAM)){  // 检查是否有待处理的消息
            mHandler.removeMessages(SAVE_FACTORY_PARAM);  // 移除旧消息
        }
        mHandler.sendEmptyMessage(SAVE_FACTORY_PARAM);  // 发送保存消息
        return true;  // 返回成功
    }


    /**
     * 同步保存出厂校准数据
     * 
     * <p>在当前线程执行保存操作。
     * 
     * @return true表示保存成功
     */
    private synchronized boolean saveFactoryCalibrate(){
        return hw.saveFactoryCalibration();  // 调用HW保存出厂校准
    }

    /**
     * 同步保存用户校准数据
     * 
     * <p>在当前线程执行保存操作。
     * 
     * @return true表示保存成功
     */
    private synchronized boolean saveUserCalibrate(){
        return hw.saveUserCalibration();  // 调用HW保存用户校准
    }


    /**
     * 异步保存用户校准参数
     * 
     * <p>发送消息到后台线程保存用户校准数据。
     * 
     * @return true表示消息已发送
     */
    public synchronized boolean saveUserCalibrateParam(){
        if(mHandler.hasMessages(SAVE_USER_PARAM)){  // 检查是否有待处理的消息
            mHandler.removeMessages(SAVE_FACTORY_PARAM);  // 移除旧消息
        }
        mHandler.sendEmptyMessage(SAVE_USER_PARAM);  // 发送保存消息
        return true;  // 返回成功
    }

    // ==================== 备份恢复接口 ====================

    /**
     * 备份校准寄存器
     * 
     * <p>将当前校准系数备份到内存，用于校准失败时恢复。
     */
    public void backUpCabteRegister() {
        hw.backUpCabteRegister();  // 调用HW备份校准系数
    }

    /**
     * 恢复校准寄存器
     * 
     * <p>从备份恢复校准系数。
     */
    public void restoreCabteRegister() {
        hw.restoreCabteRegister();  // 调用HW恢复校准系数
    }

    /**
     * 验证通道零点系数
     * 
     * @return true表示验证通过
     */
    public boolean verifyChZeroCoef() {

        return true;  // 默认返回true
    }

    /**
     * 验证所有校准数据
     */
    void verifyAll() {

    }

    /**
     * 恢复全部默认校准系数
     */
    public void rstDefaultVal(){
        hw.defaultVal();  // 调用HW恢复默认值
    }

    /**
     * 恢复指定通道档位的默认校准系数
     * 
     * @param ch 通道索引
     * @param dang 档位索引
     * @param resistanceType 阻抗类型
     */
    public void rst_coefChannel(int ch,int dang,int resistanceType){
        hw.defaultVal_coefChannel(ch,dang,resistanceType);  // 调用HW恢复默认系数
    }

    // ==================== 校准计算接口 ====================

    /**
     * 计算通道校准系数
     * 
     * @param chIdx 通道索引
     * @param scaleVal 档位值
     * @param idx 系数索引
     * @return 计算出的校准系数
     */
    public double calc_coefChannel(int chIdx,double scaleVal,int idx){
        double coef;  // 系数临时变量
        coef = hw.calc_coefChannel(chIdx,scaleVal,idx);  // 调用HW计算系数
        return coef;  // 返回系数
    }

    /**
     * 计算PGA满量程增益
     * 
     * @param chIdx 通道索引
     * @param scaleVal 档位值
     * @param result 结果数组
     */
    public void calc_pga_fs_gain(int chIdx,double scaleVal,int []result){
        hw.calc_pga_fs_gain(chIdx,scaleVal,result);  // 调用HW计算PGA增益
    }


    /**
     * 获取校准时间
     * 
     * @return 校准时间字符串
     */
    public String getCabteTime(){
        return hw.getCabteTime();  // 返回校准时间
    }


    /**
     * 获取默认通道系数（扩展版本）
     * 
     * @param chIdx 通道索引
     * @param dangwei 档位索引
     * @param pgaVal PGA值
     * @return 默认系数值
     */
    public double vol_ChannelCoef_defaultEx(int chIdx,int dangwei,int pgaVal){
        return hw.vol_ChannelCoef_defaultEx(chIdx,dangwei,pgaVal);  // 返回默认系数
    }

    /**
     * 获取默认通道系数
     * 
     * @param resistanceType 阻抗类型
     * @param dang 档位索引
     * @return 默认系数值
     */
    public double vol_ChannelCoef_default(int resistanceType,int dang){
        return hw.vol_ChannelCoef_default(resistanceType,dang);  // 返回默认系数
    }

    // ==================== 比例档位静态方法 ====================

    /**
     * 根据电压获取比例索引
     * 
     * @param resistanceType 阻抗类型
     * @param v 电压值
     * @return 比例索引
     */
    public static int getRatioIdx(int resistanceType,double v){
        return getInstance().getHw().getRatioIdx(resistanceType,v);  // 返回比例索引
    }

    /**
     * 将比例索引转换为档位索引
     * 
     * @param resistanceType 阻抗类型
     * @param idx 比例索引
     * @return 档位索引
     */
    public static int getRatioIdx2Dang(int resistanceType,int idx){
        return getInstance().getHw().getRatioIdx2Dang(resistanceType,idx);  // 返回档位索引
    }

    /**
     * 获取垂直量程
     * 
     * @param resistanceType 阻抗类型
     * @param dang 档位索引
     * @return 垂直量程值
     */
    public static double getVerticalRange(int resistanceType,int dang){
        return getInstance().getHw().getVerticalRange(resistanceType,dang);  // 返回垂直量程
    }

    /**
     * 获取比例档位数量
     * 
     * @return 档位数量
     */
    public static int getRatioDangCnt(){
        return getInstance().getHw().getRatioDangCnt();  // 返回档位数量
    }



    /**
     * 计算PGA步进
     * 
     * @param flag 标志位
     */
    public void calcPgaStep(int flag){
        hw.calcPgaSetp(flag);  // 调用HW计算PGA步进
    }

    /**
     * 计算增益
     * 
     * @param flag 标志位
     */
    public void calcGain(int flag){
        hw.calcGain(flag);  // 调用HW计算增益
    }


    // ==================== 通道幅度缓存 ====================

    /** 新输入电压缓存：用于存储各通道的新输入电压值 */
    private double [] newInputV = {-1,-1,-1,-1,-1,-1,-1,-1};  // 8通道输入电压缓存

    /** 通道幅度电压缓存：用于存储各通道的幅度电压值 */
    private double [] chAmpV = {-1,-1,-1,-1,-1,-1,-1,-1};  // 8通道幅度电压缓存

    /**
     * 设置通道幅度电压
     * 
     * @param ch 通道索引
     * @param v 幅度电压值
     */
    public synchronized void setChAmp(int ch,double v){
        if(ch >= 0 && ch < chAmpV.length) {  // 检查通道索引是否有效
            chAmpV[ch] = v;  // 设置幅度电压
        }
    }

    /**
     * 获取通道幅度电压
     * 
     * @param ch 通道索引
     * @return 幅度电压值
     */
    public synchronized double getChAmp(int ch){
        return chAmpV[ch];  // 返回幅度电压
    }

    /**
     * 设置所有通道的新输入电压（统一值）
     * 
     * @param v 输入电压值
     */
    public void setNewInputV(double v){
        synchronized (this){  // 同步锁
            Arrays.fill(newInputV,v);  // 填充所有通道
        }
    }

    /**
     * 设置所有通道的新输入电压（数组）
     * 
     * @param v 输入电压数组
     */
    public void setNewInputV(double [] v){
        synchronized (this){  // 同步锁
            if(v.length == newInputV.length){  // 检查数组长度
                System.arraycopy(v,0,newInputV,0,v.length);  // 复制数组
            }
        }
    }

    /**
     * 获取指定通道的新输入电压
     * 
     * @param ch 通道索引
     * @return 输入电压值，无效索引返回-1
     */
    public double getNewInputV(int ch){
        synchronized (this){  // 同步锁
            if(ch >=0 && ch < newInputV.length) {  // 检查通道索引是否有效
                return newInputV[ch];  // 返回输入电压
            }else{
                return -1;  // 无效索引返回-1
            }
        }
    }
}
