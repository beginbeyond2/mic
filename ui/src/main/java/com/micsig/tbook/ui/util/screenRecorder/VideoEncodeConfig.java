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

import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.util.Objects;

/**
 * 视频编码配置类
 * 
 * <p>封装视频编码器的配置参数，用于创建视频编码MediaFormat。
 * 该类是屏幕录制功能的核心配置类，定义了视频编码的各项参数。</p>
 * 
 * <p>主要配置参数包括：</p>
 * <ul>
 *   <li>width - 视频宽度（像素）</li>
 *   <li>height - 视频高度（像素）</li>
 *   <li>bitrate - 比特率，单位bps</li>
 *   <li>framerate - 帧率，单位fps</li>
 *   <li>iframeInterval - I帧间隔，单位秒</li>
 *   <li>codecName - 编码器名称（可选）</li>
 *   <li>mimeType - MIME类型（必填），如"video/avc"</li>
 *   <li>codecProfileLevel - 编码配置文件和级别（可选）</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * MediaCodecInfo.CodecProfileLevel profileLevel = new MediaCodecInfo.CodecProfileLevel();
 * profileLevel.profile = MediaCodecInfo.CodecProfileLevel.AVCProfileHigh;
 * profileLevel.level = MediaCodecInfo.CodecProfileLevel.AVCLevel41;
 * 
 * VideoEncodeConfig config = new VideoEncodeConfig(
 *     1920,                    // 宽度
 *     1080,                    // 高度
 *     4000000,                 // 4Mbps比特率
 *     30,                      // 30fps帧率
 *     1,                       // 1秒I帧间隔
 *     null,                    // 编码器名称，null表示自动选择
 *     "video/avc",             // H.264编码
 *     profileLevel             // High Profile Level 4.1
 * );
 * }</pre>
 * 
 * @author yrom
 * @version 2017/12/3
 * @since 1.0
 * @see VideoEncoder
 * @see MediaFormat
 */
public class VideoEncodeConfig {
    
    /** 视频宽度，单位像素 */
    final int width;
    
    /** 视频高度，单位像素 */
    final int height;
    
    /** 比特率，单位bps（比特每秒） */
    final int bitrate;
    
    /** 帧率，单位fps（帧每秒） */
    final int framerate;
    
    /** I帧间隔，单位秒 */
    final int iframeInterval;
    
    /** 编码器名称，可为null表示自动选择 */
    final String codecName;
    
    /** MIME类型，如"video/avc"（H.264） */
    final String mimeType;
    
    /** 编码配置文件和级别 */
    final MediaCodecInfo.CodecProfileLevel codecProfileLevel;

    /**
     * 构造函数
     * 
     * <p>创建视频编码配置对象。所有参数在构造时设置，之后不可修改。</p>
     * 
     * <p>参数说明：</p>
     * <ul>
     *   <li>width/height：视频分辨率，建议使用16的倍数</li>
     *   <li>bitrate：建议值1Mbps-10Mbps，值越大画质越好但文件越大</li>
     *   <li>framerate：常用值15、24、30、60</li>
     *   <li>iframeInterval：I帧间隔，建议1-2秒</li>
     *   <li>codecName：指定编码器名称，传null则系统自动选择</li>
     *   <li>mimeType：必填，通常使用"video/avc"表示H.264编码</li>
     *   <li>codecProfileLevel：配置文件和级别，影响编码质量和兼容性</li>
     * </ul>
     * 
     * @param width 视频宽度，单位像素
     * @param height 视频高度，单位像素
     * @param bitrate 比特率，单位bps
     * @param framerate 帧率，单位fps
     * @param iframeInterval I帧间隔，单位秒
     * @param codecName 编码器名称，传null表示自动选择
     * @param mimeType MIME类型，不能为null，如"video/avc"
     * @param codecProfileLevel 编码配置文件和级别，可为null
     * @throws NullPointerException 如果mimeType为null
     */
    public VideoEncodeConfig(int width, int height, int bitrate,
                             int framerate, int iframeInterval,
                             String codecName, String mimeType,
                             MediaCodecInfo.CodecProfileLevel codecProfileLevel) {
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;
        this.framerate = framerate;
        this.iframeInterval = iframeInterval;
        this.codecName = codecName;
        // mimeType不能为null，否则抛出NullPointerException
        this.mimeType = Objects.requireNonNull(mimeType);
        this.codecProfileLevel = codecProfileLevel;
    }

    /**
     * 转换为MediaFormat对象
     * 
     * <p>将当前配置转换为MediaCodec可用的MediaFormat对象。
     * 设置视频格式的基本参数和编码参数。</p>
     * 
     * <p>设置的参数包括：</p>
     * <ul>
     *   <li>MIME类型</li>
     *   <li>视频分辨率（宽、高）</li>
     *   <li>颜色格式（Surface）</li>
     *   <li>比特率</li>
     *   <li>帧率</li>
     *   <li>I帧间隔</li>
     *   <li>配置文件和级别（可选）</li>
     * </ul>
     * 
     * @return 配置好的MediaFormat对象
     */
    MediaFormat toFormat() {
        // 创建视频格式的MediaFormat，必须指定MIME类型、宽度和高度
        MediaFormat format = MediaFormat.createVideoFormat(mimeType, width, height);
        
        // 设置颜色格式为Surface，用于屏幕录制
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        
        // 设置比特率
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        
        // 设置帧率
        format.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        
        // 设置I帧间隔
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframeInterval);
        
        // 如果指定了配置文件和级别，设置到MediaFormat
        if (codecProfileLevel != null && codecProfileLevel.profile != 0 && codecProfileLevel.level != 0) {
            format.setInteger(MediaFormat.KEY_PROFILE, codecProfileLevel.profile);
            format.setInteger("level", codecProfileLevel.level);
        }
        
        // maybe useful
        // 可选：设置重复上一帧的超时时间
        // format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 10_000_000);
        
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
        return "VideoEncodeConfig{" +
                "width=" + width +
                ", height=" + height +
                ", bitrate=" + bitrate +
                ", framerate=" + framerate +
                ", iframeInterval=" + iframeInterval +
                ", codecName='" + codecName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", codecProfileLevel=" + (codecProfileLevel == null ? "" : Utils.avcProfileLevelToString(codecProfileLevel)) +
                '}';
    }
}
