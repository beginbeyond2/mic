/*
 * Copyright (c) 2017 Yrom Wang <http://www.yrom.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.micsig.tbook.ui.util.screenRecorder;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;
import static android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME;
import static android.media.MediaCodec.INFO_OUTPUT_FORMAT_CHANGED;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.SparseLongArray;

import com.micsig.base.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 麦克风录音器
 * 
 * <p>实现{@link Encoder}接口，负责从麦克风采集音频数据并进行AAC编码。
 * 该类是屏幕录制功能中音频录制的核心组件，使用HandlerThread实现异步录音。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>从麦克风采集PCM音频数据</li>
 *   <li>使用AudioEncoder进行AAC编码</li>
 *   <li>管理录音线程和编码流程</li>
 *   <li>处理音频帧的时间戳计算</li>
 * </ul>
 * 
 * <p>工作流程：</p>
 * <ol>
 *   <li>创建MicRecorder实例</li>
 *   <li>设置回调监听器</li>
 *   <li>调用prepare()准备录音</li>
 *   <li>自动开始录音和编码</li>
 *   <li>调用stop()停止录音</li>
 *   <li>调用release()释放资源</li>
 * </ol>
 * 
 * <p>线程模型：</p>
 * <ul>
 *   <li>主线程：创建实例、调用prepare/stop/release</li>
 *   <li>录音线程(HandlerThread)：音频采集、编码输入/输出</li>
 *   <li>回调线程：输出编码数据给调用者</li>
 * </ul>
 * 
 * @author yrom
 * @version 2017/12/4
 * @since 1.0
 * @see Encoder
 * @see AudioEncoder
 * @see AudioEncodeConfig
 */
class MicRecorder implements Encoder {
    
    /** 日志标签 */
    private static final String TAG = "MicRecorder";
    
    /** 是否输出详细日志 */
    private static final boolean VERBOSE = false;

    /** 音频编码器实例 */
    private final AudioEncoder mEncoder;
    
    /** 录音线程，用于执行音频采集和编码操作 */
    private final HandlerThread mRecordThread;
    
    /** 录音Handler，处理录音线程的消息 */
    private RecordHandler mRecordHandler;
    
    /** AudioRecord实例，用于采集麦克风音频，仅在mRecordThread中访问 */
    private AudioRecord mMic; // access in mRecordThread only!
    
    /** 采样率，单位Hz */
    private int mSampleRate;
    
    /** 声道配置，单声道或立体声 */
    private int mChannelConfig;
    
    /** 音频格式，默认为PCM 16位 */
    private int mFormat = AudioFormat.ENCODING_PCM_16BIT;

    /** 强制停止标志，线程安全 */
    private AtomicBoolean mForceStop = new AtomicBoolean(false);
    
    /** 编码器回调接口 */
    private BaseEncoder.Callback mCallback;
    
    /** 回调代理，用于在调用者线程执行回调 */
    private CallbackDelegate mCallbackDelegate;
    
    /** 声道数与采样率的乘积，用于时间戳计算 */
    private int mChannelsSampleRate;

    /**
     * 构造函数
     * 
     * <p>根据配置初始化麦克风录音器。创建AudioEncoder实例和录音线程。</p>
     * 
     * @param config 音频编码配置，包含采样率、声道数等参数
     */
    MicRecorder(AudioEncodeConfig config) {
        // 创建音频编码器
        mEncoder = new AudioEncoder(config);
        
        // 保存采样率
        mSampleRate = config.sampleRate;
        
        // 计算声道采样率乘积，用于时间戳计算
        mChannelsSampleRate = mSampleRate * config.channelCount;
        
        if (VERBOSE) Logger.i(TAG, "in bitrate " + mChannelsSampleRate * 16 /* PCM_16BIT*/);
        
        // 根据声道数设置声道配置
        mChannelConfig = config.channelCount == 2 ? AudioFormat.CHANNEL_IN_STEREO : AudioFormat.CHANNEL_IN_MONO;
        
        // 创建录音线程
        mRecordThread = new HandlerThread(TAG);
    }

    /**
     * 设置回调监听器
     * 
     * <p>实现Encoder接口方法，设置编码器回调。</p>
     * 
     * @param callback 回调接口实例
     */
    @Override
    public void setCallback(Callback callback) {
        this.mCallback = (BaseEncoder.Callback) callback;
    }

