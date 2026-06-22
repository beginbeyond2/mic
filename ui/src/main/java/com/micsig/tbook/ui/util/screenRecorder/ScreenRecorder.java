/*
 * Copyright (c) 2014 Yrom Wang <http://www.yrom.net>
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

import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 屏幕录制器
 * 
 * <p>实现Android屏幕录制功能的核心类，负责协调视频编码器、音频编码器和媒体复用器，
 * 将屏幕画面和麦克风音频合成为MP4视频文件。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>通过MediaProjection捕获屏幕画面</li>
 *   <li>使用VideoEncoder进行H.264视频编码</li>
 *   <li>使用MicRecorder进行AAC音频编码</li>
 *   <li>使用MediaMuxer合成音视频为MP4文件</li>
 *   <li>管理录制生命周期（开始、停止、释放）</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * // 创建视频配置
 * VideoEncodeConfig videoConfig = new VideoEncodeConfig(...);
 * 
 * // 创建音频配置（可选）
 * AudioEncodeConfig audioConfig = new AudioEncodeConfig(...);
 * 
 * // 创建屏幕录制器
 * ScreenRecorder recorder = new ScreenRecorder(
 *     videoConfig, audioConfig, dpi, mediaProjection, outputPath
 * );
 * 
 * // 设置回调
 * recorder.setCallback(new ScreenRecorder.Callback() {
 *     @Override
 *     public void onStart() { }
 *     
 *     @Override
 *     public void onStop(Throwable error) { }
 *     
 *     @Override
 *     public void onRecording(long presentationTimeUs) { }
 * });
 * 
 * // 开始录制
 * recorder.start();
 * 
 * // 停止录制
 * recorder.quit();
 * }</pre>
 * 
 * <p>录制流程：</p>
 * <ol>
 *   <li>创建ScreenRecorder实例</li>
 *   <li>设置回调监听器</li>
 *   <li>调用start()开始录制</li>
 *   <li>回调onStart()通知开始成功</li>
 *   <li>录制过程中回调onRecording()</li>
 *   <li>调用quit()停止录制</li>
 *   <li>回调onStop()通知停止完成</li>
 * </ol>
 * 
 * @author Yrom
 * @version 1.0
 * @since 1.0
 * @see VideoEncoder
 * @see MicRecorder
 * @see MediaProjection
 * @see MediaMuxer
 */
public class ScreenRecorder {
    
    /** 日志标签 */
    private static final String TAG = "ScreenRecorder";
    
    /** 是否输出详细日志 */
    private static final boolean VERBOSE = false;
    
    /** 无效索引常量 */
    private static final int INVALID_INDEX = -1;
    
    /** H.264视频编码MIME类型 */
    static final String VIDEO_AVC = MIMETYPE_VIDEO_AVC; // H.264 Advanced Video Coding
    
    /** AAC音频编码MIME类型 */
    static final String AUDIO_AAC = MIMETYPE_AUDIO_AAC; // H.264 Advanced Audio Coding
    
    /** 视频宽度 */
    private int mWidth;
    
    /** 视频高度 */
    private int mHeight;
    
    /** 屏幕DPI */
    private int mDpi;
    
    /** 输出文件路径 */
    private String mDstPath;
    
    /** MediaProjection实例，用于捕获屏幕 */
    private MediaProjection mMediaProjection;
    
    /** 视频编码器 */
    private VideoEncoder mVideoEncoder;
    
    /** 音频编码器（麦克风录音器） */
    private MicRecorder mAudioEncoder;

    /** 视频输出格式 */
    private MediaFormat mVideoOutputFormat = null, mAudioOutputFormat = null;
    
    /** 视频轨道索引，音频轨道索引 */
    private int mVideoTrackIndex = INVALID_INDEX, mAudioTrackIndex = INVALID_INDEX;
    
    /** 媒体复用器 */
    private MediaMuxer mMuxer;
    
    /** 复用器是否已启动 */
    private boolean mMuxerStarted = false;

