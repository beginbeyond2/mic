package com.micsig.tbook.tbookscope;   // 示波器主应用包，包含Activity生命周期消息类

import android.content.Intent;   // 导入Android Intent类，用于携带Activity返回的数据

/**
 * Activity结果消息数据类 - 封装Activity结果回调的数据
 * 
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    Activity结果消息架构                          │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │  ┌─────────────────┐                                           │
 * │  │   OtherActivity │                                           │
 * │  │  (其他Activity)  │                                           │
 * │  │                 │                                           │
 * │  │  setResult()    │                                           │
 * │  │  finish()       │                                           │
 * │  └────────┬────────┘                                           │
 * │           │                                                     │
 * │           │ 返回结果                                            │
 * │           ▼                                                     │
 * │  ┌─────────────────┐                                           │
 * │  │   MainActivity  │                                           │
 * │  │  (主Activity)    │                                           │
 * │  │                 │                                           │
 * │  │  onActivityResult() │                                       │
 * │  │  ────────────→ │                                           │
 * │  │                 │                                           │
 * │  │  RxBus事件发送  │                                           │
 * │  └────────┬────────┘                                           │
 * │           │                                                     │
 * │           │ post(RxEnum.ACTIVITY_ACTIVITYRESULT,                │
 * │           │      new ActivityMsgResult(requestCode,              │
 * │           │                         resultCode, data))           │
 * │           ▼                                                     │
 * │  ┌─────────────────────────────────────────────┐               │
 * │  │           ActivityMsgResult                 │               │
 * │  │         (Activity结果消息)                   │               │
 * │  │                                             │               │
 * │  │  ┌─────────────────────────────────────┐   │               │
 * │  │  │  数据属性                            │   │               │
 * │  │  │  - requestCode: 请求码               │   │               │
 * │  │  │  - resultCode: 结果码               │   │               │
 * │  │  │  - data: Intent数据                 │   │               │
 * │  │  └─────────────────────────────────────┘   │               │
 * │  └─────────────────────────────────────────────┘               │
 * │                       │                                       │
 * │                       ▼                                       │
 * │           ┌─────────────────────┐                             │
 * │           │  RxBus订阅者         │                             │
 * │           │  (多个组件)          │                             │
 * │           │                     │                             │
 * │           │  - 收到结果消息      │                             │
 * │           │  - 处理返回数据      │                             │
 * │           │  - 执行后续操作      │                             │
 * │           └─────────────────────┘                             │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * <h3>模块定位</h3>
 * <p>本类是Activity生命周期消息系统的数据传输对象（DTO），用于在MainActivity的onActivityResult()方法中
 * 通过RxBus事件总线发送Activity结果通知。封装了requestCode（请求码）、resultCode（结果码）
 * 和data（Intent数据）三个关键参数，实现Activity结果回调的解耦通信。</p>
 * 
 * <h3>核心职责</h3>
 * <ul>
 *   <li><b>数据封装</b>：封装Activity结果回调的三个关键参数</li>
 *   <li><b>消息传递</b>：通过RxBus通知订阅者Activity结果已返回</li>
 *   <li><b>解耦通信</b>：实现Activity结果回调的组件间解耦</li>
 * </ul>
 * 
 * <h3>Activity结果回调说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              Activity结果回调详解                              │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  onActivityResult()机制：                                     │
 * │    - 用于接收其他Activity返回的结果                            │
 * │    - 当启动的Activity结束时回调                                │
 * │    - 传递请求码、结果码和返回数据                              │
 * │                                                               │
 * │  参数说明：                                                    │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  requestCode (请求码)                        │           │
 * │    │  - 用于标识请求来源                          │           │
 * │    │  - 区分不同的Activity启动请求                │           │
 * │    │  - 由startActivityForResult()传入            │           │
 * │    │                                             │           │
 * │    │  resultCode (结果码)                         │           │
 * │    │  - 用于标识操作结果                          │           │
 * │    │  - RESULT_OK: 操作成功                       │           │
 * │    │  - RESULT_CANCELED: 操作取消                 │           │
 * │    │  - RESULT_FIRST_USER: 自定义结果码           │           │
 * │    │                                             │           │
 * │    │  data (Intent数据)                           │           │
 * │    │  - 用于携带返回的数据                        │           │
 * │    │  - 包含返回的键值对                          │           │
 * │    │  - 可携带URI、字符串、数字等                 │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                                                               │
 * │  使用流程：                                                    │
 * │    Step 1: MainActivity启动其他Activity                       │
 * │      └─ startActivityForResult(intent, requestCode)           │
 * │                                                               │
 * │    Step 2: 其他Activity处理请求                               │
 * │      └─ 执行操作，设置结果                                     │
 * │      └─ setResult(resultCode, data)                           │
 * │      └─ finish()                                              │
 * │                                                               │
 * │    Step 3: MainActivity接收回调                               │
 * │      └─ onActivityResult(requestCode, resultCode, data)       │
 * │      └─ 根据requestCode判断请求来源                           │
 * │      └─ 根据resultCode判断操作结果                             │
 * │      └─ 从data中提取返回数据                                   │
 * │                                                               │
 * │    Step 4: 发送RxBus事件                                      │
 * │      └─ RxBus.getInstance().post(                             │
 * │           RxEnum.ACTIVITY_ACTIVITYRESULT,                     │
 * │           new ActivityMsgResult(requestCode, resultCode, data))│
 * │                                                               │
 * │    Step 5: 订阅者处理结果                                      │
 * │      └─ 收到ActivityMsgResult消息                              │
 * │      └─ 根据requestCode和resultCode执行相应操作               │
 * │      └─ 从data中提取数据并处理                                 │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>requestCode请求码说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              requestCode请求码详解                             │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  请求码用途：                                                  │
 * │    - 区分不同的Activity启动请求                                │
 * │    - 在onActivityResult()中判断请求来源                        │
 * │    - 一个Activity可能启动多个不同的Activity                    │
 * │                                                               │
 * │  定义方式：                                                    │
 * │    - 使用常量定义请求码                                        │
 * │    - 使用不同的数值标识不同的请求                              │
 * │                                                               │
 * │  示例定义：                                                    │
 * │    public static final int REQUEST_CODE_PICK_FILE = 1;        │
 * │    public static final int REQUEST_CODE_PICK_IMAGE = 2;       │
 * │    public static final int REQUEST_CODE_SETTINGS = 3;         │
 * │    public static final int REQUEST_CODE_PERMISSION = 4;       │
 * │                                                               │
 * │  使用示例：                                                    │
 * │    // 启动文件选择Activity                                     │
 * │    startActivityForResult(                                    │
 * │      new Intent(this, FilePickerActivity.class),              │
 * │      REQUEST_CODE_PICK_FILE                                   │
 * │    );                                                          │
 * │                                                               │
 * │    // 在onActivityResult()中判断                               │
 * │    if (requestCode == REQUEST_CODE_PICK_FILE) {               │
 * │      // 处理文件选择结果                                       │
 * │    }                                                           │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>resultCode结果码说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              resultCode结果码详解                              │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  标准结果码（Activity类定义）：                                │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  RESULT_OK = -1                              │           │
 * │    │  - 操作成功                                   │           │
 * │    │  - 用户完成了请求的操作                       │           │
 * │    │                                             │           │
 * │    │  RESULT_CANCELED = 0                         │           │
 * │    │  - 操作取消                                   │           │
 * │    │  - 用户取消了操作或按返回键                   │           │
 * │    │                                             │           │
 * │    │  RESULT_FIRST_USER = 1                       │           │
 * │    │  - 自定义结果码起始值                         │           │
 * │    │  - 应用可以定义自己的结果码                   │           │
 * │    │  - 从RESULT_FIRST_USER开始递增               │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                                                               │
 * │  自定义结果码示例：                                            │
 * │    public static final int RESULT_ERROR = RESULT_FIRST_USER; │
 * │    public static final int RESULT_RETRY = RESULT_FIRST_USER + 1; │
 * │    public static final int RESULT_SKIP = RESULT_FIRST_USER + 2; │
 * │                                                               │
 * │  使用示例：                                                    │
 * │    // 在其他Activity中设置结果                                 │
 * │    if (success) {                                             │
 * │      setResult(RESULT_OK, dataIntent);                        │
 * │    } else {                                                   │
 * │      setResult(RESULT_CANCELED);                              │
 * │    }                                                           │
 * │    finish();                                                  │
 * │                                                               │
 * │    // 在MainActivity中判断结果                                 │
 * │    if (resultCode == RESULT_OK) {                             │
 * │      // 操作成功，处理返回数据                                 │
 * │    } else if (resultCode == RESULT_CANCELED) {                │
 * │      // 操作取消，执行取消逻辑                                 │
 * │    }                                                           │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>Intent数据说明</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              Intent数据详解                                    │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  Intent用途：                                                  │
 * │    - 携带Activity返回的数据                                    │
 * │    - 传递键值对数据                                            │
 * │    - 可携带多种类型的数据                                      │
 * │                                                               │
 * │  数据类型：                                                    │
 * │    ┌─────────────────────────────────────────────┐           │
 * │    │  基本类型                                     │           │
 * │    │  - putExtra(String, int)                     │           │
 * │    │  - putExtra(String, long)                    │           │
 * │    │  - putExtra(String, float)                   │           │
 * │    │  - putExtra(String, double)                  │           │
 * │    │  - putExtra(String, boolean)                 │           │
 * │    │                                             │           │
 * │    │  字符串类型                                   │           │
 * │    │  - putExtra(String, String)                  │           │
 * │    │  - putExtra(String, String[])                │           │
 * │    │                                             │           │
 * │    │  复合类型                                     │           │
 * │    │  - putExtra(String, Bundle)                  │           │
 * │    │  - putExtra(String, Parcelable)              │           │
 * │    │  - putExtra(String, Serializable)            │           │
 * │    │                                             │           │
 * │    │  URI类型                                     │           │
 * │    │  - setData(Uri)                              │           │
 * │    │  - putExtra(String, Uri)                     │           │
 * │    └─────────────────────────────────────────────┘           │
 * │                                                               │
 * │  使用示例：                                                    │
 * │    // 在其他Activity中设置返回数据                             │
 * │    Intent dataIntent = new Intent();                          │
 * │    dataIntent.putExtra("selected_file", filePath);            │
 * │    dataIntent.putExtra("file_size", fileSize);                │
 * │    dataIntent.putExtra("is_success", true);                   │
 * │    setResult(RESULT_OK, dataIntent);                          │
 * │    finish();                                                  │
 * │                                                               │
 * │    // 在MainActivity中提取返回数据                             │
 * │    if (resultCode == RESULT_OK && data != null) {             │
 * │      String filePath = data.getStringExtra("selected_file");  │
 * │      long fileSize = data.getLongExtra("file_size", 0);       │
 * │      boolean isSuccess = data.getBooleanExtra("is_success", false); │
 * │    }                                                           │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>RxBus事件传递流程</h3>
 * <pre>
 * ┌───────────────────────────────────────────────────────────────┐
 * │              RxBus事件传递流程                                 │
 * ├───────────────────────────────────────────────────────────────┤
 * │                                                               │
 * │  Step 1: 其他Activity返回结果                                 │
 * │    └─ setResult(resultCode, data)                             │
 * │    └─ finish()                                                │
 * │                                                               │
 * │  Step 2: MainActivity接收回调                                 │
 * │    └─ onActivityResult(requestCode, resultCode, data)         │
 * │                                                               │
 * │  Step 3: 创建ActivityMsgResult消息                            │
 * │    └─ new ActivityMsgResult(requestCode, resultCode, data)    │
 * │      - 封装三个关键参数                                        │
 * │                                                               │
 * │  Step 4: 发送RxBus事件                                        │
 * │    └─ RxBus.getInstance().post(                               │
 * │         RxEnum.ACTIVITY_ACTIVITYRESULT,                       │
 * │         new ActivityMsgResult(requestCode, resultCode, data)) │
 * │      - 事件类型：ACTIVITY_ACTIVITYRESULT                      │
 * │      - 消息对象：ActivityMsgResult                            │
 * │                                                               │
 * │  Step 5: 接收事件                                             │
 * │    └─ RxBus订阅者收到事件                                      │
 * │      - 多个组件可以订阅此事件                                  │
 * │      - 解耦通信，无需直接依赖MainActivity                      │
 * │                                                               │
 * │  Step 6: 处理返回结果                                         │
 * │    └─ 订阅者根据消息执行相应操作：                              │
 * │      - 判断requestCode确定请求来源                            │
 * │      - 判断resultCode确定操作结果                              │
 * │      - 从data中提取返回数据                                    │
 * │      - 执行后续操作                                            │
 * │                                                               │
 * └───────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h3>使用场景</h3>
 * <ul>
 *   <li><b>文件选择</b>：用户选择文件后返回文件路径</li>
 *   <li><b>图片选择</b>：用户选择图片后返回图片URI</li>
 *   <li><b>设置修改</b>：用户修改设置后返回设置结果</li>
 *   <li><b>权限请求</b>：用户授权后返回权限结果</li>
 *   <li><b>数据输入</b>：用户输入数据后返回输入结果</li>
 * </ul>
 * 
 * <h3>依赖关系</h3>
 * <ul>
 *   <li>{@link Intent} - Android Intent类，用于携带返回的数据</li>
 *   <li>{@link MainActivity#onActivityResult(int, int, Intent)} - 主Activity的结果回调方法</li>
 *   <li>{@link com.micsig.tbook.tbookscope.rxjava.RxBus} - RxBus事件总线，传递消息</li>
 *   <li>{@link com.micsig.tbook.tbookscope.rxjava.RxEnum#ACTIVITY_ACTIVITYRESULT} - 事件类型枚举</li>
 * </ul>
 * 
 * <h3>设计模式</h3>
 * <p>本类采用DTO（Data Transfer Object）设计模式，作为消息载体在组件间传递数据。
 * 封装了Activity结果回调的三个关键参数，提供完整的getter/setter方法。
 * 配合RxBus事件总线，实现观察者模式的解耦通信。</p>
 * 
 * <h3>注意事项</h3>
 * <ul>
 *   <li><b>数据完整性</b>：确保三个参数都被正确封装</li>
 *   <li><b>空数据处理</b>：data可能为null，需要判断</li>
 *   <li><b>结果码判断</b>：使用标准结果码或自定义结果码</li>
 *   <li><b>请求码匹配</b>：在订阅者中根据requestCode判断请求来源</li>
 * </ul>
 * 
 * @see Intent
 * @see MainActivity#onActivityResult(int, int, Intent)
 * @see com.micsig.tbook.tbookscope.rxjava.RxBus
 * @see com.micsig.tbook.tbookscope.rxjava.RxEnum#ACTIVITY_ACTIVITYRESULT
 * @author Micsig智能示波器团队
 * @version 2.0
 * @since 1.0
 */
