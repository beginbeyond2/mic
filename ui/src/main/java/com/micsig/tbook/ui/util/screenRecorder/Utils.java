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
import android.media.MediaCodecList;
import android.os.AsyncTask;
import android.util.SparseArray;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 编解码器工具类
 * 
 * <p>提供MediaCodec编码器查找和配置信息处理的工具方法。主要功能包括：</p>
 * <ul>
 *   <li>根据MIME类型查找支持的编码器</li>
 *   <li>编码器配置文件（Profile）和级别（Level）的字符串转换</li>
 *   <li>颜色格式的可读性转换</li>
 *   <li>异步编码器查找功能</li>
 * </ul>
 * 
 * <p>该类使用反射机制从MediaCodecInfo.CodecProfileLevel和CodecCapabilities中
 * 提取常量定义，实现配置值与可读字符串之间的双向转换。</p>
 * 
 * <p>支持的编码格式：</p>
 * <ul>
 *   <li>AVC/H.264视频编码</li>
 *   <li>AAC音频编码</li>
 * </ul>
 * 
 * @author Yrom Wang
 * @version 1.0
 * @since 1.0
 */
class Utils {

    /**
     * 回调接口
     * 
     * <p>用于异步编码器查找操作的结果回调。</p>
     * 
     * @since 1.0
     */
    interface Callback {
        /**
         * 结果回调方法
         * 
         * <p>当异步编码器查找完成时调用，返回找到的编码器信息数组。</p>
         * 
         * @param infos 找到的编码器信息数组，如果没有找到则返回空数组
         */
        void onResult(MediaCodecInfo[] infos);
    }

    /**
     * 编码器查找异步任务
     * 
     * <p>继承自AsyncTask，在后台线程中执行编码器查找操作，
     * 避免阻塞UI线程。适用于需要查询大量编码器信息的场景。</p>
     * 
     * @since 1.0
     */
    static final class EncoderFinder extends AsyncTask<String, Void, MediaCodecInfo[]> {
        
        /** 回调函数，用于返回查找结果 */
        private Callback func;

        /**
         * 构造函数
         * 
         * <p>初始化编码器查找任务，设置结果回调。</p>
         * 
         * @param func 结果回调函数，不能为null
         */
        EncoderFinder(Callback func) {
            this.func = func;
        }

        /**
         * 后台执行编码器查找
         * 
         * <p>在工作线程中调用findEncodersByType方法查找编码器，
         * 避免阻塞主线程。</p>
         * 
         * @param mimeTypes MIME类型数组，仅使用第一个元素
         * @return 找到的编码器信息数组
         */
        @Override
        protected MediaCodecInfo[] doInBackground(String... mimeTypes) {
            // 调用同步方法查找编码器，仅使用传入的第一个MIME类型
            return findEncodersByType(mimeTypes[0]);
        }

        /**
         * 任务完成后回调
         * 
         * <p>在主线程中执行，通过回调函数将查找结果返回给调用者。</p>
         * 
         * @param mediaCodecInfos 查找到的编码器信息数组
         */
        @Override
        protected void onPostExecute(MediaCodecInfo[] mediaCodecInfos) {
            // 通过回调函数返回结果
            func.onResult(mediaCodecInfos);
        }
    }

    /**
     * 异步查找编码器
     * 
     * <p>在后台线程中查找支持指定MIME类型的编码器，
     * 通过回调返回结果。适用于不希望阻塞调用线程的场景。</p>
     * 
     * <p>使用示例：</p>
     * <pre>{@code
     * Utils.findEncodersByTypeAsync("video/avc", new Utils.Callback() {
     *     @Override
     *     public void onResult(MediaCodecInfo[] infos) {
     *         // 处理查找结果
     *     }
     * });
     * }</pre>
     * 
     * @param mimeType MIME类型，如"video/avc"、"audio/mp4a-latm"
     * @param callback 结果回调函数
     */
    static void findEncodersByTypeAsync(String mimeType, Callback callback) {
        // 创建并执行异步查找任务
        new EncoderFinder(callback).execute(mimeType);
    }

