package com.micsig.tbook.tbookscope.tools;  // 存储管理模块所在包

import android.content.Intent;  // 用于发送媒体扫描广播
import android.net.Uri;  // 用于构造文件URI
import android.os.Handler;  // 用于线程间消息通信
import android.os.Message;  // 用于传递Handler消息
import android.util.Log;  // 用于日志输出

import androidx.annotation.IntRange;  // 整型范围注解

import com.micsig.base.Logger;  // 自定义日志工具
import com.micsig.base.Utils;  // 通用工具类（磁盘空间检测等）
import com.micsig.tbook.scope.Data.SaveBin;  // Bin格式保存数据类
import com.micsig.tbook.scope.Data.SaveCsv;  // CSV格式保存数据类
import com.micsig.tbook.scope.Data.SaveRecoverySession;  // 会话恢复保存数据类
import com.micsig.tbook.scope.Data.WaveData;  // 波形数据类
import com.micsig.tbook.scope.Event.EventBase;  // 事件基类
import com.micsig.tbook.scope.Event.EventFactory;  // 事件工厂类
import com.micsig.tbook.scope.Scope;  // 示波器核心类
import com.micsig.tbook.scope.ScopeBase;  // 示波器基础参数类
import com.micsig.tbook.scope.ScopeFrozen;  // 示波器冻结状态类
import com.micsig.tbook.scope.channel.BaseChannel;  // 通道基类
import com.micsig.tbook.scope.channel.Channel;  // 动态通道类
import com.micsig.tbook.scope.channel.ChannelFactory;  // 通道工厂类
import com.micsig.tbook.scope.channel.IChannel;  // 通道接口
import com.micsig.tbook.scope.channel.RefChannel;  // 参考通道类
import com.micsig.tbook.scope.mem.Memory;  // 内存同步工具
import com.micsig.tbook.tbookscope.GlobalVar;  // 全局变量
import com.micsig.tbook.tbookscope.MainActivity;  // 主Activity
import com.micsig.tbook.tbookscope.R;  // 资源ID
import com.micsig.tbook.tbookscope.main.maincenter.serialsword.ISerialsWord;  // 串行总线字接口
import com.micsig.tbook.tbookscope.rightslipmenu.RightLayoutRef;  // 参考波形右侧菜单
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;  // 串行总线右侧菜单
import com.micsig.tbook.tbookscope.util.CacheUtil;  // 缓存工具类
import com.micsig.tbook.tbookscope.wavezone.trigger.TriggerTimebase;  // 触发时基类
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;  // 串行总线管理类
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;  // 波形管理类
import com.micsig.tbook.tbookscope.wavezone.wave.wavedata.SerialBusTxtStruct;  // 串行总线文本结构
import com.micsig.tbook.ui.util.TBookUtil;  // UI工具类
import com.micsig.tbook.ui.wavezone.TChan;  // 通道UI映射工具

import java.io.BufferedInputStream;  // 缓冲输入流
import java.io.BufferedOutputStream;  // 缓冲输出流
import java.io.File;  // 文件操作类
import java.io.FileInputStream;  // 文件输入流
import java.io.FileNotFoundException;  // 文件未找到异常
import java.io.FileOutputStream;  // 文件输出流
import java.io.IOException;  // IO异常
import java.io.ObjectInputStream;  // 对象输入流（反序列化）
import java.io.ObjectOutputStream;  // 对象输出流（序列化）
import java.io.OptionalDataException;  // 可选数据异常
import java.io.StreamCorruptedException;  // 流损坏异常
import java.text.SimpleDateFormat;  // 日期格式化
import java.util.ArrayList;  // 动态数组
import java.util.Arrays;  // 数组工具
import java.util.Date;  // 日期类
import java.util.HashMap;  // 哈希映射
import java.util.Iterator;  // 迭代器
import java.util.List;  // 列表接口
import java.util.concurrent.ExecutorService;  // 线程池服务接口
import java.util.concurrent.LinkedBlockingQueue;  // 链表阻塞队列
import java.util.concurrent.ThreadPoolExecutor;  // 线程池执行器
import java.util.concurrent.TimeUnit;  // 时间单位

/**
 * Created by liwb on 2018/5/28.
 * <pre class="prettyprint">
 * SaveManage:是一个存储文件管理类，是一个单例类
 * 保存失败的几个原因：
 * 1.文件已存在
 * 2.mainActivity未创建
 * <p>
 * <p>
 * </pre>
 */
/*
 * +=============================================================================+
 * |                           SaveManage 存储管理器                              |
 * +=============================================================================+
 * | 模块定位: 示波器波形与设置文件的统一存储管理核心类                              |
 * +-----------------------------------------------------------------------------+
 * | 核心职责:                                                                    |
 * |   1. 管理波形/CSV/Bin/参考波形/串行总线解码等多种文件格式的保存与加载          |
 * |   2. 管理用户设置的序列化存储与反序列化恢复                                    |
 * |   3. 通过线程池异步执行IO操作，避免阻塞UI线程                                  |
 * |   4. 通过Handler机制将保存结果回调到UI线程                                     |
 * |   5. 生成唯一文件名并检查文件名冲突                                            |
 * +-----------------------------------------------------------------------------+
 * | 架构设计:                                                                    |
 * |   - 单例模式（静态内部类持有），保证全局唯一实例                                |
 * |   - 双线程池：单线程池(mSingleThreadExecutor)保证保存顺序；                    |
 * |                多线程池(mMultiThreadExecutor)用于并发保存                      |
 * |   - SaveSetRunnable：封装所有保存/加载操作的工作线程Runnable                   |
 * |   - UIHandler：将保存结果从工作线程转发到主线程                                |
 * |   - SaveCallBack：保存结果回调接口                                            |
 * +-----------------------------------------------------------------------------+
 * | 数据流向:                                                                    |
 * |   保存: 调用方 → saveXxx() → 设置Runnable参数 → 线程池执行 → IO写入          |
 * |         → Handler通知UI → SaveCallBack回调                                    |
 * |   加载: 调用方 → loadXxx() → 设置Runnable参数 → 线程池执行 → IO读取          |
 * |         → HashMap回填 → CacheUtil同步                                        |
 * +-----------------------------------------------------------------------------+
 * | 依赖关系:                                                                    |
 * |   - ChannelFactory: 获取通道实例（动态/数学/参考通道）                         |
 * |   - CacheUtil: 读写用户设置缓存                                               |
 * |   - Tools: 文件路径生成、磁盘检测等工具方法                                    |
 * |   - SaveCsv/SaveBin: 具体格式文件保存实现                                     |
 * |   - SerialBusManage: 串行总线解码数据获取                                     |
 * |   - MainActivity: Android上下文与资源访问                                     |
 * +-----------------------------------------------------------------------------+
 * | 使用场景:                                                                    |
 * |   - 用户点击保存按钮保存波形/CSV/Bin文件                                      |
 * |   - 保存/恢复参考波形(Ref)                                                    |
 * |   - 保存/加载用户设置（仪器参数恢复）                                          |
 * |   - 保存串行总线解码数据为CSV                                                 |
 * |   - 自动保存会话状态                                                          |
 * +=============================================================================+
 */

public class SaveManage {
    private static final String TAG = "SaveManage";  // 日志标签

    public static final String SAVE_WAVE_DEFAULT = "refwave";  // 参考波形默认保存目录名
    public static final String SAVE_CSV_DEFAULT = "csvwave";  // CSV波形默认保存目录名
    public static final String SAVE_BIN_DEFAULT = "binwave";  // Bin波形默认保存目录名
    public static final String SAVE_SETTING_DEFAULT = "default";  // 默认设置保存目录名
    public static final String SAVE_SESSION_DEFAULT = "session";  // 会话保存目录名
    public static final String SAVE_PICTURE_DEFAULT = "picture";  // 截图保存目录名

    public static final String SAVE_AUTOSAVE_DEFAULT= "autosave";  // 自动保存目录名

    //region  单例
    private static class SaveManageHolder {  // 静态内部类实现单例（线程安全）
        public static final SaveManage instance = new SaveManage();  // 静态初始化单例实例
    }

    /**
     * 获取SaveManage单例实例
     *
     * @return SaveManage全局唯一实例
     */
    public static SaveManage getInstance() {
        return SaveManage.SaveManageHolder.instance;  // 返回静态内部类持有的单例
    }
    //endregion

    private static final int Handler_UDiskUpdate = 0x01;  // Handler消息：U盘更新通知
    private static final int Handler_SaveCompleteSuccess = 0x02;  // Handler消息：保存成功
    private static final int Handler_SaveCompleteFailed = 0x03;  // Handler消息：保存失败


    private static final int ThreadPool_WHAT_SaveRecord = 0x01;  // 线程池任务类型：保存用户设置记录
    private static final int ThreadPool_WHAT_LoadRecord = 0x02;  // 线程池任务类型：加载用户设置记录
    private static final int ThreadPool_WHAT_saveWave = 0x03;  // 线程池任务类型：保存波形文件(.mwav)
    private static final int ThreadPool_WHAT_saveCSV = 0x04;  // 线程池任务类型：保存CSV文件
    private static final int ThreadPool_WHAT_saveBin = 0x05;  // 线程池任务类型：保存Bin文件
    private static final int ThreadPool_WHAT_saveRef = 0x06;  // 线程池任务类型：保存参考波形
    private static final int ThreadPool_WHAT_readRef = 0x07;  // 线程池任务类型：读取参考波形
    private static final int ThreadPool_WHAT_saveSerialCSV = 0x08;  // 线程池任务类型：保存串行总线解码CSV