    /**
     * 设置回调监听器
     * 
     * <p>设置BaseEncoder回调接口，用于接收编码输出数据。</p>
     * 
     * @param callback 回调接口实例
     */
    public void setCallback(BaseEncoder.Callback callback) {
        this.mCallback = callback;
    }

    /**
     * 准备录音
     * 
     * <p>初始化录音器和编码器。必须在有Looper的线程中调用（通常是主线程）。
     * 启动录音线程并发送准备消息。</p>
     * 
     * <p>准备流程：</p>
     * <ol>
     *   <li>创建回调代理</li>
     *   <li>启动录音线程</li>
     *   <li>发送MSG_PREPARE消息</li>
     * </ol>
     * 
     * @throws IOException 如果准备失败
     * @throws NullPointerException 如果当前线程没有Looper
     */
    @Override
    public void prepare() throws IOException {
        // 确保在HandlerThread中调用
        Looper myLooper = Objects.requireNonNull(Looper.myLooper(), "Should prepare in HandlerThread");
        
        // run callback in caller thread
        // 创建回调代理，在调用者线程执行回调
        mCallbackDelegate = new CallbackDelegate(myLooper, mCallback);
        
        // 启动录音线程
        mRecordThread.start();
        
        // 创建录音Handler
        mRecordHandler = new RecordHandler(mRecordThread.getLooper());
        
        // 发送准备消息
        mRecordHandler.sendEmptyMessage(MSG_PREPARE);
    }

    /**
     * 停止录音
     * 
     * <p>停止音频采集和编码。清除回调队列，设置停止标志，
     * 发送停止消息给录音线程。</p>
     */
    @Override
    public void stop() {
        // clear callback queue
        // 清除回调队列中的所有消息
        mCallbackDelegate.removeCallbacksAndMessages(null);
        
        // 设置强制停止标志
        mForceStop.set(true);
        
        // 发送停止消息
        if (mRecordHandler != null) mRecordHandler.sendEmptyMessage(MSG_STOP);
    }

    /**
     * 释放资源
     * 
     * <p>释放AudioRecord和编码器资源。安全退出录音线程。</p>
     */
    @Override
    public void release() {
        // 发送释放消息
        if (mRecordHandler != null) mRecordHandler.sendEmptyMessage(MSG_RELEASE);
        
        // 安全退出录音线程
        mRecordThread.quitSafely();
    }

    /**
     * 释放输出缓冲区
     * 
     * <p>释放指定索引的编码输出缓冲区。由外部调用者（如Muxer）在处理完数据后调用。</p>
     * 
     * @param index 输出缓冲区索引
     */
    void releaseOutputBuffer(int index) {
        if (VERBOSE) Logger.d(TAG, "audio encoder released output buffer index=" + index);
        
        // 发送释放输出缓冲区消息
        Message.obtain(mRecordHandler, MSG_RELEASE_OUTPUT, index, 0).sendToTarget();
    }


    /**
     * 获取输出缓冲区
     * 
     * <p>获取指定索引的编码输出缓冲区，供外部读取编码数据。</p>
     * 
     * @param index 输出缓冲区索引
     * @return 输出缓冲区ByteBuffer
     */
    ByteBuffer getOutputBuffer(int index) {
        return mEncoder.getOutputBuffer(index);
    }


    /**
     * 回调代理类
     * 
     * <p>继承自Handler，用于在调用者线程执行编码器回调。
     * 确保回调在正确的线程中执行，避免线程安全问题。</p>
     * 
     * @since 1.0
     */
    private static class CallbackDelegate extends Handler {
        
        /** 回调接口实例 */
        private BaseEncoder.Callback mCallback;

        /**
         * 构造函数
         * 
         * @param l Looper对象，指定回调执行的线程
         * @param callback 回调接口实例
         */
        CallbackDelegate(Looper l, BaseEncoder.Callback callback) {
            super(l);
            this.mCallback = callback;
        }