    /**
     * 查找支持指定MIME类型的编码器
     * 
     * <p>同步方法，遍历设备上所有编解码器，筛选出支持指定MIME类型的编码器。
     * 此方法可能耗时较长，建议在非UI线程中调用。</p>
     * 
     * <p>查找逻辑：</p>
     * <ol>
     *   <li>获取所有编解码器列表</li>
     *   <li>遍历列表，筛选编码器（isEncoder()为true）</li>
     *   <li>检查编码器是否支持指定的MIME类型</li>
     *   <li>收集所有符合条件的编码器信息</li>
     * </ol>
     * 
     * @param mimeType MIME类型，如"video/avc"（H.264）、"audio/mp4a-latm"（AAC）
     * @return 支持该MIME类型的编码器信息数组，如果没有找到则返回空数组
     */
    static MediaCodecInfo[] findEncodersByType(String mimeType) {
        // 创建编解码器列表，ALL_CODECS表示获取所有编解码器
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        
        // 用于存储找到的编码器信息
        List<MediaCodecInfo> infos = new ArrayList<>();
        
        // 遍历所有编解码器
        for (MediaCodecInfo info : codecList.getCodecInfos()) {
            // 跳过解码器，只处理编码器
            if (!info.isEncoder()) {
                continue;
            }
            
            try {
                // 尝试获取该MIME类型的能力配置
                MediaCodecInfo.CodecCapabilities cap = info.getCapabilitiesForType(mimeType);
                
                // 如果能力配置为null，跳过
                if (cap == null) continue;
            } catch (IllegalArgumentException e) {
                // 不支持该MIME类型，跳过
                continue;
            }
            
            // 编码器支持该MIME类型，添加到结果列表
            infos.add(info);
        }

        // 将List转换为数组返回
        return infos.toArray(new MediaCodecInfo[infos.size()]);
    }


    /** AAC编码配置文件映射表，键为配置值，值为配置名称 */
    static SparseArray<String> sAACProfiles = new SparseArray<>();
    
    /** AVC视频编码配置文件映射表，键为配置值，值为配置名称 */
    static SparseArray<String> sAVCProfiles = new SparseArray<>();
    
    /** AVC视频编码级别映射表，键为级别值，值为级别名称 */
    static SparseArray<String> sAVCLevels = new SparseArray<>();


    /**
     * 将AVC配置文件和级别转换为可读字符串
     * 
     * <p>将MediaCodecInfo.CodecProfileLevel对象转换为"Profile-Level"格式的字符串。
     * 例如："AVCProfileHigh-AVCLevel41"表示High Profile，Level 4.1。</p>
     * 
     * <p>如果配置文件或级别值在已知常量中不存在，则直接使用数值表示。</p>
     * 
     * @param avcProfileLevel AVC编码配置文件和级别对象
     * @return 格式化的字符串，如"AVCProfileHigh-AVCLevel41"
     */
    static String avcProfileLevelToString(MediaCodecInfo.CodecProfileLevel avcProfileLevel) {
        // 检查映射表是否已初始化，未初始化则先初始化
        if (sAVCProfiles.size() == 0 || sAVCLevels.size() == 0) {
            initProfileLevels();
        }
        
        String profile = null, level = null;
        
        // 在映射表中查找配置文件名称
        int i = sAVCProfiles.indexOfKey(avcProfileLevel.profile);
        if (i >= 0) {
            profile = sAVCProfiles.valueAt(i);
        }

        // 在映射表中查找级别名称
        i = sAVCLevels.indexOfKey(avcProfileLevel.level);
        if (i >= 0) {
            level = sAVCLevels.valueAt(i);
        }

        // 如果未找到配置文件名称，使用数值表示
        if (profile == null) {
            profile = String.valueOf(avcProfileLevel.profile);
        }
        
        // 如果未找到级别名称，使用数值表示
        if (level == null) {
            level = String.valueOf(avcProfileLevel.level);
        }
        
        // 返回"Profile-Level"格式的字符串
        return profile + '-' + level;
    }