public class ActivityMsgResult {   // Activity结果消息数据类：封装Activity结果回调的数据
    
    /**
     * 请求码 - 用于标识请求来源
     * 
     * <p>请求码用于区分不同的Activity启动请求。
     * 在MainActivity中启动其他Activity时传入requestCode，
     * 在onActivityResult()回调中根据requestCode判断请求来源。</p>
     * 
     * <h4>取值范围</h4>
     * <ul>
     *   <li><b>正整数</b>: 通常使用常量定义，如REQUEST_CODE_PICK_FILE = 1</li>
     *   <li><b>唯一性</b>: 每个请求码应唯一，避免冲突</li>
     * </ul>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>文件选择请求：REQUEST_CODE_PICK_FILE</li>
     *   <li>图片选择请求：REQUEST_CODE_PICK_IMAGE</li>
     *   <li>设置修改请求：REQUEST_CODE_SETTINGS</li>
     *   <li>权限请求：REQUEST_CODE_PERMISSION</li>
     * </ul>
     */
    private int requestCode;   // 请求码：用于标识请求来源
    
    /**
     * 结果码 - 用于标识操作结果
     * 
     * <p>结果码用于标识Activity操作的结果。
     * 由被启动的Activity设置，在onActivityResult()回调中判断操作是否成功。</p>
     * 
     * <h4>标准结果码</h4>
     * <table border="1">
     *   <tr><th>结果码</th><th>值</th><th>说明</th></tr>
     *   <tr><td>RESULT_OK</td><td>-1</td><td>操作成功</td></tr>
     *   <tr><td>RESULT_CANCELED</td><td>0</td><td>操作取消</td></tr>
     *   <tr><td>RESULT_FIRST_USER</td><td>1</td><td>自定义结果码起始值</td></tr>
     * </table>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>RESULT_OK：用户完成了请求的操作</li>
     *   <li>RESULT_CANCELED：用户取消了操作或按返回键</li>
     *   <li>自定义结果码：应用定义的特殊结果</li>
     * </ul>
     */
    private int resultCode;   // 结果码：用于标识操作结果（RESULT_OK、RESULT_CANCELED等）
    
