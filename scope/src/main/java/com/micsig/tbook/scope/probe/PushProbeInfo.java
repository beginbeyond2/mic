package com.micsig.tbook.scope.probe;

import android.util.Log;

import com.micsig.tbook.scope.Scope;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                PushProbeInfo - 探头信息推送类                                 ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Probe模块的探头信息推送类，位于probe包下，                                    ║
 * ║   负责将探头设备信息推送到服务器进行记录和管理。                                 ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 构建探头设备信息数据                                                     ║
 * ║   2. 通过HTTP POST请求推送设备信息到服务器                                     ║
 * ║   3. 处理推送结果（成功/失败）                                                 ║
 * ║                                                                              ║
 * ║ 【推送流程】                                                                 ║
 * ║   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                   ║
 * ║   │ 获取探头信息 │───▶│ 构建设备数据 │───▶│ HTTP POST   │                   ║
 * ║   │ BaseProbe   │    │ DevInfoBean │    │ 推送到服务器 │                   ║
 * ║   └─────────────┘    └─────────────┘    └──────┬──────┘                   ║
 * ║                                                 │                          ║
 * ║                                                 ▼                          ║
 * ║                                        ┌─────────────┐                     ║
 * ║                                        │ 处理响应结果 │                     ║
 * ║                                        │ (成功/失败) │                     ║
 * ║                                        └─────────────┘                     ║
 * ║                                                                              ║
 * ║ 【推送数据结构】                                                             ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        推送数据字段                                   │ ║
 * ║   │                                                                      │ ║
 * ║   │   【基本信息】                                                        │ ║
 * ║   │   devType    设备类型（探头型号，如MSP500、MDP）                       │ ║
 * ║   │   devSn      设备序列号（探头唯一生产编号）                            │ ║
 * ║   │   devUuid    设备UUID（使用序列号作为UUID）                           │ ║
 * ║   │                                                                      │ ║
 * ║   │   【备注信息（JSON格式）】                                            │ ║
 * ║   │   remark: {                                                          │ ║
 * ║   │       scopeType: 示波器型号                                          │ ║
 * ║   │       scopeSn:   示波器序列号                                        │ ║
 * ║   │       ver:       探头固件版本                                        │ ║
 * ║   │   }                                                                  │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【服务器接口】                                                               ║
 * ║   URL: {baseUrl}:8088/devInfo/add                                           ║
 * ║   Method: POST                                                               ║
 * ║   Content-Type: application/x-www-form-urlencoded                           ║
 * ║                                                                              ║
 * ║ 【应用场景】                                                                 ║
 * ║   1. 探头连接时推送设备信息                                                  ║
 * ║   2. 探头识别后记录设备信息                                                  ║
 * ║   3. 设备管理和追踪                                                          ║
 * ║   4. 售后服务和质保管理                                                      ║
 * ║                                                                              ║
 * ║ 【网络通信】                                                                 ║
 * ║   - 使用OkHttp库进行HTTP请求                                                ║
 * ║   - 异步请求，不阻塞主线程                                                   ║
 * ║   - 通过回调处理响应结果                                                     ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 静态方法，无实例状态                                                    ║
 * ║   - 异步请求，回调在子线程执行                                               ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - BaseProbe: 获取探头信息                                                 ║
 * ║   - DevInfoBean: 设备信息数据类                                             ║
 * ║   - ProbeUtils: 获取服务器基础URL                                           ║
 * ║   - Scope: 获取示波器信息                                                   ║
 * ║   - OkHttp: HTTP网络请求库                                                  ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */
public class PushProbeInfo {

    // ═══════════════════════════════════════════════════════════════════════════════
    // 常量定义
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 日志标签
     * 用于标识本类的日志输出
     */
    private static String TAG = "PushProbeInfo";

    /**
     * 服务器接口URL
     * 用于推送设备信息的服务器地址
     * 格式：{baseUrl}:8088/devInfo/add
     * 
     * <p><b>URL组成：</b></p>
     * <ul>
     *   <li>baseUrl: 根据系统语言选择的服务器地址（国内/国际）</li>
     *   <li>端口: 8088</li>
     *   <li>路径: /devInfo/add</li>
     * </ul>
     */
    final static String url= ProbeUtils.getBaseUrl() + ":8088/devInfo/add";