    //region 变量
    private MainActivity mainActivity;  // 主Activity引用，用于资源访问和广播发送
    private static ExecutorService mSingleThreadExecutor = null;  // 单线程池，保证保存操作顺序执行

    private static ExecutorService mMultiThreadExecutor = null;  // 多线程池，用于并发保存操作
    private static SaveSetRunnable mSaveSetRunable = null;  // 保存设置的工作Runnable实例

    private UIHandler uiHandler;  // UI线程Handler，用于将保存结果转发到主线程
    //endregion

    /**
     * 初始化线程池和Handler（无Activity参数版本）
     * 创建单线程池（核心1线程，丢弃溢出任务）和多线程池（核心3线程，调用者线程执行溢出任务）
     */
    public void init() {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 1,  // 创建单核心线程池
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());  // 无超时，链表阻塞队列
        threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());  // 溢出任务丢弃策略
        ThreadPoolExecutor multiThreadPool = new ThreadPoolExecutor(3, Integer.MAX_VALUE,  // 创建多核心线程池
                1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());  // 空闲1秒回收线程
        multiThreadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());  // 溢出由调用者线程执行

        mSingleThreadExecutor = threadPool;  // 赋值单线程池
        mMultiThreadExecutor = multiThreadPool;  // 赋值多线程池
        mSaveSetRunable = new SaveSetRunnable();  // 创建保存设置Runnable实例
        uiHandler = new UIHandler();  // 创建UI Handler实例
    }

    /**
     * 初始化主Activity引用
     *
     * @param mainActivity 主Activity实例
     */
    public void init(MainActivity mainActivity) {
        this.mainActivity = mainActivity;  // 保存主Activity引用

    }

    //region  存储功能接口

    /**
     * 保存到默认名称
     * 将当前用户设置保存为默认设置文件
     *
     * @return SaveManage实例（支持链式调用）
     */
    public SaveManage saveToDefaultSaveName() {
        try {
            saveUserSet(CacheUtil.DefaultSaveName, CacheUtil.get().getCurrMap(), null);  // 保存当前设置到默认文件名
        } catch (InterruptedException e) {
            e.printStackTrace();  // 打印中断异常
        }
        return this;  // 返回自身支持链式调用
    }

    /**
     * 保存到其他默认名称
     * 将当前其他设置保存为其他默认设置文件
     *
     * @return SaveManage实例（支持链式调用）
     */
    public SaveManage saveToOtherSaveName() {
        try {
            saveUserSet(CacheUtil.OtherDefaultSaveName, CacheUtil.get().getCurrOtherMap(), null);  // 保存其他设置到其他默认文件名
        } catch (InterruptedException e) {
            e.printStackTrace();  // 打印中断异常
        }
        return this;  // 返回自身支持链式调用
    }


    /**
     * 检查保存操作是否已完成
     *
     * @return true表示保存已完成，false表示正在保存中
     */
    public boolean saveIsComplete() {
        return mSaveSetRunable.isComplete;  // 返回Runnable的完成标志
    }

    /***
     * 存储用户设置
     * 将用户设置HashMap序列化保存到指定文件，同步等待保存完成
     *
     * @param fileName      保存文件名（不含后缀）
     * @param map           要保存的键值对映射
     * @param saveCallBack  保存结果回调（可为null）
     * @return SaveManage实例（支持链式调用）
     * @throws InterruptedException 线程等待中断异常
     */
    public SaveManage saveUserSet(String fileName, HashMap<String, String> map, SaveCallBack saveCallBack) throws InterruptedException {
        if (mSaveSetRunable.isComplete == false) return this;  // 如果上一次保存未完成则直接返回
        String defaultSuffix = ".SaveRecovery";  // 默认设置文件后缀
        if(fileName.equals(CacheUtil.OtherDefaultSaveName)) {  // 如果是其他默认设置
            defaultSuffix = ".SR";  // 使用.SR后缀
        }
        String path = Tools.resultSavePath(Tools.SaveType_LOCAL, Tools.SaveDir_DEFAULT, mainActivity) + fileName + defaultSuffix;  // 拼接完整保存路径
        if(Utils.isDiskAvaiable(new File(path),1)) {  // 检查磁盘空间是否可用（至少1字节）
            mSaveSetRunable.isComplete = false;  // 标记保存未完成
            mSaveSetRunable.map = map;  // 设置要保存的映射数据
            mSaveSetRunable.what = ThreadPool_WHAT_SaveRecord;  // 设置任务类型为保存记录
            mSaveSetRunable.PathFile = path;  // 设置保存文件路径
            mSaveSetRunable.saveCallBack = saveCallBack;  // 设置保存回调
            mSingleThreadExecutor.execute(mSaveSetRunable);  // 提交到单线程池执行
            while (true) {  // 自旋等待保存完成
                if (mSaveSetRunable.isComplete) break;  // 保存完成后跳出循环
            }
            Logger.i(TAG, "saveUserSet:" + path);  // 记录保存路径日志
        }
        return this;  // 返回自身支持链式调用
    }

    /**
     * 存储用户设置到指定文件路径
     * 与saveUserSet类似，但允许指定完整文件路径
     *
     * @param filePath      完整的保存文件路径
     * @param map           要保存的键值对映射
     * @param saveCallBack  保存结果回调（可为null）
     * @return SaveManage实例（支持链式调用）
     * @throws InterruptedException 线程等待中断异常
     */
    public SaveManage saveUserSetToPath(String filePath, HashMap<String, String> map, SaveCallBack saveCallBack) throws InterruptedException {
        if (!mSaveSetRunable.isComplete) return this;  // 如果上一次保存未完成则直接返回
        mSaveSetRunable.isComplete = false;  // 标记保存未完成
        mSaveSetRunable.map = map;  // 设置要保存的映射数据
        mSaveSetRunable.what = ThreadPool_WHAT_SaveRecord;  // 设置任务类型为保存记录
        mSaveSetRunable.PathFile = filePath;  // 设置完整保存路径
        mSaveSetRunable.saveCallBack = saveCallBack;  // 设置保存回调
        mSingleThreadExecutor.execute(mSaveSetRunable);  // 提交到单线程池执行
        while (true) {  // 自旋等待保存完成
            if (mSaveSetRunable.isComplete) break;  // 保存完成后跳出循环
        }
        Logger.i(TAG, "saveUserSetTopath:" + filePath);  // 记录保存路径日志
        return this;  // 返回自身支持链式调用
    }


    /**
     * 加载用户设置
     * 从默认路径反序列化加载用户设置到指定HashMap，同步等待加载完成
     *
     * @param fileName  设置文件名（不含后缀）
     * @param map       加载后数据填充的目标HashMap
     * @return true加载成功，false加载失败（文件不存在或上一次操作未完成）
     * @throws InterruptedException 线程等待中断异常
     */
    public boolean loadUserSet(String fileName, HashMap<String, String> map) throws InterruptedException {
        if (!mSaveSetRunable.isComplete) return false;  // 如果上一次操作未完成则返回失败
        String defaultSuffix = ".SaveRecovery";  // 默认设置文件后缀
        if(fileName.equals(CacheUtil.OtherDefaultSaveName)) {  // 如果是其他默认设置
            defaultSuffix = ".SR";  // 使用.SR后缀
        }
        String path = Tools.resultSavePath(Tools.SaveType_LOCAL, Tools.SaveDir_DEFAULT, mainActivity) + fileName + defaultSuffix;  // 拼接完整加载路径
        File file = new File(path);  // 创建文件对象
        if (!file.exists()) return false;  // 文件不存在则返回失败
        mSaveSetRunable.map = map;  // 设置目标映射数据
        mSaveSetRunable.what = ThreadPool_WHAT_LoadRecord;  // 设置任务类型为加载记录
        mSaveSetRunable.PathFile = path;  // 设置加载文件路径
        mSaveSetRunable.saveCallBack = null;  // 加载操作无需回调
        mSaveSetRunable.isComplete = false;  // 标记操作未完成
        mSingleThreadExecutor.execute(mSaveSetRunable);  // 提交到单线程池执行
        while (true) {  // 自旋等待加载完成
            if (mSaveSetRunable.isComplete) break;  // 加载完成后跳出循环
        }
        Logger.i(TAG, "loadUserSet() ==>" + " LoadUserSetName:" + path);  // 记录加载路径日志
        return true;  // 返回加载成功
    }

    /**
     * 从指定文件路径加载用户设置
     * 与loadUserSet类似，但允许指定完整文件路径
     *
     * @param filePath  完整的设置文件路径
     * @param map       加载后数据填充的目标HashMap
     * @return true加载成功，false加载失败
     * @throws InterruptedException 线程等待中断异常
     */
    public boolean loadUserSetFromFilePath(String filePath, HashMap<String, String> map) throws InterruptedException {
        if (!mSaveSetRunable.isComplete) return false;  // 如果上一次操作未完成则返回失败
        File file = new File(filePath);  // 创建文件对象
        if (!file.exists()) return false;  // 文件不存在则返回失败
        mSaveSetRunable.map = map;  // 设置目标映射数据
        mSaveSetRunable.what = ThreadPool_WHAT_LoadRecord;  // 设置任务类型为加载记录
        mSaveSetRunable.PathFile = filePath;  // 设置加载文件路径
        mSaveSetRunable.saveCallBack = null;  // 加载操作无需回调
        mSaveSetRunable.isComplete = false;  // 标记操作未完成
        mSingleThreadExecutor.execute(mSaveSetRunable);  // 提交到单线程池执行
        while (true) {  // 自旋等待加载完成
            if (mSaveSetRunable.isComplete) break;  // 加载完成后跳出循环
        }
        Logger.i(TAG, "loadUserSetFromFilePath() ==>" + " LoadUserSetName:" + filePath);  // 记录加载路径日志
        return true;  // 返回加载成功
    }

    /**
     * 统一保存入口方法
     * 根据文件类型(0=Wave, 1=CSV, 2=Bin)保存指定通道的波形数据
     * 每次调用创建新的Runnable实例，支持并发保存
     *
     * @param channel       通道ID（0-10）
     * @param fileType      文件类型（0=mwav, 1=csv, 2=bin）
     * @param savePath      保存目录路径
     * @param fileName      保存文件名（不含后缀）
     * @param selectList    选中的通道列表
     * @param saveCallBack  保存结果回调
     * @return SaveManage实例（支持链式调用）
     */
    public SaveManage allSaveEntrance(@IntRange(from = 0, to = 10) int channel, int fileType, String savePath, String fileName, List<Integer> selectList, SaveCallBack saveCallBack) {
//        if (!mSaveSetRunable.isComplete) return this;
        String suffix;  // 文件后缀名
        SaveSetRunnable mSaveRunnable = new SaveSetRunnable();  // 创建新的Runnable实例（每次保存独立）
        switch (fileType) {  // 根据文件类型确定后缀和任务类型
            default:  // 默认按Wave处理
            case 0:  // Wave类型
                mSaveRunnable.what = ThreadPool_WHAT_saveWave;  // 设置任务类型为保存波形
                suffix = ".mwav";  // 波形文件后缀
                break;
            case 1:  // CSV类型
                mSaveRunnable.what = ThreadPool_WHAT_saveCSV;  // 设置任务类型为保存CSV
                suffix = ".csv";  // CSV文件后缀
                break;
            case 2:  // Bin类型
                mSaveRunnable.what = ThreadPool_WHAT_saveBin;  // 设置任务类型为保存Bin
                suffix = ".bin";  // Bin文件后缀
                break;
        }
        mSaveRunnable.ChannelID = channel;  // 设置通道ID
        mSaveRunnable.saveCallBack = saveCallBack;  // 设置保存回调
        mSaveRunnable.PathFile = savePath + File.separator + fileName + suffix;  // 拼接完整保存路径
        Logger.i("SaveManage allSaveEntrance PathFile= " + mSaveRunnable.PathFile);  // 记录保存路径日志
        mSaveRunnable.selectList = selectList;  // 设置选中通道列表
        if (fileExistResultField(mSaveRunnable.PathFile, mSaveRunnable.saveCallBack)) return this;  // 文件已存在则通知失败并返回
        mSaveRunnable.isComplete = false;  // 标记保存未完成
        mSingleThreadExecutor.execute(mSaveRunnable);  // 提交到单线程池执行
        Logger.i("SaveManage allSaveEntrance PathFile= " + mSaveRunnable.PathFile);  // 再次记录保存路径日志
        return this;  // 返回自身支持链式调用
    }


    /***
     * 保存为WAV文件
     * 将指定通道的波形数据保存为.mwav格式文件，同步等待保存完成
     *
     * @param Channel       保存的通道
     * @param saveType      保存的类型，本地 U盘
     * @param fileName      保存的文件名（不带扩展名）
     * @param saveCallBack  保存结果回调
     * @return SaveManage实例（支持链式调用）
     */
    public SaveManage saveWave(@IntRange(from = 0, to = 10) int Channel, @Tools.SaveType int saveType, String fileName, SaveCallBack saveCallBack) {
        int chMax = GlobalVar.get().getChannelsCount() == GlobalVar.CHANNEL_COUNT_8 ? ChannelFactory.CH8 : ChannelFactory.CH4;  // 根据通道数确定最大通道号
        if (Channel >= ChannelFactory.CH1 && Channel <= chMax && !ChannelFactory.isChOpen(Channel)) {  // 如果通道在范围内且未开启
//            String resultMsg = String.format(mainActivity.getString(com.micsig.tbook.ui.R.string.msgTopSaveBinFailed), ChannelFactory.getChannelName(Channel));
            String resultMsg = "";  // 空错误消息
            saveCallBack.onResult(false, resultMsg);  // 回调通知保存失败
            return this;  // 返回自身
        }
        mSaveSetRunable.what = ThreadPool_WHAT_saveWave;  // 设置任务类型为保存波形
        mSaveSetRunable.ChannelID = Channel;  // 设置通道ID
        mSaveSetRunable.saveCallBack = saveCallBack;  // 设置保存回调
        mSaveSetRunable.PathFile = Tools.resultSavePath(saveType, Tools.SaveDir_REFWAVE, mainActivity) + fileName + ".mwav";  // 拼接完整保存路径
        if (fileExistResultField(mSaveSetRunable.PathFile, saveCallBack)) return this;  // 文件已存在则通知失败并返回
        mSaveSetRunable.isComplete = false;  // 标记保存未完成
        mSingleThreadExecutor.execute(mSaveSetRunable);  // 提交到单线程池执行
        while (true) {  // 自旋等待保存完成
            if (mSaveSetRunable.isComplete) break;  // 保存完成后跳出循环
        }
        return this;  // 返回自身支持链式调用
    }

    /***
     * 保存为CSV文件
     * 将指定通道的波形数据保存为.csv格式文件，异步执行
     *
     * @param Channel       保存的通道
     * @param saveType      保存的类型   本地   U盘
     * @param fileName      保存的文件名（不带扩展名）
     * @param saveCallBack  保存结果回调
     * @return SaveManage实例（支持链式调用）
     * @throws InterruptedException 线程中断异常
     */
    public SaveManage saveCSV(@IntRange(from = 0, to = 10) int Channel, @Tools.SaveType int saveType, String fileName, SaveCallBack saveCallBack) throws InterruptedException {
        if (mSaveSetRunable.isComplete == false) return this;  // 如果上一次保存未完成则直接返回
        mSaveSetRunable.what = ThreadPool_WHAT_saveCSV;  // 设置任务类型为保存CSV
        mSaveSetRunable.ChannelID = Channel;  // 设置通道ID
        mSaveSetRunable.saveCallBack = saveCallBack;  // 设置保存回调
        mSaveSetRunable.PathFile = Tools.resultSavePath(saveType, Tools.SaveDir_CSVWAVE, mainActivity) + fileName + ".csv";  // 拼接完整保存路径
        if (fileExistResultField(mSaveSetRunable.PathFile, mSaveSetRunable.saveCallBack))  // 文件已存在则通知失败
            return this;  // 返回自身
        mSaveSetRunable.isComplete = false;  // 标记保存未完成
        mSingleThreadExecutor.execute(mSaveSetRunable);  // 提交到单线程池执行
//        while (true) {
//            if (mSaveSetRunable.isComplete) break;
//        }
        return this;  // 返回自身支持链式调用（异步，不等待完成）
    }

    /***
     * 保存为CSV文件（多通道选择版）
     * 将选中通道的波形数据保存为.csv格式文件，异步执行
     *
     * @param selectList    选中要保存的通道集合
     * @param saveType      保存的类型   本地   U盘
     * @param fileName      保存的文件名（不带扩展名）
     * @param saveCallBack  保存结果回调
     * @return SaveManage实例（支持链式调用）
     * @throws InterruptedException 线程中断异常
     */
    public SaveManage saveCSV(List<Integer> selectList, @Tools.SaveType int saveType, String fileName, SaveCallBack saveCallBack) throws InterruptedException {
        if (mSaveSetRunable.isComplete == false) return this;  // 如果上一次保存未完成则直接返回
        mSaveSetRunable.what = ThreadPool_WHAT_saveCSV;  // 设置任务类型为保存CSV
//        mSaveSetRunable.ChannelID = Channel;
        mSaveSetRunable.selectList = selectList;  // 设置选中通道列表
        mSaveSetRunable.saveCallBack = saveCallBack;  // 设置保存回调
        mSaveSetRunable.PathFile = Tools.resultSavePath(saveType, Tools.SaveDir_CSVWAVE, mainActivity) + fileName + ".csv";  // 拼接完整保存路径
        if (fileExistResultField(mSaveSetRunable.PathFile, mSaveSetRunable.saveCallBack))  // 文件已存在则通知失败
            return this;  // 返回自身
        mSaveSetRunable.isComplete = false;  // 标记保存未完成
        mSingleThreadExecutor.execute(mSaveSetRunable);  // 提交到单线程池执行
        return this;  // 返回自身支持链式调用
    }



    /***
     * 保存为Bin文件
     * 将指定通道的波形数据保存为.bin格式文件，异步执行
     *
     * @param Channel       保存的通道
     * @param saveType      保存的类型  本地  U盘
     * @param fileName      保存的文件名（不带扩展名）
     * @param saveCallBack  保存结果回调
     * @return SaveManage实例（支持链式调用）
     * @throws InterruptedException 线程中断异常
     */
    public SaveManage saveBin(@IntRange(from = 0, to = 10) int Channel, @Tools.SaveType int saveType, String fileName, SaveCallBack saveCallBack) throws InterruptedException {
        if (mSaveSetRunable.isComplete == false) return this;  // 如果上一次保存未完成则直接返回
        mSaveSetRunable.what = ThreadPool_WHAT_saveBin;  // 设置任务类型为保存Bin
        mSaveSetRunable.ChannelID = Channel;  // 设置通道ID
        mSaveSetRunable.saveCallBack = saveCallBack;  // 设置保存回调
        mSaveSetRunable.PathFile = Tools.resultSavePath(saveType, Tools.SaveDir_BINWAVE, mainActivity) + fileName + ".bin";  // 拼接完整保存路径
        if (fileExistResultField(mSaveSetRunable.PathFile, mSaveSetRunable.saveCallBack))  // 文件已存在则通知失败
            return this;  // 返回自身
        mSaveSetRunable.isComplete = false;  // 标记保存未完成
        mSingleThreadExecutor.execute(mSaveSetRunable);  // 提交到单线程池执行
//        while (true) {
//            if (mSaveSetRunable.isComplete) break;
//        }
        return this;  // 返回自身支持链式调用
    }

    /**
     * 保存串行总线解码数据为CSV文件（单总线版）
     * 将指定串行总线通道(S1-S4)的解码数据保存为CSV格式
     *
     * @param Channel       串行总线通道ID（S1-S4）
     * @param saveType      保存类型（本地/U盘）
     * @param fileName      保存文件名（不含后缀）
     * @param saveCallBack  保存结果回调
     * @return SaveManage实例（支持链式调用）
     */
    public SaveManage saveSerialCSV(int Channel, @Tools.SaveType int saveType, String fileName, SaveCallBack saveCallBack) {
        if (mSaveSetRunable.isComplete == false) return this;  // 如果上一次保存未完成则直接返回
        mSaveSetRunable.isComplete = false;  // 标记保存未完成
        mSaveSetRunable.what = ThreadPool_WHAT_saveSerialCSV;  // 设置任务类型为保存串行CSV
        mSaveSetRunable.ChannelID = Channel;  // 设置串行总线通道ID
        mSaveSetRunable.saveCallBack = saveCallBack;  // 设置保存回调
        mSaveSetRunable.PathFile = Tools.resultSavePath(saveType, Tools.SaveDir_CSVSBT, mainActivity) + fileName + ".csv";  // 拼接完整保存路径
        mSingleThreadExecutor.execute(mSaveSetRunable);  // 提交到单线程池执行

        return this;  // 返回自身支持链式调用
    }

    /**
     * 保存串行总线解码数据为CSV文件（多总线版）
     * 将指定串行总线通道的所有总线解码数据保存为CSV格式
     *
     * @param Channel        串行总线通道ID
     * @param allSerialsBus  所有串行总线标识位
     * @param saveType       保存类型（本地/U盘）
     * @param fileName       保存文件名（不含后缀）
     * @param saveCallBack   保存结果回调
     * @return SaveManage实例（支持链式调用）
     */
    public SaveManage saveSerialCSV(int Channel, int allSerialsBus, @Tools.SaveType int saveType, String fileName, SaveCallBack saveCallBack) {
        if (mSaveSetRunable.isComplete == false) return this;  // 如果上一次保存未完成则直接返回
        mSaveSetRunable.isComplete = false;  // 标记保存未完成
        mSaveSetRunable.what = ThreadPool_WHAT_saveSerialCSV;  // 设置任务类型为保存串行CSV
        mSaveSetRunable.ChannelID = Channel;  // 设置串行总线通道ID
        mSaveSetRunable.allSerialsBus = allSerialsBus;  // 设置所有串行总线标识
        mSaveSetRunable.saveCallBack = saveCallBack;  // 设置保存回调
        mSaveSetRunable.PathFile = Tools.resultSavePath(saveType, Tools.SaveDir_CSVSBT, mainActivity) + fileName + ".csv";  // 拼接完整保存路径
        mSingleThreadExecutor.execute(mSaveSetRunable);  // 提交到单线程池执行

        return this;  // 返回自身支持链式调用
    }

    /***
     * 保存数据到参考通道
     * 将指定通道的波形数据保存为参考波形文件，保存后立即重新加载到参考通道
     *
     * @param Channel       保存的通道 Ch1--Ch4
     * @param saveType      保存的类型  本地  U盘
     * @param ref           参考通道编号（REF1-REF8）
     * @param saveCallBack  保存结果回调
     * @return 保存的文件位置路径，保存失败返回空字符串
     */
    public String saveRef(int Channel, @Tools.SaveType int saveType, int ref, SaveCallBack saveCallBack) {
        if (!mSaveSetRunable.isComplete) return "";  // 如果上一次操作未完成则返回空
        mSaveSetRunable.isComplete = false;  // 标记操作未完成
        String fileName = "Ref1";  // 默认参考波形文件名
        switch (ref) {  // 根据参考通道编号确定文件名
            case ChannelFactory.REF1: {  // 参考通道1
                fileName = "Ref1";  // 文件名为Ref1
            }
            break;
            case ChannelFactory.REF2: {  // 参考通道2
                fileName = "Ref2";  // 文件名为Ref2
            }
            break;
            case ChannelFactory.REF3: {  // 参考通道3
                fileName = "Ref3";  // 文件名为Ref3
            }
            break;
            case ChannelFactory.REF4: {  // 参考通道4
                fileName = "Ref4";  // 文件名为Ref4
            }
            break;
            case ChannelFactory.REF5: {  // 参考通道5
                fileName = "Ref5";  // 文件名为Ref5
            }
            break;
            case ChannelFactory.REF6: {  // 参考通道6
                fileName = "Ref6";  // 文件名为Ref6
            }
            break;
            case ChannelFactory.REF7: {  // 参考通道7
                fileName = "Ref7";  // 文件名为Ref7
            }
            break;
            case ChannelFactory.REF8: {  // 参考通道8
                fileName = "Ref8";  // 文件名为Ref8
            }
            break;
        }
        mSaveSetRunable.what = ThreadPool_WHAT_saveRef;  // 设置任务类型为保存参考波形
        mSaveSetRunable.ChannelID = Channel;  // 设置源通道ID
        mSaveSetRunable.saveCallBack = saveCallBack;  // 设置保存回调
        mSaveSetRunable.PathFile = Tools.resultSavePath(saveType, Tools.SaveDir_REFDEFAULT, mainActivity) + fileName + ".mwav";  // 拼接完整保存路径
        File file = new File(mSaveSetRunable.PathFile);  // 创建文件对象
        if (file.exists()) {  // 如果参考波形文件已存在
            file.delete();  // 删除旧文件（覆盖保存）
        }
        mSingleThreadExecutor.execute(mSaveSetRunable);  // 提交到单线程池执行
        while (true) {  // 自旋等待保存完成
            if (mSaveSetRunable.isComplete) break;  // 保存完成后跳出循环
        }
        if (file.exists()) {  // 如果保存后文件存在
            readRef(ref, mSaveSetRunable.PathFile);  // 立即重新加载参考波形
        } else {  // 保存失败
            mSaveSetRunable.PathFile = "";  // 清空路径
        }

        return mSaveSetRunable.PathFile;  // 返回保存路径（失败则为空字符串）
    }

    /**
     * 读取参考波形文件并加载到参考通道
     * 加载波形数据后同步更新缓存中的垂直缩放、触发位置、Y偏移、时基缩放等参数
     *
     * @param Channel    参考通道编号（REF1-REF8）
     * @param pathFile   参考波形文件路径，或MSS_REF_TAG特殊标记
     * @return true加载成功，false加载失败
     */
    public boolean readRef(int Channel, String pathFile) {
        Log.d(TAG, "readRef() called with: Channel = [" + Channel + "], pathFile = [" + pathFile + "]");  // 调试日志
        RefChannel refChannel = ChannelFactory.getRefChannel(Channel /*ChannelFactory.REF1*/);  // 获取参考通道实例
        if (refChannel != null) {  // 参考通道存在
            if (SaveRecoverySession.MSS_REF_TAG.equals(pathFile)  // 如果是MSS会话恢复标记
                    || refChannel.loadWave(pathFile)) {  // 或者波形加载成功
                Logger.d(TAG, "Ref" + (Channel - ChannelFactory.REF1 + 1) + ": load " + pathFile + " success!");  // 记录加载成功日志

                if(!SaveRecoverySession.MSS_REF_TAG.equals(pathFile)){  // 非MSS会话恢复标记时，需要同步UI参数
                    //SaveRecoverySession.MSS_REF_TAG.equals(pathFile)

                    CacheUtil.get().putMap(CacheUtil.MAIN_CHAN_REF_VSCALE_ID + TChan.toUiChNo(Channel),  // 缓存参考通道垂直缩放ID
                            String.valueOf(refChannel.getVScaleId()));  // 获取垂直缩放ID值
                    //修改REF的触发时刻;
                    TriggerTimebase.getInstance().putCacheForTimeBasePosition(ScopeBase.getWidth() / 2 - refChannel.getXPos_pix_original(), Channel);  // 计算并缓存触发位置（屏幕中心减去波形原始X偏移）
                    //修改ref的y偏移
                    int index = TChan.toUiChNo(Channel);  // 转换为UI通道索引
                    double offset = ScopeBase.getNewHeight() / 2 - refChannel.getPosUI();  // 计算Y偏移（屏幕高度中心减去波形UI位置）
                    Tools.putYTChannelPosition(index, offset);  // 缓存Y偏移位置

                    //CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_REF_Y_POSITION + index, String.valueOf(offset));
                    WaveManage.get().setPositionY(index, Tools.getChannelPositionUI(index));  // 设置波形管理器的Y位置
                    double scaleVal = ChannelFactory.getRefChannel(Channel).getRefTimeScaleVal();  // 获取参考通道时基缩放值
                    CacheUtil.get().putMap(CacheUtil.MAIN_BOTTOM_TIMEBASE_REF_SCALE + index  // 缓存参考通道时基缩放
                            , RightLayoutRef.getStringRefScale(TChan.toFpgaChNo(index), scaleVal));  // 转换为字符串格式

//                    CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_CH_DELAY + index, "0 ns");//重新加载Ref时，将Delay归零
                    refChannel.setDelay(0);  // 重置参考通道延迟为0
//                    String delay = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_DELAY + TChan.toUiChNo(Channel));
//                    if (refChannel.getRefType() != WaveData.FFT_WAVE) {
//                        refChannel.setDelay(TBookUtil.getBigDoubleFromM(delay.replace("s", "")));
//                    }
                    return true;  // 返回加载成功
                }
            }
        }
        return false;  // 返回加载失败
    }

    //endregion

    /**
     * 保存设置工作线程Runnable
     * 封装所有保存/加载操作的执行逻辑，通过what字段区分不同任务类型
     * 在单线程池中顺序执行，保证IO操作不会并发冲突
     */
    class SaveSetRunnable implements Runnable {
        private static final String TAG = "SaveSetRunnalbe";  // 日志标签
        public volatile int what;  // 任务类型标识（volatile保证线程可见性）
        public volatile int ChannelID;  // 通道ID
        public volatile List<Integer> selectList;  // 选中的通道列表
        public String PathFile;  // 保存/加载文件路径
        public volatile boolean isComplete = true;  // 操作完成标志（volatile保证线程可见性）
        public HashMap<String, String> map;  // 用户设置键值对映射
        public SaveCallBack saveCallBack;  // 保存结果回调
        public volatile int allSerialsBus = -1;  // 所有串行总线标识（-1表示未指定）

        /**
         * Runnable执行方法
         * 根据what字段分发到不同的保存/加载逻辑
         */
        @Override
        public void run() {
//            Thread.currentThread().setName("SaveManage");
            Logger.i(TAG, "thread pool begin! name= " + Thread.currentThread().getName());  // 记录线程开始日志
            switch (what) {  // 根据任务类型分发
                case ThreadPool_WHAT_SaveRecord: {  // 保存用户设置记录
                    HashMap<String, String> hashMap = (HashMap<String, String>) (map.clone());  // 克隆映射数据（避免并发修改）
                    // 写
                    FileOutputStream fos = null;  // 文件输出流
                    BufferedOutputStream bos = null;  // 缓冲输出流
                    ObjectOutputStream oos = null;  // 对象输出流
                    try {
                        fos = new FileOutputStream(PathFile);  // 创建文件输出流
                        bos = new BufferedOutputStream(fos);  // 包装为缓冲输出流
                        oos = new ObjectOutputStream(bos);  // 包装为对象输出流
                        oos.writeObject(hashMap);  // 序列化写入HashMap
                        oos.flush();  // 刷新对象输出流
                        oos.close();  // 关闭对象输出流
                        bos.flush();  // 刷新缓冲输出流
                        bos.close();  // 关闭缓冲输出流
                        fos.flush();  // 刷新文件输出流
                        fos.close();  // 关闭文件输出流
                    } catch (IOException e) {
                        e.printStackTrace();  // 打印IO异常
                    }
                }
                break;
                case ThreadPool_WHAT_LoadRecord: {  // 加载用户设置记录
                    // 读
                    FileInputStream fis = null;  // 文件输入流
                    BufferedInputStream bis = null;  // 缓冲输入流
                    ObjectInputStream ois = null;  // 对象输入流
                    try {
                        fis = new FileInputStream(PathFile);  // 创建文件输入流
                        bis = new BufferedInputStream(fis);  // 包装为缓冲输入流
                        ois = new ObjectInputStream(bis);  // 包装为对象输入流
                        HashMap<String, String> hashMap = new HashMap<String, String>();  // 创建临时HashMap
                        hashMap = (HashMap<String, String>) ois.readObject();  // 反序列化读取HashMap
                        ois.close();  // 关闭对象输入流
                        bis.close();  // 关闭缓冲输入流
                        fis.close();  // 关闭文件输入流
                        //CacheUtil.get().putMapAll(hashMap);
                        if (!hashMap.isEmpty()) {  // 如果读取的数据非空
                            map.clear();  // 清空目标映射
                            map.putAll(hashMap);  // 将读取的数据填充到目标映射
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();  // 打印文件未找到异常
                    } catch (OptionalDataException e) {
                        e.printStackTrace();  // 打印可选数据异常
                    } catch (StreamCorruptedException e) {
                        e.printStackTrace();  // 打印流损坏异常
                    } catch (IOException e) {
                        e.printStackTrace();  // 打印IO异常
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();  // 打印类未找到异常
                    }
                    isComplete = true;  // 标记操作完成
                    Tools.sleep(10);  // 短暂休眠10ms
                    CacheUtil.get().clearTempSaveParam(map);  // 清理临时保存参数并同步到缓存
                }
                return;  // 加载完成后直接返回（不执行后续的Handler通知逻辑）
/*                case ThreadPool_WHAT_saveWave: {
                    boolean bSave = true;
                    if (!Scope.getInstance().isRun()
                            && !ScopeFrozen.getInstance().isValid()) {
                        bSave = false;
                    }
                    if(ChannelFactory.isMathCh(ChannelID)){
                        MathChannel mathChannel = ChannelFactory.getMathChannel(ChannelID);
                        if(mathChannel != null && bSave){
                            mathChannel.save(PathFile);
                        }
                    }else if(ChannelFactory.isDynamicCh(ChannelID)){
                        Channel channel = ChannelFactory.getDynamicChannel(ChannelID);
                        if(channel != null && bSave){
                            channel.save(PathFile);
                        }
                    }
                }
                break;*/
                case ThreadPool_WHAT_saveCSV: {  // 保存CSV文件
                    boolean bSave = true;  // 是否可保存标志
                    if (!Scope.getInstance().isRun()  // 示波器未运行
                            && !ScopeFrozen.getInstance().isValid()) {  // 且冻结状态无效
                        bSave = false;  // 不可保存
                    }
                    SaveCsv saveCsv = SaveCsv.getInstance();  // 获取CSV保存实例
                    saveCsv.clear();  // 清空之前的数据
                    for (int i = 0; i < selectList.size(); i++) {  // 遍历选中通道列表
                        IChannel channel = ChannelFactory.getValidChannel(selectList.get(i));  // 获取有效通道实例
                        if(channel == null) continue;  // 通道无效则跳过
                        saveCsv.add(channel);  // 将通道数据添加到CSV保存列表
                    }
                    long needUse = saveCsv.calcStorageSize();  // 计算所需存储空间
                    File file = new File(PathFile);  // 创建目标文件对象
                    if (!Utils.isDiskAvaiable(file, needUse)) {  // 检查磁盘空间是否足够
                        bSave = false;  // 空间不足则不可保存
                    }
                    if (bSave) {  // 可以保存
                        saveCsv.save(PathFile);  // 执行CSV保存
                        saveCsv.setSaveCsvProgress(val -> {//进度条  // 设置保存进度回调
                            EventBase eventBase = new EventBase(EventFactory.EVENT_SAVECSV_RUN);  // 创建CSV保存进度事件
                            Logger.i("SaveCsv progress=" + val);  // 记录进度日志
                            eventBase.setData(val);  // 设置进度值
                            EventFactory.sendEvent(eventBase, false);  // 发送进度事件
                        });
                        while (!saveCsv.isFinish()) {  // 等待保存完成
                            try {
                                Thread.sleep(100);  // 每100ms检查一次
                            } catch (InterruptedException e) {
                                e.printStackTrace();  // 打印中断异常
                            }
                        }
                    }
                }
                break;
                case ThreadPool_WHAT_saveBin: {  // 保存Bin文件
                    Channel channel = ChannelFactory.getDynamicChannel(ChannelID);  // 获取动态通道实例
                    if (channel != null) {  // 通道有效
                        boolean bAllSegments = CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_SAVE_ALL_SEGMENT_VISIBLE)  // 是否可见"保存所有段"选项
                                && CacheUtil.get().getBoolean(CacheUtil.TOP_SLIP_SAVE_ALL_SEGMENT_CHECK);  // 是否勾选"保存所有段"
                        SaveBin.getInstance().save(channel, PathFile,bAllSegments);  // 执行Bin保存
                    }
                }
                break;
                case ThreadPool_WHAT_saveWave:  // 保存波形文件
                case ThreadPool_WHAT_saveRef: {  // 保存参考波形文件
                    boolean bSave = true;  // 是否可保存标志
                    if (!Scope.getInstance().isRun()  // 示波器未运行
                            && !ScopeFrozen.getInstance().isValid()) {  // 且冻结状态无效
                        bSave = false;  // 不可保存
                    }
                    //com.micsig.tbook.scope.channel.Channel channel = ChannelFactory.getDynamicChannel(ChannelID);
                    BaseChannel channel = null;  // 通道基类引用
                    if (ChannelFactory.isDynamicCh(ChannelID))  // 如果是动态通道
                        channel = ChannelFactory.getDynamicChannel(ChannelID);  // 获取动态通道实例
                    else if (ChannelFactory.isMathCh(ChannelID)) {  // 如果是数学通道
                        channel = ChannelFactory.getMathChannel(ChannelID);  // 获取数学通道实例
                    } else if (ChannelFactory.isRefCh(ChannelID)) {  // 如果是参考通道
                        channel = ChannelFactory.getRefChannel(ChannelID);  // 获取参考通道实例
                    }

                    Logger.i(TAG, "channel= " + channel.getName() + " ,bsave= " + bSave + " ,isOPen= " + channel.isOpen());  // 记录通道保存状态日志
                    if (channel != null && channel.isOpen() && bSave) {  // 通道有效、已开启、可保存
                        channel.save(PathFile);  // 执行波形保存
                    }
                }
                break;
                case ThreadPool_WHAT_readRef: {  // 读取参考波形（当前为空实现）
                }
                break;
                case ThreadPool_WHAT_saveSerialCSV: { //串型文本解码保存  // 保存串行总线解码数据为CSV
                    int serialbus = 0;  // 串行总线类型
                    if (ChannelID == ISerialsWord.TYPE_S1) {  // 串行总线S1
                        serialbus = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1);  // 获取S1总线类型
                    } else if (ChannelID == ISerialsWord.TYPE_S2) {  // 串行总线S2
                        serialbus = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2);  // 获取S2总线类型
                    } else if (ChannelID == ISerialsWord.TYPE_S3) {  // 串行总线S3
                        serialbus = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3);  // 获取S3总线类型
                    } else if (ChannelID == ISerialsWord.TYPE_S4) {  // 串行总线S4
                        serialbus = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4);  // 获取S4总线类型
                    } else if (ChannelID == ISerialsWord.TYPE_S12) {  // 串行总线S12（多总线）
                        serialbus = allSerialsBus;  // 使用传入的总线标识
                    }

                    File file = new File(PathFile);  // 创建目标文件对象
                    if (file.exists()) file.delete();  // 文件已存在则删除（覆盖保存）
                    StringBuilder sb = new StringBuilder();  // 用于拼接CSV内容

                    //region 各种串形解码
                    switch (serialbus) {  // 根据总线类型生成不同的CSV内容
                        case RightLayoutSerials.SERIALS_UART: {  // UART串行总线

                            LinkedBlockingQueue<SerialBusTxtStruct.UartStruct> uartListTotal = SerialBusManage.getInstance()  // 获取UART解码数据缓冲队列
                                    .getSerialTxtBufferQueue(ChannelID, serialbus, true);  // 获取全部数据（true=清空缓冲）
                            sb.append("SerialsBusTextUART " + Tools.genNameByDateTime() + "\n");  // 添加CSV标题行
                            sb.append(SerialBusTxtStruct.toCSVTitleUart() + "\n");  // 添加UART列标题
                            for (Iterator iter = uartListTotal.iterator(); iter.hasNext(); ) {  // 遍历所有UART数据
                                SerialBusTxtStruct.UartStruct uart = (SerialBusTxtStruct.UartStruct) iter.next();  // 获取UART数据项
                                sb.append(uart.toCSV() + "\n");  // 添加UART数据行
                            }
                        }
                        break;
                        case RightLayoutSerials.SERIALS_LIN: {  //ConcurrentLinkedQueue  BlockingQueue  // LIN串行总线
                            LinkedBlockingQueue<SerialBusTxtStruct.LinStruct> linListTotal = SerialBusManage.getInstance()  // 获取LIN解码数据缓冲队列
                                    .getSerialTxtBufferQueue(ChannelID, serialbus, true);  // 获取全部数据
                            sb.append("SerialsBusTextLIN " + Tools.genNameByDateTime() + "\n");  // 添加CSV标题行
                            sb.append(SerialBusTxtStruct.toCSVTitleLin() + "\n");  // 添加LIN列标题
                            for (Iterator iter = linListTotal.iterator(); iter.hasNext(); ) {  // 遍历所有LIN数据
                                SerialBusTxtStruct.LinStruct lin = (SerialBusTxtStruct.LinStruct) iter.next();  // 获取LIN数据项
                                sb.append(lin.toCSV() + "\n");  // 添加LIN数据行
                            }
                        }
                        break;
                        case RightLayoutSerials.SERIALS_CAN: {  // CAN串行总线
                            LinkedBlockingQueue<SerialBusTxtStruct.CanStruct> canListTotal = SerialBusManage.getInstance()  // 获取CAN解码数据缓冲队列
                                    .getSerialTxtBufferQueue(ChannelID, serialbus, true);  // 获取全部数据
                            sb.append("SerialsBusTextCAN " + Tools.genNameByDateTime() + "\n");  // 添加CSV标题行
                            sb.append(SerialBusTxtStruct.toCSVTitleCan() + "\n");  // 添加CAN列标题
                            for (Iterator iter = canListTotal.iterator(); iter.hasNext(); ) {  // 遍历所有CAN数据
                                SerialBusTxtStruct.CanStruct can = (SerialBusTxtStruct.CanStruct) iter.next();  // 获取CAN数据项
                                sb.append(can.toCSV() + "\n");  // 添加CAN数据行
                            }
                        }
                        break;
                        case RightLayoutSerials.SERIALS_SPI: {  // SPI串行总线
                            LinkedBlockingQueue<SerialBusTxtStruct.SpiStruct> spiListTotal = SerialBusManage.getInstance()  // 获取SPI解码数据缓冲队列
                                    .getSerialTxtBufferQueue(ChannelID, serialbus, true);  // 获取全部数据
                            sb.append("SerialsBusTextSPI " + Tools.genNameByDateTime() + "\n");  // 添加CSV标题行
                            sb.append(SerialBusTxtStruct.toCSVTitleSpi() + "\n");  // 添加SPI列标题
                            for (Iterator iter = spiListTotal.iterator(); iter.hasNext(); ) {  // 遍历所有SPI数据
                                SerialBusTxtStruct.SpiStruct spi = (SerialBusTxtStruct.SpiStruct) iter.next();  // 获取SPI数据项
                                sb.append(spi.toCSV() + "\n");  // 添加SPI数据行
                            }
                        }
                        break;
                        case RightLayoutSerials.SERIALS_I2C: {  // I2C串行总线
                            LinkedBlockingQueue<SerialBusTxtStruct.I2cStruct> i2cListTotal = SerialBusManage.getInstance()  // 获取I2C解码数据缓冲队列
                                    .getSerialTxtBufferQueue(ChannelID, serialbus, true);  // 获取全部数据
                            sb.append("SerialsBusTextI2C " + Tools.genNameByDateTime() + "\n");  // 添加CSV标题行
                            sb.append(SerialBusTxtStruct.toCSVTitleI2c() + "\n");  // 添加I2C列标题
                            for (Iterator iter = i2cListTotal.iterator(); iter.hasNext(); ) {  // 遍历所有I2C数据
                                SerialBusTxtStruct.I2cStruct i2c = (SerialBusTxtStruct.I2cStruct) iter.next();  // 获取I2C数据项
                                sb.append(i2c.toCSV() + "\n");  // 添加I2C数据行
                            }
                        }
                        break;
                        case RightLayoutSerials.SERIALS_M429: {  // ARINC429串行总线
                            LinkedBlockingQueue<SerialBusTxtStruct.Arinc429Struct> arinc429ListTotal = SerialBusManage.getInstance()  // 获取ARINC429解码数据缓冲队列
                                    .getSerialTxtBufferQueue(ChannelID, serialbus, true);  // 获取全部数据
                            sb.append("SerialsBusText429 " + Tools.genNameByDateTime() + "\n");  // 添加CSV标题行
                            sb.append(SerialBusTxtStruct.toCSVTitleArinc492() + "\n");  // 添加ARINC429列标题
                            for (Iterator iter = arinc429ListTotal.iterator(); iter.hasNext(); ) {  // 遍历所有ARINC429数据
                                SerialBusTxtStruct.Arinc429Struct a429 = (SerialBusTxtStruct.Arinc429Struct) iter.next();  // 获取ARINC429数据项
                                sb.append(a429.toCSV() + "\n");  // 添加ARINC429数据行
                            }
                        }
                        break;
                        case RightLayoutSerials.SERIALS_M1553B: {  // MIL-STD-1553B串行总线
                            LinkedBlockingQueue<SerialBusTxtStruct.MilSTD1553bStruct> m1553bListTotal = SerialBusManage.getInstance()  // 获取1553B解码数据缓冲队列
                                    .getSerialTxtBufferQueue(ChannelID, serialbus, true);  // 获取全部数据
                            sb.append("SerialsBusText1553B " + Tools.genNameByDateTime() + "\n");  // 添加CSV标题行
                            sb.append(SerialBusTxtStruct.toCSVTitleM1553b() + "\n");  // 添加1553B列标题
                            for (Iterator iter = m1553bListTotal.iterator(); iter.hasNext(); ) {  // 遍历所有1553B数据
                                SerialBusTxtStruct.MilSTD1553bStruct m1553b = (SerialBusTxtStruct.MilSTD1553bStruct) iter.next();  // 获取1553B数据项
                                sb.append(m1553b.toCSV() + "\n");  // 添加1553B数据行
                            }
                        }
                        break;
                    }
                    Tools.saveStringBuild(PathFile, sb);  // 将StringBuilder内容写入文件