    /**
     * 获取所有支持的AAC配置文件名称
     * 
     * <p>返回设备支持的所有AAC编码配置文件名称数组，
     * 如"AACObjectLC"、"AACObjectHE"等。</p>
     * 
     * @return AAC配置文件名称数组
     */
    static String[] aacProfiles() {
        // 检查映射表是否已初始化
        if (sAACProfiles.size() == 0) {
            initProfileLevels();
        }
        
        // 创建结果数组
        String[] profiles = new String[sAACProfiles.size()];
        
        // 将映射表中的值复制到数组
        for (int i = 0; i < sAACProfiles.size(); i++) {
            profiles[i] = sAACProfiles.valueAt(i);
        }
        
        return profiles;
    }

    /**
     * 将字符串转换为配置文件和级别对象
     * 
     * <p>解析"Profile-Level"格式的字符串，创建对应的CodecProfileLevel对象。
     * 支持AVC和AAC两种编码格式的配置。</p>
     * 
     * <p>字符串格式示例：</p>
     * <ul>
     *   <li>"AVCProfileHigh-AVCLevel41" - AVC High Profile Level 4.1</li>
     *   <li>"AACObjectLC" - AAC LC配置</li>
     *   <li>"2-64" - 使用数值表示（Profile 2, Level 64）</li>
     * </ul>
     * 
     * @param str 配置字符串
     * @return 解析后的CodecProfileLevel对象，解析失败返回null
     */
    static MediaCodecInfo.CodecProfileLevel toProfileLevel(String str) {
        // 确保映射表已初始化
        if (sAVCProfiles.size() == 0 || sAVCLevels.size() == 0 || sAACProfiles.size() == 0) {
            initProfileLevels();
        }
        
        String profile = str;
        String level = null;
        
        // 检查是否包含级别信息（以'-'分隔）
        int i = str.indexOf('-');
        if (i > 0) { 
            // AVC profile has level
            // 分离配置文件和级别
            profile = str.substring(0, i);
            level = str.substring(i + 1);
        }

        // 创建结果对象
        MediaCodecInfo.CodecProfileLevel res = new MediaCodecInfo.CodecProfileLevel();
        
        // 根据前缀判断编码类型并解析配置文件
        if (profile.startsWith("AVC")) {
            // AVC视频编码配置
            res.profile = keyOfValue(sAVCProfiles, profile);
        } else if (profile.startsWith("AAC")) {
            // AAC音频编码配置
            res.profile = keyOfValue(sAACProfiles, profile);
        } else {
            // 尝试解析为数值
            try {
                res.profile = Integer.parseInt(profile);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // 解析级别信息
        if (level != null) {
            if (level.startsWith("AVC")) {
                // AVC级别
                res.level = keyOfValue(sAVCLevels, level);
            } else {
                // 尝试解析为数值
                try {
                    res.level = Integer.parseInt(level);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        // 验证结果有效性：profile必须大于0，level必须大于等于0
        return res.profile > 0 && res.level >= 0 ? res : null;
    }

    /**
     * 根据值查找键
     * 
     * <p>在SparseArray中根据值反向查找对应的键。
     * 用于将配置名称转换为配置值。</p>
     * 
     * @param <T> 值的类型
     * @param array SparseArray映射表
     * @param value 要查找的值
     * @return 找到的键，未找到返回-1
     */
    private static <T> int keyOfValue(SparseArray<T> array, T value) {
        int size = array.size();
        
        // 遍历映射表查找值
        for (int i = 0; i < size; i++) {
            T t = array.valueAt(i);
            
            // 比较值（支持null和equals两种方式）
            if (t == value || t.equals(value)) {
                return array.keyAt(i);
            }
        }
        
        // 未找到返回-1
        return -1;
    }

    /**
     * 初始化配置文件和级别映射表
     * 
     * <p>使用反射机制从MediaCodecInfo.CodecProfileLevel类中提取所有静态常量，
     * 按名称前缀分类存储到对应的映射表中。</p>
     * 
     * <p>提取的常量类型：</p>
     * <ul>
     *   <li>AVCProfile* - AVC视频编码配置文件，存储到sAVCProfiles</li>
     *   <li>AVCLevel* - AVC视频编码级别，存储到sAVCLevels</li>
     *   <li>AACObject* - AAC音频编码配置，存储到sAACProfiles</li>
     * </ul>
     */
    private static void initProfileLevels() {
        // 获取CodecProfileLevel类的所有字段
        Field[] fields = MediaCodecInfo.CodecProfileLevel.class.getFields();
        
        for (Field f : fields) {
            // 只处理static final字段
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0) {
                continue;
            }
            
            String name = f.getName();
            SparseArray<String> target;
            
            // 根据字段名前缀确定目标映射表
            if (name.startsWith("AVCProfile")) {
                // AVC配置文件
                target = sAVCProfiles;
            } else if (name.startsWith("AVCLevel")) {
                // AVC级别
                target = sAVCLevels;
            } else if (name.startsWith("AACObject")) {
                // AAC配置
                target = sAACProfiles;
            } else {
                // 其他字段跳过
                continue;
            }
            
            try {
                // 将字段值和名称存入映射表
                target.put(f.getInt(null), name);
            } catch (IllegalAccessException e) {
                //ignored - 忽略访问异常
            }
        }
    }


    /** 颜色格式映射表，键为颜色格式值，值为颜色格式名称 */
    static SparseArray<String> sColorFormats = new SparseArray<>();

    /**
     * 将颜色格式值转换为可读字符串
     * 
     * <p>将MediaCodec颜色格式常量值转换为对应的常量名称。
     * 例如：21 -> "COLOR_YUV420SemiPlanar"</p>
     * 
     * <p>如果颜色格式值在已知常量中不存在，则返回十六进制表示。</p>
     * 
     * @param colorFormat 颜色格式值
     * @return 颜色格式名称或十六进制字符串
     */
    static String toHumanReadable(int colorFormat) {
        // 确保颜色格式映射表已初始化
        if (sColorFormats.size() == 0) {
            initColorFormatFields();
        }
        
        // 在映射表中查找颜色格式名称
        int i = sColorFormats.indexOfKey(colorFormat);
        if (i >= 0) return sColorFormats.valueAt(i);
        
        // 未找到则返回十六进制表示
        return "0x" + Integer.toHexString(colorFormat);
    }

    /**
     * 将字符串转换为颜色格式值
     * 
     * <p>支持两种输入格式：</p>
     * <ul>
     *   <li>颜色格式名称，如"COLOR_YUV420SemiPlanar"</li>
     *   <li>十六进制字符串，如"0x15"</li>
     * </ul>
     * 
     * @param str 颜色格式名称或十六进制字符串
     * @return 颜色格式值，无效输入返回0
     */
    static int toColorFormat(String str) {
        // 确保颜色格式映射表已初始化
        if (sColorFormats.size() == 0) {
            initColorFormatFields();
        }
        
        // 尝试从映射表中查找
        int color = keyOfValue(sColorFormats, str);
        if (color > 0) return color;
        
        // 尝试解析十六进制格式
        if (str.startsWith("0x")) {
            return Integer.parseInt(str.substring(2), 16);
        }
        
        // 无效输入返回0
        return 0;
    }

    /**
     * 初始化颜色格式映射表
     * 
     * <p>使用反射机制从MediaCodecInfo.CodecCapabilities类中提取所有以"COLOR_"开头的
     * 静态常量，建立颜色格式值与名称的映射关系。</p>
     * 
     * <p>常见的颜色格式包括：</p>
     * <ul>
     *   <li>COLOR_FormatYUV420Planar - YUV420平面格式</li>
     *   <li>COLOR_FormatYUV420SemiPlanar - YUV420半平面格式</li>
     *   <li>COLOR_FormatSurface - Surface格式</li>
     * </ul>
     */
    private static void initColorFormatFields() {
        // COLOR_
        // 获取CodecCapabilities类的所有字段
        Field[] fields = MediaCodecInfo.CodecCapabilities.class.getFields();
        
        for (Field f : fields) {
            // 只处理static final字段
            if ((f.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0) {
                continue;
            }
            
            String name = f.getName();
            
            // 只处理COLOR_开头的字段
            if (name.startsWith("COLOR_")) {
                try {
                    // 获取字段值并存入映射表
                    int value = f.getInt(null);
                    sColorFormats.put(value, name);
                } catch (IllegalAccessException e) {
                    // ignored - 忽略访问异常
                }
            }
        }

    }
}
