package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

import android.app.Activity; // 导入Activity基类
import android.util.Log; // 导入日志工具类
import android.widget.TextView; // 导入TextView控件类

import com.micsig.base.Utils; // 导入基础工具类
import com.micsig.tbook.scope.Data.SaveRecoverySession; // 导入会话保存恢复类
import com.micsig.tbook.scope.Scope; // 导入示波器核心类
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity类
import com.micsig.tbook.tbookscope.R; // 导入资源类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件类
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入RxJava事件枚举
import com.micsig.tbook.tbookscope.tools.FileUtils; // 导入文件工具类
import com.micsig.tbook.tbookscope.tools.SaveManage; // 导入保存管理工具类
import com.micsig.tbook.tbookscope.tools.ScreenControls; // 导入屏幕控制工具类
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.KeyBoardNumberUtil; // 导入数字键盘工具类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.util.DToast; // 导入自定义Toast类
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道定义类

import java.io.File; // 导入文件类
import java.time.LocalDateTime; // 导入本地日期时间类
import java.util.Arrays; // 导入数组工具类
import java.util.Comparator; // 导入比较器接口
import java.util.HashMap; // 导入HashMap类
import java.util.List; // 导入列表接口
import java.util.concurrent.CountDownLatch; // 导入倒计时门闩类
import java.util.concurrent.Executors; // 导入线程池工厂类
import java.util.concurrent.ScheduledExecutorService; // 导入定时线程池接口
import java.util.concurrent.TimeUnit; // 导入时间单位类
import java.util.concurrent.atomic.AtomicInteger; // 导入原子整数类
import java.util.concurrent.atomic.AtomicReference; // 导入原子引用类

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: 保存功能模块 - 自动保存任务管理器                           ║
 * ║  核心职责: 管理自动保存任务的创建、启动、停止和定时调度执行             ║
 * ║  架构设计: 单例模式 + 定时调度线程池，支持多类型文件并发保存            ║
 * ║  数据流向: AutoSaveTaskCondition → AutoSaveTaskManager               ║
 * ║           → SaveManage(实际保存) → RxBus(UI状态更新)                 ║
 * ║  依赖关系: 依赖 AutoSaveTaskCondition/StopCondition/TaskSuffixNumModel║
 * ║           /SaveManage/ScreenControls/RxBus/FileUtils/Command等        ║
 * ║  使用场景: 用户启动自动保存功能后，按配置的时间间隔自动保存各类文件      ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */

/**
 * 自动保存任务管理器
 * <p>单例模式，管理自动保存任务的完整生命周期，包括创建、启动、定时调度和停止</p>
 * <p>支持WAV/CSV/BIN/PICTURE/SESSION五种文件类型的并发保存</p>
 */
public class AutoSaveTaskManager { // 自动保存任务管理器

    /** 单例实例，使用volatile保证多线程可见性 */
    private static volatile AutoSaveTaskManager instance; // 单例实例
    /** 自动保存任务条件配置 */
    private AutoSaveTaskCondition autoSaveTaskCondition; // 自动保存任务条件

    /** 定时调度线程池 */
    private ScheduledExecutorService scheduler; // 定时调度线程池

    /** 倒计时门闩，用于等待所有保存任务完成 */
    private CountDownLatch latch; // 倒计时门闩，同步等待保存完成
    /** 后缀序号模型，用于UI更新序号显示 */
    private TaskSuffixNumModel taskSuffixNumModel; // 后缀序号ViewModel

    /**
     * 构造方法
     * @param autoSaveTaskCondition 自动保存任务条件配置
     * @param txtSuffixNum 后缀序号模型
     */
    public AutoSaveTaskManager(AutoSaveTaskCondition autoSaveTaskCondition, TaskSuffixNumModel txtSuffixNum) { // 构造方法
        this.autoSaveTaskCondition = autoSaveTaskCondition; // 赋值任务条件
        this.taskSuffixNumModel = txtSuffixNum; // 赋值序号模型
    }