    /** 强制退出标志 */
    private AtomicBoolean mForceQuit = new AtomicBoolean(false);
    
    /** 是否正在运行 */
    private AtomicBoolean mIsRunning = new AtomicBoolean(false);
    
    /** 虚拟显示器，用于捕获屏幕 */
    private VirtualDisplay mVirtualDisplay;
    
    /** MediaProjection回调，监听投影停止事件 */
    private MediaProjection.Callback mProjectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            // 投影停止时退出录制
            quit();
        }
    };

    /** 工作线程 */
    private HandlerThread mWorker;
    
    /** 回调Handler */
    private CallbackHandler mHandler;

    /** 用户回调接口 */
    private Callback mCallback;
    
    /** 待处理的视频编码器缓冲区索引队列 */
    private LinkedList<Integer> mPendingVideoEncoderBufferIndices = new LinkedList<>();
    
    /** 待处理的音频编码器缓冲区索引队列 */
    private LinkedList<Integer> mPendingAudioEncoderBufferIndices = new LinkedList<>();
    
    /** 待处理的音频编码器缓冲区信息队列 */
    private LinkedList<MediaCodec.BufferInfo> mPendingAudioEncoderBufferInfos = new LinkedList<>();
    
    /** 待处理的视频编码器缓冲区信息队列 */
    private LinkedList<MediaCodec.BufferInfo> mPendingVideoEncoderBufferInfos = new LinkedList<>();

    /**
     * 构造函数
     * 
     * <p>创建屏幕录制器实例。根据配置初始化视频和音频编码器。</p>
     * 
     * @param video 视频编码配置，不能为null
     * @param audio 音频编码配置，可为null（不录制音频）
     * @param dpi 屏幕DPI，用于创建VirtualDisplay
     * @param mp MediaProjection实例，用于捕获屏幕
     * @param dstPath 输出文件路径，必须是有效的文件路径
     */
    public ScreenRecorder(VideoEncodeConfig video,
                          AudioEncodeConfig audio,
                          int dpi, MediaProjection mp,
                          String dstPath) {
        mWidth = video.width;
        mHeight = video.height;
        mDpi = dpi;
        mMediaProjection = mp;
        mDstPath = dstPath;
        
        // 创建视频编码器
        mVideoEncoder = new VideoEncoder(video);
        
        // 创建音频编码器（如果配置不为null）
        mAudioEncoder = audio == null ? null : new MicRecorder(audio);

    }

    /**
     * 停止录制
     * 
     * <p>停止屏幕录制任务。如果录制未开始，直接释放资源；
     * 如果正在录制，发送停止信号。</p>
     */
    public final void quit() {
        mForceQuit.set(true);
        if (!mIsRunning.get()) {
            // 未运行，直接释放
            release();
        } else {
            // 正在运行，发送停止信号
            signalStop(false);
        }

    }

    /**
     * 开始录制
     * 
     * <p>启动屏幕录制。创建工作线程并发送开始消息。
     * 只能调用一次，重复调用会抛出异常。</p>
     * 
     * @throws IllegalStateException 如果已经调用过start()
     */
    public void start() {
        if (mWorker != null) throw new IllegalStateException();
        
        // 创建工作线程
        mWorker = new HandlerThread(TAG);
        mWorker.start();
        
        // 创建回调Handler
        mHandler = new CallbackHandler(mWorker.getLooper());
        
        // 发送开始消息
        mHandler.sendEmptyMessage(MSG_START);
    }

    /**
     * 设置回调监听器
     * 
     * @param callback 回调接口实例
     */
    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * 获取保存路径
     * 
     * @return 输出文件路径
     */
    public String getSavedPath() {
        return mDstPath;
    }

    /**
     * 录制回调接口
     * 
     * <p>定义屏幕录制过程中的回调方法，用于通知录制状态变化。</p>
     * 
     * @since 1.0
     */
    public interface Callback {
        /**
         * 录制停止回调
         * 
         * @param error 错误信息，正常停止时为null
         */
        void onStop(Throwable error);

        /**
         * 录制开始回调
         */
        void onStart();

        /**
         * 录制进行中回调
         * 
         * @param presentationTimeUs 当前帧的显示时间戳，单位微秒
         */
        void onRecording(long presentationTimeUs);
    }

    /** 消息类型：开始录制 */
    private static final int MSG_START = 0;
    
    /** 消息类型：停止录制 */
    private static final int MSG_STOP = 1;
    
    /** 消息类型：发生错误 */
    private static final int MSG_ERROR = 2;
    
    /** 停止参数：发送EOS标志 */
    private static final int STOP_WITH_EOS = 1;

    /**
     * 回调Handler类
     * 
     * <p>处理工作线程中的消息，包括开始、停止、错误等操作。</p>
     * 
     * @since 1.0
     */
    private class CallbackHandler extends Handler {
        
        /**
         * 构造函数
         * 
         * @param looper Looper对象
         */
        CallbackHandler(Looper looper) {
            super(looper);
        }

        /**
         * 处理消息
         * 
         * <p>根据消息类型执行相应操作：</p>
         * <ul>
         *   <li>MSG_START：开始录制</li>
         *   <li>MSG_STOP：停止录制</li>
         *   <li>MSG_ERROR：处理错误</li>
         * </ul>
         * 
         * @param msg 消息对象
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START:
                    try {
                        // 开始录制
                        record();
                        // 通知回调开始成功
                        if (mCallback != null) {
                            mCallback.onStart();
                        }
                        break;
                    } catch (Exception e) {
                        // 记录错误
                        msg.obj = e;
                    }
                    // 注意：这里没有break，如果发生错误继续执行停止逻辑
                    
                case MSG_STOP:
                case MSG_ERROR:
                    // 停止编码器
                    stopEncoders();
                    
                    // 如果不是带EOS停止，发送EOS标志
                    if (msg.arg1 != STOP_WITH_EOS) signalEndOfStream();
                    
                    // 通知回调停止
                    if (mCallback != null) {
                        mCallback.onStop((Throwable) msg.obj);
                    }
                    
                    // 释放资源
                    release();
                    break;
            }
        }
    }

    /**
     * 发送结束流标志
     * 
     * <p>向复用器发送EOS（End of Stream）标志，表示数据流结束。
     * 分别为视频和音频轨道发送EOS。</p>
     */
    private void signalEndOfStream() {
        // 创建EOS缓冲区信息
        MediaCodec.BufferInfo eos = new MediaCodec.BufferInfo();
        ByteBuffer buffer = ByteBuffer.allocate(0);
        eos.set(0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        
        if (VERBOSE) Log.i(TAG, "Signal EOS to muxer ");
        
        // 为视频轨道发送EOS
        if (mVideoTrackIndex != INVALID_INDEX) {
            writeSampleData(mVideoTrackIndex, eos, buffer);
        }
        
        // 为音频轨道发送EOS
        if (mAudioTrackIndex != INVALID_INDEX) {
            writeSampleData(mAudioTrackIndex, eos, buffer);
        }
        
        // 重置轨道索引
        mVideoTrackIndex = INVALID_INDEX;
        mAudioTrackIndex = INVALID_INDEX;
    }

    /**
     * 执行录制
     * 
     * <p>初始化并启动录制流程。创建复用器、准备编码器、创建虚拟显示器。</p>
     * 
     * @throws IllegalStateException 如果已经在运行或已退出
     */
    private void record() {
        // 检查状态
        if (mIsRunning.get() || mForceQuit.get()) {
            throw new IllegalStateException();
        }
        if (mMediaProjection == null) {
            throw new IllegalStateException("maybe release");
        }
        
        // 设置运行状态
        mIsRunning.set(true);

        // 注册MediaProjection回调
        mMediaProjection.registerCallback(mProjectionCallback, mHandler);
        
        try {
            // create muxer
            // 创建媒体复用器
            mMuxer = new MediaMuxer(mDstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            
            // create encoder and input surface
            // 准备视频编码器
            prepareVideoEncoder();
            
            // 准备音频编码器
            prepareAudioEncoder();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 创建虚拟显示器，捕获屏幕画面
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG + "-display",
                mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mVideoEncoder.getInputSurface(), null, null);
        
        if (VERBOSE) Log.d(TAG, "created virtual display: " + mVirtualDisplay.getDisplay());
    }

    /**
     * 复用视频数据
     * 
     * <p>将编码后的视频数据写入复用器。如果复用器未启动，将数据加入待处理队列。</p>
     * 
     * @param index 输出缓冲区索引
     * @param buffer 缓冲区信息
     */
    private void muxVideo(int index, MediaCodec.BufferInfo buffer) {
        if (!mIsRunning.get()) {
            Log.w(TAG, "muxVideo: Already stopped!");
            return;
        }
        
        // 如果复用器未启动，加入待处理队列
        if (!mMuxerStarted || mVideoTrackIndex == INVALID_INDEX) {
            mPendingVideoEncoderBufferIndices.add(index);
            mPendingVideoEncoderBufferInfos.add(buffer);
            return;
        }
        
        // 获取编码数据
        ByteBuffer encodedData = mVideoEncoder.getOutputBuffer(index);
        
        // 写入复用器
        writeSampleData(mVideoTrackIndex, buffer, encodedData);
        
        // 释放输出缓冲区
        mVideoEncoder.releaseOutputBuffer(index);
        
        // 检查是否到达流结束
        if ((buffer.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            if (VERBOSE)
                Log.d(TAG, "Stop encoder and muxer, since the buffer has been marked with EOS");
            // send release msg
            mVideoTrackIndex = INVALID_INDEX;
            signalStop(true);
        }
    }


    /**
     * 复用音频数据
     * 
     * <p>将编码后的音频数据写入复用器。如果复用器未启动，将数据加入待处理队列。</p>
     * 
     * @param index 输出缓冲区索引
     * @param buffer 缓冲区信息
     */
    private void muxAudio(int index, MediaCodec.BufferInfo buffer) {
        if (!mIsRunning.get()) {
            Log.w(TAG, "muxAudio: Already stopped!");
            return;
        }
        
        // 如果复用器未启动，加入待处理队列
        if (!mMuxerStarted || mAudioTrackIndex == INVALID_INDEX) {
            mPendingAudioEncoderBufferIndices.add(index);
            mPendingAudioEncoderBufferInfos.add(buffer);
            return;

        }
        
        // 获取编码数据
        ByteBuffer encodedData = mAudioEncoder.getOutputBuffer(index);
        
        // 写入复用器
        writeSampleData(mAudioTrackIndex, buffer, encodedData);
        
        // 释放输出缓冲区
        mAudioEncoder.releaseOutputBuffer(index);
        
        // 检查是否到达流结束
        if ((buffer.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            if (VERBOSE)
                Log.d(TAG, "Stop encoder and muxer, since the buffer has been marked with EOS");
            mAudioTrackIndex = INVALID_INDEX;
            signalStop(true);
        }
    }

    /**
     * 写入采样数据到复用器
     * 
     * <p>将编码后的数据写入MediaMuxer。处理编解码器配置数据和显示时间戳。</p>
     * 
     * @param track 轨道索引
     * @param buffer 缓冲区信息
     * @param encodedData 编码数据
     */
    private void writeSampleData(int track, MediaCodec.BufferInfo buffer, ByteBuffer encodedData) {
        // 忽略编解码器配置数据
        if ((buffer.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // The codec config data was pulled out and fed to the muxer when we got
            // the INFO_OUTPUT_FORMAT_CHANGED status.
            // Ignore it.
            if (VERBOSE) Log.d(TAG, "Ignoring BUFFER_FLAG_CODEC_CONFIG");
            buffer.size = 0;
        }
        
        // 检查是否为EOS
        boolean eos = (buffer.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
        
        // 如果数据为空且不是EOS，丢弃
        if (buffer.size == 0 && !eos) {
            if (VERBOSE) Log.d(TAG, "info.size == 0, drop it.");
            encodedData = null;
        } else {
            // 重置显示时间戳
            if (buffer.presentationTimeUs != 0) { // maybe 0 if eos
                if (track == mVideoTrackIndex) {
                    resetVideoPts(buffer);
                } else if (track == mAudioTrackIndex) {
                    resetAudioPts(buffer);
                }
            }
            
            if (VERBOSE)
                Log.d(TAG, "[" + Thread.currentThread().getId() + "] Got buffer, track=" + track
                        + ", info: size=" + buffer.size
                        + ", presentationTimeUs=" + buffer.presentationTimeUs);
            
            // 通知回调录制进度
            if (!eos && mCallback != null) {
                mCallback.onRecording(buffer.presentationTimeUs);
            }
        }
        
        // 写入复用器
        if (encodedData != null) {
            encodedData.position(buffer.offset);
            encodedData.limit(buffer.offset + buffer.size);
            mMuxer.writeSampleData(track, encodedData, buffer);
            if (VERBOSE)
                Log.i(TAG, "Sent " + buffer.size + " bytes to MediaMuxer on track " + track);
        }
    }

    /** 视频时间戳偏移量，音频时间戳偏移量 */
    private long mVideoPtsOffset, mAudioPtsOffset;

    /**
     * 重置音频显示时间戳
     * 
     * <p>将音频时间戳从绝对时间转换为相对时间，确保从0开始。</p>
     * 
     * @param buffer 缓冲区信息
     */
    private void resetAudioPts(MediaCodec.BufferInfo buffer) {
        if (mAudioPtsOffset == 0) {
            // 第一帧，记录偏移量
            mAudioPtsOffset = buffer.presentationTimeUs;
            buffer.presentationTimeUs = 0;
        } else {
            // 后续帧，减去偏移量
            buffer.presentationTimeUs -= mAudioPtsOffset;
        }
    }

    /**
     * 重置视频显示时间戳
     * 
     * <p>将视频时间戳从绝对时间转换为相对时间，确保从0开始。</p>
     * 
     * @param buffer 缓冲区信息
     */
    private void resetVideoPts(MediaCodec.BufferInfo buffer) {
        if (mVideoPtsOffset == 0) {
            // 第一帧，记录偏移量
            mVideoPtsOffset = buffer.presentationTimeUs;
            buffer.presentationTimeUs = 0;
        } else {
            // 后续帧，减去偏移量
            buffer.presentationTimeUs -= mVideoPtsOffset;
        }
    }

    /**
     * 重置视频输出格式
     * 
     * <p>保存视频编码器的输出格式，用于添加视频轨道到复用器。</p>
     * 
     * @param newFormat 新的视频输出格式
     * @throws IllegalStateException 如果格式已经设置过
     */
    private void resetVideoOutputFormat(MediaFormat newFormat) {
        // should happen before receiving buffers, and should only happen once
        if (mVideoTrackIndex >= 0 || mMuxerStarted) {
            throw new IllegalStateException("output format already changed!");
        }
        if (VERBOSE)
            Log.i(TAG, "Video output format changed.\n New format: " + newFormat.toString());
        mVideoOutputFormat = newFormat;
    }

    /**
     * 重置音频输出格式
     * 
     * <p>保存音频编码器的输出格式，用于添加音频轨道到复用器。</p>
     * 
     * @param newFormat 新的音频输出格式
     * @throws IllegalStateException 如果格式已经设置过
     */
    private void resetAudioOutputFormat(MediaFormat newFormat) {
        // should happen before receiving buffers, and should only happen once
        if (mAudioTrackIndex >= 0 || mMuxerStarted) {
            throw new IllegalStateException("output format already changed!");
        }
        if (VERBOSE)
            Log.i(TAG, "Audio output format changed.\n New format: " + newFormat.toString());
        mAudioOutputFormat = newFormat;
    }

    /**
     * 如果准备好则启动复用器
     * 
     * <p>检查视频和音频格式是否都已准备好，如果是则启动复用器，
     * 并处理待处理的缓冲区数据。</p>
     */
    private void startMuxerIfReady() {
        // 检查是否所有格式都已准备好
        if (mMuxerStarted || mVideoOutputFormat == null
                || (mAudioEncoder != null && mAudioOutputFormat == null)) {
            return;
        }

        // 添加视频轨道
        mVideoTrackIndex = mMuxer.addTrack(mVideoOutputFormat);
        
        // 添加音频轨道（如果有）
        mAudioTrackIndex = mAudioEncoder == null ? INVALID_INDEX : mMuxer.addTrack(mAudioOutputFormat);
        
        // 启动复用器
        mMuxer.start();
        mMuxerStarted = true;
        
        if (VERBOSE) Log.i(TAG, "Started media muxer, videoIndex=" + mVideoTrackIndex);
        
        // 检查是否有待处理的数据
        if (mPendingVideoEncoderBufferIndices.isEmpty() && mPendingAudioEncoderBufferIndices.isEmpty()) {
            return;
        }
        
        if (VERBOSE) Log.i(TAG, "Mux pending video output buffers...");
        
        // 处理待处理的视频数据
        MediaCodec.BufferInfo info;
        while ((info = mPendingVideoEncoderBufferInfos.poll()) != null) {
            int index = mPendingVideoEncoderBufferIndices.poll();
            muxVideo(index, info);
        }
        
        // 处理待处理的音频数据
        if (mAudioEncoder != null) {
            while ((info = mPendingAudioEncoderBufferInfos.poll()) != null) {
                int index = mPendingAudioEncoderBufferIndices.poll();
                muxAudio(index, info);
            }
        }
        
        if (VERBOSE) Log.i(TAG, "Mux pending video output buffers done.");
    }

    /**
     * 准备视频编码器
     * 
     * <p>初始化视频编码器并设置回调。创建编码器回调处理输出数据、错误和格式变化。</p>
     * 
     * @throws IOException 如果准备失败
     */
    // @WorkerThread
    private void prepareVideoEncoder() throws IOException {
        // 创建视频编码器回调
        VideoEncoder.Callback callback = new VideoEncoder.Callback() {
            boolean ranIntoError = false;

            @Override
            public void onOutputBufferAvailable(BaseEncoder codec, int index, MediaCodec.BufferInfo info) {
                if (VERBOSE) Log.i(TAG, "VideoEncoder output buffer available: index=" + index);
                try {
                    // 复用视频数据
                    muxVideo(index, info);
                } catch (Exception e) {
                    Log.e(TAG, "Muxer encountered an error! ", e);
                    Message.obtain(mHandler, MSG_ERROR, e).sendToTarget();
                }
            }

            @Override
            public void onError(Encoder codec, Exception e) {
                ranIntoError = true;
                Log.e(TAG, "VideoEncoder ran into an error! ", e);
                Message.obtain(mHandler, MSG_ERROR, e).sendToTarget();
            }

            @Override
            public void onOutputFormatChanged(BaseEncoder codec, MediaFormat format) {
                // 更新视频输出格式
                resetVideoOutputFormat(format);
                // 尝试启动复用器
                startMuxerIfReady();
            }
        };
        
        // 设置回调并准备编码器
        mVideoEncoder.setCallback(callback);
        mVideoEncoder.prepare();
    }

    /**
     * 准备音频编码器
     * 
     * <p>初始化音频编码器（麦克风录音器）并设置回调。
     * 如果音频编码器为null，直接返回。</p>
     * 
     * @throws IOException 如果准备失败
     */
    private void prepareAudioEncoder() throws IOException {
        final MicRecorder micRecorder = mAudioEncoder;
        if (micRecorder == null) return;
        
        // 创建音频编码器回调
        AudioEncoder.Callback callback = new AudioEncoder.Callback() {
            boolean ranIntoError = false;

            @Override
            public void onOutputBufferAvailable(BaseEncoder codec, int index, MediaCodec.BufferInfo info) {
                if (VERBOSE)
                    Log.i(TAG, "[" + Thread.currentThread().getId() + "] AudioEncoder output buffer available: index=" + index);
                try {
                    // 复用音频数据
                    muxAudio(index, info);
                } catch (Exception e) {
                    Log.e(TAG, "Muxer encountered an error! ", e);
                    Message.obtain(mHandler, MSG_ERROR, e).sendToTarget();
                }
            }

            @Override
            public void onOutputFormatChanged(BaseEncoder codec, MediaFormat format) {
                if (VERBOSE)
                    Log.d(TAG, "[" + Thread.currentThread().getId() + "] AudioEncoder returned new format " + format);
                // 更新音频输出格式
                resetAudioOutputFormat(format);
                // 尝试启动复用器
                startMuxerIfReady();
            }

            @Override
            public void onError(Encoder codec, Exception e) {
                ranIntoError = true;
                Log.e(TAG, "MicRecorder ran into an error! ", e);
                Message.obtain(mHandler, MSG_ERROR, e).sendToTarget();
            }


        };
        
        // 设置回调并准备编码器
        micRecorder.setCallback(callback);
        micRecorder.prepare();
    }

    /**
     * 发送停止信号
     * 
     * <p>发送停止消息到工作线程。如果stopWithEOS为true，
     * 表示需要发送EOS标志。</p>
     * 
     * @param stopWithEOS 是否发送EOS标志
     */
    private void signalStop(boolean stopWithEOS) {
        Message msg = Message.obtain(mHandler, MSG_STOP, stopWithEOS ? STOP_WITH_EOS : 0, 0);
        mHandler.sendMessageAtFrontOfQueue(msg);
    }

    /**
     * 停止编码器
     * 
     * <p>停止视频和音频编码器，清除待处理的数据队列。</p>
     */
    private void stopEncoders() {
        // 设置运行状态为false
        mIsRunning.set(false);
        
        // 清除待处理队列
        mPendingAudioEncoderBufferInfos.clear();
        mPendingAudioEncoderBufferIndices.clear();
        mPendingVideoEncoderBufferInfos.clear();
        mPendingVideoEncoderBufferIndices.clear();
        
        // maybe called on an error has been occurred
        // 停止视频编码器
        try {
            if (mVideoEncoder != null) mVideoEncoder.stop();
        } catch (IllegalStateException e) {
            // ignored
        }
        
        // 停止音频编码器
        try {
            if (mAudioEncoder != null) mAudioEncoder.stop();
        } catch (IllegalStateException e) {
            // ignored
        }

    }

    /**
     * 释放资源
     * 
     * <p>释放所有资源，包括MediaProjection、VirtualDisplay、编码器、复用器等。</p>
     */
    private void release() {
        // 注销MediaProjection回调
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mProjectionCallback);
        }
        
        // 释放虚拟显示器
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }

        // 重置格式和轨道索引
        mVideoOutputFormat = mAudioOutputFormat = null;
        mVideoTrackIndex = mAudioTrackIndex = INVALID_INDEX;
        mMuxerStarted = false;

        // 退出工作线程
        if (mWorker != null) {
            mWorker.quitSafely();
            mWorker = null;
        }
        
        // 释放视频编码器
        if (mVideoEncoder != null) {
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
        
        // 释放音频编码器
        if (mAudioEncoder != null) {
            mAudioEncoder.release();
            mAudioEncoder = null;
        }

        // 停止并释放MediaProjection
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        
        // 释放复用器
        if (mMuxer != null) {
            try {
                mMuxer.stop();
                mMuxer.release();
            } catch (Exception e) {
                // ignored
            }
            mMuxer = null;
        }
        
        mHandler = null;
    }

    /**
     * 析构函数
     * 
     * <p>在对象被垃圾回收前检查是否已调用release()。
     * 如果未调用，输出警告日志并释放资源。</p>
     * 
     * @throws Throwable 析构异常
     */
    @Override
    protected void finalize() throws Throwable {
        if (mMediaProjection != null) {
            Log.e(TAG, "release() not called!");
            release();
        }
    }

}
