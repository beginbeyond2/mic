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

import java.io.IOException;

/**
 * 编码器接口
 * 
 * <p>定义MediaCodec编码器的基本操作接口。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>准备编码器：初始化和配置编码器</li>
 *   <li>停止编码器：停止编码过程</li>
 *   <li>释放编码器：释放编码器资源</li>
 *   <li>设置回调：设置编码器事件回调</li>
 * </ul>
 * 
 * <p>实现类：</p>
 * <ul>
 *   <li>{@link BaseEncoder}：编码器基类，提供MediaCodec的封装</li>
 *   <li>{@link VideoEncoder}：视频编码器实现</li>
 *   <li>{@link AudioEncoder}：音频编码器实现</li>
 * </ul>
 * 
 * @author yrom
 * @version 2017/12/4
 */
interface Encoder {
    
    /**
     * 准备编码器
     * 
     * <p>初始化MediaCodec编码器，配置编码参数并启动编码器。</p>
     * <p>此方法必须在工作线程（HandlerThread）中调用，不能在主线程调用。</p>
     *
     * @throws IOException 如果编码器创建或配置失败
     */
    void prepare() throws IOException;

    /**
     * 停止编码器
     * 
     * <p>停止编码过程，编码器可以重新启动。</p>
     */
    void stop();

    /**
     * 释放编码器
     * 
     * <p>释放编码器资源，编码器将不可再使用。</p>
     */
    void release();

    /**
     * 设置回调
     * 
     * <p>设置编码器事件回调接口，用于接收编码过程中的事件通知。</p>
     *
     * @param callback 回调接口实例
     */
    void setCallback(Callback callback);

    /**
     * 编码器回调接口
     * 
     * <p>定义编码器事件回调方法。</p>
     */
    interface Callback {
        
        /**
         * 编码错误回调
         * 
         * <p>当编码过程中发生错误时调用。</p>
         *
         * @param encoder   发生错误的编码器实例
         * @param exception 发生的异常
         */
        void onError(Encoder encoder, Exception exception);
    }
}