    /**
     * 创建或更新自动保存任务管理器单例
     * <p>使用双重检查锁定保证线程安全的单例创建</p>
     * @param autoSaveTaskCondition 自动保存任务条件配置
     * @param txtSuffixNum 后缀序号模型
     * @return AutoSaveTaskManager单例实例
     */
     public  static AutoSaveTaskManager createOrUpdate(AutoSaveTaskCondition autoSaveTaskCondition, TaskSuffixNumModel txtSuffixNum){ // 创建或更新单例
        if(instance==null){ // 第一次检查实例是否为空
            synchronized (AutoSaveTaskManager.class){ // 同步锁保证线程安全
                if(instance==null){ // 第二次检查实例是否为空
                    instance = new AutoSaveTaskManager(autoSaveTaskCondition,txtSuffixNum); // 创建新实例
                }
            }
        }
        instance.updateCondition(autoSaveTaskCondition); // 更新任务条件
        return instance; // 返回单例实例
     }

    /**
     * 获取自动保存任务管理器单例
     * @return 单例实例
     */
     public static AutoSaveTaskManager getInstance(){ // 获取单例实例
        return instance; // 返回单例
     }

    /**
     * 更新自动保存任务条件配置
     * @param autoSaveTaskCondition 新的任务条件配置
     */
     public void updateCondition(AutoSaveTaskCondition autoSaveTaskCondition){ // 更新任务条件
        instance.autoSaveTaskCondition = autoSaveTaskCondition; // 赋值新的任务条件
     }

    /** 原始后缀序号，用于递增计算 */
    String originSuffixNum; // 原始后缀序号

    /** 任务是否已启动标志 */
    private volatile boolean started = false; // 启动标志，volatile保证可见性