        /**
         * 错误回调
         * 
         * <p>在调用者线程中执行错误回调。</p>
         * 
         * @param encoder 编码器实例
         * @param exception 异常对象
         */
        void onError(final Encoder encoder, final Exception exception) {
            Message.obtain(this, new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onError(encoder, exception);
                    }
                }
            }).sendToTarget();


        }

        /**
         * 输出格式变化回调
         * 
         * <p>在调用者线程中执行格式变化回调。</p>
         * 
         * @param encoder 编码器实例
         * @param format 新的输出格式
         */
        void onOutputFormatChanged(final BaseEncoder encoder, final MediaFormat format) {
            Message.obtain(this, new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onOutputFormatChanged(encoder, format);
                    }
                }
            }).sendToTarget();
        }

        /**
         * 输出缓冲区可用回调
         * 
         * <p>在调用者线程中执行输出缓冲区可用回调。</p>
         * 
         * @param encoder 编码器实例
         * @param index 输出缓冲区索引
         * @param info 缓冲区信息
         */
        void onOutputBufferAvailable(final BaseEncoder encoder, final int index, final MediaCodec.BufferInfo info) {
            Message.obtain(this, new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onOutputBufferAvailable(encoder, index, info);
                    }
                }
            }).sendToTarget();
        }
    }

    /** 消息类型：准备录音 */
    private static final int MSG_PREPARE = 0;
    
    /** 消息类型：输入数据 */
    private static final int MSG_FEED_INPUT = 1;
    
    /** 消息类型：输出数据 */
    private static final int MSG_DRAIN_OUTPUT = 2;
    
    /** 消息类型：释放输出缓冲区 */
    private static final int MSG_RELEASE_OUTPUT = 3;
    
    /** 消息类型：停止录音 */
    private static final int MSG_STOP = 4;
    
    /** 消息类型：释放资源 */
    private static final int MSG_RELEASE = 5;

    /**
     * 录音Handler类
     * 
     * <p>处理录音线程中的所有消息，包括音频采集、编码输入/输出等操作。</p>
     * 
     * @since 1.0
     */
    private class RecordHandler extends Handler {

        /** 缓存的BufferInfo对象池，避免频繁创建对象 */
        private LinkedList<MediaCodec.BufferInfo> mCachedInfos = new LinkedList<>();
        
        /** 正在复用的输出缓冲区索引队列 */
        private LinkedList<Integer> mMuxingOutputBufferIndices = new LinkedList<>();
        
        /** 轮询间隔，基于采样率计算，每2048个采样轮询一次 */
        private int mPollRate = 2048_000 / mSampleRate; // poll per 2048 samples

        /**
         * 构造函数
         * 
         * @param l Looper对象
         */
        RecordHandler(Looper l) {
            super(l);
        }

        /**
         * 处理消息
         * 
         * <p>根据消息类型执行相应的操作：</p>
         * <ul>
         *   <li>MSG_PREPARE：创建AudioRecord并启动录音</li>
         *   <li>MSG_FEED_INPUT：从麦克风读取数据并送入编码器</li>
         *   <li>MSG_DRAIN_OUTPUT：从编码器获取编码后的数据</li>
         *   <li>MSG_RELEASE_OUTPUT：释放输出缓冲区</li>
         *   <li>MSG_STOP：停止录音</li>
         *   <li>MSG_RELEASE：释放资源</li>
         * </ul>
         * 
         * @param msg 消息对象
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PREPARE:
                    // 创建AudioRecord实例
                    AudioRecord r = createAudioRecord(mSampleRate, mChannelConfig, mFormat);
                    if (r == null) {
                        // 创建失败，回调错误
                        Logger.e(TAG, "create audio record failure");
                        mCallbackDelegate.onError(MicRecorder.this, new IllegalArgumentException());
                        break;
                    } else {
                        // 开始录音
                        r.startRecording();
                        mMic = r;
                    }
                    try {
                        // 准备编码器
                        mEncoder.prepare();
                    } catch (Exception e) {
                        mCallbackDelegate.onError(MicRecorder.this, e);
                        break;
                    }
                    // 注意：这里没有break，继续执行MSG_FEED_INPUT的逻辑
                    
                case MSG_FEED_INPUT:
                    if (!mForceStop.get()) {
                        // 获取输入缓冲区索引
                        int index = pollInput();
                        if (VERBOSE)
                            Logger.d(TAG, "audio encoder returned input buffer index=" + index);
                        if (index >= 0) {
                            // 读取音频数据并送入编码器
                            feedAudioEncoder(index);
                            // tell encoder to eat the fresh meat!
                            // 通知编码器处理输出
                            if (!mForceStop.get()) sendEmptyMessage(MSG_DRAIN_OUTPUT);
                        } else {
                            // try later...
                            // 没有可用的输入缓冲区，稍后重试
                            if (VERBOSE) Logger.i(TAG, "try later to poll input buffer");
                            sendEmptyMessageDelayed(MSG_FEED_INPUT, mPollRate);
                        }
                    }
                    break;
                    
                case MSG_DRAIN_OUTPUT:
                    // 获取编码输出数据
                    offerOutput();
                    // 如果需要，继续轮询输入
                    pollInputIfNeed();
                    break;
                    
                case MSG_RELEASE_OUTPUT:
                    // 释放输出缓冲区
                    mEncoder.releaseOutputBuffer(msg.arg1);
                    // Nobody care what it exactly is.
                    // 从队列中移除已释放的索引
                    mMuxingOutputBufferIndices.poll();
                    if (VERBOSE) Logger.d(TAG, "audio encoder released output buffer index="
                            + msg.arg1 + ", remaining=" + mMuxingOutputBufferIndices.size());
                    // 继续轮询输入
                    pollInputIfNeed();
                    break;
                    
                case MSG_STOP:
                    // 停止AudioRecord
                    if (mMic != null) {
                        mMic.stop();
                    }
                    // 停止编码器
                    mEncoder.stop();
                    break;
                    
                case MSG_RELEASE:
                    // 释放AudioRecord
                    if (mMic != null) {
                        mMic.release();
                        mMic = null;
                    }
                    // 释放编码器
                    mEncoder.release();
                    break;
            }
        }

        /**
         * 获取编码输出数据
         * 
         * <p>循环从编码器获取输出缓冲区，直到没有更多数据或收到停止信号。</p>
         */
        private void offerOutput() {
            while (!mForceStop.get()) {
                // 从缓存池获取BufferInfo对象
                MediaCodec.BufferInfo info = mCachedInfos.poll();
                if (info == null) {
                    info = new MediaCodec.BufferInfo();
                }
                
                // 从编码器获取输出缓冲区
                int index = mEncoder.getEncoder().dequeueOutputBuffer(info, 1);
                if (VERBOSE) Logger.d(TAG, "audio encoder returned output buffer index=" + index);
                
                if (index == INFO_OUTPUT_FORMAT_CHANGED) {
                    // 输出格式变化，通知回调
                    mCallbackDelegate.onOutputFormatChanged(mEncoder, mEncoder.getEncoder().getOutputFormat());
                }
                
                if (index < 0) {
                    // 没有可用的输出缓冲区，回收BufferInfo对象
                    info.set(0, 0, 0, 0);
                    mCachedInfos.offer(info);
                    break;
                }
                
                // 记录正在复用的输出缓冲区索引
                mMuxingOutputBufferIndices.offer(index);
                
                // 通知回调有输出数据可用
                mCallbackDelegate.onOutputBufferAvailable(mEncoder, index, info);

            }
        }

        /**
         * 轮询输入缓冲区
         * 
         * <p>从编码器获取可用的输入缓冲区索引。</p>
         * 
         * @return 输入缓冲区索引，如果没有可用的返回负值
         */
        private int pollInput() {
            return mEncoder.getEncoder().dequeueInputBuffer(0);
        }

        /**
         * 根据需要轮询输入
         * 
         * <p>如果输出缓冲区队列较短，立即发送输入消息以保持数据流。</p>
         */
        private void pollInputIfNeed() {
            if (mMuxingOutputBufferIndices.size() <= 1 && !mForceStop.get()) {
                // need fresh data, right now!
                // 需要新数据，立即发送输入消息
                removeMessages(MSG_FEED_INPUT);
                sendEmptyMessageDelayed(MSG_FEED_INPUT, 0);
            }
        }
    }

    /**
     * 向编码器送入音频数据
     * 
     * <p>从AudioRecord读取PCM数据，送入编码器进行AAC编码。
     * 注意：应该等待所有输出缓冲区处理完毕后再送入新的输入数据。</p>
     * 
     * @param index 输入缓冲区索引
     */
    private void feedAudioEncoder(int index) {
        if (index < 0 || mForceStop.get()) return;
        
        final AudioRecord r = Objects.requireNonNull(mMic, "maybe release");
        
        // 检查录音状态
        final boolean eos = r.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED;
        
        // 获取输入缓冲区
        final ByteBuffer frame = mEncoder.getInputBuffer(index);
        int offset = frame.position();
        int limit = frame.limit();
        int read = 0;
        
        if (!eos) {
            // 从AudioRecord读取音频数据
            read = r.read(frame, limit);
            if (VERBOSE) Logger.d(TAG, "Read frame data size " + read + " for index "
                    + index + " buffer : " + offset + ", " + limit);
            if (read < 0) {
                read = 0;
            }
        }

        // 计算帧时间戳
        long pstTs = calculateFrameTimestamp(read << 3);
        int flags = BUFFER_FLAG_KEY_FRAME;

        if (eos) {
            // 录音已停止，发送结束标志
            flags = BUFFER_FLAG_END_OF_STREAM;
        }
        
        // feed frame to encoder
        // 将帧数据送入编码器
        if (VERBOSE) Logger.d(TAG, "Feed codec index=" + index + ", presentationTimeUs="
                + pstTs + ", flags=" + flags);
        mEncoder.queueInputBuffer(index, offset, read, pstTs, flags);
    }


    /** 最后一帧的缓存ID */
    private static final int LAST_FRAME_ID = -1;
    
    /** 帧时间戳缓存，避免重复计算 */
    private SparseLongArray mFramesUsCache = new SparseLongArray(2);

    /**
     * 计算帧时间戳
     * 
     * <p>根据采样数计算帧的显示时间戳。1个采样 = 16位。</p>
     * 
     * <p>时间戳计算逻辑：</p>
     * <ol>
     *   <li>计算采样数对应的时间长度</li>
     *   <li>获取当前系统时间</li>
     *   <li>考虑采集延迟进行调整</li>
     *   <li>确保时间戳连续递增</li>
     * </ol>
     * 
     * @param totalBits 总比特数（采样数 * 16）
     * @return 显示时间戳，单位微秒
     */
    private long calculateFrameTimestamp(int totalBits) {
        // 计算采样数（16位 = 2字节）
        int samples = totalBits >> 4;
        
        // 从缓存中查找时间戳
        long frameUs = mFramesUsCache.get(samples, -1);
        if (frameUs == -1) {
            // 缓存未命中，计算时间戳
            frameUs = samples * 1000_000 / mChannelsSampleRate;
            mFramesUsCache.put(samples, frameUs);
        }
        
        // 获取当前系统时间（微秒）
        long timeUs = SystemClock.elapsedRealtimeNanos() / 1000;
        
        // accounts the delay of polling the audio sample data
        // 减去采集延迟
        timeUs -= frameUs;
        
        long currentUs;
        
        // 获取上一帧的时间戳
        long lastFrameUs = mFramesUsCache.get(LAST_FRAME_ID, -1);
        if (lastFrameUs == -1) { 
            // it's the first frame
            // 第一帧，使用系统时间
            currentUs = timeUs;
        } else {
            // 使用上一帧的时间戳作为基准
            currentUs = lastFrameUs;
        }
        
        if (VERBOSE)
            Logger.i(TAG, "count samples pts: " + currentUs + ", time pts: " + timeUs + ", samples: " + samples);
        
        // maybe too late to acquire sample data
        // 如果时间差过大，重置时间戳
        if (timeUs - currentUs >= (frameUs << 1)) {
            // reset
            currentUs = timeUs;
        }
        
        // 更新最后一帧的时间戳
        mFramesUsCache.put(LAST_FRAME_ID, currentUs + frameUs);
        
        return currentUs;
    }

    /**
     * 创建AudioRecord实例
     * 
     * <p>根据参数创建AudioRecord对象，用于从麦克风采集音频数据。</p>
     * 
     * @param sampleRateInHz 采样率，单位Hz
     * @param channelConfig 声道配置
     * @param audioFormat 音频格式
     * @return AudioRecord实例，创建失败返回null
     */
    private static AudioRecord createAudioRecord(int sampleRateInHz, int channelConfig, int audioFormat) {
        // 获取最小缓冲区大小
        int minBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (minBytes <= 0) {
            Logger.e(TAG, String.format(Locale.US, "Bad arguments: getMinBufferSize(%d, %d, %d)",
                    sampleRateInHz, channelConfig, audioFormat));
            return null;
        }
        
        // 创建AudioRecord实例
        AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRateInHz,
                channelConfig,
                audioFormat,
                minBytes * 2);  // 缓冲区大小设为最小值的2倍

        // 检查初始化状态
        if (record.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Logger.e(TAG, String.format(Locale.US, "Bad arguments to new AudioRecord %d, %d, %d",
                    sampleRateInHz, channelConfig, audioFormat));
            return null;
        }
        
        if (VERBOSE) {
            Logger.i(TAG, "created AudioRecord " + record + ", MinBufferSize= " + minBytes);

        }
        return record;
    }

}
