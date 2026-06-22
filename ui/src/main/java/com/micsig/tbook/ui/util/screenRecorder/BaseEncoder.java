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

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * 编码器基类
 * 
 * <p>提供MediaCodec编码器的封装和基础功能实现。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>MediaCodec管理：创建、配置、启动、停止、释放</li>
 *   <li>异步回调：支持异步模式的编码器回调</li>
 *   <li>缓冲区操作：输入/输出缓冲区的获取和操作</li>
 * </ul>
 * 
 * <p>子类需要实现：</p>
 * <ul>
 *   <li>{@link #createMediaFormat()}：创建编码器的MediaFormat配置</li>
 * </ul>
 * 
 * <p>使用注意：</p>
 * <ul>
 *   <li>prepare()方法必须在工作线程（HandlerThread）中调用</li>
 *   <li>如果设置了回调，编码器将运行在异步模式</li>
 * </ul>
 * 
 * @author yrom
 * @version 2017/12/4
 */
abstract class BaseEncoder implements Encoder {
    
    /**
     * 扩展的编码器回调接口
     * 
     * <p>扩展了基础的Encoder.Callback，增加了缓冲区可用和格式变化的回调。</p>
     */
    static abstract class Callback implements Encoder.Callback {
        
        /**
         * 输入缓冲区可用回调
         * 
         * <p>当编码器的输入缓冲区可用时调用，子类可重写此方法处理输入数据。</p>
         *
         * @param encoder 编码器实例
         * @param index   可用的输入缓冲区索引
         */
        void onInputBufferAvailable(BaseEncoder encoder, int index) {
        }

        /**
         * 输出格式变化回调
         * 
         * <p>当编码器的输出格式发生变化时调用。</p>
         *
         * @param encoder 编码器实例
         * @param format  新的输出格式
         */
        void onOutputFormatChanged(BaseEncoder encoder, MediaFormat format) {
        }

        /**
         * 输出缓冲区可用回调
         * 
         * <p>当编码器的输出缓冲区可用时调用，包含编码后的数据。</p>
         *
         * @param encoder 编码器实例
         * @param index   可用的输出缓冲区索引
         * @param info    缓冲区信息，包含时间戳、大小、标志等
         */
        void onOutputBufferAvailable(BaseEncoder encoder, int index, MediaCodec.BufferInfo info) {
        }
    }

    /**
     * 默认构造函数
     */
    BaseEncoder() {
    }

    /**
     * 构造函数（指定编码器名称）
     * 
     * <p>使用指定的编码器名称创建编码器实例。</p>
     *
     * @param codecName 编码器名称，用于通过名称创建MediaCodec
     */
    BaseEncoder(String codecName) {
        this.mCodecName = codecName;
    }

    /**
     * 设置回调（基础接口版本）
     * 
     * <p>实现Encoder接口的方法，将回调转换为BaseEncoder.Callback类型。</p>
     *
     * @param callback 回调接口实例
     * @throws IllegalArgumentException 如果callback不是BaseEncoder.Callback类型
     */
    @Override
    public void setCallback(Encoder.Callback callback) {
        if (!(callback instanceof Callback)) {
            throw new IllegalArgumentException();
        }
        this.setCallback((Callback) callback);
    }

    /**
     * 设置回调（扩展接口版本）
     * 
     * <p>设置编码器回调，编码器将运行在异步模式。</p>
     *
     * @param callback 回调接口实例
     * @throws IllegalStateException 如果编码器已经创建
     */
    void setCallback(Callback callback) {
        if (this.mEncoder != null) throw new IllegalStateException("mEncoder is not null");
        this.mCallback = callback;
    }

    /**
     * 准备编码器
     * 
     * <p>必须在工作线程（HandlerThread）中调用！</p>
     * <p>创建MediaFormat配置，创建并配置MediaCodec编码器，然后启动编码器。</p>
     *
     * @throws IOException           如果编码器创建或配置失败
     * @throws IllegalStateException 如果不在工作线程或已准备过
     */
    @Override
    public void prepare() throws IOException {
        // 检查是否在工作线程
        if (Looper.myLooper() == null
                || Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("should run in a HandlerThread");
        }
        
        // 检查是否已准备
        if (mEncoder != null) {
            throw new IllegalStateException("prepared!");
        }
        
        // 创建MediaFormat配置
        MediaFormat format = createMediaFormat();
        Log.d("Encoder", "Create media format: " + format);

        // 获取MIME类型
        String mimeType = format.getString(MediaFormat.KEY_MIME);
        
        // 创建编码器
        final MediaCodec encoder = createEncoder(mimeType);
        
        try {
            // 如果设置了回调，使用异步模式
            if (this.mCallback != null) {
                // NOTE: MediaCodec maybe crash on some devices due to null callback
                encoder.setCallback(mCodecCallback);
            }
            
            // 配置编码器
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            
            // 编码器配置成功回调
            onEncoderConfigured(encoder);
            
            // 启动编码器
            encoder.start();
        } catch (MediaCodec.CodecException e) {
            Log.e("Encoder", "Configure codec failure!\n  with format" + format, e);
            throw e;
        }
        
        mEncoder = encoder;
    }

    /**
     * 编码器配置成功回调
     * 
     * <p>在MediaCodec配置成功后立即调用，子类可重写此方法进行额外配置。</p>
     *
     * @param encoder 配置成功的MediaCodec实例
     */
    protected void onEncoderConfigured(MediaCodec encoder) {
    }

    /**
     * 创建MediaCodec编码器实例
     * 
     * <p>优先使用指定的编码器名称创建，如果失败则使用MIME类型创建。</p>
     *
     * @param type MIME类型
     * @return MediaCodec实例
     * @throws IOException 如果创建编码器失败
     */
    private MediaCodec createEncoder(String type) throws IOException {
        try {
            // 优先使用指定的编码器名称
            if (this.mCodecName != null) {
                return MediaCodec.createByCodecName(mCodecName);
            }
        } catch (IOException e) {
            Log.w("@@", "Create MediaCodec by name '" + mCodecName + "' failure!", e);
        }
        // 使用MIME类型创建编码器
        return MediaCodec.createEncoderByType(type);
    }

    /**
     * 创建MediaFormat配置
     * 
     * <p>子类必须实现此方法，返回编码器的配置信息。</p>
     *
     * @return MediaFormat配置对象
     */
    protected abstract MediaFormat createMediaFormat();

    /**
     * 获取MediaCodec编码器实例
     *
     * @return MediaCodec实例
     * @throws NullPointerException 如果prepare()未调用
     */
    protected final MediaCodec getEncoder() {
        return Objects.requireNonNull(mEncoder, "doesn't prepare()");
    }

    /**
     * 获取输出缓冲区
     *
     * @param index 输出缓冲区索引
     * @return 输出缓冲区ByteBuffer
     * @throws NullPointerException 如果prepare()未调用
     * @see MediaCodec#getOutputBuffer(int)
     */
    public final ByteBuffer getOutputBuffer(int index) {
        return getEncoder().getOutputBuffer(index);
    }

    /**
     * 获取输入缓冲区
     *
     * @param index 输入缓冲区索引
     * @return 输入缓冲区ByteBuffer
     * @throws NullPointerException 如果prepare()未调用
     * @see MediaCodec#getInputBuffer(int)
     */
    public final ByteBuffer getInputBuffer(int index) {
        return getEncoder().getInputBuffer(index);
    }

    /**
     * 将数据填入输入缓冲区
     *
     * @param index  输入缓冲区索引
     * @param offset 数据偏移量
     * @param size   数据大小
     * @param pstTs  显示时间戳（微秒）
     * @param flags  缓冲区标志
     * @throws NullPointerException 如果prepare()未调用
     * @see MediaCodec#queueInputBuffer(int, int, int, long, int)
     * @see MediaCodec#getInputBuffer(int)
     */
    public final void queueInputBuffer(int index, int offset, int size, long pstTs, int flags) {
        getEncoder().queueInputBuffer(index, offset, size, pstTs, flags);
    }

    /**
     * 释放输出缓冲区
     *
     * @param index 输出缓冲区索引
     * @throws NullPointerException 如果prepare()未调用
     * @see MediaCodec#releaseOutputBuffer(int, boolean)
     */
    public final void releaseOutputBuffer(int index) {
        getEncoder().releaseOutputBuffer(index, false);
    }

    /**
     * 停止编码器
     * 
     * <p>停止编码过程，编码器可以重新启动。</p>
     *
     * @see MediaCodec#stop()
     */
    @Override
    public void stop() {
        if (mEncoder != null) {
            mEncoder.stop();
        }
    }

    /**
     * 释放编码器
     * 
     * <p>释放编码器资源，编码器将不可再使用。</p>
     *
     * @see MediaCodec#release()
     */
    @Override
    public void release() {
        if (mEncoder != null) {
            mEncoder.release();
            mEncoder = null;
        }
    }

    /** 指定的编码器名称 */
    private String mCodecName;
    
    /** MediaCodec编码器实例 */
    private MediaCodec mEncoder;
    
    /** 编码器回调 */
    private Callback mCallback;
    
    /**
     * MediaCodec回调
     * 
     * <p>当设置了mCallback时，MediaCodec将运行在异步模式。</p>
     */
    private MediaCodec.Callback mCodecCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(MediaCodec codec, int index) {
            // 输入缓冲区可用，转发给回调
            mCallback.onInputBufferAvailable(BaseEncoder.this, index);
        }

        @Override
        public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
            // 输出缓冲区可用，转发给回调
            mCallback.onOutputBufferAvailable(BaseEncoder.this, index, info);
        }

        @Override
        public void onError(MediaCodec codec, MediaCodec.CodecException e) {
            // 编码错误，转发给回调
            mCallback.onError(BaseEncoder.this, e);
        }

        @Override
        public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
            // 输出格式变化，转发给回调
            mCallback.onOutputFormatChanged(BaseEncoder.this, format);
        }
    };

}