//                    Tools.sleep(3000);
                    //endregion
                }
                break;
            }

            if (mainActivity == null) {  // 主Activity未初始化
                isComplete = true;  // 标记操作完成
                Message msg = new Message();  // 创建Handler消息
                msg.what = Handler_SaveCompleteFailed;  // 设置消息类型为保存失败
                msg.obj = new UiHandlerMsg(this.PathFile, saveCallBack);  // 设置消息内容
                uiHandler.sendMessage(msg);  // 发送消息到UI线程
                return;  // 直接返回
            }
            Memory.Sync();  // 同步内存（确保数据写入磁盘）
            boolean b = uDiskUpdate(this.PathFile);  // 通知U盘媒体扫描更新
            if (b) {  // U盘更新成功

                Message msg = new Message();  // 创建Handler消息
                msg.what = Handler_SaveCompleteSuccess;  // 设置消息类型为保存成功
                msg.obj = new UiHandlerMsg(this.PathFile, saveCallBack);  // 设置消息内容
                msg.arg1 = 1;  // 附加参数：1表示成功
                uiHandler.sendMessage(msg);  // 发送消息到UI线程
            } else {  // U盘更新失败
                Message msg = new Message();  // 创建Handler消息
                msg.what = Handler_SaveCompleteFailed;  // 设置消息类型为保存失败
                msg.obj = new UiHandlerMsg(this.PathFile, saveCallBack);  // 设置消息内容
                msg.arg1 = 0;  // 附加参数：0表示失败
                uiHandler.sendMessage(msg);  // 发送消息到UI线程
            }

            isComplete = true;  // 标记操作完成
            Logger.i(TAG, "thread pool end!");  // 记录线程结束日志
        }
    }

    /**
     * UI Handler消息数据封装类
     * 用于在Handler消息中传递文件路径和回调信息
     */
    class UiHandlerMsg {
        String pathFile;  // 文件路径
        SaveCallBack saveCallBack;  // 保存结果回调

        /**
         * 构造UiHandlerMsg
         *
         * @param pathFile      文件路径
         * @param saveCallBack  保存结果回调
         */
        public UiHandlerMsg(String pathFile, SaveCallBack saveCallBack) {
            this.pathFile = pathFile;  // 设置文件路径
            this.saveCallBack = saveCallBack;  // 设置回调
        }
    }

    /**
     * 保存结果回调接口
     * 用于通知调用方保存操作的结果
     */
    public interface SaveCallBack {
        /**
         * 保存结果回调方法
         *
         * @param success  true保存成功，false保存失败
         * @param msg      结果消息文本
         */
        void onResult(boolean success, String msg);
    }

    /**
     * UI线程Handler
     * 接收工作线程发送的保存结果消息，在主线程中处理UI更新和回调通知
     */
    class UIHandler extends Handler {
        /**
         * 处理Handler消息
         * 根据消息类型分发到保存成功、保存失败或U盘更新处理逻辑
         *
         * @param msg  Handler消息对象
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {  // 根据消息类型分发
                case Handler_SaveCompleteSuccess: {  // 保存成功
                    UiHandlerMsg handlerMsg = (UiHandlerMsg) msg.obj;  // 获取消息数据
                    saveCompleteSuccess(handlerMsg.pathFile);  // 处理保存成功逻辑
                    uDiskUpdate(handlerMsg.pathFile);  // 通知U盘媒体扫描更新
                    if (handlerMsg.saveCallBack != null) {  // 如果有回调
                        String[] paths = handlerMsg.pathFile.split("/");  // 分割路径获取文件名
                        String resultMsg = String.format(mainActivity.getString(com.micsig.tbook.ui.R.string.msgTopSaveBinSuccess), paths[paths.length - 1]);  // 格式化成功消息
                        handlerMsg.saveCallBack.onResult(true, resultMsg);  // 回调通知保存成功
                    }
                    Logger.i(TAG, "Handler_SaveCompleteSuccess["  // 记录成功日志
                            + Thread.currentThread().getName() + "]" + handlerMsg.pathFile);
                }
                break;
                case Handler_SaveCompleteFailed: {  // 保存失败
                    UiHandlerMsg handlerMsg = (UiHandlerMsg) msg.obj;  // 获取消息数据
                    saveCompleteFailed(handlerMsg.pathFile);  // 处理保存失败逻辑
//                    int spaceAvailable = msg.arg1;
                    uDiskUpdate(handlerMsg.pathFile);  // 通知U盘媒体扫描更新
                    if (handlerMsg.saveCallBack != null) {  // 如果有回调
                        String[] paths = handlerMsg.pathFile.split("/");  // 分割路径获取文件名
                        String resultMsg = String.format(mainActivity.getString(com.micsig.tbook.ui.R.string.msgTopSaveBinFailed), paths[paths.length - 1]);  // 格式化失败消息
//                        if(spaceAvailable == 0) {
//                            resultMsg = mainActivity.getString(com.micsig.tbook.ui.R.string.msgTopSaveCsvNotSpace);
//                        }
                        handlerMsg.saveCallBack.onResult(false, resultMsg);  // 回调通知保存失败
                    }
                    Logger.i(TAG, "Handler_SaveCompleteFailed["  // 记录失败日志
                            + Thread.currentThread().getName() + "]" + handlerMsg.pathFile);
                }
                break;
                case Handler_UDiskUpdate: {  // U盘更新通知
                    uDiskUpdate((String) msg.obj);  // 通知U盘媒体扫描更新
                    Logger.i(TAG, "Handler_UDiskUpdate["  // 记录U盘更新日志
                            + Thread.currentThread().getName() + "]" + (String) msg.obj);
                }
                break;

            }
        }
    }

    /**
     * 通知U盘媒体扫描更新
     * 发送广播让Android系统扫描新文件，使其在文件管理器中可见
     *
     * @param path  文件路径
     * @return true文件存在且广播发送成功，false文件不存在或Activity未初始化
     */
    public boolean uDiskUpdate(String path) {
        if (mainActivity == null) return false;  // Activity未初始化则返回失败
        if (!Tools.fileIsExists(path)) return false;  // 文件不存在则返回失败
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);  // 创建媒体扫描广播Intent
        Uri contentUri = Uri.parse("file://" + path);  // 构造文件URI
        mediaScanIntent.setData(contentUri);  // 设置扫描数据
        mainActivity.sendBroadcast(mediaScanIntent);  // 发送媒体扫描广播

        return Tools.fileIsExists(path);  // 返回文件是否存在（确认保存成功）
    }

    /**
     * 保存成功后的处理
     * 记录日志，跳过SaveRecovery类型文件的提示
     *
     * @param path  保存的文件路径
     */
    private void saveCompleteSuccess(String path) {
        //提示保存的信息
        if (path == null) return;  // 路径为空则返回
        Logger.i(TAG, "send saveCompleteSuccess!" + path);  // 记录成功日志
        String[] s = path.split("/");  // 分割路径获取文件名
        if (s[s.length - 1].contains("SaveRecovery")) return;  // SaveRecovery类型文件不提示
//        DToast.get().show(String.format(mainActivity.getString(com.micsig.tbook.ui.R.string.msgTopSaveBinSuccess), s[s.length - 1]));
    }

    /**
     * 保存失败后的处理
     * 记录日志，跳过SaveRecovery类型文件的提示
     *
     * @param path  保存的文件路径
     */
    private void saveCompleteFailed(String path) {
        //提示储存信息失败
        if (path == null) return;  // 路径为空则返回
        Logger.i(TAG, "send saveCompleteFailed!" + path);  // 记录失败日志
        String[] s = path.split("/");  // 分割路径获取文件名
        if (s[s.length - 1].contains("SaveRecovery")) return;  // SaveRecovery类型文件不提示
//        DToast.get().show(String.format(mainActivity.getString(com.micsig.tbook.ui.R.string.msgTopSaveBinFailed), s[s.length - 1]));
    }

    /**
     * 检查文件是否已存在，若存在则通过Handler通知保存失败
     *
     * @param pathFile       文件完整路径
     * @param saveCallBack   保存结果回调
     * @return true文件已存在（保存被阻止），false文件不存在（可以继续保存）
     */
    private boolean fileExistResultField(String pathFile, SaveCallBack saveCallBack) {
        boolean exist = false;  // 文件存在标志
        File file = new File(pathFile);  // 创建文件对象
        exist = file.exists();  // 检查文件是否存在
        if (exist) {  // 文件已存在
            Message msg = new Message();  // 创建Handler消息
            msg.what = Handler_SaveCompleteFailed;  // 设置消息类型为保存失败
            msg.obj = new UiHandlerMsg(pathFile, saveCallBack);  // 设置消息内容
            uiHandler.sendMessage(msg);  // 发送消息到UI线程
        }
        return exist;  // 返回文件是否存在
    }

    /**
     * 返回保存的ref文件列表，同时返回u盘和本地目录下的列表
     * 合并U盘和本地存储中指定目录下的所有文件
     *
     * @param saveDir  保存目录名
     * @return 合并后的文件数组
     */
    public File[] getFliesFromCurRef(String saveDir) {
        List<String> uDisk = Tools.getAllExternalSdcardPath();  // 获取所有外部SD卡/U盘路径
        String uDiskPath = "", localPath = "";  // U盘路径和本地路径
        if (uDisk.size() > 0) {  // 存在外部存储
            uDiskPath = Tools.resultSavePath(Tools.SaveType_UDISK, saveDir, mainActivity);  // 获取U盘保存路径
        }
        localPath = Tools.resultSavePath(Tools.SaveType_LOCAL, saveDir, mainActivity);  // 获取本地保存路径

        File[] uDiskFileList = new File(uDiskPath).listFiles();  // 获取U盘目录下的文件列表
        File[] localFileList = new File(localPath).listFiles();  // 获取本地目录下的文件列表

        if (uDiskFileList == null) uDiskFileList = new File[0];  // U盘目录为空则设为空数组
        if (localFileList == null) localFileList = new File[0];  // 本地目录为空则设为空数组

        File[] result = new File[uDiskFileList.length + localFileList.length];  // 创建合并后的结果数组
        System.arraycopy(uDiskFileList, 0, result, 0, uDiskFileList.length);  // 复制U盘文件到结果数组前部
        System.arraycopy(localFileList, 0, result, uDiskFileList.length, localFileList.length);  // 复制本地文件到结果数组后部
        return result;  // 返回合并后的文件数组
    }

    /**
     * 获取指定目录下的所有文件（不含子目录）
     *
     * @param saveDir  目录路径
     * @return 文件列表（仅包含非目录文件）
     */
    public ArrayList<File> getFilesFromCur(String saveDir) {
        ArrayList<File> arrFiles = new ArrayList<>();  // 创建结果列表
        File[] files = new File(saveDir).listFiles();  // 获取目录下的文件列表
        if (files == null || files.length == 0) return arrFiles;  // 目录为空则返回空列表
        for (File file : files) {  // 遍历所有文件
            if (!file.isDirectory()) arrFiles.add(file);  // 非目录文件添加到结果列表
        }
        return arrFiles;  // 返回文件列表
    }


    /**
     * 生成新的序列号名字，不保存序列号到cache
     * 根据当前日期和文件类型生成唯一序列号文件名（格式：yyMMddXXXX）
     *
     * @return 生成的序列号文件名
     */
    public String generateName() {
        boolean sbt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);  // 是否为串行总线文本模式
        int saveType = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_TYPE);  // 获取保存类型（0=wav,1=csv,2=bin）
        String cacheData;  // 日期缓存键
        String cacheIndex;  // 序号缓存键
        if (!sbt && saveType == 0) {//wav  // 波形文件
            cacheData = CacheUtil.GENNAME_INDEXDATE_WAV;  // 波形日期缓存键
            cacheIndex = CacheUtil.GENNAME_INDEX_WAV;  // 波形序号缓存键
        } else if (!sbt && saveType == 1) {//csv  // CSV文件
            cacheData = CacheUtil.GENNAME_INDEXDATE_CSV;  // CSV日期缓存键
            cacheIndex = CacheUtil.GENNAME_INDEX_CSV;  // CSV序号缓存键
        } else if (!sbt && saveType == 2) {//bin  // Bin文件
            cacheData = CacheUtil.GENNAME_INDEXDATE_BIN;  // Bin日期缓存键
            cacheIndex = CacheUtil.GENNAME_INDEX_BIN;  // Bin序号缓存键
        } else {//serialsBusText  // 串行总线文本
            cacheData = CacheUtil.GENNAME_INDEXDATE_SBT;  // SBT日期缓存键
            cacheIndex = CacheUtil.GENNAME_INDEX_SBT;  // SBT序号缓存键
        }
        Date date = new Date(System.currentTimeMillis());  // 获取当前时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");  // 日期格式化为yyMMdd
        String str_time = sdf.format(date);  // 格式化当前日期字符串
        if (CacheUtil.get().getOtherMapValue(cacheData).equals(str_time)) {  // 如果缓存日期与当前日期相同
            int index = Integer.parseInt(CacheUtil.get().getOtherMapValue(cacheIndex)) + 1;  // 序号加1
            String sIndex = String.format("%04d", index);  // 格式化为4位序号
            return str_time + sIndex;  // 返回日期+序号
        } else {  // 新的一天，序号从1开始
            String sIndex = String.format("%04d", 1);  // 序号为0001
            return str_time + sIndex;  // 返回日期+序号
        }
    }