    // ═══════════════════════════════════════════════════════════════════════════════
    // 公共方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 推送探头设备信息
     * 从BaseProbe提取探头信息，构建设备数据并推送到服务器
     * 
     * <p><b>推送流程：</b></p>
     * <ol>
     *   <li>获取示波器实例</li>
     *   <li>创建DevInfoBean并设置基本信息</li>
     *   <li>构建JSON格式的备注信息</li>
     *   <li>异步发送HTTP POST请求</li>
     * </ol>
     * 
     * <p><b>推送数据：</b></p>
     * <ul>
     *   <li>devType: 探头型号（如MSP500、MDP）</li>
     *   <li>devSn: 探头序列号</li>
     *   <li>devUuid: 探头UUID（使用序列号）</li>
     *   <li>remark: JSON格式备注（示波器型号、序列号、探头版本）</li>
     * </ul>
     *
     * @param baseProbe 探头实例，用于获取探头信息
     */
    public static void postDevInfo(BaseProbe baseProbe)  {

        Scope s = Scope.getInstance();                                               // 获取示波器单例实例
        DevInfoBean devInfoBean = new DevInfoBean();                                 // 创建设备信息Bean
        devInfoBean.setDevSn(baseProbe.getSN());                                     // 设置设备序列号
        devInfoBean.setDevType(baseProbe.getProbeName());                            // 设置设备类型（探头型号）
        devInfoBean.setDevUuid(baseProbe.getSN());                                   // 设置设备UUID（使用序列号）

        JSONObject jsonObject = new JSONObject();                                    // 创建JSON对象用于备注信息

        try {

            jsonObject.put("scopeType", s.getProduct());                              // 添加示波器型号
            jsonObject.put("scopeSn", s.getSn());                                     // 添加示波器序列号
            jsonObject.put("ver",baseProbe.getVersion());                             // 添加探头固件版本

        }catch (JSONException e){
            e.printStackTrace();                                                    // 打印异常堆栈
        }
        devInfoBean.setRemark(jsonObject.toString());                                // 设置备注信息（JSON字符串）
        Log.d(TAG,devInfoBean.toString());                                           // 打印设备信息日志
        postDevInfo(devInfoBean, new Callback() {                                    // 异步推送设备信息
            @Override
            public void onFailure(Call call, IOException e) {                        // 推送失败回调
                Log.d(TAG, "onFailure() called with: call = [" + call + "], e = [" + e + "]"); // 打印失败日志
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException { // 推送成功回调
                Log.d(TAG, "onResponse() called with: call = [" + call + "], response = [" + response + "]"); // 打印成功日志
                if(response.isSuccessful()){                                          // 响应成功
                                                                                  // 可在此处处理成功逻辑
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // 私有方法
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * 推送设备信息到服务器
     * 使用OkHttp发送异步POST请求
     * 
     * <p><b>请求方式：</b></p>
     * <ul>
     *   <li>HTTP方法: POST</li>
     *   <li>Content-Type: application/x-www-form-urlencoded</li>
     *   <li>请求方式: 异步请求（enqueue）</li>
     * </ul>
     * 
     * <p><b>请求参数：</b></p>
     * <pre>
     * devType=xxx&devSn=xxx&devUuid=xxx&remark=xxx
     * </pre>
     *
     * @param devInfoBean 设备信息数据对象
     * @param callback HTTP响应回调接口
     */
    private static void postDevInfo(DevInfoBean devInfoBean , Callback callback){
        FormBody formBody = new FormBody.Builder()                                   // 创建表单请求体
                .add("devType", devInfoBean.getDevType())                            // 添加设备类型字段
                .add("devSn", devInfoBean.getDevSn())                                // 添加设备序列号字段
                .add("devUuid", devInfoBean.getDevUuid())                            // 添加设备UUID字段
                .add("remark", devInfoBean.getRemark())                              // 添加备注字段
                .build();                                                            // 构建请求体
        //1.创建OkHttpClient对象
        OkHttpClient okHttpClient = new OkHttpClient();                              // 创建OkHttp客户端
        Request request = new Request.Builder()                                      // 创建请求构建器
                .url(url)                                                            // 设置请求URL
                .post(formBody)                                                      // 设置POST请求体
                .build();                                                            // 构建请求对象
        //2.创建一个call对象,参数就是Request请求对象
        okHttpClient.newCall(request).enqueue(callback);                             // 异步执行请求并注册回调
    }
}