    /**
     * 启动自动保存任务
     * <p>创建定时调度线程池，按配置的时间间隔周期性执行保存操作</p>
     * <p>每次保存批次会根据配置的保存类型创建对应目录并执行保存</p>
     */
    public void start() { // 启动自动保存任务
        if (this.scheduler != null && !this.scheduler.isShutdown()) { // 检查调度器是否已在运行
            return; // 已在运行则直接返回
        }
        scheduler = Executors.newScheduledThreadPool(1); // 创建单线程定时调度池
        AtomicInteger batchCounter = new AtomicInteger(1); // 批次计数器，原子操作保证线程安全
        started = true; // 设置启动标志
        originSuffixNum = autoSaveTaskCondition.getSuffixNum(); // 记录原始后缀序号
        long intervalTime = autoSaveTaskCondition.getIntervalTime().getTime(); // 获取时间间隔毫秒值
        List<AutoSaveTaskCondition.SaveType> saveTypes = autoSaveTaskCondition.getSaveType(); // 获取保存类型列表
        int chanNum = autoSaveTaskCondition.getSelectedChannel().size(); // 获取选中通道数量
        RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE,false); // 通知UI更新按钮状态为"运行中"
        int totalTasks = 0; // 总任务数计数
        if (saveTypes.contains(AutoSaveTaskCondition.SaveType.CSV)) { // 如果包含CSV保存类型
            totalTasks += 1; // CSV只保存一个文件
        }
        if (saveTypes.contains(AutoSaveTaskCondition.SaveType.WAV)) { // 如果包含WAV保存类型
            totalTasks += chanNum; // WAV按通道数保存
        }
        if (saveTypes.contains(AutoSaveTaskCondition.SaveType.BIN)) { // 如果包含BIN保存类型
            totalTasks += chanNum; // BIN按通道数保存
        }
        if (saveTypes.contains(AutoSaveTaskCondition.SaveType.PICTURE)) { // 如果包含截图保存类型
            totalTasks += 1; // 截图只保存一个文件
        }
        if (saveTypes.contains(AutoSaveTaskCondition.SaveType.SESSION)) { // 如果包含会话保存类型
            totalTasks += 1; // 会话只保存一个文件
        }
        int finalTotalTasks = totalTasks; // 最终总任务数，用于lambda表达式
        scheduler.scheduleWithFixedDelay(() -> { // 启动定时调度任务
            boolean userTouch = CacheUtil.get().getBoolean(CacheUtil.USER_TOUCH); // 检查用户是否正在触摸屏幕
            Log.d("TAG", "start: "+userTouch); // 打印触摸状态日志
            if(userTouch){ // 如果用户正在触摸屏幕
                return; // 跳过本次保存，避免干扰用户操作
            }
            LocalDateTime now = LocalDateTime.now(); // 获取当前时间
            if(now.isBefore(autoSaveTaskCondition.getStartTime())){ // 如果当前时间早于开始时间
                return; // 未到开始时间，跳过本次保存
            }
            ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
            screenControls.lockScreen(ScreenControls.LOCK_PROGRESS); // 锁定屏幕进度条
            if (!started) return; // 如果已停止则直接返回

            File taskDir = new File(autoSaveTaskCondition.getSavePath() +"/"+ autoSaveTaskCondition.getSaveFileName()); // 创建任务目录
            taskDir.mkdirs(); // 确保任务目录存在
            latch = new CountDownLatch(finalTotalTasks); // 创建倒计时门闩，等待所有保存任务完成
            if (saveTypes.contains(AutoSaveTaskCondition.SaveType.CSV)) { // 如果包含CSV保存类型
                File csvDir = new File(taskDir+ "/" + autoSaveTaskCondition.getStartTime().toString() // 创建CSV子目录
                        .replace("-","_") // 替换横杠为下划线
                        .replace(":","")+"_csv") ; // 移除冒号并添加_csv后缀
                csvDir.mkdirs(); // 确保CSV目录存在
                doSaveWaveCSV(csvDir.getPath()); // 执行CSV保存
            }
            if (saveTypes.contains(AutoSaveTaskCondition.SaveType.WAV)) { // 如果包含WAV保存类型
                File wavDir = new File(taskDir+ "/" + autoSaveTaskCondition.getStartTime().toString() // 创建WAV子目录
                        .replace("-","_") // 替换横杠为下划线
                        .replace(":","")+"_wav"); // 移除冒号并添加_wav后缀
                wavDir.mkdirs(); // 确保WAV目录存在
                doSaveWaveWav(wavDir.getPath()); // 执行WAV保存
            }
            if (saveTypes.contains(AutoSaveTaskCondition.SaveType.BIN)) { // 如果包含BIN保存类型
                File binDir = new File(taskDir+ "/" + autoSaveTaskCondition.getStartTime().toString() // 创建BIN子目录
                        .replace("-","_") // 替换横杠为下划线
                        .replace(":","")+"_bin"); // 移除冒号并添加_bin后缀
                binDir.mkdirs(); // 确保BIN目录存在
                doSaveWaveBin(binDir.getPath()); // 执行BIN保存
            }
            if (saveTypes.contains(AutoSaveTaskCondition.SaveType.SESSION)) { // 如果包含SESSION保存类型
                File sessionDir = new File(taskDir+ "/" + autoSaveTaskCondition.getStartTime().toString() // 创建SESSION子目录
                        .replace("-","_") // 替换横杠为下划线
                        .replace(":","")+"_session" ); // 移除冒号并添加_session后缀
                sessionDir.mkdirs(); // 确保SESSION目录存在
                doSaveSession(sessionDir.getPath()); // 执行SESSION保存
            }
            if(saveTypes.contains(AutoSaveTaskCondition.SaveType.PICTURE)){ // 如果包含PICTURE保存类型
                File pictureDir = new File(taskDir+ "/" + autoSaveTaskCondition.getStartTime().toString() // 创建PICTURE子目录
                        .replace("-","_") // 替换横杠为下划线
                        .replace(":","")+"_picture" ); // 移除冒号并添加_picture后缀
                pictureDir.mkdirs(); // 确保PICTURE目录存在
                doSavePicture(pictureDir.getPath()); // 执行截图保存
            }
            try { // 捕获中断异常
                latch.await(); // 等待所有保存任务完成
                autoAddSuffixNum(); // 自动递增后缀序号
                StopCondition stopCondition = autoSaveTaskCondition.getStopCondition(); // 获取停止条件
                RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_STOP_BUTTON_STATE,true); // 通知UI更新停止按钮状态
                if(stopCondition.getType().equals(StopCondition.StopConditionType.TIME)){ // 如果停止条件为按时间
                    if(now.isAfter(LocalDateTime.parse(stopCondition.getValue()))){ // 如果当前时间已超过指定停止时间
                        RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE,true); // 通知UI更新按钮状态为"已停止"
                        stop(); // 停止自动保存
                    }
                }else if(stopCondition.getType().equals(StopCondition.StopConditionType.AFTER_N_FRAME)){ // 如果停止条件为按帧数
                    if(batchCounter.get()==Integer.parseInt(stopCondition.getValue())){ // 如果已保存帧数达到指定帧数
                        RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE,true); // 通知UI更新按钮状态为"已停止"
                        stop(); // 停止自动保存
                    }
                }
                if(autoSaveTaskCondition.getSaveMode().equals(AutoSaveTaskCondition.SaveMode.FULL_WHEN_STOP)){ // 如果保存模式为停机时全量保存
                    if(noEnoughSpace()){ // 如果磁盘空间不足
                        RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE,true); // 通知UI更新按钮状态为"已停止"
                        stop(); // 停止自动保存
                    }
                }
                batchCounter.getAndIncrement(); // 批次计数器递增
                screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS); // 解锁屏幕进度条
            } catch (InterruptedException e) { // 捕获中断异常
                throw new RuntimeException(e); // 包装为运行时异常抛出
            }

        }, 0, intervalTime, TimeUnit.MILLISECONDS); // 初始延迟0毫秒，按间隔时间周期执行
    }

    /**
     * 停止自动保存任务
     * <p>关闭定时调度线程池，重置序号显示，通知UI更新按钮状态</p>
     */
    public void stop() { // 停止自动保存任务
        started = false; // 重置启动标志
        if (scheduler != null) { // 如果调度器不为空
            scheduler.shutdown(); // 优雅关闭调度器
            try { // 捕获中断异常
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) { // 等待5秒让任务完成
                    this.scheduler.shutdown(); // 超时则强制关闭
                }
            } catch (InterruptedException e) { // 捕获中断异常
                Thread.currentThread().interrupt(); // 恢复中断状态
            }
        }
        taskSuffixNumModel.updateText("0000000"); // 重置序号显示为初始值
        RxBus.getInstance().post(RxEnum.MSG_UPDATE_AUTO_SAVE_BUTTON_STATE,true); // 通知UI更新按钮状态为"已停止"
        scheduler = null; // 清空调度器引用
    }

    /**
     * 执行WAV波形文件保存
     * <p>遍历选中通道，逐通道保存WAV格式波形数据</p>
     * @param filePath WAV文件保存目录路径
     */
    private void doSaveWaveWav(String filePath) { // 执行WAV波形文件保存
        String input = autoSaveTaskCondition.getSaveFileName(); // 获取保存文件名
        List<Integer> selectList = autoSaveTaskCondition.getSelectedChannel();//保存CSV时选中的channel
        ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
        screenControls.lockScreen(ScreenControls.LOCK_PROGRESS); // 锁定屏幕进度条

        for (int i = 0; i < selectList.size(); i++) { // 遍历选中通道
            final int ch = selectList.get(i); // 获取当前通道索引
            String finalInput = input + "_" + originSuffixNum + "_" + getChName(ch); // 组合最终文件名：文件名_序号_通道名
            Log.d("SaveWav", "doSaveWaveWav: " + ch); // 打印保存通道日志
            SaveManage.getInstance().allSaveEntrance(selectList.get(i), 0, filePath, finalInput, null, new SaveManage.SaveCallBack() { // 调用保存入口，类型0=WAV
                @Override
                public void onResult(boolean success, String msg) { // 保存结果回调
                    if (success) { // 如果保存成功
                        SaveManage.getInstance().putCacheName(finalInput); // 缓存保存的文件名
                        FileUtils.deleteFile(filePath + FileUtils.BACKUP_FILE_SUFFIX); // 删除备份文件
                    } else { // 如果保存失败
                        FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX); // 从备份文件恢复
                    }
                    latch.countDown(); // 倒计时减1，表示一个保存任务完成
                    if(noEnoughSpace()){ // 如果磁盘空间不足
                        File wavDir = new File(filePath); // 获取WAV目录
                        deleteOldstFile(wavDir); // 删除最旧的文件释放空间
                    }
                }
            });
            Command.get().getStorage().Save_Filename(finalInput, false); // 设置底层保存文件名
        }
    }

    /**
     * 执行BIN二进制文件保存
     * <p>遍历选中通道，逐通道保存BIN格式数据（Ch8及以上通道不支持BIN保存）</p>
     * @param filePath BIN文件保存目录路径
     */
    private void doSaveWaveBin(String filePath) { // 执行BIN二进制文件保存
        String input = autoSaveTaskCondition.getSaveFileName(); // 获取保存文件名
        List<Integer> selectList = autoSaveTaskCondition.getSelectedChannel();//保存CSV时选中的channel
        ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
        screenControls.lockScreen(ScreenControls.LOCK_PROGRESS); // 锁定屏幕进度条

        for (int i = 0; i < selectList.size(); i++) { // 遍历选中通道
            final int ch = selectList.get(i); // 获取当前通道索引
            Log.d("SaveWav", "doSaveWaveWav: " + ch); // 打印保存通道日志

            if(ch>=TChan.Ch8){ // 如果通道号大于等于Ch8
                latch.countDown(); // 倒计时减1，跳过该通道
                continue; // 跳过不支持的通道
            }
            String finalInput = input+"_" + originSuffixNum +"_" + getChName(ch); // 组合最终文件名：文件名_序号_通道名
            Log.d("SaveWav", "doSaveWaveWav: " + finalInput); // 打印保存文件名日志
            SaveManage.getInstance().allSaveEntrance(selectList.get(i), 2, filePath, finalInput, null, new SaveManage.SaveCallBack() { // 调用保存入口，类型2=BIN
                @Override
                public void onResult(boolean success, String msg) { // 保存结果回调
                    if (success) { // 如果保存成功
                        SaveManage.getInstance().putCacheName(finalInput); // 缓存保存的文件名
                        FileUtils.deleteFile(filePath + FileUtils.BACKUP_FILE_SUFFIX); // 删除备份文件
                    } else { // 如果保存失败
                        FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX); // 从备份文件恢复
                    }
                    latch.countDown(); // 倒计时减1，表示一个保存任务完成
                    if(noEnoughSpace()){ // 如果磁盘空间不足
                        File wavDir = new File(filePath); // 获取BIN目录
                        deleteOldstFile(wavDir); // 删除最旧的文件释放空间
                    }
                }
            });
            Command.get().getStorage().Save_Filename(finalInput, false); // 设置底层保存文件名
        }
    }


    /**
     * 执行CSV数据文件保存
     * <p>将所有选中通道的波形数据保存为CSV格式</p>
     * @param filePath CSV文件保存目录路径
     */
    private void doSaveWaveCSV(String filePath) { // 执行CSV数据文件保存
        String finalInput = autoSaveTaskCondition.getSaveFileName() + "_" + originSuffixNum; // 组合最终文件名：文件名_序号
        List<Integer> selectList = autoSaveTaskCondition.getSelectedChannel();//保存CSV时选中的channel

        ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
        screenControls.lockScreen(ScreenControls.LOCK_PROGRESS); // 锁定屏幕进度条

        SaveManage.getInstance().allSaveEntrance(0, 1, filePath, finalInput, selectList, new SaveManage.SaveCallBack() { // 调用保存入口，类型1=CSV
            @Override
            public void onResult(boolean success, String msg) { // 保存结果回调
                if (success) { // 如果保存成功
                    SaveManage.getInstance().putCacheName(finalInput); // 缓存保存的文件名
                    FileUtils.deleteFile(filePath + FileUtils.BACKUP_FILE_SUFFIX); // 删除备份文件
                } else { // 如果保存失败
                    FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX); // 从备份文件恢复
                }
                latch.countDown(); // 倒计时减1，表示保存任务完成
                if(noEnoughSpace()){ // 如果磁盘空间不足
                    File wavDir = new File(filePath); // 获取CSV目录
                    deleteOldstFile(wavDir); // 删除最旧的文件释放空间
                }
            }
        });
    }


    /**
     * 执行截图保存
     * <p>通过RxBus发送截图指令，由截图模块执行实际保存</p>
     * @param filePath 截图保存目录路径
     */
    private void doSavePicture(String filePath) { // 执行截图保存
        String finalInput = autoSaveTaskCondition.getSaveFileName() + "_" + originSuffixNum; // 组合最终文件名：文件名_序号
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_PATH_AUTO_SAVE,filePath); // 缓存截图保存路径
        CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAVE_PICTURE_AUTO_SAVE_NAME,finalInput); // 缓存截图文件名
        RxBus.getInstance().post(RxEnum.MSG_AUTO_SAVE_INVOKE_SCREENSHOT,true); // 发送自动保存截图事件
        latch.countDown(); // 倒计时减1，截图指令已发送
    }


    /**
     * 自动递增文件名后缀序号
     * <p>将当前序号加1并格式化为7位数字，更新UI显示</p>
     */
    private void autoAddSuffixNum() {//文件名序号递增
        int oldSuffixNum = Integer.parseInt(originSuffixNum.trim()); // 解析原始序号为整数
        String tempNum = KeyBoardNumberUtil.toBits((oldSuffixNum + 1) + "", 7); // 加1后格式化为7位数字
        originSuffixNum = tempNum; // 更新原始序号
        taskSuffixNumModel.updateText(tempNum); // 更新UI显示的序号
    }


    /**
     * 执行会话文件保存
     * <p>停止示波器运行，估算存储空间，保存会话数据后恢复运行状态</p>
     * @param filePath 会话文件保存目录路径
     */
    private void doSaveSession(String filePath) { // 执行会话文件保存
        boolean selectIsFast32 = false; // 是否选择Fast32模式（当前硬编码为false）

        Scope scope = Scope.getInstance(); // 获取示波器实例
        boolean oldIsRun = scope.isRun(); // 记录当前运行状态
        if (scope.isRun()) { // 如果示波器正在运行
            Command.get().getFunctionMenu().Stop(true); // 停止运行
        } else { // 如果示波器未在运行
            Command.get().getSample().SegmentedStop(true); // 停止分段采集
        }
        int channelSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT); // 获取当前通道选择
        CacheUtil.get().putMapInForce(CacheUtil.MAIN_RECOVERY_CHANNEL_SELECT + "-1", String.valueOf(channelSelect)); // 保存通道选择到缓存
        AtomicReference<String> toastStr = new AtomicReference<>(""); // Toast消息引用

        new Thread(() -> { // 在新线程中执行会话保存
            ms_sleep(1000); // 等待1秒确保停止完成
            SaveRecoverySession saveRecoverySession = SaveRecoverySession.getInstance(); // 获取会话保存恢复实例
            long needSize = saveRecoverySession.estimateStorage(); // 估算所需存储空间
            boolean canSave = Utils.isDiskAvaiable(new File(filePath), needSize); // 检查磁盘空间是否足够
            if (selectIsFast32) { // 如果是Fast32模式
                if (needSize >= 4 * 1024 * 1024 * 1024L) { // 如果所需空间超过4GB
                    noEnoughSpace(true, scope, oldIsRun, filePath); // 处理空间不足
                    return; // 终止保存
                }
            }
            if (!canSave) { // 如果磁盘空间不足
                noEnoughSpace(false, scope, oldIsRun, filePath); // 处理空间不足
                return; // 终止保存
            }
            HashMap<String, HashMap<String, String>> map = new HashMap<>(); // 创建会话数据映射
            map.put(CacheUtil.DefaultSaveName, CacheUtil.get().getCurrMap()); // 保存默认缓存数据
            map.put(CacheUtil.OtherDefaultSaveName, CacheUtil.get().getCurrOtherMap()); // 保存其他缓存数据
            saveRecoverySession.store(map, filePath+"/"+autoSaveTaskCondition.getSaveFileName()+"_"+originSuffixNum+".mss"); // 执行会话保存，文件后缀.mss
            while (!saveRecoverySession.isDone()) { // 等待保存完成
                ms_sleep(100); // 每100毫秒检查一次
                Log.d("SaveRecoverySession", "store progress:" + saveRecoverySession.getSaveRecoveryProgress()); // 打印保存进度
                showSaveProgress(saveRecoverySession.getSaveRecoveryProgress()); // 显示保存进度
            }
//            toastStr = context.getResources().getString(R.string.top_slip_save_session_success);
            boolean saveSuccess = true; // 保存成功标志
            if (saveRecoverySession.getStatus() == SaveRecoverySession.S_FAIL) { // 如果保存状态为失败
                saveSuccess = false; // 标记保存失败
                showSaveProgress(100); // 显示进度100%（结束进度条）
            }
            boolean finalSaveSuccess = saveSuccess; // 最终保存成功标志
            boolean finalOldState = oldIsRun; // 最终旧运行状态

            Log.d("SaveRecoverySession", toastStr.get()); // 打印Toast消息日志
            ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
            if (screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) { // 如果屏幕进度条被锁定
                screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS); // 解锁屏幕进度条
            }
            if (scope.isRun() != finalOldState) { // 如果当前运行状态与原始状态不同
                //scope.setRun(finalOldState);
                if (finalOldState) { // 如果原始状态为运行
                    Command.get().getFunctionMenu().Run(true); // 恢复运行
                } else { // 如果原始状态为停止
                    Command.get().getFunctionMenu().Stop(true); // 恢复停止
                }
            }
            if (finalSaveSuccess) { //保存成功
                FileUtils.deleteFile(filePath + FileUtils.BACKUP_FILE_SUFFIX); // 删除备份文件
            } else { //保存失败
                FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX); // 从备份文件恢复
            }
        }).start(); // 启动保存线程
