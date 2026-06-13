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

public class PlaySound {
    private static final String TAG = "PlaySound";

    //region  单例
    private static class PlaySoundHolder {
        public static final PlaySound instance = new PlaySound();
    }

    public static PlaySound getInstance() {
        return PlaySound.PlaySoundHolder.instance;
    }
    //endregion

    private SoundPool sp;
    private List<Integer> soundList;
    private int streamID;
    private ExecutorService executorService;
    private PlaySoundRunnable playSoundRunnable;

    public PlaySound() {
        soundList = new ArrayList<Integer>();
        sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        initParam();

        executorService = Executors.newFixedThreadPool(2);
//        executorService.execute();
        playSoundRunnable = new PlaySoundRunnable();
    }

    public void init() {
    }

    private void initParam() {
        if (soundList.size() != 0) return;
        soundList.add(sp.load(App.get().getApplicationContext(), R.raw.gear, 1));
        soundList.add(sp.load(App.get().getApplicationContext(), R.raw.lowvioce, 1));
        soundList.add(sp.load(App.get().getApplicationContext(), R.raw.shake, 1));
        soundList.add(sp.load(App.get().getApplicationContext(), R.raw.startup, 1));
        soundList.add(sp.load(App.get().getApplicationContext(), R.raw.tweet, 1));
        soundList.add(sp.load(App.get().getApplicationContext(), R.raw.xxxxx2, 1));

    }

    class PlaySoundRunnable implements Runnable {
        public int index;

        @Override
        public void run() {
            Thread.currentThread().setName("PlaySound");
            play(index);
        }
    }

    private synchronized boolean play(@IntRange(from = 0, to = 5) int index) {
        if (index >= soundList.size() || index < 0) return false;
        sp.stop(streamID);
        int tem = soundList.get(index);
        streamID = sp.play(tem, 0.8f, 0.8f, 1, 0, 1.0f);
        return streamID != 0;
    }

    public void playButton() {
        if (!CacheUtil.get().isLoadComplete()) return;
        playSoundRunnable.index = 2;

        executorService.execute(playSoundRunnable);
        //play(2);
    }

    public void playKey() {
        play(5);
    }

    public void playSlide() {
        if (!CacheUtil.get().isLoadComplete()) return;
        playSoundRunnable.index = 4;
        executorService.execute(playSoundRunnable);
        //play(4);
    }

    public void playGear() {
        play(0);
    }

    public void playLowVioce() {
        play(1);
    }

    public void playStartUp() {
//        play(3);
    }


}