    /**
     * Intent数据 - 用于携带返回的数据
     * 
     * <p>Intent用于携带Activity返回的数据。
     * 由被启动的Activity设置，包含返回的键值对数据。
     * 可能为null，需要在使用前判断。</p>
     * 
     * <h4>数据类型</h4>
     * <ul>
     *   <li><b>基本类型</b>: int、long、float、double、boolean</li>
     *   <li><b>字符串</b>: String、String[]</li>
     *   <li><b>复合类型</b>: Bundle、Parcelable、Serializable</li>
     *   <li><b>URI</b>: Uri（用于文件、图片等）</li>
     * </ul>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>文件路径：data.getStringExtra("selected_file")</li>
     *   <li>文件大小：data.getLongExtra("file_size", 0)</li>
     *   <li>图片URI：data.getData() 或 data.getParcelableExtra("image_uri")</li>
     *   <li>布尔标志：data.getBooleanExtra("is_success", false)</li>
     * </ul>
     */
    private Intent data;   // Intent数据：用于携带返回的数据，可能为null

    /**
     * 构造函数 - 创建Activity结果消息
     * 
     * <p>创建ActivityMsgResult实例，封装Activity结果回调的三个关键参数。
     * 在MainActivity.onActivityResult()方法中创建并发送到RxBus。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>requestCode</td><td>int</td><td>请求码，用于标识请求来源</td></tr>
     *   <tr><td>resultCode</td><td>int</td><td>结果码，用于标识操作结果</td></tr>
     *   <tr><td>data</td><td>Intent</td><td>返回的数据，可能为null</td></tr>
     * </table>
     * 
     * <h4>构造示例</h4>
     * <pre>
     * ┌─────────────────────────────────────────┐
     * │  构造示例                               │
     * ├─────────────────────────────────────────┤
     * │                                         │
     * │  // 在MainActivity.onActivityResult()中 │
     * │  ActivityMsgResult result =             │
     * │    new ActivityMsgResult(               │
     * │      requestCode,                       │
     * │      resultCode,                        │
     * │      data                               │
     * │    );                                   │
     * │                                         │
     * │  // 发送事件                            │
     * │  RxBus.getInstance().post(              │
     * │    RxEnum.ACTIVITY_ACTIVITYRESULT,      │
     * │    result                               │
     * │  );                                     │
     * │                                         │
     * └─────────────────────────────────────────┘
     * </pre>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>MainActivity.onActivityResult()方法中创建</li>
     *   <li>创建后立即通过RxBus发送给订阅者</li>
     * </ul>
     * 
     * @param requestCode 请求码，用于标识请求来源
     * @param resultCode 结果码，用于标识操作结果
     * @param data 返回的数据，可能为null
     * @see MainActivity#onActivityResult(int, int, Intent)
     */
    public ActivityMsgResult(int requestCode, int resultCode, Intent data) {   // 构造函数：创建Activity结果消息，封装三个关键参数
        this.requestCode = requestCode;   // 设置requestCode属性：传入的请求码值
        this.resultCode = resultCode;   // 设置resultCode属性：传入的结果码值
        this.data = data;   // 设置data属性：传入的Intent数据
    }   // 构造函数结束

