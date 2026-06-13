package com.micsig.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {
    private static HttpUtil downloadUtil;
    private final OkHttpClient okHttpClient;

    public static HttpUtil get() {
        if (downloadUtil == null) {
            downloadUtil = new HttpUtil();
        }
        return downloadUtil;
    }
    public void doHttp(String url, Callback callback) {
        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public HttpUtil() {
        okHttpClient = new OkHttpClient();
    }

    public void download(final String url, final String destFileDir, final String destFileName, final OnDownloadListener listener) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        //异步请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                e.printStackTrace();
                listener.onDownloadFailed(e);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream is = null;
                    byte[] buf = new byte[4096];
                    int len = 0;
                    FileOutputStream fos = null;

                    //储存下载文件的目录
                    File dir = new File(destFileDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, destFileName);
                    if(file.exists()){
                        file.delete();
                    }
                    try {

                        is = response.body().byteStream();
                        long total = response.body().contentLength();
                        fos = new FileOutputStream(file);
                        long sum = 0;
                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                            sum += len;
                            int progress = (int) (sum * 1.0f / total * 100);
                            listener.onDownloading(progress);
                        }
                        fos.flush();
                        listener.onDownloadSuccess(file);
                    } catch (Exception e) {
                        listener.onDownloadFailed(e);
                    } finally {

                        try {
                            if (is != null) {
                                is.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (IOException e) {

                        }
                    }
                }else{
                    response.close();
                }
            }

        });
    }


    public interface OnDownloadListener {

        /**
         * 下载成功之后的文件
         */
        void onDownloadSuccess(File file);

        /**
         * 下载进度
         */
        void onDownloading(int progress);

        /**
         * 下载异常信息
         */

        void onDownloadFailed(Exception e);
    }
}
