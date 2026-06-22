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

import java.util.Objects;

/**
 * 音频编码配置类
 * 
 * <p>封装音频编码器的配置参数，用于创建音频编码MediaFormat。
 * 该类是音频录制功能的核心配置类，定义了音频编码的各项参数。</p>
 * 
 * <p>主要配置参数包括：</p>
 * <ul>
 *   <li>codecName - 编码器名称（可选）</li>
 *   <li>mimeType - MIME类型（必填），如"audio/mp4a-latm"</li>
 *   <li>bitRate - 比特率，单位bps</li>
 *   <li>sampleRate - 采样率，如44100Hz、48000Hz</li>
 *   <li>channelCount - 声道数，1为单声道，2为立体声</li>
 *   <li>profile - AAC配置文件，如AAC-LC、HE-AAC等</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * AudioEncodeConfig config = new AudioEncodeConfig(
 *     null,                    // 编码器名称，null表示自动选择
 *     "audio/mp4a-latm",       // AAC编码
 *     128000,                  // 128kbps比特率
 *     44100,                   // 44.1kHz采样率
 *     2,                       // 立体声
 *     MediaCodecInfo.CodecProfileLevel.AACObjectLC  // AAC-LC配置
 * );
 * }</pre>
 * 
 * @author yrom
 * @version 2017/12/3
 * @since 1.0
 * @see AudioEncoder
 * @see MediaFormat
 */
public class AudioEncodeConfig {
    
    /** 编码器名称，可为null表示自动选择 */
    final String codecName;
    
    /** MIME类型，如"audio/mp4a-latm"（AAC） */
    final String mimeType;
    
    /** 比特率，单位bps（比特每秒） */
    final int bitRate;
    
    /** 采样率，单位Hz，常用值：44100、48000 */
    final int sampleRate;
    
    /** 声道数，1=单声道，2=立体声 */
    final int channelCount;
    
    /** AAC配置文件，如AACObjectLC、AACObjectHE等 */
    final int profile;

    /**
     * 构造函数
     * 
     * <p>创建音频编码配置对象。所有参数在构造时设置，之后不可修改。</p>
     * 
     * <p>参数说明：</p>
     * <ul>
     *   <li>codecName：指定编码器名称，传null则系统自动选择合适的编码器</li>
     *   <li>mimeType：必填，通常使用"audio/mp4a-latm"表示AAC编码</li>
     *   <li>bitRate：建议值64kbps-320kbps，值越大音质越好但文件越大</li>
     *   <li>sampleRate：常用44100Hz（CD音质）或48000Hz（专业音频）</li>
     *   <li>channelCount：1=单声道，2=立体声</li>
     *   <li>profile：AAC配置文件，常用AACObjectLC（低复杂度）</li>
     * </ul>
     * 
     * @param codecName 编码器名称，传null表示自动选择
     * @param mimeType MIME类型，不能为null，如"audio/mp4a-latm"
     * @param bitRate 比特率，单位bps
     * @param sampleRate 采样率，单位Hz
     * @param channelCount 声道数（1或2）
     * @param profile AAC配置文件常量
     * @throws NullPointerException 如果mimeType为null
     */
    public AudioEncodeConfig(String codecName, String mimeType,
                             int bitRate, int sampleRate, int channelCount, int profile) {
        this.codecName = codecName;
        // mimeType不能为null，否则抛出NullPointerException
        this.mimeType = Objects.requireNonNull(mimeType);
        this.bitRate = bitRate;
        this.sampleRate = sampleRate;
        this.channelCount = channelCount;
        this.profile = profile;
    }

    /**
     * 转换为MediaFormat对象
     * 
     * <p>将当前配置转换为MediaCodec可用的MediaFormat对象。
     * 设置音频格式的基本参数和编码参数。</p>
     * 
     * <p>设置的参数包括：</p>
     * <ul>
     *   <li>MIME类型</li>
     *   <li>采样率</li>
     *   <li>声道数</li>
     *   <li>AAC配置文件</li>
     *   <li>比特率</li>
     * </ul>
     * 
     * @return 配置好的MediaFormat对象
     */
    MediaFormat toFormat() {
        // 创建音频格式的MediaFormat，必须指定MIME类型、采样率和声道数
        MediaFormat format = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount);
        
        // 设置AAC配置文件
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, profile);
        
        // 设置比特率
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        
        // 可选：设置最大输入缓冲区大小
        //format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4096 * 4);
        
        return format;
    }

    /**
     * 返回配置信息的字符串表示
     * 
     * <p>用于调试和日志输出，显示所有配置参数。</p>
     * 
     * @return 包含所有配置参数的字符串
     */
    @Override
    public String toString() {
        return "AudioEncodeConfig{" +
                "codecName='" + codecName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", bitRate=" + bitRate +
                ", sampleRate=" + sampleRate +
                ", channelCount=" + channelCount +
                ", profile=" + profile +
                '}';
    }
}