    /**
     * 获取请求码
     * 
     * <p>返回Activity启动请求的请求码。</p>
     * 
     * <h4>返回值说明</h4>
     * <p>返回请求码，用于判断请求来源。</p>
     * 
     * <h4>调用时机</h4>
     * <p>订阅者收到ActivityMsgResult消息后调用此方法，判断请求来源。</p>
     * 
     * @return 请求码，用于标识请求来源
     */
    public int getRequestCode() {   // 方法：获取请求码
        return requestCode;   // 返回requestCode属性值
    }   // getRequestCode方法结束

    /**
     * 设置请求码
     * 
     * <p>设置Activity启动请求的请求码。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>requestCode</td><td>int</td><td>请求码，用于标识请求来源</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>动态修改请求码时调用</li>
     *   <li>通常在构造时设置，很少需要动态修改</li>
     * </ul>
     * 
     * @param requestCode 请求码，用于标识请求来源
     */
    public void setRequestCode(int requestCode) {   // 方法：设置请求码
        this.requestCode = requestCode;   // 设置requestCode属性值
    }   // setRequestCode方法结束

    /**
     * 获取结果码
     * 
     * <p>返回Activity操作的结果码。</p>
     * 
     * <h4>返回值说明</h4>
     * <ul>
     *   <li><b>RESULT_OK (-1)</b>: 操作成功</li>
     *   <li><b>RESULT_CANCELED (0)</b>: 操作取消</li>
     *   <li><b>其他值</b>: 自定义结果码</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <p>订阅者收到ActivityMsgResult消息后调用此方法，判断操作结果。</p>
     * 
     * @return 结果码，用于标识操作结果
     */
    public int getResultCode() {   // 方法：获取结果码
        return resultCode;   // 返回resultCode属性值
    }   // getResultCode方法结束