//        DToast.get().show(toastStr.get());
        latch.countDown(); // 倒计时减1，会话保存任务已提交
        if(noEnoughSpace()){ // 如果磁盘空间不足
            File wavDir = new File(filePath); // 获取会话目录
            deleteOldstFile(wavDir); // 删除最旧的文件释放空间
        }
    }

    /**
     * 显示保存进度
     * <p>根据进度值控制屏幕锁定状态和进度条显示</p>
     * @param progress 保存进度百分比（0-100）
     */
    private void showSaveProgress(int progress) { // 显示保存进度

        ScreenControls screenControls = ScreenControls.getInstance(); // 获取屏幕控制实例
        if (progress < 0 || progress >= 100) { // 如果进度无效或已完成
            if (screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) { // 如果屏幕被锁定
                screenControls.unLockScreen(ScreenControls.LOCK_PROGRESS); // 解锁屏幕
            }
        } else { // 如果进度在进行中
            if (!screenControls.isLockScreen(ScreenControls.LOCK_PROGRESS)) { // 如果屏幕未被锁定
                screenControls.lockScreen(ScreenControls.LOCK_PROGRESS); // 锁定屏幕
            }
            screenControls.setProgressValue(progress); // 设置进度值
        }
        ; // 空语句
    }

    /**
     * 线程休眠工具方法
     * @param ms 休眠时间（毫秒）
     */
    private void ms_sleep(long ms) { // 线程休眠
        try { // 捕获中断异常
            Thread.sleep(ms); // 让当前线程休眠指定毫秒数
        } catch (InterruptedException e) { // 捕获中断异常
            throw new RuntimeException(e); // 包装为运行时异常抛出
        }
    }

    /**
     * 恢复示波器运行状态
     * @param scope 示波器实例
     * @param finalOldState 原始运行状态
     */
    private void recoveryScopeState(Scope scope, boolean finalOldState) { // 恢复示波器运行状态
        if (scope.isRun() == finalOldState) return; // 如果状态一致则无需恢复
        if (finalOldState) { // 如果原始状态为运行
            Command.get().getFunctionMenu().Run(true); // 恢复运行
        } else { // 如果原始状态为停止
            Command.get().getFunctionMenu().Stop(true); // 恢复停止
        }
    }

    /**
     * 处理磁盘空间不足的情况
     * @param isCausedByFast32 是否因Fast32模式导致
     * @param scope 示波器实例
     * @param oldIsRun 原始运行状态
     * @param filePath 文件路径
     */
    private void noEnoughSpace(boolean isCausedByFast32, Scope scope, boolean oldIsRun, String filePath) { // 处理空间不足
        recoveryScopeState(scope, oldIsRun); // 恢复示波器运行状态
        FileUtils.restoreFromBakFile(filePath + FileUtils.BACKUP_FILE_SUFFIX); // 从备份文件恢复
    }

    /**
     * 检查磁盘空间是否不足
     * <p>当可用空间小于10GB时判定为空间不足</p>
     * @return true表示空间不足，false表示空间充足
     */
    public boolean noEnoughSpace(){ // 检查磁盘空间是否不足
        File saveDir = new File(autoSaveTaskCondition.getSavePath()); // 获取保存目录
        long freeSpace = saveDir.getFreeSpace(); // 获取可用空间大小
//        Log.d("TAG", "noEnoughSpace: "+freeSpace);
        long estimatedSize = 10 * 1024 * 1024L *1024; // 阈值：10GB
        return freeSpace < estimatedSize; // 可用空间小于10GB则返回true
    }

    /**
     * 删除目录中最旧的文件以释放空间
     * @param storageDir 存储目录
     * @return true表示删除成功
     */
    private boolean deleteOldstFile(File storageDir){ // 删除最旧文件
        File[] files = storageDir.listFiles(); // 获取目录下所有文件
        Arrays.sort(files, Comparator.comparingLong(File::lastModified)); // 按修改时间排序
        return files[0].delete(); // 删除最旧的文件
    }

    /**
     * 根据通道索引获取通道名称
     * <p>Ch1-Ch8、Math1-Math8、Ref1-Ref8</p>
     * @param ch 通道索引（0-based）
     * @return 通道名称字符串
     */
    private String getChName(int ch){ // 获取通道名称
        ch = ch+1; // 通道索引转为1-based
        if(ch >= TChan.Ch1 && ch<=TChan.Ch8){ // 如果是CH通道
            return "CH" + ch; // 返回CHx格式
        }
        else if(ch>= TChan.Math1 && ch<= TChan.Math8){ // 如果是Math通道
            return "Math"+ (ch-8); // 返回Mathx格式
        }else if(ch>=TChan.R1 && ch<= TChan.R8){ // 如果是Ref通道
            return "Ref" +(ch-16); // 返回Refx格式
        }
        return ""; // 未知通道返回空字符串
    }
}
