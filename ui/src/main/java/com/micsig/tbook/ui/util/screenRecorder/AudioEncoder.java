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

import android.media.MediaFormat;

/**
 * 音频编码器类
 * 
 * <p>继承自{@link BaseEncoder}，专门用于音频数据的编码。
 * 该类封装了AAC音频编码的具体实现，是屏幕录制功能中音频处理的核心组件。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>基于AudioEncodeConfig配置创建音频编码器</li>
 *   <li>提供音频专用的MediaFormat创建逻辑</li>
 *   <li>继承BaseEncoder的编码流程控制</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * // 创建音频编码配置
 * AudioEncodeConfig config = new AudioEncodeConfig(
 *     null, "audio/mp4a-latm", 128000, 44100, 2, 
 *     MediaCodecInfo.CodecProfileLevel.AACObjectLC
 * );
 * 
 * // 创建音频编码器
 * AudioEncoder encoder = new AudioEncoder(config);
 * 
 * // 启动编码
 * encoder.start();
 * }</pre>
 * 
 * <p>编码流程：</p>
 * <ol>
 *   <li>创建AudioEncodeConfig配置对象</li>
 *   <li>实例化AudioEncoder</li>
 *   <li>调用start()启动编码器</li>
 *   <li>循环调用encode()进行编码</li>
 *   <li>调用stop()停止编码</li>
 * </ol>
 * 
 * @author yrom
 * @version 2017/12/3
 * @since 1.0
 * @see BaseEncoder
 * @see AudioEncodeConfig
 * @see Encoder
 */
class AudioEncoder extends BaseEncoder {
    
    /** 音频编码配置对象，存储采样率、比特率等参数 */
    private final AudioEncodeConfig mConfig;

    /**
     * 构造函数
     * 
     * <p>创建音频编码器实例。通过配置对象初始化编码器参数，
     * 并将编码器名称传递给父类BaseEncoder。</p>
     * 
     * <p>如果配置中的codecName为null，系统将自动选择合适的AAC编码器。</p>
     * 
     * @param config 音频编码配置对象，不能为null
     */
    AudioEncoder(AudioEncodeConfig config) {
        // 调用父类构造函数，传入编码器名称（可能为null）
        super(config.codecName);
        
        // 保存配置对象
        this.mConfig = config;
    }

    /**
     * 创建音频MediaFormat
     * 
     * <p>实现父类的抽象方法，根据配置创建音频专用的MediaFormat对象。
     * 该MediaFormat包含音频编码所需的所有参数，如采样率、声道数、比特率等。</p>
     * 
     * <p>MediaFormat配置内容：</p>
     * <ul>
     *   <li>MIME类型：audio/mp4a-latm（AAC）</li>
     *   <li>采样率：如44100Hz</li>
     *   <li>声道数：1或2</li>
     *   <li>AAC配置文件：如AAC-LC</li>
     *   <li>比特率：如128000bps</li>
     * </ul>
     * 
     * @return 配置好的音频MediaFormat对象
     */
    @Override
    protected MediaFormat createMediaFormat() {
        // 委托给配置对象的toFormat方法创建MediaFormat
        return mConfig.toFormat();
    }

}