    /**
     * 设置结果码
     * 
     * <p>设置Activity操作的结果码。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>resultCode</td><td>int</td><td>结果码，用于标识操作结果</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>动态修改结果码时调用</li>
     *   <li>通常在构造时设置，很少需要动态修改</li>
     * </ul>
     * 
     * @param resultCode 结果码，用于标识操作结果
     */
    public void setResultCode(int resultCode) {   // 方法：设置结果码
        this.resultCode = resultCode;   // 设置resultCode属性值
    }   // setResultCode方法结束

    /**
     * 获取Intent数据
     * 
     * <p>返回Activity返回的Intent数据。</p>
     * 
     * <h4>返回值说明</h4>
     * <ul>
     *   <li><b>非null</b>: 包含返回数据的Intent对象</li>
     *   <li><b>null</b>: 无返回数据</li>
     * </ul>
     * 
     * <h4>调用时机</h4>
     * <p>订阅者收到ActivityMsgResult消息后调用此方法，提取返回数据。</p>
     * 
     * <h4>数据提取示例</h4>
     * <pre>
     * Intent data = result.getData();
     * if (data != null) {
     *   String filePath = data.getStringExtra("selected_file");
     *   long fileSize = data.getLongExtra("file_size", 0);
     *   boolean isSuccess = data.getBooleanExtra("is_success", false);
     * }
     * </pre>
     * 
     * @return Intent数据，可能为null
     */
    public Intent getData() {   // 方法：获取Intent数据
        return data;   // 返回data属性值
    }   // getData方法结束