//    public String generateName(String oldName) {
//
//    }


    /**
     * 检查名字是否存在
     * 在指定保存目录下检查是否已有同名文件（忽略扩展名）
     *
     * @param saveDir  保存目录名
     * @param name     要检查的文件名（不含扩展名）
     * @return true名字已存在，false名字不存在
     */
    public boolean checkName(String saveDir, String name) {
        File[] files = getFliesFromCurRef(saveDir);  // 获取目录下所有文件
        if (files == null) {  // 文件列表为空
            return false;  // 名字不存在
        }
        for (File file : files) {  // 遍历所有文件
            if (file.getName()  // 获取文件名
                    .replace(".mwav", "")  // 去掉.mwav后缀
                    .replace(".wav", "")  // 去掉.wav后缀
                    .replace(".bin", "")  // 去掉.bin后缀
                    .replace(".csv", "")  // 去掉.csv后缀
                    .equals(name)) {  // 与目标名字比较
                return true;  // 名字已存在
            }
        }
        return false;  // 名字不存在
    }

    /**
     * 检查文件是否存在
     * 直接检查指定路径的文件是否存在
     *
     * @param filePath  文件完整路径
     * @return true文件存在，false文件不存在
     */
    public boolean checkFileExists(String filePath) {
        File file = new File(filePath);  // 创建文件对象
        Logger.i("SaveManage checkFileExists name= " + file.getAbsolutePath() + " ,isExists= " + file.exists());  // 记录检查结果日志
        return file.exists();  // 返回文件是否存在
    }

    /**
     * 检查名字，如果是序列号名字，则保存序列号到cache
     * 当用户确认使用生成的序列号文件名时，更新缓存中的序号计数器
     *
     * @param name  确认使用的文件名
     */
    public void putCacheName(String name) {
        String _name=name;  // 保存处理后的名字
        if (name.contains("_") && name.split("_").length>=2){  // 如果名字包含下划线且有至少两部分
            _name=name.split("_")[1];  // 取下划线后面的部分作为实际名字
        }
        boolean sbt = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_SERIALBUSTXT);  // 是否为串行总线文本模式
        int saveType = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAVE_TYPE);  // 获取保存类型
        String cacheData;  // 日期缓存键
        String cacheIndex;  // 序号缓存键
        if (!sbt && saveType == 0) {//wav  // 波形文件
            cacheData = CacheUtil.GENNAME_INDEXDATE_WAV;  // 波形日期缓存键
            cacheIndex = CacheUtil.GENNAME_INDEX_WAV;  // 波形序号缓存键
        } else if (!sbt && saveType == 1) {//csv  // CSV文件
            cacheData = CacheUtil.GENNAME_INDEXDATE_CSV;  // CSV日期缓存键
            cacheIndex = CacheUtil.GENNAME_INDEX_CSV;  // CSV序号缓存键
        } else if (!sbt && saveType == 2) {//bin  // Bin文件
            cacheData = CacheUtil.GENNAME_INDEXDATE_BIN;  // Bin日期缓存键
            cacheIndex = CacheUtil.GENNAME_INDEX_BIN;  // Bin序号缓存键
        } else {//serialsBusText  // 串行总线文本
            cacheData = CacheUtil.GENNAME_INDEXDATE_SBT;  // SBT日期缓存键
            cacheIndex = CacheUtil.GENNAME_INDEX_SBT;  // SBT序号缓存键
        }
        Date date = new Date(System.currentTimeMillis());  // 获取当前时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");  // 日期格式化为yyMMdd
        String str_time = sdf.format(date);  // 格式化当前日期字符串
        String uName;  // 缓存中计算出的预期名字
        String sIndex;  // 序号字符串
        if (CacheUtil.get().getOtherMapValue(cacheData).equals(str_time)) {  // 如果缓存日期与当前日期相同
            int index = Integer.parseInt(CacheUtil.get().getOtherMapValue(cacheIndex)) + 1;  // 序号加1
            sIndex = String.format("%04d", index);  // 格式化为4位序号
            uName = str_time + sIndex;  // 拼接预期名字
        } else {  // 新的一天
            CacheUtil.get().putOtherMap(cacheData, str_time);  // 更新缓存日期
            sIndex = String.format("%04d", 1);  // 序号为0001
            CacheUtil.get().putOtherMapAndSave(cacheIndex, sIndex);  // 保存新序号到缓存
            uName = str_time + sIndex;  // 拼接预期名字
        }

        if (uName.equals(_name)) {  // 如果预期名字与确认名字一致
            CacheUtil.get().putOtherMapAndSave(cacheIndex, sIndex);  // 保存序号到缓存（确认使用）
        }
    }

    /**
     * 获取默认保存路径的显示字符串
     * 用于UI展示，包含"内部存储空间"前缀
     *
     * @param saveDir  保存目录名
     * @return 带前缀的显示路径字符串
     */
    public String getDefaultPath(String saveDir) {
        String pre = mainActivity.getString(R.string.internal_storage_for_display);  // 获取"内部存储空间"本地化字符串
//        if (pre.equals("内部存储空间")) {
//            pre = "Internal shared storage";
//        }
        return File.separator + pre + Tools.resultDefaultSavePath(saveDir, mainActivity);  // 拼接完整显示路径
    }

    /**
     * 获取绝对默认保存路径
     * 返回不带显示前缀的实际文件系统路径
     *
     * @param saveDir  保存目录名
     * @return 绝对路径字符串
     */
    public String getAbDefaultPath(String saveDir) {
        return Tools.resultAbDefaultSavePath(saveDir, mainActivity);  // 返回绝对默认保存路径
    }
}
