package com.micsig.base; // 定义基础工具类包

import java.io.File; // 导入文件类
import java.io.FileOutputStream; // 导入文件输出流类
import java.io.IOException; // 导入IO异常类
import java.io.InputStream; // 导入输入流接口

import okhttp3.Call; // 导入OkHttp Call类
import okhttp3.Callback; // 导入OkHttp回调接口
import okhttp3.OkHttpClient; // 导入OkHttp客户端类
import okhttp3.Request; // 导入OkHttp请求类
import okhttp3.Response; // 导入OkHttp响应类

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                             HttpUtil HTTP工具类                              │
 * │                          网络请求与文件下载处理器                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【模块定位】                                                                 │
 * │   MHO示波器基础工具模块 - 网络通信组件                                        │
 * │   提供HTTP请求和文件下载功能                                                 │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【核心职责】                                                                 │
 * │   1. 提供HTTP GET请求功能                                                   │
 * │   2. 提供文件下载功能，支持进度回调                                          │
 * │   3. 采用异步方式执行网络操作                                                │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【架构设计】                                                                 │
 * │   基于OkHttp库实现HTTP通信                                                  │
 * │   采用单例模式管理OkHttpClient实例                                           │
 * │   异步回调方式处理响应结果                                                   │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【数据流向】                                                                 │
 * │   URL请求 → OkHttpClient → 异步执行 → 回调通知                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【依赖关系】                                                                 │
 * │   依赖: okhttp3.OkHttpClient (HTTP客户端)                                    │
 * │   依赖: okhttp3.Request (请求构建)                                           │
 * │   依赖: okhttp3.Response (响应处理)                                          │
 * │   被依赖: 固件升级模块、远程配置模块、数据同步模块                            │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │ 【使用示例】                                                                 │
 * │   // 发送HTTP请求                                                          │
 * │   HttpUtil.get().doHttp("http://example.com/api", callback);               │
 * │   // 下载文件                                                              │
 * │   HttpUtil.get().download(url, dir, filename, listener);                   │
 * └──────────────────────────────────────────────────────────────────────────────┘
 * 
 * @author Micsig R&D Team
 * @version 1.0
 * @since MHO Series Oscilloscope Software
 */
public class HttpUtil { // HTTP工具类
    