    /**
     * 设置Intent数据
     * 
     * <p>设置Activity返回的Intent数据。</p>
     * 
     * <h4>参数说明</h4>
     * <table border="1">
     *   <tr><th>参数</th><th>类型</th><th>说明</th></tr>
     *   <tr><td>data</td><td>Intent</td><td>返回的数据，可能为null</td></tr>
     * </table>
     * 
     * <h4>调用时机</h4>
     * <ul>
     *   <li>动态修改返回数据时调用</li>
     *   <li>通常在构造时设置，很少需要动态修改</li>
     * </ul>
     * 
     * @param data Intent数据，可能为null
     */
    public void setData(Intent data) {   // 方法：设置Intent数据
        this.data = data;   // 设置data属性值
    }   // setData方法结束

    /**
     * 获取字符串表示 - 用于调试和日志输出
     * 
     * <p>返回ActivityMsgResult对象的字符串表示，包含三个关键参数。
     * 主要用于调试、日志输出和状态追踪。</p>
     * 
     * <h4>返回格式</h4>
     * <pre>
     * ActivityMsgResult{requestCode=1, resultCode=-1, data=Intent { ... }}
     * </pre>
     * 
     * <h4>使用场景</h4>
     * <ul>
     *   <li>调试时打印消息状态</li>
     *   <li>日志记录Activity结果</li>
     *   <li>追踪RxBus事件传递</li>
     * </ul>
     * 
     * @return 字符串表示，格式为"ActivityMsgResult{requestCode=值, resultCode=值, data=值}"
     */
    @Override   // 重写Object类的toString方法
    public String toString() {   // 方法：获取字符串表示，用于调试和日志输出
        return "ActivityMsgResult{" +   // 返回字符串：类名 + 开括号
                "requestCode=" + requestCode +   // 添加requestCode属性值
                ", resultCode=" + resultCode +   // 添加resultCode属性值
                ", data=" + data +   // 添加data属性值
                '}';   // 添加闭括号
    }   // toString方法结束
}   // ActivityMsgResult类结束