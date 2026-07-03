package com.micsig.tbook.tbookscope.tools;

import android.media.AudioManager;
import android.media.SoundPool;

import androidx.annotation.IntRange;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.tbookscope.util.CacheUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by liwb on 2018/3/22.
 * <pre class ="prettyprint">
 *     PlaySound 声音的播放，此类为单例，调用：PlaySound.getInstance().playButton();
 *     public void playButton(); 按钮点击声音
 *     public void playSlide();  滑动菜单声音
 *     public void playStartUp(); 开机声音
 *     public void playKey();     外部按键声音
 *     public void playGear();   目前没有使用
 *     public void playLowVioce() 目前没有使用
 * </pre>
 */

/**
 * +-----------------------------------------------------------------------------+
 * |                         声音播放工具类 (PlaySound)                            |
 * +-----------------------------------------------------------------------------+
 * | 模块定位 : tbookscope.tools 通用工具层                                      |
 * | 核心职责 : 基于 SoundPool 管理和播放各类 UI 交互音效                         |
 * | 架构设计 : 单例模式（静态内部类持有），异步线程池播放音效                     |
 * | 数据流向 : 资源文件 → SoundPool 加载 → soundList 索引 → 线程池异步播放      |
 * | 依赖关系 : android.media.SoundPool, android.media.AudioManager,              |
 * |            com.micsig.tbook.tbookscope.util.App,                             |
 * |            com.micsig.tbook.tbookscope.util.CacheUtil                        |
 * | 使用场景 : 按钮点击音效、滑动菜单音效、开机音效、外部按键音效等               |
 * +-----------------------------------------------------------------------------+
 */
public class PlaySound {
    /** 日志标签 */ // 日志标签常量
    private static final String TAG = "PlaySound"; // 日志TAG为 PlaySound

    //region  单例
    /** 静态内部类持有单例实例，实现懒加载 */ // 单例持有者
    private static class PlaySoundHolder { // 静态内部类实现单例
        /** 单例实例 */ // 单例实例
        public static final PlaySound instance = new PlaySound(); // 创建唯一实例
    }

    /**
     * 获取 PlaySound 单例实例
     *
     * @return PlaySound 单例对象
     */
    public static PlaySound getInstance() { // 获取单例实例
        return PlaySound.PlaySoundHolder.instance; // 返回静态内部类持有的实例
    }
    //endregion

    /** SoundPool 音效播放器 */ // SoundPool 对象
    private SoundPool sp; // 音效池
    /** 音效资源ID列表，索引对应不同音效 */ // 音效资源ID列表
    private List<Integer> soundList; // 存放加载后的音效ID
    /** 当前播放的音频流ID */ // 当前播放流ID
    private int streamID; // SoundPool 播放返回的流ID
    /** 线程池，用于异步播放音效 */ // 线程池
    private ExecutorService executorService; // 固定大小线程池
    /** 音效播放任务 */ // 播放音效的 Runnable
    private PlaySoundRunnable playSoundRunnable; // 可复用的播放任务对象

    /**
     * 构造函数，初始化 SoundPool、加载音效资源、创建线程池
     */
    public PlaySound() { // 构造方法
        soundList = new ArrayList<Integer>(); // 初始化音效ID列表
        sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0); // 创建 SoundPool，最多5个并发流，使用音乐音频流
        initParam(); // 初始化音效资源参数

        executorService = Executors.newFixedThreadPool(2); // 创建固定大小为2的线程池
//        executorService.execute();
        playSoundRunnable = new PlaySoundRunnable(); // 创建可复用的播放任务对象
    }

    /**
     * 初始化方法（当前为空实现）
     */
    public void init() { // 初始化方法（预留）
    }

    /**
     * 加载所有音效资源到 SoundPool 并将音效ID存入列表
     * <p>音效索引映射：0=gear, 1=lowvioce, 2=shake, 3=startup, 4=tweet, 5=xxxxx2</p>
     */
    private void initParam() { // 初始化音效资源参数
        if (soundList.size() != 0) return; // 已加载过则不再重复加载
        soundList.add(sp.load(App.get().getApplicationContext(), R.raw.gear, 1)); // 索引0：齿轮音效
        soundList.add(sp.load(App.get().getApplicationContext(), R.raw.lowvioce, 1)); // 索引1：低音量提示音效
        soundList.add(sp.load(App.get().getApplicationContext(), R.raw.shake, 1)); // 索引2：震动/按钮点击音效
        soundList.add(sp.load(App.get().getApplicationContext(), R.raw.startup, 1)); // 索引3：开机音效
        soundList.add(sp.load(App.get().getApplicationContext(), R.raw.tweet, 1)); // 索引4：滑动/鸟鸣音效
        soundList.add(sp.load(App.get().getApplicationContext(), R.raw.xxxxx2, 1)); // 索引5：按键音效

    }

    /**
     * 音效播放任务，在线程池中异步执行音效播放
     */
    class PlaySoundRunnable implements Runnable { // 音效播放的 Runnable 实现
        /** 要播放的音效索引 */ // 音效索引
        public int index; // 指定播放哪个音效

        @Override
        public void run() { // 线程执行体
            Thread.currentThread().setName("PlaySound"); // 设置当前线程名称为 PlaySound
            play(index); // 调用同步播放方法
        }
    }

    /**
     * 同步播放指定索引的音效
     *
     * @param index 音效索引，范围 0~5
     * @return 播放成功返回 true，索引越界或播放失败返回 false
     */
    private synchronized boolean play(@IntRange(from = 0, to = 5) int index) { // 同步播放音效方法
        if (index >= soundList.size() || index < 0) return false; // 索引越界检查
        sp.stop(streamID); // 停止当前正在播放的音效
        int tem = soundList.get(index); // 获取指定索引的音效ID
        streamID = sp.play(tem, 0.8f, 0.8f, 1, 0, 1.0f); // 播放音效，左右声道音量0.8，优先级1，不循环，正常速率
        return streamID != 0; // streamID 非0表示播放成功
    }

    /**
     * 播放按钮点击音效（shake）
     * <p>仅在加载完成后播放，通过线程池异步执行</p>
     */
    public void playButton() { // 播放按钮点击音效
        if (!CacheUtil.get().isLoadComplete()) return; // 未加载完成则不播放
        playSoundRunnable.index = 2; // 设置音效索引为2（shake）

        executorService.execute(playSoundRunnable); // 提交到线程池异步播放
        //play(2);
    }

    /**
     * 播放外部按键音效（xxxxx2）
     */
    public void playKey() { // 播放外部按键音效
        play(5); // 直接同步播放索引5（xxxxx2）
    }

    /**
     * 播放滑动菜单音效（tweet）
     * <p>仅在加载完成后播放，通过线程池异步执行</p>
     */
    public void playSlide() { // 播放滑动菜单音效
        if (!CacheUtil.get().isLoadComplete()) return; // 未加载完成则不播放
        playSoundRunnable.index = 4; // 设置音效索引为4（tweet）
        executorService.execute(playSoundRunnable); // 提交到线程池异步播放
        //play(4);
    }

    /**
     * 播放齿轮音效（gear）
     */
    public void playGear() { // 播放齿轮音效
        play(0); // 直接同步播放索引0（gear）
    }

    /**
     * 播放低音量提示音效（lowvioce）
     */
    public void playLowVioce() { // 播放低音量提示音效
        play(1); // 直接同步播放索引1（lowvioce）
    }

    /**
     * 播放开机音效（startup）
     * <p>当前已禁用，方法体为空</p>
     */
    public void playStartUp() { // 播放开机音效（当前已禁用）
//        play(3);
    }


}