    // ==================== 单例成员 ====================
    private static HttpUtil downloadUtil; // 单例实例
    private final OkHttpClient okHttpClient; // OkHttp客户端实例

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 获取单例实例                                                            │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   获取HttpUtil的全局唯一实例，懒加载创建                                  │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【返回值】                                                              │
     * │   @return HttpUtil单例实例                                              │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public static HttpUtil get() { // 获取单例方法
        if (downloadUtil == null) { // 检查实例是否存在
            downloadUtil = new HttpUtil(); // 创建新实例
        }
        return downloadUtil; // 返回单例实例
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 发送HTTP请求                                                           │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   发送异步HTTP GET请求                                                  │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param url      请求URL                                              │
     * │   @param callback 回调接口，处理响应或错误                                │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   HttpUtil.get().doHttp("http://example.com/api", new Callback() {     │
     * │       @Override public void onFailure(Call call, IOException e) { }    │
     * │       @Override public void onResponse(Call call, Response response) { }│
     * │   });                                                                  │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public void doHttp(String url, Callback callback) { // 发送HTTP请求方法
        Request request = new Request.Builder().url(url).build(); // 构建GET请求
        Call call = okHttpClient.newCall(request); // 创建Call对象
        call.enqueue(callback); // 异步执行请求
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 构造函数                                                               │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   初始化OkHttpClient实例                                                │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public HttpUtil() { // 构造函数
        okHttpClient = new OkHttpClient(); // 创建OkHttpClient实例
    }

    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 下载文件                                                               │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   异步下载文件到指定目录，支持进度回调                                     │
     * │   如果目标文件已存在，会先删除再下载                                       │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【参数说明】                                                            │
     * │   @param url          文件下载URL                                       │
     * │   @param destFileDir  目标文件目录                                       │
     * │   @param destFileName 目标文件名                                         │
     * │   @param listener     下载监听器，接收进度和结果回调                       │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【使用示例】                                                            │
     * │   HttpUtil.get().download(                                              │
     * │       "http://example.com/file.bin",                                    │
     * │       "/sdcard/download",                                               │
     * │       "firmware.bin",                                                   │
     * │       new OnDownloadListener() { ... }                                  │
     * │   );                                                                   │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public void download(final String url, final String destFileDir, final String destFileName, final OnDownloadListener listener) { // 下载文件方法
        Request request = new Request.Builder() // 构建请求
                .url(url) // 设置URL
                .build(); // 构建完成
        //异步请求
        okHttpClient.newCall(request).enqueue(new Callback() { // 异步执行请求
            @Override
            public void onFailure(okhttp3.Call call, IOException e) { // 请求失败回调
                e.printStackTrace(); // 打印异常堆栈
                listener.onDownloadFailed(e); // 通知监听器下载失败
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException { // 请求成功回调
                if (response.isSuccessful()) { // 检查响应是否成功
                    InputStream is = null; // 输入流
                    byte[] buf = new byte[4096]; // 4KB缓冲区
                    int len = 0; // 读取长度
                    FileOutputStream fos = null; // 文件输出流

                    //储存下载文件的目录
                    File dir = new File(destFileDir); // 创建目录文件对象
                    if (!dir.exists()) { // 检查目录是否存在
                        dir.mkdirs(); // 创建目录
                    }
                    File file = new File(dir, destFileName); // 创建目标文件对象
                    if(file.exists()){ // 检查文件是否存在
                        file.delete(); // 删除已存在的文件
                    }
                    try {
                        is = response.body().byteStream(); // 获取响应体输入流
                        long total = response.body().contentLength(); // 获取文件总大小
                        fos = new FileOutputStream(file); // 创建文件输出流
                        long sum = 0; // 已下载字节数
                        while ((len = is.read(buf)) != -1) { // 循环读取数据
                            fos.write(buf, 0, len); // 写入文件
                            sum += len; // 累加已下载字节数
                            int progress = (int) (sum * 1.0f / total * 100); // 计算下载进度百分比
                            listener.onDownloading(progress); // 通知进度更新
                        }
                        fos.flush(); // 刷新缓冲区
                        listener.onDownloadSuccess(file); // 通知下载成功
                    } catch (Exception e) { // 捕获异常
                        listener.onDownloadFailed(e); // 通知下载失败
                    } finally {
                        try {
                            if (is != null) { // 检查输入流
                                is.close(); // 关闭输入流
                            }
                            if (fos != null) { // 检查输出流
                                fos.close(); // 关闭输出流
                            }
                        } catch (IOException e) { // 捕获关闭异常
                            // 忽略关闭异常
                        }
                    }
                }else{ // 响应失败
                    response.close(); // 关闭响应
                }
            }

        });
    }


    /**
     * ┌────────────────────────────────────────────────────────────────────────┐
     * │ 下载监听器接口                                                         │
     * ├────────────────────────────────────────────────────────────────────────┤
     * │ 【功能说明】                                                            │
     * │   定义文件下载的回调接口，用于接收下载进度和结果                            │
     * └────────────────────────────────────────────────────────────────────────┘
     */
    public interface OnDownloadListener { // 下载监听器接口

        /**
         * ┌────────────────────────────────────────────────────────────────────────┐
         * │ 下载成功回调                                                            │
         * ├────────────────────────────────────────────────────────────────────────┤
         * │ 【功能说明】                                                            │
         * │   文件下载成功时调用                                                    │
         * ├────────────────────────────────────────────────────────────────────────┤
         * │ 【参数说明】                                                            │
         * │   @param file 下载成功的文件对象                                        │
         * └────────────────────────────────────────────────────────────────────────┘
         */
        void onDownloadSuccess(File file); // 下载成功回调方法

        /**
         * ┌────────────────────────────────────────────────────────────────────────┐
         * │ 下载进度回调                                                            │
         * ├────────────────────────────────────────────────────────────────────────┤
         * │ 【功能说明】                                                            │
         * │   下载过程中周期性调用，报告当前进度                                      │
         * ├────────────────────────────────────────────────────────────────────────┤
         * │ 【参数说明】                                                            │
         * │   @param progress 下载进度（0-100）                                     │
         * └────────────────────────────────────────────────────────────────────────┘
         */
        void onDownloading(int progress); // 下载进度回调方法

        /**
         * ┌────────────────────────────────────────────────────────────────────────┐
         * │ 下载失败回调                                                            │
         * ├────────────────────────────────────────────────────────────────────────┤
         * │ 【功能说明】                                                            │
         * │   文件下载失败时调用                                                    │
         * ├────────────────────────────────────────────────────────────────────────┤
         * │ 【参数说明】                                                            │
         * │   @param e 异常信息                                                    │
         * └────────────────────────────────────────────────────────────────────────┘
         */
        void onDownloadFailed(Exception e); // 下载失败回调方法
    }
}
