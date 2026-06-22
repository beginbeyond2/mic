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
import android.util.Log;
import android.view.Surface;

import java.util.Objects;

/**
 * 视频编码器类
 * 
 * <p>继承自{@link BaseEncoder}，专门用于视频数据的编码。
 * 该类封装了H.264视频编码的具体实现，是屏幕录制功能中视频处理的核心组件。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>基于VideoEncodeConfig配置创建视频编码器</li>
 *   <li>提供视频专用的MediaFormat创建逻辑</li>
 *   <li>创建和管理输入Surface，用于接收屏幕画面</li>
 *   <li>继承BaseEncoder的编码流程控制</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * // 创建视频编码配置
 * VideoEncodeConfig config = new VideoEncodeConfig(
 *     1920, 1080, 4000000, 30, 1, null, "video/avc", null
 * );
 * 
 * // 创建视频编码器
 * VideoEncoder encoder = new VideoEncoder(config);
 * 
 * // 准备编码器
 * encoder.prepare();
 * 
 * // 获取输入Surface，用于MediaProjection
 * Surface surface = encoder.getInputSurface();
 * 
 * // 启动编码
 * encoder.start();
 * }</pre>
 * 
 * <p>编码流程：</p>
 * <ol>
 *   <li>创建VideoEncodeConfig配置对象</li>
 *   <li>实例化VideoEncoder</li>
 *   <li>调用prepare()准备编码器</li>
 *   <li>调用getInputSurface()获取输入Surface</li>
 *   <li>将Surface传递给MediaProjection</li>
 *   <li>编码器自动处理来自Surface的视频数据</li>
 *   <li>调用stop()停止编码</li>
 *   <li>调用release()释放资源</li>
 * </ol>
 * 
 * @author yrom
 * @version 2017/12/3
 * @since 1.0
 * @see BaseEncoder
 * @see VideoEncodeConfig
 * @see Encoder
 */
class VideoEncoder extends BaseEncoder {
    
    /** 是否输出详细日志 */
    private static final boolean VERBOSE = false;
    
    /** 视频编码配置对象 */
    private VideoEncodeConfig mConfig;
    
    /** 输入Surface，用于接收屏幕画面数据 */
    private Surface mSurface;


    /**
     * 构造函数
     * 
     * <p>创建视频编码器实例。通过配置对象初始化编码器参数，
     * 并将编码器名称传递给父类BaseEncoder。</p>
     * 
     * <p>如果配置中的codecName为null，系统将自动选择合适的H.264编码器。</p>
     * 
     * @param config 视频编码配置对象，不能为null
     */
    VideoEncoder(VideoEncodeConfig config) {
        // 调用父类构造函数，传入编码器名称（可能为null）
        super(config.codecName);
        
        // 保存配置对象
        this.mConfig = config;
    }

    /**
     * 编码器配置完成回调
     * 
     * <p>重写父类方法，在编码器配置完成后创建输入Surface。
     * 该Surface用于接收来自MediaProjection的屏幕画面数据。</p>
     * 
     * <p>注意：此方法在prepare()过程中被调用，在编码器启动前执行。</p>
     * 
     * @param encoder 已配置的MediaCodec实例
     */
    @Override
    protected void onEncoderConfigured(MediaCodec encoder) {
        // 创建输入Surface，用于接收屏幕画面
        mSurface = encoder.createInputSurface();
        
        if (VERBOSE) Log.i("@@", "VideoEncoder create input surface: " + mSurface);
    }

    /**
     * 创建视频MediaFormat
     * 
     * <p>实现父类的抽象方法，根据配置创建视频专用的MediaFormat对象。
     * 该MediaFormat包含视频编码所需的所有参数，如分辨率、比特率、帧率等。</p>
     * 
     * <p>MediaFormat配置内容：</p>
     * <ul>
     *   <li>MIME类型：video/avc（H.264）</li>
     *   <li>分辨率：宽x高</li>
     *   <li>颜色格式：Surface</li>
     *   <li>比特率：如4000000bps</li>
     *   <li>帧率：如30fps</li>
     *   <li>I帧间隔：如1秒</li>
     * </ul>
     * 
     * @return 配置好的视频MediaFormat对象
     */
    @Override
    protected MediaFormat createMediaFormat() {
        // 委托给配置对象的toFormat方法创建MediaFormat
        return mConfig.toFormat();
    }

    /**
     * 获取输入Surface
     * 
     * <p>返回用于接收屏幕画面的输入Surface。该Surface需要传递给
     * MediaProjection.createVirtualDisplay()方法。</p>
     * 
     * <p>注意：必须在调用prepare()后才能调用此方法，否则抛出NullPointerException。</p>
     * 
     * @return 输入Surface对象
     * @throws NullPointerException 如果prepare()未被调用
     */
    Surface getInputSurface() {
        // 确保Surface已创建
        return Objects.requireNonNull(mSurface, "doesn't prepare()");
    }

    /**
     * 释放资源
     * 
     * <p>释放输入Surface和编码器资源。必须在停止编码后调用。</p>
     * 
     * <p>释放顺序：</p>
     * <ol>
     *   <li>释放输入Surface</li>
     *   <li>调用父类release()释放编码器</li>
     * </ol>
     */
    @Override
    public void release() {
        // 释放输入Surface
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        
        // 调用父类方法释放编码器
        super.release();
    }


}
